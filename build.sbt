import Dependencies._

ThisBuild / organization := "sk.bsmk"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.8"

lazy val CompileTest = "compile->compile;test->test"

lazy val `akka-multi-cluster` = (project in file("."))
  .enablePlugins(ParadoxPlugin)
  .settings(
    name := "akka-multi-cluster",
    paradoxTheme := Some(builtinParadoxTheme("generic"))
  )
  .aggregate(alpha, beta, gamma, shared)


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
      pegdown % Test,
      akkaActorTestkit % Test
    )
  )

lazy val commonSettings =
  commonSmlBuildSettings    ++ // compiler flags
    splainSettings            ++ // gives rich output on implicit resolution errors 
    dependencyUpdatesSettings ++ // check dependency updates on startup (max once per 12h)
//    wartRemoverSettings       ++ // warts
//    acyclicSettings           ++ // check circular dependencies between packages
    ossPublishSettings ++ Seq(
  scalafmtOnCompile := true,
    Test / fork := true,
    Test / logBuffered := false,
    Test / testOptions ++= Seq(
      Tests.Argument(TestFrameworks.ScalaTest, "-h", s"${baseDirectory.value}/target/test-html-report"),
      Tests.Argument(TestFrameworks.ScalaTest, "-o")
    )
)