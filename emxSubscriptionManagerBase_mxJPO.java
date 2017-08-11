/*
 *  emxSubscriptionManagerBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;

/**
 * @version AEF 10-0-0-0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxSubscriptionManagerBase_mxJPO extends emxDomainObject_mxJPO
{
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10-0-0-0
     */
    public emxSubscriptionManagerBase_mxJPO (Context context, String[] args)
        throws Exception
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
     * @since AEF 10-0-0-0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
          if (true)
          {
              throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.SubscriptionManager.SpecifyMethodOnTriggerActionInvocation", context.getLocale().getLanguage()));
          }
          return 0;
    }

    /**
     * This method is used to notify occurence of event to all the users
     * subsribing it
     * @param context the eMatrix <code>Context</code> object
     * @param sObjectId = object on which event has occured
     * @param sEvent = name of event
     * @param sURLObject = object to pass in mail notifications
     * @returns 1 on failure 0 on success
     * @throws Exception if the operation fails
     * @since AEF 10-0-0-0
     */
    public void publishEvent (Context context, String sEvent, String sURLObject)
        throws Exception
    {
        if( com.matrixone.apps.common.SubscriptionManager.suspendSubscription(context) ) {
            return;
        }
        StringList strList = new StringList();
        strList.add(SELECT_TYPE);
        strList.add(SELECT_NAME);
        strList.add(SELECT_REVISION);
        strList.add(SELECT_ID);

        Map map = getInfo(context,strList);

        // Get Event property value
        String sEventProp = "emxComponents.Event." + sEvent.replace(' ', '_');
        String sEventPropValue = emxMailUtil_mxJPO.getMessage(context, sEventProp, null, null, null, "emxComponentsStringResource");

        // Get message to send
        String sMessageKey = "emxComponents.Event.Message";
        sMessageKey = sMessageKey + "." + sEvent.replace(' ', '_');
        String sMessageSubKeys[] = new String[7];
        sMessageSubKeys[0] = "event";
        sMessageSubKeys[1] = "type";
        sMessageSubKeys[2] = "name";
        sMessageSubKeys[3] = "rev";
        sMessageSubKeys[4] = "date";
        sMessageSubKeys[5] = "title";
        sMessageSubKeys[6] = "user";

        String sMessageSubKeyValues[] = new String[7];
        sMessageSubKeyValues[0] = sEventPropValue;
        sMessageSubKeyValues[1] = (String)map.get(SELECT_TYPE);
        sMessageSubKeyValues[2] = (String)map.get(SELECT_NAME);
        sMessageSubKeyValues[3] = (String)map.get(SELECT_REVISION);
        SimpleDateFormat sdf = new SimpleDateFormat();
        sMessageSubKeyValues[4] = sdf.format(new Date());

        // Get the attribute Value to display Tittle or Subject for Checkin\checkout and reply of messages
        if(sEvent.replace(' ', '_').equals("File_Check_Out") || sEvent.replace(' ', '_').equals("File_Checked_In")) {
          sMessageSubKeyValues[5] = getAttribute(context,"Title").getValue();
        } else if(sEvent.replace(' ', '_').equals("New_Reply")) {
          sMessageSubKeyValues[5] = getAttribute(context,"Subject").getValue();
        } else {
          sMessageSubKeyValues[5] = "";
        }
        sMessageSubKeyValues[6] = PersonUtil.getFullName(context,context.getUser());

        
        publishEvent(context, sEvent, sURLObject, sEventProp, null, null, sMessageKey, sMessageSubKeys, sMessageSubKeyValues);

    }

    public void publishEvent(Context context, String sEvent, String sURLObject, String subjectKey, String[] subjectKeyMacros, String[] subjectKeyMacroValues, String messageBodyKey, String[] messageBodyMacros, String[] messageBodyMacroValues) throws FrameworkException, Exception {

        if ( com.matrixone.apps.common.SubscriptionManager.suspendSubscription(context) )
        {
            return;
        }
        // Get all the users subscibed to specified event.
       // relationship pattern
        String relPattern = DomainConstants.RELATIONSHIP_PUBLISH_SUBSCRIBE + "," +
                            DomainConstants.RELATIONSHIP_PUBLISH + "," +
                            DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON + "," +
                            DomainConstants.RELATIONSHIP_PUSHED_SUBSCRIPTION;

        // type pattern
        String typePattern = DomainConstants.TYPE_PUBLISH_SUBSCRIBE + "," +
                             DomainConstants.TYPE_EVENT + "," +
                             DomainConstants.TYPE_PERSON;

        // business object select.
        StringList sl = new StringList();
        sl.addElement(SELECT_NAME);
        sl.addElement(SELECT_TYPE);
		//Bug# 305154 - Adding current state into selectables.
		sl.addElement(SELECT_CURRENT);

        // append sEvent with 'const' so that MQL can evalute the where clause correctly
        // MQL will fail if the value is a MQL key word, if not appended with 'const'
        String mqlEvent = "const'" + sEvent.replace('_', ' ') + "'";
        // object where
        String objWhere = "type == \"" + DomainConstants.TYPE_PUBLISH_SUBSCRIBE + "\" || " +
                          "(type == \"" + DomainConstants.TYPE_EVENT + "\" && attribute[" + DomainConstants.ATTRIBUTE_EVENT_TYPE + "] == " + mqlEvent + ") || " +
                          "type == \"" + DomainConstants.TYPE_PERSON + "\"";

        // return only Person objects.
        Pattern includeType = new Pattern(TYPE_PERSON);

        MapList ml = null;

        if("Part_Revised".equals(sEvent.replace(' ', '_')))
        {
            String strNewRevisedObj = getInfo(context,"last.id");
            DomainObject domObj = new DomainObject(strNewRevisedObj);
            ml = domObj.getRelatedObjects(context, relPattern, typePattern, sl, null, false, true, (short)
            3, objWhere, null, includeType, null, null);
        }
        else
        {
            ml = getRelatedObjects(context, relPattern, typePattern, sl, null, false, true, (short)3, objWhere, null, includeType, null, null);
        }

        // Get all the subscribed persons
        StringList lPersons = new StringList();

        for (int i = 0; i < ml.size(); i++)
        {
            Hashtable ht = (Hashtable)ml.get(i);
			//Bug# 305154 - Added below code to send mails to active persons only.
			if(STATE_PERSON_ACTIVE.equals((String)ht.get(SELECT_CURRENT))) {
				lPersons.addElement((String)ht.get("name"));
			}
        }
        // If no person found then return

        if (lPersons.size() > 0)
        {

            // Get objectid to pass in message.
            StringList lObj = null;
            if (sURLObject != null && sURLObject.length() > 0) {
              lObj = new StringList();
              lObj.addElement(sURLObject);
            }

            emxMailUtil_mxJPO.sendNotification(context, lPersons, null, null, subjectKey, null, null, messageBodyKey, messageBodyMacros, messageBodyMacroValues, lObj, null, "emxComponentsStringResource");

        }

    }

    /**
     * This is a trigger method used route object or message to user
     * specified by the value of an attribute.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds following arguments.
     *        args[0] = name of Event to publish
     *        args[1] = object id to be passed in publish message
     * @returns int 1 on failure and 0 on success
     * @throws Exception if the operation fails
     * @since AEF 10-0-0-0
     */
    public int publishEvent (Context context, String[] args)
        throws Exception
    {
        if (args != null && args.length == 2)
        {
            publishEvent(context, args[0], args[1]);
        }

        return 0;
    }
    /**
     * This method is used to get all subscription event of the context user
     *
     * @param context the eMatrix <code>Context</code> object
     * @returns MapList containing the subscription event details
     * @throws Exception if the operation fails
     * @since AEF 11-0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getSubscriptionDetailsforContextUser(Context
                context, String []args) throws Exception
    {
        MapList finalMapList = new MapList();
        com.matrixone.apps.common.Person currentUser = com.matrixone.apps.common.Person.getPerson(context);

        String attrEventType = "attribute[" + DomainConstants.ATTRIBUTE_EVENT_TYPE + "]";
        String attrIsRecursive = "attribute[" + DomainConstants.ATTRIBUTE_ISRECURSIVE + "]";

        StringList eventObjectSelect = new StringList();
        eventObjectSelect.add(DomainConstants.SELECT_ID);
        eventObjectSelect.add(attrEventType);
        StringList relSelect = new StringList();
        relSelect.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        relSelect.add(attrIsRecursive);

        //Fix for 370886. Person is connected to Event using 'Subscribed Person' and 'Pushed Subscription'
        //'Pushed Subscription' is added in the Relationship Pattern, while getting Event objects.
        MapList eventMapList = (MapList)currentUser.getRelatedObjects(context,
                DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON + "," + DomainConstants.RELATIONSHIP_PUSHED_SUBSCRIPTION,
                DomainConstants.QUERY_WILDCARD,
                eventObjectSelect,
                relSelect,
                true,
                false,
                (short)1,
                null,
                null);

        Iterator iterator = eventMapList.iterator();

        while(iterator.hasNext())
        {

            Hashtable tmpMap=(Hashtable)iterator.next();

            String eventId =(String)tmpMap.get(DomainConstants.SELECT_ID);
            eventId = eventId==null || "null".equals(eventId) ? "" : eventId;

            String relId=(String)tmpMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            relId = relId==null || "null".equals(relId) ? "" : relId;

            String isRecursive=(String)tmpMap.get(attrIsRecursive);
            isRecursive = isRecursive==null || "null".equals(isRecursive) ? "" : isRecursive;
            if(!(null == eventId || "null".equals(eventId) || "#DENIED!".equals(eventId) || "".equals(eventId)))
			{
            DomainObject myobj = new DomainObject(eventId);
            String eventType = (String)tmpMap.get(attrEventType);

            MapList dcObjectsMapList = myobj.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_PUBLISH+ "," + DomainConstants.RELATIONSHIP_PUBLISH_SUBSCRIBE,
                         DomainConstants.QUERY_WILDCARD,
                         new StringList(DomainConstants.SELECT_ID),
                         new  StringList(),
                         true,
                         false,
                         (short)2,
                         null,
                         null,
                         null,
                         new Pattern(DomainConstants.RELATIONSHIP_PUBLISH_SUBSCRIBE),
                         null);

            Iterator dcObjectMapItr = dcObjectsMapList.iterator();
            Hashtable dctmpMap;

            while(dcObjectMapItr.hasNext())
            {
                dctmpMap=(Hashtable)dcObjectMapItr.next();
                dctmpMap.put(attrEventType, eventType);
                dctmpMap.put(DomainConstants.SELECT_RELATIONSHIP_ID, relId);
                dctmpMap.put(attrIsRecursive, isRecursive);
                finalMapList.add(dctmpMap);
            }
			}
        }

        return finalMapList;

    }
    /**
     * This method is used to get the event type of the subscriptions
     *
     * @param context the eMatrix <code>Context</code> object
     * @returns vector containing the event type details
     * @throws Exception if the operation fails
     * @since AEF 11-0
     */
    public Vector getEventType(Context context, String []args) throws Exception {
        Vector vEventType = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList =(MapList)programMap.get("objectList");
        HashMap paramList = (HashMap)programMap.get("paramList");
        String languageStr = (String)paramList.get("languageStr");
        Iterator objItr = objectList.iterator();
		    Map objMap=null;
        while(objItr.hasNext()) {
            objMap = (Map)objItr.next();
            String eventType = (String)objMap.get("attribute["+PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_EventType)+"]");
            eventType = "emxComponents.Event."+eventType.replace(" ", "_");
            String i18nEvent = (String) EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),eventType);
            vEventType.add(i18nEvent);
        }
        return vEventType;
    }

    /**
     * This method is used to see if the event is recursive or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @returns vector containing the event type details
     * @throws Exception if the operation fails
     * @since AEF 11-0
     */
    public Vector isRecursive(Context context, String []args) throws Exception {
		Vector vRecurseVec = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList =(MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        String strLanguage      = (String)paramMap.get("languageStr");
        Iterator objItr = objectList.iterator();
		    Map objMap =null;
		    String rangeValue = "";
        while(objItr.hasNext()) {
            objMap = (Map)objItr.next();
             rangeValue = (String)objMap.get("attribute["+PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_IsRecursive)+"]");
             vRecurseVec.add(i18nNow.getRangeI18NString(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_IsRecursive), rangeValue ,strLanguage));

        }
        return vRecurseVec;
    }

}
