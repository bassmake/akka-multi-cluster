import sbt._

object Dependencies {

  object Version {
    val akkaVersion = "2.5.22"
  }

  import Version._

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

}
