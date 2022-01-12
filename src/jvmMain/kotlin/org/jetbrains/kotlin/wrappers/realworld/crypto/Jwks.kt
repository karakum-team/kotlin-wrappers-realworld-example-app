package org.jetbrains.kotlin.wrappers.realworld.crypto

import kotlinx.serialization.Serializable

@Serializable
data class Jwks(val keys: List<Map<String, String>>)
