package org.jetbrains.kotlin.wrappers.realworld.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlin.wrappers.realworld.db.tables.Following
import org.jetbrains.kotlin.wrappers.realworld.db.tables.Users

class FollowingService {
    fun check(
        followerUsername: String,
        followedUsername: String,
    ) = transaction {
        val followingUsers = Users.alias("following_users")
        val followedUsers = Users.alias("followed_users")

        Following
            .join(followingUsers, JoinType.INNER, Following.followerId, followingUsers[Users.id])
            .join(followedUsers, JoinType.INNER, Following.followedId, followedUsers[Users.id])
            .slice(booleanLiteral(true))
            .select {
                (followingUsers[Users.username] eq followerUsername) and
                        (followedUsers[Users.username] eq followedUsername)
            }
            .empty()
            .let { !it }
    }

    fun follow(
        followerUsername: String,
        followedUsername: String,
    ) = transaction {
        val followerId = selectUserId(followerUsername)
            .map { it[Users.id] }
            .single()
        val followedId = selectUserId(followedUsername)
            .map { it[Users.id] }
            .single()

        Following.insert {
            it[this.followerId] = followerId
            it[this.followedId] = followedId
        }

        Unit
    }

    fun unfollow(
        followerUsername: String,
        followedUsername: String,
    ) = transaction {

        Following.deleteWhere {
            (Following.followerId inSubQuery selectUserId(followerUsername)) and
                    (Following.followedId inSubQuery selectUserId(followedUsername))
        }

        Unit
    }

    private fun selectUserId(username: String) =
        Users
            .slice(Users.id)
            .select {
                Users.username eq username
            }
}
