package org.jetbrains.kotlin.wrappers.realworld.service

import com.benasher44.uuid.uuid4
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
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
        val userIsUnique = Users
            .select {
                (Users.email eq userDraft.email) or (Users.username eq userDraft.username)
            }
            .empty()

        if (!userIsUnique) return@transaction null

        val id = uuid4()
        val password = userDraft.password
        val salt = salt()
        val hash = hash(password, salt)

        Users.insert {
            it[this.id] = id
            it[email] = userDraft.email
            it[username] = userDraft.username
            it[this.hash] = hash
            it[this.salt] = salt
        }

        User(
            id = id,
            email = userDraft.email,
            username = userDraft.username,
        )
    }
}
