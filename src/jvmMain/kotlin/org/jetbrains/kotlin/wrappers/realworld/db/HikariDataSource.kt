package org.jetbrains.kotlin.wrappers.realworld.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun crateHikariDataSource(): HikariDataSource {
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
