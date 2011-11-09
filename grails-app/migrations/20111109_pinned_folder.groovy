databaseChangeLog = {

	changeSet(author: "dbwatson (generated)", id: "1320831213252-1") {
		createTable(tableName: "pinned_folder") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "pinned_folderPK")
			}

			column(name: "folder_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1320831213252-2") {
		createIndex(indexName: "_user_to_folder_folder_idx", tableName: "pinned_folder") {
			column(name: "folder_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1320831213252-3") {
		createIndex(indexName: "_user_to_folder_user_idx", tableName: "pinned_folder") {
			column(name: "user_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1320831213252-5") {
		createIndex(indexName: "unique_pinned_folder_id", tableName: "pinned_folder") {
			column(name: "tenant_id")

			column(name: "user_id")

			column(name: "folder_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1320831213252-6") {
		addForeignKeyConstraint(baseColumnNames: "folder_id", baseTableName: "pinned_folder", constraintName: "FKDBC582B5443B047D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "folder", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1320831213252-7") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pinned_folder", constraintName: "FKDBC582B55357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}
}
