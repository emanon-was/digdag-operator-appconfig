package io.digdag.plugin.aws.appconfig.getconfiguration

import scala.util.Try
import cats.implicits._
import io.digdag.plugin.aws.appconfig.implicits._
import io.digdag.client.DigdagClient
import io.digdag.client.config.Config
import io.digdag.client.config.ConfigFactory
import io.digdag.plugin.aws.appconfig.getconfiguration.GetConfiguration.Response._
import io.circe.yaml.{parser => yamlParser}

object OutputParams {

  sealed trait Error extends Err
  object Error {
    case class TextError(val err: Throwable) extends Error with Err.Throws
    case class JsonError(val err: Throwable) extends Error with Err.Throws
    case class YamlError(val err: Throwable) extends Error with Err.Throws
    case class UnsupportedError(val err: Throwable) extends Error with Err.Throws
  }

  def apply(output: Option[String], response: GetConfiguration.Response)(implicit cf: ConfigFactory): Either[Error, Config] =
    output match {
      case None => Right(cf.create())
      case Some(key) =>
        response match {
          case Text(content) => text(key, content).toEither(Error.TextError)
          case Json(content) => json(key, content).toEither(Error.JsonError)
          case Yaml(content) => yaml(key, content).toEither(Error.YamlError)
          case Unsupported(content) => unsupported(key, content).toEither(Error.UnsupportedError)
        }
    }

  def text(key: String, content: String)(implicit cf: ConfigFactory): Try[Config] = Try {
    cf.create().set(key, content)
  }

  def json(key: String, content: String)(implicit cf: ConfigFactory): Try[Config] = Try {
    cf.create().setNested(key, cf.fromJsonString(content))
  }

  def yaml(key: String, content: String)(implicit cf: ConfigFactory): Try[Config] = Try {
    val json = yamlParser.parse(content).map(_.noSpaces).valueOr(throw _)
    cf.create().setNested(key, cf.fromJsonString(json))
  }

  def unsupported(key: String, content: String)(implicit cf: ConfigFactory): Try[Config] =
    text(key, content)

}
