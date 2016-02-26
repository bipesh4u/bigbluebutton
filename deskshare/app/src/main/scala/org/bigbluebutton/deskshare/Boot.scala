package org.bigbluebutton.deskshare

import akka.actor.ActorSystem
import org.bigbluebutton.deskshare.server.recorder.{ FileRecordingServiceImp, EventRecorder }
import org.bigbluebutton.deskshare.server.red5.DeskshareApplication
import org.bigbluebutton.deskshare.server.sessions.SessionManagerGateway
import org.bigbluebutton.deskshare.server.socket.DeskShareServer
import org.bigbluebutton.deskshare.server.stream.StreamManager

class Boot extends SystemConfiguration {
  implicit val system = ActorSystem("bigbluebutton-deskshare-system")

  val redisRecorder = new EventRecorder(redisHost, redisPort, redisKeyExpiry)
  val recordingService = new FileRecordingServiceImp()
  recordingService.setRedisDispatcher(redisRecorder)
  recordingService.setRecordingDirectory(recordingDirectory)

  val deskShareServer = new DeskShareServer
  val deskShareApp = new DeskshareApplication(system, deskShareServer)
  implicit val streamManager = system.actorOf(StreamManager.props(system, deskShareApp, true, recordingService), "streamManager")

  val sessionGateway = new SessionManagerGateway(system, streamManager, keyFrameInterval, interframeInterval, waitForAllBlocks)
}
