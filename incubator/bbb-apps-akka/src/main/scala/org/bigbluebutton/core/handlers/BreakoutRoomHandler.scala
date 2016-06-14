package org.bigbluebutton.core.handlers

import org.bigbluebutton.core.api.IncomingMessage._
import org.bigbluebutton.core.api.OutGoingMessage._
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.SystemConfiguration
import org.apache.commons.codec.digest.DigestUtils
import org.bigbluebutton.core.domain._
import scala.collection._
import scala.collection.SortedSet
import java.net.URLEncoder
import org.bigbluebutton.core.bus.IncomingEventBus
import org.bigbluebutton.core.bus.BigBlueButtonEvent
import org.bigbluebutton.core.LiveMeeting
import org.bigbluebutton.core.models._

trait BreakoutRoomHandler extends SystemConfiguration {
  this: LiveMeeting =>

  val outGW: OutMessageGateway
  val eventBus: IncomingEventBus

  def getDefaultPresentationURL(): String = {
    var presURL = bbbWebDefaultPresentationURL
    val page = presModel.getCurrentPage()
    page foreach { p =>
      presURL = BreakoutRoomsUtil.fromSWFtoPDF(p.swfUri)
    }
    presURL
  }

  def handleBreakoutRoomsList(msg: BreakoutRoomsListMessage) {
    val breakoutRooms = breakoutModel.getRooms().toVector map { r => new BreakoutRoomBody(r.name, IntMeetingId(r.id)) }
    outGW.send(new BreakoutRoomsListOutMessage(props.id, breakoutRooms))
  }

  def handleCreateBreakoutRooms(msg: CreateBreakoutRooms) {
    var i = 0
    for (room <- msg.rooms) {
      i += 1
      val presURL = bbbWebDefaultPresentationURL
      val breakoutMeetingId = BreakoutRoomsUtil.createMeetingId(props.id.value, i)
      val voiceConfId = BreakoutRoomsUtil.createVoiceConfId(props.voiceConf.value, i)
      val r = breakoutModel.createBreakoutRoom(breakoutMeetingId, room.name, voiceConfId, room.users, presURL)
      val p = new BreakoutRoomOutPayload(IntMeetingId(r.id), Name(r.name), props.id,
        VoiceConf(r.voiceConfId), msg.durationInMinutes, bbbWebModeratorPassword, bbbWebViewerPassword,
        r.defaultPresentationURL)
      outGW.send(new CreateBreakoutRoom(props.id, props.recorded, p))
    }
    meeting.breakoutRoomsdurationInMinutes = msg.durationInMinutes
    meeting.breakoutRoomsStartedOn = timeNowInSeconds
  }

  def sendJoinURL(userId: String, breakoutId: String) {
    for {
      user <- meeting.getUser(IntUserId(userId))
      apiCall = "join"
      params = BreakoutRoomsUtil.joinParams(user.name.value, userId, true, breakoutId, bbbWebModeratorPassword, true)
      baseString = BreakoutRoomsUtil.createBaseString(params)
      checksum = BreakoutRoomsUtil.calculateChecksum(apiCall, baseString, bbbWebSharedSecret)
      joinURL = BreakoutRoomsUtil.createJoinURL(bbbWebAPI, apiCall, baseString, checksum)
    } yield outGW.send(new BreakoutRoomJoinURLOutMessage(props.id, props.recorded,
      IntMeetingId(breakoutId), IntUserId(userId), joinURL))
  }

  def handleRequestBreakoutJoinURL(msg: RequestBreakoutJoinURLInMessage) {
    sendJoinURL(msg.userId, msg.breakoutId)
  }

  def handleBreakoutRoomCreated(msg: BreakoutRoomCreated) {
    val room = breakoutModel.getBreakoutRoom(msg.breakoutRoomId)
    room foreach { room =>
      sendBreakoutRoomStarted(props.id.value, room.name, room.id, room.voiceConfId)
    }

    breakoutModel.getAssignedUsers(msg.breakoutRoomId) foreach { users =>
      users.foreach { u =>
        log.debug("## Sending Join URL for users: {}", u)
        sendJoinURL(u, msg.breakoutRoomId)
      }
    }
  }

  def sendBreakoutRoomStarted(meetingId: String, breakoutName: String, breakoutId: String, voiceConfId: String) {
    outGW.send(new BreakoutRoomStartedOutMessage(IntMeetingId(meetingId), props.recorded,
      new BreakoutRoomBody(breakoutName, IntMeetingId(breakoutId))))
  }

  def handleBreakoutRoomEnded(msg: BreakoutRoomEnded) {
    breakoutModel.remove(msg.breakoutRoomId)
    outGW.send(new BreakoutRoomEndedOutMessage(IntMeetingId(msg.meetingId), IntMeetingId(msg.breakoutRoomId)))
  }

  def handleBreakoutRoomUsersUpdate(msg: BreakoutRoomUsersUpdate) {
    breakoutModel.updateBreakoutUsers(msg.breakoutId, msg.users) foreach { room =>
      outGW.send(new UpdateBreakoutUsersOutMessage(props.id, props.recorded, IntMeetingId(msg.breakoutId), room.users))
    }
  }

  def handleSendBreakoutUsersUpdate(msg: SendBreakoutUsersUpdate) {
    val users = meeting.getUsers().toVector
    val breakoutUsers = users map { u => new BreakoutUser(u.id.value, u.name.value) }
    eventBus.publish(BigBlueButtonEvent(props.extId.value,
      new BreakoutRoomUsersUpdate(props.extId.value, props.id.value, breakoutUsers)))
  }

  def handleTransferUserToMeeting(msg: TransferUserToMeetingRequest) {
    var targetVoiceBridge: String = msg.targetMeetingId.value
    // If the current room is a parent room we fetch the voice bridge from the breakout room
    if (!props.isBreakout) {
      breakoutModel.getBreakoutRoom(msg.targetMeetingId.value) match {
        case Some(b) =>
          targetVoiceBridge = b.voiceConfId

        case None => // do nothing
      }
    } // if it is a breakout room, the target voice bridge is the same after removing the last digit
    else {
      targetVoiceBridge = props.voiceConf.value.dropRight(1)
    }
    // We check the iser from the mode
    meeting.getUser(msg.userId) match {
      case Some(u) =>
        if (u.voiceUser.joinedVoice.value) {
          log.info("Transferring user userId=" + u.id + " from voiceBridge=" + props.voiceConf
            + " to targetVoiceConf=" + targetVoiceBridge)
          outGW.send(new TransferUserToMeeting(props.voiceConf, VoiceConf(targetVoiceBridge), u.voiceUser.id))
        }

      case None => // do nothing
    }
  }

  def handleEndAllBreakoutRooms(msg: EndAllBreakoutRooms) {
    log.info("EndAllBreakoutRooms event received for meetingId={}", props.id.value)
    breakoutModel.getRooms().foreach { room =>
      outGW.send(new EndBreakoutRoom(IntMeetingId(room.id)))
    }
  }

}

object BreakoutRoomsUtil {
  def createMeetingId(id: String, index: Int): String = {
    id.concat("-").concat(index.toString)
  }

  def createVoiceConfId(id: String, index: Int): String = {
    id.concat(index.toString)
  }

  def fromSWFtoPDF(swfURL: String): String = {
    swfURL.replace("swf", "pdf")
  }

  def createJoinURL(webAPI: String, apiCall: String, baseString: String, checksum: String): String = {
    var apiURL = if (webAPI.endsWith("/")) webAPI else webAPI.concat("/")
    apiURL.concat(apiCall).concat("?").concat(baseString).concat("&checksum=").concat(checksum)
  }

  //
  //checksum() -- Return a checksum based on SHA-1 digest
  //
  def checksum(s: String): String = {
    DigestUtils.sha1Hex(s)
  }

  def calculateChecksum(apiCall: String, baseString: String, sharedSecret: String): String = {
    checksum(apiCall.concat(baseString).concat(sharedSecret))
  }

  def joinParams(username: String, userId: String, isBreakout: Boolean, breakoutId: String,
    password: String, redirect: Boolean): mutable.Map[String, String] = {
    val params = new collection.mutable.HashMap[String, String]
    params += "fullName" -> urlEncode(username)
    params += "userID" -> urlEncode(userId + "-" + breakoutId.substring(breakoutId.lastIndexOf("-") + 1));
    params += "isBreakout" -> urlEncode(isBreakout.toString())
    params += "meetingID" -> urlEncode(breakoutId)
    params += "password" -> urlEncode(password)
    params += "redirect" -> urlEncode(redirect.toString)

    params
  }

  def sortParams(params: mutable.Map[String, String]): SortedSet[String] = {
    collection.immutable.SortedSet[String]() ++ params.keySet
  }

  //From the list of parameters we want to pass. Creates a base string with parameters
  //sorted in alphabetical order for us to sign.
  def createBaseString(params: mutable.Map[String, String]): String = {
    val csbuf = new StringBuffer()
    val keys = sortParams(params)

    var first = true
    for (key <- keys) {
      for (value <- params.get(key)) {
        if (first) {
          first = false
        } else {
          csbuf.append("&")
        }

        csbuf.append(key)
        csbuf.append("=")
        csbuf.append(value)
      }
    }

    return csbuf.toString
  }

  def urlEncode(s: String): String = {
    URLEncoder.encode(s, "UTF-8")
  }

  //
  //encodeURIComponent() -- Java encoding similiar to JavaScript encodeURIComponent
  //
  def encodeURIComponent(component: String): String = {
    URLEncoder.encode(component, "UTF-8")
      .replaceAll("\\%28", "(")
      .replaceAll("\\%29", ")")
      .replaceAll("\\+", "%20")
      .replaceAll("\\%27", "'")
      .replaceAll("\\%21", "!")
      .replaceAll("\\%7E", "~")
  }

}
