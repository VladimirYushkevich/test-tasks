package com.yushkevich.watermark.actors

import java.math.BigInteger
import java.security.MessageDigest

import akka.actor.{ Actor, ActorLogging }

object WatermarkActor {

  final case class Watermark(ticketId: String, publication: Publication)

}

class WatermarkActor extends Actor with ActorLogging {

  import WatermarkActor._

  var publications = Set.empty[Publication]
  var ticketIdToPublication = Map.empty[String, Publication]

  def receive: Receive = {
    case Watermark(ticketId, publication) =>
      val watermark = md5HashString(publication.content, 1000)
      log.info(s"Created watermark=$watermark for ticketId=$ticketId")
      sender() ! Watermark(ticketId, Publication.of(publication, watermark = Some(watermark), ticketId = Some(ticketId)))
  }

  private def md5HashString(input: String, timeout: Int): String = {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(input.getBytes)
    val bigInt = new BigInteger(1, digest)

    Thread.sleep(timeout)
    bigInt.toString(16)
  }
}