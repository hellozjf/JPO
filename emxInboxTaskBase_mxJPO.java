/*
 *  emxInboxTaskBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */


import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.Attribute;
import matrix.db.AttributeItr;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectAttributes;
import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.Group;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Relationship;
import matrix.db.Role;
import matrix.util.List;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.dassault_systemes.enovia.bps.widget.UIFieldValue;
import com.dassault_systemes.enovia.bps.widget.UIWidget;
import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.InboxTask;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.FrameworkStringResource;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;


/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxInboxTaskBase_mxJPO extends emxDomainObject_mxJPO
{
    private static final String strAttrBracket  = "attribute[";
    private static final String strCloseBracket = "]";
    private static final String strFromBracket = "from[";
    private static final String strBracketToToBracket = "].to.to[";

    private static final String sAttrReviewersComments = PropertyUtil.getSchemaProperty("attribute_ReviewersComments");
    private static final String sAttrReviewTask = PropertyUtil.getSchemaProperty("attribute_ReviewTask");
    private static final String sAttrReviewCommentsNeeded = PropertyUtil.getSchemaProperty("attribute_ReviewCommentsNeeded");
    private static final String sAttrRouteAction = PropertyUtil.getSchemaProperty("attribute_RouteAction");
    private static final String sAttrScheduledCompletionDate = PropertyUtil.getSchemaProperty("attribute_ScheduledCompletionDate");
    private static final String sAttrTitle = PropertyUtil.getSchemaProperty("attribute_Title");
    private static final String selTaskCompletedDate = PropertyUtil.getSchemaProperty("attribute_ActualCompletionDate");
    private static final String sTypeInboxTask = PropertyUtil.getSchemaProperty("type_InboxTask");
    private static final String sRelProjectTask = PropertyUtil.getSchemaProperty("relationship_ProjectTask");
    private static final String sRelRouteTask = PropertyUtil.getSchemaProperty("relationship_RouteTask");
    private static final String sRelProjectRouteTask = PropertyUtil.getSchemaProperty("relationship_ProjectRoute");
    private static final String sRelRouteScope = PropertyUtil.getSchemaProperty("relationship_RouteScope");
    private static final String policyTask = PropertyUtil.getSchemaProperty("policy_InboxTask");

    private static final String strAttrRouteAction = "attribute["+sAttrRouteAction +"]";
    private static final String strAttrCompletionDate ="attribute["+sAttrScheduledCompletionDate+"]";
    private static final String strAttrTitle="attribute["+sAttrTitle+"]";
    private static final String strAttrTaskCompletionDate ="attribute["+selTaskCompletedDate+"]";
    private static final String strAttrTaskApprovalStatus  = getAttributeSelect(DomainObject.ATTRIBUTE_APPROVAL_STATUS);

    private static String routeIdSelectStr="from["+sRelRouteTask+"].to.id";
    private static String routeTypeSelectStr="from["+sRelRouteTask+"].to.type";
    private static String routeNameSelectStr ="from["+sRelRouteTask+"].to.name";
    private static String routeOwnerSelectStr="from["+sRelRouteTask+"].to.owner";
    private static String objectNameSelectStr="from["+sRelRouteTask+"].to.to["+sRelRouteScope+"].from.name";
    private static String objectIdSelectStr="from["+sRelRouteTask+"].to.to["+sRelRouteScope+"].from.id";
    private static final String routeApprovalStatusSelectStr ="from["+sRelRouteTask+"].to."+Route.SELECT_ROUTE_STATUS ;
    private i18nNow loc = new i18nNow();
    protected String lang=null;
    protected String rsBundle=null;

    private static final String sRelAssignedTask = PropertyUtil.getSchemaProperty("relationship_AssignedTasks");
    private static final String sRelSubTask = PropertyUtil.getSchemaProperty("relationship_Subtask");
    private static final String sRelWorkflowTask = PropertyUtil.getSchemaProperty("relationship_WorkflowTask");
    private static final String sRelWorkflowTaskAssinee = PropertyUtil.getSchemaProperty("relationship_WorkflowTaskAssignee");
    private static final String sRelWorkflowTaskDeliverable = PropertyUtil.getSchemaProperty("relationship_TaskDeliverable");

    private static final String workflowIdSelectStr = "to["+sRelWorkflowTask+"].from.id";
    private static final String workflowNameSelectStr = "to["+sRelWorkflowTask+"].from.name";
    private static final String workflowTypeSelectStr = "to["+sRelWorkflowTask+"].from.type";

    private static final String sTypeWorkflow = PropertyUtil.getSchemaProperty("type_Workflow");
    private static final String sTypeWorkflowTask = PropertyUtil.getSchemaProperty("type_WorkflowTask");

    private static final String policyWorkflowTask = PropertyUtil.getSchemaProperty("policy_WorkflowTask");
    private static final String attrworkFlowDueDate = PropertyUtil.getSchemaProperty("attribute_DueDate");
    private static final String attrTaskEstinatedFinishDate = PropertyUtil.getSchemaProperty("attribute_TaskEstimatedFinishDate");
    private static final String attrworkFlowActCompleteDate = PropertyUtil.getSchemaProperty("attribute_ActualCompletionDate");
    private static final String attrworkFlowInstructions = PropertyUtil.getSchemaProperty("attribute_Instructions");
    private static final String attrTaskFinishDate = PropertyUtil.getSchemaProperty("attribute_TaskActualFinishDate");

    private static String strAttrworkFlowDueDate = "attribute[" + attrworkFlowDueDate + "]";
    private static String strAttrTaskEstimatedFinishDate = "attribute[" + attrTaskEstinatedFinishDate + "]";
    private static String strAttrTaskFinishDate = "attribute[" + attrTaskFinishDate + "]";
    private static String strAttrworkFlowCompletinDate = "attribute[" + attrworkFlowActCompleteDate + "]";

    private static final String TYPE_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty("Policy", DomainObject.POLICY_INBOX_TASK, "state_Review");
    private static final String TYPE_INBOX_TASK_STATE_ASSIGNED = PropertyUtil.getSchemaProperty("Policy", DomainObject.POLICY_INBOX_TASK, "state_Assigned");


    // added for IR - 043921V6R2011
    public static final String  SELECT_TEMPLATE_OWNING_ORG_ID =  "from["+ RELATIONSHIP_ROUTE_TASK + "].to.from[" + RELATIONSHIP_INITIATING_ROUTE_TEMPLATE
                                                     + "].to.to[" + RELATIONSHIP_OWNING_ORGANIZATION + "].from.id";

    public static final String SELECT_ROUTE_NODE_ID = getAttributeSelect(ATTRIBUTE_ROUTE_NODE_ID);
    public static final String SELECT_TASK_ASSIGNEE_CONNECTION = "from[" + RELATIONSHIP_PROJECT_TASK + "].id";
    protected static final String PERSON_WORKSPACE_LEAD_GRANTOR = PropertyUtil.getSchemaProperty("person_WorkspaceLeadGrantor");
    protected static final String SELECT_TASK_ASSIGNEE_NAME = "from[" + RELATIONSHIP_PROJECT_TASK + "].to.name";

    private static final String TASK_PROJECT_ID = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
    private static final String TASK_PROJECT_TYPE = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
    private static final String TASK_PROJECT_NAME = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxInboxTaskBase_mxJPO (Context context, String[] args)
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
     * @since AEF Rossini
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }

    /**
     * action trigger on promote event of state "Assigned" of policy "Inbox Task"
     * sends notification to the Route Owner if it needs to be reviewed, else promotes to complete
     *
     * @param context the eMatrix Context object
     * @param holds inboxtaskid
     * @return void
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public void triggerActionPromoteOnAssignedState(matrix.db.Context context, String[] args)
        throws Exception
    {

        //get the "Review Task" attribute, if it is "No", promote to "Complete"
        //else send notification to the route owner for task review
        String reviewTask = getInfo(context, "attribute["+ATTRIBUTE_REVIEW_TASK+"]");
        if(reviewTask.equalsIgnoreCase("No"))
        {
            //promote to "Complete"
            setState(context, STATE_INBOX_TASK_COMPLETE);
            String isFDAEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.EnableFDA");
            String routeAction = getInfo(context, "attribute["+ATTRIBUTE_ROUTE_ACTION+"]");

            if(isFDAEnabled != null && "true".equalsIgnoreCase(isFDAEnabled) && "Approve".equals(routeAction)){
        try {
          String ATTRIBUTE_MIDDLE_NAME = PropertyUtil.getSchemaProperty(context,"attribute_MiddleName");
          DomainObject person = PersonUtil.getPersonObject(context);
          StringList personSelects = new StringList();
          personSelects.addElement("attribute[" + ATTRIBUTE_LAST_NAME + "]");
          personSelects.addElement("attribute[" + ATTRIBUTE_FIRST_NAME + "]");
          personSelects.addElement("attribute[" + ATTRIBUTE_MIDDLE_NAME + "]");

          Map personInfo = person.getInfo(context,personSelects);

          String lastName = (String)personInfo.get("attribute[" + ATTRIBUTE_LAST_NAME + "]");
          String firstName = (String)personInfo.get("attribute[" + ATTRIBUTE_FIRST_NAME + "]");
          String middleName = (String)personInfo.get("attribute[" + ATTRIBUTE_MIDDLE_NAME + "]");
          // Added for bug 306764
          if(middleName.equals("Unknown"))
          {
              middleName = "";
          }

          //Get the task info.
          String SELECT_ROUTE_NAME = "from[" + RELATIONSHIP_ROUTE_TASK + "].to.name";
          StringList taskSelects = new StringList();
          taskSelects.add("attribute[" + ATTRIBUTE_APPROVAL_STATUS + "]");
          taskSelects.add(SELECT_ROUTE_NAME);
          taskSelects.add("attribute[" + ATTRIBUTE_TITLE + "]");
          taskSelects.add("attribute[" + ATTRIBUTE_APPROVERS_RESPONSIBILITY + "]");
          taskSelects.add("attribute[" + ATTRIBUTE_COMMENTS + "]");

          Map taskInfo = getInfo(context,taskSelects);

          String approvalStatus = (String)taskInfo.get("attribute[" + ATTRIBUTE_APPROVAL_STATUS + "]");
          String routeName = (String)taskInfo.get(SELECT_ROUTE_NAME);
          String title = (String)taskInfo.get("attribute[" + ATTRIBUTE_TITLE + "]");
          String approversResponsibility = (String)taskInfo.get("attribute[" + ATTRIBUTE_APPROVERS_RESPONSIBILITY + "]");
          String comments = (String)taskInfo.get("attribute[" + ATTRIBUTE_COMMENTS + "]");

          String SELECT_ROUTE_ID = "from[" + RELATIONSHIP_ROUTE_TASK + "].to.id";
          String routeId = getInfo(context,SELECT_ROUTE_ID);

          emxRoute_mxJPO route = new emxRoute_mxJPO(context, null);
          route.setId(routeId);

          //Define route selects
          StringList objectSelects = new StringList();
          objectSelects.addElement(SELECT_ID);

          ContextUtil.startTransaction(context,false);
          ExpansionIterator expItr = route.getExpansionIterator(context,                    // matrix context
                                                      RELATIONSHIP_OBJECT_ROUTE,    // relationship pattern
                                                      "*",                // object pattern
                                                      objectSelects,              // object selects
                                                      new StringList(0),                       // relationship selects
                                                      true,                      // to direction
                                                      false,                       // from direction
                                                      (short) 1,              // recursion level
                                                      null,                       // object where clause
                                                      null,                       // relationship where clause
                                                      (short)0,                        // cached list
                                                      false,
                                                      false,
                                                      (short)100,
                                                      false);
          MapList routedObjects = null;
          try {
              routedObjects = FrameworkUtil.toMapList(expItr,(short)0,null,null,null,null);
          } finally {
              expItr.close();
          }
          ContextUtil.commitTransaction(context);

          Iterator routeItr = routedObjects.iterator();
          while(routeItr.hasNext())
          {
            Map routedObjectMap = (Map)routeItr.next();
            String routedObjectId = (String)routedObjectMap.get(SELECT_ID);
            String historyEntry = "Task Assignee:" +  lastName + "," + firstName + " " + middleName + " Route Name:'"
                         + routeName + "' Task Name:'" + title + "' Role Performed:'"
                         + approversResponsibility + "' Assignee Comments:'" + comments + "'";

            			String mql =  "modify bus $1 add history $2 comment $3";
            			MqlUtil.mqlCommand(context, mql, true, routedObjectId, approvalStatus, historyEntry);
          }
      } catch (Exception e){
        System.out.println("exception " + e.getMessage());
      }
      }
        }
        // The below code is commented since we are using emxNotificationUtil jpo for firing the email
        /*else
        {
            //build the select list
            StringList selects = new StringList();
            selects.addElement("id");
            selects.addElement("type");
            selects.addElement("name");
            selects.addElement("revision");
            selects.addElement("owner");
            selects.addElement("attribute["+ATTRIBUTE_TITLE+"]");
            selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.type");
            selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.name");
            selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.revision");
            selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.owner");

            //get all task-route details
            Map taskMap = getInfo(context, selects);

            String taskName = (String) taskMap.get("attribute["+ATTRIBUTE_TITLE+"]");
            if(null == taskName || taskName.equals(""))
            {
                taskName = (String) taskMap.get("name");
            }
            String taskId     = (String) taskMap.get("id");
            String taskType   = (String) taskMap.get("type");
            String taskRev    = (String) taskMap.get("revision");
            String taskOwner  = (String) taskMap.get("owner");
            String routeType  = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.type");
            String routeName  = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.name");
            String routeRev   = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.revision");
            String routeOwner = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.owner");

            //use emxMailUtil class to send notifications
            ${CLASS:emxMailUtil} mailUtil = new ${CLASS:emxMailUtil}(context, null);

            StringList toList = new StringList();
            toList.addElement(routeOwner);
            String subjectKey = "emxFramework.InboxTaskJPO.triggerActionPromoteOnAssignedState.SubjectReviewInitiated";
            String[] subjectKeys = {};
            String[] subjectValues = {};

            String messageKey = "emxFramework.InboxTaskJPO.triggerActionPromoteOnAssignedState.MessageReviewInitiated";
            String[] messageKeys = {"IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
            String[] messageValues = {taskType, taskName, taskRev, routeType, routeName, routeRev};

            StringList objectIdList = new StringList();
            objectIdList.addElement(taskId);

            mailUtil.sendNotification(context,
                              toList,
                              null,
                              null,
                              subjectKey,
                              subjectKeys,
                              subjectValues,
                              messageKey,
                              messageKeys,
                              messageValues,
                              objectIdList,
                              null);
        }*/
    }

    /**
     * action trigger on demote event of state "Review" of policy "Inbox Task"
     * sends notification to the Task Assignee informing him of the Task demotion
     *
     * @param context the eMatrix Context object
     * @param holds inboxtaskid
     * @return void
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public void triggerActionDemoteOnReviewState(matrix.db.Context context, String[] args)
        throws Exception
    {
         // The below code is commented since we are using emxNotificationUtil jpo for firing the email
         //build the selects
       /* StringList selects = new StringList();
        selects.addElement("type");
        selects.addElement("name");
        selects.addElement("revision");
        selects.addElement("owner");
        selects.addElement("attribute["+ATTRIBUTE_TITLE+"]");
        selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.type");
        selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.name");
        selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.revision");

        //get all details required
        Map taskMap = getInfo(context, selects);

        String taskName = (String) taskMap.get("attribute["+ATTRIBUTE_TITLE+"]");
        if(null == taskName || taskName.equals(""))
        {
            taskName = (String) taskMap.get("name");
        }
        String taskId     = (String) taskMap.get("id");
        String taskType   = (String) taskMap.get("type");
        String taskRev    = (String) taskMap.get("revision");
        String taskOwner  = (String) taskMap.get("owner");
        String routeType  = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.type");
        String routeName  = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.name");
        String routeRev   = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.revision");

        //use emxMailUtil class to send notifications
        ${CLASS:emxMailUtil} mailUtil = new ${CLASS:emxMailUtil}(context, null);

        StringList toList = new StringList();
        toList.addElement(taskOwner);
        String subjectKey = "emxFramework.InboxTaskJPO.triggerActionDemoteOnReviewState.SubjectReviewCompleted";
        String[] subjectKeys = {};
        String[] subjectValues = {};

        String messageKey = "emxFramework.InboxTaskJPO.triggerActionDemoteOnReviewState.MessageReviewCompletedDemote";
        String[] messageKeys = {"IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
        String[] messageValues = {taskType, taskName, taskRev, routeType, routeName, routeRev};

        StringList objectIdList = new StringList();
        objectIdList.addElement(taskId);

        mailUtil.sendNotification(context,
                          toList,
                          null,
                          null,
                          subjectKey,
                          subjectKeys,
                          subjectValues,
                          messageKey,
                          messageKeys,
                          messageValues,
                          objectIdList,
                          null);*/
    }

    /**
     * action trigger on promote event of state "Review" of policy "Inbox Task"
     * sends notification to the Task Assignee informing him of the Task promotion
     *
     * @param context the eMatrix Context object
     * @param holds inboxtaskid
     * @return void
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public void triggerActionPromoteOnReviewState(matrix.db.Context context, String[] args)
        throws Exception
    {
        //this trigger will be executed on direct promotion from Assigned to Complete
        //and from a promote from Review to Complete
        //notifications should be sent only in the second case (if the task has undergone a review)
        // The below code is commented since we are using emxNotificationUtil jpo for firing the email
       /* String reviewTask = getInfo(context, "attribute["+ATTRIBUTE_REVIEW_TASK+"]");
        if(reviewTask.equalsIgnoreCase("No"))
        {
            return;
        }
        //build selects
        StringList selects = new StringList();
        selects.addElement("type");
        selects.addElement("name");
        selects.addElement("revision");
        selects.addElement("owner");
        selects.addElement("attribute["+ATTRIBUTE_TITLE+"]");
        selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.type");
        selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.name");
        selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.revision");

        //get all details required
        Map taskMap = getInfo(context, selects);

        String taskName = (String) taskMap.get("attribute["+ATTRIBUTE_TITLE+"]");
        if(null == taskName || taskName.equals(""))
        {
            taskName = (String) taskMap.get("name");
        }
        String taskId     = (String) taskMap.get("id");
        String taskType   = (String) taskMap.get("type");
        String taskRev    = (String) taskMap.get("revision");
        String taskOwner  = (String) taskMap.get("owner");
        String routeType  = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.type");
        String routeName  = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.name");
        String routeRev   = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.revision");

        //use emxMailUtil class to send notifications
        ${CLASS:emxMailUtil} mailUtil = new ${CLASS:emxMailUtil}(context, null);

        StringList toList = new StringList();
        toList.addElement(taskOwner);
        String subjectKey = "emxFramework.InboxTaskJPO.triggerActionDemoteOnReviewState.SubjectReviewCompleted";
        String[] subjectKeys = {};
        String[] subjectValues = {};

        String messageKey = "emxFramework.InboxTaskJPO.triggerActionPromoteOnReviewState.MessageReviewCompletedPromote";
        String[] messageKeys = {"IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
        String[] messageValues = {taskType, taskName, taskRev, routeType, routeName, routeRev};

        mailUtil.sendNotification(context,
                          toList,
                          null,
                          null,
                          subjectKey,
                          subjectKeys,
                          subjectValues,
                          messageKey,
                          messageKeys,
                          messageValues,
                          null,
                          null);*/
    }

    /**
     * check trigger on promote event of state "Review" of policy "Inbox Task"
     * if the task is to be reviewed and the user is not the Route owner, an MQL error message needs to be shown
     * since Review can be done by the route owner only
     *
     * @param context the eMatrix Context object
     * @param holds inboxtaskid
     * @return int - "0" if check is true, "1" if check is false
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public int triggerCheckPromoteOnReviewState(matrix.db.Context context, String[] args)
        throws Exception
    {
        //build selects
        StringList selects = new StringList();
        selects.addElement("attribute["+ATTRIBUTE_REVIEW_TASK+"]");
        selects.addElement("from["+RELATIONSHIP_ROUTE_TASK+"].to.owner");
        selects.addElement("current");
        selects.addElement("owner");
        selects.addElement(strAttrRouteAction);        
        selects.addElement("attribute[" + DomainObject.ATTRIBUTE_COMMENTS + "]");        
		selects.addElement("attribute[" + ATTRIBUTE_APPROVAL_STATUS + "]");        

        //get the details required
        Map taskMap = getInfo(context, selects);
        String reviewTask = (String) taskMap.get("attribute["+ATTRIBUTE_REVIEW_TASK+"]");
        String routeOwner = (String) taskMap.get("from["+RELATIONSHIP_ROUTE_TASK+"].to.owner");
        String taskOwner = (String) taskMap.get("owner");
        String comments = (String) taskMap.get("attribute[" + DomainObject.ATTRIBUTE_COMMENTS + "]");
		String status = (String) taskMap.get("attribute[" + DomainObject.ATTRIBUTE_APPROVAL_STATUS + "]");
        
        String STATE_ASSIGNED = PropertyUtil.getSchemaProperty(context, "policy", POLICY_INBOX_TASK, "state_Assigned");
        String STATE_REVIEW = PropertyUtil.getSchemaProperty(context, "policy", POLICY_INBOX_TASK, "state_Review");
        String current = (String) taskMap.get("current");

        //the Task must be promoted after review only if the user is the Route owner
        if(STATE_REVIEW.equals(current) && reviewTask.equalsIgnoreCase("Yes") && !context.getUser().equals(routeOwner))
        {
            //show MQL error message
            String msg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.InboxTaskJPO.triggerCheckPromoteOnReviewState.CannotPromoteRouteInReviewState", context.getLocale());
            MqlUtil.mqlCommand(context, "notice '" + msg + "'");
            return 1;
        }else if(!"Notify Only".equals((String) taskMap.get(strAttrRouteAction)) && STATE_ASSIGNED.equals(current)){
        	if(!(context.getUser().equals(taskOwner) || context.getUser().equals("User Agent"))){
        		String msg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.InboxTaskJPO.triggerCheckPromoteOnAssignedState.CannotPromoteRouteTaskState", context.getLocale());
        		MqlUtil.mqlCommand(context, "notice '" + msg + "'");
        		return 1;
       	   }else if (UIUtil.isNullOrEmpty(comments)
					&& ("true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context, "emxComponents.Routes.ShowCommentsForTaskApproval")) || "Reject"
							.equalsIgnoreCase(status))) {
				String msg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.FormComponent.MustEnterAValidValueFor",
						context.getLocale());
       		   msg +=" "+EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Comments", context.getLocale());
               
       		   MqlUtil.mqlCommand(context, "notice '" + msg + "'");
               return 1;
           }
        }
        return 0;
    }
  /**
     * getAllTasks - gets the list of All Tasks the user has access
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     */
   public Object getAllTasks(Context context, String[] args) throws Exception
   {

        return getTasks(context,"");
   }
  /**
     * getActiveTasks - gets the list of Tasks in Assigned State
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getActiveTasks(Context context, String[] args) throws Exception
   {

        String stateInboxTaskAssigned = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_INBOX_TASK,"state_Assigned");
        String stateWorkFlowTaskAssigned = PropertyUtil.getSchemaProperty(context,"policy", policyWorkflowTask, "state_Assigned");
        String stateTaskAssign = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Assign");
        String stateTaskActive = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Active");
        String stateTaskReview = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Review");

         //commented for Bug NO:338177
        /* String WBSWhereExp = "(type == 'Task'";
        if(stateTaskReview == null || "".equals(stateTaskReview) || "null".equals(stateTaskReview))
        {
          WBSWhereExp = WBSWhereExp+")";
        } else {
          WBSWhereExp = WBSWhereExp +" && current == " + stateTaskReview + ")";
        }*/
        StringBuffer sbf=new StringBuffer();
        if(stateInboxTaskAssigned != null && !"".equals(stateInboxTaskAssigned))
        {
          sbf.append("(current == "+stateInboxTaskAssigned);
          sbf.append(" && " + "from[" + RELATIONSHIP_ROUTE_TASK + "].to.attribute[" + ATTRIBUTE_ROUTE_STATUS + "] != \"Stopped\") ");
        }
        if(stateWorkFlowTaskAssigned!=null &&!"".equals(stateWorkFlowTaskAssigned))
        {
            if(sbf.length()!=0) {
              sbf.append(" || (");
            }
            sbf.append("type == \"" + sTypeWorkflowTask + "\" && ");
            sbf.append("current == "+stateWorkFlowTaskAssigned + ")");
         }
        if( stateTaskAssign!=null &&!"".equals( stateTaskAssign))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskAssign);
        }
        if( stateTaskActive!=null &&!"".equals( stateTaskActive))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskActive);
        }
        if(stateTaskReview!=null &&!"".equals( stateTaskReview))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskReview);
        }
       // commented for Bug NO:338177
        /*  if(  WBSWhereExp!=null &&!"".equals(  WBSWhereExp))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append(WBSWhereExp);
        }*/
        return getTasks(context,sbf.toString()) ;
   }

  /**
     * getCompletedTasks - gets the list of Tasks in Complete State
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getCompletedTasks(Context context, String[] args) throws Exception
   {

        String stateInboxTaskComplete = PropertyUtil.getSchemaProperty(context,"policy", DomainObject.POLICY_INBOX_TASK,"state_Complete");
    String stateWorkFlowTaskComplete = PropertyUtil.getSchemaProperty(context,"policy", policyWorkflowTask, "state_Completed");
    String stateTaskComplete = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Complete");
    //added for the 325218
    StringBuffer sbf=new StringBuffer();
    if(stateInboxTaskComplete !=null && !"".equals(stateInboxTaskComplete))
      sbf.append("  current == "+ stateInboxTaskComplete);
    if(stateWorkFlowTaskComplete!=null &&!"".equals(stateWorkFlowTaskComplete))
     {
      if(sbf.length()!=0)
        sbf.append(" || ");
      sbf.append("current == "+stateWorkFlowTaskComplete);
     }
    if(stateTaskComplete!=null&&!"".equals(stateTaskComplete))
     {
      if(sbf.length()!=0)
        sbf.append(" || ");
        sbf.append("current == "+stateTaskComplete);
     }

       return getTasks(context,sbf.toString());
    //till here
   }
  /**
     * getTasks - gets the list of Tasks depending on condition
     * @param context the eMatrix <code>Context</code> object
     * @param busWhere condition to query
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     */
   public Object getTasks(Context context, String busWhere ) throws Exception
   {

        try
        {
      long start=System.currentTimeMillis();
            DomainObject taskObject = DomainObject.newInstance(context);
            DomainObject boPerson     = PersonUtil.getPersonObject(context);
            String stateInboxTaskReview = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_INBOX_TASK,"state_Review");
            StringList selectTypeStmts = new StringList();
            StringList selectRelStmts  = new StringList();
      //Added for Bug No 338177 Begin
            StringList selectTypeStmtId = new StringList();
            selectTypeStmtId.add(taskObject.SELECT_ID);
            //Added for Bug No 338177 End
            selectTypeStmts.add(taskObject.SELECT_NAME);
            selectTypeStmts.add(taskObject.SELECT_ID);
            selectTypeStmts.add(taskObject.SELECT_DESCRIPTION);
            selectTypeStmts.add(taskObject.SELECT_OWNER);
            selectTypeStmts.add(taskObject.SELECT_CURRENT);
            selectTypeStmts.add(strAttrRouteAction);
            selectTypeStmts.add(strAttrCompletionDate);
            selectTypeStmts.add(strAttrTaskCompletionDate);
            selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
            selectTypeStmts.add(strAttrTitle);
            selectTypeStmts.add(objectIdSelectStr);
            selectTypeStmts.add(objectNameSelectStr);
            selectTypeStmts.add(routeIdSelectStr);
            selectTypeStmts.add(routeNameSelectStr);
            selectTypeStmts.add(routeOwnerSelectStr);

            selectTypeStmts.add(taskObject.SELECT_TYPE);
            selectTypeStmts.add(routeTypeSelectStr);
            selectTypeStmts.add(workflowIdSelectStr);
            selectTypeStmts.add(workflowNameSelectStr);
            selectTypeStmts.add(workflowTypeSelectStr);
            selectTypeStmts.add(strAttrworkFlowDueDate);
            selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
            selectTypeStmts.add(strAttrworkFlowCompletinDate);
            selectTypeStmts.add(strAttrTaskFinishDate);
            selectTypeStmts.add(TASK_PROJECT_ID);
            selectTypeStmts.add(TASK_PROJECT_NAME);
            selectTypeStmts.add(TASK_PROJECT_TYPE);

            /*  selectTypeStmts.add(Route.SELECT_APPROVAL_STATUS);*/
            String sPersonId = boPerson.getObjectId();

            Pattern relPattern = new Pattern(sRelProjectTask);
            relPattern.addPattern(sRelAssignedTask);
            relPattern.addPattern(sRelWorkflowTaskAssinee);

            Pattern typePattern = new Pattern(sTypeInboxTask);
            typePattern.addPattern(DomainObject.TYPE_TASK);
            typePattern.addPattern(sTypeWorkflowTask);
			typePattern.addPattern(DomainObject.TYPE_CHANGE_TASK);

            SelectList selectStmts = new SelectList();
            taskObject.setId(sPersonId);
           // get the list of tasks that needs owner review
       //Added for bug 352071
           String strResult = MqlUtil.mqlCommand(context,"temp query bus '"+sTypeInboxTask+"' * * where 'attribute["+sAttrReviewCommentsNeeded+"]==Yes && current=="+stateInboxTaskReview+" && from["+sRelRouteTask+"].to.owner==\""+context.getUser()+"\"' select id dump |");
       //end of bug 352071
            //Added for Bug No 338177 Begin
      com.matrixone.apps.domain.util.MapList taskMapList =  taskObject.getRelatedObjects(context,
                                                                                relPattern.getPattern(),
                                                                                typePattern.getPattern(),
                                                                                selectTypeStmtId,
                                                                                selectRelStmts,
                                                                                true,
                                                                                true,
                                                                                (short)2,
                                                                                busWhere,
                                                                                null,
                                                                                null,
                                                                                null,
                                                                                null);

       //Added for bug 352071
      if(strResult!=null && !"".equals(strResult))
            {
                String taskInbox = "";
                StringList strlResult = new StringList();
                String strTemp = "";
                StringList taskIds =FrameworkUtil.split(strResult,"\n");
                Iterator taskIdIterator=taskIds.iterator();
                while(taskIdIterator.hasNext())
                {
                	Map tempMap= new HashMap();
                    taskInbox=(String)taskIdIterator.next();
                    strlResult = FrameworkUtil.split(taskInbox,"|");
                    strTemp=(String)strlResult.get(3);
                    tempMap.put("id",strTemp);
                    taskMapList.add(tempMap);
                }
            }
       //end for bug 352071
            String[] objectIds=new String[taskMapList.size()];
            Iterator idsIterator=taskMapList.iterator();

            for(int i=0;idsIterator.hasNext();i++){
                Map map=(Map)idsIterator.next();
                objectIds[i]=(String)map.get("id");
            }
            taskMapList=DomainObject.getInfo(context,objectIds,selectTypeStmts);
            //Added for Bug No 338177 End


            // Added for 318463
            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            MQLCommand mql = new MQLCommand();
            String sTaskType = "";
            String sTaskId = "";
            String sMql = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            MapList finalTaskMapList = new MapList();
           Iterator objectListItr = taskMapList.iterator();

            while(objectListItr.hasNext())
            {

                Map objectMap = (Map) objectListItr.next();
                sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
                // if Task is WBS then add the context (top) object information
                if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTaskType))
                {

                    sTaskId = (String)objectMap.get(taskObject.SELECT_ID);
                    sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                    bResult = mql.executeCommand(context, sMql);
                    if(bResult) {

                        sResult = mql.getResult().trim();
                        //Bug 318325. Added if condition to check sResult object as not null and not empty.
                        if(sResult!=null && !"".equals(sResult)) {

                            sResultTkz = new StringTokenizer(sResult,"|");
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                            objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                        }
                    }
                }
                finalTaskMapList.add(objectMap);

            }
      long end=System.currentTimeMillis();
            return finalTaskMapList;
        }
        catch (Exception ex)
        {

            System.out.println("Error in getTasks = " + ex.getMessage());
            throw ex;
        }
   }
     /**
     * getMyDeskTasks - gets the list of Tasks the user has access
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getMyDeskTasks(Context context, String[] args)
        throws Exception
    {

        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            DomainObject taskObject = DomainObject.newInstance(context);
            DomainObject boPerson     = PersonUtil.getPersonObject(context);
            StringList selectTypeStmts = new StringList();
            StringList selectRelStmts  = new StringList();
            selectTypeStmts.add(taskObject.SELECT_NAME);
            selectTypeStmts.add(taskObject.SELECT_ID);
            selectTypeStmts.add(taskObject.SELECT_DESCRIPTION);
            selectTypeStmts.add(taskObject.SELECT_OWNER);
            selectTypeStmts.add(taskObject.SELECT_CURRENT);
            selectTypeStmts.add(strAttrRouteAction);
            selectTypeStmts.add(strAttrCompletionDate);
            selectTypeStmts.add(strAttrTaskCompletionDate);
            selectTypeStmts.add(strAttrTaskApprovalStatus);
            selectTypeStmts.add(getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_ACTION));
            selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
            selectTypeStmts.add(strAttrTitle);
            selectTypeStmts.add(objectIdSelectStr);
            selectTypeStmts.add(objectNameSelectStr);
            selectTypeStmts.add(routeIdSelectStr);
            selectTypeStmts.add(routeNameSelectStr);
            selectTypeStmts.add(routeOwnerSelectStr);
            selectTypeStmts.add(routeApprovalStatusSelectStr);

            selectTypeStmts.add(taskObject.SELECT_TYPE);
            selectTypeStmts.add(routeTypeSelectStr);
            selectTypeStmts.add(workflowIdSelectStr);
            selectTypeStmts.add(workflowNameSelectStr);
            selectTypeStmts.add(workflowTypeSelectStr);
            selectTypeStmts.add(strAttrworkFlowDueDate);
            selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
            selectTypeStmts.add(strAttrworkFlowCompletinDate);
            selectTypeStmts.add(strAttrTaskFinishDate);
            selectTypeStmts.add(TASK_PROJECT_ID);
            selectTypeStmts.add(TASK_PROJECT_TYPE);
            selectTypeStmts.add(TASK_PROJECT_NAME);
            /*  selectTypeStmts.add(Route.SELECT_APPROVAL_STATUS);*/
            String sPersonId = boPerson.getObjectId();



            Pattern relPattern = new Pattern(sRelProjectTask);
            relPattern.addPattern(sRelAssignedTask);
            relPattern.addPattern(sRelWorkflowTaskAssinee);

            Pattern typePattern = new Pattern(sTypeInboxTask);
            typePattern.addPattern(DomainObject.TYPE_TASK);
            typePattern.addPattern(sTypeWorkflowTask);
            typePattern.addPattern(DomainObject.TYPE_CHANGE_TASK);

            SelectList selectStmts = new SelectList();
            taskObject.setId(sPersonId);
            String busWhere = null;

            ContextUtil.startTransaction(context,false);
            ExpansionIterator expItr = taskObject.getExpansionIterator(context,
                                                                       relPattern.getPattern(),
                                                                       typePattern.getPattern(),
                                                                       selectTypeStmts,
                                                                       selectRelStmts,
                                                                       true,
                                                                       true,
                                                                       (short)2,
                                                                       busWhere,
                                                                       null,
                                                                       (short)0,
                                                                       false,
                                                                       false,
                                                                       (short)100,
                                                                       false);

            com.matrixone.apps.domain.util.MapList taskMapList = null;
            try {
                taskMapList = FrameworkUtil.toMapList(expItr,(short)0,null,null,null,null);
            } finally {
                expItr.close();
            }
            ContextUtil.commitTransaction(context);

            // Added for 318463
            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            MQLCommand mql = new MQLCommand();
            String sTaskType = "";
            String sTaskId = "";
            String sMql = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            MapList finalTaskMapList = new MapList();
            Iterator objectListItr = taskMapList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
                // if Task is WBS then add the context (top) object information
                if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTaskType))
                {
                    sTaskId = (String)objectMap.get(taskObject.SELECT_ID);
                    sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                    bResult = mql.executeCommand(context, sMql);
                    if(bResult) {
                        sResult = mql.getResult().trim();
                        //Bug 318325. Added if condition to check sResult object as not null and not empty.
                        if(sResult!=null && !"".equals(sResult)) {
                            sResultTkz = new StringTokenizer(sResult,"|");
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                            objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                        }
                    }
                }
                finalTaskMapList.add(objectMap);
            }

            return finalTaskMapList;
        }

        catch (Exception ex)
        {
           System.out.println("Error in getMyDeskTasks = " + ex.getMessage());
            throw ex;
        }
  }


    /**
     * getTasksToBeAccepted - gets the list of Tasks assigned to any of the person assignments
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getTasksToBeAccepted(Context context, String[] args)
          throws Exception
      {
        MapList taskMapList = new MapList();
        try
        {
           final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");

           StringList selectTypeStmts = new StringList();
           selectTypeStmts.add(SELECT_NAME);
           selectTypeStmts.add(SELECT_ID);
           selectTypeStmts.add(SELECT_DESCRIPTION);
           selectTypeStmts.add(SELECT_OWNER);
           selectTypeStmts.add(SELECT_CURRENT);
           selectTypeStmts.add(strAttrRouteAction);
           selectTypeStmts.add(strAttrCompletionDate);
           selectTypeStmts.add(strAttrTaskCompletionDate);
           selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
           selectTypeStmts.add(strAttrTitle);
           selectTypeStmts.add(objectIdSelectStr);
           selectTypeStmts.add(objectNameSelectStr);
           selectTypeStmts.add(routeIdSelectStr);
           selectTypeStmts.add(routeNameSelectStr);
           selectTypeStmts.add(routeOwnerSelectStr);

            selectTypeStmts.add(SELECT_TYPE);
            selectTypeStmts.add(routeTypeSelectStr);
            selectTypeStmts.add(workflowIdSelectStr);
            selectTypeStmts.add(workflowNameSelectStr);
            selectTypeStmts.add(workflowTypeSelectStr);
            selectTypeStmts.add(strAttrworkFlowDueDate);
            selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
            selectTypeStmts.add(strAttrworkFlowCompletinDate);
            selectTypeStmts.add(strAttrTaskFinishDate);
            selectTypeStmts.add(TASK_PROJECT_ID);
            selectTypeStmts.add(TASK_PROJECT_NAME);
            selectTypeStmts.add(TASK_PROJECT_TYPE);

           String strPersonAssignments = "";
           Vector groupAssignments = new Vector();
           Vector personAssignments = PersonUtil.getAssignments(context);
           personAssignments.remove(context.getUser());

           Iterator assignmentsItr = personAssignments.iterator();
           //Begin : Bug 346478
           Role roleObj = null;
           Group groupObj = null;
           StringList slParents = new StringList();
           StringList slParentRolesOrGroups = new StringList();
           //End : Bug 346478
           while(assignmentsItr.hasNext())
           {
               String assignment = (String)assignmentsItr.next();
               //Added the below lines of code for the bug 344483, to handle persons under a group and role
               /*String cmd = MqlUtil.mqlCommand(context, "print user \"" + assignment + "\" select isagroup isarole dump |");
               boolean isGroup = "TRUE|FALSE".equalsIgnoreCase(cmd);
               boolean isRole = "FALSE|TRUE".equalsIgnoreCase(cmd);
               if(isGroup || isRole)
               {
                   if(isGroup)
                   {
                       groupAssignments = new Group(assignment).getAssignments(context);
                   }
                   else
                   {
                       groupAssignments = new Role(assignment).getAssignments(context);
                   }
                   Iterator assignmentsItrr = groupAssignments.iterator();
                   while(assignmentsItrr.hasNext())
                   {
                        String grpAssignment = ((matrix.db.Person)assignmentsItrr.next()).getName();
                        strPersonAssignments +=  grpAssignment + ",";
                   }
               }*/
               //End of code for the bug 344483
               //strPersonAssignments += assignment + ",";

               //Begin : Bug 346478 code modification
               // Is it role?
               try {
                   roleObj = new Role(assignment);
                   roleObj.open(context);
                   // Find all its parents
                   slParents = roleObj.getParents(context, true);
                   if (slParents != null) {
                       slParentRolesOrGroups.addAll(slParents);
                   }
                   roleObj.close(context);
               } catch (MatrixException me){
                   // Is it group?
                   try {
                       groupObj = new Group(assignment);
                       groupObj.open(context);

                       // Find all its parents
                       slParents = groupObj.getParents(context, true);
                       if (slParents != null) {
                           slParentRolesOrGroups.addAll(slParents);
                       }

                       groupObj.close(context);
                   }
                   catch (MatrixException me2){
                       // This is neither role nor group, must be person
                   }
               }
               //End : Bug 346478 code modification

           }
           //Remove the last ","
           //strPersonAssignments = strPersonAssignments.substring(0,(strPersonAssignments.length())-1);

           // Begin : Bug 346478 code modification
           slParentRolesOrGroups.addAll(personAssignments);
           strPersonAssignments = FrameworkUtil.join(slParentRolesOrGroups,",");
           // End : Bug 346478 code modification
           StringBuffer objWhere = new StringBuffer();
           objWhere.append(DomainObject.SELECT_OWNER + " matchlist " + "\"" + strPersonAssignments + "\" \",\"");

           // Bug 346478 : The "Notify Only" tasks whether assigned to role/group/person, are auto completed. If they are assigned to role/group then after completion
           // they should not be visible in the Tasks to be Accepted list.
           objWhere.append(" && current!=\"" + POLICY_INBOX_TASK_STATE_COMPLETE + "\"");


            Pattern typePattern = new Pattern(TYPE_INBOX_TASK);
            typePattern.addPattern(TYPE_TASK);
            //typePattern.addPattern(sTypeWorkflowTask);// For Bug 346478, we shall find the WF tasks later

            taskMapList = DomainObject.findObjects(context,
                                                 typePattern.getPattern(),
                                                 null,
                                                 objWhere.toString(),
                                                 selectTypeStmts);

            // Removing those 'Inbox Tasks' that satisfy the following criteria
            // 1) The connected Route has a Route Template that has 'Owning Organization' relationship &
            // 2) The context user is not a member of that Organization
// IR-043921V6R2011 - Changes START
            StringList slInboxTasks = new StringList( taskMapList.size() );
            for( Iterator mlItr = taskMapList.iterator(); mlItr.hasNext(); ) {
                Map mTask = (Map) mlItr.next();
                if( TYPE_INBOX_TASK.equals( (String) mTask.get( SELECT_TYPE ) ) ) {
                    slInboxTasks.addElement( (String) mTask.get( SELECT_ID ) );
                }
            }

            StringList busSelects = new StringList(2);
            busSelects.addElement( SELECT_ID );
            busSelects.addElement( SELECT_TYPE );

            DomainObject doPerson = PersonUtil.getPersonObject(context);
            MapList mlOrganizations = doPerson.getRelatedObjects( context, RELATIONSHIP_MEMBER, TYPE_ORGANIZATION,
                busSelects, new StringList( SELECT_RELATIONSHIP_ID ), true, false, (short) 1, "", "", 0 );

            StringList slMember = new StringList( mlOrganizations.size() );
            for( Iterator mlItr = mlOrganizations.iterator(); mlItr.hasNext(); ) {
                Map mOrg = (Map) mlItr.next();
                slMember.addElement( (String) mOrg.get( SELECT_ID ) );
            }

            busSelects.addElement( SELECT_TEMPLATE_OWNING_ORG_ID );
            MapList mlIboxTasksInfo = DomainObject.getInfo(context, (String[])slInboxTasks.toArray(new String[slInboxTasks.size()]), busSelects );
            StringList slToRemoveTask = new StringList( mlIboxTasksInfo.size() );
            for( Iterator mlItr = mlIboxTasksInfo.iterator(); mlItr.hasNext(); ) {
                Map mTask = (Map) mlItr.next();
                String sOrgId = (String) mTask.get( SELECT_TEMPLATE_OWNING_ORG_ID );
                if( sOrgId !=null && !"null".equals( sOrgId ) && !"".equals( sOrgId ) && !(slMember.contains( sOrgId ))) {
                    slToRemoveTask.addElement( (String) mTask.get( SELECT_ID ) );
                }
            }
            for( Iterator mlItr = taskMapList.iterator(); mlItr.hasNext(); ) {
                Map mTask = (Map) mlItr.next();
                if( slToRemoveTask.contains( (String) mTask.get( SELECT_ID ))) {
                    mlItr.remove();
                }
            }
// IR-043921V6R2011 - Changes END


            // Added for 318463
            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            MQLCommand mql = new MQLCommand();
            String sTaskType = "";
            String sTaskId = "";
            String sMql = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            MapList finalTaskMapList = new MapList();
            Iterator objectListItr = taskMapList.iterator();
            while(objectListItr.hasNext())
            {
              Map objectMap = (Map) objectListItr.next();
              sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
              // if Task is WBS then add the context (top) object information
              if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTaskType))
              {
                  sTaskId = (String)objectMap.get(DomainObject.SELECT_ID);
                  sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                  bResult = mql.executeCommand(context, sMql);
                  if(bResult) {
                      sResult = mql.getResult().trim();
                      //Bug 318325. Added if condition to check sResult object as not null and not empty.
                      if(sResult!=null && !"".equals(sResult)) {
                          sResultTkz = new StringTokenizer(sResult,"|");
                          sResultTkz.nextToken();
                          sResultTkz.nextToken();
                          sResultTkz.nextToken();
                          objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                          objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                          sResultTkz.nextToken();
                          objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                      }
                  }
              }
              finalTaskMapList.add(objectMap);
            }

            //Begin : Bug 346478 code modification
            // The Workflow Task objects are not having any infomration in them to know if they are assigned to any role or group.
            // The owner for the tasks is the Workflow owner, the assignee is not set and state Started. Therefore, we shall find
            // all the Workflow Task objects in Started state and then find out the assignee for these tasks from their activities
            // These assignees will be either role or group, we shall check if the context user has these role/group.
            final String POLICY_WORKFLOW_TASK = PropertyUtil.getSchemaProperty(context, "policy_WorkflowTask");
            final String POLICY_WORKFLOW_TASK_STATE_STARTED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_WORKFLOW_TASK, "state_Started");
            final String ATTRIBUTE_ACTIVITY = PropertyUtil.getSchemaProperty(context, "attribute_Activity");
            final String ATTRIBUTE_PROCESS = PropertyUtil.getSchemaProperty(context, "attribute_Process");
            final String SELECT_ATTRIBUTE_ACTIVITY = "attribute[" + ATTRIBUTE_ACTIVITY + "]";
            final String SELECT_WORKFLOW_PROCESS_NAME = "to["+sRelWorkflowTask+"].from.attribute[" + ATTRIBUTE_PROCESS + "]";

            selectTypeStmts.add(SELECT_ATTRIBUTE_ACTIVITY);
            selectTypeStmts.add(SELECT_WORKFLOW_PROCESS_NAME);

            typePattern = new Pattern(sTypeWorkflowTask);

            taskMapList = DomainObject.findObjects(context,
                                                    typePattern.getPattern(),
                                                    null,
                                                    "current==\"" + POLICY_WORKFLOW_TASK_STATE_STARTED + "\"",
                                                    selectTypeStmts);
            Map mapTaskInfo = null;
            String strProcessName = null;
            String strActivityName = null;
            String strResult = null;
            String strAssigneeName = null;
            StringList slActivityAssignees = new StringList();
            for (Iterator itrTasks = taskMapList.iterator(); itrTasks.hasNext();) {
                mapTaskInfo = (Map) itrTasks.next();

                strProcessName = (String)mapTaskInfo.get(SELECT_WORKFLOW_PROCESS_NAME);
                strActivityName = (String)mapTaskInfo.get(SELECT_ATTRIBUTE_ACTIVITY);

                // Get assignee for the activity
                strResult = MqlUtil.mqlCommand(context, "print process \"" + strProcessName + "\" select interactive[" + strActivityName + "].assignee dump \"|\"", true);
                slActivityAssignees = FrameworkUtil.split(strResult, "|");

                for (Iterator itrAssignees = slActivityAssignees.iterator(); itrAssignees.hasNext();) {
                    strAssigneeName = (String) itrAssignees.next();
                    if (slParentRolesOrGroups.contains(strAssigneeName)) {
                        finalTaskMapList.add(mapTaskInfo);
                        break;
                    }
                }
            }
            //End : Bug 346478 code modification

            return finalTaskMapList;

      }
      catch(Exception e)
      {
          throw new FrameworkException(e.getMessage());
      }
    }


   /**
     * showRoute - Retrives the Tasks parent objects
     * Inbox Task - Route
     * Workflow Task - Workflow
     * Task - Project Space
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showRoute(Context context, String[] args) throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");

            Map objectMap = null;
            Vector showRoute = new Vector();
            String statusImageString = "";
            String sRouteString = "";
            boolean isPrinterFriendly = false;
            String strPrinterFriendly = (String)paramMap.get("reportFormat");
            String languageStr = (String)paramMap.get("languageStr");

            String sAccDenied = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.AccessDenied");

            if (strPrinterFriendly != null )
            {
                isPrinterFriendly = true;
            }

            int objectListSize = 0 ;
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }
            for(int i=0; i< objectListSize; i++)
            {
                statusImageString = "";
                sRouteString = "";
                try
                {
                    objectMap = (HashMap) objectList.get(i);
                }
                catch(ClassCastException cce)
                {
                    objectMap = (Hashtable) objectList.get(i);
                }

                String sTypeName = (String) objectMap.get(DomainObject.SELECT_TYPE);
                String sObjectId = "";
                String sObjectName = "";

                if (TYPE_INBOX_TASK.equalsIgnoreCase(sTypeName))
                {
                    sObjectId   =(String)objectMap.get(routeIdSelectStr);
                    sObjectName = (String)objectMap.get(routeNameSelectStr);
                }
                else if (TYPE_TASK.equalsIgnoreCase(sTypeName))
                {
                    //Bug 318463. Commented below two lines and added 2 new lines to read id and name from main list.
                    sObjectId   =(String)objectMap.get("Context Object Id");
                    sObjectName = (String)objectMap.get("Context Object Name");
                }
                else if (sTypeWorkflowTask.equalsIgnoreCase(sTypeName))
                {
                    sObjectId   =(String)objectMap.get(workflowIdSelectStr);
                    sObjectName = (String)objectMap.get(workflowNameSelectStr);
                }

                //Bug 318325. If object id and Name are null don't show context object.
                if(sObjectId != null && sObjectName != null )
                {
                    String sRouteNextUrl = "./emxTree.jsp?objectId=" + XSSUtil.encodeForJavaScript(context, sObjectId);
                    //String sRouteUrl  = "javascript:showNonModalDialog('" + sRouteNextUrl + "',800,575)";
                    // Changed for bug 346533
                    String sRouteUrl  = "javascript:emxTableColumnLinkClick('" + sRouteNextUrl + "','800','575',false,'popup','')";


                    if(!isPrinterFriendly)
                    {
                        // Added for the 341122
                        sRouteString = "<a  href=\""+sRouteUrl+"\">"+XSSUtil.encodeForHTML(context,sObjectName)+"</a>&#160;";
                    }
                    else
                    {
                        sRouteString = sObjectName;
                    }
                    showRoute.add(sRouteString);
                }
                else
                {
                    showRoute.add(sAccDenied);
                }
            }

            return showRoute;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showRoute= " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * showType - shows the type of the task
     * Inbox Task - Route Action attribute value
     * Workflow Task - Activity
     * Task - WBS Task
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showType(Context context, String[] args) throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");
            String languageStr = (String)paramMap.get("languageStr");
            Vector vShowType = new Vector();
            Map objectMap = null;

            int objectListSize = 0 ;
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }

            for(int i=0; i< objectListSize; i++)
            {
                try
                {
                    objectMap = (HashMap) objectList.get(i);
                }
                catch(ClassCastException cce)
                {
                    objectMap = (Hashtable) objectList.get(i);
                }

                String sTypeName = (String) objectMap.get(DomainObject.SELECT_TYPE);
                String sTypeString = "";

                if (TYPE_INBOX_TASK.equalsIgnoreCase(sTypeName))
                {
                    sTypeString = (String) objectMap.get(strAttrRouteAction);
                    if (sTypeString == null)
                    {
                        sTypeString = "";
                    }
                    else
                    {
                        sTypeString =  EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", new Locale(languageStr),"emxFramework.Range.Route_Action."+sTypeString.replace(' ','_'));
                    }
                }
                else if (TYPE_TASK.equalsIgnoreCase(sTypeName))
                {
                    sTypeString = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Route.Type."+sTypeName.replace(' ','_'));
                }
                else if (sTypeWorkflowTask.equalsIgnoreCase(sTypeName))
                {
                    sTypeString = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Route.Type."+sTypeName.replace(' ','_'));
                }
                else
                {
                    sTypeString = EnoviaResourceBundle.getTypeI18NString(context,sTypeName, languageStr);
                }
                if("CSV".equals(paramMap.get("reportFormat"))||"CSV".equals(paramMap.get("exportFormat")))
                	vShowType.add(sTypeString);
                else
                vShowType.add(XSSUtil.encodeForXML(context, sTypeString));
            }

            return vShowType;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showType= " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * showType - shows the task instructions
     * Inbox Task - Route Instructions
     * Workflow Task - Instructions
     * Task - Notes
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showInstructions(Context context, String[] args) throws Exception
    {

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");

            Vector vShowNotes = new Vector();
            Map objectMap = null;

            int objectListSize = 0 ;
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }

            for(int i=0; i< objectListSize; i++)
            {
                try
                {
                    objectMap = (HashMap) objectList.get(i);
                }
                catch(ClassCastException cce)
                {
                    objectMap = (Hashtable) objectList.get(i);
                }

                String sTypeName = (String) objectMap.get(DomainObject.SELECT_TYPE);
                String objectId = (String) objectMap.get(DomainObject.SELECT_ID);
                String sTypeNotes = "";

                DomainObject domObject = new DomainObject(objectId);

                if (TYPE_INBOX_TASK.equalsIgnoreCase(sTypeName))
                {
                    sTypeNotes = (String) domObject.getAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS);
                }
                else if (TYPE_TASK.equalsIgnoreCase(sTypeName))
                {
                    sTypeNotes = (String) domObject.getAttributeValue(context, DomainObject.ATTRIBUTE_NOTES);
                }

                else if (sTypeWorkflowTask.equalsIgnoreCase(sTypeName))
                {
                    sTypeNotes = (String) domObject.getAttributeValue(context, attrworkFlowInstructions);
                }
                if (sTypeNotes == null)
                {
                    sTypeNotes = "";
                }
                if("CSV".equals(paramMap.get("reportFormat"))||"CSV".equals(paramMap.get("exportFormat")))
                	vShowNotes.add(sTypeNotes);
                else
                vShowNotes.add(XSSUtil.encodeForXML(context,sTypeNotes));
            }

            return vShowNotes;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showInstructions= " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * showType - shows the due date for the task
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showDueDate(Context context, String[] args) throws Exception
    {
        Vector showDueDate = new Vector();
        boolean bDisplayTime = false;

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList      = (Map)programMap.get("paramList");
            String sLanguage = (String)paramList.get("languageStr");

            String dueDate   = "";
            String dueDateOffset   = "";
            String dueDateOffsetFrom   = "";
			String assigneeSetDueDate   = "";

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                dueDate   = "";

                Map objectMap = (Map) objectListItr.next();
                String taskDueDate = "";
                String sTypeName = (String)objectMap.get(DomainObject.SELECT_TYPE);

                if ((DomainObject.TYPE_INBOX_TASK).equalsIgnoreCase(sTypeName))
                {
                    taskDueDate = (String)objectMap.get(strAttrCompletionDate);
                }
                else if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTypeName))
                {
                    taskDueDate = (String)objectMap.get(strAttrTaskEstimatedFinishDate);
                }
                else if (sTypeWorkflowTask.equalsIgnoreCase(sTypeName))
                {
                    taskDueDate = (String)objectMap.get(strAttrworkFlowDueDate);
                }
                else if(DomainConstants.RELATIONSHIP_ROUTE_NODE.equals(sTypeName))
                {
                    StringBuffer sb = new StringBuffer();
                    taskDueDate = (String)objectMap.get(strAttrCompletionDate);
                    dueDateOffset = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_DUEDATE_OFFSET));
                    dueDateOffsetFrom = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM));
					assigneeSetDueDate = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE));
                    boolean bDueDateEmpty  = UIUtil.isNullOrEmpty(taskDueDate) ? true : false;
                    boolean bDeltaDueDate = (!UIUtil.isNullOrEmpty(dueDateOffset) && bDueDateEmpty) ? true : false;

                    if(UIUtil.isNotNullAndNotEmpty(assigneeSetDueDate) && "Yes".equalsIgnoreCase(assigneeSetDueDate)){
                 	   sb.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(sLanguage),"emxComponents.AssignTasksDialog.AssigneeDueDate"));
                    }else if(!bDeltaDueDate){
                        sb.append(taskDueDate).append(" ");
                    }else{

                        sb.append(dueDateOffset).append(" ").append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(sLanguage),"emxComponents.common.DaysFrom")).
                        append(" ").append(i18nNow.getRangeI18NString( DomainObject.ATTRIBUTE_DATE_OFFSET_FROM, dueDateOffsetFrom,sLanguage));
                    }
                    taskDueDate = sb.toString();
                }

                // Below date conversion is not required since config table column settings does the conversion
                /*if (taskDueDate != null && taskDueDate.length() > 0)
                {
                    DateFormat df = null;
                    if (bDisplayTime)
                    {
                         df = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, locale);
                    }
                    else
                    {
                        df = DateFormat.getDateInstance(iDateFormat, locale);
                    }

                    dueDate = df.format(eMatrixDateFormat.getJavaDate(taskDueDate));
                }
                showDueDate.add(dueDate);
                */
                Locale locale =context.getLocale();
                String timeZone = (String) (paramList != null ? paramList.get("timeZone") : programMap.get("timeZone"));
                double clientTZOffset   = (new Double(timeZone)).doubleValue();

                if(! UIUtil.isNullOrEmpty(taskDueDate)){
                	try {
                	taskDueDate =   eMatrixDateFormat.getFormattedDisplayDateTime(taskDueDate, clientTZOffset, locale);
                	} catch (Exception dateException){
                		//do nothing,This exception is added to avoid formatting of taskduedate if the value is not of type date  i.e for ex: 4 days after Route start Date
                		taskDueDate = taskDueDate;
                	}
                }
                showDueDate.add(taskDueDate);
            }
        }
        catch (Exception ex)
        {
            System.out.println("Error in showDueDate= " + ex.getMessage());
            throw ex;
        }
        return showDueDate;
    }

        /**
     * showStatusGif - gets the status gif to be shown in the column of the Task Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     * @modified V6R2014x for refactoring
     */
    public Vector showStatusGif(Context context, String[] args) throws Exception
    {
        try
        {
            Vector enableCheckbox = new Vector();

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            String stateComplete = FrameworkUtil.lookupStateName(context, policyTask, "state_Complete");
            String stateCompleted = FrameworkUtil.lookupStateName(context, policyWorkflowTask, "state_Completed");
            String stateAssigned    = FrameworkUtil.lookupStateName(context, DomainObject.POLICY_INBOX_TASK, "state_Assigned");

            Date dueDate   = null;
            Date curDate = new Date();
            String statusImageString = "";
            String statusColor= "";

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                //XSSOK
                enableCheckbox.add(getStatusImageForTasks(context, objectMap, "../"));
            }

            return enableCheckbox;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showStatusGif= " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * getScheduledCompletionDate - get the route scheduled completion date that needs to be displayed in the column of the Route Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector getScheduledCompletionDate(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Vector enableCheckbox = new Vector();
            String selectScheduledDate = "from["+ DomainObject.RELATIONSHIP_ROUTE_NODE + "].attribute["+ DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE +"]";

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String routeId = (String) objectMap.get("id");

                DomainObject routeObject = new DomainObject(routeId);
                StringList dateList = routeObject.getInfoList(context, selectScheduledDate);

                MapList dateMapList = new MapList();

                Iterator dateListItr = dateList.iterator();
                while(dateListItr.hasNext())
                {
                    String schDate = (String) dateListItr.next();
                    HashMap dateMap = new HashMap();
                    dateMap.put("date", schDate);
                    dateMapList.add(dateMap);
                }
                dateMapList.sort("date", "descending", "date");

                String displayDate = "";
                Iterator dateMapListItr = dateMapList.iterator();
                while(dateMapListItr.hasNext())
                {
                    Map tempMap = (Map) dateMapListItr.next();
                    displayDate = (String) tempMap.get("date");
                    break;
                }
                enableCheckbox.add(displayDate);
            }
            return enableCheckbox;
        }
        catch (Exception ex)
        {
            System.out.println("Error in getScheduledCompletionDate= " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * getActualCompletionDate - get the route actual completion date that needs to be displayed in the column of the Route Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showContent(Context context, String[] args) throws Exception
    {
        try
        {
            Vector contentCheckbox = new Vector();
            String compDate = "";

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

//          Begin : Bug 346997 code modification
            Map paramList = (Map)programMap.get("paramList");
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification

            DomainObject boRoute = DomainObject.newInstance(context);

            StringList objectSelects = new StringList();
            objectSelects.add(DomainConstants.SELECT_ID);

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                // Bug 317688: Show the deliverables of Workflow tasks
                String sRouteId = null;
                String sRelName = "";
                boolean toSide = true;
                boolean fromSide = true;
                Map objectMap = (Map) objectListItr.next();
                String objType = (String)objectMap.get(DomainObject.SELECT_TYPE);

                MapList Maplist = new MapList();

                // Bug 317688: Show the deliverables of Workflow tasks
                if (TYPE_INBOX_TASK.equals(objType))
                {
                    sRouteId   =(String)objectMap.get(routeIdSelectStr);
                    sRelName = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
                    fromSide = false;
                }
                else if (sTypeWorkflowTask.equals(objType) || TYPE_TASK.equals(objType))
                {
                    sRouteId   = (String)objectMap.get(DomainObject.SELECT_ID);
                    sRelName = sRelWorkflowTaskDeliverable;
                    toSide = false;
                }

                if (sRouteId != null && !"".equals(sRouteId))
                {
                    boRoute.setId(sRouteId);

                    Maplist = boRoute.getRelatedObjects(context,
                                     sRelName,
                                     "*",
                                     objectSelects,
                                     null,
                                     toSide,
                                     fromSide,
                                     (short)0,
                                     "",
                                     "");
                }

//              Begin : Bug 346997 code modification
                if (isExporting) {
                    if(Maplist.size() > 0 && (TYPE_INBOX_TASK.equals(objType) || sTypeWorkflowTask.equals(objType)))
                    {
                        compDate = String.valueOf(Maplist.size());
                    }
                    else
                    {
                        compDate = "";
                    }
                }
                else {
                    if(Maplist.size()>0 && TYPE_INBOX_TASK.equals(objType))
                    {
                        //compDate ="<a href=javascript:showNonModalDialog('../components/emxRouteContentSummaryFS.jsp?objectId="+sRouteId+"',575,575);>"+Maplist.size()+"</a>";

                        // Modified for bug 346533
                        compDate ="<a href=\"javascript:emxTableColumnLinkClick('../components/emxRouteContentSummaryFS.jsp?objectId="+sRouteId+"','575','575', false,'popup','')\">"+Maplist.size()+"</a>";
                    }
                    else if (Maplist.size()>0 && sTypeWorkflowTask.equals(objType))
                    {
                        //compDate ="<a href=javascript:showNonModalDialog('../common/emxTable.jsp?program=emxCommonDocumentUI:getDocuments&table=APPDocumentSummary&selection=multiple&sortColumnName=Name&sortDirection=ascending&toolbar=APPWorkflowDeliverableSummaryToolBar&suiteKey=Components&header=emxComponents.Workflow.TaskDeliverables&HelpMarker=emxhelpcontentsummary&parentRelName=relationship_TaskDeliverable&objectId="+sRouteId+"',575,575);>"+Maplist.size()+"</a>";

//                      Modified for bug 346533
                        compDate ="<a href=\"javascript:emxTableColumnLinkClick('../common/emxTable.jsp?program=emxCommonDocumentUI:getDocuments&table=APPDocumentSummary&selection=multiple&sortColumnName=Name&sortDirection=ascending&toolbar=APPWorkflowDeliverableSummaryToolBar&suiteKey=Components&header=emxComponents.Workflow.TaskDeliverables&HelpMarker=emxhelpcontentsummary&parentRelName=relationship_TaskDeliverable&objectId="+sRouteId+"','575','575', false, 'popup','')\">"+Maplist.size()+"</a>";
                    }
					else if(Maplist.size()>0 && TYPE_TASK.equals(objType)){
                    	compDate="<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?DefaultCategory=PMCDeliverableCommandPowerView&amp;objectId="+sRouteId+"', '575','575', false, 'popup', '', '', '', '')\">"+Maplist.size()+"</a>";
                    }
                    else
                    {
                        compDate = "";//(new Integer(Maplist.size())).toString();
                    }
                }
//              End : Bug 346997 code modification

                contentCheckbox.add(compDate);
            }

            return contentCheckbox;
        }
        catch (Exception ex)
        {
            System.out.println("Error in getActualCompletionDate= Lanka " + ex.getMessage());
            throw ex;
        }
    }



    /**
     * showOwner - displays the owner with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showWorkspace(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            boolean blnToDisplayWorkspaceLink = true;

//          Begin : Bug 346997 code modification
            Map paramList = (Map)programMap.get("paramList");
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification

            Vector vecShowWorkspace = new Vector();
            String showWorkspace="";
            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                blnToDisplayWorkspaceLink=true;
                Map objectMap = (Map) objectListItr.next();
                String sRelatedObjectId   = (String)objectMap.get(objectIdSelectStr);
                String sRelatedObjectName = (String)objectMap.get(objectNameSelectStr);
                String sRelatedObjectNextUrl =  "./emxTree.jsp?AppendParameters=true&amp;objectId=" + sRelatedObjectId;
                String sRelatedObjectUrl     = "javascript:showModalDialog('" + sRelatedObjectNextUrl + "',800,575)";
                if(sRelatedObjectId == null){
                  blnToDisplayWorkspaceLink = false;
                }

//              Begin : Bug 346997 code modification
                if (isExporting) {
                    if(!blnToDisplayWorkspaceLink) {
                        showWorkspace = "";
                    } else {
                        showWorkspace = XSSUtil.encodeForHTML(context, sRelatedObjectName);
                    }
                }
                else {
                    if(!blnToDisplayWorkspaceLink) {
                        showWorkspace="&#160;";
                    } else {
                        showWorkspace="<a href=\""+sRelatedObjectUrl+"\">"+XSSUtil.encodeForHTML(context, sRelatedObjectName)+"</a>";
                    }
                }
//              End : Bug 346997 code modification


                vecShowWorkspace.add(showWorkspace);
            }

            return vecShowWorkspace;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showWorkspace= " + ex.getMessage());
            throw ex;
        }
    }

/**
     * showTaskName - displays the owner with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showTaskName(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            DomainObject taskObject = new DomainObject();
            Vector vecShowTaskName  = new Vector();
            String name= "";
            String sTaskName  ="";
            String taskId  ="";


            String sTypePQP = PropertyUtil.getSchemaProperty(context,"type_PartQualityPlan");

            //Bug 318463. Start: Added below variables.
            Map paramList = (Map)programMap.get("paramList");

//          Begin : Bug 346997 code modification
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification

            StringBuffer prefixLinkBuffer = new StringBuffer();
            prefixLinkBuffer.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert&amp;taskname=");

            StringBuffer tempLinkBuffer = new StringBuffer();
            tempLinkBuffer.append("&amp;relId=");
            tempLinkBuffer.append((String)paramList.get("relId"));
            tempLinkBuffer.append("&amp;parentOID=");
            tempLinkBuffer.append((String)paramList.get("parentOID"));
            tempLinkBuffer.append("&amp;jsTreeID=");
            tempLinkBuffer.append((String)paramList.get("jsTreeID"));
            tempLinkBuffer.append("&amp;objectId=");
            String sContextType = "";
            //Bug 318463. End: Added above variables.

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                //modified for IR-050410V6R2011x
               // sTaskName  = (String)objectMap.get("strAttrTitle");
                sTaskName  = (String)objectMap.get("name");
                taskId  = (String)objectMap.get(DomainObject.SELECT_ID);
                //Bug 318463. Modified if condition and assigning title to name instead of adding it directly to vector.
                if(sTaskName!= null && !sTaskName.equals("")) {
                    name = sTaskName;
                }
                else
                {
                    taskObject.setId((String)objectMap.get(taskObject.SELECT_ID));
                    name=taskObject.getInfo(context,"name");
                }
                 name=XSSUtil.encodeForXML(context,name);
                //Begin- Bug 318463. Added below code to add final href string to vector.
                StringBuffer finalURL = new StringBuffer();
                   
//              Begin : Bug 346997 code modification
                if (isExporting) {
                    finalURL.append(name);
                }
                else {
                    finalURL.append(prefixLinkBuffer.toString());
                    finalURL.append(FrameworkUtil.findAndReplace(name, "'", "\\'"));
                    finalURL.append(tempLinkBuffer.toString());
                    finalURL.append(objectMap.get(taskObject.SELECT_ID));

                    sContextType  =  (String)objectMap.get("Context Object Type");
                    if(sContextType != null && sContextType.equals(sTypePQP)) {
                        finalURL.append("&amp;suiteKey=");
                        finalURL.append("SupplierCentral");
                        finalURL.append("&amp;emxSuiteDirectory=");
                        finalURL.append("suppliercentral");
                    } else {
                        finalURL.append("&amp;suiteKey=");
                        finalURL.append((String)paramList.get("suiteKey"));
                        finalURL.append("&amp;emxSuiteDirectory=");
                        finalURL.append((String)paramList.get("SuiteDirectory"));
                    }
                    finalURL.append("', '', '', 'false', 'content', '')\"  class=\"object\">");
                    finalURL.append("<img border=\"0\" src=\"images/iconSmallTask.gif\"></img>");
                    finalURL.append(name);
                    finalURL.append("</a>");
                }
//              End : Bug 346997 code modification

                vecShowTaskName.add(finalURL.toString());
                //End- Bug 318463.
            }
            return vecShowTaskName;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showTaskName= " + ex.getMessage());
            throw ex;
        }
    }

    /** Added for IR-050410V6R2011x
     * showTaskTitle - displays the Inbox Task Title
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */
    public Vector showTaskTitle(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            DomainObject taskObject = new DomainObject();
            Vector vecShowTaskTitle  = new Vector();
            String sTaskTitle  ="";

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                sTaskTitle  = (String)objectMap.get(strAttrTitle);
                sTaskTitle = UIUtil.isNullOrEmpty(sTaskTitle) ? EMPTY_STRING : sTaskTitle;
                vecShowTaskTitle.add(XSSUtil.encodeForHTML(context, sTaskTitle));
            }
            return vecShowTaskTitle;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showTaskTitle= " + ex.getMessage());
            throw ex;
        }
    }
    /**
     * showNewWindowIcon - displays the new window icon to display object in new window
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10-6 SP2
     * @grade 0
     */
    public Vector showNewWindowIcon(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Vector vecShowNewWindow  = new Vector();

            //Bug 318463. Start: Added below variables.
            Map paramList = (Map)programMap.get("paramList");

            String sTypePQP = PropertyUtil.getSchemaProperty(context,"type_PartQualityPlan");

            String languageStr = (String)paramList.get("languageStr");
            String sNewWindow  = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.OpenNew");

            StringBuffer prefixLinkBuffer = new StringBuffer();
            prefixLinkBuffer.append("<b><a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
            prefixLinkBuffer.append((String)paramList.get("relId"));
            prefixLinkBuffer.append("&amp;parentOID=");
            prefixLinkBuffer.append((String)paramList.get("parentOID"));
            prefixLinkBuffer.append("&amp;jsTreeID=");
            prefixLinkBuffer.append((String)paramList.get("jsTreeID"));
            prefixLinkBuffer.append("&amp;objectId=");
            String sContextType = "";
            //Bug 318463. End: Added above variables.

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String taskId = (String)objectMap.get(SELECT_ID);
                String taskName = (String)objectMap.get(SELECT_NAME);
                //Begin- Bug 318463. Added below code to add final href string to vector.
                StringBuffer finalURL = new StringBuffer();
                if(paramList.get("reportFormat") != null)
                {
                    finalURL.append("<img border=\"0\" src=\"images/iconNewWindow.gif\"></img>");
                } else if(DomainConstants.RELATIONSHIP_ROUTE_NODE.equals(taskName))
                {
                    finalURL.append("");
                } else {
                    finalURL.append(prefixLinkBuffer.toString());
                    finalURL.append(taskId);

                    sContextType  =  (String)objectMap.get("Context Object Type");
                    if(sContextType != null && sContextType.equals(sTypePQP)) {
                        finalURL.append("&amp;suiteKey=");
                        finalURL.append("SupplierCentral");
                        finalURL.append("&amp;emxSuiteDirectory=");
                        finalURL.append("suppliercentral");
                    } else {
                        finalURL.append("&amp;suiteKey=");
                        finalURL.append((String)paramList.get("suiteKey"));
                        finalURL.append("&amp;emxSuiteDirectory=");
                        finalURL.append((String)paramList.get("SuiteDirectory"));
                    }
                    finalURL.append("', '875', '550', 'false', 'popup', '')\">");
                    finalURL.append("<img border=\"0\" src=\"images/iconNewWindow.gif\" title=\""+XSSUtil.encodeForHTMLAttribute(context,sNewWindow)+"\"></img>");
                    finalURL.append("</a></b>");
                }
                vecShowNewWindow.add(finalURL.toString());
                //End- Bug 318463.
            }
            return vecShowNewWindow;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showNewWindowIcon= " + ex.getMessage());
            throw ex;
        }
    }

   /*  showCheckBox - displays the owner with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showCheckBox(Context context, String[] args)
        throws Exception
    {
        try
        {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            DomainObject taskObject = new DomainObject();
            Vector vecShowCheckBox  = new Vector();
      String taskId = "";
      String state= "";
      String sTaskTitle  ="";
            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
        taskId        = (String)objectMap.get(taskObject.SELECT_ID);
        state         = (String)objectMap.get(taskObject.SELECT_CURRENT);

        if(state.equals("Complete"))
        {

          vecShowCheckBox.add("false");
        }
        else
        {

          vecShowCheckBox.add("true");
        }

      }
            return vecShowCheckBox;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showCheckBox= " + ex.getMessage());
            throw ex;
        }
    }


 /* displayLinkAccessCheck - determines if the Create New, Create Route Wizard ,Set Task Escalation, Remove Selected, Start/ResumeRoute links needs to be show in the Route Summary table
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the objectId
  * @returns boolean type
  * @throws Exception if the operation fails
  * @since AEF Rossini
  * @grade 0
  */

  public static boolean showRemoveLink(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
     // DomainObject boRoute = DomainObject.newInstance(context);
       boolean result             = false;
     String sTypeName = "";
     String sOwner = "";
       String objectId    = (String) programMap.get("objectId");
       String sUser       = context.getUser();
       String sTypeRoute          = PropertyUtil.getSchemaProperty(context,"type_Route");

     /* System.out.println(objectId);
    if(objectId != null) {
       DomainObject boRoute = new DomainObject(objectId);
       boRoute.open(context);
       sTypeName = boRoute.getTypeName();
       sOwner = boRoute.getOwner().getName();
       boRoute.close(context);
      }

     if(sOwner.equals(sUser) && sTypeName.equals(sTypeRoute)) {
    result = true;
    }*/

      return result;
  }
  public String getInboxTaskMailMessage(Context context,Locale locale,String Bundle,String baseURL, String paramSuffix) throws Exception
  {
    StringBuffer msg = new StringBuffer();
    StringBuffer contentURL = new StringBuffer();
    try{

        lang = locale.getLanguage();

        rsBundle = Bundle;

        StringList selectstmts = new StringList();
        selectstmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
        selectstmts.add(strAttrCompletionDate);
        selectstmts.add(routeIdSelectStr);
        Map taskMap = getInfo(context,selectstmts);
        String routeId = (String)taskMap.get(routeIdSelectStr);
        Pattern relPattern = new Pattern(DomainObject.RELATIONSHIP_OBJECT_ROUTE);
        Pattern typePattern = new Pattern(DomainObject.TYPE_PART);
        StringList selectBusStmts    = new StringList();
         selectBusStmts.add(SELECT_ID);
         selectBusStmts.add(SELECT_TYPE);
         selectBusStmts.add(SELECT_NAME);
         selectBusStmts.add(SELECT_REVISION);
         StringList selectRelStmts    = new StringList();


         DomainObject route = DomainObject.newInstance(context,routeId);
        MapList contentMapList = route.getRelatedObjects(context,
                                                        relPattern.getPattern(),
                                                        typePattern.getPattern(),
                                                         selectBusStmts,
                                                         selectRelStmts,
                                                         true,
                                                         true,
                                                         (short)1,
                                                         "",
                                                         "",
                                                         null,
                                                         null,
                                                         null);

        int size=contentMapList.size();

        if(size > 0)
        {
            msg.append("\n"+getTranslatedMessage("emxFramework.InboxTask.MailNotification.WhereContent.Message"));
            contentURL.append("\n"+getTranslatedMessage("emxFramework.InboxTask.MailNotification.ContentFindMoreURL"));
            Map contentMap=null;
            for(int i=0;i<size;i++)
            {
                contentMap = (Map)contentMapList.get(i);
                msg.append(contentMap.get(SELECT_TYPE));
                msg.append(contentMap.get(SELECT_NAME));
                msg.append(contentMap.get(SELECT_REVISION));
                msg.append("\n");
                contentURL.append("\n" + baseURL + "?objectId=" +contentMap.get(SELECT_ID));
                if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
                    contentURL.append("&treeMenu=" + paramSuffix);
                }
            }
        }
        if(size <= 0)
            msg.append("\n");
        msg.append(getTranslatedMessage("emxFramework.InboxTask.MailNotification.TaskInstructions"));
        msg.append("\n");
        msg.append(taskMap.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]"));
        msg.append("\n");
        msg.append(getTranslatedMessage("emxFramework.InboxTask.MailNotification.TaskDueDate"));
        msg.append("\n");
        msg.append(taskMap.get(strAttrCompletionDate));
        msg.append("\n");
        msg.append(getTranslatedMessage("emxFramework.InboxTask.MailNotification.TaskFindMoreURL"));
        msg.append("\n" + baseURL + "?objectId=" + getObjectId());
        if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
            msg.append("&treeMenu=" + paramSuffix);
        }
        msg.append("\n");
        msg.append(getTranslatedMessage("emxFramework.InboxTask.MailNotification.RouteFindMoreURL"));
        msg.append("\n" + baseURL + "?objectId=" +routeId);
        if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
            msg.append("&treeMenu=" + paramSuffix);
        }
        msg.append(contentURL.toString());
    }catch(Exception ex){ System.out.println(" error  in getInboxTaskMailMessage "+ex);}
    return msg.toString();
  }
  public String getTranslatedMessage(String text) throws Exception
  {
        return (String)loc.GetString(rsBundle, lang, text);

  }

  /* getTaskContent - gets all the contents for the Task.Which will be used for
  *                   Displaying in the Task Content Summary
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the String array args
  * @returns Object type
  * @throws Exception if the operation fails
  * @since AEF Rossini
  * @grade 0
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getTaskContent(Context context,String []args) throws Exception
  {
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    String objectId    = (String) programMap.get("objectId");
    String selectStr = "from["+DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.id";
    DomainObject taskObject = DomainObject.newInstance(context,objectId);
    objectId = taskObject.getInfo(context,selectStr);
    // Due to Resume Process implementation there can be tasks which are not connected to route and hence we cannot find
    // the route id from these tasks. Then the route id can be found by first finding the latest revision of the task
    // and then querying for the route object.
    if (objectId == null) {
        DomainObject dmoLastRevision = new DomainObject(taskObject.getLastRevision(context));
        objectId = dmoLastRevision.getInfo(context, selectStr);
    }
    Route routeObject = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
    routeObject.setId(objectId);
    StringList selListObj = new SelectList(6);
    selListObj.add(routeObject.SELECT_NAME);
    selListObj.add(routeObject.SELECT_ID);
    selListObj.add(routeObject.SELECT_TYPE);
    selListObj.add(routeObject.SELECT_DESCRIPTION);
    selListObj.add(routeObject.SELECT_POLICY);
    selListObj.add(routeObject.SELECT_CURRENT);
	selListObj.add(routeObject.SELECT_FILE_NAME);
	selListObj.add(routeObject.SELECT_FILE_FORMAT);
    // build select params for Relationship
    StringList selListRel = new SelectList(3);
    selListRel.add(routeObject.SELECT_RELATIONSHIP_ID);
    selListRel.addElement(routeObject.SELECT_ROUTE_BASEPOLICY);
    selListRel.addElement(routeObject.SELECT_ROUTE_BASESTATE);
    MapList routableObjsList = routeObject.getConnectedObjects(context,
                                                       selListObj,
                                                       selListRel,
                                                       false);
    return routableObjsList;
  }
   /* showAddContent - This method is used to determine if
   *             the context user can see the add content link.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Common 10-5
   */
   public boolean showAddContentLink(Context context,String []args) throws Exception
   {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     String objectId    = (String) programMap.get("objectId");
     return checkContentAccess(context,objectId,true);
   }
   /* showUpload - This method is used to determine if
   *             the context user can see the Upload content link.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Common 10-5
   */
   public boolean showContentUploadLink(Context context,String []args) throws Exception
   {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     String objectId    = (String) programMap.get("objectId");
     boolean bTeam = FrameworkUtil.isSuiteRegistered(context,"featureVersionTeamCentral",false,null,null);
     boolean bProgram = FrameworkUtil.isSuiteRegistered(context,"appVersionProgramCentral",false,null,null);
     boolean uploadcheck = false;
     boolean checkAccess=checkContentAccess(context,objectId,true);
     if(checkAccess){
        if(bTeam || bProgram)
          uploadcheck = true;
        else
          uploadcheck = false;
     }
     return uploadcheck;
   }

    /* showStateCondition - This method is used to show the Value
     *                      for the State Condition Column
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return Object Vector
   * @throws Exception if the operation fails
   * @since Common 10-5
   */
   public Vector showStateCondition(Context context,String []args) throws Exception
   {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     Map paramList      = (Map)programMap.get("paramList");
     String languageStr = (String)paramList.get("languageStr");
     MapList objectList   = (MapList) programMap.get("objectList");
     String sNoneValue=EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.AttachmentsDialog.none");
     Map objectMap = null;
     String sPolicy ="",stateValue="";
     Vector showStateCon = new Vector();
    int objectListSize = 0 ;
    if(objectList != null)
    {
        objectListSize = objectList.size();
    }
    for(int i=0; i< objectListSize; i++)
    {
      try{
        objectMap = (HashMap) objectList.get(i);
      }catch(ClassCastException cce){
        objectMap = (Hashtable) objectList.get(i);
      }
    sPolicy = (String) objectMap.get(Route.SELECT_ROUTE_BASEPOLICY);
    sPolicy=(String) PropertyUtil.getSchemaProperty(context,sPolicy);
       stateValue = (String)objectMap.get(Route.SELECT_ROUTE_BASESTATE);
       if (!"Ad Hoc".equals(stateValue)){
        stateValue = FrameworkUtil.lookupStateName(context,sPolicy,stateValue);
        stateValue = i18nNow.getStateI18NString(sPolicy,stateValue,languageStr);
       } else {
         stateValue = sNoneValue;
       }
     showStateCon.add(stateValue);
    }
     return showStateCon;
   }
   /* checkAccess - This method is used to determine if
   *             the context user has access on the object which will be used in the above
   *              methods defined before this.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Common 10-5
   */
   public boolean checkContentAccess (Context context,String objectId,boolean andcondition) throws Exception
   {
     boolean checkAccess = false;
     String selectStr = "from["+DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.id";
     DomainObject taskObject = DomainObject.newInstance(context,objectId);
     objectId = taskObject.getInfo(context,selectStr);
     // Due to Resume Process implementation there can be tasks which are not connected to route and hence we cannot find
     // the route id from these tasks. In case of such objects one will not be updating contents on the task.
     if (objectId == null) {
          return false;
     }
     Route route = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);

     route.setId(objectId);
     String sOwner       = route.getInfo(context, route.SELECT_OWNER);
     String sState = route.getInfo(context,route.SELECT_CURRENT);
     boolean isRouteEditable = true;
     // Do not show links if the Route State is Complete or Archive
     if(sState.equals("Complete") || sState.equals("Archive")){
       isRouteEditable = false;
     }
     route.open(context);
     Access contextAccess = route.getAccessMask(context);
     route.close(context);
     if(andcondition)
     {
       if ( (sOwner.equals(context.getUser()) && isRouteEditable) || (AccessUtil.hasAddAccess(contextAccess)) ){
        checkAccess = true;
       }
     }
     else
     {
       if ( sOwner.equals(context.getUser()) || isRouteEditable ){
          checkAccess = true;
       }
     }
     return checkAccess;
   }
    /**
     * Finds out the data for table APPRouteTaskRevisions
     * @param context The Matrix Context object
     * @param args The arguments
     * @return MapList the containing the data for table APPRouteTaskRevisions
     * @throws Exception if operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTaskRevisions(Context context, String[] args) throws Exception {
        Map programMap = (HashMap) JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");

        DomainObject dmoInboxTask = new DomainObject(strObjectId);

        StringList slBusSelect = new StringList(DomainObject.SELECT_ID);
        slBusSelect.add(DomainObject.SELECT_OWNER);

        MapList mlTaskRevisions = dmoInboxTask.getRevisionsInfo(context, slBusSelect, new StringList());
        return mlTaskRevisions;
   }

   /**
     * Finds out the data for column "Comments Or Instructions" in table APPRouteTaskRevisions
     * @param context The Matrix Context object
     * @param args The arguments
     * @return Vector containing data for column "Comments Or Instructions"
     * @throws Exception if operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
   public Vector getTaskRevisionsCommentsOrInstructions(Context context, String[] args) throws Exception {
        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList mlObjectList = (MapList)programMap.get("objectList");

        Map mapObjectInfo = null;
        String strObjectId = null;
        String strStateName = null;
        String strComments = null;
        String strInstructions = null;
        DomainObject dmoInboxTask = null;
        Map mapInfo = null;
        Vector vecResult = new Vector();

        final String STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
        final String SELECT_COMMENTS = "attribute[" + DomainObject.ATTRIBUTE_COMMENTS + "]";
        final String SELECT_ROUTE_INSTRUCTIONS = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]";

        StringList slBusSelects = new StringList();
        slBusSelects.add(DomainObject.SELECT_CURRENT);
        slBusSelects.add(SELECT_COMMENTS);
        slBusSelects.add(SELECT_ROUTE_INSTRUCTIONS);

        for (Iterator itrObjects = mlObjectList.iterator(); itrObjects.hasNext(); ) {
            mapObjectInfo = (Map)itrObjects.next();
            strObjectId = (String)mapObjectInfo.get(DomainObject.SELECT_ID);

            dmoInboxTask = new DomainObject(strObjectId);
            mapInfo = (Map)dmoInboxTask.getInfo(context, slBusSelects);

            strStateName = (String)mapInfo.get(DomainObject.SELECT_CURRENT);
            strComments = (String)mapInfo.get(SELECT_COMMENTS);
            strInstructions = (String)mapInfo.get(SELECT_ROUTE_INSTRUCTIONS);

            // If task is completed then show the comments else show the instructions
            if (STATE_COMPLETE.equals(strStateName)) {
                vecResult.add(strComments);
            }
            else {
                vecResult.add(strInstructions);
            }
        }
        return vecResult;
   }

   //Added for Next Gen UI migration - Inbox Task Details display

   /**
    * Used to display the value of Delegation allowed or not for the task
    *
    * @param context Object
    * @param args String array
    * @throws Exception
    */

   public String getAllowDelegationValue(Context context, String[] args)throws Exception
   {
        HashMap detailsMap = getInboxTaskFormFieldAccessDetails( context, args);
        String languageStr = (String)detailsMap.get("languageStr");
        if (languageStr == null)
        {
            Map programMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            languageStr = (String) requestMap.get("languageStr");
        }
        return "FALSE".equalsIgnoreCase((String)detailsMap.get(ATTRIBUTE_ALLOW_DELEGATION)) ?
        		EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.No") :
        		EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.Yes");
   }

   public boolean checksToEditTask(Context context, String[] args) throws FrameworkException {
       HashMap detailsMap = getInboxTaskPropertiesAccessCommands( context, args);
       return ((Boolean)detailsMap.get("APPTaskEdit")).booleanValue();
   }

   public boolean checksToAcceptTask(Context context, String[] args) throws FrameworkException {
       HashMap detailsMap = getInboxTaskPropertiesAccessCommands( context, args);
       return ((Boolean)detailsMap.get("APPAcceptInboxTask")).booleanValue();
   }

   public boolean checksToPromoteTask(Context context, String[] args) throws FrameworkException {
       HashMap detailsMap = getInboxTaskPropertiesAccessCommands( context, args);
       return ((Boolean)detailsMap.get("APPPromoteInboxTask")).booleanValue();
   }

   public boolean checksToDemoteTask(Context context, String[] args) throws FrameworkException {
       HashMap detailsMap = getInboxTaskPropertiesAccessCommands( context, args);
       return ((Boolean)detailsMap.get("APPDemoteInboxTask")).booleanValue();
   }


   /**
    * Access Program to display the Task complete link
    *
    * @param context Object
    * @param args String array
    * @throws Exception
    */

   public boolean displayCompleteLink(Context context, String[] args)throws Exception
   {
       HashMap detailsMap = getInboxTaskPropertiesAccessCommands( context, args);
       return ((Boolean)detailsMap.get("APPCompleteTask")).booleanValue();
   }

   /**
    * Access Program to display the Task Approve/Reject link
    *
    * @param context Object
    * @param args String array
    * @throws Exception
    */

   public boolean displayApproveRejectLink(Context context, String[] args)throws Exception
   {
       HashMap detailsMap = getInboxTaskPropertiesAccessCommands( context, args);
       return ((Boolean)detailsMap.get("APPApproveTask")).booleanValue();

   }

   /**
    * Access Program to display the Task Abstain link
    *
    * @param context Object
    * @param args String array
    * @throws Exception
    */

   public boolean displayAbstainLink(Context context, String[] args)throws Exception
   {
       HashMap detailsMap = getInboxTaskPropertiesAccessCommands( context, args);
       return ((Boolean)detailsMap.get("APPAbstainTask")).booleanValue();

   }

   /**
    * Program to display the assignee set due date
    *
    * @param context Object
    * @param args String array
    * @throws Exception
    */

   public String displayAssigneeDueDate(Context context, String[] args)throws Exception{
	   
	   String duedate = "";
       String Actualduedate = "";
       
       HashMap detailsMap = getInboxTaskFormFieldAccessDetails(context, args);
       String timeZone = (String)detailsMap.get("timeZone");
       String languageStr= (String)detailsMap.get("languageStr");
       String allowDelegation = (String)detailsMap.get(ATTRIBUTE_ALLOW_DELEGATION);
       String taskScheduledDate = (String)detailsMap.get(ATTRIBUTE_SCHEDULED_COMPLETION_DATE);

       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       Map requestMap = (Map)programMap.get("requestMap");
       Locale locale = (Locale)requestMap.get("localeObj");
	   
	   boolean bAssigneeDueDate = ((Boolean)detailsMap.get("bAssigneeDueDate")).booleanValue();
       String objectId = (String) requestMap.get("objectId");
       DomainObject routeTask = DomainObject.newInstance(context, objectId);
       String strRouteOwner = routeTask.getInfo(context,"from["+RELATIONSHIP_ROUTE_TASK+"].to.owner");
       boolean check=false;
       if(UIUtil.isNotNullAndNotEmpty(strRouteOwner)){
    	   check = strRouteOwner.equals(context.getUser()) || bAssigneeDueDate ;
       }

       String finalLzDate           = "";
       Date taskDueDate             = null;
       StringBuffer strHTMLBuffer   = new StringBuffer();
       Calendar calendar            = new GregorianCalendar();

       String temp_hhrs_mmin = EnoviaResourceBundle.getProperty(context, "emxComponents.RouteScheduledCompletionTime");

       boolean bDueDateEmpty    = ((Boolean)detailsMap.get("bDueDateEmpty")).booleanValue();

       boolean is24= false;
		String amPm = "AM";
		// 24 Hours format for _de and _ja more can be added
		if(languageStr.startsWith("de") || languageStr.startsWith("ja")){
			
			is24 = true;
			amPm="";
		}
		
       if(!bDueDateEmpty) {

           double clientTZOffset   = (new Double(timeZone)).doubleValue();
           taskDueDate             = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(taskScheduledDate);
           int iTimeZone           = (new Double(clientTZOffset * -3600000)).intValue();
           int totaloffset         = calendar.get(Calendar.ZONE_OFFSET)+ calendar.get(Calendar.DST_OFFSET);
           taskDueDate.setTime(taskDueDate.getTime()-totaloffset+iTimeZone);
           finalLzDate             = eMatrixDateFormat.getFormattedDisplayDate(taskScheduledDate,clientTZOffset,locale);
          // calendar.setTime(taskDueDate);
           calendar = getTimeNow(temp_hhrs_mmin);
           
           int hour=taskDueDate.getHours();
           String minute="";
           
           minute= taskDueDate.getMinutes()<10?  "0"+taskDueDate.getMinutes():""+taskDueDate.getMinutes();         
		   String seconds =taskDueDate.getSeconds()<10?  "0"+taskDueDate.getSeconds():""+taskDueDate.getSeconds();          
           hour = (hour>12 && !is24 ) ? hour-12 : hour;
           amPm = is24  ? amPm : (taskScheduledDate.endsWith("AM")?" AM":" PM");
           
           
           
           Actualduedate = ""+hour+":"+minute+":"+seconds+amPm;
           duedate = ""+hour+":"+minute+amPm;
       }
       //Due date is always set. wont come to else block
       //  else{
       //    calendar = getTimeNow(temp_hhrs_mmin);
       // }
       if((bAssigneeDueDate && bDueDateEmpty) || (bAssigneeDueDate || (allowDelegation.equals("TRUE"))))
       {
           strHTMLBuffer = new StringBuffer(64);
           strHTMLBuffer.append("<input type=\"text\" readonly=\"readonly\" size=\"\" name=\"DueDate\" value=\"").append(finalLzDate).append("\" id=\"DueDate\">");
		   if(check) {
			   strHTMLBuffer.append("&#160;<a href=\"javascript:showCalendar('editDataForm', 'DueDate', '").append(taskScheduledDate).append("', '', saveFieldObjByName('DueDate'))\">");
			   strHTMLBuffer.append("<img src=\"../common/images/iconSmallCalendar.gif\" alt=\"Date Picker\" border=\"0\"></a>");
		   }
           strHTMLBuffer.append("<input type=\"hidden\" name=\"DueDate").append("fieldValue\"  value=\"\">");
           strHTMLBuffer.append("<input type=\"hidden\" name=\"DueDate").append("AmPm\"  value=\"\">");
           strHTMLBuffer.append("<script language=\"JavaScript\">document.forms[\"editDataForm\"][\"DueDate\"].fieldLabel=\"DueDate\";</script>").append("&#160;");
           strHTMLBuffer.append("<select name=\"routeTime\" id= \"routeTime\" style=\"font-size: 8pt\"");		   
		   strHTMLBuffer.append((check)?(">"):("readonly=\"readonly\" onfocus=\" this.blur()\">"));
		   strHTMLBuffer.append("<Option value=\"").append(Actualduedate).append("\"").append("Selected").append(">").append(duedate).append("</Option>");
		   
            int hour = 5;
            
			boolean minFlag = true;
			boolean amFlag = true;
          // taskHour      = calendar.get(Calendar.HOUR_OF_DAY);
          // taskMinitue   = calendar.get(Calendar.MINUTE);
           for (int i=0;i<48;i++) {
               String ttime     = "";
               String Slct      ="";
               String timeValue = "";

				if(hour>12 && !is24)
				{
					hour =1 ;
				}
				if(minFlag)
				{
					ttime = hour + ":00 " + amPm;
					timeValue = hour  + ":00" + ":00 " + amPm;
					minFlag = false;
				}else
				{
					ttime = hour + ":30 " + amPm;
					timeValue = hour  + ":30" + ":00 " + amPm;
					hour++;
					minFlag = true;
					if(hour==12 && !is24)
	            	{
	            		amPm = "PM";
               }
               }

               strHTMLBuffer.append("<Option value=\"").append(timeValue).append("\"").append(Slct).append(">").append(ttime).append("</Option>");
               if(hour>12 && is24 && amFlag){
					hour=13;
					amFlag = false;
               }
           	if(hour>23 && is24){
					hour=00;
           }

           	if(ttime.equalsIgnoreCase("11:30 PM") && !is24)
           	{
           		amPm = "AM";
           	}
         }  //end for
         String defaultTime = temp_hhrs_mmin.substring(0, (temp_hhrs_mmin.indexOf(':')+3))+":00 "+(temp_hhrs_mmin.indexOf("AM")>0?"AM":"PM");
         strHTMLBuffer.append("</select>");
         strHTMLBuffer.append("<input type=\"hidden\" name=\"routeTime\" value=\"\">");
		 if(check) {
			 strHTMLBuffer.append("<a href=\"javascript:cleardate('").append(defaultTime).append("')\">");
			 strHTMLBuffer.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.Clear"));
			 strHTMLBuffer.append("</a>");
		 }
   }
       return strHTMLBuffer.toString();
   }

   protected HashMap getInboxTaskPropertiesAccessCommands( Context context, String[] args) throws FrameworkException {
       try {
           String contextUser = context.getUser();
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String objectId = (String)programMap.get("objectId");
           objectId = objectId != null ? objectId : (String)requestMap.get("objectId");

           String selAttrAssigneeSetDueDate 		= getAttributeSelect(ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
           String selAttrAllowDelegation 			= getAttributeSelect(ATTRIBUTE_ALLOW_DELEGATION);
           String selAttrScheduledCompletionDate	= getAttributeSelect(ATTRIBUTE_SCHEDULED_COMPLETION_DATE);

           InboxTask taskBean = (InboxTask)DomainObject.newInstance(context, objectId);

           String SELECT_ROUTE_ID  = "from[" + RELATIONSHIP_ROUTE_TASK + "].to.id";
           String SELECT_TASK_ASSIGNEE_NAME    = "from[" + RELATIONSHIP_PROJECT_TASK + "].to.name";
           String selAttrNeedsReview = getAttributeSelect(ATTRIBUTE_REVIEW_TASK);

           StringList slBusSelect = new StringList();
           slBusSelect.add(SELECT_CURRENT);
           slBusSelect.add(SELECT_OWNER);
           slBusSelect.add(Route.SELECT_ROUTE_ACTION);
           slBusSelect.add(selAttrNeedsReview);
           slBusSelect.add(SELECT_ROUTE_ID);
           slBusSelect.add(SELECT_TASK_ASSIGNEE_NAME);

           Map mapTaskInfo = taskBean.getInfo(context, slBusSelect);
           String taskState         = (String)mapTaskInfo.get(SELECT_CURRENT);
           String sTaskOwner        = (String)mapTaskInfo.get(SELECT_OWNER);
           String routeAction       = (String)mapTaskInfo.get(Route.SELECT_ROUTE_ACTION);
           String needsReview       = (String)mapTaskInfo.get(selAttrNeedsReview);
           String strTaskAssignee = (String)mapTaskInfo.get(SELECT_TASK_ASSIGNEE_NAME);
           String taskScheduledDate 	= (String)mapTaskInfo.get(selAttrScheduledCompletionDate);
           String assigneeDueDateOpt 	= (String)mapTaskInfo.get(selAttrAssigneeSetDueDate);
           String allowDelegation 		= (String)mapTaskInfo.get(selAttrAllowDelegation);

           boolean isAssignedToGroupOrRole = taskBean.checkIfTaskIsAssignedToGroupOrRole(context);
           boolean isApprovalRoute = "Approve".equals(routeAction);
           boolean isCommentTask = "Comment".equalsIgnoreCase(routeAction);

           // Due to Resume Process implementation there can be tasks which are not connected to route and hence we cannot find
           // the route id from these tasks. Then the route id can be found by first finding the latest revision of the task
           // and then querying for the route object.
           // Bug 302957 - Added push and pop context
           ContextUtil.pushContext(context);
           String routeId   = (String)mapTaskInfo.get(SELECT_ROUTE_ID);
           boolean isReadOnly = UIUtil.isNullOrEmpty(routeId);
           if (isReadOnly) {
               DomainObject dmoLastRevision = new DomainObject(taskBean.getLastRevision(context));
               routeId = dmoLastRevision.getInfo(context, SELECT_ROUTE_ID);
               // No action commands will be shown for such tasks
               isReadOnly = true;
           }

           Route boRoute = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
           boRoute.setId(routeId);

           String sSelectOwningOrgId =  "from[" + RELATIONSHIP_INITIATING_ROUTE_TEMPLATE + "].to.to["
                                                + RELATIONSHIP_OWNING_ORGANIZATION + "].from.id";
           StringList busSelects = new StringList(4);
           busSelects.addElement(SELECT_ID );
           busSelects.addElement(SELECT_OWNER );
           busSelects.addElement(Route.SELECT_ROUTE_STATUS );
           busSelects.addElement(sSelectOwningOrgId );
           Map mRouteInfo = boRoute.getInfo( context, busSelects );
           String routeOwner= (String) mRouteInfo.get( DomainConstants.SELECT_OWNER ) ;
           String sStatus = (String) mRouteInfo.get( Route.SELECT_ROUTE_STATUS);
           String sOwningOrgId = (String) mRouteInfo.get( sSelectOwningOrgId );

           ContextUtil.popContext(context);

           boolean isRouteStarted = "Started".equals(sStatus);
           String showAbstain = EnoviaResourceBundle.getProperty(context,"emxComponents.Routes.ShowAbstainForTaskApproval");
           showAbstain = UIUtil.isNullOrEmpty(showAbstain) ? "true" : showAbstain;

           String STATE_ASSIGNED = PropertyUtil.getSchemaProperty(context, "policy", POLICY_INBOX_TASK, "state_Assigned");
           String STATE_REVIEW = PropertyUtil.getSchemaProperty(context, "policy", POLICY_INBOX_TASK, "state_Review");
           String STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "policy", POLICY_INBOX_TASK, "state_Complete");

           boolean isInAssignedState = taskState.equals(STATE_ASSIGNED);
           boolean isInReviewState = taskState.equals(STATE_REVIEW);
           boolean isInCompleteState = taskState.equalsIgnoreCase(STATE_COMPLETE);

           boolean isTaskOwner = sTaskOwner.equals(contextUser);
           boolean isRouteOwner = routeOwner.equals(contextUser);
           boolean isTaskAssignee = contextUser.equals(strTaskAssignee);

           boolean bShowAcceptCmd = true;
           boolean bEditDetails     = false;
           boolean bCompleteLink    = false;
           boolean bApproveLink     = false;
           boolean bNeedsReview          = isInReviewState && isRouteOwner;

           //////////////////////////////////////////////////////////////////////////////
           // The command Update Assignee will be shown in Assigned state only.
           // Route owner can Update Assignee any time
           // Task assignee can Update Assignee, only if the task is delegatable
           //
           if (isInAssignedState) {
//             IR-043921V6R2011 - Changes START
               if(isAssignedToGroupOrRole && !UIUtil.isNullOrEmpty(sOwningOrgId)) {
                   Organization org = (Organization) DomainObject.newInstance( context, sOwningOrgId );
                   busSelects = new StringList(2);
                   busSelects.addElement(SELECT_ID );
                   busSelects.addElement(SELECT_NAME );
                   String sWhereClause = "( name == \"" + contextUser + "\" )";
                   MapList mlMembers = org.getMemberPersons( context, busSelects, sWhereClause, null );
                   bShowAcceptCmd = !mlMembers.isEmpty();
               }
           }

           // Show Complete link for non-Approve type of tasks
           bCompleteLink = (isInAssignedState && isTaskOwner && !isApprovalRoute);

           // for Approve tasks, show the links Approve / Reject / Abstain
           //if(!taskState.equalsIgnoreCase("Complete") && (taskState.equals("Assigned") && sTaskOwner.equals(sLoginPerson) && "Approve".equals(routeAction) || strTaskAssignee.equals(sLoginPerson))) {

           if(isInAssignedState && isApprovalRoute && (isTaskOwner || isTaskAssignee)) {
               bApproveLink = true;
           }

           HashMap returnMap = new HashMap(5);

          //Edit details link is provided when any of the  3 fields in the edit task webform is displayed, otherwise we dont display thr edit details command.
           boolean bDueDateEmpty  = UIUtil.isNullOrEmpty(taskScheduledDate);
           boolean bAssigneeDueDate  = "Yes".equals(assigneeDueDateOpt);
           boolean showTaskComments  = isTaskOwner && isInAssignedState;
           boolean showReviewComments = "Yes".equalsIgnoreCase(needsReview);

           boolean showAssigneeDueDate = (bAssigneeDueDate && bDueDateEmpty) || (bAssigneeDueDate || "TRUE".equals(allowDelegation));
           boolean canEditReviewerComments = showReviewComments && STATE_REVIEW.equalsIgnoreCase(taskState) && isRouteOwner;
           bEditDetails = (showAssigneeDueDate || showTaskComments || canEditReviewerComments);


           bCompleteLink = isRouteStarted && !isReadOnly && bCompleteLink;
           bApproveLink =  isRouteStarted && !isReadOnly && bApproveLink;
           boolean bAbstainLink = bApproveLink && "true".equalsIgnoreCase(showAbstain);
           bShowAcceptCmd = isAssignedToGroupOrRole && (taskState.equals("") || taskState.equals("Assigned")) && bShowAcceptCmd;

           returnMap.put("APPTaskEdit", Boolean.valueOf(bEditDetails));
           returnMap.put("APPCompleteTask", Boolean.valueOf(bCompleteLink));
           returnMap.put("APPApproveTask", Boolean.valueOf(bApproveLink));
           returnMap.put("APPRejectTask", Boolean.valueOf(bApproveLink));
           returnMap.put("APPAbstainTask", Boolean.valueOf(bAbstainLink));
           returnMap.put("APPAcceptInboxTask", Boolean.valueOf(bShowAcceptCmd));
           returnMap.put("APPPromoteInboxTask", Boolean.valueOf(bNeedsReview));
           returnMap.put("APPDemoteInboxTask", Boolean.valueOf(bNeedsReview));

           return returnMap;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }

   }

   protected HashMap getInboxTaskFormFieldAccessDetails( Context context, String[] args) throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       String objectId = (String)programMap.get("objectId");

       String languageStr = requestMap != null ? (String)requestMap.get("languageStr") : (String)programMap.get("languageStr");
       objectId = objectId != null ? objectId : (String)requestMap.get("objectId");
       String timeZone = requestMap != null ? (String)requestMap.get("timeZone") : (String)programMap.get("timeZone");


       String SELECT_ROUTE_ID = "from[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].to.id";

       String loggedInUser = context.getUser();

       String selAttrAssigneeSetDueDate = getAttributeSelect(ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
       String selAttrAllowDelegation = getAttributeSelect(ATTRIBUTE_ALLOW_DELEGATION);
       String selAttrScheduledCompletionDate = getAttributeSelect(ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
       String selAttrReviewTask  = getAttributeSelect(PropertyUtil.getSchemaProperty(context, "attribute_ReviewTask"));

       StringList selectables = new StringList(2);
       selectables.add(DomainConstants.SELECT_OWNER);
       selectables.add(DomainConstants.SELECT_CURRENT);
       selectables.add(selAttrAssigneeSetDueDate);
       selectables.add(selAttrAllowDelegation);
       selectables.add(selAttrScheduledCompletionDate);
       selectables.add(SELECT_ROUTE_ID);
       selectables.add(selAttrReviewTask);

       DomainObject doInboxTask = DomainObject.newInstance(context,objectId);
       Map infoMap = doInboxTask.getInfo(context,selectables);

       String taskOwner = (String)infoMap.get(SELECT_OWNER);
       String taskScheduledDate = (String)infoMap.get(selAttrScheduledCompletionDate);
       String assigneeDueDateOpt = (String)infoMap.get(selAttrAssigneeSetDueDate);
       String allowDelegation = (String)infoMap.get(selAttrAllowDelegation);
       String routeId = (String)infoMap.get(SELECT_ROUTE_ID);
       String strCurrentState = (String)infoMap.get(DomainConstants.SELECT_CURRENT);
       String reivewTask    = (String)infoMap.get(selAttrReviewTask);

        if (routeId == null) {
            DomainObject dmoLastRevision = new DomainObject(doInboxTask.getLastRevision(context));
            routeId = dmoLastRevision.getInfo(context, SELECT_ROUTE_ID);
        }

       DomainObject dmoRoute = new DomainObject(routeId);
       String routeOwner     = (String)dmoRoute.getInfo(context, DomainObject.SELECT_OWNER);
       String STATE_ASSIGNED = PropertyUtil.getSchemaProperty(context, "policy", DomainConstants.POLICY_INBOX_TASK, "state_Assigned");
       String STATE_REVIEW = PropertyUtil.getSchemaProperty(context, "policy", DomainConstants.POLICY_INBOX_TASK, "state_Review");
       boolean isInAssignedState = STATE_ASSIGNED.equals(strCurrentState);

       boolean isRouteOwner = routeOwner.equals(loggedInUser);
       boolean isTaskOwner = taskOwner.equals(loggedInUser);


       boolean showAssigneeField =  isInAssignedState &&
                                       (isRouteOwner ||
                                       (isTaskOwner && "TRUE".equalsIgnoreCase(allowDelegation)));

       boolean bDueDateEmpty  = UIUtil.isNullOrEmpty(taskScheduledDate);
       boolean bAssigneeDueDate  = "Yes".equals(assigneeDueDateOpt);
       boolean showTaskComments  = isTaskOwner && isInAssignedState;
       boolean showAssigneeDueDate = (bAssigneeDueDate && bDueDateEmpty) || (bAssigneeDueDate || allowDelegation.equals("TRUE"));
       boolean showReviewComments = "Yes".equalsIgnoreCase(reivewTask);
       boolean canEditReviewerComments = showReviewComments && STATE_REVIEW.equalsIgnoreCase(strCurrentState) && isRouteOwner;

       HashMap detailsMap = new HashMap();
       detailsMap.put("timeZone", timeZone);
       detailsMap.put("languageStr", languageStr);
       detailsMap.put("showAssigneeField", Boolean.valueOf(showAssigneeField));
       detailsMap.put("showAssigneeDueDate", Boolean.valueOf(showAssigneeDueDate));
       detailsMap.put("bDueDateEmpty", Boolean.valueOf(bDueDateEmpty));
       detailsMap.put("bAssigneeDueDate", Boolean.valueOf(bAssigneeDueDate));
       detailsMap.put("showTaskComments", Boolean.valueOf(showTaskComments));
       detailsMap.put("showReviewComments", Boolean.valueOf(showReviewComments));
       detailsMap.put("canEditReviewerComments", Boolean.valueOf(canEditReviewerComments));
       detailsMap.put(ATTRIBUTE_SCHEDULED_COMPLETION_DATE, taskScheduledDate);
       detailsMap.put(ATTRIBUTE_ALLOW_DELEGATION, allowDelegation);

       return detailsMap;
   }
// method to return system date calendar; time will be from property
   public GregorianCalendar getTimeNow(String temp_hhrs_mmin){


       GregorianCalendar cal = new GregorianCalendar();

       int hhrs              = Integer.parseInt(temp_hhrs_mmin.substring(0, temp_hhrs_mmin.indexOf(':')).trim());
       int index             = temp_hhrs_mmin.indexOf("AM");

       if(index < 0) {
            index            = temp_hhrs_mmin.indexOf("PM");
            if(hhrs < 12){
              hhrs += 12;
            }
       } else {
             if(hhrs == 12){
               hhrs = 0;
             }
       }

       int mmin              = Integer.parseInt(temp_hhrs_mmin.substring(temp_hhrs_mmin.indexOf(':')+1, index).trim());
       cal.set(Calendar.HOUR_OF_DAY, hhrs);
       cal.set(Calendar.MINUTE, mmin);

       return cal;
   }

   @com.matrixone.apps.framework.ui.PostProcessCallable
   public HashMap updateTaskDetails(Context context, String[] args) throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       String languageStr = (String)requestMap.get("languageStr");
       Locale locale = (Locale)requestMap.get("localeObj");
       String timeZone = (String)requestMap.get("timeZone");
       String taskId    = (String)requestMap.get("objectId");
       String taskComments    = (String)requestMap.get("Comments");
       String reviewerComments    = (String)requestMap.get("ReviewerComments");
       String taskScheduledDate    = (String)requestMap.get("DueDate");
       String assigneeDueTime    = (String)requestMap.get("routeTime");
       HashMap resultsMap = new HashMap();

       InboxTask inboxTaskObj = (InboxTask)DomainObject.newInstance(context,DomainConstants.TYPE_INBOX_TASK);

       if(!UIUtil.isNullOrEmpty(taskId))
    	   inboxTaskObj.setId(taskId);
       inboxTaskObj.open(context);
       String routeId = inboxTaskObj.getInfo(context, "from[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].to.id");

       if (!UIUtil.isNullOrEmpty(taskScheduledDate)) {
           double clientTZOffset = (new Double(timeZone)).doubleValue();
           taskScheduledDate     =  eMatrixDateFormat.getFormattedInputDateTime(context,taskScheduledDate,assigneeDueTime,clientTZOffset, locale);

       }



           Route route = (Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
           DomainObject dmoLastRevision = new DomainObject(inboxTaskObj.getLastRevision(context));
           String sRouteId = dmoLastRevision.getInfo(context, "from[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].to.id");

           //BusinessObject boTask = new BusinessObject( taskId );
           //boTask.open( context );


           BusinessObjectAttributes boAttrGeneric = inboxTaskObj.getAttributes(context);
           AttributeItr attrItrGeneric   = new AttributeItr(boAttrGeneric.getAttributes());
           AttributeList attrListGeneric = new AttributeList();

           String sAttrValue = "";
           String sTrimVal   = "";
           while (attrItrGeneric.next()) {
             Attribute attrGeneric = attrItrGeneric.obj();
             sAttrValue = (String)requestMap.get(attrGeneric.getName());
             if (sAttrValue != null) {
               sTrimVal = sAttrValue.trim();
               if ( attrGeneric.getName().equals(DomainConstants.ATTRIBUTE_APPROVAL_STATUS) && sTrimVal.equals("Reject") ) {
                 Pattern relPattern  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TASK);
                 Pattern typePattern = new Pattern(DomainConstants.TYPE_ROUTE);
                 BusinessObject boRoute = ComponentsUtil.getConnectedObject(context,inboxTaskObj,relPattern.getPattern(),typePattern.getPattern(),false,true);

                 if ( boRoute != null ) {
                 boRoute.open(context);

                 AttributeItr attributeItr = new AttributeItr(boRoute.getAttributes(context).getAttributes());

                 Route routeObj = (Route)DomainObject.newInstance(context,boRoute);

                 StringList routeSelects = new StringList(3);
                 routeSelects.add(Route.SELECT_OWNER);
                 routeSelects.add(Route.SELECT_NAME);
                 routeSelects.add(Route.SELECT_REVISION);
                 Map routeInfo = routeObj.getInfo(context,routeSelects);

                 String routeOwner = (String)routeInfo.get(Route.SELECT_OWNER);
                 String routeName = (String)routeInfo.get(Route.SELECT_NAME);
                 String routeRev = (String)routeInfo.get(Route.SELECT_REVISION);

                 while ( attributeItr.next() ) {
                   AttributeList attributeList = new AttributeList();
                   Attribute attribute = attributeItr.obj();

                   if( attribute.getName().equals(DomainConstants.ATTRIBUTE_ROUTE_STATUS) ) {
                     Map attrMap = new Hashtable();
                     attrMap.put(DomainConstants.ATTRIBUTE_ROUTE_STATUS, "Stopped");
                     routeObj.modifyRouteAttributes(context, attrMap);
                     /*send notification to the owner*/
                     String[] subjectKeys = {};
                     String[] subjectValues = {};

                     String[] messageKeys = {"name","IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
                     String[] messageValues = {(context.getUser()).toString(),Route.TYPE_INBOX_TASK, inboxTaskObj.getName(), inboxTaskObj.getRevision(), Route.TYPE_ROUTE, routeName, routeRev};

                     StringList objectIdList = new StringList();
                     objectIdList.addElement(taskId);

                     StringList toList = new StringList();
                     toList.add(routeOwner);
                     MailUtil.sendNotification(context,
                                               toList,
                                               null,
                                               null,
                                               "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectReject",
                                               subjectKeys,
                                               subjectValues,
                                               "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageReject",
                                               messageKeys,
                                               messageValues,
                                               objectIdList,
                                               null);
                     break;
                   }
                 }
                 boRoute.close(context);
                 }
               }
               attrGeneric.setValue(sTrimVal);
               attrListGeneric.addElement(attrGeneric);
             }
           }

           if(!UIUtil.isNullOrEmpty(reviewerComments)) {
               attrListGeneric.addElement(new Attribute(new AttributeType(sAttrReviewersComments), reviewerComments));
           }

           //Update the attributes on the Business Object
           // Do not use !UIUtil.isNullOrEmpty(taskScheduledDate) in the if condition here bcz sometimes we need to set empty value to due date.
           if (taskScheduledDate != null)
               inboxTaskObj.setAttributeValue(context,inboxTaskObj.ATTRIBUTE_SCHEDULED_COMPLETION_DATE, taskScheduledDate);
           inboxTaskObj.setAttributes(context, attrListGeneric);
           inboxTaskObj.update(context);
           String RelationshipId = FrameworkUtil.getAttribute(context,inboxTaskObj,DomainConstants.ATTRIBUTE_ROUTE_NODE_ID);

           route.setId(sRouteId);
           //Get the correct relId for the RouteNodeRel given the attr routeNodeId from the InboxTask.
           RelationshipId = route.getRouteNodeRelId(context, RelationshipId);

           // Updating the relationship Attributes
           Map attrMap = new Hashtable();

           Relationship relRouteNode = new Relationship(RelationshipId);
           relRouteNode.open(context);
           AttributeItr attrRelItrGeneric   = new AttributeItr(relRouteNode.getAttributes(context));
           while (attrRelItrGeneric.next()) {
               sTrimVal = null;
               Attribute attrGeneric = attrRelItrGeneric.obj();
               sAttrValue = attrGeneric.getName();
               if(sAttrValue.equals(DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE)) {
                   sTrimVal = taskScheduledDate;
               } else if(sAttrValue.equals(DomainConstants.ATTRIBUTE_COMMENTS)) {
                   sTrimVal = taskComments;
               } else if(sAttrValue.equals(sAttrReviewersComments) && !UIUtil.isNullOrEmpty(sAttrReviewersComments)) {
                   sTrimVal = reviewerComments;
               }
             if(sTrimVal != null) {
               attrMap.put(sAttrValue, sTrimVal);
             }
           }

           Route.modifyRouteNodeAttributes(context, RelationshipId, attrMap);
           relRouteNode.close(context);
           inboxTaskObj.close( context );




       return resultsMap;
   }

   public boolean showAssigneeDueDate(Context context, String[] args) throws Exception
   {
       HashMap detailsMap = getInboxTaskFormFieldAccessDetails(context, args);
       return ((Boolean)detailsMap.get("showAssigneeDueDate")).booleanValue();
   }

   public boolean showChangeAssigneeCommand(Context context, String[] args) throws Exception
   {
       HashMap detailsMap = getInboxTaskFormFieldAccessDetails(context, args);
       return ((Boolean)detailsMap.get("showAssigneeField")).booleanValue();
   }



   public boolean showReviewComments(Context context, String[] args) throws Exception
   {
       HashMap detailsMap = getInboxTaskFormFieldAccessDetails(context, args);
       return ((Boolean)detailsMap.get("showReviewComments")).booleanValue();
   }


   public boolean canEditReviewerComments(Context context, String[] args) throws Exception
   {
       HashMap detailsMap = getInboxTaskFormFieldAccessDetails(context, args);
       return ((Boolean)detailsMap.get("canEditReviewerComments")).booleanValue();
   }

   public boolean showTaskComments(Context context, String[] args) throws Exception
   {
       HashMap detailsMap = getInboxTaskFormFieldAccessDetails(context, args);
       return ((Boolean)detailsMap.get("showTaskComments")).booleanValue();
   }

   public boolean isAssigneeDueDatePast(Date date, String time) throws Exception {

       boolean isPastDate= false;
       if(date != null && !"".equals(date) && time != null && !"".equals(time))
       {
       String hour=time.substring(0,time.indexOf(":"));
       String minute=time.substring((time.lastIndexOf(":"))-2,time.lastIndexOf(":"));
       String ampm=time.substring(time.indexOf(" ")+1,time.indexOf(" ")+3);

       if(ampm.equals("AM")){
           if(Integer.parseInt(hour) == 12){
               hour="0";
           }
       } else if(ampm.equals("PM")){
           // Here we are converting the 12 hour format time into 24 hour format
    	   // If the hour is from 1 to 11 PM, We are adding 12
    	   if(Integer.parseInt(hour) < 12){
        	   int hr = Integer.parseInt(hour)+12;
        	   hour = Integer.toString(hr);
       }
       }
       date.setHours(Integer.parseInt(hour));
       date.setMinutes(Integer.parseInt(minute));

       Date today = new Date();
       isPastDate = (date.equals(today) || date.after(today)) ? false : true;
       }
       return isPastDate;

   }

   public Vector getTaskRouteContentActions(Context context, String[] args) throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap paramMap = (HashMap)programMap.get("paramList");
       String objectId = (String)paramMap.get("objectId");
       String languageStr = (String)paramMap.get("languageStr");
       DomainObject inboxTaskObj = DomainObject.newInstance(context,objectId);
       String customSortDirections = (String)paramMap.get("customSortDirections");
       String uiType = (String)paramMap.get("uiType");
       String customSortColumns = (String)paramMap.get("customSortColumns");
       String table = (String)paramMap.get("table");
       String routeId = inboxTaskObj.getInfo(context, "from[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].to.id");
       Vector contentActions = new Vector();
       MapList objectList = (MapList)programMap.get("objectList");
	   if(UIUtil.isNullOrEmpty(routeId)){
    	    for (int i = 0; i < objectList.size(); i++)
    	    {
    	    	contentActions.add(DomainObject.EMPTY_STRING);
    	    }
    	    return contentActions;
       }
       Route routeObject = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
       String nextURL     = "";
       String target      = "";

       // Set the domain object id with rout id
       routeObject.setId(routeId);

       String sPolicy = "";
       String sStates = "";
       String sRotableIds ="";
       String stateValue = "";
       String sNoneValue=EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.AttachmentsDialog.none");
       String sDownloadTip = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.FileDownload.Download");
       String sViewerTip   = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.Viewer");


       Iterator contentItr = objectList.iterator();
       while(contentItr.hasNext())
       {
           StringList fileNames = new StringList();
           String fileName = "";
           StringBuffer strBuf = new StringBuffer();
           Map fileMap = (Map)contentItr.next();
           try{
               fileNames = (StringList) fileMap.get(routeObject.SELECT_FILE_NAME);
           } catch ( ClassCastException excep){
               fileName  = (String)fileMap.get(routeObject.SELECT_FILE_NAME);
               if(fileName != null && !"".equals(fileName) && !"null".equals(fileName)){
                   fileNames.addElement(fileName);
               }
           }

           boolean isActions = (fileNames != null) ? true : false;

           StringBuffer viewerURL    = new StringBuffer(256);
           String contentType = (String)fileMap.get(routeObject.SELECT_TYPE);
           String contentParentType = Document.getParentType(context,contentType);
		   String regstrdViewerURL = null;

           if(isActions && Document.TYPE_DOCUMENTS.equals(contentParentType)) {
//[IR-298882]:START
				if(fileNames.size() == 1){
					String tmpFormat = (String)fileMap.get(routeObject.SELECT_FILE_FORMAT);
					String tmpObjectID = (String)fileMap.get(routeObject.SELECT_ID);
					String tmpFileName = (String)fileMap.get(routeObject.SELECT_FILE_NAME);
					regstrdViewerURL = emxCommonFileUI_mxJPO.getViewerURL(context,tmpObjectID,tmpFormat,tmpFileName,"null",false);
				}
				//if registered viewer is not available for given format.
				if(UIUtil.isNullOrEmpty(regstrdViewerURL))
				{
					sViewerTip   = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.Viewer");
					viewerURL  = new StringBuffer("../components/emxCommonDocumentPreCheckout.jsp?objectId=");
					viewerURL.append(fileMap.get(routeObject.SELECT_ID));
					viewerURL.append("&action=view");
			    }
//[IR-298882]:END
           }

           sPolicy = (String) fileMap.get(routeObject.SELECT_ROUTE_BASEPOLICY);
           sPolicy=(String) PropertyUtil.getSchemaProperty(context,sPolicy);
           sStates = (String) fileMap.get(routeObject.SELECT_ROUTE_BASESTATE);

           if(!(sStates.indexOf('[') <0 ) ) {
               sStates = sStates.substring(sStates.indexOf('[')+1,sStates.indexOf(']'));
           }

           if(sRotableIds.equals("")) {
               sRotableIds = (String)fileMap.get(routeObject.SELECT_RELATIONSHIP_ID);
           } else {
               sRotableIds += "|" + (String)fileMap.get(routeObject.SELECT_RELATIONSHIP_ID);
           }
           stateValue = (String)fileMap.get(routeObject.SELECT_ROUTE_BASESTATE);
           if (!"Ad Hoc".equals(stateValue)){
               stateValue = FrameworkUtil.lookupStateName(context,sPolicy,stateValue);
           } else {
               stateValue = sNoneValue;
           }

           if(isActions && Document.TYPE_DOCUMENTS.equals(contentParentType))
           {
        	   if(!UINavigatorUtil.isMobile(context)) {
              strBuf.append("<a href='javascript:callCheckout(\"");
              strBuf.append(fileMap.get(routeObject.SELECT_ID));
              strBuf.append("\",\"download\", \"\", \"\",\"");
              strBuf.append(customSortColumns);
              strBuf.append("\", \"");
              strBuf.append(customSortDirections);
              strBuf.append("\", \"");
              strBuf.append(uiType);
              strBuf.append("\", \"");
              strBuf.append(table);
              strBuf.append("\"");
              strBuf.append(")'>");
              strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt='");
              strBuf.append(XSSUtil.encodeForHTMLAttribute(context,sDownloadTip));
              strBuf.append("' title='");
              strBuf.append(XSSUtil.encodeForHTMLAttribute(context,sDownloadTip));
              strBuf.append("'></a>&#160;");
        	   }
              if(fileNames.size() == 1){
//[IR-298882]:START
				if(UIUtil.isNullOrEmpty(regstrdViewerURL))
				{
					 strBuf.append("<a href='javascript:showModalDialog(\"");
					 strBuf.append(viewerURL.toString());
					 strBuf.append("\",575,575)'>");
					 strBuf.append("<img border='0' src='../common/images/iconActionView.gif' alt='");
                 strBuf.append(XSSUtil.encodeForHTMLAttribute(context,sViewerTip));
					 strBuf.append("' title='");
                 strBuf.append(XSSUtil.encodeForHTMLAttribute(context,sViewerTip));
					 strBuf.append("'></a>&#160;");
				}
				else
				{
					strBuf.append(regstrdViewerURL);
				}
//[IR-298882]:END
              }
           }
           contentActions.add(strBuf.toString());
       }
       return contentActions;
   }

   /**
    * getAllRouteTasks - gets the list of all Tasks
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - objectList MapList
    * @returns Object
    * @throws Exception if the operation fails
    * @since R211
    *
    *     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getAllRouteTasks(Context context, String[] args) throws Exception
   {

       return getRouteTasks(context,args, "All");

   }

   /**
    * getActiveRouteTasks - gets the list of Tasks in Active state
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - objectList MapList
    * @returns Object
    * @throws Exception if the operation fails
    * @since R211
    *
    *     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getActiveRouteTasks(Context context, String[] args) throws Exception
   {

       return getRouteTasks(context,args, "Active");

   }

   /**
    * getReviewRouteTasks - gets the list of Tasks in Review state
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - objectList MapList
    * @returns Object
    * @throws Exception if the operation fails
    * @since R211
    *
    *     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getReviewRouteTasks(Context context, String[] args) throws Exception
   {

       return getRouteTasks(context,args, "Needs Review");

   }

   /**
    * getRouteTasks - gets the list of Tasks in the Route
    * @param context the eMatrix <code>Context</code> object
    * @param args args holds the following input arguments:
    *        0 - objectList MapList
    * @param filter which holds filter value
    * @returns MapList
    * @throws Exception if the operation fails
    * @since R211
    * @grade 0
    */
   protected MapList getRouteTasks(Context context, String[] args, String filter) throws FrameworkException {
       try
       {
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           String routeId = (String)programMap.get("objectId");
           String languageStr = (String)programMap.get("languageStr");
           boolean isFilterAll = "All".equals(filter);
           boolean isFilterActive = "Active".equals(filter);
           boolean isFilterNeedsReview = "Needs Review".equals(filter);

           DomainObject domainObject = DomainObject.newInstance(context , routeId);

           StringList objectSelects= new StringList();
   			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
   			objectSelects.addElement("from[" + DomainObject.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE + "].to.attribute["+DomainObject.ATTRIBUTE_TASKEDIT_SETTING+"].value");
           Map objInfo = domainObject.getInfo(context, objectSelects);
           String routeState = (String)objInfo.get(DomainConstants.SELECT_CURRENT);
           String attr = (String)objInfo.get("from[" + DomainObject.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE + "].to.attribute["+DomainObject.ATTRIBUTE_TASKEDIT_SETTING+"].value");

           String strTaskEditSetting="";
           if (UIUtil.isNotNullAndNotEmpty(attr)){
                 strTaskEditSetting  = attr;
           }

           StringList objSelectables = new StringList(20);
           String taskAssigneeName  = "TaskAssingee";
           String selAsigneeName           = "from["+DomainObject.RELATIONSHIP_PROJECT_TASK+"].to.name";
           String relAsigneeName           = "to.name";
           String selTaskTitle           = getAttributeSelect(DomainObject.ATTRIBUTE_TITLE);
           String selTaskAction          = getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_ACTION);
           String selRouteNodeId         = getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_NODE_ID);
           String selTaskDueDate         = getAttributeSelect(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
           String selTaskCompletedDate   = getAttributeSelect(DomainObject.ATTRIBUTE_ACTUAL_COMPLETION_DATE);
           String selTaskComment         = getAttributeSelect(DomainObject.ATTRIBUTE_COMMENTS);
           String selTaskAllowDelegation = getAttributeSelect(DomainObject.ATTRIBUTE_ALLOW_DELEGATION);
           String selAssigneeDueDateOpt  = getAttributeSelect(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
           String selDueDateOffset       = getAttributeSelect(DomainObject.ATTRIBUTE_DUEDATE_OFFSET);
           String selDueDateOffsetFrom   = getAttributeSelect(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM);
           String selReviewTask          = getAttributeSelect(DomainObject.ATTRIBUTE_REVIEW_TASK);
           String selTemplateTask        = getAttributeSelect(DomainObject.ATTRIBUTE_TEMPLATE_TASK);
           String selRouteTaskUser       = getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_TASK_USER);
           String selTaskApprovalStatus  = getAttributeSelect(DomainObject.ATTRIBUTE_APPROVAL_STATUS);
           String selRouteSequence       = getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE);
           String selRouteInstructions   = getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS);
           String selParallelNodeProcessionRule = "attribute["+PropertyUtil.getSchemaProperty(context, "attribute_ParallelNodeProcessionRule")+"]";

           MapList taskList = null;
           if(!STATE_ROUTE_DEFINE.equals(routeState)) {
               objSelectables.addElement(SELECT_ID);
               objSelectables.addElement(SELECT_TYPE);
               objSelectables.addElement(SELECT_NAME);
               objSelectables.addElement(SELECT_REVISION);
               objSelectables.addElement(SELECT_OWNER);
               objSelectables.addElement(SELECT_CURRENT);
               objSelectables.addElement(selAsigneeName);
               objSelectables.addElement(selTaskTitle);
               objSelectables.addElement(selTaskAction);
               objSelectables.addElement(selTaskApprovalStatus);
               objSelectables.addElement(selRouteNodeId);
               objSelectables.addElement(selTaskDueDate);
               objSelectables.addElement(selTaskCompletedDate);
               objSelectables.addElement(selTaskComment);
               objSelectables.addElement(selTaskAllowDelegation);
               objSelectables.addElement(selAssigneeDueDateOpt);
               objSelectables.addElement(selDueDateOffset);
               objSelectables.addElement(selDueDateOffsetFrom);
               objSelectables.addElement(selReviewTask);
               objSelectables.addElement(selTemplateTask);
               objSelectables.addElement(selRouteTaskUser);

               taskList = domainObject.getRelatedObjects (context,
                       DomainObject.RELATIONSHIP_ROUTE_TASK, //relationshipPattern
                       DomainObject.TYPE_INBOX_TASK, //typePattern
                       objSelectables, null,
                       true, false,
                       (short)1,
                       null, null,
                       0,
                       null, null, null);
           } else {
               taskList = new MapList(0);
           }


           StringList relSelectables = new StringList(20);
           relSelectables.addElement(selTaskTitle);
           relSelectables.addElement(SELECT_NAME);
           relSelectables.addElement(selTaskAction);
           relSelectables.addElement(SELECT_TYPE);
           relSelectables.addElement(selTaskApprovalStatus);
           relSelectables.addElement(selRouteNodeId);
           relSelectables.addElement(selRouteSequence);
           relSelectables.addElement(selTaskDueDate);
           relSelectables.addElement(selTaskCompletedDate);
           relSelectables.addElement(selTaskComment);
           relSelectables.addElement(selTaskAllowDelegation);
           relSelectables.addElement(selAssigneeDueDateOpt);
           relSelectables.addElement(selDueDateOffset);
           relSelectables.addElement(selDueDateOffsetFrom);
           relSelectables.addElement(relAsigneeName);
           relSelectables.addElement(selReviewTask);
           relSelectables.addElement(selTemplateTask);
           relSelectables.addElement(selRouteTaskUser);
           relSelectables.addElement(selRouteInstructions);

           //Added for Bug No : 309522 Begin
           relSelectables.addElement(selParallelNodeProcessionRule);
           //Added for Bug No : 309522 End

           Pattern typePattern = new Pattern(DomainObject.TYPE_PERSON);
           typePattern.addPattern(DomainObject.TYPE_ROUTE_TASK_USER);

           //get all route-node rels connected to the route
           MapList routeNodeList =  domainObject.getRelatedObjects(context,
                                                       DomainObject.RELATIONSHIP_ROUTE_NODE, //relationshipPattern
                                                       typePattern.getPattern(), //typePattern
                                                       null, relSelectables,
                                                       false, true,
                                                       (short)1,
                                                       null, null,
                                                       0,
                                                       null, null, null);

           MapList mlTasksAssigned = new MapList();
           MapList mlTasksInReview = new MapList();
           MapList mlTasksCompleted = new MapList();
           MapList mlInactiveConnectedTasks = new MapList();
           MapList mlInactiveDisconnectedTasks = new MapList();
           MapList mlUninstantiatedTasks = new MapList();
           StringList instantiatedTasksNodeIds = new StringList();

           // Update the sequence and parallel node processing rule from route node relationships to the route tasks
           // because this information is not available on task objects
           updateInfoFromRouteNodes(context, taskList, routeNodeList);

           // Iterate over the tasks connected to route and seggregate the different tasks
           for (Iterator itrTasksList = taskList.iterator(); itrTasksList.hasNext();) {
               Map mapInfo = (Map)itrTasksList.next();
               String strTaskId = (String)mapInfo.get(SELECT_ID);
               String current = (String)mapInfo.get(SELECT_CURRENT);
               String assignee = (String) mapInfo.get(selAsigneeName);
               if(!UIUtil.isNullOrEmpty(assignee)){
               mapInfo.put(taskAssigneeName, assignee);
               }

               String stateAssigned = DomainConstants.STATE_INBOX_TASK_ASSIGNED;
               String stateReview = DomainConstants.STATE_INBOX_TASK_REVIEW;
               String stateTaskComplete = DomainConstants.STATE_INBOX_TASK_COMPLETE;
               String tempTask = (String)mapInfo.get(getAttributeSelect(DomainObject.ATTRIBUTE_TEMPLATE_TASK));
               if(current==null) current = "";

               if((current.equals("") && tempTask.equals("Yes") && !"Modify/Delete Task List".equalsIgnoreCase(strTaskEditSetting))
                       || current.equals(stateTaskComplete) || current.equals(stateAssigned) || current.equals(stateReview) ){
            	   mapInfo.put("disableSelection", "true");
               }
               else {
            	   mapInfo.put("disableSelection", "false");
               }

               // If the assignee found is null, means the task is not connected to the person object, its inactive task
               // Otherwise Depending on the state of the task, separate them as if they are active tasks which needs review
               if (assignee == null || assignee.equals("")) {
                   mlInactiveConnectedTasks.add(mapInfo);
               } else if (TYPE_INBOX_TASK_STATE_REVIEW.equals(current)) {
                   instantiatedTasksNodeIds.add(mapInfo.get(selRouteNodeId));
                   mlTasksInReview.add(mapInfo);
               } else if (TYPE_INBOX_TASK_STATE_ASSIGNED.equals(current)) {
                   instantiatedTasksNodeIds.add(mapInfo.get(selRouteNodeId));
                   mlTasksAssigned.add(mapInfo);
               } else {
                   instantiatedTasksNodeIds.add(mapInfo.get(selRouteNodeId));
                   mlTasksCompleted.add(mapInfo);
               }
               // For this task find the revisions and add the revisions to the separate list.
               // Take care that the current task should be eliminated from the found task revisions
               // This way the list will contains the tasks which are not connected but are previous revisions

               // Update following values in the new Map list
               // level - > Add level value as 1 to this list else sorting will give problem!
               // inactiveDisconnected - > Used while forming assignee name
               // selRouteNodeId - > Update the route node id from the latest task,
               //                   This rel id has changed because of the Resume Process revised the tasks
               //If there is only one rev (that is the current task then noting to update just continue with other task.


               DomainObject dmoTask = new DomainObject(strTaskId);
               MapList mlTaskRevisions = dmoTask.getRevisionsInfo(context, objSelectables, new StringList());
               if(mlTaskRevisions.size() == 1)
                      continue;
               for (Iterator itrTaskRevisions = mlTaskRevisions.iterator(); itrTaskRevisions.hasNext();) {
                   Map mapRevisionInfo = (Map)itrTaskRevisions.next();
                   String strRevisionTaskId = (String)mapRevisionInfo.get(SELECT_ID);
                   if (strTaskId != null && !strTaskId.equals(strRevisionTaskId)) {
                	   current = (String)mapRevisionInfo.get(SELECT_CURRENT);
                	   tempTask = (String)mapRevisionInfo.get(getAttributeSelect(DomainObject.ATTRIBUTE_TEMPLATE_TASK));
                	   if((UIUtil.isNotNullAndNotEmpty(current) && tempTask.equals("Yes") && !"Modify/Delete Task List".equalsIgnoreCase(strTaskEditSetting))
                               || current.equals(stateTaskComplete) || current.equals(stateAssigned) || current.equals(stateReview) ){
                		   mapRevisionInfo.put("disableSelection", "true");
                       }
                       else {
                    	   mapRevisionInfo.put("disableSelection", "false");
                       }
                       mapRevisionInfo.put("level", "1");
                       mapRevisionInfo.put("inactiveDisconnected", "true");
                       mapRevisionInfo.put(selRouteNodeId, mapInfo.get(selRouteNodeId));
                       mlInactiveDisconnectedTasks.add(mapRevisionInfo);
                   }
               }
           }

           // Update the sequence and parallel node processing rule from route node relationships to the disconnected tasks revisions
           // because this information is not available on task objects
           updateInfoFromRouteNodes(context, mlInactiveDisconnectedTasks, routeNodeList);

           // Once we have categorized the tasks objects, now find out the uninstantiated task objects
           for (Iterator itrRouteNodeList = routeNodeList.iterator(); itrRouteNodeList.hasNext();) {
               Map mapRelInfo = (Map)itrRouteNodeList.next();
               String strRelRouteNodeID = (String)mapRelInfo.get(selRouteNodeId);

               //Ensure that No task is created for this Route Node
               boolean isTaskInstantiated = instantiatedTasksNodeIds.contains(strRelRouteNodeID);

               if (!isTaskInstantiated) {
                   mapRelInfo.put(taskAssigneeName, mapRelInfo.get(relAsigneeName));
                   mapRelInfo.put(SELECT_ID, strRelRouteNodeID);
                   mlUninstantiatedTasks.add(mapRelInfo);
               }
            }

           MapList taskDisplayList = new MapList();
           // Form the final list of tasks to be shown
           if (isFilterAll) {
               taskDisplayList.addAll(mlTasksAssigned);
               taskDisplayList.addAll(mlTasksInReview);
               taskDisplayList.addAll(mlTasksCompleted);
               taskDisplayList.addAll(mlUninstantiatedTasks);
           } else if (isFilterActive) {
               taskDisplayList.addAll(mlTasksAssigned);
               taskDisplayList.addAll(mlTasksInReview);
           } else if (isFilterNeedsReview) {
               taskDisplayList.addAll(mlTasksInReview);
           }

           // Now process this final list of task display, for some parameters like owner name etc.
           for (Iterator itrDisplayTasks = taskDisplayList.iterator(); itrDisplayTasks.hasNext();) {
               Map mapInfo = (Map)itrDisplayTasks.next();

               // Adjustments for the owner name
               String assingee = (String)mapInfo.get(taskAssigneeName);
               String routeTaskUser = (String)mapInfo.get(selRouteTaskUser);
               String strTaskRevision = (String)mapInfo.get(SELECT_REVISION);

               if (strTaskRevision == null) {
                   mapInfo.put(SELECT_REVISION, "");
               }

               if(assingee != null && !"".equals(assingee)) {
                   String fullName = PersonUtil.getFullName(context, assingee);
                   if(!fullName.equals(assingee))
                       mapInfo.put(SELECT_OWNER, fullName);
                   else if (routeTaskUser != null && !"".equals(routeTaskUser)) {
                       String isRoleGroup = routeTaskUser.substring(0,routeTaskUser.indexOf("_"));
                       if("role".equals(isRoleGroup)) {
                         mapInfo.put(SELECT_OWNER, i18nNow.getAdminI18NString("Role",PropertyUtil.getSchemaProperty(context, routeTaskUser),languageStr)+"("+EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.Role")+")");
                       } else if ("group".equals(isRoleGroup)) {
                        mapInfo.put(SELECT_OWNER, i18nNow.getAdminI18NString("Group", PropertyUtil.getSchemaProperty( context,routeTaskUser),languageStr)+"("+EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.Group")+")");
                       }
                   }
               } else if ("true".equalsIgnoreCase((String)mapInfo.get("inactiveDisconnected"))) {
                   mapInfo.put(SELECT_OWNER, PersonUtil.getFullName(context, (String)mapInfo.get(SELECT_OWNER)));
               }
               if (mapInfo.get(SELECT_OWNER) == null) {
                   mapInfo.put(SELECT_OWNER, "");
               }
           }
           return taskDisplayList;
       } catch (Exception ex) {
           throw new FrameworkException(ex);
       }
   }

   /**
    * Updates certain information from route node list to task list. This information is not available on tasks.
    * Here sequence and parallel node processing rule is being updated.
    *
    * @param context The Matrix Context object
    * @param mlTasks The list of tasks
    * @param mlRouteNodes The list of route node relationships
    */
   private static void updateInfoFromRouteNodes(Context context, MapList mlTasks, MapList mlRouteNodes) {
       if (mlTasks == null || mlRouteNodes == null) {
           return;
       }
       Map mapTaskInfo = null;
       Map mapRouteNodeInfo = null;
       String strRouteNodeIDOnTask = null;
       String strRouteNodeIDOnRel = null;
       String strRouteSequence = null;
       String strParallelProcessingRule = null;


       final String SELECT_ROUTE_NODE_ID = "attribute["+DomainObject.ATTRIBUTE_ROUTE_NODE_ID+"]";
       final String SELECT_ROUTE_SEQUENCE = "attribute["+DomainObject.ATTRIBUTE_ROUTE_SEQUENCE+"]";
       final String SELECT_PARALLEL_NODE_PROCESSING_RULE = "attribute["+PropertyUtil.getSchemaProperty(context, "attribute_ParallelNodeProcessionRule")+"]";

       // Iterate on each task in list
       for (Iterator itrTasks = mlTasks.iterator(); itrTasks.hasNext();) {
           mapTaskInfo = (Map)itrTasks.next();

           strRouteNodeIDOnTask = (String)mapTaskInfo.get(SELECT_ROUTE_NODE_ID);

           // Iterate on each route node relationship and find if the matching rel exists
           for (Iterator itrRels = mlRouteNodes.iterator(); itrRels.hasNext();) {
               mapRouteNodeInfo = (Map)itrRels.next();

               strRouteNodeIDOnRel = (String)mapRouteNodeInfo.get(SELECT_ROUTE_NODE_ID);
               if (strRouteNodeIDOnTask != null && strRouteNodeIDOnTask.equals(strRouteNodeIDOnRel)) {

                   strRouteSequence = (String)mapRouteNodeInfo.get(SELECT_ROUTE_SEQUENCE);
                   strParallelProcessingRule = (String)mapRouteNodeInfo.get(SELECT_PARALLEL_NODE_PROCESSING_RULE);

                   mapTaskInfo.put(SELECT_ROUTE_SEQUENCE, strRouteSequence);
                   mapTaskInfo.put(SELECT_PARALLEL_NODE_PROCESSING_RULE, strParallelProcessingRule);
                   break;
               }
           }//for
       }//for
   }

   public Vector showRouteTaskName(Context context, String[] args) throws Exception
   {
       Vector taskNameVec = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Map paramList      = (Map)programMap.get("paramList");
       String routeId = (String)paramList.get("objectId");
       String isTypeRouteTemplate="";

       if(routeId!=null)
       {
       DomainObject domainObject = new DomainObject(routeId);
       isTypeRouteTemplate = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
         }

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskState = (String)objectMap.get(DomainObject.SELECT_CURRENT);
           String taskId = (String)objectMap.get(DomainObject.SELECT_ID);
           String taskName = (String)objectMap.get(DomainObject.SELECT_NAME);

           if(isTypeRouteTemplate.equals(DomainConstants.TYPE_ROUTE_TEMPLATE))
           {
        	    taskName = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_TITLE));
           }


           String tempTask = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_TEMPLATE_TASK));
           String routeNodeId         = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_NODE_ID));
           String popTreeUrl = "";
           String popWindowTarget="";
           StringBuffer sb = new StringBuffer();
           sb.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
           boolean isPrinterFriendly = (paramList.get("reportFormat") != null) ? true : false;

           if(!DomainConstants.RELATIONSHIP_ROUTE_NODE.equals(taskName) && !(isTypeRouteTemplate.equals(DomainConstants.TYPE_ROUTE_TEMPLATE))) {
               popTreeUrl  = "../common/emxTree.jsp?mode=insert&amp;objectId="+taskId;
               popWindowTarget="content";
           }
           else {
               StringBuffer buffer = new StringBuffer(200);
               buffer.append("../common/emxForm.jsp?");
               buffer.append("objectId=").append(routeId.trim()).append("&amp;");
               buffer.append("relId=").append(routeNodeId).append("&amp;");
               buffer.append("form=").append("APPRouteNodeTask").append("&amp;");
               buffer.append("suiteKey=").append("Components").append("&amp;");
               buffer.append("formHeader=").append("emxComponents.Task.Properties").append("&amp;");
               buffer.append("HelpMarker=").append("emxhelptaskproperties").append("&amp;");
               buffer.append("toolbar=").append("APPRoleNodeTaskActionsToolBar");
               popTreeUrl  = "javascript:showModalDialog('" + buffer.toString() + "', 800,575)";
               popWindowTarget="";
           }

           if (taskName!=null && !taskName.equals("")){

               if(tempTask != null && tempTask.equals("Yes") && !isPrinterFriendly){

                   sb.append("<a href=\"").append(popTreeUrl).append("\" target=\"").
                   append(popWindowTarget).append("\"><img src=\"../common/images/iconSmallTask.gif\" name=\"imgTask\" border=\"0\"/>").
                   append(XSSUtil.encodeForHTML(context,taskName)).append("(t) </a>");

               }else if(!isPrinterFriendly){

                   sb.append("<a href=\"").append(popTreeUrl).append("\" target=\"").
                   append(popWindowTarget).append("\"><img src=\"../common/images/iconSmallTask.gif\" name=\"imgTask\" border=\"0\"/>").
                   append(XSSUtil.encodeForHTML(context,taskName)).append(" </a>");

               } else {
                   sb.append("<img src=\"../common/images/iconSmallTask.gif\" name=\"imgTask\" border=\"0\"/>").append(XSSUtil.encodeForHTML(context,taskName));
               }
           }
           else{

               sb.append("&#160;");

           }
           taskNameVec.add(sb.toString());
       }
       return taskNameVec;
   }

   public Vector showRouteTaskSequence(Context context, String[] args) throws Exception
   {
       Vector sequenceVector = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskState = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE));
           sequenceVector.add(taskState);
       }

       return sequenceVector;
   }


   public Vector showRouteTaskRevision(Context context, String[] args) throws Exception
   {
       Vector revisionVector = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskRev = (String)objectMap.get(DomainObject.SELECT_REVISION);
           revisionVector.add(taskRev);
       }

       return revisionVector;
   }

   public Vector showRouteTaskAssignee(Context context, String[] args) throws Exception
   {
       Vector taskAssigneeVec = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskAssignee = (String)objectMap.get(DomainObject.SELECT_OWNER);
           taskAssigneeVec.add(taskAssignee);
       }

       return taskAssigneeVec;
   }

   public Vector showRouteTaskAction(Context context, String[] args) throws Exception
   {
       Vector taskActionVec = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Map paramList      = (Map)programMap.get("paramList");
       String sLanguage = (String)paramList.get("languageStr");

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskAction = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_ACTION));
           taskActionVec.add(i18nNow.getRangeI18NString( DomainObject.ATTRIBUTE_ROUTE_ACTION, taskAction,sLanguage));
       }

       return taskActionVec;
   }

   public Vector showRouteTaskApprStatus(Context context, String[] args) throws Exception
   {
       Vector approvalStatus = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Map paramList      = (Map)programMap.get("paramList");
       String sLanguage = (String)paramList.get("languageStr");

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskAction = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_ACTION));
           String taskApprovalStatus = (taskAction.equals("Approve")) ? (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_APPROVAL_STATUS)) : "";
           approvalStatus.add(i18nNow.getRangeI18NString( DomainObject.ATTRIBUTE_APPROVAL_STATUS, taskApprovalStatus,sLanguage));
       }

       return approvalStatus;
   }

   public Vector showRouteTaskComments(Context context, String[] args) throws Exception
   {
       Vector taskCommentsVec = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskComments = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_COMMENTS));
           taskCommentsVec.add(taskComments);
       }

       return taskCommentsVec;
   }

   public Vector showRouteTaskInstructions(Context context, String[] args) throws Exception
   {
       Vector taskInstr = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskInstructions = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS));
           taskInstr.add(taskInstructions);
       }

       return taskInstr;
   }

   public Vector showTaskCompletionDate(Context context, String[] args) throws Exception
   {
       Vector completionDate = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Map paramList = (Map)programMap.get("paramList");
       String timeZone = (String)paramList.get("timeZone");
       double clientTZOffset = (new Double(timeZone)).doubleValue();

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskCompletionDate = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_ACTUAL_COMPLETION_DATE));
           taskCompletionDate = eMatrixDateFormat.getFormattedDisplayDateTime(context,
                   taskCompletionDate, true,DateFormat.MEDIUM, clientTZOffset,context.getLocale());
           completionDate.add(taskCompletionDate);
       }

       return completionDate;
   }

   public Vector showTaskState(Context context, String[] args) throws Exception
   {
       Vector stateVector = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Map paramList      = (Map)programMap.get("paramList");
       String languageStr = (String)paramList.get("languageStr");

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskState = (String)objectMap.get(DomainObject.SELECT_CURRENT);
           String taskId = (String)objectMap.get(DomainObject.SELECT_ID);
           StringBuffer sb = new StringBuffer();
           if( null == taskState){
               sb.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.TaskSummary.StateName"));
                 }
                 else{
                   BusinessObject busObj = new BusinessObject(taskId);
                   busObj.open(context);
                  sb.append(i18nNow.getStateI18NString(busObj.getPolicy().getName(),taskState, languageStr));

                   busObj.close(context);
                 }
           stateVector.add(sb.toString());
       }

       return stateVector;
   }
   public Vector showAssigneeIcon(Context context, String[] args) throws Exception
   {
       Vector showAssigneeIcon = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       HashMap paramList = (HashMap)programMap.get("paramList");
       String languageStr =  (String)paramList.get("languageStr");
       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           String taskAllowDelegation = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_ALLOW_DELEGATION));
           StringBuffer imageSB = new StringBuffer();
           if("TRUE".equalsIgnoreCase(taskAllowDelegation)){
               imageSB.append("<img src=\"../common/images/iconAssignee.gif\" name=\"imgTask\" id=\"imgTask\" alt=\"");
               imageSB.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.TaskSummary.ToolTipAssignable")).append("\"/>");
           }
           showAssigneeIcon.add(imageSB.toString());
       }
       //XSSOK
       return showAssigneeIcon;
   }

   public Vector showOwnerReviewIcon(Context context, String[] args) throws Exception
   {
       Vector showOwnerReviewIcon = new Vector();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Map paramList = (Map)programMap.get("paramList");
       String languageStr =  (String)paramList.get("languageStr");
       String routeId =  (String)paramList.get("objectId");
       DomainObject domainObject = DomainObject.newInstance(context , routeId);
       String sTypeName = domainObject.getInfo(context, domainObject.SELECT_TYPE);

       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext())
       {
           Map objectMap = (Map) objectListItr.next();
           StringBuffer imageSB = new StringBuffer();
           String reviewTask = (String)objectMap.get(getAttributeSelect(DomainObject.ATTRIBUTE_REVIEW_TASK));
           if(DomainObject.TYPE_ROUTE.equals(sTypeName) && "Yes".equalsIgnoreCase(reviewTask)){
               imageSB.append("<img src=\"../common/images/iconSmallOwnerReview.gif\" name=\"imgTask\" id=\"imgTask\" alt=\"");
               imageSB.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.TaskSummary.ToolTipOwnerReview")).append("\"/>");
           }
           showOwnerReviewIcon.add(imageSB.toString());
       }
       //XSSOK
       return showOwnerReviewIcon;
   }


   public boolean showRouteTaskContent(Context context, String[] args) throws Exception
   {
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       String showContent =  (String)programMap.get("showRouteContent");
       return (null != showContent && showContent.equals("false")) ? false : true;
   }

   public String getRouteStoppedMessage(Context context, String[] args) throws FrameworkException {
       try {
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String objectId = (String)programMap.get("objectId");
           String languageStr = (String)programMap.get("languageStr");
           objectId = objectId != null ? objectId : (String)requestMap.get("objectId");

           String SELECT_ROUTE_ID = "from[" + RELATIONSHIP_ROUTE_TASK + "].to.id";

           StringList selectList = new StringList(4);
           selectList.add(SELECT_ROUTE_ID);
           selectList.add(SELECT_OWNER);

           DomainObject doInboxTask = DomainObject.newInstance(context,objectId);
           Map taskMap = doInboxTask.getInfo(context,selectList);

           if (taskMap.get(SELECT_ROUTE_ID) == null) {
               DomainObject dmoLastRevision = new DomainObject(doInboxTask.getLastRevision(context));
               String strRouteId = dmoLastRevision.getInfo(context, SELECT_ROUTE_ID);
               taskMap.put(SELECT_ROUTE_ID, strRouteId);
           }

           String strRouteId = (String) taskMap.get(SELECT_ROUTE_ID);
           String SELECT_ATTRIBUTE_ROUTE_STATUS  = "attribute[" + ATTRIBUTE_ROUTE_STATUS + "]";
           StringList routeInfoSel = new StringList(2);
           routeInfoSel.add(SELECT_CURRENT);
           routeInfoSel.add(SELECT_ATTRIBUTE_ROUTE_STATUS);


           DomainObject routeObj = DomainObject.newInstance(context, strRouteId);
           Map routeInfo = routeObj.getInfo(context, routeInfoSel);

           if("Stopped".equals(routeInfo.get(SELECT_ATTRIBUTE_ROUTE_STATUS)) && isRouteStoppedDueToRejection(context, strRouteId)) {
               String attrComments = "attribute[" + DomainConstants.ATTRIBUTE_COMMENTS + "]";

               StringList busSelect = new StringList(2);
               busSelect.add(DomainConstants.SELECT_OWNER);
               busSelect.add(attrComments);

               DomainObject dObj = new DomainObject(strRouteId);
               String objectWhere = "attribute["+ DomainConstants.ATTRIBUTE_APPROVAL_STATUS +"] == 'Reject'";
               MapList mapTaskRejected = dObj.getRelatedObjects(context,
                       RELATIONSHIP_ROUTE_TASK, TYPE_INBOX_TASK,
                       busSelect, new StringList(), true, false,(short)1, objectWhere, "");

               if(mapTaskRejected.size()>0) {
                   Map map = (Map)mapTaskRejected.get(0);
                   String rejectUser = (String)map.get(DomainConstants.SELECT_OWNER);
                   String reason = (String) map.get(attrComments);

                   StringBuffer sb = new StringBuffer();
                   sb.append("<font color=red><b>");
                   sb.append(ComponentsUIUtil.getI18NString(context, languageStr, "emxComponents.InboxTask.RouteStoppedMessage", new String[] {rejectUser, XSSUtil.encodeForHTML(context, reason)}));
                   sb.append("</b></font>");
                   return sb.toString();
               } else {
                   return EMPTY_STRING;
               }
           }
           return EMPTY_STRING;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   protected boolean isRouteStoppedDueToRejection(Context context, String strRouteId) throws FrameworkException {
       try {

           boolean isRouteStoppedDueToRejection = true;

           final String ATTRIBUTE_CURRENT_ROUTE_NODE = PropertyUtil.getSchemaProperty(context, "attribute_CurrentRouteNode");
           final String SELECT_CURRENT_ROUTE_NODE = "attribute[" + ATTRIBUTE_CURRENT_ROUTE_NODE + "]";
           final String SELECT_ROUTE_NODE_ID = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";
           final boolean GET_TO = true;
           final boolean GET_FROM = true;

           Route objRoute = (Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
           objRoute.setId(strRouteId);

           StringList slBusSelect = new StringList();
           slBusSelect.add(SELECT_CURRENT_ROUTE_NODE);

           String strCurrentRouteNode = objRoute.getInfo(context, SELECT_CURRENT_ROUTE_NODE);

           slBusSelect.clear();
           StringList slRelSelect = new StringList();
           slRelSelect.add(SELECT_ROUTE_NODE_ID);

           String strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
           String strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]==" + strCurrentRouteNode;
           short nRecurseLevel = (short)1;

           MapList mlRouteNodes = objRoute.getRelatedObjects(context, strRelPattern, "*", slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseLevel, "", strRelWhere);
           if (mlRouteNodes == null || mlRouteNodes.size() == 0) {
               String[] formatArgs = {strCurrentRouteNode};
               String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.InboxTask.NoTaskAtLevel",formatArgs);
               throw new FrameworkException(message);
           }

           StringBuffer sbufMatchList = new StringBuffer(64);
           for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
               Map mapRouteNode = (Map)itrRouteNodes.next();
               if (sbufMatchList.length() > 0) {
                   sbufMatchList.append(",");
               }
               sbufMatchList.append(mapRouteNode.get(SELECT_ROUTE_NODE_ID));
           }

           slBusSelect = new StringList(DomainObject.SELECT_ID);
           strRelPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
           String strBusWhere = "attribute[" + DomainObject.ATTRIBUTE_APPROVAL_STATUS + "]==\"Reject\" && attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "] matchlist \"" + sbufMatchList.toString() +"\" \",\"";

           MapList mlTasks = objRoute.getRelatedObjects(context, strRelPattern, DomainObject.TYPE_INBOX_TASK, slBusSelect, new StringList(), GET_TO, !GET_FROM, nRecurseLevel, strBusWhere, "");

           if (mlTasks == null || mlTasks.size() == 0) {
               isRouteStoppedDueToRejection = false;
           }

           return isRouteStoppedDueToRejection;

       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   /**
    * Shows the Assignee Selection radio option in Change Assignee Page
    *
    * @param context The Matrix Context object
    * @param args holds paramMap
    * @returns HashMap
    * @throws Exception if the operation fails
    * @since R212
    */

   public HashMap getChangeAssigneeRangeValues(Context context, String[] args) throws Exception {
       try {
			String isResponsibleRoleEnabled = DomainConstants.EMPTY_STRING;


		
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap paramMap = (HashMap)programMap.get("paramMap");
           String languageStr = (String) paramMap.get("languageStr");

           // initialize the return variable HashMap tempMap = new HashMap();
           HashMap tempMap = new HashMap();

           // initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
           StringList fieldRangeValues = new StringList();
           StringList fieldDisplayRangeValues = new StringList();

           String i18nPerson = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.Person");
           String i18nGroup = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.Group");

           fieldRangeValues.addElement("Person");
           fieldRangeValues.addElement("Group");

           // Add the internationlized value of the range values to fieldDisplayRangeValues
           fieldDisplayRangeValues.addElement(i18nPerson);
           fieldDisplayRangeValues.addElement(i18nGroup);
			String isFDAEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.EnableFDA");
			try{
			isResponsibleRoleEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.ResponsibleRoleForSignatureMeaning.Preserve");
			if(UIUtil.isNotNullAndNotEmpty(isFDAEnabled) && isFDAEnabled.equalsIgnoreCase("true") && UIUtil.isNotNullAndNotEmpty(isResponsibleRoleEnabled) && isResponsibleRoleEnabled.equalsIgnoreCase("true"))
			{
				String strInboxTaskId = (String) paramMap.get("objectId");
				DomainObject dObjInboxTask = DomainObject.newInstance(context, strInboxTaskId);
				String strRouteTaskUser = dObjInboxTask.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_TASK_USER);
				if(UIUtil.isNotNullAndNotEmpty(strRouteTaskUser) && strRouteTaskUser.startsWith("role_"))
				{
					fieldRangeValues.remove("Group");
					fieldDisplayRangeValues.remove(i18nGroup);
				}
			}
			}
			catch(Exception e){
				isResponsibleRoleEnabled = "false";
			}

           tempMap.put("field_choices", fieldRangeValues);
           tempMap.put("field_display_choices", fieldDisplayRangeValues);

           return tempMap;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   /**
    * Reassigns the task to the selected group. Creates teh RTU object and connect to the Route and Inbox task
    *
    * @param context The Matrix Context object
    * @returns nothing
    * @throws Exception if the operation fails
    * @since R212
    */
   public void reAssignToGroup(Context context,  String newTaskAssignee, DomainObject inboxTaskObj, String sRouteId, String languageStr) throws Exception {
       try {
               String groupSymbolicName = FrameworkUtil.getAliasForAdmin(context, "Group", newTaskAssignee, true);

               DomainObject routeObject = new DomainObject(sRouteId);

               // Route.getRouteTaskUserObject() will check if any RTU is already connected to Route or not.
               // If already connected then return the RTU object else creates and returns
               // The 3rd parameter boolean true ensures to create a object if not exist
               DomainObject rtaskUser = Route.getRouteTaskUserObject(context, routeObject, true);

               Map taskInfo = getInboxTaskInfo(context, inboxTaskObj);

               // Change "Project Task" connection between InboxTask and previous assignee to the new delegator.
               String connectionId = (String) taskInfo.get(SELECT_TASK_ASSIGNEE_CONNECTION);
               DomainRelationship.setToObject(context, connectionId, rtaskUser);

               //get the Route Node Id from inbox task
               connectionId = (String) taskInfo.get(SELECT_ROUTE_NODE_ID);

               // get the route id for this inbox task
               String routeId = (String) taskInfo.get(routeIdSelectStr);
               Route tempRouteObject = (Route)DomainObject.newInstance(context, routeId);

               //Get the correct relId for the RouteNodeRel given the attr routeNodeId from the InboxTask.
               connectionId = tempRouteObject.getRouteNodeRelId(context, connectionId);

               DomainRelationship relRouteNode = new DomainRelationship(connectionId);
               relRouteNode.open(context);

               MapList items = new MapList();
               try {
                   //push context since the task owner might have only Read access
                   ContextUtil.pushContext(context);

                   // Change "RouteNode" connection between the Route and previous assignee to the new delegator.
                   DomainRelationship.setToObject(context, connectionId, rtaskUser);

                   // Change owner of existing object to the new Assignee
                   inboxTaskObj.setOwner(context, newTaskAssignee);

                   // Set the value of RTU attribute with the group symbolic name on route node relationship
                   relRouteNode.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_TASK_USER, groupSymbolicName);

                   // Set the value of RTU attribute with the group symbolic name on inbox task object
                   inboxTaskObj.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_TASK_USER, groupSymbolicName);
               } catch (Exception ex) {
                   throw (new FrameworkException(ex));
               } finally {
                   ContextUtil.popContext(context);
               }

               String currentTaskOwner = (String) taskInfo.get(SELECT_TASK_ASSIGNEE_NAME);

               //grant access to the new assignee
               Route.grantAccessToNewAssignee(context, routeObject, currentTaskOwner, newTaskAssignee);

               // Send notification to the group members
               sendNotificationToGroup(context, newTaskAssignee, taskInfo, routeObject, languageStr);
           } catch(Exception e){
               throw new FrameworkException(e.getMessage());
           }
    }
   /**
    * Updates the assignee for the Inbox Task.
    *
    * @param context The Matrix Context object
    * @returns Hashmap
    * @throws Exception if the operation fails
    * @since R212
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public HashMap updateAssignee(Context context, String[] args) throws Exception {

       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       String languageStr = (String)requestMap.get("languageStr");
       String timeZone = (String)requestMap.get("timeZone");
       String taskId    = (String)requestMap.get("objectId");
       String reassignComments    = (String)requestMap.get("ReassignComments");
       String taskScheduledDate    = (String)requestMap.get("DueDate");
       String assigneeDueTime    = (String)requestMap.get("routeTime");
       String newTaskAssignee = (String)requestMap.get("NewAssignee");
       Locale locale = (Locale)requestMap.get("localeObj");

       // To see if the new assignee is a person or group
       String cmd = MqlUtil.mqlCommand(context, "print user \"" + newTaskAssignee + "\" select isaperson isagroup dump |");
       boolean isPerson = "TRUE|FALSE".equalsIgnoreCase(cmd);
       boolean isGroup = "FALSE|TRUE".equalsIgnoreCase(cmd);

       HashMap resultsMap = new HashMap();

       InboxTask inboxTaskObj = (InboxTask)DomainObject.newInstance(context,DomainConstants.TYPE_INBOX_TASK);
       if(taskId != null && !"".equals(taskId)){
           inboxTaskObj.setId(taskId);
       }

       String routeId = inboxTaskObj.getInfo(context, "from[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].to.id");

       if (!UIUtil.isNullOrEmpty(taskScheduledDate)) {
           double clientTZOffset = (new Double(timeZone)).doubleValue();
           taskScheduledDate     =  eMatrixDateFormat.getFormattedInputDateTime(context,taskScheduledDate,assigneeDueTime,clientTZOffset, locale);
       }

           // Get the old assignee
           String sTaskOldAssignee = inboxTaskObj.getInfo(context, "from[" + DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.name");

           String sRTUAttrValue = inboxTaskObj.getAttributeValue(context, DomainConstants.ATTRIBUTE_ROUTE_TASK_USER);
           sTaskOldAssignee = !"".equalsIgnoreCase(sRTUAttrValue) ? PropertyUtil.getSchemaProperty(context, sRTUAttrValue) : sTaskOldAssignee;

           // Check if both old assignee and new assignee are same then alert the end user
           if(newTaskAssignee.equals(sTaskOldAssignee)) {
               resultsMap.put("Message", EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.ChangeAssignee.NewAssignee"));
               return resultsMap;
           }

           Route route = (Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
           String allowDelegation = (String)inboxTaskObj.getInfo(context,getAttributeSelect(ATTRIBUTE_ALLOW_DELEGATION));
           String taskRouteAction = (String)inboxTaskObj.getInfo(context,getAttributeSelect(ATTRIBUTE_ROUTE_ACTION));

           String sTaskOldAssigneeId = inboxTaskObj.getInfo(context, "from[" + DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.id");
           DomainObject dmoLastRevision = new DomainObject(inboxTaskObj.getLastRevision(context));
           String sRouteId = dmoLastRevision.getInfo(context, "from[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].to.id");
           if("TRUE".equalsIgnoreCase(allowDelegation)){
              inboxTaskObj.setAttributeValue(context,inboxTaskObj.ATTRIBUTE_SCHEDULED_COMPLETION_DATE, taskScheduledDate);
           }

           reassignComments   = (reassignComments == null || "".equals(reassignComments.trim()) || "null".equals(reassignComments)) ? "" : reassignComments;
           // If the selected new Assignee  is a group then invoke reAssignToGroup()
           if(isGroup) {
               reAssignToGroup(context, newTaskAssignee, inboxTaskObj, sRouteId, languageStr);
           }
           // If the selected new Assignee is a person
           if(isPerson) {
           String sTaskNewAssigneeId = !UIUtil.isNullOrEmpty(newTaskAssignee) ? PersonUtil.getPersonObjectID(context, newTaskAssignee) : "";

           if(!UIUtil.isNullOrEmpty(sTaskNewAssigneeId)&& !UIUtil.isNullOrEmpty(sTaskOldAssigneeId) && !sTaskNewAssigneeId.equals(sTaskOldAssigneeId)) {
                 // For delegation functionality the Allow Delegation attribute should be TRUE. So we will do the same momentarily if the Allow Delegation is No
                 boolean isAllowedDelegationMomentarily = false;
                 if (!"True".equalsIgnoreCase(allowDelegation)) {
                     isAllowedDelegationMomentarily = true;
                     inboxTaskObj.setAttributeValue(context, inboxTaskObj.ATTRIBUTE_ALLOW_DELEGATION, "TRUE");
                 }

                //////////////////////////// START ABSENCE DELEGATION HANDLING/////////////////////////////
                // Check if the new assignee has configured absence delegation and he is absent
                if ("Yes".equalsIgnoreCase(allowDelegation)) {
                    final String ATTRIBUTE_ABSENCE_DELEGATE = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceDelegate");
                    final String ATTRIBUTE_ABSENCE_END_DATE = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceEndDate");
                    final String ATTRIBUTE_ABSENCE_START_DATE = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceStartDate");
                    final String SELECT_ATTRIBUTE_ABSENCE_DELEGATE = "attribute[" + ATTRIBUTE_ABSENCE_DELEGATE + "]";
                    final String SELECT_ATTRIBUTE_ABSENCE_END_DATE = "attribute[" + ATTRIBUTE_ABSENCE_END_DATE + "]";
                    final String SELECT_ATTRIBUTE_ABSENCE_START_DATE = "attribute[" + ATTRIBUTE_ABSENCE_START_DATE + "]";

                    SimpleDateFormat dateFormat = new SimpleDateFormat (eMatrixDateFormat.getInputDateFormat(), context.getLocale());
                    StringList slBusSelect = new StringList();
                    slBusSelect.addElement(SELECT_ATTRIBUTE_ABSENCE_DELEGATE);
                    slBusSelect.addElement(SELECT_ATTRIBUTE_ABSENCE_START_DATE);
                    slBusSelect.addElement(SELECT_ATTRIBUTE_ABSENCE_END_DATE);

                    Vector vecAlreadyVisitedNewAssignees = new Vector();

                    while (!vecAlreadyVisitedNewAssignees.contains(sTaskNewAssigneeId)) {
                        DomainObject dmoNewAssignee = new DomainObject(sTaskNewAssigneeId);
                        Map mapNewAssigneeInfo = dmoNewAssignee.getInfo(context, slBusSelect);

                        String strAbsenceDelegate = (String) mapNewAssigneeInfo.get(SELECT_ATTRIBUTE_ABSENCE_DELEGATE);
                        String strAbsenceStartDate = (String) mapNewAssigneeInfo.get(SELECT_ATTRIBUTE_ABSENCE_START_DATE);
                        String strAbsenceEndDate = (String) mapNewAssigneeInfo.get(SELECT_ATTRIBUTE_ABSENCE_END_DATE);

                        // If the absence delegation is configured then
                        if (strAbsenceDelegate != null
                                && !"".equals(strAbsenceDelegate)
                                && strAbsenceStartDate != null
                                && !"".equals(strAbsenceStartDate)
                                && strAbsenceEndDate != null
                                && !"".equals(strAbsenceEndDate)) {

                            // Is the new user absent?
                            Date dtAbsenceStart = dateFormat.parse(strAbsenceStartDate);
                            Date dtAbsenceEnd = dateFormat.parse(strAbsenceEndDate);
                            Date dtToday = new Date();
                            if (dtToday.after(dtAbsenceStart)
                                    && dtToday.before(dtAbsenceEnd)) {
                                vecAlreadyVisitedNewAssignees.add(sTaskNewAssigneeId);
                                sTaskNewAssigneeId = PersonUtil.getPersonObjectID(context,
                                        strAbsenceDelegate);
                            }
                            else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }//~while

                    if(vecAlreadyVisitedNewAssignees.contains(sTaskNewAssigneeId)) {
                        resultsMap.put("Message", "Circular reference found while traversing absence delegation chain.");
                    }
                    vecAlreadyVisitedNewAssignees.clear();
                 }//~if allowdelegation is Yes
                 //
                 ////////////////////////////END ABSENCE DELEGATION HANDLING/////////////////////////////

                 //Delegate the current task to the new assignee
                 inboxTaskObj.delegateTask(context, (String)sTaskNewAssigneeId);

                 // If the attribute Allow Delegation was set "TRUE" momentarily then reset it back
                 if (isAllowedDelegationMomentarily) {
                     inboxTaskObj.setAttributeValue(context, inboxTaskObj.ATTRIBUTE_ALLOW_DELEGATION, "FALSE");
                 }

                 ///////////////////////////////////////////////////////////////////////////////////////
                 //Send the reassignment notification comment provided by the user reassigning the task
                 // Find the route owner
                 route.setId(sRouteId);
                 String strRouteOwner = route.getInfo(context, DomainObject.SELECT_OWNER);

                 // Setting the allowDelegation as false once the task is delegated to some other user by Task Owner. So that the new Task Owner
                 // cannot delegate the task once again. If first time itself the task delegation is done by route owner then we should not
                 // change the value.
                 if(!strRouteOwner.equals(context.getUser()))
                    allowDelegation = "false";
                 // Ended

                 // Find the new and old task assignee name
                 java.util.Set userList = new java.util.HashSet();
                 userList.add(sTaskNewAssigneeId);

                 Map mapInfo = com.matrixone.apps.common.Person.getPersonsFromIds(context, userList, new StringList(com.matrixone.apps.common.Person.SELECT_NAME));
                 String strNewTaskAssigneeName = (String)((Map)mapInfo.get(sTaskNewAssigneeId)).get(com.matrixone.apps.common.Person.SELECT_NAME);

                 // If current user is the route owner then send notification to new user and old task assignee
                 // If current user is the old task assignee then send notification to route owner and old task assignee
                 StringList toList = new StringList(strNewTaskAssigneeName);
                 StringList ccList = new StringList(strRouteOwner);
              // Form the subject
                 String strSubject = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.ReassignRouteApprover.Notification.Subject");
                 String strBody = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(languageStr),"emxComponents.ReassignRouteApprover.Notification.Body");

                 StringList objectIdList = new StringList(taskId);
                 StringBuffer messageText = new StringBuffer();
                 //Adding task reassign comments to body

                 StringBuffer messageBody = new StringBuffer();
                 messageBody.append(strBody).append(reassignComments);

                 messageText.append(messageBody);
                 if(UIUtil.isNullOrEmpty(taskScheduledDate)){
                	 taskScheduledDate = "";
                 }
                 String strMessageBody = "";
                 String strIconMailBody = "";
                 String agentName = emxMailUtil_mxJPO.getAgentName(context, null);
                 String basePropFile = MessageUtil.getBundleName(context);
                 String[] tableRowKeys = {"TaskType", "RouteName", "TaskName", "DueDate", "Instructions"};
                 String sInboxTaskLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, inboxTaskObj.getInfo(context, DomainObject.SELECT_NAME), taskId);
                 String sRouteLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, route.getInfo(context, DomainObject.SELECT_NAME), sRouteId);
                 String sRouteInstructions = (String)inboxTaskObj.getInfo(context,getAttributeSelect(ATTRIBUTE_ROUTE_INSTRUCTIONS));
                 String[] tableRowValues = {taskRouteAction, sRouteLink, sInboxTaskLink, taskScheduledDate, sRouteInstructions};
                 String tableHeader = "emxFramework.ProgramObject.eServicecommonInitiateRoute.TableHeader";
                 String tableRow = "emxFramework.ProgramObject.eServicecommonInitiateRoute.TableData";
                 tableHeader = MessageUtil.getMessage(context, tableHeader, null, locale, basePropFile);
                 String message = "<br><html><body><br>";
                 message += "<style>body, th, td {font-family:Verdana;font-size:11px;text-align:left;padding:5px;}</style>";
                 message += "<table border = 1><thead>" + tableHeader + "</thead><tbody><tr>";
                 message += MessageUtil.getMessage(context, null, tableRow, tableRowKeys, tableRowValues, "", locale, basePropFile);
                 message += "</tr></tbody></table><br></body></html>";
                 messageBody.append(message);

                 HashMap hmMailDetails = getInboxTaskMailDetails(context, null, toList, ccList, null, strSubject, null, null, messageBody.toString(), null, null, objectIdList, null, basePropFile, messageText.toString());
               	 if(hmMailDetails.size() > 0){
               		strMessageBody = (String)hmMailDetails.get("Email");
               		strIconMailBody = (String)hmMailDetails.get("IconMail");
               	 }

               	emxNotificationUtil_mxJPO.sendJavaMail(context, toList, ccList, new StringList(), strSubject, strIconMailBody, strMessageBody,
               			agentName, null , new StringList(), "both");

              }
           }

           BusinessObject boTask = new BusinessObject( taskId );
           boTask.open( context );

           BusinessObjectAttributes boAttrGeneric = boTask.getAttributes(context);
           AttributeItr attrItrGeneric   = new AttributeItr(boAttrGeneric.getAttributes());
           AttributeList attrListGeneric = new AttributeList();

           String sAttrValue = "";
           String sTrimVal   = "";
           while (attrItrGeneric.next()) {
             Attribute attrGeneric = attrItrGeneric.obj();
             sAttrValue = (String)requestMap.get(attrGeneric.getName());

             // Validating for the attribute allow delegation and updating the value
             if(attrGeneric.getName().equals(DomainConstants.ATTRIBUTE_ALLOW_DELEGATION)) {
                 sAttrValue = allowDelegation;
             }
             // Ended
             if (sAttrValue != null) {
               sTrimVal = sAttrValue.trim();
               if ( attrGeneric.getName().equals(DomainConstants.ATTRIBUTE_APPROVAL_STATUS) && sTrimVal.equals("Reject") ) {
                 Pattern relPattern  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_TASK);
                 Pattern typePattern = new Pattern(DomainConstants.TYPE_ROUTE);
                 BusinessObject boRoute = ComponentsUtil.getConnectedObject(context,boTask,relPattern.getPattern(),typePattern.getPattern(),false,true);

                 if ( boRoute != null ) {
                     boRoute.open(context);
                     AttributeItr attributeItr = new AttributeItr(boRoute.getAttributes(context).getAttributes());

                     Route routeObj = (Route)DomainObject.newInstance(context,boRoute);

                     StringList routeSelects = new StringList(3);
                     routeSelects.add(Route.SELECT_OWNER);
                     routeSelects.add(Route.SELECT_NAME);
                     routeSelects.add(Route.SELECT_REVISION);
                     Map routeInfo = routeObj.getInfo(context,routeSelects);

                     String routeOwner = (String)routeInfo.get(Route.SELECT_OWNER);
                     String routeName = (String)routeInfo.get(Route.SELECT_NAME);
                     String routeRev = (String)routeInfo.get(Route.SELECT_REVISION);

                     while ( attributeItr.next() ) {
                         AttributeList attributeList = new AttributeList();
                         Attribute attribute = attributeItr.obj();

                         if( attribute.getName().equals(DomainConstants.ATTRIBUTE_ROUTE_STATUS) ) {
                             Map attrMap = new Hashtable();
                             attrMap.put(DomainConstants.ATTRIBUTE_ROUTE_STATUS, "Stopped");
                             routeObj.modifyRouteAttributes(context, attrMap);
                             /*send notification to the owner*/
                             String[] subjectKeys = {};
                             String[] subjectValues = {};

                             String[] messageKeys = {"name","IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
                             String[] messageValues = {(context.getUser()).toString(),Route.TYPE_INBOX_TASK, boTask.getName(), boTask.getRevision(), Route.TYPE_ROUTE, routeName, routeRev};

                             StringList objectIdList = new StringList();
                             objectIdList.addElement(taskId);

                             StringList toList = new StringList();
                             toList.add(routeOwner);
                             MailUtil.sendNotification(context,
                                                       toList,
                                                       null,
                                                       null,
                                                       "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectReject",
                                                       subjectKeys,
                                                       subjectValues,
                                                       "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageReject",
                                                       messageKeys,
                                                       messageValues,
                                                       objectIdList,
                                                       null);
                             break;
                         }
                     }
                     boRoute.close(context);
                 }
               }
               attrGeneric.setValue(sTrimVal);
               attrListGeneric.addElement(attrGeneric);
             }
           }

           //Update the attributes on the Business Object
           boTask.setAttributes(context, attrListGeneric);
           boTask.update(context);
           String RelationshipId = FrameworkUtil.getAttribute(context,boTask,DomainConstants.ATTRIBUTE_ROUTE_NODE_ID);

           route.setId(sRouteId);

           //Get the correct relId for the RouteNodeRel given the attr routeNodeId from the InboxTask.
           RelationshipId = route.getRouteNodeRelId(context, RelationshipId);

           // Updating the relationship Attributes
           Map attrMap = new Hashtable();

           Relationship relRouteNode = new Relationship(RelationshipId);
           relRouteNode.open(context);
           AttributeItr attrRelItrGeneric   = new AttributeItr(relRouteNode.getAttributes(context));
           while (attrRelItrGeneric.next()) {
               sTrimVal = null;
               Attribute attrGeneric = attrRelItrGeneric.obj();
               sAttrValue = attrGeneric.getName();
               if(sAttrValue.equals(DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE)) {
                   sTrimVal = taskScheduledDate;
               } else if(sAttrValue.equals(DomainConstants.ATTRIBUTE_ALLOW_DELEGATION)) {
                   sTrimVal = allowDelegation;
               } else if(sAttrValue.equals(DomainConstants.ATTRIBUTE_ROUTE_ACTION)) {
                   sTrimVal = taskRouteAction;
               }
               if(sTrimVal != null) {
                   attrMap.put(sAttrValue, sTrimVal);
               }
           }
           Route.modifyRouteNodeAttributes(context, RelationshipId, attrMap);
           relRouteNode.close(context);
           boTask.close( context );


       return resultsMap;

   }

   /**
    * Get's the inbox task mail details and sends notification mail
    *
    * @param context The Matrix Context object
    * @param args
    * @throws Exception if the operation fails
    */
   public static void getInboxTaskMailDetails(Context context, String[] args) throws Exception{
	   String strMessageBody = "";
	   String strIconMailBody = "";
	   HashMap programMap = (HashMap)JPO.unpackArgs(args);
	   String agentName = emxMailUtil_mxJPO.getAgentName(context, null);
	   StringList toList = (StringList)programMap.get("toList");
	   StringList ccList = (StringList)programMap.get("ccList");
	   String subjectKey = (String)programMap.get("subjectKey");
	   String messageKey = (String)programMap.get("messageKey");
	   String[] messageKeys = (String [])programMap.get("messageKeys");
	   String[] messageValues = (String [])programMap.get("messageValues");
	   StringList objectIdList = (StringList)programMap.get("objectIdList");
	   String companyName = (String)programMap.get("companyName");
	   String basePropFile = (String)programMap.get("basePropFile");
	   String staskId = (String)programMap.get("staskId");
	   String routeId = (String)programMap.get("routeId");
	   DomainObject inboxTaskObj = new DomainObject(staskId);
	   Route route = (Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
	   route.setId(routeId);
	   String taskScheduledDate = inboxTaskObj.getAttributeValue(context, ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
	   Locale locale = emxMailUtilBase_mxJPO.getLocale(context);
	   String messageText = MessageUtil.getMessage(context, null,  messageKey, messageKeys, messageValues, companyName, locale, basePropFile);
	   StringBuffer messageBody = new StringBuffer(messageText);
	   subjectKey = MessageUtil.getMessage(context, null,  subjectKey, null, null, companyName, locale, basePropFile);

       String[] tableRowKeys = {"TaskType", "RouteName", "TaskName", "DueDate", "Instructions"};
       String sInboxTaskLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, inboxTaskObj.getInfo(context, DomainObject.SELECT_NAME), staskId);
       String sRouteLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, route.getInfo(context, DomainObject.SELECT_NAME), routeId);
       String sRouteInstructions = (String)inboxTaskObj.getInfo(context,getAttributeSelect(ATTRIBUTE_ROUTE_INSTRUCTIONS));
       String taskRouteAction = (String)inboxTaskObj.getInfo(context,getAttributeSelect(ATTRIBUTE_ROUTE_ACTION));
       String[] tableRowValues = {taskRouteAction, sRouteLink, sInboxTaskLink, taskScheduledDate, sRouteInstructions};
       String tableHeader = "emxFramework.ProgramObject.eServicecommonInitiateRoute.TableHeader";
       String tableRow = "emxFramework.ProgramObject.eServicecommonInitiateRoute.TableData";
       tableHeader = MessageUtil.getMessage(context, tableHeader, null, locale, basePropFile);
       String message = "<br><html><body><br>";
       message += "<style>body, th, td {font-family:Verdana;font-size:11px;text-align:left;padding:5px;}</style>";
       message += "<table border = 1><thead>" + tableHeader + "</thead><tbody><tr>";
       message += MessageUtil.getMessage(context, null, tableRow, tableRowKeys, tableRowValues, "", locale, basePropFile);
       message += "</tr></tbody></table><br></body></html>";
       messageBody.append(message);



	   HashMap hmMailDetailsMap = (HashMap)getInboxTaskMailDetails(context, null, toList, ccList, null, subjectKey, null, null, messageBody.toString(), messageKeys, messageValues, objectIdList, companyName, basePropFile, messageText);
	   if(hmMailDetailsMap.size() > 0){
      		strMessageBody = (String)hmMailDetailsMap.get("Email");
      		strIconMailBody = (String)hmMailDetailsMap.get("IconMail");
       }

	   emxNotificationUtil_mxJPO.sendJavaMail(context, toList, ccList, new StringList(), subjectKey, strIconMailBody, strMessageBody,
    			agentName, null , new StringList(), "both");

	  return;
   }

   /**
    * Get's the Inbox task mail details
    *
    * @param context              the eMatrix <code>Context</code> object
    * @param objectId             context objectId
    * @param toList               the eMatrix <code>StringList</code> object that holds the to list of users to notify
    * @param ccList               the eMatrix <code>StringList</code> object that holds the cc list of users to notify
    * @param bccList              the eMatrix <code>StringList</code> object that holds the bcc list of users to notify
    * @param subjectKey           the notification subject key
    * @param subjectKeys          an array of subject place holder keys
    * @param subjectValues        an array of subject place holder values
    * @param messageKey           the notification message key
    * @param messageKeys          an array of message place holder keys
    * @param messageValues        an array of message place holder values
    * @param objectIdList         the eMatrix <code>StringList</code> object that holds the list of objects to send with the notification
    * @param companyName          used for company-based messages
    * @param basePropFile         used to determine base properties file to be used
    * @param messageText          the message body used for iconMail
    * @throws Exception if the operation fails
    */
   public static HashMap getInboxTaskMailDetails(Context context,
           String objectId,
           StringList toList,
           StringList ccList,
           StringList bccList,
           String subjectKey,
           String[] subjectKeys,
           String[] subjectValues,
           String messageKey,
           String[] messageKeys,
           String[] messageValues,
           StringList objectIdList,
           String companyName,
           String basePropFile,
           String messageText)
throws Exception
{
	   HashMap names = new HashMap();
       //
       // "languages" holds the unique languages
       //
       HashMap languages = new HashMap();
       emxMailUtilBase_mxJPO.getNamesAndLanguagePreferences(context, names, languages, toList);
       emxMailUtilBase_mxJPO.getNamesAndLanguagePreferences(context, names, languages, ccList);

       //
       // send one message per language
       //

       //Added for bug 344780
       //add them to the message, do it here for localization purpose
       MapList objInfoMapList = null;
       boolean hasBusObjInfo = false;
       String sBaseURL = emxMailUtilBase_mxJPO.getBaseURL(context, null);
       Vector LocaleInfo = emxMailUtilBase_mxJPO.getLocales(context, null);
       if (objectIdList != null && objectIdList.size() != 0)
       {
           StringList busSels = new StringList(3);
           busSels.add(DomainObject.SELECT_TYPE);
           busSels.add(DomainObject.SELECT_NAME);
           busSels.add(DomainObject.SELECT_REVISION);

           objInfoMapList = DomainObject.getInfo(context, (String[])objectIdList.toArray(new String[]{}), busSels);
           hasBusObjInfo = objInfoMapList != null && objInfoMapList.size() > 0;
       }
       Iterator itr = languages.keySet().iterator();
       StringBuffer messageBuffer = new StringBuffer();
       StringBuffer sbIconMailBody = new StringBuffer();
       HashMap hmMailInfo = new HashMap();
       while (itr.hasNext())
       {
           String language = (String) itr.next();
           Locale userPrefLocale = language.length() > 0 ? MessageUtil.getLocale(language) : null;
           Locale userLocale = userPrefLocale == null ? emxMailUtilBase_mxJPO.getLocale(context) : userPrefLocale;

           //
           // build the to, cc and bcc lists for this language
           //
           StringList to = emxMailUtilBase_mxJPO.getNamesForLanguage(toList, names, language);
           StringList cc = emxMailUtilBase_mxJPO.getNamesForLanguage(ccList, names, language);

           String subject = MessageUtil.getMessage(context, null, subjectKey, null, null, null, userLocale, basePropFile);
           messageBuffer = new StringBuffer();
           sbIconMailBody = new StringBuffer();
           if(hasBusObjInfo)
           {
        	   String strBusinessObject = MessageUtil.getString("emxFramework.IconMail.ObjectDetails.BusinessObject", "", userLocale);
        	   StringBuffer strObjectInfo = new StringBuffer(strBusinessObject);
        	   String strCheckObjectMessage = MessageUtil.getString("emxFramework.IconMail.ObjectDetails.CheckBusinessObjects", "", userLocale);
        	   messageBuffer.append("<br>");
               messageBuffer.append(strCheckObjectMessage);
               sbIconMailBody.append(strCheckObjectMessage).append("\n");
               messageBuffer.append("<br>");
               for(int i=0; i < objInfoMapList.size() ; i++)
               {
                   Map objInfoMap = (Map)objInfoMapList.get(i);

                   String sObjType = (String)objInfoMap.get(DomainObject.SELECT_TYPE);
                   sObjType = UINavigatorUtil.getAdminI18NString("Type", sObjType, userLocale.getLanguage());
                   String sObjName = (String)objInfoMap.get(DomainObject.SELECT_NAME);
                   String sObjRevision = (String)objInfoMap.get(DomainObject.SELECT_REVISION);

                   sbIconMailBody.append("\n\"");
                   sbIconMailBody.append(sObjType);
                   sbIconMailBody.append("\" \"");
                   sbIconMailBody.append(sObjName);
                   sbIconMailBody.append("\" \"");
                   sbIconMailBody.append(sObjRevision);
                   sbIconMailBody.append("\"\n");

                   messageBuffer.append("<br>\"");
                   messageBuffer.append(sObjType);
                   messageBuffer.append("\" \"");
                   messageBuffer.append(sObjName);
                   messageBuffer.append("\" \"");
                   messageBuffer.append(sObjRevision);
                   messageBuffer.append("\"<br>");

                   strObjectInfo.append(" ");
                   strObjectInfo.append(sObjType);
                   strObjectInfo.append(" ");
                   strObjectInfo.append(sObjName);
                   strObjectInfo.append(" ");
                   strObjectInfo.append(sObjRevision);
                   strObjectInfo.append(",");
               }
               strBusinessObject = strObjectInfo.toString();
               strBusinessObject = strBusinessObject.substring(0, strBusinessObject.length() - 1);
               strBusinessObject += "<br>";
               messageBuffer.insert(0, strBusinessObject);
               messageBuffer.append("<br>");
               sbIconMailBody.append("\n");
           }

           // if this is the no language preference group
           if (userPrefLocale == null && LocaleInfo != null)
           {
               // generate a message containing multiple languages
               for (int i = 0; i < LocaleInfo.size(); i++)
               {
                   // Define the mail message.
                   messageBuffer.append(MessageUtil.getMessage(context,
                           null,
                           messageKey,
                           null,
                           null,
                           null,
                           (Locale) LocaleInfo.elementAt(i),
                           basePropFile));

                   // separate the different language strings
                   messageBuffer.append("<br>");
               }
           }
           // otherwise get message based on language
           else
           {
               messageBuffer.append(MessageUtil.getMessage(context, null,  messageKey, messageKeys, messageValues, companyName, userLocale, basePropFile));
           }

           MQLCommand mql = new MQLCommand();
           mql.open(context);
           mql.executeCommand(context, "get env global MX_TREE_MENU");
           String paramSuffix = mql.getResult();
           if (paramSuffix != null &&
               !"".equals(paramSuffix) &&
               !"\n".equals(paramSuffix))
           {
               mql.executeCommand(context, "unset env global MX_TREE_MENU");
           }

            String inBoxTaskId =null;
            String sTempObjId = null;
            DomainObject doTempObj = DomainObject.newInstance(context);

           // If the base URL and object id list are available,
           // then add urls to the end of the message.
           if ( (sBaseURL != null && ! "".equals(sBaseURL)) &&
               (objectIdList != null && objectIdList.size() != 0) )
           {
               // Prepare the message for adding urls.
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
                       }catch(Exception ex){System.out.println("exception in box sendNotification "+ex); }
                   }
                   // Add the url to the end of the message.
                   messageBuffer.append("<br>").append(sBaseURL).append("?objectId=").append(sTempObjId);

               }
           }

           // If is inbox task the message has to be modified accordingly.
           if(inBoxTaskId != null && !inBoxTaskId.equals("")){
               if( (messageText != null) && (!messageText.equals("")))
            	   sbIconMailBody.append(messageText);
               sbIconMailBody.append(emxMailUtilBase_mxJPO.getInboxTaskMailMessage(context, inBoxTaskId, userLocale, basePropFile, sBaseURL, paramSuffix));
           }
       }
       hmMailInfo.put("Email", messageBuffer.toString());
       hmMailInfo.put("IconMail", sbIconMailBody.toString());
	   return hmMailInfo;

	}
   /**
    * Returns the Inbox task info
    *
    * @param context The Matrix Context object
    * @returns Map containing the info on "Inbox Task".
    * @throws Exception if the operation fails
    * @since R212
    */
   public Map getInboxTaskInfo(Context context, DomainObject inboxTaskObj) throws FrameworkException
   {
       // get inbox task assignee relationship id as well as other information.
       StringList objectSelects = new StringList(7);
       objectSelects.add(SELECT_ID);
       objectSelects.add(SELECT_TYPE);
       objectSelects.add(SELECT_NAME);
       objectSelects.add(SELECT_REVISION);
       objectSelects.add(SELECT_TASK_ASSIGNEE_CONNECTION);
       objectSelects.add(routeIdSelectStr);
       objectSelects.add(SELECT_ROUTE_NODE_ID);
       objectSelects.add(SELECT_TASK_ASSIGNEE_NAME);

       Map taskInfo = inboxTaskObj.getInfo(context, objectSelects);
       return taskInfo;
   }
   /**
    * Sends the notifiation to everyone in the group
    *
    * @param context The Matrix Context object
    * @returns nothing
    * @throws Exception if the operation fails
    * @since R212
    */
   public void sendNotificationToGroup(Context context, String sGroup, Map taskInfo,DomainObject routeObject, String languageStr) throws FrameworkException {
       try {
           // Building the Object of the Mail Util to be used Later
           emxMailUtil_mxJPO mailUtil = new emxMailUtil_mxJPO(context, null);

           StringList objectSelects = new StringList(4);
           objectSelects.add(SELECT_TYPE);
           objectSelects.add(SELECT_NAME);
           objectSelects.add(SELECT_REVISION);
           objectSelects.add(SELECT_OWNER);

           Map routeInfo = routeObject.getInfo(context, objectSelects);
           String sRouteOwner = (String)routeInfo.get(SELECT_OWNER);

           String aliasGroupName = i18nNow.getAdminI18NString("Group",sGroup, languageStr);
           String sGRName ="Group" +" - "+aliasGroupName;

           // Construct String array to send mail notification
           String[] mailArguments = new String[27];
           mailArguments[0] = sGroup;
           mailArguments[1] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.SubjectNotice3";
           mailArguments[2] = "2";
           mailArguments[3] = "Name";
           mailArguments[4] = aliasGroupName;
           mailArguments[5] = "GroupOrRole";
           mailArguments[6] = "Group";
           mailArguments[7] = "emxFramework.ProgramObject.eServicecommonInitiateRoute.FirstMessage2";
           mailArguments[8] = "8";
           mailArguments[9] = "IBType";
           mailArguments[10] = (String)taskInfo.get(SELECT_TYPE);
           mailArguments[11] = "IBName";
           mailArguments[12] = (String)taskInfo.get(SELECT_NAME);
           mailArguments[13] = "IBRev";
           mailArguments[14] = (String)taskInfo.get(SELECT_REVISION);
           mailArguments[15] = "GRName";
           mailArguments[16] = sGRName;
           mailArguments[17] = "Type";
           mailArguments[18] = (String)routeInfo.get(SELECT_TYPE);
           mailArguments[19] = "Name";
           mailArguments[20] = (String)routeInfo.get(SELECT_NAME);
           mailArguments[21] = "Rev";
           mailArguments[22] = (String)routeInfo.get(SELECT_REVISION);
           mailArguments[23] = "ROwner";
           mailArguments[24] = sRouteOwner;
           mailArguments[25] = (String)taskInfo.get(SELECT_ID);
           mailArguments[26] = "";

           String oldAgentName = mailUtil.getAgentName(context, new String[] {});
           mailUtil.setAgentName(context, new String[] { sRouteOwner });
           mailUtil.sendNotificationToUser(context, mailArguments);
           mailUtil.setAgentName(context, new String[] { oldAgentName });

       } catch (Exception e) {
           throw new FrameworkException(e.toString());
       }
   }
	/**
    * Gets the internationalised value for group
    *
    * @param context The Matrix Context object
    * @returns nothing
    * @throws FrameworkException if the operation fails
    * @since R212
    */
   public String getI18nAssignee(Context context, String args[]) throws FrameworkException {
	   try {
		   HashMap programMap = (HashMap)JPO.unpackArgs(args);
		   HashMap requestMap = (HashMap)programMap.get("requestMap");
		   String languageStr = (String)requestMap.get("languageStr");
		   String taskId    = (String)requestMap.get("objectId");

		   DomainObject inboxTaskObj = new DomainObject(taskId);
    	   StringList objectSelects = new StringList();
    	   objectSelects.add(DomainConstants.SELECT_OWNER);
    	   objectSelects.add(DomainConstants.SELECT_OWNER_ISGROUP);
    	   objectSelects.add(DomainConstants.SELECT_OWNER_ISROLE);
    	   objectSelects.add(DomainConstants.SELECT_OWNER_ISPERSON);

    	   Map taskInfo = inboxTaskObj.getInfo(context, objectSelects);

    	   String isAPerson = (String)taskInfo.get(DomainConstants.SELECT_OWNER_ISPERSON);
    	   String isARole = (String)taskInfo.get(DomainConstants.SELECT_OWNER_ISROLE);
    	   String isAGroup = (String)taskInfo.get(DomainConstants.SELECT_OWNER_ISGROUP);

    	   String assignee = (String)taskInfo.get(DomainConstants.SELECT_OWNER);
    	   if("TRUE".equals(isAPerson)) {
    		   assignee = PersonUtil.getFullName(context, assignee);
    	   } else if("TRUE".equals(isARole)) {
    		   assignee = i18nNow.getAdminI18NString("Role", assignee, languageStr);
    	   } else if("TRUE".equals(isAGroup)) {
    		   assignee = i18nNow.getAdminI18NString("Group", assignee, languageStr);
    	   }
    	   return assignee;
       } catch(Exception e) {
    	   throw new FrameworkException(e.toString());
       }
   }

   /**
    * Returns the Inbox Task due date as per Client Time Zone and browser language
    *
    * @param context The Matrix Context object
    * @returns date and time
    * @throws Exception if the operation fails
    * @since R212
    */

   public String getInboxTaskDueDate(Context context, String[] args) throws Exception
   {
       try{
       Map programMap = (Map) JPO.unpackArgs(args);
       Map paramMap   = (Map)programMap.get("paramMap");
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       Locale locObj = (Locale)requestMap.get("localeObj");
       String objectId = (String)paramMap.get("objectId");
       String languageStr = (String)paramMap.get("languageStr");
       String timeZone = (String) (requestMap != null ? requestMap.get("timeZone") : programMap.get("timeZone"));
       double clientTZOffset   = (new Double(timeZone)).doubleValue();
       DomainObject dom = new DomainObject(objectId);
       String taskScheduledDate = dom.getAttributeValue(context, ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
       String dueDate = "";
       if(! UIUtil.isNullOrEmpty(taskScheduledDate)){
    	   dueDate =   eMatrixDateFormat.getFormattedDisplayDateTime(taskScheduledDate, clientTZOffset, locObj);
       }
       return dueDate;
       }catch(Exception e) {
           throw new FrameworkException(e.toString());
       }

   }

   /**
    * Range Values for Appproval Status in Inbox Task form
    * @param context
    * @param args
    * @return
    * @throws FrameworkException
    */

   public Map getTaskApprovalStatusOptions(Context context, String[] args) throws FrameworkException {

       try {
    	   Map programMap = (Map)JPO.unpackArgs(args);
    	   Map requestMap = (Map)programMap.get("requestMap");
    	   Map paramMap   = (Map)programMap.get("paramMap");
    	   String sLanguage = (String) paramMap.get("languageStr");
    	   String objectId = (String) requestMap.get("objectId");

    	   Map returnMap = new HashMap(2);
    	   StringList rangeDisplay = new StringList(3);
    	   StringList rangeActual = new StringList(3);

    	   DomainObject taskObj = DomainObject.newInstance(context, objectId);
    	   StringList selects = new StringList();
    	   selects.addElement("attribute[" + sAttrReviewTask + "]");
    	   selects.addElement("attribute[" + sAttrReviewersComments + "]");

    	   //get the details required
    	   Map taskMap = taskObj.getInfo(context, selects);
    	   String reviewTask = (String) taskMap.get("attribute[" + sAttrReviewTask + "]");
    	   String reviewComments = (String) taskMap.get("attribute[" + sAttrReviewersComments + "]");

    	   if("Yes".equals(reviewTask) && UIUtil.isNotNullAndNotEmpty(reviewComments)) {

    		   String promote= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Promote", context.getLocale());
    		   String demote= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Demote", context.getLocale());
    		   rangeActual.addElement("promote");
    		   rangeDisplay.addElement(promote);
    		   rangeActual.addElement("demote");
    		   rangeDisplay.addElement(demote);
    		   returnMap.put("field_choices", rangeActual);
    		   returnMap.put("field_display_choices", rangeDisplay);

    	   } else {
    		   matrix.db.AttributeType attribName = new matrix.db.AttributeType(
    				   DomainConstants.ATTRIBUTE_APPROVAL_STATUS);
    		   attribName.open(context);
    		   // actual range values
    		   List attributeRange = attribName.getChoices();

    		   attribName.close(context);
    		   List attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(
    				   DomainConstants.ATTRIBUTE_APPROVAL_STATUS, (StringList) attributeRange, sLanguage);
    		   attributeDisplayRange.remove(attributeRange.indexOf("Ignore"));
    		   attributeRange.remove("Ignore");
    		   attributeDisplayRange.remove(attributeRange.indexOf("Signature Reset"));
    		   attributeRange.remove("Signature Reset");
    		   attributeDisplayRange.remove(attributeRange.indexOf("None"));
    		   attributeRange.remove("None");

    		   rangeActual.addAll(attributeRange);
    		   rangeDisplay.addAll(attributeDisplayRange);

    		   returnMap.put("field_choices", rangeActual);
    		   returnMap.put("field_display_choices", rangeDisplay);
    	   }
    	   return returnMap;

       } catch (Exception e) {
    	   throw new FrameworkException(e);
       }
   }

   /**
    * Rto show approve options based on route action
    * @param context
    * @param args
    * @return
    * @throws FrameworkException
    */

   public boolean showApproveOptions(Context context, String[] args) throws FrameworkException {
       try {

    	   boolean showApprove = false;
    	   Map programMap = (Map)JPO.unpackArgs(args);
    	   String objectId = (String) programMap.get("objectId");
    	   String fromSummaryPage = (String) programMap.get("summaryPage");

    	   DomainObject taskObj = DomainObject.newInstance(context, objectId);
    	   String routeAction = taskObj.getInfo(context, "attribute[" + sAttrRouteAction + "]");

    	   if("true".equals(fromSummaryPage) && "Approve".equals(routeAction)) {
    		   showApprove = true;
    	   }
    	   return showApprove;

       } catch (Exception e) {
    	   throw new FrameworkException(e);
       }
   }

   /**
    * getTaskStatusForWidget - gets the status icon for Inbox Task and progress bar icon for WBS Tasks
    *  to be shown Widget Dash Board
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - objectList MapList
    * @returns MapList
    * @throws Exception if the operation fails
    * @since V6R2014x
    */
	public MapList getTaskStatusForWidget(Context context, String[] args) throws Exception
	{
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
	        MapList widgetDataMapList = (MapList) programMap.get(UIWidget.JPO_WIDGET_DATA);
            String fieldKey = (String) programMap.get(UIWidget.JPO_WIDGET_FIELD_KEY);
            Map<String, String> widgetArgs = (Map<String, String>) programMap.get(UIWidget.JPO_WIDGET_ARGS);

            String baseURI = widgetArgs.get(UIWidget.ARG_BASE_URI);

			for (int i = 0; i < widgetDataMapList.size(); i++) {
				Map collMap = (Map)widgetDataMapList.get(i);
				String sType = (String)collMap.get(DomainConstants.SELECT_TYPE);
				if(DomainConstants.TYPE_INBOX_TASK.equalsIgnoreCase(sType)){
					collMap.put(fieldKey, getStatusImageForTasks(context, collMap, baseURI));
				}else{
					try{
						Class reqClass = Class.forName("com.matrixone.apps.program.Widgets");
			        	Class[] classArgs = new Class[3];
			            classArgs[0] = Context.class;
			            classArgs[1] = Map.class;
			            classArgs[2] = String.class;
			            Method reqMethod = reqClass.getMethod("getProgressStatus", classArgs);
		            	Object[] methodArgs = new Object[2];
		            	methodArgs[0] = context;
		            	methodArgs[1] = collMap;
		            	methodArgs[2] = fieldKey;
		            	reqMethod.invoke(null, methodArgs);
					}catch(Exception e){
					}
				}
            }
			return widgetDataMapList;
       }catch (Exception ex){
           System.out.println("Error in getTaskStatusForWidget= " + ex.getMessage());
           throw ex;
       }
   }


	/**
	 * This method returns a Status image for each task based on states and due dates
	 * @param context
	 * @param map
	 * @return String Status Image
	 * @throws Exception
	 * @since V6R2014x
	 */
	private String getStatusImageForTasks(Context context, Map map, String baseURI)
	throws Exception{
		String stateComplete = FrameworkUtil.lookupStateName(context, policyTask, "state_Complete");
		String stateCompleted = FrameworkUtil.lookupStateName(context, policyWorkflowTask, "state_Completed");
		String stateAssigned    = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_INBOX_TASK, "state_Assigned");

		Date dueDate   = null;
		Date curDate = new Date();
		String statusImageString = "";
		String statusColor= "";

		String taskState = (String) map.get(DomainConstants.SELECT_CURRENT);

		String taskDueDate = "";
        String taskCompletedDate = "";
        String dueDateOffset = "";
        String sTypeName = (String)map.get(DomainConstants.SELECT_TYPE);
        String assigneeDueDateOpt = (String)map.get(getAttributeSelect(DomainConstants.ATTRIBUTE_ASSIGNEE_SET_DUEDATE));

        if(taskState==null)
        	taskState = "";

        if ((DomainConstants.TYPE_INBOX_TASK).equalsIgnoreCase(sTypeName)){
            taskDueDate = (String)map.get(strAttrCompletionDate);
            taskCompletedDate = (String)map.get(strAttrTaskCompletionDate);
            dueDateOffset = (String)map.get(getAttributeSelect(DomainConstants.ATTRIBUTE_DUEDATE_OFFSET));
        }else if ((DomainConstants.TYPE_TASK).equalsIgnoreCase(sTypeName)){
            taskDueDate = (String)map.get(strAttrTaskEstimatedFinishDate);
            taskCompletedDate = (String)map.get(strAttrTaskFinishDate);
            dueDateOffset = (String)map.get(getAttributeSelect(DomainConstants.ATTRIBUTE_DUEDATE_OFFSET));
        }else if (sTypeWorkflowTask.equalsIgnoreCase(sTypeName)){
            taskDueDate = (String)map.get(strAttrworkFlowDueDate);
            taskCompletedDate = (String)map.get(strAttrworkFlowCompletinDate);
            dueDateOffset = (String)map.get(getAttributeSelect(DomainConstants.ATTRIBUTE_DUEDATE_OFFSET));
        }

        boolean bDueDateEmpty = (taskDueDate == null || "".equals(taskDueDate) || "null".equals(taskDueDate)) ? true : false;
        boolean bDeltaDueDate = (dueDateOffset != null && !"".equals(dueDateOffset) && !"null".equals(dueDateOffset) && bDueDateEmpty) ? true : false;

        if(!"".equals(taskState)){
            if(taskDueDate == null || "".equals(taskDueDate)){
                dueDate = new Date();
            }else{
                dueDate = eMatrixDateFormat.getJavaDate(taskDueDate);
            }

            if(!taskState.equals(stateComplete) && !taskState.equals(stateCompleted)){
                if(dueDate != null && curDate.after(dueDate)){
                    statusColor = "Red";
                }else{
                    statusColor = "Green";
                }
            }else{
                Date actualCompletionDate = (taskCompletedDate == null || "".equals(taskCompletedDate)) ? new Date() : eMatrixDateFormat.getJavaDate(taskCompletedDate);

                if(dueDate != null && actualCompletionDate.after(dueDate)){
                    statusColor = "Red";
                }else{
                    statusColor = "Green";
                }
            }

            if(statusColor.equals("Red")){
                statusImageString = "<img border='0' src='" + baseURI + "common/images/iconStatusRed.gif' name='red' id='red' alt='*' />";
            }else if(statusColor.equals("Green")){
                statusImageString = "<img border='0' src='" + baseURI + "common/images/iconStatusGreen.gif' name='green' id='green' alt='*' />";
            }else{
                statusImageString="&#160;";
            }

        } else if(taskState.equals("") || (taskState.equals(stateAssigned) && "Yes".equalsIgnoreCase(assigneeDueDateOpt) && bDueDateEmpty) || (bDueDateEmpty && bDeltaDueDate )){
            statusImageString="&#160;";
        }

		return statusImageString;
	}

	/**
	 * This method returns Inbox Task & WBS Task object details togeather in a MapList for Widgets
	 * @param context
	 * @param args
	 * @return MapList
	 * @throws Exception
	 * @since V6R2014x
	 */
	public MapList getUserTaskForWidgets(Context context, String[] args)
	throws Exception{
		Map programMap = (Map) JPO.unpackArgs(args);
		StringList busSelects = (StringList) programMap.get("JPO_BUS_SELECTS");
		MapList retMapList = new MapList();
		MapList tempIBTaskMapList = new MapList();

		//Fetch Inbox Task object details
        String typePattern = DomainObject.TYPE_INBOX_TASK;
        String whereExpression = "owner == context.user AND current != 'Complete'";
        tempIBTaskMapList = getInboxTaskDetails(context, busSelects, typePattern, whereExpression);
        for(int i=0; i<tempIBTaskMapList.size(); i++){
        	retMapList.add((Map)tempIBTaskMapList.get(i));
        }

        /*
		 * Project Tasks are no longer required in the Widget.
		//Fetch WBS Tasks object details
        try{
        	Class reqClass = Class.forName("com.matrixone.apps.program.Widgets");
        	Class[] classArgs = new Class[2];
            classArgs[0] = Context.class;
            classArgs[1] = String[].class;
            Method reqMethod = reqClass.getMethod("getUserTasks", classArgs);
        	Object[] methodArgs = new Object[2];
        	methodArgs[0] = context;
        	methodArgs[1] = args;
        	MapList tempWBSTaskMapList = (MapList) reqMethod.invoke(null, methodArgs);
        	for(int i=0; i<tempWBSTaskMapList.size(); i++){
            	retMapList.add((Map)tempWBSTaskMapList.get(i));
        	}
        }catch(Exception e){
        }
		*/
        return retMapList;
	}

	/**
	 * This method returns the details of Inbox Task for a perticular user
	 * @param context
	 * @param busSelects
	 * @param typePattern
	 * @param whereExpression
	 * @return MapList
	 * @throws Exception
	 * @since V6R2014x
	 */
	private MapList getInboxTaskDetails(Context context, StringList busSelects, String typePattern, String whereExpression)
	throws Exception{

        MapList returnMapList= DomainObject.findObjects(context,
        		typePattern,
				QUERY_WILDCARD,  // namepattern
				QUERY_WILDCARD,  // revpattern
				QUERY_WILDCARD,  // owner pattern
				QUERY_WILDCARD,  // vault pattern
				whereExpression, // where exp
				true,
				busSelects);

        return returnMapList;
	}

	/**
     * To get the quick task complete link
     * @param context
     * @param args
     * @return StringList
     * @throws Exception
     * @since V6R2015x
     */
	public StringList getQuickTaskCompleteLink(Context context, String [] args) throws Exception{
		HashMap programMap        = (HashMap) JPO.unpackArgs(args);
	    MapList relBusObjPageList = (MapList)programMap.get("objectList");
		StringList slLinks = new StringList(relBusObjPageList.size());
		String sTaskLink = "";
	    for (int i=0; i < relBusObjPageList.size(); i++) {
	         Map collMap = (Map)relBusObjPageList.get(i);
	         String sTaskType  = (String)collMap.get(DomainObject.SELECT_TYPE);
	         String sTaskId  = (String)collMap.get(DomainObject.SELECT_ID);
	         if(sTypeInboxTask.equals(sTaskType)){
	        	 sTaskLink = "javascript:emxTableColumnLinkClick('../components/emxTaskCompletePreProcess.jsp?action=Approve&amp;summaryPage=true&amp;emxSuiteDirectory=components&amp;suiteKey=Components&amp;objectId=" + sTaskId + "', null, null, 'false', 'listHidden', '', null, 'true')";
	         }else{
	        	 sTaskLink = "javascript:emxTableColumnLinkClick('../components/emxUserTasksSummaryLinksProcess.jsp?fromPage=Complete&amp;emxSuiteDirectory=components&amp;suiteKey=Components&amp;emxTableRowId=" + sTaskId + "', null, null, 'false', 'listHidden', '', null, 'true')";
	         }
	         sTaskLink  = "<a href=\"" + sTaskLink + "\">" + "<img src=\"" + "../common/images/buttonDialogDone.gif" + "\" width=\"16px\" height=\"16px\"/>" + "</a>";
	         slLinks.add(sTaskLink);
	    }
		return slLinks;
	}

	/**
     * To get the Approval Status
     * @param context
     * @param args
     * @return StringList
     * @throws Exception
     * @since V6R2015x
     */
	public StringList getApprovalStatusInfo(Context context, String [] args) throws Exception{
		
		 final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
         final String POLICY_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Review");
         final String STRING_COMPLETED = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.LifecycleTasks.Completed", context.getLocale());
         final String STRING_APPROVED =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Approved", context.getLocale());
         final String STRING_REJECTED =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Rejected", context.getLocale());
         final String STRING_ABSTAINED =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Abstained", context.getLocale());
         final String STRING_NEEDS_REVIEW =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.NeedsReview", context.getLocale());
         final String STRING_AWAITING_APPROVAL =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.LifecycleTasks.AwaitingApproval",context.getLocale());   
         final String STRING_ROUTE_STOPPED = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.LifecycleTasks.RouteStopped", context.getLocale());
         
		HashMap programMap        = (HashMap) JPO.unpackArgs(args);
	    MapList relBusObjPageList = (MapList)programMap.get("objectList");
		StringList slLinks = new StringList(relBusObjPageList.size());
		String sTaskLink = "";
	    for (int i=0; i < relBusObjPageList.size(); i++) {
	         Map collMap = (Map)relBusObjPageList.get(i);
	         String sTaskType  = (String)collMap.get(DomainObject.SELECT_TYPE);
	         
	         String strCurrentState  = (String)collMap.get(DomainObject.SELECT_CURRENT);
	         String strApprovalStatus  = (String)collMap.get(strAttrTaskApprovalStatus);
	         String strRouteStatus  = (String)collMap.get(routeApprovalStatusSelectStr);
	         
	         if(sTypeInboxTask.equals(sTaskType)){
	        	 if (POLICY_INBOX_TASK_STATE_COMPLETE.equalsIgnoreCase(strCurrentState)) {
                         if ("Approve".equals(strApprovalStatus)) {
                        	 slLinks.add(STRING_APPROVED);
                         }
                         else if ("Reject".equals(strApprovalStatus)) {
                        	 slLinks.add(STRING_REJECTED);
                         }
                         else if ("Abstain".equals(strApprovalStatus)) {
                        	 slLinks.add(STRING_ABSTAINED);
                         }
                         else {
                        	 slLinks.add(STRING_COMPLETED);
                         }
                     }
                     else if(POLICY_INBOX_TASK_STATE_REVIEW.equals(strCurrentState)){
                    	 slLinks.add(STRING_NEEDS_REVIEW);
                     }
                     else {
                    	 if ("Stopped".equals(strRouteStatus)) {
                    		 slLinks.add(STRING_ROUTE_STOPPED);
                         } else {
                        	 slLinks.add(STRING_AWAITING_APPROVAL);
                         }
                     }
	         }else{
	        	 slLinks.add(DomainConstants.EMPTY_STRING);
	         }
	    }
		return slLinks;
	}   

	/**
	 * Method to set value of Responsible Role in hidden field added on Change Assignee form.
	 * @param context
	 * @param args
	 * @return script for hidden field and its value.
	 * @throws Exception
	 */
	public String getAssignedRolesForTask (Context context,String [] args) throws Exception
	{
		String isResponsibleRoleEnabled = DomainConstants.EMPTY_STRING;
		StringBuilder sbTaskRole = new StringBuilder();
		sbTaskRole.append("<input type = \"hidden\" name =\"taskRole\" id=\"taskRole\">");
		try {
			isResponsibleRoleEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.ResponsibleRoleForSignatureMeaning.Preserve");
			String isFDAEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.EnableFDA");		
			if(UIUtil.isNotNullAndNotEmpty(isFDAEnabled) && isFDAEnabled.equalsIgnoreCase("true") && UIUtil.isNotNullAndNotEmpty(isResponsibleRoleEnabled) && isResponsibleRoleEnabled.equalsIgnoreCase("true"))
			{
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				Map requestMap = (Map)programMap.get("requestMap");
				String strInboxTaskId = (String) requestMap.get("objectId");
				DomainObject dObjInboxTask = DomainObject.newInstance(context, strInboxTaskId);
				String strInboxTaskInfo = dObjInboxTask.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_TASK_USER);	
				sbTaskRole
				.append("<script>var roleField = document.getElementById('taskRole');roleField.value=\"").append(strInboxTaskInfo).append("\";</script>");
			}
			else
				sbTaskRole
				.append("<script>var roleField = document.getElementById('taskRole');roleField.value=\"").append(DomainConstants.EMPTY_STRING).append("\";</script>");
			
		} catch (Exception e) {
			isResponsibleRoleEnabled = "false";
		}
		return sbTaskRole.toString();
	}

	/**
	 * Access function to show Responsible Role field on Task properties and Change Assignee form.
	 * @param context
	 * @param args
	 * @return true to show field , false to not show field.
	 * @throws Exception
	 */
	public boolean showResponsibleRole(Context context, String[] args) throws Exception
	{
		boolean flag = false;
		String isResponsibleRoleEnabled = DomainConstants.EMPTY_STRING;
		try {

			isResponsibleRoleEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.ResponsibleRoleForSignatureMeaning.Preserve");
			String isFDAEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.EnableFDA");
			if(UIUtil.isNotNullAndNotEmpty(isFDAEnabled) && isFDAEnabled.equalsIgnoreCase("true") && UIUtil.isNotNullAndNotEmpty(isResponsibleRoleEnabled) && isResponsibleRoleEnabled.equalsIgnoreCase("true"))
			{
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				String strInboxTaskId  = (String) programMap.get("objectId");
				DomainObject dObjInboxTask = DomainObject.newInstance(context, strInboxTaskId);
				String strRouteTaskUser = dObjInboxTask.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_TASK_USER);
				if(UIUtil.isNotNullAndNotEmpty(strRouteTaskUser) && strRouteTaskUser.startsWith("role_"))
				{
					flag = true;
				}
			}
			

		} catch (Exception e) {
			isResponsibleRoleEnabled = "false";
					}
		return flag;
	}

	/**
	 * Method to get value for Responsible Role field on Task properties and Change Assignee form.
	 * @param context
	 * @param args
	 * @return value of Responsible Role.
	 * @throws Exception
	 */
	public String getResponsibleRole (Context context,String [] args) throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		String languageStr = (String)requestMap.get("languageStr");
		String strInboxTaskId = (String) requestMap.get("objectId");
		DomainObject dObjInboxTask = DomainObject.newInstance(context, strInboxTaskId);
		String strRouteTaskUser = dObjInboxTask.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_TASK_USER);
		if(UIUtil.isNotNullAndNotEmpty(strRouteTaskUser) && strRouteTaskUser.startsWith("role_"))
		{
			String strResponsibleRole = i18nNow.getAdminI18NString("Role", PropertyUtil.getSchemaProperty(context, strRouteTaskUser), languageStr);
			return strResponsibleRole;
		}
		return DomainConstants.EMPTY_STRING;

	}

	
	/**
     * To get the Approval Status
     * @param context
     * @param args
     * @return String
     * @throws Exception
     * @since V6R2015x
     */
	public String getApprovalStatusInfoForInboxTask(Context context, String [] args) throws Exception{
		
		 final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
         final String POLICY_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Review");
         final String STRING_COMPLETED = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.LifecycleTasks.Completed", context.getLocale());
         final String STRING_APPROVED =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Approved", context.getLocale());
         final String STRING_REJECTED =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Rejected", context.getLocale());
         final String STRING_ABSTAINED =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.Abstained", context.getLocale());
         final String STRING_NEEDS_REVIEW =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Lifecycle.NeedsReview", context.getLocale());
         final String STRING_AWAITING_APPROVAL =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.LifecycleTasks.AwaitingApproval",context.getLocale());   
         final String STRING_ROUTE_STOPPED = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.LifecycleTasks.RouteStopped", context.getLocale());
         
		HashMap programMap        = (HashMap) JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
	    String strObjectId = (String)requestMap.get("objectId");
		String strApprovalStatusForTask = DomainObject.EMPTY_STRING;
		StringList objectSelects=new StringList();
		objectSelects.add(DomainObject.SELECT_TYPE);
		objectSelects.add(strAttrTaskApprovalStatus);
		objectSelects.add(routeApprovalStatusSelectStr);
		objectSelects.add(DomainObject.SELECT_CURRENT);
		objectSelects.add(DomainObject.SELECT_NAME);
	         Map collMap = DomainObject.newInstance(context, strObjectId).getInfo(context, objectSelects);
	         String sTaskType  = (String)collMap.get(DomainObject.SELECT_TYPE);
	         
	         String strCurrentState  = (String)collMap.get(DomainObject.SELECT_CURRENT);
	         String strApprovalStatus  = (String)collMap.get(strAttrTaskApprovalStatus);
	         String strRouteStatus  = (String)collMap.get(routeApprovalStatusSelectStr);
	         
	         if(sTypeInboxTask.equals(sTaskType)){
	        	 if (POLICY_INBOX_TASK_STATE_COMPLETE.equalsIgnoreCase(strCurrentState)) {
                         if ("Approve".equals(strApprovalStatus)) {
                        	 strApprovalStatusForTask=STRING_APPROVED;
                         }
                         else if ("Reject".equals(strApprovalStatus)) {
                        	 strApprovalStatusForTask=STRING_REJECTED;
                         }
                         else if ("Abstain".equals(strApprovalStatus)) {
                        	 strApprovalStatusForTask=STRING_ABSTAINED;
                         }
                         else {
                        	 strApprovalStatusForTask=STRING_COMPLETED;
                         }
                     }
                     else if(POLICY_INBOX_TASK_STATE_REVIEW.equals(strCurrentState)){
                    	 strApprovalStatusForTask=STRING_NEEDS_REVIEW;
                     }
                     else {
                    	 if ("Stopped".equals(strRouteStatus)) {
                    		 strApprovalStatusForTask=STRING_ROUTE_STOPPED;
                         } else {
                        	 strApprovalStatusForTask=STRING_AWAITING_APPROVAL;
                         }
                     }
	         }else{
	        	 strApprovalStatusForTask=DomainConstants.EMPTY_STRING;
	         }
	    
		return strApprovalStatusForTask;
	}   

/**
     * To check if ResponsibleRole is enabled
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     * @since R419
     */
	public boolean checkIfResponsibleRoleEnabled(Context context, String [] args) throws Exception{
		
		boolean isResponsibleRoleEnabled=false;
		InboxTask inboxTask=new InboxTask();
		isResponsibleRoleEnabled = inboxTask.checkIfResponsibleRoleEnabled(context);
		return !isResponsibleRoleEnabled;

	}   	
}
