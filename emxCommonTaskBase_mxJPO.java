/* emxCommonTaskBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

*/

import java.text.SimpleDateFormat;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import matrix.db.AccessConstants;
import matrix.db.Context;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.FrameworkStringResource;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;

/**
 * The <code>emxTaskBase</code> class represents the Task JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonTaskBase_mxJPO extends com.matrixone.apps.common.Task
{
    // Create an instant of emxUtil JPO

    /** Id of the Access List Object for this Project. */
    protected DomainObject _accessListObject = null;

    /** The project access list id relative to project. */
    static protected final String SELECT_PROJECT_ACCESS_LIST_ID =
            "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";

    /** state "Create" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_CREATE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Create");

    /** state "Assign" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_ASSIGN =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Assign");

    /** state "Active" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_ACTIVE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Active");

    /** state "Review" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_REVIEW =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Review");

    /** state "Complete" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_COMPLETE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_TASK,
                                           "state_Complete");

    /** state "Archive" for the "Project Task" policy. */
    public static final String STATE_PROJECT_TASK_ARCHIVE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_PROJECT_SPACE,
                                           "state_Archive");

    /** state "Complete" for the "Project Task" policy. */
    public static final String STATE_BUSINESS_GOAL_CREATED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_BUSINESS_GOAL,
                                           "state_Created");

    /** state "Complete" for the "Project Task" policy. */
    public static final String STATE_BUSINESS_GOAL_ACTIVE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_BUSINESS_GOAL,
                                           "state_Active");

    /*
     * Added:nr2:PRG:R210:For Project Hold and Cancel Highlight
     */

    static protected final String POLICY_PROJECT_SPACE_HOLD_CANCEL =
        PropertyUtil.getSchemaProperty("policy_ProjectSpaceHoldCancel");

    /** The parent type of the task. */
    static protected final String SELECT_SUBTASK_TYPE =
            "to[" + RELATIONSHIP_SUBTASK + "].from.type";

    /** used in triggerPromoteAction and triggerDemoteAction functions. */
    boolean _doNotRecurse = false;

    /** Keeps track of confirmed accesses for the context. */
    protected ArrayList _passedAccessTypes = new ArrayList(10);

    /** Cache of PAL Objects. */
    protected Map _palObjects = new HashMap(10);

    /** Cache of Project Policies. */
    protected Map _projectPolicies = new HashMap(10);

    protected emxMailUtil_mxJPO mailUtil = null;


    /**
     * Constructs a new emxTask JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args an array of String arguments for this method
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public emxCommonTaskBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super();
        if (args != null && args.length > 0)
        {
            setId(args[0]);
        }
        mailUtil = new emxMailUtil_mxJPO(context, null);
    }

    /**
     * Trigger Method to check whether the state of the Task object is 'Active' and also the accesses for relationship Task Deliverable
     * @param context - the eMatrix <code>Context</code> object
     * @param args - args contains the Ids of Context Task Object, Document object and RelationshipId
     * @return - int (0 or 1) 0 - If success and 1 - If blocked.
     * @throws Exception if the operation fails
     * @since AEF 10.0.5.0
     */
    public int checkStateAndUser(Context context, String[] args) throws Exception {
        int iFlag = 1;

        try{

            //Instantiating the Domain Object for Task
            DomainObject dmObj = newInstance(context, args[0]);

            //build a select
            StringBuffer sel = new StringBuffer("to[");
            sel.append(RELATIONSHIP_PROJECT_ACCESS_KEY);
            sel.append("].from.from[");
            sel.append(RELATIONSHIP_PROJECT_ACCESS_LIST);
            sel.append("].to.type");

            String pqpType = dmObj.getInfo(context, sel.substring(0));
            //Check to see if Task is connected to PQP (Supplier Central), or else
            //disregard the whole thing (Program Central).
            if(!((DomainConstants.TYPE_PART_QUALITY_PLAN).equals(pqpType)))
            {
                return 0; //This is not from Supplier Central
            }

            //To get actual policy Name
            String strTaskPolicy =
                            PropertyUtil.getSchemaProperty(
                                context,
                                "policy_ProjectTask");
            //Getting and representing the state 'Create'
            String strActualStateName =
                    FrameworkUtil.lookupStateName(context, strTaskPolicy, "state_Active");


            //Getting the current state and checking if it is in Create state.
            String strCurrentState = dmObj.getInfo(context, DomainConstants.SELECT_CURRENT);

            
            Locale strLocale = context.getLocale();
            String strError = "";
            if(!strCurrentState.equals(strActualStateName)){
                strError = EnoviaResourceBundle.getProperty(context,
                                            "emxComponentsStringResource",
                                            strLocale,
                                            "emxComponents.WBSTask.CheckActiveState");
                emxContextUtil_mxJPO.mqlNotice(context, strError);
            }
            else{

                String currentUserId = com.matrixone.apps.common.Person.getPerson(context,
                                                context.getUser()).getObjectId();
                // where clause for current Task assignee
                String busWhere = "id == " + currentUserId;

                // define object selectables
                StringList busSelects = new StringList(2);
                busSelects.add(SELECT_ID);
                busSelects.add(SELECT_NAME);
                MapList taskAssignee = dmObj.getRelatedObjects (context,
                                             RELATIONSHIP_ASSIGNED_TASKS,
                                             QUERY_WILDCARD,
                                             true,
                                             false,
                                             1,
                                             busSelects,
                                             null,
                                             busWhere,
                                             null,
                                             null,
                                             null,
                                             null);

                if(taskAssignee != null && taskAssignee.size() > 0)
                {
                    iFlag = 0;
                }
                else
                {
                    strError = EnoviaResourceBundle.getProperty(context,
                                                "emxComponentsStringResource",
                                                strLocale,
                                                "emxComponents.WBSTask.CheckTaskAssignee");
                    emxContextUtil_mxJPO.mqlNotice(context, strError);
                }

            }

        }catch(Exception ex){
            ex.printStackTrace(System.out);
            throw ex;
        }

        return iFlag;
    }

    /**
     * This function verifies the user's permission for the given task.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - [PROJECT_MEMBER|PROJECT_LEAD|TASK_ASSIGNEE|TASK_OWNER] <BR>
     *      PROJECT_MEMBER to see if the context user is a project member, <BR>
     *      PROJECT_LEAD to see if the context user is a project lead, <BR>
     *      TASK_ASSIGNEE to see if the context user is an assignee of this
     *                    task, <BR>
     *      TASK_OWNER to see if the context user is an owner of any of the
     *                    direct parent tasks
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     * @grade 0
     */
    public boolean hasAccess(Context context, String args[])
        throws Exception
    {
        //program[emxTask ${OBJECTID} PROJECT_MEMBER -method hasAccess] == true
        setId(args[0]);

        boolean access = false;
        for (int i = 1; i < args.length; i++)
        {
            String accessType = args[i];

            // Check if access has already been checked.
            if (_passedAccessTypes.indexOf(args[0]+accessType) != -1)
            {
                access = true;
                break;
            }

            DomainObject accessListObject = null;
            java.util.Set pals = _palObjects.keySet();
            Iterator itr = pals.iterator();
            while (itr.hasNext())
            {
                String palId = (String) itr.next();
                StringList tasks = (StringList) _palObjects.get(palId);
                if (tasks.contains(args[0]))
                {
                    accessListObject = new DomainObject(palId);
                    break;
                }
            }
            if (accessListObject == null)
            {
                accessListObject = getAccessListObject(context);
                if (accessListObject != null)
                {
                	String output = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",accessListObject.getId(),"from[Project Access Key].to.id","|");
                    StringList tasks = FrameworkUtil.split(output, "|");
                    _palObjects.put(accessListObject.getId(), tasks);
                }
            }

            //Added:nr2:02-06-2010:PRG:R210:For Phase Gate Highlight
            //Get the Parent Project For this Task and check if it is in Hold or Cancel
            //States. If so, such projects will not be listed hence below conditions will
            //be modified. Only Project Lead/Project Owner will be able to view the tasks.
            boolean isParentProjectInHoldOrCancel = isParentProjectInHoldOrCancel(context, accessListObject);
            //End:nr2:02-06-2010:PRG:R210:For Phase Gate Highlight

            if ("TASK_ASSIGNEE".equals(accessType) && !isParentProjectInHoldOrCancel)//Added:nr2:PRG:R210:For Project
            {                                                                        //Hold and Cancel Highlight
                String objectWhere = "name == \"" + context.getUser() + "\"";
                MapList mapList = getAssignees(context,
                                               null,     // objectSelects,
                                               null,     // relationshipSelects
                                               objectWhere);
                access = mapList.size() > 0 ? true : false;
            }
            else if ("PROJECT_MEMBER".equals(accessType) ||
                     "PROJECT_LEAD".equals(accessType) ||
                     "PROJECT_OWNER".equals(accessType))
            {
                if (accessListObject != null)
                {
                    int iAccess;
                    if ("PROJECT_MEMBER".equals(accessType)) //Added:nr2:PRG:R210:For Project
                    {                                        //Hold and Cancel Highlight
                        iAccess = AccessConstants.cExecute;
                    }
                    else if ("PROJECT_LEAD".equals(accessType))
                    {
                        iAccess = AccessConstants.cModify;
                    }
                    else
                    {
                        iAccess = AccessConstants.cOverride;
                    }
                    if (accessListObject.checkAccess(context, (short) iAccess))
                    {
                        access = true;
                    }
                }
            }
            if (access == true)
            {
                _passedAccessTypes.add(args[0]+accessType);
                break;
            }
        }
        return access;
    }
  //Added:nr2:02-06-2010:PRG:R210:For Phase Gate Highlight
    private boolean isParentProjectInHoldOrCancel(Context context, DomainObject accessListObject) throws MatrixException{
        boolean isParentProjectInHoldOrCancel = false;
        try{
            // First deciding if this object is initialized with object id, if not we shall skip this check
            //String thisObjectId = this.getObjectId();
            //if (thisObjectId != null && !"".equals(thisObjectId.trim()) && !"null".equals(thisObjectId.trim())) {
            //
            // In case this is new task being created from Project Template, it is possible that it is not
            // yet connected to PAL object and hence the accesses cannot be determined completely. In here
            // as we cannot get to the project we do not know about the state of the project. Hence checking
            // availability of PAL object is important.
            // IR-065449V6R2011x!

            if (accessListObject != null)
            {
                String parentProjectPolicy = (String) _projectPolicies.get(accessListObject.getId());
                if (parentProjectPolicy == null)
                {
                    //get Parent Project
                    StringList sl = new StringList(2);
                    sl.add(SELECT_ID);
                    sl.add(SELECT_POLICY);

                    Map projectMap = getProject(context, sl);
                    parentProjectPolicy = (String)projectMap.get(SELECT_POLICY);
                    _projectPolicies.put(accessListObject.getId(), parentProjectPolicy);
                }
                if(POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(parentProjectPolicy))
                {
                    isParentProjectInHoldOrCancel = true;
                }
            }
        }
        catch(Exception e){
            throw new MatrixException(e);
        }
        return isParentProjectInHoldOrCancel;
    }
  //End:nr2:02-06-2010:PRG:R210:For Phase Gate Highlight
    /**
     * This function verifies the user's permission for the given task.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args empty - for future use
     * @return true if the person has either "Supplier Quality Engineer" or
     *              "Supplier Representative" role or both.
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     * @grade 0
     */
    public boolean hasSupplierRoles(Context context, String args[])
        throws Exception
    {
        String roleSQE = PropertyUtil.getSchemaProperty(context,"role_SupplierQualityEngineer");
        String roleSR  = PropertyUtil.getSchemaProperty(context,"role_SupplierRepresentative");

        com.matrixone.apps.common.Person person =
           com.matrixone.apps.common.Person.getPerson(context);

        return (person.hasRole(context,roleSR) ||  person.hasRole(context,roleSQE));
    }

     /**
     * When the task is promoted this function is called.
     * Depending on which state the promote is triggered from
     * it performs the necessary actions based on the arg-STATENAME value
     *
     * Note: object id must be passed as first argument.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args OBJECTID STATENAME NEXTSTATE
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public void triggerPromoteAction(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering triggerPromoteAction");

        // get values from args.
        String objectId  = args[0];
        String fromState = args[1];
        String toState   = args[2];
        String checkAssignees = args[3];

        setId(objectId);

        String strParentType = getInfo(context, SELECT_SUBTASK_TYPE);

        if (strParentType != null && strParentType.equals(TYPE_PART_QUALITY_PLAN))
        {
            _doNotRecurse = false;
        }

        if (fromState.equals(STATE_PROJECT_TASK_CREATE) && _doNotRecurse)
        {
            return;
        }

        java.util.ArrayList taskToPromoteList = new java.util.ArrayList();
        StringList busSelects = new StringList(4);
        busSelects.add(SELECT_ID);
        busSelects.add(SELECT_CURRENT);
        busSelects.add(SELECT_STATES);
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_TYPE);

        if (fromState.equals(STATE_PROJECT_TASK_CREATE))
        {
            //The first time this function is called this value will be false
            //second time around this will be true
            //The reason for doing this is since getTasks function gets all the
            //sub-tasks in one call all the sub-tasks are promoted in one pass
            //thereon if the sub-tasks call the function it returns without
            //doing anything
            //if (_doNotRecurse)
            //{
                //function called recursively return without doing anything
            //    return;
            //}

            _doNotRecurse = true;
            busSelects.add(SELECT_HAS_ASSIGNED_TASKS);

            // get all the subtasks
            MapList utsList = getTasks(context, this, 0, busSelects, null);
            if (utsList.size() > 0)
            {
                Iterator itr = utsList.iterator();
                while (itr.hasNext())
                {
                    boolean promoteTask = false;
                    Map map = (Map) itr.next();
                    String state = (String) map.get(SELECT_CURRENT);
                    StringList taskStateList =
                            (StringList) map.get(SELECT_STATES);

                    //get the position of the task's current state wrt
                    //to its state list
                    int taskCurrentPosition = taskStateList.indexOf(state);

                    //get the position to which the task need to be promoted
                    //if the toState does not exist then taskPromoteToPosition
                    //will be -1
                    int taskPromoteToPosition = taskStateList.indexOf(toState);
                    //check if the toState exists and if the current
                    //position of the task is less than the toState
                    if(taskPromoteToPosition != -1 &&
                       taskCurrentPosition < taskPromoteToPosition)
                    {
                        if ("true".equalsIgnoreCase(checkAssignees))
                        {
                            //is this task assigned to anyone?
                            //if true promote otherwise do not promote
                            if ("true".equalsIgnoreCase(
                                (String) map.get(SELECT_HAS_ASSIGNED_TASKS)))
                            {
                                promoteTask = true;
                            }
                        }
                        else
                        {
                            //task can be promoted even if the task is not
                            //assigned to anyone
                            promoteTask = true;
                        }
                    }
                    if (promoteTask)
                    {
                        String taskId = (String) map.get(SELECT_ID);
                        taskToPromoteList.add(taskId);
                    }
                }
            }
        }
        else if (fromState.equals(STATE_PROJECT_TASK_ASSIGN))
        {
        //******************start Business Goal promote to Active state*********
            //when the project is promoted from the assign to active state
            //promote the business goal if it is the first business goal
            //use super user to overcome access issue

/*


            ContextUtil.pushContext(context);
            try
            {
                 com.matrixone.apps.program.BusinessGoal businessGoal =
                   (com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context,
                   DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
                 com.matrixone.apps.program.ProjectSpace project =
                   (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                   DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
                 project.setId(objectId);
                 MapList businessGoalList = new MapList();
                 businessGoalList = businessGoal.getBusinessGoals(context, project, busSelects, null);
                 if (null != businessGoalList && businessGoalList.size()>0){
                   Iterator businessGoalItr = businessGoalList.iterator();
                   while(businessGoalItr.hasNext()) {
                     Map businessGoalMap = (Map) businessGoalItr.next();
                     String businessGoalId = (String) businessGoalMap.get(businessGoal.SELECT_ID);
                     String businessGoalState = (String) businessGoalMap.get(businessGoal.SELECT_CURRENT);
                     businessGoal.setId(businessGoalId);
                     if(fromState.equals(STATE_PROJECT_TASK_ASSIGN) && businessGoalState.equals(STATE_BUSINESS_GOAL_CREATED)){
                       businessGoal.changeTheState(context);
                     } //ends if
                   }//ends while
                 }//ends if
            }//ends try
            catch (Exception e)
            {
                DebugUtil.debug("Exception Task triggerPromoteAction- ",
                                e.getMessage());
                throw e;
            }
            finally
            {
                ContextUtil.popContext(context);
            }
*/

          //******************end Business Goal promote to Active state*********

            //when the task is promoted from Assign to Active
            //promote the parent to Active

            //get the parent task
            MapList parentList = getParentInfo(context, 1, busSelects);
            if (parentList.size() > 0)
            {
                Map map = (Map) parentList.get(0);
                String state = (String) map.get(SELECT_CURRENT);
                StringList taskStateList = (StringList) map.get(SELECT_STATES);

                //get the position of the task's current state wrt to
                //its state list
                int taskCurrentPosition = taskStateList.indexOf(state);

                //get the position to which the task need to be promoted
                //if the toState does not exist then taskPromoteToPosition
                //will be -1
                int taskPromoteToPosition = taskStateList.indexOf(toState);

                //check if the toState exists and if the current
                //position of the task is less than the toState
                if (taskPromoteToPosition != -1 &&
                    taskCurrentPosition < taskPromoteToPosition)
                {
                    String taskId = (String) map.get(SELECT_ID);
                    Task task = new Task(taskId);
                    //use super user to overcome access issue
                    ContextUtil.pushContext(context);
                    try
                    {
                        //setId(taskId);
                        //setState(context, toState);
                        task.setState(context, toState);
                    }
                    finally
                    {
                        ContextUtil.popContext(context);
                    }
                }
            }
        }
        else if(fromState.equals(STATE_PROJECT_TASK_ACTIVE))
        {
            //do nothing for now
        }
        else if(fromState.equals(STATE_PROJECT_TASK_REVIEW))
        {
        //******************start Business Goal promote****to Complete state****
             //when the project is promoted from the review to complete state
            //promote the business goal if it is the first business goal
            //use super user to overcome access issue

/*


            ContextUtil.pushContext(context);
            try
            {
                 com.matrixone.apps.program.BusinessGoal businessGoal =
                   (com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context,
                   DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
                 com.matrixone.apps.program.ProjectSpace project =
                   (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                   DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
                 project.setId(objectId);
                 MapList businessGoalList = new MapList();
                 businessGoalList = businessGoal.getBusinessGoals(context, project, busSelects, null);
                 if (null != businessGoalList && businessGoalList.size()>0){
                   Iterator businessGoalItr = businessGoalList.iterator();
                   while(businessGoalItr.hasNext()) {
                     Map businessGoalMap = (Map) businessGoalItr.next();
                     String businessGoalId = (String) businessGoalMap.get(businessGoal.SELECT_ID);
                     String businessGoalState = (String) businessGoalMap.get(businessGoal.SELECT_CURRENT);
                     businessGoal.setId(businessGoalId);
                     if(fromState.equals(STATE_PROJECT_TASK_REVIEW) && businessGoalState.equals(STATE_BUSINESS_GOAL_ACTIVE)){
                       MapList projectList = businessGoal.getProjects(context, busSelects, null);
                       boolean changeState = true;
                       if (null != projectList && projectList.size()>0){
                         Iterator projectItr = projectList.iterator();
                         while(projectItr.hasNext()) {
                           Map projectMap = (Map) projectItr.next();
                           String projectId = (String) projectMap.get(project.SELECT_ID);
                           String projectState = (String) projectMap.get(project.SELECT_CURRENT);
                           if(!projectState.equals(STATE_PROJECT_TASK_COMPLETE) && !projectState.equals(STATE_PROJECT_TASK_ARCHIVE)  && !projectId.equals(objectId)){
                             changeState = false;
                           }
                         }
                         if(changeState){
                           businessGoal.changeTheState(context);
                         }//ends if
                       }//ends if
                     }//ends if
                   }//ends while
                 }//ends if
            }//ends try
            catch (Exception e)
            {
                DebugUtil.debug("Exception Task triggerPromoteAction- ",
                                e.getMessage());
                throw e;
            }
            finally
            {
                ContextUtil.popContext(context);
            }

*/
          //******************end Business Goal promote to Complete state*******

            MapList parentList = getParentInfo(context, 1, busSelects);
            if (parentList.size() > 0)
            {
                Map map = (Map) parentList.get(0);
                String state = (String) map.get(SELECT_CURRENT);
                String parentType = (String) map.get(SELECT_TYPE);
                StringList parentStateList = (StringList)map.get(SELECT_STATES);

                if (state.equals(STATE_PROJECT_TASK_ACTIVE))
                {
                    String parentId = (String) map.get(SELECT_ID);
                    //set up the args as required for the check trigger
                    //check whether all the children for this parent is in the
                    //specified state
                    //String sArgs[] = {parentId, "", "state_Complete"};

                    setId(parentId);
                    boolean checkPassed = checkChildrenStates(context,
                                STATE_PROJECT_TASK_COMPLETE, null);

                    //int status = triggerCheckChildrenStates(context, sArgs);

                    //all children in complete state
                    if (checkPassed)
                    {
                        //get the position of the task's current state wrt to
                        //its state list
                        int taskCurrentPosition =parentStateList.indexOf(state);

                        //get the position of the Review state wrt to its
                        //state list
                        int taskPromoteToPosition =
                            parentStateList.indexOf(STATE_PROJECT_TASK_REVIEW);
                        if (parentType.equals(TYPE_PART_QUALITY_PLAN))
                        {
                          taskPromoteToPosition =
                              parentStateList.indexOf(STATE_PART_QUALITY_PLAN_COMPLETE);

                        }
                        //Review state exists and is the state next to Active.
                        //Promote the parent
                        if (taskPromoteToPosition != -1 &&
                            taskPromoteToPosition == (taskCurrentPosition + 1))
                        {
                            //use super user to overcome access issue
                            ContextUtil.pushContext(context);
                            try
                            {
                                setId(parentId);
                                promote(context);
                            }
                            finally
                            {
                                ContextUtil.popContext(context);
                            }
                        }
                    }
                }
            }
        }
        if(! taskToPromoteList.isEmpty())
        {
            //promote each of the tasks in the list
            //use super user to overcome access issue
            ContextUtil.pushContext(context);
            try
            {
                for(int i=0; i<taskToPromoteList.size(); i++)
                {
                    String id = (String)taskToPromoteList.get(i);
                    setId(id);
                    promote(context);
                }
            }
            catch (Exception e)
            {
                DebugUtil.debug("Exception Task triggerPromoteAction- ",
                                e.getMessage());
                throw e;
            }
            finally
            {
                ContextUtil.popContext(context);
            }
        }

        DebugUtil.debug("Exiting Task triggerPromoteAction");
    }

     /**
     * When the task is demoted this function is called.
     * Depending on which state the promote is triggered from
     * it performs the necessary actions based on the arg-STATENAME value
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args OBJECTID STATENAME NEXTSTATE
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public void triggerDemoteAction(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering triggerDemoteAction");

        // get values from args.
        String objectId  = args[0];
        String fromState = args[1];
        String toState   = args[2];

        setId(objectId);

        StringList busSelects = new StringList(4);
        busSelects.add(SELECT_ID);
        busSelects.add(SELECT_CURRENT);
        busSelects.add(SELECT_STATES);
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_TYPE);

        if (fromState.equals(STATE_PROJECT_TASK_ASSIGN))
        {
            //check if the parent is in Assign state
            //then check the status of the siblings
            //if none of the siblings are in assign state
            //the demote the parent to "Create" state
            MapList parentList = getParentInfo(context, 1, busSelects);
            boolean demoteParent = false;
            if (parentList.size() > 0)
            {
                Map map = (Map) parentList.get(0);
                String state = (String) map.get(SELECT_CURRENT);
                String parentId = (String) map.get(SELECT_ID);
                setId(parentId);

                if (state.equals(STATE_PROJECT_TASK_ASSIGN))
                {
                    //check if atleast one child is in assign state
                    // get children (one level)
                    MapList utsList = getTasks(context, this, 1,
                                               busSelects, null);
                    if (utsList.size() > 0)
                    {
                        demoteParent = true;
                        Iterator itr = utsList.iterator();
                        while (itr.hasNext())
                        {
                            Map taskmap = (Map) itr.next();
                            state = (String) taskmap.get(SELECT_CURRENT);
                            StringList taskStateList =
                                    (StringList) map.get(SELECT_STATES);

                            //get the position of the task's current state wrt
                            //to its state list
                            int taskCurrentPosition =
                                    taskStateList.indexOf(state);

                            //get the position of "Assign" in the state list
                            //if the "Assign" state does not exist then
                            //taskAssignPostion will be -1
                            int taskAssignStatePosition = taskStateList.indexOf(
                                    STATE_PROJECT_TASK_ASSIGN);

                            //if the current task position is less then the
                            //taskAssignStatePosition, demote the task
                            if (taskAssignStatePosition == -1 ||
                                taskCurrentPosition >= taskAssignStatePosition)
                            {
                                demoteParent = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (demoteParent)
            {
                //use super user to overcome access issue
                ContextUtil.pushContext(context);
                try
                {
                    demote(context);
                }
                finally
                {
                    ContextUtil.popContext(context);
                }
            }
        }
        else if (fromState.equals(STATE_PROJECT_TASK_ACTIVE))
        {
            //when the task is demoted from Active to Assign
            //if this is the last sibling being demoted,
            //demote the parent too

            //get the parent task
            MapList parentList = getParentInfo(context, 1, busSelects);
            boolean demoteParent = true;
            if(parentList.size() > 0)
            {
                Map map = (Map) parentList.get(0);
                String state = (String) map.get(SELECT_CURRENT);
                String parentId = (String) map.get(SELECT_ID);
                String type = (String) map.get(SELECT_TYPE);
                setId(parentId);

                if (state.equals(STATE_PROJECT_TASK_ACTIVE) && !TYPE_PART_QUALITY_PLAN.equals(type))
                {
                    //get children (one level)
                    MapList utsList = getTasks(context, this, 1,
                                               busSelects, null);
                    if (utsList.size() > 0)
                    {
                        Iterator itr = utsList.iterator();
                        while (itr.hasNext())
                        {
                            Map taskmap = (Map) itr.next();
                            state = (String) taskmap.get(SELECT_CURRENT);
                            StringList taskStateList =
                                    (StringList) map.get(SELECT_STATES);

                            //get the position of the task's current state wrt to its state list
                            int taskCurrentPosition =
                                    taskStateList.indexOf(state);

                            //get the position of "Active" in the state list
                            //if the "Active" state does not exist then
                            //taskActivePostion will be -1
                            int taskActiveStatePosition = taskStateList.indexOf(
                                    STATE_PROJECT_TASK_ACTIVE);

                            //check if any of the tasks are in active state.
                            //If there are no tasks in active state, then demote
                            //the parent task
                            if (taskActiveStatePosition != -1 &&
                                taskCurrentPosition >= taskActiveStatePosition)
                            {
                                demoteParent = false;
                                break;
                            }
                        }
                    }
                }
                else
                {
                    demoteParent = false;
                }
            }
            if (demoteParent)
            {
                //use super user to overcome access issue
                ContextUtil.pushContext(context);
                try
                {
                    demote(context);
                }
                finally
                {
                    ContextUtil.popContext(context);
                }
            }
        }
        else if(fromState.equals(STATE_PROJECT_TASK_REVIEW))
        {
            //Action:Check whether the parent is in review state if so demote
            //it to active state
            //i.e when the first child is demoted from Review to Active
            // demote the parent too

            //get the parent task
            MapList parentList = getParentInfo(context, 1, busSelects);
            if(parentList.size() > 0)
            {
                Map map = (Map) parentList.get(0);
                String state = (String) map.get(SELECT_CURRENT);
                String parentId = (String) map.get(SELECT_ID);
                setId(parentId);

                //parent is in Review state
                if(state.equals(STATE_PROJECT_TASK_REVIEW))
                {
                    StringList taskStateList =
                            (StringList) map.get(SELECT_STATES);
                    //get the position of the task's current state wrt to
                    //its state list
                    int taskCurrentPosition = taskStateList.indexOf(state);

                    //get the position of "Active" state in the state list
                    //if the "Active" state does not exist then
                    //taskActivePostion will be -1
                    int taskActiveStatePosition =
                            taskStateList.indexOf(STATE_PROJECT_TASK_ACTIVE);
                        //check if any of the tasks are in active state.
                    if (taskActiveStatePosition != -1 &&
                        taskCurrentPosition > taskActiveStatePosition)
                    {
                        //use super user to overcome access issue
                        ContextUtil.pushContext(context);
                        try
                        {
                            //demote the parent to Active state
                            setState(context, STATE_PROJECT_TASK_ACTIVE);
                        }
                        finally
                        {
                            ContextUtil.popContext(context);
                        }
                    }
                }
            }
        }
        if (toState.equals(STATE_PROJECT_TASK_ACTIVE))
        {
            setId(objectId);
            String value = getInfo(context, SELECT_PERCENT_COMPLETE);
            double percent = Double.parseDouble(value);
            if (90 < percent)
            {
                setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "90");
            }
        }

        DebugUtil.debug("Exiting Task triggerDemoteAction");
    }

     /**
     * This function notifies the task assignes
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args OBJECTID
     * @param args sideDoorFeature. True if URL needs to sent in notification
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public void notifyTaskAssignees(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering notifyTaskAssignee");


        // get values from args.
        String objectId = args[0];
        String sideDoorFeature = args[1];

        setId(objectId);
        StringList busSelects = new StringList(6);
        busSelects.add(SELECT_TYPE);
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_TASK_ESTIMATED_START_DATE);
        busSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
        busSelects.add(SELECT_TASK_ESTIMATED_DURATION);
        busSelects.add(SELECT_ASSIGNEES);

        Map objMap = getInfo(context, busSelects);
        String taskType          = (String) objMap.get(SELECT_TYPE);
        String taskName          = (String) objMap.get(SELECT_NAME);
        String taskEstStartDate  = (String) objMap.get(
                                        SELECT_TASK_ESTIMATED_START_DATE);
        String taskEstFinishDate = (String) objMap.get(
                                        SELECT_TASK_ESTIMATED_FINISH_DATE);
        String taskEstDuration   = (String) objMap.get(
                                        SELECT_TASK_ESTIMATED_DURATION);

        busSelects.clear();
        busSelects.add(SELECT_NAME);
        // get the assignees for this task
        MapList assigneeList = getAssignees(context, busSelects, null, null);
        if (assigneeList.size() > 0)
        {
            Iterator itr = assigneeList.iterator();
            StringList mailToList = new StringList(1);
            StringList mailCcList = new StringList(1);
            //build the To List
            //send one mail to all the assinees in one mail
            while (itr.hasNext())
            {
                Map map = (Map) itr.next();
                String personName = (String) map.get(SELECT_NAME);

                // Set the "to" list.
                mailToList.addElement(personName);
            }

            // Set the mail subject and message and send the mail.
            StringList objectIdList = null;
            if(sideDoorFeature.equals("true"))
            {
                objectIdList = new StringList(1);
                objectIdList.addElement(getId());
            }

            // Sends mail notification to the owners.
            //get the mail subject
            String sMailSubject = FrameworkStringResource.WBS_Task_Notify_Subject;
            String companyName = null;
            sMailSubject  = mailUtil.getMessage(
                                context, sMailSubject, null, null, companyName);

            //get the mail message
            String sMailMessage = FrameworkStringResource.WBS_Task_Notify_Message;
            String mKey[] = {"TaskType", "TaskName", "TaskEstStartDate",
                             "TaskEstFinishDate","TaskEstDuration"};
            String mValue[] = {taskType, taskName, taskEstStartDate,
                               taskEstFinishDate, taskEstDuration};
            sMailMessage  = mailUtil.getMessage(
                            context, sMailMessage, mKey, mValue, companyName);


            MailUtil.setAgentName(context, context.getUser());

            MailUtil.sendMessage(context, mailToList, mailCcList, null,
                                 sMailSubject, sMailMessage, objectIdList);
        }

        DebugUtil.debug("Exiting notifyTaskAssignee function");
    }

     /**
     * This function modifies the attributes
     * Sets the actual start date on promoting a task to Active state
     * Resets the actual start date on demoting a task from Active state
     *
     * Sets the actual completion date on promoting a task to Complete state
     * Resets the completion date on demoting a task from Complete state
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public void triggerModifyAttributes(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering triggerModifyAttributes");

        // get values from args.
        String objectId = args[0];
        String fromState = args[1];
        String toState = args[2];

        String cmd = "get env $1";
        String sEvent = MqlUtil.mqlCommand(context, cmd , true,"EVENT");

        /** "MATRIX_DATE_FORMAT". */
        SimpleDateFormat MATRIX_DATE_FORMAT =
            new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),
        Locale.US);

        setId(objectId);
        if(sEvent.equals("Promote"))
        {
            if (toState.equals(STATE_PROJECT_TASK_ACTIVE))
            {
                String actualStartDate = MATRIX_DATE_FORMAT.format(new Date());
                setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE,
                                  actualStartDate);
            }
            else if (toState.equals(STATE_PROJECT_TASK_COMPLETE))
            {
                //finish date
                Map attributes = new HashMap(3);
                Date fDate = new Date();
                String actualFinishDate = MATRIX_DATE_FORMAT.format(fDate);
                attributes.put(ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,
                               actualFinishDate);
                //setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,
                //                  actualFinishDate);
                //compute duration using the actual start and finish date
                String actualStartDate = getInfo(context,
                                                 SELECT_TASK_ACTUAL_START_DATE);
                Date sDate = MATRIX_DATE_FORMAT.parse(actualStartDate);
                Integer duration = new Integer((int)
                                DateUtil.computeDuration(sDate, fDate));
                attributes.put(ATTRIBUTE_TASK_ACTUAL_DURATION,
                               duration.toString());
                //setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_DURATION,
                //                  duration.toString());
                attributes.put(ATTRIBUTE_PERCENT_COMPLETE, "100");
                setAttributeValues(context, attributes);
           }
        }
        else if (sEvent.equals("Demote"))
        {
            if (fromState.equals(STATE_PROJECT_TASK_COMPLETE))
            {
                setAttributeValue(context,ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,"");
                setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_DURATION, "");
            }
            if (fromState.equals(STATE_PROJECT_TASK_ACTIVE))
            {
                //setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE,"");
                Map attributes = new HashMap(3);
                attributes.put(ATTRIBUTE_TASK_ACTUAL_START_DATE, "");
                attributes.put(ATTRIBUTE_PERCENT_COMPLETE, "0");
                setAttributeValues(context, attributes);
            }
        }

        DebugUtil.debug("Exiting triggerModifyAttributes function");
    }

     /**
     * This function checks the states of the child tasks have reached the
     * state defined in the trigger object
     * Parameters
     * arg[0] = objectId
     * arg[1] = relationship to check for
     * arg[2] = state to check for
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public int triggerCheckChildrenStates(Context context, String[] args)
        throws Exception
    {
        // get values from args.
        String objectId = args[0];
        //type of relationship for the child to be checked for
        String childType = args[1];
        //state in which all the childTasks need to be in before the
        //parent task can be promoted
        String checkStateArg = args[2];

        String checkMandatoryOnly = null;
        if (args.length > 3)
        {
            checkMandatoryOnly = args[3];
        }
        String where = null;
        if ("True".equalsIgnoreCase(checkMandatoryOnly))
        {
            where = SELECT_TASK_REQUIREMENT + " == Mandatory";
        }

        String checkState = PropertyUtil.getSchemaProperty(context,
                                                           "policy",
                                                           POLICY_PROJECT_TASK,
                                                           checkStateArg);

        setId(objectId);

        boolean checkPassed = checkChildrenStates(context, checkState, where);

        if (checkPassed)
        {
            return 0;
        }
        else
        {
            String sErrMsg = null;
            if (where == null)
            {
                sErrMsg = FrameworkStringResource.WBS_Task_Promote_Message;
            }
            else
            {
                sErrMsg = FrameworkStringResource.WBS_Task_Promote_Mandatory_Message;
            }
            
            String sKey[] = {"StateName"};
            String sValue[] = {checkState};
            String companyName = null;
            sErrMsg  = mailUtil.getMessage(context, sErrMsg,sKey, sValue, companyName);
            emxContextUtilBase_mxJPO.mqlNotice(context,sErrMsg);
            return 1;
        }
    }

     /**
     * Check the state of all children tasks for a specific state.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param targetState the state name to compare against
     * @param whereClause optional where clause to apply against the children
     * @return true if all children are at least at the target state; false if
     *      any of the children do not meet the target state requirement.
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    protected boolean checkChildrenStates(Context context, String targetState,
                                          String whereClause)
        throws Exception
    {
        // get children one level only
        StringList busSelects = new StringList(2);
        busSelects.add(SELECT_CURRENT);
        busSelects.add(SELECT_STATES);

        MapList utsList = getRelatedObjects(context,
                                RELATIONSHIP_SUBTASK,
                                QUERY_WILDCARD,
                                busSelects,
                                null,       // relationshipSelects
                                false,      // getTo
                                true,       // getFrom
                                (short) 1,  // recurseToLevel
                                whereClause,// objectWhere
                                null);      // relationshipWhere

        boolean checkPassed = true;

        Iterator itr = utsList.iterator();
        while (itr.hasNext())
        {
            Map map = (Map) itr.next();
            String state = (String) map.get(SELECT_CURRENT);
            StringList taskStateList = (StringList) map.get(SELECT_STATES);

            //get the position of the task's current state wrt to its
            //state list
            int taskCurrentPosition = taskStateList.indexOf(state);

            //get the position for which the state of the tasks needs
            //to be checked
            int checkStatePosition = taskStateList.indexOf(targetState);

            //check if the position being checked for exists and if the
            //current position of the task is equal to or greater than
            //the checkStatePosition
            //if this is true return true
            //else return false
            if (checkStatePosition != -1 &&
                taskCurrentPosition >= checkStatePosition)
            {
                continue;
            }
            else
            {
                checkPassed = false;
                break;
            }
        }
        return checkPassed;
    }

    /**
     * Sets the %age Completion based on whether the event is promote
     * or demote and the state on which this event occurs
     *
     * Parameters
     * arg[0] = objectId
     * arg[1] = fromState
     * arg[2] = toState
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public void triggerSetPercentageCompletion(Context context, String[] args)
        throws Exception
    {
        // get values from args.
        String objectId = args[0];
        String fromState = args[1];
        String toState   = args[2];

        setId(objectId);
        String cmd = "get env $1";
        String sEvent = MqlUtil.mqlCommand(context, cmd , true,"EVENT");

        if (sEvent.equals("Promote"))
        {
            if (fromState.equals(STATE_PROJECT_TASK_ACTIVE))
            {
                //Supplier role needs to have this set after the task has been
                //promoted to Review.
                boolean isContextPushed = false;
                ContextUtil.pushContext(context);
                isContextPushed = true;
                // set the percent complete to 100%
                setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "100");
                if(isContextPushed)
                {
                    ContextUtil.popContext(context);
                }
            }
        }
        else if (sEvent.equals("Demote"))
        {
            if (toState.equals(STATE_PROJECT_TASK_ACTIVE))
            {
                // set the percent complete to 0%
                setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "0");
            }
        }
    }

    /**
     * Demote check trigger
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - ${OBJECTID}, ${STATENAME}, ${TOSTATE}
     *
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public int triggerDemoteCheck(Context context, String[] args)
        throws Exception
    {
        // get values from args.
        String objectId = args[0];
        String fromState = args[1];
        String toState   = args[2];

        setId(objectId);
        boolean blockTrigger = false;
        StringList busSelects = new StringList(2);
        busSelects.add(SELECT_CURRENT);
        busSelects.add(SELECT_STATES);

        if (fromState.equals(STATE_PROJECT_TASK_ACTIVE))
        {
            //if atleast onechild exists in Active state block the
            //parent from being demoted

            // get the children (one level)
            MapList utsList = getTasks(context, this, 1, busSelects, null);
            if(utsList.size() > 0)
            {
                Iterator itr = utsList.iterator();
                while (itr.hasNext())
                {
                    Map map = (Map) itr.next();
                    String state = (String) map.get(SELECT_CURRENT);
                    StringList stateList = (StringList) map.get(SELECT_STATES);
                    int taskCurrentPosition = stateList.indexOf(state);
                    int taskActiveStatePosition = stateList.indexOf(
                                    STATE_PROJECT_TASK_ACTIVE);
                    //if any of the child is in a state Active or beyond
                    //block the demotion
                    if(taskCurrentPosition >= taskActiveStatePosition)
                    {
                        String sErrMsg = FrameworkStringResource.WBS_Task_Summary_Demote_Message;
                        String sKey[] = {"ToStateName", "StateName"};
                        String sValue[] = {toState, fromState};
                        String companyName = null;
                        sErrMsg = mailUtil.getMessage(context, sErrMsg, sKey, sValue, companyName);
                        emxContextUtilBase_mxJPO.mqlNotice(context,sErrMsg);

                        //task in active state, block the parent from being demoted
                        blockTrigger = true;
                        break;
                    }
                }
            }
        }
        else if (fromState.equals(STATE_PROJECT_TASK_COMPLETE))
        {
            MapList parentList = getParentInfo(context, 1, busSelects);
            if (!parentList.isEmpty())
            {
            Map map = (Map) parentList.get(0);
            String parentState = (String) map.get(SELECT_CURRENT);
            //If the parent state is in state "Complete" or beyond block
            //demote of the child.  Parent needs to be demoted to Review before
            //the child can be demoted from Complete state

            //get the position of the parent's current state wrt to its state list
            StringList parentStateList = (StringList) map.get(SELECT_STATES);
            int currentStatePosition = parentStateList.indexOf(parentState);
            int completeStatePosition = parentStateList.indexOf(STATE_PROJECT_TASK_COMPLETE);
            if(currentStatePosition >= completeStatePosition && completeStatePosition != -1)
            {
                blockTrigger = true;
                String sErrMsg = FrameworkStringResource.WBS_Task_Parent_Demote_Message;
                String sKey[] = {"StateName"};
                String sValue[] = {parentState};
                String companyName = null;
                sErrMsg = mailUtil.getMessage(context, sErrMsg, sKey, sValue, companyName);
                emxContextUtilBase_mxJPO.mqlNotice(context,sErrMsg);
            }
        }
        }

        if (blockTrigger)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Promote check trigger
     * Checks if all the dependency requirements are met
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - ${OBJECTID}, ${STATENAME}, ${TOSTATE}
     *
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public int triggerCheckDependency(Context context, String[] args)
        throws Exception
    {
        // get values from args.
        String objectId = args[0];
        String fromState = args[1];
        String toState   = args[2];

        String requiredState = null;
        String dependencyTaskName  = null;
        String type = null;

        setId(objectId);
        boolean promoteTask = true;
        StringList busSelects = new StringList(2);
        busSelects.add(SELECT_CURRENT);
        busSelects.add(SELECT_STATES);
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_HAS_PREDECESSORS);
        busSelects.add(SELECT_HAS_SUCCESSORS);

        MULTI_VALUE_LIST.add(SELECT_PREDECESSOR_IDS);
        MULTI_VALUE_LIST.add(SELECT_PREDECESSOR_TYPES);
        busSelects.add(SELECT_PREDECESSOR_IDS);
        busSelects.add(SELECT_PREDECESSOR_TYPES);
        Map objMap = getInfo(context, busSelects, MULTI_VALUE_LIST);

        String hasPredecessors = (String) objMap.get(SELECT_HAS_PREDECESSORS);
        String hasSuccessors = (String) objMap.get(SELECT_HAS_SUCCESSORS);
        Object predecessorIds = (Object) objMap.get(SELECT_PREDECESSOR_IDS);
        Object predecessorTypes = (Object) objMap.get(SELECT_PREDECESSOR_TYPES);

        StringList preIds = new StringList();
        StringList preTypes = new StringList();
        //check whether the dependency list has one or many ids
        if (predecessorIds instanceof String){
            preIds.add((String)predecessorIds);
            preTypes.add((String)predecessorTypes);
        } else if (predecessorTypes instanceof StringList) {
            preIds = (StringList) predecessorIds;
            preTypes = (StringList) predecessorTypes;
        }

        if (hasPredecessors.equals("True"))
        {
            Iterator itr = preIds.iterator();
            Iterator itr1 = preTypes.iterator();
            while(itr.hasNext())
            {
                String id = (String)itr.next();
                type = (String)itr1.next();
                setId(id);

                busSelects.clear();
                busSelects.add(SELECT_CURRENT);
                busSelects.add(SELECT_STATES);
                busSelects.add(SELECT_NAME);

                Map predecessorMap = getInfo(context, busSelects);
                String state = (String) predecessorMap.get(SELECT_CURRENT);
                StringList stateList =
                       (StringList) predecessorMap.get(SELECT_STATES);
                dependencyTaskName = (String) predecessorMap.get(SELECT_NAME);

                //get the position of active, complete and current state
                int pCurrentPosition = stateList.indexOf(state);
                int pActiveStatePosition =
                stateList.indexOf(STATE_PROJECT_TASK_ACTIVE);
                int pCompleteStatePosition =
                stateList.indexOf(STATE_PROJECT_TASK_COMPLETE);

                if (toState.equals(STATE_PROJECT_TASK_ACTIVE))
                {
                    if (type.equals("FS"))
                    {
                        //The predecessor task needs to be "Completed" before
                        //this task can go to active state
                        if (pCurrentPosition < pCompleteStatePosition)
                        {
                            requiredState = STATE_PROJECT_TASK_COMPLETE;
                            promoteTask = false;
                            break;
                        }
                    }
                    else if (type.equals("SS"))
                    {
                        //The predecessor task needs to be "Started" before
                        //this task can go to active state
                        if (pCurrentPosition < pActiveStatePosition)
                        {
                            requiredState = STATE_PROJECT_TASK_ACTIVE;
                            promoteTask = false;
                            break;
                        }
                    }
                }
                if (toState.equals(STATE_PROJECT_TASK_COMPLETE))
                {
                    if (type.equals("SF"))
                    {
                        //The predecessor task needs to be "Started" before
                        //this task can go to complete state
                        if (pCurrentPosition < pActiveStatePosition)
                        {
                            requiredState = STATE_PROJECT_TASK_ACTIVE;
                            promoteTask = false;
                            break;
                        }
                    }
                    else if (type.equals("FF"))
                    {
                        //The predecessor task needs to be "Completed" before
                        //this task can go to complete state
                        if (pCurrentPosition < pCompleteStatePosition)
                        {
                            requiredState = STATE_PROJECT_TASK_COMPLETE;
                            promoteTask = false;
                            break;
                        }
                    }
                }
            }
        }
        if (promoteTask)
        {
            return 0;
        }
        else
        {
            String sErrMsg = FrameworkStringResource.WBS_Task_Dependency_Message;
            String sKey[] = {"dependencyTaskName", "dependencyType",
                             "requiredState"};
            String sValue[] = {dependencyTaskName, type, requiredState};
            String companyName = null;
            sErrMsg  = mailUtil.getMessage(context,
                            sErrMsg, sKey, sValue, companyName);
            emxContextUtilBase_mxJPO.mqlNotice(context,sErrMsg);
            return 1;
        }
    }

    /**
    * Modify action for  Percentage complete attribute
    * Based on the %age set the trigger will promote or demote
    * the object to the required state
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - ${OBJECTID}, ${STATENAME}, ${TOSTATE}
    *
    * @throws Exception if operation fails
    * @since AEF 9.5.1.3
    * @grade 0
    */
    public void triggerModifyPercentCompleteAction(Context context,
                                                   String[] args)
        throws Exception
    {
        // get values from args.
        String objectId = args[0];

        //Determine if percent rollup is required.
        setId(objectId);
        StringList busSelects = new StringList();

        busSelects.add(SELECT_PERCENT_COMPLETE);
        busSelects.add(SELECT_CURRENT);
        busSelects.add(SELECT_STATES);
        busSelects.add(SELECT_PARENT_ID);
        busSelects.add(SELECT_TYPE);

        Map taskMap = getInfo(context, busSelects);
        String type = (String) taskMap.get(SELECT_TYPE);
        String state = (String) taskMap.get(SELECT_CURRENT);
        StringList taskStateList = (StringList) taskMap.get(SELECT_STATES);
        String parentId = (String) taskMap.get(SELECT_PARENT_ID);
        String newPercent = (String) taskMap.get(SELECT_PERCENT_COMPLETE);
        double newPercentValue = Double.parseDouble(newPercent);

        //get the position of the task's current state wrt
        //to its state list
        int taskCurrentPosition = taskStateList.indexOf(state);

        //get the position of "Active" and "Complete" in the state list
        int taskActiveStatePosition = taskStateList.indexOf(
                                               STATE_PROJECT_TASK_ACTIVE);

        int taskCompleteStatePosition = taskStateList.indexOf(
                                               STATE_PROJECT_TASK_COMPLETE);

        if (taskActiveStatePosition == -1 || taskCompleteStatePosition == -1)
        {
            //object may not be of type task or project
            return;
        }

        //1. if newpercent is 0% and the task is above active state demote it
        //    to active state. Do nothing if it is below active state
        //2. if newpercent is between 1-99% irrespective of the current state
        //    set the state to Active
        //3. if newPercent is 100% and the task is already in Complete or
        //    beyond do nothing
        //4. if newPercent is 100% and the task is below Complete state setState
        //    to review
        if((newPercentValue == 0) &&
            taskCurrentPosition > taskActiveStatePosition)
        {
            //Refer #1
            setState(context, STATE_PROJECT_TASK_ACTIVE);
        }
        else if(newPercentValue > 0 && newPercentValue < 100)
        {
            //Refer #2
            if (taskActiveStatePosition > taskCurrentPosition)

            {
                //Do not set states if this is "Part Quality Plan" object
                if(!TYPE_PART_QUALITY_PLAN.equals(type))
                {
                    setState(context, STATE_PROJECT_TASK_ASSIGN);
                    setId(objectId);
                    setState(context, STATE_PROJECT_TASK_ACTIVE);
                }
            } else if (taskActiveStatePosition < taskCurrentPosition) {
                setState(context, STATE_PROJECT_TASK_ACTIVE);
            }
        }
        else if((newPercentValue == 100) &&
            taskCurrentPosition < taskCompleteStatePosition)
        {
            //Refer #3 and #4
            //had to break up the promotion to 3 stages
            if (taskActiveStatePosition > taskCurrentPosition)
            {
                setState(context, STATE_PROJECT_TASK_ACTIVE);
                setId(objectId);
            }
            //set to Review if this is not Part Quality Plan
            if(!TYPE_PART_QUALITY_PLAN.equals(type))
            {
                setState(context, STATE_PROJECT_TASK_REVIEW);
            }
        }
        /* gqh: not necessary as rollup takes care of PC.
        if (parentId != null)
        {
            //peform percent rollup
            String[] jpoArgs = new String[1];
            jpoArgs[0] = parentId;
            calculatePercentComplete(context, jpoArgs);
        }
        */
    }

    /**
    * Calculate percent complete for the given task based on status of subtasks.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args the task id to update as argument 0
    * @throws Exception if operation fails
    * @since PC 10.0.0.0
    * @grade 0
    */
    public void calculatePercentComplete(Context context, String[] args)
        throws Exception
    {
        ContextUtil.pushContext(context);
        try
        {
            setId(args[0]);
            calculatePercentComplete(context);
        }
        finally
        {
            ContextUtil.popContext(context);
        }
    }

    /**
    * Calculate percent complete for the given task based on status of subtasks.
    *
    * @param context the eMatrix <code>Context</code> object
    * @throws Exception if operation fails
    * @since PC 10.0.0.0
    * @grade 0
    */
    protected void calculatePercentComplete(Context context)
        throws Exception
    {
        /* gqh: removed entire logic as PC is now part of date rollup.
        //get the children of the parent
        StringList busSelects = new StringList(2);

        busSelects.add(SELECT_PERCENT_COMPLETE);
        busSelects.add(SELECT_TASK_ESTIMATED_DURATION);

        MapList tasks = getTasks(context, this, 1, busSelects, null);
        double totalDuration = 0;
        double completed = 0;
        String percentCompleted="";
        //Iterator itr = tasks.iterator();
        //while (itr.hasNext())
        for (int i=0; i < tasks.size(); i++)
        {
            //Map map = (Map) itr.next();
            Map map = (Map) tasks.get(i);

            double percent = Double.parseDouble((String)
                                        map.get(SELECT_PERCENT_COMPLETE));

            double duration = Double.parseDouble((String)
                                        map.get(SELECT_TASK_ESTIMATED_DURATION));

            //Bug# 306151 - (duration = 0) problem
            if(duration <= 0)
            {
                duration = 1;
            }

            totalDuration += duration;
            completed = completed + (percent * duration);
        }

        if (totalDuration != 0) {
        completed = completed / totalDuration;
        }

        // Number format has been included for displaying single decimal point to percentCompleted

        NumberFormat numberFormat=NumberFormat.getInstance();

        numberFormat.setMaximumFractionDigits(1);

        percentCompleted=numberFormat.format(completed);

        setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, percentCompleted);
        */

    }

    /**
     * Generic Trigger to be used as Promote or any other event
     * Deliverables - when a deliverable  is completed, the attached task will be completed
     * Deliverables are often of types ECR,ECO,Document,Part, etc
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - ${OBJECTID}
     * @throws Exception if operation fails
     * @since AEF 9.5.5.0
     * @grade 0
     */
    public void triggerSetParentTaskToCompleteAction(Context context, String[] args)
        throws Exception
    {
        // get values from args.
        String objectId = args[0];

         // The object id  of the deliverable items
        setId(objectId);

        StringList selects = new StringList(1);
        selects.addElement(SELECT_ID);

        MapList boTasks = getRelatedObjects(context,
                                            RELATIONSHIP_TASK_DELIVERABLE,
                                            QUERY_WILDCARD,
                                            selects,
                                            EMPTY_STRINGLIST,
                                            true,
                                            false,
                                            (short)1,
                                            EMPTY_STRING,
                                            EMPTY_STRING);

        if ( boTasks != null && boTasks.size() > 0 )
        {
             Iterator itr = boTasks.iterator();
             while (itr.hasNext())
             {
                  Map tasks = (Map) itr.next();
                  DomainObject task = newInstance(context, (String) tasks.get(SELECT_ID));
                  task.setState(context, STATE_PROJECT_TASK_COMPLETE);
             }
        }
    }

        /**
         * Check to see if the object is a Project Space if it is not then call thru to the CreateObjectOwnership
         *    method to inherit ownership from the parent object.
         *
         * @param context the eMatrix <code>Context</code> object.
         * @param args contains a packed HashMap
         * */
     public boolean PRGcreateObjectOwnershipInheritance(Context context, String[] args) throws Exception  
     {
        String fromId = args[0];
        String toId = args[1];

        try{
			final String SELECT_IS_KINDOF_PROJECT_SPACE = "type.kindof[" + DomainConstants.TYPE_PROJECT_SPACE + "]";
			final String SELECT_COLLABORATIVE_SPACE = "project";

			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_IS_KINDOF_PROJECT_SPACE);
			slBusSelect.add(SELECT_COLLABORATIVE_SPACE);
			slBusSelect.add(DomainConstants.SELECT_ORGANIZATION);

			DomainObject toObj = DomainObject.newInstance(context, toId);
			Map taskInfo = toObj.getInfo(context, slBusSelect);
			String isProjectSpace = (String)taskInfo.get(SELECT_IS_KINDOF_PROJECT_SPACE);

			//if the to-side object is a Project Space don't inherit access.
			if("false".equalsIgnoreCase(isProjectSpace)){
              DomainAccess.createObjectOwnership(context, toId, fromId, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);

				//Get from-side object details.
				DomainObject fromObj = DomainObject.newInstance(context, fromId);
				Map parentInfo = fromObj.getInfo(context, slBusSelect);
				String parentCS = (String) parentInfo.get(SELECT_COLLABORATIVE_SPACE);
				String parentOrg = (String) parentInfo.get(DomainConstants.SELECT_ORGANIZATION);

				//Get to-side object details.
				String childCS = (String) taskInfo.get(SELECT_COLLABORATIVE_SPACE);
				String childOrg = (String) taskInfo.get(DomainConstants.SELECT_ORGANIZATION);

				//if parent't POV is different from child's POV.
				if(!parentCS.equals(childCS) || !parentOrg.equals(childOrg) ){
					//if parent's POV is empty, remove child's POV too.
					if("".equals(parentCS) || "".equals(parentOrg)){
						toObj.removePrimaryOwnership(context);
					}
					else{ toObj.setPrimaryOwnership(context, parentCS, parentOrg); }
				}
			}
           return true;
        }catch(Exception e){
           throw e;
        }

     }
     
     public HashMap getTaskFilterRangeValues(Context context, String[] args) throws Exception {
     	try{
     		String attrApprovalStatus  = PropertyUtil.getSchemaProperty(context,"attribute_ApprovalStatus");
     		StringList sApprovalRanges = FrameworkUtil.getRanges(context, attrApprovalStatus);
     		int length = sApprovalRanges.size();
     		String statusKey = "emxFramework.Range.Approval_Status.";
     		String sLanguage = context.getSession().getLanguage();
     		String sApprovalObjName = "";
     		String sApprovalObjTranslatedName = "";
     		HashMap hmAttrRangeValues = new HashMap();
     		for(int i=0;i<length;i++)
     		{
     			sApprovalObjName = (String)sApprovalRanges.get(i);
     			if(sApprovalObjName.indexOf(" ") != -1){
     				sApprovalObjName = FrameworkUtil.findAndReplace(sApprovalObjName," ", "_"); 
     			}
     			sApprovalObjTranslatedName = UINavigatorUtil.getI18nString(statusKey+sApprovalObjName, "emxFrameworkStringResource", sLanguage);
     			hmAttrRangeValues.put((String)sApprovalRanges.get(i), sApprovalObjTranslatedName);

     		}
     		return hmAttrRangeValues;
     	} catch (Exception e)
     	{
     		throw e;
     	}

     }
}
