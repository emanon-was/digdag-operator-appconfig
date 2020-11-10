package io.digdag.plugin.aws.appconfig

import java.util.{Arrays => JArrays, List => JList}
import io.digdag.spi.{Plugin, OperatorProvider, OperatorFactory}

class AwsAppConfigPlugin extends Plugin {
  override def getServiceProvider[T](clazz: Class[T]): Class[_ <: T] = {
    if (clazz != classOf[OperatorProvider]) null
    else classOf[AwsAppConfigOperatorProvider].asSubclass(clazz)
  }
}

class AwsAppConfigOperatorProvider extends OperatorProvider {
  override def get(): JList[OperatorFactory] = JArrays.asList(
    new GetConfigurationOperatorFactory("aws.appconfig.get_configuration"),
  )
}
