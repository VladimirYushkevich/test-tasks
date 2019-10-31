package com.yushkevich.watermark.actors

import java.util.UUID.randomUUID

import akka.actor.{ Actor, ActorLogging, ActorRef }
import com.yushkevich.watermark.actors.WatermarkActor.Watermark

final case class Publication(
  content: String,
  author: String,
  title: String,
  watermark: Option[String],
  ticketId: Option[String]) {
}

object Publication {

  def of(publication: Publication, watermark: Option[String], ticketId: Option[String]): Publication =
    Publication(
      content = publication.content,
      author = publication.author,
      title = publication.title,
      watermark = watermark,
      ticketId = ticketId)
}

final case class Publications(publications: Seq[Publication])

final case class TicketIdToPublications(ticketIdToPublications: Map[String, Publication])

object PublicationActor {

  final case object GetPublications

  final case class CreatePublication(publication: Publication)

  final case class GetPublication(author: String)

  final case class DeletePublication(name: String)

}

class PublicationActor(watermarkActor: ActorRef) extends Actor with ActorLogging {

  import PublicationActor._

  var publications = Set.empty[Publication]
  var watermarkedPublications = Map.empty[String, Publication]

  def receive: Receive = {
    case GetPublications =>
      sender() ! Publications(publications.toSeq)
    case CreatePublication(publication) =>
      val ticketId = randomUUID().toString

      if (publications.contains(publication)) {
        log.info(s"Publication with ticketId=$ticketId already exits")
        throw new IllegalStateException("Publication already exits")
      }
      publications += (publication)
      log.info(s"Creating watermark for ticketId=$ticketId")
      watermarkActor ! Watermark(ticketId, publication)
      sender() ! ticketId
    case GetPublication(ticketId) =>
      sender() ! watermarkedPublications.get(ticketId)
    case DeletePublication(ticketId) =>
      //      publications.find(_.author == author) foreach { publication => publications -= publication }
      watermarkedPublications -= ticketId
      sender() ! s"Publication for ticketId=$ticketId deleted."
    case Watermark(ticketId, publication) =>
      log.info(s"Watermark ${publication.watermark} is available for search")
      watermarkedPublications += (ticketId -> publication)
  }
}