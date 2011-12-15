class UrlMappings {
	static mappings = {
		"/$controller/$action?/$id?" {
			constraints {
				// apply constraints here
			}
		}

		"500"(controller:'error', action:'index')

		name homePage: "/index.html" {
		}

		name signPage: "/sign.html" {
		}

		"/api/upload/savePcl" {
			controller = "upload"
			action = "savePcl"
		}

		"/document/download/$documentId/$documentDataId" {
			controller = {
				params.documentId ==~ /^(?i)[A-F\d]{8}(?:-[A-F\d]{4}){3}-[A-F\d]{12}$/ ? "code" : "document"
			}

			action = "download"
		}

		"/document/downloadImage/$documentId/$documentDataId" {
			controller = {
				params.documentId ==~ /^(?i)[A-F\d]{8}(?:-[A-F\d]{4}){3}-[A-F\d]{12}$/ ? "code" : "document"
			}

			action = "downloadImage"
		}

		"/document/thumbnail/$documentId/$documentDataId/$pageNumber" {
			controller = {
				params.documentId ==~ /^(?i)[A-F\d]{8}(?:-[A-F\d]{4}){3}-[A-F\d]{12}$/ ? "code" : "document"
			}

			action = "thumbnail"
		}

		"/document/$action?/$documentId?/$pageNumber?" {
			controller = {
				params.documentId ==~ /^(?i)[A-F\d]{8}(?:-[A-F\d]{4}){3}-[A-F\d]{12}$/ ? "code" : "document"
			}
		}

		"/party/removeParty/$documentId/$partyId" {
			controller = "party"
			action = "removeParty"
		}

		"/party/resend/$documentId/$partyId" {
			controller = "party"
			action = "resend"
		}

		"/party/$action?/$documentId?" {
			controller = "party"
		}

		"/note/download/$documentId/$noteDataId" {
			controller = "note"
			action = "download"
		}

		"/note/list/$documentId" {
			controller = "note"
			action = "list"
		}

		"/note/saveLines/$documentId" {
			controller = "note"
			action = "saveLines"
		}

		"/note/saveText/$documentId" {
			controller = "note"
			action = "saveText"
		}

		"/printQueue/push/$printerId/$documentId" {
			controller = "printQueue"
			action = "push"
		}

		"/api/printQueue/get" {
			controller = "printQueue"
			action = "pop"
		}

		"/c/$code" {
			controller = "code"
			action = "index"
		}

		"/printQueue/printWindow/$documentId" {
			controller = "printQueue"
			action = "printWindow"
		}

		"/p/details/$documentId?" {
			controller = "printQueue"
			action = "details"
		}

		"/p/print/$documentId?/$printerId?" {
			controller = "printQueue"
			action = "push"
		}

		"/r/ra/$userId?" {
			controller = "runAs"
			action = "runas"
		}

		"/r/r" {
			controller = "runAs"
			action = "release"
		}

		"/folder/$action/$folderId/$documentId?" {
			controller = 'folder'
		}
	}
}
