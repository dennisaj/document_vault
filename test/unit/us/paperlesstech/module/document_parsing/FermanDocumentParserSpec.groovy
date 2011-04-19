package us.paperlesstech.module.document_parsing

import grails.plugin.spock.UnitSpec

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentType
import us.paperlesstech.Pcl
import us.paperlesstech.document_parsing.FermanDocumentParser;

class FermanDocumentParserSpec extends UnitSpec {
	Document doc = new Document();
	FermanDocumentParser fdt = new FermanDocumentParser()

	def "identifying cust_hard_copy from the pcl"() {
		given: "Two domain objects in the database"
		mockDomain(DocumentType.class, testInstances)
		and: "A document with cust_hard_copy pcl"
		doc.pcl = new Pcl(data:new ClassPathResource("dt_cust_hard.pcl").getFile().getBytes())

		when: "When we lookup with a PCL with the CUST_HARD_COPY identifier in it"
		def type = fdt.getDocumentType(doc)

		then: "The response should identify this as CUSTOMER_HARD_COPY"
		type == hcType

		where:
		hcType = new DocumentType(name:FermanDocumentParser.Types.CUSTOMER_HARD_COPY.name())
		otherType = new DocumentType(name:FermanDocumentParser.Types.OTHER.name())
		testInstances = [hcType, otherType]
	}

	def "parsing cust_hard_copy pcl"() {
		given: "A document with cust_hard_copy pcl"
		doc.pcl = new Pcl(data:new ClassPathResource("dt_cust_hard.pcl").getFile().getBytes())

		when: "The document is parsed"
		def m = fdt.parseCustomerHardCopy(doc)

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
}