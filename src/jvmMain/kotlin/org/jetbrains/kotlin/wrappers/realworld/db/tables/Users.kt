package org.jetbrains.kotlin.wrappers.realworld.db.tables

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = uuid("id")
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 255).uniqueIndex()
    val hash = binary("hash", 128)
    val salt = binary("salt", 128)
    val bio = varchar("bio", 1024).nullable()
    val image = varchar("image", 1024).nullable()

    override val primaryKey = PrimaryKey(id)
}


