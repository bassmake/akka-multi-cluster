import sbt._

object Dependencies {

  object Version {
    val akkaVersion = "2.5.22"
//    val akkaVersion = "2.6.0-M1"
    val scalaLoggingVersion = "3.9.2"
    val logbackVersion = "1.2.3"


    val scalaTestVersion = "3.0.5"
    val pegdownVersion = "1.6.0"
  }

  import Version._

  val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion
  
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  val logback =  "ch.qos.logback" % "logback-classic" % logbackVersion

  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  val pegdown = "org.pegdown" % "pegdown" % pegdownVersion
  val akkaActorTestkit  = "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion
  
}
