import us.paperlesstech.DocumentType
import us.paperlesstech.Printer
import us.paperlesstech.Role
import us.paperlesstech.User
import us.paperlesstech.UserRole
import us.paperlesstech.document_parsing.FermanDocumentParser;

class BootStrap {
	def springSecurityService
	def init = { servletContext ->
		println "All users ${User.list(max: 100)}"
		println "Admin ${User.findByUsername("admin")}"
		println "Authorities ${User.findByUsername("admin")?.getAuthorities()}"

		if (!checkForPcl6()) {
			throw new RuntimeException()
		}

		if(User.count() == 0) {
			def adminRole = new Role(name: 'ROLE_ADMIN').save(flush: true)
			def userRole = new Role(name: 'ROLE_USER').save(flush: true)
			String adminPassword = springSecurityService.encodePassword('admin')
			String normalPassword = springSecurityService.encodePassword('normal')
			def adminUser = new User(username: 'admin', enabled: true, userPassword: adminPassword)
			def normalUser = new User(username: 'normal', enabled: true, userPassword: normalPassword)
			adminUser.save(flush: true)
			normalUser.save(flush: true)
			UserRole.create adminUser, adminRole, true
			UserRole.create normalUser, userRole, true
		}

		if(DocumentType.count() == 0) {
			new DocumentType(name:FermanDocumentParser.Types.CUSTOMER_HARD_COPY.name(),
					searchOptions:[
						"RO_Number",
						"Customer_Name",
						"Customer_Address",
						"Home_Phone",
						"Work_Phone",
						"VIN",
						"License_Number",
						"RO_Open_Date",
						"Time_Received",
						"Time_Promised",
						"Current_Mileage",
						"Mileage_Out",
						"Estimate_Of_Repairs",
						"Service_Advisor",
						"Delivery_Date",
						"In_Service_Date",
						"Model_Year",
						"Make",
						"Model",
						"Body",
						"Color",
						"Note"
					]).save()

			new DocumentType(name:FermanDocumentParser.Types.OTHER.name(), searchOptions:["Note"]).save()
		}
		
		if (Printer.count() == 0) {
			new Printer(name:"Recursive", host:"localhost", deviceType:"lj5gray", port:9100).save()
		}
	}

	def checkForPcl6 = {
		def file = new File('/usr/local/bin/pcl6')
		return file?.exists() && file?.canRead() && file?.canExecute()
	}

	def destroy = {
	}
}
