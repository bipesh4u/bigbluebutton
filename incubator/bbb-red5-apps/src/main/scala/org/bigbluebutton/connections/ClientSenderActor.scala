package org.bigbluebutton.connections
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import org.bigbluebutton.Red5OutGateway
import org.bigbluebutton.bus.{FromPubSubMsg, PubSubMessageBus, Red5MsgBus}
import org.bigbluebutton.red5.util.Util.extractName

object ClientSenderActor {
  def props(red5AppsMsgBus: Red5MsgBus, pubSubMessageBus: PubSubMessageBus, red5OutGW:
  Red5OutGateway): Props = Props(classOf[ClientSenderActor], red5AppsMsgBus, pubSubMessageBus,
    red5OutGW)
}

class ClientSenderActor(red5AppsMsgBus: Red5MsgBus, pubSubMessageBus:
PubSubMessageBus, red5OutGW: Red5OutGateway) extends Actor with ActorLogging {
  log.warning("Creating a new ClientSenderActor warn")

  val actorName = "client-sender-actor"
  override def preStart(): Unit = {
    pubSubMessageBus.subscribe(self, actorName)
    super.preStart()
  }

  override def postStop(): Unit = {
    pubSubMessageBus.unsubscribe(self, actorName)
    super.postStop()
  }

  def receive = {
    case msg: FromPubSubMsg => {
      val name = extractName(msg.json)
      name match {
        case msg: Any => log.warning("Unknown message " + msg)
      }
    }

//    case msg: Any => log.warning(s"ConnectionActor[$sessionToken] Unknown message " + msg)
  }

}

