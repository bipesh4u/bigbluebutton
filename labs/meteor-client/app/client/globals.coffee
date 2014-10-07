# Convert a color `value` as integer to a hex color (e.g. 255 to #0000ff)
@colourToHex = (value) ->
	hex = parseInt(value).toString(16)
	hex = "0" + hex while hex.length < 6
	"##{hex}"

# retrieve account for selected user
@getCurrentUserFromSession = ->
  Meteor.Users.findOne("userId": getInSession("userId"))

@getInSession = (k) -> SessionAmplify.get k

@getMeetingName = ->
  meetName = getInSession("meetingName") # check if we actually have one in the session
  if meetName? then meetName # great return it, no database query
  else # we need it from the database
    meet = Meteor.Meetings.findOne({})
    if meet?.meetingName
      setInSession "meetingName", meet?.meetingName # store in session for fast access next time
      meet?.meetingName
    else "your meeting"

@getTime = -> # returns epoch in ms
  (new Date).valueOf()

@getTimeOfJoining = ->
  Meteor.Users.findOne(_id: getInSession "DBID")?.user?.time_of_joining

@getUsersName = ->
  name = getInSession("userName") # check if we actually have one in the session
  if name? then name # great return it, no database query
  else # we need it from the database
    user = Meteor.Users.findOne({'userId': getInSession("userId")})
    if user?.user?.name
      setInSession "userName", user.user.name # store in session for fast access next time
      user.user.name
    else null
        
@getPresentationFilename = ->
  currentPresentation = Meteor.Presentations.findOne({"presentation.current": true})
  currentPresentation?.presentation?.name
        
Handlebars.registerHelper "colourToHex", (value) =>
	@window.colourToHex(value)

Handlebars.registerHelper 'equals', (a, b) -> # equals operator was dropped in Meteor's migration from Handlebars to Spacebars
  a is b

Handlebars.registerHelper "getCurrentMeeting", ->
	Meteor.Meetings.findOne()

Handlebars.registerHelper "getCurrentSlide", ->
  currentPresentation = Meteor.Presentations.findOne({"presentation.current": true})
  presentationId = currentPresentation?.presentation?.id
  Meteor.Slides.find({"presentationId": presentationId, "slide.current": true})

# retrieve account for selected user
Handlebars.registerHelper "getCurrentUser", =>
  @window.getCurrentUserFromSession()

Handlebars.registerHelper "getHandRaiseStats", ->
	numRaised = Meteor.Users.find({'user.raise_hand':true}).fetch().length
	total = Meteor.Users.find().fetch().length
	percentageRaised = numRaised/total
	if numRaised > 0
		"#{numRaised} Hands Raised (#{percentageRaised}% of Attendees)"
	else false

# Allow access through all templates
Handlebars.registerHelper "getInSession", (k) -> SessionAmplify.get k

Handlebars.registerHelper "getMeetingName", ->
  window.getMeetingName()
    
Handlebars.registerHelper "getWhiteboardTitle", ->
  "Whiteboard: " + getPresentationFilename()

Handlebars.registerHelper "getShapesForSlide", ->
  currentSlide = getCurrentSlideDoc()
    
  # try to reuse the lines above
  Meteor.Shapes.find({whiteboardId: currentSlide?.slide?.id})

# retrieves all users in the meeting
Handlebars.registerHelper "getUsersInMeeting", ->
  Meteor.Users.find({})

Handlebars.registerHelper "isCurrentUser", (_id) ->
  _id is getInSession("DBID")

Handlebars.registerHelper "meetingIsRecording", ->
	Meteor.Meetings.findOne()?.recorded # Should only ever have one meeting, so we dont need any filter and can trust result #1

Handlebars.registerHelper "isCurrentUserMuted", ->
	getInSession "isMuted"

Handlebars.registerHelper "isCurrentUserRaisingHand", ->
	user = Meteor.Users.findOne({_id:getInSession("DBID")})
	user?.user?.raise_hand

Handlebars.registerHelper "isCurrentUserSharingAudio", ->
	user = Meteor.Users.findOne({userId:getInSession("userId")})
	user?.user?.voiceUser?.joined

Handlebars.registerHelper "isCurrentUserSharingVideo", ->
	user = Meteor.Users.findOne({userId:getInSession("userId")})
	user?.user?.webcam_stream?.length isnt 0

Handlebars.registerHelper "isCurrentUserTalking", ->
	user = Meteor.Users.findOne({userId:getInSession("userId")})
	user?.user?.voiceUser?.talking

Handlebars.registerHelper "isUserSharingAudio", (u) ->
  if u? 
    user = Meteor.Users.findOne({userId:u.userid})
    user?.user?.voiceUser?.joined
  else return false

Handlebars.registerHelper "isUserListenOnly", (u) ->
  if u?
    user = Meteor.Users.findOne({userId:u.userid})
    user?.user?.listenOnly
  else return false

Handlebars.registerHelper "isUserSharingVideo", (u) ->
  u.webcam_stream?.length isnt 0

Handlebars.registerHelper "isUserTalking", (u) ->
  if u? 
    user = Meteor.Users.findOne({userId:u.userid})
    user.user?.voiceUser?.talking
  else return false

Handlebars.registerHelper "isUserMuted", (u) ->
  if u? 
    user = Meteor.Users.findOne({userId:u.userid})
    user.user.voiceUser?.muted
  else return false

Handlebars.registerHelper "messageFontSize", ->
	style: "font-size: #{getInSession("messageFontSize")}px;"

Handlebars.registerHelper "pointerLocation", ->
  currentPresentation = Meteor.Presentations.findOne({"presentation.current": true})
  currentPresentation?.pointer

Handlebars.registerHelper "setInSession", (k, v) -> SessionAmplify.set k, v 

Handlebars.registerHelper "visibility", (section) ->
    if getInSession "display_#{section}"
        style: 'display:block'
    else
        style: 'display:none'

Handlebars.registerHelper "getChatbarTabs1", ->
    chatTabs.find().fetch()

# transform plain text links into HTML tags compatible with Flash client
@linkify = (str) ->
  www = /(^|[^\/])(www\.[\S]+($|\b))/img
  http = /\b(https?:\/\/[0-9a-z+|.,:;\/&?_~%#=@!-]*[0-9a-z+|\/&_~%#=@-])/img
  str = str.replace http, "<a href='event:$1'><u>$1</u></a>"
  str = str.replace www, "$1<a href='event:http://$2'><u>$2</u></a>"

@setInSession = (k, v) -> SessionAmplify.set k, v 

Meteor.methods
  sendMeetingInfoToClient: (meetingId, userId) ->
    setInSession("userId", userId)
    setInSession("meetingId", meetingId)
    setInSession("currentChatId", meetingId)
    setInSession("meetingName", null)
    setInSession("bbbServerVersion", Meteor.config.bbbServerVersion)
    setInSession("dateOfBuild", Meteor.config.dateOfBuild)
    setInSession("userName", null) 

@toggleCam = (event) ->
  # Meteor.Users.update {_id: context._id} , {$set:{"user.sharingVideo": !context.sharingVideo}}
  # Meteor.call('userToggleCam', context._id, !context.sharingVideo)

@toggleChatbar = ->
  setInSession "display_chatbar", !getInSession "display_chatbar"

@toggleMic = (event) ->
  if getInSession "isSharingAudio" # only allow muting/unmuting if they are in the call
    u = Meteor.Users.findOne({userId:getInSession("userId")})
    if u?
      # format: meetingId, userId, requesterId, mutedBoolean
      # TODO: insert the requesterId - the user who requested the muting of userId (might be a moderator)
      Meteor.call('publishMuteRequest', u.meetingId, u.userId, u.userId, not u.user.voiceUser.muted)
      setInSession "isMuted", not u.user.voiceUser.muted

@toggleNavbar = ->
  setInSession "display_navbar", !getInSession "display_navbar"

# toggle state of session variable
@toggleUsersList = ->
  setInSession "display_usersList", !getInSession "display_usersList"

@toggleVoiceCall = (event) -> 
	if getInSession "isSharingAudio"
		hangupCallback = -> 
			console.log "left voice conference"
			# sometimes we can hangup before the message that the user stopped talking is received so lets set it manually, otherwise they might leave the audio call but still be registered as talking
			Meteor.call("userStopAudio", getInSession("meetingId"),getInSession("userId"))
			setInSession "isSharingAudio", false # update to no longer sharing
		webrtc_hangup hangupCallback # sign out of call
	else
		# create voice call params
		username = "#{getInSession("userId")}-bbbID-#{getUsersName()}"
		# voicePin = Meteor.Meetings.findOne()?.voiceConf
		# voiceBridge = if voicePin? then voicePin else "0"
		voiceBridge = Meteor.Meetings.findOne({}).voiceConf # need to know this info for all meetings #TODO
		server = null
		joinCallback = (message) -> 
			console.log JSON.stringify message
			Meteor.call("userShareAudio", getInSession("meetingId"),getInSession("userId"))
			console.log "joined audio call"
			console.log Meteor.Users.findOne(userId:getInSession("userId"))
			setInSession "isSharingAudio", true
		webrtc_call(username, voiceBridge, server, joinCallback) # make the call
	return false

@toggleWhiteBoard = ->
  setInSession "display_whiteboard", !getInSession "display_whiteboard"

# Starts the entire logout procedure.
# meeting: the meeting the user is in
# the user's userId
@userLogout = (meeting, user) ->
  Meteor.call("userLogout", meeting, user)

  # Clear the local user session and redirect them away
  setInSession("userId", null)
  setInSession("meetingId", null)
  setInSession("currentChatId", null)
  setInSession("meetingName", null)
  setInSession("bbbServerVersion", null)
  setInSession("userName", null) 
  setInSession "display_navbar", false # needed to hide navbar when the layout template renders
  
  Router.go('logout') # navigate to logout

# color can be a number (a hex converted to int) or a string (e.g. "#ffff00")
@formatColor = (color) ->
  color ?= "0" # default value
  if !color.toString().match(/\#.*/)
    color = colourToHex(color)
  color

# thickness can be a number (e.g. "2") or a string (e.g. "2px")
@formatThickness = (thickness) ->
  thickness ?= "1" # default value
  if !thickness.toString().match(/.*px$/)
    "#" + thickness + "px" # leading "#" - to be compatible with Firefox
  thickness
    
# applies zooming to the stroke thickness
@zoomStroke = (thickness) ->
  currentSlide = @getCurrentSlideDoc()
  ratio = (currentSlide?.slide.width_ratio + currentSlide?.slide.height_ratio) / 2
  thickness * 100 / ratio

@getCurrentSlideDoc = -> # returns only one document
  currentPresentation = Meteor.Presentations.findOne({"presentation.current": true})
  presentationId = currentPresentation?.presentation?.id
  currentSlide = Meteor.Slides.findOne({"presentationId": presentationId, "slide.current": true})


#start a clientside-only collection keeping track of the chat tabs
@chatTabs = new Meteor.Collection(null)
#insert the basic tabs
@chatTabs.insert({ userId: "PUBLIC_CHAT", name: "Public", gotMail: false, class: "publicChatTab"})
@chatTabs.insert({ userId: "OPTIONS", name: "Options", gotMail: false, class: "optionsChatTab"})

#check the chat history of the user and add tabs for the private chats
@populateChatTabs = ->
  me = getInSession("userId")
  users = Meteor.Users.find().fetch()
  myPrivateChats = Meteor.Chat.find({$or: [{'message.from_userid': me, 'message.chat_type': 'PRIVATE_CHAT'},{'message.to_userid': me, 'message.chat_type': 'PRIVATE_CHAT'}] }).fetch()

  uniqueArray = []
  for chat in myPrivateChats
    if chat.message.to_userid is me
      uniqueArray.push({userId: chat.message.from_userid, username: chat.message.from_username})
    if chat.message.from_userid is me
      uniqueArray.push({userId: chat.message.to_userid, username: chat.message.to_username})

  #keep unique entries only
  uniqueArray = uniqueArray.filter((itm, i, a) ->
      i is a.indexOf(itm)
    )
  #insert the unique entries in the collection
  for u in uniqueArray
    unless chatTabs.findOne({userId: u.userId})?
      chatTabs.insert({ userId: u.userId, name: u.username, gotMail: false, class: "privateChatTab"})

Handlebars.registerHelper "grabChatTabs", ->
  chatTabs.find().fetch()