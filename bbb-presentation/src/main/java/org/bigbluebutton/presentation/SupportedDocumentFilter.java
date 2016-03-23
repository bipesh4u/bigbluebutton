/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 * <p>
 * Copyright (c) 2016 BigBlueButton Inc. and by respective authors (see below).
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * <p>
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along
 * with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
 */

package org.bigbluebutton.presentation;

import com.google.gson.Gson;
//import org.bigbluebutton.api.messaging.MessagingConstants;
//import org.bigbluebutton.api.messaging.MessagingService;
import org.apache.commons.io.FilenameUtils;
import org.bigbluebutton.presentation.ConversionUpdateMessage.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SupportedDocumentFilter {
    private static Logger log = LoggerFactory.getLogger(SupportedDocumentFilter.class);

//    private final MessagingService messagingService;

//    public SupportedDocumentFilter(MessagingService m) {
//        messagingService = m;
//    }

    public boolean isSupported(UploadedPresentation pres) {
//        File presentationFile = pres.getUploadedFile();
        String ext = FilenameUtils.getExtension(pres.getUploadedFilePath());

//        /* Get file extension - Perhaps try to rely on a more accurate method than an extension type ? */
        log.info("_____::isSupported name = " + pres.getName());
        log.info("_____::isSupported ext = " + ext);
//        int fileExtIndex = presentationFile.getName().lastIndexOf('.') + 1;
//        String ext = presentationFile.getName().toLowerCase().substring(fileExtIndex);
        boolean supported = SupportedFileTypes.isFileSupported(ext);
        notifyProgressListener(supported, pres);
        if (supported) {
            log.info("Received supported file " + pres.getUploadedFilePath());
            pres.setFileType(ext);
        }
        return supported;
    }

    private void notifyProgressListener(boolean supported, UploadedPresentation pres) {
        MessageBuilder builder = new ConversionUpdateMessage.MessageBuilder(pres);

        if (supported) {
            builder.messageKey(ConversionMessageConstants.SUPPORTED_DOCUMENT_KEY);
        } else {
            builder.messageKey(ConversionMessageConstants.UNSUPPORTED_DOCUMENT_KEY);
        }

        log.info("____SupportedDocumentFilter::notifyProgressListener " + pres.getName());
//        if (messagingService != null) {
//            Gson gson = new Gson();
//            String updateMsg = gson.toJson(builder.build().getMessage());
//            log.debug("sending: " + updateMsg);
//            messagingService.send(MessagingConstants.TO_PRESENTATION_CHANNEL, updateMsg);
//        } else {
//            log.warn("MessagingService has not been set!");
//        }
    }
}
