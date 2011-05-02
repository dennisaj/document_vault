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
}
