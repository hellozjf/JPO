/*
 * ${CLASSNAME}.java
 * program for source id migration.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.common.Task;
import com.matrixone.apps.common.WorkCalendar;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.WeeklyTimesheet;

public class emxProgramManagementMigrationBase_mxJPO extends emxCommonMigrationBase_mxJPO
{

	private final String ATTRIBUTE_NEEDS_REVIEW_DEFAULT_VALUE = "Yes";

	public emxProgramManagementMigrationBase_mxJPO(Context context, String[] args)
			throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}

	public void migrateObjects(Context context, StringList objectList) throws Exception{
		StringList slTemplateIds = new StringList();
		StringList slProjectIds = new StringList();
		StringList slConceptIds = new StringList();
		StringList slExperimentIds = new StringList();
		StringList slTimesheetIds = new StringList();
		StringList slCalendarIds = new StringList();
		StringList slFinancialItemIds = new StringList();
		StringList slWorkspaceVaultIds = new StringList();

		for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
			String objectId = (String) iterator.next();
			DomainObject object = new DomainObject(objectId);

			/******************* DON'T CHANGE THE ORDER OF OBJECT MIGRATION. **********************/
			if(object.exists(context)){
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_TEMPLATE)){
					slTemplateIds.add(objectId);
				}
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_CONCEPT)){
					slConceptIds.add(objectId);
				}
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE)
						&& !object.isKindOf(context, ProgramCentralConstants.TYPE_EXPERIMENT)){
					slProjectIds.add(objectId);
				}
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_EXPERIMENT)){
					slExperimentIds.add(objectId);
				}
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_WEEKLY_TIMESHEET)){
					slTimesheetIds.add(objectId);
				}
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_WORK_CALENDAR)){
					slCalendarIds.add(objectId);
				}
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_VAULT)){
					slWorkspaceVaultIds.add(objectId);
				}
				if(object.isKindOf(context, ProgramCentralConstants.TYPE_FINANCIAL_ITEM)){
					slFinancialItemIds.add(objectId);
				}
			}
		}

		updateProjectTemplate(context, slTemplateIds);
		updateProjectConcept(context, slConceptIds);
		updateProjectSpace(context, slProjectIds);
		updateExperiment(context, slExperimentIds);
		updateWeeklyTimesheet(context, slTimesheetIds);
		updateWorkCalendar(context, slCalendarIds);
		migrateFinancialItemObjects(context, slFinancialItemIds);
		migrateDefaultUserAccess(context, slWorkspaceVaultIds);
	}

	private void update(Context context, String objectId, String sourceId) throws Exception{
		try{
			Task task = new Task(objectId);
			if(ProgramCentralUtil.isNotNullString(sourceId)){
				task.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_SOURCE_ID, sourceId);
			}

			loadMigratedOids(objectId);
		}catch(Exception e){
			writeUnconvertedOID(ProgramCentralConstants.EMPTY_STRING, objectId);
		}
	}

	private void update(Context context, String objectId, String sourceId, String needsReview) throws Exception{
		try{
			Task task = new Task(objectId);
			if(ProgramCentralUtil.isNotNullString(sourceId)){
				task.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_SOURCE_ID, sourceId);
			}
			task.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_NEEDS_REVIEW, needsReview);

			loadMigratedOids(objectId);
		}catch(Exception e){
			writeUnconvertedOID(ProgramCentralConstants.EMPTY_STRING, objectId);
		}
	}

	private void updateProjectTemplate(Context context, StringList ids) throws FrameworkException{
		StringList objectSelects = new StringList();
		objectSelects.add(ProgramCentralConstants.SELECT_ID);
		objectSelects.add(ProgramCentralConstants.SELECT_PHYSICALID);
		try{
			for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
				String objectId = (String) iterator.next();
				Task template = new Task(objectId);
				Map objectInfo = template.getInfo(context, objectSelects);
				String physicalId = (String) objectInfo.get(ProgramCentralConstants.SELECT_PHYSICALID);				

				//set SourceId of template. 
				update(context, objectId, physicalId);

				//Get all tasks
				MapList wbs = template.getTasks(context, template, 0, objectSelects, null, true, false);

				//Iterate wbs tasks, and set physical id as sourceId. 
				for (Iterator itrWbs = wbs.iterator(); itrWbs.hasNext();) {
					Map taskInfo = (Map) itrWbs.next();
					physicalId = (String) taskInfo.get(ProgramCentralConstants.SELECT_PHYSICALID);				
					objectId = (String) taskInfo.get(SELECT_ID);
					update(context, objectId, physicalId, ATTRIBUTE_NEEDS_REVIEW_DEFAULT_VALUE);			
				}				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void updateProjectConcept(Context context, StringList ids) throws FrameworkException{
		updateProjectTemplate(context, ids);
	}

	private void updateProjectSpace(Context context, String objectId) throws Exception{
		String SELECT_PROJECT_CALENDAR_ID = "from[" + ProgramCentralConstants.RELATIONSHIP_CALENDAR + "].id";
		StringList objectSelects = new StringList();
		objectSelects.add(ProgramCentralConstants.SELECT_ID);
		objectSelects.add(ProgramCentralConstants.SELECT_PHYSICALID);
		objectSelects.add(SELECT_PROJECT_CALENDAR_ID);

		try{
			ProjectSpace project = new ProjectSpace(objectId);
			Map objectInfo = project.getInfo(context, objectSelects);

			//Set physical id as source id
			String physicalId = (String) objectInfo.get(ProgramCentralConstants.SELECT_PHYSICALID);				
			project.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_SOURCE_ID, physicalId);

			//Make the connected Calendar as Default Calendar. 
			String relId = (String) objectInfo.get(SELECT_PROJECT_CALENDAR_ID);				
			if(UIUtil.isNotNullAndNotEmpty(relId)){
				DomainRelationship.setType(context, relId, ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR);
			}
			loadMigratedOids(objectId);
		}catch(Exception e){
			writeUnconvertedOID(ProgramCentralConstants.EMPTY_STRING, objectId);
		}
	}

	private void updateProjectSpace(Context context, StringList ids) throws FrameworkException{
		try{
			for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
				String objectId = (String) iterator.next();
				//Update Project Space. 
				updateProjectSpace(context, objectId);

				//Get all tasks
				StringList objectSelects = new StringList();
				objectSelects.add(ProgramCentralConstants.SELECT_ID);
				objectSelects.add(ProgramCentralConstants.SELECT_PHYSICALID);
				Task template = new Task(objectId);
				MapList wbs = template.getTasks(context, template, 0, objectSelects, null, true, false);

				//Iterate wbs tasks, and set physical id as sourceId. 
				for (Iterator itrWbs = wbs.iterator(); itrWbs.hasNext();) {
					Map taskInfo = (Map) itrWbs.next();
					String physicalId = (String) taskInfo.get(ProgramCentralConstants.SELECT_PHYSICALID);				
					objectId = (String) taskInfo.get(SELECT_ID);
					update(context, objectId, physicalId, ATTRIBUTE_NEEDS_REVIEW_DEFAULT_VALUE);			
				}				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void updateExperiment(Context context, StringList ids) throws FrameworkException{
		String SELECT_MASTER_SOURCE_ID = "to[Experiment].from.attribute[Source Id]";
		StringList objectSelects = new StringList();
		objectSelects.add(ProgramCentralConstants.SELECT_ID);
		objectSelects.add(SELECT_MASTER_SOURCE_ID);

		try{
			for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
				String objectId = (String) iterator.next();
				Task experiment = new Task(objectId);
				Map objectInfo = experiment.getInfo(context, objectSelects);
				String sourceId = (String) objectInfo.get(SELECT_MASTER_SOURCE_ID);				

				//set SourceId of template. 
				update(context, objectId, sourceId);

				//Get all tasks
				MapList wbs = experiment.getTasks(context, experiment, 0, objectSelects, null, true, false);

				//Iterate wbs tasks, and set physical id as sourceId. 
				for (Iterator itrWbs = wbs.iterator(); itrWbs.hasNext();) {
					Map taskInfo = (Map) itrWbs.next();
					sourceId = (String) taskInfo.get(SELECT_MASTER_SOURCE_ID);				
					objectId = (String) taskInfo.get(SELECT_ID);
					update(context, objectId, sourceId, ATTRIBUTE_NEEDS_REVIEW_DEFAULT_VALUE);			
				}

			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void updateWeeklyTimesheet(Context context, StringList ids) throws FrameworkException{
		try{
			String SELECT_TASK_ID = "to["+RELATIONSHIP_HAS_EFFORTS+"].from.id";
			//get the Context Approver value from the emxSystem
			String strApprover = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.WeeklyTimesheet.ApproverSelection");

			//Perform migration update only when Context approver is set to P.  
			if("P".equals(strApprover)){
				StringList objectSelects = new StringList();
				objectSelects.add(ProgramCentralConstants.SELECT_CURRENT);
				objectSelects.add(ProgramCentralConstants.SELECT_ID);

				//Iterate all the timesheets.
				for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
					String timesheetId = (String) iterator.next();

					objectSelects = new StringList();
					objectSelects.add(ProgramCentralConstants.SELECT_ID);
					objectSelects.add(ProgramCentralConstants.SELECT_TYPE);
					StringList relSelects = new StringList();
					relSelects.add(DomainRelationship.SELECT_ID);

					WeeklyTimesheet wt = new WeeklyTimesheet(timesheetId);

					//Get all the approver contexts that are projects
					MapList mlProjects = wt.getRelatedObjects(context,
							RELATIONSHIP_APPROVER_CONTEXT, //pattern to match relationships
							TYPE_PROJECT_SPACE, //pattern to match types
							objectSelects, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
							relSelects, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
							false, //get To relationships
							true, //get From relationships
							(short)1, //the number of levels to expand, 0 equals expand all.
							EMPTY_STRING, //where clause to apply to objects, can be empty ""
							EMPTY_STRING,
							0);

					//Get Weekly timesheet efforts.
					MapList efforts = wt.getEfforts(context, null, null, null);

					//Iterate timesheet efforts
					for (Iterator itrEffort = efforts.iterator(); itrEffort.hasNext();) {
						boolean approverContextExists = false;
						Map effortInfo = (Map) itrEffort.next();

						//Get project associated with the effort
						String strTaskId = (String)effortInfo.get(SELECT_TASK_ID);
						//Skip the effort if it doesn't have any task attached.
						if(UIUtil.isNullOrEmpty(strTaskId)){
							continue;
						}
						Task task = new Task(strTaskId);
						Map projectInfo = task.getProject(context, objectSelects);
						String projectId = (String) projectInfo.get(ProgramCentralConstants.SELECT_ID);

						//Check if project is already connected as Approver Context.
						for (Iterator itrProjects = mlProjects.iterator(); itrProjects.hasNext();) {
							Map approverCtxInfo = (Map) itrProjects.next();
							String approverCtxId = (String)approverCtxInfo.get(ProgramCentralConstants.SELECT_ID);
							if(projectId.equals(approverCtxId)){
								approverContextExists = true;
								break;
							}
						}
						if(!approverContextExists){
							DomainObject project = DomainObject.newInstance(context, projectId);
							DomainRelationship.connect(context, wt, ProgramCentralConstants.RELATIONSHIP_APPROVER_CONTEXT, project);
						}
					}	
					loadMigratedOids(timesheetId);
				}
			}
		}catch(Exception e){
			throw new FrameworkException(e);
		}
	}

	private void updateWorkCalendar(Context context, StringList ids) throws Exception{
		try{
			//Get all calendars and iterate.
			DateFormat formatter = new SimpleDateFormat("hh:mm aa");
			StringList slWorkingHours = new StringList();
			StringList slLunchHours = new StringList();

			//Get the default calendar settings
			String sStart = "8";
			String sLunchTime = "12-13";
			String sWorkingHours = "8";			
			try{
				sStart = EnoviaResourceBundle.getProperty(context, "emxFramework.Schedule.StartTime");
				sLunchTime = EnoviaResourceBundle.getProperty(context, "emxFramework.Schedule.LunchTime");
				sWorkingHours = EnoviaResourceBundle.getProperty(context, "emxFramework.Schedule.WorkingHoursPerDay");
			}catch(Exception e){}

			//Get Work Start Time
			int startTime = Integer.parseInt(sStart);

			//Get work hours
			int workHours = Integer.parseInt(sWorkingHours);
			int workTimePerDay = workHours * 60;

			//Get lunch hours
			String[] aLunchTime = sLunchTime.split("-");
			String sLunchStartTime = aLunchTime[0];
			String sLunchFinishTime = aLunchTime[1];
			int lunchStart = Integer.parseInt(sLunchStartTime);
			int lunchFinish = Integer.parseInt(sLunchFinishTime);
			int lunchHours = lunchFinish - lunchStart;

			//work start time
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, startTime);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			if(startTime == 0){
				slWorkingHours.add("0:00 AM");
			}else{
				slWorkingHours.add(formatter.format(calendar.getTime()));
			}
			//work finish time
			int officeHours = workHours + lunchHours;

			calendar.add(Calendar.HOUR, officeHours);
			slWorkingHours.add(formatter.format(calendar.getTime()));

			//lunch start time
			calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, lunchStart);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			slLunchHours.add(formatter.format(calendar.getTime()));
			
			//lunch finish time
			calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, lunchFinish);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			slLunchHours.add(formatter.format(calendar.getTime()));

			for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
				StringList slDeprecatedEvents = new StringList();
				String calendarId = (String) iterator.next();
				WorkCalendar wc = new WorkCalendar(calendarId);

				//Get calendar info.
				StringList slCalendarSelects = new StringList();
				slCalendarSelects.add(ProgramCentralConstants.SELECT_NAME);
				slCalendarSelects.add(ProgramCentralConstants.SELECT_OWNER);
				Map calInfo = (Map) wc.getInfo(context, slCalendarSelects);
				String calName = (String) calInfo.get(ProgramCentralConstants.SELECT_NAME);
				String calOwner = (String) calInfo.get(ProgramCentralConstants.SELECT_OWNER);

				StringList slWeek = wc.getShortDaysOfWeek(context, Locale.ENGLISH.getLanguage());
				StringList slWorkWeek = new StringList(slWeek);
				//Get all calendars events and iterate.
				MapList events = wc.getEvents(context);

				//Skip default calendar created by user agent.
				String userAgent = PropertyUtil.getSchemaProperty(context, "person_UserAgent");
				if("DefaultCalendar".equals(calName) && userAgent.equals(calOwner)) {
					continue;	
				}

				for (Iterator itrEvents = events.iterator(); itrEvents.hasNext();) {
					Map eventInfo = (Map) itrEvents.next();
					String sFrequency = (String) eventInfo.get(WorkCalendar.SELECT_FREQUENCY);
					String sEventId = (String) eventInfo.get(SELECT_ID);
					if("1".equalsIgnoreCase(sFrequency)){
						//Create workweek
						String sWeekOff = (String) eventInfo.get(WorkCalendar.SELECT_DAY_NUMBER);
						int weekOff = Integer.parseInt(sWeekOff);
						String weekend = (String)slWeek.get(--weekOff);
						slWorkWeek.remove(weekend);
						slDeprecatedEvents.add(sEventId);
					}
					else if("0".equalsIgnoreCase(sFrequency)){
						DomainRelationship event = DomainRelationship.newInstance(context, sEventId);
						Map attributes = new HashMap();
						attributes.put(ProgramCentralConstants.ATTRIBUTE_EVENT_TYPE, "Exception");
						attributes.put(ProgramCentralConstants.ATTRIBUTE_FREQUENCY, "Daily");
						attributes.put("Calendar Exception Type", "Non Working");
						event.setAttributeValues(context, attributes);
					}
					else if("4".equalsIgnoreCase(sFrequency)){
						DomainRelationship event = DomainRelationship.newInstance(context, sEventId);
						Map attributes = new HashMap();
						attributes.put(ProgramCentralConstants.ATTRIBUTE_EVENT_TYPE, "Exception");
						attributes.put(ProgramCentralConstants.ATTRIBUTE_FREQUENCY, "Daily");
						attributes.put("Calendar Exception Type", "Working");
						event.setAttributeValues(context, attributes);
					}
				}

				//create work week
				wc.createWorkWeek(context, slWorkWeek, slWorkingHours, slLunchHours);

				//set work time per day 
				wc.setAttributeValue(context, "Working Time Per Day", workTimePerDay + EMPTY_STRING);

				//remove previous work week events. 
				String[] aDeprecatedEvents = new String[slDeprecatedEvents.size()];
				slDeprecatedEvents.toArray(aDeprecatedEvents);
				DomainRelationship.disconnect(context, aDeprecatedEvents);

				//Log migrated calendars
				loadMigratedOids(calendarId);
			}
		}catch(Exception e){
			throw new FrameworkException(e);
		}
	}
	
	/**
	 * This method will change the type of object from "Financial Item" to "Budget"
	 */
	private  void migrateFinancialItemObjects(Context context, StringList objectList) throws Exception
	{

		try{

			ContextUtil.pushContext(context);
			String cmd = "trigger off";
			MqlUtil.mqlCommand(context, mqlCommand,  cmd);
			
			String[] oidsArray = new String[objectList.size()];
			oidsArray = (String[])objectList.toArray(oidsArray);
			
			StringList busSelects = new StringList();
			
			busSelects.add(SELECT_TYPE);
			busSelects.add(SELECT_ID);
			
			MapList objectInfoList = DomainObject.getInfo(context, oidsArray, busSelects);
			
			Iterator objectInfoListIterator = objectInfoList.iterator();
			mqlLogRequiredInformationWriter("===================MIGRATION OF FINANCIAL ITEM STARTED=====================================\n\n");
			while(objectInfoListIterator.hasNext())
			{
				Map objectInfo = (Map)objectInfoListIterator.next();
				String objectType = (String)objectInfo.get(SELECT_TYPE);
				String objectId = (String)objectInfo.get(SELECT_ID);
				
				if(TYPE_FINANCIAL_ITEM.equals(objectType)){					
					mqlLogRequiredInformationWriter("Changing the type of object " + objectId+" from "+objectType+" to "+ProgramCentralConstants.TYPE_BUDGET+"\n\n");
					String mqlCommand = "modify bus $1 type $2";
					MqlUtil.mqlCommand(context, mqlCommand, objectId, ProgramCentralConstants.TYPE_BUDGET);
					// Add object to list of converted OIDs
					loadMigratedOids(objectId);
				} else {
					mqlLogRequiredInformationWriter("Skipping object <<" + objectId + ">>, NO MIGRATION NEEDED \n\n");

					// Add object to list of unconverted OIDs
					String comment = "Skipping object <<" + objectId + ">> NO MIGRATIION NEEDED";
					writeUnconvertedOID(comment, objectId);
				}
			}
			mqlLogRequiredInformationWriter("===================MIGRATION OF FINANCIAL ITEM COMPLETED=====================================\n\n");
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			String cmd = "trigger on";
			MqlUtil.mqlCommand(context, mqlCommand,  cmd);
			ContextUtil.popContext(context);
		}
	}

	/**
	 * This method will change the "Default User Access" attribute value from "None" to "Read"
	 */
	private void migrateDefaultUserAccess(Context context, StringList objectList) throws Exception
	{

		try{

			ContextUtil.pushContext(context);
			String cmd = "trigger off";
			MqlUtil.mqlCommand(context, mqlCommand,  cmd);
			
			String[] oidsArray = new String[objectList.size()];
			oidsArray = (String[])objectList.toArray(oidsArray);
			
			StringList busSelects = new StringList();
			
			busSelects.add(SELECT_ID);
			busSelects.add(SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
			
			MapList objectInfoList = DomainObject.getInfo(context, oidsArray, busSelects);
			
			Iterator objectInfoListIterator = objectInfoList.iterator();
			mqlLogRequiredInformationWriter("===================MIGRATION FOR CHANGING DEFAULT USER ACCESS STARTED=====================================\n\n");
			while(objectInfoListIterator.hasNext())
			{
				Map objectInfo = (Map)objectInfoListIterator.next();
				String objectId = (String)objectInfo.get(SELECT_ID);
				String defaultUserAccess = (String)objectInfo.get(SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS); 

				if("None".equals(defaultUserAccess)){
					mqlLogRequiredInformationWriter("Modify value of Attribute Default User Access for folder " + objectId +"\n\n");
					defaultUserAccess = "Read";
					DomainObject folderObj = DomainObject.newInstance(context, objectId);       		
					folderObj.setAttributeValue(context, ATTRIBUTE_DEFAULT_USER_ACCESS, defaultUserAccess);
					// Add object to list of converted OIDs
					loadMigratedOids(objectId);
				}else{
					mqlLogRequiredInformationWriter("Skipping object <<" + objectId + ">>, NO MIGRATION NEEDED \n\n");

					// Add object to list of unconverted OIDs
					String comment = "Skipping object <<" + objectId + ">> NO MIGRATIION NEEDED";
					writeUnconvertedOID(comment, objectId);
				}
			}
			mqlLogRequiredInformationWriter("===================MIGRATION FOR CHANGING DEFAULT USER ACCESS COMPLETED=====================================\n\n");
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			String cmd = "trigger on";
			MqlUtil.mqlCommand(context, mqlCommand,  cmd);
			ContextUtil.popContext(context);
		}
	}
}
