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
package org.bigbluebutton.deskshare.server.sessions

import akka.actor.{Props, ActorLogging, Actor}
import net.lag.logging.Logger
import org.bigbluebutton.deskshare.server.red5.DeskshareActorSystem
import scala.collection.mutable.HashMap
import org.bigbluebutton.deskshare.server.svc1.Dimension
import org.bigbluebutton.deskshare.server.stream.StreamManager
import java.awt.Point

case class CreateSession(room: String, screenDim: Dimension, blockDim: Dimension, seqNum: Int, useSVC2: Boolean)
case class RemoveSession(room: String)
case class SendKeyFrame(room: String)
case class UpdateBlock(room: String, position: Int, blockData: Array[Byte], keyframe: Boolean, seqNum: Int)
case class UpdateMouseLocation(room: String, mouseLoc:Point, seqNum: Int)
case class StopSharingDesktop(meetingId: String, stream: String)
case class IsSharingStopped(meetingId: String)
case class IsSharingStoppedReply(meetingId: String, stopped: Boolean)

class SessionManagerSVC(streamManager: StreamManager, keyFrameInterval: Int, interframeInterval: Int,
												waitForAllBlocks: Boolean, actorSystem: DeskshareActorSystem) extends Actor with ActorLogging {
//	private val log = Logger.get
 
 	private val sessions = new HashMap[String, SessionSVC]
 	private val stoppedSessions = new HashMap[String, String]

	def receive = {
		case msg: CreateSession => createSession(msg); printMailbox("CreateSession")
		case msg: RemoveSession => removeSession(msg.room); printMailbox("RemoveSession")
		case msg: SendKeyFrame => sendKeyFrame(msg.room); printMailbox("SendKeyFrame")
		case msg: UpdateBlock => updateBlock(msg.room, msg.position, msg.blockData, msg.keyframe, msg.seqNum)
		case msg: UpdateMouseLocation => updateMouseLocation(msg.room, msg.mouseLoc, msg.seqNum)
		case msg: StopSharingDesktop => handleStopSharingDesktop(msg)
		case msg: IsSharingStopped   => handleIsSharingStopped(msg)

		case msg: Any => log.warning("SessionManager: Unknown message " + msg); printMailbox("Any")
	}
 
	private def handleStopSharingDesktop(msg: StopSharingDesktop) {
    sessions.get(msg.meetingId) foreach { s =>
      stoppedSessions += msg.meetingId -> msg.stream
    }
	}
	
	private def handleIsSharingStopped(msg: IsSharingStopped) {
	  stoppedSessions.get(msg.meetingId) match {
	    case Some(s) => sender() ! new IsSharingStoppedReply(msg.meetingId, true)
	    case None    => sender() ! new IsSharingStoppedReply(msg.meetingId, false)
	  }
	}
	
	private def printMailbox(caseMethod: String) {
	  log.debug("SessionManager: mailbox message %s", caseMethod)
	}
 
	private def sendKeyFrame(room: String) {
		log.debug("SessionManager: Request to send key frame for room %s", room)
        sessions.get(room) match {
          case Some(s) =>
//						 s ! GenerateKeyFrame
						var aSessionActor = actorSystem.actorOf(Props(s), "aSessionActor")
						aSessionActor ! GenerateKeyFrame
          case None => log.warning("SessionManager: Could not find room %s", room)
        }
	}
 
	private def createSession(c: CreateSession): Unit = {
		log.debug("Creating session for %s", c.room)
		sessions.get(c.room) match {
		  case None => {
			  log.debug("SessionManager: Created session " + c.room)
			  val session: SessionSVC = new SessionSVC(this, c.room, c.screenDim, c.blockDim,
					streamManager, keyFrameInterval, interframeInterval, waitForAllBlocks, c.useSVC2, actorSystem)

			  if (session.initMe()) {
				  val old:Int = sessions.size
				  sessions += c.room -> session
//				  session.start
					var aSessionActor = actorSystem.actorOf(Props(session), "aSessionActor")
					aSessionActor ! StartSession
				  log.debug("CreateSession: Session length [%d,%d]", old, sessions.size)			    
			  } else {
			    log.error("SessionManager:Failed to create session for %s", c.room)
			  }

			}
		  case Some(s) => log.warning("SessionManager: Session already exist for %s", c.room)
		}
	}

	private def removeSession(meetingId: String): Unit = {
		log.debug("SessionManager: Removing session " + meetingId);
    	sessions.get(meetingId) foreach { s =>
				var aSessionActor = actorSystem.actorOf(Props(s), "aSessionActor")
				aSessionActor ! StopSession
				log.debug("++++ REMOVE SESSION +++%s", meetingId);
	      val old:Int = sessions.size
	      sessions -= meetingId; 
	      log.debug("RemoveSession: Session length [%d,%d]", old, sessions.size)
	      stoppedSessions.get(meetingId) foreach {ss =>
	        stoppedSessions -= meetingId  
	      }
    	}
	}
	
	private def updateMouseLocation(room: String, mouseLoc: Point, seqNum: Int): Unit = {
		sessions.get(room) match {
		  case Some(s) =>
				var aSessionActor = actorSystem.actorOf(Props(s), "aSessionActor")
				aSessionActor ! new UpdateSessionMouseLocation(mouseLoc, seqNum)
		  case None => log.warning("SessionManager: Could not update mouse loc for session %s. Does not exist.", room)
		}
	}
	
	private def updateBlock(room: String, position: Int, blockData: Array[Byte], keyframe: Boolean, seqNum: Int): Unit = {
		sessions.get(room) match {
		  case Some(s) =>
				var aSessionActor = actorSystem.actorOf(Props(s), "aSessionActor")
				aSessionActor ! new UpdateSessionBlock(position, blockData, keyframe, seqNum)
		  case None => log.warning("SessionManager: Could not update session %s. Does not exist.", room)
		}
	}
		
//	def exit() : Unit = {
//	  log.warning("SessionManager: **** Exiting Actor")
//	  context.stop(self)
//	}
//
//	def exit(reason : AnyRef) : Unit = {
//	  log.warning("SessionManager: **** Exiting SessionManager Actor %s", reason)
//		context.stop(self)
//	}
}
