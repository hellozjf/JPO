/*
 ** emxTransactionNotificationUtilBase
 **
 ** Copyright (c) 1999-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIComponent;



/**
 * The <code>emxIssueBase</code> class contains methods related to Issue Management.
 * @version  - V6R2009x Copyright (c) 2008, MatrixOne, Inc.
 */
public class emxTransactionNotificationUtilBase_mxJPO extends emxNotificationUtil_mxJPO
{


    HashMap subscriberCache = new HashMap();
    HashMap historyCache = new HashMap();
    private static String INPUT_REPLYTO_DELIMITER = "|";
    static String TRANS_HISTORY_OBJECT_DELIMITER = "id=";
    static String TRANS_HISTORY_HISTORY_START_DELIMITER = "history=";
    static String TRANS_HISTORY_EVENT_DELIMITER = "- user:";
    static String TRANS_HISTORY_TIME_DELIMITER = "time:";
    static String TRANS_HISTORY_TIME_DELIMITER1 = "AM";
    static String TRANS_HISTORY_TIME_DELIMITER2 = "PM";
    static String DEFAULT_DATE_FORMAT = "moy/dom/yr4 h12:min:sec mer";
    /** object and relationship constants.*/
    private static String OBJECT = "object";
    private static String RELATIONSHIP = "relationship";
    static String TRANS_HISTORY_TYPE_DELIMITER = "type=";
    static String TRANS_HISTORY_TRIGGER_DELIMITER = "triggerName=";
    /**
     * Create a new emxIssueBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @return a emxIssueBase object.
     * @throws Exception if the operation fails.
     * @since V6R2009x
     */
    public emxTransactionNotificationUtilBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since V6R2009x
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (true) {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.TransactionNotification.SpecifyMethodOnNotificationInvocation", context.getLocale().getLanguage()));
        }
        return 0;
    }
        /**
     * Method to send the notifications to user based on history actions parsed
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the macro TRANSHISTORY from transaction trigger
     * @return integer denoting success or failure of notification sent .
     * @throws Exception if the operation fails
     * @since R207
     **/
    public int transactionNotifications(Context context, String[] args) throws Exception
    {
        int result = 0;
        String transHistories = args[0];

        if(transHistories != null && !"".equals(transHistories))
        {
            try
            {
                Context frameContext = context.getFrameContext("emxTransactionNotificationUtil");
                BackgroundProcess backgroundProcess = new BackgroundProcess();
                backgroundProcess.submitJob(frameContext, "emxTransactionNotificationUtil", "notifyInBackground", args , (String)null);
            } catch(Exception ex)
            {
                ContextUtil.abortTransaction(context);
                ex.printStackTrace();
                throw ex;
            }
        }
        return result;
    }

    /**
     * Method to send the notifications to user based on history actions parsed
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the macro TRANSHISTORY from transaction trigger
     * @return integer denoting success or failure of notification sent .
     * @throws Exception if the operation fails
     * @since R207
     **/

     public int notifyInBackground(Context context, String[] args) throws Exception
     {
        int result = 0;
        String transHistories = args[0];

        if(transHistories != null && !"".equals(transHistories))
        {
            try
            {
                ContextUtil.startTransaction(context, true);
                HashMap transHistoryMap = parseHistory(context, transHistories);
                result = sendNotifications(context, transHistoryMap);
                ContextUtil.commitTransaction(context);
            } catch(Exception ex)
            {
                ContextUtil.abortTransaction(context);
                ex.printStackTrace();
                throw ex;
            }
        }
        return result;
     }
    /**
     * Method to parse the TRANSHISTORY string to return actions separately
     * @param transHistories is a String containing the macro TRANSHISTORY from transaction trigger
     * @return HashMap containing object ids as key and history list as value
     * @throws Exception if the operation fails
     * @since V6R2009x
     **/

   public HashMap parseHistory(Context context,String transHistories) throws Exception
    {
        try
        {
            HashMap transHistoryMap = new HashMap();
            String oid = null;
            StringList events = new StringList();
            String strtype = "";
            int idIndex = 0;
            do
            {
        //splitting the transhistory
                idIndex = transHistories.indexOf(TRANS_HISTORY_OBJECT_DELIMITER);
                if(idIndex != -1)
                {
                    if(oid != null)
                    {
                        transHistoryMap.put(oid, FrameworkUtil.join(events, "|"));
                    }
                    events = new StringList();
                    int itypeIndex = transHistories.indexOf(TRANS_HISTORY_TYPE_DELIMITER);
                    if(itypeIndex != -1)
                    {
                        oid = transHistories.substring(idIndex+3,itypeIndex).trim();
                        transHistories = transHistories.substring(itypeIndex+5);
                    }
                    int iTriggerIndex = transHistories.indexOf(TRANS_HISTORY_TRIGGER_DELIMITER);
                    int ihistoryIndex = transHistories.indexOf(TRANS_HISTORY_HISTORY_START_DELIMITER);
                    strtype = transHistories.substring(0,iTriggerIndex).trim();
                    if(oid != null)
                        oid = oid+"_"+strtype;
          //Since we had splitted the oid and type, modify the
          //transHistories to contains the rest of the content from 'history=' keyword
                    transHistories = transHistories.substring(ihistoryIndex+8);
          //While finding the id= keyword from second time onwards searching with \nkeyword
                    idIndex = transHistories.indexOf("\n"+TRANS_HISTORY_OBJECT_DELIMITER);
                    String strEventsHistory = "";
          /*
          * if the transhistory contains more id then get the list of history events for the first object
          * and store it in strEventsHistory variable and the rest of the content in transHistories.
          * if no more id is found then assign the transhistories to strEventsHistory variable directly
          */
                    if(idIndex != -1)
                    {
                        strEventsHistory = transHistories.substring(0,idIndex);
                        transHistories = transHistories.substring(idIndex);
                    }
                    else
                        strEventsHistory = transHistories;
                    //Invoke the method to parse history Events of every object using the '- user:'
                    events = parseEvents(context,strEventsHistory);
                }
            }while(idIndex != -1);
            transHistoryMap.put(oid, FrameworkUtil.join(events, "|"));
            return transHistoryMap;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }


    /**
     * Method to parse the History Events string to return stringlist of events with | separated
     * @param strEventsHistory is a String containing the list of events occured on a particular object
     * @return StringList containing list of event names,time stamp with | separated
     * @throws Exception if the operation fails
     * @since V6R2009_HF0
     **/

    public StringList parseEvents(Context context,String strEventsHistory) throws Exception
    {
        StringList events = new StringList();
        String strTemp = "";
        int iStartEvent = 0;
        String strUser = "";
        String strTime = "";
        String strEvent = "";

        boolean isAMPM = false;
        int noOfColuns = 2;
        String dateFormat = matrix.db.Environment.getValue(context,"MX_NORMAL_DATETIME_FORMAT");
        if( dateFormat == null || "".equals(dateFormat) || "null".equals(dateFormat) )
        {
            dateFormat = DEFAULT_DATE_FORMAT;
        }
        if( dateFormat.indexOf("mer") >= 0 )
        {
            isAMPM = true;
        }

        // find out how many coluns are present in the date foramt
        int colunIndex = dateFormat.indexOf(":");
        if( colunIndex == -1 )
        {
            noOfColuns = 0;
        } else if( colunIndex == dateFormat.lastIndexOf(":") )
        {
            noOfColuns = 1;
        }

        int iEventIndex = strEventsHistory.indexOf(TRANS_HISTORY_EVENT_DELIMITER);
        while(iEventIndex != -1)
        {
            strTemp = strEventsHistory.substring(0,iEventIndex).trim();
      /*
      * We need to take last index of \n since while spliting with '- user:' word we will get the value
      * something like the below one if the description contains more than one lines.
      * Eg:
      *   Test Everything  time: 07/01/2008 04:36:51 PM  state: Preliminary  description: asdfwe
      *   asdfwasdfaw
      *   asdfsadfwesad
      *   sdafawwefasdf
      *   modify - user: Test Everything  time: 7/1/2008 4:36:51 PM  state: Preliminary  Design Purchase: Design  was:
      * Here we have taken care of taking the event name from the last index of \n.
      */
            iStartEvent = strTemp.lastIndexOf("\n");
            strEvent = strEventsHistory.substring(iStartEvent+1,iEventIndex).trim();
            strEventsHistory = strEventsHistory.substring(iEventIndex+7);
            int itimeIndex = strEventsHistory.indexOf("time:");
            if(itimeIndex != -1)
            {

                //User value being set as a Global RPE variable
                if(strUser.equals(""))
                {
                    strUser = strEventsHistory.substring(0,itimeIndex).trim();
                    PropertyUtil.setGlobalRPEValue(context,"USER","\""+strUser+"\"");
                }

                // Finding the string between the time: and the next token:
                colunIndex = itimeIndex + 4;
                int tempIndex = colunIndex;
                strTime = strEventsHistory;
                for (int i = 0; i <= noOfColuns; i++ )
                {
                    strTime = strTime.substring(tempIndex+1,strTime.length());
                    tempIndex = strTime.indexOf(":");
                    colunIndex += (tempIndex+1);
                    if( i == noOfColuns )
                    {
                        // This string should return
                        // Eg : 7/1/2008 4:36:51 PM  state
                        strTime = strEventsHistory.substring(itimeIndex+5,colunIndex);
                    }
                }

                int iTimeEndIndex = -1;
                if ( isAMPM )
                {
                    iTimeEndIndex = strTime.indexOf(TRANS_HISTORY_TIME_DELIMITER1)==-1? strTime.indexOf(TRANS_HISTORY_TIME_DELIMITER2) : strTime.indexOf(TRANS_HISTORY_TIME_DELIMITER1);
                }
                //Validating if AM / PM exists along with time stamp. if not then take the time from the last indexof :
                if(iTimeEndIndex == -1)
                    iTimeEndIndex = strTime.lastIndexOf(":");
                else
                    iTimeEndIndex = iTimeEndIndex -1 ;

                String strTimeStamp = strTime.substring(0,iTimeEndIndex+3).trim();
                events.add(strEvent + "_" + strTimeStamp);

            }
            else
            {
                events.add(strEvent);
            }

            iEventIndex = strEventsHistory.indexOf(TRANS_HISTORY_EVENT_DELIMITER);

        }
        return events;
    }

    /**
     * Method to send the notifications to the users who subscribed for events
     * @param context the eMatrix <code>Context</code> object
     * @param transHistoryMap is a HashMap containing object ids as key and events list as value
     * @return integer denoting success or failure of notification sent .
     * @throws Exception if the operation fails
     * @since V6R2009x
     **/
    public int sendNotifications(Context context, HashMap transHistoryMap) throws Exception
    {
        try
        {
            Iterator iterate = transHistoryMap.entrySet().iterator();
            while(iterate.hasNext())
            {
                Map.Entry map = (Map.Entry) iterate.next();
                String strobjectId = (String) map.getKey();
                String strHistory = (String) map.getValue();
                
                // Validate whether that objectId have some history of events
                if(strHistory != null && !"".equals(strHistory))
                {
                    // split the objectid with _ char to get the details of id either objectid or connectionid
                    StringList strlTemp = FrameworkUtil.split(strobjectId, "_");
                    strobjectId = (String) strlTemp.get(0);
                    String strTemp = (String)strlTemp.get(1);
                    
                    //If the id is connection then retrieve the interface associated with it.
                    String idType = null; 
                    String typeName = null;
                    MapList eventList = null;
                    if(strTemp.equalsIgnoreCase("connection"))
                    {
                        idType = RELATIONSHIP;
                        typeName = SubscriptionUtil.getTypeNameFromId(context, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE, strobjectId);
                        eventList = SubscriptionUtil.getSubscribableEventsList(context, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE, typeName);                               
                    }
                    else
                    {
                        idType = OBJECT;
                        typeName = SubscriptionUtil.getTypeNameFromId(context, SubscriptionUtil.ADMIN_BUSINESS_TYPE, strobjectId);
                        eventList = SubscriptionUtil.getSubscribableEventsList(context, SubscriptionUtil.ADMIN_BUSINESS_TYPE, typeName);                               
                    }
                    
                    
                    if(eventList != null)
                    {
                        // Collecting the associated events command using History Bit setting
                        /**
                         Here user can override the LoadEvents method in order to customize for which events
                         user gets notified in a series of events. For eg: suppose a particular object has undergone
                         both modify,create and connect events, in such case it is enough to send only create event
                         notification. So user can override the LoadEvent method such that it should send notification
                         only for create events.
                         
                         In sample JPO we are not overridden the Load Event method.
                         
                         **/
                        StringList strlNotificationCommandName = loadEvents(context, FrameworkUtil.split(strHistory, "|"),strobjectId);
                        // After finalizing the events then compare fore history bits and then send notifications
                        strlNotificationCommandName = compareHistoryBits(context,eventList,strlNotificationCommandName);
                        if(strlNotificationCommandName != null && strlNotificationCommandName.size()>0)
                        {
                            notifySubscribers(context,strobjectId,strlNotificationCommandName,idType);
                        }
                    }
                }
                
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        
        return 0;
    }
    
    /**
     * Method to filter the list of events subscribed by user for notification
     * User will write overridden method in the Non-Base custom JPO.
     * @param context the eMatrix <code>Context</code> object
     * @param strlNotificationCommandName contains list of eventcommands subscribed for notification.
     * @return StringList contains the list of filtered events for notification.
     * @throws Exception if the operation fails
     * @since V6R2012
    */
    public StringList loadEvents (Context context, StringList strlNotificationCommandName, String objectId ){
       return loadEvents(context,strlNotificationCommandName);
     }
    
    /**
     * Method to filter the list of events subscribed by user for notification
     * User will write overridden method in the Non-Base custom JPO.
     * @param context the eMatrix <code>Context</code> object
     * @param strlNotificationCommandName contains list of eventcommands subscribed for notification.
     * @return StringList contains the list of filtered events for notification.
     * @throws Exception if the operation fails
     * @since V6R2009x
    */
    public StringList loadEvents (Context context, StringList strlNotificationCommandName ){
       return new StringList(0);
     }


    /**
     * Method to get the list of events commands associated with that object type
     * @param context the eMatrix <code>Context</code> object
     * @param typeName contains the type name of the object passed.
     * @throws Exception if the operation fails
     * @since X+3
    */

     public MapList getEventCommands(Context context,String typeName, String idType)throws Exception
    {    
         return SubscriptionUtil.getSubscribableEventsList
                                     (context, 
                                      idType.equalsIgnoreCase(RELATIONSHIP) ? SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE : SubscriptionUtil.ADMIN_BUSINESS_TYPE, 
                                      typeName);         
    }






    /**
     * Method to form the list of eventcommands for each object comparing history bit setting on commands
     * @param context the eMatrix <code>Context</code> object
     * @param objectEventsMapList contains list of eventcommands obtained from admin menu for each type.
     * @param strHistory contains the parsed history string
     * @return StringList containing list of commands that matches the History Bit setting.
     * @throws Exception if the operation fails
     * @since V6R2009x
    */


    public StringList compareHistoryBits(Context context, MapList objectEventsMapList, StringList historyEvents)throws Exception
    {

        StringList strlCommandNames = new StringList();
        String strEventHistory = "";
        String strTime = "";
        String strCmdName   = "";
        TreeMap strlHistoryEvents = new TreeMap();

        for(int iHistory=0; iHistory < historyEvents.size(); iHistory++)
        {
            strEventHistory = (String) historyEvents.get(iHistory);
            int timeIndex = strEventHistory.indexOf("_");
            strTime = null;
            if( timeIndex > 0)
            {
                strTime = strEventHistory.substring( timeIndex+1, strEventHistory.length());
                strEventHistory = strEventHistory.substring(0, timeIndex);
            }
            strlHistoryEvents.put(strEventHistory,strTime);

        }
        java.util.Set set = strlHistoryEvents.entrySet();
        Iterator itr = set.iterator();
        while(itr.hasNext())
        {
            Map.Entry map = (Map.Entry) itr.next();
            strEventHistory = (String) map.getKey();
            strTime = (String) map.getValue();
                //Retrieving names of commands and checking for History Bit setting
                for(int j=0;j<objectEventsMapList.size();j++)
                {
                    Map cmdMap = (Map)objectEventsMapList.get(j);
                    strCmdName = (String)cmdMap.get("name");
                    String strHistoryBit = UIComponent.getSetting(cmdMap,"History Bit");
                    strEventHistory = strEventHistory.trim();
                    if(strEventHistory.equals(strHistoryBit))
                    {
            // Modified the splitting character for the bug 356408
                        strlCommandNames.add(strCmdName + "~_" + strTime + "~_" + strEventHistory);
                        break;
                    }
                }

        }
        return strlCommandNames;
    }


    /**
     * Method to find the notification object matching the command name.
     * Add the subscribers list for a particular event in cache.
     * @param context the eMatrix <code>Context</code> object
     * @param strobjectId contains the id of the object passed.
     * @param strlCommandNames contains list of event commandname as values.
     * @param idType contains the type of the id passed is either relationship or object.
     * @throws Exception if the operation fails
     * @since V6R2009x
    */

    public void notifySubscribers(Context context, String strobjectId, StringList strlCommandNames,String idType)throws Exception
    {
        String strEvent = "";
        boolean isContextPushed = false;
        for(int q=0;q<strlCommandNames.size();q++)
        {
            String strCommandName = (String) strlCommandNames.get(q);

            //Retrieving the Event name and timestamp from the key in HashMap returned from the parseHistory method
            //Obtaining index of "~_" in the key
      //Modified the splitting character for the bug 356408
            int index_ = strCommandName.indexOf("~_");

            if( index_ > 0 )
            {

                String strTime = strCommandName.substring(index_+2, strCommandName.length());
                strCommandName = strCommandName.substring(0, index_);
                index_ = strTime.indexOf("~_");

                if( index_ > 0)
                {
                   //Retrieving the event name from the key
                    strEvent = strTime.substring(index_+2, strTime.length());


                    strTime = strTime.substring(0, index_);
                   //Setting the event name as a Global RPE variable for each object
                    PropertyUtil.setGlobalRPEValue(context,"EVENT","\""+strEvent+"\"");
                 //Setting the Timestamp of each event occurring on each object as a Global RPE variable
                  PropertyUtil.setGlobalRPEValue(context,"TIMESTAMP","\""+strTime+"\"");
                }

            }
            // Added to change the context to super user if the event is Change Owner event
            try
            {
                if(strEvent.equals("change owner"))
                {
                    ContextUtil.pushContext(context);
                    isContextPushed = true;
                    objectRelationshipNotification(context,strobjectId,idType,strCommandName, null);
                }
                else
                {
                    objectRelationshipNotification(context,strobjectId,idType,strCommandName, null);
                }
            }
            catch(Exception ex)
            {
                throw ex;
            }
            finally
            {
                if(isContextPushed)
                    ContextUtil.popContext(context);

            }

        }// End For
    }



}// End Class
