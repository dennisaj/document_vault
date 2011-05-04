package us.paperlesstech.helpers

class FileHelpers {
	/**
	 * Removes the extension from the file name.
	 *
	 * <pre>
	 * assert chopExtension("/tmp/test.pdf", ".pdf") == "/tmp/test"
	 * </pre>
	 *
	 * @param fileName the string name of the file
	 * @param extension the extension including the period
	 *
	 * @return The filename without the extension
	 */
	static String chopExtension(String fileName, String extension) {
		int chopLength = (extension.size() + 1) * -1
		return fileName[0..chopLength]
	}

	/**
	 * Determines the extension from the fileName.
	 *
	 * <pre>
	 * getExtension("file.pdf.png") == ".png"
	 * </pre>
	 *
	 * @param fileName the fileName to parse
	 * @return the extension or null if the extension cannot be determined
	 */
	static String getExtension(String fileName) {
		def idx = fileName.lastIndexOf('.')

		idx >= 0 ? fileName.substring(idx + 1) : null
	}
}
