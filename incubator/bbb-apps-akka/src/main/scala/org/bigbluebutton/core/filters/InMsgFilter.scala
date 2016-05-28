package org.bigbluebutton.core.filters

import org.bigbluebutton.core.api._

trait InMsgFilter {
  def handleMonitorNumberOfWebUsers(msg: MonitorNumberOfUsers)
  def handleValidateAuthToken(msg: ValidateAuthToken)
  def handleRegisterUser(msg: RegisterUser)
  def handleUserJoinedVoiceConfMessage(msg: UserJoinedVoiceConfMessage)
  def handleUserLeftVoiceConfMessage(msg: UserLeftVoiceConfMessage)
  def handleUserMutedInVoiceConfMessage(msg: UserMutedInVoiceConfMessage)
  def handleUserTalkingInVoiceConfMessage(msg: UserTalkingInVoiceConfMessage)
  def handleVoiceConfRecordingStartedMessage(msg: VoiceConfRecordingStartedMessage)
  def handleUserJoin(msg: UserJoining)
  def handleUserLeft(msg: UserLeaving)
  def handleAssignPresenter(msg: AssignPresenter)
  def handleGetUsers(msg: GetUsers)
  def handleChangeUserStatus(msg: ChangeUserStatus)
  def handleEjectUserFromMeeting(msg: EjectUserFromMeeting)
  def handleUserEmojiStatus(msg: UserEmojiStatus)
  def handleUserShareWebcam(msg: UserShareWebcam)
  def handleUserunshareWebcam(msg: UserUnshareWebcam)
  def handleMuteMeetingRequest(msg: MuteMeetingRequest)
  def handleMuteAllExceptPresenterRequest(msg: MuteAllExceptPresenterRequest)
  def handleIsMeetingMutedRequest(msg: IsMeetingMutedRequest)
  def handleMuteUserRequest(msg: MuteUserRequest)
  def handleEjectUserRequest(msg: EjectUserFromMeeting)
  def handleTransferUserToMeeting(msg: TransferUserToMeeting)
  def handleSetLockSettings(msg: SetLockSettings)
  def handleGetLockSettings(msg: GetLockSettings)
  def handleLockUserRequest(msg: LockUserRequest)
  def handleInitLockSettings(msg: InitLockSettings)
  def handleInitAudioSettings(msg: InitAudioSettings)
  def handleGetChatHistoryRequest(msg: GetChatHistoryRequest)
  def handleSendPublicMessageRequest(msg: SendPublicMessageRequest)
  def handleSendPrivateMessageRequest(msg: SendPrivateMessageRequest)
  def handleUserConnectedToGlobalAudio(msg: UserConnectedToGlobalAudio)
  def handleUserDisconnectedFromGlobalAudio(msg: UserDisconnectedFromGlobalAudio)
  def handleGetCurrentLayoutRequest(msg: GetCurrentLayoutRequest)
  def handleBroadcastLayoutRequest(msg: BroadcastLayoutRequest)
  def handleInitializeMeeting(msg: InitializeMeeting)
  def handleClearPresentation(msg: ClearPresentation)
  def handlePresentationConversionUpdate(msg: PresentationConversionUpdate)
  def handlePresentationPageCountError(msg: PresentationPageCountError)
  def handlePresentationSlideGenerated(msg: PresentationSlideGenerated)
  def handlePresentationConversionCompleted(msg: PresentationConversionCompleted)
  def handleRemovePresentation(msg: RemovePresentation)
  def handleGetPresentationInfo(msg: GetPresentationInfo)
  def handleSendCursorUpdate(msg: SendCursorUpdate)
  def handleResizeAndMoveSlide(msg: ResizeAndMoveSlide)
  def handleGotoSlide(msg: GotoSlide)
  def handleSharePresentation(msg: SharePresentation)
  def handleGetSlideInfo(msg: GetSlideInfo)
  def handlePreuploadedPresentations(msg: PreuploadedPresentations)
  def handleSendWhiteboardAnnotationRequest(msg: SendWhiteboardAnnotationRequest)
  def handleGetWhiteboardShapesRequest(msg: GetWhiteboardShapesRequest)
  def handleClearWhiteboardRequest(msg: ClearWhiteboardRequest)
  def handleUndoWhiteboardRequest(msg: UndoWhiteboardRequest)
  def handleEnableWhiteboardRequest(msg: EnableWhiteboardRequest)
  def handleIsWhiteboardEnabledRequest(msg: IsWhiteboardEnabledRequest)
  def handleSetRecordingStatus(msg: SetRecordingStatus)
  def handleGetRecordingStatus(msg: GetRecordingStatus)
  def handleStartCustomPollRequest(msg: StartCustomPollRequest)
  def handleStartPollRequest(msg: StartPollRequest)
  def handleStopPollRequest(msg: StopPollRequest)
  def handleShowPollResultRequest(msg: ShowPollResultRequest)
  def handleHidePollResultRequest(msg: HidePollResultRequest)
  def handleRespondToPollRequest(msg: RespondToPollRequest)
  def handleGetPollRequest(msg: GetPollRequest)
  def handleGetCurrentPollRequest(msg: GetCurrentPollRequest)
  // Breakout rooms
  def handleBreakoutRoomsList(msg: BreakoutRoomsListMessage)
  def handleCreateBreakoutRooms(msg: CreateBreakoutRooms)
  def handleBreakoutRoomCreated(msg: BreakoutRoomCreated)
  def handleBreakoutRoomEnded(msg: BreakoutRoomEnded)
  def handleRequestBreakoutJoinURL(msg: RequestBreakoutJoinURLInMessage)
  def handleBreakoutRoomUsersUpdate(msg: BreakoutRoomUsersUpdate)
  def handleSendBreakoutUsersUpdate(msg: SendBreakoutUsersUpdate)
  def handleEndAllBreakoutRooms(msg: EndAllBreakoutRooms)

  def handleExtendMeetingDuration(msg: ExtendMeetingDuration)
  def handleSendTimeRemainingUpdate(msg: SendTimeRemainingUpdate)
  def handleEndMeeting(msg: EndMeeting)

  // Closed Caption
  def handleSendCaptionHistoryRequest(msg: SendCaptionHistoryRequest)
  def handleUpdateCaptionOwnerRequest(msg: UpdateCaptionOwnerRequest)
  def handleEditCaptionHistoryRequest(msg: EditCaptionHistoryRequest)
}