package org.bigbluebutton.connections

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import org.bigbluebutton.{MeetingTestFixtures, TestKitUsageSpec}
import org.bigbluebutton.bus.{FromClientMsg, Red5AppsMsgBus}
import org.bigbluebutton.connections.Connection.UpdateMsg
import org.bigbluebutton.endpoint.redis.RedisPublisher
import org.bigbluebutton.red5apps.{StopSystemAfterAll, SystemConfiguration}


class Connection01Test extends TestKit(ActorSystem("Connection01Test", ConfigFactory.parseString
(TestKitUsageSpec.config)))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with StopSystemAfterAll
  with MeetingTestFixtures
  with SystemConfiguration {

  val eventBus = new Red5AppsMsgBus
  val redisPublisher = new RedisPublisher(system)

  "A Connection Actor" should {
    "update the state of the connection when receiving ClientConnected" in {
      within(500 millis) {
        val state = new ConnectionStateModel()

        val connectionActorRef = system.actorOf(Connection.props(eventBus, redisPublisher,
          "someSessionToken", "someConnectionId", state))

        val msg = FromClientMsg("ClientConnected", "somejson", "aaaa", "bbbb")
        connectionActorRef ! msg

        val a = receiveWhile() {
          case msg: Connection.UpdateMsg => {
            println("connection state connectionTime=" + msg.asInstanceOf[UpdateMsg].a)
            assert(0L != msg.asInstanceOf[UpdateMsg].a)
          }
        }

        // expectMsgClass(classOf[Connection.UpdateMsg])
      }
    }

  }
}
