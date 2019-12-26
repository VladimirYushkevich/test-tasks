package com.yushkevich.watermark.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.yushkevich.watermark.actors.WatermarkActor.Watermark
import com.yushkevich.watermark.{Publication, WatermarkGenerator}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

object WatermarkActor {

  case class Watermark(ticketId: String, publication: Publication)

  def props(watermarkGenerator: WatermarkGenerator): Props = Props(new WatermarkActor(watermarkGenerator))

}

class WatermarkActor(watermarkGenerator: WatermarkGenerator) extends Actor with ActorLogging {

  def receive: Receive = {
    case Watermark(ticketId, publication) =>
      val originalSender = sender()
      watermarkGenerator.generate(publication.content) onComplete {
        case Success(watermark) =>
          originalSender ! Watermark(ticketId, Publication(publication, watermark = Some(watermark), ticketId = Some(ticketId)))
      }
  }
}