package com.dassault_systemes.enovia.webapps.common.rest.channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.dassault_systemes.enovia.bps.widget.UIFieldValue;
import com.dassault_systemes.enovia.bps.widget.jaxb.Status;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

public class TaskChannel_mxJPO {

    private static final String typeWorkflowTask = PropertyUtil.getSchemaProperty("type_WorkflowTask");
    private static final String policyWorkflowTask = PropertyUtil.getSchemaProperty("policy_WorkflowTask");
    private static final String SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE = DomainObject.getAttributeSelect(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
    private static final String SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE = DomainObject.getAttributeSelect(DomainObject.ATTRIBUTE_ACTUAL_COMPLETION_DATE);
    private static final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = DomainObject.getAttributeSelect(DomainObject.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
    private static final String SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE = DomainObject.getAttributeSelect(DomainObject.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);
    private static final String strAttrworkFlowDueDate = DomainObject.getAttributeSelect(PropertyUtil.getSchemaProperty("attribute_DueDate"));
    private static final String strAttrworkFlowCompletionDate = DomainObject.getAttributeSelect(DomainObject.ATTRIBUTE_ACTUAL_COMPLETION_DATE);
    private static final String SELECT_ATTRIBUTE_ASSIGNEE_DUEDATE = DomainObject.getAttributeSelect(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
    private static final String SELECT_ATTRIBUTE_DUEDATE_OFFSET = DomainObject.getAttributeSelect(DomainObject.ATTRIBUTE_DUEDATE_OFFSET);
    
    private static final long ONE_DAY = 1000 * 60 * 60 * 24;
    
    public TaskChannel_mxJPO() {
    }

	public MapList getDueDateStatus(Context context, String[] args) throws Exception {

        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) JPO.unpackArgs(args);
        MapList ml = (MapList) map.get("JPO_WIDGET_DATA");
        String fieldKey = (String) map.get("JPO_WIDGET_FIELD_KEY");
		Locale strLocale = new Locale(context.getSession().getLanguage());
        
    	// i18n return strings
        String statusDue = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",strLocale,     "emxTeamCentral.Channel.Tasks.Status.Due");
        String statusOnTime = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",strLocale, "emxTeamCentral.Channel.Tasks.Status.OnTime");
        String statusLate = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",strLocale, "emxTeamCentral.Channel.Tasks.Status.Late");
        long dueWithin = ONE_DAY;
        try {
        	dueWithin *= Integer.parseInt(EnoviaResourceBundle.getProperty(context, "emxTeamCentral.InBoxTask.YellowDelay"));
        }
        catch (Exception e) {
        	// do nothing - use default of one day
        }
        
        
        // extract the object ids
        List<String> objectIds = new ArrayList<String>();
		Iterator<Map<String, String>> itr = ml.iterator();
    	while (itr.hasNext()) {
    		objectIds.add(itr.next().get(DomainObject.SELECT_ID));
    	}

        String stateComplete = FrameworkUtil.lookupStateName(context, DomainObject.POLICY_INBOX_TASK, "state_Complete");
        String stateCompleted = FrameworkUtil.lookupStateName(context, policyWorkflowTask, "state_Completed");
        String stateAssigned = FrameworkUtil.lookupStateName(context, DomainObject.POLICY_INBOX_TASK, "state_Assigned");

        Date today = new Date();

        StringList selects = new StringList();
        selects.add(DomainObject.SELECT_TYPE);
        selects.add(DomainObject.SELECT_CURRENT);
        selects.add(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
        selects.add(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
        selects.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
        selects.add(SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);
        selects.add(strAttrworkFlowDueDate);
        selects.add(strAttrworkFlowCompletionDate);
        selects.add(SELECT_ATTRIBUTE_ASSIGNEE_DUEDATE);
        selects.add(SELECT_ATTRIBUTE_DUEDATE_OFFSET);
        
        // Retrieve info for the list of objects
        itr = DomainObject.getInfo(context, objectIds.toArray(new String[objectIds.size()]), selects).iterator();
        for (int i = 0; itr.hasNext(); i++)
        {
            Map objectMap = (Map) itr.next();
            String taskState = (String) objectMap.get(DomainObject.SELECT_CURRENT);

            Date dueDate = null;
            String status = statusOnTime;
            String taskDueDate = "";
            String taskCompletedDate = "";
            String sTypeName = (String) objectMap.get(DomainObject.SELECT_TYPE);

            if (taskState == null) {
            	taskState = "";
            }

            if ((DomainObject.TYPE_INBOX_TASK).equalsIgnoreCase(sTypeName))
            {
                taskDueDate = (String) objectMap.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                taskCompletedDate = (String) objectMap.get(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
            }
            else if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTypeName))
            {
                taskDueDate = (String) objectMap.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
                taskCompletedDate = (String) objectMap.get(SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);
            }
            else if (typeWorkflowTask.equalsIgnoreCase(sTypeName))
            {
                taskDueDate = (String)objectMap.get(strAttrworkFlowDueDate);
                taskCompletedDate = (String)objectMap.get(strAttrworkFlowCompletionDate);
            }

			if (!"".equals(taskState)) {
				if (taskDueDate == null || "".equals(taskDueDate)) {
					dueDate = null;
				} else {
					dueDate = eMatrixDateFormat.getJavaDate(taskDueDate);
				}

				// if not completed
				if (!taskState.equals(stateComplete) && !taskState.equals(stateCompleted)) {
					if (dueDate != null && today.after(dueDate)) {
						status = statusLate;
					} else if (dueDate != null && ((dueDate.getTime() - today.getTime()) < dueWithin)) {
						status = statusDue;
					}
				} else {
					Date actualCompletionDate = (taskCompletedDate == null || "".equals(taskCompletedDate)) ? new Date() : eMatrixDateFormat.getJavaDate(taskCompletedDate);

					// if completed late
					if (dueDate != null && actualCompletionDate.after(dueDate)) {
						status = statusLate;
					}
				}
			}
		
        	if (status != null) {
        		Map<String, Object> destMap = (Map<String, Object>) ml.get(i);
        		UIFieldValue dataValue = new UIFieldValue();
        		dataValue.setValue(status);
        		dataValue.setBadgeTitle(status);

        		if (status.equals(statusLate)) {
        			dataValue.setBadgeStatus(Status.ERROR);
        		}
        		else if (status.equals(statusDue)) {
        			dataValue.setBadgeStatus(Status.WARNING);
        		}
//        		else {
//        			dataValue.setBadgeStatus(Status.OKAY);
//        		}

        		destMap.put(fieldKey, dataValue);
        	}
        }

		return (ml);
	}

	public MapList getDueDate(Context context, String[] args) throws Exception {

        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) JPO.unpackArgs(args);
        MapList ml = (MapList) map.get("JPO_WIDGET_DATA");
        String fieldKey = (String) map.get("JPO_WIDGET_FIELD_KEY");


        // set to one year from today so empty dates sort as we wish
    	Date date = new Date();
    	date.setYear(date.getYear() + 1);
    	String futureDate = date.toString();
        
        // Retrieve info for the list of objects
		Iterator<Map<String, Object>> itr = ml.iterator();
        for (int i = 0; itr.hasNext(); i++) {

        	Map<String, Object> objectMap = (Map<String, Object>) itr.next();
            String dueDate = (String) objectMap.get(fieldKey);
            
            // adjust empty dates forward a year from today
            if (dueDate == null || dueDate.length() == 0) {
            	objectMap.put(fieldKey, futureDate);
            }
        }
        
        return (ml);
	}
}
