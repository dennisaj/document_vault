class UrlMappings {
	static mappings = {
		def codeClosure = {
			params.documentId ==~ /^(?i)[A-F\d]{8}(?:-[A-F\d]{4}){3}-[A-F\d]{12}$/ ? "code" : "document"
		}

		"/$controller/$action?/$id?" {
			constraints {
				// apply constraints here
			}
		}

		"500"(controller:'error', action:'index')

		name homePage: "/index.html" {}

		name signPage: "/sign.html" {}

		"/document/download/$documentId/$documentDataId" {
			controller = codeClosure

			action = "download"
		}

		"/document/downloadImage/$documentId/$documentDataId" {
			controller = codeClosure

			action = "downloadImage"
		}

		"/document/thumbnail/$documentId/$documentDataId/$pageNumber" {
			controller = codeClosure

			action = "thumbnail"
		}

		"/document/$action?/$documentId?/$pageNumber?" {
			controller = codeClosure
		}

		"/party/cursiveSign/$documentId" {
			controller = "party"
			action = {
				params.documentId ==~ /^(?i)[A-F\d]{8}(?:-[A-F\d]{4}){3}-[A-F\d]{12}$/ ? "codeSign" : "cursiveSign"
			}
		}

		"/party/clickWrap/$documentId" {
			controller = "party"
			action = {
				params.documentId ==~ /^(?i)[A-F\d]{8}(?:-[A-F\d]{4}){3}-[A-F\d]{12}$/ ? "codeClickWrap" : "clickWrap"
			}
		}

		"/party/$action?/$documentId?/$partyId?" {
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
