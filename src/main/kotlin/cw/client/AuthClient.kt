package cw.client

import cw.auth.AuthFunction
import cw.rsa.Rsa
import cw.server.AuthController
import cw.server.Server
import java.io.File
import java.time.Instant
import java.util.UUID

class AuthClient(
    private val server: Server
) {

    fun login(): Context {
        println("Enter username:")
        val username = readln().trim()
        println("Enter password:")
        val password = readln().trim()
        val channel = server.auth().startSecuredChannel()
        val plain = "$username|$password"
        val path = channel.encrypt(plain)
        val res = try {
            channel.login(path)
        } catch (_: NoSuchElementException) {
            println("Invalid username or password. Please try again.")
            return login()
        }
        if (res == null) {
            println("Invalid username or password. Please try again.")
            return login()
        }
        println("You have successfully logged in.")
        println("Your session is valid until ${res.expires}.")
        return Context(res.token, res.a)
    }

    fun refresh(oldContext: Context): Context {
        val channel = server.auth().startSecuredChannel()
        val question = channel.requestRefresh(oldContext.token).question
        val answer = AuthFunction.calculate(question, oldContext.a).toString()
        val path = channel.encrypt(answer)
        val res = channel.refresh(oldContext.token, path)
        if (res == null) {
            println("Session cannot be refreshed. Please login again.")
            return login()
        }
        println("Your session is successfully refreshed.")
        println("Your session is valid until ${res.expires}.")
        return Context(res.token, res.a)
    }

    private fun AuthController.SecuredChannel.encrypt(data: String): String {
        val path = Instant.now().epochSecond.toString() + "-" + UUID.randomUUID().toString()
        val encrypted = Rsa.encrypt(data, publicKey)
        File("crypto/$path").mkdirs()
        val input = File("crypto/$path/input.txt")
        input.createNewFile()
        input.writeText(data)
        val close = File("crypto/$path/close.txt")
        close.createNewFile()
        close.writeText(encrypted)
        return path
    }
}
