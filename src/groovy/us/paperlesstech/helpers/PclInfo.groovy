package us.paperlesstech.helpers

import org.pcl.parser.Data
import org.pcl.parser.PCLCommand
import org.pcl.parser.PCLParser
import org.pcl.parser.ParserListener

class PclInfo implements ParserListener {
	PrintWriter pw
	boolean inMacro = false
	def currentDocument = new PclDocument(startPage: 1)
	def currentPage = new PclPage()

	/**
	 * A list of the documents found in the parsed PCL
	 */
	def documents = new ArrayList<PclDocument>()
	int page = 1

	def parse(Map map) {
		def pclFile = map.pclFile
		def data = map.data
		def logFile = map.logFile
		assert pclFile || data

		def p = {
			def parser = new PCLParser()
			parser.addListener(this)
			if (pclFile) {
				parser.parse(pclFile)
			} else if (data) {
				parser.parseByteArray(data)
			}
		}

		if (logFile) {
			new File(logFile).withPrintWriter(p)
		} else {
			p.call()
		}

		assert documents
		documents.each { document ->
			assert document.startPage > 0
			assert document.endPage > 0
			assert document.pages.size() > 0
			document.pages.each { page ->
				assert page.pageData
			}
		}
	}

	void command(long position, PCLCommand cmd) {
		if (pw) {
			pw.println("$position:cmd:$cmd")
		}

		// Check for start and end macro commands
		if (cmd.commandSignature == '&fX') {
			if (cmd.value == "0") {
				inMacro = true
			} else if (cmd.value == "1") {
				inMacro = false
			} else if (cmd.value.endsWith("y3")) {
				currentPage.macro = cmd.value[0..<cmd.value.indexOf('y')]
			}
			// Check for resetting the printer state
		} else if (cmd.commandSignature == '%X' && cmd.value == "-12345") {
			// If there is data in the current page add it to the current document
			if (currentDocument.pages || currentPage.pageData) {
				if (currentPage.pageData) {
					currentDocument.pages.push(currentPage)
				}
				documents.push(currentDocument)
			}

			currentPage = new PclPage()
			currentDocument = new PclDocument(startPage: page)
		}
	}

	void data(long position, Data data) {
		if (pw) {
			pw.println("$position:data:$data")
		}

		// Ignore data in a macro
		if (inMacro) {
			return
		}

		// The original data from the PCL
		String origData = new String(data.bytes)

		// Check to see if the data contains any non empty lines that don't start with @PJL
		// This is looking for actual text that would be printed on the document to skip blank pages
		boolean nonBlankPage = origData.readLines().any { line ->
			String trimmed = line.trim()
			trimmed && !trimmed.startsWith("@PJL")
		}

		if (nonBlankPage) {
			currentDocument.endPage = page
			currentPage << origData
		}

		// If we weren't in a macro, increment the page count by the number of form feed characters in data
		int pageBreakCount = data.bytes.count(12)
		if (pageBreakCount && currentPage.pageData) {
			currentDocument.pages.push(currentPage)
			currentPage = new PclPage()
		}

		page += pageBreakCount
	}
};

class PclDocument {
	int endPage
	List<PclPage> pages = []
	int startPage
};

class PclPage {
	String macro
	private def buffer = new StringBuilder()
	private def cache

	String getPageData() {
		if (cache) {
			return cache
		} else {
			cache = buffer.toString()
			return cache
		}
	}

	void setPageData(String pageData) {
		buffer = new StringBuilder(pageData)
		cache = pageData
	}

	void leftShift(String data) {
		buffer << data
		cache = null
	}
};
