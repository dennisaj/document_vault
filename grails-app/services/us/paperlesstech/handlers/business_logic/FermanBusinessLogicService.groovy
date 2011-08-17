package us.paperlesstech.handlers.business_logic

import us.paperlesstech.handlers.Handler
import us.paperlesstech.handlers.PclHandlerService
import us.paperlesstech.helpers.PclDocument
import java.util.regex.Matcher
import java.util.regex.Pattern
import us.paperlesstech.TagService

class FermanBusinessLogicService {
	static transactional = true
	PclHandlerService pclHandlerService
	TagService tagService

	enum FermanDocumentTypes {
		CustomerHardCopy(~/20901|20913/),
		WarrantyRepairOrder(~/206/),
		ServiceInvoice(~/20813/),
		Other(null)

		private final Pattern pattern

		private FermanDocumentTypes(Pattern pattern) {
			this.pattern = pattern
		}

		boolean matches(String input) {
			pattern?.matcher(input)?.matches()
		}
	}

	void afterPCLImportFile(Map input) {
		def d = Handler.getDocument(input)
		def savedDocument = d.save()

		if (savedDocument) {
			["RO_Number", "VIN"].each {
				def value = savedDocument.searchField(it)
				if (value) {
					log.info "Creating tag $value"
					tagService.createTag(value)
					savedDocument.addTag(value)
					log.info "Added tag $value to document $d"
				}
			}
			Handler.setDocument(input, savedDocument)
		}
	}

	void beforePCLImportFile(Map input) {
		def d = Handler.getDocument(input)
		def pclDocument = input.pclDocument
		assert pclDocument

		FermanDocumentTypes t = getDocumentType(pclDocument)

		def m = [:]
		switch (t) {
			case FermanDocumentTypes.Other:
				m.raw = parseOther(pclDocument)
				break
			case FermanDocumentTypes.WarrantyRepairOrder:
				m = parseWarrantyRepairOrder(pclDocument)
				break
			case FermanDocumentTypes.CustomerHardCopy:
				m = parseCustomerHardCopy(pclDocument)
				break
			case FermanDocumentTypes.ServiceInvoice:
				m = parseServiceInvoice(pclDocument)
				break
			default:
				throw new IllegalArgumentException("Unknown type: $t")
		}
		m.DocumentType = t.toString()

		sanitizeAll(m)

		m.each { key, value ->
			if (key && value) {
				d.searchField(key, value)
			}
		}

		log.debug "Imported fields ($m)"
	}

	public FermanDocumentTypes getDocumentType(PclDocument pclDocument) {
		String macro = pclDocument.pages[0].macro
		FermanDocumentTypes type = FermanDocumentTypes.values().find { it.matches(macro) } ?: FermanDocumentTypes.Other

		type
	}

	private String getField(String line, int start, int end) {
		int length = line.length()
		if (start > length) {
			return ""
		}

		return line[start..<Math.min(end + 1, length)].trim()
	}

	Map parseWarrantyRepairOrder(PclDocument pclDocument) {
		def lines = pclDocument.pages[0].pageData.readLines()

		lines = lines.reverse()

		def m = [:]

		def line
		while (lines && !line?.trim()) {
			line = lines.pop()
		}

		m["Work_Phone"] = getField(line, 40, 54)
		m["RO_Open_Date"] = getField(line, 55, 67)
		m["RO_Number"] = cleanRO(getField(line, 68, 79))

		line = lines.pop()
		m["Customer_Name"] = getField(line, 0, 39)

		line = lines.pop()
		m["Customer_Address"] = getField(line, 0, 39)
		m["Home_Phone"] = getField(line, 40, 54)
		m["RO_Close_Date"] = getField(line, 55, 67)
		m["Cross_Reference_Number"] = getField(line, 68, 79)

		line = lines.pop()
		m["Customer_Address"] = (m["Customer_Address"] + "\n" + getField(line, 0, 39)).trim()

		line = lines.pop()
		m["Customer_Address"] = (m["Customer_Address"] + "\n" + getField(line, 0, 39)).trim()
		m["Body"] = getField(line, 40, 54)
		m["Mileage_In"] = getField(line, 55, 67)
		m["Mileage_Out"] = getField(line, 68, 79)

		lines.pop()
		line = lines.pop()
		m["Model_Year"] = getField(line, 0, 9)
		m["Make"] = getField(line, 10, 24)
		m["Model"] = getField(line, 25, 39)
		m["License_Number"] = getField(line, 40, 54)
		m["Service_Advisor"] = getField(line, 55, 79)

		lines.pop()
		line = lines.pop()
		m["VIN"] = getField(line, 0, 24)
		m["Color"] = getField(line, 25, 39)
		m["Delivery_Date"] = getField(line, 55, 67)
		m["In_Service_Date"] = getField(line, 68, 79)

		m["raw"] = trimTokens(lines.join("\n"))

		m
	}

	Map parseServiceInvoice(PclDocument pclDocument) {
		def lines = pclDocument.pages[0].pageData.readLines()

		lines = lines.reverse()

		def m = [:]

		def line
		while (lines && !line?.trim()) {
			line = lines.pop()
		}

		m["RO_Open_Date"] = getField(line, 55, 67)
		m["RO_Number"] = cleanRO(getField(line, 68, 79))

		// Skip one line
		lines.pop()
		line = lines.pop()
		m["RO_Close_Date"] = getField(line, 55, 67)
		m["Status"] = getField(line, 68, 79)

		lines.pop()
		line = lines.pop()
		m["Mileage_In"] = getField(line, 55, 67)
		m["Mileage_Out"] = getField(line, 68, 79)

		lines.pop()
		line = lines.pop()
		m["Service_Advisor"] = getField(line, 55, 79)

		line = lines.pop()
		m["Customer_Name"] = getField(line, 0, 35)

		line = lines.pop()
		m["Customer_Address"] = getField(line, 0, 35)
		m["Work_Phone"] = getField(line, 36, 54)
		m["VIN"] = getField(line, 55, 79)

		line = lines.pop()
		m["Customer_Address"] = (m["Customer_Address"] + "\n" + getField(line, 0, 35)).trim()

		line = lines.pop()
		m["Customer_Address"] = (m["Customer_Address"] + "\n" + getField(line, 0, 35)).trim()
		m["Home_Phone"] = getField(line, 36, 54)
		m["Delivery_Date"] = getField(line, 55, 67)
		m["In_Service_Date"] = getField(line, 68, 79)

		lines.pop()
		line = lines.pop()
		m["Model_Year"] = getField(line, 0, 5)
		m["Make"] = getField(line, 6, 20)
		m["Model"] = getField(line, 21, 35)
		m["Body"] = getField(line, 36, 54)
		m["Color"] = getField(line, 55, 67)
		m["License_Number"] = getField(line, 68, 79)

		m["raw"] = trimTokens(lines.join("\n"))

		m
	}

	Map parseCustomerHardCopy(PclDocument pclDocument) {
		// Since the data that we are extracting is duplicated on each page, currently we only extract the first page
		def lines = pclDocument.pages[0].pageData.readLines()

		lines = lines.reverse()

		def m = [:]

		// Either the first three or first four lines are blank (we have samples of each)
		lines.pop()
		lines.pop()
		lines.pop()
		def line = lines.pop()
		if (!line.trim())
			line = lines.pop()
		m["RO_Open_Date"] = getField(line, 55, 67)
		m["RO_Number"] = cleanRO(getField(line, 68, 79))

		// Skip one line
		lines.pop()
		line = lines.pop()
		m["Time_Received"] = getField(line, 55, 67)
		m["Time_Promised"] = getField(line, 68, 79)

		lines.pop()
		line = lines.pop()
		m["Current_Mileage"] = getField(line, 55, 67)
		m["Mileage_Out"] = getField(line, 68, 79)

		lines.pop()
		line = lines.pop()
		m["Estimate_Of_Repairs"] = getField(line, 40, 54)
		m["Service_Advisor"] = getField(line, 55, 79)

		lines.pop()
		line = lines.pop()
		def nameAddress = []
		nameAddress += getField(line, 0, 35)

		line = lines.pop()
		nameAddress += getField(line, 0, 35)
		m["Work_Phone"] = getField(line, 36, 54)
		m["VIN"] = getField(line, 55, 79)

		line = lines.pop()
		nameAddress += getField(line, 0, 35)

		line = lines.pop()
		nameAddress += getField(line, 0, 35)
		m["Home_Phone"] = getField(line, 36, 54)
		m["Delivery_Date"] = getField(line, 55, 67)
		m["In_Service_Date"] = getField(line, 68, 79)

		// TODO evaluate how to parse the customer name and address
		// TODO for now assuming line 1 is the customer name and the rest is the address
		if (nameAddress.size() >= 1) {
			m["Customer_Name"] = nameAddress[0]
		}

		if (nameAddress.size() >= 2) {
			m["Customer_Address"] = nameAddress[1..<nameAddress.size()].join("\n").trim()
		}

		// Skip one line
		lines.pop()
		line = lines.pop()
		m["Model_Year"] = getField(line, 0, 5)
		m["Make"] = getField(line, 6, 20)
		m["Model"] = getField(line, 21, 35)
		m["Body"] = getField(line, 36, 54)
		m["Color"] = getField(line, 55, 67)
		m["License_Number"] = getField(line, 68, 79)

		m["raw"] = trimTokens(lines.join("\n"))

		m
	}

	String parseOther(PclDocument pclDocument) {
		StringBuilder builder = new StringBuilder()
		pclDocument.pages.each {
			builder << it.pageData
		}
		String data = builder.toString()

		trimTokens(data)
	}

	/**
	 * Replaces \ with / because the searchable plugin doesn't escape them
	 *
	 * @param input The string to sanitize
	 *
	 * @return The input string with all \ replaced by /
	 */
	private String sanitize(input) {
		input.replaceAll(/\x5c/, "/")
	}

	/**
	 * Replaces every value in the map with the value sanitized by #sanitize
	 *
	 * @param m The map to sanitize
	 *
	 * @return The original map with the values replaced
	 */
	private Map sanitizeAll(Map m) {
		m.each { k, v ->
			m[k] = sanitize(v)
		}
	}

	/**
	 * Separate a block of text into individual tokens then mash
	 * them back together in a newline delimited string.
	 */
	private String trimTokens(input) {
		def m = input =~ /(?m)(\S+)/
		def lines = m*.getAt(1)

		lines.join("\n")
	}

	private String cleanRO(String RO) {
		def idx = RO.indexOf('/')
		if (idx >= 0) {
			RO = RO[0..<idx]
		}

		RO
	}
}
