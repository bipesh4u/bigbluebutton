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
package org.bigbluebutton.modules.webrtcscreenshare.events
{
	import flash.events.Event;
	import org.bigbluebutton.main.api.JSLog;

	public class WebRTCPublishWindowChangeState extends Event
	{
		public static const DISPLAY_INSTALL:String = "WebRTC Deskshare Display Install Screen";
		public static const DISPLAY_RETRY:String = "WebRTC Deskshare Display Retry Screen";
		public static const DISPLAY_FALLBACK:String = "WebRTC Deskshare Display Fallback Screen";

		public function WebRTCPublishWindowChangeState(type:String, bubbles:Boolean=true, cancelable:Boolean=false)
		{
			JSLog.warn("new WebRTCPublishWindowChangeState", type);
			super(type, bubbles, cancelable);
		}

	}
}

