databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1312574146194-1") {
		createTable(tableName: "document_notes") {
			column(name: "document_notes_id", type: "bigint")

			column(name: "document_data_id", type: "bigint")
		}
	}

	changeSet(author: "seth (generated)", id: "1312818108929-1") {
		addColumn(tableName: "document_notes") {
			column(name: "notes_idx", type: "integer")
		}
	}

	changeSet(author: "seth (generated)", id: "1312574146194-3") {
		addForeignKeyConstraint(baseColumnNames: "document_data_id", baseTableName: "document_notes", constraintName: "FK_NOTES_DDATA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document_data", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1312574146194-4") {
		addForeignKeyConstraint(baseColumnNames: "document_notes_id", baseTableName: "document_notes", constraintName: "FK_NOTES_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}
}
