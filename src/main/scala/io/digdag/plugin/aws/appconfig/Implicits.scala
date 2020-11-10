package io.digdag.plugin.aws.appconfig

import scala.util.{Try, Success, Failure}

object Implicits {
  implicit class RichTry[T](val result: Try[T]) {
    def toEither: Either[_ <: Throwable, T] = result match {
      case Failure(a) => Left(a)
      case Success(b) => Right(b)
    }
  }
}
