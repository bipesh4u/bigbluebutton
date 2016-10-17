package org.bigbluebutton.connections

import akka.actor.{Actor, ActorLogging, Props}
import com.google.gson.{Gson, JsonObject, JsonParser}
import org.bigbluebutton.bus._
import org.bigbluebutton.common.messages.MessagingConstants
import org.bigbluebutton.connections.Connection.UpdateMsg

object Connection {
  def props(red5AppsBus: Red5MsgBus, pubSubMessageBus: PubSubMessageBus, sessionToken: String,
            connectionId: String, state: ConnectionStateModel = new ConnectionStateModel): Props =
    Props(classOf[Connection], red5AppsBus, pubSubMessageBus, sessionToken, connectionId, state)

  case class UpdateMsg(a: Long)
}


class Connection(red5AppsBus: Red5MsgBus, pubSubMessageBus: PubSubMessageBus, sessionToken:
String, connectionId: String, state: ConnectionStateModel) extends Actor with ActorLogging {
  log.warning(s"Creating a new Connection: sessionToken=$sessionToken connectionId=$connectionId " +
    s"connectionTime=${state.getConnectionTime}")

  override def preStart(): Unit = {
    red5AppsBus.subscribe(self, sessionToken)
    super.preStart()
  }

  override def postStop(): Unit = {
    red5AppsBus.unsubscribe(self, sessionToken)
    super.postStop()
  }

  def receive = {
    case msg: FromClientMsg => {
      msg.name match {
        case "ValidateAuthTokenRequestMessage" => handleValidateAuthTokenRequest(msg)
        case "ClientConnected"                 => handleClientConnected(msg)
        case "ClientDisconnected"              => handleClientDisconnected(msg)
        case _                                 => handleTransitMessage(msg)
      }
    }

    case msg: Any => log.warning(s"ConnectionActor[$sessionToken] Unknown message " + msg)
  }


  private def handleValidateAuthTokenRequest(msg: FromClientMsg): Unit = {

    val json = addReplyChannelToJsonMessage(msg.json)
    log.info(s"ValidateAuthToken [$json]")

    val outMsg = ToPubSubMsg(msg.json)
    pubSubMessageBus.publish(PubSubMsg(MessagingConstants.TO_BBB_APPS_PATTERN, outMsg))
   // send to pubsub with replychannel
//    redisPublisher.publish(MessagingConstants.TO_MEETING_CHANNEL, json)
  }

  private def handleClientConnected(msg: FromClientMsg): Unit = {
    log.info(s"_____handleClientConnected___${msg.sessionToken}  ${msg.connectionId}")
    state.setConnectionTime(genTimestamp())
    sender ! UpdateMsg(state.getConnectionTime)
  }

  private def handleClientDisconnected(msg: FromClientMsg): Unit = {
    log.info(s"_____handleClientDisconnected___${msg.sessionToken}  ${msg.connectionId}")
    state.setDisconnectionTime(genTimestamp())
  }

  private def handleTransitMessage(msg: FromClientMsg): Unit = {
    val outMsg = ToPubSubMsg(msg.json)
    pubSubMessageBus.publish(PubSubMsg(MessagingConstants.TO_BBB_APPS_PATTERN, outMsg))
  }

  private def addReplyChannelToJsonMessage(message: String): String = {
    val parser: JsonParser = new JsonParser
    val obj: JsonObject = parser.parse(message).asInstanceOf[JsonObject]
    var json = ""

    if (obj.has("header") && obj.has("body")) {
      val header: JsonObject = obj.get("header").getAsJsonObject
      val body: JsonObject = obj.get("body").getAsJsonObject
      header.addProperty("returnChannel", sessionToken)

      val res: JsonObject = new JsonObject
      res.add("body", body)
      res.add("header", header)
      val gson = new Gson()
      json = gson.toJson(res)
    }
    json
  }

  private def genTimestamp() = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

}
