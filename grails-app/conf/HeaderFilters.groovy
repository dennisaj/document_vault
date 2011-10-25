import grails.util.Metadata

class HeaderFilters {
	static buildNumber = Metadata.current['environment.BUILD_NUMBER'] ?: '0'
	static buildDate = Metadata.current['build.date'] ?: new Date().toString()
	static version = Metadata.current['app.version'] ?: '0'

	def filters = {
		all(url:'/*') {
			before = {
				response.setHeader('X-DV-BUILD-NUMBER', buildNumber)
				response.setHeader('X-DV-BUILD-DATE', buildDate)
				response.setHeader('X-DV-VERSION', version)
			}
		}
	}
}
