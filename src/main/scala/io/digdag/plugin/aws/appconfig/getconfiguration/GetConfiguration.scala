package io.digdag.plugin.aws.appconfig.getconfiguration

import io.digdag.plugin.aws.appconfig.Implicits._
import scala.util.{Try, Success, Failure}
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
    case class Other(val content: String) extends Response
  }


  def apply(profile: Params.Profile, resource: Params.Resource): Either[Error, Response] = for (
    awsClient <- awsClient(profile);
    awsRequest <- awsRequest(resource);
    awsResponse <- request(awsClient, awsRequest);
    response <- response(awsResponse)
  ) yield response


  def awsClient(profile: Params.Profile): Either[Error, AppConfigClient] = Try {
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
  }.toEither.left.map(Error.ClientBuildError(_))


  def awsRequest(resource: Params.Resource): Either[Error, GetConfigurationRequest] = Try {
    var builder = GetConfigurationRequest.builder()
      .application(resource.application)
      .environment(resource.environment)
      .configuration(resource.confinguration)
      .clientId(resource.clientId)
    builder = resource.clientConfigurationVersion match {
      case None => builder
      case Some(x) => builder.clientConfigurationVersion(x)
    }
    builder.build()
  }.toEither.left.map(Error.RequestBuildError(_))


  def request(client: AppConfigClient, request: GetConfigurationRequest): Either[Error, GetConfigurationResponse] = Try {
      client.getConfiguration(request)
  }.toEither.left.map(Error.RequestError(_))


  def response(response: GetConfigurationResponse): Either[Error, Response] = Try {
    val content = response.content().asUtf8String()
    response.contentType() match {
      case "text/plain" => Response.Text(content)
      case "application/json" => Response.Json(content)
      case "application/x-yaml" => Response.Yaml(content)
      case _ => Response.Other(content)
    }
  }.toEither.left.map(Error.ResponseError(_))

}
