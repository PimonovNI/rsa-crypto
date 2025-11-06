package cw.auth

import cw.db.FileRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

object Authentication {
    private val t = 10
    private val userRepository = FileRepository("db/username.txt", UserSerializer())
    private val sessionRepository = FileRepository("db/session.txt", SessionSerializer())
    private val askRepository = FileRepository("db/ask.txt", AskSerializer())

    fun create(username: String, password: String, role: Role, permissions: Set<CatalogPermission>) {
        synchronized(this) {
            val users = userRepository.findAll()
            if (users.any { it.username == username }) {
                throw IllegalArgumentException("User with name `$username` already created.")
            }
            val lastId = users.maxOfOrNull { it.id } ?: -1
            val user = User(lastId + 1, username, password, role, permissions)
            userRepository.create(user)
        }
    }

    fun users(): List<UserDto> {
        synchronized(this) {
            return userRepository.findAll().map { UserDto(it.id, it.username, it.role, it.permissions) }
        }
    }

    fun deleteUser(username: String) {
        synchronized(this) {
            userRepository.deleteBy { it.username == username }
        }
    }

    fun authenticate(username: String, password: String): AuthenticationResponse? {
        synchronized(this) {
            val user = userRepository.findOneBy { it.username == username }
            if (user.password != password) {
                return null
            }
            return createSession(user.id)
        }
    }

    private fun createSession(id: Int): AuthenticationResponse {
        val token = UUID.randomUUID().toString()
        val expires = Instant.now().plusSeconds(t * 60L)
        val a = Random.nextInt(1, 1000)
        val session = Session(id, token, expires, a)
        sessionRepository.updateOrCreate(session) { it.id == id }
        return AuthenticationResponse(id, token, expires, a)
    }

    fun check(token: String): User {
        synchronized(this) {
            val session = sessionRepository.findOneBy { it.token == token }
            if (Instant.now().isAfter(session.expires)) {
                throw SessionExpired()
            }
            return userRepository.findOneBy { it.id == session.id }
        }
    }

    fun requestRefresh(token: String): RefreshRequestResponse {
        synchronized(this) {
            val session = sessionRepository.findOneBy { it.token == token }
            val question = askRepository.findAll().random().value
            val newSession = Session(session.id, session.token, session.expires, session.a, question)
            sessionRepository.updateBy(newSession) { it.id == session.id }
            return RefreshRequestResponse(question)
        }
    }

    fun refresh(token: String, answer: BigDecimal): AuthenticationResponse? {
        synchronized(this) {
            val session = sessionRepository.findOneBy { it.token == token }
            val expected = AuthFunction.calculate(session.question!!, session.a)
            if (answer != expected) {
                sessionRepository.deleteBy { it.id == session.id }
                return null
            }
            return createSession(session.id)
        }
    }
}
