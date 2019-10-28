package com.yushkevich.watermark

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.yushkevich.watermark.WatermarkActor._

import scala.concurrent.Future
import scala.concurrent.duration._

trait WatermarkRoutes extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[WatermarkRoutes])

  // other dependencies that UserRoutes use
  def watermarkActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val watermarkRoutes: Route =
    pathPrefix("api" / "v2" / "watermark") {
      concat(
        pathEnd {
          concat(
            get {
              val publications: Future[Publications] =
                (watermarkActor ? GetPublications).mapTo[Publications]
              complete(publications)
            },
            post {
              entity(as[Publication]) { publication =>
                val publicationCreated: Future[ActionPerformed] =
                  (watermarkActor ? CreatePublication(publication)).mapTo[ActionPerformed]
                onSuccess(publicationCreated) { performed =>
                  log.info("Created publication [{}]: {}", publication.author, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path(Segment) { author =>
          concat(
            get {
              val maybePublication: Future[Option[Publication]] =
                (watermarkActor ? GetPublication(author)).mapTo[Option[Publication]]
              rejectEmptyResponse {
                complete(maybePublication)
              }
            },
            delete {
              val publicationDeleted: Future[ActionPerformed] =
                (watermarkActor ? DeletePublication(author)).mapTo[ActionPerformed]
              onSuccess(publicationDeleted) { performed =>
                log.info("Deleted publication [{}]: {}", author, performed.description)
                complete((StatusCodes.OK, performed))
              }
            })
        })
    }
}

