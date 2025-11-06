package cw.auth

import cw.db.Record
import java.math.BigDecimal

data class Ask(
    val value: BigDecimal
) : Record
