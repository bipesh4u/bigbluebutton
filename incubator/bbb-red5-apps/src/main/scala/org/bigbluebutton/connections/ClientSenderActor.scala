package org.bigbluebutton.connections
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import org.bigbluebutton.Red5OutGateway
import org.bigbluebutton.bus.{FromClientMsg, PubSubMessageBus, Red5AppsMsgBus}

object ClientSenderActor {
  def props(red5AppsMsgBus: Red5AppsMsgBus, pubSubMessageBus: PubSubMessageBus, red5OutGW:
  Red5OutGateway): Props = Props(classOf[ClientSenderActor], red5AppsMsgBus, pubSubMessageBus,
    red5OutGW)
}

class ClientSenderActor(red5AppsMsgBus: Red5AppsMsgBus, pubSubMessageBus:
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

    case msg: Any => log.warning("Unknown message " + msg)
  }



}

