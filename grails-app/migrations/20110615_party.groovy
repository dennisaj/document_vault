databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1308167870582-1") {
		createTable(tableName: "highlight") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "highlightPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "lower_rightx", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "lower_righty", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "page_number", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "party_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "required", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "upper_leftx", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "upper_lefty", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "seth (generated)", id: "1308167870582-2") {
		createTable(tableName: "party") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "partyPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "code", type: "varchar(255)") {
				constraints(nullable: "false", unique: "true")
			}

			column(name: "color", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "document_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "document_permission", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "expiration", type: "timestamp")

			column(name: "sent", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "signator_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "seth (generated)", id: "1308167870582-3") {
		createIndex(indexName: "unique_party_code", tableName: "party", unique: "true") {
			column(name: "code")
		}
	}

	changeSet(author: "seth (generated)", id: "1308167870582-4") {
		createIndex(indexName: "unique_document_signator", tableName: "party") {
			column(name: "document_id")

			column(name: "signator_id")
		}
	}

	changeSet(author: "seth (generated)", id: "1308167870582-5") {
		addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "highlight", constraintName: "FK_HIGHLIGHT_PARTY", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "party", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1308167870582-6") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "party", constraintName: "FK_PARTY_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1308167870582-7") {
		addForeignKeyConstraint(baseColumnNames: "signator_id", baseTableName: "party", constraintName: "FK_PARTY_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}
}
