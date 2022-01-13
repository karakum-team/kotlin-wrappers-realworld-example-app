package org.jetbrains.kotlin.wrappers.realworld

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.server.testing.*
import javax.sql.DataSource

fun prepareTestEnvironment(dataSource: DataSource) = createTestEnvironment {
    config = HoconApplicationConfig(ConfigFactory.load())

    module { init(dataSource, testing = true) }
}
