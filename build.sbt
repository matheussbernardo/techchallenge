val Http4sVersion = "0.23.18"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.4.5"
val MunitCatsEffectVersion = "1.0.7"
val DoobieVersion = "1.0.0-RC1"

enablePlugins(
  JavaAppPackaging,
  DockerPlugin
)

Compile / mainClass := Some("com.example.techchallenge.Main")
Docker / packageName := "matheussbernardo/techchallenge"
dockerExposedPorts ++= Seq(8080, 8080)
dockerBaseImage := "eclipse-temurin:17-jre"

lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "techchallenge",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.2.1",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
