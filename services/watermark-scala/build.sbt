
lazy val akkaHttpVersion = "10.1.10"
lazy val akkaVersion = "2.5.26"

lazy val IntegrationTest = config("it") extend Test

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    inThisBuild(List(
      organization := "com.yushkevich",
      scalaVersion := "2.12.8"
    )),
    name := "watermark-scala",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    ) ++ testDependencies
  )

lazy val testDependencies = Seq(
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5",
  "org.scalamock" %% "scalamock" % "4.4.0"
).map(_ % "it,test")

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

coverageMinimum := 81
coverageFailOnMinimum := true
coverageEnabled.in(Test, test) := true