package com.gu.anghammarad.testutils

import org.scalatest.exceptions.TestFailedException

import scala.util.{Failure, Success, Try}

trait TryValues {
  implicit class TestTry[T](tried: Try[T]) {
    def success: T = tried match {
      case Success(t) => t
      case Failure(err) =>
        throw new TestFailedException("Could not get successful value from failed Try", err, 10)
    }

    def failure: Throwable = tried match {
      case Success(t) =>
        throw new TestFailedException(s"Could not get failure value from successful Try $t", 10)
      case Failure(err) =>
        err
    }
  }
}
