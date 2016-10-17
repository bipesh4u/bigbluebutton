package org.bigbluebutton.connections

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import org.bigbluebutton.bus.{FromClientMsg, PubSubMessageBus, Red5MsgBus}

import scala.collection.mutable

object ConnectionsManager {
  def props(system: ActorSystem, red5AppsMsgBus: Red5MsgBus, pubSubMessageBus:
  PubSubMessageBus): Props = Props(classOf[ConnectionsManager], system, red5AppsMsgBus,
    pubSubMessageBus)
}

class ConnectionsManager(system: ActorSystem, red5AppsMsgBus: Red5MsgBus, pubSubMessageBus:
PubSubMessageBus) extends Actor with ActorLogging {
  log.warning("Creating a new ConnectionsManager warn")

  val actorName = "connection-manager-actor"

  override def preStart(): Unit = {
    red5AppsMsgBus.subscribe(self, actorName)
    super.preStart()
  }

  override def postStop(): Unit = {
    red5AppsMsgBus.unsubscribe(self, actorName)
    super.postStop()
  }

  private val connections = new mutable.HashMap[String, ActorRef]

  def receive = {
    case msg:FromClientMsg => {
      msg.name match {
        case "ClientConnected"      => handleClientConnected(msg)
        case "ClientDisconnected"   => handleClientDisconnected(msg)

        // case _                      => handleTransitMessage(msg) // TODO - Most likely have to
        // remove this as keeping it means the Connection actor (already subscribed in the
        // messagebus) will receive the message twice
      }
    }
    case msg: Any => log.warning("Unknown message " + msg)
  }

  private def handleClientConnected(msg: FromClientMsg): Unit = {
    log.info(s"Client connected sessionToken=${msg.sessionToken} connId=${msg.connectionId}")

    connections.get(msg.sessionToken) match {
      case None => {
        if (log.isDebugEnabled) {
          log.debug(s"First encounter of connId=${msg.connectionId} for sessionToken=${msg
            .sessionToken}")
        }

        val newConnection = system.actorOf(Connection.props(red5AppsMsgBus, pubSubMessageBus, msg
          .sessionToken, msg.connectionId), msg.sessionToken)
        connections += msg.sessionToken -> newConnection

        newConnection ! msg
      }
      case Some(conn) => {
        if (log.isDebugEnabled) {
          log.debug(s"Connection connId=${msg.connectionId} for sessionToken=${msg.sessionToken} " +
            s"already exists")
        }
        conn ! msg
      }
    }
  }

  private def handleClientDisconnected(msg: FromClientMsg): Unit = {
    log.info(s"Client disconnected sessionToken=${msg.sessionToken} connId=${msg.connectionId}")

    connections.get(msg.sessionToken) foreach { connection =>
      connection forward msg
    }

    connections -= msg.sessionToken
  }

  private def handleTransitMessage(msg: FromClientMsg): Unit = {
    connections.get(msg.sessionToken) foreach { connection =>
      connection ! msg
    }
  }

}
