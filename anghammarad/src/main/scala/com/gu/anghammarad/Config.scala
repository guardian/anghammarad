package com.gu.anghammarad

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{GetObjectRequest, S3ObjectInputStream}
import com.gu.anghammarad.models._

import scala.io.Source
import scala.util.{Failure, Try}


object Env {
  case class Env(app: String, stack: String, stage: String) {
    override def toString: String = s"App: $app, Stack: $stack, Stage: $stage"
  }

  def apply(): Env = Env(
    Option(System.getenv("App")).getOrElse("DEV"),
    Option(System.getenv("Stack")).getOrElse("DEV"),
    Option(System.getenv("Stage")).getOrElse("DEV")
  )
}

object Config {
  private val CredentialsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("deployTools"),
    new EnvironmentVariableCredentialsProvider()
  )

  private val s3Client = AmazonS3Client
    .builder
    .withCredentials(CredentialsProvider)
    .build()

  private def fetchContent(request: GetObjectRequest): Try[S3ObjectInputStream] = {
    val contentRequest = Try(s3Client.getObject(request).getObjectContent)
    contentRequest.recoverWith {
      case err => {
        Failure(err)
      }
    }
  }

  private def fetchString(request: GetObjectRequest): Try[String] = {
    for {
      s3Stream <- fetchContent(request)
      contentString <- Try(Source.fromInputStream(s3Stream).mkString)
      _ <- Try(s3Stream.close())
    } yield contentString
  }

  def loadConfig(): Try[String] = {
    val env = Env()
    val bucket = s"anghammarad-configuration/${env.stage}"
    val key = s"anghammarad-config.json"

    val request = new GetObjectRequest(bucket, key)
    fetchString(request)
  }


  def loadMappings(config: String): List[Mapping] = {
    ???
  }
}
