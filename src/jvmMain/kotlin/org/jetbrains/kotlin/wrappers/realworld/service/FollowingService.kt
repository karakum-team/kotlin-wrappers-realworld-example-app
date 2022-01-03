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
        val (followingUsers, followedUsers) = Users.aliases(
            followerUsername,
            followedUsername,
        )

        Following
            .join(followingUsers, JoinType.INNER, Following.followerId, followingUsers[Users.id])
            .join(followedUsers, JoinType.INNER, Following.followedId, followedUsers[Users.id])
            .slice(booleanLiteral(true))
            .selectAll()
            .empty()
            .let { !it }
    }

    fun follow(
        followerUsername: String,
        followedUsername: String,
    ) = transaction {
        val (followingUsers, followedUsers) = Users.aliases(
            followerUsername,
            followedUsername,
        )

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

        Following.deleteWhere {
            (Following.followerId inSubQuery Users.selectUserId(followerUsername)) and
                    (Following.followedId inSubQuery Users.selectUserId(followedUsername))
        }

        Unit
    }

    private fun Users.selectUserId(username: String) = slice(id).select { Users.username eq username }

    private fun Users.aliases(
        followerUsername: String,
        followedUsername: String,
    ): Pair<QueryAlias, QueryAlias> {
        val followingUsers = selectUserId(followerUsername).alias("following_users")
        val followedUsers = selectUserId(followedUsername).alias("followed_users")

        return followingUsers to followedUsers
    }
}
