import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"

  lazy val digdagVersion = "0.9.42"
  lazy val slf4jVersion = "1.7.30"

  val digdagDeps = Seq(
    "io.digdag" % "digdag-spi" % digdagVersion,
    "io.digdag" % "digdag-plugin-utils" % digdagVersion
  ).map(dep => Seq(dep % Provided, dep % Test)).flatten

  val slf4jDeps = Seq(
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.slf4j" % "slf4j-simple" % slf4jVersion
  ).map(dep => Seq(dep % Provided, dep % Test)).flatten

  // AwsSDK
  val awsjavasdkVersion = "2.15.9"
  val awsjavasdkDeps = Seq(
    "software.amazon.awssdk" % "bom" % awsjavasdkVersion pomOnly(),
    "software.amazon.awssdk" % "appconfig" % awsjavasdkVersion,
  )
}
