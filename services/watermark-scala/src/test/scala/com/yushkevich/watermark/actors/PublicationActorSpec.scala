package com.yushkevich.watermark.actors

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.yushkevich.watermark.Commons._
import com.yushkevich.watermark.actors.PublicationActor.{CreatePublication, DeletePublication, GetPublications}
import com.yushkevich.watermark.{Publication, PublicationRepository, Publications, WatermarkGenerator}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._

class PublicationActorSpec
  extends TestKit(ActorSystem("PublicationActorSpec"))
    with WordSpecLike
    with ImplicitSender
    with MockFactory
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  /* ScalaMock supports two different styles—expectation-first and record-then-verify (Mockito-style).
  For expectation-first, use mock to create the fake object and expects to set expectations.
  For record-then-verify, use stub to create the fake object, when to setup return values and verify to verify calls.*/
  var publicationRepositoryMock: PublicationRepository = stub[PublicationRepository]
  var watermarkGeneratorMock: WatermarkGenerator = mock[WatermarkGenerator]

  "GetPublications" should {
    val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(watermarkGeneratorMock))
    val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))

    "return list of publication" in {
      (publicationRepositoryMock.get: () => Future[Seq[Publication]]).when().returns(Future.successful(Seq(testPublication)))

      publicationActor ! GetPublications

      expectMsg(Publications(Seq(testPublication)))
    }

    "return empty list of publication" in {
      (publicationRepositoryMock.get: () => Future[Seq[Publication]]).when().returns(Future.successful(Seq.empty))

      publicationActor ! GetPublications

      expectMsg(Publications(Seq.empty))
    }
  }

  "CreatePublication" should {
    "create publication and successfully watermark it" in {
      val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(watermarkGeneratorMock))
      val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))

      (watermarkGeneratorMock.generate _).expects(*).returning(Future.successful("watermark"))
      (publicationRepositoryMock.save _).when(testNewPublication).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewPublication)

      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedPublication).once()
    }

    "create publication and failed to watermark it, watermark actor resumed and available to consume next message" in {
      val testProbe = TestProbe()
      var watermarkActor: ActorRef = null

      val makerMock = (f: ActorRefFactory) => {
        val actorRef = f.actorOf(TestActorRef(WatermarkActor.props(watermarkGeneratorMock)).props)
        testProbe.watch(actorRef)
        watermarkActor = actorRef
        actorRef
      }
      val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))
      val oldWatermarkActor = watermarkActor

      (watermarkGeneratorMock.generate _).expects(*).twice().throwing(new IllegalArgumentException("Failed to create watermark, recoverable"))
      (publicationRepositoryMock.save _).when(testNewPublication).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewPublication)
      publicationActor ! CreatePublication(testNewPublication)

      expectMsg("ticketId")
      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedPublication).never()
      assert(oldWatermarkActor == watermarkActor)
    }

    "create publication and failed to watermark it, watermark actor terminated next message will be not consumed" in {
      val testProbe = TestProbe()
      var watermarkActor: ActorRef = null

      val makerMock = (f: ActorRefFactory) => {
        val actorRef = f.actorOf(TestActorRef(WatermarkActor.props(watermarkGeneratorMock)).props)
        testProbe.watch(actorRef)
        watermarkActor = actorRef
        actorRef
      }
      val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))
      val oldWatermarkActor = watermarkActor

      (watermarkGeneratorMock.generate _).expects(*).once().throwing(new IllegalStateException("Failed to create watermark, fatal"))
      (publicationRepositoryMock.save _).when(testNewPublication).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewPublication)
      publicationActor ! CreatePublication(testNewPublication)

      expectMsg("ticketId")
      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedPublication).never()
      testProbe.expectTerminated(watermarkActor)
      assert(oldWatermarkActor == watermarkActor)
    }

    "create publication and failed to watermark it, watermark actor recreated" in {
      val testProbe = TestProbe()
      var watermarkActor: ActorRef = null

      val makerMock = (f: ActorRefFactory) => {
        val actorRef = f.actorOf(TestActorRef(WatermarkActor.props(watermarkGeneratorMock)).props)
        testProbe.watch(actorRef)
        watermarkActor = actorRef
        actorRef
      }
      val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))
      val oldWatermarkActor = watermarkActor

      (watermarkGeneratorMock.generate _).expects(*).once().throwing(new RuntimeException("Unknown error"))
      (publicationRepositoryMock.save _).when(testNewPublication).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewPublication)

      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedPublication).never()
      testProbe.expectTerminated(oldWatermarkActor)
      assert(oldWatermarkActor != watermarkActor)
    }

    "create publication and failed in future to watermark it, watermark actor recreated" in {
      val testProbe = TestProbe()
      var watermarkActor: ActorRef = null

      val makerMock = (f: ActorRefFactory) => {
        val actorRef = f.actorOf(TestActorRef(WatermarkActor.props(watermarkGeneratorMock)).props)
        testProbe.watch(actorRef)
        watermarkActor = actorRef
        actorRef
      }
      val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))
      val oldWatermarkActor = watermarkActor

      (watermarkGeneratorMock.generate _).expects(*).once().returning(Future.failed(new RuntimeException("Failed future")))
      (publicationRepositoryMock.save _).when(testNewPublication).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewPublication)

      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedPublication).never()
      assert(oldWatermarkActor == watermarkActor)
    }
  }

  "DeletePublication" should {
    val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(watermarkGeneratorMock))
    val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))

    "delete publication successfully" in {
      (publicationRepositoryMock.delete _).when(*).returns(Future.successful("ticketId"))

      publicationActor ! DeletePublication("ticketId")

      expectMsg("Publication for ticketId='ticketId' deleted.")
      expectNoMessage(2.seconds)
    }

    "delete publication failed" in {
      (publicationRepositoryMock.delete _).when(*).returning(Future.failed(new IllegalCallerException("Something failed")))

      publicationActor ! DeletePublication("ticketId")

      expectNoMessage(2.seconds)
    }
  }
}