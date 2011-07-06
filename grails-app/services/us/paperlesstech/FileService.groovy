package us.paperlesstech

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import org.apache.shiro.crypto.hash.Sha256Hash
import org.springframework.beans.factory.InitializingBean

class FileService implements InitializingBean {
	static transactional = false
	def grailsApplication
	private String bucket
	private String s3CachePath
	private AWSCredentials credentials
	private String diskCache

	/**
	 * Initializes the object after Spring has injected the beans.  This should only be called by Spring.
	 */
	void afterPropertiesSet() {
		bucket = grailsApplication.config.document_vault.aws.s3.bucket
		assert bucket
		diskCache = grailsApplication.config.document_vault.files.cache
		assert diskCache
		String accessKey = grailsApplication.config.document_vault.aws.credentials.accessKey
		assert accessKey
		String secretKey = grailsApplication.config.document_vault.aws.credentials.secretKey
		assert secretKey
		credentials = new BasicAWSCredentials(accessKey, secretKey)
		s3CachePath = grailsApplication.config.document_vault.aws.s3.cachePath
		assert s3CachePath
	}

	/**
	 * Creates and returns a document data object, storing the data in S3 and caching a local copy.
	 *
	 * @param args a map of arguments to the method.  One of (file, inputStream, and bytes) is required
	 * 		file: If present the File to use for the data.
	 * 		inputStream: If present the InputStream to use for the data.
	 * 		bytes: If present the byte[] to use for the data.
	 * 		mimeType: The mimeType of the data. (required)
	 * 		pages: The number of pages of the data, defaults to 1.
	 * 		dateCreated: The date to use for the document data, defaults to now.
	 * @return
	 */
	DocumentData createDocumentData(Map args) {
		File file = args.file
		byte[] bytes = args.bytes
		InputStream inputStream = args.inputStream
		MimeType mimeType = args.mimeType
		assert mimeType
		int pages = args.pages ?: 1
		Date dateCreated = args.dateCreated ?: new Date()
		assert file || bytes || inputStream

		if (file) {
			bytes = file.bytes
		}

		if (inputStream) {
			bytes = inputStream.bytes
		}

		String fileKey = getHash(bytes)
		int fileSize = bytes.length

		DocumentData dd = DocumentData.findByFileKey(fileKey)
		if (dd) {
			return dd
		}

		File cached = getLocalFile(fileKey, mimeType)
		createFileConcurrently(cached) { tmpFile ->
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes)
			AmazonS3Client s3Client = new AmazonS3Client(credentials)
			ObjectMetadata metadata = new ObjectMetadata()
			metadata.setContentType(mimeType.downloadContentType)
			s3Client.putObject(bucket, getS3Name(fileKey, mimeType), bais, metadata)
			bais.close()

			tmpFile.bytes = bytes

			assert tmpFile.length() == fileSize
		}

		dd = DocumentData.findByFileKey(fileKey)
		if (dd) {
			return dd
		}

		// We set the date when creating the DocumentData rather than on insert to prevent ordering problems
		// with two documents being added to the file at the same time, like on PCL import
		dd = new DocumentData(mimeType: mimeType, fileKey: fileKey, fileSize: fileSize, pages: pages, dateCreated: dateCreated)
		dd
	}

	private File createFileConcurrently(File protectedFile, Closure closure) {
		File tmp = File.createTempFile("fileService", ".tmp")
		closure.call(tmp)
		tmp.renameTo(protectedFile)
		protectedFile
	}

	/**
	 * Returns the absolute path to the cached version of the document data. This file should not be modified.
	 *
	 * @param dd The document to lookup
	 *
	 * @return The absolute path of the cached version of the document data.
	 */
	String getAbsolutePath(DocumentData dd) {
		assert dd.fileKey

		File f = getFile(dd.fileKey, dd.mimeType)
		f.absolutePath
	}

	/**
	 * Returns the bytes from the file represented by the passed document data.
	 *
	 * @param documentData Looks up the file represented by this object.
	 *
	 * @return a byte[] of the contents of the file
	 */
	byte[] getBytes(DocumentData documentData) {
		getFile(documentData.fileKey, documentData.mimeType).bytes
	}

	private String getHash(byte[] bytes) {
		new Sha256Hash(bytes).toHex()
	}

	/**
	 * Retrieves the file from the local cache if it is present or pulls it from S3 if it is not
	 */
	private File getFile(String fileKey, MimeType mimeType) {
		File cached = getLocalFile(fileKey, mimeType)
		if (cached.canRead()) {
			return cached
		}

		AmazonS3Client s3 = new AmazonS3Client(credentials)
		createFileConcurrently(cached) { file ->
			GetObjectRequest request = new GetObjectRequest(bucket, getS3Name(fileKey, mimeType))
			s3.getObject(request, file)
			assert getHash(file.bytes) == fileKey
		}

		cached
	}

	/**
	 * Returns an InputStream of the file represented by the passed document data. NOTE: the caller must close this.
	 *
	 * @param documentData Looks up the file reprsented by this object
	 *
	 * @return A BufferedInputStream from the represented file
	 */
	InputStream getInputStream(DocumentData documentData) {
		getFile(documentData.fileKey, documentData.mimeType).newInputStream()
	}

	private File getLocalFile(String hash, MimeType mimeType) {
		new File(diskCache, "${hash}${mimeType.downloadExtension}".toString())
	}

	private String getS3Name(String hash, MimeType mimeType) {
		"$s3CachePath/${hash}${mimeType.downloadExtension}".toString()
	}

	/**
	 * Returns the text from the file represented by the passed document data.
	 *
	 * @param documentData Looks up the file represented by this object.
	 *
	 * @return all text from the contents of the file
	 */
	String getText(DocumentData documentData) {
		getFile(documentData.fileKey, documentData.mimeType).text
	}

	/**
	 * Calls the passed closure passing it an InputStream to the file represented by the document data.  The input
	 * stream will be closed after the closure is called.
	 *
	 * @param documentData Looks up the file represented by this object.
	 * @param closure Called and passed the InputStream as an argument
	 *
	 * @return returns the return value of the closure
	 */
	def withInputStream(DocumentData documentData, Closure closure) {
		InputStream is = getInputStream(documentData)
		is.withStream {
			closure.call(is)
		}
	}
}
