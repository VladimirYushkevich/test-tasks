package com.yushkevich.watermark

import akka.actor.{ Actor, ActorLogging, Props }

final case class Publication(author: String, title: String)

final case class Publications(publications: Seq[Publication])

object WatermarkActor {

  final case class ActionPerformed(description: String)

  final case object GetPublications

  final case class CreatePublication(publication: Publication)

  final case class GetPublication(author: String)

  final case class DeletePublication(name: String)

  def props: Props = Props[WatermarkActor]
}

class WatermarkActor extends Actor with ActorLogging {

  import WatermarkActor._

  var publications = Set.empty[Publication]

  def receive: Receive = {
    case GetPublications =>
      sender() ! Publications(publications.toSeq)
    case CreatePublication(publication) =>
      publications += publication
      sender() ! ActionPerformed(s"Publication $publication created.")
    case GetPublication(author) =>
      sender() ! publications.find(_.author == author)
    case DeletePublication(author) =>
      publications.find(_.author == author) foreach { publication => publications -= publication }
      sender() ! ActionPerformed(s"Publication for author $author deleted.")
  }
}