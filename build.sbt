ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.5"

lazy val props = new {
  val http4sVersion = "1.0.0-M31"
  val circeVersion = "0.15.0-M1"
  val catsEffectVersion = "3.3.12"
  val monadicVersion = "0.3.1"
  val catsEffectTestingSpecs2Version = "1.4.0"
  val scalaTestVersion = "3.2.13"
  val log4CatsVersion = "2.4.0"
  val logbackVersion = "1.4.0"
  val sttpVersion = "3.7.6"
  val commonsCodecVersion = "1.15"
  val pureConfigVersion = "0.17.1"
}

lazy val root = (project in file(".")).settings(
  name := "discord-chore",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % props.catsEffectVersion,
    "org.typelevel" %% "cats-effect-std" % props.catsEffectVersion,
    compilerPlugin("com.olegpy" %% "better-monadic-for" % props.monadicVersion),
    "org.typelevel" %% "cats-effect-testing-specs2" % props.catsEffectTestingSpecs2Version % Test,
    "org.http4s" %% "http4s-circe" % props.http4sVersion,
    "org.http4s" %% "http4s-dsl" % props.http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % props.http4sVersion,
    "io.circe" %% "circe-generic" % props.circeVersion,
    "org.scalactic" %% "scalactic" % props.scalaTestVersion,
    "org.scalatest" %% "scalatest" % props.scalaTestVersion % "test",
    "org.typelevel" %% "log4cats-core" % props.log4CatsVersion,
    "org.typelevel" %% "log4cats-slf4j" % props.log4CatsVersion,
    "ch.qos.logback" % "logback-classic" % props.logbackVersion,
    "com.softwaremill.sttp.client3" %% "core" % props.sttpVersion,
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % props.sttpVersion,
    "com.softwaremill.sttp.client3" %% "circe" % props.sttpVersion,
    "commons-codec" % "commons-codec" % props.commonsCodecVersion,
    "com.github.pureconfig" %% "pureconfig" % props.pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % props.pureConfigVersion
  )
)
