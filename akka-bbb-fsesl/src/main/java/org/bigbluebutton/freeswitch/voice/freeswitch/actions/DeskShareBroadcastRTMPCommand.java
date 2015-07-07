/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2015 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
* version.
* 
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
*
*/
package org.bigbluebutton.freeswitch.voice.freeswitch.actions;

import org.bigbluebutton.freeswitch.voice.events.ConferenceEventListener;
import org.freeswitch.esl.client.transport.message.EslMessage;

public class DeskShareBroadcastRTMPCommand extends FreeswitchCommand {

	private String broadcastPath;
	private boolean broadcast;
	private String timestamp; //TODO should we remove this?
	// TODO add

	public DeskShareBroadcastRTMPCommand(String room, String requesterId, String broadcastPath, String timestamp, boolean broadcast){
		super(room, requesterId);
		this.broadcastPath = broadcastPath;
		this.broadcast = broadcast;
		this.timestamp = timestamp;
	}


	@Override
	public String getCommandArgs() {
		String action = "norecord";
		if (broadcast)
			action = "record";

		System.out.println("\n\n\n\n\n DESKSHARE BROADCAST RTMP " + broadcast + "\n\n\n\n");

		broadcastPath = "rtmp://192.168.0.109/live/abc/dev-test"; //TODO remove overwriting
		//TODO perhaps use "rtmp" in the command
		return SPACE + getRoom() + SPACE + action + SPACE + broadcastPath;
	}

	public void handleResponse(EslMessage response, ConferenceEventListener eventListener) {
		//Test for Known Conference
		System.out.println("\n\n\n\n\nBROADCAST RTMP\n\n\n\n");
	}
}