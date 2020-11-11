package io.digdag.plugin.aws.appconfig

import scala.util.{Try, Success, Failure}
import scala.reflect.ClassTag
import io.digdag.client.config.Config

object Implicits {
  implicit class RichTry[T](val result: Try[T]) {
    def toEither(): Either[_ <: Throwable, T] = result match {
      case Failure(a) => Left(a)
      case Success(b) => Right(b)
    }
    def unwrap(): T = result match {
      case Failure(a) => throw a
      case Success(b) => b
    }
  }

  implicit class RichConfig(val config: Config) {
    def getRequiredValue[T](key: String)(implicit tag: ClassTag[T]): Try[T] = Try {
      config.get(key, tag.runtimeClass.asInstanceOf[Class[T]])
    }
    def getOptionalValue[T](key: String)(implicit tag: ClassTag[T]): Try[Option[T]] = Try {
      val value = config.getOptional(key, tag.runtimeClass.asInstanceOf[Class[T]])
      Option(value.orNull())
    }
    def getRequiredConfig(key: String): Try[Config] = Try {
      config.getNested(key)
    }
    def getOptionalConfig(key: String): Option[Config] = {
      val value = config.getNestedOrGetEmpty(key)
      if (value.isEmpty) None else Some(value)
    }
  }
}
