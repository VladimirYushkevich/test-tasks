package com.yushkevich.watermark

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.yushkevich.watermark.actors.{PublicationActor, WatermarkActor}
import com.yushkevich.watermark.routes.PublicationRoutes
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class PublicationRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with PublicationRoutes {
  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe()
  val watermarkActorRef: ActorRef = system.actorOf(Props[WatermarkActor], "watermarkActor")
  override val publicationActorRef: ActorRef = system.actorOf(Props(classOf[PublicationActor], watermarkActorRef), "publicationActor")

  lazy val routes: Route = publicationRoutes

  val base: String = "/api/v2"

  "PublicationRoutes" should {
    s"return no publications if no present (GET $base/publications)" in {
      val request = HttpRequest(uri = "/api/v2/publications")

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"publications":[]}"""
      }
    }

    s"be able to add publications (POST $base/publications)" in {
      val publication = Publication("Content", "Author", "Title", None, None)
      val publicationEntity = Marshal(publication).to[MessageEntity].futureValue

      val request = Post("/api/v2/publications").withEntity(publicationEntity)

      request ~> routes ~> check {
        status shouldBe StatusCodes.Created
        contentType shouldBe ContentTypes.`text/plain(UTF-8)`
        entityAs[String] should not be empty
      }

      Thread.sleep(3000)
    }

    s"be able to retrieve publication (GET $base/publications/Author)" in {
      val request = HttpRequest(uri = "/api/v2/publications/Author")

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"author":"Author","content":"Content","title":"Title"}"""
      }
    }

    s"be able to remove publications (DELETE $base/publications)" in {
      val request = Delete(uri = "/api/v2/publications/Author")

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`text/plain(UTF-8)`
        entityAs[String] should not be empty
      }
    }
  }
}