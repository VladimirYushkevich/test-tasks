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
  with PublicationProtocol {

  private implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(3.seconds)

  private def routes(testActorRef: TestActorRef[PublicationActor]): Route = PublicationRoutes(testActorRef)

  private val base: String = "/api/v2/publications"
  private val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(null))

  s"GET $base" should {
    "return publications if present" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case GetPublications =>
            val originalSender = sender()
            for {
              publications <- Future.successful(Seq(testWatermarkedJournal, testWatermarkedBook))
            } yield {
              originalSender ! publications
            }
        }
      })
      val request = HttpRequest(uri = s"$base")

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe
          """[{"author":"Journal Author","content":"Journal Content","ticketId":"journalTicketId","title":"Journal Title","watermark":"journalWatermark"},{"author":"Book Author","content":"Book Content","ticketId":"bookTicketId","title":"Book Title","topic":"SCIENCE","watermark":"bookWatermark"}]"""
      }
    }

    "return no publications if no present" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case GetPublications =>
            val originalSender = sender()
            for {
              publications <- Future.successful(Seq.empty)
            } yield {
              originalSender ! publications
            }
        }
      })
      val request = HttpRequest(uri = s"$base")

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """[]"""
      }
    }
  }

  s"GET $base/:ticketID" should {
    val request = HttpRequest(uri = s"$base/ticketId")
    "be able to retrieve journal by ticket id" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case GetPublication(_) =>
            val originalSender = sender()
            for {
              publication <- Future.successful(Some(testWatermarkedJournal))
            } yield {
              originalSender ! publication
            }
        }
      })

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"author":"Journal Author","content":"Journal Content","ticketId":"journalTicketId","title":"Journal Title","watermark":"journalWatermark"}"""
      }
    }

    "be able to retrieve book by ticket id" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case GetPublication(_) =>
            val originalSender = sender()
            for {
              publication <- Future.successful(Some(testWatermarkedBook))
            } yield {
              originalSender ! publication
            }
        }
      })

      request ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"author":"Book Author","content":"Book Content","ticketId":"bookTicketId","title":"Book Title","topic":"SCIENCE","watermark":"bookWatermark"}"""
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

  s"DELETE $base/:ticketID" should {
    val request = Delete(uri = s"$base/ticketId")
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

  s"POST $base" should {
    val journalRequest = Post(s"$base").withEntity(Marshal(testNewJournal).to[MessageEntity].futureValue)
    "be able to add journal" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case CreatePublication(_) =>
            sender ! "journalTicketId"
        }
      })

      journalRequest ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.Created
        contentType shouldBe ContentTypes.`text/plain(UTF-8)`
        entityAs[String] shouldBe "journalTicketId"
      }
    }

    "be able to add book" in {
      val bookRequest = Post(s"$base").withEntity(Marshal(testNewBook).to[MessageEntity].futureValue)
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case CreatePublication(_) =>
            sender ! "bookTicketId"
        }
      })

      bookRequest ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.Created
        contentType shouldBe ContentTypes.`text/plain(UTF-8)`
        entityAs[String] shouldBe "bookTicketId"
      }
    }

    "return CONFLICT when publication exists" in {
      val testActorRef: TestActorRef[PublicationActor] = TestActorRef(new PublicationActor(null, makerMock) {
        override def receive: Receive = {
          case CreatePublication(_) =>
            sender ! Status.Failure(new IllegalStateException("Publication already exits"))
        }
      })

      journalRequest ~> routes(testActorRef) ~> check {
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

      journalRequest ~> routes(testActorRef) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }
  }
}
