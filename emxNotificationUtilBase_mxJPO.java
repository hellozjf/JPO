/**
 *   Copyright (c) 1992-2016 Dassault Systemes.
 *   All Rights Reserved.
 *
 */

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Collections;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.util.MxMessage;

import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.IconMail;
import matrix.db.JPO;
import matrix.db.JPOSupport;
import matrix.db.Query;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectItr;
import matrix.db.RelationshipWithSelectList;
import matrix.util.MatrixException;
import matrix.util.StringItr;
import matrix.util.StringList;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.QueryIterator;

/**
 * The <code>emxNotificationUtilBase</code> class contains static methods for sending email.
 *
 * @version AEF 10.0.0.0
 */

public class emxNotificationUtilBase_mxJPO  extends emxUtil_mxJPO {
	/** Lock object for synchronization.*/
	static private final Object _lock = new Object();
	/** StringList which holds attribute actual names mapped from symbolic names.*/
	static protected StringList _sl;
	/** Actual Notification type name mapped from symbolic name.*/
	static protected String _notificationType;
	/** Actual administration vault name mapped from symbolic name.*/
	static protected String _adminVault;
	/** Actual Active state name mapped from symbolic name.*/
	static protected String _stateActive;
	/** Vault pattern used for search of notification objects.*/
	static protected String _vaultPattern;

	/** programMap holds treemap values. */
	static protected Hashtable programMap = new Hashtable();
	/** ageMap holds timeout values. */
	static protected Hashtable ageMap = new Hashtable();
	/** _componentAge is a long value. */
	static long _componentAge = 3600 * 1000;
	/** Create new instance of emxMailUtil class. */
	static protected emxMailUtil_mxJPO mailUtil = null;

	/** Prefix for bundle name.*/
	static final protected String _bundlePrefix = "emx";
	/** Suffix for bundle name.*/
	static final protected String _bundleSuffix = "StringResource";
	static final protected String iconMail = "IconMail";
	static final protected String email = "Email";
	static final protected String both = "Both";

	/** Constants for the delimiters and token types.*/
	private static final String SELECT = "<";
	private static final String END_SELECT = ">";
	private static final String MACRO = "{";
	private static final String END_MACRO = "}";
	private static final String DELIMITER_START = "$";
	private static final String DELIMITER_TYPE = SELECT + MACRO;
	private static final String TEXT = "text";
	private static final String USER_LIST_DELIMITER = "~";
	private static final String INPUT_LIST_DELIMITER = ";";
	private static final String INPUT_REPLYTO_DELIMITER = "|";
	private static final String OUTPUT_LIST_DELIMITER = ", ";
	private static final String OUTPUT_NO_VALUE = "";
	private static final String OUTPUT_EMPTY_VALUE = "";

	/** object and relationship constants.*/
	private static final String OBJECT = "object";
	private static final String RELATIONSHIP = "relationship";

	/** Create a static mapping from token type to end delimiter string.*/
	private static final Map delimiters;

	static {
		Map m = new HashMap(2);
		m.put(SELECT, END_SELECT);
		m.put(MACRO, END_MACRO);
		delimiters = Collections.unmodifiableMap(m);
	}

	public emxNotificationUtilBase_mxJPO(Context context) throws Exception  {
		this(context, null);
	}
	

	/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	public emxNotificationUtilBase_mxJPO(Context context, String[] args)
	throws Exception {
		super(context, args);

		synchronized(_lock)
		{
			if (_sl == null)
			{
				// Get admin names that are used in this program from property names.
				String[] s = new String[18];
				s[0]  = "type_Notification";
				s[1]  = "attribute_Filter";
				s[2]  = "attribute_FromAgent";
				s[3]  = "attribute_StaticToList";
				s[4]  = "attribute_StaticCcList";
				s[5]  = "attribute_StaticBccList";
				s[6]  = "attribute_DynamicToList";
				s[7]  = "attribute_DynamicCcList";
				s[8]  = "attribute_DynamicBccList";
				s[9]  = "attribute_SubjectText";
				s[10] = "attribute_BodyText";
				s[11] = "attribute_BodyHTML";
				s[12] = "attribute_RegisteredSuite";
				s[13] = "attribute_Attachments";
				s[14] = "attribute_URLSuffix";
				s[15] = "attribute_ReplyTo";
				s[16] = "attribute_PreprocessJPO";
				s[17] = "vault_eServiceAdministration";

				ArrayList adminNames = getAdminNameFromProperties(context, s);

				_notificationType = (String)adminNames.get(0);
				_adminVault = (String)adminNames.get(17);

				// Get all the information about trigger objects found.
				StringList sl = new StringList(17);
				sl.addElement("current");
				sl.addElement("attribute[" +  (String)adminNames.get(1) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(2) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(3) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(4) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(5) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(6) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(7) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(8) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(9) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(10) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(11) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(12) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(13) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(14) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(15) + "].value");
				sl.addElement("attribute[" +  (String)adminNames.get(16) + "].value");
				_sl = sl.unmodifiableCopy();

				_vaultPattern = _adminVault;
				if (_vaultPattern == null || _vaultPattern.length() == 0)
					_vaultPattern = "*";

				// Get state names from properties
				s = new String[2];
				s[0]  = "policy_BusinessRule";
				s[1]  = "state_Active";
				ArrayList stateNames = getStateNamesFromProperties(context, s);
				_stateActive = (String)stateNames.get(0);
				mailUtil = new emxMailUtil_mxJPO(context, null);
				// Get cache age from properties file.
				String propValue = MessageUtil.getMessage(context, "emxNavigator.UICache.ComponentAge", null, null, null, "emxSystem");
				try
				{
					_componentAge = Long.parseLong(propValue) * 1000;
				}
				catch (Exception e)
				{
					_componentAge = 3600 * 1000;
				}
			}
		}
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an int 0, status code.
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	public int mxMain(Context context, String[] args) throws Exception {
		if (true) {
			String languageStr = context.getSession().getLanguage();
			String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.Invocation", new Locale(languageStr));
			exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.NotificationUtil", new Locale(languageStr));
			throw new Exception(exMsg);
		}
		return 0;
	}

	/**
	 * This method creates a mail notification based on an object id.
	 * Only use this method if invoking from beans.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *        objectId - a String holding the id of an object to be used for
	 *          evaluating selects embedded in the subject and message strings
	 *        notificationName - a String holding the name of the Notification object
	 *        payload - a Map containing information to pass on to helper JPO methods
	 * @return an int 0 status code
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	public static int objectNotificationFromMap(Context context, String[] args)
	throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		Map map = (Map) JPO.unpackArgs(args);

		objectNotification(context,
				(String) map.get("objectId"),
				(String) map.get("notificationName"),
				(Map) map.get("payload"));
		return 0;
	}

	/**
	 * This method creates a mail notification based on an object id.
	 * Only use this method if calling from triggers or tcl.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectId - a String holding the id of an object to be used for
	 *          evaluating selects embedded in the subject and message strings
	 *        notificationName - a String holding the name of the Notification object
	 * @return an int 0 status code
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	public static int objectNotification(Context context, String[] args)
	throws Exception {
		if (args == null || args.length < 2) {
			throw (new IllegalArgumentException());
		}
		int index = 0;
		String objectId = args[index++];
		String notificationName = args[index++];

		objectNotification(context, objectId, notificationName, null);
		return 0;
	}

	/**
	 * This method creates a mail notification based on an object id.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId a String holding the id of an object to be used for
	 *          evaluating selects embedded in the subject and message strings
	 * @param notificationName a String holding the name of the Notification object
	 * @param payload a Map containing information to pass on to helper JPO methods
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	public static void objectNotification(Context context,
			String objectId, String notificationName, Map payload)
	throws Exception {

		if(objectId != null && objectId.length() > 0)
		{
			String strResult = "";
			try
			{
				//replace MQL call with Busselects
				boolean isExists =  doesBusinessObjectExist(context, objectId);
				if(isExists)
					objectRelationshipNotification(context, objectId, OBJECT, notificationName, payload);
				else
				{
					strResult =  MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3",objectId,"name","|");					
					objectRelationshipNotification(context, objectId, RELATIONSHIP, notificationName, payload);
				}

			}
			catch(Exception e)
			{
				//Flow comes to catch block with the ID is neither an objectid nor a relationshipid IR-422193-V6R2013x
				return;
			}
		}
	}

	public static boolean doesBusinessObjectExist(Context context, String busId)throws Exception
	{
		boolean returnValue = false;
		try{
			String[] objIDs = new String[]{busId};

			StringList busSelect = new StringList();
			busSelect.add(DomainConstants.SELECT_EXISTS);

			MapList mapList = DomainObject.getInfo(context, objIDs, busSelect);

			Map objectInformation = (Map) mapList.get(0);

			returnValue   = Boolean.parseBoolean((String) objectInformation.get(DomainConstants.SELECT_EXISTS));
		}
		catch(Exception e){}

		return returnValue;
	}



	/**
	 * This method creates a mail notification based on a relationship id.
	 * Only use this method if invoking from beans.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *        relId - a String holding the id of a relationship to be used for
	 *          evaluating selects embedded in the subject and message strings
	 *        notificationName - a String holding the name of the Notification object
	 *        payload - a Map containing information to pass on to helper JPO methods
	 * @return an int 0 status code
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	public static int relationshipNotificationFromMap(Context context, String[] args)
	throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		Map map = (Map) JPO.unpackArgs(args);

		relationshipNotification(context,
				(String) map.get("relId"),
				(String) map.get("notificationName"),
				(Map) map.get("payload"));
		return 0;
	}

	/**
	 * This method creates a mail notification based on a relationship id.
	 * Only use this method if calling from triggers or tcl.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        relId - a String holding the id of a relationship to be used for
	 *          evaluating selects embedded in the subject and message strings
	 *        notificationName - a String holding the name of the Notification object
	 * @return an int 0 status code
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	public static int relationshipNotification(Context context, String[] args)
	throws Exception {
		if (args == null || args.length < 2) {
			throw (new IllegalArgumentException());
		}
		int index = 0;
		String relId = args[index++];
		String notificationName = args[index++];

		relationshipNotification(context, relId, notificationName, null);

		return 0;
	}

	/**
	 * This method creates a mail notification based on a relationship id.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param relId a String holding the id of a relationship to be used for
	 *          evaluating selects embedded in the subject and message strings
	 * @param notificationName a String holding the name of the Notification object
	 * @param payload a Map containing information to pass on to helper JPO methods
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	public static void relationshipNotification(Context context,
			String relId, String notificationName, Map payload)
	throws Exception {

		if(relId != null && relId.length() > 0)
		{
			String strResult = "";
			try
			{
				strResult =  MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3",relId,"exists","|");				
				if(strResult.equalsIgnoreCase("TRUE"))
					objectRelationshipNotification(context, relId, OBJECT, notificationName, payload);
				else
				{
					strResult = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3",relId,"name","|");					
					objectRelationshipNotification(context, relId, RELATIONSHIP, notificationName, payload);
				}
			}
			catch(Exception e)
			{
				throw e;
			}
		}
	}


	/**
	 * This method creates a mail notification based on a either an object or relationship id.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be used for
	 *          evaluating selects embedded in the subject and message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param notificationName a String holding the name of the Notification object
	 * @param payload a Map containing information to pass on to helper JPO methods
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	public static void objectRelationshipNotification(Context context,
			String id, String idType, String notificationName, Map payload)
	throws Exception {
		StringTokenizer tokens = null;
		String value;
		BusinessObjectWithSelectList notificationWithSelectList = (BusinessObjectWithSelectList)programMap.get(notificationName);
		synchronized(_lock) {
			long time = System.currentTimeMillis();
			if (notificationWithSelectList != null) {
				Long timeout =  (Long)ageMap.get(notificationName);
				if (timeout != null && timeout.longValue() < time)
					notificationWithSelectList = null;
			}
			if (notificationWithSelectList == null) {
				// search for all the notification business objects with the name provided
				notificationWithSelectList = new BusinessObjectWithSelectList(1);
				Query tempQuery = new Query("");
				tempQuery.setBusinessObjectType(_notificationType);
				tempQuery.setBusinessObjectName(notificationName);
				tempQuery.setBusinessObjectRevision("*");
				tempQuery.setVaultPattern(_vaultPattern);
				try
				{
					ContextUtil.startTransaction(context,false);
					QueryIterator qItr = tempQuery.getIterator(context,_sl,(short)1000);
					try {
						while(qItr.hasNext())
							notificationWithSelectList.addElement(qItr.next());
					} finally {
						qItr.close();
					}
					ContextUtil.commitTransaction(context);
				}
				catch(Exception ex)
				{
					ContextUtil.abortTransaction(context);
					throw new Exception(ex.toString());
				}
				programMap.put(notificationName, notificationWithSelectList);
				ageMap.put(notificationName, new Long(time + _componentAge));
			}
		}

		for (int j = 0; j < notificationWithSelectList.size(); j++)
		{
			BusinessObjectWithSelect bows = notificationWithSelectList.getElement(j);
			if (bows.getSelectData((String)_sl.elementAt(0)).compareTo(_stateActive) == 0)
			{
				String filter = bows.getSelectData((String)_sl.elementAt(1));

				// Evaluate filter to determine if email should be sent
				if (evaluateFilter(context, id, idType, notificationName, payload, filter) != 0) {
					continue;
				}

				value = bows.getSelectData((String)_sl.elementAt(2));
				String fromAgent = inputOrJPOToString(context, id, idType, notificationName, payload, value);

				value = bows.getSelectData((String)_sl.elementAt(3));
				StringList toList = inputListOrJPOToStringList(context, id, idType, notificationName, payload, value);

				value = bows.getSelectData((String)_sl.elementAt(4));
				StringList ccList = inputListOrJPOToStringList(context, id, idType, notificationName, payload, value);

				value = bows.getSelectData((String)_sl.elementAt(5));
				StringList bccList = inputListOrJPOToStringList(context, id, idType, notificationName, payload, value);

				value = bows.getSelectData((String)_sl.elementAt(6));
				StringList toSelectList = inputListOrJPOToStringList(context, id, idType, notificationName, payload, value);

				value = bows.getSelectData((String)_sl.elementAt(7));
				StringList ccSelectList = inputListOrJPOToStringList(context, id, idType, notificationName, payload, value);

				value = bows.getSelectData((String)_sl.elementAt(8));
				StringList bccSelectList = inputListOrJPOToStringList(context, id, idType, notificationName, payload, value);

				String subjectKey = bows.getSelectData((String)_sl.elementAt(9));
				String messageKey = bows.getSelectData((String)_sl.elementAt(10));
				String messageHTMLKey = bows.getSelectData((String)_sl.elementAt(11));
				String basePropFile = bows.getSelectData((String)_sl.elementAt(12));
				if (basePropFile != null && basePropFile.length() > 0) {
					String temp ="";
					temp=EnoviaResourceBundle.getProperty(context, "eServiceSuite"+ bows.getSelectData((String)_sl.elementAt(12)).replaceAll("\\\\s", "") + ".StringResourceFileId").trim();
					basePropFile = temp.toString();
				}

				value = bows.getSelectData((String)_sl.elementAt(13));
				StringList idSelectList = inputListOrJPOToStringList(context, id, idType, notificationName, payload, value);

				value = bows.getSelectData((String)_sl.elementAt(14));
				String urlSuffix = inputOrJPOToString(context, id, idType, notificationName, payload, value);

				StringList replyTo = null;
				value = bows.getSelectData((String)_sl.elementAt(15));
				if (value != null && value.length() > 0) {
					tokens = new StringTokenizer(value, INPUT_REPLYTO_DELIMITER);
					replyTo = new StringList();
					while (tokens.hasMoreTokens()) {
						replyTo.addElement(tokens.nextToken().trim());
					}
				}

				String preprocessJPOMethod = bows.getSelectData((String)_sl.elementAt(16));

				/* From the Tolist obtained search to get the pipe(|) delimitors so as to list the personList
                 based on their notification type. The convention follwed here is, all the persons in the tolist upto the 1st pipe
                 are the person to be notified through Email and in between 1st pipe and 2nd pipe are the persons to be notified throug IconMail and
                 persons follwing the 2nd pipe are to be notified through both(Email and IconMail) the ways */


				HashMap finalMap = getRefineList(context, toList);
				StringList bothToList = (StringList)finalMap.get(both);
				StringList iconMailToList = (StringList)finalMap.get(iconMail);
				StringList emailToList = (StringList)finalMap.get(email);


				finalMap = getRefineList(context, ccList);
				StringList bothCcList = (StringList)finalMap.get(both);
				StringList iconMailCcList = (StringList)finalMap.get(iconMail);
				StringList emailCcList = (StringList)finalMap.get(email);


				finalMap = getRefineList(context, bccList);
				StringList bothBccList = (StringList)finalMap.get(both);
				StringList iconMailBccList = (StringList)finalMap.get(iconMail);
				StringList emailBccList = (StringList)finalMap.get(email);

				finalMap = getRefineList(context, toSelectList);
				StringList bothToSelectList = (StringList)finalMap.get(both);
				StringList iconMailToSelectList = (StringList)finalMap.get(iconMail);
				StringList emailToSelectList = (StringList)finalMap.get(email);

				finalMap = getRefineList(context, ccSelectList);
				StringList bothCcSelectList = (StringList)finalMap.get(both);
				StringList iconMailCcSelectList = (StringList)finalMap.get(iconMail);
				StringList emailCcSelectList = (StringList)finalMap.get(email);

				finalMap = getRefineList(context, bccSelectList);
				StringList bothBccSelectList = (StringList)finalMap.get(both);
				StringList iconMailBccSelectList = (StringList)finalMap.get(iconMail);
				StringList emailBccSelectList = (StringList)finalMap.get(email);

				StringList finalIdList = combineStaticAndDynamicLists(context, id, idType, null, idSelectList, false);


				// Build up final to Both list
				StringList finalToBothList = combineStaticAndDynamicLists(context, id, idType, bothToList, bothToSelectList, true);
				// Build up final cc Both list
				StringList finalCcBothList = combineStaticAndDynamicLists(context, id, idType, bothCcList, bothCcSelectList, true);
				// Build up final bcc Both list
				StringList finalBccBothList = combineStaticAndDynamicLists(context, id, idType, bothBccList, bothBccSelectList, true);


				// Build up final to Email list
				StringList finalToEmailList = combineStaticAndDynamicLists(context, id, idType, emailToList, emailToSelectList, true);
				// Build up final cc Email list
				StringList finalCcEmailList = combineStaticAndDynamicLists(context, id, idType, emailCcList, emailCcSelectList, true);
				// Build up final bcc Email list
				StringList finalBccEmailList = combineStaticAndDynamicLists(context, id, idType, emailBccList, emailBccSelectList, true);

				// Build up final to IconMail list
				StringList finalToIconMailList = combineStaticAndDynamicLists(context, id, idType, iconMailToList, iconMailToSelectList, true);
				// Build up final cc IconMail list
				StringList finalCcIconMailList = combineStaticAndDynamicLists(context, id, idType, iconMailCcList, iconMailCcSelectList, true);
				// Build up final bcc IconMail list
				StringList finalBccIconMailList = combineStaticAndDynamicLists(context, id, idType, iconMailBccList, iconMailBccSelectList, true);

				finalToEmailList.addAll(finalToBothList);
				finalToIconMailList.addAll(finalToBothList);

				finalCcEmailList.addAll(finalCcBothList);
				finalCcIconMailList.addAll(finalCcBothList);

				finalBccEmailList.addAll(finalBccBothList);
				finalBccIconMailList.addAll(finalBccBothList);

				String from = "";
				if (fromAgent != null && fromAgent.trim().length() > 0) {
					if (fromAgent.startsWith("$<person_")) {
						from = substituteValues(context, fromAgent);
					} else {
						from = substituteValues(context, fromAgent, id, idType, null);
					}
				}

				// Evaluate selects in replyTo
				if (replyTo != null && replyTo.size() > 1) {
					String user = (String) replyTo.elementAt(0);
					String personal = (String) replyTo.elementAt(1);
					if (user != null && user.trim().length() > 0) {
						if (user.startsWith("$<person_")) {
							replyTo.setElementAt(substituteValues(context, user), 0);
						} else {
							replyTo.setElementAt(substituteValues(context, user, id, idType, null), 0);
						}
					}
					if (personal != null && personal.trim().length() > 0) {
						replyTo.setElementAt(substituteValues(context, personal, id, idType, null), 1);
					}
				}

				// Pass results to sendNotification through  email
				if(finalToEmailList.size()>0 || finalCcEmailList.size()>0 || finalBccEmailList.size()>0)
				{
					sendNotification(context, id, idType, notificationName, payload, finalToEmailList, finalCcEmailList, finalBccEmailList,
							subjectKey, messageKey, messageHTMLKey, finalIdList, basePropFile, urlSuffix,
							from, replyTo, preprocessJPOMethod, email);
				}

				// Pass results to sendNotification through  IconMail
				if(finalToIconMailList.size()>0 || finalCcIconMailList.size()>0 || finalBccIconMailList.size()>0)
				{
					sendNotification(context, id, idType, notificationName, payload, finalToIconMailList, finalCcIconMailList, finalBccIconMailList,
							subjectKey, messageKey, messageHTMLKey, finalIdList, basePropFile, urlSuffix,
							from, replyTo, preprocessJPOMethod, iconMail);
				}

			}
		}
	}

	/**
	 * This method creates one or more mail notification based on an object id.
	 * The object id passed in will be expanded to get parent id's.
	 * The parent ids will be used to create mail notifications.
	 * Only use this method if calling from triggers or tcl.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectId - id of an object that we want to expand.  From this we will
	 *          check the ids from the expand to be used for
	 *          evaluating selects embedded in the subject and message strings
	 *        notificationName - a String holding the name of the Notification object
	 *        relationshipPattern - relationship to expand on
	 *        typePattern - type to expand to
	 *        toDirection - expand to direction
	 *        fromDirection - expand from direction
	 * @return an int 0 status code
	 * @throws Exception if the operation fails
	 * @since AEF R211
	 */

	public static int objectParentsNotification(Context context, String[] args)
	throws Exception {
		if (args == null || args.length < 2) {
			throw (new IllegalArgumentException());
		}
		int index = 0;
		String objectId = args[index++];
		String notificationName = args[index++];
		String relationshipPattern = PropertyUtil.getSchemaProperty(context, args[index++]);
		String typePattern = PropertyUtil.getSchemaProperty(context, args[index++]);
		boolean toDirection = "true".equals((String)args[index++])? true:false;
		boolean fromDirection = "true".equals((String)args[index++])? true:false;
		short level = 1;

		StringList objectSelects = new StringList(1);
		objectSelects.add("id");

		DomainObject domainObject = DomainObject.newInstance(context, objectId);

		// expand from parent
		MapList mapList = domainObject.getRelatedObjects(
				context,             // context.
				relationshipPattern, // relationship pattern.
				typePattern,         // type pattern.
				objectSelects,       // business object selectables.
				null,                // relationship selectables.
				toDirection,         // expand to direction.
				fromDirection,       // expand from direction.
				level,               // level
				null,                // object where clause
				null,
				0,
				null,
				null,
				null);

		Iterator itr = mapList.iterator();
		while (itr.hasNext())
		{
			Map map = (Map) itr.next();
			String id = (String) map.get("id");

			objectNotification(context, id, notificationName, null);
		}
		return 0;
	}

	/**
	 * This method creates a mail notification for the specified users.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *        objectId - a String holding the id of an object to be used for
	 *          evaluating selects embedded in the subject and message strings,
	 *          and also to send with the notification
	 *        toList - a StringList containing the list of users to notify
	 *        ccList - a StringList containing the list of users to cc
	 *        subjectKey - a String that contains the notification subject key
	 *        messageKey - a String that contains the notification message key
	 *        objectIdList - a StringList containing select clauses evaluated to send objects with the notification
	 *        basePropFileName - a String holding the property file to search for keys
	 *        toSelectList - a StringList containing select clauses evaluated to expand the list of users to notify
	 *        ccSelectList - a StringList containing select clauses evaluated to expand the list of users to cc
	 *        filter - a String holding an expression used to determine if the notification should be sent
	 *        urlSuffix - a String holding a suffix to be appended to the embedded URL
	 *        fromAgent - a String holding the from user appearing in the mail
	 *        messageJPOMethod - a String holding the JPO name and method name (colon delimited) used to append to the message
	 *        filterJPOMethod - a String holding the JPO name and method name (colon delimited) used to filter
	 * @return an int 0 status code
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	public static int createNotificationFromMap(Context context, String[] args)
	throws Exception {

		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		Map map = (Map) JPO.unpackArgs(args);

		createNotification(context,
				(String) map.get("objectId"),
				(StringList) map.get("toList"),
				(StringList) map.get("ccList"),
				(String) map.get("subjectKey"),
				(String) map.get("messageKey"),
				(StringList) map.get("objectIdList"),
				(String) map.get("basePropFileName"),
				(StringList) map.get("toSelectList"),
				(StringList) map.get("ccSelectList"),
				(String) map.get("filter"),
				(String) map.get("urlSuffix"),
				(String) map.get("fromAgent"),
				(String) map.get("messageJPOMethod"),
				(String) map.get("filterJPOMethod"));
		return 0;
	}

	/**
	 * This method creates a mail notification for the specified users.
	 * Only use this method if calling from triggers or tcl,
	 * otherwise use the method createNotificationFromMap().
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectId - a String holding the id of an object to be used for
	 *          evaluating selects embedded in the subject and message strings,
	 *          and also to send with the notification if no idSelectList given
	 *        toList - a semicolon separated list of users to notify
	 *        ccList - a semicolon separated list of users to cc
	 *        subjectKey - a String that contains the notification subject key
	 *        messageKey - a String that contains the notification message key
	 *        basePropName - a String the property file to search for keys
	 *        toSelectList - a semicolon separated list containing select clauses evaluated to expand the list of users to notify
	 *        ccSelectList - a semicolon separated list containing select clauses evaluated to expand the list of users to cc
	 *        filter - a String holding an expression used to determine if the notification should be sent
	 *        urlSuffix - a String holding a suffix to be appended to the embedded URL
	 *        fromAgent - a String holding the from user appearing in the mail
	 *        idSelectList - a semicolon separated list containing select clauses evaluated to send objects with the notification
	 *        messageJPOMethod - a String holding the JPO name and method name (colon delimited) used to append to the message
	 *        filterJPOMethod - a String holding the JPO name and method name (colon delimited) used to filter
	 * @return an int 0 status code
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	public static int createNotification(Context context, String[] args)
	throws Exception {
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		int index = 0;
		StringTokenizer tokens = null;

		String objectId = args[index++];

		StringList toList = null;
		if (args.length > index && args[index] != null) {
			tokens = new StringTokenizer(args[index++], INPUT_LIST_DELIMITER);
			toList = new StringList();
			while (tokens.hasMoreTokens()) {
				toList.addElement(tokens.nextToken().trim());
			}
		}
		StringList ccList = null;
		if (args.length > index && args[index] != null) {
			tokens = new StringTokenizer(args[index++], INPUT_LIST_DELIMITER);
			ccList = new StringList();
			while (tokens.hasMoreTokens()) {
				ccList.addElement(tokens.nextToken().trim());
			}
		}

		String subjectKey = null;
		if (args.length > index) {
			subjectKey = args[index++];
		}
		String messageKey = null;
		if (args.length > index) {
			messageKey = args[index++];
		}
		String basePropName = null;
		if (args.length > index) {
			basePropName = args[index++];
		}

		StringList toSelectList = null;
		if (args.length > index && args[index] != null) {
			tokens = new StringTokenizer(args[index++], INPUT_LIST_DELIMITER);
			toSelectList = new StringList();
			while (tokens.hasMoreTokens()) {
				toSelectList.addElement(tokens.nextToken().trim());
			}
		}
		StringList ccSelectList = null;
		if (args.length > index && args[index] != null) {
			tokens = new StringTokenizer(args[index++], INPUT_LIST_DELIMITER);
			ccSelectList = new StringList();
			while (tokens.hasMoreTokens()) {
				ccSelectList.addElement(tokens.nextToken().trim());
			}
		}

		String filter = null;
		if (args.length > index) {
			filter = args[index++];
		}
		String urlSuffix = null;
		if (args.length > index) {
			urlSuffix = args[index++];
		}
		String fromAgent = null;
		if (args.length > index) {
			fromAgent = args[index++];
		}

		StringList idSelectList = null;
		if (args.length > index && args[index] != null) {
			tokens = new StringTokenizer(args[index++], INPUT_LIST_DELIMITER);
			idSelectList = new StringList();
			while (tokens.hasMoreTokens()) {
				idSelectList.addElement(tokens.nextToken().trim());
			}
		}

		String messageJPOMethod = null;
		if (args.length > index) {
			messageJPOMethod = args[index++];
		}
		String filterJPOMethod = null;
		if (args.length > index) {
			filterJPOMethod = args[index++];
		}

		createNotification(context, objectId, toList, ccList, subjectKey,
				messageKey, idSelectList, basePropName,
				toSelectList, ccSelectList, filter, urlSuffix, fromAgent,
				messageJPOMethod, filterJPOMethod);
		return 0;
	}

	/**
	 * This method creates a mail notification for the specified users.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId a String holding the id of an object to be used for
	 *          evaluating selects embedded in the subject and message strings,
	 *          and also to send with the notification if no idSelectList given
	 * @param toList a StringList that contains the list of users to notify
	 * @param ccList a StringList that contains the list of users to cc
	 * @param subjectKey a String that contains the notification subject key
	 * @param messageKey a String that contains the notification message key
	 * @param idSelectList a StringList containing select clauses evaluated to send objects with the notification
	 * @param basePropFile String that is used for the name of property files to search for keys.
	 * @param toSelectList a StringList containing select clauses evaluated to expand the list of users to notify
	 * @param ccSelectList a StringList containing select clauses evaluated to expand the list of users to cc
	 * @param filter a String holding an expression used to determine if the notification should be sent
	 * @param urlSuffix a String holding a suffix to be appended to the embedded URL
	 * @param fromAgent a String holding the from user appearing in the mail
	 * @param messageJPOMethod a String holding the JPO name and method name (colon delimited) used to append to the message
	 * @param filterJPOMethod a String holding the JPO name and method name (colon delimited) used to filter
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	public static void createNotification(Context context,
			String objectId, StringList toList, StringList ccList,
			String subjectKey, String messageKey,
			StringList idSelectList, String basePropFile,
			StringList toSelectList, StringList ccSelectList,
			String filter, String urlSuffix, String fromAgent,
			String messageJPOMethod, String filterJPOMethod)
	throws Exception {

		// Evaluate filter to determine if email should be sent
		if (filter != null && !filter.equals("") && objectId != null
				&& !objectId.equals("")) {
			String expression = substituteValues(context, filter);
			String filterResult = MqlUtil.mqlCommand(context, "evaluate expression $1 on bus $2",expression,objectId);
			if (filterResult.startsWith("FALSE")) {
				return;
			}
		}
		// Build up final to list
		StringList finalToList = combineStaticAndDynamicLists(context, objectId, OBJECT, toList, toSelectList, true);

		// Build up final cc list
		StringList finalCcList = combineStaticAndDynamicLists(context, objectId, OBJECT, ccList, ccSelectList, true);

		// Build up final object id list
		StringList finalIdList = combineStaticAndDynamicLists(context, objectId, OBJECT, null, idSelectList, false);

		// String from = substituteValues(context, fromAgent);
		String from = "";
		if (fromAgent != null && fromAgent.trim().length() > 0
				&& fromAgent.startsWith("$<person_")) {
			from = substituteValues(context, fromAgent);
		} else {
			from = substituteValues(context, fromAgent, objectId, null);
		}

		if (objectId == null || objectId.equals("")) {
			if (finalIdList != null && !finalIdList.isEmpty()) {
				// If no objectId is given, we will assume the first object id
				// in the objectIdList can be used to evaluate selects embedded
				// in the
				// subject and message strings.
				objectId = (String) finalIdList.firstElement();
			}
		} else {
			// If an objectId is given but no objectIdList, we add objectId
			// to the objectIdList.
			if (finalIdList == null) {
				finalIdList = new StringList(1);
			}
			if (finalIdList.size() < 1) {
				finalIdList.add(objectId);
			}
		}

		// Execute filter JPO method to determine if email should be sent
		if (filterJPOMethod != null && !"".equals(filterJPOMethod.trim())) {
			int status = 0;
			int index = filterJPOMethod.indexOf(":");
			String methodName = "";
			String name = "";
			if (index >= 0) {
				methodName = filterJPOMethod.substring(index+1);
				name = filterJPOMethod.substring(0, index);
			}

			try {

				status = JPO.invoke(context, name, new String[0], methodName,(String[]) finalIdList.toArray());

			} catch (Throwable e) {
				System.out
				.println("exception trying to invoke filter JPO method: "
						+ e.toString());
			}
			if (status != 0) {
				return;
			}
		}

		// Pass results to sendNotification
		sendNotification(context, objectId, finalToList, finalCcList, null,
				subjectKey, messageKey, finalIdList, basePropFile, urlSuffix,
				from, messageJPOMethod);
	}

	private static String getClassNameForProg(Context context, String name) throws Exception
	{
		StringBuffer command = new StringBuffer();
		String className = MqlUtil.mqlCommand(context, "print program $1 select $2 dump $3",name,"classname","|");

		return className;
	}

	/**
	 * This method sends a mail notification to the specified users.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId String contains id of the object used to evaluate embedded select clauses
	 *          in the subject and message
	 * @param toList StringList that contains the list of users to notify
	 * @param ccList StringList that contains the list of users to cc
	 * @param bccList StringList that contains the list of users to bcc
	 * @param subjectKey String that contains the notification subject key
	 * @param messageKey String that contains the notification message key
	 * @param objectIdList StringList that contains the ids of objects to send with the notification
	 * @param basePropFile String that is used for the name of property files to search for keys
	 * @param urlSuffix String that contains additional info to append to embedded URLs
	 * @param fromAgent a String holding the from user appearing in the mail
	 * @param messageJPOMethod - a String holding the JPO name and method name (colon delimited) used to append to the message
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	public static void sendNotification(Context context, String objectId,
			StringList toList, StringList ccList, StringList bccList,
			String subjectKey, String messageKey, StringList objectIdList,
			String basePropFile, String urlSuffix, String fromAgent,
			String messageJPOMethod)
	throws Exception {

		// If no toList given, then nobody to notify
		if (toList == null || toList.size() < 1) {
			return;
		}

		// If no subject given, then icon mail won't work
		if (subjectKey == null || subjectKey.equals("")) {
			return;
		}

		if (objectId == null || objectId.equals("")) {
			if (objectIdList != null && !objectIdList.isEmpty()) {
				// If no objectId is given, we will assume the first object id
				// in the objectIdList can be used to evaluate selects embedded in the
				// subject and message strings.
				objectId = (String) objectIdList.firstElement();
			}
		}

		// If the base URL is available, then construct urls which will be
		// appended to the end of the message.
		StringBuffer urlsBuffer = new StringBuffer();
		String baseURL = emxMailUtil_mxJPO.getBaseURL(context, null);
		if (baseURL != null && !"".equals(baseURL)) {
			if (objectIdList != null && objectIdList.size() > 0) {
				Iterator i = objectIdList.iterator();
				while (i.hasNext()) {
					urlsBuffer.append(baseURL);
					urlsBuffer.append("?objectId=");
					urlsBuffer.append((String) i.next());
					if (urlSuffix != null && !"".equals(urlSuffix)) {
						urlsBuffer.append(urlSuffix);
					}
					urlsBuffer.append("\n\n");
				}
			}
		}
		String urls = urlsBuffer.toString();

		// "names" holds the user names and language preferences
		HashMap names = new HashMap();

		// "languages" holds the unique languages
		HashMap languages = new HashMap();

		emxMailUtil_mxJPO.getNamesAndLanguagePreferences(context, names, languages, toList);
		emxMailUtil_mxJPO.getNamesAndLanguagePreferences(context, names, languages, ccList);
		emxMailUtil_mxJPO.getNamesAndLanguagePreferences(context, names, languages, bccList);

		// send one message per language
		Iterator itr = languages.keySet().iterator();
		while (itr.hasNext()) {
			String language = (String) itr.next();
			Locale locale = null;

			if (language.length() > 0) {
				locale = MessageUtil.getLocale(language);
			}

            if (locale == null || locale.equals("")) {
                locale = emxMailUtil_mxJPO.getLocale(context);
            }
			// build the to, cc and bcc lists for this language
			StringList to = emxMailUtil_mxJPO.getNamesForLanguage(toList, names, language);
			StringList cc = emxMailUtil_mxJPO.getNamesForLanguage(ccList, names, language);
			StringList bcc = emxMailUtil_mxJPO.getNamesForLanguage(bccList, names, language);

			// fetch i18n subject and message content
			String subject = null;
			StringBuffer message = new StringBuffer();
			subject = getMessage(context, objectId, subjectKey,
					locale,
					basePropFile);

			Vector<Locale> locales = new Vector<Locale>();
			locales.add(locale);
			if(UIUtil.isNullOrEmpty(language) && emxMailUtil_mxJPO._locales != null){
				locales = emxMailUtil_mxJPO._locales;
			}

			Iterator<Locale> vcItr = locales.iterator();
			if(vcItr == null || !vcItr.hasNext()){
				message.append(urls); //send the generic \ default url 
			} else {
			while(vcItr.hasNext()){
				locale = vcItr.next();
			message.append(getMessage(context, objectId, messageKey,
					locale,
					basePropFile));
			
			message.append("\n");

			message.append(urls);

			if (messageJPOMethod != null && !"".equals(messageJPOMethod.trim())) {
				String msg = "";
				int index = messageJPOMethod.indexOf(":");
				String methodName = "";
				String name = "";
				if (index >= 0) {
					methodName = messageJPOMethod.substring(index+1);
					name = messageJPOMethod.substring(0, index);
				}

				String className = getClassNameForProg(context, name);
				try {
					Class jpo = JPOSupport.newClass(context, className, 0);
					Class[] types = new Class[] {
							context.getClass(),
							StringList.class,
							Locale.class,
							String.class,
							String.class,
							String.class};
					Method method = jpo.getMethod(methodName, types);

					Object[] args = new Object[] {
							context,
							objectIdList,
							locale == null ? getLocale(context) : locale,
									basePropFile,
									baseURL,
									urlSuffix};

					msg = (String)method.invoke(null, args);

				} catch (Exception e) {
					System.out.println("exception trying to invoke message JPO method: " + e.toString());
				}

				if (msg.trim().length()>0) {
					message.append("\n");
					message.append(msg);
				}
			}
				message.append("\n\n");
			}
		}
			//message.append(urls); //move to if else block of vcItr
			// Send the mail message.
			sendMail(context, to, cc, bcc, subject, message.toString(), objectIdList, fromAgent);
		}
	}

	/**
	 * This method sends a mail notification to the specified users.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be
	 *            used for evaluating selects embedded in the subject and
	 *            message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param notificationName a String holding the name of the Notification object
	 * @param payload a Map containing information to pass on to helper JPO methods
	 * @param toList StringList that contains the list of users to notify
	 * @param ccList StringList that contains the list of users to cc
	 * @param bccList StringList that contains the list of users to bcc
	 * @param subjectKey String that contains the notification subject key
	 * @param messageKey String that contains the notification message key
	 * @param messageHTMLKey a String that contains the notification HTML message key
	 * @param objectIdList StringList that contains the ids of objects to send with the notification
	 * @param basePropFile String that is used for the name of property files to search for keys
	 * @param urlSuffix String that contains additional info to append to embedded URLs
	 * @param fromAgent a String holding the from user appearing in the mail
	 * @param replyTo a StringList containing select clauses evaluated to fill out
	 *            the replyTo user and personal information
	 * @param preprocessJPOMethod a String holding the JPO name and method name (colon delimited) used to append to the message
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static void sendNotification(Context context, String id, String idType,
			String notificationName, Map payload,
			StringList toList, StringList ccList, StringList bccList,
			String subjectKey, String messageKey, String messageHTMLKey,
			StringList objectIdList, String basePropFile, String urlSuffix,
			String fromAgent, StringList replyTo, String preprocessJPOMethod)
	throws Exception {
		//Call the overloaded sendNotification with notification type as Both
		sendNotification(context, id, idType, notificationName, payload, toList, ccList, bccList,
				subjectKey, messageKey, messageHTMLKey, objectIdList, basePropFile, urlSuffix,
				fromAgent, replyTo, preprocessJPOMethod, both);
	}
	/**
	 * This method sends a mail notification to the specified users.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be
	 *            used for evaluating selects embedded in the subject and
	 *            message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param notificationName a String holding the name of the Notification object
	 * @param payload a Map containing information to pass on to helper JPO methods
	 * @param toList StringList that contains the list of users to notify
	 * @param ccList StringList that contains the list of users to cc
	 * @param bccList StringList that contains the list of users to bcc
	 * @param subjectKey String that contains the notification subject key
	 * @param messageKey String that contains the notification message key
	 * @param messageHTMLKey a String that contains the notification HTML message key
	 * @param objectIdList StringList that contains the ids of objects to send with the notification
	 * @param basePropFile String that is used for the name of property files to search for keys
	 * @param urlSuffix String that contains additional info to append to embedded URLs
	 * @param fromAgent a String holding the from user appearing in the mail
	 * @param replyTo a StringList containing select clauses evaluated to fill out
	 *            the replyTo user and personal information
	 * @param preprocessJPOMethod a String holding the JPO name and method name (colon delimited) used to append to the message
	 * @param notifyType Notification Type selected by user (email, iconmail, or both)
	 * @throws Exception if the operation fails
	 * @since x+2
	 */

	protected static void sendNotification(Context context, String id, String idType,
			String notificationName, Map payload,
			StringList toList, StringList ccList, StringList bccList,
			String subjectKey, String messageKey, String messageHTMLKey,
			StringList objectIdList, String basePropFile, String urlSuffix,
			String fromAgent, StringList replyTo, String preprocessJPOMethod, String notifyType)
	throws Exception {

		final String subscriptionAgent = PropertyUtil.getSchemaProperty(context, "person_SubscriptionAgent");

		// If no toList given, then nobody to notify
		if ((toList == null || toList.size() < 1) && (ccList == null || ccList.size() < 1) && (bccList == null || bccList.size() < 1) ) {
			return;
		}

		// If no subject given, then icon mail won't work
		if (subjectKey == null || subjectKey.equals("")) {
			return;
		}

		if (id == null || id.equals("")) {
			if (objectIdList != null && !objectIdList.isEmpty()) {
				// If no objectId is given, we will assume the first object id
				// in the objectIdList can be used to evaluate selects embedded in the
				// subject and message strings.
				id = (String) objectIdList.firstElement();
			}
		}

		// If the base URL is available, then construct urls which will be
		// appended to the end of the message.
		StringBuffer urlsBuffer = new StringBuffer();
		String baseURL = emxMailUtil_mxJPO.getBaseURL(context, null);
		if (baseURL != null && !"".equals(baseURL)) {
			if (objectIdList != null && objectIdList.size() > 0) {
				urlsBuffer.append("\n");
				Iterator i = objectIdList.iterator();
				while (i.hasNext()) {

					urlsBuffer.append("\n");

					// Modified for Pub / Sub Relationship Feature
					if(idType.equals("object"))
					{
						urlsBuffer.append(baseURL);
						urlsBuffer.append("?objectId=");
					}
					else
						urlsBuffer.append("relationshipID=");
					urlsBuffer.append((String) i.next());
					if (urlSuffix != null && !"".equals(urlSuffix)) {
						urlsBuffer.append(urlSuffix);
					}
					urlsBuffer.append("\n");
				}
			}
			//Added for bug 356405
			else
			{
				urlsBuffer.append("\n");
				if(idType.equals("object"))
				{
					urlsBuffer.append(baseURL);
					urlsBuffer.append("?objectId=");
					urlsBuffer.append(id);
				}
				else
				{
					urlsBuffer.append("relationshipID=");
					urlsBuffer.append(id);
				}
				urlsBuffer.append("\n");

			}
			//Ended
		}
		// Added for bug 356405
		else
		{
			urlsBuffer.append("\n");
			if(!idType.equals("object"))
			{
				urlsBuffer.append("relationshipID=");
				urlsBuffer.append(id);
			}
			urlsBuffer.append("\n");
		}
		//Ended
		String urls = urlsBuffer.toString();

		// Create the arguments for the JPO method
		Map info = new HashMap();
		info.put("id", id);
		info.put("idType", idType);
		info.put("notificationName", notificationName);
		info.put("payload", payload);
		info.put("from", fromAgent);
		info.put("replyTo", replyTo);
		info.put("objectIdList", objectIdList);
		info.put("bundleName", basePropFile);
		info.put("baseURL", baseURL);
		info.put("urlSuffix", urlSuffix);

		// "names" holds the user names and language preferences
        HashMap nameAndLanguage = new HashMap();

		// "languages" holds the unique languages
		HashMap languages = new HashMap();

        emxMailUtil_mxJPO.getNamesAndLanguagePreferences(context, nameAndLanguage, languages, toList);
        emxMailUtil_mxJPO.getNamesAndLanguagePreferences(context, nameAndLanguage, languages, ccList);
        emxMailUtil_mxJPO.getNamesAndLanguagePreferences(context, nameAndLanguage, languages, bccList);

        // name is the key, frequency is the value 
        HashMap<String,String> nameAndFrequency = new HashMap<String,String>();
        // load the maps
        getNamesAndFrequencyPreferences(context, nameAndFrequency, toList);
        getNamesAndFrequencyPreferences(context, nameAndFrequency, ccList);
        getNamesAndFrequencyPreferences(context, nameAndFrequency, bccList);

        // send one message per language
        Iterator itr = languages.keySet().iterator();
        while (itr.hasNext()) {
            String language = (String) itr.next();
            Locale locale = null;

			if (language.length() > 0) {
				locale = MessageUtil.getLocale(language);
			}

			if (locale == null || locale.equals("")) {
                locale = emxMailUtil_mxJPO.getLocale(context);
			}

			info.put("locale", locale);
			info.put("status", "");

			// build the to, cc and bcc lists for this language
            StringList to = emxMailUtil_mxJPO.getNamesForLanguage(toList, nameAndLanguage, language);
            StringList cc = emxMailUtil_mxJPO.getNamesForLanguage(ccList, nameAndLanguage, language);
            StringList bcc = emxMailUtil_mxJPO.getNamesForLanguage(bccList, nameAndLanguage, language);

			info.put("toList", to);
			info.put("ccList", cc);
			info.put("bccList", bcc);

			// fetch i18n subject and message content
			String subject = inputOrJPOTolocalizedString(context, subjectKey, info, null);

			Vector<Locale> locales = new Vector<Locale>();
			locales.add(locale);
			if(UIUtil.isNullOrEmpty(language) && emxMailUtil_mxJPO._locales != null){
				locales = emxMailUtil_mxJPO._locales;
			}

			Iterator<Locale> vcItr = locales.iterator();
			String messageHTML = "";
			String messageText = "";
			while(vcItr.hasNext()){
				locale=vcItr.next();
				info.put("locale", locale);
				messageHTML = messageHTML + inputOrJPOTolocalizedString(context, messageHTMLKey, info, urls);
				messageText = messageText + inputOrJPOTolocalizedString(context, messageKey, info, urls);
			//fromAgent = "Subscription Agent";
			if((notifyType.equalsIgnoreCase(email)) && (fromAgent.equalsIgnoreCase(subscriptionAgent) || (replyTo!=null && ((String) replyTo.elementAt(0)).equalsIgnoreCase(subscriptionAgent) )))
			{
				bcc.addAll(to);
				bcc.addAll(cc);
				to.clear();
				cc.clear();
				if(replyTo!=null)
				{
					String replyToObj = (String) replyTo.elementAt(1);
					if(FrameworkUtil.isObjectId(context, replyToObj))
					{
						replyTo.setElementAt((String) replyTo.elementAt(1)+"|", 1);
					}
				}

				String toUsers = to.toString();
				toUsers = "TO: "+toUsers.substring(1,toUsers.length()-1);

				String ccUsers = cc.toString();
				ccUsers = "CC: "+ccUsers.substring(1,ccUsers.length()-1);

				String bccUsers = bcc.toString();
				bccUsers= "BCC: "+bccUsers.substring(1,bccUsers.length()-1);

				if(messageHTML!=null && !"null".equalsIgnoreCase(messageHTML))
				{
					messageHTML = "-+-----+-----+-----+-----+-----+-----+-----+-----+-<br/> <b> Do Not Edit This Line</b><br/><br/>";
					messageHTML = messageHTML+toUsers+"<br/>"+ccUsers+"<br/>"+bccUsers+"<br/><br/>"+inputOrJPOTolocalizedString(context, messageHTMLKey, info, null);
				}
				if(messageText!=null && !"null".equalsIgnoreCase(messageText))
				{
					messageText = "-+-----+-----+-----+-----+-----+-----+-----+-----+-\n Do Not Edit This Line \n\n";
					messageText = messageText+toUsers+"\n"+ccUsers+"\n"+bccUsers+"\n\n"+inputOrJPOTolocalizedString(context, messageKey, info, urls);
				}
				// start code for appending TYPE NAME REVISION in the Subject of the mail
				DomainObject dObj = new DomainObject(id);
				StringList objectSelects= new StringList();
	            objectSelects.addElement(DomainConstants.SELECT_NAME);
	            objectSelects.addElement(DomainConstants.SELECT_TYPE);
	            objectSelects.addElement(DomainConstants.SELECT_REVISION);
	            Map objInfo = dObj.getInfo(context, objectSelects);
	            String objType = (String)objInfo.get(DomainConstants.SELECT_TYPE);
	            String objName = (String)objInfo.get(DomainConstants.SELECT_NAME);
	            String objRevision =  (String)objInfo.get(DomainConstants.SELECT_REVISION);
				subject = subject+" | '"+objType+"' '"+objName+"' '"+objRevision+"'";
				// end code for appending TYPE NAME REVISION in the Subject of the mail
			}

			// Execute preprocess JPO method to determine final content and whether to send
			if (preprocessJPOMethod != null && preprocessJPOMethod.length() > 0) {
				info.put("subject", subject);
				info.put("messageText", messageText);
				info.put("messageHTML", messageHTML);
				int index = preprocessJPOMethod.indexOf(":");
				String methodName = "";
				String name = "";
				if (index >= 0) {
					methodName = preprocessJPOMethod.substring(index+1);
					name = preprocessJPOMethod.substring(0, index);
				}

				try
				{
					// Pack arguments into string array
					String[] args = JPO.packArgs(info);
					try {

						Map resultsMap = (Map)JPO.invoke(context, name, new String[0], methodName, args, Map.class);


						String status = (String) resultsMap.get("status");
						if (status != null && status.length() > 0) {
							// Any sort of status implies not sending the mail
							continue;
						}

						to = (StringList) resultsMap.get("toList");
						cc = (StringList) resultsMap.get("ccList");
						bcc = (StringList) resultsMap.get("bccList");
						subject = (String) resultsMap.get("subject");
						messageText = (String) resultsMap.get("messageText");
						messageHTML = (String) resultsMap.get("messageHTML");
						objectIdList = (StringList) resultsMap.get("objectIdList");
						fromAgent = (String) resultsMap.get("from");
						replyTo = (StringList) resultsMap.get("replyTo");

					} catch (Throwable e) {
						System.out.println("Exception trying to invoke method " + methodName +
								" of JPO " + name + " ERROR: " + e.toString());
					}
				} catch (Exception e) {
					System.out.println("Exception trying to print program " + name + " ERROR: " + e.toString());
				}

            }
            
				if(UIUtil.isNotNullAndNotEmpty(messageHTML)){
				messageHTML = messageHTML + "\n\n";
				}
				if(UIUtil.isNotNullAndNotEmpty(messageText)){
				messageText = messageText + "\n\n";
			}
				
			}
            // get frequencies for notification recipients
            HashSet<String> frequencies = new HashSet<String>();
            getNotificationFrequencies(nameAndFrequency, to, frequencies);
            getNotificationFrequencies(nameAndFrequency, cc, frequencies);
            getNotificationFrequencies(nameAndFrequency, bcc, frequencies);

            // loop over notification frequencies
            Iterator<String> itrFreq = frequencies.iterator();
            String frequency;
            while (itrFreq.hasNext()) {
                frequency = itrFreq.next();
                // build the to, cc and bcc lists for this frequency
                StringList toFreq = getNamesForFrequency(to, nameAndFrequency, frequency);
                StringList ccFreq = getNamesForFrequency(cc, nameAndFrequency, frequency);
                StringList bccFreq = getNamesForFrequency(bcc, nameAndFrequency, frequency);

	            // if we have someone to notify
                if (toFreq.size() > 0 || ccFreq.size() > 0 || bccFreq.size() > 0) {
                	// send if the preference is to notify immediately
                	if (frequency.equalsIgnoreCase("immediately")) {
		                sendJavaMail(context, toFreq, ccFreq, bccFreq, subject, messageText, messageHTML, 
		                		fromAgent, replyTo, objectIdList, notifyType);
                	} else {
                		MapList spools;
                		if (frequency.equalsIgnoreCase("default")) {
                	    	// find the spools for this type of notification
                			spools = getSpools(context, notificationName);
                		} else {
                			// find the spool for this frequency
                			spools = getSpool(context, frequency);
                		}
                	
			            // spool the notification if appropriate
                		if (spools != null && spools.size() > 0) {
			                spoolJavaMail(context, toFreq, ccFreq, bccFreq, subject, messageText, messageHTML, 
			                		fromAgent, replyTo, objectIdList, notifyType, notificationName, spools);
			        	} else {
			                sendJavaMail(context, toFreq, ccFreq, bccFreq, subject, messageText, messageHTML, 
			                		fromAgent, replyTo, objectIdList, notifyType);
			        	}
	            	}
	            }
            }
        }
    }
    
    /**
     * This method returns all of the spools that the given notification object is connected to.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param notificationName a String holding the name of the Notification object
     * @return a MapList of spool object ids
     * @throws Exception if the operation fails
     * @since x+2
     */

	private static MapList getSpools(Context context, String notificationName) throws Exception {
		MapList spools = null;
		StringList selects = new StringList();
		selects.add("id");
		MapList notifications = DomainObject.findObjects(context,
				PropertyUtil.getSchemaProperty(context, "type_Notification"),
				PropertyUtil.getSchemaProperty(context, "vault_eServiceAdministration"),
				"name eq '" + notificationName + "'", selects);
		if (notifications.size() > 0) {
			Map m1 = (Map)notifications.get(0);
			DomainObject notification = new DomainObject((String)m1.get("id"));

			//Modified:21-July-2010:s2e:R210:PRG:2011x- Email Notification Enhancement
			boolean isContextPushed = false;
			try{
				ContextUtil.pushContext(context);
				isContextPushed = true;
				spools = notification.getRelatedObjects(context,
						PropertyUtil.getSchemaProperty(context, "relationship_NotificationSpool"), // rel type
						PropertyUtil.getSchemaProperty(context, "type_NotificationSpool"), // object type
						selects, // object selects
						null, // rel selects
						false, //get to-side
						true, // get from-side
						(short)1, // recurseToLevel
						"", // objectWhere
						"", // relationshipWhere
						50 // limit
				);
			}
			catch(Exception e){
				throw new MatrixException(e);
			}
			finally{
				if (isContextPushed) {
					ContextUtil.popContext(context);
				}
			}
			//End:21-July-2010:s2e:R210:PRG:2011x- Email Notification Enhancement
		}
		return spools;
	}

    private static MapList getSpool(Context context, String spoolName) throws Exception {
    	MapList spool = null; 
    	
		boolean isContextPushed = false;
		try {
			ContextUtil.pushContext(context);
			isContextPushed = true;

			StringList selects = new StringList(1);
	        selects.add("id");
	    	spool = DomainObject.findObjects(context, 
	    			PropertyUtil.getSchemaProperty(context, "type_NotificationSpool"),
	    			spoolName, "*", "*", "*",
					"attribute[Enable For Preference]==TRUE", false, selects);
		}
		catch(Exception e) {
			throw new MatrixException(e);
		}
		finally {
			if (isContextPushed) {
				ContextUtil.popContext(context);
			}
		}
		
		return (spool);
    }

    /**
     * This method spools a mail notification to the specified users. Sometime later the
     * spooled messages can be consolidated and sent.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param to StringList that contains the list of users to notify
     * @param cc StringList that contains the list of users to cc
     * @param bcc StringList that contains the list of users to bcc
     * @param subject String that contains the notification subject
     * @param messageText String that contains the notification message
     * @param messageHTML a String that contains the notification HTML message
     * @param fromAgent a String holding the from user appearing in the mail
     * @param replyTo a StringList containing the replyTo user and personal information
     * @param objectIdList StringList that contains the ids of objects to send with the notification
     * @param notifyType Notification Type selected by user (email, iconmail, or both)
     * @param notificationName a String holding the name of the Notification object
     * @throws Exception if the operation fails
     * @since x+2
     */

	private static void spoolJavaMail(Context context, StringList to,
			StringList cc, StringList bcc, String subject, String messageText,
			String messageHTML, String fromAgent, StringList replyTo,
			StringList objectIdList, String notifyType, String notificationName,
			MapList spools) throws Exception {

		AttributeList attributes = new AttributeList();
		AttributeType attrType = null;
		Attribute attrObject = null;
		StringBuffer buf = new StringBuffer();

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_StaticToList"));
		buf.delete(0, buf.length());
		if (to != null) {
			int count = 0;
			Iterator stringItr = to.iterator();
			while (stringItr.hasNext()) {
				if (count > 0) {
					buf.append(",");
				}
				String name = (String)stringItr.next();
				buf.append(name);
				count++;
			}
		}
		attrObject = new Attribute(attrType, buf.toString());
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_StaticCcList"));
		buf.delete(0, buf.length());
		if (cc != null) {
			int count = 0;
			Iterator stringItr = cc.iterator();
			while (stringItr.hasNext()) {
				if (count > 0) {
					buf.append(",");
				}
				String name = (String)stringItr.next();
				buf.append(name);
				count++;
			}
		}
		attrObject = new Attribute(attrType, buf.toString());
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_StaticBccList"));
		buf.delete(0, buf.length());
		if (bcc != null) {
			int count = 0;
			Iterator stringItr = bcc.iterator();
			while (stringItr.hasNext()) {
				if (count > 0) {
					buf.append(",");
				}
				String name = (String)stringItr.next();
				buf.append(name);
				count++;
			}
		}
		attrObject = new Attribute(attrType, buf.toString());
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_SubjectText"));
		attrObject = new Attribute(attrType, subject);
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_BodyText"));
		attrObject = new Attribute(attrType, messageText);
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_BodyHTML"));
		attrObject = new Attribute(attrType, messageHTML);
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_FromAgent"));
		attrObject = new Attribute(attrType, fromAgent);
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_ReplyTo"));
		buf.delete(0, buf.length());
		if (replyTo != null) {
			int count = 0;
			Iterator stringItr = replyTo.iterator();
			while (stringItr.hasNext()) {
				if (count > 0) {
					buf.append(",");
				}
				String name = (String)stringItr.next();
				buf.append(name);
				count++;
			}
		}
		attrObject = new Attribute(attrType, buf.toString());
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_Attachments"));
		buf.delete(0, buf.length());
		if (objectIdList != null) {
			int count = 0;
			Iterator stringItr = objectIdList.iterator();
			while (stringItr.hasNext()) {
				if (count > 0) {
					buf.append(",");
				}
				String name = (String)stringItr.next();
				buf.append(name);
				count++;
			}
		}
		attrObject = new Attribute(attrType, buf.toString());
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_NotificationType"));
		attrObject = new Attribute(attrType, notifyType);
		attributes.addElement(attrObject);

		attrType = new AttributeType(
				PropertyUtil.getSchemaProperty(context, "attribute_NotificationName"));
		attrObject = new Attribute(attrType, notificationName);
		attributes.addElement(attrObject);

		Iterator spoolItr = spools.iterator();
		while (spoolItr.hasNext()) {
			Map child = (Map)spoolItr.next();
			String spoolId = (String)child.get("id");
			DomainObject spool = new DomainObject(spoolId);
			// Create rel to hold deferred mail elements
			RelationshipType relType = new RelationshipType(
					PropertyUtil.getSchemaProperty(context, "relationship_NotificationRequest"));

			//Modified:10-August-2010:s2e:R210:PRG:2011x- Email Notification Enhancement
			boolean isContextPushed = false;
			try{
				ContextUtil.pushContext(context);
				isContextPushed = true;
				Relationship rel = spool.addRelatedObject(context,
						relType,
						true, // isFrom
						spoolId);
				rel.setAttributes(context, attributes);
			}
			catch(Exception e){
				throw new MatrixException(e);
			}
			finally{
				if (isContextPushed) {
					ContextUtil.popContext(context);
				}
			}
			//End:10-August-2010:s2e:R210:PRG:2011x- Email Notification Enhancement

		}
		return;
	}

	/**
	 * This method sends mail using the Java Mail API.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param toList StringList that contains the list of users to notify
	 * @param ccList StringList that contains the list of users to cc
	 * @param bccList StringList that contains the list of users to bcc
	 * @param subject String that contains the subject
	 * @param messageText String that contains the message in plain text
	 * @param messageHTML String that contains the message in HTML format
	 * @param fromAgent a String holding the from user appearing in the mail
	 * @param replyTo a StringList holding the replyTo user and personal info appearing in the mail
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	public static void sendJavaMail(Context context, StringList toList,
			StringList ccList, StringList bccList, String subject,
			String messageText, String messageHTML,
			String fromAgent, StringList replyTo) throws Exception {
		//send notifications through both Email and IconMail
		sendJavaMail(context, toList, ccList, bccList, subject, messageText, messageHTML,
				fromAgent, replyTo,null, both);
	}


	/**
	 * This method sends mail using the Java Mail API.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param toList StringList that contains the list of users to notify
	 * @param ccList StringList that contains the list of users to cc
	 * @param bccList StringList that contains the list of users to bcc
	 * @param subject String that contains the subject
	 * @param messageText String that contains the message in plain text
	 * @param messageHTML String that contains the message in HTML format
	 * @param fromAgent a String holding the from user appearing in the mail
	 * @param replyTo a StringList holding the replyTo user and personal info appearing in the mail
	 * @param notifyType Notification Type selected by user (email, iconmail, or both)
	 * @throws Exception if the operation fails
	 * @since x+2
	 */

	public static void sendJavaMail(Context context, StringList toList,
			StringList ccList, StringList bccList, String subject,
			String messageText, String messageHTML,
			String fromAgent, StringList replyTo, StringList objectIdList, String notifyType) throws Exception 
			{
		// IR-058366V6R2011x Modification -Starts
		boolean isContextPushed = false;
		emxContextUtil_mxJPO utilityClass = new emxContextUtil_mxJPO(context, null);
		// IR-058366V6R2011x Modification -Ends
		try
		{


			MxMessage msg = new MxMessage();
			//msg.setDebug(true);

			if (replyTo != null && replyTo.size() > 1) {
				msg.setReplyTo((String)replyTo.elementAt(0));
				msg.setReplyToText((String)replyTo.elementAt(1));
			}

			msg.setSubject(subject);

			if (messageText != null) {
				msg.setMessage(messageText);
			}

			if (messageHTML != null) {
				msg.setHtmlMessage(messageHTML);
			}
			if (toList != null) {
				ArrayList to = new ArrayList();
				to.addAll(toList);
				msg.setToList(to);
			}

			if (ccList != null) {
				ArrayList cc = new ArrayList();
				cc.addAll(ccList);
				msg.setCcList(cc);
			}

			if (bccList != null) {
				ArrayList bcc = new ArrayList();
				bcc.addAll(bccList);
				msg.setBccList(bcc);
			}
			// IR-058366V6R2011x Commented here and declared at the begining before try block -starts
			// boolean isContextPushed = false;
			// ${CLASS:emxContextUtil} utilityClass = new ${CLASS:emxContextUtil}(context, null);
			// IR-058366V6R2011x Commented here and declared at the begining before try block -Ends
			// Test if spoofing should be performed on the "from" field.
			String agentName = fromAgent;
			if (agentName == null || "".equals(agentName)) {
				agentName = emxMailUtil_mxJPO.getAgentName(context, null);
			}
			if (agentName != null && !"".equals(agentName)) {
				try {
					// Push Notification Agent
					String[] pushArgs = { agentName };
					utilityClass.pushContext(context, pushArgs);
					isContextPushed = true;

				} catch (Exception ex) {
				}
			}

			//msg.sendJavaMail(context, false); false for not sending icon mail
			//status can be 0 or 1. Zero represents external mails have been sent successfully and 1
			// implies external mail was not configured on subscribed persons.

			try
			{
				//Send mail only using email
				if(email.equalsIgnoreCase(notifyType) || both.equalsIgnoreCase(notifyType))
				{
					int emailStatus = msg.sendJavaMail(context, false);
				}
			}
			catch(Exception ex)
			{
				System.out.println("Message: Please check SMTP settings for sending an Email");
				throw new FrameworkException(ex);
			}

			//Send mail only using IconMail
			if(iconMail.equalsIgnoreCase(notifyType) || both.equalsIgnoreCase(notifyType))
			{
				IconMail iconMail = new IconMail();
				iconMail.create(context);
				iconMail.setMessage(messageText);
				if (toList != null && toList.size() > 0)
					iconMail.setToList(toList);
				if (ccList != null && ccList.size() > 0)
					iconMail.setCcList(ccList);
				if (bccList != null && bccList.size() > 0)
					iconMail.setBccList(bccList);
				if(objectIdList != null && objectIdList.size() != 0)
				{
					BusinessObjectList bol = new BusinessObjectList(objectIdList.size());

					Iterator i = objectIdList.iterator();
					while (i.hasNext()) {
						String id = (String) i.next();
						BusinessObject bo = new BusinessObject(id);
						// Added the condition to check if the mail is sending for relationship then not attaching the
						// connection id
						if(messageText.indexOf("relationshipID=") == -1)
						{
							bo.open(context);
							bol.addElement(bo);
						}

						bo.open(context);
						bol.addElement(bo);

					}
					iconMail.setObjects(bol);
				}
				iconMail.send(context, subject, false);
			}
			// IR-058366V6R2011x Modification - Starts
			// if (isContextPushed == true) {
			// Pop Notification Agent
			//     utilityClass.popContext(context, null);
			//  }
			// IR-058366V6R2011x Modification - Ends
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		finally // IR-058366V6R2011x Added finally block - Starts
		{
			if (isContextPushed == true) {
				// Pop Notification Agent
				utilityClass.popContext(context, null);
			}
		} // IR-058366V6R2011x Added finally block - Ends
			}


	/**
	 * This method sends an icon mail notification to the specified users.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param toList StringList that contains the list of users to notify
	 * @param ccList StringList that contains the list of users to cc
	 * @param bccList StringList that contains the list of users to bcc
	 * @param subject String that contains the notification subject
	 * @param message String that contains the notification message
	 * @param objectIdList StringList that contains the ids of objects to send with the notification
	 * @param fromAgent a String holding the from user appearing in the mail
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	protected static void sendMail(Context context, StringList toList,
			StringList ccList, StringList bccList, String subject,
			String message, StringList objectIdList,
			String fromAgent) throws Exception {

		// viewing Icon mail in application '<' and '>' are read as tags by html
		// These char's need to be eliminated before sending the mail message.
		subject = subject.replace('<', ' ');
		subject = subject.replace('>', ' ');
		message = message.replace('<', ' ');
		message = message.replace('>', ' ');

		// Create iconmail object.
		IconMail mail = new IconMail();
		mail.create(context);

		// Set the "to" list.
		mail.setToList(toList);

		// Set the "cc" list.
		if (ccList != null) {
			mail.setCcList(ccList);
		}

		// Set the "bcc" list.
		if (bccList != null) {
			mail.setBccList(bccList);
		}

		// Set the object list.  If the object id list is available,
		// then send the objects along with the notification.
		if (objectIdList != null && objectIdList.size() != 0) {
			BusinessObjectList bol = new BusinessObjectList(objectIdList.size());

			Iterator i = objectIdList.iterator();
			while (i.hasNext()) {
				String id = (String) i.next();
				BusinessObject bo = new BusinessObject(id);
				bo.open(context);
				bol.addElement(bo);
			}

			mail.setObjects(bol);
		}

		// Set the message.
		mail.setMessage(message);

		boolean isContextPushed = false;
		emxContextUtil_mxJPO utilityClass = new emxContextUtil_mxJPO(context,
				null);
		// Test if spoofing should be performed on the "from" field.
		String agentName = fromAgent;
		if (agentName == null || "".equals(agentName)) {
			agentName = emxMailUtil_mxJPO.getAgentName(context, null);
		}
		if (agentName != null && !"".equals(agentName)) {
			try {
				// Push Notification Agent
				String[] pushArgs = { agentName };
				utilityClass.pushContext(context, pushArgs);
				isContextPushed = true;
			} catch (Exception ex) {
			}
		}

		// Set the subject and send the iconmail.
		mail.send(context, subject);

		if (isContextPushed == true) {
			// Pop Notification Agent
			utilityClass.popContext(context, null);
		}
	}

	/**
	 * Returns a processed and translated message for a given key.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId a String contains id of the object
	 * @param messageKey the notification message key
	 * @param locale the locale used to determine language
	 * @param basePropFileName used to identify property file.
	 * @return a String containing the message
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	protected static String getMessage(Context context, String objectId,
			String messageKey,
			Locale locale, String basePropFileName)
	throws Exception {
		return (getMessage(context, objectId, OBJECT, messageKey, null, locale, basePropFileName));
	}

	/**
	 * Returns a processed and translated message for a given key or string.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId a String contains id of the object
	 * @param messageKey the notification message key or string
	 * @param formatArgs an array of message place holder values
	 * @param locale the locale used to determine language
	 * @param basePropFileName used to identify property file if key given
	 * @return a String containing the message
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	protected static String getMessage(Context context, String objectId,
			String messageKey, Object[] formatArgs,
			Locale locale, String basePropFileName) throws Exception {

		return getMessage(context, objectId, OBJECT, messageKey, formatArgs, locale, basePropFileName);
	}

	/**
	 * Returns a processed and translated message for a given key.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be
	 *            used for evaluating selects embedded in the subject and
	 *            message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param messageKey the notification message key
	 * @param locale the locale used to determine language
	 * @param basePropFileName used to identify property file.
	 * @return a String containing the message
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static String getMessage(Context context, String id, String idType,
			String messageKey,
			Locale locale, String basePropFileName)
	throws Exception {
		return (getMessage(context, id, idType, messageKey, null, locale, basePropFileName));
	}

	/**
	 * Returns a processed and translated message for a given key or string.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be
	 *            used for evaluating selects embedded in the subject and
	 *            message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param messageKey the notification message key or string
	 * @param formatArgs an array of message place holder values
	 * @param locale the locale used to determine language
	 * @param basePropFileName used to identify property file if key given
	 * @return a String containing the message
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static String getMessage(Context context, String id, String idType,
			String messageKey, Object[] formatArgs,
			Locale locale, String basePropFileName) throws Exception {

		if (locale == null || locale.equals("")) {
			locale = getLocale(context);
		}

		String message = null;
		if (basePropFileName == null || basePropFileName.equals("")) {
			message = loadConvert(messageKey);
		} else {
			// Define the message from the given key.
			message = MessageUtil.getString(basePropFileName, messageKey, null,
					locale);
		}

		if (message != null) {
			// Do the normal macro substitution (i.e. $<type>, ${OBJECTID}, etc.)
			message = substituteValues(context, message, id, idType, locale.getLanguage());

			// Substitute using format arguments (java.text.MessageFormat)
			if (formatArgs != null && formatArgs.length > 0 && message != null) {
				message = substituteMessageArgs(context, message, formatArgs,locale);
			}
		}

		return (message);
	}

	/**
	 * Get locale for given context.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @return locale object
	 * @since AEF 10.0.0.0
	 */

	protected static Locale getLocale(Context context) {
		return (MessageUtil.getLocale(context.getSession().getLanguage()));
	}

	/**
	 * Replace symbolic names embedded in a string with true names.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param expression the string containing embedded selectables
	 * @return the evaluated result
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */
	protected static String substituteValues(Context context, String expression)
	throws Exception {
		return (substituteValues(context, expression, null, null));
	}

	/**
	 * Replace object selectables embedded in a string with their
	 * values based on the given object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param expression the string containing embedded selectables
	 * @param objectId the id of the object used to evaluate the selectables
	 * @param language the string containing a language for i18n of type and vault
	 * @return the evaluated result
	 * @throws Exception  if the operation fails
	 * @since AEF 10.0.0.0
	 */

	protected static String substituteValues(Context context, String expression,
			String objectId, String language) throws Exception {
		return substituteValues(context, expression, objectId, OBJECT, language);
	}

	/**
	 * Replace selectables embedded in a string with their
	 * values based on the given object or relationship.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param expression the string containing embedded selectables
	 * @param id a String holding the id of an object or relationship to be
	 *            used for evaluating selects embedded in the subject and
	 *            message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param language the string containing a language for i18n of type and vault
	 * @return the evaluated result
	 * @throws Exception  if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static String substituteValues(Context context, String expression,
			String id, String idType, String language) throws Exception {
		try {
			if (expression == null || expression.equals("")) {
				return expression;
			}
			// Initialize the return string.
			StringBuffer message = new StringBuffer();

			// Create a mapping from token type to a set
			// that holds selects, macros, and variables.
			Map expressions = new HashMap(3);
			expressions.put(SELECT, new HashSet());
			expressions.put(MACRO, new HashSet());

			// Create a mapping from token type to a map that maps
			// the selects, macros, and variables to their values.
			Map values = new HashMap(3);
			values.put(SELECT, new HashMap());
			values.put(MACRO, new HashMap());

			// Create lists to hold the tokens and their types.
			ArrayList tokens = new ArrayList();
			ArrayList types = new ArrayList();

			//
			// Parse the input string into typed tokens.
			//

			// Loop through the tokens until the end of the string.
			StringTokenizer st = new StringTokenizer(expression,
					DELIMITER_START, true);
			while (true) {
				String token = null;
				String type = null;

				try {
					// get everything up to the first delimiter
					token = st.nextToken(DELIMITER_START);

					// if this is the delimiter, then determine the token type
					if ("$".equals(token)) {
						type = st.nextToken(DELIMITER_TYPE);

						// look for the matching end token
						String delimiter = (String) delimiters.get(type);

						// if this wasn't a known delimiter
						if (delimiter == null) {
							// must have been some text
							tokens.add(type);
							types.add(TEXT);
						} else {
							token = st.nextToken(delimiter);

							if (type.equals(SELECT) && token.indexOf("_") > -1) {
								token = replaceSymbolicTokens(context, token);
							}

							tokens.add(token);
							types.add(type);

							// add the expression to the list by type
							((HashSet) expressions.get(type)).add(token);

							// throw out the matching end token
							token = st.nextToken();
						}
					}
					// otherwise, save the token as text
					else {
						// must have been some text
						tokens.add(token);
						types.add(TEXT);
					}

				} catch (NoSuchElementException e) {
					break;
				}
			}

			//
			// Get the selectable information from the object.
			//

			// Create a select list from the select set.
			StringList selects = new StringList(((HashSet) expressions
					.get(SELECT)).size());
			Iterator itr = ((HashSet) expressions.get(SELECT)).iterator();

			while (itr.hasNext()) {
				selects.add((String) itr.next());
			}

			// use the select list to get info from the object
			// save the select map
			if (id != null) {
				values.put(SELECT, getInfo(context, id, idType, selects));
			}

			// If select is type or vault then internationalize them.

			if (language != null) {
				Hashtable selectMap = new Hashtable((Map) values.get(SELECT));

				/*modified by common team to accomodate macro Current(State), for any changes/issues please contact BPS-Common team*/

				for (int i = 0; i < selects.size(); i++) {
					if (((String) selects.get(i)).equalsIgnoreCase("type")) {
						StringList selectValueList = (StringList) selectMap.get(selects
								.get(i));
						String selectValue = (String) selectValueList.firstElement();
						String selectProp = "emxFramework.Type."
						+ selectValue.replace(' ', '_');
						String selectPropValue = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, selectProp, new Locale(language));						
						selectValueList.setElementAt(selectPropValue, 0);
						selectMap.put(selects.get(i), selectValueList);
					} else if (((String) selects.get(i))
							.equalsIgnoreCase("vault")) {
						StringList selectValueList = (StringList) selectMap.get(selects
								.get(i));
						String selectValue = (String) selectValueList.firstElement();
						String selectProp = "emxFramework.Vault."
						+ selectValue.replace(' ', '_');
						String selectPropValue = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, selectProp, new Locale(language));						
						selectValueList.setElementAt(selectPropValue, 0);
						selectMap.put(selects.get(i), selectValueList);
					}else if (((String) selects.get(i))
							.equalsIgnoreCase("current")) {
						DomainObject dom = DomainObject.newInstance(context,id);
						StringList selectValueList = (StringList) selectMap.get(selects
								.get(i));
						String selectValue = (String) selectValueList.firstElement();
						String selectProp = "emxFramework.State." + dom.getPolicy(context).getName().replace(' ', '_') + "."
						+ selectValue.replace(' ', '_');
						String selectPropValue = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, selectProp, new Locale(language));						
						selectValueList.setElementAt(selectPropValue, 0);
						selectMap.put(selects.get(i), selectValueList);
					}
				}

				values.put(SELECT, selectMap);
			}

			// Build the macro map from the macro set.
			Map macros = new HashMap();
			itr = ((HashSet) expressions.get(MACRO)).iterator();
			while (itr.hasNext()) {
				String name = (String) itr.next();
				String value = MqlUtil.mqlCommand(context, "get env $1",name);				
				macros.put(name, value);
			}

			// Save the macro map.
			values.put(MACRO, macros);

			//
			// Put the string back together while replacing tokens with values.
			//

			// Loop through the token and type list together.
			Iterator tokenItr = tokens.iterator();
			Iterator typeItr = types.iterator();

			while (tokenItr.hasNext()) {
				String token = (String) tokenItr.next();
				String type = (String) typeItr.next();

				// Get the appropriate token map based on the type.
				Map valueMap = (Map) values.get(type);

				// If the type map is null, then the token is text,
				// so just put it in the message.
				// Otherwise, get the value for the token
				// and put that in the message.
				if (valueMap == null) {
					message.append(token);
				} else {
					String value = null;
					// if we are dealing with the SELECT map then we have
					// StringList entries with multiple potential values
					if (type.equals(SELECT)) {
						StringList result = (StringList) valueMap.get(token);
						if (result != null) {
							if (result.size() == 1) {
								value = (String) result.firstElement();
							} else {
								Set set = new HashSet();
								// move values into set to eliminate duplicates
								set.addAll(result);
								Iterator setItr = set.iterator();
								StringBuffer list = new StringBuffer();
								if (setItr.hasNext()) {
									list.append((String) setItr.next());
								}
								while (setItr.hasNext()) {
									list.append(OUTPUT_LIST_DELIMITER);
									list.append((String) setItr.next());
								}
								value = list.toString();
							}
						}
					} else {
						value = (String) valueMap.get(token);
					}
					if (id != null) {
						if (value == null) {
							message.append(OUTPUT_NO_VALUE);
						} else if ("".equals(value)) {
							message.append(OUTPUT_EMPTY_VALUE);
						} else {
							message.append(value);
						}
					} else {
						// if there is no value, just place the token
						message.append(value == null ? token : value);
					}
				}
			}

			return (message.toString());
		} catch (Exception e) {
			throw (e);
		}
	}

	/**
	 * Given an expression, replace all of the symbolic references
	 * with their real values.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param expression the expression containing the symbolic references
	 * @return the evaluated result
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	protected static String replaceSymbolicTokens(Context context,
			String expression) throws Exception {
		String newExpression = expression;

		if (expression.indexOf("_") > -1) {
			StringTokenizer st = new StringTokenizer(expression, " [],\'\"",true);
			StringBuffer buffer = new StringBuffer();

			while (true) {
				try {
					String token = st.nextToken();

					if (token.indexOf("_") > -1) {
						buffer.append(lookupSymbolicToken(context, token));
					} else {
						buffer.append(token);
					}
				} catch (NoSuchElementException e) {
					break;
				}
			}

			newExpression = buffer.toString();
		}

		return (newExpression);
	}

	/**
	 * Lookup a symbolic token and return the real value.  If
	 * the token can't be found, return the original token.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param symbolicToken the token to lookup
	 * @return the result of the property lookup
	 * @since AEF 10.0.0.0
	 */

	protected static String lookupSymbolicToken(Context context,
			String symbolicToken) {
		String token = null;

		try {
			token = PropertyUtil.getSchemaProperty(context, symbolicToken);
		} catch (Exception e) {
		}

		if (token == null || token.length() == 0) {
			token = symbolicToken;
		}

		return (token);
	}

	/**
	 * Returns a formatted message.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param message the message containing format information
	 * @param formatArgs array of replacement values for the message
	 * @param locale the locale used to determine language
	 * @return a String containing the formatted message
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */

	protected static String substituteMessageArgs(Context context, String message,
			Object[] formatArgs, Locale locale) throws Exception {
		MessageFormat formatter = new MessageFormat("");
		formatter.setLocale(locale);
		formatter.applyPattern(message);

		return (formatter.format(formatArgs));
	}


	/**
	 * Get information regarding a given business object.
	 * The map that is returned contains all StringList entries. This ensures that
	 * multiple values are handled. This also means for a single value you need to
	 * extract the first element on the StringList.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId the id of the given object
	 * @param objectSelects the eMatrix <code>StringList</code> object that holds the list of object selectables
	 * @return Map containing information about the object based on selectables
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */
	protected static Map getInfo(Context context,
			String objectId,
			StringList objectSelects) throws Exception {
		return getInfo(context, objectId, OBJECT, objectSelects);
	}

	/**
	 * Get information regarding a given business object or relationship.
	 * The map that is returned contains all StringList entries. This ensures that
	 * multiple values are handled. This also means for a single value you need to
	 * extract the first element on the StringList.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be
	 *            used for evaluating selects embedded in the subject and
	 *            message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param selects the eMatrix <code>StringList</code> object that holds the list of selectables
	 * @return Map containing information about the object or relationship based on selectables
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static Map getInfo(Context context,
			String id, String idType,
			StringList selects) throws Exception {

		HashMap hashMap = null;
		if (idType.equalsIgnoreCase(OBJECT)) {
			BusinessObject bo = new BusinessObject(id);
			BusinessObjectWithSelect bows = bo.select(context, selects);
			Vector keys = bows.getSelectKeys();
			int size = keys.size();
			hashMap = new HashMap(size);
			for (int i=0; i < size; i++) {
				String name = (String) keys.elementAt(i);
				hashMap.put(name, bows.getSelectDataList(name));
			}
		} else {
			String[] relIdList = {id};
			RelationshipWithSelectList relWSelList = Relationship.getSelectRelationshipData(context, relIdList,
					selects);
			RelationshipWithSelectItr relWSelItr =
				new RelationshipWithSelectItr(relWSelList);

			relWSelItr.next();
			RelationshipWithSelect relWSel = relWSelItr.obj();
			Vector keys = relWSel.getSelectKeys();
			int size = keys.size();
			hashMap = new HashMap(size);
			for (int i=0; i < size; i++) {
				String name = (String) keys.elementAt(i);
				hashMap.put(name, relWSel.getSelectDataList(name));
			}
		}
		return (hashMap);
	}

	/*
	 * Converts encoded &#92;uxxxx to unicode chars
	 * and changes special saved chars to their original forms
	 */
	protected static String loadConvert(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);

		for (int x=0; x<len; ) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value=0;
					for (int i=0; i<4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0': case '1': case '2': case '3': case '4':
						case '5': case '6': case '7': case '8': case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a': case 'b': case 'c':
						case 'd': case 'e': case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A': case 'B': case 'C':
						case 'D': case 'E': case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed \\\\uxxxx encoding.");
						}
					}
					outBuffer.append((char)value);
				} else {
					if (aChar == 't') aChar = '\t';
					else if (aChar == 'r') aChar = '\r';
					else if (aChar == 'n') aChar = '\n';
					else if (aChar == 'f') aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}

	/**
	 * Gets additional message that needs to be appended with the Message body of Inbox Task Notification.
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectIdList the idList for which the message needs to be returned
	 * @param locale the Locale object
	 * @param basePropFile the name of the property file in which the notification keys are defined
	 * @param baseURL the URL link to the corresponding inbox task
	 * @param urlSuffix the Suffix that needs to be appended with the url
	 * @return String containing message to be appended
	 * @throws Exception if the operation fails
	 * @since AEF 10.6.SP1
	 */

	public static String getNotificationMessageForInboxTask(Context context, StringList objectIdList, Locale locale, String basePropFile, String baseURL, String urlSuffix)
	throws Exception {
		String inBoxTaskId =null;
		String sTempObjId = null;
		String msg=null;
		DomainObject doTempObj = DomainObject.newInstance(context);
		// If the base URL and object id list are available,
		// then iterate
		if ( (baseURL != null && ! "".equals(baseURL)) &&
				(objectIdList != null && objectIdList.size() != 0) )
		{
			Iterator i = objectIdList.iterator();
			while (i.hasNext())
			{
				sTempObjId= (String)i.next();

				if( (sTempObjId != null) && (!sTempObjId.equals("")))
				{
					try{

						doTempObj.setId(sTempObjId);

						if( (doTempObj.getInfo(context,DomainConstants.SELECT_TYPE)).equals(DomainConstants.TYPE_INBOX_TASK)){

							inBoxTaskId = sTempObjId;
							break;
						}
					}catch(Exception ex){
						throw (ex);
					}
				}
			}

		}
		// If it is inbox task the message has to be modified accordingly.
		if( (inBoxTaskId != null) && (!inBoxTaskId.equals("")) ){
			if(locale !=null){
				msg = emxMailUtil_mxJPO.getInboxTaskMailMessage(context,inBoxTaskId,locale,basePropFile,baseURL,urlSuffix);
			}else{
				msg = emxMailUtil_mxJPO.getInboxTaskMailMessage(context,inBoxTaskId,getLocale(context),basePropFile,baseURL,urlSuffix);
			}
		}
		return msg;
	}

	/**
	 * Evaluate given filter expression (or JPO method).
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be used for
	 *          evaluating selects embedded in the subject and message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param notificationName a String holding the name of the Notification object
	 * @param payload a Map containing information to pass on to helper JPO methods
	 * @param filter a String holding the filter expression or "JPO <jponame>:<methodname>"
	 * @return int containing status (0 pass, filter out)
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static int evaluateFilter(Context context, String id, String idType, String notificationName,
			Map payload, String filter) throws Exception {
		int status = 0;
		if (filter != null && !filter.equals("")) {
			if (filter.startsWith("JPO")) {
				// Execute filter JPO method to determine if email should be sent
				int index = filter.indexOf(":");
				String methodName = "";
				String name = "";
				if (index >= 0) {
					methodName = filter.substring(index+1);
					name = filter.substring(4, index);
				}

				try
				{
					// Create the arguments for the JPO method
					Map note = new HashMap();
					note.put("id", id);
					note.put("idType", idType);
					note.put("notificationName", notificationName);
					note.put("payload", payload);

					// Pack arguments into string array
					String[] args = JPO.packArgs(note);
					try {
						status = JPO.invoke(context,  name, new String[0], methodName, args);

					} catch (Throwable e) {
						System.out.println("Exception trying to invoke method " + methodName +
								" of JPO " + name + " ERROR: " + e.toString());
					}
				} catch (Exception e) {
					System.out.println("Exception trying to print program " + name + " ERROR: " + e.toString());
				}
			} else {
				if (id != null && !id.equals("")) {
					String expression = substituteValues(context, filter);
					StringBuffer command = new StringBuffer();
					command.append("evaluate expression $1'");
					if (idType.equalsIgnoreCase(OBJECT)) {
						command.append("' on bus ");
					} else {
						command.append("' on relationship ");
					}
					command.append("$2");
					String filterResult = MqlUtil.mqlCommand(context, command
							.toString(),expression,id);
					if (filterResult.startsWith("FALSE")) {
						status = 1;
					}
				}
			}
		}
		return status;
	}

	/**
	 * Parse list of items and return as a StringList.
	 *
	 * @param value a String holding the delimited list of items
	 * @return list a StringList holding the items
	 * @since AEF 11.0.0.0
	 */

	protected static StringList inputListToStringList(String value) {

		StringList list = null;
		if (value != null && value.length() > 0) {
			StringTokenizer tokens = new StringTokenizer(value, INPUT_LIST_DELIMITER);
			list = new StringList();
			while (tokens.hasMoreTokens()) {
				list.addElement(tokens.nextToken().trim());
			}
		}
		return list;
	}

	/**
	 * Parse list of items (or execute JPO method) and return as a StringList.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be used for
	 *          evaluating selects embedded in the subject and message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param notificationName a String holding the name of the Notification object
	 * @param payload a Map containing information to pass on to helper JPO methods
	 * @param value a String holding the delimited list of items
	 * @return list a StringList holding the items
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static StringList inputListOrJPOToStringList(Context context, String id, String idType, String notificationName,
			Map payload, String value) throws Exception {

		StringList list = null;
		if (value != null && value.length() > 0) {
			if (value.startsWith("JPO")) {
				// Execute JPO method to determine list of items
				int index = value.indexOf(":");
				String methodName = "";
				String name = "";
				if (index >= 0) {
					methodName = value.substring(index+1);
					name = value.substring(4, index);
				}

					// Create the arguments for the JPO method
					Map note = new HashMap();
					note.put("id", id);
					note.put("idType", idType);
					note.put("notificationName", notificationName);
					note.put("payload", payload);

					// Pack arguments into string array
					String[] args = JPO.packArgs(note);
					try {
						list = (StringList)JPO.invoke(context, name, new String[0], methodName, args, StringList.class);

				} catch (Exception e) {
					throw new FrameworkException(e);
				}
			} else {
				list = inputListToStringList(value);
			}
		}
		return list;
	}

	/**
	 * Take given value (or execute JPO method) and return as a String.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be used for
	 *          evaluating selects embedded in the subject and message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param notificationName a String holding the name of the Notification object
	 * @param payload a Map containing information to pass on to helper JPO methods
	 * @param value a String holding the given value
	 * @return item a String holding the resulting item
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static String inputOrJPOToString(Context context, String id, String idType, String notificationName,
			Map payload, String value) throws Exception {

		String item = null;
		if (value != null && value.length() > 0) {
			if (value.startsWith("JPO")) {
				// Execute JPO method to determine item
				int index = value.indexOf(":");
				String methodName = "";
				String name = "";
				if (index >= 0) {
					methodName = value.substring(index+1);
					name = value.substring(4, index);
				}

				try
				{
					// Create the arguments for the JPO method
					Map note = new HashMap();
					note.put("id", id);
					note.put("idType", idType);
					note.put("notificationName", notificationName);
					note.put("payload", payload);

					// Pack arguments into string array
					String[] args = JPO.packArgs(note);
					try {
						item = (String)JPO.invoke(context, name, new String[0], methodName, args, String.class);

					} catch (Throwable e) {
						System.out.println("Exception trying to invoke method " + methodName +
								" of JPO " + name + " ERROR: " + e.toString());
					}
				} catch (Exception e) {
					System.out.println("Exception trying to print program " + name + " ERROR: " + e.toString());
				}
			} else {
				item = value;
			}
		}
		return item;
	}

	/**
	 * Take given key value (or execute JPO method) and return localized String.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param value a String holding the given value
	 * @param info a Map containing information to pass on to helper JPO methods
	 * @param urls a String of urls to append to the results
	 * @return results a String holding the localized results
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static String inputOrJPOTolocalizedString(Context context, String value, Map info,
			String urls) throws Exception {

		StringBuffer results = new StringBuffer();
		if (value != null && value.length() > 0) {
			if (value.startsWith("JPO")) {
				// Execute JPO method to determine item
				int index = value.indexOf(":");
				String methodName = "";
				String name = "";
				if (index >= 0) {
					methodName = value.substring(index+1);
					name = value.substring(4, index);
				}

				try {

					// Pack arguments into string array
					String[] args = JPO.packArgs(info);
					try {
						results.append((String)JPO.invoke(context, name, new String[0], methodName, args, String.class));

					} catch (Throwable e) {
						System.out.println("Exception trying to invoke method " + methodName +
								" of JPO " + name + " ERROR: " + e.toString());
					}
				} catch (Exception e) {
					System.out.println("Exception trying to print program " + name + " ERROR: " + e.toString());
				}
			} else {
				results.append(getMessage(context,
						(String) info.get("id"),
						(String) info.get("idType"),
						value,
						(Locale) info.get("locale"),
						(String) info.get("bundleName")));
				if (!UIUtil.isNullOrEmpty(urls)) {
					results.append("\n").append(urls);
				}
			}
		}
		return results.toString();
	}

	/**
	 * Combine a static list and the results of a dynamic list.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param id a String holding the id of an object or relationship to be used for
	 *          evaluating selects embedded in the subject and message strings
	 * @param idType a String set to either "object" or "relationship"
	 * @param list a StringList holding the static values
	 * @param selects a StringList holding the dynamic values
	 * @param userListMode a boolean set to true if evaluated selects generate lists of users
	 * @return finalList a StringList holding the combined lists (fully evaluates)
	 * @throws Exception if the operation fails
	 * @since AEF 11.0.0.0
	 */

	protected static StringList combineStaticAndDynamicLists(Context context, String id, String idType,
			StringList list, StringList selects, boolean userListMode) throws Exception {

		// Build up final list
		StringList finalList = new StringList();
		Iterator itr = null, itr2 = null;
		if (list != null && list.size() > 0) {
			itr = list.iterator();
			while (itr.hasNext()) {
				finalList.add(substituteValues(context, (String) itr.next()));
			}
		}

		// Evaluate select clauses and add to corresponding final list
		if (id != null) {
			if (selects != null && selects.size() > 0) {
				StringList finalSelects = new StringList(selects.size());
				itr = selects.iterator();
				while (itr.hasNext()) {
					finalSelects.add(substituteValues(context, (String) itr.next()));
				}
				Map values = getInfo(context, id, idType, finalSelects);
				itr = finalSelects.iterator();
				while (itr.hasNext()) {
					String select = (String) itr.next();
					StringList result = (StringList) values.get(select);
					if (result != null) {
						itr2 = result.iterator();
						while (itr2.hasNext()) {
							if (userListMode) {
								StringTokenizer tokens = new StringTokenizer(
										(String) itr2.next(), USER_LIST_DELIMITER);
								while (tokens.hasMoreTokens()) {
									finalList.addElement(tokens.nextToken()
											.trim());
								}
							}else {
								finalList.add((String) itr2.next());
							}

						}
					}
				}
				// move values into set to eliminate duplicates
				Set set = new HashSet();
				set.addAll(finalList);
				Iterator setItr = set.iterator();
				finalList = new StringList(set.size());
				while (setItr.hasNext()) {
					finalList.add((String) setItr.next());
				}
			}
		}
		return finalList;
	}


	/**
	 * Parse userList and groups email persons List, IconMail persons List, and Both persons list
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param userList contains the list of the users along with notification type in <person>|<notification type>
	 * @return HashMap a containing 'Email', 'IconMail' and 'Both' keys that represent Notification Type and has list of persons
	 * as their values
	 * @throws Exception if the operation fails
	 * @since X+2
	 */
	private static HashMap getRefineList(Context context, StringList userList) throws Exception
	{
		HashMap finalMap = new HashMap();
		StringList EmailToList = new StringList();
		StringList IconMailToList = new StringList();
		StringList BothToList = new StringList();
		StringTokenizer strTokenizer = null;


		finalMap.put(email,EmailToList);
		finalMap.put(iconMail,IconMailToList);
		finalMap.put(both,BothToList);
		if(userList!=null && userList.size()>0)
		{
			//Parse the toList to get the email persons List  IconMail persons List and Both persons list .
			for(int i =0;i<userList.size();i++)
			{
				String personNotify = userList.get(i).toString();
				strTokenizer = new StringTokenizer(personNotify, "|");

				if(strTokenizer.hasMoreTokens())
				{                	
					String toPerson = strTokenizer.nextToken();
					if(strTokenizer.hasMoreTokens())
					{
						String notifyType =strTokenizer.nextToken();
						if(email.equalsIgnoreCase(notifyType))
						{
							EmailToList.add(toPerson);
						}
						else if(iconMail.equalsIgnoreCase(notifyType))
						{
							IconMailToList.add(toPerson);
						}
						else if(both.equalsIgnoreCase(notifyType) || "".equalsIgnoreCase(notifyType.trim()))
						{
							BothToList.add(toPerson);
						}
					}
					else
					{
						BothToList.add(toPerson);
					}
				}	
			}
		}
		return finalMap;
	}
	/**
	 * resetCache.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an int 0 for success and non-zero for failure
	 */

	public int resetCache(Context context, String[] args)
	{
		synchronized(_lock) {
			programMap.clear();
			ageMap.clear();
		}
		return 0;
	}

	/**
	 * Specifies the component age value.
	 *
	 * @param age is a long value
	 */


	public void setComponentAge(long age)
	{
		synchronized(_lock) {
			_componentAge = age;
		}
	}

	/**
	 * Specifies the component age value.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - Contains Long value
	 * @return an int 0 for success and non-zero for failure
	 * @throws Exception if the operation fails
	 */

	public int setComponentAge(Context context, String[] args) throws Exception
	{
		Long l = new Long(args[0]);
		synchronized(_lock) {
			_componentAge = l.longValue();
		}
		return 0;
	}

    /**
     * This method gets the names and notification frequency preferences from the list.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param names from the list along with notification frequency preference will be added to this map
     * @param frequencies notification frequency preferences will be added to this map
     * @param list the list of recipients
     * @since AEF R213
     */

    protected static void getNamesAndFrequencyPreferences(Context context,
                                                HashMap<String,String> nameAndFrequency,
                                                final StringList names)
    {
        if (names != null) {
        	StringItr itr = new StringItr(names);
            while (itr.next()) {
            	if (nameAndFrequency.containsKey(itr.obj()) == false) {
                    String frequency = "";
                    try {
                        frequency = PropertyUtil.getAdminProperty(context, "user", itr.obj(), "preference_EmailNotifications");
                        if (frequency == null || frequency.length() == 0) {
                        	frequency = "default";
                        }
                    } catch (Exception ex) {
                    	frequency = "default";
                    }
                    nameAndFrequency.put(itr.obj(), frequency);
                }
            }
        }
    }
    protected static void getNotificationFrequencies(final HashMap<String,String> nameAndFrequency, final StringList names, HashSet<String> frequencies) {
        if (names != null && frequencies != null) {
        	StringItr itr = new StringItr(names);
            while (itr.next()) {
            	frequencies.add(nameAndFrequency.get(itr.obj()));
            }
        }
    }
	
    /**
     * This method gets names from the list with given notification frequency preference.
     *
     * @param list the list of recipients
     * @param nameAndFrequency map of key names and value frequency
     * @param frequency String holds the frequency we are filtering on
     * @return a StringList contains list of recipients expecting a notification in the given time frame
     * @since R213
     */

    protected static StringList getNamesForFrequency(final StringList list,
                                           final HashMap<String,String> nameAndFrequency,
                                           final String frequency)
    {
        StringList names = null;

        if (list != null) {
            names = new StringList(list.size());

            StringItr itr = new StringItr(list);
            while (itr.next()) {
                if (frequency.equals(nameAndFrequency.get(itr.obj()))) {
                    names.addElement(itr.obj());
                }
            }
        }

        return (names);
    }
    
	/**
	 * Called when showing preference choices.  Used in AEFEmailNotificationPreference
	 * command, "Access Program" and "Access Function" properties.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @return always return "true" to allow preference.
	 * @since BPS 2012x
	 */
    public boolean hasEmailNotificationAccess(Context context,String args[]) throws Exception {
    	return (true);
    }

	public static String getObjectLinkHTML(Context context, String text, String objectId) throws Exception {
		
		String link = getObjectLink(context, objectId);
		if (link != null && link.length() > 0) {
			link = "<a href='" + link + "'>" + text + "</a>";
		}
		else {
			link = text;
		}
		
		return (link);
	}

	public static String getObjectLink(Context context, String objectId) throws Exception {
		
		String link = "";
		String baseURL = emxMailUtil_mxJPO.getBaseURL(context, null);
		if (baseURL != null && baseURL.length() > 0) {
			link = baseURL + "?objectId=" + objectId;
		}
		
		return (link);
	}
}
