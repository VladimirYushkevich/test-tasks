package com.yushkevich.watermark

import java.util.UUID.randomUUID

import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PublicationRepository {
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
    watermarkedPublications.values.toSeq
  }

  override def index(ticketId: String, publication: Publication): Unit = {
    watermarkedPublications += (ticketId -> publication)
  }

  override def get(ticketId: String): Future[Option[Publication]] = Future {
    watermarkedPublications.get(ticketId)
  }

  override def delete(ticketId: String): Future[String] = Future {
    val watermarkedPublication = watermarkedPublications(ticketId)
    val publication = watermarkedPublication match {
      case Book(content, author, title, _, _, topic) =>
        Book(content, author, title, None, None, topic)
      case Journal(content, author, title, _, _) =>
        Journal(content, author, title, None, None)
    }
    publications.remove(publication)
    logger.info(s"Deleted from publications: $publication")
    watermarkedPublications -= ticketId
    logger.info(s"Deleted from watermarked publications: $watermarkedPublication")
    ticketId
  }
}
