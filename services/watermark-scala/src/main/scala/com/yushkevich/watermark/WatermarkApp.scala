package com.yushkevich.watermark

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import com.yushkevich.watermark.actors.{PublicationActor, WatermarkActor}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object WatermarkApp extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("watermarkAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val maker = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(Md5WatermarkGenerator), "watermarkActor")
  val publicationActor: ActorRef = system.actorOf(PublicationActor.props(InMemoryPublicationRepository, maker), "publicationActor")

  lazy val routes: Route = PublicationRoutes(publicationActor)

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      logger.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      logger.error(s"Server could not start!", e)
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}