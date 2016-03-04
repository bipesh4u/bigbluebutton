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
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}

class DeskshareApplication extends MultiThreadedApplicationAdapter {

//  private val logger = Red5LoggerFactory.getLogger(classOf[DeskshareApplication], "deskshare")


//  lazy val logger = Red5LoggerFactory.getLogger(this.getClass, "deskshare")

//  val config = ConfigFactory.load()

//  var appScope: IScope = null

  def printlog(string: String):Unit = {

//    println("\n\n "+ string + " println\n\n")
//    logger.info("\n\n "+ string + "  info\n\n")
//    logger.error("\n\n  "+ string + " error\n\n")
//    logRed5.debug("RED5 log debugGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG")
  }
  override def appStart(app: IScope): Boolean = {

//    lazy val redisHost = Try(config.getString("redis.host")).getOrElse("127.iWRONGGGGG0.0.1")
//    lazy val redisPort = Try(config.getInt("redis.port")).getOrElse(6379)
//    lazy val redisPassword = Try(config.getString("redis.password")).getOrElse("")
//    lazy val keysExpiresInSec = Try(config.getInt("redis.keyExpiry")).getOrElse(14 * 86400) // 14 days
//
//    println("\n\n\n\n redis host=" + redisHost)

    implicit val system = ActorSystem("bigbluebutton-deskshare-system")
    implicit def executionContext = system.dispatcher

    val a = system.actorOf(SampleActor.props(system, true), "my-first-actor")
    a ! "hey"

    import scala.concurrent.duration._
    system.scheduler.schedule(
    (5.seconds),
    (5.seconds),
    a,
    "eee")
    printlog("deskShare appStart after")
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
