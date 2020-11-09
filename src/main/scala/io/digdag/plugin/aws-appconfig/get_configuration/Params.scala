package io.digdag.plugin.aws.appconfig.get_configuration

import io.digdag.client.config.Config
import collection.JavaConverters._
import scala.util.Try

case class Params (
  val credentials: Option[Params.Credentials],
  val resource: Params.Resource,
  val store: Params.Store,
)

object Params {
  case class Credentials(
    val accessKeyId: String,
    val secretAccessKey: String
  )
  case class Resource(
    val application: String,
    val environment: String,
    val coufinguration: String,
    val clientId: String,
    val clientConfigurationVersion: Option[String]
  )
  case class Store(
    val pairs: Seq[(String, String)]
  )
}

object ReadParams {

  def apply(config: Config): Try[Params] = for (
    a <- credentials(config);
    b <- resource(config);
    c <- store(config)
  ) yield Params(a, b, c)

  private def credentials(config: Config): Try[Option[Params.Credentials]] = {
    Try {
      val node = config.getNestedOrGetEmpty("credentials")
      if (node.isEmpty) None
      else {
        Some(
          Params.Credentials(
            node.get("access_key_id", classOf[String]),
            node.get("secret_access_key", classOf[String])
          ))
      }
    }
  }

  private def resource(config: Config): Try[Params.Resource] = {
    Try {
      val node = config.getNested("resource")
      Params.Resource(
        node.get("application", classOf[String]),
        node.get("environment", classOf[String]),
        node.get("configuration", classOf[String]),
        node.get("client_id", classOf[String]),
        {
          val opt = node.getOptional("client_configuration_version", classOf[String])
          if (opt.isPresent()) Some(opt.get()) else None
        }
      )
    }
  }

  private def store(config: Config): Try[Params.Store] = {
    Try {
      val node = config.getNestedOrGetEmpty("store")
      Params.Store(node.getKeys().asScala.map(key=>(key, node.get(key, classOf[String]))))
    }
  }
}
