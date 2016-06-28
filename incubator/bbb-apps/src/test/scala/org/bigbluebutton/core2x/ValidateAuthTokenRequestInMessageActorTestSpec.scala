package org.bigbluebutton.core2x

import akka.actor.ActorSystem
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import com.typesafe.config.ConfigFactory
import org.bigbluebutton.SystemConfiguration
import org.bigbluebutton.core.bus.OutgoingEventBus
import org.bigbluebutton.core.{ OutMessageGateway, StopSystemAfterAll, TestKitUsageSpec }
import org.bigbluebutton.core2x.api.OutGoingMessage.{ DisconnectUser2x, UserRegisteredEvent2x, ValidateAuthTokenSuccessReplyOutMessage }
import org.bigbluebutton.core2x.bus.IncomingEventBus2x
import org.bigbluebutton.core2x.models.{ MeetingStateModel, MeetingStatus }
import org.scalatest.{ Matchers, WordSpecLike }

import scala.concurrent.duration._

class ValidateAuthTokenRequestInMessageActorTestSpec extends TestKit(ActorSystem("ValidateAuthTokenRequestInMessageActorTestSpec",
  ConfigFactory.parseString(TestKitUsageSpec.config)))
    with DefaultTimeout with ImplicitSender with WordSpecLike
    with Matchers with StopSystemAfterAll with MeetingTestFixtures with SystemConfiguration {

  val eventBus = new IncomingEventBus2x
  val outgoingEventBus = new OutgoingEventBus
  val outGW = new OutMessageGateway(outgoingEventBus)
  outgoingEventBus.subscribe(testActor, outgoingMessageChannel)

  "A MeetingActor" should {
    "Send a DisconnectUser when receiving ValitadateAuthTokenCommand and there is no registered user" in {
      within(500 millis) {
        val state: MeetingStateModel = new MeetingStateModel(
          piliProps, abilities, registeredUsers, users, chats, layouts, polls,
          whiteboards, presentations, breakoutRooms, captions, new MeetingStatus)

        val meetingActorRef = system.actorOf(MeetingActor2x.props(piliProps, eventBus, outGW, state))
        meetingActorRef ! du30ValidateAuthTokenCommand
        expectMsgClass(classOf[DisconnectUser2x])
      }
    }
  }

  "A MeetingActor" should {
    "Send a ValidateAuthTokenReply when receiving ValitadateAuthTokenCommand and there is registered user" in {
      within(500 millis) {
        val state: MeetingStateModel = new MeetingStateModel(piliProps,
          abilities, registeredUsers, users, chats, layouts, polls, whiteboards,
          presentations, breakoutRooms, captions, new MeetingStatus)
        val meetingActorRef = system.actorOf(MeetingActor2x.props(piliProps, eventBus, outGW, state))
        meetingActorRef ! du30RegisterUserCommand
        expectMsgClass(classOf[UserRegisteredEvent2x])
        meetingActorRef ! du30ValidateAuthTokenCommand
        expectMsgClass(classOf[ValidateAuthTokenSuccessReplyOutMessage])
      }
    }
  }
}