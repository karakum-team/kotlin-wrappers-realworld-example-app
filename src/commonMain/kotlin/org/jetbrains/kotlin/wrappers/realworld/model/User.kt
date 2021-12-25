package org.jetbrains.kotlin.wrappers.realworld.model

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val email: String,
    val username: String,
    val password: String? = null,
    val token: String? = null,
    val bio: String? = null,
    val image: String? = null,
    val following: Boolean? = null
)

@Serializable
data class Credentials(
    val email: String,
    val password: String,
)
