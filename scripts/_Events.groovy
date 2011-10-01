eventDefaultStart = {
	createUnitTest = { Map args = [:] ->
		def superClass
			// map unit test superclass to Spock equivalent
			switch(args["superClass"]) {
				case "ControllerUnitTestCase":
					superClass = "ControllerSpec"
					break
				case "TagLibUnitTestCase":
					superClass = "TagLibSpec"
					break
				default:
					superClass = "UnitSpec"
			}
		createArtifact name: args["name"], suffix: "${args['suffix']}Spec", type: "Spec", path: "test/unit", superClass: superClass
	}
	createIntegrationTest = { Map args = [:] ->
		createArtifact name: args["name"], suffix: "${args['suffix']}IntegrationSpec", type: "Spec", path: "test/integration", superClass: "IntegrationSpec"
	}
}

eventCreateWarStart = { warname, stagingDir ->
	ant.propertyfile(file: "${stagingDir}/WEB-INF/classes/application.properties") {
		ant.antProject.properties.findAll { k,v-> k.startsWith('environment.BUILD') } .each { k,v->
			entry(key: k, value: v)
		}
		entry(key: 'build.date', value: new Date())
	}
}
