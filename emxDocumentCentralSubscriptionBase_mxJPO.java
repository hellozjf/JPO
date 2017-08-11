/*
 **  emxDocumentCentralSubscriptionBase.java
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of MatrixOne,
 **  Inc.  Copyright notice is precautionary only
 **  and does not evidence any actual or intended publication of such program
 **
 **  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 **  Author   : Anil KJ
 **  Version  : "$Revision: 1.13 $"
 **  Date     : "$Date: Wed Oct 22 16:02:44 2008 $"
 **
 **  staic const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.13 Wed Oct 22 16:02:44 2008 przemek Experimental przemek $";
 */

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
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.Person;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.Pattern;

import com.matrixone.apps.domain.util.MapList;
import matrix.util.StringList;
/**
*
* @exclude
*/
public class emxDocumentCentralSubscriptionBase_mxJPO
{

    public emxDocumentCentralSubscriptionBase_mxJPO(Context context, String []args)
        throws Exception
    {
        //EMPTY CONSTRUCTOR
    }

     /**
      * This method is executed if a specific method is not specified.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args the Java <code>String[]</code> object
      * @return int
      * @throws Exception if the operation fails
      */
     public int mxMain (Context context, String[] args) throws Exception
     {
         if (true)
         {
            throw new Exception (
            "Must specify method on emxDocumentCentralSubscription invocation");
         }

         return 0;
     }

    /**
     * Get all Event object connected to objectId passed thry
     * Publish Subscribe object and connected to logged in User
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object holds following input arguments:
     *    0 - HashMap containing one String entry for key "objectId"
     *
     * @return the eMatrix<code>TreeMap</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.1.0
     */
    public TreeMap getAllObjectEvents(matrix.db.Context context,
                                    String []args  ) throws Exception
    {
        /*
         *  Author     : Neaz Faiyaz
         *  Date       : 02/16/02
         *  Notes      :
         *  History    :
         *
         */

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
        objSelect.addElement("attribute[" + sAttType + "]");

        //Build select params for relationship

        StringList relSelect = new StringList();

        String objWhere = "";

        DomainObject domainObj = new DomainObject();
        domainObj.setId(objectId);

        //Get all the event objects connected to objectId passed

        MapList result = domainObj.getRelatedObjects( context,
            relPattern.getPattern(), "*", objSelect, relSelect, false, true,
            (short)3, objWhere, null, typePattern, null, null);

        //create treeMap with Event type and Object Id as name and value

        Hashtable temp;
        TreeMap treeMap = new TreeMap();

        Iterator iterator = result.iterator();

        while(iterator.hasNext())
        {
            temp = (Hashtable)iterator.next();
            treeMap.put(temp.get("attribute[" + sAttType + "]"),
                temp.get(DomainConstants.SELECT_ID));
        }

        //filter out Events which are not connected to logged in user

        TreeMap filteredMap =
            filterOutUnWantedEvents(context, treeMap);

        return filteredMap;
    }

    /**
     * This method filters out Events which are not connected to logged in
     * user
     *
     * @param context <code>Context</code> object
     * @param treeMap <code>TreeMap</code> object
     *
     * @return a Java <code>TreeMap</code>
     *
     * @throws FrameworkException if the operation fails
     *
     * @since AEF 9.5.3.1
     */
    protected TreeMap filterOutUnWantedEvents(Context context, TreeMap treeMap)
        throws FrameworkException
    {
        /*
         *  Author     : Neaz Faiyaz
         *  Date       : 02/16/02
         *  Notes      :
         *  History    :
         *
         */

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
     * @param eventId <code>String</code> object
     *
     * @return a Java <code>TreeMap</code>
     *
     * @throws FrameworkException if the operation fails
     *
     * @since AEF 9.5.3.1
     */
    protected boolean checkForConnectedPerson(Context context, String eventId)
        throws FrameworkException
    {
        /*
         *  Author     : Neaz Faiyaz
         *  Date       : 02/16/02
         *  Notes      :
         *  History    :
         *
         */

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
            relPattern.getPattern(), "*", objSelect, relSelect, false, true,
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
     * This method return true if the logged in user is present in the
     * treeMap passed
     *
     * @param context <code>Context</code> object
     * @param treeMap <code>TreeMap</code> object
     *
     * @return a Java <code>TreeMap</code>
     *
     * @since AEF 9.5.3.1
     */
    protected boolean checkIfOwnerExists(Context context, TreeMap treeMap)
    {
        /*
         *  Author     : Neaz Faiyaz
         *  Date       : 02/16/02
         *  Notes      :
         *  History    :
         *
         */

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
     * Connect event to person logged in, if already connected then does nothing
     *
     * @param context <code>Context</code> object
     * @param evtId <code>String</code> object
     *
     *
     * @throws FrameworkException if the operation fails
     * @throws MatrixException if the operation fails
     *
     * @since AEF 9.5.3.1
     */
    protected void connectEventPerson(Context context,
            String evtId) throws FrameworkException, MatrixException
    {
        /*
         *  Author     : Neaz Faiyaz
         *  Date       : 02/16/02
         *  Notes      :
         *  History    :
         *
         */


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
     * Create event object, set the event type attribute and connect
     * it to logged in person and Publish Subscribe object
     *
     * @param context <code>Context</code> object
     * @param psId <code>String</code> object
     * @param attributeValue <code>String</code> object
     *
     * @throws FrameworkException if the operation fails
     * @throws MatrixException if the operation fails
     *
     * @since AEF 9.5.3.1
     */
    protected void createEventObject(Context context,
            String psId, String attributeValue) throws FrameworkException,
            MatrixException
    {
        /*
         *  Author     : Neaz Faiyaz
         *  Date       : 02/16/02
         *  Notes      :
         *  History    :
         *
         */

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
     * Return the object id of ps connected to subscribed object,
     * if dosent exists, create it
     *
     * @param context <code>Context</code> object
     * @param parentId <code>String</code> object
     * @param createNew <code>boolean</code> object
     *
     * @return a Java <code>String</code>
     *
     * @throws FrameworkException if the operation fails
     * @throws MatrixException if the operation fails
     *
     * @since AEF 9.5.3.1
     */
    public static String getPublicSubscribeObject(Context context,
                                    String parentId, boolean createNew)
                                    throws FrameworkException, MatrixException
    {
        /*
         *  Author     : Neaz Faiyaz
         *  Date       : 02/16/02
         *  Notes      :
         *  History    :
         *
         */

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
            relPattern.getPattern(), "*", objSelect, relSelect, false, true,
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
     * Process Subscribed Events
     *
     * @param context <code>Context</code> object
     * @param args the Java <code>String[]</code> object contains a Map with the following entries:
     *    parentId         - parentId for Subscription
     *    vectorSubEvents  - the Java <code>Vector</code> containing al Sub events
     * @throws FrameworkException if the operation fails
     *
     * @since AEF 9.5.3.1
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

        //get ps id of existing or new publish subscribe object

        String psId = getPublicSubscribeObject(context, parentId, true);

        //create relationship patern for search

        Pattern relPattern = new Pattern(sRelPublish);

        //create type patern for search

        Pattern typePattern = new Pattern(sTypeEvent);

        //Build select params for object

        StringList objSelect = new StringList();
        objSelect.addElement( DomainConstants.SELECT_ID ) ;
        objSelect.addElement( "attribute[" + sAttrEventType + "]" ) ;

        //Build select params for relationship

        StringList relSelect = new StringList();

        DomainObject domainObj = new DomainObject();
        domainObj.setId(psId);

        //Get all Events connected to PS

        MapList maplist = domainObj.getRelatedObjects(context,
            relPattern.getPattern(), "*", objSelect, relSelect,
            false, true, (short)1, null, null) ;

        Hashtable temp;
        Hashtable treeMap = new Hashtable();

        Iterator iterator = maplist.iterator();

        while(iterator.hasNext())
        {
            temp = (Hashtable)iterator.next();
            treeMap.put(temp.get("attribute[" + sAttrEventType + "]"),
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
     * Process UnSubscribed Events
     *
     * @param context <code>Context</code> object
     * @param args the Java <code>String[]</code> object contains a Map with the following entries:
     *    unSubEvents - token separated string of event ids to unsubsribe
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.3.1
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
     * Get Subscription Details
     *
     * @param context <code>Context</code> object
     * @param args the Java <code>String[]</code> object, holds no arguments
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.3.1
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

        StringList eventObjectSelect = new StringList();
        eventObjectSelect.add(DomainConstants.SELECT_ID);
        eventObjectSelect.add("attribute[" + sAttType + "]");

        MapList eventMapList = (MapList)currentUser.getRelatedObjects(context,
        sRelSubscribedPerson,"*",eventObjectSelect,new  StringList(),true,false,
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
            String eventType=(String)tmpMap.get("attribute[" + sAttType + "]");

            MapList dcObjectsMapList=myobj.getRelatedObjects(context,
                         sRelPublish+","+sRelPublishSubscribe,"*", dcObjectSelects,
                         new  StringList(), true, false, (short)2, null,
                         null, null,psRelPattern,null);

            Iterator dcObjectMapItr = dcObjectsMapList.iterator();
            Hashtable dctmpMap;

            while(dcObjectMapItr.hasNext())
            {
                dctmpMap=(Hashtable)dcObjectMapItr.next();
                dctmpMap.put("attribute[" + sAttType + "]",eventType);
                dctmpMap.put("eventId",eventId);
                finalMapList.add(dctmpMap);
            }
        }

        return finalMapList;

    }
}
