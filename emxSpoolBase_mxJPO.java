/*
 **   emxSpoolBase
 **
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **	  @since R210
 */
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;

/**
 *  The emxSpoolBase class represents the Spool JPO Functionality
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  @since R210
 **/

public class emxSpoolBase_mxJPO extends emxDomainObject_mxJPO
{
	protected static final boolean isDebugEnabled = true;

	protected static final String EMAIL_ADDRESS_SEPARATOR = ",";
	protected static final String EMAIL_ATTACHMENT_SEPARATOR = ",";
	protected static final String CONSOLIDATION_JPO_SEPERATOR = ":";
	private static final String EMAIL_BODY_SEPARATOR = "\n";

	protected static final String ATTRIBUTE_STATIC_FROM_AGENT_LIST = PropertyUtil.getSchemaProperty("attribute_FromAgent");
	protected static final String SELECT_ATTRIBUTE_STATIC_FROM_AGENT_LIST = "attribute[" + ATTRIBUTE_STATIC_FROM_AGENT_LIST + "]";

	protected static final String ATTRIBUTE_STATIC_TO_LIST = PropertyUtil.getSchemaProperty("attribute_StaticToList");
	protected static final String SELECT_ATTRIBUTE_STATIC_TO_LIST = "attribute[" + ATTRIBUTE_STATIC_TO_LIST + "]";

	protected static final String ATTRIBUTE_STATIC_CC_LIST = PropertyUtil.getSchemaProperty("attribute_StaticCcList");
    protected static final String SELECT_ATTRIBUTE_STATIC_CC_LIST = "attribute[" + ATTRIBUTE_STATIC_CC_LIST + "]";

    protected static final String ATTRIBUTE_STATIC_BCC_LIST = PropertyUtil.getSchemaProperty("attribute_StaticBccList");
    protected static final String SELECT_ATTRIBUTE_STATIC_BCC_LIST = "attribute[" + ATTRIBUTE_STATIC_BCC_LIST + "]";

    protected static final String ATTRIBUTE_SUBJECT_TEXT = PropertyUtil.getSchemaProperty("attribute_SubjectText");
    protected static final String SELECT_ATTRIBUTE_SUBJECT_TEXT = "attribute[" + ATTRIBUTE_SUBJECT_TEXT + "]";

    protected static final String ATTRIBUTE_BODY_TEXT = PropertyUtil.getSchemaProperty("attribute_BodyText");
    protected static final String SELECT_ATTRIBUTE_BODY_TEXT = "attribute[" + ATTRIBUTE_BODY_TEXT + "]";

    protected static final String ATTRIBUTE_BODY_HTML = PropertyUtil.getSchemaProperty("attribute_BodyHTML");
    protected static final String SELECT_ATTRIBUTE_BODY_HTML= "attribute[" + ATTRIBUTE_BODY_HTML + "]";

    protected static final String ATTRIBUTE_FROM_AGENT = PropertyUtil.getSchemaProperty("attribute_FromAgent");
    protected static final String SELECT_ATTRIBUTE_FROM_AGENT = "attribute[" + ATTRIBUTE_FROM_AGENT + "]";

    protected static final String ATTRIBUTE_REPLY_TO = PropertyUtil.getSchemaProperty("attribute_ReplyTo");
    protected static final String SELECT_ATTRIBUTE_REPLY_TO = "attribute[" + ATTRIBUTE_REPLY_TO + "]";

    protected static final String ATTRIBUTE_ATTACHMENTS = PropertyUtil.getSchemaProperty("attribute_Attachments");
    protected static final String SELECT_ATTRIBUTE_ATTACHMENTS = "attribute[" + ATTRIBUTE_ATTACHMENTS + "]";

    protected static final String ATTRIBUTE_NOTIFICATION_TYPE = PropertyUtil.getSchemaProperty("attribute_NotificationType");
    protected static final String SELECT_ATTRIBUTE_NOTIFICATION_TYPE = "attribute[" + ATTRIBUTE_NOTIFICATION_TYPE + "]";

    protected static final String ATTRIBUTE_NOTIFICATION_NAME = PropertyUtil.getSchemaProperty("attribute_NotificationName");
    protected static final String SELECT_ATTRIBUTE_NOTIFICATION_NAME = "attribute[" + ATTRIBUTE_NOTIFICATION_NAME + "]";

    protected static final String ATTRIBUTE_CONSOLIDATION_JPO = PropertyUtil.getSchemaProperty("attribute_ConsolidationJPO");
    protected static final String SELECT_ATTRIBUTE_CONSOLIDATION_JPO = "attribute[" + ATTRIBUTE_CONSOLIDATION_JPO + "]";

    protected static final String TYPE_NOTIFICATION_SPOOL = PropertyUtil.getSchemaProperty("type_NotificationSpool");
    protected static final String TYPE_NOTIFICATION = PropertyUtil.getSchemaProperty("type_Notification");

    protected static final String RELATIONSHIP_NOTIFICATION_REQUEST = PropertyUtil.getSchemaProperty("relationship_NotificationRequest");

    protected static final String DEFAULT_CONSOLIDATION_PROGRAM = "emxSpool";
    protected static final String DEFAULT_CONSOLIDATION_METHOD = "consolidateNotifications";


	/**
	 * Prints the message on the console
	 * @param strMessage the string argument for storing message to print
	 * @since R210
	 */
    protected void debug(String strMessage) {
    	if (!isDebugEnabled) {
    		return;
    	}

    	System.out.println("DEBUG : emxSpoolBase_mxJPO : " + new Date() + " : " + strMessage);
    }

	/**
	 * Constructs a new emxSpool JPO object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args an array of String arguments for this method
	 * @throws Exception if the operation fails
	 * @since R210
	 */
	public emxSpoolBase_mxJPO (Context context, String[] args) throws Exception
	{
		super(context,args);
	}

	/**
	 * This method processes the Notification Spool. To consolidate, the method finds the corresponding Notification object and
	 * the attribute Consolidation JPO. If this attribute is blank then default consolidation program will be called
	 * i.e. emxSpool:consolidateNotifications otherwise custom specified consolidation program will be executed.
	 * This method will group the notification requests coming from same notification objects before sending them to
	 * consolidation program.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - an array of String arguments, args[0] = name of the spool object
	 * @throws MatrixException if the operation fails
	 * @return void
	 * @since R210
	 */
	public void processSpool(Context context, String[] args) throws MatrixException
	{

		String strSpoolName = args[0];
		try {
			debug("Begin:processSpool (" + strSpoolName + ")");

			StringList slBusSelect = new StringList(SELECT_ID);

		    //Queries on the given criteria into a map list containing a mapping of selectable names to values
			MapList mlSpool = DomainObject.findObjects(
													context,  									// The current context object
													TYPE_NOTIFICATION_SPOOL,  					// The type pattern
													strSpoolName,								// The name pattern
													"-",										// The revision pattern
													null,										// The owner pattern
													"*", 										// The vault pattern
													null, 										// The where expression
													false,										// expandType
													slBusSelect);								// The select clause

			if (mlSpool == null || mlSpool.size() == 0)
			{
                String[] formatArgs = {strSpoolName};
                String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.SpoolBase.SpoolNotFound",formatArgs);
				throw new MatrixException(message);
			}

			Map mapSpoolInfo = (Map)mlSpool.get(0);
			String strObjectIdSpool = (String)mapSpoolInfo.get(SELECT_ID);
			debug ("Spool Id " + strObjectIdSpool);

			DomainObject domNotificationSpool = DomainObject.newInstance(context, strObjectIdSpool);

			StringList relationshipSelects = new StringList(DomainRelationship.SELECT_ID);
			relationshipSelects.add(SELECT_ATTRIBUTE_FROM_AGENT);
			relationshipSelects.add(SELECT_ATTRIBUTE_STATIC_TO_LIST);
			relationshipSelects.add(SELECT_ATTRIBUTE_STATIC_CC_LIST);
			relationshipSelects.add(SELECT_ATTRIBUTE_STATIC_BCC_LIST);
			relationshipSelects.add(SELECT_ATTRIBUTE_SUBJECT_TEXT);
			relationshipSelects.add(SELECT_ATTRIBUTE_BODY_TEXT);
			relationshipSelects.add(SELECT_ATTRIBUTE_BODY_HTML);
			relationshipSelects.add(SELECT_ATTRIBUTE_ATTACHMENTS);
			relationshipSelects.add(SELECT_ATTRIBUTE_NOTIFICATION_NAME);
			relationshipSelects.add(SELECT_ATTRIBUTE_NOTIFICATION_TYPE);
			relationshipSelects.add(SELECT_ATTRIBUTE_REPLY_TO);

			// expand to get Notification Spool
			MapList mlNotificationRequests =  domNotificationSpool.getRelatedObjects(
												context,                		  		// context.
												RELATIONSHIP_NOTIFICATION_REQUEST, 		// relationship pattern
												TYPE_NOTIFICATION_SPOOL, 				// type filter.
												null,        							// business object selectables.
												relationshipSelects,    		  		// relationship selectables.
												true,                  		 			// expand to direction.
												false,                   		  		// expand from direction.
												(short) 1,             			  		// level
												null, 						 			// object where clause
												null,
												0);

			mlNotificationRequests.addSortKey(SELECT_ATTRIBUTE_NOTIFICATION_NAME, "ascending", "String");
			mlNotificationRequests.addSortKey(SELECT_ATTRIBUTE_NOTIFICATION_TYPE, "ascending", "String");
			mlNotificationRequests.sort();

			debug("Total Notification Request=" + mlNotificationRequests.size());

			MapList mlSimilarNotificationRequest = new MapList();
			Map mapLastNotificationSpool = new HashMap();
			for(int itr = 0; itr < mlNotificationRequests.size(); itr++)
			{
				Map mapNotificationRequest = (Map) mlNotificationRequests.get(itr);
				String strNotificationName = (String)mapNotificationRequest.get(SELECT_ATTRIBUTE_NOTIFICATION_NAME);

				if(mlSimilarNotificationRequest.size() == 0)
				{
					mlSimilarNotificationRequest.add(mapNotificationRequest);
				}
				else
				{
					mapLastNotificationSpool = (Map)mlSimilarNotificationRequest.get(mlSimilarNotificationRequest.size() - 1);

					if (isSimilarNotificationName(mapNotificationRequest, mapLastNotificationSpool))
					{
						mlSimilarNotificationRequest.add(mapNotificationRequest);
					}
					else
					{
						// Calls this method to process similar Notification Requests
						processSimilarRequests(context, mlSimilarNotificationRequest);

						mlSimilarNotificationRequest.clear();
						mlSimilarNotificationRequest.add(mapNotificationRequest);
					}
				}
			}

			if(!mlSimilarNotificationRequest.isEmpty())
			{
				// Calls this method to process similar Notification Requests
				processSimilarRequests(context, mlSimilarNotificationRequest);

				mlSimilarNotificationRequest.clear();
			}
		}
		catch(Exception e){
			if (isDebugEnabled) {
				e.printStackTrace();
			}
			throw new MatrixException(e);
		}
		finally {
			debug("End:processSpool (" + strSpoolName + ")");
		}
	}

	/**
	 * This method consolidates the Notification Requests. Those are consolidated based on to, cc, bcc
	 * of the notification requests. After sending the consolidated notification the notification request from
	 * spool are deleted. This method assumes that the notification requests passed in are from same
	 * notification object i.e. they all have same subject
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - packed MapList containing Map of Notification Request information
	 * @throws MatrixException if the operation fails
	 * @return void
	 * @since R210
	 */
	public void consolidateNotifications(Context context, String[] args) throws MatrixException
	{
		try {
			debug("Begin:consolidateNotifications");
			ContextUtil.startTransaction(context, true);

			MapList mlNotificationRequests = (MapList)JPO.unpackArgs(args);

			String[] fieldKeys = new String[3];
			fieldKeys[0] = SELECT_ATTRIBUTE_ATTACHMENTS;
			fieldKeys[1] = SELECT_ATTRIBUTE_BODY_TEXT;
			fieldKeys[2] = SELECT_ATTRIBUTE_BODY_HTML;
			
			String[] fieldSeparators = new String[3];
			fieldSeparators[0] = EMAIL_ATTACHMENT_SEPARATOR;
			fieldSeparators[1] = EMAIL_BODY_SEPARATOR;
			fieldSeparators[2] = EMAIL_BODY_SEPARATOR;

			//
			// Reorder the recipient addresses
			//
			for (Iterator itrNotificationRequests = mlNotificationRequests.iterator(); itrNotificationRequests.hasNext();)
			{
				Map mapNotificationRequest = (Map) itrNotificationRequests.next();

				sortRecipientAddress(mapNotificationRequest, SELECT_ATTRIBUTE_STATIC_TO_LIST);
				sortRecipientAddress(mapNotificationRequest, SELECT_ATTRIBUTE_STATIC_CC_LIST);
				sortRecipientAddress(mapNotificationRequest, SELECT_ATTRIBUTE_STATIC_BCC_LIST);
			}

			//
			// Sort the notification requests as per To, Cc and Bcc list
			// so that similar notification requests will come closer
			//
			mlNotificationRequests.addSortKey(SELECT_ATTRIBUTE_SUBJECT_TEXT, "ascending", "String");
			mlNotificationRequests.addSortKey(SELECT_ATTRIBUTE_STATIC_TO_LIST, "ascending", "String");
			mlNotificationRequests.addSortKey(SELECT_ATTRIBUTE_STATIC_CC_LIST, "ascending", "String");
			mlNotificationRequests.addSortKey(SELECT_ATTRIBUTE_STATIC_BCC_LIST, "ascending", "String");
			mlNotificationRequests.sort();

			//
			// Consolidate these notification requests now
			//
			MapList mlConsolidatedNotificationRequests = new MapList();
			for (Iterator itrNotificationRequests = mlNotificationRequests.iterator(); itrNotificationRequests.hasNext();)
			{
				Map mapNotificationRequest = (Map) itrNotificationRequests.next();

				if (mlConsolidatedNotificationRequests.size() == 0)
				{
					mlConsolidatedNotificationRequests.add(mapNotificationRequest);
				}
				else
				{
					// Get the last Not. Req
					Map mapLastNotificationRequest = (Map)mlConsolidatedNotificationRequests.get(mlConsolidatedNotificationRequests.size() - 1);

					if (isSimilarNotificationRequest(mapNotificationRequest, mapLastNotificationRequest))
					{
						mergeSimilarNotification(mapNotificationRequest, mapLastNotificationRequest, fieldKeys, fieldSeparators);
					}
					else
					{
						mlConsolidatedNotificationRequests.add(mapNotificationRequest);
					}
				}
			}

			//
			// Send the consolidated emails
			//
			
			sendNotifications(context, mlConsolidatedNotificationRequests);

			//
			// Delete the spooled requests
			//
			int i = 0;
			String[] strNotificationRequestsRelIds = new String[mlNotificationRequests.size()];
			for (Iterator itrNotificationRequests = mlNotificationRequests.iterator(); itrNotificationRequests.hasNext();)
			{
				Map mapNotificationRequest = (Map) itrNotificationRequests.next();

				strNotificationRequestsRelIds[i] = (String)mapNotificationRequest.get(DomainRelationship.SELECT_ID);
				i++;
			}

			debug("Deleting Notification Request relationship id " + new StringList(strNotificationRequestsRelIds));
			DomainRelationship.disconnect(context, strNotificationRequestsRelIds);

			ContextUtil.commitTransaction(context);
		}
		catch (Exception exp) {
			ContextUtil.abortTransaction(context);
			if (isDebugEnabled) {
				exp.printStackTrace();
			}
			throw new MatrixException(exp);
		}
		finally {
			debug("End:consolidateNotifications");
		}
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
			
			for (int i = 0; i < mlNotificationRequests.size(); i++) {
				
				Map mapNotificationRequest = (Map) mlNotificationRequests.get(i);

				StringList slToList = toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_STATIC_TO_LIST), EMAIL_ADDRESS_SEPARATOR);
				String strFromAgent = (String) mapNotificationRequest.get(SELECT_ATTRIBUTE_FROM_AGENT);
				StringList slCcList = toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_STATIC_CC_LIST), EMAIL_ADDRESS_SEPARATOR);
				StringList slBccList =  toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_STATIC_BCC_LIST), EMAIL_ADDRESS_SEPARATOR);
				String strSubject = (String)mapNotificationRequest.get(SELECT_ATTRIBUTE_SUBJECT_TEXT);
				String strBody = (String)mapNotificationRequest.get(SELECT_ATTRIBUTE_BODY_TEXT);
				String strMsg = (String)mapNotificationRequest.get(SELECT_ATTRIBUTE_BODY_HTML);
				StringList slAttachments = toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_ATTACHMENTS), EMAIL_ATTACHMENT_SEPARATOR);
				String strNotificationType = (String) mapNotificationRequest.get(SELECT_ATTRIBUTE_NOTIFICATION_TYPE); 
				String strNotificationName = (String)mapNotificationRequest.get(SELECT_ATTRIBUTE_NOTIFICATION_NAME);
				StringList slReplyTo = toStringList((String)mapNotificationRequest.get(SELECT_ATTRIBUTE_REPLY_TO), EMAIL_ADDRESS_SEPARATOR);
				
				debug("sendNotification To:" + slToList + "fromAgent:" + strFromAgent + " Cc:" + slCcList + " Bcc:" + slBccList + " Subject:" + strSubject + " Attachments:" + slAttachments + " strNotificationName:" +strNotificationName);
				
//				sendNotification(context, fromAgent, slToList, slCcList, slBccList, strSubject, strBody, strMsg, slAttachments);
				sendNotification(context, slToList, slCcList, slBccList, strSubject, strBody, strMsg, strFromAgent, slReplyTo, slAttachments, strNotificationType);
			}
		}
		catch (Exception e) {
			throw new MatrixException(e);
		}
	}

/*
	protected void sendNotification(Context context, StringList to, StringList cc, 
									StringList bcc, String subject, String body,
									String message, StringList attachments) throws FrameworkException 
	{
		MailUtil.sendNotification(context, to, cc, bcc, subject, null, null, message, null, null, attachments, null);
	}
*/
	protected void sendNotification(Context context, StringList to, StringList cc, 
			StringList bcc, String subject, String messageText, String messageHTML,
			String fromAgent, StringList replyTo, StringList attachments, String notifyType) throws Exception 
	{
		emxNotificationUtil_mxJPO.sendJavaMail(context, to, cc, bcc, subject, messageText, messageHTML,
												fromAgent, replyTo, attachments, notifyType);
	}

	/**
	 * This method Converts a string with values separated using given separator to StringList
	 *
	 * @param strValue The value
	 * @param strSeparator The separator
	 * @return The StringList object with each element being individual values. If strValue is null or "" it will be empty list
	 * @since R210
	 */
	protected StringList toStringList(String strValue, String strSeparator) {
		StringList slResult = new StringList();

		if (!(strValue == null || "".equals(strValue.trim())))
		{
			strValue = strValue.trim();
			slResult = FrameworkUtil.split(strValue, strSeparator);
		}

		return slResult;
	}

	/**
	 * This method consolidates the given Notification Requests assuming these are similar notifications.
	 * For now it merges the Attachments.
	 *
	 * @param mapSrcNotificationRequest Map of notification request which is to be merged
	 * @param mapDestNotificationRequest Map of notification request in which to be merged.
	 * This will be modified as a result.
	 * @return void
	 * @since R210
	 */
	protected void mergeSimilarNotification(Map mapSrcNotificationRequest, Map mapDestNotificationRequest, String[] fieldKeys, String[] fieldSeparators)
	{
		if (mapDestNotificationRequest == null || mapSrcNotificationRequest == null) {
			return;
		}

		for (int i = 0, j = 0; i < fieldKeys.length && j < fieldSeparators.length; i++, j++) {
			String strDest = (String)mapDestNotificationRequest.get(fieldKeys[i]);
			String strSrc = (String)mapSrcNotificationRequest.get(fieldKeys[i]);
			if (strDest == null)
		{
				strDest = "";
		}
			if (strSrc == null)
		{
				strSrc = "";
			}
			if (strSrc.length() != 0) {
				if (strDest.length() != 0) {
					strDest += fieldSeparators[j];
		}
				strDest += strSrc;

				mapDestNotificationRequest.put(fieldKeys[i], strDest);
			}
		}
	}

	/**
	 * This method checks if the given two notifications are similar by comparing the to, cc and bcc lists.
	 * It assumes they have the same subject.
	 *
	 * @param mapNotificationRequest1 The first notification request
	 * @param mapNotificationRequest2 The second notification request
	 * @return true if these are similar request
	 * @since R210
	 */
	protected boolean isSimilarNotificationRequest(Map mapNotificationRequest1, Map mapNotificationRequest2)
	{
		if (isSimilarValue(SELECT_ATTRIBUTE_SUBJECT_TEXT, mapNotificationRequest1, mapNotificationRequest2) &&
			isSimilarValue(SELECT_ATTRIBUTE_STATIC_TO_LIST, mapNotificationRequest1, mapNotificationRequest2) &&
			isSimilarValue(SELECT_ATTRIBUTE_STATIC_CC_LIST, mapNotificationRequest1, mapNotificationRequest2) &&
			isSimilarValue(SELECT_ATTRIBUTE_STATIC_BCC_LIST, mapNotificationRequest1, mapNotificationRequest2) &&
			isSimilarValue(SELECT_ATTRIBUTE_NOTIFICATION_TYPE, mapNotificationRequest1, mapNotificationRequest2))
		{
			return true;
		}

		return false;
	}

	/**
	 * This method checks whether the given values corresponding to given key are similar
	 *
	 * @param objKey the key to be used to get the value from the map
	 * @param map1 first map
	 * @param map2 second map
	 * @return true if the values are similar or null Similarity is checked using equals method
	 * @since R210
	 */
	protected boolean isSimilarValue(Object objKey, Map map1, Map map2)
	{
		Object objValue1 = map1.get(objKey);
		Object objValue2 = map2.get(objKey);

		if (objValue1 == null && objValue2 == null)
		{
			return true;
		}

		if (objValue1 != null && objValue1.equals(objValue2))
		{
			return true;
		}

		return false;
	}

	/**
	 * This method sorts the recipients addresses in the map, which are separated by
	 * EMAIL_ADDRESS_SEPARATOR and kept against key given by strSelectKey
	 *
	 * @param mapNotificationRequest The map containing Notification Request information
	 * @param strSelectKey The key to be used to get the recipient addresses from map
	 * @return void
	 * @since R210
	 */
	protected void sortRecipientAddress(Map mapNotificationRequest, String strSelectKey) {
		String strRecipient = (String)mapNotificationRequest.get(strSelectKey);
		if (strRecipient != null && !"".equals(strRecipient.trim())) {
			strRecipient = strRecipient.trim();

			StringList slRecipient = FrameworkUtil.split(strRecipient, EMAIL_ADDRESS_SEPARATOR);
			slRecipient.sort();

			strRecipient = FrameworkUtil.join(slRecipient, EMAIL_ADDRESS_SEPARATOR);
			mapNotificationRequest.put(strSelectKey, strRecipient);
		}
	}

	/**
	 * This method checks if the given two notifications are similar by comparing the Notification Name.
	 * It assumes they have the same subject.
	 *
	 * @param mapNotificationRequest1 The first notification request
	 * @param mapNotificationRequest2 The second notification request
	 * @return true if these are similar request
	 * @since R210
	 */
	protected boolean isSimilarNotificationName(Map mapNotificationRequest1, Map mapNotificationRequest2)
	{
		if (isSimilarValue(SELECT_ATTRIBUTE_NOTIFICATION_NAME, mapNotificationRequest1, mapNotificationRequest2))
		{
			return true;
		}

		return false;
	}

	/**
	 * This method processes the notification requests where Notification Name are same.
	 * It invokes application specific consolidation JPO or Notification object if present else
	 * invokes default consolidation program
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param mlSimilarNotifications - MapList containing Map of similar Notification Request information
	 * @throws MatrixException if the operation fails
	 * @return void
	 * @since R210
	 */
	protected void processSimilarRequests(Context context, MapList mlSimilarNotifications) throws MatrixException
	{
		try {
			debug("Begin:processSimilarRequests");
			// Get object list information from packed arguments
			if(mlSimilarNotifications == null || mlSimilarNotifications.isEmpty())
			{
				return ;
			}

			// Get attribute information from above maplist
			Map mapSimilarNotifications = (Map) mlSimilarNotifications.get(0);
			String strNotificationName = (String)mapSimilarNotifications.get(SELECT_ATTRIBUTE_NOTIFICATION_NAME);

			StringList slBusSelect = new StringList(SELECT_ID);

			//Queries on the given criteria into a map list containing a mapping of selectable names to values
			MapList mlNotification = DomainObject.findObjects(
													context,  									// The current context object
													TYPE_NOTIFICATION,  						// The type pattern
													strNotificationName,						// The name pattern
													QUERY_WILDCARD,								// The revision pattern
													QUERY_WILDCARD,								// The owner pattern
													QUERY_WILDCARD, 							// The vault pattern
													"", 										// The where expression
													false,										// expandType
													slBusSelect);								// The select clause

			if (mlNotification == null || mlNotification.isEmpty())
			{
                String[] formatArgs = {strNotificationName};
                String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.SpoolBase.NotificationNameNotFound",formatArgs);
				throw new MatrixException(message);
			}

			//
			// Find application specific consolidation JPO
			//
			Map mapNotificationInfo = (Map)mlNotification.get(0);
			String strObjectIdNotification = (String)mapNotificationInfo.get(SELECT_ID);
			DomainObject domNotificationSpool = DomainObject.newInstance(context, strObjectIdNotification);
			String strConsolidationJPO = domNotificationSpool.getInfo(context, SELECT_ATTRIBUTE_CONSOLIDATION_JPO);

			String strProgramName = DEFAULT_CONSOLIDATION_PROGRAM;
            String strMethodName = DEFAULT_CONSOLIDATION_METHOD;

			if (strConsolidationJPO != null && !"".equals(strConsolidationJPO))
			{
				strConsolidationJPO = FrameworkUtil.findAndReplace(strConsolidationJPO, "JPO", "");
				strConsolidationJPO = strConsolidationJPO.trim();
				StringList slConsolidationJPO = FrameworkUtil.split(strConsolidationJPO, CONSOLIDATION_JPO_SEPERATOR);
				if (slConsolidationJPO.size() == 2) {
					strProgramName = (String)slConsolidationJPO.get(0);
					strMethodName = (String)slConsolidationJPO.get(1);
				}
			}

            String className = MqlUtil.mqlCommand(context, "print program $1 select $2 dump $3", strProgramName, "classname", "|");
            
			// invoke the program or function if both are defined
			if((strProgramName != null && !"".equals(strProgramName)) &&
					(strMethodName != null && !"".equals(strMethodName)))
			{
				debug("Invoking consolidation program " + strProgramName + ":" + strMethodName);
				String[] cArgs = new String[0];
				String[] mArgs = JPO.packArgs(mlSimilarNotifications);
				JPO.invokeLocal(context, className, new String[0], strMethodName, mArgs);
			}
			else
			{
				debug("Not Invoking consolidation program!");
			}
		}
		catch (Throwable e) {
			if (isDebugEnabled) {
				debug("Exception: "+e.toString());
				e.printStackTrace();
			}
			throw new MatrixException(e);
		}
		finally {
			debug("End:processSimilarRequests");
		}
	}
}
