class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller:"home", action:"index")
		"500"(view:'/error')
		"/api/document/savePcl" {
			controller = "document"
			action = "savePcl"
		}

		"/document/downloadImage/$id/$pageNumber" {
			controller = "document"
			action = "downloadImage"
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

		"/signatureCode/downloadImage/$id/$pageNumber" {
			controller = "signatureCode"
			action = "downloadImage"
		}

		"/signatureCode/send/$documentId/$email" {
			controller = "signatureCode"
			action = "send"
		}

		"/signatureCode/image/$id/$pageNumber" {
			controller = "signatureCode"
			action = "image"
		}

		"/signatureCode/sign/$id/$pageNumber" {
			controller = "signatureCode"
			action = "sign"
		}

		name taggedUpload: "/upload/$tag**" {
			controller = "upload"
			action = "index"
		}

		"/upload/ajaxSave" {
			controller = "upload"
			action = "ajaxSave"
		}

		"/upload/save" {
			controller = "upload"
			action = "save"
		}

		"/upload/$tag**/ajaxSave" {
			controller = "upload"
			action = "ajaxSave"
		}

		"/upload/$tag**/save" {
			controller = "upload"
			action = "save"
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

		"/tag/document/list/$id" {
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
