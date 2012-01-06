eventCreateWarStart = { warname, stagingDir ->
	ant.propertyfile(file: "${stagingDir}/WEB-INF/classes/application.properties") {
		ant.antProject.properties.findAll { k,v-> k.startsWith('environment.BUILD') } .each { k,v->
			entry(key: k, value: v)
		}
		entry(key: 'build.date', value: new Date())
	}
}
