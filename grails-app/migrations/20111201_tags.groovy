databaseChangeLog = {

	changeSet(author: "dbwatson (generated)", id: "1322767004662-1") {
		createTable(tableName: "tag_links") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tag_linksPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tag_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tag_ref", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-2") {
		createTable(tableName: "tags") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tagsPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false", unique: "true")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-3") {
		createTable(tableName: "tenant_config") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tenant_configPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "_key", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "_value", type: "varchar(4096)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-4") {
		addColumn(tableName: "login_record") {
			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-5") {
		addColumn(tableName: "note") {
			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-6") {
		addColumn(tableName: "preference") {
			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-7") {
		createIndex(indexName: "group_tenant_id_idx", tableName: "_group") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-8") {
		createIndex(indexName: "role_tenant_id_idx", tableName: "_role") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-9") {
		createIndex(indexName: "user_tenant_id_idx", tableName: "_user") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-10") {
		createIndex(indexName: "activity_log_tenant_id_idx", tableName: "activity_log") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-11") {
		createIndex(indexName: "document_tenant_id_idx", tableName: "document") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-12") {
		createIndex(indexName: "document_data_tenant_id_idx", tableName: "document_data") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-13") {
		createIndex(indexName: "folder_tenant_id_idx", tableName: "folder") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-14") {
		createIndex(indexName: "highlight_tenant_id_idx", tableName: "highlight") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-15") {
		createIndex(indexName: "login_record_tenant_id_idx", tableName: "login_record") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-16") {
		createIndex(indexName: "note_tenant_id_idx", tableName: "note") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-17") {
		createIndex(indexName: "party_tenant_id_idx", tableName: "party") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-18") {
		createIndex(indexName: "permission_tenant_id_idx", tableName: "permission") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-19") {
		createIndex(indexName: "pinned_folder_tenant_id_idx", tableName: "pinned_folder") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-20") {
		createIndex(indexName: "preference_tenant_id_idx", tableName: "preference") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-21") {
		createIndex(indexName: "preview_image_tenant_id_idx", tableName: "preview_image") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-22") {
		createIndex(indexName: "printer_tenant_id_idx", tableName: "printer") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-23") {
		createIndex(indexName: "profile_tenant_id_idx", tableName: "profile") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-24") {
		createIndex(indexName: "name_unique_1322767004493", tableName: "tags", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-25") {
		createIndex(indexName: "tenant_config_tenant_id_idx", tableName: "tenant_config") {
			column(name: "tenant_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1322767004662-26") {
		addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "tag_links", constraintName: "FK7C35D6D45A3B441D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tags", referencesUniqueColumn: "false")
	}
}
