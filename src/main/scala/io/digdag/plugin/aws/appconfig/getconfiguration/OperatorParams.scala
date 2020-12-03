package io.digdag.plugin.aws.appconfig.getconfiguration

import scala.util.Try
import io.digdag.plugin.aws.appconfig.implicits._
import io.digdag.client.config.Config
import io.circe.parser.parse
import io.circe.generic.auto._

case class OperatorParams(
  val client: OperatorParams.Client,
  val params: OperatorParams.Params,
  val output: Option[String]
)

object OperatorParams {

  case class Client(
    val credentials: Option[Credentials],
    val profile: Option[Profile],
    val region: Option[String]
  )

  case class Credentials(
    val access_key_id: String,
    val secret_access_key: String
  )

  case class Profile(
    val name: Option[String],
    val file: Option[String]
  )

  case class RequiredParams(
    val params: Params,
    val output: Option[String]
  )

  case class Params(
    val application: String,
    val environment: String,
    val configuration: String,
    val client_id: String,
    val client_configuration_version: Option[Integer]
  )

  sealed trait Error extends Err
  object Error {
    case class ConfigJsonParseError(val err: Throwable) extends Error with Err.Throws
    case class ClientJsonParseError(val err: Throwable) extends Error with Err.Throws
    case class ParamsJsonParseError(val err: Throwable) extends Error with Err.Throws
    case class UnexpectedError(val err: Throwable) extends Error with Err.Throws
  }

  def apply(config: Config): Either[Error, OperatorParams] =
    for {
      json <- parse(config.toString()).left.map(Error.ConfigJsonParseError)
      clientJson <- json.flatten("aws.configure", "aws.appconfig", "aws.appconfig.get_configuration").toEither(Error.UnexpectedError)
      paramsJson <- json.flatten("aws.appconfig.get_configuration").toEither(Error.UnexpectedError)
      client <- clientJson.as[Client].left.map(Error.ClientJsonParseError)
      requiredParams <- paramsJson.as[RequiredParams].left.map(Error.ParamsJsonParseError)
    } yield OperatorParams(client, requiredParams.params, requiredParams.output)
}
