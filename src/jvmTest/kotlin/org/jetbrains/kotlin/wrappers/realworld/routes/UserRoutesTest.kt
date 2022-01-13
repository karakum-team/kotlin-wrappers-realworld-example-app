package org.jetbrains.kotlin.wrappers.realworld.routes

import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.wrappers.realworld.db.crateHikariDataSource
import org.jetbrains.kotlin.wrappers.realworld.model.Credentials
import org.jetbrains.kotlin.wrappers.realworld.model.User
import org.jetbrains.kotlin.wrappers.realworld.model.UserDraft
import org.jetbrains.kotlin.wrappers.realworld.model.UserInfo
import org.jetbrains.kotlin.wrappers.realworld.prepareTestEnvironment
import kotlin.test.*

class UserRoutesTest {
    lateinit var dataSource: HikariDataSource

    @BeforeTest
    internal fun setUp() {
        dataSource = crateHikariDataSource()
    }

    @AfterTest
    internal fun tearDown() {
        dataSource.close()
    }

    @Test
    fun testUserCreationAndLogin() {
        withApplication(prepareTestEnvironment(dataSource)) {
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
                assertTrue(checkNotNull(user.token).isNotEmpty())
            }

            with(handleRequest(HttpMethod.Post, "/api/users/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

                setBody(
                    Json.encodeToString(
                        Credentials(
                            email = "test_user@gmail.com",
                            password = "querty",
                        )
                    )
                )
            }) {
                assertEquals(HttpStatusCode.OK, response.status())

                val user = Json.decodeFromString<User>(response.content ?: "")

                assertEquals("test_user@gmail.com", user.email)
                assertEquals("test_user", user.username)
                assertTrue(checkNotNull(user.token).isNotEmpty())
            }
        }
    }

    @Test
    fun testDoubleUserCreationWithSameLogin() {
        withApplication(prepareTestEnvironment(dataSource)) {
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
                assertTrue(checkNotNull(user.token).isNotEmpty())
            }

            with(handleRequest(HttpMethod.Post, "/api/users") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

                setBody(
                    Json.encodeToString(
                        UserDraft(
                            email = "other_test_user@gmail.com",
                            username = "test_user",
                            password = "querty",
                        )
                    )
                )
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testUnknownUserLogin() {
        withApplication(prepareTestEnvironment(dataSource)) {
            with(handleRequest(HttpMethod.Post, "/api/users/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

                setBody(
                    Json.encodeToString(
                        Credentials(
                            email = "test_user@gmail.com",
                            password = "querty",
                        )
                    )
                )
            }) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun testUserInfoUpdate() {
        withApplication(prepareTestEnvironment(dataSource)) {
            val token: String

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

                token = checkNotNull(user.token)
            }

            with(handleRequest(HttpMethod.Put, "/api/user") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $token")

                setBody(
                    Json.encodeToString(
                        UserInfo(
                            email = "second_test_user@gmail.com",
                            bio = "I am a developer",
                            image = "Here is my pic",
                        )
                    )
                )
            }) {
                assertEquals(HttpStatusCode.OK, response.status())

                val user = Json.decodeFromString<User>(response.content ?: "")

                assertEquals("second_test_user@gmail.com", user.email)
                assertEquals("test_user", user.username)
                assertEquals("I am a developer", user.bio)
                assertEquals("Here is my pic", user.image)
            }

            with(handleRequest(HttpMethod.Get, "/api/user") {
                addHeader(HttpHeaders.Authorization, "Bearer $token")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())

                val user = Json.decodeFromString<User>(response.content ?: "")

                assertEquals("second_test_user@gmail.com", user.email)
                assertEquals("test_user", user.username)
                assertEquals("I am a developer", user.bio)
                assertEquals("Here is my pic", user.image)
            }
        }
    }
}
