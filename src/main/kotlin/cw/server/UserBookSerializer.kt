package cw.server

import cw.db.RecordSerializer
import java.time.Instant

class UserBookSerializer : RecordSerializer<UserBook> {
    override fun serialize(record: UserBook): String =
        "${record.id}|${record.action}|${record.whenDone}"

    override fun deserialize(str: String): UserBook {
        val parts = str.split("|")
        return UserBook(parts[0].toInt(), parts[1], Instant.parse(parts[2]))
    }
}
