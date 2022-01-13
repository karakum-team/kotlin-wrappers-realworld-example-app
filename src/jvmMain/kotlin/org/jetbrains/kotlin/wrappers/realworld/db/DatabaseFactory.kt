package org.jetbrains.kotlin.wrappers.realworld.db

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class DatabaseFactory(pool: DataSource) {

    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("Initialising database")
        Database.connect(pool)
        runFlyway(pool)
    }

    private fun runFlyway(datasource: DataSource) {
        val flyway = Flyway.configure()
            .dataSource(datasource)
            .locations("org/jetbrains/kotlin/wrappers/realworld/db")
            .load()
        try {
            flyway.info()
            flyway.migrate()
        } catch (exception: Exception) {
            log.error("Exception running flyway migration", exception)
            throw exception
        }
        log.info("Flyway migration has finished")
    }
}
