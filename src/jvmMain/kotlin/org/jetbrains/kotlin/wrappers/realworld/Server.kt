package org.jetbrains.kotlin.wrappers.realworld

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.config.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.wrappers.realworld.crypto.Jwks
import org.jetbrains.kotlin.wrappers.realworld.crypto.JwtConfig
import org.jetbrains.kotlin.wrappers.realworld.db.DatabaseFactory
import org.jetbrains.kotlin.wrappers.realworld.routes.profileRouting
import org.jetbrains.kotlin.wrappers.realworld.routes.userRouting
import org.jetbrains.kotlin.wrappers.realworld.service.FollowingService
import org.jetbrains.kotlin.wrappers.realworld.service.UserService
import java.io.File
import java.util.concurrent.TimeUnit

fun HTML.index() {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
        div {
            id = "root"
        }
        script(src = "/static/kotlin-wrappers-realworld-example-app.js") {}
    }
}

fun main() {
    embeddedServer(Netty, environment = applicationEngineEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load())

        connector {
            port = 8080
        }

        module { init() }
    }).start(wait = true)
}

fun Application.init(testing: Boolean = false) {
    DatabaseFactory.init()

    val userService = UserService()
    val followingService = FollowingService()

    val jwtConfig = JwtConfig(
        privateKey = environment.config.property("jwt.privateKey").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        realm = environment.config.property("jwt.realm").getString(),
    )

    val jwkProvider = if (testing) {
        val jwks = Json.decodeFromString<Jwks>(
            File("certs/jwks.json").readText()
        )

        JwkProvider { keyId ->
            Jwk.fromValues(jwks.keys.find { it["kid"] == keyId })
        }
    } else {
        JwkProviderBuilder(jwtConfig.issuer)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
    }

    install(DefaultHeaders)
    install(CallLogging)

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwkProvider, jwtConfig.issuer) {
                acceptLeeway(3)
            }
            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        get("/") {
            call.respondHtml(HttpStatusCode.OK, HTML::index)
        }

        static("/static") {
            resources()
        }

        static(".well-known") {
            staticRootFolder = File("certs")
            file("jwks.json")
        }

        userRouting(jwtConfig, jwkProvider, userService)
        profileRouting(userService, followingService)
    }
}
