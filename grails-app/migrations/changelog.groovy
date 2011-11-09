databaseChangeLog = {

	changeSet(author: "dbwatson (generated)", id: "1319809075898-1") {
		createTable(tableName: "_group") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "_groupPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "description", type: "varchar(255)")

			column(name: "_external", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "external_id", type: "varchar(255)")

			column(name: "last_updated", type: "timestamp")

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "protect", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "realm", type: "varchar(255)")

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-2") {
		createTable(tableName: "_group_to_role") {
			column(name: "role_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-3") {
		createTable(tableName: "_group_to_user") {
			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-4") {
		createTable(tableName: "_role") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "_rolePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "description", type: "varchar(255)")

			column(name: "_external", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp")

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "protect", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "realm", type: "varchar(255)")

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-5") {
		createTable(tableName: "_role_to_user") {
			column(name: "role_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-6") {
		createTable(tableName: "_user") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "_userPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "action_hash", type: "varchar(255)")

			column(name: "date_created", type: "timestamp")

			column(name: "enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "expiration", type: "timestamp")

			column(name: "_external", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "external_id", type: "varchar(255)")

			column(name: "last_updated", type: "timestamp")

			column(name: "password_hash", type: "varchar(255)")

			column(name: "profile_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "realm", type: "varchar(255)")

			column(name: "remoteapi", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-7") {
		createTable(tableName: "_user_passwd_history") {
			column(name: "user_id", type: "bigint")

			column(name: "passwd_history_string", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-8") {
		createTable(tableName: "_user_to_delegators") {
			column(name: "user_delegators_id", type: "bigint")

			column(name: "user_id", type: "bigint")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-9") {
		createTable(tableName: "activity_log") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "activity_logPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "action", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "delegate_id", type: "bigint")

			column(name: "document_id", type: "bigint")

			column(name: "ip", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "page_number", type: "varchar(255)")

			column(name: "params", type: "varchar(4096)")

			column(name: "status", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "uri", type: "varchar(4096)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint")

			column(name: "user_agent", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-10") {
		createTable(tableName: "document") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "documentPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "created_by_id", type: "bigint")

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "folder_id", type: "bigint")

			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "last_updated_by_id", type: "bigint")

			column(name: "name", type: "varchar(255)")

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-11") {
		createTable(tableName: "document_data") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "document_dataPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "file_key", type: "varchar(255)") {
				constraints(nullable: "false", unique: "true")
			}

			column(name: "file_size", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "mime_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "pages", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-12") {
		createTable(tableName: "document_other_field") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "document_othePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "document_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "_key", type: "varchar(255)")

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "_value", type: "varchar(4096)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-13") {
		createTable(tableName: "document_search_field") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "document_searPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "document_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "_key", type: "varchar(255)")

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "_value", type: "varchar(4096)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-14") {
		createTable(tableName: "document_to_document_data") {
			column(name: "document_files_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "document_data_id", type: "bigint")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-15") {
		createTable(tableName: "domain_tenant_map") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "domain_tenantPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "domain_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "mapped_tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-16") {
		createTable(tableName: "folder") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "folderPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "created_by_id", type: "bigint")

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "last_updated_by_id", type: "bigint")

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "parent_id", type: "bigint")

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-17") {
		createTable(tableName: "highlight") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "highlightPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "accepted", type: "timestamp")

			column(name: "height", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "_left", type: "integer") {
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

			column(name: "_top", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "width", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-18") {
		createTable(tableName: "login_record") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "login_recordPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "last_updated", type: "timestamp")

			column(name: "owner_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "remote_addr", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "remote_host", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_agent", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-19") {
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

			column(name: "_left", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "note", type: "varchar(4096)")

			column(name: "page_number", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "_top", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-20") {
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

			column(name: "rejected", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "sent", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "signator_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "viewed", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-21") {
		createTable(tableName: "permission") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "permissionPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "actions", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "group_id", type: "bigint")

			column(name: "managed", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "possible_actions", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "role_id", type: "bigint")

			column(name: "target", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-22") {
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

	changeSet(author: "dbwatson (generated)", id: "1319809075898-23") {
		createTable(tableName: "preview_image") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "preview_imagePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "data_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "document_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "page_number", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "source_height", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "source_width", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "thumbnail_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-24") {
		createTable(tableName: "printer") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "printerPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "device_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "host", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "port", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-25") {
		createTable(tableName: "profile") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "profilePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "email", type: "varchar(255)")

			column(name: "email_hash", type: "varchar(255)")

			column(name: "full_name", type: "varchar(255)")

			column(name: "last_updated", type: "timestamp")

			column(name: "nick_name", type: "varchar(255)")

			column(name: "non_verified_email", type: "varchar(255)")

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-26") {
		createTable(tableName: "url") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "urlPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "alt_text", type: "varchar(255)")

			column(name: "description", type: "varchar(255)")

			column(name: "location", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-27") {
		addPrimaryKey(columnNames: "group_id, role_id", tableName: "_group_to_role")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-28") {
		addPrimaryKey(columnNames: "group_id, user_id", tableName: "_group_to_user")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-29") {
		addPrimaryKey(columnNames: "role_id, user_id", tableName: "_role_to_user")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-30") {
		createIndex(indexName: "group_description_idx", tableName: "_group") {
			column(name: "description")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-31") {
		createIndex(indexName: "group_external_id_idx", tableName: "_group") {
			column(name: "external_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-32") {
		createIndex(indexName: "unique_group_name", tableName: "_group") {
			column(name: "tenant_id")

			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-33") {
		createIndex(indexName: "unique_role_name", tableName: "_role") {
			column(name: "tenant_id")

			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-34") {
		createIndex(indexName: "unique_username", tableName: "_user") {
			column(name: "tenant_id")

			column(name: "username")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-35") {
		createIndex(indexName: "user_external_id_idx", tableName: "_user") {
			column(name: "external_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-36") {
		createIndex(indexName: "activity_log_document_idx", tableName: "activity_log") {
			column(name: "document_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-37") {
		createIndex(indexName: "file_key_unique_1319809075797", tableName: "document_data", unique: "true") {
			column(name: "file_key")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-38") {
		createIndex(indexName: "unique_document_other_key", tableName: "document_other_field") {
			column(name: "document_id")

			column(name: "_key")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-39") {
		createIndex(indexName: "unique_document_search_key", tableName: "document_search_field") {
			column(name: "document_id")

			column(name: "_key")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-40") {
		createIndex(indexName: "unique_folder_name", tableName: "folder") {
			column(name: "parent_id")

			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-41") {
		createIndex(indexName: "code_unique_1319809075821", tableName: "party", unique: "true") {
			column(name: "code")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-42") {
		createIndex(indexName: "unique_signator_id", tableName: "party") {
			column(name: "document_id")

			column(name: "signator_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-43") {
		createIndex(indexName: "unique__key", tableName: "preference") {
			column(name: "user_id")

			column(name: "_key")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-44") {
		createIndex(indexName: "unique_page_number", tableName: "preview_image") {
			column(name: "document_id")

			column(name: "page_number")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-45") {
		createIndex(indexName: "unique_printer_name", tableName: "printer") {
			column(name: "tenant_id")

			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-46") {
		createIndex(indexName: "unique_email", tableName: "profile") {
			column(name: "tenant_id")

			column(name: "email")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-47") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "_group_to_role", constraintName: "FK8F887FFB44DB1038", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-48") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "_group_to_role", constraintName: "FK8F887FFBAE2C9F5C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_role", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-49") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "_group_to_user", constraintName: "FK8F89EB5044DB1038", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-50") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "_group_to_user", constraintName: "FK8F89EB505357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-51") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "_role_to_user", constraintName: "FK549E2625AE2C9F5C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_role", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-52") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "_role_to_user", constraintName: "FK549E26255357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-53") {
		addForeignKeyConstraint(baseColumnNames: "profile_id", baseTableName: "_user", constraintName: "FK571A4AAC8440878", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "profile", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-54") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "_user_passwd_history", constraintName: "FKA4C160285357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-55") {
		addForeignKeyConstraint(baseColumnNames: "user_delegators_id", baseTableName: "_user_to_delegators", constraintName: "FK35DDC57F6874A763", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-56") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "_user_to_delegators", constraintName: "FK35DDC57F5357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-57") {
		addForeignKeyConstraint(baseColumnNames: "delegate_id", baseTableName: "activity_log", constraintName: "FK611AA61463DB5F42", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-58") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "activity_log", constraintName: "FK611AA6145AFED49D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-59") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "activity_log", constraintName: "FK611AA6145357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-60") {
		addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "document", constraintName: "FK335CD11BD073FB19", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-61") {
		addForeignKeyConstraint(baseColumnNames: "folder_id", baseTableName: "document", constraintName: "FK335CD11B443B047D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "folder", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-62") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "document", constraintName: "FK335CD11B44DB1038", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-63") {
		addForeignKeyConstraint(baseColumnNames: "last_updated_by_id", baseTableName: "document", constraintName: "FK335CD11B4C8B98C3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-64") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "document_other_field", constraintName: "FKB2EF55E75AFED49D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-65") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "document_search_field", constraintName: "FKA39F08475AFED49D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-66") {
		addForeignKeyConstraint(baseColumnNames: "document_data_id", baseTableName: "document_to_document_data", constraintName: "FK902F482E2A5E6954", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document_data", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-67") {
		addForeignKeyConstraint(baseColumnNames: "document_files_id", baseTableName: "document_to_document_data", constraintName: "FK902F482EA3A48C65", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-68") {
		addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "folder", constraintName: "FKB45D1C6ED073FB19", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-69") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "folder", constraintName: "FKB45D1C6E44DB1038", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-70") {
		addForeignKeyConstraint(baseColumnNames: "last_updated_by_id", baseTableName: "folder", constraintName: "FKB45D1C6E4C8B98C3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-71") {
		addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "folder", constraintName: "FKB45D1C6EA030DEC1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "folder", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-72") {
		addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "highlight", constraintName: "FKD7658CB4B3F4F217", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "party", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-73") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "login_record", constraintName: "FKF43101E7BF3E1354", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-74") {
		addForeignKeyConstraint(baseColumnNames: "data_id", baseTableName: "note", constraintName: "FK33AFF249B3C78", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document_data", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-75") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "note", constraintName: "FK33AFF25AFED49D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-76") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "note", constraintName: "FK33AFF25357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-77") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "party", constraintName: "FK6581AE65AFED49D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-78") {
		addForeignKeyConstraint(baseColumnNames: "signator_id", baseTableName: "party", constraintName: "FK6581AE692C22974", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-79") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "permission", constraintName: "FKE125C5CF44DB1038", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-80") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "permission", constraintName: "FKE125C5CFAE2C9F5C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_role", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-81") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "permission", constraintName: "FKE125C5CF5357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-82") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "preference", constraintName: "FKA8FCBCDB5357633C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-83") {
		addForeignKeyConstraint(baseColumnNames: "data_id", baseTableName: "preview_image", constraintName: "FK2987FA2449B3C78", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document_data", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-84") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "preview_image", constraintName: "FK2987FA245AFED49D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1319809075898-85") {
		addForeignKeyConstraint(baseColumnNames: "thumbnail_id", baseTableName: "preview_image", constraintName: "FK2987FA2499523BF6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document_data", referencesUniqueColumn: "false")
	}

	include file: '20111109_pinned_folder.groovy'
}
