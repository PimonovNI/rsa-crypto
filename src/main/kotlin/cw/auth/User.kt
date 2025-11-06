package cw.auth

import cw.db.Record

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val role: Role,
    val permissions: Set<CatalogPermission>
) : Record
