package io.digdag.plugin.aws.appconfig

import io.digdag.spi.{OperatorFactory, Operator, OperatorContext, TaskResult}
import io.digdag.util.BaseOperator
import io.digdag.client.config.Config
import io.digdag.plugin.aws.appconfig.implicits._

class GetConfigurationOperatorFactory(val operatorName: String) extends OperatorFactory {
  override def getType(): String = operatorName
  override def newOperator(ctx: OperatorContext): Operator = new GetConfigurationOperator(ctx)
}

class GetConfigurationOperator(ctx: OperatorContext) extends BaseOperator(ctx) {
  import io.digdag.plugin.aws.appconfig.getconfiguration._

  sealed trait Error {
    def throws(): Unit = this match {
      case Error.OperatorParamsError(e) => throw e.cause
      case Error.GetConfigurationError(e) => throw e.cause
      case Error.StoreParamsError(e) => throw e.cause
    }
  }

  object Error {
    case class OperatorParamsError(val err: OperatorParams.Error) extends Error
    case class GetConfigurationError(val err: GetConfiguration.Error) extends Error
    case class StoreParamsError(val err: StoreParams.Error) extends Error
  }

  override def runTask(): TaskResult = {
    val config = request.getConfig
    val result: Either[Error, Config] = for {
      operatorParams <- OperatorParams(config).left.map(Error.OperatorParamsError)
      response <- GetConfiguration(operatorParams.profile, operatorParams.resource).left.map(Error.GetConfigurationError)
      storeParams <- StoreParams(operatorParams.store, response).left.map(Error.StoreParamsError)
    } yield storeParams

    result match {
      case Left(err) => {
        println(err) // TODO: Logger
        err.throws
        TaskResult.empty(request)
      }
      case Right(storeParams) => {
        TaskResult.defaultBuilder(request)
          .also(_.storeParams(storeParams))
          .build()
      }
    }
  }
}
