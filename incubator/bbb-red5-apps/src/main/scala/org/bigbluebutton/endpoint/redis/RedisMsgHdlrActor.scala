package org.bigbluebutton.endpoint.redis

import akka.actor.{Actor, ActorLogging, Props}
import org.bigbluebutton.bus.PubSubMessageBus
import org.bigbluebutton.endpoint.redis.json.UnhandledJsonMsgHdlr
import org.bigbluebutton.red5apps.messages.Red5InJsonMsg

object RedisMsgHdlrActor {
  def props(pubSubBus: PubSubMessageBus): Props =
    Props(classOf[RedisMsgHdlrActor], pubSubBus)
}

class RedisMsgHdlrActor(val pubSubBus: PubSubMessageBus)
    extends Actor with ActorLogging
    with UnhandledJsonMsgHdlr {

  def receive = {
    case msg: Red5InJsonMsg => handleReceivedJsonMsg(msg)
    case _ => // do nothing
  }

}
