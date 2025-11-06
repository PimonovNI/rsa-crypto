package cw.auth

import cw.db.Record
import java.math.BigDecimal
import java.time.Instant

data class Session(
    val id: Int,
    val token: String,
    val expires: Instant,
    val a: Int,
    val question: BigDecimal? = null
) : Record
