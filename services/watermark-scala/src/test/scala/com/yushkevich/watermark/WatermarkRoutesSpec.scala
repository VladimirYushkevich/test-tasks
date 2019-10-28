package com.yushkevich.watermark

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class WatermarkRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with WatermarkRoutes {
  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe()
  override val watermarkActor: ActorRef =
    system.actorOf(WatermarkActor.props, "watermark")

  lazy val routes: Route = watermarkRoutes

  val base: String = "/api/v2"

  "WatermarkRoutes" should {
    s"return no publications if no present (GET $base/watermark)" in {
      val request = HttpRequest(uri = "/api/v2/watermark")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"publications":[]}""")
      }
    }

    s"be able to add publications (POST $base/watermark)" in {
      val publication = Publication("Author", "Title")
      val publicationEntity = Marshal(publication).to[MessageEntity].futureValue

      val request = Post("/api/v2/watermark").withEntity(publicationEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"description":"Publication Publication(Author,Title) created."}""")
      }
    }

    s"be able to retrieve publication (GET $base/watermark/Author)" in {
      val request = HttpRequest(uri = "/api/v2/watermark/Author")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"author":"Author","title":"Title"}""")
      }
    }

    s"be able to remove publications (DELETE $base/watermark)" in {
      val request = Delete(uri = "/api/v2/watermark/Author")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"description":"Publication for author Author deleted."}""")
      }
    }
  }
}