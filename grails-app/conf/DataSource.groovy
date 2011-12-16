dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            loggingSql = true
            dbCreate = "none" // one of 'create', 'create-drop','update'
            url = "jdbc:h2:devDb;MVCC=TRUE"
        }
    }
    test {
        dataSource {
            loggingSql = true
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE"
        }
    }
    production {
        dataSource {
            username = "document_vault"
            password = ""
            url = "jdbc:mysql://localhost/document_vault"
            driverClassName = "com.mysql.jdbc.Driver"
            dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
            dbCreate = "none"
        }
    }
}
