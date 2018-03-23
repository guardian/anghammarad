package com.gu.anghammarad

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{GetObjectRequest, S3ObjectInputStream}

import scala.io.Source
import scala.util.Try


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

  def getStage(): String = Option(System.getenv("Stage")).getOrElse("DEV")

  def loadConfig(stage: String): Try[String] = {
    val bucket = s"anghammarad-configuration/$stage"
    val key = s"anghammarad-config.json"

    val request = new GetObjectRequest(bucket, key)
    fetchString(request)
  }
}
