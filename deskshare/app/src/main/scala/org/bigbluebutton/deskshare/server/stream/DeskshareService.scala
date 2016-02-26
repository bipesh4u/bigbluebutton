/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
* version.
* 
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
*
*/
package org.bigbluebutton.deskshare.server.stream

import akka.actor.{ ActorRef, ActorSystem, Props }
import org.bigbluebutton.deskshare.server.stream.StreamManager.{ StreamPublishingReply, IsStreamPublishing }
import org.slf4j.LoggerFactory
import akka.pattern.ask
import scala.concurrent.duration._

import scala.util.{ Failure, Success }
import org.bigbluebutton.deskshare.server.sessions.SessionManagerGateway
import org.red5.server.api.Red5
import java.util.HashMap

class DeskshareService(actorSystem: ActorSystem, streamManager: ActorRef, sessionGateway: SessionManagerGateway) {
  implicit def executionContext = actorSystem.dispatcher
  private val log = LoggerFactory.getLogger(classOf[DeskshareService])

  def checkIfStreamIsPublishing(room: String): HashMap[String, Any] = {
    //		val room: String = Red5.getConnectionLocal().getScope().getName();
    log.debug("Checking if %s is streaming.", room)
    var publishing = false
    var width = 0
    var height = 0

    val future = streamManager.ask(IsStreamPublishing(room))(3.seconds)
    future onComplete {
      case Success(rep) => {
        val reply = rep.asInstanceOf[StreamPublishingReply]
        publishing = reply.publishing
        width = reply.width
        height = reply.height
        log.info("CASE1111111")
      }
      case Failure(failure) => {
        log.warn("DeskshareService: Timeout waiting for reply to IsStreamPublishing for room %s", room)
        log.warn("CASE2222222")
      }
    }

    val stream = new HashMap[String, Any]()
    stream.put("publishing", publishing)
    stream.put("width", width)
    stream.put("height", height)

    return stream;
  }

  def startedToViewStream(stream: String): Unit = {
    log.debug("DeskshareService: Started viewing stream for room %s", stream)
    sessionGateway.sendKeyFrame(stream)
  }

  def stopSharingDesktop(meetingId: String) {
    log.debug("DeskshareService: Stop sharing for meeting [%s]", meetingId)
    sessionGateway.stopSharingDesktop(meetingId, meetingId)
  }
}
