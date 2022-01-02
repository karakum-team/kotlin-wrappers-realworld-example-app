package org.jetbrains.kotlin.wrappers.realworld.routes

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.jetbrains.kotlin.wrappers.realworld.crypto.JwtConfig
import org.jetbrains.kotlin.wrappers.realworld.model.Credentials
import org.jetbrains.kotlin.wrappers.realworld.model.User
import org.jetbrains.kotlin.wrappers.realworld.model.UserDraft
import org.jetbrains.kotlin.wrappers.realworld.model.UserInfo
import org.jetbrains.kotlin.wrappers.realworld.service.UserService
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

fun Route.userRouting(jwtConfig: JwtConfig, jwkProvider: JwkProvider, userService: UserService) {
    route("/api/users") {
        post("login") {
            val credentials = call.receive<Credentials>()
            val user = userService.authorize(credentials)

            if (user == null) {
                call.respondText("Bad credentials", status = HttpStatusCode.Unauthorized)
            } else {
                call.respond(user.copy(token = generateToken(jwtConfig, jwkProvider, user)))
            }
        }

        post {
            val userDraft = call.receive<UserDraft>()
            val user = userService.createUser(userDraft)

            if (user == null) {
                call.respondText("Wrong username or password", status = HttpStatusCode.BadRequest)
            } else {
                call.respond(user.copy(token = generateToken(jwtConfig, jwkProvider, user)))
            }
        }
    }

    authenticate("auth-jwt") {
        route("/api/user") {
            get {
                val username = getUsername()
                val user = userService.getUser(username)

                if (user == null) {
                    call.respondText("User is not found", status = HttpStatusCode.NotFound)
                } else {
                    call.respond(user)
                }
            }

            put {
                val username = getUsername()
                val userInfo = call.receive<UserInfo>()
                val user = userService.updateUser(username, userInfo)

                if (user == null) {
                    call.respondText("User is not found", status = HttpStatusCode.NotFound)
                } else {
                    userInfo.username?.let {
                        call.respond(user.copy(token = generateToken(jwtConfig, jwkProvider, user)))
                    } ?: run {
                        call.respond(user)
                    }
                }
            }
        }
    }
}

private fun generateToken(jwtConfig: JwtConfig, jwkProvider: JwkProvider, user: User): String {
    val publicKey = jwkProvider.get("138248dc-88dc-4562-b642-b418243c2851").publicKey
    val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(jwtConfig.privateKey))
    val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)

    return JWT.create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim("username", user.username)
        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
}

private fun PipelineContext<*, ApplicationCall>.getUsername(): String {
    val principal = requireNotNull(call.principal<JWTPrincipal>())
    return principal.payload.getClaim("username").asString()
}
