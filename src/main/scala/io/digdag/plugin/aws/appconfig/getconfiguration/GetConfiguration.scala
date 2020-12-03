package io.digdag.plugin.aws.appconfig.getconfiguration

import scala.util.Try
import io.digdag.plugin.aws.appconfig.implicits._
import software.amazon.awssdk.services.appconfig.{
  AppConfigClient => SdkAppConfigClient
}
import software.amazon.awssdk.services.appconfig.model.{
  GetConfigurationRequest,
  GetConfigurationResponse,
}

object GetConfiguration {

  sealed trait Error extends Err
  object Error {
    case class RequestBuildError(val err: Throwable) extends Error with Err.Throws
    case class RequestError(val err: Throwable) extends Error with Err.Throws
    case class ResponseError(val err: Throwable) extends Error with Err.Throws
  }

  sealed trait Response { val content: String }
  object Response {
    case class Text(val content: String) extends Response
    case class Json(val content: String) extends Response
    case class Yaml(val content: String) extends Response
    case class Unsupported(val content: String) extends Response
  }

  def apply(awsClient: SdkAppConfigClient, params: OperatorParams.Params): Either[Error, Response] =
    for {
      awsRequest <- requestBuild(params).toEither(Error.RequestBuildError)
      awsResponse <- request(awsClient, awsRequest).toEither(Error.RequestError)
      response <- response(awsResponse).toEither(Error.ResponseError)
    } yield response

  private def requestBuild(params: OperatorParams.Params): Try[GetConfigurationRequest] =
    Try {
      GetConfigurationRequest.builder()
        .application(params.application)
        .environment(params.environment)
        .configuration(params.configuration)
        .clientId(params.client_id)
        .also { builder =>
          params.client_configuration_version
            .map(_.toString())
            .map(builder.clientConfigurationVersion(_))
        }
        .build()
    }

  private def request(client: SdkAppConfigClient, request: GetConfigurationRequest): Try[GetConfigurationResponse] =
    Try {
      client.getConfiguration(request)
    }

  private def response(response: GetConfigurationResponse): Try[Response] =
    Try {
      val content = response.content().asUtf8String()
      response.contentType() match {
        case "text/plain" => Response.Text(content)
        case "application/json" => Response.Json(content)
        case "application/x-yaml" => Response.Yaml(content)
        case _ => Response.Unsupported(content)
      }
    }
}
