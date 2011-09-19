databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1316463819380-4") {
		dropIndex(indexName: "activity_log_document_idx", tableName: "activity_log")
	}

	changeSet(author: "seth (generated)", id: "1316463819380-5") {
		dropColumn(columnName: "document", tableName: "activity_log")
	}

	changeSet(author: "seth (generated)", id: "1316463819380-1") {
		addColumn(tableName: "activity_log") {
			column(name: "document_id", type: "bigint")
		}
	}

	changeSet(author: "seth (generated)", id: "1316463819380-2") {
		createIndex(indexName: "activity_log_document_idx", tableName: "activity_log") {
			column(name: "document_id")
		}
	}

	changeSet(author: "seth (generated)", id: "1316463819380-3") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "activity_log", constraintName: "FK_ACTIVITY_LOG_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}
}
