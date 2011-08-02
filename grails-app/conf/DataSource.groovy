

// NOTE: This file is NOT used, but is left in tact with default settings. 

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

environments {
    development {
        dataSource {
            url = "jdbc:hsqldb:mem:devDB"
        }
    }
    test {
        dataSource {
            url = "jdbc:hsqldb:mem:testDb"
        }
    }
    production {
        dataSource {
            url = "jdbc:hsqldb:file:prodDb;shutdown=true"
        }
    }
}
