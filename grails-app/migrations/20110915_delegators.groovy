databaseChangeLog = {
	changeSet(author: "seth (generated)", id: "1316096440808-1") {
		createTable(tableName: "users_delegators") {
			column(name: "user_delegators_id", type: "bigint")

			column(name: "user_id", type: "bigint")
		}
	}

	changeSet(author: "seth (generated)", id: "1316096440808-2") {
		addForeignKeyConstraint(baseColumnNames: "user_delegators_id", baseTableName: "users_delegators", constraintName: "FK_USERS_DELEGATORS_DELEGATORS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1316096440808-3") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "users_delegators", constraintName: "FK_USERS_DELEGATORS_USERS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1316194231907-1") {
		addColumn(tableName: "activity_log") {
			column(name: "delegate_id", type: "bigint")
		}
	}

	changeSet(author: "seth (generated)", id: "1316194231907-2") {
		addForeignKeyConstraint(baseColumnNames: "delegate_id", baseTableName: "activity_log", constraintName: "FK_ACTIVITY_LOG_DELEGATE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}
}
