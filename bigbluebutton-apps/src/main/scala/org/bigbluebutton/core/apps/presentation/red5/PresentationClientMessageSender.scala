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
      case msg: GetPresentationInfoOutMsg           => handleGetPresentationInfoOutMsg(msg)
      case msg: ResizeAndMoveSlideOutMsg            => handleResizeAndMoveSlideOutMsg(msg)
      case msg: SharePresentationOutMsg             => handleSharePresentationOutMsg(msg)
      case msg: PresentationConversionDone          => handlePresentationConversionDone(msg)
      case _ => // do nothing
    }
  }
  
  private def handlePresentationConversionDone(msg: PresentationConversionDone) {
    val args = new java.util.HashMap[String, Object]()
	args.put("meetingID", msg.meetingID);
	args.put("code", msg.code);
	
	val presentation = new java.util.HashMap[String, Object]();
	presentation.put("id", msg.presentation.id)
	presentation.put("name", msg.presentation.name)
	presentation.put("current", msg.presentation.current:java.lang.Boolean)
	
	val pages = new ArrayList[Page]()
	
	msg.presentation.pages.values foreach {p =>
//      println("PresentationClientMessageSender **** Page [" + p.id + "," + p.num + "]")
      pages.add(p)
    }
	
	presentation.put("pages", pages)
	
	args.put("presentation", presentation);

	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  		
//  	println("PresentationClientMessageSender - handlePresentationConversionDone \n" + message.get("msg") + "\n")

	val m = new BroadcastClientMessage(msg.meetingID, "conversionCompletedUpdateMessageCallback", message);
    service.sendMessage(m);      
  }
 
  
  private def handleGetPresentationInfoOutMsg(msg: GetPresentationInfoOutMsg) {
    val info = msg.info
    
    // Build JSON
    val args = new java.util.HashMap[String, Object]()
	  args.put("meetingID", msg.meetingID);
	
    // Create a map for our current presenter
    val presenter = new java.util.HashMap[String, String]()
    presenter.put("userId", info.presenter.userId)
    presenter.put("name", info.presenter.name)
    presenter.put("assignedBy", info.presenter.assignedBy)
	
    args.put("presenter", presenter)
    
    // Create an array for our presentations
    val presentations = new ArrayList[java.util.HashMap[String, Object]]     
    info.presentations.foreach { pres =>
	   val presentation = new java.util.HashMap[String, Object]();
	   presentation.put("id", pres.id)
	   presentation.put("name", pres.name)
	   presentation.put("current", pres.current:java.lang.Boolean)      
	   
	   // Get the pages for a presentation
       val pages = new ArrayList[Page]()	
	   pres.pages.values foreach {p =>
         pages.add(p)
       }   
	  // store the pages in the presentation 
	  presentation.put("pages", pages)
	
	  // add this presentation into our presentations list
	  presentations.add(presentation);	   
    }
    
    // add the presentation to our map to complete our json
    args.put("presentations", presentations)

    val message = new java.util.HashMap[String, Object]() 
	  val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	 	
  	println("***** PresentationClientMessageSender - handleGetPresentationInfoOutMsg to user[" +msg.requesterID + "] message[" + message.get("msg") + "]")
  	
	val m = new DirectClientMessage(msg.meetingID, msg.requesterID, "getPresentationInfoReply", message);
//	service.sendMessage(m);	
  }

  
  private def handleResizeAndMoveSlideOutMsg(msg: ResizeAndMoveSlideOutMsg) {
	val args = new java.util.HashMap[String, Object]();
	args.put("id", msg.page.id)
	args.put("num", msg.page.num:java.lang.Integer)
	args.put("current", msg.page.current:java.lang.Boolean)
	args.put("swfUri", msg.page.swfUri)
	args.put("txtUri", msg.page.txtUri)
	args.put("pngUri", msg.page.pngUri)
	args.put("thumbUri", msg.page.thumbUri)
	args.put("xOffset", msg.page.xOffset:java.lang.Double);
	args.put("yOffset", msg.page.yOffset:java.lang.Double);
	args.put("widthRatio", msg.page.widthRatio:java.lang.Double);
	args.put("heightRatio", msg.page.heightRatio:java.lang.Double);

	val message = new java.util.HashMap[String, Object]() 
	val gson = new Gson();
  	message.put("msg", gson.toJson(args))
  	  	
	val m = new BroadcastClientMessage(msg.meetingID, "moveCallback", message);
	service.sendMessage(m);	    
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