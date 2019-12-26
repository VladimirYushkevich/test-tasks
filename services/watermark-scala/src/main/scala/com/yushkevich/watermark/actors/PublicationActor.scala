package com.yushkevich.watermark.actors

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, OneForOneStrategy, Props, Status}
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
   * @param watermarkActorMaker   Way to create watermark actor
   * @return
   */
  def props(publicationRepository: PublicationRepository, watermarkActorMaker: ActorRefFactory => ActorRef): Props = Props(
    new PublicationActor(publicationRepository, watermarkActorMaker: ActorRefFactory => ActorRef))

}

class PublicationActor(publicationRepository: PublicationRepository, watermarkActorMaker: ActorRefFactory => ActorRef) extends Actor with ActorLogging {
  private val watermarkActor: ActorRef = watermarkActorMaker(context)

  def receive: Receive = {
    case GetPublications =>
      val originalSender = sender()
      /* By convention, the callback style is to be used for side-effecting responses, such as the database access call
      for example. The monadic style is for “pure” functional representations, free of side-effecting code. */
      for {
        publications <- publicationRepository.get()
      } yield {
        originalSender ! Publications(publications)
      }
    case CreatePublication(publication) =>
      val originalSender = sender()
      publicationRepository.save(publication).onComplete {
        case Success(Some(ticketId)) =>
          watermarkActor ! Watermark(ticketId, publication)
          originalSender ! ticketId
        case Success(None) =>
          originalSender ! Status.Failure(new IllegalStateException("Publication already exits"))
        case Failure(e) =>
          originalSender ! Status.Failure(new RuntimeException("Creation failed, reason: ", e))
      }
    case GetPublication(ticketId) =>
      val originalSender = sender()
      for {
        publication <- publicationRepository.get(ticketId)
      } yield {
        originalSender ! publication
      }
    case DeletePublication(ticketId) =>
      val originalSender = sender()
      publicationRepository.delete(ticketId).onComplete {
        case Success(ticketId) =>
          originalSender ! s"Publication for ticketId='$ticketId' deleted."
        case Failure(e) =>
          originalSender ! Status.Failure(throw new RuntimeException("Deletion failed, reason: ", e))
      }
    case Watermark(ticketId, publication) =>
      log.info(s"Watermark ${publication.watermark} is available for search")
      publicationRepository.index(ticketId, publication)
  }

  // Works only when child (WatermarkActor) created by PublicationActor
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.minute) {
      case _: IllegalArgumentException => Resume //  Parent starts the child actor keeping it internal state
      case _: IllegalStateException => Stop // Stop the child permanently
      case _: Exception => Escalate // Escalate the failure by failing itself and propagate failure to its parent
    }
}