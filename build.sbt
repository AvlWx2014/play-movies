import sbt.Keys._
import play.sbt.PlaySettings

lazy val root = (project in file("."))
    .enablePlugins(PlayService, PlayLayoutPlugin)
    .settings(
      name := "movies",
      organization := "com.smithr",
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.13.3",
      libraryDependencies ++= Seq(
        guice,
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
        "org.mongodb.scala" % "mongo-scala-driver_2.13" % "4.0.5",
//        "org.mongodb" % "mongodb-driver-sync" % "4.0.5",
        "net.codingwell" %% "scala-guice" % "4.2.6"
      ),
      scalacOptions ++= Seq(
        "-feature",
        "-deprecation",
        "-Xfatal-warnings"
      )
    )


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.smithr.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.smithr.binders._"
