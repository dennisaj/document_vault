package us.paperlesstech.helpers

import grails.plugin.spock.IntegrationSpec
import org.springframework.core.io.ClassPathResource

class PclInfoSpec extends IntegrationSpec {
	def "test parsing file"() {
		when:
		PclInfo info = new PclInfo()
		info.parse(map)

		then:
		info.documents.size() == 2
		info.documents[0].startPage == 3
		info.documents[0].endPage == 3
		info.documents[0].pages.size() == 1
		info.documents[0].pages[0].macro == "206"
		info.documents[0].pages[0].pageData.contains("57026641")
		info.documents[1].startPage == 4
		info.documents[1].endPage == 5
		info.documents[1].pages.size() == 2
		info.documents[1].pages[0].macro == "20813"
		info.documents[1].pages[0].pageData.contains("Pre-Invoice")
		info.documents[1].pages[1].macro == "20813"
		info.documents[1].pages[1].pageData.contains("IN-CABIN MICROFILTER")

		where:
		map << [[pclFile: testFile], [data: testFile.bytes]]
	}

	File getTestFile() {
		new ClassPathResource("3pages_2_doc.pcl").getFile()
	}
}
