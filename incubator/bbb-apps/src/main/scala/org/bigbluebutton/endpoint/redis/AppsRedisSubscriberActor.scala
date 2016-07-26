package org.bigbluebutton.endpoint.redis

import akka.actor.Props
import java.net.InetSocketAddress

import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{ Message, PMessage }
import org.bigbluebutton.SystemConfiguration
import org.bigbluebutton.core.pubsub.receivers.RedisMessageReceiver
import org.bigbluebutton.core2x.bus.{ IncomingJsonMessage, IncomingJsonMessageBus, ReceivedJsonMessage }
import redis.api.servers.ClientSetname

object AppsRedisSubscriberActor extends SystemConfiguration {

  val channels = Seq("time")
  val patterns = Seq("bigbluebutton:to-bbb-apps:*", "bigbluebutton:from-voice-conf:*")

  def props(msgReceiver: RedisMessageReceiver): Props =
    Props(classOf[AppsRedisSubscriberActor], msgReceiver,
      redisHost, redisPort,
      channels, patterns).withDispatcher("akka.rediscala-subscriber-worker-dispatcher")
}

class AppsRedisSubscriberActor(
  msgReceiver: RedisMessageReceiver,
  redisHost: String,
  redisPort: Int,
  channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
    extends RedisSubscriberActor(
      new InetSocketAddress(
        redisHost,
        redisPort),
      channels, patterns) {

  // Set the name of this client to be able to distinguish when doing
  // CLIENT LIST on redis-cli
  write(ClientSetname("BbbAppsAkkaSub").encodedRequest)

  def onMessage(message: Message) {
    log.error(s"SHOULD NOT BE RECEIVING: $message")
  }

  def onPMessage(pmessage: PMessage) {
    //log.debug(s"RECEIVED:\n $pmessage \n")
    msgReceiver.handleMessage(pmessage.patternMatched, pmessage.channel, pmessage.data)
  }
}