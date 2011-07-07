package us.paperlesstech.handlers.business_logic

import grails.plugin.spock.UnitSpec
import org.springframework.core.io.ClassPathResource
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.handlers.PclHandlerService
import us.paperlesstech.handlers.business_logic.FermanBusinessLogicService.FermanDocumentTypes

class FermanBusinessLogicServiceSpec extends UnitSpec {
	FermanBusinessLogicService service
	PclHandlerService pclHandlerService = Mock()
	String custHardText
	String otherText

	def setup() {
		mockLogging(FermanBusinessLogicService)
		service = new FermanBusinessLogicService()
		service.pclHandlerService = pclHandlerService

		custHardText = new ClassPathResource("dt_cust_hard.pcl").file.text
		custHardText = custHardText.substring(custHardText.indexOf("\n\n"))

		otherText = new ClassPathResource("dt_other.pcl").file.text
		otherText = otherText.substring(otherText.indexOf("\n\n"))
	}

	def "identifying cust_hard_copy from the pcl"() {
		given:
		byte[] bytes = new byte[1]

		when: "When we lookup with a PCL with the CUST_HARD_COPY identifier in it"
		def type = service.getDocumentType(bytes)

		then: "The response should identify this as CUSTOMER_HARD_COPY"
		1 * pclHandlerService.pclToString(bytes, false) >> new ClassPathResource("dt_cust_hard.pcl").file.text
		type == FermanDocumentTypes.CustomerHardCopy
	}

	def "parsing cust_hard_copy pcl"() {
		given:
		byte[] bytes = new byte[1]

		when: "The document is parsed"
		def m = service.parseCustomerHardCopy(bytes)

		then: "All of the following fields should be parsed out"
		1 * pclHandlerService.pclToString(bytes) >> custHardText
		"4/29/10" == m["RO_Open_Date"]
		"6001001/1" == m["RO_Number"]
		"15:54" == m["Time_Received"]
		"4/29 17:00" == m["Time_Promised"]
		"45555" == m["Current_Mileage"]
		"45556" == m["Mileage_Out"]
		"100.00" == m["Estimate_Of_Repairs"]
		"ARKONA SUPPORT/" == m["Service_Advisor"]
		"ABC DISTRIBUTING INC" == m["Customer_Name"]
		"3525056434" == m["Work_Phone"]
		"123456LLL" == m["VIN"]
		"PO BOX 619000\nNORTH MIAMI, FL  332619000" == m["Customer_Address"]
		"3525555555" == m["Home_Phone"]
		"4/29" == m["Delivery_Date"]
		"4/30" == m["In_Service_Date"]
		"2010" == m["Model_Year"]
		"OTHER" == m["Make"]
		"CAR" == m["Model"]
		"GOOD" == m["Body"]
		"RED" == m["Color"]
		"123XX" == m["License_Number"]
	}

	def "parsing unknown should split the words in the document"() {
		given:
		byte[] bytes

		when: "A document with an unknown type should split the document on words"
		def result = service.parseOther(bytes).split("\n")

		then:
		1 * pclHandlerService.pclToString(bytes) >> otherText
		result == ["Regular", "data1", "go3s,", "here", "and", "can", "span", "multiple", "lines."]
	}

	def "should sanitize strings"() {
		when: "Harmful characters should be replaced"
		def m = service.sanitizeAll(single: singleSlash, multiple: multipleSlashes)

		then: "slashes should be replaced"
		m.single == "ends with a /"
		m.multiple == "/one slash /two slash"


		where:
		singleSlash = "ends with a \\"
		multipleSlashes = /\one slash \two slash/
	}

	def "afterPclImportFile should tag the document"() {
		setup:
		def tags = []
		mockDomain(Document)
		def d = new Document()
		d.metaClass.save = { d }
		d.metaClass.addTag = { it -> tags << it }
		d.searchField("RO_Number", "RO_Number")
		d.searchField("VIN", "VIN")

		when:
		service.afterPCLImportFile(document: d)

		then:
		tags[0] == "RO_Number"
		tags[1] == "VIN"
	}
}
