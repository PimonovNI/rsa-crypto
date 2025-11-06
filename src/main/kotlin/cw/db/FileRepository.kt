package cw.db

import java.io.File

class FileRepository<R : Record>(
    path: String,
    private val serializer: RecordSerializer<R>
) {
    private val file = File(path)

    init {
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
    }

    fun findAll(): List<R> =
        file.readLines(Charsets.UTF_8)
            .filter { it.isNotBlank() }
            .map { serializer.deserialize(it) }

    fun findBy(filter: (R) -> Boolean): List<R> =
        findAll().filter(filter)

    fun findOneBy(filter: (R) -> Boolean): R =
        findAll().first(filter)

    fun create(record: R) {
        val line = serializer.serialize(record)
        file.appendText("\n$line\n", Charsets.UTF_8)
    }

    fun updateBy(record: R, selector: (R) -> Boolean) {
        val data = findAll().toMutableList()
        for (i in 0..<data.size) {
            if (selector(data[i])) {
                data[i] = record
            }
        }
        val text = data.map { serializer.serialize(it) }.joinToString("\n")
        file.writeText(text, Charsets.UTF_8)
    }

    fun updateOrCreate(record: R, selector: (R) -> Boolean) {
        val data = findBy(selector)
        if (data.isEmpty()) {
            create(record)
        } else {
            updateBy(record, selector)
        }
    }

    fun deleteBy(selector: (R) -> Boolean) {
        val text = findAll().filter { !selector(it) }
            .map { serializer.serialize(it) }
            .joinToString { "\n" }
        file.writeText(text, Charsets.UTF_8)
    }

    fun clear() {
        file.writeText("", Charsets.UTF_8)
    }
}
