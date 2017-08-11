/* emxTaskBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.36.2.1 Thu Dec  4 07:55:16 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.36 Tue Oct 28 18:55:12 2008 przemek Experimental przemek $
 */

/*
Change History:
Date       Change By  Release   Bug/Functionality         Details
-----------------------------------------------------------------------------------------------------------------------------
29-Apr-09   wqy        V6R2010   373332                   Change Code for I18n
 */


import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.Dimension;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Unit;
import matrix.db.UnitItr;
import matrix.db.UnitList;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;
import matrix.util.StringResource;

import com.matrixone.apps.common.AssignedTasksRelationship;
import com.matrixone.apps.common.DependencyRelationship;
import com.matrixone.apps.common.ICDocument;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.ProjectManagement;
import com.matrixone.apps.common.SubtaskRelationship;
import com.matrixone.apps.common.TaskDateRollup;
import com.matrixone.apps.common.TaskHolder;
import com.matrixone.apps.common.WorkCalendar;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIForm;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UITable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.DurationKeyword;
import com.matrixone.apps.program.DurationKeywords;
import com.matrixone.apps.program.DurationKeywordsUtil;
import com.matrixone.apps.program.Financials;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ProjectTemplate;
import com.matrixone.apps.program.Risk;
import com.matrixone.apps.program.Task;
import com.matrixone.jdom.Element;

/**
 * The <code>emxTaskBase</code> class represents the Task JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxTaskBase_mxJPO extends com.matrixone.apps.program.Task
{
	private static final String SELECT_ATTRIBUTE_COMMENTS = "attribute[" + ATTRIBUTE_COMMENTS + "]";
	private static final String SELECT_ATTRIBUTE_CRITICAL_TASK = "attribute[" + ATTRIBUTE_CRITICAL_TASK + "]";
	private static final String SELECT_IS_DELETED_SUBTASK = "to[" + DomainConstants.RELATIONSHIP_DELETED_SUBTASK + "]";
	private static final String SELECT_IS_PARENT_TASK_DELETED = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].from.to[" + DomainConstants.RELATIONSHIP_DELETED_SUBTASK + "]";
	private static final String SELECT_DELETED_SUBTASK_ATTRIBUTE_COMMENTS = SELECT_IS_DELETED_SUBTASK + "." + SELECT_ATTRIBUTE_COMMENTS;
	private static final String SELECT_TASK_DEPENDENCY_REL_ID = "from[" + DomainObject.RELATIONSHIP_DEPENDENCY + "].id";
	private static final String SELECT_SUBTASK_ATTRIBUTE_SEQUENCE_ORDER = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_SEQUENCE_ORDER+"]";
	private static final String SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER = "from[" + RELATIONSHIP_DEPENDENCY + "].to." + SELECT_SUBTASK_ATTRIBUTE_SEQUENCE_ORDER;
	private static final String SELECT_SUBTASK_ATTRIBUTE_TASK_WBS = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_TASK_WBS+"]";
	private static final String SELECT_PREDECESSOR_TASK_ATTRIBUTE_TASK_WBS = "from[" + RELATIONSHIP_DEPENDENCY + "].to." + SELECT_SUBTASK_ATTRIBUTE_TASK_WBS;
	private static final String SELECT_PREDECESSOR_TASK_PROJECT_NAME = "from[" + RELATIONSHIP_DEPENDENCY + "].to.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";
	private static final String SELECT_PREDECESSOR_TASK_PROJECT_TYPE = "from[" + RELATIONSHIP_DEPENDENCY + "].to.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
	private static final String SELECT_PREDECESSOR_TASK_PROJECT_ID = "from[" + RELATIONSHIP_DEPENDENCY + "].to.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
	private static final String ATTRIBUTE_LAG_TIME = PropertyUtil.getSchemaProperty("attribute_LagTime");
	private static final String SELECT_PREDECESSOR_LAG_TIME_INPUT = "from[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ATTRIBUTE_LAG_TIME + "].inputvalue";
	private static final String SELECT_PREDECESSOR_LAG_TIME_UNITS = "from[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ATTRIBUTE_LAG_TIME + "].inputunit";
	private static final String SELECT_TASK_PROJECT_CURRENT = "to["+RELATIONSHIP_SUBTASK+"].from.current";
	private static final String SELECT_TASK_PROJECT_TYPE = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
	private static final String SELECT_TASK_PROJECT_ID = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
	private static final String SELECT_PARENT_TASK_IDS = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].from.id";
	private static final String TOTAL_EFFORT = "Total_Effort";
	private static final String SELECT_TASK_ASSIGNEE_ID = "to[" + DomainObject.RELATIONSHIP_ASSIGNED_TASKS + "].from.id";
	private static final String SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+ProgramCentralConstants.TYPE_EXPERIMENT+"]";
	private static final String SELECT_PARENTOBJECT_KINDOF_PROJECT_BASELINE = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+ProgramCentralConstants.TYPE_PROJECT_BASELINE+"]";
	private static final String SELECT_PARENTOBJECT_KINDOF_PROJECT_TEMPLATE = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+DomainObject.TYPE_PROJECT_TEMPLATE+"]";
	private static final String SELECT_PARENTOBJECT_KINDOF_PROJECT_CONCEPT = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+DomainObject.TYPE_PROJECT_CONCEPT+"]";
	private static final String FROM_PROJECT_TEMPLATE_WBS ="FromProjectTemplateWBS";
	private static final String FROM_PROJECT_WBS ="FromProjectWBS";
	private static final String SELECT_ATTRIBUTE_SCHEDULE_FROM = "attribute[Schedule From]";
	private static final String SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD = "attribute[" + DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD + "]";
	private static final String RELATIONSHIP_CONTRIBUTES_TO = PropertyUtil.getSchemaProperty("relationship_ContributesTo");
	private static final String SELECT_CONTRIBUTES_TO_RELATIONSHIP_ID = "from[" + RELATIONSHIP_CONTRIBUTES_TO + "].id";
	private static final String RELATIONSHIP_SHADOW_GATE = PropertyUtil.getSchemaProperty("relationship_ShadowGate");
	private static final String SELECT_SHADOW_GATE_ID = "from["+RELATIONSHIP_SHADOW_GATE+"].id";
	private static final String SELECT_IS_TASK_MANAGEMENT=ProgramCentralConstants.SELECT_TYPE+".kindof["+ProgramCentralConstants.TYPE_TASK_MANAGEMENT+"]";

	private static final String SELECT_SUCCESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER = "to[" + RELATIONSHIP_DEPENDENCY + "].from." + SELECT_SUBTASK_ATTRIBUTE_SEQUENCE_ORDER;
	private static final String SELECT_SUCCESSOR_TASK_PROJECT_NAME = "to[" + RELATIONSHIP_DEPENDENCY + "].from.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";
	private static final String SELECT_SUCCESSOR_TASK_PROJECT_TYPE = "to[" + RELATIONSHIP_DEPENDENCY + "].from.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
	private static final String SELECT_SUCCESSOR_TASK_PROJECT_ID = "to[" + RELATIONSHIP_DEPENDENCY + "].from.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
	private static final String SELECT_SUCCESSOR_LAG_TIME_INPUT = "to[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ATTRIBUTE_LAG_TIME + "].inputvalue";
	private static final String SELECT_SUCCESSOR_LAG_TIME_UNITS = "to[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ATTRIBUTE_LAG_TIME + "].inputunit";
	private static final String SELECT_SUCCESSOR_IDS = "to[" + RELATIONSHIP_DEPENDENCY + "].from.id";
	public static final String SELECT_SUCCESSOR_TYPES = "to[" + RELATIONSHIP_DEPENDENCY + "].attribute[" +DependencyRelationship.ATTRIBUTE_DEPENDENCY_TYPE + "]";


	// Create an instant of emxUtil JPO
	protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null;

	/** Id of the Access List Object for this Project. */
	protected DomainObject _accessListObject = null;

	/** The project access list id relative to project. */
	static protected final String SELECT_PROJECT_ACCESS_LIST_ID =
			"to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";

	/** The project access key id relative to task predecessor. */
	static protected final String SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR =
			"from[" + RELATIONSHIP_DEPENDENCY + "].to." + SELECT_PROJECT_ACCESS_KEY_ID;

	static protected final String SELECT_PROJECT_ACCESS_KEY_ID_FOR_SUCCESSOR =
			"to[" + RELATIONSHIP_DEPENDENCY + "].from." + SELECT_PROJECT_ACCESS_KEY_ID;

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
	/** The parent type of the task. */
	public static final String SELECT_SUBTASK_TYPE =
			"to[" + RELATIONSHIP_SUBTASK + "].from.type";

	/** state "Complete" for the "Project Task" policy. */
	public static final String STATE_TASK_COMPLETE =
			PropertyUtil.getSchemaProperty("policy",
					POLICY_PROJECT_TASK,
					"state_Complete");
	/** attribute "Percent Allocation". */
	public static final String ATTRIBUTE_PERCENT_ALLOCATION =
			PropertyUtil.getSchemaProperty("attribute_PercentAllocation");
	public static final String SELECT_FROM_SUBTASK = "from["+RELATIONSHIP_SUBTASK+"]";



	/** used in triggerPromoteAction and triggerDemoteAction functions. */
	boolean _doNotRecurse = false;
	boolean isGateOrMilestone = false;
	//**Start**
	String blockValue ="";
	//**End**

	//used to set the Actual Start and Finish date's timings
	//Addition:9-Jun-2010:DI1:R210 PRG:056258
	public static final String START_DATE_SET_TIME = "08:00:00 AM";
	public static final String FINISH_DATE_SET_TIME = "05:00:00 PM";
	public static final int HOURS_PER_DAY = 8;
	//End Addition:9-Jun-2010:DI1:R210 PRG:056258

	/** Keeps track of confirmed accesses for the context. */
	protected ArrayList _passedAccessTypes = new ArrayList();


	protected Map _taskMap = (Map)new HashMap();

	/**
	 * Constructs a new emxTask JPO object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the id
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.1
	 */
	public emxTaskBase_mxJPO (Context context, String[] args)
			throws Exception
			{
		// Call the super constructor
		super();
		if (args != null && args.length > 0)
		{
			setId(args[0]);
		}
			}

	/**
	 * This function verifies the user's permission for the given task.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *      PROJECT_MEMBER to see if the context user is a project member, <BR>
	 *      PROJECT_LEAD to see if the context user is a project lead, <BR>
	 *      PROJECT_OWNER to see if the context user is a project owner, <BR>
	 *      TASK_ASSIGNEE to see if the context user is an assignee of this
	 *                    task, <BR>
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.0
	 */
	public boolean hasAccess(Context context, String args[])
			throws Exception
			{

		//program[emxTask PROJECT_MEMBER -method hasAccess
		//            -construct ${OBJECTID}] == true
		boolean access = false;
		for (int i = 0; i < args.length; i++)
		{
			String accessType = args[i];

			// Check if access has already been checked.
			if (_passedAccessTypes.indexOf(accessType) != -1)
			{
				access = true;
				break;
			}

			if ("TASK_ASSIGNEE".equals(accessType))
			{
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
				DomainObject accessListObject = getAccessListObject(context);

				if (accessListObject != null)
				{
					int iAccess;
					if ("PROJECT_MEMBER".equals(accessType))
					{
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
				_passedAccessTypes.add(accessType);
				break;
			}
		}
		return access;
			}

	/**
	 * When the task is promoted this function is called.
	 * Depending on which state the promote is triggered from
	 * it performs the necessary actions based on the arg-STATENAME value
	 *
	 * Note: object id must be passed as first argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
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

		if (fromState.equals(STATE_PROJECT_TASK_CREATE) && (_doNotRecurse && !isGateOrMilestone))
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
		//Added:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
		busSelects.add(SELECT_POLICY);
		//End:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight

		busSelects.add(ProgramCentralConstants.SELECT_KINDOF_GATE);
		busSelects.add(ProgramCentralConstants.SELECT_KINDOF_MILESTONE);

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

			busSelects.add(SELECT_HAS_ASSIGNED_TASKS);
                        busSelects.add("relationship["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"].to");

			// get all the subtasks
			MapList utsList = getTasks(context, this, 0, busSelects, null);
			if (utsList.size() > 0)
			{
				_doNotRecurse = true;
				Iterator itr = utsList.iterator();
				while (itr.hasNext())
				{
					boolean promoteTask = false;
					Map map = (Map) itr.next();
					String state = (String) map.get(SELECT_CURRENT);
                                        Object routes =  map.get("relationship["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"].to");
					StringList taskStateList =
							(StringList) map.get(SELECT_STATES);

					//get the position of the task's current state wrt
					//to its state list
					int taskCurrentPosition = taskStateList.indexOf(state);

					//get the position to which the task need to be promoted
					//if the toState does not exist then taskPromoteToPosition
					//will be -1
					//Added:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
					String type = (String) map.get(SELECT_TYPE);
					String policy = (String) map.get(SELECT_POLICY);

					boolean isGateType      =   "TRUE".equalsIgnoreCase((String)map.get(ProgramCentralConstants.SELECT_KINDOF_GATE));
					boolean isMilestoneType =   "TRUE".equalsIgnoreCase((String)map.get(ProgramCentralConstants.SELECT_KINDOF_MILESTONE));

					if(type != null && ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy) && (isGateType || isMilestoneType)){

						if( !_doNotRecurse && (STATE_PROJECT_TASK_ASSIGN.equals(toState) || STATE_PROJECT_TASK_ACTIVE.equals(toState))){
							String [] arg1 = new String[4];
							arg1[0] = objectId;
							arg1[1] = STATE_PROJECT_TASK_ASSIGN;
							arg1[2] = STATE_PROJECT_TASK_ACTIVE;
							arg1[3] = "false";
							triggerPromoteAction(context,arg1);
							triggerSetPercentageCompletion(context,arg1);

							arg1[0] = objectId;
							arg1[1] = STATE_PROJECT_TASK_ACTIVE;
							arg1[2] = STATE_PROJECT_TASK_REVIEW;
							arg1[3] = "false";
							triggerPromoteAction(context,arg1);
						}
					}
					//End:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
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
			if (routes != null) {
				DomainObject taskObject = DomainObject.newInstance(
									context, taskId);
				StringList relSelects = new StringList("attribute["+ DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE+ "]");
				String relWhere = "attribute["+ DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE+ "] == \"state_Create\"";
				MapList routeList = taskObject.getRelatedObjects(
						context,
						DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
						DomainConstants.TYPE_ROUTE, null,
						relSelects, false, true, (short) 0, "",
						relWhere, (short) 0);
				if (routeList != null && routeList.size() > 0) {
					promoteTask = false;
				}
			}
			if(promoteTask)
						{
							taskToPromoteList.add(taskId);
						}
					}
				}
				//_doNotRecurse = false;
			}
			else{
				//Added:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
				Map mInfo = getInfo(context, busSelects);
				String type = (String) mInfo.get(SELECT_TYPE);
				String policy = (String) mInfo.get(SELECT_POLICY);
				boolean isGateType      =   "TRUE".equalsIgnoreCase((String)mInfo.get(ProgramCentralConstants.SELECT_KINDOF_GATE));
				boolean isMilestoneType =   "TRUE".equalsIgnoreCase((String)mInfo.get(ProgramCentralConstants.SELECT_KINDOF_MILESTONE));
				if(type != null && ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy) && (isGateType || isMilestoneType)) {

					//toState = STATE_PROJECT_TASK_REVIEW;
					String [] arg1 = new String[4];
					arg1[0] = objectId;
					arg1[1] = STATE_PROJECT_TASK_ASSIGN;
					arg1[2] = STATE_PROJECT_TASK_ACTIVE;
					arg1[3] = "false";
					triggerPromoteAction(context,arg1);

					//String [] arg2 = new String[4];
					arg1[0] = objectId;
					arg1[1] = STATE_PROJECT_TASK_ACTIVE;
					arg1[2] = STATE_PROJECT_TASK_REVIEW;
					arg1[3] = "false";
					triggerPromoteAction(context,arg1);

					triggerSetPercentageCompletion(context,arg1);
					isGateOrMilestone = true;
				}
				//End:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
			}
		}
	        else if (fromState.equals(STATE_PROJECT_TASK_ASSIGN))
		{
			//******************start Business Goal promote to Active state*********
			//when the project is promoted from the assign to active state
			//promote the business goal if it is the first business goal
			//use super user to overcome access issue
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
				if (null != businessGoalList && businessGoalList.size()>0)
				{
					Iterator businessGoalItr = businessGoalList.iterator();
					while(businessGoalItr.hasNext())
					{
						Map businessGoalMap = (Map) businessGoalItr.next();
						String businessGoalId = (String) businessGoalMap.get(businessGoal.SELECT_ID);
						String businessGoalState = (String) businessGoalMap.get(businessGoal.SELECT_CURRENT);
						businessGoal.setId(businessGoalId);
						if(fromState.equals(STATE_PROJECT_TASK_ASSIGN) && businessGoalState.equals(STATE_BUSINESS_GOAL_CREATED))
						{
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
				//Added:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight
				String type = (String) map.get(SELECT_TYPE);
				String policy = (String) map.get(SELECT_POLICY);

				boolean isGateType      =   "TRUE".equalsIgnoreCase((String)map.get(ProgramCentralConstants.SELECT_KINDOF_GATE));
				boolean isMilestoneType =   "TRUE".equalsIgnoreCase((String)map.get(ProgramCentralConstants.SELECT_KINDOF_MILESTONE));

				if(type != null && ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy) && (isGateType || isMilestoneType)){

					if(STATE_PROJECT_TASK_ACTIVE.equals(toState)){
						toState = STATE_PROJECT_TASK_REVIEW;
						_doNotRecurse = true;
					}
				}
				//End:nr2:17-05-2010:PRG:R210:For Phase Gate Highlight

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
				//Added:nr2:PRG:R210:For Project Hold Cancel Highlight
				//Coming in this condition since Project May be in Hold
				//and Task is promoted.
				else if(taskPromoteToPosition == -1){
					String id = (String) map.get(SELECT_ID); //Should be Project Space id
					DomainObject dObj = DomainObject.newInstance(context,id);

					if(dObj.isKindOf(context, TYPE_PROJECT_SPACE)){
						ProjectSpace ps = new ProjectSpace();
						StringList projectSpaceStates = ps.getStates(context, POLICY_PROJECT_SPACE);

						int tasktoBePromotedToPosition = projectSpaceStates.indexOf(toState);
						//Check the Value stored in Previous Project State Attribute.
						//To store the new state only if greater than the one stored.
						Task task = new Task(id);

						HashMap programMap = new HashMap();
						programMap.put(SELECT_ID, id);
						String[] arrJPOArguments = JPO.packArgs(programMap);
						String previousState = (String)JPO.invoke(context, "emxProgramCentralUtilBase", null, "getPreviousState",arrJPOArguments,String.class);

						int previousStatePos = projectSpaceStates.indexOf(previousState);

						if(previousStatePos == -1){
							previousStatePos = 6;
						}

						if (tasktoBePromotedToPosition != -1 && tasktoBePromotedToPosition > previousStatePos)
						{
							//String taskId = (String) map.get(SELECT_ID);

							//use super user to overcome access issue
							ContextUtil.pushContext(context);
							try
							{
								programMap.put(SELECT_CURRENT, toState);
								String[] arrJPOArgs = JPO.packArgs(programMap);
								JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
							}
							finally
							{
								ContextUtil.popContext(context);
							}
						}
					}
				} //End of Else if
				//End:nr2:PRG:R210:For Project Space Hold Cancel Highlight
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
				if (null != businessGoalList && businessGoalList.size()>0)
				{
					Iterator businessGoalItr = businessGoalList.iterator();
					while(businessGoalItr.hasNext())
					{
						Map businessGoalMap = (Map) businessGoalItr.next();
						String businessGoalId = (String) businessGoalMap.get(businessGoal.SELECT_ID);
						String businessGoalState = (String) businessGoalMap.get(businessGoal.SELECT_CURRENT);
						businessGoal.setId(businessGoalId);
						if(fromState.equals(STATE_PROJECT_TASK_REVIEW) && businessGoalState.equals(STATE_BUSINESS_GOAL_ACTIVE))
						{
							MapList projectList = businessGoal.getProjects(context, busSelects, null);
							boolean changeState = true;
							if (null != projectList && projectList.size()>0)
							{
								Iterator projectItr = projectList.iterator();
								while(projectItr.hasNext())
								{
									Map projectMap = (Map) projectItr.next();
									String projectId = (String) projectMap.get(project.SELECT_ID);
									String projectState = (String) projectMap.get(project.SELECT_CURRENT);
									if(!projectState.equals(STATE_PROJECT_TASK_COMPLETE) && !projectState.equals(STATE_PROJECT_TASK_ARCHIVE)  && !projectId.equals(objectId))
									{
										changeState = false;
									}
								}
								if(changeState)
								{
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
						if (parentType != null && parentType.equals(TYPE_PART_QUALITY_PLAN))
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
						//Added:nr2:PRG:R210:For Project Hold Cancel Highlight
						//Coming in this condition since Project May be in Hold
						//and Task is promoted.
						else if(taskPromoteToPosition == -1){
							String id = (String) map.get(SELECT_ID); //Should be Project Space id
							DomainObject dObj = DomainObject.newInstance(context,id);

							if(dObj.isKindOf(context, TYPE_PROJECT_SPACE)){
								ProjectSpace ps = new ProjectSpace();
								StringList projectSpaceStates = ps.getStates(context, POLICY_PROJECT_SPACE);

								int tasktoBePromotedToPosition = projectSpaceStates.indexOf(toState); //toState is Completed hence 4
								//Check the Value stored in Previous Project State Attribute.
								//To store the new state only if greater than the one stored.
								Task task = new Task(id);

								HashMap programMap = new HashMap();
								programMap.put(SELECT_ID, id);
								String[] arrJPOArguments = JPO.packArgs(programMap);
								String previousState = (String)JPO.invoke(context, "emxProgramCentralUtilBase", null, "getPreviousState",arrJPOArguments,String.class);

								int previousStatePos = projectSpaceStates.indexOf(previousState); //This will be 2 (Active) most Probably

								if(previousStatePos == -1){
									previousStatePos = 6;
								}

								if (tasktoBePromotedToPosition != -1 && tasktoBePromotedToPosition > previousStatePos)
								{
									//String taskId = (String) map.get(SELECT_ID);

									//use super user to overcome access issue
									ContextUtil.pushContext(context);
									try
									{
										programMap.put(SELECT_CURRENT, STATE_PROJECT_TASK_REVIEW);
										String[] arrJPOArgs = JPO.packArgs(programMap);
										JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
									}
									finally
									{
										ContextUtil.popContext(context);
									}
								}
							}
						} //End of Else if
						//End:nr2:PRG:R210:For Project Space Hold Cancel Highlight
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
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
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
		busSelects.add(SELECT_POLICY);

		if (fromState.equals(STATE_PROJECT_TASK_ASSIGN))
		{
			//check if the parent is in Assign state
			//then check the status of the siblings
			//if none of the siblings are in assign state
			//the demote the parent to "Create" state
			MapList parentList = getParentInfo(context, 1, busSelects);
			boolean demoteParent = false;
			//Added:10-Aug-2010"PRG:R210:For Hold and Cancel Highlight
			String parentPolicy = "";
			String parentId = EMPTY_STRING;
			//End:10-Aug-2010"PRG:R210:For Hold and Cancel Highlight

			if (parentList.size() > 0)
			{
				Map map = (Map) parentList.get(0);
				String state = (String) map.get(SELECT_CURRENT);
				parentId = (String) map.get(SELECT_ID);
				setId(parentId);

				//Added:10-Aug-2010:PRG:R210:For Hold and Cancel Highlight
				parentPolicy = getInfo(context,SELECT_POLICY);

				//if (state.equals(STATE_PROJECT_TASK_ASSIGN))
				if (state.equals(STATE_PROJECT_TASK_ASSIGN) ||
						(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(state)
								&& ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(parentPolicy)))
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
							/*                            StringList taskStateList =
                                    (StringList) map.get(SELECT_STATES);
							 */
							StringList taskStateList =
									(StringList) taskmap.get(SELECT_STATES);

							//Added:nr2:PRG:R210:For Project Gate Highlight
							String taskId = (String) taskmap.get(SELECT_ID);
							String taskPolicy = "";
							taskPolicy = (String)taskmap.get(SELECT_POLICY);
							//End:nr2:PRG:R210:For Project Gate Highlight

							//get the position of the task's current state wrt
							//to its state list
							int taskCurrentPosition =
									taskStateList.indexOf(state);

							//get the position of "Assign" in the state list
							//if the "Assign" state does not exist then
							//taskAssignPostion will be -1
							int taskAssignStatePosition = taskStateList.indexOf(
									STATE_PROJECT_TASK_ASSIGN);

							//Added:nr2:PRG:R210:For Project Gate Highlight
							if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy) && taskAssignStatePosition == -1)
							{
								taskAssignStatePosition = 1;
							}
							//End:nr2:PRG:R210:For Project Gate Highlight

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
					//Added:10-Aug-2010"PRG:R210:For Hold and Cancel Highlight
					if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(parentPolicy)){
						HashMap programMap = new HashMap();
						programMap.put(SELECT_ID,parentId);
						programMap.put(SELECT_CURRENT, STATE_PROJECT_TASK_CREATE);
						String[] arrJPOArgs = JPO.packArgs(programMap);
						JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
					}
					//End:10-Aug-2010"PRG:R210:For Hold and Cancel Highlight
					else{
						demote(context);
					}
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
			//Added:nr2:PRG:R210:For project Gate Highlight
			String parentIdHolder = "";
			String parentId = EMPTY_STRING;
			//End:nr2:PRG:R210:For project Gate Highlight

			MapList parentList = getParentInfo(context, 1, busSelects);
				boolean demoteParent = false;
			if(parentList.size() > 0)
			{
				demoteParent = true;
				Map map = (Map) parentList.get(0);
				String state = (String) map.get(SELECT_CURRENT);
				String type = (String) map.get(SELECT_TYPE);
				parentId = (String) map.get(SELECT_ID);
				//Added:nr2:PRG:R210:For project Gate Highlight
				parentIdHolder = parentId;
				//End:nr2:PRG:R210:For project Gate Highlight
				setId(parentId);

				//Added:nr2:PRG:R210:10-Aug-2010:For project Gate Highlight
				String parentPolicy = getInfo(context,SELECT_POLICY);
				if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(parentPolicy)){

					HashMap programMap = new HashMap();
					programMap.put(SELECT_ID, parentId);
					String[] arrJPOArguments = JPO.packArgs(programMap);
					String previousState = (String)JPO.invoke(context, "emxProgramCentralUtilBase", null, "getPreviousState",arrJPOArguments,String.class);

					if(null!=previousState && STATE_PROJECT_TASK_ACTIVE.equals(previousState)){
						state = STATE_PROJECT_TASK_ACTIVE;
					}
				}
				//End:nr2:PRG:R210:10-Aug-2010:For project Gate Highlight

				if (state.equals(STATE_PROJECT_TASK_ACTIVE) && (type == null || !TYPE_PART_QUALITY_PLAN.equals(type)))
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
							/*                            StringList taskStateList =
                                    (StringList) map.get(SELECT_STATES);
							 */
							String taskPolicy = (String)taskmap.get(SELECT_POLICY);
							StringList taskStateList =
									(StringList) taskmap.get(SELECT_STATES);

							//get the position of the task's current state wrt to its state list
							int taskCurrentPosition =
									taskStateList.indexOf(state);

							//get the position of "Active" in the state list
							//if the "Active" state does not exist then
							//taskActivePostion will be -1
							int taskActiveStatePosition = taskStateList.indexOf(
									STATE_PROJECT_TASK_ACTIVE);

							if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy) && taskActiveStatePosition == -1)
							{
								taskActiveStatePosition = 1;
							}
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
					//Added:nr2:PRG:R210:03-Aug-2010:For Project Gate highlight
					//Comes here if parent is a Gate/Milestone
					if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(parentPolicy) &&
							state.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW)){
						demoteParent = true;
					}
					//End:nr2:PRG:R210:03-Aug-2010:For Project Gate highlight
					else{
						demoteParent = false;
					}
				}
			}
			if (demoteParent)
			{
				//use super user to overcome access issue
				ContextUtil.pushContext(context);
				try
				{
					//Added:nr2:PRG:R210:For Project Hold Cancel Highlight
					//if Project is in Hold and subtask's states are changed
					//The intended demoted state is remembered.
					String projectPolicy = getInfo(context,SELECT_POLICY);

					if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(projectPolicy)){
						HashMap programMap = new HashMap();
						programMap.put(SELECT_ID,parentId);
						programMap.put(SELECT_CURRENT, STATE_PROJECT_SPACE_ASSIGN);
						String[] arrJPOArgs = JPO.packArgs(programMap);
						JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
					}
					else{
						demote(context);
					}
					//End:nr2:PRG:R210:For Project Hold Cancel Highlight
				}
				finally
				{
					ContextUtil.popContext(context);
				}
			}
			//Added:nr2:PRG:R210:For Project Gate Highlight
			//This addition simulates the behaviour of task demotion for gates/milestones
			//If the selected task being demoted from review state is gate or milestone they
			//are demoted in the order review->Active->Assign
			setId(objectId);
			String policyName = getInfo(context,SELECT_POLICY);
			if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policyName)){
				String [] arg = new String[3];
				arg[0] = objectId;
				arg[1] = STATE_PROJECT_TASK_ASSIGN;
				arg[2] = STATE_PROJECT_TASK_CREATE;
				//arg1[3] = "false";
				triggerDemoteAction(context,arg);
			}
			setId(parentIdHolder);
			//End:nr2:PRG:R210:For Project Gate Highlight
		}
		else if(fromState.equals(STATE_PROJECT_TASK_REVIEW))
		{
			//Action:Check whether the parent is in review state if so demote
			//it to active state
			//i.e when the first child is demoted from Review to Active
			// demote the parent too

			//get the parent task
			//Added:nr2:PRG:R210:For project Gate Highlight
			String parentIdHolder = "";
			//End:nr2:PRG:R210:For project Gate Highlight
			MapList parentList = getParentInfo(context, 1, busSelects);
			if(parentList.size() > 0)
			{
				Map map = (Map) parentList.get(0);
				String state = (String) map.get(SELECT_CURRENT);
				String parentId = (String) map.get(SELECT_ID);

				//Added:nr2:PRG:R210:For project Gate Highlight
				parentIdHolder = parentId;
				//End:nr2:PRG:R210:For project Gate Highlight
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

					//Added:nr2:31-05-2010:PRG:R210:For Stage Gate Highlight
					//This condition is added so that non-availability of Active state
					//in Project Review policy does not block the demotion of Parent
					//tasks in Active state.
					String taskPolicy =  (String)map.get(SELECT_POLICY);
					if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy) && taskActiveStatePosition == -1){
						taskActiveStatePosition = 2;
					}
					//End:nr2:31-05-2010:PRG:R210:For Stage Gate Highlight

					//check if any of the tasks are in active state.
					if (taskActiveStatePosition != -1 &&
							taskCurrentPosition > taskActiveStatePosition)
					{
						//use super user to overcome access issue
						ContextUtil.pushContext(context);
						try
						{
							//demote the parent to Active state
							//Added:nr2:31-05-2010:PRG:R210:For Stage Gate Highlight
							//Demote the parent to Active state only when the parent is not
							//Geverned by Project Review Policy.
							if(! ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy)){
								if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy))
								{
									HashMap programMap = new HashMap();
									programMap.put(SELECT_ID, parentId);
									programMap.put(SELECT_CURRENT, STATE_PROJECT_SPACE_ACTIVE);
									String[] arrJPOArgs = JPO.packArgs(programMap);
									JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
								}
								else
									setState(context, STATE_PROJECT_TASK_ACTIVE);
							}
							//End:nr2:31-05-2010:PRG:R210:For Stage Gate Highlight
						}
						finally
						{
							ContextUtil.popContext(context);
						}
					}
				}
			}

			//Added:nr2:PRG:R210:For Project Gate Highlight
			//This addition simulates the behaviour of task demotion for gates/milestones
			//If the selected task being demoted from review state is gate or milestone they
			//are demoted in the order review->Active->Assign

			//process for task instead of parent
			setId(objectId);
			String policyName = getInfo(context,SELECT_POLICY);
			//Coming at this position means

			if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policyName)){
				String [] arg = new String[3];
				arg[0] = objectId;
				arg[1] = STATE_PROJECT_TASK_REVIEW;
				arg[2] = STATE_PROJECT_TASK_ACTIVE;
				//arg1[3] = "false";
				triggerSetPercentageCompletion(context,arg);

				arg[0] = objectId;
				arg[1] = STATE_PROJECT_TASK_ACTIVE;
				arg[2] = STATE_PROJECT_TASK_ASSIGN;
				triggerDemoteAction(context,arg);
				//String [] arg2 = new String[4];
			}
			setId(parentIdHolder);
			//End:nr2:PRG:R210:For Project gate Highlight
		}
		if (toState.equals(STATE_PROJECT_TASK_ACTIVE))
		{
			setId(objectId);
			String value = getInfo(context, SELECT_PERCENT_COMPLETE);
			double percent = Task.parseToDouble(value);
			if (90 < percent)
			{
				setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "90");
			}
		}
		//[MODIFIED::IR-114043V6R2013]
		//TaskDateRollup rollup = new TaskDateRollup(objectId);
		//rollup.validateTask(context);
		//[END::IR-114043V6R2013]
		DebugUtil.debug("Exiting Task triggerDemoteAction");
			}

	/**
     * This function notifies the task assignees when moved from create state to assign/active state
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws Exception if operation fails
     */
     public void notifyTaskAssignees(Context context, String[] args) throws Exception{
        try{
		DebugUtil.debug("Entering notifyTaskAssignee");
            // get task id from args.
            String iTaskObjectId = args[0];
            setId(iTaskObjectId);

            StringList busSelects = new StringList(SELECT_NAME);
            // get the assignees for this task
            MapList assigneeList = getAssignees(context, busSelects, null, null);

            if (assigneeList.size() > 0){

                StringList iAssigneeList = new StringList(assigneeList.size());
			Iterator itr = assigneeList.iterator();
                while (itr.hasNext()){
				Map map = (Map) itr.next();
				String personName = (String) map.get(SELECT_NAME);
                    iAssigneeList.addElement(personName);
                }

                HashMap argMap =  new HashMap();
                argMap.put(ProgramCentralConstants.ASSIGNEE_LIST, iAssigneeList);
                argMap.put(ProgramCentralConstants.TASK_OBJECTID, iTaskObjectId);
                argMap.put(ProgramCentralConstants.EVENT, "create");

                String[] argsList = JPO.packArgs(argMap);
                JPO.invoke(context, "emxProgramCentralUtilBase", null, "sendTaskMailHelper", argsList);
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
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
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
	 */
	public void triggerModifyAttributes(Context context, String[] args)
			throws Exception
			{
		DebugUtil.debug("Entering triggerModifyAttributes");

		// get values from args.
		String objectId = args[0];
		String fromState = args[1];
		String toState = args[2];

		//String cmd = "get env EVENT";
		String cmd = "get $1 $2";
		String sEvent = MqlUtil.mqlCommand(context, cmd , true,"env","EVENT");

		/** "MATRIX_DATE_FORMAT". */
		SimpleDateFormat MATRIX_DATE_FORMAT =
				new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),
						Locale.US);

		setId(objectId);
		StringList slTaskSelects = new StringList();
		slTaskSelects.addElement(SELECT_POLICY);
		slTaskSelects.addElement(SELECT_TYPE);
		slTaskSelects.addElement(SELECT_NAME);
		slTaskSelects.add(SELECT_PROJECT_ACCESS_KEY_ID);
		//Added:nr2:PRG:R210:29-05-2010:For Project Gate Highlight
		Map mTaskInfo = getInfo(context,slTaskSelects);
		//End:nr2:PRG:R210:29-05-2010:For Project Gate Highlight
		String taskPolicy = (String)mTaskInfo.get(SELECT_POLICY);
		String sTaskType  = (String)mTaskInfo.get(SELECT_TYPE);
		String sTaskName  = (String)mTaskInfo.get(SELECT_NAME);
		String projectAccessListId = (String)mTaskInfo.get(SELECT_PROJECT_ACCESS_KEY_ID);
		TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
		double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
		double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();

		int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
		java.text.DateFormat format = DateFormat.getDateTimeInstance(
				iDateFormat, iDateFormat, Locale.US);
		String inputTime = START_DATE_SET_TIME;
		if(sEvent.equals("Promote"))
		{
			if (toState.equals(STATE_PROJECT_TASK_ACTIVE) || (toState.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW) &&
					taskPolicy.equals(ProgramCentralConstants.POLICY_PROJECT_REVIEW))) //Added:nr2:Condition to Check for Gate or Milestones
			{
				//Modified:07-Dec-10:R211:PRG:IR-080718V6R2012
				//Modified because Task is not accepting past/pre Actual dates.
				String actualStartDate="";
				String oldActualStartDate = getAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE);
				Date sDate = new Date();
				if(null==oldActualStartDate || "".equals(oldActualStartDate) || "null".equalsIgnoreCase(oldActualStartDate))
				{
					actualStartDate = format.format(sDate);
				}
				else{
					actualStartDate = oldActualStartDate;
					sDate= MATRIX_DATE_FORMAT.parse(actualStartDate);
					actualStartDate = format.format(sDate);
				}
				inputTime = getTime(context, format.parse(actualStartDate), true);
				actualStartDate = eMatrixDateFormat
						.getFormattedInputDateTime(context,
								actualStartDate,inputTime, clientTZOffset, Locale.US);
				setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE,
						actualStartDate);
			}
			else if (toState.equals(STATE_PROJECT_TASK_COMPLETE))
			{
				//finish date
				Map attributes = new HashMap(3);
				String actualFinishDate="";
				String oldActualFinishDate = getAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);
				Date fDate = new Date();
				if(null==oldActualFinishDate || "".equals(oldActualFinishDate) || "null".equalsIgnoreCase(oldActualFinishDate))
				{
					actualFinishDate = format.format(fDate);
				}
				else{
					actualFinishDate = oldActualFinishDate;
					fDate= MATRIX_DATE_FORMAT.parse(actualFinishDate);
					actualFinishDate = format.format(fDate);
				}

				inputTime = getTime(context, format.parse(actualFinishDate), false);
				actualFinishDate = eMatrixDateFormat
						.getFormattedInputDateTime(context,
								actualFinishDate,inputTime, clientTZOffset, Locale.US);
				attributes.put(ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,
						actualFinishDate);

				//End:Modified:07-Dec-10:R211:PRG:IR-080718V6R2012

				//setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,
				//                  actualFinishDate);
				//compute duration using the actual start and finish date
                String actualStartDate = getInfo(context, SELECT_TASK_ACTUAL_START_DATE);
                
                if(ProgramCentralUtil.isNullString(actualStartDate)){
                	 actualStartDate = format.format(fDate);
                	 actualStartDate = eMatrixDateFormat
                             .getFormattedInputDateTime(context,
                                     actualStartDate,START_DATE_SET_TIME, clientTZOffset, Locale.US);
                	 setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE,
                             actualStartDate);
                }
                
                
				String estDuration = getInfo(context, SELECT_TASK_ESTIMATED_DURATION);
				Date sDate = MATRIX_DATE_FORMAT.parse(actualStartDate);
				fDate= MATRIX_DATE_FORMAT.parse(actualFinishDate);
				if (sDate.compareTo(fDate) > 0)
				{
					//make sure actual finish is not before actual start.
					attributes.put(ATTRIBUTE_TASK_ACTUAL_FINISH_DATE, actualStartDate);
					attributes.put(ATTRIBUTE_TASK_ACTUAL_DURATION, "0");
				}
				else if (Task.parseToDouble(estDuration) == 0)
				{
					inputTime = getTime(context,fDate, true);
					//if dur is zero, then set finish as start time.
					actualFinishDate = eMatrixDateFormat.getFormattedInputDateTime(context,
							format.format(fDate), inputTime, clientTZOffset, Locale.US);
				}
				else
				{
					WorkCalendar taskCalendar =  WorkCalendar.getDefaultCalendar(); 
					Integer duration =    (int)taskCalendar.computeDuration(context,sDate, fDate);
					//Integer duration = new Integer((int)DateUtil.computeDuration(sDate, fDate));
					attributes.put(ATTRIBUTE_TASK_ACTUAL_DURATION,
							duration.toString());
				}
				//setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_DURATION,
				//                  duration.toString());
				attributes.put(ATTRIBUTE_PERCENT_COMPLETE, "100");
				setAttributeValues(context, attributes);

				if(ProgramCentralUtil.isNotNullString(projectAccessListId)){
					Task commonTask = new Task();
					commonTask.setId(getId(context));
					commonTask.rollupAndSave(context);
				}
			}
			else if (toState.equals(STATE_PROJECT_TASK_ACTIVE))
			{
				Task commonTask = new Task();
				commonTask.setId(getId(context));
				commonTask.rollupAndSave(context);
			}
		}
		else if (sEvent.equals("Demote"))
		{
			if(ProgramCentralConstants.TYPE_PHASE.equalsIgnoreCase(sTaskType) &&  fromState.equals(STATE_PROJECT_TASK_ACTIVE))
			{
				Task taskObj = new Task(getId(context));
				boolean isToBlockDemoteTask = taskObj.hasConnectedEfforts(context);
				if(isToBlockDemoteTask)
				{
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
							"emxProgramCentral.WeeklyTimeSheet.DemotePhase.PhaseWithEffortCannotBeDemoted", context.getSession().getLanguage());
					throw new Exception(sErrMsg+sTaskName );
				}
			}
			if (fromState.equals(STATE_PROJECT_TASK_COMPLETE))
			{
				setAttributeValue(context,ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,"");
				setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_DURATION, "");
			}
			if (fromState.equals(STATE_PROJECT_TASK_ACTIVE) || (fromState.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW) &&
					taskPolicy.equals(ProgramCentralConstants.POLICY_PROJECT_REVIEW))) //Added:nr2:Condition to Check for Gate or Milestones)
			{
				//setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE,"");
				Map attributes = new HashMap(3);
				attributes.put(ATTRIBUTE_TASK_ACTUAL_START_DATE, "");
				attributes.put(ATTRIBUTE_PERCENT_COMPLETE, "0");
				setAttributeValues(context, attributes);
			}
			//Added:nr2:PRG:R210:29-05-2010:For Project Gate Highlight
			//This is added as when Gate is demoted from Review state to Create state, %complete
			//should become 0 and Actual start date as blank
			if(fromState.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW) &&
					taskPolicy.equals(ProgramCentralConstants.POLICY_PROJECT_REVIEW)){
				setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE,"");
				setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_DURATION,"0.0");
			}
			//End:nr2:PRG:R210:29-05-2010:For Project Gate Highlight
		}

		DebugUtil.debug("Exiting triggerModifyAttributes function");
			}
	/**
	 * This function modifies the parent attributes.
	 * If the child task is get demoted from 'Complete' to 'Review' state and
	 * no other child task is in 'Complete' state, then removes parent task's
	 * actual end date and actual duration.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @throws Exception if operation fails
	 * @since V6R2013X.
	 */
	public void triggerModifyParentActualAttributes(Context context, String[] args) throws FrameworkException {

		String objectId = args[0];
		String fromState = args[1];
		String toState = args[2];

		StringList selectList   =   new StringList();
		selectList.add(SELECT_ID);
		selectList.add(SELECT_TYPE);
		selectList.add(SELECT_CURRENT);
		selectList.add(SELECT_POLICY);
		selectList.add(SELECT_STATES);

		setId(objectId);
		MapList parentList = getParentInfo(context, 1, selectList);
		boolean isRemoveParentAttributeValues = true;

		if(parentList.size() > 0) {
			Map map = (Map) parentList.get(0);
			String parentId = (String) map.get(SELECT_ID);
			String type = (String) map.get(SELECT_TYPE);
			String parentState = (String) map.get(SELECT_CURRENT);
			setId(parentId);

			//get children (one level)
			MapList childTaskList = getTasks(context,this,1,selectList,null);
			if (childTaskList.size() > 0) {
				Iterator itr = childTaskList.iterator();
				while (itr.hasNext()) {
					Map taskmap = (Map) itr.next();
					String childState = (String)taskmap.get(SELECT_CURRENT);
					String taskPolicy = (String)taskmap.get(SELECT_POLICY);
					StringList taskStateList =(StringList) taskmap.get(SELECT_STATES);

					//get the position of the task's current state from its state list.
					int taskCurrentPosition =   taskStateList.indexOf(childState);

					//get the position of "Complete" in the state list
					//if the "Complete" state does not exist then taskReviewPostion will be -1
					int taskCompletePostion = taskStateList.indexOf(STATE_PROJECT_TASK_COMPLETE);

					if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy) && taskCompletePostion == -1) {
						taskCompletePostion = 1;
					}
					//check if any of the tasks are in Complete state.
					//If there are no tasks in Complete state, then dont demote the parent task
					if (taskCompletePostion != -1 && taskCurrentPosition >= taskCompletePostion) {
						isRemoveParentAttributeValues = false;
						break;
					}
				}
			}
		}
		if (isRemoveParentAttributeValues) {
        	try{
				ProgramCentralUtil.pushUserContext(context);
			Map attributeMap    =   new HashMap();
			attributeMap.put(ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,"");
			attributeMap.put(ATTRIBUTE_TASK_ACTUAL_DURATION,"");
			setAttributeValues(context,attributeMap);
		}
        	finally{
        		ProgramCentralUtil.popUserContext(context);
        	}
        }
	}

	/**
	 * This function checks the states of the child tasks have reached the
	 * state defined in the trigger object
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the relationship to check for
	 *        2 - String containing the state to check for
	 * @return int based on success or failure
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
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
				sErrMsg = "emxProgramCentral.ProgramObject.emxProgramTriggerCheckChildrenStates.PromoteMessage";
			}
			else
			{
				sErrMsg = "emxProgramCentral.ProgramObject.emxProgramTriggerCheckChildrenStates.PromoteMandatoryMessage";
			}
			//**Start**
			if(ProjectSpace.isEPMInstalled(context))
			{
				PropertyUtil.setGlobalRPEValue(context,"FAILED_FROM_CHILD_CHECK", "true");
			}
			checkState = i18nNow.getStateI18NString(POLICY_PROJECT_TASK, checkState, context.getSession().getLanguage());
			String sKey[] = {"StateName"};
			String sValue[] = {checkState};
			String companyName = null;
			sErrMsg  = emxProgramCentralUtilClass.getMessage(context, sErrMsg,
					sKey, sValue, companyName);
			emxContextUtilBase_mxJPO.mqlNotice(context, sErrMsg);
			throw new MatrixException(sErrMsg);
		}
			}

	/**
	 * Check the state of all children tasks for a specific state.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param targetState the state name to compare against
	 * @param whereClause optional where clause to apply against the children
	 * @return boolean true if all children are at least at the target state;
	 *                  false if any of the children do not meet the target state requirement.
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
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
			}else if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equalsIgnoreCase(state) || ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equalsIgnoreCase(state)){ 
            	// This else if loop is added for  IR-492789-3DEXPERIENCER2015x
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
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
	 */
	public void triggerSetPercentageCompletion(Context context, String[] args)
			throws Exception
			{
		// get values from args.
		String objectId = args[0];
		String fromState = args[1];
		String toState   = args[2];

		String newState = PropertyUtil.getRPEValue(context, "State", true);
		if(ProgramCentralUtil.isNullString(newState)){
			PropertyUtil.setRPEValue(context, "State", fromState, true);
		}

		//**Start*
		if(ProjectSpace.isEPMInstalled(context))
		{
			boolean check      = true;
			String sErrMsg = null;
			String taskName = "";

			setId(objectId);
			String cmd = "get $1 $2";
			String sEvent = MqlUtil.mqlCommand(context, cmd , true,"env", "EVENT");

			String relPattern = DomainConstants.RELATIONSHIP_SUBTASK;
			String typePattern = DomainConstants.TYPE_PART_QUALITY_PLAN;

			StringList busSelects = new StringList(1);
			busSelects.add(SELECT_TYPE);

			/* Update the Percentage Complete only for the WBS connected to Project Or Project Template */
			MapList parentList = getParentInfo(context,0, busSelects);

			Iterator itr = parentList.iterator();
			boolean updatePercentageComplete = false;
			while (itr.hasNext())
			{
				Map map = (Map) itr.next();
				String parentType = (String) map.get(SELECT_TYPE);
				if(TYPE_PROJECT_SPACE.equals(parentType) || mxType.isOfParentType(context,parentType,DomainConstants.TYPE_PROJECT_SPACE) ||
						TYPE_PROJECT_TEMPLATE.equals(parentType) )//Modified for Subtype
						{
					updatePercentageComplete = true;
					break;
						}
			}
			//Added:nr2:29-05-2010:PRG:R210:For Stage Gate highlight
			String taskPolicy = getInfo(context,SELECT_POLICY);
			//End:nr2:29-05-2010:PRG:R210:For Stage Gate highlight
			if (updatePercentageComplete)
			{
				if (sEvent.equals("Promote"))
				{
					/*                if (fromState.equals(STATE_PROJECT_TASK_ACTIVE) || (fromState.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW) &&
                        taskPolicy.equals(ProgramCentralConstants.POLICY_PROJECT_REVIEW))) //Added:nr2:For stage Gate highlight
					 */
					if (fromState.equals(STATE_PROJECT_TASK_ACTIVE)) //Added:nr2:For stage Gate highlight
					{
						// set the percent complete to 100%
						ICDocument icObj = new ICDocument();
						check  =  icObj.checkICDeliverableforCompletionState(context,objectId);

						if(check)
						{
							setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "100");
						}
						else
						{
							PropertyUtil.setGlobalRPEValue(context,"BLOCK_CALL_MESSAGE", "true");
							sErrMsg = "emxProgramCentral.ProgramObject.triggerSetPercentageCompletion.TaskPromoteMessage";
							DomainObject taskObj = DomainObject.newInstance(context,objectId);
							taskName = (String)taskObj.getName(context);
							String taskState = getInfo(context,SELECT_CURRENT);
							String blockPromote =PropertyUtil.getGlobalRPEValue(context,"BLOCK_CALL_PROMOTE_ACTIVE");
							if(taskState!=null && !"null".equals(taskState) && !"".equals(taskState) && taskState.equals(STATE_PROJECT_TASK_REVIEW))
							{
								if(blockPromote!=null && !"true".equalsIgnoreCase(blockPromote))
								{
									if(POLICY_PROJECT_TASK.equals(taskPolicy)){ //Added:nr2:PRG:R210:For stage Gate
										setState(context, STATE_PROJECT_TASK_ACTIVE);
									}
									setId(objectId);
								}
							}
							String sKey[] = {"taskName"};
							String sValue[] = {taskName};
							sErrMsg  = emxProgramCentralUtilClass.getMessage(context, sErrMsg, sKey, sValue, null);
							emxContextUtilBase_mxJPO.mqlNotice(context, sErrMsg);
						}

					}

				}
				else if (sEvent.equals("Demote"))
				{
					if (toState.equals(STATE_PROJECT_TASK_ACTIVE)|| (toState.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_CREATE) &&
							taskPolicy.equals(ProgramCentralConstants.POLICY_PROJECT_REVIEW))) //Added:nr2:For stage Gate highlight)
					{
						// set the percent complete to 0%
						setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "0");
					}
				}
			}
		}
		//**End**
		else
		{
			setId(objectId);
			String cmd = "get $1 $2";
			String sEvent = MqlUtil.mqlCommand(context, cmd , true,"env", "EVENT");

			String relPattern = DomainConstants.RELATIONSHIP_SUBTASK;
			String typePattern = DomainConstants.TYPE_PART_QUALITY_PLAN;

			StringList busSelects = new StringList(1);
			busSelects.add(SELECT_TYPE);

			/* Update the Percentage Complete only for the WBS connected to Project Or Project Template */
			MapList parentList = getParentInfo(context,0, busSelects);

			Iterator itr = parentList.iterator();
			boolean updatePercentageComplete = false;
			while (itr.hasNext())
			{
				Map map = (Map) itr.next();
				String parentType = (String) map.get(SELECT_TYPE);
				if(TYPE_PROJECT_SPACE.equals(parentType) || mxType.isOfParentType(context,parentType,DomainConstants.TYPE_PROJECT_SPACE) ||
						TYPE_PROJECT_TEMPLATE.equals(parentType) )//Modified for Subtype
						{
					updatePercentageComplete = true;
					break;
						}
			}

			//Added:nr2:29-05-2010:PRG:R210:For Stage Gate highlight
			String taskPolicy = getInfo(context,SELECT_POLICY);
			//End:nr2:29-05-2010:PRG:R210:For Stage Gate highlight

			if (updatePercentageComplete)
			{
				if (sEvent.equals("Promote"))
				{
					/*                if (fromState.equals(STATE_PROJECT_TASK_ACTIVE) || (fromState.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_EW) &&
                        taskPolicy.equals(ProgramCentralConstants.POLICY_PROJECT_REVIEW))) //Added:nr2:For stage Gate highlight)
					 */
					if (fromState.equals(STATE_PROJECT_TASK_ACTIVE)) //Added:nr2:For stage Gate highlight)

					{
						// set the percent complete to 100%
						setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "100");
					}
				}
				else if (sEvent.equals("Demote"))
				{
					/*                if (toState.equals(STATE_PROJECT_TASK_ACTIVE)|| (toState.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_CREATE) &&
                        taskPolicy.equals(ProgramCentralConstants.POLICY_PROJECT_REVIEW))) //Added:nr2:For stage Gate highlight))
					 */
					if (toState.equals(STATE_PROJECT_TASK_ACTIVE)) //Added:nr2:For stage Gate highlight))
					{
						// set the percent complete to 0%
						setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "0");
					}
				}
				//[MODIFIED::IR-114043V6R2013]
				TaskDateRollup rollup = new TaskDateRollup(objectId);
				rollup.validateTask(context);
				//[END::IR-114043V6R2013]
			}
		}
			}

	/**
	 * Demote check trigger
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @return int based on success or failure
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
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
					//Added:nr2:31-05-2010:PRG:R210:For Stage Gate Highlight
					//This condition is added so that non-availability of Active state
					//in Project Review policy does not block the demotion of sibling
					//tasks in Active state.
					String taskId = (String) map.get(SELECT_ID);
					DomainObject gateObj = DomainObject.newInstance(context,taskId);
					String taskPolicy = gateObj.getInfo(context,SELECT_POLICY);
					if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy) && taskActiveStatePosition == -1){
						taskActiveStatePosition = 2;
					}
					//End:nr2:31-05-2010:PRG:R210:For Stage Gate Highlight
					//if any of the child is in a state Active or beyond
					//block the demotion
					if(taskCurrentPosition >= taskActiveStatePosition)
					{
						String sErrMsg = "emxProgramCentral.ProgramObject.emxProgramTriggerStateActiveCheckSummaryTask.DemoteMessage";
						String sKey[] = {"ToStateName", "StateName"};
						String sValue[] = {toState, fromState};
						String companyName = null;
						sErrMsg = emxProgramCentralUtilClass.getMessage(
								context, sErrMsg, sKey, sValue, companyName);
						emxContextUtilBase_mxJPO.mqlNotice(context, sErrMsg);
						blockTrigger = true;
						throw new MatrixException(sErrMsg);
					}
				}
			}
		}
		else if (fromState.equals(STATE_PROJECT_TASK_COMPLETE))
		{
			MapList parentList = getParentInfo(context, 1, busSelects);
			if(!parentList.isEmpty())
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
					String sErrMsg = "emxProgramCentral.ProgramObject.emxProgramTriggerStateCompleteCheckParent.DemoteMessage";
					String sKey[] = {"StateName"};
					String sValue[] = {parentState};
					String companyName = null;
					sErrMsg = emxProgramCentralUtilClass.getMessage(
							context, sErrMsg, sKey, sValue, companyName);
					emxContextUtilBase_mxJPO.mqlNotice(context, sErrMsg);
					throw new MatrixException(sErrMsg);
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
	 * Demote check trigger for blocking Gate Demotion from Complete State
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @return int based on success or failure
	 * @throws Exception if operation fails
	 */
	public int triggerGateDemoteCheck(Context context, String[] args)
			throws Exception
			{
		// get values from args.
		String objectId = args[0];
		String fromState = args[1];
		String toState   = args[2];

		setId(objectId);
		boolean blockTrigger = false;
		StringList busSelects = new StringList(1);
		busSelects.add(ProgramCentralConstants.SELECT_IS_GATE);

		if (fromState.equals(STATE_PROJECT_TASK_COMPLETE))
		{
			DomainObject dObj = DomainObject.newInstance(context);
			dObj.setId(objectId);
			Map dObjMap = dObj.getInfo(context, busSelects);
			if("true".equalsIgnoreCase((String)dObjMap.get(ProgramCentralConstants.SELECT_IS_GATE)))
				{
					blockTrigger = true;
					String sErrMsg = "emxProgramCentral.ProgramObject.emxProgramTriggerStateCompleteCheckGateType.DemoteMessage";
					String sKey[] = {};
					String sValue[] = {};
					String companyName = null;
					sErrMsg = emxProgramCentralUtilClass.getMessage(
							context, sErrMsg, sKey, sValue, companyName);
					//${CLASS:emxContextUtilBase}.mqlNotice(context, sErrMsg);
					//System.out.println(sErrMsg);
					//MqlUtil.mqlCommand(context, "notice " + sErrMsg );
					throw new MatrixException(sErrMsg);
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
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @return int based on success or failure
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
	 */
	public int triggerCheckDependency(Context context, String[] args) throws Exception
	{
		// get values from args.
		String objectId = args[0];
		String fromState = args[1];
		String toState   = args[2];

		//Added:12-APR-2011:S4E\MS9:R210.HF4 PRG:HF-097114V6R2011x
		//getting environment variable "decisionName" which is assigned to requested Project State in projectPolicyChangeSequence method of emxProjectHoldAndCancelBase JPO
		String decisionName = MqlUtil.mqlCommand(context,"get $1 $2", "env","decisionName");
		//End:12-APR-2011:S4E\MS9:R210.HF4 PRG:HF-097114V6R2011x

		String requiredState = null;
		String dependencyTaskName  = null;
		String type = null;

		setId(objectId);
		boolean promoteTask = true;
		StringList busSelects = new StringList(2);
		busSelects.add(SELECT_POLICY);
		busSelects.add(SELECT_CURRENT);
		busSelects.add(SELECT_STATES);
		busSelects.add(SELECT_NAME);
		busSelects.add(SELECT_HAS_PREDECESSORS);
		busSelects.add(SELECT_HAS_SUCCESSORS);
		busSelects.add(SELECT_TASK_PROJECT_TYPE);

		MULTI_VALUE_LIST.add(SELECT_PREDECESSOR_IDS);
		MULTI_VALUE_LIST.add(SELECT_PREDECESSOR_TYPES);
		busSelects.add(SELECT_PREDECESSOR_IDS);
		busSelects.add(SELECT_PREDECESSOR_TYPES);
		Map objMap = getInfo(context, busSelects, MULTI_VALUE_LIST);

		String projectType = (String) objMap.get(SELECT_TASK_PROJECT_TYPE);
		if (PropertyUtil.getSchemaProperty(context, "type_ProjectSnapshot").equals(projectType)) {
			return 0;
		}
		String hasPredecessors = (String) objMap.get(SELECT_HAS_PREDECESSORS);
		String hasSuccessors = (String) objMap.get(SELECT_HAS_SUCCESSORS);
		Object predecessorIds = (Object) objMap.get(SELECT_PREDECESSOR_IDS);
		Object predecessorTypes = (Object) objMap.get(SELECT_PREDECESSOR_TYPES);

		String taskPolicy = (String) objMap.get(SELECT_POLICY);

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
				//Modified:12-APR-2011:S4E\MS9:R210.HF4 PRG:HF-097114V6R2011x
				if ((toState.equals(STATE_PROJECT_TASK_ACTIVE) || ((ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(taskPolicy))&& (toState.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW))))&& !decisionName.equals(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD) && !decisionName.equals(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL))
				{//End:12-APR-2011:S4E\MS9:R210.HF4 PRG:HF-097114V6R2011x
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
				// [MODIFIED::Feb 23, 2011:S4E:R211:IR-097114V6R2012::Start]
				if (toState.equals(STATE_PROJECT_TASK_COMPLETE))
				{
					// [MODIFIED::Feb 23, 2011:S4E:R211:IR-097114V6R2012::End]
					//Added:nr2:PRG:R212:14 Jun 2011:IR-097114V6R2012:Added If condition
					if(!ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equalsIgnoreCase(decisionName) &&
							!ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equalsIgnoreCase(decisionName)){
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
					//End:nr2:PRG:R212:14 Jun 2011:IR-097114V6R2012
				}
			}
		}
		if (promoteTask)
		{
			return 0;
		}
		else
		{
			String sErrMsg = "";
			String strTranslatedRequiredStateName = i18nNow.getStateI18NString(Task.POLICY_PROJECT_TASK, requiredState, context.getSession().getLanguage());
			if(DomainConstants.STATE_PROJECT_SPACE_COMPLETE.equalsIgnoreCase(requiredState)){
				sErrMsg = "emxProgramCentral.ProgramObject.emxProgramTriggerCheckDependencyOnCompleteState.Message";
			}else{
				sErrMsg = "emxProgramCentral.ProgramObject.emxProgramTriggerCheckDependency.Message";
			}
			String sKey[] = {"dependencyTaskName", "dependencyType","requiredState"};
			String sValue[] = {dependencyTaskName, type, strTranslatedRequiredStateName}; //Modified:2-Sept-2010:rg6:R210 PRG:IR-065172V6R2011x
			String companyName = null;
			//**Start**
			if(ProjectSpace.isEPMInstalled(context))
			{
				PropertyUtil.setGlobalRPEValue(context,"FAILED_FROM_DEPENDENCY_CHECK", "true");
			}
			//**End**
			sErrMsg  = emxProgramCentralUtilClass.getMessage(context,
					sErrMsg, sKey, sValue, companyName);
			emxContextUtilBase_mxJPO.mqlNotice(context, sErrMsg);
			throw new MatrixException(sErrMsg);
			//return 1;
		}

	}

	private void changeStateTo(Context context,String objectId , String state) throws FrameworkException{

		Task task = new Task();
		task.setId(objectId);

		task.setState(context, state);

	}

	/**
	 * Modify action for  Percentage complete attribute
	 * Based on the %age set the trigger will promote or demote
	 * the object to the required state
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
	 */
	public int triggerModifyPercentCompleteAction(Context context, String[] args) throws Exception {
		StringList busSelects = new StringList();
		busSelects.add(SELECT_PERCENT_COMPLETE);
		busSelects.add(SELECT_CURRENT);
		busSelects.add(SELECT_STATES);
		busSelects.add(SELECT_PARENT_ID);
		busSelects.add(SELECT_TYPE);
		busSelects.add(SELECT_POLICY);
		busSelects.add(ProgramCentralConstants.SELECT_NEEDS_REVIEW);
		busSelects.add("from[Subtask]");

		try {
			// get values from args.
			String objectId = args[0];
			if(UIUtil.isNotNullAndNotEmpty(objectId)) {
				setId(objectId);

				Map taskMap = getInfo(context, busSelects);
				String type = (String) taskMap.get(SELECT_TYPE);
				String state = (String) taskMap.get(SELECT_CURRENT);
				StringList taskStateList = (StringList) taskMap.get(SELECT_STATES);
				String parentId = (String) taskMap.get(SELECT_PARENT_ID);
				String newPercent = (String) taskMap.get(SELECT_PERCENT_COMPLETE);
				double newPercentValue = Task.parseToDouble(newPercent);
				String taskPolicy = (String) taskMap.get(SELECT_POLICY);
				String taskReviewCheck = (String)taskMap.get(ProgramCentralConstants.SELECT_NEEDS_REVIEW);
				String isSummaryTask = (String)taskMap.get("from[Subtask]");
				//get the position of the task's current state wrt
				//to its state list
				int taskCurrentPosition = taskStateList.indexOf(state);
				//get the position of "Active" and "Complete" in the state list
				int taskActiveStatePosition = taskStateList.indexOf(STATE_PROJECT_TASK_ACTIVE);
				int taskCompleteStatePosition = taskStateList.indexOf(STATE_PROJECT_TASK_COMPLETE);

				if ((taskActiveStatePosition == -1 && !ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy))
						|| taskCompleteStatePosition == -1)   //Added::nr2:PRG:R210:14-06-2010:For Stage Gate Highlight
				{
					//object may not be of type task or project
					if(!ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy))
						return 0;
				}

				//1. if newpercent is 0% and the task is above active state demote it
				//    to active state. Do nothing if it is below active state
				//2. if newpercent is between 1-99% irrespective of the current state
				//    set the state to Active
				//3. if newPercent is 100% and the task is already in Complete or
				//    beyond do nothing
				//4. if newPercent is 100% and the task is below Complete state setState
				//    to review or complete based on taskReviewCheck.
				//5. if taskReviewCheck = True...Set state to Review.
				//   taskReviewCheck = False...Set state to Compleate.

				//Since there is no Active state in Project Review Policy, taskActiveStatePosition will be -1 and control
				//will never go in conditions where <anything> > taskActiveStatePosition
				//Hence we make this 2 (as for Project Task Policy), to enable comparison to take place.

				if(taskActiveStatePosition == -1 && ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)) {
					taskActiveStatePosition = 2;
					taskCompleteStatePosition = 4;
				}
				if((newPercentValue == 0) && taskCurrentPosition > taskActiveStatePosition) {
					//Refer #1
					if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy)){
						changeStateTo(context,objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_CREATE);
					} else {
						//This condition is added if ProjectSpace is governed by Project Space Hold Cancel Policy
						int activeStatePosition = taskStateList.indexOf(STATE_PROJECT_TASK_ACTIVE);
						if(activeStatePosition != -1){
							changeStateTo(context,objectId, STATE_PROJECT_TASK_CREATE);
						}
					}
				} else if(newPercentValue > 0 && newPercentValue < 100) {
					//Refer #2
					if (taskActiveStatePosition > taskCurrentPosition) {
						//Do not set states if this is "Part Quality Plan" object
						if(!TYPE_PART_QUALITY_PLAN.equals(type)) {
							//This is added so that Gate will not be Moved to assign state.
							if(! ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy) && !ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)){
								changeStateTo(context, objectId, STATE_PROJECT_TASK_ACTIVE);
							}
						}
						//Coming in this condition if Gate/Milestone (with policy Project Review)
						//is in Create state and %Complete is set between 1-99%.
						if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy)){
							changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW);
						} else if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)) {
							HashMap programMap = new HashMap();
							programMap.put(SELECT_ID, parentId);
							programMap.put(SELECT_CURRENT, STATE_PROJECT_SPACE_REVIEW);
							String[] arrJPOArgs = JPO.packArgs(programMap);
							JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
						}
					} else if (taskActiveStatePosition < taskCurrentPosition) {
						//control will come in this if condition when Gate/Milestone is in Review/Complete state.
						//We do not want to move the Gate to Create state (As there is only this state below current state)
						//of the Gate.Also Gate should not be demoted from Complete state.
						if(!ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy) && !ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)){ //End:nr2:PRG:R210:14-06-2010:For Stage Gate Highlight
							changeStateTo(context, objectId, STATE_PROJECT_TASK_ACTIVE);
						}
					}
				} else if((newPercentValue == 100) && taskCurrentPosition < taskCompleteStatePosition) {
					//At this Position we do not want any further comparison to be made for Project Review Policy
					//So we will set taskActiveStatePosition as -1, which we had set to 2 earlier.
					/*              if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy)){
                    taskActiveStatePosition = -1;
                }
					 */
					//Refer #3 and #4
					//had to break up the promotion to 3 stages
					if (taskActiveStatePosition > taskCurrentPosition) {
						if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy)){
							changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW);
						} else if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)) {
							HashMap programMap = new HashMap();
							programMap.put(SELECT_ID, objectId);
							programMap.put(SELECT_CURRENT, STATE_PROJECT_SPACE_ACTIVE);
							String[] arrJPOArgs = JPO.packArgs(programMap);
							JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
							setId(objectId);
						} else {
							changeStateTo(context, objectId, STATE_PROJECT_TASK_ACTIVE);
						}
					}

					//set to Complete if this is "Part Quality Plan" object
					//because PQP has different number of states than Project and Task
					if(!TYPE_PART_QUALITY_PLAN.equals(type)) {
						if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy)){
							changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW);
						} else if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)) {
							HashMap programMap = new HashMap();
							programMap.put(SELECT_ID, objectId);
							programMap.put(SELECT_CURRENT, STATE_PROJECT_SPACE_REVIEW);
							String[] arrJPOArgs = JPO.packArgs(programMap);
							JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
							setId(objectId);
						}

						else {
							changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW);
                    	String toState = PropertyUtil.getRPEValue(context, "State", true);

                    	if(ProgramCentralConstants.POLICY_PROJECT_TASK.equals(taskPolicy)
                    			&& "false".equalsIgnoreCase(isSummaryTask)
                    			&& "NO".equalsIgnoreCase(taskReviewCheck)
                    			&& !(STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(toState))) {
                    		changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE);
							}
						}
					}
				}
			}
			return 0;
		}
		catch (Exception exp) {
			throw exp;
		}
	}

	/**
	 * Calculate percent complete for the given task based on status of subtasks.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing task id to update as argument 0
	 * @throws Exception if operation fails
	 * @since PC 10.0.0.0
	 */
	private String parentTaskId = "";
	private String action = "";
	public void calculatePercentComplete(Context context, String[] args)
			throws Exception
			{
		//Added:12-Mar-10:vm3:R209:PRG:Bug 016043
		if(args.length > 1) {
			parentTaskId = args[1];
			action = args[2];
		}
		//End-Added:12-Mar-10:vm3:R209:PRG:Bug 016043
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
	 */
	protected void calculatePercentComplete(Context context)
			throws Exception
			{
		// gqh: commented out all logic; percent complete is part of date rollup now.
		/*
        //get the children of the parent
        StringList busSelects = new StringList(2);

      //Added:12-Mar-10:vm3:R209:PRG:Bug 016043
        String selectedTaskDuration = "";
        double dselectedTaskDuration = 0.0;
        String selectedTaskPercentComplete = "";
        double dselectedTaskPercentComplete = 0.0;
        if(!parentTaskId.equals("") && action.equals("remove"))
        {

            Map selectedTaskMap = new HashMap();
            String selectedTaskId = this.getId();
            busSelects.add(SELECT_PERCENT_COMPLETE);
            busSelects.add(SELECT_TASK_ESTIMATED_DURATION);

            try{
                selectedTaskMap = getInfo(context,busSelects);
            }
            catch(Exception e){
                e.printStackTrace();
            }

            selectedTaskDuration = (String)selectedTaskMap.get(SELECT_TASK_ESTIMATED_DURATION);
            selectedTaskPercentComplete = (String)selectedTaskMap.get(SELECT_PERCENT_COMPLETE);
            dselectedTaskDuration = Task.parseToDouble(selectedTaskDuration);
            dselectedTaskPercentComplete = Task.parseToDouble(selectedTaskPercentComplete);
            MapList parerntIdList =  this.getParentInfo(context, 1, new StringList(DomainConstants.SELECT_ID));
            String parentId = parentTaskId;
            this.setId(parentId);
        }
      //End-Added:12-Mar-10:vm3:R209:PRG:Bug 016043
        busSelects.add(SELECT_PERCENT_COMPLETE);
        busSelects.add(SELECT_TASK_ESTIMATED_DURATION);

        MapList tasks = getTasks(context, this, 1, busSelects, null);
        double totalDuration = 0;
        double completed = 0;
        String percentCompleted = "";
        Iterator itr = tasks.iterator();
        double percent = 0.0;
        double duration = 0.0;

       while (itr.hasNext())
        {
            Map map = (Map) itr.next();

            percent = Task.parseToDouble((String)
                                        map.get(SELECT_PERCENT_COMPLETE));

            duration = Task.parseToDouble((String)
                                        map.get(SELECT_TASK_ESTIMATED_DURATION));

            //Bug# 306151 - (duration = 0) problem
            if(duration <= 0)
            {
                duration = 1;
            }

            totalDuration += duration;
            completed = completed + (percent * duration);

        }
       //Added:12-Mar-10:vm3:R209:PRG:Bug 016043
       if(!parentTaskId.equals("") && action.equals("remove"))
       {
           totalDuration -= dselectedTaskDuration;
           completed -= dselectedTaskPercentComplete*dselectedTaskDuration;
       }
       //End-Added:12-Mar-10:vm3:R209:PRG:Bug 016043
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
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws Exception if operation fails
	 * @since AEF 9.5.5.0
	 */
	public void triggerSetParentTaskToCompleteAction(Context context, String[] args)
			throws Exception
			{
		// get values from args.
		//**Start**
		if(ProjectSpace.isEPMInstalled(context))
		{

			String objectId = args[0];

			// The object id  of the deliverable items
			setId(objectId);

			StringList selects = new StringList(1);
			selects.addElement(SELECT_ID);

			String sReachedCompState = "attribute["+DomainConstants.ATTRIBUTE_REACHED_COMPLETION+"]";

			//list of relationship object selectables
			StringList relSelects    = new StringList(1);
			relSelects.addElement(DomainRelationship.SELECT_ID);
			relSelects.addElement(sReachedCompState);

			//Relationship Where
			String yes = "Yes";
			String relWhere = "(!("+sReachedCompState +" ~~ " +yes+ "))";


			//get the IC objects
			MapList boTasks= getRelatedObjects(
					context,                                     //matrix context
					DomainConstants.RELATIONSHIP_TASK_DELIVERABLE,
					QUERY_WILDCARD,                              // type pattern,
					selects,                                     // objectSelects
					relSelects,                                  // relationshipPattern
					true,                                        // getTo
					false,                                       // getFrom
					(short)1,                                    // recurseToLevel
					null,                                        // objectWhere
					relWhere                                     // relationshipWhere
					);

			//logic for Deliverable Objects
			//Before completing the Task, should check for associated Delvierable objects for Completion.
			boolean blnPromote= false;
			String  promote    = "";
			String relId       ="";
			String reachedCompState ="";
			ICDocument design = new ICDocument();

			Map  icMap = null;

			Map objectMap      = null;

			int cnt             = 0;
			String taskId       ="";


			if ( boTasks != null && boTasks.size() > 0 )
			{
				Iterator taskObjItr = boTasks.iterator();
				String[] taskArr    = new String[boTasks.size()];

				//Setting Relationship attribute reached completion state to True
				//For all the tasks related to the non IC object
				while(taskObjItr.hasNext())
				{
					icMap               = (Map)taskObjItr.next();
					relId               = (String)icMap.get(DomainRelationship.SELECT_ID);
					reachedCompState    = (String)icMap.get(sReachedCompState);

					if((relId != null && !"".equals(relId)) && !reachedCompState.equals("Yes"))
					{
						DomainRelationship.setAttributeValue(context, relId, DomainConstants.ATTRIBUTE_REACHED_COMPLETION, "Yes");
					}
				}

				for (int i = 0; i < boTasks.size(); i++)
				{
					try
					{
						taskArr[i] =
								(String) ((HashMap) boTasks.get(i)).get("id");
					}
					catch (Exception e)
					{
						taskArr[i] =
								(String) ((Hashtable) boTasks.get(i)).get("id");
					}

				} // end of for loop

				//Call this method to check whether all the deliverables have reached
				//their completion state or not.

				MapList TaskIds = design.checkICDeliverableforCompletionState (context,taskArr);

				if ( TaskIds != null && TaskIds.size() > 0 )
				{
					Iterator itr = TaskIds.iterator();

					while (itr.hasNext())
					{
						objectMap = (Map) itr.next();
						java.util.Set keyValueSet = objectMap.entrySet();
						Iterator paramMapIter = keyValueSet.iterator();
						while (paramMapIter.hasNext())
						{
							Map.Entry me = (Map.Entry) paramMapIter.next();
							taskId = (String) me.getKey();
							DomainObject task = newInstance(context, taskId);
							promote = (String) me.getValue();
							if (promote.equals("true"))
							{
								blnPromote = true;
							}
							else
							{
								blnPromote = false;
							}
							if(blnPromote)
							{
								task.setState(context, STATE_PROJECT_TASK_REVIEW);
							}
						}
					}
				}
			}

		}
		//**End**
		else
		{
			String objectId = args[0];

			// The object id  of the deliverable items
			setId(objectId);

			StringList selects = new StringList(1);
			selects.addElement(SELECT_ID);

			MapList boTasks = getRelatedObjects(context,
					RELATIONSHIP_TASK_DELIVERABLE,
					QUERY_WILDCARD,
					selects,
					null,
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


			}


	/****************************************************************************************************
	 *       Methods for Config Table Conversion Task
	 ****************************************************************************************************/
	/**
	 * gets the list of Assigned WBS Task Objects to context User
	 * Used for PMCAssignedWBSTaskSummary table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since PMC 11.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getAssignedWBSTask(Context context, String[] args)
			throws Exception
			{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
				DomainConstants.TYPE_TASK, "PROGRAM");

		// Assigned status means not in the complete,archive or create state
		StringBuffer busWhere =  new StringBuffer();
		busWhere.append(task.SELECT_CURRENT);
		busWhere.append("!='");
		busWhere.append(STATE_PROJECT_TASK_COMPLETE);
		busWhere.append("'");

		// append the Not create state
		if (!"".equals(STATE_PROJECT_TASK_CREATE))
		{
			busWhere.append(" && ");
			busWhere.append(task.SELECT_CURRENT);
			busWhere.append("!='");
			busWhere.append(STATE_PROJECT_TASK_CREATE);
			busWhere.append("'");
		}

		// append the Not archive state
		if (!"".equalsIgnoreCase(STATE_PROJECT_TASK_ARCHIVE))
		{
			busWhere.append(" && ");
			busWhere.append(task.SELECT_CURRENT);
			busWhere.append("!='");
			busWhere.append(STATE_PROJECT_TASK_ARCHIVE);
			busWhere.append("'");
		}
		return getMyTasks(context, args, busWhere.toString());
			}

	/**
	 * gets the list of Completed WBS Task objects to context User
	 * Used for PMCAssignedWBSTaskSummary table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since PMC 11.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getCompletedWBSTask(Context context, String[] args)
			throws Exception
			{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
				DomainConstants.TYPE_TASK, "PROGRAM");
		String busWhere = task.SELECT_CURRENT + "=='" + STATE_PROJECT_TASK_COMPLETE +"'";
		return getMyTasks(context, args, busWhere);
			}

	/**
	 * gets the list of All WBS Task Objects to context User
	 * Used for PMCAssignedWBSTaskSummary table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since PMC 11.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getAllWBSTask(Context context, String[] args)
			throws Exception
			{
		StringBuffer busWhere =  new StringBuffer();
		busWhere.append("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current" + "!='" + ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD +"'");
		busWhere.append(" && ");
		busWhere.append("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current" + "!='" + ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL + "'");
		return getMyTasks(context, args, busWhere.toString());
			}

	/**
	 * gets the list of WBS Task to context User
	 * Used for PMCAssignedWBSTaskSummary table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since PMC 11.0.0.0
	 */
	public Object getMyTasks(Context context, String[] args, String busWhere)
			throws Exception
			{
		// Check license while listing Risk, if license check fails here
		// the risks will not be listed.
		//
		ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_PRG_LICENSE_ARRAY);

		//Assigned task must be a WBS task. 
		String sBusWhere = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "]=='True'";
		if(UIUtil.isNotNullAndNotEmpty(busWhere)){
			busWhere += " && " + sBusWhere;
		}else{
			busWhere = sBusWhere;
		}
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
				DomainConstants.TYPE_TASK, "PROGRAM");

		MapList assignmentList = null;
		try
		{
			com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
			StringList busSelects = new StringList(1);
			busSelects.add(task.SELECT_ID);
			assignmentList = person.getAssignments(context, busSelects, busWhere);

			//Added:nr2:PRG:R210:29-05-2010:For Project Space Hold and Cancel Highlight
			assignmentList = removeTasksWithProjectInHoldOrCancel(context,assignmentList);
			//End:nr2:PRG:R210:29-05-2010:For Project Space Hold and Cancel Highlight
			assignmentList = hideExperimentTask(context,assignmentList); //Added for "What if"
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return assignmentList;
		}
			}

	/**
	 * Hide Experiment Task from WBS Task.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param assignmentList list of Task.
	 * @return task list.
	 * @throws MatrixException, if operation fails.
	 */
	private static MapList hideExperimentTask(Context context,MapList assignmentList) throws MatrixException
	{
		MapList mlTaskList = new MapList();
		String []strTaskIDs = new String[assignmentList.size()];

		for(int i=0;i<assignmentList.size();i++){
			Map taskMap = (Map)assignmentList.get(i);
			String strTaskId = (String)taskMap.get(DomainObject.SELECT_ID);
			strTaskIDs[i] = strTaskId;
		}

		StringList slSelect = new StringList(2);
		slSelect.addElement(ProgramCentralConstants.SELECT_PROJECT_TYPE);
		slSelect.addElement(DomainObject.SELECT_ID);

		DomainObject object = DomainObject.newInstance(context);
		MapList mlParentInfo = object.getInfo(context, strTaskIDs, slSelect);

		for(int ind=0;ind<mlParentInfo.size();ind++){
			Map temp = (Map)mlParentInfo.get(ind);
			String strProjectType = (String)temp.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);
			if(!strProjectType.equalsIgnoreCase(ProgramCentralConstants.TYPE_EXPERIMENT)){
				mlTaskList.add(temp);
			}
		}
		return mlTaskList;
	}

	//Added:nr2:PRG:R210:29-05-2010:For Project Space Hold and Cancel Highlight
	public static MapList removeTasksWithProjectInHoldOrCancel(Context context,MapList assignmentList)
			throws MatrixException{
		try{
			//For the Tasks whose parent project are in Hold or Cancel states
			//Remove such tasks from the MapList
			MapList tasksToRemoveList = new MapList();
			for(int i=0;(null != assignmentList && i<assignmentList.size());i++){
				Map taskMap = (Map) assignmentList.get(i);
				String taskId = (String) taskMap.get(SELECT_ID);

				com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK,DomainConstants.TYPE_PROGRAM);
				if(null != taskId && !"".equals(taskId)){
					taskObj.setId(taskId);
				}
				StringList sl = new StringList();
				sl.add(SELECT_ID);
				Map projectMap = taskObj.getProject(context, sl);
				String projectId = (String)projectMap.get(SELECT_ID);
				com.matrixone.apps.program.ProjectSpace projObj = (com.matrixone.apps.program.ProjectSpace)
						DomainObject.newInstance(context,
								DomainConstants.TYPE_PROJECT_SPACE,
								DomainConstants.TYPE_PROGRAM);
				projObj.setId(projectId);

				String projectPolicy = projObj.getInfo(context,SELECT_POLICY);
				if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(projectPolicy)){
					tasksToRemoveList.add(taskMap);
				}
			}
			if (assignmentList != null) {
				assignmentList.removeAll(tasksToRemoveList);
			}
			return assignmentList;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}


	/**
	 * This method is used to show the status image.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns Vector containing all the status image value as String.
	 * @throws Exception if the operation fails
	 * @since PMC 11.0.0.0
	 */
	public Vector getStatusIcon(Context context, String[] args)
			throws Exception
			{
		Vector showIcon = new Vector();

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
				DomainConstants.TYPE_TASK, "PROGRAM");
		String policyName = task.getDefaultPolicy(context);
		String COMPLETE_STATE = PropertyUtil.getSchemaProperty(context, "policy",
				policyName, "state_Complete");
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			//Added:08-June-2010:ak4:R210:PRG:Bug:055631
			HashMap paramList = (HashMap) programMap.get("paramList");
			String exportFormat = (String)paramList.get("exportFormat");
			//End:08-June-2010:ak4:R210:PRG:Bug:055631
			MapList objectList = (MapList) programMap.get("objectList");
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),
					Locale.US);
			Map objectMap = null;
			int i = 0;
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			boolean flag = false;
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
				i++;
			}

			StringList busSelect = new StringList(7);
			busSelect.add(DomainConstants.SELECT_ID);
			busSelect.add(DomainConstants.SELECT_TYPE);
			busSelect.add(task.SELECT_BASELINE_CURRENT_END_DATE);
			busSelect.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
			busSelect.add(task.SELECT_CURRENT);
			busSelect.add(task.SELECT_PERCENT_COMPLETE);
			busSelect.add(SELECT_IS_DELETED_SUBTASK);
			busSelect.add(SELECT_DELETED_SUBTASK_ATTRIBUTE_COMMENTS);
			//Merge From 2012:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
			busSelect.add(SELECT_TASK_ACTUAL_FINISH_DATE);
			//End:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
			MapList actionList = DomainObject.getInfo(context, objIdArr, busSelect);

			int actionListSize = 0;
			if (actionList != null)
			{
				actionListSize = actionList.size();
			}

			int yellowRedThreshold = Integer.parseInt(EnoviaResourceBundle.getProperty(context,
					"eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
			Date tempDate = new Date();
			Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(),
					tempDate.getDate());

			StringList taskSubtypesList = Task.getAllTaskTypeNames(context);

			String strI18nBehindSchedule = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.Common.Legend.BehindSchedule", context.getSession().getLanguage());

			String strI18nOnTime = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.Common.OnTime", context.getSession().getLanguage());

			String strI18nLate = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.Common.Late", context.getSession().getLanguage());

			for (i = 0; i < actionListSize && actionList != null; i++){
				String statusGif = "";
				String statusToolTip = "";
				objectMap = (Map) actionList.get(i);
				// Start Deleted Task tooltip should change with comments
				boolean blDeletedTask = false;
				String strComments = null;
				String strObjectId = (String) objectMap.get(DomainConstants.SELECT_ID);
				String strCurrent = (String) objectMap.get(task.SELECT_CURRENT);
				String strPercentComplete = (String) objectMap.get(task.SELECT_PERCENT_COMPLETE);
				String strObjectType = (String) objectMap.get(DomainConstants.SELECT_TYPE);
				String taskEstFinishDate = (String) objectMap.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
				
				if(strObjectType.equalsIgnoreCase(DomainConstants.TYPE_PERSON) || UIUtil.isNullOrEmpty(taskEstFinishDate)){
					statusGif = ProgramCentralConstants.EMPTY_STRING;
					showIcon.add("<label>"+statusGif+"</label>");
					continue;
				}
				if(taskSubtypesList.indexOf(strObjectType)>=0){
					if("TRUE".equalsIgnoreCase((String) objectMap.get(SELECT_IS_DELETED_SUBTASK))){
						blDeletedTask = true;
						strComments = (String) objectMap.get(SELECT_DELETED_SUBTASK_ATTRIBUTE_COMMENTS);
					}
				}
				//End Deleted Task tooltip should change with comments
				Date baselineCurrentEndDate = null;
				String baselineCurrentEndDateString = (String) objectMap.get(task.SELECT_BASELINE_CURRENT_END_DATE);
				Date estFinishDate = sdf.parse((String) objectMap.get(
						task.SELECT_TASK_ESTIMATED_FINISH_DATE));
				//Merge From 2012:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
				Date actualFinishDate = null;
				String actualFinishDateString   =   (String) objectMap.get(task.SELECT_TASK_ACTUAL_FINISH_DATE);
				if (actualFinishDateString != null && !actualFinishDateString.equals("")) {
					actualFinishDate    =   sdf.parse(actualFinishDateString);
				}
				//End:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
				if (!"".equals(baselineCurrentEndDateString))
				{
					baselineCurrentEndDate = sdf.parse((String) objectMap.get(
							task.SELECT_BASELINE_CURRENT_END_DATE));
				}
				long daysRemaining;
				if (null == baselineCurrentEndDate)
				{
					daysRemaining = (long) task.computeDuration(sysDate,
							estFinishDate);
				}
				else
				{
					daysRemaining = (long) task.computeDuration(sysDate,
							baselineCurrentEndDate);
				}

				if (null == baselineCurrentEndDate)
				{
					if (strCurrent.equals(COMPLETE_STATE) || strPercentComplete.equals("100.0"))
					{
						statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" alt=\"";
						if(blDeletedTask){
							//Added for special character.
							statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strComments;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}else {
							//Merge From 2012:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
							if(actualFinishDate != null && actualFinishDate.compareTo(estFinishDate)<=0) {
								statusGif += strI18nOnTime + "\" title=\""+strI18nOnTime;
								//Added:08-June-2010:ak4:R210:PRG:Bug:055631
								statusToolTip = strI18nOnTime;
								//End:08-June-2010:ak4:R210:PRG:Bug:055631
							}else{
								statusGif += strI18nOnTime + "\" title=\""+strI18nLate;
								statusToolTip = strI18nLate;
							}
							//End:I16:PRG:R213:21-Oct-2011:IR-134662V6R2013
						}
						statusGif += "\"/>";
					}
					else if (!strCurrent.equals(COMPLETE_STATE) && sysDate.after(estFinishDate))
					{
						statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"";
						if(blDeletedTask){
							//Added for special character.
							statusGif += XSSUtil.encodeForHTML(context,strComments) +"\" title=\""+XSSUtil.encodeForHTML(context,strComments);
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strComments;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}else {
							statusGif += strI18nLate + "\" title=\""+strI18nLate;
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strI18nLate;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}
						statusGif += "\"/>";
					}
					else if (!strCurrent.equals(COMPLETE_STATE) && daysRemaining <= yellowRedThreshold)
					{
						statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" alt=\"";
						if(blDeletedTask){
							//Added for special character.
							statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strComments;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}else {
							statusGif += strI18nBehindSchedule + "\" title=\""+strI18nBehindSchedule;
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strI18nBehindSchedule;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}
						statusGif += "\"/>";
					}
					else
					{
						statusGif = ProgramCentralConstants.EMPTY_STRING;
					}
				}
				else
				{
					if (strCurrent.equals(COMPLETE_STATE) || "100.0".equals(strPercentComplete))
					{
						statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" alt=\"";
						if(blDeletedTask){
							//Added for special character.
							statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strComments;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}else {
							statusGif += strI18nOnTime + "\" title=\""+strI18nOnTime;
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strI18nOnTime;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}
						statusGif += "\"/>";
					}
					else if (!strCurrent.equals(COMPLETE_STATE) && sysDate.after(baselineCurrentEndDate))
					{
						statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"";
						if(blDeletedTask){
							//Added for special character.
							statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip =  XSSUtil.encodeForHTML(context,strComments);
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}else {
							statusGif += strI18nLate + "\" title=\""+strI18nLate;
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strI18nLate;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}
						statusGif += "\"/>";
					}
					else if (!strCurrent.equals(COMPLETE_STATE) && (daysRemaining <= yellowRedThreshold))
					{
						statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" alt=\"";
						if(blDeletedTask){
							//Added for special character.
							statusGif += XSSUtil.encodeForHTML(context,strComments) + "\" title=\""+XSSUtil.encodeForHTML(context,strComments);
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strComments;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}else {
							statusGif += strI18nBehindSchedule + "\" title=\""+strI18nBehindSchedule;
							//Added:08-June-2010:ak4:R210:PRG:Bug:055631
							statusToolTip = strI18nBehindSchedule;
							//End:08-June-2010:ak4:R210:PRG:Bug:055631
						}
						statusGif += "\"/>";
					}
					else
					{
						statusGif = ProgramCentralConstants.EMPTY_STRING;
					}
				}
				//Added:08-June-2010:ak4:R210:PRG:Bug:055631
				if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
					showIcon.add(statusToolTip);// statusToolTip will get exported
				}else{
					//End:08-June-2010:ak4:R210:PRG:Bug:055631
					showIcon.add("<label>"+statusGif+"</label>");// this default string displayed in Browser
				}
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return showIcon;
		}
			}
	public Vector getQuestionIcon(Context context, String[] args)
			throws Exception
			{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
		String templateType = task.TYPE_PROJECT_TEMPLATE;
		Vector showIcon = new Vector();
		String statusGif = null;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		String jsTreeID = (String) paramList.get("jsTreeID");
		Iterator objectListIterator = objectList.iterator();
		while (objectListIterator.hasNext())
		{
			statusGif = "";
			Map objectMap = (Map) objectListIterator.next();
			String taskId = (String) objectMap.get(DomainObject.SELECT_ID);
			String taskLevel = (String) objectMap.get(DomainObject.SELECT_LEVEL);
			StringList busSelects = new StringList(2);
			busSelects.add(task.SELECT_HAS_QUESTIONS);
			busSelects.add(task.SELECT_QUESTION_ID);
			task.setId(taskId);
			String thisObjectType = task.getType(context);
			String questionURL = null;
			if (!thisObjectType.equals(templateType))
			{
				Map map = task.getInfo(context, busSelects);
				if(((String)map.get(task.SELECT_HAS_QUESTIONS)).equalsIgnoreCase("true"))
				{
					statusGif = "<a href=\"JavaScript:showDialog('../common/emxTree.jsp?objectId=";
					statusGif +=XSSUtil.encodeForHTML(context,(String)map.get(task.SELECT_QUESTION_ID));
					statusGif+="&amp;mode=replace&amp;jsTreeID=";
					statusGif+=XSSUtil.encodeForHTML(context,(String)jsTreeID);
					statusGif+="&amp;AppendParameters=false')\"";
					statusGif+= "><img src=\"../common/images/iconStatusCheckmark.gif\" border=\"0\" alt=\"";
					statusGif += "\"/></a>";
				}
			}
			showIcon.add(statusGif);
		}

		return showIcon;
			}
	/**
	 * This method is used to show the slipdays.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns Vector containing the slipdays value as Long.
	 * @throws Exception if the operation fails
	 * @since PMC 11.0.0.0
	 */
	public Vector getSlipdays(Context context, String[] args)
			throws Exception
			{
		Vector showSlipDays = new Vector();

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
				DomainConstants.TYPE_TASK, "PROGRAM");
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),
					Locale.US);
			Map objectMap = null;
			int i = 0;

			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
				i++;
			}

			StringList busSelect = new StringList(2);
			busSelect.add(task.SELECT_TASK_ACTUAL_FINISH_DATE);
			busSelect.add(task.SELECT_TASK_ESTIMATED_FINISH_DATE);

			MapList actionList = DomainObject.getInfo(context, objIdArr,
					busSelect);

			int actionListSize = 0;
			if (actionList != null)
			{
				actionListSize = actionList.size();
			}

			int yellowRedThreshold = Integer.parseInt(EnoviaResourceBundle.getProperty(context,
					"eServiceApplicationProgramCentral.SlipThresholdYellowRed"));
			Date tempDate = new Date();
			Date sysDate = new Date(tempDate.getYear(), tempDate.getMonth(),
					tempDate.getDate());

			for (i = 0; i < actionListSize && actionList != null; i++)
			{
				objectMap = (Map) actionList.get(i);
				long slipDays = (long) 0;
				long actualFinishDate = 0;
				Date actFinishedDate = null;

				String actFinishDate = (String) objectMap.get(task.SELECT_TASK_ACTUAL_FINISH_DATE);
				String taskEstFinishDate = (String) objectMap.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE);
				if(UIUtil.isNullOrEmpty(taskEstFinishDate)){
					showSlipDays.add("" + slipDays);
					continue;
				}
				Date estFinishDate = sdf.parse(taskEstFinishDate);
				if ((actFinishDate != null) && !actFinishDate.equals(""))
				{
					actualFinishDate = sdf.parse(actFinishDate).getTime();
					actFinishedDate = sdf.parse(actFinishDate);
					actFinishedDate.setHours(0);
					actFinishedDate.setMinutes(0);
					actFinishedDate.setSeconds(0);
				}

				estFinishDate.setHours(0);
				estFinishDate.setMinutes(0);
				estFinishDate.setSeconds(0);
				sysDate.setHours(0);
				sysDate.setMinutes(0);
				sysDate.setSeconds(0);

				//get Slip Days for complete but late task
				if ((actFinishDate != null) && !actFinishDate.equals("") && actFinishedDate != null &&
						actFinishedDate.after(estFinishDate))
				{
					slipDays = task.computeDuration(estFinishDate,
							actFinishedDate) - 1;//take out the starting day
				}

				//get Slip Days for incomplete and late task
				if (((actFinishDate == null) || actFinishDate.equals("")) &&
						sysDate.after(estFinishDate))
				{
					slipDays = task.computeDuration(estFinishDate, sysDate) -
							1;//take out the starting day
				}

				showSlipDays.add("" + slipDays);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return showSlipDays;
		}
			}

	/**
	 * This method is used to show the Project Name.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns Vector containing the project name value as String.
	 * @throws Exception if the operation fails
	 * @since PMC 11.0.0.0
	 */
	public Vector getProjectName(Context context, String[] args)
			throws Exception
			{
		Vector showProjectName = new Vector();
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
				DomainConstants.TYPE_TASK, "PROGRAM");
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map objectMap = null;

			Iterator objectListIterator = objectList.iterator();
			int i = 0;
			String[] objIdArr = new String[objectList.size()];
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
				i++;
			}
			final String SELECT_TASK_PROJECT_NAME = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";

			StringList busSelect = new StringList(1);
			busSelect.add(task.SELECT_ID);
			busSelect.add(SELECT_TASK_PROJECT_NAME);
			busSelect.add(SELECT_TASK_PROJECT_TYPE);
			busSelect.add(SELECT_TASK_PROJECT_ID);


			MapList actionList = DomainObject.getInfo(context, objIdArr,busSelect);

			int actionListSize = 0;
			if (actionList != null)
			{
				actionListSize = actionList.size();
			}

			for (i = 0; i < actionListSize && actionList != null; i++)
			{
				objectMap = (Map) actionList.get(i);
				String taskId = (String) objectMap.get(SELECT_ID);
				String taskProjectName = (String) objectMap.get(SELECT_TASK_PROJECT_NAME);
				String taskProjectType = (String) objectMap.get(SELECT_TASK_PROJECT_TYPE);
				String taskProjectId = (String) objectMap.get(SELECT_TASK_PROJECT_ID);
				StringBuffer strUrl = new StringBuffer();

				//If task Assignee does not have access to project then taskProjectName will be empty String.
				if (ProgramCentralUtil.isNullString(taskProjectName))
				{
					taskProjectName = EMPTY_STRING;
					strUrl.append(taskProjectName);
				}
				else
				{
					String symType = FrameworkUtil.getAliasForAdmin(context, "Type", taskProjectType, true);
					String iconImage;
					try{
						iconImage = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + symType);
					}catch(Exception e){
						iconImage  = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon.type_ProjectSpace");
					}
					taskProjectName = "<img src=\"../common/images/"+iconImage+"\"/>" + XSSUtil.encodeForHTML(context,taskProjectName);

					strUrl.append("<a href ='javascript:showModalDialog(\"");
					strUrl.append("../common/emxTree.jsp?objectId=");
					strUrl.append(XSSUtil.encodeForURL(context,taskProjectId));
					strUrl.append("\", \"875\", \"550\", \"false\", \"popup\")'>");

					strUrl.append(taskProjectName);
					strUrl.append("</a>");
				}
				showProjectName.add(strUrl.toString());
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return showProjectName;
		}
			}

	/**
	 * This method is used to show the Program Name.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns Vector containing the program name value as String.
	 * @throws Exception if the operation fails
	 * @since PMC 11.0.0.0
	 */
	public Vector getProgramName(Context context, String[] args)throws Exception
	{
		Vector showProgramName = new Vector();
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
				DomainConstants.TYPE_TASK, "PROGRAM");
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map objectMap = null;

			Iterator objectListIterator = objectList.iterator();
			int i = 0;
			String[] objIdArr = new String[objectList.size()];
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
				i++;
			}

			StringList busSelect = new StringList(1);
			busSelect.add(task.SELECT_ID);

			MapList actionList = DomainObject.getInfo(context, objIdArr,
					busSelect);

			int actionListSize = 0;
			if (actionList != null){
				actionListSize = actionList.size();
			}

			for (i = 0; i < actionListSize && actionList != null; i++){
				String programName = DomainObject.EMPTY_STRING;
				objectMap = (Map) actionList.get(i);

				String taskId = (String) objectMap.get(SELECT_ID);
				task.setId(taskId);

				StringList busSelects = new StringList(1);
				busSelects.add(ProjectSpace.SELECT_PROGRAM_NAME);
			  try {
				Map programInfo = task.getProject(context, busSelects);
				Object program = programInfo.get(ProjectSpace.SELECT_PROGRAM_NAME);

				if(program instanceof StringList){
					StringList programList = (StringList)program;
					for(int index=0; index<programList.size();index++){
						String progName = (String)programList.get(index);
						progName = "<img src=\"../common/images/iconSmallProgram.gif\"/>" +XSSUtil.encodeForHTML(context, progName);

						if(ProgramCentralUtil.isNullString(programName)){
							programName = progName;
						}else{
							programName += ","+progName;
						}
					}
				}else if(program instanceof String){
					programName = (String)program;
					if(ProgramCentralUtil.isNullString(programName) || "#DENIED!".equals(programName)){
						programName = DomainObject.EMPTY_STRING;
					}else{
						programName = "<img src=\"../common/images/iconSmallProgram.gif\"/>" +XSSUtil.encodeForHTML(context, programName);
					}
				}else{
					programName = DomainObject.EMPTY_STRING;
				}
			    } catch (Exception e) {
					programName = DomainObject.EMPTY_STRING;
			    }

				showProgramName.add(programName);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return showProgramName;
		}
	}

	/**
	 * This method is used to get choices for Percentage Compelete attribute
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns HashMap containing TaskPercentages sorted in ascending order.
	 * @throws Exception if the operation fails
	 * @since PMC 10.Next
	 */
	public HashMap getPerCompelete(Context context, String[] args)
			throws Exception
			{
		HashMap map = new HashMap();
		try
		{
			//load percentage values from properties file
			String percentages = EnoviaResourceBundle.getProperty(context, "emxComponents.TaskPercentages");

			StringList percentageList = new StringList();
			StringTokenizer parser = new StringTokenizer(percentages,",");
			while (parser.hasMoreTokens())
			{
				String sValue = parser.nextToken();
				sValue = sValue.trim();
				percentageList.add(sValue);
			}
			// Added For Bug No: 326784
			Collections.sort(percentageList,new Comparator()
			{
				public int compare(Object o1,Object o2)
				{
					if(o1==null || o2==null) {
						throw new RuntimeException("Comparision can't be done so,Sorting is not possible");
					}
					String first=(String)o1;
					String second=(String)o2;
					double firstEntry=Task.parseToDouble(first);
					double secondEntry=Task.parseToDouble(second);
					if (firstEntry < secondEntry) {
						return -1;
					}
					if (firstEntry > secondEntry) {
						return 1;
					}
					return 0;
				}
			});

			map.put("field_choices", percentageList);
			map.put("field_display_choices", percentageList);
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return map;
		}
			}

	/**
	 * getProjectRoleRange - This Method indicates the role the person serves for the project.
	 * Used for PMCWBSViewtable table
	 * This function was added to fix the BUG : 357120
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *    objectList - Contains a MapList of Maps which contains object names
	 * @return Vector of "Project Role" values for each row
	 * @throws Exception if the operation fails
	 * @since PMC V6R2009x
	 */
	public HashMap getProjectRoleRange(Context context, String[] args)
			throws Exception
			{
		HashMap mapReturnRoleRange = new HashMap();
		try
		{
			String sLanguage = context.getSession().getLanguage();
			// [MODIFIED::PRG:RG6:Jan 20, 2011:IR-055750V6R2012:R211::Start]
			StringList slProjectRoles = new StringList();
			StringList slProjectRolesTranslated = new StringList();

			// get project roles
			StringList strList = ProgramCentralUtil.getAllProjectRoles(context);
			// get i18 roles
			emxProjectMemberBase_mxJPO projMember = new emxProjectMemberBase_mxJPO(context,args);
			Map mapRoleI18nStrings = projMember.geti18nProjectRoleRDOValues(context);


			if(null != strList)
			{
				int size = strList.size();
				for(int itr = 0; itr < size; itr++)
				{
					String strRoleTemp = (String)strList.get(itr);
					String strI18RoleVal = (String)mapRoleI18nStrings.get(strRoleTemp);

					if(ProgramCentralUtil.isNullString(strRoleTemp))
					{
						strRoleTemp = "";
						strI18RoleVal = "";
					}
					else
					{
						if(ProgramCentralUtil.isNullString(strI18RoleVal))
						{
							strI18RoleVal = strRoleTemp;
						}
					}

					slProjectRoles.add(strRoleTemp);
					slProjectRolesTranslated.add(strI18RoleVal.trim());
				}
			}

			mapReturnRoleRange.put("field_choices", slProjectRoles);
			mapReturnRoleRange.put("field_display_choices", slProjectRolesTranslated);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		// [MODIFIED::PRG:RG6:Jan 20, 2011:IR-055750V6R2012:R211::End]
		return  mapReturnRoleRange;
			}


	/**
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.0.0
	 */
	public int triggerChangeOwnerAction (Context context, String[] args)
			throws Exception
			{
		/*
        try{
            String objectId = args[0];
			String sKindOfOwner = args[1];

			if (sKindOfOwner == null)
               sKindOfOwner = "";

            if (!("owner").equalsIgnoreCase(sKindOfOwner))
			{
				return 0;
			}

            setId(objectId);

            StringList selectables = new StringList(2);
            selectables.add(SELECT_TASK_ESTIMATED_DURATION);
            selectables.add(SELECT_SUBTASK_IDS);
            Map taskInfo = getInfo(context, selectables);
            if (taskInfo.get(SELECT_SUBTASK_IDS) == null)
            {
                String sDuration = (String) taskInfo.get(SELECT_TASK_ESTIMATED_DURATION);
                Long duration = new Long(Task.parseToDouble(sDuration).longValue());
                TaskDateRollup taskdaterollup = new TaskDateRollup(getId());
                taskdaterollup.updateDuration(context, duration, true);
            }
        } catch(Exception ex){
            throw ex;
        }
		 */
		return 0;
			}

	/**
	 * Returns the list of names of the tasks assigned to context user.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request parameters.
	 * @return a list of formatted names of the tasks assigned to context user.
	 * @throws Exception if operation fails.
	 */
	public Vector getName (Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		String colorStyle = "color:default";

		if (ProgramCentralUtil.isNotNullString(strPrinterFriendly))
			isPrinterFriendly = true;
		else
			strPrinterFriendly = "";
		Iterator objectListIterator = objectList.iterator();
		Vector columnValues = new Vector(objectList.size());
		StringList slTaskSelectables = new StringList(3);
		slTaskSelectables.add(SELECT_ATTRIBUTE_CRITICAL_TASK);
		slTaskSelectables.add(SELECT_NAME);
		slTaskSelectables.add(SELECT_CURRENT);

		while (objectListIterator.hasNext()){
			Map objectMap = (Map) objectListIterator.next();
			String taskId = (String) objectMap.get(DomainObject.SELECT_ID);
			DomainObject taskObj  = DomainObject.newInstance(context, taskId);
			Map<String,String> taskInfo = taskObj.getInfo(context, slTaskSelectables);
			String critcalTask = (String)taskInfo.get(SELECT_ATTRIBUTE_CRITICAL_TASK);
			String sState = taskInfo.get(SELECT_CURRENT);
			String sTaskName = taskInfo.get(SELECT_NAME);

			StringBuffer sRetValue = new StringBuffer();
			colorStyle = "color:default";
			if(("true").equalsIgnoreCase(critcalTask) && !(STATE_TASK_COMPLETE.equalsIgnoreCase(sState))
					&& !("csv".equalsIgnoreCase(strPrinterFriendly))){
				colorStyle = "color:red";
			}
			if(!isPrinterFriendly){
                sRetValue.append("<input type=\"hidden\" name='forsort' value=\"" +XSSUtil.encodeForXML(context, sTaskName) + "\"/>");
				sRetValue.append("<a href ='javascript:showModalDialog(\"");
				sRetValue.append("../common/emxTree.jsp?objectId=");
				sRetValue.append(XSSUtil.encodeForURL(context,taskId));
				sRetValue.append("\", \"875\", \"550\", \"false\", \"popup\")' style=\""+colorStyle+"\">");
			}
			sRetValue.append(XSSUtil.encodeForXML(context, (String)taskObj.getName()));
			if(!isPrinterFriendly){
				sRetValue.append("</a>");
			}
			columnValues.add(sRetValue.toString());
		}
		return columnValues;
	}

	/* This method promotes a Task to Complete state
	 * on selection of Actual Finish Date.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - New Value String
	 *        1 - Old Value  String
	 * @return void
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */

	public void updateTaskActualFinishDate(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map mpParamMap = (HashMap)programMap.get("paramMap");
		String strNewVal = (String)mpParamMap.get("New Value");
		String strOldVal = (String)mpParamMap.get("Old Value");
		String objId = (String)mpParamMap.get("objectId");
		String SELECT_ACT_START_DATE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_TaskActualStartDate") + "]";

		if(ProgramCentralUtil.isNotNullString(strNewVal)){
		//Extracting client Time Zone Offset from context object itself
		TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
		double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
		double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
		int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
		Map requestMap = (Map) programMap.get("requestMap");
		Locale locale = (Locale)requestMap.get("locale");
		if(null==locale)
		{
			Object objLocale = (Object)requestMap.get("localeObj");
			if(null!=objLocale)
			{
				if(objLocale instanceof Locale)
				{
					locale = (Locale)requestMap.get("localeObj");
				}
				else if(objLocale  instanceof String[])
				{
					String[]strlocale = (String[])requestMap.get("localeObj");
					locale = new Locale(strlocale[0]) ;
				}
				else if(objLocale instanceof String)
				{
					locale = new Locale((String)objLocale) ;
				}
			}
			else
			{
				throw new IllegalArgumentException ("Locale is not defined correctly");
			}
		}
		java.text.DateFormat format = DateFormat.getDateTimeInstance(
				iDateFormat, iDateFormat, locale);

		Calendar calToday = Calendar.getInstance();

		String strNewDateVal = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context, strNewVal, clientTZOffset, locale);

		Date dtNewValue = eMatrixDateFormat.getJavaDate(strNewDateVal);
		calToday.setTime(dtNewValue);

		if (ProjectSpace.isEPMInstalled(context)) {
			blockValue ="";
		}
		if (strNewVal == null) {
			strNewVal = "";
		}
		if (strOldVal == null) {
			strOldVal = "";
		}
		if (!strNewVal.equals(strOldVal)) {
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject
					.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			task.setId(objId);
			String sCompleteStateName   =   null;
			if (task.isKindOf(context,ProgramCentralConstants.TYPE_PROJECT_CONCEPT)) {
				sCompleteStateName = ProgramCentralConstants.STATE_PROJECT_CONCEPT_REVIEW;
			} else {
				sCompleteStateName = ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE;
			}

			String strFieldValueAttr = com.matrixone.apps.domain.util.eMatrixDateFormat
					.getFormattedInputDate(context, strNewVal, clientTZOffset,
							locale);
			String strTempfieldValueAttr = eMatrixDateFormat
					.getFormattedDisplayDateTime(context, strFieldValueAttr,
							true, iDateFormat, clientTZOffset, locale);
			Date newFinishDate = format.parse(strTempfieldValueAttr);

			Calendar calNewFinishDate = Calendar.getInstance();
			calNewFinishDate.setTime(newFinishDate);
			calNewFinishDate.set(Calendar.HOUR_OF_DAY,17);
			calNewFinishDate.set(Calendar.MINUTE,0);
			calNewFinishDate.set(Calendar.SECOND,0);
			calNewFinishDate.set(Calendar.MILLISECOND,0);
			newFinishDate = calNewFinishDate.getTime();

			StringList busSelect = new StringList(3);
			busSelect.add(DomainConstants.SELECT_NAME);
			busSelect.add(SELECT_ACT_START_DATE);
			busSelect.add(DomainConstants.SELECT_CURRENT);
			busSelect.add(Task.SELECT_TASK_ESTIMATED_DURATION);

			Map taskData = (Map)task.getInfo(context,busSelect);
			String taskName =  (String)taskData.get(DomainObject.SELECT_NAME);
			String strActualStartDate = (String) taskData
					.get(SELECT_ACT_START_DATE);
			String strEstDuration = (String) taskData.get(Task.SELECT_TASK_ESTIMATED_DURATION);
			if (Task.parseToDouble(strEstDuration) == 0)
			{
				//if est duration is 0l then set finish time to 8am.
				calNewFinishDate.set(Calendar.HOUR_OF_DAY,8);
				newFinishDate = calNewFinishDate.getTime();
			}
			String strNewFinishDate = format.format(newFinishDate).trim();

			try {
				// **Start**
				if (ProjectSpace.isEPMInstalled(context)) {

					String strCurState = (String) taskData
							.get(task.SELECT_CURRENT);
					if (!STATE_PROJECT_TASK_ASSIGN.equals(strCurState)) {
						boolean blnUpdate   = false;
						Date fDate = new Date();
						Date sDate = new Date();
						Integer duration;
						if (strCurState.equals(STATE_PROJECT_TASK_ACTIVE)
								|| strCurState
								.equals(STATE_PROJECT_TASK_REVIEW)) {
							blnUpdate = true;
							PropertyUtil.setGlobalRPEValue(context,
									"BLOCK_CALL_PROMOTE_ACTIVE", "true");
							task.setState(context, sCompleteStateName);
							String strVal = PropertyUtil.getGlobalRPEValue(
									context, "BLOCK_CALL_MESSAGE");
							if (strVal != null
									&& "true".equalsIgnoreCase(strVal)) {
								task.setState(context, strCurState);
							}
						}

						//Modified for 360727 Start
						if (strCurState.equals(STATE_PROJECT_TASK_COMPLETE)) {
							blnUpdate = true;
						}
						//End
						if (blnUpdate) {
							blockValue = PropertyUtil.getGlobalRPEValue(
									context, "BLOCK_CALL_PROMOTE_ACTIVE");
							if (blockValue != null
									&& !"true".equalsIgnoreCase(blockValue)) {
								strNewFinishDate = eMatrixDateFormat
										.getFormattedInputDateTime(context,
												strNewFinishDate,FINISH_DATE_SET_TIME, clientTZOffset, locale);
								/* gqh: replaced with date rollup call below.
                                    task.setAttributeValue(
                                                    context,
                                                    task.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,
                                                    strNewFinishDate);
								 */

								updateTaskMap(context, objId, "actualFinishDate", newFinishDate);
							}
							fDate = format.parse(strNewFinishDate);
						}
						/* gqh: duration performed by date rollup.
                            if (strActualStartDate != null
                                    && !"".equals(strActualStartDate)
                                    && !"null".equals(strActualStartDate)) {
                                sDate = eMatrixDateFormat.getJavaDate(
                                        strActualStartDate, locale);
                                duration = new Integer((int) DateUtil
                                        .computeDuration(sDate, fDate));
                                if (blockValue != null
                                        && !"true".equalsIgnoreCase(blockValue)) {
                                    task
                                            .setAttributeValue(
                                                    context,
                                                    task.ATTRIBUTE_TASK_ACTUAL_DURATION,
                                                    duration.toString());
                                }
                            }
						 */
					} else {
						PropertyUtil.setGlobalRPEValue(context,
								"BLOCK_CALL_PROMOTE_ACTIVE", "true");
						task.setState(context, sCompleteStateName);
						String strValue = PropertyUtil.getGlobalRPEValue(
								context, "BLOCK_CALL_MESSAGE");
						if (strValue != null
								&& "true".equalsIgnoreCase(strValue)) {
							task.setState(context,
									STATE_PROJECT_TASK_ASSIGN);
						}
					}
				}
				else {
					/* gqh: commented out logic; replaced by date rollup function.

                        String strCurState = (String) taskData
                                .get(task.SELECT_CURRENT);

                        Date fDate, sDate;
                            Integer duration;
                        fDate = format.parse(strNewFinishDate);
                        strNewFinishDate = eMatrixDateFormat
                        .getFormattedInputDateTime(context,
                                strNewFinishDate,FINISH_DATE_SET_TIME, clientTZOffset, locale);
                        task.setAttributeValue(context,
                                task.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE,
                                strNewFinishDate);

                        if (strActualStartDate != null
                                && !"".equals(strActualStartDate)
                                && !"null".equals(strActualStartDate)) {
                            sDate = eMatrixDateFormat.getJavaDate(
                                    strActualStartDate, locale);
                        } else {
                            sDate = eMatrixDateFormat.getJavaDate(
                                    strNewFinishDate, locale);
                            }
                        duration = new Integer((int) DateUtil.computeDuration(
                                sDate, fDate));
                        task.setAttributeValue(context,
                                task.ATTRIBUTE_TASK_ACTUAL_DURATION, duration
                                        .toString());
					 */
					task.setState(context, sCompleteStateName);


					updateTaskMap(context, objId, "actualFinishDate", newFinishDate);
				}
			} catch (Exception e) {
				throw new Exception (e.toString());
			}
		}
		}
	}//end of the method

	/* This method will update the value of Percentage Complete attribute of a Task.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - New Value String
	 *        1 - Old Value  String
	 * @return void
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */

	public void updateTaskPercentageComplete(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map mpParamMap = (HashMap)programMap.get("paramMap");
		String strNewVal = (String)mpParamMap.get("New Value");
		String objId = (String)mpParamMap.get("objectId");

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		task.setId(objId);

		///ADDED for 358231
		StringList slSubtasks = task.getInfoList(context, SELECT_SUBTASK_IDS);
		boolean isSummaryTask = !(slSubtasks == null || slSubtasks.size() == 0);
		if (isSummaryTask)
		{
			//Error
			String strTaskName = task.getInfo(context, DomainObject.SELECT_NAME);
			String strErrorMsg = "emxProgramCentral.WBS.PercentageCompletedCannotChangeForParent";
			String sKey[] = {"TaskName"};
			String sValue[] = {strTaskName};

			String companyName = null;
			strErrorMsg  = emxProgramCentralUtil_mxJPO.getMessage(context,
					strErrorMsg,
					sKey,
					sValue,
					companyName);
			emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
			return;
		}
		///END

		//updateTaskMap(context, objId, "percentComplete", strNewVal);
		Map objectValues = new HashMap(1);
		objectValues.put("percentComplete", strNewVal);

		Map objectList = new HashMap(1);
		objectList.put(objId, objectValues);

		task.updateDates(context, objectList, true, false);

		task.rollupAndSave(context);


		/* DLK Removed; replaced by date rollup call.
        // get the states for the object
        StringList busSelects = new StringList(1);
        busSelects.add(task.SELECT_PERCENT_COMPLETE);
        Map taskMap           = task.getInfo(context, busSelects);
        String oldPercent     = (String) taskMap.get(task.SELECT_PERCENT_COMPLETE);
        if(strNewVal == null) {
            strNewVal = "0.0";
        } else if(strNewVal.indexOf("%")>-1) {
            strNewVal = strNewVal.substring(0,strNewVal.indexOf("%"));
        }
        if(oldPercent == null) {
            strNewVal = "0.0";
        } else if(oldPercent.indexOf("%")>-1) {
            oldPercent = oldPercent.substring(0,oldPercent.indexOf("%"));
        }
        double oldPercentValue     = Task.parseToDouble(oldPercent);
        double currentPercentValue = Task.parseToDouble(strNewVal);
        if(currentPercentValue != oldPercentValue) { //begining of if 1
                        // set the percent complete to value given by user
                        task.setAttributeValue(context, task.ATTRIBUTE_PERCENT_COMPLETE, strNewVal);
         } //end of if 1
		 */
	}//end of the method

	/**
	 * gets the list of deliverables for a task
	 * Used for APPDocumentSummary table from command command_PMCDeliverable
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central 10.7.SP1
	 * @grade 0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getAllDeliverables(Context context, String[] args)
			throws Exception
			{
		MapList deliverablesList = new MapList();

		try
		{
			HashMap programMap        = (HashMap) JPO.unpackArgs(args);
			String  parentId          = (String) programMap.get("objectId");

			// Since the old command href called emxCommonDocumentUI:getDocuments, call
			// that method first (requires a new instance of the emxCommonDocumentUI JPO)
			//
			emxCommonDocumentUI_mxJPO emxCommonDocumentUI = new emxCommonDocumentUI_mxJPO(context, null);

			deliverablesList = (MapList)emxCommonDocumentUI.getDocuments(context, args);

			// Call to get deliverables for VPLM Tasks
			//
			String[] oids = new String[1];
			oids[0] = parentId;
			emxVPLMTask_mxJPO emxVPLMTask = new emxVPLMTask_mxJPO(context, oids);
			MapList vplmDeliverablesList = emxVPLMTask.getDeliverables(context, args);

			if (vplmDeliverablesList != null && !vplmDeliverablesList.isEmpty())
			{
				deliverablesList.addAll(vplmDeliverablesList);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return deliverablesList;
			}


	/**
	 * gets the list of deliverables for a task Used for APPDocumentSummary
	 * table from command command_PMCDeliverable
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: 0 - objectId - task OID
	 * @returns Object
	 * @throws Exception
	 *             if the operation fails
	 * @since Program Central X+2
	 * @grade 0
	 */
	public Object getAllDependencies(Context context, String[] args)
			throws Exception
			{
		MapList maplist= new MapList();
		try
		{
			HashMap programMap        = (HashMap) JPO.unpackArgs(args);

			String  parentId          = (String) programMap.get("objectId");
			// Since the old command href called
			// emxCommonDocumentUI:getDocuments, call
			// that method first (requires a new instance of the
			// emxCommonDocumentUI JPO)
			//

			StringList busSelects=new StringList(2);
			busSelects.add("from["+DomainConstants.RELATIONSHIP_DEPENDENCY+"].to.id");
			DomainObject dom=DomainObject.newInstance(context, parentId);
			busSelects.add(SELECT_ID);
			maplist =dom.getRelatedObjects(context,DomainConstants.RELATIONSHIP_DEPENDENCY, "*",busSelects,null,true,false,(short) 0,"",null);




		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return maplist;
			}

	/**
	 * getMembers - This method gets the List of all Members added to the
	 * Project. Used for PMCProjectMemberSummary table.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @return MapList contains list of project members
	 * @throws Exception
	 *             if the operation fails
	 * @since PMC X+2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMembers(Context context, String[] args) throws Exception {

		HashMap programMap  = (HashMap) JPO.unpackArgs(args);
		String objectId     = (String) programMap.get("objectId");

		AssignedTasksRelationship assignee = null;
		MapList assigneesList = null;
		boolean isKindOfRisk = false;
		boolean editFlag=false;

		StringList busSelects = new StringList(3);
		busSelects.add(Task.SELECT_OWNER);
		busSelects.add(Task.SELECT_TYPE);
		busSelects.add(ProgramCentralConstants.SELECT_IS_RISK);

		StringList relSelects = new StringList(1);
		relSelects.add(AssignedTasksRelationship.SELECT_ID);

		StringList memberSelects = new StringList(8);
		memberSelects.add(Person.SELECT_ID);
		memberSelects.add(Person.SELECT_TYPE);
		memberSelects.add(Person.SELECT_NAME);
		memberSelects.add(Person.SELECT_LEVEL);
		memberSelects.add(Person.SELECT_FIRST_NAME);
		memberSelects.add(Person.SELECT_COMPANY_NAME);
		memberSelects.add(Person.SELECT_LAST_NAME);
		memberSelects.add(Person.SELECT_EMAIL_ADDRESS);

		try {
			Task task = (Task)DomainObject.newInstance(context,TYPE_TASK,PROGRAM);

			//Set the object and determine if object is a task or risk
			task.setId(objectId);
			Map taskInfo = task.getInfo(context,busSelects);

			String kindOfRiskString = (String)taskInfo.get(ProgramCentralConstants.SELECT_IS_RISK);
			isKindOfRisk = "TRUE".equalsIgnoreCase(kindOfRiskString);

			if(isKindOfRisk) {
				Risk risk = (Risk)DomainObject.newInstance(context,TYPE_RISK,PROGRAM);
				risk.setId(objectId);
				assigneesList = risk.getAssignees(context, memberSelects, relSelects, null, null);
				editFlag      = risk.checkAccess(context, (short) AccessConstants.cModify);

			} else {
				relSelects.add(AssignedTasksRelationship.SELECT_ASSIGNEE_ROLE);
				assigneesList = task.getAssignees(context, memberSelects, relSelects, null);
				editFlag      = task.checkAccess(context, (short) AccessConstants.cModify);
			}

			Iterator objectListItr = assigneesList.iterator();

			while (objectListItr.hasNext()) {
				Map objectMap = (Map) objectListItr.next();
				if(editFlag) {
					objectMap.put("editFlag","true");
				} else {
					objectMap.put("editFlag","false");
				}
			}
		}catch(Exception exception){
			throw new MatrixException(exception);
		}
		return assigneesList;
	}

	/**
	 * Returns the Full Name of the aasignee
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: 0 - objectList MapList 1 -
	 *            paramList HashMap
	 * @returns Vector containing the WBS task name value as String.
	 * @throws Exception
	 *             if the operation fails
	 * @since PMC X+2
	 */
	public Vector getAssigneeFullName(Context context, String[] args)
			throws Exception
			{

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		com.matrixone.apps.program.Risk risk = (com.matrixone.apps.program.Risk) DomainObject.newInstance(context, DomainConstants.TYPE_RISK, "PROGRAM");

		com.matrixone.apps.common.Person person =(com.matrixone.apps.common.Person) DomainObject.newInstance(context,
				DomainConstants.TYPE_PERSON);

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		String taskObjectId = (String) paramList.get("objectId");
		String jsTreeID = (String) paramList.get("jsTreeID");
		String language = context.getSession().getLanguage();
		String strSuiteDir = (String) paramList.get("SuiteDirectory");
		String portalMode = (String) paramList.get("portalMode");
		Iterator objectListIterator = objectList.iterator();
		Vector columnValues =  new Vector(objectList.size());
		boolean boolIsOwner=false;
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}

		String riskType = risk.TYPE_RISK;

		// Set the object and determine if object is a task or risk
		task.setId(taskObjectId);
		StringList busSelects = new StringList(7);
		busSelects.add(task.SELECT_OWNER);
		busSelects.add(task.SELECT_TYPE);
		Map taskInfo = task.getInfo(context, busSelects);

		String type = (String) taskInfo.get(task.SELECT_TYPE);
		String owner = (String) taskInfo.get(task.SELECT_OWNER);

		while (objectListIterator.hasNext())
		{
			StringBuffer equivalentLink = new StringBuffer();
			Map objectMap = (Map) objectListIterator.next();
			String objectId = (String) objectMap.get("id");
			String lastName = (String) objectMap.get(person.SELECT_LAST_NAME);
			String firstName = (String) objectMap.get(person.SELECT_FIRST_NAME);
			String fullName = (String) objectMap.get(person.SELECT_NAME);
			String personName = lastName + ", " + firstName;
			String preURL =  "../common/emxTree.jsp?AppendParameters=false&mode=insert&jsTreeID=" +jsTreeID + "&objectId=";
			String imageStr = "";

			if(fullName.equals(owner)) {
				boolIsOwner = true;
			}

			if(boolIsOwner)
			{
				imageStr = "../common/images/iconSmallProjectLead.gif";
			}
			else{
				imageStr =  "../common/images/iconSmallGroup.gif";
			}
			preURL =  "../common/emxTree.jsp?AppendParameters=false&mode=insert&jsTreeID=" +jsTreeID + "&objectId="+objectId+"&target=content";

			equivalentLink.append("<input type=\"hidden\" name=forsort value=\"" +lastName + "\">");

			equivalentLink.append("<img src=\"" + imageStr + "\" border=\"0\">");
			equivalentLink.append("<a href=\"javascript:emxTableColumnLinkClick('" + preURL);
			equivalentLink.append("&emxSuiteDirectory=");
			equivalentLink.append(strSuiteDir);
			equivalentLink.append("&defaultsortname=");
			equivalentLink.append(personName);
			equivalentLink.append("&jsTreeID=");
			equivalentLink.append(jsTreeID);
			if(portalMode != null && !"".equals(portalMode) && !"null".equals(portalMode) && ("true".equals(portalMode) || "false".equals(portalMode)))
			{
				equivalentLink.append("', '600', '600', 'false', 'popup','')\"  class='object'>");
			}
			else
			{
				equivalentLink.append("', '600', '600', 'false', 'content','')\"  class='object'>");
			}
			equivalentLink.append(personName);
			equivalentLink.append("</a>");
			if(!isPrinterFriendly)
			{
				columnValues.addElement(equivalentLink.toString());
			}else{
				columnValues.addElement("<img src=\"" + imageStr + "\" border=\"0\">"+personName);

			}
		}
		return columnValues;
			}




	/**
	 * getEmail - This method gets the List of all Email Ids of Project Task
	 * Assignees Used for PMCTaskAssigneeSummary table.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @return MapList contains list of project members
	 * @throws Exception
	 *             if the operation fails
	 * @since PMC X+2
	 */


	public Vector getEmail(Context context, String[] args)
			throws Exception
			{


		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		Iterator objectListIterator = objectList.iterator();
		com.matrixone.apps.common.Person person =(com.matrixone.apps.common.Person) DomainObject.newInstance(context,
				DomainConstants.TYPE_PERSON);
		Vector vecEMail=new Vector();
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}

		while (objectListIterator.hasNext())
		{
			StringBuffer sbEmailAddress=new StringBuffer();
			Map objectMap = (Map) objectListIterator.next();
			String strEmailAddress = (String) objectMap.get(person.SELECT_EMAIL_ADDRESS);
			sbEmailAddress.append("<a href=\"mailto:"+XSSUtil.encodeForURL(context,strEmailAddress)+"\">"+XSSUtil.encodeForHTML(context,strEmailAddress)+"</a>");
			if(!isPrinterFriendly)
			{
				vecEMail.addElement(sbEmailAddress.toString());
			}else{
				vecEMail.addElement(strEmailAddress);
			}
		}
		return vecEMail;
			}


	/**
	 * showCheckbox - determines if the checkbox needs to be enabled for
	 * PMCTaskAssigneeSummary table
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: 0 - objectList MapList
	 * @returns Object of type Vector
	 * @throws Exception
	 *             if the operation fails
	 * @since Common 10-7-X2
	 * @grade 0
	 */

	public Vector showCheckbox(Context context, String[] args)
			throws Exception
			{
		Vector enableCheckbox = new Vector();

		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			String objectId = (String) programMap.get("objectId");

			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			com.matrixone.apps.program.Risk risk =(com.matrixone.apps.program.Risk) DomainObject.newInstance(context, DomainConstants.TYPE_RISK, "PROGRAM");
			com.matrixone.apps.common.Person person =(com.matrixone.apps.common.Person) DomainObject.newInstance(context,
					DomainConstants.TYPE_PERSON);

			com.matrixone.apps.common.AssignedTasksRelationship assignee = null;

			String riskType = risk.TYPE_RISK;
			Iterator objectListItr = objectList.iterator();
			Map objectMap=null;

			while (objectListItr.hasNext())
			{
				objectMap = (Map) objectListItr.next();
				String editFlag = (String) objectMap.get("editFlag");
				enableCheckbox.add(editFlag);

			}
			// assigneesList.add(taskMap);
		}catch(Exception e)
		{
		}

		return enableCheckbox;
			}

	/**
	 * Mass Task Assignment - update multiple task owners and assignees
	 *
	 * @param context the user context object for the current session
	 * @param args contains a MapList of changes with keys of:
	 *        - New Owner:
	 *              "CHANGE_TYPE" "NEW_OWNER"
	 *              "TASK_ID" [task object id]
	 *              "PERSON_NAME" [username]
	 *        - Assignee Added:
	 *              "CHANGE_TYPE" "ASSIGNEE_ADDED"
	 *              "TASK_ID" [task object id]
	 *              "PERSON_ID" [person object id]
	 *        - Assignee Removed:
	 *              "CHANGE_TYPE" "ASSIGNEE_REMOVED"
	 *              "TASK_ID" [task object id]
	 *              "PERSON_ID" [person object id]
	 *
	 * @throws Exception if the operation fails
	 * @returns nothing
	 * @since AEF 10.5.0.0
	 * @grade 0
	 *
	 * Got from Dale Kort
	 */

	public void applyMassAssignment ( Context context , String args[] )
			throws Exception {

		Map map = ( Map ) JPO.unpackArgs ( args );
		MapList mlChanges = ( MapList ) map.get ( "Changes" );

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);

		if ( mlChanges != null ) {
			mlChanges.sort( "TASK_ID", "ascending", "string" );
		}

		Map mChange;
		String sChangeType;
		String sPersonName;
		String sRelPersonTaskId;
		StringList mailToList = new StringList(1);
		StringList mailCcList = new StringList(1);
		StringList objectIdList = null;

		String taskName = "";
		String taskEstStartDate = "";
		String taskEstFinishDate = "";
		String taskCriticality  = "";
		String companyName = null;
		String sTempMailMessage = "";
		String languageStr = context.getSession().getLanguage();
		String sFinalMailMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
				"emxProgramCentral.Common.AssignTaskMessageTop", languageStr);
		sFinalMailMessage += "\n";
		String sProjectName = "";
		String sMailMessage = "";
		Map<String, List<String>> assignedPersonToTasks = new HashMap<String, List<String>>();
		String mKey[] = {"TaskName", "TaskEstStartDate", "TaskEstFinishDate"};
		java.text.SimpleDateFormat date = new java.text.SimpleDateFormat("MM/dd/yy");

		// Use an MQL session to set owner because it's faster :)
		MQLCommand _mql = new MQLCommand();
		_mql.open(context);

		for ( int i=0; mlChanges != null && i < mlChanges.size(); i++ ) {
			mChange = (Map) mlChanges.get( i );
			sChangeType = (String) mChange.get( "CHANGE_TYPE" );

			// If change type is material, get the material name and determine the appropriate ID
			// before passing to the applyMaterial method
			if ( sChangeType.equals( "NEW_OWNER" ) ) {
				sPersonName = (String) mChange.get( "PERSON_NAME" );
				//_mql.executeCommand( context, "modify bus " + (String) mChange.get( "TASK_ID" ) + " owner \"" + sPersonName + "\"");
				String taskId = (String) mChange.get( "TASK_ID");
				String queryString = "modify bus $1 $2 $3";
				_mql.executeCommand( context,queryString,taskId,"owner",sPersonName);
			} else if ( sChangeType.equals( "ASSIGNEE_ADDED" ) ) {
				List<String> valTaskSet = new ArrayList<String>();
				objectIdList = new StringList(1);
				task.setId( (String) mChange.get( "TASK_ID" ) );
				task.addAssignee( context, (String) mChange.get( "PERSON_ID" ), null , (String) mChange.get( "PERSON_PERCENTALLOCATION" ));

				String personId = (String) mChange.get("PERSON_ID");
				String taskId = (String) mChange.get("TASK_ID");

				Map mapProject = task.getProject(context, new StringList(SELECT_NAME));
				sProjectName = (String)mapProject.get(SELECT_NAME);

				if(assignedPersonToTasks.containsKey(personId)) {
					List<String> taskIdList = assignedPersonToTasks.get(personId);
					taskIdList.add(taskId);
				} else {
					List<String> taskIdList  = new ArrayList<String>();
					taskIdList.add(taskId);
					assignedPersonToTasks.put(personId,taskIdList);
				}
			} else if ( sChangeType.equals( "ASSIGNEE_REMOVED" ) ) {
				//_mql.executeCommand( context, "disconnect bus " + (String) mChange.get( "TASK_ID" ) + " rel \"" + PropertyUtil.getSchemaProperty( context, "relationship_AssignedTasks" ) + "\" from " + (String) mChange.get( "PERSON_ID" ) );
				String taskId = (String) mChange.get( "TASK_ID");
				String relAssginedTask = PropertyUtil.getSchemaProperty( context, "relationship_AssignedTasks");
				String personId = (String) mChange.get( "PERSON_ID");
				String queryString =  "disconnect bus $1 $2 $3 from $4";
				_mql.executeCommand( context,queryString,taskId,"rel",relAssginedTask,personId);
				if (_mql.getError() != null && !_mql.getError().equals( "" ) ) throw new Exception( _mql.getError() );
			} else if ( sChangeType.equals( "ASSIGNEE_PERCENT_CHANGED" ) ) {
				sRelPersonTaskId = (String) mChange.get( "PERSON_RELTASKID" );

				if ((sRelPersonTaskId != null ) && (!sRelPersonTaskId.equals("null")))
				{
					DomainRelationship relationship = new
							DomainRelationship((String)sRelPersonTaskId);

					String rel_allocation=(String)PropertyUtil.getSchemaProperty(context,"attribute_PercentAllocation");
					ContextUtil.pushContext(context);
					try
					{
						relationship.setAttributeValue(
								context,
								rel_allocation,
								(String) mChange.get( "PERSON_PERCENTALLOCATION" ));
					}
					finally
					{
						ContextUtil.popContext(context);
					}
				}
			}

		}

		//get the mail subject
		String mSubjectKey[] = {"ProjectName"};
		String mSubjectValue[] = {sProjectName};
		String sMailSubject = "emxProgramCentral.Common.AssignTaskSubject";
		sMailSubject  = emxProgramCentralUtilClass.getMessage(
				context, sMailSubject, mSubjectKey, mSubjectValue, companyName);

		//get the mail message
		MailUtil.setAgentName(context, context.getUser());

		java.util.Set keyValueSet = assignedPersonToTasks.entrySet();
		Iterator keyMapIter = keyValueSet.iterator();
		while (keyMapIter.hasNext())
		{
			Map.Entry me = (Map.Entry) keyMapIter.next();
			String personId = (String) me.getKey();
			List<String> values = (List<String>) me.getValue();
			objectIdList.clear();
			sFinalMailMessage = DomainObject.EMPTY_STRING;
			// Set the "to" list.
			DomainObject personObject = DomainObject.newInstance(context, personId);
			String personName = (String) personObject.getName(context);
			mailToList.clear();
			mailToList.addElement(personName);
			for(int i= 0; i < values.size(); i++) {
				DomainObject dom = DomainObject.newInstance(context, values.get(i));
				objectIdList.addElement(values.get(i));
				StringList sList = new StringList(3);
				sList.addElement(DomainConstants.SELECT_NAME);
				sList.add(SELECT_TASK_ESTIMATED_START_DATE);
				sList.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
				sList.add(SELECT_CRITICAL_TASK);
				sList.add("to[" + RELATIONSHIP_SUBTASK + "].from.name");

				Map taskMap = (Map)dom.getInfo(context,sList);
				taskName = (String)taskMap.get(DomainConstants.SELECT_NAME);
				Date startD = eMatrixDateFormat.getJavaDate((String)taskMap.get(SELECT_TASK_ESTIMATED_START_DATE));
				taskEstStartDate = date.format(startD);

				startD = eMatrixDateFormat.getJavaDate((String)taskMap.get(SELECT_TASK_ESTIMATED_FINISH_DATE));
				taskEstFinishDate = date.format(startD);

				Map mapProject = task.getProject(context, new StringList(SELECT_NAME));
				sProjectName = (String)mapProject.get(SELECT_NAME);

				taskCriticality  = (String)taskMap.get(SELECT_CRITICAL_TASK);

				if ("TRUE".equals(taskCriticality))
				{
					sMailMessage = "emxProgramCentral.Common.AssignCriticalTaskMessage";
				}
				else
				{
					sMailMessage = "emxProgramCentral.Common.AssignTaskMessage";
				}

				String mValue[] = {taskName, taskEstStartDate, taskEstFinishDate};
				sTempMailMessage  = emxProgramCentralUtilClass.getMessage(
						context, sMailMessage, mKey, mValue, companyName);
				sFinalMailMessage += "  " + sTempMailMessage + "\n";
			}
			// Sends mail notification
			MailUtil.sendMessage(context, mailToList, mailCcList, null,
					sMailSubject, sFinalMailMessage, objectIdList);
		}

		_mql.close(context);

	}
	/**
	 * Where : In the Structure Browser, Edit mode of State column
	 * How : Get the objectId from argument map and get all states through "SELECT_STATES".
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "paramMap"
	 *        paramMap holds the following input arguments:
	 *          0 - String containing "objectId"
	 * @returns StringList
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public StringList getStates(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map mpParamMap = (HashMap) programMap.get("paramMap");
		String strObjectId = (String) mpParamMap.get("objectId");

		StringList stateList = new StringList();
		DomainObject dom = DomainObject.newInstance(context);
		StringList sList = new StringList(3);
		sList.addElement(DomainConstants.SELECT_STATES);
		sList.addElement(DomainConstants.SELECT_TYPE);
		sList.addElement(DomainConstants.SELECT_ID);

		dom.setId(strObjectId);
		Map map = (Map)dom.getInfo(context,sList);
		String sType = (String)map.get(DomainConstants.SELECT_TYPE);
		stateList = (StringList)map.get(DomainConstants.SELECT_STATES);

		if(sType.equals(DomainConstants.TYPE_PROJECT_SPACE)){
			stateList.remove(DomainConstants.STATE_PROJECT_SPACE_ARCHIVE);
		}else {
			String relType = DomainConstants.RELATIONSHIP_SUBTASK+","+DomainConstants.RELATIONSHIP_DELETED_SUBTASK;
			for(;;){
				Map childMap = (Map) dom.getRelatedObject(context,
						relType,
						true,
						sList,
						null);
				if(childMap!=null && childMap.size()!=0) {
					if(((String)childMap.get(DomainConstants.SELECT_TYPE)).equals(DomainConstants.TYPE_TASK)){
						dom.setId((String)childMap.get(DomainConstants.SELECT_ID));
						StringList subStateList = (StringList)dom.getInfoList(context,DomainConstants.SELECT_STATES);
						// Task Review to be removed since Project Concept having Review state
						subStateList.remove((String)DomainConstants.STATE_PROJECT_SPACE_REVIEW);
						stateList.addAll(subStateList);
						break;
					} else {
						dom.setId((String)childMap.get(DomainConstants.SELECT_ID));
					}
				}else {
					break;
				}

			}
		}

		return stateList;
	}

	/**
	 * Where : In the Structure Browser, Edit mode of State column
	 * How : Get the objectId from argument map and get all states through "SELECT_STATES".
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "paramMap"
	 *        paramMap holds the following input arguments:
	 *          0 - String containing "objectId"
	 * @returns StringList
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public HashMap getAvailableStates(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");
		String languageStr = (String) programMap.get("languageStr");
		HashMap hashMap = new HashMap(2);
		StringList stateList = new StringList();
		StringList i18nStatesList = new StringList();

		DomainObject dom = DomainObject.newInstance(context);
		StringList sList = new StringList(3);
		sList.addElement(DomainConstants.SELECT_STATES);
		sList.addElement(DomainConstants.SELECT_TYPE);
		sList.addElement(DomainConstants.SELECT_ID);
		sList.addElement(DomainConstants.SELECT_POLICY);
		dom.setId(strObjectId);
		Map map = (Map)dom.getInfo(context,sList);
		String sType = (String)map.get(DomainConstants.SELECT_TYPE);
		stateList = (StringList)map.get(DomainConstants.SELECT_STATES);
		String sPolicy = (String)map.get(DomainConstants.SELECT_POLICY);
		for(int i=0;i<stateList.size();i++){
			i18nStatesList.addElement(i18nNow.getStateI18NString(sPolicy,(String)stateList.get(i),languageStr));
		}
		//i18nStatesList = i18nNow.getAdminI18NStringList("state",stateList,languageStr);
		hashMap.put("field_choices", stateList);
		hashMap.put("field_display_choices", i18nStatesList);
		return hashMap;
	}



	/**
	 * Where : In the Structure Browser, TableMenu - > WBS Tasks
	 * How : Get the objectId from argument map and extract the objects
	 *          with "Subtask" relationship through getWBSTasks
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "paramMap"
	 *        paramMap holds the following input arguments:
	 *          0 - String containing "objectId"
	 * @returns MapList
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWBSSubtasks(Context context, String[] args) throws Exception
	{
		HashMap arguMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String) arguMap.get("objectId");
		String strExpandLevel = (String) arguMap.get("expandLevel");
		String selectedProgram = (String) arguMap.get("selectedProgram");
		String selectedTable = (String) arguMap.get("selectedTable");
		//Added:10-June-2010:vf2:R210 PRG:IR-056503
		String effortFilter = (String) arguMap.get("PMCWBSEffortFilter");
		//End:10-June-2010:vf2:R210 PRG:IR-056503
		MapList mapList = new MapList();

		DomainObject domobjTask = DomainObject.newInstance(context,strObjectId);
		short nExpandLevel =  ProgramCentralUtil.getExpandLevel(strExpandLevel);
		if(domobjTask.isKindOf(context, TYPE_PROJECT_MANAGEMENT) && selectedTable.equalsIgnoreCase("PMCProjectTaskEffort")){
			String[] arrJPOArguments = new String[3];
			HashMap programMap = new HashMap();
			programMap.put("objectId", strObjectId);
			programMap.put("ExpandLevel", strExpandLevel);
			//Added:10-June-2010:vf2:R210 PRG:IR-056503
			if(!"null".equals(effortFilter) && null!= effortFilter && !"".equals(effortFilter)) {
				programMap.put("effortFilter", effortFilter);
			}
			//End:10-June-2010:vf2:R210 PRG:IR-056503
			arrJPOArguments = JPO.packArgs(programMap);
			mapList = (MapList)JPO.invoke(context,
					"emxEffortManagementBase", null, "getProjectTaskList",
					arrJPOArguments, MapList.class);
		}
		else
		{
			mapList = (MapList) getWBSTasks(context,strObjectId,DomainConstants.RELATIONSHIP_SUBTASK,nExpandLevel);
		}

		//Added:nr2:PRG:R211:IR-072682V6R2012
		HashMap hmTemp = new HashMap();
		hmTemp.put("expandMultiLevelsJPO","true");
		mapList.add(hmTemp);
		//End::nr2:PRG:R211:IR-072682V6R2012
		boolean isAnDInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionAerospaceProgramManagementAccelerator",false,null,null);
		if(isAnDInstalled){
			boolean isLocked = Task.isParentProjectLocked(context, strObjectId);
	 		if(isLocked){
				for(Object tempMap : mapList){
					((Map)tempMap).put("disableSelection", "true"); 
					((Map)tempMap).put("RowEditable", "readonly");
				}
			}
 		}
		return mapList;
	}

	/**
	 * Where : In the Structure Browser, TableMenu - > WBS Deleted Tasks
	 * How : Get the objectId from argument map and extract the objects
	 *          with "Deleted Subtask" relationship through getDeletedTasks
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "paramMap"
	 *        paramMap holds the following input arguments:
	 *          0 - String containing "objectId"
	 * @returns MapList
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWBSDeletedSubtasks(Context context, String[] args) throws Exception {

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		MapList resultList = new MapList();

		HashMap arguMap = (HashMap)JPO.unpackArgs(args);
		String selectedTable = (String) arguMap.get("selectedTable");
		String strDirection = "from";
		String strObjectId = (String) arguMap.get("objectId");
		task.setId(strObjectId);
		//Added:nr2:PRG:R211:23 Oct 2010:IR-072682V6R2012
		String strExpandLevel = (String) arguMap.get("expandLevel");

		//End:nr2:PRG:R211:23 Oct 2010:IR-072682V6R2012
		StringList objectSelects = new StringList(2);
		StringList relationshipSelects = new StringList(4);

		objectSelects.addElement(DomainConstants.SELECT_ID);
		objectSelects.addElement(DomainConstants.SELECT_NAME);
		relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
		relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
		relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
		relationshipSelects.addElement(DomainConstants.KEY_LEVEL);
		//Added:3-Feb-09:yox:R207:PRG : Bug :366917
        MapList wbsDeletedTasks = new MapList();

		//Added:nr2:PRG:R211:23 Oct 2010:IR-072682V6R2012
		short recursionLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
		//End:nr2:PRG:R211:23 Oct 2010:IR-072682V6R2012
		//short recursionLevel = (short)1;

		//Modified:25-May-10:rg6:R210:PRG Bug :IR-052879V6R2011x
		String relPattern = DomainConstants.RELATIONSHIP_DELETED_SUBTASK;
		//End:25-May-10:rg6:R210:PRG Bug :IR-052879V6R2011x
		String typePattern= DomainConstants.TYPE_TASK_MANAGEMENT;

		//Modified:24-Mar-09:QZV:R207:ECH:Bug:371011 - equals check is replaced with isKindOf
		//Modified:6-Oct-2010:R211:RG6:IR-072844V6R2012
		if(task.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)||task.isKindOf(context, DomainConstants.TYPE_PROJECT_CONCEPT)){
			//End:R207:ECH:Bug:371011
			//End Modified:6-Oct-2010:R211:RG6:IR-072844V6R2012
			wbsDeletedTasks = task.getDeletedTasks(context, objectSelects, relationshipSelects);
		}else{
			if(selectedTable.equalsIgnoreCase("PMCProjectTaskEffort")){
				String[] arrJPOArguments = new String[1];
				HashMap programMap = new HashMap();
				programMap.put("objectId", strObjectId);
				arrJPOArguments = JPO.packArgs(programMap);
				wbsDeletedTasks = (MapList)JPO.invoke(context,
						"emxEffortManagementBase", null, "getProjectTaskList",
						arrJPOArguments, MapList.class);
			}
			else {
           //below code is commented to block the task expand on deleted task view. because subtask are already present there in single level.
/*                wbsDeletedTasks = task.getRelatedObjects(   context,
						relPattern,
						typePattern,
						false,
						true,
						recursionLevel,
						objectSelects,
						relationshipSelects,
						"",
						"",
						"",
						"",
                        null) ;*/
			}
		}
		//End:R207:PRG :Bug :366917
		Iterator iterator = wbsDeletedTasks.iterator();
		while(iterator.hasNext()){
			Hashtable htable = (Hashtable) iterator.next();
			htable.put("RowEditable","readonly");
            htable.put("hasChildren","false");
			htable.put("selection","none");
			htable.remove("level");//IR-203180V6R2014 and IR-203181V6R2014
			htable.put("direction",strDirection);
		}
		boolean isAnDInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionAerospaceProgramManagementAccelerator",false,null,null);
		if(isAnDInstalled){
			boolean isLocked = Task.isParentProjectLocked(context, strObjectId);
	 		if(isLocked){
				for(Object tempMap : wbsDeletedTasks){
					((Map)tempMap).put("disableSelection", "true"); 
					((Map)tempMap).put("RowEditable", "readonly");
				}
			}
		}

		//Modified:08-Dec-10:s4e:R211:PRG:IR-079209V6R2012
		//Commented below code,Was giving error because deleted task is attached at level 1 with project not in hirarchy.
		/*      //Added:nr2:PRG:R211:IR-072682V6R2012
        HashMap hmTemp = new HashMap();
        hmTemp.put("expandMultiLevelsJPO","true");
        wbsDeletedTasks.add(hmTemp);
        //End::nr2:PRG:R211:IR-072682V6R2012
		 */
		//End:Modified:08-Dec-10:s4e:R211:PRG:IR-079209V6R2012
		return  wbsDeletedTasks;
	}

	/**
	 * Where : In the Structure Browser, TableMenu - > All Tasks
	 * How : Get the objectId from argument map and extract the objects
	 *          with "Deleted Subtask" & "Subtask" relationship.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "paramMap"
	 *        paramMap holds the following input arguments:
	 *          0 - String containing "objectId"
	 * @returns MapList
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */


	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWBSAllSubtasks(Context context, String[] args) throws Exception {
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		HashMap arguMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String) arguMap.get("objectId");
		String selectedTable = (String) arguMap.get("selectedTable");
		MapList wbsSubTasks = new MapList();
		//
		String strExpandLevel = (String) arguMap.get("expandLevel");

		short nExpandLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
		//
		String strDirection = "from";
		task.setId(strObjectId);

		StringList objectSelects = new StringList(2);
		StringList relationshipSelects = new StringList(4);

		objectSelects.addElement(DomainConstants.SELECT_ID);
		objectSelects.addElement(DomainConstants.SELECT_NAME);
		relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
		relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
		relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
		relationshipSelects.addElement(DomainConstants.SELECT_LEVEL);
		if(task.isKindOf(context, TYPE_TASK_MANAGEMENT) && selectedTable.equalsIgnoreCase("PMCProjectTaskEffort")){
			String[] arrJPOArguments = new String[1];
			HashMap programMap = new HashMap();
			programMap.put("objectId", strObjectId);
			arrJPOArguments = JPO.packArgs(programMap);
			wbsSubTasks = (MapList)JPO.invoke(context,
					"emxEffortManagementBase", null, "getProjectTaskList",
					arrJPOArguments, MapList.class);
		}//Modified:6-Oct-2010:R211:RG6:IR-072844V6R2012
		else if(task.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)||task.isKindOf(context, DomainConstants.TYPE_PROJECT_CONCEPT) || task.isKindOf(context, TYPE_TASK_MANAGEMENT)){
			//End Modified:6-Oct-2010:R211:RG6:IR-072844V6R2012
			wbsSubTasks = getWBSTasks(context,strObjectId,DomainConstants.RELATIONSHIP_SUBTASK+","+DomainConstants.RELATIONSHIP_DELETED_SUBTASK,nExpandLevel);// [MODIFIED::PRG:RG6:Feb 14, 2011:IR-089983V6R2012 :R211::]
			// [ADDED::PRG:RG6:Feb 14, 2011:IR-089983V6R2012 :R211::Start]
			// rel select rel_name is must be in selectable in order to below code to work
			if(null != wbsSubTasks)
			{
				Iterator it = wbsSubTasks.iterator();
				while(it.hasNext())
				{
					Map mapSubTaskObj = (Map)it.next();
					if(null != mapSubTaskObj)
					{
						String sRelName = (String)mapSubTaskObj.get(DomainConstants.SELECT_RELATIONSHIP_NAME);
						if(ProgramCentralUtil.isNotNullString(sRelName))
						{
							// ReadOnly for Deleted Tasks
							if(DomainConstants.RELATIONSHIP_DELETED_SUBTASK.equalsIgnoreCase(sRelName))
							{
								mapSubTaskObj.put("RowEditable", "readonly");
								mapSubTaskObj.put("selection","none");
								mapSubTaskObj.put("direction",strDirection);
							}
						}
					}
				}
			}
			// [ADDED::PRG:RG6:Feb 14, 2011:IR-089983V6R2012 :R211::End]
		}

		//Added:nr2:PRG:R211:IR-072682V6R2012
		HashMap hmTemp = new HashMap();
		hmTemp.put("expandMultiLevelsJPO","true");
		wbsSubTasks.add(hmTemp);
		//End::nr2:PRG:R211:IR-072682V6R2012
		boolean isAnDInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionAerospaceProgramManagementAccelerator",false,null,null);
		if(isAnDInstalled){
			boolean isLocked = Task.isParentProjectLocked(context, strObjectId);
	 		
	 		if(isLocked){
				for(Object tempMap : wbsSubTasks){
					((Map)tempMap).put("disableSelection", "true"); 
					((Map)tempMap).put("RowEditable", "readonly");
				}
			}
 		}
		return wbsSubTasks;
	}


	/**
	 * Where : Internal Private method
	 * How :  Depend on the objectId and Relationship, extracts the objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param String containing "objectId"
	 * @param String containing "relPattern"
	 * @returns MapList
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */


	private MapList getWBSTasks(Context context, String objectId, String relPattern) throws Exception
	{
		return getWBSTasks(context,objectId, relPattern,(short)1);
	}

	/**
	 * Where : In the Structure Browser, display current state at PMCWBSViewTable table
	 * How : gets the current state of the Project Space & Task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "paramMap"
	 *        paramMap holds the following input arguments:
	 *          0 - String containing "objectId"
	 * @returns MapList
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public Vector getStateName(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		String exportFormat = (String)paramList.get("exportFormat");
		String sState = "";
		String sPolicy = "";
		String toolTip = "";
		Vector columnValues = new Vector();
		String languageStr = (String) paramList.get("languageStr");

		try {
			final String STRING_DELETED = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.Common.Deleted", languageStr);

			// Find all the required infomration on each of the tasks here
			String[] strObjectIds = new String[objectList.size()];
			int size = objectList.size();
			for (int i = 0; i < size; i++) {
				Map mapObject = (Map) objectList.get(i);
				String taskId = (String) mapObject.get(DomainObject.SELECT_ID);
				strObjectIds[i] = taskId;
			}

			StringList slBusSelect = new StringList();
			slBusSelect.add(DomainConstants.SELECT_ID);
			slBusSelect.add(DomainConstants.SELECT_CURRENT);
			slBusSelect.add(DomainConstants.SELECT_POLICY);
			slBusSelect.add(SELECT_IS_DELETED_SUBTASK);

			Map mapTaskInfo = new LinkedHashMap();
			BusinessObjectWithSelectList objectWithSelectList = DomainObject.getSelectBusinessObjectData(context, strObjectIds, slBusSelect);
			for (BusinessObjectWithSelectItr objectWithSelectItr = new BusinessObjectWithSelectItr(objectWithSelectList); objectWithSelectItr.next();) {
				BusinessObjectWithSelect objectWithSelect = objectWithSelectItr.obj();

				Map mapTask = new HashMap();
				for (Iterator itrSelectables = slBusSelect.iterator(); itrSelectables.hasNext();) {
					String strSelectable = (String)itrSelectables.next();
					mapTask.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
				}

				mapTaskInfo.put(objectWithSelect.getSelectData(SELECT_ID), mapTask);
			}
			/*
            Iterator objectListIterator = objectList.iterator();
            while (objectListIterator.hasNext())
            {
                Map objectMap = (Map) objectListIterator.next();
                String taskId = (String) objectMap.get(DomainObject.SELECT_ID);

			 */

			Iterator objectIdItr = mapTaskInfo.keySet().iterator();
			while(objectIdItr.hasNext()){

				String taskId = (String)objectIdItr.next();

				Map objectInfo = (Map)mapTaskInfo.get(taskId);

				if (objectInfo!=null) {
					sState = XSSUtil.encodeForHTML(context,(String)objectInfo.get(DomainConstants.SELECT_CURRENT));
					sPolicy = (String)objectInfo.get(DomainConstants.SELECT_POLICY);

					if("TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_IS_DELETED_SUBTASK))) {
						toolTip = STRING_DELETED;
						if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
							sState = toolTip;
						}
						else {
							sState = "<font color=\"red\">"+toolTip+"</font>";
						}
					} else {
						toolTip = i18nNow.getStateI18NString(sPolicy,sState, languageStr);
						sState = toolTip;
					}
					if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
						columnValues.add(sState);
					}
					else {
						columnValues.add("<label title=\""+toolTip+"\">"+sState+"</label>");
					}
				}
			}
		} catch(Exception e) {
			System.out.println("Exception at getStateName "+e);
			throw e;
		} finally {
			return columnValues;
		}
	}


	/**
	 * gets the persons connected to the Task
	 * Used to display current state at PMCWBSViewTable table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - ProjectSpace/Task id
	 * @returns Object (Vector)
	 * @throws Exception if the operation fails
	 * @since Program Central X+2
	 * @grade 0
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMemberTasks(Context context, String args[]) throws Exception{

		// Check license while listing member tasks, if license check fails here
		// the task will not be listed.
		//
		ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_PRG_LICENSE_ARRAY);

		MapList mapList = new MapList();
		MapList resultList = new MapList();
		com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

		try {

			HashMap arguMap = (HashMap)JPO.unpackArgs(args);
			String rootNode = (String) arguMap.get("objectId");
			String selectedTable = (String) arguMap.get("selectedTable");
			String roolLabel = (String) arguMap.get("RootLabel");
			String projectId = (String) arguMap.get("projectID");
			String strExpandLevel = (String) arguMap.get("expandLevel");

			//String cmd = "print type \"" + DomainConstants.TYPE_TASK_MANAGEMENT + "\" select derivative dump ,";
			String cmd = "print type $1 select $2 dump $3";
			String taskSubtypes = MqlUtil.mqlCommand(context,cmd,DomainConstants.TYPE_TASK_MANAGEMENT,"derivative",",");

			StringList rootNodeSelects = new StringList(3);
			DomainObject rootNodeObj = DomainObject.newInstance(context, rootNode);

			rootNodeSelects.addElement(DomainConstants.SELECT_TYPE);
			rootNodeSelects.addElement(DomainConstants.SELECT_NAME);
			rootNodeSelects.addElement(DomainConstants.SELECT_REVISION);

			rootNodeObj.open(context);
			Map rootNodeTNR = rootNodeObj.getInfo(context,rootNodeSelects);
			rootNodeObj.close(context);

			String rootNodeType = (String) rootNodeTNR.get(DomainConstants.SELECT_TYPE);

			String relPattern = null;
			String typePattern=null;
			int recursionLevel = 1;
			boolean getTo = false;
			boolean getFrom = false;
			String busWhereClause = null;
			String relWhereClause =null;
			String postRelPattern = null;
			String postTypePattern = null;
			Map postPatterns = null;
			Map taskIndexMap = null;
			boolean blProjectType = false;

			DomainObject prjObject = DomainObject.newInstance(context);
			// Object and Relationship selects

			StringList objectSelects = new StringList(4);
			StringList relationshipSelects = new StringList(4);

			objectSelects.addElement(DomainConstants.SELECT_ID);
			//objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(person.SELECT_NAME);
			objectSelects.addElement(person.SELECT_FIRST_NAME);
			objectSelects.addElement(person.SELECT_LAST_NAME);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
			relationshipSelects.addElement(DomainConstants.KEY_LEVEL);

			getFrom = true;
			if((rootNodeObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)|| rootNodeType.equals(DomainConstants.TYPE_PROJECT_CONCEPT)) &&
					("All".equals(strExpandLevel))) {
				relPattern = DomainConstants.RELATIONSHIP_MEMBER + "," + DomainConstants.RELATIONSHIP_ASSIGNED_TASKS;
				typePattern = DomainConstants.TYPE_PERSON + "," + taskSubtypes;
				objectSelects.addElement(SELECT_TASK_PROJECT_ID);
				recursionLevel = 0;
			}
			//Modified - isKindOf replaced equals:27-Feb-09:QZV:R207:ECH:Bug:369860
			//if(rootNodeObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)|| rootNodeType.equals(DomainConstants.TYPE_PROJECT_CONCEPT)) {
			if((rootNodeObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)|| rootNodeType.equals(DomainConstants.TYPE_PROJECT_CONCEPT)) &&
					(!"All".equals(strExpandLevel))) {
				//End:R207:ECH:Bug:369860
				relPattern = DomainConstants.RELATIONSHIP_MEMBER;
				typePattern = DomainConstants.TYPE_PERSON;
				blProjectType = true;

			} else if(rootNodeType.equals(DomainConstants.TYPE_PERSON)){
				relPattern = DomainConstants.RELATIONSHIP_ASSIGNED_TASKS;
				//typePattern = DomainConstants.TYPE_TASK;
				typePattern = taskSubtypes;

				if(projectId != null){
					// Project Space getting ProjectID in request map.
					prjObject.setId(projectId);
				}
				else if((String) arguMap.get("parentOID") != null){
					prjObject.setId((String) arguMap.get("parentOID"));
				} else {
					MapList conceptList = rootNodeObj.getRelatedObjects(context,
							RELATIONSHIP_MEMBER,
							DomainConstants.TYPE_PROJECT_CONCEPT,
							rootNodeSelects,
							null, // relationshipSelects
							true,      // getTo
							false,       // getFrom
							(short) 1,  // recurseToLevel
							null,// objectWhere
							null);
					boolean found =false;
					if(conceptList != null && conceptList.size()>0){
						for(int i=0;i<conceptList.size();i++){
							Map conceptMap = (Map)conceptList.get(i);
							String conceptId = (String)conceptMap.get(DomainConstants.SELECT_ID);
							String conceptName = (String)conceptMap.get(DomainConstants.SELECT_NAME);
							if(roolLabel.equalsIgnoreCase(conceptName)){
								prjObject.setId(conceptId);
								found = true;
								break;
							}
						}
					}
					if(!found){
						new FrameworkException("Root Project Concept Not Found");
					}
				} // Else ProjectId equal null
				MapList taskList = prjObject.getRelatedObjects(context,
						RELATIONSHIP_SUBTASK,
						taskSubtypes,
						objectSelects,
						relationshipSelects, // relationshipSelects
						false,      // getTo
						true,       // getFrom
						(short) 0,  // recurseToLevel
						null,// objectWhere
						null);      // relationshipWhere


				taskIndexMap = new HashMap(taskList.size());
				int counter =0;
				if(taskList != null){
					for(int i=0;i<taskList.size();i++){
						Map projMap = (Map)taskList.get(i);
						String id = (String)projMap.get(DomainConstants.SELECT_ID);
						taskIndexMap.put(id, String.valueOf(counter++));
					}
				}
			}
			if(rootNodeObj.isKindOf(context, TYPE_TASK_MANAGEMENT) && selectedTable.equalsIgnoreCase("PMCProjectTaskEffort")){
				String[] arrJPOArguments = new String[1];
				HashMap programMap = new HashMap();
				programMap.put("objectId", rootNode);
				arrJPOArguments = JPO.packArgs(programMap);
				mapList = (MapList)JPO.invoke(context,
						"emxEffortManagementBase", null, "getProjectTaskList",
						arrJPOArguments, MapList.class);
			}
			//Added:29-Oct-2010:vf2:R211 PRG:IR-077147
			if(!(rootNodeObj.isKindOf(context, TYPE_TASK_MANAGEMENT) && selectedTable.equalsIgnoreCase("PMCProjectTaskEffort"))){
				//End:29-Oct-2010:vf2:R211 PRG:IR-077147
				mapList = rootNodeObj.getRelatedObjects(context,
						relPattern,
						typePattern,
						getTo,
						getFrom,
						recursionLevel,
						objectSelects,
						relationshipSelects,
						busWhereClause,
						relWhereClause,
						postRelPattern,
						postTypePattern,
						postPatterns) ;
				Iterator iterator = mapList.iterator();
				if("All".equals(strExpandLevel)){
					while(iterator.hasNext()){
						Map htable = (Map) iterator.next();
						String strProjId = (String)htable.get(SELECT_TASK_PROJECT_ID);
						String strRelnTypeObj =  (String)htable.get(DomainConstants.SELECT_RELATIONSHIP_TYPE);
						if(RELATIONSHIP_MEMBER.equals(strRelnTypeObj) || rootNode.equals(strProjId)){
							htable.put("RowEditable","readonly");
						} else {
							iterator.remove();
						}
					}
				}
				else{ // if Expand All is not clicked
					while(iterator.hasNext()){
						Map htable = (Map) iterator.next();
						String strId = (String)htable.get("id");
						if(blProjectType || taskIndexMap.get(strId)!=null){
							htable.put("RowEditable","readonly");
						} else {
							iterator.remove();
						}
					}
				}
			}
			boolean isAnDInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionAerospaceProgramManagementAccelerator",false,null,null);
			if(isAnDInstalled){
				boolean isLocked = Task.isParentProjectLocked(context, rootNode);
		 		if(isLocked){
					for(Object tempMap : mapList){
						((Map)tempMap).put("disableSelection", "true"); 
						((Map)tempMap).put("RowEditable", "readonly");
					}
				}
	 		}
		}
		catch(Exception e) {
			throw e;
		}
		finally{
			return mapList;
		}
	}

	/**
	 * gets the Project Members
	 * 1. Get all the Projects and keep it in to HashSet (To remove Duplicates)
	 * 2. For each project get the Members
	 * Used for WBS Structure Browser Column Owner
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns StringList
	 * @throws Exception if the operation fails
	 * @since Program Central X+2
	 * @grade 0
	 */

	public HashMap getProjectMembers(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		//Map mpParamMap = (HashMap) programMap.get("paramMap");
		String strObjectId = (String) programMap.get("objectId");
		HashMap tempMap = new HashMap();

		com.matrixone.apps.common.MemberRelationship member = (com.matrixone.apps.common.MemberRelationship) DomainRelationship.newInstance(context, DomainConstants.RELATIONSHIP_MEMBER);
		com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE,"PROGRAM");

		try {
			DomainObject dom = DomainObject.newInstance(context);

			StringList typeSelects = new StringList(6);
			typeSelects.addElement(DomainConstants.SELECT_ID);
			typeSelects.addElement(DomainConstants.SELECT_TYPE);
			typeSelects.addElement(DomainConstants.SELECT_NAME);
			typeSelects.addElement("to[" + DomainConstants.RELATIONSHIP_SUBTASK + "].from.id");
			typeSelects.addElement("to[" + DomainConstants.RELATIONSHIP_SUBTASK + "].from.name");
			typeSelects.addElement("to[" + DomainConstants.RELATIONSHIP_SUBTASK + "].from.type");

			dom.setId(strObjectId);
			String strType = (String)dom.getInfo(context,DomainConstants.SELECT_TYPE);
			String strTaskProjectRole = null;
			String strProjectId = null;
			//String cmd = "print type \"" + DomainConstants.TYPE_TASK_MANAGEMENT + "\" select derivative dump ,";
			String cmd = "print type $1 select $2 dump $3";
			String taskSubtypes = MqlUtil.mqlCommand(context,cmd,DomainConstants.TYPE_TASK_MANAGEMENT,"derivative",",");
			if(taskSubtypes.indexOf(strType)>=0){
				Map mainMap;
				dom.setId(strObjectId);
				strTaskProjectRole = (String)dom.getInfo(context,"attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
				MapList mainList = dom.getRelatedObjects(context,
						DomainConstants.RELATIONSHIP_SUBTASK,
						taskSubtypes,
						typeSelects,
						null,       // relationshipSelects
						true,      // getTo
						false,       // getFrom
						(short) 0,  // recurseToLevel
						null,// objectWhere
						null);      // relationshipWhere
				if(mainList != null && mainList.size() >0){
					mainMap= (Map) mainList.get(mainList.size() -1);
				}else{
					mainMap = (Map) dom.getInfo(context,typeSelects);
				}
				strProjectId = (String) mainMap.get("to[" + DomainConstants.RELATIONSHIP_SUBTASK + "].from.id");
			} else {
				strProjectId = strObjectId;
			}

			// Retrieve the poject's member list.
			StringList busSelects = new StringList(6);
			busSelects.add(person.SELECT_ID);
			busSelects.add(person.SELECT_TYPE);
			busSelects.add(person.SELECT_NAME);
			busSelects.add(person.SELECT_FIRST_NAME);
			busSelects.add(person.SELECT_COMPANY_NAME);
			busSelects.add(person.SELECT_LAST_NAME);

			StringList relSelects = new StringList(2);
			relSelects.add(member.SELECT_PROJECT_ROLE);
			relSelects.add(member.SELECT_PROJECT_ACCESS);

			StringList memberList = new StringList();
			StringList personList = new StringList();


			project.setId(strProjectId);
			String projectVisibility = project.getInfo(context,project.SELECT_PROJECT_VISIBILITY);

			MapList membersList = null;
			String relWhere = null;
			if(strTaskProjectRole != null && !"".equalsIgnoreCase(strTaskProjectRole)){
				relWhere = "attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"] == \""+strTaskProjectRole+"\"";
			}
			if (projectVisibility.equalsIgnoreCase("Members")) {
				membersList = project.getMembers(context,busSelects,relSelects, null, relWhere);
			} else {
				membersList = project.getMembers(context,busSelects,relSelects, null, relWhere, false, true);
			}
			Iterator itr = membersList.iterator();

			while (itr.hasNext()) {
				Map mapPersonName = (Map) itr.next();
				String fullName = (String) mapPersonName.get(person.SELECT_LAST_NAME) + ", " + (String) mapPersonName.get(person.SELECT_FIRST_NAME);
				if(!memberList.contains((String)fullName)){
					memberList.add(fullName);
					personList.add((String) mapPersonName.get(person.SELECT_ID));
				}
			}
			tempMap.put("field_choices", personList);
			tempMap.put("field_display_choices", memberList);
		} catch(Exception e){
			System.out.println("Exception at getProject Members"+e.getMessage());
			e.printStackTrace();
		}
		return tempMap;
	}


	/**
	 * gets the Immediate Parent Project Map
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central X+2
	 * @grade 0
	 */

	// private HashMap getParentProjectMap(Context context,String objectId) throws Exception {
	private HashMap getParentProjectMap(Context context,DomainObject dom) throws Exception {
		String relType = DomainConstants.RELATIONSHIP_SUBTASK;
		//String cmd = "print type \"" + DomainConstants.TYPE_TASK_MANAGEMENT + "\" select derivative dump ,";
		String cmd = "print type $1 select $2 dump $3";
		String taskSubtypes = MqlUtil.mqlCommand(context,cmd,DomainConstants.TYPE_TASK_MANAGEMENT,"derivative",",");
		StringList sList = new StringList(5);
		sList.addElement(SELECT_PARENT_ID);
		sList.addElement(SELECT_PARENT_NAME);
		sList.addElement(SELECT_PARENT_TYPE);
		sList.addElement(SELECT_PARENT_WBS);
		sList.addElement(SELECT_PARENT_SEQUENCE_ORDER);
		sList.addElement(DomainConstants.SELECT_ID);

		//  DomainObject dom = DomainObject.newInstance(context, objectId);
		MapList mainList = null;
		//[MODIFIED:PA4:27-Sept-2011:IR-119618V6R2013]
		ContextUtil.pushContext(context,
				PropertyUtil.getSchemaProperty(context,
						"person_UserAgent"),
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING);

		try{
			mainList = dom.getRelatedObjects(context,
					relType,
					taskSubtypes,
					sList,
					null,       // relationshipSelects
					true,      // getTo
					false,       // getFrom
					(short) 0,  // recurseToLevel
					null,// objectWhere
					null);      // relationshipWhere
		}finally{
			ContextUtil.popContext(context);
		}
		//[END:PA4:27-Sept-2011:IR-119618V6R2013]

		Map mainMap;
		if(mainList != null && mainList.size() >0){
			mainMap= (Map) mainList.get(mainList.size() -1);
		}else{
			mainMap = (Map) dom.getInfo(context,sList);
		}

		HashMap returnMap = new HashMap();
		String strId = null;
		String strName = null;
		String strType = null;
		if(mainMap.get(SELECT_PARENT_ID)== null){
			strId = (String) mainMap.get(DomainConstants.SELECT_ID);
			dom.setId(strId);
			strName = dom.getName();
			strType = dom.getType(context);
			returnMap.put(DomainConstants.SELECT_ID,strId);
			returnMap.put(DomainConstants.SELECT_NAME,strName);
			returnMap.put(DomainConstants.SELECT_TYPE,strType);
			returnMap.put(SELECT_PARENT_WBS,"0");
			returnMap.put(SELECT_PARENT_SEQUENCE_ORDER,"0");
		} else{
			strId = (String) mainMap.get(SELECT_PARENT_ID);
			strName =(String) mainMap.get(SELECT_PARENT_NAME);
			strType = (String) mainMap.get(SELECT_PARENT_TYPE);
			returnMap.put(DomainConstants.SELECT_ID,strId);
			returnMap.put(DomainConstants.SELECT_NAME,strName);
			returnMap.put(DomainConstants.SELECT_TYPE,strType);
			returnMap.put(SELECT_PARENT_WBS,(String) mainMap.get(SELECT_PARENT_WBS));
			returnMap.put(SELECT_PARENT_SEQUENCE_ORDER,(String) mainMap.get(SELECT_PARENT_SEQUENCE_ORDER));
		}
		return returnMap;
	}

	static protected StringList getSelectableValues(BusinessObjectWithSelect bws, String selectable, StringList listObject)
	{
		char cFieldSep = 0x07;
		String value = (String) bws.getSelectData(selectable);
		String[] temp = value.split(String.valueOf(cFieldSep));
		if (listObject == null)
			listObject = new StringList(1);
		listObject.clear();
		for(int i=0; i < temp.length ; i++)
		{
			listObject.add(temp[i]);
		}
		return listObject;
	}

	/**
	 * gets the Task Dependency details for the column
	 * Used for WBS Structure Browser
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central X+2
	 * @grade 0
	 */

	public Object getTaskDependencyColumn(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");

		Map paramList = (Map) programMap.get("paramList");
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null )
		{
			isPrinterFriendly = true;
		}

		//build an array of all the task ids from SB.
		String[] taskIds = new String[objectList.size()];
		for (int i=0; i < objectList.size(); i++)
		{
			Map map = (Map) objectList.get(i);
			String taskId = (String) map.get(DomainObject.SELECT_ID);
			taskIds[i] = taskId;
		}

		//selectables relative to task to build the dependency field.
		StringList selectables = new StringList(7);
		selectables.add(Task.SELECT_PREDECESSOR_TYPES);
		selectables.add(SELECT_PREDECESSOR_LAG_TIME_INPUT);
		selectables.add(SELECT_PREDECESSOR_LAG_TIME_UNITS);
		selectables.add(Task.SELECT_PREDECESSOR_IDS);
		selectables.add(SELECT_PROJECT_ACCESS_KEY_ID);
		selectables.add(SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR);
		selectables.add(SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER);

		//adk call to retrieve task dependency information for each task.
		BusinessObjectWithSelectList bwsl = new BusinessObjectWithSelectList();

		try{
			ProgramCentralUtil.pushUserContext(context);
			bwsl = BusinessObject.getSelectBusinessObjectData(context, taskIds, selectables);
		}finally{
			ProgramCentralUtil.popUserContext(context);
		}


		StringList prjSpaceSubTypes = getAllProjectSubTypeNames(context,TYPE_PROJECT_SPACE);
		StringList prjConceptSubTypes = getAllProjectSubTypeNames(context,TYPE_PROJECT_CONCEPT);
		StringList prjTemplateSubTypes = getAllProjectSubTypeNames(context,TYPE_PROJECT_TEMPLATE);

		Vector results = new Vector(objectList.size());

		StringList predecessorTypes = new StringList();
		StringList predecessorLagTimes = new StringList();
		StringList predecessorLagTimeUnits = new StringList();
		StringList predecessorIds = new StringList();
		StringList projectAccessKeyIds = new StringList();
		StringList projectAccessKeyIdsForPredecessor = new StringList();
		StringList predecessorTaskSequenceOrders = new StringList();

		Map palMapCache = new HashMap();
		String prjSpaceSubTypesHref = null;
		String prjConceptSubTypesHref = null;
		String prjTemplateSubTypesHref = null;

		StringBuffer value = new StringBuffer();
		StringBuffer toolTip = new StringBuffer();

		boolean isObjectInaccessible = false;
		for(int j=0; j < bwsl.size(); j++)
		{
			value.setLength(0);
			toolTip.setLength(0);
			BusinessObjectWithSelect bws = bwsl.getElement(j);
			predecessorIds = getSelectableValues(bws, Task.SELECT_PREDECESSOR_IDS, predecessorIds);
			if (predecessorIds.size() > 0 && !"".equals(((String)predecessorIds.get(0)).trim()))
			{
				predecessorTypes = getSelectableValues(bws, Task.SELECT_PREDECESSOR_TYPES, predecessorTypes);
				predecessorLagTimes = getSelectableValues(bws, SELECT_PREDECESSOR_LAG_TIME_INPUT, predecessorLagTimes);
				predecessorLagTimeUnits = getSelectableValues(bws, SELECT_PREDECESSOR_LAG_TIME_UNITS, predecessorLagTimeUnits);
				projectAccessKeyIds = getSelectableValues(bws, SELECT_PROJECT_ACCESS_KEY_ID, projectAccessKeyIds);
				projectAccessKeyIdsForPredecessor = getSelectableValues(bws, SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR, projectAccessKeyIdsForPredecessor);
				predecessorTaskSequenceOrders = getSelectableValues(bws, SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER, predecessorTaskSequenceOrders);
				MapList depedencyList = new MapList();

				for (int i=0; i < predecessorIds.size(); i++){
					Map dependencyMap  = new HashMap();

					String predecessorId = (String) predecessorIds.get(i);
					String predecessorType = (String) predecessorTypes.get(i);
					String predecessorLagTime = (String) predecessorLagTimes.get(i);
					String predecessorLagTimeUnit = (String) predecessorLagTimeUnits.get(i);
					String projectAccessKeyId = (String) projectAccessKeyIds.get(0);
					String projectAccessKeyIdForPredecessor = (String) projectAccessKeyIdsForPredecessor.get(i);
					String predecessorTaskSequenceOrder = (String) predecessorTaskSequenceOrders.get(i);

					if(ProgramCentralUtil.isNullString(predecessorTaskSequenceOrder)){
						// this should not be the case as every task should have a seq order unless a depend is againt a project.
						predecessorTaskSequenceOrder = "0";
					}

					dependencyMap.put("PredecessorId", predecessorId);
					dependencyMap.put("PredecessorType", predecessorType);
					dependencyMap.put("PredecessorLagType", predecessorLagTime);
					dependencyMap.put("PredecessorLagTypeUnit", predecessorLagTimeUnit);
					dependencyMap.put("ProjectAccessKeyIdPredecessor", projectAccessKeyIdForPredecessor);
					dependencyMap.put("PredecessorTaskSequenceId", predecessorTaskSequenceOrder);

					depedencyList.add(dependencyMap);
				}

				depedencyList.sort("PredecessorTaskSequenceId", ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);

				for (int i=0; i < depedencyList.size(); i++)
				{
					String projectName = null;
					String externalProjectState = EMPTY_STRING;

					Map dependencyMap = (Map)depedencyList.get(i);
					String predecessorId = (String) dependencyMap.get("PredecessorId");
					String predecessorType = (String) dependencyMap.get("PredecessorType");
					String predecessorLagTime = (String) dependencyMap.get("PredecessorLagType");
					String predecessorLagTimeUnit = (String) dependencyMap.get("PredecessorLagTypeUnit");
					String projectAccessKeyId = (String) projectAccessKeyIds.get(0);
					String projectAccessKeyIdForPredecessor = (String) dependencyMap.get("ProjectAccessKeyIdPredecessor");
					String predecessorTaskSequenceOrder = (String) dependencyMap.get("PredecessorTaskSequenceId");

					String predecessorProjectType = (String) palMapCache.get(projectAccessKeyIdForPredecessor);
					isObjectInaccessible = false;
					if (predecessorProjectType == null)
					{
						List<String> externalProjectSelectableList = new ArrayList<String>();
						externalProjectSelectableList.add(projectAccessKeyIdForPredecessor);
						externalProjectSelectableList.add("from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type");
						externalProjectSelectableList.add("from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name");
						externalProjectSelectableList.add("from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current");
						externalProjectSelectableList.add("|");

						//PRG:RG6:R213:Mql Injection:parameterized Mql:17-Oct-2011:start
						String sCommandStatement = "print bus $1 select $2 $3 $4 dump $5";
						String output =  MqlUtil.mqlCommand(context,sCommandStatement,externalProjectSelectableList);
						if(ProgramCentralUtil.isNullString(output)){
							isObjectInaccessible = true;
							try{
								ProgramCentralUtil.pushUserContext(context);
								output =  MqlUtil.mqlCommand(context,sCommandStatement,externalProjectSelectableList);
							}finally{
								ProgramCentralUtil.popUserContext(context);
							}
						}
						//PRG:RG6:R213:Mql Injection:parameterized Mql:17-Oct-2011:End
						StringList projectInfo = FrameworkUtil.split(output, "|");
						predecessorProjectType = (String) projectInfo.get(0);
						palMapCache.put(projectAccessKeyIdForPredecessor, predecessorProjectType);
						palMapCache.put("name:" + projectAccessKeyIdForPredecessor, projectInfo.get(1));
						palMapCache.put("current:"+ projectAccessKeyIdForPredecessor, projectInfo.get(2));
						palMapCache.put("isAccssible:"+ projectAccessKeyIdForPredecessor, isObjectInaccessible);
					}

					if (!projectAccessKeyId.equals(projectAccessKeyIdForPredecessor))
					{
						if("Experiment".equals(predecessorProjectType)){
							continue;
						}
						//dependent tasks are from different projects; prefix dependency label with project name.
						projectName = (String) palMapCache.get("name:" + projectAccessKeyIdForPredecessor);
						externalProjectState = (String)palMapCache.get("current:"+ projectAccessKeyIdForPredecessor);
						isObjectInaccessible = (boolean)palMapCache.get("isAccssible:"+ projectAccessKeyIdForPredecessor);
					}

					String href = "../programcentral/emxProgramCentralUtil.jsp?mode=showTask&amp;objectId=" + predecessorId + "&amp;palId=" + projectAccessKeyIdForPredecessor;

					if (i > 0)
					{
						//seperate multiple dependencies with comma.
						toolTip.append(",");
						value.append(",");
					}

					String tip = null;
					if (projectName != null)
					{
						tip = projectName + ":" + predecessorTaskSequenceOrder + ":" + predecessorType;
					}
					else
					{
						tip = predecessorTaskSequenceOrder + ":" + predecessorType;
					}
					tip += Task.parseToDouble(predecessorLagTime) < 0 ? "" : "+";
					tip += predecessorLagTime + " " + predecessorLagTimeUnit;

					if (ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(externalProjectState)
							|| ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(externalProjectState)
							|| isObjectInaccessible) {

						value.append(tip);
					} else {
						tip = XSSUtil.encodeForHTML(context, tip);
						value.append("<a target='listHidden' href='" +  href + "'>");
						value.append(tip);
						value.append("</a>");					
					}
					toolTip.append(tip);
				}
			}
			if (!"".equals(toolTip))
			{   //Modified:26-Aug-11:MS9:R212:PRG:IR-083967V6R2012x
				if(isPrinterFriendly && ("CSV".equals(strPrinterFriendly) || "HTML".equals(strPrinterFriendly)))
				{
					results.add(toolTip.toString());
				} else {
					value.insert(0,"<div title='" + toolTip.toString() + "'>");
					value.append("</div>");
					results.add(value.toString());
				}
			}
			else
			{
				results.add(value.toString());
			}
			//End:26-Aug-11:MS9:R212:PRG:IR-083967V6R2012x
		}
		return results;
	}


	/**
	 * This method returns successors of the task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central R418
	 */

	public Object getTaskSuccessorColumn(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");

		Map paramList = (Map) programMap.get("paramList");
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null )
		{
			isPrinterFriendly = true;
		}

		//build an array of all the task ids from SB.
		String[] taskIds = new String[objectList.size()];
		for (int i=0; i < objectList.size(); i++)
		{
			Map map = (Map) objectList.get(i);
			String taskId = (String) map.get(DomainObject.SELECT_ID);
			taskIds[i] = taskId;
		}
		//If the successor is project then following selectables are required to get the PAL id of successor project
		String SELECT_PROJECT_ACCESS_LIST_ID_FOR_SUCCESSOR = "to[" + RELATIONSHIP_DEPENDENCY + "].from.to[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.id";
		String IS_SUCCESSOR_KINDOF_PROJECT = "to["+RELATIONSHIP_DEPENDENCY+"].from.type.kindof["+DomainObject.TYPE_PROJECT_SPACE+"]";

		//selectables relative to task to build the dependency field.
		StringList selectables = new StringList(7);
		selectables.add(SELECT_SUCCESSOR_TYPES);
		selectables.add(SELECT_SUCCESSOR_LAG_TIME_INPUT);
		selectables.add(SELECT_SUCCESSOR_LAG_TIME_UNITS);
		selectables.add(SELECT_SUCCESSOR_IDS);
		selectables.add(SELECT_PROJECT_ACCESS_KEY_ID);
		selectables.add(SELECT_PROJECT_ACCESS_KEY_ID_FOR_SUCCESSOR);
		selectables.add(SELECT_PROJECT_ACCESS_LIST_ID_FOR_SUCCESSOR);
		selectables.add(SELECT_SUCCESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER);
		selectables.add(IS_SUCCESSOR_KINDOF_PROJECT);

		//adk call to retrieve task dependency information for each task.
		BusinessObjectWithSelectList bwsl = new BusinessObjectWithSelectList();

		try{
			ProgramCentralUtil.pushUserContext(context);
			bwsl = BusinessObject.getSelectBusinessObjectData(context, taskIds, selectables);
		}finally{
			ProgramCentralUtil.popUserContext(context);
		}


		StringList prjSpaceSubTypes = getAllProjectSubTypeNames(context,TYPE_PROJECT_SPACE);
		StringList prjConceptSubTypes = getAllProjectSubTypeNames(context,TYPE_PROJECT_CONCEPT);
		StringList prjTemplateSubTypes = getAllProjectSubTypeNames(context,TYPE_PROJECT_TEMPLATE);

		Vector results = new Vector(objectList.size());

		StringList successorTypes = new StringList();
		StringList successorLagTimes = new StringList();
		StringList successorLagTimeUnits = new StringList();
		StringList successorIds = new StringList();
		StringList projectAccessKeyIds = new StringList();
		StringList projectAccessKeyIdsForSuccessor = new StringList();
		StringList projectAccessListIdsForSuccessor = new StringList();
		StringList successorTaskSequenceOrders = new StringList();
		StringList successorObjectTypeList = new StringList();

		Map palMapCache = new HashMap();
		String prjSpaceSubTypesHref = null;
		String prjConceptSubTypesHref = null;
		String prjTemplateSubTypesHref = null;

		StringBuffer value = new StringBuffer();
		StringBuffer toolTip = new StringBuffer();

		boolean isObjectInaccessible = false;
		for(int j=0; j < bwsl.size(); j++)
		{
			value.setLength(0);
			toolTip.setLength(0);
			BusinessObjectWithSelect bws = bwsl.getElement(j);
			successorIds = getSelectableValues(bws, SELECT_SUCCESSOR_IDS, successorIds);
			if (successorIds.size() > 0 && !"".equals(((String)successorIds.get(0)).trim()))
			{
				successorTypes = getSelectableValues(bws, SELECT_SUCCESSOR_TYPES, successorTypes);
				successorLagTimes = getSelectableValues(bws, SELECT_SUCCESSOR_LAG_TIME_INPUT, successorLagTimes);
				successorLagTimeUnits = getSelectableValues(bws, SELECT_SUCCESSOR_LAG_TIME_UNITS, successorLagTimeUnits);
				projectAccessKeyIds = getSelectableValues(bws, SELECT_PROJECT_ACCESS_KEY_ID, projectAccessKeyIds);
				projectAccessKeyIdsForSuccessor = getSelectableValues(bws, SELECT_PROJECT_ACCESS_KEY_ID_FOR_SUCCESSOR, projectAccessKeyIdsForSuccessor);
				projectAccessListIdsForSuccessor = getSelectableValues(bws, SELECT_PROJECT_ACCESS_LIST_ID_FOR_SUCCESSOR, projectAccessListIdsForSuccessor);
				successorTaskSequenceOrders = getSelectableValues(bws, SELECT_SUCCESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER, successorTaskSequenceOrders);
				successorObjectTypeList = getSelectableValues(bws, IS_SUCCESSOR_KINDOF_PROJECT, successorObjectTypeList);
				MapList depedencyList = new MapList();

				int countProjectAccessKey = 0;
				int countProjectAccessList = 0;

				for (int i=0; i < successorIds.size(); i++){
					Map dependencyMap  = new HashMap();

					String successorId = (String) successorIds.get(i);
					String successorType = (String) successorTypes.get(i);
					String successorLagTime = (String) successorLagTimes.get(i);
					String successorLagTimeUnit = (String) successorLagTimeUnits.get(i);
					String projectAccessKeyId = (String) projectAccessKeyIds.get(0);
					String projectAccessKeyIdForSuccessor = (String) projectAccessKeyIdsForSuccessor.get(countProjectAccessKey);
					String successorTaskSequenceOrder = (String) successorTaskSequenceOrders.get(i);
					String successorIsProject = (String) successorObjectTypeList.get(i);

					if("TRUE".equalsIgnoreCase(successorIsProject)){
						// Project is successor of a task
						projectAccessKeyIdForSuccessor = (String) projectAccessListIdsForSuccessor.get(countProjectAccessList);
						countProjectAccessList++;
					} else {
						countProjectAccessKey++;
					}

					if(ProgramCentralUtil.isNullString(successorTaskSequenceOrder)){
						// this should not be the case as every task should have a seq order unless a depend is againt a project.
						successorTaskSequenceOrder = "0";
					}

					dependencyMap.put("SuccessorId", successorId);
					dependencyMap.put("SuccessorType", successorType);
					dependencyMap.put("SuccessorLagType", successorLagTime);
					dependencyMap.put("SuccessorLagTypeUnit", successorLagTimeUnit);
					dependencyMap.put("ProjectAccessKeyIdSuccessor", projectAccessKeyIdForSuccessor);
					dependencyMap.put("SuccessorTaskSequenceId", successorTaskSequenceOrder);

					depedencyList.add(dependencyMap);
				}

				depedencyList.sort("SuccessorTaskSequenceId", ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);

				for (int i=0; i < depedencyList.size(); i++)
				{
					String projectName = null;
					String externalProjectState = EMPTY_STRING;

					Map dependencyMap = (Map)depedencyList.get(i);
					String successorId = (String) dependencyMap.get("SuccessorId");
					String successorType = (String) dependencyMap.get("SuccessorType");
					String successorLagTime = (String) dependencyMap.get("SuccessorLagType");
					String successorLagTimeUnit = (String) dependencyMap.get("SuccessorLagTypeUnit");
					String projectAccessKeyId = (String) projectAccessKeyIds.get(0);
					String projectAccessKeyIdForSuccessor = (String) dependencyMap.get("ProjectAccessKeyIdSuccessor");
					String successorTaskSequenceOrder = (String) dependencyMap.get("SuccessorTaskSequenceId");

					String successorProjectType = (String) palMapCache.get(projectAccessKeyIdForSuccessor);
					isObjectInaccessible = false;
					if (successorProjectType == null)
					{
						List<String> externalProjectSelectableList = new ArrayList<String>();
						externalProjectSelectableList.add(projectAccessKeyIdForSuccessor);
						externalProjectSelectableList.add("from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type");
						externalProjectSelectableList.add("from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name");
						externalProjectSelectableList.add("from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current");
						externalProjectSelectableList.add("|");

						//PRG:RG6:R213:Mql Injection:parameterized Mql:17-Oct-2011:start
						String sCommandStatement = "print bus $1 select $2 $3 $4 dump $5";
						String output =  MqlUtil.mqlCommand(context,sCommandStatement,externalProjectSelectableList);
						if(ProgramCentralUtil.isNullString(output)){
							isObjectInaccessible = true;
							try{
								ProgramCentralUtil.pushUserContext(context);
								output =  MqlUtil.mqlCommand(context,sCommandStatement,externalProjectSelectableList);
							}finally{
								ProgramCentralUtil.popUserContext(context);
							}
						}
						//PRG:RG6:R213:Mql Injection:parameterized Mql:17-Oct-2011:End
						StringList projectInfo = FrameworkUtil.split(output, "|");
						successorProjectType = (String) projectInfo.get(0);
						palMapCache.put(projectAccessKeyIdForSuccessor, successorProjectType);
						palMapCache.put("name:" + projectAccessKeyIdForSuccessor, projectInfo.get(1));
						palMapCache.put("current:"+ projectAccessKeyIdForSuccessor, projectInfo.get(2));
						palMapCache.put("isAccssible:"+ projectAccessKeyIdForSuccessor, isObjectInaccessible);
					}

					if (!projectAccessKeyId.equals(projectAccessKeyIdForSuccessor))
					{
						if("Experiment".equals(successorProjectType)){
							continue;
						}
						//dependent tasks are from different projects; prefix dependency label with project name.
						projectName = (String) palMapCache.get("name:" + projectAccessKeyIdForSuccessor);
						externalProjectState = (String)palMapCache.get("current:"+ projectAccessKeyIdForSuccessor);
						isObjectInaccessible = (boolean)palMapCache.get("isAccssible:"+ projectAccessKeyIdForSuccessor);
					}

					String href = "../programcentral/emxProgramCentralUtil.jsp?mode=showTask&amp;objectId=" + successorId + "&amp;palId=" + projectAccessKeyIdForSuccessor;

					if (i > 0)
					{
						//seperate multiple dependencies with comma.
						toolTip.append(",");
						value.append(",");
					}

					String tip = null;
					if (projectName != null)
					{
						tip = projectName + ":" + successorTaskSequenceOrder + ":" + successorType;
					}
					else
					{
						tip = successorTaskSequenceOrder + ":" + successorType;
					}
					tip += Task.parseToDouble(successorLagTime) < 0 ? "" : "+";
					tip += successorLagTime + " " + successorLagTimeUnit;

					if (ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(externalProjectState)
							|| ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(externalProjectState)
							|| isObjectInaccessible) {

						value.append(tip);
					} else {
						tip = XSSUtil.encodeForHTML(context, tip);
						value.append("<a target='listHidden' href='" +  href + "'>");
						value.append(tip);
						value.append("</a>");					
					}
					toolTip.append(tip);
				}
			}
			if (!"".equals(toolTip))
			{   //Modified:26-Aug-11:MS9:R212:PRG:IR-083967V6R2012x
				if(isPrinterFriendly && ("CSV".equals(strPrinterFriendly) || "HTML".equals(strPrinterFriendly)))
				{
					results.add(toolTip.toString());
				} else {
					value.insert(0,"<div title='" + toolTip.toString() + "'>");
					value.append("</div>");
					results.add(value.toString());
				}
			}
			else
			{
				results.add(value.toString());
			}
			//End:26-Aug-11:MS9:R212:PRG:IR-083967V6R2012x
		}
		return results;
	}

	/**
	 * gets the Id details for the column
	 * Used for WBS Structure Browser
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	public Vector getProjectIdColumn(Context context, String[] args)
			throws Exception
			{
		Vector vId = null;
		try
		{
			HashMap projectMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) projectMap.get("objectList");

			vId = new Vector(objectList.size());
			Map map = null;
			for(int i=0; i<objectList.size(); i++) {
				map  = (Map) objectList.get(i);
				String type = (String) map.get("type");
				if(TYPE_TASK.equalsIgnoreCase(type)){
					vId.add(""+(i+1));
				}else{
					vId.add(""+i);
				}
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
		return vId;

			}



	/**
	 * Where : Task/ProjectSpace/ProjectConcept Name at WBS StrctureBrowser Table View Column
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectList"
	 *        1 - String containing "paramList"
	 * @returns Vector
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public Vector getNameColumn (Context context, String[] args) throws Exception
	{
		return getNameColumnData(context,FROM_PROJECT_WBS,args);
	}

	public Vector getProjectTemplateWBSNameColumn (Context context, String[] args) throws Exception
	{
		return getNameColumnData(context,FROM_PROJECT_TEMPLATE_WBS,args);
	}


	public Vector getNameColumnData (Context context,String mode, String[] args) throws Exception
	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		String exportFormat = (String)paramList.get("exportFormat");
		//boolean isMobile = UINavigatorUtil.isMobile(context);
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}
		String menuLink = DomainConstants.EMPTY_STRING;
		boolean fromProjTemp = false;
		if(FROM_PROJECT_TEMPLATE_WBS.equals(mode)) {
			menuLink = "&amp;treeMenu=type_TaskTemplate";
			fromProjTemp = true;
		}


		//
		// Find all the required infomration on each of the tasks here
		//
		String[] strObjectIds = new String[objectList.size()];
		int size = objectList.size();
		for (int i = 0; i < size; i++) {
			Map mapObject = (Map) objectList.get(i);
			String taskId = (String) mapObject.get(DomainObject.SELECT_ID);
			strObjectIds[i] = taskId;
		}

		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainConstants.SELECT_ID);
		slBusSelect.add(DomainConstants.SELECT_TYPE);
		slBusSelect.add(DomainConstants.SELECT_NAME);
		slBusSelect.add(DomainConstants.SELECT_CURRENT);
		slBusSelect.add(DomainConstants.SELECT_POLICY);
		slBusSelect.add(SELECT_IS_DELETED_SUBTASK);
		slBusSelect.add(SELECT_ATTRIBUTE_CRITICAL_TASK);
		slBusSelect.add(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT);
		slBusSelect.add(SELECT_PARENTOBJECT_KINDOF_PROJECT_BASELINE);
		slBusSelect.add(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
		slBusSelect.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_BASELINE);
		slBusSelect.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		slBusSelect.add(Person.SELECT_LAST_NAME);
		slBusSelect.add(Person.SELECT_FIRST_NAME);

		String IS_SUMMARY_TASK = "from["+DomainRelationship.RELATIONSHIP_SUBTASK+"].to.id";
		slBusSelect.add(DomainConstants.SELECT_DESCRIPTION);
		slBusSelect.add(IS_SUMMARY_TASK);
		slBusSelect.add(SELECT_IS_PARENT_TASK_DELETED);
		Map mapTaskInfo = new LinkedHashMap();
		BusinessObjectWithSelectList objectWithSelectList = DomainObject.getSelectBusinessObjectData(context, strObjectIds, slBusSelect);
		for (BusinessObjectWithSelectItr objectWithSelectItr = new BusinessObjectWithSelectItr(objectWithSelectList); objectWithSelectItr.next();) {
			BusinessObjectWithSelect objectWithSelect = objectWithSelectItr.obj();

			Map mapTask = new HashMap();
			for (Iterator itrSelectables = slBusSelect.iterator(); itrSelectables.hasNext();) {
				String strSelectable = (String)itrSelectables.next();
				mapTask.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
			}

			mapTaskInfo.put(objectWithSelect.getSelectData(SELECT_ID), mapTask);
		}

		//
		// No proceed with the logic of generating column values
		//

		Vector columnValues = new Vector(objectList.size());

		/*
 		Iterator objectListIterator = objectList.iterator();
        while (objectListIterator.hasNext())
        {
            Map objectMap = (Map) objectListIterator.next();
            String taskId = (String) objectMap.get(DomainObject.SELECT_ID);
		 */

		Iterator objectIdItr = mapTaskInfo.keySet().iterator();
		while(objectIdItr.hasNext()){

			String taskId = (String)objectIdItr.next();

			Map objectInfo = (Map)mapTaskInfo.get(taskId);
			boolean blDeletedTask = false;

			String strName = (String)objectInfo.get(SELECT_NAME);
			//Added:NZF:2013:Adding Tool Tip and Highlighting Sumamry Tasks
			String strDescription = (String)objectInfo.get(SELECT_DESCRIPTION);
			strDescription = " - "+strDescription;
			String strChildTasIds = (String) objectInfo.get(IS_SUMMARY_TASK);
			String strStrong = "";
			if(!strChildTasIds.isEmpty()){
				strStrong = "font-weight: bold";
			}
			//End:NZF:2013:Adding Tool Tip and Highlighting Sumamry Tasks
			boolean isPersonType = false;
			String taskObjType = (String)objectInfo.get(SELECT_TYPE);
			if(taskObjType.equalsIgnoreCase(DomainConstants.TYPE_PERSON)){
				strName = (String)objectInfo.get(Person.SELECT_LAST_NAME)+","+(String)objectInfo.get(Person.SELECT_FIRST_NAME);
				isPersonType = true;
			}
			String strNameandDesc = strName+strDescription;

			if("TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_IS_DELETED_SUBTASK)) || "TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_IS_PARENT_TASK_DELETED))) {
				blDeletedTask = true;
			}

			String critcalTask = (String)objectInfo.get(SELECT_ATTRIBUTE_CRITICAL_TASK);
			String sState = (String)objectInfo.get(SELECT_CURRENT);
			String taskPolicy =  (String)objectInfo.get(SELECT_POLICY);
			boolean isPolicyProjectReview = false;
			if(ProgramCentralConstants.TYPE_GATE.equals(taskObjType) && (ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy))){
				isPolicyProjectReview = true;
			}
			StringBuffer sBuff = new StringBuffer();
			//Check if the task comes from Project Template

		/*	if(ProgramCentralUtil.isNullString(exportFormat)) {
				if(isMobile){
				strName = XSSUtil.encodeForHTML(context, strName);
				strNameandDesc = XSSUtil.encodeForHTML(context, strNameandDesc);
				} else {
				strName = XSSUtil.encodeForXML(context, strName);
				strNameandDesc = XSSUtil.encodeForXML(context, strNameandDesc);
				}
			}*/
			if(ProgramCentralUtil.isNullString(exportFormat)) {
				strName = XSSUtil.encodeForXML(context, strName);
				strNameandDesc = XSSUtil.encodeForXML(context, strNameandDesc);
			}

			boolean isExperimentTask = "TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT)) || "TRUE".equalsIgnoreCase((String)objectInfo.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT));
			boolean isProjectBaselineTask = "TRUE".equalsIgnoreCase((String)objectInfo.get(SELECT_PARENTOBJECT_KINDOF_PROJECT_BASELINE)) || "TRUE".equalsIgnoreCase((String)objectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_BASELINE));
			//Added for "What If" functionality to hide hyperlink from Experiment project and task:start
			if(!(isExperimentTask || isProjectBaselineTask)){
				if (blDeletedTask){
					if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
						sBuff.append(strName);
					}
					else {
						sBuff.append("<font color='red'>");
						sBuff.append(strName);
						sBuff.append("</font>");
					}
				}else if( critcalTask!=null && sState!=null &&
						critcalTask.equalsIgnoreCase("true") &&
						!sState.equalsIgnoreCase(STATE_TASK_COMPLETE)) {
					if(!isPrinterFriendly){
						if (isPersonType) { // if block added to show person object in content page as per Widgetization HL.
							sBuff.append("<a href ='../common/emxTree.jsp?objectId=").append(XSSUtil.encodeForURL(context,taskId));
							sBuff.append("' title=\"").append(strNameandDesc);
							sBuff.append("\" style=\"color:red;").append(strStrong).append("\">");
							sBuff.append(strName);
						} else {
							sBuff.append("<a href ='javascript:showModalDialog(\"");
							sBuff.append("../common/emxTree.jsp?objectId=");
							sBuff.append(XSSUtil.encodeForURL(context,taskId));
							if(fromProjTemp && !isPolicyProjectReview && !"True".equalsIgnoreCase((String) objectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE))){
								sBuff.append(menuLink);
							}
							sBuff.append("\", \"812\", \"700\",\"true\",\"popup\",\"Medium\")' title=\""+strNameandDesc+"\" style=\"color:red;"+strStrong+"\">");
							sBuff.append(strName);
						}
						sBuff.append("</a>");
					}
					else {
						if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
							sBuff.append(strName);
						}
						else {
							sBuff.append("<font color='red'>");
							sBuff.append(strName);
							sBuff.append("</font>");
						}
					}
				}
				else{
					if(!isPrinterFriendly){
						if (isPersonType) { // if block added to show person object in content page as per Widgetization HL.
							sBuff.append("<a href ='../common/emxTree.jsp?objectId=").append(XSSUtil.encodeForURL(context,taskId));
							sBuff.append("' title=\"").append(strNameandDesc);
							sBuff.append("\" style=\"").append(strStrong).append("\">");
						} else {
							sBuff.append("<a href ='javascript:showModalDialog(\"");
							sBuff.append("../common/emxTree.jsp?objectId=");
							sBuff.append(XSSUtil.encodeForURL(context,taskId));
							if(fromProjTemp && !isPolicyProjectReview && !"True".equalsIgnoreCase((String) objectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE))){
								sBuff.append(menuLink);
							}
							sBuff.append("\", \"812\", \"700\",\"true\",\"popup\",\"Medium\")' title=\""+strNameandDesc+"\" style=\""+strStrong+"\">");
						}
					}
					sBuff.append(strName);
					if(!isPrinterFriendly){
						sBuff.append("</a>");
					}
				}
			}else{
				if(critcalTask!=null && sState!=null &&
						critcalTask.equalsIgnoreCase("true") &&
						!sState.equalsIgnoreCase(STATE_TASK_COMPLETE)) {
					if("CSV".equalsIgnoreCase(exportFormat)){
						sBuff.append(strName);
					}else{
						sBuff.append("<p style=\"color:red;"+strStrong+"\"  title=\""+strNameandDesc+"\">");
						sBuff.append(strName);
						sBuff.append("</p>");
					}
				}else{
					if("CSV".equalsIgnoreCase(exportFormat)){
						sBuff.append(strName);
					}else{
						sBuff.append("<p style=\""+strStrong+"\" title=\""+strNameandDesc+"\">");
						sBuff.append(strName);
						sBuff.append("</p>");
					}
				}
			}
			columnValues.add(sBuff.toString());
		}
		return columnValues;
	}

	/**
	 * Where : Private method to check update validity for Est Duration/Start/End Date.
	 * How :  Check the validity based on field type
	 *
	 *           feldType
	 *              duration  0
	 *              startDate 1
	 *              endDate   2
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "fieldType"
	 *        1 - String containing "currentState"
	 *        2 - String containing the "hasSubTask"
	 * @returns boolean
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public boolean checkEditable(int fieldType, String currentState, String hasSubTask) {
		// fieldType duration  0
		//           startDate 1
		//           endDate   2
		boolean blEditable = true;

		switch (fieldType){
		case 0: if(currentState.equalsIgnoreCase(STATE_PROJECT_TASK_COMPLETE)||
				currentState.equalsIgnoreCase(STATE_PROJECT_TASK_REVIEW) ||
				hasSubTask.equalsIgnoreCase("true"))
			blEditable = false;
		break;
		case 1: if(currentState.equalsIgnoreCase(STATE_PROJECT_TASK_COMPLETE)||
				currentState.equalsIgnoreCase(STATE_PROJECT_TASK_REVIEW) ||
				currentState.equalsIgnoreCase(STATE_PROJECT_TASK_ACTIVE))
			blEditable = false;
		break;
		case 2: if(currentState.equalsIgnoreCase(STATE_PROJECT_TASK_COMPLETE)||
				currentState.equalsIgnoreCase(STATE_PROJECT_TASK_REVIEW))
			blEditable = false;
		break;
		//Added:nr2:25-02-2010:PRG:IR-030653V6R2011
		case 3: if(currentState.equalsIgnoreCase(STATE_PROJECT_TASK_COMPLETE)||
				currentState.equalsIgnoreCase(STATE_PROJECT_TASK_REVIEW) ||
				currentState.equalsIgnoreCase(STATE_PROJECT_TASK_ACTIVE))
			blEditable = false;
		break;
		//End:nr2:25-02-2010:PRG:IR-030653V6R2011
		}
		return blEditable;
	}



	/**
	 * Where : Private method for Est Duration/Start/End Date update logic.
	 * How : Pass values to Task.updateDates to rollup Duration/StartDate/EndDate
	 *
	 *           feldType
	 *              duration  0
	 *              startDate 1
	 *              endDate   2
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectId"
	 *        1 - String containing "fieldType"
	 *        2 - String containing the "fieldValue"
	 * @returns none
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public void updateEstimatedDate(Context context,
			String objectId,
			String fieldType,
			Object fieldValue) throws Exception {

		if(ProgramCentralUtil.isNullString(objectId)){
			Task task = (Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK,PROGRAM);
			task.setId(objectId);
			String projectId = task.getInfo(context,ProgramCentralConstants.SELECT_PROJECT_ID);

			String SELECT_PROJECT_BUDGET_ID = "from["+ProgramCentralConstants.RELATIONSHIP_PROJECT_FINANCIAL_ITEM+"].to.id";
			DomainObject project = DomainObject.newInstance(context, projectId);
			String projectBudgetId = project.getInfo(context, SELECT_PROJECT_BUDGET_ID);

			updateEstimatedDate(context, objectId, fieldType, fieldValue,projectId,projectBudgetId);
		}
	}

	public void updateEstimatedDate(Context context, String objectId, String fieldType, Object fieldValue,String projectId, String projectBudgetId) throws Exception {

		try {

			updateTaskMap(context, objectId, fieldType, fieldValue);

			if(ProgramCentralUtil.isNotNullString(projectId) && ProgramCentralUtil.isNotNullString(projectBudgetId)) {
				updateBudgetDates(context,objectId,projectId,projectBudgetId);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	/** This method updates the budget start and end dates if the duration for any of the tasks is changed.
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @throws Exception if operation fails
	 * @author IXE
	 */
	private void updateBudgetDates(Context context, String objectId, String projectId, String projectBudgetId) throws Exception {

		DomainObject project = DomainObject.newInstance(context, projectId);

		StringList busSelect = new StringList(3);
		busSelect.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		busSelect.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

		Map<String,String> projectMap = project.getInfo(context, busSelect);

		String projectEstStartDate  = projectMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		String projectEstFinishDate = projectMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

		DomainObject budget = DomainObject.newInstance(context, projectBudgetId);

		Map<String,String> attributes = new HashMap<String,String>();
		attributes.put(Financials.ATTRIBUTE_COST_INTERVAL_START_DATE,projectEstStartDate);
		attributes.put(Financials.ATTRIBUTE_COST_INTERVAL_END_DATE,projectEstFinishDate);

		//Setting attribute
		budget.setAttributeValues(context,attributes);
	}

	/**
	 * Where : In the Structure Browser, Updating Est Duration in Edit mode
	 * How : Get the objectId from map update Estimate Duration with "New Value" and
	 *       pass it to the internal program updateEstimatedDate for rollup.
	 *
	 *       *    feldType
	 *              duration  0
	 *              startDate 1
	 *              endDate   2
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "columnMap"
	 *        1 - String containing "paramMap"
	 *        paramMap holds the following arguments:
	 *          0 - String containing the "objectId"
	 *          1 - String containing the "New Value"
	 * @returns none
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public void updateEstDuration(Context context, String[] args) throws Exception {

		Task task = (Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK, "PROGRAM");

		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		String newAttrValue = (String) paramMap.get("New Value");

		newAttrValue = newAttrValue.toLowerCase();// input unit of the duration can be "D" for days or "H" for hours which needs to be converted to lower case.

		String strLanguage = context.getSession().getLanguage();
		String strI18Days = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.DurationUnits.Days", strLanguage);
		String strI18Hours = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.DurationUnits.Hours", strLanguage);

		strI18Days = strI18Days.toLowerCase();
		strI18Hours = strI18Hours.toLowerCase();
		//input unit of duration entered in the duration column can be "Days" or "Hours" need to replace it by "d" or "h" respectively.
		if(newAttrValue.indexOf(strI18Days) != -1){
			newAttrValue = newAttrValue.replace(strI18Days, "d");
		}else if(newAttrValue.indexOf(strI18Hours) != -1){
			newAttrValue = newAttrValue.replace(strI18Hours, "h");
		}


		if(newAttrValue.contains("d") || newAttrValue.contains("h") ){

		int newAttrLength = newAttrValue.length();
		char durationUnit = newAttrValue.charAt(newAttrLength -1);
		String durationValue =  newAttrValue.trim().substring(0, newAttrLength -2);

		DecimalFormat numberFormatter = new DecimalFormat(".##");
		durationValue = numberFormatter.format(Task.parseToDouble(durationValue));
		newAttrValue = new StringBuffer(durationValue).append(" ").append(durationUnit).toString();
		}

		StringList selectList = new StringList(9);
		selectList.addElement(SELECT_NAME);
		selectList.addElement(SELECT_TYPE);
		selectList.addElement(SELECT_CURRENT);
		selectList.addElement(SELECT_HAS_SUBTASK);
		selectList.addElement(ProgramCentralConstants.SELECT_PROJECT_ID);
		selectList.addElement(ProgramCentralConstants.SELECT_TASK_PROJECT_BUDGET_ID);
		selectList.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		selectList.addElement(ATTRIBUTE_TASK_ACTUAL_START_DATE);

		task.setId(objectId);

		Map mp = task.getInfo(context,selectList);

		String strCurrentState = (String)mp.get(SELECT_CURRENT);
		String strName  = (String)mp.get(SELECT_NAME);
		String strHasSubTask = (String)mp.get(SELECT_HAS_SUBTASK);
		String projectId = (String)mp.get(ProgramCentralConstants.SELECT_PROJECT_ID);
		String projectBudgetId = (String)mp.get(ProgramCentralConstants.SELECT_TASK_PROJECT_BUDGET_ID);

		if(!checkEditable(0,strCurrentState,strHasSubTask)){
			String strErrorMsg = "emxProgramCentral.WBS.DurationCannotChange";
			String callFrom = (String) paramMap.get("callFrom");
			// Modified for Bug # 341607 - Begin
			String sKey[] = {"TaskName"};
			String sValue[] = {strName};
			// Modified for Bug # 341607 - End
			if(!"WhatIf".equalsIgnoreCase(callFrom)){
				String companyName = null;
				strErrorMsg  = emxProgramCentralUtilClass.getMessage(context,
						strErrorMsg,
						sKey,
						sValue,
						companyName);
				emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
			}
		}else
		{
			updateEstimatedDate(context,objectId,"durationS",newAttrValue,projectId,projectBudgetId);
		}
	}

	/**
	 * Where : In the Structure Browser, Updating Est Start Date in Edit mode
	 * How : Get the objectId from map update Estimate StartDate with "New Value" and
	 *       pass it to the internal program updateEstimatedDate for rollup.
	 *       *    feldType
	 *              duration  0
	 *              startDate 1
	 *              endDate   2
	 *
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "columnMap"
	 *        1 - String containing "paramMap"
	 *        paramMap holds the following arguments:
	 *          0 - String containing the "objectId"
	 *          1 - String containing the "New Value"
	 * @returns none
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public void updateEstStartDate(Context context, String[] args) throws Exception {

		Task task = (Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK, "PROGRAM");

		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap columnMap = (HashMap) inputMap.get("columnMap");
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		HashMap requestMap = (HashMap) inputMap.get("requestMap");
		String objectId = (String) paramMap.get("objectId");
		String newAttrValue = (String) paramMap.get("New Value");
		String callFrom = (String)inputMap.get("callFrom");
		Locale locale 			= (Locale)requestMap.get("locale");

		if(ProgramCentralUtil.isNotNullString(newAttrValue)){
		StringList selectList = new StringList(4);
		selectList.addElement(SELECT_NAME);
		selectList.addElement(SELECT_CURRENT);
        selectList.addElement(SELECT_POLICY);
        selectList.addElement(SELECT_TASK_CONSTRAINT_TYPE);
		selectList.addElement(ProgramCentralConstants.SELECT_PROJECT_ID);
		selectList.addElement(ProgramCentralConstants.SELECT_TASK_PROJECT_BUDGET_ID);

		task.setId(objectId);
		Map taskMap = task.getInfo(context,selectList);

		String strCurrentState 		= (String)taskMap.get(SELECT_CURRENT);
		String strName  			= (String)taskMap.get(SELECT_NAME);
		String strOldConstraintType = (String)taskMap.get(SELECT_TASK_CONSTRAINT_TYPE);
        String strPolicy            = (String)taskMap.get(SELECT_POLICY);
		String projectId 			= (String)taskMap.get(ProgramCentralConstants.SELECT_PROJECT_ID);
		String projectBudgetId 		= (String)taskMap.get(ProgramCentralConstants.SELECT_TASK_PROJECT_BUDGET_ID);

		if(!checkEditable(1,strCurrentState,null)){

            // IR-447723
            String i18ProjectState = EnoviaResourceBundle.getStateI18NString(context, strPolicy, strCurrentState, context.getLocale().getLanguage());
            String strErrorMsg = "emxProgramCentral.WBS.StartDateCannotChange";
			String sKey[] = {"TaskName","State"};
            // String sValue[] = {strName,strCurrentState};
            String sValue[] = {strName,i18ProjectState};

			String companyName = null;
			strErrorMsg  = emxProgramCentralUtilClass.getMessage(context,
					strErrorMsg,
					sKey,
					sValue,
					companyName);
			emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);

		} else {
			double clientTZOffset 	= Double.parseDouble((String)(requestMap.get("timeZone")));
			int iDateFormat 		= eMatrixDateFormat.getEMatrixDisplayDateFormat();
			DateFormat format 		= DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, locale);

			String fieldValue 		= eMatrixDateFormat.getFormattedInputDate(context,newAttrValue, clientTZOffset,locale);
			String tempfieldValue = eMatrixDateFormat.getFormattedDisplayDateTime(context, fieldValue, true,iDateFormat, clientTZOffset,locale);
			Date date 				= format.parse(tempfieldValue);

			updateEstimatedDate(context,objectId,"startDate",date,projectId,projectBudgetId);

		}//End::20 July, 2011:MS9:R212:IR-116909V6R2012x
	}
	}



	/**
	 * Where : In the Structure Browser, Updating Est End Date in Edit mode
	 * How : Get the objectId from map update EstEndDate with "New Value" and
	 *       pass it to the internal program updateEstimatedDate for rollup.
	 *       *    feldType
	 *              duration  0
	 *              startDate 1
	 *              endDate   2
	 *
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "columnMap"
	 *        1 - String containing "paramMap"
	 *        paramMap holds the following arguments:
	 *          0 - String containing the "objectId"
	 *          1 - String containing the "New Value"
	 * @returns none
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */
	public void updateEstEndDate(Context context, String[] args) throws Exception {

                Task task = (Task) DomainObject.newInstance(context,TYPE_TASK,PROGRAM);
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap columnMap = (HashMap) inputMap.get("columnMap");
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		HashMap requestMap = (HashMap) inputMap.get("requestMap");
		String objectId = (String) paramMap.get("objectId");
		String projectId = (String) requestMap.get("projectID");

		if(UIUtil.isNullOrEmpty(projectId)){
			projectId = (String) requestMap.get("objectId");
		}

		String fieldExpression = (String) columnMap.get("expression_businessobject");
		UIForm thisForm = new UIForm();
		String attrName = thisForm.getAttrNameFromSelect(fieldExpression);
		String attrSelectName = "attribute["+attrName+"]";

		String newAttrValue = (String) paramMap.get("New Value");

		if(ProgramCentralUtil.isNotNullString(newAttrValue)){
		double clientTZOffset = Double.parseDouble((String)(requestMap.get("timeZone")));
		int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
		Locale locale = (Locale)requestMap.get("locale");
		java.text.DateFormat format = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, locale);

		boolean isStartDateInvalid = false;
		String ATTRIBUTE_SCHEDULED_FROM = PropertyUtil.getSchemaProperty(context,"attribute_ScheduleFrom");
		final String SELECT_ATTRIBUTE_SCHEDULED_FROM = "attribute["+ATTRIBUTE_SCHEDULED_FROM+"]";
		final String SELECT_ATTRIBUTE_SCHEDULED_FROM_TASK = "to[Project Access Key].from.from[Project Access List].to.attribute[Schedule From].value";

		StringList selectList = new StringList(7);
		selectList.addElement(SELECT_NAME);
		selectList.addElement(SELECT_CURRENT);
		selectList.addElement(SELECT_TYPE);
		selectList.addElement(SELECT_POLICY);
		selectList.addElement(ProgramCentralConstants.SELECT_CALENDAR_ID);
		selectList.addElement(ProgramCentralConstants.SELECT_CALENDAR_DATE);
		selectList.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);

		StringList projectSelectable = new StringList(4);
		projectSelectable.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		projectSelectable.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
		projectSelectable.addElement(SELECT_ATTRIBUTE_SCHEDULED_FROM);
		projectSelectable.addElement(ProgramCentralConstants.SELECT_PROJECT_FINANCIAL_ITEM_ID);

		String[] objectIdArray = new String[1];
		objectIdArray[0] = projectId;   //Project id

		BusinessObjectWithSelectList bwsl = BusinessObject.getSelectBusinessObjectData(context, objectIdArray, projectSelectable);

		BusinessObjectWithSelect projectBwsl = bwsl.getElement(0);
		String projectBudgetId 		= (String)projectBwsl.getSelectData(ProgramCentralConstants.SELECT_PROJECT_FINANCIAL_ITEM_ID);
		String projScheduledFrom	= (String)projectBwsl.getSelectData(SELECT_ATTRIBUTE_SCHEDULED_FROM);
		String projStartDate 		= (String)projectBwsl.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		String projectEndDate 		= (String)projectBwsl.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

		objectIdArray[0] = objectId;    //Task id
		bwsl = BusinessObject.getSelectBusinessObjectData(context, objectIdArray, selectList);
		BusinessObjectWithSelect tasktBwsl = bwsl.getElement(0);

		String strName  		= (String)tasktBwsl.getSelectData(SELECT_NAME);
		String typeOfObject 	= (String)tasktBwsl.getSelectData(SELECT_TYPE);
		String taskCurrentState = (String)tasktBwsl.getSelectData(SELECT_CURRENT);
		String strPolicy		= (String)tasktBwsl.getSelectData(SELECT_POLICY);
		String taskCalendarId 	= (String)tasktBwsl.getSelectData(ProgramCentralConstants.SELECT_CALENDAR_ID);
		String taskCalendarDate = (String)tasktBwsl.getSelectData(ProgramCentralConstants.SELECT_CALENDAR_DATE);
		String taskStartDate 	= (String)tasktBwsl.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);

		Date dtProjStartDate 			= eMatrixDateFormat.getJavaDate(projStartDate);
		Date dtProjEndDate 				= eMatrixDateFormat.getJavaDate(projectEndDate);
		String strFormattedInputDate	= eMatrixDateFormat.getFormattedInputDate(context,newAttrValue,clientTZOffset,locale);
		Date dtProjNewStartDate = eMatrixDateFormat.getJavaDate(strFormattedInputDate);

		if(taskCurrentState!=null && (taskCurrentState.equals("Active") || (taskCurrentState.equals("Review")&& !strPolicy.equalsIgnoreCase(ProgramCentralConstants.POLICY_PROJECT_REVIEW))))
		{
			Date dTaskStartDate = eMatrixDateFormat.getJavaDate(taskStartDate);
			int dateDifference=0;

			if (ProgramCentralUtil.isNotNullString(taskCalendarId)) {
				if (dTaskStartDate.compareTo(dtProjNewStartDate) < 0) {
					WorkCalendar taskCalendar = WorkCalendar.getCalendarObject(context, taskCalendarId, taskCalendarDate);
					dateDifference =    (int)taskCalendar.computeDuration(context,dTaskStartDate, dtProjNewStartDate);
				} else {
					dateDifference = 1;
				}
			} else {
				DateUtil dateUtil = new DateUtil();
				dateDifference = (int)dateUtil.computeDuration(dTaskStartDate, dtProjNewStartDate);
			}
			task.setId(objectId);
			task.updateFinishDate(context, dtProjNewStartDate);
			task.updateStartDate(context, dTaskStartDate);
			task.updateDuration(context, dateDifference);
			//Added:24-Mar-10:vm3:R209:PRG:Bug 045883
			Date date = null;
			String fieldValue = eMatrixDateFormat.getFormattedInputDate(context,newAttrValue, clientTZOffset,locale);
			String tempfieldValue = eMatrixDateFormat.getFormattedDisplayDateTime(context, fieldValue, true,iDateFormat, clientTZOffset,locale);
			date = format.parse(tempfieldValue);

			updateEstimatedDate(context,objectId,"finishDate",date,projectId,projectBudgetId);
			//End Added:24-Mar-10:vm3:R209:PRG:Bug 045883
		}
		else
		{
			if (ProgramCentralConstants.ATTRIBUTE_SCHEDULED_FROM_RANGE_START.equals(projScheduledFrom)) {
				isStartDateInvalid = dtProjNewStartDate.before(dtProjStartDate);
			} else if (ProgramCentralConstants.ATTRIBUTE_SCHEDULED_FROM_RANGE_FINISH.equals(projScheduledFrom)) {
				//Allow Finish Date of Task to go beyond Project finish date.
				//If Project Is scheduled from Finish Date
				if(!attrName.equalsIgnoreCase(DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE)){
					isStartDateInvalid = dtProjNewStartDate.after(dtProjEndDate);
				}
			}

			if(!checkEditable(2,taskCurrentState,null)){
				String strErrorMsg = "emxProgramCentral.WBS.EndDateCannotChange";
                // IR-447723
                String i18ProjectState = EnoviaResourceBundle.getStateI18NString(context, strPolicy, taskCurrentState, context.getLocale().getLanguage());
				String sKey[] = {"TaskName","State"};
				// String sValue[] = {strName,taskCurrentState};
                String sValue[] = {strName,i18ProjectState};
				String companyName = null;
				strErrorMsg  = emxProgramCentralUtilClass.getMessage(context,
						strErrorMsg,
						sKey,
						sValue,
						companyName);
				emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
				// Modified:1-Apr-09:nzf:R207:PRG:Bug:361320
			} else if(isStartDateInvalid) {

				StringList slProjectSpaceTypeHierarchy = ProgramCentralUtil.getSubTypesList(context, TYPE_PROJECT_SPACE);

				if(isStartDateInvalid && !slProjectSpaceTypeHierarchy.contains(typeOfObject)){
					//End:R207:PRG:Bug:361320
					String strErrorASAP = "emxProgramCentral.WBS.EndDateCannotChangeASAP";
					String strErrorALAP = "emxProgramCentral.WBS.EndDateCannotChangeALAP";
					String sKey[] = {"TaskName"};
					String sValue[] = {strName};
					String companyName = null;

				}

			} else {
				String fieldValue = eMatrixDateFormat.getFormattedInputDate(context,newAttrValue,clientTZOffset,locale);
				String tempfieldValue = eMatrixDateFormat.getFormattedDisplayDateTime(context,fieldValue,true,iDateFormat, clientTZOffset,locale);
				Date date = format.parse(tempfieldValue);

				updateEstimatedDate(context,objectId,"finishDate",date,projectId,projectBudgetId);
			}
		}
	}
	}

	/**
	 * [ADDED::PA4:25-Feb-2011:IR-069051V6R2012::START]
	 *
	 * Info:This method helps to remove Gantt Chart command option from Action toolbar
	 * in case of Project Template.
	 *
	 * Settings Required for PMCWBSGanttChart command:
	 * Access Program: emxTask
	 * Access Function:hasAccessForGanttChart
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public boolean hasAccessForGanttChart(Context context, String args[]) throws MatrixException {
		try{
			final String PARENT_REL="relationship[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.relationship[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to." + SELECT_ID;
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) inputMap.get("objectId");

			if(null==objectId || "".equals(objectId)){
				throw new Exception("objectId is NULL");
			}

			DomainObject domObj = DomainObject.newInstance(context, objectId);
			if(domObj.isKindOf(context, TYPE_PROJECT_TEMPLATE)){
				return false;
			}
			else if(domObj.isKindOf(context, TYPE_TASK_MANAGEMENT)){
				String parentId = null;
				//[MODIFIED:PA4:27-Sept-2011:IR-119618V6R2013]
				try{
					ContextUtil.pushContext(context,
							PropertyUtil.getSchemaProperty(context,
									"person_UserAgent"),
									DomainConstants.EMPTY_STRING,
									DomainConstants.EMPTY_STRING);
					parentId = domObj.getInfo(context,PARENT_REL);
				}finally{
					ContextUtil.popContext(context);
				}
				if(null==parentId || "".equals(parentId)){
					MapList assigneeList = ((com.matrixone.apps.common.Task)domObj).getAssignees(context, null, null, null);
					if(assigneeList == null || assigneeList.size()==0)
						throw new Exception("parentId is NULL");
					else
						return true;
				}
				try{
					ContextUtil.pushContext(context,
							PropertyUtil.getSchemaProperty(context,
									"person_UserAgent"),
									DomainConstants.EMPTY_STRING,
									DomainConstants.EMPTY_STRING);
					domObj.setId(parentId);
					if(domObj.isKindOf(context, TYPE_PROJECT_TEMPLATE)){
						return false;
					}
				}finally{
					ContextUtil.popContext(context);
				}
				//[END:PA4:27-Sept-2011:IR-119618V6R2013]
			}
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}
	//[ADDED::PA4:25-Feb-2011:IR-069051V6R2012::END]


	/**
	 * Where : In the Structure Browser, All the Actions links at WBSView
	 *
	 * How : Get the objectId from argument map, show the link based on following conditions
	 *      1. selectedTable should not contain "BaseLine" string
	 *
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectId"
	 *        1 - String containing "selectedTable"
	 * @returns boolean
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public boolean hasAccessForWBSView(Context context, String args[]) throws Exception {

		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String strTable = (String)inputMap.get("selectedTable");
		String strProgram = (String)inputMap.get("selectedProgram");
		String objectId = (String)inputMap.get("objectId");

		StringList busSelects = new StringList();
		busSelects.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);
		busSelects.add(DomainConstants.SELECT_HAS_MODIFY_ACCESS);

		DomainObject domObj = DomainObject.newInstance(context, objectId);
		Map infoMap = domObj.getInfo(context, busSelects);

		String objType = (String) infoMap.get(DomainConstants.SELECT_TYPE);
		String rootObjType = (String) infoMap.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);

		if(DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(objType) ||
				DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(rootObjType)) {	//From task popup in Template side.
			ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
			boolean isCtxUserOwnerOrCoOwner =  projectTemplate.isOwnerOrCoOwner(context, objectId);
			return (isCtxUserOwnerOrCoOwner) ? true : false;
		}

		// Start User access for the Links
		boolean blAccess = false;
		//${CLASS:emxProjectMember} emxProjectMember = new ${CLASS:emxProjectMember}(context, args);
		//boolean projectAccess = emxProjectMember.hasAccess(context, args);//Checks Modify_Access
		String hasModifyAcc = (String) infoMap.get(DomainConstants.SELECT_HAS_MODIFY_ACCESS);

		// Baseline is Part of Command Name PMCWBSBaselineView
		//selectedProgram=emxTask:getWBSAllSubtasks
		//selectedProgram=emxTask:getWBSDeletedtasks

		if(strTable!=null && strTable.indexOf("BaseLine")>0 ||
				(strProgram!=null && strProgram.indexOf("MemberTasks")>0) ||
				(strProgram!=null && strProgram.indexOf("Deleted")>0)){
			blAccess = false;
		}else if(Boolean.valueOf(hasModifyAcc)){
			blAccess = true;
		}

		return blAccess;
	}

	/**
	 * Where : PMCWBSProjectTemplateAssignQuestion Command for disabling it if no questions are there in the Project Template.
	 * ADDED for BUG : 299885
	 *
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectId"
	 *        1 - String containing "selectedTable"
	 * @returns boolean
	 * @throws Exception if operation fails
	 * @since PMC V6R2009x
	 */

	public boolean hasAccessForWBSQuestionsCommand(Context context, String args[]) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String)inputMap.get("objectId");

		// Start User access for the Links
		DomainObject dom = DomainObject.newInstance(context,strObjectId);

		boolean hasAccess = hasAccessForWBSView (context, args);

		if (hasAccess){

			//
			// Show Assign Questions command only if there are questions to assign
			//
			StringList selectStmts  = new StringList();
			selectStmts.addElement(DomainConstants.RELATIONSHIP_PROJECT_QUESTION);

			// Rel selects
			//
			StringList selectRelStmts = new StringList(0);


			MapList mpQueationsList = dom.getRelatedObjects(context,
					DomainConstants.RELATIONSHIP_PROJECT_QUESTION,
					DomainConstants.TYPE_QUESTION,
					selectStmts,
					selectRelStmts,
					false,
					true,
					(short)1,
					null,
					null);


			if (mpQueationsList.size() > 0) {
				hasAccess = true;
			}
			else {
				hasAccess = false;
			}
		}
		return hasAccess;
	}



	/**
	 * Where : In the Structure Browser, Create Baseline enable at Actions Links
	 *
	 * How : Get the objectId from argument map, show the link based on following conditions
	 *      1. Baseline Initial start date should be null
	 *      2. selectedTable should be contain "BaseLine" string
	 *
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectId"
	 *        1 - String containing "selectedTable"
	 * @returns boolean
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public boolean hasAccessForCreateBaseline(Context context, String args[]) throws Exception
	{
		com.matrixone.apps.program.ProjectSpace project =
				(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
						DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

		HashMap inputMap = (HashMap)JPO.unpackArgs(args);

		String strTable = (String)inputMap.get("selectedTable");
		String strProgram = (String)inputMap.get("selectedProgram");

		String strObjectId = (String)inputMap.get("objectId");
		boolean blCreateBaseline = false;
		boolean blAccess = false;

		StringList sList = new StringList(2);
		sList.addElement(ProjectSpace.SELECT_TYPE);
		sList.addElement(ProjectSpace.SELECT_BASELINE_INITIAL_START_DATE);

		project.setId(strObjectId);

		Map objectMap = (Map) project.getInfo(context,sList);
		String strBaselineInitialStartDate  = (String)objectMap.get(ProjectSpace.SELECT_BASELINE_INITIAL_START_DATE);
		String strType = (String)objectMap.get(ProjectSpace.SELECT_TYPE);


		if(null==strBaselineInitialStartDate || "".equals(strBaselineInitialStartDate)){
			blCreateBaseline = true;
		}


		if(mxType.isOfParentType(context,strType,DomainConstants.TYPE_PROJECT_SPACE)){
			if(strTable!=null && strTable.indexOf("BaseLine")>0) {
				if(blCreateBaseline && !(ProgramCentralUtil.isNotNullString(strProgram)&& (strProgram.indexOf("MemberTasks")>0 ||
						strProgram.indexOf("Deleted")>0))){
					blAccess = true;
				}
				else
					blAccess = false;
			}else {
				blAccess = false;
			}
		}else {
			blAccess = false;
		}


		return blAccess;
	}


	/**
	 * Where : In the Structure Browser, Revise Baseline enable at Actions Links
	 *
	 * How : Get the objectId from argument map, show the link based on following conditions
	 *      1. Baseline Initial start date should not be null
	 *      2. selectedTable should be contain "BaseLine" string
	 *
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectId"
	 *        1 - String containing "selectedTable"
	 * @returns boolean
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public boolean hasAccessForReviseBaseline(Context context, String args[]) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String strTable = (String)inputMap.get("selectedTable");
		String strProgram = (String)inputMap.get("selectedProgram");
		String strObjectId = (String)inputMap.get("objectId");
		boolean blReviseBaseline = false;
		boolean blAccess = false;

		StringList sList = new StringList(2);
		sList.addElement(ProjectSpace.SELECT_TYPE);
		sList.addElement(ProjectSpace.SELECT_BASELINE_INITIAL_START_DATE);
		sList.addElement(DomainConstants.SELECT_HAS_MODIFY_ACCESS);

		ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
		project.setId(strObjectId);

		Map objectMap = (Map) project.getInfo(context,sList);
		String strBaselineInitialStartDate  = (String)objectMap.get(ProjectSpace.SELECT_BASELINE_INITIAL_START_DATE);
		String strType = (String)objectMap.get(ProjectSpace.SELECT_TYPE);
		String hasModifyAcc = (String)objectMap.get(DomainConstants.SELECT_HAS_MODIFY_ACCESS);

		if(ProgramCentralUtil.isNotNullString(strBaselineInitialStartDate)){
			blReviseBaseline = true;
		}

		if(mxType.isOfParentType(context,strType,DomainConstants.TYPE_PROJECT_SPACE)){
			if(strTable!=null && strTable.indexOf("BaseLine")>0) {
				if(blReviseBaseline && !(ProgramCentralUtil.isNotNullString(strProgram)&& (strProgram.indexOf("MemberTasks")>0 ||
						strProgram.indexOf("Deleted")>0))){
					blAccess = true;
				}
				else {
					blAccess = false;
				}
			} else {
				blAccess = false;
			}
		} else {
			blAccess = false;
		}

		return (blAccess && Boolean.valueOf(hasModifyAcc));
	}

	/**
	 * This method will return true if Baseline is created and baseline initial dates are set on Project Space object.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments
	 * @return true if baseline is created and baseline dates are set
	 * @throws Exception if operation fails
	 */
	public boolean hasAccessToBaselineView(Context context, String args[]) throws Exception
	{

		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		
		String strObjectId = (String)inputMap.get("objectId");
		
		boolean hasAccess = false;

		StringList objectSelect = new StringList(2);
		objectSelect.addElement(ProjectSpace.SELECT_TYPE);
		objectSelect.addElement(ProjectSpace.SELECT_BASELINE_INITIAL_START_DATE);
		objectSelect.addElement(ProjectSpace.SELECT_BASELINE_INITIAL_END_DATE);

		ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
		project.setId(strObjectId);

		Map objectMap = (Map) project.getInfo(context,objectSelect);
		String strBaselineInitialStartDate  = (String)objectMap.get(ProjectSpace.SELECT_BASELINE_INITIAL_START_DATE);
		String strBaselineInitialEndDate  = (String)objectMap.get(ProjectSpace.SELECT_BASELINE_INITIAL_END_DATE);
		
		if(ProgramCentralUtil.isNotNullString(strBaselineInitialStartDate) && ProgramCentralUtil.isNotNullString(strBaselineInitialEndDate)){
			hasAccess = true;
		}

		return hasAccess;
	}


	/**
	 * Where : In the Structure Browser, Toolbar Action MSProject Links enability.
	 *          emxProgramCentral.Project.LaunchForViewingInMSProject
	 *          emxProgramCentral.Project.LaunchForEditingInMSProject
	 *
	 * How : Get the objectId from argument map, and validity will be based on following
	 *      1. Type should be ProjectSpace
	 *      2. MSPComponentInstalled should be configured.
	 *
	 * Settings Required :
	 *      1. For Edit -> Access Mask = Modify , AccessProgram / Function
	 *      2. For View -> AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectId"
	 * @returns boolean
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	// Access to commands Need to check with MS Intergaration
	// For Edit -> Access Mask = Modify , AccessProgra/Function
	// For View -> AccessProgram/Function

	public boolean hasAccessForMSProject(Context context, String args[]) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String)inputMap.get("objectId");

		StringList busSelects = new StringList();
		busSelects.add(DomainConstants.SELECT_TYPE);
		busSelects.add("current.access");
		
		DomainObject domObj = DomainObject.newInstance(context, strObjectId);
		Map infoMap = domObj.getInfo(context, busSelects);
    				
		String strObjectType = (String) infoMap.get(DomainConstants.SELECT_TYPE);
		String currAccess = (String) infoMap.get("current.access");

		boolean isProjectType = false;
		boolean blAccess = false;
		if(mxType.isOfParentType(context,strObjectType,DomainConstants.TYPE_PROJECT_SPACE)){
			isProjectType = true;
		}
		//MSDesktopIntegration-Start
		String MSDIPropertyKey = "emxProgramCentral.MSDI.MSPComponentInstalled";
		String MSDIPropertyValue = EnoviaResourceBundle.getProperty(context,MSDIPropertyKey);

		//Show the following links only if the property is set to true
		if(MSDIPropertyValue != null && "true".equalsIgnoreCase(MSDIPropertyValue) && isProjectType){
			blAccess = true;
		}
		
		blAccess = blAccess && ( "All".equalsIgnoreCase(currAccess) || currAccess.contains("read"));
		return blAccess;
	}

	/**
	 * Where : In the Structure Browser, Updating State in Edit mode
	 * How : Get the objectId from argument map update with "New Value" state.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "columnMap"
	 *        1 - String containing the "paramMap"
	 *        @param args holds the following input arguments:
	 *          0 - String containing "objectId"
	 *          1 - String containing "New Value"
	 * @returns None
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */
	public void updateState(Context context, String[] args) throws Exception {
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap columnMap = (HashMap) inputMap.get("columnMap");
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		String newAttrValue = (String) paramMap.get("New Value");

			Task commonTask = new Task();
			commonTask.setId(objectId);

     if(ProgramCentralUtil.isNotNullString(newAttrValue)){
        	PropertyUtil.setRPEValue(context, "State", newAttrValue, true);
        	commonTask.setState(context, newAttrValue);
            commonTask.rollupAndSave(context);
		}
	}


	/**
	 * Where : In the Structure Browser, If the user doesn't have permission to
	 *          modify field then message throws when click on "Edit".
	 * How : Get the objectId from argument map update map with Action message.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "columnMap"
	 *
	 * @returns HashMap
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	@com.matrixone.apps.framework.ui.PreProcessCallable
	public HashMap preProcessCheckForEdit (Context context, String[] args) throws Exception
	{
		// unpack the incoming arguments
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		HashMap tableData = (HashMap) inputMap.get("tableData");
		MapList objectList = (MapList) tableData.get("ObjectList");
		String strObjectId = (String) paramMap.get("objectId");

		HashMap returnMap = null;
		DomainObject dom = DomainObject.newInstance(context, strObjectId);
		// Checking Access
		boolean editFlag = dom.checkAccess(context, (short) AccessConstants.cModify);
		//IR-179642V6R2013x and  IR-179642V6R2014 Begin
		String sPrjState = ProgramCentralConstants.EMPTY_STRING;
		StringList busSelect = new StringList();
		busSelect.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current");
		busSelect.add(SELECT_CURRENT);
		Map mpParentInfo = new HashMap();
		try
		{
			ContextUtil.pushContext(context);
			mpParentInfo = dom.getInfo(context, busSelect);

			if(dom.isKindOf(context, TYPE_TASK_MANAGEMENT)){
				sPrjState=(String)mpParentInfo.get("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current");
				if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(sPrjState))
				{
					editFlag = false;
				}
			}
		}
		finally
		{
			ContextUtil.popContext(context);
		}


		//		else if(dom.isKindOf(context, TYPE_PROJECT_SPACE)){
		//        		if(!ProgramCentralUtil.hasAccessToModifyProject(context, strObjectId)){
		//
		//        			 sPrjState = (String)mpParentInfo.get(SELECT_CURRENT);
		//        			if(sPrjState.equals(DomainObject.STATE_PROJECT_SPACE_COMPLETE))
		//        			{
		//        			editFlag = true;
		//        			}else{editFlag = false;}
		//        		}else{
		//        			editFlag = true;
		//        		}
		//		}
		//IR-179642V6R2013x and  IR-179642V6R2014 End
		if(editFlag){
			returnMap = new HashMap(2);
			returnMap.put("Action","Continue");
			returnMap.put("ObjectList",objectList);
		} else {
			returnMap = new HashMap(3);
			returnMap.put("Action","Stop");
			returnMap.put("Message","emxProgramCentral.WBS.NoEdit");
			returnMap.put("ObjectList",objectList);
		}
		return returnMap;

	}
	@com.matrixone.apps.framework.ui.PreProcessCallable
	public HashMap preProcessCheckForLock (Context context, String[] args) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		HashMap tableData = (HashMap) inputMap.get("tableData");
		MapList objectList = (MapList) tableData.get("ObjectList");
		String strObjectId = (String) paramMap.get("objectId");
		HashMap returnMap = null;
		boolean lockFlag = false;
		lockFlag =  Task.isParentProjectLocked(context, strObjectId);
		if(!lockFlag){
			returnMap = new HashMap(2);
			returnMap.put("Action","Continue");
			returnMap.put("ObjectList",objectList);
		} else {
			returnMap = new HashMap(3);
			returnMap.put("Action","Stop");
			returnMap.put("Message","emxProgramCentral.WBS.NoEdit");
			returnMap.put("ObjectList",objectList);
		}
		return returnMap;
	}

	/**
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments
	 *
	 * @returns true if user has modify access.
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean hasModifyAccess(Context context, String[] args) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String) inputMap.get("objectId");
		DomainObject dom = DomainObject.newInstance(context, strObjectId);

		boolean editFlag = dom.checkAccess(context, (short) AccessConstants.cModify);

		String sPrjState = ProgramCentralConstants.EMPTY_STRING;
		StringList busSelect = new StringList();
		busSelect.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current");
		busSelect.add(SELECT_CURRENT);
		busSelect.add("type.kindof[" + TYPE_TASK_MANAGEMENT + "]");
		Map mpParentInfo = new HashMap();
		try
		{
			ProgramCentralUtil.pushUserContext(context);
			mpParentInfo = dom.getInfo(context, busSelect);
			boolean isKindOfTaskManagement = "True".equalsIgnoreCase((String)mpParentInfo.get("type.kindof[" + TYPE_TASK_MANAGEMENT + "]"));

			if(isKindOfTaskManagement){
				sPrjState=(String)mpParentInfo.get("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current");
				if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(sPrjState))
				{
					editFlag = false;
				}
			}
		}
		finally
		{
			ProgramCentralUtil.popUserContext(context);
		}
		return editFlag;
	}

	/**
	 * Where : In the Structure Browser, If the user doesn't have permission to
	 *          modify field then message throws when click on "Edit".
	 * How : Get the objectId from argument map update map with Action message.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "columnMap"
	 *
	 * @returns HashMap
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public HashMap postProcessRefresh (Context context, String[] args) throws Exception
	{
		String rollupMessage = EMPTY_STRING;
		Map postProcessMap = (Map)JPO.unpackArgs(args);
		String isFromGantt = (String)postProcessMap.get("FromGantt");
		
		HashMap returnMap = new HashMap(1);
		returnMap.put("Action","refresh");
		returnMap.put("SORT","ID|ascending");//SORT colname|direction
		
		try {

			String strMsg = EMPTY_STRING;
			Map updatedMap = new HashMap(); 
			if(!_taskMap.isEmpty()){
				updatedMap = com.matrixone.apps.common.Task.updateDates(context,_taskMap,true);
				rollupMessage = (String)updatedMap.get("RollupMessage");
				strMsg = (String)updatedMap.get("Message");

				if(Boolean.valueOf(isFromGantt)){
					if(ProgramCentralUtil.isNotNullString(rollupMessage)){
						rollupMessage = XSSUtil.encodeForJavaScript(context, rollupMessage);
						returnMap.put("RollupMessage", rollupMessage);
					}
					return returnMap;
				}
			if(ProgramCentralUtil.isNotNullString(strMsg) && !"false".equalsIgnoreCase(strMsg)){
				throw new Exception(strMsg);
			}
				
				if(ProgramCentralUtil.isNotNullString(rollupMessage)){
					emxContextUtilBase_mxJPO.mqlNotice(context,rollupMessage);
				}
			}

		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
                return returnMap;
	}


	/**
	 *
	 * Used to display dependency column in Task Dependency table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	public Vector getWBSRowState(Context context, String[] args)
			throws Exception
			{
		StringList stateList = new StringList();

		HashMap programMap        = (HashMap) JPO.unpackArgs(args);
		String languageStr        = context.getSession().getLanguage();
		MapList  objectList       = (MapList) programMap.get("objectList");
		Iterator objectListIterator = objectList.iterator();

		DomainObject domainObject = DomainObject.newInstance(context);

		while (objectListIterator.hasNext())
		{
			Map objectMap = (Map) objectListIterator.next();
			String strObjectId = (String) objectMap.get(DomainConstants.SELECT_ID);

			StringList busSelects = new StringList(2);
			busSelects.addElement(DomainConstants.SELECT_CURRENT);
			busSelects.addElement(DomainConstants.SELECT_STATES);
			busSelects.addElement(DomainConstants.SELECT_TYPE);

			domainObject.setId(strObjectId);

			Map map = (Map)domainObject.getInfo(context,busSelects);

			String strCurrentState = (String) map.get(DomainConstants.SELECT_CURRENT);
			StringList strStates = domainObject.getInfoList(context,DomainConstants.SELECT_STATES);
			String strType = (String) map.get(DomainConstants.SELECT_TYPE);

			StringList i18nStatesList = i18nNow.getAdminI18NStringList("state",strStates,languageStr);

			StringBuffer sb = new StringBuffer("<select name=\"");
			sb.append(strObjectId);
			sb.append("\" >");

			for(int i=0; i<strStates.size(); i++) {

				String stateName = (String)strStates.elementAt(i);

				sb.append("<option value=\"");
				sb.append(stateName);
				sb.append("\"");
				if(stateName.equals(strCurrentState)) {
					sb.append(" selected=\"selected\" ");
				}
				sb.append(">");
				sb.append(i18nStatesList.elementAt(i));
				sb.append("</option> ");
			}
			sb.append("</select>");
			stateList.add(sb.toString());
		}
		return stateList;
			}


	/**
	 * gets the list of dependent tasks for the given task.
	 * Used for PMCDependentTask table from command command_PMCDependentTask
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getTaskDependencies(Context context, String[] args)
			throws Exception
			{
		MapList taskList = new MapList();

		try
		{
			final String SELECT_LAG_TIME_UNIT = "attribute[" + DependencyRelationship.ATTRIBUTE_LAG_TIME + "].inputunit";
			final String SELECT_DEPENDENT_TASK_PROJECT_MEMBER = "to[Project Access Key].from.from[Project Access List].to.from[Member].to.id";
			//
			// Find the units defined in dimension for Lag Time
			//
			Unit unit = null;
			UnitList unitList = null;
			AttributeType attrType = new AttributeType(DependencyRelationship.ATTRIBUTE_LAG_TIME);
			try
			{
				Dimension dimension = attrType.getDimension(context);
				if (dimension == null) {
					unitList = new UnitList();
				}
				else {
					unitList = dimension.getUnits(context);
				}
			}
			catch (Exception e) {
				unitList = new UnitList();
			}

			com.matrixone.apps.program.ProjectSpace project =
					(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
			com.matrixone.apps.common.DependencyRelationship dependency = null;
			com.matrixone.apps.common.SubtaskRelationship subtask = null;
			HashMap programMap        = (HashMap) JPO.unpackArgs(args);
			String  objectId          = (String) programMap.get("objectId");
			this.setId(objectId);

			StringList busSelects = new StringList();
			StringList relSelects = new StringList();
			busSelects.add(SELECT_ID);

			try
			{
				project = getProject(context);
			}
			catch (Exception e)
			{
				//user has no access to project; try to expand from parent
				MapList parent = getParentInfo(context, 1, busSelects);
				if (parent.size() > 0)
				{
					Map parentInfo = (Map) parent.get(0);
					String parentId = (String) parentInfo.get(SELECT_ID);
					project.setId(parentId);
				}

				/*if(null == topTaskId || "".equals(topTaskId)){
                      topTaskId = objectId;
                    } */
			}

			if (project!=null && project.getObjectId() != null)
			{
				busSelects.add(SELECT_NAME);
				busSelects.add("to[" + RELATIONSHIP_SUBTASK + "]." + subtask.SELECT_TASK_WBS);
				taskList = getPredecessors(context, busSelects, null, null);
			}
			else
			{
				taskList = new MapList();
			}

			busSelects.clear();
			busSelects.add(SELECT_ID);
			busSelects.add(ProgramCentralConstants.SELECT_PROJECT_ID);
			busSelects.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);
			busSelects.add(ProgramCentralConstants.SELECT_PROJECT_NAME);
			busSelects.add(SELECT_DEPENDENT_TASK_PROJECT_MEMBER);

			MapList parentList = getParentInfo(context, 0, busSelects);
			Iterator ParentItr = parentList.iterator();
			ArrayList relatedIdList = new ArrayList(50);

			// Add all the parents of the current task to the relatedIdList.
			// If a task is in this list it will not be selectable.
			while (ParentItr.hasNext()) {
				Map relative = (Map) ParentItr.next();
				relatedIdList.add(relative.get(SELECT_ID));
			}
			// Add all the children of the current task to the relatedIdList.
			MapList childrenList = getTasks(context, this, 0, busSelects, relSelects);
			Iterator childItr    = childrenList.iterator();
			while (childItr.hasNext()) {
				Map relative = (Map) childItr.next();
				relatedIdList.add(relative.get(SELECT_ID));
			}
			relatedIdList.add(objectId);

			//get the tasks dependencies
			relSelects.clear();
			relSelects.add(dependency.SELECT_DEPENDENCY_TYPE);

			relSelects.add(dependency.SELECT_LAG_TIME);
			relSelects.add(SELECT_LAG_TIME_UNIT);
			relSelects.add(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);

			MapList dependencyList = getPredecessors(context, busSelects, relSelects, null);

			String taskProjectId = project.getObjectId();
			String contextPersonId = PersonUtil.getPersonObjectID(context);

			//Added for:IR-217802V6R2014
			MapList hideTaskList = new MapList();
			for(int i=0;i<dependencyList.size();i++){
				Map dependencyMap =  (Map)dependencyList.get(i);
				String dependentTaskId = (String)dependencyMap.get(DomainObject.SELECT_ID);
				String dependentTaskProjectId = (String)dependencyMap.get(ProgramCentralConstants.SELECT_PROJECT_ID);
				Object dependentTaskProjectMember = dependencyMap.get(SELECT_DEPENDENT_TASK_PROJECT_MEMBER);
				if(dependentTaskProjectMember != null){
					if(dependentTaskProjectMember instanceof StringList){
						StringList memberList = (StringList)dependentTaskProjectMember;
						if(!taskProjectId.equals(dependentTaskProjectId) && !memberList.contains(contextPersonId)){
							for(int j=0;j<taskList.size();j++){
								Map taskMap = (Map)taskList.get(j);
								String taskId = (String)taskMap.get(DomainObject.SELECT_ID);
								if(taskId.equals(dependentTaskId)){
									hideTaskList.add(taskMap);
								}
							}
						}
					}else{
						String strMemberId = (String)dependentTaskProjectMember;
						if(!taskProjectId.equals(dependentTaskProjectId) && !contextPersonId.equals(strMemberId)){
							for(int j=0;j<taskList.size();j++){
								Map taskMap = (Map)taskList.get(j);
								String taskId = (String)taskMap.get(DomainObject.SELECT_ID);
								if(taskId.equals(dependentTaskId)){
									hideTaskList.add(taskMap);
								}
							}
						}
					}
				}
				taskList.removeAll(hideTaskList);
			}//End

			// Iterate and add the predessor info and index number to each task maps
			Iterator listItr = taskList.iterator();
			while (listItr.hasNext()) {
				Map object            = (Map) listItr.next();
				String taskId         = (String) object.get(SELECT_ID);
				String dependencyType = "";
				String lagTime        = "";
				String lagTimeUnit        = "";
				String strDurationKeyword = "";
				if (dependencyList.isEmpty()) {
					dependencyType = "  ";
				} else {
					Iterator dependencyListItr = dependencyList.iterator();
					while(dependencyListItr.hasNext()) {
						Map dependencyMap   = (Map) dependencyListItr.next();
						String dependencyId = (String) dependencyMap.get(SELECT_ID);
						lagTime = (String) dependencyMap.get(dependency.SELECT_LAG_TIME);
						lagTimeUnit = (String) dependencyMap.get(SELECT_LAG_TIME_UNIT);
						strDurationKeyword = (String) dependencyMap.get(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);

						//
						// denormalizing Lagtime from its default Unit.
						//
						for(UnitItr unitItr = new UnitItr(unitList);unitItr.next();){
							unit = unitItr.obj();
							if(unit.getName().equals(lagTimeUnit)){
								lagTime = unit.denormalize(lagTime);
								break;
							}
						}

						if(dependencyId.equals(taskId)) {
							dependencyType = (String) dependencyMap.get(dependency.SELECT_DEPENDENCY_TYPE);
							break;
						} else {
							dependencyType = "  "; //blank
						}
					}
				}
				object.put("dependency", dependencyType);
				object.put("lagTime", lagTime);
				object.put("lagTimeUnit", lagTimeUnit);
				object.put(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, strDurationKeyword);

				//set displayState for each task
				String displayState = "none";

				Iterator relatedItr = relatedIdList.iterator();
				while (relatedItr.hasNext()) {
					String parentId = (String) relatedItr.next();
					// If the nodeId is in the familyIdList grey it out.
					if (parentId.equals(taskId)) {
						displayState = "greyOut";
						break;
					}
					//If a node has same id as objectId passed in, then it is the selected node
					else if (objectId.equals(taskId)) {
						displayState = "selected";
						break;
					}
				} //end while

				object.put ("DISPLAYSTATE",displayState);
			}
			// sort the MapList by WBS number
			taskList.sort("to[" + RELATIONSHIP_SUBTASK + "]." + subtask.SELECT_TASK_WBS, "ascending", "multilevel");
		}catch(Exception exc) {
		}
		return taskList;
			} //end of method.

	/**
	 * gets the dependent task project.
	 * Used to display project column in Task Dependency table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */

	public StringList getTaskDependentProject(Context context, String[] args)throws Exception {
		StringList dependencyProjectNameList = new StringList();
		HashMap programMap       = (HashMap) JPO.unpackArgs(args);
		HashMap paramList        = (HashMap) programMap.get("paramList");
		String  objectId         = (String) paramList.get("objectId");
		MapList taskList         = (MapList) programMap.get("objectList");

		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if(strPrinterFriendly != null){
			isPrinterFriendly = true;
		}

		StringList objectSelects = new StringList(6);
		objectSelects.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);
		objectSelects.add(ProgramCentralConstants.SELECT_PROJECT_ID);
		objectSelects.add(ProgramCentralConstants.SELECT_PROJECT_NAME);
		objectSelects.add(SELECT_TASK_PROJECT_CURRENT);
		objectSelects.add(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT);
		objectSelects.add(SELECT_PARENTOBJECT_KINDOF_PROJECT_TEMPLATE);
		objectSelects.add(SELECT_PARENTOBJECT_KINDOF_PROJECT_CONCEPT);

		String prjHyperLnkFirstPart = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=programcentral&amp;suiteKey=ProgramCentral";
		String prjHyperLnkMdlPart ="'700', '600', 'false', 'popup', '')\"   class=\"object\">";
		String prjHyperLnkImgPart ="<img border=\"0\" src=\"images/iconSmallProject.gif\"/>";
		String templateHyperLnkImgPart ="<img border=\"0\" src=\"images/iconSmallProjectTemplate.gif\"/>";
		String conceptHyperLnkImgPart ="<img border=\"0\" src=\"images/iconSmallProjectConcept.gif\"/>";
		String experimentHyperLnkImgPart ="<img border=\"0\" src=\"images/iconMenuMaterialsCompliance.gif\"/>";
		String prjHyperLnkLastPart ="</a>";

		String []taskIdArray = new String[taskList.size()];

		for(int i=0;i<taskList.size();i++){
			Map <String,String> taskMap = (Map)taskList.get(i);
			taskIdArray[i] = taskMap.get(DomainObject.SELECT_ID);
		}

		MapList taskProjectInfoMapList = DomainObject.getInfo(context, taskIdArray, objectSelects);

		for(int i=0;i<taskProjectInfoMapList.size();i++) {

			StringBuffer projectLink = new StringBuffer();

			Map <String,String>taskProjectMap = (Map)taskProjectInfoMapList.get(i);
			String strTaskProjectType = taskProjectMap.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);
			String strTaskProjectName = taskProjectMap.get(ProgramCentralConstants.SELECT_PROJECT_NAME);
			String strTaskProjectId = taskProjectMap.get(ProgramCentralConstants.SELECT_PROJECT_ID);
			String isTaskProjectTypeExperiment = taskProjectMap.get(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT);
			String isTaskProjectTypeTemplate = taskProjectMap.get(SELECT_PARENTOBJECT_KINDOF_PROJECT_TEMPLATE);
			String isTaskProjectTypeConcept = taskProjectMap.get(SELECT_PARENTOBJECT_KINDOF_PROJECT_CONCEPT);
			String projectState = taskProjectMap.get(SELECT_TASK_PROJECT_CURRENT);

			if (ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(projectState)
					|| ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(projectState)) {
				isPrinterFriendly = true;
			}
			if(!isPrinterFriendly && !"TRUE".equalsIgnoreCase(isTaskProjectTypeExperiment)){
				projectLink.append(prjHyperLnkFirstPart);
				projectLink.append("&amp;objectId=");
				projectLink.append(XSSUtil.encodeForURL(context,strTaskProjectId));
				projectLink.append("', ");
				projectLink.append(prjHyperLnkMdlPart);
			}

			if(!("csv".equalsIgnoreCase(strPrinterFriendly)) && !"TRUE".equalsIgnoreCase(isTaskProjectTypeExperiment)){
				if("TRUE".equalsIgnoreCase(isTaskProjectTypeTemplate)){
					projectLink.append(templateHyperLnkImgPart);
				}else if("TRUE".equalsIgnoreCase(isTaskProjectTypeConcept)){
					projectLink.append(conceptHyperLnkImgPart);
				}else{
					projectLink.append(prjHyperLnkImgPart);
				}
			}

			if(!isPrinterFriendly && !"TRUE".equalsIgnoreCase(isTaskProjectTypeExperiment)){
				projectLink.append(prjHyperLnkLastPart);
				projectLink.append(prjHyperLnkFirstPart);
				projectLink.append("&amp;objectId=");
				projectLink.append(XSSUtil.encodeForURL(context,strTaskProjectId));
				projectLink.append("', ");
				projectLink.append(prjHyperLnkMdlPart);
			}
			if("TRUE".equalsIgnoreCase(isTaskProjectTypeExperiment)){
				projectLink.append(experimentHyperLnkImgPart);
			}

			projectLink.append(XSSUtil.encodeForXML(context,strTaskProjectName));

			if(!isPrinterFriendly && !"TRUE".equalsIgnoreCase(isTaskProjectTypeExperiment)){
				projectLink.append(prjHyperLnkLastPart);
			}
			dependencyProjectNameList.add(projectLink.toString());

		}
		return dependencyProjectNameList;
	}

	/**
	 * gets the dependency type like SF, SS, etc.
	 * Used to display dependency column in Task Dependency table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	public Object getTaskDependency(Context context, String[] args)
			throws Exception
			{
		StringList dependencyList = new StringList();
		StringList projectList = new StringList();
		HashMap programMap        = (HashMap) JPO.unpackArgs(args);
		String  objectId          = (String) programMap.get("objectId");
		String languageStr        = context.getSession().getLanguage();
		MapList  objectList       = (MapList) programMap.get("objectList");
		Iterator objectListIterator = objectList.iterator();
		Map paramList = (Map) programMap.get("paramList");
		String tableName = (String) paramList.get("selectedTable");
		// Create iterator for Dependency type
		StringList dependencyTypeList = this.getDependencyTypes(context);

		//Create Localized Range Values for Attribute "Dependency Type"
		StringList i18nDependencyTypeList = i18nNow.getAttrRangeI18NStringList(PropertyUtil.getSchemaProperty(context, "attribute_DependencyType"),
				dependencyTypeList,
				languageStr);
		//ADDED for PRG:Bug:IR-059285V6R2011x
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		boolean isPrinterFriendly = false;
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}
		boolean isModifyOp = false;
		if(!"PMCAddTaskDependencyTable".equals(tableName)){
			isModifyOp = true;
		}


		while (objectListIterator.hasNext())
		{
			Map objectMap = (Map) objectListIterator.next();
			String strDependency = (String) objectMap.get("dependency");
			String taskId = (String) objectMap.get(SELECT_ID);
			String taskLevel = (String) objectMap.get(SELECT_LEVEL);
			StringBuffer dependencyType = new StringBuffer("");
			//MODIFIED for PRG:Bug:IR-059285V6R2011x
			if("0".equals(taskLevel) && !isModifyOp)
				dependencyType.append(strDependency);
			else if(!isPrinterFriendly)
			{
				dependencyType = new StringBuffer("<select name=\"");
				dependencyType.append(XSSUtil.encodeForHTML(context,taskId));
				dependencyType.append("\" >");
				if(isModifyOp){
					strDependency = XSSUtil.encodeForHTML(context,strDependency);
					strDependency = ProgramCentralUtil.isNotNullString(strDependency)?strDependency:"";
				}

				for(int i=0; i<dependencyTypeList.size(); i++) {
					String currentDependency =XSSUtil.encodeForHTML(context, (String)dependencyTypeList.elementAt(i));
					dependencyType.append("<option value=\"");
					dependencyType.append(currentDependency);
					dependencyType.append("\"");
					if(isModifyOp && strDependency.equals(currentDependency)) {
						dependencyType.append(" selected='selected' ");
					}
					dependencyType.append(">");
					dependencyType.append(i18nDependencyTypeList.elementAt(i));
					dependencyType.append("</option> ");
				}
				dependencyType.append("</select>");
			}
			else
			{
				dependencyType.append(strDependency);
			}
			//END for PRG:Bug:IR-059285V6R2011x
			dependencyList.add(dependencyType.toString());
		}
		return dependencyList;
			}


	/**
	 * Returns list of tasks that are not dependent on selected task.
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWBSIndependentTaskList(Context context, String[] args)
			throws MatrixException
			{
		try{
			HashMap arguMap = (HashMap)JPO.unpackArgs(args);
			String strSelectedTaskId = (String)arguMap.get("selectedTaskId");
			String strProjectId = (String)arguMap.get("objectId");
			String strExpandLevel = (String) arguMap.get("expandLevel");
			MapList taskMapList = new MapList();

			//taskMapList = getWBSSubtasks(context, args);
			short nExpandLevel =  ProgramCentralUtil.getExpandLevel(strExpandLevel);
			taskMapList = (MapList) getWBSTasks(context,strProjectId,DomainConstants.RELATIONSHIP_SUBTASK,nExpandLevel);
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			task.setId(strSelectedTaskId);
			//To get all the dependent task.
			StringList slSubtasks = task.getInfoList(context, "from["+RELATIONSHIP_DEPENDENCY+"].to.id");

			StringList tempSelects = new StringList();
			tempSelects.add(task.SELECT_ID);
			tempSelects.add(task.SELECT_NAME);
			//To get all the parent task.
			MapList taskParents = task.getParentInfo(context,0,tempSelects);
			StringList finalListParent=new StringList();
			for (Iterator itrRelatedObjects = taskParents.iterator(); itrRelatedObjects.hasNext();)
			{
				Map mapRelatedObjectInfoParent = (Map) itrRelatedObjects.next();
				String strParentTaskId= (String)mapRelatedObjectInfoParent.get(task.SELECT_ID);
				finalListParent.add(strParentTaskId);
			}
			//To get all the child Task.
			MapList taskChildren = task.getTasks(context,task,0,tempSelects,null);
			StringList finalListChildren=new StringList();
			for (Iterator itrRelatedObjects = taskChildren.iterator(); itrRelatedObjects.hasNext();)
			{
				Map mapRelatedObjectInfoChildren = (Map) itrRelatedObjects.next();
				String strChildTaskId= (String)mapRelatedObjectInfoChildren.get(task.SELECT_ID);
				finalListChildren.add(strChildTaskId);
			}
			StringList slProjSpaceSubtypeList = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_SPACE);
			StringList slProjConceptSubtypeList = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_CONCEPT);

			ArrayList<Integer> alIndexes = new ArrayList<Integer>();

			for(int index=0 ; index<taskMapList.size(); index++){
				Map taskMap = (Map) taskMapList.get(index);
				String type = (String)taskMap.get(DomainConstants.SELECT_TYPE);
				String id =  (String)taskMap.get(DomainConstants.SELECT_ID);
				String name =  (String)taskMap.get(DomainConstants.SELECT_NAME);

				if(ProgramCentralUtil.isNullString(id)){
					alIndexes.add(index);
					continue;
				}
				if(strSelectedTaskId.equals(id)|| (!slSubtasks.isEmpty()&& slSubtasks.contains(id)) || finalListParent.contains(id)||finalListChildren.contains(id))
					taskMap.put("disableSelection", "true");
				else
					taskMap.put("disableSelection", slProjSpaceSubtypeList.contains(type) || slProjConceptSubtypeList.contains(type)?"true":"");
			}

			for (Iterator itrIndexes = alIndexes.iterator(); itrIndexes.hasNext();) {
				int index = (Integer) itrIndexes.next();
				taskMapList.remove(index);
			}
			return taskMapList;
		}catch(Exception e){
			throw new MatrixException(e);
		}
			}
	/**
	 * gets the task slack time.
	 * Used to display slack time column in Task Dependency table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	public Object getTaskSlackTime(Context context, String[] args)
			throws Exception
			{
		StringList latTimeLsit = new StringList();
		StringList projectList = new StringList();
		HashMap programMap        = (HashMap) JPO.unpackArgs(args);
		Map paramList = (Map) programMap.get("paramList");
		String  objectId          = (String) programMap.get("objectId");
		MapList  objectList       = (MapList) programMap.get("objectList");
		String languageStr = (String) paramList.get("languageStr");
		String tableName = (String) paramList.get("selectedTable");
		Iterator objectListIterator = objectList.iterator();
		StringBuffer lagTime = new StringBuffer();

		//
		// Find the units defined in dimension for Lag Time
		//
		Unit unit = null;
		UnitList unitList = null;
		AttributeType attrType = new AttributeType(DependencyRelationship.ATTRIBUTE_LAG_TIME);
		//ADDED for PRG:Bug:IR-059285V6R2011x
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}
		boolean isModifyOp = false;
		if(!"PMCAddTaskDependencyTable".equals(tableName)){
			isModifyOp = true;
		}
		try{
			Dimension dimension = attrType.getDimension(context);
			if (dimension == null) {
				unitList = new UnitList();
			}
			else {
				unitList = dimension.getUnits(context);
			}
		}
		catch (Exception e) {
			unitList = new UnitList();
		}

		while (objectListIterator.hasNext()){
			Map objectMap = (Map) objectListIterator.next();

			String strId = XSSUtil.encodeForHTML(context,(String) objectMap.get(SELECT_ID));
			String taskLevel = (String) objectMap.get(SELECT_LEVEL);
			String strLagTime = "";
			String strLagTimeUnit = "";
			if("0".equals(taskLevel) && !isModifyOp)
				lagTime.append(strLagTime);
			else{
				if(isModifyOp){
					strLagTime =XSSUtil.encodeForHTML(context,(String) objectMap.get("lagTime"));
					strLagTime = ProgramCentralUtil.isNotNullString(strLagTime)?strLagTime:"";
					strLagTimeUnit = (String) objectMap.get("lagTimeUnit");
					strLagTimeUnit = ProgramCentralUtil.isNotNullString(strLagTimeUnit)?strLagTimeUnit:"";
				}

				if(isModifyOp && !"".equals(strLagTime) && strLagTime.charAt(0) != '-' && !strLagTime.equals("0")){
					strLagTime = "+" + strLagTime;
				}
				lagTime = new StringBuffer();
				//Modified for PRG:Bug:IR-059285V6R2011x
				if(isPrinterFriendly){
					//Modified:13-Dec-10:s4e:R211:PRG:IR-083026V6R2012
					//Modified to display Slack time unit in Export and printer friendly view
					String strLagTimeUnitDisplay="";
					for (int i = 0; i < unitList.size(); i++) {
						unit = (Unit)unitList.get(i);
						if(strLagTimeUnit.equals(unit.getName())){
							strLagTimeUnitDisplay = unit.getLabel();
						}
					}
					lagTime.append(strLagTime);
					lagTime.append(" ");
					lagTime.append(strLagTimeUnitDisplay);
					//End:Modified:13-Dec-10:s4e:R211:PRG:IR-083026V6R2012
				}
				else{

					lagTime.append("<input type=\"text\" name=\"lag_");
					lagTime.append(strId);
					lagTime.append("\" id=\"lag_");
					lagTime.append(strId);
					lagTime.append("\" size=\"9\" value=\"");
					lagTime.append(strLagTime);

					lagTime.append("\" onChange = \"validLagTime(this,'"+strId+"')\"/>");

					lagTime.append(" ");
					// Show the units list box
					lagTime.append("<select name=\"").append("unit_").append(strId).append("\" id=\"").append("unit_").append(strId).append("\" onchange=\"checkDurationKeyword('"+strId+"')\">");
					for (int i = 0; i < unitList.size(); i++) {
						unit = (Unit)unitList.get(i);
						String lable=ProgramCentralConstants.EMPTY_STRING;
						String unitLabel=unit.getLabel();
						if("Days".equals(unitLabel))
						{
							lable =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Days", context.getSession().getLanguage()) ;
						}else if("Hours".equals(unitLabel))
						{
							lable =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Hours", context.getSession().getLanguage()) ;
						}
						lagTime.append("<option value=\"").append(unit.getName()).append("\"").append((strLagTimeUnit.equals(unit.getName()))?" selected='selected'":"").append(">")
						.append(lable)
						.append("</option>");
					}
					lagTime.append("</select>");

				}

			}
			latTimeLsit.add(lagTime.toString());
			//END for PRG:Bug:IR-059285V6R2011x





		}

		return latTimeLsit;
			}



	/**
	 * displays checkboxes.
	 * Used to display checkbox column in Task Dependency table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	public Object displayTaskDependencyCheckbox(Context context, String[] args)
			throws Exception
			{
		StringList ckbLsit = new StringList();
		HashMap  programMap         = (HashMap) JPO.unpackArgs(args);
		String   objectId           = (String) programMap.get("objectId");
		MapList  objectList         = (MapList) programMap.get("objectList");
		Iterator objectListIterator = objectList.iterator();

		while (objectListIterator.hasNext())
		{
			Map objectMap = (Map) objectListIterator.next();
			String strId = (String) objectMap.get(SELECT_ID);
			//<input type="checkbox" name="emxTableRowId" value="28105.43455.4459.64941|28105.43455.4392.61715" onclick="doCheckboxClick(this); doSelectAllCheck(this)">
			StringBuffer ckbColumn = new StringBuffer("<input type=\"checkbox\" name=\"selectedIds\"");
			ckbColumn.append(" value=\"");
			ckbColumn.append(strId);
			ckbColumn.append("\" ");
			ckbColumn.append("onclick=\"doCheckboxClick(this); doSelectAllCheck(this)\"");
			ckbColumn.append(">");
			ckbLsit.add(ckbColumn.toString());
		}
		return ckbLsit;
			}

	/**
	 * checks whether context user can edit or not task depedency.
	 * Used to display/not display action commands in Task Dependency table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	public Boolean hasTaskDepednecyEditAccess(Context context, String[] args) throws Exception {
		HashMap  programMap   = (HashMap) JPO.unpackArgs(args);
		String   objectId     = (String) programMap.get("objectId");

		StringList busSelects = new StringList();
		busSelects.add(DomainConstants.SELECT_HAS_MODIFY_ACCESS);
		busSelects.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);

		DomainObject domObj = DomainObject.newInstance(context, objectId);
		Map infoMap = domObj.getInfo(context, busSelects);

		String hasModifyAcc = (String) infoMap.get(DomainConstants.SELECT_HAS_MODIFY_ACCESS);
		String rootObjType = (String) infoMap.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);

		if(DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(rootObjType)){//From task popup in Template side.
			ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
			boolean isCtxUserOwnerOrCoOwner =  projectTemplate.isOwnerOrCoOwner(context, objectId);
			if(!isCtxUserOwnerOrCoOwner)
				return false;
		}
		return Boolean.valueOf(hasModifyAcc);
	}

	/**
	 * When the task is promoted to Finish State this function is called.
	 * Gets the tasks which are dependent on this task and notifies the assignees
	 * if the Dependency Type is FS or FF.
	 *
	 * Note: object id must be passed as first argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @throws Exception if operation fails
	 * @since AEF X+2
	 */
	public void triggerNotifyFinishDependentTaskAction(Context context, String[] args)
			throws Exception
			{

		DebugUtil.debug("Entering triggerNotifyDependentTaskAction");

		// get values from args.
		String objectId  = args[0];
		String fromState = args[1];
		String toState   = args[2];

		setId(objectId);

		String parentTaskName = getName(context);
		StringList taskPromoteList = new StringList(1);

		StringList busSelects = new StringList(4);
		busSelects.add(SELECT_ID);
		busSelects.add(SELECT_NAME);
		busSelects.add(SELECT_CURRENT);
		busSelects.add(SELECT_STATES);

		String sDependencyType = "attribute["+DomainConstants.ATTRIBUTE_DEPENDENCY_TYPE+"]";

		StringList relSelects = new StringList(2);
		relSelects.add(SELECT_RELATIONSHIP_ID );
		relSelects.add(sDependencyType);

		// get all the Dependency
		MapList utsList = getRelatedObjects(context,
				RELATIONSHIP_DEPENDENCY,
				QUERY_WILDCARD,
				busSelects,
				relSelects,       // relationshipSelects
				true,      // getTo
				false,       // getFrom
				(short) 1,  // recurseToLevel
				null,// objectWhere
				null);      // relationshipWhere
		if (utsList.size() > 0)
		{
			Iterator itr = utsList.iterator();
			while (itr.hasNext())
			{
				Map map = (Map) itr.next();
				String strID = (String) map.get(SELECT_ID);
				String strState = (String) map.get(SELECT_CURRENT);
				String strDependecyType = (String)map.get(sDependencyType);
				// get all the Dependency
				setId(strID);
				MapList dependList = getRelatedObjects(context,
						RELATIONSHIP_DEPENDENCY,
						QUERY_WILDCARD,
						busSelects,
						relSelects,       // relationshipSelects
						false,      // getTo
						true,       // getFrom
						(short) 1,  // recurseToLevel
						null,// objectWhere
						null);      // relationshipWhere
				boolean blPromote = false;
				if(dependList!=null && dependList.size()>0){
					int t=0;
					//Added:P6E:PRG:R208:24-Mar-2011:HF-100513V6R2010x_
					//blPromote = true;
					//End:P6E:PRG:R208:24-Mar-2011:HF-100513V6R2010x_
					while(dependList.size()>t){
						Map dependMap = (Map)dependList.get(t++);
						StringList dependStates = (StringList)dependMap.get(SELECT_STATES);
						String dependState = (String)dependMap.get(SELECT_CURRENT);
						int dependStatePos = dependStates.indexOf(dependState);
						int activeStatePos = dependStates.indexOf(STATE_PROJECT_TASK_ACTIVE);
						int completeStatePos = dependStates.indexOf(STATE_PROJECT_TASK_COMPLETE);

						String dependType = (String)dependMap.get(sDependencyType);
						if(((dependType.equalsIgnoreCase("FS") ||
								dependType.equalsIgnoreCase("FF")) && dependStatePos < completeStatePos) || ((dependType.equalsIgnoreCase("SS") ||
										dependType.equalsIgnoreCase("SF")) && dependStatePos < activeStatePos)){
							//Added:P6E:PRG:R208:24-Mar-2011:HF-100513V6R2010x_
							//blPromote = false;
							//break;
							continue;
						} else if(!taskPromoteList.contains(strID)) {
							taskPromoteList.addElement(strID);
						}
						//End:P6E:PRG:R208:24-Mar-2011:HF-100513V6R2010x_
					}
				}
				//Added:P6E:PRG:R208:24-Mar-2011:HF-100513V6R2010x_
				/*if(blPromote) {
                    taskPromoteList.addElement(strID);
                }*/
				//End:P6E:PRG:R208:24-Mar-2011:HF-100513V6R2010x_
			}
			setId(objectId);
		}

		if(!taskPromoteList.isEmpty()) {
			Object[] objects = taskPromoteList.toArray();
			//promote each of the tasks in the list
			//use super user to overcome access issue
			ContextUtil.pushContext(context);
			try
			{
			String baseurl = emxMailUtilBase_mxJPO.getBaseURL(context, null);
				for(int i=0; i<objects.length; i++)
				{
					String id = (String)objects[i];
					setId(id);
					busSelects.clear();
					busSelects.add(SELECT_NAME);
					busSelects.add(SELECT_TYPE);
					busSelects.add(SELECT_DESCRIPTION);
					busSelects.add(SELECT_HAS_ASSIGNED_TASKS);
					busSelects.add(SELECT_TASK_ESTIMATED_START_DATE);
					busSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
					busSelects.add(SELECT_TASK_ESTIMATED_DURATION);
					busSelects.add(SELECT_ASSIGNEES);
					busSelects.add(SELECT_OWNER);
					busSelects.add(SELECT_CURRENT);
					busSelects.add(SELECT_POLICY);
					busSelects.add(ProgramCentralConstants.SELECT_TASK_PROJECT_NAME);

					Map objMap = getInfo(context, busSelects);

					String taskType          = (String) objMap.get(SELECT_TYPE);
					String taskName          = (String) objMap.get(SELECT_NAME);
					String taskDescription   = (String) objMap.get(SELECT_DESCRIPTION);
					String taskEstStartDate  = (String) objMap.get(
							SELECT_TASK_ESTIMATED_START_DATE);
					String taskEstFinishDate = (String) objMap.get(
							SELECT_TASK_ESTIMATED_FINISH_DATE);
					String taskEstDuration   = (String) objMap.get(
							SELECT_TASK_ESTIMATED_DURATION);
					String taskOwner = (String) objMap.get(SELECT_OWNER);
					String currentState = (String) objMap.get(SELECT_CURRENT);
					String taskPolicy = (String) objMap.get(SELECT_POLICY);
					String projectSpace =  (String) objMap.get(ProgramCentralConstants.SELECT_TASK_PROJECT_NAME);
					if(currentState.equals(STATE_PROJECT_TASK_CREATE) && !ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy))
						promote(context); // Promote To Assign State
						busSelects.clear();
						busSelects.add(SELECT_NAME);
						// get the assignees for this task
						MapList assigneeList = getAssignees(context, busSelects, null, null);

								if (assigneeList.size() > 0)
								{Iterator itr = assigneeList.iterator();
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
                        // Sends mail notification to the owners.
                        //get the mail subject

                        String sMailSubject =
                            "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyFinishDependentTask.Subject";
                        String companyName = null;
                        String mKey[] = {"PreTaskName","TaskName"};
                        String mValue[] ={parentTaskName,taskName};


                       //get the mail message
                        String sMailMessage =
                            "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyFinishDependentTask.Message";
                        String mKey1[] = {"TaskName", "TaskDescription", "TaskEstStartDate",
                                "TaskEstFinishDate","TaskAsignees","Owner","TaskURL"};

                        String mValue1[] = {taskName, taskDescription, taskEstStartDate,
                                taskEstFinishDate, mailToList.toString(), taskOwner, ""};
                      
                        String rpeUserName = PropertyUtil.getGlobalRPEValue(context,ContextUtil.MX_LOGGED_IN_USER_NAME);
                        MailUtil.setAgentName(context, rpeUserName);

                        //MailUtil.setAgentName(context, context.getUser());

                        StringList objectIdList = new StringList(1);
                        objectIdList.addElement(getId());



                          String basePropFile ="emxProgramCentralStringResource";
                              emxMailUtilBase_mxJPO.sendNotification(context,
                    		  mailToList,
                                 mailCcList,
                                 mailCcList,
                                 sMailSubject,
                                 mKey,
                                 mValue,
                                 sMailMessage,
                                 mKey1,
                                 mValue1,
                                 objectIdList,
                                 "",
                                 basePropFile);
                      
                      
                     	} // if
				} //for

			} // try
			catch (Exception e)
			{
				DebugUtil.debug("Exception Task triggerPromoteAction- ",e.getMessage());
				throw e;
			}
			finally
			{
				ContextUtil.popContext(context);
			}
		} // if

		DebugUtil.debug("Exiting Task triggerNotifyDependentTaskAction");

			}

	/**
	 * When the task is promoted to Active State this function is called.
	 * Gets the tasks which are dependent on this task and notifies the assignees
	 * if the Dependency Type is SS or SF.
	 *
	 * Note: object id must be passed as first argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @throws Exception if operation fails
	 * @since AEF X+2
	 */
	public void triggerNotifyStartDependentTaskAction(Context context, String[] args)
			throws Exception
			{

		DebugUtil.debug("Entering triggerNotifyDependentTaskAction");

		// get values from args.
		String objectId  = args[0];
		String fromState = args[1];
		String toState   = args[2];

		setId(objectId);

		String parentTaskName = getName(context);
		StringList taskPromoteList = new StringList(1);

		StringList busSelects = new StringList(4);
		busSelects.add(SELECT_ID);
		busSelects.add(SELECT_NAME);
		busSelects.add(SELECT_CURRENT);
		busSelects.add(SELECT_STATES);

		String sDependencyType = "attribute["+DomainConstants.ATTRIBUTE_DEPENDENCY_TYPE+"]";

		StringList relSelects = new StringList(2);
		relSelects.add(SELECT_RELATIONSHIP_ID );
		relSelects.add(sDependencyType);

		// get all the Dependency
		MapList utsList = getRelatedObjects(context,
				RELATIONSHIP_DEPENDENCY,
				QUERY_WILDCARD,
				busSelects,
				relSelects,       // relationshipSelects
				true,      // getTo
				false,       // getFrom
				(short) 1,  // recurseToLevel
				null,// objectWhere
				null);      // relationshipWhere

		if (utsList.size() > 0)
		{
			Iterator itr = utsList.iterator();
			while (itr.hasNext())
			{
				Map map = (Map) itr.next();
				String strID = (String) map.get(SELECT_ID);
				String strDependecyType = (String)map.get(sDependencyType);
				// get all the Dependency
				setId(strID);
				MapList dependList = getRelatedObjects(context,
						RELATIONSHIP_DEPENDENCY,
						QUERY_WILDCARD,
						busSelects,
						relSelects,       // relationshipSelects
						false,      // getTo
						true,       // getFrom
						(short) 1,  // recurseToLevel
						null,// objectWhere
						null);      // relationshipWhere
				boolean blPromote = false;
				if(dependList!=null && dependList.size()>0){
					int t=0;
					blPromote = true;
					while(dependList.size()>t){
						Map dependMap = (Map)dependList.get(t++);
						StringList dependStates = (StringList)dependMap.get(SELECT_STATES);
						String dependState = (String)dependMap.get(SELECT_CURRENT);
						int dependStatePos = dependStates.indexOf(dependState);
						int activeStatePos = dependStates.indexOf(STATE_PROJECT_TASK_ACTIVE);
						int completeStatePos = dependStates.indexOf(STATE_PROJECT_TASK_COMPLETE);

						String dependType = (String)dependMap.get(sDependencyType);
						if(((dependType.equalsIgnoreCase("FS") ||
								dependType.equalsIgnoreCase("FF")) && dependStatePos < completeStatePos) || ((dependType.equalsIgnoreCase("SS") ||
										dependType.equalsIgnoreCase("SF")) && dependStatePos < activeStatePos)){

							blPromote = false;
							break;
						}
					}
				}
				if(blPromote) {
					taskPromoteList.addElement(strID);
				}
			}
			setId(objectId);
		}

		if(!taskPromoteList.isEmpty()) {
			Object[] objects = taskPromoteList.toArray();
			//promote each of the tasks in the list
			//use super user to overcome access issue
			ContextUtil.pushContext(context);
			try
			{

				for(int i=0; i<objects.length; i++)
				{
					String id = (String)objects[i];
					setId(id);

					busSelects.clear();
					busSelects.add(SELECT_NAME);
					busSelects.add(SELECT_TYPE);
					busSelects.add(SELECT_DESCRIPTION);
					busSelects.add(SELECT_HAS_ASSIGNED_TASKS);
					busSelects.add(SELECT_TASK_ESTIMATED_START_DATE);
					busSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
					busSelects.add(SELECT_TASK_ESTIMATED_DURATION);
					busSelects.add(SELECT_ASSIGNEES);
					busSelects.add(SELECT_OWNER);
					busSelects.add(SELECT_CURRENT);

					Map objMap = getInfo(context, busSelects);

					String taskType          = (String) objMap.get(SELECT_TYPE);
					String taskName          = (String) objMap.get(SELECT_NAME);
					String taskDescription   = (String) objMap.get(SELECT_DESCRIPTION);
					String taskEstStartDate  = (String) objMap.get(
							SELECT_TASK_ESTIMATED_START_DATE);
					String taskEstFinishDate = (String) objMap.get(
							SELECT_TASK_ESTIMATED_FINISH_DATE);
					String taskEstDuration   = (String) objMap.get(
							SELECT_TASK_ESTIMATED_DURATION);
					String taskOwner = (String) objMap.get(SELECT_OWNER);
					String currentState = (String) objMap.get(SELECT_CURRENT);
					if(currentState.equals(STATE_PROJECT_TASK_CREATE))
						promote(context);
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
                        // Sends mail notification to the owners.
                        //get the mail subject

                        String sMailSubject =
                            "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyStartDependentTask.Subject";
                        String companyName = null;
                        String mKey[] = {"PreTaskName","TaskName"};
                        String mValue[] ={parentTaskName,taskName};


                         //get the mail message
                        String sMailMessage =
                            "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyStartDependentTask.Message";
                        String mKey1[] = {"ProjectName","TaskName", "TaskDescription", "TaskEstStartDate",
                                "TaskEstFinishDate","TaskAsignees","Owner","TaskURL"};

                        String mValue1[] = {parentTaskName, taskName, taskDescription, taskEstStartDate,
                                taskEstFinishDate, mailToList.toString(), taskOwner, ""};
                  
                        String rpeUserName = PropertyUtil.getGlobalRPEValue(context,ContextUtil.MX_LOGGED_IN_USER_NAME);
                        MailUtil.setAgentName(context, rpeUserName);
                        //MailUtil.setAgentName(context, context.getUser());


                        StringList objectIdList = new StringList(1);
                        objectIdList.addElement(getId());

                         
                        String basePropFile ="emxProgramCentralStringResource";
                        emxMailUtilBase_mxJPO.sendNotification(context,
                           mailToList,
                           mailCcList,
                           mailCcList,
                           sMailSubject,
                           mKey,
                           mValue,
                           sMailMessage,
                           mKey1,
                           mValue1,
                           objectIdList,
                           "",
                           basePropFile);
                   		} // if
				} //for

			} // try
			catch (Exception e)
			{
				DebugUtil.debug("Exception Task triggerPromoteAction- ",e.getMessage());
				throw e;
			}
			finally
			{
				ContextUtil.popContext(context);
			}
		} // if

		DebugUtil.debug("Exiting Task triggerNotifyDependentTaskAction");

			}


	/**
	 * Returns true if view mode
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return - boolean
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public boolean showViewFields(Context context,String args[]) throws Exception
	{
		boolean showFields= false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String mode = (String) programMap.get("mode");
		if("view".equals(mode)){
			showFields = true;
		}
		return showFields;
	}

	/**
	 * Returns true if view mode
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return - boolean
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public boolean showEditFields(Context context,String args[]) throws Exception
	{
		boolean showFields= false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String mode = (String) programMap.get("mode");
		if("edit".equals(mode)){
			showFields = true;
		}
		return showFields;
	}


	/**
	 * This method is used to get the Owner Name
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - MapList
	 *            containing the objects list
	 * @return String
	 * @throws Exception
	 *             if the operation fails
	 * @since PMC V6R2008-1
	 */
	public String getOwnerName(Context context, String args[]) throws Exception
	{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		com.matrixone.apps.common.Person person =
				(com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		task.setId(objectId);
		String projectOwner = task.getInfo(context, DomainConstants.SELECT_OWNER);
		String ownerId = person.getPerson(context, projectOwner).getId();
		person.setId(ownerId);
		StringList busSelects = new StringList(2);
		busSelects.add(person.SELECT_LAST_NAME);
		busSelects.add(person.SELECT_FIRST_NAME);
		Map personFullNameMap = person.getInfo(context, busSelects);
		String strLastName = (String) personFullNameMap.get(person.SELECT_LAST_NAME);
		String strFirstName = (String) personFullNameMap.get(person.SELECT_FIRST_NAME);
		String personName = strLastName + ", " + strFirstName;
		return personName;
	}


	public String getActualDuration(Context context, String args[])throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");

		Task task = (Task)DomainObject.newInstance(context, DomainConstants.TYPE_TASK, ProgramCentralConstants.PROGRAM);
		task.setId(objectId);

		String taskActualDuration = task.getInfo(context,task.SELECT_TASK_ACTUAL_DURATION);
		String strI18DurationUnit = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Days", context.getSession().getLanguage());

		return (taskActualDuration + " " + strI18DurationUnit) ;
	}





	/**
	 * checks whether context user have the previllage to access External Test.
	 * Used to display/not display action commands in Task Dependency table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	public boolean hasAccessToExternalTask(Context context, String[] args)  throws Exception
	{
		HashMap  programMap   = (HashMap) JPO.unpackArgs(args);
		String   objectId     = (String) programMap.get("objectId");
		DomainObject dom = DomainObject.newInstance(context, objectId);
		HashMap parentMap = getParentProjectMap(context,dom);
		String strProjectType = (String)parentMap.get(DomainConstants.SELECT_TYPE);
		if(strProjectType!=null && strProjectType.equalsIgnoreCase(DomainConstants.TYPE_PROJECT_TEMPLATE)){
			return false;
		}
		return true;
	}

	/**
	 * get the Subtypes of the passed aparemeter of Project.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */

	private StringList getAllProjectSubTypeNames(Context context, String type) throws FrameworkException
	{
		StringList subTypeList = new StringList();
		//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:start
		String sCommandStatement = "print type $1 select $2 dump $3";
		String subTypes =  MqlUtil.mqlCommand(context, sCommandStatement,type, "derivative", "|");
		//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:End

		if("".equalsIgnoreCase(subTypes)){
			subTypeList.addElement(type);
			return subTypeList;
		} else {
			subTypes = subTypes+"|"+type;
		}
		subTypeList = FrameworkUtil.split(subTypes, "|");
		return subTypeList;
	}

	/**
	 * Where : In the Structure Browser, Updating Owner in Edit mode
	 * How : Get the objectId from argument map update with "New Value" owner.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "columnMap"
	 *        1 - String containing the "paramMap"
	 *        @param args holds the following input arguments:
	 *          0 - String containing "objectId"
	 *          1 - String containing "New Value"
	 * @returns None
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */
	public void updateOwner(Context context, String[] args) throws Exception {
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap columnMap = (HashMap) inputMap.get("columnMap");
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		String newOwnerId = (String) paramMap.get("New Value");

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		task.setId(objectId);
		// DomainObject assignee = PersonUtil.getPersonObject(context, newOwnerId);

		DomainObject assignee =null;
		try {
			assignee = PersonUtil.getPersonObject(context, newOwnerId);
		}
		catch(Exception e) {
			assignee = DomainObject.newInstance(context, newOwnerId);
		}
		String newOwner = assignee.getInfo(context, assignee.SELECT_NAME);

		if (task.isKindOf(context, DomainConstants.TYPE_TASK_MANAGEMENT))
		{
			//Get the current owner's name
			String currentOwner = task.getInfo(context, task.SELECT_OWNER);
			String currentOwnerRelId = null;
			//Get the new user's name

			// go through the assigneesList to check whether the new owner is already in the list
			//and determine the relationship id of the currentOwner
			StringList busSelects = new StringList(2);
			busSelects.add(assignee.SELECT_ID);
			busSelects.add(assignee.SELECT_NAME);
			StringList relSelects = new StringList(2);
			relSelects.add(assignee.SELECT_RELATIONSHIP_ID);
			MapList assigneesList = task.getAssignees(context, busSelects, relSelects, null);

			boolean assigneeInList = false;
			Iterator assigneesListItr = assigneesList.iterator();
			while(assigneesListItr.hasNext()) {
				Map assigneeMap = (Map) assigneesListItr.next();

				if(newOwnerId.equals((String) assigneeMap.get(assignee.SELECT_ID))) {
					assigneeInList = true;
				}
				if(currentOwner.equals((String) assigneeMap.get(assignee.SELECT_NAME))) {
					currentOwnerRelId = (String) assigneeMap.get(assignee.SELECT_RELATIONSHIP_ID);
				}
			}  //end while

				//Change the owner of the task
				task.setOwner(context, newOwner);

			//Commented:IR-126842V6R2013x:start
				//Must remove previous owner from assignee list
				/*if (currentOwnerRelId != null)
        {
          task.removeAssignee(context, currentOwnerRelId);
        }
        //Add the new owner to the assigneesList
        if(!assigneeInList) {
           task.addAssignee(context, newOwnerId, null);
            }*/
				//End
		}
		else if (task.isKindOf(context, DomainConstants.TYPE_PROJECT_MANAGEMENT))
		{
			com.matrixone.apps.program.ProjectSpace projectSpace = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
			projectSpace.setId(objectId);
			projectSpace.setOwner(context, newOwner);
		}
	}
	/**
	 * getProjectRole - This Method indicates the role the person serves for the project.
	 * Used for PMCProjectMemberSummary table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *    objectList - Contains a MapList of Maps which contains object names
	 * @return Vector of "Project Role" values for each row
	 * @throws Exception if the operation fails
	 * @since PMC V6R2008-1
	 */
	public String getProjectRole(Context context, String[] args)
			throws Exception
			{

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String objectId = (String) paramMap.get("objectId");
		String strMode=(String) requestMap.get("mode");
		String sLanguage = context.getSession().getLanguage();

		StringList slProjectRoles = new StringList();
		StringList slIntProjectRoles = new StringList();
		StringList rolesList = new StringList();
		task.setId(objectId);

		String actualType = task.getType(context);
		// Modified:22-Jul-09:nzf:R208:PRG:Bug:375210
		boolean isProjectSpace = task.isKindOf(context,DomainConstants.TYPE_PROJECT_SPACE);
		StringBuffer returnString=new StringBuffer();
		if(!isProjectSpace){
			// End:R208:PRG:Bug:375210
			MapList typeMapList = mxType.getAttributes(context, actualType);
			Iterator typeMapListItr = typeMapList.iterator();
			//String i18nSelectedRole = null;
			//Modified:29-Apr-09:WQY:R207:PRG Bug :373332
			String strSelectedRole = "";
			while(typeMapListItr.hasNext())
			{
				Map item = (Map) typeMapListItr.next();
				String attrName = (String) item.get("name");
				String attrType = (String) item.get("type");
				if ( attrName.equals( task.ATTRIBUTE_PROJECT_ROLE) )
				{
					rolesList = (StringList)item.get("choices");
					String sChoice;
					String showRDORoles = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.showRDORoles");
					if(showRDORoles!= null && !"true".equalsIgnoreCase(showRDORoles))
					{
						//To filter out any Role choices which have a symbolic name and start with "role_"
						Iterator roleItr = rolesList.iterator();
						while(roleItr.hasNext())
						{
							if(((String)roleItr.next()).startsWith("role_"))
							{
								roleItr.remove();
							}
						}
					}
					StringItr rolesList2 = new StringItr(rolesList);
					while(rolesList2.next())
					{
						String sProjectRole = (String)rolesList2.obj();
						// IR Fix 310707 - End
						if(sProjectRole.startsWith("role_"))
						{
							String sIntProjectRole = PropertyUtil.getSchemaProperty(context, sProjectRole);
							if(sIntProjectRole == null)
							{
								sIntProjectRole = sProjectRole;
							}
							else
							{
								sIntProjectRole = i18nNow.getRoleI18NString(sIntProjectRole, sLanguage);
							}
							//Added:3-Mar-10:VF2:R209:PRG Bug :036275
							if(rolesList.contains(sIntProjectRole))
							{
								if (!"".equals(sIntProjectRole)) {
									sIntProjectRole += EnoviaResourceBundle.getProperty(context, "ProgramCentral",
											"emxProgramCentral.MemberRoles.Suffix.RDO", context.getSession().getLanguage());
								}
							}
							//End:3-Mar-10:VF2:R209:PRG Bug :036275
							//Added:29-Apr-09:WQY:R207:PRG Bug :373332
							slProjectRoles.add(sProjectRole);
							slIntProjectRoles.add(sIntProjectRole);
							//End:R207:PRG Bug :373332

						}
						else
						{
							//Added:29-Apr-09:WQY:R207:PRG Bug :373332
							slProjectRoles.add(sProjectRole);
							slIntProjectRoles.add(i18nNow.getRangeI18NString(task.ATTRIBUTE_PROJECT_ROLE,sProjectRole, sLanguage));
							//End:R207:PRG Bug :373332
						}
					}
				}
			}
			//Modified:29-Apr-09:WQY:R207:PRG Bug :373332
			strSelectedRole = (String)task.getInfo(context, "attribute[" + ATTRIBUTE_PROJECT_ROLE + "]");
			//Added:3-Mar-10:VF2:R209:PRG Bug :036275
			String roleSelected = strSelectedRole;
			//End:3-Mar-10:VF2:R209:PRG Bug :036275
			if(strSelectedRole != null && !"".equals(strSelectedRole))
			{
				if(strSelectedRole.startsWith("role_"))
				{
					strSelectedRole = PropertyUtil.getSchemaProperty(context, strSelectedRole);
				}
			}
			if("edit".equals(strMode))
			{
				returnString.append("<select name=ProjectRole>");
				int i=0;
				while(i<slProjectRoles.size())
				{
					String role=XSSUtil.encodeForHTML(context,(String)slProjectRoles.get(i));
					//Added:29-Apr-09:WQY:R207:PRG Bug :373332
					String strIntRole = XSSUtil.encodeForHTML(context,(String)slIntProjectRoles.get(i));
					//Added:3-Mar-10:VF2:R209:PRG Bug :036275 added roleSelected.equalsIgnoreCase(role) in if clause
					if(strSelectedRole!=null && (strSelectedRole.equalsIgnoreCase(role) || roleSelected.equalsIgnoreCase(role))){
						returnString.append("<option value='"+role+"' selected>"+strIntRole+"</option>");
						//End:R207:PRG Bug :373332
					}
					else{
						returnString.append("<option value='"+role+"'>"+strIntRole+"</option>");

					}
					i++;
				}
				returnString.append("</select>");
			}
			else
			{
				//Modified:3-Mar-10:VF2:R209:PRG Bug :036275
				int nIndex = slProjectRoles.indexOf(roleSelected);
				if(nIndex > -1) {
					String strIntRole = (String)slIntProjectRoles.get(nIndex);
					returnString.append(strIntRole);
				}
				//End:3-Mar-10:VF2:R209:PRG Bug :036275
			}
			// Modified:22-Jul-09:nzf:R208:PRG:Bug:375210
		}
		if(!isProjectSpace){
			return  returnString.toString();
		}else
			return "";
		// End:R208:PRG:Bug:375210
			}

	/**
	 * updateDependency - This Method will update Dependency in WBS SB from
	 *   the Edit menu.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since PMC V6R2009-1.0
	 */
	public boolean updateDependency(Context context,String args[]) throws Exception {

		String languageStr = context.getSession().getLanguage();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String objectId = (String) paramMap.get("objectId");// get the task Id
		String newValue = (String) paramMap.get("New Value");//Get the dependency entered by the user.Eg:Project1:1:FS+5, 2:SF-5, 4:SS
		String oldValue = (String) paramMap.get("Old Value");
		Map projectTaskMap = (Map)paramMap.get("projectTaskMap");
		String calledFrom = (String) paramMap.get("calledFrom");
		String projectID = (String) requestMap.get("projectID"); // Get the project id
		boolean isProjectScheduledFromFinishDate = false;
		boolean isCalledFromExperimentCompareUI = false;

		final String SELECT_TASK_SEQUENCE_ORDER = "to.to[Subtask].attribute[Sequence Order]";
		final String SELECT_TASK_LAGTIME_VALUE = "attribute[Lag Time].inputvalue";
		final String SELECT_TASK_LAGTIME_UNIT = "attribute[Lag Time].inputunit";
		DependencyRelationship dependency = (DependencyRelationship) DomainRelationship.newInstance(context,RELATIONSHIP_DEPENDENCY);
		final String SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD = "attribute[" + ATTRIBUTE_ESTIMATED_DURATION_KEYWORD + "]";

		if(ProgramCentralUtil.isNullString(projectID)){
			//IF THERE IS NO PROJECT THEN GET THE TEMPLATE ID
			projectID = (String) requestMap.get("objectId");
		}

		//For WhatIf functionality:start
		StringList slSelectable = new StringList();
		slSelectable.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
		slSelectable.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
		slSelectable.addElement(ProgramCentralConstants.SELECT_PROJECT_ID);
		slSelectable.addElement(ProgramCentralConstants.SELECT_PROJECT_NAME);
		slSelectable.addElement(SELECT_CURRENT);

		Task taskObject = (Task) DomainObject.newInstance(context,TYPE_TASK,PROGRAM);
		taskObject.setId(objectId);
		Map <String,String>taskInfoMap = taskObject.getInfo(context, slSelectable);
		//Start of code for Bug No 360412
		String dependentTaskState =  taskInfoMap.get(SELECT_CURRENT);
		String projectName = taskInfoMap.get(ProgramCentralConstants.SELECT_PROJECT_NAME);
		
		if(ProgramCentralUtil.isNotNullString(taskInfoMap.get(ProgramCentralConstants.SELECT_PROJECT_ID))){
			projectID = taskInfoMap.get(ProgramCentralConstants.SELECT_PROJECT_ID);
		}
		if("WhatIfCompareView".equals(calledFrom)){
			isCalledFromExperimentCompareUI = true;
		}

		StringList projectSelectables = new StringList(SELECT_TYPE);
		projectSelectables.addElement(SELECT_ATTRIBUTE_SCHEDULE_FROM);
		projectSelectables.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
		projectSelectables.addElement(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
		projectSelectables.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		projectSelectables.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

		DomainObject masterProject = DomainObject.newInstance(context, projectID);
		Map projectInfo = masterProject.getInfo(context, projectSelectables);

		String projectScheduledFrom = (String)projectInfo.get(SELECT_ATTRIBUTE_SCHEDULE_FROM);
		String projectType = (String)projectInfo.get(SELECT_TYPE);
		String isKindofProjectSPace = (String)projectInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
		boolean isTaskManagement = "TRUE".equalsIgnoreCase((String)projectInfo.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT));

		if("Project Finish Date".equalsIgnoreCase(projectScheduledFrom)){
			isProjectScheduledFromFinishDate = true;
		}
		String dependencyType			= EMPTY_STRING;
		HashMap oldDependantTasks 		= new HashMap();
		HashMap<String, Map> oldDependantTaskInfo = new HashMap<String, Map>();
		HashMap connectionKeywordMap 	= new HashMap();
		HashMap addPredMap    			= new HashMap();
		HashMap modifyPredMap   		= new HashMap();
		ArrayList deleteTasks 			= new ArrayList();
		StringList busSelects = new StringList(5);
		StringList relSelects = new StringList(5);
		busSelects.add(SELECT_ID);
		busSelects.add(SELECT_PROJECT_ID_FROM_TASK);
		busSelects.add(SELECT_PREDECESSOR_TYPES);
		busSelects.add(SELECT_PROJECT_NAME_FROM_TASK);
		busSelects.add(SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER);

		relSelects.add(SELECT_TASK_LAGTIME_VALUE);
		relSelects.add(SELECT_TASK_LAGTIME_UNIT);
		relSelects.add(SELECT_TASK_SEQUENCE_ORDER);
		relSelects.add(DependencyRelationship.SELECT_DEPENDENCY_TYPE);
		relSelects.add(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);

		//get the existing dependant tasks for the selected tasks
		MapList predecessorList = taskObject.getPredecessors(context, busSelects, relSelects, null);
		Iterator predecessorItr = predecessorList.iterator();
		StringList oldValueList = new StringList();
		Map<String, MapList> projectTaskCache = new HashMap<String, MapList>();
		try {
			if(newValue!=null) {
				//loop the dependant tasks to add them to old dependant tasks map which can be used to distinguish between new and
				// exiting dependant tasks.Also the relationship ids are added to delete arraylist to delete the undesired dependancies
				while (predecessorItr.hasNext()) {
					Map predecessorObj = (Map) predecessorItr.next();
					String predecessorId = (String) predecessorObj.get(SELECT_ID);
					String connectionId = (String) predecessorObj.get(DependencyRelationship.SELECT_ID);
					String predecessorProjectId = (String) predecessorObj.get(SELECT_PROJECT_ID_FROM_TASK);
					String dependencyProjectName =(String)predecessorObj.get(SELECT_PROJECT_NAME_FROM_TASK);
					String strOldValue = ProgramCentralConstants.EMPTY_STRING;
					if(ProgramCentralUtil.isNotNullString(projectName) && !projectName.equalsIgnoreCase(dependencyProjectName)) {
						strOldValue=strOldValue+ dependencyProjectName + ":";
					}
					strOldValue = strOldValue +(String) predecessorObj.get(SELECT_TASK_SEQUENCE_ORDER);
					strOldValue = strOldValue +":" +(String) predecessorObj.get(dependency.SELECT_DEPENDENCY_TYPE);
					String strLagTime = (String) predecessorObj.get(SELECT_TASK_LAGTIME_VALUE);
					if(Task.parseToDouble(strLagTime)<0) {
						strOldValue = strOldValue + strLagTime;
					}
					else {
						strOldValue = strOldValue +"+" + strLagTime ; //check for - lag time As well
					}
					strOldValue = strOldValue +" " +(String) predecessorObj.get(SELECT_TASK_LAGTIME_UNIT);
					oldValueList.add(strOldValue);

					if(isProjectScheduledFromFinishDate && !projectID.equals(predecessorProjectId) && isCalledFromExperimentCompareUI){
						continue;
					}
					String strDurationKeyword = (String) predecessorObj.get(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
					oldDependantTasks.put(predecessorId,connectionId);
					oldDependantTaskInfo.put(predecessorId, predecessorObj);
					connectionKeywordMap.put(connectionId, strDurationKeyword);
					deleteTasks.add(connectionId);
				}
				// As the value that comes from dependency column is coma sepearted with dependencies, tokenize each dependency
				// Eg: Project1:1:FS+5, 2:SF-5, 4:SS
				String[] newDependencyArray = newValue.split(ProgramCentralConstants.COMMA);

				for (int i=0;i<newDependencyArray.length;i++) {

					String dependencyValue = newDependencyArray[i];
					if (ProgramCentralUtil.isNullString(dependencyValue)) {
						continue;
					}
					String taskSeqNumber = EMPTY_STRING;
					String projName =EMPTY_STRING;
					dependencyType=EMPTY_STRING;
					String lagTime=EMPTY_STRING;
					String taskID=EMPTY_STRING;
					boolean extProject = false;
					String slack=EMPTY_STRING;
					Double roundupLagTime=0.0;
					//Split the value using colon ':'
					String[] dependencyComponents = dependencyValue.split(" *: *");
					//Check to see if there are just 2 elements, which means that is an interal task dependency.  Eg:2:SF-5
					//Get the task sequence number and type of dependency.
					if(dependencyComponents.length == 2) {
						taskSeqNumber = dependencyComponents[0].trim();
						dependencyType  = dependencyComponents[1].trim();
					}
					//If there are three elements in the array, which means that is an external task dependency.  Eg:Project1:1:FS+5
					//Get the external project name, task sequence number and type of dependency and set the boolean extProject to true
					else if(dependencyComponents.length ==3) {
						projName    	= dependencyComponents[0].trim();
						taskSeqNumber 	= dependencyComponents[1].trim();
						dependencyType  = dependencyComponents[2].trim();
						extProject 		= true;
					}
					//Get the slack time if exiting
					if(dependencyType.length() >2) {
						String lagtimeUnit = "d";
						//if input has no 'd' or 'h' mentioned, append d to the input.
						if(!dependencyType.contains("d") && !dependencyType.contains("h")){
							dependencyType += ProgramCentralConstants.SPACE + "d";
						}else if(dependencyType.contains("h")){
							lagtimeUnit = "h"; 
						}
						lagTime = dependencyType.substring(2);
						//below changes is done for rounding up slak time.
						String lagTimeSign = dependencyType.substring(2,3);
						slack = dependencyType.substring(2,lagTime.length());
						roundupLagTime = Task.parseToDouble(slack);
						roundupLagTime = (double) Math.round(roundupLagTime * 100)/100 ;
						lagTime         =roundupLagTime.toString();
						StringBuffer sbLagTime = new StringBuffer();
                    	if("+".equalsIgnoreCase(lagTimeSign)){
						sbLagTime.append(lagTimeSign);
                    	}
						sbLagTime.append(lagTime);
						sbLagTime.append(" ");
						sbLagTime.append(lagtimeUnit);
						lagTime = sbLagTime.toString();
					}
					dependencyType = dependencyType.substring(0,2); //get the type of dependency
					dependencyType = dependencyType.toUpperCase();
					String sTypeWhere = EMPTY_STRING;
					MapList taskList = null;
					StringList typeSelects = new StringList();
					StringList relationSelects = new StringList(2);
					relationSelects.add("attribute[" + ATTRIBUTE_SEQUENCE_ORDER + "]");
					typeSelects.add(SELECT_ID);
					typeSelects.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);

					//If external dependency is to be created get the project Id pertaining to the project name else project Id remains as selected tasks parent
					// If external dependency is to be created and if no project with entered name, alert the exception to the user
					DomainObject dObj = masterProject;
					boolean isdObjKindofTaskManagement = false;
					if(extProject) {
						if(isProjectScheduledFromFinishDate && ProgramCentralConstants.TYPE_EXPERIMENT.equals(projectType) && isCalledFromExperimentCompareUI){   // this will avoid adding external dependency to Experiment task
							continue;
						}
						boolean expandSubType = false;
						if("TRUE".equalsIgnoreCase(isKindofProjectSPace)){
							expandSubType = true;
						}
						String typePatternPrj = ProgramCentralConstants.TYPE_PROJECT_SPACE + "," +ProgramCentralConstants.TYPE_PROJECT_CONCEPT;
						MapList listOfProjects = DomainObject.findObjects(context,
								typePatternPrj,
								projName,
								ProgramCentralConstants.QUERY_WILDCARD,
								ProgramCentralConstants.QUERY_WILDCARD,
								ProgramCentralConstants.QUERY_WILDCARD,
								null,
								expandSubType,
								typeSelects);
						Iterator mapIterator = listOfProjects.iterator();

						if(mapIterator.hasNext()) {
							Map projMap = (Map)mapIterator.next();
							String prjID = (String)projMap.get(SELECT_ID);
							isdObjKindofTaskManagement = "TRUE".equalsIgnoreCase((String)projMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT));
							dObj = DomainObject.newInstance(context);
							dObj.setId(prjID);
						} else {
							String errMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
											 "emxProgramCentral.Common.Dependency.InvalidExternalProject", languageStr);
							errMessage = StringResource.format(errMessage, new String[]{"ProjectName"}, new String[]{projName});
							throw new FrameworkException(errMessage);
						}
					}
					//From the enetered task sequence get the task Id. If no task with given sequence alert the user.

					//Added:24-Apr-09:nr2:R207:PRG Bug :371521
					//Passing the Project if root node is task or its subtype
					if ((extProject && isdObjKindofTaskManagement) || isTaskManagement) {

						String prjID = Task.getParentProject(context,dObj);
						dObj.setId(prjID);
					}
					//End:R207:PRG Bug :371521
					String dObjId = dObj.getId(context);
					if(projectTaskMap !=null && isCalledFromExperimentCompareUI && projectTaskMap.containsKey(dObjId)){
						taskList = (MapList)projectTaskMap.get(dObj.getId(context));
					} else {

						if(!projectTaskCache.containsKey(dObjId)){
							taskList = dObj.getRelatedObjects(context,
									DomainConstants.RELATIONSHIP_SUBTASK,
									DomainConstants.TYPE_TASK_MANAGEMENT,
									typeSelects,
									relationSelects,
									false,
									true,
									(short)0,
									null,
									null,
									0);
							projectTaskCache.put(dObjId, taskList);
						} else {
							taskList = projectTaskCache.get(dObjId);
						}
					}
					Iterator mapItr = taskList.iterator();
					while(mapItr.hasNext()) {
						Map map = (Map)mapItr.next();
						String relAttribute = (String)map.get("attribute[" + ATTRIBUTE_SEQUENCE_ORDER + "]");
						if(relAttribute!=null && relAttribute.equals(taskSeqNumber)) {
							taskID = (String)map.get(SELECT_ID);
						}
					}
					if(ProgramCentralUtil.isNullString(taskID)) {
						String errMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
										"emxProgramCentral.Common.Dependency.InvalidSequenceNumber", languageStr);
						errMessage = StringResource.format(errMessage, new String[]{"SequenceNo"}, new String[]{taskSeqNumber});
						throw new FrameworkException(errMessage);
					}
					HashMap attributes = new HashMap();
					attributes.put(DependencyRelationship.ATTRIBUTE_LAG_TIME,lagTime);
					attributes.put(DependencyRelationship.ATTRIBUTE_DEPENDENCY_TYPE,dependencyType);
					String connectionID = (String)oldDependantTasks.get(taskID);
					//If dependency already exists between tasks, add the rel id and attributes to modify map and delete the rel id from Delete arraylist
					if(ProgramCentralUtil.isNotNullString(connectionID)) {

						Map predecessorObj = oldDependantTaskInfo.get(taskID);
						String predecessorDependencyType = (String) predecessorObj.get(dependency.SELECT_DEPENDENCY_TYPE);
						String strLagTime = (String) predecessorObj.get(SELECT_TASK_LAGTIME_VALUE);
						String  strLagTimeUnit = (String) predecessorObj.get(SELECT_TASK_LAGTIME_UNIT);
						String strDurationKeyword = (String) predecessorObj.get(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
						boolean hasSameDurationKeyword = false;
						if(ProgramCentralUtil.isNotNullString(strDurationKeyword) && ProgramCentralUtil.isNotNullString((String)connectionKeywordMap.get(connectionID)) && strDurationKeyword.equalsIgnoreCase((String)connectionKeywordMap.get(connectionID))){
							hasSameDurationKeyword = true;
						}else if(ProgramCentralUtil.isNullString(strDurationKeyword) && ProgramCentralUtil.isNullString((String)connectionKeywordMap.get(connectionID))){
							hasSameDurationKeyword = true;
						}

						if(Task.parseToDouble(strLagTime) > 0){
							strLagTime = "+"+strLagTime+" "+strLagTimeUnit;     //append the "+" sign to lag time if it's greater than 0
						}else {
							strLagTime = strLagTime+" "+strLagTimeUnit;
						}
						attributes.put(ATTRIBUTE_ESTIMATED_DURATION_KEYWORD,connectionKeywordMap.get(connectionID));
						checkDurationKeyword(context, attributes, objectId);
						if(!(lagTime.equals(strLagTime) && dependencyType.equalsIgnoreCase(predecessorDependencyType) && hasSameDurationKeyword)){
							modifyPredMap.put(connectionID,attributes);
						}
						deleteTasks.remove(connectionID);
					}
					//if no dependency exiting between tasks add them to add Map
					else {
						addPredMap.put(taskID,attributes);
					}
				}

				//Added:26-Feb-09:yox:R207:PRG:Bug :369516

				String [] newValueArray = newValue.split(",");
				boolean isValidToAdd = true;
				int size = newValueArray.length;

				for(int i=0;i<size;i++)
				{
					if(!oldValueList.contains(newValueArray[i]))
					{
						String newlyAddedDependency = newValueArray[i].toUpperCase();
						if(newlyAddedDependency.contains("FS")||newlyAddedDependency.contains("SS"))
						{
							isValidToAdd = false;
							break;
						}

					}
				}
				// Added:26-Feb-09:yox:R207:PRG:Bug :369516

                if(!isCalledFromExperimentCompareUI){
					isValidDependency(context, dependentTaskState,dependencyType,isValidToAdd);
                }

				//isValidDependency(context,dependentTaskState,dependencyType);
				//End:R207:PRG :Bug :369516
				ContextUtil.startTransaction(context, true);
				String[] deleteArray = new String[deleteTasks.size()];
				for(int i=0;i<deleteTasks.size();i++) {
					deleteArray[i] = (String)deleteTasks.get(i);
				}
				//ADDED FOR BUG 355394
				String strProjStartDate = (String)projectInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
				String strProjEndDate 	= (String)projectInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

				Date dtProjStartDate = eMatrixDateFormat.getJavaDate(strProjStartDate);
				Date dtProjEndDate 	 = eMatrixDateFormat.getJavaDate(strProjEndDate);
				Set set = oldDependantTasks.entrySet();
				//ADDED FOR BUG 355394

				if(deleteArray!=null && deleteArray.length>0) {
					//Code added for not allowing the remove the dependency relationship if having interface Configuration Usage set on it.
					String connectionId = DomainObject.EMPTY_STRING;
					for(int m=0 ;m<deleteArray.length;m++){
						connectionId = deleteArray[m];
						String INTERFACE_CONFIGURATION_USAGE = PropertyUtil.getSchemaProperty(context,"interface_ConfigurationUsage");
						StringList relationshipSelects = new StringList();
						relationshipSelects.add(SELECT_RELATIONSHIP_ID);
						relationshipSelects.add("interface["+INTERFACE_CONFIGURATION_USAGE +"]");
						DomainRelationship domRel = new DomainRelationship(connectionId);
						Hashtable relData = (Hashtable)domRel.getRelationshipData(context, relationshipSelects);
						StringList pcIdList =  new StringList();
						pcIdList  = (StringList)relData.get("interface["+INTERFACE_CONFIGURATION_USAGE +"]");
						
						if(!pcIdList.isEmpty()){
							String strVal =(String)pcIdList.get(0);
							if(null!=pcIdList && strVal.equalsIgnoreCase("FALSE")){
																
								//if(isCalledFromExperimentCompareUI){
									taskObject.removePredecessors(context, new String[]{connectionId}, false);
								/*}else{
									taskObject.removePredecessor(context, connectionId);
									//taskObject.removePredecessors(context, new String[]{connectionId}, false);
									//DomainRelationship.disconnect(context, connectionIds);
								}*/
                                    
							}else if(null!=pcIdList){                                
								String sErrorMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
										"emxProgramCentral.Milestone.NoUpdateAllowed", languageStr);
								emxContextUtil_mxJPO.mqlError(context,sErrorMsg);
							}
						}else{
							//taskObject.removePredecessors(context, deleteArray);
                        	taskObject.removePredecessors(context, deleteArray,false);
							break;
						}
					}
					StringList list = new StringList();
					list.add(objectId);
					TaskDateRollup.validateTasks(context,list);
				}


				// modify the task dependencies present in modify map
				if(!modifyPredMap.isEmpty()){
					modifyPredMap.put("isValidToAdd", Boolean.toString(isValidToAdd));
					if(isCalledFromExperimentCompareUI){
						taskObject.modifyPredecessors(context, (Map) modifyPredMap,false);
					} else {
						taskObject.modifyPredecessors(context, (Map) modifyPredMap,true);
					}
				}
				// create new dependencies between selected task and tasks in add map
				if(!addPredMap.isEmpty()){
					addPredMap.put("isValidToAdd", Boolean.toString(isValidToAdd));
					if(isCalledFromExperimentCompareUI){
						taskObject.addPredecessors(context, (Map) addPredMap, false);
					} else {
						taskObject.addPredecessors(context, (Map) addPredMap, true);
					}
				}
				ContextUtil.commitTransaction(context);
			}
		} catch (Exception e) {
			e.printStackTrace();
			emxContextUtilBase_mxJPO.mqlNotice(context,  e.toString());
			ContextUtil.abortTransaction(context);
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * updateProjectRole - This Method will update Project Role in WBS SB from
	 *   the Edit menu.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return boolean
	 * @since PMC V6R2008-2.0
	 */
	public boolean updateProjectRole(Context context,String args[]) throws Exception
	{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String ProjectRole[] = (String[]) requestMap.get("ProjectRole");
		String strRole = ProjectRole[0];
		String objectId = (String) paramMap.get("objectId");
		task.setId(objectId);
		task.setAttributeValue(context, task.ATTRIBUTE_PROJECT_ROLE, strRole);
		return true;
	}
	/**
	 * cutPasteTasksInWBS - This Method will move the task in the WBS SB from
	 *   the Edit menu.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return Void
	 * @throws Exception if the operation fails
	 * @since PMC V6R2008-2.0
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public Map cutPasteTasksInWBS(Context context, String[] args) throws Exception
	{
		Map returnHashMap = new HashMap();
		MapList mlItems = new MapList();
		StringList rollupObjectList = new StringList();
		StringList createToActiveStateList = new StringList();
		createToActiveStateList.add(STATE_PROJECT_SPACE_CREATE);
		createToActiveStateList.add(STATE_PROJECT_SPACE_ASSIGN);
		createToActiveStateList.add(STATE_PROJECT_SPACE_ACTIVE);
		//allow to do operations in latest version of PT which can be in "Inactive" state.
		createToActiveStateList.add(ProjectTemplate.STATE_PROJECT_TEMPLATE_INACTIVE);

		Map programMap = (HashMap) JPO.unpackArgs(args);

		Map mParamMap = (Map)programMap.get("paramMap");
		String sRootTreeId = (String)mParamMap.get("objectId");

		Element rootElement = (Element)programMap.get("contextData");
		Element rootParent = rootElement.getParentElement();

		Map mValidityTestBasedOnObjectId = new HashMap();
		int validCutPasteOpeation = 0; //{0=validOperation, 1=InvalidOpearion}
		if(rootParent != null){
			mValidityTestBasedOnObjectId = validateCutPasteOperation(context, rootParent);
			validCutPasteOpeation = (int) mValidityTestBasedOnObjectId.get("validCutPasteOpeationValue");
		}

		String sParentOID = (String)rootElement.getAttributeValue("objectId");
		String strLastOperation = (String)rootElement.getAttributeValue("lastOperation");

		List lCElement = rootElement.getChildren();
		if(lCElement != null){

			StringList taskSelects = new StringList();
			taskSelects.add(SELECT_CURRENT);
			taskSelects.add(SELECT_NAME);
			taskSelects.add(DomainConstants.SELECT_POLICY);
			if("cut".equalsIgnoreCase(strLastOperation)){ //Added for Parent Task
				taskSelects.addElement(SELECT_PROJECT_ACCESS_LIST_ID_FOR_PROJECT);
				taskSelects.addElement(SELECT_PROJECT_ACCESS_LIST_ID_FOR_TASK);
				taskSelects.addElement("to["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].id");
				taskSelects.addElement("to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].id");
			}

			Task parentTask = (Task)DomainObject.newInstance(context,TYPE_TASK,PROGRAM); //Task to which to be Pasted.
			parentTask.setId(sParentOID);

			Map parentTaskInfoMap = parentTask.getInfo(context, taskSelects);

			if("cut".equalsIgnoreCase(strLastOperation)){ //Added for child Task
				taskSelects.addElement("to["+RELATIONSHIP_SUBTASK+"].id");
				taskSelects.addElement("to["+RELATIONSHIP_SUBTASK+"].type");
			}

			Map errorMap = new HashMap();
			String langStr = context.getSession().getLanguage();
			StringList slDependencyTaskNameList = new StringList();
        String strLanguage=context.getSession().getLanguage();

			Task childTask = (Task) DomainObject.newInstance(context, TYPE_TASK, PROGRAM);//Cut/Copied Task
			Iterator itrC  = lCElement.iterator();
			while(itrC.hasNext()){
				Element childCElement = (Element)itrC.next();
				String sObjectId = (String)childCElement.getAttributeValue("objectId");
				String sRelId = (String)childCElement.getAttributeValue("relId");
				String sRowId = (String)childCElement.getAttributeValue("rowId");
				String markup = (String)childCElement.getAttributeValue("markup");
				String spasteAbove = (String)childCElement.getAttributeValue("paste-above");
				String pasteAtId = null;

				childTask.clear();
				childTask.setId(sObjectId);

				Map childTaskInfoMap = childTask.getInfo(context, taskSelects);

				String parentTaskState = (String)parentTaskInfoMap.get(SELECT_CURRENT);
				String childTaskState = (String)childTaskInfoMap.get(SELECT_CURRENT);

				Integer value = (Integer) mValidityTestBasedOnObjectId.get(sObjectId);
				if( value != null && Integer.valueOf(value) > 0 ){
					errorMap.put("validCutPasteOpeation", validCutPasteOpeation);
					errorMap.put("objectId",sObjectId);
					continue;
				}
				if (spasteAbove != null){
					StringList objectList = FrameworkUtil.split(spasteAbove,"|");
					pasteAtId = (String)objectList.elementAt(0) ;
				}

    			if ("resequence".equals(markup)){
    				if(!createToActiveStateList.contains(childTaskState)){
    					String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.cutTask.CutOperationNotice", langStr);
    					return getMessageMap(context, message);
    				}
    				if(childTask.isMandatoryTask(context,sObjectId)){
    					String strTaskRequirement = (String)childTask.getAttributeValue(context, ATTRIBUTE_TASK_REQUIREMENT);
    					if(strTaskRequirement.equalsIgnoreCase("Mandatory")){
    						String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.cutTask.MandatoryTaskNotice", langStr);
    						return getMessageMap(context, message);
    					}
    				}
    				DomainRelationship domRel = new DomainRelationship(sRelId);
    				Map attribMap = generateSequenceInfoMap(context, parentTask, pasteAtId);
    				domRel.setAttributeValues(context,attribMap);

    				parentTask.reSequence(context, sParentOID);

    			} else if ("add".equals(markup)) {
                    String strParentTaskName = (String)parentTaskInfoMap.get(DomainConstants.SELECT_NAME);
                    String parentObjPolicy 		= (String)parentTaskInfoMap.get(DomainConstants.SELECT_POLICY);
                    
                    if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(parentObjPolicy)){
                    	String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                                "emxProgramcentral.Milestone.SubtaskCreationAlert", strLanguage);
                    	returnHashMap.put("Message",sErrMsg);
                        returnHashMap.put("Action", "ERROR");
                        return(returnHashMap);
                    }
					boolean isToshowDependencyMsg = parentTask.isToshowDependencyMessage(context, sParentOID);
					if(isToshowDependencyMsg) {
						if(!slDependencyTaskNameList.contains(strParentTaskName)) {
							slDependencyTaskNameList.add(strParentTaskName);
						}
					}

					if("cut".equalsIgnoreCase(strLastOperation)){//Add.Cut
						String strCheckRel = (String) childTaskInfoMap.get("to["+RELATIONSHIP_SUBTASK+"].id");
						String strCheckRelType = (String) childTaskInfoMap.get("to["+RELATIONSHIP_SUBTASK+"].type");

						if(ProgramCentralUtil.isNotNullString(strCheckRel) && !(TYPE_PROJECT_SPACE.equals(strCheckRelType))){
							String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Task.CannotBeAddedToMultipleParents", langStr);
							return getMessageMap(context, message);
						}

						if(!createToActiveStateList.contains(parentTaskState)){ //i.e. in Review, Complete or Archive
							String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Project.TaskInState3", langStr);
							return getMessageMap(context, message);
						}
						DomainRelationship domRel = DomainRelationship.connect(context, parentTask, RELATIONSHIP_SUBTASK, childTask);
						Map attribMap = generateSequenceInfoMap(context, parentTask, pasteAtId);
						domRel.setAttributeValues(context,attribMap);
						sRelId = domRel.getName();

						validateTaskPALConnection(context, parentTaskInfoMap, childTaskInfoMap);

						StringList taskStatesList = new StringList(createToActiveStateList);
						taskStatesList.add(STATE_PROJECT_SPACE_REVIEW);
						taskStatesList.add(STATE_PROJECT_SPACE_COMPLETE);
						int childTaskStateIndex = taskStatesList.indexOf(childTaskState);
						int parentTaskStateIndex = taskStatesList.indexOf(parentTaskState);

						if(childTaskStateIndex > parentTaskStateIndex &&
								STATE_PROJECT_TASK_ACTIVE.equalsIgnoreCase(childTaskState)){
							parentTask.setState(context, childTaskState);//Promote Parent Task.
						}else if(childTaskStateIndex < parentTaskStateIndex && STATE_PROJECT_TASK_ASSIGN.equalsIgnoreCase(childTaskState)){
							boolean demoteParent = true;
							MapList siblingTasksInfoList = getTasks(context, parentTask, 1, new StringList(SELECT_CURRENT), null);
							Iterator itr = siblingTasksInfoList.iterator();
							while(itr.hasNext()){
								Map siblingTaskInfoMap = (Map) itr.next();
								String siblingTaskState = (String) siblingTaskInfoMap.get(SELECT_CURRENT);
								if(!(STATE_PROJECT_TASK_CREATE.equalsIgnoreCase(siblingTaskState) || STATE_PROJECT_TASK_ASSIGN.equalsIgnoreCase(siblingTaskState))){
									demoteParent = false;
									break;
								}
							}
							if(demoteParent){
								parentTask.setState(context, STATE_PROJECT_TASK_ASSIGN);
							}
						}
						parentTask.reSequence(context, sParentOID);
						//parentTask.rollupAndSave(context);
						if(!rollupObjectList.contains(sParentOID)){
							rollupObjectList.add(sParentOID);
						}

					} else if("copy".equalsIgnoreCase(strLastOperation)){//Add.Copy
						if(ProgramCentralUtil.isNotNullString(sRootTreeId) &&
								checkIfProject(context,sRootTreeId) && sRootTreeId.equals(sObjectId)){
							String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Project.WBS.cutPaste.ErrorOnCopyProject", langStr);
							return getMessageMap(context, message);
						}

						if(!createToActiveStateList.contains(parentTaskState)) { // i.e. in Review, Complete or Archive state.
							String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Project.TaskInState3", langStr);
							return getMessageMap(context, message);
						}

						Map copyTaskMap = new HashMap();
						childTask.cloneTaskWithStructure(context, parentTask, null, copyTaskMap, true);
						DomainObject copyTask = (DomainObject)copyTaskMap.get(sObjectId);
						if(copyTask == null){
							String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.WBS.copypaste.project", langStr);
							return getMessageMap(context, message);
						}

						sObjectId = copyTask.getId(context);
						DomainObject clonedTask = DomainObject.newInstance(context,sObjectId);
						sRelId = clonedTask.getInfo(context,"to["+RELATIONSHIP_SUBTASK+"].id");
						DomainRelationship domRel1 = new DomainRelationship(sRelId);
						Map attribMap1 = generateSequenceInfoMap(context, parentTask, pasteAtId);
						domRel1.setAttributeValues(context,attribMap1);

						parentTask.reSequence(context, sParentOID);
						parentTask.rollupAndSave(context);
					}
				} else if ("cut".equals(markup)){
					if(validCutPasteOpeation > 0){
						String errorMessage = emxProgramCentralUtilClass.getMessage(context, "emxProgramCentral.Common.InvalidCutPasteOperation", null, null, null);
						return getMessageMap(context, errorMessage);
					}

					Map deletedTasksInfoMap = childTask.getRelatedObject(context,
							RELATIONSHIP_DELETED_SUBTASK,
							true,
							new StringList(),
							new StringList());
					if( deletedTasksInfoMap != null ){
						String message = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.cutTask.Notice");
						return getMessageMap(context, message);
					}

					if(!createToActiveStateList.contains(childTaskState)){
						String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.cutTask.CutOperationNotice", langStr);
						return getMessageMap(context, message);
					}

					if(childTask.isMandatoryTask(context,sObjectId)){
						String strTaskRequirement = (String)childTask.getAttributeValue(context, ATTRIBUTE_TASK_REQUIREMENT);
						if(strTaskRequirement.equalsIgnoreCase("Mandatory")){
							String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.cutTask.MandatoryTaskNotice", langStr);
							return getMessageMap(context, message);
						}
					}
					childTask.disconnectTasks(context, new String[]{sRelId}, sParentOID);
					//Demote parent if needed.
					if(STATE_PROJECT_TASK_ACTIVE.equalsIgnoreCase(childTaskState) && STATE_PROJECT_TASK_ACTIVE.equalsIgnoreCase(parentTaskState)){
						boolean demoteParent = true;
						MapList siblingTasksInfoList = getTasks(context, parentTask, 1, new StringList(SELECT_CURRENT), null);
						Iterator itr = siblingTasksInfoList.iterator();
						while(itr.hasNext()){
							Map siblingTaskInfoMap = (Map) itr.next();
							String siblingTaskState = (String) siblingTaskInfoMap.get(SELECT_CURRENT);
							if(!(STATE_PROJECT_TASK_CREATE.equalsIgnoreCase(siblingTaskState) || STATE_PROJECT_TASK_ASSIGN.equalsIgnoreCase(siblingTaskState))){
								demoteParent = false;
								break;
							}
						}
						if(demoteParent){
							parentTask.setState(context, STATE_PROJECT_TASK_ASSIGN);
						}
					}
					parentTask.rollupAndSave(context);
				}

				Map tempHashMap = new HashMap();
				tempHashMap.put("oid", sObjectId);
				tempHashMap.put("rowId", sRowId);
				tempHashMap.put("relid", sRelId);
				tempHashMap.put("markup", markup);
				mlItems.add(tempHashMap);
			}

			if(null != errorMap && errorMap.size()>0){
				String errorMessage = emxProgramCentralUtilClass.getMessage(context, "emxProgramCentral.Common.InvalidCutPasteOperation", null, null, null);
				return getMessageMap(context, errorMessage);
			}

			if(slDependencyTaskNameList.size() > 0) {
				String sKey[] = {"dependencyType1","dependencyType2"};
				String sValue[] = {Task.START_TO_FINISH, Task.FINISH_TO_FINISH};
				String warningKey = emxProgramCentralUtilClass.getMessage(context,"emxProgramCentral.WBS.cutpaste.removeDependency",sKey,sValue,null);
				emxContextUtilBase_mxJPO.mqlNotice(context, warningKey+"\\n"+slDependencyTaskNameList.toString());
			}
			
			if(!rollupObjectList.isEmpty()){
				Iterator<String> objectsTobeRolledUpIterator = rollupObjectList.iterator();
				while(objectsTobeRolledUpIterator.hasNext()){
					String objectId = objectsTobeRolledUpIterator.next();
					parentTask.clear();
					parentTask.setId(objectId);
					parentTask.rollupAndSave(context);
					
				}
		}
		}
		
		
		returnHashMap.put("changedRows", mlItems);
		returnHashMap.put("Action", "success");
		return(returnHashMap);
	}


	/**
	 *
	 * @param context
	 * @param key
	 * @return
	 * @throws FrameworkException
	 */
	private Map getMessageMap(Context context, String message) throws FrameworkException {
		Map<String, String> returnHashMap = new HashMap<String, String>();
		returnHashMap.put("Message", message);
		returnHashMap.put("Action", "ERROR");
		return returnHashMap;
	}


	/**
	 * It validates cut/copy-paste operation for selected rows and returns objectId to their valid/invalid
	 * operatin code(0-Valid, 1-Invalid).
	 *
	 * @param context
	 * @param rootParent
	 * @return mValidityTestBasedOnObjectId : A map of objectIds to their valid/invalid operatin code(0-Valid, 1-Invalid)
	 * @throws FrameworkException
	 */
	private Map validateCutPasteOperation(Context context, Element rootParent) throws FrameworkException {

		Map mValidityTestBasedOnObjectId = new HashMap();
		int validCutPasteOpeation = 0;
		List childList = rootParent.getChildren();

		for(int i = 0; (i < childList.size()); i++){
			Element e = (Element) childList.get(i);
			List ccElementList = e.getChildren();
			for(int j = 0; j < ccElementList.size(); j++){
				Element ccElement  = (Element) ccElementList.get(j);
				String operation = ccElement.getAttributeValue("markup");
				String objId = ccElement.getAttributeValue("objectId");

				if("cut".equalsIgnoreCase(operation)){
					validCutPasteOpeation++;
					if(mValidityTestBasedOnObjectId.containsKey(objId)){
						Integer value = (Integer) mValidityTestBasedOnObjectId.get(objId);
						mValidityTestBasedOnObjectId.put(objId,(Integer.valueOf(value))+1);
					}
					mValidityTestBasedOnObjectId.put(objId,1);
				} else if("add".equalsIgnoreCase(operation)){
					if(mValidityTestBasedOnObjectId.containsKey(objId)){
						Integer value = (Integer) mValidityTestBasedOnObjectId.get(objId);
						mValidityTestBasedOnObjectId.put(objId,(Integer.valueOf(value))-1);
					}
					validCutPasteOpeation--;
				}
				else{
					validCutPasteOpeation = 0;
				}
			}
		}

		mValidityTestBasedOnObjectId.put("validCutPasteOpeationValue", validCutPasteOpeation);

		return mValidityTestBasedOnObjectId;

	}


	/**
	 * Generates next sequence number and wbs level for the task to be pasted.
	 *
	 * @param context
	 * @param parent : To which the task to be pasted.
	 * @param atTaskId : Task above which the cut/copied task will be pasted.
	 * @return sequenceInfoMap : Map holds both sequence number and wbs for the task to be pasted
	 * @throws FrameworkException
	 */
	private Map generateSequenceInfoMap(Context context, TaskHolder parent, String atTaskId) throws FrameworkException {
		Map sequenceInfoMap = new HashMap();

		Map seqMap = Task.getNextSequenceInformation(context, parent, atTaskId);
		String nextSequence = (String) seqMap.get(KEY_SEQ_NUMBER);
		String nextWBS = (String) seqMap.get(KEY_WBS_NUMBER);

		sequenceInfoMap.put(ATTRIBUTE_SEQUENCE_ORDER , nextSequence);
		sequenceInfoMap.put(ATTRIBUTE_TASK_WBS , nextWBS);

		return sequenceInfoMap;
	}


	/**
	 * Checks if cut task and parent or task to which it is pasted are having same PAL (Project Access List) object.
	 * If not disconnect from exiting one and connects to Parent task PAL.
	 * @param parentTaskInfoMap
	 * @param childTaskInfoMap
	 * @throws Exception
	 */
	private void validateTaskPALConnection(Context context, Map parentTaskInfoMap, Map childTaskInfoMap) throws Exception {

		String childTaskId = (String) childTaskInfoMap.get(SELECT_ID);
		Task childTask = new Task(childTaskId);

		String projectAccessListId = (String)parentTaskInfoMap.get(SELECT_PROJECT_ACCESS_LIST_ID_FOR_PROJECT);
		String parentPalRelId = (String)parentTaskInfoMap.get("to["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].id");

		if(ProgramCentralUtil.isNullString(projectAccessListId)) {
			projectAccessListId = (String)parentTaskInfoMap.get(SELECT_PROJECT_ACCESS_LIST_ID_FOR_TASK);
			parentPalRelId = (String)parentTaskInfoMap.get("to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].id");
		}

		String childProjectAccessListId = (String)childTaskInfoMap.get(SELECT_PROJECT_ACCESS_LIST_ID_FOR_PROJECT);
		String childPalRelId = (String)childTaskInfoMap.get("to["+RELATIONSHIP_PROJECT_ACCESS_LIST+"].id");
		boolean isProject = true;

		if(ProgramCentralUtil.isNullString(childProjectAccessListId)) {
			childProjectAccessListId = (String)childTaskInfoMap.get(SELECT_PROJECT_ACCESS_LIST_ID_FOR_TASK);
			childPalRelId = (String)childTaskInfoMap.get("to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].id");
			isProject = false;
		}

		if(!projectAccessListId.equals(childProjectAccessListId) && !isProject) {

			StringList slBusSelect = new StringList(SELECT_ID);
			slBusSelect.addElement("to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].id");
			StringList slRelSelect = new StringList();

			MapList mlRelatedObjects = childTask.getRelatedObjects(context,
					RELATIONSHIP_SUBTASK,
					TYPE_TASK_MANAGEMENT,
					slBusSelect,
					slRelSelect,
					false,
					true,
					(short)0,
					"",
					"",
					0);

			String taskIds[] = new String[mlRelatedObjects.size()+1];
			String relIds[] = new String[mlRelatedObjects.size()+1];

			for(int i = 0; i < mlRelatedObjects.size(); i++) {
				Map taskMap = (Map)mlRelatedObjects.get(i);
				taskIds[i] = (String)taskMap.get(SELECT_ID);
				relIds[i] = (String)taskMap.get("to["+RELATIONSHIP_PROJECT_ACCESS_KEY+"].id");
			}

			taskIds[mlRelatedObjects.size()] = childTaskId;
			relIds[mlRelatedObjects.size()] = childPalRelId;

			DomainRelationship.disconnect(context,relIds);

			DomainRelationship.connect(context,
					new DomainObject(projectAccessListId),
					RELATIONSHIP_PROJECT_ACCESS_KEY,
					true,
					taskIds);
		}
	}


	/**
	 * getWBSProjectTemplateSubtasks - This Method will get the Tasks and sub tasks in Project Template
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return MapList
	 * @throws Exception if the operation fails
	 * @since PMC V6R2009-1.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWBSProjectTemplateSubtasks(Context context, String[] args) throws Exception
	{
		HashMap arguMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String) arguMap.get("objectId");

		//
		String strExpandLevel = (String) arguMap.get("expandLevel");

		short nExpandLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
		StringList ObjSelects=new StringList();
		MapList newList=new MapList();
		try
		{
			MapList mapList = (MapList) getWBSTasks(context,strObjectId,DomainConstants.RELATIONSHIP_SUBTASK,nExpandLevel);
			Iterator itr=  mapList.iterator();
			while (itr.hasNext())
			{
				Map map=(Map)itr.next();
				map.put("RowEditable","show");
				map.put("direction","from");
				newList.add(map);
			}
			//inform SB that the structure includes multi-level structure.
			HashMap hmTemp = new HashMap();
			hmTemp.put("expandMultiLevelsJPO", "true");
			newList.add(hmTemp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return newList;
	}


	/**
	 * This Method will return MapList of expanded WBS of Project with seleced Expansion Level.
	 * Added by OEF for IR-017626V6R2011 02/12/2009.
	 * @param context Matrix Context object
	 * @param objectId ProjectId
	 * @param relPattern Relationship for Expansion
	 * @param nExpandLevel Expansion Level of WBS
	 * @return MapList of WBS with Level of Expansion
	 * @throws Exception
	 */
	protected MapList getWBSTasks(Context context, String objectId, String relPattern,short nExpandLevel) throws Exception
	{
		// Check license while listing All Tasks and WBS tasks, if license check fails here
		// the tasks will not be listed.
		//
		//ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_PRG_LICENSE_ARRAY);

		/*com.matrixone.apps.common.MemberRelationship member = (com.matrixone.apps.common.MemberRelationship) DomainRelationship.newInstance(context, DomainConstants.RELATIONSHIP_MEMBER);
		com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE,"PROGRAM");*/

		MapList mapList = new MapList();
		MapList resultList = new MapList();
		String loginUser = context.getUser();
		String rowEditable = "show";
		try {
			String rootNode = objectId;
			StringList rootNodeSelects = new StringList(3);
			DomainObject rootNodeObj = DomainObject.newInstance(context, rootNode);

			rootNodeSelects.addElement(DomainConstants.SELECT_TYPE);
			rootNodeSelects.addElement(DomainConstants.SELECT_NAME);
			rootNodeSelects.addElement(DomainConstants.SELECT_REVISION);
			rootNodeSelects.addElement("to["+DomainConstants.RELATIONSHIP_DELETED_SUBTASK+"].id");
			rootNodeObj.open(context);
			Map rootNodeTNR = rootNodeObj.getInfo(context,rootNodeSelects);
			rootNodeObj.close(context);
			String rootNodeType = (String) rootNodeTNR.get(DomainConstants.SELECT_TYPE);
			// Restricting to show mark as deleted task's subtasks
			/*String rootNodeHasMarkedDel = rootNodeObj.getInfo(context,"to["+DomainConstants.RELATIONSHIP_DELETED_SUBTASK+"].id");;
			
			if(ProgramCentralUtil.isNotNullString(rootNodeHasMarkedDel)) {
				return resultList;
            }*/
/*			String immediateProjectId = null;
			StringList taskSubtypesList = Task.getAllTaskTypeNames(context);

			if(taskSubtypesList.indexOf(rootNodeType)>=0)
			{
				// HashMap immediateProject = getParentProjectMap(context,rootNode);
				HashMap immediateProject = getParentProjectMap(context,rootNodeObj);
				immediateProjectId = (String)immediateProject.get(DomainConstants.SELECT_ID);
			}
			else
			{
				immediateProjectId = objectId;
			}*/

			String typePattern=null;
			boolean getTo = false;
			boolean getFrom = false;
			String busWhereClause = null;
			String relWhereClause =null;
			String postRelPattern = null;
			String postTypePattern = null;
			Map postPatterns = null;
			String strDirection = null;

			// Object and Relationship selects

			StringList objectSelects = new StringList(5);
			StringList relationshipSelects = new StringList(4);

			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);
			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
			objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_MEMBER  + "].to.name");
			objectSelects.addElement("from[" + DomainConstants.RELATIONSHIP_MEMBER  + "].attribute[" + DomainConstants.ATTRIBUTE_PROJECT_ACCESS + "]");
			objectSelects.addElement(SELECT_IS_PARENT_TASK_DELETED);
			if (nExpandLevel != 0)
			{
				//selectable to determine if task is summary to display plus sign in SB.
				objectSelects.addElement("from[" + relPattern + "]");
			}
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
			relationshipSelects.addElement(DomainConstants.SELECT_LEVEL);
			relationshipSelects.addElement(SubtaskRelationship.SELECT_SEQUENCE_ORDER);
			relationshipSelects.addElement("from.id");//Added for "What if"

			typePattern = ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT;
			getFrom = true;
			strDirection = "from";
			mapList = rootNodeObj.getRelatedObjects(context,
					relPattern,
					typePattern,
					getTo,
					getFrom,
					nExpandLevel,
					objectSelects,
					relationshipSelects,
					busWhereClause,
					relWhereClause,
					postRelPattern,
					postTypePattern,
					postPatterns) ;
			// Role Access for MasterProjectSchedule
			Iterator itrWbsTasks = mapList.iterator();
			while(itrWbsTasks.hasNext())
			{
				Hashtable hTableWbsTasks = (Hashtable) itrWbsTasks.next();
				String strTaskType = (String)hTableWbsTasks.get(DomainConstants.SELECT_TYPE);
				String isParentTaskDeleted = (String)hTableWbsTasks.get(SELECT_IS_PARENT_TASK_DELETED);

				if (nExpandLevel != 0)
				{
					//Determine if task is summary inform SB to display a plus sign.
					String strHasSubTask = (String) hTableWbsTasks.get("from[" + relPattern + "]");
					if (strHasSubTask != null)
						hTableWbsTasks.put("hasChildren", strHasSubTask);
					else
						hTableWbsTasks.put("hasChildren", "True");
				}

				if("TRUE".equalsIgnoreCase(isParentTaskDeleted)){
					rowEditable = "readonly";
				}
				hTableWbsTasks.put("RowEditable",rowEditable);
				if(rowEditable.equalsIgnoreCase("readonly"))
				{
					hTableWbsTasks.put("selection","none");
				}
				hTableWbsTasks.put("direction",strDirection);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			return mapList;
		}
	}

	/**
	 * This method is used to get the Calendar Name
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - MapList
	 *            containing the objects list
	 * @return String
	 * @throws Exception
	 *             if the operation fails
	 * @since PMC V6R2008-1
	 */
	public String getCalendarName(Context context, String args[]) throws Exception
	{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String objectId = (String) paramMap.get("objectId");
		String strMode = (String) requestMap.get("mode");
		WorkCalendar calendar= WorkCalendar.getCalendar(context, objectId);
		String strLanguage = context.getSession().getLanguage();
		String strClear = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
				"emxProgramCentral.Common.Clear", strLanguage);
		String strCalendarName = "";
		String strCalendarID = "";
		String popTreeUrl = "";
		if(calendar != null){
			strCalendarName = XSSUtil.encodeForHTML(context,(String)calendar.getName());
			strCalendarID = calendar.getId();
		}

		if("view".equals(strMode)){
			if(strCalendarID != null && !"".equals(strCalendarID.trim())){
				DomainObject objCalendar = DomainObject.newInstance(context, strCalendarID);
				String strCalendarIcon = objCalendar.getInfo(context, DomainConstants.SELECT_TYPE);
				String strSymbolicType = FrameworkUtil.getAliasForAdmin(context, "Type", strCalendarIcon, true);
				String strTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strSymbolicType);

				popTreeUrl = "<img src=\"../common/images/"+strTypeIcon+"\" border=\"0\" title ='"+strCalendarName+"'/>"+strCalendarName;
			}

		}else if("edit".equals(strMode) ){
			popTreeUrl = "<input type=\"textbox\" readonly=\"true\" value=\""+ strCalendarName +"\" name=\"calendar\">"
					+"<input type=\"hidden\" value=\""+ strCalendarID +"\" name=\"hideCalendar\">"
					+("<input ")
					+("type=\"button\" name=\"btnCalendar\" ")
					+("size=\"200\" value=\"...\" ")
					+("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_WorkCalendar")
					+("&table=PMCTaskCalendarSearchTable")
					+("&selection=single")
					+("&excludeOIDprogram=emxProgramCentralUtil:getExcludeOIDForCalendar")
            +("&submitURL=../programcentral/FullSearchUtil.jsp?mode=chooser&chooserType=CalendarChooser&fieldNameActual=hideCalendar&fieldNameDisplay=calendar&HelpMarker=emxhelpfullsearch&sortColumnName=ProjectCalendar&sortDirection=descending&objectId="+objectId)
					+("')\">")
					+("<a name=\"ancClear\" href=\"#ancClear\" class=\"dialogClear\" onclick=\"document.editDataForm.calendar.value='',document.editDataForm.hideCalendar.value=''\">")
					+("<emxUtil:i18n localize=\"i18nId\">")
					+strClear
					+("</emxUtil:i18n>")
					+("</a>");
		}
		return popTreeUrl;
	}

	/**
	 * updateCalendar - This Method will update Calendar in WBS SB from
	 *   the Edit menu.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return boolean
	 * @since PMC V6R2008-2.0
	 */
	public boolean updateCalendar(Context context,String args[]) throws Exception
	{
		String languageStr = context.getSession().getLanguage();
		String strAlert = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
				"emxProgramCentral.Common.CalendarMismatchWarning", languageStr);
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");


		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String[] strcalendarIDs = (String[]) requestMap.get("hideCalendar");
		String strcalendarID = "";
		if(strcalendarIDs != null && strcalendarIDs.length > 0){
			strcalendarID = strcalendarIDs[0];
		}
		String objectId = (String) paramMap.get("objectId");
		task.setId(objectId);
		String strOwnerCalendarID = task.getOwnerCalendar(context);

		if(strOwnerCalendarID != null && !"".equals(strOwnerCalendarID)&& strcalendarID != null && !"".equals(strcalendarID)){
			if(!strOwnerCalendarID.equals(strcalendarID)){
				emxContextUtilBase_mxJPO.mqlNotice(context, strAlert);
			}
		}

		// removing already connected Calender
		task.removeCalendar(context);
		// Adding new Calender
		if(strcalendarID!= null && !"".equals(strcalendarID.trim())){
			task.addCalendar(context, strcalendarID);
		}
		// calling date roll up
		TaskDateRollup rollup = new TaskDateRollup(objectId);
		rollup.validateTask(context);
		return true;
	}
	//Added:10-Nov-09:nzf:R209:PRG:WBS Task Constraint
	/**
	 * getDefaultTaskConstraints - This method will get Default Constraint Type attibute for Project Space in view and edit mode
	 * Used in form : PMCProjectDetailsViewForm
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R209
	 */
	public String getDefaultTaskConstraints(Context context, String[] args) throws Exception
	{
		String strReturnVal = "";
		try {
			HashMap arguMap = (HashMap)JPO.unpackArgs(args);

			String sLanguage = context.getSession().getLanguage();

			AttributeType atrDefaultConstraint = new AttributeType(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE);
			atrDefaultConstraint.open(context);
			StringList strList = atrDefaultConstraint.getChoices(context);
			atrDefaultConstraint.close(context);

			final String ATTR_PROJECT_SCHEDULE_FROM_VAL = "attribute["+ATTRIBUTE_PROJECT_SCHEDULE_FROM+"]";
			final String ATTR_DEFAULT_TASK_CONSTRAINT_TYPE_VAL = "attribute["+ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE+"]";

			Map paramMap = (Map) arguMap.get("paramMap");
			Map requestMap = (Map) arguMap.get("requestMap");
			String strMode = (String) requestMap.get("mode");
			String strObjectId = (String) paramMap.get("objectId");

			String strDefaultTaskConstraintRange = "";
			String strDefaultTaskConstraintTranslated = "";

			DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);
			String strDefaultTaskConstraint = dmoProject.getInfo(context,ATTR_DEFAULT_TASK_CONSTRAINT_TYPE_VAL);
			String strProjectScheduledFrom = dmoProject.getInfo(context,ATTR_PROJECT_SCHEDULE_FROM_VAL);

			/*
			 * If Mode = view.
			 */
			if(strDefaultTaskConstraint.equals("")){
				if("Project Start Date".equals(strProjectScheduledFrom)){
					strReturnVal = i18nNow.getRangeI18NString(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP, sLanguage);
				}else{
					strReturnVal = i18nNow.getRangeI18NString(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP, sLanguage);
				}
			}else{
				strReturnVal = i18nNow.getRangeI18NString(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, strDefaultTaskConstraint, sLanguage);
			}

			if("edit".equals(strMode)){

				/*
				 * If Mode = edit.
				 */


				StringList slTaskConstraintsRanges = new StringList();
				StringList slTaskConstraintsRangesTranslated = new StringList();
				String strDefaultConstraintHTML = "<select  id = \""+ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE+"\" name=\""+ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE+"\">";

				for(int i=0; i<strList.size();i++){
					String strTaskConstraintRange = (String)strList.get(i);

					strDefaultTaskConstraintRange = XSSUtil.encodeForHTML(context,strTaskConstraintRange);
					strDefaultTaskConstraintTranslated = i18nNow.getRangeI18NString(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, strTaskConstraintRange, sLanguage);

					if(strDefaultTaskConstraint.equals(strDefaultTaskConstraintRange)){
						strDefaultConstraintHTML+="<option selected='true' value=\""+XSSUtil.encodeForHTML(context,strDefaultTaskConstraintRange)+"\">"+XSSUtil.encodeForHTML(context,strDefaultTaskConstraintTranslated)+"</option>";
					}else{
						strDefaultConstraintHTML+="<option value=\""+XSSUtil.encodeForHTML(context,strDefaultTaskConstraintRange)+"\">"+XSSUtil.encodeForHTML(context,strDefaultTaskConstraintTranslated)+"</option>";
					}

				}
				strDefaultConstraintHTML += "</select>";

				strReturnVal = strDefaultConstraintHTML;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strReturnVal;
	}

	/**
	 * getDefaultTaskConstraints - This method will get Task Constraint Type attibute for Task in view and edit mode
	 * Used in form : PMCProjectTaskViewForm
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R209
	 */
	public String getTaskConstraints(Context context, String[] args) throws Exception
	{
		String strReturnVal = "";
		try {
			HashMap arguMap = (HashMap)JPO.unpackArgs(args);

			String sLanguage = context.getSession().getLanguage();

			AttributeType atrDefaultConstraint = new AttributeType(ATTRIBUTE_TASK_CONSTRAINT_TYPE);
			atrDefaultConstraint.open(context);
			StringList strList = atrDefaultConstraint.getChoices(context);
			atrDefaultConstraint.close(context);

			final String ATTR_PROJECT_SCHEDULE_FROM_VAL = "attribute["+ATTRIBUTE_PROJECT_SCHEDULE_FROM+"]";
			final String ATTR_TASK_CONSTRAINT_TYPE_VAL = "attribute["+ATTRIBUTE_TASK_CONSTRAINT_TYPE+"]";
			final String ATTR_DEFAULT_TASK_CONSTRAINT_TYPE_VAL = "attribute["+ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE+"]";

			Map paramMap = (Map) arguMap.get("paramMap");
			Map requestMap = (Map) arguMap.get("requestMap");
			String strMode = (String) requestMap.get("mode");
			String strObjectId = (String) paramMap.get("objectId");

			String strTaskConstraintsRange = "";
			String strTaskConstraintsRangeTranslated = "";

			com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task();
			task.newInstance(context);
			task.setId(strObjectId);
			String strTaskConstraint = task.getInfo(context,ATTR_TASK_CONSTRAINT_TYPE_VAL);
			// Modified:5-Jan-10:nzf:R208:PRG:Bug:IR-030681
			// strTaskConstraint = "";
			// End:R208:PRG:Bug:IR-030681
			boolean isSummeryTask = task.hasRelatedObjects(context, DomainConstants.RELATIONSHIP_SUBTASK, true);

			DomainObject dmoProject = task.getProjectObject(context);

			String strDefaultTaskConstraint = dmoProject.getInfo(context,ATTR_DEFAULT_TASK_CONSTRAINT_TYPE_VAL);
			String strProjectScheduledFrom = dmoProject.getInfo(context,ATTR_PROJECT_SCHEDULE_FROM_VAL);
			/*
			 * If Mode = view.
			 */
			if(strTaskConstraint.equals("")){
				if("Project Start Date".equals(strProjectScheduledFrom)){
					strReturnVal = i18nNow.getRangeI18NString(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP, sLanguage);
				}else{
					strReturnVal = i18nNow.getRangeI18NString(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP, sLanguage);
				}
			}else{
				strReturnVal = i18nNow.getRangeI18NString(ATTRIBUTE_TASK_CONSTRAINT_TYPE, strTaskConstraint, sLanguage);;
			}

			if("edit".equals(strMode)){

				/*
				 * If Mode = edit.
				 */
				StringList slTaskConstraintsRanges = new StringList();
				StringList slTaskConstraintsRangesTranslated = new StringList();
				String strDefaultConstraintHTML = "<select id = \""+ATTRIBUTE_TASK_CONSTRAINT_TYPE+"\" name=\""+ATTRIBUTE_TASK_CONSTRAINT_TYPE+"\">";

				if(isSummeryTask){
					if("Project Start Date".equals(strProjectScheduledFrom)){

						for(int i=0; i<strList.size();i++){
							String strTaskConstraintRange = (String)strList.get(i);

							strTaskConstraintsRange = strTaskConstraintRange;
							//strTaskConstraintsRangeTranslated = i18nNow.getRangeI18NString(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, strTaskConstraintRange, sLanguage);
							strTaskConstraintsRangeTranslated = i18nNow.getRangeI18NString(ATTRIBUTE_TASK_CONSTRAINT_TYPE, strTaskConstraintRange, sLanguage);//IR-179629V6R2013x
							// Modified:5-Jan-10:nzf:R208:PRG:Bug:IR-033016
							if(strTaskConstraintsRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP) || strTaskConstraintsRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strTaskConstraintsRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET)){
								// End:R208:PRG:Bug:IR-033016
								//strDefaultConstraintHTML+="<option value=\""+strTaskConstraintsRange+"\">"+strTaskConstraintsRangeTranslated+"</option>";
								if(strTaskConstraint.equals(strTaskConstraintsRange)){
									strDefaultConstraintHTML+="<option selected='true' value=\""+XSSUtil.encodeForHTML(context,strTaskConstraintsRange)+"\">"+XSSUtil.encodeForHTML(context,strTaskConstraintsRangeTranslated)+"</option>";
								}else{
									strDefaultConstraintHTML+="<option value=\""+XSSUtil.encodeForHTML(context,strTaskConstraintsRange)+"\">"+XSSUtil.encodeForHTML(context,strTaskConstraintsRangeTranslated)+"</option>";
								}
							}

						}
						strDefaultConstraintHTML += "</select>";
						strReturnVal = strDefaultConstraintHTML;
					}else{

						for(int i=0; i<strList.size();i++){
							String strTaskConstraintRange = (String)strList.get(i);

							strTaskConstraintsRange = strTaskConstraintRange;
							//strTaskConstraintsRangeTranslated = i18nNow.getRangeI18NString(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, strTaskConstraintRange, sLanguage);
							strTaskConstraintsRangeTranslated = i18nNow.getRangeI18NString(ATTRIBUTE_TASK_CONSTRAINT_TYPE, strTaskConstraintRange, sLanguage);//IR-179629V6R2013x
							// Modified:5-Jan-10:nzf:R208:PRG:Bug:IR-033016
							if(strTaskConstraintsRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP) || strTaskConstraintsRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strTaskConstraintsRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET)){
								// End:R208:PRG:Bug:IR-033016
								strDefaultConstraintHTML+="<option value=\""+XSSUtil.encodeForHTML(context,strTaskConstraintsRange)+"\">"+XSSUtil.encodeForHTML(context,strTaskConstraintsRangeTranslated)+"</option>";
							}

						}
						strDefaultConstraintHTML += "</select>";
						strReturnVal = strDefaultConstraintHTML;
					}
				}else{
					for(int i=0; i<strList.size();i++){
						String strTaskConstraintRange = (String)strList.get(i);

						strTaskConstraintsRange = strTaskConstraintRange;
						//Modified:17-Feb-2011:hp5:R211:PRG:IR-048984V6R2012
						strTaskConstraintsRangeTranslated = i18nNow.getRangeI18NString(ATTRIBUTE_TASK_CONSTRAINT_TYPE, strTaskConstraintRange, sLanguage);
						//End:17-Feb-2011:hp5:R211:PRG:IR-048984V6R2012

						if(strTaskConstraint.equals(strTaskConstraintsRange)){
							strDefaultConstraintHTML+="<option selected='true' value=\""+XSSUtil.encodeForHTML(context,strTaskConstraintsRange)+"\">"+XSSUtil.encodeForHTML(context,strTaskConstraintsRangeTranslated)+"</option>";
						}else{
							strDefaultConstraintHTML+="<option value=\""+XSSUtil.encodeForHTML(context,strTaskConstraintsRange)+"\">"+XSSUtil.encodeForHTML(context,strTaskConstraintsRangeTranslated)+"</option>";
						}
					}
					strDefaultConstraintHTML += "</select>";
					strReturnVal = strDefaultConstraintHTML;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strReturnVal;
	}

	/**
	 * Gets the column value for Constraint Type in WBs summary table
	 *
	 * @param context The Matrix Context object
	 * @param args The packed arguments map
	 * @return The list of values of Constraints Type for each row in WBS summary
	 * @throws MatrixException if operation fails
	 */
	public Vector getProjectAndTaskConstraints(Context context, String[] args) throws MatrixException
	{

		Vector vId = new Vector();
		try {
			final String ATTR_PROJECT_SCHEDULE_FROM = "attribute["+ATTRIBUTE_PROJECT_SCHEDULE_FROM+"]";
			final String ATTR_TASK_CONSTRAINT_TYPE_VAL = "attribute["+ATTRIBUTE_TASK_CONSTRAINT_TYPE+"]";
			final String ATTR_DEFAULT_TASK_CONSTRAINT_TYPE_VAL = "attribute["+ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE+"]";
			final String IS_KINDOF_PROJECT_CONCEPT = "type.kindof[" + TYPE_PROJECT_CONCEPT + "]";
			final String IS_KINDOF_PROJECT_SPACE = "type.kindof[" + TYPE_PROJECT_SPACE + "]";
			final String IS_KINDOF_PROJECT_TEMPLATE = "type.kindof[" + TYPE_PROJECT_TEMPLATE + "]";
			String sLanguage = context.getSession().getLanguage();

			HashMap projectMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) projectMap.get("objectList");
			vId = new Vector(objectList.size());

			Map paramList = (Map) projectMap.get("paramList");
			String strObjectId = (String)paramList.get("objectId");

			//
			// Find all the required infomration on each of the tasks here
			//
			String[] strObjectIds = new String[objectList.size()];
			int size = objectList.size();
			for (int i = 0; i < size; i++) {
				Map mapObject = (Map) objectList.get(i);
				String taskId = (String) mapObject.get(DomainObject.SELECT_ID);
				strObjectIds[i] = taskId;
			}

			StringList slBusSelect = new StringList();
			slBusSelect.add(DomainConstants.SELECT_ID);
			slBusSelect.add(ATTR_DEFAULT_TASK_CONSTRAINT_TYPE_VAL);
			slBusSelect.add(ATTR_TASK_CONSTRAINT_TYPE_VAL);
			slBusSelect.add(ATTR_PROJECT_SCHEDULE_FROM);
			slBusSelect.add(IS_KINDOF_PROJECT_SPACE);
			slBusSelect.add(IS_KINDOF_PROJECT_CONCEPT);
			slBusSelect.add(IS_KINDOF_PROJECT_TEMPLATE);

			Map mapTaskInfo = new HashMap();
			BusinessObjectWithSelectList objectWithSelectList = DomainObject.getSelectBusinessObjectData(context, strObjectIds, slBusSelect);
			for (BusinessObjectWithSelectItr objectWithSelectItr = new BusinessObjectWithSelectItr(objectWithSelectList); objectWithSelectItr.next();) {
				BusinessObjectWithSelect objectWithSelect = objectWithSelectItr.obj();

				Map mapTask = new HashMap();
				for (Iterator itrSelectables = slBusSelect.iterator(); itrSelectables.hasNext();) {
					String strSelectable = (String)itrSelectables.next();
					mapTask.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
				}

				mapTaskInfo.put(objectWithSelect.getSelectData(SELECT_ID), mapTask);
			}

			for(int i=0; i<objectList.size() ; i++){

				Map objectMap = (Map)objectList.get(i);
				String id = (String)objectMap.get(DomainConstants.SELECT_ID);
				Map objectInfo = (Map)mapTaskInfo.get(id);
				String isProjectSpace = (String)objectInfo.get(IS_KINDOF_PROJECT_SPACE);
				String isProjectConcept = (String)objectInfo.get(IS_KINDOF_PROJECT_CONCEPT);
				String isProjectTemplate = (String)objectInfo.get(IS_KINDOF_PROJECT_TEMPLATE);
				String strDefaultTaskConstraint = EMPTY_STRING;
				if ("TRUE".equalsIgnoreCase(isProjectSpace) || "TRUE".equalsIgnoreCase(isProjectConcept))
				{
					strDefaultTaskConstraint = (String)objectInfo.get(ATTR_DEFAULT_TASK_CONSTRAINT_TYPE_VAL);
				} else if("TRUE".equalsIgnoreCase(isProjectTemplate)) {
					strDefaultTaskConstraint = ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP;
					String scheduleFrom = (String)objectInfo.get(ATTR_PROJECT_SCHEDULE_FROM);
					if(ProgramCentralConstants.ATTRIBUTE_SCHEDULED_FROM_RANGE_FINISH.equalsIgnoreCase(scheduleFrom)){
						strDefaultTaskConstraint = ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP;
					}
					strDefaultTaskConstraint = ((EnoviaResourceBundle.getRangeI18NString(context,ATTRIBUTE_TASK_CONSTRAINT_TYPE, strDefaultTaskConstraint, sLanguage)));
				} else {
					strDefaultTaskConstraint = (String)objectInfo.get(ATTR_TASK_CONSTRAINT_TYPE_VAL);
					strDefaultTaskConstraint = ((EnoviaResourceBundle.getRangeI18NString(context,ATTRIBUTE_TASK_CONSTRAINT_TYPE, strDefaultTaskConstraint, sLanguage)));
				}
				vId.add(strDefaultTaskConstraint);
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return vId;
	}

	/**
	 * getTaskConstraintRange - Will get range values for attribute "Task Constraint Type" used in
	 * table : PMCWBSViewTable
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R209
	 */
	public Map getTaskConstraintRange(Context context, String[] args) throws MatrixException {

		try {
			String strDefaultProjectVal = EMPTY_STRING;
			String ATTRIBUTE_PROJECT_SCHEDULE_FROM_VAL = "attribute["+ DomainConstants.ATTRIBUTE_PROJECT_SCHEDULE_FROM+"]";
			String  ATTRIBUTE_DEFAULT_CONSTRAINT_VAL = "attribute["+DomainConstants.ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE+"]";
			String ATTRIBUTE_TASK_CONSTRAINT_VAL = "attribute["+ DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE+"]";
			String sLanguage = context.getSession().getLanguage();
			Map programMap =  JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");
			String strParentObjectId = (String)requestMap.get("objectId");
			StringList selectableList = new StringList();
			selectableList.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			selectableList.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			selectableList.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
			selectableList.add(SELECT_TASK_PROJECT_ID);
			selectableList.add(SELECT_TASK_PROJECT_TYPE);
			selectableList.add(ATTRIBUTE_PROJECT_SCHEDULE_FROM_VAL);
			selectableList.add(ATTRIBUTE_DEFAULT_CONSTRAINT_VAL);
			selectableList.add(ATTRIBUTE_TASK_CONSTRAINT_VAL);

			DomainObject dmoParentObject = DomainObject.newInstance(context);
			dmoParentObject.setId(strParentObjectId);
			Map<String,String> parentObjectInfoMap = dmoParentObject.getInfo(context, selectableList);
			String parentType = parentObjectInfoMap.get(SELECT_TASK_PROJECT_TYPE);
			String isProjectSpace = parentObjectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isProjectConcept = parentObjectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			String isProjectTemplate = parentObjectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

			if("true".equalsIgnoreCase(isProjectSpace) || "true".equalsIgnoreCase(isProjectConcept)){
				strDefaultProjectVal = parentObjectInfoMap.get(ATTRIBUTE_DEFAULT_CONSTRAINT_VAL);
			}
			else if("true".equalsIgnoreCase(isProjectTemplate)){
				strDefaultProjectVal = DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP;
				String scheduleFrom = parentObjectInfoMap.get(ATTRIBUTE_PROJECT_SCHEDULE_FROM_VAL);
				if(ProgramCentralConstants.ATTRIBUTE_SCHEDULED_FROM_RANGE_FINISH.equalsIgnoreCase(scheduleFrom)){
					strDefaultProjectVal = DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP;
				}
			} else {
				strDefaultProjectVal = parentObjectInfoMap.get(ATTRIBUTE_TASK_CONSTRAINT_VAL);
			}

			AttributeType atrTaskConstraint = new AttributeType(ATTRIBUTE_TASK_CONSTRAINT_TYPE);
			atrTaskConstraint.open(context);
			StringList strList = atrTaskConstraint.getChoices(context);
			atrTaskConstraint.close(context);

			StringList slTaskConstraintsRanges = new StringList();
			StringList slTaskConstraintsRangesTranslated = new StringList();
			Map map = new HashMap();

			if(strList.contains(strDefaultProjectVal)){
				slTaskConstraintsRanges.add(strDefaultProjectVal);
				strList.remove(strDefaultProjectVal);
				slTaskConstraintsRangesTranslated.add(EnoviaResourceBundle.getRangeI18NString(context, ATTRIBUTE_TASK_CONSTRAINT_TYPE, strDefaultProjectVal, sLanguage));
			}

			if(Boolean.valueOf(isProjectTemplate) || TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(parentType)){
				String secondConstraintType = (strDefaultProjectVal.equalsIgnoreCase(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP))
						? ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP : ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP;
				slTaskConstraintsRanges.add(secondConstraintType);
				slTaskConstraintsRangesTranslated.add(EnoviaResourceBundle.getRangeI18NString(context,ATTRIBUTE_TASK_CONSTRAINT_TYPE, secondConstraintType, sLanguage));
			} else {
				for(int i=0; i<strList.size();i++){
					String strTaskConstraintRange = (String)strList.get(i);
					slTaskConstraintsRanges.add(strTaskConstraintRange);
					slTaskConstraintsRangesTranslated.add(EnoviaResourceBundle.getRangeI18NString(context,ATTRIBUTE_TASK_CONSTRAINT_TYPE, strTaskConstraintRange, sLanguage));
				}
			}

			map.put("field_choices", slTaskConstraintsRanges);
			map.put("field_display_choices", slTaskConstraintsRangesTranslated);

			return  map;
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * Gets Object specific Constraint Type Ranges in table PMCWBSViewTable
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public HashMap getConstraintTypeRange(Context context, String[] args)
			throws Exception
			{
		try {
			String sLanguage = context.getSession().getLanguage();
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map paramMap = (HashMap) programMap.get("rowValues");
			String strObjectId = (String) paramMap.get("objectId");
			DomainObject dmoObject = DomainObject.newInstance(context,strObjectId);
			String strAttributeInt = "";

			final String ATTR_PROJECT_SCHEDULE_FROM_VAL = "attribute["+ATTRIBUTE_PROJECT_SCHEDULE_FROM+"]";

			AttributeType atrTaskConstraint = new AttributeType(ATTRIBUTE_TASK_CONSTRAINT_TYPE);

			DomainObject dmoProject = null;
			String strProjectScheduledFrom = null;
			boolean isSummeryTask = false;

			//If Project or Template then set attribute to "Default constraint Type" else "Task Constraint Type"
			if(dmoObject.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE) || dmoObject.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_CONCEPT)){
				atrTaskConstraint = new AttributeType(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE);
				strAttributeInt = ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE;
			}else{
				atrTaskConstraint = new AttributeType(ATTRIBUTE_TASK_CONSTRAINT_TYPE);
				strAttributeInt = ATTRIBUTE_TASK_CONSTRAINT_TYPE;
				Task task = new Task(strObjectId);
				dmoProject = task.getProjectObject(context);
				isSummeryTask = task.hasRelatedObjects(context, DomainConstants.RELATIONSHIP_SUBTASK, true);
				strProjectScheduledFrom = dmoProject.getInfo(context,ATTR_PROJECT_SCHEDULE_FROM_VAL);
			}

			atrTaskConstraint.open(context);
			StringList strList = atrTaskConstraint.getChoices(context);
			atrTaskConstraint.close(context);

			StringList slTaskConstraintsRanges = new StringList();
			StringList slTaskConstraintsRangesTranslated = new StringList();
			HashMap map = new HashMap();
			String i18nSelectedRole = null;

			//Below code creates Constraint Type Ranges for Project, Summary Tasks and Leaf Tasks.
			for(int i=0; i<strList.size();i++){
				String strTaskConstraintRange = (String)strList.get(i);
				if(isSummeryTask){
					if(ProgramCentralConstants.ATTRIBUTE_SCHEDULED_FROM_RANGE_START.equals(strProjectScheduledFrom)){
						if(strTaskConstraintRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP) || strTaskConstraintRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strTaskConstraintRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET)){
							slTaskConstraintsRanges.add(strTaskConstraintRange);
							slTaskConstraintsRangesTranslated.add(i18nNow.getRangeI18NString(strAttributeInt, strTaskConstraintRange, sLanguage));
						}
					}else{
						if(strTaskConstraintRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP) || strTaskConstraintRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strTaskConstraintRange.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET)){
							slTaskConstraintsRanges.add(strTaskConstraintRange);
							slTaskConstraintsRangesTranslated.add(i18nNow.getRangeI18NString(strAttributeInt, strTaskConstraintRange, sLanguage));
						}
					}
				}else{
					slTaskConstraintsRanges.add(strTaskConstraintRange);
					slTaskConstraintsRangesTranslated.add(i18nNow.getRangeI18NString(strAttributeInt, strTaskConstraintRange, sLanguage));
				}
			}

			map.put("RangeValues", slTaskConstraintsRanges);
			map.put("RangeDisplayValue", slTaskConstraintsRangesTranslated);

			return  map;
		} catch (MatrixException e) {
			e.printStackTrace();
			throw e;
		}
			}
	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	/*
 public HashMap getWBSTaskConstraintRange(Context context, String[] args)
 throws Exception
 {
     try {
         String sLanguage = context.getSession().getLanguage();

         AttributeType atrTaskConstraint = new AttributeType(ATTRIBUTE_TASK_CONSTRAINT_TYPE);
         atrTaskConstraint.open(context);
         StringList strList = atrTaskConstraint.getChoices(context);
         atrTaskConstraint.close(context);

         StringList slTaskConstraintsRanges = new StringList();
         StringList slTaskConstraintsRangesTranslated = new StringList();
         HashMap map = new HashMap();
         String i18nSelectedRole = null;

         for(int i=0; i<strList.size();i++){
             String strTaskConstraintRange = (String)strList.get(i);

                 slTaskConstraintsRanges.add(strTaskConstraintRange);
                 slTaskConstraintsRangesTranslated.add(i18nNow.getRangeI18NString(ATTRIBUTE_TASK_CONSTRAINT_TYPE, strTaskConstraintRange, sLanguage));
         }

         map.put("field_choices", slTaskConstraintsRanges);
         map.put("field_display_choices", slTaskConstraintsRangesTranslated);

         return  map;
     } catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
 }*/
	/**
	 * updateDefaultConstraintType - Will update attribute Default Constraint Type (Project Space)
	 * form : PMCProjectDetailsViewForm
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R209
	 */

	public boolean updateDefaultConstraintType(Context context,String args[]) throws Exception
	{
		try {
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			HashMap columnMap = (HashMap) inputMap.get("columnMap");
			HashMap paramMap = (HashMap) inputMap.get("paramMap");
			String strObjectId = (String) paramMap.get("objectId");
			String strNewDefaultTaskconstraint = (String) paramMap.get("New Value");

			DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);
			dmoProject.setAttributeValue(context, ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, strNewDefaultTaskconstraint);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * updateTaskConstraintType - updates Task Constraint Type attribute for Task
	 * Form : PMCProjectTaskViewForm
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R209
	 */
	public boolean updateTaskConstraintType(Context context,String args[]) throws Exception{
		try {
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String TaskConstraints[] = (String[]) requestMap.get(ATTRIBUTE_TASK_CONSTRAINT_TYPE);
			String strSelectedTaskconstraint = TaskConstraints[0];
			String objectId = (String) paramMap.get("objectId");
			String TaskConstraintsDate[] 		= (String[]) requestMap.get("TaskConstraintDate_msvalue");
			String strTimeZone 					= (String) requestMap.get("timeZone");
			String strTempDate 					= TaskConstraintsDate[0];
			double clientTZOffset 				= Double.parseDouble(strTimeZone);
			Locale local 						= context.getLocale();

			//Conver MS_Date to standard date vale
			String sStartDate                   = eMatrixDateFormat.getDateValue(context,strTempDate,strTimeZone,local);

			int iDateFormat 				= eMatrixDateFormat.getEMatrixDisplayDateFormat();
			DateFormat format 				= DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, local);
			String strFormattedInputDate 			= eMatrixDateFormat.getFormattedDisplayDateTime(context, sStartDate, true,iDateFormat, clientTZOffset,local);
			Date date 						= format.parse(strFormattedInputDate);

			Map objectValues = new HashMap(2);
			objectValues.put("constraintDate", date);
			objectValues.put("constraintType", strSelectedTaskconstraint);

			Map dateList = new HashMap(1);
			dateList.put(objectId, objectValues);

			task.updateDates(context, dateList, true, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * updateTaskConstraintDate - updates Task Constraint Date attribute for Task
	 * Form : PMCProjectTaskViewForm
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R209
	 */
	public boolean updateTaskConstraintDate(Context context,String args[]) throws Exception {
		try {
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String TaskConstraintsDate[] 			= (String[]) requestMap.get("TaskConstraintDate");
			String strTempDate = TaskConstraintsDate[0];
			Locale local = context.getLocale();
			String strTimeZone 						= (String) requestMap.get("timeZone");
			double clientTZOffset 					= Double.parseDouble(strTimeZone);
			String objectId = (String) paramMap.get("objectId");

			//Update the attribute
			task.setId(objectId);
			String strTaskConstraintType = task.getInfo(context, getAttributeSelect(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE));
			int iDateFormat 			 = eMatrixDateFormat.getEMatrixDisplayDateFormat();
			DateFormat format 			 = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, local);
			String strFormattedInputDate = eMatrixDateFormat.getFormattedDisplayDateTime(context, strTempDate, true,iDateFormat, clientTZOffset,local);
			Date date 	  				 = format.parse(strFormattedInputDate);
			
			Map objectValues = new HashMap(2);
			objectValues.put("constraintDate", date);
			objectValues.put("constraintType", strTaskConstraintType);

			Map dateList = new HashMap(1);
			dateList.put(objectId, objectValues);
			task.updateDates(context, dateList, true, false);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * updateConstraintType - Will update attribute Default Constraint Type (Project Space)
	 * and attribute Task Constraint Type (Task Management) used in table :PMCWBSViewTable
	 * Summery Tasks: Onle selected constraints will be allowed for Summery Tasks.
	 * It also calls rollupAndSave to start the RollUp process.
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since R209
	 */
	public void updateConstraintType(Context context,String[] args) throws Exception{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) paramMap.get("objectId");
		String strNewDefaultTaskconstraint = (String) paramMap.get("New Value");

		DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);

		StringList TaskContraintInfoList = new StringList();
		TaskContraintInfoList.addElement(SELECT_ID);
		TaskContraintInfoList.addElement(SELECT_TYPE);
		TaskContraintInfoList.addElement(SELECT_NAME);
		TaskContraintInfoList.addElement(SELECT_CURRENT);
        TaskContraintInfoList.addElement(SELECT_POLICY);
		TaskContraintInfoList.addElement(Task.SELECT_TASK_CONSTRAINT_DATE);
		TaskContraintInfoList.addElement(Task.SELECT_TASK_CONSTRAINT_TYPE);
		TaskContraintInfoList.addElement(SELECT_DEFAULT_CONSTRAINT_TYPE);
		TaskContraintInfoList.addElement(Task.SELECT_TASK_ESTIMATED_START_DATE);
		TaskContraintInfoList.addElement(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);

		Map TaskContraintInfoMap = dmoObject.getInfo(context, TaskContraintInfoList);

		String objectType = (String)TaskContraintInfoMap.get(SELECT_TYPE);
		String strName = (String)TaskContraintInfoMap.get(SELECT_NAME);
		String strCurrentState =(String)TaskContraintInfoMap.get(SELECT_CURRENT);
        String strPolicy = (String)TaskContraintInfoMap.get(SELECT_POLICY);
		String strOldValOfDate = (String)TaskContraintInfoMap.get(Task.SELECT_TASK_CONSTRAINT_DATE);
		String taskEstStartDate = (String)TaskContraintInfoMap.get(Task.SELECT_TASK_ESTIMATED_START_DATE);
		String taskEstFinishDate = (String)TaskContraintInfoMap.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);

		SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);

		if(DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(objectType) || DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(objectType))
		{
			if(!checkEditable(3, strCurrentState, null)){
				String callFrom = (String) paramMap.get("callFrom");
				if(!"WhatIf".equalsIgnoreCase(callFrom)){
					String strErrorMsg = "emxProgramCentral.WBS.ConstraintDateCanNotChange";
                    // IR-447723
                    String i18ProjectState = EnoviaResourceBundle.getStateI18NString(context, strPolicy, strCurrentState, context.getLocale().getLanguage());
					String sKey[] = {"ProjectName","State"};
					// String sValue[] = {strName,strCurrentState};
                    String sValue[] = {strName,i18ProjectState};
					String companyName = null;
					strErrorMsg  = emxProgramCentralUtilClass.getMessage(context,
							strErrorMsg,
							sKey,
							sValue,
							companyName);
					emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
				}
				return;
			}
			dmoObject.setAttributeValue(context, ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, strNewDefaultTaskconstraint);
		}
		else
		{
			com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task();
			task.newInstance(context);
			task.setId(strObjectId);

			StringList busSelect = new StringList();
			busSelect.addElement(SELECT_ID);
			busSelect.addElement(ProgramCentralConstants.ATTRIBUTE_PROJECT_SCHEDULE_FROM);
			boolean isSummeryTask = dmoObject.hasRelatedObjects(context, DomainRelationship.RELATIONSHIP_SUBTASK, true);
			Map projectInfoMap = new HashMap();
			projectInfoMap = task.getProject(context, busSelect);
			String strProjectScheduledFrom = (String)projectInfoMap.get(ProgramCentralConstants.ATTRIBUTE_PROJECT_SCHEDULE_FROM);

			Date oldConstraintDate = null;
			if(!ProgramCentralUtil.isNullString(strOldValOfDate)){
				oldConstraintDate = dateFormat.parse(strOldValOfDate);
			}

			String languageStr = context.getSession().getLanguage();
			String strAlertPart1 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.RestrictTaskConstraint.AlertPart1", languageStr);
			String strAlertPart2 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.RestrictTaskConstraint.AlertPart2", languageStr);
			String strConstraintASAP = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.TaskConstriant.ASAP", languageStr);
			String strConstraintALAP = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.TaskConstriant.ALAP", languageStr);
			String strConstraintFNLT = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.TaskConstriant.FNLT", languageStr);
			String strConstraintSNLT = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.TaskConstriant.SNLT", languageStr);
			String strConstraintSNET = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.TaskConstriant.SNET", languageStr);

			if(!checkEditable(3, strCurrentState, null)){
				String callFrom = (String) paramMap.get("callFrom");
				if(!"WhatIf".equalsIgnoreCase(callFrom)){
					String strErrorMsg = "emxProgramCentral.WBS.ConstraintDateCanNotChangeOnTask";
                    // IR-447723
                    String i18ProjectState = EnoviaResourceBundle.getStateI18NString(context, strPolicy, strCurrentState, context.getLocale().getLanguage());
					String sKey[] = {"TaskName","State"};
					// String sValue[] = {strName,strCurrentState};
                    String sValue[] = {strName,i18ProjectState};
					String companyName = null;
					strErrorMsg  = emxProgramCentralUtilClass.getMessage(context,
							strErrorMsg,
							sKey,
							sValue,
							companyName);
					emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
				}
				return;
			}

			if(!isSummeryTask){

				if(!DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP.equalsIgnoreCase(strNewDefaultTaskconstraint) && !DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP.equalsIgnoreCase(strNewDefaultTaskconstraint))
				{
					Map objectMap = (Map) _taskMap.get(strObjectId);
					Date newDate = null;
					if (objectMap == null) {
						objectMap = new HashMap();
						_taskMap.put(strObjectId, objectMap);
						newDate = oldConstraintDate;

					}
					else if(objectMap.containsKey("constraintDate"))
					{
						newDate =(Date)objectMap.get("constraintDate");

					}
					Calendar constraintDate = Calendar.getInstance();
					if(strNewDefaultTaskconstraint.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNLT)|| strNewDefaultTaskconstraint.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET) || strNewDefaultTaskconstraint.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_MSON))
					{
						if(null == newDate)
						{
							newDate = dateFormat.parse(taskEstStartDate);
						}
						constraintDate.setTime(newDate);
						constraintDate.set(Calendar.HOUR_OF_DAY, 8);
						constraintDate.set(Calendar.MINUTE, 0);
						constraintDate.set(Calendar.SECOND, 0);

					}
					else if(strNewDefaultTaskconstraint.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_MFON)|| strNewDefaultTaskconstraint.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strNewDefaultTaskconstraint.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNET))
					{
						if(null == newDate)
						{
							newDate = dateFormat.parse(taskEstFinishDate);
						}
						constraintDate.setTime(newDate);
						constraintDate.set(Calendar.HOUR_OF_DAY, 17);
						constraintDate.set(Calendar.MINUTE, 0);
						constraintDate.set(Calendar.SECOND, 0);
					}
					newDate = constraintDate.getTime();
					objectMap.put("constraintDate", newDate);
				}
				updateTaskMap(context, strObjectId, "constraintType", strNewDefaultTaskconstraint);
				}else{
				if("Project Start Date".equals(strProjectScheduledFrom)){
					if(strNewDefaultTaskconstraint.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP) || strNewDefaultTaskconstraint.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strNewDefaultTaskconstraint.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET)){
						Map objectMap = (Map) _taskMap.get(strObjectId);
						if (objectMap == null) {
							objectMap = new HashMap();
							_taskMap.put(strObjectId, objectMap);
							objectMap.put("constraintDate", oldConstraintDate);
						}
						updateTaskMap(context, strObjectId, "constraintType", strNewDefaultTaskconstraint);
					}else{
						//Show warning message if invalid Task Constraint is selected
						String strErrorMsg = strAlertPart1+strName+" "+strAlertPart2+" '"+strConstraintASAP+"' , '"+strConstraintFNLT+"' , '"+strConstraintSNET+"'";
						emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
					}
				}else{
					if(strNewDefaultTaskconstraint.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP) || strNewDefaultTaskconstraint.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strNewDefaultTaskconstraint.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET)){
						Map objectMap = (Map) _taskMap.get(strObjectId);
						if (objectMap == null) {
							objectMap = new HashMap();
							_taskMap.put(strObjectId, objectMap);
							objectMap.put("constraintDate", oldConstraintDate);
						}
						updateTaskMap(context, strObjectId, "constraintType", strNewDefaultTaskconstraint);
					}else{
						//Show warning message if invalid Task Constraint is selected
						String strErrorMsg = strAlertPart1+strName+" "+strAlertPart2+" '"+strConstraintALAP+"' , '"+strConstraintFNLT+"' , '"+strConstraintSNET+"'";
						emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
					}
				}
			}
		}
	}

	/**
	 * updateConstriantDate - updates attribute Task Constraint Date for Project Space and Task Management Types in
	 * table : PMCWBSViewTable
	 * It also calls rollupAndSave functionality
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since R209
	 */
	public void updateConstriantDate(Context context,String args[]) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) paramMap.get("objectId");
		String strNewValOfDate = (String) paramMap.get("New Value");
		DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
		Locale locale = (Locale)requestMap.get("locale");
		if(null==locale)
		{
			locale = (Locale)requestMap.get("localeObj");
		}
		StringList TaskContraintInfoList = new StringList();
		TaskContraintInfoList.addElement(SELECT_ID);
		TaskContraintInfoList.addElement(SELECT_TYPE);
		TaskContraintInfoList.addElement(SELECT_NAME);
		TaskContraintInfoList.addElement(SELECT_CURRENT);
        TaskContraintInfoList.addElement(SELECT_POLICY);
		TaskContraintInfoList.addElement(Task.SELECT_TASK_CONSTRAINT_DATE);
		TaskContraintInfoList.addElement(Task.SELECT_TASK_CONSTRAINT_TYPE);
		TaskContraintInfoList.addElement(SELECT_DEFAULT_CONSTRAINT_TYPE);

		Map TaskContraintInfoMap = dmoObject.getInfo(context, TaskContraintInfoList);

		String objectType = (String)TaskContraintInfoMap.get(SELECT_TYPE);
		String strName = (String)TaskContraintInfoMap.get(SELECT_NAME);
		String strCurrentState =(String)TaskContraintInfoMap.get(SELECT_CURRENT);
        String strPolicy =(String)TaskContraintInfoMap.get(SELECT_POLICY);

		String strOldValOfDate = (String)TaskContraintInfoMap.get(Task.SELECT_TASK_CONSTRAINT_DATE);
		String strOldValOfType = (String)TaskContraintInfoMap.get(Task.SELECT_TASK_CONSTRAINT_TYPE);
		String strOldValOfTypePrj = (String)TaskContraintInfoMap.get(SELECT_DEFAULT_CONSTRAINT_TYPE);

		TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
		double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
		double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
		Date oldDate = null;
		Date newDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);

		if(ProgramCentralUtil.isNotNullString(strNewValOfDate)){
        strNewValOfDate = eMatrixDateFormat.getFormattedInputDate(strNewValOfDate,clientTZOffset, locale);
        
		newDate = dateFormat.parse(strNewValOfDate);
		if(null!=strOldValOfDate && !"null".equalsIgnoreCase(strOldValOfDate)&&!"".equals(strOldValOfDate))
		{
			oldDate = eMatrixDateFormat.getJavaDate(strOldValOfDate, locale);
			Calendar calOldFinishDate = Calendar.getInstance();
			calOldFinishDate.setTime(oldDate);
			calOldFinishDate.set(Calendar.HOUR_OF_DAY, 12);
			calOldFinishDate.set(Calendar.MINUTE, 0);
			calOldFinishDate.set(Calendar.SECOND, 0);
			calOldFinishDate.set(Calendar.MILLISECOND, 0);
			oldDate = calOldFinishDate.getTime();
		}
		}
		//Here to check if the old value(Constraint date attribute)is not present then should go not compare date and directly enter in the loop
		if((null==strOldValOfDate || "null".equalsIgnoreCase(strOldValOfDate)||"".equals(strOldValOfDate)) || ((null != newDate) && !(newDate.compareTo(oldDate)==0))) {
			if(DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(objectType) || DomainConstants.TYPE_PROJECT_CONCEPT.equalsIgnoreCase(objectType))
			{
				if(!checkEditable(3, strCurrentState, null)){
					String callFrom = (String) paramMap.get("callFrom");
					if(!"WhatIf".equalsIgnoreCase(callFrom)){
                        // IR-447723
                        String i18ProjectState = EnoviaResourceBundle.getStateI18NString(context, strPolicy, strCurrentState, context.getLocale().getLanguage());
						String strErrorMsg = "emxProgramCentral.WBS.ConstraintDateCanNotChange";
						String sKey[] = {"ProjectName","State"};
						// String sValue[] = {strName,strCurrentState};
                        String sValue[] = {strName,i18ProjectState};
						String companyName = null;
						strErrorMsg  = emxProgramCentralUtilClass.getMessage(context,
								strErrorMsg,
								sKey,
								sValue,
								companyName);
						emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
					}
					return;
				}

				//DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
				String reportDate = dateFormat.format(newDate);

				com.matrixone.apps.common.Task commonTask = new com.matrixone.apps.common.Task(strObjectId);
				commonTask.setAttributeValue(context, ATTRIBUTE_TASK_CONSTRAINT_DATE, reportDate);
				//Rollup the Project
				commonTask.rollupAndSave(context);

			}else{
				if(!checkEditable(3, strCurrentState, null)){
					String strErrorMsg = "emxProgramCentral.WBS.ConstraintDateCanNotChangeOnTask";
                    // IR-447723
                    String i18ProjectState = EnoviaResourceBundle.getStateI18NString(context, strPolicy, strCurrentState, context.getLocale().getLanguage());

                    String callFrom = (String) paramMap.get("callFrom");
					if(!"WhatIf".equalsIgnoreCase(callFrom)){
						String sKey[] = {"TaskName","State"};
                        // String sValue[] = {strName,strCurrentState};
						String sValue[] = {strName,i18ProjectState};
						String companyName = null;
						strErrorMsg  = emxProgramCentralUtilClass.getMessage(context,
								strErrorMsg,
								sKey,
								sValue,
								companyName);
						emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
					}
					return;
				}
				//DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
				Calendar constraintDate = Calendar.getInstance();
				constraintDate.setTime(newDate);
				if(strOldValOfType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNLT)|| strOldValOfType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET) || strOldValOfType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_MSON))
				{
					constraintDate.set(Calendar.HOUR_OF_DAY, 8);
					constraintDate.set(Calendar.MINUTE, 0);
					constraintDate.set(Calendar.SECOND, 0);


				}else if(strOldValOfType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_MFON)|| strOldValOfType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strOldValOfType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNET))
				{
					constraintDate.set(Calendar.HOUR_OF_DAY, 17);
					constraintDate.set(Calendar.MINUTE, 0);
					constraintDate.set(Calendar.SECOND, 0);
				}
				String reportDate = dateFormat.format(constraintDate.getTime());

				Map objectMap = (Map) _taskMap.get(strObjectId);
				if (objectMap == null) {
					objectMap = new HashMap();
					_taskMap.put(strObjectId, objectMap);
					objectMap.put("constraintType", strOldValOfType);
				}
				updateTaskMap(context, strObjectId, "constraintDate", newDate);
			}

		}
	}


	/**
	 * This method will generate an Error message if a Task violates its Task Constraint
	 * @param context
	 * @param strConstraintType
	 * @param strTaskName
	 * @param currentDate
	 * @param constraintDate
	 * @throws MatrixException
	 * @since R209
	 */
	static void showErrorMessage(Context  context,String strConstraintType,String strTaskName,Date currentDate,Date constraintDate) throws MatrixException{
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			String strTodaysDate = dateFormat.format(currentDate);
			String strConstraintDate = dateFormat.format(constraintDate);
			String languageStr = context.getSession().getLanguage();

			String strAlertPart1 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.PreventTaskPromotion.AlertPart1", languageStr);
			String strAlertPart2 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.PreventTaskPromotion.AlertPart2", languageStr);
			String strAlertPart3 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.PreventTaskPromotion.AlertPart3", languageStr);
			String strErrorMsg = strAlertPart1+strTaskName+ProgramCentralConstants.SPACE+strAlertPart2+" '"+strConstraintType+"','"+strConstraintDate+"' "+strAlertPart3+" '"+strTodaysDate+"'";
			emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
			//throw new MatrixException(strErrorMsg);
		} catch (Exception e) {
			throw new MatrixException(e.getMessage());
		}
	}

	/**
	 * This trigger will check Task Constraints.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 *        1 - String containing the from state
	 *        2 - String containing the to state
	 * @return int based on success or failure
	 * @throws Exception if operation fails
	 * @since R209
	 */
	public int triggerCheckTaskConstraints(Context context, String[] args)
			throws Exception
			{
		// get values from args.
		String objectId = args[0];
		String fromState = args[1];
		String toState   = args[2];

		boolean blockTrigger = false;

		final String ATTRIBUTE_TASK_CONSTRAINT_TYP = "attribute["+ATTRIBUTE_TASK_CONSTRAINT_TYPE+"]";
		final String ATTRIBUTE_TASK_CONSTRAINT_DAT = "attribute["+ATTRIBUTE_TASK_CONSTRAINT_DATE+"]";
		final String SELECT_IS_EXPERIMENT_TASK = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+ProgramCentralConstants.TYPE_EXPERIMENT+"]";

		//OBJECT SELECTS
		StringList busSelects = new StringList(2);
		busSelects.add(SELECT_CURRENT);
		busSelects.add(SELECT_STATES);
		busSelects.add(SELECT_NAME);
		busSelects.add(ATTRIBUTE_TASK_CONSTRAINT_TYP);
		busSelects.add(ATTRIBUTE_TASK_CONSTRAINT_DAT);
		busSelects.add(SELECT_IS_EXPERIMENT_TASK);

		//TASK OBJECT
		com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task();
		task.newInstance(context);
		task.setId(objectId);

		Map mpTaskInfo = task.getInfo(context, busSelects);

		String strCurrent = (String)mpTaskInfo.get(DomainConstants.SELECT_CURRENT);
		String strTaskConstraintType = (String)mpTaskInfo.get(ATTRIBUTE_TASK_CONSTRAINT_TYP);
		String strTaskConstraintDate = (String)mpTaskInfo.get(ATTRIBUTE_TASK_CONSTRAINT_DAT);
		String strTaskName = (String)mpTaskInfo.get(SELECT_NAME);
		String isExpTask = (String)mpTaskInfo.get(SELECT_IS_EXPERIMENT_TASK);

		//Dates
		java.util.Date todaysDate = new java.util.Date();
		java.util.Date constraintDate = null;
		if(null!=strTaskConstraintDate && !"".equals(strTaskConstraintDate) && !"null".equals(strTaskConstraintDate) ){
			constraintDate = new Date(strTaskConstraintDate);
		}
		String languageStr = context.getSession().getLanguage();
		StringList types = new StringList();
		String strConstraintTypei18 ="";
		if(strTaskConstraintType!=null){
			if(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNET.equals(strTaskConstraintType)){
				//Finish No Earlier Than Check Logic
				if(STATE_PROJECT_TASK_COMPLETE.equals(toState)){
					if(todaysDate.before(constraintDate)){
						blockTrigger = true;
						strConstraintTypei18 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
								"emxProgramCentral.TaskConstriant.TaskConstriant.FNET", languageStr);
						emxTaskBase_mxJPO.showErrorMessage(context, strConstraintTypei18, strTaskName, todaysDate, constraintDate);
					}
				}
			}else if(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT.equals(strTaskConstraintType)){
				//Finish No Later Than Check Logic
				if(STATE_PROJECT_TASK_COMPLETE.equals(toState) && !"true".equalsIgnoreCase(isExpTask)){
					if(todaysDate.after(constraintDate)){
						blockTrigger = true;
						strConstraintTypei18 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
								"emxProgramCentral.TaskConstriant.TaskConstriant.FNLT", languageStr);
						emxTaskBase_mxJPO.showErrorMessage(context, strConstraintTypei18, strTaskName, todaysDate, constraintDate);
					}
				}
			}else if(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_MFON.equals(strTaskConstraintType)){
				//Must finish On
				if(STATE_PROJECT_TASK_COMPLETE.equals(toState) && !"true".equalsIgnoreCase(isExpTask)){
					if(!todaysDate.equals(constraintDate)){
						blockTrigger = true;
						strConstraintTypei18 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
								"emxProgramCentral.TaskConstriant.TaskConstriant.MFON", languageStr);
						emxTaskBase_mxJPO.showErrorMessage(context, strConstraintTypei18, strTaskName, todaysDate, constraintDate);
					}
				}
			}else if(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_MSON.equals(strTaskConstraintType)){
				//Must Start On
				if(STATE_PROJECT_TASK_ACTIVE.equals(toState) && !"true".equalsIgnoreCase(isExpTask)){
					if(!todaysDate.equals(constraintDate)){
						blockTrigger = true;
						strConstraintTypei18 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
								"emxProgramCentral.TaskConstriant.TaskConstriant.MSON", languageStr);
						emxTaskBase_mxJPO.showErrorMessage(context, strConstraintTypei18, strTaskName, todaysDate, constraintDate);
					}
				}
			}else if(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET.equals(strTaskConstraintType)){
				//Start No Earlier Than
				if(STATE_PROJECT_TASK_ACTIVE.equals(toState) && !"true".equalsIgnoreCase(isExpTask)){
					if(todaysDate.before(constraintDate)){
						blockTrigger = true;
						strConstraintTypei18 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
								"emxProgramCentral.TaskConstriant.TaskConstriant.SNET", languageStr);
						emxTaskBase_mxJPO.showErrorMessage(context, strConstraintTypei18, strTaskName, todaysDate, constraintDate);
					}
				}
			}else if(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNLT.equals(strTaskConstraintType)){
				//Start No Later Than
				if(STATE_PROJECT_TASK_ACTIVE.equals(toState)){
					if(todaysDate.after(constraintDate) && !"true".equalsIgnoreCase(isExpTask)){
						blockTrigger = true;
						strConstraintTypei18 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
								"emxProgramCentral.TaskConstriant.TaskConstriant.SNLT", languageStr);
						emxTaskBase_mxJPO.showErrorMessage(context, strConstraintTypei18, strTaskName, todaysDate, constraintDate);
					}
				}
			}
		}

		if (blockTrigger)
		{
			// Preventing the trigger from blocking the promtotion of Task.
			// Below code returns 0 to make sure the trigger shows a warning message and the execution continues as expected.
			return 0;
		}
		else
		{
			return 0;
		}
			}
	/**
	 * isPlanningView - This will evaluate if the Task Constraint Type,Date columns should be shown in planning view or not
	 * It will also check if the project is of type concept
	 * @param context
	 * @param args
	 * @return
	 * @since R209
	 * @throws Exception
	 */
	public boolean isPlanningView(Context context,String args[]) throws Exception
	{
		try {
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String) inputMap.get("projectID");
			if(strObjectId==null){
				strObjectId = (String) inputMap.get("objectId");
			}
			String selectedTable = (String)inputMap.get("selectedTable");
			DomainObject dmoObject = DomainObject.newInstance(context,strObjectId);
			
			if(dmoObject.isKindOf(context, DomainConstants.TYPE_PROJECT_CONCEPT) || !selectedTable.contains("PMCWBSViewTable")){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * getPMCWBSPlanningViewsTable - Will generate table based on PMCWBSViewTable and add some dynamic columns
	 * in new WBS Planning view
	 * @param context
	 * @param args
	 * @return
	 * @since R209
	 * @throws FrameworkException
	 */
	public static MapList getPMCWBSPlanningViewTable(Context context, String[]args) throws FrameworkException{
		MapList mlColumns = new MapList();
		MapList mlToRender = new MapList();
		try {
			Map hmTablePMCWBSView = new HashMap();
			HashMap mapSettings = new HashMap();
			//Code for dynamically creating command "APPDiscussionCommand"
			UITable uiCmdDiscussion = new UITable();

			mlColumns = uiCmdDiscussion.getColumns(context,"PMCWBSViewTable",null);
			String languageStr = context.getSession().getLanguage();

			StringList types = new StringList();
			String strConstraintTypei18 ="";
			strConstraintTypei18 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskConstriant.WBSPlanningView.ExcludeColumns",Locale.US);
			StringList slExcludeColumns = FrameworkUtil.splitString(strConstraintTypei18, ",");
			for(Iterator itr = mlColumns.iterator();itr.hasNext();){
				Map ColumnMap = (Map)itr.next();
				String strColumnName = (String)ColumnMap.get("name");

				//Added:16-Feb-10:vm3:R209:PRG:Bug 030734
				String strConstraintTypei181 = "";
				String languageString = context.getSession().getLanguage();
				strConstraintTypei181 = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.TaskConstriant.WBSPlanningView.ConstraintsColumns", languageStr);
				if (strConstraintTypei181.equals(strColumnName))
				{
					Map settingsMap = (Map) ColumnMap.get("settings");
					settingsMap.put("Column Type","program");
					settingsMap.put("Printer Friendly","true");
					ColumnMap.put("settings", settingsMap);
				}
				//End-Added:16-Feb-10:vm3:R209:PRG:Bug 030734

				if(!slExcludeColumns.contains(strColumnName)){
					mlToRender.add(ColumnMap);
				}
				//System.out.println("ColumnMap : "+ColumnMap);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mlToRender;
	}
	//END:R209:PRG:WBS Task Constraint

	/**
	 * This function modifies the attribute Estimated Duration Keyword
	 * If Duration Field is modified then this method will check and reset duration keyword value if required.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 *
	 */
	public int triggerModifyDurationKeyword(Context context, String[] args) throws MatrixException {

		final String SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD = "attribute[" + DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD + "]";
		final String SELECT_PREFERRED_DURATION_UNIT = "attribute["+ DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "].inputunit";
		StringList slSelectList = new StringList();
		slSelectList.add(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
		slSelectList.add(Task.SELECT_TASK_ESTIMATED_DURATION);
		slSelectList.add(SELECT_PREFERRED_DURATION_UNIT);
		slSelectList.add(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);
		String objectId = args[0];

		try {
			setId(objectId);
			Map mp = getInfo(context,slSelectList);

			String strDurationKeyword = (String)mp.get(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
			String strDuration  = (String)mp.get(Task.SELECT_TASK_ESTIMATED_DURATION);
			String strPreferredDurationUnit = (String)mp.get(SELECT_PREFERRED_DURATION_UNIT);
			String isKindofTaskManagement = (String)mp.get(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);

			if("TRUE".equalsIgnoreCase(isKindofTaskManagement)) {

				if(null!=strDurationKeyword && !"".equals(strDurationKeyword) && !"null".equals(strDurationKeyword)) {

					Unit unit = null;
					UnitList unitList = new UnitList();
					unitList = emxDurationKeywordsBase_mxJPO.getUnitList(context,DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION);
					Map unitsLabelMap = new HashMap();
					if(unitList.size()>0)
					{
						for (int i = 0; i < unitList.size(); i++)
						{
							unit = (Unit)unitList.get(i);
							unitsLabelMap.put(unit.getName(), unit);
						}
					}
					unit = (Unit) unitsLabelMap.get(strPreferredDurationUnit);
					if (unit != null)
					{
						String strProjectId = getProjectId(context);
						DurationKeywords durationKeywords = new DurationKeywords(context, strProjectId);
						//Added for special character.(Passed context)
						DurationKeyword [] sDurationKeywords = durationKeywords.getDurationKeywords(context,DurationKeyword.ATTRIBUTE_NAME, strDurationKeyword);
						double nTaskEstDuration = Task.parseToDouble(unit.denormalize(strDuration));
						String strTaskEstDurationUnit = unit.getName();
						if(null!=sDurationKeywords)
						{
							double nDuration = sDurationKeywords[0].getDuration();
							String strDurationUnit = sDurationKeywords[0].getUnit();
							if(nDuration!=nTaskEstDuration)
							{
								setAttributeValue(context, ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, "");
							}
							else if(!strDurationUnit.equals(strTaskEstDurationUnit))
							{
								setAttributeValue(context, ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, "");
							}
						}
					}
				}
			}
			return 0;
		}
		catch (Exception e)
		{
			throw new MatrixException(e);
		}
	}
	/**
	 * gets the task slack time.
	 * Used to display slack time column in Task Dependency table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectId - task OID
	 * @returns Object
	 * @throws Exception if the operation fails
	 * @since Program Central V6R2008-1
	 * @grade 0
	 */
	public StringList getTaskDependancyDurationKeyword(Context context, String[] args)
			throws Exception
			{
		StringList latTimeLsit = new StringList();
		StringList projectList = new StringList();
		HashMap programMap        = (HashMap) JPO.unpackArgs(args);
		Map paramList = (Map) programMap.get("paramList");
		String  objectId          = (String) paramList.get("objectId");
		setId(objectId);
		String strProjectId = objectId;
		if(isKindOf(context,DomainConstants.TYPE_TASK_MANAGEMENT))
		{
			strProjectId = getProjectId(context);
		}
		DurationKeyword[] sdurationKeyword = DurationKeywordsUtil.getDurationKeywordsValueForDependancy(context,strProjectId);
		MapList  objectList       = (MapList) programMap.get("objectList");
		String languageStr = (String) paramList.get("languageStr");

		Iterator objectListIterator = objectList.iterator();
		StringBuffer lagTime = new StringBuffer();
		int checking = 0;
		while (objectListIterator.hasNext())
		{
			Map objectMap = (Map) objectListIterator.next();
			String strId = XSSUtil.encodeForHTML(context,(String) objectMap.get(SELECT_ID));
			lagTime = new StringBuffer();
			if(checking==0)
			{
				lagTime.append("<script>");
				lagTime.append("function populateDurationUnit(checkBoxValue){");
				lagTime.append("var durationKeyword = \"durationKeyword_\"+checkBoxValue;");
				lagTime.append("var lag = \"lag_\"+checkBoxValue;");
				lagTime.append("var unit = \"unit_\"+checkBoxValue;");
				lagTime.append("var durationKeywordVal = XSSUtil.encodeForJavaScript(context, document.getElementById(durationKeyword).value);");
				lagTime.append("if(durationKeywordVal!=\"NotSelected\"){");
				lagTime.append("var temp = new Array();")
				.append("temp = durationKeywordVal.split('|');")
				.append("document.getElementById(lag).value = temp[1];")
				.append("document.getElementById(unit).value = temp[2];")
				.append("}else {document.getElementById(lag).value = \"\"; } }");
				lagTime.append("function checkDurationKeyword(checkBoxValue){");
				lagTime.append("var durationKeyword = \"durationKeyword_\"+checkBoxValue;");
				lagTime.append("var lag = \"lag_\"+checkBoxValue;");
				lagTime.append("var unit = \"unit_\"+checkBoxValue;");
				lagTime.append("var durationKeywordVal =  XSSUtil.encodeForJavaScript(context, document.getElementById(durationKeyword).value);");
				lagTime.append("if(durationKeywordVal!=\"NotSelected\"){");
				lagTime.append("var temp = new Array();")
				.append("temp = durationKeywordVal.split('|');")
				.append("if(!(document.getElementById(lag).value==temp[1]) {" )
				.append("if(document.getElementById(unit).value==temp[2]) ")
				.append("{ document.getElementById(durationKeyword).value=\"NotSelected\";")
				.append("}")
				.append("}")
				.append("}")
				.append("}");
				lagTime.append("</script>");
			}
			String strDurationKeyword = (String) objectMap.get(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
			lagTime.append("<select name=\"").append("durationKeyword_").append(strId)
			.append("\" id=\"").append("durationKeyword_").append(strId).append("\" onchange=\"populateDurationUnit('"+strId+"');\">");
			lagTime.append("<option value=\"NotSelected\"></option>");
			for (int i = 0; i < sdurationKeyword.length; i++) {
				DurationKeyword  durationKeyword = sdurationKeyword[i];
				lagTime.append("<option value=\"").append(durationKeyword.getName()+"|"+durationKeyword.getDuration()+"|"+durationKeyword.getUnit()).append("\"").append((durationKeyword.getName().equals(strDurationKeyword))?" selected='selected'":"").append(">")
				.append(XSSUtil.encodeForHTML(context,(String)durationKeyword.getName()))
				.append("</option>");
			}
			lagTime.append("</select>");

			latTimeLsit.add(lagTime.toString());
			checking ++;
		}
		return latTimeLsit;
			}

	public void checkDurationKeyword(Context context, Map attributeMap, String objectId)throws FrameworkException
	{
		try
		{
			setId(objectId);
			String strProjectId = objectId;
			String strDurationKeyword = (String)attributeMap.get(DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
			if(null!=strDurationKeyword && !"".equals(strDurationKeyword) && !"null".equals(strDurationKeyword))
			{
				String strLagDuration = (String)attributeMap.get(DependencyRelationship.ATTRIBUTE_LAG_TIME);
				double nLagDuration = 0;
				String strLagDurationUnit = "";
				try
				{
					nLagDuration =  Task.parseToDouble(strLagDuration.substring(0,strLagDuration.indexOf(" ")));
					strLagDurationUnit =  strLagDuration.substring(strLagDuration.indexOf(" "),strLagDuration.length());
				}
				catch (NumberFormatException ne)
				{
					nLagDuration = 0;
				}
				if(isKindOf(context,DomainConstants.TYPE_TASK_MANAGEMENT))
				{
					strProjectId = getProjectId(context);
				}
				DurationKeywords durationKeywords = new DurationKeywords(context, strProjectId);
				//Added for special character.(Passed context)
				DurationKeyword [] sDurationKeywords = durationKeywords.getDurationKeywords(context,DurationKeyword.ATTRIBUTE_NAME, strDurationKeyword);
				if(null!=sDurationKeywords)
				{
					double nDuration = sDurationKeywords[0].getDuration();
					String strDurationUnit = sDurationKeywords[0].getUnit();
					if(nDuration!=nLagDuration)
					{
						attributeMap.put(DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, "");
					}
					else if(!strDurationUnit.equals(strLagDurationUnit))
					{
						attributeMap.put(DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, "");
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new FrameworkException(e);
		}
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean isWBSDurationKeywordExists(Context context, String args[]) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String)inputMap.get("objectId");
		setId(strObjectId);
		DurationKeyword [] durationKeyword;
		try
		{
			ContextUtil.pushContext(context);
			durationKeyword = DurationKeywordsUtil.getDurationKeywordsValueForWBS(context, strObjectId);
		}
		finally
		{
			ContextUtil.popContext(context);
		}
		boolean blAccess = false;
		if(null!=durationKeyword && durationKeyword.length>0)
		{
			blAccess = true;
		}
		return blAccess;
	}

	/**
	 * @param context
	 * @return
	 * @throws FrameworkException
	 */
	private String getProjectId(Context context) throws FrameworkException
	{
		String strObjectId;
		StringList busSelects = new StringList(1);
		busSelects.add(SELECT_ID);
		Map projectInfo = getProject(context, busSelects);
		strObjectId = (String) projectInfo.get(SELECT_ID);
		return strObjectId;
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean isDependencyDurationKeywordExists(Context context, String args[]) throws Exception
	{
		boolean blAccess = false;
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String)inputMap.get("objectId");
		if(!(ProgramCentralUtil.isNullString(strObjectId))){
			setId(strObjectId);
			DurationKeyword [] durationKeyword = DurationKeywordsUtil.getDurationKeywordsValueForDependancy(context, strObjectId);
			if(null!=durationKeyword && durationKeyword.length>0)
			{
				blAccess = true;
			}
		}
		return blAccess;
	}
	//Added:For Advanced Project Business Calendar:PRG:nr2:10/12/09
	public String getTaskCalendar(Context context,String strObjectId)
			throws FrameworkException{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task)DomainObject.newInstance(context,DomainConstants.TYPE_TASK,"PROGRAM");
		task.setId(strObjectId);

		return task.getTaskCalendar(context);
	}

	public String getOwnerCalendar(Context context,String strObjectId)
			throws FrameworkException{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		task.setId(strObjectId);

		return task.getOwnerCalendar(context);
	}

	public String getProjectCalendar(Context context,String strObjectId)
			throws FrameworkException{

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		task.setId(strObjectId);
		return task.getProjectCalendar(context);
	}


	/**
	 * Gives non working days for the calendar associated with the given task or project.
	 * @param context the ENOVIA Context object.
	 * @param args request arguments
	 * @return A map of non working days for the given project or task.
	 * @throws Exception if operation fails.
	 */
	public Map getNonWorkingDays(Context context,String[] args) throws Exception{
		Map returnVal = new HashMap();
		String strObjectId = EMPTY_STRING;
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			strObjectId = (String) requestMap.get("objectId");
			String strStartDate = (String)requestMap.get("calStDate");
			String strEndDate = (String)requestMap.get("calLastDate");
			Calendar cal = GregorianCalendar.getInstance();
			String nonWorkingDayIden = "NON-WORKING";

			Task task = (Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK,PROGRAM);
			task.setId(strObjectId);
			WorkCalendar workCalendar = task.getSchedulingCalendar(context);


			Locale locale 			= (Locale)requestMap.get("localeObj");
			double clientTZOffset 	= Task.parseToDouble((String)(requestMap.get("timeZone")));
			int iDateFormat 		= eMatrixDateFormat.getEMatrixDisplayDateFormat();
			DateFormat format 		= DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, locale);

			strStartDate = eMatrixDateFormat.getFormattedInputDate(context,strStartDate, clientTZOffset,locale);
			strStartDate = eMatrixDateFormat.getFormattedDisplayDateTime(context, strStartDate, true,iDateFormat, clientTZOffset,locale);
			Date startDate 	= format.parse(strStartDate);

			strEndDate = eMatrixDateFormat.getFormattedInputDate(context,strEndDate, clientTZOffset,locale);
			strEndDate = eMatrixDateFormat.getFormattedDisplayDateTime(context, strEndDate, true,iDateFormat, clientTZOffset,locale);
			Date endDate 	= format.parse(strEndDate);

			try{
				ProgramCentralUtil.pushUserContext(context);
				nonWorkingDayIden = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.NonWorkingDay.identifier", "en");


			}finally{
				ProgramCentralUtil.popUserContext(context);
			}

			Map holidayListMap = new HashMap();

			if(workCalendar == null){
				cal.setTime(startDate);
				while(endDate.after(startDate)){
					int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

					//By default Saturday and Sunday will be off
					if(dayOfWeek==7 || dayOfWeek==1){
						holidayListMap.put(startDate,nonWorkingDayIden);
					}
					cal.add(Calendar.DAY_OF_YEAR, 1);
					startDate = cal.getTime();
				}
			}
			else {
				try{
					ProgramCentralUtil.pushUserContext(context);
					nonWorkingDayIden = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.NonWorkingDay.identifier", "en");

					if(ProgramCentralUtil.isNullString(nonWorkingDayIden) || EMPTY_STRING.equals(nonWorkingDayIden)){
						nonWorkingDayIden = "NON-WORKING";
					}
					Set holidayListSet = ((WorkCalendar)workCalendar).getHolidays(context,startDate,endDate);

					Iterator itr = holidayListSet.iterator();
					while(itr.hasNext()){
						holidayListMap.put(itr.next(),nonWorkingDayIden);
					}

				}finally{
					ProgramCentralUtil.popUserContext(context);
				}

			}
			returnVal.putAll(holidayListMap);

		}
		catch (Exception e){
			e.getMessage();
		}
		return returnVal;
	}

	public Map getCompanyNonWorkingDays(Context context,String[] args) throws Exception{
		Map returnVal = new HashMap();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String strStartDate = (String)requestMap.get("calStDate");
			String strEndDate = (String)requestMap.get("calLastDate");
			

			TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
			double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
			double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
			int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
			Locale locale = context.getLocale();
			java.text.DateFormat format = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, locale);

			String strNewStartDateVal = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context, strStartDate, clientTZOffset, locale);
			String strNewEndDateVal = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context, strEndDate, clientTZOffset, locale);

			Date startDate = eMatrixDateFormat.getJavaDate(strNewStartDateVal);
			Date endDate = eMatrixDateFormat.getJavaDate(strNewEndDateVal);
			
			WorkCalendar workcalendar = WorkCalendar.getDefaultCalendar();
			Set holidayListSet = workcalendar.getHolidays(context,startDate,endDate);
			
			String nonWorkingDayIden = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.NonWorkingDay.identifier", "en"); 

			if(ProgramCentralUtil.isNullString(nonWorkingDayIden) || EMPTY_STRING.equals(nonWorkingDayIden))
				nonWorkingDayIden = "NON-WORKING";
			Map holidayListMap = new HashMap();
			Iterator itr = holidayListSet.iterator();
			while(itr.hasNext()){
				holidayListMap.put(itr.next(),nonWorkingDayIden);
			}
			
			returnVal.putAll(holidayListMap);
			/*

			StringList WorkCaledDarIdList = new StringList();
			String WorkCalendarId = EMPTY_STRING;

			//If no Calendar Attached Create a Calendar
			String languageStr = context.getSession().getLanguage();
			String TYPE_WORK_CALENDAR = ProgramCentralConstants.TYPE_WORK_CALENDAR;
			String CALENDAR = "Calendar";

			//String mqlCmd = "temp query bus '" + TYPE_WORK_CALENDAR + "' DefaultCalendar 1 select id dump |;";
			String mqlCmd = "temp query bus $1 $2 $3 select $4 dump $5 $6";
			String calendarIdStr = EMPTY_STRING;
			try{
				ProgramCentralUtil.pushUserContext(context);
				calendarIdStr = MqlUtil.mqlCommand(context,mqlCmd,TYPE_WORK_CALENDAR,"DefaultCalendar","1","id","|",";");
			}finally{
				ProgramCentralUtil.popUserContext(context);
			}

			WorkCalendar wc = null;
			if(ProgramCentralUtil.isNullString(calendarIdStr) || EMPTY_STRING.equals(calendarIdStr)){
				try{
					ProgramCentralUtil.pushUserContext(context);
					BusinessObject bo = new BusinessObject(TYPE_WORK_CALENDAR,"DefaultCalendar","1",ProgramCentralConstants.VAULT_eSERVICE_PRODUCTION);
					bo.create(context, CALENDAR);
					wc = new WorkCalendar(bo);
				}
				catch(Exception e){
					throw new MatrixException(e);
				}
				finally{
					ProgramCentralUtil.popUserContext(context);
				}
				String[] titleAndNotes = {"Saturday","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday"};
				String dayNumberStr = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DefaultCalendarHighlighting.WeeklyNonWorkingDays", languageStr);
				if(ProgramCentralUtil.isNullString(dayNumberStr) ||EMPTY_STRING.equals(dayNumberStr))
					dayNumberStr = "0,1";   //0=Saturday,1=Sunday

				StringList dayNumberStrList = FrameworkUtil.split(dayNumberStr, ",");
				Map attributeMap = new HashMap();
				for(int i=0;i<dayNumberStrList.size();i++){
					int value = Integer.valueOf(dayNumberStrList.get(i).toString());
					attributeMap.put(ATTRIBUTE_TITLE, titleAndNotes[value]);
					attributeMap.put(ATTRIBUTE_NOTES, titleAndNotes[value]);
					attributeMap.put(ProgramCentralConstants.ATTRIBUTE_FREQUENCY, "1");
					attributeMap.put(ProgramCentralConstants.ATTRIBUTE_DAY_NUMBER, "" + value);
					try{
						ProgramCentralUtil.pushUserContext(context);
						wc.createEvent(context, attributeMap);
					}
					catch(Exception e){
						throw new MatrixException(e);
					}
					finally{
						ProgramCentralUtil.popUserContext(context);
					}
					attributeMap.clear();
				}
				WorkCalendarId = wc.getInfo(context, SELECT_ID);
			}
			else{
				StringList calIdStr = FrameworkUtil.split(calendarIdStr, "\\|");
				WorkCalendarId = calIdStr.get(calIdStr.size() - 1 ).toString();
			}
			if(!"".equals(WorkCalendarId))
				WorkCaledDarIdList.add(WorkCalendarId);
			String nonWorkingDayIden = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.NonWorkingDay.identifier", "en");

			if(ProgramCentralUtil.isNullString(nonWorkingDayIden) || EMPTY_STRING.equals(nonWorkingDayIden))
				nonWorkingDayIden = "NON-WORKING";

			Map holidayListMap = new HashMap();
			try{
				ProgramCentralUtil.pushUserContext(context);
				for(int i=0;i < WorkCaledDarIdList.size();i++){
					String calId = (String)WorkCaledDarIdList.get(i);
					WorkCalendar workcalendar = new WorkCalendar(calId);
					Set holidayListSet = workcalendar.getHolidays(context,strStartDate,strEndDate);
					Iterator itr = holidayListSet.iterator();
					while(itr.hasNext()){
						holidayListMap.put(itr.next(),nonWorkingDayIden);
					}
				}
			}finally{
				ProgramCentralUtil.popUserContext(context);
			}
			returnVal.putAll(holidayListMap);*/
		}
		catch (Exception e){
			e.getMessage();
		}
		return returnVal;
	}

	//Added:nr2:27/01/10:PRG:IR-029184V6R2011
	/**
	 * This method is used to get choices for Policy for the task
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns HashMap containing Policy.
	 * @throws Exception if the operation fails
	 * @since PMC 11.Next
	 */
	public HashMap getPMCTaskPolicies(Context context,String[] args) throws Exception {

		HashMap returnMap = new HashMap();
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");

			String objectId = (String)paramMap.get("objectId");
			String languageStr = context.getSession().getLanguage();
			String policyName = "";
			String i18npolicyName = "";
			StringList fieldRangeValue = new StringList();
			StringList fieldDisplayRangeValue = new StringList();
			DomainObject task = DomainObject.newInstance(context, objectId);
			StringList busSelect = new StringList();
			busSelect.add(SELECT_TYPE);
			busSelect.add(SELECT_POLICY);
			busSelect.add("from["+RELATIONSHIP_SUBTASK+"].to."+SELECT_POLICY);
			busSelect.add("to["+RELATIONSHIP_SUBTASK+"].from."+SELECT_POLICY);

			//Map taskInfo = task.getInfo(context, busSelect);
			String[] taskIds = {objectId};
			MapList mlTaskDetails = DomainObject.getInfo(context, taskIds, busSelect); 
			Map taskInfo = (Map)mlTaskDetails.get(0);
			String hasProjectTask  = (String)taskInfo.get("from["+RELATIONSHIP_SUBTASK+"].to."+SELECT_POLICY);
			String hasProjectReview  = (String)taskInfo.get("to["+RELATIONSHIP_SUBTASK+"].from."+SELECT_POLICY);

			if(("Project Task".equalsIgnoreCase((String)taskInfo.get(SELECT_POLICY)) && ProgramCentralUtil.isNotNullString(hasProjectTask) && hasProjectTask.indexOf("Task")>=0 )){
				fieldRangeValue.add(ProgramCentralConstants.POLICY_PROJECT_TASK);
				i18npolicyName=EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Policy.Project_Task");
				fieldDisplayRangeValue.add(i18npolicyName);

				returnMap.put("field_choices",fieldRangeValue);
				returnMap.put("field_display_choices",fieldDisplayRangeValue);
				return returnMap;
			}else if(("Project Review".equalsIgnoreCase((String)taskInfo.get(SELECT_POLICY)) && ProgramCentralUtil.isNotNullString(hasProjectReview) && "Project Review".equalsIgnoreCase(hasProjectReview) )){
				fieldRangeValue.add(ProgramCentralConstants.POLICY_PROJECT_REVIEW);
				i18npolicyName=EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Policy.Project_Review");
				fieldDisplayRangeValue.add(i18npolicyName);

				returnMap.put("field_choices",fieldRangeValue);
				returnMap.put("field_display_choices",fieldDisplayRangeValue);
				return returnMap;
			}else if(task.isKindOf(context,ProgramCentralConstants.TYPE_GATE) || task.isKindOf(context,ProgramCentralConstants.TYPE_MILESTONE)){

				fieldRangeValue.add(ProgramCentralConstants.POLICY_PROJECT_TASK);
				fieldRangeValue.add(ProgramCentralConstants.POLICY_PROJECT_REVIEW);
				i18npolicyName=EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Policy.Project_Task");
				fieldDisplayRangeValue.add(i18npolicyName);
				i18npolicyName=EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Policy.Project_Review");
				fieldDisplayRangeValue.add(i18npolicyName);

				returnMap.put("field_choices",fieldRangeValue);
				returnMap.put("field_display_choices",fieldDisplayRangeValue);
				return returnMap;
			}

			//Added:10-May-10:ak4:R210:ECH Enhancement
			String type=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Type.ChangeTask");
			//End:10-May-10:ak4:R210:ECH Enhancement
			//Modified:10-May-10:ak4:R210:ECH Enhancement
			if(task.isKindOf(context,DomainConstants.TYPE_TASK)||type.equalsIgnoreCase((String)taskInfo.get(SELECT_TYPE))){
				policyName = (String)taskInfo.get(SELECT_POLICY);
			}
			else{
				return returnMap;
			}
			fieldRangeValue.add(policyName);
			i18npolicyName=EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Policy.Project_Task");
			fieldDisplayRangeValue.add(i18npolicyName);
			returnMap.put("field_choices",fieldRangeValue);
			returnMap.put("field_display_choices",fieldDisplayRangeValue);
			return returnMap;
		}
		catch (FrameworkException fxe){
			fxe.printStackTrace();
		}
		finally{
			return returnMap;
		}
	}
	//End:nr2:27/01/10:PRG:IR-029184V6R2011

	//Added:14-May-2010:s4e:R210 PRG:WBSEnhancement
	/**
	 * This method is used to update assignee role from Assignee Summary page.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args   *
	 *  @throws Exception if the operation fails     *
	 */
	public void  updateAssigneeRole(Context context, String[] args) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map paramMap = (Map)programMap.get("paramMap");
		String strAssigneeRole = (String)paramMap.get("New Value");
		if(null!=strAssigneeRole && !"null".equalsIgnoreCase(strAssigneeRole) && !"".equals(strAssigneeRole))
		{
			boolean flag = false;
			try {
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				flag = true;
				String strRelationshipId = (String)paramMap.get("relId");
				DomainRelationship relDo = DomainRelationship.newInstance(context, strRelationshipId);
				relDo.setAttributeValue(context,ATTRIBUTE_ASSIGNEE_ROLE , strAssigneeRole);
			}
			catch (Exception e)
			{
				throw new MatrixException(e);

			}
			finally
			{
				if(flag)
				{
					ContextUtil.popContext(context);
				}
			}
		}
	}
	//End:14-May-2010:s4e:R210 PRG:WBSEnhancement

	//Added:14-May-2010:s4e:R210 PRG:WBSEnhancement
	/**
	 * This method is used to update Percent Allocation from Assignee Summary page.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args   *
	 *  @throws Exception if the operation fails     *
	 */
	public void  updateAllocation(Context context, String[] args) throws Exception{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map paramMap = (Map)programMap.get("paramMap");
		Map requestMap = (Map)programMap.get("requestMap");
		String strTaskId = (String)requestMap.get("objectId");
		String strPercentAllocation = (String)paramMap.get("New Value");
		String strAssigneeId = (String)paramMap.get("objectId");
		String strUser=context.getUser();
		String strLanguage = context.getSession().getLanguage();
		StringBuffer sbAssigneeName =  new StringBuffer();

		boolean isContextUserValid = false;
		final String SELECT_PROJECT_ID = "to[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
		final String SELECT_TASK_ASSIGNEE = "to[" + ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS + "].from.name";
		StringList busSelect = new StringList();
		busSelect.add(ProgramCentralConstants.SELECT_CURRENT);
		busSelect.add(SELECT_PROJECT_ID);
		busSelect.add(SELECT_TASK_ASSIGNEE);
		DomainObject dmoTaskObject = DomainObject.newInstance(context, strTaskId);
		String[] taskIds = {strTaskId};
		MapList taskInfoMapList = dmoTaskObject.getInfo(context, taskIds, busSelect);
		StringList taskAssignees = new StringList();
		Map taskInfoMap = new HashMap();
		String strCurrentTaskState = "";
		for (Iterator itrTaskInfoList = taskInfoMapList.iterator(); itrTaskInfoList.hasNext();) {
			taskInfoMap = (Map) itrTaskInfoList.next();
			strCurrentTaskState = (String) taskInfoMap.get(SELECT_CURRENT);
			String assignee = (String)taskInfoMap.get(SELECT_TASK_ASSIGNEE);
			taskAssignees = FrameworkUtil.split(assignee, matrix.db.SelectConstants.cSelectDelimiter);
		}
		//If context user is task assignee, the user is valid
		if(taskAssignees.contains(strUser))
			isContextUserValid = true;

		//Else, if user has Project Lead access, the user is valid
		else{
        	MapList results = DomainAccess.getAccessSummaryList(context, strTaskId);
        	Iterator resultsItr = results.iterator();
        	while(resultsItr.hasNext()){
        		Map mapObjects = (Map) resultsItr.next();
        		String projectAccess = (String)mapObjects.get("access");
        		String userName = (String)mapObjects.get(SELECT_NAME);
        		if(userName.endsWith("_PRJ")){
        			userName = userName.replace("_PRJ","");
        			if(userName.equalsIgnoreCase(strUser) ){
        				if(projectAccess.equalsIgnoreCase(ROLE_PROJECT_LEAD)){
					isContextUserValid = true;
					break;
				}
        				else{
        					isContextUserValid = false;
        					break;
        				}
        			}
        		}
			}
		}

		//      if((contextUserRole.contains(ROLE_PROJECT_LEAD) || strUser.equalsIgnoreCase(strAssignee)) && !ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equalsIgnoreCase(strCurrentTaskState))
		if(isContextUserValid && !ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equalsIgnoreCase(strCurrentTaskState)){
			if(strPercentAllocation == null ){
				strPercentAllocation="100.0";
			}
			try{
				if(new Double(strPercentAllocation).doubleValue() < 1){
					strPercentAllocation="1.0";
				}
				if(new Double(strPercentAllocation).doubleValue() > 1000){
					strPercentAllocation="100.0";
				}
			}catch(Exception ex){
				strPercentAllocation="100.0";
			}
			boolean flag = false;
			try {
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				flag = true;
				String strRelationshipId = (String)paramMap.get("relId");
				DomainRelationship relDo = DomainRelationship.newInstance(context, strRelationshipId);
				relDo.setAttributeValue(context,ATTRIBUTE_PERCENT_ALLOCATION, strPercentAllocation);
			}
			catch (Exception e) { }
			finally{
				if(flag){
					ContextUtil.popContext(context);
				}
			}
		}
		else{
			if(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equalsIgnoreCase(strCurrentTaskState)){
				String strErrorMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.TaskAssignment.Error", context.getSession().getLanguage());
				emxContextUtilBase_mxJPO.mqlNotice(context, dmoTaskObject.getName()+": "+strErrorMsg);
			}else{
				com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
				person.setId(strAssigneeId);
				StringList nameSelects = new StringList(2);
				nameSelects.add(person.SELECT_NAME);
				nameSelects.add(person.SELECT_LAST_NAME);
				nameSelects.add(person.SELECT_FIRST_NAME);
				Map personInfoMap = person.getInfo(context, nameSelects);
				String strAssignee = (String)personInfoMap.get(person.SELECT_NAME);
				String strAssigneeLastName = (String)personInfoMap.get(person.SELECT_LAST_NAME);
				String strAssigneeFirstName = (String)personInfoMap.get(person.SELECT_FIRST_NAME);

				String strTxtNotice = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.Task.NoAccessOnPercentAllocation", strLanguage);
				sbAssigneeName.append(strAssigneeLastName);
				sbAssigneeName.append(",");
				sbAssigneeName.append(strAssigneeFirstName);
				strTxtNotice = strTxtNotice+sbAssigneeName.toString();
				emxContextUtilBase_mxJPO.mqlNotice(context, strTxtNotice);
				return;
			}
		}

	}

	//This method is will check for the commands which will be visible only in WBSDeleted view.
	/**
	 * Where : In the Structure Browser, All the Actions links at WBSDeletedView
	 *
	 * How : Get the objectId from argument map, show the link based on following conditions
	 *           *
	 * Settings Required :
	 *      AccessProgram /Function
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "objectId"
	 *        1 - String containing "selectedTable"
	 * @returns boolean
	 * @throws Exception if operation fails
	 * @since PMC V6R2008-1
	 */

	public boolean hasAccessForWBSDeletedTaskView(Context context, String args[]) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		emxProjectMember_mxJPO emxProjectMember = new emxProjectMember_mxJPO(context, args);
		boolean projectAccess = emxProjectMember.hasAccess(context, args);//Only checks for Modify access.

		String strTable = (String)inputMap.get("selectedTable");
		String strProgram = (String)inputMap.get("selectedProgram");

		boolean blAccess = false;

		// Baseline is Part of Command Name PMCWBSBaselineView
		//selectedProgram=emxTask:getWBSAllSubtasks
		//selectedProgram=emxTask:getWBSDeletedtasks

		if((strProgram!=null && strProgram.indexOf("Deleted")>0)){
			blAccess = true;
		}
		else {
			blAccess = false;
		}
		return blAccess;
	}


	/**
	 *
	 * It checks the Assignees command from the category menu of the Gates present in WBS of a project template.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments like objectId.
	 *
	 * @returns boolean
	 * @throws Exception if operation fails
	 */
	public boolean hasAccessForAssignee(Context context, String args[]) throws Exception
	{
		boolean hasAccess = true;
		Map programMap =  JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		DomainObject object = DomainObject.newInstance(context, objectId);

		Map parentObjMap = getParentProjectMap(context, object);

		if( parentObjMap != null )
		{
			String parentObjId = (String) parentObjMap.get(SELECT_ID);
			try {
				ContextUtil.pushContext(context);
				DomainObject parentObj = DomainObject.newInstance(context, parentObjId);
				if (parentObj.isKindOf(context, TYPE_PROJECT_TEMPLATE)
						&& object.isKindOf(context,
								ProgramCentralConstants.TYPE_GATE)) {
					hasAccess = false;
					return hasAccess;
				}
			} finally {
				ContextUtil.popContext(context);
			}

		}
		boolean isExperimentOrBaselineTask = hasAccessForTaskManagementCategoryCommand(context, args);
		hasAccess = hasAccess && isExperimentOrBaselineTask;
		return hasAccess;
	}

	/**
	 * This method checks whether the Document Create command should be disable or not if the person is not the owner and Assignee.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception if the operation fails
	 */

	public boolean hasAccessForDeliverable(Context context, String args[]) throws Exception	{

		Map programMap = (Map) JPO.unpackArgs(args);
		String strProjectID = (String)programMap.get("objectId");
		DomainObject dmoObject = DomainObject.newInstance(context,strProjectID);
		String hasModifySelect = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current.access[modify]";
        SelectList objectSelects = new SelectList(3);

		objectSelects.addElement(SELECT_TASK_ASSIGNEE_ID);
        objectSelects.addElement(SELECT_OWNER);
		objectSelects.addElement(hasModifySelect);
        objectSelects.addElement(ProgramCentralConstants.SELECT_PROJECT_POLICY_FROM_TASK);
		objectSelects.addElement(ProgramCentralConstants.SELECT_IS_BUSINESS_GOAL);
		Map objectMap = dmoObject.getInfo(context, objectSelects);
		boolean hasModifyAccess = Boolean.parseBoolean((String)objectMap.get(hasModifySelect));
		if(Boolean.parseBoolean((String)objectMap.get(ProgramCentralConstants.SELECT_IS_BUSINESS_GOAL)))
			 hasModifyAccess=true;
        String projectPolicy = (String)objectMap.get(ProgramCentralConstants.SELECT_PROJECT_POLICY_FROM_TASK);
		StringList assigneeList = (StringList) objectMap.get(SELECT_TASK_ASSIGNEE_ID);
        String owner = (String)objectMap.get(SELECT_OWNER);

        String loggedInUser = context.getUser();

        if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy))
    	{
    		return false;
    	}
		if(assigneeList == null) {
            if(hasModifyAccess || loggedInUser.equals(owner) ){
				return true;
			}
			return false;
		}
		String[] assignedTask = (String[]) assigneeList.toArray(new String[assigneeList.size()]);
		SelectList nameSelects = new SelectList();
		nameSelects.addElement(SELECT_NAME);
		MapList mObjectInfo = DomainObject.getInfo(context ,assignedTask ,nameSelects );
		Iterator itr = mObjectInfo.iterator();
		while(itr.hasNext()) {
			Map strAssigeeId = (Map) itr.next();
			String strAssigneeName = (String)strAssigeeId.get(SELECT_NAME);
            if(hasModifyAccess|| strAssigneeName.equals(loggedInUser))
            {
				return true;
			}
		}
		return false;
	}

	//Added:21-May-2010:DI1:R210 PRG:WBS Enhancement General

	/*
	 * This method verifies the value of Actual Start Date is before the
	 * Actual Start Date for a Task in Complete state or just update it in Review/Active state
	 * When a Task is in Create/Assign state it updates the Actual Start date promoting the task's state to Active state
	 *
	 * @param context the eMatrix <code>Context</code> object
	 *
	 * @param args holds the following input arguments: 0 - New Value String 1 -
	 * Old Value String
	 *
	 * @return void
	 *
	 * @throws Exception if the operation fails
	 *
	 * @since PMC 10-6
	 */

	public void updateTaskActualStartDate(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map mpParamMap = (HashMap) programMap.get("paramMap");
		//Added:13-OCT-10:S2E:R210:PRG TVT issue:WBS Enhancement General-change real startdate of project in gridmode -FR
		Map requestMap = (Map) programMap.get("requestMap");
		//End:13-OCT-10:S2E:R210:PRG TVT issue:WBS Enhancement General-change real startdate of project in gridmode -FR
		String strNewVal = (String) mpParamMap.get("New Value");
		String strOldVal = (String) mpParamMap.get("Old Value");
		String strObjectId = (String) mpParamMap.get("objectId");
		String SELECT_ACT_START_DATE = "attribute["
				+ PropertyUtil.getSchemaProperty(context,
						"attribute_TaskActualStartDate") + "]";
		String SELECT_ACT_FINISH_DATE = "attribute["
				+ PropertyUtil.getSchemaProperty(context,
						"attribute_TaskActualFinishDate") + "]";

		if(ProgramCentralUtil.isNotNullString(strNewVal)){
		TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
		double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
		double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();

		int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();

		Locale locale = (Locale)requestMap.get("locale");
		java.text.DateFormat format = DateFormat.getDateTimeInstance(
				iDateFormat, iDateFormat, locale);

		String strNewDateVal = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context, strNewVal, clientTZOffset, locale);

		Date dtNewValue = eMatrixDateFormat.getJavaDate(strNewDateVal);
		Calendar calToday = Calendar.getInstance();
		calToday.setTime(dtNewValue);
		strNewVal = format.format((calToday.getTime()));

		if (ProjectSpace.isEPMInstalled(context)) {
			blockValue = "";
		}
		if (strNewVal == null) {
			strNewVal = "";
		}
		if (strOldVal == null) {
			strOldVal = "";
		}
		if (!strNewVal.equals(strOldVal)) {
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject
					.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			task.setId(strObjectId);

			String sPolicyName = PropertyUtil.getSchemaProperty(context,
					"policy_ProjectTask");
			String strPolicyName = PropertyUtil.getSchemaProperty(context,
					"policy_ProjectReview");

			String sActiveStateName = PropertyUtil.getSchemaProperty(context,
					"policy", sPolicyName, "state_Active");

			//Added:21-SEP-10:S2E:R210:PRG:Bug:IR-070213
			String sReviewStateName = PropertyUtil.getSchemaProperty(context,
					"policy", sPolicyName, "state_Review");
			//End:21-SEP-10:S2E:R210:PRG:Bug:IR-070213

			//Modified:9-Jun-2010:DI1:R210 PRG:056258
			String strFieldValueAttr = eMatrixDateFormat.getFormattedInputDateTime(
					context, strNewVal,START_DATE_SET_TIME, clientTZOffset, locale);
			String strTempfieldValueAttr = eMatrixDateFormat
					.getFormattedDisplayDateTime(context, strFieldValueAttr,
							true, iDateFormat, clientTZOffset, locale);
			Date newStartDate = format.parse(strTempfieldValueAttr);

			StringList busSelect = new StringList(4);
			//End Modified:9-Jun-2010:DI1:R210 PRG:056258
			busSelect.add(DomainConstants.SELECT_NAME);
			busSelect.add(SELECT_ACT_START_DATE);
			busSelect.add(SELECT_ACT_FINISH_DATE);
			busSelect.add(DomainConstants.SELECT_CURRENT);
			//Added:21-SEP-10:S2E:R210:PRG:Bug:IR-070213
			busSelect.add(DomainConstants.SELECT_POLICY);
			//End:21-SEP-10:S2E:R210:PRG:Bug:IR-070213
			Map taskData = (Map) task.getInfo(context, busSelect);
			String taskName = (String) taskData.get(DomainObject.SELECT_NAME);
			String strActualStartDate = (String) taskData.get(SELECT_ACT_START_DATE);
			String strActualFinishDate = (String) taskData.get(SELECT_ACT_FINISH_DATE);

			String strNewStartDate = format.format(newStartDate).trim();

			strNewStartDate = eMatrixDateFormat
					.getFormattedInputDateTime(context,
							strNewStartDate,START_DATE_SET_TIME, clientTZOffset, locale);
			try {
				if (ProjectSpace.isEPMInstalled(context)) {
					String strCurState = (String) taskData
							.get(task.SELECT_CURRENT);
					if (!STATE_PROJECT_TASK_ASSIGN.equals(strCurState)) {
						boolean blnUpdate = false;
						Date fDate = new Date();
						Date sDate = new Date();
						Integer duration;
						if (strCurState.equals(STATE_PROJECT_TASK_ACTIVE)
								|| strCurState
								.equals(STATE_PROJECT_TASK_REVIEW)) {
							blnUpdate = true;
							PropertyUtil.setGlobalRPEValue(context,
									"BLOCK_CALL_PROMOTE_ACTIVE", "true");
							task.setState(context, sActiveStateName);
							String strVal = PropertyUtil.getGlobalRPEValue(
									context, "BLOCK_CALL_MESSAGE");
							if (strVal != null
									&& "true".equalsIgnoreCase(strVal)) {
								task.setState(context, strCurState);
							}
						}

						if (strCurState.equals(STATE_PROJECT_TASK_COMPLETE)) {
							blnUpdate = true;
						}

						if (blnUpdate) {
							blockValue = PropertyUtil.getGlobalRPEValue(
									context, "BLOCK_CALL_PROMOTE_ACTIVE");
							if (!"true".equalsIgnoreCase(blockValue)) {
								task
								.setAttributeValue(
										context,
										task.ATTRIBUTE_TASK_ACTUAL_START_DATE,
										strNewStartDate);
							}
							fDate = format.parse(strNewStartDate);
						}
						if (strActualStartDate != null
								&& !"".equals(strActualStartDate)
								&& !"null".equals(strActualStartDate)) {
							sDate = eMatrixDateFormat.getJavaDate(
									strActualStartDate, locale);
							duration = new Integer((int) DateUtil
									.computeDuration(sDate, fDate));
							if (!"true".equalsIgnoreCase(blockValue)) {
								task
								.setAttributeValue(
										context,
										task.ATTRIBUTE_TASK_ACTUAL_DURATION,
										duration.toString());
							}
						}
					} else {
						PropertyUtil.setGlobalRPEValue(context,
								"BLOCK_CALL_PROMOTE_ACTIVE", "true");
						task.setState(context, sActiveStateName);
						String strValue = PropertyUtil.getGlobalRPEValue(
								context, "BLOCK_CALL_MESSAGE");
						if ("true".equalsIgnoreCase(strValue)) {
							task.setState(context,
									STATE_PROJECT_TASK_ASSIGN);
						}
					}

				}
				else {
					String strCurState = (String) taskData
							.get(task.SELECT_CURRENT);
					//Added:21-SEP-10:S2E:R210:PRG:Bug:IR-070213
					String policy = (String) taskData.get(SELECT_POLICY);
					//End:21-SEP-10:S2E:R210:PRG:Bug:IR-070213
					if (STATE_PROJECT_TASK_ASSIGN.equals(strCurState)
							|| STATE_PROJECT_TASK_CREATE
							.equals(strCurState)) {
						//Added:21-SEP-10:S2E:R210:PRG:Bug:IR-070213
						if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policy))
						{
							task.setState(context, sReviewStateName);
						}
						//End:21-SEP-10:S2E:R210:PRG:Bug:IR-070213
						else
						{
							task.setState(context, sActiveStateName);
						}
					}

					// call date roluup to save new user entry for %complete.
					Map objectList = new HashMap(1);
					Map objectValues = new HashMap(1);
					objectValues.put("actualStartDate", newStartDate);
					objectList.put(strObjectId, objectValues);
					updateTaskMap(context, strObjectId, "actualStartDate", newStartDate);
					/* gqh: removed in favor of date rollup call above.
                        Date fDate = null;
                        Date sDate = null;
                    task.setAttributeValue(context,
                                task.ATTRIBUTE_TASK_ACTUAL_START_DATE,
                                strNewStartDate);
                        if (strActualFinishDate != null
                                && !"".equals(strActualFinishDate)
                                && !"null".equals(strActualFinishDate)) {
                            fDate = eMatrixDateFormat.getJavaDate(
                                    strActualFinishDate, locale);
                            sDate = eMatrixDateFormat.getJavaDate(
                                    strNewStartDate, locale);

                            Integer duration = new Integer((int) DateUtil
                                    .computeDuration(sDate, fDate));
                            task.setAttributeValue(context,
                                    task.ATTRIBUTE_TASK_ACTUAL_DURATION,
                                    duration.toString());
                        }
					 */
				}
			} catch (Exception e) {
				throw e;
			}
		}
		}
	}// end of the method



	/**This method validates Newly entered Actulal Finish Date for any Task/Project
	 * @param context
	 * @param calToday
	 * @param today
	 * @param newFinishDate
	 * @param taskName
	 * @param strActualStartDate
	 * @param canUpdate
	 * @return
	 * @throws Exception
	 */
	public int triggerCheckActualFinishDate(Context context, String[] args) throws Exception
	{

		String strNewVal = args[2];
		if(null!=strNewVal && !"".equalsIgnoreCase(strNewVal) && !"null".equalsIgnoreCase(strNewVal))
		{
			String strOldVal = args[1];;
			String objId = args[0];
			final String SELECT_ACT_START_DATE = "attribute["
					+ PropertyUtil.getSchemaProperty(context,
							"attribute_TaskActualStartDate") + "]";

			TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
			double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
			double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
			int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
			Locale locale = context.getLocale();
			java.text.DateFormat format = DateFormat.getDateTimeInstance(
					iDateFormat, iDateFormat, locale);

			Calendar calToday = Calendar.getInstance();
			calToday.setTime(eMatrixDateFormat.getJavaDate(strNewVal, locale));
			strNewVal = format.format((calToday.getTime()));
			calToday = Calendar.getInstance();

			calToday.set(Calendar.HOUR_OF_DAY, 0);
			calToday.set(Calendar.MINUTE, 0);
			calToday.set(Calendar.SECOND, 0);
			calToday.set(Calendar.MILLISECOND, 0);
			Date today = calToday.getTime();

			// **Start**
			if (ProjectSpace.isEPMInstalled(context)) {
				blockValue = "";
			}
			// **End**
			if (strNewVal == null) {
				strNewVal = "";
			}
			if (strOldVal == null) {
				strOldVal = "";
			}

			if (!strNewVal.equals(strOldVal)) {
				com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject
						.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
				task.setId(objId);

				String sPolicyName = PropertyUtil.getSchemaProperty(context,
						"policy_ProjectTask");
				String sCompleteStateName = PropertyUtil.getSchemaProperty(context,
						"policy", sPolicyName, "state_Complete");

				String strFieldValueAttr = com.matrixone.apps.domain.util.eMatrixDateFormat
						.getFormattedInputDateTime(context, strNewVal, FINISH_DATE_SET_TIME,clientTZOffset,
								locale);
				String strTempfieldValueAttr = eMatrixDateFormat
						.getFormattedDisplayDateTime(context, strFieldValueAttr,
								true, iDateFormat, clientTZOffset, locale);
				Date newFinishDate = format.parse(strTempfieldValueAttr);

				Calendar calNewFinishDate = Calendar.getInstance();
				calNewFinishDate.setTime(newFinishDate);
				calNewFinishDate.set(Calendar.HOUR_OF_DAY, 0);
				calNewFinishDate.set(Calendar.MINUTE, 0);
				calNewFinishDate.set(Calendar.SECOND, 0);
				calNewFinishDate.set(Calendar.MILLISECOND, 0);
				newFinishDate = calNewFinishDate.getTime();

				StringList busSelect = new StringList(4);
				busSelect.add(DomainConstants.SELECT_NAME);
				busSelect.add(DomainConstants.SELECT_NAME);
				busSelect.add(SELECT_ACT_START_DATE);
				busSelect.add(SELECT_HAS_SUBTASK);

				Map taskData = (Map) task.getInfo(context, busSelect);
				String taskName = (String) taskData.get(DomainObject.SELECT_NAME);
				String taskType = (String) taskData.get(DomainObject.SELECT_TYPE);
				String strActualStartDate = (String) taskData.get(SELECT_ACT_START_DATE);
				if(null==strActualStartDate || "".equals(strActualStartDate) || "null".equalsIgnoreCase(strActualStartDate))
				{
					String strTempFieldValueAttr = com.matrixone.apps.domain.util.eMatrixDateFormat
							.getFormattedInputDateTime(context, strNewVal, START_DATE_SET_TIME,clientTZOffset,
									locale);
					task.setAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE, strTempFieldValueAttr);

					strActualStartDate = eMatrixDateFormat
							.getFormattedDisplayDateTime(context, strTempFieldValueAttr,
									true, iDateFormat, clientTZOffset, locale);;
				}
				String strHasSubtask = (String) taskData.get(SELECT_HAS_SUBTASK);

				if("true".equalsIgnoreCase(strHasSubtask)){
					StringList slSubtaskStateList = task.getInfoList(context, "from["+RELATIONSHIP_SUBTASK+"].to."+SELECT_CURRENT);
					for(int i=0;i<slSubtaskStateList.size();i++)
					{
						if(!STATE_PROJECT_SPACE_COMPLETE.equals(slSubtaskStateList.get(i)))
						{
							String sPromoteMsg = "emxProgramCentral.Alert.InvalidTaskActualFinishDate";
							String sKey[] = { "taskName","taskType" };
							String sValue[] = { taskName,taskType };
							sPromoteMsg = emxProgramCentralUtilClass.getMessage(context,
									sPromoteMsg, sKey, sValue, null);

							emxContextUtilBase_mxJPO.mqlNotice(context, sPromoteMsg);
							throw new MatrixException(sPromoteMsg);
							//return 1;
						}
					}
				}
				long actStartDateMS = Date.parse(strActualStartDate);
				Date actualStartDate = new Date(actStartDateMS);
				calToday.setTime(actualStartDate);
				calToday.set(Calendar.HOUR_OF_DAY, 0);
				calToday.set(Calendar.MINUTE, 0);
				calToday.set(Calendar.SECOND, 0);
				calToday.set(Calendar.MILLISECOND, 0);

				actualStartDate = calToday.getTime();
				if (newFinishDate.compareTo(actualStartDate) < 0) {
					String sPromoteMsg = "emxProgramCentral.Alert.InvalidActualTaskEndDate";
					String sKey[] = { "taskName","taskType" };
					String sValue[] = { taskName,taskType };
					sPromoteMsg = emxProgramCentralUtilClass.getMessage(
							context, sPromoteMsg, sKey, sValue, null);

					emxContextUtilBase_mxJPO.mqlNotice(context, sPromoteMsg);
					throw new MatrixException(sPromoteMsg);
					//return 1;
				}
			}
		}
		//End:Modified:26-July-2010:s4e:R210 PRG:ActualDates
		return 0;
	}

	/** This method validates Newly entered Actulal Start Date for any Task/Project
	 * @param context
	 * @param startDateUpdateFlag
	 * @param calToday
	 * @param newStartDate
	 * @param taskName
	 * @param strActualFinishDate
	 * @param canUpdate
	 * @return
	 * @throws Exception
	 */
	public int triggerCheckActualStartDate(Context context,String[] args)
			throws Exception
			{
		String strNewVal = args[2];
		if(null!=strNewVal && !"".equalsIgnoreCase(strNewVal) && !"null".equalsIgnoreCase(strNewVal))
		{
			String strOldVal = args[1];;
			String objId = args[0];
			String strStartDateUpdateFlag = "emxProgramCentral.TaskContraint.UpdateStartDate";
			strStartDateUpdateFlag = emxProgramCentralUtilClass.getMessage(context,
					strStartDateUpdateFlag, null, null, null);
			boolean startDateUpdateFlag = "true"
					.equalsIgnoreCase(strStartDateUpdateFlag) ? true : false;

			String SELECT_ACT_START_DATE = "attribute["
					+ PropertyUtil.getSchemaProperty(context,
							"attribute_TaskActualStartDate") + "]";
			String SELECT_ACT_FINISH_DATE = "attribute["
					+ PropertyUtil.getSchemaProperty(context,
							"attribute_TaskActualFinishDate") + "]";

			TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
			double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
			double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();

			int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
			Locale locale = emxProgramCentralUtil_mxJPO.getLocale(context);
			java.text.DateFormat format = DateFormat.getDateTimeInstance(
					iDateFormat, iDateFormat, locale);

			Calendar calToday = Calendar.getInstance();
			calToday.setTime(eMatrixDateFormat.getJavaDate(strNewVal, locale));
			strNewVal = format.format((calToday.getTime()));
			calToday = Calendar.getInstance();

			calToday.set(Calendar.HOUR_OF_DAY, 0);
			calToday.set(Calendar.MINUTE, 0);
			calToday.set(Calendar.SECOND, 0);
			calToday.set(Calendar.MILLISECOND, 0);
			Date today = calToday.getTime();

			// **Start**
			if (ProjectSpace.isEPMInstalled(context)) {
				blockValue = "";
			}
			// **End**
			if (strNewVal == null) {
				strNewVal = "";
			}
			if (strOldVal == null) {
				strOldVal = "";
			}
			if (!strNewVal.equals(strOldVal)) {
				com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject
						.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
				task.setId(objId);

				String sPolicyName = PropertyUtil.getSchemaProperty(context,
						"policy_ProjectTask");
				String sActiveStateName = PropertyUtil.getSchemaProperty(context,
						"policy", sPolicyName, "state_Active");
				String strFieldValueAttr = eMatrixDateFormat.getFormattedInputDate(
						context, strNewVal, clientTZOffset, locale);
				String strTempfieldValueAttr = eMatrixDateFormat
						.getFormattedDisplayDateTime(context, strFieldValueAttr,
								true, iDateFormat, clientTZOffset, locale);
				Date newStartDate = format.parse(strTempfieldValueAttr);

				Calendar calNewStartDate = Calendar.getInstance();
				calNewStartDate.setTime(newStartDate);
				calNewStartDate.set(Calendar.HOUR_OF_DAY, 0);
				calNewStartDate.set(Calendar.MINUTE, 0);
				calNewStartDate.set(Calendar.SECOND, 0);
				calNewStartDate.set(Calendar.MILLISECOND, 0);
				newStartDate = calNewStartDate.getTime();

				String SUBTASK_CURRENT="from["+SELECT_HAS_SUBTASK+"].to."+SELECT_CURRENT;

				StringList busSelect = new StringList(4);
				busSelect.add(DomainConstants.SELECT_NAME);
				busSelect.add(DomainObject.SELECT_TYPE);
				busSelect.add(SELECT_ACT_FINISH_DATE);
				busSelect.add(SELECT_HAS_SUBTASK);
				busSelect.add(SUBTASK_CURRENT);

				Map taskData = (Map) task.getInfo(context, busSelect);
				String taskName = (String) taskData.get(DomainObject.SELECT_NAME);
				String taskType = (String) taskData.get(DomainObject.SELECT_TYPE);
				String strActualFinishDate = (String) taskData.get(SELECT_ACT_FINISH_DATE);
				String strHasSubtask = (String) taskData.get(SELECT_HAS_SUBTASK);
				String strSubtaskState = (String) taskData.get(SUBTASK_CURRENT);

				if("true".equalsIgnoreCase(strHasSubtask))
				{
					if(STATE_PROJECT_TASK_CREATE.equalsIgnoreCase(strSubtaskState) ||
							STATE_PROJECT_TASK_ASSIGN.equalsIgnoreCase(strSubtaskState)){
						String sPromoteMsg = "emxProgramCentral.Alert.InvalidTaskActualStartDate";
						String sKey[] = { "taskName","taskType" };
						String sValue[] = { taskName,taskType };
						sPromoteMsg = emxProgramCentralUtilClass.getMessage(context,
								sPromoteMsg, sKey, sValue, null);

						emxContextUtilBase_mxJPO.mqlNotice(context, sPromoteMsg);
						throw new MatrixException(sPromoteMsg);
						//return 1;
					}
				}
				Date actualFinishDate = null;
				if(startDateUpdateFlag)
				{
					if (strActualFinishDate != null && !"".equals(strActualFinishDate)
							&& !"null".equals(strActualFinishDate)) {
						long actFinishDateMS = Date.parse(strActualFinishDate);
						actualFinishDate = new Date(actFinishDateMS);
					}
					if (null!=actualFinishDate){
						calToday.setTime(actualFinishDate);
						calToday.set(Calendar.HOUR_OF_DAY, 0);
						calToday.set(Calendar.MINUTE, 0);
						calToday.set(Calendar.SECOND, 0);
						calToday.set(Calendar.MILLISECOND, 0);

						actualFinishDate = calToday.getTime();

						if (!(newStartDate.compareTo(actualFinishDate) <= 0)) {
							String sPromoteMsg = "emxProgramCentral.Alert.InvalidActualTaskStartDate";
							String sKey[] = { "taskName","taskType" };
							String sValue[] = { taskName,taskType };
							sPromoteMsg = emxProgramCentralUtilClass.getMessage(context,
									sPromoteMsg, sKey, sValue, null);

							emxContextUtilBase_mxJPO.mqlNotice(context, sPromoteMsg);
							throw new MatrixException(sPromoteMsg);
							//return 1;
						}
					}
				}
			}
		}
		return 0;
			}

	//Added:14-June-2010:s4e:R210 PRG:WBSEnhancement
	/**
	 * Demotes all the Subtask to  "Create" state. when any Task is marked for deletion.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args, containg request Map.
	 * @throws FrameworkException if operation fails.
	 * @since PRG R210
	 * @author S4E
	 * @grade 0
	 */
	public void demoteSubtaskToCreate(Context context,String[] args) throws FrameworkException
	{
		try{

			String strParentTaskId = args[0].trim();
			DomainObject taskDo = DomainObject.newInstance(context, strParentTaskId);


			String strTypePattern = DomainConstants.TYPE_TASK;
			String strRelationshipPattern = DomainConstants.RELATIONSHIP_SUBTASK;
			StringList slBusSelect = new StringList();
			slBusSelect.add(DomainObject.SELECT_ID);
			slBusSelect.add(DomainObject.SELECT_CURRENT);
			slBusSelect.add(SELECT_HAS_SUBTASK);

			StringList slRelSelect = new StringList();
			StringList slLeafNodeTaskIdList = new StringList();

			boolean getFrom = true;
			boolean getTo = false;
			short recurseToLevel = 0;
			String strBusWhere = "("+SELECT_CURRENT+"!="+STATE_PROJECT_TASK_CREATE+")";
			String strRelWhere = "";
			Map mapRelatedObjectInfo = null;

			MapList mlRelatedObjects = taskDo.getRelatedObjects(context,
					strRelationshipPattern, //pattern to match relationships
					strTypePattern, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
					slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getTo, //get To relationships
					getFrom, //get From relationships
					recurseToLevel, //the number of levels to expand, 0 equals expand all.
					strBusWhere, //where clause to apply to objects, can be empty ""
					strRelWhere, //where clause to apply to relationship, can be empty ""
					0);//limit

			if(null!=mlRelatedObjects && !mlRelatedObjects.isEmpty())
			{
				for(Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();)
				{
					mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
					String strSubtaskId = (String)mapRelatedObjectInfo.get(SELECT_ID);
					String strHasSubtask = (String)mapRelatedObjectInfo.get(SELECT_HAS_SUBTASK);
					if(strHasSubtask.equalsIgnoreCase("False"))
					{
						slLeafNodeTaskIdList.add(strSubtaskId);
					}

				}
			}
			if(null!=slLeafNodeTaskIdList && !slLeafNodeTaskIdList.isEmpty())
			{
				for(int nCount=0;nCount<slLeafNodeTaskIdList.size();nCount++)
				{
					String strLeafNodeTaskId = (String)slLeafNodeTaskIdList.get(nCount);
					DomainObject subTaskDo = DomainObject.newInstance(context,strLeafNodeTaskId);
					subTaskDo.setState(context, STATE_PROJECT_TASK_CREATE);
				}
			}
			taskDo.setState(context, STATE_PROJECT_TASK_CREATE);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new FrameworkException(e);
		}
	}
	//End:14-June-2010:s4e:R210 PRG:WBSEnhancement

	//Added:06-Aug-2010:s4e:R210 PRG:EnforceMandatoryTasks
	/**
	 * checkTaskEnforcement - This method checks property emxProgramCentral.EnforceMandatoryTasks
	 * If the property is true the only TaskRequirement column will be readonly for WBS view
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *    objectList - Contains a MapList of Maps which contains object names
	 * @return StringList of boolean values according to property key value
	 * @throws MatrixException if the operation fails
	 * @since PMC V6R2011x
	 */
	public StringList checkTaskEnforcement(Context context, String[] args) throws Exception
	{

		boolean blIsEditable=true;
		StringList slIsEditable=new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Iterator objectListIterator = objectList.iterator();

		Map objectMap = (Map) objectListIterator.next();
		String taskId= (String)objectMap.get(SELECT_ID);
		Task task=  new Task();
		task.setId(taskId);
		boolean isMandatory= task.isMandatoryTask(context,taskId);
		if(isMandatory)
		{
			blIsEditable=false;
		}
		for(int nCount=0;nCount<objectList.size();nCount++)
		{
			slIsEditable.add(blIsEditable);
		}

		return slIsEditable;
	}

	//End:Added:06-Aug-2010:s4e:R210 PRG:EnforceMandatoryTasks

	//Added:06-Aug-2010:s4e:R210 PRG:EnforceMandatoryTasks
	/**
	 * getTaskRequirementRange - This method gets the range for attribute "Task Requirement" while editing an Task
	 * If the property emxProgramCentral.EnforceMandatoryTasks is true the only "Optional" will be displayed
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *    objectList - Contains a MapList of Maps which contains object names
	 * @return Map of "Task Requirement" values
	 * @throws MatrixException if the operation fails
	 * @since PMC V6R2011x
	 */
	public Map getTaskRequirementRange(Context context, String[] args)
			throws Exception
			{
		try {
			String strCurrentTaskRequirement = "";
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			String objectId = (String) paramMap.get("objectId");
			task.setId(objectId);

			boolean isMandatory= task.isMandatoryTask(context,objectId);

			strCurrentTaskRequirement= task.getAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_REQUIREMENT);


			String sLanguage = context.getSession().getLanguage();
			AttributeType atrTaskRequirement = new AttributeType(DomainConstants.ATTRIBUTE_TASK_REQUIREMENT);
			atrTaskRequirement.open(context);
			StringList strList = atrTaskRequirement.getChoices(context);
			atrTaskRequirement.close(context);

			StringList slTaskRequirementValues = new StringList();
			StringList slTaskRequirementTranslated = new StringList();
			Map map = new HashMap();
			String i18nSelectedRole = null;
			String strTaskRequirement="";

			if(isMandatory)
			{
				if(null!=strCurrentTaskRequirement && !"null".equalsIgnoreCase(strCurrentTaskRequirement)&&!"".equals(strCurrentTaskRequirement))
				{
					slTaskRequirementValues.add(strCurrentTaskRequirement);
					slTaskRequirementTranslated.add(i18nNow.getRangeI18NString(DomainConstants.ATTRIBUTE_TASK_REQUIREMENT, strCurrentTaskRequirement, sLanguage));
				}
			}
			else{
				for(int i=0; i<strList.size();i++){
					strTaskRequirement= (String)strList.get(i);

					if(strTaskRequirement != null )
					{
						slTaskRequirementValues.add(strTaskRequirement);
						//slTaskRequirementTranslated.add(i18nNow.getRangeI18NString(DomainConstants.ATTRIBUTE_TASK_REQUIREMENT, strTaskRequirement, sLanguage));
					}
				}
			}
			slTaskRequirementTranslated= i18nNow.getAttrRangeI18NStringList(DomainConstants.ATTRIBUTE_TASK_REQUIREMENT, slTaskRequirementValues, sLanguage);
			map.put("field_choices", slTaskRequirementValues);
			map.put("field_display_choices", slTaskRequirementTranslated);

			return  map;
		} catch (MatrixException e) {
			e.printStackTrace();
			throw e;
		}
			}

	//End:Added:06-Aug-2010:s4e:R210 PRG:EnforceMandatoryTasks


	//Added:03-Sept-2010:s4e:R210 PRG:WBS Enhancement
	/**
	 * This function calls the method rollupAndSave() to rollup related task dates
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws Exception if operation fails
	 * @since PRG R210
	 */
	public void triggerRollupTaskDates(Context context, String[] args) throws Exception
	{
		// get values from args.
		String objectId = args[0];
		if(null!=objectId && !"null".equalsIgnoreCase(objectId)&&!"".equalsIgnoreCase(objectId))
		{
			DomainObject dobj = DomainObject.newInstance(context, objectId);
			if(dobj.isKindOf(context,TYPE_PROJECT_MANAGEMENT))
			{
				com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject
						.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
				task.setId(objectId);
				task.rollupAndSave(context);
			}
		}

	}

	//End:Added:03-Sept-2010:s4e:R210 PRG:WBS Enhancement

	/* Returns range values for state column in WBS of Projects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws Exception if operation fails
	 * @since PRG R211
	 */
	public Map getTaskManagementStateRange(Context context, String[] args)throws Exception
	{
		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map paramMap = (HashMap) programMap.get("paramMap");
		String strObjectId = (String) paramMap.get("objectId");
		String strLan = context.getSession().getLanguage();
		// [MODIFIED::PRG:RG6:Jan 20, 2011:IR-091708V6R2012 :R211::Start]
		if(ProgramCentralUtil.isNullString(strObjectId))
		{
			throw new IllegalArgumentException("Object id is null");
		}

		StringList slObjectSelects = new StringList();
		slObjectSelects.add(SELECT_POLICY);

		DomainObject domObj  = DomainObject.newInstance(context,strObjectId);
		Map mapObjectInfo    = domObj.getInfo(context,slObjectSelects);

		String sObjectPolicy = (String) mapObjectInfo.get(SELECT_POLICY);

		if(ProgramCentralUtil.isNullString(sObjectPolicy))
		{
			throw new IllegalArgumentException("Policy Value is null");
		}

		Map mapStates = new HashMap();
		StringList slStates = new StringList();
		slStates =ProjectSpace.getStates(context, sObjectPolicy);

		StringList sli18States = new StringList();

		if(null != slStates)
		{
			int size = slStates.size();
			for(int i=0; i< size; i++)
			{
				String sObjState  = (String)slStates.get(i);
				String si18nState = "";
				if(ProgramCentralUtil.isNotNullString(sObjState))
				{
					si18nState = i18nNow.getStateI18NString(sObjectPolicy, sObjState, strLan);

					if(ProgramCentralUtil.isNullString(si18nState))
					{
						si18nState = sObjState;
					}
				}

				sli18States.add(si18nState);
			}
		}

		mapStates.put("field_choices", slStates);//this.getStates(context, args));
		mapStates.put("field_display_choices",sli18States);// i18nNow.getStateI18NStringList(new StringList(DomainConstants.POLICY_PROJECT_TASK), this.getStates(context, args), context.getSession().getLanguage()));

		// [MODIFIED::PRG:RG6:Jan 20, 2011:IR-091708V6R2012 :R211::END]
		return mapStates;
	}

	//End:Added:03-Sept-2010:s4e:R210 PRG:WBS Enhancement
	/* Returns reload range values of states for particular item in WBS of Projects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws Exception if operation fails
	 * @since PRG R211
	 */
	public static Map getTaskManagementStates(Context context, String[] args)throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap hmRowValues = (HashMap) programMap.get("rowValues");
		String strObjectId = (String) hmRowValues.get("objectId");
		String strLan = context.getSession().getLanguage();
		// [MODIFIED::PRG:RG6:Jan 20, 2011:IR-091708V6R2012 :R211::Start]
		if(ProgramCentralUtil.isNullString(strObjectId))
		{
			throw new IllegalArgumentException("Object id is null");
		}

		StringList slObjectSelects = new StringList();
		slObjectSelects.add(SELECT_POLICY);

		DomainObject domObj = DomainObject.newInstance(context,strObjectId);
		Map mapObjectInfo    = domObj.getInfo(context,slObjectSelects);

		String sObjectPolicy = (String) mapObjectInfo.get(SELECT_POLICY);

		if(ProgramCentralUtil.isNullString(sObjectPolicy))
		{
			throw new IllegalArgumentException("Policy Value is null");
		}

		StringList slStates = new StringList();
		slStates =ProjectSpace.getStates(context, sObjectPolicy);

		StringList sli18States = new StringList();

		if(null != slStates)
		{
			int size = slStates.size();
			for(int i=0; i< size; i++)
			{
				String sObjState  = (String)slStates.get(i);
				String si18nState = "";
				if(ProgramCentralUtil.isNotNullString(sObjState))
				{
					si18nState = i18nNow.getStateI18NString(sObjectPolicy, sObjState, strLan);

					if(ProgramCentralUtil.isNullString(si18nState))
					{
						si18nState = sObjState;
					}
				}

				sli18States.add(si18nState);
			}
		}

		// [MODIFIED::PRG:RG6:Jan 20, 2011:IR-091708V6R2012 :R211::END]

		Map returnMap = new HashMap();
		returnMap.put("RangeValues", slStates);
		returnMap.put("RangeDisplayValue", sli18States);

		return returnMap;
	}

	// [Added::May 26, 2011:S4E:R212:IR-104931V6R2012x::Start]
	/**This function modifies the attribute RemainingEffort
	 * If Duration Field is modified then this method will check and reset Remaining Effort value for all the Effort Object connected to Task.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 * @since PMC V6R2012x
	 *
	 */
	public int triggerUpdateRemainingEffort(Context context, String[] args) throws MatrixException {

		long startTime = System.currentTimeMillis();
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String attribute_TotalEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_TotalEffort");
		String attribute_Duration=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedDuration");
		double totalEffort = 0.0;
		double total_pending = 0.0;
		StringList slEffortIds = new StringList();
		StringList selectList  = new StringList();
		selectList.add(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);
		selectList.add("from["+RELATIONSHIP_HAS_EFFORTS+"].to.id");
		selectList.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
		selectList.add("attribute["+attribute_Duration+"].unitvalue[h]");

		try {
			String objectId = args[0];
			setId(objectId);

			Map taskInfoMap = getInfo(context,selectList);
			String isKindofTaskManagement = (String)taskInfoMap.get(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);
			Object slEffortIdValue = taskInfoMap.get("from["+RELATIONSHIP_HAS_EFFORTS+"].to.id");

			if(slEffortIdValue instanceof String) {
				slEffortIds.add((String)slEffortIdValue);
			} else if(slEffortIdValue instanceof StringList) {
				slEffortIds = (StringList)slEffortIdValue;
			}
			if("TRUE".equalsIgnoreCase(isKindofTaskManagement) && !slEffortIds.isEmpty()) {

				emxEffortManagementBase_mxJPO emxEffortJPO = new emxEffortManagementBase_mxJPO(context,new String[0]);
				double final_remaining = emxEffortJPO.getTaskPlannedEffort(context,this,taskInfoMap);

				MapList effortMapList = emxEffortJPO.getEffortInfoMapList(context,this,relPattern,type_Effort,false,true,(short)1);
				String totalEffortString = emxEffortJPO.getGivenStateEffort(context, ProgramCentralConstants.STATE_EFFORT_APPROVED, true,effortMapList);
				String totalpendingString = emxEffortJPO.getGivenStateEffort(context,ProgramCentralConstants.STATE_EFFORT_SUBMIT,true,effortMapList);

				if(ProgramCentralUtil.isNullString(totalEffortString)) {
					totalEffortString = "0.0";
				}
				totalEffort = Task.parseToDouble(totalEffortString);

				if(ProgramCentralUtil.isNullString(totalpendingString)) {
					totalpendingString = "0.0";
				}
				total_pending = Task.parseToDouble(totalpendingString);

				final_remaining=final_remaining-(total_pending+totalEffort);
				if(final_remaining < 0 )
					final_remaining=0;
				String strRemainingEffort = Double.toString(final_remaining);

				double dbl_remaining=0;//Added:di7:16-Sept-2011:HF-104931V6R2011x_:R210
				String attribute_RemainingEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_RemainingEffort");
				String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
				for(int nCount=0;nCount<slEffortIds.size();nCount++)
				{
					String strEffortId= (String)slEffortIds.get(nCount);
					DomainObject effortDo = DomainObject.newInstance(context,strEffortId);
					double plannedEffort = emxEffortJPO.getPlannedEffort(context,strEffortId);
					String sState = FrameworkUtil.getCurrentState(context,effortDo).getName();

					String str_Orig = effortDo.getAttributeValue(context,attribute_Originator);
					double totalEffortsByPerson = emxEffortJPO.getTotalPersonEffort(context,strEffortId, str_Orig);

					dbl_remaining = (plannedEffort - totalEffortsByPerson);
					if(dbl_remaining < 0)
						dbl_remaining = 0;

					strRemainingEffort = Double.toString(dbl_remaining);
					//END:di7:16-Sept-2011:HF-104931V6R2011x_:R210
					effortDo.setAttributeValue(context, attribute_RemainingEffort, strRemainingEffort);
				}
			}
			return 0;
		}
		catch (Exception e)
		{
			throw new MatrixException(e);
		}
	}    // [Added::May 26, 2011:S4E:R212:IR-104931V6R2012x::End]


	/**
	 * This Method will return boolean true if object is of type project space,concept,template and false otherwise.
	 * @param context Matrix Context object
	 * @param objectId String
	 * @return boolean
	 * @throws MatrixException
	 * @author RG6
	 */
	private boolean checkIfProject(Context context,String sObjectId) throws MatrixException
	{
		if(ProgramCentralUtil.isNullString(sObjectId))
		{
			throw new IllegalArgumentException("Object id is null");
		}

		boolean isProject = false;
		try
		{
			StringList slSelectInfo = new StringList();
			slSelectInfo.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			slSelectInfo.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			slSelectInfo.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
			DomainObject dObj = DomainObject.newInstance(context);
			dObj.setId(sObjectId);
			Map mProjectInfo = dObj.getInfo(context, slSelectInfo);
			if(null != mProjectInfo)
			{
				String sIsProjectConcept = (String)mProjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
				String sIsProjectSpace = (String)mProjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				String sIsProjectTemplate = (String)mProjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

				isProject = "true".equalsIgnoreCase(sIsProjectConcept) || "true".equalsIgnoreCase(sIsProjectSpace) || "true".equalsIgnoreCase(sIsProjectTemplate);
			}
		}
		catch(Exception e)
		{
			throw new MatrixException(e);
		}
		return isProject;
	}

	//Added:MS9:27-07-2011:PRG:R212:IR-118680V6R2012x
	/**
	 * Deny Edit acess to object of type Project at the root level
	 * as it is not allowed to put dependency on project at root level
	 * @param context
	 * @param args The arguments, it contains requestMap
	 * @return StringList
	 * @throws Exception if the operation fails
	 * @since 2012x
	 * @author MS9
	 */
	public StringList isDependencyCellsEditable (Context context, String[] args) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) mapProgram.get("objectList");
		StringList slHasAccess = new StringList();
		
		Map mpData = (Map)objectList.get(0);
		String strId = (String)mpData.get(DomainConstants.SELECT_ID);
		int intLevel = Integer.parseInt((String)mpData.get(DomainConstants.SELECT_LEVEL));
		
		StringList busSelect = new StringList(3);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

		MapList projectInfoList 	= ProgramCentralUtil.getObjectDetails(context, new String[]{strId}, busSelect);
		Map<String,String> objectInfo = (Map)projectInfoList.get(0);
		String isProjectSpace 		= objectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		String isProjectConcept 	= objectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
		String isProjectTemplate 	= objectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

		if(intLevel==0 && "true".equalsIgnoreCase(isProjectSpace) || 
				"true".equalsIgnoreCase(isProjectTemplate) || "true".equalsIgnoreCase(isProjectConcept)) {
			slHasAccess.add("false");
		} else {
			slHasAccess.add("true");
		}

		for(int i=1, size = objectList.size(); i< size;i++){
			slHasAccess.add("true");
		}

		return slHasAccess;
	}


	/**
	 * Updates the name of the task in WBS grid
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args Request arguments
	 * @throws MatrixException if operations fails.
	 */
	public void updateWBSElementsName(Context context, String[] args) throws MatrixException {
		try {
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK, "PROGRAM");
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) inputMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			task.setId(objectId);
			String taskState = (String) task.getInfo(context, SELECT_CURRENT);
			if(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equals(taskState)||
					ProgramCentralConstants.STATE_PROJECT_SPACE_ARCHIVE.equals(taskState)){
				String editingNotAllowedMessage = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Common.TaskNameEditingNotAllowed", context.getSession().getLanguage());
				emxContextUtilBase_mxJPO.mqlNotice(context,editingNotAllowedMessage);
				return;
			}
			String newAttrValue = (String) paramMap.get("New Value");
			task.setName(context, newAttrValue);
		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}

	/**
	 * Updates Project Task Types.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args The arguments, it contains paramMap
	 * @throws MatrixException
	 */

	public void updateWBSElementsType(Context context, String[] args) throws MatrixException {

		try {
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			HashMap columnMap = (HashMap) inputMap.get("columnMap");
			HashMap paramMap = (HashMap) inputMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			String strNewTaskType = (String) paramMap.get("New Value");

			if(!strNewTaskType.equalsIgnoreCase(ProgramCentralConstants.TYPE_EXPERIMENT)){
				Task task = new Task();
				task.updateTaskType(context, objectId, strNewTaskType);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * In the Structure Browser, For getting the Task SubTypes Ranges
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args
	 * @return HashMap of Task Type Values
	 * @throws MatrixException
	 */

	public HashMap getTaskTypes(Context context, String[] args) throws MatrixException {

    	String policy = ProgramCentralConstants.EMPTY_STRING;
    	 try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			policy = (String) requestMap.get("PolicyName");
		} catch (Exception e) {
			e.printStackTrace();
		}

		HashMap mapTaskTypeNames = new HashMap();
		StringList slTaskSubTypes = ProgramCentralUtil.getTaskSubTypesList(context);

		if(FrameworkUtil.isSuiteRegistered(context, "appVersionAerospaceProgramManagementAccelerator", false, null, null)) {
			slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_MilestoneOpportunity"));
			slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_MilestonePayment"));
			slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_MilestoneRisk"));
			slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_MilestoneFee"));
			slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_ValidationTask"));
		}

		StringList slTaskSubTypesIntNames = new StringList();
        if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(policy))
        {
        	StringList reviewTypeList = new StringList(2);
        	reviewTypeList.addAll(ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_GATE));
        	reviewTypeList.addAll(ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_MILESTONE));
        	slTaskSubTypes.retainAll(reviewTypeList);
        }

        try {
			ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PRG_LICENSE_ARRAY);
		} catch (MatrixException e) {//To restrict DPJ users from creating Gate.
			if(slTaskSubTypes.contains(ProgramCentralConstants.TYPE_GATE)){
				slTaskSubTypes.remove(ProgramCentralConstants.TYPE_GATE);
			}
		}

		int count = 0;
        String language = context.getSession().getLanguage();
		for (Iterator iterator = slTaskSubTypes.iterator(); iterator.hasNext();) {
			String str = (String) iterator.next();
            String i18nTaskTypeName = i18nNow.getTypeI18NString(slTaskSubTypes.get(count).toString(),language);
			slTaskSubTypesIntNames.add(i18nTaskTypeName);
			count++;
		}

		mapTaskTypeNames.put("field_choices", slTaskSubTypes);
		mapTaskTypeNames.put("field_display_choices", slTaskSubTypesIntNames);
		return mapTaskTypeNames;
	}

	/**
	 * Number of SubTasks to Add.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args
	 * @return HashMap
	 * @throws MatrixException
	 */

	public HashMap getRangeForTasksToAdd(Context context, String[] args) throws MatrixException
	{
		HashMap mapTaskTypeNames = new HashMap();
		String strTasksToAdd = "1|2|3|4|5|6|7|8|9|10";
		StringList slTasksToCreate = FrameworkUtil.split(strTasksToAdd, "|");
		mapTaskTypeNames.put("field_choices", slTasksToCreate);
		mapTaskTypeNames.put("field_display_choices", slTasksToCreate);
		return mapTaskTypeNames;
	}

	/**
	 * updateDescription - Where : In the Structure Browser, For updating Project and Task Description.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args
	 * @throws MatrixException
	 */

	public void updateDescription(Context context, String[] args) throws MatrixException {
		try {
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
					DomainConstants.TYPE_TASK, "PROGRAM");
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			HashMap columnMap = (HashMap) inputMap.get("columnMap");
			HashMap paramMap = (HashMap) inputMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			String newAttrValue = (String) paramMap.get("New Value");

			task.setId(objectId);
			task.setDescription(context, newAttrValue);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * Generate Members and NonMembers Columns for Task Assignment Matrix View
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args The arguments, it contains requestMap
	 * @return The MapList object containing definitions about new columns for showing Benefit Intervals
	 * @throws MatrixException
	 */

	public MapList getMembersConnectedToProject (Context context, String[] args) throws MatrixException
	{
		try {
			final String strDateFormat=eMatrixDateFormat.getEMatrixDateFormat();
			Map mapProgram = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map)mapProgram.get("requestMap");
			//
			// Following code gets business objects information
			//

			Map mapColumn = new HashMap();
			Map mapSettings = new HashMap();
			MapList mlColumns = new MapList();

			String strObjectId = (String) requestMap.get("objectId");
			String tableName=(String)requestMap.get("selectedTable");
			com.matrixone.apps.common.Person person =
					(com.matrixone.apps.common.Person) DomainObject.newInstance(context,
							DomainConstants.TYPE_PERSON);

			HashMap paramMap = new HashMap();
			paramMap.put("objectId", strObjectId);
			String[] argss = JPO.packArgs(paramMap);

			String strI18nMember = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskAssignment.Member", context.getSession().getLanguage());
			String strI18nNonMember = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskAssignment.NonMember", context.getSession().getLanguage());

			emxProjectMember_mxJPO jpo = new emxProjectMember_mxJPO(context, argss);
			MapList ml = jpo.getMembers(context, argss);
			//Find External Project Members.
			ProjectSpace project = new ProjectSpace();
			//StringList slPeopleAssignedToTasks = project.getProjectTaskAssignees(context, strObjectId);
			MapList mlPeopleAssignedToTasks = project.getProjectTaskAssignees(context, strObjectId);
			StringList slPeopleAssignedToTasks = new StringList(mlPeopleAssignedToTasks.size());
			for(int i = 0; i < mlPeopleAssignedToTasks.size(); i++) {
				slPeopleAssignedToTasks.add((String)((Map)mlPeopleAssignedToTasks.get(i)).get(SELECT_ID));
			}

			for (Iterator iterator = ml.iterator(); iterator.hasNext();) {
				Map objectMap = (Map) iterator.next();

				String objType = (String) objectMap.get(person.SELECT_TYPE);
				String objName = (String) objectMap.get(person.SELECT_NAME);
				String objId = (String) objectMap.get(person.SELECT_ID);
				if(objId.contains("personid_")){
					objId = objId.replace("personid_","");
				}
				slPeopleAssignedToTasks.remove(objId);
				String userType = (String) objectMap.get(person.SELECT_TYPE);
				String lastName = (String) objectMap.get(person.SELECT_LAST_NAME);
				String firstName = (String) objectMap.get(person.SELECT_FIRST_NAME);

				mapColumn = new HashMap();
				mapColumn.put("name", objId);
				mapColumn.put("label", lastName+" "+firstName);
				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxTask");
				mapSettings.put("function","getColumnMemberAssigneeData");
				mapSettings.put("Column Type","program");
				mapSettings.put("Editable","true");
				mapSettings.put("Export","true");
				mapSettings.put("Field Type","Basic");
				mapSettings.put("Sortable","false");
				mapSettings.put("Update Program","emxTask");
				mapSettings.put("Update Function","updateAssigneePercentage");
				mapSettings.put("Validate","doValForPerAllocTaskAssignmentMatrix");
				mapSettings.put("Edit Access Program","emxTask");
				mapSettings.put("Edit Access Function","isAssigneeColumnCellsEditable");
				mapSettings.put("Group Header",strI18nMember);
				mapSettings.put("Width","50");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);

			}

			StringList slBusSelect = new StringList();
			slBusSelect.add(person.SELECT_ID);
			slBusSelect.add(person.SELECT_LAST_NAME);
			slBusSelect.add(person.SELECT_FIRST_NAME);

			String[] strExternalProjectMembers = new String[slPeopleAssignedToTasks.size()];

			Object[] NonProjMemberIds = slPeopleAssignedToTasks.toArray();
			for (int j = 0; j < slPeopleAssignedToTasks.size(); j++)
				strExternalProjectMembers[j] = (String)NonProjMemberIds[j];

			MapList mlTaskAssigneeInfo = DomainObject.getInfo(context, strExternalProjectMembers, slBusSelect);

			for (Iterator iterator2 = mlTaskAssigneeInfo.iterator(); iterator2.hasNext();) {
				Map mpPersonDetails = (Map) iterator2.next();

				String objId = (String) mpPersonDetails.get(person.SELECT_ID);
				String lastName = (String) mpPersonDetails.get(person.SELECT_LAST_NAME);
				String firstName = (String) mpPersonDetails.get(person.SELECT_FIRST_NAME);

				mapColumn = new HashMap();
				mapColumn.put("name", objId);
				mapColumn.put("label", lastName+" "+firstName);
				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxTask");
				mapSettings.put("function","getColumnMemberAssigneeData");
				mapSettings.put("Column Type","program");
				mapSettings.put("Editable","true");
				mapSettings.put("Export","true");
				mapSettings.put("Field Type","Basic");
				mapSettings.put("Sortable","false");
				mapSettings.put("Update Program","emxTask");
				mapSettings.put("Update Function","updateAssigneePercentage");
				mapSettings.put("Validate","doValForPerAllocTaskAssignmentMatrix");
				mapSettings.put("Edit Access Program","emxTask");
				mapSettings.put("Edit Access Function","isAssigneeColumnCellsEditable");
				mapSettings.put("Group Header",strI18nNonMember);
				mapSettings.put("Width","50");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);

			}

			return mlColumns;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * Get 2 digit decimal value
	 * @param dblValue
	 * @return double
	 */

	private double getTwoDigitDecimalValue(double dblValue){

		if("NaN".equalsIgnoreCase(""+dblValue)){
			dblValue = 0;
		}

		DecimalFormat twoDForm = new DecimalFormat("#.##");
		double dblPersonsTotalEfforts = Task.parseToDouble(twoDForm.format(dblValue));
		return dblPersonsTotalEfforts;
	}

	/**
	 * Gets the % Allocation Data for each Task Assignee in a Project.
	 *
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing Assignee % values for Tasks for a person.
	 * @throws MatrixException
	 */

	public Vector getColumnMemberAssigneeData(Context context, String[] args) throws MatrixException
	{
		Vector vecResult = new Vector();
		try {
			// Create result vector
			// Get object list information from packed arguments
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map columnMap = (Map) programMap.get("columnMap");
			Map paramList = (Map) programMap.get("paramList");
			String strProjectID = (String) paramList.get("parentOID");
			//HashMap hmPersonPer = getPercentEstimatedEfforsProjectMember(context,strProjectID);
			String strPersonId = (String) columnMap.get(SELECT_NAME);
			DomainObject dmoPerson = DomainObject.newInstance(context,strPersonId);
			String ATTRIBUTE_PERCENT_ALLOCATION = (String)PropertyUtil.getSchemaProperty(context,"attribute_PercentAllocation");
			String SELECT_ATTRIBUTE_PERCENT_ALLOCATION = "attribute["+ATTRIBUTE_PERCENT_ALLOCATION+"]";
			StringList slBusSelect = new StringList(1);
			slBusSelect.add(SELECT_ID);
			StringList slRelSelect = new StringList(1);
			slRelSelect.add(SELECT_RELATIONSHIP_ID);
			slRelSelect.add(SELECT_ATTRIBUTE_PERCENT_ALLOCATION);

			String strTypePattern = TYPE_TASK_MANAGEMENT;
			String strRelPattern = RELATIONSHIP_ASSIGNED_TASKS;

			MapList assignedTasks = dmoPerson.getRelatedObjects(context,
					strRelPattern,
					strTypePattern,
					slBusSelect,
					slRelSelect, // relationshipSelects
					false,      // getTo
					true,       // getFrom
					(short) 1,  // recurseToLevel
					null,// objectWhere
					null);      // relationshipWhere

			HashMap hm = new HashMap();
			for (Iterator iterator = assignedTasks.iterator(); iterator
					.hasNext();) {
				Map map = (Map) iterator.next();
				String strTaskId = (String)map.get(SELECT_ID);
				String strPercenComplete = (String)map.get(SELECT_ATTRIBUTE_PERCENT_ALLOCATION);
				hm.put(strTaskId, strPercenComplete);
			}
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				Map map = (Map)itrTableRows.next();
				String strTaskId = (String)map.get(SELECT_ID);
				String strNodeLevel = (String)map.get("id[level]");
				String strPerComp = (String)hm.get(strTaskId);
				if("0".equals(strNodeLevel)){
					vecResult.add("");
				}else{
					if(ProgramCentralUtil.isNotNullString(strPerComp)){
						vecResult.add(strPerComp);
					}else{
						vecResult.add("0");
					}
				}
			}
			return vecResult;

		} catch (Exception exp) {
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}

	/**
	 * Updates % allocation of Assignees from Task Assignment Matrix Grid.
	 * If Task is not Assigneed and % allocation is done then the TAsk is first assigned and then % allcotion is set.
	 * Person can be disconnected from Task if % Allocation is set to 0.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param it contains columnMap and paramMap maps
	 * @throws MatrixException
	 */

	public void updateAssigneePercentage(Context context, String[] args) throws MatrixException{
		try {
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
					DomainConstants.TYPE_TASK, "PROGRAM");
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			HashMap columnMap = (HashMap) inputMap.get("columnMap");
			HashMap paramMap = (HashMap) inputMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			String selectedPerson = (String) columnMap.get(SELECT_NAME);
			String newPercentageAllocationValue = (String) paramMap.get("New Value");

			task.updateTaskAssigneePercentage(context, objectId, selectedPerson, newPercentageAllocationValue);

		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * isAssigneeColumnCellsEditable: Where : In Structure Browser the root node is not editable.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return StringList The StringList object containing Access Information
	 * @throws MatrixException
	 */

	public StringList isAssigneeColumnCellsEditable(Context context, String[] args) throws MatrixException
	{
		try {
			StringList slAccessList = new StringList();
			
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			int size = objectList.size();
			String[] objIds = new String[size];
			
			for(int i=0;i<size;i++){
				Map objectMap = (Map)objectList.get(i);
				String id = (String)objectMap.get(SELECT_ID);
				objIds[i] = id;
			}

			MapList taskInfoList = ProgramCentralUtil.getObjectDetails(context, objIds, new StringList(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT));
			for(int i=0;i<size;i++){
				Map objectMap = (Map)taskInfoList.get(i);
				String isTaskManagement = (String)objectMap.get(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);

				if("true".equalsIgnoreCase(isTaskManagement)){
					slAccessList.add("true");
				}else{
					slAccessList.add("false");
				}
			}

			return slAccessList;

		} catch (Exception exp) {
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}

	/**
	 * Shows the status icon with Task Delivarables as tooltip.
	 * Clicking on the icon will land the user on the Task Delivarables page.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments paramList, objectList.
	 * @returns Vector containing icon for Task Delivarable.
	 * @throws MatrixException
	 */

	public Vector getTaskDelivarablesIcon(Context context, String[] args) throws MatrixException
	{
		Vector vecIconList = new Vector();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramList = (HashMap) programMap.get("paramList");
			String exportFormat = (String)paramList.get("exportFormat");
			MapList objectList = (MapList) programMap.get("objectList");
			Map objectMap = null;
			int i = 0;
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];

			//Get a list of Project Space Types to hide the icon at root level
			StringList slTaskSubTypes = ProgramCentralUtil.getTaskSubTypesList(context);

			//Remove Milestone and its sub-types from the list as milestone are not allowed to have Delivarables.
			slTaskSubTypes.remove(ProgramCentralConstants.TYPE_MILESTONE);
			//Added:P6E:29-May:PRG:R214:IR-154335V6R2013x::Start
			StringList mileStoneSubtypeList =   ProgramCentralUtil.getSubTypesList(context,ProgramCentralConstants.TYPE_MILESTONE);
			slTaskSubTypes.removeAll(mileStoneSubtypeList);
			//Added:P6E:29-May:PRG:R214:IR-154335V6R2013x::End
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
				i++;
			}

			String strLanguage = context.getSession().getLanguage();
			String SELECT_DELEVERABLE_POLICY = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.policy";
			String SELECT_DELEVERABLE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.id";
			String SELECT_DELEVERABLE_NAME = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.name";
			String SELECT_DELEVERABLE_STATE = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.current";
			String SELECT_DELEVERABLE_MD = "from["+ProgramCentralConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.modified";


			StringList busSelect = new StringList(7);
			busSelect.add(DomainConstants.SELECT_ID);
			busSelect.add(SELECT_DELEVERABLE_POLICY);
			busSelect.add(DomainConstants.SELECT_TYPE);
			busSelect.add(DomainConstants.SELECT_POLICY);
			busSelect.add(SELECT_DELEVERABLE);
			busSelect.add(SELECT_DELEVERABLE_NAME);
			busSelect.add(SELECT_DELEVERABLE_STATE);
			busSelect.add(SELECT_DELEVERABLE_MD);

			MapList mlTaskDetails = DomainObject.getInfo(context, objIdArr, busSelect);

			int actionListSize = 0;
			if (mlTaskDetails != null)
			{
				actionListSize = mlTaskDetails.size();
			}

			String strI18nAddDeliverable = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.TaskAssignment.AddDeliverable", strLanguage);

			//Strings for Delivarables and Tooltip
			String strIcon = "";
			String strToolTip = "";

			//Variables for Delivarables and Tooltip
			String strDelvarables = "";
			String strTaskId = "";
			String strTaskType = "";
			String strHref = "";
			String strDelNames = "";
			String strDelState = "";
			String strModifiedDate = "";
			String iconDeliverable = "";
			String strPolicy = "";
			String strIntState = "";
			String taskPolicy = "";
			StringList slDelNames = new StringList();
			StringList slDelState = new StringList();
			StringList slDelModDate = new StringList();
			StringList slPolicy = new StringList();

			//Variables for building the href
			StringBuffer sbHrefMaker = new StringBuffer();
			StringBuffer sbLinkMaker = new StringBuffer();

			Date dtModifiedDate;
			String strInternationalDate;
			SimpleDateFormat formatter;
			formatter = new SimpleDateFormat("d MMM yy",ProgramCentralUtil.getLocale(context));
			for (i = 0; i < actionListSize; i++)
			{
				Map mpObjDetails = (Map) mlTaskDetails.get(i);
				strDelvarables = (String) mpObjDetails.get(SELECT_DELEVERABLE);
				strTaskId = (String) mpObjDetails.get(SELECT_ID);
				strTaskType = (String)mpObjDetails.get(SELECT_TYPE);
				taskPolicy = (String) mpObjDetails.get(SELECT_POLICY);
				strToolTip = "";
				if(slTaskSubTypes.contains(strTaskType) || mileStoneSubtypeList.contains(strTaskType)&& ProgramCentralConstants.POLICY_PROJECT_TASK.equals(taskPolicy)){

					if(ProgramCentralUtil.isNotNullString(strDelvarables)){
						strDelNames = (String) mpObjDetails.get(SELECT_DELEVERABLE_NAME);
						slDelNames = FrameworkUtil.splitString(strDelNames, matrix.db.SelectConstants.cSelectDelimiter);
						strDelState = (String) mpObjDetails.get(SELECT_DELEVERABLE_STATE);
						slDelState = FrameworkUtil.splitString(strDelState, matrix.db.SelectConstants.cSelectDelimiter);
						strModifiedDate = (String) mpObjDetails.get(SELECT_DELEVERABLE_MD);
						slDelModDate = FrameworkUtil.splitString(strModifiedDate, matrix.db.SelectConstants.cSelectDelimiter);
						strPolicy = (String) mpObjDetails.get(SELECT_DELEVERABLE_POLICY);
						slPolicy = FrameworkUtil.splitString(strPolicy, matrix.db.SelectConstants.cSelectDelimiter);

						for(int j=0;j<slDelModDate.size();j++){
							dtModifiedDate = new Date(slDelModDate.get(j).toString());
							strInternationalDate = formatter.format(dtModifiedDate);
							strIntState = i18nNow.getStateI18NString((String)slPolicy.get(j),(String)slDelState.get(j), strLanguage);
							if(j==0)
								strToolTip += XSSUtil.encodeForHTML(context, (String)slDelNames.get(j))+" "+strIntState+" "+strInternationalDate;
							else
								strToolTip += "&#xD; "+XSSUtil.encodeForHTML(context, (String)slDelNames.get(j))+" "+strIntState+" "+strInternationalDate;
						}
						strIcon = "iconSmallDocumentAttachment";
					}else{
						strToolTip = strI18nAddDeliverable;
						strIcon = "utilTreeLineNodeClosedSBDisabled";
					}
					sbHrefMaker = new StringBuffer();
					//Added for special character.
					//strToolTip = XSSUtil.encodeForHTML(context, strToolTip); //Commented for: 168842V6R2013x&167942V6R2013x
					iconDeliverable = "<img src=\"../common/images/"+strIcon+".gif\" border=\"0\" alt=\"" + strToolTip + "\" title=\""+ strToolTip + "\"/>";

					sbHrefMaker.append("../common/emxPortal.jsp?portal=PMCDefaultGatePortal");
					sbHrefMaker.append("&amp;suiteKey=ProgramCentral");
					sbHrefMaker.append("&amp;StringResourceFileId=emxProgramCentralStringResource");
					sbHrefMaker.append("&amp;SuiteDirectory=programcentral&amp;objectId="+strTaskId+"&amp;isFromRMB=true&amp;isFromRMB=true");

					strHref = sbHrefMaker.toString();
					sbLinkMaker = new StringBuffer();
					sbLinkMaker.append("<a href=\"javascript:emxTableColumnLinkClick('" + strHref);
					sbLinkMaker.append("', '600', '600', 'false', 'popup','')\"  class='object'>");
					sbLinkMaker.append(iconDeliverable);
					sbLinkMaker.append("</a>");
					if(("CSV".equals(exportFormat) || "HTML".equals(exportFormat)) &&
							!strI18nAddDeliverable.equals(strToolTip)){
						vecIconList.add(strToolTip);
					}else{
						vecIconList.add(sbLinkMaker.toString());
					}
				}else{
					vecIconList.add(DomainConstants.EMPTY_STRING);
				}
			}

			return vecIconList;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new MatrixException(ex);
		}
	}

	/**
	 * Denies Edit acess to object of type Project Template at the root level
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args    argument map,it contains requestMap
	 * @return StringList containing column values
	 * @throws MatrixException if the operation fails
	 */
	public StringList isProjectTemplateSummaryTableColumnsEditable (Context context, String[] args) throws MatrixException
	{
		//Added:RG6:14:PRG:R213:14-11-2011::Start
		MapList objectList = null;
		Map mapProgram = null ;
		try
		{
			mapProgram = (Map) JPO.unpackArgs(args);
			objectList = (MapList) mapProgram.get("objectList");
		}
		catch (Exception e)
		{
			throw new MatrixException(e);
		}

		StringList slHasAccess = new StringList();
		if(null == objectList)
		{
			throw new IllegalArgumentException("Object list is null");
		}

		Map mpData = (Map)objectList.get(0);
		String strId = (String)mpData.get(DomainConstants.SELECT_ID);
		int intLevel = Integer.parseInt((String)mpData.get(DomainConstants.SELECT_LEVEL));
		DomainObject domObj = DomainObject.newInstance(context,strId);

		if( (intLevel==0 && domObj.isKindOf(context,ProgramCentralConstants.TYPE_PROJECT_TEMPLATE)) )
		{
			slHasAccess.add(false);
		}
		else
		{
			slHasAccess.add(true);
		}

		for(int i=1; i<objectList.size();i++)
		{
			slHasAccess.add(true);
		}

		return slHasAccess;
		//End:RG6::PRG:R213:14-11-2011::End
	}

	/**
	 * Returns false if method invoked from RMB.
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap
	 * @return boolean true or false values.
	 * @throws Exception if the operation fails.
	 * @since R214
	 * */

	public boolean isNotFromRMB(Context context,String[] args) throws MatrixException {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strIsRMB =  (String)programMap.get("isRMB");
			if(ProgramCentralUtil.isNotNullString(strIsRMB) && "true".equals(strIsRMB))
				return false;
			else
				return true;
		}catch (Exception e){
			throw new MatrixException(e);
		}
	}

	/**
	 * Returns reload range values of Percentage for particular item in WBS of Projects.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments.
	 * @throws Exception if operation fails
	 */
	public Map getPercentageRangeValues(Context context, String[] args)throws Exception
	{
		Map returnMap = new HashMap();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap hmRowValues = (HashMap) programMap.get("rowValues");
			String strObjectId = (String) hmRowValues.get("objectId");

			if(ProgramCentralUtil.isNotNullString(strObjectId)){
				DomainObject domObj = DomainObject.newInstance(context,strObjectId);
				String sObjectPolicy = (String) domObj.getInfo(context, DomainObject.SELECT_POLICY);
				StringList percentageList = new StringList();

				if(sObjectPolicy.equalsIgnoreCase(ProgramCentralConstants.POLICY_PROJECT_REVIEW)){
					percentageList.add("0.0");
					percentageList.add("100.0");
				}else{
					String percentages = EnoviaResourceBundle.getProperty(context,"emxComponents.TaskPercentages");
					StringTokenizer parser = new StringTokenizer(percentages,",");
					while (parser.hasMoreTokens()){
						String sValue = parser.nextToken();
						sValue = sValue.trim();
						percentageList.add(sValue);
					}
				}

				returnMap.put("RangeValues", percentageList);
				returnMap.put("RangeDisplayValue", percentageList);
			}
			return returnMap;
		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * Added to  exclude the projects while add project above and below of selected task
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments.
	 * @return StringList
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeProjectsfromAddExisting(Context context, String[] args)throws MatrixException
	{
		StringList busSelectsAll = new StringList();
		String strMasterProjectId=ProgramCentralConstants.EMPTY_STRING;
		String defaultOrg = PersonUtil.getDefaultOrganization(context, context.getUser());
		String defaultProj = PersonUtil.getDefaultProject(context, context.getUser());

		final String SELECT_EXPERIMET_PARENT_ID = "to["+ProgramCentralConstants.RELATIONSHIP_EXPERIMENT+"].from.id";
		final String SELECT_MEMBER_ID = "from["+DomainRelationship.RELATIONSHIP_MEMBER+"].to.id";

		try {
			Map programMap = (Map)JPO.unpackArgs(args);
			strMasterProjectId = (String)programMap.get("objectId");
			DomainObject task = DomainObject.newInstance(context,strMasterProjectId);
			StringList domSelectable = new StringList();
			domSelectable.addElement(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);
			domSelectable.addElement(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
			Map taskInfo  =task.getInfo(context, domSelectable);
			boolean isKindOfTaskManagement = "True".equalsIgnoreCase((String)taskInfo.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT));
			if(isKindOfTaskManagement){
				strMasterProjectId = (String)taskInfo.get(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);
			}
			busSelectsAll.addElement(strMasterProjectId);
			DomainObject doProjectSpcae =DomainObject.newInstance(context, strMasterProjectId);

			StringList busSelects = new StringList();
			busSelects.add(doProjectSpcae.SELECT_TYPE);
			busSelects.add(doProjectSpcae.SELECT_ID);
			busSelects.add(DomainObject.SELECT_CURRENT);
			busSelects.add(SELECT_MEMBER_ID);
			busSelects.add(SELECT_OWNER);
			busSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY);
			String relPattern = DomainConstants.RELATIONSHIP_SUBTASK;
			String typePattern=ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT;
			MapList mlRelatedProjects=doProjectSpcae.getRelatedObjects(context,relPattern,typePattern,busSelects,null,false,true,(short)0,null,null,0);
			MapList mlRelatedProjectsToRelationship=doProjectSpcae.getRelatedObjects(context,relPattern,typePattern,busSelects,null,true,false,(short)0,null,null,0);
			mlRelatedProjects.addAll(mlRelatedProjectsToRelationship);

			String strType = doProjectSpcae.getInfo(context, SELECT_TYPE);

			//Added for "WhatIf" functionallity to hide already added project in the experiment
			String strRelatedPtojectTypes = ProgramCentralConstants.EMPTY_STRING;
			if(ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(strType)){
				for(int i=0; i<mlRelatedProjects.size();i++){
					Map mpRelatedProject = (Map) mlRelatedProjects.get(i);
					strRelatedPtojectTypes = (String)mpRelatedProject.get(DomainObject.SELECT_TYPE);

					if(strRelatedPtojectTypes.equalsIgnoreCase(ProgramCentralConstants.TYPE_EXPERIMENT)){
						busSelectsAll.addElement((String)mpRelatedProject.get(DomainObject.SELECT_ID));
					}
				}

				String strProjectIds[] = new String[busSelectsAll.size()];
				for(int i=0;i<busSelectsAll.size();i++){
					strProjectIds[i] = (String)busSelectsAll.get(i);
				}
				StringList slSelectable = new StringList();
				slSelectable.addElement(SELECT_EXPERIMET_PARENT_ID);

				MapList mlProjectLists  = DomainObject.getInfo(context, strProjectIds, slSelectable);

				for(int index=0;index<mlProjectLists.size();index++){
					Map projectMap = (Map) mlProjectLists.get(index);
					String strProjectId = (String)projectMap.get(SELECT_EXPERIMET_PARENT_ID);
					if(strProjectId != null){
						busSelectsAll.addElement(strProjectId);
					}
				}//End
			}else{
				for (int i = 0; i < mlRelatedProjects.size(); i++){
					Map mpRelatedProject = (Map) mlRelatedProjects.get(i);
					strRelatedPtojectTypes = (String)mpRelatedProject.get(DomainObject.SELECT_TYPE);

					if(strRelatedPtojectTypes.equalsIgnoreCase(ProgramCentralConstants.TYPE_PROJECT_SPACE)|| strRelatedPtojectTypes.equalsIgnoreCase(ProgramCentralConstants.TYPE_PROJECT_CONCEPT))
					{
						busSelectsAll.addElement((String)mpRelatedProject.get(DomainObject.SELECT_ID));
					}
				}
			}

			String typePatternPrj = ProgramCentralConstants.TYPE_PROJECT_SPACE + "," +ProgramCentralConstants.TYPE_PROJECT_CONCEPT;
			MapList mlProjectList = DomainObject.findObjects(context,
					typePatternPrj,
					null,
					null,
					busSelects);

			StringList typeList = new StringList(ProgramCentralConstants.TYPE_EXPERIMENT);
			if(!TYPE_PROJECT_CONCEPT.equalsIgnoreCase(strType)) { //When Project Concept is parent, another concept can be added as sub-project
				typeList.add(ProgramCentralConstants.TYPE_PROJECT_CONCEPT);
			}


			StringList stateList = new StringList(DomainObject.STATE_PROJECT_SPACE_ARCHIVE);
			stateList.add(ProgramCentralConstants.STATE_PROJECT_SPACE_COMPLETE);
			stateList.add(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);
			stateList.add(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD);

			for(int i=0;i<mlProjectList.size();i++){
				Map mpProject = (Map) mlProjectList.get(i);
				String strArchivePtojectId = (String)mpProject.get(DomainObject.SELECT_ID);
				String owner = (String)mpProject.get(SELECT_OWNER);
				String strPtojectType = (String)mpProject.get(DomainObject.SELECT_TYPE);
				String strProjectState = (String)mpProject.get(DomainObject.SELECT_CURRENT);
				String strProjectVisibility = (String)mpProject.get(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY);

				StringList orgList = PersonUtil.getOrganizations(context, owner, "");
				StringList collabSpaceList = PersonUtil.getProjects(context, owner, "");

				if((typeList.contains(strPtojectType) || stateList.contains(strProjectState)
						|| (strProjectVisibility.equalsIgnoreCase("Company") && !(orgList.contains(defaultOrg)))
						|| (strProjectVisibility.equalsIgnoreCase("Member") && !(orgList.contains(defaultOrg)) && !(collabSpaceList.contains(defaultProj))))){

					busSelectsAll.addElement(strArchivePtojectId);
				}
			}

		}catch (Exception e) {
			throw new MatrixException(e);
		}

		return busSelectsAll;
	}

	/**
	 * Get the list edit access settings for Name column in Prject WBS
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request arguments
	 * @return A list of edit access settings for Name column in Project WBS.
	 * @throws MatrixException if operation fails.
	 */
	public StringList isTaskNameCellEditable(Context context, String[] args) throws MatrixException{
		try{
			StringList isCellEditable = new StringList();
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			int size = objectList.size();
			String[] objIds = new String[size];

			for(int i=0;i<size;i++){
				Map objectMap = (Map)objectList.get(i);
				String id = (String)objectMap.get(SELECT_ID);
				objIds[i] = id;
			}

			MapList taskInfoList = ProgramCentralUtil.getObjectDetails(context, objIds, new StringList(SELECT_CURRENT));
			for(int i=0;i<size;i++){
				Map objectMap = (Map)taskInfoList.get(i);
				String taskState = (String)objectMap.get(SELECT_CURRENT);

				if(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equals(taskState) || 
						ProgramCentralConstants.STATE_PROJECT_SPACE_ARCHIVE.equals(taskState)){
					isCellEditable.add("false");
				}else{
					isCellEditable.add("true");
				}
			}

			return isCellEditable;

		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	/**
	 * Returns true if Question command is accessible. Given task must be part of a template
	 * to access this command.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args request arguments
	 * @return true if Question command is accessible
	 * @throws Exception if operation fails.
	 */
	public boolean isQuestionCommandAccessible(Context context, String args[]) throws Exception{
		boolean hasAccess = Boolean.TRUE;
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String taskId = (String)inputMap.get("objectId");
		HashMap settingsMap = (HashMap) inputMap.get("SETTINGS");
		String commandName = (String)settingsMap.get("CmdName");
		String SELECT_HAS_TASK_QUESTION = "to["+ RELATIONSHIP_QUESTION +"]";

		StringList busSelects = new StringList();
		busSelects.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);
		busSelects.add(SELECT_HAS_TASK_QUESTION);

		DomainObject domObj = DomainObject.newInstance(context, taskId);
		Map infoMap = domObj.getInfo(context, busSelects);
		String hasTaskQuestion = (String) infoMap.get(SELECT_HAS_TASK_QUESTION);
		String rootObjType = (String) infoMap.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);
		if(DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(rootObjType)){//From task popup in Template side.
			ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
			boolean isCtxUserOwnerOrCoOwner =  projectTemplate.isOwnerOrCoOwner(context, taskId);
			if(!isCtxUserOwnerOrCoOwner)
				return false;
		}

		if("PMCAssignQuestion".equalsIgnoreCase(commandName) || "PMCCreateQuestionAndAssign".equalsIgnoreCase(commandName)){
			hasAccess = !Boolean.valueOf(hasTaskQuestion);
		} else if("PMCUnassignQuestion".equals(commandName)){
			hasAccess = Boolean.valueOf(hasTaskQuestion);
		}
		return hasAccess;
	}

	public void createOwnershipForTaskAssigneeTrigger(Context context, String args[])throws Exception{
		String personId = args[0];
		String personName = args[1];
		String taskId = args[2];

		if(ProgramCentralUtil.isNotNullString(personName) && !DomainAccess.hasObjectOwnership(context, taskId, null, personName+"_PRJ", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP))
		{
			DomainAccess.createObjectOwnership(context, taskId, personId, "Project Lead", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
		}
	}

	public void deleteOwnershipForTaskAssigneeTrigger(Context context, String args[])throws Exception{
		String personId = args[0];
		String personName = args[1];
		String taskId = args[2];
		DomainAccess.deleteObjectOwnership(context, taskId, null, personName+"_PRJ", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
	}

	/**
	 * This method returns false when we want to hide this column and hideWeeklyEfforts is passed in as true.
	 * @param context the user context object for the current session
	 * @param args contains the parameter map.
	 * @throws Exception if the operation fails
	 */
	public boolean hideWeeklyEfforts(Context context, String args[]) throws MatrixException
	{
		boolean blAccess = true;
		try {
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			String hideWeeklyEfforts = (String)inputMap.get("hideWeeklyEfforts");
			if(null!=hideWeeklyEfforts && "true".equals(hideWeeklyEfforts)) {
				blAccess = false;
			}
		}catch (Exception e) {
			throw new MatrixException(e);
		}
		return blAccess;
	}


	//NEW Task creation UI
	/**
	 * Get Task policy Range value.
	 * @param context - the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return policy range value.
	 * @throws Exception if operation fails.
	 */
	public Map getTaskPolicyRange(Context context, String[] args)  throws Exception
	{
		Map mpPolicyMap = new HashMap();
		String language = context.getSession().getLanguage();
		try{
			Map programMap =  JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");
			String languageStr = (String) requestMap.get("languageStr");
			String taskType = (String) requestMap.get("type");
			StringList taskTypeList = FrameworkUtil.split(taskType, ProgramCentralConstants.COMMA);
			String policy= (String) requestMap.get("PolicyName");

			if (((String)taskTypeList.get(0)).contains("_selectedType:")){
				taskType =	(String)(FrameworkUtil.split((String)taskTypeList.get(0), ":")).get(1);
			}else if(ProgramCentralUtil.isNotNullString(taskType)){
				taskType = taskType;
			} else {
	    		if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(policy))
	    		{
	    			taskType = ProgramCentralConstants.TYPE_GATE;
	    		}else
	    		{
				taskType = ProgramCentralConstants.TYPE_TASK;
			}
			}

			MapList policyList = mxType.getPolicies(context, taskType, true);

			StringList fieldRangeValues = new StringList();
			StringList fieldDisplayRangeValues = new StringList();

			if (ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(policy)) {

				String policyName = ProgramCentralConstants.POLICY_PROJECT_REVIEW;
				String policyDisplayValue = EnoviaResourceBundle.getAdminI18NString(context, "Policy", policyName,language);

				fieldRangeValues.addElement(policyName);
				fieldDisplayRangeValues.addElement(policyDisplayValue);

			} else {
				Iterator itr = policyList.iterator();
				while (itr.hasNext()) {
					Map policyMap = (Map) itr.next();
					String policyName=(String)policyMap.get("name");
					String policyDisplayValue = EnoviaResourceBundle.getAdminI18NString(context, "Policy", policyName,language);

					fieldRangeValues.addElement(policyName);
					fieldDisplayRangeValues.addElement(policyDisplayValue);
				}
			}

			mpPolicyMap.put("field_choices", fieldRangeValues);
			mpPolicyMap.put("field_display_choices", fieldDisplayRangeValues);

		}catch(Exception e)       {
			e.printStackTrace();
			throw e;
		}

		return  mpPolicyMap;
	}

	/**
	 * Get selected task policy.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - The args hold information about object.
	 * @return selected policy
	 * @throws Exception If operation fails.
	 */
	public Map getTaskPolicy(Context context, String[] args)throws Exception
	{
		Map mpPolicyMap	=	new HashMap();
		try{
			Map programMap 			= JPO.unpackArgs(args);
			Map requestMap 			= (Map)programMap.get("requestMap");
			String languageStr = (String) requestMap.get("languageStr");
			String taskType = (String) requestMap.get("type");
			StringList taskTypeList = FrameworkUtil.split(taskType, ProgramCentralConstants.COMMA);

			if (((String)taskTypeList.get(0)).contains("_selectedType:")){
				taskType =	(String)(FrameworkUtil.split((String)taskTypeList.get(0), ":")).get(1);
			} else {
				taskType = ProgramCentralConstants.TYPE_TASK;
			}

			MapList policyList =mxType.getPolicies(context, taskType, true);

			StringList fieldRangeValues = new StringList();
			StringList fieldDisplayRangeValues = new StringList();

			Iterator itr = policyList.iterator();
			while (itr.hasNext())
			{
				Map policyMap = (Map) itr.next();
				String policyName=(String)policyMap.get("name");
				String policyDisplayValue=EnoviaResourceBundle.getAdminI18NString(context,
						"Policy", policyName, context.getSession().getLanguage());
				fieldRangeValues.addElement(policyName);
				fieldDisplayRangeValues.addElement(policyDisplayValue);
			}

			mpPolicyMap.put("RangeValues", fieldRangeValues);
			mpPolicyMap.put("RangeDisplayValue", fieldDisplayRangeValues);

		}catch(Exception e)       {
			e.printStackTrace();
			throw e;
		}
		return  mpPolicyMap;
	}

	/**
	 * Create new task.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - The args hold information about object.
	 * @return New object.
	 * @throws Exception If operation fails.
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map createNewTask(Context context,String[]args)throws Exception
	{
		Task task =  (Task)DomainObject.newInstance(context,DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
		Task newTask = (Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);

		Map returnMap=new HashMap();
		try{
			Map programMap = JPO.unpackArgs(args);

			String selectedTaskId           = (String) programMap.get("objectId");
			String parentId			        = (String) programMap.get("parentId");
			String addTask 					= (String)programMap.get("addTask");
			String autonameCheck 			= (String) programMap.get("autoNameCheck");
			String taskName 				= (String) programMap.get("Name");
			String taskType 				= (String) programMap.get("TypeActual");
			String selectedPolicy 			= (String) programMap.get("Policy");
			String ownerId 					= (String) programMap.get("OwnerOID");
			String description 				= (String) programMap.get("Description");
			String assigneeIds 				= (String) programMap.get("AssigneeOID");
			String taskRequirement 			= (String) programMap.get("TaskRequirement");
			String projectRole 				= (String) programMap.get("ProjectRole");
			String calendarId 				= (String) programMap.get("Calendar");
			String taskConstraintDate 		= (String) programMap.get("TaskConstraintDate");
			String taskConstraintType 		= (String) programMap.get("Task Constraint Type");
			String durationKeyword 			= (String) programMap.get("DurationKeywords");
			String duration = (String) programMap.get("Duration");
			String durationUnit = (String) programMap.get("units_Duration");
			String deliverableId            = (String) programMap.get("DeliverableOID");

			if("true".equalsIgnoreCase(autonameCheck)){
				String symbolicTypeName 	= PropertyUtil.getAliasForAdmin(context, "Type", taskType, true);
				String symbolicPolicyName 	= PropertyUtil.getAliasForAdmin(context, "Policy", selectedPolicy, true);

				taskName =  FrameworkUtil.autoName(context,
						symbolicTypeName,
						null,
						symbolicPolicyName,
						null,
						null,
						true,
						true);
			}

			if(ProgramCentralUtil.isNullString(selectedPolicy)){
				selectedPolicy = task.getDefaultPolicy(context);
			}

			if(ProgramCentralUtil.isNullString(parentId)){
				parentId = selectedTaskId;
			}

			Map <String,String>basicTaskInfoMap = new HashMap();
			Map <String,String>taskAttributeMap = new HashMap();
			Map <String,String>relatedInfoMap = new HashMap();

			if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(selectedPolicy)){
				duration = "0";
				durationUnit = "d";
			}

			//Basic info
			basicTaskInfoMap.put("name", taskName);
			basicTaskInfoMap.put("type", taskType);
			basicTaskInfoMap.put("policy", selectedPolicy);
			basicTaskInfoMap.put("description", description);
			basicTaskInfoMap.put("ParentId", parentId);
			basicTaskInfoMap.put("selectedObjectId", selectedTaskId);
			basicTaskInfoMap.put("AddTask", addTask);

			//Attribute info
			taskAttributeMap.put("Project Role", projectRole);
			taskAttributeMap.put("TaskConstraintDate", taskConstraintDate);
			taskAttributeMap.put("TaskConstraintType", taskConstraintType);
			taskAttributeMap.put("DurationKeywords",durationKeyword);
			taskAttributeMap.put("Duration", duration);
			taskAttributeMap.put("DurationUnit", durationUnit);
			taskAttributeMap.put("TaskRequirement", taskRequirement);

			//Related info
			relatedInfoMap.put("Owner", ownerId);
			relatedInfoMap.put("Assignee", assigneeIds);
			relatedInfoMap.put("Calendar", calendarId);
			relatedInfoMap.put("deliverableId", deliverableId);

			//Create new task
			newTask = task.createTask(context,
					basicTaskInfoMap,
					taskAttributeMap,
					relatedInfoMap);

			String newTaskId = newTask.getObjectId();
			returnMap.put("id", newTaskId);

		}catch(Exception e){
			e.printStackTrace();
		}

		return  returnMap;
	}

	/**
	 * Get assignee Id list.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - The args hold information about object.
	 * @return assignee Id list.
	 * @throws MatrixException if Operation fails.
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeMembersToAddAsAssignee(Context context, String[] args)throws MatrixException
	{
		StringList returnList = new StringList();
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String)programMap.get("objectId");
			com.matrixone.apps.common.Task task = (com.matrixone.apps.common.Task)DomainObject.newInstance(context,DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
			task.setId(objectId);

			StringList busSelect = new StringList(SELECT_ID);

			Map projectInfo = task.getProject(context,busSelect);
			String strProjectId = (String) projectInfo.get(DomainConstants.SELECT_ID);

			DomainObject project = DomainObject.newInstance(context,strProjectId);
			returnList = project.getInfoList(context, ProgramCentralConstants.SELECT_MEMBER_ID);

		} catch(Exception ex){
			throw new MatrixException(ex);
		}

		return returnList;
	}

	/**
	 * Get duration keyword range.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - The args hold information about object.
	 * @return Duration keyword range value.
	 * @throws Exception If operation fails.
	 */
	public Map getDurationKeywordRange(Context context, String[] args)throws Exception
	{
		Map returnMap = new HashMap();
		try
		{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map requestMap = (HashMap)programMap.get("requestMap");
			String selectedNodeId = (String)requestMap.get("objectId");

			StringList durationKeywords = new StringList();
			StringList durationKeywordsDisplayValues = new StringList();

			durationKeywords.add(0, "");
			durationKeywordsDisplayValues.add(0, "");

			DurationKeyword[] durationKeyword = DurationKeywordsUtil.getDurationKeywordsValueForWBS(context,selectedNodeId);
			for(int i=0;i<durationKeyword.length;i++){
				durationKeywords.add(durationKeyword[i].getName()+"|"+durationKeyword[i].getDuration()+"|"+durationKeyword[i].getUnit());
				durationKeywordsDisplayValues.add(durationKeyword[i].getName());
			}

			returnMap.put("field_choices", durationKeywords);
			returnMap.put("field_display_choices", durationKeywordsDisplayValues);

		}catch (Exception e){
			e.printStackTrace();
			throw e;
		}

		return  returnMap;
	}

	/**
	 * Get duration keyword field.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - The args hold information about object.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean hasDurationKeyword(Context context, String[] args)throws Exception
	{
		boolean hasDuarationKeyword = false;
		try	{
			Task task 				= new Task();
			Map programMap = (HashMap) JPO.unpackArgs(args);
			String selectedNodeId = (String)programMap.get("objectId");

			boolean isVisible = task.isVisibleForTemplate(context, selectedNodeId);
			if(isVisible){
				StringList durationKeywords = new StringList();
				StringList durationKeywordsDisplayValues = new StringList();

				durationKeywords.add(0, "");
				durationKeywordsDisplayValues.add(0, "");

				DurationKeyword[] durationKeyword = DurationKeywordsUtil.getDurationKeywordsValueForWBS(context,selectedNodeId);
				if(durationKeyword.length > 0){
					hasDuarationKeyword =  true;
				}
			}

		}catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		return  hasDuarationKeyword;
	}

	/**
	 * Return boolean value.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - The args hold information about object.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean isVisibleForTemplate(Context context,String[] args)throws Exception
	{
		Task task 				= new Task();
		Map programMap 			= JPO.unpackArgs(args);
		String selectedTaskId   = (String) programMap.get("objectId");

		return task.isVisibleForTemplate(context, selectedTaskId);
	}

	public boolean hasAssigneeToolbarAccess(Context context, String args[]) throws Exception{
		return hasAccessForDeliverable(context,args);
	}

	/**
	 *
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param objectId - objectId of task to be updated
	 * @param fieldType - attribute name of the task
	 * @param fieldValue - attribute value of the task
	 */
	private void updateTaskMap(Context context, String objectId, String fieldType, Object fieldValue)
	{
		Map objectMap = (Map) _taskMap.get(objectId);
		if (objectMap == null) {
			objectMap = new HashMap();
			_taskMap.put(objectId, objectMap);
		}
		objectMap.put(fieldType, fieldValue);
	}
	
	
	public String updateForGanttChart(Context context, String[] ganttArgumentArray) throws Exception {

		Map<String,Object> ganttInfoMap  = JPO.unpackArgs(ganttArgumentArray);
		List<Map<String, Map>> startDateMapList = (List)ganttInfoMap.get("startDateMapList");
		List<Map<String, Map>> finishDateMapList = (List)ganttInfoMap.get("finishDateMapList");
		List<Map<String, Map>> durationMapList = (List)ganttInfoMap.get("durationMapList");
		
		if (startDateMapList != null) {
			for(Map<String, Map> startDateMap : startDateMapList) {
				String[] packedArguemntArray = JPO.packArgs(startDateMap);
				updateEstStartDate(context, packedArguemntArray);
			}
		} if (finishDateMapList != null) {
			for(Map<String, Map> finishDateMap : finishDateMapList) {
				String[] packedArguemntArray = JPO.packArgs(finishDateMap);
				updateEstEndDate(context, packedArguemntArray);
			}
		} if (durationMapList != null) {
			for(Map<String, Map> durationMap : durationMapList) {
				String[] packedArguemntArray = JPO.packArgs(durationMap);
				updateEstDuration(context, packedArguemntArray);
			}
		}
		
		Map postProcessMap = new HashMap();
		postProcessMap.put("FromGantt","true");
		String[] argumentArray = JPO.packArgs(postProcessMap);
		Map postProcessRefreshhMap = postProcessRefresh(context,argumentArray);
		String rollupMessage = (String) postProcessRefreshhMap.get("RollupMessage");
		
		return (ProgramCentralUtil.isNotNullString(rollupMessage)) ? rollupMessage : ""; 
	}

	public void updateActualDataForMSP(Context context, String[] actualDataArray) throws Exception {

		Map<String,Object> InfoMap = JPO.unpackArgs(actualDataArray);
		String updateOperation  = (String)InfoMap.get("updateOperation");

		if ("ActualStartDate".equalsIgnoreCase(updateOperation)) {
			updateTaskActualStartDate(context, actualDataArray);
		}
		else if ("ActualFinishDate".equalsIgnoreCase(updateOperation)) {
			updateTaskActualFinishDate(context, actualDataArray);
		}
		postProcessRefresh(context,actualDataArray);
	}
	/**
	 * This Access Function checks access for RMB lifecycle command.
	 * It returns false only for Project Template Task.
	 */
	public boolean hasAccessForTemplateTaskRMB(Context context, String args[]) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);

		String strObjectId = (String)inputMap.get("RMBID");
		if(null !=strObjectId){
			StringList selectables =new StringList();
			selectables.add(SELECT_TASK_PROJECT_TYPE);
			selectables.add(SELECT_TYPE);

			DomainObject dObj = DomainObject.newInstance(context, strObjectId);
			Map infoMAp = dObj.getInfo(context, selectables );

			String isTempLate = (String)infoMAp.get(SELECT_TASK_PROJECT_TYPE);
			String isTask = (String)infoMAp.get(SELECT_TYPE);
			if(DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(isTempLate) &&
					(DomainConstants.TYPE_TASK.equalsIgnoreCase(isTask) || "Phase".equalsIgnoreCase(isTask) || "Milestone".equalsIgnoreCase(isTask))){
				return false;
			}
		}
		return true;
	}

	/**
	 * updateDeliverable - This Method will update the Tasks Deliverable
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return boolean
	 */
	public boolean updateDeliverable(Context context,String args[]) throws Exception
	{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String deliverables[] = (String[]) requestMap.get("Deliverable");
		String deliverableId = deliverables[0];


		String objectId = (String) paramMap.get("objectId");

		task.setId(objectId);
		String strContributesToId = task.getInfo(context,SELECT_CONTRIBUTES_TO_RELATIONSHIP_ID);

		if(ProgramCentralUtil.isNotNullString(deliverableId)){
			DomainObject deliverable = DomainObject.newInstance(context,deliverableId);

			if (ProgramCentralUtil.isNotNullString(strContributesToId))
				DomainRelationship.setToObject( context, strContributesToId, deliverable);
			else
				DomainRelationship.connect( context, task, RELATIONSHIP_CONTRIBUTES_TO, deliverable);
		}
		else
		{
			if (ProgramCentralUtil.isNotNullString(strContributesToId))
				DomainRelationship.disconnect( context, strContributesToId);
		}

		return true;
	}

	/**
	 * Method to check if DPG product is installed and we are creating a Task
	 *
	 * @param context
	 * @param args
	 * @return	true - if DPG is installed and creating a Task
	 * 		false - if DPG is not Installed or creating Phase, Gate, Milestone
	 * @exception throws FrameworkException
	 * @since R418
	 */
	public boolean isDPGInstalledandCreatingTask(Context context,String args[]) throws Exception
	{
		try{
			boolean access = FrameworkUtil.isSuiteRegistered(context,"appVersionENO6WDeliverablesPlanning",false,null,null);
			// fzs we are not adding Deliverables to Task for R418 so disable the field be setting access to false
			access = false;

			if (access == true)
			{
				String taskType = DomainConstants.TYPE_TASK;
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				String sType = (String) programMap.get("type");
				String sObjectId = (String) programMap.get("objectId");

				if (sType != null)
				{
					StringList taskTypeList = FrameworkUtil.split(sType, ProgramCentralConstants.COMMA);

					if (((String)taskTypeList.get(0)).contains("_selectedType:"))
					{
						taskType = (String)(FrameworkUtil.split((String)taskTypeList.get(0), ":")).get(1);
					}

					if (taskType.equalsIgnoreCase(DomainConstants.TYPE_TASK))
						return true;
					else
						return false;
				}
				else
				{
					DomainObject domainObject = DomainObject.newInstance(context,sObjectId);
					if (domainObject.isKindOf(context,TYPE_TASK))
						return true;
					else
						return false;

				}
			}
			return access;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}
	/**
	 * This method returns the list of tasks. This method is called from the copy partial schedule page.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args request arguments
	 * @return list of tasks.
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSubTasks(Context context, String[] args) throws Exception {

		Map arguMap 		= (Map)JPO.unpackArgs(args);
		String strObjectId 	= (String) arguMap.get("objectId");
		String strExpandLevel = (String) arguMap.get("expandLevel");

		StringList relationshipSelects = new StringList();
		StringList objectSelects = new StringList(SELECT_TYPE);
		objectSelects.add(SELECT_ID);
		objectSelects.add(SELECT_HAS_SUBTASK);

		short nExpandLevel =  ProgramCentralUtil.getExpandLevel(strExpandLevel);
		DomainObject rootNodeObject = DomainObject.newInstance(context, strObjectId);
		TaskHolder parent = (TaskHolder)DomainObject.newInstance(context,TYPE_PROJECT_SPACE,PROGRAM);
		((ProjectSpace)parent).setId(strObjectId);

		MapList taskList = Task.getTasks(context,parent,nExpandLevel,objectSelects,relationshipSelects,false);

		for(int i=0; i<taskList.size(); i++) {
			Map taskinfo = (Map) taskList.get(i);
			String hasSubtask = (String) taskinfo.get(SELECT_HAS_SUBTASK);
			if("FALSE".equalsIgnoreCase(hasSubtask)){
				taskinfo.put("hasChildren", "false");
			}
			taskinfo.remove("sortBy");
			taskinfo.remove("id[connection]");
		}
		int taskSize = taskList.size();
		taskList.add(0,taskSize);

		return taskList;
	}

	public void updateNeedsReview(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map mpParamMap = (HashMap)programMap.get("paramMap");
		String strNewVal = (String)mpParamMap.get("New Value");
		String objId = (String)mpParamMap.get("objectId");

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, TYPE_TASK, "PROGRAM");
		task.setId(objId);
		task.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_NEEDS_REVIEW, strNewVal);
	}


	/**
	 * isEstimatedDateCellEditable: Where : if Gate is a Shadow Gate the Estimated Start and End Dates are not editable
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return StringList The StringList object containing Access Information
	 * @throws MatrixException
	 */

	public StringList isEstimatedDateCellEditable(Context context, String[] args) throws MatrixException
	{
		StringList slAccessList = new StringList();
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map objectMap = null;
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];

			int i = 0;
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[i] = (String) objectMap.get(DomainObject.SELECT_ID);
				i++;
			}
			StringList busSelect = new StringList(1);
			busSelect.addElement(SELECT_SHADOW_GATE_ID);
			MapList mlTaskDetails = DomainObject.getInfo(context, objIdArr, busSelect);

			for (Iterator itrTableRows = mlTaskDetails.iterator(); itrTableRows.hasNext();)
			{
				Map map = (Map)itrTableRows.next();
				String strShadowGateId = (String)map.get(SELECT_SHADOW_GATE_ID);
				if(strShadowGateId == null || strShadowGateId.equals("")){
					slAccessList.add(true);
				}else{
					slAccessList.add(false);
				}
			}
			return slAccessList;

		} catch (Exception exp) {
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}
	/**
	 * This method is used to show Default Calendar column of Project calendar table.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @returns Vector containing column data.
	 * @throws Exception if the operation fails
	 */
	public Vector getDefaultProjectCalendarColumn(Context context, String[] args) throws Exception {

		Vector returnList = new Vector();
		Map programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		Iterator objectLiIterator = objectList.iterator();
		while(objectLiIterator.hasNext()){
			Map calendarMap =  (Map)objectLiIterator.next();
			String objectId = (String)calendarMap.get(SELECT_ID);

			StringBuilder sb = new StringBuilder();
			sb.append("<input type=\"radio\" name=\"DefaultCalendar\" id=\"DefaultCalendar\" value=\"");
			sb.append(objectId);
			sb.append("\"/>");

			returnList.add(sb.toString());
		}
		return returnList;

   }
/**
 * This method is called from a table displayed on the project properties page.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the input arguments
 * @return  Vector containing column data.
 * @throws Exception if the operation fails
 */
   public Vector getDefaultCalendarColumn(Context context, String[] args) throws Exception {

	   Vector vResult = new Vector();
	   Map paramMap = (Map) JPO.unpackArgs(args);
	   Map paramList = (Map)paramMap.get("paramList");
	   MapList objectList = (MapList) paramMap.get("objectList");
	   String sLanguage = (String)paramList.get("languageStr");

	   String sLabelYes = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Yes", sLanguage);
	   String sLabelNo = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.No", sLanguage);

	   sLabelYes = XSSUtil.encodeForXML(context, sLabelYes);
	   sLabelNo = XSSUtil.encodeForXML(context, sLabelNo);

	   boolean isChangeDefaultCalendarAllowed = false;
	   if(objectList.size() > 0){
		   Map calendarInfo = (Map)objectList.get(0);
		   String parentProjectId = (String)calendarInfo.get("id[parent]");

		   if(ProgramCentralUtil.isNotNullString(parentProjectId)){
			   DomainObject project = DomainObject.newInstance(context, parentProjectId);
			   StringList selectable = new StringList(SELECT_CURRENT);
			   selectable.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
			   selectable.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
			   Map projectInfoMap = (Map)project.getInfo(context, selectable);

			   String projectState = (String)projectInfoMap.get(SELECT_CURRENT);
			   String projectSpaceType =  (String)projectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
			   String projectConceptType =  (String)projectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
			   
			   if("TRUE".equalsIgnoreCase(projectSpaceType) && (ProgramCentralConstants.STATE_PROJECT_SPACE_CREATE.equalsIgnoreCase(projectState) || ProgramCentralConstants.STATE_PROJECT_SPACE_ASSIGN.equalsIgnoreCase(projectState))){
				   isChangeDefaultCalendarAllowed = true;
			   }
			   else if("TRUE".equalsIgnoreCase(projectConceptType)){
				   isChangeDefaultCalendarAllowed = true;
			   }
		   }
	   }

	   String sResult = EMPTY_STRING;
	   Iterator<Map> objectListIterator = objectList.iterator();
	   while(objectListIterator.hasNext()){
		   Map calendarMap = (Map) objectListIterator.next();
		   String sRowID = (String)calendarMap.get("id[level]");
		   String sOID = (String)calendarMap.get(ProgramCentralConstants.SELECT_ID);
		   String sRelId = (String)calendarMap.get(DomainRelationship.SELECT_ID);
		   String parentId = (String)calendarMap.get("id[parent]");
		   String relationshipName = (String)calendarMap.get(ProgramCentralConstants.KEY_RELATIONSHIP);

		   String sStyleNo = "style='background-color:#5F747D;color:#FFFFFF;font-weight:normal;min-width:90px;width=100%;text-align:center;vertical-align:middle;height:20px;line-height:20px;padding:0px;margin:0px;'";
		   String sStyleYes = "style='background-color:#5F747D;color:#FFFFFF;font-weight:normal;min-width:90px;width=100%;text-align:center;vertical-align:middle;height:20px;line-height:20px;padding:0px;margin:0px;'";

		   StringBuilder sbResult = new StringBuilder();
		   if(ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR.equalsIgnoreCase(relationshipName)) {

			   sbResult.append("<div ");
			   sbResult.append(sStyleYes);

			   if(isChangeDefaultCalendarAllowed) {
				   sbResult.append("  onclick='window.open(\"../programcentral/emxProgramCentralUtil.jsp?mode=defaultProjectCalendar&amp;subMode=removeDefaultProjectCalendar&amp;relationship="+ ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS + "&amp;from=false&amp;objectId=" + sOID + "&amp;parentId=" + parentId + "&amp;rowId=" + sRowID +"&amp;relId=" + sRelId + "\", \"listHidden\", \"\", true);'");
				   sbResult.append(" onmouseout='this.style.background=\"#5F747D\";this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.innerHTML=\"").append(sLabelYes).append("\"'");
				   sbResult.append(" onmouseover='this.style.background=\"#cc0000\";    this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.style.cursor=\"pointer\"; this.innerHTML=\"").append(sLabelNo).append("\"'");
			   }
			   sbResult.append(">").append(sLabelYes).append("</div>");
		   } else {

			   sbResult.append("<div ");
			   sbResult.append(sStyleNo);

			   if(isChangeDefaultCalendarAllowed) {
				   sbResult.append(" onclick='window.open(\"../programcentral/emxProgramCentralUtil.jsp?mode=defaultProjectCalendar&amp;subMode=setAsDefaultProjectCalendar&amp;relationship=" + ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS + "&amp;from=false&amp;objectId=" + sOID + "&amp;rowId=" + sRowID + "&amp;parentId=" + parentId + "&amp;relId=" + sRelId + "\", \"listHidden\", \"\", true);'");
				   sbResult.append(" onmouseout='this.style.background=\"#5F747D\";this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.innerHTML=\"").append(sLabelNo).append("\"'");
				   sbResult.append(" onmouseover='this.style.background=\"#009c00\"; this.style.color=\"#FFF\";this.style.fontWeight=\"normal\"; this.style.cursor=\"pointer\"; this.innerHTML=\"").append(sLabelYes).append("\"'");
			   }
			   sbResult.append(">").append(sLabelNo).append("</div>");
		   }

		   sResult = sbResult.toString();
		   vResult.add(sResult);
	   }
	   return vResult;
	}
   /**
    * This method is used to hide unnecessary RMB command from Project Template WBS.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the input arguments
    * @return true/false depends on condition
    * @throws Exception
    */
    public boolean hasAccessForWBSRMBCommand(Context context, String args[]) throws Exception {

	   boolean hasAccess = Boolean.TRUE;
	   Map inputMap = (Map)JPO.unpackArgs(args);
	   String objectId = (String)inputMap.get("objectId");

	   StringList busSelects = new StringList();
	   busSelects.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);

	   DomainObject domObj = DomainObject.newInstance(context, objectId);
	   Map infoMap = domObj.getInfo(context, busSelects);

	   String objType = (String) infoMap.get(DomainConstants.SELECT_TYPE);
	   String rootObjType = (String) infoMap.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);

	   if(DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(objType) ||
			   DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(rootObjType)) {	//From task popup in Template side.
		   ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
		   boolean isCtxUserOwnerOrCoOwner =  projectTemplate.isOwnerOrCoOwner(context, objectId);
		   return (isCtxUserOwnerOrCoOwner) ? true : false;
	   }

	   if(hasAccess){
		   hasAccess = hasAccessForWBSView(context, args);
	   }
	   return hasAccess;
   }

    /**
    * Invoked from Attachment for Risk,Quality,Business Goal or Task Deliverables Action Toolbar.
    *
    * This function returns true/false depending on State of the Task.
    * @param context the eMatrix <code>Context</code> object
    * @param The args hold information about object.
    * @return False-if task is in Review or Complete state.True in case of all other states.
    * @throws Exception if operation fails.
    */
    public boolean hasAccessToPMCContentSummaryToolBarActions(Context context, String args[]) throws Exception {

    	Map inputMap = (Map)JPO.unpackArgs(args);
    	String objectId = (String)inputMap.get("objectId");
    	boolean hasAccess = true;

    	StringList selectables = new StringList();
    	selectables.add(SELECT_CURRENT);
    	selectables.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
    	selectables.add(ProgramCentralConstants.SELECT_IS_BUSINESS_GOAL);//If called from Risk/Quality/BusinessGoal
    	selectables.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);

    	DomainObject domOBj = DomainObject.newInstance(context, objectId);
    	Map infoMap = (Map) domOBj.getInfo(context, selectables);

    	String currState = (String) infoMap.get(SELECT_CURRENT);
    	String isKindOfTaskMgmt = (String) infoMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
		String isKindOfBusinessGoal	= (String) infoMap.get(ProgramCentralConstants.SELECT_IS_BUSINESS_GOAL);
		String objType = (String) infoMap.get(DomainConstants.SELECT_TYPE);
		String rootObjType = (String) infoMap.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);

		hasAccess = !Boolean.valueOf(isKindOfBusinessGoal) ||
					!DomainConstants.STATE_BUSINESS_GOAL_COMPLETE.equalsIgnoreCase(currState);

		if(DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(objType) ||
				DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(rootObjType)){//From task popup in Template side.
			ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
			boolean isCtxUserOwnerOrCoOwner =  projectTemplate.isOwnerOrCoOwner(context, objectId);
			if(!isCtxUserOwnerOrCoOwner)
				return false;
		}

    	if(Boolean.valueOf(isKindOfTaskMgmt)){
    		hasAccess = true;
    		if(STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(currState) ||
    		   STATE_PROJECT_TASK_COMPLETE.equalsIgnoreCase(currState)){
    			hasAccess = false;
    		}
    	}

    	return hasAccess;
    }

    /**
     * This method will return the task estimated duration along with input unit.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the input arguments
     * @return the task estimated duration along with input unit.
     * @throws Exception if the operation fails
     */
    public StringList getEstimatedDurationColumn(Context context, String[] args) throws Exception {
    	StringList returnList = new StringList();
    	String SELECT_WORKING_TIME_PER_DAY = "from[" + ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR + "].to."+WorkCalendar.SELECT_WORKING_TIME_PER_DAY;
    	try
    	{
    		String strLanguage = context.getSession().getLanguage();
    		Map projectMap = (Map) JPO.unpackArgs(args);
    		MapList objectList = (MapList) projectMap.get("objectList");

    		int size = objectList.size();
    		String[] strObjectIds = new String[size];

    		for (int i = 0; i < size; i++) {
    			Map mapObject = (Map) objectList.get(i);
    			String taskId = (String) mapObject.get(DomainObject.SELECT_ID);
    			strObjectIds[i] = taskId;
    		}

    		String taskEstimatedDuration = EMPTY_STRING;
    		String durationInputUnit = EMPTY_STRING;
    		String strI18Days = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.DurationUnits.Days", strLanguage);
    		String strI18Hours = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.DurationUnits.Hours", strLanguage);

    		StringList slBusSelect = new StringList();
    		slBusSelect.add(DomainConstants.SELECT_ID);
    		slBusSelect.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
    		slBusSelect.add(SELECT_TASK_ESTIMATED_DURATION);
    		slBusSelect.add(SELECT_TASK_ESTIMATED_DURATION+".inputunit");

    		MapList taskInfoList = DomainObject.getInfo(context, strObjectIds, slBusSelect);
    		Iterator<Map> taskInfoIterator = taskInfoList.iterator();

    		while(taskInfoIterator.hasNext()){
    			Map taskInfo = taskInfoIterator.next();
    			taskEstimatedDuration = (String)taskInfo.get(SELECT_TASK_ESTIMATED_DURATION);
    			durationInputUnit = (String)taskInfo.get(SELECT_TASK_ESTIMATED_DURATION+".inputunit");
    			String objectId = (String)taskInfo.get(DomainConstants.SELECT_ID);
    			if("h".equalsIgnoreCase(durationInputUnit)){
    				double duration = Task.parseToDouble(taskEstimatedDuration);    				
    				String isTaskMagt = (String)taskInfo.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
    				if("false".equalsIgnoreCase(isTaskMagt)){
    					DomainObject project = DomainObject.newInstance(context, objectId); 
    					String workingTime = (String)project.getInfo(context, SELECT_WORKING_TIME_PER_DAY);
        				double dWorkingTime = Task.parseToDouble(workingTime);
        				duration = duration * (dWorkingTime/60);
    				}else{
    				duration = duration * HOURS_PER_DAY;
    				}
    				taskEstimatedDuration = Double.toString(duration);
    				taskEstimatedDuration = taskEstimatedDuration + ProgramCentralConstants.SPACE +strI18Hours;
    			} else {
    				taskEstimatedDuration = taskEstimatedDuration + " "+strI18Days;
    			}
    			returnList.add(taskEstimatedDuration);
    		}
    	} catch (Exception ex) {
    		throw ex;
    	}
   		return returnList;
    }

    /**
     * This method will check whether the task is summary task or not, if it's a summary task then the Estimated Duration cell and the Duration Keyword cell will not be editable.
     * @param context  the eMatrix <code>Context</code> object
     * @param args holds the input arguments
     * @return StringList containing values true or false. false- If the task is summary task.
     * @throws Exception if the operation fails
     */
    public StringList isEstimatedDurationCellEditable (Context context, String[] args) throws Exception
    {
    	StringList editAccessList = new StringList();
    	Map programMap = (Map) JPO.unpackArgs(args);
    	MapList objectList = (MapList) programMap.get("objectList");
    	int size = objectList.size();
    	String taskIds[] = new String[size];

    	for(int i=0;i<size;i++){
    		Map<String,String> taskMap = (Map)objectList.get(i);
    		String taskId = taskMap.get(SELECT_ID);
    		taskIds[i] = taskId;
    	}
    	StringList selectables = new StringList();
    	selectables.add(SELECT_FROM_SUBTASK);
    	MapList taskInfoList = ProgramCentralUtil.getObjectDetails(context, taskIds, selectables);

    	Iterator<Map> objectListIterator = taskInfoList.iterator();
    	while(objectListIterator.hasNext()){
    		Map<String,String> taskMap = objectListIterator.next();
    		String hasSubTask = taskMap.get(SELECT_FROM_SUBTASK);

    		if("True".equalsIgnoreCase(hasSubTask)){
    			editAccessList.add("false");
    		} else {
    			editAccessList.add("true");
    		}
    	}

    	return editAccessList;
    }

	/**
	 * When the Deliverable is promoted this function is called.
	 * If Tasks associated with Deliverable are connected to
	 * a Project Space, and are only connected to a single Deliverable
	 * it will be promoted to passed state.
	 *
	 * Note: object id must be passed as first argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the Deliverable object id
	 *        1 - String containing the Deliverable current state
	 *        2 - String containing the Deliverable to state
	 *        3 - String containing the Task to state
	 * @throws Exception if operation fails
	 */
    public int autoPromoteTask(Context context, String[] args) throws Exception
    {
        // get values from args.
        String deliverableOID     = args[0];
        String deliverableCurrent = args[1];
        String deliverableToState = args[2];
        String taskToState        = args[3];
        String strDeliverableMessage = "";
        String strRouteMessage       = "";
        String strChildrenMessage    = "";
        String strLanguage = context.getSession().getLanguage();

        DomainObject doDeliverable = DomainObject.newInstance(context, deliverableOID);

        try
        {
            // get Tasks from Deliverable
            StringList objectSelects = new StringList();
            objectSelects.addElement(SELECT_ID);
            objectSelects.addElement(SELECT_NAME);
            objectSelects.addElement(SELECT_TYPE);
            objectSelects.addElement(SELECT_POLICY);
            objectSelects.addElement(SELECT_CURRENT);
            objectSelects.addElement(SELECT_STATES);
            objectSelects.addElement(SELECT_PERCENT_COMPLETE);
            StringList relSelects = new StringList();
            MapList mlTasks = doDeliverable.getRelatedObjects(context,
                                                              DomainConstants.RELATIONSHIP_TASK_DELIVERABLE,
                                                              DomainConstants.TYPE_TASK,
                                                              objectSelects,
                                                              relSelects,
                                                              true,
                                                              false,
                                                              (short) 1,
                                                              null,
                                                              null,
                                                              0);
            if(!mlTasks.isEmpty())
            {
                Iterator itrTasks = mlTasks.iterator();
                while(itrTasks.hasNext())
                {
                    Map mTask = (Map)itrTasks.next();
                    String strTaskOID        = (String)mTask.get(SELECT_ID);
                    String strTaskName       = (String)mTask.get(SELECT_NAME);
                    String state             = (String) mTask.get(SELECT_CURRENT);
                    StringList taskStateList = (StringList) mTask.get(SELECT_STATES);
                    String newPercent        = (String) mTask.get(SELECT_PERCENT_COMPLETE);
                    double newPercentValue   = Task.parseToDouble(newPercent);
                    String type              = (String) mTask.get(SELECT_TYPE);
                    String taskPolicy        = (String) mTask.get(SELECT_POLICY);

                    //to its state list
                    int taskTargetPosition       = taskStateList.indexOf(taskToState);
                    //get the position of "Active" and "Complete" in the state list
                    int taskActiveStatePosition   = taskStateList.indexOf(STATE_PROJECT_TASK_ACTIVE);
                    int taskReviewStatePosition   = taskStateList.indexOf(STATE_PROJECT_TASK_REVIEW);
                    int taskCompleteStatePosition = taskStateList.indexOf(STATE_PROJECT_TASK_COMPLETE);

                    // check if Task is attached to Project Space
                    if(isProjectSpaceTask(context, strTaskOID))
                    {
                        DomainObject doTask = DomainObject.newInstance(context, strTaskOID);
                        MapList mlDeliverables = doTask.getRelatedObjects(context,
                                                                          DomainConstants.RELATIONSHIP_TASK_DELIVERABLE,
                                                                          "*",
                                                                          objectSelects,
                                                                          relSelects,
                                                                          false,
                                                                          true,
                                                                          (short) 1,
                                                                          null,
                                                                          null,
                                                                          0);

                        // promote Tasks that are attached to single Delivarable
                        if(mlDeliverables.size() > 1)
                        {
                            if(strDeliverableMessage == null || strDeliverableMessage.equals(""))
                            {
                               strDeliverableMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Deliverable.AutoPromoteMultiple", strLanguage) + "\n";
                            }
                            strDeliverableMessage += strTaskName+"\n";
                        }
                        else if(hasChildTasks(context, strTaskOID))
                        {
                            if(strChildrenMessage == null || strChildrenMessage.equals(""))
                        {
                               strChildrenMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Task.AutoPromoteChildren", strLanguage) + "\n";
                            }
                            strChildrenMessage += strTaskName+"\n";
                        }
                        else if(hasConnectedRoutes(context, strTaskOID))
                        {
                            if(strRouteMessage == null || strRouteMessage.equals(""))
                            {
                               strRouteMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Task.AutoPromoteRoutes", strLanguage) + "\n";
                            }
                            strRouteMessage += strTaskName+"\n";
                        }
                        else
                            {

                            //NX5 - Modify Percent Complete Trigger blocking
                            // We need to complete the preqs here
                            if(newPercentValue >= 0 && newPercentValue < 100) {

                                if ( taskReviewStatePosition == taskTargetPosition &&
                                    !TYPE_PART_QUALITY_PLAN.equalsIgnoreCase(type) &&
                                    ProgramCentralConstants.POLICY_PROJECT_TASK.equalsIgnoreCase(taskPolicy)) {

                                    doTask.setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, "100");
                                }
                            }
                            // NX5 - End Blocking Mod

                            // A zero indicates the object is already at that state,
                            // a positive number indicates the number of states promoted,
                            // a negative number indicates the number of states demoted.
                            int iResult = doTask.setState(context, taskToState);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            if(strDeliverableMessage != null && !strDeliverableMessage.equals(""))
            {
                MqlUtil.mqlCommand(context, "notice " +strDeliverableMessage);
            }

            if(strChildrenMessage != null && !strChildrenMessage.equals(""))
            {
                MqlUtil.mqlCommand(context, "notice " +strChildrenMessage);
            }
            if(strRouteMessage != null && !strRouteMessage.equals(""))
            {
                MqlUtil.mqlCommand(context, "notice " +strRouteMessage);
            }
        }

        return 0;
    }

    /**
     * check that passed Task is associated with a Project Space
     *
     * @throws Exception if operation fails
     */
    private boolean isProjectSpaceTask(Context context, String strTaskOID)throws Exception
    {
        boolean bPrjSpace = false;
        com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task();
        task.newInstance(context);
        task.setId(strTaskOID);

        StringList busSelect = new StringList();
        busSelect.addElement(SELECT_ID);
        busSelect.addElement(SELECT_TASK_PROJECT_TYPE);
        busSelect.addElement(ProgramCentralConstants.ATTRIBUTE_PROJECT_SCHEDULE_FROM);
        Map projectInfoMap = new HashMap();
        projectInfoMap = task.getProject(context, busSelect);

        if(projectInfoMap.size() >= 1)
        {
            String strType = (String)projectInfoMap.get("type");
            if(strType.equalsIgnoreCase(ProgramCentralConstants.TYPE_PROJECT_SPACE))
        {
            bPrjSpace = true;
        }
        }

        return bPrjSpace;
    }

    /**
     * check that passed Task has children
     *
     * @throws Exception if operation fails
     */
    private boolean hasChildTasks(Context context, String strTaskOID)
    {
        boolean bHasChildren = false;

        StringList objectSelects = new StringList();
        objectSelects.addElement(DomainRelationship.SELECT_ID);
        objectSelects.addElement(DomainRelationship.SELECT_NAME);
        StringList relSelects = new StringList();

        try
        {
            DomainObject doTask = DomainObject.newInstance(context, strTaskOID);
            MapList mlChildren = doTask.getRelatedObjects(context,
                              DomainConstants.RELATIONSHIP_SUBTASK,
                              "*",
                              objectSelects,
                              relSelects,
                              false,
                              true,
                              (short) 1,
                              null,
                              null,
                              0);
            if(!mlChildren.isEmpty())
            {
                bHasChildren = true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return bHasChildren;
    }

    /**
     * check that passed Task has children
     *
     * @throws Exception if operation fails
     */
    private boolean hasConnectedRoutes(Context context, String strTaskOID)
    {
        boolean bHasRoutes = false;

        StringList objectSelects = new StringList();
        objectSelects.addElement(DomainRelationship.SELECT_ID);
        objectSelects.addElement(DomainRelationship.SELECT_NAME);
        StringList relSelects = new StringList();

        try
        {
            DomainObject doTask = DomainObject.newInstance(context, strTaskOID);
            MapList mlRoutes = doTask.getRelatedObjects(context,
                                                          DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                                                          "*",
                                                          objectSelects,
                                                          relSelects,
                                                          false,
                                                          true,
                                                          (short) 1,
                                                          null,
                                                          null,
                                                          0);
            if(!mlRoutes.isEmpty())
            {
                bHasRoutes = true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return bHasRoutes;
    }

    /**
     * Attachement can be added in Risk(for Project or Task) or Quality(for Project).
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean canCreateNewDocForAttachment(Context context, String args[]) throws Exception	{
    	Map programMap = (Map) JPO.unpackArgs(args);
		String objectId = (String)programMap.get("objectId");

		StringList busSelects = new StringList();
		busSelects.add(ProgramCentralConstants.SELECT_IS_QUALITY);
		busSelects.add(SELECT_CURRENT);

		DomainObject dmoObject 	= DomainObject.newInstance(context, objectId);
		Map  objectInfoMap 		= dmoObject.getInfo(context, busSelects);
		String isKindOfQuality 	= (String) objectInfoMap.get(ProgramCentralConstants.SELECT_IS_QUALITY);
		String current 			= (String) objectInfoMap.get(SELECT_CURRENT);

		boolean hasAccess = !Boolean.valueOf(isKindOfQuality) &&
							!ProgramCentralConstants.STATE_QUALITY_CONTROLLED.equalsIgnoreCase(current);

		return (hasAccess && hasAccessForDeliverable(context, args));
    }


    /**
     * Files can be uploaded for Attachement in Risk(for Project or Task) or Quality(for Project).
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean canUploadFilesForAttachment(Context context, String args[]) throws Exception	{
    	Map programMap = (Map) JPO.unpackArgs(args);
		String objectId = (String)programMap.get("objectId");

		StringList busSelects = new StringList();
		busSelects.add(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
		busSelects.add(SELECT_CURRENT);
		busSelects.add("current.access[fromconnect]");

		DomainObject dmoObject = DomainObject.newInstance(context, objectId);
		Map  objectInfoMap = dmoObject.getInfo(context, busSelects);

		String isKindOfControlledFolder	= (String) objectInfoMap.get(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
		String current = (String) objectInfoMap.get(SELECT_CURRENT);
		String hasFromConnect = (String) objectInfoMap.get("current.access[fromconnect]");

		boolean hasAccess = Boolean.valueOf(hasFromConnect) &&
							(!Boolean.valueOf(isKindOfControlledFolder) ||
							  DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(current));

		return (hasAccess && hasAccessForDeliverable(context, args));
    }


    /**
     * Where : PMCProjectTaskPropertiesEdit & PMCProjectTemplateTaskPropertiesEdit(both from Task RMB and Task Popup)
     *
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean canEditTaskProperties(Context context, String[] args) throws Exception
	{
    	boolean hasAccess = Boolean.TRUE;
		Map programMap = (Map) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");

		StringList busSelects = new StringList();
		busSelects.add(DomainConstants.SELECT_CURRENT);
		busSelects.add(SELECT_IS_DELETED_SUBTASK);
		busSelects.add(DomainConstants.SELECT_HAS_MODIFY_ACCESS);
		busSelects.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);
		busSelects.add(SELECT_IS_PARENT_TASK_DELETED);

		DomainObject domObj = DomainObject.newInstance(context, objectId);
		Map infoMap = domObj.getInfo(context, busSelects);

		String currState = (String) infoMap.get(DomainConstants.SELECT_CURRENT);
		String isDeleted = (String) infoMap.get(SELECT_IS_DELETED_SUBTASK);
		String isParentDeleted = (String) infoMap.get(SELECT_IS_PARENT_TASK_DELETED);
		String hasModifyAcc = (String) infoMap.get(DomainConstants.SELECT_HAS_MODIFY_ACCESS);
		String objType = (String) infoMap.get(DomainConstants.SELECT_TYPE);
		String rootObjType = (String) infoMap.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);
		boolean isAnDInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionAerospaceProgramManagementAccelerator",false,null,null);
		boolean isLocked = false;
		if(DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(objType) ||
			DomainConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(rootObjType)){//From task popup in Template side.
			ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
			hasAccess =  projectTemplate.isOwnerOrCoOwner(context, objectId);
		}else if("TRUE".equalsIgnoreCase(isDeleted) || "TRUE".equalsIgnoreCase(isParentDeleted)){
			hasAccess = Boolean.FALSE;
		}
		else{
			if(isAnDInstalled){
				isLocked  = Task.isParentProjectLocked(context, objectId);
			}
		}
		hasAccess = (hasAccess && Boolean.valueOf(hasModifyAcc) &&
						!(DomainConstants.STATE_PROJECT_SPACE_COMPLETE.equalsIgnoreCase(currState)) && !isLocked);
		return hasAccess;
	}

	public boolean hasAccessToNeedsReview(Context context, String args[]) throws Exception {
		Map params = (Map)JPO.unpackArgs(args);
		String strObjectId = (String)params.get("objectId");
		String strMode = (String)params.get("mode");
		Task task = new Task(strObjectId);
		return task.hasAccessToNeedsReview(context, strMode);
	}
	
	 public boolean hasAccessForTaskManagementCategoryCommand(Context context, String args[]) throws Exception
	 {
		 boolean hasAccess = true;
		 Map programMap =  JPO.unpackArgs(args);
		 String objectId = (String) programMap.get("objectId");
		 DomainObject domainObject = DomainObject.newInstance(context, objectId);
		 StringList selectables = new StringList();
		 selectables.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
		 selectables.add(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT);
		 selectables.add(SELECT_PARENTOBJECT_KINDOF_PROJECT_BASELINE);

		 Map<String,String> taskInfo = domainObject.getInfo(context, selectables);
		 boolean isKindOfTaskManagement = "TRUE".equalsIgnoreCase(taskInfo.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT));
		 boolean isExperimentTask = "TRUE".equalsIgnoreCase(taskInfo.get(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT));
		 boolean isProjectBaselineTask = "TRUE".equalsIgnoreCase(taskInfo.get(SELECT_PARENTOBJECT_KINDOF_PROJECT_BASELINE));

		 if(isKindOfTaskManagement && (isExperimentTask || isProjectBaselineTask)){
			 hasAccess = false;
		 }

		 return hasAccess;
	 }
public static Map getTaskManagementTypes(Context context, String[] args)throws Exception
	    {
	        Map programMap 	= (Map) JPO.unpackArgs(args);
	        Map hmRowValues = (Map) programMap.get("rowValues");
	        String objectId = (String) hmRowValues.get("objectId");
	        
	        StringList taskSubTypesIntNameList = new StringList();

	        StringList objectSelects = new StringList(2);
	        objectSelects.add(SELECT_POLICY);
	        objectSelects.add(ProgramCentralConstants.SELECT_SUMMARY);

	        DomainObject domObj	 = DomainObject.newInstance(context,objectId);
	        Map mapObjectInfo 	 = domObj.getInfo(context,objectSelects);
	        String objectPolicy  = (String) mapObjectInfo.get(SELECT_POLICY);
	        String isSummaryTask = (String) mapObjectInfo.get(ProgramCentralConstants.SELECT_SUMMARY);

			StringList slTaskTypes 				= ProgramCentralUtil.getTaskSubTypesList(context);
	        
        /*if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(sObjectPolicy))
        {
	        	StringList reviewTypeList = new StringList(2);
	        	reviewTypeList.addAll(ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_GATE));
	        	reviewTypeList.addAll(ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_MILESTONE));
	        	slTaskTypes.retainAll(reviewTypeList);
	        }*/	        
	        if("true".equalsIgnoreCase(isSummaryTask)){
	        	StringList reviewTypeList = new StringList(2);
	        	reviewTypeList.addAll(ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_TASK));
	        	reviewTypeList.addAll(ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_PHASE));
	        	slTaskTypes.retainAll(reviewTypeList);
	        }

	        int count = 0;
	        String language = context.getSession().getLanguage();
	        for (Iterator iterator = slTaskTypes.iterator(); iterator.hasNext();) {
	            String str = (String) iterator.next();
	            String i18nTaskTypeName = i18nNow.getTypeI18NString(slTaskTypes.get(count).toString(),language);
	            taskSubTypesIntNameList.add(i18nTaskTypeName);
	            count++;
	        }	        
	        Map returnMap = new HashMap();
	        returnMap.put("RangeValues", slTaskTypes);
	        returnMap.put("RangeDisplayValue", taskSubTypesIntNameList);

	        return returnMap;
	    }
	    
	    /**
	     * isAssigneeColumnCellsEditable: Where : In Structure Browser the root node is not editable.
	     * @param context the ENOVIA <code>Context</code> object
	     * @param args The arguments, it contains objectList and paramList maps
	     * @return StringList The StringList object containing Access Information
	     * @throws MatrixException
	     */

	    public StringList isTypeCellEditable(Context context, String[] args) throws MatrixException
	    {
	    	try {

	    		StringList isCellEditable = new StringList();
	    		Map programMap 		= (Map) JPO.unpackArgs(args);
	    		MapList objectList 	= (MapList) programMap.get("objectList");
	    		
	    		int size = objectList.size();
	    		String[] objIds = new String[size];

	    		for(int i=0;i<size;i++){
	    			Map objectMap = (Map)objectList.get(i);
	    			String id = (String)objectMap.get(SELECT_ID);
	    			objIds[i] = id;
	    		}
	    		
	    		StringList busSelect = new StringList(2);
    		busSelect.addElement(SELECT_IS_TASK_MANAGEMENT);
	    		busSelect.addElement(SELECT_CURRENT);

	    		MapList taskInfoList = ProgramCentralUtil.getObjectDetails(context, objIds,busSelect);
	    		for(int i=0;i<size;i++){
	    			Map objectMap = (Map)taskInfoList.get(i);
    			String isTaskManagement = (String)objectMap.get(SELECT_IS_TASK_MANAGEMENT);
	    			String taskSatte 		= (String)objectMap.get(SELECT_CURRENT);

	    			if("true".equalsIgnoreCase(isTaskManagement) && 
	    					(ProgramCentralConstants.STATE_PROJECT_TASK_CREATE.equalsIgnoreCase(taskSatte)
	    							|| ProgramCentralConstants.STATE_PROJECT_TASK_ASSIGN.equalsIgnoreCase(taskSatte))){
	    				isCellEditable.add("true");
	    			}else{
	    				isCellEditable.add("false");
	    			}
	    		}

	    		return isCellEditable;
	 
	    	} catch (Exception exp) {
	    		exp.printStackTrace();
	    		throw new MatrixException();
	    	}
	    }
		
		/**
	     * Check whether summary task cell editable or not.
	     * @param context the ENOVIA <code>Context</code> object.
	     * @param args request arguments
	     * @return A list of edit access settings for column in Project WBS.
	     * @throws MatrixException if operation fails.
	     */
	    public StringList isSummaryTaskCellEditable(Context context, String[] args) throws MatrixException
	    {
	    	try{
	    		StringList isCellEditable = new StringList();
	    		Map programMap = (HashMap) JPO.unpackArgs(args);
	    		MapList objectList = (MapList) programMap.get("objectList");
	    		int size = objectList.size();
	    		String[] objIds = new String[size];

	    		for(int i=0;i<size;i++){
	    			Map objectMap = (Map)objectList.get(i);
	    			String id = (String)objectMap.get(SELECT_ID);
	    			objIds[i] = id;
	    		}
	    		
    		StringList busSel = new StringList();
    		busSel.add("from[" + RELATIONSHIP_SUBTASK + "]");
    		MapList taskInfoList = ProgramCentralUtil.getObjectDetails(context, objIds, busSel);
	    		for(int i=0;i<size;i++){
	    			Map objectMap = (Map)taskInfoList.get(i);
    			String isSummasryTask = (String)objectMap.get("from[" + RELATIONSHIP_SUBTASK + "]");
    			if(isSummasryTask.equalsIgnoreCase("true")){
    				isCellEditable.add("false");
    			}else{
    				isCellEditable.add("true");
    			}
    		}
    		return isCellEditable;
	    			
    	}catch(Exception e){
    		throw new MatrixException(e);
    	}
    }
    
    /**
	     * Disable to update the duration of "Project Review" objects.
	     * @param context
	     * @param args
	     * @return
	     * @throws MatrixException
	     */
	    public StringList isDurationCellEditable(Context context, String[] args) throws MatrixException
	    {
	    	try{
	    		StringList isCellEditable = new StringList();
	    		Map programMap = (HashMap) JPO.unpackArgs(args);
	    		MapList objectList = (MapList) programMap.get("objectList");
	    		int size = objectList.size();
	    		String[] objIds = new String[size];

	    		for(int i=0;i<size;i++){
	    			Map objectMap = (Map)objectList.get(i);
	    			String id = (String)objectMap.get(SELECT_ID);
	    			objIds[i] = id;
	    		}
	    		StringList busSel = new StringList(2);
	    		busSel.add(ProgramCentralConstants.SELECT_SUMMARY);
	    		busSel.add(SELECT_POLICY);

	    		MapList taskInfoList = ProgramCentralUtil.getObjectDetails(context, objIds, busSel);
	    		
	    		for(int i=0;i<size;i++){
	    			Map objectMap = (Map)taskInfoList.get(i);
	    			String isSummasryTask = (String)objectMap.get(ProgramCentralConstants.SELECT_SUMMARY);
	    			if(isSummasryTask.equalsIgnoreCase("true")){
	    				isCellEditable.add("false");
	    			}else{
	    				String policy = (String)objectMap.get(SELECT_POLICY);
	    				if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(policy)){
	    				isCellEditable.add("false");
	    			}else{
	    				isCellEditable.add("true");
	    			}
	    		}
	    		}
	    		return isCellEditable;

	    	}catch(Exception e){
	    		throw new MatrixException(e);
	    	}
	    }

	    /**
     * Check whether owner cell editable or not.
     * @param context the ENOVIA <code>Context</code> object.
     * @param args request arguments
     * @return A list of edit access settings for column in Project WBS.
     * @throws MatrixException if operation fails.
     */
    public StringList isOwnerCellEditable(Context context, String[] args) throws MatrixException
    {
    	try{
    		StringList isCellEditable = new StringList();
    		Map programMap = (HashMap) JPO.unpackArgs(args);
    		MapList objectList = (MapList) programMap.get("objectList");
    		int size = objectList.size();
    		String[] objIds = new String[size];
    		for(int i=0;i<size;i++){
    			Map objectMap = (Map)objectList.get(i);
    			String id = (String)objectMap.get(SELECT_ID);
    			objIds[i] = id;
    		}
    		
    		String hasAccessOnProject  = EMPTY_STRING;
    		String loggedInUser = context.getUser();
    		
    		StringList busSel = new StringList();
    		busSel.add(SELECT_OWNER);
    		busSel.add(SELECT_CURRENT);
    		busSel.add("to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current.access[modify]");
    		busSel.add("to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current.access[read]");
    		MapList taskInfoList = DomainObject.getInfo(context, objIds, busSel);
    		
    		for(int i=0;i<size;i++){
    			Map objectMap = (Map)taskInfoList.get(i);
    			String hasModifyAccessOnProject = (String)objectMap.get("to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current.access[modify]");
    			String hasReadAccessOnProject = (String)objectMap.get("to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current.access[read]");
    			boolean isTaskOwner = loggedInUser.equalsIgnoreCase((String)objectMap.get(SELECT_OWNER));
    			String taskState = (String)objectMap.get(SELECT_CURRENT);
    			boolean isTaskComplete = (ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equals(taskState));

    			/* Allow owner cell editable if
    			1. Task is not in Complete state and user has modify access on project(i.e. Project Lead in project).
    			2. Task is not in Complete state and user is Project member and task owner(i.e. Project Lead in project).
    			*/
    			boolean isEditable = (!isTaskComplete && ProgramCentralUtil.isNotNullString(hasModifyAccessOnProject) && hasModifyAccessOnProject.equalsIgnoreCase("true")) ||
    					(!isTaskComplete && ProgramCentralUtil.isNotNullString(hasReadAccessOnProject) && hasReadAccessOnProject.equalsIgnoreCase("true") && isTaskOwner);
    			
    			if(isEditable){
    				isCellEditable.add("true");
    			}else{
    				isCellEditable.add("false");
    			}
    		}
    		
	    		return isCellEditable;

	    	}catch(Exception e){
	    		throw new MatrixException(e);
	    	}
	    }
    
    /**
	     * Trigger : Propagating task information to parent.
	     * @param context - The ENOVIA <code>Context</code> object.
	     * @param args - Holds information related objects
	     * @return - 0 or 1. 
	     * @throws Exception - If operation fails.
	     */
	    public int triggerPropagateTaskInfoToParent(Context context, String[]args)throws Exception{
	    	
	    	try{
	    		String parentObjId  = args[0];
	    		String childObjId 	= args[1];
	    		
	    		Task task = new Task(parentObjId);
	    		
	    		StringList busSelects = new StringList(2);
	        	busSelects.add(SELECT_CURRENT);
	            busSelects.add(SELECT_STATES);
	            busSelects.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
	    		
	            MapList taskList 	= Task.getTasks(context, task, 1, busSelects, null);
	        	int taskSize 		= taskList.size();
	        	
	        	if(taskSize == 1){
	        		Map parentInfo 				= task.getInfo(context, busSelects);
	        		String isProjectTemplate 			= (String)parentInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
	        		if("true".equalsIgnoreCase(isProjectTemplate)){
	        			return 0;
	        		}
	        		String parentState 			= (String)parentInfo.get(SELECT_CURRENT);
	        		StringList parentStateList 	= (StringList) parentInfo.get(SELECT_STATES);
	        		int  parentPos 				= parentStateList.indexOf(parentState);
	        		
	        		Map childTaskMap 			= (Map) taskList.get(0);
	        		String childState 			= (String) childTaskMap.get(SELECT_CURRENT);
	                StringList childTaskStateList = (StringList) childTaskMap.get(SELECT_STATES);
	                int childTasktPosition 		= childTaskStateList.indexOf(childState);
	    			
	    			if(parentPos > childTasktPosition){
	    				task.setState(context, (String)parentStateList.get(childTasktPosition));
	        		}
	        	}
	    		
	    		return 0;
	    		
	    	}catch(Exception e){
	    		return 1;
	    	}
	    }
}
