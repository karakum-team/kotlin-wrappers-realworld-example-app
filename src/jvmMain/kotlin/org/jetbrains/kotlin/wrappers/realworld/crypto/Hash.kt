package org.jetbrains.kotlin.wrappers.realworld.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

fun salt() = ByteArray(16).also { SecureRandom().nextBytes(it) }

fun hash(password: String, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
    return factory.generateSecret(spec).encoded
}
