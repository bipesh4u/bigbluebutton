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

import scala.util.{ Failure, Success }
import org.red5.server.api.Red5
import java.util.HashMap
//import org.red5.logging.Red5LoggerFactory

class DeskshareService {
//  println("\n\n DeskshareService 001 println \n\n")

//  private val log = Red5LoggerFactory.getLogger(classOf[DeskshareService], "deskshare")
//  lazy val log = Red5LoggerFactory.getLogger(this.getClass, "deskshare")

//  log.info("\n\n DeskshareService 001 info \n\n")
//  log.error("\n\n DeskshareService 001 error \n\n")

  def checkIfStreamIsPublishing(room: String): HashMap[String, Any] = {
    //		val room: String = Red5.getConnectionLocal().getScope().getName();
//    log.debug("Checking if %s is streaming.", room)

    val stream = new HashMap[String, Any]()

    return stream
  }

  def startedToViewStream(stream: String): Unit = {
//    log.debug("DeskshareService: Started viewing stream for room %s", stream)
    //    boot.sessionGateway.sendKeyFrame(stream)
  }

  def stopSharingDesktop(meetingId: String) {
//    log.debug("DeskshareService: Stop sharing for meeting [%s]", meetingId)
    //    boot.sessionGateway.stopSharingDesktop(meetingId, meetingId)
  }
}
