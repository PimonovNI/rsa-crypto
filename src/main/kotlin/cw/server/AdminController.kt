package cw.server

import cw.auth.Authentication
import cw.auth.CatalogPermission
import cw.auth.Role
import cw.auth.UserDto
import cw.server.Observer.recordAction

class AdminController {

    private val maxUserCount = 14

    fun role(token: String): Role =
        Authentication.check(token).role

    fun list(token: String): List<UserDto> {
        val user = Authentication.check(token)
        if (user.role !in setOf(Role.Admin, Role.Manager)) {
            throw NotEnoughPermission("read users data")
        }
        return Authentication.users()
            .also { recordAction(user.id, "list users") }
    }

    fun create(username: String, password: String, role: Role, permissions: Set<CatalogPermission>, token: String) {
        val user = Authentication.check(token)
        if (user.role != Role.Admin) {
            throw NotEnoughPermission("create user")
        }
        val users = Authentication.users()
        if (users.size + 1 > maxUserCount) {
            throw IllegalStateException("Cannot create more than $maxUserCount users.")
        }
        Authentication.create(username, password, role, permissions)
        recordAction(user.id, "create user `$username`")
    }

    fun delete(username: String, token: String) {
        val user = Authentication.check(token)
        if (user.role != Role.Admin) {
            throw NotEnoughPermission("delete user")
        }
        if (Authentication.users().filter { it.role == Role.Admin }.any { it.username == username }) {
            throw IllegalArgumentException("You cannot delete admin.")
        }
        Authentication.deleteUser(username)
        recordAction(user.id, "delete user `$username`")
    }
}
