/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
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
package org.bigbluebutton.deskshare.server.recorder;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRecordingServiceImp implements RecordingService {
	private static final Logger log = LoggerFactory.getLogger(FileRecordingServiceImp.class);
	private String recordingDir;
	private EventRecorder redisDispatcher;
	
	@Override
	public Recorder getRecorderFor(String name) {
		FileRecorder r = new FileRecorder(name, recordingDir);
		r.addListener(redisDispatcher);
		return r;
	}

	/**
	 * Set the location of where the recorded files will be stored
	 * @param path The absolute path of where the files will be stored
	 */
	public void setRecordingDirectory(String path) {
		File f = new File(path);
		if (!f.exists()) {
			try {
				if (!f.mkdirs()) {
					log.error("Failed to create recording directory [" + path + "]");
				}
			} catch (SecurityException ex) {
				log.error("Security Error. Failed to create recording directory [" + path + "].");
			}
		}
		
		recordingDir = path;
	}

	public void setRedisDispatcher(EventRecorder d) {
		redisDispatcher = d;
	}
}
