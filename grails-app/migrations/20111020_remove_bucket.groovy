databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1319132024608-1") {
		addColumn(tableName: "folder") {
			column(name: "parent_id", type: "bigint")
		}
	}

	changeSet(author: "seth (generated)", id: "1319132024608-2") {
		dropForeignKeyConstraint(baseTableName: "BUCKET", baseTableSchemaName: "PUBLIC", constraintName: "FK_BUCKET_GROUP")
	}

	changeSet(author: "seth (generated)", id: "1319132024608-3") {
		dropForeignKeyConstraint(baseTableName: "FOLDER", baseTableSchemaName: "PUBLIC", constraintName: "FK_FOLDER_BUCKET")
	}

	changeSet(author: "seth (generated)", id: "1319132024608-4") {
		dropForeignKeyConstraint(baseTableName: "TAG_LINKS", baseTableSchemaName: "PUBLIC", constraintName: "FK_TAG_LINKS_TAGS")
	}

	changeSet(author: "seth (generated)", id: "1319132024608-5") {
		createIndex(indexName: "unique_name", tableName: "folder") {
			column(name: "parent_id")

			column(name: "name")
		}
	}

	changeSet(author: "seth (generated)", id: "1319132024608-6") {
		addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "folder", constraintName: "FK_FOLDER_PARENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "folder", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1319132024608-8") {
		dropColumn(columnName: "BUCKET_ID", tableName: "FOLDER")
	}

	changeSet(author: "seth (generated)", id: "1319132024608-9") {
		dropTable(tableName: "BUCKET")
	}

	changeSet(author: "seth (generated)", id: "1319132024608-10") {
		dropTable(tableName: "TAG_LINKS")
	}

	changeSet(author: "seth (generated)", id: "1319132024608-11") {
		dropTable(tableName: "TAGS")
	}
}
