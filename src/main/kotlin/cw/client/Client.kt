package cw.client

import cw.auth.Role
import cw.auth.SessionExpired
import cw.server.Server

class Client {

    private lateinit var context: Context
    private lateinit var role: Role
    private val server = Server()
    private val auth = AuthClient(server)

    fun start() {
        context = auth.login()
        role = server.admin().role(context.token)
        menu()
    }

    private fun menu() {
        try {
            if (role == Role.Admin || role == Role.Manager) {
                adminMenu()
            } else {
                userMenu()
            }
        } catch (e: SessionExpired) {
            println("Your session is expired!")
            context = auth.refresh(context)
            role = server.admin().role(context.token)
            menu()
        }
    }

    private fun adminMenu() {
        println("1. Manage users")
        println("2. Manage catalogs")
        println("3. Log out")
        println("4. Quit")
        val option = readln().toIntOrNull()
        when (option) {
            1 -> {
                AdminClient(server.admin(), context.token).start()
                adminMenu()
            }
            2 -> {
                CatalogClient(server.catalog(), context.token).start()
                adminMenu()
            }
            3 -> start()
            4 -> {}
            else -> {
                println("Unknown option.")
                adminMenu()
            }
        }
    }

    private fun userMenu() {
        println("1. Manage catalogs")
        println("2. Log out")
        println("3. Quit")
        val option = readln().toIntOrNull()
        when (option) {
            1 -> {
                CatalogClient(server.catalog(), context.token).start()
                userMenu()
            }
            2 -> start()
            3 -> {}
            else -> {
                println("Unknown option.")
                userMenu()
            }
        }
    }
}
