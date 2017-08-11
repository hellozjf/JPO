/*   emxWhatIfBase
 **
 **   Copyright (c) 2003-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **   This JPO contains the implementation of emxWorkCalendar
 **
 **   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.9.2.2 Thu Dec  4 07:55:10 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9.2.1 Thu Dec  4 01:53:19 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9 Wed Oct 22 15:49:37 2008 przemek Experimental przemek $
 */


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import matrix.db.AccessConstants;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.AssignedTasksRelationship;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Issue;
import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.ProjectManagement;
import com.matrixone.apps.common.SubtaskRelationship;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.Experiment;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;

/**
 * The <code>emxWhatIfBase</code> class contains methods for Experiment.
 *
 * @version PMC 10.5.1.2 - Copyright(c) 2013, MatrixOne, Inc.
 */

public class emxWhatIfBase_mxJPO extends DomainObject
{
	private final static String SELECT_SUBTASK_ATTRIBUTE_SEQUENCE_ORDER = "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_SEQUENCE_ORDER+"]";
	private final static String SELECT_PROJECT_ACCESS_KEY_ID ="to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";
	private final static String SELECT_PREDECESSOR_LAG_TIME_INPUT = "from[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ProgramCentralConstants.ATTRIBUTE_LAG_TIME + "].inputvalue";
	private final static String SELECT_PREDECESSOR_LAG_TIME_UNITS = "from[" + RELATIONSHIP_DEPENDENCY + "].attribute[" + ProgramCentralConstants.ATTRIBUTE_LAG_TIME + "].inputunit";
	private final static String SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR = "from[" + RELATIONSHIP_DEPENDENCY + "].to." + SELECT_PROJECT_ACCESS_KEY_ID;
	private final static String SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER = "from[" + RELATIONSHIP_DEPENDENCY + "].to." + SELECT_SUBTASK_ATTRIBUTE_SEQUENCE_ORDER;
	private final static String SELECT_PARENT_TASK_ID = "to["+ProgramCentralConstants.RELATIONSHIP_EXPERIMENT+"].from.id";
	private final static String SELECT_ASSIGNEE_NAMES = "to["+DomainObject.RELATIONSHIP_ASSIGNED_TASKS+"].from.name";
	private final static String SELECT_ASSIGNEE_IDS = "to["+DomainObject.RELATIONSHIP_ASSIGNED_TASKS+"].from.id";
	private final static String SELECT_ASSIGNEE_FIRSTNAME = "to["+DomainObject.RELATIONSHIP_ASSIGNED_TASKS+"].from."+Person.SELECT_FIRST_NAME;
	private final static String SELECT_ASSIGNEE_LASTNAME = "to["+DomainObject.RELATIONSHIP_ASSIGNED_TASKS+"].from."+Person.SELECT_LAST_NAME;
	private final static String SELECT_PERCENT_ALLOCATION = "attribute["+ProgramCentralConstants.ATTRIBUTE_PERCENT_ALLOCATION+"].value";
	private final static String ATTRIBUTE_ASSIGNED_DATE=(String)PropertyUtil.getSchemaProperty("attribute_AssignedDate");
	private final static String SELECT_ASSIGNED_DATE = "attribute["+ATTRIBUTE_ASSIGNED_DATE+"].value";
	private final static String SELECT_ASSIGNEE_ROLE = "attribute["+AssignedTasksRelationship.ATTRIBUTE_ASSIGNEE_ROLE+"].value";
	private final static String SELECT_PARENTOBJECT_KINDOF_EXPERIMENT_PROJECT = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+ProgramCentralConstants.TYPE_EXPERIMENT+"]";
	private final static String SELECT_TASK_PROJECT_MEMBER_ID = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.from["+DomainRelationship.RELATIONSHIP_MEMBER+"].to.id";
	private final static String SELECT_TASK_ASSIGNEE_ID = "to[Assigned Tasks].from.id";
	private final static String SELECT_TASK_PROJECT_VISIBILITY = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to."+ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY;
	private final static String ATTRIBUTE_ESTIMATED_START_DATE = PropertyUtil.getSchemaProperty("attribute_EstimatedStartDate");
	private final static String SELECT_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_ESTIMATED_START_DATE + "]";
	private final static String ATTRIBUTE_ESTIMATED_END_DATE = PropertyUtil.getSchemaProperty("attribute_EstimatedEndDate");
	private final static String SELECT_ESTIMATED_END_DATE = "attribute[" + ATTRIBUTE_ESTIMATED_END_DATE + "]";
	private final static String ATTRIBUTE_COMMENTS = PropertyUtil.getSchemaProperty("attribute_Comments");
	private final static String SELECT_ROUTE_TASK_APPROVAL_REJECTION_COMMENTS = "from["+DomainObject.RELATIONSHIP_OBJECT_ROUTE+"].to.to["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].from.attribute["+ATTRIBUTE_COMMENTS+"].value";
	private static final String SELECT_TASK_PROJECT_ID = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
	private static final String SELECT_CORRESPONDING_PROJECT_TASK_ID = "to["+ProgramCentralConstants.RELATIONSHIP_EXPERIMENT +"].from.id";
	private static final String SELECT_CORRESPONDING_EXPERIMENT_TASK_ID = "from["+ProgramCentralConstants.RELATIONSHIP_EXPERIMENT +"].to.id";

	private int COUNT=1;
	private String LEVEL="1";
	private final static String DUPLICATE_ID = "dupId";
	private final static String EXPAND_MULTI_LEVELS_JPO = "expandMultiLevelsJPO";
	private final static String PARENT_TASK_ID = "TaskId";
	protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null;
	public final static String SELECT_HAS_SUBTASK =  "from[" + RELATIONSHIP_SUBTASK + "]";

	Experiment experiment = new Experiment();

	public emxWhatIfBase_mxJPO (Context context, String[] args)	throws Exception{
		// Call the super constructor
		super();
		if (args != null && args.length > 0){
			setId(args[0]);
		}
	}

	/**
	 * Gets code to link WBS of experiments.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return List of WBS link.
	 * @throws MatrixException if operation fails.
	 */
	public StringList getExperimentWBS(Context context, String[] args) throws MatrixException
	{
		StringList experimentWBSList = new StringList();
		try{
			Map programMap =  JPO.unpackArgs(args);
			Map paramList = (Map) programMap.get("paramList");
			String strProjectId = (String) paramList.get("parentOID");
			MapList objectList = (MapList) programMap.get("objectList");

			for (int i = 0; i < objectList.size(); i++){
				Map mpObjDetails = (Map) objectList.get(i);
				String strTaskId = (String) mpObjDetails.get(SELECT_ID);
				String sbLinkMaker = EMPTY_STRING ;
				float parent = 0;
				float child = 0;
				try {
					if(ProgramCentralUtil.isNotNullString(strProjectId)){
						DomainObject dmoParent = DomainObject.newInstance(context,strProjectId);
						parent = Float.parseFloat(dmoParent.getAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION));
					}
					if(ProgramCentralUtil.isNotNullString(strTaskId)){
						DomainObject dmoChild = DomainObject.newInstance(context,strTaskId);
						child = Float.parseFloat(dmoChild.getAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION));
					}
					DecimalFormat decim = new DecimalFormat("#.##");
					sbLinkMaker += Task.parseToDouble(decim.format(parent-child));
				} catch (Exception e) {
					e.printStackTrace();
				}
				experimentWBSList.addElement(sbLinkMaker);
			}
			return experimentWBSList;

		}catch (Exception ex){
			ex.printStackTrace();
			throw new MatrixException(ex);
		}
	}

	/**
	 * get task name for Experiment compare view. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return task name list.
	 * @throws Exception, if operation fails.
	 */
	public StringList getNameColumn(Context context,String[]args)throws Exception
	{
		try{
			StringList selectable = new StringList();
			Map programMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			StringList taskNameList = new StringList(objectList.size());

			String []taskIdArray = new String[objectList.size()];
			for(int i=0;i<objectList.size();i++){
				Map objectMap = (Map)objectList.get(i);
				String taskId = (String) objectMap.get(DomainObject.SELECT_ID);
				taskIdArray[i] = taskId;
			}

			selectable.addElement(DomainObject.SELECT_NAME);
			selectable.addElement(DomainObject.SELECT_ID);
			MapList taskNameMapList = DomainObject.getInfo(context, taskIdArray, selectable);

			for(int i=0;i<taskNameMapList.size();i++){
				Map <String,String>taskMap = (Map)taskNameMapList.get(i);
				String taskName = taskMap.get(DomainObject.SELECT_NAME);
				String taskId = taskMap.get(DomainObject.SELECT_ID);

				String strURL = "../programcentral/emxProgramCentralWhatIfAnalysis.jsp?mode=launchWBS&amp;objectid="+XSSUtil.encodeForURL(context,taskId);
				StringBuilder sbLink = new StringBuilder("<a target='listHidden' href='");
				sbLink.append(strURL);
				sbLink.append("' class='object'>");
				sbLink.append(taskName);
				sbLink.append("</a>");
				taskNameList.addElement(sbLink.toString());
			}

			return taskNameList;
		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * gets the Task Dependency details for dependency column.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return dependency detail list.
	 * @throws Exception, if operation fails.
	 */
	public StringList getTaskDependency(Context context,String[]args)throws Exception
	{
		long start = System.currentTimeMillis();
		try{
			Map programMap = JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");

			int objectListSize = objectList.size();

			String[] taskIds = new String[objectListSize];
			for (int i=0; i < objectListSize; i++){
				Map map = (Map) objectList.get(i);
				String taskId = (String) map.get(DomainObject.SELECT_ID);
				taskIds[i] = taskId;
			}

			StringList selectables = new StringList(8);
			selectables.addElement(Task.SELECT_PREDECESSOR_TYPES);
			selectables.addElement(SELECT_PREDECESSOR_LAG_TIME_INPUT);
			selectables.addElement(SELECT_PREDECESSOR_LAG_TIME_UNITS);
			selectables.addElement(Task.SELECT_PREDECESSOR_IDS);
			selectables.addElement(ProgramCentralConstants.SELECT_PROJECT_ACCESS_KEY_ID);
			selectables.addElement(SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR);
			selectables.addElement(SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER);
			selectables.addElement(Experiment.SELECT_PREDECESSOR_SID);

			BusinessObjectWithSelectList bwsl = null;
			try{
				ProgramCentralUtil.pushUserContext(context);
				bwsl = BusinessObject.getSelectBusinessObjectData(context, taskIds, selectables);
			}finally{
				ProgramCentralUtil.popUserContext(context);
			}

			StringList dependencyList 		= new StringList(objectListSize);
			Map<String,String> palMapCache 	= new HashMap<String,String>();

			StringBuilder value 	= new StringBuilder();
			StringBuilder actValue 	= new StringBuilder();

			StringList predecessorTypes = new StringList();
			StringList predecessorLagTimes = new StringList();
			StringList predecessorLagTimeUnits = new StringList();
			StringList predecessorIds = new StringList();
			StringList projectAccessKeyIds = new StringList();
			StringList predecessorSIDList 		= new StringList();

			StringList projectAccessKeyIdsForPredecessor = new StringList();
			StringList predecessorTaskSequenceOrders = new StringList();

			int size = bwsl.size();

			for(int j = 0; j < size; j++){
				value.setLength(0);
				actValue.setLength(0);

				BusinessObjectWithSelect bws = bwsl.getElement(j);

				predecessorIds = bws.getSelectDataList(Task.SELECT_PREDECESSOR_IDS);
				int predecessorSize = predecessorIds == null ? 0 :predecessorIds.size();

				if(predecessorSize > 0){
					predecessorTypes 		= bws.getSelectDataList(Task.SELECT_PREDECESSOR_TYPES);
					predecessorLagTimes 	= bws.getSelectDataList(SELECT_PREDECESSOR_LAG_TIME_INPUT);
					predecessorLagTimeUnits = bws.getSelectDataList(SELECT_PREDECESSOR_LAG_TIME_UNITS);
					predecessorSIDList 		= bws.getSelectDataList(Experiment.SELECT_PREDECESSOR_SID);
					predecessorTaskSequenceOrders = bws.getSelectDataList(SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER);

					projectAccessKeyIds 	= bws.getSelectDataList(SELECT_PROJECT_ACCESS_KEY_ID);
					projectAccessKeyIdsForPredecessor = bws.getSelectDataList(SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR);

					MapList depedencyList = new MapList();

					for (int i=0; i < predecessorSize; i++){
						Map<String,String> dependencyMap  = new HashMap<String,String>();

						String predecessorId = (String) predecessorIds.get(i);
						String predecessorType = (String) predecessorTypes.get(i);
						String predecessorLagTime = (String) predecessorLagTimes.get(i);
						String predecessorLagTimeUnit = (String) predecessorLagTimeUnits.get(i);
						String projectAccessKeyIdForPredecessor = (String) projectAccessKeyIdsForPredecessor.get(i);
						String predecessorTaskSequenceOrder = (String) predecessorTaskSequenceOrders.get(i);
						String predecessorSID = (String) predecessorSIDList.get(i);

						if(ProgramCentralUtil.isNullString(predecessorTaskSequenceOrder)){
							// this should not be the case as every task should have a seq order unless a depend is against a project.
							predecessorTaskSequenceOrder = "0";
						}

						dependencyMap.put("PredecessorId", predecessorId);
						dependencyMap.put("PredecessorType", predecessorType);
						dependencyMap.put("PredecessorLagType", predecessorLagTime);
						dependencyMap.put("PredecessorLagTypeUnit", predecessorLagTimeUnit);
						dependencyMap.put("ProjectAccessKeyIdPredecessor", projectAccessKeyIdForPredecessor);
						dependencyMap.put("PredecessorTaskSequenceId", predecessorTaskSequenceOrder);
						dependencyMap.put("predecessorSID", predecessorSID);

						depedencyList.add(dependencyMap);
					}

					depedencyList.sort("PredecessorTaskSequenceId", ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);

					for (int i=0; i < predecessorSize; i++){
						Map dependencyMap 				= (Map)depedencyList.get(i);

						String predecessorType = (String) dependencyMap.get("PredecessorType");
						String predecessorLagTime = (String) dependencyMap.get("PredecessorLagType");
						String predecessorLagTimeUnit = (String) dependencyMap.get("PredecessorLagTypeUnit");

						String predecessorSID 			= (String) dependencyMap.get("predecessorSID");
						String predecessorTaskSeqOrder 	= (String) dependencyMap.get("PredecessorTaskSequenceId");

						String projectAccessKeyId = (String) projectAccessKeyIds.get(0);
						String projectAccessKeyIdForPredecessor = (String) dependencyMap.get("ProjectAccessKeyIdPredecessor");

						String predecessorProjectType = (String) palMapCache.get(projectAccessKeyIdForPredecessor);

						if (predecessorProjectType == null){
							String sCommandStatement = "print bus $1 select $2 $3 dump $4";

							String output =  MqlUtil.mqlCommand(context, sCommandStatement,
									projectAccessKeyIdForPredecessor,
									"from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type","from[" +
											RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name", "|"); 

							StringList projectInfo = FrameworkUtil.split(output, "|");
							predecessorProjectType = (String) projectInfo.get(0);

							palMapCache.put(projectAccessKeyIdForPredecessor, predecessorProjectType);
							palMapCache.put("name:" + projectAccessKeyIdForPredecessor, (String)projectInfo.get(1));
						}

						String projectName = DomainObject.EMPTY_STRING;
						if (!projectAccessKeyId.equals(projectAccessKeyIdForPredecessor)){
							if("Experiment".equals(predecessorProjectType)){
								continue;
							}
							projectName = (String) palMapCache.get("name:" + projectAccessKeyIdForPredecessor);
						}

						if (i>0){ //In case multiple dependencies
							value.append(ProgramCentralConstants.COMMA);
							actValue.append(ProgramCentralConstants.COMMA);
						}

						StringBuilder toolTip 	= new StringBuilder();
						StringBuilder actual 	= new StringBuilder();

						if (ProgramCentralUtil.isNotNullString(projectName)){
							toolTip.append(projectName).append(":").append(predecessorTaskSeqOrder).append(":").append(predecessorType);
							actual.append(projectName).append(":").append(predecessorSID).append(":").append(predecessorType);
						}else{
							toolTip.append(predecessorTaskSeqOrder).append(":").append(predecessorType);
							actual.append(predecessorSID).append(":").append(predecessorType);
						}

						toolTip.append(Task.parseToDouble(predecessorLagTime) < 0 ?DomainObject.EMPTY_STRING : "+");
						toolTip.append(predecessorLagTime).append(" ").append(predecessorLagTimeUnit);

						actual.append(Task.parseToDouble(predecessorLagTime) < 0 ?DomainObject.EMPTY_STRING : "+");
						actual.append(predecessorLagTime).append(" ").append(predecessorLagTimeUnit);

						value.append(toolTip.toString());
						actValue.append(actual.toString());
					}
				}

				Map<String,String> dependencyMap = new HashMap<String, String>();
				dependencyMap.put("ActualValue", actValue.toString());
				dependencyMap.put("DisplayValue", value.toString());

				dependencyList.add(dependencyMap);
			}

			long end = System.currentTimeMillis();
			System.out.println("Total dependency:"+(end-start));

			return dependencyList;

		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * 
	 * @param bws
	 * @param selectable
	 * @param listObject
	 * @return
	 */
	static protected StringList getSelectableValues(Context context,BusinessObjectWithSelect bws,
			String selectable, StringList listObject){
		char cFieldSep = 0x07;
		String value = (String) bws.getSelectData(selectable);
		String[] temp = value.split(String.valueOf(cFieldSep));
		if (listObject == null)
			listObject = new StringList(1);
		listObject.clear();
		for(int i=0; i < temp.length ; i++){
			listObject.add(temp[i]);
		}
		return listObject;
	}

	/**
	 * update master project. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @throws Exception,If operation fails.
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public Map updateMasterProject(Context context,String[]args)throws Exception
	{
		try{
			String SELECT_CORRESPONDING_TASK_PARENT_EXPERIMENT_ID = "from["+ProgramCentralConstants.RELATIONSHIP_EXPERIMENT+"].to.to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
			String SELECT_CORRESPONDING_EXPERIMENT_TASK_ID = "from["+ProgramCentralConstants.RELATIONSHIP_EXPERIMENT+"].to.id";
			String SELECT_TASK_PROJECT_TYPE = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
			String IS_TASK_PARENT_KINDOF_EXPERIMENT = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type.kindof[" + ProgramCentralConstants.TYPE_EXPERIMENT+ "]";
			String SELECT_CORRESPONDING_TASK_OF_EXP_TASK_PARENT = "to[Subtask].from.to[Experiment].from.id";
			String SELECT_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT = "to[Subtask].from.from[Experiment].to.id";
			String SELECT_EXPERIMENT_ID_OF_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT = "to[Subtask].from.from[Experiment].to.to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";

			String CORRESPONDING_EXPERIMENT_ID = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.to[Experiment].from.from[Experiment].to.id";
			String CORRESPONDING_EXPERIMENT_NAME = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.to[Experiment].from.from[Experiment].to.name";
			Map <Object,Object>returnHashMap = new HashMap<Object,Object>(); 
			MapList mlItems=new MapList(); 

			Map programMap =  JPO.unpackArgs(args);
			Map mParamMap = (Map)programMap.get("paramMap");
			String objectId1 = (String)mParamMap.get("objectId1"); // Experiment Id
			String objectId2 = (String)mParamMap.get("objectId2"); // Experiment or Project Id

			String objectType1 = (String)mParamMap.get("objectType1");//Experiment
			String objectType2 = (String)mParamMap.get("objectType2");// Experiment or Project Space

			String fromExperimentId = EMPTY_STRING;
			String toExperimentId = EMPTY_STRING;

			String correspondingMasterProjectTaskId = EMPTY_STRING;
			String CORRESPONDING_EXPERIMENT_TASK_ID = EMPTY_STRING;
			String CORRESPONDING_EXPERIMENT_TASK_ID_PROJECT_ID = EMPTY_STRING;

			boolean isCompareExperiments = false;
			if(ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(objectType2)){
				SELECT_CORRESPONDING_TASK_OF_EXP_TASK_PARENT = "to[Subtask].from.to[Experiment].from.from[Experiment].to.id";
				SELECT_EXPERIMENT_ID_OF_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT = "to[Subtask].from.to[Experiment].from.from[Experiment].to.to[Project Access Key].from.from[Project Access List].to.id";
				CORRESPONDING_EXPERIMENT_TASK_ID = "to[Experiment].from.from[Experiment].to.id";
				CORRESPONDING_EXPERIMENT_TASK_ID_PROJECT_ID = "to[Experiment].from.from[Experiment].to."+ProgramCentralConstants.SELECT_TASK_PROJECT_ID;
				isCompareExperiments = true;
			}

			DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_EXPERIMENT_TASK_ID);
			DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_TASK_PARENT_EXPERIMENT_ID);
			DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT);
			DomainObject.MULTI_VALUE_LIST.add(SELECT_EXPERIMENT_ID_OF_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT);
			DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_TASK_OF_EXP_TASK_PARENT);
			DomainObject.MULTI_VALUE_LIST.add(CORRESPONDING_EXPERIMENT_ID);
			DomainObject.MULTI_VALUE_LIST.add(CORRESPONDING_EXPERIMENT_NAME);
			DomainObject.MULTI_VALUE_LIST.add(CORRESPONDING_EXPERIMENT_TASK_ID);
			DomainObject.MULTI_VALUE_LIST.add(CORRESPONDING_EXPERIMENT_TASK_ID_PROJECT_ID);

			//get sync data
			Element rootElement = (Element) programMap.get("contextData");
			String sParentOID = (String)rootElement.getAttributeValue("objectId");
			List lCElement     = rootElement.getChildren();
			boolean validRelId = false;

			if(lCElement != null){
				Iterator itrC  = lCElement.iterator();
				while(itrC.hasNext()){
					com.matrixone.jdom.Element childCElement = (com.matrixone.jdom.Element)itrC.next();
					String sObjectId = (String)childCElement.getAttributeValue("objectId");
					String sRelId = (String)childCElement.getAttributeValue("relId");
					String sRowId = (String)childCElement.getAttributeValue("rowId");
					String markup    = (String)childCElement.getAttributeValue("markup");
					String syncDir    = (String)childCElement.getAttributeValue("syncDir");

					if(isCompareExperiments && "right".equalsIgnoreCase(syncDir)){
						fromExperimentId = objectId1;
						toExperimentId = objectId2;
					} else if (isCompareExperiments && "left".equalsIgnoreCase(syncDir)){
						fromExperimentId = objectId2;
						toExperimentId = objectId1;
					}

					//Here validating relId is valid or invalid.
					try{
						String []relIds = new String[1];
						relIds[0] = sRelId; 
						StringList relationshipSelects = new StringList();
						relationshipSelects.addElement(DomainRelationship.SELECT_NAME);

						MapList relIdInfoList = DomainRelationship.getInfo(context, relIds, relationshipSelects);
						Map relInfoMap = (Map)relIdInfoList.get(0);
						String relName = (String)relInfoMap.get(DomainRelationship.SELECT_NAME);

						if(ProgramCentralUtil.isNotNullString(relName)){
							validRelId = true;
						}else{
							validRelId = false;
							markup = "cut";
						}
					}catch(FrameworkException exp){
						validRelId = false;
					}

					if(validRelId){

						if(ProgramCentralUtil.isNotNullString(sParentOID) && !markup.equals("cut")){
							StringList selectable = new StringList(14);
							selectable.addElement(SELECT_CORRESPONDING_TASK_OF_EXP_TASK_PARENT);
							selectable.addElement(SELECT_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT);
							selectable.addElement(IS_TASK_PARENT_KINDOF_EXPERIMENT);
							selectable.addElement(SELECT_EXPERIMENT_ID_OF_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT);
							selectable.addElement(DomainObject.SELECT_NAME);
							selectable.addElement("to[Subtask].from.type");
							selectable.addElement("to[Subtask].from.name");
							selectable.addElement("to[Experiment].from.id");
							selectable.addElement("to[Subtask].attribute[Task WBS]");
							selectable.addElement("to[Subtask].attribute[Sequence Order]");
							selectable.addElement(SELECT_TASK_PROJECT_ID);
							selectable.addElement("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.to[Experiment].from.from[Experiment].to.id");
							selectable.addElement(CORRESPONDING_EXPERIMENT_TASK_ID);
							selectable.addElement(CORRESPONDING_EXPERIMENT_TASK_ID_PROJECT_ID);
							selectable.addElement(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);

							String[]objectIds = new String[1];
							objectIds[0] = sObjectId;

							MapList synchedObjectInfoList = DomainObject.getInfo(context, objectIds, selectable);

							Map synchedObjectInfo = (Map)synchedObjectInfoList.get(0);
							String selectedTaskParentName = (String)synchedObjectInfo.get("to[Subtask].from.name");
							String selectedTaskParentType = (String)synchedObjectInfo.get("to[Subtask].from.type");
							String isParentExperimentProject = (String)synchedObjectInfo.get(IS_TASK_PARENT_KINDOF_EXPERIMENT);
							String isExperiment = (String)synchedObjectInfo.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
							if(ProgramCentralUtil.isNullString(isParentExperimentProject)){
								isParentExperimentProject = isExperiment;
							}


							Object currespondingTaskOfExpTaskParent = synchedObjectInfo.get(SELECT_CORRESPONDING_TASK_OF_EXP_TASK_PARENT);
							Object correspondingTaskOfProjectTaskParent = synchedObjectInfo.get(SELECT_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT);
							Object projectTaskDupParentIdList = synchedObjectInfo.get(SELECT_EXPERIMENT_ID_OF_CORRESPONDING_TASK_OF_PROJECT_TASK_PARENT);
							String objectName = (String)synchedObjectInfo.get(DomainObject.SELECT_NAME);
							String selectedTaskProjectId = (String)synchedObjectInfo.get(SELECT_TASK_PROJECT_ID);
							Object correspondingExperimentIdList = synchedObjectInfo.get(CORRESPONDING_EXPERIMENT_ID);
							Object correspondingExperimentNameList = synchedObjectInfo.get(CORRESPONDING_EXPERIMENT_NAME);
							correspondingMasterProjectTaskId = (String)synchedObjectInfo.get("to[Experiment].from.id");
							Object correspondingExperimentTaskId = synchedObjectInfo.get(CORRESPONDING_EXPERIMENT_TASK_ID);
							Object correspondingExperimentTaskIdProjectId = synchedObjectInfo.get(CORRESPONDING_EXPERIMENT_TASK_ID_PROJECT_ID);

							MapList toExperimentTaskList = new MapList();
							MapList toSubExperimentList = new MapList();
							if(isCompareExperiments && !fromExperimentId.equals(selectedTaskProjectId)){
								StringList busSelects = new StringList(SELECT_ID);
								busSelects.add(SELECT_NAME);
								busSelects.add(SELECT_TYPE);
								busSelects.add("to[Subtask].from.id");
								busSelects.addElement(DomainObject.SELECT_LEVEL);
								StringList relationshipSelects = new StringList(2);
								relationshipSelects.add(SubtaskRelationship.SELECT_TASK_WBS);
								relationshipSelects.add(SubtaskRelationship.SELECT_SEQUENCE_ORDER);

								DomainObject toObject = DomainObject.newInstance(context,toExperimentId);
								toExperimentTaskList = toObject.getRelatedObjects(
										context,
										RELATIONSHIP_SUBTASK,
										ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT,
										busSelects,
										new StringList(),
										false,
										true,
										(short)0,
										DomainConstants.EMPTY_STRING,
										DomainConstants.EMPTY_STRING,
										0);


								Iterator toExperimentTaskListIterator = toExperimentTaskList.iterator();

								while(toExperimentTaskListIterator.hasNext()){
									Map objectinfo = (Map)toExperimentTaskListIterator.next();
									String objectType = (String)objectinfo.get(SELECT_TYPE);


									if(ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(objectType)){
										toSubExperimentList.add(objectinfo);
									}
								}

							}


							if("TRUE".equalsIgnoreCase(isParentExperimentProject)){

								if(!isCompareExperiments){
									sParentOID = (String)((StringList)currespondingTaskOfExpTaskParent).get(0);
								} 
								else {

									boolean parentTaskExistsInMasterProject = false;
									String targetExperimentId = EMPTY_STRING;
									if("True".equalsIgnoreCase(isExperiment)){
										targetExperimentId = toExperimentId;
										StringList siblingExperimentIds = (StringList)synchedObjectInfo.get(CORRESPONDING_EXPERIMENT_TASK_ID);
										Iterator toSubExperimentListIterator = toSubExperimentList.iterator();

										while(toSubExperimentListIterator.hasNext()){
											Map objectinfo = (Map)toSubExperimentListIterator.next();
											String objectId = (String)objectinfo.get(SELECT_ID);
											String parentTaskId = (String)objectinfo.get("to[Subtask].from.id");
											if(siblingExperimentIds.contains(objectId)){
												sParentOID = parentTaskId;
												parentTaskExistsInMasterProject = true;
												break;
											}
										}
									} else {
										boolean isTaskFromSubExperiment = false;

										// If Task is under sub-Experiment then following condition will be true
										if(!fromExperimentId.equals(selectedTaskProjectId)){
											isTaskFromSubExperiment = true;
											StringList busSelects = new StringList(SELECT_ID);
											busSelects.add(SELECT_NAME);

											DomainObject toObject = DomainObject.newInstance(context,toExperimentId);
											MapList subExperimentList = toSubExperimentList;

											StringList subExperimentIdList = new StringList(subExperimentList.size());
											StringList subExperimentNameList = new StringList(subExperimentList.size());
											Iterator subExperimentsListIterator = subExperimentList.iterator();
											while(subExperimentsListIterator.hasNext()){
												Map subExperimentInfo =  (Map)subExperimentsListIterator.next();
												String subExperimentId = (String)subExperimentInfo.get(SELECT_ID);
												String subExperimentName = (String)subExperimentInfo.get(SELECT_NAME);
												subExperimentIdList.add(subExperimentId);
												subExperimentNameList.add(subExperimentName);
											}


											if(correspondingExperimentIdList != null && correspondingExperimentIdList instanceof StringList){

												StringList parentOIDList = (StringList)correspondingExperimentIdList;
												for(int k=0 ;k<parentOIDList.size(); k++){
													String tempExpId = (String)parentOIDList.get(k);
													if(subExperimentIdList.contains(tempExpId)){
														targetExperimentId = tempExpId;
														break;
													}
												}

												if(ProgramCentralUtil.isNullString(targetExperimentId) && correspondingExperimentNameList instanceof StringList){
													StringList parentNameList = (StringList)correspondingExperimentNameList;
													for(int k=0 ;k<parentNameList.size(); k++){
														String tempExpName = (String)parentNameList.get(k);
														if(subExperimentNameList.contains(tempExpName)){
															targetExperimentId = (String)subExperimentIdList.get(k);;
															break;
														}
													}
												}

											}


										} else {
											targetExperimentId = toExperimentId;
										}

										String correspondingTaskId = EMPTY_STRING;

										//we are adding a task,if its corresponding task exists in other Experiment that means the task we are trying to add
										//has cut and then pasted here, need to remove Experiment relationship as newly added task will have Experiment relationship with Parent project task
										if(correspondingExperimentTaskId instanceof StringList){
											StringList parentExperiementIdList = (StringList)correspondingExperimentTaskIdProjectId;

											for(int i=0; i< parentExperiementIdList.size();  i++){
												String tempExpId = (String)parentExperiementIdList.get(i);
												if(targetExperimentId.equals(tempExpId)){
													correspondingTaskId = (String)((StringList)correspondingExperimentTaskId).get(i);
													break;
												}
											}
											if(ProgramCentralUtil.isNotNullString(correspondingTaskId)){
												DomainObject correspondingTask = DomainObject.newInstance(context,correspondingTaskId);
												String relId = correspondingTask.getInfo(context, "to[Experiment].id");
												DomainRelationship.disconnect(context, relId);

											}
										}


										correspondingMasterProjectTaskId = (String)synchedObjectInfo.get("to[Experiment].from.id");
										if(projectTaskDupParentIdList != null && projectTaskDupParentIdList instanceof StringList){

											StringList parentOIDList = (StringList)projectTaskDupParentIdList;
											for(int k=0 ;k<parentOIDList.size(); k++){
												String tempExpId = (String)parentOIDList.get(k);
												if(tempExpId.equals(targetExperimentId)){
													sParentOID = (String)((StringList)currespondingTaskOfExpTaskParent).get(k);
													parentTaskExistsInMasterProject = true;
													break;
												}
											}
										}
										else if(projectTaskDupParentIdList == null && currespondingTaskOfExpTaskParent != null ){

											StringList parentOIDList = (StringList) currespondingTaskOfExpTaskParent;
											for(int k=0 ;k<parentOIDList.size(); k++){
												String tempExpId = (String)parentOIDList.get(k);
												if(tempExpId.equals(targetExperimentId)){
													sParentOID = (String)((StringList)currespondingTaskOfExpTaskParent).get(k);
													parentTaskExistsInMasterProject = true;
													break;
												}/*else if(tempExpId.equals(sParentOID)){
													parentTaskExistsInMasterProject = true;
													break;
												}*/
											}
										}

									}	
									//If parent of a selected task does not exists in corresponding master project then following code will be executed.
									if(!parentTaskExistsInMasterProject){
										int selectedTaskParentLevel = 0;
										String strSelectedTaskLevel = (String)synchedObjectInfo.get("to[Subtask].attribute[Task WBS]");
										if(ProgramCentralUtil.isNotNullString(strSelectedTaskLevel)){
											selectedTaskParentLevel = (FrameworkUtil.split(strSelectedTaskLevel,".")).size();
											selectedTaskParentLevel = selectedTaskParentLevel -1;
										}
										String strSelectedTaskSequenceOrder = (String)synchedObjectInfo.get("to[Subtask].attribute[Sequence Order]");
										int selectedTaskSequenceOrder = Integer.parseInt(strSelectedTaskSequenceOrder);

										Map taskInfoByLevel = new HashMap();
										StringList busSelects = new StringList(4);
										busSelects.addElement(DomainObject.SELECT_ID);
										busSelects.addElement(DomainObject.SELECT_LEVEL);
										busSelects.addElement(ProgramCentralConstants.SELECT_NAME);
										busSelects.addElement(ProgramCentralConstants.SELECT_TYPE);

										StringList relationshipSelects = new StringList(3);
										relationshipSelects.add(SubtaskRelationship.SELECT_TASK_WBS);
										relationshipSelects.add(SubtaskRelationship.SELECT_SEQUENCE_ORDER);


										if(selectedTaskParentLevel ==0){
											sParentOID = targetExperimentId;
										} else {

											DomainObject toObject = DomainObject.newInstance(context,targetExperimentId);
											MapList taskList = toObject.getRelatedObjects(
													context,
													RELATIONSHIP_SUBTASK,
													TYPE_PROJECT_MANAGEMENT,
													busSelects,
													relationshipSelects,
													false,
													true,
													(short)0,
													DomainConstants.EMPTY_STRING,
													DomainConstants.EMPTY_STRING,
													0);

											for (Object objectMap :taskList) {

												Map<String,String> taskInfoMap 	= (Map<String,String>)objectMap;
												String taskLevel 				= taskInfoMap.get(KEY_LEVEL);

												if(taskInfoByLevel.containsKey(taskLevel)){
													MapList levelList = (MapList)taskInfoByLevel.get(taskLevel);
													levelList.add(taskInfoMap);
													taskInfoByLevel.put(taskLevel, levelList);
												}else{
													MapList levelList = new MapList();
													levelList.add(taskInfoMap);
													taskInfoByLevel.put(taskLevel, levelList);
												}
											}

											MapList parentTaskList = (MapList)taskInfoByLevel.get(Integer.toString(selectedTaskParentLevel));
											MapList updatedParentTaskList = new MapList();
											Iterator parentTaskListIterator = parentTaskList.iterator();
											while(parentTaskListIterator.hasNext()){
												Map<String,String> parentTaskMap = (Map)parentTaskListIterator.next();
												String strParentTaskSequenceOrder = parentTaskMap.get(SubtaskRelationship.SELECT_SEQUENCE_ORDER);
												int parentTaskSequenceOrder = Integer.parseInt(strParentTaskSequenceOrder);
												if( parentTaskSequenceOrder < selectedTaskSequenceOrder){
													updatedParentTaskList.add(parentTaskMap);
												}
											}

											if(updatedParentTaskList!=null && !updatedParentTaskList.isEmpty()){
												updatedParentTaskList.sort(SubtaskRelationship.SELECT_SEQUENCE_ORDER,ProgramCentralConstants.DESCENDING_SORT,ProgramCentralConstants.SORTTYPE_INTEGER);
												Map taskParentInfo = (Map)updatedParentTaskList.get(0);
												sParentOID = (String)taskParentInfo.get(SELECT_ID);//Initialize parentOID 
												String taskParentType = (String)taskParentInfo.get(ProgramCentralConstants.SELECT_TYPE);
												String taskParentName = (String)taskParentInfo.get(ProgramCentralConstants.SELECT_NAME);

												//Select correct parentOID
												if(selectedTaskParentName.equals(taskParentName) && selectedTaskParentType.equals(taskParentType)){
													sParentOID = (String)taskParentInfo.get(SELECT_ID);
												} else {
													for(int i=0;i<parentTaskList.size();i++){
														Map parentObjectInfo = (Map)parentTaskList.get(i);
														String parentObjectType = (String)parentObjectInfo.get(ProgramCentralConstants.SELECT_TYPE);
														String parentObjectName = (String)parentObjectInfo.get(ProgramCentralConstants.SELECT_NAME);
														if(selectedTaskParentName.equals(parentObjectName) && selectedTaskParentType.equals(parentObjectType)){
															sParentOID = (String)parentObjectInfo.get(SELECT_ID);
														}
													}
												}
											}else{
												for(int i=0;i<parentTaskList.size();i++){
													Map parentObjectInfo = (Map)parentTaskList.get(i);
													String parentObjectType = (String)parentObjectInfo.get(ProgramCentralConstants.SELECT_TYPE);
													String parentObjectName = (String)parentObjectInfo.get(ProgramCentralConstants.SELECT_NAME);
													if(selectedTaskParentName.equals(parentObjectName) && selectedTaskParentType.equals(parentObjectType)){
														sParentOID = (String)parentObjectInfo.get(SELECT_ID);
													}
												}
											}
										}
									}
								}	
							}else{
								String projectTaskDupId = DomainObject.EMPTY_STRING;

								StringList projectTaskParentDupIdsList = new StringList();
								if(projectTaskDupParentIdList != null && projectTaskDupParentIdList instanceof StringList){
									projectTaskParentDupIdsList = (StringList)projectTaskDupParentIdList;
								}

								StringList projectTaskDupIdsList = new StringList();
								if(correspondingTaskOfProjectTaskParent != null && correspondingTaskOfProjectTaskParent instanceof StringList){
									projectTaskDupIdsList = (StringList)correspondingTaskOfProjectTaskParent;
									if(!projectTaskDupIdsList.contains(sParentOID)){
										for(int i=0;i<projectTaskParentDupIdsList.size();i++){
											String taskParentDupId = (String)projectTaskParentDupIdsList.get(i);
											if(objectId1.equalsIgnoreCase(taskParentDupId)){
												projectTaskDupId = (String)projectTaskDupIdsList.get(i);
												break;
											}
										}
										sParentOID = projectTaskDupId;
									}
								}


							}

							if(ProgramCentralUtil.isNullString(sParentOID)){
								String strErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
										"emxProgramCentral.Experiment.InvalidSync", context.getSession().getLanguage());
								returnHashMap.put("Message", strErrMsg+" "+objectName);
								returnHashMap.put("Action", "ERROR");
								return returnHashMap;
							}

						}

						if(ProgramCentralUtil.isNullString(sParentOID) && markup.equals("cut") && syncDir.equalsIgnoreCase("right")){
							sParentOID = objectId2;
						}else if(ProgramCentralUtil.isNullString(sParentOID) && markup.equals("cut") && syncDir.equalsIgnoreCase("left")){
							sParentOID = objectId1;
						}

						Task taskTargetParent = new Task(sParentOID);
						String strParentTaskState = taskTargetParent.getInfo(context, DomainConstants.SELECT_CURRENT);

						StringList stateList = new StringList(3);
						stateList.addElement(DomainConstants.STATE_PROJECT_SPACE_COMPLETE);
						stateList.addElement(DomainConstants.STATE_PROJECT_SPACE_REVIEW);
						stateList.addElement(DomainConstants.STATE_PROJECT_SPACE_ARCHIVE);

						if(stateList.contains(strParentTaskState)) {

							String strErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
									"emxProgramCentral.Project.TaskInState3", context.getSession().getLanguage());

							returnHashMap.put("Message", strErrMsg);
							returnHashMap.put("Action", "ERROR");

							return returnHashMap;
						}

						if(markup.equals("cut")){
							boolean isSummaryTask = false;
							String IS_SUMMARY_TASK = "from["+DomainRelationship.RELATIONSHIP_SUBTASK+"].to.id";

							StringList selectable = new StringList(4);
							selectable.addElement(SELECT_CORRESPONDING_TASK_PARENT_EXPERIMENT_ID);
							selectable.addElement(IS_TASK_PARENT_KINDOF_EXPERIMENT);
							selectable.addElement(IS_SUMMARY_TASK);
							selectable.addElement(DomainObject.SELECT_CURRENT);
							selectable.addElement(DomainObject.SELECT_NAME);

							String[]objectIds = new String[1];
							objectIds[0] = sObjectId;

							MapList synchedObjectInfoList = DomainObject.getInfo(context, objectIds, selectable);

							Map synchedObjectInfo = (Map)synchedObjectInfoList.get(0);
							String isParentExperimentProject = (String)synchedObjectInfo.get(IS_TASK_PARENT_KINDOF_EXPERIMENT);
							Object rootObjectIds = synchedObjectInfo.get(SELECT_CORRESPONDING_TASK_PARENT_EXPERIMENT_ID);
							String isSummaryObject 				= (String)synchedObjectInfo.get(IS_SUMMARY_TASK);
							String taskstate	 				= (String)synchedObjectInfo.get(DomainObject.SELECT_CURRENT);
							String taskName		 				= (String)synchedObjectInfo.get(DomainObject.SELECT_NAME);

							if("true".equalsIgnoreCase(isSummaryObject)){
								isSummaryTask = true;
							}

							if("true".equalsIgnoreCase(isParentExperimentProject)){

								Map<String,String> returnMap = deleteSyncedTask(context, sParentOID, sObjectId,taskstate,isSummaryTask,sRelId);
								String action = (String)returnMap.get("Action");
								String errorMsg = (String)returnMap.get("Message");

								if("ERROR".equalsIgnoreCase(action)){
									returnHashMap.put("Message", errorMsg);
									returnHashMap.put("Action", "ERROR");
								}else if("WARNING".equalsIgnoreCase(action)){
									String nameList = PropertyUtil.getRPEValue(context, "nameList", true);
									if(ProgramCentralUtil.isNotNullString(nameList)){
										nameList += ","+taskName; 
									}else{
										nameList = taskName;
									}

									PropertyUtil.setRPEValue(context, "SyncDone", "false", true);
									PropertyUtil.setRPEValue(context, "nameList", nameList, true);
								}

							}else{
								StringList cloneTaskRootObjectList = new StringList();
								String cloneTaskRootObjectId = DomainObject.EMPTY_STRING;

								if(rootObjectIds != null && rootObjectIds instanceof StringList){
									cloneTaskRootObjectList = (StringList)rootObjectIds;
								}else if(rootObjectIds != null && rootObjectIds instanceof String){
									cloneTaskRootObjectId = (String)rootObjectIds;
								}else {
									Map<String,String> returnMap = deleteSyncedTask(context, sParentOID, sObjectId,taskstate,isSummaryTask,sRelId);
									String action = (String)returnMap.get("Action");
									String errorMsg = (String)returnMap.get("Message");

									if("ERROR".equalsIgnoreCase(action)){
										returnHashMap.put("Message", errorMsg);
										returnHashMap.put("Action", "ERROR");
									}else if("WARNING".equalsIgnoreCase(action)){
										String nameList = PropertyUtil.getRPEValue(context, "nameList", true);
										if(ProgramCentralUtil.isNotNullString(nameList)){
											nameList += ","+taskName; 
										}else{
											nameList = taskName;
										}
										System.out.println("list:::"+nameList);
										PropertyUtil.setRPEValue(context, "SyncDone", "false", true);
										PropertyUtil.setRPEValue(context, "nameList", nameList, true);
									}
								}

								if(ProgramCentralUtil.isNotNullString(cloneTaskRootObjectId)){
									if(!objectId1.equalsIgnoreCase(cloneTaskRootObjectId)){
										Map<String,String> returnMap = deleteSyncedTask(context, sParentOID, sObjectId,taskstate,isSummaryTask,sRelId);
										String action = (String)returnMap.get("Action");
										String errorMsg = (String)returnMap.get("Message");
										if("ERROR".equalsIgnoreCase(action)){
											returnHashMap.put("Message", errorMsg);
											returnHashMap.put("Action", "ERROR");
										}else if("WARNING".equalsIgnoreCase(action)){
											String nameList = PropertyUtil.getRPEValue(context, "nameList", true);
											if(ProgramCentralUtil.isNotNullString(nameList)){
												nameList += ","+taskName; 
											}else{
												nameList = taskName;
											}

											PropertyUtil.setRPEValue(context, "SyncDone", "false", true);
											PropertyUtil.setRPEValue(context, "nameList", nameList, true);
										}
									}
								}else if(cloneTaskRootObjectList.size()>0){
									if(!cloneTaskRootObjectList.contains(objectId1)){
										Map<String,String> returnMap = deleteSyncedTask(context, sParentOID, sObjectId,taskstate,isSummaryTask,sRelId);
										String action = (String)returnMap.get("Action");
										String errorMsg = (String)returnMap.get("Message");
										if("ERROR".equalsIgnoreCase(action)){
											returnHashMap.put("Message", errorMsg);
											returnHashMap.put("Action", "ERROR");
										}else if("WARNING".equalsIgnoreCase(action)){
											String nameList = PropertyUtil.getRPEValue(context, "nameList", true);
											if(ProgramCentralUtil.isNotNullString(nameList)){
												nameList += ","+taskName; 
											}else{
												nameList = taskName;
											}

											PropertyUtil.setRPEValue(context, "SyncDone", "false", true);
											PropertyUtil.setRPEValue(context, "nameList", nameList, true);
										}
									}else{
										DomainObject taskObj  = DomainObject.newInstance(context, sObjectId);
										String isExpRelExists = taskObj.getInfo(context, "from[Experiment]");
										if("true".equalsIgnoreCase(isExpRelExists)){
											DomainRelationship.disconnect(context, sRelId);
											Task parentTask = new Task();
											parentTask.reSequence(context, objectId2);
										}
									}
								}
							}

							Map<String,String> xmlInfoMap = new HashMap<String,String>(); 
							xmlInfoMap.put("oid", sObjectId);
							xmlInfoMap.put("rowId", sRowId);
							xmlInfoMap.put("relid", sRelId);
							xmlInfoMap.put("markup", markup);

							mlItems.add(xmlInfoMap);

						}else{

							final String SELECT_KINDOF_TASK_MANAGEMENT = "type.kindof[" + DomainObject.TYPE_TASK_MANAGEMENT+ "]";
							Experiment expObject = new Experiment(sObjectId);

							StringList objectSelectables = new StringList(3);
							objectSelectables.addElement(SELECT_KINDOF_TASK_MANAGEMENT);
							objectSelectables.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
							objectSelectables.addElement(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
							objectSelectables.addElement(SELECT_PARENT_TASK_ID);

							Map experimentInfoList = expObject.getInfo(context, objectSelectables);
							String isKindOfTaskManagement = (String)experimentInfoList.get(SELECT_KINDOF_TASK_MANAGEMENT);
							String isKindOfProjectSpace = (String)experimentInfoList.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
							String isKindOfExperiment = (String)experimentInfoList.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);

							String newTaskId = DomainObject.EMPTY_STRING;

							if(!isCompareExperiments){
								if("TRUE".equalsIgnoreCase(isKindOfTaskManagement) &&
										!isPresentInProject(context,objectId1,sObjectId)){
									newTaskId = expObject.cloneWBSTask(context,objectId2, sParentOID, sRelId,isCompareExperiments);
								}else if("TRUE".equalsIgnoreCase(isKindOfExperiment)){
									String strExpParentId = (String)experimentInfoList.get(SELECT_PARENT_TASK_ID);

									if(isPresentInProject(context,objectId2,strExpParentId)){
										String strMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
												"emxProgramCentral.Experiment.AlreadyAddedProject", context.getSession().getLanguage());
										MqlUtil.mqlCommand(context, "warning $1", strMsg);
									}else{
										newTaskId = expObject.addOrignalProject(context,sParentOID,strExpParentId,sRelId);
									}
								}else if("TRUE".equalsIgnoreCase(isKindOfProjectSpace)){
									if(isPresentInProject(context,objectId1,sObjectId)){
										String strMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
												"emxProgramCentral.Experiment.AlreadyAddedProject", context.getSession().getLanguage());
										MqlUtil.mqlCommand(context, "warning $1", strMsg);
									}else{
										newTaskId = expObject.addProjectInExperiment(context,sParentOID,sObjectId,sRelId);
									}
								}
							} else{

								if("TRUE".equalsIgnoreCase(isKindOfTaskManagement)){
									newTaskId = expObject.cloneWBSTask(context,objectId2, sParentOID, sRelId,isCompareExperiments);

									//following condition is added to connect the newly added Experiment task to master project task
									if(isCompareExperiments && ProgramCentralUtil.isNotNullString(correspondingMasterProjectTaskId)){
										Experiment experimentObject = new Experiment(correspondingMasterProjectTaskId);
										boolean hasFromConnectAccessBit = experimentObject.checkAccess(context, (short) AccessConstants.cFromConnect);
										boolean hasToConnectAccessBit = experimentObject.checkAccess(context, (short) AccessConstants.cToConnect);

										if(hasFromConnectAccessBit && hasToConnectAccessBit ){
											DomainRelationship.connect(context,correspondingMasterProjectTaskId ,
													ProgramCentralConstants.RELATIONSHIP_EXPERIMENT,newTaskId ,true);
										}else{
											try {
												ProgramCentralUtil.pushUserContext(context);
												DomainRelationship.connect(context,correspondingMasterProjectTaskId,
														ProgramCentralConstants.RELATIONSHIP_EXPERIMENT,newTaskId,true);
											} finally {
												ProgramCentralUtil.popUserContext(context);
											}
										}									
									}
								}else if("TRUE".equalsIgnoreCase(isKindOfExperiment)){

									newTaskId = expObject.addExperimentInExperiment(context,sParentOID,sObjectId,sRelId);								
								}													
							}
							Map<String,String> xmlInfoMap = new HashMap<String,String>();
							xmlInfoMap.put("oid", newTaskId);
							xmlInfoMap.put("rowId", sRowId);
							xmlInfoMap.put("relid", sRelId);
							xmlInfoMap.put("markup", markup);

							mlItems.add(xmlInfoMap);
						}
					}else{
						Map<String,String> xmlInfoMap = new HashMap<String,String>();
						xmlInfoMap.put("oid", sObjectId);
						xmlInfoMap.put("rowId", sRowId);
						xmlInfoMap.put("relid", sRelId);
						xmlInfoMap.put("markup", markup);

						mlItems.add(xmlInfoMap);
					}
				}
			}

			returnHashMap.put("changedRows", mlItems);
			returnHashMap.put("Action", "success");

			return returnHashMap;

		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	private Map<String,String>deleteSyncedTask(Context context,
			String parentId,
			String selectedObjectId,
			String taskState,
			boolean isSummaryTask,String relId)throws Exception {

		Map<String,String> returnHashMap = new HashMap<String,String>(); 

		if(!isSummaryTask){
			String mqlCommand = "delete bus $1";
			MqlUtil.mqlCommand(context, true, true, mqlCommand, true, selectedObjectId);

			returnHashMap.put("Action", "success");

			Task parentTask = new Task();
			parentTask.reSequence(context, parentId);

		}else if(isSummaryTask) {
			String relationshipIds[] = new String[1];
			relationshipIds[0]= relId;

			Map <String,Object>childrenMap 	= hasChildren(context, selectedObjectId,taskState);
			String hasChildren = (String)childrenMap.get("hasChildren");
			String hasError = (String)childrenMap.get("Error");

			if("true".equalsIgnoreCase(hasChildren)	&& "false".equalsIgnoreCase(hasError)) {
				StringList sToBeDeletedTaskIds 	= (StringList)childrenMap.get("toBeDeleted");
				StringList toBeDisconnected 	= (StringList)childrenMap.get("toBeDisconnected");

				//Delete child tasks
				int length = sToBeDeletedTaskIds.size();
				for(int i = 0; i<length; i++) {
					String taskId 		= (String)sToBeDeletedTaskIds.get(i);
					String mqlCommand 	= "delete bus $1";
					MqlUtil.mqlCommand(context, true, true, mqlCommand, true, taskId);
				}

				//Disconnect project or experiment from summary task
				length = toBeDisconnected.size();
				for(int i=0; i<length; i++){
					String taskId 		= (String)toBeDisconnected.get(i);
					String mqlCommand 	= "disconnect businessobject $1 relationship $2 from $3";
					MqlUtil.mqlCommand(context, true, true, mqlCommand, true, taskId,"Subtask",selectedObjectId);
				}

				//Delete summary task
				String mqlCommand = "delete bus $1";
				MqlUtil.mqlCommand(context, true, true, mqlCommand, true, selectedObjectId);

				//Do rollup
				Task parentTask = new Task();
				parentTask.reSequence(context, parentId);

				returnHashMap.put("Action", "success");

			}else if("true".equalsIgnoreCase(hasError)) {
				String errorMsg = (String)childrenMap.get("Message");
				returnHashMap.put("Message", errorMsg);
				returnHashMap.put("Action", "ERROR");
			}
		} 

		return returnHashMap;
	}
	/**
	 * Get children list of select task.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param objectId is a selected task id.
	 * @return selected task info map.
	 * @throws Exception if operation fails.
	 */
	private Map<String,Object> hasChildren(Context context,String objectId,String taskState)throws Exception
	{
		Map <String,Object>returnMap = new HashMap<String,Object>();

		StringList toBeNotDeletedStateList = new StringList(6);
		toBeNotDeletedStateList.addElement(ProgramCentralConstants.STATE_PROJECT_TASK_ASSIGN);
		toBeNotDeletedStateList.addElement(ProgramCentralConstants.STATE_PROJECT_TASK_ACTIVE);
		toBeNotDeletedStateList.addElement(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW);
		toBeNotDeletedStateList.addElement(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE);
		toBeNotDeletedStateList.addElement(ProgramCentralConstants.STATE_PROJECT_SPACE_ARCHIVE);
		toBeNotDeletedStateList.addElement(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL);

		if(toBeNotDeletedStateList.contains(taskState)){
			returnMap.put("Error", "true");
			returnMap.put("Message", "Selected task or child tasks are beyond Create state");
		}else {

			StringList objectSelects = new StringList(3);
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			objectSelects.addElement(ProgramCentralConstants.SELECT_PROJECT_ID);

			StringList relationshipSelects = new StringList(0);
			short sRecurseToLevel = 0;

			DomainObject object = DomainObject.newInstance(context, objectId);
			MapList mapList = object.getRelatedObjects(context,
					ProgramCentralConstants.RELATIONSHIP_SUBTASK,
					DomainObject.TYPE_PROJECT_MANAGEMENT,
					objectSelects,
					relationshipSelects,
					false,
					true,
					sRecurseToLevel,
					DomainConstants.EMPTY_STRING,
					DomainConstants.EMPTY_STRING,
					0);

			StringList toBeDeletedTaskIdList = new StringList();
			StringList toBeDisconnected = new StringList();
			if(mapList.size()>0){

				mapList.sort(DomainConstants.SELECT_LEVEL,
						ProgramCentralConstants.DESCENDING_SORT,ProgramCentralConstants.SORTTYPE_INTEGER);

				returnMap.put("hasChildren", "true");

				int resultsize = mapList.size();
				for(int i=0; i<resultsize; i++) {
					Map objectMap = (Map)mapList.get(i);
					String taskId = (String)objectMap.get(DomainConstants.SELECT_ID);
					String isProjectSpace 	= (String)objectMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);

					if("true".equalsIgnoreCase(isProjectSpace)){
						toBeDisconnected.addElement(taskId);
					}
				}

				for(int i=0; i<resultsize; i++) {

					Map objectMap 		= (Map)mapList.get(i);
					String taskId 		= (String)objectMap.get(DomainConstants.SELECT_ID);
					String taskProjectId = (String)objectMap.get(ProgramCentralConstants.SELECT_PROJECT_ID);

					if(taskProjectId != null && !toBeDisconnected.contains(taskProjectId)){
						toBeDeletedTaskIdList.addElement(taskId);
					}else if(!toBeDisconnected.contains(taskId)){
						toBeDeletedTaskIdList.addElement(taskId);
					}
				}

				returnMap.put("children",mapList);
				returnMap.put("Error", "false");
				returnMap.put("toBeDeleted",toBeDeletedTaskIdList);
				returnMap.put("toBeDisconnected",toBeDisconnected);

			}else{
				returnMap.put("hasChildren", "false");
				returnMap.put("Error", "false");
			}
		}

		return returnMap;
	}

	/**
	 * Check for selected project is present in Master project or not. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param strProjectId is master project id.
	 * @param strChildObjectId is a selected project id.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	private boolean isPresentInProject(Context context,String strProjectId,String strChildObjectId)throws Exception
	{
		boolean isAlreadyAdded = false;

		DomainObject project = DomainObject.newInstance(context,strProjectId);
		String projectType = project.getType(context);
		StringList busSelects = new StringList();
		busSelects.add(DomainObject.SELECT_TYPE); 
		busSelects.add(DomainObject.SELECT_ID);

		if(projectType.equals(ProgramCentralConstants.TYPE_EXPERIMENT)){
			busSelects.add(SELECT_PARENT_TASK_ID);
		}

		String relPattern = DomainConstants.RELATIONSHIP_SUBTASK;
		String typePattern=ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT; 
		String strRelatedPtojectId=ProgramCentralConstants.EMPTY_STRING;

		MapList mlRelatedProjects=project.getRelatedObjects(context,
				relPattern,
				typePattern,
				busSelects,
				null,false,
				true,(short)0,
				null,null,0);
		if(projectType.equals(ProgramCentralConstants.TYPE_EXPERIMENT)){
			for (int i=0; i<mlRelatedProjects.size(); i++){
				Map mpRelatedProject = (Map) mlRelatedProjects.get(i);
				strRelatedPtojectId = (String)mpRelatedProject.get(SELECT_PARENT_TASK_ID);
				if(strChildObjectId.equals(strRelatedPtojectId)){
					isAlreadyAdded = true;
					break;
				}
			}
		}else{
			for (int i=0; i<mlRelatedProjects.size(); i++){
				Map mpRelatedProject = (Map) mlRelatedProjects.get(i);
				strRelatedPtojectId = (String)mpRelatedProject.get(DomainObject.SELECT_ID);
				if(strChildObjectId.equals(strRelatedPtojectId)){
					isAlreadyAdded = true;
					break;
				}
			}
		}
		return isAlreadyAdded;
	}

	/**
	 * Update duration of object.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @throws Exception,if operation fails.
	 */
	public void updateDuration(Context context,String[]args)throws Exception
	{
		try{

			Map inputMap = JPO.unpackArgs(args);
			Map columnMap = (Map) inputMap.get("columnMap");
			Map paramMap = (Map) inputMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			String newAttrValue = (String) paramMap.get("New Value");

			/*			if(ProgramCentralUtil.isNotNullString(newAttrValue)){

				if("Days".equalsIgnoreCase(newAttrValue.substring(newAttrValue.lastIndexOf(" ")+1)))
					newAttrValue = newAttrValue.replace(newAttrValue.substring(newAttrValue.lastIndexOf(" ")+1), "d");
				else
					newAttrValue = newAttrValue.replace(newAttrValue.substring(newAttrValue.lastIndexOf(" ")+1), "h");

				DomainObject taskObject = DomainObject.newInstance(context, objectId);
				boolean hasAccess = taskObject.checkAccess(context, (short) AccessConstants.cModify);
				if(hasAccess){

					StringList selectList = new StringList(2);
					selectList.addElement(SELECT_CURRENT);
					selectList.addElement(SELECT_HAS_SUBTASK);

					Map objectMap = taskObject.getInfo(context,selectList);

					String strCurrentState	= (String)objectMap.get(SELECT_CURRENT);
					String strHasSubTask 	= (String)objectMap.get(SELECT_HAS_SUBTASK);

					//if(checkEditable(0,strCurrentState,strHasSubTask)){
						Map<String,String> taskInfoMap = new HashMap<String,String>();
						taskInfoMap.put("durationS",newAttrValue);

						Map<String,Map<String,String>> taskMap 	= new HashMap<String,Map<String,String>>();
						taskMap.put(objectId,taskInfoMap);

						try {
							ContextUtil.startTransaction(context,true);
							String strMsg = Task.updateDates(context,taskMap);

							if(!"false".equalsIgnoreCase(strMsg)){
								ContextUtil.abortTransaction(context);
								throw new Exception(strMsg);
							}
							ContextUtil.commitTransaction(context);
						}catch(Exception e){
							e.printStackTrace();
							ContextUtil.abortTransaction(context);
						}
					//}

				}else{
					String strMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Experiment.ModifyAsscess", context.getSession().getLanguage());
					MqlUtil.mqlCommand(context, "warning $1", strMsg);
				}
			}*/
		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}

	}

	/**
	 * Update constraint date of object.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @throws Exception,if operation fails.
	 */
	public void updateConstraintDate(Context context,String args[]) throws Exception
	{
		Map programMap =JPO.unpackArgs(args);
		Map paramMap = (Map) programMap.get("paramMap");
		Map requestMap = (Map) programMap.get("requestMap");
		String newConstraintDate = (String) paramMap.get("New Value");
		Locale locale = (Locale)requestMap.get("locale");

		if(null==locale)
		{
			locale = (Locale)requestMap.get("localeObj");
		}

		if(ProgramCentralUtil.isNotNullString(newConstraintDate)){

			Task task = (Task)DomainObject.newInstance(context, DomainObject.TYPE_TASK, DomainObject.PROGRAM);
			String strObjectId = (String) paramMap.get("objectId");

			StringList busSelect = new StringList(3);
			busSelect.addElement(DomainObject.SELECT_CURRENT);
			busSelect.addElement(ProjectManagement.SELECT_TASK_CONSTRAINT_DATE);
			busSelect.addElement(ProjectManagement.SELECT_TASK_CONSTRAINT_TYPE);

			task.setId(strObjectId);

			Map<String,String> objectMap 	= task.getInfo(context, busSelect);
			String objectState 				= objectMap.get(DomainObject.SELECT_CURRENT);

			if(checkEditable(1, objectState, null)) {

				String oldConstraintdate 		= objectMap.get(ProjectManagement.SELECT_TASK_CONSTRAINT_DATE);

				SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
				TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
				double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
				double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();

				Date oldDate = null;
				if(null!=oldConstraintdate && !"".equals(oldConstraintdate)) {
					oldDate = eMatrixDateFormat.getJavaDate(oldConstraintdate, locale);
				}

				newConstraintDate = eMatrixDateFormat.getFormattedInputDate(context,newConstraintDate,clientTZOffset, locale);
				Date newDate = dateFormat.parse(newConstraintDate);

				if((null==oldConstraintdate ||"".equals(oldConstraintdate)) || (!(newDate.compareTo(oldDate)==0))){

					String strTaskConstraintType 	= objectMap.get(ProjectManagement.SELECT_TASK_CONSTRAINT_TYPE);
					Calendar constraintDate = Calendar.getInstance();
					constraintDate.setTime(newDate);
					if(strTaskConstraintType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNLT)|| strTaskConstraintType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET) || strTaskConstraintType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_MSON))
					{
						constraintDate.set(Calendar.HOUR_OF_DAY, 8);
						constraintDate.set(Calendar.MINUTE, 0);
						constraintDate.set(Calendar.SECOND, 0); 


					}else if(strTaskConstraintType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_MFON)|| strTaskConstraintType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || strTaskConstraintType.equals(DomainConstants.ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNET))
					{
						constraintDate.set(Calendar.HOUR_OF_DAY, 17);
						constraintDate.set(Calendar.MINUTE, 0);
						constraintDate.set(Calendar.SECOND, 0); 
					}

					String reportDate = dateFormat.format(constraintDate.getTime());
					task.setAttributeValue(context, ATTRIBUTE_TASK_CONSTRAINT_DATE, reportDate);
				}

			}

		}

	}

	/**
	 * Update constraint type of Task.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @throws Exception,if operation fails.
	 */
	public void updateConstraintType(Context context,String args[]) throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		Map paramMap = (Map) programMap.get("paramMap");
		String strObjectId = (String) paramMap.get("objectId");
		String newewConstraintType = (String) paramMap.get("New Value");

		if(ProgramCentralUtil.isNotNullString(newewConstraintType)) {

			Task task = (Task)DomainObject.newInstance(context, DomainObject.TYPE_TASK, DomainObject.PROGRAM);
			String SELECT_PROJECT_SCHEDULE_FROM_TASK = "to[Project Access Key].from.from[Project Access List].to.attribute[Schedule From]";
			String SELECT_IS_SUMMARY_TASK = "from[Subtask]";

			StringList busSelect = new StringList(5);
			busSelect.addElement(DomainObject.SELECT_CURRENT);
			busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			busSelect.addElement(SELECT_IS_SUMMARY_TASK);
			busSelect.addElement(SELECT_PROJECT_SCHEDULE_FROM_TASK);

			task.setId(strObjectId);
			Map objectMap = task.getInfo(context, busSelect);
			String objectState = (String)objectMap.get(DomainObject.SELECT_CURRENT);

			if(checkEditable(1, objectState, null)) {

				String isProjectSpace = (String)objectMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				if("true".equalsIgnoreCase(isProjectSpace)) {
					task.setAttributeValue(context, ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, newewConstraintType);
				}else{

					boolean isSummeryTask 	= false;
					Object taskObjects 		= (String)objectMap.get(SELECT_IS_SUMMARY_TASK);
					String strProjectScheduledFrom = (String)objectMap.get(SELECT_PROJECT_SCHEDULE_FROM_TASK);

					if(taskObjects != null){
						isSummeryTask = true;
					}

					if(!isSummeryTask){
						task.setAttributeValue(context, ATTRIBUTE_TASK_CONSTRAINT_TYPE, newewConstraintType);
					}else{
						if("Project Start Date".equals(strProjectScheduledFrom)){
							if(newewConstraintType.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP) || newewConstraintType.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || newewConstraintType.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET)){
								task.setAttributeValue(context, ATTRIBUTE_TASK_CONSTRAINT_TYPE, newewConstraintType);
							}
						}else{
							if(newewConstraintType.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ALAP) || newewConstraintType.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_FNLT) || newewConstraintType.equals(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_SNET)){
								task.setAttributeValue(context, ATTRIBUTE_TASK_CONSTRAINT_TYPE, newewConstraintType);
							}
						}
					}
				}
			}

		}

	}

	private boolean checkEditable(int fieldType, String currentState, String hasSubTask) 
	{
		boolean blEditable = true;

		switch (fieldType) {

		case 0: if(currentState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE)||
				currentState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW) ||
				hasSubTask.equalsIgnoreCase("true"))
			blEditable = false;
		break;
		case 1: if(currentState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE)||
				currentState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW) ||
				currentState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_ACTIVE))
			blEditable = false;
		break;

		}
		return blEditable;
	}

	/**
	 * Update Start Date of task.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @throws Exception,if operation fails.
	 */
	public void updateStartDate(Context context,String args[])throws Exception
	{
		try{
			Map inputMap = JPO.unpackArgs(args);
			Map paramMap = (Map) inputMap.get("paramMap");

			String newAttrValue = (String) paramMap.get("New Value");
			if(ProgramCentralUtil.isNotNullString(newAttrValue)){
				inputMap.put("callFrom", "WhatIf");
				JPO.invoke(context,
						"emxTask",
						new String[] {},
						"updateEstStartDate",
						JPO.packArgs(inputMap),
						Map.class);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * Update End date of Task.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @throws Exception,if operation fails.
	 */
	public void updateEndDate(Context context,String args[])throws Exception
	{
		try{
			Map inputMap = JPO.unpackArgs(args);
			Map paramMap = (Map) inputMap.get("paramMap");
			Map requestMap = (Map) inputMap.get("requestMap");
			String objectId = (String) paramMap.get("objectId");
			String newAttrValue = (String) paramMap.get("New Value");
			//TODO
		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * Get respective parent task of Experiment WBS Task. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return parent task.
	 * @throws Exception, if operation fails.
	 */
	public StringList getParentTask (Context context, String[] args) throws Exception
	{
		long start = System.currentTimeMillis();
		String isExperiment = "true";
		String isBaseLine 	= "true";
		Map programMap 		=  JPO.unpackArgs(args);
		Map paramList 		= (Map) programMap.get("paramList");
		MapList objectList 	= (MapList) programMap.get("objectList");


		String projectId = (String)paramList.get("objectId");
		if(projectId == null || projectId.isEmpty()){
			projectId = (String)paramList.get("parentOID");
		}

		int size = objectList.size();
		StringList taskList = new StringList(size);

		if(projectId != null || !projectId.isEmpty()){

			StringList objectSelect = new StringList();
			objectSelect.addElement(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
			objectSelect.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_BASELINE);

			DomainObject rootObj 	= DomainObject.newInstance(context, projectId);
			Map rootObjInfo 		= rootObj.getInfo(context, objectSelect);
			isExperiment 			= (String)rootObjInfo.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
			isBaseLine 				= (String)rootObjInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_BASELINE);

			if("true".equalsIgnoreCase(isExperiment) || "true".equalsIgnoreCase(isBaseLine)){

				String []objectIds = new String[size];
				for(int i=0;i<size;i++){
					Map objectMap 		= (Map) objectList.get(i);
					String strTaskId 	= (String)objectMap.get(DomainObject.SELECT_ID);
					objectIds[i] 		= strTaskId;
				}

				MapList taskInfoList = null;
				StringList subExperimentList = new StringList();

				objectSelect.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_SOURCE_ID);
				objectSelect.addElement(Experiment.SELECT_PROJECT_SOURCE_ID);
				objectSelect.addElement(SELECT_ID);

				try{
					ProgramCentralUtil.pushUserContext(context);
					taskInfoList = DomainObject.getInfo(context, objectIds, objectSelect);

				}finally{
					ProgramCentralUtil.popUserContext(context);
				}

				//Identify sub-project
				subExperimentList.addElement(projectId);

				for(int i=0;i<size; i++){
					Map taskMap 		= (Map)taskInfoList.get(i);
					String isExp 	= (String)taskMap.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
					String isBL 	= (String)taskMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_BASELINE);

					if("true".equalsIgnoreCase(isExp) || "true".equalsIgnoreCase(isBL)){
						Map objectMap 	= (Map)objectList.get(i);

						String id 		= (String)objectMap.get(SELECT_ID);
						subExperimentList.addElement(id);
					}
				}

				//Start processing link
				String toolTipMasterTask = EnoviaResourceBundle.getProperty(context, 
						"ProgramCentral","emxProgramCentral.Tooltip.MasterTask", 
						context.getSession().getLanguage());

				String relationship = ProgramCentralConstants.RELATIONSHIP_EXPERIMENT;

				if("true".equalsIgnoreCase(isBaseLine)){
					relationship = ProgramCentralConstants.RELATIONSHIP_PROJECT_BASELINE;
				}

				Map<String,Object> masterTaskInfo = experiment.getMasterProjectTaskInfo(context, subExperimentList,relationship,false,true);

				for(int i=0;i<size; i++){
					Map taskInfo 		= (Map)taskInfoList.get(i);
					String taskSID 		= (String)taskInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_SOURCE_ID);
					String projectSID 	= (String)taskInfo.get(Experiment.SELECT_PROJECT_SOURCE_ID);

					StringBuilder htmlLink = new StringBuilder();

					if(projectSID != null && !projectSID.isEmpty()){
						Map<String,String> projectTaskSIDMap = (Map<String, String>) masterTaskInfo.get(projectSID);

						if(projectTaskSIDMap == null){
							htmlLink.append("");
						}else{
							String masterTaskId = projectTaskSIDMap.get(taskSID);

							if(masterTaskId == null || masterTaskId.isEmpty()){
								htmlLink.append("");
							}else{
								htmlLink.append("<a href ='javascript:showModalDialog(\"");
								htmlLink.append("../common/emxTree.jsp?objectId=");
								htmlLink.append(masterTaskId);
								htmlLink.append("\", \"875\", \"550\", \"false\", \"popup\")' title=\""+toolTipMasterTask+"\">");
								htmlLink.append("<img src=\"../common/images/iconSmallRFLPLMFlowReference.gif\" border=\"0\" />");
								htmlLink.append("</a>");
							}
						}
					}else{
						htmlLink.append("");
					}

					taskList.addElement(htmlLink.toString());
				}

			}else{
				for(int i=0;i<size; i++){
					taskList.addElement(EMPTY_STRING);
				}
			}

		}else{
			for(int i=0;i<size; i++){
				taskList.addElement(EMPTY_STRING);
			}
		}

		long end = System.currentTimeMillis();
		System.out.println("Parent task link pro::"+(end-start));

		return taskList;
	}

	/**
	 * Exclude Experiment project from search timesheet and project list page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return list of project except experiment.
	 * @throws Exception, if operation fails.
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeExperimentProject(Context context,String[]args)throws Exception
	{
		StringList slFinalList = new StringList();
		try{
			StringList slSelect = new StringList(2);
			slSelect.addElement(DomainObject.SELECT_ID);
			slSelect.add(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);

			String typePatternPrj = ProgramCentralConstants.TYPE_EXPERIMENT;

			MapList mpProjectList = DomainObject.findObjects(context,
					typePatternPrj,
					null,
					null,
					slSelect);                                   

			for (Iterator iterator = mpProjectList.iterator(); iterator.hasNext();) {
				Map projectInfoMap = (Map) iterator.next();
				String experimentId = (String)projectInfoMap.get(DomainObject.SELECT_ID);				
				String isExpProject = (String)projectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);

				if(isExpProject.equalsIgnoreCase("TRUE")){
					slFinalList.addElement(experimentId);
				}
			}

			return slFinalList;
		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * Hide Experiment task from comman search.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return exclude task list.
	 * @throws Exception, if operation fails.
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList hideExperimentTask(Context context,String[]args)throws Exception
	{
		StringList excludeList = new StringList();

		try{

			StringList slSelect = new StringList();
			slSelect.addElement(DomainObject.SELECT_ID);

			StringBuilder whereClause =  new StringBuilder();
			whereClause.append("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type" + "=='" + ProgramCentralConstants.TYPE_EXPERIMENT +"'");

			MapList mlList = DomainObject.findObjects(context, ProgramCentralConstants.TYPE_TASK_MANAGEMENT, null, whereClause.toString(), slSelect);

			for (Object objectMap : mlList) {
				Map<String,String> taskMap = (Map<String,String>)objectMap;
				String taskId = taskMap.get(DomainObject.SELECT_ID);

				if(ProgramCentralUtil.isNotNullString(taskId)){
					excludeList.addElement(taskId);
				}
			}

			return excludeList;

		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 * Get experiment wbs task.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return task list.
	 * @throws Exception, if operation fails.
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getExperimentWBSSubtasks(Context context, String[] args) throws Exception
	{
		try{
			Map programMap 		= (Map) JPO.unpackArgs(args);
			String objectId 	= (String)programMap.get("objectId");
			String expandLevel 	= (String)programMap.get("expandLevel");
			short recurLevel 	= ProgramCentralUtil.getExpandLevel(expandLevel); 

			MapList expandList = experiment.getWBSObjectList(context, objectId, recurLevel);
			updateListWithDupId(context,expandList);

			expandList.sort(DUPLICATE_ID, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_INTEGER);

			return expandList;

		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}

	}



	/**
	 * Put new key in the map for identify in the compare structure.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param mlTaskList are list of task.
	 * @throws Exception, if operation fails.
	 */
	private static void updateTaskMapList(Context context,MapList mlTaskList)throws Exception
	{
		boolean isContainExpandMultiLevelJPO = false;
		StringList slSelectable = new StringList();
		slSelectable.addElement(DomainObject.SELECT_ID);
		slSelectable.addElement(SELECT_PARENT_TASK_ID);

		String []strTaskIDs = new String[mlTaskList.size()];
		int cnt = 0;
		Iterator taskIterator1 = mlTaskList.iterator();
		while(taskIterator1.hasNext()){
			Map objectMap = (Map)taskIterator1.next();
			if(!objectMap.containsKey((EXPAND_MULTI_LEVELS_JPO))){
				String strTaskId = (String)objectMap.get(DomainObject.SELECT_ID);
				strTaskIDs[cnt] = strTaskId;
				cnt++;
			}
		}
		if(mlTaskList.size()>0){
			int index=0;
			for(int ind = 0;ind<mlTaskList.size();ind++){
				Map objectMap = (Map)mlTaskList.get(ind);
				if(objectMap.containsKey((EXPAND_MULTI_LEVELS_JPO))){
					index = ind;
					isContainExpandMultiLevelJPO = true;
					break;
				}
			}
			if(index != 0)
				mlTaskList.remove(index);
		}
		MapList mlList = new MapList();
		if(mlTaskList.size()>0 && !isContainExpandMultiLevelJPO){
			mlList = DomainObject.getInfo(context, strTaskIDs, slSelectable);
		}else{
			if(isContainExpandMultiLevelJPO){
				mlTaskList.remove(0);
			}
		}
		for(int i=0;i<mlList.size();i++){
			Map mpTask = (Map)mlList.get(i);
			Map mpTask1 = (Map)mlTaskList.get(i);
			String strTaskId = (String)mpTask.get(DomainObject.SELECT_ID);
			String strMasterTaskId = (String)mpTask.get(SELECT_PARENT_TASK_ID);
			String strTaskId1 = (String)mpTask1.get(DomainObject.SELECT_ID);
			if(strMasterTaskId!=null){
				if(strTaskId.equals(strTaskId1)){
					mpTask1.put(PARENT_TASK_ID, strMasterTaskId);
				}
			}else{
				mpTask1.put(PARENT_TASK_ID, strTaskId);
			}
		}
	}

	/**
	 * Sort mapList and put new parameter according to task seq.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param mlTaskList are list of task.
	 * @throws Exception, if operation fails.
	 */
	private void updateListWithDupId(Context context,MapList mlTaskList)throws Exception
	{
		try{
			COUNT = 1;
			if(mlTaskList.size()>0){
				int index=0;
				for(int ind = 0;ind<mlTaskList.size();ind++){
					Map objectMap = (Map)mlTaskList.get(ind);
					if(objectMap.containsKey((EXPAND_MULTI_LEVELS_JPO))){
						index = ind;
						break;
					}
				}
				if(index != 0)
					mlTaskList.remove(index);
			}

			mlTaskList.sort(DomainObject.SELECT_LEVEL,ProgramCentralConstants.ASCENDING_SORT, "integer");
			MapList mlFirstLevelTaskList = new MapList();
			MapList mlOtherTaskList = new MapList();
			for(int i=0;i<mlTaskList.size();i++){
				Map taskMap = (Map)mlTaskList.get(i);
				if(!taskMap.containsKey((EXPAND_MULTI_LEVELS_JPO))){
					String strLevel = (String)taskMap.get(DomainObject.SELECT_LEVEL);
					if(strLevel.equals(LEVEL)){
						mlFirstLevelTaskList.add(taskMap); 
					}else{
						mlOtherTaskList.add(taskMap); 
					}
				}
			}
			mlFirstLevelTaskList.sort(SubtaskRelationship.SELECT_SEQUENCE_ORDER, ProgramCentralConstants.ASCENDING_SORT,"integer");
			if(!mlFirstLevelTaskList.isEmpty() && mlFirstLevelTaskList.size()>0){
				for(int i=0;i<mlFirstLevelTaskList.size();i++){
					Map mapTask1 = (Map)mlFirstLevelTaskList.get(i);
					mapTask1.put(DUPLICATE_ID, Integer.toString(COUNT));
					String strTaskId = (String)mapTask1.get(DomainObject.SELECT_ID);
					updateSubListWithDupId(context,strTaskId,mlTaskList,mlOtherTaskList);
					COUNT++;
				}
			}

		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Sort subMaplist according to key.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param mlOtherTaskList are list of task.
	 * @throws Exception, if operation fails.
	 */
	private void updateSubListWithDupId(Context context,String sTaskId,MapList mlTaskList,MapList mlOtherTaskList)
	{
		MapList list = new MapList();
		for(int i=0;i<mlOtherTaskList.size();i++){
			String childId=DomainObject.EMPTY_STRING;
			Map mpTask = (Map)mlOtherTaskList.get(i);
			String parentIds = (String)mpTask.get("from.id");

			if(parentIds.equals(sTaskId)){
				list.add(mpTask);
			}
		}
		if(list!=null && !list.isEmpty()&& list.size()>0)
			list.sort(SubtaskRelationship.SELECT_SEQUENCE_ORDER,ProgramCentralConstants.ASCENDING_SORT,"integer");

		if(list.size()>1){
			for(int i=0;i<list.size();i++){
				COUNT++;
				Map task1 = (Map)list.get(i);
				task1.put(DUPLICATE_ID,Integer.toString(COUNT));
				String strTaskId = (String)task1.get(DomainObject.SELECT_ID);
				updateSubListWithDupId(context,strTaskId,mlTaskList,mlOtherTaskList);
			}
		}else{
			for(int i=0;i<list.size();i++){
				COUNT++;
				Map mpTask1 = (Map)list.get(i);
				mpTask1.put(DUPLICATE_ID,Integer.toString(COUNT));
				String strTaskId = (String)mpTask1.get(DomainObject.SELECT_ID);
				updateSubListWithDupId(context,strTaskId,mlTaskList,mlOtherTaskList);
			}
		}
	}

	/**
	 * Experiment task is editable or not.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return string list.
	 * @throws Exception, if operation fails.
	 */
	public StringList isTaskNameEditable(Context context,String[]args)throws Exception
	{
		StringList slTaskNameEditable = new StringList();
		Map programMap = JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		String []strTaskIDs = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			String strTaskId = (String)objectMap.get(DomainObject.SELECT_ID);
			strTaskIDs[i] = strTaskId;
		}

		StringList slSelect = new StringList(4);
		slSelect.addElement(SELECT_PARENT_TASK_ID);
		slSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		slSelect.addElement(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
		slSelect.addElement(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);


		DomainObject object = DomainObject.newInstance(context);
		MapList mlParentInfo = object.getInfo(context, strTaskIDs, slSelect);

		Iterator mapListIterator = mlParentInfo.iterator();
		while (mapListIterator.hasNext()){
			Map mpTask = (Map) mapListIterator.next();

			String strParentTaskId = (String) mpTask.get(SELECT_PARENT_TASK_ID);
			String isProjectSpace = (String)mpTask.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isExpProject = (String)mpTask.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
			String isTaskManagement = (String)mpTask.get(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);


			if("true".equalsIgnoreCase(isProjectSpace) || "true".equalsIgnoreCase(isExpProject)){
				slTaskNameEditable.addElement(true);
			}else if("true".equalsIgnoreCase(isTaskManagement) && strParentTaskId!=null && !strParentTaskId.isEmpty()){
				slTaskNameEditable.addElement(false);
			}else{
				slTaskNameEditable.addElement(true);
			}
		}
		return slTaskNameEditable;
	}

	/**
	 * Check for project state if project has complete or archive or cancle state then 
	 * experiment create command should not be visible.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return True if Create Experiment operation is allowed.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToCreateExperiment(Context context,String[]args)throws Exception{
		boolean hasAccess = false;
		Map programMap =  JPO.unpackArgs(args);
		String sProjectId = (String)programMap.get("objectId");
		DomainObject project = DomainObject.newInstance(context,sProjectId);

		hasAccess = project.checkAccess(context, (short) AccessConstants.cModify) && 
				ProgramCentralUtil.checkContextRole(context, ProgramCentralConstants.ROLE_PROJECT_LEAD);
		String sProjectState  = project.getInfo(context, DomainObject.SELECT_CURRENT);		
		if(sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_REVIEW_COMPLETE)
				|| sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_REVIEW_ARCHIEVE)
				|| sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL)){
			hasAccess = false;
		}
		return hasAccess;
	}

	/**
	 * Checks if the context user is allowed to Delete Experiment. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return True if Delete Experiment operation is allowed.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToDeleteExperiment(Context context,String[]args)throws Exception{
		boolean hasAccess = false;
		Map programMap =  JPO.unpackArgs(args);
		String sProjectId = (String)programMap.get("objectId");
		DomainObject project = DomainObject.newInstance(context,sProjectId);

		hasAccess = project.checkAccess(context, (short) AccessConstants.cModify) && 
				ProgramCentralUtil.checkContextRole(context, ProgramCentralConstants.ROLE_PROJECT_LEAD);

		String sProjectState  = project.getInfo(context, DomainObject.SELECT_CURRENT);		
		if(sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_REVIEW_COMPLETE)
				|| sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_REVIEW_ARCHIEVE)
				|| sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL)){
			hasAccess = false;
		}
		return hasAccess;
	}

	/**
	 * Checks if the context user is allowed to modify Experiment. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return True if Modify Experiment operation is allowed.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToModifyExperiment(Context context,String[]args)throws Exception{
		boolean hasAccess = false;
		Map programMap =  JPO.unpackArgs(args);
		String sProjectId = (String)programMap.get("objectId");
		DomainObject project = DomainObject.newInstance(context,sProjectId);

		hasAccess = project.checkAccess(context, (short) AccessConstants.cModify) && 
				ProgramCentralUtil.checkContextRole(context, ProgramCentralConstants.ROLE_PROJECT_LEAD);

		String sProjectState  = project.getInfo(context, DomainObject.SELECT_CURRENT);		
		if(sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_REVIEW_COMPLETE)
				|| sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_REVIEW_ARCHIEVE)
				|| sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL)){
			hasAccess = false;
		}
		return hasAccess;
	}

	/**
	 * Check for project state if project has complete or archive or cancle state then 
	 * experiment create command should not be visible.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToCreateOrCompareExperiment(Context context,String[]args)throws Exception{
		boolean hasAccess = false;
		Map programMap =  JPO.unpackArgs(args);
		String sProjectId = (String)programMap.get("objectId");
		DomainObject project = DomainObject.newInstance(context,sProjectId);

		hasAccess = project.checkAccess(context, (short) AccessConstants.cModify) && 
				ProgramCentralUtil.checkContextRole(context, ProgramCentralConstants.ROLE_PROJECT_LEAD);

		String sProjectState  = project.getInfo(context, DomainObject.SELECT_CURRENT);
		if(sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_REVIEW_COMPLETE)
				|| sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_REVIEW_ARCHIEVE)
				|| sProjectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL)){
			hasAccess = false;
		}
		return hasAccess;
	}

	/**
	 * Get duplicate Id of Task.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return duplicate id.
	 * @throws Exception if operation fails.
	 */
	public StringList getDuplicateId(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList dupIdList = new StringList(objectList.size());

		Iterator objectListIterator = objectList.iterator();
		while (objectListIterator.hasNext()){
			Map objectMap = (Map) objectListIterator.next();
			String strDupId = (String)objectMap.get("dupId");
			String strTaskLevel = (String)objectMap.get("level");
			String isRootNode = (String)objectMap.get("Root Node");
			if(strDupId !=null)
				dupIdList.addElement(strDupId);
			else{
				if(strTaskLevel!=null && strTaskLevel.equalsIgnoreCase("0")&&
						isRootNode !=null && isRootNode.equalsIgnoreCase("true")){
					dupIdList.addElement(strTaskLevel);
				}
			}
		}

		return dupIdList;
	}

	/**
	 * Display name of Task Assignees.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return List of assignee names.
	 * @throws Exception if operation fails.
	 */
	public StringList getAssigneeName(Context context,String[]args)throws Exception
	{
		Map assigneeInfo = new HashMap();
		StringList slAssigneeNameList = new StringList();

		Map programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		DomainObject.MULTI_VALUE_LIST.add(SELECT_ASSIGNEE_NAMES);
		DomainObject.MULTI_VALUE_LIST.add(SELECT_ASSIGNEE_IDS);
		DomainObject.MULTI_VALUE_LIST.add(SELECT_ASSIGNEE_FIRSTNAME);
		DomainObject.MULTI_VALUE_LIST.add(SELECT_ASSIGNEE_LASTNAME);

		StringList busSelects = new StringList();
		busSelects.add(SELECT_ASSIGNEE_FIRSTNAME);
		busSelects.add(SELECT_ASSIGNEE_LASTNAME);
		busSelects.add(SELECT_ASSIGNEE_NAMES);
		busSelects.add(SELECT_ASSIGNEE_IDS);

		String []assigneeIdArray = new String[objectList.size()];

		for(int i=0;i<objectList.size();i++){
			Map assigneeMap = (Map)objectList.get(i);
			assigneeIdArray[i] = (String)assigneeMap.get(DomainObject.SELECT_ID);
		}
		MapList mlAssigneeList = DomainObject.getInfo(context, assigneeIdArray, busSelects);

		for(int i=0;i<mlAssigneeList.size();i++){
			String strAssigneeName = DomainObject.EMPTY_STRING;
			Map mpAssignee = (Map)mlAssigneeList.get(i);

			Object objectUserName = mpAssignee.get(SELECT_ASSIGNEE_NAMES);
			Object objectFirstName = mpAssignee.get(SELECT_ASSIGNEE_FIRSTNAME);
			Object objectLastName = mpAssignee.get(SELECT_ASSIGNEE_LASTNAME);

			if(objectUserName instanceof StringList){
				StringList slAssigneeUserName = (StringList)objectUserName;
				StringList slAssigneeFirstName = (StringList)objectFirstName;
				StringList slAssigneeLastName = (StringList)objectLastName;

				for(int j=0;j<slAssigneeUserName.size();j++){
					String strAssigneeFName = (String)slAssigneeFirstName.get(j);
					String strAssigneeLName = (String)slAssigneeLastName.get(j);
					strAssigneeName += strAssigneeLName+ProgramCentralConstants.COMMA+strAssigneeFName+";";
				}
				strAssigneeName = strAssigneeName.substring(0, strAssigneeName.length()-1);
			}else{
				if(objectUserName != null)
					strAssigneeName = (String)objectLastName +ProgramCentralConstants.COMMA+ (String)objectFirstName;
			}
			slAssigneeNameList.addElement(strAssigneeName);
		}

		StringList slFinalAssigneeNameList = new StringList();
		for(int i=0;i<slAssigneeNameList.size();i++){
			String strAssigneeName = DomainObject.EMPTY_STRING;
			String strAssigneeNames = (String)slAssigneeNameList.get(i);

			if(UIUtil.isNotNullAndNotEmpty(strAssigneeNames)){
				String []assigneeNameArry = strAssigneeNames.split(";");
				Arrays.sort(assigneeNameArry);
				for(int j=0;j<assigneeNameArry.length;j++){
					strAssigneeName += assigneeNameArry[j] + "; ";
				}
			}

			slFinalAssigneeNameList.addElement(strAssigneeName);
		}
		return slFinalAssigneeNameList;
	}

	/**
	 * Add sync assignee to the master project Task or vice versa.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @throws Exception if operation fails.
	 */
	public void syncAssignee(Context context,String[]args)throws Exception
	{
		Map inputMap = (HashMap)JPO.unpackArgs(args);
		Map paramMap = (HashMap) inputMap.get("paramMap");
		String newAssigneeNames = (String) paramMap.get("New Value");
		String objectId = (String) paramMap.get("objectId");

		if(UIUtil.isNotNullAndNotEmpty(newAssigneeNames)){
			removeTaskAssignee(context,objectId);
			addAssignee(context,objectId,newAssigneeNames);
		}else{
			removeTaskAssignee(context,objectId);
		}
	}

	/**
	 * Add assignee in selected task.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param strTaskId is a task id.
	 * @param newAssigneeNames are name of assignees.
	 * @throws Exception if operation fails.
	 */
	private static void addAssignee(Context context,String strTaskId,String newAssigneeNames)throws Exception
	{
		DomainObject taskObject = DomainObject.newInstance(context,strTaskId);

		String busWhere = DomainObject.EMPTY_STRING;

		StringList busSelects = new StringList();
		busSelects.add(Person.SELECT_ID);
		busSelects.add(Person.SELECT_FIRST_NAME);
		busSelects.add(Person.SELECT_LAST_NAME);
		busSelects.add(Person.SELECT_NAME);

		if(UIUtil.isNotNullAndNotEmpty(newAssigneeNames)){
			String[] assigneeNameArray	=	newAssigneeNames.split(";");

			for(int i=0;i<assigneeNameArray.length;i++){
				String strAssigneeName = assigneeNameArray[i];
				String[] assigneeFLNames = strAssigneeName.split(ProgramCentralConstants.COMMA);
				String sAssigneeLName = assigneeFLNames[0].trim();
				String sAssigneeFName = assigneeFLNames[1].trim();

				if(i==0){
					busWhere = "("+Person.SELECT_FIRST_NAME+"=='" + sAssigneeFName + "' && "+Person.SELECT_LAST_NAME+"=='" +sAssigneeLName+ "'"+")";
				}else{
					busWhere += " || (" +Person.SELECT_FIRST_NAME+"=='" + sAssigneeFName + "' &&"+Person.SELECT_LAST_NAME+"=='" + sAssigneeLName + "'"+")";
				}
			}

			MapList mlAssigneeList = DomainObject.findObjects(context, DomainObject.TYPE_PERSON, null, busWhere, busSelects);

			for(int i=0;i<mlAssigneeList.size();i++){
				Map assigneeMap = (Map)mlAssigneeList.get(i);
				String strAssigneeId = (String)assigneeMap.get(DomainObject.SELECT_ID);

				DomainObject assigneeObject = DomainObject.newInstance(context,strAssigneeId);
				DomainRelationship connection = DomainRelationship.connect(context,assigneeObject,
						ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS,taskObject);
			}
		}
	}

	/**
	 * Remove all assignee of Task.
	 * @param context the eMatrix <code>Context</code> object.  
	 * @param strTaskId is a task id.
	 * @throws Exception if operation fails.
	 */
	private static void removeTaskAssignee(Context context,String strTaskId)throws Exception
	{
		Task task  = new Task(strTaskId);
		DomainObject taskObject = DomainObject.newInstance(context,strTaskId);
		MapList mlTaskAssigneeList = getTaskAssignee(context,strTaskId);

		if(mlTaskAssigneeList.size()>0){
			for(int i=0;i<mlTaskAssigneeList.size();i++){
				Map taskAssignee = (Map)mlTaskAssigneeList.get(i);
				String strAssigneeId = (String)taskAssignee.get(DomainObject.SELECT_ID);
				String strAssigneeRelId = (String)taskAssignee.get(Person.SELECT_RELATIONSHIP_ID);
				DomainRelationship.disconnect(context, strAssigneeRelId);
			}
		}
	}

	/**
	 * Get Assignee details.
	 * @param context the eMatrix <code>Context</code> object.  
	 * @param strTaskId is a Task id. 
	 * @return list of assignee details.
	 * @throws Exception, if operation fails.
	 */
	private static MapList getTaskAssignee(Context context,String strTaskId)throws FrameworkException
	{
		Task task  = new Task();
		task.setId(strTaskId);

		StringList busSelects = new StringList(2);
		busSelects.add(Person.SELECT_ID);
		busSelects.add(Person.SELECT_NAME);

		StringList relSelects = new StringList(4);
		relSelects.add(Person.SELECT_RELATIONSHIP_ID);
		relSelects.add(SELECT_PERCENT_ALLOCATION);
		relSelects.add(SELECT_ASSIGNED_DATE);
		relSelects.add(SELECT_ASSIGNEE_ROLE);

		MapList mlAssigneeList = task.getAssignees(context, busSelects, relSelects, null);

		return mlAssigneeList;
	}

	/**
	 * Get dynamic column for originator name.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects. 
	 * @return list of column map.
	 * @throws Exception if operation fails.
	 */
	public MapList getDynamicOriginetorNameColumn(Context context,String[]args)throws Exception
	{
		MapList mlColumnList = new MapList();
		Map columnMap = new HashMap();
		Map colSettingMap = new HashMap();

		String sColumnHeading = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Experiment.Creator", context.getSession().getLanguage());

		colSettingMap.put("Column Type","program");
		colSettingMap.put("Registered Suite","ProgramCentral");
		colSettingMap.put("Editable","false");
		colSettingMap.put("Export","true");
		colSettingMap.put("Sortable","false");
		colSettingMap.put("program","emxWhatIf");
		colSettingMap.put("function","getOriginatorName");	
		colSettingMap.put("Style Program","emxWhatIf");
		colSettingMap.put("Style Function","getColumnStyleInfo");
		colSettingMap.put("Field Type","attribute");

		columnMap.put("name","OriginatorColumn");
		columnMap.put("label", sColumnHeading);
		columnMap.put("settings", colSettingMap);

		mlColumnList.add(columnMap);

		return mlColumnList;
	}

	/**
	 * Get color style for originator name column. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return list of color style for column.
	 * @throws Exception if operation fails.
	 */
	public StringList getColumnStyleInfo(Context context,String[]args)throws Exception
	{
		StringList slStyleInfoList = new StringList();

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		String strContextPersonName = context.getUser();

		String []expIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			String strExpId = (String)objectMap.get(DomainObject.SELECT_ID);
			expIdArray[i] = strExpId;
		}

		StringList slSelect = new StringList(2);
		slSelect.addElement(DomainObject.SELECT_TYPE);
		slSelect.addElement(DomainObject.SELECT_ORIGINATOR);
		slSelect.addElement(DomainObject.SELECT_OWNER);
		slSelect.addElement(Experiment.IS_PROJECT_USED_AS_SUBPROJECT);

		DomainObject object = DomainObject.newInstance(context);
		MapList mlParentInfo = object.getInfo(context, expIdArray, slSelect);
		if(mlParentInfo != null && !mlParentInfo.isEmpty()){
			for(int i=0;i<mlParentInfo.size();i++){
				Map <String,String>parentMap  = (Map)mlParentInfo.get(i);
				String strUsed = parentMap.get(Experiment.IS_PROJECT_USED_AS_SUBPROJECT);
				String strType = parentMap.get(DomainObject.SELECT_TYPE);
				String strOriginatorName = parentMap.get(DomainObject.SELECT_ORIGINATOR);
				String strOwnerName = parentMap.get(DomainObject.SELECT_OWNER);

				if((ProgramCentralUtil.isNotNullString(strUsed)) && !strType.equalsIgnoreCase(ProgramCentralConstants.TYPE_PROJECT_SPACE)){
					slStyleInfoList.addElement("ResourcePlanningYellowBackGroundColor");
				}else if(!strContextPersonName.equalsIgnoreCase(strOwnerName) && !strContextPersonName.equalsIgnoreCase(strOriginatorName) && !strType.equalsIgnoreCase(ProgramCentralConstants.TYPE_PROJECT_SPACE)){
					slStyleInfoList.addElement("ResourcePlanningYellowBackGroundColor");
				}else {
					slStyleInfoList.addElement(DomainObject.EMPTY_STRING);
				}

			}
		}

		return slStyleInfoList;
	}

	/**
	 * Get Originator name.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return Originator name list.
	 * @throws Exception if operation fails.
	 */
	public StringList getOriginatorName(Context context,String[]args)throws Exception
	{
		StringList slOriginatorNameList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		String []expIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			String strExpId = (String)objectMap.get(DomainObject.SELECT_ID);
			expIdArray[i] = strExpId;
		}

		StringList slSelect = new StringList(2);
		slSelect.addElement(DomainObject.SELECT_ORIGINATOR);

		DomainObject object = DomainObject.newInstance(context);
		MapList mlParentInfo = object.getInfo(context, expIdArray, slSelect);
		if(mlParentInfo != null && !mlParentInfo.isEmpty()){
			for(int i=0;i<mlParentInfo.size();i++){
				Map <String,String>parentMap  = (Map)mlParentInfo.get(i);
				String strOriginatorName = parentMap.get(DomainObject.SELECT_ORIGINATOR);
				String strOriginatorFullName = PersonUtil.getFullName(context, strOriginatorName);
				slOriginatorNameList.addElement(strOriginatorFullName);
			}
		}
		return slOriginatorNameList;
	}

	/**
	 * Get state of project and project task for specific language.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return state list.
	 * @throws Exception if operation fails.
	 */
	public StringList getStateNames(Context context,String[]args)throws Exception
	{
		StringList slStateNameList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		String []objectIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			String strExpId = (String)objectMap.get(DomainObject.SELECT_ID);
			objectIdArray[i] = strExpId;
		}

		StringList slSelect = new StringList(2);
		slSelect.addElement(DomainObject.SELECT_CURRENT);
		slSelect.addElement(DomainObject.SELECT_POLICY);

		DomainObject object = DomainObject.newInstance(context);
		MapList mlObjectInfo = object.getInfo(context, objectIdArray, slSelect);

		if(mlObjectInfo != null && mlObjectInfo.size()>0){
			String lang = context.getSession().getLanguage();
			int size = mlObjectInfo.size();

			for(int i=0;i<size;i++){
				Map <String,String>objectMap  = (Map)mlObjectInfo.get(i);
				String sState = objectMap.get(DomainObject.SELECT_CURRENT);
				String sPolicy = objectMap.get(DomainObject.SELECT_POLICY);

				sState = i18nNow.getStateI18NString(sPolicy,sState, lang);

				slStateNameList.addElement(sState);
			}
		}

		return slStateNameList;
	}

	/**
	 * Get type of project and project task for specific language.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return type list.
	 * @throws Exception if operation fails.
	 */
	public StringList getType(Context context,String[]args)throws Exception
	{
		StringList slTypeList = new StringList();
		Map programMap = JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		String lang = context.getSession().getLanguage();

		String []objectIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			String strExpId = (String)objectMap.get(DomainObject.SELECT_ID);
			objectIdArray[i] = strExpId;
		}

		StringList slSelect = new StringList(1);
		slSelect.addElement(DomainObject.SELECT_TYPE);

		DomainObject object = DomainObject.newInstance(context);
		MapList mlObjectInfo = object.getInfo(context, objectIdArray, slSelect);

		if(mlObjectInfo != null && mlObjectInfo.size()>0){

			for(int i=0;i<mlObjectInfo.size();i++){
				String i18nTypeName = DomainObject.EMPTY_STRING;
				Map <String,String>objectMap  = (Map)mlObjectInfo.get(i);

				String sType = objectMap.get(DomainObject.SELECT_TYPE);
				if(sType.equalsIgnoreCase(ProgramCentralConstants.TYPE_EXPERIMENT)){
					i18nTypeName = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Experiment.Type", lang);
				}else{
					i18nTypeName = i18nNow.getTypeI18NString(sType,lang);
				}
				slTypeList.addElement(i18nTypeName);
			}
		}
		return slTypeList;
	}

	/**
	 * Enable Change process management feature.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args hold information about object.
	 * @return boolean value
	 * @throws Exception if operation fails.
	 */
	public boolean enableChangeProcessManagementChannel(Context context,String[]args)throws Exception
	{
		boolean enableChannel = false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String projectId = (String) programMap.get("objectId");
		if(ProgramCentralUtil.isNullString(projectId)){
			projectId = (String) programMap.get("parentOID");
		}
		enableChannel = experiment.enableApprovalProcess(context, projectId);
		return enableChannel;
	}

	/**
	 * Display Experiment Issue List.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return Issue list.
	 * @throws Exception if operation fails.
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getExpIssues(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		String projectId = (String) programMap.get("objectId");

		return experiment.getExperimentIssueList(context, projectId);
	}

	/**
	 * Display Experiment Issue name.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return Issue name list.
	 * @throws Exception if operation fails.
	 */
	public StringList getIssueName(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList issueNameList = new StringList(objectList.size());

		Iterator objectListIterator = objectList.iterator();
		while (objectListIterator.hasNext()){
			Map objectMap = (Map) objectListIterator.next();
			String issueName = (String) objectMap.get(DomainObject.SELECT_NAME);
			issueNameList.addElement(issueName);
		}

		return issueNameList;
	}

	/**
	 * Display experiment name with hyperlink.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return experiment name list.
	 * @throws Exception If operation fails.
	 */
	public StringList getRouteExpName(Context context,String[]args)throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		Map paramList = (Map) programMap.get("paramList");
		MapList objectList = (MapList) programMap.get("objectList");
		String projectId = (String) paramList.get("objectId");

		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map issueMap = (Map)objectList.get(i);
			String issueId = (String)issueMap.get(DomainObject.SELECT_ID);
			issueIdArray[i] = issueId;
		}

		return experiment.getExperimentName(context, projectId, issueIdArray);
	}

	/**
	 * Display route image with tool tip as route name .
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return route image list.
	 * @throws Exception If operation fails.
	 */
	public StringList getRouteName(Context context,String[]args)throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map issueMap = (Map)objectList.get(i);
			String issueId = (String)issueMap.get(DomainObject.SELECT_ID);
			issueIdArray[i] = issueId;
		}

		return experiment.getRouteImage(context, issueIdArray);

	}

	/**
	 * Get Project member.
	 * @param context The ENOVIA <code>Context</code> object. 
	 * @param projectId is a project space object id.
	 * @return project member list.
	 * @throws Exception if operation fails.
	 */
	public MapList getProjectMember(Context context,String projectId)throws Exception
	{
		StringList selects = new StringList(1);
		selects.add(DomainObject.SELECT_ID);
		selects.add(DomainObject.SELECT_NAME);

		StringList relSelects = new StringList(2);
		relSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
		relSelects.add(MemberRelationship.SELECT_PROJECT_ACCESS);

		ProjectSpace objectProjectSpace = new ProjectSpace(projectId);
		MapList projectMemeberMapList =  objectProjectSpace.getMembers(context,selects,relSelects,DomainObject.EMPTY_STRING,DomainObject.EMPTY_STRING);

		return projectMemeberMapList;
	}
	/**
	 * Get project member for approver.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args hold information about object.
	 * @return project member list.
	 * @throws Exception if operation fails.
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeProjectMemberForApprover(Context context,String[]args)throws Exception
	{
		StringList slMemberList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strProjectId = (String) programMap.get("objectId");

		MapList projectMemeberMapList =  getProjectMember(context,strProjectId);

		for(int i=0;i<projectMemeberMapList.size();i++){
			Map <String,String>memberMap = (Map)projectMemeberMapList.get(i);

			String strMemberId = memberMap.get(DomainObject.SELECT_ID);
			slMemberList.addElement(strMemberId);
		}

		return slMemberList;
	}

	/**
	 * Get project member for assignee for solution.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args hold information about object.
	 * @return project member list.
	 * @throws Exception if operation fails.
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeProjectMemberForAssignee(Context context,String[]args)throws Exception
	{
		StringList slMemberList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strProjectId = (String) programMap.get("objectId");

		Map<String,Map> projectMemberAccessBitMap = experiment.getProjectOwnershipVector(context,strProjectId);
		MapList projectMemebersList =  getProjectMember(context,strProjectId);

		for(int i=0;i<projectMemebersList.size();i++){
			Map <String,String>projectMemeberInfo = (Map)projectMemebersList.get(i);
			String memberName = projectMemeberInfo.get(DomainObject.SELECT_NAME);
			String memberId = projectMemeberInfo.get(DomainObject.SELECT_ID);
			
			Map<String,String>accessInfo = projectMemberAccessBitMap.get(memberName);
			String memberAccessKey = accessInfo.get(DomainAccess.KEY_ACCESS_GRANTED);
			String []memberAccessbitArray 	= memberAccessKey.split("\\,");
			
			List memberAccessbitList = Arrays.asList(memberAccessbitArray);

			if(ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD.equalsIgnoreCase(memberAccessKey)){
				slMemberList.addElement(memberId);
			}
		}

		return slMemberList;
	}

	/**
	 * Display Experiment project which is created by context user.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args hold information about object.
	 * @return List of experiment project.
	 * @throws Exception if operation fails.
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeExperimentProject(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		String projectId = (String) programMap.get("objectId");
		DomainObject object = DomainObject.newInstance(context, projectId);

		String strType = object.getInfo(context, "type.kindof["+ProgramCentralConstants.TYPE_PROJECT_SPACE+"]");
		if(!("TRUE".equalsIgnoreCase(strType))){
			projectId = (String) programMap.get("rootObjectId");
		}

		return experiment.getContextUserExperimentProjects(context, projectId);
	}

	/**
	 * Create issue with route and connect with project space.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args hold information about object.
	 * @throws Exception if operation fails.
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createIssueWithRoute(Context context,String[]args)throws Exception
	{
		try{
			ContextUtil.startTransaction(context, true);
			Map programMap = JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");
			Map paramMap = (Map)programMap.get("paramMap");
			Locale locale = context.getLocale();
			Map attributeMap = new HashMap();
			Map <String,String> detailsMap = new HashMap();

			//Get Form value
			String strDescription   = (String)requestMap.get("Description");
			String strExpApproverId = (String)requestMap.get("ApproverOID");
			String strExpAssigneeId = (String)requestMap.get("AssigneeOID");
			String strExpAsSolution = (String)requestMap.get("AddExistingExprimentOID");
			String strEstStartDate  = (String)requestMap.get("EstimatedStartDate");
			String strEstFinishDate = (String)requestMap.get("EstimatedFinishDate");
			String strIssuePriority = (String)requestMap.get("Priority");
			String strIssuePolicy = (String)requestMap.get("Policy");
			String strVault = (String)requestMap.get("Vault");
			String strProblemType = (String)requestMap.get("ProblemType");
			String strTimeZone = (String)requestMap.get("timeZone");
			String strActionRequired = (String)requestMap.get("ActionRequired");
			String routeTemplateId = (String)requestMap.get("RouteTemplate");
			double dClientTimeZoneOffset = (new Double(strTimeZone)).doubleValue();

			//Get new object Id
			String strNewIssueObjectId = (String)paramMap.get("newObjectId");
			String strProjectId = (String)requestMap.get("parentOID");
			if(ProgramCentralUtil.isNullString(strProjectId)){
				strProjectId = (String)requestMap.get("objectId");
			}

			if(ProgramCentralUtil.isNotNullString(strEstStartDate)){
				strEstStartDate = strEstStartDate.trim();
				strEstStartDate = eMatrixDateFormat.getFormattedInputDate(context,strEstStartDate,dClientTimeZoneOffset,locale);
			}
			if(ProgramCentralUtil.isNotNullString(strEstFinishDate)){
				strEstFinishDate = strEstFinishDate.trim();
				strEstFinishDate = eMatrixDateFormat.getFormattedInputDate(context,strEstFinishDate,dClientTimeZoneOffset,locale);
			}

			if(ProgramCentralUtil.isNullString(strVault)){
				strVault = ProjectSpace.DEFAULT_VAULTS;
			}

			attributeMap.put(Issue.ATTRIBUTE_ESTIMATED_START_DATE, strEstStartDate);
			attributeMap.put(Issue.ATTRIBUTE_ESTIMATED_END_DATE, strEstFinishDate);
			attributeMap.put(PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_attribute_Priority), strIssuePriority);
			attributeMap.put(PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_attribute_ProblemType), strProblemType);

			detailsMap.put("IssueId", strNewIssueObjectId);
			detailsMap.put("ProjectId", strProjectId);
			detailsMap.put("Vault", strVault);
			detailsMap.put("CompanyId",Company.getHostCompany(context));
			detailsMap.put("Assignee", strExpAssigneeId);

			experiment.updateAttributeAndConnectWithProject(context,attributeMap,detailsMap);

			Map <String,String>issueDetails = new HashMap();
			issueDetails.put("IssueId", strNewIssueObjectId);
			issueDetails.put("isExpAdded", strExpAsSolution);
			issueDetails.put("Approver", strExpApproverId);
			issueDetails.put("TaskDueDate", strEstFinishDate);
			issueDetails.put("RouteOriginatedDate", strEstStartDate);
			issueDetails.put("Assignee", strExpAssigneeId);
			issueDetails.put("ActionRequired", strActionRequired);
			issueDetails.put("selectedRouteTemplateId", routeTemplateId);
			issueDetails.put("Vault", strVault);
			issueDetails.put("StartDate", strEstStartDate);

			experiment.createAndConnectWithRoute(context, issueDetails);
			ContextUtil.commitTransaction(context);
		}catch(Exception e){
			ContextUtil.abortTransaction(context);
		}
	}

	/**
	 * Display Duration of issue.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about project.
	 * @return Issue Duration list.
	 * @throws Exception if operation fails.
	 */
	public StringList getIssueDuration(Context context,String[]args)throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList slDurationList = new StringList(objectList.size());
		for(int i=0;i<objectList.size();i++){
			Map<String,String>issueMap = (Map)objectList.get(i);
			String issueStartDate = issueMap.get(SELECT_ESTIMATED_START_DATE);
			String issueEndDate = issueMap.get(SELECT_ESTIMATED_END_DATE);
			int iDuration = Issue.daysBetween(issueEndDate, issueStartDate);
			slDurationList.addElement(String.valueOf(iDuration));
		}
		return slDurationList;
	}

	/**
	 * Add experiment to the Issue object.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about project.
	 * @throws Exception if operation fails.
	 */
	public void addExistingExperiment(Context context,String[]args)throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		Map paramMap   = (Map)programMap.get("paramMap");
		String strExpProjectId  = (String)paramMap.get("New Value");
		String strIssueId  = (String)paramMap.get("objectId");
		PropertyUtil.setGlobalRPEValue(context,"expUpdated", "true");

		experiment.addExperiment(context, strIssueId, strExpProjectId);

	}

	/**
	 * Display range list for approval process.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return rangeMap
	 * @throws Exception if operation fails.
	 */
	public Map getApprovalRangeList(Context context,String[]args)throws Exception
	{
		Map rangeMap = new HashMap();
		StringList slActualList = new StringList();
		slActualList.addElement("Approve");
		slActualList.addElement("Reject");

		StringList slDisplayList = new StringList();
		slDisplayList.addElement(EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.ChangeProject.Experiment.Approve", context.getSession().getLanguage()));
		slDisplayList.addElement(EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.ChangeProject.Experiment.Reject", context.getSession().getLanguage()));

		rangeMap.put("field_choices", slActualList);
		rangeMap.put("field_display_choices", slDisplayList);
		return rangeMap;
	}

	/**
	 * Display approval status value of route task.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return approval status list.
	 * @throws Exception if operation fails.
	 */
	public StringList getApprovalStatusValue(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList routeApprovedList = new StringList(objectList.size());
		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map routeMap = (Map)objectList.get(i);
			String issueId = (String)routeMap.get(DomainObject.SELECT_ID);
			issueIdArray[i] = issueId;
		}
		return experiment.getApprovalStatus(context, issueIdArray);
	}

	/**
	 * Update route task status value.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @throws Exception if operation fails.
	 */
	public void updateApproveRejectStatus(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		Map paramMap = (Map)programMap.get("paramMap");

		String strAppRejectValue = (String)paramMap.get("New Value");
		String strIssueId = (String)paramMap.get("objectId");

		experiment.getApproveRejectRouteTask(context, strIssueId, strAppRejectValue);
	}

	/**
	 * Update route task comments.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @throws Exception if operation fails.
	 */
	public void updateApproveRejectComments(Context context,String[]args)throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		Map paramMap = (Map)programMap.get("paramMap");

		String strAppRejectComments = (String)paramMap.get("New Value");
		String strIssueId = (String)paramMap.get("objectId");

		experiment.updateRouteTaskComments(context, strIssueId, strAppRejectComments);
	}

	/**
	 * Get comments value of route task.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return comment list.
	 * @throws Exception if operation fails.
	 */
	public StringList getApprovalRejectionComment(Context context,String[]args)throws Exception
	{
		StringList slCommentList = new StringList();
		String approverMsg = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL, 
				"emxProgramCentral.ChangeProject.Experiment.multipleAssignee", context.getSession().getLanguage());

		Map programMap = JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList routeApprovedList = new StringList(objectList.size());

		String contextPersonId = PersonUtil.getPersonObjectID(context);
		DomainObject.MULTI_VALUE_LIST.add(Experiment.SELECT_ISSUE_ROUTE_TASK_ASSIGNEE_ID);
		DomainObject.MULTI_VALUE_LIST.add(SELECT_ROUTE_TASK_APPROVAL_REJECTION_COMMENTS);
		DomainObject.MULTI_VALUE_LIST.add(Experiment.SELECT_ISSUE_ROUTE_STATUS);
		DomainObject.MULTI_VALUE_LIST.add(Experiment.SELECT_ISSUE_ROUTE_ID);

		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map routeMap = (Map)objectList.get(i);
			String issueId = (String)routeMap.get(DomainObject.SELECT_ID);
			issueIdArray[i] = issueId;
		}

		StringList slSelectable = new StringList(3);
		slSelectable.addElement(SELECT_ROUTE_TASK_APPROVAL_REJECTION_COMMENTS);
		slSelectable.addElement(Experiment.SELECT_ISSUE_ROUTE_TASK_ASSIGNEE_ID);
		slSelectable.addElement(Experiment.SELECT_ISSUE_ROUTE_STATUS);
		slSelectable.addElement(Experiment.SELECT_ISSUE_ROUTE_ID);

		MapList routeTaskList = new MapList();
		try{
			ProgramCentralUtil.pushUserContext(context);
			routeTaskList = DomainObject.getInfo(context, issueIdArray, slSelectable);
		}finally{
			ProgramCentralUtil.popUserContext(context);
		}


		StringList tempRoutIdList = new StringList();
		Map routCountMap = new HashMap();

		for(int i=0;i<routeTaskList.size();i++){
			Map routAssigneeCountMap = new HashMap();
			StringList routAssigneeIdList = new StringList();
			Map routeMap = (Map)routeTaskList.get(i);
			StringList routeTaskAssigneeIdList = (StringList)routeMap.get(Experiment.SELECT_ISSUE_ROUTE_TASK_ASSIGNEE_ID);
			StringList routeIdList = (StringList)routeMap.get(Experiment.SELECT_ISSUE_ROUTE_ID);

			if(routeTaskAssigneeIdList != null && routeIdList.size()<=1){
				String routeId = (String)routeIdList.get(0);

				for(int index=0;index<routeTaskAssigneeIdList.size();index++){
					String assigneeId = (String)routeTaskAssigneeIdList.get(index);
					String count = "1";

					if(!routAssigneeIdList.contains(assigneeId)){
						routAssigneeIdList.addElement(assigneeId);
						routAssigneeCountMap.put(assigneeId,count);
					}else{
						count = (String)routAssigneeCountMap.get(assigneeId);
						count = String.valueOf(Integer.valueOf(count)+1);
						routAssigneeCountMap.put(assigneeId,count);
					}
				}
				routCountMap.put(routeId, routAssigneeCountMap);
			}
		}

		StringList routAssigneeIdList = new StringList();
		for(int i=0;i<routeTaskList.size();i++){
			Map routeMap = (Map)routeTaskList.get(i);
			StringList approvalRejectionCommentList = (StringList)routeMap.get(SELECT_ROUTE_TASK_APPROVAL_REJECTION_COMMENTS);
			StringList routeTaskAssingeeIdList = (StringList)routeMap.get(Experiment.SELECT_ISSUE_ROUTE_TASK_ASSIGNEE_ID);
			StringList routeStatusList = (StringList)routeMap.get(Experiment.SELECT_ISSUE_ROUTE_STATUS);
			StringList routeIdList = (StringList)routeMap.get(Experiment.SELECT_ISSUE_ROUTE_ID);

			if(routeStatusList.size()<= 1){
				String routeStatus = (String)routeStatusList.get(0);
				String routeId = (String)routeIdList.get(0);
				if(approvalRejectionCommentList != null && routeTaskAssingeeIdList != null){
					boolean isAdded = false;
					for(int index=0;index<routeTaskAssingeeIdList.size();index++){
						String approvalRejectionComments = (String)approvalRejectionCommentList.get(index);
						String assigneeId = (String)routeTaskAssingeeIdList.get(index);

						if(contextPersonId.equalsIgnoreCase(assigneeId)){
							Map routAssigneeCountMap = (Map)routCountMap.get(routeId);
							String count = (String)routAssigneeCountMap.get(assigneeId);
							int totalOccurences = Integer.valueOf(count);

							if(totalOccurences == 1 && (!routAssigneeIdList.contains(assigneeId)|| !tempRoutIdList.contains(routeId))){
								slCommentList.addElement(approvalRejectionComments);
								routAssigneeIdList.addElement(assigneeId);
								tempRoutIdList.addElement(routeId);
							}else if(!routAssigneeIdList.contains(assigneeId) || !tempRoutIdList.contains(routeId)){
								slCommentList.addElement(approverMsg);
								routAssigneeIdList.addElement(assigneeId);
								tempRoutIdList.addElement(routeId);
							}
							isAdded = true;
						}
					}
					if(!isAdded){
						if("Finished".equalsIgnoreCase(routeStatus)&& routeTaskAssingeeIdList.size()==1){
							String approvalRejectionComments = (String)approvalRejectionCommentList.get(0);
							slCommentList.addElement(approvalRejectionComments);
						}else{
							slCommentList.addElement(DomainObject.EMPTY_STRING);
						}
					}
				}else{
					slCommentList.addElement(DomainObject.EMPTY_STRING);
				}
			}else{
				slCommentList.addElement(DomainObject.EMPTY_STRING);
			}
		}
		return slCommentList;
	}

	/**
	 * Check access for approve and reject task.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return access list.
	 * @throws Exception if operation fails.
	 */
	public StringList hasAccessToApproveReject(Context context,String[]args)throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			String strIssueId = (String)objectMap.get(DomainObject.SELECT_ID);
			issueIdArray[i] = strIssueId;
		}
		return experiment.hasAccessToApproveRejectTask(context, issueIdArray);
	}

	/**
	 * Check access for add experiment project in issue object.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return access list.
	 * @throws Exception if operation fails.
	 */
	public StringList hasAccessToAddExperiment(Context context,String[]args)throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		MapList objectList = (MapList) programMap.get("objectList");
		String projectId = (String)requestMap.get("objectId");

		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			String strIssueId = (String)objectMap.get(DomainObject.SELECT_ID);
			issueIdArray[i] = strIssueId;
		}
		return experiment.hasAccessToAddSolution(context, issueIdArray,projectId);
	}

	/**
	 * This method allow to sync left if enable process isn't active.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToSyncLeft(Context context,String[]args)throws Exception
	{
		boolean hasAccess = true;

		Map programMap =  JPO.unpackArgs(args);
		String projectId = (String)programMap.get("ParentobjectId");
		String experimentId = (String)programMap.get("objectId");
		hasAccess = experiment.hasAccessToSyncLeft(context, projectId,experimentId);

		return hasAccess;
	}

	/**
	 * This method allow to sync Right if enable process isn't active.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToSyncRight(Context context,String[]args)throws Exception
	{
		boolean hasAccess = true;

		Map programMap = JPO.unpackArgs(args);
		String projectId = (String)programMap.get("ParentobjectId");
		String experimentId = (String)programMap.get("objectId");

		String objectType1 = (String)programMap.get("objectType1");
		String objectType2 = (String)programMap.get("objectType2");

		String objectId1 = (String)programMap.get("objectId1");
		String objectId2 = (String)programMap.get("objectId2");

		if(ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(objectType1) && ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(objectType2)){
			String []idArray = new String[1];
			idArray[0] = objectId2;
			StringList selectable = new StringList(1);
			selectable.addElement(Experiment.SELECT_EXPERIMENT_ROUTE_ID);

			MapList objectInfoList = DomainObject.getInfo(context, idArray, selectable);
			Map infoMap = (Map)objectInfoList.get(0);
			Object object = infoMap.get(Experiment.SELECT_EXPERIMENT_ROUTE_ID);
			if(object != null){
				hasAccess = false;
			}
		} else{
			hasAccess = experiment.hasAccessToSyncRight(context, projectId, experimentId);
		}

		return hasAccess;
	}

	/**
	 * Get Issue Icon.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return issue Icon list.
	 * @throws Exception if operation fails.
	 */
	public StringList getIssueIcon(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		Map paramList = (Map)programMap.get("paramList");
		String strProjectId = (String) paramList.get("parentOID");
		MapList objectList = (MapList) programMap.get("objectList");

		String []expArrayId = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			expArrayId[i] = XSSUtil.encodeForHTML(context,(String)objectMap.get(DomainObject.SELECT_ID));
		}
		return experiment.getIssueIcon(context, expArrayId);
	}

	/**
	 * Display Issue Owner name.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return issue owner name list.
	 * @throws Exception if operation fails.
	 */
	public StringList getIssueCreator(Context context,String[]args)throws Exception
	{
		StringList creatorList = new StringList();

		Map programMap =  JPO.unpackArgs(args);
		Map paramList = (Map)programMap.get("paramList");
		String strProjectId = (String) paramList.get("parentOID");
		MapList objectList = (MapList) programMap.get("objectList");

		String []expArrayId = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			expArrayId[i] = (String)objectMap.get(DomainObject.SELECT_ID);
		}

		StringList slSelectable = new StringList(1);
		slSelectable.addElement(DomainObject.SELECT_OWNER);

		MapList issueOwnerList = DomainObject.getInfo(context, expArrayId, slSelectable);
		for (int i = 0; i < issueOwnerList.size(); i++){
			Map mpExpDetails = (Map) issueOwnerList.get(i);
			String creatorName = (String)mpExpDetails.get(DomainObject.SELECT_OWNER);
			creatorName = PersonUtil.getFullName(context, creatorName);
			creatorList.addElement(creatorName);
		}

		return creatorList;
	}

	/**
	 * Get project vault.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return project vault
	 * @throws Exception if operation fails.
	 */
	public String getProjectVault(Context context,String[]args)throws Exception
	{
		String projectVault = DomainObject.EMPTY_STRING;
		Map programMap =  JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");

		String projectId = (String)requestMap.get("objectId");
		DomainObject project = DomainObject.newInstance(context, projectId);
		projectVault = project.getInfo(context, DomainObject.SELECT_VAULT);

		return projectVault;
	}

	/**
	 * Get range value for action required fields.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return range value.
	 * @throws Exception if operation fails.
	 */
	public Map getActionRequiredRangeList(Context context,String[]args)throws Exception
	{
		Map rangeMap = new HashMap();
		StringList slActualList = new StringList(2);
		slActualList.addElement("Any");
		slActualList.addElement("All");

		StringList slDisplayList = new StringList(2);
		slDisplayList.addElement(EnoviaResourceBundle.getProperty(context, "Components", 
				"emxComponents.ActionRequiredDialog.Any", context.getSession().getLanguage()));
		slDisplayList.addElement(EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Common.All", context.getSession().getLanguage()));

		rangeMap.put("field_choices", slActualList);
		rangeMap.put("field_display_choices", slDisplayList);

		return rangeMap;
	}

	/**
	 * Display Route Task approval or Rejection status.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return status list.
	 * @throws Exception if operation fails.
	 */
	public StringList getRouteApprovalStatus(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList routeApprovedList = new StringList(objectList.size());
		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map routeMap = (Map)objectList.get(i);
			String issueId = (String)routeMap.get(DomainObject.SELECT_ID);
			issueIdArray[i] = issueId;
		}

		return experiment.getStatus(context, issueIdArray);
	}

	/**
	 * Display color according to route task approval status.
	 * if all route tasks are approved then it should be green color.
	 * if some route tasks are approved then it should be yellow color.
	 * if any route task is rejected then it should be red color.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about object.
	 * @return status color list.
	 * @throws Exception if operation fails.
	 */
	public StringList getStatusColor(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList routeApprovedList = new StringList(objectList.size());
		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map routeMap = (Map)objectList.get(i);
			String issueId = (String)routeMap.get(DomainObject.SELECT_ID);
			issueIdArray[i] = issueId;
		}

		return experiment.getColumnColor(context, issueIdArray);
	}

	/**
	 * Allow to add route template.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToAddRouteTemplate(Context context,String[]args)throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		String projectId = (String)programMap.get("objectId");

		if(ProgramCentralUtil.isNullString(projectId)){
			projectId = (String)programMap.get("parentOID");
		}
		return experiment.hasAccessToAddRouteTemplate(context, projectId);
	}

	/**
	 * Allow to add route template.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToAddRouteApprover(Context context,String[]args)throws Exception
	{
		Map programMap = JPO.unpackArgs(args);
		String projectId = (String)programMap.get("objectId");

		if(ProgramCentralUtil.isNullString(projectId)){
			projectId = (String)programMap.get("parentOID");
		}
		return experiment.hasAccessToAddRouteApprover(context, projectId);
	}

	/**
	 * Show Promote Icon for Issue object promotion.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return list of Icon.
	 * @throws Exception if operation fails.
	 */
	public StringList completeIssue(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		Map paramList = (Map)programMap.get("paramList");
		String strProjectId = (String) paramList.get("parentOID");
		MapList objectList = (MapList) programMap.get("objectList");

		return experiment.completeIssue(context, objectList);
	}

	/**
	 * Get Experiment issue assignee.
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return issue assignee list.
	 * @throws Exception if operation fails.
	 */
	public StringList getExpIssueAssigneeName(Context context,String[]args)throws Exception
	{
		Map programMap =  JPO.unpackArgs(args);
		Map paramList = (Map)programMap.get("paramList");
		String strProjectId = (String) paramList.get("parentOID");
		MapList objectList = (MapList) programMap.get("objectList");

		String []issueIdArray = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map) objectList.get(i);
			issueIdArray[i] = (String)objectMap.get(DomainObject.SELECT_ID);
		}

		return experiment.getIssueAssigneeName(context, issueIdArray);
	}

	/**
	 * 
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return true when the depedency is updated
	 * @throws Exception
	 */
	public void updateDependency(Context context,String args[]) throws Exception
	{
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			final String SELECT_PROJECT_ID = "to[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
			String objectId = (String) paramMap.get("objectId");// get the task Id
			DomainObject taskObject = DomainObject.newInstance(context,objectId);

			String projectID = taskObject.getInfo(context, SELECT_PROJECT_ID);
			requestMap.put("projectID", projectID);
			paramMap.put("calledFrom", "WhatIfCompareView");

			Map packMap = new HashMap();
			packMap.put("paramMap", paramMap);
			packMap.put("requestMap", requestMap);

			JPO.invoke(context, "emxTask",
					new String[] {}, "updateDependency",
					JPO.packArgs(packMap),
					null);

		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	/**
	 * 
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return true when sequence number is updated.
	 * @throws Exception
	 */
	public void updateSequenceNumber(Context context,String args[]) throws Exception
	{
		try{
			ContextUtil.startTransaction(context, true);

			String KEY_SEQ_NUMBER = "SEQ_NUMBER";
			String KEY_WBS_NUMBER = "WBS_NUMBER";
			String SELECT_SEQUENCE_ORDER_VALUE ="Sequence Order";

			Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");

			String objectId = (String) paramMap.get("objectId");// get the task Id
			String newSeqNo = (String) paramMap.get("New Value");
			String relId = (String) paramMap.get("relId");
			String syncDir = (String) paramMap.get("syncDir");
			String objectId2 = (String) paramMap.get("projectId");
			String objectId1 = (String) paramMap.get("expId");			
			String strAddBefore = EMPTY_STRING;



			Map syncedTaskRelAttribute = DomainRelationship.getAttributeMap(context,relId);
			String taskSeq  = (String)syncedTaskRelAttribute.get(SELECT_SEQUENCE_ORDER_VALUE);

			Map relAttribMap = new HashMap();

			if(!taskSeq.equalsIgnoreCase(newSeqNo)){

				if("right".equalsIgnoreCase(syncDir)){
					task.setId(objectId2);
				}else{
					task.setId(objectId1);
				}

				StringList busSelects = new StringList(1);
				busSelects.addElement(DomainObject.SELECT_ID);


				StringList relationshipSelects = new StringList(2);
				relationshipSelects.addElement(SubtaskRelationship.SELECT_TASK_WBS);
				relationshipSelects.addElement(SubtaskRelationship.SELECT_SEQUENCE_ORDER);


				MapList parentTaskList =Task.getTasks(context, task, 0, busSelects, relationshipSelects);
				parentTaskList.sort(SubtaskRelationship.SELECT_SEQUENCE_ORDER,
						ProgramCentralConstants.ASCENDING_SORT,ProgramCentralConstants.SORTTYPE_INTEGER);

				int rootParentChildTaskCount = parentTaskList.size();
				if(Integer.parseInt(newSeqNo) <= rootParentChildTaskCount) {
					//decide to where newly sync task added in the master project.
					Map swapTaskRelAttribute = new HashMap();
					String taskRelId = DomainObject.EMPTY_STRING;
					for(int i=0; i<rootParentChildTaskCount; i++){
						Map parentTaskMap = (Map)parentTaskList.get(i);
						String strParentTaskSeq = (String)parentTaskMap.get(SubtaskRelationship.SELECT_SEQUENCE_ORDER);
						String taskId = (String)parentTaskMap.get(DomainObject.SELECT_ID);
						taskRelId = (String)parentTaskMap.get(DomainRelationship.SELECT_ID);

						if(strParentTaskSeq.equals(newSeqNo)){
							swapTaskRelAttribute = DomainRelationship.getAttributeMap(context,taskRelId);
							break;
						}
					}

					if(UIUtil.isNotNullAndNotEmpty(relId) && UIUtil.isNotNullAndNotEmpty(taskRelId)){

						DomainRelationship syncedTaskRel = DomainRelationship.newInstance(context, relId);
						syncedTaskRel.setAttributeValues(context, swapTaskRelAttribute);

						DomainRelationship swapTaskRel = DomainRelationship.newInstance(context, taskRelId);
						swapTaskRel.setAttributeValues(context, syncedTaskRelAttribute);
					}
				}
			}
			ContextUtil.commitTransaction(context);
		}catch (Exception e){
			ContextUtil.abortTransaction(context);
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param context The ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return map which refreshes the PMCWhatIfExperimentStructure.
	 * @throws Exception
	 */
	@Deprecated
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map postProcessRefresh(Context context,String[]args)throws Exception
	{
		//String syncDir = "right";
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestInfo = (HashMap) inputMap.get("requestMap");

		String objectId1 = (String)requestInfo.get("objectId1");//Experiment ID
		String objectId2 = (String)requestInfo.get("objectId2");//Project ID

		String objectType1 = (String)requestInfo.get("objectType1");
		String objectType2 = (String)requestInfo.get("objectType2");
		boolean isCompareExperiments = false;
		String fromExperimentId = EMPTY_STRING;
		String toExperimentId = EMPTY_STRING; 

		if(ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(objectType1) && ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(objectType2)){
			isCompareExperiments = true;
		}

		final String SELECT_PROJECT_TYPE = "to[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
		final String SELECT_PROJECT_ID= "to[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
		Document doc = (Document) inputMap.get("XMLDoc");
		Element rootElement = (Element)doc.getRootElement();
		List elementList     = rootElement.getChildren("object");

		MapList updatedValuesList = new MapList();

		//New mapList for Est start date
		MapList tasksDateList = new MapList();

		StringList syncDirList = new StringList(2);

		if(elementList != null){
			java.util.Iterator itrC  = elementList.iterator();
			while(itrC.hasNext()){
				Element childCElement = (Element)itrC.next();
				String sObjectId = (String)childCElement.getAttributeValue("objectId");
				String sRelId = (String)childCElement.getAttributeValue("relId");
				String sRowId = (String)childCElement.getAttributeValue("rowId");
				String markup    = (String)childCElement.getAttributeValue("markup");
				String syncDir    = (String)childCElement.getAttributeValue("syncDir");

				if(!syncDirList.contains(syncDir)){
					syncDirList.addElement(syncDir);
				}

				List colList = (List)childCElement.getChildren();
				if(colList != null){
					java.util.Iterator itrC1  = colList.iterator();
					Map<String,String> taskDateMap = new HashMap<String,String>();

					taskDateMap.put("objectId", sObjectId);

					while(itrC1.hasNext()) {
						Element colEle = (Element)itrC1.next();
						String colValue = (String)colEle.getTextTrim();
						String colName = (String)colEle.getAttributeValue("name");
						HashMap modifiedValues = new HashMap();

						if("ID".equals(colName) || "Dependency".equals(colName)) {

							modifiedValues.put(colName, colValue);
							modifiedValues.put("objectId", sObjectId);
							modifiedValues.put("markup", markup);
							modifiedValues.put("relId", sRelId);
							modifiedValues.put("syncDir", syncDir);
							updatedValuesList.add(modifiedValues);

						}else if("PhaseEstimatedStartDate".equalsIgnoreCase(colName) ||
								"Constraint Date".equalsIgnoreCase(colName)||
								"ConstraintType".equalsIgnoreCase(colName)||
								"PhaseEstimatedDuration".equalsIgnoreCase(colName)) {

							taskDateMap.put(colName, colValue);

						}
					}
					tasksDateList.add(taskDateMap);
				}


				//following condition is added for newly added tasks
				if(sRelId == null && sRowId==null && markup==null && sObjectId != null){
					List newTaskList = (List)childCElement.getChildren();
					if(newTaskList != null){
						java.util.Iterator itrNewTaskList  = newTaskList.iterator();
						while(itrNewTaskList.hasNext()){
							Element newTaskElement = (Element)itrNewTaskList.next();
							markup = (String)newTaskElement.getAttributeValue("markup");
							syncDir    = (String)newTaskElement.getAttributeValue("syncDir");

							if(isCompareExperiments && "right".equalsIgnoreCase(syncDir)){
								fromExperimentId = objectId1;
								toExperimentId = objectId2;
							} else if (isCompareExperiments && "left".equalsIgnoreCase(syncDir)){
								fromExperimentId = objectId2;
								toExperimentId = objectId1;
							}

							if(!syncDirList.contains(syncDir)){
								syncDirList.addElement(syncDir);
							}

							List columnList = (List)newTaskElement.getChildren();
							if(columnList != null){
								java.util.Iterator itrColumn  = columnList.iterator();
								while(itrColumn.hasNext()){
									Element columnElement = (Element)itrColumn.next();
									String colValue = (String)columnElement.getTextTrim();
									String colName = (String)columnElement.getAttributeValue("name");
									String taskObjectId = (String)newTaskElement.getAttributeValue("objectId");
									DomainObject task = DomainObject.newInstance(context, taskObjectId);
									StringList busSelect = new StringList();
									busSelect.addElement(SELECT_PROJECT_TYPE);
									busSelect.addElement("from[Experiment].to."+SELECT_PROJECT_ID);
									busSelect.addElement("from[Experiment].to.id");
									busSelect.addElement("to[Experiment].from.id");
									busSelect.addElement(SELECT_NAME);

									if(isCompareExperiments){
										busSelect.addElement("to[Experiment].from.from[Experiment].to.id");
										busSelect.addElement("to[Experiment].from.from[Experiment].to.to[Project Access Key].from.from[Project Access List].to.id");
										DomainObject.MULTI_VALUE_LIST.add("to[Experiment].from.from[Experiment].to.id");
										DomainObject.MULTI_VALUE_LIST.add("to[Experiment].from.from[Experiment].to.to[Project Access Key].from.from[Project Access List].to.id");

									}

									Map taskDetails = task.getInfo(context, busSelect);
									String parentType = (String)taskDetails.get(SELECT_PROJECT_TYPE);
									Object toId = taskDetails.get("from[Experiment].to.id");
									String fromId = (String)taskDetails.get("to[Experiment].from.id");
									String taskName = (String)taskDetails.get(SELECT_NAME);

									if(TYPE_PROJECT_SPACE.equals(parentType)){
										StringList taskParentProjectId = (StringList)taskDetails.get("from[Experiment].to."+SELECT_PROJECT_ID);
										StringList toIdList = (StringList)toId;
										for(int i=0;i<toIdList.size();i++){
											if(objectId1.equals((String)taskParentProjectId.get(i))){
												taskObjectId = (String)toIdList.get(i);
											}
										}			
									} else {
										if(!isCompareExperiments){
											taskObjectId = fromId;
										}else {
											boolean hasCorrespondingTask = false;
											Object correspondingTaskIds = taskDetails.get("to[Experiment].from.from[Experiment].to.id");
											Object correspondingExperimentIds = taskDetails.get("to[Experiment].from.from[Experiment].to.to[Project Access Key].from.from[Project Access List].to.id");


											DomainObject experiment = DomainObject.newInstance(context, toExperimentId);
											StringList typeSelects = new StringList(1);
											typeSelects.add(SELECT_ID);
											typeSelects.add(SELECT_TYPE);
											typeSelects.add(SELECT_NAME);
											StringList relationshipSelect = new StringList(0);

											//Get the list of all the sub-Experiments
											MapList taskList = experiment.getRelatedObjects(context,
													DomainConstants.RELATIONSHIP_SUBTASK,
													ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT,
													typeSelects,
													relationshipSelect,
													false,
													true,
													(short)0,
													DomainObject.EMPTY_STRING, 
													DomainObject.EMPTY_STRING, 
													0);

											StringList subExperimentIds = new StringList();
											MapList taskNameMatchList = new MapList();
											Iterator taskListIterator = taskList.iterator();
											while(taskListIterator.hasNext()){
												Map taskInfo =  (Map)taskListIterator.next();
												String strTaskId = (String)taskInfo.get(SELECT_ID);
												String strTaskType = (String)taskInfo.get(SELECT_TYPE);
												String strTaskName = (String)taskInfo.get(SELECT_NAME);

												if(ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(strTaskType)){
													subExperimentIds.add(strTaskId);
												}
												if(taskName.equals(strTaskName)){
													taskNameMatchList.add(taskInfo);
												}

											}

											if(correspondingExperimentIds instanceof StringList){
												StringList experimentIds = (StringList) correspondingExperimentIds;
												StringList taskIds = (StringList)correspondingTaskIds;

												for(int i=0;i<experimentIds.size();i++){
													if(toExperimentId.equals((String)experimentIds.get(i))){
														taskObjectId = (String)taskIds.get(i);
														hasCorrespondingTask = true;
														break;
													}else if(subExperimentIds.contains((String)experimentIds.get(i))){
														taskObjectId = (String)taskIds.get(i);
														hasCorrespondingTask = true;
														break;
													}
												}
											}

											//If new task is added from one Experiment to other Experiment and that task is not in master project then get the correct task by name
											if(!hasCorrespondingTask){
												if(taskNameMatchList!=null && !taskNameMatchList.isEmpty() && taskNameMatchList.size() ==1){
													taskObjectId = (String)((Map)(taskNameMatchList.get(0))).get(SELECT_ID);
													hasCorrespondingTask = true;
												}
											}


										}
									}

									// Following condition is added for Experiment to Experiment Comparison
									if(ProgramCentralUtil.isNullString(taskObjectId)){
										continue;
									}

									HashMap modifiedValues = new HashMap();
									if("ID".equals(colName) || "Dependency".equals(colName)){
										modifiedValues.put(colName, colValue);
										modifiedValues.put("markup", markup);
										modifiedValues.put("objectId", taskObjectId);
										modifiedValues.put("syncDir", syncDir);
										updatedValuesList.add(modifiedValues);
									}

								}
							}

						}
					}
				}

			}

		}

		StringList projectRollupList = new StringList();
		//Logic for update Estimated start date and constraint date of task
		if(tasksDateList != null && tasksDateList.size()>0) {

			/*Map statusMap = experiment.updateTaskEstimatedInformation(context, tasksDateList);
			String action = (String)statusMap.get("Action");
			String error = (String)statusMap.get("ErrorMsg");

			if("Error".equalsIgnoreCase(action)){
				Map returnHashMap = new HashMap();
				returnHashMap.put("Message", error);
				returnHashMap.put("Action", "ERROR");

				return returnHashMap;
			}
			StringList parentProjectList = (StringList)statusMap.get("parentProjectList");

			if(parentProjectList !=null && !parentProjectList.isEmpty()){
				projectRollupList.addAll(parentProjectList);
			}

			if(projectRollupList.contains(objectId1)){
				projectRollupList.remove(objectId1);
			}
			if(projectRollupList.contains(objectId2)){
				projectRollupList.remove(objectId2);
			}*/
		}

		Iterator taskIteratorToUpdateSequenceNumber = updatedValuesList.iterator();

		while(taskIteratorToUpdateSequenceNumber.hasNext()){
			Map paramMap = new HashMap();
			Map programMap = new HashMap();
			Map mpTaskInfo 		= (Map)taskIteratorToUpdateSequenceNumber.next();
			String objectId = (String)mpTaskInfo.get("objectId");
			String id = (String)mpTaskInfo.get("ID");
			String markup = (String)mpTaskInfo.get("markup");
			String relId = (String)mpTaskInfo.get("relId");
			String syncDir = (String)mpTaskInfo.get("syncDir");

			if("changed".equalsIgnoreCase(markup) && UIUtil.isNotNullAndNotEmpty(id)){

				paramMap.put("objectId", objectId);
				paramMap.put("New Value", id);
				paramMap.put("relId", relId);
				paramMap.put("syncDir", syncDir);
				paramMap.put("projectId", objectId2);
				paramMap.put("expId", objectId1);

				programMap.put("paramMap", paramMap);

				updateSequenceNumber(context, JPO.packArgs(programMap));

			}
		}

		if(updatedValuesList != null && updatedValuesList.size()>0){
			MapList updateDependencyList = new MapList();
			Iterator updatedValuesListIterator = updatedValuesList.iterator();
			while(updatedValuesListIterator.hasNext()){
				Map updatedValueMap = (Map)updatedValuesListIterator.next();
				String dependencyValue = (String)updatedValueMap.get("Dependency");
				if(dependencyValue !=null){
					updateDependencyList.add(updatedValueMap);
				}

			}
			updateTaskDependency(context, updateDependencyList);
		}

		if(projectRollupList != null && !projectRollupList.isEmpty()){
			Task task = new Task();
			for(int i=0;i< projectRollupList.size();i++){
				task.setId((String)projectRollupList.get(i));
				task.rollupAndSave(context);
			}
		}

		if(syncDirList.contains("right")){
			Task task = new Task();
			task.setId(objectId2);
			task.rollupAndSave(context);
		}

		if(syncDirList.contains("left")){
			Task task = new Task();
			task.setId(objectId1);
			task.rollupAndSave(context);
		}

		String rpeValue = PropertyUtil.getRPEValue(context, "SyncDone", true);
		PropertyUtil.setRPEValue(context, "SyncDone", "true", true);

		if("false".equalsIgnoreCase(rpeValue)){
			String nameList = PropertyUtil.getRPEValue(context, "nameList", true);
			PropertyUtil.setRPEValue(context, "nameList", "null", true);

			String[] messageValues 	=	new String[1];
			messageValues[0] 		=	nameList;
			Locale locale			=	new Locale(context.getSession().getLanguage());
			String alertMsg 		=	MessageUtil.getMessage(context,null,"emxProgramcentral.Experiment.Sync.Alert",
					messageValues,null,locale,
					ProgramCentralConstants.RESOURCE_BUNDLE);

			MqlUtil.mqlCommand(context, "warning $1", alertMsg);
		}

		//		HashMap returnHashMap = new HashMap();



		/*HashMap returnHashMap = new HashMap();

		StringBuilder output = new StringBuilder();
		output.append("{");
		output.append("main:function() {");
		output.append("var topFrame = findFrame(top, \"PMCWhatIfExperimentStructure\");"); 
		output.append("topFrame.location.href = topFrame.location.href;"); 
		output.append("}}");                                       
		returnHashMap.put("Action","execScript");
		returnHashMap.put("Message", output.toString());*/

		HashMap returnHashMap = new HashMap();
		returnHashMap.put("Action","refresh");

		return returnHashMap;

	}

	/**
	 * Update selected task dependency.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param updatedValuesList - List contains selected task dependency value.
	 * @throws Exception If operation fails.
	 */
	private void updateTaskDependency(Context context,MapList updatedValuesList)throws Exception
	{
		final String SELECT_PROJECT_TYPE = "to[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
		final String SELECT_PROJECT_ID= "to[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + ProgramCentralConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";

		int listSize = updatedValuesList.size();
		String[] taskObjectIds = new String[listSize];

		for(int i=0;i<listSize;i++){
			Map mpTaskInfo 		= (Map)updatedValuesList.get(i);
			String objectId = (String)mpTaskInfo.get("objectId");
			taskObjectIds[i] = objectId;
		}

		StringList taskSelectable = new StringList(2);
		taskSelectable.addElement(SELECT_PROJECT_ID);
		taskSelectable.addElement(SELECT_ID);

		BusinessObjectWithSelectList bwsl = BusinessObject.getSelectBusinessObjectData(context, taskObjectIds, taskSelectable);

		Map<String,String> taskParentMap = new HashMap<String,String>();
		for(int i=0; i<listSize; i++){
			BusinessObjectWithSelect bws = bwsl.getElement(i);
			String taskId 			 	 = bws.getSelectData(SELECT_ID);
			String taskProjectId 		 = bws.getSelectData(SELECT_PROJECT_ID);
			if(ProgramCentralUtil.isNullString(taskProjectId)){
				taskProjectId = taskId;           //This code is added for subproject, for subproject SELECT_PROJECT_ID will be null
			}
			taskParentMap.put(taskId, taskProjectId);
		}

		Map<String,MapList> projectTaskMap = new HashMap<String,MapList>();

		Iterator taskIteratorToUpdateDependency = updatedValuesList.iterator();
		while(taskIteratorToUpdateDependency.hasNext()){
			Map paramMap = new HashMap();
			Map programMap = new HashMap();
			Map requestMap = new HashMap();

			MapList projectTaskMapList = new MapList();
			Map mpTaskInfo 		= (Map)taskIteratorToUpdateDependency.next();
			String objectId = (String)mpTaskInfo.get("objectId");
			String dependency = (String)mpTaskInfo.get("Dependency");
			requestMap.put("objectId", objectId);
			if(dependency !=null){

				paramMap.put("objectId", objectId);
				String projectId = taskParentMap.get(objectId);
				if(!projectTaskMap.containsKey(projectId)) {

					DomainObject projectObject = DomainObject.newInstance(context,projectId);

					StringList typeSelects = new StringList(1);
					typeSelects.add(DomainConstants.SELECT_ID);

					StringList relationSelects = new StringList(1);
					relationSelects.add("attribute[" + DomainConstants.ATTRIBUTE_SEQUENCE_ORDER + "]");

					MapList taskList = projectObject.getRelatedObjects(context,
							DomainConstants.RELATIONSHIP_SUBTASK,
							DomainConstants.TYPE_TASK_MANAGEMENT,
							typeSelects,
							relationSelects,
							false,
							true,
							(short)0,
							DomainObject.EMPTY_STRING, 
							DomainObject.EMPTY_STRING, 
							0);

					projectTaskMap.put(projectId, taskList);
				}

				paramMap.put("projectTaskMap", projectTaskMap);
				paramMap.put("New Value", dependency);
				paramMap.put("calledFrom", "WhatIfCompareView");
				programMap.put("paramMap", paramMap);
				programMap.put("requestMap", requestMap);

				JPO.invoke(context, "emxTask",
						new String[] {}, "updateDependency",
						JPO.packArgs(programMap),
						null);
			}
		}
	}



	/**
	 * Return boolean value.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - The args hold information about object.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean isAssigneeFieldVisible(Context context,String[] args)throws Exception {

		Map programMap 			= JPO.unpackArgs(args);
		String selectedTaskId   = (String) programMap.get("objectId");
		boolean isVisible = true;

		StringList objectSelects = new StringList(2);
		objectSelects.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
		objectSelects.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE_TASK);
		objectSelects.addElement(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);

		DomainObject object = DomainObject.newInstance(context, selectedTaskId);
		Map objectInfoMap = object.getInfo(context, objectSelects);

		String isProjectTemplate 		= (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
		String isProjectTemplateTask 	= (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE_TASK);
		String isExperiment             = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);

		if(ProgramCentralUtil.isNotNullString(isExperiment) && "true".equalsIgnoreCase(isExperiment)){
			isVisible = false;
		} else if(ProgramCentralUtil.isNotNullString(isProjectTemplate) && "true".equalsIgnoreCase(isProjectTemplate)) {
			isVisible = false;
		} else if(ProgramCentralUtil.isNotNullString(isProjectTemplateTask) && "true".equalsIgnoreCase(isProjectTemplateTask)) {
			isVisible = false;
		} 

		return isVisible;
	}
	/**
	 * This method will return the StringList containing Project, Experiment and task names along with status icon 
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - holds information about object.
	 * @return the StringList containing Project, Experiment and task names along with status icon 
	 * @throws Exception if operation fails.
	 */
	public StringList getName(Context context, String[] args)	throws Exception
	{
		StringList returnList  = new StringList();
		String SELECT_CORRESPONDING_EXPERIMENT_TASK_IDS_OF_PROJECT_TASK = "to[Experiment].from.from[Experiment].to.id";
		String SELECT_CORRESPONDING_EXPERIMENT_ID = "to[Experiment].from.from[Experiment].to."+ProgramCentralConstants.SELECT_TASK_PROJECT_ID;
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramList = (HashMap) programMap.get("paramList");
			String objectId = (String)paramList.get("objectId");
			String objectId1 = (String)paramList.get("objectId1");
			String objectId2 = (String)paramList.get("objectId2");

			String objectType1 = (String)paramList.get("objectType1");
			String objectType2 = (String)paramList.get("objectType2");
			boolean isCompareExperiments = false;

			if(ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(objectType1) && ProgramCentralConstants.TYPE_EXPERIMENT.equalsIgnoreCase(objectType2)){
				isCompareExperiments = true;
			}

			MapList objectList = (MapList) programMap.get("objectList");
			Iterator taskListIterator = objectList.iterator();

			StringList taskUniqueLeftList = new StringList();
			StringList taskUniqueRightList = new StringList();
			StringList movedTaskList = new StringList();

			while(taskListIterator.hasNext()){
				Map taskInfoMap = (Map) taskListIterator.next();
				String matchResult = (String) taskInfoMap.get("matchresult");
				String taskId = (String) taskInfoMap.get(SELECT_ID);
				if("left".equalsIgnoreCase(matchResult)){
					taskUniqueLeftList.add(taskId);
				}else if("right".equalsIgnoreCase(matchResult)){
					taskUniqueRightList.add(taskId);
				}
			}

			StringList rightSubExperimentIdList = new StringList();
			rightSubExperimentIdList.add(objectId2);
			// StringList subExperimentNameList = new StringList();

			if(!taskUniqueLeftList.isEmpty()){
				StringList busSelects = new StringList(SELECT_ID);
				busSelects.add(SELECT_NAME);

				DomainObject rightObject = DomainObject.newInstance(context,objectId2);
				MapList subExperimentList = rightObject.getRelatedObjects(
						context,
						RELATIONSHIP_SUBTASK,
						ProgramCentralConstants.TYPE_PROJECT_SPACE,
						busSelects,
						new StringList(),
						false,
						true,
						(short)0,
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING,
						0);

				Iterator subExperimentsListIterator = subExperimentList.iterator();
				while(subExperimentsListIterator.hasNext()){
					Map subExperimentInfo =  (Map)subExperimentsListIterator.next();
					String subExperimentId = (String)subExperimentInfo.get(SELECT_ID);
					String subExperimentName = (String)subExperimentInfo.get(SELECT_NAME);
					rightSubExperimentIdList.add(subExperimentId);
					//subExperimentNameList.add(subExperimentName);
				}
			}

			StringList leftSubExperimentIdList = new StringList();
			leftSubExperimentIdList.add(objectId1);

			if(!taskUniqueRightList.isEmpty()){
				StringList busSelects = new StringList(SELECT_ID);
				busSelects.add(SELECT_NAME);

				DomainObject leftObject = DomainObject.newInstance(context,objectId1);
				MapList subExperimentList = leftObject.getRelatedObjects(
						context,
						RELATIONSHIP_SUBTASK,
						ProgramCentralConstants.TYPE_EXPERIMENT,
						busSelects,
						new StringList(),
						false,
						true,
						(short)0,
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING,
						0);

				Iterator subExperimentsListIterator = subExperimentList.iterator();
				while(subExperimentsListIterator.hasNext()){
					Map subExperimentInfo =  (Map)subExperimentsListIterator.next();
					String subExperimentId = (String)subExperimentInfo.get(SELECT_ID);
					String subExperimentName = (String)subExperimentInfo.get(SELECT_NAME);
					leftSubExperimentIdList.add(subExperimentId);
					//subExperimentNameList.add(subExperimentName);
				}
			}

			int sizeTaskUniqueLeft = taskUniqueLeftList.size();
			int sizeTaskUniqueRight = taskUniqueRightList.size();

			String[] taskUniqueLeft = new String[sizeTaskUniqueLeft];
			String[] taskUniqueRight = new String[sizeTaskUniqueRight];

			for(int i = 0; i < sizeTaskUniqueLeft; i++){
				taskUniqueLeft[i] = (String)taskUniqueLeftList.get(i);
			}

			for(int i = 0; i< sizeTaskUniqueRight; i++){
				taskUniqueRight[i] = (String)taskUniqueRightList.get(i);
			}

			StringList uniqueLeftSelectables = new StringList(SELECT_CORRESPONDING_PROJECT_TASK_ID);
			uniqueLeftSelectables.add("to[Experiment].from.to[Project Access Key].from.from[Project Access List].to.id");
			uniqueLeftSelectables.add(SELECT_ID);
			DomainObject.MULTI_VALUE_LIST.add("to[Experiment].from.to[Project Access Key].from.from[Project Access List].to.id");

			if(isCompareExperiments){
				uniqueLeftSelectables.add(SELECT_CORRESPONDING_EXPERIMENT_TASK_IDS_OF_PROJECT_TASK);
				uniqueLeftSelectables.add(SELECT_CORRESPONDING_EXPERIMENT_ID);

				DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_EXPERIMENT_TASK_IDS_OF_PROJECT_TASK);
				DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_EXPERIMENT_ID);
			}



			MapList listTaskUniqueLeft = new MapList();
			MapList listTaskUniqueRight = new MapList();
			if(taskUniqueLeft.length > 0){
				listTaskUniqueLeft = DomainObject.getInfo(context, taskUniqueLeft, uniqueLeftSelectables);
			}


			StringList uniqueRightSelectables = new StringList(SELECT_CORRESPONDING_EXPERIMENT_TASK_ID);
			uniqueRightSelectables.add("from[Experiment].to.to[Project Access Key].from.from[Project Access List].to.id");
			uniqueRightSelectables.add(SELECT_ID);
			if(isCompareExperiments){
				uniqueRightSelectables.add(SELECT_CORRESPONDING_EXPERIMENT_TASK_IDS_OF_PROJECT_TASK);
				uniqueRightSelectables.add(SELECT_CORRESPONDING_EXPERIMENT_ID);

				DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_EXPERIMENT_TASK_IDS_OF_PROJECT_TASK);
				DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_EXPERIMENT_ID);
			}


			DomainObject.MULTI_VALUE_LIST.add("from[Experiment].to.to[Project Access Key].from.from[Project Access List].to.id");
			DomainObject.MULTI_VALUE_LIST.add(SELECT_CORRESPONDING_EXPERIMENT_TASK_ID);



			if(taskUniqueRight.length > 0){
				listTaskUniqueRight = DomainObject.getInfo(context, taskUniqueRight, uniqueRightSelectables);
			}


			Iterator listTaskUniqueLeftIterator = listTaskUniqueLeft.iterator();
			while(listTaskUniqueLeftIterator.hasNext()){
				Map taskUniqueLeftInfo = (Map)listTaskUniqueLeftIterator.next();
				String taskId = (String)taskUniqueLeftInfo.get(SELECT_ID);
				Object correspondingTaskId = taskUniqueLeftInfo.get(SELECT_CORRESPONDING_PROJECT_TASK_ID);
				Object correspondingProjectId = taskUniqueLeftInfo.get("to[Experiment].from.to[Project Access Key].from.from[Project Access List].to.id");

				if(isCompareExperiments){
					correspondingTaskId = taskUniqueLeftInfo.get(SELECT_CORRESPONDING_EXPERIMENT_TASK_IDS_OF_PROJECT_TASK);
					correspondingProjectId = taskUniqueLeftInfo.get(SELECT_CORRESPONDING_EXPERIMENT_ID);

				}


				String strTaskId = EMPTY_STRING;
				String strProjectId = EMPTY_STRING;

				if(correspondingProjectId instanceof StringList){
					StringList projectIds = (StringList)correspondingProjectId;
					for(int i=0; i<projectIds.size(); i++){
						if(rightSubExperimentIdList.contains((String)projectIds.get(i))){
							strTaskId = (String)projectIds.get(i);
						}
					}
				} else if(correspondingProjectId instanceof String){
					if(rightSubExperimentIdList.contains((String)correspondingProjectId)){
						strTaskId = (String)correspondingTaskId;
					}
				}

				if(ProgramCentralUtil.isNotNullString(strTaskId)){
					movedTaskList.add(taskId);
				}
			}


			Iterator listTaskUniqueRightIterator = listTaskUniqueRight.iterator();

			while(listTaskUniqueRightIterator.hasNext()){
				Map taskUniqueRightInfo = (Map)listTaskUniqueRightIterator.next();
				String taskId = (String)taskUniqueRightInfo.get(SELECT_ID);
				Object correspondingTaskId = taskUniqueRightInfo.get(SELECT_CORRESPONDING_EXPERIMENT_TASK_ID);
				Object correspondingProjectId = taskUniqueRightInfo.get("from[Experiment].to.to[Project Access Key].from.from[Project Access List].to.id");

				if(isCompareExperiments){
					correspondingTaskId = taskUniqueRightInfo.get(SELECT_CORRESPONDING_EXPERIMENT_TASK_IDS_OF_PROJECT_TASK);
					correspondingProjectId = taskUniqueRightInfo.get(SELECT_CORRESPONDING_EXPERIMENT_ID);

				}

				String strTaskId = EMPTY_STRING;
				String strProjectId = EMPTY_STRING;
				if(correspondingProjectId instanceof StringList){
					StringList projectIds = (StringList)correspondingProjectId;
					for(int i=0; i<projectIds.size(); i++){
						if(leftSubExperimentIdList.contains((String)projectIds.get(i))){
							strTaskId = (String)projectIds.get(i);
						}
					}

				}else if(correspondingProjectId instanceof String){
					if(leftSubExperimentIdList.contains((String)correspondingProjectId)){
						strTaskId = (String)correspondingTaskId;
					}
				}

				if(ProgramCentralUtil.isNotNullString(strTaskId)){
					movedTaskList.add(taskId);
				}
			}

			String rowAdded = "<img src=\"../common/images/iconStatusAdded.gif\" border=\"0\" />";
			String rowRemoved = "<img src=\"../common/images/iconStatusRemoved.gif\" border=\"0\" />";
			String rowMoved = "<img src=\"../common/images/iconStatusResequenced.gif\" border=\"0\" />";

			String projectSpaceIcon = "<img src=\"../common/images/iconSmallProject.gif\" border=\"0\" />";
			String experimentIcon = "<img src=\"../common/images/iconMenuMaterialsCompliance.gif\" border=\"0\" />";
			String taskIcon = "<img src=\"../common/images/iconSmallTask.gif\" border=\"0\" />";

			StringList slTaskManagementSubTypeList = ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_TASK_MANAGEMENT);
			StringList slExperimentSubTypeList = ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_EXPERIMENT);
			StringList slProjectSpaceSubTypeList = ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_PROJECT_SPACE);



			Iterator taskObjectListIterator = objectList.iterator();
			while(taskObjectListIterator.hasNext()){
				Map objectMap = (Map)taskObjectListIterator.next();
				String objectType = (String)objectMap.get(ProgramCentralConstants.SELECT_TYPE);

				if(slTaskManagementSubTypeList.contains(objectType)){
					objectMap.put("typeIcon", taskIcon);
				}else if(slExperimentSubTypeList.contains(objectType)){
					objectMap.put("typeIcon", experimentIcon);
				}else if(slProjectSpaceSubTypeList.contains(objectType)){
					objectMap.put("typeIcon", projectSpaceIcon);
				}
			}


			Iterator objectListIterator = objectList.iterator();
			while (objectListIterator.hasNext())
			{
				String changeIndicator = EMPTY_STRING;
				Map objectMap = (Map) objectListIterator.next();
				String parentProjectId = (String)objectMap.get(SELECT_TASK_PROJECT_ID);
				String matchResult = (String) objectMap.get("matchresult");
				String rootNode = (String) objectMap.get("Root Node");
				String typeIcon = (String) objectMap.get("typeIcon");

				String taskName = (String) objectMap.get("name");
				String taskId = (String) objectMap.get(SELECT_ID);
				StringBuffer sBuffer = new StringBuffer();

				if("left".equalsIgnoreCase(matchResult)){
					changeIndicator = rowAdded;
					if(movedTaskList.contains(taskId)){
						changeIndicator = rowMoved;

					}
				}else if("right".equalsIgnoreCase(matchResult)){
					changeIndicator = rowRemoved;
					if(movedTaskList.contains(taskId)){
						changeIndicator = rowMoved;
					} 

				} else{
					if("true".equalsIgnoreCase(rootNode)){
						DomainObject rootObject = DomainObject.newInstance(context,taskId);
						StringList selectable = new StringList(2);
						selectable.add(SELECT_NAME);
						selectable.add(SELECT_TYPE);
						Map rootNodeInfo = rootObject.getInfo(context, selectable);
						taskName = (String)rootNodeInfo.get(SELECT_NAME);
						String type = (String)rootNodeInfo.get(SELECT_TYPE);

						if(slExperimentSubTypeList.contains(type)){
							typeIcon = experimentIcon;
						}else if(slProjectSpaceSubTypeList.contains(type)){
							typeIcon = projectSpaceIcon;
						}

					}
				}
				sBuffer.append(changeIndicator);
				sBuffer.append(typeIcon);
				sBuffer.append(XSSUtil.encodeForXML(context, taskName));
				returnList.add(sBuffer.toString());
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return returnList;
		}
	}


	/**
	 * It returns true when submode is DifferenceOnly otherwise false. That is toggle from Entire structure to Diff. Only view.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean showCompareAllCommand(Context context, String args[]) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String subMode = (String)inputMap.get("subMode");
		boolean showCompareAll = false;
		if(ProgramCentralUtil.isNotNullString(subMode) && "DifferenceOnly".equalsIgnoreCase(subMode)){
			showCompareAll = true;
		}
		return showCompareAll;
	}

	/**
	 * It returns true when submode is EntireStructure otherwise false. That is toggle from Diff. Only view to Entire structure.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean showCompareDiffOnlyCommand(Context context, String args[]) throws Exception
	{
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		String subMode = (String)inputMap.get("subMode");
		boolean isDifferenceOnly = false;
		if(ProgramCentralUtil.isNullString(subMode) || "EntireStructure".equalsIgnoreCase(subMode)){
			isDifferenceOnly = true;
		}
		return isDifferenceOnly;
	}

	/**
	 * This method will check whether actual dates and duration columns should be displayed or not. For Experiment it will not displayed.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return True if object is other than Experiment.
	 * @throws Exception if operation fails.
	 */
	public boolean hasAccessToActualDatesColumn(Context context,String[]args)throws Exception{
		boolean hasAccess = true;
		Map programMap =  JPO.unpackArgs(args);
		String sProjectId = (String)programMap.get("objectId");
		DomainObject project = DomainObject.newInstance(context,sProjectId);


		String isExperiment  = project.getInfo(context, ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);

		if("TRUE".equalsIgnoreCase(isExperiment)){
			hasAccess = false;
		}

		return hasAccess;
	}


	// ########### New Experiment sync implementation ############################

	/**
	 * Sync data from left to right or vise-versa.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args  - Holds information about objects.
	 * @return Map with "Action = refresh" and "changedRows = xmlList".
	 * @throws Exception if operation fails.
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map postProcessAction(Context context,String[]args)throws Exception
	{
		System.out.println("Post process operation start.....");
		long start = System.currentTimeMillis();

		Map inputMap 		= (Map)JPO.unpackArgs(args);
		Document doc 		= (Document) inputMap.get("XMLDoc");
		Element rootElement = (Element)doc.getRootElement();
		List syncedObjectList    = rootElement.getChildren("object");

		Map requestMap 		= (Map) inputMap.get("requestMap");
		String objectId1 	= (String)requestMap.get("objectId1");
		String objectId2 	= (String)requestMap.get("objectId2");
		boolean leftSync 	= false;
		boolean rightSync 	= false;

		MapList syncedTaskDependencyList 	= new MapList();
		MapList syncedTaskSeqIdList 		= new MapList();
		MapList modifiedTaskInfoList 		= new MapList();
		MapList newlyAddedTaskInfoList 		= new MapList();

		Map returnMap = new HashMap(1);
		returnMap.put("Action","refresh");

		if(syncedObjectList != null){
			Iterator syncedObjectIterator  = syncedObjectList.iterator();
			while(syncedObjectIterator.hasNext()){
				Element syncedObjectRowInfo = (Element)syncedObjectIterator.next();

				//Synced row information
				String objectId = (String)syncedObjectRowInfo.getAttributeValue("objectId");
				String relId 	= (String)syncedObjectRowInfo.getAttributeValue("relId");
				String rowId 	= (String)syncedObjectRowInfo.getAttributeValue("rowId");
				String markup   = (String)syncedObjectRowInfo.getAttributeValue("markup");
				String syncDir  = (String)syncedObjectRowInfo.getAttributeValue("syncDir");

				if(Experiment.SYNC_DIR_LEFT.equalsIgnoreCase(syncDir)){
					leftSync = true;
				}

				if(Experiment.SYNC_DIR_RIGHT.equalsIgnoreCase(syncDir)){
					rightSync = true;
				}

				//Newly added/Deleted Tasks
				if(ProgramCentralUtil.isNullString(markup)){
					List childObjectList 	= (List)syncedObjectRowInfo.getChildren();
					if(childObjectList != null){
						Iterator childObjectIterator  = childObjectList.iterator();
						while(childObjectIterator.hasNext()){
							Element childObjectRowInfo = (Element)childObjectIterator.next();

							//child row information
							String childObjectId = (String)childObjectRowInfo.getAttributeValue("objectId");
							String childRelId 	 = (String)childObjectRowInfo.getAttributeValue("relId");
							String childRowId 	 = (String)childObjectRowInfo.getAttributeValue("rowId");
							String childMarkup   = (String)childObjectRowInfo.getAttributeValue("markup");
							String childSyncDir  = (String)childObjectRowInfo.getAttributeValue("syncDir");
							String matchResult   = (String)childObjectRowInfo.getAttributeValue("matchresult");

							Map<String,String> taskInfoMap = new HashMap<String,String>();
							taskInfoMap.put("objectId", childObjectId);
							taskInfoMap.put("relId", childRelId);
							taskInfoMap.put("rowId", childRowId);
							taskInfoMap.put("markup", childMarkup);
							taskInfoMap.put("syncDir", childSyncDir);
							taskInfoMap.put("matchresult", matchResult);

							if(Experiment.SYNC_DIR_LEFT.equalsIgnoreCase(childSyncDir)){
								leftSync = true;
							}

							if(Experiment.SYNC_DIR_RIGHT.equalsIgnoreCase(childSyncDir)){
								rightSync = true;
							}

							if(Experiment.MARKUP_ADD.equalsIgnoreCase(childMarkup)){
								List childObjectColmList 	= (List)childObjectRowInfo.getChildren();
								List childsofElementList = childObjectRowInfo.getChildren("column");

								if(childObjectColmList != null){

									Iterator childObjectColmIterator  = childObjectColmList.iterator();
									while(childObjectColmIterator.hasNext()) {
										Element childObjectColmInfo 	= (Element)childObjectColmIterator.next();

										String colName 	= (String)childObjectColmInfo.getAttributeValue("name");
										String colValue = (String)childObjectColmInfo.getTextTrim();

										taskInfoMap.put(colName, colValue);
									}
								}

								if(ProgramCentralUtil.isNotNullString(childRowId)){
									StringList rowIdList = FrameworkUtil.split(childRowId, ProgramCentralConstants.COMMA);
									taskInfoMap.put(KEY_LEVEL, String.valueOf(rowIdList.size()-1));
								}

							}else if(Experiment.MARKUP_CUT.equalsIgnoreCase(childMarkup)){
								taskInfoMap.put("pid", objectId);
							}

							newlyAddedTaskInfoList.add(taskInfoMap);
						}
					}

				}else if(Experiment.MARKUP_CHANGED.equalsIgnoreCase(markup)){
					//Column information
					List syncedObjectColmList 	= (List)syncedObjectRowInfo.getChildren();
					if(syncedObjectColmList != null){

						Map<String,String> taskEstimatedInfoMap = new HashMap<String,String>();
						taskEstimatedInfoMap.put("objectId", objectId);
						taskEstimatedInfoMap.put("relId", relId);
						taskEstimatedInfoMap.put("rowId", rowId);
						taskEstimatedInfoMap.put("markup", markup);
						taskEstimatedInfoMap.put("syncDir", syncDir);
						boolean isChanged = false;

						Iterator syncedObjectColmIterator  = syncedObjectColmList.iterator();
						while(syncedObjectColmIterator.hasNext()) {
							Element syncedObjectColmInfo 	= (Element)syncedObjectColmIterator.next();

							String colName 	= (String)syncedObjectColmInfo.getAttributeValue("name");
							String colValue = (String)syncedObjectColmInfo.getTextTrim();

							//Task Estimated plan information
							if("PhaseEstimatedDuration".equalsIgnoreCase(colName)
									||"PhaseEstimatedStartDate".equalsIgnoreCase(colName)||"PhaseEstimatedEndDate".equalsIgnoreCase(colName)
									||"Constraint Date".equalsIgnoreCase(colName)||"ConstraintType".equalsIgnoreCase(colName)
									||"Name".equalsIgnoreCase(colName)||"Description".equalsIgnoreCase(colName)){

								taskEstimatedInfoMap.put(colName, colValue);
								isChanged = true;

							}else if("Dependency".equals(colName)) {

								Map<String,String> taskDependencyInfoMap = new HashMap<String,String>();
								taskDependencyInfoMap.put("objectId", objectId);
								taskDependencyInfoMap.put("relId", relId);
								taskDependencyInfoMap.put("rowId", rowId);
								taskDependencyInfoMap.put("markup", markup);
								taskDependencyInfoMap.put("syncDir", syncDir);
								taskDependencyInfoMap.put(colName, colValue);
								syncedTaskDependencyList.add(taskDependencyInfoMap);

							}else if("ID".equals(colName)) {

								Map<String,String> taskDependencyInfoMap = new HashMap<String,String>();
								taskDependencyInfoMap.put("objectId", objectId);
								taskDependencyInfoMap.put("relId", relId);
								taskDependencyInfoMap.put("rowId", rowId);
								taskDependencyInfoMap.put("markup", markup);
								taskDependencyInfoMap.put("syncDir", syncDir);
								taskDependencyInfoMap.put(colName, colValue);

								syncedTaskSeqIdList.add(taskDependencyInfoMap);
							}
						}
						if(isChanged){
							modifiedTaskInfoList.add(taskEstimatedInfoMap);
							isChanged = false;
						}
					}
				}
			}
		}

		//Sync all changes
		MapList newXmlInfoList = experiment.synceWBSObjects(context, 
				newlyAddedTaskInfoList,
				syncedTaskDependencyList, 
				syncedTaskSeqIdList,
				modifiedTaskInfoList,
				objectId1,
				objectId2,
				leftSync,
				rightSync);

		returnMap.put("changedRows", newXmlInfoList);

		long end = System.currentTimeMillis();
		System.out.println("Post process operation ends.....::"+(end-start));

		return returnMap;
	}

	/**
	 * Get constraint type
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param args - Holds object information.
	 * @return constraint type list with english and non-english.
	 * @throws Exception
	 */
	public StringList getConstraintsType(Context context, String[] args) throws Exception
	{
		String language 	= context.getSession().getLanguage();

		Map projectMap 		= (Map) JPO.unpackArgs(args);
		MapList objectList 	= (MapList) projectMap.get("objectList");
		int objectSize 		= objectList.size();

		StringList constraintTypeList = new StringList(objectSize);

		String[] objectIds = new String[objectSize];
		for (int i = 0; i < objectSize; i++) {
			Map mapObject = (Map) objectList.get(i);
			String taskId = (String) mapObject.get(DomainObject.SELECT_ID);
			objectIds[i] = taskId;
		}

		StringList busSelect = new StringList(3);
		busSelect.addElement(ProjectManagement.SELECT_TASK_CONSTRAINT_TYPE);
		busSelect.addElement(ProjectManagement.SELECT_DEFAULT_CONSTRAINT_TYPE);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);

		MapList objectInfoList = getObjectDetails(context, objectIds, busSelect);

		for(int i=0; i<objectSize; i++){
			Map objectInfo = (Map)objectInfoList.get(i);

			String isProjectSpace 	= (String)objectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String constraintType 	= (String)objectInfo.get(ProjectManagement.SELECT_TASK_CONSTRAINT_TYPE);

			if("true".equalsIgnoreCase(isProjectSpace)){
				constraintType 	= (String)objectInfo.get(ProjectManagement.SELECT_DEFAULT_CONSTRAINT_TYPE);
			}

			String displayValue = EnoviaResourceBundle.getRangeI18NString(context,ATTRIBUTE_TASK_CONSTRAINT_TYPE, constraintType, language);


			Map<String,String> obejectMap = new HashMap<String, String>();
			obejectMap.put("ActualValue", constraintType);
			obejectMap.put("DisplayValue", displayValue);

			constraintTypeList.add(obejectMap);
		}

		return constraintTypeList;
	}
	/**
	 * Get object details based on selectable.
	 * @param context - The ENOVIA <code>Context</code> object.
	 * @param objectIds - Array of object ids.
	 * @param objectSelects - bus selectable.
	 * @return object information list.
	 * @throws Exception if operation fails.
	 */
    private MapList getObjectDetails(Context context,String[] objectIds,StringList objectSelects)throws MatrixException {
    	
       MapList objectInfoList = new MapList();
       if(objectIds == null || objectIds.length == 0){
              return objectInfoList; 
       }
       try {
              ProgramCentralUtil.pushUserContext(context);
              objectInfoList = DomainObject.getInfo(context, objectIds, objectSelects);
       } finally {
              ProgramCentralUtil.popUserContext(context);
       }
       return objectInfoList;
    }
    
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postProcessRefreshAfterEdit (Context context, String[] args) throws Exception { 
    	Map returnHashMap = new HashMap();
    	String isUpdated = PropertyUtil.getGlobalRPEValue(context,"expUpdated");
    	StringBuilder output = new StringBuilder();
    	if(ProgramCentralUtil.isNotNullString(isUpdated) && "true".equalsIgnoreCase(isUpdated)){ 

    		output.append("{");
    		output.append("main:function() {");
    		output.append("var detailsDisplay_1 = findFrame(getTopWindow(), \"PMCWhatIfProjectChangeList\");"); 
    		output.append("var detailsDisplay_2 = findFrame(getTopWindow(), \"PMCWhatIfProjectExperimentsList\");"); 
    		output.append("detailsDisplay_1.refreshSBTable();"); 
    		output.append("detailsDisplay_2.refreshSBTable();"); 
    		output.append("}}"); 
        	returnHashMap.put("Action","execScript");
        	returnHashMap.put("Message", output.toString());

    	}else{
    		returnHashMap.put("Action","refresh");
    	}
    	return returnHashMap;
    }


}

