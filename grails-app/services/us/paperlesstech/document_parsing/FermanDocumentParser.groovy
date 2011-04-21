package us.paperlesstech.document_parsing

import us.paperlesstech.Document
import us.paperlesstech.DocumentType

class FermanDocumentParser extends DocumentParser {
	public static enum Types {
		CUSTOMER_HARD_COPY,
		OTHER
	}

	@Override
	public DocumentType getDocumentType(Document d) {
		assert d?.pcl?.data

		String data = pclToString(d, false)

		Types type = Types.OTHER
		if(data.contains("&f20901y")) {
			type = Types.CUSTOMER_HARD_COPY
		}

		return DocumentType.findByNameIlike("%${type.name()}%")
	}

	Map parseCustomerHardCopy(Document d) {
		assert d?.pcl?.data

		String data = pclToString(d)
		def lines = data.split(/\r\n|\n/).toList()

		lines = lines.reverse()


		def m = [:]

		// Either the first three or first four lines are blank (we have samples of each)
		lines.pop()
		lines.pop()
		lines.pop()
		def line = lines.pop()
		if(!line.trim())
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
		if(nameAddress.size() >= 1) {
			m["Customer_Name"] = nameAddress[0]
		}

		if(nameAddress.size() >= 2) {
			m["Customer_Address"] = nameAddress[1 ..< nameAddress.size()].join("\n").trim()
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

		return m
	}

	@Override
	public Map parseDocument(Document d) {
		assert d?.type

		Types t = Types.valueOf(d.type.name)
		def m = [:]
		switch(t) {
			case Types.OTHER:
				m.raw = parseOther(d)
				break
			case Types.CUSTOMER_HARD_COPY:
				m = parseCustomerHardCopy(d)
				break
			default:
				throw new IllegalArgumentException(t)
		}

		sanitizeAll(m)

		m
	}

	public String parseOther(Document d) {
		String data = pclToString(d)

		def m = data =~ /(?m)(\S+)/
		def lines = m*.getAt(1)

		return lines.join("\n")
	}

	private String getField(String line, int start, int end) {
		int length = line.length()
		if(start > length) {
			return ""
		}

		return line[start ..< Math.min(end+1, length)].trim()
	}

	private String pclToString(Document d, boolean skipPclHeader = true) {
		String data = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(d.pcl.data))).getText();

		// The text starts after the first double blank line
		if (skipPclHeader) {
			def startOfText = data.indexOf("\n\n")
			if (startOfText < 0)
				startOfText = data.indexOf("\r\n\r\n")
			data = data.substring(startOfText)
		}

		return data
	}

	/**
	 * Replaces \ with / because the searchable plugin doesn't escape them
	 *
	 * @param input The string to sanitize
	 *
	 * @return The input string with all \ replaced by /
	 */
	private String sanitize(String input) {
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
