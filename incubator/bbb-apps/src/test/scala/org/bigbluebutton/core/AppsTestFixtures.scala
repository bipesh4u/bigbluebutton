package org.bigbluebutton.core

trait AppsTestFixtures {

  val meetingId = "testMeetingId"
  val externalMeetingId = "testExternalMeetingId"
  val meetingName = "test meeting"
  val record = false
  val voiceConfId = "85115"
  val durationInMinutes = 10
  val autoStartRecording = false
  val allowStartStopRecording = false
  val moderatorPassword = "modpass"
  val viewerPassword = "viewpass"
  val createTime = System.currentTimeMillis
  val createDate = "Oct 26, 2015"
  val isBreakout = false
  val red5DeskShareIP = "red5IP"
  val red5DeskShareApp = "screenshare"

  val mProps = new MeetingProperties(meetingId, externalMeetingId,
    meetingName, record,
    voiceConfId,
    durationInMinutes,
    autoStartRecording, allowStartStopRecording,
    moderatorPassword, viewerPassword,
    createTime, createDate,
    red5DeskShareIP,
    red5DeskShareApp,
    isBreakout)

}