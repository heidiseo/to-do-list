ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.5"

lazy val props = new {
  val http4sVersion = "0.23.14"
  val circeVersion = "0.15.0-M1"
}

lazy val root = (project in file(".")).settings(
  name := "discord-chore",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.3.12",
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.3.12",
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.3.12",
    // better monadic for compiler plugin as suggested by documentation
    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    "org.typelevel" %% "cats-effect-testing-specs2" % "1.4.0" % Test,
    "org.http4s" %% "http4s-dsl" % props.http4sVersion,
    "org.http4s" %% "http4s-circe" % props.http4sVersion,
    "org.http4s" %% "http4s-dsl" % props.http4sVersion,
    "io.circe" %% "circe-generic" % props.circeVersion
  )
)
