package org.bigbluebutton.endpoint.redis

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import org.bigbluebutton.bus.{PubSubMessageBus, PubSubMsg, ToPubSubMsg}
import redis.RedisClient


object RedisSenderActor {
  def props(pubSubMessageBus: PubSubMessageBus, redis: RedisClient): Props =
    Props(classOf[RedisSenderActor], pubSubMessageBus, redis)
}

class RedisSenderActor(pubSubMessageBus: PubSubMessageBus, redis: RedisClient)
    extends Actor with ActorLogging {

  log.warning("Creating a new ConnectionsManager warn")

  val actorName = "redis-sender-actor"

  override def preStart(): Unit = {
    pubSubMessageBus.subscribe(self, actorName)
    super.preStart()
  }

  override def postStop(): Unit = {
    pubSubMessageBus.unsubscribe(self, actorName)
    super.postStop()
  }

  def receive = {
    case msg: PubSubMsg    => handlePubSubMsg(msg)
    case _                 => // do nothing for now.
  }

  private def publish(channel: String, data: String) {
    println(s"PUBLISH TO [$channel]: \n [$data]")
    redis.publish(channel, data)
  }

  private def handlePubSubMsg(msg: PubSubMsg): Unit = {
    val json = msg.payload.asInstanceOf[ToPubSubMsg].json
    publish(msg.channel, json)
  }


}
