package org.jetbrains.kotlin.wrappers.realworld.crypto

import com.benasher44.uuid.uuid4
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

fun main() {
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    val keyPair = gen.generateKeyPair()

    println(keyPair.private)
    println(keyPair.public)

    val modulus = (keyPair.private as RSAPrivateKey).modulus
    val exponent = (keyPair.public as RSAPublicKey).publicExponent

    println("modulus: ${Base64.getUrlEncoder().withoutPadding().encodeToString(modulus.toByteArray())}")
    println("exponent: ${Base64.getUrlEncoder().withoutPadding().encodeToString(exponent.toByteArray())}")

    val privateKeyEncoded = (keyPair.private as RSAPrivateKey).encoded

    println("privateKey: ${Base64.getEncoder().encodeToString(privateKeyEncoded)}")

    println("kid: ${uuid4()}")
}
