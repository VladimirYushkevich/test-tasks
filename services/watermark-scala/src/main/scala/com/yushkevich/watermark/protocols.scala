package com.yushkevich.watermark

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

case class Publication(
                        content: String,
                        author: String,
                        title: String,
                        watermark: Option[String],
                        ticketId: Option[String]) {
}

object Publication {
  def apply(publication: Publication, watermark: Option[String], ticketId: Option[String]): Publication =
    Publication(
      content = publication.content,
      author = publication.author,
      title = publication.title,
      watermark = watermark,
      ticketId = ticketId)
}

case class Publications(publications: Seq[Publication])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  val publicationApply: ((String, String, String, Option[String], Option[String]) => Publication) = Publication.apply
  implicit val publicationFormat: RootJsonFormat[Publication] = jsonFormat5(publicationApply)
  implicit val publicationsFormat: RootJsonFormat[Publications] = jsonFormat1(Publications)
}