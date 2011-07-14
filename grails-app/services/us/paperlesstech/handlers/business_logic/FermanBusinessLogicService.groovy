package us.paperlesstech.handlers.business_logic

import us.paperlesstech.handlers.Handler
import us.paperlesstech.handlers.PclHandlerService

class FermanBusinessLogicService {
	static transactional = true
	PclHandlerService pclHandlerService

	enum FermanDocumentTypes {
		CustomerHardCopy,
		Other
	}

	void afterPCLImportFile(Map input) {
		def d = Handler.getDocument(input)
		def savedDocument = d.save()

		if (savedDocument) {
			["RO_Number", "VIN"].each {
				def value = savedDocument.searchField(it)
				if (value) {
					log.info "Creating tag $value"
					savedDocument.addTag(value)
					log.info "Added tag $value to document $d"
				}
			}
			Handler.setDocument(input, savedDocument)
		}
	}

	void beforePCLImportFile(Map input) {
		def d = Handler.getDocument(input)

		FermanDocumentTypes t = getDocumentType(input.bytes)

		def m = [:]
		switch (t) {
			case FermanDocumentTypes.Other:
				m.raw = parseOther(input.bytes)
				break
			case FermanDocumentTypes.CustomerHardCopy:
				m = parseCustomerHardCopy(input.bytes)
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

	public FermanDocumentTypes getDocumentType(byte[] bytes) {
		String data = pclHandlerService.pclToString(bytes, false)

		FermanDocumentTypes type = FermanDocumentTypes.Other
		if (data.contains("&f20901y") || data.contains("&f20913y")) {
			type = FermanDocumentTypes.CustomerHardCopy
		}

		type
	}

	private String getField(String line, int start, int end) {
		int length = line.length()
		if (start > length) {
			return ""
		}

		return line[start..<Math.min(end + 1, length)].trim()
	}

	Map parseCustomerHardCopy(byte[] bytes) {
		String data = pclHandlerService.pclToString(bytes)
		def lines = data.split(/\r\n|\n/).toList()

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
		m["RO_Number"] = getField(line, 68, 79)

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

		m
	}

	String parseOther(byte[] bytes) {
		String data = pclHandlerService.pclToString(bytes)

		def m = data =~ /(?m)(\S+)/
		def lines = m*.getAt(1)

		lines.join("\n")
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
}
