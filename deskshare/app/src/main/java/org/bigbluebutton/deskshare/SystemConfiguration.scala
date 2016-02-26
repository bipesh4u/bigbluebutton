package org.bigbluebutton.deskshare

import com.typesafe.config.ConfigFactory

import scala.util.Try

trait SystemConfiguration {

  val config = ConfigFactory.load()

  lazy val recordingDirectory = Try(config.getString("recordingDirectory")).getOrElse("/var/bigbluebutton/deskshare")
  lazy val redisHost = Try(config.getString("redis.host")).getOrElse("127.0.0.1")
  lazy val redisPort = Try(config.getInt("redis.port")).getOrElse(6379)
  lazy val redisKeyExpiry = Try(config.getInt("redis.keyExpiry")).getOrElse(1209600)

  lazy val keyFrameInterval = Try(config.getInt("keyFrameInterval")).getOrElse(5000)
  lazy val interframeInterval = Try(config.getInt("interframeInterval")).getOrElse(200)
  lazy val waitForAllBlocks = Try(config.getBoolean("waitForAllBlocks")).getOrElse(false)

}