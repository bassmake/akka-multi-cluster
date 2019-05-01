import Dependencies._

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.8"

lazy val `akka-multi-cluster` = (project in file("."))
  .settings(
    name := "akka-multi-cluster"
  )
  .aggregate(alpha, beta, gamma)


lazy val alpha = (project in file("alpha"))
  .settings(
    commonSettings,
    name := "alpha"
  )
  .dependsOn(shared)

lazy val beta = (project in file("beta"))
  .settings(
    commonSettings,
    name := "beta"
  )
  .dependsOn(shared)

lazy val gamma = (project in file("gamma"))
  .settings(
    commonSettings,
    name := "gamma"
  )
  .dependsOn(shared)

lazy val shared = (project in file("shared"))
  .settings(
    commonSettings,
    name := "shared",
    libraryDependencies ++= Seq(
      akkaActor
    )
  )

lazy val commonSettings = smlBuildSettings ++ Seq(

)