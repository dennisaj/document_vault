package us.paperlesstech.module.document_parsing.ferman

import grails.test.*

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentType
import us.paperlesstech.Pcl

class FermanDocumentTypeTests extends GrailsUnitTestCase {
	Document dt_cust_hard;

	protected void setUp() {
		super.setUp()

		dt_cust_hard = new Document()
		dt_cust_hard.pcl = new Pcl()
		dt_cust_hard.pcl.data = new ClassPathResource("dt_cust_hard.pcl").getFile().getBytes()
	}

	public void testGetDocumentTypeDtCustHard() {
		FermanDocumentType fdt = new FermanDocumentType()

		def hcType = new DocumentType(name:FermanDocumentType.Types.CUSTOMER_HARD_COPY.name())
		def unknownType = new DocumentType(name:FermanDocumentType.Types.UNKNOWN.name())
		def testInstances = [unknownType, hcType]
		mockDomain(DocumentType.class, testInstances)

		assertEquals("Should identify as CUST_HARD", hcType, fdt.getDocumentType(dt_cust_hard))
	}

	public void testParseCustomerHardCopy() {
		FermanDocumentType fdt = new FermanDocumentType()
		def m = fdt.parseCustomerHardCopy(dt_cust_hard)

		assertEquals("4/29/10", m["RO_Open_Date"])
		assertEquals("6001001/1", m["RO_Number"])
		assertEquals("15:54", m["Time_Received"])
		assertEquals("4/29 17:00", m["Time_Promised"])
		assertEquals("45555", m["Current_Mileage"])
		assertEquals("45556", m["Mileage_Out"])
		assertEquals("100.00", m["Estimate_Of_Repairs"])
		assertEquals("ARKONA SUPPORT/", m["Service_Advisor"])
		assertEquals("ABC DISTRIBUTING INC", m["Customer_Name"])
		assertEquals("3525056434", m["Work_Phone"])
		assertEquals("123456LLL", m["VIN"])
		assertEquals("PO BOX 619000\nNORTH MIAMI, FL  332619000", m["Customer_Address"])
		assertEquals("3525555555", m["Home_Phone"])
		assertEquals("4/29", m["Delivery_Date"])
		assertEquals("4/30", m["In_Service_Date"])
		assertEquals("2010", m["Model_Year"])
		assertEquals("OTHER", m["Make"])
		assertEquals("CAR", m["Model"])
		assertEquals("GOOD", m["Body"])
		assertEquals("RED", m["Color"])
		assertEquals("123XX", m["License_Number"])
	}

	protected void tearDown() {
		super.tearDown()
	}
}