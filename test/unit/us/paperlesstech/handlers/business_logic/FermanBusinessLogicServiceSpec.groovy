package us.paperlesstech.handlers.business_logic

import grails.plugin.spock.UnitSpec
import org.springframework.core.io.ClassPathResource
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.handlers.PclHandlerService
import us.paperlesstech.handlers.business_logic.FermanBusinessLogicService.FermanDocumentTypes
import us.paperlesstech.helpers.PclDocument
import us.paperlesstech.helpers.PclPage
import us.paperlesstech.TagService
import us.paperlesstech.helpers.PclInfo

class FermanBusinessLogicServiceSpec extends UnitSpec {
	FermanBusinessLogicService service
	PclHandlerService pclHandlerService = Mock()
	TagService tagService = Mock()
	File custHard
	File warrantyRepairOrder
	File serviceInvoice
	File techHardCard
	File repairOrderAuditCopy
	String otherText

	def setup() {
		mockLogging(FermanBusinessLogicService)
		service = new FermanBusinessLogicService()
		service.pclHandlerService = pclHandlerService
		service.tagService = tagService

		custHard = new ClassPathResource("dt_cust_hard.pcl").file
		warrantyRepairOrder = new ClassPathResource("WarrantyRepairOrder.pcl").file
		serviceInvoice = new ClassPathResource("ServiceInvoice.pcl").file
		techHardCard = new ClassPathResource("TechHardCard.pcl").file
		repairOrderAuditCopy = new ClassPathResource("RepairOrderAuditCopy.pcl").file

		otherText = new ClassPathResource("dt_other.pcl").file.text
		otherText = otherText.substring(otherText.indexOf("\n\n"))
	}

	def "identifying cust_hard_copy from the pcl"() {
		given:
		def pclDocument = new PclDocument()
		pclDocument.pages.add(new PclPage(macro: macro))

		when: "When we lookup with a PCL with the CUST_HARD_COPY identifier in it"
		def type = service.getDocumentType(pclDocument)

		then: "The response should identify this as CUSTOMER_HARD_COPY"
		type == FermanDocumentTypes.CustomerHardCopy

		where:
		macro << ['20901', '20913']
	}

	def "parsing cust_hard_copy pcl"() {
		given:
		def pclInfo = new PclInfo()
		pclInfo.parse(pclFile: custHard)

		when: "The document is parsed"
		def m = service.parseCustomerHardCopy(pclInfo.documents[0])

		then: "All of the following fields should be parsed out"
		"4/29/10" == m["RO_Open_Date"]
		"6001001" == m["RO_Number"]
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
		"Customer\nPay\nTEST\n************************\nTEST\nCustomer\nPay\nIGUUG\n\\" == m["raw"]
	}

	def "parsing WarrantyRepairOrder pcl"() {
		given:
		def pclInfo = new PclInfo()
		pclInfo.parse(pclFile: warrantyRepairOrder)

		when: "The document is parsed"
		def m = service.parseWarrantyRepairOrder(pclInfo.documents[0])

		then: "All of the following fields should be parsed out"
		"7/14/11" == m["RO_Open_Date"]
		"57026641" == m["RO_Number"]
		"JACKIE STEPHENSON" == m["Customer_Name"]
		"13017 TERRACE BROOK PL\nTAMPA, FL  336370000" == m["Customer_Address"]
		"813-846-3918" == m["Home_Phone"]
		"7/14/11" == m["RO_Close_Date"]
		"x-ref #" == m["Cross_Reference_Number"]
		"4DR SDN I4 CV" == m["Body"]
		"23079" == m["Mileage_In"]
		"23081" == m["Mileage_Out"]
		"2010" == m["Model_Year"]
		"NISSAN" == m["Make"]
		"ALTIMA" == m["Model"]
		"abc 123" == m["License_Number"]
		"Larry Edwards       7484" == m["Service_Advisor"]
		"1N4AL2AP0AC100933" == m["VIN"]
		"RED" == m["Color"]
		"7/15/11" == m["Delivery_Date"]
		"7/16/11" == m["In_Service_Date"]
		m["raw"]
	}

	def "parsing ServiceInvoice pcl"() {
		given:
		def pclInfo = new PclInfo()
		pclInfo.parse(pclFile: serviceInvoice)

		when: "The document is parsed"
		def m = service.parseServiceInvoice(pclInfo.documents[0])

		then: "All of the following fields should be parsed out"
		"8/17/11" == m["RO_Open_Date"]
		"57028977" == m["RO_Number"]
		"8/17/11" == m["RO_Close_Date"]
		"Final" == m["Status"]
		"49721" == m["Mileage_In"]
		"49726" == m["Mileage_Out"]
		"Chris Csercsics/3284*W*" == m["Service_Advisor"]
		"JULIO ALONSO" == m["Customer_Name"]
		"3840 N LAKE DR UNIT 127\nTAMPA, FL  336142044" == m["Customer_Address"]
		"999-999-9999" == m["Work_Phone"]
		"813-928-1926" == m["Home_Phone"]
		"4 DOOR SEDAN" == m["Body"]
		"2005" == m["Model_Year"]
		"NISSAN" == m["Make"]
		"ALTIMA" == m["Model"]
		"ABC 123" == m["License_Number"]
		"1N4AL11D45N454075" == m["VIN"]
		"MYSTIC EME" == m["Color"]
		"12/21/04" == m["Delivery_Date"]
		"12/21/04" == m["In_Service_Date"]
		m["raw"]
	}

	def "parsing RepairOrderAuditCopy pcl"() {
		given:
		def pclInfo = new PclInfo()
		pclInfo.parse(pclFile: repairOrderAuditCopy)

		when: "The document is parsed"
		def m = service.parseRepairOrderAuditCopy(pclInfo.documents[0])

		then: "All of the following fields should be parsed out"
		"MARK M ABIRI" == m["Customer_Name"]
		"305-467-6011" == m["Work_Phone"]
		"7/13/11" == m["RO_Open_Date"]
		"57026565" == m["RO_Number"]
		"813-236-3641" == m["Home_Phone"]
		"Pre-Invoice" == m["RO_Close_Date"]
		"R3213" == m["Receipt_Number"]
		"2 DOOR EXTEND" == m["Body"]
		"124454" == m["Mileage_In"]
		"124454" == m["Mileage_Out"]
		"James Spicer" == m["Service_Advisor"]
		"1917 E HANNA AVE\nTAMPA, FL  336103544" == m["Customer_Address"]
		"1995" == m["Model_Year"]
		"NISSAN TRUCK" == m["Make"]
		"PICKUP" == m["Model"]
		"1N6HD16S9SC445910" == m["VIN"]
		"GREEN" == m["Color"]
		"ABC 123" == m["License_Number"]
		"ACC 3621" == m["Account_Number"]
		"10/05/07" == m["Delivery_Date"]
		"10/06/07" == m["In_Service_Date"]
		m["raw"]
	}

	def "parsing TechHardCard pcl"() {
		given:
		def pclInfo = new PclInfo()
		pclInfo.parse(pclFile: techHardCard)

		when: "The document is parsed"
		def m = service.parseTechHardCard(pclInfo.documents[0])

		then: "All of the following fields should be parsed out"
		"AMPARO T FECTO" == m["Customer_Name"]
		"813-345-8990" == m["Home_Phone"]
		"8/17/11" == m["RO_Open_Date"]
		"57029007" == m["RO_Number"]
		"11:28" == m["Time_Received"]
		"Waiting" == m["Time_Promised"]
		"1523" == m["Key_Tag_Number"]
		"10202" == m["Current_Mileage"]
		"12345" == m["Mileage_Out"]
		"Larry Edwar" == m["Service_Advisor"]
		"21026 TANGOR RD\nLAND O LAKES, FL  346377426" == m["Customer_Address"]
		"999-999-9999" == m["Work_Phone"]
		"4 DR 2.5S XTR" == m["Body"]
		"ABCD" == m["Engine_Code"]
		"2009" == m["Model_Year"]
		"NISSAN" == m["Make"]
		"AL" == m["Model"]
		"1N4AL21E69N554344" == m["VIN"]
		"WINTER FRO" == m["Color"]
		"ABC 123" == m["License_Number"]
		"9/13/09" == m["Delivery_Date"]
		"9/16/09" == m["In_Service_Date"]
		m["raw"]
	}

	def "parsing unknown should split the words in the document"() {
		given:
		def pclDocument = new PclDocument()
		pclDocument.pages.add(new PclPage(pageData: otherText))

		when: "A document with an unknown type should split the document on words"
		def result = service.parseOther(pclDocument).split("\n")

		then:
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

	def "afterPclImportFile should tag the document and rename it"() {
		setup:
		def tags = []
		mockDomain(Document)
		def d = new Document()
		d.metaClass.save = { d }
		d.metaClass.addTag = { it -> tags << it }
		d.searchField("RO_Number", "RO_Number")
		d.searchField("VIN", "VIN")
		d.searchField("Customer_Name", "NAME")
		d.searchField("Key_Tag_Number", "TAG")

		when:
		service.afterPCLImportFile(document: d)

		then:
		d.name == "NAME - TAG"
		2 * tagService.createTag(_)
		tags[0] == "RO_Number"
		tags[1] == "VIN"
	}
}
