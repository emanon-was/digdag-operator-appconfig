package io.digdag.plugin.aws.appconfig

import io.digdag.spi.{OperatorFactory, Operator, OperatorContext, TaskResult}
import io.digdag.util.BaseOperator
import io.digdag.client.config.Config
import io.digdag.plugin.aws.appconfig.get_configuration._

class GetConfigurationOperatorFactory(val operatorName: String) extends OperatorFactory {
  override def getType(): String = operatorName
  override def newOperator(ctx: OperatorContext): Operator = new GetConfigurationOperator(ctx)
}

class GetConfigurationOperator(ctx: OperatorContext) extends BaseOperator(ctx) {
  override def runTask(): TaskResult = {
    val config = request.getConfig
    println(ReadParams(config))
    TaskResult.empty(request)
  }
}
