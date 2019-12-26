package com.yushkevich.watermark

import java.math.BigInteger
import java.security.MessageDigest

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait WatermarkGenerator {
  def generate(input: String): Future[String]
}

object Md5WatermarkGenerator extends WatermarkGenerator with LazyLogging {

  override def generate(input: String): Future[String] = Future {
    logger.info(s"Generating watermark for input: $input")

    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(input.getBytes)
    val bigInt = new BigInteger(1, digest)

    bigInt.toString(16)
  }
}
