package com.yushkevich.watermark

import akka.actor.{ActorRef, ActorRefFactory}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{StatusCodes, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.yushkevich.watermark.Commons._
import com.yushkevich.watermark.actors.{PublicationActor, WatermarkActor}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class PublicationRoutesIntegrationTest
  extends WordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with PublicationProtocol {

  private val maker = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(Md5WatermarkGenerator), "watermarkActorIt")
  private val publicationActor: ActorRef = system.actorOf(PublicationActor.props(InMemoryPublicationRepository, maker), "publicationActorIt")

  private lazy val routes: Route = PublicationRoutes(publicationActor)

  private val base: String = "/api/v2/publications"

  s"GET $base" should {
    val request = HttpRequest(uri = s"$base")
    "return no publications" in {
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe
          """[]"""
      }
    }

    "return some publications" in {
      val bookTicketId: String = createAndValidatePublication(testNewBook)
      val journalTicketId: String = createAndValidatePublication(testNewJournal)

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[Seq[Publication]] should equal(Seq(
          Book(testNewBook.content, testNewBook.author, testNewBook.title, Some("d263f6e586e70eafc8c7424d6440d1b6"), Some(bookTicketId), Topic.SCIENCE),
          Journal(testNewJournal.content, testNewJournal.author, testNewJournal.title, Some("272dfc41733c2efec33863c8b0c87982"), Some(journalTicketId))
        ))
      }

      deletePublication(bookTicketId)
      deletePublication(journalTicketId)
    }
  }

  s"GET $base/:ticketId" should {
    "return NOT FOUND" in {
      val request = HttpRequest(uri = s"$base/ticketId")

      request ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return some publication" in {
      val bookTicketId: String = createAndValidatePublication(testNewBook)
      val request = HttpRequest(uri = s"$base/$bookTicketId")

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[Publication] shouldBe Book(testNewBook.content, testNewBook.author, testNewBook.title, Some("d263f6e586e70eafc8c7424d6440d1b6"), Some(bookTicketId), Topic.SCIENCE)
      }

      deletePublication(bookTicketId)
    }
  }

  s"POST $base" should {
    "return CONFLICT when publication already exist" in {
      val bookTicketId: String = createAndValidatePublication(testNewBook)
      val sameBookRequest = Post(s"$base").withEntity(Marshal(testNewBook).to[MessageEntity].futureValue)

      sameBookRequest ~> routes ~> check {
        status shouldBe StatusCodes.Conflict
      }

      deletePublication(bookTicketId)
    }
  }

  s"DELETE $base" should {
    "return NOT FOUND for not existing publication" in {
      val notExistingPublicationRequest = Delete(uri = s"$base/ticketId")

      notExistingPublicationRequest ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }

  private def createAndValidatePublication(publication: Publication): String = {
    val bookRequest = Post(s"$base").withEntity(Marshal(publication).to[MessageEntity].futureValue)

    var ticketId: String = null
    bookRequest ~> routes ~> check {
      status shouldBe StatusCodes.Created
      contentType shouldBe ContentTypes.`text/plain(UTF-8)`
      ticketId = responseAs[String]
      responseAs[String] should not be empty
    }

    ticketId
  }

  private def deletePublication(ticketId: String) = {
    val request = Delete(uri = s"$base/$ticketId")

    request ~> routes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`text/plain(UTF-8)`
      entityAs[String] shouldBe s"Publication for ticketId='$ticketId' deleted."
    }
  }
}
