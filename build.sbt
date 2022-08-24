ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.5"

lazy val props = new {
  val http4sVersion = "1.0.0-M31"
  val circeVersion = "0.15.0-M1"
  val catsEffectVersion = "3.3.12"
  val monadicVersion = "0.3.1"
  val catsEffectTestingSpecs2Version = "1.4.0"
}

lazy val root = (project in file(".")).settings(
  name := "discord-chore",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % props.catsEffectVersion,
    "org.typelevel" %% "cats-effect-kernel" % props.catsEffectVersion,
    "org.typelevel" %% "cats-effect-std" % props.catsEffectVersion,
    compilerPlugin("com.olegpy" %% "better-monadic-for" % props.monadicVersion),
    "org.typelevel" %% "cats-effect-testing-specs2" % props.catsEffectTestingSpecs2Version % Test,
    "org.http4s" %% "http4s-circe" % props.http4sVersion,
    "org.http4s" %% "http4s-dsl" % props.http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % props.http4sVersion,
    "io.circe" %% "circe-generic" % props.circeVersion
  )
)
