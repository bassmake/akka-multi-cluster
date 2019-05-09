import Dependencies._

ThisBuild / organization := "sk.bsmk"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.8"

lazy val CompileTest = "compile->compile;test->test"

lazy val `akka-multi-cluster` = (project in file("."))
  .settings(
    name := "akka-multi-cluster"
  )
  .aggregate(alpha, beta, gamma, shared, `iot-functional`, `iot-object-oriented`)


lazy val alpha = (project in file("alpha"))
  .settings(
    commonSettings,
    name := "alpha"
  )
  .dependsOn(shared % CompileTest)

lazy val beta = (project in file("beta"))
  .settings(
    commonSettings,
    name := "beta"
  )
  .dependsOn(shared % CompileTest)

lazy val gamma = (project in file("gamma"))
  .settings(
    commonSettings,
    name := "gamma"
  )
  .dependsOn(shared % CompileTest)

lazy val shared = (project in file("shared"))
  .settings(
    commonSettings,
    name := "shared",
    libraryDependencies ++= Seq(
      akkaActor,
//      akkaCluster,
//      akkaSlf4j,
//      scalaLogging,
//      logback,

      scalaTest % Test,
      akkaActorTestkit % Test
    )
  )

lazy val `iot-object-oriented` = (project in file("iot-object-oriented"))
  .settings(
    commonSettings,
    name := "iot-object-oriented",
    libraryDependencies ++= Seq(
      akkaActor,

      scalaTest % Test,
      akkaActorTestkit % Test
    )
  )

lazy val `iot-functional` = (project in file("iot-functional"))
  .settings(
    commonSettings,
    name := "iot-functional",
    libraryDependencies ++= Seq(
      akkaActor,

      scalaTest % Test,
      akkaActorTestkit % Test
    )
  )


lazy val smlBuildSettings =
  commonSmlBuildSettings    ++ // compiler flags
    splainSettings            ++ // gives rich output on implicit resolution errors
    dependencyUpdatesSettings ++ // check dependency updates on startup (max once per 12h)
    wartRemoverSettings       ++ // warts
    acyclicSettings           ++ // check circular dependencies between packages
    ossPublishSettings

lazy val commonSettings =
  commonSmlBuildSettings    ++ // compiler flags
    splainSettings            ++ // gives rich output on implicit resolution errors 
    dependencyUpdatesSettings ++ // check dependency updates on startup (max once per 12h)
//    wartRemoverSettings       ++ // warts
//    acyclicSettings           ++ // check circular dependencies between packages
    ossPublishSettings ++ Seq(
  scalafmtOnCompile := true
)