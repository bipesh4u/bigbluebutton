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
package org.bigbluebutton.deskshare.server.red5

import org.red5.server.adapter.MultiThreadedApplicationAdapter
import org.red5.server.api.{ IContext, IConnection }
import org.red5.server.api.scope.{ IScope }
import org.red5.logging.Red5LoggerFactory
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

class DeskshareApplication extends MultiThreadedApplicationAdapter {

  private val logger = Red5LoggerFactory.getLogger(classOf[DeskshareApplication])

  def printlog(string: String):Unit = {

    println("\n\n "+ string + " println\n\n")
    logger.info("\n\n "+ string + "  info\n\n")
    logger.error("\n\n  "+ string + " error\n\n")
  }

//  var appScope: IScope = null

  override def appStart(app: IScope): Boolean = {
    printlog("deskShare appStart 2")

    val config = ConfigFactory.load()
    implicit val system = ActorSystem("bigbluebutton-deskshare-system", config)
    val a = system.actorOf(SampleActor.props(system, true))
    a ! "hey"

    printlog("deskShare appStart after")
    //    appScope = app
//    super.setScope(appScope)
//    if (appScope == null) printlog("APSCOPE IS NULL!!!!")
//    else printlog("APPSCOPE is NOT NULL!!!!")
    super.appStart(app)
  }

  override def appConnect(conn: IConnection, params: Array[Object]): Boolean = {
    printlog("deskShare appConnect to scope " + conn.getScope().getContextPath());
//    var meetingId = params(0).asInstanceOf[String]
    super.appConnect(conn, params);
  }

  override def appDisconnect(conn: IConnection) {
    printlog("deskShare appDisconnect");
    super.appDisconnect(conn);
  }

  override def appStop(app: IScope) {
    printlog("Stopping deskshare")
    super.appStop(app)
  }

}
