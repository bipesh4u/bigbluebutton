/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2016 BigBlueButton Inc. and by respective authors (see below).
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

package org.bigbluebutton.modules.webrtcscreenshare.managers
{
    //import org.bigbluebutton.modules.webrtcscreenshare.managers.*;
	import com.asfusion.mate.events.Dispatcher;

	import flash.external.ExternalInterface;

	import org.as3commons.logging.api.ILogger;
	import org.as3commons.logging.api.getClassLogger;
	import org.bigbluebutton.core.UsersUtil;
	import org.bigbluebutton.core.managers.UserManager;
	import org.bigbluebutton.main.events.MadePresenterEvent;
	import org.bigbluebutton.modules.webrtcscreenshare.events.UseJavaModeCommand;
	import org.bigbluebutton.modules.webrtcscreenshare.events.WebRTCViewStreamEvent;
	import org.bigbluebutton.modules.webrtcscreenshare.model.ScreenshareOptions;
	import org.bigbluebutton.modules.webrtcscreenshare.events.ShareWindowEvent;
	import org.bigbluebutton.modules.webrtcscreenshare.events.WebRTCPublishWindowChangeState;
	import org.bigbluebutton.modules.webrtcscreenshare.view.components.WebRTCDesktopPublishWindow;
	import org.bigbluebutton.modules.webrtcscreenshare.services.WebRTCScreenshareService;
	import org.bigbluebutton.modules.webrtcscreenshare.utils.BrowserCheck;
	import org.bigbluebutton.modules.webrtcscreenshare.events.DeskshareToolbarEvent;
	import org.bigbluebutton.main.api.JSLog;

	public class WebRTCScreenshareManager {
		private static const LOGGER:ILogger = getClassLogger(WebRTCScreenshareManager);

		private var publishWindowManager:WebRTCPublishWindowManager;
		private var viewWindowManager:WebRTCViewerWindowManager;
		private var toolbarButtonManager:ToolbarButtonManager;
		private var module:WebRTCScreenshareModule;
		private var service:WebRTCScreenshareService;
		private var globalDispatcher:Dispatcher;
		private var sharing:Boolean = false;
		private var usingWebRTC:Boolean = false;
		private var chromeExtensionKey:String = null;

		public function WebRTCScreenshareManager() {
			JSLog.warn("WebRTCScreenshareManager::WebRTCScreenshareManager", {});
			service = new WebRTCScreenshareService();
			globalDispatcher = new Dispatcher();
			publishWindowManager = new WebRTCPublishWindowManager(service);
			viewWindowManager = new WebRTCViewerWindowManager(service);
		}

		public function handleStartModuleEvent(module:WebRTCScreenshareModule):void {
			LOGGER.debug("WebRTC Screenshare Module starting");
			JSLog.warn("WebRTCScreenshareManager::handleStartModuleEvent", {});
			this.module = module;
			service.handleStartModuleEvent(module);

			if (UsersUtil.amIPresenter()) {
				initDeskshare();
			}
		}

		public function handleStopModuleEvent():void {
			LOGGER.debug("WebRTC Screenshare Module stopping");

			publishWindowManager.stopSharing();
			viewWindowManager.stopViewing();
			service.disconnect();
		}

		/*presenter stopped their program stream*/
		public function handleStreamStoppedEvent():void {
			LOGGER.debug("WebRTCScreenshareManager::handleStreamStoppedEvent Sending screenshare stopped command");
			JSLog.warn("WebRTCScreenshareManager::handleStreamStoppedEvent", {});
			stopWebRTCDeskshare();
		}

		/*viewer being told there is no more stream*/
		public function handleStreamStopEvent(args:Object):void {
			LOGGER.debug("WebRTCScreenshareManager::handleStreamStopEvent");
			JSLog.warn("WebRTCScreenshareManager::handleStreamStopEvent", {});
			viewWindowManager.handleViewWindowCloseEvent();
		}

		public function handleRequestStopSharingEvent():void {
			JSLog.warn("WebRTCScreenshareManager::handleRequestStopSharingEvent", {});
			/* stopping WebRTC Screenshare. Alert ScreenshareManager to reset toolbar */
			globalDispatcher.dispatchEvent(new DeskshareToolbarEvent(DeskshareToolbarEvent.STOP));
			stopWebRTCDeskshare();
		}

		private function stopWebRTCDeskshare():void {
			LOGGER.debug("WebRTCScreenshareManager::stopWebRTCScreenshare");
			JSLog.warn("WebRTCScreenshareManager::stopWebRTCScreenshare", {});
			viewWindowManager.stopViewing();

			if (ExternalInterface.available) {
				ExternalInterface.call("vertoExitScreenShare");
			}
		}

		private function startWebRTCDeskshare():void {
			LOGGER.debug("WebRTCScreenshareManager::startWebRTCScreenshare");
			JSLog.warn("WebRTCScreenshareManager::startWebRTCScreenshare", {});

			if (ExternalInterface.available) {
				var videoTag:String = "localVertoVideo";
				var onFail:Function = function(args:Object):void {
					JSLog.warn("onFail - as", args);
					JSLog.warn("WebRTCScreenshareManager::startWebRTCScreenshare - falling back" +
                            " to java", {});
					globalDispatcher.dispatchEvent(new UseJavaModeCommand())
				};
				ExternalInterface.addCallback("onFail", onFail);

				var voiceBridge:String = UserManager.getInstance().getConference().voiceBridge;
				var myName:String = UserManager.getInstance().getConference().getMyName();

				ExternalInterface.call(
					'vertoShareScreen',
					videoTag,
					voiceBridge,
					myName,
					null,
					"onFail",
					chromeExtensionKey
				);
			}
		}

		private function initDeskshare():void {
			JSLog.warn("WebRTCScreenshareManager::initDeskshare", {});
			sharing = false;
			var options:ScreenshareOptions = new ScreenshareOptions();
			options.parseOptions();
			if (options.chromeExtensionKey) {
				chromeExtensionKey = options.chromeExtensionKey;
			}
		}

		public function handleMadePresenterEvent(e:MadePresenterEvent):void {
			LOGGER.debug("Got MadePresenterEvent ");
			initDeskshare();
		}

		public function handleMadeViewerEvent(e:MadePresenterEvent):void{
			LOGGER.debug("Got MadeViewerEvent ");
			if (sharing) {
				publishWindowManager.stopSharing();
				stopWebRTCDeskshare();
			}
			sharing = false;
		}

		private function canIUseVertoOnThisBrowser(newOnWebRTCBrokeFailure:Function = null, newOnNoWebRTCFailure:Function = null, newOnSuccess:Function = null):void {
			LOGGER.debug("WebRTCScreenshareManager::canIUseVertoOnThisBrowser");
			JSLog.warn("WebRTCScreenshareManager::canIUseVertoOnThisBrowser", {});
			var options:ScreenshareOptions = new ScreenshareOptions();
			options.parseOptions();
			var onNoWebRTCFailure:Function, onWebRTCBrokeFailure:Function, onSuccess:Function;

			onNoWebRTCFailure = (newOnNoWebRTCFailure != null) ? newOnNoWebRTCFailure : function(message:String):void {
				JSLog.warn(message, {});
				usingWebRTC = false;
				// send out event to fallback to Java
				JSLog.warn("WebRTCScreenshareManager::handleStartSharingEvent - falling back to java", {});
				globalDispatcher.dispatchEvent(new UseJavaModeCommand());
				return;
			};

			onWebRTCBrokeFailure = (newOnWebRTCBrokeFailure != null) ? newOnWebRTCBrokeFailure : function(message:String):void {
				JSLog.warn(message, {});
				publishWindowManager.openWindow();
				globalDispatcher.dispatchEvent(new WebRTCPublishWindowChangeState(WebRTCPublishWindowChangeState.DISPLAY_INSTALL));
			};

			onSuccess = (newOnSuccess != null) ? newOnSuccess : function(message:String):void {
				JSLog.warn("WebRTCScreenshareManager::handleStartSharingEvent onSuccess", message);
				usingWebRTC = true;
				startWebRTCDeskshare();
			};

			if (options.tryWebRTCFirst && BrowserCheck.isWebRTCSupported()) {
				JSLog.warn("WebRTCScreenshareManager::handleStartSharingEvent WebRTC Supported", {});
				if (BrowserCheck.isFirefox()) {
					onSuccess("Firefox, lets try");
				} else {
					if (chromeExtensionKey != null) {

						JSLog.warn("WebRTCScreenshareManager::handleStartSharingEvent chrome extension link exists - ", chromeExtensionKey);
						if (ExternalInterface.available) {

							var success2:Function = function(exists:Boolean):void {
								ExternalInterface.addCallback("success2", null);
								JSLog.warn("WebRTCScreenshareManager::handleStartSharingEvent inside onSuccess2", {});
								if (exists) {
									JSLog.warn("Chrome Extension exists", {});
									onSuccess("worked");
								} else {
									onWebRTCBrokeFailure("No Chrome Extension");
									JSLog.warn("no chrome extension", {});
								}
							};
							ExternalInterface.addCallback("success2", success2);
							ExternalInterface.call("checkChromeExtInstalled", "success2", chromeExtensionKey);
						}
					} else {
						onNoWebRTCFailure("No chromeExtensionKey in config.xml");
						return;
					}
				}
			} else {
				onNoWebRTCFailure("Web browser doesn't support WebRTC");
				return;
			}
		}

		/*handle start sharing event*/
		public function handleStartSharingEvent():void {
			LOGGER.debug("WebRTCScreenshareManager::handleStartSharingEvent");
			JSLog.warn("WebRTCScreenshareManager::handleStartSharingEvent", {});
			canIUseVertoOnThisBrowser();
		}

		public function handleShareWindowCloseEvent():void {
			publishWindowManager.handleShareWindowCloseEvent();
			sharing = false;
			stopWebRTCDeskshare();
		}

		public function handleViewWindowCloseEvent():void {
			LOGGER.debug("Received stop viewing command");
			JSLog.warn("WebRTCScreenshareManager::handleViewWindowCloseEvent", {});
			viewWindowManager.handleViewWindowCloseEvent();
		}

		public function handleStreamStartEvent(e:WebRTCViewStreamEvent):void{
			JSLog.warn("WebRTCScreenshareManager::handleStreamStartEvent rtmp=", e.rtmp);
			// if (!usingWebRTC) { return; } //TODO this was causing issues
			if (sharing) return; //TODO must uncomment this for the non-webrtcscreenshare
			JSLog.warn("WebRTCScreenshareManager::handleStreamStartEvent after sharing return", {});
			var isPresenter:Boolean = UserManager.getInstance().getConference().amIPresenter;
			JSLog.warn("WebRTCScreenshareManager::handleStreamStartEvent isPresenter=", isPresenter);
			LOGGER.debug("Received start viewing command when isPresenter==[{0}]",[isPresenter]);

			if(isPresenter) {
				JSLog.warn("WebRTCScreenshareManager::handleStreamStartEvent is presenter", {});
				publishWindowManager.startViewing(e.rtmp, e.videoWidth, e.videoHeight);
			} else {
				JSLog.warn("WebRTCScreenshareManager::handleStreamStartEvent is viewer", {});
				viewWindowManager.startViewing(e.rtmp, e.videoWidth, e.videoHeight);
			}

			 sharing = true; //TODO must uncomment this for the non-webrtcscreenshare
		}

		public function handleUseJavaModeCommand():void {
			JSLog.warn("WebRTCScreenshareManager::handleUseJavaModeCommand", {});
			usingWebRTC = false;
		}

		public function handleRequestStartSharingEvent():void {
			JSLog.warn("WebRTCScreenshareManager::handleRequestStartSharingEvent", {});
			initDeskshare();
			handleStartSharingEvent();
		}

		public function handleScreenShareStartedEvent(event: WebRTCViewStreamEvent):void {
			if (UsersUtil.amIPresenter()) {
			} else {
				/*handleStreamStartEvent(ScreenshareModel.getInstance().streamId, event.width, event.height);*/
				handleStreamStartEvent(null);
			}

			var dispatcher:Dispatcher = new Dispatcher();
			dispatcher.dispatchEvent(new WebRTCViewStreamEvent(WebRTCViewStreamEvent.START));
		}
		
		/*public function handleIsSharingScreenEvent(event: IsSharingScreenEvent):void {*/
		public function handleIsSharingScreenEvent():void {
			if (UsersUtil.amIPresenter()) {
			} else {
				/*handleStreamStartEvent(ScreenshareModel.getInstance().streamId, event.width, event.height);*/
				handleStreamStartEvent(null);
			}

			var dispatcher:Dispatcher = new Dispatcher();
			dispatcher.dispatchEvent(new WebRTCViewStreamEvent(WebRTCViewStreamEvent.START));
		}
	}
}
