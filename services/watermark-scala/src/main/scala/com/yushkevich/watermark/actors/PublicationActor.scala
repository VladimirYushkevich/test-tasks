package com.yushkevich.watermark.actors

import java.util.UUID.randomUUID

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, Status}
import com.yushkevich.watermark._
import com.yushkevich.watermark.actors.PublicationActor.{CreatePublication, DeletePublication, GetPublication, GetPublications}
import com.yushkevich.watermark.actors.WatermarkActor.Watermark

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Supervisor
 * Another good practice is to declare what messages an Actor can receive in the companion object of the Actor
 */
object PublicationActor {

  case object GetPublications

  case class CreatePublication(publication: Publication)

  case class GetPublication(ticketId: String)

  case class DeletePublication(name: String)

  /**
   * It is a good idea to provide factory methods on the companion object of each Actor which help keeping
   * the creation of suitable Props as close to the actor definition as possible. This also avoids the pitfalls
   * associated with using the Props.apply(...) method which takes a by-name argument, since within a companion object
   * the given code block will not retain a reference to its enclosing scope
   *
   * @param publicationRepository Publication repository
   * @return
   */
  def props(publicationRepository: PublicationRepository): Props = Props(new PublicationActor(publicationRepository))

}

class PublicationActor(publicationRepository: PublicationRepository) extends Actor with ActorLogging {

  def receive: Receive = {
    case GetPublications =>
      val originalSender = sender
      // By convention, the callback style is to be used for side-effecting responses, such as the database access call
      // for example. The monadic style is for “pure” functional rep‐ resentations, free of side-effecting code.
      for {
        publications <- publicationRepository.get()
      } yield {
        originalSender ! Publications(publications)
      }
    case CreatePublication(publication) =>
      val originalSender = sender
      publicationRepository.save(publication).onComplete {
        case Success(Some(ticketId)) =>
          context.actorOf(Props[WatermarkActor], s"watermarkActor-${randomUUID()}") ! Watermark(ticketId, publication)
          originalSender ! ticketId
        case Success(None) =>
          originalSender ! Status.Failure(new IllegalStateException("Publication already exits"))
        case Failure(e) => Status.Failure(e)
      }
    case GetPublication(ticketId) =>
      val originalSender = sender
      for {
        publication <- publicationRepository.get(ticketId)
      } yield {
        originalSender ! publication
      }
    case DeletePublication(ticketId) =>
      val originalSender = sender
      publicationRepository.delete(ticketId).onComplete {
        case Success(ticketId) =>
          originalSender ! s"Publication for ticketId='$ticketId' deleted."
        case Failure(e) => Status.Failure(e)
      }
    case Watermark(ticketId, publication) =>
      log.info(s"Watermark ${publication.watermark} is available for search")
      publicationRepository.index(ticketId, publication)
  }

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.minute) {
      case _: Exception => Escalate
    }
}