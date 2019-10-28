package com.yushkevich.watermark

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.yushkevich.watermark.WatermarkActor.ActionPerformed
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._

  implicit val publicationJsonFormat = jsonFormat2(Publication)
  implicit val publicationsJsonFormat = jsonFormat1(Publications)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}