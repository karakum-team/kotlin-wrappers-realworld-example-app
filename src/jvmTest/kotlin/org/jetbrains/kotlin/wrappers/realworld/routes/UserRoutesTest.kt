package org.jetbrains.kotlin.wrappers.realworld.routes

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.wrappers.realworld.model.User
import org.jetbrains.kotlin.wrappers.realworld.model.UserDraft
import org.jetbrains.kotlin.wrappers.realworld.testEnv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class UserRoutesTest {
    @Test
    fun testUserCreation() {
        withApplication(testEnv) {
            with(handleRequest(HttpMethod.Post, "/api/users") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

                setBody(
                    Json.encodeToString(
                        UserDraft(
                            email = "test_user@gmail.com",
                            username = "test_user",
                            password = "querty",
                        )
                    )
                )
            }) {
                assertEquals(HttpStatusCode.Created, response.status())

                val user = Json.decodeFromString<User>(response.content ?: "")

                assertEquals("test_user@gmail.com", user.email)
                assertEquals("test_user", user.username)
                assertFalse(checkNotNull(user.token).isBlank())
            }
        }
    }
}
