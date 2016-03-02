package org.bigbluebutton.deskshare.server.red5

/**
  * Created by anton on 02/03/16.
  */

import akka.actor.{ ActorSystem, ActorLogging, Actor, Props }
import akka.actor.Actor._

object SampleActor {
  def props(actorSystem: ActorSystem, record: Boolean): Props =
    Props(classOf[SampleActor], actorSystem, record)
}

class SampleActor(val actorSystem: ActorSystem, val record: Boolean) extends Actor with ActorLogging {

  log.info("aaaaaaaaaa")
  log.error("aaaaaaaaaa")
  log.warning("aaaaaaaaaa")
  println(" did this workaaaa")

  def receive = {
    case m: Any => toPrint("______")
  }

  def toPrint(string: String):Unit = {
    log.error(string)
    log.info(string)
    println(string + " from println")
  }
}
