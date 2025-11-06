package cw.server

import cw.db.FileRepository
import java.time.Instant

object Observer {

    private val userBookStorage = FileRepository("db/us_book.txt", UserBookSerializer())

    fun recordAction(id: Int, name: String, timestamp: Instant = Instant.now()) {
        userBookStorage.create(UserBook(id, name, timestamp))
    }
}
