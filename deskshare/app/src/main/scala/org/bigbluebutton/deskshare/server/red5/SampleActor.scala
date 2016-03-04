package org.bigbluebutton.deskshare.server.red5

/**
  * Created by anton on 02/03/16.
  */

import akka.actor.{ ActorSystem, Actor, Props }
import akka.actor.Actor._
import akka.event.Logging

import scala.util._

//import org.red5.logging.Red5LoggerFactory
object SampleActor {
  def props(actorSystem: ActorSystem, record: Boolean): Props =
    Props(classOf[SampleActor], actorSystem, record)
}

class SampleActor(val actorSystem: ActorSystem, val record: Boolean) extends Actor {

  val log = Logging(actorSystem, this)

//  val loggerActor = Logging(actorSystem, "my-sample-actor")
//  private val logred5 = Red5LoggerFactory.getLogger(classOf[SampleActor])

  log.info("aaaaaaaaaa")
  log.error("aaaaaaaaaa")
  log.warning("aaaaaaaaaa")

  def receive = {
    case "eeee" =>  sender ! ("REPLYYYYYYYYYYYY"); toPrint("REPLUUU")
    case m: Any => toPrint("______" + m)
  }

  def toPrint(string: String):Unit = {
    log.error(string)
    log.info(string)
    println(string + " from println")
  }
}
