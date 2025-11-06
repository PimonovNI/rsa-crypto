package cw.client

import cw.auth.Permission
import cw.server.CatalogController
import cw.server.NotEnoughPermission

class CatalogClient(
    private val server: CatalogController,
    private val token: String
) {

    fun start() {
        println("1. List catalogs")
        println("2. Open catalog")
        println("3. Back")
        val option = readln().toIntOrNull()
        when (option) {
            1 -> {
                val catalogs = server.list(token)
                catalogs.forEach(::println)
                start()
            }

            2 -> {
                println("Enter name:")
                val name = readln()
                val catalogs = server.list(token)
                if (!catalogs.contains(name)) {
                    println("Invalid name or do not enough permission to this catalog.")
                    start()
                    return
                }
                println("Catalog `$name` successfully opened.")
                inCatalog(name)
                start()
            }

            3 -> {}

            else -> {
                println("Unknown option.")
                start()
            }
        }
    }

    private fun inCatalog(name: String) {
        val permissions = server.open(name).permissions(token)
        try {
            val actions = actions(permissions)
            actions.forEach { (index, action) ->
                println("$index. ${action.name}")
            }
            when (val option = readln().toIntOrNull()) {
                in actions.keys -> {
                    actions[option]?.action?.invoke(name)
                }

                else -> {
                    println("Unknown option.")
                    inCatalog(name)
                }
            }
        } catch (e: NotEnoughPermission) {
            println(e.message)
            inCatalog(name)
        }
    }

    private val fileAction = listOf(
        setOf(Permission.R) to Action("List files") { catalog ->
            server.open(catalog)
                .list(token)
                .forEach(::println)
            inCatalog(catalog)
        },
        setOf(Permission.R) to Action("Read file content") { catalog ->
            val fileName = fileName(catalog) ?: return@Action
            val content = server.open(catalog).file(fileName).read(token)
            println(content)
            inCatalog(catalog)
        },
        setOf(Permission.W) to Action("Create a new file") { catalog ->
            println("Enter file name:")
            val name = readln().trim()
            val files = server.open(catalog)
                .list(token)
            if (files.contains(name)) {
                println("Files with name `$name` already exists.")
                inCatalog(catalog)
                readln()
            }
            println("Enter content:")
            val content = readln().trim()
            server.open(catalog).create(name, content, token)
            inCatalog(catalog)
        },
        setOf(Permission.W) to Action("Rewrite file") { catalog ->
            val fileName = fileName(catalog) ?: return@Action
            println("Enter content:")
            val content = readln().trim()
            server.open(catalog).file(fileName).rewrite(content, token)
            inCatalog(catalog)
        },
        setOf(Permission.E) to Action("Execute file") { catalog ->
            val fileName = fileName(catalog) ?: return@Action
            server.open(catalog).file(fileName).execute(token)
            inCatalog(catalog)
        },
        setOf(Permission.A) to Action("Append line to file") { catalog ->
            val fileName = fileName(catalog) ?: return@Action
            println("Enter content:")
            val content = readln().trim()
            server.open(catalog).file(fileName).append(content + "\n", token)
            inCatalog(catalog)
        }
    )

    private fun fileName(catalog: String): String? {
        println("Enter file name:")
        val fileName = readln().trim()
        val files = server.open(catalog)
            .list(token)
        if (!files.contains(fileName)) {
            println("Invalid name or do not enough permission to this catalog.")
            return null
        }
        return fileName
    }

    private fun actions(permissions: Set<Permission>): Map<Int, Action> =
        fileAction.filter { action -> action.first.any { permissions.contains(it) } }
            .map { it.second }
            .plus(Action("Back") {})
            .mapIndexed { index, action -> index + 1 to action }
            .toMap()

    private class Action(
        val name: String,
        val action: (String) -> Unit
    )
}
