package org.jetbrains.kotlin.wrappers.realworld

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.server.testing.*

val testEnv = createTestEnvironment {
    config = HoconApplicationConfig(ConfigFactory.load())

    module { init(testing = true) }
}
