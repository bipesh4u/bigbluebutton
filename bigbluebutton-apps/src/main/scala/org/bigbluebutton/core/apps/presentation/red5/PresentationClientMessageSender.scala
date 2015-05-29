package org.bigbluebutton.core.apps.presentation.red5

import org.bigbluebutton.conference.meeting.messaging.red5.ConnectionInvokerService
import org.bigbluebutton.core.api._
import org.bigbluebutton.conference.meeting.messaging.red5.BroadcastClientMessage
import org.bigbluebutton.conference.meeting.messaging.red5.DirectClientMessage
import collection.JavaConversions._
import com.google.gson.Gson
import java.util.ArrayList
import org.bigbluebutton.core.apps.presentation.Page
import org.bigbluebutton.core.apps.presentation.Presentation


class PresentationClientMessageSender(service: ConnectionInvokerService) extends OutMessageListener2 {
  
	private val OFFICE_DOC_CONVERSION_SUCCESS_KEY = "OFFICE_DOC_CONVERSION_SUCCESS";
    private val OFFICE_DOC_CONVERSION_FAILED_KEY = "OFFICE_DOC_CONVERSION_FAILED";
    private val SUPPORTED_DOCUMENT_KEY = "SUPPORTED_DOCUMENT";
    private val UNSUPPORTED_DOCUMENT_KEY = "UNSUPPORTED_DOCUMENT";
    private val PAGE_COUNT_FAILED_KEY = "PAGE_COUNT_FAILED";
    private val PAGE_COUNT_EXCEEDED_KEY = "PAGE_COUNT_EXCEEDED";	
    private val GENERATED_SLIDE_KEY = "GENERATED_SLIDE";
    private val GENERATING_THUMBNAIL_KEY = "GENERATING_THUMBNAIL";
    private val GENERATED_THUMBNAIL_KEY = "GENERATED_THUMBNAIL";
    private val CONVERSION_COMPLETED_KEY = "CONVERSION_COMPLETED";
    
  def handleMessage(msg: IOutMessage) {
    msg match {
      case msg: SharePresentationOutMsg             => handleSharePresentationOutMsg(msg)
      case _ => // do nothing
    }
  }

  private def handleSharePresentationOutMsg(msg: SharePresentationOutMsg) {
	val args = new java.util.HashMap[String, Object]();
	
	val presentation = new java.util.HashMap[String, Object]();
	presentation.put("id", msg.presentation.id)
	presentation.put("name", msg.presentation.name)
	presentation.put("current", msg.presentation.current:java.lang.Boolean)      
	   
	// Get the pages for a presentation
    val pages = new ArrayList[Page]()	
	msg.presentation.pages.values foreach {p =>
     pages.add(p)
    }   
	// store the pages in the presentation 
	presentation.put("pages", pages)
	
	args.put("presentation", presentation);
	
    val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	  	
	val m = new BroadcastClientMessage(msg.meetingID, "sharePresentationCallback", message);
	service.sendMessage(m);	    
  }
}