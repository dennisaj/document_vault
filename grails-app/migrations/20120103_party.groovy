databaseChangeLog = {

	changeSet(author: "cashurst (generated)", id: "1325618855251-1") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "color", tableName: "party")
	}
}
