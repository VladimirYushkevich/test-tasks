package com.yushkevich.watermark

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.yushkevich.watermark.actors.PublicationActor.{CreatePublication, DeletePublication, GetPublication, GetPublications}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object PublicationRoutes extends JsonSupport with LazyLogging {
  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(1.seconds) // usually we'd obtain the timeout from the system's configuration

  def apply(publicationActor: ActorRef)(implicit ec: ExecutionContext): Route =
    pathPrefix("api" / "v2" / "publications") {
      concat(
        pathEnd {
          concat(
            get {
              complete((publicationActor ? GetPublications).mapTo[Publications])
            },
            post {
              entity(as[Publication]) { publication =>
                onComplete((publicationActor ? CreatePublication(publication)).mapTo[String]) {
                  case Failure(e) => e match {
                    case _: IllegalStateException => complete(StatusCodes.Conflict)
                    case _: Exception => complete(StatusCodes.InternalServerError)
                  }
                  case Success(ticketId) => complete(StatusCodes.Created, ticketId)
                }
              }
            })
        },
        rejectEmptyResponse {
          path(Segment) { ticketId =>
            concat(
              get {
                complete((publicationActor ? GetPublication(ticketId)).mapTo[Option[Publication]])
              },
              delete {
                complete((publicationActor ? DeletePublication(ticketId)).mapTo[String])
              })
          }
        })
    }
}
