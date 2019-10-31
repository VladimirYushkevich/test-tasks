package com.yushkevich.watermark

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.yushkevich.watermark.actors.{ Publication, Publications }
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._

  implicit val publicationJsonFormat = jsonFormat5(Publication.apply)
  implicit val publicationsJsonFormat = jsonFormat1(Publications)
}