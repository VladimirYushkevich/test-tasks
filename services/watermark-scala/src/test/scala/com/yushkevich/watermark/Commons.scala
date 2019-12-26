package com.yushkevich.watermark

object Commons {

  val testPublication: Publication = Publication("Content", "Author", "Title", Some("watermarkId"), Some("ticketId"))
  val testCreatedPublication: Publication = Publication("Content", "Author", "Title", None, Some("ticketId"))
  val testWatermarkedPublication: Publication = Publication("Content", "Author", "Title", Some("watermark"), Some("ticketId"))
  val testNewPublication: Publication = Publication("Content", "Author", "Title", None, None)

}
