Template.displayUserIcons.events
  'click .raisedHandIcon': (event) ->
    # the function to call 'userLowerHand'
    # the meeting id
    # the _id of the person whose land is to be lowered
    # the userId of the person who is lowering the hand
    console.log "lower hand- client click handler"
    Meteor.call('userLowerHand', getInSession("meetingId"), @_id, getInSession("userId"), getInSession("DBID") )

  'click .muteIcon': (event) ->
    toggleMic @