// import com.tuplejump.sbt.yeoman.Yeoman
import com.typesafe.sbt.SbtScalariform._

import scalariform.formatter.preferences._

name := "default-stack-template"

version := "4.0.0"

// scalaVersion := "2.11.8"
scalaVersion := "2.13.1"

herokuAppName in Compile := "your-heroku-app-name"
herokuJdkVersion in Compile := "8"


resolvers += Resolver.jcenterRepo

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += caffeine
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

lazy val silhouetteVersion = "6.1.0"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-testkit" % silhouetteVersion % "test",
  "com.iheart" %% "ficus" % "1.4.7",
  "net.codingwell" %% "scala-guice" % "4.2.5"
  // "com.iheart" %% "ficus" % "1.2.6",
  // "org.webjars" %% "webjars-play" % "2.5.0-2",
  // "org.webjars" % "bootstrap" % "3.1.1",
  // "org.webjars" % "jquery" % "1.11.0",
  // "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3",
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint" // Enable recommended additional warnings.
  // "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  // "-Ywarn-dead-code", // Warn when dead code is identified.
  // "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  // "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  // "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

//********************************************************
// Scalariform settings
//********************************************************

//defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(DanglingCloseParenthesis, Preserve)
