package com.yushkevich.watermark

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.yushkevich.watermark.Topic.Topic
import spray.json._

sealed trait Publication {
  val content: String
  val author: String
  val title: String
  val watermark: Option[String]
  val ticketId: Option[String]

  def getWatermarkProps: String
}

case class Book(
                 content: String,
                 author: String,
                 title: String,
                 watermark: Option[String],
                 ticketId: Option[String],
                 topic: Topic) extends Publication {
  override def getWatermarkProps: String = s"$content|$title|$author|$topic"
}

case class Journal(
                    content: String,
                    author: String,
                    title: String,
                    watermark: Option[String],
                    ticketId: Option[String]) extends Publication {
  override def getWatermarkProps: String = s"$content|$title|$author"
}

object Topic extends Enumeration {
  type Topic = Value
  val BUSINESS, SCIENCE, MEDIA = Value
}

class EnumJsonConverter[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
  override def write(obj: T#Value): JsValue = JsString(obj.toString)

  override def read(json: JsValue): T#Value = {
    json match {
      case JsString(txt) => enu.withName(txt)
      case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
    }
  }

}

trait PublicationProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val enumConverter: EnumJsonConverter[Topic.type] = new EnumJsonConverter(Topic)
  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat6(Book)
  implicit val journalFormat: RootJsonFormat[Journal] = jsonFormat5(Journal)

  implicit object PublicationJsonFormat extends RootJsonFormat[Publication] {
    def write(publication: Publication): JsValue = {
      publication match {
        case book: Book => bookFormat.write(book)
        case journal: Journal => journalFormat.write(journal)
      }
    }

    def read(value: JsValue): Publication = {
      value match {
        case known: JsObject =>
          if (known.fields.contains("topic") && known.fields.keys.to(Set).diff(Set("content", "author", "title", "topic", "watermark", "ticketId")).isEmpty) {
            bookFormat.read(known)
          } else if (known.fields.keys.to(Set).diff(Set("content", "author", "title", "topic", "watermark", "ticketId")).isEmpty) {
            journalFormat.read(known)
          } else {
            deserializationError("Publication expected")
          }
        case unrecognized => deserializationError(s"Deserialization problem $unrecognized")
      }
    }
  }

}