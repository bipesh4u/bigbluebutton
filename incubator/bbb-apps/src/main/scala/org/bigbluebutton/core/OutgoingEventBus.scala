package org.bigbluebutton.core

import akka.actor.ActorRef
import akka.event.{ EventBus, LookupClassification }
import org.bigbluebutton.core.api.OutGoingMsg.OutMsg

case class BigBlueButtonOutMessage(val topic: String, val payload: OutMsg)

class OutgoingEventBus extends EventBus with LookupClassification {
  type Event = BigBlueButtonOutMessage
  type Classifier = String
  type Subscriber = ActorRef

  // is used for extracting the classifier from the incoming events  
  override protected def classify(event: Event): Classifier = event.topic

  // will be invoked for each event for all subscribers which registered themselves
  // for the event’s classifier
  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event.payload
  }

  // must define a full order over the subscribers, expressed as expected from
  // `java.lang.Comparable.compare`
  override protected def compareSubscribers(a: Subscriber, b: Subscriber): Int =
    a.compareTo(b)

  // determines the initial size of the index data structure
  // used internally (i.e. the expected number of different classifiers)
  override protected def mapSize: Int = 128
}
