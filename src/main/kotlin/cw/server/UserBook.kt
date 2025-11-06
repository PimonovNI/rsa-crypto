package cw.server

import cw.db.Record
import java.time.Instant

data class UserBook(
    val id: Int,
    val action: String,
    val whenDone: Instant
) : Record
