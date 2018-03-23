package com.gu.anghammarad.common

import scala.util.{Failure, Try}


class AnghammaradException private(message: String, causedBy: Option[Throwable])
  extends RuntimeException(message, causedBy.orNull)

object AnghammaradException {
  def Fail[A](message: String): Try[A] = {
    Failure(
      new AnghammaradException(message, None)
    )
  }

  def Fail[A](message: String, causedBy: Throwable): Try[A] = {
    Failure(
      new AnghammaradException(message, Some(causedBy))
    )
  }
}
