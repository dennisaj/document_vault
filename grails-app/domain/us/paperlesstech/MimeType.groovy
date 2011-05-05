package us.paperlesstech

import us.paperlesstech.helpers.FileHelpers

public enum MimeType {
	JPEG(["image/jpeg"], [".jpg", ".jpeg", ".jpe", ".jif", ".jfif", ".jfi"]),
	PCL(["application/pcl", "application/vnd.hp-pcl"], [".pcl"]),
	PDF(["application/pdf", "application/x-pdf"], [".pdf"]),
	PNG(["image/png"], [".png"]),
	TIFF(["image/tiff", "image/tiff-fx"], [".tff", ".tif"])

	private final List mimeTypes
	private final List fileExtensions

	MimeType(List mimeTypes, List fileExtensions) {
		this.mimeTypes = mimeTypes
		this.fileExtensions = fileExtensions
	}

	String getDownloadContentType() {
		mimeTypes[0]
	}

	String getDownloadExtension() {
		fileExtensions[0]
	}

	/**
	 * Takes a map [mimeType:"", fileName:""] and determines the mime type.  It first tries to determine it based on
	 * the mimeType and if it fails it returns the mime type based on the file extension parsed from the fileName.
	 *
	 * @param m a map [mimeType:"", fileName:""]
	 * @return The MimeType or null if it could not be determined
	 */
	static MimeType getMimeType(Map m) {
		def mimeType = m?.mimeType?.toLowerCase()
		def fileName = m?.fileName?.toLowerCase()
		def extension = fileName ? FileHelpers.getExtension(fileName) : null

		MimeType type = MimeType.values().find { it.mimeTypes.contains(mimeType) }

		if (!type) {
			type = MimeType.values().find { it.fileExtensions.contains(extension) }
		}

		type
	}
}