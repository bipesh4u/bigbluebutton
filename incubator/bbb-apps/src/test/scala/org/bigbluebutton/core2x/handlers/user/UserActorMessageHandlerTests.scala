package org.bigbluebutton.core2x.handlers.user

import org.bigbluebutton.core.{ OutMessageGateway, UnitSpec }
import org.bigbluebutton.core2x.MeetingTestFixtures
import org.bigbluebutton.core2x.api.IncomingMsg._
import org.bigbluebutton.core2x.api.OutGoingMsg._
import org.bigbluebutton.core2x.domain.Clients
import org.bigbluebutton.core2x.models.{ MeetingStateModel, MeetingStatus, RegisteredUsersModel, UsersModel }
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

class UserActorMessageHandlerTests extends UnitSpec with MockitoSugar with MeetingTestFixtures {
  it should "eject user if user has ability" in {

    val testRegUsers = new RegisteredUsersModel
    testRegUsers.add(richardRegisteredUser)
    testRegUsers.add(fredRegisteredUser)
    testRegUsers.add(antonRegisteredUser)

    val testUsers = new UsersModel
    testUsers.save(richardUser)
    testUsers.save(fredUser)
    testUsers.save(antonUser)

    val clients = new Clients

    val state: MeetingStateModel = new MeetingStateModel(bbbDevProps, abilities, clients, testRegUsers,
      testUsers, chats, layouts, polls,
      whiteboards, presentations,
      breakoutRooms, captions,
      new MeetingStatus)

    val mockOutGW = mock[OutMessageGateway]
    // Create the class under test and pass the mock to it
    val classUnderTest = new UserActorMessageHandler(richardRegisteredUser, mockOutGW)

    val ejectUserMsg = new EjectUserFromMeetingInMsg(bbbDevIntMeetingId, antonIntUserId, richardIntUserId)

    // Use the class under test
    classUnderTest.handleEjectUserFromMeeting(ejectUserMsg, state)

    // Then verify the class under test used the mock object as expected
    // The disconnect user shouldn't be called as user has ability to eject another user
    verify(mockOutGW, never()).send(new DisconnectUser2x(ejectUserMsg.meetingId, ejectUserMsg.ejectedBy))

  }
}