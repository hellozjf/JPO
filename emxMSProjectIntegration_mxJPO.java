/*   ${CLASSNAME}.java
 **
 **   Copyright (c) 2003-2017 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **  @quickReview 16:02:23 AMA3: FUN058265 Multiple Calendars Integration
 **  @quickReview 16:09:16 ACE2: TSK2879141: ENOVIA_GOV_MSF_2017x_FD01_Microsoft_Project_Additional_Performance_Improvements
 **  @quickReview 17:01:11 ACE2 Code Clean-up
 **  @quickReview 17:03:06 ACE2 IR-504776-3DEXPERIENCER2017x: Circular dependency issue 
 **  @quickreview 17:03:23 ODW IR-500132-3DEXPERIENCER2018x: Creating a Project from MPI sets Company visibility by default without prompting user for selection
 **  @quickReview 17:04:03 ODW IR-494888-3DEXPERIENCER2018x:MPI_Project members are not shown in "Resource Name" column 
 **  @quickReview 17:04:14 BLN1: To hide cutome attribute which user doest not want to see in column list of MSProject
 **  @quickReview 17:05:29 ACE2 IR-519220-3DEXPERIENCER2017x: Task added from MS project sets incorrect default value to custom attributes & compare UI keep trying to sync
 *
 */

import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.AssignedTasksRelationship;
import com.matrixone.apps.common.DependencyRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.WorkCalendar;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UITable;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.jdom.output.XMLOutputter;


/**
 * The <code>emxMSProjectIntegration</code> class represents the JPO for the MS
 * Project integration synchronization mechanism
 */
public class emxMSProjectIntegration_mxJPO extends ProjectSpace 
{
	/* Fields */
	
	// Enable/disable debugging; Must be false in release
	private boolean debug = false;
	
	// Metadata
	private Boolean isExperiment;
	private String projectSchduleBasedOn;
	private String projectVisibility;
	private boolean inTransaction, sequenceChanged;
	
	// Context
	private Context context;
	private StringList experimentSubTypes, projectSpaceSubTypes, taskSubTypes;
	private Map<String, String> defaultTypePolicyMap = new HashMap<String, String>();

	private static HashMap<String, String> userNameIdMap = new HashMap<String, String>(); // Map of user's "<last name> <first name>" to userId
	private HashMap<String, String> userUIDBusinessIdMap = new HashMap<String, String>(); // Map of user's "project UID" to BusinessId

	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
	private SimpleDateFormat MATRIX_DATE_FORMAT;
	
	// Project and task
	private Task task, parentTask;
	private ProjectSpace project, subProject;
	private StringList busSelects = new StringList(); // Selected 'selectable' expressions
	private StringList taskSelectable, projectSelectable; // Set of 'selectable' expressions used in Table
	private StringList taskRelSelectable = new StringList();

	// Created business items
	private StringList projectList = new StringList();
	private HashMap<String, String> taskUIDMap = new HashMap<String, String>();
	private HashMap<String, Element> taskIdElementMap = new HashMap<String, Element>();

	private HashMap<String, HashMap> dependenciesMap = new HashMap<String, HashMap>();
	
	// Constants
	private static final String RELATIONSHIP_SUBTASK = PropertyUtil.getSchemaProperty("relationship_Subtask");
	private static final String SELECT_ATTRIBUTE_SCHEDULEBASEDON = "attribute[" + PropertyUtil.getSchemaProperty("attribute_ScheduleBasedOn") + "]";

	private RelationshipType relSubTask = new RelationshipType(RELATIONSHIP_SUBTASK);
	
	private static final String RELATIONSHIP_ATTR_SEPARATOR = ",";
	private static final String SELECT_TASK_WBS = "to["+ RELATIONSHIP_SUBTASK +"].";
	private static final String SELECT_TO_RELATIONSHIP_SUBTASK_ID = "to[" + RELATIONSHIP_SUBTASK + "].id";
	private static final String SELECT_TASK_CALENDAR = "from[" + ProgramCentralConstants.RELATIONSHIP_CALENDAR +"].to.id";
	private static final String SELECT_ATTRIBUTE_PROJECT_SCHEDULE_FROM = "attribute[" + ATTRIBUTE_PROJECT_SCHEDULE_FROM	+ "]";
	private static final String SELECT_DEFAULT_CALENDAR = "from["+ ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR + "].to.id";
	private static final String SELECT_TASK_ASSIGNEE_ID = "to[" + ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS + "].from.id";
	private static final String SELECT_SUCCESSOR_IDS = "to[" + RELATIONSHIP_DEPENDENCY + "].from.id"; // No access from ProjectSpace / Task, so duplicating
	private static final String SELECT_ASSIGNEE_ALLOCATION = "to[" + ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS + "]."  + "attribute[" + PropertyUtil.getSchemaProperty("attribute_PercentAllocation") + "]";
		

	// TODO: ACE2 - Reconsider if such metadata is required and should it be partially static
	private Map<String, Map> taskBusColAttrMap; // Relationship and Business less Custom JPO attrinute updates (Task onky)
	private static Map<String, String> taskColNameBusAttrMap = new HashMap<String,String>(); // Business attrinute updates (Task onky)
	private static Map<String, String> taskColNameRelAttrMap = new HashMap<String,String>(); // Relationship attrinute updates (Task onky)

	private Map<String, Map> taskUpdateColAttrMap = new HashMap<String,Map>(), projectUpdateColAttrMap = new HashMap<String,Map>(); // Custom JPO updates
	private Map<String, Map> taskColAttrMap = new HashMap<String,Map>(), projectColAttrMap = new HashMap<String,Map>(); // Relationship and Business attrinute updates
	
	//ignore basic attributes as specific API is called for each basic attribute
	private final StringList basicfields = new StringList(new String[] { "Owner", "NodeType", "Name", "Company" });
	private final StringList basicfieldsExisting = new StringList(new String[] { "Name", "Owner", "State","NodeType", "Title", "CreationDate", "Company", "Finish", "Start" });
	
	//following fields need to pass in updateDates() and other attributes needs to pass in setAttributeValues
	private final StringList scheduleFieldsInSave = new StringList(new String[] {"ConstraintType", "ConstraintDate", "Duration", "Start", "Finish", "ActualStart", "ActualFinish", "PercentComplete"});
	
	private HashMap<String, String> GetConstraintConstantMap = new HashMap<String, String>() { { put("As Soon As Possible", "0"); put("As Late As Possible", "1"); put("Must Start On", "2"); put("Must Finish On", "3"); put("Start No Earlier Than", "4"); put("Start No Later Than", "5"); put("Finish No Earlier Than", "6"); put("Finish No Later Than", "7"); } };
	
	/* Initiation */

	public emxMSProjectIntegration_mxJPO () { /* Do nothing */ }
	
	public emxMSProjectIntegration_mxJPO(Context context, String[] args) throws Exception 
	{
		// instantiate beans
		task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
		parentTask = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
		project = (ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
		subProject = (ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);

		//instantiate mapping of project and tasks fields
		projectSelectable = SetProjectTasksMapping(context, false);
		taskSelectable = SetProjectTasksMapping(context, true);
	}
	
	public String mxMain(Context context, String[] args) throws Exception { return DomainConstants.EMPTY_STRING; }
	
	private StringList SetProjectTasksMapping(Context context, boolean forTask) throws Exception 
	{
		StringList selectableList = new StringList();
		
		for(HashMap columnMap : (List<HashMap>)UITable.getColumns(UITable.getTable(context, forTask ? "MPITaskColumnMapping" : "MPIProjectColumnMapping"))) 
		{
			String columnName = UITable.getName(columnMap);
				
			Map columnSettingMap = new HashMap();
			columnSettingMap.put("Name", columnName);

			String columnTypeDetail = UITable.getSetting(columnMap, "MPIColumnType");
			if(null != columnTypeDetail && !columnTypeDetail.isEmpty()) { columnSettingMap.put("MPIColumnType", columnTypeDetail); }
				
			String ignoreForServer = UITable.getSetting(columnMap, "MPIIgnoreForServer");
			if(null != ignoreForServer && !ignoreForServer.isEmpty()){ columnSettingMap.put("MPIIgnoreForServer", ignoreForServer); }

			String columnExpr = (String)columnMap.get("expression_businessobject");
			columnSettingMap.put("BusinessExpression", columnExpr);

			if(columnExpr != null && !columnExpr.isEmpty()) 
			{
				(forTask ? taskColAttrMap : projectColAttrMap).put(columnExpr, columnSettingMap);
				selectableList.add(columnExpr);

				if(forTask) { taskColNameBusAttrMap.put(columnName, columnExpr); }
			}
			else if(DomainConstants.EMPTY_STRING.equals(ignoreForServer)) 
			{
				String columnRelExpr = (String)columnMap.get("expression_relationship");
				columnSettingMap.put("RelationshipExpression", columnRelExpr);

				if(!taskRelSelectable.contains(columnRelExpr)){ taskRelSelectable.add(columnRelExpr); }
				(forTask ? taskColAttrMap : projectColAttrMap).put(columnRelExpr, columnSettingMap);

				if(forTask) { taskColNameRelAttrMap.put(columnName,columnRelExpr); }
			}
				
			String functionName = UITable.getSetting(columnMap, "Update Function");
			String programName = UITable.getSetting(columnMap, "Update Program");

			if(functionName != null && !functionName.isEmpty())
			{
				columnSettingMap.put("Update Function", functionName);
				columnSettingMap.put("Update Program", programName);
				(forTask ? taskUpdateColAttrMap : projectUpdateColAttrMap).put(columnExpr, columnSettingMap);
			}
		}
			
		taskBusColAttrMap = new HashMap<String,Map>(taskColAttrMap);
		taskBusColAttrMap.keySet().removeAll(taskUpdateColAttrMap.keySet());
		
		return selectableList;
	}

	/* Public Methods */

	/*
	 * JPO Entry point
	 */
	public String executeMPICommand(Context context, String[] args) throws Exception 
	{
		try
		{
			DebugLog("executeMPICommand", false, null, false);

			this.context = context;
			context.setLanguage(args[0]);
			context.setLocale(new Locale(args[0])); 

			eMatrixDateFormat.setEMatrixDateFormat(context);
			MATRIX_DATE_FORMAT = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);

			boolean isExistingProject = true;
			Element commandElement = CreateElementFromXML(args[1]), responseElement = null;
			String commandName = commandElement.getAttributeValue("cname");
			
			switch(commandName)
			{
				case "validateresource":
					responseElement = CreateValidateResourceResponse(ValidateResource(commandElement, null), "success");
					break;

				case "isExistingProject":
					isExistingProject = false; // Need to check if exists

				case "getProjectData":
					responseElement = GetProjectData(GetElementValue(commandElement, "BusinessID"), !isExistingProject, true);
					break;

				case "mergeProject":
					responseElement = MergeProject(commandElement, args[0], args[2], args[3]);
					break;

				case "MPISyncUIColumnSummary":
					responseElement = GetConstraintMetaData(args[0]);
					break;
			}

			DebugReport("executeMPICommand", true, "End command " + commandName);
			CommandTermination(null);

			return CreateXMLFromElement(responseElement);
		} 
		catch (Exception e) 
		{ 
			throw CommandTermination(e);
		}
	}
	
	/*
	 * Detail required by login
	 */
	public StringList GetProjectSpaceSubTypes(Context context, String[] args) throws Exception 
	{
		if (null == projectSpaceSubTypes) { projectSpaceSubTypes = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_SPACE); }

		return projectSpaceSubTypes;
	}
	
	public MapList getAllUserProjects(Context context, String[] args) throws Exception
	{
		ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_PRG_LICENSE_ARRAY);
		busSelects.clear();
		busSelects.add(SELECT_ID);
		return getProjects(context, Person.getPerson(context), new Pattern(TYPE_PROJECT_SPACE).getPattern(), busSelects, null, "type!=" + "'" + ProgramCentralConstants.TYPE_PROJECT_SNAPSHOT + "'", DomainConstants.EMPTY_STRING, true);
	}
	
	/* Private Methods - Commands */
	
	/**
	 * Validate Resource names
	 * Created UID map only when empty and not null invalidResources is provided
	 */
	private ArrayList<String> ValidateResource(Element projectElement, ArrayList<String> invalidResources) throws Exception
	{
		boolean hasInvalidResources = null == invalidResources || 0 != invalidResources.size();
		if (null == invalidResources) { invalidResources = new ArrayList<String>(); }

		for (Element resource : (List<Element>)projectElement.getChild("Resources").getChildren("Resource")) 
		{
			GetUserFromLastNameFirstName(resource.getChild("Name").getText(), hasInvalidResources ? null : resource.getChild("ResourceUID").getText(), invalidResources);
		}

		return invalidResources;
	}
	
	/**
	 * Get Project data
	 * @param transactionOnly: Get details like Project exists, isExperiment and ownedByUser only
	 * @param isRootProject: Get complete open response
	 * @param !isRootProject: Get Project element only
	 * 
	 */
	private Element GetProjectData(String businessID, boolean transactionOnly, boolean isRootProject) throws Exception 
	{
		Map projectMap = null;
		String projectType = null;
		
		Boolean isLoggedInSameAsOwner = false;
		Boolean isExistingProject = !transactionOnly || IsExistingProject(businessID, false, null);
		isRootProject |= transactionOnly;

		/* Query Project data */

		if (isExistingProject)
		{
			busSelects.clear();
			busSelects.add(SELECT_TYPE);
			busSelects.add(SELECT_OWNER);
		}

		if (!transactionOnly)
		{
			busSelects.add(SELECT_ID);
			busSelects.add(SELECT_POLICY);
			busSelects.add(SELECT_CURRENT);
			busSelects.add(SELECT_DEFAULT_CALENDAR);
			busSelects.add(SELECT_PERCENT_COMPLETE);
			busSelects.add(SELECT_TASK_ACTUAL_DURATION);
			busSelects.add(SELECT_TASK_CONSTRAINT_DATE);
			busSelects.add(SELECT_TASK_ACTUAL_START_DATE);
			busSelects.add(SELECT_TASK_ESTIMATED_DURATION);
			busSelects.add(SELECT_TASK_ACTUAL_FINISH_DATE);
			busSelects.addAll(projectSelectable);
		}
		
		/* Read Project data */
		
		if (isExistingProject)
		{
			project.setId(businessID);
			projectMap = project.getInfo(context, busSelects);

			IsExperiment(projectType = (String)projectMap.get(SELECT_TYPE));
			isLoggedInSameAsOwner = context.getUser().equals((String)projectMap.get(SELECT_OWNER));
		}

		/* Create Response */
		
		Element responseElement = isRootProject ? new Element("transaction") : new Element("Project");
		Element projectElement = responseElement;

		if (isRootProject)
		{
			// Create transaction
			responseElement.setAttribute("result", "success");

			AddElements(responseElement, "IsExperiment", isExperiment.toString());
			AddElements(responseElement, "LoggedinIsOwner", isLoggedInSameAsOwner.toString());
			
			if (transactionOnly)
			{
				AddElements(responseElement, "ProjectExist", isExistingProject.toString());

				return responseElement;
			}

			AddElements(responseElement, "BusinessID", businessID);
			AddElements(responseElement, "ProjectScheduleOn", projectSchduleBasedOn = (String)projectMap.get(SELECT_ATTRIBUTE_SCHEDULEBASEDON));
		
			// Create Project
			Element projects = new Element("Projects");
			responseElement.addContent(projects);

			projectElement = new Element("Project");
			projects.addContent(projectElement);
		}

		projectList.add(businessID); 
		int parentIndex = projectList.indexOf(businessID);
		AddElements(projectElement, "Title", (String) projectMap.get(SELECT_NAME));
		AddElements(projectElement, "PercentComplete", Integer.toString(new Integer(Math.round(Float.valueOf((String) projectMap.get(SELECT_PERCENT_COMPLETE))))));
		
		SetProjectDetails(projectElement, projectMap, parentIndex, false);			
		
		// Create Tasks, Summary
		Element tasksElement = new Element("Tasks");
		projectElement.addContent(tasksElement);
		
		Element summaryTaskElement = new Element("Task");
		tasksElement.addContent(summaryTaskElement);
		
		SetProjectDetails(summaryTaskElement, projectMap, parentIndex, true);

		// Create calendar
		String calendarID = (String) projectMap.get(SELECT_DEFAULT_CALENDAR);
		if (calendarID != null && calendarID.length() > 0)
		{
			WorkCalendar workCalendar = new WorkCalendar(calendarID);
			workCalendar.load(context);
			String calendarName = workCalendar.getName(context);

			AddElements(projectElement, "Calendar", calendarID, "CalendarName", calendarName);
			AddElements(summaryTaskElement, "Calendar", calendarID, "CalendarName", calendarName);
		}

		// Create Resources		
		Element resourcesElement = new Element("ProjectMemberResources");		
		CreateResourceElements(resourcesElement);		
		projectElement.addContent(resourcesElement);
	
		// Create task structure
		CreateTaskElements(tasksElement, businessID, 1, parentIndex, 1);

		if (isRootProject) 
		{ 
			HashMap<String, String> resourceUIDs = new HashMap<String, String> ();
			
			// Enrich task data
			UpdateTaskData(projectType, resourceUIDs); 
			
			// Read resources meta
			ReadProjectResources(projectElement, resourceUIDs);
		}

		return responseElement;
	}
	
	public Element MergeProject(Element commandElement, String userLanguage, String deletedTasks, String deletedProjects) throws Exception 
	{
		InitTransaction();
		
		ArrayList<String> invalidResources = new ArrayList<String>();
		String projectId = ExecuteMergeProject(commandElement.getChild("Projects").getChild("Project"), true, null, userLanguage, deletedTasks, deletedProjects, invalidResources, false); 
		
		if (invalidResources.size() > 0) 
		{
			 return CreateValidateResourceResponse(invalidResources, "invalidresource");
		}
		else
		{
			return GetProjectData(projectId, false, true);
		}
	}

	private Element GetConstraintMetaData(String language) throws Exception
	{
		Element labelInfo = new Element("LabelInformation");
		
		Element label = AddElements(labelInfo, "Label");
		Element constraintList = AddElements(label, "ConstraintList");
		
		AttributeType atrDefaultConstraint = new AttributeType(ATTRIBUTE_TASK_CONSTRAINT_TYPE);

		atrDefaultConstraint.open(context);
		for (String constrType : (List<String>)atrDefaultConstraint.getChoices(context))
		{
			Element constraint = AddElements(constraintList, "Constraint");

			constraint.setAttribute("Name", constrType);
			constraint.setAttribute("Label", EnoviaResourceBundle.getRangeI18NString(context, ATTRIBUTE_TASK_CONSTRAINT_TYPE, constrType, language));
		}

		atrDefaultConstraint.close(context);

		return labelInfo;	
	}

	/* Private Methods - Initiation */
	
	private void InitTransaction() throws Exception { ContextUtil.startTransaction(context, inTransaction = true); }
	
	private Exception CommandTermination(Exception e) throws Exception
	{
		// Success
		if (null == e) 
		{
			if (inTransaction) { ContextUtil.commitTransaction(context); }

			return null;
		}

		// Fail
		if (inTransaction) { ContextUtil.abortTransaction(context); }

		e.printStackTrace();
		return -1 == e.getMessage().indexOf("well-formed character") ? e : new Exception("One of the tasks in the project contains characters that is not supported, Check the task names and the notes. \n" + e.getMessage());
	}
	
	/**
	 * XML String to Element
	 */
	private Element CreateElementFromXML(String xml) throws Exception 
	{
		Element element = new SAXBuilder().build(new StringReader(xml)).getRootElement();
		if (element == null) { throw new Exception("Failed to create XElement from string xml: " + xml); }

		return element;
	}
	
	/**
	 * Element to XML String
	 */
	private String CreateXMLFromElement(Element element) throws Exception 
	{
		Document document = new Document(element);
		document.setDocType(null);

		return new XMLOutputter().outputString(document);
	}
	
	/* Private Methods - Get Project */
	
	private void SetProjectDetails(Element projectElement, Map detailsMap, int parentIndex, boolean isSummary) throws Exception
	{
		AddElements(projectElement, "UID",  parentIndex + "#0");
		AddElements(projectElement, "ID", isSummary ? "0" : (DomainConstants.EMPTY_STRING + parentIndex));

		if (isSummary)
		{
			AddElements(projectElement, "Summary", "True");
			AddElements(projectElement, "OutlineNumber", "0");
			AddElements(projectElement, "OutlineLevel", "0");
			AddElements(projectElement, "BusinessID", (String) detailsMap.get(SELECT_ID));
			AddElements(projectElement, "State", (String) detailsMap.get(SELECT_CURRENT));	
			AddElements(projectElement, "PercentComplete", SetDetailsAsPerFormat("percent", (String) detailsMap.get(SELECT_PERCENT_COMPLETE)));
			AddElements(projectElement, "NodeType", (String) detailsMap.get(SELECT_TYPE));
			AddElements(projectElement, "Policy", (String) detailsMap.get(SELECT_POLICY));

			String taskEstDuration = (String) detailsMap.get(SELECT_TASK_ESTIMATED_DURATION);
			AddElements(projectElement, "Duration", taskEstDuration);

			if("Actual".equals(projectSchduleBasedOn))
			{	
				AddElements(projectElement, "ActualStart", FormatAsProjectDate((String) detailsMap.get(SELECT_TASK_ACTUAL_START_DATE)));
				AddElements(projectElement, "ActualFinish", FormatAsProjectDate((String) detailsMap.get(SELECT_TASK_ACTUAL_FINISH_DATE)));
				AddElements(projectElement, "ActualDuration", (String) detailsMap.get(SELECT_TASK_ACTUAL_DURATION));
			}
		}

		String content, scheduleFrom = (String) detailsMap.get(SELECT_ATTRIBUTE_PROJECT_SCHEDULE_FROM);

		for(Map setting : projectColAttrMap.values()) 
		{
			content = DomainConstants.EMPTY_STRING;
			String sKey = (String) setting.get("BusinessExpression");	

			//to handle, if value is multivalue (for relationship attribute)
			if(detailsMap.get(sKey) instanceof String) 
			{
				content = (String) detailsMap.get(sKey);
			}
			else if(detailsMap.get(sKey) instanceof StringList) 
			{
				for(String detail : (List<String>)detailsMap.get(sKey))
				{
					content = content.concat(detail).concat(RELATIONSHIP_ATTR_SEPARATOR);
				}

				content = content.substring(0, content.length() -1 );
			}

			String columnName = (String)setting.get("Name");
			String columnType = (String)setting.get("MPIColumnType");

			if(columnName.equals("ScheduleFrom")) 
			{
				content = scheduleFrom.equals("Project Finish Date") ? "False" : "True";
			}			
			else if(columnName.equals("ProjectStart")) 
			{
				if(isSummary) { columnName = "Start"; }

				content = (String) detailsMap.get(!isSummary && scheduleFrom.equals("Project Start Date") ? SELECT_TASK_CONSTRAINT_DATE : SELECT_TASK_ESTIMATED_START_DATE);
			}
			else if(columnName.equals("ProjectFinish")) 
			{
				if(isSummary) { columnName = "Finish"; }
				
				content = (String) detailsMap.get(!isSummary && scheduleFrom.equals("Project Finish Date") ? SELECT_TASK_CONSTRAINT_DATE : SELECT_TASK_ESTIMATED_FINISH_DATE);
			}

			if(columnType != null && !columnType.isEmpty() && null != content && !content.isEmpty()) 
			{
				content = SetDetailsAsPerFormat(columnType.toLowerCase(), content);
			}

			AddElements(projectElement, columnName, content);
		}
	}
	
	private void CreateResourceElements(Element resourcesElement) throws Exception
	{
		busSelects.clear();
		busSelects.add(Person.SELECT_ID);
		busSelects.add(Person.SELECT_FIRST_NAME);
		busSelects.add(Person.SELECT_LAST_NAME);
		busSelects.add(Person.SELECT_EMAIL_ADDRESS);

		MapList membersList = project.getMembers(context, busSelects, null, null, null);

		ListIterator membersItr = membersList.listIterator();
		
		while (membersItr.hasNext())
		{
			Element resourceElement = new Element("Resource");
			Map membersMap = (Map) membersItr.next();
			String personName = (String)membersMap.get(Person.SELECT_LAST_NAME) + " " + (String)membersMap.get(Person.SELECT_FIRST_NAME);
			//add to the resourceMap if the person is not already added to the resource list
			String businessId = (String)membersMap.get(Person.SELECT_ID);
			//create the resource details
			AddElements(resourceElement, "ResourceUID", businessId);

			AddElements(resourceElement, "Name", personName);
			AddElements(resourceElement, "EmailAddress", (String)membersMap.get(Person.SELECT_EMAIL_ADDRESS));
			resourcesElement.addContent(resourceElement);
		}
	}
	
	private int CreateTaskElements(Element tasksElement, String id, int counter, int parentIndex, int level) throws Exception
	{
		StringList taskSelects = new StringList();
		taskSelects.add(task.SELECT_ID);
		taskSelects.add(task.SELECT_TYPE);
		taskSelects.add(task.SELECT_HAS_SUBTASK);

		task.setId(id);

		for (Map map : (List<Map>)task.getTasks(context, task, 1, taskSelects, null)) 
		{
			String index = String.valueOf(counter++);
			String taskID = (String) map.get(task.SELECT_ID);
			boolean isProject = IsProject((String) map.get(task.SELECT_TYPE));
			boolean hasSubTask = "true".equalsIgnoreCase((String) map.get(task.SELECT_HAS_SUBTASK));
		
			/* Create Task element*/
			Element taskElement = new Element("Task");
			tasksElement.addContent(taskElement);
			
			AddElements(taskElement, "ID", index);
			AddElements(taskElement, "Type", "1");
			AddElements(taskElement, "EffortDriven", "0");
			AddElements(taskElement, "BusinessID", taskID);
			AddElements(taskElement, "UID", parentIndex + "#" + index);
			AddElements(taskElement, "OutlineLevel", Integer.toString(level));
			AddElements(taskElement, "Summary", isProject || hasSubTask ? "True" : "False");

			taskIdElementMap.put(taskID, taskElement);
			taskUIDMap.put(taskID, parentIndex + "#" + index);
			
			if(isProject)
			{
				taskElement.addContent(GetProjectData(taskID, false, false));
			}
			else if(hasSubTask) 
			{
				counter = CreateTaskElements(tasksElement, taskID, counter, parentIndex, level + 1);
			}
		}

		return counter;
	}

	private void UpdateTaskData(String projectType, HashMap<String, String> resourceUIDs) throws Exception
	{
		busSelects.clear();
		busSelects.add(SELECT_ID);
		busSelects.add(SELECT_POLICY);
		busSelects.add(SELECT_TASK_CALENDAR);
		busSelects.add(SELECT_SUCCESSOR_IDS);
		busSelects.add(SELECT_PREDECESSOR_IDS);
		busSelects.add(task.SELECT_HAS_SUBTASK);		
		busSelects.add(SELECT_PREDECESSOR_TYPES);
		busSelects.add(task.SELECT_SUCCESSOR_TYPES);
		busSelects.add(SELECT_PREDECESSOR_LAG_TIMES);
		busSelects.add(task.SELECT_SUCCESSOR_LAG_TIMES);
		busSelects.add(SELECT_ATTRIBUTE_PROJECT_SCHEDULE_FROM);
		
		//Filter the TaskSelect by Project type			
		if(projectType != null && !projectType.isEmpty())
		{
			StringList taskBusSelect = new StringList();
			StringList taskRelSelect = new StringList();
			GetTypeSpecificTaskSelectables(projectType, isExperiment.toString(), taskBusSelect, taskRelSelect);

			busSelects.addAll(taskBusSelect);
			taskRelSelectable = taskRelSelect;
		}

		for(String rel : (List<String>)taskRelSelectable)
		{
			busSelects.add(SELECT_TASK_WBS.concat(rel));
		}
				
		busSelects.add(SELECT_TASK_ASSIGNEE_ID);
		busSelects.add(SELECT_ASSIGNEE_ALLOCATION);

		MULTI_VALUE_LIST.add(SELECT_TASK_ASSIGNEE_ID);
		MULTI_VALUE_LIST.add(SELECT_ASSIGNEE_ALLOCATION);
		
		DecimalFormat unitsFormat = new DecimalFormat("0.##");
	
		for (Map taskDetailsMap : (List<Map>)DomainObject.newInstance(context).getInfo(context, taskIdElementMap.keySet().toArray(new String[taskIdElementMap.size()]), busSelects)) 
		{
			String currentTaskId = (String) taskDetailsMap.get(task.SELECT_ID);

			Element taskElement = taskIdElementMap.get(currentTaskId);
			AddTaskDetails(currentTaskId, taskElement, taskColAttrMap, taskDetailsMap);

			AddTaskResources(taskElement, taskDetailsMap, resourceUIDs);
			AddTaskDependencies(taskElement, taskDetailsMap, true, unitsFormat);
			AddTaskDependencies(taskElement, taskDetailsMap, false, unitsFormat);
		}
	}
	
	private void AddTaskDetails(String id, Element taskElement, Map<String, Map> colAttrMap, Map detailsMap) throws Exception 
	{
		AddElements(taskElement, "Policy", (String) detailsMap.get(SELECT_POLICY));
		
		WorkCalendar taskCalendar = WorkCalendar.getCalendar(context, id);

		if (taskCalendar != null)
		{			
			AddElements(taskElement, "Calendar", taskCalendar.getId(context), "CalendarName", taskCalendar.getName(context));
		}

		String sKey = DomainConstants.EMPTY_STRING;
		try {
			String content = DomainConstants.EMPTY_STRING;

			for(String key : colAttrMap.keySet())
			{
				content = DomainConstants.EMPTY_STRING;
				Map colSettingMap = colAttrMap.get(sKey = key);

				//for relationship selectable(only for WBS)
				if (!detailsMap.containsKey(sKey)) 
				{
					sKey = SELECT_TASK_WBS.concat(key);
				}
				//to handle, if value is multivalue (for relationship attribute)
				if(detailsMap.get(sKey) instanceof String)
				{					
					content = (String) detailsMap.get(sKey);
				}
				else if(detailsMap.get(sKey) instanceof StringList) {
					content = DomainConstants.EMPTY_STRING;
					for(String contentItem : (List<String>) detailsMap.get(sKey)){
						content = content.concat(contentItem).concat(RELATIONSHIP_ATTR_SEPARATOR);
					}
					content = content.substring(0, content.length() - 1);
				}

				String columnName = (String)colSettingMap.get("Name");

				String columnType = (String)colSettingMap.get("MPIColumnType");
				String sIgnoreForServer = (String)colSettingMap.get("MPIIgnoreForServer"); 

				if(sIgnoreForServer != null && sIgnoreForServer.equals("true"))
					continue;				

				if((columnName.equals("ActualStart") || columnName.equals("ActualFinish") || columnName.equals("ActualDuration")) && "Estimated".equals(projectSchduleBasedOn))
				{
					AddElements(taskElement, columnName, DomainConstants.EMPTY_STRING);
					continue;
				}			

				if(columnType != null && !columnType.isEmpty()) 
				{
					if(columnType.equals("constraint")) {

						if(IsProject((String)detailsMap.get(task.SELECT_TYPE))) 
						{
							content = "As Soon As Possible";
							String sScheduleFrom = (String) detailsMap.get(SELECT_ATTRIBUTE_PROJECT_SCHEDULE_FROM);

							if(null != sScheduleFrom && sScheduleFrom.equals("Project Finish Date"))		  
								content = "As Late As Possible";
						}
					}
					if(content!= null && !content.isEmpty()) {
						content = SetDetailsAsPerFormat(columnType.toLowerCase(),content);
					}
				}

				AddElements(taskElement, columnName, content);
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new Exception("Null pointer exception for " + sKey + " " + e.getMessage());
		}
	}

	private void AddTaskResources(Element taskElement, Map taskDetailsMap, HashMap<String, String> resourceUIDs) throws Exception
	{
		if(!(taskDetailsMap.get(SELECT_TASK_ASSIGNEE_ID) instanceof StringList)) { return; }

		Element resourcesElement = AddElements(taskElement, "Resources");

		List<String> resourceIDs = (List<String>)taskDetailsMap.get(SELECT_TASK_ASSIGNEE_ID);
		List<String> resourceAllocatoins = (List<String>)taskDetailsMap.get(SELECT_ASSIGNEE_ALLOCATION);

		for(int i = 0; i < resourceIDs.size(); i++)
		{
			String allocation = resourceAllocatoins.get(i);
			String resourceBusinessID = resourceIDs.get(i);

			String resourceUID = resourceUIDs.get(resourceBusinessID);
			if (null == resourceUID) { resourceUIDs.put(resourceBusinessID, resourceUID = DomainConstants.EMPTY_STRING + resourceUIDs.size()); }

			// Assign the resource to a task
			Element resource = AddElements(resourcesElement, "Resource");
			AddElements(resource, "ResourceUID", resourceUID, "Units", allocation);
		}
	}

	private void ReadProjectResources(Element rootProjectElement, HashMap<String, String> resourceUIDs) throws Exception
	{
		if (0 == resourceUIDs.size()) { return; }
		
		String locationId;
		WorkCalendar workCalendar;
		Element resourceElement, resourcesElement = AddElements(rootProjectElement, "Resources");
		
		busSelects = StringList.create(Person.SELECT_ID, Person.SELECT_FIRST_NAME, Person.SELECT_LAST_NAME, Person.SELECT_LOCATION_ID, Person.SELECT_EMAIL_ADDRESS);
		for (HashMap<String, String> detailsMap : (List<HashMap<String, String>>)FrameworkUtil.toMapList(BusinessObject.getSelectBusinessObjectData(context, resourceUIDs.keySet().toArray(new String[resourceUIDs.size()]), busSelects)))
		{
			resourceElement = AddElements(resourcesElement, "Resource");
			workCalendar = null != (locationId = detailsMap.get(Person.SELECT_LOCATION_ID)) ? WorkCalendar.getCalendar(context, locationId) : null;

			AddElements(resourceElement, "ResourceUID", resourceUIDs.get(detailsMap.get(Person.SELECT_ID)));
			AddElements(resourceElement, "Name", String.format("%s %s", detailsMap.get(Person.SELECT_LAST_NAME), detailsMap.get(Person.SELECT_FIRST_NAME)), "EmailAddress", detailsMap.get(Person.SELECT_EMAIL_ADDRESS));
			if (null != workCalendar){ AddElements(resourceElement, "Calendar", workCalendar.getId(context), "CalendarName", workCalendar.getName(context)); }
		}
	}

	private void AddTaskDependencies(Element taskElement, Map taskDetailsMap, boolean forPredecessor,  DecimalFormat unitsFormat)
	{
		Object listPreds = taskDetailsMap.get(forPredecessor ? task.SELECT_PREDECESSOR_IDS : SELECT_SUCCESSOR_IDS);
		Object listTypes = taskDetailsMap.get(forPredecessor ? task.SELECT_PREDECESSOR_TYPES : task.SELECT_SUCCESSOR_TYPES);
		Object listPredsLagTime = taskDetailsMap.get(forPredecessor ? task.SELECT_PREDECESSOR_LAG_TIMES : task.SELECT_SUCCESSOR_LAG_TIMES);

		// create predecessor tags
		if (listPreds instanceof String) 
		{
			AddTaskPredecessor(taskElement, (String) listPreds, forPredecessor, (String) listPredsLagTime, (String) listTypes, unitsFormat);
		}
		else if (listPreds instanceof StringList) 
		{
			StringList sl = (StringList) listPreds;
			StringList st = (StringList) listTypes;
			StringList sLag = (StringList) listPredsLagTime;

			for (int k = 0; k < sl.size(); k++) 
			{
				AddTaskPredecessor(taskElement, (String) sl.elementAt(k), forPredecessor, (String) sLag.elementAt(k), (String) st.elementAt(k), unitsFormat);
			} 
		}
	}
	
	private void AddTaskPredecessor(Element taskElement, String id, boolean forPredecessor, String lagTime, String predType, DecimalFormat unitsFormat)
	{
		String predecessorUID = taskUIDMap.get(id);
		if (predecessorUID == null) { return; }
		
		Element linkElement = AddElements(taskElement, "Dependency");
		AddElements(linkElement, "LinkId", id, "Link", predecessorUID, "IsPredecessor", forPredecessor ? "True" : "False", "Type", predType, "Lag", unitsFormat.format(Double.parseDouble(lagTime)));
	}
	
	/* Private Methods - Validate */

	public Element CreateValidateResourceResponse(ArrayList resourceNamesList, String result) throws Exception 
	{
		Element responseElement = new Element("transaction");
		responseElement.setAttribute("result", result);

		for (String name : (List<String>)resourceNamesList) 
		{
			AddElements(responseElement, "Resource", name);
		}

		return responseElement;
	}
	
	/* Private Methods - Merge */
	
	private String ExecuteMergeProject(Element projectElement, boolean isRootProject, Map<String, String> createdIDs, String userLanguage, String deletedTasks, String deletedProjects, ArrayList<String> invalidResources, boolean parentExists) throws Exception
	{
		if (isRootProject) 
		{ 
			// Check and update Resources
			if (0 != ValidateResource(projectElement, invalidResources).size()) { return null; }

			// Create Project
			createdIDs = new HashMap<String, String>(); 
		}

		String projectId = GetElementValue(projectElement, "BusinessID");
		boolean existingProject = IsExistingProject(projectId, true, userLanguage);
		
		if (existingProject)
		{
			project.setId(projectId);
		}
		else
		{
			String uid = projectElement.getChild("UID").getText();
			String type = GetElementValue(projectElement, "NodeType");
			if (null == type || type.isEmpty() || !IsProject(type)) { type = DomainConstants.TYPE_PROJECT_SPACE; }
			project.create(context, type, GetElementValue(projectElement, "Name"), DomainConstants.TYPE_PROJECT_SPACE, context.getVault().getName());
		
			// Set currency to project from settings
			String preferredCurrency = PersonUtil.getCurrency(context);
			if (preferredCurrency.isEmpty() || preferredCurrency.equals("As Entered") || preferredCurrency.equals("Unassigned")) { preferredCurrency = "Dollar"; }
			if (preferredCurrency != null) { project.setAttributeValue(context, ATTRIBUTE_CURRENCY, preferredCurrency); }
		
			createdIDs.put(uid, (projectId = project.getId(context)));
		}

		/* Read Project */

		project.open(context);

		if (isRootProject)
		{
			projectSchduleBasedOn = GetElementValue(projectElement, "ScheduleBasedOn"); 
			projectVisibility = GetElementValue(projectElement, "Visibility");

			String calendar = GetElementValue(projectElement, "Calendar");
			if (null != calendar && !calendar.isEmpty()) 
			{
				project.addCalendars(context, new StringList(calendar.concat("|DefaultCalendar"))); 
			}
		}
			
		String name = GetElementValue(projectElement, "Name");
		if (null != name && !name.isEmpty()) { project.setName(context, name); }

		HashMap attributes = new HashMap();
			
		if(!existingProject) // ACE2: Why ?
		{
			if(projectVisibility != null) {
				attributes.put(ATTRIBUTE_PROJECT_VISIBILITY, projectVisibility);
			}
			String percentComplete = GetElementValue(projectElement, "PercentComplete");
			if (percentComplete != null && !percentComplete.equals("0")) { attributes.put(ATTRIBUTE_PERCENT_COMPLETE, percentComplete); }
				
			String estDuration = GetElementValue(projectElement, "Duration");
			if (estDuration != null && estDuration.length() > 0) { attributes.put(ATTRIBUTE_TASK_ESTIMATED_DURATION, estDuration); }

			String actDuration = GetElementValue(projectElement, "ActualDuration");
			if (actDuration != null && actDuration.length() > 0) { attributes.put(ATTRIBUTE_TASK_ACTUAL_DURATION, actDuration); }
		}

		Map paramMap = new HashMap();
		paramMap.put("ObjectId", projectId);
		for(Map settings : projectUpdateColAttrMap.values()) 
		{
			paramMap.put("UpdatedValue", GetElementValue(projectElement, (String)settings.get("Name")));
			JPO.invoke(context, (String)settings.get("Update Program"), null, (String)settings.get("Update Function"), JPO.packArgs(paramMap));
		}		

		for(Map settings : projectColAttrMap.values()) 
		{
			String columnName = (String)settings.get("Name");
			String columnValue = GetElementValue(projectElement, columnName);

			/* NOTE */ if (null == columnValue || (!existingProject && columnValue.isEmpty()) || (existingProject ? basicfieldsExisting : basicfields).contains(columnName) || "true".equals(settings.get("MPIIgnoreForServer")) || columnName.equals("WBS") || columnName.equals("OutlineNumber"))
			{ 
				continue; 
			}
			
			String columnType = (String)settings.get("MPIColumnType");
			if (null != columnType && "user".equals(columnType.toLowerCase())) 
			{ 
				String personName = GetUserFromLastNameFirstName(columnValue, null, null); 
				 	
				if(personName != null && !personName.isEmpty()) { columnValue = personName; }
			}

			if(columnName.equals("ScheduleFrom")) 
			{
				//attributes.put(ATTRIBUTE_TASK_CONSTRAINT_DATE, GetElementValue(projectElement, columnValue.equals("True") ? "ProjectStart" : "ProjectFinish"));
				/* NOTE */attributes.put(ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, columnValue.equals("True") ? "As Soon As Possible" : "As Late As Possible");

				columnValue = columnValue.equals("True") ? "Project Start Date" : "Project Finish Date";
			}

			if ("Description".equals(columnName))
			{
				project.setDescription(context, columnValue);
			}
			else
			{
				String selectable =  (String)settings.get("BusinessExpression");

				attributes.put(!selectable.contains("attribute") ? selectable : selectable.substring(selectable.indexOf("[") + 1, selectable.indexOf("]")), columnValue);
			}
		}
		
			project.setAttributeValues(context, attributes);
		project.close(context);
			
		if (existingProject)			
		{
			// Delete tasks
			MergeDelete(deletedTasks, false);

			// Disconnect sub project
			MergeDelete(deletedProjects, true);
		}
		
		// Create Tasks
		boolean summary = !parentExists && !existingProject; // No Skip summary for existing project structures
		for (Element taskElement : GetProjectTasks(projectElement))
		{
			if (summary) { summary = false; continue; }
			
			Element subProjectElement = taskElement.getChild("Project");
			String taskType = taskElement.getChild("NodeType").getText();

			if (null == subProjectElement && IsProject(taskType)) { subProjectElement = taskElement; }

			if (null != subProjectElement)
			{
				String subProjectID = ExecuteMergeProject(subProjectElement, false, createdIDs, userLanguage, null, null, null, parentExists || existingProject);

				// Get BusinessID and translate if UID
				String parentID = GetElementValue(taskElement, "ParentBusId");

				if (null != parentID)
				{
					if (createdIDs.containsKey(parentID)) { parentID = createdIDs.get(parentID); }

					try
					{
						busSelects.clear();
						busSelects.add(SELECT_TO_RELATIONSHIP_SUBTASK_ID);

						subProject.setId(subProjectID);
						String connectionId = (String) subProject.getInfo(context, busSelects).get(SELECT_TO_RELATIONSHIP_SUBTASK_ID);
						if(ProgramCentralUtil.isNotNullString(connectionId))
						{
							DomainRelationship.disconnect(context, connectionId);
						}
							
						task.setId(parentID);
						task.addExistingForMSProject(context, subProjectID, parentID, relSubTask);
					}
					catch(Exception invalid)
					{
						// ACE2: Fix - DO NOT SEND PROJECT PARENT IFF NO DELTA
					}
				}
			}
			else
			{
				ExecuteMergeTask(taskElement, createdIDs, GetDetaultTaskType(taskType), existingProject, isRootProject);
			}
		}
		
		SaveProject(projectId, isRootProject);
		
		return projectId;
	}
	
	private void ExecuteMergeTask(Element taskElement, Map<String, String> createdIDs, String taskType, boolean existingProject, boolean isRootProject) throws Exception
	{
		try
		{
			String taskId = GetElementValue(taskElement, "BusinessID");
		
			// Get BusinessID and translate if UID
			String parentID = GetElementValue(taskElement, "ParentBusId");
			if (createdIDs.containsKey(parentID)) { parentID = createdIDs.get(parentID); }
			
			boolean existingTask = null != taskId && !taskId.isEmpty();

			if (!existingTask)
			{
				if (null != parentID && !parentID.isEmpty())
				{
					parentTask.setId(parentID);

					String taskName = taskElement.getChild("Name").getText();

					String nextTaskID = GetElementValue(taskElement, "NextBusId");
					if (DomainConstants.EMPTY_STRING.equals(nextTaskID)) { nextTaskID = null; }

					String taskPolicy = defaultTypePolicyMap.get(taskType);
					if (null == taskPolicy || 0 == taskPolicy.length()) 
					{
						taskPolicy = task.getDefaultPolicy(context, taskType);
						defaultTypePolicyMap.put(taskType, taskPolicy);
					}
			
					sequenceChanged = true;
					task.create(context, taskType, taskName, taskPolicy, parentTask, nextTaskID);
			
					createdIDs.put(taskElement.getChild("UID").getText(), (taskId = task.getId()));
				}
			}
			else //existing task
			{
				//handle parent/level structure change
				if (parentID != null && parentID.contains("~"))
				{
					parentID = parentID.substring(1);
					task.setId(taskId);
					String connectionId = task.getInfo(context, SELECT_TO_RELATIONSHIP_SUBTASK_ID);
					DomainRelationship.disconnect(context, connectionId);

					task.setId(parentID);
					String nextTaskID = GetElementValue(taskElement, "NextBusId");
					task.addExistingForMSProject(context, taskId , nextTaskID, relSubTask);
				}

				task.setId(taskId);
			}
		
			if (existingProject && isRootProject)
			{
				String calendar = GetElementValue(taskElement, "Calendar");
				
				if (null == calendar || calendar.isEmpty())
				{
					task.removeCalendar(context);
				}
				else
				{
					task.addCalendar(context, calendar);
				} 
			}

			HashMap attributes = new HashMap();

			boolean updateConstrain = false;
			String selectable = null;
			for(Map settings : taskBusColAttrMap.values()) 
			{
				String columnName = (String)settings.get("Name");
				String columnValue = GetElementValue(taskElement, columnName);

				boolean skip = false; 
			
				if (existingProject) 
				{
					skip |= columnName.equals("ConstraintDate") || columnName.equals("ConstraintType") || columnName.equals("WBS");
					
					// skip to set actual data for estimated based scheduling project
					skip |= "Estimated".equals(projectSchduleBasedOn) && (columnName.equals(ATTRIBUTE_TASK_ACTUAL_START_DATE) || columnName.equals(ATTRIBUTE_TASK_ACTUAL_FINISH_DATE)) || columnName.equals(ATTRIBUTE_TASK_ACTUAL_DURATION);
				}
			
				String type = (String)settings.get("MPIColumnType");

				if ("user".equals(type) && null != columnValue) { columnValue = GetUserFromLastNameFirstName(columnValue, null, null); /* keep outside assign block, as null may be returned */ }
				
				if(null != columnValue && (existingTask || !columnValue.isEmpty()) && !skip && null != (selectable = (String)settings.get("BusinessExpression")))
				{
					// Basic attributes 
					if ("name".equals(selectable)) { skip = true; task.setName(context, columnValue); }
					if ("owner".equals(selectable)) { skip = true; task.setOwner(context, columnValue); }
					if ("description".equals(selectable)) { skip = true; task.setDescription(context, columnValue); }
					if ("type".equals(selectable)) { skip = true; if (!columnValue.isEmpty()) { task.updateTaskType(context, taskId, columnValue); } }
					if ("current".equals(selectable)) { skip = true; if (!columnValue.isEmpty()) { try { task.setState(context, columnValue); } catch(Exception ex) { /* IR-072641V6R2012 */ throw columnValue.equals(task.STATE_PROJECT_SPACE_COMPLETE) ? new Exception(ex.getMessage() + "\nNotice: Cannot set state " + columnValue + "; Please Take appropriate actions manually to Complete the Gate") : ex; } } }

					if (skip) { continue; } // Basic attributes

					if (selectable.contains("attribute")) { selectable = selectable.substring(selectable.indexOf("[") + 1, selectable.indexOf("]")); }

					if (scheduleFieldsInSave.contains(columnName) && ("duration".equals(type) || "date".equals(type)))
					{
						//skip duration modification if its summary task
						if (GetElementValue(taskElement, "Summary").equals("True")) { continue; }
						
						updateConstrain = true;
					}
				
					attributes.put(selectable, columnValue);
				}
			}

			if (!existingProject || updateConstrain)
			{
				String mspTaskConstraintType = GetElementValue(taskElement, "ConstraintType");
				String mspTaskConstraintDate = GetElementValue(taskElement, "ConstraintDate");

				//start and finish value is changed and no change in constraint type(when we add dependency), if we dont set then constraint type is changed
				if(null != mspTaskConstraintDate && !mspTaskConstraintDate.isEmpty()) 
				{ 
					attributes.put(ATTRIBUTE_TASK_CONSTRAINT_DATE, mspTaskConstraintDate);
				}
			
				attributes.put(ATTRIBUTE_TASK_CONSTRAINT_TYPE, mspTaskConstraintType);
			}

			task.setAttributeValues(context, attributes);
		
			Map paramMap = new HashMap();
			paramMap.put("ObjectId", taskId);
			for(Map settings : taskUpdateColAttrMap.values()) 
			{
				paramMap.put("UpdatedValue", GetElementValue(taskElement, (String)settings.get("Name")));
				JPO.invoke(context, (String)settings.get("Update Program"), null, (String)settings.get("Update Function"), JPO.packArgs(paramMap));
			}

			// Read Assignments
			List<Map> resourceMembership = null;
			for (Element resourceElement : (List<Element>) taskElement.getChild("Resources").getChildren("Resource"))
			{
				String units = resourceElement.getAttributeValue("Units");
				String resourceId = resourceElement.getAttributeValue("ResourceID");

				boolean removed = resourceId.contains("-");
				boolean modified = resourceId.contains("~");

				resourceId = userUIDBusinessIdMap.get((modified || removed) ? resourceId.substring(1) : resourceId);

				if (modified || removed) 
				{ 
					if (null == resourceMembership)
					{
						StringList busSelect = new StringList();
						StringList relSelect = new StringList();
						busSelect.add(Person.SELECT_ID);
						relSelect.add(AssignedTasksRelationship.SELECT_ID);
						resourceMembership = (List<Map>)task.getAssignees(context, busSelect, relSelect, null);
					}

					// Remove membership of user to task
					for (Map membership : resourceMembership) 
					{
						if (membership.get(Person.SELECT_ID).equals(resourceId)) 
						{
							task.removeAssignee(context, (String) membership.get(AssignedTasksRelationship.SELECT_ID));

							break;
						}
					}
				}

				if (modified || !removed) { task.addAssignee(context, resourceId, null, units); }
			}

			// Read Dependencies
			for (Element dependencyElement : (List<Element>) taskElement.getChild("Dependencies").getChildren("Dependency"))
			{
				String lag = dependencyElement.getAttributeValue("Lag");
				String type = dependencyElement.getAttributeValue("Type");
				String link = dependencyElement.getAttributeValue("Link");
				String linkId = dependencyElement.getAttributeValue("LinkId");
				boolean isPredecessor = !"False".equals(dependencyElement.getAttributeValue("IsPredecessor")); // True default

				if ((null == linkId || linkId.isEmpty()) && createdIDs.containsKey(link)) { linkId = createdIDs.get(link); }
			
				boolean removed = linkId.contains("-");
				boolean modified = linkId.contains("~");

				linkId = (modified || removed) ? linkId.substring(1) : linkId;

				task.setId(isPredecessor ? taskId : linkId);

				if (modified || removed) 
				{ 
					busSelects.clear();
					busSelects.add(task.SELECT_ID);
					StringList relSelects = new StringList();
					relSelects.add(DependencyRelationship.SELECT_DEPENDENCY_TYPE);
					List<Map> dependencyList = (List<Map>)task.getPredecessors(context, busSelects, relSelects, null);

					// Remove link of dependency to task
					for (Map dependencyLink : dependencyList) 
					{
						if (dependencyLink.get(task.SELECT_ID).equals(isPredecessor ? linkId : taskId)) 
						{
							task.removePredecessors(context, new String[] { (String) dependencyLink.get(DependencyRelationship.SELECT_ID) }, false);
						
							break;
						}
					}
				}

				if (!removed) 
				{  
					HashMap predecessorMap = dependenciesMap.get(isPredecessor ? taskId : linkId);

					if (null == predecessorMap) { dependenciesMap.put(isPredecessor ? taskId : linkId, (predecessorMap = new HashMap())); }

					attributes = new HashMap();
					attributes.put(DependencyRelationship.ATTRIBUTE_LAG_TIME, lag);
					attributes.put(DependencyRelationship.ATTRIBUTE_DEPENDENCY_TYPE, type);
			
					predecessorMap.put(isPredecessor ? linkId : taskId, attributes);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			throw new MatrixException("Error in saving task " + GetElementValue(taskElement, "Name") + " (" + GetElementValue(taskElement, "UID") + "): " + e.toString()); 
		}
	}
	
	private void MergeDelete(String deletedTasks, boolean isProject) throws Exception
	{
		if (ProgramCentralUtil.isNotNullString(deletedTasks))
		{
			busSelects.clear();

			if (isProject)
			{
				busSelects.add("to[" + RELATIONSHIP_SUBTASK + "].id");
			}
			else
			{
				busSelects.add(DomainConstants.SELECT_ID);
				busSelects.add(DomainConstants.SELECT_NAME);
				busSelects.add(DomainConstants.SELECT_CURRENT);
			}

			String[] deletionParents = deletedTasks.split("\\|"), deletedIdsWithParent, deletedIds;
			for (int i = 0; i < deletionParents.length; i++)
			{
				sequenceChanged = true;
				deletedIdsWithParent = deletionParents[i].split(",");
				deletedIds = new String[deletedIdsWithParent.length - 1];

				for(int j = 1; j < deletedIdsWithParent.length; j++)
				{
					deletedIds[j - 1] = deletedIdsWithParent[j];
				}

				DeleteTasks(deletedIds, deletedIdsWithParent[0], busSelects, isProject);
			}
		}
	}
	
	/**
	 * Returns the modified task Attribute Selectables List based upon the typeName filter
	 *
	 * @param taskBusSelect To store bus selects
	 * @param taskRelSelect To store relationships selects
	 */
	private void GetTypeSpecificTaskSelectables(String typeName, String isExperiment, StringList taskBusSelect, StringList taskRelSelect) throws Exception
	{
		StringList filter = (StringList)JPO.invoke(context, "MSFUtil", null, "GetTypeSpecificAttributeList", new String[] {typeName, isExperiment}, StringList.class);

                taskBusSelect.addAll(taskSelectable);
		taskRelSelect.addAll(taskRelSelectable);

		if (null == filter) { return;}

		for(Object name : filter)
		{
			String busSelectExpr = (String)taskColNameBusAttrMap.get(name);
			if(busSelectExpr != null)
			{
				taskBusSelect.removeElement(busSelectExpr);
				continue;
			}
			
			String relSelectExpr = (String)taskColNameRelAttrMap.get(name);
			if(relSelectExpr != null)
			{
				taskRelSelect.removeElement(relSelectExpr);
			}
		}
	}
	
	private void SaveProject(String projectId, boolean isRootProject) throws Exception
	{
		if (isRootProject)
		{
			if (!dependenciesMap.isEmpty())
			{
				for (String key : dependenciesMap.keySet())
				{
					task.setId(key);
					task.addPredecessors(context, dependenciesMap.get(key), false);
				}
			}
		}

		task = (Task) DomainObject.newInstance(context, TYPE_TASK, DomainConstants.PROGRAM); 
		task.setId(projectId);
		if(sequenceChanged) { task.reSequence(context, projectId); }

		task.rollupAndSave(context);
	}
	
	/**
	 * Deletes the objects specified by the ids.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectIds the ids of the objects to delete
	 */
	private void DeleteTasks(String[] removedList, String parentId, StringList busSelects /* passed to ensure init */, boolean isProject) throws Exception 
	{
		String[] taskIds = isProject ? null : new String[removedList.length];

		// Delete lowest task first
		for(int i = 0; i < removedList.length; i++)
		{
			String lastId = removedList[removedList.length - 1 - i];

			if (isProject)
			{
				subProject.setId(lastId);
				DomainRelationship.disconnect(context, (String) subProject.getInfo(context, busSelects).get("to[" + RELATIONSHIP_SUBTASK + "].id"));
			}
			else
			{
				taskIds[i] = lastId;
			}
		}

		if (isProject) { return; }

		StringList taskInCreate = new StringList();
		StringList taskInNonCreate = new StringList();
		for (Map taskDetailsMap : (List<Map>)DomainObject.getInfo(context, taskIds, busSelects))
		{
			String taskId = (String) taskDetailsMap.get(DomainConstants.SELECT_ID);

			if (!"Create".equalsIgnoreCase((String) taskDetailsMap.get(DomainObject.SELECT_CURRENT)) &&
			    !"Assign".equalsIgnoreCase((String) taskDetailsMap.get(DomainObject.SELECT_CURRENT))) {
				taskInNonCreate.add((String) taskDetailsMap.get(DomainConstants.SELECT_NAME));
			} else {
				taskInCreate.add(taskId);
			}
		}

		if (taskInNonCreate.size() > 0) 
		{
			String errorMessage = "Unable to delete listed task(s). Task is beyond Create state: \n [";
			for (int k = 0; k < taskInNonCreate.size(); k++) 
			{
				errorMessage += taskInNonCreate.get(k);
				if (k <= taskInNonCreate.size() - 2) { errorMessage += ","; }
			}
			errorMessage += "]";
			throw new Exception(errorMessage);
		}

		if (taskInCreate.size() > 0) 
		{
			task.setId(parentId);
			task.delete(context, (String[]) taskInCreate.toArray(new String[taskInCreate.size()]));
		}
	}
	
	/* Private Methods - Utility */
	
	/*
	 * Get child element value by name
	 */
	private String GetElementValue(Element element, String name) 
	{
		Element child = element.getChild(name); return null == child ? null : child.getText(); 
	}
	
	/*
	 * Add child Elements with {name, data}
	 */
	private Element AddElements(Element parent, String... nameData) 
	{
		Element element = null;
		for (int i = 0; i < nameData.length; i += 2)
		{
			element = new Element(nameData[i]);
			if (i + 1 < nameData.length) { element.setText(nameData[i + 1]); }
			parent.addContent(element);
		}

		return element;
	}
	
	private boolean IsProject(String type) throws Exception
	{
		return null != type && !type.isEmpty() && GetProjectSpaceSubTypes(context, null).contains(type);
	}
	
	public void IsExperiment(String sProjectType) throws Exception
	{
		if (null == experimentSubTypes) { experimentSubTypes = ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_EXPERIMENT); }

		isExperiment = experimentSubTypes.contains(sProjectType);
	}
	
	private boolean IsExistingProject(String projectId, boolean readOnly, String userLanguage) throws Exception
	{
		boolean existingProject = null != projectId && !projectId.isEmpty() && "true".equalsIgnoreCase(MqlUtil.mqlCommand(context, "print bus $1 select exists dump", /* true - isSuperUser required ?, */ projectId));
		if (readOnly && existingProject) 
		{
			project.setId(projectId); // Set project and determine edit access
			if (!project.checkAccess(context, (short) AccessConstants.cModify)) 
			{
				try { EnoviaResourceBundle.getProperty(context, "IntegrationFramework", "emxIEFDesignCenter.Common.NoModify", userLanguage); } catch (Exception ex) { throw new Exception("No modify access: " + ex.getMessage()); }
			}
		}

		return existingProject;
	}
	
	private List<Element> GetProjectTasks(Element project) 
	{  
		Element tasks = project.getChild("Tasks");

		return null == tasks ? new ArrayList<Element>() : (List<Element>)tasks.getChildren("Task");
	}
	
	private String GetDetaultTaskType(String taskType) throws Exception
	{
		if (null == taskType || 0 == taskType.length()) { return DomainConstants.TYPE_TASK; }
		
		if (null == taskSubTypes) { taskSubTypes = ProgramCentralUtil.getTaskSubTypesList(context); }

		return taskSubTypes.contains(taskType) ? taskType : DomainConstants.TYPE_TASK;
	}
	
	/* Private Methods - Format */
	
	private String SetDetailsAsPerFormat(String columnType, String content)
	{
		if(columnType.equals("date")) { return FormatAsProjectDate(content); }
		
		if(columnType.equals("user")) { return GetPersonLastNameFirstName(content); }
		
		if(columnType.equals("constraint")) { return GetConstraintConstantMap.get(content); }
		
		if(columnType.equals("percent")) { return Integer.toString(new Integer(Math.round(Float.valueOf(content)))); }

		return content;
	}
	
	private String FormatAsProjectDate(String dateContent)
	{
		return (null == dateContent || dateContent.isEmpty()) ? DomainConstants.EMPTY_STRING : dateFormat.format(MATRIX_DATE_FORMAT.parse(dateContent, new java.text.ParsePosition(0)));
	}
	
	/*
	 * Add and get valid userLastNameFirstName (provided in format "<last name> <first name>") in userNameIdMap
	 */
	private String GetUserFromLastNameFirstName(String userLastNameFirstName, String uid, ArrayList<String> invalidResources) throws Exception 
	{
		if (userLastNameFirstName.isEmpty()) { return null; }

		if (null == userNameIdMap.get(userLastNameFirstName) && userLastNameFirstName.indexOf(" ") != -1) 
		{
			busSelects.clear();
			busSelects.addElement(Person.SELECT_NAME);
			busSelects.addElement(Person.SELECT_FIRST_NAME);
			busSelects.addElement(Person.SELECT_LAST_NAME);
			busSelects.addElement("to[Member].from.type.kindof[Organization]"); // IR-016606V6R2012 fix
			
			String whereClause = Person.SELECT_FIRST_NAME + " match '*" + userLastNameFirstName.substring(userLastNameFirstName.lastIndexOf(" ")).trim() + "'" 
				+ " && " + Person.SELECT_LAST_NAME + " match '" + userLastNameFirstName.substring(0, userLastNameFirstName.indexOf(" ")) + "*" + "'";

			for (Map user : (List<Map>)findObjects(context, TYPE_PERSON, "*", "*", "*", "*" /* VaultUtil.getSearchVaultPattern(context) */, whereClause, false, busSelects)) 
			{
				if (userLastNameFirstName.equals((String) user.get(Person.SELECT_LAST_NAME) + " " + user.get(Person.SELECT_FIRST_NAME))) 
				{
					if (null != (String) user.get("to[Member].from.type.kindof[Organization]")) 
					{
						userNameIdMap.put(userLastNameFirstName, (String) user.get(Person.SELECT_NAME));
					}

					break;
				}
			}
		}

		String userName = userNameIdMap.get(userLastNameFirstName);

		if (null == userName || userName.isEmpty())
		{
			if (null != invalidResources) { invalidResources.add(userLastNameFirstName); }
		}
		else if (null != uid)
		{
			userUIDBusinessIdMap.put(uid, Person.getPerson(context, userName).getId(context));
		}

		return userName;
	}
	
	/**
	 * The values contained in the dependency maplist is MS Project uids these
	 * uids change with each operation This function converts the uid into PC
	 * taskIds
	 *
	 * TODO: ACE2 - Cache this data ?
	 *
	 * @param context the user context object for the current session
	 */
	private String GetPersonLastNameFirstName(String userName) {
		//get the last name "  " first name for the owner
		java.util.Set userList = new java.util.HashSet();
		userList.add(userName);

		StringList personSelect = new StringList();
		personSelect.add(Person.SELECT_FIRST_NAME);
		personSelect.add(Person.SELECT_LAST_NAME);
		
		try 
		{
			Map ownerMap = (Map) Person.getPersonsFromNames(context, userList, personSelect).get(userName);
			if (ownerMap != null) { return ownerMap.get(Person.SELECT_LAST_NAME) + " " + ownerMap.get(Person.SELECT_FIRST_NAME); }
		} 
		catch (Exception e) { e.printStackTrace(); }
			
		return null;
	}
	
	/** DEBUG * SUPPORT *********************************** */
	
	private boolean verbose = false, csvActive = false;
	private String debugOperation, csvLogs = DomainConstants.EMPTY_STRING, csvTriggerFunction = "##";
	private HashMap<String, Long> operationTime = new HashMap<String, Long>();
	private HashMap<String, Integer> operationCount = new HashMap<String, Integer>();
	private int csvLevel = 0, maxCscLevel = 3;

	private void DebugLog(String operation, boolean end, String message, boolean showTime)
	{
		if (!debug) { return; }
		
		Long timeNow = new Long(0);
		if (showTime || null != operation) { timeNow = new Date().getTime(); }
		
		if (null != operation) 
		{ 
			if (end)
			{
				// End
				operationTime.remove("End_" + operation);
				operationTime.put("End_" + operation, timeNow);
				
				// Total
				Long start = operationTime.get("Start_" + operation);
				Long total = operationTime.remove("Total_" + operation) + timeNow - start;
				operationTime.put("Total_" + operation, total);
				
				debugOperation = null;
				
				if (verbose)
				{
					System.out.println("## END OPERATION *******************\n## " + operation + "\n## End time: " + timeNow + "\n## Run time: " + total + "\n## *********************************\n\n");
				}

				if (csvActive)
				{
					if (csvLevel < maxCscLevel)
					{
						int seconds = (int)((timeNow - start)/1000);
						csvLogs += csvLevel + "," + operation + "," + seconds + "," + (total/1000) + "," + "End - " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(timeNow) + "\n";
					}
					csvLevel--;
				}
			}
			else
			{
				if (verbose)
				{
					System.out.println("\n\n## START OPERATION *****************\n## " + operation + "\n## Start time: " + timeNow + "\n## *********************************");
				}
				
				// Start
				operationTime.remove("Start_" + operation);
				operationTime.put("Start_" + operation, timeNow);
				
				// Sum
				if (!operationTime.containsKey("Total_" + operation)) { operationTime.put("Total_" + operation, new Long(0)); }
				if (!operationCount.containsKey(operation)) { operationCount.put(operation, new Integer(0)); }
				
				Integer total = operationCount.remove(operation);
				operationCount.put(operation, total + 1);
				
				debugOperation = operation; 

				if (!csvActive)
				{
					if (csvTriggerFunction.equals("##") || csvTriggerFunction.equals("##" + operation)) { csvActive = true; }
				}
				
				if (csvActive)
				{
					csvLevel++;
					if (csvLevel < maxCscLevel)
					{
						csvLogs += csvLevel + "," + operation + ",-,-," + "Start - " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(timeNow) + "\n";
					}
				}
			}
		}
		
		// Show debug messages
		if (verbose && showTime) { System.out.println("Time (millis): " + timeNow); }
		if (verbose && null != message) { System.out.println(null == debugOperation ? message : "##" + debugOperation + ": " + message); }
	}
	
	private void DebugReport(String parentOperation, boolean operationEnd, String endMessage)
	{
		if (!debug) { return; }
		if (operationEnd) { DebugLog(parentOperation, true, endMessage, false); }

		Long referenceTime = new Long(0);
		if (null != parentOperation) { referenceTime = operationTime.get("Total_" + parentOperation); }
		
		System.out.println("\n\n\n## DEBUG REPORT ********************\n## CSV *****************************\nDepth,Function,Seconds,Total,State" + "\n" + csvLogs + "\n## END CSV *************************");

		for (String key : operationTime.keySet()) {
		    
			if (key.startsWith("Total_"))
			{
				key = key.replace("Total_", DomainConstants.EMPTY_STRING);
				
				try 
				{
					System.out.println("##\n## Operation: " + key + "\n## Total run time: " + operationTime.get("Total_" + key) + "\n## Total calls: " + operationCount.get(key));
				
					if (null != parentOperation)
					{
						System.out.println("## Percent total run: " + (100 * operationTime.get("Total_" + key) / referenceTime) + "%");
					}
				
					System.out.println("## Last run time: " + (operationTime.get("End_" + key) - operationTime.get("Start_" + key)));
				}
				catch(Exception ex)
				{
					System.out.println("##\n## Incomplete Operation: " + key);
				}
			}
		}
		
		System.out.println("## END REPORT **********************\n\n\n");
	}
	
	/** END * DEBUG * SUPPORT ***************************** */
}

