# --------------------------------------------------------------------------------------------------------------------
# If a function's last line is the statement false that represents the function returning false
# A function such as a click handler will continue along with the propogation and default behaivour if not stopped
# Returning false stops propogation/prevents default. You cannot always use the event object to call these methods
# Because most Meteor event handlers set the event object to the exact context of the event which does not 
# allow you to simply call these methods.
# --------------------------------------------------------------------------------------------------------------------

@sendMessage = ->
  message = linkify $('#newMessageInput').val() # get the message from the input box
  unless (message?.length > 0 and (/\S/.test(message))) # check the message has content and it is not whitespace
    return # do nothing if invalid message

  chattingWith = getInSession('inChatWith')

  if chattingWith isnt "PUBLIC_CHAT"
    dest = Meteor.Users.findOne(_id: chattingWith)

  messageForServer = { # construct message for server
    "message": message
    "chat_type": if chattingWith is "PUBLIC_CHAT" then "PUBLIC_CHAT" else "PRIVATE_CHAT"
    "from_userid": getInSession("DBID")
    "from_username": getUsersName()
    "from_tz_offset": "240"
    "to_username": if chattingWith is "PUBLIC_CHAT" then "public_chat_username" else dest.user.name
    "to_userid": if chattingWith is "PUBLIC_CHAT" then "public_chat_userid" else chattingWith
    "from_lang": "en"
    "from_time": getTime()
    "from_color": "0x000000"
    # "from_color": "0x#{getInSession("messageColor")}"
  }

  Meteor.call "sendChatMessagetoServer", getInSession("meetingId"), messageForServer, getInSession("userId")
  $('#newMessageInput').val '' # Clear message box

Template.chatInput.events
  'click #sendMessageButton': (event) ->
    sendMessage()

  'keypress #newMessageInput': (event) -> # user pressed a button inside the chatbox
    if event.shiftKey and event.which is 13
      $("#newMessageInput").append("\r") # Change newline character
      return
    
    if event.which is 13 # Check for pressing enter to submit message
      sendMessage()
      $('#newMessageInput').val("")
      return false

Template.chatInput.rendered  = ->
   $('input[rel=tooltip]').tooltip()
   $('button[rel=tooltip]').tooltip()

# This method returns all messages for the user. It looks at the session to determine whether the user is in
#private or public chat. If true is passed, messages returned are from before the user joined. Else, the messages are from after the user joined
@getFormattedMessagesForChat = ->
  friend = chattingWith = getInSession('inChatWith') # the recipient(s) of the messages
  after = before = greeting = []

  if chattingWith is 'PUBLIC_CHAT' # find all public messages
    before = Meteor.Chat.find({'message.chat_type': chattingWith, 'message.from_time': {$lt: String(getTimeOfJoining())}}).fetch()
    after = Meteor.Chat.find({'message.chat_type': chattingWith, 'message.from_time': {$gt: String(getTimeOfJoining())}}).fetch()

    greeting = [
      'message':
        'message': getGreeting()
        'from_username': 'System',
        'from_time': getTimeOfJoining()
        'from_color': '0x3399FF' # A nice blue in hex
    ]
  else
    me = getInSession("DBID")
    after = Meteor.Chat.find({ # find all messages between current user and recipient
      'message.chat_type': 'PRIVATE_CHAT',
      $or: [{'message.from_userid': me, 'message.to_userid': friend},{'message.from_userid': friend, 'message.to_userid': me}]
    }).fetch()

  messages = (before.concat greeting).concat after

@getGreeting = ->
  "Welcome to #{window.getMeetingName()}!\r\r
  For help on using BigBlueButton see these (short) <a href='http://www.bigbluebutton.org/videos/' target='_blank'>tutorial videos</a>.\r\r
  To join the audio bridge click the headset icon (upper-left hand corner).  Use a headset to avoid causing background noise for others.\r\r\r
  This server is running BigBlueButton #{getInSession 'bbbServerVersion'}.\r\r"

Template.chatbar.helpers
  getCombinedMessagesForChat: ->
    msgs = getFormattedMessagesForChat()
    len = msgs.length # get length of messages
    i = 0
    while i < len # Must be a do while, for loop compiles and stores the length of array which can change inside the loop!
      if msgs[i].message.from_userid isnt 'System' # skip system messages
        j = i+1 # Start looking at messages right after the current one

        while j < len
          deleted = false
          if msgs[j].message.from_userid isnt 'System' # Ignore system messages
            # Check if the time discrepancy between the two messages exceeds window for grouping
            if (parseFloat(msgs[j].message.from_time)-parseFloat(msgs[i].message.from_time)) >= 60000 # 60 seconds/1 minute
              break # Messages are too far between, so them seperated and stop joining here

            if msgs[i].message.from_userid is msgs[j].message.from_userid # Both messages are from the same user
              msgs[i].message.message += "\r#{msgs[j].message.message}" # Combine the messages
              msgs.splice(j,1) # Delete the message from the collection
              deleted = true
            else break # Messages are from different people, move on
            #
          else break # This is the break point in the chat, don't merge
          #
          len = msgs.length
          ++j if not deleted
      #
      ++i
      len = msgs.length

    msgs

@detectUnreadChat = ->
  #if the current tab is not the same as the tab we just published in
  Meteor.Chat.find({}).observe({
    added: (chatMessage) =>
      findDestinationTab = ->
        if chatMessage.message?.chat_type is "PUBLIC_CHAT"
          "PUBLIC_CHAT"
        else
          chatMessage.message?.from_userid

      populateChatTabs() # check if we need to open a new tab
      destinationTab = findDestinationTab()
      if destinationTab isnt getInSession "inChatWith"
        chatTabs.update({userId: destinationTab}, {$set: {gotMail: true}})
    })

# When chatbar gets rendered, scroll to the bottom
Template.chatbar.rendered = ->
  detectUnreadChat()
  $('#chatbody').scrollTop($('#chatbody')[0]?.scrollHeight)
  false
# Scrolls the message container to the bottom. The number of pixels to scroll down is the height of the container
Handlebars.registerHelper "autoscroll", ->
  $('#chatbody').scrollTop($('#chatbody')[0]?.scrollHeight)
  false

Template.optionsBar.events
  'click .private-chat-user-entry': (event) -> # clicked a user's name to begin private chat
    setInSession 'display_chatPane', true
    setInSession "inChatWith", @_id

Template.optionsBar.helpers
  thereArePeopletoChatWith: -> # Subtract 1 for the current user. Returns whether there are other people in the chat
    # TODO: Add a check for the count to only include users who are allowed to private chat
    (Meteor.Users.find({'meetingId': getInSession("meetingId")}).count()-1) >= 1

Template.optionsBar.rendered = ->
  $('div[rel=tooltip]').tooltip()

Template.optionsFontSize.events
  "click .fontSizeSelector": (event) ->
    selectedFontSize = parseInt(event.target.id)
    if selectedFontSize
      setInSession "messageFontSize", selectedFontSize
    else setInSession "messageFontSize", 12

Template.tabButtons.events
  'click .close': (event) -> # user closes private chat
    setInSession 'inChatWith', 'PUBLIC_CHAT'
    setInSession 'display_chatPane', true
    console.log "userId: #{@userId}"
    id = chatTabs.findOne({userId: @userId})
    if id?
      chatTabs.remove(id)

    Meteor.call("deletePrivateChatMessages", getInSession("userId"), @userId)

    return false # stops propogation/prevents default

  'click .optionsChatTab': (event) ->
    setInSession 'display_chatPane', false

  'click .privateChatTab': (event) ->
    setInSession 'display_chatPane', true

  'click .publicChatTab': (event) ->
    setInSession 'display_chatPane', true

  'click .tab': (event) -> 
    setInSession "inChatWith", @userId

  'click .gotUnreadMail': (event) ->
    chatTabs.update({userId: @userId}, {$set: {gotMail: false}})

Template.tabButtons.helpers
	makeTabButton: -> # create tab button for private chat or other such as options
		safeClass = @class.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
		safeName = @name.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

		button = ''
		button += '<li class=\"'
		button += 'active ' if getInSession("inChatWith") is @userId
		button += 'gotUnreadMail ' if @gotMail and getInSession("displayChatNotifications")
		button += "tab #{safeClass}"
		button += '\">'
		button += "<a href='#' data-toggle='tab' id=\"#{safeName}\">"
		button += "<button class=\"close closeTab\" type=\"button\"><sup><b>X</b></sup></button> " if @class is 'privateChatTab'
		button += "#{safeName}"
		button += '</a>'
		button += '</li>'
		button

@activateBreakLines = (str) ->
  if typeof str is 'string'
    res = str.replace /\\n/gim, '<br/>'
    res = res.replace /\r/gim, '<br/>'

# make links received from Flash client clickable in HTML
@toClickable = (str) ->
  if typeof str is 'string'
    res = str.replace /<a href='event:/gim, "<a target='_blank' href='"
    res = res.replace /<a href="event:/gim, '<a target="_blank" href="'

Template.message.helpers
  toClockTime: (epochTime) ->
    if epochTime is null
      return ""
    local = new Date()
    offset = local.getTimezoneOffset()
    epochTime = epochTime - offset * 60000 # 1 min = 60 s = 60,000 ms
    dateObj = new Date(epochTime)
    hours = dateObj.getUTCHours()
    minutes = dateObj.getUTCMinutes()
    if minutes < 10
      minutes = "0" + minutes
    hours + ":" + minutes

  sanitizeAndFormat: (str) ->
    if typeof str is 'string'
      # First, replace replace all tags with the ascii equivalent (excluding those involved in anchor tags)
      res = str.replace(/&/g, '&amp;').replace(/<(?![au\/])/g, '&lt;').replace(/\/([^au])>/g, '$1&gt;').replace(/([^=])"(?!>)/g, '$1&quot;');
      res = toClickable res
      res = activateBreakLines res

Template.notificationSettings.events
	"click #chatNotificationOn": (event) ->
		console.log "on"
		setInSession "displayChatNotifications", true

	"click #chatNotificationOff": (event) ->
		console.log "off"
		setInSession "displayChatNotifications", false
