package us.paperlesstech

import grails.plugin.spock.IntegrationSpec
import java.security.SecureRandom
import spock.lang.Shared
import spock.lang.Unroll

class FileServiceIntegrationSpec extends IntegrationSpec {
	def fileService
	def partyService
	// If more tests are added and this is parallelized this will break
	@Shared
	String randomText
	@Shared
	def testFile
	@Shared
	def testInputStream
	@Shared
	def testBytes

	def setupSpec() {
		SecureRandom random = new SecureRandom()
		randomText = new BigInteger(130, random).toString(32)
		testFile = File.createTempFile("test", "test")
		testFile.text = randomText
		testInputStream = new ByteArrayInputStream(randomText.bytes)
		testBytes = randomText.bytes
	}

	@Unroll("testing fileService with #bytes | #file | #inputStream | #mimeType | #pages | #dateCreated")
	def "test fileService"() {
		when:
		def dd = null
		def exception = false
		try {
			dd = fileService.createDocumentData(bytes: bytes, file: file, inputStream: inputStream,
					mimeType: mimeType, pages: pages, dateCreated: dateCreated)
		} catch (Exception e) {
			exception = true
		}

		then:
		verifyCreatedDocument(dd, mimeType, pages, dateCreated)

		where:
		bytes     | file     | inputStream     | mimeType     | pages | dateCreated
		testBytes | null     | null            | MimeType.PCL | 2     | new GregorianCalendar(2011, 7, 5).time
		null      | testFile | null            | MimeType.PCL | 2     | new GregorianCalendar(2011, 7, 5).time
		null      | null     | testInputStream | MimeType.PCL | 2     | new GregorianCalendar(2011, 7, 5).time
	}

	void verifyCreatedDocument(DocumentData dd, MimeType mimeType, int pages, Date dateCreated) {
		assert dd.mimeType == mimeType
		assert dd.pages == pages
		assert dd.dateCreated == dateCreated
		assert dd.fileKey
		assert dd.fileSize == randomText.length()
		assert fileService.getBytes(dd) == randomText.bytes
		assert fileService.getText(dd) == randomText
		assert fileService.getInputStream(dd).text == randomText
		fileService.withInputStream(dd) { is ->
			assert is.text == randomText
		}
		String path = fileService.getAbsolutePath(dd)
		assert path.contains(dd.fileKey)
		File f = new File(path)
		assert f.canRead()
		assert f.text == randomText

		// Test that the file can be downloaded
		f.delete()
		// This will have to download the file to succeed
		assert fileService.getText(dd) == randomText
		String newPath = fileService.getAbsolutePath(dd)
		assert newPath == path
		f = new File(newPath)
		assert f.canRead()
	}
}
