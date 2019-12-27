package com.yushkevich.watermark.actors

import akka.actor.{ActorSystem, Status}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.yushkevich.watermark.Commons._
import com.yushkevich.watermark.WatermarkGenerator
import com.yushkevich.watermark.actors.PublicationActor.IndexPublication
import com.yushkevich.watermark.actors.WatermarkActor.CreateWatermark
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._

class WatermarkActorSpec
  extends TestKit(ActorSystem("test-actor-system"))
  with WordSpecLike
  with ImplicitSender
  with MockFactory
  with Matchers
  with BeforeAndAfterAll {

  private val watermarkGeneratorMock: WatermarkGenerator = mock[WatermarkGenerator]

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "CreateWatermark" should {
    val watermarkActor = TestActorRef(WatermarkActor.props(watermarkGeneratorMock))
    "successfully create journal watermark" in {
      (watermarkGeneratorMock.generate _).expects(*).returning(Future.successful("journalWatermark"))

      watermarkActor ! CreateWatermark("journalTicketId", testNewJournal)

      expectMsg(IndexPublication("journalTicketId", "journalWatermark", testNewJournal))
      expectNoMessage(2.seconds)
    }

    "successfully create book watermark" in {
      (watermarkGeneratorMock.generate _).expects(*).returning(Future.successful("bookWatermark"))

      watermarkActor ! CreateWatermark("bookTicketId", testNewBook)

      expectMsg(IndexPublication("bookTicketId", "bookWatermark", testNewBook))
      expectNoMessage(2.seconds)
    }

    "send error message itself and throw original exception after failed attempt to create publication" in {
      val testProbe = TestProbe()
      testProbe.watch(watermarkActor)
      val exception: RuntimeException = new RuntimeException("Filed to generate watermark in future")
      (watermarkGeneratorMock.generate _).expects(*).returning(Future.failed(exception))

      watermarkActor ! CreateWatermark("bookTicketId", testNewBook)

      expectMsg(Status.Failure(exception))
    }
  }

}
