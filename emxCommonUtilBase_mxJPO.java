/*   emxCommonDocumentBase.java
**
**   Copyright (c) 2002-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the code for checkin
**
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

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;


/**
 * The <code>emxCommonUtilBase</code> class contains utility code for common.
 *
 * @version VCP 10.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonUtilBase_mxJPO extends emxDomainObject_mxJPO
{

		/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since Components R207
	 * @grade 0
	 */
	public emxCommonUtilBase_mxJPO (Context context, String[] args) throws Exception
	{
		super(context, args);
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns nothing
	 * @throws Exception if the operation fails
	 * @since Components R207
	 */
	public int mxMain(Context context, String[] args)
		throws Exception
	{
		if (true)
		{
			throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.MethodOnCommonFile", context.getLocale().getLanguage()));
		}
		return 0;
	}


	/**
	* Get mail notification in xml format
	*
	* @param context the eMatrix <code>Context</code> object
	* @param map  Holds the parameters passed from the calling method
	* @return Document - jdom object holding XML
	* @throws Exception if the operation fails
	* @since Components R207
	* @grade 0
	*/
	public com.matrixone.jdom.Document getMailXML(Context context, Map info) throws Exception
	{
		// get base url
		String baseURL = (String)info.get("baseURL");
		// get notification name
		String notificationName = (String)info.get("notificationName");
		HashMap eventCmdMap = UIMenu.getCommand(context, notificationName);
		String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
		String eventKey = "emxComponents.Notification.Event." + eventName.replace(' ', '_');
		String bundleName = (String)info.get("bundleName");
		String locale = ((Locale)info.get("locale")).toString();
		String i18NEvent = EnoviaResourceBundle.getProperty(context, bundleName, new Locale(locale), eventKey);
		String messageType = (String)info.get("messageType");

		// get document id
		String mainDocId = (String)info.get("id");
		// get document object info
		DomainObject mainDoc = DomainObject.newInstance(context, mainDocId);
		StringList selectList = new StringList(3);
		selectList.addElement(SELECT_TYPE);
		selectList.addElement(SELECT_NAME);
		selectList.addElement(SELECT_REVISION);
		Map mainDocInfo = mainDoc.getInfo(context, selectList);
		String mainDocType = (String)mainDocInfo.get(SELECT_TYPE);
		String i18NMainDocType = UINavigatorUtil.getAdminI18NString("type", mainDocType, locale);
		String mainDocName = (String)mainDocInfo.get(SELECT_NAME);
		String mainDocRev = (String)mainDocInfo.get(SELECT_REVISION);


		// header data
		HashMap headerInfo = new HashMap();
		headerInfo.put("header", i18NEvent + " : " + mainDocType + " " + mainDocName + " " + mainDocRev);

		// footer data
		HashMap footerInfo = new HashMap();
		ArrayList dataLineInfo = new ArrayList();
		if (messageType.equalsIgnoreCase("html"))
		{
			//String[] messageKeys = {"href", "type", "name", "revision"};
			String[] messageValues = new String[4];
			messageValues[0] = baseURL + "?objectId=" + mainDocId;
			messageValues[1] = i18NMainDocType;
			messageValues[2] = mainDocName;
			messageValues[3] = mainDocRev;
			String viewLink = MessageUtil.getMessage(context,null,
													 "emxComponents.Object.Event.Html.Mail.ViewLink",
													 messageValues,null,
													 context.getLocale(),bundleName);


			dataLineInfo.add(viewLink);
		} else {
			//String[] messageKeys = {"type", "name", "revision"};
			String[] messageValues = new String[3];
			messageValues[0] = i18NMainDocType;
			messageValues[1] = mainDocName;
			messageValues[2] = mainDocRev;
			String viewLink = MessageUtil.getMessage(context,null,
													 "emxComponents.Object.Event.Text.Mail.ViewLink",
													 messageValues,null,
													 context.getLocale(),bundleName);


			dataLineInfo.add(viewLink);
			dataLineInfo.add(baseURL + "?objectId=" + mainDocId);
		}

		footerInfo.put("dataLines", dataLineInfo);

		return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, null, footerInfo));

	}

	/**
	* Get the notification in text format
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  Holds the parameters passed from the calling method
	* @return String - the text message of the notification
	* @throws Exception if the operation fails
	* @since Components R207
	* @grade 0
	*/
	public String getNotificationText(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "text");
		com.matrixone.jdom.Document doc = getMailXML(context, info);

		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));

	}

	/**
	* Get the notification in HTML format
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args  Holds the parameters passed from the calling method
	* @return String - the HTML message of the notification
	* @throws Exception if the operation fails
	* @since Components R207
	* @grade 0
	*/
	public String getNotificationHTML(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "html");
		com.matrixone.jdom.Document doc = getMailXML(context, info);

		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));

    }


	/**
	* Method retrieves All revisions of the Context Object
	* @param context the eMatrix <code>Context</code> object
	* @param args holds arguments
	* @return MapList - returns the Maplist of revisions
	* @throws Exception if the operation fails
	* @since Components R207
	* @grade 0
	*/
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllRevisions(Context context, String[] args) throws Exception
	{
	    Map programMap = (HashMap) JPO.unpackArgs(args);
	    String strObjectId = (String)  programMap.get("objectId");
	    setId(strObjectId);
	    StringList objectList= new StringList(DomainConstants.SELECT_ID);
	    StringList multivalueList= new StringList();
	    //Function call to retrive the information of the revisions of context object, based on the object selects.
	    MapList relBusObjList = this.getRevisionsInfo(context,objectList,multivalueList);
	    return relBusObjList;
    }

    /**
    * Get mail notification for Content added or Deleted in xml format
    *
    * @param context the eMatrix <code>Context</code> object
    * @param info contains a Map with the following keys:
    *        baseURL          - String containing base URL
    *        notificationName - String containing the name of the Notification Event
    *        bundleName       - String containing the name of the Resource bundle
    *        locale           - java.util.Locale Object containg locale information
    *        messageType      - String mentioning the type of the message ,allowed values are "html" or "text"
    *        id               - String containing the Object Id of the Object on which Notification Event Triggered
    *
    * @return Document - jdom object holding XML
    * @throws Exception if the operation fails
    */
    public com.matrixone.jdom.Document getContentAddDeleteMailXML(Context context, Map info) throws Exception
    {
        // get base url
        String baseURL          = (String)info.get("baseURL");
        // get notification name
        String notificationName = (String)info.get("notificationName");
        HashMap eventCmdMap     = UIMenu.getCommand(context, notificationName);
        String eventName        = UIComponent.getSetting(eventCmdMap, "Event Type");
        String eventKey         = "emxComponents.Notification.Event." + eventName.replace(' ', '_');
        String bundleName       = (String)info.get("bundleName");
        String locale           = ((Locale)info.get("locale")).toString();
        String i18NEvent        = EnoviaResourceBundle.getProperty(context, bundleName, new Locale(locale), eventKey);
        String i18NIn           = EnoviaResourceBundle.getProperty(context, bundleName, new Locale(locale), "emxComponents.Notification.Event.In");
        String messageType      = (String)info.get("messageType");

        // get from Object id
        String fromObjectId     = (String)info.get("id");

        // get from object info
        DomainObject fromObject = DomainObject.newInstance(context, fromObjectId);
        StringList selectList   = new StringList(3);
        selectList.addElement(SELECT_TYPE);
        selectList.addElement(SELECT_NAME);
        selectList.addElement(SELECT_REVISION);

        Map fromObjectInfo          = fromObject.getInfo(context, selectList);
        String fromObjectType       = (String)fromObjectInfo.get(SELECT_TYPE);
        String i18NfromObjectType   = UINavigatorUtil.getAdminI18NString("type", fromObjectType, locale);
        String fromObjectName       = (String)fromObjectInfo.get(SELECT_NAME);
        String fromObjectRev        = (String)fromObjectInfo.get(SELECT_REVISION);


        // header data
        HashMap headerInfo = new HashMap();
        StringBuffer strBufHeaderInfo = new StringBuffer();
        strBufHeaderInfo.append(i18NEvent).append(" ");
        strBufHeaderInfo.append(i18NIn).append(" ");
        strBufHeaderInfo.append(fromObjectType).append(" ");
        strBufHeaderInfo.append(fromObjectName).append(" ");
        strBufHeaderInfo.append(fromObjectRev);

        headerInfo.put("header",strBufHeaderInfo.toString());

        // footer data
        HashMap footerInfo = new HashMap();
        ArrayList footerInfoList = new ArrayList();

        // getting Details of Added or Removed Content
        String toObjectType     = MqlUtil.mqlCommand(context, "get env TOTYPE");
        String toObjectName     = MqlUtil.mqlCommand(context, "get env TONAME");
        String toObjectRevision = MqlUtil.mqlCommand(context, "get env TOREVISION");

        // adding Content Details to message
        StringBuffer contentDetails = new StringBuffer();
        contentDetails.append(i18NEvent).append(" : ");
        contentDetails.append(toObjectType).append(" ");
        contentDetails.append(toObjectName);
        //contentDetails.append(toObjectRevision);

        footerInfoList.add(contentDetails.toString());

        if (messageType.equalsIgnoreCase("html"))
        {
            footerInfoList.add("<br></br>");

            String[] fromObjectValues = new String[4];
            fromObjectValues[0] = baseURL + "?objectId=" + fromObjectId;
            fromObjectValues[1] = i18NfromObjectType;
            fromObjectValues[2] = fromObjectName;
            fromObjectValues[3] = fromObjectRev;
            String fromObjectViewLink  = MessageUtil.getMessage(context,null,
                                                        "emxComponents.Object.Event.Html.Mail.ViewLink",
                                                        fromObjectValues,null,
                                                        context.getLocale(),bundleName);
            footerInfoList.add(fromObjectViewLink);
        } else {
            String[] fromObjectValues = new String[3];
            fromObjectValues[0] = i18NfromObjectType;
            fromObjectValues[1] = fromObjectName;
            fromObjectValues[2] = fromObjectRev;
            String fromObjectViewLink  = MessageUtil.getMessage(context,null,
                                                        "emxComponents.Object.Event.Text.Mail.ViewLink",
                                                        fromObjectValues,null,
                                                        context.getLocale(),bundleName);
            footerInfoList.add(fromObjectViewLink);
            footerInfoList.add(baseURL + "?objectId=" + fromObjectId);
        }

        footerInfo.put("dataLines", footerInfoList);

        return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, null, footerInfo));

    }

    /**
    * Get the notification in text format for Content added or Content deleted Event
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains a Map with the following entries:
    *        baseURL          - String containing base URL
    *        notificationName - String containing the name of the Notification Event
    *        bundleName       - String containing the name of the Resource bundle
    *        locale           - java.util.Locale Object containg locale information
    *        id               - String containing the Object Id of the Object on which Notification Event Triggered
    * @return String - the text message of the notification
    * @throws Exception if the operation fails
    */
    public String getContentAddDeleteNotificationText(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "text");
        com.matrixone.jdom.Document doc = getContentAddDeleteMailXML(context, info);

        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));

    }

    /**
    * Get the notification in HTML format for Content added or Content deleted Event
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains a Map with the following entries:
    *        baseURL          - String containing base URL
    *        notificationName - String containing the name of the Notification Event
    *        bundleName       - String containing the name of the Resource bundle
    *        locale           - java.util.Locale Object containg locale information
    *        id               - String containing the Object Id of the Object on which Notification Event Triggered
    * @return String - the HTML message of the notification
    * @throws Exception if the operation fails
    */
    public String getContentAddDeleteNotificationHTML(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "html");
        com.matrixone.jdom.Document doc = getContentAddDeleteMailXML(context, info);

        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));

    }

	/**
	* Used in Policy access filters to check if a role has necessary permissions based on an access program.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args contains a Map with the ObjectId, JPO program name and JPO function name :
	*
	* @return boolean - true (if function that is getting called provides access),
	*                   false(if function that is getting called does not provide access)
	*                   if function is not there return the  default value true
	* @throws Exception if the operation fails
	*/
	public Boolean checkAccess (Context context, String[] args)throws Exception
	{
		Boolean returnValue = new Boolean(true);
		String programName = (String) args [1];
		String functionName = (String) args[2];
		DebugUtil.debug("programName ==>"+programName);
		DebugUtil.debug("functionName ==>"+functionName);
		try
		{
			returnValue = (Boolean)JPO.invoke(context, programName, null, functionName, args, Class.forName("java.lang.Boolean"));
		}
		catch(Exception e)
		{
			DebugUtil.debug("checkAccess-----Exception= "+ e.toString());
		}
		return Boolean.valueOf(returnValue);
	}
}
