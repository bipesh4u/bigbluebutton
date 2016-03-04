///**
// * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
// *
// * Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
// *
// * This program is free software; you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free Software
// * Foundation; either version 3.0 of the License, or (at your option) any later
// * version.
// *
// * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
// * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License along
// * with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
// *
// */
//package org.bigbluebutton.deskshare;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.red5.logging.Red5LoggerFactory;
//import org.red5.server.adapter.MultiThreadedApplicationAdapter;
//import org.slf4j.Logger;
//import org.red5.server.api.IContext;
//import org.red5.server.api.IConnection;
//import org.red5.server.api.scope.IScope;
//
//public class Red5AppAdapter extends MultiThreadedApplicationAdapter {
//    private static Logger log = Red5LoggerFactory.getLogger(Red5AppAdapter.class, "deskshare");
//
//
////    log.info("AAAAAAAAAAAAAAAAA");
//
//    @Override
//    public boolean appStart(IScope app) {
//        super.appStart(app);
//        log.info("BBB Screenshare appStart\n\n\n\n\n\n\n\n");
//        return true;
//    }
//
//    @Override
//    public boolean appConnect(IConnection conn, Object[] params) {
//        log.info("BBB Screenshare appConnect");
//        return super.appConnect(conn, params);
//    }
//
//    @Override
//    public boolean roomConnect(IConnection conn, Object[] params) {
//        log.info("BBB Screenshare roomConnect");
//        return super.roomConnect(conn, params);
//    }
//
//
//    @Override
//    public void appDisconnect(IConnection conn) {
//        log.info("BBB Screenshare appDisconnect");
//        super.appDisconnect(conn);
//    }
//
//    @Override
//    public void roomDisconnect(IConnection conn) {
//        log.info("BBB Screenshare roomDisconnect");
//
//        super.roomDisconnect(conn);
//    }
//
//}
