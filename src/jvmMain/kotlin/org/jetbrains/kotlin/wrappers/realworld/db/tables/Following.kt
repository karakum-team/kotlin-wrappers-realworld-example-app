package org.jetbrains.kotlin.wrappers.realworld.db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Following : Table() {
    val followerId = uuid("follower_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val followedId = uuid("followed_id").references(Users.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(followerId, followedId)
}
