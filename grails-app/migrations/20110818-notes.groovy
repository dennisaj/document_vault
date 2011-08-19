databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1313694576051-1") {
		createTable(tableName: "note") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "notePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "data_id", type: "bigint")

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "document_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "note", type: "varchar(4096)")

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "seth (generated)", id: "1313694576051-2") {
		dropForeignKeyConstraint(baseTableName: "DOCUMENT_NOTES", baseTableSchemaName: "PUBLIC", constraintName: "FK_NOTES_DDATA")
	}

	changeSet(author: "seth (generated)", id: "1313694576051-3") {
		dropForeignKeyConstraint(baseTableName: "DOCUMENT_NOTES", baseTableSchemaName: "PUBLIC", constraintName: "FK_NOTES_DOCUMENT")
	}

	changeSet(author: "seth (generated)", id: "1313694576051-4") {
		addForeignKeyConstraint(baseColumnNames: "data_id", baseTableName: "note", constraintName: "FK_NOTES_DDATA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document_data", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1313694576051-5") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "note", constraintName: "FK_NOTES_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1313694576051-6") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "note", constraintName: "FK_NOTES_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1313694576051-7") {
		dropTable(tableName: "DOCUMENT_NOTES")
	}
}
