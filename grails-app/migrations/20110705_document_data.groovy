databaseChangeLog = {

	changeSet(author: "dbwatson (generated)", id: "1309894626393-1") {
		addColumn(tableName: "document_data") {
			column(name: "file_key", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1309894626393-2") {
		addColumn(tableName: "document_data") {
			column(name: "file_size", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1309894626393-3") {
		createIndex(indexName: "file_key_unique_idx", tableName: "document_data", unique: "true") {
			column(name: "file_key")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1309894626393-4") {
		dropColumn(columnName: "DATA", tableName: "DOCUMENT_DATA")
	}
}
