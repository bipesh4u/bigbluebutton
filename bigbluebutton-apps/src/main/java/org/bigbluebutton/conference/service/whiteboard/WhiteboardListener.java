package org.bigbluebutton.conference.service.whiteboard;

import org.bigbluebutton.conference.service.messaging.MessagingConstants;
import org.bigbluebutton.conference.service.messaging.redis.MessageHandler;

import org.bigbluebutton.core.api.IBigBlueButtonInGW;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

public class WhiteboardListener implements MessageHandler{

        private IBigBlueButtonInGW bbbInGW;

        public void setBigBlueButtonInGW(IBigBlueButtonInGW bbbInGW) {
                this.bbbInGW = bbbInGW;
        }

        @Override
        public void handleMessage(String pattern, String channel, String message) {
                if (channel.equalsIgnoreCase(MessagingConstants.TO_WHITEBOARD_CHANNEL)) {
                        System.out.println("AntonChannel=(whiteboard)" + channel);

                        JsonParser parser = new JsonParser();
                        JsonObject obj = (JsonObject) parser.parse(message);
                        JsonObject headerObject = (JsonObject) obj.get("header");
                        JsonObject payloadObject = (JsonObject) obj.get("payload");

                        String eventName =  headerObject.get("name").toString().replace("\"", "");

                        if(eventName.equalsIgnoreCase("get_whiteboard_shapes_request") ||
                                eventName.equalsIgnoreCase("whiteboard_cleared_message") ||
                                eventName.equalsIgnoreCase("undo_whiteboard_request")) {
                        //more cases to follow

                                String roomName = payloadObject.get("meeting_id").toString().replace("\"", "");
                                String requesterID = payloadObject.get("requester_id").toString().replace("\"", "");

                                if(eventName.equalsIgnoreCase("get_whiteboard_shapes_request")) {
                                        if(payloadObject.get("whiteboard_id") != null){
                                                String whiteboardID = payloadObject.get("whiteboard_id").toString().replace("\"", "");
                                                System.out.println("\n FOUND A whiteboardID:" + whiteboardID + "\n");
                                                bbbInGW.requestWhiteboardAnnotationHistory(roomName, requesterID, whiteboardID, requesterID);
                                        }
                                        else {
                                                System.out.println("\n DID NOT FIND A whiteboardID \n");
                                        }
                                        System.out.println("\n\n\n user<" + requesterID + "> requested the shapes.\n\n");
                                }
                                else {
                                        String whiteboardID = payloadObject.get("whiteboard_id").toString().replace("\"", "");
                                        
                                        if(eventName.equalsIgnoreCase("whiteboard_cleared_message")) {
                                                bbbInGW.clearWhiteboard(roomName, requesterID, whiteboardID);
                                        }
                                        else if(eventName.equalsIgnoreCase("undo_whiteboard_request")) {
                                                bbbInGW.undoWhiteboard(roomName, requesterID, whiteboardID);
                                        }
                                }
                        }
                }
        }
}
