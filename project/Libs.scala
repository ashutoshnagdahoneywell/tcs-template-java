import sbt._

object Libs {
  val ScalaVersion = "2.12.4"

  val `scalatest`       = "org.scalatest"          %% "scalatest"      % "3.0.5"  //Apache License 2.0
  val `scala-async`     = "org.scala-lang.modules" %% "scala-async"    % "0.9.7"  //BSD 3-clause "New" or "Revised" License
  val `junit`           = "junit"                  % "junit"           % "4.12"   //Eclipse Public License 1.0
  val `junit-interface` = "com.novocode"           % "junit-interface" % "0.11"   //BSD 2-clause "Simplified" License
  val `mockito-core`    = "org.mockito"            % "mockito-core"    % "2.16.0" //MIT License
}

object CSW {
  val Version = "0.4.0"

  val `csw-location`      = "org.tmt" %% "csw-location"      % Version
  val `csw-config-client` = "org.tmt" %% "csw-config-client" % Version
  val `csw-logging`       = "org.tmt" %% "csw-logging"       % Version
  val `csw-framework`     = "org.tmt" %% "csw-framework"     % Version
  val `csw-command`       = "org.tmt" %% "csw-command"       % Version
  val `csw-messages`      = "org.tmt" %% "csw-messages"      % Version
}

object Akka {
  val Version                   = "2.5.11" //all akka is Apache License 2.0
  val `akka-stream`             = "com.typesafe.akka" %% "akka-stream" % Version
  val `akka-remote`             = "com.typesafe.akka" %% "akka-remote" % Version
  val `akka-stream-testkit`     = "com.typesafe.akka" %% "akka-stream-testkit" % Version
  val `akka-actor`              = "com.typesafe.akka" %% "akka-actor" % Version
  val `akka-actor-typed`        = "com.typesafe.akka" %% "akka-actor-typed" % Version
  val `akka-testkit-typed`      = "com.typesafe.akka" %% "akka-testkit-typed" % Version
  val `akka-distributed-data`   = "com.typesafe.akka" %% "akka-distributed-data" % Version
  val `akka-multi-node-testkit` = "com.typesafe.akka" %% "akka-multi-node-testkit" % Version
  val `akka-cluster-tools`      = "com.typesafe.akka" %% "akka-cluster-tools" % Version
  val `akka-cluster-typed`      = "com.typesafe.akka" %% "akka-cluster-typed" % Version
  val `akka-slf4j`              = "com.typesafe.akka" %% "akka-slf4j" % Version
}
