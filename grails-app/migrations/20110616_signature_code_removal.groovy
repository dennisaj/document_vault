databaseChangeLog = {

	changeSet(author: "seth (generated)", id: "1308254867320-1") {
		dropForeignKeyConstraint(baseTableName: "SIGNATURE_CODE", baseTableSchemaName: "PUBLIC", constraintName: "FK_SIGNATURE_CODE_DOCUMENT")
	}

	changeSet(author: "seth (generated)", id: "1308254867320-2") {
		dropIndex(indexName: "CODE_UNIQUE_1307406573336", tableName: "SIGNATURE_CODE")
	}

	changeSet(author: "seth (generated)", id: "1308254867320-3") {
		dropIndex(indexName: "UNIQUE_SIGNATURE_CODE", tableName: "SIGNATURE_CODE")
	}

	changeSet(author: "seth (generated)", id: "1308254867320-4") {
		dropTable(tableName: "SIGNATURE_CODE")
	}
}
