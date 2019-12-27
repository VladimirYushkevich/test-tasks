package com.yushkevich.watermark.actors

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Status}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.yushkevich.watermark.Commons._
import com.yushkevich.watermark.actors.PublicationActor.{CreatePublication, DeletePublication, GetPublications}
import com.yushkevich.watermark.{PublicationRepository, WatermarkGenerator}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._

class PublicationActorSpec
  extends TestKit(ActorSystem("test-actor-system"))
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
  private val publicationRepositoryMock: PublicationRepository = stub[PublicationRepository]
  private val watermarkGeneratorMock: WatermarkGenerator = mock[WatermarkGenerator]

  "GetPublications" should {
    val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(watermarkGeneratorMock))
    val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))

    "return list of publication" in {
      (() => publicationRepositoryMock.get()).when().returns(Future.successful(Seq(testWatermarkedJournal, testWatermarkedBook)))

      publicationActor ! GetPublications

      expectMsg(Seq(testWatermarkedJournal, testWatermarkedBook))
    }

    "return empty list of publication" in {
      (() => publicationRepositoryMock.get()).when().returns(Future.successful(Seq.empty))

      publicationActor ! GetPublications

      expectMsg(Seq.empty)
    }
  }

  "CreatePublication" should {
    "create journal and successfully watermark it" in {
      val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(watermarkGeneratorMock))
      val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))

      (watermarkGeneratorMock.generate _).expects(*).returning(Future.successful("journalWatermark"))
      (publicationRepositoryMock.save _).when(testNewJournal).returns(Future.successful(Some("journalTicketId")))

      publicationActor ! CreatePublication(testNewJournal)

      expectMsg("journalTicketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("journalTicketId", testWatermarkedJournal).once()
    }

    "create book and successfully watermark it" in {
      val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(watermarkGeneratorMock))
      val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))

      (watermarkGeneratorMock.generate _).expects(*).returning(Future.successful("bookWatermark"))
      (publicationRepositoryMock.save _).when(testNewBook).returns(Future.successful(Some("bookTicketId")))

      publicationActor ! CreatePublication(testNewBook)

      expectMsg("bookTicketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("bookTicketId", testWatermarkedBook).once()
    }

    "failed to save it" in {
      val makerMock = (f: ActorRefFactory) => f.actorOf(WatermarkActor.props(watermarkGeneratorMock))
      val publicationActor = TestActorRef(PublicationActor.props(publicationRepositoryMock, makerMock))
      val exception = new RuntimeException("Failed to save")

      (publicationRepositoryMock.save _).when(testNewJournal).returns(Future.failed(exception))

      publicationActor ! CreatePublication(testNewJournal)

      expectMsg(Status.Failure(exception))
    }

    "create publication and failed to generate watermark before future, watermark actor resumed and available to consume next message" in {
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

      (watermarkGeneratorMock.generate _).expects(*).twice().throwing(new IllegalArgumentException("Failed to create watermark before future, recoverable"))
      (publicationRepositoryMock.save _).when(testNewJournal).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewJournal)
      publicationActor ! CreatePublication(testNewJournal)

      expectMsg("ticketId")
      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedJournal).never()
      assert(oldWatermarkActor == watermarkActor)
    }

    "create publication and failed to watermark it from future, watermark actor resumed and available to consume next message" in {
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

      (watermarkGeneratorMock.generate _).expects(*).twice().returning(Future.failed(new IllegalArgumentException("Failed to create watermark, recoverable")))
      (publicationRepositoryMock.save _).when(testNewJournal).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewJournal)
      publicationActor ! CreatePublication(testNewJournal)

      expectMsg("ticketId")
      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedJournal).never()
      assert(oldWatermarkActor == watermarkActor)
    }

    "create publication and failed to generate watermark, watermark actor terminated next message will be not consumed" in {
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
      (publicationRepositoryMock.save _).when(testNewJournal).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewJournal)
      publicationActor ! CreatePublication(testNewJournal)

      expectMsg("ticketId")
      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedJournal).never()
      testProbe.expectTerminated(watermarkActor)
      assert(oldWatermarkActor == watermarkActor)
    }

    "create publication and failed to generate watermark, watermark actor recreated" in {
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
      (publicationRepositoryMock.save _).when(testNewJournal).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewJournal)

      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedJournal).never()
      testProbe.expectTerminated(oldWatermarkActor)
      assert(oldWatermarkActor != watermarkActor)
    }

    "create publication and failed in future to generate watermark, watermark actor recreated" in {
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
      (publicationRepositoryMock.save _).when(testNewJournal).returns(Future.successful(Some("ticketId")))

      publicationActor ! CreatePublication(testNewJournal)

      expectMsg("ticketId")
      expectNoMessage(2.seconds)
      (publicationRepositoryMock.index _).verify("ticketId", testWatermarkedJournal).never()
      testProbe.expectTerminated(oldWatermarkActor)
      assert(oldWatermarkActor != watermarkActor)
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
      val exception = new RuntimeException("Something failed")
      (publicationRepositoryMock.delete _).when(*).returning(Future.failed(exception))

      publicationActor ! DeletePublication("ticketId")

      expectMsg(Status.Failure(exception))
    }
  }
}