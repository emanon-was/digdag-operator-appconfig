package io.digdag.plugin.aws.appconfig.getconfiguration

import io.digdag.client.config.Config
import collection.JavaConverters._
import scala.util.Try
import io.digdag.plugin.aws.appconfig.Implicits._

object OperatorParams {

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

  def apply(config: Config): Try[Values] = {
    for {
      profile <- profile(config)
      resource <- resource(config)
      store <- store(config)
    } yield {
      Values(profile, resource, store)
    }
  }

  private def profile(config: Config): Try[Profile] = {
    for {
      profileConfig <- config.getRequiredConfig("profile")
      region <- profileConfig.getRequiredValue[String]("region")
      credentials <- credentials(profileConfig)
    } yield {
      Profile(region, credentials)
    }
  }

  private def credentials(profileConfig: Config): Try[Option[Credentials]] = {
    val ret = for {
      credentialsConfig <- profileConfig.getOptionalConfig("credentials")
    } yield {
      for {
        accessKeyId <- credentialsConfig.getRequiredValue[String]("access_key_id")
        secretAccessKey <- credentialsConfig.getRequiredValue[String]("secret_access_key")
      } yield {
        Credentials(accessKeyId, secretAccessKey)
      }
    }
    Try(ret.map(_.unwrap()))
  }

  private def resource(config: Config): Try[Resource] = {
    for {
      resourceConfig <- config.getRequiredConfig("resource")
      application <- resourceConfig.getRequiredValue[String]("application")
      environment <- resourceConfig.getRequiredValue[String]("environment")
      configuration <- resourceConfig.getRequiredValue[String]("configuration")
      clientId <- resourceConfig.getRequiredValue[String]("client_id")
      clientConfigurationVersion <- resourceConfig.getOptionalValue[String]("client_configuration_version")
    } yield {
      Resource(application, environment, configuration, clientId, clientConfigurationVersion)
    }
  }

  private def store(config: Config): Try[Option[String]] = {
    config.getOptionalValue[String]("store")
  }
}
