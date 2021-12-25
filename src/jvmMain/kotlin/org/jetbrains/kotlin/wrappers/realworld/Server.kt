package org.jetbrains.kotlin.wrappers.realworld

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.html.*
import org.jetbrains.kotlin.wrappers.realworld.db.DatabaseFactory

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
    DatabaseFactory.init()

    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(DefaultHeaders)
        install(CallLogging)

        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            static("/static") {
                resources()
            }
        }
    }.start(wait = true)
}
