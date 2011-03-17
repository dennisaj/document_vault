package us.paperlesstech

import us.paperlesstech.Document
import us.paperlesstech.Pcl

import grails.test.*

class ModelTests extends GroovyTestCase {
	def sessionFactory

	def cleanUpGorm() {
		def session = sessionFactory.currentSession
		session.flush()
		session.clear()
	}

	void testDocumentMapping() {
		assertEquals "There should be no documents created yet", 0, Document.count()

		Document d = new Document();
		assertNull "Date created should be null", d.dateCreated
		d.save(flush:true)

		cleanUpGorm()

		d = Document.findByIdIsNotNull()
		assertNotNull "Date should have been filled in", d.dateCreated
		assertTrue "The create date should be less than now", d.dateCreated <= new Date()
	}

	void testPclMapping() {
		assertEquals "There should be no data at the start", 0, Pcl.count()

		// Testing that we can create a document without a Pcl
		Document d = new Document();
		d.save(flush: true)

		assertEquals "Creating a document without a Pcl should be possible", 0, Pcl.count()

		// Test that saving a document cascades to save the Pcl
		d.pcl = new Pcl(data: new byte[1])
		d.pcl.data[0] = 1;
		d.save(flush: true)
		cleanUpGorm()

		assertEquals "One Pcl should have been saved", 1, Pcl.count()

		// Reload the document
		d = Document.findByIdIsNotNull();
		assertNotNull "The Pcl should be attached to the document", d.pcl
		assertEquals "The Pcl data should have saved", 1, d.pcl.data[0]

		// Test that save cascades updates to the Pcl
		d.pcl.data[0] = 42;
		d.save(flush:true)
		cleanUpGorm()

		d = Document.findByPclIsNotNull();
		assertEquals "Verify the save was cascaded", 42, d.pcl.data[0]

		// Test that delete cascades
		d.delete(flush: true)
		cleanUpGorm()

		assertEquals "The delete should have cascaded to delete the Pcl", 0, Pcl.count()
	}

	void testPdfMapping() {
		assertEquals "There should be no data at the start", 0, Pdf.count()

		// Testing that we can create a document without a Pdf
		Document d = new Document();
		d.save(flush: true)

		assertEquals "Creating a document without a Pdf should be possible", 0, Pdf.count()

		// Test that saving a document cascades to save the Pdf
		d.pdf = new Pdf(data: new byte[1])
		d.pdf.data[0] = 1;
		d.save(flush: true)
		cleanUpGorm()

		assertEquals "One Pdf should have been saved", 1, Pdf.count()

		// Reload the document
		d = Document.findByIdIsNotNull();
		assertNotNull "The Pdf should be attached to the document", d.pdf
		assertEquals "The Pdf data should have saved", 1, d.pdf.data[0]

		// Test that save cascades updates to the Pdf
		d.pdf.data[0] = 42;
		d.save(flush:true)
		cleanUpGorm()

		d = Document.findByPdfIsNotNull();
		assertEquals "Verify the save was cascaded", 42, d.pdf.data[0]

		// Test that delete cascades
		d.delete(flush: true)
		cleanUpGorm()

		assertEquals "The delete should have cascaded to delete the Pdf", 0, Pdf.count()
	}

	void testImageMapping() {
		assertEquals "There should be no data at the start", 0, Image.count()

		// Testing that we can create a document without a Image
		Document d = new Document();
		d.save(flush: true)

		assertEquals "Creating a document without a Image should be possible", 0, Image.count()


		// Test that saving a document cascades to save the Image
		Image i = new Image(data:new byte[1], pageNumber:1)
		i.data[0] = 1
		d.addToImages(i)
		i = new Image(data:new byte[1], pageNumber:2)
		i.data[0] = 2
		d.addToImages(i)
		i = new Image(data:new byte[1], pageNumber:3)
		i.data[0] = 3
		d.addToImages(i)
		d.save(flush: true)
		cleanUpGorm()

		assertEquals "Three Images should have been saved", 3, Image.count()

		// Reload the document
		d = Document.findByIdIsNotNull();
		assertEquals "The Images should be attached to the document", 3, d.images.size()
		assertEquals "The Image data should have saved", 1, d.getSortedImages()[0].data[0]
		assertEquals "The Image data should have saved", 2, d.getSortedImages()[1].data[0]
		assertEquals "The Image data should have saved", 3, d.getSortedImages()[2].data[0]

		// Test that save cascades updates to the Pdf
		d.getSortedImages()[2].data[0] = 42;
		d.save(flush:true)
		cleanUpGorm()

		d = Document.findByIdIsNotNull();
		assertEquals "Verify the save was cascaded", 42, d.getSortedImages()[2].data[0]

		// Test that delete cascades
		d.delete(flush: true)
		cleanUpGorm()

		assertEquals "The delete should have cascaded to delete the Images", 0, Image.count()
	}
}