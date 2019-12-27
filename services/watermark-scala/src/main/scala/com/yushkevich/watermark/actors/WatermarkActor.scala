package com.yushkevich.watermark.actors

import akka.actor.{Actor, ActorLogging, Props, Status}
import com.yushkevich.watermark.actors.PublicationActor.IndexPublication
import com.yushkevich.watermark.actors.WatermarkActor.{CreateWatermark, CreationError}
import com.yushkevich.watermark.{Publication, WatermarkGenerator}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object WatermarkActor {

  case class CreateWatermark(ticketId: String, publication: Publication)

  case class CreationError(throwable: Throwable)

  def props(watermarkGenerator: WatermarkGenerator): Props = Props(new WatermarkActor(watermarkGenerator))

}

class WatermarkActor(watermarkGenerator: WatermarkGenerator) extends Actor with ActorLogging {

  def receive: Receive = {
    case CreateWatermark(ticketId, publication) =>
      val originalSender = sender()
      watermarkGenerator.generate(publication) onComplete {
        case Success(watermark) =>
          originalSender ! IndexPublication(ticketId, watermark, publication)
        case Failure(e) =>
          self ! CreationError(e)
      }
    case CreationError(e) =>
      // to handle exception from future
      throw e
  }
}