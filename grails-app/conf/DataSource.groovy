dataSource {
    pooled = true
    driverClassName = "org.hsqldb.jdbcDriver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            loggingSql = true
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:hsqldb:file:devDb;shutdown=true"
        }
    }
    test {
        dataSource {
            loggingSql = true
            dbCreate = "update"
            url = "jdbc:hsqldb:mem:testDb"
        }
    }
    production {
        dataSource {
            username = "document_vault"
            password = "K6mouJjt"
            driverClassName = "com.mysql.jdbc.Driver"
            dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
            dbCreate = "update"
            url = "jdbc:mysql://localhost/document_vault"
        }
    }
}
