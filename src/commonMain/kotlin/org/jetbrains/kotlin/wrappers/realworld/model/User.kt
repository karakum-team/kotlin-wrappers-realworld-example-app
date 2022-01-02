package org.jetbrains.kotlin.wrappers.realworld.model

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val email: String,
    val username: String,
    val token: String? = null,
    val bio: String? = null,
    val image: String? = null,
)

@Serializable
data class UserDraft(
    val email: String,
    val username: String,
    val password: String,
)

@Serializable
data class UserInfo(
    val email: String? = null,
    val username: String? = null,
    val password: String? = null,
    val bio: String? = null,
    val image: String? = null,
)

@Serializable
data class Credentials(
    val email: String,
    val password: String,
)
