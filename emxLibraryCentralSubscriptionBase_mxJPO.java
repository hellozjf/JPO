/*
 *  emxLibraryCentralSubscriptionBase.java
 * 
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of MatrixOne,
 *  Inc.  Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 */

import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;

import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.library.LibraryCentralConstants;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.Pattern;

import com.matrixone.apps.domain.util.MapList;
import matrix.util.StringList;

/**
 * The <code>emxLibraryCentralSubscriptionBase</code> class manages the Subscriptiondetails.
 *
 * @exclude
 */

public class emxLibraryCentralSubscriptionBase_mxJPO extends emxTransactionNotificationUtil_mxJPO
{

	static String TRANS_HISTORY_WAS_DELIMITER = "was:";
	static String TRANS_HISTORY_REVISION_DELIMITER = "revision:";
    
	/**
	 *  Creates ${CLASSNAME} object.
	 */
    public emxLibraryCentralSubscriptionBase_mxJPO(Context context, String []args) 
        throws Exception
    {
    	 super(context, args);
    }

     /**
      * This method is executed if a specific method is not specified.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args the Java <code>String[]</code> object
      * @return int
      * @throws Exception if the operation fails
	  * @exclude
      */
     public int mxMain (Context context, String[] args) throws Exception
     {
         if (true)
         {
            throw new Exception (
            "Must specify method on emxLibraryCentralSubscription invocation");
         }

         return 0;
     }

    /**
     * Gets all Event object connected to objectId passed thrugh
     * Publish Subscribe object and connected to logged in User
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the holds following input arguments:
     *    0 - HashMap containing one String entry for key "objectId"
     * @return theTreeMap with all objectEvents
     * @throws Exception if the operation fails
     */
    public TreeMap getAllObjectEvents(matrix.db.Context context,
                                    String []args  ) throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
          throw (new IllegalArgumentException());
        }

        Map map = (Map) JPO.unpackArgs(args);
        String objectId = (String)map.get("objectId");

        //get the symbolic names

        String sTypeEvent = PropertyUtil.getSchemaProperty(context,"type_Event");

        String sRelPublishSubscribe =
            PropertyUtil.getSchemaProperty(context,"relationship_PublishSubscribe");

        String sRelSubscribedPerson =
            PropertyUtil.getSchemaProperty(context,"relationship_SubscribedPerson");

        String sRelPublish =
            PropertyUtil.getSchemaProperty(context,"relationship_Publish");

        String sAttType =
            PropertyUtil.getSchemaProperty(context,"attribute_EventType");

        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("attribute[");
        strBuffer.append(sAttType);
        strBuffer.append("]");

        //create relationship pattern for search

        Pattern relPattern = new Pattern(sRelPublishSubscribe);
        relPattern.addPattern(sRelPublish);
        relPattern.addPattern(sRelSubscribedPerson);

        //create type patern for search

        Pattern typePattern = new Pattern(sTypeEvent);

        //Build select params for object

        StringList objSelect = new StringList();

        objSelect.addElement(DomainConstants.SELECT_TYPE );
        objSelect.addElement(DomainConstants.SELECT_ID );
        objSelect.addElement(strBuffer.toString());

        //Build select params for relationship

        StringList relSelect = new StringList();

        String objWhere = "";

        DomainObject domainObj = new DomainObject();
        domainObj.setId(objectId);

        //Get all the event objects connected to objectId passed

        MapList result = domainObj.getRelatedObjects( context,
            relPattern.getPattern(), LibraryCentralConstants.QUERY_WILDCARD, objSelect, relSelect, false, true,
            (short)3, objWhere, null, typePattern, null, null);

        //create treeMap with Event type and Object Id as name and value

        Hashtable temp;
        TreeMap treeMap = new TreeMap();

        Iterator iterator = result.iterator();

        while(iterator.hasNext())
        {
            temp = (Hashtable)iterator.next();
            treeMap.put(temp.get(strBuffer.toString()),
                temp.get(DomainConstants.SELECT_ID));
        }

        //filter out Events which are not connected to logged in user

        TreeMap filteredMap =
            filterOutUnWantedEvents(context, treeMap);

        return filteredMap;
    }

    /**
     * This method filters out Events which are not connected to logged in user
     *
    * @param context the eMatrix <code>Context</code> object
     * @param treeMap <code>TreeMap</code> object
     * @return a Java <code>TreeMap</code>
     * @throws FrameworkException if the operation fails
     */
    protected TreeMap filterOutUnWantedEvents(Context context, TreeMap treeMap)
        throws FrameworkException
    {

        TreeMap result = new TreeMap();

        java.util.Set set = treeMap.keySet();
        Iterator key = set.iterator();

        //iterate thru all the object id passed

        while(key.hasNext())
        {
            String strKey = (String)key.next();
            String strValue = (String)treeMap.get(strKey);

            //check if this object is connected to logged in user

            boolean personExists = checkForConnectedPerson(context, strValue);

            if(personExists)
            {
                result.put(strKey, strValue);
            }
        }

        return result;
    }

    /**
     * This method gets all the Person Objects connected to Event object
     * id passed and return true if logged in user is present
     *
     * @param context <code>Context</code> object
     * @param eventId the event ID
     * @return boolean true if logged in user is present 
     * @throws FrameworkException if the operation fails
     */
    protected boolean checkForConnectedPerson(Context context, String eventId)
        throws FrameworkException
    {
        //get the symbolic names
        String sTypeEvent = PropertyUtil.getSchemaProperty(context,"type_Event");

        String sRelSubscribedPerson =
            PropertyUtil.getSchemaProperty(context,"relationship_SubscribedPerson");

        String sTypePerson = PropertyUtil.getSchemaProperty(context,"type_Person");

        DomainObject domainObj = new DomainObject();
        domainObj.setId(eventId);

        //create relationship pattern for search

        Pattern relPattern = new Pattern(sRelSubscribedPerson);

        //create type patern for search

        Pattern typePattern = new Pattern(sTypeEvent);

        //Build select params for object

        StringList objSelect = new StringList();
        objSelect.addElement(DomainConstants.SELECT_ID);
        objSelect.addElement(DomainConstants.SELECT_NAME);

        //Build select params for relationship

        StringList relSelect = new StringList();

        MapList mapList = domainObj.getRelatedObjects(context,
            relPattern.getPattern(), LibraryCentralConstants.QUERY_WILDCARD, objSelect, relSelect, false, true,
            (short)1, null, null);

        //create treeMap with object id and instance name as name and value

        Hashtable temp;
        TreeMap treeMap = new TreeMap();

        Iterator iterator = mapList.iterator();

        while(iterator.hasNext())
        {
            temp = (Hashtable)iterator.next();
            treeMap.put(temp.get(DomainConstants.SELECT_ID),
                temp.get(DomainConstants.SELECT_NAME));
        }

        //check if logged in user is present in the treeMap

        boolean ownerExists = checkIfOwnerExists(context, treeMap);
        return ownerExists;
    }

    /**
     * This method return true if the logged in user is present in the treeMap passed
     *
     * @param context <code>Context</code> object
     * @param treeMap TreeMap with person event details
     *
     * @return boolean true if the logged in user is present in the treeMap passed
     */
    protected boolean checkIfOwnerExists(Context context, TreeMap treeMap)
    {
        java.util.Set set = treeMap.keySet();
        Iterator key = set.iterator();

        boolean exists = false;

        String strKey = null;
        String strValue = null;

        while(key.hasNext())
        {
            strKey = (String)key.next();
            strValue = (String)treeMap.get(strKey);

            if(strValue.equals(context.getUser()))
            {
                exists = true;

                // Break if user found
                break;
            }
        }

        return exists;
    }

    /**
     * Connects event to person logged in, if already connected then does nothing
     *
     * @param context <code>Context</code> object
     * @param evtId the event Id
     * @throws FrameworkException if the operation fails
     * @throws MatrixException if the operation fails
     */
    protected void connectEventPerson(Context context,
            String evtId) throws FrameworkException, MatrixException
    {

        // Check if User is already connected to Event object id passed
        boolean relExists = checkForConnectedPerson(context, evtId);
        if(relExists)
        {
            // return without any operation if User is already connected

            return;
        }

        //Get sysmbolic name

        String sRelSubscribePerson =
            PropertyUtil.getSchemaProperty(context,"relationship_SubscribedPerson");
        DomainObject evtObj = new DomainObject();
        evtObj.setId(evtId);

        Person person = Person.getPerson(context);

        evtObj.connect(context, new RelationshipType(sRelSubscribePerson),
            true,person);
    }

    /**
     * Creates event object, set the event type attribute and connect
     * it to logged in person and Publish Subscribe object
     *
     * @param context <code>Context</code> object
     * @param psId person id
     * @param attributeValue the attribute Value
     * @throws FrameworkException if the operation fails
     * @throws MatrixException if the operation fails
     */
    protected void createEventObject(Context context,
            String psId, String attributeValue) throws FrameworkException,
            MatrixException
    {

        // Get symbolic name

        String sAttrEventType =
            PropertyUtil.getSchemaProperty(context,"attribute_EventType");

        String sRelPublish =
            PropertyUtil.getSchemaProperty(context,"relationship_Publish");

        String sRelSubscribePerson =
            PropertyUtil.getSchemaProperty(context,"relationship_SubscribedPerson");

        String sEventId = FrameworkUtil.autoName(context, "type_Event", "",
            "policy_Event", context.getVault().getName());
        DomainObject eventObj = new DomainObject();
        eventObj.setId(sEventId);

        DomainObject psObj = new DomainObject();
        psObj.setId(psId);
        psObj.connect(context, new RelationshipType(sRelPublish),true,eventObj);
        Person person = Person.getPerson(context);
        eventObj.connect(context, new RelationshipType(sRelSubscribePerson),
            true,person);
        eventObj.setAttributeValue(context,sAttrEventType,attributeValue);

        eventObj.update(context);
    }

    /**
     * Returns the object id of subscribtions connected to subscribed object,if it dosent exists, create it
     *
     * @param context <code>Context</code> object
     * @param parentId id of the parent
     * @param createNew 
     *
     * @return String id of subscribtions
     * @throws FrameworkException if the operation fails
     * @throws MatrixException if the operation fails
     */
    public static String getPublicSubscribeObject(Context context,
                                    String parentId, boolean createNew)
                                    throws FrameworkException, MatrixException
    {

        String sTypePublishSubscribe =
            PropertyUtil.getSchemaProperty(context,"type_PublishSubscribe");
        String sRelPublishSubscribe  =
            PropertyUtil.getSchemaProperty(context,"relationship_PublishSubscribe");
        //create relationship patern for search

        Pattern relPattern = new Pattern(sRelPublishSubscribe);

        //create type patern for search

        Pattern typePattern = new Pattern(sTypePublishSubscribe);

        //Build select params for object

        StringList objSelect = new StringList();
        objSelect.addElement( DomainConstants.SELECT_ID ) ;

        //Build select params for relationship

        StringList relSelect = new StringList();

        DomainObject domainObj = new DomainObject();
        domainObj.setId(parentId);

        //to implement type pattern

        MapList maplist = domainObj.getRelatedObjects( context,
            relPattern.getPattern(), LibraryCentralConstants.QUERY_WILDCARD, objSelect, relSelect, false, true,
            (short)1, null, null ) ;

        Hashtable temp;
        Hashtable treeMap = new Hashtable();

        Iterator iterator = maplist.iterator();

        while(iterator.hasNext())
        {
            temp = (Hashtable)iterator.next();
            treeMap.put(temp.get(DomainConstants.SELECT_ID),
                temp.get(DomainConstants.SELECT_ID));
        }

        //if PS object is not found i.e. maplist.size() == 0 then create it

        String psId = null;
        if(treeMap.size() == 0)
        {
            if( createNew )
            {
                psId = FrameworkUtil.autoName(context, "type_PublishSubscribe",
                "", "policy_PublishSubscribe", context.getVault().getName());

                //Use SubscriptionManager.initiate() method to connect Publish
                //Subscribe Object thru Super user

                Workspace workspace = new Workspace();
                workspace.setId(parentId);
                workspace.getSubscriptionManager().initiate(context,
                                                           parentId, psId);
            }
        }
        else
        {
            //treeMap has all
            java.util.Set hashSubEvntKeys = treeMap.keySet();

            Iterator itEvents = hashSubEvntKeys.iterator();

            psId = (String)itEvents.next();
        }
        return psId;
    }

   /**
     * Processes the Subscribed Events
     *
     * @param context <code>Context</code> object
     * @param args holds following arguments:
     *    0 - parentId parentId for Subscription
     *    1 - vectorSubEvents the Java <code>Vector</code> containing al Sub events
     * @throws FrameworkException if the operation fails
     */
    public void processSubscribeEvents(Context context, String []args)
                                            throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
          throw (new IllegalArgumentException());
        }
        Map map = (Map) JPO.unpackArgs(args);

        String parentId = (String)map.get("parentId");
        Vector vctSubEvtStr = (Vector)map.get("vectorSubEvents");

       // Get Symbolic Names

        String sAttrEventType =
            PropertyUtil.getSchemaProperty(context,"attribute_EventType");

        String sRelPublish =
            PropertyUtil.getSchemaProperty(context,"relationship_Publish");

        String sTypeEvent =
            PropertyUtil.getSchemaProperty(context,"type_Event");

        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("attribute[");
        strBuffer.append(sAttrEventType);
        strBuffer.append("]");

        //get ps id of existing or new publish subscribe object

        String psId = getPublicSubscribeObject(context, parentId, true);


        //create relationship patern for search

        Pattern relPattern = new Pattern(sRelPublish);

        //create type patern for search

        Pattern typePattern = new Pattern(sTypeEvent);

        //Build select params for object

        StringList objSelect = new StringList();
        objSelect.addElement( DomainConstants.SELECT_ID ) ;
        objSelect.addElement(strBuffer.toString()) ;

        //Build select params for relationship

        StringList relSelect = new StringList();

        DomainObject domainObj = new DomainObject();
        domainObj.setId(psId);

        //Get all Events connected to PS

        MapList maplist = domainObj.getRelatedObjects(context,
            relPattern.getPattern(), LibraryCentralConstants.QUERY_WILDCARD, objSelect, relSelect,
            false, true, (short)1, null, null) ;


        Hashtable temp;
        Hashtable treeMap = new Hashtable();

        Iterator iterator = maplist.iterator();

        while(iterator.hasNext())
        {
            temp = (Hashtable)iterator.next();
            treeMap.put(temp.get(strBuffer.toString()),
                temp.get(DomainConstants.SELECT_ID));
        }
        //check if event object's event type attribute exists in vctSubEvtStr
        //if exists remove it from evtKeys and connect it to user

        Enumeration e = treeMap.keys();

        while(e.hasMoreElements())
        {
            String key = (String)e.nextElement();

            boolean existsInSubList = vctSubEvtStr.contains(key);

            if(existsInSubList)
            {
                vctSubEvtStr.remove(key);

                String subId = (String)treeMap.get(key);
                connectEventPerson(context, subId);
            }
        }

        //Create event object with the left over keys in vctSubEvtStr

        for(int i = 0; i < vctSubEvtStr.size(); i++)
        {
            createEventObject(context, psId,
                (String)vctSubEvtStr.elementAt(i));
        }
    }

    /**
     * Processes the  UnSubscribed Events
     *
     * @param context <code>Context</code> object
     * @param args hols following arguments
     *    0 - unSubEvents token separated string of event ids to unsubsribe
     * @throws Exception if the operation fails
     */
    public void processUnSubscribeEvents(Context context, String []args)
                                                            throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
          throw (new IllegalArgumentException());
        }

        Map map = (Map) JPO.unpackArgs(args);

        String strUnSubEvtIds = (String)map.get("unSubEvents");

        String sRelSubscribePerson =
            PropertyUtil.getSchemaProperty(context,"relationship_SubscribedPerson");

        StringTokenizer st = new StringTokenizer(strUnSubEvtIds, ";");

        // Disconnect Person and Event object

        while (st.hasMoreTokens())
        {
            DomainObject domainObj = new DomainObject() ;
            domainObj.setId(st.nextToken()) ;

            Person person = Person.getPerson(context);

            person.disconnect(context, new RelationshipType(
                sRelSubscribePerson), false, domainObj);
        }
    }

    /**
     * Gets the Subscription Details
     *
     * @param context <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public static MapList getSubscriptionDetailsforContextualUser(Context
                context, String []args) throws Exception
    {

        String sRelPublishSubscribe =
            PropertyUtil.getSchemaProperty(context,"relationship_PublishSubscribe");

        String sRelSubscribedPerson =
            PropertyUtil.getSchemaProperty(context,"relationship_SubscribedPerson");

        String sRelPublish =
            PropertyUtil.getSchemaProperty(context,"relationship_Publish");

        String sAttType =
            PropertyUtil.getSchemaProperty(context,"attribute_EventType");

        MapList finalMapList = new MapList();
        Person currentUser = Person.getPerson(context);

        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("attribute[");
        strBuffer.append(sAttType);
        strBuffer.append("]");

        StringList eventObjectSelect = new StringList();
        eventObjectSelect.add(DomainConstants.SELECT_ID);
        eventObjectSelect.add(strBuffer.toString());

        MapList eventMapList = (MapList)currentUser.getRelatedObjects(context,
        sRelSubscribedPerson,LibraryCentralConstants.QUERY_WILDCARD,eventObjectSelect,new  StringList(),true,false,
                                                        (short)1,null,null);

        Iterator iterator = eventMapList.iterator();

        Pattern psRelPattern= new Pattern(sRelPublishSubscribe);

        Hashtable tmpMap;

        StringList dcObjectSelects = new StringList();
        dcObjectSelects.add(DomainConstants.SELECT_ID);
        dcObjectSelects.add(DomainConstants.SELECT_NAME);
        dcObjectSelects.add(DomainConstants.SELECT_REVISION);
        dcObjectSelects.add(DomainConstants.SELECT_TYPE);
        dcObjectSelects.add(DomainConstants.SELECT_DESCRIPTION);

        while(iterator.hasNext())
        {
            tmpMap=(Hashtable)iterator.next();
            String eventId=(String)tmpMap.get(DomainConstants.SELECT_ID);
            DomainObject myobj = new DomainObject(eventId);
            String eventType=(String)tmpMap.get(strBuffer.toString());

            MapList dcObjectsMapList=myobj.getRelatedObjects(context,
                         sRelPublish+","+sRelPublishSubscribe,LibraryCentralConstants.QUERY_WILDCARD, dcObjectSelects,
                         new  StringList(), true, false, (short)2, null,
                         null, null,psRelPattern,null);

            Iterator dcObjectMapItr = dcObjectsMapList.iterator();
            Hashtable dctmpMap;

            while(dcObjectMapItr.hasNext())
            {
                dctmpMap=(Hashtable)dcObjectMapItr.next();
                dctmpMap.put(strBuffer.toString(),eventType);
                dctmpMap.put("eventId",eventId);
                finalMapList.add(dctmpMap);
            }
        }

        return finalMapList;

    }
    
    /**
     * Method sends the notifications to user based on history actions parsed
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *      0 - macro TRANSHISTORY from transaction trigger
     * @return integer denoting success or failure of notification sent .
     * @throws Exception if the operation fails
     **/
    public int transactionNotifications(Context context, String[] args) throws Exception
    {
        int result = 0;
        String transHistories = args[0];

        if(transHistories != null && !"".equals(transHistories)) {
            try {
                Context frameContext = context.getFrameContext("emxLibraryCentralSubscriptionBase");
                BackgroundProcess backgroundProcess = new BackgroundProcess();
                backgroundProcess.submitJob(frameContext, "emxLibraryCentralSubscriptionBase", "notifyInBackground", args , (String)null);
            } catch(Exception ex) {
                ContextUtil.abortTransaction(context);
                ex.printStackTrace();
                throw ex;
            }
        }
        return result;
    }
    
  
    /**
     * Overridden Method to parse the History Events string to return stringlist of events with | separated
     * 
     * This method is overridden to parse the promote/demote events with state (e.g promote to Active_)
     * 
     * @param strEventsHistory is a String containing the list of events occured on a particular object
     * @return StringList containing list of event names,time stamp with | separated
     * @throws Exception if the operation fails
     **/

    public StringList parseEvents(Context context,String strEventsHistory) throws Exception
    {
        StringList events   = new StringList();
        String strTemp      = "";
        int iStartEvent     = 0;
        String strUser      = "";
        String strTime      = "";
        boolean isAMPM      = false;
        int noOfColuns      = 2;
        String dateFormat   = matrix.db.Environment.getValue(context,"MX_NORMAL_DATETIME_FORMAT");
        if( dateFormat == null || "".equals(dateFormat) || "null".equals(dateFormat) )  {
            dateFormat = super.DEFAULT_DATE_FORMAT;
        }
        if( dateFormat.indexOf("mer") >= 0 ) {
            isAMPM = true;
        }

        // find out how many columns are present in the date format
        int colunIndex      = dateFormat.indexOf(":");
        if( colunIndex == -1 ) {
            noOfColuns = 0;
        } else if( colunIndex == dateFormat.lastIndexOf(":")) {
            noOfColuns = 1;
        }
        int iEventIndex = strEventsHistory.indexOf(super.TRANS_HISTORY_EVENT_DELIMITER);
        while(iEventIndex != -1) {
            strTemp                 = strEventsHistory.substring(0,iEventIndex).trim();
            StringBuffer strEvent   = new StringBuffer();
            iStartEvent             = strTemp.lastIndexOf("\n");
            strEvent.append(strEventsHistory.substring(iStartEvent+1,iEventIndex).trim());
            if (strEvent.toString().equals("promote") || strEvent.toString().equals("demote")) {
                strEvent.append(" to ");
                strEvent.append(strEventsHistory.substring(strEventsHistory.lastIndexOf(':')+1,strEventsHistory.length()).trim());
            }
            
            strEventsHistory        = strEventsHistory.substring(iEventIndex+7);
            if(strEvent.toString().startsWith("change")||strEvent.toString().startsWith("modify")){
                if(!isModified(strEventsHistory)){
                    iEventIndex = strEventsHistory.indexOf(super.TRANS_HISTORY_EVENT_DELIMITER);
                    continue;
                }
            }
            int itimeIndex          = strEventsHistory.indexOf("time:");
            if(itimeIndex != -1) {

                //User value being set as a Global RPE variable
                if(strUser.equals(""))
                {
                    strUser = strEventsHistory.substring(0,itimeIndex).trim();
                    PropertyUtil.setGlobalRPEValue(context,"USER","\""+strUser+"\"");
                }

                // Finding the string between the time: and the next token:
                colunIndex      = itimeIndex + 4;
                int tempIndex   = colunIndex;
                strTime         = strEventsHistory;
                for (int i = 0; i <= noOfColuns; i++ ) {
                    strTime = strTime.substring(tempIndex+1,strTime.length());
                    tempIndex = strTime.indexOf(":");
                    colunIndex += (tempIndex+1);
                    if( i == noOfColuns ) {
                        // This string should return Eg : 7/1/2008 4:36:51 PM  state
                        strTime = strEventsHistory.substring(itimeIndex+5,colunIndex);
                    }
                }

                int iTimeEndIndex = -1;
                if ( isAMPM ) {
                    iTimeEndIndex = strTime.indexOf(super.TRANS_HISTORY_TIME_DELIMITER1)==-1? strTime.indexOf(super.TRANS_HISTORY_TIME_DELIMITER2) : strTime.indexOf(super.TRANS_HISTORY_TIME_DELIMITER1);
                }
                //Validating if AM / PM exists along with time stamp. if not then take the time from the last indexof :
                if(iTimeEndIndex == -1)
                    iTimeEndIndex = strTime.lastIndexOf(":");
                else
                    iTimeEndIndex = iTimeEndIndex -1 ;

                String strTimeStamp = strTime.substring(0,iTimeEndIndex+3).trim();
                strEvent.append("_").append(strTimeStamp);
                events.add(strEvent.toString());    
            }else {
                events.add(strEvent);
            }

            iEventIndex = strEventsHistory.indexOf(super.TRANS_HISTORY_EVENT_DELIMITER);
        }
        return events;
    }

   
    /**
     * Overridden Method to filter the list of events subscribed by user for notification
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param strlNotificationCommandName contains list of eventcommands subscribed for notification.
     * @return StringList contains the list of filtered events for notification.
     * @throws Exception if the operation fails
    */
    public StringList loadEvents (Context context, StringList strlNotificationCommandName ){
        StringList slNotificationEvents = new StringList();
        String strEventHistory          = "";
        for (int i = 0; i < strlNotificationCommandName.size(); i++) {
            strEventHistory = (String) strlNotificationCommandName.get(i);
            if (strEventHistory.indexOf("modify_") >= 0 ||
                strEventHistory.indexOf("revisioned_")  >= 0 ||
                strEventHistory.indexOf("lock_")  >= 0 ||
                strEventHistory.indexOf("promote to")  >= 0 ||
                strEventHistory.indexOf("demote to")  >= 0) 
            {
                slNotificationEvents.add(strEventHistory);
            } else if (strEventHistory.indexOf("change name_")  >= 0) {
                slNotificationEvents.add(strEventHistory.replaceFirst("change name_","modify_"));
            } else if (strEventHistory.indexOf("change owner_")  >= 0) {
                slNotificationEvents.add(strEventHistory.replaceFirst("change owner_","modify_"));
            } else if (strEventHistory.indexOf("change policy_")  >= 0) {
                slNotificationEvents.add(strEventHistory.replaceFirst("change policy_","modify_"));
            }
        }
        return slNotificationEvents;
    }
    
    
    /**
     * Method to check if the value is modified in the transaction history
     * 
     * @param strEventsHistory is a String containing the list of events
     * which are to be parsed,Always First Event in the List is checked for modification 
     * @return boolean true if value is modified , otherwise false
    */
    protected boolean isModified(String strEventHistory){
        //retain First Event in strEventHistory : list of Events
        int iEventNext = strEventHistory.indexOf(super.TRANS_HISTORY_EVENT_DELIMITER);
        if(iEventNext!=-1){
            strEventHistory = strEventHistory.substring(0,iEventNext);
            int iNewLine = strEventHistory.lastIndexOf("\n");
            if(iNewLine != -1){
                strEventHistory = strEventHistory.substring(0,iNewLine);
            }
        }
        //strEventHistory contains the Event to be checked for modification
        
        // remove 'revision:' part, if present in strEventHistory
        int iRevision = strEventHistory.indexOf(TRANS_HISTORY_REVISION_DELIMITER);
        if(iRevision != -1){
            strEventHistory = strEventHistory.substring(0,iRevision);
        }
        try{
            int iWas = strEventHistory.indexOf(TRANS_HISTORY_WAS_DELIMITER);
            String strWithNewValue = strEventHistory.substring(0,iWas).trim();
            String strOldValue = strEventHistory.substring(iWas+4).trim();
            if(strOldValue.equals("") && strWithNewValue.endsWith(":")){
                //return false if both New value and Old value are blank
                return false;
            }
            if(strWithNewValue.endsWith(strOldValue)){
                int strOldValueLength = strOldValue.length();
                int strWithNewValueLength = strWithNewValue.length();
                if(strWithNewValue.substring(0,strWithNewValueLength - strOldValueLength).trim().endsWith(":")){
                    //return false if both New value and Old value are same
                    return false;
                }
            }
        }catch(StringIndexOutOfBoundsException e){
            // return true if any error occurs
        }
        return true;
    }
}
