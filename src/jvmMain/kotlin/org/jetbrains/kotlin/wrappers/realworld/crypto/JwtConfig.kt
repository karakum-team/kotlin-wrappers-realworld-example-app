package org.jetbrains.kotlin.wrappers.realworld.crypto

class JwtConfig(
    val privateKey: String,
    val issuer: String,
    val audience: String,
    val realm: String,
)
