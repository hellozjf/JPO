
/*   emxProjectBaselineBase
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;
import com.matrixone.apps.common.SubtaskRelationship;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectBaseline;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;


/**
 * The <code>emxProjectBaselineBase</code> class contains methods for Project Baseline.
 *
 * @version PMC R418 - Copyright(c) 2013, MatrixOne, Inc.
 */

public class emxProjectBaselineBase_mxJPO extends DomainObject
{

	private final String SELECT_CURRENT_ESTIMATED_START_DATE = "Current Estimated Start Date";
	private final String SELECT_CURRENT_ESTIMATED_FINISH_DATE = "Current Estimated Finish Date";
	private final String SELECT_CURRENT_ESTIMATED_DURATION = "Current Estimated Duration";
	private final String SELECT_CURRENT_ESTIMATED_DURATION_UNIT = "Current Estimated Duration Unit";

	public emxProjectBaselineBase_mxJPO (Context context, String[] args)	throws Exception{
		// Call the super constructor
		super();
		if (args != null && args.length > 0){
			setId(args[0]);
		}
	}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWBSTasks(Context context, String[] args ) throws Exception{
			Map programMap = (Map) JPO.unpackArgs(args);
			String baselineId = (String) programMap.get("objectId");	//Must be baseline
			String projectId = (String) programMap.get("parentOID");	//Must be project
		String sExpandLevel = (String) programMap.get("expandLevel");
		short expandLevel = ProgramCentralUtil.getExpandLevel(sExpandLevel);
		
			String typePattern = ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT;
		String relPattern = ProgramCentralConstants.RELATIONSHIP_SUBTASK;
			StringList objectSelects = new StringList(5);
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);
			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
			objectSelects.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_SOURCE_ID);
			objectSelects.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			objectSelects.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
			objectSelects.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
			objectSelects.addElement(ProgramCentralConstants.SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT);
			objectSelects.addElement("from[" + relPattern + "]");

			StringList relationshipSelects = new StringList(4);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
			relationshipSelects.addElement(DomainConstants.SELECT_LEVEL);
			relationshipSelects.addElement(SubtaskRelationship.SELECT_SEQUENCE_ORDER);
			relationshipSelects.addElement("from.id");//Added for "What if"

		ProjectBaseline baseline = new ProjectBaseline(baselineId);
		MapList wbs = baseline.getSubtasks(context, projectId, objectSelects, relationshipSelects, expandLevel);
		return wbs;
	}

	//TODO This method should be part of emxProjectSpaceBase.
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProjectBaselines(Context context, String[] args ) throws MatrixException{
		MapList baselines = new MapList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			ProjectSpace project = new ProjectSpace(objectId);
			StringList objectSelects = new StringList(5);
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);
			objectSelects.addElement(DomainConstants.SELECT_CURRENT);
			StringList relationshipSelects = new StringList();
			baselines = project.getProjectBaselines(context, objectSelects, relationshipSelects);
			return baselines;
		}catch(Exception exception){ throw new MatrixException(exception); }
		}

	/**
	 * This method will return the StringList containing names of all objects.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return list of names of all objects.
	 * @throws Exception if operation fails.
	 */
	public StringList getNameColumn(Context context,String[]args)throws Exception
	{
		try{
			StringList selectable = new StringList();
			Map programMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			String projectId = (String)paramList.get("objectId");
			StringList taskNameList = new StringList(objectList.size());

			String []taskIdArray = new String[objectList.size()];
			for(int i=0;i<objectList.size();i++){
				Map objectMap = (Map)objectList.get(i);
				String taskId = (String) objectMap.get(DomainObject.SELECT_ID);
				taskIdArray[i] = taskId;
			}

			selectable.addElement(DomainObject.SELECT_NAME);
			selectable.addElement(DomainObject.SELECT_ID);
			MapList objectInfoList = DomainObject.getInfo(context, taskIdArray, selectable);

			for(int i=0; i<objectInfoList.size();i++){
				Map <String,String>taskMap = (Map)objectInfoList.get(i);
				String name = taskMap.get(DomainObject.SELECT_NAME);
				String objectId = taskMap.get(DomainObject.SELECT_ID);
				String strURL = "../programcentral/emxProgramCentralWhatIfAnalysis.jsp?mode=launchProjectBaselineWBS&amp;objectid=" + XSSUtil.encodeForURL(context, objectId) + "&amp;parentOID=" + XSSUtil.encodeForURL(context, projectId);
				StringBuilder sbLink = new StringBuilder("<a target='listHidden' href='");
				sbLink.append(strURL);
				sbLink.append("' class='object'>");
				sbLink.append(name);
				sbLink.append("</a>");
				taskNameList.addElement(sbLink.toString());
			}

			return taskNameList;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map postProcessRefresh (Context context, String[] args) throws Exception
	{	
		Map <String,String> returnMap = new HashMap<String,String>(1);
		returnMap.put("Action","refresh");
		return returnMap;
	}

	public StringList getTaskNames(Context context, String[]args)throws Exception{
		try{
			StringList selectable = new StringList();
			Map programMap =  JPO.unpackArgs(args);
			Map paramList = (Map)programMap.get("paramList");

			String portal = (String) paramList.get("portal");
			String parentOID = (String) paramList.get("parentOID");

			MapList objectList = (MapList) programMap.get("objectList");
			StringList slNames = new StringList();
			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				Map taskInfo  = (Map) iterator.next();
				String id  = (String) taskInfo.get(ProgramCentralConstants.SELECT_ID);
				String type = (String) taskInfo.get(ProgramCentralConstants.SELECT_TYPE);
				String name = (String) taskInfo.get(ProgramCentralConstants.SELECT_NAME);
				DomainObject task = DomainObject.newInstance(context, id);
				if(UIUtil.isNullOrEmpty(name)){
					StringList slBusSelect = new StringList();
					slBusSelect.add("to[Project Baseline].from.name");
					slBusSelect.add(ProgramCentralConstants.SELECT_NAME);
					if(task.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE)){
						Map rootNodeInfo = task.getInfo(context, slBusSelect);
						name = (String)rootNodeInfo.get(SELECT_NAME);
					}
				}
				StringBuffer sbResult = new StringBuffer();
				sbResult.append("<a href=''>");
				sbResult.append(name);
				sbResult.append("</a>");
				slNames.add(sbResult.toString());
			}
			return slNames;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	public StringList getCurrentEstimatedDuration(Context context, String[]args)throws MatrixException{
		try{
			String ctxLang = context.getLocale().getLanguage();
			String strI18Days = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.DurationUnits.Days", ctxLang);
			String strI18Hours = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.DurationUnits.Hours", ctxLang);

			Map programMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramMap = (Map) programMap.get("paramList");
			StringList slDuration = new StringList();

			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				Map taskInfo = (Map) iterator.next();
				String type = (String) taskInfo.get(ProgramCentralConstants.SELECT_TYPE);
				String level = (String) taskInfo.get(ProgramCentralConstants.SELECT_LEVEL);
				String duration = EMPTY_STRING;
				String durationUnit = EMPTY_STRING;
				if(ProgramCentralConstants.TYPE_PROJECT_BASELINE.equals(type) && "0".equals(level)){
					String projectId = (String) paramMap.get("parentOID");
					StringList busSelect = new StringList();
					busSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
					busSelect.add(ProgramCentralConstants.SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT);
					DomainObject project = DomainObject.newInstance(context, projectId);
					Map<String, String> projectInfo = project.getInfo(context, busSelect);
					duration = projectInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
					durationUnit = projectInfo.get(ProgramCentralConstants.SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT);
				}else{
					duration = (String) taskInfo.get(SELECT_CURRENT_ESTIMATED_DURATION);
					durationUnit = (String) taskInfo.get(SELECT_CURRENT_ESTIMATED_DURATION_UNIT);
				}

				if(UIUtil.isNotNullAndNotEmpty(duration)){
				double _duration = Task.parseToDouble(duration);
				if("h".equalsIgnoreCase(durationUnit)){
					_duration = _duration * 8;	//8 is multiplier for conversion between days and hours
					duration = Double.toString(_duration);
					duration= duration + ProgramCentralConstants.SPACE + strI18Hours;
				} else {
					duration = duration + ProgramCentralConstants.SPACE + strI18Days;
				}
				}
				slDuration.add(duration);
			}
			return slDuration;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	public StringList getCurrentEstimatedDurationStyleInfo(Context context, String[]args)throws MatrixException{
		try{
			String ctxLang = context.getLocale().getLanguage();
			String strI18Days = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.DurationUnits.Days", ctxLang);
			String strI18Hours = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.DurationUnits.Hours", ctxLang);

			Map programMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			StringList slStyle = new StringList();

			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				Map taskInfo = (Map) iterator.next();
				String type = (String) taskInfo.get(ProgramCentralConstants.SELECT_TYPE);
				String level = (String) taskInfo.get(ProgramCentralConstants.SELECT_LEVEL);
				String oDuration = EMPTY_STRING;
				String duration = EMPTY_STRING;
				String durationUnit = EMPTY_STRING;
				if(ProgramCentralConstants.TYPE_PROJECT_BASELINE.equals(type) && "0".equals(level)){
					String baselineId = (String) paramList.get("objectId");
					String projectId = (String) paramList.get("parentOID");
					StringList busSelect = new StringList();
					busSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
					busSelect.add(ProgramCentralConstants.SELECT_TASK_ESTIMATED_DURATION_INPUTUNIT);

					DomainObject baseline = DomainObject.newInstance(context, baselineId);
					Map<String, String> baselineInfo = baseline.getInfo(context, busSelect);

					DomainObject project = DomainObject.newInstance(context, projectId);
					Map<String, String> projectInfo = project.getInfo(context, busSelect);

					duration = projectInfo.get("attribute[Task Estimated Duration]");
					durationUnit = projectInfo.get("to[Project Baseline].from.attribute[Task Estimated Duration].value");
					oDuration = baselineInfo.get("attribute[Task Estimated Duration]");
				}else{
					duration = (String)taskInfo.get(SELECT_CURRENT_ESTIMATED_DURATION);
					durationUnit = (String)taskInfo.get(SELECT_CURRENT_ESTIMATED_DURATION_UNIT);
					oDuration = (String)taskInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
				}

				double diff = 0;
				if(UIUtil.isNotNullAndNotEmpty(duration)){
				double _duration = Task.parseToDouble(duration);
				double _oDuration = Task.parseToDouble(oDuration);
					diff = Math.abs(_duration - _oDuration);
				}
				if(diff > 0){
					slStyle.add("custYellow");
				}else{
					slStyle.add(EMPTY_STRING);
				}
			}
			return slStyle;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	public StringList getCurrentEstimatedStartDate(Context context, String[]args)throws MatrixException{
		try{
			Map programMap =  JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramList");
			MapList objectList = (MapList) programMap.get("objectList");

			StringList slDates = new StringList();
			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				Map taskInfo = (Map) iterator.next();
				String startDate = EMPTY_STRING;
				String type = (String) taskInfo.get(ProgramCentralConstants.SELECT_TYPE);
				String level = (String) taskInfo.get(SELECT_LEVEL);
				if(ProgramCentralConstants.TYPE_PROJECT_BASELINE.equals(type) && ("0".equals(level))){
					String projectId = (String) paramMap.get("parentOID");	
					DomainObject project = DomainObject.newInstance(context, projectId);
					startDate = project.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
				}else{
					startDate = (String)taskInfo.get(SELECT_CURRENT_ESTIMATED_START_DATE);	
				}
				slDates.add(startDate);
			}
			return slDates;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	public StringList getCurrentEstimatedStartDateStyleInfo(Context context, String[]args)throws MatrixException{
		try{
			Map programMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map params = (Map) programMap.get("paramList");
			StringList slStyles = new StringList();

			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				Map taskInfo = (Map) iterator.next();
				String type = (String) taskInfo.get(ProgramCentralConstants.SELECT_TYPE);
				String level = (String) taskInfo.get(ProgramCentralConstants.SELECT_LEVEL);
				String startDate = EMPTY_STRING;
				String oStartDate = EMPTY_STRING;
				if(ProgramCentralConstants.TYPE_PROJECT_BASELINE.equals(type) && "0".equals(level)){
					String baselineId = (String) params.get("objectId");	
					String projectId = (String) params.get("parentOID");	
					DomainObject project = DomainObject.newInstance(context, projectId);
					startDate = project.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
					DomainObject baseline = DomainObject.newInstance(context, baselineId);
					oStartDate = baseline.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
				}else{
					startDate = (String)taskInfo.get(SELECT_CURRENT_ESTIMATED_START_DATE);	
					oStartDate = (String)taskInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
				}

				if(!oStartDate.equals(startDate)){
					slStyles.add("custYellow");
				}else{
					slStyles.add(EMPTY_STRING);				
				}
			}
			return slStyles;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	public StringList getCurrentEstimatedFinishDate(Context context, String[]args)throws MatrixException{
		try{
			Map programMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			StringList slDates = new StringList();

			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				Map taskInfo = (Map) iterator.next();
				String finishDate = EMPTY_STRING;
				String type = (String) taskInfo.get(ProgramCentralConstants.SELECT_TYPE);
				String level = (String) taskInfo.get(SELECT_LEVEL);
				if(ProgramCentralConstants.TYPE_PROJECT_BASELINE.equals(type) && ("0".equals(level))){
					String projectId = (String) paramList.get("parentOID");	
					DomainObject project = DomainObject.newInstance(context, projectId);
					finishDate = project.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
				}else{
					finishDate = (String)taskInfo.get(SELECT_CURRENT_ESTIMATED_FINISH_DATE);	
				}
				slDates.add(finishDate);
			}
			return slDates;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	public StringList getCurrentEstimatedFinishDateStyleInfo(Context context, String[]args)throws MatrixException{
		try{
			Map programMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			StringList slStyles = new StringList();

			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				Map taskInfo = (Map) iterator.next();
				String type = (String) taskInfo.get(ProgramCentralConstants.SELECT_TYPE);
				String level = (String) taskInfo.get(ProgramCentralConstants.SELECT_LEVEL);
				String finishDate = EMPTY_STRING;
				String oFinishDate = EMPTY_STRING;
				if(ProgramCentralConstants.TYPE_PROJECT_BASELINE.equals(type) && "0".equals(level)){
					String baselineId = (String) paramList.get("objectId");	
					String projectId = (String) paramList.get("parentOID");	
					DomainObject project = DomainObject.newInstance(context, projectId);
					finishDate = project.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
					DomainObject baseline = DomainObject.newInstance(context, baselineId);
					oFinishDate = baseline.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
				}else{
					finishDate = (String)taskInfo.get(SELECT_CURRENT_ESTIMATED_FINISH_DATE);	
					oFinishDate = (String)taskInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
				}

				if(!oFinishDate.equals(finishDate)){
					slStyles.add("custYellow");
				}else{
					slStyles.add(EMPTY_STRING);				
				}
			}
			return slStyles;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean isCurrentPlanRequired(Context context,String[]args)throws Exception{
		Map programMap =  JPO.unpackArgs(args);
		String objectId = (String)programMap.get("objectId");
		DomainObject object = DomainObject.newInstance(context, objectId);
		return object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_BASELINE);
	}

	public StringList getDeltaDurationStyleInfo(Context context, String[]args)throws FrameworkException{
		try{
			Map programMap =  JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			StringList slStyles = new StringList();

			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				Map objectInfo = (Map) iterator.next();
				slStyles.add("right-align");
			}
			return slStyles;
		}catch(Exception e){
			throw new FrameworkException(e);
		}
	}
	
	public StringList getTaskDependencyColumn(Context context, String[] args) throws Exception
	{
		Map programMap = (HashMap) JPO.unpackArgs(args);
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
		selectables.add(ProgramCentralConstants.SELECT_PREDECESSOR_LAG_TIME_INPUT);
		selectables.add(ProgramCentralConstants.SELECT_PREDECESSOR_LAG_TIME_UNITS);
		selectables.add(Task.SELECT_PREDECESSOR_IDS);
		selectables.add(ProgramCentralConstants.SELECT_PROJECT_ACCESS_KEY_ID);
		selectables.add(ProgramCentralConstants.SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR);
		selectables.add(ProgramCentralConstants.SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER);

		//adk call to retrieve task dependency information for each task.
		BusinessObjectWithSelectList bwsl = new BusinessObjectWithSelectList();        

		try{
			ProgramCentralUtil.pushUserContext(context);			
			bwsl = BusinessObject.getSelectBusinessObjectData(context, taskIds, selectables);        
		}finally{
			ProgramCentralUtil.popUserContext(context);			
		}


		StringList results = new StringList(objectList.size());

		StringList predecessorTypes = new StringList();
		StringList predecessorLagTimes = new StringList();
		StringList predecessorLagTimeUnits = new StringList();
		StringList predecessorIds = new StringList();
		StringList projectAccessKeyIds = new StringList();
		StringList projectAccessKeyIdsForPredecessor = new StringList();
		StringList predecessorTaskSequenceOrders = new StringList();

		Map palMapCache = new HashMap();
		

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
				predecessorLagTimes = getSelectableValues(bws, ProgramCentralConstants.SELECT_PREDECESSOR_LAG_TIME_INPUT, predecessorLagTimes);
				predecessorLagTimeUnits = getSelectableValues(bws, ProgramCentralConstants.SELECT_PREDECESSOR_LAG_TIME_UNITS, predecessorLagTimeUnits);
				projectAccessKeyIds = getSelectableValues(bws, ProgramCentralConstants.SELECT_PROJECT_ACCESS_KEY_ID, projectAccessKeyIds);
				projectAccessKeyIdsForPredecessor = getSelectableValues(bws, ProgramCentralConstants.SELECT_PROJECT_ACCESS_KEY_ID_FOR_PREDECESSOR, projectAccessKeyIdsForPredecessor);
				predecessorTaskSequenceOrders = getSelectableValues(bws, ProgramCentralConstants.SELECT_PREDECESSOR_TASK_ATTRIBUTE_SEQUENCE_ORDER, predecessorTaskSequenceOrders);
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
					String projectName = EMPTY_STRING;
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

						StringList projectInfo = FrameworkUtil.split(output, "|");
						predecessorProjectType = (String) projectInfo.get(0);
						palMapCache.put(projectAccessKeyIdForPredecessor, predecessorProjectType);
						palMapCache.put("name:" + projectAccessKeyIdForPredecessor, projectInfo.get(1));
						palMapCache.put("current:"+ projectAccessKeyIdForPredecessor, projectInfo.get(2));
						palMapCache.put("isAccssible:"+ projectAccessKeyIdForPredecessor, isObjectInaccessible);
					}

					if (!projectAccessKeyId.equals(projectAccessKeyIdForPredecessor))
					{
						
						//dependent tasks are from different projects; prefix dependency label with project name.
						projectName = (String) palMapCache.get("name:" + projectAccessKeyIdForPredecessor);
						externalProjectState = (String)palMapCache.get("current:"+ projectAccessKeyIdForPredecessor);
						isObjectInaccessible = (boolean)palMapCache.get("isAccssible:"+ projectAccessKeyIdForPredecessor);
					}

					if (i > 0)
					{
						
						toolTip.append(",");
						value.append(",");
					}

					String tip = EMPTY_STRING;
					if (ProgramCentralUtil.isNotNullString(projectName))
					{
						tip = projectName + ":" + predecessorTaskSequenceOrder + ":" + predecessorType;
					}
					else
					{
						tip = predecessorTaskSequenceOrder + ":" + predecessorType;
					}
					tip += Task.parseToDouble(predecessorLagTime) < 0 ? "" : "+";
					tip += predecessorLagTime + " " + predecessorLagTimeUnit;

					value.append(tip);
					toolTip.append(tip);
				}
			}
			if (!"".equals(toolTip))
			{   
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

		}
		return results;
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
}

