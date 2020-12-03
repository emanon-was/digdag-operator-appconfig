package io.digdag.plugin.aws.appconfig

import io.digdag.spi.{OperatorFactory, Operator, OperatorContext, TaskResult}
import io.digdag.util.BaseOperator
import io.digdag.client.config.Config
import io.digdag.plugin.aws.appconfig.implicits._
import org.slf4j.{Logger, LoggerFactory}

// import io.circe.{Decoder, Encoder}
// import io.circe.generic.semiauto._
// import io.circe.parser.decode

class GetConfigurationOperatorFactory(val operatorName: String) extends OperatorFactory {
  override def getType(): String = operatorName
  override def newOperator(ctx: OperatorContext): Operator = new GetConfigurationOperator(operatorName, ctx)
}

class GetConfigurationOperator(operatorName: String, ctx: OperatorContext) extends BaseOperator(ctx) {

  import io.digdag.plugin.aws.appconfig.getconfiguration._

  override def runTask(): TaskResult = {
    val config = request.getConfig
    val configFactory = config.getFactory()

    val result = for {
      operatorParams <- OperatorParams(config)
      appConfigClient <- AppConfigClient(operatorParams.client)
      appConfigResponse <- GetConfiguration(appConfigClient, operatorParams.params)
      outputParams <- OutputParams(operatorParams.output, appConfigResponse)(configFactory)
    } yield outputParams

    val logger = LoggerFactory.getLogger(operatorName)
    result match {
      case Left(err) => {
        logger.error("{}", err)
        err.panic
      }
      case Right(outputParams) => {
        logger.info("OutputParams: {}", outputParams)
        TaskResult.defaultBuilder(request)
          .also(_.storeParams(outputParams))
          .build()
      }
    }
  }
}
