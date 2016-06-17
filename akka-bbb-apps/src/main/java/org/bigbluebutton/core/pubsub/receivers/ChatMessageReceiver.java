package org.bigbluebutton.core.pubsub.receivers;

import org.bigbluebutton.common.messages.GetChatHistoryRequestMessage;
import org.bigbluebutton.common.messages.MessagingConstants;
import org.bigbluebutton.common.messages.SendPrivateChatMessage;
import org.bigbluebutton.common.messages.SendPublicChatMessage;
import org.bigbluebutton.common.messages2x.chat.SendPublicChatMessage2x;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import org.bigbluebutton.core.api.IBigBlueButtonInGW;

public class ChatMessageReceiver implements MessageHandler{

	private IBigBlueButtonInGW bbbGW;
	
	public ChatMessageReceiver(IBigBlueButtonInGW bbbGW) {
		this.bbbGW = bbbGW;
	}

	@Override
	public void handleMessage(String pattern, String channel, String message) {
		if (channel.equalsIgnoreCase(MessagingConstants.TO_CHAT_CHANNEL)) {
			JsonParser parser = new JsonParser();
			JsonObject obj = (JsonObject) parser.parse(message);
			if (obj.has("header") && obj.has("payload")) {
				JsonObject header = (JsonObject) obj.get("header");
				if (header.has("name")) {
					String messageName = header.get("name").getAsString();
					if (GetChatHistoryRequestMessage.GET_CHAT_HISTORY_REQUEST.equals(messageName)) {
						GetChatHistoryRequestMessage msg = GetChatHistoryRequestMessage.fromJson(message);
						bbbGW.getChatHistory(msg.meetingId, msg.requesterId, msg.replyTo);
					} else if (SendPublicChatMessage.SEND_PUBLIC_CHAT_MESSAGE.equals(messageName)){
						// System.out.println("___SendPublicChatMessage (1x) " + message);
						SendPublicChatMessage msg = SendPublicChatMessage.fromJson(message);
						bbbGW.sendPublicMessage(msg.meetingId, msg.requesterId, msg.messageInfo);
					} else if (SendPublicChatMessage2x.SEND_PUBLIC_CHAT_MESSAGE.equals(messageName)){
						// System.out.println("___SendPublicChatMessage2x " + message);
						SendPublicChatMessage2x msg = SendPublicChatMessage2x.fromJson(message);
						// msg.payload.message.message = "2x:" + msg.payload.message.message;
						// System.out.println("^^^SendPublicChatMessage2x toJSON " + msg.toJson());
						bbbGW.sendPublicMessage2x(msg.payload.meetingID, msg.payload.requesterID,
								msg.payload.message);
					} else if (SendPrivateChatMessage.SEND_PRIVATE_CHAT_MESSAGE.equals(messageName)){
						SendPrivateChatMessage msg = SendPrivateChatMessage.fromJson(message);
						bbbGW.sendPrivateMessage(msg.meetingId, msg.requesterId, msg.messageInfo);
					}
				}
			}
		}
	}
}
