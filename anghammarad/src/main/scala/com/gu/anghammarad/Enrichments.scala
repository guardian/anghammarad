package com.gu.anghammarad

import cats.syntax.either._

import scala.util.{Failure, Success, Try}

object Enrichments {
  implicit class RichList[A](as: List[A]) {
    def traverseT[B](f: A => Try[B]): Try[List[B]] = {
      as.foldRight[Try[List[B]]](Success(Nil)) { (a, acc) =>
        for {
          b <- f(a)
          bs <- acc
        } yield b :: bs
      }
    }

    def traverseE[L, B](f: A => Either[L, B]): Either[L, List[B]] = {
      as.foldRight[Either[L, List[B]]](Right(Nil)) { (a, acc) =>
        for {
          b <- f(a)
          bs <- acc
        } yield b :: bs
      }
    }
  }

  implicit class RichTry[A](underlying: Try[A]) {
    def toEither: Either[Throwable, A] = underlying match {
      case Failure(e) => Left(e)
      case Success(a) => Right(a)
    }
  }
}
