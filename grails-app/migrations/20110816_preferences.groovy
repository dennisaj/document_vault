databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1313509954803-1") {
		createTable(tableName: "preference") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "preferencePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "_key", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "_value", type: "varchar(4096)")
		}
	}

	changeSet(author: "seth (generated)", id: "1313509954803-3") {
		createIndex(indexName: "unique_key", tableName: "preference") {
			column(name: "user_id")

			column(name: "_key")
		}
	}

	changeSet(author: "seth (generated)", id: "1313509954803-4") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "preference", constraintName: "FK_PREFERENCE_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}
}
