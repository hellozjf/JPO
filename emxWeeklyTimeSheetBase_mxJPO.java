import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.Role;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import com.matrixone.apps.program.WeeklyTimesheet;
import com.matrixone.apps.program.YearConfig;
import com.matrixone.apps.program.fiscal.CalendarType;
import com.matrixone.apps.program.fiscal.Helper;
import com.matrixone.apps.program.fiscal.Interval;
import com.matrixone.apps.program.fiscal.IntervalType;


public class emxWeeklyTimeSheetBase_mxJPO extends emxDomainObject_mxJPO {
	/** relationship "has Effort". */	
	public static final String RELATIONSHIP_HAS_EFFORTS = 
			PropertyUtil.getSchemaProperty("relationship_hasEfforts");
	/** attribute "Week Ending Date". */	
	protected static final String ATTRIBUTE_WEEK_ENDING_DATE = PropertyUtil.getSchemaProperty("attribute_WeekEndingDate");
	/** relationship "Effort". */		
	public static final String RELATIONSHIP_EFFORTS = 
			PropertyUtil.getSchemaProperty("relationship_Effort");
	/** relationship "Approver Context". */		
	public static final String RELATIONSHIP_APPROVER_CONTEXT = 
			PropertyUtil.getSchemaProperty("relationship_ApproverContext");
	/** value of attribute "Week Ending Date". */
	protected static final String SELECT_ATTRIBUTE_END_DATE = "attribute["+ATTRIBUTE_WEEK_ENDING_DATE+"].value";
	/** value of attribute "Week Ending Date". */
	protected static final String SELECT_ATTRIBUTE_ENDING_DATE = "attribute["+ATTRIBUTE_WEEK_ENDING_DATE+"].value";
	/** attribute "Project Access". */
	protected static final String SELECT_ATTRIBUTE_PROJECT_ACCESS = "attribute["+ATTRIBUTE_PROJECT_ACCESS+"]";
	/** attribute "Project Role". */		
	protected static final String SELECT_ATTRIBUTE_PROJECT_ROLE = "attribute["+ATTRIBUTE_PROJECT_ROLE+"]";
	/** timesheet object state "Exists". */	
	protected static final String STATE_WEEKLY_TIMESHEET_EXISTS = "Exists";
	/** timesheet object state "Approved". */	
	protected static final String STATE_WEEKLY_TIMESHEET_APPROVED = "Approved";
	/** timesheet object state "Rejected". */	
	protected static final String STATE_WEEKLY_TIMESHEET_REJECTED = "Rejected";
	/** timesheet object state "Submit". */	
	protected static final String STATE_WEEKLY_TIMESHEET_SUBMIT = "Submit";
	/** type "Phase". */	
	public static final String TYPE_PHASE = 
			PropertyUtil.getSchemaProperty("type_Phase" );
	public static final String TYPE_GATE = 
			PropertyUtil.getSchemaProperty("type_Gate" );
	/** type "Milestone". */	
	public static final String TYPE_MILESTONE = 
			PropertyUtil.getSchemaProperty("type_Milestone" );
	/** attribute "Total Effort". */	
	public static final String ATTRIBUTE_TOTAL_EFFORT = 
			PropertyUtil.getSchemaProperty("attribute_TotalEffort");  
	/** value for effort parent task id. */		
	private static final Object SELECT_EFFORT_PARENT_TASK_ID = "to[" + RELATIONSHIP_HAS_EFFORTS + "].from.id";
	/** value for efforts total effort. */
	private static final Object SELECT_TASK_TO_EFFORT_TOTAL_EFFORT = "from["+RELATIONSHIP_HAS_EFFORTS+"].to.attribute["+
			ATTRIBUTE_TOTAL_EFFORT+"].value";
	/** value for effort originator. */
	private static final Object SELECT_TASK_TO_EFFORT_ORIGINATOR = "from["+RELATIONSHIP_HAS_EFFORTS+"].to.attribute["+
			DomainConstants.ATTRIBUTE_ORIGINATOR+"].value";
	/** value for effort week ending data. */
	private static final Object SELECT_TASK_TO_EFFORT_WEEK_END_DATE = "from["+RELATIONSHIP_HAS_EFFORTS+"].to.attribute["+
			ATTRIBUTE_WEEK_ENDING_DATE+"].value";
	/** value for task parent id through relationship subtask. */
	private static final Object SELECT_TASK_PARENT_ID = "to[" + RELATIONSHIP_SUBTASK + "].from.id";
	/** value for task parent type through relationship subtask. */
	private static final Object SELECT_PARENT_TYPE = "to[" + RELATIONSHIP_SUBTASK + "].from.type";
	/** used to check parent type is project. */
	private static final Object SELECT_IS_PARENT_TYPE_KINDOF_PROJECT = "to[" + RELATIONSHIP_SUBTASK + "].from.type.kindof[" + TYPE_PROJECT_SPACE + "]";
	/** used to check parent type is phase. */
	private static final Object SELECT_IS_PARENT_TYPE_KINDOF_PHASE = "to[" + RELATIONSHIP_SUBTASK + "].from.type.kindof[" + TYPE_PHASE + "]";
	/** value for Effort state through relationship subtask. */
	private static final Object SELECT_EFFORT_CURRENT_STATE = "from[" + RELATIONSHIP_HAS_EFFORTS + "].to.current";
	/** role "External Project Lead". */
	public static final String ROLE_EXTERNAL_PROJECT_LEAD =
			PropertyUtil.getSchemaProperty( "role_ExternalProjectLead" );
	/** role "Project Owner". */
	public static final String ROLE_PROJECT_OWNER = "Project Owner";	
	/** policy "Person". */	
	protected static final String POLICY_PERSON =
			PropertyUtil.getSchemaProperty( "policy_Person" );
	/** attribute "Approver Comments". */
	public static final String ATTRIBUTE_APPROVER_COMMENTS = 
			PropertyUtil.getSchemaProperty("attribute_ApproverComments");
	/** attribute "Effort Comments". */	
	public static final String ATTRIBUTE_EFFORT_COMMENTS = 
			PropertyUtil.getSchemaProperty("attribute_EffortComments");
	/** attribute "User Comments". */	
	public static final String ATTRIBUTE_USER_COMMENTS = 
			PropertyUtil.getSchemaProperty("attribute_UserComments");
	/** type "Weekly Timesheet". */	
	public static final String TYPE_WEEKLY_TIMESHEET = 
			PropertyUtil.getSchemaProperty("type_WeeklyTimesheet");
	/** relationship "Approver Context Project". */	
	public static final String RELATIONSHIP_APPROVER_CONTEXT_PROJECT = 
			PropertyUtil.getSchemaProperty("relationship_ApproverContextProject");
	/** relationship "Approver Context Resource Pool". */	
	public static final String RELATIONSHIP_APPROVER_CONTEXT_RESOURCE_POOL = 
			PropertyUtil.getSchemaProperty("relationship_ApproverContextResourcePool");
	/** relationship "Weekly Timesheet". */	
	public static final String RELATIONSHIP_WEEKLY_TIMESHEET = 
			PropertyUtil.getSchemaProperty("relationship_WeeklyTimesheet");
	/** policy "Weekly Timesheet". */	
	public static final String POLICY_WEEKLY_TIMESHEET = 
			PropertyUtil.getSchemaProperty("policy_WeeklyTimesheet");
	//TODO these variable should be removed once thye are available in DomainConstant.java
	/** attribute "Sunday". */		
	public static final String ATTRIBUTE_SUNDAY = 
			PropertyUtil.getSchemaProperty("attribute_Sunday"); 	
	/** attribute "Monday". */		
	public static final String ATTRIBUTE_MONDAY = 
			PropertyUtil.getSchemaProperty("attribute_Monday"); 
	/** attribute "Tuesday". */		
	public static final String ATTRIBUTE_TUESDAY = 
			PropertyUtil.getSchemaProperty("attribute_Tuesday"); 
	/** attribute "Wednesday". */		
	public static final String ATTRIBUTE_WEDNESDAY = 
			PropertyUtil.getSchemaProperty("attribute_Wednesday"); 
	/** attribute "Thursday". */		
	public static final String ATTRIBUTE_THURSDAY = 
			PropertyUtil.getSchemaProperty("attribute_Thursday"); 
	/** attribute "Friday". */		
	public static final String ATTRIBUTE_FRIDAY = 
			PropertyUtil.getSchemaProperty("attribute_Friday"); 
	/** attribute "Saturday". */		
	public static final String ATTRIBUTE_SATURDAY = 
			PropertyUtil.getSchemaProperty("attribute_Saturday"); 
	/** type "Effort". */	
	public static final String TYPE_EFFORT = 
			PropertyUtil.getSchemaProperty("type_Effort" );
	/** policy "Effort". */	
	public static final String POLICY_EFFORT = 
			PropertyUtil.getSchemaProperty("policy_Effort" );
	/** attribute "Remaining Effort". */	
	public static final String ATTRIBUTE_REMAINING_EFFORT = 
			PropertyUtil.getSchemaProperty("attribute_RemainingEffort");  	
	//public final static String ATTRIBUTE_YEAR = PropertyUtil.getSchemaProperty("attribute_Year");
	/** attribute "year". */	
	public final static String ATTRIBUTE_YEAR = "year";
	/** attribute "timeframe". */	
	public final static String ATTRIBUTE_TIMEFRAME = "timeframe";
	/** attribute "timeframe display". */	
	public final static String ATTRIBUTE_TIMEFRAME_DISPLAY = "timeframedisplay";
	/** attribute "Effort Submission". */	
	public static final String ATTRIBUTE_EFFORT_SUBMISSION = 
			PropertyUtil.getSchemaProperty("attribute_EffortSubmission");  	
	//public static final String ATTRIBUTE_APPROVER_SELECTION = 
	//PropertyUtil.getSchemaProperty("attribute_ApproverSelection");
	/** task object state "Assign". */	
	protected static final String STATE_TASK_ASSIGN = "Assign";
	/** task object state "Create". */
	protected static final String STATE_TASK_CREATE = "Create";
	/** effort attribute "Remaining Effort". */		
	protected static final String SELECT_ATTRIBUTE_REMAINING_EFFORT = "attribute["+ATTRIBUTE_REMAINING_EFFORT+"].value";
	/** effort attribute "Total Effort". */
	protected static final String SELECT_ATTRIBUTE_TOTAL_EFFORT = "attribute["+ATTRIBUTE_TOTAL_EFFORT+"]";
	/** delimiter */
	protected static final String delimiter="|";
	/** value as "Calendar" for filters. */	
	protected static final String SELECT_CALENDAR = "Calendar";
	/** value as "Effort" for filters. */		
	protected static final String SELECT_EFFORT = "Effort";

	protected static final String SELECT_TASK_ID = "to[" + RELATIONSHIP_HAS_EFFORTS + "].from.id";
	/** state "Create". */
	protected static final String STATE_CREATE = "Create";
	/** project state "All". */
	protected static final String STATE_PROJECT_ALL = "All";
	/** project state "Create". */
	protected static final String STATE_PROJECT_CREATE = "Create";
	/** project state "Assign". */	
	protected static final String STATE_PROJECT_ASSIGN = "Assign";
	/** project state "Active". */
	protected static final String STATE_PROJECT_ACTIVE = "Active";
	/** project state "Review". */
	protected static final String STATE_PROJECT_REVIEW = "Review";
	/** project state "Complete". */
	protected static final String STATE_PROJECT_COMPLETE = "Complete";
	/** project state "Archive". */
	protected static final String STATE_PROJECT_ARCHIVE = "Archive";
	/** value for "Project Lead". */
	protected static final String SEELCT_APPROVER_PROJECT_LEAD = "P";
	/** value for "Resource Manager". */	
	protected static final String SEELCT_APPROVER_RESOURCE_MANAGER = "R";
	/** value as "Fiscal" for filters. */	
	protected static final String SELECT_FISCAL = "Fiscal";
	/** value as "Monthly" for filters. */	
	protected static final String SELECT_MONTHLY = "Monthly";
	/** value as "Weekly" for filters. */	
	protected static final String SELECT_WEEKLY = "Weekly";
	/** value as "Yearly" for filters. */	
	protected static final String SELECT_YEARLY = "Yearly";
	/** value as "Quarterly" for filters. */	
	protected static final String SELECT_QUARTERLY = "Quarterly";
	/** value as "year" for date. */	
	protected static final String SELECT_YEAR = "year";
	/** value as "week" for date. */	
	protected static final String SELECT_WEEK = "week";
	/** value as "month" for date. */	
	protected static final String SELECT_MONTH = "month";


	/**
	 * Constructor
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @throws Exception
	 *             if the operation fails
	 */
	public emxWeeklyTimeSheetBase_mxJPO(Context context, String[] args)
			throws Exception {
		super(context, args);
	}

	/**
	 * Main entry point.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception
	 *             if the operation fails
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		return 0;
	}

	/**
	 * This method used to get Weekly Timesheet objects.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @param args
	 *            The command line arguments
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWeeklyTimeSheets(Context context, String[] args)
			throws Exception {
		StringList strLstObjSelects = new StringList();
		StringList strLstRelSelects = new StringList();
		Person person = new Person(PersonUtil
				.getPersonObjectID(context));
		return getWeeklyTimeSheets(context, strLstObjSelects, strLstRelSelects,
				person, null, null, null);
	}
	/**
	 * This method used to get Weekly Timesheet objects.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @param args
	 *            The command line arguments
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList displayTimesheetTasks(Context context, String[] args)
			throws Exception {
		MapList timesheetTasks = new MapList();
		try{
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			String strPersonId = PersonUtil.getPersonObjectID(context);
			String objectId = (String) paramMap.get("objectId");
			DomainObject domObj = DomainObject.newInstance(context, objectId);
			String shasEffort_relationship = PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
			MapList mlEfforts = new MapList();
			MapList mlModifiedEffortsList = new MapList();
			StringList objectSelects = new StringList();
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.add("to["+RELATIONSHIP_COMPANY_PROJECT+"].from.id");
			objectSelects.add("attribute["+ATTRIBUTE_EFFORT_SUBMISSION+"]");
			String strPhase = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Type.Phase"); 
			String strTask = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Type.Task");
			if(domObj.isKindOf(context,
					TYPE_WEEKLY_TIMESHEET)) {
				WeeklyTimesheet weeklyTimesheet = new WeeklyTimesheet(objectId);
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);							
				try {
					mlEfforts = weeklyTimesheet.getEfforts(context, null, null, null);
				} finally {			
					ContextUtil.popContext(context);			
				}	
				int size = mlEfforts.size();
				for(int i=0; i<size; i++)
				{
					Map mEffort = new HashMap();
					mEffort = (Map)mlEfforts.get(i);
					String strTaskId = (String)mEffort.get("to["+shasEffort_relationship+"].from.id");
					if(ProgramCentralUtil.isNotNullString(strTaskId))
					{	
						mlModifiedEffortsList.add(mEffort);
					}
				}

				int modSize = mlModifiedEffortsList.size();
				for(int i=0; i<modSize; i++)
				{
					Map mEffort = new HashMap();
					mEffort = (Map)mlModifiedEffortsList.get(i);
					String strTaskId = (String)mEffort.get("to["+shasEffort_relationship+"].from.id");
					String strTaskType = (String)mEffort.get("to["+shasEffort_relationship+"].from.type");
					String strEffortId = (String)mEffort.get(DomainConstants.SELECT_ID);

					Task task = new Task(strTaskId);
					Map mp = task.getProject(context, objectSelects,true);
					String projectId = (String)mp.get(DomainConstants.SELECT_ID);
					String strType = "";
					mp.put("level", "1");
					boolean isSameProject = false;
					int index=0;
					if(timesheetTasks.size()>0){
						for(int j=0;j<timesheetTasks.size();j++){
							Map mpProject = (Map)timesheetTasks.get(j);
							String strProjectId = (String)mpProject.get(DomainConstants.SELECT_ID);
							if(strProjectId.equalsIgnoreCase(projectId)){
								index = j;
								isSameProject = true;
								break;
							}
						}
					}
					if(!isSameProject)
					{
						Map calculatedMap = getTotalEffortForProject(context,mp,mlModifiedEffortsList);
						calculatedMap.put("selection", "multiple");
						timesheetTasks.add(calculatedMap);
					}
					mEffort.put("level", "2");
					mEffort.put("hasChildren", "false");
					mEffort.put("selection", "multiple");
					if(isSameProject)
						timesheetTasks.add(index+1,mEffort);
					else
						timesheetTasks.add(mEffort);
				}

				if(modSize > 0)
				{
					Map totalMap = new HashMap();
					totalMap.put(SELECT_ID,strPersonId);
					totalMap.put("isTotalRow", "true");
					Map calTotalMap = getTotalEffort(context,totalMap,mlModifiedEffortsList);
					calTotalMap.put("hasChildren", "false");
					timesheetTasks.add(calTotalMap);
				}
			} else if(domObj.isKindOf(context,
					TYPE_PROJECT_SPACE)) {
				return expandProjects(context,args);				
			}
			HashMap hmTemp = new HashMap();
			hmTemp.put("expandMultiLevelsJPO","true");  
			timesheetTasks.add(hmTemp);
		}catch(Exception e){
			throw new Exception(e.getMessage());
		}
		return timesheetTasks;
	}

	/**
	 * This method used to get sum of all weekdays and total effort for each
	 * effort within a project.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param Map
	 *            projectMap - contains project map.
	 * @param MapList
	 *            efforts - holds the map of effort object.
	 * @return Map - containing calculated sum of weekdays and total effort for
	 *         a project
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x.
	 * @author vf2
	 * 
	 */	
	public Map getTotalEffortForProject(Context context,Map projectMap,MapList efforts) throws Exception {
		StringList objectSelects = new StringList();
		objectSelects.add(DomainConstants.SELECT_ID);
		String shasEffort_relationship = PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		StringList strAttributeList = new StringList();
		strAttributeList.add(ATTRIBUTE_SUNDAY);
		strAttributeList.add(ATTRIBUTE_MONDAY);
		strAttributeList.add(ATTRIBUTE_TUESDAY);
		strAttributeList.add(ATTRIBUTE_WEDNESDAY);
		strAttributeList.add(ATTRIBUTE_THURSDAY);
		strAttributeList.add(ATTRIBUTE_FRIDAY);
		strAttributeList.add(ATTRIBUTE_SATURDAY);	
		strAttributeList.add(ATTRIBUTE_TOTAL_EFFORT);	

		for(int idx=0;idx<efforts.size();idx++){
			Map Effort = new HashMap();
			Effort = (Map)efforts.get(idx);
			String strTaskId = (String)Effort.get("to["+shasEffort_relationship+"].from.id");
			Task task = new Task(strTaskId);
			Map mp = task.getProject(context, objectSelects);
			String projectId = (String)mp.get(DomainConstants.SELECT_ID);

			if(projectMap.get(DomainConstants.SELECT_ID).equals(projectId)){
				String effortId = (String)Effort.get(DomainConstants.SELECT_ID);
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				DomainObject effortDomainObject = DomainObject.newInstance(context, effortId);				
				AttributeList effortAttributeList = null;
				try {
					effortAttributeList =  effortDomainObject.getAttributeValues(context, strAttributeList);
				} finally {			
					ContextUtil.popContext(context);			
				}				
				if(projectMap.get("isEffort")== null || "".equals(projectMap.get("isEffort"))) {
					for(int i=0; i<effortAttributeList.size(); i++) {
						if(i == 7) {
							projectMap.put("attribute["+strAttributeList.get(i)+"]",Task.parseToDouble(((Attribute)effortAttributeList.get(i)).getValue()));
						} else {
							projectMap.put("attribute["+strAttributeList.get(i)+"]",Task.parseToDouble(((Attribute)effortAttributeList.get(i)).getValue()));
						}
					}
					projectMap.put("isEffort","true");
				} else {
					for(int i=0; i<effortAttributeList.size(); i++) {
						if(i == 7) {
							projectMap.put("attribute["+strAttributeList.get(i)+"]",Task.parseToDouble(((Attribute)effortAttributeList.get(i)).getValue())
									+ (Double)projectMap.get("attribute["+strAttributeList.get(i)+"]"));
						} else {
							projectMap.put("attribute["+strAttributeList.get(i)+"]",Task.parseToDouble(((Attribute)effortAttributeList.get(i)).getValue()) 
									+ (Double)projectMap.get("attribute["+strAttributeList.get(i)+"]"));
						}
					}							
				}								
			}				
		}
		return projectMap;
	}	

	/**
	 * This method used to get Weekly Timesheet objects details.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the timesheet id.
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x.
	 * @author vf2
	 * 
	 */		
	public MapList displayTimesheetTasks(Context context,String strTimesheetId)
			throws Exception {
		String[] arrJPOArguments = new String[1];
		HashMap programMap = new HashMap();
		programMap.put("objectId", strTimesheetId);
		arrJPOArguments = JPO.packArgs(programMap);
		MapList mpTasks = (MapList)JPO.invoke(context,
				"emxWeeklyTimeSheetBase", null, "displayTimesheetTasks",
				arrJPOArguments, MapList.class);
		return mpTasks;
	}

	/**
	 * Returns list of timesheet to be approved by context user.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the timesheet id.
	 * @param String
	 *            containing the timesheet state.	          
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x.
	 * @author wpk
	 * 
	 */		
	public MapList getTimesheetsToApprove(Context context,String objectId,StringList strState)
			throws Exception {
		MapList timesheetsToApprove = new MapList();
		try{
			DomainObject domObjProject = DomainObject.newInstance(context, objectId);
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(DomainConstants.SELECT_REVISION);
			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
			objectSelects.addElement(SELECT_ATTRIBUTE_END_DATE);
			StringBuffer stbTypeName = new StringBuffer(50);
			stbTypeName.append(TYPE_WEEKLY_TIMESHEET);
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
			StringBuffer stbRelPattern = new StringBuffer(50);
			stbRelPattern.append(RELATIONSHIP_APPROVER_CONTEXT);
			StringBuffer stbObjWhere = new StringBuffer(50);
			if (strState != null
					&& !strState.equals("")
					&& !strState.equals("null"))
			{
				stbObjWhere.append("(");
				for(int i=0;i<strState.size();i++){
					String state = (String)strState.get(i);
					stbObjWhere.append("current == "+state);
					if(i<strState.size()-1){
						stbObjWhere.append(" || ");
					}
				}
				stbObjWhere.append(")");
			}
			timesheetsToApprove = domObjProject.getRelatedObjects(context,
					stbRelPattern.toString(), stbTypeName.toString(),objectSelects, relSelects,
					true, false, (short)1,stbObjWhere.toString(),"",0,null,null,null);

		}catch(Exception e){
			throw new Exception(e.getMessage());
		}
		return timesheetsToApprove;
	}
	/**
	 * This method is used to get list of timesheet filtered by person,state,
	 * startdate and enddate.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param StringList
	 *            strLstObjSelects List of extra object selectables
	 * @param StringList
	 *            strLstRelSelects List of extra relationship selectables
	 * @param Person
	 *            person - Person object whose timesheet needs to be searched
	 * @param StringList
	 *            strState - state of timesheet being searched
	 * @param Date
	 *            startDate - start date after which timesheets are created
	 * @param Date
	 *            startDate - end date before which timesheets are created
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 */
	private MapList getWeeklyTimeSheets(Context context,
			StringList strLstObjSelects, StringList strLstRelSelects,
			Person person, StringList strState, Date startDate, Date endDate)
					throws Exception {
		return WeeklyTimesheet.getWeeklyTimesheets(context, strLstObjSelects,
				strLstRelSelects, person, strState, startDate, endDate);
	}

	/**
	 * This method used to get Week number for each Weekly Timesheet objects.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - containing week number value as String.
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	public Vector getWeekNo(Context context, String[] args) throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		Vector vectorWeekNo = new Vector();
		MapList objectList = (MapList) paramMap.get("objectList");
		Iterator objectListIterator = objectList.iterator();
		String weekEndDate = EMPTY_STRING;
		Map mapObject = null;  
		Date weekEndingDate = null;
		while (objectListIterator.hasNext()) {
			mapObject = (Map) objectListIterator.next();
			weekEndDate = (String) mapObject.get(SELECT_ATTRIBUTE_ENDING_DATE);
			weekEndingDate = eMatrixDateFormat.getJavaDate(weekEndDate);
			String strWeekNo = getWeekNo(context,weekEndingDate);
			vectorWeekNo.add(new Integer(strWeekNo).toString());
		}
		return vectorWeekNo;
	}

	/**
	 * This method used to get Week number.
	 * 
	 * @param Date
	 *            weekEndDate - week ending date
	 * @return String - week number.
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	private String getWeekNo(Context context,Date weekEndDate) throws Exception {
		WeeklyTimesheet weeklyTimesheet = new WeeklyTimesheet();
		return weeklyTimesheet.getWeekNo(context,weekEndDate);
	}

	/**
	 * This method used to list Weekly Timesheet which are in Exist or Rejected
	 * state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTimesheetInProcess(Context context, String[] args)
			throws Exception {
		StringList strLstObjSelects = new StringList();
		StringList strLstRelSelects = new StringList();
		Person person = new Person(PersonUtil
				.getPersonObjectID(context));
		MapList timesheetList =  getWeeklyTimeSheets(context, strLstObjSelects, strLstRelSelects,
				person, null, null, null);
		MapList inProcessList = new MapList();
		for(int i=0;i<timesheetList.size();i++) {
			Map map = (Map)timesheetList.get(i);
			if(map.containsValue(STATE_WEEKLY_TIMESHEET_EXISTS) ||map.containsValue(STATE_WEEKLY_TIMESHEET_REJECTED)){
				inProcessList.add(map);
			}
		}
		return inProcessList;
	}

	/**
	 * This method used to list Weekly Timesheet which are in Submitted state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTimesheetSubmitted(Context context, String[] args)
			throws Exception {
		StringList strLstObjSelects = new StringList();
		StringList strLstRelSelects = new StringList();
		Person person = new Person(PersonUtil
				.getPersonObjectID(context));
		StringList strLstState = new StringList();
		strLstState.add(STATE_WEEKLY_TIMESHEET_SUBMIT);
		return getWeeklyTimeSheets(context, strLstObjSelects, strLstRelSelects,
				person, strLstState, null, null);
	}
	/**
	 * This method used to list Weekly Timesheet which are in Submitted state for approver.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSubmittedTimesheetForApprover(Context context, String[] args) throws Exception {
		
			MapList returnList=new MapList(); 
			MapList relatedEffort=new MapList();  
			StringList projectInfo =new StringList(); 
			projectInfo.add(SELECT_ID);
			projectInfo.add(SELECT_TYPE);
			StringList relatedEffortID=new StringList(); 
			MapList relatedEffort1=new MapList(); 
			MapList TimeSheetEffortId=new MapList(); 
			StringList strSelectable=new StringList(); 
			strSelectable.add(SELECT_ID); 
			strSelectable.add(SELECT_CURRENT); 
			String state = ProgramCentralConstants.STATE_EFFORT_SUBMIT;  
			String strWhere="current == \""+state+"\"";
			
			Person person = new Person(PersonUtil .getPersonObjectID(context)); 
			
			MapList memberProject = person.getRelatedObjects(context, 
											RELATIONSHIP_MEMBER, 
											TYPE_PROJECT_SPACE, 
											projectInfo, 
											null, 
											true, 
											false, 
											(short)1, 
											null, 
											"", 
											0, 
											null, 
											null, 
											null);
			
 
			for(int i=0;i<memberProject.size();i++) { 
			Map ProjectMap=(Map)memberProject.get(i); 
			String id=(String) ProjectMap.get(SELECT_ID); 
				if((TYPE_PROJECT_SPACE).equalsIgnoreCase((String)ProjectMap.get(SELECT_TYPE))) {
					DomainObject domj=DomainObject.newInstance(context, id); 
					relatedEffort1=domj.getRelatedObjects(context, 
							RELATIONSHIP_EFFORTS, 
							TYPE_EFFORT, 
							new StringList(SELECT_ID), 
							null, 
							false, 
							true, 
							(short)1, 
							null, 
							"", 
							0, 
							null, 
							null, 
							null); 
					relatedEffort.addAll(relatedEffort1);
					}
			} 
			
			for(int j=0;j<relatedEffort.size();j++) { 
			Map efforMap=(Map) relatedEffort.get(j); 
			String id=(String)efforMap.get(SELECT_ID); 
			relatedEffortID.add(id); 
			} 
			
		StringList strLstState = new StringList();
		strLstState.add(STATE_WEEKLY_TIMESHEET_SUBMIT);
			MapList weeklytimeSheet=getWeeklyTimesheetsToApprove(context,person, strLstState); 
			StringList strList=new StringList();  
			strList.add(ProgramCentralConstants.STATE_EFFORT_SUBMIT);
			MapList effortList = new MapList(); 
			
			if(relatedEffort.size()>0) { 
				for(int k=0;k<weeklytimeSheet.size();k++) { 
				int count=0; 
				Map WeekltTimesheetMap=(Map)weeklytimeSheet.get(k); 
				String id=(String)WeekltTimesheetMap.get(SELECT_ID); 
				WeeklyTimesheet weeklyimtsheet = new WeeklyTimesheet(id); 
				effortList=weeklyimtsheet.getEfforts(context, strList, null, null); 
				
					for(int j=0;j<effortList.size();j++) {
					TimeSheetEffortId=new MapList(); 
					Map effortMap=(Map)effortList.get(j); 
					String Effortid=(String)effortMap.get(SELECT_ID); 
						if(relatedEffortID.contains(Effortid)) { 
							count++; 
						} 
					} 
				if(count>0) 
				returnList.add(WeekltTimesheetMap); 
				
				else { 
				for(int j=0;j<effortList.size();j++) { 
				Map effortMap=(Map)effortList.get(j); 
				String Effortid=(String)effortMap.get(SELECT_ID); 
				TimeSheetEffortId.remove(Effortid); 
						} 
					} 			
				} 
			} 
			return returnList; 
	}
	/**
	 * This method used to list Weekly Timesheet which are in Submitted state for approver.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getApprovedTimesheetForApprover(Context context, String[] args)
			throws Exception {
		StringList strLstObjSelects = new StringList();
		StringList strLstRelSelects = new StringList();
		Person person = new Person(PersonUtil
				.getPersonObjectID(context));
		StringList strLstState = new StringList();
		strLstState.add(STATE_WEEKLY_TIMESHEET_REJECTED);
		return getWeeklyTimesheetsToApprove(context,person, strLstState);
	}
	/**
	 * returns list of timesheet to be approved by context user
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param Person person -
	 *            Person object whose timesheet needs to be searched
	 * @return MapList - List containint details of timesheet to be 
	 * 					 approved in the form of Map
	 * @throws Exception 
	 * @exception Exception
	 *                if operation fails
	 * @since Program Central R210
	 * @throws Exception 
	 */
	public MapList getWeeklyTimesheetsToApprove (Context context, Person person,StringList strState) throws Exception{
		boolean isTimesheetApprover = false;
		MapList projecList = new MapList();
		MapList timesheetToApprove = new MapList();
		MapList tempTimesheetToApprove = new MapList();
		try{
			matrix.db.RoleList rl = Role.getRoles(context);
			String stateActive = PropertyUtil.getSchemaProperty(context, "policy",
					POLICY_PERSON, "state_Active" );
			//TODO this needs to be replaced with actual preference on project.
			String strApprover = ProgramCentralConstants.PROJECT_ROLE_PROJECT_LEAD;
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(DomainConstants.SELECT_REVISION);
			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
			String ROLE_EXTERNAL_PROJECT_LEAD = PropertyUtil.getSchemaProperty( context,"role_ExternalProjectLead" );
			StringBuffer stbTypeName = new StringBuffer(50);
			stbTypeName.append(TYPE_WEEKLY_TIMESHEET);
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
			relSelects.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
			StringBuffer stbRelPattern = new StringBuffer(50);
			stbRelPattern.append(RELATIONSHIP_WEEKLY_TIMESHEET);
			for(int i=0;i<rl.size();i++){
				Role role = (Role)rl.get(i);
				if((role.toString().equalsIgnoreCase(DomainConstants.ROLE_PROJECT_LEAD)) || 
						(role.toString().equalsIgnoreCase(DomainConstants.ROLE_RESOURCE_MANAGER)) ||
						(role.toString().equalsIgnoreCase(ROLE_EXTERNAL_PROJECT_LEAD))){
					isTimesheetApprover = true;
				}
			}
			if(!isTimesheetApprover){
				return projecList;
			}
			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"] ~~ \""+DomainConstants.ROLE_PROJECT_LEAD+"\"");
			sbWhere.append(" || ");
			sbWhere.append("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"] ~~ \""+ROLE_EXTERNAL_PROJECT_LEAD+"\"");
			sbWhere.append(" || ");
			sbWhere.append("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ACCESS+"] ~~ \""+ROLE_PROJECT_OWNER+"\"");
			projecList = ProjectSpace.getProjects(context, person, objectSelects, relSelects, null,sbWhere.toString());
			stbRelPattern = new StringBuffer(50);
			stbRelPattern.append(RELATIONSHIP_RESOURCE_MANAGER);
			stbTypeName = new StringBuffer(50);
			stbTypeName.append(TYPE_ORGANIZATION);
			StringBuffer stbObjWhere = new StringBuffer(50);
			stbObjWhere.append("current == "+stateActive);
			relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
			MapList orgList = person.getRelatedObjects(context,
					stbRelPattern.toString(), stbTypeName.toString(),objectSelects, relSelects,
					true, false, (short)1,stbObjWhere.toString(),"",0,null,null,null);
			projecList.addAll(orgList);
			for(int j=0;j<projecList.size();j++){
				Map mpProject = (Map)projecList.get(j);
				String strProjectId = (String)mpProject.get(DomainConstants.SELECT_ID);
				MapList mlTimesheets = getTimesheetsToApprove(context,strProjectId,strState);
				for(int k=0;k<mlTimesheets.size();k++){
					timesheetToApprove.add(mlTimesheets.get(k));
				}
			}
			StringList strList = new StringList();
			for(int m=0;m<timesheetToApprove.size();m++) {
				Map timesheetMap = (Map)timesheetToApprove.get(m);
				String strTimesheetId = (String)timesheetMap.get(DomainConstants.SELECT_ID);
				if(!strList.contains(strTimesheetId)){
					strList.add(strTimesheetId);
					tempTimesheetToApprove.add(timesheetMap);
				}
			}		
			timesheetToApprove = tempTimesheetToApprove;
		}catch(FrameworkException e){
			throw (new FrameworkException(e.getMessage()));
		}catch(MatrixException e){
			throw (new MatrixException(e.getMessage()));
		}
		return timesheetToApprove;
	}
	/**
	 * This method used to list Weekly Timesheet which are in Approved state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTimesheetApproved(Context context, String[] args)
			throws Exception {
		StringList strLstObjSelects = new StringList();
		StringList strLstRelSelects = new StringList();
		Person person = new Person(PersonUtil
				.getPersonObjectID(context));
		StringList strLstState = new StringList();
		strLstState.add(STATE_WEEKLY_TIMESHEET_APPROVED);
		return getWeeklyTimeSheets(context, strLstObjSelects, strLstRelSelects,
				person, strLstState, null, null);
	}

	/**
	 * This method used to list Weekly Timesheet which are in Rejected state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTimesheetRejected(Context context, String[] args)
			throws Exception {
		StringList strLstObjSelects = new StringList();
		StringList strLstRelSelects = new StringList();
		Person person = new Person(PersonUtil
				.getPersonObjectID(context));
		StringList strLstState = new StringList();
		strLstState.add(STATE_WEEKLY_TIMESHEET_REJECTED);
		return getWeeklyTimeSheets(context, strLstObjSelects, strLstRelSelects,
				person, strLstState, null, null);
	}

	/**
	 * This method used to check timesheet id in case of copy timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return boolean
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x            
	 * @author vf2  
	 * 
	 */		
	public boolean copyObjectID(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String copyObjectId = (String)programMap.get("copyObjectId");
		boolean isCopyObjectId = false;
		if(!"null".equals(copyObjectId) && null!= copyObjectId && !"".equals(copyObjectId)){	
			isCopyObjectId = true;
		}		
		return isCopyObjectId; 
	}    

	/**
	 * This method used to check whether already timesheet exist for current
	 * week.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return boolean
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 * 
	 */			
	public boolean isTimesheetExistForUser(Context context, String[] args) throws Exception {
		Person p = new Person(PersonUtil.getPersonObjectID(context));
		String strIsTimesheetExist = p.getInfo(context, "from["+RELATIONSHIP_WEEKLY_TIMESHEET+"]");
		if(strIsTimesheetExist.equalsIgnoreCase("true")){
			return true;
		}else{
			return false;
		}
	}  

	/**
	 * This method is used to show the status icon image.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the status icon value as String.
	 * @throws Exception
	 *             if the operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 */
	public Vector getStatusIcon(Context context, String[] args)
			throws Exception {
		Vector showIcon = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map objectMap = null;
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			int arrayCount = 0;
			while (objectListIterator.hasNext()) {
				objectMap = (Map) objectListIterator.next();
				objIdArr[arrayCount] = (String) objectMap
						.get(WeeklyTimesheet.SELECT_ID);
				arrayCount++;
			}
			MapList actionList = DomainObject.getInfo(context, objIdArr,
					new StringList(WeeklyTimesheet.SELECT_CURRENT));
			int actionListSize = 0;
			if (actionList != null) {
				actionListSize = actionList.size();
			}
			for (int i = 0; i < actionListSize; i++) {
				String statusGif = ProgramCentralConstants.EMPTY_STRING;
				objectMap = (Map) actionList.get(i);
				String timesheetStatus = (String) objectMap
						.get(WeeklyTimesheet.SELECT_CURRENT);
				if(timesheetStatus.equals(STATE_WEEKLY_TIMESHEET_SUBMIT)) {					
					statusGif = "<img src=\"images/iconStatusYellow.gif\" border=\"0\" />";					
				} else if(timesheetStatus.equals(STATE_WEEKLY_TIMESHEET_REJECTED)) {
					statusGif = "<img src=\"images/iconStatusRed.gif\" border=\"0\" />";		                    
				} else if(timesheetStatus.equals(STATE_WEEKLY_TIMESHEET_APPROVED)) {
					statusGif = "<img src=\"images/iconStatusGreen.gif\" border=\"0\" />";		                    
				}
				showIcon.addElement(statusGif);
			}
		} catch (Exception ex) {
			throw new MatrixException(ex);
		} finally {
			return showIcon;
		}
	}

	/**
	 * This method is used to get name of timesheet object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: requestMap - Contains a
	 *            Map with object name.
	 * @return Vector containing the name of timesheet object as string.
	 * @throws Exception
	 *             if the operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 */     
	public String getTimesheetName(Context context, String[] args) throws MatrixException {
		StringBuffer sbTimesheetLink = null;
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);   	
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String copyObjectId = (String)requestMap.get("copyObjectId"); 
			String name = EMPTY_STRING;
			if(!"null".equals(copyObjectId) && null!= copyObjectId && !"".equals(copyObjectId)){
				DomainObject domainObject = DomainObject.newInstance(context, copyObjectId);
				name = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
				String imageStr = "../common/images/iconColHeadTime.gif";
				sbTimesheetLink = new StringBuffer();
				sbTimesheetLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
				sbTimesheetLink.append("&nbsp;&nbsp;");
				sbTimesheetLink.append("<a href=\"JavaScript:showModalDialog('../common/emxPortal.jsp?");
				sbTimesheetLink.append("portal=PMCWeeklyTimeSheet&showPageHeader=false");
				sbTimesheetLink.append("','1050','800','false','popup')\">");                    
				sbTimesheetLink.append(name);
				sbTimesheetLink.append("</a>");    		    		
			} 
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}   	      	
		return sbTimesheetLink.toString();
	}

	/**
	 * This method is used to get name of timesheet owner.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the names of timesheet owner.
	 * @throws Exception
	 *             if the operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 */    
	public Vector getTimeSheetOwnerName(Context context, String[] args)
			throws MatrixException {
		Vector timeSheetOwnerName = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map objectMap = null;
			Iterator objectListIterator = objectList.iterator();
			String objectId = EMPTY_STRING;
			String strPerosnId = ""; 
			while (objectListIterator.hasNext()) {
				objectMap = (Map) objectListIterator.next();
				objectId = (String) objectMap
						.get(DomainConstants.SELECT_ID);
				DomainObject dobj = DomainObject.newInstance(context,objectId);
				strPerosnId = dobj.getInfo(context, "to["+RELATIONSHIP_WEEKLY_TIMESHEET+"].from.id");
				String imageStr = "../common/images/iconSmallPerson.gif";
				StringBuffer sbownerLink = new StringBuffer();
				sbownerLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
				sbownerLink.append("<a href=\"JavaScript:showModalDialog('../common/emxTree.jsp?objectId=");
				sbownerLink.append(XSSUtil.encodeForURL(context,strPerosnId));
				sbownerLink.append("','700','600','false','popup')\">");                    
				sbownerLink.append(XSSUtil.encodeForHTML(context,(PersonUtil.getFullName(context,dobj.getOwner(context).toString()))));
				sbownerLink.append("</a>");
				timeSheetOwnerName.addElement(sbownerLink.toString());
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return timeSheetOwnerName;
	}	

	/**
	 * This method is used to check state of timesheet object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: args[0] - Contains a
	 *            timesheet id and args[0] - Contains the current state of
	 *            timesheet object.
	 * @return integer value 0 or 1
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */  	
	public int triggercheckCurrentState(Context context,String[] args) throws Exception {
		try{
			String objectId  = args[0];
			String state = args[1];
			DomainObject dobj = DomainObject.newInstance(context, objectId);
			String strState = dobj.getInfo(context, DomainConstants.SELECT_CURRENT);
			if(STATE_WEEKLY_TIMESHEET_SUBMIT.equalsIgnoreCase(strState) || STATE_WEEKLY_TIMESHEET_APPROVED.equalsIgnoreCase(strState)){
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimesheet.DeleteAlert", context.getSession().getLanguage());
				throw new Exception(sErrMsg);
			}
			else{
				return 0;
			}
		}catch(Exception e){
			throw new Exception(e.getMessage());
		}
	} 
	/**
	 * trigger method to validate sumbition of Weekly Timesheet
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @return String - week number
	 * @throws Exception 
	 * @exception Exception
	 *                if operation fails
	 * @since Program Central R210
	 * @throws Exception 
	 */
	public int triggerValidateEffortSubmition(Context context,String[] args) throws Exception {
		try{
			String objectId  = args[0];
			String state = args[1];
			DomainObject dobj = DomainObject.newInstance(context, objectId);
			String strTotalEffort = dobj.getAttributeValue(context,ATTRIBUTE_TOTAL_EFFORT);
			int intTotal = 0;
			double d = 0.0d;
			d = Task.parseToDouble(strTotalEffort);
			if(state.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT) || state.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_APPROVED)){
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimesheet.SubmitEffortalert", context.getSession().getLanguage());
				throw new Exception(sErrMsg);
			}
			else{
				return 0;
			}
		}catch(Exception e){
			throw new Exception(e.getMessage());
		}
	} 

	/**
	 * This method is used to submit the all efforts within the timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: args[0] - Contains a
	 *            timesheet id.
	 * @return integer value 0 or 1
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */ 	
	public int triggersubmitWeeklyTimesheet (Context context,String[] args) throws Exception {
		try{
			String objectId  = args[0];
			WeeklyTimesheet wt = new WeeklyTimesheet(objectId);
			i18nNow i18nnow = new i18nNow();
			String personId = PersonUtil.getPersonObjectID(context);
			String language = context.getSession().getLanguage();
			MapList mpEfforts = wt.getEfforts(context, null, null, null);
			MapList mpEffortToDisconnect = new MapList();
			if(mpEfforts.size()<=0){
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimesheet.TimesheetWithoutEffort", context.getSession().getLanguage());
				throw new Exception(sErrMsg);
			}
			StringList strTaskWithZeroEffort = new StringList();
			for(int i=0;i<mpEfforts.size();i++){
				Map effort = (Map)mpEfforts.get(i);
				String strEffortId = (String)effort.get(DomainConstants.SELECT_ID);
				String strEffortState = (String)effort.get(DomainConstants.SELECT_CURRENT);
				DomainObject objEffort = DomainObject.newInstance(context,strEffortId);
				String strTotalEffort = objEffort.getAttributeValue(context,ATTRIBUTE_TOTAL_EFFORT);
				int intTotal = 0;
				double d = 0.0d;
				d = Task.parseToDouble(strTotalEffort);

				String strTaskId = (String)effort.get("to["+RELATIONSHIP_HAS_EFFORTS+"].from.id");
				DomainObject objTask = DomainObject.newInstance(context,strTaskId);
				StringList strObjectList = new StringList(2);
				strObjectList.add(SELECT_CURRENT);
				strObjectList.add(SELECT_TYPE);
				strObjectList.add(SELECT_NAME);
				Map taskMap =  objTask.getInfo(context, strObjectList);

				if(d==0.0){
					RelationshipType relEfforts = new matrix.db.RelationshipType(
							RELATIONSHIP_EFFORTS);
					RelationshipType relHasEfforts = new matrix.db.RelationshipType(
							RELATIONSHIP_HAS_EFFORTS);
					String strProject = getProjectFromEffort(context, strEffortId);	
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						DomainObject objProject = DomainObject.newInstance(context, strProject);
						objEffort.disconnect(context,relEfforts,false, objProject);
						objEffort.disconnect(context,relHasEfforts,false, objTask);
						objEffort.disconnect(context,relEfforts,false, DomainObject.newInstance(context, objectId));	
					} finally {			
						ContextUtil.popContext(context);			
					}
					strTaskWithZeroEffort.add(taskMap.get(SELECT_NAME));
					continue;
				}
				String strType = (String)taskMap.get(SELECT_TYPE);
				if(!strType.equals(TYPE_PHASE))
				{
					setRemainingEffort(context,strEffortId,strTaskId,personId);
				}

				if(strEffortState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_REJECTED)){
					objEffort.promote(context);
				}else if(!strEffortState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_APPROVED) && 
						!strEffortState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){
					objEffort.promote(context);
					objEffort.promote(context);
					if(taskMap.get(SELECT_TYPE).equals(TYPE_TASK) || taskMap.get(SELECT_TYPE).equals(TYPE_PHASE)) {
						if(taskMap.get(SELECT_TYPE).equals(TYPE_PHASE)){
							ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
							try {
								MqlUtil.mqlCommand(context, "trigger off", true);	//PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
							} finally {			
								ContextUtil.popContext(context);			
							}
						}                    
						/*if(taskMap.get(SELECT_CURRENT).equals(STATE_TASK_CREATE)){ 
							ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
							try {
								objTask.promote(context);
								objTask.promote(context);      
							} finally {			
								ContextUtil.popContext(context);			
							}
						} else if(taskMap.get(SELECT_CURRENT).equals(STATE_TASK_ASSIGN)){ 
							ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
							try {
								objTask.promote(context);    
							} finally {			
								ContextUtil.popContext(context);			
							}
						}*/
						if(taskMap.get(SELECT_TYPE).equals(TYPE_PHASE)){	
							ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
							try {
								MqlUtil.mqlCommand(context, "trigger on", true);	//PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
							} finally {			
								ContextUtil.popContext(context);			
							}
						}
					}
				}
			}
			if(mpEfforts.size()==strTaskWithZeroEffort.size()){
				return 1;
			}
			return 0;
		}catch(Exception e){
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * This method is used to validate effort for the timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: args[0] - Contains a
	 *            state information.
	 * @return integer value 0 or 1
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */ 		
	public int triggerValidateEffortProcessing (Context context,String[] args) throws Exception {
		try{
			String strState = args[1];
			if(!strState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimesheet.EffortCanNotRejected", context.getSession().getLanguage());
				throw new Exception(sErrMsg);
			}else{
				return 0;
			}
		}catch(Exception e){
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * This method is used to reject the all efforts and timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: args[0] - Contains a
	 *            state information.
	 * @return integer value 0 or 1
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */ 	
	public int triggerRejectTimesheet (Context context,String[] args) throws Exception {
		String objectId  = args[0];
		int retValue = 0;
		WeeklyTimesheet wt = new WeeklyTimesheet(objectId);
		MapList mpEfforts = wt.getEfforts(context, null, null, null);
		String personId = PersonUtil.getPersonObjectID(context);
		try{
			boolean isApprover = false;
			for(int i=0;i<mpEfforts.size();i++){
				Map effort = (Map)mpEfforts.get(i);
				String strEffortId = (String)effort.get(DomainConstants.SELECT_ID);
				MapList approverList = getApprover(context,strEffortId);
				for(int idx=0; idx<approverList.size();idx++){
					Map approverMap = (Map)approverList.get(idx);
					String loginPersonId = PersonUtil.getPersonObjectID(context);
					if(loginPersonId.equals(approverMap.get(DomainConstants.SELECT_ID))){
						isApprover = true;
					}
				}
				if(isApprover) {
					String strEffortState = (String)effort.get(DomainConstants.SELECT_CURRENT);
					DomainObject objEffort = DomainObject.newInstance(context,strEffortId);
					String strTaskId = (String)effort.get("to["+RELATIONSHIP_HAS_EFFORTS+"].from.id");

					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);							
					String strType = "";
					try {
						DomainObject domTaskObj = DomainObject.newInstance(context,strTaskId);
						strType = (String)domTaskObj.getInfo(context,SELECT_TYPE);	
					}finally {
						ContextUtil.popContext(context);
					}
					if(!strType.equals(TYPE_PHASE))
					{
						setRemainingEffort(context,strEffortId,strTaskId,personId);
					}
					if(strEffortState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){
						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);								
						try {
							MqlUtil.mqlCommand(context, "trigger off", true); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
							objEffort.demote(context);
							MqlUtil.mqlCommand(context, "trigger on", true); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
						} finally {
							ContextUtil.popContext(context);
						}
					}
					isApprover = false;
				}
			}			
			return retValue;
		}catch(Exception e){
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * This method is used to reject entire timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: args[0] - Contains a
	 *            state information.
	 * @return integer value 0 or 1
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */ 	
	public int triggerRejectEntireTimesheet (Context context,String[] args) throws Exception {
		try{
			String objectId  = args[0];
			int retValue = 0;
			DomainObject objEffort = DomainObject.newInstance(context,objectId);
			String strTimesheetId = getTimesheetFromEffort(context, objectId);
			if (strTimesheetId != null
					&& !strTimesheetId.equals("")
					&& !strTimesheetId.equals("null"))
			{
				DomainObject objTimesheeDomainObject = DomainObject.newInstance(context, strTimesheetId);
				String strState = objTimesheeDomainObject.getInfo(context, DomainConstants.SELECT_CURRENT);
				if(strState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);							
					try {
						MqlUtil.mqlCommand(context, "trigger off", true); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
						objTimesheeDomainObject.demote(context);
						MqlUtil.mqlCommand(context, "trigger on", true);	//PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
					} finally {			
						ContextUtil.popContext(context);			
					}	
				} 
			}
			return retValue;
		}catch(Exception e){
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */		
	public void triggerdeleteConnectedEfforts(Context context,String[] args) throws Exception {

	}	

	/**
	 * This method is used to submit the timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the timesheet object
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public void submit(Context context, String[] args) throws MatrixException{
		try {
			Person person = new Person(PersonUtil
					.getPersonObjectID(context));
			String strTimesheetId = (String)JPO.unpackArgs(args);
			MapList mpTasks = displayTimesheetTasks(context, strTimesheetId);
			ContextUtil.startTransaction(context, true);
			DomainObject objTimesheeDomainObject = DomainObject.newInstance(context, strTimesheetId);
			String strState = objTimesheeDomainObject.getInfo(context, DomainConstants.SELECT_CURRENT);
			if(strState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_REJECTED)==true){				
				objTimesheeDomainObject.promote(context);    				  				
			} else {
				objTimesheeDomainObject.promote(context);
				objTimesheeDomainObject.promote(context); 								
			}									
			String[] strArgs = new String[3];
			strArgs[0] = strTimesheetId;
			strArgs[1] = "from";
			strArgs[2] = RELATIONSHIP_APPROVER_CONTEXT;
			for(int i=0;i<mpTasks.size();i++){
				Map task = (Map)mpTasks.get(i);
				String strType = (String)task.get(DomainConstants.SELECT_TYPE);
				String strId = (String)task.get(DomainConstants.SELECT_ID);
				if (strType != null
						&& !strType.equals("")
						&& !strType.equals("null"))
				{
					if(strType.equalsIgnoreCase(DomainConstants.TYPE_PROJECT_SPACE)){
						String strProjectLead = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.Common.ProjectLead", context.getSession().getLanguage());
						String strPreference = null;
						String strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");
						if(SEELCT_APPROVER_PROJECT_LEAD.equals(strApprover)){
							strPreference = strProjectLead;
						}
						if(strProjectLead.equalsIgnoreCase(strPreference)){
							if(isRelationshipExist(context,strTimesheetId,strId)) {							
								ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
								try {
									//MODIFIED::PA4:13-Jun-2011:IR-100970V6R2012x:START
									DomainObject objPrj = DomainObject.newInstance(context,strId);
									//MODIFIED::PA4:13-Jun-2011:IR-100970V6R2012x:END
									DomainRelationship.connect(context, objTimesheeDomainObject, 
											RELATIONSHIP_APPROVER_CONTEXT, objPrj);
								}finally {			
									ContextUtil.popContext(context);			
								}    																
							}								
						}
						else{
							StringList objectSelects = new StringList();
							objectSelects.add(SELECT_ID);
							objectSelects.add(SELECT_TYPE);
							objectSelects.add(SELECT_NAME);
							StringList relSelects = new StringList();
							StringBuffer stbTypeName = new StringBuffer();
							stbTypeName.append(TYPE_ORGANIZATION);
							StringBuffer stbRelPattern = new StringBuffer();
							stbRelPattern.append(RELATIONSHIP_COMPANY_DEPARTMENT);
							stbRelPattern.append(",");
							stbRelPattern.append(RELATIONSHIP_DIVISION);
							stbRelPattern.append(",");
							stbRelPattern.append(RELATIONSHIP_MEMBER);
							String strOrgId = person.getInfo(context, "to["+DomainConstants.RELATIONSHIP_EMPLOYEE+"].from.id");
							MapList orgList = person.getRelatedObjects(context,
									stbRelPattern.toString(), // relationships pattern 
									stbTypeName.toString(),   // type pattern
									objectSelects,            // Object select
									relSelects,				  // rel select
									true,                     // from traverse
									false,                    // to traverse
									(short)0,                 // recurse level
									null,                     // object where clause
									"",                       // rel where clause
									0);                       // total no. of objects to retrieve
							StringList slOrg = null;
							if(orgList!=null && orgList.size()>0) {
								slOrg = new StringList();
								for(int k=0;k<orgList.size();k++){
									Map orgMap = (Map)orgList.get(k);
									String strCompanyBUId = (String)orgMap.get(DomainConstants.SELECT_ID);
									if(!slOrg.contains(strCompanyBUId)){
										slOrg.add(strCompanyBUId);
									}										
								}
								if (strOrgId != null
										&& !strOrgId.equals("")
										&& !strOrgId.equals("null"))
								{
									for(int k=0;k<slOrg.size();k++){
										DomainObject domObj = DomainObject.newInstance(context,slOrg.get(k).toString());										
										if(isRelationshipExist(context,strTimesheetId,slOrg.get(k).toString())) {                                                                                		  
											ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
											try {
												DomainRelationship.connect(context, objTimesheeDomainObject, 
														RELATIONSHIP_APPROVER_CONTEXT, domObj); 
											}finally {			
												ContextUtil.popContext(context);			
											}                                    	                                    	  	
										}								
									}
								}
							}
						}
					}
				}
			}
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			try {
				ContextUtil.commitTransaction(context);
			}finally {			
				ContextUtil.popContext(context);			
			}    			
		} catch (Exception e) {
			ContextUtil.abortTransaction(context);
			throw new MatrixException(e.getMessage());					
		} 	
	}

	/**
	 * This method is used to reject the timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectId - Contains a
	 *            timesheet id and comments - Contains approver comments
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public void reject(Context context, String[] args) throws Exception{
		try {
			ContextUtil.startTransaction(context, true);
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strTimesheetId = (String)programMap.get("objectId");
			String strComments = (String)programMap.get("comments");
			DomainObject objTimesheeDomainObject = DomainObject.newInstance(context, strTimesheetId);
			String strState = objTimesheeDomainObject.getInfo(context, DomainConstants.SELECT_CURRENT);			
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			try {
				objTimesheeDomainObject.setAttributeValue(context, ATTRIBUTE_APPROVER_COMMENTS, strComments);				
			}finally {
				ContextUtil.popContext(context);
			}
			if(objTimesheeDomainObject.isKindOf(context,
					TYPE_WEEKLY_TIMESHEET)) {
				if(STATE_WEEKLY_TIMESHEET_SUBMIT.equalsIgnoreCase(strState)) {
					objTimesheeDomainObject.demote(context);
				} else {
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.WeeklyTimesheet.ActionOnRejectedTimesheet", context.getSession().getLanguage());
					ContextUtil.abortTransaction(context);
					throw new Exception(sErrMsg);					
				}
			}else if(objTimesheeDomainObject.isKindOf(context,
					TYPE_EFFORT)){
				if(strState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){
					String strPersonId = PersonUtil.getPersonObjectID(context);	
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					String strTaskId = objTimesheeDomainObject.getInfo(context,SELECT_TASK_ID);
					//modified:ms9:6-12-2010:HF-083875V6R2011x_ start
					try {
						DomainObject domObj = DomainObject.newInstance(context,strTaskId);
						if(!domObj.isKindOf(context,TYPE_PHASE))
							setRemainingEffort(context,strTimesheetId,strTaskId,strPersonId);
						//modified:ms9:6-12-2010:HF-083875V6R2011x_ end
						objTimesheeDomainObject.demote(context);
					} finally {
						ContextUtil.popContext(context);
					}
				}
				if(strState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_APPROVED)){
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.WeeklyTimesheet.ActionOnRejectedTimesheetEffort", context.getSession().getLanguage());
					MqlUtil.mqlCommand(context, "notice " + sErrMsg);			
				}
			}
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			try {
				ContextUtil.commitTransaction(context);
			}finally {	
				ContextUtil.popContext(context);
			}
		} catch (FrameworkException e) {
			ContextUtil.abortTransaction(context);
			throw new FrameworkException(e.getMessage());		
		} 	
	}

	/**
	 * This method is used to approve the timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectId - Contains a
	 *            timesheet id and comments - Contains approver comments
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public void approve(Context context, String[] args) throws Exception{
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strTimesheetId = (String)programMap.get("objectId");
			String strComments = (String)programMap.get("comments");
			DomainObject objTimesheeDomainObject = DomainObject.newInstance(context, strTimesheetId);
			String strState = objTimesheeDomainObject.getInfo(context, DomainConstants.SELECT_CURRENT);

			if(objTimesheeDomainObject.isKindOf(context,
					TYPE_WEEKLY_TIMESHEET)) {
				if(STATE_WEEKLY_TIMESHEET_SUBMIT.equalsIgnoreCase(strState)) {

					WeeklyTimesheet wt = new WeeklyTimesheet(strTimesheetId);
					MapList mpEfforts = wt.getEfforts(context, null, null, null);
					boolean isAllEffortApproved = true;

					approveEfforts(context,mpEfforts);

					for(int j=0;j<mpEfforts.size();j++) {
						Map efforts = (Map)mpEfforts.get(j);
						String currentState = (String)efforts.get(DomainConstants.SELECT_CURRENT);
						if(!currentState.equalsIgnoreCase(ProgramCentralConstants.STATE_EFFORT_APPROVED)){
							isAllEffortApproved = false;
							break;
						}
					}
					if(isAllEffortApproved) {
						objTimesheeDomainObject.promote(context);
					}


				} else {
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.WeeklyTimesheet.ActionOnRejectedTimesheet", context.getSession().getLanguage());
					throw new Exception(sErrMsg);					
				}
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				try {
					objTimesheeDomainObject.setAttributeValue(context, ATTRIBUTE_APPROVER_COMMENTS, strComments);
				} finally {
					ContextUtil.popContext(context);
				}
			}else if(objTimesheeDomainObject.isKindOf(context,
					TYPE_EFFORT)){
				if(strState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){
					objTimesheeDomainObject.promote(context);
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						String taskId = objTimesheeDomainObject.getInfo(context,SELECT_TASK_ID);
						getPercentageComplete(context,taskId,strTimesheetId);
						com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task(taskId);
						task.rollupAndSave(context);						
					}
					finally {
						ContextUtil.popContext(context);
					}
				}
				if(strState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_REJECTED)){
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.WeeklyTimesheet.ActionOnApprovedTimesheetEffort", context.getSession().getLanguage());
					MqlUtil.mqlCommand(context, "notice " + sErrMsg);
				}			
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				try {
					objTimesheeDomainObject.setAttributeValue(context, ATTRIBUTE_EFFORT_COMMENTS, strComments);
				} finally {
					ContextUtil.popContext(context);
				}
			}
		} catch (FrameworkException e) {
			throw new FrameworkException(e.getMessage());		
		} 	
	}

	/**
	 * This method will approve the efforts for which logged in user is approver.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param  mpEfforts      
	 * 			   mapList of submited efforts
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2015x
	 * @author e3b
	 * 
	 */

	private void approveEfforts(Context context,MapList mpEfforts)throws Exception
	{
		try{
			boolean isApprover = false;
			for(int i=0;i<mpEfforts.size();i++){
				Map effort = (Map)mpEfforts.get(i);
				String strEffortId = (String)effort.get(DomainConstants.SELECT_ID);
				MapList approverList = getApprover(context,strEffortId);
				for(int idx=0; idx<approverList.size();idx++){
					Map approverMap = (Map)approverList.get(idx);
					String loginPersonId = PersonUtil.getPersonObjectID(context);
					if(loginPersonId.equals(approverMap.get(DomainConstants.SELECT_ID))){
						isApprover = true;
					}
				}
				if(isApprover) {
					String strEffortState = (String)effort.get(DomainConstants.SELECT_CURRENT);
					DomainObject objEffort = DomainObject.newInstance(context,strEffortId);
					if(strEffortState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){	
						ProgramCentralUtil.pushUserContext(context);
						try {
							MqlUtil.mqlCommand(context, "trigger off", true, false);
							objEffort.promote(context);
							MqlUtil.mqlCommand(context, "trigger on", true, false);
						} finally {
							ProgramCentralUtil.popUserContext(context);
						}
					}
					effort.put(DomainConstants.SELECT_CURRENT, ProgramCentralConstants.STATE_EFFORT_APPROVED);
					ContextUtil.commitTransaction(context);
					ProgramCentralUtil.pushUserContext(context);
					try {
						String taskId = objEffort.getInfo(context,SELECT_TASK_ID);
						getPercentageComplete(context,taskId,strEffortId);
						com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task(taskId);
						task.rollupAndSave(context);
					} finally {
						ProgramCentralUtil.popUserContext(context);
					}
				}
			}
		}catch(Exception e){
			ContextUtil.abortTransaction(context);
			throw new FrameworkException(e.getMessage());			
		} 
	}
	/**
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * @return integer
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */		
	public int triggerApproveEntireTimesheet(Context context, String[] args) throws Exception{
		try {
			//TODO if effort being approved is last in timesheet, then approve entire timesheet.
			String objectId  = args[0];
			int retValue = 0;
			DomainObject objEffort = DomainObject.newInstance(context,objectId);
			String strTimesheetId = getTimesheetFromEffort(context, objectId);
			if(strTimesheetId!=null){
				WeeklyTimesheet wt = new WeeklyTimesheet(strTimesheetId);
				String strCurrent = wt.getInfo(context, DomainConstants.SELECT_CURRENT);
				MapList mpEfforts = wt.getEfforts(context, null, null, null);
				int noOfEfforts = mpEfforts.size();
				int noOfApprovedEfforts = 0;
				for(int i=0;i<mpEfforts.size();i++){
					Map effort = (Map)mpEfforts.get(i);
					String strEffortState = (String)effort.get(DomainConstants.SELECT_CURRENT);
					if(strEffortState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_APPROVED)){
						noOfApprovedEfforts = noOfApprovedEfforts + 1;
					}
				}
				if(noOfEfforts == noOfApprovedEfforts){
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);													
					try {
						MqlUtil.mqlCommand(context, "trigger off", true); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
						if(strCurrent.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){
							wt.promote(context);
						}
						if(strCurrent.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_REJECTED)){
							wt.promote(context);
							wt.promote(context);
						}
						MqlUtil.mqlCommand(context, "trigger on", true); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
					} finally {			
						ContextUtil.popContext(context);			
					}	
				}
			}
			return 0;
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());	
		} 
	}

	/**
	 * This method is used to approve the all efforts and timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: args[0] - Contains a
	 *            state information.
	 * @return integer value 0 or 1
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */ 	
	public int triggerApproveTimesheet(Context context, String[] args) throws Exception{
		try {
			String objectId  = args[0];
			int retValue = 0;
			WeeklyTimesheet wt = new WeeklyTimesheet(objectId);
			MapList mpEfforts = wt.getEfforts(context, null, null, null);
			ContextUtil.startTransaction(context, true);
			try{
				boolean isApprover = false;
				for(int i=0;i<mpEfforts.size();i++){
					Map effort = (Map)mpEfforts.get(i);
					String strEffortId = (String)effort.get(DomainConstants.SELECT_ID);
					MapList approverList = getApprover(context,strEffortId);
					for(int idx=0; idx<approverList.size();idx++){
						Map approverMap = (Map)approverList.get(idx);
						String loginPersonId = PersonUtil.getPersonObjectID(context);
						if(loginPersonId.equals(approverMap.get(DomainConstants.SELECT_ID))){
							isApprover = true;
						}
					}
					if(isApprover) {
						String strEffortState = (String)effort.get(DomainConstants.SELECT_CURRENT);
						DomainObject objEffort = DomainObject.newInstance(context,strEffortId);
						if(strEffortState.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT)){	
							ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
							try {
								MqlUtil.mqlCommand(context, "trigger off", true); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
								objEffort.promote(context);
								MqlUtil.mqlCommand(context, "trigger on", true);	//PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
							} finally {
								ContextUtil.popContext(context);
							}
						}
						effort.put(DomainConstants.SELECT_CURRENT, ProgramCentralConstants.STATE_EFFORT_APPROVED);
						ContextUtil.commitTransaction(context);
						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						try {
							String taskId = objEffort.getInfo(context,SELECT_TASK_ID);
							getPercentageComplete(context,taskId,strEffortId);
							com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task(taskId);
							task.rollupAndSave(context);
						} finally {
							ContextUtil.popContext(context);
						}
						isApprover = false;
					}
				}
			}catch(Exception e){
				ContextUtil.abortTransaction(context);
				throw new FrameworkException(e.getMessage());			
			} 
			try{
				boolean isStateApproved = false;
				for(int j=0;j<mpEfforts.size();j++) {
					Map efforts = (Map)mpEfforts.get(j);
					String currentState = (String)efforts.get(DomainConstants.SELECT_CURRENT);
					if(currentState.equalsIgnoreCase(ProgramCentralConstants.STATE_EFFORT_APPROVED)==false){
						isStateApproved = true;
					}
				}
				if(isStateApproved) {
					return 1;
				} else
					return 0;
			} catch(Exception e){
				ContextUtil.abortTransaction(context);
				throw new FrameworkException(e.getMessage());
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * Lists all the projects with the tasks assigned to context user.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args
	 * @return StringList containing the project id's.
	 * @throws MatrixException if the operation fails
	 */ 	
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList addTask(Context context, String[] args) throws MatrixException
	{
		try 
		{
			StringList slProjectList = new StringList();
			StringList slMemProjectList = new StringList();
			String projectId= "";
			String strPersonId = PersonUtil.getPersonObjectID(context);
			Person person = new Person(strPersonId);
			String strHoldCancelPropKey = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.WeeklyTimesheet.DisplayHoldAndCanceledProjects");		    
			boolean isHoldAndCanceledProjectDisplay =(ProgramCentralUtil.isNotNullString(strHoldCancelPropKey) && "True".equalsIgnoreCase(strHoldCancelPropKey))?true:false;
			String SEPERATOR = ",";
			StringList slProjectSelectList = new StringList();
			slProjectSelectList.add(SELECT_ID);
			slProjectSelectList.add(SELECT_STATES);
			slProjectSelectList.add("attribute["+ATTRIBUTE_EFFORT_SUBMISSION+"]");
			slProjectSelectList.add(DomainConstants.SELECT_POLICY);
			slProjectSelectList.add(SELECT_CURRENT);	

			MapList mlProject = person.getRelatedObjects(context,
					RELATIONSHIP_MEMBER,
					TYPE_PROJECT_SPACE,slProjectSelectList, null,
					true, false, (short)1,null,null,0);

			StringList slTaskSelectList = new StringList();
			slTaskSelectList.add(SELECT_ID);
			slTaskSelectList.add(SELECT_NAME);
			slTaskSelectList.add(SELECT_TYPE);
			slTaskSelectList.add(SELECT_STATES);
			slTaskSelectList.add(SELECT_CURRENT);	

			MapList mlAssignedTask = person.getRelatedObjects(context,
					RELATIONSHIP_ASSIGNED_TASKS,
					TYPE_TASK_MANAGEMENT,slTaskSelectList, null,
					false, true, (short)1,null,null,0);	

			StringList slTaskIdList = new StringList();
			Map mapTaskObject = new HashMap();
			String[] excludeSubTypeArray			=	new String[] {ProgramCentralConstants.TYPE_PHASE,			
					ProgramCentralConstants.TYPE_VPLM_TASK};
			List<String> taskManagementSubTypeList	=	
					ProgramCentralUtil.getFilteredSubTypeList(context,ProgramCentralConstants.TYPE_TASK_MANAGEMENT,excludeSubTypeArray);

			if(null!=mlAssignedTask)
			{
				int assignedTaskSize = mlAssignedTask.size();	
				for(int i=0; i<assignedTaskSize; i++)
				{
					Map projectMap = (Map)mlAssignedTask.get(i);
					String strTaskId = (String)projectMap.get(SELECT_ID);
					slTaskIdList.add(strTaskId);
					Map mapTaskData = new HashMap();
					mapTaskData.put(SELECT_TYPE, projectMap.get(SELECT_TYPE));
					mapTaskData.put(SELECT_CURRENT, projectMap.get(SELECT_CURRENT));
					mapTaskData.put(SELECT_STATES,projectMap.get(SELECT_STATES));
					mapTaskObject.put(strTaskId,mapTaskData);
				}
			}			
			if(null!=mlProject)
			{
				int projectListSize = mlProject.size();	
				boolean isProjectAdd = false;
				for(int i=0; i<projectListSize; i++)
				{
					Map projectMap = (Map)mlProject.get(i);
					String strProjectId = (String)projectMap.get(SELECT_ID);
					slMemProjectList.add(strProjectId);
					DomainObject domProject = newInstance(context,strProjectId);
					String effortSubmission = (String)projectMap.get("attribute["+ATTRIBUTE_EFFORT_SUBMISSION+"]");
					String strPolicy = (String)projectMap.get(DomainConstants.SELECT_POLICY);
					if(!isHoldAndCanceledProjectDisplay && !ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(strPolicy))
					{
						StringBuffer sbTypePattern = new StringBuffer(50);
						short recurssionlLevel =0;
						if(TYPE_PHASE.equals(effortSubmission))
						{
							sbTypePattern.append(TYPE_PHASE);
							recurssionlLevel =1;
						} 
						else 
						{
							sbTypePattern.append(TYPE_TASK_MANAGEMENT);
						}
						MapList taskMapList = domProject.getRelatedObjects(context,
								RELATIONSHIP_SUBTASK,
								sbTypePattern.toString(),
								slTaskSelectList,
								null,
								false,
								true,
								recurssionlLevel,
								null, 
								null,0);	
						if(null!=taskMapList)
						{
							int projectTaskSizeList = taskMapList.size();
							for(int taskIndex=0; taskIndex<projectTaskSizeList;taskIndex++)
							{
								isProjectAdd = false;
								Map mapProjectTaskObject = (Map)taskMapList.get(taskIndex);
								String strTaskMgtId = (String)mapProjectTaskObject.get((SELECT_ID));
								String strTaskType = (String)mapProjectTaskObject.get(SELECT_TYPE);
								StringList taskStateList = (StringList) mapProjectTaskObject.get(SELECT_STATES);
								String strTaskCurrent = (String)mapProjectTaskObject.get(SELECT_CURRENT);
								int taskCurrentPosition = taskStateList.indexOf(strTaskCurrent);
								if(slTaskIdList.contains(strTaskMgtId))
								{
									slTaskIdList.remove(strTaskMgtId);
									if(TYPE_PHASE.equals(effortSubmission)&& taskCurrentPosition>1)
									{
										isProjectAdd = true;
									}
									else if(TYPE_TASK.equals(effortSubmission) && taskManagementSubTypeList.contains(strTaskType))
									{
										isProjectAdd = true;
									}
								}
								if(((TYPE_PHASE.equals(effortSubmission) && strTaskType.equals(TYPE_PHASE) && taskCurrentPosition>1) || isProjectAdd) 
										&& !slProjectList.contains(strProjectId))
								{
									slProjectList.add(strProjectId);
								}
							}
						}
					}
				}
			}
			int unAssignProjectTaskSize = slTaskIdList.size();
			for(int taskIndex=0;taskIndex<unAssignProjectTaskSize;taskIndex++)
			{
				String strTaskId = (String)slTaskIdList.get(taskIndex);
				Map mapTaskDataValues = (Map)mapTaskObject.get(strTaskId);
				String strTaskType = (String)mapTaskDataValues.get(SELECT_TYPE);
				StringList taskStateList = (StringList) mapTaskDataValues.get(SELECT_STATES);
				String strTaskCurrent = (String)mapTaskDataValues.get(SELECT_CURRENT);
				int taskCurrentPosition = taskStateList.indexOf(strTaskCurrent);
				if(ProgramCentralUtil.isNotNullString(strTaskId))
				{
					Task task = new Task(strTaskId);							        						        
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					Map projectMap = null;
					try 
					{
						projectMap = task.getProject(context,slProjectSelectList,true);
					}
					finally 
					{
						ContextUtil.popContext(context);
					}
					projectId = (String)projectMap.get(SELECT_ID);
					if(!slMemProjectList.contains(projectId))
					{
						String strPolicy = (String)projectMap.get(DomainConstants.SELECT_POLICY);
						String strProjectType = (String)projectMap.get(DomainConstants.SELECT_TYPE);
						if(!isHoldAndCanceledProjectDisplay && !ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(strPolicy))
						{
							String effortSubmission = (String)projectMap.get("attribute["+ATTRIBUTE_EFFORT_SUBMISSION+"]");
							if(TYPE_PHASE.equals(effortSubmission) && task.isKindOf(context,TYPE_PHASE)&& !strProjectType.equalsIgnoreCase(ProgramCentralConstants.TYPE_EXPERIMENT))
							{
								if(taskCurrentPosition>1)
								{
									slProjectList.add(strTaskId);
								}
							}
							else if(TYPE_TASK.equals(effortSubmission)&&!strProjectType.equalsIgnoreCase(ProgramCentralConstants.TYPE_EXPERIMENT) && taskManagementSubTypeList.contains(strTaskType))
							{
								slProjectList.add(strTaskId);
							}
						}
					}
				}
			}
			return slProjectList;
		} 
		catch (Exception e) 
		{
			throw new MatrixException(e);
		}
	}
	/**
	 * This method is used to expand the project task and phase in search.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectId - Contains a
	 *            object id.
	 * @return MapList contains map of task and phase
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */ 	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList expandProject(Context context, String[] args) throws MatrixException{
		MapList strProjectList = new MapList();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String taskId = (String)programMap.get("objectId");
			Task task = new Task();
			task.setId(taskId);
			boolean isProjectSpace = false;
			if(task.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)){
				isProjectSpace = true;
			}else{
				return strProjectList;
			}
			String strPreference = task.getAttributeValue(context, ATTRIBUTE_EFFORT_SUBMISSION);
			Person person = new Person(PersonUtil
					.getPersonObjectID(context));
			StringList task_busSelects =  new StringList(6);
			task_busSelects.add(task.SELECT_ID);
			task_busSelects.add(task.SELECT_NAME);
			task_busSelects.add(SELECT_TYPE);
			task_busSelects.add(SELECT_CURRENT);
			StringList relSelects = new StringList(1);
			MapList subTask = task.getTasks(context, 0, task_busSelects, relSelects,false); // newly added

			String[] excludeSubTypeArray			=	null;
			if (strPreference.equals(TYPE_PHASE)) {
				excludeSubTypeArray	=	new String[] {ProgramCentralConstants.TYPE_TASK,ProgramCentralConstants.TYPE_GATE,
						ProgramCentralConstants.TYPE_MILESTONE,ProgramCentralConstants.TYPE_VPLM_TASK};

			} else if (strPreference.equals(TYPE_TASK)) {
				excludeSubTypeArray	=	new String[] {	ProgramCentralConstants.TYPE_PHASE};
			}
			List<String> taskManagementSubTypeList	=	
					ProgramCentralUtil.getFilteredSubTypeList(context,ProgramCentralConstants.TYPE_TASK_MANAGEMENT,excludeSubTypeArray);

			for(int i=0;i<subTask.size();i++){
				Map mpTask = (Map)subTask.get(i);
				String strTaskId = (String)mpTask.get(DomainConstants.SELECT_ID);
				String strTaskType = (String)mpTask.get(SELECT_TYPE);
				task.setId(strTaskId);
				String strTaskState = (String)mpTask.get(SELECT_CURRENT);
				if(task.isKindOf(context, TYPE_PHASE) && strPreference.equals(TYPE_PHASE) && !strTaskState.equals(STATE_TASK_CREATE) && !strTaskState.equals(STATE_TASK_ASSIGN))
				{
					//Added  to adjust the level of all tasks to level 1 in timesheet
					mpTask.put("level","1");
					strProjectList.add(mpTask);				
				}else{
					MapList assignees = task.getAssignees(context, task_busSelects, relSelects, null);
					for(int j=0;j<assignees.size();j++){
						Map mpAssignee = (Map)assignees.get(j);
						String strAssigneeId = (String)mpAssignee.get(DomainConstants.SELECT_ID);
						if(isProjectSpace){
							String strType;
							String strTask = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Type.Task");
							if(strPreference.equalsIgnoreCase(strTask)){
								strType = DomainConstants.TYPE_TASK;
								if(strAssigneeId.equalsIgnoreCase(person.getId())){
									if(strTask.equalsIgnoreCase(strType) && taskManagementSubTypeList.contains(strTaskType)){
										//Added  to adjust the level of all tasks to level 1 in timesheet
										mpTask.put("level","1");
										strProjectList.add(mpTask);
									}
								}
							}
						}
						else if(strAssigneeId.equalsIgnoreCase(person.getId())){
							strProjectList.add(mpTask);
						}
					}
				}
			}
			Integer fullTextObjCount = new Integer(strProjectList.size()-1);
			if (strProjectList.size() == 0)
			{
				fullTextObjCount = new Integer(0);
			}
			else
			{
				fullTextObjCount = new Integer(strProjectList.size());

				int nLastElementIndex = fullTextObjCount - 1;
				Map mapLastElement = (Map)strProjectList.get(nLastElementIndex);
				if (mapLastElement.containsKey("expandMultiLevelsJPO"))
				{
					fullTextObjCount--;
				}
			}
			strProjectList.add(0, fullTextObjCount);
		} catch(Exception ex) {
			throw new MatrixException(ex.getMessage());
		}
		return strProjectList;
	}	

	/**
	 * This method is used to expand the project task and phase in default and
	 * detailed view of timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectId - Contains a
	 *            task id and parentOID - Contains a project id.
	 * @return MapList contains Map of task or phase.
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */ 	
	public MapList expandProjects(Context context, String[] args) throws MatrixException{
		MapList strProjectList = new MapList();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String taskId = (String)programMap.get("objectId");
			String parentId = (String)programMap.get("parentOID");
			Task task = new Task();
			task.setId(taskId);
			boolean isProjectSpace = false;
			if(task.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)){
				isProjectSpace = true;
			}else{
				return strProjectList;
			}
			String strPreference = task.getAttributeValue(context, ATTRIBUTE_EFFORT_SUBMISSION);
			Person person = new Person(PersonUtil
					.getPersonObjectID(context));
			StringList task_busSelects =  new StringList(6);
			task_busSelects.add(task.SELECT_ID);
			task_busSelects.add(task.SELECT_NAME);
			task_busSelects.add(SELECT_TYPE);	
			StringList relSelects = new StringList(1);

			MapList subTask = task.getTasks(context, 0, task_busSelects, relSelects,false); 
			for(int i=0;i<subTask.size();i++){
				Map mpTask = (Map)subTask.get(i);
				String strTaskId = (String)mpTask.get(DomainConstants.SELECT_ID);
				String strTaskType = (String)mpTask.get(SELECT_TYPE);
				task.setId(strTaskId);
				MapList assignees = task.getAssignees(context, task_busSelects, relSelects, null);
				for(int j=0;j<assignees.size();j++){
					Map mpAssignee = (Map)assignees.get(j);
					String strAssigneeId = (String)mpAssignee.get(DomainConstants.SELECT_ID);
					if(isProjectSpace){
						String strType;

						String strPhase = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Type.Phase"); 
						String strTask = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Type.Task");
						if(strPreference.equalsIgnoreCase(strPhase)){
							strType = TYPE_PHASE;
						}else{
							strType = DomainConstants.TYPE_TASK;
						}					
						if(strAssigneeId.equalsIgnoreCase(person.getId())){
							WeeklyTimesheet weeklyTimesheet = new WeeklyTimesheet(parentId);
							MapList efforts = weeklyTimesheet.getEfforts(context, null, task, person);
							for(int k=0;k<efforts.size();k++) {
								if(strPhase.equalsIgnoreCase(strType) && !strTaskType.equalsIgnoreCase(TYPE_TASK)){
									strProjectList.add((Map)efforts.get(k));
								}else if(strTask.equalsIgnoreCase(strType) && !strTaskType.equalsIgnoreCase(TYPE_PHASE)){
									strProjectList.add((Map)efforts.get(k));
								}
							}
						}
					}
					else if(strAssigneeId.equalsIgnoreCase(person.getId())){
						strProjectList.add(mpTask);
					}
				}
			}
		} catch(Exception ex) {
			throw new MatrixException(ex.getMessage());
		}
		return strProjectList;
	}	

	/**
	 * This method is used to get effort of sunday for each effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the sunday attribute effort.
	 * @throws MatrixException
	 *             if the operation fails
	 */		
	public Vector getSundayEffort(Context context, String[] args) throws MatrixException 
	{
		Vector lstReturnDayEfforts = getDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_SUNDAY);
		return lstReturnDayEfforts;
	}

	/**
	 * This method is used to set value for attribute Sunday effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: paramMap - Contains a
	 *            effort id and value modified value for attribute Sunday.
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 */		
	public void updateSundayEffort(Context context, String[] args) throws MatrixException 
	{

		updateDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_SUNDAY);
	}

	/**
	 * This method is used to get effort of Monday for each effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the Sunday attribute effort.
	 * @throws MatrixException
	 *             if the operation fails
	 */		
	public Vector getMondayEffort(Context context, String[] args) throws MatrixException 
	{
		Vector lstReturnDayEfforts = getDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_MONDAY);
		return lstReturnDayEfforts;		
	}

	/**
	 * This method is used to set value for attribute Monday effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: paramMap - Contains a
	 *            effort id and value modified value for attribute Monday.
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 */		
	public void updateMondayEffort(Context context, String[] args) throws MatrixException 
	{
		updateDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_MONDAY);
	}	

	/**
	 * This method is used to get effort of Tuesday for each effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the Tuesday attribute effort.
	 * @throws MatrixException
	 *             if the operation fails
	 */		
	public Vector getTuesdayEffort(Context context, String[] args) throws MatrixException 
	{
		Vector lstReturnDayEfforts = getDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_TUESDAY);
		return lstReturnDayEfforts;
	}

	/**
	 * This method is used to set value for attribute Tuesday effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: paramMap - Contains a
	 *            effort id and value modified value for attribute Tuesday.
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 */			
	public void updateTuesdayEffort(Context context, String[] args) throws MatrixException 
	{
		updateDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_TUESDAY);
	}		

	/**
	 * This method is used to get effort of Wednesday for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the Wednesday attribute effort.
	 * @throws MatrixException
	 *             if the operation fails
	 */			
	public Vector getWednesdayEffort(Context context, String[] args) throws MatrixException 
	{
		Vector lstReturnDayEfforts = getDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_WEDNESDAY);
		return lstReturnDayEfforts;
	}

	/**
	 * This method is used to set value for attribute Wednesday effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: paramMap - Contains a
	 *            effort id and value modified value for attribute Wednesday.
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 */			
	public void updateWednesdayEffort(Context context, String[] args) throws MatrixException 
	{
		updateDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_WEDNESDAY);
	}			

	/**
	 * This method is used to get effort of Thursday for each effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the Thursday attribute effort.
	 * @throws MatrixException
	 *             if the operation fails
	 */			
	public Vector getThursdayEffort(Context context, String[] args) throws MatrixException 
	{
		Vector lstReturnDayEfforts = getDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_THURSDAY);
		return lstReturnDayEfforts;
	}

	/**
	 * This method is used to set value for attribute Thursday effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: paramMap - Contains a
	 *            effort id and value modified value for attribute Thursday.
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 */			
	public void updateThursdayEffort(Context context, String[] args) throws MatrixException 
	{
		updateDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_THURSDAY);
	}		

	/**
	 * This method is used to get effort of Friday for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the Friday attribute effort.
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x            
	 * @author vf2  
	 */		
	public Vector getFridayEffort(Context context, String[] args) throws MatrixException
	{
		Vector lstReturnDayEfforts = getDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_FRIDAY);
		return lstReturnDayEfforts;
	}

	/**
	 * This method is used to set value for attribute Friday effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: paramMap - Contains a
	 *            effort id and value modified value for attribute Friday.
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 */			
	public void updateFridayEffort(Context context, String[] args) throws MatrixException 
	{
		updateDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_FRIDAY);
	}			

	/**
	 * This method is used to get effort of Saturday for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the Saturday attribute effort.
	 * @throws MatrixException
	 *             if the operation fails
	 */			
	public Vector getSaturdayEffort(Context context, String[] args) throws MatrixException 
	{
		Vector lstReturnDayEfforts = getDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_SATURDAY);
		return lstReturnDayEfforts;
	}

	/**
	 * This method is used to set value for attribute Saturday effort object.
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: paramMap - Contains a
	 *            effort id and value modified value for attribute Saturday.
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 */		
	public void updateSaturdayEffort(Context context, String[] args) throws MatrixException 
	{
		updateDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_SATURDAY);
	}


	/**
	 * Returns the list of the efforts submitted for a day.  
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the sunday attribute effort.
	 * @throws MatrixException
	 *             if the operation fails
	 */		
	public Vector getDayEffort(Context context, String[] args, String strAttributeName) throws MatrixException 
	{
		Vector retEffortList = new Vector();
		try 
		{
			Map paramMap = (Map) JPO.unpackArgs(args);
			assert(null != paramMap);
			//Map paramList = (Map)paramMap.get("paramList");
			//String strObejectId = (String)paramList.get("objectId");
			MapList objectList = (MapList)paramMap.get("objectList");
			assert(null != objectList);
			Iterator objectListIterator = objectList.iterator();
			DomainObject dObj = DomainObject.newInstance(context);
			StringList slObjectInfo = new StringList();
			slObjectInfo.addElement("attribute["+strAttributeName+"]");
			Map mProjectEffortsIndexMap = new HashMap();
			Map mProjectEffortMap = new HashMap();
			int index = 0; 
			int sTotalColumnIndex = 0;
			double sTotalEffortsOnADay = 0.0;
			String[] sObjectArray = new String[objectList.size()];
			String sPersonUserAgent = PropertyUtil.getSchemaProperty(context, "person_UserAgent");
			StringList slObjectSelect = new StringList();
			slObjectSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			Boolean sLevelCheck=false;
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setMaximumFractionDigits(2);
			while(objectListIterator.hasNext()) 
			{
				Map mapObject = (Map) objectListIterator.next();
				String sLevel = (String)mapObject.get("level");
				String strEffort = "";
				if("2".equalsIgnoreCase(sLevel))
				{
					//2. Get the efforts for task and calculate total efforts for the project and update in the projectEffortMap 
					String sEffortObjId = (String)mapObject.get(DomainConstants.SELECT_ID);
					dObj.setId(sEffortObjId);
					Map mObjInfo = dObj.getInfo(context, slObjectInfo);
					//strEffort = mObjInfo.get("attribute["+strAttributeName+"]").toString();
					strEffort = Double.toString(Task.parseToDouble(mObjInfo.get("attribute["+strAttributeName+"]").toString()));
					String sParentId = (String)mapObject.get("id[parent]");
					String sTotalEffortForProject = "0";
					if(mProjectEffortMap.size()!=0){
						sTotalEffortForProject = mProjectEffortMap.get(sParentId).toString();
					}
					double dCurrentEffort = Task.parseToDouble(strEffort);
					double dProjectEffort = Task.parseToDouble(sTotalEffortForProject);
					dProjectEffort = dProjectEffort + dCurrentEffort;
					mProjectEffortMap.put(sParentId, dProjectEffort);
					sTotalEffortsOnADay = sTotalEffortsOnADay + dCurrentEffort;
					String strRoundedValue = numberFormat.format(sTotalEffortsOnADay);
					if (strRoundedValue != null && !"".equals(strRoundedValue)) {
						sTotalEffortsOnADay = Task.parseToDouble(strRoundedValue);
					}
				}
				else
				{
					if(sLevel.equalsIgnoreCase("0")){
						sLevelCheck=true;
						break;
					}else{
						strEffort = mapObject.get("attribute["+strAttributeName+"]").toString();
					}
					String sObjId = (String)mapObject.get(DomainConstants.SELECT_ID);
					dObj.setId(sObjId);
					String isProjectSpace = DomainObject.EMPTY_STRING;
					//For Non project members do not have access on project 
					ContextUtil.pushContext(context, sPersonUserAgent,DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						Map mObjectInfo = dObj.getInfo(context, slObjectSelect);
						isProjectSpace = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
					} finally {			
						ContextUtil.popContext(context);			
					}	

					if("true".equalsIgnoreCase(isProjectSpace))
					{
						if(!mProjectEffortsIndexMap.containsKey(sObjId))  // 1.if new project then add the project in list and store it's index 
						{
							mProjectEffortsIndexMap.put(sObjId, index);
							mProjectEffortMap.put(sObjId, "0");
						}
					}
					else  // total column
					{
						sTotalColumnIndex = index;
					}
				}
				//sObjectArray[index] = strEffort;
				sObjectArray[index] = Double.toString(Task.parseToDouble(strEffort));
				index++;
			}

			if(!sLevelCheck){		
				Iterator it = mProjectEffortsIndexMap.keySet().iterator();
				//3. update the project's total efforts in the ret object list    
				while(it.hasNext())
				{
					String sProjectId = (String)it.next();
					int iIndex = (Integer)mProjectEffortsIndexMap.get(sProjectId);
					String sTotalProjectEfforts = mProjectEffortMap.get(sProjectId).toString();
					sObjectArray[iIndex] = ""+Double.parseDouble(numberFormat.format(Task.parseToDouble(sTotalProjectEfforts)));

				}
				//4. update the total efforts in the timesheet 
				/*DomainObject rootObject = DomainObject.newInstance(context,strObejectId);
			if(rootObject.getType(context).equals(ProgramCentralConstants.TYPE_WEEKLY_TIMESHEET)){
				sObjectArray[sTotalColumnIndex] = DomainObject.EMPTY_STRING;
			}else{*/
				sObjectArray[sTotalColumnIndex] = ""+sTotalEffortsOnADay;
				//}

				retEffortList.clear();
				for(int i=0;i<sObjectArray.length;i++)
				{
				if(sObjectArray[i].contains("."))
				{
					String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
					sObjectArray[i] = sObjectArray[i].replace('.',separator.charAt(0));
				}
					retEffortList.add(sObjectArray[i]);
				}	
			}			
		}			
		catch(Exception e) 
		{
			throw new MatrixException(e);
		}
		return retEffortList;
	}

	/**
	 * Updates the efforts submitted for a day.
	 * @param context the eMatrix <code>Context</code> object
	 * 
	 * @param args holds the following input arguments: 
	 * 		   paramMap - Contains a effort id and new value submitted for column.
	 * @return void
	 * @throws MatrixException
	 *             if the operation fails
	 */		
	public void updateDayEffort(Context context, String[] args, String strAttributeName) throws MatrixException 
	{
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			assert(null != programMap);
			Map paramMap = (Map) programMap.get("paramMap");
			assert(null != paramMap);
			String effortId = (String)paramMap.get("objectId");
			String newValue = (String)paramMap.get("New Value");
			validateEffort(context,newValue);
			DomainObject effortDomainObject = DomainObject.newInstance(context, effortId); 
			effortDomainObject.setAttributeValue(context, strAttributeName, newValue);
		}
		catch(Exception e)
		{
			throw new MatrixException(e.getMessage());
		}
	}

	/**
	 * This method is used to get total effort for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector containing the value for total effort for each effort.
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x            
	 * @author vf2  
	 */		
	public Vector getTotalEffort(Context context, String[] args) throws MatrixException 
	{
		Vector vTotalEffort = getDayEffort(context,args,ProgramCentralConstants.ATTRIBUTE_TOTAL_EFFORT);
		return vTotalEffort;
	}	

	/**
	 * This method is used to modify the weekdays attributes and total effort
	 * attribute for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: args[0] - Contains a
	 *            effort object.
	 * @return integer value of 0 or 1.
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */			
	public int triggerModifyEfforts(Context context, String[] args) throws MatrixException {
		int triggerValue = 0;
		try {
			String objectId  = args[0];	
			DomainObject dobj = DomainObject.newInstance(context, objectId);
			double totalEffortSum = 0;
			StringList strAttributeList = new StringList();
			strAttributeList.add(ATTRIBUTE_SUNDAY);
			strAttributeList.add(ATTRIBUTE_MONDAY);
			strAttributeList.add(ATTRIBUTE_TUESDAY);
			strAttributeList.add(ATTRIBUTE_WEDNESDAY);
			strAttributeList.add(ATTRIBUTE_THURSDAY);
			strAttributeList.add(ATTRIBUTE_FRIDAY);
			strAttributeList.add(ATTRIBUTE_SATURDAY);	        
			AttributeList attributeList = (AttributeList)dobj.getAttributeValues(context, strAttributeList);	        
			for(int i=0; i<attributeList.size(); i++) {
				totalEffortSum = totalEffortSum + Task.parseToDouble(((Attribute)attributeList.get(i)).getValue());
			}
			dobj.setAttributeValue(context, ATTRIBUTE_TOTAL_EFFORT, new Double(totalEffortSum).toString());	        
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return triggerValue;
	}  	

	/**
	 * This method is used to get approved or rejected date for effort and timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x            
	 * @author vf2  
	 */		
	public Vector getApprovedRejectedDate(Context context,String[] args)throws MatrixException {
		Vector stateDate = new Vector();
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			Map map = null;
			while (objectListIterator.hasNext()) {
				Map mapObject = (Map) objectListIterator.next();
				String id = (String)mapObject.get(DomainConstants.SELECT_ID);
				String strStateDate = "";
				String isTotalRow = (String)mapObject.get("isTotalRow");
				if(isTotalRow!=null && "true".equals(isTotalRow)) {
					stateDate.add("");
				} else {				
					StringList objSelectList = new StringList(2);
					objSelectList.add(SELECT_TYPE);
					objSelectList.add(SELECT_CURRENT);
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					DomainObject dobj = DomainObject.newInstance(context, id);
					try {
						map = dobj.getInfo(context, objSelectList);
					} finally {			
						ContextUtil.popContext(context);			
					}	
					String objType = (String)map.get(SELECT_TYPE);					
					if(objType.equalsIgnoreCase(TYPE_EFFORT)||objType.equalsIgnoreCase(TYPE_WEEKLY_TIMESHEET)) {					
						String currentState = (String)map.get(SELECT_CURRENT);											
						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						try {
							if(ProgramCentralConstants.STATE_EFFORT_REJECTED.equalsIgnoreCase(currentState)) {
								strStateDate = dobj.getInfo(context,"state["+ProgramCentralConstants.STATE_EFFORT_REJECTED+"].actual");
							} else if(ProgramCentralConstants.STATE_EFFORT_APPROVED.equalsIgnoreCase(currentState)){
								strStateDate = dobj.getInfo(context,"state["+ProgramCentralConstants.STATE_EFFORT_APPROVED+"].actual");
							}
						} finally {			
							ContextUtil.popContext(context);			
						}					
						if(currentState.equals(ProgramCentralConstants.STATE_EFFORT_REJECTED) || currentState.equals(ProgramCentralConstants.STATE_EFFORT_APPROVED)) {
							Date statesDate = eMatrixDateFormat.getJavaDate(strStateDate);
							java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
							strStateDate = sdf.format(statesDate);
						}

					} 
					stateDate.add(strStateDate);
				}	
			}					
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		} 	
		return stateDate;
	}	

	/**
	 * This method is used to get task icon for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x            
	 * @author vf2  
	 */		
	public Vector getTaskIcon(Context context,String[] args) throws MatrixException {
		Vector taskStatusIcon = null; 
		try {
			taskStatusIcon = new Vector();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map objectMap = null;
			String statusGif = ProgramCentralConstants.EMPTY_STRING;
			double totalEffort = 0.0;
			double remainingEffort = 0.0;
			double plannedEffort = 0.0;
			double approved_effort = 0.0;
			String taskId = null;
			AttributeList attributeList = null;
			MapList mpList = null;
			StringBuffer stbObjWhere = new StringBuffer();
			stbObjWhere.append("current=="+ProgramCentralConstants.STATE_EFFORT_APPROVED);	
			emxEffortManagementBase_mxJPO  emxEffortMgmt = new emxEffortManagementBase_mxJPO(context,args);
			Iterator objectListIterator = objectList.iterator();						
			while (objectListIterator.hasNext()) {
				objectMap = (Map) objectListIterator.next();
				String objectId = (String) objectMap.get(WeeklyTimesheet.SELECT_ID);
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				try 
				{
					DomainObject domainObject = DomainObject.newInstance(context, objectId);				
					if(domainObject.isKindOf(context, TYPE_EFFORT)) {
						StringList strAttributeList = new StringList();
						strAttributeList.add(ATTRIBUTE_TOTAL_EFFORT);
						strAttributeList.add(ATTRIBUTE_REMAINING_EFFORT);						

						plannedEffort = getPlanEffort(context,objectId,emxEffortMgmt);
						taskId = domainObject.getInfo(context, SELECT_TASK_ID);	
						if(null != taskId && taskId.trim().length()>0) {
							DomainObject dObjTask = DomainObject.newInstance(context, taskId);	
							mpList = dObjTask.getRelatedObjects(context,
									RELATIONSHIP_HAS_EFFORTS, TYPE_EFFORT,new StringList(DomainConstants.SELECT_ID), null,
									false, true, (short)0,stbObjWhere.toString(),"",0,null,null,null);
							for(int i=0;i<mpList.size();i++){
								Map effortMap = (Map)mpList.get(i);
								DomainObject dObjEffort = DomainObject.newInstance(context, effortMap.get(DomainConstants.SELECT_ID).toString());							
								approved_effort += Task.parseToDouble(dObjEffort.getAttributeValue(context,ATTRIBUTE_TOTAL_EFFORT));											
							}								
						}
						attributeList = domainObject.getAttributeValues(context, strAttributeList);					
						totalEffort = Task.parseToDouble(((Attribute)attributeList.get(0)).getValue());
						remainingEffort = Task.parseToDouble(((Attribute)attributeList.get(1)).getValue());

						if((totalEffort + approved_effort + remainingEffort) > plannedEffort) {
							statusGif = "<img src=\"images/iconStatusRed.gif\" border=\"0\" />";
						}														
						taskStatusIcon.addElement(statusGif);
					}
				}
				finally {
					ContextUtil.popContext(context);	
				}
				taskStatusIcon.addElement(ProgramCentralConstants.EMPTY_STRING);
			}			
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		} 	
		return taskStatusIcon;
	}

	/**
	 * Return the planned effort for the task and effort object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId String
	 * @param emxEffortMgmt ${CLASS:emxEffortManagementBase}
	 * @return double value of planned effort
	 * @throws Exception if the operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 */	
	private double getPlanEffort(Context context, String objectId,emxEffortManagementBase_mxJPO emxEffortMgmt) throws Exception {		
		double planEffort = 0.0;
		String sType = "";
		String type_TaskManagement=DomainConstants.TYPE_TASK_MANAGEMENT;
		String type_effort=ProgramCentralConstants.TYPE_EFFORT;
		String attribute_Duration=ProgramCentralConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION;
		String attribute_TaskFinishDate=ProgramCentralConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE;
		String relPattern=ProgramCentralConstants.RELATIONSHIP_HAS_EFFORTS;
		if (objectId != null && objectId.trim().length() > 0) {
			DomainObject bus  = DomainObject.newInstance(context, objectId);
			try{
				bus.open(context);
			}catch (FrameworkException Ex) {	
			}
			sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_effort)){
				Vector taskVec = emxEffortMgmt.getEffortIds(context,objectId,relPattern,type_TaskManagement,true,false,(short)1);
				String sTaskId = (String)taskVec.get(0);
				DomainObject boTask  = DomainObject.newInstance(context, sTaskId);
				try{
					boTask.open(context);
				} catch (FrameworkException Ex) {
					throw Ex;
				}

				Double dur = null;		            
				//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:start
				String sCommandStatement = "print bus $1 select $2 dump";
				String resultinHours =  MqlUtil.mqlCommand(context, sCommandStatement,sTaskId, "attribute["+attribute_Duration+"].unitvalue[h]"); 
				//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:End

				String fdate = boTask.getAttributeValue(context,attribute_TaskFinishDate);
				HashMap hmap = emxEffortMgmt.getAllocationData(context,sTaskId,objectId);
				HashMap AssgnedDateMap = emxEffortMgmt.getAssignedDateData(context,sTaskId,objectId);
				if(AssgnedDateMap!=null) {
					String assigneddate = (String)AssgnedDateMap.get("assigneddate");
					if(assigneddate!=null && assigneddate.trim().length() > 0 ) {
						long long_dur = DateUtil.computeDuration(new Date(assigneddate),new Date(fdate));
						if(long_dur == 0 )
							long_dur=1;
						String longStr = ""+long_dur;
						dur = new Double(longStr);
					}
				}
				if(hmap!=null) {
					planEffort = Task.parseToDouble(resultinHours);		                  		                 
				}
			}
		}	
		return planEffort;
	}

	/**
	 * This method is used to get value of total hours attribute for each
	 * timesheet object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getTotalHrsForTimesheet(Context context,String[] args) throws MatrixException {
		Vector totalHrs = new Vector();
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			while (objectListIterator.hasNext()) {
				Map mapObject = (Map) objectListIterator.next();
				String timesheetId = (String)mapObject.get(DomainConstants.SELECT_ID);
				if(timesheetId != null) {
					MapList Efforts = new MapList();
					WeeklyTimesheet weeklyTimesheet = new WeeklyTimesheet(timesheetId);
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						Efforts = weeklyTimesheet.getEfforts(context, null, null, null);
					}finally {			
						ContextUtil.popContext(context);			
					}	
					if(Efforts != null) {
						double totalEffort = 0.0;
						for(int i = 0; i<Efforts.size(); i++){
							Map effort = new HashMap();
							effort = (Map)Efforts.get(i);
							String sTaskId = (String)effort.get(SELECT_TASK_ID);
							if(ProgramCentralUtil.isNotNullString(sTaskId))
							{
								totalEffort = totalEffort + Task.parseToDouble((String)effort.get("attribute["+ATTRIBUTE_TOTAL_EFFORT+"]"));						
							}
						}
						String strTotalEffort = Double.toString(totalEffort);
						if(strTotalEffort.contains("."))
						{
							String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
							strTotalEffort = strTotalEffort.replace('.',separator.charAt(0));
						}
						totalHrs.add(strTotalEffort);	
					}					
				} 			
			}							
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		} 	
		return totalHrs;
	}

	/**
	 * This method is used to get value of total hours to approve attribute for
	 * each timesheet object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector
	 * @throws MatrixException
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */			
	public Vector getTotalHrsToApprove(Context context,String[] args) throws MatrixException {
		Vector totalHrsApprove = new Vector();
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			while (objectListIterator.hasNext()) {
				Map mapObject = (Map) objectListIterator.next();
				String timesheetId = (String)mapObject.get(DomainConstants.SELECT_ID);
				if(timesheetId != null) {
					MapList Efforts = new MapList();
					WeeklyTimesheet weeklyTimesheet = new WeeklyTimesheet(timesheetId);
					StringList strStateList = new StringList();
					strStateList.add(ProgramCentralConstants.STATE_EFFORT_SUBMIT);
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						Efforts = weeklyTimesheet.getEfforts(context, strStateList, null, null);
					} finally {			
						ContextUtil.popContext(context);			
					}	
					if(Efforts != null) {
						double totalEffort = 0.0;
						for(int i = 0; i<Efforts.size(); i++){
							Map effort = new HashMap();
							effort = (Map)Efforts.get(i);												
							totalEffort = totalEffort + Task.parseToDouble((String)effort.get("attribute["+ATTRIBUTE_TOTAL_EFFORT+"]"));
						}
						String strTotalEffort = Double.toString(totalEffort);
						if(strTotalEffort.contains("."))
						{
							String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
							strTotalEffort = strTotalEffort.replace('.',separator.charAt(0));
						}
						totalHrsApprove.add(strTotalEffort);
					}					
				}		
			}							
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		}	
		return totalHrsApprove;
	}	

	/**
	 * This method is used to check object type.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return StringList
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public StringList checObjectType(Context context,String[] args)
			throws Exception
			{
		StringList strList = new StringList();
		try 
		{
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");  
			String sPersonUserAgent = PropertyUtil.getSchemaProperty(context, "person_UserAgent");
			StringList slSelect =  new StringList();
			slSelect.addElement(DomainConstants.SELECT_CURRENT);

			for(int iterator = 0; iterator < objectList.size(); iterator++)
			{
				Map map = (Map)objectList.get(iterator);
				String objectID = (String)map.get("id");
				String strType = (String)map.get(DomainConstants.SELECT_TYPE);
				String strName = (String)map.get(DomainConstants.SELECT_NAME);

				if(strType!=null && strType.equalsIgnoreCase(TYPE_EFFORT))
				{
					String strCurrent = "";
					DomainObject domObject = DomainObject.newInstance(context, objectID);
					Map mInfo = domObject.getInfo(context, slSelect);
					strCurrent = (String)mInfo.get(DomainConstants.SELECT_CURRENT);

					if(!STATE_WEEKLY_TIMESHEET_APPROVED.equalsIgnoreCase(strCurrent) && 
							!STATE_WEEKLY_TIMESHEET_SUBMIT.equalsIgnoreCase(strCurrent))
					{
						strList.add(true);
					}
					else{
						strList.add(false);
					}
				}
				else{
					strList.add(false);
				}
			}
			return strList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e; 
		}
			}

	/**
	 * This method is used to check object isKindOf Phase.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return StringList
	 * @throws Exception
	 *             if the operation fails
	 * @since V6R2012
	 * @author ms9
	 */		
	public StringList checObjectTypeForRemainingEfforts(Context context,String[] args)
			throws Exception
			{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");  
			StringList strList = new StringList();

			String strType = "";
			String strName = "";
			for(int iterator = 0; iterator < objectList.size(); iterator++){
				Map map = (Map)objectList.get(iterator);
				String objectID = (String)map.get("id");
				DomainObject domObject = DomainObject.newInstance(context, objectID);
				strType = (String)map.get(DomainConstants.SELECT_TYPE);
				strName = (String)map.get(DomainConstants.SELECT_NAME);
				if(strType!=null && strType.equalsIgnoreCase(TYPE_EFFORT)){
					String strParentId = (String)map.get("to[hasEfforts].from.id");
					DomainObject domParentObj = DomainObject.newInstance(context,strParentId);
					String strCurrent = domObject.getInfo(context, DomainConstants.SELECT_CURRENT);
					if(!strCurrent.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_APPROVED) && 
							!strCurrent.equalsIgnoreCase(STATE_WEEKLY_TIMESHEET_SUBMIT) && !domParentObj.isKindOf(context, TYPE_PHASE))
						strList.add(true);
					else{
						strList.add(false);
					}
				}
				else{
					strList.add(false);
				}
			}
			return strList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e; 
		}
			}

	/**
	 * This method is used to check object type.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return StringList
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public StringList checObjectTypeForApprover(Context context,String[] args)
			throws Exception
			{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");  
			StringList strList = new StringList();

			String strType = "";
			String strName = "";

			for(int iterator = 0; iterator < objectList.size(); iterator++){
				Map map = (Map)objectList.get(iterator);
				String objectID = (String)map.get("id");
				DomainObject domObject = DomainObject.newInstance(context, objectID);
				strType = (String)map.get(DomainConstants.SELECT_TYPE);
				strName = (String)map.get(DomainConstants.SELECT_NAME);
				if(strType!=null && strType.equalsIgnoreCase(TYPE_EFFORT)){
					strList.add(true);
				}
				else{
					strList.add(false);
				}
			}
			return strList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e; 
		}
			}

	/**
	 * Gets the month name for displaying in the Labor Report Table column for
	 * Dynamic value
	 * 
	 * @param nMonth
	 *            The month number
	 * @return String containing month name
	 * @throws Exception
	 *             if operation fails
	 */
	public String getMonthName(Context context,int nCurrentMonthValue)  throws Exception {
		try { 
			String strI18MonthName = "";
			final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
			i18nNow loc = new i18nNow();
			String strLanguage=context.getSession().getLanguage();
			if(nCurrentMonthValue == 1){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jan");
			}
			if(nCurrentMonthValue == 2){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Feb");
			}
			if(nCurrentMonthValue == 3){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Mar");
			}
			if(nCurrentMonthValue == 4){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Apr");
			}
			if(nCurrentMonthValue == 5){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.May");
			}
			if(nCurrentMonthValue == 6){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jun");
			}
			if(nCurrentMonthValue == 7){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Jul");
			}
			if(nCurrentMonthValue == 8){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Aug");
			}
			if(nCurrentMonthValue == 9){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Sep");
			}
			if(nCurrentMonthValue == 10){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Oct");
			}
			if(nCurrentMonthValue == 11){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Nov");
			}
			if(nCurrentMonthValue == 12){
				strI18MonthName = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.Common.Dec");
			}
			return strI18MonthName;
		}
		catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}

	/**
	 * Gets Labor Report table data for Project Space
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails 
	 */
	public MapList getPhaseChildLaborReport(Context context, String[] args) throws Exception
	{ 
		MapList mlTaskList = new MapList();
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strProjectIds = (String) programMap.get("emxTableRowId");
			if((null ==strProjectIds)||("".equals(strProjectIds)))  
			{
				strProjectIds = (String) programMap.get("relId");
				if((null ==strProjectIds)||("".equals(strProjectIds)))  
				{
					strProjectIds = (String) programMap.get("emxParentIds");
				}
			}
			String[] strProjectIdArray = strProjectIds.split("\\|");
			String strYearType = (String) programMap.get("YearType");
			String strSelYear = (String) programMap.get("selYear");
			String strView = (String) programMap.get("selTimeline");

			int nFiscalYear = 0; 
			boolean isReportByPhase = true;

			for (int i = 0; i < strProjectIdArray.length; i++) {
				MapList mlPartialProjectData = getLaborReportForProject(context, strProjectIdArray[i],strYearType, strSelYear, strView, isReportByPhase);
				mlTaskList.addAll(mlPartialProjectData);
			}

			Map map = new HashMap();
			map.put("expandMultiLevelsJPO","true");
			mlTaskList.add(map);
		}
		catch (Exception exp) 
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
		return mlTaskList ;
	}    

	/**
	 * Returns the Labor Report data for provided project id and the filter
	 * values
	 * 
	 * @param context
	 * @param strYearType
	 * @param strSelYear
	 * @param strView
	 * @param strView
	 * @param isReportByPhase
	 * @return MapList
	 * @throws MatrixException
	 *             if operation fails
	 */
	private MapList getLaborReportForProject(Context context, String strProjectId, String strYearType, String strSelYear, String strView, boolean isReportByPhase) throws MatrixException 
	{
		MapList mlReportData = new MapList();
		try 
		{
			int indexOfFiscal = strYearType.indexOf("Fiscal");
			CalendarType calendarType = null;
			if(indexOfFiscal == -1)
			{
				calendarType = CalendarType.ENGLISH;
			}
			else if(indexOfFiscal != -1)
			{
				calendarType = CalendarType.FISCAL;
			}
			MapList mlEfforts = getEffortsForLaborReportForProject(context, strProjectId, strSelYear, strYearType);
			double iTotalEffort = 0.0;
			for (Iterator itrEfforts = mlEfforts.iterator(); itrEfforts.hasNext();) 
			{
				Map mapEffortInfo = (Map) itrEfforts.next();
				double strTotalEfforts = (Double)mapEffortInfo.get(SELECT_EFFORT);
				iTotalEffort+= strTotalEfforts;
			}
			MapList mlTasks = getAllTasksForLaborReportForProject(context, strProjectId, strSelYear, strYearType,iTotalEffort,null);
			MapList toBeRemoved = new MapList();

			String strWeekEndDate = "";
			String strState = "";
			Calendar cal = Calendar.getInstance();
			String strChildEffortVal = "";
			String strMonth = "";
			StringBuffer sbStrMonth = new StringBuffer();
			StringBuffer sbStrEfforts =  new StringBuffer();
			StringBuffer sbStrWeek = new StringBuffer();  		
			StringList strTaskEffortTotalVals = new StringList();
			StringList strEffortMonths = new StringList();
			StringList strEffortStates = new StringList();
			String delimiter="|";
			StringList strMonthNames =  new StringList();
			String strEffort = "";
			double dEffort = 0.0;
			Map mapTaskInfo = new HashMap();
			String strWeekNo = "";
			String strYear = "";
			StringBuffer sbDateAndEffort = new StringBuffer();
			com.matrixone.apps.program.fiscal.Iterator iterator = null;
			com.matrixone.apps.program.fiscal.Calendar fiscalCalendar = Helper.getCalendar(calendarType);
			Interval[] range = null;

			for (Iterator itrTasks = mlTasks.iterator(); itrTasks.hasNext();) 
			{
				mapTaskInfo = (Map) itrTasks.next();
				String isTaskAssigned = (String)mapTaskInfo.get("to["+RELATIONSHIP_ASSIGNED_TASKS+"]");
				String strType = (String)mapTaskInfo.get(SELECT_TYPE);
				if(isTaskAssigned.equalsIgnoreCase("False") && (strType.equalsIgnoreCase(TYPE_MILESTONE) 
						|| strType.equalsIgnoreCase(TYPE_MILESTONE))){
					toBeRemoved.add(mapTaskInfo);
					continue;
				}
				if(mapTaskInfo.containsKey(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT)) 
				{
					if(mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("java.lang.String"))
					{
						strWeekEndDate = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
						strState = (String) mapTaskInfo.get(SELECT_EFFORT_CURRENT_STATE);
						if(ProgramCentralConstants.STATE_EFFORT_APPROVED.equals(strState)) 
						{	
							Date dDate = new Date(strWeekEndDate); 
							cal.setTime(dDate);
							strChildEffortVal = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
							if((null !=strChildEffortVal)&&(!"".equals(strChildEffortVal)))    
							{
								dEffort += (Double)Task.parseToDouble(strChildEffortVal.trim());
								Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
								strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
								strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
								strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

								sbStrMonth.append(strMonth);
								sbStrMonth.append("-");
								sbStrMonth.append(strYear);
								sbStrMonth.append(delimiter);

								sbStrWeek.append(strWeekNo);
								sbStrWeek.append("-");
								sbStrWeek.append(strYear);
								sbStrWeek.append(delimiter);

								sbStrEfforts.append(strChildEffortVal);
								sbStrEfforts.append(delimiter);
							}
						}
					}
					else if (mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("matrix.util.StringList"))
					{	
						strTaskEffortTotalVals= (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
						strEffortMonths = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
						strEffortStates = (StringList) mapTaskInfo.get(SELECT_EFFORT_CURRENT_STATE);
						int ilength = strTaskEffortTotalVals.size();
						for (int i=0; i<ilength ; i++)
						{
							strState = (String) strEffortStates.get(i);
							if(ProgramCentralConstants.STATE_EFFORT_APPROVED.equals(strState)) 
							{	
								strWeekEndDate = (String)strEffortMonths.get(i);
								Date dDate = new Date(strWeekEndDate); 
								cal.setTime(dDate);

								iterator = fiscalCalendar.getIterator(IntervalType.WEEKLY);
								range = iterator.range(dDate, dDate);
								Interval it = range[0];
								strWeekNo = String.valueOf(it.getIntervalNumber());
								strYear = String.valueOf(it.getYear());

								iterator = fiscalCalendar.getIterator(IntervalType.MONTHLY);
								range = iterator.range(dDate, dDate);
								it = range[0];
								strMonth= String.valueOf(it.getIntervalNumber());
								sbStrMonth.append(strMonth);
								sbStrMonth.append("-");
								sbStrMonth.append(strYear);
								sbStrMonth.append(delimiter);

								sbStrWeek.append(strWeekNo);
								sbStrWeek.append("-");
								sbStrWeek.append(strYear);
								sbStrWeek.append(delimiter);

								strMonthNames.addElement(strMonth);
								strEffort  = (String)strTaskEffortTotalVals.get(i);
								sbStrEfforts.append(strEffort);
								sbStrEfforts.append(delimiter);
								dEffort += (Double)Task.parseToDouble(strEffort.trim());
							}
						}
					}
				}
			}
			for(int i=0;i<toBeRemoved.size();i++){
				mlTasks.removeAll(toBeRemoved);
			}
			//
			// Create project row in report data
			//
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_TYPE);
			slBusSelect.add(SELECT_NAME);

			DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
			Map mapProjectInfo = dmoProject.getInfo(context, slBusSelect);

			Map mapProjectRow = new HashMap();
			mapProjectRow.put(SELECT_LEVEL, "1");
			mapProjectRow.put(SELECT_ID, strProjectId);
			mapProjectRow.put(SELECT_TYPE, (String)mapProjectInfo.get(SELECT_TYPE));
			mapProjectRow.put(SELECT_NAME, (String)mapProjectInfo.get(SELECT_NAME));
			mapProjectRow.put("Total Efforts", iTotalEffort); 	
			mapProjectRow.put("Effort Week", sbStrWeek); 	
			mapProjectRow.put("Effort Month", sbStrMonth);
			mapProjectRow.put("Effort Values",sbStrEfforts );
			//
			// Create tasks row in report data
			//
			for (Iterator itrTasks = mlTasks.iterator(); itrTasks.hasNext();) 
			{
				mapTaskInfo = (Map) itrTasks.next();
				String strLevel = (String)mapTaskInfo.get(SELECT_LEVEL);

				if (!"1".equals(strLevel))
				{
					continue;
				}

				MapList mlTaskRow = getPhaseDataOfLaborReportForProject(context, mapTaskInfo, mlTasks, mlEfforts, strYearType, strSelYear);
				mlReportData.addAll(mlTaskRow);
			}
		}
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
		return mlReportData;
	}

	/**
	 * Returns the Labor Report data for provided Phase id and the filter values
	 * 
	 * @param context
	 * @param mapPhaseInfo
	 * @param mlTasks
	 * @param mlEfforts
	 * @param strYearType
	 * @param strSelYear
	 * @return MapList
	 * @throws MatrixException
	 *             if operation fails
	 */
	private MapList getPhaseDataOfLaborReportForProject(Context context, Map mapPhaseInfo, MapList mlTasks, MapList mlEfforts, String strYearType, String strSelYear) throws MatrixException
	{
		MapList mlPhaseData = new MapList();
		HashSet hsTasks = new HashSet();
		double dEffort = 0.0;
		Calendar cal = Calendar.getInstance();
		String strMonth = "";
		String strYear = "";
		StringBuffer sbStrMonth = new StringBuffer();
		StringBuffer sbStrPersonMonth = new StringBuffer();
		StringBuffer sbStrWeek = new StringBuffer();
		StringBuffer sbStrEfforts =  new StringBuffer();
		StringBuffer sbStrPersonEfforts =  new StringBuffer();
		StringBuffer sbStrPersonWeek = new StringBuffer();
		StringBuffer sbDateAndEfforts = new StringBuffer();
		String delimiter="|";
		StringList strMonthNames =  new StringList();
		StringList strEffortMonths = new StringList();
		String strWeekEndDate = "";
		String strState = "";
		StringList strEffortStates = new StringList();
		Map mpParent = new HashMap();
		String strParentEfforts = "";
		double dParentEffort = 0.0;
		StringBuffer sbParentMonth = new StringBuffer();
		StringBuffer sbParentEffortValues = new StringBuffer();
		StringBuffer sbParentWeek = new StringBuffer();
		String strChildEffortVal = "";
		String strEffort = "";
		StringList strTaskEffortTotalVals = new StringList();
		StringBuffer sbDateAndEffort = new StringBuffer();
		double strTotalEfforts = 0.0;
		String strWeekNo = "";
		try 
		{
			//
			// Create the data below phase row
			//
			int indexOfFiscal = strYearType.indexOf("Fiscal");
			CalendarType calendarType = null;
			if(indexOfFiscal == -1)
			{
				calendarType = CalendarType.ENGLISH;
			}
			else if(indexOfFiscal != -1)
			{
				calendarType = CalendarType.FISCAL;
			}

			com.matrixone.apps.program.fiscal.Iterator iterator = null;
			com.matrixone.apps.program.fiscal.Calendar fiscalCalendar = Helper.getCalendar(calendarType);
			Interval[] range = null;

			double iTotalEffort = 0.0;
			MapList mlTasksSubtree = filterTasksSubtree(mapPhaseInfo, mlTasks);
			MapList mlTasksSubTreeCopy = new MapList();
			mlTasksSubTreeCopy.addAll(mlTasksSubtree);
			for (Iterator itrTasksSubTree = mlTasksSubtree.iterator(); itrTasksSubTree.hasNext();) 
			{
				Map mapTaskInfo = (Map) itrTasksSubTree.next();
				strTotalEfforts = (Double)mapTaskInfo.get("TotalEffort");
				if(mapTaskInfo.containsKey(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT)) 
				{
					if(mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("java.lang.String"))
					{
						strWeekEndDate = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
						strState = (String) mapTaskInfo.get(SELECT_EFFORT_CURRENT_STATE);
						if(ProgramCentralConstants.STATE_EFFORT_APPROVED.equals(strState)) 
						{	
							StringBuffer sbDateAndEffort1 = (StringBuffer)mapTaskInfo.get("DateAndEffort");
							sbDateAndEffort.append(sbDateAndEffort1);
							boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);

							if(isEffortSelYear==true)
							{
								Date dDate = new Date(strWeekEndDate); 
								cal.setTime(dDate);
								strChildEffortVal = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);

								if((null !=strChildEffortVal)&&(!"".equals(strChildEffortVal)))    
								{
									dEffort += (Double)Task.parseToDouble(strChildEffortVal.trim());

									Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
									strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
									strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
									strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);


									sbStrMonth.append(strMonth);
									sbStrMonth.append("-");
									sbStrMonth.append(strYear);
									sbStrMonth.append(delimiter);


									sbStrWeek.append(strWeekNo);
									sbStrWeek.append("-");
									sbStrWeek.append(strYear);
									sbStrWeek.append(delimiter);
									sbStrEfforts.append(strChildEffortVal);
									sbStrEfforts.append(delimiter);
								}
							}
						}
					}

					else if (mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("matrix.util.StringList"))
					{	
						strTaskEffortTotalVals= (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
						strEffortMonths = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
						StringBuffer sbDateAndEffort1 = (StringBuffer)mapTaskInfo.get("DateAndEffort");
						strEffortStates = (StringList) mapTaskInfo.get(SELECT_EFFORT_CURRENT_STATE);
						sbDateAndEffort.append(sbDateAndEffort1);
						int ilength = strTaskEffortTotalVals.size();
						for (int i=0; i<ilength ; i++)
						{
							strState = (String) strEffortStates.get(i);
							if(ProgramCentralConstants.STATE_EFFORT_APPROVED.equals(strState)) 
							{	
								strWeekEndDate = (String)strEffortMonths.get(i);
								boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
								if(isEffortSelYear==true)
								{
									Date dDate = new Date(strWeekEndDate); 
									cal.setTime(dDate);

									Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
									strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
									strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
									strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

									sbStrMonth.append(strMonth);
									sbStrMonth.append("-");
									sbStrMonth.append(strYear);
									sbStrMonth.append(delimiter);

									sbStrWeek.append(strWeekNo);
									sbStrWeek.append("-");
									sbStrWeek.append(strYear);
									sbStrWeek.append(delimiter);

									strMonthNames.addElement(strMonth);
									strEffort  = (String)strTaskEffortTotalVals.get(i);
									sbStrEfforts.append(strEffort);
									sbStrEfforts.append(delimiter);
									dEffort += (Double)Task.parseToDouble(strEffort.trim());
								}
							}
						}
					}
				}
			}
			iTotalEffort+= strTotalEfforts;
			//
			// Create the phase row
			//
			Map mapPhaseInfoCopy = shalowCopy(mapPhaseInfo);
			String strPhaseId = (String) mapPhaseInfoCopy.get(SELECT_ID);
			mapPhaseInfoCopy.put(SELECT_LEVEL, "2");
			mapPhaseInfoCopy.put("Total Efforts", dEffort); 		
			mapPhaseInfoCopy.put("Effort Month", sbStrMonth);
			mapPhaseInfoCopy.put("Effort Values",sbStrEfforts );
			mapPhaseInfoCopy.put("Effort Week",sbStrWeek );
			mapPhaseInfoCopy.put("DateAndEffort",sbDateAndEffort);
			mapPhaseInfoCopy.put("Total Efforts1",getTotalForReport(sbDateAndEffort));
			mlPhaseData.add(mapPhaseInfoCopy);
			strMonthNames.clear();
			StringList slEffortMonths = new StringList();
			strWeekEndDate = "";
			mpParent = new HashMap();
			strParentEfforts = "";
			dParentEffort = 0.0;

			strChildEffortVal = "";
			strEffort = "";
			StringList slTaskEffortTotalVals = new StringList();
			strTotalEfforts = 0.0;
			String strPrevOwner = null;
			StringList slTasksAdded = new StringList();
			String strOwnerCopy = "";
			double iEffort = 0.0;
			String strEffortOriginator = "";
			String strOriginator = "";
			int ssize=0;
			for (Iterator itrEfforts = mlEfforts.iterator(); itrEfforts.hasNext();) 
			{

				dEffort = 0.0;
				sbStrMonth = new StringBuffer();
				sbStrWeek = new StringBuffer();
				sbStrPersonMonth = new StringBuffer();
				sbStrPersonWeek = new StringBuffer();
				sbStrPersonEfforts = new StringBuffer();
				sbDateAndEfforts = new StringBuffer();
				sbStrEfforts =  new StringBuffer();
				Map mapEffortInfo = (Map) itrEfforts.next();
				String strOwner = (String)mapEffortInfo.get(SELECT_ORIGINATOR);
				if (strPrevOwner == null || (!strOwner.equals(strPrevOwner)))
				{
					slTasksAdded.clear();
					String strTaskId = (String)mapEffortInfo.get(SELECT_EFFORT_PARENT_TASK_ID);
					Map mapTaskInfo = filterTaskInfo(strTaskId, mlTasksSubTreeCopy);
					String strEffortVal = Double.toString((Double)mapEffortInfo.get(SELECT_ATTRIBUTE_TOTAL_EFFORT));										
					if (mapTaskInfo != null)
					{
						//
						// Create person row
						//
						StringBuffer sbPersonDateAndEfforts = new StringBuffer();
						String strParentTask = (String)mapTaskInfo.get("parentTask");
						Map mapPersonRow = new HashMap();
						mapPersonRow.put(SELECT_LEVEL, "3");
						mapPersonRow.put(SELECT_NAME, PersonUtil.getFullName(context, strOwner));
						mapPersonRow.put(SELECT_ID, PersonUtil.getPersonObjectID(context, strOwner));
						mapPersonRow.put("Total Efforts", iEffort);
						mapPersonRow.put("Effort Month", sbStrPersonMonth);
						mapPersonRow.put("Effort Values",sbStrPersonEfforts);
						mapPersonRow.put("Effort Week",sbStrPersonWeek);
						mapPersonRow.put("DateAndEffort",sbDateAndEfforts);
						mapPersonRow.put("Total Efforts1",getTotalForReport(sbDateAndEfforts));
						mlPhaseData.add(mapPersonRow);
						ssize = mlPhaseData.size();
						strPrevOwner = strOwner;
						//
						// Create a task row
						//
						String strChildTaskId = (String) mapTaskInfo.get(SELECT_ID);
						if(mapTaskInfo.containsKey(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT)) 
						{								
							if(mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("java.lang.String"))
							{
								strWeekEndDate = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
								boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
								if(isEffortSelYear==true)
								{
									StringList effortList= new StringList();
									DomainObject taskDo = DomainObject.newInstance(context,strParentTask);
									StringList slTaskList = taskDo.getInfoList(context, "from["+RELATIONSHIP_SUBTASK+"].to.id");
									slTaskList.add(strParentTask);

									for(int k=0;k<mlEfforts.size();k++){
										Map effortMap = (Map)mlEfforts.get(k);
										if(strParentTask.equals((String)effortMap.get(SELECT_EFFORT_PARENT_TASK_ID))|| strTaskId.equals((String)effortMap.get(SELECT_EFFORT_PARENT_TASK_ID))){
											effortList.add((String)effortMap.get(SELECT_ID));
										}	
									}
									for(int m=0;m<effortList.size();m++){
										sbDateAndEfforts.append(getWeekData(context, (String)effortList.get(m), strSelYear, strYearType, strOwner));
									}

									StringList slAllEffort = new StringList();
									for (Iterator itrEfforts1 = mlEfforts.iterator(); itrEfforts1.hasNext();) 
									{
										Map tempMap = (Map) itrEfforts1.next();
										if(slTaskList.contains((String)tempMap.get(SELECT_EFFORT_PARENT_TASK_ID))){
											slAllEffort.add((String)tempMap.get(SELECT_ID));
										}
									}
									for(int p=0;p<slAllEffort.size();p++){
										sbPersonDateAndEfforts.append(getWeekData(context, (String)slAllEffort.get(p), strSelYear, strYearType, strOwner));
									}

									Date dDate = new Date(strWeekEndDate); 
									cal.setTime(dDate);
									strChildEffortVal = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
									strEffortOriginator = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_ORIGINATOR);

									if((null !=strChildEffortVal)&&(!"".equals(strChildEffortVal))&& (strEffortOriginator.equals(strOwner)) )    
									{
										dEffort += (Double)Task.parseToDouble(strChildEffortVal.trim());

										Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
										strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
										strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
										strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

										sbStrMonth.append(strMonth);
										sbStrMonth.append("-");
										sbStrMonth.append(strYear);
										sbStrMonth.append(delimiter);

										sbStrWeek.append(strWeekNo);
										sbStrWeek.append("-");
										sbStrWeek.append(strYear);
										sbStrWeek.append(delimiter);

										sbStrEfforts.append(strChildEffortVal);
										sbStrEfforts.append(delimiter);
									}
								}
							}
							else if (mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("matrix.util.StringList"))
							{	
								slTaskEffortTotalVals= (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
								StringList strEffortOriginators = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_ORIGINATOR);
								slEffortMonths = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);

								StringBuffer sbDateAndEfforts1 = new StringBuffer();						
								StringList effortList = new StringList();
								DomainObject taskDo = DomainObject.newInstance(context,strParentTask);
								StringList slTaskList = taskDo.getInfoList(context, "from["+RELATIONSHIP_SUBTASK+"].to.id");
								slTaskList.add(strParentTask);
								for(int k=0;k<mlEfforts.size();k++){
									Map effortMap = (Map)mlEfforts.get(k);
									if(strParentTask.equals((String)effortMap.get(SELECT_EFFORT_PARENT_TASK_ID)) || strTaskId.equals((String)effortMap.get(SELECT_EFFORT_PARENT_TASK_ID))){
										effortList.add((String)effortMap.get(SELECT_ID));
									}	

								}
								for(int m=0;m<effortList.size();m++){
									sbDateAndEfforts.append(getWeekData(context, (String)effortList.get(m), strSelYear, strYearType, strOwner));
								}		
								StringList slAllEffort = new StringList();
								for (Iterator itrEfforts1 = mlEfforts.iterator(); itrEfforts1.hasNext();) 
								{
									Map tempMap = (Map) itrEfforts1.next();
									if(slTaskList.contains((String)tempMap.get(SELECT_EFFORT_PARENT_TASK_ID))){
										slAllEffort.add((String)tempMap.get(SELECT_ID));
									}
								}
								for(int p=0;p<slAllEffort.size();p++){
									sbPersonDateAndEfforts.append(getWeekData(context, (String)slAllEffort.get(p), strSelYear, strYearType, strOwner));
								}
								int ilength = strEffortOriginators.size();
								for (int i=0; i<ilength ; i++)
								{
									strWeekEndDate = (String)slEffortMonths.get(i);
									boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
									if(isEffortSelYear==true)
									{
										Date dDate = new Date(strWeekEndDate); 
										cal.setTime(dDate);
										strOriginator = (String)strEffortOriginators.get(i);

										if(strOriginator.equalsIgnoreCase(strOwner)){

											Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
											strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
											strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
											strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

											sbStrMonth.append(strMonth);
											sbStrMonth.append("-");
											sbStrMonth.append(strYear);
											sbStrMonth.append(delimiter);

											sbStrWeek.append(strWeekNo);
											sbStrWeek.append("-");
											sbStrWeek.append(strYear);
											sbStrWeek.append(delimiter);								             

											strMonthNames.addElement(strMonth);
											strEffort  = (String)slTaskEffortTotalVals.get(i);
											sbStrEfforts.append(strEffort);
											sbStrEfforts.append(delimiter);
											dEffort += (Double)Task.parseToDouble(strEffort.trim());
										}
									}
								}
							}
						}
						mapTaskInfo.put(SELECT_LEVEL, "4");
						mapTaskInfo.put("hasChildren", "false");
						mapTaskInfo.put("Total Efforts",dEffort);
						mapTaskInfo.put("Effort Month",sbStrMonth );
						mapTaskInfo.put("Effort Values",sbStrEfforts );
						mapTaskInfo.put("Effort Week",sbStrWeek );						
						mapTaskInfo.put("DateAndEffort",sbDateAndEfforts);
						mapTaskInfo.put("Total Efforts1",getTotalForReport(sbDateAndEfforts));


						mpParent = (Map)mlPhaseData.get(ssize-1);
						dParentEffort = (Double)mpParent.get("Total Efforts");
						sbParentMonth = (StringBuffer)mpParent.get("Effort Month");
						sbParentEffortValues= (StringBuffer)mpParent.get("Effort Values");
						sbParentWeek = (StringBuffer)mpParent.get("Effort Week");

						dParentEffort+=dEffort;
						sbParentMonth.append(sbStrMonth);
						sbParentEffortValues.append(sbStrEfforts);
						sbParentWeek.append(sbStrWeek);

						mpParent.put("Total Efforts", dParentEffort);
						mpParent.put("Effort Month", sbParentMonth);
						mpParent.put("Effort Values", sbParentEffortValues);
						mpParent.put("Effort Week", sbParentWeek);
						mpParent.put("DateAndEffort",sbPersonDateAndEfforts);
						mpParent.put("Total Efforts1",getTotalForReport(sbPersonDateAndEfforts));						
						mlPhaseData.add(mapTaskInfo);
					}
					else 
					{
						//Do nothing!
					}
					slTasksAdded.add(strTaskId);
				}
				else 
				{
					String strTaskId = (String)mapEffortInfo.get(SELECT_EFFORT_PARENT_TASK_ID);
					Map mapTaskInfo = filterTaskInfo(strTaskId, mlTasksSubtree);

					if (mapTaskInfo != null && !slTasksAdded.contains(strTaskId))
					{
						//
						// Create a task row
						//
						String strChildTaskId = (String) mapTaskInfo.get(SELECT_ID);
						if(mapTaskInfo.containsKey(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT)) 
						{
							if(mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("java.lang.String"))
							{
								StringBuffer sbTempDateAndEfforts = new StringBuffer();
								sbTempDateAndEfforts = getWeekData(context, (String)mapEffortInfo.get(SELECT_ID), strSelYear, strYearType, strOwner);
								sbDateAndEfforts.append(sbTempDateAndEfforts);
								strWeekEndDate = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
								boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);

								if(isEffortSelYear==true)
								{
									Date dDate = new Date(strWeekEndDate); 
									cal.setTime(dDate);
									strChildEffortVal = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
									strEffortOriginator = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_ORIGINATOR);

									if((null !=strChildEffortVal)&&(!"".equals(strChildEffortVal))&& (strEffortOriginator.equals(strOwner)) )    
									{
										dEffort += (Double)Task.parseToDouble(strChildEffortVal.trim());

										Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
										strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
										strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
										strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

										sbStrMonth.append(strMonth);
										sbStrMonth.append("-");
										sbStrMonth.append(strYear);
										sbStrMonth.append(delimiter);

										sbStrWeek.append(strWeekNo);
										sbStrWeek.append("-");
										sbStrWeek.append(strYear);
										sbStrWeek.append(delimiter);
										sbStrEfforts.append(strChildEffortVal);
										sbStrEfforts.append(delimiter);
									}
								}
							}
							else if (mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("matrix.util.StringList"))
							{	
								slEffortMonths = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
								slTaskEffortTotalVals= (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
								StringList strEffortOriginators = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_ORIGINATOR);
								StringBuffer sbfDateAndEffort = (StringBuffer) mapTaskInfo.get("DateAndEffort");
								StringList slDateAndEffort  = FrameworkUtil.split(sbfDateAndEffort.toString(), "|");
								StringList slSepDateAndEffort = new StringList();
								StringBuffer sbDateEffort =  new StringBuffer();
								int index = 0;
								for(int nCount = 0; nCount<slDateAndEffort.size()-1;nCount++)
								{
									index++;
									sbDateEffort.append(slDateAndEffort.get(nCount));
									if(nCount!=slDateAndEffort.size()-1)
									{
										sbDateEffort.append("|");
									}
									if(index%7 == 0)
									{
										slSepDateAndEffort.add(sbDateEffort);
										sbDateEffort= new StringBuffer();
									}								
								}
								int length = slSepDateAndEffort.size();
								int ilength = strEffortOriginators.size();
								for (int i=0; i<ilength ; i++)
								{
									strWeekEndDate = (String)slEffortMonths.get(i);
									boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
									if(isEffortSelYear==true)
									{									
										Date dDate = new Date(strWeekEndDate); 
										cal.setTime(dDate);
										strOriginator = (String)strEffortOriginators.get(i);
										if(strOriginator.equalsIgnoreCase(strOwner)){
											sbDateAndEfforts.append(slSepDateAndEffort.get(i));

											Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
											strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
											strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
											strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);


											sbStrMonth.append(strMonth);
											sbStrMonth.append("-");
											sbStrMonth.append(strYear);
											sbStrMonth.append(delimiter);

											sbStrWeek.append(strWeekNo);
											sbStrWeek.append("-");
											sbStrWeek.append(strYear);
											sbStrWeek.append(delimiter);

											strMonthNames.addElement(strMonth);
											strEffort  = (String)slTaskEffortTotalVals.get(i);
											sbStrEfforts.append(strEffort);
											sbStrEfforts.append(delimiter);
											dEffort += (Double)Task.parseToDouble(strEffort.trim());
										}
									}
								}
							}
							mapTaskInfo.put(SELECT_LEVEL, "4");
							mapTaskInfo.put("hasChildren", "false");
							mapTaskInfo.put("Total Efforts",dEffort );
							mapTaskInfo.put("Effort Month",sbStrMonth );
							mapTaskInfo.put("Effort Values",sbStrEfforts );
							mapTaskInfo.put("Effort Week",sbStrWeek );
							mapTaskInfo.put("DateAndEffort",sbDateAndEfforts );
							mapTaskInfo.put("Total Efforts1",getTotalForReport(sbDateAndEfforts));		


							mpParent = (Map)mlPhaseData.get(ssize-1);
							dParentEffort = (Double)mpParent.get("Total Efforts");
							sbParentMonth = (StringBuffer)mpParent.get("Effort Month");
							sbParentEffortValues= (StringBuffer)mpParent.get("Effort Values");
							sbParentWeek = (StringBuffer)mpParent.get("Effort Week");
							dParentEffort+=dEffort;
							sbParentMonth.append(sbStrMonth);
							sbParentEffortValues.append(sbStrEfforts);
							sbParentWeek.append(sbStrWeek);

							mpParent.put("Total Efforts", dEffort);
							mpParent.put("Effort Month", sbParentMonth);
							mpParent.put("Effort Values", sbParentEffortValues);
							mpParent.put("Effort Week", sbParentWeek);		

							mlPhaseData.add(mapTaskInfo);
						}
					}
					else 
					{
						//Do nothing!
					}
					slTasksAdded.add(strTaskId);
				}
				strOwnerCopy = strOwner;
			}
		}
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
		return mlPhaseData;
	}

	/**
	 * @param strTaskId
	 * @param mlTasksSubtree
	 * @return Map
	 * @throws MatrixException
	 */
	private Map filterTaskInfo(String strTaskId, MapList mlTasksSubtree) throws MatrixException {
		for (Iterator itrTasks = mlTasksSubtree.iterator(); itrTasks.hasNext();) {
			Map mapTaskInfo = (Map) itrTasks.next();

			String strCurrentTaskId = (String)mapTaskInfo.get(SELECT_ID);
			if (strCurrentTaskId.equals(strTaskId))
			{

				return shalowCopy(mapTaskInfo);
			}
		}

		return null;
	}

	/**
	 * @param mapPhaseInfo
	 * @param mlTasks
	 * @return MapList
	 * @throws MatrixException
	 */
	private MapList filterTasksSubtree(Map mapPhaseInfo, MapList mlTasks) throws MatrixException
	{
		MapList mlTaskSubtree = new MapList();
		try
		{
			String strPhaseId = (String)mapPhaseInfo.get(SELECT_ID);
			String [] strTaskEffortTotalVals1 = new String [10];
			String strEffort = "";
			String [] strSplit=new String [5];
			String delimiter1="\\,";
			for (Iterator itrTasks = mlTasks.iterator(); itrTasks.hasNext();) {
				Map mapTaskInfo = (Map) itrTasks.next();
				String strParentTask = "";
				double dEffort = 0.0;
				String strTaskId = (String)mapTaskInfo.get(SELECT_ID);
				String strTaskParentId = (String)mapTaskInfo.get(SELECT_TASK_PARENT_ID);
				for (int i=0;i<mlTasks.size();i++) {
					Map mpTaskInfo = (Map) mlTasks.get(i);
					String taskId = (String)mpTaskInfo.get(SELECT_ID);
					if(strTaskParentId.equalsIgnoreCase(taskId)){
						strParentTask = (String)mpTaskInfo.get("parentTask");
						break;
					}
				}
				String strTaskEffortTotalval = "";
				if((null !=strParentTask)&&(!"".equals(strParentTask)))    
				{
					strTaskId = strParentTask;
				}
				if (strPhaseId.equals(strTaskId) || strPhaseId.equals(strTaskParentId))
				{
					mapTaskInfo.put("parentTask", strPhaseId);
					if(mapTaskInfo.containsKey(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT)) 
					{
						if(mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("java.lang.String"))
						{
							strTaskEffortTotalval= (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);

							dEffort += (Double)Task.parseToDouble(strTaskEffortTotalval.trim());

							mapTaskInfo.put("TotalEffort",dEffort );	
						}
						else if (mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("matrix.util.StringList"))
						{		
							StringList strTaskEffortTotalVals= (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
							int ilength = strTaskEffortTotalVals.size();
							for (int i=0; i<ilength ; i++)
							{
								strEffort  = (String)strTaskEffortTotalVals.get(i);
								dEffort += (Double)Task.parseToDouble(strEffort.trim());
							}
							mapTaskInfo.put("TotalEffort",dEffort );	
						}		
					}
					mapTaskInfo.put("TotalEffort",dEffort );
					mlTaskSubtree.add(shalowCopy(mapTaskInfo));
				}
			}
		}
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
		return mlTaskSubtree;

	}

	/**
	 * 
	 * @param mapTaskInfo
	 * @return
	 */

	private Map shalowCopy(Map mapTaskInfo) {

		Map mapCopy = new HashMap();

		for (Iterator itrKeys = mapTaskInfo.keySet().iterator(); itrKeys.hasNext();) {
			Object objKey = (Object) itrKeys.next();
			mapCopy.put(objKey, mapTaskInfo.get(objKey));
		}

		return mapCopy;
	}

	/**
	 * This method is used to get all tasks for a project.
	 * 
	 * @param context
	 * @param strProjectId
	 * @return MapList
	 * @throws MatrixException
	 */	
	private MapList getAllTasksForLaborReportForProject(Context context, String strProjectId,String strSelYear, String strYearType,double dbTotal,String strPersonName) throws MatrixException
	{
		try {
			StringList slBusSelect = new StringList();				
			slBusSelect.add(SELECT_ID);
			slBusSelect.add(SELECT_TYPE);
			slBusSelect.add(SELECT_NAME);
			slBusSelect.add(SELECT_TASK_PARENT_ID);
			slBusSelect.add(SELECT_PARENT_TYPE);
			slBusSelect.add(SELECT_IS_PARENT_TYPE_KINDOF_PROJECT);
			slBusSelect.add(SELECT_IS_PARENT_TYPE_KINDOF_PHASE);
			slBusSelect.add(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
			slBusSelect.add(SELECT_TASK_TO_EFFORT_ORIGINATOR);
			slBusSelect.add(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
			slBusSelect.add(SELECT_EFFORT_CURRENT_STATE);
			slBusSelect.add("to["+RELATIONSHIP_ASSIGNED_TASKS+"]");

			DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
			StringList slRelSelect = new StringList();
			String strRelationshipPattern = RELATIONSHIP_SUBTASK;
			String strTypePattern = TYPE_TASK_MANAGEMENT;
			final boolean GET_TO = true;
			final boolean GET_FROM = true;
			short recurseToLevel = 0;
			String strObjectWhere = null;
			String strRelWhere = null;
			int nLimit = 0;
			MapList mlTasks = new MapList();
			if(dbTotal>0.0){
				mlTasks = dmoProject.getRelatedObjects(context, strRelationshipPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, recurseToLevel, strObjectWhere, strRelWhere, nLimit);
			}
			MapList taskMapList = new MapList();
			if(mlTasks.size()>0){			
				for(int i=0;i<mlTasks.size();i++) {	
					StringBuffer sbDateAndEffort = new StringBuffer();
					StringList totalEffortList = new StringList();
					Map taskMap = (Map)mlTasks.get(i);
					if(!taskMap.containsKey(SELECT_EFFORT_CURRENT_STATE)){
						taskMapList.add(mlTasks.get(i));
						continue;
					}									
					if(taskMap.get(SELECT_EFFORT_CURRENT_STATE).getClass().getName().equals("matrix.util.StringList")){
						StringList strStateList = (StringList)taskMap.get(SELECT_EFFORT_CURRENT_STATE);
						StringList strTotalEffortList = (StringList)taskMap.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);																		
						if(strStateList!=null) {						
							String effortId = null;
							double totalEffort = 0.0;
							DomainObject dObjTask = DomainObject.newInstance(context, (String)taskMap.get(SELECT_ID));
							MapList mpList = dObjTask.getRelatedObjects(context,
									RELATIONSHIP_HAS_EFFORTS, TYPE_EFFORT,new StringList(DomainConstants.SELECT_ID), null,
									false, true, (short)0,null,"",0,null,null,null);																													
							for(int j=0;j<strStateList.size();j++) {
								if(strStateList.get(j).equals(ProgramCentralConstants.STATE_EFFORT_APPROVED)) {	
									Map EffortMap = (Map)mpList.get(j);									
									effortId = (String)EffortMap.get(SELECT_ID);			
									StringBuffer sbDateAndEffort1= getWeekData(context, effortId,strSelYear,strYearType,strPersonName);	
									sbDateAndEffort.append(sbDateAndEffort1);								
									totalEffortList.add(strTotalEffortList.get(j));									
								} else {							
									Map EffortMap = (Map)mpList.get(j);									
									effortId = (String)EffortMap.get(SELECT_ID);			
									StringBuffer sbDateAndEffort1= getWeekData(context, effortId,strSelYear,strYearType,strPersonName,false);	
									sbDateAndEffort.append(sbDateAndEffort1);								
									totalEffortList.add("0.0");
								}
							}
						}											
						taskMap.put(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT,totalEffortList);	
						taskMap.put("DateAndEffort",sbDateAndEffort);	
						taskMap.put("Total Efforts1",getTotalForReport(sbDateAndEffort));
					} else if(taskMap.get(SELECT_EFFORT_CURRENT_STATE).getClass().getName().equals("java.lang.String")){
						String strState = (String)taskMap.get(SELECT_EFFORT_CURRENT_STATE);
						String strTotalEffort = (String)taskMap.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
						String strtolEffort = "0.0";
						StringBuffer sbDateAndEffort1= new StringBuffer();	
						if(strState!=null) {
							String effortId = null;								
							DomainObject dObjTask = DomainObject.newInstance(context, (String)taskMap.get(SELECT_ID));
							MapList mpList = dObjTask.getRelatedObjects(context,
									RELATIONSHIP_HAS_EFFORTS, TYPE_EFFORT,new StringList(DomainConstants.SELECT_ID), null,
									false, true, (short)0,null,"",0,null,null,null);
							for(int m=0; m<mpList.size(); m++){
								if(strState.equals(ProgramCentralConstants.STATE_EFFORT_APPROVED)) {	
									Map EffortMap = (Map)mpList.get(m);									
									effortId = (String)EffortMap.get(SELECT_ID);																	
									sbDateAndEffort1= getWeekData(context, effortId,strSelYear,strYearType,strPersonName);
									sbDateAndEffort.append(sbDateAndEffort1);
									strtolEffort = strTotalEffort;
								} else {							
									Map EffortMap = (Map)mpList.get(m);									
									effortId = (String)EffortMap.get(SELECT_ID);																	
									sbDateAndEffort1= getWeekData(context, effortId,strSelYear,strYearType,strPersonName,false);
									sbDateAndEffort.append(sbDateAndEffort1);
									strtolEffort = "0.0";
								}	
							}
						}	
						taskMap.put(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT,strtolEffort);
						taskMap.put("DateAndEffort",sbDateAndEffort1);	
						taskMap.put("Total Efforts1",getTotalForReport(sbDateAndEffort1));

					}
					taskMapList.add(taskMap);
				}
			}			
			return taskMapList;
		} 
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}	

	/**
	 * This method is used to get efforts from a project.
	 * 
	 * @param context
	 * @param strProjectId
	 * @param strSelYear
	 * @param strYearType
	 * @return MapList
	 * @throws MatrixException
	 */
	private MapList getEffortsForLaborReportForProject(Context context, String strProjectId,String strSelYear, String strYearType) throws MatrixException 
	{
		try {
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ID);
			slBusSelect.add(SELECT_EFFORT_PARENT_TASK_ID);
			slBusSelect.add(SELECT_ORIGINATOR);
			slBusSelect.add(ATTRIBUTE_MONDAY);
			slBusSelect.add(ATTRIBUTE_TUESDAY);
			slBusSelect.add(ATTRIBUTE_WEDNESDAY);
			slBusSelect.add(ATTRIBUTE_THURSDAY);
			slBusSelect.add(ATTRIBUTE_FRIDAY);
			slBusSelect.add(ATTRIBUTE_SATURDAY);
			slBusSelect.add(ATTRIBUTE_SUNDAY);
			slBusSelect.add(SELECT_ATTRIBUTE_TOTAL_EFFORT);
			slBusSelect.add(SELECT_ATTRIBUTE_ENDING_DATE);

			DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
			StringList slRelSelect = new StringList();
			String strRelationshipPattern = RELATIONSHIP_EFFORTS;
			String strTypePattern = TYPE_EFFORT;
			final boolean GET_TO = true;
			final boolean GET_FROM = true;
			short recurseToLevel = 1;
			StringBuffer strObjectWhere = new StringBuffer();
			String strRequestStartDate = "";
			String strRequestEndDate = "";		
			Date dtStartDate = null;
			Date dtFinishDate = null;
			String strTime= " 12:00:00 PM";
			MapList mlEfforts = new MapList();
			SimpleDateFormat sdf;
			int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
			sdf = new SimpleDateFormat("MM/dd/yyyy");
			int iSelYear = Integer.valueOf(strSelYear.trim());

			CalendarType calendarType= getCalendarType(context, strYearType);
			Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(strSelYear.trim())); 
			dtStartDate = interval.getStartDate();
			dtFinishDate = interval.getEndDate();

			strRequestStartDate = sdf.format(dtStartDate);
			dtFinishDate = getDurationToModifyWeekEndDate(context,false, true , dtFinishDate);

			strRequestEndDate = sdf.format(dtFinishDate.getTime());

			strObjectWhere.append(SELECT_ATTRIBUTE_ENDING_DATE + ">=\"" +strRequestStartDate+"\" && "+SELECT_ATTRIBUTE_ENDING_DATE + "<=\"" +strRequestEndDate+"\"");
			strObjectWhere.append(" && ");
			strObjectWhere.append(SELECT_CURRENT +"==\""+ProgramCentralConstants.STATE_EFFORT_APPROVED +"\"");
			String strRelWhere = null;
			int nLimit = 0;
			double dTotal = 0.0;
			mlEfforts = dmoProject.getRelatedObjects(context, strRelationshipPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, recurseToLevel, strObjectWhere.toString(), strRelWhere, nLimit);
			for (Iterator itrEfforts = mlEfforts.iterator(); itrEfforts.hasNext();) {
				Map mapEffortInfo = (Map) itrEfforts.next();
				String strTotalEffort = (String) mapEffortInfo.get(SELECT_ATTRIBUTE_TOTAL_EFFORT);						
				String strEffortId = (String) mapEffortInfo.get(SELECT_ID);
				double totalEffort = getTotalEffortForWeek(context,strEffortId,strTotalEffort,strSelYear,strYearType);
				mapEffortInfo.put(SELECT_EFFORT, totalEffort);
				mapEffortInfo.put(SELECT_ATTRIBUTE_TOTAL_EFFORT, totalEffort);
			}				
			mlEfforts.sort(SELECT_ORIGINATOR, "ascending", "String");					
			return mlEfforts;
		} 
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}	

	private double getTotalEffortForWeek(Context context,String effortId, String totalEffort,String strSelYear, String strYearType) throws Exception{
		Date dtStartDate = null;
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

		CalendarType calendarType= getCalendarType(context, strYearType);
		Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(strSelYear.trim())); 
		dtStartDate = interval.getStartDate();
		double wkTotalEffort = 0.0;
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.setTime(dtStartDate);
		calStartDate.set(Calendar.HOUR,0);
		calStartDate.set(Calendar.HOUR_OF_DAY,0);
		calStartDate.set(Calendar.MINUTE,0);
		calStartDate.set(Calendar.SECOND,0);
		calStartDate.set(Calendar.MILLISECOND,0);
		Calendar calWkEndDate = Calendar.getInstance();	
		StringList strEffortList = new StringList();
		//MODIFIED::PA4:13-Jun-2011:IR-104838V6R2012x:START
		Locale locale = context.getLocale();
		int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
		java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance(iDateFormat, locale);
		//MODIFIED::PA4:13-Jun-2011:IR-104838V6R2012x:END

		if(effortId!=null && effortId.trim().length()>0){
			DomainObject effortDObj = DomainObject.newInstance(context, effortId);
			String wkEndingDate = effortDObj.getAttributeValue(context, ATTRIBUTE_WEEK_ENDING_DATE);			
			Date wkEndDate = eMatrixDateFormat.getJavaDate(wkEndingDate);
			calWkEndDate.setTime(wkEndDate);
			calWkEndDate.set(Calendar.HOUR,0);
			calWkEndDate.set(Calendar.HOUR_OF_DAY,0);
			calWkEndDate.set(Calendar.MINUTE,0);
			calWkEndDate.set(Calendar.SECOND,0);	

			if(calWkEndDate.after(calStartDate) ||calWkEndDate.compareTo(calStartDate)==0){				
				int iDayNo = wkEndDate.getDay();
				String day = getDayName(iDayNo); 

				String dayEffort =  effortDObj.getAttributeValue(context, day);	
				strEffortList.add(dayEffort);
				for(int j=0;j<6;j++){
					//MODIFIED::PA4:13-Jun-2011:IR-104838V6R2012x:START
					//String prevDate = dateFormat.format(wkEndDate.getTime() - MILLIS_IN_DAY);
					//MODIFIED::PA4:13-Jun-2011:IR-104838V6R2012x:END
					//Date pdate = eMatrixDateFormat.getJavaDate(prevDate);
					Calendar calendar = Calendar.getInstance(locale);
					calendar.setTime(wkEndDate);
					calendar.add(Calendar.DAY_OF_WEEK,-1);
					Date pdate = calendar.getTime();
					Calendar calpDate = Calendar.getInstance();
					calpDate.setTime(pdate);
					calpDate.set(Calendar.HOUR,0);
					calpDate.set(Calendar.HOUR_OF_DAY,0);
					calpDate.set(Calendar.MINUTE,0);
					calpDate.set(Calendar.SECOND,0);
					if(calpDate.after(calStartDate) ||calpDate.compareTo(calStartDate)==0){
						int dayNo = pdate.getDay();
						String day1 = getDayName(dayNo); 
						strEffortList.add(effortDObj.getAttributeValue(context, day1));
						wkEndDate = pdate;
					} else {
						break;
					}
				}					
			}		   				
		}
		for(int k=0;k<strEffortList.size();k++){
			wkTotalEffort += Task.parseToDouble((String)strEffortList.get(k));
		}
		return wkTotalEffort;
	}

	private StringBuffer getWeekData(Context context, String effortId,String strSelYear, String strYearType) throws Exception {
		StringBuffer sbEffort = new StringBuffer();
		Date dtStartDate = null;
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

		CalendarType calendarType= getCalendarType(context, strYearType);
		Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(strSelYear.trim())); 
		dtStartDate = interval.getStartDate();

		double wkTotalEffort = 0.0;
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.setTime(dtStartDate);
		calStartDate.set(Calendar.HOUR,0);
		calStartDate.set(Calendar.HOUR_OF_DAY,0);
		calStartDate.set(Calendar.MINUTE,0);
		calStartDate.set(Calendar.SECOND,0);
		calStartDate.set(Calendar.MILLISECOND,0);
		Calendar calWkEndDate = Calendar.getInstance();	
		StringList strEffortList = new StringList();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

		if(effortId!=null && effortId.trim().length()>0){
			DomainObject dObjEffort = DomainObject.newInstance(context, effortId);
			String wkEndingDate = dObjEffort.getAttributeValue(context, ATTRIBUTE_WEEK_ENDING_DATE);			
			Date wkEndDate = eMatrixDateFormat.getJavaDate(wkEndingDate);
			calWkEndDate.setTime(wkEndDate);
			calWkEndDate.set(Calendar.HOUR,0);
			calWkEndDate.set(Calendar.HOUR_OF_DAY,0);
			calWkEndDate.set(Calendar.MINUTE,0);
			calWkEndDate.set(Calendar.SECOND,0);	

			if(calWkEndDate.after(calStartDate) ||calWkEndDate.compareTo(calStartDate)==0){				
				int iDayNo = wkEndDate.getDay();
				String day = getDayName(iDayNo); 				
				String dayEffort =  dObjEffort.getAttributeValue(context, day);	
				sbEffort.append(sdf.format(wkEndDate)+"="+Task.parseToDouble(dayEffort)+"|");
				for(int j=0;j<6;j++){
					String prevDate = sdf.format(wkEndDate.getTime() - MILLIS_IN_DAY);
					Date pdate = eMatrixDateFormat.getJavaDate(prevDate);
					Calendar calpDate = Calendar.getInstance();
					calpDate.setTime(pdate);
					calpDate.set(Calendar.HOUR,0);
					calpDate.set(Calendar.HOUR_OF_DAY,0);
					calpDate.set(Calendar.MINUTE,0);
					calpDate.set(Calendar.SECOND,0);
					if(calpDate.after(calStartDate) ||calpDate.compareTo(calStartDate)==0){
						int dayNo = pdate.getDay();
						String day1 = getDayName(dayNo); 
						String strEffortValue =  dObjEffort.getAttributeValue(context, day1);
						sbEffort.append(sdf.format(wkEndDate)+"="+Task.parseToDouble(strEffortValue)+"|");						
						wkEndDate = pdate;
					} else {
						break;
					}
				}					
			}								
		}
		return sbEffort;
	}	

	private StringBuffer getWeekData(Context context, String effortId,String strSelYear, String strYearType,String strPersonName) throws Exception 
	{
		return getWeekData(context, effortId, strSelYear, strYearType,strPersonName, true);
	}
	private StringBuffer getWeekData(Context context, String effortId,String strSelYear, String strYearType,String strPersonName, boolean updateApprovedEffort) throws Exception {
		StringBuffer sbEffort = new StringBuffer();
		Date dtStartDate = null;
		Date dtFinishDate = null;
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

		CalendarType calendarType= getCalendarType(context, strYearType);
		Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(strSelYear.trim())); 
		dtStartDate = interval.getStartDate();
		dtFinishDate = interval.getEndDate();

		double wkTotalEffort = 0.0;
		String wkEndingDate = null;
		Calendar calStartDate = Calendar.getInstance();
		calStartDate.setTime(dtStartDate);
		calStartDate.set(Calendar.HOUR,0);
		calStartDate.set(Calendar.HOUR_OF_DAY,0);
		calStartDate.set(Calendar.MINUTE,0);
		calStartDate.set(Calendar.SECOND,0);
		calStartDate.set(Calendar.MILLISECOND,0);
		Calendar calFinishDate = getCalendarInstance(dtFinishDate);
		Calendar calWkEndDate = Calendar.getInstance();	
		StringList strEffortList = new StringList();		
		//MODIFIED::PA4:13-Jun-2011:IR-104838V6R2012x:START
		Locale locale = context.getLocale();
		int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
		java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance(iDateFormat, locale);
		//MODIFIED::PA4:13-Jun-2011:IR-104838V6R2012x:END

		if(effortId!=null && effortId.trim().length()>0){
			DomainObject dObjEffort = DomainObject.newInstance(context, effortId);
			wkEndingDate = dObjEffort.getAttributeValue(context, ATTRIBUTE_WEEK_ENDING_DATE);	
			if(strPersonName == null){
				Date wkEndDate = eMatrixDateFormat.getJavaDate(wkEndingDate);
				calWkEndDate.setTime(wkEndDate);
				calWkEndDate.set(Calendar.HOUR,0);
				calWkEndDate.set(Calendar.HOUR_OF_DAY,0);
				calWkEndDate.set(Calendar.MINUTE,0);
				calWkEndDate.set(Calendar.SECOND,0);	
				if(calWkEndDate.after(calStartDate) ||calWkEndDate.compareTo(calStartDate)==0)
				{	
					if(!calWkEndDate.after(calFinishDate))
					{
						int iDayNo = wkEndDate.getDay();
						String day = getDayName(iDayNo); 				
						String dayEffort =  dObjEffort.getAttributeValue(context, day);	
						//MODIFIED::PA4:13-Jun-2011:IR-104838V6R2012x:START
						if(!updateApprovedEffort)
						{
							dayEffort = "0";
						}
						sbEffort.append(dateFormat.format(wkEndDate)+"="+Task.parseToDouble(dayEffort)+"|");
					}
					//String prevDate = dateFormat.format(wkEndDate.getTime() - MILLIS_IN_DAY);
					Calendar calendar = Calendar.getInstance(locale);	
					calendar.setTime(wkEndDate);
					calendar.add(Calendar.DAY_OF_WEEK,-1);
					for(int j=0;j<6;j++){
						//Date pdate = eMatrixDateFormat.getJavaDate(prevDate, locale); This method is not working for passed formatted value non english OS
						Date pdate = calendar.getTime();
						wkEndDate = pdate;
						Calendar calpDate = Calendar.getInstance();
						calpDate.setTime(pdate);
						calpDate.set(Calendar.HOUR,0);
						calpDate.set(Calendar.HOUR_OF_DAY,0);
						calpDate.set(Calendar.MINUTE,0);
						calpDate.set(Calendar.SECOND,0);
						if(calpDate.after(calStartDate) ||calpDate.compareTo(calStartDate)==0){
							if((calpDate.before(calFinishDate) || calpDate.compareTo(calFinishDate) == 0))
							{
								int dayNo = pdate.getDay();
								String day1 = getDayName(dayNo); 
								String strEffortValue =  dObjEffort.getAttributeValue(context, day1);
								if(!updateApprovedEffort)
								{
									strEffortValue = "0";
								}
								sbEffort.append(dateFormat.format(wkEndDate)+"="+Task.parseToDouble(strEffortValue)+"|");						
							}
							//String prevDate = dateFormat.format(wkEndDate.getTime() - MILLIS_IN_DAY);
							Date dtPrevDate = new Date(wkEndDate.getTime() - MILLIS_IN_DAY);
							calendar.setTime(dtPrevDate);
						} else {
							break;
						}
					}					
					//MODIFIED::PA4:13-Jun-2011:IR-104838V6R2012x:END
				}	
			} else {
				String originator = dObjEffort.getAttributeValue(context, ATTRIBUTE_ORIGINATOR);
				if(originator.equalsIgnoreCase(strPersonName)){
					wkEndingDate = dObjEffort.getAttributeValue(context, ATTRIBUTE_WEEK_ENDING_DATE);			
					Date wkEndDate = eMatrixDateFormat.getJavaDate(wkEndingDate);
					calWkEndDate.setTime(wkEndDate);
					calWkEndDate.set(Calendar.HOUR,0);
					calWkEndDate.set(Calendar.HOUR_OF_DAY,0);
					calWkEndDate.set(Calendar.MINUTE,0);
					calWkEndDate.set(Calendar.SECOND,0);	
					if(calWkEndDate.after(calStartDate) ||calWkEndDate.compareTo(calStartDate)==0)
					{
						if(!calWkEndDate.after(calFinishDate))
						{
							int iDayNo = wkEndDate.getDay();
							String day = getDayName(iDayNo); 				
							String dayEffort =  dObjEffort.getAttributeValue(context, day);	
							if(!updateApprovedEffort)
							{
								dayEffort = "0";
							}
							sbEffort.append(dateFormat.format(wkEndDate)+"="+Task.parseToDouble(dayEffort)+"|");
						}
						Calendar calendar = Calendar.getInstance(locale);						
						for(int j=0;j<6;j++){
							//String prevDate = dateFormat.format(wkEndDate.getTime() - MILLIS_IN_DAY);
							calendar.setTime(wkEndDate);
							calendar.add(Calendar.DAY_OF_WEEK,-1);
							//Date pdate = eMatrixDateFormat.getJavaDate(prevDate); This method is not working for passed formatted value non english OS
							Date pdate = calendar.getTime();
							wkEndDate = pdate;
							Calendar calpDate = Calendar.getInstance();
							calpDate.setTime(pdate);
							calpDate.set(Calendar.HOUR,0);
							calpDate.set(Calendar.HOUR_OF_DAY,0);
							calpDate.set(Calendar.MINUTE,0);
							calpDate.set(Calendar.SECOND,0);
							if((calpDate.after(calStartDate) ||calpDate.compareTo(calStartDate) == 0))
							{
								if((calpDate.before(calFinishDate) || calpDate.compareTo(calFinishDate) == 0))
								{
									int dayNo = pdate.getDay();
									String day1 = getDayName(dayNo); 
									String strEffortValue =  dObjEffort.getAttributeValue(context, day1);
									if(!updateApprovedEffort)
									{
										strEffortValue = "0";
									}
									sbEffort.append(dateFormat.format(wkEndDate)+"="+Task.parseToDouble(strEffortValue)+"|");						
								}
								//prevDate = dateFormat.format(wkEndDate.getTime() - MILLIS_IN_DAY);
								Date dtPrevDate = new Date(wkEndDate.getTime() - MILLIS_IN_DAY);
								calendar.setTime(dtPrevDate);								
							} 
							else
							{
								break;
							}
						}					
					}
				}
			}
		}
		return sbEffort;
	}		
	private double getPartialEffortsForWeek(Map mapObject,Calendar calStartDate,Calendar calFinishDate) throws MatrixException
	{
		double returnPartialEffortValue = 0.0;
		StringBuffer sbEffortAndDate = (StringBuffer)mapObject.get("DateAndEffort");

		try
		{
			if((null  != sbEffortAndDate  &&  ProgramCentralUtil.isNotNullString(sbEffortAndDate.toString()))) 
			{										
				String strEffortDate = sbEffortAndDate.toString();
				StringList slDateEffort = FrameworkUtil.splitString(strEffortDate,"|");
				int size1 = slDateEffort.size();
				for(int k=0;k<size1;k++)
				{
					String strEffortKeyValue = (String)slDateEffort.get(k);
					if(ProgramCentralUtil.isNotNullString(strEffortKeyValue))
					{
						String[] strWeekDate = strEffortKeyValue.split("\\=");
						Date dWeekDate = new Date(strWeekDate[0]); 
						Calendar calWeekDate = getCalendarInstance(dWeekDate);

						if((calWeekDate.after(calStartDate) || calWeekDate.compareTo(calStartDate) == 0)
								&& (calWeekDate.before(calFinishDate) || calWeekDate.compareTo(calFinishDate) == 0))  
						{
							if(ProgramCentralUtil.isNotNullString(strWeekDate[1]))
							{
								returnPartialEffortValue = returnPartialEffortValue + Task.parseToDouble(strWeekDate[1]);
							}
						}
					}

				}
			}
		} 
		catch (Exception e) 
		{
			throw new MatrixException(e);	
		}
		return returnPartialEffortValue;
	}

	private double CalculateTotalEffortsForWeek(Context context, Map programParamMap)throws MatrixException
	{
		Map programMap = (Map) programParamMap.get("programMap");
		Map effortObjMap = (Map) programParamMap.get("effortMap");

		Map paramMap = (Map) programMap.get("paramList");
		String sYearType = (String)paramMap.get("YearType");
		String strSelYear = (String)paramMap.get("selYear");

		boolean isFiscal = true; 

		if(SELECT_CALENDAR.equalsIgnoreCase(sYearType))
		{
			isFiscal = false;
		}
		Date dtStartDate = null;
		Date dtFinishDate = null;
		if(ProgramCentralUtil.isNotNullString(strSelYear))
		{
			Interval interval = Helper.yearInterval(CalendarType.ENGLISH,Integer.parseInt(strSelYear.trim())); 
			dtStartDate = interval.getStartDate();
			dtFinishDate = interval.getEndDate();
		}
		dtFinishDate = getDurationToModifyWeekEndDate(context,isFiscal, true , dtFinishDate);

		Calendar calStartDate = getCalendarInstance(dtStartDate);
		Calendar calFinishDate = getCalendarInstance(dtFinishDate);

		String strAttrbWeekEndDate = (String) effortObjMap.get(SELECT_ATTRIBUTE_ENDING_DATE);
		Date wkEndDate = eMatrixDateFormat.getJavaDate(strAttrbWeekEndDate);
		Calendar calEffortWkEndDate = getCalendarInstance(wkEndDate);

		double totalEfforValueForWeek = 0;

		if(calEffortWkEndDate.after(calStartDate) ||calEffortWkEndDate.compareTo(calStartDate)==0)
		{
			totalEfforValueForWeek = 0;

			if(calEffortWkEndDate.after(calFinishDate))  
			{
				totalEfforValueForWeek = getPartialEffortsForWeek(effortObjMap,calStartDate,calFinishDate);
			}
			else
			{
				Double totalEfforts = (Double)effortObjMap.get("Total Efforts1");
				totalEfforValueForWeek = totalEfforts.doubleValue();
			}
		}
		return totalEfforValueForWeek;
	}
	private String getDayName(int dayNo){
		String strDayValue = null;
		switch(dayNo){
		case 0:strDayValue = ATTRIBUTE_SUNDAY;break;
		case 1:strDayValue = ATTRIBUTE_MONDAY;break;
		case 2:strDayValue = ATTRIBUTE_TUESDAY;break;
		case 3:strDayValue = ATTRIBUTE_WEDNESDAY;break;
		case 4:strDayValue = ATTRIBUTE_THURSDAY;break;
		case 5:strDayValue = ATTRIBUTE_FRIDAY;break;
		case 6:strDayValue = ATTRIBUTE_SATURDAY;break;
		}
		return strDayValue; 
	}

	private double getTotalForReport(StringBuffer sbEffortAndDate) {
		double dbTotalEfforts = 0.0;
		if((sbEffortAndDate != null && !"".equals(sbEffortAndDate.toString()) && !"null".equals(sbEffortAndDate))) {										
			String strEffortDate = sbEffortAndDate.toString();
			StringList slDateEffort = FrameworkUtil.split(strEffortDate,"|");			
			for(int k=0;k<slDateEffort.size()-1;k++){
				String strEffortKeyValue = (String)slDateEffort.get(k);
				String[] strWeekDate = strEffortKeyValue.split("\\=");
				for(int i=1;i<strWeekDate.length;i+=2){
					dbTotalEfforts += Task.parseToDouble(strWeekDate[i]);
				}				
			}
		}
		return dbTotalEfforts;
	}

	//MODIFIED::PA4:14-Jun-2011:IR-104838V6R2012x:START	
	/**
	 * This method is used to get Date Range when no specific locale
	 * is provided and default will be taken as Locale.US. 
	 * @param dStartDateOfWeek
	 * @param dEndDateOfWeek
	 * @return
	 * @throws MatrixException
	 */
	private StringList getDateRange(Date dStartDateOfWeek, Date dEndDateOfWeek) throws MatrixException {
		return getDateRange(dStartDateOfWeek, dEndDateOfWeek, Locale.US); 
	}    

	/**
	 * This method is overloaded verion of old getDateRange method,
	 * to support i18n of dates.
	 * @param dStartDateOfWeek
	 * @param dEndDateOfWeek
	 * @param locale
	 * @return
	 * @throws MatrixException
	 */
	private StringList getDateRange(Date dStartDateOfWeek, Date dEndDateOfWeek, Locale locale) throws MatrixException {
		StringList dateList = new StringList();
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
		java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance(iDateFormat, locale);
		Date date = null;
		String startDate = dateFormat.format(dStartDateOfWeek);
		String endDate = dateFormat.format(dEndDateOfWeek);
		while(true){
			if(endDate.equals(startDate) == true){
				dateList.add(endDate);
				break;
			}
			dateList.add(startDate);
			//String nextDate = dateFormat.format(dStartDateOfWeek.getTime() + MILLIS_IN_DAY);
			//date = eMatrixDateFormat.getJavaDate(nextDate, locale); This method is not working for passed formatted value non english OS
			Calendar calendar = Calendar.getInstance(locale);
			calendar.setTime(dStartDateOfWeek);
			calendar.add(Calendar.DAY_OF_WEEK,1);
			dStartDateOfWeek = calendar.getTime();
			//dStartDateOfWeek = date;    
			startDate = dateFormat.format(dStartDateOfWeek);
		} 	   	
		return dateList;
	}	
	//MODIFIED::PA4:14-Jun-2011:IR-104838V6R2012x:END

	/**
	 * Gets the data for the column "Name" for table "PMCLaborReport"
	 * 
	 * @param context
	 *            The matrix context object
	 * @param args
	 *            The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception
	 *             if operation fails
	 */
	public Vector getColumnNameData(Context context, String[] args)
			throws Exception {
		try {
			Vector vecResult = new Vector();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			Map mapObjectInfo = null;
			String strName = "";
			String strObjName = "";

			for (Iterator itrObjects = objectList.iterator(); itrObjects
					.hasNext();) {
				mapObjectInfo = (Map) itrObjects.next();
				if(mapObjectInfo.containsKey(SELECT_NAME)) 
				{
					strName = (String)mapObjectInfo.get(SELECT_NAME);
				}
				vecResult.add(strName);
			}
			if (strName==null || "".equals(strName))
			{
				String strObjectId = (String)paramList.get("relId");
				if (strObjectId==null || "".equals(strObjectId))
				{
					String  selectedItems= (String)paramList.get("objectId");

					strObjectId = selectedItems.toString();
				}
				DomainObject domId=  DomainObject.newInstance(context, strObjectId);
				strObjName = domId.getName(context);
				strName = strObjName;
				vecResult.add(strObjName);
			}
			return vecResult;
		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}

	}

	/**
	 * Gets the data for the column "<Type>" for table "<PMCLaborReport>"
	 * 
	 * @param context
	 *            The matrix context object
	 * @param args
	 *            The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception
	 *             if operation fails
	 */
	public Vector getColumnTypeData(Context context, String[] args)
			throws Exception {
		try {
			Vector vecResult = new Vector();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			Map mapObjectInfo = null;
			for (Iterator itrObjects = objectList.iterator(); itrObjects
					.hasNext();) {
				mapObjectInfo = (Map) itrObjects.next();

				vecResult.add((String)mapObjectInfo.get(SELECT_TYPE));
			}
			return vecResult;
		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}

	/**
	 * Gets the data for the column "<Total>" for table "<PMCLaborReport>"
	 * 
	 * @param context
	 *            The matrix context object
	 * @param args
	 *            The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception
	 *             if operation fails
	 */

	public Vector getProjectEfforts(Context context, String[] args)
			throws Exception {
		try {
			Vector vecResult = new Vector();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map columnMap = (Map) programMap.get("columnMap");
			String strName = (String) columnMap.get(SELECT_NAME);
			Map mapObjectInfo = null;
			for (Iterator itrObjects = objectList.iterator(); itrObjects
					.hasNext();) {
				mapObjectInfo = (Map) itrObjects.next();
				Double totalEfforts = (Double)mapObjectInfo.get("Total Efforts1");
				if(!"null".equals(totalEfforts) && null!= totalEfforts && !"".equals(totalEfforts)){
					double dTotal = totalEfforts.doubleValue();
					String strTotal = Double.toString(dTotal);
					vecResult.add(strTotal);
				}else {
					vecResult.add("0.0");
				}
			}
			return vecResult;
		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}	

	private double getTotalForReport(Context context,Map programParamMap)  throws MatrixException
	{
		return CalculateTotalEffortsForWeek(context, programParamMap);
	}

	private Calendar getCalendarInstance(Date date)
	{
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(date);
		calDate.set(Calendar.HOUR,0);
		calDate.set(Calendar.HOUR_OF_DAY,0);
		calDate.set(Calendar.MINUTE,0);
		calDate.set(Calendar.SECOND,0);
		calDate.set(Calendar.MILLISECOND,0);
		return calDate;
	}

	/**
	 * Generates required columns dynamically
	 * 
	 * @param context
	 *            The matrix context object
	 * @param args
	 *            The arguments, it contains requestMap
	 * @return The MapList object containing definitions about new columns for
	 *         showing Dynamic
	 * @throws Exception
	 *             if operation fails
	 */
	public MapList getDynamicLaborReport (Context context, String[] args) throws Exception
	{
		MapList mlColumns = new MapList();
		try {
			Map mapProgram = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map)mapProgram.get("requestMap");
			String strViewSelected = (String)requestMap.get("PMCWTSViewFilter");
			String strPassFromJSP = (String)requestMap.get("passFromJsp");
			String strYear =  (String)requestMap.get("selYear");
			StringBuffer sbYear = new StringBuffer();
			sbYear.append(strYear);
			int dYear = Integer.valueOf(strYear.trim());
			int dNextYear = dYear+1;
			String strYearType = (String)requestMap.get("YearType");
			String strTimeLine = (String)requestMap.get("selTimeline");
			IntervalType intervalType = intervaltype(strViewSelected);
			String strObjectId = (String) requestMap.get("emxTableRowId");
			boolean istypeProjectSpace = false;
			int nMonth  = 0;
			int nYear= 0;
			int nFinishWeek = 0;
			int nStartWeek = 0;
			int nNumberOfWeeks = 0;
			int totalnumWeeks = 0;
			Map mapColumn = null;
			Map mapSettings = null;
			String strMonth = "";
			Date dtStartDate = null;
			Date dtFinishDate = null;
			String strRequestStartDate = "";
			String strRequestEndDate = "";

			CalendarType calendarType= getCalendarType(context, strYearType);
			Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(strYear)); 
			dtStartDate = interval.getStartDate();
			dtFinishDate =interval.getEndDate();
			Interval [] intervals = null; 
			com.matrixone.apps.program.fiscal.Calendar calendar = Helper.getCalendar(calendarType);
			com.matrixone.apps.program.fiscal.Iterator iterator = calendar.getIterator(intervalType);
			if(strPassFromJSP.equalsIgnoreCase("true"))
			{
				intervals = iterator.range(dtStartDate, dtFinishDate);
				strPassFromJSP = (String)requestMap.put("passFromJsp", "true");    
			}
			else 
			{
				intervals = iterator.range(dtStartDate, dtFinishDate);
			}
			int nTimeframe  = 0 ;
			int nYearTimeframe = 0;
			Map mapObjectInfo = null;
			String strWK = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ResourceRequest.TimeLine.WeekPrefix", context.getSession().getLanguage());
			Interval timeInterval = null;
			for(int l =0; l < intervals.length ; l++)
			{
				timeInterval = intervals[l];
				nTimeframe = timeInterval.getIntervalNumber();
				nYearTimeframe =timeInterval.getYear();
				Date start = timeInterval.getStartDate();
				Date end = timeInterval.getEndDate();
				mapColumn = new HashMap();
				DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

				mapColumn.put("name", nTimeframe+"-"+nYearTimeframe);
				mapColumn.put("label", dateFormat.format(start)+"-"+dateFormat.format(end));

				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxWeeklyTimeSheet");
				mapSettings.put("function","getColumnLaborReportData");
				mapSettings.put("Column Type","program");
				mapSettings.put("Printer Friendly","true");
				mapSettings.put("Editable","true");
				mapSettings.put("Sortable","false");
				mapSettings.put("Export","true");
				mapSettings.put("Group Header",nYearTimeframe+"");
				mapColumn.put("settings", mapSettings);
				mapColumn.put("strTimeLine", strTimeLine);
				mlColumns.add(mapColumn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mlColumns;
	}

	private IntervalType intervaltype(String strViewSelected) 
	{
		IntervalType intervalType = null; 
		if((null !=strViewSelected)&&(!"".equals(strViewSelected)))  
		{
			if(SELECT_WEEKLY.equalsIgnoreCase(strViewSelected))
			{
				intervalType = IntervalType.WEEKLY;
			}
			else if(SELECT_MONTHLY.equalsIgnoreCase(strViewSelected))
			{
				intervalType = IntervalType.MONTHLY;
			}
			else if(SELECT_QUARTERLY.equalsIgnoreCase(strViewSelected))
			{
				intervalType = IntervalType.QUARTERLY;
			}
			else if(SELECT_YEARLY.equalsIgnoreCase(strViewSelected))
			{
				intervalType = IntervalType.YEARLY;
			}
		}
		return intervalType;
	}


	/**
	 * Gets the data for the column "Dynamic" for table "PMCLaborReportTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */    
	public Vector getColumnLaborReportData(Context context, String[] args)  throws Exception 
	{
		Vector vecResult = new Vector();
		try {   		
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			int isize = (objectList).size();
			Map paramList = (Map) programMap.get("paramList");
			String strTimeLine =  (String)paramList.get("selTimeline");
			HashMap columnMap = (HashMap) programMap.get("columnMap");
			Locale locale = context.getLocale();
			String strTimeLineFromTable = (String)columnMap.get("strTimeLine");
			StringList dateRangeList = new StringList();
			int nMonth = 0;
			int nYear  =0;          		
			String strName= (String) columnMap.get(SELECT_NAME);

			String[] nameSplit = strName.split("-");  
			String strYearType =  (String)paramList.get("YearType");
			CalendarType calendarType = getCalendarType(context, strYearType);
			IntervalType intervalType = intervaltype(strTimeLineFromTable);
			Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(nameSplit[1])); 
			Date dtStartDate = interval.getStartDate();
			Date dtFinishDate =interval.getEndDate();
			com.matrixone.apps.program.fiscal.Calendar calendar = Helper.getCalendar(calendarType);
			com.matrixone.apps.program.fiscal.Iterator iterator = calendar.getIterator(intervalType);
			Interval[] intervals = iterator.range(dtStartDate, dtFinishDate);
			Date start = null;
			Date end = null;
			for(int l =0; l < intervals.length ; l++)
			{
				Interval timeInterval = intervals[l];
				int nTimeframe = timeInterval.getIntervalNumber();				
				int nTYear = timeInterval.getYear();				
				if(nTimeframe==Integer.parseInt(nameSplit[0]) && nTYear==Integer.parseInt(nameSplit[1]))
				{
					start = timeInterval.getStartDate();
					end = timeInterval.getEndDate();
					break;
				}
			}
			dateRangeList = getDateRange(start,end,locale);
			for (int i=0;i<objectList.size();i++) {
				double effortValue = 0.0;
				Map mapObjectInfo = (Map) objectList.get(i);
				StringBuffer sbEffortAndDate = (StringBuffer)mapObjectInfo.get("DateAndEffort");
				if((sbEffortAndDate != null && !"".equals(sbEffortAndDate.toString()) && !"null".equals(sbEffortAndDate))) {										
					String strEffortDate = sbEffortAndDate.toString();
					StringList slDateEffort = FrameworkUtil.split(strEffortDate,"|");
					HashMap dateEffortMap = new HashMap();
					for(int k=0;k<slDateEffort.size()-1;k++){
						String strEffortKeyValue = (String)slDateEffort.get(k);
						String[] strWeekDate = strEffortKeyValue.split("\\=");
						if(dateEffortMap.containsKey(strWeekDate[0])){
							double oldEffortValue = Task.parseToDouble(dateEffortMap.get(strWeekDate[0]).toString());
							dateEffortMap.put(strWeekDate[0], oldEffortValue + Task.parseToDouble(strWeekDate[1]));
						} else {
							dateEffortMap.put(strWeekDate[0], Task.parseToDouble(strWeekDate[1]));
						}
					}
					for(int j=0;j<dateRangeList.size();j++) {
						Double dEffortValue = (Double)dateEffortMap.get(dateRangeList.get(j));
						if(dEffortValue!=null){
							effortValue += dEffortValue;
							NumberFormat numberFormat = NumberFormat.getInstance();
							numberFormat.setMaximumFractionDigits(2);
							String strRoundedValue = numberFormat.format(effortValue);
							if (strRoundedValue != null && !"".equals(strRoundedValue)) {
								effortValue = Task.parseToDouble(strRoundedValue);
							}
						}					
					}
				}
				vecResult.add(Double.toString(effortValue));
			}			
		} 
		catch (Exception exp) {    	          
			throw exp;
		}
		return vecResult;
	}

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public MapList getWeeklyTimeframes(Context context,Date fromDate, Date toDate,CalendarType calendarType,int Year) throws MatrixException {

		if (fromDate == null || toDate == null) {
			throw new IllegalArgumentException();
		}
		if (toDate.before(fromDate)) {
			throw new MatrixException("Start date beyond end date");
		}
		MapList mlTimeframes = new MapList();
		com.matrixone.apps.program.fiscal.Iterator iterator = null;
		com.matrixone.apps.program.fiscal.Calendar fiscalCalendar = Helper.getCalendar(calendarType);
		Interval[] range = null;

		Interval interval = Helper.yearInterval(calendarType,Year); 
		Date startDate = interval.getStartDate();
		Date endDate = interval.getEndDate();

		Calendar calStart = Calendar.getInstance();
		calStart.setTime(fromDate);

		int nStartYear = Year;
		iterator = fiscalCalendar.getIterator(IntervalType.WEEKLY);
		range = iterator.range(startDate, startDate);
		Interval it = range[0];
		int nStartWeek = it.getIntervalNumber();

		int nEndYear = Year;
		range = iterator.range(endDate, endDate);
		it = range[0];
		int nEndWeek = it.getIntervalNumber();

		int nWeek = 1;
		int nYear = nStartYear;

		while (true) {
			Map mapTimeframe = new HashMap();
			mapTimeframe.put(ATTRIBUTE_TIMEFRAME, new Integer(nWeek));
			mapTimeframe.put(ATTRIBUTE_YEAR, new Integer(nYear));
			mlTimeframes.add(mapTimeframe);
			nWeek++;
			if (nYear != nEndYear) {
				if (nWeek >= this.maxValidTimeframe(nYear)) {
					nWeek = this.minValidTimeframe(nYear);
					nYear++;
				}
			}
			else {
				if (nWeek > nEndWeek) {
					break;
				}
			}
		}
		return mlTimeframes;
	}
	/**
	 * 
	 * @param year
	 * @return
	 */
	public int maxValidTimeframe(int year) {
		Calendar c = Calendar.getInstance();  
		c.set(year, 0, 1);  
		return c.getActualMaximum(Calendar.WEEK_OF_YEAR);   
	}
	/**
	 * 
	 * @param year
	 * @return
	 */ 
	public int minValidTimeframe(int year) {
		return 1;
	}
	/** Method to get Status from properties
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - args contains a Map with the following entries
	 *                      objectId - The object Id of Context object
	 * @return - boolean (true or false)
	 * @throws Exception if the operation fails
	 * @since Common 11.0
	 */
	public HashMap getFilterStates(Context context, String[] args) throws Exception
	{
		HashMap statusMap = new HashMap();
		StringList fieldRangeValues = new StringList();
		fieldRangeValues.add(SELECT_MONTHLY);
		fieldRangeValues.add(SELECT_WEEKLY);
		StringList fieldDisplayRangeValues = new StringList();
		for(int i=1;i<=2;i++) {
			String strStatusFilter = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.WeeklyTimesheetReport.DefaultTimeLineInterval" + i + ".Options", context.getSession().getLanguage());
			fieldDisplayRangeValues.addElement(strStatusFilter);
		}
		statusMap.put("field_choices", fieldRangeValues);
		statusMap.put("field_display_choices", fieldDisplayRangeValues);
		return statusMap;
	}
	/**
	 * Gets Labor Report Membership table data for Project Space
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMembershipChildLaborReport(Context context, String[] args) throws Exception
	{ 
		MapList mlTaskList = new MapList();
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strProjectIds = (String) programMap.get("emxTableRowId");
			if((null ==strProjectIds)||("".equals(strProjectIds)))  
			{
				strProjectIds = (String) programMap.get("relId");
				if((null ==strProjectIds)||("".equals(strProjectIds)))  
				{
					strProjectIds = (String) programMap.get("emxParentIds");
				}
			}
			String[] strProjectIdArray = strProjectIds.split("\\|");
			String strYearType = (String) programMap.get("YearType");
			String strSelYear = (String) programMap.get("selYear");
			String strView = (String) programMap.get("selTimeline");

			int nFiscalYear = 0; 
			boolean isReportByPhase = true;

			for (int i = 0; i < strProjectIdArray.length; i++) {
				MapList mlPartialProjectData = getMembershipProject(context, strProjectIdArray[i],strYearType, strSelYear, strView, isReportByPhase);
				mlTaskList.addAll(mlPartialProjectData);
			}    		
		}
		catch (Exception exp) 
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
		return mlTaskList ;
	}   

	/**
	 * Returns the Labor Report data for provided project id and the filter
	 * values
	 * 
	 * @param context
	 * @param strProjectId
	 * @param strYearType
	 * @param strSelYear
	 * @param strView
	 * @param isReportByPhase
	 * @return MapList
	 * @throws MatrixException
	 *             if operation fails
	 */   
	private MapList getMembershipProject(Context context, String strProjectId, String strYearType, String strSelYear, String strView, boolean isReportByPhase) throws MatrixException 
	{
		CalendarType calendarType= getCalendarType(context, strYearType);
		MapList mlReportData = new MapList();
		MapList mlEfforts = getEffortsForLaborReportForProject(context, strProjectId, strSelYear, strYearType);
		double iTotalEffort = 0.0;
		for (Iterator itrEfforts = mlEfforts.iterator(); itrEfforts.hasNext();) 
		{
			Map mapEffortInfo = (Map) itrEfforts.next();
			double strTotalEfforts = (Double)mapEffortInfo.get(SELECT_EFFORT);
			iTotalEffort+= strTotalEfforts;
		}

		//
		// Find all the tasks for this project
		//
		MapList mlTasks = getAllTasksForLaborReportForProject(context, strProjectId,strSelYear, strYearType,iTotalEffort,null);

		String strWeekEndDate = "";
		Calendar cal = Calendar.getInstance();
		String strChildEffortVal = "";
		String strMonth = "";
		StringBuffer sbStrMonth = new StringBuffer();
		StringBuffer sbStrEfforts =  new StringBuffer();
		StringBuffer sbStrWeek = new StringBuffer();  		
		StringList strTaskEffortTotalVals = new StringList();
		StringList strEffortMonths = new StringList();
		String delimiter="|";
		StringList strMonthNames =  new StringList();
		String strEffort = "";
		double dEffort = 0.0;
		Map mapTaskInfo = new HashMap();
		String strWeekNo = "";
		String strYear = "";
		StringBuffer sbEffortAndDate = new StringBuffer();
		for (Iterator itrTasks = mlTasks.iterator(); itrTasks.hasNext();) 
		{
			mapTaskInfo = (Map) itrTasks.next();
			if(mapTaskInfo.containsKey(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT)) 
			{
				if(mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("java.lang.String"))
				{
					strWeekEndDate = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);
					StringBuffer sbEffortAndDate1 = (StringBuffer)mapTaskInfo.get("DateAndEffort");
					sbEffortAndDate.append(sbEffortAndDate1);
					Date dDate = new Date(strWeekEndDate); 
					cal.setTime(dDate);
					strChildEffortVal = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
					if((null !=strChildEffortVal)&&(!"".equals(strChildEffortVal)))    
					{
						dEffort += (Double)Task.parseToDouble(strChildEffortVal.trim());
						strEffort  = strChildEffortVal;
						Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
						strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
						strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
						strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);
						sbStrMonth.append(strMonth);
						sbStrMonth.append("-");
						sbStrMonth.append(strYear);
						sbStrMonth.append(delimiter);

						sbStrWeek.append(strWeekNo);
						sbStrWeek.append("-");
						sbStrWeek.append(strYear);
						sbStrWeek.append(delimiter);
						sbStrEfforts.append(strEffort);
						sbStrEfforts.append(delimiter);

					}

				}
				else if (mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("matrix.util.StringList"))
				{					 
					StringBuffer sbEffortAndDate1 = (StringBuffer)mapTaskInfo.get("DateAndEffort");
					sbEffortAndDate.append(sbEffortAndDate1); 
					strTaskEffortTotalVals= (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
					strEffortMonths = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);	 

					int ilength = strTaskEffortTotalVals.size();
					for (int i=0; i<ilength ; i++)
					{
						strWeekEndDate = (String)strEffortMonths.get(i);
						Date dDate = new Date(strWeekEndDate); 
						cal.setTime(dDate);
						Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
						strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
						strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
						strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

						sbStrMonth.append(strMonth);
						sbStrMonth.append("-");
						sbStrMonth.append(strYear);
						sbStrMonth.append(delimiter);

						sbStrWeek.append(strWeekNo);
						sbStrWeek.append("-");
						sbStrWeek.append(strYear);
						sbStrWeek.append(delimiter);

						strMonthNames.addElement(strMonth);
						strEffort  = (String)strTaskEffortTotalVals.get(i);
						sbStrEfforts.append(strEffort);
						sbStrEfforts.append(delimiter);
						dEffort += (Double)Task.parseToDouble(strEffort.trim());
					}
				}
			}
		}
		//
		// Create project row in report data
		//
		StringList slBusSelect = new StringList();
		slBusSelect.add(SELECT_TYPE);
		slBusSelect.add(SELECT_NAME);
		slBusSelect.add(SELECT_ID);

		DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
		Map mapProjectInfo = dmoProject.getInfo(context, slBusSelect);

		Map mapProjectRow = new HashMap();
		mapProjectRow.put(SELECT_LEVEL, "1");
		mapProjectRow.put(SELECT_ID, strProjectId);
		mapProjectRow.put(SELECT_TYPE, (String)mapProjectInfo.get(SELECT_TYPE));
		mapProjectRow.put(SELECT_NAME, (String)mapProjectInfo.get(SELECT_NAME));
		mapProjectRow.put("Total Efforts", iTotalEffort); 	
		mapProjectRow.put("Effort Week", sbStrWeek); 	
		mapProjectRow.put("Effort Month", sbStrMonth);
		mapProjectRow.put("Effort Values",sbStrEfforts );
		mapProjectRow.put("DateAndEffort", sbEffortAndDate);
		mapProjectRow.put("Total Efforts1", getTotalForReport(sbEffortAndDate));		
		mlReportData.add(mapProjectRow);
		return mlReportData;
	}

	/**
	 * This method used to get data when we expand labor report roort node in
	 * membership view.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            contains the paramMap
	 * @return MapList
	 * @throws Exception
	 *             if operation fails
	 * @since Added by wpk for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList expandMembershipChildLaborReport(Context context, String[] args) throws Exception 
	{
		MapList mlReportData = new MapList();
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		//HashMap requestMap = (HashMap)paramMap.get("RequestValuesMap");
		String strObjectId = (String)paramMap.get("objectId");
		String YearType = (String)paramMap.get("YearType");
		String selYear = (String)paramMap.get("selYear");
		String selTimeline = (String)paramMap.get("selTimeline");
		MapList mlPartialProjectData = getMembershipReportForProject(context, strObjectId,YearType, selYear, selTimeline, false);
		HashMap hmTemp = new HashMap();
		hmTemp.put("expandMultiLevelsJPO","true");  
		mlPartialProjectData.add(hmTemp);
		return mlPartialProjectData;
	}

	/**
	 * This method used to get data when we expand labor report roort node in
	 * phase view.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            contains the paramMap
	 * @return MapList
	 * @throws Exception
	 *             if operation fails
	 * @since Added by wpk for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList expandPhaseChildLaborReport(Context context, String[] args) throws Exception 
	{
		MapList mlReportData = new MapList();
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		//HashMap requestMap = (HashMap)paramMap.get("RequestValuesMap");
		String strObjectId = (String)paramMap.get("objectId");
		String YearType = (String)paramMap.get("YearType");
		String selYear = (String)paramMap.get("selYear");
		String selTimeline = (String)paramMap.get("selTimeline");
		MapList mlPartialProjectData = getLaborReportForProject(context, strObjectId,YearType, selYear, selTimeline, true);
		HashMap hmTemp = new HashMap();
		hmTemp.put("expandMultiLevelsJPO","true");  
		mlPartialProjectData.add(hmTemp);
		return mlPartialProjectData;
	}

	/**
	 * Returns the Labor Report data for provided project id and the filter
	 * values
	 * 
	 * @param context
	 * @param strProjectId
	 * @param strYearType
	 * @param strSelYear
	 * @param strView
	 * @param isReportByPhase
	 * @return MapList
	 * @throws MatrixException
	 *             if operation fails
	 */ 
	private MapList getMembershipReportForProject(Context context, String strProjectId, String strYearType, String strSelYear, String strView, boolean isReportByPhase) throws MatrixException 
	{
		MapList mlReportData = new MapList();
		try 
		{
			MapList mpPersonsList = getPersonsForProject(context, strProjectId, strSelYear, strYearType);
			mlReportData.addAll(mpPersonsList);

		}
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
		return mlReportData;
	}

	/**
	 * Gets Persons Data for Specified Project Space
	 * 
	 * @param context The Matrix Context object
	 * @param strProjectId
	 * @param strSelYear
	 * @param strYearType
	 * @throws Exception if operation fails 
	 */
	private MapList getPersonsForProject(Context context, String strProjectId, String strSelYear, String strYearType) throws MatrixException
	{

		MapList mpPersonsForProject = new MapList();
		try{
			MapList mapPersonList = getPersonForProject(context,strProjectId);//TODO Verify for Non project members
			final boolean GET_TO = true;
			final boolean GET_FROM = true;
			StringList sbBusSelect = new StringList();
			sbBusSelect.add(SELECT_TYPE);
			sbBusSelect.add(SELECT_NAME);
			sbBusSelect.add(SELECT_ID);

			StringList sbRelSelect = new StringList();
			sbRelSelect.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
			sbRelSelect.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ACCESS+"]");

			short recurseToLevel = 0;
			String strObjectWhere = null;

			int nLimit = 0;
			MapList mlProjectLeads = new MapList();
			StringList slPersonIds = new StringList();
			String strPersonId = "";

			double iEffort = 0.0;
			StringBuffer sbStrPersonMonth = new StringBuffer();
			StringBuffer sbStrPersonEfforts = new StringBuffer();
			StringBuffer sbStrPersonWeek = new StringBuffer();
			for (Iterator itrPersons = mapPersonList.iterator(); itrPersons.hasNext();) 
			{
				Map mapPersonInfo = (Map)itrPersons.next();
				strPersonId = (String) mapPersonInfo.get(SELECT_ID);
				slPersonIds.add(strPersonId);
				String strProjectRole = (String)mapPersonInfo.get(SELECT_ATTRIBUTE_PROJECT_ROLE);
				String strProjectAccess = (String)mapPersonInfo.get(SELECT_ATTRIBUTE_PROJECT_ACCESS);
					if((null!= strProjectRole && !"".equals(strProjectRole))|| (null!= strProjectAccess && !"".equals(strProjectAccess)))
					{
						mlProjectLeads.add(mapPersonInfo);   						
					}   
				}
			Person loggedInPerson = new Person(PersonUtil
					.getPersonObjectID(context));
			String strLoggedInPersonId = (String) loggedInPerson.getId(); 
			boolean bPLflag = false;
			for (Iterator itrPersons = mlProjectLeads.iterator(); itrPersons.hasNext();) 
			{
				Map mapPersonInfo = (Map)itrPersons.next();
				String strId = (String)mapPersonInfo.get(SELECT_ID);
				if(strId.equals(strLoggedInPersonId))
				{
					bPLflag = true;
					break;
				}
			}
			MapList mpTasksList = new MapList();
			if(bPLflag==true)
			{
				for (Iterator itrPersons = mapPersonList.iterator(); itrPersons.hasNext();) 
				{
					sbStrPersonMonth = new StringBuffer();
					sbStrPersonEfforts = new StringBuffer();
					sbStrPersonWeek = new StringBuffer();
					int ssize = 0;
					Map mapPersonRow = (Map)itrPersons.next();
					String strPersonName = (String)mapPersonRow.get(SELECT_NAME);
					String sPersonId= (String)mapPersonRow.get(SELECT_ID);
					mapPersonRow.put(SELECT_LEVEL, "2");
					mapPersonRow.put(SELECT_NAME, strPersonName );
					mapPersonRow.put("Total Efforts", iEffort);
					mapPersonRow.put("Effort Month", sbStrPersonMonth);
					mapPersonRow.put("Effort Values",sbStrPersonEfforts);
					mapPersonRow.put("Effort Week",sbStrPersonWeek);
					mpPersonsForProject.add(mapPersonRow);
					ssize = mpPersonsForProject.size();
					mpTasksList = getTaskFromProject(context,strProjectId, strPersonName, mpPersonsForProject, ssize, strSelYear, strYearType);	
					mpPersonsForProject.addAll(mpTasksList);
				}
			}    		
			MapList mapResourceManagersList =  getResourceManagersAndCompanyList(context, strProjectId);
			MapList mapRMPersonList = new MapList();
			String strCompanyId = "";
			for (Iterator itrPerson = mapResourceManagersList.iterator(); itrPerson.hasNext();)
			{
				Map mpPerson = (Map)itrPerson.next();
				String strLevel = (String)mpPerson.get(SELECT_LEVEL);

				if ("2".equals(strLevel))
				{
					mapRMPersonList.add(mpPerson);
				}
				else if ("1".equals(strLevel))
				{
					strCompanyId = (String)mpPerson.get(SELECT_ID);
				}

			}
			boolean bRMFlag = false;
			for (Iterator itrPersons = mapRMPersonList.iterator(); itrPersons.hasNext();) 
			{
				Map mapPersonInfo = (Map)itrPersons.next();
				String strId = (String)mapPersonInfo.get(SELECT_ID);
				if(strId.equals(strLoggedInPersonId))
				{
					bRMFlag = true;
					break;
				}

			}
			MapList mpRMMembers = new MapList();
			if(bRMFlag==true)
			{
				MapList mpCompanyMembers = getCompanyMembers(context, strCompanyId);
				for (Iterator itrCompanyMembers = mpCompanyMembers.iterator(); itrCompanyMembers.hasNext();) 
				{
					Map mpCompanyMember = (Map)itrCompanyMembers.next();
					String strCompanyMemberId = (String)mpCompanyMember.get(SELECT_ID);
					if(slPersonIds.contains(strCompanyMemberId))
					{
						mpRMMembers.add(mpCompanyMember);
					}
				}
			}
			for (Iterator itrRMMembers = mpRMMembers.iterator(); itrRMMembers.hasNext();) 
			{
				int ssize = 0;
				iEffort = 0.0;
				sbStrPersonMonth = new StringBuffer();
				sbStrPersonEfforts = new StringBuffer();
				sbStrPersonWeek = new StringBuffer();
				Map mapPersonRow = (Map)itrRMMembers.next();
				String strRMPersonId = (String)mapPersonRow.get(SELECT_ID);
				if(!slPersonIds.contains(strPersonId))
				{
					String strPersonName = (String)mapPersonRow.get(SELECT_NAME);
					String sPersonId= (String)mapPersonRow.get(SELECT_ID);
					mapPersonRow.put(SELECT_LEVEL, "2");
					mapPersonRow.put(SELECT_NAME, strPersonName );
					mapPersonRow.put("Total Efforts", iEffort);
					mapPersonRow.put("Effort Month", sbStrPersonMonth);
					mapPersonRow.put("Effort Values",sbStrPersonEfforts);
					mapPersonRow.put("Effort Week",sbStrPersonWeek);
					mpPersonsForProject.add(mapPersonRow);
					ssize = mpPersonsForProject.size();
					mpTasksList = getTaskFromProject(context,strProjectId, strPersonName, mpPersonsForProject, ssize, strSelYear, strYearType);
					mpPersonsForProject.addAll(mpTasksList);
				}
			}
		}
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
		return mpPersonsForProject;
	}

	/**
	 * Gets Tasks Data for Specified Persons
	 * 
	 * @param context The Matrix Context object
	 * @param strProjectId
	 * @param strPersonName
	 * @param mpPersonsForProject
	 * @param ssize
	 * @param strSelYear
	 * @param strYearType
	 * @throws Exception 
	 * 			if operation fails 
	 */
	private MapList getTaskFromProject(Context context, String strProjectId, String strPersonName, MapList mpPersonsForProject, int ssize, String strSelYear, String strYearType) throws Exception {
		MapList mpTasksForPerson =  new MapList();
		try {
			int indexOfFiscal = strYearType.indexOf("Fiscal");
			CalendarType calendarType = null;
			if(indexOfFiscal == -1)
			{
				calendarType = CalendarType.ENGLISH;
			}
			else if(indexOfFiscal != -1)
			{
				calendarType = CalendarType.FISCAL;
			}
			MapList mlEfforts = getEffortsForLaborReportForProject(context, strProjectId, strSelYear, strYearType);
			double iTotalEffort = 0.0;
			for (Iterator itrEfforts = mlEfforts.iterator(); itrEfforts.hasNext();) 
			{
				Map mapEffortInfo = (Map) itrEfforts.next();
				double strTotalEfforts = (Double)mapEffortInfo.get(SELECT_EFFORT);
				iTotalEffort+= strTotalEfforts;
			}
			MapList mapTasksForProject = getAllTasksForLaborReportForProject(context, strProjectId,strSelYear, strYearType,iTotalEffort,strPersonName);
			String strWeekEndDate = "";
			Calendar cal = Calendar.getInstance();
			String strChildEffortVal = "";
			String strMonth = "";

			StringList slTaskEffortTotalVals = new StringList();
			StringList slEffortMonths = new StringList();
			String delimiter="|";
			StringList strMonthNames =  new StringList();
			String strEffort = "";

			Map mapTaskInfo = new HashMap();
			String strWeekNo = "";
			String strYear = "";
			String strEffortOriginator = "";
			String strTaskId = "";
			int iTaskSize = 0;   		
			StringBuffer sbDateAndEffort = new StringBuffer();
			for (Iterator itrTasks = mapTasksForProject.iterator(); itrTasks.hasNext();) 
			{
				double dEffort = 0.0; 
				StringBuffer sbStrMonth = new StringBuffer();
				StringBuffer sbStrEfforts =  new StringBuffer();
				StringBuffer sbStrWeek = new StringBuffer();  		
				mapTaskInfo = (Map) itrTasks.next();
				String strLevel = (String)mapTaskInfo.get(SELECT_LEVEL);
				Map mpParent = new HashMap();
				String strParentEfforts = "";
				double dParentEffort = 0.0;
				StringBuffer sbParentMonth = new StringBuffer();
				StringBuffer sbParentEffortValues = new StringBuffer();
				StringBuffer sbParentWeek = new StringBuffer();
				if(mapTaskInfo.containsKey(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT)) 
				{					
					if(mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("java.lang.String"))
					{
						strWeekEndDate = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);						
						StringBuffer sbDateAndEffort1 = (StringBuffer)mapTaskInfo.get("DateAndEffort");
						sbDateAndEffort.append(sbDateAndEffort1);						
						boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
						if(isEffortSelYear==true)
						{
							Date dDate = new Date(strWeekEndDate); 
							cal.setTime(dDate);
							strChildEffortVal = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
							strEffortOriginator = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_ORIGINATOR);
							if((null !=strChildEffortVal)&&(!"".equals(strChildEffortVal))&& (strEffortOriginator.equals(strPersonName)) )    
							{
								dEffort += (Double)Task.parseToDouble(strChildEffortVal.trim());

								Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
								strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
								strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
								strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

								sbStrMonth.append(strMonth);
								sbStrMonth.append("-");
								sbStrMonth.append(strYear);
								sbStrMonth.append(delimiter);

								sbStrWeek.append(strWeekNo);
								sbStrWeek.append("-");
								sbStrWeek.append(strYear);
								sbStrWeek.append(delimiter);
								sbStrEfforts.append(strChildEffortVal);
								sbStrEfforts.append(delimiter);
								mapTaskInfo.put(SELECT_LEVEL, "3");
								mapTaskInfo.put("Total Efforts",dEffort);
								mapTaskInfo.put("Effort Month",sbStrMonth );
								mapTaskInfo.put("Effort Values",sbStrEfforts );
								mapTaskInfo.put("Effort Week",sbStrWeek );
								String strParentId = (String)mapTaskInfo.get(SELECT_TASK_PARENT_ID);
								mpParent = (Map)mpPersonsForProject.get(ssize-1);
								String parentPhase = "";
								int intIndexOfPhase = 0;
								double dParentPhaseEffort = 0.0;
								StringBuffer sbParentPhaseMonth = new StringBuffer();
								StringBuffer sbParentPhaseEffortValues = new StringBuffer();
								StringBuffer sbParentPhaseWeek = new StringBuffer();

								for(int x=0;x<mpTasksForPerson.size();x++){
									Map mpPersonTask = (Map)mpTasksForPerson.get(x);
									String strPhaseId = (String)mpPersonTask.get(DomainConstants.SELECT_ID);
									if(strPhaseId.equalsIgnoreCase(strParentId)){
										parentPhase = (String)mpPersonTask.get("parentPhase");
										intIndexOfPhase = x;
										break;
									}
								}
								if((null !=parentPhase)&&(!"".equals(parentPhase)))    
								{
									for(int x=0;x<mpTasksForPerson.size();x++){
										Map mpParentPhase = (Map)mpTasksForPerson.get(x);
										String strPhaseId = (String)mpParentPhase.get(DomainConstants.SELECT_ID);	
										if(strPhaseId.equalsIgnoreCase(parentPhase)){
											mapTaskInfo.put(SELECT_LEVEL, "4");
											mapTaskInfo.put("parentPhase", parentPhase);

											dParentPhaseEffort = (Double)mpParentPhase.get("Total Efforts");
											sbParentPhaseMonth = (StringBuffer)mpParentPhase.get("Effort Month");
											sbParentPhaseEffortValues= (StringBuffer)mpParentPhase.get("Effort Values");
											sbParentPhaseWeek = (StringBuffer)mpParentPhase.get("Effort Week");

											dParentPhaseEffort+=dEffort;
											sbParentPhaseMonth.append(sbStrMonth);
											sbParentPhaseEffortValues.append(sbStrEfforts);
											sbParentPhaseWeek.append(sbStrWeek);

											mpParentPhase.put("Total Efforts", dParentPhaseEffort);
											mpParentPhase.put("Effort Month", sbParentPhaseMonth);
											mpParentPhase.put("Effort Values", sbParentPhaseEffortValues);
											mpParentPhase.put("Effort Week", sbParentPhaseWeek);
											break;
										}
									}
								}
								dParentEffort = (Double)mpParent.get("Total Efforts");
								sbParentMonth = (StringBuffer)mpParent.get("Effort Month");
								sbParentEffortValues= (StringBuffer)mpParent.get("Effort Values");
								sbParentWeek = (StringBuffer)mpParent.get("Effort Week");

								dParentEffort+=dEffort;
								sbParentMonth.append(sbStrMonth);
								sbParentEffortValues.append(sbStrEfforts);
								sbParentWeek.append(sbStrWeek);

								mpParent.put("Total Efforts", dParentEffort);
								mpParent.put("Effort Month", sbParentMonth);
								mpParent.put("Effort Values", sbParentEffortValues);
								mpParent.put("Effort Week", sbParentWeek);

								if((null !=parentPhase)&&(!"".equals(parentPhase))){
									mpTasksForPerson.add(intIndexOfPhase+1,mapTaskInfo);
								}else{
									mpTasksForPerson.add(mapTaskInfo);
								}
								iTaskSize = mpTasksForPerson.size();
								strTaskId = (String)mapTaskInfo.get(SELECT_ID);
							}
						}
					}
					else if (mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("matrix.util.StringList"))
					{	
						slTaskEffortTotalVals= (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
						StringList strEffortOriginators = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_ORIGINATOR);
						slEffortMonths = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);						
						StringBuffer sbDateAndEffort1 = (StringBuffer)mapTaskInfo.get("DateAndEffort");
						sbDateAndEffort.append(sbDateAndEffort1);
						int ilength = strEffortOriginators.size();
						for (int i=0; i<ilength ; i++)
						{
							strWeekEndDate = (String)slEffortMonths.get(i);
							boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
							if(isEffortSelYear==true)
							{
								Date dDate = new Date(strWeekEndDate); 
								cal.setTime(dDate);
								strEffortOriginator = (String)strEffortOriginators.get(i);

								if(strEffortOriginator.equalsIgnoreCase(strPersonName)){
									dEffort = 0.0; 
									sbStrMonth = new StringBuffer();
									sbStrEfforts =  new StringBuffer();
									sbStrWeek = new StringBuffer();  

									Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
									strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
									strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
									strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

									String sMonth = strMonth;
									sMonth = sMonth + "-" + strYear + delimiter;
									String sWeek = strWeekNo;
									sWeek = sWeek + "- " +strYear + delimiter;

									sbStrMonth.append(strMonth);
									sbStrMonth.append("-");
									sbStrMonth.append(strYear);
									sbStrMonth.append(delimiter);

									sbStrWeek.append(strWeekNo);
									sbStrWeek.append("-");
									sbStrWeek.append(strYear);
									sbStrWeek.append(delimiter);								             

									strMonthNames.addElement(strMonth);
									strEffort  = (String)slTaskEffortTotalVals.get(i);

									String sEffort = strEffort;
									sEffort = sEffort + delimiter;

									sbStrEfforts.append(strEffort);
									sbStrEfforts.append(delimiter);
									dEffort = (Double)Task.parseToDouble(strEffort.trim());

									if(!mpTasksForPerson.contains(mapTaskInfo))
									{
										mapTaskInfo.put(SELECT_LEVEL, "3");
										mapTaskInfo.put("Total Efforts",dEffort);
										mapTaskInfo.put("Effort Month",sbStrMonth );
										mapTaskInfo.put("Effort Values",sbStrEfforts );
										mapTaskInfo.put("Effort Week",sbStrWeek );
										mpParent = (Map)mpPersonsForProject.get(ssize-1);

										String parentPhase = "";
										int intIndexOfPhase = 0;
										String strParentId = (String)mapTaskInfo.get(SELECT_TASK_PARENT_ID);
										double dParentPhaseEffort = 0.0;
										StringBuffer sbParentPhaseMonth = new StringBuffer();
										StringBuffer sbParentPhaseEffortValues = new StringBuffer();
										StringBuffer sbParentPhaseWeek = new StringBuffer();
										for(int x=0;x<mpTasksForPerson.size();x++){
											Map mpPersonTask = (Map)mpTasksForPerson.get(x);
											String strPhaseId = (String)mpPersonTask.get(DomainConstants.SELECT_ID);
											if(strPhaseId.equalsIgnoreCase(strParentId)){
												parentPhase = (String)mpPersonTask.get("parentPhase");
												intIndexOfPhase = x;
												break;
											}
										}
										if((null !=parentPhase)&&(!"".equals(parentPhase)))    
										{
											for(int x=0;x<mpTasksForPerson.size();x++){
												Map mpParentPhase = (Map)mpTasksForPerson.get(x);
												String strPhaseId = (String)mpParentPhase.get(DomainConstants.SELECT_ID);	
												if(strPhaseId.equalsIgnoreCase(parentPhase)){
													mapTaskInfo.put(SELECT_LEVEL, "4");
													mapTaskInfo.put("parentPhase", parentPhase);

													dParentPhaseEffort = (Double)mpParentPhase.get("Total Efforts");
													sbParentPhaseMonth = (StringBuffer)mpParentPhase.get("Effort Month");
													sbParentPhaseEffortValues= (StringBuffer)mpParentPhase.get("Effort Values");
													sbParentPhaseWeek = (StringBuffer)mpParentPhase.get("Effort Week");

													dParentPhaseEffort+=dEffort;
													sbParentPhaseMonth.append(sbStrMonth);
													sbParentPhaseEffortValues.append(sbStrEfforts);
													sbParentPhaseWeek.append(sbStrWeek);

													mpParentPhase.put("Total Efforts", dParentPhaseEffort);
													mpParentPhase.put("Effort Month", sbParentPhaseMonth);
													mpParentPhase.put("Effort Values", sbParentPhaseEffortValues);
													mpParentPhase.put("Effort Week", sbParentPhaseWeek);
													break;
												}
											}
										}

										dParentEffort = (Double)mpParent.get("Total Efforts");
										sbParentMonth = (StringBuffer)mpParent.get("Effort Month");
										sbParentEffortValues= (StringBuffer)mpParent.get("Effort Values");
										sbParentWeek = (StringBuffer)mpParent.get("Effort Week");

										dParentEffort+=dEffort;
										sbParentMonth.append(sbStrMonth);
										sbParentEffortValues.append(sbStrEfforts);
										sbParentWeek.append(sbStrWeek);

										mpParent.put("Total Efforts", dParentEffort);
										mpParent.put("Effort Month", sbParentMonth);
										mpParent.put("Effort Values", sbParentEffortValues);
										mpParent.put("Effort Week", sbParentWeek);

										String strTaskParentId = (String)mapTaskInfo.get(SELECT_TASK_PARENT_ID);
										if((null !=parentPhase)&&(!"".equals(parentPhase))){
											mpTasksForPerson.add(intIndexOfPhase+1,mapTaskInfo);
										}else{
											mpTasksForPerson.add(mapTaskInfo);
										}
										iTaskSize = mpTasksForPerson.size();
										strTaskId = (String)mapTaskInfo.get(SELECT_ID);
									}
									else 
									{
										mapTaskInfo = new HashMap();
										mapTaskInfo = (Map)mpTasksForPerson.get(iTaskSize-1);
										strTaskId = (String)mapTaskInfo.get(SELECT_ID);
										StringBuffer sbProcessedIds = (StringBuffer)mapTaskInfo.get("processedChilds");
										boolean tobeProcessed = true;
										String strProcessedIds = "";
										if((null !=sbProcessedIds)&&(!"".equals(sbProcessedIds)))    
										{
											strProcessedIds = sbProcessedIds.toString();
											if(strProcessedIds.indexOf(strTaskId)==-1){
												tobeProcessed = false;
											}
										}

										if(tobeProcessed){
											double dPreviousEffort = (Double)mapTaskInfo.get("Total Efforts");
											StringBuffer sbPreviousMonth = (StringBuffer)mapTaskInfo.get("Effort Month");
											StringBuffer sbPreviousEffortValues = (StringBuffer)mapTaskInfo.get("Effort Values");
											StringBuffer sbPreviousWeek = (StringBuffer)mapTaskInfo.get("Effort Week");

											dPreviousEffort+=dEffort;
											sbPreviousMonth.append(sbStrMonth);
											sbPreviousEffortValues.append(sbStrEfforts);
											sbPreviousWeek.append(sbStrWeek);

											mapTaskInfo.put("Total Efforts", dPreviousEffort);
											mapTaskInfo.put("Effort Month", sbPreviousMonth);
											mapTaskInfo.put("Effort Values", sbPreviousEffortValues);
											mapTaskInfo.put("Effort Week", sbPreviousWeek);

											mpParent = (Map)mpPersonsForProject.get(ssize-1);
											String parentPhase = "";
											int intIndexOfPhase = 0;
											String strParentId = (String)mapTaskInfo.get(SELECT_TASK_PARENT_ID);
											double dParentPhaseEffort = 0.0;
											StringBuffer sbParentPhaseMonth = new StringBuffer();
											StringBuffer sbParentPhaseEffortValues = new StringBuffer();
											StringBuffer sbParentPhaseWeek = new StringBuffer();

											for(int x=0;x<mpTasksForPerson.size();x++){
												Map mpPersonTask = (Map)mpTasksForPerson.get(x);
												String strPhaseId = (String)mpPersonTask.get(DomainConstants.SELECT_ID);
												if(strPhaseId.equalsIgnoreCase(strParentId)){
													parentPhase = (String)mpPersonTask.get("parentPhase");
													intIndexOfPhase = x;
													break;
												}
											}
											if((null !=parentPhase)&&(!"".equals(parentPhase)))    
											{
												for(int x=0;x<mpTasksForPerson.size();x++){
													Map mpParentPhase = (Map)mpTasksForPerson.get(x);
													String strPhaseId = (String)mpParentPhase.get(DomainConstants.SELECT_ID);	
													if(strPhaseId.equalsIgnoreCase(parentPhase)){

														dParentPhaseEffort = (Double)mpParentPhase.get("Total Efforts");
														sbParentPhaseMonth = (StringBuffer)mpParentPhase.get("Effort Month");
														sbParentPhaseEffortValues= (StringBuffer)mpParentPhase.get("Effort Values");
														sbParentPhaseWeek = (StringBuffer)mpParentPhase.get("Effort Week");

														dParentPhaseEffort+=dEffort;
														sbParentPhaseMonth.append(sbStrMonth);
														sbParentPhaseEffortValues.append(sbStrEfforts);
														sbParentPhaseWeek.append(sbStrWeek);

														mpParentPhase.put("Total Efforts", dParentPhaseEffort);
														mpParentPhase.put("Effort Month", sbParentPhaseMonth);
														mpParentPhase.put("Effort Values", sbParentPhaseEffortValues);
														mpParentPhase.put("Effort Week", sbParentPhaseWeek);
														break;
													}
												}
											}
											dParentEffort = (Double)mpParent.get("Total Efforts");
											sbParentMonth = (StringBuffer)mpParent.get("Effort Month");
											sbParentEffortValues= (StringBuffer)mpParent.get("Effort Values");
											sbParentWeek = (StringBuffer)mpParent.get("Effort Week");

											dParentEffort+=dEffort;
											sbParentMonth.append(sMonth);
											sbParentEffortValues.append(sEffort);
											sbParentWeek.append(sWeek);

											mpParent.put("Total Efforts", dParentEffort);
											mpParent.put("Effort Month", sbParentMonth);
											mpParent.put("Effort Values", sbParentEffortValues);
											mpParent.put("Effort Week", sbParentWeek);
										}
									}						
								}
							}
						}
					}					
					mpParent.put("DateAndEffort",sbDateAndEffort);
					mpParent.put("Total Efforts1", getTotalForReport(sbDateAndEffort));						
				}				
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return mpTasksForPerson;
	}

	/**
	 * Gets Child Tasks Data for Specified Phase
	 * 
	 * @param context The Matrix Context object
	 * @param strTaskId
	 * @param mapTasksForProject
	 * @param strPersonName
	 * @param iTaskSize
	 * @param ssize
	 * @param mpPersonsForProject
	 * @param strSelYear
	 * @param strYearType
	 * @throws Exception if operation fails 
	 */
	private MapList getChildTasksForPhase(Context context, String strTaskId, MapList mapTasksForProject, String strPersonName, int iTaskSize, int ssize, MapList mpPersonsForProject, String strSelYear, String strYearType) throws MatrixException {
		MapList mpChildTasks = new MapList();
		try {

			CalendarType calendarType= getCalendarType(context, strYearType);
			Map mapTaskInfo = null;
			Calendar cal = Calendar.getInstance();
			String strChildEffortVal = "";
			String strMonth = "";
			StringBuffer sbStrMonth = new StringBuffer();
			StringBuffer sbStrEfforts =  new StringBuffer();
			StringBuffer sbStrWeek = new StringBuffer();  		
			StringList slTaskEffortTotalVals = new StringList();
			StringList slEffortMonths = new StringList();
			String delimiter="|";
			StringList strMonthNames =  new StringList();
			String strEffort = "";
			StringBuffer sbProcessedChilds = new StringBuffer();
			String strWeekNo = "";
			String strYear = "";
			String strEffortOriginator = "";
			String strWeekEndDate = "";    		
			StringBuffer sbDateAndEffort = new StringBuffer();
			for (Iterator itrTasks = mapTasksForProject.iterator(); itrTasks.hasNext();) 
			{
				double dEffort = 0.0;
				sbStrMonth = new StringBuffer();
				sbStrEfforts =  new StringBuffer();
				sbStrWeek = new StringBuffer();  		
				mapTaskInfo = (Map) itrTasks.next();
				String strLevel = (String)mapTaskInfo.get(SELECT_LEVEL);
				String strTaskParentId = (String)mapTaskInfo.get(SELECT_TASK_PARENT_ID);
				String strTaskOriginator = (String)mapTaskInfo.get(SELECT_ORIGINATOR);
				if(strTaskParentId.equals(strTaskId))
				{
					//
					//Declarations for Phase Objects
					//
					Map mpParent = new HashMap();
					String strParentEfforts = "";
					double dParentEffort = 0.0;
					StringBuffer sbParentMonth = new StringBuffer();
					StringBuffer sbParentEffortValues = new StringBuffer();
					StringBuffer sbParentWeek = new StringBuffer();
					//
					//Declarations for Person Objects
					//
					Map mpPersonParent = new HashMap();
					String strPersonParentEfforts = "";
					double dPersonParentEffort = 0.0;
					StringBuffer sbPersonParentMonth = new StringBuffer();
					StringBuffer sbPersonParentEffortValues = new StringBuffer();
					StringBuffer sbPersonParentWeek = new StringBuffer();

					if(mapTaskInfo.containsKey(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT)) 
					{
						if(mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("java.lang.String"))
						{
							strWeekEndDate = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);						
							StringBuffer sbDateAndEffort1 = (StringBuffer)mapTaskInfo.get("DateAndEffort");
							sbDateAndEffort.append(sbDateAndEffort1);						
							boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
							if(isEffortSelYear==true)
							{
								Date dDate = new Date(strWeekEndDate); 
								cal.setTime(dDate);
								strChildEffortVal = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
								strEffortOriginator = (String) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_ORIGINATOR);
								if((null !=strChildEffortVal)&&(!"".equals(strChildEffortVal))&& (strEffortOriginator.equals(strPersonName)) )    
								{
									dEffort += (Double)Task.parseToDouble(strChildEffortVal.trim());
									Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
									strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
									strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
									strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

									sbStrMonth.append(strMonth);
									sbStrMonth.append("-");
									sbStrMonth.append(strYear);
									sbStrMonth.append(delimiter);

									sbStrWeek.append(strWeekNo);
									sbStrWeek.append("-");
									sbStrWeek.append(strYear);
									sbStrWeek.append(delimiter);
									sbStrEfforts.append(strChildEffortVal);
									sbStrEfforts.append(delimiter);
									mapTaskInfo.put(SELECT_LEVEL, "4");
									mapTaskInfo.put("Total Efforts",dEffort);

									mapTaskInfo.put("Effort Month",sbStrMonth );
									mapTaskInfo.put("Effort Values",sbStrEfforts );
									mapTaskInfo.put("Effort Week",sbStrWeek );

									mpParent = (Map)mapTasksForProject.get(iTaskSize-1);
									mapTaskInfo.put("parentPhase",(String)mpParent.get(DomainConstants.SELECT_ID) );
									mapTaskInfo.put("parentPhaseIndex",iTaskSize-1);
									dParentEffort = (Double)mpParent.get("Total Efforts");
									sbParentMonth = (StringBuffer)mpParent.get("Effort Month");
									sbParentEffortValues= (StringBuffer)mpParent.get("Effort Values");
									sbParentWeek = (StringBuffer)mpParent.get("Effort Week");

									dParentEffort+=dEffort;
									sbParentMonth.append(sbStrMonth);
									sbParentEffortValues.append(sbStrEfforts);
									sbParentWeek.append(sbStrWeek);

									mpParent.put("Total Efforts", dParentEffort);
									mpParent.put("Effort Month", sbParentMonth);

									mpParent.put("Effort Values", sbParentEffortValues);
									mpParent.put("Effort Week", sbParentWeek);

									mpParent.put("processedChilds", sbProcessedChilds.append((String)mapTaskInfo.get(DomainConstants.SELECT_ID)+"|"));

									mpPersonParent = (Map)mpPersonsForProject.get(ssize-1);
									dPersonParentEffort = (Double)mpPersonParent.get("Total Efforts");
									sbPersonParentMonth = (StringBuffer)mpPersonParent.get("Effort Month");
									sbPersonParentEffortValues= (StringBuffer)mpPersonParent.get("Effort Values");
									sbPersonParentWeek = (StringBuffer)mpPersonParent.get("Effort Week");

									dPersonParentEffort+=dEffort;
									sbPersonParentMonth.append(sbStrMonth);
									sbPersonParentEffortValues.append(sbStrEfforts);
									sbPersonParentWeek.append(sbStrWeek);

									mpPersonParent.put("Total Efforts", dPersonParentEffort);
									mpPersonParent.put("Effort Month", sbPersonParentMonth);
									mpPersonParent.put("Effort Values", sbPersonParentEffortValues);
									mpPersonParent.put("Effort Week", sbPersonParentWeek);
									mpChildTasks.add(mapTaskInfo);
								}
							}
						}
						else if (mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT).getClass().getName().equals("matrix.util.StringList"))
						{	
							slTaskEffortTotalVals= (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_TOTAL_EFFORT);
							StringList strEffortOriginators = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_ORIGINATOR);
							slEffortMonths = (StringList) mapTaskInfo.get(SELECT_TASK_TO_EFFORT_WEEK_END_DATE);					
							StringBuffer sbDateAndEffort1 = (StringBuffer)mapTaskInfo.get("DateAndEffort");
							sbDateAndEffort.append(sbDateAndEffort1);						 
							int ilength = slTaskEffortTotalVals.size();
							int iChildTask=0;
							for (int i=0; i<ilength ; i++)
							{
								strWeekEndDate = (String)slEffortMonths.get(i);
								boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
								if(isEffortSelYear==true)
								{
									Date dDate = new Date(strWeekEndDate); 
									cal.setTime(dDate);
									strEffortOriginator = (String)strEffortOriginators.get(i);
									if(strEffortOriginator.equalsIgnoreCase(strPersonName))
									{
										Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
										strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
										strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
										strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

										String sMonth = strMonth;
										sMonth= sMonth + "-" + strYear + delimiter;

										String sWeekNo = strWeekNo;
										sWeekNo= sWeekNo + "-" + strYear + delimiter;

										sbStrMonth.append(strMonth);
										sbStrMonth.append("-");
										sbStrMonth.append(strYear);
										sbStrMonth.append(delimiter);

										sbStrWeek.append(strWeekNo);
										sbStrWeek.append("-");
										sbStrWeek.append(strYear);
										sbStrWeek.append(delimiter);								             

										strMonthNames.addElement(strMonth);
										strEffort  = (String)slTaskEffortTotalVals.get(i);
										sbStrEfforts.append(strEffort);
										sbStrEfforts.append(delimiter);

										String sEffort = strEffort;
										sEffort = sEffort + delimiter;
										dEffort+= (Double)Task.parseToDouble(strEffort.trim());
										if(!mpChildTasks.contains(mapTaskInfo))
										{
											mapTaskInfo.put(SELECT_LEVEL, "4");
											mapTaskInfo.put("Total Efforts",dEffort);
											mapTaskInfo.put("Effort Month",sbStrMonth );
											mapTaskInfo.put("Effort Values",sbStrEfforts );
											mapTaskInfo.put("Effort Week",sbStrWeek );

											mpParent = (Map)mapTasksForProject.get(iTaskSize-1);
											mapTaskInfo.put("parentPhase",(String)mpParent.get(DomainConstants.SELECT_ID) );
											mapTaskInfo.put("parentPhaseIndex",iTaskSize-1);
											dParentEffort = (Double)mpParent.get("Total Efforts");
											sbParentMonth = (StringBuffer)mpParent.get("Effort Month");
											sbParentEffortValues= (StringBuffer)mpParent.get("Effort Values");
											sbParentWeek = (StringBuffer)mpParent.get("Effort Week");

											dParentEffort+=dEffort;
											sbParentMonth.append(sbStrMonth);
											sbParentEffortValues.append(sbStrEfforts);
											sbParentWeek.append(sbStrWeek);

											mpParent.put("Total Efforts", dParentEffort);
											mpParent.put("Effort Month", sbParentMonth);
											mpParent.put("Effort Values", sbParentEffortValues);
											mpParent.put("Effort Week", sbParentWeek);	
											mpParent.put("processedChilds", sbProcessedChilds.append((String)mapTaskInfo.get(DomainConstants.SELECT_ID)+"|"));

											mpPersonParent = (Map)mpPersonsForProject.get(ssize-1);
											dPersonParentEffort = (Double)mpPersonParent.get("Total Efforts");
											sbPersonParentMonth = (StringBuffer)mpPersonParent.get("Effort Month");
											sbPersonParentEffortValues= (StringBuffer)mpPersonParent.get("Effort Values");
											sbPersonParentWeek = (StringBuffer)mpPersonParent.get("Effort Week");

											dPersonParentEffort+=dEffort;
											sbPersonParentMonth.append(sbStrMonth);
											sbPersonParentEffortValues.append(sbStrEfforts);
											sbPersonParentWeek.append(sbStrWeek);

											mpPersonParent.put("Total Efforts", dPersonParentEffort);
											mpPersonParent.put("Effort Month", sbPersonParentMonth);
											mpPersonParent.put("Effort Values", sbPersonParentEffortValues);
											mpPersonParent.put("Effort Week", sbPersonParentWeek);

											mpChildTasks.add(mapTaskInfo);
											iChildTask = mpChildTasks.size();
										}
										else
										{
											mapTaskInfo = (Map)mpChildTasks.get(iChildTask-1);
											double dChildEffort = (Double)Task.parseToDouble(strEffort.trim());
											mapTaskInfo.put("Total Efforts",dEffort);
											mapTaskInfo.put("Effort Month",sbStrMonth );
											mapTaskInfo.put("Effort Values",sbStrEfforts );
											mapTaskInfo.put("Effort Week",sbStrWeek );

											mpParent = (Map)mapTasksForProject.get(iTaskSize-1);
											dParentEffort = (Double)mpParent.get("Total Efforts");
											sbParentMonth = (StringBuffer)mpParent.get("Effort Month");
											sbParentEffortValues= (StringBuffer)mpParent.get("Effort Values");
											sbParentWeek = (StringBuffer)mpParent.get("Effort Week");

											dParentEffort+=dChildEffort;
											sbParentMonth.append(sMonth);
											sbParentEffortValues.append(sEffort);
											sbParentWeek.append(sWeekNo);

											mpParent.put("Total Efforts", dParentEffort);
											mpParent.put("Effort Month", sbParentMonth);
											mpParent.put("Effort Values", sbParentEffortValues);
											mpParent.put("Effort Week", sbParentWeek);	
											mpParent.put("processedChilds", sbProcessedChilds.append((String)mapTaskInfo.get(DomainConstants.SELECT_ID)+"|"));

											mpPersonParent = (Map)mpPersonsForProject.get(ssize-1);
											dPersonParentEffort = (Double)mpPersonParent.get("Total Efforts");
											sbPersonParentMonth = (StringBuffer)mpPersonParent.get("Effort Month");
											sbPersonParentEffortValues= (StringBuffer)mpPersonParent.get("Effort Values");
											sbPersonParentWeek = (StringBuffer)mpPersonParent.get("Effort Week");

											dPersonParentEffort+=dChildEffort;
											sbPersonParentMonth.append(sMonth);
											sbPersonParentEffortValues.append(sEffort);
											sbPersonParentWeek.append(sWeekNo);

											mpPersonParent.put("Total Efforts", dPersonParentEffort);
											mpPersonParent.put("Effort Month", sbPersonParentMonth);
											mpPersonParent.put("Effort Values", sbPersonParentEffortValues);
											mpPersonParent.put("Effort Week", sbPersonParentWeek);
										}
									}
								}
							}
						}						
						mpParent.put("DateAndEffort",sbDateAndEffort);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mpChildTasks;
	}

	/**
	 * This method is used to get member from company.
	 * @param context The Matrix Context object
	 * @param strCompanyId
	 * @throws Exception if operation fails 
	 */
	private MapList getCompanyMembers(Context context, String strCompanyId)throws MatrixException {
		MapList mpMembers = new MapList();		
		String strRelPattern =  PropertyUtil.getSchemaProperty(context,"relationship_Member");		
		String strTypePattern = PropertyUtil.getSchemaProperty(context,"type_Person");

		StringList sbBusSelect = new StringList();
		sbBusSelect.add(SELECT_TYPE);
		sbBusSelect.add(SELECT_NAME);
		sbBusSelect.add(SELECT_ID);
		StringList sbRelSelect = new StringList();	
		final boolean GET_TO = true;
		final boolean GET_FROM = true;
		short recurseToLevel = 1;
		String strObjectWhere = "";
		String strRelWhere = "";
		try {
			DomainObject dCompanyId = DomainObject.newInstance(context,strCompanyId);
			mpMembers = dCompanyId.getRelatedObjects(context,strRelPattern, strTypePattern, sbBusSelect, sbRelSelect, GET_TO,GET_FROM, recurseToLevel, strObjectWhere, strRelWhere,0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mpMembers;
	}


	/**
	 * This method is used to get resource manager & company list.
	 * 
	 * @param context
	 * @param String
	 *            Containing the project id
	 * @return MapList
	 * @throws MatrixException
	 */
	private MapList getResourceManagersAndCompanyList(Context context, String strProjectId)throws MatrixException {
		MapList mapCompanyPersonList = new MapList();
		try {
			String strRelProjCompPersonPattern = PropertyUtil.getSchemaProperty(context,"relationship_CompanyProject")+","+PropertyUtil.getSchemaProperty(context,"relationship_ResourceManagers");
			String strTypeProjCompPersonPattern =  PropertyUtil.getSchemaProperty(context,"type_Company")+","+PropertyUtil.getSchemaProperty(context,"type_Person");
			final boolean GET_TO = true;
			final boolean GET_FROM = true;
			StringList sbBusSelect = new StringList();
			sbBusSelect.add(SELECT_TYPE);
			sbBusSelect.add(SELECT_NAME);
			sbBusSelect.add(SELECT_ID);

			StringList sbRelSelect = new StringList();			
			short recurseToLevel = 2;
			String strObjectWhere = "";
			String strRelWhere = "";
			int nLimit = 0;

			DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
			mapCompanyPersonList = dmoProject.getRelatedObjects(context,strRelProjCompPersonPattern, strTypeProjCompPersonPattern, sbBusSelect, sbRelSelect, GET_TO,GET_FROM, recurseToLevel, strObjectWhere, strRelWhere,0);

		} catch (Exception e) {

			e.printStackTrace();
		}
		return mapCompanyPersonList;
	}


	/**
	 * This method is used to get persons for project
	 * @param context
	 * @param strProjectId
	 * @return MapList
	 * @throws MatrixException
	 */
	private MapList getPersonForProject(Context context,
			String strProjectId)throws MatrixException {
		MapList mapPersonList = new MapList();
		try {
			final boolean GET_TO = true;
			final boolean GET_FROM = true;
			StringList sbBusSelect = new StringList();
			sbBusSelect.add(SELECT_TYPE);
			sbBusSelect.add(SELECT_NAME);
			sbBusSelect.add(SELECT_ID);

			StringList sbRelSelect = new StringList();
			sbRelSelect.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
			sbRelSelect.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ACCESS+"]");

			short recurseToLevel = 0;
			String strObjectWhere = null;

			int nLimit = 0;

			DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
			mapPersonList = dmoProject.getRelatedObjects(context,RELATIONSHIP_MEMBER, TYPE_PERSON, sbBusSelect, sbRelSelect, !GET_TO,GET_FROM, recurseToLevel, strObjectWhere, null, nLimit);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapPersonList;
	}

	/**
	 * This method is used to check efforts are in selected year.
	 * 
	 * @param context
	 *            The Matrix Context object
	 * @param strWeekEndDate
	 * @param strSelYear
	 * @param strYearType
	 * @throws Exception
	 *             if operation fails
	 */	
	private boolean isEfforsInTheSelectedYear(Context context, String strWeekEndDate, String strSelYear, String strYearType) throws Exception
	{ 
		boolean bflag = false;
		Date dtWeekEndDate = null;
		Date dtWeekStartDate = null;
		Interval[] range = null;
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		int modifiedDurationValue = 6* MILLIS_IN_DAY;

		dtWeekEndDate = eMatrixDateFormat.getJavaDate(strWeekEndDate);
		dtWeekStartDate = new Date(dtWeekEndDate.getTime() - modifiedDurationValue);

		int iSelYear = Integer.valueOf(strSelYear.trim());
		int weekStartDateYear = Helper.year(dtWeekStartDate);
		int weekEndDateYear = Helper.year(dtWeekEndDate);
		if(weekEndDateYear == iSelYear){
			bflag = true; 
		}else if(weekStartDateYear == iSelYear){
			bflag = true; 
		}
		return bflag;
	}
	/**
	 * This method used to get efforts & projects for timesheet in submit state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectId - Contains a id
	 *            of timesheet object
	 * @return MapList - List containing the projects and efforts in the form of
	 *         Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList displayTimesheetTasksSubmitted(Context context, String[] args)
			throws MatrixException {
		try {
			StringList stateList = new StringList(1);
			stateList.add(ProgramCentralConstants.STATE_EFFORT_SUBMIT);
			return displayTimesheetTasks(context,args,stateList); 
		} catch(Exception e){
			throw new MatrixException(e.getMessage());
		}
	}

	/**
	 * This method used to get efforts & projects for timesheet in Approved state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectId - Contains a id
	 *            of timesheet object
	 * @return MapList - List containing the projects and efforts in the form of
	 *         Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList displayTimesheetTasksApproved(Context context, String[] args)
			throws MatrixException {
		try {
			StringList stateList = new StringList(1);
			stateList.add(ProgramCentralConstants.STATE_EFFORT_APPROVED);
			return displayTimesheetTasks(context,args,stateList); 
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
	}	

	/**
	 * This method used to get efforts & projects for timesheet in Rejected state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectId - Contains a id
	 *            of timesheet object
	 * @return MapList - List containing the projects and efforts in the form of
	 *         Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList displayTimesheetTasksRejected(Context context, String[] args)
			throws MatrixException {
		try {
			StringList stateList = new StringList(1);
			stateList.add(ProgramCentralConstants.STATE_EFFORT_REJECTED);			
			return displayTimesheetTasks(context,args,stateList); 
		} catch(Exception e){
			throw new MatrixException(e.getMessage());
		}
	}	

	/**
	 * This method used to get efforts & projects for timesheet in All states.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.       
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList displayTimesheetTasksAll(Context context, String[] args)
			throws MatrixException {
		try{
			return displayTimesheetTasks(context,args,null);
		}catch(Exception e){
			throw new MatrixException(e.getMessage());
		}
	}

	/**
	 * This method used to get Weekly Timesheet objects.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @param filterState
	 *            Contains filter states in StringList
	 * @return MapList - List containing details of timesheet in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */
	public MapList displayTimesheetTasks(Context context, String[] args,StringList filterState)
			throws MatrixException {
		MapList timesheetTasks = new MapList();
		boolean isApprover = false;
		try{
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			String strPersonId = PersonUtil.getPersonObjectID(context);
			String objectId = (String) paramMap.get("timesheetID");
			DomainObject domObj = DomainObject.newInstance(context, objectId);
			String shasEffort_relationship = PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
			MapList mlEfforts = new MapList();
			MapList mlModifiedEffortsList = new MapList();
			StringList objectSelects = new StringList();
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.add("to["+RELATIONSHIP_COMPANY_PROJECT+"].from.id");
			objectSelects.add("attribute["+ATTRIBUTE_EFFORT_SUBMISSION+"]");
			String strPhase = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Type.Phase"); 
			String strTask = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Type.Task");
			if(domObj.isKindOf(context,
					TYPE_WEEKLY_TIMESHEET)) {
				WeeklyTimesheet weeklyTimesheet = new WeeklyTimesheet(objectId);
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);							
				try {
					mlEfforts = weeklyTimesheet.getEfforts(context, filterState, null, null);	
				}finally {	
					ContextUtil.popContext(context);
				}

				int size = mlEfforts.size();
				for(int i=0; i<size; i++)
				{
					Map mEffort = new HashMap();
					mEffort = (Map)mlEfforts.get(i);
					String strTaskId = (String)mEffort.get("to["+shasEffort_relationship+"].from.id");
					if(ProgramCentralUtil.isNotNullString(strTaskId))
					{	
						mlModifiedEffortsList.add(mEffort);
					}
				}

				int modSize = mlModifiedEffortsList.size();
				for(int i=0; i<modSize; i++){
					Map mEffort = new HashMap();
					mEffort = (Map)mlModifiedEffortsList.get(i);
					String strTaskId = (String)mEffort.get("to["+shasEffort_relationship+"].from.id");
					String strTaskType = (String)mEffort.get("to["+shasEffort_relationship+"].from.type");
					String strEffortId = (String)mEffort.get(DomainConstants.SELECT_ID);

					Task task = new Task(strTaskId);
					Map mp = task.getProject(context, objectSelects);
					String projectId = (String)mp.get(DomainConstants.SELECT_ID);
					String strType = "";
					mp.put("level", "1");
					boolean isSameProject = false;
					int index=0;
					if(timesheetTasks.size()>0){
						for(int j=0;j<timesheetTasks.size();j++){
							Map mpProject = (Map)timesheetTasks.get(j);
							String strProjectId = (String)mpProject.get(DomainConstants.SELECT_ID);
							if(strProjectId.equalsIgnoreCase(projectId)){
								index = j;
								isSameProject = true;
								break;
							}
						}
					}
					MapList approverList = getApprover(context,strEffortId);
					for(int idx=0; idx<approverList.size();idx++){
						Map approverMap = (Map)approverList.get(idx);
						String loginPersonId = PersonUtil.getPersonObjectID(context);
						if(loginPersonId.equals(approverMap.get(DomainConstants.SELECT_ID))){
							isApprover = true;
							if(!isSameProject){
								Map calculatedMap = getTotalEffortForProject(context,mp,mlModifiedEffortsList);
								calculatedMap.put("selection", "multiple");
								timesheetTasks.add(calculatedMap);
							}
						}
					}					
					mEffort.put("level", "2");
					mEffort.put("hasChildren", "false");
					mEffort.put("selection", "multiple");
					if(isApprover) {
						if(isSameProject) 
							timesheetTasks.add(index+1,mEffort);
						else 
							timesheetTasks.add(mEffort);						
					}
					isApprover = false;
				}
				if(modSize>0) {
					Map totalMap = new HashMap();
					totalMap.put(SELECT_ID, strPersonId);						
					totalMap.put("isTotalRow", "true");
					Map calTotalMap = getTotalEffort(context,totalMap,mlModifiedEffortsList);
					calTotalMap.put("hasChildren", "false");
					timesheetTasks.add(calTotalMap);
				}
			} else if(domObj.isKindOf(context,
					TYPE_PROJECT_SPACE)) {
				return expandProjects(context,args);				
			}
			HashMap hmTemp = new HashMap();
			hmTemp.put("expandMultiLevelsJPO","true");  
			timesheetTasks.add(hmTemp);
		}catch(Exception e){
			throw new MatrixException(e.getMessage());		
		} 	
		return timesheetTasks;
	}	

	/**
	 * This method used to get approver details for a timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *           effortId
	 * @return MapList - List containing details of approvers in the form of Map
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */	
	public MapList getApprover(Context context,String effortId) throws MatrixException {
		MapList approverMapList = new MapList();
		String strTaskId = null;
		try {
			Person person = new Person(PersonUtil
					.getPersonObjectID(context));
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);						
			try {
				DomainObject dObjEffort = DomainObject.newInstance(context, effortId);
				strTaskId = dObjEffort.getInfo(context, "to["+RELATIONSHIP_HAS_EFFORTS+"].from.id");

				Task task = new Task(strTaskId);
				Map projectMap = task.getProject(context, null);		
				String strProjectId = (String)projectMap.get(DomainConstants.SELECT_ID);			
				DomainObject dObjProject = DomainObject.newInstance(context, strProjectId);			
				String strProjectLead = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Common.ProjectLead", context.getSession().getLanguage());
				String strResourceMgr = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Common.ResourceManager", context.getSession().getLanguage());
				String strPreference = null;
				String strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");
				if(SEELCT_APPROVER_RESOURCE_MANAGER.equals(strApprover)){
					strPreference = strResourceMgr;
				} else if(SEELCT_APPROVER_PROJECT_LEAD.equals(strApprover)){
					strPreference = strProjectLead;
				}
				StringBuffer strRelWhere = new StringBuffer();
				if(strPreference.equals(strProjectLead) == true) {
					MapList accessList1=ProjectSpace.getOwnershipAccess(context, strProjectId);
					MapList leadList = new MapList();
					Iterator itr = accessList1.iterator();
					while(itr.hasNext())
					{
						Map list=(Map)itr.next();
						Map map=new HashMap();
						String name=(String)list.get(ProjectSpace.KEY_PERSON_NAME);
						String isPersonOwnership=(String)list.get(ProjectSpace.KEY_IS_PERSON_OWNERSHIP);
						if(ProgramCentralUtil.isNotNullString(name) && isPersonOwnership.equalsIgnoreCase("true")){
							String id=PersonUtil.getPersonObjectID(context, name);
							String access=(String)list.get(DomainAccess.KEY_ACCESS_GRANTED);
							if(access.equals(ROLE_PROJECT_LEAD)){
								map.put(DomainConstants.SELECT_ID, id);
								map.put(DomainConstants.SELECT_NAME, name);
								leadList.add(map);
							}
						}

					}
					approverMapList = leadList;
				} else if(strPreference.equals(strResourceMgr) == true) {
					String strCompanyId = person.getInfo(context, "to["+DomainConstants.RELATIONSHIP_EMPLOYEE+"].from.id");
					StringList busSel = new StringList(2);
					busSel.add(DomainConstants.SELECT_ID);
					busSel.add(DomainConstants.SELECT_NAME);				
					StringList list = getDeptAndBusinessUnitIds(context,strCompanyId,person);
					MapList rmMapList = new MapList();
					for(int k=0;k<list.size();k++) {
						DomainObject dobjRM = DomainObject.newInstance(context,list.get(k).toString());
						MapList rmList = dobjRM.getRelatedObjects(context,
								RELATIONSHIP_RESOURCE_MANAGER,
								TYPE_PERSON,
								busSel,
								null,
								false,
								true,
								(short) 1,
								null, 
								null,0);	
						for(int j=0;j<rmList.size();j++) {
							Map rmMap = (Map)rmList.get(j);
							if(!rmMapList.contains(rmMap)){
								rmMapList.add(rmMap);
							}
						}					
					}
					approverMapList = rmMapList;
				}		
			} finally {			
				ContextUtil.popContext(context);			
			}	
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		} 	
		return approverMapList;
	}

	/**
	 * This method used to get name of approver.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing details of approver.
	 * @throws Exception
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */	
	public Vector getApprover(Context context, String[] args) throws MatrixException {
		Vector vecApprover = new Vector();
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			HashMap paramList = (HashMap) paramMap.get("paramList");
			String exportFormat = (String)paramList.get("exportFormat");
			Iterator objectListIterator = objectList.iterator();
			while (objectListIterator.hasNext()) {
				Map mapObject = (Map) objectListIterator.next();
				String objectId = (String)mapObject.get(DomainConstants.SELECT_ID);
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				boolean isWBSEffortObj = false;
				try {
					DomainObject domObj = DomainObject.newInstance(context,objectId);
					if(domObj.isKindOf(context,TYPE_EFFORT) ||(domObj.isKindOf(context,TYPE_PHASE) || domObj.isKindOf(context,TYPE_TASK)))
					{
						isWBSEffortObj = true;
					}
				}
				finally {
					ContextUtil.popContext(context);
				}
				String isTotalRow = (String)mapObject.get("isTotalRow");	
				if(isTotalRow!=null && "true".equals(isTotalRow)) {
					vecApprover.add("");
				} else {
					if(isWBSEffortObj){
						MapList approverList = getApprover(context,objectId);				    
						if(approverList.size() > 0){	
							String imageStr = "../common/images/iconSmallPerson.gif";
							StringBuffer sbApproverLink = new StringBuffer();
							for(int i=0;i<approverList.size();i++) {
								Map approverMap = (Map)approverList.get(i);
								String pesonName = PersonUtil.getFullName(context,approverMap.get(DomainConstants.SELECT_NAME).toString());
								if("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){									 
									sbApproverLink.append(pesonName);
								}
								else{								
									sbApproverLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
									sbApproverLink.append("<a href=\"JavaScript:showModalDialog('../common/emxTree.jsp?objectId=");
									sbApproverLink.append(approverMap.get(DomainConstants.SELECT_ID));
									sbApproverLink.append("','700','600','false','popup')\">");                    
									sbApproverLink.append(pesonName);
									sbApproverLink.append("</a>");		
								}
							}
							vecApprover.addElement(sbApproverLink.toString());
						} else {
							vecApprover.addElement("");
						}				    
					} else {
						vecApprover.addElement("");
					}	
				}								
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}										
		return vecApprover;
	}

	/**
	 * This method used to get state for each object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing state of object.
	 * @throws MatrixException
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */		
	public Vector getState(Context context, String[] args) throws MatrixException {
		Vector vState = new Vector();
		String strState = null;
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();									
			DomainObject domObj = null;
			while (objectListIterator.hasNext()) {
				Map mapObject = (Map) objectListIterator.next();
				String objectId = (String)mapObject.get(DomainConstants.SELECT_ID);
				String isTotalRow = (String)mapObject.get("isTotalRow");	

				//[MODIFIED:Di7:10-Aug-2011:PRG:IR-120082V6R2012x:IR-121336V6R2012x::Start]
				if (!"true".equalsIgnoreCase(isTotalRow))
				{
					//MODIFIED::PA4:13-Jun-2011:IR-100970V6R2012x:START
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						if(objectId!= null) {

							domObj = DomainObject.newInstance(context,objectId);
							if(domObj.isKindOf(context, TYPE_PROJECT_SPACE)){
								strState = "";
							}
							else{
								strState = (String)domObj.getInfo(context,DomainConstants.SELECT_CURRENT);
							}

							if(ProgramCentralConstants.STATE_EFFORT_EXISTS.equals(strState))
							{
								strState = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
										"emxProgramCentral.Common.Create", context.getSession().getLanguage());
							}				
							else
							{
								String sLanguage = context.getSession().getLanguage();
								StringList slObjectSelects = new StringList();
								slObjectSelects.add(SELECT_POLICY);
								Map mapObjectInfo    = domObj.getInfo(context,slObjectSelects);
								String sObjectPolicy = (String) mapObjectInfo.get(SELECT_POLICY);
								strState = i18nNow.getStateI18NString(sObjectPolicy, strState, sLanguage);
							}							
							vState.add(strState);
						}
					} finally {
						ContextUtil.popContext(context);
						//MODIFIED::PA4:13-Jun-2011:IR-100970V6R2012x:END
					}
				}
				else
				{
					vState.add("");
				}
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		} 	
		return vState;
	}		

	/**
	 * This method used to get rejection comments for effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing details of rejection comments for effort object.
	 * @throws MatrixException
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */	
	public Vector getRejectionComments(Context context,String[] args) throws MatrixException {
		Vector vRejComments = new Vector();
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			for(int i=0;i<objectList.size();i++) {
				Map objectMap = (Map)objectList.get(i);
				String objectId = (String)objectMap.get(DomainConstants.SELECT_ID);	
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				try {
					DomainObject domObj = DomainObject.newInstance(context,objectId);
					String isTotalRow = (String)objectMap.get("isTotalRow");	
					if(isTotalRow!=null && "true".equals(isTotalRow)) {
						vRejComments.add("");
					} else {
						if(domObj.isKindOf(context, TYPE_EFFORT)){
							//[ADDED:Di7:25-07-2011:PRG:IR-083875V6R2012x:START]
							StringList objList = new StringList();
							objList.add(DomainConstants.SELECT_CURRENT);

							Map map = domObj.getInfo(context, objList);
							String strState = (String) map.get(SELECT_CURRENT);
							String strComment = "";
							if(strState.equals(ProgramCentralConstants.STATE_EFFORT_APPROVED))
							{
								strComment = domObj.getAttributeValue(context,ATTRIBUTE_EFFORT_COMMENTS);
							}
							else
							{
								strComment = domObj.getAttributeValue(context,ATTRIBUTE_APPROVER_COMMENTS);
							}
							vRejComments.add(strComment);
							//[ADDED:Di7:25-07-2011:PRG:IR-083875V6R2012x:END]
						} else {
							vRejComments.add("");
						}
					}
				}	
				finally {			
					ContextUtil.popContext(context);			
				}	
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		}	
		return vRejComments;			
	}

	/**
	 * Returns timeline intervals for labor report 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request arguments
	 * @return a Map of intervals.
	 * @throws Exception if operation fails.
	 */		
	public HashMap getTimeLineInterval(Context context, String[] args)
			throws Exception
			{
		try{
			StringList strList = new StringList();
			HashMap timeIntervalMap = new HashMap();
			StringList strListKey = new StringList();
			strListKey.add(SELECT_MONTHLY);
			strListKey.add(SELECT_WEEKLY);			
			String strResponseType = "";
			for(int iterator = 1; iterator <= 2; iterator++){
				strResponseType = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimesheetReport.DefaultTimeLineInterval" + iterator + ".Options" , context.getSession().getLanguage());
				strList.addElement(strResponseType);
			}
			timeIntervalMap.put("field_choices", strListKey);
			timeIntervalMap.put("field_display_choices", strList);			
			return timeIntervalMap;
		}
		catch(Exception ex)
		{
			throw ex;
		}
			}

	/**
	 * This method used to get values for ReportingYearBy field from properties
	 * file.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * 
	 * @return HashMap - containing Fiscal & Calendar values for ReportingYearBy
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public HashMap getReportYearBy(Context context, String[] args)
			throws Exception
			{
		try{
			StringList strList = new StringList();
			StringList strListKey = new StringList();
			HashMap statusMap = new HashMap();
			strListKey.add(SELECT_FISCAL);
			strListKey.add(SELECT_CALENDAR);
			String strResponseType = "";
			for(int iterator = 1; iterator <= 2; iterator++){
				strResponseType = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimesheetReport.ReportYearBy"+iterator+".Options", context.getSession().getLanguage());
				strList.addElement(strResponseType);
			}
			statusMap.put("field_choices", strListKey);
			statusMap.put("field_display_choices", strList);
			return statusMap;
		}
		catch(Exception ex)
		{
			throw ex;
		}
			}

	/**
	 * This method used to get values for Project state field from properties
	 * file.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * 
	 * @return HashMap - containing project state values.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public HashMap getStates(Context context, String[] args)
			throws Exception
			{
		try{
			StringList strList = new StringList();
			StringList strListKey = new StringList();
			HashMap stateMap = new HashMap();	
			strListKey.add(STATE_PROJECT_ALL);
			strListKey.add(STATE_PROJECT_ACTIVE);
			strListKey.add(STATE_PROJECT_COMPLETE);
			String strResponseType = "";
			for(int iterator = 1; iterator <= 3; iterator++){
				strResponseType = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimesheetReport.States"+iterator+".Options", context.getSession().getLanguage());

				strList.addElement(strResponseType);
			}
			stateMap.put("field_choices", strListKey);
			stateMap.put("field_display_choices", strList);			
			return stateMap;
		}
		catch(Exception ex)
		{
			throw ex;
		}
			}

	/**
	 * This method used to get value for reporting year.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * 
	 * @return String - containing the current year as reporting year value.
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public String getReportYear(Context context, String[] args){
		Date currentDate = new Date();
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyy");
		String endDate1 = sdf.format(currentDate);
		return endDate1;
	}

	/**
	 * This method used to display labor report by person.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            contains the paramMap.
	 * @return MapList
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList displayPersonForLaborReportByPerson(Context context, String[] args)
			throws Exception {
		MapList timesheetTasks = new MapList();
		try{
			Map paramMap 				= JPO.unpackArgs(args);
			Map requestMap 				= (Map)paramMap.get("RequestValuesMap");
			String YearType 			= (String)paramMap.get("YearType");
			String ContextUser 			= (String)paramMap.get("ContextUser");
			String selYear 				= (String)paramMap.get("selYear");
			String selTimeline 			= (String)paramMap.get("selTimeline");
			String strObjectId 			= (String)paramMap.get("objectId");
			String strTimesheetRowId	= (String)paramMap.get("strTimesheetRowId");
			String[] strObjectIds 		= (String[])requestMap.get("objectId");
			String personId 			= EMPTY_STRING;			
			String projectState 		= (String)paramMap.get("ProjectState");
			String projectOwner 		= (String)paramMap.get("ProjectOwner");
			StringBuilder stbObjWhere 	= new StringBuilder();
			String project_State 		= EMPTY_STRING;

			if(STATE_PROJECT_ALL.equals(projectState)){
				project_State = project_State;
			} else if(STATE_PROJECT_ACTIVE.equals(projectState)){
				stbObjWhere.append("(");
				stbObjWhere.append("current=="+STATE_PROJECT_ASSIGN + "||");
				stbObjWhere.append("current=="+STATE_PROJECT_ACTIVE + "||");
				stbObjWhere.append("current=="+STATE_PROJECT_REVIEW);
				stbObjWhere.append(")");
				project_State = stbObjWhere.toString();
			} else if(STATE_PROJECT_COMPLETE.equals(projectState)){
				stbObjWhere.append("current=="+projectState);
				project_State = stbObjWhere.toString();
			}

			StringList personIdList = new StringList();
			MapList members = new MapList();
			if(ProgramCentralUtil.isNotNullString(strTimesheetRowId)){
				personIdList = FrameworkUtil.split(strTimesheetRowId, "^");

				for(int i=0;i<personIdList.size();i++){
					Map mpPerson = new HashMap();
					personId 	 = (String)personIdList.get(i);

					mpPerson.put(DomainConstants.SELECT_ID, personId);
					mpPerson.put("level", "1");
					members.add(mpPerson);
				}
			}else{
				ProjectSpace projectSpace = new ProjectSpace(strObjectId);
				StringList objectSelects = new StringList();
				objectSelects.add(DomainConstants.SELECT_ID);
				members = projectSpace.getMembers(context, objectSelects, null, null, null);				
			}

			for(int i=0;i<members.size();i++){
				Map mpPerson = new HashMap();
				mpPerson = (Map)members.get(i);
				personId = (String)mpPerson.get(DomainConstants.SELECT_ID);
				mpPerson.put(DomainConstants.SELECT_ID, personId);
				mpPerson.put("level", "1");
				timesheetTasks.add(mpPerson);
			}

			for(int j=0;j<timesheetTasks.size();j++){
				Map mapPerson = (Map)timesheetTasks.get(j);
				String strPersonId = (String)mapPerson.get(DomainConstants.SELECT_ID);
				StringList objectSelects = new StringList();
				StringBuffer sbMonth = new StringBuffer();
				StringBuffer sbValue = new StringBuffer();
				StringBuffer sbWeek = new StringBuffer();
				Double total = 0.0;
				objectSelects.addElement(SELECT_ID);
				objectSelects.addElement(DomainConstants.SELECT_CURRENT);
				objectSelects.addElement("to["+RELATIONSHIP_EFFORTS+"].from.id");
				objectSelects.add(SELECT_EFFORT_PARENT_TASK_ID);
				objectSelects.addElement(SELECT_ATTRIBUTE_END_DATE);
				objectSelects.addElement(SELECT_ATTRIBUTE_TOTAL_EFFORT);
				Person person = new Person(strPersonId);
				person.getInfo(context, DomainConstants.SELECT_NAME);
				String strProjectOwnerId="";
				if(ProgramCentralUtil.isNotNullString(projectOwner))				
					strProjectOwnerId = PersonUtil.getPersonObjectID(context, projectOwner);	
				StringBuffer sbWhere = new StringBuffer();				
				String strRequestStartDate = "";
				String strRequestEndDate = "";			
				Date dtStartDate = null;
				Date dtFinishDate = null;
				MapList mlEfforts = new MapList();
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				CalendarType calendarType= getCalendarType(context, YearType);

				Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(selYear.trim())); 
				dtStartDate = interval.getStartDate();
				dtFinishDate = interval.getEndDate();
				strRequestStartDate = sdf.format(dtStartDate);
				strRequestEndDate = sdf.format(dtFinishDate);

				sbWhere.append("(");
				sbWhere.append(SELECT_ATTRIBUTE_ENDING_DATE + ">=\"" +strRequestStartDate+"\" && "+SELECT_ATTRIBUTE_ENDING_DATE + "<=\"" +strRequestEndDate+"\"");
				sbWhere.append(")");				
				sbWhere.append(" && ");
				sbWhere.append("(owner == '" +  person.getInfo(context, DomainConstants.SELECT_NAME)+"')");
				sbWhere.append(" && ");
				sbWhere.append("(");
				sbWhere.append("current == "+ProgramCentralConstants.STATE_EFFORT_APPROVED);
				sbWhere.append(")");

				MapList ml = DomainObject.findObjects(context,TYPE_EFFORT,QUERY_WILDCARD,"-",
						QUERY_WILDCARD,QUERY_WILDCARD,sbWhere.toString(),false,objectSelects);
				StringBuffer sbDateAndEffort = new StringBuffer();		

				for(int i1=0;i1<ml.size();i1++){
					Map mpEffort = (Map)ml.get(i1);
					String EffortId = (String)mpEffort.get(DomainConstants.SELECT_ID);
					String strWeekEndDate = (String)mpEffort.get(SELECT_ATTRIBUTE_END_DATE);
					String strTotal = (String)mpEffort.get(SELECT_ATTRIBUTE_TOTAL_EFFORT);					
					StringBuffer sbDateAndEffort1 = getWeekData(context, EffortId,selYear,YearType,person.getInfo(context, DomainConstants.SELECT_NAME));

					DomainObject objEffort = DomainObject.newInstance(context, EffortId);
					MapList projects = objEffort.getRelatedObjects(context,
							RELATIONSHIP_EFFORTS, TYPE_PROJECT_SPACE,objectSelects, null,
							true, false, (short)1,project_State,"",0,null,null,null);
					getProjectsOwnedByUser(context, strProjectOwnerId, projects);	//Modified:PRG:I16:R213:IR-122277V6R2013	Project Owner ID is passed
					if(projects.size() > 0) {					
						sbDateAndEffort.append(sbDateAndEffort1);	//Added From above :PRG:I16:R213:IR-122277V6R2013	
						Map calculatedMonthWeekTotal = calculatedMonthWeekTotal(context, strWeekEndDate, strTotal, 
								strTotal,selYear, YearType);
						StringBuffer Month = (StringBuffer)calculatedMonthWeekTotal.get("Month");
						sbMonth.append(Month);
						StringBuffer Week = (StringBuffer)calculatedMonthWeekTotal.get("Week");
						sbWeek.append(Week);
						StringBuffer Value = (StringBuffer)calculatedMonthWeekTotal.get("Total");
						sbValue.append(Value);
						Double dbTotal = (Double)calculatedMonthWeekTotal.get("Grand Total");
						total += dbTotal;
					}
				}
				mapPerson.put("Effort Month", sbMonth);
				mapPerson.put("Effort Values", sbValue);
				mapPerson.put("Effort Week", sbWeek);
				mapPerson.put("Total Efforts", total);				
				mapPerson.put("DateAndEffort", sbDateAndEffort);
				mapPerson.put("Total Efforts1", getTotalForReport(sbDateAndEffort));				
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		return timesheetTasks;
	}

	/**
	 * This method used to display labor report by phase.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            contains the paramMap.
	 * @return MapList
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */	

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList displayProjectsForLaborReportByPhase(Context context, String[] args)
			throws Exception {
		MapList projectList = new MapList();
		MapList ml = new MapList();
		try{									
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)paramMap.get("RequestValuesMap");
			String strObjectId = (String)paramMap.get("objectId");
			String YearType = (String)paramMap.get("YearType");
			String selYear = (String)paramMap.get("selYear");
			String selTimeline = (String)paramMap.get("selTimeline");
			String projectState = (String)paramMap.get("ProjectState");
			String projectOwner = (String)paramMap.get("ProjectOwner");
			StringBuffer stbObjWhere = new StringBuffer();
			String project_State = null;
			String strProjectOwnerId ="";
			if(STATE_PROJECT_ALL.equals(projectState)){
				project_State = project_State;
			} else if(STATE_PROJECT_ACTIVE.equals(projectState)){
				stbObjWhere.append("(");
				stbObjWhere.append("current=="+STATE_PROJECT_ASSIGN + "||");
				stbObjWhere.append("current=="+STATE_PROJECT_ACTIVE + "||");
				stbObjWhere.append("current=="+STATE_PROJECT_REVIEW);
				stbObjWhere.append(")");
				project_State = stbObjWhere.toString();
			} else if(STATE_PROJECT_COMPLETE.equals(projectState)){
				stbObjWhere.append("current=="+projectState);
				project_State = stbObjWhere.toString();
			}
			StringList objectSelects = new StringList();
			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
			objectSelects.addElement(SELECT_ID);
			objectSelects.addElement("to["+RELATIONSHIP_EFFORTS+"].from.id");
			objectSelects.addElement(SELECT_EFFORT_PARENT_TASK_ID);
			objectSelects.addElement(SELECT_ATTRIBUTE_END_DATE);
			objectSelects.addElement(SELECT_ATTRIBUTE_TOTAL_EFFORT);
			Person person = new Person(strObjectId);
			//person.getInfo(context, DomainConstants.SELECT_NAME);
			StringBuffer sbWhere = new StringBuffer();
			if(ProgramCentralUtil.isNotNullString(projectOwner))
			{
				strProjectOwnerId = PersonUtil.getPersonObjectID(context, projectOwner);	
			}			
			Person personPrjOwner = new Person(strProjectOwnerId);
			String strRequestStartDate = "";
			String strRequestEndDate = "";			
			Date dtStartDate = null;
			Date dtFinishDate = null;
			MapList mlEfforts = new MapList();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

			CalendarType calendarType= getCalendarType(context, YearType);
			Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(selYear.trim())); 
			dtStartDate = interval.getStartDate();
			dtFinishDate = interval.getEndDate();
			strRequestStartDate = sdf.format(dtStartDate);
			strRequestEndDate = sdf.format(dtFinishDate);

			sbWhere.append("(");
			sbWhere.append(SELECT_ATTRIBUTE_ENDING_DATE + ">=\"" +strRequestStartDate+"\" && "+SELECT_ATTRIBUTE_ENDING_DATE + "<=\"" +strRequestEndDate+"\"");
			sbWhere.append(")");				
			sbWhere.append(" && ");
			sbWhere.append("(owner == '" +  person.getInfo(context, DomainConstants.SELECT_NAME)+"')");
			sbWhere.append(" && ");
			sbWhere.append("(");
			sbWhere.append("current == "+ProgramCentralConstants.STATE_EFFORT_APPROVED);
			sbWhere.append(")");

			ml = DomainObject.findObjects(context,TYPE_EFFORT,QUERY_WILDCARD,"-",
					QUERY_WILDCARD,QUERY_WILDCARD,sbWhere.toString(),false,objectSelects);    
			for(int i=0;i<ml.size();i++){
				Map mpEffort = (Map)ml.get(i);
				String EffortId = (String)mpEffort.get(DomainConstants.SELECT_ID);
				String strWeekEndDate = (String)mpEffort.get(SELECT_ATTRIBUTE_END_DATE);
				String strTotal = (String)mpEffort.get(SELECT_ATTRIBUTE_TOTAL_EFFORT);			
				Map calculatedMonthWeekTotal = calculatedMonthWeekTotal(context, strWeekEndDate, strTotal, 
						strTotal,selYear, YearType);		
				StringBuffer sbDateAndWeek = new StringBuffer();			
				sbDateAndWeek = getWeekData(context, EffortId,selYear,YearType,person.getInfo(context, DomainConstants.SELECT_NAME));
				calculatedMonthWeekTotal.put("DateAndEffort", sbDateAndWeek);				
				String strTaskId = (String)mpEffort.get(SELECT_EFFORT_PARENT_TASK_ID);
				DomainObject objEffort = DomainObject.newInstance(context,EffortId);
				MapList projects = objEffort.getRelatedObjects(context,
						RELATIONSHIP_EFFORTS, TYPE_PROJECT_SPACE,objectSelects, null,
						true, false, (short)1,project_State,"",0,null,null,null);
				getProjectsOwnedByUser(context, strProjectOwnerId, projects);
				for(int j=0;j<projects.size();j++) {
					Map mpEffort1 = new HashMap();
					Map project = (Map)projects.get(j);
					String projectId = (String)project.get(DomainConstants.SELECT_ID);
					mpEffort1.put(DomainConstants.SELECT_ID, projectId);
					mpEffort1.put("level", "2");

					boolean isSameProject = false;
					int index=0;
					if(projectList.size()>0){
						for(int x=0;x<projectList.size();x++){							
							Map mpProject = (Map)projectList.get(x);
							String strProjectId = (String)mpProject.get(DomainConstants.SELECT_ID);
							if(strProjectId.equalsIgnoreCase(projectId)){
								index = x;
								isSameProject = true;
								break;
							}
						}
					}
					if(isSameProject) {												
						Map mpProject = (Map)projectList.get(index);
						projectList.remove(index);
						StringBuffer sbMonth = new StringBuffer();
						StringBuffer sbValue = new StringBuffer();
						StringBuffer sbWeek = new StringBuffer();
						sbMonth = (StringBuffer)mpProject.get("Effort Month");
						sbWeek = (StringBuffer)mpProject.get("Effort Week");
						sbValue = (StringBuffer)mpProject.get("Effort Values");
						Double dbTotal = (Double)mpProject.get("Total Efforts");
						Double dbTotal1 = (Double)mpProject.get("Total Efforts1");						
						StringBuffer sbDateAndWeek1 = new StringBuffer();
						sbDateAndWeek1 = (StringBuffer)mpProject.get("DateAndEffort");						
						Map newMapProject = formKeysForDynamicColumn(sbMonth, sbWeek, sbValue, dbTotal, calculatedMonthWeekTotal, mpProject,sbDateAndWeek1,dbTotal1);						
						projectList.add(index, newMapProject);						
						Task task = new Task(projectId);
						StringList task_busSelects =  new StringList(6);
						task_busSelects.add(task.SELECT_ID);
						task_busSelects.add(task.SELECT_NAME);
						task_busSelects.add(SELECT_TYPE);	
						StringList relSelects = new StringList(1);
						MapList subTask = task.getTasks(context, 0, task_busSelects, relSelects,false);
						for(int i1=0;i1<subTask.size();i1++){
							Map mpTask = (Map)subTask.get(i1);
							String strMpTaskId = (String)mpTask.get(DomainConstants.SELECT_ID);
							int indexOfTask = isTaskAdded(context, strMpTaskId, projectList);
							if(strMpTaskId.equalsIgnoreCase(strTaskId)){
								if(indexOfTask == -1){
									Map mapTask = new HashMap();
									mapTask.put("level", "3");
									mapTask.put("hasChildren", "false");
									mapTask.put(DomainConstants.SELECT_ID, strMpTaskId);											    												
									Map map = formKeysForDynamicColumn(null, null, null, null, calculatedMonthWeekTotal, mapTask,null,null);				    				
									projectList.add(index+1,map);									
									break;
								}
								else{																											
									Map mapTask4 = (Map)projectList.get(indexOfTask);
									projectList.remove(indexOfTask);
									StringBuffer sbMonthTask = new StringBuffer();
									StringBuffer sbWeekTask = new StringBuffer();
									StringBuffer sbValueTask = new StringBuffer();
									sbMonthTask = (StringBuffer)mapTask4.get("Effort Month");
									sbWeekTask = (StringBuffer)mapTask4.get("Effort Week");
									sbValueTask = (StringBuffer)mapTask4.get("Effort Values");
									Double dbTotalTask = (Double)mapTask4.get("Total Efforts");	
									Double dbTotalTask1 = (Double)mapTask4.get("Total Efforts1");										
									StringBuffer sbDateAndWeek2 = new StringBuffer();
									sbDateAndWeek2 = (StringBuffer)mapTask4.get("DateAndEffort");																		
									Map NewMapTask = formKeysForDynamicColumn(sbMonthTask, sbWeekTask, sbValueTask, dbTotalTask, calculatedMonthWeekTotal, mapTask4,sbDateAndWeek2,dbTotalTask1);																	
									projectList.add(indexOfTask, NewMapTask);																	
									break;
								}
							}
						}
					}
					else 
					{	    							    	
						Map map = formKeysForDynamicColumn(null, null, null, null, calculatedMonthWeekTotal, mpEffort1,null,null);
						projectList.add(map);	    					    								    	
						Task task = new Task(projectId);
						StringList task_busSelects =  new StringList(6);
						Map calculatedMonthWeekTotal1 = calculatedMonthWeekTotal(context, strWeekEndDate, strTotal, 
								strTotal,selYear, YearType);
						calculatedMonthWeekTotal1.put("DateAndEffort", sbDateAndWeek);
						task_busSelects.add(task.SELECT_ID);
						task_busSelects.add(task.SELECT_NAME);
						task_busSelects.add(SELECT_TYPE);	
						StringList relSelects = new StringList(1);
						MapList subTask = task.getTasks(context, 0, task_busSelects, relSelects,false);
						for(int i1=0;i1<subTask.size();i1++){
							Map mpTask = (Map)subTask.get(i1);
							String strMpTaskId = (String)mpTask.get(DomainConstants.SELECT_ID);
							int indexOfTask = isTaskAdded(context, strMpTaskId, projectList);
							if(strMpTaskId.equalsIgnoreCase(strTaskId)){
								if(indexOfTask == -1){
									Map mapTask1 = new HashMap();
									mapTask1.put("level", "3");
									mapTask1.put("hasChildren", "false");
									mapTask1.put(DomainConstants.SELECT_ID, strMpTaskId);			    								    								    				
									Map map1 = formKeysForDynamicColumn(null, null, null, null, calculatedMonthWeekTotal1, mapTask1,null,null);				    				
									projectList.add(map1);				    								    				
									break;
								}
								else{			    								    				
									Map mapTask = (Map)projectList.get(indexOfTask);
									projectList.remove(indexOfTask);
									StringBuffer sbMonthTask = new StringBuffer();
									StringBuffer sbWeekTask = new StringBuffer();
									StringBuffer sbValueTask = new StringBuffer();
									sbMonthTask = (StringBuffer)mapTask.get("Effort Month");
									sbWeekTask = (StringBuffer)mapTask.get("Effort Week");
									sbValueTask = (StringBuffer)mapTask.get("Effort Values");
									Double dbTotalTask = (Double)mapTask.get("Total Efforts");
									Double dbTotalTask1 = (Double)mapTask.get("Total Efforts1");									
									StringBuffer sbDateAndWeek2 = new StringBuffer();
									sbDateAndWeek2 = (StringBuffer)mapTask.get("DateAndEffort");																		
									Map newMapTask = formKeysForDynamicColumn(sbMonthTask, sbWeekTask, sbValueTask, dbTotalTask, calculatedMonthWeekTotal1, mapTask,sbDateAndWeek2,dbTotalTask1);												    				
									projectList.add(indexOfTask, newMapTask);				    				
									break;
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		} 
		HashMap hmTemp = new HashMap();
		hmTemp.put("expandMultiLevelsJPO","true");  
		projectList.add(hmTemp);
		return projectList;
	}

	/**
	 * This method is used to check task is added in project or not.
	 * @param context
	 * @param strTaskId
	 * @param projectList
	 * @return int
	 */
	private int isTaskAdded(Context context,String strTaskId, MapList projectList){
		int isTaskAdded = -1;
		for(int x=0;x<projectList.size();x++){
			Map mpProject = (Map)projectList.get(x);
			String strProjectId = (String)mpProject.get(DomainConstants.SELECT_ID);
			if(strProjectId.equalsIgnoreCase(strTaskId)){
				isTaskAdded = x;
				break;
			}
		}
		return isTaskAdded;
	}

	/**
	 * Used to calculate total efforts.
	 * @param context
	 * @param strWeekEndDate
	 * @param strTotal
	 * @param strGrandTotal
	 * @param strSelYear
	 * @param strYearType
	 * @return Map
	 * @throws Exception
	 */
	private Map calculatedMonthWeekTotal(Context context,String strWeekEndDate,String strTotal,String strGrandTotal,String strSelYear,String strYearType) throws Exception{
		int indexOfFiscal = strYearType.indexOf("Fiscal");
		CalendarType calendarType= getCalendarType(context, strYearType);
		Map mpcalculatedMonthWeekTotal = new HashMap();
		Double dEffort = 0.0;
		StringBuffer sbStrMonth = new StringBuffer();
		StringBuffer sbStrWeek = new StringBuffer();
		StringBuffer sbStrEfforts = new StringBuffer();
		boolean isEffortSelYear = isEfforsInTheSelectedYear(context, strWeekEndDate, strSelYear, strYearType);
		if(isEffortSelYear==true)
		{
			Date dDate = new Date(strWeekEndDate); 
			Calendar cal = Calendar.getInstance();
			cal.setTime(dDate);

			Map mpYearMonthWeek = getYearMonthWeek(context, dDate, calendarType);
			String strYear = (String)mpYearMonthWeek.get(SELECT_YEAR);
			String strMonth = (String)mpYearMonthWeek.get(SELECT_MONTH);
			String strWeekNo = (String)mpYearMonthWeek.get(SELECT_WEEK);

			sbStrMonth.append(strMonth);
			sbStrMonth.append("-");
			sbStrMonth.append(strYear);
			sbStrMonth.append(delimiter);
			mpcalculatedMonthWeekTotal.put("Month", sbStrMonth);


			sbStrWeek.append(strWeekNo);
			sbStrWeek.append("-");
			sbStrWeek.append(strYear);
			sbStrWeek.append(delimiter);
			mpcalculatedMonthWeekTotal.put("Week", sbStrWeek);

			String strEffort  = strTotal;
			sbStrEfforts.append(strEffort);
			sbStrEfforts.append(delimiter);
			mpcalculatedMonthWeekTotal.put("Total", sbStrEfforts);

			dEffort =Task.parseToDouble(strGrandTotal);
			mpcalculatedMonthWeekTotal.put("Grand Total", dEffort);
		}else{
			mpcalculatedMonthWeekTotal.put("Month", sbStrMonth);
			mpcalculatedMonthWeekTotal.put("Week", sbStrWeek);
			mpcalculatedMonthWeekTotal.put("Total", sbStrEfforts);
			mpcalculatedMonthWeekTotal.put("Grand Total", dEffort);
		}
		return mpcalculatedMonthWeekTotal;
	}

	/**
	 * @param sbMonth
	 * @param sbWeek
	 * @param sbValue
	 * @param dbTotal
	 * @param calculatedMonthWeekTotal
	 * @param mpProject
	 * @return Map
	 */
	private Map formKeysForDynamicColumn(StringBuffer sbMonth,StringBuffer sbWeek,StringBuffer sbValue,Double dbTotal,Map calculatedMonthWeekTotal,Map mpProject,StringBuffer dateAndWeek,Double dbTotal1){
		StringBuffer sbDateAndWeek = new StringBuffer();
		if(null != dateAndWeek && !dateAndWeek.equals("")){
			sbDateAndWeek.append(dateAndWeek);
		}
		if (sbMonth != null
				&& !sbMonth.equals("")
				&& !sbMonth.equals("null"))
		{
			sbMonth.append((StringBuffer)calculatedMonthWeekTotal.get("Month"));
		}else{
			mpProject.put("Effort Month", (StringBuffer)calculatedMonthWeekTotal.get("Month"));
		}
		if (sbWeek != null
				&& !sbWeek.equals("")
				&& !sbWeek.equals("null"))
		{
			sbWeek.append((StringBuffer)calculatedMonthWeekTotal.get("Week"));
		}else{
			mpProject.put("Effort Week", (StringBuffer)calculatedMonthWeekTotal.get("Week"));
		}
		if (sbValue != null
				&& !sbValue.equals("")
				&& !sbValue.equals("null"))
		{
			sbValue.append((StringBuffer)calculatedMonthWeekTotal.get("Total"));
		}else{
			mpProject.put("Effort Values", (StringBuffer)calculatedMonthWeekTotal.get("Total"));
		}
		if (dbTotal != null
				&& !dbTotal.equals("")
				&& !dbTotal.equals("null"))
		{
			dbTotal += (Double)calculatedMonthWeekTotal.get("Grand Total");
			mpProject.put("Total Efforts", dbTotal);
		}else{
			mpProject.put("Total Efforts", (Double)calculatedMonthWeekTotal.get("Grand Total"));
		}	
		if (sbDateAndWeek != null
				&& !sbDateAndWeek.equals("")
				&& !sbDateAndWeek.equals("null"))
		{
			mpProject.put("DateAndEffort", sbDateAndWeek.append((StringBuffer)calculatedMonthWeekTotal.get("DateAndEffort")));
		}else{
			mpProject.put("DateAndEffort", (StringBuffer)calculatedMonthWeekTotal.get("DateAndEffort"));
		}	
		if (dbTotal1 != null
				&& !dbTotal1.equals("")
				&& !dbTotal1.equals("null"))
		{
			dbTotal1 += getTotalForReport((StringBuffer)calculatedMonthWeekTotal.get("DateAndEffort"));
			mpProject.put("Total Efforts1", dbTotal1);
		}else{
			mpProject.put("Total Efforts1", getTotalForReport((StringBuffer)calculatedMonthWeekTotal.get("DateAndEffort")));
		}	
		return mpProject;
	}

	/**
	 * This method used to get remaining effort for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing value of attribute remaining effort as
	 *         string.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 * 
	 */			
	public Vector getRemainingEffort(Context context, String[] args) throws MatrixException {
		Vector vRemainingEffort = new Vector();	
		Vector vecEffortId = new Vector();
		int effortVecSize = 0;	
		String originator = null;
		Map map = null;
		//MODIFIED::PA4:25-July-2011:IR-121339V6R2012x
		String[] strEffortIDs = null;
		String current = null;
		String strPlannedEffort = null;
		String strTotalEffort = null;
		double remainingEffort = 0;
		double totalEffort = 0;
		double plannedEffort = 0;
		DomainObject domObjEffort = null;

		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");				
			emxEffortManagementBase_mxJPO  emxEffortMgmt = new emxEffortManagementBase_mxJPO(context,args);			
			for(int i=0;i<objectList.size();i++) {
				Map objectMap = (Map)objectList.get(i);
				String objectId = (String)objectMap.get(DomainConstants.SELECT_ID);	
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				DomainObject domObj = null;
				try {
					domObj = DomainObject.newInstance(context,objectId);
					if(domObj.isKindOf(context, TYPE_EFFORT )) {
						StringList objList = new StringList();
						objList.add(DomainConstants.SELECT_CURRENT);
						objList.add(SELECT_TASK_ID);
						objList.add(SELECT_ORIGINATED);
						objList.add(SELECT_ATTRIBUTE_TOTAL_EFFORT);
						map= domObj.getInfo(context,objList);
						originator = domObj.getAttributeValue(context, ATTRIBUTE_ORIGINATOR);
						String originatorId = PersonUtil.getPersonObjectID(context, originator);
						String strState = (String)map.get(SELECT_CURRENT);
						String taskId = (String)map.get(SELECT_TASK_ID);	
						DomainObject taskDomObj = null;
						boolean hasPhase = false;
						taskDomObj = DomainObject.newInstance(context,taskId);
						hasPhase = taskDomObj.isKindOf(context, TYPE_PHASE);
						if(hasPhase) {	                
							vRemainingEffort.add("");
						} else {	            
							String rem_Effort = "0";
							HashMap per_PersonEffort = getPersonEffortMapping(context,taskId,originatorId);	
							if(per_PersonEffort.size() > 0) {
								Iterator keyItr = per_PersonEffort.keySet().iterator();
								while (keyItr.hasNext()) {
									String name = (String) keyItr.next();
									vecEffortId = (Vector) per_PersonEffort.get(name);
								}		            		         
							}        		        
							effortVecSize = vecEffortId.size();
							plannedEffort = emxEffortMgmt.getPlannedEffort(context,objectId); 
							if(strState.equals(ProgramCentralConstants.STATE_EFFORT_EXISTS) && effortVecSize == 1) {	                		
								rem_Effort = Double.toString(plannedEffort);
							} 
							else{
								totalEffort = 0;
								strEffortIDs = (String[]) vecEffortId.toArray(new String[0]);
								for(String strEffortId:strEffortIDs){
									domObjEffort = newInstance(context,strEffortId);
									map = domObjEffort.getInfo(context, objList);
									current = (String)map.get(SELECT_CURRENT);
									if(current.equalsIgnoreCase(ProgramCentralConstants.STATE_EFFORT_APPROVED)){
										strTotalEffort = (String) map.get(SELECT_ATTRIBUTE_TOTAL_EFFORT);
										if(!ProgramCentralUtil.isNullString(strTotalEffort))
											totalEffort = Task.parseToDouble(strTotalEffort) + totalEffort;
									}
								}
								remainingEffort = plannedEffort - totalEffort;
								rem_Effort = Double.toString(remainingEffort);
							}
							//END::PA4:25-July-2011:IR-121339V6R2012x
							vRemainingEffort.add(rem_Effort);
						}
					} else {
						vRemainingEffort.add("");
					}
				}finally{
					ContextUtil.popContext(context);	
				}
			}
		}
		catch(Exception ex) {
			throw new MatrixException(ex.getMessage());
		}
		return vRemainingEffort;
	}

	/**
	 * Takes the task and person name and gets the list of effortid's for the
	 * person name
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the task id
	 * @param String
	 *            containing the login user id
	 * 
	 * @return HashMap - containing effort id's of login user.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 * 
	 */			
	public HashMap getPersonEffortMapping(Context context,String taskId,String personId)
			throws Exception
			{
		String[] args = new String[0];
		StringList busSelects = new StringList(1);
		busSelects.add(SELECT_ID);
		StringList relSelects = new StringList(1);
		MapList maplist = new MapList();
		HashMap returnMap = new HashMap();
		HashMap mapItem = new HashMap();
		String personName="";
		Vector vecItem = null;
		String originator = null;
		Vector effortIds = new Vector();
		Task task = null;
		emxEffortManagementBase_mxJPO emxEffortMgmt = new emxEffortManagementBase_mxJPO(context,args);
		ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
		try {
			effortIds = emxEffortMgmt.getEffortIds(context,taskId,RELATIONSHIP_HAS_EFFORTS,TYPE_EFFORT,false,true,(short)1);
			task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			task.setId(taskId);
			maplist = (MapList)task.getAssignees(context, busSelects, relSelects, null);
		} finally {
			ContextUtil.popContext(context);		  
		}    		    	    

		for (int i = 0; i < maplist.size(); i++) {
			vecItem = new Vector();
			String id =(String)((Map)maplist.get(i)).get("id");
			if(personId!=null && personId.equals(id)) {
				DomainObject bus  = DomainObject.newInstance(context, id);
				bus.open(context);
				personName = bus.getName();
				bus.close(context);
				String fid = "";
				for (int z = 0; z < effortIds.size(); z++)
				{
					fid =(String)effortIds.get(z);
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						DomainObject bo  = DomainObject.newInstance(context, fid);
						bo.open(context);              
						originator = bo.getAttributeValue(context,DomainConstants.ATTRIBUTE_ORIGINATOR);  
						bo.close(context); 
					} finally {
						ContextUtil.popContext(context);		  
					}

					if(originator!=null && personName!=null && personName.equals(originator))
					{           	  
						vecItem.addElement(fid);
					}              
				}
				returnMap.put(personName,vecItem);
			}	         
		}
		return returnMap;
			}
	/**
	 * This method used to set remaining effort for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            String EffortId
	 * @return Void
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x            
	 * @author vf2  
	 */	    
	private void setRemainingEffort(Context context, String effortId,String taskId,String personId)throws MatrixException {		
		double plannedEfort = 0.0;
		double remainingEffort = 0.0;
		HashMap per_PersonEffort = new HashMap();			
		Vector vecEffortId = new Vector();
		DomainObject domEffort = null;			
		double submittedEffort = 0.0;	
		MapList newEffortMapList = null;
		MapList effortIdMapList = new MapList();
		int effortMapListSize = 0;
		String strEffortId = null;       	 	
		int currentIndex = 0; 
		int effortVecSize = 0;  
		double dCurrRemainingeffort = 0.0;
		try {									     	    
			emxEffortManagementBase_mxJPO  emxEffortMgmt = new emxEffortManagementBase_mxJPO(context,new String[0]);		
			if(ProgramCentralUtil.isNotNullString(effortId)) {
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);					        
				try {
					DomainObject domObjEffort = newInstance(context,effortId);	
					StringList slList = new StringList();
					slList.add(SELECT_CURRENT);
					slList.add(SELECT_ORIGINATED);
					slList.add(SELECT_ORIGINATOR);
					slList.add(ProgramCentralConstants.SELECT_ATTRIBUTE_REMAINING_EFFORT);
					slList.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TOTAL_EFFORT);

					Map effortMap = domObjEffort.getInfo(context, slList);
					String strState = (String)effortMap.get(SELECT_CURRENT);					
					String originator = (String)effortMap.get(SELECT_ORIGINATOR);
					String originatorId = PersonUtil.getPersonObjectID(context, originator);
					String sTotalEfffort = (String)effortMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TOTAL_EFFORT);
					String sRemainingEfffort = (String)effortMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_REMAINING_EFFORT);
					submittedEffort = ProgramCentralUtil.isNotNullString(sTotalEfffort)?  Task.parseToDouble(sTotalEfffort) : 0;
					dCurrRemainingeffort = ProgramCentralUtil.isNotNullString(sRemainingEfffort)?  Task.parseToDouble(sRemainingEfffort) : 0;

					per_PersonEffort = getPersonEffortMapping(context,taskId,originatorId);														

					if(per_PersonEffort.size() > 0) {
						Iterator keyItr = per_PersonEffort.keySet().iterator();
						while (keyItr.hasNext()) {
							String name = (String) keyItr.next();
							vecEffortId = (Vector) per_PersonEffort.get(name);
						}		            		         
					}

					String[] saEffortIds = new String[vecEffortId.size()];
					vecEffortId.copyInto(saEffortIds);
					StringList slSelect = new StringList();	        		
					slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_REMAINING_EFFORT);	        		
					double[] dblValues = new double[saEffortIds.length];
					BusinessObjectWithSelectList withSelectList = null;
					BusinessObjectWithSelect bows = null;
					withSelectList = BusinessObject.getSelectBusinessObjectData(context, saEffortIds, slSelect);
					int i=0;
					for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(withSelectList); itr.next();)
					{
						bows = itr.obj();
						String strValue = bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_REMAINING_EFFORT);
						double dblValue = Task.parseToDouble(strValue);
						dblValues[i] = dblValue;
						i++;
					}	        		
					effortVecSize = vecEffortId.size();
					if(effortVecSize > 1) {			        	
						Map currentMap = new HashMap();		        	 		        	 
						currentMap.put("wkDate",(String)effortMap.get(SELECT_ORIGINATED));
						currentMap.put("id", effortId);  
						effortIdMapList = getPersonEfforts(context,vecEffortId);
						effortMapListSize = effortIdMapList.size();
						currentIndex = effortIdMapList.indexOf(currentMap);
					}
					if(effortVecSize == 1) {								
						plannedEfort = emxEffortMgmt.getPlannedEffort(context,effortId);																
						remainingEffort = plannedEfort - submittedEffort;
					} else if(STATE_WEEKLY_TIMESHEET_EXISTS.equals(strState)) {	        	 
						if(currentIndex != -1) {		        		 	
							Arrays.sort(dblValues);
							dCurrRemainingeffort = dblValues[1];
							remainingEffort = dCurrRemainingeffort - submittedEffort;
						}		        			        				        			        
					} else if(STATE_WEEKLY_TIMESHEET_SUBMIT.equals(strState) || STATE_WEEKLY_TIMESHEET_REJECTED.equals(strState)) {		        	 
						newEffortMapList = new MapList();
						for(int m = currentIndex; m < effortMapListSize; m++) {
							newEffortMapList.add((Map)effortIdMapList.get(m));
						}
						updateRemainingEffort(context,newEffortMapList,submittedEffort,strState);        		 	        	
					}			        
					if(STATE_WEEKLY_TIMESHEET_EXISTS.equals(strState)) {																		
						domObjEffort.setAttributeValue(context, ATTRIBUTE_REMAINING_EFFORT, Double.toString(remainingEffort));
					}	
				} finally {
					ContextUtil.popContext(context);	
				}
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		} 
	}	    

	/**
	 * This method used to update remaining effort for each effort object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param MapList
	 *            String effortMapList	 
	 * @param String
	 *            String strState
	 * @param double
	 *            String submittedEffort
	 * @return void
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2012            
	 * @author vf2  
	 */	  	
	private void updateRemainingEffort(Context context,MapList effortMapList,double submittedEffort,String strState) throws Exception{
		DomainObject domEffort = null;
		double rem_Effort = 0.0;
		double newRemaingEffort = 0.0;	
		for(int k=0;k<effortMapList.size();k++) {
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);					        
			try 
			{
				domEffort = newInstance(context,(String)((Map)effortMapList.get(k)).get(SELECT_ID));
				rem_Effort = Task.parseToDouble(domEffort.getAttributeValue(context, ATTRIBUTE_REMAINING_EFFORT));		        
				if(STATE_WEEKLY_TIMESHEET_SUBMIT.equals(strState)) {
					newRemaingEffort = submittedEffort + rem_Effort;
				} else if(STATE_WEEKLY_TIMESHEET_REJECTED.equals(strState)) {
					newRemaingEffort = rem_Effort - submittedEffort;
				}	
				domEffort.setAttributeValue(context, ATTRIBUTE_REMAINING_EFFORT, Double.toString(newRemaingEffort));		        
			} finally {
				ContextUtil.popContext(context);	
			}			 			 			 
		}			
	}

	/**
	 * This method used to get person efforts in ascending order.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param Vector
	 *            String vecEffortId	 
	 * @return MapList - containing the sorted effot map. 
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2012            
	 * @author vf2  
	 */		
	private MapList getPersonEfforts(Context context,Vector vecEffortId) throws Exception {
		MapList effortMapList = new MapList();  
		Map map = null;
		String str_weekending = null;
		for (int i = 0; i < vecEffortId.size(); i++) {
			String strEffortId =(String)vecEffortId.get(i);
			if(ProgramCentralUtil.isNotNullString(strEffortId)) {
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				try {
					DomainObject domEffortObj = newInstance(context, strEffortId);
					domEffortObj.open(context);
					str_weekending = domEffortObj.getInfo(context,SELECT_ORIGINATED);
					domEffortObj.close(context);
				} finally {			
					ContextUtil.popContext(context);			
				}            	
				if(ProgramCentralUtil.isNotNullString(str_weekending)) {   
					map = new HashMap();                	
					map.put("wkDate",str_weekending);
					map.put("id", strEffortId);    
					effortMapList.add(map);
				}                
			}
		}
		if(effortMapList.size() > 1) {
			effortMapList.sort("wkDate", "ascending", "date");
		}
		return effortMapList;
	}

	/**
	 * This method used to calculate percentage completed for each task.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            String containing the taskId
	 * @return Void
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	private void getPercentageComplete(Context context,String taskId,String strEffortId) throws MatrixException {
		try {
			String[] args = new String[0]; 
			emxEffortManagementBase_mxJPO emxEffortJPO = new emxEffortManagementBase_mxJPO(context,args);
			DomainObject bus  = DomainObject.newInstance(context, taskId);
			bus.open(context);
			String  strPercentage = bus.getAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE);
			double perComplete = Task.parseToDouble(strPercentage);
			if(perComplete < 100) {
				String strType = bus.getTypeName();
				if(strType != null){						
					double totalEffort = emxEffortJPO.getTotalTaskEffort(context,taskId,ProgramCentralConstants.STATE_EFFORT_APPROVED);
					double remain_effort = emxEffortJPO.getTotalTaskRemainingEffort(context, taskId);

					double total_effort = totalEffort;
					double total_pending = emxEffortJPO.getTotalTaskEffort(context,taskId,ProgramCentralConstants.STATE_EFFORT_SUBMIT);
					double final_remaining = emxEffortJPO.getTaskPlannedEffort(context,taskId);
					if(remain_effort<=0 && total_effort==0 && total_pending==0)
					{
						remain_effort= final_remaining;
					}
					final_remaining=final_remaining-(total_pending+total_effort);
					if(final_remaining < 0 ) {
						final_remaining=0;
					}                     
					double submit_effort = emxEffortJPO.getTotalTaskEffort(context,taskId,ProgramCentralConstants.STATE_EFFORT_SUBMIT);      
					double f_results = 0;
					if ((totalEffort + remain_effort) != 0)
					{
						f_results = totalEffort/(totalEffort + (final_remaining + submit_effort));
					}           
					double task_rem = f_results*100;
					if(task_rem>100)
						task_rem=100;
					String srem_task = ""+emxEffortJPO.roundWBSPercentComplete(task_rem);

					if("100.0".equalsIgnoreCase(srem_task)){

						StringList busSelects = new StringList(2);
						StringList relSelects = new StringList();
						busSelects.add(SELECT_ID);
						busSelects.add(SELECT_CURRENT);
						busSelects.add("from[" + RELATIONSHIP_SUBTASK + "]");
						busSelects.add(DomainConstants.SELECT_POLICY);

						MapList subtaskList = bus.getRelatedObjects(context, 
								RELATIONSHIP_SUBTASK, 
								ProgramCentralConstants.TYPE_TASK_MANAGEMENT, 
								busSelects, 
								relSelects, 
								false,  
								true, 
								(short)0, 
								"", 
								"", 
								0, 
								null, 
								null, 
								null);

						DomainObject task = DomainObject.newInstance(context);
						Iterator itr = subtaskList.iterator();
						while (itr.hasNext()){
							Map map          = (Map) itr.next();
							String taskID    = (String) map.get(SELECT_ID);
							String taskstate = (String) map.get(SELECT_CURRENT);
							String hasSubTask = (String) map.get("from[" + RELATIONSHIP_SUBTASK + "]");
							String taskPolicy = (String) map.get(DomainConstants.SELECT_POLICY);

							if("FALSE".equalsIgnoreCase(hasSubTask)){
								task.setId(taskID);
								if(ProgramCentralConstants.POLICY_PROJECT_TASK.equalsIgnoreCase(taskPolicy) && "Create".equalsIgnoreCase(taskstate)){
									task.promote(context);
									task.promote(context);
									task.promote(context);
								}else if(ProgramCentralConstants.POLICY_PROJECT_TASK.equalsIgnoreCase(taskPolicy) && "Assign".equalsIgnoreCase(taskstate)){
									task.promote(context);
									task.promote(context);
								}else if(ProgramCentralConstants.POLICY_PROJECT_TASK.equalsIgnoreCase(taskPolicy) && "Active".equalsIgnoreCase(taskstate)){
									task.promote(context);
								}else if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(taskPolicy) && "Create".equalsIgnoreCase(taskstate)){
									task.promote(context);
								}
							}	
						}
					}
					bus.setAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE, srem_task);
				}
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
	}	

	/**
	 * This method used to update the remaining effort attribute for effort.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            String containing the effortId
	 * @return Void
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public void updateRemainingEffort(Context context, String[] args) throws MatrixException {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String effortId = (String)paramMap.get("objectId");
			String newValue = (String)paramMap.get("New Value");
			DomainObject effortDomainObject = DomainObject.newInstance(context, effortId); 
			effortDomainObject.setAttributeValue(context, ATTRIBUTE_REMAINING_EFFORT, newValue);
		} catch(Exception e){
			throw new MatrixException(e.getMessage());
		}
	}	

	/**
	 * This method used to show  for effort.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector showProgressBar(Context context,String[] args) throws MatrixException {
		Vector vprogressBar = new Vector();
		String taskId = null;
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");	
			HashMap paramList = (HashMap) paramMap.get("paramList");
			String exportFormat = (String)paramList.get("exportFormat");
			emxEffortManagementBase_mxJPO emxEffortJPO = new emxEffortManagementBase_mxJPO(context,args);	        				        
			for(int i=0;i<objectList.size();i++) {
				Map objectMap = (Map)objectList.get(i);
				String objectId = (String)objectMap.get(DomainConstants.SELECT_ID);		
				String isTotalRow = (String)objectMap.get("isTotalRow");
				if(isTotalRow!=null && "true".equals(isTotalRow)) {
					vprogressBar.add(ProgramCentralConstants.EMPTY_STRING);
				} else {
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						DomainObject domObj = DomainObject.newInstance(context,objectId);	
						if(domObj.isKindOf(context, TYPE_EFFORT )){
							double perComplete = 0.0;
							boolean hasPhase = false;

							taskId = domObj.getInfo(context,SELECT_TASK_ID);
							DomainObject taskdomObject = DomainObject.newInstance(context, taskId);
							hasPhase = taskdomObject.isKindOf(context, TYPE_PHASE);
							perComplete = Task.parseToDouble(taskdomObject.getAttributeValue(context, ATTRIBUTE_PERCENT_COMPLETE));

							if(hasPhase)
							{
								vprogressBar.add(ProgramCentralConstants.EMPTY_STRING);
							}

							else if ("CSV".equalsIgnoreCase(exportFormat) || "HTML".equalsIgnoreCase(exportFormat)){
								String roundPerCompletedValue = ""+Math.round(perComplete);
								StringBuffer percentComplete = new StringBuffer();
								percentComplete.append(XSSUtil.encodeForHTML(context,roundPerCompletedValue)+"%");
								vprogressBar.add(percentComplete.toString());
							}
							else{
								double approvedEffort,plannedEffort = 0.0;
								String roundPerCompleted = ""+Math.round(perComplete);
								//approvedEffort = emxEffortJPO.getTotalTaskEffort(context,taskId,ProgramCentralConstants.STATE_EFFORT_APPROVED);
								//plannedEffort = emxEffortJPO.getTaskPlannedEffort(context,taskId);									
								StringBuffer sbProgressBar = new StringBuffer();
								sbProgressBar.append("<table>");
								sbProgressBar.append("<tr>");
								sbProgressBar.append("<td>");
								sbProgressBar.append(XSSUtil.encodeForHTML(context,roundPerCompleted)+"%");
								sbProgressBar.append("</td>");	
								sbProgressBar.append("<td>");
								sbProgressBar.append("<div style=\"width:102px;height:10px;background:#F0F0F0;border:1px solid black;\">");
								sbProgressBar.append("<div style=\"width:"+XSSUtil.encodeForHTML(context,roundPerCompleted)+"px;background: blue;height:8px; border:1px solid white;\">");
								sbProgressBar.append("</div></div>");
								sbProgressBar.append("</td>");
								//sbProgressBar.append("<td>");
								//sbProgressBar.append(""+Math.round(plannedEffort-approvedEffort)+"/"+""+Math.round(plannedEffort));
								//sbProgressBar.append("</td>");
								sbProgressBar.append("</tr>");
								sbProgressBar.append("</table>");
								vprogressBar.add(sbProgressBar.toString());
							}
						}else {
							vprogressBar.add(ProgramCentralConstants.EMPTY_STRING);
						}								
					} finally {
						ContextUtil.popContext(context);
					}
				}
			}	
			return vprogressBar;
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		} 	
	}

	/**
	 * This method used to get name of objects in all views of timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing name of corresponding objects.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getName(Context context,String[] args) throws MatrixException {
		Vector vName = new Vector();		
		String strTaskName=ProgramCentralConstants.EMPTY_STRING;
		String strName =ProgramCentralConstants.EMPTY_STRING;
		String strType =ProgramCentralConstants.EMPTY_STRING;
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");	
			Map mParamList = (Map)paramMap.get("paramList");
			String sExportFormat = (String)mParamList.get("exportFormat");
			Locale locale = (Locale) mParamList.get("localeObj");
			boolean isCurrentlyExported = false;
			if(null != sExportFormat && "CSV".equalsIgnoreCase(sExportFormat.trim())){
				isCurrentlyExported = true;
			}
			for(int i=0;i<objectList.size();i++) {
				Map objectMap = (Map)objectList.get(i);
				String objectId = (String)objectMap.get(SELECT_ID);	
				String isTotalRow = (String)objectMap.get("isTotalRow");	
				if(isTotalRow!=null && "true".equals(isTotalRow)) {				
					String total = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.WeeklyTimesheet.Total", context.getSession().getLanguage());
					String totalCsv =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.WeeklyTimesheet.TotalCSV", context.getSession().getLanguage());
					if(null != sExportFormat && "CSV".equalsIgnoreCase(sExportFormat.trim())){
						vName.add(totalCsv);
					} else {
						String image = "<img border=\"0\" src=\"../common/images/iconSmallTask.gif\" title=\"\"></img>";
						vName.add(image+total);
					}
				} else {
					if(objectId != null && objectId.trim().length()>0){
						Map map = new HashMap();
						StringList selectObjList = new StringList(2);
						selectObjList.add(SELECT_TASK_ID);
						selectObjList.add(SELECT_NAME);
						selectObjList.add(SELECT_TYPE);
						selectObjList.add(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE);
						DomainObject domObj = null;
						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						try {
							domObj = DomainObject.newInstance(context,objectId);	
							map = domObj.getInfo(context,selectObjList);	

							strName = (String)map.get(SELECT_NAME);						
							strType = (String)map.get(SELECT_TYPE);		

							//Format timesheet name. If Locale is English then don't format
							if(TYPE_WEEKLY_TIMESHEET.equals(strType) && !"en".equals(locale.getLanguage())){
								DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT, locale);
								String wkEndDate = (String)map.get(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE);
								Date endDate = eMatrixDateFormat.getJavaDate(wkEndDate);
								Calendar endCal = Calendar.getInstance(locale);				
								endCal.setTime(endDate);
								Calendar startCal = endCal;
								startCal.add(Calendar.DATE, -6);
								Date startDate = startCal.getTime();
								String wkStartDate = formatter.format(startDate);
								wkEndDate = formatter.format(endDate);
								strName = wkStartDate + ProgramCentralConstants.SPACE + ProgramCentralConstants.HYPHEN + ProgramCentralConstants.SPACE + wkEndDate;

							}

							if(domObj.isKindOf(context, TYPE_EFFORT)){
								DomainObject taskdomObject = null;

								taskdomObject = DomainObject.newInstance(context, (String)map.get(SELECT_TASK_ID));
								strTaskName = taskdomObject.getInfo(context,SELECT_NAME);
								if(isCurrentlyExported){ // [MODIFIED::PRG:RG6:Dec 29, 2010:IR-083876V6R2012:R211]
									vName.addElement(strTaskName);
								} else {
									StringBuffer sbName = new StringBuffer();
									sbName.append("<img border=\"0\" src=\"../common/images/iconSmallTask.gif\" title=\"\"></img>");
									sbName.append("<a href=\"JavaScript:showModalDialog('../common/emxTree.jsp?objectId=");
									sbName.append(XSSUtil.encodeForURL(context,objectId));
									sbName.append("','700','600','false','popup')\">");                    
									sbName.append(XSSUtil.encodeForXML(context,strTaskName));
									sbName.append("</a>");		
									vName.addElement(sbName.toString());							
								}
							} else {
								if(isCurrentlyExported){ 
									if(!domObj.isKindOf(context, TYPE_PERSON)) {
										vName.addElement(strName);
									} else {
										vName.addElement(XSSUtil.encodeForXML(context,(PersonUtil.getFullName(context,strName))));
									}
								} else {
									StringBuffer sbNameLink = new StringBuffer();	
									if(domObj.isKindOf(context, TYPE_WEEKLY_TIMESHEET)) {
										sbNameLink.append("<img border=\"0\" src=\"../common/images/iconColHeadTime.gif\" title=\"\"></img>");
									} else if(domObj.isKindOf(context, TYPE_PROJECT_SPACE)) {
										sbNameLink.append("<img border=\"0\" src=\"../common/images/iconSmallProject.gif\" title=\"\"></img>");
									} else if(domObj.isKindOf(context, TYPE_PERSON)) {
										sbNameLink.append("<img border=\"0\" src=\"../common/images/iconSmallPerson.gif\" title=\"\"></img>");
									} else if(domObj.isKindOf(context, TYPE_TASK)) {
										sbNameLink.append("<img border=\"0\" src=\"../common/images/iconSmallTask.gif\" title=\"\"></img>");
									}
									sbNameLink.append("<a href=\"JavaScript:showModalDialog('../common/emxTree.jsp?objectId=");
									sbNameLink.append(XSSUtil.encodeForURL(context,objectId));
									sbNameLink.append("','700','600','false','popup')\">");                    
									if(!domObj.isKindOf(context, TYPE_PERSON)) {
										//Added for special character.
										sbNameLink.append(XSSUtil.encodeForXML(context,strName));
										//sbNameLink.append(strName);
									} else{
										sbNameLink.append(XSSUtil.encodeForXML(context,(PersonUtil.getFullName(context,strName))));
									}
									sbNameLink.append("</a>");		
									vName.addElement(sbNameLink.toString());					
								}	
							}
						} finally {
							ContextUtil.popContext(context);		
						}		
					} else {
						vName.addElement(ProgramCentralConstants.EMPTY_STRING);
					}	
				}
			}		
			return vName;
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
	}

	/**
	 * This method used to get type of objects in all views of timesheet.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing type of corresponding objects.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */	
	public Vector getType(Context context,String[] args) throws MatrixException {
		Vector vType = new Vector();
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");		        				        
			String languageStr = context.getSession().getLanguage();
			for(int i=0;i<objectList.size();i++) {
				Map objectMap = (Map)objectList.get(i);
				String objectId = (String)objectMap.get(DomainConstants.SELECT_ID);	
				String isTotalRow = (String)objectMap.get("isTotalRow");	
				if(isTotalRow!=null && "true".equals(isTotalRow)) {
					vType.add("");
				} else {
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						DomainObject domObj = DomainObject.newInstance(context,objectId);
						//modified by ms9 start
						if(domObj.isKindOf(context, ProgramCentralConstants.TYPE_EFFORT))
						{
							vType.add(i18nNow.getTypeI18NString((String)objectMap.get("to[hasEfforts].from.type"), languageStr)); 
						}
						else
						{

							vType.add(i18nNow.getTypeI18NString(domObj.getInfo(context,SELECT_TYPE), languageStr));
						}
					} finally {			
						ContextUtil.popContext(context);			
					}	
				}
				//modified by ms9 end
			}
			return vType;
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		} 	
	}	

	/**
	 * This method used to create html input field for week start date.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * @return String - String containing html input field for week start date.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public String getWeekStartDate(Context context, String[] args) throws MatrixException {
		String strWeekStart = EMPTY_STRING;
		try { 		
			strWeekStart = "<input type=\"text\" readonly=\"true\" size=\"20\" name=\"WeekStart\" value=\"\" id=\"WeekStart\">";
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}   	      	
		return strWeekStart;
	}	

	/**
	 * This method used to create html input field for week end date.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * @return String - String containing html input field for week end date.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */			
	public String getWeekEndDate(Context context, String[] args) throws MatrixException {
		String strWeekEnd = EMPTY_STRING;
		try {
			strWeekEnd = "<input type=\"text\" readonly=\"true\" size=\"20\" name=\"WeekEnd\" value=\"\" id=\"WeekEnd\">";
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}   	      	
		return strWeekEnd;
	}		

	public String getActualWeekEndDate(Context context, String[] args) throws MatrixException {
		String strActualWeekEnd = EMPTY_STRING;
		try {
			strActualWeekEnd = "<input type=\"hidden\" readonly=\"true\" name=\"ActualWeekEndDate\" value=\"\" id=\"ActualWeekEndDate\">";
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}   	      	
		return strActualWeekEnd;
	}		

	/**
	 * This method used to create html input field for week number.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * @return String - String containing html input field for week number.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */			
	public String getWeekNumber(Context context, String[] args) throws MatrixException {
		String strWeekNo = null;
		try {
			strWeekNo = "<input type=\"text\" readonly=\"true\" size=\"20\" name=\"WeekNo\" value=\"\" id=\"WeekNo\">";
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}   	      	
		return strWeekNo;
	}	

	/**
	 * This method used to calculate the total effort of each day.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param Map
	 * @param MapList
	 *            - containing effort object in form of map.
	 * @return Map - containing total effort for each day.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	private Map getTotalEffort(Context context,Map projectMap,MapList efforts) throws Exception {
		StringList strAttributeList = new StringList();
		strAttributeList.add(ATTRIBUTE_SUNDAY);
		strAttributeList.add(ATTRIBUTE_MONDAY);
		strAttributeList.add(ATTRIBUTE_TUESDAY);
		strAttributeList.add(ATTRIBUTE_WEDNESDAY);
		strAttributeList.add(ATTRIBUTE_THURSDAY);
		strAttributeList.add(ATTRIBUTE_FRIDAY);
		strAttributeList.add(ATTRIBUTE_SATURDAY);	
		strAttributeList.add(ATTRIBUTE_TOTAL_EFFORT);	     
		for(int idx=0;idx<efforts.size();idx++){
			Map Effort = new HashMap();
			Effort = (Map)efforts.get(idx);	  
			String effortId = (String)Effort.get(DomainConstants.SELECT_ID);
			DomainObject effortDomainObject = DomainObject.newInstance(context, effortId);	
			AttributeList effortAttributeList = null;
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			try {
				effortAttributeList =  effortDomainObject.getAttributeValues(context, strAttributeList);
			} finally {
				ContextUtil.popContext(context);	
			}
			if(projectMap.get("isEffort")== null || "".equals(projectMap.get("isEffort"))) {
				for(int i=0; i<effortAttributeList.size(); i++) {
					if(i == 7) {
						projectMap.put("attribute["+strAttributeList.get(i)+"]",Task.parseToDouble(((Attribute)effortAttributeList.get(i)).getValue()));
					} else {
						projectMap.put("attribute["+strAttributeList.get(i)+"]",Task.parseToDouble(((Attribute)effortAttributeList.get(i)).getValue()));
					}
				}
				projectMap.put("isEffort","true");
			} else {
				for(int i=0; i<effortAttributeList.size(); i++) {
					if(i == 7) {
						projectMap.put("attribute["+strAttributeList.get(i)+"]",Task.parseToDouble(((Attribute)effortAttributeList.get(i)).getValue())
								+ (Double)projectMap.get("attribute["+strAttributeList.get(i)+"]"));
					} else {
						projectMap.put("attribute["+strAttributeList.get(i)+"]",Task.parseToDouble(((Attribute)effortAttributeList.get(i)).getValue()) 
								+ (Double)projectMap.get("attribute["+strAttributeList.get(i)+"]"));
					}
				}							
			}																		
		}
		return projectMap;
	}		

	/**
	 * This method used to get date in which object is in submit state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing date of submit state.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getSubmitedOn(Context context,String[] args)throws MatrixException {
		Vector vSubmittedDate = new Vector();
		Map map = null;
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();	        				        
			while (objectListIterator.hasNext()) {
				Map mapObject = (Map) objectListIterator.next();
				String id = (String)mapObject.get(DomainConstants.SELECT_ID);
				String strStateDate = "";
				String isTotalRow = (String)mapObject.get("isTotalRow");	
				if(isTotalRow!=null && "true".equals(isTotalRow)) {
					vSubmittedDate.add("");
				} else {
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						DomainObject dobj = DomainObject.newInstance(context, id);
						StringList objSelectList = new StringList(2);
						objSelectList.add(SELECT_TYPE);
						objSelectList.add(SELECT_CURRENT);
						map = dobj.getInfo(context, objSelectList);
						String objType = (String)map.get(SELECT_TYPE);
						if(objType.equalsIgnoreCase(TYPE_EFFORT)||objType.equalsIgnoreCase(TYPE_WEEKLY_TIMESHEET)) {
							String currentState = (String)map.get(SELECT_CURRENT);					
							if(ProgramCentralConstants.STATE_EFFORT_SUBMIT.equalsIgnoreCase(currentState)) {
								ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
								try {
									strStateDate = dobj.getInfo(context,"state["+ProgramCentralConstants.STATE_EFFORT_SUBMIT+"].start");
								} finally {			
									ContextUtil.popContext(context);			
								}															
								Date statesDate = eMatrixDateFormat.getJavaDate(strStateDate);
								java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
								strStateDate = sdf.format(statesDate);							
							}		
						} 
						vSubmittedDate.add(strStateDate);
					} finally {			
						ContextUtil.popContext(context);			
					}	
				}	
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return vSubmittedDate;
	}
	private String[] increaseWeekNumber(String[] strWeekNo){
		String[] strNewString = new String[strWeekNo.length];
		for(int i=0;i<strWeekNo.length;i++){
			String strWeek = strWeekNo[i];
			int indexOfhyphen = strWeek.indexOf("-");
			if(indexOfhyphen!=-1){
				String subWeek = strWeek.substring(0,indexOfhyphen);
				String strRemaining = strWeek.substring(indexOfhyphen, strWeek.length());
				int intWeek = Integer.parseInt(subWeek);
				intWeek = intWeek-1;
				String strNewWeek = String.valueOf(intWeek)+strRemaining;
				strNewString[i] = strNewWeek;
			}
		}
		return strNewString;
	}

	/**
	 * This method used to get WBS task.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing name of corresponding objects.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getWBS(Context context,String[] args) throws MatrixException {
		Vector vWBS = new Vector();
		String taskId = null;
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");		        				       
			for(int i=0;i<objectList.size();i++) {
				Map objectMap = (Map)objectList.get(i);
				String objectId = (String)objectMap.get(DomainConstants.SELECT_ID);	
				String isTotalRow = (String)objectMap.get("isTotalRow");	
				if(isTotalRow!=null && "true".equals(isTotalRow)) {				
					vWBS.add("");
				} else {
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					try {
						DomainObject domObj = DomainObject.newInstance(context,objectId);	
						if(domObj.isKindOf(context, TYPE_EFFORT))
						{
							taskId = domObj.getInfo(context,SELECT_TASK_ID);
							DomainObject domObjTask = DomainObject.newInstance(context,taskId);	
							StringList wbsList = domObjTask.getInfoList(context,"to["+RELATIONSHIP_SUBTASK+"].attribute["+ATTRIBUTE_TASK_WBS+"].value");							
							for(int j=0;j<wbsList.size();j++){
								vWBS.add(wbsList.get(j));
							}						
						} 
						else {
							vWBS.add("");
						}	
					}finally {			
						ContextUtil.popContext(context);			
					}								
				} 
			}
			return vWBS;
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());		
		} 	
	}		

	/**
	 * This method used to get second last effort id from the list of efforts.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param HashMap
	 *            Map containing the effort ids
	 * @return String - containing the effort id
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	private String getSecondLastUserEfforts(Context context, HashMap hm) throws Exception
	{
		String secondlasteffort = null;
		if(hm != null) {

			Iterator keyItr = hm.keySet().iterator();
			while (keyItr.hasNext()){
				String latest = "" ;
				String name = (String) keyItr.next();
				Vector value = (Vector) hm.get(name);
				if(value!=null)
				{
					if(value.size()==1)
					{
						secondlasteffort = (String) value.get(0);
					}else{
						secondlasteffort = getSecondLastEffort(context,value);
					}	
				}
			}
		}
		return secondlasteffort;
	}	

	/**
	 * This method used to get second last effort id from the list of efforts.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param Vector
	 *             containing the effort ids
	 * @return String - containing the effort id 
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x            
	 * @author vf2  
	 */	    
	private String getSecondLastEffort(Context context, Vector vSelect) throws Exception
	{
		String attribute_WeekEndingDate=ProgramCentralConstants.ATTRIBUTE_WEEK_ENDING_DATE;
		if(vSelect!= null && vSelect.size()==1)
		{
			return ((String)vSelect.get(0));
		}
		String return_id = "";
		ArrayList list = new ArrayList();
		if(vSelect!=null && vSelect.size()>1)
		{
			for (int i = 0; i < vSelect.size(); i++)
			{
				String id =(String)vSelect.get(i);
				if(id != null && id.trim().length() > 0)
				{
					DomainObject bus = DomainObject.newInstance(context, id);
					bus.open(context);
					String str_weekending = bus.getAttributeValue(context,attribute_WeekEndingDate);
					if(str_weekending!=null && str_weekending.trim().length()>0) {
						Date weekendDate = new Date(str_weekending);
						list.add(weekendDate);
					}
				}  
			}
			ArrayList prelist = new ArrayList();
			prelist.addAll(list);
			Collections.sort(list);
			if(list.size()>1) {
				Date secondlast = (Date)list.get(list.size()-2);
				int idx = prelist.indexOf(secondlast);
				return_id = (String)vSelect.get(idx);
			}else {
				return_id = (String)vSelect.get(0);
			}
		}
		return return_id;
	}

	/**
	 * This method used to get timesheet id from effort.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the effort id
	 * @return String - containing the timesheet id
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */	      
	public static String getTimesheetFromEffort(Context context, String strEffortId) throws Exception
	{
		String strTimesheetId = "";
		try{
			MapList mapTimesheet = new MapList();
			DomainObject objEffort = DomainObject.newInstance(context, strEffortId);
			StringBuffer stbRelPattern = new StringBuffer(50);
			stbRelPattern.append(RELATIONSHIP_EFFORTS);
			StringBuffer stbTypeName = new StringBuffer(50);
			stbTypeName.append(TYPE_WEEKLY_TIMESHEET);
			StringList objectSelects = new StringList();
			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
			objectSelects.addElement(SELECT_ID);
			StringList relSelects = new StringList(1);
			mapTimesheet = objEffort.getRelatedObjects(context,
					stbRelPattern.toString(), stbTypeName.toString(),objectSelects, relSelects,
					true, false, (short)1,null,"",0,null,null,null);
			if(mapTimesheet.size()==1){
				strTimesheetId = (String)((Map)mapTimesheet.get(0)).get(SELECT_ID);
			}
		}catch(Exception e){
			throw new MatrixException(e.getMessage());
		}
		return strTimesheetId;
	}

	/**
	 * This method used to get project id from effort.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the effort id
	 * @return String - containing the timesheet id
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author wpk
	 */	      
	public static String getProjectFromEffort(Context context, String strEffortId) throws Exception
	{
		String strTimesheetId = "";
		try{
			MapList mapTimesheet = new MapList();
			DomainObject objEffort = DomainObject.newInstance(context, strEffortId);
			StringBuffer stbRelPattern = new StringBuffer(50);
			stbRelPattern.append(RELATIONSHIP_EFFORTS);
			StringBuffer stbTypeName = new StringBuffer(50);
			stbTypeName.append(TYPE_PROJECT_SPACE);
			StringList objectSelects = new StringList();
			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
			objectSelects.addElement(SELECT_ID);
			StringList relSelects = new StringList(1);
			//To get Project/Task from effort where User is not member to respective project
			//Added:PRG:I16:R213:24-Nov-2011:IR-120341V6R2013 Start
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			try{
				mapTimesheet = objEffort.getRelatedObjects(context,
						stbRelPattern.toString(), stbTypeName.toString(),objectSelects, relSelects,
						true, false, (short)1,null,"",0,null,null,null);
			}finally{
				ContextUtil.popContext(context);
			}
			//Added:PRG:I16:R213:24-Nov-2011:IR-120341V6R2013 End
			if(mapTimesheet.size()==1){
				strTimesheetId = (String)((Map)mapTimesheet.get(0)).get(SELECT_ID);
			}
		}catch(Exception e){
			throw new MatrixException(e.getMessage());
		}
		return strTimesheetId;
	}

	/**
	 * This method used to get object id's of business units and departments.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the orgnization id
	 * @return StringList - list containing the id's.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public StringList getDeptAndBusinessUnitIds(Context context,String orgID) throws MatrixException {
		StringList sObjectlist = new StringList();
		try {
			StringList objectSelects = new StringList();
			objectSelects.add(DomainConstants.SELECT_ID);
			DomainObject domOrg = DomainObject.newInstance(context, orgID);
			StringBuffer stbRelPattern = new StringBuffer();
			stbRelPattern.append(RELATIONSHIP_DIVISION);
			stbRelPattern.append(",");
			stbRelPattern.append(RELATIONSHIP_COMPANY_DEPARTMENT);
			StringBuffer stbTypeName = new StringBuffer();
			stbTypeName.append(TYPE_BUSINESS_UNIT);
			stbTypeName.append(",");
			stbTypeName.append(TYPE_DEPARTMENT);			
			MapList mapListBusiUnit = domOrg.getRelatedObjects(context,
					stbRelPattern.toString(),stbTypeName.toString(),objectSelects, null,
					true, true, (short)0,null,"",0,null,null,null);          
			for(int i=0;i<mapListBusiUnit.size();i++){
				Map busiUnitMap = (Map)mapListBusiUnit.get(i);
				if(!sObjectlist.contains(busiUnitMap.get(DomainConstants.SELECT_ID))) {
					sObjectlist.add(busiUnitMap.get(DomainConstants.SELECT_ID));
				}
			}			
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return sObjectlist;
	}

	/**
	 * This method used to check whether relationship exist between two objects.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the timesheet id
	 * @param String
	 *            containing the business unit or department id.
	 * @return boolean - true or false.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */	
	private boolean isRelationshipExist(Context context,String timesheetId,String strId) throws MatrixException {
		boolean isRelExist = false;
		try{						
			StringList objectSelects = new StringList();
			objectSelects.add(DomainConstants.SELECT_ID);
			StringBuffer stbTypeName = new StringBuffer();
			stbTypeName.append(TYPE_PROJECT_SPACE);
			stbTypeName.append(",");
			stbTypeName.append(TYPE_ORGANIZATION);
			StringBuffer stbObjWhere = new StringBuffer();
			stbObjWhere.append("id=="+strId);
			DomainObject timesheetdObj = DomainObject.newInstance(context, timesheetId);
			MapList mpList = timesheetdObj.getRelatedObjects(context,
					RELATIONSHIP_APPROVER_CONTEXT, stbTypeName.toString(),objectSelects, null,
					true, true, (short)0,stbObjWhere.toString(),"",0,null,null,null);	
			if(mpList.size()== 0){
				isRelExist = true; 
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return isRelExist;
	}	

	/**
	 * This method used to get project state values from properties file to
	 * timesheet report.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * 
	 * @return HashMap - containing the project state values.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public HashMap getProjectStateFilter(Context context, String[] args) throws Exception
	{	
		HashMap projectStateMap = new HashMap();
		StringList fieldRangeValues = new StringList();
		fieldRangeValues.add(STATE_PROJECT_ALL);
		fieldRangeValues.add(STATE_PROJECT_ACTIVE);
		fieldRangeValues.add(STATE_PROJECT_COMPLETE);
		StringList fieldDisplayRangeValues = new StringList();
		for( int i=1;i<=3;i++) {
			String strProjectState = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.WeeklyTimesheetReport.States"+i+".Options", context.getSession().getLanguage());
			fieldDisplayRangeValues.addElement(strProjectState);
		}
		projectStateMap.put("field_choices", fieldRangeValues);
		projectStateMap.put("field_display_choices", fieldDisplayRangeValues);
		return projectStateMap;
	}

	/**
	 * This method used to get values for reporting year filter from properties
	 * file.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * 
	 * @return HashMap - containing the reporting year values.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public HashMap getReportingYearFilter(Context context, String[] args) throws Exception
	{
		HashMap statusMap = new HashMap();
		StringList fieldRangeValues = new StringList();
		StringList fieldDisplayRangeValues = new StringList();
		String strRangeValue = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ReportingYear.TotalRanges"); 
		int rangeValue = new Integer(strRangeValue);
		Date currDate = new Date();
		int currYear = currDate.getYear()+1900;
		int subyear = rangeValue/2;
		for( int i=subyear;i>=1;i--)
		{
			int year = currYear-i;
			fieldRangeValues.add(Integer.toString(year));
			fieldDisplayRangeValues.add(Integer.toString(year));
		}
		fieldRangeValues.add(Integer.toString(currYear));
		fieldDisplayRangeValues.add(Integer.toString(currYear));
		for( int i=1;i<=subyear;i++)
		{
			int year = currYear+i;
			fieldRangeValues.add(Integer.toString(year));
			fieldDisplayRangeValues.add(Integer.toString(year));
		}		
		statusMap.put("field_choices", fieldRangeValues);
		statusMap.put("field_display_choices", fieldDisplayRangeValues);
		return statusMap;
	}

	/**
	 * This method used to get list of project owned by user.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the project owner id
	 * @param MapList
	 *            containing the list of projects           
	 * @return void
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */			
	private void getProjectsOwnedByUser(Context context, String projectOwner,
			MapList projects) throws MatrixException {
		for(int j=0;j<projects.size();j++) {
			Map project = (Map)projects.get(j);
			String projectId = (String)project.get(DomainConstants.SELECT_ID);
			if(projectOwner != null && projectOwner.trim().length()>0) {
				StringList ownerList = getProjectOwnerList(context, projectId);
				for(int l=0;l<ownerList.size();l++) {
					String projectOwnerId = (String)ownerList.get(l);
					if(!projectOwner.equals(projectOwnerId)){
						projects.remove(project);
					}
				}					
			}
		}
	}

	/**
	 * This method used to get list of project owner id's.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the project id
	 * @return StringList - contains the project owner id's.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	private StringList getProjectOwnerList(Context context, String projectId) throws MatrixException {
		StringList projList = new StringList();
		if(projectId != null && projectId.trim().length() > 0) {
			DomainObject dobjProject = DomainObject.newInstance(context, projectId);
			StringBuffer strRelWhere = new StringBuffer(30);
			strRelWhere.append("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ACCESS+"] ~~ \""+ROLE_PROJECT_OWNER+"\"");
			MapList mpList = dobjProject.getRelatedObjects(context,
					RELATIONSHIP_MEMBER,
					TYPE_PERSON,
					new StringList(DomainConstants.SELECT_ID),
					null,
					false,
					true,
					(short) 1,
					null, 
					strRelWhere.toString(),0);
			for(int i=0;i<mpList.size();i++) {
				Map ProjectMap = (Map)mpList.get(i);
				projList.add(ProjectMap.get(DomainConstants.SELECT_ID));
			}			
		}
		return projList;
	}

	/**
	 * This method used to change the approver for timesheet according to
	 * project preference as project lead or resource manager.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            containing the project id
	 * @return void
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */			
	public void changeApprover(Context context, String[] args) throws MatrixException {
		try { 
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String projId = (String)programMap.get("objectId");
			String strUpdatedPreference = (String)programMap.get("strUpdatedPreference");
			String[] timesheetIds = null;
			StringList newList = new StringList();
			MapList mpEffortsOfProject = new MapList();
			boolean toDisconnect = false;
			if(projId != null && projId.trim().length()>0) {
				DomainObject dObjProject = DomainObject.newInstance(context, projId);
				String strCompanyId = dObjProject.getInfo(context, "to["+RELATIONSHIP_COMPANY_PROJECT+"].from.id");
				StringList listBUOrDept = getDeptAndBusinessUnitIds(context,strCompanyId);
				listBUOrDept.add(strCompanyId);
				StringList list = new StringList();
				String strProjectLead = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Common.ProjectLead", context.getSession().getLanguage());
				String strResourceMgr = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Common.ResourceManager", context.getSession().getLanguage());
				String strPreference = null;
				String strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");
				if(SEELCT_APPROVER_RESOURCE_MANAGER.equals(strApprover)){
					strPreference = strResourceMgr;
				} else if(SEELCT_APPROVER_PROJECT_LEAD.equals(strApprover)){
					strPreference = strProjectLead;
				}				
				if(strPreference.equals(strProjectLead)) {
					list = listBUOrDept;
					newList.add(projId);
					WeeklyTimesheet wtProject = new WeeklyTimesheet(projId);
					mpEffortsOfProject = wtProject.getEfforts(context, null, null, null);
					toDisconnect = true;
				} else if(strPreference.equals(strResourceMgr)) {
					list.add(projId);
					newList = listBUOrDept;
				}
				for(int i=0;i<list.size();i++) {				
					MapList mpTimesheetList = getTimesheetsToApprove(context,list.get(i).toString(),null);
					timesheetIds = new String[mpTimesheetList.size()];
					for(int idx=0; idx<mpTimesheetList.size(); idx++) {
						Map timesheetMap = (Map)mpTimesheetList.get(idx);
						String timesheetId = (String)timesheetMap.get(DomainConstants.SELECT_ID);			
						if(toDisconnect){
							WeeklyTimesheet wtTimesheet = new WeeklyTimesheet(timesheetId);
							MapList mpEffortsOfTimesheet = wtTimesheet.getEfforts(context, null, null, null);
							boolean allTimesheetTaskAdded = true;
							int commonTask = 0;
							int uncommonTask = 0;
							for(int x=0;x<mpEffortsOfTimesheet.size();x++){
								Map mapEffortOfTimesheet = (Map)mpEffortsOfTimesheet.get(x);
								String strEffortId = (String)mapEffortOfTimesheet.get(DomainConstants.SELECT_ID);
								if(isTaskAdded(context, strEffortId, mpEffortsOfProject) == -1){										
									uncommonTask = uncommonTask+1;
								}else{
									commonTask = commonTask+1;
								}
							}
							if(commonTask == mpEffortsOfTimesheet.size()){
								String relId = (String)timesheetMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);								
								DomainRelationship.disconnect(context, relId);
								timesheetIds[idx] = timesheetId;
							}else if(commonTask>0 && uncommonTask>0){
								timesheetIds[idx] = timesheetId;
							}
						}else{
							String relId = (String)timesheetMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);								
							DomainRelationship.disconnect(context, relId);
							timesheetIds[idx] = timesheetId;
						}
					}						
				}
				for(int m=0;m<newList.size();m++) {
					String projOrBusinessUintId = newList.get(m).toString();				
					for(int idx=0; idx<timesheetIds.length; idx++) {
						String timesheetId = timesheetIds[idx];
						if(timesheetId!= null && timesheetId.trim().length()>0){
							DomainObject dObjTimeSheet = DomainObject.newInstance(context, timesheetId);
							if(strPreference.equals(strProjectLead)) {						
								DomainRelationship.connect(context, dObjTimeSheet, 
										RELATIONSHIP_APPROVER_CONTEXT, dObjProject);						
							} else if(strPreference.equals(strResourceMgr)) {
								DomainObject dObjBusiUnit = DomainObject.newInstance(context, projOrBusinessUintId);
								if(isRelationshipExist(context,timesheetId,projOrBusinessUintId)) {
									DomainRelationship.connect(context, dObjTimeSheet, 
											RELATIONSHIP_APPROVER_CONTEXT, dObjBusiUnit);
								}						
							}
						}
					}
				}				
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}	
	}

	/**
	 * This method used to get current year value for reporting year field in
	 * reports.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The command line arguments
	 * @return Vector - containing the current year value as string.
	 * @throws Exception
	 *             if operation fails
	 * @since V6R2011x.
	 * 
	 */	
	public String getCurrentYear(Context context,String[] args) throws MatrixException {
		String currYear = "";
		try {
			Date currDate = new Date();
			currYear = Integer.toString(currDate.getYear()+1900);
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return currYear;
	}

	/**
	 * This method used to update the comments entered by approver for each
	 * effort.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            The effort id & comments entered by approver
	 * @return Void
	 * @throws MatrixException
	 *             if operation fails
	 * @since V6R2011x.
	 * 
	 */		
	public void updateRejectionEffort(Context context, String[] args) throws MatrixException {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String effortId = (String)paramMap.get("objectId");
			String newValue = (String)paramMap.get("New Value");
			DomainObject effortDomainObject = DomainObject.newInstance(context, effortId); 
			effortDomainObject.setAttributeValue(context, ATTRIBUTE_EFFORT_COMMENTS, newValue);
		} catch(Exception e){
			throw new MatrixException(e.getMessage());
		}
	}

	/**
	 * This method used to validate effort value entered for each day.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            contains the updated value. 
	 * @return void
	 * @throws MatrixException
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 * 
	 */	
	private void validateEffort(Context context,String newValue) throws MatrixException {
		try {
			Pattern pattern = Pattern.compile("^\\d*\\.{0,1}\\d+$");	    
			Matcher matcher = pattern.matcher(newValue);
			boolean result = matcher.find();
			if(!result){	 	    	
				String strError = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimeSheetReports.PMCPlzEntValNo", context.getSession().getLanguage());
				throw new Exception(strError);
			}
			double updatedValue = Task.parseToDouble(newValue); 
			if(updatedValue > 24.0) {	    	
				String strErrorMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.WeeklyTimeSheet.Effort.validateEffortEntry", context.getSession().getLanguage());
				throw new Exception(strErrorMsg);	    	
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
	}		
	/**
	 * Returns the object ids of the task with zero total efforts
	 * @param context
	 * @param strTimesheetId
	 * @return StringList
	 * @throws Exception 
	 */
	public StringList getTaskWithZeroEffort(Context context,String[] args) throws Exception{
		StringList strTaskWithZeroEffort = new StringList();
		try{
			ContextUtil.startTransaction(context, true);
			String strTimesheetId = (String)JPO.unpackArgs(args);
			WeeklyTimesheet wt = new WeeklyTimesheet(strTimesheetId);
			MapList mpEfforts = wt.getEfforts(context, null, null, null);
			for(int i=0;i<mpEfforts.size();i++){
				Map effort = (Map)mpEfforts.get(i);
				String strEffortId = (String)effort.get(DomainConstants.SELECT_ID);
				DomainObject objEffort = DomainObject.newInstance(context,strEffortId);
				String strTotalEffort = objEffort.getAttributeValue(context,ATTRIBUTE_TOTAL_EFFORT);
				int intTotal = 0;
				double d = 0.0d;
				d = Task.parseToDouble(strTotalEffort);
				String strTaskId = (String)effort.get("to["+RELATIONSHIP_HAS_EFFORTS+"].from.id");
				DomainObject objTask = DomainObject.newInstance(context,strTaskId);
				StringList strObjectList = new StringList(2);
				strObjectList.add(SELECT_CURRENT);
				strObjectList.add(SELECT_TYPE);
				strObjectList.add(SELECT_NAME);
				Map taskMap =  objTask.getInfo(context, strObjectList);
				if(d==0.0){
					strTaskWithZeroEffort.add(taskMap.get(SELECT_NAME));
				}
			}
			if(mpEfforts.size()==strTaskWithZeroEffort.size()){
				strTaskWithZeroEffort.clear();
				strTaskWithZeroEffort.add(0, "AllTaskWithZeroEffort");
				ContextUtil.abortTransaction(context);
			}else{
				ContextUtil.commitTransaction(context);
			}

		}catch(Exception e){
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		return strTaskWithZeroEffort;
	}

	/**
	 * This method is used to connect timesheets & resource pool.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: 
	 *            args[0] - Contains a resource pool id.
	 *            args[1] - Contains a person id.
	 * @return integer value 0 or 1
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */ 	
	public int triggerconnectMemberResourcePoolOrProject(Context context,String[] args) throws Exception {
		int result = 1;
		String strApprover = null;
		try{
			String fromId  = args[0];
			String personId  = args[1];			
			if(ProgramCentralUtil.isNotNullString(fromId) && ProgramCentralUtil.isNotNullString(personId)) {					
				strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");
				DomainObject domObj = newInstance(context,fromId);	
				if((SEELCT_APPROVER_RESOURCE_MANAGER.equals(strApprover) && domObj.isKindOf(context, TYPE_ORGANIZATION)) ||
						(SEELCT_APPROVER_PROJECT_LEAD.equals(strApprover) && (domObj.isKindOf(context, TYPE_PROJECT_SPACE) && !domObj.isKindOf(context, ProgramCentralConstants.TYPE_EXPERIMENT)))) {
					connectTimesheetAndDeptOrBUnit(context,fromId,personId);	
				}	
				result = 0;
			}
			return result; 			
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}

	/**
	 * This method is used to disconnect timesheets & resource pool.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: 
	 *            args[0] - Contains a resource pool id.
	 *            args[1] - Contains a person id.
	 * @return integer value 0 or 1
	 * @throws Exception
	 *             if the operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */ 		
	public int triggerdisconnectMemberResourcePoolOrProject(Context context,String[] args) throws Exception {
		int result = 1;		
		String strApprover = null;
		try{
			String fromId  = args[0];
			String personId  = args[1];
			if(ProgramCentralUtil.isNotNullString(fromId) && ProgramCentralUtil.isNotNullString(personId)) {
				strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");
				DomainObject domObj = newInstance(context,fromId);				
				if((SEELCT_APPROVER_RESOURCE_MANAGER.equals(strApprover) && domObj.isKindOf(context, TYPE_ORGANIZATION)) ||
						(SEELCT_APPROVER_PROJECT_LEAD.equals(strApprover) && domObj.isKindOf(context, TYPE_PROJECT_SPACE))) {
					disconnectTimesheetAndDeptOrBUnit(context,fromId,personId);	
				}				
				result = 0;
			}
			return result;					
		} catch(Exception e) {			
			e.printStackTrace();
			throw new Exception(e.getMessage());			
		}
	}

	/**
	 * This method used to connect with Approver Context relationship between
	 * timesheet & Department or Business Unit when Member is added through Add
	 * Existing option
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param deptOrBUnitId
	 *            containing the department or business unit id
	 * @param personId
	 *            containing the person id
	 * @return void
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2012
	 * @author vf2
	 */		
	public void connectTimesheetAndDeptOrBUnit(Context context, String deptOrBUnitId,String personId) throws Exception {
		try {
			StringList stateList = new StringList();
			stateList.add(STATE_WEEKLY_TIMESHEET_SUBMIT);
			StringList slTimesheet = new StringList();
			StringList slconnectTimesheet = new StringList();
			WeeklyTimesheet timesheet = new WeeklyTimesheet();
			String[] strTimesheetArray = null;
			String strTimesheet = null;
			int timesheetCount = 0;
			int size = 0;
			if(ProgramCentralUtil.isNotNullString(deptOrBUnitId) && ProgramCentralUtil.isNotNullString(personId)) {
				MapList timeSheetMapList = timesheet.getWeeklyTimesheets(context,null,null,new Person(personId),stateList,null,null);
				for(int i=0;i<timeSheetMapList.size();i++) {			
					slTimesheet.add((String)((Map)timeSheetMapList.get(i)).get(SELECT_ID));
				}
				timesheetCount = slTimesheet.size();
				if(timesheetCount>0) {
					DomainObject domObjDeptOrBUnit = DomainObject.newInstance(context, deptOrBUnitId); 
					for(int k=0;k<timesheetCount;k++) { 
						size++;
						strTimesheet = slTimesheet.get(k).toString();
						if(isRelationshipExist(context,strTimesheet,deptOrBUnitId)) {                                                                                		  
							slconnectTimesheet.add(strTimesheet);
						}
						if(size == timesheetCount && !slconnectTimesheet.isEmpty()) {
							strTimesheetArray = slconnectTimesheet.toString().substring(1, slconnectTimesheet.toString().length()-1).split(",");
							DomainRelationship.connect(context, domObjDeptOrBUnit, RELATIONSHIP_APPROVER_CONTEXT, false, strTimesheetArray);
						}		          	  
					}
				}
			}
		} catch(Exception e) {
			throw e;
		}
	}	

	/**
	 * This method used to disconnect with Approver Context relationship between
	 * timesheet & Department or Business Unit when Member is added through
	 * remove option
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param deptOrBUnitId
	 *            containing the department or business unit id
	 * @param personId
	 *            containing the person id
	 * @return void
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2012
	 * @author vf2
	 */		
	public void disconnectTimesheetAndDeptOrBUnit(Context context, String deptOrBUnitId,String personId) throws Exception {
		String[] strTimesheetArray = null;	
		StringList sldisconnectTimesheet = new StringList();
		MapList timeSheetMapList = new MapList();
		DomainObject domDeptOrBUnitId = null;
		try {														    
			if(ProgramCentralUtil.isNotNullString(deptOrBUnitId) && ProgramCentralUtil.isNotNullString(personId)) {				
				domDeptOrBUnitId = newInstance(context,deptOrBUnitId);	
				StringList objectSelects = new StringList(4);			
				objectSelects.addElement(SELECT_ID);
				objectSelects.addElement(SELECT_TYPE);
				objectSelects.addElement(SELECT_NAME);
				objectSelects.addElement(SELECT_CURRENT);	
				StringList relSelects = new StringList(SELECT_RELATIONSHIP_ID);	
				StringBuffer stbTypeName = new StringBuffer(25);
				stbTypeName.append(TYPE_WEEKLY_TIMESHEET);			
				StringBuffer stbRelPattern = new StringBuffer(30);
				stbRelPattern.append(RELATIONSHIP_APPROVER_CONTEXT);			
				StringBuffer stbObjWhere = new StringBuffer(100);
				stbObjWhere.append("current=="+STATE_WEEKLY_TIMESHEET_SUBMIT+" && to["+RELATIONSHIP_WEEKLY_TIMESHEET+"].from.id=="+personId);																			
				timeSheetMapList = domDeptOrBUnitId.getRelatedObjects(context,
						stbRelPattern.toString(), stbTypeName.toString(),objectSelects, relSelects,
						true, false, (short)1,stbObjWhere.toString(),"",0);	

				for(int i=0;i<timeSheetMapList.size();i++){							
					sldisconnectTimesheet.add((String)((Map)timeSheetMapList.get(i)).get(SELECT_RELATIONSHIP_ID));
				}					
				if(!sldisconnectTimesheet.isEmpty()) {
					strTimesheetArray = sldisconnectTimesheet.toString().substring(1, sldisconnectTimesheet.toString().length()-1).split(",");
					DomainRelationship.disconnect(context, strTimesheetArray);					
				}	
			}
		} catch(Exception e) {
			throw e;
		}
	}

	/**
	 * This method used to get object id's of business units and departments.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *            containing the orgnization id
	 * @return StringList - list containing the id's.
	 * @throws MatrixException
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public StringList getDeptAndBusinessUnitIds(Context context,String orgID,Person person) throws MatrixException {
		StringList sObjectlist = new StringList();
		try {		
			StringList objectSelects = new StringList();
			objectSelects.add(SELECT_ID);
			objectSelects.add(SELECT_TYPE);
			objectSelects.add(SELECT_NAME);
			StringList relSelects = new StringList();
			StringBuffer stbTypeName = new StringBuffer();
			stbTypeName.append(TYPE_ORGANIZATION);
			StringBuffer stbRelPattern = new StringBuffer();
			stbRelPattern.append(RELATIONSHIP_COMPANY_DEPARTMENT);
			stbRelPattern.append(",");
			stbRelPattern.append(RELATIONSHIP_DIVISION);
			stbRelPattern.append(",");
			stbRelPattern.append(RELATIONSHIP_MEMBER);
			//Added:NZF:13-May-2011:IR-107159V6R2012x
			stbRelPattern.append(",");
			stbRelPattern.append(RELATIONSHIP_RESOURCE_MANAGER);
			//End:NZF:13-May-2011:IR-107159V6R2012x
			MapList mapListBusiUnit = person.getRelatedObjects(context,
					stbRelPattern.toString(), // relationships pattern 
					stbTypeName.toString(),   // type pattern
					objectSelects,            // Object select
					relSelects,				  // rel select
					true,                     // from traverse
					false,                    // to traverse
					(short)0,                 // recurse level
					null,                     // object where clause
					"",                       // rel where clause
					0);                       // total no. of objects to retrieve
			for(int i=0;i<mapListBusiUnit.size();i++){
				Map busiUnitMap = (Map)mapListBusiUnit.get(i);
				if(!sObjectlist.contains(busiUnitMap.get(DomainConstants.SELECT_ID))) {
					sObjectlist.add(busiUnitMap.get(DomainConstants.SELECT_ID));
				}
			}			
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return sObjectlist;
	}	

	/**
	 * This method is used to get project id's for a login user in add task
	 * search page.
	 * 
	 * @param MapList
	 *            Contains the wbs details for a specific project.
	 * @param StringList
	 *            Contains the assigned tasks for user.
	 * @param StringList
	 *            Contains the list of project id's.
	 * @param String
	 *            Containing the priject id.
	 * 
	 * @return StringList containing the project id's.
	 * @since Added by vf2 for release version V6R2011x.
	 */ 	
	private StringList getAssignedProjectToPerson(MapList taskMapList,StringList assignedTaskList,StringList slProjectList, String ProjectId) {
		boolean isTaskAssigned = false;
		int taskSize = taskMapList.size();
		if(taskSize > 0) {			 
			for(int j=0;j<taskSize;j++) {
				if(assignedTaskList.size() > 0) {
					if(assignedTaskList.contains((String)((Map)taskMapList.get(j)).get(SELECT_ID))) {
						isTaskAssigned = true;
						break;
					}
				} else {					
					if(STATE_PROJECT_ACTIVE.equals((String)((Map)taskMapList.get(j)).get(SELECT_CURRENT))) {
						isTaskAssigned = true;
						break;
					}									
				}
			}
			if(isTaskAssigned) {
				slProjectList.add(ProjectId);
				isTaskAssigned = false;
			}
		}
		return slProjectList;
	}	
	/**
	 * @param isWeekEndDate
	 * @param dWeekEndDate
	 * @return
	 * @throws Exception
	 */
	private Date getDurationToModifyWeekEndDate(Context context,boolean isFiscal,boolean isWeekEndDate , Date dWeekEndDate) throws MatrixException {
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		int CONST_VAL = 7;
		YearConfig yearConfig = new YearConfig(isFiscal);
		int startDayOfWeek = yearConfig.getStartDayOfWeek(context, 1900);  //property file
		int endDayofWeek = CONST_VAL;
		if(startDayOfWeek>1)
		{
			endDayofWeek = startDayOfWeek -1 ; 
		}
		long modifiedDurationValue = 0;
		Calendar weekEndDateCalendar = Calendar.getInstance();
		weekEndDateCalendar.setTime(dWeekEndDate);

		if(isWeekEndDate)
		{
			int lastDayOflastWeek = weekEndDateCalendar.get(Calendar.DAY_OF_WEEK);

			if(endDayofWeek > lastDayOflastWeek)
			{
				modifiedDurationValue = endDayofWeek - lastDayOflastWeek;
			}
			else
			{
				modifiedDurationValue = endDayofWeek - lastDayOflastWeek;
				modifiedDurationValue = CONST_VAL + modifiedDurationValue;
			}

			modifiedDurationValue = (modifiedDurationValue ) * MILLIS_IN_DAY;
		}

		Date modifiedWeekEndDate = new Date(dWeekEndDate.getTime() + modifiedDurationValue);

		return modifiedWeekEndDate;
	}

	private CalendarType getCalendarType(Context context,String sYearType)
	{
		CalendarType calendarType = null;

		if(SELECT_FISCAL.equalsIgnoreCase(sYearType))
		{
			calendarType = CalendarType.FISCAL;
		}
		else{
			calendarType = CalendarType.ENGLISH;
		}

		return calendarType;
	}

	/**
	 * Gets the year month and week for input date and calendar type.
	 * @param context the ENOVIA <code>Context</code> object
	 * @param Date Date
	 * @param CalendarType Calendar type
	 * @throws MatrixException if JPO operations fail.
	 */	
	public Map getYearMonthWeek(Context context,Date dDate, CalendarType calendarType)throws MatrixException
	{
		Map mpYearMonthWeek = new HashMap();

		try
		{
			WeeklyTimesheet wkt = new WeeklyTimesheet();
			wkt.getYearMonthWeek(context,dDate,calendarType);
		}
		catch(Exception e) 
		{
			throw new MatrixException(e.getMessage());
		}
		return mpYearMonthWeek;
	}

	/**
	 * 
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args contains Mapped information
	 * @return true if Action(Approve and Reject) can performed on Submitted TimeSheet 
	 *         false Action(Approve and Reject) not be performed on Rejected TimeSheet
	 * @throws Exception
	 */
	public boolean hasAccessOnApproveRejectCmd(Context context, String[] args)throws Exception
	{
		boolean isApproveReject = true;
		boolean isResourceManagerOrLead = false;
		String strFilterValue = "";		
		String strFilterValueMethod = "";
		String strApprover = "";
		try{
			HashMap hmParam = (HashMap)JPO.unpackArgs(args);		
			strFilterValue = (String)hmParam.get("selectedFilter");		
			strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");
			if(!("".equalsIgnoreCase(strFilterValue)) && ProgramCentralUtil.isNotNullString(strFilterValue)){
				StringList slValues = FrameworkUtil.split(strFilterValue, ":");		
				strFilterValueMethod = (String)slValues.get(1);
			}else{
				strFilterValue = (String)hmParam.get("expandProgramMenu");
				String strSelectPrg = "";
				if("PMCWeeklyTimeSheetApproverFilter".equalsIgnoreCase(strFilterValue) && ProgramCentralUtil.isNotNullString(strFilterValue)){
					strSelectPrg = (String)hmParam.get("selectedProgram");
					if(!("".equalsIgnoreCase(strSelectPrg)) && ProgramCentralUtil.isNotNullString(strSelectPrg)){
						StringList slValues = FrameworkUtil.split(strSelectPrg, ":");		
						strFilterValueMethod = (String)slValues.get(1);				
					}
				}
			}
			if("R".equalsIgnoreCase(strApprover) && ProgramCentralUtil.isNotNullString(strApprover)){
				String strRMRole = DomainConstants.ROLE_RESOURCE_MANAGER;		
				Vector vRoles = PersonUtil.getUserRoles(context);
				if(vRoles.contains(strRMRole))
					isResourceManagerOrLead = true;
			}else if("P".equalsIgnoreCase(strApprover) && ProgramCentralUtil.isNotNullString(strApprover)){
				String strLeadRole = DomainConstants.ROLE_PROJECT_LEAD;		
				Vector vRoles = PersonUtil.getUserRoles(context);
				if(vRoles.contains(strLeadRole))
					isResourceManagerOrLead = true;
			}
			if(ProgramCentralUtil.isNotNullString(strFilterValueMethod) && "getApprovedTimesheetForApprover".equalsIgnoreCase(strFilterValueMethod) && isResourceManagerOrLead){
				isApproveReject = false;
			}
			if(!("".equalsIgnoreCase(strFilterValueMethod)) && ProgramCentralUtil.isNotNullString(strFilterValueMethod)){
				if(("displayTimesheetTasksRejected".equalsIgnoreCase(strFilterValueMethod) || "displayTimesheetTasksApproved".equalsIgnoreCase(strFilterValueMethod)) && isResourceManagerOrLead){
					isApproveReject = false;
				}
			}
		}catch(Exception e){
			throw new MatrixException(e);
		}
		return isApproveReject;
	}

	/**
	 * This methods checks for the Approver, by reading Properties file Approver key.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args containing object related parameters 
	 * @return true if Approver setting in emxProgramCentral.properties is R and logged in person is Resource Manager
	 *              or if Approver setting in emxProgramCentral.properties is P and logged in person is External/Project Lead
	 *         otherwise false
	 * @throws MatrixException
	 */
	public boolean checkApprover(Context context, String[] args)throws MatrixException
	{
		boolean bIsRMApprover = false;
		String sProjectLead=ProgramCentralConstants.ROLE_PROJECT_LEAD;
		String sExternalProjectLead=ProgramCentralConstants.ROLE_EXTERNAL_PROJECT_LEAD;
		String sResourceManager=ProgramCentralConstants.ROLE_RESOURCE_MANAGER;
		String VPLMProjectLeader=ProgramCentralConstants.ROLE_VPLM_PROJECT_LEADER;
		String VPLMProjectOwner=ProgramCentralConstants.ROLE_VPLM_PROJECT_OWNER;

		try{
			Vector vRoles = PersonUtil.getUserRoles(context);
			String strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");

			//If Approver Selection is not set in propery file, return false
			if(ProgramCentralUtil.isNullString(strApprover))
				return false;

			// Check if user is VPM User and get all VPM roles.
			StringList ancestorList = new StringList();
			if(vRoles.contains(ProgramCentralConstants.ROLE_VPLM_EXCHANGE_USER)){
				String contextRole = context.getRole();
				if (contextRole != null && !"".equals(contextRole)){
					String role = "role.ancestor";
					String sCommandStatement = "print role $1 select $2 dump $3";
					String ancestors = MqlUtil.mqlCommand(context, sCommandStatement,contextRole,role,"|");
					ancestorList = FrameworkUtil.split(ancestors, "|");
				}

				if("P".equalsIgnoreCase(strApprover)&& ancestorList.contains(VPLMProjectLeader)){
					bIsRMApprover = true;
				}else if("R".equalsIgnoreCase(strApprover)&& ancestorList.contains(VPLMProjectOwner)){
					bIsRMApprover = true;
				}

			}else{
				if(("R".equalsIgnoreCase(strApprover) && (vRoles.contains(sResourceManager)))||
						("P".equalsIgnoreCase(strApprover) && (vRoles.contains(sProjectLead) ||  vRoles.contains(sExternalProjectLead)))){
					bIsRMApprover = true;
				}
			}

		}catch(Exception e){
			throw new MatrixException(e);
		}
		return bIsRMApprover;
	}

	/**
	 * To display WeeklyTimeSheet command of MyDesk menu. Logged in person atleast have ProjectUser role or
	 * Resource Manager role with Appprover key set to R.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args containing object related parameters 
	 * @return true if logged person is User or valid Approver
	 *         false otherwise
	 * @throws MatrixException
	 */
	public boolean hasAccessOnWeeklyTimeSheetCmd(Context context, String[] args) throws MatrixException
	{
		boolean hasAccess = true;
		String sProjectUser=ProgramCentralConstants.ROLE_PROJECT_USER;
		String sExternalProjectUser=ProgramCentralConstants.ROLE_EXTERNAL_PROJECT_USER;
		String sResourceManager=ProgramCentralConstants.ROLE_RESOURCE_MANAGER;
		try{
			Vector vRoles = PersonUtil.getUserRoles(context);
			String strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");

			if(ProgramCentralUtil.isNotNullString(strApprover) && ("P".equalsIgnoreCase(strApprover) && vRoles.contains(sResourceManager))
					&& !(vRoles.contains(sProjectUser) || vRoles.contains(sExternalProjectUser)))
			{
				hasAccess = false;
			}					
		}catch(Exception e){
			throw new MatrixException(e);
		}
		return hasAccess;
	}

	/**
	 * Refreshes the structure browser when click on save in edit mode.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the "columnMap"
	 * @returns Map
	 * @throws Exception if operation fails
	 */

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map postProcessRefresh (Context context, String[] args) throws Exception
	{
		Map inputMap = (HashMap)JPO.unpackArgs(args);

		Map returnMap = new HashMap(1);
		returnMap.put("Action","refresh");
		return returnMap;

	}

	public boolean hasAccess(Context context, String[] args) throws MatrixException
	{
		boolean hasAccess = false;
		try{
			Vector vRoles = PersonUtil.getUserRoles(context);
			String strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");
			String strResourceManager = ProgramCentralConstants.ROLE_RESOURCE_MANAGER;
			String strProjectLead = ProgramCentralConstants.ROLE_PROJECT_LEAD;
			String strExternalProjectLead = ProgramCentralConstants.ROLE_EXTERNAL_PROJECT_LEAD;

			if("RESPONSIBLE_RESOURCE_MANAGER".equalsIgnoreCase(args[0]))
			{
				if(ProgramCentralUtil.isNotNullString(strApprover) && "R".equalsIgnoreCase(strApprover) 
						&& vRoles.contains(strResourceManager))
					hasAccess = true;
			}
			else if("RESPONSIBLE_PROJECT_LEAD".equalsIgnoreCase(args[0]))
			{
				if(ProgramCentralUtil.isNotNullString(strApprover) && "P".equalsIgnoreCase(strApprover) 
						&& (vRoles.contains(strProjectLead) || vRoles.contains(strExternalProjectLead)))
					hasAccess = true;
			}

		}catch(Exception e)
		{
			throw new MatrixException(e);
		}
		return hasAccess;
	}

	/**
	 * Gives the names of timesheet names created by the context user.
	 * @param context the ENOVIA Context object.
	 * @param args Request parameters
	 * @return A list HTML formatted timesheet names created by context user.  
	 * @throws Exception if operation fails.
	 */
	public StringList getTimeSheetName (Context context, String[] args)throws Exception
	{
		StringList vTimesheetName = new StringList();
		try{
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			Map mParamList = (Map)paramMap.get("paramList");
			MapList objectList = (MapList)paramMap.get("objectList");	
			String strTableName = (String)mParamList.get("table");
			Locale locale = (Locale) mParamList.get("localeObj");
			DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			for(int i=0;i<objectList.size();i++) {
				Map objectMap = (Map)objectList.get(i);
				String timeSheetId = (String)objectMap.get(DomainConstants.SELECT_ID);	
				String timeSheetName = (String)objectMap.get(DomainConstants.SELECT_NAME);

				//Format timesheet name
				if(!"en".equals(locale.getLanguage())){
					String wkEndDate = (String)objectMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE + ".value");
					Date endDate = eMatrixDateFormat.getJavaDate(wkEndDate);
					Calendar endCal = Calendar.getInstance(locale);				
					endCal.setTime(endDate);
					Calendar startCal = endCal;
					startCal.add(Calendar.DATE, -6);
					Date startDate = startCal.getTime();
					String wkStartDate = formatter.format(startDate);
					wkEndDate = formatter.format(endDate);
					timeSheetName = wkStartDate + ProgramCentralConstants.SPACE + ProgramCentralConstants.HYPHEN + ProgramCentralConstants.SPACE + wkEndDate;
				}

				//Prepare Url
				StringBuffer sbURL = new StringBuffer();
				if(!"PMCWeeklyTimeSheetApprover".equalsIgnoreCase(strTableName)){
					sbURL.append("<a target='listHidden' href=\"../programcentral/emxProgramCentralWeeklyTimesheetUtil.jsp?mode=displayTimesheetTasks&amp;objectId=");
				}else{
					sbURL.append("<a target='listHidden' href=\"../programcentral/emxProgramCentralWeeklyTimesheetUtil.jsp?mode=displayTimesheetTasksForApprover&amp;objectId=");
				}
				sbURL.append(XSSUtil.encodeForURL(context,timeSheetId));
				sbURL.append("\">");
				sbURL.append(XSSUtil.encodeForHTML(context,timeSheetName));
				sbURL.append("</a>");

				vTimesheetName.addElement(sbURL.toString());
			}
		}catch(Exception e){
			throw e;
		}
		return vTimesheetName;
	}

	/**
	 * Creates a grid in the WBS Task table dynamically to store efforts against the task listed. 
	 * @param context the ENOVIA Context object.
	 * @param args request argumnets
	 * @return A Maplist of grid columns generated dynamically.
	 * @throws MatrixException if operation fails.
	 */
	public MapList getDynamicWeekColumns(Context context, String[] args) throws MatrixException
	{
		try {
			Calendar calendar = Calendar.getInstance();
			int week = calendar.get(Calendar.WEEK_OF_YEAR);
			String day = ProgramCentralConstants.EMPTY_STRING;
			String columnName = ProgramCentralConstants.EMPTY_STRING;
			String columnLabel = ProgramCentralConstants.EMPTY_STRING;
			Map mapColumn = new HashMap();
			Map mapSettings = new HashMap();
			MapList mlColumns = new MapList();
			Map mapProgram = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map)mapProgram.get("requestMap");
			Locale locale = (Locale)requestMap.get("localeObj");
			String projectId = (String) requestMap.get("parentOID");
			String strSelectedDate = (String)requestMap.get("PMCDatePickerCommand");
			String ctxLanguage = context.getSession().getLanguage();
			String ctxUser = context.getUser();

			Calendar startDate = Calendar.getInstance();
			Calendar endDate = Calendar.getInstance();

			String strStartDayOfWeek = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.CompanyStandards.FiscalYear.StartDayOfWeek");
			if(ProgramCentralUtil.isNullString(strStartDayOfWeek)){
				strStartDayOfWeek = "2";	
			}
			int startDay = Integer.parseInt(strStartDayOfWeek);			    

			String timesheetName = EMPTY_STRING;	
			String timesheetDisplayName = EMPTY_STRING;	
			Date selectedDate = null;
			if(ProgramCentralUtil.isNotNullString(strSelectedDate)){
				String timeZoneOffset = (String) requestMap.get("timeZone");
				String formattedInputDate = eMatrixDateFormat.getFormattedInputDate(context, strSelectedDate, 
						Task.parseToDouble(timeZoneOffset), context.getLocale());
				selectedDate = eMatrixDateFormat.getJavaDate(formattedInputDate);
			}else{
				selectedDate = calendar.getTime(); 
			}
			calendar.setTime(selectedDate);
			//week = calendar.get(Calendar.WEEK_OF_YEAR);
			WeeklyTimesheet timesheet = new WeeklyTimesheet();
			week = timesheet.getWeekNumber(context, selectedDate);
			timesheetName = timesheet.getTimesheetNameByDate(context, selectedDate);
			timesheetDisplayName = timesheet.getTimesheetNameByDate(context, selectedDate, locale);

			for (int cnt=1; cnt<=8; cnt++) {
				mapColumn = new HashMap();
				if (8==cnt){
					columnName 	= ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Common.Total", Locale.ENGLISH.getLanguage()); 
					day = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Common.Total", ctxLanguage); 
					columnLabel = day;
				}
				else if (7==cnt){
					columnName 	= ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Sunday", Locale.ENGLISH.getLanguage()); 
					day = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Sunday", ctxLanguage); 
					columnLabel = day.substring(0, 3);
				}
				else if (6==cnt){
					columnName 	= ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Saturday", Locale.ENGLISH.getLanguage()); 
					day = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Saturday", ctxLanguage); 
					columnLabel = day.substring(0, 3);
				}
				else if (5==cnt){
					columnName 	= ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Friday", Locale.ENGLISH.getLanguage()); 
					day = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Friday", ctxLanguage); 
					columnLabel = day.substring(0, 3);
				}
				else if (4==cnt){
					columnName 	= ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Thursday", Locale.ENGLISH.getLanguage()); 
					day = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Thursday", ctxLanguage); 
					columnLabel = day.substring(0, 3);
				}
				else if (3==cnt){
					columnName 	= ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Wednesday", Locale.ENGLISH.getLanguage()); 
					day = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Wednesday", ctxLanguage); 
					columnLabel = day.substring(0, 3);
				}
				else if (2==cnt){
					columnName 	= ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Tuesday", Locale.ENGLISH.getLanguage()); 
					day = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Tuesday", ctxLanguage); 
					columnLabel = day.substring(0, 3);
				}
				else if (1==cnt){
					columnName 	= ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Monday", Locale.ENGLISH.getLanguage()); 
					day = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.Calendar.Monday", ctxLanguage); 
					columnLabel = day.substring(0, 3);
				}

				mapColumn.put("name", columnName);
				mapColumn.put("label", columnLabel);
				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxWeeklyTimeSheet");
				mapSettings.put("function","getEfforts");
				mapSettings.put("Update Program","emxWeeklyTimeSheet");
				mapSettings.put("Update Function","updateEffortsAgainstAssignment");
				mapSettings.put("Column Type","programHTMLOutput");

				Map timesheetInfo = new HashMap();
				StringList slTimesheetSelect = new StringList();
				slTimesheetSelect.add(ProgramCentralConstants.SELECT_CURRENT);
				slTimesheetSelect.add(ProgramCentralConstants.SELECT_POLICY);

				String state = "Not Created";
				WeeklyTimesheet wt = timesheet.getTimesheetByName(context, timesheetName);
				if(null != wt){
					timesheetInfo = wt.getInfo(context, slTimesheetSelect);
					state = (String) timesheetInfo.get(SELECT_CURRENT);
				}
				//If timesheet is created and in Exists or Rejected state, make Editable true
				if("Not Created".equals(state) 
						|| ProgramCentralConstants.STATE_EFFORT_EXISTS.equals(state)
						|| ProgramCentralConstants.STATE_EFFORT_REJECTED.equals(state)){
					if(columnLabel.equals(ProgramCentralUtil.getPMCI18nString(context, 
							"emxProgramCentral.Common.Total", ctxLanguage))){
						mapSettings.put("Editable","false");
					}else{
						mapSettings.put("Editable","true");
					}
				}else {
					//Else if timesheet is not created, make Editable true
					mapSettings.put("Editable","false");
				}

				//Translate Timesheet State
				if("Not Created".equals(state)){
					state = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.WeeklyTimesheet.NotCreated", ctxLanguage);
				} else if(ProgramCentralConstants.STATE_EFFORT_EXISTS.equals(state)){
					state = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Common.Create", ctxLanguage);
				}else{
					String sObjectPolicy = (String) timesheetInfo.get(SELECT_POLICY);
					state = i18nNow.getStateI18NString(sObjectPolicy, state, ctxLanguage);
				}							
				mapSettings.put("Sortable","false");
				mapSettings.put("Width","5");
				mapSettings.put("Group Header", "wk" + week + ", " + timesheetDisplayName + ", " + state);
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);
			}
			return mlColumns;
		} catch (Exception e) {
			throw new MatrixException();
		}
	}

	/**
	 * Displays efforts made against the task listed in the WBS Task page.
	 * @param context the ENOVIA Context object. 
	 * @param args request arguments
	 * @return List of efforts against made against the WBS Tasks. 
	 * @throws MatrixException if operation fails.
	 */
	public StringList getEfforts(Context context, String[] args) throws MatrixException{
		StringList effortList = new StringList();
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("paramList");			
			Map columnMap = (Map)programMap.get("columnMap");			
			String day = (String) columnMap.get("name");
			String strSelectedDate = (String)requestMap.get("PMCDatePickerCommand");

			Calendar calendar = Calendar.getInstance();
			Date selectedDate = null;
			if(ProgramCentralUtil.isNotNullString(strSelectedDate)){
				String timeZoneOffset = (String) requestMap.get("timeZone");
				String formattedInputDate = eMatrixDateFormat.getFormattedInputDate(context, strSelectedDate, 
						Task.parseToDouble(timeZoneOffset), context.getLocale());
				selectedDate = eMatrixDateFormat.getJavaDate(formattedInputDate);
			}else{
				selectedDate = calendar.getTime(); 
			}
			calendar.setTime(selectedDate);
			WeeklyTimesheet wt = new WeeklyTimesheet();

			//This is not a db method
			String timesheetName = wt.getTimesheetNameByDate(context, selectedDate);
			WeeklyTimesheet timesheet = wt.getTimesheetByName(context, timesheetName);
			//String timesheetId = EMPTY_STRING;
			/*if(timesheet != null){
			timesheetId = timesheet.getId(context);
			}*/
			MapList taskInfoList  = (MapList) programMap.get("objectList");
			Iterator itrTaskInfoList = taskInfoList.iterator();
			double totalEffort = 0;
			StringBuffer sbURL = new StringBuffer();
			while (itrTaskInfoList.hasNext()) {
				Map taskInfo = (Map) itrTaskInfoList.next();
				String taskId = (String)taskInfo.get(SELECT_ID);
				//if(ProgramCentralUtil.isNotNullString(timesheetId)){
				if(timesheet != null){
					Task task = new Task(taskId);
					MapList effortInfoList = timesheet.getEfforts(context, null, task, null);
					if(effortInfoList.size()>0){
						Map effortInfo = (Map) effortInfoList.get(0);
						String effort = "0";
						if(day.contains("Total")){
							effort = (String) effortInfo.get("attribute[Total Effort]");
							totalEffort = Task.parseToDouble(effort);
							if(totalEffort > 0){
								sbURL = new StringBuffer();
								sbURL.append("<b>");
								sbURL.append(effort);
								sbURL.append("</b>");	
								//								sbURL.append("<a style=\"font-weight:bold\" href=\"JavaScript:showModalDialog('../common/emxIndentedTable.jsp?tableMenu=PMCWeeklyTimesheetViews&amp;jsTreeID=root&amp;table=PMCWeeklyTimeSheetView&amp;header=emxProgramCentral.WeeklyTimesheet.MyWeeklyTimeSheet&amp;SuiteDirectory=programcentral&amp;expandProgram=emxWeeklyTimeSheet:displayTimesheetTasks&amp;StringResourceFileId=emxProgramCentralStringResource&amp;suiteKey=ProgramCentral&amp;HelpMarker=emxhelptimesheetview&amp;multiColumnSort=false&amp;customize=true&amp;massPromoteDemote=false&amp;triggerValidation=false&amp;objectCompare=false&amp;postProcessJPO=emxWeeklyTimeSheet:postProcessRefresh&amp;rowGrouping=false&amp;displayView=details&amp;showPageURLIcon=false&amp;objectId=");
								//								sbURL.append(timesheetId);
								//								sbURL.append("','700','600','false','popup')\">");                    
								//								sbURL.append(effort);
								//								sbURL.append("</a>");		
								effort = sbURL.toString();
							}
						}else{
							effort = (String) effortInfo.get("attribute[" + day + "]");
						}
						effortList.add(effort);
					}else{
						effortList.add("0.0");
					}

				}else {
					effortList.add(" ");
				}
			}
		}catch(Exception e){
			throw new MatrixException();
		}
		return effortList;
	}

	/**
	 * Updates effort for the tasks listed in the WBS Task page. 
	 * @param context the ENOVIA Context object
	 * @param args reuqest arguments
	 * @throws Exception if operation fails.
	 */
	public void updateEffortsAgainstAssignment(Context context, String[] args) throws Exception{
		Map programMap = (Map) JPO.unpackArgs(args);
		Map paramMap = (Map)programMap.get("paramMap");
		Map columnMap = (Map)programMap.get("columnMap");
		Map requestMap = (Map)programMap.get("requestMap");
		try{
			String strNewEffort = (String) paramMap.get("New Value");
			String taskId = (String) paramMap.get("objectId");
			String ctxUser = context.getUser();
			//if new value is null, return.
			if(ProgramCentralUtil.isNullString(strNewEffort))
				return;
			//if entered effort is less than 1 or greater than 24, return 
			double effort = Task.parseToDouble(strNewEffort);
			if(effort < 0)
				return;
			else if(effort > 24){
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.WeeklyTimeSheet.Effort.validateEffortEntry", context.getSession().getLanguage());
				MqlUtil.mqlCommand(context, "error " + sErrMsg);
				return;
			}

			String strSelectedDate = XSSUtil.decodeFromURL((String)requestMap.get("PMCDatePickerCommand"));

			Calendar calendar = Calendar.getInstance();
			Date selectedDate = null;
			if(ProgramCentralUtil.isNotNullString(strSelectedDate)){
				String timeZoneOffset = (String) requestMap.get("timeZone");
				String formattedInputDate = eMatrixDateFormat.getFormattedInputDate(context, strSelectedDate, 
						Task.parseToDouble(timeZoneOffset), context.getLocale());
				selectedDate = eMatrixDateFormat.getJavaDate(formattedInputDate);
			}else{
				selectedDate = calendar.getTime(); 
			}
			calendar.setTime(selectedDate);
			WeeklyTimesheet wt = new WeeklyTimesheet();

			//Not a db method
			String timesheetName = wt.getTimesheetNameByDate(context, selectedDate);
			WeeklyTimesheet timesheet = wt.getTimesheetByName(context, timesheetName);
			//String timesheetId = timesheet.getId(context);

			//Extract end date from timesheet name
			String[] timesheetDates = timesheetName.split(" - ");
			String strEndDate = timesheetDates[1];

			//Check if the timesheet for same week name is present. If not, create one. 
			Map attributes = new HashMap();
			if(timesheet == null){
				SimpleDateFormat sdf = new SimpleDateFormat("MMM dd-yy");
				Date endDate = sdf.parse(strEndDate);
				
				timesheet = new WeeklyTimesheet();
				String result = timesheet.create(context, POLICY_WEEKLY_TIMESHEET, null, ctxUser, 
						null, endDate, attributes);
			}
			//Take the task Object and  new  Effort  Value 
			String effortId = DomainObject.EMPTY_STRING;
			String day = (String) columnMap.get("name");
			Person person = new Person(ctxUser);
			Task task = new Task(taskId);
			MapList effortInfoList = timesheet.getEfforts(context, null, task, null);
			if(effortInfoList.size()>0){
				Map effortInfo = (Map) effortInfoList.get(0);
				effortId = (String) effortInfo.get(SELECT_ID);
			}else{
				effortId = timesheet.addEffort(context, taskId);
			}
			DomainObject wkEffort = DomainObject.newInstance(context, effortId);
			wkEffort.setAttributeValue(context, day, strNewEffort);
		}catch(Exception e ){
			throw new MatrixException(e);
		}
	}

}

