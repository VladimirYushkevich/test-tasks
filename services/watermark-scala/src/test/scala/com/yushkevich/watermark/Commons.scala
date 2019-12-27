package com.yushkevich.watermark

object Commons {

  val testWatermarkedBook: Publication = Book("Book Content", "Book Author", "Book Title", Some("bookWatermark"), Some("bookTicketId"), Topic.SCIENCE)
  val testNewBook: Publication = Book("Book Content", "Book Author", "Book Title", None, None, Topic.SCIENCE)

  val testWatermarkedJournal: Publication = Journal("Journal Content", "Journal Author", "Journal Title", Some("journalWatermark"), Some("journalTicketId"))
  val testNewJournal: Publication = Journal("Journal Content", "Journal Author", "Journal Title", None, None)

}
