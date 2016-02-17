# Periodically check the status of the WebRTC call, when a call has been established attempt to hangup,
# retry if a call is in progress, send the leave voice conference message to BBB
@exitVoiceCall = (event, afterExitCall) ->
	if not Meteor.config.useSIPAudio
		leaveWebRTCVoiceConference_verto();
		cur_call = null;
		return
	else
		# To be called when the hangup is initiated
		hangupCallback = ->
			console.log "Exiting Voice Conference"

		# Checks periodically until a call is established so we can successfully end the call
		# clean state
		getInSession("triedHangup", false)
		# function to initiate call
		(checkToHangupCall = (context) ->
		# if an attempt to hang up the call is made when the current session is not yet finished, the request has no effect
		# keep track in the session if we haven't tried a hangup
		if BBB.getCallStatus() isnt null and !getInSession("triedHangup")
			console.log "Attempting to hangup on WebRTC call"
			if BBB.amIListenOnlyAudio() # notify BBB-apps we are leaving the call call if we are listen only
				Meteor.call('listenOnlyRequestToggle', BBB.getMeetingId(), BBB.getMyUserId(), BBB.getMyAuthToken(), false)
			BBB.leaveVoiceConference hangupCallback
			getInSession("triedHangup", true) # we have hung up, prevent retries
			notification_WebRTCAudioExited()
			if afterExitCall
				afterExitCall this, Meteor.config.app.listenOnly
		else
			console.log "RETRYING hangup on WebRTC call in #{Meteor.config.app.WebRTCHangupRetryInterval} ms"
			setTimeout checkToHangupCall, Meteor.config.app.WebRTCHangupRetryInterval # try again periodically
		)(@) # automatically run function
		return false

# join the conference. If listen only send the request to the server
@joinVoiceCall = (event, options) ->
	extension = Meteor.Meetings.findOne().voiceConf;
	conferenceUsername = "FreeSWITCH User"
	conferenceIdNumber = "1008";
	if !isWebRTCAvailable()
		notification_WebRTCNotSupported()
		return

	if not Meteor.config.useSIPAudio
		if options.watchOnly?
			toggleWhiteboardVideo("video")

		vertoServerCredentials = {
			vertoPort: "8082",
			hostName: Meteor.config.vertoServerAddress,
			login: "1008",
			password: Meteor.config.freeswitchProfilePassword,
		}

		wasCallSuccessful = false
		debuggerCallback = (message) ->
			console.log("CALLBACK: "+JSON.stringify(message));
			#
			# Beginning of hacky method to make Firefox media calls succeed.
			# Always fail the first time. Retry on failure.
			#
			if !!navigator.mozGetUserMedia and message.errorcode is 1001
				callIntoConference_verto(extension, conferenceUsername, conferenceIdNumber, ((m) -> console.log("CALLBACK: "+JSON.stringify(m))), "webcam", options, vertoServerCredentials)
			#
			# End of hacky method
			#
		callIntoConference_verto(extension, conferenceUsername, conferenceIdNumber, debuggerCallback, "webcam", options, vertoServerCredentials);
		return
	else
		# create voice call params
		joinCallback = (message) ->
			console.log "Beginning WebRTC Conference Call"

		notification_WebRTCAudioJoining()
		if options.isListenOnly
			Meteor.call('listenOnlyRequestToggle', BBB.getMeetingId(), BBB.getMyUserId(), BBB.getMyAuthToken(), true)

		requestedListenOnly = options.isListenOnly?
		BBB.joinVoiceConference joinCallback, requestedListenOnly # make the call #TODO should we apply role permissions to this action?

		return false