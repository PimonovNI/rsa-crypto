package cw.server

import cw.auth.Authentication
import cw.auth.Permission
import cw.auth.User
import cw.server.Observer.recordAction
import java.io.File

class CatalogController {

    fun list(token: String): Set<String> {
        val user = Authentication.check(token)
        val permissions = user.permissions.map { it.catalog }.toSet()
        val catalogs = File(root).list { file, _ -> file.isDirectory }!!
        return catalogs.filter { permissions.contains(it) }.toSet()
            .also { recordAction(user.id, "list catalogs") }
    }

    fun open(name: String): InCatalogController = InCatalogController(name)

    class InCatalogController(private val name: String) {

        fun permissions(token: String): Set<Permission> {
            return Authentication.check(token).permissions
                .firstOrNull { it.catalog == name }
                ?.permissions
                ?: emptySet()
        }

        fun list(token: String): Set<String> {
            val user = Authentication.check(token)
            if (user.permissions.none { it.catalog == name }) {
                throw NotEnoughPermission("read files from catalog `$name`")
            }
            return File("$root/$name").list()!!.toSet()
                .also { recordAction(user.id, "list files in catalog `$name`") }
        }

        fun create(name: String, content: String, token: String) {
            val user = Authentication.check(token)
            if (!user.permissions(this.name).contains(Permission.W)) {
                throw NotEnoughPermission("write file to catalog `${this.name}`")
            }
            val file = File("$root/${this.name}/$name")
            file.createNewFile()
            file.writeText(content)
            recordAction(user.id, "create file `$name` in catalog `${this.name}")
        }

        fun file(name: String) = FileController(this.name, name)
    }

    class FileController(private val catalog: String, private val name: String) {

        fun read(token: String): String {
            val user = Authentication.check(token)
            if (!user.permissions(catalog).contains(Permission.R)) {
                throw NotEnoughPermission("read file `$name` in catalog `$catalog`")
            }
            return file().readText()
                .also { recordAction(user.id, "read file `$name` in catalog `$catalog`") }
        }

        fun execute(token: String) {
            val user = Authentication.check(token)
            if (!user.permissions(catalog).contains(Permission.E)) {
                throw NotEnoughPermission("execute file `$name` in catalog `$catalog`")
            }
            // Imitation of executing
            println("=== Executing file `$name` in catalog `$catalog` ===")
            println(file().readText())
            println("=== End of execution ===")
            recordAction(user.id, "execute file `$name` in catalog `$catalog`")
        }

        fun rewrite(content: String, token: String) {
            val user = Authentication.check(token)
            if (!user.permissions(catalog).contains(Permission.W)) {
                throw NotEnoughPermission("write into file `$name` in catalog `$catalog`")
            }
            file().writeText(content)
            recordAction(user.id, "rewrite file `$name` in catalog `$catalog`")
        }

        fun append(content: String, token: String) {
            val user = Authentication.check(token)
            if (!user.permissions(catalog).contains(Permission.A)) {
                throw NotEnoughPermission("append content to file `$name` in catalog `$catalog`")
            }
            file().appendText(content)
            recordAction(user.id, "append to file `$name` in catalog `$catalog`")
        }

        private fun file(): File =
            File("$root/$catalog/$name")
    }

    private companion object {
        private val root = "system"

        private fun User.permissions(catalog: String): Set<Permission> {
            return permissions.firstOrNull { it.catalog == catalog }
                ?.permissions
                ?: emptySet()
        }
    }
}
