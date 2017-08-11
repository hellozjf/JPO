/*
 ** emxPLCTransactionNotificationUtilBase.java
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of MatrixOne, Inc.
 ** Copyright notice is precautionary only and does not evidence any actual or intended
 ** publication of such program.
 **
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.common.Issue;
import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIComponent;

/**
 * The <code>emxPLCTransactionNotificationUtilBase</code> class contains common transaction notification utility methods for PLC
 * @version Variant Configuration R207 - Copyright (c) 2008-2016 Dassault Systemes.
 * @since PLC R207
 */

public class emxPLCTransactionNotificationUtilBase_mxJPO extends emxTransactionNotificationUtil_mxJPO
{
   //private String _event;
   private static String OBJECT = "object";
   private static String RELATIONSHIP = "relationship";

   /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    */
   public emxPLCTransactionNotificationUtilBase_mxJPO (Context context, String[] args) throws Exception
   {
      super(context, args);

      //if (args == null || args.length == 0)
      //{
      //   System.out.println("Invalid arguments for emxPLCTransactionNotificationUtilBase");
      //   throw new IllegalArgumentException();
      //}
      //_event = args[0];
      //System.out.println("emxPLCTransactionNotificationUtilBase._event: " + _event);
      //if (_event == null || _event.trim().length() == 0) throw new IllegalArgumentException();
   }

   /**
    * Method to filter the list of events subscribed by user for notification
    * @param context the eMatrix <code>Context</code> object
    * @param strlNotificationCommandName contains list of eventcommands subscribed for notification.
    * @return StringList contains the list of filtered events for notification.
    * @throws Exception if the operation fails
    * @since R207
   */
   public StringList loadEvents (Context context, StringList strlNotificationCommandName )
   {
      return strlNotificationCommandName;
   }

   public int transactionNotifications(Context context, String[] args) throws Exception
   {
       int result = 0;
       String transHistories = args[0];

       if(transHistories != null && !"".equals(transHistories))
       {
           try
           {
               ContextUtil.pushContext(context);
               Context frameContext = context.getFrameContext("emxPLCTransactionNotificationUtilBase");
               BackgroundProcess backgroundProcess = new BackgroundProcess();
               backgroundProcess.submitJob(frameContext, "emxPLCTransactionNotificationUtilBase", "notifyInBackground", args , (String)null);
           } catch(Exception ex)
           {
               ContextUtil.abortTransaction(context);               
               ex.printStackTrace();
               throw ex;
           }finally{
               //Set the context back to the context user
               ContextUtil.popContext(context);
          }
       }
       return result;
   }

   /**
    * Overridden Method to form the list of eventcommands for each object comparing history bit setting on commands
    * - This Method is overridden because the history event string consists of additional information and is not same as history bit
    * - from the command( Sample History string - connect From GBOM type to GBOM type, History bit - connect)
    * - This method also includes code to identify the type of event occured using the
    * - second argument i.e., args[1] passed to the method.
    * @param context the eMatrix <code>Context</code> object
    * @param objectEventsMapList contains list of eventcommands obtained from admin menu for each type.
    * @param args
    * @param strHistory contains the parsed history string
    * @return StringList containing list of commands that matches the History Bit setting.
    * @throws Exception if the operation fails
    * @since R207
   */


   public StringList compareHistoryBits(Context context, MapList objectEventsMapList, StringList historyEvents, String[] args) 
   throws Exception
   {
       // ** Customization: Reading the type of event from the args.
       String strCommandNameParam = args[1];

       StringList strlCommandNames = new StringList();
       String strEventHistory = "";
       String strTime = "";
       String strCmdName   = "";
       TreeMap strlHistoryEvents = new TreeMap();

       for(int iHistory=0; iHistory < historyEvents.size(); iHistory++) {
           strEventHistory = (String) historyEvents.get(iHistory);
		   // Modified by KXB for Mx378256 STARTS
           int timeIndex = strEventHistory.lastIndexOf("_");
		   // Modified by KXB for Mx378256 ENDS
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
       
       while(itr.hasNext()) {
           Map.Entry map = (Map.Entry) itr.next();
           strEventHistory = (String) map.getKey();
           strTime = (String) map.getValue();

           //Retrieving names of commands and checking for History Bit setting
           for(int j=0;j<objectEventsMapList.size();j++) {
               Map cmdMap = (Map)objectEventsMapList.get(j);
               strCmdName = (String)cmdMap.get("name");
               //To identify the event name
               if(strCmdName.equals(strCommandNameParam)) {
                   String strHistoryBit = UIComponent.getSetting(cmdMap,"History Bit");
                   strEventHistory = strEventHistory.trim();
                   if(!"".equals(strHistoryBit) && strEventHistory.startsWith(strHistoryBit)) {
                       strlCommandNames.add(strCmdName + "~_" + strTime + "~_" + strEventHistory);
                       break;
                   }
               }
           }
       }

       return strlCommandNames;
   }

   /**
    * Notify in BG
    * Overridden method from emxTransactionNotificationUtil JPO.
    * - Only change is to pass an additional parameter args[] to sendNotification method.
    * @param context - eMatrix context object
    * @param args
    * @return int .
    * @throws Exception if the operation fails
    * @since R207
    */
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
              result = sendNotifications(context, transHistoryMap, args);
              ContextUtil.commitTransaction(context);
          } catch(Exception ex) {
              ContextUtil.abortTransaction(context);
              ex.printStackTrace();
              throw ex;
          }
      }
      return result;
   }


   /**
    * An overridden method from emxTransactionNotificationUtil JPO
    * - Heavily customized to deal with transaction events being changed.
    * @param context - eMatrix context object.
    * @param transHistoryMap
    * @param args
    * @return int
    * @throws Exception
    * @since R207
    */
   public int sendNotifications(Context context, HashMap transHistoryMap, String[] args) throws Exception
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
                   
                   String idType = null; 
                   String typeName = null;
                   MapList eventList = null;
                   
                   if(strTemp.equalsIgnoreCase("connection")) {
                	   
                	   // We actually want to report on the "From" object of the Connection Id we are given.  Get that
                	   // Object Id, and reset everything to make it look like we are dealing with a Business Object.
                	   // Note: For Engineering Change and Issue, we will find that type on the From Side, so go get the "To"
                	   // side and check those for PLC related types.
                	   
                	   StringList slSelects = new StringList(2);
                	   slSelects.add("from.id");
                	   slSelects.add("from.type");

                	   String[] saObjects = new String[] {strobjectId};
                	   MapList relationshipData = DomainRelationship.getInfo(context, saObjects, slSelects);
                	   
                	   if (relationshipData != null) {
                    	   // Reset the object Id
                		   Map mapRel = (Map)relationshipData.get(0);
                		   String tempObjectId = (String)mapRel.get("from.id");
                		   String tempObjectType = (String)mapRel.get("from.type");
                		   		
                		   strobjectId = tempObjectId;

                		   // If we have an Issue or an Engineering Change, then we need to get the object on the TO 
                		   // side of the given connection and report on that instead.
                		   
                		   String typeIssue = PropertyUtil.getSchemaProperty(context, "type_Issue");
                		   String typeEC = PropertyUtil.getSchemaProperty(context, "type_EngineeringChange");
                		   String typeChangeAction = PropertyUtil.getSchemaProperty(context, "type_ChangeAction");
                		   String fullHistory = args[0];

                		   if (tempObjectType != null && fullHistory != null && (tempObjectType.equals(typeIssue) || tempObjectType.equals(typeEC) || tempObjectType.equals(typeChangeAction))) {
                			   StringList slSelectsTo = new StringList(1);
                        	   slSelectsTo.add("to.id");
                        	   relationshipData = DomainRelationship.getInfo(context, saObjects, slSelectsTo);
                        	   if (relationshipData != null) {
                        		   mapRel = (Map)relationshipData.get(0);
                        		   strobjectId = (String)mapRel.get("to.id");
                        	   }
                		   }

                	   } else {
                		   // Problem with the domain get, keep this as a relationship.
                		   idType = RELATIONSHIP;
                           typeName = SubscriptionUtil.getTypeNameFromId(context, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE, strobjectId);
                           eventList = SubscriptionUtil.getSubscribableEventsList(context, SubscriptionUtil.ADMIN_RELATIONSHIP_TYPE, typeName);                               
                	   }
                   }
                   
                   // Only set this if it hasn't been already.
                   if (idType == null) {
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
                	      
                	   StringList strlNotificationCommandName = loadEvents(context, FrameworkUtil.split(strHistory, "|"));
                	   // After finalizing the events then compare fore history bits and then send notifications
                	   strlNotificationCommandName = compareHistoryBits(context, eventList, strlNotificationCommandName, args);

                	   if (strlNotificationCommandName != null && strlNotificationCommandName.size()>0)
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
      

}
