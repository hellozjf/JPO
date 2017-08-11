/*
**   emxCommonCompleteTaskBase.java
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.lang.*;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIMenu;


/**
 * The <code>emxCommonCompleteTaskBase</code> class contains
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonCompleteTaskBase_mxJPO
{
 private static final String  sRelRouteTask                  = PropertyUtil.getSchemaProperty("relationship_RouteTask");
 private static final String  sRelObjectRoute                = PropertyUtil.getSchemaProperty("relationship_ObjectRoute");
 private static final String  sRelRouteNode                  = PropertyUtil.getSchemaProperty("relationship_RouteNode");
 private static final String  sAttActualCompletionDate       = PropertyUtil.getSchemaProperty("attribute_ActualCompletionDate");
 private static final String  sAttComments                   = PropertyUtil.getSchemaProperty("attribute_Comments");
 private static final String  sAttApprovalStatus             = PropertyUtil.getSchemaProperty("attribute_ApprovalStatus");
 private static final String  sAttRouteNodeID                = PropertyUtil.getSchemaProperty("attribute_RouteNodeID");
 private static final String  sAttCurrentRouteNode           = PropertyUtil.getSchemaProperty("attribute_CurrentRouteNode");
 private static final String  sAttRouteStatus                = PropertyUtil.getSchemaProperty("attribute_RouteStatus");
 private static final String  sAttRouteCompletionAction      = PropertyUtil.getSchemaProperty("attribute_RouteCompletionAction");
 private static final String  sAttRouteAction                = PropertyUtil.getSchemaProperty("attribute_RouteAction");
 private static final String  sAttParallelNodeProcessionRule = PropertyUtil.getSchemaProperty("attribute_ParallelNodeProcessionRule");
 private static final String  sAttRouteBaseState             = PropertyUtil.getSchemaProperty("attribute_RouteBaseState");
 private static final String  sAttRouteBasePolicy            = PropertyUtil.getSchemaProperty("attribute_RouteBasePolicy");
 private static final String  sRouteDelegationGrantor        = PropertyUtil.getSchemaProperty("person_RouteDelegationGrantor");
 private static final String  sAttAutoStopOnRejection        = PropertyUtil.getSchemaProperty("attribute_AutoStopOnRejection");
 private static final String  sAttFirstName        = PropertyUtil.getSchemaProperty("attribute_FirstName");
 private static final String  sAttLastName        = PropertyUtil.getSchemaProperty("attribute_LastName");
 protected static emxMailUtil_mxJPO mailUtil = null;
 protected static emxSubscriptionManager_mxJPO SubscriptionManager = null;
 protected static emxCommonInitiateRoute_mxJPO InitiateRoute = null;
 protected static emxcommonPushPopShadowAgentBase_mxJPO ShadowAgent = null;

   /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     * @grade 0
     */
    public emxCommonCompleteTaskBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonCompleteTask.SpecifyMethodOnServiceCommonCompleteTaskInvocation", context.getLocale().getLanguage()));
        }
        return 0;
    }

    /**
     * emxServicecommonCompleteTask method to remove the proicess from the tcl
     *
     * The method is supposed to be invoked on completion of any task of route.
     * The high level operations performed by this method are:-
     * If the task is rejected, and the attribute Auto Stop On Rejection is Immediate, then the route will be stops.
     * If the task is rejected, and the attribute Auto Stop On Rejection is Deferred, then the route will be stopped on completion of all the task on this level.
     * If the task is completed, and there are no more tasks on the level, then decides if the route is to be stopped or finished. It is stopped if 
     * one of the tasks on this level is rejected and Auto Stop On Rejection was set to Deferred.
     * If all the tasks on this level is completed, none of the task is rejected, and there are more tasks in the route, then next level tasks are activated.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @returns booelan
     * @throws Exception if the operation fails
     * @since AEF 10 minor 1
     */
    public static int completeTask (Context context, String args[]) throws Exception
    {
        try {
           ShadowAgent = new emxcommonPushPopShadowAgentBase_mxJPO(context,null);
           ShadowAgent.pushContext(context,null);

           String  sStateAssigned                  = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_INBOX_TASK ,"state_Assigned");
           String  sStateReview                   = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_INBOX_TASK ,"state_Review");
           String  sStateComplete                  = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_ROUTE ,"state_Complete");
           String message= "";

           String sObjectsNotSatisfied  = "";
           String sRoutesInProcess  = "";
           String sPromotedObjects ="" ;
           StringBuffer sBufObjectsNotSatisfied  = new StringBuffer();
           StringBuffer sBufRoutesInProcess  = new StringBuffer();
           StringBuffer sBufPromotedObjects =new StringBuffer();
           StringBuffer sBufInCompleteRoutes = new StringBuffer();
           String sInCompleteRoutes ="";
           // Get absolute names from symbolic names
           String  sDate =  MqlUtil.mqlCommand(context,"get env $1", "TIMESTAMP");

           //Initializing the jpos to be used
           mailUtil = new emxMailUtil_mxJPO(context, null);

           InitiateRoute = new emxCommonInitiateRoute_mxJPO(context, null);

           // Getting the type name rev from teh argument's passed
           String sType = args[0];
           String sName = args[1];
           String sRev  = args[2];
         //the below line is commented for the bug 319223
           // String bConsiderAdhocRoutes ="FALSE";
           // Get setting from emxSystem.properties file to
           // check if Ad Hoc routes should be considered or not

         // Bug 293332
           String arguments[] = new String[4];
          /* arguments[0]= "emxFramework.AdHocRoutesBlockLifecycle";
           arguments[1]=  "0";
           arguments[2] = "";
           arguments[3]= "emxSystem";
           String bConsiderAdhocRoutes =mailUtil.getMessage(context,arguments);
           // set default to false if property doesn't exists
           bConsiderAdhocRoutes = bConsiderAdhocRoutes.toUpperCase();
           if (!bConsiderAdhocRoutes.equals("TRUE")) {
               bConsiderAdhocRoutes = "FALSE";
           }*/

           // Set Actual Completion Date attribute in Inbox Task
            BusinessObject bObject = new BusinessObject(sType,sName,sRev,"");
            bObject.open(context);
            String ObjectId = bObject.getObjectId();
            bObject.close(context);
            
            DomainObject inboxTask = new DomainObject(ObjectId);
            ShadowAgent.popContext(context,null);
            inboxTask.setAttributeValue(context, sAttActualCompletionDate, sDate);
            ShadowAgent.pushContext(context,null);

           // Copy 'Approval Status', 'Actual Completion Date', 'Comments' to Route Node relationship
           SelectList objectSelects = new SelectList();
           objectSelects.addElement("attribute["+sAttRouteNodeID+"]");
           objectSelects.addElement("attribute["+sAttApprovalStatus+"]");
           objectSelects.addElement("attribute["+sAttActualCompletionDate+"]");
           objectSelects.addElement("attribute["+sAttComments+"]");
           objectSelects.addElement("attribute["+sAttRouteAction+"]");

           Map objectMap = inboxTask.getInfo(context, objectSelects);
           
           String sRouteNodeIDOnIB      = (String)objectMap.get("attribute["+sAttRouteNodeID+"]");
           String sApprovalStatus       = (String)objectMap.get("attribute["+sAttApprovalStatus+"]");
           String sActualCompletionDate = (String)objectMap.get("attribute["+sAttActualCompletionDate+"]");
           String sComments             = (String)objectMap.get("attribute["+sAttComments+"]");
           String sRouteActionOfTask    = (String)objectMap.get("attribute["+sAttRouteAction+"]");
           
           StringList lRouteNodeId      =  inboxTask.getInfoList(context, "from["+sRelRouteTask+"].businessobject.from["+sRelRouteNode+"].id");
           StringList lRouteNodeIdAttr  =  inboxTask.getInfoList(context, "from["+sRelRouteTask+"].businessobject.from["+sRelRouteNode+"].attribute["+sAttRouteNodeID+"]");

           String sRouteNodeId = "";
           String sRouteNodeIdAttr ="";

           // Get matching relationship id
            int bRouteNodeIdFound = 0;
           // need to update this for loop

         for(int i=0; i< lRouteNodeId.size(); i++) {

             sRouteNodeId = (String)lRouteNodeId.elementAt(i);

             sRouteNodeIdAttr = (String) lRouteNodeIdAttr.elementAt(i);
             if (sRouteNodeIDOnIB.equals(sRouteNodeIdAttr))
             {
              bRouteNodeIdFound = 1;
              break;
             }
           }

           // If Route Node Id not found then
           // Error out
           if (bRouteNodeIdFound == 0)
           {
             String arguments1[] = new String[13];
             arguments1[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidRouteNodeId";
             arguments1[1]  = "3";
             arguments1[2]  = "type";
             arguments1[3]  = sType;
             arguments1[4]  = "name";
             arguments1[5]  = sName;
             arguments1[6]  = "rev";
             arguments1[7]  = sRev;
             arguments1[8]  = "";
             message=mailUtil.getMessage(context,arguments1);
             MqlUtil.mqlCommand(context, "notice $1",message);
             return 1;
           }
            Map map = new HashMap();
            map.put(sAttApprovalStatus,sApprovalStatus);
            map.put(sAttActualCompletionDate,sActualCompletionDate);
            map.put(sAttComments,sComments);
            DomainRelationship.setAttributeValues(context,sRouteNodeId,map);
           String relationshipIds[] = new String[1];
           relationshipIds[0]= sRouteNodeId;
           SelectList RelSelects = new SelectList();
           RelSelects.addElement("from.id");
           RelSelects.addElement("from.owner");
           RelSelects.addElement("from.type");
           RelSelects.addElement("from.name");
           RelSelects.addElement("from.revision");
           RelSelects.addElement("from.attribute["+sAttCurrentRouteNode+"]");
           RelSelects.addElement("from.attribute["+sAttRouteStatus+"]");
           RelSelects.addElement("from.attribute["+sAttRouteCompletionAction+"]");
           RelSelects.addElement("to.name");
           RelSelects.addElement("to.attribute["+sAttFirstName+"]");
           RelSelects.addElement("to.attribute["+sAttLastName+"]");
           RelSelects.addElement("attribute["+sAttParallelNodeProcessionRule+"]");
           RelSelects.addElement("from.to["+sRelObjectRoute+"].from.id");
           RelSelects.addElement("from.to["+sRelObjectRoute+"].from.current.satisfied");
           RelSelects.addElement("from.to["+sRelObjectRoute+"].from.type");
           RelSelects.addElement("from.to["+sRelObjectRoute+"].from.name");
           RelSelects.addElement("from.to["+sRelObjectRoute+"].from.revision");
           RelSelects.addElement("from.to["+sRelObjectRoute+"].from.current");
           RelSelects.addElement("from.to["+sRelObjectRoute+"].from.policy");

           MapList relMapList = DomainRelationship.getInfo(context, relationshipIds, RelSelects);
           // Get information on attached route

           Map relMap = (Map)relMapList.get(0);
           String sRouteId          =  (String)relMap.get("from.id");
           String sOwner            =  (String)relMap.get("from.owner");
           String sRouteType        =  (String)relMap.get("from.type");
           String sRouteName        =  (String)relMap.get("from.name");
           String sRouteRev         =  (String)relMap.get("from.revision");
           String sRouteStatus      =  (String)relMap.get("from.attribute["+sAttRouteStatus+"]");
           String sRouteCompletionAction      =  (String)relMap.get("from.attribute["+sAttRouteCompletionAction+"]");
           String sPerson           =  (String)relMap.get("to.name");
           //lvc
           String sFirstName = (String)relMap.get("to.attribute["+sAttFirstName+"]");
           String sLastName = (String)relMap.get("to.attribute["+sAttLastName+"]");
           String sProcessionRule   =  (String)relMap.get("attribute["+sAttParallelNodeProcessionRule+"]");
           int sCurrentRouteNode    = Integer.parseInt((String)relMap.get("from.attribute["+sAttCurrentRouteNode+"]"));
           final String SELECT_TASK_ASSIGNEE_ID = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.id";

           DomainObject Route = new DomainObject(sRouteId);

           //modified for the 327641   12/28/2006-- Begin
           objectSelects = new SelectList();
           objectSelects.addElement(DomainConstants.SELECT_ID);
           objectSelects.addElement("current");
           objectSelects.addElement("attribute["+sAttRouteNodeID+"]");
           objectSelects.addElement("owner");
           objectSelects.addElement(DomainConstants.SELECT_TYPE);
           objectSelects.addElement(DomainConstants.SELECT_NAME);
           objectSelects.addElement(DomainConstants.SELECT_REVISION);
           objectSelects.addElement(SELECT_TASK_ASSIGNEE_ID);

           SelectList relSelects = new SelectList();

           MapList ObjectsList= Route.getRelatedObjects(context,
                                                       sRelRouteTask,
                                                       "*",
                                                       objectSelects,
                                                           relSelects,
                                                           true,
                                                           false,
                                                           (short)0,
                                                           null,
                                                           null);
           //modified for the 327641  12/28/2006-- Ends

    //Start: Resume Process
            // Due to Resume Process algorithm, there can be some tasks which are connected to route but are not connected to person
            // These tasks should be removed from the ObjectsList.
            MapList mlFilteredObjectsList = new MapList();
            Map mapCurrentObjectInfo = null;
            for (Iterator itrObjectsList = ObjectsList.iterator(); itrObjectsList.hasNext(); ) {
                mapCurrentObjectInfo = (Map)itrObjectsList.next();

                if (mapCurrentObjectInfo.get(SELECT_TASK_ASSIGNEE_ID) != null) {
                    mlFilteredObjectsList.add(mapCurrentObjectInfo);
                }
            }
            ObjectsList = mlFilteredObjectsList;
    //End: Resume Process

    //Start: Auto-Stop
            // If this task is rejected then send the rejection notice
            
            
            //Find if all tasks are completed on this level and Find at least one task is rejected on this level
            final String SELECT_CURRENT_ROUTE_NODE = "attribute[" + sAttCurrentRouteNode + "]";
            final String SELECT_ROUTE_NODE_ID = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";
            final String SELECT_AUTO_STOP_ON_REJECTION = "attribute[" + sAttAutoStopOnRejection + "]";
            final String SELECT_APPROVAL_STATUS = "attribute["+sAttApprovalStatus+"]";
            final boolean GET_TO = true;
            final boolean GET_FROM = true;
            
            StringList slBusSelect = new StringList();
            slBusSelect.add(com.matrixone.apps.common.Route.SELECT_ROUTE_STATUS);
            slBusSelect.add(SELECT_CURRENT_ROUTE_NODE);
            slBusSelect.add(SELECT_AUTO_STOP_ON_REJECTION);

            Map mapInfo = Route.getInfo(context, slBusSelect);

            String strCurrentRouteLevel = (String)mapInfo.get(SELECT_CURRENT_ROUTE_NODE);
            String sAutoStopOnRejection  = (String)mapInfo.get(SELECT_AUTO_STOP_ON_REJECTION);

            // Expand route and get 'Route Node ID' on relationship 'Route Node' for 'Route Sequence' = 'Current Route Node'
            slBusSelect = new StringList();
            StringList slRelSelect = new StringList();
            slRelSelect.add(SELECT_ROUTE_NODE_ID);

            String strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
            String strTypePattern = "*";
            String strBusWhere = "";
            String strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]==" + strCurrentRouteLevel;
            short nRecurseLevel = (short)1;

            MapList mlRouteNodes = Route.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseLevel, strBusWhere, strRelWhere);
            
            StringBuffer sbufMatchList = new StringBuffer(64);
            Map mapRouteNode = null;
            for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
                mapRouteNode = (Map)itrRouteNodes.next();

                if (sbufMatchList.length() > 0) {
                    sbufMatchList.append(",");
                }
                sbufMatchList.append(mapRouteNode.get(SELECT_ROUTE_NODE_ID));
            }

            // Expand route and get id for tasks with 'Route Node ID' = 'Route Node ID' just found.
            slBusSelect = new StringList(DomainObject.SELECT_ID);
            slBusSelect.add(DomainObject.SELECT_CURRENT);
            slBusSelect.add(SELECT_APPROVAL_STATUS);

            slRelSelect = new StringList();
            strRelPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
            strTypePattern = DomainObject.TYPE_INBOX_TASK;
            strBusWhere = "(attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "] matchlist \"" + sbufMatchList.toString() +"\" \",\")";
            strRelWhere = "";
            nRecurseLevel = (short)1;

            MapList mlTasks = Route.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, GET_TO, !GET_FROM, nRecurseLevel, strBusWhere, strRelWhere);

            // Find if at least one task is incomplete on this level
            Map mapTaskInfo = null;
            String strTaskState = "";
            String strApprovalStatus = "";
            boolean isLevelCompleted = true;
            boolean isTaskRejectedOnThisLevel = false;

            for (Iterator itrRouteNodes = mlTasks.iterator(); itrRouteNodes.hasNext();) {
                mapTaskInfo = (Map)itrRouteNodes.next();

                strTaskState = (String)mapTaskInfo.get(DomainObject.SELECT_CURRENT);
                strApprovalStatus = (String)mapTaskInfo.get(SELECT_APPROVAL_STATUS);

                if(!strTaskState.equals(sStateComplete)) {
                    isLevelCompleted = false;
                }

                if ("Reject".equals(strApprovalStatus)) {
                    isTaskRejectedOnThisLevel = true;
                }
            }
            
            boolean isRouteToBeStopped = false;
            if ("Approve".equals(sRouteActionOfTask))
            {
                if ("Reject".equals(sApprovalStatus)) {
                    isRouteToBeStopped = true;
                }
            }
            // Bug 346841 : Removed following if from another else part to also consider Approve kind of tasks. 
                if (isTaskRejectedOnThisLevel) {
                    isRouteToBeStopped = true;
                }
            
            if (!isLevelCompleted) {
                if ("any".equalsIgnoreCase(sProcessionRule)) {
                    isLevelCompleted = true;
                }
            }

            if (isRouteToBeStopped) {
                if ("Immediate".equals(sAutoStopOnRejection) || isLevelCompleted) {
                   
                    // Set Route Status attribute to Stopped
                    com.matrixone.apps.common.Route rtObj   = new com.matrixone.apps.common.Route();
                	rtObj.stopRoute(context, Route.getId(context));

                    Map objectDetails= null;
                    String sState = null;
                    String sRouteNodeID = null;

                    for( int i=0; i< ObjectsList.size() ; i++)
                    {
                        objectDetails = (Map) ObjectsList.get(i);
                        sState = (String) objectDetails.get("current");
                        sRouteNodeID = (String) objectDetails.get("attribute[" + sAttRouteNodeID + "]");

                        if (sState.equals(sStateAssigned) || sState.equals(sStateReview))
                        {
                            if ((sProcessionRule.toLowerCase()).equals("any"))
                            {
                                DomainRelationship.disconnect(context, sRouteNodeID);

                                // Delete unsigned/non-completed tasks
                                DomainObject.deleteObjects(context, new String[]{(String)objectDetails.get(DomainConstants.SELECT_ID)});
                            }
                        }
                     }//for

                }
            } // if (isRouteToBeStopped)
    //End: Auto-Stop

           // If Approval Status == Reject

           if (!"Reject".equals(sApprovalStatus))
           {
              // Expand route and get the current state of all Inbox Tasks associated with it

    //commented for the BugNo 327641   12/28/2006-- Ends
                int bFound =0;
    // Added Boolean variable to check if there are any tasks having status != Complete for the bug no 340260
                boolean isNonCompleteTasksThere = false;
    // Logic to check if there any tasks that are not in Complete State
    // Till here
                for( int i=0; i< ObjectsList.size() ; i++)
                {
                    Map objectDetails= (Map) ObjectsList.get(i);
                    String sState =(String) objectDetails.get("current");
    // Added for bug no 340260
                        if(!sState.equals("Complete"))
                            isNonCompleteTasksThere = true;
                }
                for( int i=0; i< ObjectsList.size() ; i++)
                {
                    Map objectDetails= (Map) ObjectsList.get(i);
                    String sState =(String) objectDetails.get("current");
    // till here
                    
                    String sRouteNodeID = (String) objectDetails.get("attribute["+sAttRouteNodeID+"]");
                    if (sState.equals(sStateAssigned) || sState.equals(sStateReview))
                    {
                        if ((sProcessionRule.toLowerCase()).equals("any"))
                        {

    // Added "If Approval Status is Abstain" and "there are still tasks which are not completed", then don't promote the Route for bug no 340260
                            if(sApprovalStatus.equals("Abstain") && isNonCompleteTasksThere)
                            {
                                bFound=1;
                                break;
                            }
   //Till here
                            String sRouteNodeConnectionId = (String) objectDetails.get("id");
                            /*bRouteNodeIdFound = 1;
                            for(int count=0; count< lRouteNodeId.size(); count++)
                            {
                            sRouteNodeId = (String)lRouteNodeId.elementAt(i);
                            sRouteNodeIdAttr = (String) lRouteNodeIdAttr.elementAt(i);
                            if(sRouteNodeID.equals(sRouteNodeIdAttr))
                            {
                            bRouteNodeIdFound = 1;
                            break;
                            }
                            }
                            // If Route Node Id not found then
                            // Error out
                            if (bRouteNodeIdFound == 0)
                            {
                            String sCurrTaskType = "";
                            String sCurrTaskName = "";
                            String sCurrTaskRev  = "";
                            arguments = new String[9];
                            arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidRouteNodeId";
                            arguments[1]  = "3";
                            arguments[2]  = "type";
                            arguments[3]  = sCurrTaskType;
                            arguments[4]  = "name";
                            arguments[5]  = sCurrTaskName;
                            arguments[6]  = "rev";
                            arguments[7]  = sCurrTaskRev;
                            arguments[8]  = "";
                            message= mailUtil.getMessage(context,arguments);
                            MqlUtil.mqlCommand(context,"notice "+message+"");
                            return 1;
                            }
    */

                            //DomainRelationship.disconnect(context, sRouteNodeId);
                            //Added code for the Bug NO:330220
                            com.matrixone.apps.common.Route route = (com.matrixone.apps.common.Route)DomainObject.newInstance(context,sRouteId);
                            String orgRelId=route.getRouteNodeRelId(context,sRouteNodeID);
                            DomainRelationship.disconnect(context, orgRelId);

                            //Added code for the Bug NO:330220
                            // Delete unsigned/non-completed tasks
                            String sTaskId = (String)objectDetails.get(DomainConstants.SELECT_ID);
                            java.lang.String[] objectIds = new String[1];
                            objectIds [0] = sTaskId;
                            DomainObject.deleteObjects(context,objectIds);

                            // Send mail to owner of task about deletion
                            String sTaskOwner = (String)objectDetails.get("owner");
                            String sDelTaskType = (String)objectDetails.get(DomainConstants.SELECT_TYPE);
                            String sDelTaskName= (String)objectDetails.get(DomainConstants.SELECT_NAME);
                            String sDelTaskRev = (String)objectDetails.get(DomainConstants.SELECT_REVISION);

                            arguments = new String[19];
                            arguments[0]  = sTaskOwner;
                            arguments[1]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectDeleteTask";
                            arguments[2]  = "0";
                            arguments[3]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageDeletionTask";
                            arguments[4]  = "6";
                            arguments[5]  = "IBType";
                            arguments[6]  = sDelTaskType;
                            arguments[7]  = "IBName";
                            arguments[8]  = sDelTaskName;
                            arguments[9]  = "IBRev";
                            arguments[10]  = sDelTaskRev;
                            arguments[11]  = "IBType2";
                            arguments[12]  = sType;
                            arguments[13]  = "IBName2";
                            arguments[14]  = sName;
                            arguments[15]  = "IBRev2";
                            arguments[16]  = sRev;
                            arguments[17]  = "";
                            arguments[18]  = "";

                            mailUtil.sendNotificationToUser(context,arguments);
                        }
                        else {
                            bFound =1;
                            break;
                        }
                    }
                }//for

                // If None of the Inbox Task objects are returned and Route Status == Started
                if (bFound == 0 && sRouteStatus.equals("Started")) {

                    // Increment Current Route Node attribute on attached Route object
                    sCurrentRouteNode++;
                    Route.setAttributeValue(context,sAttCurrentRouteNode,""+sCurrentRouteNode);

                    // Expand Route Node relationship and get all Relationship Ids whose
                    // Route Sequence == Current Route Node value
                    arguments = new String[5];
                    arguments[0]=sRouteType;
                    arguments[1]=sRouteName;
                    arguments[2]=sRouteRev;
                    arguments[3]=""+sCurrentRouteNode;
                    arguments[4]="0";

                    int outStr1 =  InitiateRoute.InitiateRoute(context, arguments);

                    // Return 0 if no more tasks for route
                    if (outStr1 == 0) {
                        MqlUtil.mqlCommand(context,"override bus $1", sRouteId);
                        Route.promote(context);

                        Route.setAttributeValue(context,sAttRouteStatus,"Finished");

                        // Send notification to subscribers
                        String conArgs[]= {sRouteId};

                        // Expand Object Route relationship to get routed items
                        MapList objectList= Route.getRelatedObjects(context,
                                                                    sRelObjectRoute,
                                                                    "*",
                                                                    objectSelects,
                                                                    relSelects,
                                                                    true,
                                                                    false,
                                                                    (short)0,
                                                                    null,
                                                                    null);

                        if(objectList.size() > 0) {
                            for(int i1=0; i1< objectList.size(); i1++) {
                                Map objectmap =(Map) objectList.get(i1);
                                String sObjectId= (String)objectmap.get(DomainConstants.SELECT_ID);

                                String out11=MqlUtil.mqlCommand(context,"print bus $1 select $2 dump", sObjectId, "grantor["+sRouteDelegationGrantor+"]");

                                if (!out11.equals("FALSE")) {
                                    //modified for the bug 316518
                                    MqlUtil.mqlCommand(context,"modify bus $1 revoke grantor $2", sObjectId, sRouteDelegationGrantor);
                                    //modified for the bug 316518
                                }
                            }
                        }

                        if (sRouteCompletionAction.equals("Promote Connected Object")) {
                           objectSelects = new SelectList();
                           objectSelects.addElement(DomainConstants.SELECT_ID);
                           objectSelects.addElement(DomainConstants.SELECT_TYPE);
                           objectSelects.addElement(DomainConstants.SELECT_NAME);
                           objectSelects.addElement(DomainConstants.SELECT_REVISION);
                           objectSelects.addElement("current.satisfied");
                           objectSelects.addElement("current");
                           objectSelects.addElement("policy");
                           objectSelects.addElement("state");
                           relSelects = new SelectList();
                           relSelects.addElement("attribute["+sAttRouteBaseState+"].value");
                           relSelects.addElement("attribute["+sAttRouteBasePolicy+"].value");

                           // Get all the Route content information
                           MapList objectlist= Route.getRelatedObjects(context,
                                                            sRelObjectRoute,
                                                            "*",
                                                            objectSelects,
                                                            relSelects,
                                                            true,
                                                            false,
                                                            (short)0,
                                                            null,
                                                            null);

                            for(int count=0; count<objectlist.size();count++) {

                                Map object =(Map) objectlist.get(count);
                                String sObjType = (String) object.get(DomainConstants.SELECT_TYPE);
                                String sObjName = (String) object.get(DomainConstants.SELECT_NAME);
                                String sObjRev = (String) object.get(DomainConstants.SELECT_REVISION);
                                String sObjId = (String) object.get(DomainConstants.SELECT_ID);
                                String sIsObjSatisfied = (String) object.get("current.satisfied");
                                String sObjCurrent = (String) object.get("current");
                                String sObjPolicy = (String) object.get("policy");

                                StringList  lObjState=new StringList();
                                if(object.get("state") instanceof StringList) {
                                    lObjState = (StringList) object.get("state");
                                }

                                //String lObjState = (String) object.get("state");
                                String sObjBaseState = (String) object.get("attribute["+sAttRouteBaseState+"].value");
                                String sObjBasePolicy= (String) object.get("attribute["+sAttRouteBasePolicy+"].value");
                                int bPromoteObject = 1;


                                // Check if object state and policy maches with base state and policy
                                if (!sObjBaseState.equals("Ad Hoc")) {
                                    sObjBasePolicy= PropertyUtil.getSchemaProperty(context,sObjBasePolicy);
                                    sObjBaseState= FrameworkUtil.lookupStateName(context, sObjBasePolicy, sObjBaseState);

                                    // Get names from properties
                                    String sTempStore =sObjBaseState;
                                    if (sObjBaseState.equals("")) {
                                        arguments = new String[13];
                                        arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidPolicy";
                                        arguments[1]  = "5";
                                        arguments[2]  = "State";
                                        arguments[3]  = sTempStore;
                                        arguments[4]  = "Type";
                                        arguments[5]  = sRouteType;
                                        arguments[6]  = "OType";
                                        arguments[7]  = sObjType;
                                        arguments[8]  = "OName";
                                        arguments[9]  = sObjName;
                                        arguments[10]  = "ORev";
                                        arguments[11]  = sObjRev;
                                        arguments[12]  = "";

                                        message= mailUtil.getMessage(context,arguments);
                                        MqlUtil.mqlCommand(context, "notice $1",message);

                                        return 1;
                                    }

                                    sTempStore =sObjBasePolicy;

                                    if (sObjBasePolicy.equals("")) {

                                        arguments = new String[13];
                                        arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidState";
                                        arguments[1]  = "5";
                                        arguments[2]  = "Policy";
                                        arguments[3]  = sTempStore;
                                        arguments[4]  = "Type";
                                        arguments[5]  = sRouteType;
                                        arguments[6]  = "OType";
                                        arguments[7]  = sObjType;
                                        arguments[8]  = "OName";
                                        arguments[9]  = sObjName;
                                        arguments[10]  = "ORev";
                                        arguments[11]  = sObjRev;
                                        arguments[12]  = "";
                                        message= mailUtil.getMessage(context,arguments);
                                        MqlUtil.mqlCommand(context, "notice $1",message);

                                        return 1;
                                    }
                                }

                                //the below else block is commented for the bug 319223 -- this functionality regarding this bug
                                /* else
                                {
                                if (bConsiderAdhocRoutes.equals("FALSE"))
                                {
                                continue;
                                }
                                }*/

                                if (sObjBaseState.equals("Ad Hoc") && (!sObjBaseState.equals(sObjCurrent) || !sObjBasePolicy.equals(sObjPolicy))) {
                                    continue;
                                }

                                // Check if object is in the last state
                                /* if ([lsearch $lObjState "$sObjCurrent"] == [expr [llength $lObjState] - 1]) {
                                continue;
                                }*/

                                //Modified for Bug No: 293332 and Bug no: 293506
                                if(lObjState.indexOf(sObjCurrent) == (lObjState.size()-1)) {
                                    continue;
                                }

                                objectSelects = new SelectList();
                                objectSelects.addElement("current");
                                objectSelects.addElement(DomainConstants.SELECT_TYPE);
                                objectSelects.addElement(DomainConstants.SELECT_NAME);
                                objectSelects.addElement(DomainConstants.SELECT_REVISION);
                                relSelects = new SelectList();
                                relSelects.addElement("attribute["+sAttRouteBaseState+"].value");
                                relSelects.addElement("attribute["+sAttRouteBasePolicy+"].value");
                                DomainObject dObject = new DomainObject(sObjId);

                                //should retrieve only Route objects
                                //Include Route sub_types if applicable, use addPattern()
                                Pattern typePattern = new Pattern(DomainObject.TYPE_ROUTE);

                                //Modified for Bug No: 293332 and Bug no: 293506
                                MapList ObjectList= dObject.getRelatedObjects(context,
                                                                                                sRelObjectRoute,
                                                                                                typePattern.getPattern(),//"*",
                                                                                                objectSelects,
                                                                                                relSelects,
                                                                                                false,
                                                                                                true,
                                                                                                (short)1,
                                                                                                "",
                                                                                                "");

                                // Check for each object if there is any route which is not complete

                                for(int i=0; i<ObjectList.size() ; i++) {
                                    Map objectsMap = (Map)ObjectList.get(i);
                                    String sObjRouteBaseState =(String) objectsMap.get("attribute["+sAttRouteBaseState+"].value");
                                    String sObjRouteBasePolicy =(String) objectsMap.get("attribute["+sAttRouteBasePolicy+"].value");
                                    String sObjRouteType =(String) objectsMap.get(DomainConstants.SELECT_TYPE);
                                    String sObjRouteName =(String) objectsMap.get(DomainConstants.SELECT_NAME);
                                    String sObjRouteRev =(String) objectsMap.get(DomainConstants.SELECT_REVISION);
                                    String sObjRouteCurrent =(String) objectsMap.get("current");

                                    if (sObjRouteBaseState.equals("")) {
                                        sObjRouteBaseState = "Ad Hoc";
                                    }

                                    if (!sObjRouteBaseState.equals("Ad Hoc")) {
                                        //Get names from properties

                                        // Bug 293332
                                        String sTempStore = sObjRouteBasePolicy;
                                        sObjRouteBasePolicy= PropertyUtil.getSchemaProperty(context,sObjRouteBasePolicy);

                                        if (sObjRouteBasePolicy.equals("")) {
                                            arguments = new String[13];
                                            arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidPolicy";
                                            arguments[1]  = "5";
                                            arguments[2]  = "State";
                                            arguments[3]  = sTempStore;
                                            arguments[4]  = "Type";
                                            arguments[5]  = sRouteType;
                                            arguments[6]  = "OType";
                                            arguments[7]  = sObjType;
                                            arguments[8]  = "OName";
                                            arguments[9]  = sObjName;
                                            arguments[10]  = "ORev";
                                            arguments[11]  = sObjRev;
                                            arguments[12]  = "";

                                            message= mailUtil.getMessage(context,arguments);
                                            MqlUtil.mqlCommand(context, "notice $1",message);

                                            return 1;
                                        }
                                        // Bug 293332

                                        sTempStore = sObjRouteBaseState;
                                        sObjRouteBaseState = FrameworkUtil.lookupStateName(context, sObjRouteBasePolicy, sObjRouteBaseState);
                                        if (sObjRouteBaseState.equals("")) {
                                            arguments = new String[13];
                                            arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidState";
                                            arguments[1]  = "5";
                                            arguments[2]  = "Policy";
                                            arguments[3]  = sTempStore;
                                            arguments[4]  = "Type";
                                            arguments[5]  = sRouteType;
                                            arguments[6]  = "OType";
                                            arguments[7]  = sObjType;
                                            arguments[8]  = "OName";
                                            arguments[9]  = sObjName;
                                            arguments[10]  = "ORev";
                                            arguments[11]  = sObjRev;
                                            arguments[12]  = "";

                                            message= mailUtil.getMessage(context,arguments);
                                            MqlUtil.mqlCommand(context, "notice $1",message);

                                            return 1;
                                        }
                                    }

                                    // If Route Base State is Ad Hoc or Route Base State and Policy are
                                    // same as object state and policy
                                    if ((sObjRouteBaseState.equals("Ad Hoc")) || (sObjRouteBaseState.equals(sObjCurrent) && sObjRouteBasePolicy.equals(sObjPolicy))) {
                                        // Set flag if Route still in work
                                        if (!sObjRouteCurrent.equals(sStateComplete)) {
                                            sBufInCompleteRoutes.append(sObjRouteType+" ");
                                            sBufInCompleteRoutes.append(sObjRouteName+" ");
                                            sBufInCompleteRoutes.append(sObjRouteRev+",");
                                            // Bug 293332
                                            bPromoteObject = 0;
                                        }
                                    }
                                }//for

                                if (!sInCompleteRoutes.equals("")) {
                                    sBufRoutesInProcess.append(sObjType+" ");
                                    sBufRoutesInProcess.append(sObjName+" ");
                                    sBufRoutesInProcess.append(sObjRev+" : ");
                                    sBufRoutesInProcess.append(sInCompleteRoutes+"\n");
                                }

                                // Check if all the signatures are approved
                                if (sIsObjSatisfied.equals("FALSE")) {
                                    sBufObjectsNotSatisfied.append(sObjType+" ");
                                    sBufObjectsNotSatisfied.append(sObjName+" ");
                                    sBufObjectsNotSatisfied.append(sObjRev+"\n");
                                    // Bug 293332
                                    bPromoteObject = 0;
                                }

                                if (bPromoteObject == 1) {
                                    MqlUtil.mqlCommand(context,"promote bus $1", sObjId);
                                    sBufPromotedObjects.append(sObjType+" ");
                                    sBufPromotedObjects.append(sObjName+" ");
                                    sBufPromotedObjects.append(sObjRev+"\n");
                                }
                            }//for

                            sInCompleteRoutes = sBufInCompleteRoutes.toString();
                            sRoutesInProcess  = sBufRoutesInProcess.toString();
                            sObjectsNotSatisfied = sBufObjectsNotSatisfied.toString();
                            sPromotedObjects = sBufPromotedObjects.toString();

                            if (sObjectsNotSatisfied.equals("") && sRoutesInProcess.equals("")) {
                                if (sPromotedObjects.equals("")) {
                                    arguments = new String[3];
                                    arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.None";
                                    arguments[1]  = "0";
                                    arguments[2]  = "";

                                    sPromotedObjects =mailUtil.getMessage(context,arguments);
                                }

                  arguments = new String[21];
                                arguments[0]  = sOwner;
                                arguments[1]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectComplete";
                                arguments[2]  = "0";
                  //Bug # 335612 - modified below strig resource form emxFramework.ProgramObject.eServicecommonCompleteTask.MessageComplete to emxFramework.ProgramObject.eServicecommonCompleteTask.MessageCompletePromoteConnectedObject
                  arguments[3]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageCompletePromoteConnectedObject";
                  arguments[4]  = "7";
                                arguments[5]  = "RType";
                                arguments[6]  = sRouteType;
                                arguments[7]  = "RName";
                                arguments[8]  = sRouteName;
                                arguments[9]  = "RRev";
                                arguments[10]  = sRouteRev;
                                arguments[11]  = "PromotedObj";
                                arguments[12]  = sPromotedObjects;
                  //Bug # 335612
                  arguments[13]  = "RType";
                  arguments[14]  = sRouteType;
                  arguments[15]  = "RName";
                  arguments[16]  = sRouteName;
                  arguments[17]  = "RRev";
                  arguments[18]  = sRouteRev;
                  arguments[19]  = sRouteId;
                  arguments[20]  = "";
                  String routeSymbolicName = FrameworkUtil.getAliasForAdmin(context, "type", Route.getType(context), true);
                  String mappedTreeName = UIMenu.getTypeToTreeNameMapping(routeSymbolicName);
                  String[] treeMenu = {mappedTreeName};
                  mailUtil.setTreeMenuName(context, treeMenu);
                                mailUtil.sendNotificationToUser(context,arguments);
                            }
                            else {
                                if (sRoutesInProcess.equals("")) {
                                    arguments = new String[3];
                                    arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.None";
                                    arguments[1]  = "0";
                                    arguments[2]  = "";

                                    sRoutesInProcess =mailUtil.getMessage(context,arguments);
                                }
                                if (sObjectsNotSatisfied.equals("")) {
                                    arguments = new String[3];
                                    arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.None";
                                    arguments[1]  = "0";
                                    arguments[2]  = "";
                                    sObjectsNotSatisfied =mailUtil.getMessage(context,arguments);
                                }

                                arguments = new String[19];
                                arguments[0]  = sOwner;
                                arguments[1]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectNotComplete";
                                arguments[2]  = "0";
                                arguments[3]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageNotComplete";
                                arguments[4]  = "6";
                                arguments[5]  = "RType";
                                arguments[6]  = sRouteType;
                                arguments[7]  = "RName";
                                arguments[8]  = sRouteName;
                                arguments[9]  = "RRev";
                                arguments[10]  = sRouteRev;
                                arguments[11]  = "PromotedObj";
                                arguments[12]  = sPromotedObjects;
                                arguments[13]  = "RInProcess";
                                arguments[14]  = sRoutesInProcess;
                                arguments[15]  = "ONotApproved";
                                arguments[16]  = sObjectsNotSatisfied;
                                arguments[17]  = "";
                                arguments[18]  = "";

                                mailUtil.sendNotificationToUser(context,arguments);
                            }
                        }
                        else {
                            arguments = new String[13];
                            arguments[0]  = sOwner;
                            arguments[1]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectRouteComplete";
                            arguments[2]  = "0";
                            arguments[3]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageRouteComplete";
                            arguments[4]  = "3";
                            arguments[5]  = "RType";
                            arguments[6]  = sRouteType;
                            arguments[7]  = "RName";
                            arguments[8]  = sRouteName;
                            arguments[9]  = "RRev";
                            arguments[10]  = sRouteRev;
                            arguments[11]  = sRouteId;
                            arguments[12]  = "";
                            String routeSymbolicName = FrameworkUtil.getAliasForAdmin(context, "type", Route.getType(context), true);
                            String mappedTreeName = UIMenu.getTypeToTreeNameMapping(routeSymbolicName);
                            String[] treeMenu = {mappedTreeName};
                            mailUtil.setTreeMenuName(context, treeMenu);
                      // Added for the bug no 335211 - Begin
                      String oldagentName=mailUtil.getAgentName(context,args);
                      String user=context.getUser();
                      mailUtil.setAgentName(context,new String[]{user});
                      mailUtil.sendNotificationToUser(context,arguments);
                      mailUtil.setAgentName(context,new String[]{oldagentName});
                      // Added for the bug no 335211 - Ends
                        }
                    }
                }
            }
           
           if ("Approve".equals(sRouteActionOfTask)) {
               if ("Reject".equals(sApprovalStatus)) {
                   Map payload = new HashMap();
                   payload.put("subject", "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectReject");
                   payload.put("message", "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageReject");
                   String[] messageKeys = {"name", "IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
                   String[] messageValues = {sLastName + "," + sFirstName+""+" (" + sPerson + ")",
                   						  sType, sName, sRev, sRouteType, sRouteName, sRouteRev};
                   payload.put("messageKeys", messageKeys);
                   payload.put("messageValues", messageValues);
                   payload.put("tableHeader", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableHeader");
                   payload.put("tableRow", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableData");
                   String[] tableKeys = {"TaskType", "RouteName", "TaskName", "TaskOwner", "CompletionDate", "Comments"};
                   String sInboxTaskLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, sName, ObjectId);
                   String sRouteLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, sRouteName, sRouteId);
                   String[] tableValues = {sRouteActionOfTask, sRouteLink, sInboxTaskLink, sPerson, sActualCompletionDate, sComments};
                   payload.put("tableRowKeys", tableKeys);
                   payload.put("tableRowValues", tableValues);
                   emxNotificationUtil_mxJPO.objectNotification(context, ObjectId, "APPObjectRouteTaskRejectedEvent", payload);
               }
               else if ("Approve".equals(sApprovalStatus)) {
                   Map payload = new HashMap();
                   payload.put("subject", "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectApprove");
                   payload.put("message", "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageApprove");
                   String[] messageKeys = {"name", "IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
                   String[] messageValues = {sLastName + "," + sFirstName+""+" (" + sPerson + ")",
                   						  sType, sName, sRev, sRouteType, sRouteName, sRouteRev};
                   payload.put("messageKeys", messageKeys);
                   payload.put("messageValues", messageValues);
                   payload.put("tableHeader", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableHeader");
                   payload.put("tableRow", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableData");
                   String[] tableKeys = {"TaskType", "RouteName", "TaskName", "TaskOwner", "CompletionDate", "Comments"};
                   String sInboxTaskLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, sName, ObjectId);
                   String sRouteLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, sRouteName, sRouteId);
                   String[] tableValues = {sRouteActionOfTask, sRouteLink, sInboxTaskLink, sPerson, sActualCompletionDate, sComments};
                   payload.put("tableRowKeys", tableKeys);
                   payload.put("tableRowValues", tableValues);
                   new emxNotificationUtilBase_mxJPO(context,null).objectNotification(context, ObjectId, "APPObjectRouteTaskApprovedEvent", payload);
               }
           }
               
        } 
        catch(Exception e) {
        	if(e.getMessage().toString().contains("promote business object failed"))
        	{
        		 String strLanguage = context.getSession().getLanguage();
                 Locale strLocale = context.getLocale();
				 String strDateError =EnoviaResourceBundle.getProperty(context,
                                                "emxComponentsStringResource",
                                                strLocale,
                                                "emxComponents.Common.PromoteConnectObjectFailed");
        		emxContextUtil_mxJPO.mqlNotice(context,strDateError);
        	}else {
         throw e;
        	}
        }
        finally {
            ShadowAgent.popContext(context,null);
        }

        return 0;
    }// eof method
}// eof class
