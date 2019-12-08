package com.yushkevich.watermark

import java.util.UUID.randomUUID

import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait PublicationRepository {
  def save(publication: Publication): Future[Option[String]]

  def get(): Future[Seq[Publication]]

  def get(ticketId: String): Future[Option[Publication]]

  def index(ticketId: String, publication: Publication): Unit

  def delete(ticketId: String): Future[String]
}

object InMemoryPublicationRepository extends PublicationRepository with LazyLogging {
  var publications: mutable.Set[Publication] = scala.collection.mutable.Set[Publication]()
  var watermarkedPublications = Map.empty[String, Publication]

  override def save(publication: Publication): Future[Option[String]] = Future {
    logger.info(s"Publications: $publications, saving $publication")

    if (publications.contains(publication)) {
      logger.warn(s"Publication $publication already exits")
      None
    } else {
      publications += publication

      Some(randomUUID().toString)
    }
  }

  override def get(): Future[Seq[Publication]] = Future {
    publications.toSeq
  }

  override def index(ticketId: String, publication: Publication): Unit = {
    watermarkedPublications += (ticketId -> publication)
  }

  override def get(ticketId: String): Future[Option[Publication]] = Future {
    watermarkedPublications.get(ticketId)
  }

  override def delete(ticketId: String): Future[String] = Future {
    watermarkedPublications -= ticketId
    ticketId
  }
}
