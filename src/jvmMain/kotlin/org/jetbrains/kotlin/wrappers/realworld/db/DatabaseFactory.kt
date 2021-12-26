package org.jetbrains.kotlin.wrappers.realworld.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object DatabaseFactory {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun init() {
        log.info("Initialising database")
        val pool = hikari()
        Database.connect(pool)
        runFlyway(pool)
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:test"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
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
