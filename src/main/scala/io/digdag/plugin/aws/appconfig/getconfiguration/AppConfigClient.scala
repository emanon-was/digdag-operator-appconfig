package io.digdag.plugin.aws.appconfig.getconfiguration

import scala.util.Try
import cats.implicits._
import io.digdag.plugin.aws.appconfig.implicits._
import java.nio.file.Paths
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProvider,
  DefaultCredentialsProvider,
  StaticCredentialsProvider,
  ProfileCredentialsProvider,
  AwsBasicCredentials,
}
import software.amazon.awssdk.profiles.ProfileFile
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.appconfig.{
  AppConfigClient => SdkAppConfigClient
}

object AppConfigClient {

  sealed trait Error extends Err
  object Error {
    case class CredentialsProviderBuildError(val err: Throwable) extends Error with Err.Throws
    case class RegionBuildError(val err: Throwable) extends Error with Err.Throws
    case class ClientBuildError(val err: Throwable) extends Error with Err.Throws
  }

  def apply(client: OperatorParams.Client): Either[Error, SdkAppConfigClient] =
    for {
      credentialsProvider <- credentialsProviderBuild(client).toEither(Error.CredentialsProviderBuildError)
      region <- regionBuild(client).toEither(Error.RegionBuildError)
      client <- clientBuild(credentialsProvider, region).toEither(Error.ClientBuildError)
    } yield client


  private def clientBuild(credentialsProvider: AwsCredentialsProvider, maybeRegion: Option[Region]): Try[SdkAppConfigClient] =
    Try {
      SdkAppConfigClient.builder()
        .credentialsProvider(credentialsProvider)
        .also(builder => maybeRegion.map(builder.region(_)))
        .build()
    }

  private def regionBuild(client: OperatorParams.Client): Try[Option[Region]] =
    Try(client.region.map(Region.of(_)))

  private def credentialsProviderBuild(client: OperatorParams.Client): Try[AwsCredentialsProvider] = {
    for (staticCredentialsProvider <- staticCredentialsProviderBuild(client).sequence) {
      return staticCredentialsProvider
    }
    for (profileCredentialsProvider <- profileCredentialsProviderBuild(client).sequence) {
      return profileCredentialsProvider
    }
    Try(DefaultCredentialsProvider.create())
  }

  private def staticCredentialsProviderBuild(client: OperatorParams.Client): Try[Option[AwsCredentialsProvider]] =
    Try {
      for (credentials <- client.credentials) yield {
        AwsBasicCredentials.create(credentials.access_key_id, credentials.secret_access_key)
          .let(StaticCredentialsProvider.create(_))
      }
    }

  private def profileCredentialsProviderBuild(client: OperatorParams.Client): Try[Option[AwsCredentialsProvider]] =
    Try {
      for (profile <- client.profile) yield {
        ProfileCredentialsProvider.builder()
          .also { builder => profile.name.map(builder.profileName(_)) }
          .also { builder => profile.file
                   .map(Paths.get(_))
                   .map(ProfileFile.builder().content(_).`type`(ProfileFile.Type.CREDENTIALS).build())
                   .map(builder.profileFile(_)) }
          .build()
      }
    }
}
