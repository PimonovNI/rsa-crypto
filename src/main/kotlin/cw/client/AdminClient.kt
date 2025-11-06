package cw.client

import cw.auth.CatalogPermission
import cw.auth.Permission
import cw.auth.Role
import cw.server.AdminController

class AdminClient(
    private val admin: AdminController,
    private val token: String
) {
    fun start() {
        val role = admin.role(token)
        when (role) {
            Role.Manager -> startManager()
            Role.Admin -> startAdmin()
            Role.User -> {
                println("You do not have permission to visit this page.")
            }
        }
    }

    private fun startManager() {
        println("1. List users")
        println("2. Back")
        when (readln().toIntOrNull()) {
            1 -> {
                admin.list(token).forEach { user ->
                    println("${user.id}\t${user.username}\t${user.role}\t${user.permissions}")
                }
                startManager()
            }

            2 -> {}

            else -> {
                println("Unknown option.")
                startManager()
            }
        }
    }

    private fun startAdmin() {
        println("1. List users")
        println("2. Create user")
        println("3. Delete user")
        println("4. Back")
        when (readln().toIntOrNull()) {
            1 -> {
                admin.list(token).forEach { user ->
                    println("${user.id}\t${user.username}\t${user.role}\t${user.permissions}")
                }
                startAdmin()
            }

            2 -> {
                println("Enter username:")
                val username = readln().trim()
                println("Enter password:")
                val password = readln().trim()
                var role: Role
                while (true) {
                    println("Enter role (user/manager):")
                    try {
                        role = Role.valueOf(readln().trim().capitalize())
                    } catch (e: IllegalArgumentException) {
                        continue
                    }
                    break
                }
                val permissions = readPermissions()
                try {
                    admin.create(username, password, role, permissions, token)
                } catch (e: IllegalArgumentException) {
                    println(e.message)
                    startAdmin()
                    return
                } catch (e: IllegalStateException) {
                    println(e.message)
                    startAdmin()
                    return
                }
                println("User `$username` created!")
                startAdmin()
            }

            3 -> {
                println("Enter name:")
                val username = readln().trim()
                try {
                    admin.delete(username, token)
                } catch (e: IllegalArgumentException) {
                    println(e.message)
                    startAdmin()
                    return
                }
                println("User with name `$username` deleted.")
            }

            4 -> {}

            else -> {
                println("Unknown option.")
                startAdmin()
            }
        }
    }

    private fun readPermissions(collected: Set<CatalogPermission> = emptySet()): Set<CatalogPermission> {
        println("1. Add permission")
        println("2. Save and continue")
        val option = readln().toIntOrNull()
        when (option) {
            1 -> {
                println("Enter catalog name:")
                val catalog = readln().trim()
                val permissions = mutableSetOf<Permission>()
                println("Has READ permission (yes/no):")
                if (readln().trim().lowercase() == "yes") {
                    permissions.add(Permission.R)
                }
                println("Has WRITE permission (yes/no):")
                if (readln().trim().lowercase() == "yes") {
                    permissions.add(Permission.W)
                }
                println("Has EXECUTE permission (yes/no):")
                if (readln().trim().lowercase() == "yes") {
                    permissions.add(Permission.E)
                }
                println("Has APPEND permission (yes/no):")
                if (readln().trim().lowercase() == "yes") {
                    permissions.add(Permission.A)
                }
                return readPermissions(collected + CatalogPermission(catalog, permissions))
            }

            2 -> return collected

            else -> {
                println("Unknown option.")
                return readPermissions(collected)
            }
        }
    }
}
