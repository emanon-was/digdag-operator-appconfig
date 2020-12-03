import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"

  // cats
  val catsDeps = "org.typelevel" %% "cats-core" % "2.2.0"

  // Digdag
  val digdagVersion = "0.9.42"
  val digdagDeps = Seq(
    "io.digdag" % "digdag-spi",
    "io.digdag" % "digdag-plugin-utils"
  ).map(_ % digdagVersion)

  // Logger
  val slf4jVersion = "1.7.30"
  val slf4jDeps = Seq(
    "org.slf4j" % "slf4j-api",
    "org.slf4j" % "slf4j-simple"
  ).map(_ % slf4jVersion)

  // AwsSDK
  val awsjavasdkVersion = "2.15.9"
  val awsjavasdkDeps = Seq(
    "software.amazon.awssdk" % "bom" % awsjavasdkVersion pomOnly(),
    "software.amazon.awssdk" % "appconfig" % awsjavasdkVersion,
  )

  // Json/Yaml
  val circeVersion = "0.13.0"
  val circleDeps = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-yaml",
  ).map(_ % circeVersion)
}
