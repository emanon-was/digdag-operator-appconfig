package io.digdag.plugin.aws.appconfig.getconfiguration

import scala.util.{Try, Success, Failure}
import io.digdag.plugin.aws.appconfig.implicits._
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProvider,
  DefaultCredentialsProvider,
  StaticCredentialsProvider,
  AwsBasicCredentials,
}
import software.amazon.awssdk.services.appconfig.AppConfigClient
import software.amazon.awssdk.services.appconfig.model.{
  GetConfigurationRequest,
  GetConfigurationResponse,
}

object GetConfiguration {

  sealed trait Error {
    val cause: Throwable
  }

  object Error {
    case class ClientBuildError(val cause: Throwable) extends Error
    case class RequestBuildError(val cause: Throwable) extends Error
    case class RequestError(val cause: Throwable) extends Error
    case class ResponseError(val cause: Throwable) extends Error
  }

  sealed trait Response {
    val content: String
  }

  object Response {
    case class Text(val content: String) extends Response
    case class Json(val content: String) extends Response
    case class Yaml(val content: String) extends Response
    case class Unsupported(val content: String) extends Response
  }

  def apply(profile: OperatorParams.Profile, resource: OperatorParams.Resource): Either[Error, Response] =
    for {
      awsClient <- awsClient(profile).toEither(Error.ClientBuildError)
      awsRequest <- awsRequest(resource).toEither(Error.RequestBuildError)
      awsResponse <- request(awsClient, awsRequest).toEither(Error.RequestError)
      response <- response(awsResponse).toEither(Error.ResponseError)
    } yield {
      response
    }

  private def awsClient(profile: OperatorParams.Profile): Try[AppConfigClient] = Try {
    val region = Region.of(profile.region)
    val credentialsProvider = profile.credentials match {
      case None => DefaultCredentialsProvider.create()
      case Some(credentials) => StaticCredentialsProvider.create(
        AwsBasicCredentials.create(
          credentials.accessKeyId,
          credentials.secretAccessKey
        ))
    }
    AppConfigClient.builder()
      .region(region)
      .credentialsProvider(credentialsProvider)
      .build()
  }

  private def awsRequest(resource: OperatorParams.Resource): Try[GetConfigurationRequest] = Try {
    GetConfigurationRequest.builder()
      .application(resource.application)
      .environment(resource.environment)
      .configuration(resource.confinguration)
      .clientId(resource.clientId)
      .let { builder =>
        resource.clientConfigurationVersion match {
          case None => builder
          case Some(x) => builder.clientConfigurationVersion(x)
        }
      }
      .build()
  }

  private def request(client: AppConfigClient, request: GetConfigurationRequest): Try[GetConfigurationResponse] = Try {
      client.getConfiguration(request)
  }

  private def response(response: GetConfigurationResponse): Try[Response] = Try {
    val content = response.content().asUtf8String()
    response.contentType() match {
      case "text/plain" => Response.Text(content)
      case "application/json" => Response.Json(content)
      case "application/x-yaml" => Response.Yaml(content)
      case _ => Response.Unsupported(content)
    }
  }
}
