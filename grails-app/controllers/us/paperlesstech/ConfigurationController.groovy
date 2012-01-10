package us.paperlesstech

class ConfigurationController {
	def tenantService

	def addFlag(String flag) {
		flag = flag?.trim().encodeAsHTML()
		assert flag

		def flags = tenantService.getTenantConfigList('flag')
		assert !flags.contains(flag)

		new TenantConfig(key: 'flag', value: flag).save(failOnError: true)
		flags = tenantService.getTenantConfigList('flag')

		render(template: 'flags', model: [flags: flags])
	}

	def index() { 
		render view: 'index'
	}

	def listFlags() {
		def flags = tenantService.getTenantConfigList('flag')

		[flags: flags]
	}

	def removeFlag(String flag) {
		flag = flag?.trim().encodeAsHTML()
		assert flag

		def flags = tenantService.getTenantConfigList('flag')
		assert flags.contains(flag)

		TenantConfig.findByKeyAndValue('flag', flag).delete(flush: true)
		flags = tenantService.getTenantConfigList('flag')

		render(template: 'flags', model: [flags: flags])
	}
}
