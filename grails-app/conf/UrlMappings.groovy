class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller:"home", action:"index")
		"500"(view:"/error")
		"/api/upload/savePcl" {
			controller = "upload"
			action = "savePcl"
		}

		"/document/$action?/$documentId?/$pageNumber?"{
			controller = "document"
		}

		"/printQueue/push/$printerId/$documentId" {
			controller = "printQueue"
			action = "push"
		}

		"/api/printQueue/get" {
			controller = "printQueue"
			action = "pop"
		}

		"/signatureCode/downloadImage/$documentId/$pageNumber" {
			controller = "signatureCode"
			action = "downloadImage"
		}

		"/signatureCode/send/$documentId/$email" {
			controller = "signatureCode"
			action = "send"
		}

		"/signatureCode/image/$documentId/$pageNumber" {
			controller = "signatureCode"
			action = "image"
		}

		"/signatureCode/sign/$documentId" {
			controller = "signatureCode"
			action = "sign"
		}

		// This parameter has to be called "term" for the autocomplete to work
		"/tag/list/$term" {
			controller = "tag"
			action = "list"
		}

		"/tag/create/$name**" {
			controller = "tag"
			action = "create"
		}

		"/tag/document/list/$documentId" {
			controller = "tag"
			action = "documentList"
		}

		"/tag/documentList/$documentId" {
			controller = "tag"
			action = "documentList"
		}

		"/tag/document/add" {
			controller = "tag"
			action = "documentAdd"
		}

		"/tag/document/remove" {
			controller = "tag"
			action = "documentRemove"
		}

		"/tag/documents/$name" {
			controller = "tag"
			action = "documents"
		}

		"/tag/documents/$name**" {
			controller = "tag"
			action = "documents"
		}
	}
}
