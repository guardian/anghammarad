package com.gu.anghammarad

import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider}
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}

import scala.concurrent.{Future, Promise}


object AWS {
  /**
    * Use this to make an SNS client, or provide your own.
    */
  def snsClient(credentialsProvider: AWSCredentialsProviderChain): AmazonSNSAsync = {
    AmazonSNSAsyncClientBuilder.standard()
      .withRegion(Regions.EU_WEST_1)
      .withCredentials(credentialsProvider)
      .build()
  }

  def credentialsProvider(): AWSCredentialsProviderChain = {
    new AWSCredentialsProviderChain(
      // EC2
      InstanceProfileCredentialsProvider.getInstance(),
      // Lambda
      new EnvironmentVariableCredentialsProvider(),
      // local
      new ProfileCredentialsProvider("deployTools")
    )
  }

  private class AwsAsyncPromiseHandler[R <: AmazonWebServiceRequest, T](promise: Promise[T]) extends AsyncHandler[R, T] {
    def onError(e: Exception): Unit = {
      promise failure e
    }
    def onSuccess(r: R, t: T): Unit = {
      promise success t
    }
  }

  private[anghammarad] def awsToScala[R <: AmazonWebServiceRequest, T](sdkMethod: ( (R, AsyncHandler[R, T]) => java.util.concurrent.Future[T])): (R => Future[T]) = { req =>
    val p = Promise[T]()
    sdkMethod(req, new AwsAsyncPromiseHandler(p))
    p.future
  }
}
