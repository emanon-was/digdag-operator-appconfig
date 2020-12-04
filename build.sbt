import Dependencies._

ThisBuild / scalaVersion     := "2.12.12"
ThisBuild / version          := "1.1.0"
ThisBuild / organization     := "io.digdag.plugin"
ThisBuild / organizationName := "aws-appconfig"

ThisBuild / javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
ThisBuild / publishTo := Some(Resolver.file("digdag", new File("./.digdag/plugins/")))
ThisBuild / publishMavenStyle := true
ThisBuild / isSnapshot := true
ThisBuild / crossPaths := false
ThisBuild / exportJars := true

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    name := "digdag-operator-aws-appconfig",
    resolvers += "bintray-digdag" at "https://dl.bintray.com/digdag/maven",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += catsDeps,
    libraryDependencies ++= digdagDeps.map(dep => Seq(dep % Provided, dep % Test)).flatten,
    libraryDependencies ++= slf4jDeps.map(dep => Seq(dep % Provided, dep % Test)).flatten,
    libraryDependencies ++= awsjavasdkDeps,
    libraryDependencies ++= circleDeps,
  )


// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.

// ThisBuild / description := "Some descripiton about your project."
// ThisBuild / licenses    := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
// ThisBuild / homepage    := Some(url("https://github.com/example/project"))
// ThisBuild / scmInfo := Some(
//   ScmInfo(
//     url("https://github.com/your-account/your-project"),
//     "scm:git@github.com:your-account/your-project.git"
//   )
// )
// ThisBuild / developers := List(
//   Developer(
//     id    = "Your identifier",
//     name  = "Your Name",
//     email = "your@email",
//     url   = url("http://your.url")
//   )
// )
// ThisBuild / pomIncludeRepository := { _ => false }
// ThisBuild / publishTo := {
//   val nexus = "https://oss.sonatype.org/"
//   if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
//   else Some("releases" at nexus + "service/local/staging/deploy/maven2")
// }



