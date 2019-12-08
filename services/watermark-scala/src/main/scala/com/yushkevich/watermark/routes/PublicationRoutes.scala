package com.yushkevich.watermark.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.yushkevich.watermark._
import com.yushkevich.watermark.actors.PublicationActor.{CreatePublication, DeletePublication, GetPublication, GetPublications}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait PublicationRoutes extends JsonSupport with LazyLogging {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  def publicationActorRef: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(1.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val publicationRoutes: Route =
    pathPrefix("api" / "v2" / "publications") {
      concat(
        pathEnd {
          concat(
            get {
              val publications: Future[Publications] =
                (publicationActorRef ? GetPublications).mapTo[Publications]
              complete(publications)
            },
            post {
              entity(as[Publication]) { publication =>
                def handleResponse: Future[String] => Route = {
                  onComplete(_) {
                    case Failure(e) => e match {
                      case _: IllegalStateException => complete(StatusCodes.Conflict)
                      case _: Exception => complete(StatusCodes.InternalServerError)
                    }
                    case Success(ticketId) => complete(StatusCodes.Created, ticketId)
                  }
                }

                val ticketIdCreated: Future[String] =
                  (publicationActorRef ? CreatePublication(publication)).mapTo[String]
                handleResponse(ticketIdCreated)
              }
            })
        },
        path(Segment) { ticketId =>
          concat(
            get {
              val maybePublication: Future[Option[Publication]] =
                (publicationActorRef ? GetPublication(ticketId)).mapTo[Option[Publication]]
              rejectEmptyResponse {
                complete(maybePublication)
              }
            },
            delete {
              val ticketIdDeleted: Future[String] =
                (publicationActorRef ? DeletePublication(ticketId)).mapTo[String]
              onSuccess(ticketIdDeleted) { message =>
                logger.info(message)
                complete((StatusCodes.OK, message))
              }
            })
        })
    }
}

