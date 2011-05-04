package us.paperlesstech

import us.paperlesstech.helpers.FileHelpers

public enum MimeType {
	PCL(["application/pcl", "application/vnd.hp-pcl"] as Set, ["pcl"] as Set),
	PNG(["image/png"] as Set, ["png"] as Set),
	PDF(["application/pdf", "application/x-pdf"] as Set, ["pdf"] as Set)

	private final Set mimeTypes
	private final Set fileExtensions

	MimeType(Set mimeTypes, Set fileExtensions) {
		this.mimeTypes = mimeTypes
		this.fileExtensions = fileExtensions
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