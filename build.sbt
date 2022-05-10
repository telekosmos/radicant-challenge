ThisBuild / organization := "dev.telekosmos"
ThisBuild / scalaVersion := "2.13.4"

val LogbackVersion = "1.2.10"
val circeVersion = "0.14.1"
val Http4sVersion = "0.23.7"
val pureconfigVersion = "0.17.1"

lazy val root = (project in file(".")).settings(
  name := "radicant-challenge",
  libraryDependencies ++= Seq(
    "com.github.pureconfig" %% "pureconfig"    % pureconfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigVersion,
    "ch.qos.logback"  %  "logback-classic"     % LogbackVersion         % Runtime,

    "org.typelevel" %% "cats-effect" % "3.3.5",
    "org.typelevel" %% "cats-effect-kernel" % "3.3.5",
    "org.typelevel" %% "cats-effect-std" % "3.3.5",

    "org.http4s"    %% "http4s-dsl"          % Http4sVersion,
    "org.http4s"    %% "http4s-blaze-server" % Http4sVersion,
    "org.http4s"    %% "http4s-blaze-client" % Http4sVersion,
    "org.http4s"    %% "http4s-circe"        % Http4sVersion,

    "io.circe"      %% "circe-generic"   % circeVersion,
    // Optional for string interpolation to JSON model
    "io.circe" %% "circe-literal" % circeVersion,

    "org.scalamock" %% "scalamock" % "5.2.0" % Test,
    "org.scalatest" %% "scalatest" % "3.2.9" % Test,

    // better monadic for compiler plugin as suggested by documentation
    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
)
