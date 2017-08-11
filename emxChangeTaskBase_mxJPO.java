/**
 * ${CLASSNAME}.java
 * Created on Nov 4, 2008

 * @author QZV
 *
 * The <code>${CLASSNAME}</code> class/interface contains ...
 *
 * @version  - Copyright (c) 2005, MatrixOne, Inc.
 */

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import matrix.db.AttributeType;
import matrix.db.AttributeTypeList;
import matrix.db.BusinessInterface;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.StateItr;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.enterprisechange.ChangeProject;
import com.matrixone.apps.enterprisechange.ChangeTask;
import com.matrixone.apps.enterprisechange.Decision;
import com.matrixone.apps.enterprisechange.EnterpriseChangeConstants;
import com.matrixone.apps.enterprisechange.EnterpriseChangeUtil;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.jsystem.util.StringUtils;

/**
 * Implementation for Change Task
 * @author QZV
 *
 * The &lt;code&gt;${CLASSNAME}&lt;/code&gt; class contains ...
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxChangeTaskBase_mxJPO extends ChangeTask {
	public static final String SUITE_KEY ="EnterpriseChange";
	/**
	 * Constructor
	 */
	public emxChangeTaskBase_mxJPO() {
		super();
	}

	/**
	 * Whats up?
	 * @param id
	 * @throws Exception
	 */
	public emxChangeTaskBase_mxJPO(String id) throws Exception {
		super(id);
	}

	/**
	 * Constructor
	 * @param context
	 * @param args
	 * @throws FrameworkException
	 */
	public emxChangeTaskBase_mxJPO(Context context, String[] args) throws FrameworkException {
		super();
		if (args != null && args.length > 0)
		{
			setId(args[0]);
		}
	}

	/**
	 * Delete Trigger on Issue Rel to check for the Change Task state
	 * @param context
	 * @param args
	 * @return int
	 * @throws Exception
	 * @deprecated since R418
	 */
	public int disconnectCheckOnResolvedToRel(Context context, String[] args) throws Exception {
		String toObjId = args[1];

		DomainObject toObj = new DomainObject(toObjId);
		if(toObj.isKindOf(context, DomainConstants.TYPE_CHANGE_TASK)) {
			String objState = toObj.getInfo(context, "current");
			if(EnterpriseChangeConstants.STATE_CHANGE_TASK_COMPLETE.equals(objState)) {
				String strNotice = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxEnterpriseChange.Pdcm.CanNotDeleteIssueOrDeliverable", context.getSession().getLanguage());
			//	MqlUtil.mqlCommand(context, "notice '" + strNotice + "'");
				MqlUtil.mqlCommand(context, "notice $1", strNotice);
				return 1;
			}
		} else {
			return 0;
		}
		return 0;
	}

	/**
	 * Delete Trigger on Task Deliverable Rel to check for the Change Task state
	 * @param context
	 * @param args
	 * @return int
	 * @throws Exception
	 */
	public int disconnectCheckOnTaskDeliverableRel(Context context, String[] args) throws Exception {
		return disconnectCheckOnIssueAndTaskDeliverableRels(context, args);
	}

	/**
	 * Delete Trigger on Issue Rel to check for the Change Task state
	 * @param context
	 * @param args
	 * @return int
	 * @throws Exception
	 */
	public int disconnectCheckOnIssueAndTaskDeliverableRels(Context context, String[] args) throws Exception {
		String fromObjId = args[0];
		String toObjId = args[1];

		DomainObject fromObj = new DomainObject(fromObjId);
		DomainObject toObj = new DomainObject(toObjId);
		if(fromObj.isKindOf(context, DomainConstants.TYPE_CHANGE_TASK) && (toObj.isKindOf(context, PropertyUtil.getSchemaProperty(context, "type_Change")))) {
			String objState = fromObj.getInfo(context, "current");
			if(EnterpriseChangeConstants.STATE_CHANGE_TASK_COMPLETE.equals(objState)) {
				String strNotice = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxEnterpriseChange.Pdcm.CanNotDeleteIssueOrDeliverable",context.getSession().getLanguage());

				//MqlUtil.mqlCommand(context, "notice '" + strNotice + "'");
				MqlUtil.mqlCommand(context, "notice $1", strNotice);
				return 1;
			}
		} else {
			return 0;
		}
		return 0;
	}

	/**
	 * Sets the state state based on the change task deliverable state
	 * @param context
	 * @param args
	 * @throws Exception
	 * @deprecated : since R418 Use setChangeTaskStateOnChangeProcessPromoteDemote(matrix.db.Context,java.lang.String[])
	 */
	public void setRelatedObjectState(Context context, String[] args) throws Exception {

		String objectId = args[0];
		StringList relatedObjectIds = getRelatedObjects(context, args);
		if(relatedObjectIds.isEmpty()) {
			return;
		}

		HashMap paramMap = new HashMap();
		paramMap.put("objectId", objectId);
		paramMap.put("isBGProcess", "false");
		paramMap.put("relatedObjectIds", relatedObjectIds);

		promoteRelatedObjects(context, args);

	}

	/**
	 * Trigger on EC, ECR, ECO promote and demote
	 * which triggers the change task to promote and demote the states
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void setChangeTaskStateOnChangeProcessPromoteDemote(Context context, String[] args) throws Exception {

		String objectId = args[0];
		StringList relatedObjectIds = getRelatedObjects(context, args);
		//Added for IR-046491V6R2011
		String changeDelChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.MultipleChangeDeliverable");
		//End IR-046491V6R2011
		String autoPromoteDemoteChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.AutoPromoteDemote");
		//Modified for IR-046491V6R2011
		if(relatedObjectIds.isEmpty()||changeDelChoice.equalsIgnoreCase("true")||autoPromoteDemoteChoice.equalsIgnoreCase("false")){
			return;
		}

		String strNotice = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxEnterpriseChange.Pdcm.BGNotice",context.getSession().getLanguage());

		//MqlUtil.mqlCommand(context, "notice '" + strNotice + "'");
		MqlUtil.mqlCommand(context, "notice $1", strNotice);

		//submitting to the BG Process
		BackgroundProcess bgpObj = new BackgroundProcess();
		HashMap paramMap = new HashMap();
		paramMap.put("objectId", objectId);
		paramMap.put("changeObjStateName", args[3]);
		paramMap.put("isBGProcess", "true");
		paramMap.put("relatedObjectIds", relatedObjectIds);

		Object objectArray[] = {context,paramMap};
		Class objectTypeArray[] = {context.getClass(),paramMap.getClass()};
		ChangeTask cTask = new ChangeTask();
		bgpObj.submitJob(context.getFrameContext(context.getSession().toString()),
				cTask, "promoteRelatedObjects" ,objectArray,objectTypeArray);

	}

	/**
	 * Promote the objects passed in the args
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void promoteRelatedObjects(Context context, String[] args) throws Exception {

		//Added for IR-046491V6R2011
		String changeDelChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.MultipleChangeDeliverable");
		String autoPromoteDemoteChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.AutoPromoteDemote");
		if(changeDelChoice.equalsIgnoreCase("true")||autoPromoteDemoteChoice.equalsIgnoreCase("false")){
			return;
		}
		//End IR-046491V6R2011
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String objectId = paramMap.get("objectId").toString();
		StringList relatedObjectIds = (StringList)paramMap.get("relatedObjectIds");
		String changeObjSourceStateName = (String)paramMap.get("changeObjStateName");
		boolean isBGProcess = "true".equals(paramMap.get("isBGProcess").toString());

		DomainObject doObj = new DomainObject(objectId);

		String changeObjState = doObj.getInfo(context, DomainConstants.SELECT_CURRENT);
		changeObjSourceStateName = changeObjSourceStateName != null ? changeObjSourceStateName : changeObjState;
		String changeObjType = doObj.getInfo(context, DomainConstants.SELECT_TYPE);
		String policyName = doObj.getInfo(context, DomainConstants.SELECT_POLICY);
		changeObjState = FrameworkUtil.reverseLookupStateName(context, policyName, changeObjState);
		String changeObjTypeSym =  FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_TYPE, changeObjType, true);

		boolean demote = isTaskStatePriorToCurrent(context, objectId, changeObjSourceStateName);

		Task relatedObj = new Task();

		String propStateKey = "eServiceSuiteEnterpriseChange.StateMapping."
			+ changeObjTypeSym + "."
			+ StringUtils.replaceAll(changeObjState, " ", "");

		String propSetStartDateStateKey = "eServiceSuiteEnterpriseChange.StateMapping."
			+ changeObjTypeSym
			+ ".SetStartDateUponState";

		String propSetEndDateStateKey = "eServiceSuiteEnterpriseChange.StateMapping."
			+ changeObjTypeSym
			+ ".SetEndDateUponState";

		String propMarkTaskForDelete = "eServiceSuiteEnterpriseChange.StateMapping."
			+ changeObjTypeSym
			+ ".MarkTaskForDelete";

		String toState = getPropertyVal(context, propStateKey, changeObjType, changeObjTypeSym);//FrameworkProperties.getProperty(propStateKey);
		boolean setStartDate = changeObjState.equals(getPropertyVal(context, propSetStartDateStateKey, changeObjType, changeObjTypeSym));//changeObjState.equals(FrameworkProperties.getProperty(propSetStartDateStateKey));
		boolean markTaskDelete = changeObjState.equals(getPropertyVal(context, propMarkTaskForDelete, changeObjType, changeObjTypeSym));//changeObjState.equals(FrameworkProperties.getProperty(propMarkTaskForDelete));
		boolean setEndDate = changeObjState.equals(getPropertyVal(context, propSetEndDateStateKey, changeObjType, changeObjTypeSym));//FrameworkProperties.getProperty(propSetEndDateStateKey)

		SimpleDateFormat sdformat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
		String strTaskDate = sdformat.format(new Date());

		String taskId = "";
		String parentTaskId = "";
		String relatedObjPolicy = "";

		Iterator listItr = relatedObjectIds.iterator();
		while(listItr.hasNext()) {
			try {

				taskId = listItr.next().toString();
				relatedObj.setId(taskId);

				relatedObjPolicy = relatedObj.getInfo(context, DomainConstants.SELECT_POLICY);
				toState = FrameworkUtil.lookupStateName(context, relatedObjPolicy, toState);

				if(markTaskDelete) {
					parentTaskId = relatedObj.getInfo(context, "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].from.id");
					Task parentObj = new Task(parentTaskId);
					if(!DomainObject.TYPE_ENGINEERING_CHANGE.equals(changeObjType) ||
							(!EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_SUBMIT.equals(changeObjSourceStateName)
									&& !EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_COMPLETE.equals(changeObjSourceStateName))) {
						parentObj.markForDelete(context, taskId, "Cancelled");
						setEndDate = true;
					}
				} else {
					if(isBGProcess) {
						if(isTaskStatePriorToCurrent(context, taskId, toState)) {
							if(validateCheckTriggers(context, new StringList(taskId))) {
								relatedObj.setState(context, toState);
							} else {
								return;
							}
						} else {
							if(demote)
								demoteTaskState(context, taskId, toState);
						}
					} else {
						relatedObj.setState(context, toState);
					}
				}

				if(setStartDate) {
					String taskActStartDate = relatedObj.getInfo(context,
							"attribute["+DomainConstants.ATTRIBUTE_TASK_ACTUAL_START_DATE+"].value").toString();
					if(taskActStartDate == null || "".equals(taskActStartDate)) {
						relatedObj.setAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ACTUAL_START_DATE, strTaskDate);
					}
				}

				if(setEndDate) {
					relatedObj.setAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE, strTaskDate);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check for the property entries
	 * @param context
	 * @param propKey
	 * @param changeType
	 * @param changeObjTypeSym
	 * @return String
	 * @throws Exception
	 */
	private String getPropertyVal(Context context, String propKey, String changeType, String changeObjTypeSym) throws Exception {
		String returnString = "";
		try {
			returnString = EnoviaResourceBundle.getProperty(context,propKey);
		} catch(FrameworkException e) {
			//String superType = MqlUtil.mqlCommand(context, "print type '"+changeType+"' select derived dump");
			String superType = MqlUtil.mqlCommand(context, "print type $1 select $2 dump",changeType,"derived");
			String superTypeSym = FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_TYPE, superType, true);
			return getPropertyVal(context, propKey.replaceAll(changeObjTypeSym, superTypeSym), superType, superTypeSym);
		}
		return returnString;
	}

	/**
	 * Method to get the related objects
	 * @param context
	 * @param args
	 */
	private StringList getRelatedObjects(Context context, String[] args) throws Exception {
		if(args.length <= 1) {
			return new StringList();
		}

		String objectId = args[0];
		String relPattern = "";
		String typePattern = "";

		StringTokenizer relTok = new StringTokenizer(args[1]);
		while(relTok.hasMoreTokens()) {
			relPattern += PropertyUtil.getSchemaProperty(context,relTok.nextToken()) + ",";
		}
		StringTokenizer typeTok = new StringTokenizer(args[2]);
		while(typeTok.hasMoreTokens()) {
			typePattern += PropertyUtil.getSchemaProperty(context,typeTok.nextToken()) + ",";
		}

		StringList objSels = new StringList(DomainConstants.SELECT_ID);

		DomainObject deliverableObj = new DomainObject(objectId);
		MapList mapList = deliverableObj.getRelatedObjects(context, relPattern,
				typePattern, objSels, null, true, true, (short)1, null, null);

		StringList objectIdsList = new StringList();
		Iterator iterator = mapList.iterator();
		while(iterator.hasNext()) {
			Map map = (Map)iterator.next();
			objectIdsList.addElement(map.get(DomainConstants.SELECT_ID).toString());
		}
		return objectIdsList;
	}

	/**
	 * check for the cancellation of the Change process object
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean canCancelChange(Context context, String[] args) throws Exception {

		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String changeObjId =  paramMap.get("objectId").toString();

		DomainObject changeObj = new DomainObject(changeObjId);
		StringList taskIdList = changeObj.getInfoList(context, "to["+DomainObject.RELATIONSHIP_TASK_DELIVERABLE+"].from.id");
		if(taskIdList.isEmpty()) {
			return new Boolean(true);
		}

		DomainObject taskObj = new DomainObject();

		Iterator itr = taskIdList.iterator();
		while(itr.hasNext()) {
			String taskId = itr.next().toString();

			taskObj.setId(taskId);

			if(!taskObj.isKindOf(context, DomainConstants.TYPE_CHANGE_TASK)) {
				continue;
			}

			StringList objectSels = new StringList("from[Task Deliverable].to.current");
			objectSels.addElement("from[Task Deliverable].to.type");

			StringList typeList;
			StringList currentList;

			StringList changeProcessList = FrameworkUtil.split(getTypePatternString(context), ",");

			String POLICY_ENGINEERING_CHANGE = PropertyUtil.getSchemaProperty(context,"policy_EngineeringChangeStandard");
			String STATE_EC_REJECT = PropertyUtil.getSchemaProperty(context, "policy", POLICY_ENGINEERING_CHANGE, "state_Reject");

			MapList mapList = taskObj.getRelatedObjects(context, "Subtask", "*", objectSels, null, false, true, (short)0, null, null);
			Iterator mapListItr = mapList.iterator();
			while(mapListItr.hasNext()) {
				Map map = (Map)mapListItr.next();
				typeList = ChangeProject.toStringList(map.get("from[Task Deliverable].to.type"));
				currentList = ChangeProject.toStringList(map.get("from[Task Deliverable].to.current"));
				for(int i=0; i < typeList.size(); i++) {
					if(changeProcessList.contains(typeList.get(i).toString())) {
						String subChangeState = currentList.get(i).toString();
						if(!EnterpriseChangeConstants.STATE_ECO_STANDARD_CANCELLED.equals(subChangeState)
								&& !STATE_EC_REJECT.equals(subChangeState)) {
							return new Boolean(false);
						}
					}
				}

			}
		}

		return new Boolean(true);
	}

	/**
	 * AccessFunction
	 * the deprecation of this function has been removed  as the access function on
	 * ECHPDCMAddExsistingDeliverable has been added again for IR-046491V6R2011
	 * this function is depricated because we removed access function on ECHPDCMAddExsistingDeliverable
	 * for IR-041102-EChaddMultipleEC
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean showCommandForChangeTask(Context context, String[] args) throws Exception {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");
			//Added for IR-046491V6R2011
			String changeDelChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.MultipleChangeDeliverable");
			//End IR-046491V6R2011
			DomainObject doObj = new DomainObject(strObjectId);

			if(!doObj.isKindOf(context, EnterpriseChangeConstants.TYPE_CHANGE_TASK) ||
					EnterpriseChangeConstants.STATE_CHANGE_TASK_COMPLETE.equals(doObj.getInfo(context, "current"))) {
				return false;
			}

			String typePatternStr = getTypePatternString(context);

			MapList mapList = doObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_TASK_DELIVERABLE, typePatternStr,
					null, null, false, true, (short)1, null, null);
			//modified for IR-046491V6R2011
			if(!mapList.isEmpty()&& changeDelChoice.equalsIgnoreCase("false")) {
				return false;
			}
		} catch (Exception ex) {
			throw ex;
		}
		return true;
	}

	/**
	 * Method to return the change types that are allowed
	 * @param context
	 * @return String of comma separated types
	 * @throws Exception
	 */
	public String getTypePatternString(Context context) throws Exception {
		//Getting the allowed types from the inter-op command
		String typePattern = "";
		HashMap cmdMap = UICache.getCommand(context, EnterpriseChangeConstants.COMMAND_INTEROP_DELIVERABLE_SEARCH);
		if (cmdMap != null && !"null".equals(cmdMap) && cmdMap.size() >= 0) {
			HashMap settingsList = (HashMap) cmdMap.get("settings");
			if (settingsList != null && !"null".equals(settingsList) && settingsList.size() > 0) {
				for (Iterator itr = settingsList.keySet().iterator(); itr.hasNext();) {
					String key = (String)itr.next();
					if (key.startsWith("Type")) {
						if (typePattern.length() > 0)
							typePattern += ",";

						typePattern += (String)settingsList.get(key);
					}
				}
			}
		}
		//If this settings are empty, get the defaults from the props
		if(typePattern.length() <= 0) {
			typePattern = EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Pdcm.DeliverableTypes");
		}

		String typePatternStr = "";
		StringList typePatternList = FrameworkUtil.split(typePattern, ",");
		Iterator itr = typePatternList.iterator();
		while(itr.hasNext()) {
			typePatternStr += PropertyUtil.getSchemaProperty(context,itr.next().toString()) + ",";
		}

		return typePatternStr;
	}

	/**
	 * AccessFunction
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean hideCommandForChangeTask(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");

		DomainObject doObj = new DomainObject(strObjectId);

		if(doObj.isKindOf(context, TYPE_CHANGE_TASK)) {
			return false;
		}

		return true;
	}

	/**
	 * AccessFunction
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean hideCommandForRelasedTask(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");

		DomainObject doObj = new DomainObject(strObjectId);
		if(doObj.isKindOf(context, EnterpriseChangeConstants.TYPE_CHANGE_TASK)) {
			String objState = doObj.getInfo(context, "current");
			if(EnterpriseChangeConstants.STATE_CHANGE_TASK_COMPLETE.equals(objState)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Method to show Resolved Item command
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean showCommandResolvedItemCommand(Context context, String[] args) throws Exception {
		return !hideCommandForChangeTask(context, args);
	}

	/**
	 * Method to show Add Deliverable command
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 *
	 */
	public boolean showAddDeliverableCommand(Context context, String[] args) throws Exception {
		return !hideCommandForChangeTask(context, args);
	}

	/**
	 * AccessFunction
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean hideLifeCycleCommandForChangeTask(Context context, String[] args) throws Exception {
		return !showLifeCycleCommand(context, args);
	}

	/**
	 * AccessFunction
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	public boolean showLifeCycleCommand(Context context, String[] args) throws Exception {

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");

			Task task = new Task(strObjectId);
			//check for type of task
			if(!task.isKindOf(context, EnterpriseChangeConstants.TYPE_CHANGE_TASK)) {
				return false;
			}

			Map map = task.getProject(context, new StringList(SELECT_ID));
			String projectId = map.get(SELECT_ID).toString();

			//check for Project Lead role
			if(hasProjectLeadRole(context, projectId)) {
				return false;
			}
		} catch (Exception ex) {
			throw ex;
		}
		return true;
	}

	/**
	 * @param context
	 * @param projectId
	 * @return boolean
	 * @throws Exception
	 */
	public boolean hasProjectLeadRole(Context context, String projectId) throws Exception {
		return !getProjectLeadRole(context, projectId).isEmpty();
	}

	/**
	 * @param context
	 * @param projectId
	 * @return boolean
	 * @throws Exception
	 */
	public MapList getProjectLeadRole(Context context, String projectId) throws Exception {
		StringList objectSels = new StringList();
		objectSels.addElement(SELECT_NAME);
		setId(projectId);
		MapList mapList = getRelatedObjects(context, RELATIONSHIP_MEMBER, TYPE_PERSON,
				objectSels, null, false, true, (short)1, SELECT_NAME + " == '"+context.getUser()+"'",
				"attribute[" + ATTRIBUTE_PROJECT_ROLE + "].value == '" + ProjectSpace.RANGE_PROJECT_LEAD + "'");

		return mapList;
	}

	/**
	 * Validates the checktriggers of the related objects before promotion
	 * @param context
	 * @param listOfTriggers
	 * @return boolean
	 * @throws Exception
	 */
	private boolean validateCheckTriggers(Context context, StringList relatedObjectIds) throws Exception {
		Iterator listItr = relatedObjectIds.iterator();
		String taskId = "";
		HashMap paramMap = new HashMap();

		while(listItr.hasNext()) {
			taskId = listItr.next().toString();
			paramMap.put("objectId", taskId);
			paramMap.put("RequestValuesMap", new HashMap());
			MapList checkTriggerList = (MapList)JPO.invoke(context, "emxTriggerValidation", null, "getCheckTriggers", JPO.packArgs(paramMap), MapList.class);
			Iterator mplItr = checkTriggerList.iterator();
			while(mplItr.hasNext()) {
				Map map = (Map)mplItr.next();
				String selectedTriggerId = (String)map.get("id");

				String result = (String)JPO.invoke(context, "emxTriggerValidationResults", null, "executeTriggers", JPO.packArgs(selectedTriggerId), String.class);
				if(result.startsWith("Fail")) {
					//Failure Mail
					String sSubject = "emxEnterpriseChange.RelatedObjectPromote.FailreMailSubjet";
					String sMessage = "emxEnterpriseChange.RelatedObjectPromote.FailreMailContent";
					String sResourceBundle = "emxEnterpriseChangeStringResource";

					StringList toList = new StringList(context.getUser());

					StringList ccList = new StringList();
					StringList bccList = new StringList();
					StringList objectIdList = new StringList(taskId);

					setId(taskId);
					toList.add(getInfo(context, SELECT_OWNER));

					// notify all Component Engineers
					emxMailUtil_mxJPO.sendNotification(context,
							toList, ccList, bccList,
							sSubject,null, null,
							sMessage, null, null,
							objectIdList, null,
							sResourceBundle) ;

					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check and only promote while setting the state
	 * @param context
	 * @param objectId
	 * @param stateName
	 * @return int
	 * @throws FrameworkException
	 */
	private int demoteTaskState(Context context, String objectId, String stateName) throws FrameworkException {
		setId(objectId);
		boolean flag = openObject(context);
		int i1;
		try {
			int i = 0;
			int j = -1;
			int k = 0;
			for (StateItr stateitr = new StateItr(getStates(context)); stateitr.next();) {
				if (stateitr.obj().getName().equalsIgnoreCase(stateName))
					j = k;
				if (stateitr.obj().isCurrent())
					i = k;
				k++;
			}

			int l = j-i;
			for(; l < 0; l++)
				demote(context);

			i1 = j - i;
		} catch (Exception exception) {
			throw new FrameworkException(exception);
		}
		closeObject(context, flag);
		return i1;
	}

	/**
	 * Check for the state of task is prior to the current
	 * @param context
	 * @param objectId
	 * @param stateName
	 * @return
	 * @throws FrameworkException
	 */
	private boolean isTaskStatePriorToCurrent(Context context, String objectId, String stateName) throws FrameworkException {
		setId(objectId);
		boolean flag = openObject(context);
		try {
			int i = 0;
			int j = -1;
			int k = 0;
			for (StateItr stateitr = new StateItr(getStates(context)); stateitr.next();) {
				if (stateitr.obj().getName().equalsIgnoreCase(stateName))
					j = k;
				if (stateitr.obj().isCurrent())
					i = k;
				k++;
			}

			return (j-i) > 0;
		} catch (Exception exception) {
			throw new FrameworkException(exception);
		} finally {
			closeObject(context, flag);
		}
	}

	/**
	 * gets the type and subtypes of the changetask as hidden variables
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 */
	public String getHiddenFields(Context context, String[] args) throws Exception {

		StringBuffer outPut = new StringBuffer();
		try {
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");
			String strObjectId = (String)paramMap.get("objectId");

			setId(strObjectId);
			String isChangeTask = isKindOf(context, TYPE_CHANGE_TASK) ? "true" : "false";
			outPut.append("<input type=\"hidden\" id=\"ECHChangeTask\" name=\"ECHChangeTask\" value=\""+ isChangeTask +"\"></input>");

		} catch(Exception ex) {
			throw  new FrameworkException((String)ex.getMessage());
		}
		return outPut.toString();
	}

	/**
	 * Gets the range vals for the Task Requirement Field
	 * for Change Task value should only be mandatory
	 * @param context
	 * @param args
	 * @return HashMap
	 * @throws Exception
	 */
	public HashMap getTaskRequirementVals(Context context, String[] args) throws Exception {
		try {
			String sLanguage = context.getSession().getLanguage();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String objectId = (String) requestMap.get("objectId");

			DomainObject taskObj = new DomainObject(objectId);

			AttributeType atrTaskReq = new AttributeType(ATTRIBUTE_TASK_REQUIREMENT);
			atrTaskReq.open(context);
			StringList strList = atrTaskReq.getChoices(context);
			atrTaskReq.close(context);

			StringList slTaskReqVals = new StringList();
			StringList slTaskReqValsInt = new StringList();
			HashMap map = new HashMap();

			Iterator valsItr = strList.iterator();
			while(valsItr.hasNext()) {
				String rangeVal = (String) valsItr.next();
				//Task Requirement value of Change task should only be Mandatory
				if(taskObj.isKindOf(context, TYPE_CHANGE_TASK) && !rangeVal.equals("Mandatory"))
					continue;

				slTaskReqVals.addElement(rangeVal);
				slTaskReqValsInt.addElement(i18nNow.getRangeI18NString(ATTRIBUTE_TASK_REQUIREMENT, rangeVal, sLanguage));
			}

			map.put("field_choices", slTaskReqVals);
			map.put("field_display_choices", slTaskReqValsInt);

			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Method to disconnect the deliverables from the change task
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void disconnectDeliverables(Context context, String[] args) throws Exception {
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strChangeTaskObjId = (String)programMap.get("objectId");
			String[] delRelIds = (String[])programMap.get("delRelIds");
			StringList objIdList = (StringList)programMap.get("delObjIds");

			DomainObject changeTask = new DomainObject(strChangeTaskObjId);

			String applicabilityPropagated = changeTask.getAttributeValue(context, EnterpriseChangeConstants.ATTRIBUTE_APPLICABILITY_PROPAGATED);
			if (applicabilityPropagated!=null && applicabilityPropagated.equalsIgnoreCase(EnterpriseChangeConstants.RANGE_YES)) {
				MapList taskDeliverables = changeTask.getRelatedObjects(context,
						RELATIONSHIP_TASK_DELIVERABLE,
						getTypePatternString(context),
						new StringList(SELECT_ID),
						null,
						false,
						true,
						(short)1,
						null,
						null,
						0);

				Iterator<Map<String,String>> taskDeliverablesItr = taskDeliverables.iterator();
				while (taskDeliverablesItr.hasNext()) {
					Map<String,String> taskDeliverable = taskDeliverablesItr.next();
					String taskDeliverableId = taskDeliverable.get(SELECT_ID);
					if (objIdList.contains(taskDeliverableId)) {
						emxApplicabilityDecision_mxJPO applicability = new emxApplicabilityDecision_mxJPO(context, args);
						applicability.dynamicRemoveApplicabilityForChange(context, taskDeliverableId);
					}
				}
			}

			DomainRelationship.disconnect(context, delRelIds);
			//Added for IR-057312V6R2011x
		}catch(Exception ex){
			String changeDelChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.MultipleChangeDeliverable");
			String autoPromoteDemoteChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.AutoPromoteDemote");
			if(changeDelChoice.equalsIgnoreCase("true")||autoPromoteDemoteChoice.equalsIgnoreCase("false")){
				if(ex.toString().indexOf("No todisconnect access to object")!=-1 ||ex.toString().indexOf("No fromdisconnect access to object")!=-1){
					String sErrorMsg=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxEnterpriseChange.Alert.CannotRemoveDeliverable",context.getSession().getLanguage());
					//MqlUtil.mqlCommand(context, "notice " + sErrorMsg);
					MqlUtil.mqlCommand(context, "notice $1", sErrorMsg);
				}
			}
		}
		//End of IR-057312V6R2011x
	}


	public Object checkViewMode(Context context, String[] args)	throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strMode = (String) programMap.get("mode");
			Boolean isViewMode = new Boolean(false);
			// check the mode of the table.
			if( (strMode == null) || (strMode != null && ("null".equals(strMode) || "view".equalsIgnoreCase(strMode) || "".equals(strMode))) ) {
				isViewMode = new Boolean(true);
			}
			return isViewMode;
		} catch (Exception e) {
			throw e;
		}
	}


	public StringList excludeECOAndECInReviewState (Context context, String[] args) throws Exception {
		try {
			StringList objSelect = new StringList(2);
			objSelect.addElement(DomainConstants.SELECT_ID);
			StringBuffer stbTypeSelect = new StringBuffer("");
			stbTypeSelect = stbTypeSelect.append(EnterpriseChangeConstants.TYPE_ECO)
			.append(",")
			.append(EnterpriseChangeConstants.TYPE_ENGINEERING_CHANGE);
			String strWhereExp =DomainConstants.SELECT_CURRENT + " == " + EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_REVIEW;
			MapList ECOAndECListInReviewState = DomainObject.findObjects(context,stbTypeSelect.toString(),DomainConstants.QUERY_WILDCARD,strWhereExp,objSelect);
			String strOId = "";
			StringList tempStrList = new StringList();
			for (int i=0; i < ECOAndECListInReviewState.size(); i++) {
				HashMap tempMap =  (HashMap) ECOAndECListInReviewState.get(i);
				strOId = tempMap.get("id").toString();
				tempStrList.addElement(strOId);
			}
			return tempStrList;
		} catch (Exception e) {
			throw e;
		}
	}

	/*this method is been for the IR-041102v62011, To exclude the oids that are already attached to the Task*/

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedTaskDeliverablesAndECOECInReview(Context context, String[] args) throws Exception
	{

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strChangeTaskObjId = (String)programMap.get("objectId");

		StringList tempStrList = new StringList();
		tempStrList.addAll(new DomainObject(strChangeTaskObjId).getInfoList(context, "from["+EnterpriseChangeConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.id"));
		//Added for IR-047115V6R2011x
		StringList excECOECList =excludeECOAndECInReviewState(context,args);
		tempStrList.addAll(excECOECList);
		//End of IR-047115V6R2011x
		return tempStrList;
	}

	/**
	 * This function verifies the task is a Change Task and in a Project.
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private boolean isChangeTaskAndInProject(Context context) throws Exception
	{
		//initialize result
		boolean result = false;

		String taskType = getInfo(context, SELECT_TYPE);
		//check if task is a Change Task
		String changeTask = EnterpriseChangeConstants.TYPE_CHANGE_TASK;
		if (taskType.equals(changeTask))
		{
			//check if parent task is a Project Template
			String rootNodeType = getInfo(context, "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type");

			String templateType = PropertyUtil.getSchemaProperty(context, TYPE_PROJECT_TEMPLATE);
			String conceptType = PropertyUtil.getSchemaProperty(context, TYPE_PROJECT_CONCEPT);
			if (templateType.equals(rootNodeType)){ result = false;}
			else{ result = true;}
		}
		return result;
	}

	/**
	 * Returns Change Objects id connected as deliverable to a Change Task
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private StringList getCorrespondingDeliverableId(Context context)
	throws Exception
	{
		StringList delivearbleIdList = new StringList();
		delivearbleIdList = getInfoList(context, "from["+EnterpriseChangeConstants.RELATIONSHIP_TASK_DELIVERABLE+"].to.id");

		return delivearbleIdList;
	}
	/**
	 * Trigger to check if the deliverables are in complete state or not
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int triggerCheckChangeTaskReviewPromote(Context context, String[] args)
	throws Exception
	{
		String changeDelChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.MultipleChangeDeliverable");
		String autoPromoteDemoteChoice=EnoviaResourceBundle.getProperty(context,"emxEnterpriseChange.Allow.AutoPromoteDemote");
		if(!(changeDelChoice.equalsIgnoreCase("true")||autoPromoteDemoteChoice.equalsIgnoreCase("false"))){
			return 0;
		}
		// get values from args.
		String changeTaskId = args[0];
		setId(changeTaskId);
		boolean isdeliverableComplete=true;
		//Check if trigger should be bypassed
		if (!isChangeTaskAndInProject(context)){ return 0;}

		//get corresponding deliverables
		StringList delivearbleIdList = getCorrespondingDeliverableId(context);
		String TYPE_ECA = PropertyUtil.getSchemaProperty(context,"type_VPLMtyp@PLMActionBase");
		if(TYPE_ECA==null || TYPE_ECA.equals(""))
			TYPE_ECA = PropertyUtil.getSchemaProperty(context,"type_PLMActionBase");

		for (Iterator iterator = delivearbleIdList.iterator(); iterator.hasNext();) {

			String deliverableId=(String) iterator.next();
			DomainObject deliverableObj = new DomainObject(deliverableId);
			String deliverableStatus = deliverableObj.getInfo(context, SELECT_CURRENT);
			if(deliverableObj.isKindOf(context,EnterpriseChangeConstants.TYPE_ENGINEERING_CHANGE)){
				if(!(EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_COMPLETE.equals(deliverableStatus)||EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_CLOSE.equals(deliverableStatus)||EnterpriseChangeConstants.STATE_ENGINEERING_CHANGE_REJECT.equals(deliverableStatus))){isdeliverableComplete = false ;break;}
			}
			else if(deliverableObj.isKindOf(context,EnterpriseChangeConstants.TYPE_ECO)){
				if(!(EnterpriseChangeConstants.STATE_ECO_RELEASE.equals(deliverableStatus)||EnterpriseChangeConstants.STATE_ECO_IMPLEMENTED.equals(deliverableStatus)||EnterpriseChangeConstants.STATE_ECO_CANCELLED.equals(deliverableStatus))){isdeliverableComplete = false ;break;}
			}
			else if(deliverableObj.isKindOf(context,EnterpriseChangeConstants.TYPE_ECR)){
				if(!(EnterpriseChangeConstants.STATE_ECR_COMPLETE.equals(deliverableStatus)||EnterpriseChangeConstants.STATE_ECR_CANCELLED.equals(deliverableStatus))){isdeliverableComplete = false ;break;}
			}
			else if(deliverableObj.isKindOf(context,TYPE_ECA)){ //Added for IR-057596V6R2011x
				if(!(EnterpriseChangeConstants.STATE_ECA_SHARED.equals(deliverableStatus))){isdeliverableComplete = false ;break;}
			}
			else if(deliverableObj.isKindOf(context,EnterpriseChangeConstants.TYPE_DEFECT)){ //Added for IR-057187V6R2011x
				if(!(EnterpriseChangeConstants.STATE_DEFECT_CLOSED.equals(deliverableStatus))){isdeliverableComplete = false ;break;}
			}else if(deliverableObj.isKindOf(context,EnterpriseChangeConstants.TYPE_DEFECT_ACTION)){ //Added for IR-057187V6R2011x
				if(!(EnterpriseChangeConstants.STATE_DEFECT_ACTION_CLOSED.equals(deliverableStatus))){isdeliverableComplete = false ;break;}
			}
		}
		if (!isdeliverableComplete)
		{
			String strMsg = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxEnterpriseChange.ChangeTask_NotComplete", context.getSession().getLanguage());
			//MqlUtil.mqlCommand(context, "notice '" + strMsg + "'");
			MqlUtil.mqlCommand(context, "notice $1", strMsg);
			return 1;
		}
		return 0;
	}

	/**
	 * Get the Change Task Applicability Contexts
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @return MapList of Change Task Related Impacted Objects
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212.HFDerivations
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getChangeTaskApplicabilityContexts(Context context, String[] args) throws Exception {
		try {
			MapList returnMapList = new MapList();
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String changeTaskId = (String) paramMap.get("objectId");

			if (changeTaskId!=null && !changeTaskId.isEmpty()) {
				returnMapList = new DomainObject(changeTaskId).getRelatedObjects(context,
						EnterpriseChangeConstants.RELATIONSHIP_IMPACTED_OBJECT,
						EnterpriseChangeConstants.TYPE_MODEL,
						new StringList(DomainConstants.SELECT_ID),
						new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
						false,	//to relationship
						true,	//from relationship
						(short)1,
						DomainConstants.EMPTY_STRING, //objectWhereClause
						DomainConstants.EMPTY_STRING, //relationshipWhereClause
						0);
			}
			return returnMapList;
		} catch (Exception e) {
			throw e;
		}
	}


	/**
	 * Exclude Applicability Contexts already connected to Change Task as Impacted Objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return StringList - containing id of object already connected to Change Task as Applicability Cntexts
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212.HFDerivations
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeChangeTaskApplicabilityContexts(Context context, String[] args)throws Exception {
		try {
			StringList returnStringList = new StringList();
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");

			MapList applicabilityContexts = this.getChangeTaskApplicabilityContexts(context, args);

			Iterator<Map<String,String>> applicabilityContextsItr = applicabilityContexts.iterator();
			while (applicabilityContextsItr.hasNext()) {
				Map<String,String> applicabilityContext = applicabilityContextsItr.next();
				if (applicabilityContext!=null && !applicabilityContext.isEmpty()) {
					String applicabilityContextId = applicabilityContext.get(DomainConstants.SELECT_ID);
					if (applicabilityContextId!=null && !applicabilityContextId.isEmpty()) {
						returnStringList.addElement(applicabilityContextId);
					}
				}
			}
			return returnStringList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Remove Change Task Applicability Contexts
	 * The system will check if Applicability Contexts objects can be removed from Change Task.
	 * If the Applicability Context is not used by the latest valid Decision and all opened Decisions of the Change Task
	 * If so, a warning message will be generated
	 * Other Applicability Contexts objects will be disconnected.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *  - String objectId: Change Task ID
	 *  - String[] emxTableRowId: selected Applicability Context objects
	 *  - String strLanguage: Language
	 * @return String - containing the warning message of the unauthorized Applicability Context disconnection
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212.HFDerivations
	 */
	public String removeChangeTaskApplicabilityContexts(Context context, String[] args)throws Exception {
		try {
			String returnString = "";
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			String[] emxTableRowIds = (String[]) programMap.get("emxTableRowId");
			String strLanguage = (String) programMap.get("strLanguage");

			if ((objectId!=null && !objectId.isEmpty()) && (emxTableRowIds!=null && emxTableRowIds.length>0)) {
				Map<String,String> xmlApplicabilityExpressions = new HashMap<String,String>();
				ChangeTask changeTask = new ChangeTask(objectId);
				String latestValidDecisionId = changeTask.getLatestValidDecision(context);
				if (latestValidDecisionId!=null && !latestValidDecisionId.isEmpty()) {
					Decision latestValidDecision = new Decision(latestValidDecisionId);
					String latestXMLApplicabilityExpression = latestValidDecision.getXMLApplicabilityExpression(context, changeTask.getGrantedChangeDisciplines(context));
					if (latestXMLApplicabilityExpression!=null && !latestXMLApplicabilityExpression.isEmpty()) {
						xmlApplicabilityExpressions.put(latestValidDecisionId, latestXMLApplicabilityExpression);
					}
				}

				StringList openDecisions = changeTask.getOpenDecisions(context);
				Iterator<String> openDecisionsItr = openDecisions.iterator();
				while (openDecisionsItr.hasNext()) {
					String openDecision = openDecisionsItr.next();
					if (openDecision!=null && !openDecision.isEmpty()) {
						Decision decision = new Decision(openDecision);
						String xmlApplicabilityExpression = decision.getXMLApplicabilityExpression(context, changeTask.getGrantedChangeDisciplines(context));
						if (xmlApplicabilityExpression!=null && !xmlApplicabilityExpression.isEmpty()) {
							xmlApplicabilityExpressions.put(openDecision, xmlApplicabilityExpression);
						}
					}
				}

				StringBuffer strBuffer = new StringBuffer();
				for (int i=0;i<emxTableRowIds.length;i++) {
					StringList concernedDecisions = new StringList();
					String strTableRowId = emxTableRowIds[i];
					StringList slEmxTableRowId = FrameworkUtil.split(strTableRowId, "|");
					if (slEmxTableRowId.size() > 0){
						String selectedRelId = (String)slEmxTableRowId.get(0);
						String selectedObjectId = (String)slEmxTableRowId.get(1);
						if ((selectedObjectId!=null && !selectedObjectId.isEmpty()) && (selectedRelId!=null && !selectedRelId.isEmpty())) {
							String selectedObjectIdName = new DomainObject(selectedObjectId).getInfo(context, DomainConstants.SELECT_NAME);
							Iterator<String> xmlApplicabilityExpressionsKeysItr = xmlApplicabilityExpressions.keySet().iterator();
							while (xmlApplicabilityExpressionsKeysItr.hasNext()) {
								String xmlApplicabilityExpressionsKey = xmlApplicabilityExpressionsKeysItr.next();
								if (xmlApplicabilityExpressionsKey!=null && !xmlApplicabilityExpressionsKey.isEmpty()) {
									String xmlApplicabilityExpression = xmlApplicabilityExpressions.get(xmlApplicabilityExpressionsKey);
									if (xmlApplicabilityExpression!=null && !xmlApplicabilityExpression.isEmpty()) {
										SAXBuilder builder = new SAXBuilder();
										builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
										builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
										builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
										Document document = builder.build(new StringReader(xmlApplicabilityExpression));
										Element root = document.getRootElement();
										if (root!=null) {
											List<Element> contextElements = EnterpriseChangeUtil.getElementByName(context, root, "Context");
											Iterator<Element> contextElementsItr = contextElements.iterator();
											while(contextElementsItr.hasNext()) {
												Element contextElement = contextElementsItr.next();
												if (contextElement!=null) {
													String contextElementHolderName = contextElement.getAttributeValue("HolderName");
													if (contextElementHolderName!=null && contextElementHolderName.equalsIgnoreCase(selectedObjectIdName)) {
														concernedDecisions.addElement(xmlApplicabilityExpressionsKey);
													}
												}
											}
										}
									}
								}
							}//End of while
							if (concernedDecisions!=null && !concernedDecisions.isEmpty()) {
								if (strBuffer!=null && !strBuffer.toString().isEmpty()) {
									strBuffer.append("\\n");
								}
								strBuffer.append(selectedObjectIdName);
								strBuffer.append(":");
								strBuffer.append(" ");
								strBuffer.append(EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxEnterpriseChange.Alert.ApplicabilityContext.UsedByDecisionApplicability",strLanguage));
								strBuffer.append(" ");
								Iterator<String> concernedDecisionsItr = concernedDecisions.iterator();
								while (concernedDecisionsItr.hasNext()) {
									String concernedDecisionId = concernedDecisionsItr.next();
									if (concernedDecisionId!=null && !concernedDecisionId.isEmpty()) {
										strBuffer.append(new DomainObject(concernedDecisionId).getInfo(context, DomainConstants.SELECT_NAME));
									}
									if (concernedDecisionsItr.hasNext()) {
										strBuffer.append(" ");
									}
								}
							} else {
								DomainRelationship.disconnect(context, selectedRelId, false);
							}
						}
					}
				}

				if (strBuffer!=null && !strBuffer.toString().isEmpty()) {
					strBuffer.insert(0, EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxEnterpriseChange.Alert.ApplicabilityContext.CannotBeRemoved",strLanguage));
				}
				returnString = strBuffer.toString();
			}
			return returnString;
		} catch (Exception e) {
			throw e;
		}
	}

	/**

	 *isMPUsedInApplicabilityContext method will be called from Delete check action trigger on type Manufacturing Plan
	 *MP will not be allowed for deletion if added as applicable item in the Decision.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *  - String objectId: MP ID
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212.HFDerivations
	 * @deprecated since R418
	 */
	public int isMPUsedInApplicabilityContext(Context context, String[] args)throws Exception {
		int isMPDeleteAllowed = 1;
		try {
			String strMPId = args[0];
			DomainObject dmoMP = new DomainObject(strMPId);

			StringList selectRelStmts = new StringList(1);
			selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement(SELECT_ID);

			MapList mlDecision = 	dmoMP.getRelatedObjects(context,
					EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM,        // relationship pattern
					EnterpriseChangeConstants.TYPE_DECISION,  // object pattern
					selectStmts,       // object selects
					selectRelStmts,    // relationship selects
					true,             // to direction
					false,              // from direction
					(short) 1,         // recursion level
					null,              // object where clause
					null,
					0);

			if(mlDecision.size() == 0){
				isMPDeleteAllowed = 0;
			}

			if (isMPDeleteAllowed ==1){
				String errorMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxEnterpriseChange.Error.MPDeletionNotAllowed",context.getSession().getLanguage());
				emxContextUtil_mxJPO.mqlError(context, errorMessage);
			}

		}catch (Exception e) {
			throw e;
		}
		return isMPDeleteAllowed;
	}



	/**
	 * Get the Change Task Impacted Objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @return MapList of Change Task Related Impacted Objects
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getChangeTaskImpactedObjects(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String changeTaskId = (String) paramMap.get("objectId");

    		returnMapList = new DomainObject(changeTaskId).getRelatedObjects(context,
    				EnterpriseChangeConstants.RELATIONSHIP_IMPACTED_OBJECT,
    				QUERY_WILDCARD,
					new StringList(DomainConstants.SELECT_ID),
					new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
					false,	//to relationship
					true,	//from relationship
					(short)1,
					DomainConstants.EMPTY_STRING, //objectWhereClause
					DomainConstants.EMPTY_STRING, //relationshipWhereClause
					0);

		}catch (Exception e){
			throw e;
		}finally{
			return returnMapList;
		}
	}


	/**
	 * Exclude Impacted Objects already connected to Change Task as Impacted Objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return StringList - containing id of object already connected to Change Task as Impacted Objects
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeChangeTaskImpactedObjects(Context context, String[] args)throws Exception{
		StringList returnStringList = new StringList();
		try{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");

			StringList selectStmts = new StringList(1);
			selectStmts.addElement(SELECT_ID);

			StringList selectRelStmts = new StringList(1);
			selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

			if(objectId!=null && !objectId.equalsIgnoreCase("")){
				DomainObject domObj = new DomainObject(objectId);

				MapList relatedImpactedObjects = domObj.getRelatedObjects(context,
						EnterpriseChangeConstants.RELATIONSHIP_IMPACTED_OBJECT,
						DomainConstants.QUERY_WILDCARD,
						new StringList(DomainConstants.SELECT_ID),
						new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
						false,	//to relationship
						true,	//from relationship
						(short)1,
						DomainConstants.EMPTY_STRING, // object where clause
						DomainConstants.EMPTY_STRING, // relationship where clause
						0);

				Iterator tempIterator = relatedImpactedObjects.iterator();
				while(tempIterator.hasNext()){
					Map tempMap = (Map)tempIterator.next();
					String tempId = (String)tempMap.get(DomainConstants.SELECT_ID);
					returnStringList.add(tempId);
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnStringList;
		}
	}

  	
  	/**
  	 * set Change discipline field.
  	 * @param context - The eMatrix <code>Context</code> object.
  	 * @param args holds information about object.
  	 * @return change discipline fields name.
  	 * @throws Exception if operation fails.
  	 */
  	public void setChangeDisciplineAttribute(Context context,String[]args)throws Exception
  	{
  		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		String newObjectId = (String) paramMap.get("newObjectId");
		HashMap requestMap = (HashMap) paramMap.get("requestMap");
		
		DomainObject newObject = new DomainObject(newObjectId);
  		
		//Check if an the change discipline interface has been already connected
		//add interface attribute for Change Discipline
		String strInterfaceName = PropertyUtil.getSchemaProperty(context,"interface_ChangeDiscipline");

		String sCommandStatement = "print bus $1 select $2 dump";
		String sCommandResult =  MqlUtil.mqlCommand(context, sCommandStatement,newObjectId, "interface["+ strInterfaceName + "]"); 

		//If no interface --> add one
		if("false".equalsIgnoreCase(sCommandResult)){
			String strAddInterface = "modify bus $1 add interface $2";
			MqlUtil.mqlCommand(context, strAddInterface,newObjectId, strInterfaceName);
		}

		BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
		AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

		java.util.Iterator listInterfaceAttributesItr = listInterfaceAttributes.iterator();
		while(listInterfaceAttributesItr.hasNext()){
			String attrName = ((AttributeType) listInterfaceAttributesItr.next()).getName();
			String attrNameSmall = attrName.replaceAll(" ", "");
			String attrNameSmallHidden = attrNameSmall + "Hidden";
			String[] attrNameValueArr = (String[])requestMap.get(attrNameSmallHidden);
			
			if(attrNameValueArr != null && attrNameValueArr.length > 0){
				String attrNameValue = attrNameValueArr[0];
				if(!attrNameValue.equalsIgnoreCase("") && !attrNameValue.equalsIgnoreCase("No")){
					newObject.setAttributeValue(context, attrName, attrNameValue);
				}else{
					newObject.setAttributeValue(context, attrName, "No");
				}
			}
		}
  	}

  	/**
  	 * set applicability context Field on task creation page.
  	 * @param The eMatrix <code>Context</code> object.
  	 * @param args holds information about object.
  	 * @return applicability context Field.
  	 * @throws Exception if operation fails.
  	 */
  	public void setApplicabilityContext(Context context,String[] args)throws Exception
  	{
  		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		String newObjectId = (String) paramMap.get("newObjectId");
		HashMap requestMap = (HashMap) paramMap.get("requestMap");
		
		DomainObject newObject = new DomainObject(newObjectId);
		
  		Boolean showApplicabilityContext = false;
		try{
			Boolean manageApplicability = true;
			if(manageApplicability!=null){
				showApplicabilityContext = manageApplicability;
			}
		}catch(Exception e){
			showApplicabilityContext = false;
		}
		if (showApplicabilityContext) {

			String[] applicabilityContextsArr = (String[])requestMap.get("ApplicabilityContextsHidden");
			if(applicabilityContextsArr != null && applicabilityContextsArr.length > 0){
				String applicabilityContexts = applicabilityContextsArr[0];
				if (!applicabilityContexts.equalsIgnoreCase("")) {
					StringList applicabilityContextsList = FrameworkUtil.split(applicabilityContexts, ",");
					if (applicabilityContextsList!= null && !applicabilityContextsList.isEmpty()) {
						for(int i=0;i<applicabilityContextsList.size();i++){
							String applicabilityContext = (String)applicabilityContextsList.get(i);
							if (applicabilityContext!=null && !applicabilityContext.isEmpty()) {
								DomainRelationship domRel = DomainRelationship.connect(context,
										newObject,
										PropertyUtil.getSchemaProperty(context,"relationship_ImpactedObject"),
										new DomainObject(applicabilityContext));
							}
						}
					}
				}
			}			
		}
		else{
			String[] strImpactedObjectArr = (String[])requestMap.get("impactedObjectHidden");
			if(strImpactedObjectArr != null && strImpactedObjectArr.length > 0){
				String strImpactedObject = strImpactedObjectArr[0];
				if(strImpactedObject != null && !strImpactedObject.equalsIgnoreCase("")){
					DomainRelationship domRel = DomainRelationship.connect(context,
							newObject,
							PropertyUtil.getSchemaProperty(context,"relationship_ImpactedObject"),
							new DomainObject(strImpactedObject));
				}
			}			
		}
  	}
}
