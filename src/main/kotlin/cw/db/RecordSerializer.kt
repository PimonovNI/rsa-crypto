package cw.db

interface RecordSerializer<R : Record> {
    fun serialize(record: R): String
    fun deserialize(str: String): R
}
