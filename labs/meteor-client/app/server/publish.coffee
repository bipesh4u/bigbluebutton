# Publish only the users that are in the particular meetingId
# On the client side we pass the meetingId parameter
Meteor.publish 'users', (meetingId) ->
  Meteor.Users.find({meetingId: meetingId}, {fields: { 'userId': 0, 'user.userid': 0, 'user.extern_userid': 0, 'user.voiceUser.userid': 0, 'user.voiceUser.web_userid': 0 }})

Meteor.publish 'chat', (meetingId, me) ->
  Meteor.Chat.find({$or: [ {'message.chat_type': 'PUBLIC_CHAT', 'meetingId': meetingId},{'message.from_userid': me, 'meetingId': meetingId},{'message.to_userid': me, 'meetingId': meetingId}] })

Meteor.publish 'shapes', (meetingId) ->
  Meteor.Shapes.find({meetingId: meetingId})

Meteor.publish 'slides', (meetingId) ->
  Meteor.Slides.find({meetingId: meetingId})

Meteor.publish 'meetings', (meetingId) ->
  Meteor.Meetings.find({meetingId: meetingId})

Meteor.publish 'presentations', (meetingId) ->
  Meteor.Presentations.find({meetingId: meetingId})

# Clear all data in subcriptions
@clearCollections = ->
    Meteor.Users.remove({})
    console.log "cleared Users Collection!"
    Meteor.Chat.remove({})
    console.log "cleared Chat Collection!"
    Meteor.Meetings.remove({})
    console.log "cleared Meetings Collection!"
    Meteor.Shapes.remove({})
    console.log "cleared Shapes Collection!"
    Meteor.Slides.remove({})
    console.log "cleared Slides Collection!"
    Meteor.Presentations.remove({})
    console.log "cleared Presentations Collection!"