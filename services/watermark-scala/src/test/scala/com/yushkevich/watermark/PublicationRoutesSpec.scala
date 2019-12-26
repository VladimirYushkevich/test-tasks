package com.yushkevich.watermark

import akka.actor.{ActorRefFactory, ActorSystem, Status}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{StatusCodes, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestActorRef
import com.yushkevich.watermark.Commons._
import com.yushkevich.watermark.actors.PublicationActor.{CreatePublication, DeletePublication, GetPublication, GetPublications}
import com.yushkevich.watermark.actors.{PublicationActor, WatermarkActor}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future
import scala.concurrent.duration._

class PublicationRoutesSpec
  extends WordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with JsonSupport {

  private implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(3.seconds)

  private def routes(testActorRef: TestActorRef[PublicationActor]): Route = PublicationRoutes(testActorRef)

  private val base: String = "/api/v2"
  private val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(null))

  s"GET $base/publications" should {
    "return no publications if no present" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case GetPublications =>
            val originalSender = sender()
            for {
              publications <- Future.successful(Seq.empty)
            } yield {
              originalSender ! Publications(publications)
            }
        }
      })
      val request = HttpRequest(uri = "/api/v2/publications")

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"publications":[]}"""
      }
    }
  }

  s"GET $base/publications/[:ticketID]" should {
    val request = HttpRequest(uri = "/api/v2/publications/ticketId")
    "be able to retrieve publication by ticket id" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case GetPublication(_) =>
            val originalSender = sender()
            for {
              publication <- Future.successful(Some(testPublication))
            } yield {
              originalSender ! publication
            }
        }
      })

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"author":"Author","content":"Content","ticketId":"ticketId","title":"Title","watermark":"watermarkId"}"""
      }
    }

    "return NOT FOUND when no publication found by ticket id" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case GetPublication(_) =>
            val originalSender = sender()
            for {
              publication <- Future.successful(None)
            } yield {
              originalSender ! publication
            }
        }
      })

      request ~> Route.seal(routes(testActorRef)) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }

  s"DELETE $base/publications/[:ticketID]" should {
    val request = Delete(uri = "/api/v2/publications/ticketId")
    "be able to remove publications by ticket id" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case DeletePublication(_) =>
            sender ! "Publication for ticketId='ticketId' deleted."
        }
      })

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`text/plain(UTF-8)`
        entityAs[String] shouldBe "Publication for ticketId='ticketId' deleted."
      }
    }

    "return INTERNAL SERVER ERROR when exception raised during deletion" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case DeletePublication(_) =>
            throw new IllegalStateException("Deletion failed")
        }
      })

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }
  }

  s"POST $base/publications" should {
    val request = Post("/api/v2/publications").withEntity(Marshal(Publication("Content", "Author", "Title", None, None))
      .to[MessageEntity].futureValue)
    "be able to add publications" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case CreatePublication(_) =>
            sender ! "ticketId"
        }
      })

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.Created
        contentType shouldBe ContentTypes.`text/plain(UTF-8)`
        entityAs[String] shouldBe "ticketId"
      }
    }

    "return CONFLICT when publication exists" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case CreatePublication(_) =>
            sender ! Status.Failure(new IllegalStateException("Publication already exits"))
        }
      })

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.Conflict
      }
    }
    "return INTERNAL SERVER ERROR during failed creation" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case CreatePublication(_) =>
            sender ! Status.Failure(new RuntimeException("Application error"))
        }
      })

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }
  }
}
