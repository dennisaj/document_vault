package us.paperlesstech.handlers.business_logic

import grails.plugin.spock.UnitSpec
import org.springframework.core.io.ClassPathResource
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.handlers.business_logic.FermanBusinessLogicService.FermanDocumentTypes
import us.paperlesstech.TagService
import us.paperlesstech.Document

class FermanBusinessLogicServiceSpec extends UnitSpec {
	FermanBusinessLogicService service
	TagService tagService = Mock()
	DocumentData custHardData = new DocumentData(mimeType: MimeType.PCL,
			data: new ClassPathResource("dt_cust_hard.pcl").getFile().getBytes())
	DocumentData otherData = new DocumentData(mimeType: MimeType.PCL,
			data: new ClassPathResource("dt_other.pcl").getFile().getBytes())

	def setup() {
		mockLogging(FermanBusinessLogicService)
		service = new FermanBusinessLogicService()
		service.tagService = tagService
	}

	def "identifying cust_hard_copy from the pcl"() {
		when: "When we lookup with a PCL with the CUST_HARD_COPY identifier in it"
		def type = service.getDocumentType(custHardData)

		then: "The response should identify this as CUSTOMER_HARD_COPY"
		type == FermanDocumentTypes.CustomerHardCopy
	}

	def "parsing cust_hard_copy pcl"() {
		when: "The document is parsed"
		def m = service.parseCustomerHardCopy(custHardData)

		then: "All of the following fields should be parsed out"
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
		expect: "A document with an unknown type should split the document on words"
		service.parseOther(otherData).split("\n") == ["Regular", "data1", "go3s,", "here", "and", "can", "span",
				"multiple", "lines."]
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
		1 * tagService.createTag("RO_Number")
		1 * tagService.createTag("VIN")
		tags[0] == "RO_Number"
		tags[1] == "VIN"
	}
}
