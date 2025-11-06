package cw.auth

import java.math.BigDecimal
import java.time.Instant

data class AuthenticationResponse(
    val id: Int,
    val token: String,
    val expires: Instant,
    val a: Int
)

data class RefreshRequestResponse(
    val question: BigDecimal
)

data class UserDto(
    val id: Int,
    val username: String,
    val role: Role,
    val permissions: Set<CatalogPermission>
)
