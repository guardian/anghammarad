package com.gu.anghammarad


import java.util.concurrent.CompletableFuture

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.auth.credentials.{EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider, InstanceProfileCredentialsProvider}



import scala.concurrent.{Future, Promise}


object AWS {
  /**
    * Use this to make an SNS client, or provide your own.
    */
  def snsClient(credentialsProvider: AwsCredentialsProviderChain): SnsAsyncClient = {
    SnsAsyncClient.builder()
      .region(Region.EU_WEST_1)
      .credentialsProvider(credentialsProvider)
      .build()
  }

  def credentialsProvider(): AwsCredentialsProviderChain = {
    AwsCredentialsProviderChain.of(
      // EC2
      InstanceProfileCredentialsProvider.create(),
      // Lambda
      EnvironmentVariableCredentialsProvider.create(),
      // local
      ProfileCredentialsProvider.create("deployTools"),
    )
  }

  private[anghammarad] def asScala[T](cf: CompletableFuture[T]): Future[T] = {
    val p = Promise[T]()                                                                                                                                           
    cf.whenCompleteAsync{ (result, ex) =>                                                                                                                          
      if (result == null) p failure ex                                                                                                                             
      else                p success result                                                                                                                         
    }                                                                                                                                                              
    p.future 
  }

}