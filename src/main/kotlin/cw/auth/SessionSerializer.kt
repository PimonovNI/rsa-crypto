package cw.auth

import cw.db.RecordSerializer
import java.math.BigDecimal
import java.time.Instant

class SessionSerializer : RecordSerializer<Session> {
    override fun serialize(record: Session): String =
        "${record.id}|${record.token}|${record.expires}|${record.a}" +
                if (record.question != null) {
                    "|${record.question}"
                } else ""

    override fun deserialize(str: String): Session {
        val parts = str.split("|")
        val question = if(parts.size < 5) {
            null
        } else {
            BigDecimal(parts[4])
        }
        return Session(parts[0].toInt(), parts[1], Instant.parse(parts[2]), parts[3].toInt(), question)
    }
}
