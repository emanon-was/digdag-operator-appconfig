package io.digdag.plugin.aws.appconfig

import io.digdag.spi.{OperatorFactory, Operator, OperatorContext, TaskResult}
import io.digdag.util.BaseOperator
import io.digdag.client.config.Config

class GetConfigurationOperatorFactory(val operatorName: String) extends OperatorFactory {
  override def getType(): String = operatorName
  override def newOperator(ctx: OperatorContext): Operator = new GetConfigurationOperator(ctx)
}

class GetConfigurationOperator(ctx: OperatorContext) extends BaseOperator(ctx) {

  import io.digdag.plugin.aws.appconfig.getconfiguration._

  override def runTask(): TaskResult = {
    val config = request.getConfig
    println(OperatorParams(config))
    for (
      params <- OperatorParams(config)
    ) yield {
      println(params)
      println(GetConfiguration(params.profile, params.resource))
    }
    TaskResult.empty(request)
  }
}
