package us.paperlesstech.module.document_parsing.ferman

import us.paperlesstech.Document
import us.paperlesstech.DocumentType
import us.paperlesstech.Text

class FermanDocumentType {
	public static enum Types {
		CUSTOMER_HARD_COPY,
		UNKNOWN
	}

	public DocumentType getDocumentType(Document d) {
		assert d.pcl
		assert d.pcl.data

		String data = pclToString(d)

		Types type = Types.UNKNOWN
		if(data.contains("&f20901y")) {
			type = Types.CUSTOMER_HARD_COPY
		}

		return DocumentType.findByNameIlike("%${type.name()}%")
	}

	Map parseCustomerHardCopy(Document d) {
		assert d.pcl
		assert d.pcl.data

		String data = pclToString(d)
		// The text starts after the first double blank line
		def startOfText = data.indexOf("\n\n")
		if(startOfText < 0)
			startOfText = data.indexOf("\r\n\r\n")
		data = data.substring(startOfText)

		def lines = []
		data.eachLine { lines += it }

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

	public Text parseDocument(Document d) {
		assert d.type

		Types t = Types.valueOf(d.type.name)
		Text text = new Text()
		switch(t) {
			case Types.UNKNOWN:
				text.raw = parseUnknown(d)
				break
			case Types.CUSTOMER_HARD_COPY:
				def m = parseCustomerHardCopy(d)
				sanitizeAll(m)
				text.parsedFields = m
				break
			default:
				throw new IllegalArgumentException(t)
		}

		return text
	}

	public String parseUnknown(Document d) {
		String data = pclToString(d)

		def m = data =~ /(?m)\s([A-Za-z0-9.,\\/:-]+)\s/
		def lines = []
		m.each { lines += it[1] }

		return lines.join("\n")
	}

	private String advanceToNonBlankLine(List lines) {
		while(lines.last().isEmpty()) {
			lines.pop()
		}

		return lines.pop()
	}

	private String getField(String line, int start, int end) {
		int length = line.length()
		if(start > length) {
			return ""
		}

		return line[start ..< Math.min(end+1, length)].trim()
	}

	private String pclToString(Document d) {
		String data = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(d.pcl.data))).getText();

		return data
	}

	private String sanitize(String input) {
		// Replace all backslashes because the searchable plugin doesn't escape them.
		return input.replaceAll(/\x5c/, "/")
	}

	private void sanitizeAll(Map m) {
		m.each { k, v ->
			m[k] = sanitize(v)
		}
	}
}
