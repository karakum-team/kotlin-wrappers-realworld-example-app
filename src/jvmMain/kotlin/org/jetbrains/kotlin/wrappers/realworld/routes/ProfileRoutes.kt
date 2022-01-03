package org.jetbrains.kotlin.wrappers.realworld.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.jetbrains.kotlin.wrappers.realworld.model.Profile
import org.jetbrains.kotlin.wrappers.realworld.service.FollowingService
import org.jetbrains.kotlin.wrappers.realworld.service.UserService

fun Route.profileRouting(userService: UserService, followingService: FollowingService) {
    route("/api/profiles") {
        authenticate("auth-jwt", optional = true) {
            get("/{username}") {
                val authorizedUsername = getUsernameOrNull()
                val requestedUsername = call.parameters.getOrFail("username")

                val user = userService.getUser(requestedUsername)

                val following = authorizedUsername?.let {
                    followingService.check(authorizedUsername, requestedUsername)
                }

                if (user == null) {
                    call.respondText("User is not found", status = HttpStatusCode.NotFound)
                } else {
                    call.respond(
                        Profile(
                            username = user.username,
                            bio = user.bio,
                            image = user.image,
                            following = following
                        )
                    )
                }
            }
        }

        authenticate("auth-jwt") {
            post("/{username}") {
                val authorizedUsername = getUsername()
                val requestedUsername = call.parameters.getOrFail("username")

                followingService.follow(authorizedUsername, requestedUsername)
                call.respond(HttpStatusCode.Created)
            }

            delete("/{username}") {
                val authorizedUsername = getUsername()
                val requestedUsername = call.parameters.getOrFail("username")

                followingService.unfollow(authorizedUsername, requestedUsername)
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }
}
