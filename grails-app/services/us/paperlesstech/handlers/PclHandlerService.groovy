package us.paperlesstech.handlers

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import org.springframework.core.io.ClassPathResource
import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.helpers.PclDocument

class PclHandlerService extends Handler {
	static handlerFor = [MimeType.PCL]
	static pcl2pdf = new ClassPathResource("scripts/pcl2pdf.sh").file.absolutePath
	static transactional = true
	def handlerChain

	byte[] createPdf(DocumentData d, PclDocument pclDocument) {
		assert d.mimeType == MimeType.PCL

		String pclPath = fileService.getAbsolutePath(d)
		File pdfFile = new File(FileHelpers.chopExtension(pclPath, ".pcl") + ".pdf")

		try {
			def cmd = """/bin/bash $pcl2pdf $pclPath ${pclDocument.startPage} ${pclDocument.endPage}"""
			log.debug "PDF create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PDF conversion failed: $cmd")
			}

			return pdfFile.bytes
		} finally {
			pdfFile.delete()
		}
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)
		def pclDocument = input.pclDocument
		assert pclDocument

		def data = fileService.createDocumentData(mimeType: MimeType.PCL, bytes: input.bytes)
		d.addToFiles(data)

		input.bytes = createPdf(data, pclDocument)
		handlerChain.importFile(document: d, documentData: new DocumentData(mimeType: MimeType.PDF), bytes: input.bytes)
		input.bytes = null

		assert d.files.size() == 2
		assert d.previewImages.size() == d.files.first().pages
	}
}
