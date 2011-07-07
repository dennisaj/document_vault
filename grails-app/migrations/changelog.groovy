databaseChangeLog = {

	changeSet(author: "dbwatson (generated)", id: "1307476009700-1") {
		createTable(tableName: "activity_log") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "activity_logPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ip", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "params", type: "varchar(4096)")

			column(name: "uri", type: "varchar(4096)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint")

			column(name: "user_agent", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "action", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "document", type: "varchar(255)")

			column(name: "page_number", type: "varchar(255)")

			column(name: "status", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-2") {
		createTable(tableName: "details") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "detailsPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(255)")

			column(name: "display_name", type: "varchar(255)")

			column(name: "logo", type: "varchar(255)")

			column(name: "logo_small", type: "varchar(255)")

			column(name: "name", type: "varchar(255)")

			column(name: "url_id", type: "bigint")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-3") {
		createTable(tableName: "document") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "documentPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-4") {
		createTable(tableName: "document_data") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "document_dataPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "mime_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "pages", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "file_key", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "file_size", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-5") {
		createTable(tableName: "document_document_data") {
			column(name: "document_files_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "document_data_id", type: "bigint")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-6") {
		createTable(tableName: "document_other_field") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "document_othePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "document_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "_key", type: "varchar(255)")

			column(name: "_value", type: "varchar(4096)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-7") {
		createTable(tableName: "document_search_field") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "document_searPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "document_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "_key", type: "varchar(255)")

			column(name: "_value", type: "varchar(4096)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-8") {
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

	changeSet(author: "dbwatson (generated)", id: "1307476009700-9") {
		createTable(tableName: "federation_provider") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "federation_prPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "auto_provision", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "details_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "uid", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-10") {
		createTable(tableName: "federation_provider_props") {
			column(name: "props", type: "bigint")

			column(name: "props_idx", type: "varchar(255)")

			column(name: "props_elt", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-11") {
		createTable(tableName: "groups") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "groupsPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "description", type: "varchar(255)")

			column(name: "external", type: "bit") {
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
		}

		modifySql() {
			replace(replace: '"external"', with: "external")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-12") {
		createTable(tableName: "groups_roles") {
			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "role_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-13") {
		createTable(tableName: "groups_users") {
			column(name: "user_base_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-14") {
		createTable(tableName: "level_permission_fifth") {
			column(name: "level_permission_id", type: "bigint")

			column(name: "fifth_string", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-15") {
		createTable(tableName: "level_permission_first") {
			column(name: "level_permission_id", type: "bigint")

			column(name: "first_string", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-16") {
		createTable(tableName: "level_permission_fourth") {
			column(name: "level_permission_id", type: "bigint")

			column(name: "fourth_string", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-17") {
		createTable(tableName: "level_permission_second") {
			column(name: "level_permission_id", type: "bigint")

			column(name: "second_string", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-18") {
		createTable(tableName: "level_permission_sixth") {
			column(name: "level_permission_id", type: "bigint")

			column(name: "sixth_string", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-19") {
		createTable(tableName: "level_permission_third") {
			column(name: "level_permission_id", type: "bigint")

			column(name: "third_string", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-20") {
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

	changeSet(author: "dbwatson (generated)", id: "1307476009700-21") {
		createTable(tableName: "permission") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "permissionPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "actions", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

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

			column(name: "type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint")

			column(name: "class", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-22") {
		createTable(tableName: "preview_image") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "preview_imagePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
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

			column(name: "height", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "page_number", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "width", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-23") {
		createTable(tableName: "print_queue") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "print_queuePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "document_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "printer_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-24") {
		createTable(tableName: "printer") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "printerPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
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
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-25") {
		createTable(tableName: "profile_base") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "profile_basePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "email", type: "varchar(255)")

			column(name: "email_hash", type: "varchar(255)")

			column(name: "full_name", type: "varchar(255)")

			column(name: "last_updated", type: "timestamp")

			column(name: "nick_name", type: "varchar(255)")

			column(name: "non_verified_email", type: "varchar(255)")

			column(name: "class", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-26") {
		createTable(tableName: "roles") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "rolesPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "description", type: "varchar(255)")

			column(name: "external", type: "bit") {
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
		}

		modifySql() {
			replace(replace: '"external"', with: "external")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-27") {
		createTable(tableName: "roles_users") {
			column(name: "user_base_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "role_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-29") {
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

	changeSet(author: "dbwatson (generated)", id: "1307476009700-30") {
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

	changeSet(author: "dbwatson (generated)", id: "1307476009700-31") {
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

	changeSet(author: "dbwatson (generated)", id: "1307476009700-32") {
		createTable(tableName: "users") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "usersPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tenant_id", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "action_hash", type: "varchar(255)")

			column(name: "date_created", type: "timestamp")

			column(name: "enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "expiration", type: "timestamp")

			column(name: "external", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "federated", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "federation_provider_id", type: "bigint")

			column(name: "last_updated", type: "timestamp")

			column(name: "password_hash", type: "varchar(255)")

			column(name: "profile_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "realm", type: "varchar(255)")

			column(name: "remoteapi", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "class", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}

		modifySql() {
			replace(replace: '"external"', with: "external")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-33") {
		createTable(tableName: "users_passwd_history") {
			column(name: "user_base_id", type: "bigint")

			column(name: "passwd_history_string", type: "varchar(255)")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-34") {
		createTable(tableName: "users_users") {
			column(name: "user_base_followers_id", type: "bigint")

			column(name: "user_base_id", type: "bigint")

			column(name: "user_base_follows_id", type: "bigint")
		}
	}

	changeSet(author: "seth (generated)", id: "1308779031843-1") {
		createTable(tableName: "highlight") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "highlightPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "accepted", type: "timestamp")

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

	changeSet(author: "seth (generated)", id: "1308779031843-2") {
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

	changeSet(author: "dbwatson (generated)", id: "1307476009700-35") {
		addPrimaryKey(columnNames: "group_id, role_id", tableName: "groups_roles")
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-36") {
		addPrimaryKey(columnNames: "group_id, user_base_id", tableName: "groups_users")
	}

	changeSet(author: "dbwatson (generated)", id: "1307476009700-37") {
		addPrimaryKey(columnNames: "role_id, user_base_id", tableName: "roles_users")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-38") {
		createIndex(indexName: "unique_document_other_fields", tableName: "document_other_field") {
			column(name: "document_id")

			column(name: "_key")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-39") {
		createIndex(indexName: "unique_document_search_fields", tableName: "document_search_field") {
			column(name: "document_id")

			column(name: "_key")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-40") {
		createIndex(indexName: "unique_group", tableName: "groups") {
			column(name: "tenant_id")

			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-41") {
		createIndex(indexName: "unique_preview_image", tableName: "preview_image") {
			column(name: "document_id")

			column(name: "page_number")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-42") {
		createIndex(indexName: "unique_printer", tableName: "printer") {
			column(name: "tenant_id")

			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-43") {
		createIndex(indexName: "unique_profile_base", tableName: "profile_base") {
			column(name: "tenant_id")

			column(name: "email")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-44") {
		createIndex(indexName: "unique_role", tableName: "roles") {
			column(name: "tenant_id")

			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-47") {
		createIndex(indexName: "unique_tag_name", tableName: "tags", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-48") {
		createIndex(indexName: "unique_user", tableName: "users") {
			column(name: "tenant_id")

			column(name: "username")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1309894626393-3") {
		createIndex(indexName: "file_key_unique_idx", tableName: "document_data", unique: "true") {
			column(name: "file_key")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307560218168-1") {
		createIndex(indexName: "activity_log_document_idx", tableName: "activity_log") {
			column(name: "document")
		}
	}

	changeSet(author: "seth (generated)", id: "1308779031843-3") {
		createIndex(indexName: "unique_party_code", tableName: "party", unique: "true") {
			column(name: "code")
		}
	}

	changeSet(author: "seth (generated)", id: "1308779031843-4") {
		createIndex(indexName: "unique_document_signator", tableName: "party") {
			column(name: "document_id")

			column(name: "signator_id")
		}
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-49") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "activity_log", constraintName: "FK_ACTIVITY_LOG_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-50") {
		addForeignKeyConstraint(baseColumnNames: "url_id", baseTableName: "details", constraintName: "FK_DETAILS_URL", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "url", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-51") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "document", constraintName: "FK_DOCUMENT_GROUP", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "groups", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-52") {
		addForeignKeyConstraint(baseColumnNames: "document_data_id", baseTableName: "document_document_data", constraintName: "FK_DDDATA_DDATA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document_data", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-53") {
		addForeignKeyConstraint(baseColumnNames: "document_files_id", baseTableName: "document_document_data", constraintName: "FK_DDDATA_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-54") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "document_other_field", constraintName: "FK_DOTHER_FIELD_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-55") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "document_search_field", constraintName: "FK_DSEARCH_FIELD_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-56") {
		addForeignKeyConstraint(baseColumnNames: "details_id", baseTableName: "federation_provider", constraintName: "FK_FED_PROVIDER_DETAILS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "details", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-57") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "groups_roles", constraintName: "FK_GROUP_ROLES_GROUP", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "groups", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-58") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "groups_roles", constraintName: "FK_GROUP_ROLES_ROLE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "roles", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-59") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "groups_users", constraintName: "FK_GROUP_USERS_GROUP", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "groups", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-60") {
		addForeignKeyConstraint(baseColumnNames: "user_base_id", baseTableName: "groups_users", constraintName: "FK_GROUP_USERS_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-61") {
		addForeignKeyConstraint(baseColumnNames: "level_permission_id", baseTableName: "level_permission_fifth", constraintName: "FK_LP5_PERMISSION", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "permission", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-62") {
		addForeignKeyConstraint(baseColumnNames: "level_permission_id", baseTableName: "level_permission_first", constraintName: "FK_LP1_PERMISSION", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "permission", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-63") {
		addForeignKeyConstraint(baseColumnNames: "level_permission_id", baseTableName: "level_permission_fourth", constraintName: "FK_LP4_PERMISSION", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "permission", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-64") {
		addForeignKeyConstraint(baseColumnNames: "level_permission_id", baseTableName: "level_permission_second", constraintName: "FK_LP2_PERMISSION", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "permission", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-65") {
		addForeignKeyConstraint(baseColumnNames: "level_permission_id", baseTableName: "level_permission_sixth", constraintName: "FK_LP6_PERMISSION", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "permission", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-66") {
		addForeignKeyConstraint(baseColumnNames: "level_permission_id", baseTableName: "level_permission_third", constraintName: "FK_LP3_PERMISSION", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "permission", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-67") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "login_record", constraintName: "FK_LOGIN_RECORD_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-68") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "permission", constraintName: "FK_PERMISSION_GROUP", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "groups", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-69") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "permission", constraintName: "FK_PERMISSION_ROLE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "roles", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-70") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "permission", constraintName: "FK_PERMISSION_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-71") {
		addForeignKeyConstraint(baseColumnNames: "data_id", baseTableName: "preview_image", constraintName: "FK_PREVIEW_IMAGE_DDATA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document_data", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-72") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "preview_image", constraintName: "FK_PREVIEW_IMAGE_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-73") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "print_queue", constraintName: "FK_PRINT_QUEUE_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-74") {
		addForeignKeyConstraint(baseColumnNames: "printer_id", baseTableName: "print_queue", constraintName: "FK_PRINT_QUEUE_PRINTER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "printer", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-75") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "print_queue", constraintName: "FK_PRINT_QUEUE_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-76") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "roles_users", constraintName: "FK_ROLE_USERS_ROLE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "roles", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-77") {
		addForeignKeyConstraint(baseColumnNames: "user_base_id", baseTableName: "roles_users", constraintName: "FK_ROLE_USERS_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-79") {
		addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "tag_links", constraintName: "FK_TAG_LINKS_TAGS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tags", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-80") {
		addForeignKeyConstraint(baseColumnNames: "federation_provider_id", baseTableName: "users", constraintName: "FK_USER_FED_PROVIDER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "federation_provider", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-81") {
		addForeignKeyConstraint(baseColumnNames: "profile_id", baseTableName: "users", constraintName: "FK_USER_PROFILE_BASE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "profile_base", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-82") {
		addForeignKeyConstraint(baseColumnNames: "user_base_id", baseTableName: "users_passwd_history", constraintName: "FK_USER_PASS_HIST_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-83") {
		addForeignKeyConstraint(baseColumnNames: "user_base_followers_id", baseTableName: "users_users", constraintName: "FK_users_users_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-84") {
		addForeignKeyConstraint(baseColumnNames: "user_base_follows_id", baseTableName: "users_users", constraintName: "FK_users_users_USER2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "dbwatson (generated)", id: "1307406573455-85") {
		addForeignKeyConstraint(baseColumnNames: "user_base_id", baseTableName: "users_users", constraintName: "FK_users_users_USER3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1308779031843-5") {
		addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "highlight", constraintName: "FK_HIGHLIGHT_PARTY", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "party", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1308779031843-6") {
		addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "party", constraintName: "FK_PARTY_DOCUMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "document", referencesUniqueColumn: "false")
	}

	changeSet(author: "seth (generated)", id: "1308779031843-7") {
		addForeignKeyConstraint(baseColumnNames: "signator_id", baseTableName: "party", constraintName: "FK_PARTY_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "users", referencesUniqueColumn: "false")
	}
}
