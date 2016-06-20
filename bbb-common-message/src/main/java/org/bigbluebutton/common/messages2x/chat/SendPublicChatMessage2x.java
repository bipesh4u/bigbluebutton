package org.bigbluebutton.common.messages2x.chat;

import org.bigbluebutton.common.messages2x.AbstractEventMessage;
import org.bigbluebutton.common.messages2x.objects.ChatMessage;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

public class SendPublicChatMessage2x extends AbstractEventMessage {

    public static final String SEND_PUBLIC_CHAT_MESSAGE = "SendPublicChatMessage";
    public final Payload payload;

    public SendPublicChatMessage2x(String meetingID, String requesterID, ChatMessage chatMessage) {
        super();
        header.name = SEND_PUBLIC_CHAT_MESSAGE;

        this.payload = new Payload();
        payload.meetingID = meetingID;
        payload.requesterID = requesterID;
        payload.message = chatMessage;
    }

    public static SendPublicChatMessage2x fromJson(String message) {
        ObjectMapper mapper = JsonFactory.create();
        return mapper.readValue(message, SendPublicChatMessage2x.class);
    }

    public class Payload {
        public ChatMessage message;
        public String meetingID;
        public String requesterID;
    }
}
