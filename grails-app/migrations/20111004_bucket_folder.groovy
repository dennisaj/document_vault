databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1317761164555-1") {
		createTable(tableName: "bucket") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "bucketPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "seth (generated)", id: "1317761164555-2") {
		createTable(tableName: "folder") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "folderPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "bucket_id", type: "bigint")

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "seth (generated)", id: "1317761164555-3") {
		addColumn(tableName: "document") {
			column(name: "folder_id", type: "bigint")
		}
	}

	changeSet(author: "seth (generated)", id: "1317761164555-4") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "bucket", constraintName: "FK_BUCKET_GROUP", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "groups", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1317761164555-5") {
		addForeignKeyConstraint(baseColumnNames: "folder_id", baseTableName: "document", constraintName: "FK_DOCUMENT_FOLDER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "folder", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1317761164555-6") {
		addForeignKeyConstraint(baseColumnNames: "bucket_id", baseTableName: "folder", constraintName: "FK_FOLDER_BUCKET", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "bucket", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1317761164555-7") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "folder", constraintName: "FK_FOLDER_GROUPS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "groups", referencesUniqueColumn: "false")
	}
}
