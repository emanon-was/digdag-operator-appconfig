package io.digdag.plugin.aws.appconfig.getconfiguration

import scala.util.Try
import cats.implicits._
import io.digdag.plugin.aws.appconfig.implicits._
import io.digdag.client.config.Config

object OperatorParams {

  sealed trait Error {
    val cause: Throwable
  }

  object Error {
    case class ConvertError(val cause: Throwable) extends Error
  }

  case class Values (
    val profile: Profile,
    val resource: Resource,
    val store: Option[String]
  )

  case class Profile(
    val region: String,
    val credentials: Option[Credentials]
  )

  case class Credentials(
    val accessKeyId: String,
    val secretAccessKey: String
  )

  case class Resource(
    val application: String,
    val environment: String,
    val confinguration: String,
    val clientId: String,
    val clientConfigurationVersion: Option[String]
  )

  def apply(config: Config): Either[Error, Values] = {
    val result = for {
      profile <- profile(config)
      resource <- resource(config)
      store <- store(config)
    } yield {
      Values(profile, resource, store)
    }
    result.toEither(Error.ConvertError)
  }

  private def profile(config: Config): Try[Profile] =
    for {
      profileConfig <- config.getRequiredNode("profile")
      region <- profileConfig.getRequiredValue[String]("region")
      credentials <- credentials(profileConfig).sequence
    } yield {
      Profile(region, credentials)
    }

  private def credentials(profileConfig: Config): Option[Try[Credentials]] =
    for {
      credentialsConfig <- profileConfig.getOptionalNode("credentials")
    } yield {
      for {
        accessKeyId <- credentialsConfig.getRequiredValue[String]("access_key_id")
        secretAccessKey <- credentialsConfig.getRequiredValue[String]("secret_access_key")
      } yield {
        Credentials(accessKeyId, secretAccessKey)
      }
    }

  private def resource(config: Config): Try[Resource] =
    for {
      resourceConfig <- config.getRequiredNode("resource")
      application <- resourceConfig.getRequiredValue[String]("application")
      environment <- resourceConfig.getRequiredValue[String]("environment")
      configuration <- resourceConfig.getRequiredValue[String]("configuration")
      clientId <- resourceConfig.getRequiredValue[String]("client_id")
      clientConfigurationVersion <- resourceConfig.getOptionalValue[String]("client_configuration_version")
    } yield {
      Resource(application, environment, configuration, clientId, clientConfigurationVersion)
    }

  private def store(config: Config): Try[Option[String]] =
    config.getOptionalValue[String]("store")

}
