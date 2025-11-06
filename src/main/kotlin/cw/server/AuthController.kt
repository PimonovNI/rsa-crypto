package cw.server

import cw.auth.Authentication
import cw.auth.AuthenticationResponse
import cw.auth.RefreshRequestResponse
import cw.rsa.Rsa
import cw.rsa.RsaKey
import java.io.File
import java.math.BigDecimal

class AuthController {

    fun startSecuredChannel(): SecuredChannel =
        SecuredChannel()

    class SecuredChannel {

        private val keys = Rsa.generateKeys()

        val publicKey: RsaKey
            get() = keys.public

        fun login(path: String): AuthenticationResponse? {
            val (username, password) = safeDecrypt(path) { data->
                data.split("|")
            }
            return Authentication.authenticate(username, password)
                .also { if (it != null) Observer.recordAction(it.id, "logged in") }
        }

        fun requestRefresh(token: String): RefreshRequestResponse {
            return Authentication.requestRefresh(token)
        }

        fun refresh(token: String, path: String): AuthenticationResponse? {
            val answer = safeDecrypt(path) { data ->
                BigDecimal(data)
            }
            return Authentication.refresh(token, answer)
                .also { if (it != null) Observer.recordAction(it.id, "refreshed session") }
        }

        private fun decrypt(path: String): String {
            val encrypted = File("crypto/$path/close.txt")
            val data = Rsa.decrypt(encrypted.readText(), keys.private)
            val decrypted = File("crypto/$path/output.txt")
            decrypted.createNewFile()
            decrypted.writeText(data)
            return data
        }

        private fun <T> safeDecrypt(path: String, function: (String) -> T): T =
            try {
                val data = decrypt(path)
                function(data)
            } catch (e: Exception) {
                val safe = File("crypto/$path/input.txt").readText()
                function(safe)
            }
    }
}
