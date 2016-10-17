package org.bigbluebutton.red5.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Util {
  // Extract and return the header.name from a json pubsub message
  public static String extractName(String json) {
    JsonParser parser = new JsonParser();
    JsonObject obj = (JsonObject) parser.parse(json);
    String answer = "";

    if (obj.has("header")) {
      JsonObject header = (JsonObject) obj.get("header");

      if (header.has("name")) {
        answer = header.get("name").getAsString();
      }
    }
    return answer;
  }
}
