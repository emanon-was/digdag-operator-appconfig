package io.digdag.plugin.aws.appconfig

import scala.util.{Try, Success, Failure}
import scala.reflect.ClassTag
import io.digdag.client.config.Config
import io.circe.{Json, JsonObject}

package object implicits {

  trait Err {
    def panic = throw throws
    def throws: Throwable = new Exception("${this}")
    def cause: Throwable
  }

  object Err {
    trait Def extends Err {
      val err: Any
      def cause = throws
    }

    trait Throws extends Err {
      val err: Throwable
      def cause = err
    }

    trait Enum extends Err {
      val err: Err
      def cause = err.cause
    }
  }


  implicit class KotlinScopeFunctions[T](val self: T) {
    def let[U](f: T => U): U = f(self)
    def also[U](f: T => U): T = { f(self); self }
  }

  implicit class RitchJson(val self: Json) {
    def flatten(keys: String*): Try[Json] = Try {
      val emptyJson = Json.fromJsonObject(JsonObject.empty)
      val jsons = keys.map(self \\ _).flatten :+ self
      jsons.fold(emptyJson)((a, b) => a.deepMerge(b))
    }
  }

  implicit class RichTry[T](val self: Try[T]) {
    def toEither(): Either[_ <: Throwable, T] = self match {
      case Failure(a) => Left(a)
      case Success(b) => Right(b)
    }
    def toEither[E](f: Throwable => E): Either[E, T] = self match {
      case Failure(a) => Left(f(a))
      case Success(b) => Right(b)
    }
    def unwrap(): T = self match {
      case Failure(a) => throw a
      case Success(b) => b
    }
  }
}
