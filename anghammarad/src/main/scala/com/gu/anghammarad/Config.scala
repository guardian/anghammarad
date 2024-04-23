package com.gu.anghammarad

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{GetObjectRequest, S3ObjectInputStream}
import com.gu.anghammarad.common.AnghammaradException.Fail

import scala.io.Source
import scala.util.{Success, Try}


object Config {
  val credentialsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("deployTools"),
    new EnvironmentVariableCredentialsProvider()
  )

  private val s3Client = AmazonS3Client
    .builder
    .withCredentials(credentialsProvider)
    .withRegion(Regions.EU_WEST_1)
    .build()

  private def fetchContent(request: GetObjectRequest): Try[S3ObjectInputStream] = {
    Try(s3Client.getObject(request).getObjectContent)
  }

  private def fetchString(request: GetObjectRequest): Try[String] = {
    for {
      s3Stream <- fetchContent(request)
      contentString <- Try(Source.fromInputStream(s3Stream).mkString)
      _ <- Try(s3Stream.close())
    } yield contentString
  }

  def getStage(): Try[String] = {
    Option(System.getenv("Stage")) match {
      case Some(stage) => Success(stage)
      case None => Fail("Could nto find environment variable, 'Stage'")
    }
  }

  def loadConfig(stage: String): Try[String] = {
    val bucket = s"anghammarad-configuration"
    val key = s"$stage/anghammarad-config.json"

    val request = new GetObjectRequest(bucket, key)
    fetchString(request)
  }
}
