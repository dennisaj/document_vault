package us.paperlesstech

class SignatureService {
    static transactional = true

	/**
	 * Loads the specified document and adds the signatures to it
	 *
	 * @param signatures A map of the raw signature image data for each page of the document
	 * @param pageNumber The page number the signature goes on
	 * @param imageData Base64 encoded image of the signature
	 *
	 * @return the map with the decoded images
	 */
	def saveSignatureToMap(Map<Integer, Byte[]> signatures, pageNumber, imageData) {
		assert imageData

		def decodedData = imageData[PreviewImage.imageDataPrefix.size()..-1].decodeBase64()

		signatures[pageNumber] = decodedData
	}
}
