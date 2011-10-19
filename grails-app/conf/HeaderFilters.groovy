import grails.util.Metadata

class HeaderFilters {
	static buildNumber = Metadata.current['environment.BUILD_NUMBER'] ?: '0'
	static buildDate = Metadata.current['build.date'] ?: new Date().toString()

	def filters = {
		all(url:'/*') {
			before = {
				response.setHeader('X-DV-VERSION', buildNumber)
				response.setHeader('X-DV-BUILD-DATE', buildDate)
			}
		}
	}
}
