package cw.auth

import cw.db.RecordSerializer

class UserSerializer : RecordSerializer<User> {
    override fun serialize(record: User): String {
        val perm = record.permissions.joinToString("*") {
            it.catalog + "-" + it.permissions.joinToString(",") { p -> p.name }
        }
        return "${record.id}|${record.username}|${record.password}|${record.role.name}|$perm"
    }

    override fun deserialize(str: String): User {
        val parts = str.split("|")
        val permissions = parts[4].split("*").map { data ->
            val (catalog, perm) = data.split("-")
            val permissions = perm.split(",")
                .map { Permission.valueOf(it) }
                .toSet()
            CatalogPermission(catalog, permissions)
        }.toSet()
        return User(parts[0].toInt(), parts[1], parts[2], Role.valueOf(parts[3]), permissions)
    }
}
