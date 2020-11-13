package io.digdag.plugin.aws.appconfig.getconfiguration

import scala.util.Try
import cats.implicits._
import io.digdag.plugin.aws.appconfig.implicits._
import io.digdag.client.DigdagClient
import io.digdag.client.config.Config
import io.digdag.client.config.ConfigFactory
import io.digdag.plugin.aws.appconfig.getconfiguration.GetConfiguration.Response._
import io.circe.yaml.{parser => yamlParser}

object StoreParams {

  sealed trait Error {
    val cause: Throwable
  }

  object Error {
    case class TextError(val cause: Throwable) extends Error
    case class JsonError(val cause: Throwable) extends Error
    case class YamlError(val cause: Throwable) extends Error
    case class UnsupportedError(val cause: Throwable) extends Error
  }

  def apply(store: Option[String], response: GetConfiguration.Response): Either[Error, Config] =
    store match {
      case None => Right(Digdag.emptyConfig())
      case Some(key) =>
        response match {
          case Text(content) => text(key, content).toEither(Error.TextError)
          case Json(content) => json(key, content).toEither(Error.JsonError)
          case Yaml(content) => yaml(key, content).toEither(Error.YamlError)
          case Unsupported(content) => unsupported(key, content).toEither(Error.UnsupportedError)
        }
    }

  object Digdag {
    def configFactory(): ConfigFactory = new ConfigFactory(DigdagClient.objectMapper())
    def emptyConfig(): Config = configFactory().create()
  }

  def text(key: String, content: String): Try[Config] = Try {
    Digdag.emptyConfig().set(key, content)
  }

  def json(key: String, content: String): Try[Config] = Try {
    Digdag.emptyConfig().setNested(key, Digdag.configFactory().fromJsonString(content))
  }

  def yaml(key: String, content: String): Try[Config] = Try {
    val json = yamlParser.parse(content)
      .map(_.noSpaces)
      .valueOr(throw _)
    Digdag.emptyConfig().setNested(key, Digdag.configFactory().fromJsonString(json))
  }

  def unsupported(key: String, content: String): Try[Config] =
    text(key, content)

}
