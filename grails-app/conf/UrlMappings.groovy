class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller:"home", action:"index")
		"500"(view:'/error')
		"/api/document/save" {
			controller = "document"
			action = "saveApi"
		}
		
		"/document/downloadPdf/$id**.pdf" {
			controller = "document"
			action = "downloadPdf"
		}
	}
}