databaseChangeLog = {

	changeSet(author: "dbwatson (generated)", id: "1307541543215-1") {
		addColumn(tableName: "activity_log") {
			column(name: "action", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307541543215-2") {
		addColumn(tableName: "activity_log") {
			column(name: "document", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307541543215-3") {
		addColumn(tableName: "activity_log") {
			column(name: "page_number", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307541543215-4") {
		addColumn(tableName: "activity_log") {
			column(name: "status", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307541543215-5") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "DATE_CREATED", tableName: "PERMISSION")
	}

	changeSet(author: "dbwatson (generated)", id: "1307560218168-1") {
		createIndex(indexName: "activity_log_document_idx", tableName: "activity_log") {
			column(name: "document")
		}
	}
}
