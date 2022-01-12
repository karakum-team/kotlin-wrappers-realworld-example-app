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
        Following
            .select {
                (Following.followerId inSubQuery Users.selectUserId(followerUsername)) and
                        (Following.followedId inSubQuery Users.selectUserId(followedUsername))
            }
            .empty()
            .let { !it }
    }

    fun follow(
        followerUsername: String,
        followedUsername: String,
    ) = transaction {
        val followingUsers = Users.selectUserId(followerUsername).alias("following_users")
        val followedUsers = Users.selectUserId(followedUsername).alias("followed_users")

        Following.insert(
            (followingUsers crossJoin followedUsers)
                .slice(
                    followingUsers[Users.id],
                    followedUsers[Users.id],
                )
                .selectAll(),
            columns = listOf(
                Following.followerId,
                Following.followedId,
            )
        )

        Unit
    }

    fun unfollow(
        followerUsername: String,
        followedUsername: String,
    ) = transaction {

        Following
            .deleteWhere {
                (Following.followerId inSubQuery Users.selectUserId(followerUsername)) and
                        (Following.followedId inSubQuery Users.selectUserId(followedUsername))
            }

        Unit
    }

    private fun Users.selectUserId(username: String) = slice(id).select { Users.username eq username }
}
