package org.bigbluebutton.bus

trait Red5AppsMsgTrait
trait Red5BusMsgTrait extends Red5AppsMsgTrait
trait PubSubBusMsgTrait extends Red5AppsMsgTrait

case class Red5AppsMsg(topic: String, payload: Red5BusMsgTrait)
case class FromClientMsg(name: String, json: String, connectionId: String, sessionToken: String)
  extends Red5BusMsgTrait
case class ToClientMsg(name: String, json: String, connectionId: String, sessionToken: String)
  extends Red5BusMsgTrait

case class PubSubMsg(channel: String, payload: PubSubBusMsgTrait)
case class FromPubSubMsg(json: String) extends PubSubBusMsgTrait
case class ToPubSubMsg(json: String) extends PubSubBusMsgTrait



