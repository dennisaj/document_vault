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
		
		"/document/image/$id/$pageNumber" {
			controller = "document"
			action = "image"
		}
		
		"/document/sign/$id/$pageNumber" {
			controller = "document"
			action = "sign"
		}
		
		"/printQueue/push/$printerId/$documentId" {
			controller = "printQueue"
			action = "push"
		}
		
		"/api/printQueue/get" {
			controller = "printQueue"
			action = "pop"
		}
	}
}