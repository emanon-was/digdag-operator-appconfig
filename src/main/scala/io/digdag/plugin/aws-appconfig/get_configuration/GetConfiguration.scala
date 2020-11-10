package io.digdag.plugin.aws.appconfig.get_configuration

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
    case class BuildClientError(val cause: Throwable) extends Error
    case class BuildRequestError(val cause: Throwable) extends Error
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

  def awsClient(profile: Params.Profile): Either[Error, AppConfigClient] = {
    val ret = Try {
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
    ret match {
      case Failure(x) => Left(Error.BuildClientError(x))
      case Success(x) => Right(x)
    }
  }

  def awsRequest(resource: Params.Resource): Either[Error, GetConfigurationRequest] = {
    val ret = Try {
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
    }
    ret match {
      case Failure(x) => Left(Error.BuildRequestError(x))
      case Success(x) => Right(x)
    }
  }

  def request(client: AppConfigClient, request: GetConfigurationRequest): Either[Error, GetConfigurationResponse] = {
    val ret = Try {
      client.getConfiguration(request)
    }
    ret match {
      case Failure(x) => Left(Error.RequestError(x))
      case Success(x) => Right(x)
    }
  }

  def response(response: GetConfigurationResponse): Either[Error, Response] = {
    val ret = Try {
      val content = response.content().asUtf8String()
      response.contentType() match {
        case "text/plain" => Response.Text(content)
        case "application/json" => Response.Json(content)
        case "application/x-yaml" => Response.Yaml(content)
        case _ => Response.Other(content)
      }
    }
    ret match {
      case Failure(x) => Left(Error.ResponseError(x))
      case Success(x) => Right(x)
    }
  }
}
