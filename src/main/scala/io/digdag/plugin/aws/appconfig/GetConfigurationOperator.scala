package io.digdag.plugin.aws.appconfig

import io.digdag.spi.{OperatorFactory, Operator, OperatorContext, TaskResult}
import io.digdag.util.BaseOperator
import io.digdag.client.config.Config
import io.digdag.plugin.aws.appconfig.implicits._
import org.slf4j.{Logger, LoggerFactory}

class GetConfigurationOperatorFactory(val operatorName: String) extends OperatorFactory {
  override def getType(): String = operatorName
  override def newOperator(ctx: OperatorContext): Operator = new GetConfigurationOperator(operatorName, ctx)
}

class GetConfigurationOperator(operatorName: String, ctx: OperatorContext) extends BaseOperator(ctx) {

  import io.digdag.plugin.aws.appconfig.getconfiguration._

  sealed trait Error {}
  object Error {
    case class OperatorParamsError(val err: OperatorParams.Error) extends Error
    case class GetConfigurationError(val err: GetConfiguration.Error) extends Error
    case class StoreParamsError(val err: StoreParams.Error) extends Error
  }

  implicit class ErrorCause(val err: Error) {
    def cause(): Throwable = err match {
      case Error.OperatorParamsError(e) => e.cause
      case Error.GetConfigurationError(e) => e.cause
      case Error.StoreParamsError(e) => e.cause
    }
  }

  override def runTask(): TaskResult = {
    val config = request.getConfig
    val result: Either[Error, Config] = for {
      operatorParams <- OperatorParams(config).left.map(Error.OperatorParamsError)
      response <- GetConfiguration(operatorParams.profile, operatorParams.resource).left.map(Error.GetConfigurationError)
      storeParams <- StoreParams(operatorParams.store, response).left.map(Error.StoreParamsError)
    } yield storeParams

    val logger = LoggerFactory.getLogger(operatorName)
    result match {
      case Left(err) => {
        logger.error("{}", err)
        throw err.cause()
      }
      case Right(storeParams) => {
        logger.info("StoreParams: {}", storeParams)
        TaskResult.defaultBuilder(request)
          .also(_.storeParams(storeParams))
          .build()
      }
    }
  }
}
