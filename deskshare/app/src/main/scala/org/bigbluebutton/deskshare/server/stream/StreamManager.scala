/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2016 BigBlueButton Inc. and by respective authors (see below).
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

import akka.actor.{ ActorSystem, ActorLogging, Props, Actor }
import org.bigbluebutton.deskshare.server.red5.DeskshareApplication
import org.bigbluebutton.deskshare.server.stream.StreamManager._
import org.red5.server.api.scope.IScope
import org.red5.server.api.so.ISharedObject

import scala.collection.mutable.HashMap
import org.bigbluebutton.deskshare.server.recorder._

object StreamManager {
  def props(actorSystem: ActorSystem, record: Boolean, recordingService: RecordingService): Props =
    Props(classOf[StreamManager], actorSystem, record, recordingService)

  case class IsStreamPublishing(room: String)
  case class StreamPublishingReply(publishing: Boolean, width: Int, height: Int)
  case class CreateStream(room: String, width: Int, height: Int)
  case class DestroyStream(room: String)
  case class SetApplication(app: DeskshareApplication)
  private case class AddStream(room: String, stream: DeskshareStream)
  private case class RemoveStream(room: String)

}

class StreamManager(val actorSystem: ActorSystem, val record: Boolean, val recordingService: RecordingService) extends Actor with ActorLogging {

  val actorRef = context.actorOf(StreamManager.props(actorSystem, record, recordingService), "stream-manager-actor")
  var deskshareApplication: DeskshareApplication = null

  println("StreamManager in class")

  private val streams = new HashMap[String, DeskshareStream]

  def receive = {
    case cs: AddStream => {
      log.debug("StreamManager: Adding stream %s", cs.room)
      streams += cs.room -> cs.stream
    }
    case ds: RemoveStream => {
      log.debug("StreamManager: Removing Stream %s", ds.room)
      streams -= ds.room
    }
    case is: IsStreamPublishing => {
      log.debug("StreamManager: Received IsStreamPublishing message for %s", is.room)
      streams.get(is.room) match {
        case Some(str) => sender() ! new StreamPublishingReply(true, str.width, str.height)
        case None => sender() ! new StreamPublishingReply(false, 0, 0)
      }
    }
    case msg: CreateStream => createStream(msg.room, msg.width, msg.height)
    case msg: DestroyStream => destroyStream(msg.room)
    case msg: SetApplication => setDeskshareApplication(msg.app)
    case m: Any => log.warning("StreamManager: StreamManager received unknown message: %s", m)
  }

  def setDeskshareApplication(application: DeskshareApplication) = {
    deskshareApplication = application
  }
  def createStream(room: String, width: Int, height: Int): Option[DeskshareStream] = {
    try {
      log.debug("StreamManager: Creating stream for [ %s ]", room)
      val stream = new DeskshareStream(deskshareApplication, room, width, height, record, recordingService.getRecorderFor(room))
      log.debug("StreamManager: Initializing stream for [ %s ]", room)
      if (stream.initializeStream) {
        log.debug("StreamManager: Starting stream for [ %s ]", room)
        //				stream.start
        actorRef ! new AddStream(room, stream)
        return Some(stream)
      } else {
        log.debug("StreamManager: Failed to initialize stream for [ %s ]", room)
        return None
      }
    } catch {
      case nl: java.lang.NullPointerException =>
        log.error("StreamManager: %s", nl.toString())
        nl.printStackTrace
        return None
      case _: Throwable => log.error("StreamManager:Exception while creating stream for [ %s ]", room); return None
    }
  }
  def destroyStream(room: String) {
    actorRef ! new RemoveStream(room)
  }

}
