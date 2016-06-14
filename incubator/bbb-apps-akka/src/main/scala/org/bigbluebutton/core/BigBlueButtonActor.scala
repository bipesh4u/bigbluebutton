package org.bigbluebutton.core

import akka.actor._
import akka.actor.ActorLogging
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

import scala.concurrent.duration._
import org.bigbluebutton.core.bus._
import org.bigbluebutton.core.api.IncomingMessage._
import org.bigbluebutton.core.api.OutGoingMessage._
import org.bigbluebutton.SystemConfiguration

object BigBlueButtonActor extends SystemConfiguration {
  def props(system: ActorSystem,
    eventBus: IncomingEventBus,
    outGW: OutMessageGateway): Props =
    Props(classOf[BigBlueButtonActor], system, eventBus, outGW)
}

class BigBlueButtonActor(val system: ActorSystem,
    eventBus: IncomingEventBus, outGW: OutMessageGateway) extends Actor with ActorLogging {

  implicit def executionContext = system.dispatcher
  implicit val timeout = Timeout(5 seconds)

  private var meetings = new collection.immutable.HashMap[String, RunningMeeting]

  def receive = {
    case msg: CreateMeeting => handleCreateMeeting(msg)
    case msg: DestroyMeeting => handleDestroyMeeting(msg)
    case msg: KeepAliveMessage => handleKeepAliveMessage(msg)
    case msg: PubSubPing => handlePubSubPingMessage(msg)
    case msg: ValidateAuthToken => handleValidateAuthToken(msg)
    case msg: GetAllMeetingsRequest => handleGetAllMeetingsRequest(msg)
    case msg: UserJoinedVoiceConfMessage => handleUserJoinedVoiceConfMessage(msg)
    case msg: UserLeftVoiceConfMessage => handleUserLeftVoiceConfMessage(msg)
    case msg: UserLockedInVoiceConfMessage => handleUserLockedInVoiceConfMessage(msg)
    case msg: UserMutedInVoiceConfMessage => handleUserMutedInVoiceConfMessage(msg)
    case msg: UserTalkingInVoiceConfMessage => handleUserTalkingInVoiceConfMessage(msg)
    case msg: VoiceConfRecordingStartedMessage => handleVoiceConfRecordingStartedMessage(msg)
    case msg: DeskShareStartedRequest => handleDeskShareStartedRequest(msg)
    case msg: DeskShareStoppedRequest => handleDeskShareStoppedRequest(msg)
    case msg: DeskShareRTMPBroadcastStartedRequest => handleDeskShareRTMPBroadcastStartedRequest(msg)
    case msg: DeskShareRTMPBroadcastStoppedRequest => handleDeskShareRTMPBroadcastStoppedRequest(msg)
    case msg: DeskShareGetDeskShareInfoRequest => handleDeskShareGetDeskShareInfoRequest(msg)
    case _ => // do nothing
  }

  private def findMeetingWithVoiceConfId(voiceConfId: String): Option[RunningMeeting] = {
    meetings.values.find(m => { m.mProps.voiceConf.value == voiceConfId })
  }

  private def handleUserJoinedVoiceConfMessage(msg: UserJoinedVoiceConfMessage) {
    findMeetingWithVoiceConfId(msg.voiceConfId) foreach { m => m.actorRef ! msg }
  }

  private def handleUserLeftVoiceConfMessage(msg: UserLeftVoiceConfMessage) {
    findMeetingWithVoiceConfId(msg.voiceConfId) foreach { m =>
      m.actorRef ! msg
    }
  }

  private def handleUserLockedInVoiceConfMessage(msg: UserLockedInVoiceConfMessage) {
    findMeetingWithVoiceConfId(msg.voiceConfId) foreach { m =>
      m.actorRef ! msg
    }
  }

  private def handleUserMutedInVoiceConfMessage(msg: UserMutedInVoiceConfMessage) {
    findMeetingWithVoiceConfId(msg.voiceConfId) foreach { m =>
      m.actorRef ! msg
    }
  }

  private def handleVoiceConfRecordingStartedMessage(msg: VoiceConfRecordingStartedMessage) {
    findMeetingWithVoiceConfId(msg.voiceConfId) foreach { m =>
      m.actorRef ! msg
    }

  }

  private def handleUserTalkingInVoiceConfMessage(msg: UserTalkingInVoiceConfMessage) {
    findMeetingWithVoiceConfId(msg.voiceConfId) foreach { m =>
      m.actorRef ! msg
    }
  }

  private def handleValidateAuthToken(msg: ValidateAuthToken) {
    meetings.get(msg.meetingID) foreach { m =>
      m.actorRef ! msg

      //      val future = m.actorRef.ask(msg)(5 seconds)
      //      future onComplete {
      //        case Success(result) => {
      //          log.info("Validate auth token response. meetingId=" + msg.meetingID + " userId=" + msg.userId + " token=" + msg.token)
      //          /**
      //           * Received a reply from MeetingActor which means hasn't hung!
      //           * Sometimes, the actor seems to hang and doesn't anymore accept messages. This is a simple
      //           * audit to check whether the actor is still alive. (ralam feb 25, 2015)
      //           */
      //        }
      //        case Failure(failure) => {
      //          log.warning("Validate auth token timeout. meetingId=" + msg.meetingID + " userId=" + msg.userId + " token=" + msg.token)
      //          outGW.send(new ValidateAuthTokenTimedOut(msg.meetingID, msg.userId, msg.token, false, msg.correlationId))
      //        }
      //      }
    }
  }

  private def handleKeepAliveMessage(msg: KeepAliveMessage): Unit = {
    outGW.send(new KeepAliveMessageReply(msg.aliveID))
  }

  private def handlePubSubPingMessage(msg: PubSubPing): Unit = {
    outGW.send(new PubSubPong(msg.system, msg.timestamp))
  }

  private def handleDestroyMeeting(msg: DestroyMeeting) {
    log.info("Received DestroyMeeting message for meetingId={}", msg.meetingId)
    val meetingId = msg.meetingId.value

    meetings.get(meetingId) match {
      case None => log.info("Could not find meetingId={}", meetingId)
      case Some(m) => {
        meetings -= meetingId
        log.info("Kick everyone out on meetingId={}", meetingId)
        if (m.mProps.isBreakout) {
          log.info("Informing parent meeting {} that a breakout room has been ended{}", m.mProps.externalMeetingID, m.mProps.meetingID)
          eventBus.publish(BigBlueButtonEvent(m.mProps.externalMeetingID,
            BreakoutRoomEnded(m.mProps.externalMeetingID, m.mProps.meetingID)))
        }
        outGW.send(new EndAndKickAll(msg.meetingID, m.mProps.recorded))
        outGW.send(new DisconnectAllUsers(msg.meetingID))
        log.info("Destroyed meetingId={}", msg.meetingID)
        outGW.send(new MeetingDestroyed(msg.meetingID))

        /** Unsubscribe to meeting and voice events. **/
        eventBus.unsubscribe(m.actorRef, m.mProps.meetingID)
        eventBus.unsubscribe(m.actorRef, m.mProps.voiceBridge)

        // Stop the meeting actor.
        context.stop(m.actorRef)
      }
    }
  }

  private def handleCreateMeeting(msg: CreateMeeting): Unit = {
    meetings.get(msg.meetingID) match {
      case None => {
        log.info("Create meeting request. meetingId={}", msg.mProps.meetingID)

        var m = RunningMeeting(msg.mProps, outGW, eventBus)

        /** Subscribe to meeting and voice events. **/
        eventBus.subscribe(m.actorRef, m.mProps.meetingID)
        eventBus.subscribe(m.actorRef, m.mProps.voiceBridge)

        meetings += m.mProps.meetingID -> m
        outGW.send(new MeetingCreated(m.mProps.meetingID, m.mProps.externalMeetingID, m.mProps.recorded, m.mProps.meetingName,
          m.mProps.voiceBridge, msg.mProps.duration, msg.mProps.moderatorPass,
          msg.mProps.viewerPass, msg.mProps.createTime, msg.mProps.createDate))

        m.actorRef ! new InitializeMeeting(m.mProps.meetingID, m.mProps.recorded)
      }
      case Some(m) => {
        log.info("Meeting already created. meetingID={}", msg.mProps.meetingID)
        // do nothing
      }
    }
  }

  private def handleGetAllMeetingsRequest(msg: GetAllMeetingsRequest) {

    var len = meetings.keys.size
    println("meetings.size=" + meetings.size)
    println("len_=" + len)

    val set = meetings.keySet
    val arr: Array[String] = new Array[String](len)
    set.copyToArray(arr)
    val resultArray: Array[MeetingInfo] = new Array[MeetingInfo](len)

    for (i <- 0 until arr.length) {
      val id = arr(i)
      val duration = meetings.get(arr(i)).head.mProps.duration
      val name = meetings.get(arr(i)).head.mProps.meetingName
      val recorded = meetings.get(arr(i)).head.mProps.recorded
      val voiceBridge = meetings.get(arr(i)).head.mProps.voiceBridge

      var info = new MeetingInfo(id, name, recorded, voiceBridge, duration)
      resultArray(i) = info

      //send the users
      self ! (new GetUsers(id, "nodeJSapp"))

      //send the presentation
      self ! (new GetPresentationInfo(id, "nodeJSapp", "nodeJSapp"))

      //send chat history
      self ! (new GetChatHistoryRequest(id, "nodeJSapp", "nodeJSapp"))

      //send lock settings
      self ! (new GetLockSettings(id, "nodeJSapp"))

      //send desktop sharing info
      self ! (new DeskShareGetDeskShareInfoRequest(id, "nodeJSapp", "nodeJSapp"))

    }

    outGW.send(new GetAllMeetingsReply(resultArray))
  }

  private def handleDeskShareStartedRequest(msg: DeskShareStartedRequest) {
    log.info("handleDeskShareStartedRequest: msg.conferenceName=" + msg.conferenceName)
    findMeetingWithVoiceConfId(msg.conferenceName) foreach { m =>
      {
        //        println(msg.conferenceName + " (in for each) handleDeskShareStartedRequest BBBActor   ")
        m.actorRef ! msg
      }
    }
  }

  private def handleDeskShareStoppedRequest(msg: DeskShareStoppedRequest) {
    log.info("handleDeskShareStoppedRequest msg.conferenceName=" + msg.conferenceName)
    findMeetingWithVoiceConfId(msg.conferenceName) foreach { m =>
      {
        //        println(msg.conferenceName + " (in for each) handleDeskShareStoppedRequest BBBActor   ")
        m.actorRef ! msg
      }
    }
  }

  private def handleDeskShareRTMPBroadcastStartedRequest(msg: DeskShareRTMPBroadcastStartedRequest) {
    log.info("handleDeskShareRTMPBroadcastStartedRequest msg.conferenceName=" + msg.conferenceName)
    findMeetingWithVoiceConfId(msg.conferenceName) foreach { m =>
      {
        //        println(msg.conferenceName + " (in for each) handleDeskShareRTMPBroadcastStartedRequest BBBActor   ")
        m.actorRef ! msg
      }
    }
  }

  private def handleDeskShareRTMPBroadcastStoppedRequest(msg: DeskShareRTMPBroadcastStoppedRequest) {
    log.info("handleDeskShareRTMPBroadcastStoppedRequest msg.conferenceName=" + msg.conferenceName)
    findMeetingWithVoiceConfId(msg.conferenceName) foreach { m =>
      {
        //        println(msg.conferenceName + " (in for each) handleDeskShareRTMPBroadcastStoppedRequest BBBActor   ")
        m.actorRef ! msg
      }
    }
  }

  private def handleDeskShareGetDeskShareInfoRequest(msg: DeskShareGetDeskShareInfoRequest): Unit = {
    val m = meetings.values.find(m => {
      m.mProps.meetingID == msg.conferenceName
    })
    m foreach { mActor => mActor.actorRef ! msg }
  }

}

