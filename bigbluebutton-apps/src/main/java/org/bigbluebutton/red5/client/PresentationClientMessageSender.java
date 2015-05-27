package org.bigbluebutton.red5.client;

import java.util.HashMap;
import java.util.Map;

import org.bigbluebutton.conference.meeting.messaging.red5.BroadcastClientMessage;
import org.bigbluebutton.conference.meeting.messaging.red5.ConnectionInvokerService;
import org.bigbluebutton.conference.meeting.messaging.red5.DirectClientMessage;
import org.bigbluebutton.red5.pub.messages.GetPresentationInfoReplyMessage;
import org.bigbluebutton.red5.pubsub.messages.GoToSlideReplyMessage;
import org.bigbluebutton.red5.pubsub.messages.PresentationConversionProgressMessage;
import org.bigbluebutton.red5.sub.messages.PresentationRemovedMessage;
import org.bigbluebutton.red5.pub.messages.Constants;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PresentationClientMessageSender {
	private ConnectionInvokerService service;
	
	public PresentationClientMessageSender(ConnectionInvokerService service) {
		this.service = service;
	}
	
	public void handlePresentationMessage(String message) {
		JsonParser parser = new JsonParser();
		JsonObject obj = (JsonObject) parser.parse(message);
		
		if (obj.has("header") && obj.has("payload")) {
			JsonObject header = (JsonObject) obj.get("header");

			if (header.has("name")) {
				String messageName = header.get("name").getAsString();
				switch (messageName) {
				  case PresentationRemovedMessage.PRESENTATION_REMOVED:
					  processPresentationRemovedMessage(message);
					  break;
				  case GetPresentationInfoReplyMessage.GET_PRESENTATION_INFO_REPLY:
					  processGetPresentationInfoReplyMessage(message);
					  break;
				  case GoToSlideReplyMessage.GO_TO_SLIDE_REPLY:
					  processGoToSlideMessage(message);
					  break;
				  case PresentationConversionProgressMessage.PRESENTATION_CONVERSION_PROGRESS:
					  processPresentationConversionProgress(message);
					  break;
				}
			}
		}
	}

	private void processPresentationConversionProgress(String json) {
		PresentationConversionProgressMessage msg = PresentationConversionProgressMessage.fromJson(json);
		if (msg != null) {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("presentationID", msg.presentationId);
			args.put("meetingID", msg.meetingId);
			args.put("code", msg.code);
			args.put("messageKey", msg.messageKey);
			args.put("presentationName", msg.presentationName);

			Map<String, Object> message = new HashMap<String, Object>();
			Gson gson = new Gson();
			message.put("msg", gson.toJson(args));

			System.out.println("RedisPubSubMessageHandler - processPresentationConversionProgress \n"
			+ message.get("msg") + "\n");

			BroadcastClientMessage m = new BroadcastClientMessage(msg.meetingId, "conversionUpdateMessageCallback", message);
			service.sendMessage(m);
		}
	}

	private void processPresentationRemovedMessage(String json) {
		PresentationRemovedMessage msg = PresentationRemovedMessage.fromJson(json);
		if (msg != null) {
			Map<String, Object> args = new HashMap<String, Object>(); 
			args.put("presentationID", msg.presentationId);

			Map<String, Object> message = new HashMap<String, Object>();
			Gson gson = new Gson();
			message.put("msg", gson.toJson(args));

			System.out.println("RedisPubSubMessageHandler - processPresentationRemovedMessage \n" + message.get("msg") + "\n");

			BroadcastClientMessage m = new BroadcastClientMessage(msg.meetingId, "removePresentationCallback", message);
			service.sendMessage(m);
		}
	}

	private void processGetPresentationInfoReplyMessage(String json) {
		GetPresentationInfoReplyMessage msg = GetPresentationInfoReplyMessage.fromJson(json);
		if (msg != null) {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("meetingID", msg.meetingId);
			args.put("presenter", msg.presenter);
			args.put("presentations", msg.presentations);

			Map<String, Object> message = new HashMap<String, Object>();
			Gson gson = new Gson();
			message.put("msg", gson.toJson(args));

			System.out.println("RedisPubSubMessageHandler - processGetPresentationInfoReplyMessage \n" + message.get("msg") + "\n");

			DirectClientMessage m = new DirectClientMessage(msg.meetingId, msg.requesterId, "getPresentationInfoReply", message);
			service.sendMessage(m);
		}
	}

	private void processGoToSlideMessage(String json) {
		GoToSlideReplyMessage msg = GoToSlideReplyMessage.fromJson(json);
		if (msg != null) {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("id", msg.page.get(Constants.ID));
			args.put("widthRatio", msg.page.get(Constants.WIDTH_RATIO));
			args.put("yOffset", msg.page.get(Constants.Y_OFFSET));
			args.put("xOffset", msg.page.get(Constants.X_OFFSET));
			args.put("num", msg.page.get("num"));
			args.put("heightRatio", msg.page.get(Constants.HEIGHT_RATIO));
			args.put("pngUri", msg.page.get("png_uri"));
			args.put("thumbUri", msg.page.get("thumb_uri"));
			args.put(Constants.CURRENT, msg.page.get(Constants.CURRENT));
			args.put("txtUri", msg.page.get("txt_uri"));
			args.put("swfUri", msg.page.get("swf_uri"));

			Map<String, Object> message = new HashMap<String, Object>();
			Gson gson = new Gson();
			message.put("msg", gson.toJson(args));

			System.out.println("RedisPubSubMessageHandler - processGoToSlideMessage \n"
			+ message.get("msg") + "\n");

			BroadcastClientMessage m = new BroadcastClientMessage(msg.meetingId, "goToSlideCallback", message);
			service.sendMessage(m);
		}
	}

}
