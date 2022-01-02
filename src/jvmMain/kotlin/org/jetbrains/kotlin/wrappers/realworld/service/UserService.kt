package org.jetbrains.kotlin.wrappers.realworld.service

import com.benasher44.uuid.uuid4
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlin.wrappers.realworld.crypto.hash
import org.jetbrains.kotlin.wrappers.realworld.crypto.salt
import org.jetbrains.kotlin.wrappers.realworld.db.tables.Users
import org.jetbrains.kotlin.wrappers.realworld.model.Credentials
import org.jetbrains.kotlin.wrappers.realworld.model.User
import org.jetbrains.kotlin.wrappers.realworld.model.UserDraft
import org.jetbrains.kotlin.wrappers.realworld.model.UserInfo

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
            .map(::toUser)
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

    fun getUser(username: String) = transaction {
        Users
            .select {
                Users.username eq username
            }
            .map(::toUser)
            .singleOrNull()
    }

    fun updateUser(username: String, userInfo: UserInfo) = transaction {
        val user = getUser(username) ?: return@transaction null

        Users.update({ Users.id eq user.id }) { query ->
            userInfo.email?.let { query[email] = it }
            userInfo.username?.let { query[this.username] = it }

            userInfo.password?.let {
                val salt = salt()
                val hash = hash(it, salt)

                query[this.hash] = hash
                query[this.salt] = salt
            }

            userInfo.bio?.let { query[bio] = it }
            userInfo.image?.let { query[image] = it }
        }

        User(
            id = user.id,
            email = userInfo.email ?: user.email,
            username = userInfo.username ?: user.username,
            bio = userInfo.bio ?: user.bio,
            image = userInfo.image ?: user.image,
        )
    }

    private fun toUser(resultRow: ResultRow) = User(
        id = resultRow[Users.id],
        email = resultRow[Users.email],
        username = resultRow[Users.username],
        bio = resultRow[Users.bio],
        image = resultRow[Users.image],
    )
}
