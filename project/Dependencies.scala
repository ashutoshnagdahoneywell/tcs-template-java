import sbt._

object Dependencies {

  val TcstemplatejavaAssembly = Seq(
    CSW.`csw-framework`,
    CSW.`csw-command`,
    CSW.`csw-location`,
    CSW.`csw-messages`,
    CSW.`csw-logging`,
    Akka.`akka-actor`,
    Akka.`akka-slf4j`,
    Akka.`akka-remote`,
    Akka.`akka-actor-typed`,
    Libs.`scalatest` % Test,
    Libs.`junit` % Test,
    Libs.`junit-interface` % Test
  )

  val TcstemplatejavaClient = Seq(
    CSW.`csw-framework`,
    CSW.`csw-command`,
    CSW.`csw-location`,
    CSW.`csw-messages`,
    CSW.`csw-logging`,
    Akka.`akka-actor`,
    Akka.`akka-slf4j`,
    Akka.`akka-remote`,
    Akka.`akka-actor-typed`,
    Libs.`scalatest` % Test,
    Libs.`junit` % Test,
    Libs.`junit-interface` % Test
  )

  val TcstemplatejavaHcd = Seq(
    CSW.`csw-framework`,
    CSW.`csw-command`,
    CSW.`csw-location`,
    CSW.`csw-messages`,
    CSW.`csw-logging`,
    Akka.`akka-actor`,
    Akka.`akka-slf4j`,
    Akka.`akka-remote`,
    Akka.`akka-actor-typed`,
    Libs.`scalatest` % Test,
    Libs.`junit` % Test,
    Libs.`junit-interface` % Test
  )

  val TcstemplatejavaDeploy = Seq(
    CSW.`csw-framework`
  )
}
