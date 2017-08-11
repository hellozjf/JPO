/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of MatrixOne,
 * Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.program.ProgramCentralConstants;


/**
 *  emxProgramCentralNotificationUtilBase
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 **/

public class emxWeeklyTimesheetNotificationBase_mxJPO extends emxSpool_mxJPO
{
	private static final String ATTRIBUTE_APPROVER_COMMENTS = PropertyUtil.getSchemaProperty("attribute_ApproverComments");
	public static final String TYPE_EFFORT = PropertyUtil.getSchemaProperty("type_Effort");
	public static final String RELATIONSHIP_EFFORT = PropertyUtil.getSchemaProperty("relationship_Effort");
	final String SELECT_ATTRIBUTE_APPROVER_COMMENTS = 
		"attribute[" + ATTRIBUTE_APPROVER_COMMENTS + "]";
	public static final String RELATIONSHIP_WEEKLY_TIMESHEET = 
		PropertyUtil.getSchemaProperty("relationship_WeeklyTimesheet");
	
	/**
	 * Constructs a new emxTimesheetRejectionNotification JPO object
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args an array of String arguments for this method
	 * @throws Exception 
	 * @throws Exception if the operation fails
	 */
	public emxWeeklyTimesheetNotificationBase_mxJPO (Context context, String[] args) throws Exception
	{
		super(context,args);
	}
	
	/**
	 * This method is used to get name of timesheet owner.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args 
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return StringList containing the names of timesheet owner.
	 * @throws Exception
	 *             if the operation fails
	 * @since Added by s2e for release version V6R2011x.
	 */  
	public StringList getTimeSheetOwner(Context context, String[] args)
	throws MatrixException {
		StringList slTimesheetOwner = new StringList();
		String strTimesheetOwner = null;
		try {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get(SELECT_ID);
		DomainObject dmoOwner = DomainObject.newInstance(context, objectId);
		StringList slBusSelect = new StringList(SELECT_ID);
		slBusSelect.add(SELECT_NAME);
		
		MapList mlTimesheet =  dmoOwner.getRelatedObjects(
				context,                		 // context.
				RELATIONSHIP_WEEKLY_TIMESHEET,   // relationship pattern
				TYPE_PERSON,        			 // type filter.
				slBusSelect,       				 // business object selectables.
				null,    		  				 // relationship selectables.
				true,                  		 	 // expand to direction.
				false,                   		 // expand from direction.
				(short) 1,             			 // level
				"", 							 // object where clause
				null,
				0);
		if(mlTimesheet.size() > 0){
			Map mapBusInfo = (Map)mlTimesheet.get(0);
			strTimesheetOwner = (String)mapBusInfo.get(SELECT_NAME); 
		}
		strTimesheetOwner = strTimesheetOwner + "|" + "Both";
		slTimesheetOwner.add(strTimesheetOwner);
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return slTimesheetOwner;
	}
	
    /**
     * This function is used to form the Email Text Body Content for Timesheet Rejection Event
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the input arguments to get Timesheet map
     * @throws Exception if operation fails
     * @since R210
     */
	public String formEmailTextBodyContentsForTimesheetRejection(Context context, String[] args)throws MatrixException{
		StringBuffer stringBuffer = new StringBuffer();
		try{
			HashMap mpTimesheet = (HashMap)JPO.unpackArgs(args);
			String strObjId = (String)mpTimesheet.get(SELECT_ID);
			DomainObject dmoTimesheetId = DomainObject.newInstance(context, strObjId);
			String strRejCmt = dmoTimesheetId.getInfo(context, SELECT_ATTRIBUTE_APPROVER_COMMENTS);
			
			mpTimesheet.put("messageType", "text");
		    com.matrixone.jdom.Document doc = getRejectedTimesheetMailXML(context, strRejCmt, mpTimesheet);
		    String strMsgBody = emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text");
		    
			stringBuffer.append(strMsgBody).append("\n");
			
		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return stringBuffer.toString();
	}
	
	/**
     * This function is used to form the Email HTML Body Content for Timesheet Rejection Event
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the input arguments to get Timesheet map
     * @throws Exception if operation fails
     * @since R210
     */
	public String formEmailHTMLBodyContentsForTimesheetRejection(Context context, String[] args)throws MatrixException{
		StringBuffer stringBuffer = new StringBuffer();
		try{
			HashMap mpTimesheet = (HashMap)JPO.unpackArgs(args);
			String strObjId = (String)mpTimesheet.get(SELECT_ID);
			DomainObject dmoTimesheetId = DomainObject.newInstance(context, strObjId);
			String strRejCmt = dmoTimesheetId.getInfo(context, SELECT_ATTRIBUTE_APPROVER_COMMENTS);
			
			mpTimesheet.put("messageType", "html");
		    com.matrixone.jdom.Document doc = getRejectedTimesheetMailXML(context, strRejCmt, mpTimesheet);
		    String strMsgBody = emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html");
		    
			stringBuffer.append(strMsgBody).append("\n");
			
		}catch(Exception e){
			throw new MatrixException(e);
		}
		return stringBuffer.toString();
	}
	
	/**
	 * This method used to get name of the approver.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing details of approver.
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */	
	public StringList getApproverName(Context context, String[] args)
	throws MatrixException {
		StringList slApprover = new StringList();
		String strEftId = ""; 
		try {
			emxWeeklyTimeSheetBase_mxJPO objWeeklyTimesheet = new emxWeeklyTimeSheetBase_mxJPO(context, args);
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get(SELECT_ID);
			DomainObject dmoTimesheetId = DomainObject.newInstance(context, objectId);
			
			MapList mlEfforts =  dmoTimesheetId.getRelatedObjects(
	                context,                	// context.
	                RELATIONSHIP_EFFORT,   		// relationship pattern
	                TYPE_EFFORT,         		// type filter.
	                new StringList(SELECT_ID), 	// business object selectables.
	                null,    					// relationship selectables.
	                false,                  	// expand to direction.
	                true,                   	// expand from direction.
	                (short) 1,              	// level
	                "",            				// object where clause
	                "",							// relationship where clause
	                0); 
			
			for (Iterator itr = mlEfforts.iterator(); itr.hasNext();) {
				Map mpEft = (Map) itr.next();
				strEftId = (String)mpEft.get(SELECT_ID);
			}
			
			MapList mlApprover = objWeeklyTimesheet.getApprover(context, strEftId);
			for (Iterator itr = mlApprover.iterator(); itr.hasNext();) {
				Map mpApp = (Map) itr.next();
				String strApp = (String)mpApp.get(SELECT_NAME);
				strApp = strApp + "|" + "Both";
				slApprover.addElement(strApp);
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return slApprover;
	}
	
	/**
     * This function is used to form the Email Text Body Content for Timesheet Submitted Event
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the input arguments to get Timesheet map
     * @throws Exception if operation fails
     * @since R210
     */
	public String formEmailTextBodyForSubmittedTimesheet(Context context, String[] args)throws MatrixException{
		StringBuffer stringBuffer = new StringBuffer();
		String strDate = "";
		String strMsgBody = null;
		String strPersonName = null;
		try{
			emxWeeklyTimeSheetBase_mxJPO objWeeklyTimesheet = new emxWeeklyTimeSheetBase_mxJPO(context, null);
			
			Map mpTimesheet = (Map)JPO.unpackArgs(args);
			String strObjId = (String)mpTimesheet.get(SELECT_ID);
			//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:start
	        String sCommandStatement = "print bus $1 select $2 dump";
	        strDate =  MqlUtil.mqlCommand(context, sCommandStatement,strObjId, "state[Submit].start"); 
	        //PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:End
			
			DomainObject dmoOwner = DomainObject.newInstance(context, strObjId);
			StringList slBusSelect = new StringList(SELECT_ID);
			slBusSelect.add(SELECT_NAME);
			
			MapList mlTimesheet =  dmoOwner.getRelatedObjects(
					context,                		 // context.
					RELATIONSHIP_WEEKLY_TIMESHEET,   // relationship pattern
					TYPE_PERSON,        			 // type filter.
					slBusSelect,       				 // business object selectables.
					null,    		  				 // relationship selectables.
					true,                  		 	 // expand to direction.
					false,                   		 // expand from direction.
					(short) 1,             			 // level
					"", 							 // object where clause
					null,
					0);
			if(mlTimesheet.size() > 0){
				Map mapBusInfo = (Map)mlTimesheet.get(0);
				//String strObjectId = (String)mapBusInfo.get(SELECT_ID);
				strPersonName = (String)mapBusInfo.get(SELECT_NAME); 
			}
			
			mpTimesheet.put("messageType", "text");
		    com.matrixone.jdom.Document doc = getTimesheetSubmittedMailXML(context, strPersonName, strDate, mpTimesheet);
		    strMsgBody = emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text");
			stringBuffer.append(strMsgBody).append("\n");
			
		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return stringBuffer.toString();
	}
	
	
	/**
     * This function is used to form the Email HTML Body Content for Timesheet Submitted Event
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the input arguments to get Timesheet map
     * @throws Exception if operation fails
     * @since R210
     */
	public String formEmailHTMLBodyForSubmittedTimesheet(Context context, String[] args)throws MatrixException{
		StringBuffer stringBuffer = new StringBuffer();
		String strDate = "";
		String strMsgBody = null;
		String strPersonName = null;
		try{
			emxWeeklyTimeSheetBase_mxJPO objWeeklyTimesheet = new emxWeeklyTimeSheetBase_mxJPO(context, null);
			
			Map mpTimesheet = (Map)JPO.unpackArgs(args);
			String strObjId = (String)mpTimesheet.get(SELECT_ID);
			//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:start
	        String sCommandStatement = "print bus $1 select $2 dump";
	        strDate =  MqlUtil.mqlCommand(context, sCommandStatement,strObjId, "state[Submit].start"); 
	        //PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:End
			DomainObject dmoOwner = DomainObject.newInstance(context, strObjId);
			StringList slBusSelect = new StringList(SELECT_ID);
			slBusSelect.add(SELECT_NAME);
			
			MapList mlTimesheet =  dmoOwner.getRelatedObjects(
					context,                		 // context.
					RELATIONSHIP_WEEKLY_TIMESHEET,   // relationship pattern
					TYPE_PERSON,        			 // type filter.
					slBusSelect,       				 // business object selectables.
					null,    		  				 // relationship selectables.
					true,                  		 	 // expand to direction.
					false,                   		 // expand from direction.
					(short) 1,             			 // level
					"", 							 // object where clause
					null,
					0);
			if(mlTimesheet.size() > 0){
				Map mapBusInfo = (Map)mlTimesheet.get(0);
				strPersonName = (String)mapBusInfo.get(SELECT_NAME); 
			}
			
			mpTimesheet.put("messageType", "html");
		    com.matrixone.jdom.Document doc = getTimesheetSubmittedMailXML(context, strPersonName, strDate, mpTimesheet);
		    strMsgBody = emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html");
			stringBuffer.append(strMsgBody).append("\n");
			
		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return stringBuffer.toString();
	}
	
 /** The icon mail message created by this method  
  * @param context the eMatrix <code>Context</code> object
  * @param info info map
  * @return com.matrixone.jdom.Document
  * @throws Exception 
  * @since PRG R210
  */
 private com.matrixone.jdom.Document getTimesheetSubmittedMailXML(Context context, String strSubmittedBy, String strSubmittedOn, Map info) throws MatrixException
 {
	 try{
		 // get base url
	     String baseURL = (String)info.get("baseURL");
	     int ind = baseURL.lastIndexOf("/");
	     String str = baseURL.substring(ind);
	     baseURL = FrameworkUtil.findAndReplace(baseURL, str, "");
	     
	     int index = baseURL.lastIndexOf("/");
	     String string = baseURL.substring(index);
	     baseURL = FrameworkUtil.findAndReplace(baseURL, string, "");
	     baseURL = baseURL + "/" + "programcentral" + "/"; 
	     
	     String bundleName = (String)info.get("bundleName");
	     String locale = ((Locale)info.get("locale")).toString();
	
	     // tree menu passed as url parameter in order to show the navigation tree for workspace vault
	     String messageType = (String)info.get("messageType");
	   
	     // get workspace vault id
	     String sTimesheetObjectId = (String)info.get("id");
	     
	     StringBuffer baseURLBuf = new StringBuffer(256);
	     baseURLBuf.append(baseURL.toString());
         baseURLBuf.append("emxProgramCentralWeeklyTimesheetUtil.jsp?mode=displayTimesheetTasksForApprover");
         baseURLBuf.append("&amp;objectId=");
         baseURLBuf.append(sTimesheetObjectId);
	   
	     // get workspace vault object info
	     DomainObject mainDoc = DomainObject.newInstance(context, sTimesheetObjectId);
	     StringList selectList = new StringList(3);
	     selectList.addElement(DomainConstants.SELECT_TYPE);
	     selectList.addElement(DomainConstants.SELECT_NAME);
	     selectList.addElement(DomainConstants.SELECT_REVISION);
	     Map mpTimesheetInfo = mainDoc.getInfo(context, selectList);
	     String strTimesheetType = (String)mpTimesheetInfo.get(DomainConstants.SELECT_TYPE);
	     String i18NMainTimesheetType = UINavigatorUtil.getAdminI18NString("type", strTimesheetType, locale);
	     String strTimesheetName = (String)mpTimesheetInfo.get(DomainConstants.SELECT_NAME);
	     String mainTimesheetRev = (String)mpTimesheetInfo.get(DomainConstants.SELECT_REVISION);
	
	     // header data
	     Map headerInfo = new HashMap();;
	     //headerInfo.put("header", "\n" + i18NMainTimesheetType + " " + strTimesheetName);
	     String StrHeader = i18NMainTimesheetType + " " + strTimesheetName;
	     headerInfo.put("header", "\n" + StrHeader);
	    
	     //body info
	     Map bodyInfo = new HashMap();;
	  
	     // footer data
	     Map footerInfo=new HashMap();;
	  
	       // footerInfo = new HashMap();
	         ArrayList dataLineInfo = new ArrayList();
	         
	         if (messageType.equalsIgnoreCase("html")) {
	             String[] messageValues = new String[4];
	            // messageValues[0] = baseURL + "?objectId=" + sTimesheetObjectId +"&action=submit";
	             messageValues[0] = baseURLBuf.toString();
	             messageValues[1] = strSubmittedBy;
	             messageValues[2] = strSubmittedOn;
	             String viewLink = MessageUtil.getMessage(context,null,
	                     "emxProgramCentral.TimesheetSubmittedNotification.TimesheetHTML",
	                     messageValues,null,
	                     context.getLocale(),bundleName);
	             dataLineInfo.add(StrHeader);
	             dataLineInfo.add(viewLink);
	         } else {
	             String[] messageValues = new String[3];
	             messageValues[0] = strSubmittedBy;
	             messageValues[1] = strSubmittedOn;
	             String viewLink = MessageUtil.getMessage(context,null,
	                     "emxProgramCentral.TimesheetSubmittedNotification.TimesheetText",
	                     messageValues,null,
	                     context.getLocale(),bundleName);
	             dataLineInfo.add(StrHeader);
	             dataLineInfo.add(viewLink);
	             dataLineInfo.add(baseURLBuf.toString());
	             //dataLineInfo.add(baseURL + "&objectId=" + sTimesheetObjectId);
	         }
	      footerInfo.put("dataLines", dataLineInfo);
	     return emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, bodyInfo, footerInfo);
	 }catch(Exception e){
		 throw new MatrixException(e);
	 }
 }
 
 /** The icon mail message created by this method  
  * @param context the eMatrix <code>Context</code> object
  * @param info info map
  * @return com.matrixone.jdom.Document
  * @throws Exception 
  * @since PRG R210
  */
 private com.matrixone.jdom.Document getRejectedTimesheetMailXML(Context context, String strRejCmt, Map info) throws MatrixException
 {
	 try{
	     // get base url
	     String baseURL = (String)info.get("baseURL");
	     int ind = baseURL.lastIndexOf("/");
	     String str = baseURL.substring(ind);
	     baseURL = FrameworkUtil.findAndReplace(baseURL, str, "");
	     
	     int index = baseURL.lastIndexOf("/");
	     String string = baseURL.substring(index);
	     baseURL = FrameworkUtil.findAndReplace(baseURL, string, "");
	     baseURL = baseURL + "/" + "programcentral" + "/"; 
	     
	     String bundleName = (String)info.get("bundleName");
	     String locale = ((Locale)info.get("locale")).toString();
	
	     // tree menu passed as url parameter in order to show the navigation tree for workspace vault
	     String messageType = (String)info.get("messageType");
	   
	     // get workspace vault id
	     String sTimesheetObjectId = (String)info.get("id");
	   
	     StringBuffer baseURLBuf = new StringBuffer(256);
	     baseURLBuf.append(baseURL.toString());
         baseURLBuf.append("emxProgramCentralWeeklyTimesheetUtil.jsp?mode=displayTimesheetTasks");
         baseURLBuf.append("&amp;objectId=");
         baseURLBuf.append(sTimesheetObjectId);
         
	     // get workspace vault object info
	     DomainObject mainDoc = DomainObject.newInstance(context, sTimesheetObjectId);
	     StringList selectList = new StringList(3);
	     selectList.addElement(DomainConstants.SELECT_TYPE);
	     selectList.addElement(DomainConstants.SELECT_NAME);
	     selectList.addElement(DomainConstants.SELECT_REVISION);
	     Map mpTimesheetInfo = mainDoc.getInfo(context, selectList);
	     String strTimesheetType = (String)mpTimesheetInfo.get(DomainConstants.SELECT_TYPE);
	     String i18NMainTimesheetType = UINavigatorUtil.getAdminI18NString("type", strTimesheetType, locale);
	     String strTimesheetName = (String)mpTimesheetInfo.get(DomainConstants.SELECT_NAME);
	     String mainTimesheetRev = (String)mpTimesheetInfo.get(DomainConstants.SELECT_REVISION);
	
	     // header data
	     Map headerInfo = new HashMap();
	     //headerInfo.put("header", "\n"+ i18NMainTimesheetType + " " + strTimesheetName);
	     String StrHeader = i18NMainTimesheetType + " " + strTimesheetName;
	     headerInfo.put("header", "\n" + StrHeader);
	     
	     //body info
	     Map bodyInfo = new HashMap();;
	  
	     // footer data
	     Map footerInfo=new HashMap();;
	  
	        //footerInfo = new HashMap();
	         ArrayList dataLineInfo = new ArrayList();
	         
	         if (messageType.equalsIgnoreCase("html")) {
	             String[] messageValues = new String[4];
	             //messageValues[0] = baseURL + "?objectId=" + sTimesheetObjectId;
	             messageValues[0] = baseURLBuf.toString();
	             messageValues[1] = strTimesheetName;
	             messageValues[2] = strRejCmt;
	             String viewLink = MessageUtil.getMessage(context,null,
	                     "emxProgramCentral.TimesheetRejectionNotification.TimesheetHTML",
	                     messageValues,null,
	                     context.getLocale(),bundleName);
	             dataLineInfo.add(viewLink);
	         } else {
	             String[] messageValues = new String[3];
	             messageValues[0] = strTimesheetName;
	             messageValues[1] = strRejCmt;
	             String viewLink = MessageUtil.getMessage(context,null,
	                     "emxProgramCentral.TimesheetRejectionNotification.TimesheetText",
	                     messageValues,null,
	                     context.getLocale(),bundleName);
	             dataLineInfo.add(viewLink);
	             dataLineInfo.add(baseURLBuf.toString());
	             //dataLineInfo.add(baseURL + "&objectId=" + sTimesheetObjectId);
	         }
	      footerInfo.put("dataLines", dataLineInfo);
	     return emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, bodyInfo, footerInfo);
	 }catch(Exception e){
		 e.printStackTrace();
		 throw new MatrixException(e);
	 }
 }
 
protected boolean shouldSendNotificationForSubTimesheet(Context context, String strTo) throws MatrixException
{
	boolean isNotifForSubTimesheet = true;
	context.setUser(strTo);
	isNotifForSubTimesheet = PersonUtil.getSubmittedTimesheetNotification(context);
	if(!isNotifForSubTimesheet)
	{
		return false;
	}
	return true;
}

protected boolean shouldSendNotificationForRejTimesheet(Context context, String strTo) throws MatrixException
{
	boolean isNotifForRejTimesheet = true;
	context.setUser(strTo);
	isNotifForRejTimesheet = PersonUtil.getRejectedTimesheetNotification(context);
	if(!isNotifForRejTimesheet)
	{
		return false;
	}
	return true;
}

	 /**
	 * This method sends the Email of Consolidated Notification Requests according to toList,
	 * ccList, bccList
	 *
	 * @param context The Matrix Context object
	 * @param mlNotificationRequests The MapList with each Map representing the Notification Request relationship
	 * 								 information. SELECT_ATTRIBUTE_ATTACHMENTS key represents the EMAIL_ATTACHMENT_SEPARATOR object ids
	 * @throws MatrixException if operation fails
	 * @return void
	 * @since R210
	 */
 protected void sendNotifications(Context context, MapList mlNotificationRequests) throws MatrixException
	{
		try {
			i18nNow i18n = new i18nNow();
			StringList slTo = new StringList();
			String strTo = null;
			final String STRING_SUBMITTED_TIMESHEET_BODY_LINE = i18n.GetString(ProgramCentralConstants.RESOURCE_BUNDLE, context.getSession().getLanguage(), "emxProgramCentral.TimesheetSubmittedNotification.BodyText");
			final String STRING_REJECTED_TIMESHEET_BODY_LINE = i18n.GetString(ProgramCentralConstants.RESOURCE_BUNDLE, context.getSession().getLanguage(), "emxProgramCentral.TimesheetRejectionNotification.BodyText");
			for (int itrNotification = 0; itrNotification< mlNotificationRequests.size(); itrNotification++) {
				Map mapNotificationRequest = (Map) mlNotificationRequests.get(itrNotification);

				StringList slToList = toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_STATIC_TO_LIST), EMAIL_ADDRESS_SEPARATOR);
				String fromAgent = (String) mapNotificationRequest.get(SELECT_ATTRIBUTE_FROM_AGENT);
				StringList slCcList = toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_STATIC_CC_LIST), EMAIL_ADDRESS_SEPARATOR);
				StringList slBccList =  toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_STATIC_BCC_LIST), EMAIL_ADDRESS_SEPARATOR);
				String strSubject = (String)mapNotificationRequest.get(SELECT_ATTRIBUTE_SUBJECT_TEXT);
				String strBody = (String)mapNotificationRequest.get(SELECT_ATTRIBUTE_BODY_TEXT);
				StringList slAttachments = toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_ATTACHMENTS), EMAIL_ATTACHMENT_SEPARATOR);
				String strNotificationName = (String)mapNotificationRequest.get(SELECT_ATTRIBUTE_NOTIFICATION_NAME);

				debug("sendNotification To:" + slToList + "fromAgent:" + fromAgent + " Cc:" + slCcList + " Bcc:" + slBccList + " Subject:" + strSubject + " Attachments:" + slAttachments + "strNotificationName:" +strNotificationName);
				
				String strContextUser = context.getUser();
				
				if("PMCWeeklyTimesheetSubmissionEvent".equalsIgnoreCase(strNotificationName))
				{
					strBody = STRING_SUBMITTED_TIMESHEET_BODY_LINE + strBody+"\n";
					for(int i = 0; i < slToList.size(); i++)
					{
						strTo = (String)slToList.get(i); 
						if(shouldSendNotificationForSubTimesheet(context, strTo))
						{
							slTo.add(strTo);
						}
					}
					context.setUser(strContextUser);
					MailUtil.sendNotification(context, slTo, slCcList,
						slBccList, strSubject, null, null,
						strBody.toString(), null, null,
						slAttachments, null);
				}
				else if("PMCWeeklyTimesheetRejectionEvent".equalsIgnoreCase(strNotificationName))
				{
						strBody = STRING_REJECTED_TIMESHEET_BODY_LINE + strBody+"\n";
					for(int i = 0; i < slToList.size(); i++)
					{
						strTo = (String)slToList.get(i); 
						if(shouldSendNotificationForRejTimesheet(context, strTo))
						{
							slTo.add(strTo);
						}
				}
					context.setUser(strContextUser);
					MailUtil.sendNotification(context, slTo, slCcList,
						slBccList, strSubject, null, null,
						strBody.toString(), null, null,
						slAttachments, null);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}
	
 
}


