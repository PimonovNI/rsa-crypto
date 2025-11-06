package cw.auth

import cw.db.RecordSerializer
import java.math.BigDecimal

class AskSerializer : RecordSerializer<Ask> {
    override fun serialize(record: Ask): String =
        record.value.toString()

    override fun deserialize(str: String): Ask {
        return Ask(BigDecimal(str))
    }
}
