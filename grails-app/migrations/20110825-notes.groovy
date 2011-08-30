databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1314293656233-1") {
		addColumn(tableName: "note") {
			column(name: "_left", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "seth (generated)", id: "1314293656233-2") {
		addColumn(tableName: "note") {
			column(name: "_top", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "seth (generated)", id: "1314625758008-1") {
		addColumn(tableName: "note") {
			column(name: "page", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}
}
