package org.jetbrains.kotlin.wrappers.realworld.service

import com.benasher44.uuid.uuid4
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlin.wrappers.realworld.crypto.hash
import org.jetbrains.kotlin.wrappers.realworld.crypto.salt
import org.jetbrains.kotlin.wrappers.realworld.db.tables.Users
import org.jetbrains.kotlin.wrappers.realworld.model.Credentials
import org.jetbrains.kotlin.wrappers.realworld.model.User
import org.jetbrains.kotlin.wrappers.realworld.model.UserDraft

class UserService {
    fun authorize(credentials: Credentials) = transaction {
        Users
            .select {
                Users.email eq credentials.email
            }
            .filter {
                val password = credentials.password
                val salt = it[Users.salt]
                val hash = it[Users.hash]

                hash(password, salt).contentEquals(hash)
            }
            .map {
                User(
                    id = it[Users.id],
                    email = it[Users.email],
                    username = it[Users.username],
                    bio = it[Users.bio],
                    image = it[Users.image],
                )
            }
            .singleOrNull()
    }

    fun createUser(userDraft: UserDraft) = transaction {
        val password = userDraft.password
        val salt = salt()
        val hash = hash(password, salt)

        val resultedValues = Users.insert {
            it[id] = uuid4()
            it[email] = userDraft.email
            it[username] = userDraft.username
            it[this.hash] = hash
            it[this.salt] = salt
        }.resultedValues ?: throw IllegalArgumentException("User $userDraft can not be created")


        resultedValues
            .map {
                User(
                    id = it[Users.id],
                    email = it[Users.email],
                    username = it[Users.username],
                )
            }
            .single()
    }
}
