package com.gu.anghammarad

import software.amazon.awssdk.auth._
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.auth.credentials.{EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse}
import com.gu.anghammarad.common.AnghammaradException.Fail

import scala.io.Source
import scala.util.{Success, Try}


object Config {
  val credentialsProvider = AwsCredentialsProviderChain.of(
    ProfileCredentialsProvider.create("deployTools"),
    EnvironmentVariableCredentialsProvider.create()
  )

  private val s3Client = S3Client
    .builder
    .credentialsProvider(credentialsProvider)
    .region(Region.EU_WEST_1)
    .build()

  private def fetchContent(request: GetObjectRequest): Try[ResponseInputStream[GetObjectResponse]] = {
    Try(s3Client.getObject(request, ResponseTransformer.toInputStream()))
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

    val request = GetObjectRequest.builder().key(key).bucket(bucket).build();

    fetchString(request)
  }
}
