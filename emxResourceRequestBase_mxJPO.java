/*
 * emxResourceRequestBase
 *
 * Copyright (c) 1999-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * Dassault Systems.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 */

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.Signature;
import matrix.db.SignatureItr;
import matrix.db.SignatureList;
import matrix.db.State;
import matrix.db.StateBranch;
import matrix.db.StateBranchItr;
import matrix.db.StateBranchList;
import matrix.db.StateItr;
import matrix.db.StateList;
import matrix.db.StateRequirement;
import matrix.db.StateRequirementItr;
import matrix.db.StateRequirementList;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PolicyUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.program.Currency;
import com.matrixone.apps.program.FTE;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ResourcePlan;
import com.matrixone.apps.program.ResourcePlanTemplate;
import com.matrixone.apps.program.ResourceRequest;

public class emxResourceRequestBase_mxJPO extends emxDomainObjectBase_mxJPO 
{

	protected static final String SELECT_RESOURCE_POOL_NAME = "from["+RELATIONSHIP_RESOURCE_POOL+"].to.name";
	protected static final String SELECT_RESOURCE_POOL_ID = "from["+RELATIONSHIP_RESOURCE_POOL+"].to.id";
	protected static final String SELECT_COMPETENCY = "from["+RELATIONSHIP_RESOURCE_REQUEST_SKILL+"].attribute["+ATTRIBUTE_COMPETENCY+"]";
	protected static final String SELECT_BUSINESS_SKILL_NAME = "from["+RELATIONSHIP_RESOURCE_REQUEST_SKILL+"].to.name";
	protected static final String SELECT_BUSINESS_SKILL_ID = "from["+RELATIONSHIP_RESOURCE_REQUEST_SKILL+"].to.id";
	protected static final String SELECT_PROJECTROLE = "from["+RELATIONSHIP_SUBTASK+"].attribute["+ATTRIBUTE_PROJECT_ROLE+"]";
	protected static final String SELECT_RESOURCE_MANAGER_NAME = "from["+RELATIONSHIP_RESOURCE_MANAGER+"].to.name";
	protected static final String SELECT_PROJECT_SPACE_ID = "to["+RELATIONSHIP_RESOURCE_PLAN+"].from.id";
	protected static final String SELECT_PROJECT_SPACE_NAME = "to["+RELATIONSHIP_RESOURCE_PLAN+"].from.name";
	protected static final String SELECT_PROJECT_SPACE_STATE = "to["+RELATIONSHIP_RESOURCE_PLAN+"].from.current";
	protected static final String SELECT_RESOURCE_PLAN_FTE = "to["+RELATIONSHIP_RESOURCE_PLAN+"].attribute["+ATTRIBUTE_FTE+"]";
	protected static final String SELECT_PROJECT_TEMPLATE_ID = "to["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].fromrel["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_PLAN_TEMPLATE+"].from."+SELECT_ID;
	protected static final String SELECT_PROJECT_MEMBERS = "from["+RELATIONSHIP_MEMBER+"].to.id";
	protected static final String SELECT_THREAD_ID = "from["+RELATIONSHIP_THREAD+"].to.id";
	protected static final String SELECT_SUBSCRIPTION_ID = "from["+RELATIONSHIP_PUBLISH_SUBSCRIBE+"].to.id";
	protected static final String SELECT_MESSAGE_ID = "from["+RELATIONSHIP_MESSAGE+"].to.id";
	protected static final String SELECT_PERSON_ID_FROM_RESOURCE_REQUEST = "from["+RELATIONSHIP_ALLOCATED+"].to.id";
	protected static final String SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST = "from["+RELATIONSHIP_ALLOCATED+"].attribute["+ATTRIBUTE_FTE+"]";
	protected static final String SELECT_PERSON_STATE_FROM_RESOURCE_REQUEST = "from["+RELATIONSHIP_ALLOCATED+"].attribute["+ATTRIBUTE_RESOURCE_STATE+"]";
	protected static final String SELECT_RESOURCE_MANAGER_ID = "from["+RELATIONSHIP_RESOURCE_MANAGER+"].to."+SELECT_ID;
	protected static final String SELECT_RESOURCE_ID = "from["+RELATIONSHIP_ALLOCATED+"].to."+SELECT_ID;
	protected static final String SELECT_ORGANIZATION_ID = "to["+RELATIONSHIP_COMPANY_PROJECT+"].from.id";
	protected static final String SELECT_ORGANIZATION_NAME = "to["+RELATIONSHIP_COMPANY_PROJECT+"].from.name";
	protected static final String SELECT_PROJECT_ROLE = "attribute_ProjectRole";
	protected static final String SELECT_BU_ID = "to["+RELATIONSHIP_BUSINESS_UNIT_PROJECT+"].from.id";
	protected static final String SELECT_BU_NAME = "to["+RELATIONSHIP_BUSINESS_UNIT_PROJECT+"].from.name";
	final String SELECT_REL_ATTRIBUTE_FTE = DomainRelationship.getAttributeSelect(ATTRIBUTE_FTE);
	final String SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST_TYPE = "from[" + RELATIONSHIP_ALLOCATED + "].to.type.kindof";
	public static final String SELECT_ATTRIBUTE_START_DATE = "attribute[" + PropertyUtil.getSchemaProperty("attribute_StartDate") + "]";
	public static final String SELECT_ATTRIBUTE_END_DATE = "attribute[" + PropertyUtil.getSchemaProperty("attribute_EndDate") + "]";
	public static final String ATTRIBUTE_PHASE_START_DATE = "attribute["+ATTRIBUTE_TASK_ESTIMATED_START_DATE+"]";
	public static final String ATTRIBUTE_PHASE_FINISH_DATE = "attribute["+ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE+"]";
	public static final String ATTRIBUTE_PHASE_ESTIMATED_DURATION = "attribute["+ATTRIBUTE_TASK_ESTIMATED_DURATION+"]";
	public static final String SELECT_RESOURCE_REQUEST_PHASE_RELATIONSHIP_ID = "from["+ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE+"]."+SELECT_ID;
	protected static final int TRIGGER_SUCCESS = 0;
	protected static final int TRIGGER_FAILURE = 1;
	public static final String ATTRIBUTE_STANDARD_COST = PropertyUtil.getSchemaProperty("attribute_StandardCost");
	public static final String SELECT_ATTRIBUTE_STANDARD_COST = "attribute["+ATTRIBUTE_STANDARD_COST+"].value";
	public static final String SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY = "attribute["+ ATTRIBUTE_STANDARD_COST + "].inputunit";
	public static final String SELECT_PERSON_PHASE_FTE_RELATIONSHIP_ID = "from["+ResourcePlanTemplate.RELATIONSHIP_PERSON_PHASE_FTE+"]."+SELECT_ID;
	public static final String SELECT_ATTRIBUTE_FTE = "attribute["+ATTRIBUTE_FTE+"].value";
	public static final String SELECT_REQUEST_RESOURCE_PLAN_PREFRENCE = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from.attribute["+ResourceRequest.ATTRIBUTE_RESOURCE_PLAN_PREFRENCE+"].value";

	public static final String ATTRIBUTE_RATE = PropertyUtil.getSchemaProperty("attribute_Rate");
	public static final String ATTRIBUTE_START_EFFECTIVITY = PropertyUtil.getSchemaProperty("attribute_StartEffectivity");
	public static final String SELECT_RATE_PERIOD_RATE = "from[" + RELATIONSHIP_RATE_PERIOD + "]."+DomainRelationship.getAttributeSelect(ATTRIBUTE_RATE);
	public static final String SELECT_RATE_PERIOD_START_EFFECTIVITY = "from[" + RELATIONSHIP_RATE_PERIOD + "]."+DomainRelationship.getAttributeSelect(ATTRIBUTE_START_EFFECTIVITY);
	public static final String SELECT_IS_RESOURCE_REQUEST = "type.kindof["+TYPE_RESOURCE_REQUEST+"]";
	public static final String RESOURCE_PLAN_PREFERENCE_PHASE="Phase";
	public static final String RESOURCE_PLAN_PREFERENCE_TIMELINE="Timeline";

	public static final double WORKING_HOURS_PER_DAY= 8;

	public static final String ROLE_EXTERNAL_PROJECT_LEAD = "External Project Lead";
	public static final String ACCESS_PROJECT_OWNER = "Project Owner";
	public static final String ACCESS_PROJECT_LEAD = "Project Lead";

	protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null; 
	private Context context;
	/**
	 * Constructor 
	 * 
	 * @param context The Matrix Context object
	 *@param args The arguments array
	 * @throws Exception if operation fails
	 */
	public emxResourceRequestBase_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
		this.context = context;
	}


	/**
	 * Main method of JPO. Execution entry point.
	 *
	 * @param context The Matrix Context object
	 * @param args The command line arguments
	 * @returns int  return code 1 for error 0 for success
	 * @throws MatrixException if the operation fails
	 */
	public int mxMain(Context context, String[] args) throws MatrixException {
		throw new MatrixException("This JPO cannot be run stand alone.");
	}


	/**
	 * Gets resource plan table data for resource request
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableResourcePlanRequestData(Context context, String[] args) throws Exception
	{
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");
			String strProjectID = (String) programMap.get("projectID");
			MapList mlRequests = new MapList();

			if(null==strProjectID || "".equals(strProjectID) || "Null".equalsIgnoreCase(strProjectID))
			{
				strProjectID = strObjectId;
			}
			DomainObject dmoObject = DomainObject.newInstance(context, strProjectID);
			String strResourcePlanPref = getResourcePlanPreference(context, dmoObject);
			if(null!=strResourcePlanPref)
			{
				if(RESOURCE_PLAN_PREFERENCE_TIMELINE.equals(strResourcePlanPref))
				{
					mlRequests = getTableResourcePlanTimelineRequestData(context, args);
				}
				else if(RESOURCE_PLAN_PREFERENCE_PHASE.equals(strResourcePlanPref))
				{
					mlRequests = getTableResourcePlanPhaseRequestData(context, args);
				}
			}
			ResourceRequest.tableValidateCurrency(context,mlRequests);		
			return mlRequests ;
		} 
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}
	private MapList getTableResourcePlanPhaseRequestData(Context context,
			String[] args) throws MatrixException {
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");
			String strProjectID = (String) programMap.get("projectID");
			String strLanguage = (String)programMap.get("languageStr");
			String strFilterValue = (String)programMap.get("PMCResourceRequestPhaseTimelineFilter");
			String strLifecycleFilter =(String) programMap.get("PMCResourceRequestLifecycleFilter");
			MapList mlRequests = getResourceRequestDetailInfo(context,
					strObjectId, strLifecycleFilter);
			Map mapResourceRequestTranslatedStates = getTranstedResourceRequestPolicyName(
					context, strLanguage);
			Map mapRequestInfo = null;
			String strStateValue = null;
			FTE fte = FTE.getInstance(context);
			for (Iterator iterRequest = mlRequests.iterator(); iterRequest .hasNext();)
			{
				mapRequestInfo = (Map) iterRequest.next();
				strStateValue = (String) mapRequestInfo.get(SELECT_CURRENT);
				mapRequestInfo.put("CurrentState", mapResourceRequestTranslatedStates.get(strStateValue));
				mapRequestInfo.put("ViewBy", strFilterValue);

				getTableResourcePlanUpdateFTEMap(context, strProjectID, strFilterValue,
						mapRequestInfo, fte, RESOURCE_PLAN_PREFERENCE_PHASE, TYPE_RESOURCE_REQUEST, null);	
				Object objPersonFTEType = null;
				if(mapRequestInfo.get(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST)!=null)
				{
					mapRequestInfo.put("PersonId", mapRequestInfo.get(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST));
				}
				getTableResourcePlanUpdatePersonFTEMap(mapRequestInfo);
			}
			return mlRequests ;
		} 
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}


	/**
	 * @param mapRequestInfo
	 * @param fte
	 * @return
	 * @throws MatrixException
	 */
	private void getTableResourcePlanUpdatePersonFTEMap(Map mapRequestInfo) throws MatrixException {
		Object objPersonFTEType;
		FTE fte;
		if(mapRequestInfo.get(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST)!=null)
		{
			objPersonFTEType = mapRequestInfo.get(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST_TYPE); 
			Map mapPersonsFTEValues = new HashMap(); 
			if (objPersonFTEType instanceof String) {
				String strPersonFTE = (String)mapRequestInfo.get(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);
				Map mapFTEValues = null;
				if(null != strPersonFTE && !"null".equals(strPersonFTE) && !"".equals(strPersonFTE)){    
					fte = FTE.getInstance(context, strPersonFTE);
					mapFTEValues = fte.getAllFTE();
					if(null != mapFTEValues && !"null".equals(mapFTEValues) && !"".equals(mapFTEValues)){ 
						for (Iterator iter = mapFTEValues.keySet().iterator(); iter.hasNext();) 
						{
							String strTimeFrame = (String)iter.next();
							Double dFTEValue = 0D;
							if(null!=mapPersonsFTEValues.get(strTimeFrame))
							{
								dFTEValue= (Double)mapPersonsFTEValues.get(strTimeFrame);
							}
							dFTEValue = new Double(dFTEValue.doubleValue()+((Double)mapFTEValues.get(strTimeFrame)).doubleValue());
							mapPersonsFTEValues.put(strTimeFrame,dFTEValue);
						}
					}
				}
			}
			if (objPersonFTEType instanceof StringList) 
			{
				StringList strPersonFTE = (StringList)mapRequestInfo.get(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);
				Map mapFTEValues = null;
				if(strPersonFTE.size() != 0){
					for(int i=0; i<strPersonFTE.size(); i++)
					{
						String strFTE = (String)strPersonFTE.get(i);
						if(null != strFTE && !"null".equals(strFTE) && !"".equals(strFTE)){
							fte = FTE.getInstance(context, strFTE);
							mapFTEValues = fte.getAllFTE();
							if(null != mapFTEValues && !"null".equals(mapFTEValues) && !"".equals(mapFTEValues)){
							for (Iterator iter = mapFTEValues.keySet().iterator(); iter.hasNext();) 
							{
								String strTimeFrame = (String)iter.next();
								Double dFTEValue = 0D;
								if(null!=mapPersonsFTEValues.get(strTimeFrame))
								{
									dFTEValue= (Double)mapPersonsFTEValues.get(strTimeFrame);
								}
								dFTEValue = new Double(dFTEValue.doubleValue()+((Double)mapFTEValues.get(strTimeFrame)).doubleValue());
								mapPersonsFTEValues.put(strTimeFrame,dFTEValue);
							}
						}
					}
				}
			}
			}
			mapRequestInfo.put("PersonFTE", mapPersonsFTEValues);
		}
	}


	/**
	 * @param strFilterValue
	 * @param mapRequestInfo
	 * @param fte
	 * @param strRequestId TODO
	 * @throws MatrixException
	 */
	private void getTableResourcePlanUpdateFTEMap(Context context, String strProjectId, String strFilterValue,
			Map mapRequestInfo, FTE fte, String strResourcePlanPrefMode, String strObjectType, String strRequestId) throws MatrixException 
			{
		try {
			if(null != strFilterValue && RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strResourcePlanPrefMode))
			{
				if(RESOURCE_PLAN_PREFERENCE_TIMELINE.equalsIgnoreCase(strFilterValue))
				{
					if(null != mapRequestInfo.get(SELECT_REL_ATTRIBUTE_FTE) && !"null".equals(mapRequestInfo.get(SELECT_REL_ATTRIBUTE_FTE)) 
							&& !"".equals(mapRequestInfo.get(SELECT_REL_ATTRIBUTE_FTE)))
					{
						fte = FTE.getInstance(context, (String)mapRequestInfo.get(SELECT_REL_ATTRIBUTE_FTE));
						mapRequestInfo.put("FTE", fte);
					}
				}
				else if(RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strFilterValue))
				{
					if(TYPE_RESOURCE_REQUEST.equals(strObjectType))
					{
						Map phaseFTEMap = getCalculatedFTEForPhase(mapRequestInfo);
						mapRequestInfo.put("FTE",phaseFTEMap);
					}
					else if(TYPE_PERSON.equals(strObjectType))
					{
						Map mapPhaseFTEValue = getCalculatedPhaseFTEForPerson(context, mapRequestInfo,strRequestId);
						mapRequestInfo.put("FTE", mapPhaseFTEValue);
					}
				}
			}
			else if(RESOURCE_PLAN_PREFERENCE_TIMELINE.equalsIgnoreCase(strResourcePlanPrefMode))
			{
				if(null != mapRequestInfo.get(SELECT_REL_ATTRIBUTE_FTE) && !"null".equals(mapRequestInfo.get(SELECT_REL_ATTRIBUTE_FTE)) 
						&& !"".equals(mapRequestInfo.get(SELECT_REL_ATTRIBUTE_FTE)))
				{
					fte = FTE.getInstance(context, (String)mapRequestInfo.get(SELECT_REL_ATTRIBUTE_FTE));
					if(null != strFilterValue && RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strFilterValue))
					{
						Map mapPhaseFTEValue = getPhaseColumns(context, strProjectId, fte,strRequestId);
						mapRequestInfo.put("FTE", mapPhaseFTEValue);
					}
					else
					{
						mapRequestInfo.put("FTE", fte);
					}
				}
			}
		} catch (Exception e) 
		{
			throw new MatrixException(e);
		}
			}
	/**
	 * @param mapRequestInfo
	 * @param strFilterValue
	 * @throws MatrixException
	 */
	private Map getCalculatedFTEForPhase(Map mapRequestInfo) throws MatrixException 
	{
		Object objResourceReqPhase = mapRequestInfo.get(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
		Object objResourceReqPhaseFTE = mapRequestInfo.get(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
		StringList slResourceReqPhaseId = new StringList();
		StringList slResourceReqPhaseFTE = new StringList();
		if (objResourceReqPhase instanceof String) 
		{
			slResourceReqPhaseId.add((String)objResourceReqPhase);
			slResourceReqPhaseFTE.add((String)objResourceReqPhaseFTE);
		}
		else if (objResourceReqPhase instanceof StringList) 
		{
			slResourceReqPhaseId = (StringList)objResourceReqPhase;
			slResourceReqPhaseFTE = (StringList)objResourceReqPhaseFTE;
		}
		Map phaseFTEMap = new HashMap();
		if(null!=slResourceReqPhaseId && slResourceReqPhaseId.size()>0)
		{
			for(int i=0; i<slResourceReqPhaseId.size();i++)
			{
				String strPhaseId = (String)slResourceReqPhaseId.get(i);
				phaseFTEMap.put("PhaseOID"+"-"+strPhaseId, (String)slResourceReqPhaseFTE.get(i));
			}
		}
		return phaseFTEMap;
	}

	/**
	 * @param mapRequestInfo
	 * @param strFilterValue
	 * @throws MatrixException
	 */
	private Map getCalculatedPhaseFTEForPerson(Context context, Map mapRequestInfo,String strRequestId) throws MatrixException 
	{
		BusinessObjectWithSelectList businessObjectWithSelectList = null;
		String strPersonId = (String)mapRequestInfo.get(SELECT_ID);
		String[] strResourceRequestIds = new String[]{strRequestId};
		StringList slSelectList = new StringList();
		slSelectList.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
		slSelectList.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_NAME);
		slSelectList.add(SELECT_RESOURCE_REQUEST_PHASE_RELATIONSHIP_ID);
		BusinessObjectWithSelectList resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strResourceRequestIds,slSelectList);
		StringList slResourceReqPhaseId = new StringList();
		StringList slResourceReqPhaseRelId = new StringList();
		for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
		{
			BusinessObjectWithSelect bows = itr.obj();
			slResourceReqPhaseId = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
			slResourceReqPhaseRelId = bows.getSelectDataList(SELECT_RESOURCE_REQUEST_PHASE_RELATIONSHIP_ID);
		}
		Map phaseFTEMap = new HashMap();
		if(null!=slResourceReqPhaseId && slResourceReqPhaseId.size()>0)
		{
			for(int i=0; i<slResourceReqPhaseId.size();i++)
			{
				String strPhaseId = (String)slResourceReqPhaseId.get(i);
				String strPhaseFTERelId = (String)slResourceReqPhaseRelId.get(i);
				String sCommandStatement = "print bus $1 select $2 dump";
				String strPersonPhaseFTE =  MqlUtil.mqlCommand(context, sCommandStatement,strPersonId, "from[Person Phase FTE|torel.id=="+strPhaseFTERelId+"].attribute[FTE].value"); 
				phaseFTEMap.put("PhaseOID"+"-"+strPhaseId, strPersonPhaseFTE);
			}
		}
		return phaseFTEMap;
	}



	/**
	 * @param context
	 * @param strObjectId
	 * @param strLifecycleFilter
	 * @param SELECT_REL_ATTRIBUTE_FTE
	 * @param SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST_TYPE
	 * @return
	 * @throws Exception
	 * @throws FrameworkException
	 */
	private MapList getResourceRequestDetailInfo(Context context,
			String strObjectId, String strLifecycleFilter)
	throws Exception, FrameworkException 
	{
		String strRelationshipType = RELATIONSHIP_RESOURCE_PLAN;
		String strType = TYPE_RESOURCE_REQUEST;
		String whereClause = "" ;
		StringList busSelect = new StringList();
		busSelect.add(DomainConstants.SELECT_ID);
		busSelect.add(DomainConstants.SELECT_NAME);
		busSelect.add(SELECT_CURRENT);
		busSelect.add(SELECT_BUSINESS_SKILL_NAME);
		busSelect.add(SELECT_COMPETENCY);
		busSelect.add(SELECT_RESOURCE_POOL_NAME);
		busSelect.add(SELECT_RESOURCE_POOL_ID);
		busSelect.add(SELECT_ATTRIBUTE_START_DATE);
		busSelect.add(SELECT_ATTRIBUTE_END_DATE);
		busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
		busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_NAME);
		busSelect.add(SELECT_RESOURCE_REQUEST_PHASE_RELATIONSHIP_ID);
		busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
		busSelect.add(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST);
		busSelect.add(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);
		busSelect.add(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST_TYPE);
		busSelect.add(SELECT_ATTRIBUTE_STANDARD_COST);
		busSelect.add(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY);
		StringList relSelect = new StringList();
		relSelect.add(DomainRelationship.SELECT_ID);
		relSelect.add(SELECT_REL_ATTRIBUTE_FTE);
		if(null != strLifecycleFilter && !"".equals(strLifecycleFilter) && !"null".equals(strLifecycleFilter))
		{
			if("Open".equals(strLifecycleFilter))
			{
				whereClause = "("+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED+" || "+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED+" || "+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_REJECTED+" || "+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_CREATE+" )";
			}
			else if("Committed" .equals(strLifecycleFilter))
			{
				whereClause = "("+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_COMMITTED+")";
			}
			else if("Rejected" .equals(strLifecycleFilter))
			{
				whereClause = "("+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_REJECTED+")";
			}
		}
		DomainObject dom = DomainObject.newInstance(context, strObjectId);
		MapList mlRequests = dom.getRelatedObjects(
				context,
				strRelationshipType,
				strType,
				busSelect,
				relSelect,
				false,
				true,
				(short)1,
				whereClause,
				DomainConstants.EMPTY_STRING,0);
		return mlRequests;
	}


	/**
	 * Gets resource plan table data for resource request
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	public MapList getTableResourcePlanTimelineRequestData(Context context, String[] args) throws Exception
	{
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");
			String strProjectID = (String) programMap.get("projectID");
			String strFilterValue = (String)programMap.get("PMCResourceRequestPhaseTimelineFilter");
			String strLifecycleFilter =(String) programMap.get("PMCResourceRequestLifecycleFilter");

			String strLanguage = (String)programMap.get("languageStr");
			MapList mlRequests = getResourceRequestDetailInfo(context,strObjectId, strLifecycleFilter);

			Map mapResourceRequestTranslatedStates = getTranstedResourceRequestPolicyName(context, strLanguage);

			Map mapRequestInfo = null;
			String strStateValue = null;
			String strRequestId = null;
			FTE fte = FTE.getInstance(context);

			for (Iterator iterRequest = mlRequests.iterator(); iterRequest .hasNext();)
			{
				mapRequestInfo = (Map) iterRequest.next();
				strRequestId = (String) mapRequestInfo.get(SELECT_ID);
				strStateValue = (String) mapRequestInfo.get(SELECT_CURRENT);
				mapRequestInfo.put("CurrentState", mapResourceRequestTranslatedStates.get(strStateValue));
				mapRequestInfo.put("ViewBy", strFilterValue);
				getTableResourcePlanUpdateFTEMap(context, strProjectID, strFilterValue,
						mapRequestInfo, fte, RESOURCE_PLAN_PREFERENCE_TIMELINE,TYPE_RESOURCE_REQUEST, strRequestId);	
				Object objPersonFTEType = null;
				if(mapRequestInfo.get(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST)!=null)
				{
					mapRequestInfo.put("PersonId", mapRequestInfo.get(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST));
				}
				getTableResourcePlanUpdatePersonFTEMap(mapRequestInfo);
			}
			return mlRequests ;
		} 
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}


	private Map getTranstedResourceRequestPolicyName(Context context,String strLanguage) throws MatrixException 
	{
		Policy policy = new Policy(POLICY_RESOURCE_REQUEST); 
		policy.open(context);
		StateRequirementList slStateRequirement = policy.getStateRequirements(context);
		policy.close(context);

		StateRequirement stateRequirement = null;
		Map mapResourceRequestTranslatedStates = new HashMap();
		for (StateRequirementItr requirementItr = new StateRequirementItr(slStateRequirement); requirementItr.next();)
		{
			stateRequirement = requirementItr.obj();
			mapResourceRequestTranslatedStates.put(stateRequirement.getName(), i18nNow.getStateI18NString(POLICY_RESOURCE_REQUEST, stateRequirement.getName(), strLanguage));
		}
		return mapResourceRequestTranslatedStates;
	}

	/**
	 * Gets resource pool > resource request table data
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableResourcePoolRequestData(Context context, String[] args) throws Exception
	{ 
		try
		{
			Map programMap = (Map)JPO.unpackArgs(args);
			String strObjId = (String) programMap.get("objectId");
			String strLifecycleFilter =(String) programMap.get("PMCResourcePoolLifecycleFilter");
			final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to[" + RELATIONSHIP_RESOURCE_PLAN + "].attribute[" + ATTRIBUTE_FTE + "]";
			String strLanguage = context.getSession().getLanguage();
			DomainObject dmoResourcePool = DomainObject.newInstance(context, strObjId);
			final String SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST_TYPE = "from[" + RELATIONSHIP_ALLOCATED + "].to.type.kindof";
			final String SELECT_RESOURCE_REQUEST_PROJECT_NAME = "to["+ RELATIONSHIP_RESOURCE_PLAN +"].from.name";
			final String SELECT_RESOURCE_REQUEST_PROJECT_ID = "to["+ RELATIONSHIP_RESOURCE_PLAN +"].from.id";
			final String SELECT_RESOURCE_REQUEST_PROJECT_OWNER = "to["+ RELATIONSHIP_RESOURCE_PLAN +"].from.owner";
			final String SELECT_RESOURCE_REQUEST_PROJECT_STATE = "to["+ RELATIONSHIP_RESOURCE_PLAN +"].from.current";
			String strRelationshipPattern = RELATIONSHIP_RESOURCE_POOL;
			String strTypePattern = TYPE_RESOURCE_REQUEST;
			String whereClause = "" ;
			StringList slBusSelect = new StringList();

			slBusSelect.add(SELECT_ID);
			slBusSelect.add(SELECT_NAME);
			slBusSelect.add(SELECT_CURRENT);
			slBusSelect.add(SELECT_BUSINESS_SKILL_NAME);
			slBusSelect.add(SELECT_BUSINESS_SKILL_ID);
			slBusSelect.add(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
			slBusSelect.add(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST);
			slBusSelect.add(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);
			slBusSelect.add(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST_TYPE);
			slBusSelect.add(SELECT_RESOURCE_REQUEST_PROJECT_NAME);
			slBusSelect.add(SELECT_RESOURCE_REQUEST_PROJECT_ID);
			slBusSelect.add(SELECT_RESOURCE_REQUEST_PROJECT_OWNER);
			slBusSelect.add(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY);
			slBusSelect.add(SELECT_RESOURCE_REQUEST_PROJECT_STATE);
			StringList slRelSelect = new StringList();

			if(null != strLifecycleFilter && !"".equals(strLifecycleFilter) && !"null".equals(strLifecycleFilter)){
				if("All".equals(strLifecycleFilter))
				{
					whereClause = "("+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_REQUESTED+" || "+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_PROPOSED+"|| "+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_COMMITTED+"||"+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_REJECTED+")";
				}
				else if("Open".equals(strLifecycleFilter))
				{
					whereClause = "("+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_REQUESTED+" || "+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_PROPOSED+")";
				}
				else if("Committed" .equals(strLifecycleFilter))
				{
					whereClause = "("+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_COMMITTED+")";
				}
				else if("Rejected" .equals(strLifecycleFilter))
				{
					whereClause = "("+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_REJECTED+")";
				}
			}
			boolean getFrom = false;
			boolean getTo = true;
			short recurseToLevel = 1;
			String strBusWhere = "";
			String strRelWhere = "";
			MapList mlResourceRequestsList = new MapList();
			try{
				ProgramCentralUtil.pushUserContext(context);
				mlResourceRequestsList = dmoResourcePool.getRelatedObjects(context,
						strRelationshipPattern, //pattern to match relationships
						strTypePattern, //pattern to match types
						slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
						slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
						getTo, //get To relationships
						getFrom, //get From relationships
						recurseToLevel, //the number of levels to expand, 0 equals expand all.
						whereClause, //where clause to apply to objects, can be empty ""
						strRelWhere,0); //where clause to apply to relationship, can be empty ""
			}finally{
				ProgramCentralUtil.popUserContext(context);
			}
			
			/*MapList mlResourceRequestsList = dmoResourcePool.getRelatedObjects(context,
					strRelationshipPattern, //pattern to match relationships
					strTypePattern, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
					slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getTo, //get To relationships
					getFrom, //get From relationships
					recurseToLevel, //the number of levels to expand, 0 equals expand all.
					whereClause, //where clause to apply to objects, can be empty ""
					strRelWhere,0); //where clause to apply to relationship, can be empty ""
*/			

			Map mapResourceRequestTranslatedStates = getTranstedResourceRequestPolicyName(
					context, strLanguage);

			Map mapRequestInfo = null;
			String strStateValue = null;
			String strProjectId = null;
			boolean isProjectCompleted 	= false;
			boolean isProjectOnHold 	= false;
			FTE fte = null;
			for (Iterator iterRequest = mlResourceRequestsList.iterator(); iterRequest .hasNext();)
			{
				mapRequestInfo = (Map) iterRequest.next();
				String strResourcereqID = (String) mapRequestInfo.get(SELECT_ID);
				strStateValue = (String) mapRequestInfo.get(SELECT_CURRENT);
				strProjectId = (String) mapRequestInfo.get(SELECT_RESOURCE_REQUEST_PROJECT_ID);
				//isProjectCompleted = PolicyUtil.checkState(context,strProjectId,STATE_PROJECT_SPACE_COMPLETE,PolicyUtil.GE);
				//isProjectOnHold = PolicyUtil.checkState(context,strProjectId,ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD,PolicyUtil.GE);
				String projectState = (String) mapRequestInfo.get(SELECT_RESOURCE_REQUEST_PROJECT_STATE);
				if (ProgramCentralUtil.isNotNullString(projectState) && (
						projectState.equalsIgnoreCase(STATE_PROJECT_SPACE_COMPLETE) || projectState.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD))){
					iterRequest.remove();
				}
				mapRequestInfo.put("CurrentState", mapResourceRequestTranslatedStates.get(strStateValue));
				if(mapRequestInfo.get(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE) != null )
				{
					fte = FTE.getInstance(context, (String)mapRequestInfo.get(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE));
					mapRequestInfo.put("FTE", fte);
				}
				Object objPersonFTEType = null;
				if(mapRequestInfo.get(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST)!=null)
				{
					mapRequestInfo.put("PersonId", mapRequestInfo.get(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST));
				}

				if(mapRequestInfo.get(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST)!=null)
				{
					objPersonFTEType = mapRequestInfo.get(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST_TYPE);
					Map mapPersonsFTEValues = new HashMap(); 
					if (objPersonFTEType instanceof String) {
						String strPersonFTE = (String)mapRequestInfo.get(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);
						Map mapFTEValues = null;

						fte = FTE.getInstance(context, strPersonFTE);

						mapFTEValues = fte.getAllFTE();
						if(null != mapFTEValues){
							for (Iterator iter = mapFTEValues.keySet().iterator(); iter.hasNext();) 
							{
								String strTimeFrame = (String)iter.next();
								Double dFTEValue = 0D;
								if(null!=mapPersonsFTEValues.get(strTimeFrame))
								{
									dFTEValue= (Double)mapPersonsFTEValues.get(strTimeFrame);
								}
								dFTEValue = new Double(dFTEValue.doubleValue()+((Double)mapFTEValues.get(strTimeFrame)).doubleValue());
								mapPersonsFTEValues.put(strTimeFrame,dFTEValue);
							}
						}
					}

					if (objPersonFTEType instanceof StringList) {
						StringList strPersonFTE = (StringList)mapRequestInfo.get(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);

						Map mapFTEValues = null;
						for(int i=0; i<strPersonFTE.size(); i++)
						{
							fte =  FTE.getInstance(context, (String)strPersonFTE.get(i));
							mapFTEValues = fte.getAllFTE();
							if(null != mapFTEValues){
								for (Iterator iter = mapFTEValues.keySet().iterator(); iter.hasNext();) 
								{
									String strTimeFrame = (String)iter.next();
									Double dFTEValue = 0D;
									if(null!=mapPersonsFTEValues.get(strTimeFrame))
									{
										dFTEValue= (Double)mapPersonsFTEValues.get(strTimeFrame);
									}
									dFTEValue = new Double(dFTEValue.doubleValue()+((Double)mapFTEValues.get(strTimeFrame)).doubleValue());
									mapPersonsFTEValues.put(strTimeFrame,dFTEValue);
								}
							}
						}
					}
					mapRequestInfo.put("PersonFTE", mapPersonsFTEValues);
				}
				mapRequestInfo.put("RowEditable","readonly");
			}
			int nMonth  = 0;
			int nYear= 0;
			int nFTESpanInMonths = 0;
			String strFTESpanInMonths = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlanTable.FTESpanInMonths") ;
			Calendar calStartDate = Calendar.getInstance();
			Date dtStartDate = new Date();
			calStartDate.setTime(dtStartDate);
			nMonth = calStartDate.get(Calendar.MONTH)+1; //0=January
			nYear = calStartDate.get(Calendar.YEAR);
			nFTESpanInMonths = Integer.parseInt(strFTESpanInMonths);
			String strFromDate = nMonth +"-" +nYear; 
			programMap.put("strFromDate",strFromDate);
			ResourceRequest.tableValidateCurrency(context,mlResourceRequestsList);				   

			return mlResourceRequestsList;
		} 
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}

	/**
	 * Gets the data for the column "SkillCompetency" for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Vector getColumnSkillCompetencyData(Context context, String[] args) throws Exception
	{
		Map ResourceRequestMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList)ResourceRequestMap.get("objectList");
		Iterator objectListIterator = objectList.iterator();
		Vector requestVector = new Vector();
		Map mapObject = null;
		final String SELECT_PERSON_SKILL = "from[" + RELATIONSHIP_HAS_BUSINESS_SKILL + "].to.name"; 
		Object objSkill = null;
		StringList slSkill = null;
		String strSkill = null;
		StringList slValue = null;
		int nTotalSkills = 0;
		while (objectListIterator.hasNext())
		{
			mapObject = (Map) objectListIterator.next();
			String strObjectId =(String) mapObject.get(SELECT_ID);
			DomainObject dmoObject = DomainObject.newInstance(context,strObjectId);
			if(dmoObject.isKindOf(context, TYPE_PERSON))
			{
				slSkill = dmoObject.getInfoList(context, SELECT_PERSON_SKILL);
			}
			else if (dmoObject.isKindOf(context, TYPE_RESOURCE_REQUEST))
			{
				slSkill = dmoObject.getInfoList(context, SELECT_BUSINESS_SKILL_NAME);
			}
			if (null == slSkill || slSkill.size()<=0)
			{
				requestVector.add(DomainConstants.EMPTY_STRING);
			}
			else  
			{
				requestVector.add(FrameworkUtil.join(slSkill, ","));
			}
		}
		return requestVector ;
	}


	/**
	 * Generates required FTE columns dynamically
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains requestMap 
	 * @return The MapList object containing definitions about new columns for showing FTE
	 * @throws Exception if operation fails
	 */
	public MapList getDynamicFTEColumn (Context context, String[] args) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)mapProgram.get("requestMap");
		MapList mlColumns = new MapList();
		Map mapColumn = null;
		Map mapSettings = null;
		boolean isFilterCost=showCurrencyFilter(context, JPO.packArgs(requestMap));
		String strObjectId = (String) requestMap.get("objectId");
		String strTotal = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.ResourceRequest.ColumnHeader.Total", context.getSession().getLanguage()); 
		DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);
		boolean istypeProjectSpace = false;
		boolean isTypeProjectTemplate = false;
		isTypeProjectTemplate = dmoProject.isKindOf(context, TYPE_PROJECT_TEMPLATE);
		String strFilterValue = (String)requestMap.get("PMCResourceRequestPhaseTimelineFilter");
		if(null==strFilterValue || "".equals(strFilterValue) || "null".equals(strFilterValue))
		{
			strFilterValue = getResourcePlanPreference(context, dmoProject);
		}
		if(!isTypeProjectTemplate && !RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strFilterValue))
		{
			istypeProjectSpace = dmoProject.isKindOf(context, TYPE_PROJECT_SPACE);
			Map mapDate = getMinMaxRequestDates(context,strObjectId);
			Calendar calStartDate = Calendar.getInstance();
			Calendar calFinishDate = Calendar.getInstance();  
			String strMonth = "";
			String strFTESpanInMonths = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.ResourcePlanTable.FTESpanInMonths") ;
			int nFTESpanInMonths = 0;
			FTE fteRequest = FTE.getInstance(context);
			String strTimeframeConfigName = fteRequest.getTimeframeConfigName();
			Date dtStartDate = null;
			Date dtFinishDate = null;
			dtStartDate = (Date)mapDate.get("ReqStartDate");
			dtFinishDate = (Date)mapDate.get("ReqEndDate");

			if(null != dtStartDate && null != dtFinishDate)
			{    		
				if (istypeProjectSpace)
				{
					MapList mlTimeFrames =  fteRequest.getTimeframes(dtStartDate,dtFinishDate);
					int nTimeframe  = 0 ;
					int nYearTimeframe = 0;
					Map mapObjectInfo = null;

					for (Iterator itrTableRows = mlTimeFrames.iterator(); itrTableRows.hasNext();)
					{
						mapObjectInfo = (Map) itrTableRows.next();
						nTimeframe =(Integer)mapObjectInfo.get("timeframe");
						nYearTimeframe =(Integer)mapObjectInfo.get("year");
						mapColumn = new HashMap();
						mapColumn.put("name", nTimeframe+"-"+nYearTimeframe);
						if("Monthly".equals(strTimeframeConfigName)){ 
							strMonth = getMonthName(context,nTimeframe);
							mapColumn.put("label", strMonth+"-"+nYearTimeframe);
						}else if("Weekly".equals(strTimeframeConfigName)){
							mapColumn.put("label", "Wk"+nTimeframe +"-"+nYearTimeframe);
						}else{
							mapColumn.put("label", "Q"+nTimeframe +"-"+nYearTimeframe);  
						}
						mapSettings = new HashMap();
						mapSettings.put("Registered Suite","ProgramCentral");
						mapSettings.put("Validate","validateNumberofPersonSB");
						mapSettings.put("program","emxResourceRequest");
						mapSettings.put("Edit Access Program","emxResourceRequest");
						mapSettings.put("Edit Access Function","isCellsEditable");
						mapSettings.put("function","getColumnFTEData");
						if(isFilterCost)
						{
							mapSettings.put("Style Program","emxResourceRequest");
							mapSettings.put("Style Function","getFTEStyleInfoRightAlign");
						}
						else
						{
							mapSettings.put("Style Program","emxResourceRequest");
							mapSettings.put("Style Function","getFTEStyleInfo");                
						}
						mapSettings.put("format","numeric");
						mapSettings.put("Column Type","program");
						mapSettings.put("Printer Friendly","true");
						if(!isFilterCost)
						{
							mapSettings.put("Editable","true");
						}
						else
						{
							mapSettings.put("Editable","false");
						}
						mapSettings.put("Sortable","false");
						mapSettings.put("Export","true");
						mapSettings.put("Field Type","attribute");
						mapSettings.put("Update Program","emxResourceRequest");
						mapSettings.put("Update Function","updateFTEColumnData");
						mapSettings.put("Group Header",nYearTimeframe+"");
						mapSettings.put("Calculate Sum", "true");
						mapColumn.put("settings", mapSettings);
						mlColumns.add(mapColumn);
					}
					mapColumn = new HashMap();				
					mapColumn.put("name","Total");
					mapColumn.put("label", strTotal);
					mapSettings = new HashMap();
					if(isFilterCost)
					{
						mapSettings.put("Style Program","emxResourceRequest");
						mapSettings.put("Style Function","getFTEStyleInfoRightAlign");
					}
					mapSettings.put("Edit Access Program","emxResourceRequest");
					mapSettings.put("Edit Access Function","isCellsEditable");
					mapSettings.put("Registered Suite","ProgramCentral");
					mapSettings.put("program","emxResourceRequest");
					mapSettings.put("function","getColumnFTEData");
					mapSettings.put("Column Type","program");
					mapSettings.put("Editable","false");
					mapSettings.put("Export","true");
					mapSettings.put("Field Type","attribute");
					mapSettings.put("Sortable","false");
					mapSettings.put("format","numeric");
					mapSettings.put("Calculate Sum", "true");
					mapColumn.put("settings", mapSettings);
					mlColumns.add(mapColumn);
				}
				else
				{
					MapList mlTimeFrames =  fteRequest.getTimeframes(dtStartDate,dtFinishDate);
					int nTimeframe  = 0 ;
					int nYearTimeframe = 0;
					Map mapObjectInfo = null;
					for (Iterator itrTableRows = mlTimeFrames.iterator(); itrTableRows.hasNext();)
					{
						mapObjectInfo = (Map) itrTableRows.next();
						nTimeframe =(Integer)mapObjectInfo.get("timeframe");
						nYearTimeframe =(Integer)mapObjectInfo.get("year");
						strMonth = getMonthName(context,nTimeframe);
						mapColumn = new HashMap();
						mapColumn.put("name", nTimeframe+"-"+nYearTimeframe);
						if("Monthly".equals(strTimeframeConfigName)){ 
							strMonth = getMonthName(context,nTimeframe);
							mapColumn.put("label", strMonth+"-"+nYearTimeframe);
						}else if("Weekly".equals(strTimeframeConfigName)){
							mapColumn.put("label", "Wk"+nTimeframe +"-"+nYearTimeframe);
						}else{
							mapColumn.put("label", "Q"+nTimeframe +"-"+nYearTimeframe);  
						}
						mapSettings = new HashMap();
						mapSettings.put("Registered Suite","ProgramCentral");
						mapSettings.put("Validate","validateNumberofPersonSB");
						mapSettings.put("program","emxResourceRequest");
						mapSettings.put("Edit Access Program","emxResourceRequest");
						mapSettings.put("Edit Access Function","isCellsEditable");
						mapSettings.put("function","getColumnFTEData");
						mapSettings.put("Style Program","emxResourceRequest");
						mapSettings.put("Style Function","getFTEStyleInfo");
						mapSettings.put("Column Type","program");
						mapSettings.put("Printer Friendly","true");
						if(checkResourceManagerAccess(context,args)){
							mapSettings.put("Editable","true");
						}else{
							mapSettings.put("Editable","false");
						}
						mapSettings.put("Export","true");
						mapSettings.put("Field Type","attribute");
						mapSettings.put("Sortable","false");
						mapSettings.put("Update Program","emxResourceRequest");
						mapSettings.put("Update Function","updateFTEColumnData");
						mapSettings.put("Group Header",nYearTimeframe+"");
						mapColumn.put("settings", mapSettings);
						mlColumns.add(mapColumn);
					}
				}
			}
		}
		else
		{
			mlColumns = getDynamicPhaseColumns(context,requestMap,isFilterCost, false,
					strTotal,dmoProject, isTypeProjectTemplate);
		}
		return mlColumns;
	}


	/**
	 * @param context
	 * @param requestMap TODO
	 * @param isFilterCost
	 * @param strTotal
	 * @param dmoProject
	 * @param mlColumns
	 * @throws MatrixException 
	 */
	private MapList getDynamicPhaseColumns(Context context, 
			Map requestMap,boolean isFilterCost, boolean isAddReosurce, String strTotal,DomainObject dmoProject, boolean isTypeProjectTemplate)
	throws MatrixException 
	{
		try {
			MapList mlColumns = new MapList();
			Map mapColumn;
			Map mapSettings;
			Map mapObjectInfo = null;
			StringList slBusSelects = new StringList();
			slBusSelects.add(SELECT_ID);
			slBusSelects.add(SELECT_NAME);
			slBusSelects.add(ResourcePlanTemplate.ATTRIBUTE_PHASE_START_DATE);
			slBusSelects.add(ResourcePlanTemplate.ATTRIBUTE_PHASE_FINISH_DATE);

			String attrTaskWBSId =  "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_TASK_WBS+"]";
			slBusSelects.add(attrTaskWBSId);

			MapList mlPhaseList = dmoProject.getRelatedObjects(context,
					RELATIONSHIP_SUBTASK,
					ProjectSpace.TYPE_PHASE,
					slBusSelects,
					null,       // relationshipSelects
					false,      // getTo
					true,       // getFrom
					(short) 1,  // recurseToLevel
					"",// objectWhere
					null,0);      // relationshipWhere

			mlPhaseList.sortStructure(context, attrTaskWBSId, "ascending", "emxWBSColumnComparator");

			String strTimeZone = (String)requestMap.get("timeZone");
			double clientTimeZone = Task.parseToDouble(strTimeZone);
			for (Iterator itrTableRows = mlPhaseList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				mapColumn = new HashMap();
				String strPhaseId = (String)mapObjectInfo.get(SELECT_ID);
				String strPhaseName = (String)mapObjectInfo.get(SELECT_NAME);
				String strStartDate = (String)mapObjectInfo.get(ResourcePlanTemplate.ATTRIBUTE_PHASE_START_DATE);
				String strFinishDate = (String)mapObjectInfo.get(ResourcePlanTemplate.ATTRIBUTE_PHASE_FINISH_DATE);
				String strstartDate = eMatrixDateFormat.getFormattedDisplayDate(strStartDate,clientTimeZone , context.getLocale());
				String strfinishDate = eMatrixDateFormat.getFormattedDisplayDate(strFinishDate, clientTimeZone, context.getLocale());
				mapColumn.put("isGrid","true");
				mapColumn.put("name", "PhaseOID"+"-"+strPhaseId);
				mapColumn.put("label", strPhaseName);
				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				//mapSettings.put("Validate","validateNumberofPerson");
				mapSettings.put("Edit Access Program","emxResourceRequest");
				mapSettings.put("Edit Access Function","isCellsEditable");
				if(isAddReosurce)
				{
					mapSettings.put("program","emxResourceRequest");
					mapSettings.put("function","getWBSColumnFTEValue");
					mapSettings.put("Column Type","programHTMLOuptut");
				}
				else
				{
					mapSettings.put("Column Type","program");
					mapSettings.put("program","emxResourceRequest");
					mapSettings.put("function","getColumnDataForCostAndFTE");
					if(isTypeProjectTemplate)
					{
						mapSettings.put("Update Program","emxResourceRequest");
						mapSettings.put("Update Function","updateFTEPhaseColumnData");
					}
					else
					{
						mapSettings.put("Update Program","emxResourceRequest");
						mapSettings.put("Update Function","updateFTEColumnData");
					}
					if(isFilterCost)
					{
						mapSettings.put("Style Program","emxResourceRequest");
						mapSettings.put("Style Function","getFTEStyleInfoRightAlign");
					}
					else
					{
						//mapSettings.put("Style Program","emxResourceRequest");
						//mapSettings.put("Style Function","getFTEStyleInfo");
					}
				}
				mapSettings.put("format","numeric");
				mapSettings.put("Printer Friendly","true");
				mapSettings.put("Calculate Sum", "true");
				if(!isFilterCost)
				{
					mapSettings.put("Editable","true");
				}
				else
				{
					mapSettings.put("Editable","false");
				}
				mapSettings.put("Export","true");
				mapSettings.put("Sortable","false");
				mapSettings.put("Width","120");
				if(isTypeProjectTemplate)
				{
					mapSettings.put("Group Header","emxProgramCentral.Type.Phase");
				}
				else
				{
					mapSettings.put("Group Header",strstartDate+"-"+strfinishDate);
				}
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);
			}
			if(!isAddReosurce)
			{
				mapColumn = new HashMap();				
				mapColumn.put("name","Total");
				mapColumn.put("label", strTotal);
				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxResourceRequest");
				if(isTypeProjectTemplate)
				{
					mapSettings.put("function","getColumnDataForCostAndFTE");
				}else
				{
					mapSettings.put("function","getColumnFTEPhaseData");
				}
				if(isFilterCost)
				{
					mapSettings.put("Style Program","emxResourceRequest");
					mapSettings.put("Style Function","getFTEStyleInfoRightAlign");
				}
				mapSettings.put("Column Type","program");
				mapSettings.put("Printer Friendly","true");
				mapSettings.put("Editable","false");
				mapSettings.put("Export","true");
				mapSettings.put("format","numeric");
				mapSettings.put("Calculate Sum", "true");
				mapSettings.put("Sortable","false");
				mapSettings.put("Width","120");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);
			}
			return mlColumns;
		}
		catch (NumberFormatException e)
		{
			throw new MatrixException(e);
		} 
		catch (Exception e) 
		{
			throw new MatrixException(e);
		}
	}

	public StringList getFTEStyleInfo(Context context, String[] args)  throws Exception 
	{
		try 
		{
			StringList slFTEStyles = new StringList();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map columnMap = (Map) programMap.get("columnMap");
			int nMonth = 0;
			int nYear  =0;
			String strColumnName = (String) columnMap.get(SELECT_NAME);
			if (strColumnName.indexOf("-") == -1)
			{
				throw new MatrixException("Invalid FTE column name '"+strColumnName+"'");
			}
			String[] strSplitColumnName = strColumnName.split("-");
			nMonth = Integer.parseInt(strSplitColumnName[0]);
			nYear = Integer.parseInt(strSplitColumnName[1]);
			Map mapObjectInfo = null;
			FTE fte = null;
			double fteValue = 0d;
			NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, false);
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				Double dAllChildFTEValues = 0D;
				String strType = "";
				String strRequestId = (String)mapObjectInfo.get("id");
				Map requestDataMap = null;
				DomainObject dmoRequest = DomainObject.newInstance(context, strRequestId);
				StringList slSelect = new StringList();
				slSelect.add(SELECT_TYPE);
				slSelect.add(SELECT_CURRENT);
				slSelect.add(SELECT_RESOURCE_PLAN_FTE);
				slSelect.add(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST);
				slSelect.add(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);
				slSelect.add(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST_TYPE);
				try{
					ProgramCentralUtil.pushUserContext(context);
				        requestDataMap = dmoRequest.getInfo(context, slSelect);
				}finally{
					ProgramCentralUtil.popUserContext(context);
				}
				if(null!=requestDataMap)
				{
					strType = (String)requestDataMap.get(SELECT_TYPE);
					String strRequestState = (String)requestDataMap.get(SELECT_CURRENT);
					if(null != (requestDataMap.get(SELECT_RESOURCE_PLAN_FTE)) && !"".equals(requestDataMap.get(SELECT_RESOURCE_PLAN_FTE))){
						String strFTE = (String)requestDataMap.get(SELECT_RESOURCE_PLAN_FTE);
						fte = FTE.getInstance(context, strFTE);
						fteValue = fte.getFTE(nYear, nMonth);
					}
					String strFormattedFTE = numberFormat.format(fteValue);
					fteValue = Task.parseToDouble(strFormattedFTE);
					Object objPersonId  = mapObjectInfo.get("PersonId");
					String strPersonId = "";
					StringList slPersonId =new StringList();
					//Modify :IR-179578V6R2013x:start
					if(objPersonId instanceof String){
						strPersonId = (String)mapObjectInfo.get("PersonId");
					}else if (objPersonId instanceof StringList){
						slPersonId = (StringList)mapObjectInfo.get("PersonId");
					}
					getTableResourcePlanUpdatePersonFTEMap(mapObjectInfo); 
					Map mapPersonFTEValues = (Map)mapObjectInfo.get("PersonFTE");//End:IR-179578V6R2013x
					if(null!=mapPersonFTEValues && null!=mapPersonFTEValues.get(nMonth+"-"+nYear))
					{
						dAllChildFTEValues = (Double)mapPersonFTEValues.get(nMonth+"-"+nYear);  
						String strFormattedChildFTE = numberFormat.format(dAllChildFTEValues);
						dAllChildFTEValues =  Task.parseToDouble(strFormattedChildFTE);
					}
					if(TYPE_RESOURCE_REQUEST.equals(strType))
					{
						if(objPersonId == null){
							slFTEStyles.addElement("");
						}
						else
						{
							if(fteValue != dAllChildFTEValues.doubleValue())
							{
								slFTEStyles.addElement("ResourcePlanningRedBackGroundColor");
							}
							else if(fteValue != 0 && dAllChildFTEValues.doubleValue()!= 0 && (STATE_RESOURCE_REQUEST_COMMITTED).equals(strRequestState))
							{
								slFTEStyles.addElement("ResourcePlanningGreenBackGroundColor");
							}
							else if(fteValue != 0 && fteValue == dAllChildFTEValues.doubleValue() && !(STATE_RESOURCE_REQUEST_COMMITTED).equals(strRequestState))
							{
								slFTEStyles.addElement("ResourcePlanningYellowBackGroundColor");
							}
							else if(fteValue == 0 && fteValue == dAllChildFTEValues.doubleValue())
							{
								slFTEStyles.addElement("");
							}else{
								slFTEStyles.addElement("");
							}
						}
					}
					else if(TYPE_PERSON.equals(strType))
						slFTEStyles.addElement("");
					else 
						slFTEStyles.addElement("");
				}
				else
				{
					slFTEStyles.addElement("");
				}
			}
			return slFTEStyles;

		} catch (Exception exp) {
			throw exp;
		}
	}

	public StringList getFTEStyleInfoRightAlign(Context context, String[] args)  throws Exception 
	{
		try 
		{
			StringList slFTEStyles = new StringList();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			Map columnMap = (Map) programMap.get("columnMap");
			for (int i=0; i<objectList.size();i++)
			{
				slFTEStyles.addElement("ColumnRightAllign");
			}
			return slFTEStyles;

		} catch (Exception exp) {
			throw exp;
		}
	}


	/**
	 * Gets the data for the column "FTE" for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Vector getColumnFTEData(Context context, String[] args)  throws Exception {
		try {
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			Map paramList = (Map) programMap.get("paramList");           

			String strCostsFTEFilter =(String) paramList.get("PMCResourceRequestCostsFTEFilter");
			if("Costs".equalsIgnoreCase(strCostsFTEFilter)){
				vecResult=getColumnFTECostValue(context,programMap);
			}
			else{
				vecResult=getColumnOnlyFTEValue(context,programMap);
			}

			return vecResult;
		} catch (Exception exp) {
			throw exp;
		}
	}
	//End:28-May-2010:s4e:R210 PRG:ARP
	/**
	 * Decides if the certain column is to be shown for the Resource Request summary table of Resource Pool
	 * Used for : Column "ProjectOwner", "ProjectName" of "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isResourcePoolRequestSummaryTable (Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strObjId = (String) programMap.get("objectId");
		// [ADDED::PRG:RG6:Jan 4, 2011:IR-077536V6R2012  :R211::Start]
		String isRMB = (String) programMap.get("isRMB");
		if(null != isRMB && "true".equalsIgnoreCase(isRMB.trim())){
			String strRmbTableRowId = (String) programMap.get("rmbTableRowId");
			StringList slRowIds= FrameworkUtil.splitString(strRmbTableRowId,"|");
			if(null != slRowIds && slRowIds.size() >1){
				String strParentId = (String)slRowIds.get(2);
				if(null != strParentId && !"null".equals(strParentId) && !"".equalsIgnoreCase(strParentId.trim())){
					strObjId = strParentId;
				}
			}
		}
		DomainObject dmoObject = DomainObject.newInstance(context, strObjId);
		return dmoObject.isKindOf(context, TYPE_ORGANIZATION);
	}

	/**
	 * Decides if the certain column is to be shown for the Resource Request summary table of Resource Plan
	 * Used for : Column "ProjectRole", "Organization" of "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isResourcePlanRequestSummaryTable (Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strObjId = (String) programMap.get("objectId");

		DomainObject dmoObject = DomainObject.newInstance(context, strObjId);
		return dmoObject.isKindOf(context, TYPE_PROJECT_SPACE);
	}





	/**
	 * In resource requests table, for each expand operation the data of people associated with requests are returned.
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableExpandChildResourceRequestData(Context context,String[] args) throws Exception
	{

		Map programMap = (Map)JPO.unpackArgs(args);
		String strRequestId = (String) programMap.get("objectId");
		String strProjectId = (String) programMap.get("projectID");
		String strParentId = (String) programMap.get("parentId");
		if(null==strParentId)
		{
			strParentId =(String) programMap.get("parentOID");
		}
		String strExpandlevel = (String)programMap.get("expandLevel");
		String strPMCResourceRequestPhaseTimelineFilter = (String) programMap.get("PMCResourceRequestPhaseTimelineFilter");
		MapList mlRequests = new MapList();
		MapList mlReturnList = new MapList();
		MapList mlRequestPersonList = null;
		if(null==strProjectId || "".equals(strProjectId) || "Null".equalsIgnoreCase(strProjectId))
		{
			strProjectId = strParentId;
		}
		programMap.put("projectID", strProjectId);
		String[] arrJPOArguments    = new String[1];
		DomainObject dmoObject = DomainObject.newInstance(context, strRequestId);
		if(dmoObject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)){
			String strResourcePlanPref = getResourcePlanPreference(context, dmoObject);

			if(null!=strResourcePlanPref)
			{
				if(RESOURCE_PLAN_PREFERENCE_TIMELINE.equals(strResourcePlanPref))
				{
					mlRequests = getTableResourcePlanTimelineRequestData(context, args);
				}
				else if(RESOURCE_PLAN_PREFERENCE_PHASE.equals(strResourcePlanPref))
				{
					mlRequests = getTableResourcePlanPhaseRequestData(context, args);
				}
			}
			if("1".equals(strExpandlevel))
				return mlRequests;

			for(int i=0;i<mlRequests.size();i++)
			{
				mlReturnList.add(mlRequests.get(i));
				strRequestId = (String)((Map)mlRequests.get(i)).get(DomainConstants.SELECT_ID);
				programMap.put("objectId",strRequestId);
				arrJPOArguments = JPO.packArgs(programMap);
				mlRequestPersonList = getResourceForExpand(context,arrJPOArguments);
				mlReturnList.addAll(mlRequestPersonList);
			}
			return mlReturnList;
		}
		else {
			arrJPOArguments = JPO.packArgs(programMap);
			return getResourceForExpand(context,arrJPOArguments);
		}
	}

	/**
	 * This method will return the resources connected to resource request
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all resources connected to resource request
	 * @throws Exception if operation fails
	 */
	private MapList getResourceForExpand(Context context,String[] args) throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		String strRequestId = (String) programMap.get("objectId");
		String strProjectId = (String) programMap.get("projectID");
		String strPMCResourceRequestPhaseTimelineFilter = (String) programMap.get("PMCResourceRequestPhaseTimelineFilter");
		String strExpandlevel = (String)programMap.get("expandLevel");

		String strLanguage = context.getSession().getLanguage();//TODO Take from Program Map

		DomainObject dmoRequest = DomainObject.newInstance(context, strRequestId);

		final String SELECT_REL_ATTRIBUTE_RESOURCE_STATE = DomainRelationship.getAttributeSelect(ATTRIBUTE_RESOURCE_STATE);
		final String SELECT_REL_ATTRIBUTE_FTE = DomainRelationship.getAttributeSelect(ATTRIBUTE_FTE);

		final String SELECT_PERSON_SKILL = "from[" + RELATIONSHIP_HAS_BUSINESS_SKILL + "].to.name"; 

		final String SELECT_PERSON_RESOURCE_POOL_TYPE = "to[" + RELATIONSHIP_MEMBER + "].from.type.kindof";
		final String SELECT_PERSON_RESOURCE_POOL_NAME = "to[" + RELATIONSHIP_MEMBER + "].from.name";
		final String SELECT_PERSON_RESOURCE_POOL_ID = "to[" + RELATIONSHIP_MEMBER + "].from.id";

		String strRelationshipPattern = RELATIONSHIP_ALLOCATED;
		String strTypePattern = TYPE_PERSON;

		StringList slBusSelect = new StringList();
		slBusSelect.add(SELECT_ID);
		slBusSelect.add(SELECT_NAME);
		slBusSelect.add(SELECT_PERSON_SKILL);

		slBusSelect.add(SELECT_PERSON_RESOURCE_POOL_TYPE);
		slBusSelect.add(SELECT_PERSON_RESOURCE_POOL_NAME);
		slBusSelect.add(SELECT_PERSON_RESOURCE_POOL_ID);

		StringList slRelSelect = new StringList();
		slRelSelect.add(DomainRelationship.SELECT_ID);
		slRelSelect.add(SELECT_REL_ATTRIBUTE_RESOURCE_STATE);
		slRelSelect.add(SELECT_REL_ATTRIBUTE_FTE);

		boolean getTo = false; 
		boolean getFrom = true; 
		short recurseToLevel = 1;
		String strBusWhere = "";
		String strRelWhere = "";

		MapList mlRequestPersonList = dmoRequest.getRelatedObjects(context,
				strRelationshipPattern, //pattern to match relationships
				strTypePattern, //pattern to match types
				slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
				slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
				getTo, //get To relationships
				getFrom, //get From relationships
				recurseToLevel, //the number of levels to expand, 0 equals expand all.
				strBusWhere, //where clause to apply to objects, can be empty ""
				strRelWhere,0); //where clause to apply to relationship, can be empty ""

		// Fetch translated attribute ranges 
		AttributeType attributeType = new AttributeType(ATTRIBUTE_RESOURCE_STATE);
		attributeType.open(context);
		StringList slResourceStateRanges = attributeType.getChoices(context);
		StringList slResourceStateTranslatedRanges = i18nNow.getAttrRangeI18NStringList(ATTRIBUTE_RESOURCE_STATE, slResourceStateRanges, strLanguage);
		attributeType.close(context);

		Map mapResourceStateTranslatedRanges = new HashMap();
		int nTotalRanges = slResourceStateRanges.size();
		for (int i = 0; i < nTotalRanges; i++) 
		{
			mapResourceStateTranslatedRanges.put(slResourceStateRanges.get(i), slResourceStateTranslatedRanges.get(i));
		}

		Map mapRequestPersonInfo = null;
		String strResourceState = null;
		FTE fte = null;

		Object objResourcePoolType = null;
		Object objResourcePoolName = null;
		Object objResourcePoolID = null;
		String strResourcePoolType = null;
		String strResourcePoolName = null;
		String strResourcePoolID = "";
		StringList slResourcePoolType = null;
		StringList slResourcePoolName = null;
		StringList slResourcePoolID = null;
		StringList slConsolidatedResourcePoolNames = null;
		StringList slConsolidatedResourcePoolIDs = null;
		int nTotalResourcePools = 0;
		String strLevel = "2";
		if("1".equals(strExpandlevel))
			strLevel = strExpandlevel;

		for (Iterator itrRequestPerson = mlRequestPersonList.iterator(); itrRequestPerson.hasNext();)
		{
			mapRequestPersonInfo = (Map) itrRequestPerson.next();
			strResourceState = (String)mapRequestPersonInfo.get(SELECT_REL_ATTRIBUTE_RESOURCE_STATE);
			mapRequestPersonInfo.put("CurrentState", mapResourceStateTranslatedRanges.get(strResourceState));
			fte = FTE.getInstance(context, (String)mapRequestPersonInfo.get(SELECT_REL_ATTRIBUTE_FTE));
			mapRequestPersonInfo.put("FTE", fte);
			if(null!=strProjectId && !"null".equalsIgnoreCase(strProjectId) && !"".equals(strProjectId))
			{
				DomainObject projectDo = DomainObject.newInstance(context,strProjectId);
				String strResourcePlanPrefMode = projectDo.getAttributeValue(context, ResourceRequest.ATTRIBUTE_RESOURCE_PLAN_PREFRENCE);
				getTableResourcePlanUpdateFTEMap(context, strProjectId, strPMCResourceRequestPhaseTimelineFilter, mapRequestPersonInfo, fte, strResourcePlanPrefMode,TYPE_PERSON, strRequestId);
			}
			if(null!=strPMCResourceRequestPhaseTimelineFilter && !"null".equalsIgnoreCase(strPMCResourceRequestPhaseTimelineFilter) && !"".equals(strPMCResourceRequestPhaseTimelineFilter))
			{
				mapRequestPersonInfo.put("ViewBy", strPMCResourceRequestPhaseTimelineFilter);
			}  
			/*level is decided on the basis of ExpandLevel if expand level is 1 then level will be 1 otherwise 2*/
			mapRequestPersonInfo.put("level", strLevel);			
		}
		return mlRequestPersonList;
	}
	/**
	 * Gets the data for the column "State" for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing column state values 
	 * @throws Exception if operation fails
	 */
	public Vector getColumnStateData(Context context, String[] args)throws Exception 
	{
		try 
		{
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			HashMap paramList = (HashMap)programMap.get("paramList");
			String strLanguage = (String)paramList.get("languageStr");
			String strStateName = "";
			StringList slObjectselect = new StringList();
			Map mpPolicyStateValue = null;
			String strState = "";
			String strPolicy = "";
			Map mapRowData = null;
			i18nNow i18nnow = new i18nNow();
			String  strCurrentStateValue = null;
			for (Iterator itrStates = objectList.iterator(); itrStates.hasNext();) 
			{
				mapRowData = (Map) itrStates.next();
				String strObjectId =(String) mapRowData.get(SELECT_ID);
				String strRelationship = (String)mapRowData.get("relationship");
				strCurrentStateValue ="";
				if(ProgramCentralUtil.isNotNullString(strRelationship)&&
						DomainConstants.RELATIONSHIP_ALLOCATED.equalsIgnoreCase(strRelationship))
				{
					String strRelationshipId = (String)mapRowData.get("id[connection]");
					DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strRelationshipId);
					Map mapRelationShip = domainRelationship.getRelationshipData(context, new StringList(DomainRelationship.getAttributeSelect(ATTRIBUTE_RESOURCE_STATE)));
					strCurrentStateValue = (String)((List)mapRelationShip.get(DomainRelationship.getAttributeSelect(ATTRIBUTE_RESOURCE_STATE))).get(0);
					strStateName = i18nnow.getRangeI18NString(ATTRIBUTE_RESOURCE_STATE,strCurrentStateValue, strLanguage);
				}
				else
				{
					DomainObject dmoObject = DomainObject.newInstance(context,strObjectId);
					slObjectselect.add(SELECT_CURRENT);
					slObjectselect.add(SELECT_POLICY);
					mpPolicyStateValue = dmoObject.getInfo(context,slObjectselect);
					strPolicy = (String)mpPolicyStateValue.get(SELECT_POLICY);
					strState = (String)mpPolicyStateValue.get(SELECT_CURRENT);
					strStateName = i18nNow.getStateI18NString(strPolicy,strState, strLanguage);
				}
				vecResult.add(XSSUtil.encodeForXML(context,strStateName));
			}

			return vecResult;
		}
		catch (Exception exp) 
		{
			throw exp;
		}

	}

	/**
	 * This will grant the access to Resource Request for responsible  Resource Manager & Project Lead.
	 * where responsible means to the Resource Manager who owns the Resource Pools child heirarchy & to the project Lead 
	 * who leads/owns the Project & this Resource Request is associated to corresponding Resource Pool or Project space.
	 * 
	 * @param context Matrix Context Object
	 * @param args String array holding the variable at 0 index,
	 *  set from the policy definition state user access.
	 * @return boolean 
	 * @throws Exception if operation fails
	 */
	public boolean hasAccess(Context context, String args[]) throws Exception
	{
		boolean hasAccess = false;
		final short RECURSE_TO_ALL = 0;
		final short RECURSE_TO_ONE = 1;

		if ("RESPONSIBLE_RESOURCE_MANAGER".equalsIgnoreCase(args[0]))
		{
			final String  SELECT_REQUEST_ORGANIZATION_ID = "from["+RELATIONSHIP_RESOURCE_POOL+"].to.id";
			final String  SELECT_REQUEST_ORGANIZATION_NAME = "from["+RELATIONSHIP_RESOURCE_POOL+"].to.name";

			final String  SELECT_PERSON_ORGANIZATION_ID = SELECT_ID;
			final String  SELECT_PERSON_ORGANIZATION_NAME = SELECT_NAME;

			StringList slBusSelect = new StringList();
			StringList slThisResourcePoolsId = new StringList();
			slBusSelect.add(SELECT_REQUEST_ORGANIZATION_ID);
			slBusSelect.add(SELECT_REQUEST_ORGANIZATION_NAME);

			DomainObject dmoResourceRequest = DomainObject.newInstance(context,this.getId());
			// get Resource Pool Info
			Map mapOrganizationInfo =  dmoResourceRequest.getInfo(context, slBusSelect);

			String strRequestOrganizationId = (String) mapOrganizationInfo.get(SELECT_REQUEST_ORGANIZATION_ID);
			String strRequestOrganizationName = (String) mapOrganizationInfo.get(SELECT_REQUEST_ORGANIZATION_NAME);

			// Expand the context user to get its organizations

			String strObjectId = PersonUtil.getPersonObjectID(context);

			DomainObject dmoResourceManager = DomainObject.newInstance(context,strObjectId);
			StringList slBusSelectable = new StringList();
			slBusSelectable.add(SELECT_PERSON_ORGANIZATION_ID);
			slBusSelectable.add(SELECT_PERSON_ORGANIZATION_NAME);

			String  strBusWhere = null;
			String strRelWhere = null;
			StringList slRelSelectable = new StringList();
			boolean getToSide = true;
			boolean getFromSide = false;
			MapList mlOrganizationList = dmoResourceManager.getRelatedObjects( context,
					RELATIONSHIP_RESOURCE_MANAGER,//pattern to match relationships
					TYPE_ORGANIZATION, //pattern to match types
					slBusSelectable, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
					slRelSelectable, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getToSide, //get To relationships
					getFromSide, //get From relationships
					RECURSE_TO_ONE, //the number of levels to expand, 0 equals expand all.
					strBusWhere, //where clause to apply to objects, can be empty ""
					strRelWhere,0); //where clause to apply to relationship, can be empty ""

			StringList slPersonOrganizationIds = new StringList();
			String strPersonOrganizationId =  null;
			String strPersonOrganizationName = null;

			for (Iterator itrOrganization = mlOrganizationList.iterator(); itrOrganization.hasNext();)
			{
				Map mapOrganization = (Map) itrOrganization.next();
				strPersonOrganizationId = (String) mapOrganization.get(SELECT_PERSON_ORGANIZATION_ID);
				strPersonOrganizationName = (String) mapOrganization.get(SELECT_PERSON_ORGANIZATION_NAME);

				if (!slPersonOrganizationIds.contains(strPersonOrganizationId))
				{
					slPersonOrganizationIds.add(strPersonOrganizationId);
				}
			}

			slThisResourcePoolsId.addAll(slPersonOrganizationIds);

			for (int i = 0; i < slPersonOrganizationIds.size(); i++)
			{
				String strResourcePoolId = (String) slPersonOrganizationIds.get(i);
				DomainObject dmoResourcePool = DomainObject.newInstance(context,strResourcePoolId);
				final String SELECT_TO_ID = SELECT_ID;
				final String SELECT_TO_NAME = SELECT_NAME;

				String strTypePattern = TYPE_ORGANIZATION;
				String strRelPattern = RELATIONSHIP_DIVISION + "," + RELATIONSHIP_COMPANY_DEPARTMENT + "," + RELATIONSHIP_ORGANIZATION_PLANT + "," + RELATIONSHIP_SUBSIDIARY;
				StringList slThisRelSelect = new StringList();
				StringList slThisBusSelect = new StringList();

				slThisBusSelect.add(SELECT_TO_ID);
				slThisBusSelect.add(SELECT_TO_NAME);

				MapList mlResourcePoolList = dmoResourcePool.getRelatedObjects( context,
						strRelPattern,//pattern to match relationships
						strTypePattern, //pattern to match types
						slThisBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
						slThisRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
						false, //get To relationships
						true, //get From relationships
						RECURSE_TO_ALL, //the number of levels to expand, 0 equals expand all.
						strBusWhere, //where clause to apply to objects, can be empty ""
						strRelWhere,0); //where clause to apply to relationship, can be empty ""
				String strThisResourcePoolId = "";

				for (Iterator itrResourcePool = mlResourcePoolList.iterator(); itrResourcePool.hasNext();)
				{
					Map mapResourcePool = (Map) itrResourcePool.next();
					strThisResourcePoolId = (String) mapResourcePool.get(SELECT_TO_ID);

					if(strThisResourcePoolId != null && !slThisResourcePoolsId.contains(strThisResourcePoolId))
					{
						slThisResourcePoolsId.add(strThisResourcePoolId);
					}
				}
			}

			if (slThisResourcePoolsId.contains(strRequestOrganizationId))
			{
				hasAccess = true;
			}
		}

		else if("RESPONSIBLE_PROJECT_LEAD".equalsIgnoreCase(args[0]))
		{
			DomainObject dmoResourceRequest = DomainObject.newInstance(context,this.getId());

			final String SELECT_ATTRIBUTE_MEMBER_PROJECT_ACCESS = "attribute["+ATTRIBUTE_PROJECT_ACCESS+"]"; 

			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_NAME);
			slBusSelect.add(SELECT_ID);
			StringList slRelationshipSelect = new StringList();
			String strBusWhere = "";
			String strRelWhere = "";


			MapList mlProjectInfo = dmoResourceRequest.getRelatedObjects(context,
					RELATIONSHIP_RESOURCE_PLAN,//pattern to match relationships
					TYPE_PROJECT_SPACE, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Objects.
					slRelationshipSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					true, //get To relationships
					false, //get From relationships
					RECURSE_TO_ONE, //the number of levels to expand, 0 equals expand all.
					strBusWhere, //where clause to apply to objects, can be empty ""
					strRelWhere,0); //where clause to apply to relationship, can be empty "")


			String strProjectId = "";

			for (Iterator itrProjects = mlProjectInfo.iterator(); itrProjects.hasNext();)
			{
				Map projects = (Map) itrProjects.next();
				strProjectId = (String) projects.get(SELECT_ID);
			}

			DomainObject dmoProjectSpace = DomainObject.newInstance(context,strProjectId);

			String strRelPattern = RELATIONSHIP_MEMBER;
			String strTypePattern = TYPE_PERSON;
			slBusSelect.add(SELECT_ID);

			StringList slRelSelect = new StringList();
			slRelSelect.add(SELECT_ATTRIBUTE_MEMBER_PROJECT_ACCESS);

			boolean getTo = false;
			boolean getFrom = true;
			String  strBusWhere1 = "";

			String strRelationshipWhere = "";

			strRelationshipWhere ="("+SELECT_ATTRIBUTE_MEMBER_PROJECT_ACCESS+"==\"Project Owner\" or "+ SELECT_ATTRIBUTE_MEMBER_PROJECT_ACCESS+"==\"Project Lead\")";

			MapList mlMembersList = dmoProjectSpace.getRelatedObjects( context,
					strRelPattern,//pattern to match relationships
					strTypePattern, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Objects.
					slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getTo, //get To relationships
					getFrom, //get From relationships
					RECURSE_TO_ALL, //the number of levels to expand, 0 equals expand all.
					strBusWhere1, //where clause to apply to objects, can be empty ""
					strRelationshipWhere,0); //where clause to apply to relationship, can be empty ""

			String strProjectLeadName = null;
			StringList slProjectLeadName =  new StringList();

			for (Iterator itrMember = mlMembersList.iterator(); itrMember.hasNext();)
			{
				Map mapPerson = (Map) itrMember.next();
				strProjectLeadName = (String) mapPerson.get(SELECT_NAME);

				if (!slProjectLeadName.contains(strProjectLeadName))
				{
					slProjectLeadName.add(strProjectLeadName);
				}
			}

			if (slProjectLeadName.contains(context.getUser()))
			{
				hasAccess = true;
			}
		}
		return hasAccess;
	}
	/**
	 * This method returns access permissions of Command PMCReuseRejectedRequest depending on the state of request and role of the user.
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains packed Map which has Request Object Id against key "objectId".
	 * @throws Exception If the operation fails.
	 */

	public boolean canReuseRejectedRequest(Context context,String[] args)
	throws Exception
	{
		Map paramMap = (Map)JPO.unpackArgs(args);
		String strRequestID = "";
		strRequestID = (String) paramMap.get("objectId");
		boolean isRequestObject = false;

		String strState = "";
		boolean hasAccess = false;
		try
		{
			DomainObject domainObject = newInstance(context, strRequestID);
			isRequestObject = domainObject.isKindOf(context,DomainConstants.TYPE_RESOURCE_REQUEST);
			if(isRequestObject)
			{
				strState = domainObject.getInfo(context,DomainConstants.SELECT_CURRENT);
				String[] strUSer = new String[]{"RESPONSIBLE_PROJECT_LEAD"};
				this.setId(strRequestID);
				hasAccess =  hasAccess(context,strUSer);
				if (STATE_RESOURCE_REQUEST_REJECTED.equals(strState) && hasAccess == true)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return true;
			}
		}
		catch (FrameworkException Ex)
		{
			throw Ex;
		}
	}


	/**
	 * Gets the data for the column "State" for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing column state values 
	 * @throws Exception if operation fails
	 */
	public Vector getColumnProjectNameData(Context context, String[] args)throws Exception 
	{
		try 
		{
			// Create result vector
			Vector vecResult = new Vector();

			Map programMap = (Map) JPO.unpackArgs(args);
			Map paramList = (Map) programMap.get("paramList");
			MapList objectList = (MapList) programMap.get("objectList");
			boolean isExport = false;
			boolean isPrinterFriendly = false;
			final String SELECT_RESOURCE_REQUEST_PROJECT_NAME = "to["+ RELATIONSHIP_RESOURCE_PLAN +"].from.name";
			final String SELECT_RESOURCE_REQUEST_PROJECT_ID= "to["+ RELATIONSHIP_RESOURCE_PLAN +"].from.id";
			String strReport= (String)paramList.get("reportFormat");    
			String strExport= (String)paramList.get("exportFormat");
			if((ProgramCentralUtil.isNotNullString(strExport) && "CSV".equalsIgnoreCase(strExport)) || 
					(ProgramCentralUtil.isNotNullString(strReport) && "CSV".equalsIgnoreCase(strReport))) 
			{
				isExport = true;
			}
			if((ProgramCentralUtil.isNotNullString(strExport) && "HTML".equalsIgnoreCase(strExport)) || 
					(ProgramCentralUtil.isNotNullString(strReport) && "HTML".equalsIgnoreCase(strReport))) 
			{
				isPrinterFriendly = true;
			}
			
			List<String> projectIds = new ArrayList<String>();
			Map <String,String>accessMap = null;
			for(int i=0;i<objectList.size();i++){
				Map objectMap = (Map)objectList.get(i);
				String projectId = (String) objectMap.get(SELECT_RESOURCE_REQUEST_PROJECT_ID);
				if(projectId != null){
					projectIds.add(projectId);
				}else{
					continue;
				}
			}
			//Get access
			if(projectIds.size() > 0)
			{
				String[] projectIdsArr = projectIds.toArray(new String[projectIds.size()]);
				accessMap = ProgramCentralUtil.hasAccessToViewProject(context, projectIdsArr);
			}
			
			for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();){
				Map mapRowData = (Map) itrObjects.next();
				String strProjectName = (String) mapRowData.get(SELECT_RESOURCE_REQUEST_PROJECT_NAME);
				String strProjectId = (String) mapRowData.get(SELECT_RESOURCE_REQUEST_PROJECT_ID);

				if(ProgramCentralUtil.isNullString(strProjectId)){
					vecResult.addElement(EMPTY_STRING);
				}else{
					String hasAccessToViewProject = accessMap.get(strProjectId);
					String imageStr = "../common/images/iconSmallProject.gif";
					StringBuffer sbProjectLink = new StringBuffer();

					if(isExport){
						sbProjectLink.append(XSSUtil.encodeForURL(context,strProjectName));
					}else if (isPrinterFriendly){
						StringBuffer sbSubstanceLink = new StringBuffer();
						sbProjectLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
						//Added for special character.
						sbProjectLink.append(XSSUtil.encodeForHTML(context, strProjectName));
					}else if(hasAccessToViewProject.equalsIgnoreCase("Yes")){
						sbProjectLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
						sbProjectLink.append("<a href='../common/emxTree.jsp?objectId=").append(strProjectId);
						sbProjectLink.append("' >");
						sbProjectLink.append(XSSUtil.encodeForHTML(context, strProjectName));
						sbProjectLink.append("</a>");
					}else{
						StringBuffer sbSubstanceLink = new StringBuffer();
						sbProjectLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
						sbProjectLink.append(XSSUtil.encodeForHTML(context, strProjectName));
					}
					vecResult.addElement(sbProjectLink.toString());
				}
			}
			return vecResult;
			
		}catch (Exception exp) {
			throw exp;
		}

	}

	/**
	 * Gets the data for the column "State" for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing column state values 
	 * @throws Exception if operation fails
	 */
	public Vector getColumnOrganisationData(Context context, String[] args)throws Exception 
	{
		try 
		{
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map mapRowData = null;
			String  strCurrentStateValue = null;
			Object objOrganization = null;
			Object objOrganizationId = null;
			Object objOrganizationType = null;
			int nTotalResourcePools = 0;
			StringList slOrganizationID = new StringList();
			StringList slOrganizationName = new StringList();
			StringList slOrganizationType = new StringList();
			StringList slOrganizationIsBU = new StringList();
			StringList slOrganizationIsDEP = new StringList();
			StringList slOrganizationIsCOM = new StringList();
			StringList slPersonResourcePools = null;
			for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) 
			{
				slPersonResourcePools = new StringList();
				boolean isBusinessUnit = false;
				boolean isDepartment = false;
				boolean isCompany = false;
				mapRowData = (Map) itrObjects.next();
				String strObjectId =(String) mapRowData.get(SELECT_ID);
				ResourceRequest request = new ResourceRequest(strObjectId);
				Map mapOrgData = request.getOrganizationData(context);
				if(null!=mapOrgData)
				{
					slOrganizationID = (StringList)mapOrgData.get("OrganizationId");
					slOrganizationName = (StringList)mapOrgData.get("OrganizationName");
					slOrganizationType = (StringList)mapOrgData.get("OrganizationType");
					slOrganizationIsBU = (StringList)mapOrgData.get("OrganizationIsBU");
					slOrganizationIsDEP = (StringList)mapOrgData.get("OrganizationIdIsDEP");
					slOrganizationIsCOM = (StringList)mapOrgData.get("OrganizationIdIsCOM");
					nTotalResourcePools = slOrganizationID.size();
					for(int i=0;i<nTotalResourcePools;i++)
					{
						String imageStr = "";
						slPersonResourcePools = new StringList();
						String strOrganizationID =(String)slOrganizationID.get(i);
						String strOrganization = (String)slOrganizationName.get(i);
						isBusinessUnit = "true".equalsIgnoreCase((String)slOrganizationIsBU.get(i))?true:false;
						isDepartment = "true".equalsIgnoreCase((String)slOrganizationIsDEP.get(i))?true:false;
						isCompany = "true".equalsIgnoreCase((String)slOrganizationIsCOM.get(i))?true:false;

						String strType = (String)slOrganizationType.get(i);
						if (!TYPE_PERSON.equals(strType)) {
							if(isBusinessUnit){
								imageStr = "../common/images/iconSmallBusinessUnit.gif";
							}
							if(isDepartment){
								imageStr = "../common/images/iconSmallDeparment.gif";
							}
							if(isCompany){
								imageStr = "../common/images/iconSmallCompany.gif";
							}
						}
						StringBuffer sbSubstanceLink = new StringBuffer();
						sbSubstanceLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
						sbSubstanceLink.append("<a href='../common/emxTree.jsp?objectId=");
						sbSubstanceLink.append(XSSUtil.encodeForURL(context,strOrganizationID));
						sbSubstanceLink.append("' class='object' target='content' >");
						sbSubstanceLink.append(XSSUtil.encodeForHTML(context,strOrganization));
						sbSubstanceLink.append("</a>");
						slPersonResourcePools.add(sbSubstanceLink.toString());
					}
				}
				vecResult.addElement(FrameworkUtil.join(slPersonResourcePools, ",  "));
			}
			return vecResult;
		}
		catch (Exception exp) 
		{
			throw exp;
		}
	}

	/**
	 * Gets resource plan table data for resource request
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableResourcePlanWBSData(Context context, String[] args) throws Exception
	{
		try
		{
			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
					DomainConstants.TYPE_TASK, "PROGRAM");
			Map programMap = (Map) JPO.unpackArgs(args);
			String strProjectId = (String) programMap.get("objectId");

			final String SELECT_REL_ATTRIBUTE_FTE = DomainRelationship.getAttributeSelect(ATTRIBUTE_FTE);
			i18nNow i18n = new i18nNow();
			String strLanguage = (String)programMap.get("languageStr");
			String strRelationshipType = RELATIONSHIP_SUBTASK;
			String strType = TYPE_TASK_MANAGEMENT;

			final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
			final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";

			String ATTRIBUTE_PROJECT_ROLE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_ProjectRole") + "]";
			String strObjWhereClause = DomainConstants.EMPTY_STRING;
			StringList busSelect = new StringList();
			busSelect.add(DomainConstants.SELECT_ID);
			busSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			busSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
			busSelect.add(ATTRIBUTE_PROJECT_ROLE);
			busSelect.add(SELECT_CURRENT);
			busSelect.add(SELECT_POLICY);
			busSelect.add(task.SELECT_HAS_SUBTASK);
			StringList relSelect = new StringList();

			DomainObject domProject = DomainObject.newInstance(context,strProjectId);
			MapList mlTasks = domProject.getRelatedObjects( context,
					strRelationshipType,
					strType,
					busSelect,
					relSelect,
					false,
					true,
					(short)0,
					strObjWhereClause,
					DomainConstants.EMPTY_STRING,0);
			Map mapTaskInfo = null;
			int cntprojLeadComplteTask=0;
			String strObjCurState = null;
			String strProjectRole = null;
			MapList mlTemp = new MapList();
			for(Iterator itrTasks = mlTasks.iterator();itrTasks.hasNext();)
			{
				mapTaskInfo = (Map) itrTasks.next();
				strObjCurState = (String)mapTaskInfo.get(SELECT_CURRENT);
				strProjectRole = (String)mapTaskInfo.get(ATTRIBUTE_PROJECT_ROLE);
				if(STATE_PROJECT_SPACE_COMPLETE.equals(strObjCurState))
				{
					if(ROLE_PROJECT_LEAD.equals(strProjectRole))
					{
						cntprojLeadComplteTask++;
					}
				}
				else
				{
					mlTemp.add(mapTaskInfo);
				}
			}
			mlTasks = mlTemp;
			StringList busOrgSelect = new StringList();
			busOrgSelect.add(SELECT_ORGANIZATION_ID);
			busOrgSelect.add(SELECT_ORGANIZATION_NAME);
			busOrgSelect.add(SELECT_BU_ID);
			busOrgSelect.add(SELECT_BU_NAME);

			Map mapOrg = domProject.getInfo(context,busOrgSelect);

			String strProjectSpaceToTaskRelationship = null;
			String strTaskId = null;
			String strLevel = null;
			String strState = null;
			String strPolicy = null;
			String strHasSubtask=null;
			String strOrganizationId = null;
			String strOrganizationName = null;
			String strBUId = null;
			String strBUName = null;
			String strStartDate = null;
			String strFinishtDate= null;
			boolean isProjectLeadRoleExists = false;
			boolean isProjectRoleAlreadyPresent = false;
			StringList slProjectRoleList = new StringList();
			MapList mlRemoveObjectList = new MapList();
			Map mapRoleFTEInfo = new HashMap();
			for(Iterator itrTasks = mlTasks.iterator();itrTasks.hasNext();)
			{
				mapTaskInfo = (Map) itrTasks.next();
				strProjectRole = (String)mapTaskInfo.get(ATTRIBUTE_PROJECT_ROLE);
				strStartDate = (String) mapTaskInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
				strFinishtDate = (String) mapTaskInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE); 
				strProjectSpaceToTaskRelationship = (String)mapTaskInfo.get("relationship");
				strTaskId = (String)mapTaskInfo.get(SELECT_ID);
				strLevel = (String)mapTaskInfo.get(SELECT_LEVEL);
				strState = (String)mapTaskInfo.get(SELECT_CURRENT);
				strPolicy = (String)mapTaskInfo.get(SELECT_POLICY);
				strHasSubtask = (String)mapTaskInfo.get(task.SELECT_HAS_SUBTASK);
				strOrganizationId = (String)mapOrg.get(SELECT_ORGANIZATION_ID);
				strOrganizationName = (String)mapOrg.get(SELECT_ORGANIZATION_NAME);
				strBUId = (String)mapOrg.get(SELECT_BU_ID);
				strBUName = (String)mapOrg.get(SELECT_BU_NAME);
				Date dtStartDate = eMatrixDateFormat.getJavaDate(strStartDate);
				Date dtFinishDate = eMatrixDateFormat.getJavaDate(strFinishtDate);
				MapList mlMonthYearList = null;   
				FTE fteRequest = FTE.getInstance(context);
				if (strStartDate != null && !"".equals(strStartDate) && !"null".equals(strStartDate)&& strFinishtDate != null && !"".equals(strFinishtDate) && !"null".equals(strFinishtDate))
				{
					mlMonthYearList = fteRequest.getTimeframes(dtStartDate,dtFinishDate);
				}
				ResourceRequest resourceRequest = new ResourceRequest();
				Map mapFTE = resourceRequest.calculateFTE(context, dtStartDate, dtFinishDate, "1");
				isProjectRoleAlreadyPresent =  slProjectRoleList.contains(strProjectRole);
				if(isProjectRoleAlreadyPresent == false)
				{
					slProjectRoleList.add(strProjectRole);
					mapRoleFTEInfo.put(strProjectRole,mapFTE);
				}
				else
				{
					if("False".equalsIgnoreCase(strHasSubtask))
					{
						mlRemoveObjectList.add(mapTaskInfo);
						Map oldMapFTE =(Map)mapRoleFTEInfo.get(strProjectRole);
						for (Iterator iterator = oldMapFTE.keySet().iterator(); iterator.hasNext();) 
						{
							String strMonthYear = (String) iterator.next();
							Double nFTE = (Double)oldMapFTE.get(strMonthYear);
							if(null != (mapFTE.get(strMonthYear)))
							{
								Double iFTE = (Double)mapFTE.get(strMonthYear);
								if(iFTE >0)
								{
									Double inewFTE = iFTE + nFTE;
									mapFTE.put(strMonthYear,inewFTE);
								}
							}
							else
							{
								mapFTE.put(strMonthYear,nFTE);
							}
						}
						mapRoleFTEInfo.put(strProjectRole,mapFTE);
					}
				}
			}
			if (slProjectRoleList.size() != 0) 
			{
				strOrganizationId = (String)mapOrg.get(SELECT_ORGANIZATION_ID);
				strOrganizationName = (String)mapOrg.get(SELECT_ORGANIZATION_NAME);
				strBUId = (String)mapOrg.get(SELECT_BU_ID);
				strBUName = (String)mapOrg.get(SELECT_BU_NAME);
				if(slProjectRoleList.contains("Project Lead"))
				{
					isProjectLeadRoleExists = true;
				}
				if(!isProjectLeadRoleExists && cntprojLeadComplteTask == 0)
				{
					//Create one map consisting of projectlead role
					Map mapProjectRole = new HashMap();
					mapProjectRole.put("level",strLevel);
					mapProjectRole.put("id","dummy");
					mapProjectRole.put("relationship","dummy");
					mapProjectRole.put("attribute[Project Role]","Project Lead");
					mapProjectRole.put("FTEToBeDisplayed","1");
					mapProjectRole.put(SELECT_CURRENT, i18n.getStateI18NString(strPolicy, strState, strLanguage));
					if(null != strBUId && !"".equals(strBUId) && !"".equals(strBUId)){
						mapProjectRole.put("OrganizationID",strBUId);
						mapProjectRole.put("OrganizationName",strBUName);
					}else{
						mapProjectRole.put("OrganizationID",strOrganizationId);
						mapProjectRole.put("OrganizationName",strOrganizationName);
					}
					mlTasks.add(mapProjectRole);
				}
				for(int i=0; i<mlRemoveObjectList.size(); i++)
				{
					mlTasks.remove(mlRemoveObjectList.get(i));
				}
				for(Iterator iterRequest = mlTasks.iterator();iterRequest.hasNext();)
				{
					mapTaskInfo = (Map) iterRequest.next();
					strProjectRole = (String)mapTaskInfo.get("attribute[Project Role]");
					if(ProgramCentralUtil.isNotNullString(strProjectRole))
					{
						Map mapGetFTEValue = (Map) mapRoleFTEInfo.get(strProjectRole);
						if(("Project Lead".equals(strProjectRole))&& !slProjectRoleList.contains("Project Lead"))
						{
							mapTaskInfo.put("FTEToBeDisplayed","1"); 
							if(null != strBUId && !"".equals(strBUId) && !"".equals(strBUId)){
								mapTaskInfo.put("OrganizationID",strBUId);
								mapTaskInfo.put("OrganizationName",strBUName);
							}else{
								mapTaskInfo.put("OrganizationID",strOrganizationId);
								mapTaskInfo.put("OrganizationName",strOrganizationName);
							}
						}
						else
						{
							mapTaskInfo.put("FTEToBeDisplayed",mapGetFTEValue);
							if(null != strBUId && !"".equals(strBUId) && !"".equals(strBUId)){
								mapTaskInfo.put("OrganizationID",strBUId);
								mapTaskInfo.put("OrganizationName",strBUName);
							}else{
								mapTaskInfo.put("OrganizationID",strOrganizationId);
								mapTaskInfo.put("OrganizationName",strOrganizationName);
							}
						}
					}   
				}   
			} 
			else
			{
				strOrganizationId = (String)mapOrg.get(SELECT_ORGANIZATION_ID);
				strOrganizationName = (String)mapOrg.get(SELECT_ORGANIZATION_NAME);
				strBUId = (String)mapOrg.get(SELECT_BU_ID);
				strBUName = (String)mapOrg.get(SELECT_BU_NAME);
				Map mapProjectRole = new HashMap();
				mapProjectRole.put("level","1");
				mapProjectRole.put("id","dummy");
				mapProjectRole.put("relationship","dummy");
				mapProjectRole.put("attribute[Project Role]","Project Lead");
				mapProjectRole.put("FTEToBeDisplayed","1");
				mapProjectRole.put(SELECT_CURRENT, "");
				if(null != strBUId && !"".equals(strBUId) && !"".equals(strBUId)){
					mapProjectRole.put("OrganizationID",strBUId);
					mapProjectRole.put("OrganizationName",strBUName);
				}else{
					mapProjectRole.put("OrganizationID",strOrganizationId);
					mapProjectRole.put("OrganizationName",strOrganizationName);
				}
				mlTasks.add(mapProjectRole);
			} 
			return mlTasks ;
		}
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}        
	/**
	 * Gets project role data for displaying in the CreateResourceRequest from WBS Table
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains requestMap 
	 * @return The Vector containing project role values
	 * @throws Exception if operation fails
	 */

	public Vector getProjectRoleData(Context context, String[] args)throws Exception 
	{
		try 
		{
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramMap=(Map)programMap.get("paramList");
			String strlanguageString = (String)paramMap.get("languageStr");
			Iterator objectListIterator = objectList.iterator();
			Map mapObject = null;
			String strProjectRole = "";
			String strProjectLeadRole = "";
			String sIntProjectRole = "";
			boolean bPresent = false;
			String strLanguage=context.getSession().getLanguage();
			MapList mlRemoveObjectList = new MapList();
			final String strCoreTeamMember = "Core Team Member";
			final String strProgramManager = "Program Manager";
			while (objectListIterator.hasNext())
			{
				mapObject = (Map) objectListIterator.next();
				strProjectRole = (String)mapObject.get("attribute[Project Role]");
				sIntProjectRole = i18nNow.getRoleI18NString(strProjectRole, strlanguageString);
				if(strCoreTeamMember.equalsIgnoreCase(strProjectRole))
				{
					i18nNow loc = new i18nNow();
					String propertyFile = "emxFrameworkStringResource";
					String propertyKey = "emxFramework.Range.Project_Role.Core_Team_Member";
					sIntProjectRole = EnoviaResourceBundle.getProperty(context, "Framework", 
							propertyKey, strlanguageString); 
				}
				if(strProgramManager.equalsIgnoreCase(strProjectRole))
				{
					i18nNow loc = new i18nNow();
					String propertyFile = "emxFrameworkStringResource";
					String propertyKey = "emxFramework.Range.Project_Role.Program_Manager";
					sIntProjectRole = EnoviaResourceBundle.getProperty(context, "Framework", 
							propertyKey, strlanguageString);
				}
				bPresent =  vecResult.contains(sIntProjectRole);
				if((bPresent == false)&& strProjectRole!= null && !"".equals(strProjectRole) && !"null".equals(strProjectRole) )
				{
					vecResult.add(sIntProjectRole);
				}
				else
				{
					mlRemoveObjectList.add(mapObject);
				}
			}
			for(int i=0; i<mlRemoveObjectList.size(); i++)
			{
				objectList.remove(mlRemoveObjectList.get(i));
			}
			return vecResult;
		}
		catch (Exception exp) 
		{
			throw exp;
		}
	}

	/**
	 * Generates required FTE columns dynamically for CreateResourceRequest from WBS
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains requestMap 
	 * @return The MapList object containing definitions about new columns for showing FTE
	 * @throws Exception if operation fails
	 */
	public MapList getWBSDynamicFTEColumn (Context context, String[] args) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)mapProgram.get("requestMap");
		MapList mlColumns = new MapList();
		Map mapColumn = null;
		Map mapSettings = null;
		String strObjectId = (String) requestMap.get("objectId");
		DomainObject dmoProject = DomainObject.newInstance(context, strObjectId);
		String strType = dmoProject.getInfo(context, SELECT_TYPE);
		Calendar calStartDate = Calendar.getInstance();
		Calendar calFinishDate = Calendar.getInstance();  
		int nMonth  = 0;
		int nYear= 0;
		int nFinishMonth = 0;
		int nFinishYear=0;
		int nNumberOfMonths = 0;
		int nFinishWeek = 0;
		int nStartWeek = 0;
		int nNumberOfWeeks = 0;
		int totalnumWeeks = 0;

		final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
		final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";
		FTE fteRequest = FTE.getInstance(context);
		String strTimeframeConfigName = fteRequest.getTimeframeConfigName();
		StringList slBusSelect = new StringList();
		slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

		String strMonth = "";
		String strFTESpanInMonths = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.ResourcePlanTable.FTESpanInMonths") ;
		int nFTESpanInMonths = 0;
		if (dmoProject.isKindOf(context, TYPE_PROJECT_SPACE)) {

			Map mapObjInfo = dmoProject.getInfo(context, slBusSelect);
			String strStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			String strFinishtDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE); 
			Date dtStartDate = eMatrixDateFormat.getJavaDate(strStartDate);
			Date dtFinishDate = eMatrixDateFormat.getJavaDate(strFinishtDate);
			calStartDate.setTime(dtStartDate);
			calFinishDate.setTime(dtFinishDate);
			nMonth = calStartDate.get(Calendar.MONTH)+1; //0=January
			nYear = calStartDate.get(Calendar.YEAR);

			nFinishMonth = calFinishDate.get(Calendar.MONTH)+1;//0=January
			nFinishYear = calFinishDate.get(Calendar.YEAR);

			MapList mlMonthYearList = null;   
			if (strStartDate != null && !"".equals(strStartDate) && !"null".equals(strStartDate)&& strFinishtDate != null && !"".equals(strFinishtDate) && !"null".equals(strFinishtDate))
			{
				mlMonthYearList = fteRequest.getTimeframes(dtStartDate,dtFinishDate);

			}
			StringList slMonthYear = new StringList();
			int iMonthYearSize = mlMonthYearList.size();
			Map mapFTE = new HashMap();
			MapList mlFTE = new MapList();
			Map mapObject = new HashMap();
			Iterator objectListIterator = mlMonthYearList.iterator();
			for (int i =0;i<iMonthYearSize ;i++)
			{
				mapObject = (Map) objectListIterator.next();
				int nTimeFrame = (Integer)mapObject.get("timeframe");
				int nTimeYear = (Integer)mapObject.get("year");

				strMonth = getMonthName(context,nTimeFrame);
				mapColumn = new HashMap();
				mapColumn.put("name", nTimeFrame+"-"+nTimeYear );
				if("Monthly".equals(strTimeframeConfigName)){ 
					strMonth = getMonthName(context,nTimeFrame);
					mapColumn.put("label", strMonth+"-"+nTimeYear);
				}else if("Weekly".equals(strTimeframeConfigName)){
					mapColumn.put("label", "Wk"+nTimeFrame +"-"+nTimeYear);
				}else{
					mapColumn.put("label", "Q"+nTimeFrame +"-"+nTimeYear);  
				}

				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("Validate","validateNumberofPerson");
				mapSettings.put("program","emxResourceRequest");
				mapSettings.put("function","getWBSColumnFTEValue");
				mapSettings.put("Column Type","programHTMLOutput");
				mapSettings.put("submit","true");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);
			}
		}
		else 
		{
			String strFromMonth = (String) requestMap.get("strFromMonth");
			String strFromYear = (String) requestMap.get("strFromYear");
			if (strFromMonth != null && !"".equals(strFromMonth) && !"null".equals(strFromMonth)) {
				nMonth = Integer.parseInt(strFromMonth); //0=January
				nYear = Integer.parseInt(strFromYear);
				nFTESpanInMonths = Integer.parseInt(strFTESpanInMonths);  
			}else{
				Date dtStartDate = new Date();
				calStartDate.setTime(dtStartDate);
				nMonth = calStartDate.get(Calendar.MONTH)+1; //0=January
				nYear = calStartDate.get(Calendar.YEAR);
				nFTESpanInMonths = Integer.parseInt(strFTESpanInMonths);   
			}
			for (int i =  0 ; i < nFTESpanInMonths; i++)
			{
				strMonth = getMonthName(context,nMonth);
				mapColumn = new HashMap();
				mapColumn.put("name", nMonth+"-"+nYear );
				mapColumn.put("label", strMonth+"-"+nYear);
				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("Validate","validateNumberofPerson");
				mapSettings.put("program","emxResourceRequest");
				mapSettings.put("function","getWBSColumnFTEValue");
				mapSettings.put("Column Type","programHTMLOutput");
				mapSettings.put("submit","true");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);
				nMonth++ ;
				if(nMonth > 12)
				{
					nYear++;
					nMonth = 1 ;
				}
			}
		}
		return mlColumns;
	}  

	/**
	 * Gets the data for the column "FTE" for table "PMCResourceRequestCreateWBSTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Vector getWBSColumnFTEData(Context context, String[] args)  throws Exception 
	{
		try 
		{
			// Create result vector
			Vector vecResult = new Vector();

			// Get object list information from packed arguments
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			String strRequestId = (String)paramList.get("requestId");
			//Added:05-Aug-09:yox:R208:PRG:008408
			final String SELECT_ATTRIBUTE_REQUEST_START_DATE = "attribute["
				+ ATTRIBUTE_START_DATE
				+ "]";

			final String SELECT_ATTRIBUTE_REQUEST_END_DATE = "attribute["
				+ ATTRIBUTE_END_DATE
				+ "]";

			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ATTRIBUTE_REQUEST_START_DATE);
			slBusSelect.add(SELECT_ATTRIBUTE_REQUEST_END_DATE);
			DomainObject request = DomainObject.newInstance(context,strRequestId);
			Map mapRequestDetails = request.getInfo(context,slBusSelect);
			String strRequestStartDate = (String)mapRequestDetails.get(SELECT_ATTRIBUTE_REQUEST_START_DATE);
			String strRequestEndDate = (String)mapRequestDetails.get(SELECT_ATTRIBUTE_REQUEST_END_DATE);
			Date dtStartDate = eMatrixDateFormat.getJavaDate(strRequestStartDate);
			Date dtEndDate = eMatrixDateFormat.getJavaDate(strRequestEndDate);
			//End:R208:PRG:008408

			Iterator objectListIterator = objectList.iterator();
			Map mapObject = null;
			String strFTEData = "";
			int nObjectListSize = objectList.size() ; 
			for (int i =0;i<nObjectListSize ;i++)
			{
				mapObject = (Map) objectListIterator.next();
				strFTEData = (String)mapObject.get("FTEToBeDisplayed");
				Map columnMap = (Map) programMap.get("columnMap");
				String strColumnName = (String) columnMap.get(SELECT_NAME);
				if (strColumnName.indexOf("-") == -1)
				{
					throw new MatrixException("Invalid FTE column name '"+strColumnName+"'");
				}
				//Modified:05-Aug-09:yox:R208:PRG:008408
				String[] strSplitColumnName = strColumnName.split("-");
				int nMonth = Integer.parseInt(strSplitColumnName[0]);
				int nYear = Integer.parseInt(strSplitColumnName[1]);

				FTE fte = FTE.getInstance(context);
				Date dateTimelineMin = fte.getStartDate(strColumnName);
				Date dateTimelineMax = fte.getEndDate(strColumnName);

				if(!(dateTimelineMax.after(dtStartDate) && dateTimelineMin.before(dtEndDate))){
					strFTEData = "0";
				}
				//End:R208:PRG:008408
				Map mapObjectInfo = null;
				String strFTEText = "<input type=\"textbox\" name="+strColumnName+i+" value="+strFTEData+">"; 
				String strFTE = strFTEText;
				vecResult.add(strFTE+"");
			}
			return vecResult;
		} 
		catch (Exception exp) 
		{
			throw exp;
		}
	}



	public ResourceRequest createWBSRequest(Context context, String[] args)  throws Exception{
		try{
			ContextUtil.startTransaction(context, true);
			Map programMap	= (Map) JPO.unpackArgs(args);
			String strProjectSpaceId	= (String)programMap.get("projectObjectId");
			String strProjectRole		= (String)programMap.get("ProjectRole");
			String strOrganizationId	= (String)programMap.get("OrganizationID");
			String standardCost 		= (String)programMap.get("standardCost");
			String currencyUnit 		= ProgramCentralUtil.getCurrencyUnit(context);
			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			BigDecimal nStandardCost = ResourceRequest.validateStandardCost(context,
					strLanguage, locale, standardCost);
			MapList mlFTE = (MapList)programMap.get("FTEValue");
			String strResourceRequestId = FrameworkUtil.autoName(context,"type_ResourceRequest","policy_ResourceRequest");
			DomainObject dmoProjectSpace = DomainObject.newInstance(context,strProjectSpaceId);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
			Task task = new Task(strProjectSpaceId);
			StringList strList = new StringList();
			final String SELECT_REL_ATTRIBUTE_FTE = DomainRelationship.getAttributeSelect(ATTRIBUTE_FTE);
			String strRelationshipType = RELATIONSHIP_SUBTASK;
			String strType = TYPE_TASK_MANAGEMENT;
			final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
			final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";

			String SELECT_ATTRIBUTE_PROJECT_ROLE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_ProjectRole") + "]";
			final String ATTRIBUTE_STANDARD_COST = PropertyUtil.getSchemaProperty(context, "attribute_StandardCost");

			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

			Map mapObjInfo = dmoProjectSpace.getInfo(context, slBusSelect);
			String strStartDateTmp = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			String strFinishtDateTmp = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

			StringList busSelect = new StringList();
			busSelect.add(DomainConstants.SELECT_ID);

			busSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			busSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
			busSelect.add(SELECT_ATTRIBUTE_PROJECT_ROLE);

			StringList relSelect = new StringList();

			DomainObject domProject = DomainObject.newInstance(context, strProjectSpaceId);

			MapList mlTasks = domProject.getRelatedObjects( context,
					strRelationshipType,
					strType,
					busSelect,
					relSelect,
					false,
					true,
					(short)0,
					DomainConstants.EMPTY_STRING,
					DomainConstants.EMPTY_STRING,0);

			MapList mapFilteredLst = new MapList();
			String strStartDate = "";
			String strFinishDate = "";
			Date finalStartDate = null;
			Date finalEndDate = null;
			ArrayList arrStartList = new ArrayList();
			ArrayList arrFinishList = new ArrayList();
			MapList mlLisTemp = new MapList();

			String stringProjectRole = "";
			for(int i = 0; i< mlTasks.size();i++){
				Map map = (Map)mlTasks.get(i);
				stringProjectRole = (String)map.get(SELECT_ATTRIBUTE_PROJECT_ROLE);
				if(strProjectRole.equalsIgnoreCase(stringProjectRole) && !stringProjectRole.equals("")){
					mlLisTemp.add(map);
					strStartDate = (String)map.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
					strFinishDate = (String)map.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
					Date start = simpleDateFormat.parse(strStartDate);
					Date finish = simpleDateFormat.parse(strFinishDate);
					if(null == finalStartDate || start.before(finalStartDate)){
						finalStartDate = start;
					}
					if(null == finalEndDate ||finish.after(finalEndDate)){
						finalEndDate = finish;
					}
				}
			}
			int startMonth = 0;
			int startYear = 0; 
			int finishMonth = 0;
			int finishYear = 0; 
			String strStartTempDate = "";
			String strFinishTempDate = "";
			FTE calFte = FTE.getInstance(context); //Added:20-Sep-10:rg6:R210:PRG:IR-072107V6R2011x

			if (null != mlLisTemp && mlLisTemp.size()>0)
			{
				String strFinalStartDateTimeFrame = calFte.getTimeFrame(finalStartDate);
				startMonth  = Integer.parseInt(strFinalStartDateTimeFrame.substring(0,(strFinalStartDateTimeFrame.indexOf("-"))));
				startYear = Integer.parseInt(strFinalStartDateTimeFrame.substring((strFinalStartDateTimeFrame.indexOf("-")+1),strFinalStartDateTimeFrame.length()));

				String strFinalEndDateTimeFrame = calFte.getTimeFrame(finalEndDate);
				finishMonth = Integer.parseInt(strFinalEndDateTimeFrame.substring(0,(strFinalEndDateTimeFrame.indexOf("-"))));
				finishYear = Integer.parseInt(strFinalEndDateTimeFrame.substring((strFinalEndDateTimeFrame.indexOf("-")+1),strFinalEndDateTimeFrame.length()));
				strStartTempDate = simpleDateFormat.format(finalStartDate);
				strFinishTempDate = simpleDateFormat.format(finalEndDate);
			}
			String strRelationship = PropertyUtil.getSchemaProperty(context, "relationship_ResourcePlan");
			FTE fte = FTE.getInstance(context);
			String strFTEValue = "";

			Iterator objectListIterator = mlFTE.iterator();
			Map mapFTE = null;
			String strStartStartDate = "";
			String strLastEndtDate = "";
			boolean setStartDate = false;
			while (objectListIterator.hasNext())
			{
				mapFTE = (Map) objectListIterator.next();
				String strMonthYear = "";
				String[] strMonthYearSpilt = null;
				int nMonth = 0;
				int nYear  = 0;
				double nFTE= 0d;
				for (Iterator iter = mapFTE.keySet().iterator(); iter.hasNext();) 
				{
					strMonthYear = (String) iter.next();
					try{
						nFTE = Task.parseToDouble((String)mapFTE.get(strMonthYear));
						if(nFTE >= 0 && setStartDate == false){
							strStartStartDate = strMonthYear; 
							setStartDate = true;
						}
						if(nFTE >= 0){
							strLastEndtDate = strMonthYear;
						}
					}catch (NumberFormatException e) {
						String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.ResourceRequest.SelectNumericFTE", strLanguage); 
						MqlUtil.mqlCommand(context, "Error " + "\""+sErrMsg+"\"" ); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
						throw new Exception(sErrMsg);
					}
					strMonthYearSpilt = strMonthYear.split("-");
					nMonth = Integer.parseInt(strMonthYearSpilt[0]);
					nYear = Integer.parseInt(strMonthYearSpilt[1]);
					fte.setFTE(nYear, nMonth, nFTE);
				}
			}
			String strStartDateArray [] = strStartStartDate.split("-");
			String strEndDateArray [] = strLastEndtDate.split("-");  
			int nStartMonth = Integer.parseInt(strStartDateArray[0]) ;
			int nStartYear = Integer.parseInt(strStartDateArray[1]);
			int nFinishMonth = Integer.parseInt(strEndDateArray[0]) ;
			int nFinishYear = Integer.parseInt(strEndDateArray[1]);
			Date dtStartDate = calFte.getStartDate(strStartStartDate);
			Date dtFinishDate = calFte.getEndDate(strLastEndtDate);
			String stringStartDate  = simpleDateFormat.format(dtStartDate);
			String stringFinishDate = simpleDateFormat.format(dtFinishDate);
			if ((strProjectRole.equalsIgnoreCase("Project Lead")) && mlLisTemp.size() == 0){
				strStartDate = strStartDateTmp;
				strFinishDate = strFinishtDateTmp;
			}else{
				if((nStartYear == startYear && startMonth == nStartMonth)){
					strStartDate = strStartTempDate;
				}
				else{
					strStartDate = simpleDateFormat.format(dtStartDate);
				}
				if(nFinishYear == finishYear && finishMonth == nFinishMonth){
					strFinishDate = strFinishTempDate;

				}else {
					strFinishDate = simpleDateFormat.format(dtFinishDate);
				}
			}

			String baseCurrency = Currency.getBaseCurrency(context, strProjectSpaceId); 
			double dblStdCost = Currency.toBaseCurrency(context, strProjectSpaceId, nStandardCost.doubleValue(), false);

			Map resAttributes = new HashMap<String, String>();
			resAttributes.put(ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE, strProjectRole);
			resAttributes.put(ProgramCentralConstants.ATTRIBUTE_START_DATE, strStartDate);
			resAttributes.put(ProgramCentralConstants.ATTRIBUTE_END_DATE, strFinishDate);
			resAttributes.put(ATTRIBUTE_STANDARD_COST, dblStdCost + ProgramCentralConstants.SPACE + baseCurrency);

			DomainObject dmoResourceRequest = DomainObject.newInstance(context,	strResourceRequestId);
			dmoResourceRequest.setAttributeValues(context,resAttributes);

			DomainRelationship dmrResourcePlan = DomainRelationship.connect(context,dmoProjectSpace,strRelationship,dmoResourceRequest);  
			strFTEValue = fte.getXML();
			dmrResourcePlan.setAttributeValue(context,ATTRIBUTE_FTE,strFTEValue);

			if(strOrganizationId!= null && !"".equals(strOrganizationId) && !"null".equals(strOrganizationId) )
			{
				DomainObject dmoResourcePool = DomainObject.newInstance(context,
						strOrganizationId);
				String strResourcePoolRel = PropertyUtil.getSchemaProperty(context,
				"relationship_ResourcePool");
				DomainRelationship dmrResourcePool= DomainRelationship.connect(context,dmoResourceRequest,strResourcePoolRel,dmoResourcePool);
			}

			String strSkills = (String)programMap.get("BusinessSkillId");
			if (strSkills != null && !"".equals(strSkills) && !"null".equals(strSkills)) {
				String[]strSkill = null;
				strSkill = strSkills.split(",");

				for(int i=0;i<strSkill.length;i++){
					String strRequestSkill = ""; 
					strRequestSkill = strSkill[i];
					DomainObject dmoSkill = DomainObject.newInstance(context,strRequestSkill);
					String strResourceRequestSkillRel = PropertyUtil.getSchemaProperty(context,
					"relationship_ResourceRequestSkill");
					DomainRelationship dmrResourceRequestSkill= DomainRelationship.connect(context,dmoResourceRequest,strResourceRequestSkillRel,dmoSkill);
				}
			}
			ContextUtil.commitTransaction(context);
			return new ResourceRequest(strResourceRequestId);
		} 
		catch (Exception exp) 
		{
			ContextUtil.abortTransaction(context);
			throw new MatrixException(exp);
		}
	}





	/**
	 * This function notifies the request submission
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id of request
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
	 */
	public void notifySubmitRequest(Context context, String[] args)
	throws Exception
	{
		// get values from args.
		String objectId = args[0];

		DomainObject dmoRequest = DomainObject.newInstance(context,objectId);

		StringList busSelects = new StringList();
		busSelects.add(SELECT_RESOURCE_POOL_ID);
		Map objMap = dmoRequest.getInfo(context, busSelects);
		String strResourcePoolID          = (String) objMap.get(SELECT_RESOURCE_POOL_ID);
		DomainObject dmoResourcePool = DomainObject.newInstance(context,strResourcePoolID);
		String busSelectsManager = SELECT_RESOURCE_MANAGER_NAME;
		StringList slManagers = dmoResourcePool.getInfoList(context, busSelectsManager);
		String strComment = MqlUtil.mqlCommand(context,"get env global SUBMIT_REQUEST"); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011

		StringList mailToList = new StringList(1);
		StringList mailCcList = new StringList(1);

		//      Set the "to" list.
		for(int i =0; i<slManagers.size(); i++){
			mailToList.addElement(slManagers.get(i));
		}

		String sMailSubject = "emxProgramCentral.ResourceRequest.emxSubmitRequestNotifyMembers.Subject";
		String companyName = null;
		sMailSubject  = emxProgramCentralUtilClass.getMessage(
				context, sMailSubject, null, null, companyName);

		//get the mail message
		String sMailMessage = "emxProgramCentral.ResourceRequest.emxSubmitRequestNotifyMembers.Message";

		sMailMessage  = emxProgramCentralUtilClass.getMessage(
				context, sMailMessage, null, null, companyName);

		sMailMessage = sMailMessage + "\n"+ strComment;

		String strPersonName = context.getUser();
		MailUtil.setAgentName(context, strPersonName);

		mailCcList.addElement(strPersonName);

		MailUtil.sendMessage(context, mailToList, mailCcList, null,
				sMailSubject, sMailMessage , null);

	}



	/**
	 * This function notifies the request rejection
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id of request
	 * @throws Exception if operation fails
	 * @since PRG V6R2010x
	 */
	public int notifyRejectRequest(Context context, String[] args) throws Exception {
		String objectId = args[0];
		String strNextStateName = args[1];
		String strtRejectedState = STATE_RESOURCE_REQUEST_REJECTED;
		final String  STR_PROJECT_LEAD_ROLE="Project Lead";
		final String  STR_PROJECT_OWNNER="Project Owner";
		String strComment = MqlUtil.mqlCommand(context,"get env global REJECT_REQUEST"); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011

		Person per = new Person();
		String strPersonName = context.getUser();

		if(strNextStateName.equals(strtRejectedState)){
			final String SELECT_REL_ATTRIBUTE_PROJECT_ROLE = DomainRelationship.getAttributeSelect(ATTRIBUTE_PROJECT_ROLE);
			DomainObject dmoRequest = DomainObject.newInstance(context,	objectId);

			StringList busSelects = new StringList();
			busSelects.add(SELECT_PROJECT_SPACE_ID);

			Map objMap = new HashMap();
			MapList mlManagers = new MapList();
			try{
				ProgramCentralUtil.pushUserContext(context);
				objMap = dmoRequest.getInfo(context, busSelects);

				String strProjectSpaceID          = (String) objMap.get(SELECT_PROJECT_SPACE_ID);
				DomainObject dmoProjectSpace = DomainObject.newInstance(context,strProjectSpaceID);

				String strRelationshipType = RELATIONSHIP_MEMBER;
				String strType = TYPE_PERSON;

				StringList busSelect = new StringList();
				busSelect.add(SELECT_ID);
				busSelect.add(SELECT_NAME);
				busSelect.add(SELECT_OWNER);


				StringList relSelect = new StringList();
				relSelect.add(SELECT_REL_ATTRIBUTE_PROJECT_ROLE);

				mlManagers = dmoProjectSpace.getRelatedObjects(
						context,
						strRelationshipType,
						strType,
						busSelect,
						relSelect,
						false,
						true,
						(short)0,
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING,0);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				ProgramCentralUtil.popUserContext(context);
			}

			StringList mailToList = new StringList();
			StringList mailCcList = new StringList(1);
			Map mapRequestInfo = null;
			String strProjectRoleLead = DomainObject.EMPTY_STRING;
			String strProjectRoleLeadName= DomainObject.EMPTY_STRING;

			for( Iterator iterRequest = mlManagers.iterator();iterRequest.hasNext();){
				mapRequestInfo = (Map) iterRequest.next();
				strProjectRoleLead = (String) mapRequestInfo.get(SELECT_REL_ATTRIBUTE_PROJECT_ROLE);

				String strProjectOwner = (String) mapRequestInfo.get(SELECT_OWNER);
				strProjectRoleLeadName = (String)mapRequestInfo.get(SELECT_NAME);

				if (strProjectRoleLead.equalsIgnoreCase(STR_PROJECT_LEAD_ROLE)) {
					mailToList.addElement(strProjectRoleLeadName);
				}

				if(mailToList.size() == 0){
					if(strPersonName.equals(strProjectOwner)){
						mailToList.addElement(strProjectOwner);
					}else{
						mailToList.addElement(strProjectOwner);
					}
				}
			}

			String sMailSubject = "emxProgramCentral.ResourceRequest.RejectNotification.Subject";
			String companyName = DomainObject.EMPTY_STRING;
			sMailSubject  = emxProgramCentralUtilClass.getMessage(context, sMailSubject, null, null, companyName);

			//get the mail message
			String sMailMessage = "emxProgramCentral.ResourceRequest.RejectNotification.Message";
			sMailMessage  = emxProgramCentralUtilClass.getMessage(context, sMailMessage, null, null, companyName);

			sMailMessage = sMailMessage + "\n"+ strComment;

			sMailMessage  = emxProgramCentralUtilClass.getMessage(context, sMailMessage, null, null, companyName);


			MailUtil.setAgentName(context, strPersonName);

			mailCcList.addElement(strPersonName);

			MailUtil.sendMessage(context, mailToList, mailCcList, null,sMailSubject, sMailMessage , null);
		}
		return TRIGGER_SUCCESS;
	}   

	/*
	 * This method shows the combobox for Project Role Values in the Create New Request Web form
	 */
	public String getProjectRole(Context context,String[]args) throws Exception 
	{
		try 
		{
			AttributeType atrProjectRole = new AttributeType(
					DomainConstants.ATTRIBUTE_PROJECT_ROLE);
			atrProjectRole.open(context);
			StringList strList = atrProjectRole.getChoices(context);
			atrProjectRole.close(context);
			String i18nsProjectRole = null;
			String sProjectRole = null;
			Vector vRole = new Vector();

			StringBuffer output = new StringBuffer();
			output.append("<select name=\"ProjectRole\">");
			String sLanguage = context.getSession().getLanguage();
			StringList slList = i18nNow.getAttrRangeI18NStringList(atrProjectRole.toString(),strList,sLanguage);
			//End:3-Mar-11:s2e:R211:PRG:IR-098670V6R2012
			for (int i = 0; i < slList.size(); i++) {
				i18nsProjectRole = (String) slList.get(i);
				sProjectRole = (String) strList.get(i);
				if (sProjectRole != null && !sProjectRole.startsWith("role_") && i18nsProjectRole != null && !i18nsProjectRole.startsWith("role_")) {
					//Added:17-Mar-2010:di1:R209 PRG:IR-044384
					if(sProjectRole.indexOf('&') > 0 && i18nsProjectRole.indexOf('&') > 0)
					{
						sProjectRole = sProjectRole.replaceAll("&", "&amp;");
						i18nsProjectRole = i18nsProjectRole.replaceAll("&", "&amp;");
					}
					output.append("<option value='"+XSSUtil.encodeForHTML(context,sProjectRole)+"'>"+i18nsProjectRole+"</option>");
				}
			}
			output.append("</select>"); 
			String strOuput = output.toString();
			return strOuput;           
		} 
		catch (Exception exp)
		{
			throw exp;
		}
	}

	/*
	 * This method shows the calendar for request start date in the Create New Request Web form
	 */ 
	public String getRequestStartDate(Context context, String args[]) throws Exception
	{
		final String ROW_NAME = "RequestStartDate";
		return getRequestDateCalendar(context, args, ROW_NAME);
	}


	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @throws FrameworkException
	 */
	private String getRequestDateCalendar(Context context, String[] args, String rowName)
	throws Exception, FrameworkException {
		Map programMap = (Map)JPO.unpackArgs(args);
		Map requestMap = (Map) programMap.get("requestMap");
		String strObjId = "";

		strObjId = (String) requestMap.get("objectId");
		DomainObject dmoProject = DomainObject.newInstance(context, strObjId);
		final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
		final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";
		StringList slBusSelect = new StringList();
		if(rowName.equalsIgnoreCase("RequestStartDate")){
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		}
		else if(rowName.equalsIgnoreCase("RequestEndDate"))
		{
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
		}
		Map mapObjInfo = dmoProject.getInfo(context, slBusSelect);
		String strDate = "";
		if(rowName.equalsIgnoreCase("RequestStartDate")){
			strDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE); 
		}
		else if(rowName.equalsIgnoreCase("RequestEndDate"))
		{
			strDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE); 
		}

		Date dtDate = new Date(strDate);
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat
				.getEMatrixDateFormat(), Locale.US);
		String strNewDate           = sdf.format(dtDate);
		String timezone                = (String) requestMap.get("timeZone");
		double dbTimeZone              = Task.parseToDouble(timezone);
		Locale strLocale = (Locale)requestMap.get("localeObj");        
		strDate     = eMatrixDateFormat.getFormattedDisplayDate(strNewDate, dbTimeZone,strLocale); 


		StringBuffer strHTMLBuffer = new StringBuffer(64);
		strHTMLBuffer.append("<input type='text' readonly='readonly' size='' name='").append(rowName).append("' value=\'"
				+ XSSUtil.encodeForHTML(context,strDate) +"'/>");
		strHTMLBuffer.append("<a href=\"javascript:showCalendar('emxCreateForm', '").append(rowName).append("', '").append(dtDate).append("', '')\">");
		strHTMLBuffer.append("<img src='../common/images/iconSmallCalendar.gif' border='0' valign='absmiddle'/>");
		strHTMLBuffer.append("</a>");

		return strHTMLBuffer.toString();
	}


	/*
	 * This method shows the calendar for request end date in the Create New Request Web form
	 */ 
	public String getRequestEndDate(Context context, String args[]) throws Exception
	{
		final String ROW_NAME = "RequestEndDate";
		return getRequestDateCalendar(context, args, ROW_NAME);
	}


	/**
	 * This function gives the persons which are related to the selected resource pool.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getExcludeOIDforResourcePoolPerson(Context context, String args[]) throws Exception
	{

		MapList mapUserList = null;
		StringList select = new StringList();
		select.add(DomainConstants.SELECT_ID);
		select.add(DomainConstants.SELECT_NAME);
		mapUserList =DomainObject.findObjects(context,PropertyUtil.getSchemaProperty(context,"type_Person"),"*",null,select);

		StringList  slAllUserIds = new StringList();
		StringList  slAllUserNames = new StringList();

		for(int i=0;i<mapUserList.size();i++){

			Map mapUser = (Map) mapUserList.get(i);

			String strUserId = (String) mapUser.get(DomainConstants.SELECT_ID);
			String strUserName= (String) mapUser.get(DomainConstants.SELECT_NAME);

			slAllUserIds.add(strUserId);
			slAllUserNames .add(strUserName);
		}

		StringList slExcludePersonIds = new StringList();
		Map programMap = (Map) JPO.unpackArgs(args);
		String strResourcePoolObjectId = (String) programMap.get("objectId");
		DomainObject domResourcePoolObject = DomainObject.newInstance(context,strResourcePoolObjectId);

		String strTypePattern = DomainConstants.TYPE_PERSON;
		String strRelationshipPattern = DomainConstants.RELATIONSHIP_MEMBER;
		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainObject.SELECT_ID);
		slBusSelect.add(DomainObject.SELECT_NAME);

		StringList slRelSelect = new StringList();
		boolean getTo = false; 
		boolean getFrom = true; 
		short recurseToLevel = 1;
		String strBusWhere = "";
		String strRelWhere = "";

		MapList mlRelatedObjects = domResourcePoolObject.getRelatedObjects(context,
				strRelationshipPattern, //pattern to match relationships
				strTypePattern, //pattern to match types
				slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
				slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
				getTo, //get To relationships
				getFrom, //get From relationships
				recurseToLevel, //the number of levels to expand, 0 equals expand all.
				strBusWhere, //where clause to apply to objects, can be empty ""
				strRelWhere,0); //where clause to apply to relationship, can be empty ""
		Map mapRelatedObjectInfo = null;

		String strPersonId = "";
		String strPersonName = "";
		StringList slPersonToBeAdded = new StringList();

		for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();) 
		{
			mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
			strPersonId = (String)mapRelatedObjectInfo.get(DomainObject.SELECT_ID);
			slPersonToBeAdded.add(strPersonId);
		}

		emxResourcePool_mxJPO emxRPRObj= new emxResourcePool_mxJPO(context,args);
		StringList strPMCUserList= emxRPRObj.getPMCUser(context);

		for(int i=0;i<slPersonToBeAdded.size();i++)
		{
			if(!strPMCUserList.contains(slPersonToBeAdded.get(i)))
			{
				slExcludePersonIds.add(slPersonToBeAdded.get(i));
			}        	
		}
		for(int i=0;i<slAllUserIds.size();i++)
		{
			if(!slPersonToBeAdded.contains(slAllUserIds.get(i)))
			{
				slExcludePersonIds.add(slAllUserIds.get(i));
			}
		}
		return slExcludePersonIds;
	}

	/* This function gives the persons which are related to the selected resource pool.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments
	 * @throws Exception if operation fails
	 */
	public StringList getExcludeOIDforResourcePoolSkill(Context context, String args[]) throws Exception
	{

		MapList mapUserList = null;
		StringList select = new StringList();
		select.add(DomainConstants.SELECT_ID);
		select.add(DomainConstants.SELECT_NAME);
		mapUserList =DomainObject.findObjects(context,PropertyUtil.getSchemaProperty(context,"type_BusinessSkill"),"*",null,select);

		StringList  slAllSkillsIds = new StringList();
		StringList  slAllSkillsNames = new StringList();

		for(int i=0;i<mapUserList.size();i++){

			Map mapSkill = (Map) mapUserList.get(i);

			String strSkillId = (String) mapSkill.get(DomainConstants.SELECT_ID);
			String strSkillName= (String) mapSkill.get(DomainConstants.SELECT_NAME);

			slAllSkillsIds.add(strSkillId);
			slAllSkillsNames .add(strSkillName);
		}

		StringList slSkillIds = new StringList();
		Map programMap = (Map) JPO.unpackArgs(args);
		String strResourcePoolObjectId = (String) programMap.get("objectId");
		DomainObject domResourcePoolObject = DomainObject.newInstance(context, strResourcePoolObjectId);

		String strPoolToHostComRel = RELATIONSHIP_DIVISION + "," + RELATIONSHIP_COMPANY_DEPARTMENT;
		StringList BusSelect = new StringList();
		BusSelect.add(DomainObject.SELECT_ID);
		StringList relSelects = new StringList();
		MapList mlHostCompanyList = ResourcePlan.getLeafNodes(context, strResourcePoolObjectId, strPoolToHostComRel, "to", "end", BusSelect, relSelects);
		String strHostCompanyId = strResourcePoolObjectId; 
		if(null != mlHostCompanyList && mlHostCompanyList.size()>0)
		{
			Map mapHostCompanyId = (Map)mlHostCompanyList.get(0);
			if(null != mlHostCompanyList && mlHostCompanyList.size()>0)
			{
				strHostCompanyId = (String)mapHostCompanyId.get(DomainObject.SELECT_ID);  
			}
		}
		DomainObject domHostCompanyObject = DomainObject.newInstance(context, strHostCompanyId);
		String strTypePattern = DomainConstants.TYPE_BUSINESS_SKILL;
		String strRelationshipPattern = DomainConstants.RELATIONSHIP_ORGANIZATION_SKILL+","+DomainConstants.RELATIONSHIP_SUBSKILL;
		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainObject.SELECT_ID);
		slBusSelect.add(DomainObject.SELECT_NAME);

		StringList slRelSelect = new StringList();

		boolean getTo = false; 
		boolean getFrom = true; 
		short recurseToLevel = 0;
		String strBusWhere = "";
		String strRelWhere = "";

		MapList mlRelatedObjects = domHostCompanyObject.getRelatedObjects(context,
				strRelationshipPattern, //pattern to match relationships
				strTypePattern, //pattern to match types
				slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
				slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
				getTo, //get To relationships
				getFrom, //get From relationships
				recurseToLevel, //the number of levels to expand, 0 equals expand all.
				strBusWhere, //where clause to apply to objects, can be empty ""
				strRelWhere,0); //where clause to apply to relationship, can be empty ""
		Map mapRelatedObjectInfo = null;

		String strSkillId = "";
		String strSkillName = "";
		StringList slSkillToBeAdded = new StringList();

		for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();) 
		{
			mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
			strSkillId = (String)mapRelatedObjectInfo.get(DomainObject.SELECT_ID);
			slSkillToBeAdded.add(strSkillId);
		}
		for(int i=0;i<slAllSkillsIds.size();i++)
		{
			if(!slSkillToBeAdded.contains(slAllSkillsIds.get(i)))
			{
				slSkillIds.add(slAllSkillsIds.get(i));
			}
		}
		return slSkillIds;
	}

	/**
	 * Gets the data for the column icon "Discussion" for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Vector getColumnDiscussionIcon(Context context, String args[]) throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Vector vecResult = new Vector();
		Map paramList = (Map) programMap.get("paramList");
		Map columnMap = (Map) programMap.get("columnMap");
		String strIcon = "";
		Map mapObjectInfo = null;
		boolean isExport = false;
		boolean isPrinterFriendly = false;
		String strReport= (String)paramList.get("reportFormat");    
		String strExport= (String)paramList.get("exportFormat");
		if((ProgramCentralUtil.isNotNullString(strExport) && "CSV".equalsIgnoreCase(strExport)) || 
				(ProgramCentralUtil.isNotNullString(strReport) && "CSV".equalsIgnoreCase(strReport))) 
		{
			isExport = true;
		}
		if((ProgramCentralUtil.isNotNullString(strExport) && "HTML".equalsIgnoreCase(strExport)) || 
				(ProgramCentralUtil.isNotNullString(strReport) && "HTML".equalsIgnoreCase(strReport))) 
		{
			isPrinterFriendly = true;
		}
		String strRequestId = "";
		for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
		{
			mapObjectInfo = (Map) itrTableRows.next();
			strRequestId = XSSUtil.encodeForURL(context,(String)mapObjectInfo.get("id"));
			String strRelId = XSSUtil.encodeForURL(context,(String)mapObjectInfo.get(DomainConstants.SELECT_RELATIONSHIP_ID)); 
			DomainObject dmoRequest = DomainObject.newInstance(context,strRequestId);
			if(dmoRequest.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)){
				vecResult.add("");
			}else{
				StringList busSelects = new StringList();
				busSelects.add(SELECT_THREAD_ID);
				Map objThreadMap =  dmoRequest.getInfo(context,busSelects);
				String strThreadID = (String) objThreadMap.get(SELECT_THREAD_ID);
				StringBuffer strHTMLBuffer = new StringBuffer(64);
				if (ProgramCentralUtil.isNotNullString(strThreadID)) {
					DomainObject dmoThread = DomainObject.newInstance(context,strThreadID);
					StringList busSelectsMessage = new StringList();
					busSelectsMessage.add(SELECT_MESSAGE_ID);
					Map objMessageMap =  dmoThread.getInfo(context,busSelectsMessage);
					String strMessageID = (String) objMessageMap.get(SELECT_MESSAGE_ID);
					if(!"null".equals(strMessageID) && null!= strMessageID && !"".equals(strMessageID)){
						if(isPrinterFriendly)
						{
							strHTMLBuffer.append("<img src=\"../common/images/iconSmallDiscussion.gif\" border=\"0\" valign=\"absmiddle\"></img>");
						}
						else
						{
							strHTMLBuffer.append("<a href='../common/emxTree.jsp?mode=insert&DefaultCategory=APPDiscussionCommand");
							strHTMLBuffer.append("&emxSuiteDirectory=components");
							strHTMLBuffer.append("&relId=").append(strRelId); 
							strHTMLBuffer.append("&parentOID=").append(strRequestId);
							strHTMLBuffer.append("&suiteKey=Components&objectId=").append(strRequestId);
							strHTMLBuffer.append("' class='object' target='content' >");
							strHTMLBuffer.append("<img src=\"../common/images/iconSmallDiscussion.gif\" border=\"0\" valign=\"absmiddle\"></img>");
							strHTMLBuffer.append("</a> ");
						}
					} 
				} 
				StringList slSubscribedEvents = new StringList();
				String strUserName = "";
				Person per = new Person();
				String rpeUserName = (per.getPerson(context)).toString();
				StringList slPersonTokens = FrameworkUtil.split(
						rpeUserName, ".");
				strUserName = (String) slPersonTokens.get(1);
				emxSubscriptionUtil_mxJPO SubUtil = new emxSubscriptionUtil_mxJPO(context,args);
				slSubscribedEvents = SubUtil.getSubscribedEventsAndNotificationTypes(context,strRequestId,strUserName);
				if( slSubscribedEvents.size()>0)
				{
					if(isPrinterFriendly)
					{
						strHTMLBuffer.append("<img src=\"../common/images/iconSmallSubscription.gif\" border=\"0\" valign=\"absmiddle\"></img>");
					}
					else
					{
						strHTMLBuffer.append("<a href=\"javascript:showModalDialog('../components/emxSubscriptionDialog.jsp");
						strHTMLBuffer.append("?emxSuiteDirectory=components");
						// Modified 29-Sep-2010:PRG:RG6:IR-034313V6R2011x
						strHTMLBuffer.append("&relId="+strRelId);  
						//End Modified 29-Sep-2010:PRG:RG6:IR-034313V6R2011x
						strHTMLBuffer.append("&parentOID=");
						strHTMLBuffer.append(strRequestId);
						strHTMLBuffer.append("&suiteKey=Components&objectId=");
						strHTMLBuffer.append(strRequestId);
						strHTMLBuffer.append("', '875', '550', 'false', 'popup')\" >");
						strHTMLBuffer.append("<img src=\"../common/images/iconSmallSubscription.gif\" border=\"0\" valign=\"absmiddle\"></img>");
						strHTMLBuffer.append("</a> ");
					}
				}
				strIcon = strHTMLBuffer.toString();
				String strIconURL = FrameworkUtil.findAndReplace(strIcon,"&","&#38;");
				vecResult.add(strIconURL);
			}
		}
		return vecResult;
	}


	/**
	 * It Will Display people icon if person is attached to resource request     * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */

	public Vector getColumnPersonIcon(Context context, String args[]) throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Vector vecResult = new Vector();
		Map paramList = (Map) programMap.get("paramList");
		Map columnMap = (Map) programMap.get("columnMap");
		String strIcon = "";
		Map mapObjectInfo = null;
		String strRequestId = "";
		String strRelationshipPattern = RELATIONSHIP_ALLOCATED;
		String strTypePattern = TYPE_PERSON;

		StringList slBusSelect = new StringList();
		slBusSelect.add(SELECT_ID);
		slBusSelect.add(SELECT_NAME);

		StringList slRelSelect = new StringList();
		slRelSelect.add(DomainRelationship.SELECT_ID);
		slRelSelect.add(SELECT_REL_ATTRIBUTE_FTE);

		boolean getTo = false; 
		boolean getFrom = true; 
		short recurseToLevel = 1;
		String strBusWhere = "";
		String strRelWhere = "";

		for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
		{
			mapObjectInfo = (Map) itrTableRows.next();
			strRequestId = (String)mapObjectInfo.get("id");
			String strRelId = (String)mapObjectInfo.get(DomainConstants.SELECT_RELATIONSHIP_ID); 
			DomainObject dmoRequest = DomainObject.newInstance(context,strRequestId);
			if(dmoRequest.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE) || dmoRequest.isKindOf(context, DomainConstants.TYPE_PERSON)){
				vecResult.add("");
			}
			else
			{
				MapList mlRequestPersonList = dmoRequest.getRelatedObjects(context,strRelationshipPattern,strTypePattern,slBusSelect,
						slRelSelect,getTo,getFrom,recurseToLevel,strBusWhere,strRelWhere,0); 
				int personInfoSize = mlRequestPersonList.size();
				if(mlRequestPersonList.size()>0)
				{
					StringBuffer strNameBuffer = new StringBuffer(64);
					for (int i=0;i<personInfoSize;i++)
					{
						Map mapRequestPersonInfo = (Map) mlRequestPersonList.get(i);
		
						strNameBuffer.append(XSSUtil.encodeForHTML(context,(PersonUtil.getFullName(context,(String)mapRequestPersonInfo.get(SELECT_NAME)))));
						if(i<personInfoSize-1)
						{
							strNameBuffer.append("; ");
						}
					}
					StringBuffer strHTMLBuffer = new StringBuffer(64);
					strHTMLBuffer.append("<img src=\"../common/images/iconSmallPeople.gif\" border=\"0\" title=\""+strNameBuffer+"\" valign=\"absmiddle\"></img>");
					strIcon = strHTMLBuffer.toString();
				}
				else
				{
					strIcon = "";
				}
				String strIconURL = FrameworkUtil.findAndReplace(strIcon,"&","&#38;");
				vecResult.add(strIconURL);
			}
		}
		return vecResult;
	}

	/**
	 * This method creates the Organization Textbox and chooser for displaying in the Create Resource Request from WBS Table
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */  

	public Vector getOrganizationTextBox(Context context,String[]args) throws Exception 
	{
		try 
		{
			Map programMap = (Map)JPO.unpackArgs(args);

			MapList objectList =(MapList) programMap.get("objectList");
			Vector vProjectRole =new Vector();
			String strProjectrole = "";
			String strProjectLeadRole = "";
			String strOrgName = "";
			String strOrgId = "";
			String strTaskId = "";
			for (int i=0;i<objectList.size();i++)
			{
				Map mapTask = (Map)objectList.get(i);
				strProjectrole = (String)mapTask.get("attribute[Project Role]");
				strOrgName = (String)mapTask.get("OrganizationName");
				strOrgId = (String)mapTask.get("OrganizationID");

				DomainObject dmoResourcePool = DomainObject.newInstance(context,strOrgId);
				String busSelectsManager = SELECT_RESOURCE_MANAGER_NAME;
				StringList slManagers = dmoResourcePool.getInfoList(context, busSelectsManager);
				if(slManagers.size()==0){
					strOrgName = "";
					strOrgId = "";
				}

				if((!vProjectRole.contains(strProjectrole))&& strProjectrole!= null && !"".equals(strProjectrole) && !"null".equals(strProjectrole) ){
					vProjectRole.add(strProjectrole);
				}
			}
			int vProjectroleSize =  vProjectRole.size();
			String strLanguage = context.getSession().getLanguage();
			String strClear = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Clear", strLanguage);
			Vector vOrganisationTextBox = new Vector();
			StringBuffer strBufferOrgText = new StringBuffer();
			for(int j=0;j<vProjectroleSize;j++)
			{ 
				Map mapTask = (Map)objectList.get(j);
				strTaskId = (String)mapTask.get("id");
				String strPRole = (String)vProjectRole.get(j);
				strBufferOrgText = new StringBuffer();
				strBufferOrgText.append("<input type='textbox' readonly='true' value='");
				strBufferOrgText.append(XSSUtil.encodeForHTML(context,strOrgName));
				strBufferOrgText.append("' name='txtOrganization");
				strBufferOrgText.append(j);
				strBufferOrgText.append("' id='txtOrganization");
				strBufferOrgText.append(j);
				strBufferOrgText.append("'></input><input type='hidden' value='");
				strBufferOrgText.append(XSSUtil.encodeForHTML(context,strOrgId));
				strBufferOrgText.append("' name='txtOrganizationId");
				strBufferOrgText.append(j);
				strBufferOrgText.append("' id='txtOrganizationId");
				strBufferOrgText.append(j);
				strBufferOrgText.append("'></input><input type='hidden' name='cntRequest' value='");
				strBufferOrgText.append(vProjectroleSize);
				strBufferOrgText.append("'><input type='hidden' name='txtProjectRole");
				strBufferOrgText.append(j);
				strBufferOrgText.append("' value='");
				strBufferOrgText.append(strPRole);
				strBufferOrgText.append("'></input><input type='hidden' name='Taskobj");
				strBufferOrgText.append(XSSUtil.encodeForHTML(context,strTaskId));
				strBufferOrgText.append("' value='");
				strBufferOrgText.append(j);
				strBufferOrgText.append("'></input><input type='button' name='btnOrganization' size='200' value='...' ");               
				strBufferOrgText.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp");
				strBufferOrgText.append("?field=TYPES=type_Organization");
				strBufferOrgText.append("&amp;table=AEFGeneralSearchResults&amp;cancelLabel=emxProgramCentral.Common.Close&amp;selection=single");
				strBufferOrgText.append("&amp;excludeOIDprogram=emxResourceRequest:getExcludeInActiveResourcePool");
				strBufferOrgText.append("&amp;submitURL=../programcentral/emxProgramCentralResourceRequestAutonomySearchSelect.jsp");
				strBufferOrgText.append("&amp;fieldNameActual=txtOrganizationId"+j+"&amp;fieldNameDisplay=txtOrganization"+j+"&amp;fieldNameOID=txtOrganizationId"+j);
				strBufferOrgText.append("&amp;suiteKey=ProgramCentral&amp;showInitialResults=true");
				strBufferOrgText.append("&amp;checkStoredResult=true&amp;submitURL=AEFSearchUtil.jsp');\">");
				strBufferOrgText.append("</input><a href=\"javascript:basicClear('txtOrganization");
				strBufferOrgText.append(j);
				strBufferOrgText.append("');javascript:basicClear('txtOrganizationId");
				strBufferOrgText.append(j);
				strBufferOrgText.append("')\">");
				strBufferOrgText.append(strClear);
				strBufferOrgText.append("</a>");           
				vOrganisationTextBox.add(strBufferOrgText.toString()); 
			}
			return vOrganisationTextBox;           
		} 
		catch (Exception exp)
		{
			throw exp;
		}
	}

	/**
	 * Sets the state 'Rejected' of Resource Request when the resource manger rejects any request.
	 * 
	 * @param context The Matrix Context object
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public void reject(Context context,String[] args) throws Exception 
	{
		this.reject(context);
	}

	/**
	 * Sets the rejected state to the request when the resource manger rejects any request.
	 * 
	 * @param context The Matrix Context object
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public void reject(Context context) throws Exception 
	{
		String strCurrentState = this.getInfo(context,DomainConstants.SELECT_CURRENT);
		String strNextState = STATE_RESOURCE_REQUEST_REJECTED;

		String strLanguage = context.getSession().getLanguage();

		if ( !(STATE_RESOURCE_REQUEST_REQUESTED.equals(strCurrentState) || STATE_RESOURCE_REQUEST_PROPOSED.equals(strCurrentState)) )
		{
			String strErrorMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ResourceRequest.NotInDesiredState", strLanguage);
			throw new MatrixException(strErrorMessage);
		}
		else
		{
			this.promote(context, strNextState);   
		}
	}

	/**
	 * Signs all the signatures between the current state and  given state of this request
	 * If there are multiple branches from this state and any of the branches is already signed then they are unsigned.
	 * 
	 * @param context The Matrix Context object
	 * @param strNextState The to state
	 * @throws MatrixException 
	 */
	private void promote(Context context, String strNextState) throws MatrixException 
	{
		try
		{
			ContextUtil.startTransaction(context, true);

			StateList stateList = this.getStates(context);
			State currentState = null;

			for (StateItr stateItr = new StateItr(stateList); stateItr.next();){
				State state = stateItr.obj();

				if (state.isCurrent()){
					currentState = state;
					break;
				}
			} 

			if (currentState == null){
				throw new MatrixException("Current state not found");
			}

			StateBranchList currentStateBranchList = currentState.getBranches();

			for (StateBranchItr stateBranchItr = new  StateBranchItr(currentStateBranchList); stateBranchItr.next();){

				StateBranch stateBranch = stateBranchItr.obj();
				SignatureList signatureList = stateBranch.getSignatures();

				for (SignatureItr signatureItr = new SignatureItr(signatureList); signatureItr.next();){

					Signature signature = signatureItr.obj();

					//Obviously it is! :) still do sanity check
					if (signature.isSigned()){ 
						String sCommandStatement = "unsign bus $1 signature $2";
						MqlUtil.mqlCommand(context, sCommandStatement, this.getId(context), signature.getName()); 
					}
				}
			}
			String strComment = DomainObject.EMPTY_STRING;
			String strCurrentState = this.getInfo(context, SELECT_CURRENT);
			SignatureList slSignature =(SignatureList)this.getSignatures(context,strCurrentState,strNextState);

			for( Iterator itr =  slSignature.iterator();itr.hasNext();){
				Signature objSignature = (Signature)itr.next();
				this.approveSignature(context, objSignature, strComment);
			}

			//Promote object to next level
			this.promote(context);

			ContextUtil.commitTransaction(context);

		}catch (MatrixException mex){
			ContextUtil.abortTransaction(context);
			throw new MatrixException(mex);
		}
	}


	/**
	 * Sets the requested state to the request when the Project lead submits the request
	 * 
	 * @param context The Matrix Context object
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public void request(Context context,String[]args) throws Exception 
	{
		String strCurrentState = this.getInfo(context, SELECT_CURRENT);
		if (STATE_RESOURCE_REQUEST_CREATE.equals(strCurrentState)) 
		{ 
			this.promote(context);
		}
		else if (STATE_RESOURCE_REQUEST_REJECTED.equals(strCurrentState))
		{
			this.promote(context, STATE_RESOURCE_REQUEST_REQUESTED);
		}
	}

	/**
	 * This method removes the actual resource of the request and send mail to that resource and resource manager @ removal.
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */  

	public String removeAssignment(Context context,String[]args) throws Exception 
	{
		try 
		{ 
			Map programMap = (Map) JPO.unpackArgs(args);
			String strPersonId = (String)programMap.get("PersonId");
			String strRequestId = (String)programMap.get("RequestId");

			//Sending mail to Resource Manager,Resource which is removed & Project Lead
			DomainObject dmoRequestObject = DomainObject.newInstance(context,strRequestId);
			String strPlanPreference= "";
			String strState = "";
			String strRelationshipPattern = DomainConstants.RELATIONSHIP_ALLOCATED;
			String strTypePattern = DomainConstants.TYPE_PERSON;
			StringList slRelSelect = new StringList();
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_NAME);
			String strBusWhere = "";
			String strRelWhere = "";
			boolean getFrom = true;
			boolean getTo = false;
			short recurseToLevel = 0;
			final String SELECT_REL_ATTRIBUTE_RESOURCE_STATE = DomainRelationship.getAttributeSelect(DomainConstants.ATTRIBUTE_RESOURCE_STATE);
			slRelSelect = new StringList();
			slRelSelect.add(DomainRelationship.SELECT_ID);
			slRelSelect.add(SELECT_REL_ATTRIBUTE_RESOURCE_STATE);
			strRelWhere = "(to.id=="+strPersonId+")";       

			MapList mlResourceRequestsList = dmoRequestObject.getRelatedObjects(context,
					strRelationshipPattern,
					strTypePattern, 
					slBusSelect,
					slRelSelect, 
					getTo,
					getFrom, 
					recurseToLevel,
					strBusWhere, 
					strRelWhere,0);

			slBusSelect.clear();
			slBusSelect.add(SELECT_RESOURCE_POOL_ID);
			slBusSelect.add(SELECT_NAME);
			slBusSelect.add(SELECT_CURRENT);
			slBusSelect.add(SELECT_PROJECT_SPACE_NAME);
			slBusSelect.add(SELECT_REQUEST_RESOURCE_PLAN_PREFRENCE);

			Map mapResoourceRequestData = dmoRequestObject.getInfo(context, slBusSelect);

			Map mapRequestPersonInfo = null;
			String strRelId = "";
			String sErrMsg = "";

			String strRequestState = "";
			String strRequestName 		= "";
			String strResourcePoolID 	= "";
			String strResourceName 		= "";
			String strProjectName		= "";
			for (Iterator itrRequestPerson = mlResourceRequestsList.iterator(); itrRequestPerson.hasNext();)
			{
				mapRequestPersonInfo = (Map) itrRequestPerson.next();
				strState = (String)mapRequestPersonInfo.get(SELECT_REL_ATTRIBUTE_RESOURCE_STATE);
				strRelId = (String)mapRequestPersonInfo.get(DomainRelationship.SELECT_ID);
				strResourceName			= (String) mapRequestPersonInfo.get(SELECT_NAME);
			}

			if(null!=mapResoourceRequestData)
			{
				strResourcePoolID   	= (String) mapResoourceRequestData.get(SELECT_RESOURCE_POOL_ID);
				strRequestState 		= (String) mapResoourceRequestData.get(SELECT_CURRENT);
				strRequestName			= (String) mapResoourceRequestData.get(SELECT_NAME);
				strPlanPreference 		= (String) mapResoourceRequestData.get(SELECT_REQUEST_RESOURCE_PLAN_PREFRENCE);
				strProjectName			= (String) mapResoourceRequestData.get(SELECT_PROJECT_SPACE_NAME);
			}
			if (strRequestState.equals(DomainConstants.STATE_RESOURCE_REQUEST_CREATE) || strRequestState.equals(DomainConstants.STATE_RESOURCE_REQUEST_COMMITTED) || strRequestState.equals(DomainConstants.STATE_RESOURCE_REQUEST_REJECTED))
			{
				DomainObject dmoResourcePool = DomainObject.newInstance(context,
						strResourcePoolID);
				String busSelectsManager = SELECT_RESOURCE_MANAGER_NAME;
				StringList slManagers = dmoResourcePool.getInfoList(context, busSelectsManager);

				StringList mailToList = new StringList();
				StringList mailCcList = new StringList();

				for(int i =0; i<slManagers.size(); i++)
				{
					mailToList.addElement(slManagers.get(i));
				}
				mailToList.addElement(strResourceName);
				String sMailSubject = "emxProgramCentral.ResourceRequest.emxRemoveAssignmentNotify.Subject";
				String companyName = null;
				sMailSubject  = emxProgramCentralUtilClass.getMessage(
						context, sMailSubject, null, null, companyName);

				String sMailMessage = "emxProgramCentral.ResourceRequest.emxRemoveAssignmentNotifyMembers.Message";

				String[] messageValues = new String[2];
				messageValues[0]=strRequestName;
				messageValues[1]=strProjectName;
				sMailMessage  = emxProgramCentralUtilClass.getMessage(
						context, sMailMessage,new String[]{"ResourceRequest","ProjectName"}, messageValues, companyName);

				Person per = new Person();
				String strPersonName = context.getUser();

				MailUtil.setAgentName(context, strPersonName);

				mailCcList.addElement(strPersonName);

				MailUtil.sendMessage(context, mailToList, mailCcList, null,
						sMailSubject, sMailMessage , null);


				if(strPlanPreference.equalsIgnoreCase(RESOURCE_PLAN_PREFERENCE_PHASE))
				{
					StringList slPersonPhaseFTERelList = new StringList();
					DomainObject resourceDo= DomainObject.newInstance(context, strPersonId);
					StringList slPhaseFTERelIdList = dmoRequestObject.getInfoList(context, "from["+ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE+"].id");
					//To get and disconnect all the relationship of type "PersonPhaseFTE" if that person is connected to relationship "PhaseFTE" 
					//Which exist between ResourceRequest and particular Phase
					if(null!=slPhaseFTERelIdList && !slPhaseFTERelIdList.isEmpty())
					{
						for(int i=0;i<slPhaseFTERelIdList.size();i++)
						{ 
							String strPhaseFTERelId= (String)slPhaseFTERelIdList.get(i);   	
							String strPersonPhaseFTERelId = getPersonPhaseFTERelId(
									context, strPersonId, strPhaseFTERelId);
							if(null!=strPersonPhaseFTERelId && !"null".equalsIgnoreCase(strPersonPhaseFTERelId) && !"".equals(strPersonPhaseFTERelId))
							{
								slPersonPhaseFTERelList.add(strPersonPhaseFTERelId);
							}					
						}
					}
					if(null!=slPersonPhaseFTERelList && !slPersonPhaseFTERelList.isEmpty())
					{
						String[] strRelIdsToDisconnect = (String[])slPersonPhaseFTERelList.toArray(new String[slPersonPhaseFTERelList.size()]);
						DomainRelationship.disconnect(context,strRelIdsToDisconnect);
					}

				}
				DomainRelationship.disconnect(context,strRelId);  
			}
			else
			{
				sErrMsg = "Error";

			}
			return sErrMsg; 
		}
		catch (Exception exp)
		{
			throw exp;
		}
	}


	private String getPersonPhaseFTERelId(Context context, String strPersonId,
			String strPhaseFTERelId) throws FrameworkException {
		String sCommandStatement = "print bus $1 select $2 dump";
		String strPersonPhaseFTERelId =  MqlUtil.mqlCommand(context, sCommandStatement,strPersonId, "from["+ResourcePlanTemplate.RELATIONSHIP_PERSON_PHASE_FTE+"|torel.id=="+strPhaseFTERelId+"].id"); 
		return strPersonPhaseFTERelId;
	}

	/**
	 * This method updates the FTE value for ResourceRequest Table 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */  

	public void updateFTEColumnData(Context context,String[]args) throws Exception 
	{
		try 
		{
			String strResourcePlanMode = getResourceRequestPlanMode(context, args);
			if(RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strResourcePlanMode))
			{
				updateFTEColumnDataByPhase(context, args);
			}
			else if(RESOURCE_PLAN_PREFERENCE_TIMELINE.equalsIgnoreCase(strResourcePlanMode))
			{
				updateFTEColumnDataByTimeLine(context, args);
			}

		}
		catch (Exception exp)
		{
			throw exp;
		}
	}

	private String getResourceRequestPlanMode(Context context, String[] args) throws MatrixException 
	{
		boolean isContextPushed = false;
		try 
		{
			Map programMap 		= (Map) JPO.unpackArgs(args);
			Map mpRequestMap    = (Map)programMap.get("requestMap");
			Map mpParamMap      = (Map)programMap.get("paramMap");
			Map mapParam        = new HashMap();
			String strProjectOrOraganizationID  = (String)mpRequestMap.get("objectId");
			mapParam.put("objectId", strProjectOrOraganizationID);
			String[] arrJPOArguments    = new String[1];
			arrJPOArguments = JPO.packArgs(mapParam);
			boolean bisResourcePlanTable = isResourcePlanRequestSummaryTable(context,arrJPOArguments);
			String strRelId     		= (String)mpParamMap.get("relId");
			String strObjectId  		= (String)mpParamMap.get("objectId");
			String strResourcePlanMode 	= "";
			String strProjectId 		= "";
			if(!bisResourcePlanTable)
			{
				String strRequestID     = "" ;
				String strTypePerson    = "" ;
				final String SELECT_REL_ATTRIBUTE_FTE = DomainRelationship.getAttributeSelect(ATTRIBUTE_FTE);
				final String SELECT_REL_ATTRIBUTE_RESOURCE_STATE = DomainRelationship.getAttributeSelect(ATTRIBUTE_RESOURCE_STATE);
				DomainRelationship dmrAllocatedOrResourcePlan = null;

				StringList relSelect = new StringList();
				relSelect.add(DomainRelationship.SELECT_NAME);
				relSelect.add(DomainRelationship.SELECT_FROM_ID);
				relSelect.add(DomainRelationship.SELECT_TO_ID);
				relSelect.add(DomainRelationship.SELECT_TO_TYPE);

				if( strRelId!= null && !"".equals(strRelId) && !"null".equals(strRelId)) 
				{
					dmrAllocatedOrResourcePlan  = DomainRelationship.newInstance(context,strRelId);
					Map mapRelData              = dmrAllocatedOrResourcePlan.getRelationshipData(context,relSelect);
					StringList slRelName        = (StringList)mapRelData.get(DomainRelationship.SELECT_NAME);
					StringList slRelFromId      = (StringList)mapRelData.get(DomainRelationship.SELECT_FROM_ID);
					StringList slRelToType      = (StringList)mapRelData.get(DomainRelationship.SELECT_TO_TYPE);
					StringList slRelToId        = (StringList)mapRelData.get(DomainRelationship.SELECT_TO_ID);
					strTypePerson               = (String)slRelToType.get(0);
					if(TYPE_PERSON.equals(strTypePerson))
					{
						strRequestID = (String)slRelFromId.get(0);
					}
					else
					{
						strRequestID = (String)slRelToId.get(0);  
					}
				}
				else
				{
					strRequestID  =  strObjectId;
				}
				DomainObject reqObject = DomainObject.newInstance(context, strRequestID);
				ProgramCentralUtil.pushUserContext(context);
				isContextPushed = true;
				strProjectId = reqObject.getInfo(context, SELECT_PROJECT_SPACE_ID);
			}
			else
			{
				strProjectId = strProjectOrOraganizationID;
			}
			DomainObject domProjectId = DomainObject.newInstance(context, strProjectId);
			ResourceRequest resourceRequest = new ResourceRequest();
			strResourcePlanMode = getResourcePlanPreference(context, domProjectId);
			return strResourcePlanMode;
		} 
		catch (Exception e) 
		{
			throw new MatrixException(e);
		}
		finally{
			if(isContextPushed){
			ProgramCentralUtil.popUserContext(context);
			}
		}

	}
	/**
	 * This method updates the FTE value for ResourceRequest Table 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */  

	public void updateFTEColumnDataByPhase(Context context,String[]args) throws MatrixException 
	{
		try 
		{
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			Map programMap                      = (Map) JPO.unpackArgs(args);
			Map mpParamMap                      = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (Map)programMap.get("columnMap");
			Map mpRequestMap                    = (Map)programMap.get("requestMap");
			String strPhaseOID                  = (String)mpColumnMap.get("name");
			String strPhaseName             	= (String)mpColumnMap.get("label");
			String strProjectOrOraganizationID  = (String)mpRequestMap.get("objectId");
			String strFTENewValue               = "";
			String strLanguage 					= context.getSession().getLanguage();
			String[] arrJPOArguments            = new String[1];
			String[] strPhaseOIDSpilt           = null;
			double nNewFTEValue                 = 0;
			boolean bisResourcePlanTable        = false;
			FTE fte                             = null;
			String strPhaseId 					= "";
			Map mapParam                        = new HashMap();
			mapParam.put("objectId", strProjectOrOraganizationID);
			arrJPOArguments = JPO.packArgs(mapParam);
			bisResourcePlanTable = isResourcePlanRequestSummaryTable(context,arrJPOArguments);
			try
			{
				nNewFTEValue = Task.parseToDouble((String)mpParamMap.get("New Value"));
			}
			catch (NumberFormatException e) {
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ResourceRequest.SelectFTE", strLanguage);
				throw new MatrixException(sErrMsg + " " + strPhaseName);
			}
			String strRelId     = (String)mpParamMap.get("relId");
			String strObjectId  = (String)mpParamMap.get("objectId");
			strPhaseOIDSpilt   	= strPhaseOID.split("-");
			strPhaseId          = strPhaseOIDSpilt[1];
			MapList mlPersonInfo        = null;
			MapList mlRequestsInfo      = null;
			String strFTE               = "";
			String strResourceState     = "";
			String strRequestState     = "";
			String strRequestID         = "" ;
			String strTypePerson        = "" ;
			String strOldAllocatedFTEValue="";
			DomainRelationship dmrAllocatedOrResourcePlan = null;

			StringList relSelect = new StringList();
			relSelect.add(DomainRelationship.SELECT_NAME);
			relSelect.add(DomainRelationship.SELECT_FROM_ID);
			relSelect.add(DomainRelationship.SELECT_TO_ID);
			relSelect.add(DomainRelationship.SELECT_TO_TYPE);
			relSelect.add(SELECT_ATTRIBUTE_FTE);

			if( strRelId!= null && !"".equals(strRelId) && !"null".equals(strRelId)) {
				dmrAllocatedOrResourcePlan = DomainRelationship.newInstance(context,
						strRelId);

				Map mapRelData              = dmrAllocatedOrResourcePlan.getRelationshipData(context,relSelect);
				StringList slRelName        = (StringList)mapRelData.get(DomainRelationship.SELECT_NAME);
				StringList slRelFromId      = (StringList)mapRelData.get(DomainRelationship.SELECT_FROM_ID);
				StringList slRelToType      = (StringList)mapRelData.get(DomainRelationship.SELECT_TO_TYPE);
				StringList slRelToId        = (StringList)mapRelData.get(DomainRelationship.SELECT_TO_ID);
				strTypePerson               = (String)slRelToType.get(0);
				StringList slAllocatedFTE      = (StringList)mapRelData.get(SELECT_ATTRIBUTE_FTE);

				String strFTEValue = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.ResourceRequest.FTE") ;
				int nFTEValidateValue = Integer.parseInt(strFTEValue); 

				if(TYPE_PERSON.equals(strTypePerson) && !"Hours".equalsIgnoreCase(numberofPeopleUnit))
				{
					if(nNewFTEValue >nFTEValidateValue || nNewFTEValue<0){
						String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.ResourceRequest.InvalidFTE", strLanguage);
						throw new MatrixException(sErrMsg);
					}
				}
				if(TYPE_PERSON.equals(strTypePerson)){
					strRequestID         =(String)slRelFromId.get(0);
					strOldAllocatedFTEValue =(String)slAllocatedFTE.get(0);
				}else{
					strRequestID         =(String)slRelToId.get(0);  
				}
			}else{
				strRequestID  =  strObjectId;
			}
			DomainObject dmoRequest     = newInstance(context,strRequestID);
			StringList slSelectList = new StringList();
			slSelectList.add(SELECT_ID);
			slSelectList.add(SELECT_TYPE);
			slSelectList.add(SELECT_CURRENT);
			slSelectList.add(SELECT_ATTRIBUTE_START_DATE);
			slSelectList.add(SELECT_ATTRIBUTE_END_DATE);
			slSelectList.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
			slSelectList.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
			slSelectList.add(SELECT_RESOURCE_REQUEST_PHASE_RELATIONSHIP_ID);
			if(TYPE_PERSON.equals(strTypePerson))
			{
				slSelectList.add(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST);
				slSelectList.add(SELECT_PERSON_STATE_FROM_RESOURCE_REQUEST+".value");
				slSelectList.add(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST+".value");
			}
			else
			{
				slSelectList.add(SELECT_RESOURCE_PLAN_FTE);
			}
			BusinessObjectWithSelectList businessObjectWithSelectList = null;
			String[] strResourceRequestIds = new String[]{strRequestID};
			BusinessObjectWithSelectList resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strResourceRequestIds,slSelectList);
			Map mapRequestRelResourcePlanData = new HashMap();
			String strResourceRequestStartDate = "";
			String strResourceRequestEndDate = "";
			String strResourceRequestId = "";
			StringList slPhaseIdList = new StringList();
			StringList slPhaseFTEList = new StringList();
			StringList slPersonIdList = new StringList();
			StringList slPersonFTEList = new StringList();
			StringList slPersonStateList = new StringList();
			String strResourceReqFTE = "";
			String strResourceAllocatedFTE = "";
			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
			{
				BusinessObjectWithSelect bows = itr.obj();
				strResourceRequestId = bows.getSelectData(SELECT_ID);
				strRequestState = bows.getSelectData(SELECT_CURRENT);
				if(TYPE_PERSON.equals(strTypePerson))
				{
					slPersonIdList = bows.getSelectDataList(SELECT_PERSON_ID_FROM_RESOURCE_REQUEST);
					slPersonStateList = bows.getSelectDataList(SELECT_PERSON_STATE_FROM_RESOURCE_REQUEST+".value");
					slPersonFTEList = bows.getSelectDataList(SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST+".value");
					for (int i = 0; i < slPersonIdList.size(); i++) 
					{
						String strPersonObjId = (String)slPersonIdList.get(i);
						if(strObjectId.equals(strPersonObjId))
						{
							strResourceAllocatedFTE = (String)slPersonFTEList.get(i);
							strResourceState = (String)slPersonStateList.get(i);
							break;
						}
					}
				}
				else
				{
					strResourceReqFTE = bows.getSelectData(SELECT_RESOURCE_PLAN_FTE);
				}
			}
			if (strRequestState !=null)
			{
				if((bisResourcePlanTable == true && strRequestState.equals("Create") && !TYPE_PERSON.equals(strTypePerson)) 
						|| (bisResourcePlanTable == true && strRequestState.equals("Create") && strResourceState.equals("Requested") && TYPE_PERSON.equals(strTypePerson))
						|| (bisResourcePlanTable == true && strRequestState.equals("Rejected"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Requested") && strResourceState.equals("Requested"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Requested") && strResourceState.equals("Proposed"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Proposed") && strResourceState.equals("Requested"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Proposed") && strResourceState.equals("Proposed"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Committed") && strResourceState.equals("Committed")))
				{
					if(TYPE_PERSON.equals(strTypePerson))
					{
						if(bisResourcePlanTable == true )
						{
							newUpdateFTEPersonPhaseColumnData(context,resourceRequestObjWithSelectList,strRelId, strObjectId,strPhaseId, strResourceAllocatedFTE, nNewFTEValue);
						}
						else{
							String strTimeFrame = strPhaseOID;
							updatePhaseFTEFromResourcePool(context,strRequestID,strRelId,strObjectId, strOldAllocatedFTEValue,strTimeFrame,nNewFTEValue,numberofPeopleUnit);
						}
					}
					else
					{
						newUpdateFTEPhaseColumnData(context,resourceRequestObjWithSelectList,strRelId,strPhaseId, nNewFTEValue);
					}
				}
				else if((bisResourcePlanTable == true && strRequestState.equals("Requested"))
						|| (bisResourcePlanTable == true && strRequestState.equals("Proposed"))
						|| (bisResourcePlanTable == true && strRequestState.equals("Committed")))

				{
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.ResourceRequest.AccessFTEEdit", strLanguage);
					throw new Exception(sErrMsg);
				}
				else{
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.ResourceRequest.AccessFTEEdit", strLanguage);
					throw new Exception(sErrMsg);
				}
			}
		}
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
	}

	//Added:26-Aug-2010:s4e:R210 PRG:ARP
	//Added to connect Person To relationship "PhaseFTE" by relationship "PersonPhaseFTE" when ResourcePlanPreference="Phase"
	//FTE is also divided according to Timeframes for Phase and for Request by Hours by Hours basis
	/**
	 * updatePhaseFTEFromResourcePool  when ResourcePlanPrefernce is "Phase".
	 * 
	 * @param context The Matrix Context object
	 * @param strRequestId Resource Request Id
	 * @param strRelId Request-Resource relationship Id
	 * @param strResourceId Request Resource Id
	 * @param strOldAllocatedFTEValue FTE value for relationship "Allocated"
	 * @param strTimeFrame Timeframe column for updation 
	 * @param nNewFTEValue FTE for timeframe
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public void updatePhaseFTEFromResourcePool(Context context,String strRequestID,String strRelId,String strResourceId,String strOldAllocatedFTEValue,String strTimeFrame,double nNewFTEValue,String numberofPeopleUnit) throws MatrixException 
	{
		boolean flag = false;    	
		try 
		{    			
			double nNewTimelineFTEValue         = 0;
			String strLanguage 					= context.getSession().getLanguage();   				
			String[] strTimeFrameSpilt           = null;
			String strMonth="";
			String strYear="";
			String strTypePerson="";
			String strPhaseId="";
			String strFTEValue = "";
			double nOldAllocatedTimeFrameFTE=0d;
			Double nNewPersonPhaseFTEValue=0d;
			StringList slTimeFramePhaseList = new StringList();
			strTimeFrameSpilt   	= strTimeFrame.split("-");
			int nMonth = Integer.parseInt(strTimeFrameSpilt[0]);
			int nYear = Integer.parseInt(strTimeFrameSpilt[1]);
			FTE fte = FTE.getInstance(context);
			FTE allocatedFTE = FTE.getInstance(context);

			allocatedFTE = FTE.getInstance(context, strOldAllocatedFTEValue);
			nOldAllocatedTimeFrameFTE=allocatedFTE.getFTE(nYear, nMonth);

			DomainObject dmoRequest= DomainObject.newInstance(context,strRequestID);

			StringList slRequestPhaseIdList = dmoRequest.getInfoList(context, ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
			StringList slRequestPhaseFTEIdRelList = dmoRequest.getInfoList(context, "from["+ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE+"].id");
			for(int nCount=0;nCount<slRequestPhaseIdList.size();nCount++)
			{
				strPhaseId= (String)slRequestPhaseIdList.get(nCount);
				DomainObject phaseDo = DomainObject.newInstance(context, strPhaseId);
				String strPhaseStartDate = phaseDo.getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE);
				String strPhaseFinishDate = phaseDo.getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
				Date fromDate=eMatrixDateFormat.getJavaDate(strPhaseStartDate);
				Date toDate=eMatrixDateFormat.getJavaDate(strPhaseFinishDate);
				MapList mlTimeFrames = fte.getTimeframes(fromDate, toDate);
				for(int nIndex=0;nIndex<mlTimeFrames.size();nIndex++)
				{
					Map mapTimeFrame = (Map)mlTimeFrames.get(nIndex);
					int nPhaseTimeframeMonth = (Integer)mapTimeFrame.get("timeframe");
					int nPhaseTimeframeYear  = (Integer)mapTimeFrame.get("year");
					if(nMonth==nPhaseTimeframeMonth && nYear==nPhaseTimeframeYear)
					{
						slTimeFramePhaseList.add(strPhaseId);
					}
				}
			}
			if( strRelId!= null && !"".equals(strRelId) && !"null".equals(strRelId)) 
			{
				DomainRelationship dmrAllocated =DomainRelationship.newInstance(context,strRelId);
				allocatedFTE.setFTE(nYear, nMonth, nNewFTEValue);
				strFTEValue = allocatedFTE.getXML();
				dmrAllocated.setAttributeValue(context, ATTRIBUTE_FTE,strFTEValue);
			}  
			for(int i=0;i<slTimeFramePhaseList.size();i++)
			{
				String strTimeFramePhaseId= (String)slTimeFramePhaseList.get(i);
				DomainObject phaseDo = DomainObject.newInstance(context, strTimeFramePhaseId);
				String strPhaseStartDate = phaseDo.getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE);
				String strPhaseFinishDate = phaseDo.getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
				Date fromDate=eMatrixDateFormat.getJavaDate(strPhaseStartDate);
				Date toDate=eMatrixDateFormat.getJavaDate(strPhaseFinishDate);
				MapList mlTimeFrames = fte.getTimeframes(fromDate, toDate);
				int nMlTimeFramesSize =mlTimeFrames.size();
				int nIndex = slRequestPhaseIdList.indexOf(strTimeFramePhaseId);
				String strPhaseFTERelId = (String)slRequestPhaseFTEIdRelList.get(nIndex);

				String strPersonPhaseFTERelId = getPersonPhaseFTERelId(context,
						strResourceId, strPhaseFTERelId);

				if(null!=strPersonPhaseFTERelId && !"null".equalsIgnoreCase(strPersonPhaseFTERelId)&&!"".equals(strPersonPhaseFTERelId)){
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					flag = true;       									
					DomainRelationship personPhaseFTERelDo = DomainRelationship.newInstance(context,strPersonPhaseFTERelId);
					String strOldPersonPhaseFTEValue = personPhaseFTERelDo.getAttributeValue(context, ATTRIBUTE_FTE);
					Double nOldPersonPhaseFTEValue = Task.parseToDouble(strOldPersonPhaseFTEValue);    
					if("Hours".equalsIgnoreCase(numberofPeopleUnit))
					{
						nNewPersonPhaseFTEValue= nOldPersonPhaseFTEValue-((nOldPersonPhaseFTEValue /nMlTimeFramesSize)-(nNewFTEValue/slTimeFramePhaseList.size()));
					}
					else{
						nNewPersonPhaseFTEValue=((nOldPersonPhaseFTEValue*(nMlTimeFramesSize-1))+(nNewFTEValue/slTimeFramePhaseList.size()))/nMlTimeFramesSize; 					
					}
					personPhaseFTERelDo.setAttributeValue(context, ATTRIBUTE_FTE, String.valueOf(nNewPersonPhaseFTEValue));
				}				

			}  		
		}
		catch (Exception e) {
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


	/**
	 * This method updates the FTE value for ResourceRequest Table 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */  

	public void updateFTEColumnDataByTimeLine(Context context,String[]args) throws Exception 
	{
		try 
		{
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			Map programMap                      = (Map) JPO.unpackArgs(args);
			Map mpParamMap                      = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (Map)programMap.get("columnMap");
			Map mpRequestMap                    = (Map)programMap.get("requestMap");
			String strMonthYear                 = (String)mpColumnMap.get("name");
			String strMonthYearName             = (String)mpColumnMap.get("label");
			String strProjectOrOraganizationID  = (String)mpRequestMap.get("objectId");
			String strFTENewValue               = "";

			String[] arrJPOArguments            = new String[1];
			String[] strMonthYearSpilt          = null;
			double nNewFTEValue                 = 0;
			int nTimeLine                          = 0;
			int nYear                           = 0;
			boolean bisResourcePlanTable        = false;
			FTE fte                             = null;
			Map mapParam                        = new HashMap();
			mapParam.put("objectId", strProjectOrOraganizationID);
			arrJPOArguments = JPO.packArgs(mapParam);
			bisResourcePlanTable = isResourcePlanRequestSummaryTable(context,arrJPOArguments);
			try
			{
				nNewFTEValue = Task.parseToDouble((String)mpParamMap.get("New Value"));
			}
			catch (NumberFormatException e) {
				String strLanguage = context.getSession()
				.getLanguage();

				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ResourceRequest.SelectFTE", strLanguage);
				throw new MatrixException(sErrMsg + " " + strMonthYearName);
			}
			String strRelId     = (String)mpParamMap.get("relId");
			String strObjectId  = (String)mpParamMap.get("objectId");
			strMonthYearSpilt   = strMonthYear.split("-");
			nTimeLine              = Integer.parseInt(strMonthYearSpilt[0]);
			nYear               = Integer.parseInt(strMonthYearSpilt[1]);
			MapList mlPersonInfo        = null;
			MapList mlRequestsInfo      = null;
			String strRelationshipType  = RELATIONSHIP_ALLOCATED;
			String strPersonType        = TYPE_PERSON;
			String strFTE               = "";
			String strPersonId          = "";
			String strResourceState     = "";
			String strRequestID         = "" ;
			String strTypePerson        = "" ;
			final String SELECT_REL_ATTRIBUTE_FTE = DomainRelationship.getAttributeSelect(ATTRIBUTE_FTE);
			final String SELECT_REL_ATTRIBUTE_RESOURCE_STATE = DomainRelationship.getAttributeSelect(ATTRIBUTE_RESOURCE_STATE);
			final String ATTRIBUTE_START_DATE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_StartDate") + "]";
			final String ATTRIBUTE_END_DATE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_EndDate") + "]";
			DomainRelationship dmrAllocatedOrResourcePlan = null;

			StringList relSelect = new StringList();
			relSelect.add(DomainRelationship.SELECT_NAME);
			relSelect.add(DomainRelationship.SELECT_FROM_ID);
			relSelect.add(DomainRelationship.SELECT_TO_ID);
			relSelect.add(DomainRelationship.SELECT_TO_TYPE);
			relSelect.add(SELECT_REL_ATTRIBUTE_FTE);
			relSelect.add(SELECT_REL_ATTRIBUTE_RESOURCE_STATE);
			boolean isPerson = false;
			if( strRelId!= null && !"".equals(strRelId) && !"null".equals(strRelId)) {
				dmrAllocatedOrResourcePlan = DomainRelationship.newInstance(context,
						strRelId);

				Map mapRelData              = dmrAllocatedOrResourcePlan.getRelationshipData(context,relSelect);
				StringList slRelName        = (StringList)mapRelData.get(DomainRelationship.SELECT_NAME);
				StringList slRelFromId      = (StringList)mapRelData.get(DomainRelationship.SELECT_FROM_ID);
				StringList slRelToType      = (StringList)mapRelData.get(DomainRelationship.SELECT_TO_TYPE);
				StringList slRelToId        = (StringList)mapRelData.get(DomainRelationship.SELECT_TO_ID);
				strTypePerson               = (String)slRelToType.get(0);

				String strFTEValue = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourceRequest.FTE") ;
				int nFTEValidateValue = Integer.parseInt(strFTEValue); 

				if(TYPE_PERSON.equals(strTypePerson) && !"Hours".equalsIgnoreCase(numberofPeopleUnit)){
					if(nNewFTEValue>nFTEValidateValue || nNewFTEValue<0){
						String strLanguage = context.getSession().getLanguage();
						String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.ResourceRequest.InvalidFTE", strLanguage);
						throw new MatrixException(sErrMsg);
					}
				}
				if(TYPE_PERSON.equals(strTypePerson)){
					isPerson = true;
					strRequestID         =(String)slRelFromId.get(0);
					strResourceState 	  = (String)((StringList)mapRelData.get(SELECT_REL_ATTRIBUTE_RESOURCE_STATE)).get(0);
				}else{
					strRequestID         =(String)slRelToId.get(0);  
				}
			}else{
				strRequestID  =  strObjectId;
			}
			StringList slBusSelect = new StringList();
			slBusSelect.add("to["+RELATIONSHIP_RESOURCE_PLAN+"].attribute["+ATTRIBUTE_FTE+"]");
			slBusSelect.add(SELECT_CURRENT);
			slBusSelect.add(ATTRIBUTE_START_DATE);
			slBusSelect.add(ATTRIBUTE_END_DATE);

			ProgramCentralUtil.pushUserContext(context);
			DomainObject dmoRequest     = newInstance(context,strRequestID);
			Map mapObjInfo =   dmoRequest.getInfo(context,slBusSelect);
			ProgramCentralUtil.popUserContext(context);
			String strRequestFTEValue = (String) mapObjInfo.get("to["+RELATIONSHIP_RESOURCE_PLAN+"].attribute["+ATTRIBUTE_FTE+"]");
			String strRequestState = (String) mapObjInfo.get(SELECT_CURRENT);
			String strRequestStartDate = (String) mapObjInfo.get(ATTRIBUTE_START_DATE);
			String strRequestEndDate = (String) mapObjInfo.get(ATTRIBUTE_END_DATE);

			Date dtRequestStartDate = eMatrixDateFormat.getJavaDate(strRequestStartDate);
			Date dtRequestFinishDate = eMatrixDateFormat.getJavaDate(strRequestEndDate);
			Calendar calRequestStartDate = Calendar.getInstance();
			Calendar calRequestFinishDate = Calendar.getInstance();
			calRequestStartDate.setTime(dtRequestStartDate);
			calRequestFinishDate.setTime(dtRequestFinishDate);

			int nRequestStartMonth = 0;
			int nRequestStartYear = 0;
			int nRequestFinishMonth = 0;
			int nRequestFinishYear = 0;

			nRequestStartMonth = calRequestStartDate.get(Calendar.MONTH) + 1; //0=January
			nRequestStartYear = calRequestStartDate.get(Calendar.YEAR);
			nRequestFinishMonth = calRequestFinishDate.get(Calendar.MONTH) + 1;//0=January
			nRequestFinishYear = calRequestFinishDate.get(Calendar.YEAR);

			fte = FTE.getInstance(context, strRequestFTEValue);
			double nRequestFTEValue     = 0;
			nRequestFTEValue            = fte.getFTE(nYear, nTimeLine);  
			boolean isChangeFTE = false;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat (com.matrixone.apps.domain.util.eMatrixDateFormat.getEMatrixDateFormat());  
			if (strRequestState !=null)
			{
				if((bisResourcePlanTable == true && strRequestState.equals("Create") && !TYPE_PERSON.equals(strTypePerson)) 
						|| (bisResourcePlanTable == true && strRequestState.equals("Create") && strResourceState.equals("Requested") && TYPE_PERSON.equals(strTypePerson))
						|| (bisResourcePlanTable == true && strRequestState.equals("Rejected"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Requested") && strResourceState.equals("Requested"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Requested") && strResourceState.equals("Proposed"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Proposed") && strResourceState.equals("Requested"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Proposed") && strResourceState.equals("Proposed"))
						|| (bisResourcePlanTable == false && strRequestState.equals("Committed") && strResourceState.equals("Committed")))
				{
					FTE ftenew = FTE.getInstance(context);
					int nreqStartChange = 0;
					int nreqEndChange =0;
					String strReqFinishDate = "";
					String strReqStartDate  = "";
					boolean isUpdateFTE = fte.isUpdateFTE(dtRequestStartDate, dtRequestFinishDate, nTimeLine, nYear);
					String strFTEValue = dmrAllocatedOrResourcePlan.getAttributeValue(context,ATTRIBUTE_FTE);
					fte = FTE.getInstance(context, strFTEValue);
					Map mapAllFTE = fte.getAllFTE();
					if(!isPerson)
					{
						nreqStartChange =  fte.checkResourceRequestDateChange(dtRequestStartDate,nTimeLine,nYear,nNewFTEValue,true,nRequestFTEValue);
						if(nreqStartChange != 0)
						{
							if(nRequestStartYear == nYear && nRequestStartMonth == nTimeLine){
								strReqStartDate = strRequestStartDate;
							}else{
								int nReqStartDateChange = fte.changeinRequestDate(dtRequestStartDate,nTimeLine,nYear,true);
								strReqStartDate = fte.getChangedRequestDate(dtRequestStartDate,nReqStartDateChange,nreqStartChange,true);
							}
						}
						if(nreqStartChange == 0){
							nreqEndChange =  fte.checkResourceRequestDateChange(dtRequestFinishDate,nTimeLine,nYear,nNewFTEValue,false,nRequestFTEValue);
							if(nreqEndChange != 0)
							{
								if(nYear == nRequestFinishYear && nTimeLine == nRequestFinishMonth){
									strReqFinishDate = strRequestEndDate;
								}else{
									int nReqFinishDateChange = fte.changeinRequestDate(dtRequestFinishDate,nTimeLine,nYear,false);
									strReqFinishDate = fte.getChangedRequestDate(dtRequestFinishDate,nReqFinishDateChange,nreqEndChange,false);
								}
							}
						}
					}
					if(nreqStartChange == -1 || nreqEndChange == -1){
						mapAllFTE.remove(nTimeLine+"-"+nYear);
						ftenew.setAllFTE(mapAllFTE);
						isChangeFTE = true;
					}
					else if(isUpdateFTE || nreqStartChange == 1 || nreqEndChange == 1){
						mapAllFTE.put(nTimeLine+"-"+nYear,nNewFTEValue);
						ftenew.setAllFTE(mapAllFTE);
						isChangeFTE = true;
					}

					if(nreqStartChange == -1 || nreqStartChange == 1 ){
						dmoRequest.setAttributeValue(context,DomainConstants.ATTRIBUTE_START_DATE,strReqStartDate);
					}
					else if(nreqEndChange == -1 || nreqEndChange == 1 ){
						dmoRequest.setAttributeValue(context,DomainConstants.ATTRIBUTE_END_DATE,strReqFinishDate);
					}
					if(isChangeFTE)
					{
						strFTENewValue = ftenew.getXML();
						dmrAllocatedOrResourcePlan.setAttributeValue(context,ATTRIBUTE_FTE,strFTENewValue);
					}
				}
				else if((bisResourcePlanTable == true && strRequestState.equals("Requested"))
						|| (bisResourcePlanTable == true && strRequestState.equals("Proposed"))
						|| (bisResourcePlanTable == true && strRequestState.equals("Committed")))
				{
					String strLanguage = context.getSession().getLanguage();
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.ResourceRequest.NotInDesiredState", strLanguage); 
					throw new MatrixException(sErrMsg)  ;
				}
				else{
					String strLanguage = context.getSession().getLanguage();
					String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.ResourceRequest.NotInDesiredState", strLanguage);
					throw new MatrixException(sErrMsg)  ;
				}
			}
		}
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
	}

	/**
	 * This function gives the resource pools which are in inactive state and do not have any resource manager.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getExcludeInActiveResourcePool(Context context, String args[]) throws Exception
	{

		MapList mapUserList = null;
		StringList select = new StringList();
		select.add(DomainConstants.SELECT_ID);
		select.add(DomainConstants.SELECT_NAME);
		select.add(DomainConstants.SELECT_CURRENT);
		select.add(SELECT_RESOURCE_MANAGER_ID);
		String strState = "Inactive";
		String whereExp = "";
		String strId = " ";

		whereExp = "(("+SELECT_CURRENT+"=="+strState+") || !("+SELECT_RESOURCE_MANAGER_ID+"!=null))";
		mapUserList =DomainObject.findObjects(context,PropertyUtil.getSchemaProperty(context,"type_Organization"),"*",whereExp,select);
		StringList  slAllOrganizationIds = new StringList();

		for(int i=0;i<mapUserList.size();i++){

			Map mapUser = (Map) mapUserList.get(i);
			String strOrganizationId = (String) mapUser.get(DomainConstants.SELECT_ID);

			slAllOrganizationIds.add(strOrganizationId);
		}
		return slAllOrganizationIds;
	}

	/**
	 * Gets add resource table data for resource request
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	public MapList getTableAddResourceData(Context context, String[] args) throws Exception
	{
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList mlRequests = new MapList();
			String strObjectId = (String) programMap.get("objectId");
			String strRequestId = (String) programMap.get("requestId");
			String strPersontId = (String) programMap.get("personId");
			String strPersonId = "";
			String strTokenId = "";
			String[] strPersonArr = strPersontId.split("~");
			for(int i=0;i<strPersonArr.length;i++){
				java.util.StringTokenizer strTokenizer = new java.util.StringTokenizer(
						strPersonArr[i], "|");
				int nTokenCount = strTokenizer.countTokens();
				for (int j = 0; j < nTokenCount; j++) {

					strTokenId = strTokenizer.nextToken();
					if (j == 0) {
						strPersonId = strTokenId;
					}
				}
				Map mapProjectRole = new HashMap();
				mapProjectRole.put("PersonId",strPersonId);
				mapProjectRole.put("FTEToBeDisplayed","1");
				mlRequests.add(mapProjectRole);
			}
			return mlRequests ;
		}
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}    

	/**
	 * Gets person data for displaying in the PMCAddResource Table
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains requestMap 
	 * @return The Vector containing project role values
	 * @throws Exception if operation fails
	 */

	public Vector getColumnPersonData(Context context, String[] args)throws Exception 
	{
		try 
		{
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			Map mapObject = null;
			String strPersonId = "";
			String strProjectLeadRole = "";
			String sIntProjectRole = "";
			while (objectListIterator.hasNext())
			{
				mapObject = (Map) objectListIterator.next();
				strPersonId = (String)mapObject.get("PersonId");
				DomainObject dmo = DomainObject.newInstance(context,strPersonId);
				String strPersonName = dmo.getInfo(context,DomainConstants.SELECT_NAME);
				vecResult.add(XSSUtil.encodeForHTML(context,strPersonName));
			}
			return vecResult;
		}
		catch (Exception exp) 
		{
			throw exp;
		}
	}

	/**
	 * Used when Creating Request from WBS for table PMCResourceRequestCreateWBSTable
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, packed by AEF UI table component framework 
	 * @return The Vector containing State values
	 * @throws Exception if operation fails
	 */

	public Vector getStateData(Context context, String[] args)throws Exception 
	{
		try 
		{
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramMap=(Map)programMap.get("paramList");
			String strLanuguage=(String)paramMap.get("languageStr");
			Iterator objectListIterator = objectList.iterator();
			Map mapObject = null;
			String strState = "";
			String strStateCreate = "";
			while (objectListIterator.hasNext())
			{
				mapObject = (Map) objectListIterator.next();
				strState = (String)mapObject.get(SELECT_CURRENT);
				i18nNow loc = new i18nNow();
				String propertyFile = "emxProgramCentralStringResource";
				String propertyKey = "emxProgramCentral.ResourceRequest.create";
				strStateCreate = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						propertyKey, strLanuguage);
				vecResult.add(strStateCreate);
			}

			return vecResult;
		}
		catch (Exception exp) 
		{
			throw exp;
		}
	}

	/**
	 * This method clones the request and connect the new request to Project space.
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */

	public ResourceRequest cloneRequest(Context context,String[]args) throws Exception
	{
		try {
			Map programMap = (Map) JPO.unpackArgs(args);

			String strReqID				= (String)programMap.get("reqId");
			String strRelationship		= (String)programMap.get("relationshipName");
			String strprojectFromId		= (String)programMap.get("projectFromId");

			String strRPoolId			= (String)programMap.get("ResourcePool");
			StringList slReosurceId		= (StringList)programMap.get("Resource");
			StringList slReosurceFTE	= (StringList)programMap.get("ResourceFTE");
			String strPRole				= (String)programMap.get("PRole");
			String strRequestFTE		= (String)programMap.get("RequestFTE"); 
			String standrdCost 			= (String)programMap.get("stdCost");

			Object objSkillype = null;
			objSkillype = programMap.get("Skill");
			StringList slSkillsId  = new StringList();
			String strSkillsId = "";

			String strRequestId = FrameworkUtil.autoName(context,"type_ResourceRequest","policy_ResourceRequest");

			DomainObject domprojectFromId =DomainObject.newInstance(context, strprojectFromId);
			DomainObject domReqObject =DomainObject.newInstance(context, strReqID);

			final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
			final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";

			final String ATTRIBUTE_STANDARD_COST = PropertyUtil.getSchemaProperty(context, "attribute_StandardCost");

			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			slBusSelect.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

			Map mapObjInfo = domprojectFromId.getInfo(context, slBusSelect);
			String strStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
			String strFinishtDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE); 

			DomainObject domRequestId = DomainObject.newInstance(context, strRequestId);
			String strRPoolRel = RELATIONSHIP_RESOURCE_POOL;
			String strSkillRel = RELATIONSHIP_RESOURCE_REQUEST_SKILL;
			String strAllocatedRel = RELATIONSHIP_ALLOCATED;

			DomainRelationship dmrResourcePlan = DomainRelationship.connect(context,domprojectFromId,strRelationship,domRequestId);

			if(null!=strRPoolId && !"".equals(strRPoolId)&& !"Null".equalsIgnoreCase(strRPoolId))
			{
				DomainObject domRPoolObject = DomainObject.newInstance(context, strRPoolId);
				DomainRelationship dmrResourcePool = DomainRelationship.connect(context,domRequestId,strRPoolRel,domRPoolObject);
			}

			if(null == strPRole || "".equals(strPRole.trim()) || "null".equals(strPRole)){
				domRequestId.setAttributeValue(context,ATTRIBUTE_PROJECT_ROLE,EMPTY_STRING);
			}else
			{
				domRequestId.setAttributeValue(context, ATTRIBUTE_PROJECT_ROLE, strPRole);
			}
			if(null == standrdCost || "".equals(standrdCost.trim()) || "null".equals(standrdCost))
			{
				domRequestId.setAttributeValue(context,ATTRIBUTE_STANDARD_COST,EMPTY_STRING);
			}
			else
			{                
				domRequestId.setAttributeValue(context, ATTRIBUTE_STANDARD_COST, standrdCost);
			}

			domRequestId.setAttributeValue(context,DomainConstants.ATTRIBUTE_START_DATE,strStartDate);
			domRequestId.setAttributeValue(context,DomainConstants.ATTRIBUTE_END_DATE,strFinishtDate);

			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			try{
				if (objSkillype instanceof String) {
					strSkillsId = (String)programMap.get("Skill"); 
					if("null".equals(strSkillsId) || strSkillsId == null || "".equals(strSkillsId)){
						strSkillsId = EMPTY_STRING;
					}else{
						DomainObject domSkillObject = DomainObject.newInstance(context, strSkillsId);
						DomainRelationship dmrResourceSkill = DomainRelationship.connect(context,domRequestId,strSkillRel,domSkillObject);
					}
				}else{
					slSkillsId = (StringList)programMap.get("Skill"); 
					if("null".equals(slSkillsId) || slSkillsId == null || "".equals(slSkillsId)){
						slSkillsId = EMPTY_STRINGLIST;
					}else{
						for(int m=0;m<slSkillsId.size();m++){
							String strskiiId = (String) slSkillsId.get(m); 
							DomainObject domSkillObject = DomainObject.newInstance(context, strskiiId);
							DomainRelationship dmrResourceSkill = DomainRelationship.connect(context,domRequestId,strSkillRel,domSkillObject);
						}
					}
				}
			}
			finally{
				ContextUtil.popContext(context);
			}
			Date dtStartDate = null;
			Date dtFinishDate = null;
			MapList mlColumns = new MapList();
			dtStartDate = eMatrixDateFormat.getJavaDate(strStartDate);
			dtFinishDate = eMatrixDateFormat.getJavaDate(strFinishtDate);

			if(null != slReosurceId){
				for(int m=0;m<slReosurceId.size();m++){
					String strResourceId = (String) slReosurceId.get(m); 
					String strResourceFTE = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourceRequest.FTE") ;
					double dResourceFTE =   Task.parseToDouble(strResourceFTE);
					FTE fteResource = FTE.getInstance(context);
					mlColumns = fteResource.getTimeframes(dtStartDate, dtFinishDate);
					Iterator objectNewListIterator = mlColumns.iterator();
					Map mapNewObject = new HashMap();
					DomainObject domResourceObject = DomainObject.newInstance(context, strResourceId);
					DomainRelationship dmrResource = DomainRelationship.connect(context,domRequestId,strAllocatedRel,domResourceObject);
					for(int n=0;n<mlColumns.size();n++){
						mapNewObject = (Map) objectNewListIterator.next();
						int nTimeframe = (Integer)mapNewObject.get("timeframe");
						int nYear = (Integer)mapNewObject.get("year");
						fteResource.setFTE(nYear, nTimeframe, dResourceFTE);
						String strPersonFTE = fteResource.getXML();
						dmrResource.setAttributeValue(context, ATTRIBUTE_FTE, strPersonFTE);
					}
				}
			}
			String strFTEValue = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourceRequest.FTE") ;
			double dFTE =   Task.parseToDouble(strFTEValue);
			FTE fte = FTE.getInstance(context);
			mlColumns = fte.getTimeframes(dtStartDate, dtFinishDate);
			Iterator objectNewListIterator = mlColumns.iterator();
			Map mapNewObject = new HashMap();

			for(int m=0;m<mlColumns.size();m++){
				mapNewObject = (Map) objectNewListIterator.next();
				int nTimeframe = (Integer)mapNewObject.get("timeframe");
				int nYear = (Integer)mapNewObject.get("year");
				fte.setFTE(nYear, nTimeframe, dFTE);
				String strFTE = fte.getXML();
				dmrResourcePlan.setAttributeValue(context, ATTRIBUTE_FTE, strFTE);
			}
			return new ResourceRequest(strRequestId);
		} 
		catch (Exception exp)
		{
			throw exp;
		}

	}

	/**
	 * Add the resources from the resource request. If the provided resource are already from this request, then they will be ignored.
	 * 
	 * @param context The Matrix Context object
	 * @param args packed arguments.This is unpacked to maplist which gives the resource to be assigned for this requests. 
	 *                          This MapList will contain maps with following information.
	 *                           Key "id" Value the person object id
	 *                           Key "FTE" Value the FTE object initilized for this person assignment
	 *                           Key "Resource State" Value any of the range values for attribute "Resource State" (Requested/Proposed/Committed)
	 *                           Each of this persons will be connected to resource request object with "Allocated" relationship having
	 *                           attribute "Resource State" = given Resource State.
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public void addResourcesToRequest(Context context,String[] args) throws Exception
	{
		MapList mlResourceList = (MapList) JPO.unpackArgs(args);
		Map mapResourceMap = null;
		StringBuffer sBuff = new StringBuffer();
		String strResoureId = "";
		String strResourceState = "";
		String strRequestId = "";
		try{
			for (Iterator itrResource = mlResourceList.iterator(); itrResource.hasNext();)
			{
				mapResourceMap = (Map) itrResource.next();

				strResoureId = (String)mapResourceMap.get("Resource_Id");
				strRequestId = (String)mapResourceMap.get("RequestId");
				DomainObject dmoRequest = DomainObject.newInstance(context,strRequestId);
				String strPlanPreference= dmoRequest.getInfo(context,SELECT_REQUEST_RESOURCE_PLAN_PREFRENCE);
				StringList slRequestPhaseIdList = dmoRequest.getInfoList(context, ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
				MapList mlFTE = (MapList)mapResourceMap.get("FTE");
				strResourceState = (String)mapResourceMap.get("ResourceState");

				FTE fte = FTE.getInstance(context);
				String strFTEValue = "";
				String strPhaseId="";
				Iterator objectListIterator = mlFTE.iterator();
				Map mapFTE = null;
				while (objectListIterator.hasNext())
				{
					mapFTE = (Map) objectListIterator.next();
					String strMonthYear = "";
					String[] strMonthYearSpilt = null;
					int nMonth = 0;
					int nYear  = 0;
					double nFTE= 0d;
					if(!"null".equals(mapFTE)){
						FTE ftePlan = FTE.getInstance(context);
						for (Iterator iter = mapFTE.keySet().iterator(); iter.hasNext();) 
						{
							strMonthYear = (String) iter.next();
							nFTE = Task.parseToDouble((String)mapFTE.get(strMonthYear));
							strMonthYearSpilt = strMonthYear.split("-");
							nMonth = Integer.parseInt(strMonthYearSpilt[0]);
							nYear = Integer.parseInt(strMonthYearSpilt[1]);
							ftePlan.setFTE(nYear, nMonth, nFTE);
						}
						fte = ResourcePlanTemplate.getCalculatedFTEMap(ftePlan, fte);
						if(strPlanPreference.equalsIgnoreCase(RESOURCE_PLAN_PREFERENCE_PHASE))
						{
							updatePersonPhaseFTE(context,strResoureId,strRequestId,fte);
						}
					}
				}
				strFTEValue = fte.getXML();
				DomainObject dmoResourcePerson = DomainObject.newInstance(context,strResoureId);
				DomainRelationship domainRelationship = DomainRelationship.connect(context,dmoRequest,DomainConstants.RELATIONSHIP_ALLOCATED, dmoResourcePerson);
				domainRelationship.setAttributeValue(context, ATTRIBUTE_FTE,strFTEValue);
				domainRelationship.setAttributeValue(context, ATTRIBUTE_RESOURCE_STATE, strResourceState);
				if(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED.equalsIgnoreCase(strResourceState))
				{
					assignPeoplesToProjectSpace(context, strRequestId);
				}
			}
		}             
		catch (Exception e) {
			ContextUtil.abortTransaction(context);
			throw new MatrixException(e);
		}
	}
	public void removeResources(Context context,String[] args) throws Exception
	{
		StringList slResourceIdList = (StringList) JPO.unpackArgs(args);

		String strRequestId = this.getId(); 
		DomainObject dmoRequestObject = DomainObject.newInstance(context, strRequestId);

		String strPlanPreference= dmoRequestObject.getInfo(context,SELECT_REQUEST_RESOURCE_PLAN_PREFRENCE);


		String strRelationshipPattern = RELATIONSHIP_ALLOCATED;
		String strTypePattern = TYPE_PERSON;

		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainObject.SELECT_ID);
		slBusSelect.add(SELECT_NAME);

		StringList slRelSelect = new StringList();
		slRelSelect.add(DomainRelationship.SELECT_ID);

		boolean getTo = false; 
		boolean getFrom = true; 
		short recurseToLevel = 1;
		String strBusWhere = "";
		String strRelWhere = "";

		MapList mlRelatedObjects = dmoRequestObject.getRelatedObjects(context,
				strRelationshipPattern, //pattern to match relationships
				strTypePattern, //pattern to match types
				slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
				slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
				getTo, //get To relationships
				getFrom, //get From relationships
				recurseToLevel, //the number of levels to expand, 0 equals expand all.
				strBusWhere, //where clause to apply to objects, can be empty ""
				strRelWhere,0); //where clause to apply to relationship, can be empty ""
		Map mapRelatedObjectInfo = null;
		String strRelId = null;
		String strResourceId = null;
		StringList slResourceIds = new StringList();
		StringList slRelIds = new StringList();

		for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects .hasNext();) 
		{
			mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
			strResourceId = (String)mapRelatedObjectInfo.get(DomainConstants.SELECT_ID);

			if(slResourceIdList.contains(strResourceId))
			{
				strRelId = (String)mapRelatedObjectInfo.get(DomainRelationship.SELECT_ID);
				slRelIds.add(strRelId);
			}
		}
		if(strPlanPreference.equalsIgnoreCase(RESOURCE_PLAN_PREFERENCE_PHASE))
		{
			for(int nCount=0;nCount<slResourceIdList.size();nCount++)
			{ 
				String strPersonId= (String)slResourceIdList.get(nCount);
				DomainObject resourceDo= DomainObject.newInstance(context, strPersonId);
				StringList slPhaseFTERelIdList = dmoRequestObject.getInfoList(context, "from["+ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE+"].id");
				if(null!=slPhaseFTERelIdList && !slPhaseFTERelIdList.isEmpty())
				{
					for(int i=0;i<slPhaseFTERelIdList.size();i++)
					{ 
						String strPhaseFTERelId= (String)slPhaseFTERelIdList.get(i);   	
						String strPersonPhaseFTERelId = getPersonPhaseFTERelId(
								context, strPersonId, strPhaseFTERelId);
						if(null!=strPersonPhaseFTERelId && !"null".equalsIgnoreCase(strPersonPhaseFTERelId) && !"".equals(strPersonPhaseFTERelId))
						{
							slRelIds.add(strPersonPhaseFTERelId);
						}					
					}
				}
			}    		
		}

		String[] strRelIdsToDisconnect = (String[])slRelIds.toArray(new String[slRelIds.size()]);
		DomainRelationship.disconnect(context,strRelIdsToDisconnect);
	}

	/**
	 * This method is used to get Resource State attribute ranges both actual & display names.
	 * 
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return MapList with maps of each state info about Resource State attribute ranges.Each Map contains actual name with key "Name" 
	 * and display name with key "DisplayName".
	 * @throws Exception if operation fails
	 */

	public MapList getResourceStateRanges(Context context,String[] args)throws Exception
	{
		try {

			String sLanguage = context.getSession().getLanguage();

			AttributeType atrResourceState = new AttributeType(DomainConstants.ATTRIBUTE_RESOURCE_STATE);
			atrResourceState.open(context);
			StringList slResourceStateChoices = (StringList)atrResourceState.getChoices(context);    
			atrResourceState.close(context);

			String strResourceState = null;
			String strResourceStateDisplay = null;
			Map mapResourceStates = null;
			MapList mlResourceRequest = new MapList();

			for(int i= 0; i < slResourceStateChoices.size(); i++)
			{
				strResourceState = (String)slResourceStateChoices.get(i);

				if(strResourceState != null)
				{
					strResourceStateDisplay = i18nNow.getRangeI18NString(DomainConstants.ATTRIBUTE_RESOURCE_STATE, strResourceState, sLanguage);

					mapResourceStates = new HashMap();
					mapResourceStates.put("Name", XSSUtil.encodeForHTML(context,strResourceState));
					mapResourceStates.put("DisplayName", XSSUtil.encodeForHTML(context,strResourceStateDisplay));
				}
				mlResourceRequest.add(mapResourceStates);
			}   
			return  mlResourceRequest;
		} 
		catch (Exception e) 
		{
			throw e;           
		}
	}

	/**
	 * This method is used to get Resource Request policy state both actual & display names.
	 * 
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return MapList with maps of each state info each map contains actual name with key "Name" and display name with key "DisplayName";
	 * @throws Exception if operation fails
	 */

	public MapList getResourceRequestStates(Context context,String[] args) throws Exception
	{
		String strLanguage = context.getSession().getLanguage();
		Map mapResourceStates = new HashMap();
		Policy policy = new Policy(POLICY_RESOURCE_REQUEST); 
		policy.open(context);
		StateRequirementList slStateRequirement = policy.getStateRequirements(context);
		policy.close(context);

		StateRequirement stateRequirement = null;

		MapList mlResourceRequestTranslatedStates = new MapList();
		Map mapResourceRequestStates = null;
		String strStateName = null;
		String strStateDisplayName = null;

		for (StateRequirementItr requirementItr = new StateRequirementItr(slStateRequirement); requirementItr.next();)
		{
			stateRequirement = requirementItr.obj();
			strStateName = stateRequirement.getName();

			if(strStateName != null)
			{
				strStateDisplayName = i18nNow.getStateI18NString(POLICY_RESOURCE_REQUEST, strStateName, strLanguage);
				mapResourceRequestStates = new HashMap();

				mapResourceRequestStates.put("Name",strStateName);
				mapResourceRequestStates.put("DisplayName",strStateDisplayName);
			}
			mlResourceRequestTranslatedStates.add(mapResourceRequestStates);
		}
		return mlResourceRequestTranslatedStates ;
	}
	/**
	 * This Method updates the life cycle states of TYPE_RESOURCE_REQUEST and ATTRIBUTE_RESOURCE_STATE ranges
	 * 
	 * @param context The Matrix Context Object
	 * @param args The Packed arguments String array
	 * @throws Exception if operation fails
	 * 
	 */
	public void updateResourceRequestState(Context context, String[] args) throws Exception 
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map paramMap = (Map) programMap.get("paramMap");
		Map requestMap = (Map) programMap.get("requestMap");
		String strObjectId = (String) paramMap.get("objectId");
		this.setId(strObjectId);
		String strNewValue = (String) paramMap.get("New Value");
		String strRelId = (String) paramMap.get("relId");
		String strProjectId = (String) requestMap.get("projectID");
		String strResourcePoolId = (String) requestMap.get("resourcePoolId");
		String languageStr = context.getSession().getLanguage();
		DomainObject domObject = null;
		String strCurrentState = null;
		boolean isProjectLead = false;
		boolean isResourceManager = false;
		if (ProgramCentralUtil.isNullString(strObjectId) && ProgramCentralUtil.isNullString(strNewValue) && ProgramCentralUtil.isNullString(strResourcePoolId)) 
		{
			throw new IllegalArgumentException();
		}
		else
		{
			domObject = DomainObject.newInstance(context,strObjectId);
			isResourceManager = true;
		}
		if(domObject.isKindOf(context,TYPE_RESOURCE_REQUEST))
		{
			if(isProjectLead)
			{
				strCurrentState = domObject.getInfo(context,DomainConstants.SELECT_CURRENT);
				if(DomainConstants.STATE_RESOURCE_REQUEST_CREATE.equalsIgnoreCase(strCurrentState) &&
						DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED.equalsIgnoreCase(strNewValue))
				{
					String[] methodArguments = new String[0];
					request(context,methodArguments);
				}
			}
			else if(isResourceManager)
			{
				strCurrentState = domObject.getInfo(context,DomainConstants.SELECT_CURRENT);
				String[] arguments = new String[0];
				if(DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED.equalsIgnoreCase(strCurrentState))
				{
					if(DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED.equalsIgnoreCase(strNewValue))
					{
						propose(context);
					}
					else if(DomainConstants.STATE_RESOURCE_REQUEST_REJECTED.equalsIgnoreCase(strNewValue))
					{
						reject(context,arguments);
					}
				}
				else if(DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED.equalsIgnoreCase(strCurrentState))
				{
					if (DomainConstants.STATE_RESOURCE_REQUEST_COMMITTED.equalsIgnoreCase(strNewValue))
					{
						commit(context,arguments);
					}
					else if (DomainConstants.STATE_RESOURCE_REQUEST_REJECTED.equalsIgnoreCase(strNewValue))
					{
						reject(context,arguments);
					}
				}
				else if(DomainConstants.STATE_RESOURCE_REQUEST_COMMITTED.equalsIgnoreCase(strCurrentState))
				{
					return;
				}
			}
		}
		else if(domObject.isKindOf(context,TYPE_PERSON))
		{
			setResourceInfo(context,strObjectId, strNewValue,strRelId);
		}
	}
	/**
	 * This Method updates the ATTRIBUTE_RESOURCE_STATE and ATTRIBUTE_FTE ranges values for particular Resource
	 * 
	 * @param context The Matrix Context Object
	 * @param strResourceId Resource Id for which attributes are modified
	 * @param fte new selected ATTRIBUTE_FTE value
	 * @param strResourceState new selected ATTRIBUTE_RESOURCE_STATE range value
	 * @throws MatrixException if operation fails
	 * 
	 */ 

	public void setResourceInfo(Context context, String strResourceId,String strResourceState,String strRelId) throws MatrixException
	{
		DomainRelationship.setAttributeValue(context,strRelId,ATTRIBUTE_RESOURCE_STATE,strResourceState);
	} 


	/**
	 *  Resource Request object promoted to state 'Proposed'
	 *  if object is having resource connected with non zero FTE value
	 *  
	 * @param context Matrix Context Object
	 * @throws Exception if operation fails
	 */
	public void propose(Context context) throws Exception
	{
		String strNextState = STATE_RESOURCE_REQUEST_PROPOSED;
		String strCurrentState = this.getInfo(context,DomainConstants.SELECT_CURRENT);
		String languageStr = context.getSession().getLanguage();
		boolean IfNotInAppropriateState = !STATE_RESOURCE_REQUEST_REQUESTED.equals(strCurrentState);
		if(IfNotInAppropriateState)
		{
			String strTxtNotice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ResourceRequest.NotInDesiredState", languageStr);
			throw new MatrixException(strTxtNotice);
		}
		else
		{
			this.promote(context, strNextState);
		}
	}

	/**
	 *  Resource Request object promoted from state 'Proposed' to state 'Committed' 
	 *  
	 * @param context Matrix Context Object
	 * @throws Exception if operation fails
	 */
	public void commit(Context context,String[] args) throws MatrixException
	{
		this.commit(context);
	}

	/**
	 *  Resource Request object promoted from state 'Proposed' to state 'Committed' 
	 *  
	 * @param context Matrix Context Object
	 * @throws Exception if operation fails
	 */
	public void commit(Context context) throws MatrixException{
		try { 
			String strNextState = STATE_RESOURCE_REQUEST_COMMITTED;
			String strCurrentState = this.getInfo(context,DomainConstants.SELECT_CURRENT);
			String strLanguage = context.getSession().getLanguage();
			boolean IfNotInAppropriateState = !DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED.equals(strCurrentState) && !DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED.equals(strCurrentState);
			if(IfNotInAppropriateState){
				String strTxtNotice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ResourceRequest.NotInDesiredState", strLanguage);
				throw new MatrixException(strTxtNotice);
			}else{
				this.promote(context, strNextState);
			}
		}
		catch (Exception ex) 
		{
			throw new MatrixException(ex);
		}

	}

	/**
	 * Returns the requested FTEs for all provided ResourceRequests
	 * 
	 * @param context Matrix context Object
	 * @param args packed Map objects.This map contains following value
	 *       Key ResourceRequestIds - value StringList containing ResourceRequests Ids
	 * @return MapList which contains Maps of ResourceRequest FTEs,each map contains following
	 *       Key RequestId - value Resource Request Id
	 *       Key RequestFTE - value FTE object
	 *       Key RequestName - value Resource Request Name
	 * @throws Exception if operation fails
	 */
	public MapList getFTE(Context context, String[] args) throws Exception 
	{
		if (context ==  null)
		{
			throw new IllegalArgumentException("context");
		}
		if (args == null)
		{
			throw new IllegalArgumentException("args");
		}
		Map programMap = (Map)JPO.unpackArgs(args);
		StringList slResourceRequestIds = (StringList) programMap.get("ResourceRequestIds");
		if (slResourceRequestIds == null || slResourceRequestIds.size() == 0) 
		{
			throw new IllegalArgumentException(" Null or empty ResourceRequestIds");
		}
		final String SELECT_RESOURCE_REQUEST_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].to.id";
		final String SELECT_RESOURCE_REQUEST_NAME = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].to.name";
		final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].attribute["+ATTRIBUTE_FTE+"]";

		MapList mlResourceRequestInfo = new MapList();
		String[] strResourceRequestIds = (String[])slResourceRequestIds.toArray(new String[slResourceRequestIds.size()]);

		StringList slResourceRequestSelect = new StringList();
		slResourceRequestSelect.add(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
		slResourceRequestSelect.add(SELECT_RESOURCE_REQUEST_ID);
		slResourceRequestSelect.add(SELECT_RESOURCE_REQUEST_NAME);

		mlResourceRequestInfo = DomainObject.getInfo(context,strResourceRequestIds,slResourceRequestSelect);
		Map mapRequestInfo = null;
		Map mapResultMap = null;
		MapList mlResultList = new MapList();
		String strResourceRequestFTE= null;
		String strResourceRequestId =  null;
		String strResourceRequestName =  null;
		Map mapResourceRequest = null;

		for (Iterator itrRequestInfo = mlResourceRequestInfo.iterator(); itrRequestInfo.hasNext();)
		{
			mapRequestInfo = (Map)itrRequestInfo.next();
			strResourceRequestFTE = (String) mapRequestInfo.get(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
			strResourceRequestId = (String) mapRequestInfo.get(SELECT_RESOURCE_REQUEST_ID);
			strResourceRequestName = (String) mapRequestInfo.get(SELECT_RESOURCE_REQUEST_NAME);

			mapResultMap = new HashMap();
			mapResultMap.put("RequestFTE",FTE.getInstance(strResourceRequestFTE));
			mapResultMap.put("RequestId",strResourceRequestId);
			mapResultMap.put("RequestName",strResourceRequestName);

			mlResultList.add(mapResultMap);
		}
		return mlResultList;
	}
	/**
	 * Returns the resource information for this request (FTE and/or Resource State)
	 * (This method basically read information from "Allocated" relationship)
	 * 
	 * @param context The Matrix Context object
	 * 
	 * @param args packed Map object.This Map contains following keys
	 *         Key ResourceIds - value StringList containing all resource ids
                 Key getFTE - boolean to get Attribute "FTE" or Not
                 Key getResourceState - boolean to get Attribute "Resource State" or Not

	 * @return the requested information MapList having each Map object with following keys
	 *         Key "ResourceFTE" Value the FTE object for this resource
	 *         Key "ResourceState" Value the Resource State attribute for this resource
	 *         Key "ResourceId" Value the  object Id for this resource
	 *         
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public MapList getResourceInfo(Context context, String[] args) throws MatrixException
	{
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			StringList slRequestIds = (StringList) programMap.get("ResourceIds"); 
			String strRequestId = (String) programMap.get("RequestId");
			if (strRequestId != null) {
				this.setId(strRequestId);
			}
			boolean getFTE = (Boolean) programMap.get("getFTE"); 
			boolean getResourceState = (Boolean) programMap.get("getResourceState"); 
			if (context == null)
			{
				throw new IllegalArgumentException("context");
			}

			final String SELECT_REL_ATTRIBUTE_ALLOCATED_ATTRIBUTE_FTE = DomainRelationship.getAttributeSelect(ATTRIBUTE_FTE);
			final String SELECT_REL_ATTRIBUTE_ALLOCATED_ATTRIBUTE_RESOURCE_STATE = DomainRelationship.getAttributeSelect(ATTRIBUTE_RESOURCE_STATE);
			String strRelationshipPattern = RELATIONSHIP_ALLOCATED;
			String strTypePattern = TYPE_PERSON; 

			StringList slBusSelect = new StringList();
			slBusSelect.add(DomainObject.SELECT_ID);
			StringList slRelSelect = new StringList();
			if (getFTE)
			{
				slRelSelect.add(SELECT_REL_ATTRIBUTE_ALLOCATED_ATTRIBUTE_FTE);
			}
			if (getResourceState)
			{
				slRelSelect.add(SELECT_REL_ATTRIBUTE_ALLOCATED_ATTRIBUTE_RESOURCE_STATE);
			}
			boolean getTo = false; 
			boolean getFrom = true; 
			short recurseToLevel = 1;
			String strBusWhere = "";
			String strRelWhere = "";

			MapList mlResourceInfo = this.getRelatedObjects(context,
					strRelationshipPattern, //pattern to match relationships
					strTypePattern, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
					slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getTo, //get To relationships
					getFrom, //get From relationships
					recurseToLevel, //the number of levels to expand, 0 equals expand all.
					strBusWhere, //where clause to apply to objects, can be empty ""
					strRelWhere,0); //where clause to apply to relationship, can be empty ""


			Map mapRelatedObjectInfo = null;
			String strResourceId = null;
			String strResourceFTE = null;
			StringList slResourceFTE = null;
			StringList slResourceState = null;
			String strResourceState = null;
			Object objFTE =  null;
			Object objResourceState =  null;

			if (slRequestIds != null && slRequestIds.size() > 0) 
			{
				MapList mlFilteredList = new MapList();
				for (Iterator itrRelatedObjects = mlResourceInfo.iterator(); itrRelatedObjects.hasNext();)
				{
					mapRelatedObjectInfo = (Map) itrRelatedObjects.next();

					strResourceId = (String)mapRelatedObjectInfo.get(DomainObject.SELECT_ID);
					if (!slRequestIds.contains(strResourceId))
					{
						continue;
					}
					mlFilteredList.add(mapRelatedObjectInfo);
				}
				mlResourceInfo = mlFilteredList;
			}  
			for (Iterator itrRelatedObjects = mlResourceInfo.iterator(); itrRelatedObjects.hasNext();)
			{
				mapRelatedObjectInfo = (Map) itrRelatedObjects.next();

				mapRelatedObjectInfo.put("ResourceId",(String) mapRelatedObjectInfo.get(SELECT_ID));
				if (getFTE) {
					mapRelatedObjectInfo.put("ResourceFTE", FTE.getInstance(context, (String) mapRelatedObjectInfo.get(SELECT_REL_ATTRIBUTE_ALLOCATED_ATTRIBUTE_FTE)));
				}
				if (getResourceState) {
					mapRelatedObjectInfo.put("ResourceState",(String) mapRelatedObjectInfo.get(SELECT_REL_ATTRIBUTE_ALLOCATED_ATTRIBUTE_RESOURCE_STATE));
				}
			} 
			return mlResourceInfo;
		}
		catch (Exception exp)
		{
			throw new MatrixException (exp);
		}
	}

	/**
	 * When proposed request is Committed the check is performed to ensure if the allocated resources are
	 * loaded at least once across request timespan.  
	 * 
	 * @param context The Matrix Context object
	 * @param args argumenst from trigger program parameters object
	 *          
	 * @return int 0 for success and 1 for failure
	 * @throws MatrixException if operation fails
	 */

	public int triggerValidateResourceRequestFTE(Context context,String[] args) throws MatrixException
	{
		try
		{
			String strLanguage = context.getSession().getLanguage();
			String strResourceRequestId = (String) args[0];
			String strNextState = (String) args[1];
			if (STATE_RESOURCE_REQUEST_REJECTED.equals(strNextState))
			{
				return TRIGGER_SUCCESS;
			}
			else if(STATE_RESOURCE_REQUEST_COMMITTED.equals(strNextState))
			{
				Map mapResourceList = new HashMap();
				mapResourceList.put("RequestId",strResourceRequestId);
				mapResourceList.put("ResourceIds",null);
				mapResourceList.put("getFTE",true);
				mapResourceList.put("getResourceState",true);

				String[] strMethodArgs = JPO.packArgs(mapResourceList);
				MapList mlAllocatedPeople = getResourceInfo(context,strMethodArgs);
				for (Iterator itrAllocatedPeople = mlAllocatedPeople.iterator(); itrAllocatedPeople.hasNext();) 
				{
					Map mapAllocatedPerson = (Map) itrAllocatedPeople.next();
					FTE fte = (FTE)mapAllocatedPerson.get("ResourceFTE");
					Map mapAllFTEs = fte.getAllFTE();
					boolean isLoaded = false;
					for (Iterator itrTimeFrames = mapAllFTEs.keySet().iterator(); itrTimeFrames.hasNext();) 
					{
						String strTimeFrame = (String) itrTimeFrames.next();
						Double dFTE = (Double)mapAllFTEs.get(strTimeFrame);
						if (dFTE.doubleValue() > 0) 
						{
							isLoaded = true;
							break;
						}
					}
					if (!isLoaded) 
					{
						String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.ResourceRequest.ResourcesNotLoaded", strLanguage);
						throw new MatrixException(sErrMsg);
					}
				}
				return TRIGGER_SUCCESS;
			}
			else
			{
				return TRIGGER_FAILURE;
			}
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * Gets resource pool table data for Business Skill
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	public MapList getTableResourcePoolBusinessSkillData(Context context, String[] args) throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");
		DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
		dmoObject.getToRelationship(context);

		String strRelationshipPattern = RELATIONSHIP_ORGANIZATION_SKILL;
		String strTypePattern = TYPE_ORGANIZATION;


		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainObject.SELECT_ID);

		StringList slRelSelect = new StringList();
		slRelSelect.add(DomainRelationship.SELECT_ID);

		boolean getTo = false; //TODO customize code
		boolean getFrom = true; //TODO customize code
		short recurseToLevel = 1;
		String strBusWhere = "";
		String strRelWhere = "";

		MapList mlRelatedObjects = dmoObject.getRelatedObjects(context,
				strRelationshipPattern, //pattern to match relationships
				strTypePattern, //pattern to match types
				slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
				slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
				getTo, //get To relationships
				getFrom, //get From relationships
				recurseToLevel, //the number of levels to expand, 0 equals expand all.
				strBusWhere, //where clause to apply to objects, can be empty ""
				strRelWhere,0); //where clause to apply to relationship, can be empty ""

		return mlRelatedObjects;
	}
	public String getSubscribedRequestMessageText(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "text");
		com.matrixone.jdom.Document doc = getRequestMailXML(context, info);
		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));
	}

	public String getSubscribedRequestMessageHTML(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "html");
		com.matrixone.jdom.Document doc = getRequestMailXML(context, info);
		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));
	}


	/**
	 * Sets the mail message for Request Notifications
	 * 
	 * @param context The Matrix Context object
	 */
	public com.matrixone.jdom.Document getRequestMailXML(Context context, Map info) throws Exception
	{
		// get base url
		String baseURL = (String)info.get("baseURL");
		// get notification name
		String notificationName = (String)info.get("notificationName");
		Map eventCmdMap = UIMenu.getCommand(context, notificationName);
		String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
		String eventKey = "emxProgramCentral.ResourceRequest.Event." + eventName.replace(' ', '_');
		String bundleName = (String)info.get("bundleName");
		String locale = ((Locale)info.get("locale")).toString();
		String i18NEvent = EnoviaResourceBundle.getProperty(context, bundleName, eventKey, locale);
		// get Message Type
		String messageType = (String)info.get("messageType");

		// get route id
		String routeId = (String)info.get("id");
		// get document object info
		DomainObject route = DomainObject.newInstance(context, routeId);
		StringList selectList = new StringList(3);
		selectList.addElement(SELECT_TYPE);
		selectList.addElement(SELECT_NAME);
		selectList.addElement(SELECT_REVISION);
		Map routeInfo = route.getInfo(context, selectList);
		String routeType = (String)routeInfo.get(SELECT_TYPE);
		String i18NRouteType = UINavigatorUtil.getAdminI18NString("type", routeType, locale);
		String routeName = (String)routeInfo.get(SELECT_NAME);
		String routeRev = (String)routeInfo.get(SELECT_REVISION);

		// header data
		Map headerInfo = new HashMap();
		headerInfo.put("header", i18NEvent + " : " + i18NRouteType + " " + routeName + " " + routeRev);

		// body data
		Map bodyInfo = null;
		MapList objList = route.getRelatedObjects(context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE, "*", selectList, null, true, false, (short)1, null, null,0);
		if (objList != null && objList.size() > 0)
		{
			bodyInfo = new HashMap();
			Map fieldInfo = new HashMap();
			bodyInfo.put(EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ResourceRequest.Event.Mail.Connected_Objects", locale), fieldInfo);
			for(int i = 0; i < objList.size(); i++)
			{
				Map objInfo = (Map) objList.get(i);
				String objType = (String)objInfo.get(SELECT_TYPE);
				String i18NObjectType = UINavigatorUtil.getAdminI18NString("type", objType, locale);
				String objName = (String)objInfo.get(SELECT_NAME);
				String objRev = (String)objInfo.get(SELECT_REVISION);
				fieldInfo.put(EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ResourceRequest.Event.Mail.TNR", locale), i18NObjectType + " " + objName + " " + objRev);
			}
		}

		// footer data
		Map footerInfo = new HashMap();
		ArrayList dataLineInfo = new ArrayList();
		if (messageType.equalsIgnoreCase("html"))
		{
			String[] messageValues = new String[4];
			messageValues[0] = baseURL + "?objectId=" + routeId;
			messageValues[1] = i18NRouteType;
			messageValues[2] = routeName;
			messageValues[3] = routeRev;
			String viewLink = MessageUtil.getMessage(context,null,
					"emxProgramCentral.Object.Event.Html.Mail.ViewLink",
					messageValues,null,
					context.getLocale(),bundleName);

			dataLineInfo.add(viewLink);
		} else {
			String[] messageValues = new String[3];
			messageValues[0] = i18NRouteType;
			messageValues[1] = routeName;
			messageValues[2] = routeRev;
			String viewLink = MessageUtil.getMessage(context,null,
					"emxProgramCentral.Object.Event.Text.Mail.ViewLink",
					messageValues,null,
					context.getLocale(),bundleName);

			dataLineInfo.add(viewLink);
			dataLineInfo.add(baseURL + "?objectId=" + routeId);
		}
		footerInfo.put("dataLines", dataLineInfo);

		return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, bodyInfo, footerInfo));
	}

	/**
	 * Sets the notification object parameters when the request goes from requested state to rejected or proposed
	 * 
	 * @param context The Matrix Context object
	 */
	public void publishSubscriptionForRejectedOrProposed(Context context, String[] args) throws Exception
	{
		try {
			String strRequestobjectId = "";
			String strState = "";
			String strNotificationName  = "";

			strRequestobjectId = args[0];
			DomainObject dmoRequest = DomainObject.newInstance(context,strRequestobjectId);
			strState = dmoRequest.getInfo(context, SELECT_CURRENT);

			String[] strNotificationParam = new String[2];
			strNotificationParam[0] = strRequestobjectId;

			if (STATE_RESOURCE_REQUEST_REJECTED .equals(strState))
			{
				strNotificationName = "PMCResourceRequestRejectedEvent"; 
				strNotificationParam[1] = strNotificationName;
				emxNotificationUtil_mxJPO notificationUtil = new emxNotificationUtil_mxJPO(context, new String[0]);
				notificationUtil.objectNotification(context,strNotificationParam);
			}
			else if (STATE_RESOURCE_REQUEST_PROPOSED .equals(strState))
			{
				strNotificationName = "PMCResourceRequestProposedEvent"; 
				strNotificationParam[1] = strNotificationName;
				emxNotificationUtil_mxJPO notificationUtil = new emxNotificationUtil_mxJPO(context, new String[0]);
				notificationUtil.objectNotification(context,strNotificationParam);
			}
		} catch (Exception e) {
		}

	}

	/**
	 * Sets the notification object parameters when the request goes from proposed state to rejected or committed
	 * 
	 * @param context The Matrix Context object
	 */
	public void publishSubscriptionForRejectedOrCommitted(Context context, String[] args) throws Exception
	{
		try {
			String strRequestobjectId = "";
			String strState = "";
			String strNotificationName  = "";

			strRequestobjectId = args[0];
			DomainObject dmoRequest = DomainObject.newInstance(context,strRequestobjectId);
			strState = dmoRequest.getInfo(context, SELECT_CURRENT);

			String[] strNotificationParam = new String[2];
			strNotificationParam[0] = strRequestobjectId;

			if (STATE_RESOURCE_REQUEST_REJECTED .equals(strState))
			{
				strNotificationName = "PMCResourceRequestRejectedEvent"; 
				strNotificationParam[1] = strNotificationName;
				emxNotificationUtil_mxJPO notificationUtil = new emxNotificationUtil_mxJPO(context, new String[0]);
				notificationUtil.objectNotification(context,strNotificationParam);
			}
			else if (STATE_RESOURCE_REQUEST_COMMITTED .equals(strState))
			{
				strNotificationName = "PMCResourceRequestCommittedEvent"; 
				strNotificationParam[1] = strNotificationName;
				emxNotificationUtil_mxJPO notificationUtil = new emxNotificationUtil_mxJPO(context, new String[0]);
				notificationUtil.objectNotification(context,strNotificationParam);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * This will notify the resource manager when promoting to proposed state without any resources to it.
	 * @param context Matrix Context object
	 * @param args will hold the resource request id set through eService Program Argument.
	 * @throws Exception if operation fails
	 */
	public int triggerCheckIfResourceAllocated(Context context, String[] args) throws MatrixException
	{
		try {
			String strResourceRequestId = args[0];
			String strNextResourceRequestState = args[1];
			boolean isSuccess = false;
			String strLanguage = context.getSession().getLanguage();

			DomainObject dmoResourceRequest = DomainObject.newInstance(context,strResourceRequestId);
			StringList slBusSelect = new StringList(DomainConstants.SELECT_CURRENT);
			Map mapResInfo = dmoResourceRequest.getInfo(context, slBusSelect);
			String strCurrentState = (String)mapResInfo.get(DomainConstants.SELECT_CURRENT);

			if (strNextResourceRequestState.equals(STATE_RESOURCE_REQUEST_REJECTED))
			{
				return TRIGGER_SUCCESS;
			}
			else if( (STATE_RESOURCE_REQUEST_REQUESTED.equals(strCurrentState) && STATE_RESOURCE_REQUEST_PROPOSED.equals(strNextResourceRequestState)) 
					|| (STATE_RESOURCE_REQUEST_PROPOSED.equals(strCurrentState) && STATE_RESOURCE_REQUEST_COMMITTED.equals(strNextResourceRequestState)))
			{
				final String SELECT_REL_ATTRIBUTE_FTE = "attribute["+ATTRIBUTE_FTE+"]";
				StringList slBusSelects = new StringList();
				slBusSelects.add(SELECT_ID);
				slBusSelects.add(SELECT_NAME);

				StringList slRelSelects = new StringList();
				slRelSelects.add(SELECT_REL_ATTRIBUTE_FTE);
				short recurseToLevel = 1;

				MapList mlPersons = dmoResourceRequest.getRelatedObjects(context,
						RELATIONSHIP_ALLOCATED, //pattern to match relationships
						TYPE_PERSON, //pattern to match types
						slBusSelects, //the eMatrix StringList object that holds the list of select statement pertaining to Business obejcts.
						slRelSelects, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
						false, //get To relationships
						true, //get From relationships
						recurseToLevel, //the number of levels to expand, 0 equals expand all.
						"", //where clause to apply to objects, can be empty ""
						"",0); //where clause to apply to relationship, can be empty ""
				StringList slPersonIds = new StringList();
				String strPersonFTE = "";
				FTE fte;
				Map mapFTE ;
				String strValue = "";

				for (Iterator itrPersons = mlPersons.iterator(); itrPersons.hasNext();)
				{
					Map person = (Map) itrPersons.next();
					String strPersonId = (String) person.get(SELECT_ID);
					strPersonFTE = (String) person.get(SELECT_REL_ATTRIBUTE_FTE);
					fte = FTE.getInstance(context, strPersonFTE);
					mapFTE = (HashMap) fte.getAllFTE();

					for (Iterator itrKey= mapFTE.keySet().iterator(); itrKey.hasNext();)
					{
						strValue = (String) itrKey.next();
						double dbFTEValue = (Double)mapFTE.get(strValue);

						if(null != strPersonId && dbFTEValue > 0d)
						{
							isSuccess = true;
							break;
						}
					}
				}
				if (isSuccess)
				{
					return TRIGGER_SUCCESS;
				}
			} 
			return TRIGGER_FAILURE;
		}

		catch (Exception ex)
		{
			throw new MatrixException(ex);
		}

	}

	/**
	 * Sets the created state to the request when the Project lead clicks ReuseRejectedRequest command
	 * 
	 * @param context The Matrix Context object
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public void reuseRejectedRequestByPL(Context context,String[]args) throws Exception 
	{
		this.request(context, args);
	}

	/** It will ensure that all resources allocated to Resource Request should be in "Proposed / Rejected" state before Resource Requests promotion to 
	 *   "Proposed / Rejected" state.
	 * 
	 * @param context Matrix Context Object
	 * @param args String array holding arguments set from trigger definition as eService Program arguments.
	 * @throws MatrixException if Operation fails.
	 */
	public int triggerProposeOrRejectResources(Context context,String[] args) throws MatrixException
	{
		try
		{
			String strResourceRequestId = args[0];
			String strNextResourceRequestState = args[1];
			String strLanguage = context.getSession().getLanguage();

			boolean isSuccess = false;

			final String SELECT_REL_ATTRIBUTE_RESOURCE_STATE = "attribute["+ATTRIBUTE_RESOURCE_STATE+"]";
			final String SELECT_REL_ATTRIBUTE_FTE = "attribute["+ATTRIBUTE_FTE+"]";

			StringList slBusSelects = new StringList();
			slBusSelects.add(SELECT_NAME);
			slBusSelects.add(SELECT_ID);

			StringList slRelSelects = new StringList();
			slRelSelects.add(DomainRelationship.SELECT_ID);
			slRelSelects.add(SELECT_REL_ATTRIBUTE_RESOURCE_STATE);
			slRelSelects.add(SELECT_REL_ATTRIBUTE_FTE);

			short recurseToLevel = 1;

			DomainObject dmoResourceRequest = DomainObject.newInstance(context,strResourceRequestId);

			MapList mlResources = dmoResourceRequest.getRelatedObjects(context,
					RELATIONSHIP_ALLOCATED, //pattern to match relationships
					TYPE_PERSON, //pattern to match types
					slBusSelects, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
					slRelSelects, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					false, //get To relationships
					true, //get From relationships
					recurseToLevel, //the number of levels to expand, 0 equals expand all.
					null, //where clause to apply to objects, can be empty ""
					null,0); //where clause to apply to relationship, can be empty ""

			String strRelationshipId = "";
			String strResourceId = "";


			String strResourceStatus = (STATE_RESOURCE_REQUEST_PROPOSED.equalsIgnoreCase(strNextResourceRequestState))?ATTRIBUTE_RESOURCE_STATE_RANGE_PROPOSED:ATTRIBUTE_RESOURCE_STATE_RANGE_REJECTED;
			for (Iterator itrResource = mlResources.iterator(); itrResource.hasNext();)
			{
				Map mapResource = (Map) itrResource.next();
				strRelationshipId = (String) mapResource.get(DomainRelationship.SELECT_ID);
				strResourceId = (String) mapResource.get(SELECT_ID);
				DomainRelationship.setAttributeValue(context,strRelationshipId,ATTRIBUTE_RESOURCE_STATE,strResourceStatus);
			}
			return TRIGGER_SUCCESS;
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}
	/**
	 * This will set Resource Request to Committed State if all prerequisites satisfied
	 *  along with all resources allocated to it in committed state too.
	 * 
	 * @param context Matrix Context Object
	 * @param args String array holding arguments set from trigger definition as eService Program arguments.
	 * @throws MatrixException if operation fails.
	 */
	public int triggerCommitOrRejectResources(Context context,String[] args) throws MatrixException
	{
		try {
			String strResourceRequestId = args[0];
			String strNextState = args[1];
			String [] strResourceRequestIds = new String[1];
			strResourceRequestIds[0] = strResourceRequestId; 
			DomainObject dmoResourceRequest =  DomainObject.newInstance(context,strResourceRequestId);
			final String SELECT_REL_FROM_ALLOCATED_ID = "from["+RELATIONSHIP_ALLOCATED+"].id";
			StringList slSelectList = new StringList();
			slSelectList.add(SELECT_REL_FROM_ALLOCATED_ID);
			BusinessObjectWithSelectList resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strResourceRequestIds,slSelectList);

			BusinessObjectWithSelect bows = null;
			boolean isSuccess = false;
			Map mapRequestRelResourcePlanData = new HashMap();
			String strResourceStatus = (STATE_RESOURCE_REQUEST_COMMITTED.equalsIgnoreCase(strNextState))?ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED:ATTRIBUTE_RESOURCE_STATE_RANGE_REJECTED;
			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
			{
				bows = itr.obj();
				StringList slAllocatedRelationshipIds = bows.getSelectDataList(SELECT_REL_FROM_ALLOCATED_ID);
				if (slAllocatedRelationshipIds != null) {
					int size = slAllocatedRelationshipIds.size();
					for(int i = 0;i < size; i++)
					{
						String strRelationshipId = (String)slAllocatedRelationshipIds.get(i);
						DomainRelationship.setAttributeValue(context,strRelationshipId,ATTRIBUTE_RESOURCE_STATE, strResourceStatus);
					}
				}
			}
			if (STATE_RESOURCE_REQUEST_COMMITTED.equalsIgnoreCase(strNextState)){
				try{

					ProgramCentralUtil.pushUserContext(context);
					this.assignPeoplesToProjectSpace(context, strResourceRequestId);

				}catch (Exception ex) {
					throw new MatrixException(ex);
				}finally{
					ProgramCentralUtil.popUserContext(context);
				}
			}

			return TRIGGER_SUCCESS;
		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}
	/**
	 * This will set Resource Request to Committed State if all prerequisites required
	 * to make Request in Committed State taken care by Check Trigger.
	 * @param context Matrix context object
	 * @param args String array holding list of resource requests to be approved.
	 * @throws MatrixException if operation fails.
	 */
	public void approve(Context context,String[] args) throws MatrixException
	{
		try
		{
			ContextUtil.startTransaction(context, true);

			Map mapResourceRequestIds = (Map) JPO.unpackArgs(args);
			StringList slResourceRequestIds = (StringList)mapResourceRequestIds.get("ResourceRequestIds"); 

			String strResourceRequestId = "";
			int nSetStateSuccess = 0;
			String languageStr = context.getSession().getLanguage();
			String strComment = "";
			for (int i = 0; i < slResourceRequestIds.size(); i++ )
			{
				strResourceRequestId = (String) slResourceRequestIds.get(i);

				DomainObject dmoResourceRequest = DomainObject.newInstance(context,strResourceRequestId);
				String currentState = dmoResourceRequest.getInfo(context, SELECT_CURRENT);

				if(currentState.equalsIgnoreCase(STATE_RESOURCE_REQUEST_REQUESTED))
				{
					this.setId(strResourceRequestId);
					this.propose(context);
					this.commit(context);
				}
				else if(currentState.equalsIgnoreCase(STATE_RESOURCE_REQUEST_PROPOSED))
				{
					this.setId(strResourceRequestId);
					this.commit(context);
				}
			}
			ContextUtil.commitTransaction(context);
		} 
		catch (Exception e)
		{
			ContextUtil.abortTransaction(context);
			throw new MatrixException(e);
		}
	}
	/**
	 * Ensures to change the 'Resource State' Attribute value to 'Requested' from 'Rejected' when Resource
	 * Request is promoted back to state 'Requested'.
	 * 
	 * @param context Matrix Context object
	 * @param args String array, holding values set as arguments to 
	 *      eService Program Arguments for this trigger
	 * @throws MatrixException
	 */
	public int  triggerResourcesToRequestedState(Context context,String[] args) throws MatrixException
	{
		try
		{
			String strResourceRequestId = args[0];
			String strNextState = args[1];

			if(STATE_RESOURCE_REQUEST_REQUESTED.equals(strNextState))
			{
				boolean isSuccess = false;
				String [] strResourceRequestIds = new String[1];
				strResourceRequestIds[0] = strResourceRequestId; 

				BusinessObjectWithSelectList businessObjectWithSelectList = null;
				final String SELECT_REL_FROM_ALLOCATED_ID = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].id";

				StringList slSelectList = new StringList();
				slSelectList.add(SELECT_REL_FROM_ALLOCATED_ID);

				BusinessObjectWithSelectList resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strResourceRequestIds,slSelectList);

				Map mapRequestRelResourcePlanData = new HashMap();

				for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
				{
					BusinessObjectWithSelect bows = itr.obj();

					StringList slAllocatedRelationshipIds = bows.getSelectDataList(SELECT_REL_FROM_ALLOCATED_ID);

					if (slAllocatedRelationshipIds != null) 
					{
						int size = slAllocatedRelationshipIds.size();
						for (int i = 0; i < size; i++) 
						{
							String strRelationshipId = (String)slAllocatedRelationshipIds.get(i);
							//
							// Note: Context is pushed for access issue
							// to be validated how to remove such code
							//
							ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
							try
							{
								DomainRelationship.setAttributeValue(context,strRelationshipId,ATTRIBUTE_RESOURCE_STATE,ATTRIBUTE_RESOURCE_STATE_RANGE_REQUESTED);
							}
							finally
							{
								ContextUtil.popContext(context);
							}
						}
					}
				}

				return TRIGGER_SUCCESS;
			}
			else
			{
				return TRIGGER_FAILURE;
			}
		}
		catch (Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**Propose the selected resource requests by validating the allocated resources with non zero FTE value.
	 * 
	 * @param context Matrix Context 
	 * @param args
	 * @throws MatrixException
	 */
	public StringList propose(Context context,String[] args) throws Exception
	{
		StringList slAffectingRequests = new StringList();
		String strResourceRequestName ="";
		// Added:18-Jan-2011:hp5:R211 PRG:IR-010642V6R2012
		i18nNow i18n = new i18nNow();
		final String STRING_NO_RESOURCE_ASSIGNED_TO_REQUEST = i18n.GetString("emxProgramCentralStringResource", context.getSession().getLanguage(), "emxProgramCentral.CurrencyConversionForResourceRequest.NoResourceAssignedToRequest");
		// End:18-Jan-2011:hp5:R211 PRG:IR-010642V6R2012
		Map mapResourceRequestIds =(Map)JPO.unpackArgs(args);
		StringList slResourceRequestIds = (StringList) mapResourceRequestIds.get("ResourceRequestIds");
		for (int i = 0; i < slResourceRequestIds.size();i++)
		{
			try
			{
				String strResourceRequestId = (String) slResourceRequestIds.get(i);
				this.setId(strResourceRequestId);
				DomainObject dmoResourceRequest = DomainObject.newInstance(context,this.getId());
				strResourceRequestName = dmoResourceRequest.getInfo(context,SELECT_NAME);
				this.propose(context);
			}
			catch(Exception ex)
			{
				slAffectingRequests.add(strResourceRequestName);
			}
		}
		// Added:19-Jan-2011:hp5:R211 PRG:IR-010642V6R2012    	 
		if(!slAffectingRequests.isEmpty())
		{
			String str= "" +(STRING_NO_RESOURCE_ASSIGNED_TO_REQUEST +slAffectingRequests.toString());
			MqlUtil.mqlCommand(context, "notice \""+str+"\""); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
		} 
		// End:19-Jan-2011:hp5:R211 PRG:IR-010642V6R2012
		return slAffectingRequests;
	}
	/**
	 * This Method updates the resource pool for the request  in edit mode and also disconnects the old resource pool 
	 * and allocated resources which are not member of new resource pool.
	 * 
	 * @param context The Matrix Context Object
	 * @param args The Packed arguments String array 
	 * @throws Exception if operation fails
	 * 
	 */
	public void updateResourcePool(Context context, String[] args) throws Exception 
	{
		try{
			//This map gives the  updated FTE values
			Map programMap                      = (Map) JPO.unpackArgs(args);
			Map mpParamMap                      = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (Map)programMap.get("columnMap");
			Map mpRequestMap                    = (Map)programMap.get("requestMap");
			String strOrganizationNewValue      = (String)mpParamMap.get("New Value");
			if(null==strOrganizationNewValue || "".equals(strOrganizationNewValue) || "null".equalsIgnoreCase(strOrganizationNewValue))
			{
				String strArrOrganizationNewValue[]      = (String[])mpRequestMap.get("ResourcePoolOID");
				if(null!=strArrOrganizationNewValue && strArrOrganizationNewValue.length!=0)
					strOrganizationNewValue = strArrOrganizationNewValue[0];
			}
			boolean isNewValValid = (null!=strOrganizationNewValue && !"".equals(strOrganizationNewValue) && !"null".equalsIgnoreCase(strOrganizationNewValue));
			String strRequestObjectId  = (String)mpParamMap.get("objectId");    		 
			DomainObject dmoRequest     = newInstance(context,strRequestObjectId);
			StringList slInvalidSelection = new StringList();
			String strRequestState = null;
			String strRequestName = null;
			StringList busSelect = new StringList();
			busSelect.add(SELECT_NAME);
			busSelect.add(SELECT_TYPE);
			busSelect.add(SELECT_CURRENT);
			busSelect.add(SELECT_ID);
			busSelect.add(SELECT_RESOURCE_POOL_ID);
			Map mapResourceRequestInfo= dmoRequest.getInfo(context, busSelect);
			String strOrganizationOldValue = (String)mapResourceRequestInfo.get(SELECT_RESOURCE_POOL_ID);
			//Connecting Resource Request Object to Resource Pool(Organization) object with ResourcePool relationship
			if(null!=strOrganizationOldValue && !"".equals(strOrganizationOldValue) && !"null".equals(strOrganizationOldValue))
			{
				//Finding the Resource Pool which is already connected to the Request.
				//Added:12-Mar-2010:s4e:R209 PRG:IR-031392
				//to get people allocated to resource pool
				Pattern relPattern = new Pattern(RELATIONSHIP_RESOURCE_POOL);
				relPattern.addPattern(RELATIONSHIP_ALLOCATED);
				relPattern.addPattern(RELATIONSHIP_RESOURCE_REQUEST_SKILL);
				Pattern typePattern = new Pattern(TYPE_ORGANIZATION);
				typePattern.addPattern(TYPE_PERSON);	         
				typePattern.addPattern(TYPE_COMPANY);
				typePattern.addPattern(TYPE_BUSINESS_UNIT);				
				typePattern.addPattern(TYPE_DEPARTMENT);
				typePattern.addPattern(TYPE_BUSINESS_SKILL);
				//End:12-Mar-2010:s4e:R209 PRG:IR-031392
				String whereClause = "" ;
				String relWhere= "";
				StringList relSelect = new StringList();
				relSelect.add(DomainRelationship.SELECT_RELATIONSHIP_ID);

				MapList mlRequests = dmoRequest.getRelatedObjects(
						context,
						relPattern.getPattern(),
						typePattern.getPattern(),
						busSelect,
						relSelect,
						true,
						true,
						(short)1,
						whereClause,
						relWhere,0);


				Map mapRequestInfo = null;
				String strRelId = null;
				String strObjectId ="";
				String strObjectName ="";
				String strObjectType="";
				Map ParamMap = new HashMap();
				Map PersonMap = new HashMap();
				StringList slMemberList= new StringList();
				String disconnectRelIds[] = null;
				StringList sldisconnectList = new StringList();
				if(isNewValValid)
				{
					DomainObject organizationDob = DomainObject.newInstance(context,strOrganizationNewValue);
					slMemberList = organizationDob.getInfoList(context,"from[" +DomainConstants.RELATIONSHIP_MEMBER+ "].to."+SELECT_ID);
				}

				for (Iterator itrRequest = mlRequests.iterator(); itrRequest .hasNext();)
				{
					mapRequestInfo = (Map) itrRequest.next();
					strObjectId = (String)mapRequestInfo.get(SELECT_ID);					
					strObjectName =(String)mapRequestInfo.get(SELECT_NAME);
					strRelId = (String) mapRequestInfo.get(SELECT_RELATIONSHIP_ID);
					strObjectType = (String)mapRequestInfo.get(SELECT_TYPE);
					strRequestState = (String) mapResourceRequestInfo.get(SELECT_CURRENT);
					boolean flag= false;
					//Added:12-Mar-2010:s4e:R209 PRG:IR-031392
					//This block will check if the if the person which are connected to Resource requests belong to new Resource pool
					//If person does not belong to "Resource Pool" it will get disconnected from the request.
					if(TYPE_PERSON.equals(strObjectType))
					{
						flag = slMemberList.contains(strObjectId);
						if(!(slMemberList.contains(strObjectId)))
						{
							sldisconnectList.add(strRelId);
							//DomainRelationship.disconnect(context,strRelId);							
						}												
					}
					//End:12-Mar-2010:s4e:R209 PRG:IR-031392
					//Modified:12-Mar-2010:s4e:R209 PRG:IR-031392
					//All relationship need to be disconnected is added to sldisconnectList.which includes person ids 
					//and also old ResourcePool id.
					else if((TYPE_BUSINESS_UNIT.equals(strObjectType))||(TYPE_COMPANY.equals(strObjectType))||(TYPE_DEPARTMENT.equals(strObjectType)))
					{
						if(strRequestState.equals(DomainConstants.STATE_RESOURCE_REQUEST_CREATE) || strRequestState.equals(DomainConstants.STATE_RESOURCE_REQUEST_REJECTED))
						{
							//Disconnecting the already existing resource pool   							
							sldisconnectList.add(strRelId);
							//Connecting newly selected resource pool to the request.
							if(isNewValValid)
							{
								DomainObject dmoResourcePool     = newInstance(context,strOrganizationNewValue);
								String strResourcePoolRel = PropertyUtil.getSchemaProperty(context,"relationship_ResourcePool");
								DomainRelationship dmrResourcePool= DomainRelationship.connect(context,dmoRequest,strResourcePoolRel,dmoResourcePool);
							}
						}
						else
						{
							strRequestName = (String) mapResourceRequestInfo.get(SELECT_NAME);
							slInvalidSelection.add(strRequestName);
						}
					}
				}
				if(!sldisconnectList.isEmpty())
				{
					disconnectRelIds = sldisconnectList.toString().substring(1, sldisconnectList.toString().length()-1).split(",");
					DomainRelationship.disconnect(context, disconnectRelIds);
				}
			}
			else if(ProgramCentralUtil.isNotNullString(strOrganizationNewValue))
			{
				strRequestState = (String) mapResourceRequestInfo.get(SELECT_CURRENT);
				if(strRequestState.equals(DomainConstants.STATE_RESOURCE_REQUEST_CREATE) || strRequestState.equals(DomainConstants.STATE_RESOURCE_REQUEST_REJECTED))
				{
					DomainObject dmoResourcePool     = newInstance(context,strOrganizationNewValue);
					String strResourcePoolRel = PropertyUtil.getSchemaProperty(context,"relationship_ResourcePool");
					DomainRelationship dmrResourcePool= DomainRelationship.connect(context,dmoRequest,strResourcePoolRel,dmoResourcePool);
				}
				else
				{
					strRequestName = (String) mapResourceRequestInfo.get(SELECT_NAME);
					slInvalidSelection.add(strRequestName);
				}
			}
			if(!slInvalidSelection.isEmpty())
			{
				String strErrorMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.ResourceRequest.NotInDesiredState", context.getSession().getLanguage());
				MqlUtil.mqlCommand(context, "Error" +slInvalidSelection+ "\""+strErrorMessage+"\""); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
			}
		}         
		catch (Exception exp)
		{
			throw exp;
		}         
	}

	/**
	 * This Method updates the business skill for the request  in edit mode
	 * 
	 * @param context The Matrix Context Object
	 * @param args The Packed arguments String array 
	 * @throws Exception if operation fails
	 * 
	 */
	public void updateBusinessSkill(Context context, String[] args) throws Exception 
	{
		try{
			//This map gives the  updated FTE values
			Map programMap                      = (Map) JPO.unpackArgs(args);
			Map mpParamMap                      = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (Map)programMap.get("columnMap");
			Map mpRequestMap                    = (Map)programMap.get("requestMap");
			String[] strArrSkillDisplayNewValue = (String[])mpRequestMap.get("Business SkillDisplay");
			String strArrSkillNewValue[]      = (String[])mpRequestMap.get("Business Skill");
			String strSkillNewValue = null;
			String strSkillDisplayValue = null;
			if(null!=strArrSkillNewValue)
				strSkillNewValue = strArrSkillNewValue[0];
			if(null!=strArrSkillDisplayNewValue)
				strSkillDisplayValue = strArrSkillDisplayNewValue[0];
			String strRequestObjectId  = (String)mpParamMap.get("objectId");
			DomainObject dmoRequest     = newInstance(context,strRequestObjectId);
			String strNewValue = (String)mpParamMap.get("New Value");
			String strRelId = null;

			String strRelationshipType = RELATIONSHIP_RESOURCE_REQUEST_SKILL;
			String strType = TYPE_BUSINESS_SKILL;
			String whereClause = "" ;
			StringList busSelect = new StringList();
			StringList relSelect = new StringList();
			relSelect.add(DomainRelationship.SELECT_RELATIONSHIP_ID);

			MapList mlRequests = dmoRequest.getRelatedObjects(
					context,
					strRelationshipType,
					strType,
					busSelect,
					relSelect,
					false,
					true,
					(short)1,
					whereClause,
					DomainConstants.EMPTY_STRING,0);

			Map mapRequestInfo = null;
			for (Iterator iterRequest = mlRequests.iterator(); iterRequest .hasNext();)
			{
				mapRequestInfo = (Map) iterRequest.next();
				strRelId = (String) mapRequestInfo.get(SELECT_RELATIONSHIP_ID);
			}

			String strSkillId = "";
			boolean isRemoveBusinessSkill =false;
			boolean addNewBusinessSkill = false;
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			try
			{   
				if(ProgramCentralUtil.isNotNullString(strSkillDisplayValue))
				{
					if(ProgramCentralUtil.isNotNullString(strSkillNewValue))
					{
						isRemoveBusinessSkill = true;
						strSkillId = strSkillNewValue;
						addNewBusinessSkill = true;
					}
				}
				else if (ProgramCentralUtil.isNotNullString(strNewValue))
				{
					isRemoveBusinessSkill = true;
					strSkillId = strNewValue; 
					addNewBusinessSkill = true;
				}
				else
				{
					isRemoveBusinessSkill = true;
				}
				if(isRemoveBusinessSkill && ProgramCentralUtil.isNotNullString(strRelId))
				{
					DomainRelationship.disconnect(context,strRelId);
				}
				if(addNewBusinessSkill)
				{
					DomainObject dmoSkill     = newInstance(context,strSkillId);
					String strBusinessSkillRel = RELATIONSHIP_RESOURCE_REQUEST_SKILL;
					DomainRelationship dmrResourceRequestSkill= DomainRelationship.connect(context,dmoRequest,strBusinessSkillRel,dmoSkill);
				}
			}
			finally
			{
				ContextUtil.popContext(context);
			}
		}
		catch (Exception exp)
		{
			throw exp;
		}
	}


	/* This Method updates the FTE for the request  in edit mode when the Request Start date is changed.
	 * 
	 * @param context The Matrix Context Object
	 * @param args The Packed arguments String array 
	 * @throws Exception if operation fails
	 * 
	 */
	public void updateFTEforRequestStartDate(Context context, String[] args) throws MatrixException 
	{
		try{
			//This map gives the  updated FTE values
			Map programMap                      = (Map) JPO.unpackArgs(args);
			Map mpParamMap                      = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (Map)programMap.get("columnMap");
			Map mpRequestMap                    = (Map)programMap.get("requestMap");

			String strStartDateNewValue               = "";

			String newEndDateValue	=	((String[])mpRequestMap.get(ProgramCentralConstants.REQUEST_END_DATE))[0];
			strStartDateNewValue = (String)mpParamMap.get("New Value");
			String strRequestObjectId  = (String)mpParamMap.get("objectId");
			String strResourcePlanRelId  = (String)mpParamMap.get("relId");
			DomainObject dmoRequest     = newInstance(context,strRequestObjectId);

			String strProjectId = dmoRequest.getInfo(context, SELECT_PROJECT_SPACE_ID);
			String strTimeZone = ProgramCentralConstants.EMPTY_STRING;
			Object objTimezone = mpRequestMap.get("timeZone");
			if(objTimezone instanceof String)
				strTimeZone = (String)objTimezone;
			else if(objTimezone instanceof String[])				
				strTimeZone = ((String[])mpRequestMap.get("timeZone"))[0];

			double clientTimeZone = Task.parseToDouble(strTimeZone);
			String strLocale = ProgramCentralConstants.EMPTY_STRING;
			Locale locale = null;
			Object objLocale = mpRequestMap.get("localeObj");
			if(objLocale instanceof String)
				strLocale = (String)objLocale;
			else if(objLocale instanceof String[])				
				strLocale = ((String[])mpRequestMap.get("localeObj"))[0];			
			else if(objLocale instanceof Object)				
				locale = (Locale)objLocale;
			if(ProgramCentralUtil.isNotNullString(strLocale))				
				locale = new Locale(strLocale);			

			strStartDateNewValue = eMatrixDateFormat.getFormattedInputDate(context, strStartDateNewValue, clientTimeZone, locale);
			newEndDateValue = eMatrixDateFormat.getFormattedInputDate(context, newEndDateValue, clientTimeZone, locale);

			//Connecting Resource Request Object to Resource Pool(Organization) object with ResourcePool relationship
			if(strStartDateNewValue!= null && !"".equals(strStartDateNewValue) && !"null".equals(strStartDateNewValue) )
			{

				DomainRelationship dmoResourcePlanRel = new DomainRelationship(strResourcePlanRelId);

				String strRequestEndDate = "";
				String strRequestStartDate = "";
				strRequestStartDate = dmoRequest.getAttributeValue(context,ATTRIBUTE_START_DATE);
				strRequestEndDate = dmoRequest.getAttributeValue(context,ATTRIBUTE_END_DATE);

				String strOldFTE = "";
				final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to[" + RELATIONSHIP_RESOURCE_PLAN + "].attribute[" + ATTRIBUTE_FTE + "]";
				strOldFTE = dmoRequest.getInfo(context, SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);


				FTE fte = FTE.getInstance(context);
				double nFTEValue = 0;

				Date dtRequestNewStartDate = eMatrixDateFormat.getJavaDate(strStartDateNewValue);
				Date dtRequestStartDate = eMatrixDateFormat.getJavaDate(strRequestStartDate);

				if(! dtRequestNewStartDate.equals(dtRequestStartDate)){

					if(newEndDateValue!= null && !"".equals(newEndDateValue) && !"null".equals(newEndDateValue) )
					{
						Date dtRequestFinishDate = eMatrixDateFormat.getJavaDate(strRequestEndDate);
						Date newRequestFinishDate = eMatrixDateFormat.getJavaDate(newEndDateValue);

						Calendar calRequestNewStartDate = Calendar.getInstance();
						Calendar calRequestNewEndDate = Calendar.getInstance();
						Calendar calRequestStartDate = Calendar.getInstance();
						Calendar calRequestFinishDate = Calendar.getInstance();

						calRequestNewStartDate.setTime(dtRequestNewStartDate);
						calRequestNewStartDate.set(Calendar.HOUR_OF_DAY, 00);
						calRequestNewStartDate.set(Calendar.MINUTE, 0);
						calRequestNewStartDate.set(Calendar.SECOND, 0);

						calRequestNewEndDate.setTime(newRequestFinishDate);
						calRequestNewEndDate.set(Calendar.HOUR_OF_DAY, 00);
						calRequestNewEndDate.set(Calendar.MINUTE, 0);
						calRequestNewEndDate.set(Calendar.SECOND, 0);

						calRequestFinishDate.setTime(dtRequestFinishDate);
						calRequestFinishDate.set(Calendar.HOUR_OF_DAY, 12);
						calRequestFinishDate.set(Calendar.MINUTE, 0);
						calRequestFinishDate.set(Calendar.SECOND, 0);

						calRequestStartDate.setTime(dtRequestStartDate);
						calRequestStartDate.set(Calendar.HOUR_OF_DAY, 00);
						calRequestStartDate.set(Calendar.MINUTE, 0);
						calRequestStartDate.set(Calendar.SECOND, 0);

						dtRequestNewStartDate = calRequestNewStartDate.getTime();
						newRequestFinishDate	=	calRequestNewEndDate.getTime();
						dtRequestFinishDate = calRequestFinishDate.getTime();
						dtRequestStartDate = calRequestStartDate.getTime();

						int nRequestNewStartMonth = 0;
						int nRequestNewStartYear = 0;
						int nRequestFinishMonth = 0;
						int nRequestFinishYear = 0;


						FTE fteRequest = FTE.getInstance(context);

						nRequestNewStartMonth = calRequestNewStartDate.get(Calendar.MONTH) + 1; //0=January
						nRequestNewStartYear = calRequestNewStartDate.get(Calendar.YEAR);

						nRequestFinishMonth = calRequestFinishDate.get(Calendar.MONTH) + 1;//0=January
						nRequestFinishYear = calRequestFinishDate.get(Calendar.YEAR);

						MapList mlNewMonthYearList = null;  
						MapList mlOldMonthYearList = null;   

						if (strStartDateNewValue != null && !"".equals(strStartDateNewValue) && !"null".equals(strStartDateNewValue)&& strRequestEndDate != null && !"".equals(strRequestEndDate) && !"null".equals(strRequestEndDate))
						{
							mlNewMonthYearList = fteRequest.getTimeframes(dtRequestNewStartDate,newRequestFinishDate);

						}

						if (strRequestStartDate != null && !"".equals(strRequestStartDate) && !"null".equals(strRequestStartDate)&& strRequestEndDate != null && !"".equals(strRequestEndDate) && !"null".equals(strRequestEndDate))
						{
							mlOldMonthYearList = fteRequest.getTimeframes(dtRequestStartDate,dtRequestFinishDate);

						}

						StringList slMonthYear = new StringList();
						int iNewMonthYearSize = mlNewMonthYearList.size();
						int iOldMonthYearSize = mlOldMonthYearList.size();
						Map mapFTE = new HashMap();
						MapList mlFTE = new MapList();
						Map mapNewObject = new HashMap();
						Iterator objectNewListIterator = mlNewMonthYearList.iterator();
						String strFTE = "";

						for (int i =0;i<iNewMonthYearSize ;i++)
						{
							mapNewObject = (Map) objectNewListIterator.next();
							int nNewTimeFrame = (Integer)mapNewObject.get("timeframe");
							int nNewTimeYear = (Integer)mapNewObject.get("year");
							mapFTE = checkForExistingFTEValue(context,mlOldMonthYearList,nNewTimeFrame,nNewTimeYear,strOldFTE);
							mlFTE.add(mapFTE);
						}

						//Setting attribute FTE on relationship Resource Plan

						String strFTEValue = "";

						if(mlFTE!=null)
						{
							String strMonthYear = "";
							double nFTE = 0;
							String[] strMonthYearSpilt = null;
							int nMonth = 0;
							int nYear  = 0;
							Iterator objectListIterator = mlFTE.iterator();
							while (objectListIterator.hasNext())
							{
								mapFTE = (Map) objectListIterator.next();

								for (Iterator iter = mapFTE.keySet().iterator(); iter.hasNext();) 
								{
									strMonthYear = (String) iter.next();
									nFTE = Task.parseToDouble((String)mapFTE.get(strMonthYear));
									strMonthYearSpilt = strMonthYear.split("-");
									nRequestNewStartMonth = Integer.parseInt(strMonthYearSpilt[0]);
									nRequestNewStartYear = Integer.parseInt(strMonthYearSpilt[1]);
									fte.setFTE(nRequestNewStartYear, nRequestNewStartMonth, nFTE);
								}
							}
						}


						strFTEValue = fte.getXML();
						dmoResourcePlanRel.setAttributeValue(context,ATTRIBUTE_FTE,strFTEValue);

						dmoRequest.setAttributeValue(context,ATTRIBUTE_START_DATE,strStartDateNewValue);
					}
				}

			}
		}

		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}

	}


	/* This Method updates the FTE for the request  in edit mode when the Request End date is changed.
	 * 
	 * @param context The Matrix Context Object
	 * @param args The Packed arguments String array 
	 * @throws Exception if operation fails
	 * 
	 */
	public void updateFTEforRequestEndDate(Context context, String[] args) throws MatrixException 
	{
		try{
			//This map gives the  updated FTE values
			Map programMap                      = (Map) JPO.unpackArgs(args);
			Map mpParamMap                      = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (Map)programMap.get("columnMap");
			Map mpRequestMap                    = (Map)programMap.get("requestMap");

			String strEndDateNewValue               = "";

			String newStartDateValue	=	((String[])mpRequestMap.get(ProgramCentralConstants.REQUEST_START_DATE))[0];
			strEndDateNewValue = (String)mpParamMap.get("New Value");
			String strRequestObjectId  = (String)mpParamMap.get("objectId");
			String strResourcePlanRelId  = (String)mpParamMap.get("relId");

			String strTimeZone = ProgramCentralConstants.EMPTY_STRING;
			Object objTimezone = mpRequestMap.get("timeZone");
			if(objTimezone instanceof String)
				strTimeZone = (String)objTimezone;
			else if(objTimezone instanceof String[])				
				strTimeZone = ((String[])mpRequestMap.get("timeZone"))[0];

			double clientTimeZone = Task.parseToDouble(strTimeZone);

			String strLocale = ProgramCentralConstants.EMPTY_STRING;
			Locale locale = null;
			Object objLocale = mpRequestMap.get("localeObj");
			if(objLocale instanceof String)
				strLocale = (String)objLocale;
			else if(objLocale instanceof String[])				
				strLocale = ((String[])mpRequestMap.get("localeObj"))[0];			
			else if(objLocale instanceof Object)				
				locale = (Locale)objLocale;
			if(ProgramCentralUtil.isNotNullString(strLocale))				
				locale = new Locale(strLocale);

			newStartDateValue = eMatrixDateFormat.getFormattedInputDate(context, newStartDateValue, clientTimeZone, locale);
			strEndDateNewValue = eMatrixDateFormat.getFormattedInputDate(context, strEndDateNewValue, clientTimeZone, locale);
			DomainObject dmoRequest     = newInstance(context,strRequestObjectId);

			String strProjectId = dmoRequest.getInfo(context, SELECT_PROJECT_SPACE_ID);
			//Connecting Resource Request Object to Resource Pool(Organization) object with ResourcePool relationship
			if(strEndDateNewValue!= null && !"".equals(strEndDateNewValue) && !"null".equals(strEndDateNewValue) )
			{
				DomainRelationship dmoResourcePlanRel = new DomainRelationship(strResourcePlanRelId);


				String strRequestStartDate = "";
				String strRequestEndDate = "";
				//old start date might get updated in updateFTEforRequestStartDate(),hence using old start date from cache.
				strRequestStartDate = 	(String)mpParamMap.get("Old value");
				strRequestEndDate = dmoRequest.getAttributeValue(context,ATTRIBUTE_END_DATE);
				String strOldFTE = "";
				final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to[" + RELATIONSHIP_RESOURCE_PLAN + "].attribute[" + ATTRIBUTE_FTE + "]";
				strOldFTE = dmoRequest.getInfo(context, SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);

				Date dtRequestNewFinishDate = eMatrixDateFormat.getJavaDate(strEndDateNewValue);
				Date dtRequestEndDate = eMatrixDateFormat.getJavaDate(strRequestEndDate);

				FTE fte = FTE.getInstance(context);
				double nFTEValue = 0;

				if(! dtRequestNewFinishDate.equals(dtRequestEndDate)){

					if(newStartDateValue!= null && !"".equals(newStartDateValue) && !"null".equals(newStartDateValue) )
					{                
						Date dtRequestStartDate = eMatrixDateFormat.getJavaDate(strRequestStartDate);
						Date requestNewStartDate = eMatrixDateFormat.getJavaDate(newStartDateValue);


						Calendar calRequestStartDate = Calendar.getInstance();
						Calendar calRequestFinishDate = Calendar.getInstance();
						Calendar calRequestNewFinishDate = Calendar.getInstance();
						Calendar calRequestNewStartDate = Calendar.getInstance();

						calRequestNewFinishDate.setTime(dtRequestNewFinishDate);
						calRequestNewFinishDate.set(Calendar.HOUR_OF_DAY, 12);
						calRequestNewFinishDate.set(Calendar.MINUTE, 0);
						calRequestNewFinishDate.set(Calendar.SECOND, 0);

						calRequestNewStartDate.setTime(requestNewStartDate);
						calRequestNewStartDate.set(Calendar.HOUR_OF_DAY, 12);
						calRequestNewStartDate.set(Calendar.MINUTE, 0);
						calRequestNewStartDate.set(Calendar.SECOND, 0);

						calRequestFinishDate.setTime(dtRequestEndDate);
						calRequestFinishDate.set(Calendar.HOUR_OF_DAY, 12);
						calRequestFinishDate.set(Calendar.MINUTE, 0);
						calRequestFinishDate.set(Calendar.SECOND, 0);

						calRequestStartDate.setTime(dtRequestStartDate);
						calRequestStartDate.set(Calendar.HOUR_OF_DAY, 00);
						calRequestStartDate.set(Calendar.MINUTE, 0);
						calRequestStartDate.set(Calendar.SECOND, 0);

						dtRequestNewFinishDate = calRequestNewFinishDate.getTime();
						requestNewStartDate	=	calRequestNewStartDate.getTime();
						dtRequestEndDate = calRequestFinishDate.getTime();
						dtRequestStartDate = calRequestStartDate.getTime();

						int nRequestStartMonth = 0;
						int nRequestStartYear = 0;
						int nRequestFinishMonth = 0;
						int nRequestFinishYear = 0;


						FTE fteRequest = FTE.getInstance(context);

						nRequestStartMonth = calRequestStartDate.get(Calendar.MONTH) + 1; //0=January
						nRequestStartYear = calRequestStartDate.get(Calendar.YEAR);

						nRequestFinishMonth = calRequestFinishDate.get(Calendar.MONTH) + 1;//0=January
						nRequestFinishYear = calRequestFinishDate.get(Calendar.YEAR);

						MapList mlNewMonthYearList = null;  
						MapList mlOldMonthYearList = null;  
						if (strEndDateNewValue != null && !"".equals(strEndDateNewValue) && !"null".equals(strEndDateNewValue)&& strRequestStartDate != null && !"".equals(strRequestStartDate) && !"null".equals(strRequestStartDate))
						{
							mlNewMonthYearList = fteRequest.getTimeframes(requestNewStartDate,dtRequestNewFinishDate);

						}

						if (strRequestEndDate != null && !"".equals(strRequestEndDate) && !"null".equals(strRequestEndDate)&& strRequestStartDate != null && !"".equals(strRequestStartDate) && !"null".equals(strRequestStartDate))
						{
							mlOldMonthYearList = fteRequest.getTimeframes(dtRequestStartDate,dtRequestEndDate);

						}
						StringList slMonthYear = new StringList();
						int iNewMonthYearSize = mlNewMonthYearList.size();

						Map mapFTE = new HashMap();
						MapList mlFTE = new MapList();
						Map mapNewObject = new HashMap();
						Iterator objectNewListIterator = mlNewMonthYearList.iterator();


						for (int i =0;i<iNewMonthYearSize ;i++)
						{
							mapNewObject = (Map) objectNewListIterator.next();
							int nNewTimeFrame = (Integer)mapNewObject.get("timeframe");
							int nNewTimeYear = (Integer)mapNewObject.get("year");
							mapFTE = checkForExistingFTEValue(context,mlOldMonthYearList,nNewTimeFrame,nNewTimeYear,strOldFTE);
							mlFTE.add(mapFTE);
						}


						//Setting attribute FTE on relationship Resource Plan

						String strFTEValue = "";

						if(mlFTE!=null)
						{
							String strMonthYear = "";
							double nFTE = 0;
							String[] strMonthYearSpilt = null;
							int nMonth = 0;
							int nYear  = 0;

							Iterator objectListIterator = mlFTE.iterator();

							while (objectListIterator.hasNext())
							{
								mapFTE = (Map) objectListIterator.next();


								for (Iterator iter = mapFTE.keySet().iterator(); iter.hasNext();) 
								{
									strMonthYear = (String) iter.next();
									nFTE = Task.parseToDouble((String)mapFTE.get(strMonthYear));
									strMonthYearSpilt = strMonthYear.split("-");
									nRequestStartMonth = Integer.parseInt(strMonthYearSpilt[0]);
									nRequestStartYear = Integer.parseInt(strMonthYearSpilt[1]);
									fte.setFTE(nRequestStartYear, nRequestStartMonth, nFTE);
								}
							}
						}

						strFTEValue = fte.getXML();
						dmoResourcePlanRel.setAttributeValue(context,ATTRIBUTE_FTE,strFTEValue);

						dmoRequest.setAttributeValue(context,ATTRIBUTE_END_DATE,strEndDateNewValue);

					}
				}
			}
		}

		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
	} 



	public Map checkForExistingFTEValue(Context context,MapList mlOldMonthYearList,int nNewTimeFrame,int nNewTimeYear,String strOldFTE)throws Exception{
		try {
			Iterator objectOldListIterator = mlOldMonthYearList.iterator();
			int iOldMonthYearSize = mlOldMonthYearList.size();
			Map mapOldObject = new HashMap();
			Map mapFTE = new HashMap();
			double nFTEValue = 0;
			String strFTE = "";
			String strMonthYearList = nNewTimeFrame+"-"+nNewTimeYear;
			for(int k =0;k<iOldMonthYearSize ;k++){
				mapOldObject = (Map) objectOldListIterator.next();
				int nOldTimeFrame = (Integer)mapOldObject.get("timeframe");
				int nOldTimeYear = (Integer)mapOldObject.get("year");
				FTE fte = FTE.getInstance(context);

				if(nNewTimeFrame == nOldTimeFrame && nNewTimeYear==nOldTimeYear ){
					if(null != strOldFTE && !"null".equals (strOldFTE)&& !"".equals(strOldFTE)){
						// Assigning consistent key for FTE
						fte = FTE.getInstance(context, strOldFTE);
						nFTEValue   = fte.getFTE(nOldTimeYear, nOldTimeFrame);  
						strFTE = Double.toString(nFTEValue);
						mapFTE.put(strMonthYearList, strFTE);
					}
				}
			}

			if(mapFTE.size() == 0){
				strFTE = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourceRequest.DefaultFTEValue") ;
				mapFTE.put(strMonthYearList, strFTE);
			}
			return mapFTE ; 
		}catch (Exception exp)
		{
			throw exp;
		}
	}
	/**
	 * This method clones the request and connect the new request to Project space.
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */

	public ResourceRequest copyRequest(Context context,String[]args) throws MatrixException 
	{
		try 
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strProjectID = (String)programMap.get("projectId");
			DomainObject domProjectId = DomainObject.newInstance(context, strProjectID);
			ResourceRequest resourceRequest = new ResourceRequest();
			String strResourcePlanMode = getResourcePlanPreference(context, domProjectId);
			if(RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strResourcePlanMode))
			{
				resourceRequest = copyRequestByPhase(context, args);
			}
			else if(RESOURCE_PLAN_PREFERENCE_TIMELINE.equalsIgnoreCase(strResourcePlanMode))
			{
				resourceRequest = copyRequestByTimeLine(context, args);
			}
			return resourceRequest;
		} 
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
	}
	/**
	 * This method clones the request and connect the new request to Project space.
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */

	public ResourceRequest copyRequestByPhase(Context context,String[]args) throws MatrixException 
	{
		try 
		{
			Map programMap = (Map) JPO.unpackArgs(args);

			String strReqID = (String)programMap.get("requestId");
			String strProjectID = (String)programMap.get("projectId");
			String ATTRIBUTE_PROJECT_ROLE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_ProjectRole") + "]";
			final String SELECT_REL_ATTRIBUTE_FTE =  "to["+ RELATIONSHIP_RESOURCE_PLAN+"].attribute["+ATTRIBUTE_FTE+"]";
			DomainObject domProjectId = DomainObject.newInstance(context, strProjectID);
			DomainObject domReqObject = DomainObject.newInstance(context, strReqID);
			StringList slSelectable = new StringList();
			slSelectable.add(SELECT_BUSINESS_SKILL_ID);
			slSelectable.add(SELECT_RESOURCE_POOL_ID);
			slSelectable.add(SELECT_ATTRIBUTE_START_DATE);
			slSelectable.add(SELECT_ATTRIBUTE_END_DATE);
			slSelectable.add(SELECT_ATTRIBUTE_STANDARD_COST);
			slSelectable.add(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY);
			slSelectable.add(ATTRIBUTE_PROJECT_ROLE);
			slSelectable.add(SELECT_REL_ATTRIBUTE_FTE);
			Map dataMap =  domReqObject.getInfo(context,slSelectable);
			StringList slResourceId = null;
			StringList slResourceFTE = null;
			slResourceId = domReqObject.getInfoList(context,SELECT_PERSON_ID_FROM_RESOURCE_REQUEST);
			slResourceFTE = domReqObject.getInfoList(context,SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);
			StringList slPhaseId = null;
			StringList slPhaseFTE = null;
			slPhaseId = domReqObject.getInfoList(context,ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
			slPhaseFTE = domReqObject.getInfoList(context,ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
			String strRPool = "";
			String strSkillsId = ""; 
			String strPRole = "";
			String strStartDate = "";
			String strEndDate = "";
			String strStandardCost = "";
			String strStandardCostCurrency = "";
			strRPool = ((String)dataMap.get(SELECT_RESOURCE_POOL_ID));
			strSkillsId = ((String)dataMap.get(SELECT_BUSINESS_SKILL_ID));
			strPRole = ((String)dataMap.get(ATTRIBUTE_PROJECT_ROLE));
			strStartDate = ((String)dataMap.get(SELECT_ATTRIBUTE_START_DATE));
			strEndDate = ((String)dataMap.get(SELECT_ATTRIBUTE_END_DATE));
			strStandardCost = ((String)dataMap.get(SELECT_ATTRIBUTE_STANDARD_COST));
			strStandardCostCurrency = ((String)dataMap.get(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY));
			String strRequestId = FrameworkUtil.autoName(context,"type_ResourceRequest","policy_ResourceRequest");
			DomainObject domRequestId = newInstance(context, strRequestId);
			DomainRelationship dmrResourcePlan = DomainRelationship.connect(context,domProjectId,RELATIONSHIP_RESOURCE_PLAN,domRequestId);
			FTE fte = FTE.getInstance(context);
			if(null != dataMap.get(SELECT_REL_ATTRIBUTE_FTE) && !"null".equals(dataMap.get(SELECT_REL_ATTRIBUTE_FTE)) && !"".equals(dataMap.get(SELECT_REL_ATTRIBUTE_FTE)))
			{
				fte = FTE.getInstance(context, (String)dataMap.get(SELECT_REL_ATTRIBUTE_FTE));
			}
			String strFTE = fte.getXML();
			dmrResourcePlan.setAttributeValue(context, ATTRIBUTE_FTE, strFTE);
			connectResourceRequestPhase(context, domRequestId,slPhaseId, slPhaseFTE);
			//Connecting selected requests Person & its FTE values to new request.  
			if(null != slResourceId && !slResourceId.isEmpty())
			{
				for(int m=0;m<slResourceId.size();m++)
				{
					String strResourceId = (String) slResourceId.get(m); 
					String strResourceFTE = (String) slResourceFTE.get(m); 
					DomainObject domResourceObject = DomainObject.newInstance(context, strResourceId);
					StringList slPersonPhaseFTE= new StringList();
					StringList slPersonPhaseId= new StringList();
					if(null != slPhaseId && !slPhaseId.isEmpty())
					{
						for(int nCount=0;nCount<slPhaseId.size();nCount++)
						{
							String strPhaseId= (String)slPhaseId.get(nCount);
							String strPhaseFTERelId = getPhaseFTERelId(context,
									strReqID, strPhaseId);

							String strPersonPhaseFTERelId = getPersonPhaseFTERelId(
									context, strResourceId, strPhaseFTERelId); 
							DomainRelationship personPhaseFTERelDo = DomainRelationship.newInstance(context,strPersonPhaseFTERelId);
							String strPersonPhaseFTEValue = personPhaseFTERelDo.getAttributeValue(context, ATTRIBUTE_FTE);
							slPersonPhaseFTE.add(strPersonPhaseFTEValue);

							String strNewPhaseFTERelId = getPhaseFTERelId(
									context, strRequestId, strPhaseId);
							slPersonPhaseId.add(strNewPhaseFTERelId);						

						}
					}
					connectResourceRequestPhase(context, domResourceObject,slPersonPhaseId, slPersonPhaseFTE);
					connectAllocated(context, domRequestId, strResourceFTE,domResourceObject);
				}
			}

			updateResourcePoolSkillInCopyRequest(context, domRequestId, strRPool, strSkillsId);
			domRequestId.setAttributeValue(context,DomainConstants.ATTRIBUTE_PROJECT_ROLE,strPRole);
			domRequestId.setAttributeValue(context,DomainConstants.ATTRIBUTE_START_DATE,strStartDate);
			domRequestId.setAttributeValue(context,DomainConstants.ATTRIBUTE_END_DATE,strEndDate);
			domRequestId.setAttributeValue(context,ATTRIBUTE_STANDARD_COST,strStandardCost+" "+strStandardCostCurrency);
			return new ResourceRequest(strRequestId);
		} 
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}

	}


	private String getPhaseFTERelId(Context context, String strReqID,
			String strPhaseId) throws FrameworkException {
		//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:start
		String sCommandStatement = "print bus $1 select $2 dump";
		String strPhaseFTERelId =  MqlUtil.mqlCommand(context, sCommandStatement,strReqID, "from["+ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE+"|to.id=="+strPhaseId+"].id"); 
		//PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:End
		return strPhaseFTERelId;
	}


	private void connectAllocated(Context context, DomainObject domFromObject,
			String strResourceFTE, DomainObject domToObject)
	throws FrameworkException, MatrixException 
	{
		String strAllocatedRel = RELATIONSHIP_ALLOCATED;
		DomainRelationship dmrResource = DomainRelationship.connect(context,domFromObject,strAllocatedRel,domToObject);
		FTE ftePerson = FTE.getInstance(context, strResourceFTE);
		String strPersonFTE = ftePerson.getXML();
		dmrResource.setAttributeValue(context, ATTRIBUTE_FTE, strPersonFTE);
	}


	private void connectResourceRequestPhase(Context context,DomainObject domFromObject, StringList slPhaseId, StringList slPhaseFTE)throws Exception, FrameworkException, MatrixException 
	{
		boolean flag=false;
		try{
			if(domFromObject.isKindOf(context, TYPE_RESOURCE_REQUEST))
			{
				String strResourceRequestPhaseRel = ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE;
				if(null != slPhaseId)
				{
					for(int m=0;m<slPhaseId.size();m++)
					{
						String strPhaseId = (String) slPhaseId.get(m); 
						String strPhaseFTE = (String) slPhaseFTE.get(m); 
						DomainObject domPhaseObject = DomainObject.newInstance(context, strPhaseId);
						DomainRelationship dmrPhase = DomainRelationship.connect(context,domFromObject,strResourceRequestPhaseRel,domPhaseObject);
						//FTE ftePhase = FTE.getInstance(strPhaseFTE);
						//String strReqPhaseFTE = ftePhase.getXML();
						dmrPhase.setAttributeValue(context, ATTRIBUTE_FTE, strPhaseFTE);
					}
				}
			}
			else{
				if(null != slPhaseId)
				{
					for(int m=0;m<slPhaseId.size();m++)
					{
						String strResourceId=  domFromObject.getId(context);
						String strPhaseFTERelId = (String) slPhaseId.get(m); 
						String strPersonPhaseFTE = (String) slPhaseFTE.get(m); 
						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						flag = true;     
						String strPersonPhaseFTERelId = ResourceRequest.connectPersonToPhaseFTERel(
								context, strResourceId, strPhaseFTERelId);
						DomainRelationship personPhaseFTERelDo = DomainRelationship.newInstance(context,strPersonPhaseFTERelId);
						personPhaseFTERelDo.setAttributeValue(context, ATTRIBUTE_FTE, strPersonPhaseFTE);
					}
				}

			}

		}
		catch (Exception e) {
			ContextUtil.abortTransaction(context);
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

	private void updateResourcePoolSkillInCopyRequest(Context context,
			DomainObject domRequestId, String strRPool, String strSkillsId)
	throws FrameworkException, Exception 
	{
		if(null!=strRPool && !"".equals(strRPool) && !"null".equalsIgnoreCase(strRPool))
		{
			DomainObject domRPoolObject = DomainObject.newInstance(context, strRPool);
			String strRPoolRel = RELATIONSHIP_RESOURCE_POOL;
			DomainRelationship dmrResourcePool = DomainRelationship.connect(context,domRequestId,strRPoolRel,domRPoolObject);
		}
		//End Modified:1-Jun-2010:di1:R210 PRG:Advanced Resource Planning

		//Need to be a super user to set the Business Skill

		ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
		try
		{
			if("null".equals(strSkillsId) || strSkillsId == null || "".equals(strSkillsId)){
				strSkillsId = EMPTY_STRING;
			}else{
				DomainObject domSkillObject = DomainObject.newInstance(context, strSkillsId);
				String strSkillRel = RELATIONSHIP_RESOURCE_REQUEST_SKILL;
				DomainRelationship dmrResourceSkill = DomainRelationship.connect(context,domRequestId,strSkillRel,domSkillObject);
			}
		}             
		finally
		{
			ContextUtil.popContext(context);
		}
	}
	/**
	 * This method clones the request and connect the new request to Project space.
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */

	public ResourceRequest copyRequestByTimeLine(Context context,String[]args) throws MatrixException 
	{
		try 
		{
			Map programMap = (Map) JPO.unpackArgs(args);

			String strReqID = (String)programMap.get("requestId");
			String strProjectID = (String)programMap.get("projectId");
			String ATTRIBUTE_PROJECT_ROLE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_ProjectRole") + "]";
			String ATTRIBUTE_START_DATE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_StartDate") + "]";
			String ATTRIBUTE_END_DATE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_EndDate") + "]";
			final String SELECT_REL_ATTRIBUTE_FTE =  "to["+ RELATIONSHIP_RESOURCE_PLAN+"].attribute["+ATTRIBUTE_FTE+"]";
			DomainObject domProjectId = DomainObject.newInstance(context, strProjectID);
			DomainObject domReqObject = DomainObject.newInstance(context, strReqID);
			String strRequestId = FrameworkUtil.autoName(context,"type_ResourceRequest","policy_ResourceRequest");
			DomainObject domRequestId = DomainObject.newInstance(context, strRequestId);
			StringList slSelectable = new StringList();
			slSelectable.add(SELECT_BUSINESS_SKILL_ID);
			slSelectable.add(SELECT_RESOURCE_POOL_ID);
			slSelectable.add(ATTRIBUTE_START_DATE);
			slSelectable.add(ATTRIBUTE_END_DATE);
			slSelectable.add(SELECT_ATTRIBUTE_STANDARD_COST);
			slSelectable.add(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY);
			slSelectable.add(ATTRIBUTE_PROJECT_ROLE);
			slSelectable.add(SELECT_REL_ATTRIBUTE_FTE);

			Map dataMap =  domReqObject.getInfo(context,slSelectable);

			StringList slResourceId = null;
			StringList slResourceFTE = null;
			slResourceId = domReqObject.getInfoList(context,SELECT_PERSON_ID_FROM_RESOURCE_REQUEST);
			slResourceFTE = domReqObject.getInfoList(context,SELECT_PERSON_FTE_FROM_RESOURCE_REQUEST);


			String strRPool = "";
			String strSkillsId = ""; 
			String strPRole = "";
			String strStartDate = "";
			String strEndDate = "";
			String strStandardCost = "";
			String strStandardCostCurrency = "";
			String strRelationship = RELATIONSHIP_RESOURCE_PLAN;
			strRPool = ((String)dataMap.get(SELECT_RESOURCE_POOL_ID));
			strSkillsId = ((String)dataMap.get(SELECT_BUSINESS_SKILL_ID));
			strPRole = ((String)dataMap.get(ATTRIBUTE_PROJECT_ROLE));
			strStartDate = ((String)dataMap.get(ATTRIBUTE_START_DATE));
			strEndDate = ((String)dataMap.get(ATTRIBUTE_END_DATE));
			strStandardCost = ((String)dataMap.get(SELECT_ATTRIBUTE_STANDARD_COST));
			strStandardCostCurrency = ((String)dataMap.get(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY));

			//Assigning selected requests FTe values to new request.  
			//Connecting project space and resource request with Resource Plan relationship
			DomainRelationship dmrResourcePlan = DomainRelationship.connect(context,domProjectId,strRelationship,domRequestId);
			FTE fte = FTE.getInstance(context);
			if(null != dataMap.get(SELECT_REL_ATTRIBUTE_FTE) && !"null".equals(dataMap.get(SELECT_REL_ATTRIBUTE_FTE)) && !"".equals(dataMap.get(SELECT_REL_ATTRIBUTE_FTE)))
			{
				// Assigning consistent key for FTE
				fte = FTE.getInstance(context, (String)dataMap.get(SELECT_REL_ATTRIBUTE_FTE));
			}
			String strFTE = fte.getXML();
			dmrResourcePlan.setAttributeValue(context, ATTRIBUTE_FTE, strFTE);

			//Connecting selected requests Person & its FTE values to new request.  
			if(null != slResourceId){
				for(int m=0;m<slResourceId.size();m++){
					String strResourceId = (String) slResourceId.get(m); 
					String strResourceFTE = (String) slResourceFTE.get(m); 
					String strAllocatedRel = RELATIONSHIP_ALLOCATED;
					DomainObject domResourceObject = DomainObject.newInstance(context, strResourceId);
					DomainRelationship dmrResource = DomainRelationship.connect(context,domRequestId,strAllocatedRel,domResourceObject);
					FTE ftePerson = FTE.getInstance(context, strResourceFTE);
					String strPersonFTE = ftePerson.getXML();
					dmrResource.setAttributeValue(context, ATTRIBUTE_FTE, strPersonFTE);
				}
			}
			updateResourcePoolSkillInCopyRequest(context, domRequestId, strRPool, strSkillsId);
			domRequestId.setAttributeValue(context,DomainConstants.ATTRIBUTE_PROJECT_ROLE,strPRole);
			domRequestId.setAttributeValue(context,DomainConstants.ATTRIBUTE_START_DATE,strStartDate);
			domRequestId.setAttributeValue(context,DomainConstants.ATTRIBUTE_END_DATE,strEndDate);
			domRequestId.setAttributeValue(context,ATTRIBUTE_STANDARD_COST,strStandardCost+" "+strStandardCostCurrency);
			return new ResourceRequest(strRequestId);
		} 
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}

	}
	/** 
	 * Committed peoples allocated to Resource request would be members of corresponding project
	 *  if they are not already members.
	 * 
	 * @param context Matrix context object
	 * @param strResourceRequestId Resource Request whose allocated persons can be members of project
	 * @throws Exception if operation fails
	 */
	private void assignPeoplesToProjectSpace(Context context,String strResourceRequestId) throws Exception
	{
		final String SELECT_REQUEST_PROJECT_ID = "to["+RELATIONSHIP_RESOURCE_PLAN+"].from.id";
		final String SELECT_ATTRIBUTE_PROJECT_ROLE = "attribute[" + ATTRIBUTE_PROJECT_ROLE + "]";
		final String SELECT_PROJECT_MEMBER_IDS ="to["+RELATIONSHIP_RESOURCE_PLAN+"].businessobject.from["+RELATIONSHIP_MEMBER+"].to.id";

		DomainObject dmoResourceRequest = newInstance(context,strResourceRequestId);

		//
		// Get resource request's project members
		//
		StringList slBusSelect= new StringList(SELECT_REQUEST_PROJECT_ID);
		slBusSelect.add(SELECT_ATTRIBUTE_PROJECT_ROLE);
		slBusSelect.add(SELECT_ATTRIBUTE_PROJECT_ROLE);
		
		Map projectInfoMap =  dmoResourceRequest.getInfo(context, slBusSelect);
		String strProjectId = (String)projectInfoMap.get(SELECT_REQUEST_PROJECT_ID);
		String strProjectRole = (String)projectInfoMap.get(SELECT_ATTRIBUTE_PROJECT_ROLE);
		ProjectSpace projectSpace = new ProjectSpace(strProjectId);

		StringList objectSelects = new StringList();
		objectSelects.add(SELECT_ID);

		final boolean INCLUDE_ROLE_OR_GROUP=true;
		final boolean SUPER_USER = true;

		MapList mlProjectMembers = projectSpace.getMembers(context, objectSelects, null, null, null, !INCLUDE_ROLE_OR_GROUP, SUPER_USER);

		//
		// Get all the resources of this request
		//

		Map mapResourceList = new HashMap();
		mapResourceList.put("RequestId",strResourceRequestId);
		mapResourceList.put("ResourceIds",null);
		mapResourceList.put("getFTE",true);
		mapResourceList.put("getResourceState",true);

		String[] strMethodArgs = JPO.packArgs(mapResourceList);

		this.setId(strResourceRequestId);
		MapList mlRequestPeople = this.getResourceInfo(context,strMethodArgs);

		List lstNewMembers = new ArrayList();

		for (Iterator itrRequestPeople = mlRequestPeople.iterator(); itrRequestPeople.hasNext();) 
		{
			Map mapRequestPerson = (Map) itrRequestPeople.next();
			String strRequestPersonId = (String)mapRequestPerson.get("ResourceId");

			boolean isAlreadyMember = false;
			for (Iterator itrProjectMembers = mlProjectMembers.iterator(); itrProjectMembers.hasNext();) 
			{
				Map mapProjectMember = (Map) itrProjectMembers.next();
				String strProjectMemberId = (String)mapProjectMember.get(SELECT_ID);

				if (strProjectMemberId.equals(strRequestPersonId))
				{
					isAlreadyMember = true;
					break;
				}
			}

			if (!isAlreadyMember)
			{
				lstNewMembers.add(strRequestPersonId);
			}
		}

		// [Modified::Jan 21, 2011:S4E:R211:IR-043589V6R2012::Start]
		if (lstNewMembers.size() > 0) 
		{
			String[] memberIds = (String[])lstNewMembers.toArray(new String[lstNewMembers.size()]);
			if(ProgramCentralUtil.isNotNullString(strProjectRole))
			{
				Map attriMap = new HashMap();
				attriMap.put(ATTRIBUTE_PROJECT_ROLE, strProjectRole);
				Map personMap = new HashMap();
				for(int nCount=0;nCount<memberIds.length;nCount++)
				{
					personMap.put(memberIds[nCount],attriMap);
				}
				// [MODIFIED::Apr 25, 2011:S4E:R211:	IR-104179V6R2012x::Start] 
				//Using Push-Pop context because Resource manager needs to modify only the relationship attribute "Project Role",
				//Resource manager has fromConnect access on object ProjectSpace which does not allow to modify attribute "Project Role" on relationship Member.
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				try {
					projectSpace.addMembers(context, personMap);
				}
				finally{
					ContextUtil.popContext(context);
				}
			}else{
				try{
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					projectSpace.addMembers(context, memberIds);
				}finally{
					ContextUtil.popContext(context);
			}
	}
		}
	}

	/**
	 * returns project owner data in ResourceRequestSummary table
	 * 
	 * @param context Matrix context object
	 * @param args packed arguments from table method
	 * @return vector holding all project owner data
	 * @throws MatrixException
	 */
	public Vector getColumnProjectOwnerData(Context context, String[] args) throws Exception
	{
		try 
		{
			//Create result vector
			Vector vecResult = new Vector();
			// Get object list information from packed arguments
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			final String SELECT_RESOURCE_REQUEST_PROJECT_OWNER = "to["+ RELATIONSHIP_RESOURCE_PLAN +"].from.owner";
			Iterator objectListIterator = objectList.iterator();
			Map mapObject = null;
			String strObjectId = "";
			StringList slSelects = new StringList();
			String strProjectOwner = "";
			Map mapObjectInfo = new HashMap(); 

			while (objectListIterator.hasNext())
			{
				mapObject = (Map) objectListIterator.next();
				strProjectOwner = (String)mapObject.get(SELECT_RESOURCE_REQUEST_PROJECT_OWNER);
				strProjectOwner = PersonUtil.getFullName(context,strProjectOwner);

				vecResult.add(strProjectOwner);
			}

			return vecResult;
		}
		catch (Exception exp) 
		{
			throw exp;
		}
	}

	public Map getMinMaxRequestDates(Context context,String strProjectId) throws Exception {
		try {
			Map mapDate = new HashMap();
			DomainObject dmoProject = DomainObject.newInstance(context, strProjectId);
			String busTypeSelect = SELECT_TYPE ;
			boolean isTypeProjectSpace = false;
			isTypeProjectSpace = dmoProject.isKindOf(context, TYPE_PROJECT_SPACE);
			String strRelationshipType = "";
			String strType = "";
			boolean strFrom = false;
			boolean strTo = false;

			if(isTypeProjectSpace){
				strRelationshipType = RELATIONSHIP_RESOURCE_PLAN;
				strType = TYPE_RESOURCE_REQUEST;
				strTo =  true;
			}else{
				strRelationshipType = RELATIONSHIP_RESOURCE_POOL;
				strType = TYPE_RESOURCE_REQUEST;
				strFrom = true;
			}

			final String SELECT_ATTRIBUTE_REQUEST_START_DATE = "attribute[" + ATTRIBUTE_START_DATE  + "]";
			final String SELECT_ATTRIBUTE_REQUEST_FINISH_DATE = "attribute[" + ATTRIBUTE_END_DATE  + "]";

			StringList busSelect = new StringList();
			busSelect.add(DomainConstants.SELECT_ID);
			busSelect.add(SELECT_ATTRIBUTE_REQUEST_START_DATE);
			busSelect.add(SELECT_ATTRIBUTE_REQUEST_FINISH_DATE);


			StringList relSelect = new StringList();
			String whereClause = "" ;
			if(!isTypeProjectSpace)
			{
				whereClause = "("+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_REQUESTED+" || "+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_PROPOSED+" || "+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_COMMITTED+" )";
			}
			MapList mlRequests = dmoProject.getRelatedObjects(
					context,
					strRelationshipType,
					strType,
					busSelect,
					relSelect,
					strFrom,
					strTo,
					(short)1,
					whereClause,
					DomainConstants.EMPTY_STRING,0);
			Map mapRequestInfo = null;
			String strRequestId = "";
			String strRequestStartDate = "";
			String strRequestEndDate = "";
			String strchkReqStartDate = "";
			String strchkReqEndDate = "";

			Date dtStartDate = null;
			Date dtEndDate = null;

			Date dtChkStartDate = null;
			Date dtChkEndDate = null;

			for (Iterator iterRequest = mlRequests.iterator(); iterRequest .hasNext();)
			{
				mapRequestInfo = (Map) iterRequest.next();
				strRequestId = (String) mapRequestInfo.get(SELECT_ID);
				strRequestStartDate = (String) mapRequestInfo.get(SELECT_ATTRIBUTE_REQUEST_START_DATE);
				strRequestEndDate = (String) mapRequestInfo.get(SELECT_ATTRIBUTE_REQUEST_FINISH_DATE);
				if((null == strchkReqStartDate  || "null" .equals(strchkReqStartDate) || "".equals(strchkReqStartDate))){
					strchkReqStartDate = strRequestStartDate;
					dtChkStartDate =  eMatrixDateFormat.getJavaDate(strchkReqStartDate);
				}
				if((null == strchkReqEndDate  || "null" .equals(strchkReqEndDate) || "".equals(strchkReqEndDate))){
					strchkReqEndDate = strRequestEndDate;
					dtChkEndDate =  eMatrixDateFormat.getJavaDate(strchkReqEndDate);
				}

				if(null != strRequestStartDate  && !"null" .equals(strRequestStartDate) && !"".equals(strRequestStartDate)){
					dtStartDate = eMatrixDateFormat.getJavaDate(strRequestStartDate);
					if(dtStartDate.before(dtChkStartDate)){
						dtChkStartDate = dtStartDate;
					}
				}
				if(null != strRequestEndDate  && !"null" .equals(strRequestEndDate) && !"".equals(strRequestEndDate)){
					dtEndDate = eMatrixDateFormat.getJavaDate(strRequestEndDate);
					if(dtEndDate.after(dtChkEndDate)){
						dtChkEndDate = dtEndDate;
					}
				}
			}
			mapDate.put("ReqStartDate",dtChkStartDate);
			mapDate.put("ReqEndDate",dtChkEndDate);

			return mapDate;
		} 
		catch (Exception exp)
		{
			throw exp;
		}

	}
	/**
	 * Returns the name values for Resource request & full name value for person object in PMCResourceRequestSummaryTable
	 * 
	 * @param context Matrix Context object
	 * @param args String array
	 * @return vector holding mentioned values
	 * @throws Exception if operation fails
	 */

	public Vector getColumnRequestNameData(Context context,String[] args) throws Exception
	{
		try
		{
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map mapRowData = null;
			String strObjectId = "";
			String strObjectName = "";
			DomainObject dmoObject;
			String strFullName ="";
			for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) 
			{
				mapRowData = (Map) itrObjects.next();
				strObjectId =(String) mapRowData.get(SELECT_ID);
				dmoObject = DomainObject.newInstance(context,strObjectId);
				StringBuffer sbRequestLink = new StringBuffer();
				strObjectName = (String) dmoObject.getInfo(context,SELECT_NAME);

				if(dmoObject.isKindOf(context, TYPE_RESOURCE_REQUEST))
				{
					vecResult.add(strObjectName);
				}
				else if(dmoObject.isKindOf(context, TYPE_PERSON))
				{
					strFullName = PersonUtil.getFullName(context,strObjectName);
					vecResult.add(strFullName);
				}
				else if(dmoObject.isKindOf(context, TYPE_PROJECT_SPACE))
				{
					vecResult.add(strObjectName);
				}
			}
			return vecResult;
		}
		catch (Exception ex)
		{
			throw new MatrixException(ex);
		}

	}

	/**
	 * This method decides whether the command is should be enabled or not once the project is completed.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean isCommandEnabled(Context context,String[]args) throws Exception {

		//[Added::Jan 5, 2011:s4e:R211:IR-088687V6R2012::Start] 
		String sLanguage = context.getSession().getLanguage();
		PersonUtil contextUser =  new PersonUtil();
		String strPreferredCurrency = contextUser.getCurrency(context);
		if("As Entered".equals(strPreferredCurrency) || null==strPreferredCurrency || "null".equalsIgnoreCase(strPreferredCurrency) || "".equals(strPreferredCurrency))
		{
			return false;	
		}
		Map programMap = (Map) JPO.unpackArgs(args);
		String strProjectID = (String)programMap.get("objectId");
		DomainObject dmoObject = DomainObject.newInstance(context,strProjectID);
		if (dmoObject.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_TEMPLATE) &&
				!dmoObject.getOwner(context).getName().equalsIgnoreCase(context.getUser())) {
			return false;
		}
		boolean isCommandEnabled = isProjectSpaceStateInvalid(context, dmoObject );
		if(isCommandEnabled)
		{
			return false;
		}
		else
		{
			String strPlanPrefernece = getResourcePlanPreference(context, dmoObject);
			if(RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strPlanPrefernece))
			{
				return false;
			}
		}
		return true;
	}


	/**
	 * @param context
	 * @param dmoObject
	 * @param isCommandEnabled
	 * @return
	 * @throws FrameworkException
	 */
	private boolean isProjectSpaceStateInvalid(Context context,
			DomainObject dmoObject)
	throws FrameworkException {
		String isProjectState = dmoObject.getInfo(context, SELECT_CURRENT);
		if(dmoObject.isKindOf(context, TYPE_PROJECT_SPACE)){
			boolean isProjectCompleted = STATE_PROJECT_SPACE_COMPLETE.equals(isProjectState) || 
			STATE_PROJECT_SPACE_ARCHIVE.equals(isProjectState) || //Added:nr2:PRG:R211:IR-071354V6R2012 	
			ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(isProjectState) ||
			ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(isProjectState)
			;
			return  isProjectCompleted;
		}
		else
		{
			return  false;
		}
	}

	/* Decides if the certain command is to be shown for the Resource Request summary table of Resource Plan
	 * Used for : command PMCDeleteRequest,PMCRemoveAssignment,PMCSubmitRequest,PMCCopyRequest
	 * 
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isResourcePlanRequestCommand (Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strObjId = (String) programMap.get("objectId");
		String isRMB = (String) programMap.get("isRMB");
		if(null != isRMB && "true".equalsIgnoreCase(isRMB.trim())){
			String strRmbTableRowId = (String) programMap.get("rmbTableRowId");
			StringList slRowIds= FrameworkUtil.splitString(strRmbTableRowId,"|");
			if(null != slRowIds && slRowIds.size() >1){
				String strParentId = (String)slRowIds.get(2);
				if(null != strParentId  && !"null".equals(strParentId) && !"".equalsIgnoreCase(strParentId.trim())){
					strObjId = strParentId;
				}
			}
		}
		DomainObject dmoObject = DomainObject.newInstance(context,strObjId);
		boolean isCommandEnabled= true;
		if(dmoObject.isKindOf(context, TYPE_PROJECT_SPACE)){
			boolean isProjectCompleted = false;
			isProjectCompleted = PolicyUtil.checkState(context,strObjId,STATE_PROJECT_SPACE_COMPLETE,PolicyUtil.GE);
			boolean isProjectCancelled = false;
			isProjectCancelled = PolicyUtil.checkState(context,strObjId,ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD,PolicyUtil.GT);
			if(isProjectCompleted || isProjectCancelled){
				isCommandEnabled = false;
			}else{
				isCommandEnabled = true;
			}
			return isCommandEnabled;
		}else{
			return false;
		}
	}

	/* Decides whether to display Copy/Delete Request commandes on Request propeties page or not  depending on the user role PL/RM 
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isResourceRequestPropertiesPageCommand (Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strRequestObjId = (String) programMap.get("objectId");
		boolean isCommandEnabled= true;
		String resourcePoolId = (String) programMap.get("resourcePoolId");
		String projectID = (String) programMap.get("projectID");

		if(null != resourcePoolId){
			isCommandEnabled = false;
			return isCommandEnabled;
		}else{
			if(null != projectID && !"null".equals(projectID) && !"".equals(projectID)){
				DomainObject dmoObject = DomainObject.newInstance(context,projectID);
				boolean isProjectCompleted = false;
				isProjectCompleted = PolicyUtil.checkState(context,projectID,STATE_PROJECT_SPACE_COMPLETE,PolicyUtil.GE);

				if(isProjectCompleted){
					isCommandEnabled = false;
				}else{
					isCommandEnabled = true;
				}
			}
			return isCommandEnabled;
		}
	}


	/* Decides whether to display Edit link on rpoperties page of resource request
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isRequestEditable (Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strRequestObjId = (String) programMap.get("objectId");
		boolean isCommandEnabled= true;
		String resourcePoolId = (String) programMap.get("resourcePoolId");

		if(null != resourcePoolId){
			isCommandEnabled = false;
			return isCommandEnabled;
		}else{
			DomainObject dmoRequestObject = DomainObject.newInstance(context,strRequestObjId);
			String strRequestState = dmoRequestObject.getInfo(context,SELECT_CURRENT);
			if("Requested".equals(strRequestState) || "Proposed".equals(strRequestState) || "Committed".equals(strRequestState)){
				isCommandEnabled = false;
			}else{
				isCommandEnabled = true;
			}
			return isCommandEnabled;
		}
	}
	/**
	 * This function notifies the request submission
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id of request
	 * @throws Exception if operation fails
	 * @since AEF 9.5.1.3
	 */
	public void notifyDeleteRequest(Context context, String[] args)
	throws Exception
	{
		Map mapResourceRequestIds = (Map) JPO.unpackArgs(args);
		String[]  slResourceRequestId = (String[])mapResourceRequestIds.get("ResourceRequestIds"); 
		int nSetStateSuccess ;
		String languageStr = context.getSession().getLanguage();
		String strResourceRequestId = "";

		for(int i =0;i<slResourceRequestId.length;i++){
			StringList slManagers = new StringList();
			strResourceRequestId = slResourceRequestId[i];
			DomainObject dmoRequest = DomainObject.newInstance(context,strResourceRequestId);
			StringList busSelects = new StringList();
			busSelects.add(SELECT_RESOURCE_POOL_ID);
			busSelects.add(SELECT_NAME);
			Map objMap = dmoRequest.getInfo(context, busSelects);
			String strResourcePoolID          = (String) objMap.get(SELECT_RESOURCE_POOL_ID);
			DomainObject dmoResourcePool = DomainObject.newInstance(context,strResourcePoolID);
			String busSelectsManager = SELECT_RESOURCE_MANAGER_NAME;
			slManagers = dmoResourcePool.getInfoList(context, busSelectsManager);
			String strRequestName = (String) objMap.get(SELECT_NAME);
			StringList mailToList = new StringList(1);
			StringList mailCcList = new StringList(1);
			for(int m =0; m<slManagers.size(); m++){
				mailToList.addElement(slManagers.get(m));
			}
			i18nNow i18n = new i18nNow();
			String strLanguage = context.getSession().getLanguage();
			String strMsg = i18n.GetString("emxProgramCentralStringResource",strLanguage,"emxProgramCentral.ResourceRequest.emxDeleteRequestNotifyMembers.Message");
			String sMailSubject = "emxProgramCentral.ResourceRequest.emxDeleteRequestNotifyMembers.Subject";
			String companyName = null;
			sMailSubject  = emxProgramCentralUtilClass.getMessage(
					context, sMailSubject, null, null, companyName);

			//get the mail message
			String sMailMessage = strMsg + strRequestName;

			sMailMessage  = emxProgramCentralUtilClass.getMessage(
					context, sMailMessage, null, null, companyName);
			Person per = new Person();
			String rpeUserName = (per.getPerson(context)).toString();
			StringList slPersonTokens = FrameworkUtil.split(
					rpeUserName, ".");
			String strPersonName = (String) slPersonTokens.get(1);
			MailUtil.setAgentName(context, strPersonName);

			mailCcList.addElement(strPersonName);

			MailUtil.sendMessage(context, mailToList, mailCcList, null,
					sMailSubject, sMailMessage , null);
		}
	}


	/**
	 * Gets the data for the column "FTE" for table "PMCResourceRequestCreateWBSTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Vector getWBSColumnFTEValue(Context context, String[] args)  throws Exception 
	{
		try 
		{
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramList");
			String strObjectId = (String)programMap.get("objectId");
			String strPlanPreference = RESOURCE_PLAN_PREFERENCE_TIMELINE;
			if(null==strObjectId || "".equals(strObjectId) || "Null".equalsIgnoreCase(strObjectId))
			{
				strObjectId = (String)paramMap.get("objectId");
			}
			boolean istypeProjectSpace = false;
			if(null!=strObjectId && !"".equals(strObjectId) && !"Null".equalsIgnoreCase(strObjectId))
			{
				DomainObject dObjProject = DomainObject.newInstance(context, strObjectId);
				strPlanPreference = getResourcePlanPreference(context, dObjProject);
				istypeProjectSpace = dObjProject.isKindOf(context, TYPE_PROJECT_SPACE);
			}
			MapList objectList = (MapList) programMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			Map mapObject = null;
			Object objFTEData = null;
			Map mapFTEData = new HashMap();
			String strFTEData = "";
			int nObjectListSize = objectList.size() ; 
			for (int i =0;i<nObjectListSize ;i++)
			{
				mapObject = (Map) objectListIterator.next();
				objFTEData = (Object)mapObject.get("FTEToBeDisplayed");

				Map columnMap = (Map) programMap.get("columnMap");
				String strColumnName = (String) columnMap.get(SELECT_NAME);
				if (strColumnName.indexOf("-") == -1)
				{
					throw new MatrixException("Invalid FTE column name '"+strColumnName+"'");
				}
				String[] strSplitColumnName = strColumnName.split("-");
				String strFTEText = null;
				if(!istypeProjectSpace || RESOURCE_PLAN_PREFERENCE_TIMELINE.equals(strPlanPreference))
				{
					int nMonth = Integer.parseInt(strSplitColumnName[0]);
					int nYear = Integer.parseInt(strSplitColumnName[1]);

					int iFTEData = 0;

					if(objFTEData instanceof String)
					{
						strFTEData = (String)mapObject.get("FTEToBeDisplayed");
					}else{
						mapFTEData = (Map)mapObject.get("FTEToBeDisplayed");
						NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, false);
						if(null!=mapFTEData.get(nMonth+"-"+nYear))
						{
							strFTEData = String.valueOf(numberFormat.format((Double)mapFTEData.get(nMonth+"-"+nYear)));
						}
						else
						{
							strFTEData ="0";
						}
					}
					if(null == (strFTEData) || "null".equals(strFTEData) || "".equals(strFTEData)){
						strFTEData = String.valueOf(0);
					}
					Map mapObjectInfo = null;
					strFTEText = "<input type='textbox' name='"+strColumnName+i+"' value='"+strFTEData+"'>"; 
				}
				else if(RESOURCE_PLAN_PREFERENCE_PHASE.equals(strPlanPreference))
				{
					String strPhaseName = strSplitColumnName[0];
					String strPhaseOId =  strSplitColumnName[1];
					int iFTEData = 0;
					if(objFTEData instanceof String)
					{
						strFTEData = (String)mapObject.get("FTEToBeDisplayed");
					}else{
						mapFTEData = (Map)mapObject.get("FTEToBeDisplayed");
						strFTEData = String.valueOf((Integer)mapFTEData.get(strPhaseName));
					}
					if(null == (strFTEData) || "null".equals(strFTEData) || "".equals(strFTEData)){
						strFTEData = String.valueOf(0);
					}
					Map mapObjectInfo = null;
					strFTEText = "<input type='textbox' name='PhaseOID-"+strPhaseOId+"' value='"+strFTEData+"'>"; 
				}
				vecResult.add(strFTEText);
			}
			return vecResult;
		} 
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}
	/**
	 * checks whether the logged in user is resource manager of the Resource pool
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args olds the following input arguments:
	 *        0 - String containing the object id of request
	 * @return Boolean true,if logged in user is resource manager of the resource pool
	 * @throws Exception
	 */
	public Boolean checkResourceManagerAccess(Context context, String[] args)
	throws Exception
	{ 
		boolean displayMenu = false;
		Map mapResourceRequestIds = (Map) JPO.unpackArgs(args);
		String strPersonId = PersonUtil.getPersonObjectID(context);
		String strResourcePoolID = (String)mapResourceRequestIds.get("objectId");

		if(null == strResourcePoolID){
			Map requestMap = (Map)mapResourceRequestIds.get("requestMap");
			strResourcePoolID = (String)requestMap.get("objectId");
		}
		emxResourcePool_mxJPO resourcePool = new emxResourcePool_mxJPO(context,args);
		MapList lstResourceManagers = new MapList();
		resourcePool.setId(strResourcePoolID);
		lstResourceManagers = resourcePool.getResourceManagers(context, new String[] {DomainObject.SELECT_ID}); 
		for (Iterator iter = lstResourceManagers.iterator(); iter.hasNext();) {
			Map mapResourceManagerDetail = (Map) iter.next();
			if(strPersonId.equals(mapResourceManagerDetail.get(DomainObject.SELECT_ID))){
				displayMenu = true;
				break;
			}
		}
		String strPMCResourcePoolFilter = (String)mapResourceRequestIds.get("PMCResourcePoolFilter");
		String strPMCResourcePoolResourceManagerFilter = (String)mapResourceRequestIds.get("PMCResourcePoolResourceManagerFilter");

		String strParentResourcePoolId = strResourcePoolID;
		String strRelPattern = RELATIONSHIP_DIVISION + "," + RELATIONSHIP_COMPANY_DEPARTMENT + "," + RELATIONSHIP_ORGANIZATION_PLANT + "," + RELATIONSHIP_SUBSIDIARY;
		resourcePool.setId(strResourcePoolID);
		MapList lstAllRelatedResourcePools  = new MapList();
		MapList lstAllRelatedResourcePools1  = new MapList();

		lstAllRelatedResourcePools  = resourcePool.getAllRelatedResourcePools(context, strPMCResourcePoolFilter, strPMCResourcePoolResourceManagerFilter,strParentResourcePoolId, strRelPattern, true, false, true);

		lstAllRelatedResourcePools1  = resourcePool.getAllRelatedResourcePools(context, strPMCResourcePoolFilter, strPMCResourcePoolResourceManagerFilter,strParentResourcePoolId, strRelPattern, false, true, true);
		if(displayMenu == false){
			for (Iterator iterPool = lstAllRelatedResourcePools.iterator(); iterPool.hasNext();) {
				Map mapResourcePoolDetail = (Map) iterPool.next();
				strResourcePoolID = (String)mapResourcePoolDetail.get(DomainObject.SELECT_ID);
				resourcePool.setId(strResourcePoolID);
				lstResourceManagers = resourcePool.getResourceManagers(context, new String[] {DomainObject.SELECT_ID}); 
				for (Iterator iter = lstResourceManagers.iterator(); iter.hasNext();) {
					Map mapResourceManagerDetail = (Map) iter.next();
					if(strPersonId.equals(mapResourceManagerDetail.get(DomainObject.SELECT_ID))){
						displayMenu = true;
						break;
					}
				}
			}

			for (Iterator iterPool = lstAllRelatedResourcePools1.iterator(); iterPool.hasNext();) {
				Map mapResourcePoolDetail = (Map) iterPool.next();
				strResourcePoolID = (String)mapResourcePoolDetail.get(DomainObject.SELECT_ID);
				resourcePool.setId(strResourcePoolID);
				lstResourceManagers = resourcePool.getResourceManagers(context, new String[] {DomainObject.SELECT_ID}); 
				for (Iterator iter = lstResourceManagers.iterator(); iter.hasNext();) {
					Map mapResourceManagerDetail = (Map) iter.next();
					if(strPersonId.equals(mapResourceManagerDetail.get(DomainObject.SELECT_ID))){
						displayMenu = true;
						break;
					}
				}
			}
		}
		return displayMenu;
	}


	/**
	 * Generates required FTE columns dynamically for CreateResourceRequest from WBS
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains requestMap 
	 * @return The MapList object containing definitions about new columns for showing FTE
	 * @throws Exception if operation fails
	 */
	public MapList getAddResourceDynamicFTEColumn (Context context, String[] args) throws Exception
	{
		Map mapProgram = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)mapProgram.get("requestMap");
		MapList mlColumns = new MapList();
		Map mapColumn = null;
		Map mapSettings = null;
		// 
		// Following code gets business objects information 
		//
		String strObjectId = (String) requestMap.get("objectId");

		String strRequestId = (String) requestMap.get("requestId");
		DomainObject dmoProject= DomainObject.newInstance(context, strObjectId);
		boolean istypeProjectSpace = false;
		istypeProjectSpace = dmoProject.isKindOf(context, TYPE_PROJECT_SPACE);
		String strPlanPreference = getResourcePlanPreference(context, dmoProject);
		if(!istypeProjectSpace || (null != strPlanPreference && RESOURCE_PLAN_PREFERENCE_TIMELINE.equalsIgnoreCase(strPlanPreference)))
		{
			DomainObject dmoRequest= DomainObject.newInstance(context, strRequestId);
			String strType = dmoRequest.getInfo(context, SELECT_TYPE);

			Calendar calStartDate = Calendar.getInstance();
			Calendar calFinishDate = Calendar.getInstance();  

			int nMonth  = 0;
			int nYear= 0;
			int nFinishMonth = 0;
			int nFinishYear=0;
			int nNumberOfMonths = 0;

			int nFinishWeek = 0;
			int nStartWeek = 0;
			int nNumberOfWeeks = 0;
			int totalnumWeeks = 0;

			final String SELECT_ATTRIBUTE_REQUEST_START_DATE = "attribute[" + ATTRIBUTE_START_DATE  + "]";
			final String SELECT_ATTRIBUTE_REQUEST_FINISH_DATE = "attribute[" + ATTRIBUTE_END_DATE  + "]";


			FTE fteRequest = FTE.getInstance(context);
			String strTimeframeConfigName = fteRequest.getTimeframeConfigName();
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ATTRIBUTE_REQUEST_START_DATE);
			slBusSelect.add(SELECT_ATTRIBUTE_REQUEST_FINISH_DATE);

			String strMonth = "";
			String strFTESpanInMonths = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.ResourcePlanTable.FTESpanInMonths") ;
			int nFTESpanInMonths = 0;

			Map mapObjInfo = dmoRequest.getInfo(context, slBusSelect);
			String strStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_REQUEST_START_DATE);
			String strFinishtDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_REQUEST_FINISH_DATE); 

			Date dtStartDate = eMatrixDateFormat.getJavaDate(strStartDate);
			Date dtFinishDate = eMatrixDateFormat.getJavaDate(strFinishtDate);

			calStartDate.setTime(dtStartDate);
			calFinishDate.setTime(dtFinishDate);
			nMonth = calStartDate.get(Calendar.MONTH)+1; //0=January
			nYear = calStartDate.get(Calendar.YEAR);

			nFinishMonth = calFinishDate.get(Calendar.MONTH)+1;//0=January
			nFinishYear = calFinishDate.get(Calendar.YEAR);

			MapList mlMonthYearList = null;   
			if (strStartDate != null && !"".equals(strStartDate) && !"null".equals(strStartDate)&& strFinishtDate != null && !"".equals(strFinishtDate) && !"null".equals(strFinishtDate))
			{
				mlMonthYearList = fteRequest.getTimeframes(dtStartDate,dtFinishDate);

			}
			StringList slMonthYear = new StringList();
			int iMonthYearSize = mlMonthYearList.size();
			Map mapFTE = new HashMap();
			MapList mlFTE = new MapList();
			Map mapObject = new HashMap();
			Iterator objectListIterator = mlMonthYearList.iterator();
			for (int i =0;i<iMonthYearSize ;i++)
			{
				mapObject = (Map) objectListIterator.next();
				//strFTEData = 
				int nTimeFrame = (Integer)mapObject.get("timeframe");
				int nTimeYear = (Integer)mapObject.get("year");

				strMonth = getMonthName(context,nTimeFrame);
				// Related Objects - Name column
				mapColumn = new HashMap();
				mapColumn.put("name", nTimeFrame+"-"+nTimeYear );
				if("Monthly".equals(strTimeframeConfigName)){ 
					strMonth = getMonthName(context,nTimeFrame);
					mapColumn.put("label", strMonth+"-"+nTimeYear);
				}else if("Weekly".equals(strTimeframeConfigName)){
					mapColumn.put("label", "Wk"+nTimeFrame +"-"+nTimeYear);
				}else{
					mapColumn.put("label", "Q"+nTimeFrame +"-"+nTimeYear);  
				}

				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxResourceRequest");
				mapSettings.put("function","getWBSColumnFTEValue");
				mapSettings.put("Column Type","programHTMLOutput");
				mapSettings.put("submit","true");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);
			}
		}
		else if(RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strPlanPreference))
		{
			String strTotal = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ResourceRequest.ColumnHeader.Total", context.getLocale().getLanguage());
			boolean isFilterCost = false;
			mlColumns = getDynamicPhaseColumns(context,  requestMap,isFilterCost, true,
					strTotal,dmoProject, false);

		}

		return mlColumns;
	}  

	//Added:12-Jan-2010:vf2:R209 RG:IR-013972
	/**
	 * This method creates the Business Skill Textbox and chooser for displaying
	 * in the Create Resource Request from WBS Table
	 * 
	 * @param context
	 *            The matrix context object
	 * @param args
	 *            The arguments, it contains objectList and paramList maps
	 * @throws Exception
	 *             if operation fails           
	 */

	public Vector getBusinessSkillTextBox(Context context, String[] args)
	throws Exception {
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Vector vBusinessnm = new Vector();
			String strLanguage = context.getSession().getLanguage();
			String strClear = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Clear", strLanguage);
			Vector vBusinessSkill = new Vector();
			StringBuffer strbuffer=null;
			for (int idx = 0; idx < objectList.size(); idx++) {
				strbuffer = new StringBuffer();
				strbuffer.append("<input type='textbox'  readonly='true' value=''  name='txtBusinessSkill");
				strbuffer.append(idx);
				strbuffer.append("'></input><input type='hidden' value='' name='txtBusinessSkillId");
				strbuffer.append(idx);
				strbuffer.append("'></input><input type='button' name='btnBusinessSkill' size='200' value='...' ");
				strbuffer.append("onClick=\"javascript:selectResourcePoolSkill('txtBusinessSkillId"+idx+"','txtBusinessSkill"+idx+"','txtBusinessSkillId"+idx+"',document.forms[0].txtOrganizationId"+idx+");\"");
				strbuffer.append("></input>");
				strbuffer.append("<a href=\"javascript:basicClear('txtBusinessSkill"+ idx +"');javascript:basicClear('txtBusinessSkillId"+ idx +"');\">");
				strbuffer.append(strClear);
				strbuffer.append("</a>");				
				vBusinessSkill.add(strbuffer.toString());
			}
			return vBusinessSkill;
		} catch (Exception e) {
			throw e;
		}
	} 
	//Added:9-Mar-2010:s4e:R209 PRG:IR-013248
	/**
	 * This method returns Request related to specific (context) resource pool and which are in only state "Requested" or "Proposed" state.
	 * 
	 * @param context
	 *            The matrix context object
	 * @param returns
	 *            StringList containing ResourceRequests related to context ResourcePool
	 * @param args
	 *            The arguments, it contains programMap
	 * @throws Exception
	 *             if operation fails           
	 */

	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getIncludeOIDForResourcePoolRequestSearch(Context context, String[] args)throws Exception
	{
		StringList slFinalResourceReqList = new StringList();
		try 
		{
			Map programMap = (Map) JPO.unpackArgs(args);    
			String strResourcePoolId ="";   
			String strResourceRequestId =""; 
			strResourcePoolId = (String)programMap.get("ResourcePoolId");
			DomainObject ResourcePoolDbo = DomainObject.newInstance(context,strResourcePoolId);
			String strRelationshipPattern = RELATIONSHIP_RESOURCE_POOL;
			String strTypePattern = TYPE_RESOURCE_REQUEST;
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ID);
			boolean getFrom = false;
			boolean getTo = true;
			short recurseToLevel = 1;
			String strBusWhere = "";
			String strRelWhere = "";
			//where clause to exclude requests in state Create/Rejected/Committed.
			strBusWhere = ("!"+("("+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_REJECTED+")" +
					"||("+SELECT_CURRENT+"=="+DomainConstants.STATE_RESOURCE_REQUEST_CREATE+")"));
			MapList mlResourcePoolRequestList = ResourcePoolDbo.getRelatedObjects(context,
					strRelationshipPattern, //pattern to match relationships
					strTypePattern, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Objects.
					null, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getTo, //get To relationships
					getFrom, //get From relationships
					recurseToLevel, //the number of levels to expand, 0 equals expand all.
					strBusWhere, //where clause to apply to objects, can be empty ""
					strRelWhere,0); //where clause to apply to relationship, can be empty ""
			Map mapResourceRequestIds = null;
			Iterator itrmlResourcePoolRequestList = mlResourcePoolRequestList.iterator();
			while (itrmlResourcePoolRequestList.hasNext())
			{
				mapResourceRequestIds = (Map) itrmlResourcePoolRequestList.next();
				strResourceRequestId = (String)mapResourceRequestIds.get(SELECT_ID);
				slFinalResourceReqList.add(strResourceRequestId);                            
			} 
			return slFinalResourceReqList;             
		}
		catch (Exception e)
		{       
			throw e;
		}
	}
	//End:9-Mar-2010:s4e:R209 PRG:IR-013248

	public Vector getStandardCost(Context context,String[]args) throws Exception 
	{
		try 
		{
			Map programMap 			= (Map) JPO.unpackArgs(args);
			MapList objectList 		= (MapList) programMap.get("objectList");
			Vector	currencyVector	= new Vector();
			Map requestMap = (Map)programMap.get("paramList");

			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			String strCurrency = emxProgramCentralUtil_mxJPO.getCurrencySymbol(context, locale,ProgramCentralUtil.getCurrencyUnit(context));
			StringBuffer	currencyBuffer	= new StringBuffer();
			for (int idx = 0; idx < objectList.size(); idx++) {
				currencyBuffer	= new StringBuffer();
				currencyBuffer.append("<input type=\"textbox\"  value=\""+ emxProgramCentralUtil_mxJPO.getFormattedNumberValue(context,0)+ "\"  name=\"txtStandardCost"+idx+"\" ");
				currencyBuffer.append("/>");
				currencyBuffer.append(XSSUtil.encodeForHTML(context,(String)strCurrency));
				currencyBuffer.append("<input type=\"hidden\" value=\"\" name=\"txtStandardCost"+idx+"\"/>");
				currencyVector.add(currencyBuffer.toString());
			}
			return currencyVector;

		} catch (Exception e) {			
			throw e;
		}
	}

	//Added:24-May-2010:di1:R210 PRG:Advanced Resource Planning
	public int triggerCheckResourcePool(Context context,String[] args)
	throws Exception 
	{
		String sPromoteMsg = ResourceRequest.triggerCheckResourcePoolMessage(context, args);
		if(ProgramCentralUtil.isNotNullString(sPromoteMsg))
		{
			sPromoteMsg = ProgramCentralUtil.getMessage(context,sPromoteMsg, null, null, null);	
			MqlUtil.mqlCommand(context, "notice " + sPromoteMsg); //PRG:RG6:R213:Mql Injection:Static Mql:24-Oct-2011
			return TRIGGER_FAILURE;
		}
		return TRIGGER_SUCCESS;
	} 

	/*
	 * This method shows the combobox for Resource Plan Preference Values in the Resource Plan Preference Web form
	 */
	public String getResourcePlanPreferences(Context context,String[]args) throws Exception 
	{
		try 
		{
			Map programMap = (Map) JPO.unpackArgs(args);      	    
			Map requestMap = (Map)programMap.get("requestMap");
			String strProjectID = (String)requestMap.get("objectId");
			AttributeType atrResourcePlanPreference = new AttributeType(ResourceRequest.ATTRIBUTE_RESOURCE_PLAN_PREFRENCE);
			atrResourcePlanPreference.open(context);
			StringList strList = atrResourcePlanPreference.getChoices(context);
			//Modified:4-Mar-11:s2e:R211:PRG:IR-098670V6R2012
			String sLanguage = context.getSession().getLanguage();
			StringList slList = i18nNow.getAttrRangeI18NStringList(atrResourcePlanPreference.toString(), strList, sLanguage);
			//End:4-Mar-11:s2e:R211:PRG:IR-098670V6R2012
			atrResourcePlanPreference.close(context);
			String sResourcePlanPreference = null;            
			DomainObject dmoObject = DomainObject.newInstance(context,strProjectID);
			String strResourcePlaneValue=null;
			strResourcePlaneValue = dmoObject.getAttributeValue(context, ResourceRequest.ATTRIBUTE_RESOURCE_PLAN_PREFRENCE);            
			StringBuffer output = new StringBuffer();
			if(isResourcePlanPreferenceEnabled(context, args))				
			{			
				output.append("<select name=\"ResourcePlanPreference\">");

				for (int i = 0; i < strList.size(); i++) {
					//Modified:09-Mar-11:hp5:R211:PRG:IR-098670V6R2012
					sResourcePlanPreference = (String) strList.get(i);
					String i18nsResourcePlanPreference = (String)slList.get(i);
					if(null!=sResourcePlanPreference && sResourcePlanPreference.equalsIgnoreCase(strResourcePlaneValue))
					{
						output.append("<option value='"+XSSUtil.encodeForHTML(context,sResourcePlanPreference)+"' selected='selected'>"+i18nsResourcePlanPreference+"</option>");
					}else{
						output.append("<option value='"+XSSUtil.encodeForHTML(context,sResourcePlanPreference)+"'>"+i18nsResourcePlanPreference+"</option>");
					}
				}
				output.append("</select>");
			}
			else
			{
				String strKey ="emxFramework.Range.Resource_Plan_Preference.";
				String i18nstrResourcePlaneValue = EnoviaResourceBundle.getProperty(context, "Framework", 
						strKey+strResourcePlaneValue, sLanguage);
				output.append("<label>");
				output.append(i18nstrResourcePlaneValue);
				output.append("</label>");
				//End:09-Mar-11:hp5:R211:PRG:IR-098670V6R2012
			}

			String strOuput = output.toString();
			return strOuput;           
		} 
		catch (Exception exp)
		{
			throw exp;
		}
	}

	/**
	 * This method decides whether the command is should be enabled or not once the project is completed.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean isResourcePlanPreferenceEnabled(Context context,String[]args) throws Exception {

		Map programMap = (Map) JPO.unpackArgs(args);

		String strProjectID = (String)programMap.get("objectId");
		if(null==strProjectID || "".equals(strProjectID) || "null".equalsIgnoreCase(strProjectID))
		{
			Map paramMap = (Map)programMap.get("paramMap");
			strProjectID = (String)paramMap.get("objectId");
		}
		DomainObject dmoObject = DomainObject.newInstance(context,strProjectID);
		boolean isCommandEnabled= true;

		if(dmoObject.isKindOf(context, TYPE_PROJECT_SPACE)){
			boolean isProjectCompleted = false;
			isProjectCompleted = PolicyUtil.checkState(context,strProjectID,STATE_PROJECT_SPACE_COMPLETE,PolicyUtil.GE); 		  
			if(isProjectCompleted){
				isCommandEnabled = false;
			}
		}else{
			isCommandEnabled = true;
		}

		if(isCommandEnabled)
		{
			String strSelect="from["+RELATIONSHIP_RESOURCE_PLAN+"]"; 		  
			String strResult=dmoObject.getInfo(context, strSelect); 		  
			isCommandEnabled = "false".equalsIgnoreCase(strResult)? true: false;
		}

		return isCommandEnabled;
	}

	/**
	 * This method sets the Resource Plan Preference attribute value of the project space.
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return void
	 * @throws Exception if operation fails
	 */

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void setResourcePlanPreference(Context context, String[] args)  throws Exception 
	{    	    	
		try 
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			String strProjectSpaceId = (String) requestMap.get("objectId");
			DomainObject dboObj=null;
			if(null!=strProjectSpaceId &!"".equals(strProjectSpaceId) && !"null".equalsIgnoreCase(strProjectSpaceId))
			{
				String strResourcePlanPreference = (String)requestMap.get("ResourcePlanPreference");
				if(ProgramCentralUtil.isNotNullString(strResourcePlanPreference)){
					dboObj=DomainObject.newInstance(context, strProjectSpaceId);
					dboObj.setAttributeValue(context, ResourceRequest.ATTRIBUTE_RESOURCE_PLAN_PREFRENCE, strResourcePlanPreference);
				}
			}

		} 
		catch (Exception exp)
		{
			throw exp;
		}
	}

	//End Added:24-May-2010:di1:R210 PRG:Advanced Resource Planning
	//Added:31-May-2010:s4e:R210 PRG:ARP
	/**
	 * This method is used to get FTE value and Total FTE for request with the standard cost for FTE if Costs/FTE filter is selected as "Costs"
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *        
	 * @returns vector of all FTE values of Requests 
	 * @throws Exception if the operation fails	 * 
	 */
	public Vector getColumnFTECostValue(Context context, Map programMap)  throws Exception {
		try {
			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			PersonUtil contextUser =  new PersonUtil();
			String userCompanyID =contextUser.getUserCompanyId(context);
			Map conversionMap = new HashMap();
			conversionMap = ProgramCentralUtil.getCurrencyConversionMap(context, userCompanyID);
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			NumberFormat format = ProgramCentralUtil.getNumberFormatInstance(2, true);
			Vector vecResult = new Vector();
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			Map columnMap = (Map) programMap.get("columnMap");
			String strCurrencyFilter =(String) paramList.get("PMCResourceRequestCurrencyFilter");
			long nReqDays = 0;

			int nMonth = 0;
			int nYear  =0;
			String strColumnName = (String) columnMap.get(SELECT_NAME);

			if (strColumnName.indexOf("-") == -1 && !"Total".equals(strColumnName))
			{
				throw new MatrixException("Invalid FTE column name '"+strColumnName+"'");
			}
			else if(!"Total".equals(strColumnName)){
				String[] strSplitColumnName = strColumnName.split("-");
				nMonth = Integer.parseInt(strSplitColumnName[0]);
				nYear = Integer.parseInt(strSplitColumnName[1]);
			}
			Map mapObjectInfo = null;

			FTE fte = FTE.getInstance(context);
			double fteValue = 0d;
			double totalRowCost=0d;  
			double total=0d;
			String strTotalRowCost = "";
			String strTotalCost = "";

			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String strResourceReqId = (String)mapObjectInfo.get(SELECT_ID);
				DomainObject doResourceReq = DomainObject.newInstance(context,strResourceReqId);
				String strFteValue ="";				
				String SELECT_IS_PROJECT_SPACE = "type.kindof["+TYPE_PROJECT_SPACE+"]";
				StringList slSelects = new StringList();
				slSelects.add(DomainConstants.SELECT_ID);
				slSelects.add(SELECT_IS_PROJECT_SPACE);
				slSelects.add(SELECT_IS_RESOURCE_REQUEST);
				slSelects.add(SELECT_ATTRIBUTE_START_DATE);
				slSelects.add(SELECT_ATTRIBUTE_END_DATE);
				slSelects.add(SELECT_ATTRIBUTE_END_DATE);
				slSelects.add("to[Resource Plan].attribute[FTE].value");
				Map mapObjInfo = doResourceReq.getInfo(context,slSelects);

				String sIsProjectSpaceType = (String) mapObjInfo.get(SELECT_IS_PROJECT_SPACE);
				String sIsResourceRequestType = (String) mapObjInfo.get(SELECT_IS_RESOURCE_REQUEST);
				boolean isProjectSpace = ProgramCentralUtil.isNotNullString(sIsProjectSpaceType) && "true".equalsIgnoreCase(sIsProjectSpaceType)?true:false;
				boolean isResourceRequest = ProgramCentralUtil.isNotNullString(sIsResourceRequestType) && "true".equalsIgnoreCase(sIsResourceRequestType)?true:false;
				if(isProjectSpace){					
					MapList mpChildObjList = new MapList();
					int count = 0;
					for (Iterator itrTableRows1 = objectList.iterator(); itrTableRows1.hasNext();)
					{
						Map mapObjectInf = (Map) itrTableRows1.next();
						mpChildObjList = (MapList)mapObjectInf.get("children");
					}
					String strObjId = (String) paramList.get("objectId");
					Map mpNewProgramMap = new HashMap();
					mpNewProgramMap.put("objectList", mpChildObjList);
					mpNewProgramMap.put("paramList", paramList);
					mpNewProgramMap.put("columnMap", columnMap);
					Vector vec = null;

					vec = getColumnFTECostValue(context,mpNewProgramMap);	

					BigDecimal dblValue = new BigDecimal(0);
					for (Iterator iterator = vec.iterator(); iterator.hasNext();) {
						String strValue = (String) iterator.next();

						if(!"".equals(strValue) && null != strValue){
							BigDecimal bdValue = emxProgramCentralUtilBase_mxJPO.getNormalizedCurrencyValue(context, locale, strValue);
							strValue = ""+bdValue;
							dblValue = dblValue.add(bdValue);
						}
					}
					BigDecimal bdValue = emxProgramCentralUtilBase_mxJPO.getNormalizedCurrencyValue(context, locale, ""+dblValue);
					vecResult.add(format.format(Task.parseToDouble(""+bdValue)));
				}
				else
				{
					if(isResourceRequest)
					{
						Map selectMap = new HashMap();
						selectMap.put("conversionMap",conversionMap);
						selectMap.put("currencyFilter",strCurrencyFilter);
						selectMap.put("resourceReqId",strResourceReqId);

						double standardCost = getStandardCostValue(context, selectMap);  
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							String  strReqStartDate = (String)mapObjInfo.get(SELECT_ATTRIBUTE_START_DATE);
							String  strReqEndDate = (String)mapObjInfo.get(SELECT_ATTRIBUTE_END_DATE);
							Date reqStartDate = new Date(strReqStartDate);
							Date reqEndDate = new Date(strReqEndDate);

							Date fromDate = fte.getStartDate(strColumnName);
							Date toDate = fte.getEndDate(strColumnName);
							nReqDays = ProgramCentralUtil.computeDuration(fromDate,toDate);
							String strFTE = (String)mapObjInfo.get("to[Resource Plan].attribute[FTE].value");
							if(ProgramCentralUtil.isNotNullString(strFTE))
							{
								fte = FTE.getInstance(context, strFTE);
								fteValue = fte.getFTE(nYear, nMonth);                
								if("Hours".equalsIgnoreCase(numberofPeopleUnit))
								{
									fteValue=fteValue*standardCost;
								}
								else
								{
									fteValue=fteValue*nReqDays*WORKING_HOURS_PER_DAY*standardCost;
								}
								if(null!=mapObjectInfo.get("totalRowCost"))
								{
									totalRowCost=(Double)mapObjectInfo.get("totalRowCost");
								}
								else
								{
									totalRowCost=0d;                                                        
								}
								totalRowCost=totalRowCost+fteValue;
								mapObjectInfo.put("totalRowCost",totalRowCost);					
							}
							total =total+fteValue;
							vecResult.add(format.format(fteValue));
						}
					}
					else
					{
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							vecResult.add("0");
						}			
					}
					if("Total".equalsIgnoreCase(strColumnName))
					{
						if(null!=mapObjectInfo.get("totalRowCost"))
						{
							totalRowCost=(Double)mapObjectInfo.get("totalRowCost");
							strTotalRowCost=format.format(totalRowCost);						
							vecResult.add(strTotalRowCost);
							mapObjectInfo.put("totalRowCost",0.0);											
						}
						else
						{
							vecResult.add("0");
						}

					}					
				}			
			}			
			return vecResult;
		}
		catch (Exception e) {
			throw new MatrixException(e);
		}
	}

	/**
	 * This method is used to get FTE value and Total FTE for request without the standard cost if Costs/FTE filter is selected as "FTE"
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *        
	 * @returns vector of all FTE values of Requests 
	 * @throws Exception if the operation fails	 * 
	 */
	public Vector getColumnOnlyFTEValue(Context context, Map programMap)  throws Exception {
		try {
			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			Vector vecResult = new Vector();
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			Map columnMap = (Map) programMap.get("columnMap");
			int nMonth = 0;
			int nYear  =0;
			NumberFormat format = ProgramCentralUtil.getNumberFormatInstance(2, true);
			String strColumnName = (String) columnMap.get(SELECT_NAME);

			if (strColumnName.indexOf("-") == -1 && !"Total".equals(strColumnName))
			{
				throw new MatrixException("Invalid FTE column name '"+strColumnName+"'");
			}
			else if(!"Total".equals(strColumnName)){
				String[] strSplitColumnName = strColumnName.split("-");
				nMonth = Integer.parseInt(strSplitColumnName[0]);
				nYear = Integer.parseInt(strSplitColumnName[1]);
			}	
			Map mapObjectInfo = null;
			FTE fte = FTE.getInstance(context);
			double fteValue = 0d;
			double totalRowCost=0d;  
			double total=0d;
			String strTotalRowCost = "";
			String strTotalCost = "";
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next(); 
				String strObjectId = (String)mapObjectInfo.get(SELECT_ID);
				fteValue=0d; 
				DomainObject doResourceReq = DomainObject.newInstance(context,strObjectId);
				if(doResourceReq.isKindOf(context, TYPE_PROJECT_SPACE))
				{
					MapList mpChildObjList = new MapList();
					int count1 = 0;
					for (Iterator itrTableRows1 = objectList.iterator(); itrTableRows1.hasNext();)
					{
						Map mapObjectInf = (Map) itrTableRows1.next();
						mpChildObjList = (MapList)mapObjectInf.get("children");
					}
					String strObjId = (String) paramList.get("objectId");
					Map mpNewProgramMap = new HashMap();
					mpNewProgramMap.put("objectList", mpChildObjList);
					mpNewProgramMap.put("paramList", paramList);
					mpNewProgramMap.put("columnMap", columnMap);
					Vector vec = null;

					vec = getColumnOnlyFTEValue(context,mpNewProgramMap);	

					BigDecimal dblValue = new BigDecimal(0);
					for (Iterator iterator = vec.iterator(); iterator.hasNext();) {
						String strValue = (String) iterator.next();

						BigDecimal bdValue = emxProgramCentralUtilBase_mxJPO.getNormalizedCurrencyValue(context, locale, strValue);
						strValue = ""+bdValue;
						if(!"".equals(strValue) && null != strValue){
							dblValue = dblValue.add(bdValue);
						}
					}
					String strFTEValue = ""+dblValue;
					if(null != strFTEValue){
						vecResult.add(format.format(Task.parseToDouble(strFTEValue)));
					}else{
						vecResult.add("0");
					}

				}
				else
				{
					if(doResourceReq.isKindOf(context, TYPE_RESOURCE_REQUEST))
					{
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							String strFTE = doResourceReq.getInfo(context, "to[Resource Plan].attribute[FTE].value");
							if(ProgramCentralUtil.isNullString(strFTE)){
								try{
									ProgramCentralUtil.pushUserContext(context);
									strFTE = doResourceReq.getInfo(context, "to[Resource Plan].attribute[FTE].value");	
								}finally{
									ProgramCentralUtil.popUserContext(context);
								}
							}
							
							if(ProgramCentralUtil.isNotNullString(strFTE))
							{
								fte = FTE.getInstance(context, strFTE);
								fteValue = fte.getFTE(nYear, nMonth);  
								if(null!=mapObjectInfo.get("totalRowCost"))
								{
									totalRowCost=(Double)mapObjectInfo.get("totalRowCost");
								}
								else
								{
									totalRowCost=0d;                                                        
								}
								totalRowCost=totalRowCost+fteValue;
								mapObjectInfo.put("totalRowCost",totalRowCost);  
							}
							total =total+fteValue;
							vecResult.add(format.format(fteValue));
						}
					}
					else if (doResourceReq.isKindOf(context, TYPE_PERSON))
					{
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							String relId = (String)mapObjectInfo.get("id[connection]");
							DomainRelationship relationship = DomainRelationship.newInstance(context, relId);
							String strFTE = relationship.getAttributeValue(context, ATTRIBUTE_FTE);
							if(ProgramCentralUtil.isNotNullString(strFTE))
							{
								fte = FTE.getInstance(context, strFTE);
								fteValue = fte.getFTE(nYear, nMonth);  
								if(null!=mapObjectInfo.get("totalRowCost"))
								{
									totalRowCost=(Double)mapObjectInfo.get("totalRowCost");
								}
								else
								{
									totalRowCost=0d;                                                        
								}
								totalRowCost=totalRowCost+fteValue;
								mapObjectInfo.put("totalRowCost",totalRowCost);  
							}
							total =total+fteValue;
							vecResult.add(format.format(fteValue));
						}
					}
					else
					{
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							strTotalCost=format.format(total);
							vecResult.add(strTotalCost);
							if(null!= mapObjectInfo.get("totalRowCost"))
							{
								totalRowCost=(Double)mapObjectInfo.get("totalRowCost");                                                         
							}
							else
							{
								totalRowCost=0d;                                                                                
							}
							totalRowCost=totalRowCost+total;
							mapObjectInfo.put("totalRowCost",totalRowCost);
						}
					}
					if("Total".equalsIgnoreCase(strColumnName))
					{
						if(null!=mapObjectInfo.get("totalRowCost"))
						{
							totalRowCost=(Double)mapObjectInfo.get("totalRowCost");
							strTotalRowCost=format.format(totalRowCost);
							mapObjectInfo.put("totalRowCost",0.0);
						}
						vecResult.add(strTotalRowCost);
					}
				}
			}
			return vecResult;
		}
		catch (Exception e) {
			throw new MatrixException(e);
		}
	}
	//End:31-May-2010:s4e:R210 PRG:ARP
	//Added:31-May-2010:s4e:R210 PRG:ARP
	/**
	 * This method is used to get standard value of resource request as per the currency
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *        
	 * @returns double value of standard cost of resource request. 
	 * @throws Exception if the operation fails	 * 
	 */
	public double getStandardCostValue(Context context, Map selectMap)  throws Exception 
	{
		try{
			String displayCurrency = (String)selectMap.get("currencyFilter");
			if(ProgramCentralUtil.isNullString(displayCurrency)){
				displayCurrency = PersonUtil.getCurrency(context);
			}
			String strResourceReqId = (String)selectMap.get("resourceReqId");
			boolean isPoolCostConsidered = ((null!=(String)selectMap.get("isPoolCostConsidered")&&"true".equalsIgnoreCase((String)selectMap.get("isPoolCostConsidered")))? true:false); 
			double standardCost = 0d;
			double standardCostConverted;
			String strResourcePoolStdCost = "from["+RELATIONSHIP_RESOURCE_POOL+"].to."+SELECT_ATTRIBUTE_STANDARD_COST;    
			String strResourcePoolStdCostCurrency = "from["+RELATIONSHIP_RESOURCE_POOL+"].to."+SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY;
			String strConnectedProjectId = "from[" + ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE + "].to.to[" + ProgramCentralConstants.RELATIONSHIP_SUBTASK + "].from.id";
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_PROJECT_SPACE_ID);
			slBusSelect.add(SELECT_ATTRIBUTE_STANDARD_COST);
			slBusSelect.add(strResourcePoolStdCost);
			slBusSelect.add(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY);
			slBusSelect.add(strResourcePoolStdCostCurrency);
			slBusSelect.add(strConnectedProjectId);
			DomainObject reqDo = DomainObject.newInstance(context, strResourceReqId);
			
			Map requestInfoMap = null;
			String baseCurrency = DomainObject.EMPTY_STRING;
			try{
				ProgramCentralUtil.pushUserContext(context);
				requestInfoMap =  reqDo.getInfo(context,slBusSelect);
				
				String projectId = (String)requestInfoMap.get(SELECT_PROJECT_SPACE_ID);
				if(ProgramCentralUtil.isNullString(projectId)){
					projectId = (String)requestInfoMap.get(strConnectedProjectId); 	
				}
				
				baseCurrency = Currency.getBaseCurrency(context, projectId);
			}finally{
				ProgramCentralUtil.popUserContext(context);
			}

			String strReqStandardCost = (String)requestInfoMap.get(SELECT_ATTRIBUTE_STANDARD_COST);
			String strPoolStandardCost = (String)requestInfoMap.get(strResourcePoolStdCost);
			String strRequestCostCurrency= (String)requestInfoMap.get(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY);
			String strPoolStandardCostCurrency = (String)requestInfoMap.get(strResourcePoolStdCostCurrency);

			double reqStandardCost = Task.parseToDouble(strReqStandardCost);
			double poolStandardCost = 0.0;
			standardCost=reqStandardCost;
			if(isPoolCostConsidered){
				if(ProgramCentralUtil.isNotNullString(strPoolStandardCost)){
					poolStandardCost = Task.parseToDouble(strPoolStandardCost);
				}else{
					poolStandardCost=0.0;
				}

				if("".equals(strReqStandardCost) || reqStandardCost==0.0){
					if(poolStandardCost == 0.0){
						standardCost=0.0;
					}else{
						standardCost=poolStandardCost;
					}
				}
			}
			
			return Currency.convert(context, standardCost, baseCurrency, displayCurrency, new Date(), false);
		}catch (Exception e){
			throw new MatrixException(e);
		}
	}
	/**
	 * This method is used to get Range values for CurrencyFilter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *        
	 * @returns Map containing Range values for Currency Exchange 
	 * @throws Exception if the operation fails	 * 
	 */
	public Map getResourceRequestCurrencyFilterRange(Context context, String[] args) throws Exception
	{
		try {
			emxFinancialItemBase_mxJPO financial = new emxFinancialItemBase_mxJPO(context, args);
			return financial.getBenefitOrBudgetCurrencyFilterRange(context, args);
		} catch (Exception e) {
			throw new MatrixException(e);           
		}
	}
	//End:28-May-2010:s4e:R210 PRG:ARP
	//Added:28-May-2010:s4e:R210 PRG:ARP
	/**
	 * This method is used to get Range values for CostFTE Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *        
	 * @returns Map containing Range values for Assignee role 
	 * @throws Exception if the operation fails	 * 
	 */
	public Map getCostFTEFilterRange(Context context, String[] args) throws Exception
	{
		Map rangeMap = new HashMap();
		StringList slDisplayList = new StringList();
		StringList slOriginalList = new StringList();

		String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
		slOriginalList.add(numberofPeopleUnit);    	
		String resourceCost=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.ResourceCost");
		slOriginalList.add(resourceCost);

		numberofPeopleUnit = getFteHourCostsValue(context,numberofPeopleUnit);
		slDisplayList.add(numberofPeopleUnit);

		resourceCost = getFteHourCostsValue(context,resourceCost);
		slDisplayList.add(resourceCost);

		rangeMap.put("field_choices",slOriginalList);
		rangeMap.put("field_display_choices",slDisplayList);
		return  rangeMap;

	}
	//End:28-May-2010:s4e:R210 PRG:ARP

	//Added:28-May-2010:s4e:R210 PRG:ARP
	/**
	 * This method is used to get Range values for Phase/Timline Filter
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 	 *        
	 * @returns Map containing Range values for Assignee role 
	 * @throws Exception if the operation fails	 * 
	 */
	public Map getPhaseTimeLineFilterRange(Context context, String[] args) throws Exception
	{
		Map rangeMap = new HashMap();
		StringList slDisplayList = new StringList();
		StringList slOriginalList = new StringList();
		//Added:2-Jun-2010:di1:R210:PRG:Advance Resource Planning
		Map programMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		String strProjectID = (String)requestMap.get("objectId");		
		DomainObject dmoObject = DomainObject.newInstance(context,strProjectID);
		String strRerourcePlanValue = getResourcePlanPreference(context, dmoObject); 
		String strLanguage = (String)requestMap.get("languageStr");
		String viewByPhase = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.ResourceRequest.PhaseTimelineFilter.Phase", strLanguage);
		String viewByTimeline = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.ResourceRequest.PhaseTimelineFilter.Timeline", strLanguage);
		if(!RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strRerourcePlanValue))
		{
			slOriginalList.add(RESOURCE_PLAN_PREFERENCE_TIMELINE);
			slOriginalList.add(RESOURCE_PLAN_PREFERENCE_PHASE);
			slDisplayList.add(viewByTimeline);
			slDisplayList.add(viewByPhase);
		}
		else
		{
			slOriginalList.add(RESOURCE_PLAN_PREFERENCE_PHASE);
			slOriginalList.add(RESOURCE_PLAN_PREFERENCE_TIMELINE);
			slDisplayList.add(viewByPhase);
			slDisplayList.add(viewByTimeline);
		}
		//End Added:2-Jun-2010:di1:R210:PRG:Advance Resource Planning
		rangeMap.put("field_choices",slOriginalList);
		rangeMap.put("field_display_choices",slDisplayList);
		return  rangeMap;

	}
	//End:28-May-2010:s4e:R210 PRG:ARP
	//Added:28-May-2010:s4e:R210 PRG:ARP
	public boolean showCurrencyFilter(Context context, String args[]) throws Exception
	{
		boolean blAccess = false;
		Map inputMap = (Map)JPO.unpackArgs(args);
		String costsFTEFilterValue = (String)inputMap.get("PMCResourceRequestCostsFTEFilter");
		String resourcePlanCostsFTEFilterValue = (String)inputMap.get("PMCResourcePlanTemplateCostFTEFilter");

		if(null==costsFTEFilterValue && null==resourcePlanCostsFTEFilterValue )
		{
			blAccess = false;
		}
		else{

			if("Costs".equalsIgnoreCase(costsFTEFilterValue) ||"Costs".equalsIgnoreCase(resourcePlanCostsFTEFilterValue))
			{
				blAccess=true;
			}
			else{
				blAccess = false;
			}
		}		

		return blAccess;
	}
	//End:28-May-2010:s4e:R210 PRG:ARP


	/**
	 * Decides the availability of Edit command button for resource request summary table.
	 * @param context the ENOVIA <code>Context</code> user.
	 * @param args request parameters.
	 * @return true that indicates availability of the edit command button.
	 * @throws Exception if operation fails.
	 */
	public boolean isModeCommandEnabled(Context context,String[]args) throws Exception {

		Map programMap = (Map) JPO.unpackArgs(args);
		String strProjectID = (String)programMap.get("objectId");		

		String filterCurrency = (String)programMap.get("PMCResourceRequestCurrencyFilter");
		//START:Added For IR-205506V6R2014
		String  prefferedCurrency = PersonUtil.getCurrency(context);
		if(ProgramCentralUtil.isNullString(prefferedCurrency) || prefferedCurrency.equals("As Entered") || prefferedCurrency.equals("Unassigned")){
			return false;	
		}
		//END:Added For IR-205506V6R2014
		if(ProgramCentralUtil.isNotNullString(filterCurrency) && !prefferedCurrency.equals(filterCurrency))
			return false;

		DomainObject dmoObject = DomainObject.newInstance(context,strProjectID);		
		boolean isCommandEnabled= true;
		if(dmoObject.isKindOf(context, TYPE_PROJECT_SPACE)){
			boolean isProjectCompleted = false;
			isProjectCompleted = PolicyUtil.checkState(context,strProjectID,STATE_PROJECT_SPACE_COMPLETE,PolicyUtil.GE);
			boolean isProjectCancelled = false;
			isProjectCancelled = PolicyUtil.checkState(context,strProjectID,ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD,PolicyUtil.GT);
			if(isProjectCompleted || isProjectCancelled){
				isCommandEnabled = false;
			}
		}else{
			isCommandEnabled = true;
		}
		if(isCommandEnabled)
		{			
			String PMCResourceRequestPhaseTimelineFilter = (String)programMap.get("PMCResourceRequestPhaseTimelineFilter");
			if(ProgramCentralUtil.isNotNullString(PMCResourceRequestPhaseTimelineFilter))
			{
				String strRerourcePlanValue = getResourcePlanPreference(context, dmoObject);
				if(PMCResourceRequestPhaseTimelineFilter.equalsIgnoreCase(strRerourcePlanValue))
				{
					isCommandEnabled = true;
				}
				else
				{
					isCommandEnabled = false;
				}
			}
		}

		return isCommandEnabled;
	}


	/**
	 * @param context
	 * @param dmoObject
	 * @return
	 * @throws FrameworkException
	 */
	private String getResourcePlanPreference(Context context, DomainObject dmoObject)
	throws FrameworkException {
		String strRerourcePlanValue = dmoObject.getAttributeValue(context, ResourceRequest.ATTRIBUTE_RESOURCE_PLAN_PREFRENCE);
		return strRerourcePlanValue;
	}

	public String getNoOfPeople(Context context,String[]args) throws Exception {

		StringBuffer strBfReturn = new StringBuffer(); 
		Map programMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");	
		String strProjectID= (String)requestMap.get("objectId");
		String languageStr = context.getSession().getLanguage();
		String phaseName=ProgramCentralConstants.EMPTY_STRING;
		DomainObject dmoProject = DomainObject.newInstance(context, strProjectID);
		StringList slBusSelects = new StringList();
		slBusSelects.add(SELECT_ID);
		slBusSelects.add(SELECT_NAME);
		MapList mlPhaseList = ResourcePlanTemplate.getPhasesForResourceRequestView(context,
				dmoProject, slBusSelects);
		String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
	     	 
		phaseName = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Type.Phase", languageStr);
		String strUnit = getFteHourCostsValue(context, numberofPeopleUnit);
		String strPhaseName=null;
		String strPhaseId=null;
		strBfReturn.append("<tr><td><label>");
		strBfReturn.append(XSSUtil.encodeForHTML(context,phaseName));
		strBfReturn.append("</label>");
		strBfReturn.append("</td><td><label>");
		strBfReturn.append(strUnit);
		strBfReturn.append("</label>");
		strBfReturn.append("</td></tr>");
		for(Iterator iterRequest = mlPhaseList.iterator(); iterRequest .hasNext();)
		{
			Map tempMap=(Map)iterRequest.next();
			strPhaseName=(String)tempMap.get(SELECT_NAME);
			strPhaseId=(String)tempMap.get(SELECT_ID);
			strBfReturn.append("<tr><td><label>");
			strBfReturn.append(XSSUtil.encodeForHTML(context,strPhaseName));
			strBfReturn.append("</label>");
			strBfReturn.append("</td><td>");
			strBfReturn.append("<input type=\"textbox\" name=\"");
			strBfReturn.append("PhaseOID"+"-"+XSSUtil.encodeForHTML(context,strPhaseId));
			strBfReturn.append("\" id=\"");
			strBfReturn.append("PhaseOID"+XSSUtil.encodeForHTML(context,strPhaseId));
			//MODIFIED:PA4:25-Aug-2011:IR-123555V6R2012x
			strBfReturn.append("\" value=\"1\" />");
			strBfReturn.append("</td></tr>");
		}

		return strBfReturn.toString();
	}

	/**
	 * This method decides whether the command 'CreateResourceRequestByPhase' should be enabled or not once the project is completed.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean isCreateByPhaseEnabled(Context context,String[]args) throws Exception 
	{
		//[Added::Jan 5, 2011:s4e:R211:IR-088687V6R2012::Start] 
		String sLanguage = context.getSession().getLanguage();
		PersonUtil contextUser =  new PersonUtil();
		String strPreferredCurrency = contextUser.getCurrency(context);
		if("As Entered".equals(strPreferredCurrency) || null==strPreferredCurrency || "null".equalsIgnoreCase(strPreferredCurrency) || "".equals(strPreferredCurrency))
		{
			return false;	
		}
		//[Added::Jan 5, 2011:s4e:R211:IR-088687V6R2012::End] 
		Map programMap = (Map) JPO.unpackArgs(args);
		String strProjectID = (String)programMap.get("objectId");
		DomainObject dmoObject = DomainObject.newInstance(context,strProjectID);
		String strRerourcePlanValue = getResourcePlanPreference(context,
				dmoObject);
		if(!RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strRerourcePlanValue))
		{
			return false;
		}
		if(dmoObject.isKindOf(context, TYPE_PROJECT_SPACE)){
			boolean isProjectCompleted = isProjectSpaceStateInvalid(context, dmoObject);
			if(isProjectCompleted){
				return false;
			}
		}
		StringList isPhaseSubtask = dmoObject.getInfoList(context, ResourcePlanTemplate.SELECT_IS_ANY_SUBTASK_PHASE);
		if(null!=isPhaseSubtask && isPhaseSubtask.size()>0)
		{
			if("false".equalsIgnoreCase((String)isPhaseSubtask.get(0)))
			{
				return false;
			}			
		}
		return true;
	}




	//End Added:31-May-2010:di1:R210:PRG:Advance Resource Planning
	/**
	 * Gets resource plan table data for resource request
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableResourcePlanTemplateRequestData(Context context, String[] args) throws Exception
	{
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strRelId = (String) programMap.get("relId");

			String strLanguage = (String)programMap.get("languageStr");
			StringList relSelect = new StringList();
			MapList mlRequests = new MapList();
			String [] strRelIds = new String[1];
			strRelIds[0] = strRelId;
			ResourcePlanTemplate planTemplate = new ResourcePlanTemplate();
			StringList slRequestIdList = planTemplate.getRequestIdListFromResourcePlanTemplate(
					context, strRelIds);
			BusinessObjectWithSelectList resourceRequestObjWithSelectList = null;
			BusinessObjectWithSelect bows = null;
			String[] strRequestIds = null;
			if(null!=slRequestIdList)
			{
				Map mapResourceRequestTranslatedStates = getTranstedResourceRequestPolicyName(
						context, strLanguage);
				Map mapRequestInfo = null;
				strRequestIds = new String[slRequestIdList.size()];
				slRequestIdList.copyInto(strRequestIds);
				StringList busSelect = new StringList();
				busSelect.add(SELECT_ID);
				busSelect.add(SELECT_NAME);
				busSelect.add(SELECT_CURRENT);
				busSelect.add(SELECT_RESOURCE_POOL_NAME);
				busSelect.add(SELECT_RESOURCE_POOL_ID);
				busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
				busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_NAME);
				busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
				resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,busSelect);
				String strRequestId = null;
				String strRequestName = null;
				String strCurrentState = null;
				for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
				{
					mapRequestInfo = new HashMap();
					bows = itr.obj();
					strRequestId = bows.getSelectData(SELECT_ID);
					mapRequestInfo.put(SELECT_ID,strRequestId);
					strRequestName = bows.getSelectData(SELECT_NAME);
					mapRequestInfo.put(SELECT_NAME,strRequestName);
					strCurrentState = bows.getSelectData(SELECT_CURRENT);
					mapRequestInfo.put("CurrentState", mapResourceRequestTranslatedStates.get(strCurrentState));
					if(null!=bows.getSelectData(SELECT_RESOURCE_POOL_NAME))
					{
						mapRequestInfo.put("Organization", bows.getSelectData(SELECT_RESOURCE_POOL_NAME));
						mapRequestInfo.put("OrganizationID", bows.getSelectData(SELECT_RESOURCE_POOL_ID));
					}
					Map phaseFTEMap = new HashMap();
					StringList slPhaseIdList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
					if(null!=slPhaseIdList && slPhaseIdList.size()>0)
					{
						for(int i=0; i<slPhaseIdList.size(); i++)
						{
							String strPhaseId = (String)slPhaseIdList.get(i);
							String strFTE = (String)(bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE)).get(i);
							phaseFTEMap.put("PhaseOID"+"-"+strPhaseId, strFTE);
						}
						mapRequestInfo.put("FTE",phaseFTEMap);
					}
					mapRequestInfo.put("ViewBy", RESOURCE_PLAN_PREFERENCE_PHASE);
					mlRequests.add(mapRequestInfo);
				}
			}
			return mlRequests ;
		} 
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}

	/**
	 * This method is used to refresh the table after clicking on Apply button.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns HashMap.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2012
	 * @author MS9
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map postProcessRefreshTable(Context context, String[] args) throws MatrixException 
	{
		try{
			Map mapReturn = new HashMap();
			mapReturn.put("Action","refresh");
			return mapReturn;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * Gets the data for the column "FTE" for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Vector getColumnFTEPhaseData(Context context, String[] args)  throws Exception {
		try { 
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramMap = (Map) programMap.get("paramList");         
			if(showCurrencyFilter(context,JPO.packArgs(paramMap))){
				vecResult=getColumnFTEPhaseCostData(context,programMap);
			}
			else{
				vecResult=getColumnFTEPhaseOnlyFTEData(context,programMap);
			}
			return vecResult;

		} catch (Exception exp) {
			throw exp;
		}
	}

	//Start:26-Feb-2011:ms9:R211 PRG:IR-085800
	/**
	 * Gets the data for the column "FTE" for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 * @since 2012
	 */
	public Vector getColumnDataForCostAndFTE(Context context, String[] args)  throws Exception {
		try { 
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramMap = (Map) programMap.get("paramList");         
			if(showCurrencyFilter(context,JPO.packArgs(paramMap))){
				vecResult=getFTEDataForCost(context,programMap);
			}
			else{
				vecResult=getFTEDataForPhase(context,programMap);
			}
			return vecResult;
		} catch (MatrixException exp) {
			throw exp;
		}
	}
	//End:26-Feb-2011:ms9:R211 PRG:IR-085800

	//Start:26-Feb-2011:ms9:R211 PRG:IR-085800
	/**
	 * Gets the data for the column "FTE"  Total FTE for request for table "PMCResourceRequestSummaryTable" without cost
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 * @since 2012
	 */     
	public Vector getFTEDataForPhase(Context context, Map programMap)  throws MatrixException 
	{
		try 
		{       	
			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			Vector vecResult = new Vector();
			MapList objectList = (MapList) programMap.get("objectList");
			String strViewBy = RESOURCE_PLAN_PREFERENCE_PHASE;
			Map paramList = (Map) programMap.get("paramList");
			Map columnMap = (Map) programMap.get("columnMap");

			String strColumnName = (String) columnMap.get(SELECT_NAME);
			double total=0d;
			double totalRowFTE=0d;
			double fteValue=0d; 
			String strTotalRowFTE = "";
			String strTotalFTE = "";
			Map mapObjectInfo = null;
			NumberFormat format = ProgramCentralUtil.getNumberFormatInstance(2, true);
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String strObjectId = (String)mapObjectInfo.get(SELECT_ID);
				fteValue=0d; 

				DomainObject doResourceReq = DomainObject.newInstance(context,strObjectId);
				strViewBy = (String)mapObjectInfo.get("ViewBy");
				if(null!=strViewBy && !"".equals(strViewBy)&&!"null".equalsIgnoreCase(strViewBy) && strViewBy.equalsIgnoreCase(RESOURCE_PLAN_PREFERENCE_PHASE))
				{
					if(doResourceReq.isKindOf(context, TYPE_RESOURCE_REQUEST) || doResourceReq.isKindOf(context, TYPE_PERSON))
					{
						Map mapPhaseFTE = (Map)mapObjectInfo.get("FTE");
						String strFteValue = (String)mapPhaseFTE.get(strColumnName);
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							if(null!=strFteValue && !"null".equals(strFteValue) && !"".equals(strFteValue))
							{
								fteValue = Task.parseToDouble(strFteValue);								
								if(null!=mapObjectInfo.get("totalRowCost"))
								{
									totalRowFTE=(Double)mapObjectInfo.get("totalRowCost");
								}
								else
								{
									totalRowFTE=0d;							
								}
								totalRowFTE=totalRowFTE+fteValue;
								mapObjectInfo.put("totalRowCost",totalRowFTE);								
							}	
							total =total+fteValue;	
							String[] strPhaseColumnInfo = strColumnName.split("-");
							String strPhaseOID          = strPhaseColumnInfo[1];
							String sCommandStatement = "print bus $1 select $2 dump";
							String strPhaseFTERelId =  MqlUtil.mqlCommand(context, sCommandStatement,strObjectId, "from["+ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE+"|to.id=="+strPhaseOID+"].id"); 
							if(!strPhaseFTERelId.equals(""))
							{
								DomainRelationship domRel = DomainRelationship.newInstance(context,strPhaseFTERelId);
								String strFTEVal = domRel.getAttributeValue(context, "FTE");
								fteValue = Task.parseToDouble(strFTEVal); 
							}
							vecResult.add(format.format(fteValue));
						}
					}
					else
					{
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							strTotalFTE=format.format(total);
							vecResult.add(strTotalFTE);
							if(null!= mapObjectInfo.get("totalRowCost"))
							{
								totalRowFTE=(Double)mapObjectInfo.get("totalRowCost");								
							}
							else
							{
								totalRowFTE=0d;										
							}
							totalRowFTE=totalRowFTE+total;
							mapObjectInfo.put("totalRowCost",totalRowFTE);
						}
					}
					if("Total".equalsIgnoreCase(strColumnName))
					{
						if(null!=mapObjectInfo.get("totalRowCost"))
						{
							String strId = (String)mapObjectInfo.get("id");
							DomainObject domObj = DomainObject.newInstance(context,strId);
							StringList slBusSelect = new StringList();
							slBusSelect.add(DomainObject.SELECT_ID);
							StringList slRelSelect = new StringList();
							slRelSelect.add(DomainRelationship.SELECT_ID);
							MapList mlRelatedObjects = domObj.getRelatedObjects(context,
									ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE, //pattern to match relationships
									ProgramCentralConstants.TYPE_PHASE, 				//pattern to match types
									slBusSelect, 										//StringList object-holds list of select statement pertaining to Business Obejcts.
									slRelSelect,										//StringList object-holds list of select statement pertaining to Relationships.
									true, 												//get To relationships
									true, 												//get From relationships
									(short)1, 											//the number of levels to expand, 0 equals expand all.
									"", 												//where clause to apply to objects, optional
									"",													//where clause to apply to relationship, can be empty ""
									0);
							if(mlRelatedObjects!=null)
							{
								Map mpRelatedObjectInfo= new HashMap ();
								totalRowFTE = 0d;
								for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();) 
								{
									mpRelatedObjectInfo = (Map) itrRelatedObjects.next();
									String strPhaseFTERelId = (String)mpRelatedObjectInfo.get("id[connection]");
									DomainRelationship domRel = DomainRelationship.newInstance(context,strPhaseFTERelId);
									String strFTEVal = domRel.getAttributeValue(context, "FTE");
									totalRowFTE = totalRowFTE + Task.parseToDouble(strFTEVal); 
								}
							}
							strTotalRowFTE=format.format(totalRowFTE);
							mapObjectInfo.put("totalRowCost",0.0);
						}
						vecResult.add(strTotalRowFTE);
					}
				}
				else
				{
					DomainObject domResourceReq = DomainObject.newInstance(context,strObjectId);
					StringList slBusSelect = new StringList();
					String[]strObjectIds = new String[1];
					strObjectIds[0] = strObjectId;
					slBusSelect.add("to["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].fromrel["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_PLAN_TEMPLATE+"].from.id");
					slBusSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
					slBusSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
					slBusSelect.add("to["+RELATIONSHIP_RESOURCE_PLAN+"].from.id");
					BusinessObjectWithSelectList resourceRequestObjWithSelectList = new BusinessObjectWithSelectList();;
					if(null!=mapObjectInfo.get(strObjectId))
					{
						resourceRequestObjWithSelectList = (BusinessObjectWithSelectList)mapObjectInfo.get(strObjectId);
					}
					else
					{
						resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strObjectIds,slBusSelect);
					}
					BusinessObjectWithSelect bows = null;
					String strProjectId = null;
					String strProjectTemplateId = null;
					StringList slPhaseIdList = new StringList();
					StringList slPhaseFTEList = new StringList();
					for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
					{
						bows = itr.obj();
						strProjectTemplateId = (String) bows.getSelectData("to["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].fromrel["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_PLAN_TEMPLATE+"].from.id");
						strProjectId = (String)bows.getSelectData("to["+RELATIONSHIP_RESOURCE_PLAN+"].from.id");
						slPhaseIdList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
						slPhaseFTEList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
					}
					if(resourceRequestObjWithSelectList.size()>0)
					{
						mapObjectInfo.put(strObjectId,resourceRequestObjWithSelectList);
					}
					if(null!= slPhaseFTEList && slPhaseIdList.size()>0)
					{
						if(null!=strProjectTemplateId)
						{
							String strPhaseId = strColumnName.substring(strColumnName.indexOf("-")+1,strColumnName.length());
							int nIndex = slPhaseIdList.indexOf(strPhaseId);
							if(nIndex!=-1)
							{
								String strFteValue = (String)slPhaseFTEList.get(nIndex);
								vecResult.add(strFteValue);
							}
							else
							{
								double douTotal= 0 ;
								for(int i=0;i<slPhaseFTEList.size();i++)
								{
									String temp = slPhaseFTEList.get(i).toString();
									douTotal = douTotal+Task.parseToDouble(temp);
								}
								String strTotal = douTotal+"";
								vecResult.add(strTotal);
							}
						}
					}
					else if(null!=strProjectId)
					{
						MapList mpChildObjList = new MapList();
						int count = 0;
						for (Iterator itrTableRows1 = objectList.iterator(); itrTableRows1.hasNext();)
						{
							Map mapObjectInf = (Map) itrTableRows1.next();
							mpChildObjList = (MapList)mapObjectInf.get("children");
						}
						String strObjId = (String) paramList.get("objectId");
						Map mpNewProgramMap = new HashMap();
						mpNewProgramMap.put("objectList", mpChildObjList);
						Map hmNewParamList = new HashMap();
						hmNewParamList.put("projectID",strObjId);
						mpNewProgramMap.put("paramList", hmNewParamList);
						mpNewProgramMap.put("columnMap", columnMap);
						Vector vec = null;

						vec = getFTEDataForPhase(context,mpNewProgramMap);

						BigDecimal dblValue = new BigDecimal(0);
						for (Iterator iterator = vec.iterator(); iterator.hasNext();) {
							String strValue = (String) iterator.next();
							BigDecimal bdValue = emxProgramCentralUtilBase_mxJPO.getNormalizedCurrencyValue(context, locale, strValue);
							strValue = ""+bdValue;
							if(!"".equals(strValue) && null != strValue){
								dblValue = dblValue.add(bdValue);
							}
						}
						String strFTEValue = ""+dblValue;
						if(null != strFTEValue){
							vecResult.add(format.format(Task.parseToDouble(strFTEValue)));
						}else{
							vecResult.add("0");
						}
					}        		
				}
			}
			return vecResult;
		} catch (MatrixException exp) {
			throw exp;
		}
	}

	/**
	 * Gets the data for the column "FTE"  Total FTE for request for table "PMCResourceRequestSummaryTable" without cost
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */   
	public Vector getColumnFTEPhaseOnlyFTEData(Context context, Map programMap)  throws Exception 
	{
		try 
		{       	
			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			Vector vecResult = new Vector();
			MapList objectList = (MapList) programMap.get("objectList");
			//String strViewBy = (String)((Map)(objectList.get(0))).get("ViewBy");
			String strViewBy = RESOURCE_PLAN_PREFERENCE_PHASE;
			Map paramList = (Map) programMap.get("paramList");
			//Added NZF
			String strProjectID = (String)paramList.get("projectID");
			Vector vecForTotal = new Vector();
			Map columnMap = (Map) programMap.get("columnMap");
			String strColumnName = (String) columnMap.get(SELECT_NAME);
			double total=0d;
			double totalRowFTE=0d;
			double fteValue=0d; 
			String strTotalRowFTE = "";
			String strTotalFTE = "";
			Map mapObjectInfo = null;
			NumberFormat format = ProgramCentralUtil.getNumberFormatInstance(2, true);
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String strObjectId = (String)mapObjectInfo.get(SELECT_ID);
				fteValue=0d; 

				DomainObject doResourceReq = DomainObject.newInstance(context,strObjectId);
				if(null!=strViewBy && !"".equals(strViewBy)&&!"null".equalsIgnoreCase(strViewBy) && strViewBy.equalsIgnoreCase(RESOURCE_PLAN_PREFERENCE_PHASE))
				{
					if(doResourceReq.isKindOf(context, TYPE_RESOURCE_REQUEST) || doResourceReq.isKindOf(context, TYPE_PERSON))
					{
						String strFteValue = "";
						Map mapPhaseFTE = (Map)mapObjectInfo.get("FTE");
						if(null != mapPhaseFTE){
							strFteValue = (String)mapPhaseFTE.get(strColumnName);
						}

						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							if(null!=strFteValue && !"null".equals(strFteValue) && !"".equals(strFteValue))
							{
								fteValue = Task.parseToDouble(strFteValue);								
								if(null!=mapObjectInfo.get("totalRowCost"))
								{
									totalRowFTE=(Double)mapObjectInfo.get("totalRowCost");
								}
								else
								{
									totalRowFTE=0d;							
								}
								totalRowFTE=totalRowFTE+fteValue;
								mapObjectInfo.put("totalRowCost",totalRowFTE);								
							}	
							total =total+fteValue;	
							vecResult.add(format.format(fteValue));
						}			
					}
					else
					{
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							strTotalFTE=format.format(total);
							vecResult.add(strTotalFTE);
							if(null!= mapObjectInfo.get("totalRowCost"))
							{
								totalRowFTE=(Double)mapObjectInfo.get("totalRowCost");								
							}
							else
							{
								totalRowFTE=0d;										
							}
							totalRowFTE=totalRowFTE+total;
							mapObjectInfo.put("totalRowCost",totalRowFTE);
						}
					}
					if("Total".equalsIgnoreCase(strColumnName))
					{
						if(null!=mapObjectInfo.get("totalRowCost"))
						{
							totalRowFTE=(Double)mapObjectInfo.get("totalRowCost");
							strTotalRowFTE=format.format(totalRowFTE);
							mapObjectInfo.put("totalRowCost",0.0);
						}

						vecResult.add(strTotalRowFTE);
					}
				}
				else
				{
					DomainObject domResourceReq = DomainObject.newInstance(context,strObjectId);
					StringList slBusSelect = new StringList();
					String[]strObjectIds = new String[1];
					strObjectIds[0] = strObjectId;
					slBusSelect.add("to["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].fromrel["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_PLAN_TEMPLATE+"].from.id");
					slBusSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
					slBusSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
					slBusSelect.add("to["+RELATIONSHIP_RESOURCE_PLAN+"].from.id");
					BusinessObjectWithSelectList resourceRequestObjWithSelectList = new BusinessObjectWithSelectList();;
					if(null!=mapObjectInfo.get(strObjectId))
					{
						resourceRequestObjWithSelectList = (BusinessObjectWithSelectList)mapObjectInfo.get(strObjectId);
					}
					else
					{
						resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strObjectIds,slBusSelect);
					}
					BusinessObjectWithSelect bows = null;
					String strProjectId = null;
					String strProjectTemplateId = null;
					StringList slPhaseIdList = new StringList();
					StringList slPhaseFTEList = new StringList();
					for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
					{
						bows = itr.obj();
						strProjectTemplateId = (String) bows.getSelectData("to["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].fromrel["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_PLAN_TEMPLATE+"].from.id");
						strProjectId = (String)bows.getSelectData("to["+RELATIONSHIP_RESOURCE_PLAN+"].from.id");
						slPhaseIdList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
						slPhaseFTEList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
					}
					if(resourceRequestObjWithSelectList.size()>0)
					{
						mapObjectInfo.put(strObjectId,resourceRequestObjWithSelectList);
					}
					if(null != slPhaseIdList && slPhaseIdList.size()>0)
					{
						if(null!=strProjectTemplateId)
						{
							String strPhaseId = strColumnName.substring(strColumnName.indexOf("-")+1,strColumnName.length());
							int nIndex = slPhaseIdList.indexOf(strPhaseId);
							if(nIndex!=-1)
							{
								String strFteValue = (String)slPhaseFTEList.get(nIndex);
								vecResult.add(strFteValue);
							}
							else
							{
								double douTotal= 0 ;
								for(int i=0;i<slPhaseFTEList.size();i++)
								{
									String temp = slPhaseFTEList.get(i).toString();
									douTotal = douTotal+Task.parseToDouble(temp);
								}
								String strTotal = douTotal+"";
								vecResult.add(strTotal);
							}
						}
					}
					else if(null!=strProjectId)
					{
						MapList mpChildObjList = new MapList();
						int count = 0;
						for (Iterator itrTableRows1 = objectList.iterator(); itrTableRows1.hasNext();)
						{
							Map mapObjectInf = (Map) itrTableRows1.next();
							mpChildObjList = (MapList)mapObjectInf.get("children");
						}
						String strObjId = (String) paramList.get("objectId");
						Map mpNewProgramMap = new HashMap();
						mpNewProgramMap.put("objectList", mpChildObjList);
						Map hmNewParamList = new HashMap();
						hmNewParamList.put("projectID",strObjId);
						mpNewProgramMap.put("paramList", hmNewParamList);
						mpNewProgramMap.put("columnMap", columnMap);
						Vector vec = null;
						vec = getColumnFTEPhaseOnlyFTEData(context,mpNewProgramMap);	

						BigDecimal dblValue = new BigDecimal(0);
						for (Iterator iterator = vec.iterator(); iterator.hasNext();) {
							String strValue = (String) iterator.next();
							BigDecimal bdValue = emxProgramCentralUtilBase_mxJPO.getNormalizedCurrencyValue(context, locale, strValue);
							strValue = ""+bdValue;
							if(!"".equals(strValue) && null != strValue){
								dblValue = dblValue.add(bdValue);
							}
						}

						String strFTEValue = ""+dblValue;
						if(null != strFTEValue){
							vecResult.add(format.format(Task.parseToDouble(strFTEValue)));
						}else{//END NZF
							vecResult.add("0");
						}
					}        		
				}
			}
			return vecResult;
		} catch (Exception exp) {
			throw exp;
		}
	}

	/**
	 * Gets the data for the column "FTE" with cost and  Total FTE for request for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 * @since 2012
	 */  
	public Vector getFTEDataForCost(Context context, Map programMap)  throws Exception 
	{
		try 
		{
			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			Vector vecResult = new Vector();
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			Map columnMap = (Map) programMap.get("columnMap");
			String strColumnName = (String) columnMap.get(SELECT_NAME);
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			String strCurrencyFilter =(String) paramList.get("PMCResourceRequestCurrencyFilter");
			if(ProgramCentralUtil.isNullString(strCurrencyFilter))
			{
				strCurrencyFilter =(String) paramList.get("PMCResourcePlanTemplateCurrencyFilter");
			}
			String strParentOID = (String)paramList.get("parentOID");
			if(ProgramCentralUtil.isNullString(strParentOID))
			{
				strParentOID = (String)paramList.get("projectID");
			}
			DomainObject tempDom = null;
			String strViewFilterValue =  null;
			if(ProgramCentralUtil.isNotNullString(strParentOID))
			{
				tempDom = DomainObject.newInstance(context, strParentOID);
				strViewFilterValue = getResourcePlanPreference(context, tempDom);
			}
			double total=0d;
			double totalRowCost=0d;
			double fteValue=0d; 
			double fteVal=0d; 
			String strTotalRowCost = "";
			String strTotalCost = "";
			PersonUtil contextUser =  new PersonUtil();
			String userCompanyID =contextUser.getUserCompanyId(context);
			Map conversionMap = new HashMap();
			conversionMap = ProgramCentralUtil.getCurrencyConversionMap(context, userCompanyID);
			NumberFormat format = ProgramCentralUtil.getNumberFormatInstance(2, true);
			Map mapObjectInfo = null;
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String strObjectId = (String)mapObjectInfo.get(SELECT_ID);
				DomainObject doResourceReq = DomainObject.newInstance(context,strObjectId);
				fteValue = 0d;
				String strFteValue ="";	
				if(doResourceReq.isKindOf(context, TYPE_PROJECT_SPACE))
				{
					MapList mpChildObjList = new MapList();
					int count = 0;
					for (Iterator itrTableRows1 = objectList.iterator(); itrTableRows1.hasNext();)
					{
						Map mapObjectInf = (Map) itrTableRows1.next();
						mpChildObjList = (MapList)mapObjectInf.get("children");
					}
					String strObjId = (String) paramList.get("objectId");
					Map mpNewProgramMap = new HashMap();
					mpNewProgramMap.put("objectList", mpChildObjList);
					mpNewProgramMap.put("paramList", paramList);
					mpNewProgramMap.put("columnMap", columnMap);
					Vector vec = null;
					vec = getFTEDataForCost(context,mpNewProgramMap);

					BigDecimal dblValue = new BigDecimal(0);
					for (Iterator iterator = vec.iterator(); iterator.hasNext();) {
						String strValue = (String) iterator.next();
						if(!"".equals(strValue) && null != strValue){
							BigDecimal bdValue = emxProgramCentralUtilBase_mxJPO.getNormalizedCurrencyValue(context, locale, strValue);
							strValue = ""+bdValue;
							dblValue = dblValue.add(bdValue);
						}
					}
					BigDecimal bdValue = emxProgramCentralUtilBase_mxJPO.getNormalizedCurrencyValue(context, locale, ""+dblValue);
					vecResult.add(format.format(Task.parseToDouble(""+bdValue)));
				}
				else
				{
					if(doResourceReq.isKindOf(context, TYPE_RESOURCE_REQUEST))
					{
						Map selectMap = new HashMap();
						selectMap.put("conversionMap",conversionMap);
						selectMap.put("currencyFilter",strCurrencyFilter);
						selectMap.put("resourceReqId",strObjectId);
						double standardCost = getStandardCostValue(context, selectMap);
						String phaseID=  strColumnName.substring(strColumnName.indexOf("-")+1, strColumnName.length());
						Date fromDate= null;
						Date toDate=null;
						if(!"Total".equalsIgnoreCase(phaseID))
						{
							DomainObject phaseDo = DomainObject.newInstance(context, phaseID);
							StringList objectSelects = new StringList();
							objectSelects.add(ATTRIBUTE_PHASE_START_DATE);
							objectSelects.add(ATTRIBUTE_PHASE_FINISH_DATE);    				
							Map mapPhaseInfo = phaseDo.getInfo(context, objectSelects);
							fromDate=eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_START_DATE));
							toDate=eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_FINISH_DATE));
							Map mapPhaseFTE = (Map)mapObjectInfo.get("FTE");
							strFteValue = (String)mapPhaseFTE.get(strColumnName);						
						}					
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							if(null!=strFteValue && !"null".equals(strFteValue) && !"".equals(strFteValue))
							{
								if(null!=tempDom && tempDom.isKindOf(context, TYPE_PROJECT_SPACE))
								{
									if(ProgramCentralUtil.isNotNullString(strViewFilterValue) && RESOURCE_PLAN_PREFERENCE_PHASE.equalsIgnoreCase(strViewFilterValue))
									{
										ResourceRequest resourceRequest = new ResourceRequest();
										Map mapFTE = new HashMap();
										Map requestInfoMap = resourceRequest.calculateFTE(context, fromDate, toDate, 1+"");
										for (Iterator iterator = requestInfoMap.keySet().iterator(); iterator.hasNext();) 
										{
											String strTimeFrame = (String) iterator.next();
											mapFTE.put(strTimeFrame, Task.parseToDouble(strFteValue));
										}		
										fteValue=resourceRequest.calculateTotalPhaseCost(context, fromDate, toDate, requestInfoMap,mapFTE,strFteValue,standardCost);
									}
									else
									{
										ResourceRequest resourceRequest = new ResourceRequest();
										Map requestInfoMap = getRequestTimeLineFTEInfo(
												context, strObjectId);
										FTE fte = FTE.getInstance(context, (String)requestInfoMap.get(SELECT_RESOURCE_PLAN_FTE));
										Map mapFTE = getFullTimeframeFTE(context, strObjectId, requestInfoMap, fte.getAllFTE());
										Map mapNewFTE = resourceRequest.getNormalizedFTEForPhase(context, tempDom.getId(context), phaseID, requestInfoMap,mapFTE);
										fteValue=resourceRequest.calculateTotalPhaseCost(context, fromDate, toDate, requestInfoMap, mapNewFTE,standardCost);
									}
								}
								else
								{
									ResourceRequest resourceRequest = new ResourceRequest();
									Map mapFTE = new HashMap();
									Map requestInfoMap = resourceRequest.calculateFTE(context, fromDate, toDate, 1+"");
									for (Iterator iterator = requestInfoMap.keySet().iterator(); iterator.hasNext();) 
									{
										String strTimeFrame = (String) iterator.next();
										mapFTE.put(strTimeFrame, Task.parseToDouble(strFteValue));
									}		
									fteValue=resourceRequest.calculateTotalPhaseCost(context, fromDate, toDate, requestInfoMap,mapFTE,strFteValue,standardCost);
								}
								if(null!=mapObjectInfo.get("totalRowCost"))
								{
									totalRowCost=(Double)mapObjectInfo.get("totalRowCost");								                                                                                                         
								}
								else
								{
									totalRowCost=0d;                                   
								}
								totalRowCost=totalRowCost+fteValue;
								mapObjectInfo.put("totalRowCost",totalRowCost);                                                       
							}
							total =total+fteValue;  						
							strFteValue = format.format(fteValue)+"";
							vecResult.add(strFteValue);
						}
					}
					else
					{
						if(!"Total".equalsIgnoreCase(strColumnName))
						{
							vecResult.add("-");
						}
					}
					if("Total".equalsIgnoreCase(strColumnName))
					{
						if(null!=mapObjectInfo.get("totalRowCost"))
						{
							totalRowCost=(Double)mapObjectInfo.get("totalRowCost");
							strTotalRowCost=format.format(totalRowCost);
							strTotalRowCost = strTotalRowCost+"";
							vecResult.add(strTotalRowCost);
							mapObjectInfo.put("totalRowCost",0.0);
						}
						else {
							vecResult.add("-");
						}
					}        		
				}
			}
			return vecResult;
		} catch (MatrixException exp) {
			throw exp;
		}
	}


	/**
	 * @param context
	 * @param strObjectId
	 * @return
	 * @throws FrameworkException
	 */
	public Map getRequestTimeLineFTEInfo(Context context, String strObjectId)
	throws FrameworkException {
		DomainObject reqDo = DomainObject.newInstance(context,strObjectId);
		StringList slBusSelect = new StringList(SELECT_ATTRIBUTE_START_DATE);
		slBusSelect.add(SELECT_ATTRIBUTE_END_DATE);
		slBusSelect.add(SELECT_RESOURCE_PLAN_FTE);
		slBusSelect.add(SELECT_PROJECT_SPACE_ID);
		Map requestInfoMap = reqDo.getInfo(context, slBusSelect);
		return requestInfoMap;
	}

	/**
	 * Gets the data for the column "FTE" with cost and  Total FTE for request for table "PMCResourceRequestSummaryTable"
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */  
	public Vector getColumnFTEPhaseCostData(Context context, Map programMap)  throws Exception 
	{
		try 
		{       	
			Vector vecResult = new Vector();
			MapList objectList = (MapList) programMap.get("objectList");
			Map paramList = (Map) programMap.get("paramList");
			Map columnMap = (Map) programMap.get("columnMap");
			String strColumnName = (String) columnMap.get(SELECT_NAME);
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			String strCurrencyFilter =(String) paramList.get("PMCResourceRequestCurrencyFilter");
			if(null==strCurrencyFilter || "".equals(strCurrencyFilter) || "Null".equalsIgnoreCase(strCurrencyFilter))
			{
				strCurrencyFilter =(String) paramList.get("PMCResourcePlanTemplateCurrencyFilter");
			}
			double total=0d;
			double totalRowCost=0d;
			double fteValue=0d; 
			String strTotalRowCost = "";
			String strTotalCost = "";
			PersonUtil contextUser =  new PersonUtil();
			String userCompanyID =contextUser.getUserCompanyId(context);
			Map conversionMap = new HashMap();
			conversionMap = ProgramCentralUtil.getCurrencyConversionMap(context, userCompanyID);
			NumberFormat format = ProgramCentralUtil.getNumberFormatInstance(2, true);
			Map mapObjectInfo = null;
			String strParentOID = (String)paramList.get("parentOID");
			if(ProgramCentralUtil.isNullString(strParentOID))
			{
				strParentOID = (String)paramList.get("projectID");
			}
			DomainObject tempDom = null;
			String strViewFilterValue =  null;
			if(ProgramCentralUtil.isNotNullString(strParentOID))
			{
				tempDom = DomainObject.newInstance(context, strParentOID);
				strViewFilterValue = getResourcePlanPreference(context, tempDom);
			}
			for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				String strObjectId = (String)mapObjectInfo.get(SELECT_ID);
				DomainObject doResourceReq = DomainObject.newInstance(context,strObjectId);
				fteValue = 0d;
				String strFteValue ="";				
				if(doResourceReq.isKindOf(context, TYPE_RESOURCE_REQUEST))
				{
					Map selectMap = new HashMap();
					selectMap.put("conversionMap",conversionMap);
					selectMap.put("currencyFilter",strCurrencyFilter);
					selectMap.put("resourceReqId",strObjectId);
					double standardCost = getStandardCostValue(context, selectMap);
					String phaseID=  strColumnName.substring(strColumnName.indexOf("-")+1, strColumnName.length());
					Date fromDate= null;
					Date toDate=null;
					if(!"Total".equalsIgnoreCase(phaseID))
					{
						DomainObject phaseDo = DomainObject.newInstance(context, phaseID);
						StringList objectSelects = new StringList();
						objectSelects.add(ATTRIBUTE_PHASE_START_DATE);
						objectSelects.add(ATTRIBUTE_PHASE_FINISH_DATE);    				
						Map mapPhaseInfo = phaseDo.getInfo(context, objectSelects);
						fromDate=eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_START_DATE));
						toDate=eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_FINISH_DATE));
						Map mapPhaseFTE = (Map)mapObjectInfo.get("FTE");
						strFteValue = (String)mapPhaseFTE.get(strColumnName);						
					}					
					if(!"Total".equalsIgnoreCase(strColumnName))
					{
						if(null!=strFteValue && !"null".equals(strFteValue) && !"".equals(strFteValue))
						{
							ResourceRequest resourceRequest = new ResourceRequest();
							Map requestInfoMap = getRequestTimeLineFTEInfo(context, strObjectId);
							FTE fte = FTE.getInstance(context, (String)requestInfoMap.get(SELECT_RESOURCE_PLAN_FTE));
							Map mapFTE = getFullTimeframeFTE(context, strObjectId, requestInfoMap, fte.getAllFTE());
							if(null!=tempDom && tempDom.isKindOf(context, TYPE_PROJECT_SPACE))
								mapFTE = resourceRequest.getNormalizedFTEForPhase(context, tempDom.getId(context), phaseID, requestInfoMap,mapFTE);
							fteValue=resourceRequest.calculateTotalPhaseCost(context, fromDate, toDate, requestInfoMap, mapFTE,standardCost);
							if(null!=mapObjectInfo.get("totalRowCost"))
							{
								totalRowCost=(Double)mapObjectInfo.get("totalRowCost");								                                                                                                         
							}
							else
							{
								totalRowCost=0d;                                   
							}
							totalRowCost=totalRowCost+fteValue;
							mapObjectInfo.put("totalRowCost",totalRowCost);                                                       
						}
						total =total+fteValue;  						
						strFteValue = format.format(fteValue)+"";
						vecResult.add(strFteValue);
					}
				}
				else
				{
					if(!"Total".equalsIgnoreCase(strColumnName))
					{
						vecResult.add("0");
					}
				}
				if("Total".equalsIgnoreCase(strColumnName))
				{
					if(null!=mapObjectInfo.get("totalRowCost"))
					{
						totalRowCost=(Double)mapObjectInfo.get("totalRowCost");
						strTotalRowCost=format.format(totalRowCost);
						strTotalRowCost = strTotalRowCost+"";
						vecResult.add(strTotalRowCost);
						mapObjectInfo.put("totalRowCost",0.0);
					}
					else if(mapObjectInfo.containsKey("children"))
					{
						double totalCost = 0;
						MapList mlObjInfo = (MapList) mapObjectInfo.get("children");
						for (Iterator tempItrTableRows = mlObjInfo.iterator(); tempItrTableRows.hasNext();)
						{
							Map mapTempObjectInfo = (Map) tempItrTableRows.next();

							if(null!=mapTempObjectInfo.get("totalRowCost"))
							{
								double totalRowCost1=(Double)mapTempObjectInfo.get("totalRowCost");
								totalCost = totalCost + totalRowCost1;
							}
						}
						strTotalRowCost=format.format(totalCost);
						strTotalRowCost = strTotalRowCost+"";
						vecResult.add(strTotalRowCost);
						mapObjectInfo.put("totalRowCost",0.0);
					}
					else 
					{
						vecResult.add("0");
					}
				}
			}
			return vecResult;
		} catch (Exception exp) {
			throw exp;
		}
	}


	/**
	 * Gets the data for the column "FTE" for table "PMCResourceRequestSummaryTable" in Phase View
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @return The Vector object containing
	 * @throws Exception if operation fails
	 */
	public Map getPhaseColumns(Context context,String strProjectId, FTE fte,String strRequestId)  throws Exception 
	{
		Map mapPhaseFTEValue = calculatePhaseFTEReverse(context, strProjectId,
				strRequestId);
		return mapPhaseFTEValue;
	}

	private Map calculatePhaseFTEReverse(Context context, String strProjectId, String strRequestId)
	throws MatrixException 
	{
		Map mapPhaseFTEValue = new HashMap();
		try {
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, false);
			MapList mlPhaseList = ResourceRequest.getPhaseList(context, strProjectId);
			ResourceRequest resourceRequest = new ResourceRequest();
			Map requestInfoMap = getRequestTimeLineFTEInfo(
					context, strRequestId);
			FTE fte = FTE.getInstance(context, (String)requestInfoMap.get(SELECT_RESOURCE_PLAN_FTE));
			Map fullTimeFrameFTEMap = getFullTimeframeFTE(context, strRequestId, requestInfoMap, fte.getAllFTE());
			Date reqStartDate = eMatrixDateFormat.getJavaDate((String)requestInfoMap.get(SELECT_ATTRIBUTE_START_DATE));
			Date reqEndDate = eMatrixDateFormat.getJavaDate((String)requestInfoMap.get(SELECT_ATTRIBUTE_END_DATE));
			int nCounter = 0;
			Map resourceRequestDays = resourceRequest.getRequestTimeLineDays(reqStartDate, reqEndDate);
			double timeFrameDuration = 0;
			double fullTimeFrameDuration = 0;
			double nTimeFramePhaseFTE;
			double fullTimeFrameFTE;
			double nTotalPhaseFTE = 0;
			double phaseTimeFrameDuration = 0;
			Map mapTimeFrameCount = new HashMap();
			for (Iterator iter = mlPhaseList.iterator(); iter.hasNext();) 
			{
				nCounter=0;
				nTotalPhaseFTE = 0;
				phaseTimeFrameDuration = 0;
				Map mapPhaseInfo = (Map)iter.next();            
				String strPhaseId = (String)mapPhaseInfo.get(SELECT_ID);
				Date phaseStartDate=eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_START_DATE));
				Date phaseEndDate=eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_FINISH_DATE));
				MapList timeframesList = fte.getTimeframes(phaseStartDate,phaseEndDate);
				int nTimeFrameSize = timeframesList.size();
				double totalFullTimeFrameDuration = 0d;
				if(reqStartDate.after(phaseStartDate))
				{
					phaseStartDate = reqStartDate;
				}
				if(reqEndDate.before(phaseEndDate))
				{
					phaseEndDate= reqEndDate;
				}
				for(Iterator innerIter = timeframesList.iterator(); innerIter.hasNext();)
				{
					Map strInnerTimeFrame = (Map)innerIter.next();
					String strMonth = strInnerTimeFrame.get("timeframe").toString();
					String strYear = strInnerTimeFrame.get("year").toString();
					String strTimeFrame=strMonth+"-"+strYear;
					Date fromDate = fte.getStartDate(strTimeFrame);
					Date toDate = fte.getEndDate(strTimeFrame);
					String strStartDate = fte.getTimeFrame(phaseStartDate);
					String strEndDate = fte.getTimeFrame(phaseEndDate);
					StringList slStartDateTimeFrame = FrameworkUtil.split(strStartDate, "-");
					StringList slEndDateTimeFrame = FrameworkUtil.split(strEndDate, "-");
					if(nCounter==0)
					{
						if((Integer.parseInt(slStartDateTimeFrame.get(0).toString())==Integer.parseInt(slEndDateTimeFrame.get(0).toString())) && 
								(Integer.parseInt(slStartDateTimeFrame.get(1).toString())==Integer.parseInt(slEndDateTimeFrame.get(1).toString())))
						{
							timeFrameDuration = ProgramCentralUtil.computeDuration(phaseStartDate, phaseEndDate);
						}
						else
						{
							timeFrameDuration = fte.getNumberofDaysSinceStartTimeFrame(phaseStartDate);
						}
					}
					else if(nCounter==(nTimeFrameSize-1))
					{
						timeFrameDuration = fte.getNumberofDaysTillEndTimeFrame(phaseEndDate);
					}
					else
					{
						timeFrameDuration = ProgramCentralUtil.computeDuration(fromDate, toDate);
					} 
					fullTimeFrameFTE = 0;
					if(null!=fullTimeFrameFTEMap.get(strTimeFrame))
					{
						fullTimeFrameFTE = (Double)fullTimeFrameFTEMap.get(strTimeFrame);
					}                    
					if("Hours".equalsIgnoreCase(numberofPeopleUnit))
					{
						if(null!=resourceRequestDays.get(strTimeFrame))
							fullTimeFrameDuration = (Double)resourceRequestDays.get(strTimeFrame);
					}
					else
					{
						fullTimeFrameDuration = ProgramCentralUtil.computeDuration(fromDate, toDate);
					}
					totalFullTimeFrameDuration = totalFullTimeFrameDuration + fullTimeFrameDuration;
					nTimeFramePhaseFTE = (timeFrameDuration*fullTimeFrameFTE )/fullTimeFrameDuration;
					phaseTimeFrameDuration = phaseTimeFrameDuration+timeFrameDuration;
					nTotalPhaseFTE = nTotalPhaseFTE+nTimeFramePhaseFTE;
					nCounter++;
				}
				if(!"Hours".equalsIgnoreCase(numberofPeopleUnit))
				{
					nTotalPhaseFTE = ((totalFullTimeFrameDuration/new Double(nTimeFrameSize))*nTotalPhaseFTE)/phaseTimeFrameDuration;
				}
				mapPhaseFTEValue.put("PhaseOID"+"-"+strPhaseId, numberFormat.format(nTotalPhaseFTE));
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}		
		return mapPhaseFTEValue;
	}  


	public Map getFullTimeframeFTE(Context context, String strRequestId, Map requestInfoMap,Map mapFTE)throws FrameworkException 
	{
		Map fullTimeFrameFTEMap = new HashMap();
		MapList mlFullTimeFrameFTE = new MapList();
		String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
		try {
			NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, false);
			FTE fte = FTE.getInstance(context);
			Map mapObjectInfo = null;
			int nTimeframe  = 0 ;
			int nYearTimeframe = 0;
			long nReqDays =0; 
			long totalTimeFrameDuration = 0;
			double fullTimeFrameFTE = 0;
			String strTimeFrame = "";
			Date reqStDate=eMatrixDateFormat.getJavaDate((String)requestInfoMap.get(SELECT_ATTRIBUTE_START_DATE));
			Date reqEnDate=eMatrixDateFormat.getJavaDate((String)requestInfoMap.get(SELECT_ATTRIBUTE_END_DATE));
			MapList mlTimeFrames = fte.getTimeframes(reqStDate,reqEnDate);
			for (Iterator itrTableRows = mlTimeFrames.iterator(); itrTableRows.hasNext();)
			{
				mapObjectInfo = (Map) itrTableRows.next();
				nTimeframe =(Integer)mapObjectInfo.get("timeframe");
				nYearTimeframe =(Integer)mapObjectInfo.get("year");
				strTimeFrame=nTimeframe+"-"+nYearTimeframe;
				Date timeFrameStDate = fte.getStartDate(strTimeFrame);
				Date  timeFrameEnDate= fte.getEndDate(strTimeFrame);
				totalTimeFrameDuration = ProgramCentralUtil.computeDuration(timeFrameStDate, timeFrameEnDate);
				double requestFTE= (Double)mapFTE.get(strTimeFrame);

				if(reqStDate.after(timeFrameStDate))
				{
					timeFrameStDate = reqStDate;
				}
				if(reqEnDate.before(timeFrameEnDate))
				{
					timeFrameEnDate= reqEnDate;
				}
				nReqDays = ProgramCentralUtil.computeDuration(timeFrameStDate,timeFrameEnDate);
				if("Hours".equalsIgnoreCase(numberofPeopleUnit))
				{
					fullTimeFrameFTE =  requestFTE;
				}
				else
				{
					fullTimeFrameFTE= (totalTimeFrameDuration*requestFTE)/nReqDays;
				}
				fullTimeFrameFTE= Task.parseToDouble(numberFormat.format(fullTimeFrameFTE));
				fullTimeFrameFTEMap.put(strTimeFrame, fullTimeFrameFTE);
			}  

		} catch (MatrixException e) {
		}
		return fullTimeFrameFTEMap;
	}



	/**
	 * This method updates the FTE phase value for Resource Request template Table 
	 * @param context The matrix context object
	 * @param args The arguments, it contains objectList and paramList maps
	 * @throws Exception if operation fails
	 */  

	public void updateFTEPhaseColumnData(Context context,String[]args) throws MatrixException 
	{
		try 
		{
			Map programMap                      = (Map) JPO.unpackArgs(args);
			Map mpParamMap                      = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (Map)programMap.get("columnMap");
			Map mpRequestMap                    = (Map)programMap.get("requestMap");
			String sErrMsg =null;
			String strPhaseColumnName                 = (String)mpColumnMap.get("name");
			String strProjectOrOraganizationID  = (String)mpRequestMap.get("objectId");
			String strLanguage = context.getSession().getLanguage();
			double nNewFTEValue                 = 0;
			String strFTEVal =  (String)mpParamMap.get("New Value");
			if(ProgramCentralUtil.isNotNullString(strFTEVal)){
				try{
					nNewFTEValue = Task.parseToDouble(strFTEVal);
					if(nNewFTEValue < 0){
						throw new NumberFormatException();
					}
				}
				catch (NumberFormatException e){
					sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Validate.ValidatePeople", strLanguage);
					MqlUtil.mqlCommand(context, "notice " + sErrMsg);
					return;
				}
			}
			String[] strPhaseColumnInfo = strPhaseColumnName.split("-");
			MapList mlRequestsInfo      = null;
			String strObjectId          = (String)mpParamMap.get("objectId");            
			String strPhaseOID          = strPhaseColumnInfo[1];            
			String strRelID             = null;
			StringList slPhaseRelId     = null;            
			StringList slPhaseList      = null;
			int index					= 0;
			final String RESOURCE_REQUEST_PHASE = "from["+ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE+"].to."+SELECT_ID;
			final String RESOURCE_REQUEST_REL_ID = "from["+ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE+"]."+SELECT_ID;
			StringList objectSelects = new StringList(2);
			objectSelects.add(RESOURCE_REQUEST_PHASE);
			objectSelects.add(RESOURCE_REQUEST_REL_ID);

			BusinessObjectWithSelect bows = null;
			BusinessObjectWithSelectList requestBObj=BusinessObject.getSelectBusinessObjectData(context,new String[]{strObjectId},objectSelects);
			if(!requestBObj.isEmpty())
			{
				for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(requestBObj); itr.next();)
				{
					bows = itr.obj();
					slPhaseList = bows.getSelectDataList(RESOURCE_REQUEST_PHASE);
					slPhaseRelId = bows.getSelectDataList(RESOURCE_REQUEST_REL_ID);
				}
			}
			DomainObject domRequestObj =DomainObject.newInstance(context, strObjectId);
			Map resourceRequestmap = domRequestObj.getInfo(context, objectSelects);

			DomainRelationship domRelObj = null;
			index = slPhaseList.indexOf(strPhaseOID);
			if(slPhaseList.contains(strPhaseOID))
			{        		
				strRelID= (String)slPhaseRelId.get(index);
				domRelObj=DomainRelationship.newInstance(context, strRelID);        		
			}
			else
			{
				DomainObject domObj = DomainObject.newInstance(context, strPhaseOID);
				domRelObj=DomainRelationship.connect(context,domRequestObj,ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE,domObj);
			}
			domRelObj.setAttributeValue(context, ATTRIBUTE_FTE, nNewFTEValue+"");
		}catch (Exception exp){
			throw new MatrixException(exp);
		}
	}


	/**
	 * Program to get the cell level access for object's Resource pool and Project Role field
	 * Meeting etc.
	 * 
	 * @param context
	 *            the eMatrix Context object
	 * @@param String array contains Meetings Ids for edit
	 * @throws Matrix
	 *             Exception if the operation fails
	 * @grade 0
	 */

	public StringList getFTEViewEditAccess(Context context,
			String args[]) throws Exception {
		Map inputMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)inputMap.get("requestMap");
		MapList objectMap = (MapList) inputMap.get("objectList");
		int listSize=objectMap.size();
		boolean isAccessible=!showCurrencyFilter(context,JPO.packArgs(requestMap));
		StringList returnStringList = new StringList(listSize);
		DomainObject dmoObject = null;
		for(int iterator = 0; iterator < objectMap.size(); iterator++)
		{
			Map map = (Map)objectMap.get(iterator);
			String strObjectId = (String)map.get(SELECT_ID); 
			dmoObject = DomainObject.newInstance(context, strObjectId);
			if(dmoObject.isKindOf(context, TYPE_PROJECT_SPACE))
				returnStringList.add(false);
			else
				returnStringList.addElement(Boolean.valueOf(isAccessible));
		}
		return returnStringList;
	}

	/**
	 * Program to get the cell level access for object's Standard Cost field
	 * Meeting etc.
	 * 
	 * @param context
	 *            the eMatrix Context object
	 * @@param String array contains Meetings Ids for edit
	 * @throws Matrix
	 *             Exception if the operation fails
	 * @since V6 R207 Author : Haripriya K
	 * @grade 0
	 */

	public StringList getCostViewEditAccess(Context context,
			String args[]) throws Exception {
		Map inputMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)inputMap.get("requestMap");
		MapList objectMap = (MapList) inputMap.get("objectList");
		boolean isAccessible = true;
		boolean isCostView=showCurrencyFilter(context,JPO.packArgs(requestMap));
		if(isCostView)
		{
			String strCurrencyFilter =(String) requestMap.get("PMCResourceRequestCurrencyFilter");
			String strDefaultCurrencyDisplay = "";
			if(null==strCurrencyFilter || "".equals(strCurrencyFilter) || "Null".equalsIgnoreCase(strCurrencyFilter))
			{
				strCurrencyFilter =(String) requestMap.get("PMCResourcePlanTemplateCurrencyFilter");
			}
			strDefaultCurrencyDisplay =  ProgramCentralUtil.getCurrencyUnit(context);
			if(strDefaultCurrencyDisplay != null && !"".equalsIgnoreCase(strDefaultCurrencyDisplay)){
				if(strDefaultCurrencyDisplay.equalsIgnoreCase(strCurrencyFilter))
				{
					isAccessible = true;
				}
				else
				{
					isAccessible = false;
				}
			}
		}
		int listSize=objectMap.size();
		StringList returnStringList = new StringList(listSize);
		String strCurrent, strObjType;
		DomainObject dmoObject = null;
		for (int i=0;i<listSize;i++) {
			Map mapObj =(Map)objectMap.get(i);
			if(mapObj != null){
				String strObjId = (String)mapObj.get(DomainConstants.SELECT_ID);
				if(null != strObjId && !"".equalsIgnoreCase(strObjId))
				{
					dmoObject = DomainObject.newInstance(context, strObjId);
					StringList slSelectable = new StringList();
					slSelectable.add(SELECT_IS_RESOURCE_REQUEST);
					slSelectable.add(SELECT_CURRENT);
					Map mapObjInfo = dmoObject.getInfo(context,slSelectable);
					String sIsResourceRequestType = (String) mapObjInfo.get(SELECT_IS_RESOURCE_REQUEST);
					boolean isResourceRequest = ProgramCentralUtil.isNotNullString(sIsResourceRequestType) && "true".equalsIgnoreCase(sIsResourceRequestType)?true:false;
					if(isResourceRequest)
					{
						String strCurrentState = (String) mapObjInfo.get(SELECT_CURRENT);
						boolean isStateAccess = strCurrentState.equalsIgnoreCase(DomainConstants.STATE_RESOURCE_REQUEST_REJECTED) || strCurrentState.equalsIgnoreCase(DomainConstants.STATE_RESOURCE_REQUEST_CREATE); 
						if(isStateAccess && isAccessible)
						{
							returnStringList.addElement(Boolean.valueOf(isAccessible));
						}
						else
						{
							returnStringList.addElement(Boolean.FALSE);
						}
					}else{
						returnStringList.addElement(Boolean.FALSE);
					}
				}
			}
		}
		return returnStringList;
	}

	/**
	 * This method creates the Resoursce Pool Textbox and chooser for displaying in the Create Resource Request 
	 * @param context The matrix context object
	 * @throws Exception if operation fails
	 */  

	public String getResourcePoolTextBox(Context context,String[]args) throws Exception 
	{       
		String strOrgText = getResourcePoolStr(context,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING,true);

		return strOrgText.toString();
	}

	/**
	 * This method creates the Preferred Person Textbox and chooser for displaying in the Create Resource Request
	 * @param context The matrix context object
	 * @throws Exception if operation fails
	 */  

	public String getPreferredPersonTextBox(Context context,String[]args) throws Exception 
	{
		StringBuffer strOrgText = new StringBuffer();
		String strLanguage = context.getSession().getLanguage();
		String strClear = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Common.Clear", strLanguage);
		strOrgText.append("<input type='textbox' readonly='true' value='' name='PreferredPersonDisplay' id='PreferredPersonDisplay'></input>");
		strOrgText.append("<input type='hidden' name='PreferredPersonOID' id='PreferredPersonOID'></input>"); 
		strOrgText.append("<input type='hidden' name='PreferredPerson' id='PreferredPerson'></input>");
		strOrgText.append("<input type='button' name='btnPreferredPerson' ");
		strOrgText.append("size='200' value='...' ");
		strOrgText.append("onClick=\"javascript:selectResourcePoolPreferredPerson();\">");
		strOrgText.append("</input>");
		strOrgText.append("<a href=\"javascript:basicClear('PreferredPerson');javascript:basicClear('PreferredPersonDisplay');javascript:basicClear('PreferredPersonOID');\">");
		strOrgText.append(strClear);
		strOrgText.append("</a>");

		return strOrgText.toString();           
	}

	/**
	 * This method creates the Business Skill Textbox and chooser for displaying in the Create Resource Request
	 * @param context The matrix context object
	 * @throws Exception if operation fails
	 */  

	public String getbusinessSkillTextBox(Context context,String[]args) throws Exception 
	{
		String strOrgText = getbusinessSkillStr(context,DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

		return strOrgText.toString();      
	}


	/**
	 * @param context TODO
	 * @return
	 * @throws MatrixException 
	 */
	private String getbusinessSkillStr(Context context,String strName,String strId) throws MatrixException {
		try {
			String strLanguage = context.getSession().getLanguage();
			String strClear = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Clear", strLanguage);
			if(null==strName || "null".equalsIgnoreCase(strName))
			{strName=DomainConstants.EMPTY_STRING;}
			if(null==strId || "null".equalsIgnoreCase(strId))
			{strId=DomainConstants.EMPTY_STRING;}
			StringBuffer strOrgText = new StringBuffer();
			String strHostCompanyId = Company.getHostCompany(context);
			strOrgText.append("<input type='hidden' name='DefaultResourcePoolId' id='DefaultResourcePoolId' value='"+strHostCompanyId+"'></input>"  );

			strOrgText.append("<input type='textbox' readonly='true' value='"+XSSUtil.encodeForHTML(context,strName)+"' name='Business SkillDisplay' id='Business SkillDisplay'></input>");                
			strOrgText.append("<input type='hidden' name='Business Skill' id='Business Skill' "+XSSUtil.encodeForHTML(context,strId)+"></input>"  );
			strOrgText.append("<input type='button' name='btnBusinessSkill' ");
			strOrgText.append("size='200' value='...' ");
			strOrgText.append("onClick=\"javascript:selectResourcePoolSkill('Business Skill','Business SkillDisplay','Business SkillOID',document.forms[0].ResourcePoolOID);\">");
			strOrgText.append("</input>");
			strOrgText.append("<a href=\"javascript:basicClear('Business SkillDisplay');javascript:basicClear('Business Skill');\">");
			strOrgText.append(strClear);
			strOrgText.append("</a>");	                
			return strOrgText.toString();      
		} catch (FrameworkException e) {
			throw new MatrixException(e);
		} catch (Exception e) {
			throw new MatrixException(e);
		}      
	}

	/**
	 * This method creates the Resoursce Pool Textbox and chooser for displaying in the Create Resource Request 
	 * @param context The matrix context object
	 * @throws Exception if operation fails
	 */  

	public String getResourcePoolEditTextBox(Context context,String[]args) throws Exception 
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		String strResourceReqId = (String) requestMap.get("objectId");
		DomainObject strResourceReqDomObj =DomainObject.newInstance(context, strResourceReqId);
		StringList busSelect=new StringList();
		busSelect.add(SELECT_RESOURCE_POOL_NAME);
		busSelect.add(SELECT_RESOURCE_POOL_ID);
		Map mapResReqInfo = strResourceReqDomObj.getInfo(context, busSelect);        
		String strReturn = getResourcePoolStr(context,(String)mapResReqInfo.get(SELECT_RESOURCE_POOL_NAME),(String)mapResReqInfo.get(SELECT_RESOURCE_POOL_ID),false);
		return strReturn;
	}

	public String getBusinessSkillEditTextBox(Context context,String[]args) throws Exception 
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		String strResourceReqId = (String) requestMap.get("objectId");
		DomainObject strResourceReqDomObj =DomainObject.newInstance(context, strResourceReqId);
		StringList busSelect=new StringList();
		busSelect.add(SELECT_BUSINESS_SKILL_NAME);
		busSelect.add(SELECT_BUSINESS_SKILL_ID);
		Map mapResReqInfo = strResourceReqDomObj.getInfo(context, busSelect);

		String strReturn = getbusinessSkillStr(context,(String)mapResReqInfo.get(SELECT_BUSINESS_SKILL_NAME), (String)mapResReqInfo.get(SELECT_BUSINESS_SKILL_ID));
		return strReturn;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private String getResourcePoolStr(Context context,String strName,String strId,boolean hasFields) throws Exception {
		String strLanguage = context.getSession().getLanguage();
		String strClear = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Clear", strLanguage);
		if(ProgramCentralUtil.isNullString(strName)) {
			strName=DomainConstants.EMPTY_STRING;
		}
		if(ProgramCentralUtil.isNullString(strId)) {
			strId=DomainConstants.EMPTY_STRING;
		}
		StringBuffer strOrgText = new StringBuffer();    	
		strOrgText.append("<input type='textbox' readonly='true' value='"+XSSUtil.encodeForHTML(context, strName)+"' name='ResourcePoolDisplay' id='ResourcePoolDisplay'");
		if(hasFields) {
			strOrgText.append("onchange=\"javascript:basicClear('PreferredPersonDisplay');\"");
		}
		strOrgText.append("></input>");
		strOrgText.append("<input type='hidden' value='"+XSSUtil.encodeForHTML(context, strId)+"' name='ResourcePoolOID' id='ResourcePoolOID' ");
		if(hasFields)
		{
			strOrgText.append("onchange=\"javascript:basicClear('PreferredPersonOID');javascript:basicClear('PreferredPerson');\"");
		}
		strOrgText.append("></input>");
		strOrgText.append("<input type='button' name='btnOrganization' ");
		strOrgText.append("size='200' value='...' ");
		strOrgText.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp");
		strOrgText.append("?field=TYPES=type_Organization");
		strOrgText.append("&amp;table=PMCGenericResourcePoolSearchResults&amp;cancelLabel=emxProgramCentral.Common.Close&amp;selection=single");
		strOrgText.append("&amp;excludeOIDprogram=emxResourceRequest:getExcludeInActiveResourcePool");
		strOrgText.append("&amp;submitURL=../programcentral/emxProgramCentralResourceRequestAutonomySearchSelect.jsp");
		strOrgText.append("&amp;searchMode=GeneralResourcePoolMode&amp;fieldNameActual=ResourcePoolOID&amp;fieldNameDisplay=ResourcePoolDisplay&amp;fieldNameOID=ResourcePoolOID");
		strOrgText.append("&amp;suiteKey=ProgramCentral&amp;showInitialResults=true");
		strOrgText.append("&amp;checkStoredResult=true&amp;submitURL=AEFSearchUtil.jsp');\">");
		strOrgText.append("</input>");
		strOrgText.append("<a href=\"javascript:basicClear('ResourcePool');javascript:basicClear('ResourcePoolOID');");
		if(hasFields)
		{
			strOrgText.append("javascript:basicClear('PreferredPersonDisplay');javascript:basicClear('PreferredPersonOID');javascript:basicClear('PreferredPerson');");
		}
		strOrgText.append("\">");
		strOrgText.append(strClear);
		strOrgText.append("</a>");
		return strOrgText.toString();
	}
	/**
	 * @param strLanguage
	 * @param strValue
	 * @return
	 * @throws Exception
	 */
	private String getFteHourCostsValue(Context context,
			String strValue) throws Exception {		
		String strLanguage = context.getSession().getLanguage();
		if(null!=strValue && !"".equals(strValue) && !"null".equalsIgnoreCase(strValue)){
			strValue = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ResourceRequest.FTEHOURCOSTS." + strValue.toUpperCase(), strLanguage);
		}
		return strValue;
	}

	private void newUpdateFTEPhaseColumnData(Context context,BusinessObjectWithSelectList resourceRequestObjWithSelectList, 
			String strResourcePlanRelId,
			String strPhaseId, Double nNewFTEValue) throws MatrixException 
			{
		try 
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
			Map mapRequestRelResourcePlanData = new HashMap();
			String strResourceRequestStartDate = "";
			String strResourceRequestEndDate = "";
			String strResourceRequestId = "";
			StringList slPhaseIdList = new StringList();
			StringList slPhaseRelIdList = new StringList();
			StringList slPhaseFTEList = new StringList();
			StringList slPersonIdList = new StringList();
			StringList slPersonFTEList = new StringList();
			StringList slPersonStateList = new StringList();
			String strResourceReqFTE = "";
			String strResourceAllocatedFTE = "";
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
			{
				BusinessObjectWithSelect bows = itr.obj();
				strResourceRequestId = bows.getSelectData(SELECT_ID);
				strResourceRequestStartDate = bows.getSelectData(SELECT_ATTRIBUTE_START_DATE);
				strResourceRequestEndDate = bows.getSelectData(SELECT_ATTRIBUTE_END_DATE);
				slPhaseIdList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
				slPhaseFTEList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
				slPhaseRelIdList = bows.getSelectDataList(SELECT_RESOURCE_REQUEST_PHASE_RELATIONSHIP_ID);
				strResourceReqFTE = bows.getSelectData(SELECT_RESOURCE_PLAN_FTE);
			}
			Date requestStartDate = eMatrixDateFormat.getJavaDate(strResourceRequestStartDate);
			Date requestEndDate = eMatrixDateFormat.getJavaDate(strResourceRequestEndDate);
			DomainObject domRequestObj =DomainObject.newInstance(context, strResourceRequestId);
			DomainObject phaseDo = DomainObject.newInstance(context,strPhaseId);
			DomainRelationship domPhaseFTERelObj = null;
			int index = slPhaseIdList.indexOf(strPhaseId);
			double nOldPhaseFTE = 0d;
			if(slPhaseIdList.contains(strPhaseId))
			{   	
				String strRelID= (String)slPhaseRelIdList.get(index);
				nOldPhaseFTE = Task.parseToDouble((String)slPhaseFTEList.get(index));
				domPhaseFTERelObj=DomainRelationship.newInstance(context, strRelID);
			}
			else
			{
				domPhaseFTERelObj= DomainRelationship.connect(context,domRequestObj,ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE,phaseDo);
			}
			NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, true);
			DomainRelationship resourcePlanDomRelObj=DomainRelationship.newInstance(context, strResourcePlanRelId);
			FTE resourcePlanFTE = FTE.getInstance(context, strResourceReqFTE);
			StringList slBusSelects =  new StringList();
			slBusSelects.add(ATTRIBUTE_PHASE_START_DATE);
			slBusSelects.add(ATTRIBUTE_PHASE_FINISH_DATE);
			Map mapPhaseInfo = phaseDo.getInfo(context, slBusSelects);
			Date phaseStartDate = eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_START_DATE));
			Date phaseEndDate = eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_FINISH_DATE));

			String strFinalResourcePlanFTEvalue = updatePhaseResourcePlanFTE(context, nNewFTEValue,
					nOldPhaseFTE, resourcePlanFTE, phaseStartDate, phaseEndDate);
			domPhaseFTERelObj.setAttributeValue(context, ATTRIBUTE_FTE, String.valueOf(nNewFTEValue));
			resourcePlanDomRelObj.setAttributeValue(context, ATTRIBUTE_FTE, strFinalResourcePlanFTEvalue);

			updateRequestDateByPhase(context, simpleDateFormat,
					requestStartDate, requestEndDate, phaseStartDate,
					phaseEndDate, domRequestObj);
		}
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
			}


	private String updatePhaseResourcePlanFTE(Context context,
			Double nNewFTEValue, double nOldPhaseFTE, FTE resourcePlanFTE,
			Date phaseStartDate, Date phaseEndDate)
	throws Exception, MatrixException, FrameworkException 
	{  
		String strFinalResourcePlanFTEvalue="";
		ResourceRequest resourceRequest = new ResourceRequest();
		double newResourcePlanTimeFrameFTE = 0d;
		double oldResourcePlanTimeFrameFTE = 0d;
		Map newPhaseFTEMap = resourceRequest.calculateFTE(context, phaseStartDate, phaseEndDate, nNewFTEValue+"");
		Map oldPhaseFTEMap = resourceRequest.calculateFTE(context, phaseStartDate, phaseEndDate, nOldPhaseFTE+"");
		for (Iterator objectListIterator = newPhaseFTEMap.keySet().iterator();objectListIterator.hasNext();)
		{
			String strTimeline = (String) objectListIterator.next();
			String[] strTimeLineSpilt = strTimeline.split("-");
			int nTimeFrame = Integer.parseInt(strTimeLineSpilt[0]);
			int nTimeYear = Integer.parseInt(strTimeLineSpilt[1]);
			newResourcePlanTimeFrameFTE = 0;
			double newPhaseFTEValue = 0d;
			double oldPhaseFTEValue = 0d;
			if(null!=newPhaseFTEMap.get(strTimeline))
			{
				newPhaseFTEValue = (Double)newPhaseFTEMap.get(strTimeline);
			}
			if(null!=oldPhaseFTEMap.get(strTimeline))
			{
				oldPhaseFTEValue = (Double)oldPhaseFTEMap.get(strTimeline);
			}
			oldResourcePlanTimeFrameFTE=resourcePlanFTE.getFTE(nTimeYear, nTimeFrame);
			if(newPhaseFTEValue>0)
			{
				newResourcePlanTimeFrameFTE=oldResourcePlanTimeFrameFTE+(newPhaseFTEValue-oldPhaseFTEValue);
			}
			resourcePlanFTE.setFTE(nTimeYear, nTimeFrame, newResourcePlanTimeFrameFTE);
		}
		strFinalResourcePlanFTEvalue= resourcePlanFTE.getXML();
		return strFinalResourcePlanFTEvalue;
	}


	private void updateRequestDateByPhase(Context context,
			SimpleDateFormat simpleDateFormat, Date requestStartDate,
			Date requestEndDate, Date phaseStartDate, Date phaseEndDate,
			DomainObject domRequestObj) throws FrameworkException {
		if(null==requestStartDate ||(null!=requestStartDate && requestStartDate.after(phaseStartDate)))
		{
			requestStartDate = phaseStartDate; 
		}
		if(null==requestEndDate||(null!=requestEndDate && requestEndDate.before(phaseEndDate)))
		{
			requestEndDate = phaseEndDate;
		}
		String stringStartDate  = simpleDateFormat.format(requestStartDate);
		String stringFinishDate = simpleDateFormat.format(requestEndDate);
		domRequestObj.setAttributeValue(context,DomainConstants.ATTRIBUTE_START_DATE, stringStartDate);
		domRequestObj.setAttributeValue(context,DomainConstants.ATTRIBUTE_END_DATE, stringFinishDate);
	}

	private void newUpdateFTEPersonPhaseColumnData(Context context,BusinessObjectWithSelectList resourceRequestObjWithSelectList, 
			String strRelId, String strPersonId,
			String strPhaseId, String strResourceAllocatedFTE, Double nNewFTEValue) throws MatrixException{
		boolean flag = false;	
		try 
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat());
			Map mapRequestRelResourcePlanData = new HashMap();
			String strResourceRequestStartDate = "";
			String strResourceRequestEndDate = "";
			String strResourceRequestId = "";
			StringList slPhaseIdList = new StringList();
			StringList slPhaseRelIdList = new StringList();
			StringList slPhaseFTEList = new StringList();
			StringList slPersonIdList = new StringList();
			StringList slPersonFTEList = new StringList();
			StringList slPersonStateList = new StringList();
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
			{
				BusinessObjectWithSelect bows = itr.obj();
				strResourceRequestId = bows.getSelectData(SELECT_ID);
				strResourceRequestStartDate = bows.getSelectData(SELECT_ATTRIBUTE_START_DATE);
				strResourceRequestEndDate = bows.getSelectData(SELECT_ATTRIBUTE_END_DATE);
				slPhaseIdList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
				slPhaseFTEList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
				slPhaseRelIdList = bows.getSelectDataList(SELECT_RESOURCE_REQUEST_PHASE_RELATIONSHIP_ID);
			}
			Date requestStartDate = eMatrixDateFormat.getJavaDate(strResourceRequestStartDate);
			Date requestEndDate = eMatrixDateFormat.getJavaDate(strResourceRequestEndDate);
			int index = slPhaseIdList.indexOf(strPhaseId);
			NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, true);
			String strFinalPhaseFTEvalue ="";
			String strFinalResourcePlanFTEvalue="";
			FTE allocatedFTE = FTE.getInstance(context, strResourceAllocatedFTE);
			StringList slBusSelects =  new StringList();
			slBusSelects.add(ATTRIBUTE_PHASE_START_DATE);
			slBusSelects.add(ATTRIBUTE_PHASE_FINISH_DATE);
			DomainObject phaseDo = DomainObject.newInstance(context,strPhaseId);
			Map mapPhaseInfo = phaseDo.getInfo(context, slBusSelects);
			Date phaseStartDate = eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_START_DATE));
			Date phaseEndDate = eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_FINISH_DATE));
			FTE fte = FTE.getInstance(context);
			double newAllocatedTimeFrameFTE = 0d;
			double oldAllocatedTimeFrameFTE = 0d;
			double nOldPhaseFTE = 0d;
			DomainObject domRequestObj =DomainObject.newInstance(context, strResourceRequestId);
			DomainRelationship dmrPersonPhaseFTE = null;
			String strPersonPhaseFTEId = "";
			boolean isCreateNewRel = false;
			String strRelPhaseFTEId = ""; 
			if(slPhaseIdList.contains(strPhaseId))
			{   	
				strRelPhaseFTEId = (String)slPhaseRelIdList.get(index);
				String sCommandStatement = "print bus $1 select $2 dump";
				strPersonPhaseFTEId =  MqlUtil.mqlCommand(context, sCommandStatement, strPersonId, "from[Person Phase FTE|torel.id=="+strRelPhaseFTEId+"].id"); 
				if(null==strPersonPhaseFTEId || "".equals(strPersonPhaseFTEId) || "Null".equalsIgnoreCase(strPersonPhaseFTEId))
				{
					isCreateNewRel = true;
				}
			}
			else
			{
				DomainRelationship domPhaseFTERelObj = DomainRelationship.connect(context,domRequestObj,ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE,phaseDo);
				strRelPhaseFTEId = domPhaseFTERelObj.getName();
				isCreateNewRel = true;
			}
			if(isCreateNewRel)
			{
				strPersonPhaseFTEId = ResourceRequest.connectPersonToPhaseFTERel(
						context, strPersonId, strRelPhaseFTEId);
			}
			dmrPersonPhaseFTE = DomainRelationship.newInstance(context,strPersonPhaseFTEId);
			if(isCreateNewRel)
			{
				String strOldPhaseFTE= dmrPersonPhaseFTE.getAttributeValue(context,ATTRIBUTE_FTE);
				nOldPhaseFTE =  Task.parseToDouble(strOldPhaseFTE);
			}
			ResourceRequest resourceRequest = new ResourceRequest();
			Map newPhaseFTEMap = resourceRequest.calculateFTE(context, phaseStartDate, phaseEndDate, nNewFTEValue+"");
			Map oldPhaseFTEMap = resourceRequest.calculateFTE(context, phaseStartDate, phaseEndDate, nOldPhaseFTE+"");
			for (Iterator objectListIterator = newPhaseFTEMap.keySet().iterator();objectListIterator.hasNext();)
			{
				String strTimeline = (String) objectListIterator.next();
				String[] strTimeLineSpilt = strTimeline.split("-");
				int nTimeFrame = Integer.parseInt(strTimeLineSpilt[0]);
				int nTimeYear = Integer.parseInt(strTimeLineSpilt[1]);
				newAllocatedTimeFrameFTE = 0;
				double newPhaseFTEValue = 0d;
				double oldPhaseFTEValue = 0d;
				if(null!=newPhaseFTEMap.get(strTimeline))
				{
					newPhaseFTEValue = (Double)newPhaseFTEMap.get(strTimeline);
				}
				if(null!=oldPhaseFTEMap.get(strTimeline))
				{
					oldPhaseFTEValue = (Double)oldPhaseFTEMap.get(strTimeline);
				}

				oldAllocatedTimeFrameFTE = allocatedFTE.getFTE(nTimeYear, nTimeFrame);
				if(newPhaseFTEValue>0)
				{
					newAllocatedTimeFrameFTE=oldAllocatedTimeFrameFTE+(newPhaseFTEValue-oldPhaseFTEValue);
				}
				allocatedFTE.setFTE(nTimeYear, nTimeFrame, newAllocatedTimeFrameFTE);
			}
			strFinalResourcePlanFTEvalue= allocatedFTE.getXML();
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			flag = true;	
			dmrPersonPhaseFTE.setAttributeValue(context, ATTRIBUTE_FTE, String.valueOf(nNewFTEValue));
			DomainRelationship dmrAllocated = DomainRelationship.newInstance(context,strRelId);
			dmrAllocated.setAttributeValue(context, ATTRIBUTE_FTE, strFinalResourcePlanFTEvalue);
			updateRequestDateByPhase(context, simpleDateFormat,
					requestStartDate, requestEndDate, phaseStartDate,
					phaseEndDate, domRequestObj);
		}
		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
		finally 
		{
			if(flag)
			{
				ContextUtil.popContext(context);
			}
		}
	}

	/* Decides whether to display Edit link on rpoperties page of resource request
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isRequestDateEditable (Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strRequestObjId = (String) programMap.get("objectId");
		boolean isCommandEnabled= true;
		String resourcePoolId = (String) programMap.get("resourcePoolId");

		if(null != resourcePoolId){
			isCommandEnabled = false;
			return isCommandEnabled;
		}else
		{
			DomainObject dmoReqObject = DomainObject.newInstance(context, strRequestObjId);
			String strProjectId = dmoReqObject.getInfo(context,SELECT_PROJECT_SPACE_ID);
			DomainObject dmoObject = DomainObject.newInstance(context, strProjectId);
			String strResourcePlanPref = getResourcePlanPreference(context, dmoObject);
			if(RESOURCE_PLAN_PREFERENCE_TIMELINE.equals(strResourcePlanPref))
			{
				isCommandEnabled = isRequestEditable(context, args);
			}
			else
			{
				isCommandEnabled = false;
			}
			return isCommandEnabled;
		}
	}

	/* Decides whether to display Edit link on rpoperties page of resource request
	 * @param context The Matrix Context object
	 * @param args The packed arguments
	 * @return true if column is to be shown
	 * @throws Exception if operation fails
	 */
	public boolean isModePhaseEnabled (Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String strRequestObjId = (String) programMap.get("objectId");
		boolean isCommandEnabled = false;
		String resourcePoolId = (String) programMap.get("resourcePoolId");

		if(null != resourcePoolId){
			isCommandEnabled = false;
			return isCommandEnabled;
		}else
		{
			DomainObject dmoReqObject = DomainObject.newInstance(context, strRequestObjId);
			String strProjectId = dmoReqObject.getInfo(context,SELECT_PROJECT_SPACE_ID);
			DomainObject dmoObject = DomainObject.newInstance(context, strProjectId);
			String strResourcePlanPref = getResourcePlanPreference(context, dmoObject);
			if(RESOURCE_PLAN_PREFERENCE_PHASE.equals(strResourcePlanPref))
			{
				isCommandEnabled = true;
			}
			return isCommandEnabled;

		}
	}

	/**
	 * @deprecated
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 * @{@link getCurrencyUnit(Context context)}
	 */
	public static String getCurrencyUnit(Context context, String[]args) throws MatrixException
	{
		return ProgramCentralUtil.getCurrencyUnit(context);
	}


	public String getStandardCostTextBox(Context context,String[]args) throws Exception 
	{
		try 
		{
			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			Map programMap 			= (Map) JPO.unpackArgs(args);
			StringBuffer	currencyBuffer	= new StringBuffer();
			Map requestMap = (Map)programMap.get("requestMap");
			String strResourceReqId = (String) requestMap.get("objectId");
			DomainObject doResourceReq = DomainObject.newInstance(context,strResourceReqId);
			String strFteValue ="";	
			double standardCost = 0;
			String strCurrencyFilter = ProgramCentralUtil.getCurrencyUnit(context);
			if(doResourceReq.isKindOf(context, TYPE_RESOURCE_REQUEST))
			{
				PersonUtil contextUser =  new PersonUtil();
				String userCompanyID =contextUser.getUserCompanyId(context);
				Map conversionMap = new HashMap();
				conversionMap = ProgramCentralUtil.getCurrencyConversionMap(context, userCompanyID);
				Map selectMap = new HashMap();
				selectMap.put("conversionMap",conversionMap);
				selectMap.put("currencyFilter",strCurrencyFilter);
				selectMap.put("resourceReqId",strResourceReqId);
				selectMap.put("isPoolCostConsidered", "false");
				standardCost = getStandardCostValue(context, selectMap);
			}
			currencyBuffer	= new StringBuffer();
			currencyBuffer.append("<input type=\"textbox\"  value=\""+ emxProgramCentralUtil_mxJPO.getFormattedNumberValue(context, locale, standardCost)+ "\"  name=\"StandardCost\" ");
			currencyBuffer.append("/>");
			currencyBuffer.append(emxProgramCentralUtil_mxJPO.getCurrencySymbol(context, locale,strCurrencyFilter));
			return currencyBuffer.toString();
		} 
		catch (Exception e) 
		{			
			throw e;
		}
	}

	public Vector getColumnRequestStandardCostData(Context context,String[] args) throws Exception
	{
		try
		{
			Locale locale = ProgramCentralUtil.getLocale(context);
			String strLanguage 	  = locale.getLanguage();
			Vector vecResult = new Vector();
			Map programMap = (Map) JPO.unpackArgs(args);
			Map paramList = (Map) programMap.get("paramList");
			PersonUtil contextUser =  new PersonUtil();
			String userCompanyID =contextUser.getUserCompanyId(context);
			MapList objectList = (MapList) programMap.get("objectList");
			Map mapRowData = null;
			String strObjectId = "";
			String strStandardCost = "";
			String strStandardCostCurrency = "";
			DomainObject dmoObject;
			String strFullName ="";
			String strCurrencyFilter =(String) paramList.get("PMCResourceRequestCurrencyFilter");
			String strDefaultCurrencyDisplay = "";
			if(null==strCurrencyFilter || "".equals(strCurrencyFilter) || "Null".equalsIgnoreCase(strCurrencyFilter))
			{
				strCurrencyFilter =(String) paramList.get("PMCResourcePlanTemplateCurrencyFilter");
			}
			if(null==strCurrencyFilter || "".equals(strCurrencyFilter) || "Null".equalsIgnoreCase(strCurrencyFilter))
			{
				strCurrencyFilter = ProgramCentralUtil.getCurrencyUnit(context);
				strDefaultCurrencyDisplay = strCurrencyFilter;
			}
			for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) 
			{
				Map mapObjectInfo = (Map) itrObjects.next();
				strObjectId = (String)mapObjectInfo.get(SELECT_ID);
				DomainObject doResourceReq = DomainObject.newInstance(context,strObjectId);
				String strFteValue ="";				
				String SELECT_IS_PROJECT_SPACE = "type.kindof["+TYPE_PROJECT_SPACE+"]";
				StringList slSelects = new StringList();
				slSelects.add(DomainConstants.SELECT_ID);
				slSelects.add(SELECT_IS_PROJECT_SPACE);
				slSelects.add(SELECT_IS_RESOURCE_REQUEST);
				Map mapBookmarkInfo = doResourceReq.getInfo(context,slSelects);

				String sIsProjectSpaceType = (String) mapBookmarkInfo.get(SELECT_IS_PROJECT_SPACE);
				String sIsResourceRequestType = (String) mapBookmarkInfo.get(SELECT_IS_RESOURCE_REQUEST);
				if(null != sIsProjectSpaceType && "TRUE".equalsIgnoreCase(sIsProjectSpaceType.trim()))
				{
					vecResult.add("");
				}
				if(null != sIsResourceRequestType && "TRUE".equalsIgnoreCase(sIsResourceRequestType.trim()))
				{
					Map selectMap = new HashMap();
					selectMap.put("currencyFilter",strCurrencyFilter);
					selectMap.put("resourceReqId",strObjectId);
					selectMap.put("isPoolCostConsidered", "false");
					double standardCost = getStandardCostValue(context, selectMap);
					vecResult.add(Currency.format(context, strCurrencyFilter, standardCost));
				}
				else
				{
					Map mapColMap = (Map)programMap.get("columnMap");
					mapColMap.put("Editable", "false");
					vecResult.add("-");
				}
			}
			return vecResult;
		}
		catch (Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * Updates the standard cost of a resource request object.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request parameters
	 * @throws MatrixException if operation fails.
	 */
	public void updateResourceRequestCost(Context context, String[] args) throws MatrixException 
	{
		try{
			Map programMap = (Map)JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramMap");
			Map requestMap = (Map) programMap.get("requestMap");
			String requestId = (String) paramMap.get("objectId");
			String strNewValue = (String) paramMap.get("New Value");
			DomainObject domObject = DomainObject.newInstance(context,requestId);
			String projectId  = "";

			try{
				if(ProgramCentralUtil.isNullString(projectId)){
					projectId = (String) requestMap.get("parentOID");
				}
				if(ProgramCentralUtil.isNullString(projectId)){
					projectId = (String) requestMap.get("projectID");
				}
				if(ProgramCentralUtil.isNullString(projectId)){
					throw new Exception();
				}
			}catch(Exception e){				
				projectId = (String)domObject.getInfo(context, SELECT_PROJECT_SPACE_ID);
			}

			if(ProgramCentralUtil.isNullString(strNewValue)){
				String strCostValue[] = (String[])requestMap.get("StandardCost");
				if(null!=strCostValue && strCostValue.length>0){
					strNewValue = strCostValue[0];
				}else
					strNewValue = "0.0";
			}
			String baseCurrency = Currency.getBaseCurrency(context, projectId);
			
			Locale currencyLocale = Currency.getCurrencyLocale(context,baseCurrency);
			NumberFormat nFormater = NumberFormat.getInstance(currencyLocale);
			Number numericValue =  nFormater.parse(strNewValue);
			strNewValue = numericValue.toString();
			
			strNewValue = Currency.toBaseCurrency(context, projectId,strNewValue,false);
			domObject.setAttributeValue(context,ATTRIBUTE_STANDARD_COST,strNewValue + ProgramCentralConstants.SPACE + baseCurrency);			
		}catch (Exception e){
			throw new MatrixException(e);
		}
	}

	/**
	 * Add the resources from the resource request when ResourcePlanPrefernce is "Phase".
	 * 
	 * @param context The Matrix Context object
	 * @param args packed arguments.This is unpacked to maplist which gives the resource to be assigned for this requests. 
	 *                          This MapList will contain maps with following information.
	 *                           Key "id" Value the person object id
	 *                           Key "FTE" Value the FTE object initilized for this person assignment
	 *                           Key "Resource State" Value any of the range values for attribute "Resource State" (Requested/Proposed/Committed)
	 *                           Each of this persons will be connected to resource request object with "Allocated" relationship having
	 *                           attribute "Resource State" = given Resource State.
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public void addResourcesToRequestByPhase(Context context,String[] args) throws MatrixException
	{
		boolean flag = false;	
		try{
			ContextUtil.startTransaction(context, true);

			MapList mlResourceList = (MapList) JPO.unpackArgs(args);
			Map mapResourceMap = null;
			StringBuffer sBuff = new StringBuffer();
			String strResourceId = "";
			String strResourceState = "";
			String strRequestId = "";
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");

			for (Iterator itrResource = mlResourceList.iterator(); itrResource.hasNext();)
			{
				mapResourceMap = (Map) itrResource.next();
				strResourceId = (String)mapResourceMap.get("Resource_Id");
				DomainObject dmoResourcePerson = DomainObject.newInstance(context,strResourceId); 
				strRequestId = (String)mapResourceMap.get("RequestId");
				DomainObject dmoRequest = DomainObject.newInstance(context,strRequestId);
				StringList slRequestPhaseIdList = dmoRequest.getInfoList(context, ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
				MapList mlFTE = (MapList)mapResourceMap.get("FTE");
				strResourceState = (String)mapResourceMap.get("ResourceState");
				String strMode= (String)mapResourceMap.get("mode");

				FTE fte = FTE.getInstance(context);
				String strFTEValue = "";
				Iterator objectListIterator = mlFTE.iterator();
				Map mapFTE = null;

				while (objectListIterator.hasNext())
				{
					mapFTE = (Map) objectListIterator.next();
					String strMonthYear = "";
					String[] strMonthYearSpilt = null;
					int nMonth = 0;
					int nYear  = 0;
					double nFTE= 0d;
					String strFTE = "";
					String strPhaseId="";

					if(strMode.equals("getTableResourcePlanRequestData"))
					{
						if(null!= mapFTE && !"null".equals(mapFTE))
						{
							strPhaseId= (String)mapFTE.get("PhaseId");
							strFTE = (String)mapFTE.get("PhaseOID-"+strPhaseId);
							nFTE = Task.parseToDouble(strFTE);
							DomainObject phaseDo = DomainObject.newInstance(context, strPhaseId);
							if(!slRequestPhaseIdList.contains(strPhaseId))
							{
								DomainRelationship domPhaseFTERelObj = DomainRelationship.connect(context,dmoRequest,ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE,phaseDo);
							}
							String strPhaseStartDate = phaseDo.getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE);
							String strPhaseFinishDate = phaseDo.getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
							Date fromDate=eMatrixDateFormat.getJavaDate(strPhaseStartDate);
							Date toDate=eMatrixDateFormat.getJavaDate(strPhaseFinishDate);
							ResourceRequest resourceRequest = new ResourceRequest();
							Map mapCalculatedFTE = resourceRequest.calculateFTE(context, fromDate, toDate, strFTE);
							FTE ftePlan = FTE.getInstance(context);
							ftePlan.setAllFTE(mapCalculatedFTE);
							fte = ResourcePlanTemplate.getCalculatedFTEMap(ftePlan, fte);
						}
						String strPhaseFTERelId = getPhaseFTERelId(context,
								strRequestId, strPhaseId); 
						DomainRelationship phaseFTERelDo = DomainRelationship.newInstance(context, strPhaseFTERelId);
						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						flag = true;     
						String strPersonPhaseFTERelId = ResourceRequest.connectPersonToPhaseFTERel(
								context, strResourceId, strPhaseFTERelId);
						DomainRelationship personPhaseFTERelDo = DomainRelationship.newInstance(context,strPersonPhaseFTERelId);
						personPhaseFTERelDo.setAttributeValue(context, ATTRIBUTE_FTE, strFTE);
					} 
					else{
						if(null!= mapFTE && !"null".equals(mapFTE)){
							FTE ftePlan = FTE.getInstance(context);
							for (Iterator iter = mapFTE.keySet().iterator(); iter.hasNext();) 
							{
								StringList slToConnectPersonPhaseFTEList = new StringList();
								strMonthYear = (String) iter.next();
								nFTE = Task.parseToDouble((String)mapFTE.get(strMonthYear));
								strMonthYearSpilt = strMonthYear.split("-");
								nMonth = Integer.parseInt(strMonthYearSpilt[0]);
								nYear = Integer.parseInt(strMonthYearSpilt[1]);
								ftePlan.setFTE(nYear, nMonth, nFTE);
								//This method will update FTE for relationship "PersonPhaseFTE"
							}    						  			
							fte = ResourcePlanTemplate.getCalculatedFTEMap(ftePlan, fte);
							updatePersonPhaseFTE(context,strResourceId,strRequestId,fte);
						}
					}
				}
				strFTEValue = fte.getXML();
				DomainRelationship domainRelationship = DomainRelationship.connect(context,dmoRequest,DomainConstants.RELATIONSHIP_ALLOCATED, dmoResourcePerson);
				domainRelationship.setAttributeValue(context, ATTRIBUTE_FTE,strFTEValue);
				domainRelationship.setAttributeValue(context, ATTRIBUTE_RESOURCE_STATE, strResourceState);  
				if(DomainConstants.ATTRIBUTE_RESOURCE_STATE_RANGE_COMMITTED.equalsIgnoreCase(strResourceState))
				{
					assignPeoplesToProjectSpace(context, strRequestId);
				}
			}
			ContextUtil.commitTransaction(context);
		}
		catch (Exception e) {
			ContextUtil.abortTransaction(context);
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
	//END:Added:12-Aug-2010:s4e:R210 PRG:ARP

	//Added:26-Aug-2010:s4e:R210 PRG:ARP
	//Added to connect Person To relationship "PhaseFTE" by relationship "PersonPhaseFTE" when ResourcePlanPreference="Phase"
	//FTE is also divided according to Timeframes for Phase and for Request by Hours by Hours basis
	/**
	 * updatePersonPhaseFTE  when ResourcePlanPrefernce is "Phase".
	 * 
	 * @param context The Matrix Context object
	 * @param strResourceId Request Resource Id
	 * @param strRequestId Resource Request Id
	 * @param nMonth Timeframe month
	 * @param nYear Timeframe Year
	 * @param nFTE FTE for timeframe
	 * @throws MatrixException if operation fails or passed invalid arguments
	 */
	public void updatePersonPhaseFTE(Context context,String strResourceId,String strRequestId,FTE fte) throws Exception
	{
		boolean flag=false;
		try{
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			DomainObject dmoRequest = DomainObject.newInstance(context,strRequestId);
			String strRelationshipPattern = ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE;
			String strTypePattern =ProgramCentralConstants.TYPE_PHASE;
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ID);
			slBusSelect.add(SELECT_NAME);
			slBusSelect.add(ATTRIBUTE_PHASE_START_DATE);
			slBusSelect.add(ATTRIBUTE_PHASE_FINISH_DATE);

			StringList slRelSelect = new StringList();
			slRelSelect.add(DomainRelationship.SELECT_TO_ID);
			slRelSelect.add(DomainRelationship.SELECT_TO_NAME);
			boolean getFrom = true;
			boolean getTo = true;
			short recurseToLevel = 1;
			String strBusWhere = "";
			String strRelWhere = "";

			MapList mlPhaseList = dmoRequest.getRelatedObjects(context,
					strRelationshipPattern, //pattern to match relationships
					strTypePattern, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
					null, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getTo, //get To relationships
					getFrom, //get From relationships
					recurseToLevel, //the number of levels to expand, 0 equals expand all.
					strBusWhere, //where clause to apply to objects, can be empty ""
					strRelWhere,//where clause to apply to relationship, can be empty ""
					0); //limit

			Map mapPhaseFTEValue = calculatePhaseFTEReverse(context, fte,
					mlPhaseList);

			for (Iterator iterator = mapPhaseFTEValue.keySet().iterator(); iterator.hasNext();) {
				String strPersonPhaseFTERelId="";
				String strToConnectPhase = (String)iterator.next();  
				String strToConnectPhaseId = strToConnectPhase.substring("PhaseOID-".length(),strToConnectPhase.length());
				String strPhaseFTERelId = getPhaseFTERelId(context,strRequestId, strToConnectPhaseId); 
				strPersonPhaseFTERelId=getPersonPhaseFTERelId(context, strResourceId, strPhaseFTERelId);
				String strPersonPhasaeFTEValue = (String)mapPhaseFTEValue.get(strToConnectPhase);
				if(null==strPersonPhaseFTERelId || "null".equalsIgnoreCase(strPersonPhaseFTERelId) || "".equals(strPersonPhaseFTERelId))
				{
					strPersonPhaseFTERelId = ResourceRequest.connectPersonToPhaseFTERel(context, strResourceId, strPhaseFTERelId);
				}
				DomainRelationship personPhaseFTERelDo = DomainRelationship.newInstance(context,strPersonPhaseFTERelId);
				personPhaseFTERelDo.setAttributeValue(context, ATTRIBUTE_FTE, strPersonPhasaeFTEValue);
			}
		}             
		catch (Exception e) {
			ContextUtil.abortTransaction(context);
			throw new MatrixException(e);
		}
		finally 
		{
			ContextUtil.popContext(context);
		}
	}

	/**
	 * The method gets the selected resource requests, finds the associated resource pools and if
	 * resource pool is associated with the request then assign the standard cost of the 
	 * corrosponding resource pool to that request.
	 * @author RG6
	 * @param context The Matrix Context object
	 * @param resourceRequestIds - StringList containing selected resource request
	 * @throws MatrixException if operation fails or passed invalid arguments
	 * @since R210
	 */  
	public void assignResourcePoolStandardCost(Context context,String[] args)  throws MatrixException 
	{
		try 
		{
			ContextUtil.startTransaction(context, true);
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			StringList slSelectedResourceReqIds = (StringList) programMap.get("resourceRequestIds");
			if(slSelectedResourceReqIds == null){
				throw new IllegalArgumentException();
			}
			DomainObject dObjResourceReq = DomainObject.newInstance(context);
			DomainObject dObjResourcePool = DomainObject.newInstance(context);
			StringList slAttributeSelect = new StringList();
			slAttributeSelect.add(SELECT_ATTRIBUTE_STANDARD_COST);
			slAttributeSelect.add(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY);
			for(int i=0;i<slSelectedResourceReqIds.size();i++){
				String strResourceReqId = (String)slSelectedResourceReqIds.get(i);
				dObjResourceReq.setId(strResourceReqId);
				String resourcePoolId = dObjResourceReq.getInfo(context, SELECT_RESOURCE_POOL_ID);
				if(resourcePoolId != null && ! "".equalsIgnoreCase(resourcePoolId)){
					dObjResourcePool.setId(resourcePoolId);
					Map dataMap = dObjResourcePool.getInfo(context, slAttributeSelect);
					String strResourcePoolStandardCost = (String)dataMap.get(SELECT_ATTRIBUTE_STANDARD_COST);
					String strResourcePoolStandardCostCurrency =(String)dataMap.get(SELECT_ATTRIBUTE_STANDARD_COST_CURRENCY);
					dObjResourceReq.setAttributeValue(context,ATTRIBUTE_STANDARD_COST,strResourcePoolStandardCost+" "+strResourcePoolStandardCostCurrency);
				}
			}
			ContextUtil.commitTransaction(context);
		}catch (Exception exp)
		{
			ContextUtil.abortTransaction(context);
			throw new MatrixException(exp);
		}	
	}

	/**
	 * The method gets the resources assigned to the selected request 
	 * @author ms9
	 * @param context The Matrix Context object
	 * @param 
	 * @throws MatrixException if operation fails or passed invalid arguments
	 * @since R210
	 */
	public String getResources(Context context,String[] args)  throws MatrixException 
	{
		StringBuffer strPersonList= new StringBuffer(100);
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			String strObjectId = (String)requestMap.get("objectId");

			DomainObject domObj = DomainObject.newInstance(context,strObjectId);

			MapList mpList = new MapList();
			String RELATIONSHIPS = ProgramCentralConstants.RELATIONSHIP_ALLOCATED;
			String TYPES = ProgramCentralConstants.TYPE_PERSON;
			StringList objectSelects = new StringList(3);
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);

			StringList relationshipSelects = new StringList();
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			mpList = domObj.getRelatedObjects(context,
					RELATIONSHIPS,
					TYPES,
					objectSelects,				// object Selects
					relationshipSelects,       	// relationship Selects
					true,      					// getTo
					true,       				// getFrom
					(short) 1,  				// recurseToLevel
					null,       				// objectWhere
					null,						// relationshipWhere
					0);

			if(!mpList.isEmpty() && null!=mpList)
			{
				int personInfoSize = mpList.size();
				for (int i = 0; i < personInfoSize; i++) 
				{
					Map tempMap = (Map)mpList.get(i);
					strPersonList.append(PersonUtil.getFullName(context,(String)tempMap.get(SELECT_NAME)));
					if(i<personInfoSize-1)
					{
						strPersonList.append("; ");
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new MatrixException(e);
		}
		return strPersonList.toString();
	}

	/**
	 * The method is called from the webform PMCResourceRequestNormalForm for the field StandardCost in order to 
	 * show the formattted currentcy value.
	 * @author RG6
	 * @param context The Matrix Context object
	 * @param resourceRequestId - 
	 * @throws MatrixException if operation fails or passed invalid arguments
	 * @since R210.HF1 and R212
	 */  
	public String getStandardCostLabel(Context context,String[] args) throws Exception 
	{
		try{
			Map programMap 			= (Map) JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");
			String strResourceReqId = (String) requestMap.get("objectId");
			DomainObject doResourceReq = DomainObject.newInstance(context,strResourceReqId);
			String strFteValue ="";	
			double standardCost = 0;
			String strCurrencyFilter = ProgramCentralUtil.getCurrencyUnit(context);
			if(doResourceReq.isKindOf(context, TYPE_RESOURCE_REQUEST)){
				Map selectMap = new HashMap();
				selectMap.put("currencyFilter",strCurrencyFilter);
				selectMap.put("resourceReqId",strResourceReqId);
				selectMap.put("isPoolCostConsidered", "false");
				standardCost = getStandardCostValue(context, selectMap);
			}
			return Currency.format(context, strCurrencyFilter, standardCost);
		}catch (Exception e){			
			throw e;
		}
	}

	//Added:25-Jan-2011:hp5:R211 PRG:IR-075153V6R2012
	public Vector getPersonRoles(Context context, String[] args) throws MatrixException
	{
		Vector roleVec = new Vector();
		Map mapObject = null;
		String personId = null;
		String STR_SEPARATOR= ",";
		String STR_TILD = "~";
		StringList slObjectList = new StringList(SELECT_ID);
		StringList slRelList = new StringList("attribute["+ATTRIBUTE_PROJECT_ROLE+"]");
		String select_role = "attribute["+ATTRIBUTE_PROJECT_ROLE+"]";
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramList");
			MapList objectList = (MapList) programMap.get("objectList");
			String strLanguage = (String)paramMap.get("languageStr");
			Iterator objectListIterator = objectList.iterator();
			while (objectListIterator.hasNext()) {
				mapObject = (Map) objectListIterator.next();
				personId = (String) mapObject.get(SELECT_ID);
				if(ProgramCentralUtil.isNotNullString(personId)) {
					DomainObject personDObj = DomainObject.newInstance(context,personId);					 
					MapList mlRole = personDObj.getRelatedObjects(context,
							RELATIONSHIP_MEMBER,TYPE_COMPANY,slObjectList,slRelList,
							true, false, (short)1,null,EMPTY_STRING,0);
					if(null!=mlRole && mlRole.size()>0) {
						for(int k=0;k<mlRole.size();k++) {
							Map roleMap = (Map)mlRole.get(k);
							String strRole = (String)roleMap.get(select_role);
							StringList roleList = FrameworkUtil.split(strRole,STR_TILD);
							StringBuffer sbRole = new StringBuffer();
							String strrole = "";
							String roles = "";

							for(int i=0;i<roleList.size();i++) 
							{
								strrole = (String)roleList.get(i);
								strrole = PropertyUtil.getSchemaProperty(context,strrole);
								strrole = i18nNow.getAdminI18NString("Role", strrole.trim() ,strLanguage);
								roles += strrole + ", ";
							}
							if (!roles.equals(""))
							{
								roles = roles.substring(0,(roles.length())-2);
							}
							roleVec.add(roles);
							roles = "";
						}
					}else {
						roleVec.add("");
					}
					personId = null;
				}
			}			
		} catch(Exception e) {
			throw new MatrixException(e);
		}
		return roleVec;
	}

	/*
	 * This method is used to check whether a particular command is having
	 * access to the logged in user.
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * 
	 * @param args holds the following input arguments: objectId - String
	 * containing object id.
	 * 
	 * @return boolean containing access to the command for the logged in user.
	 * 
	 * @throws Exception if the operation fails
	 * 
	 * @since PMC R211
	 */
	public boolean hasAccessToCommand(Context context, String[] args)
	throws MatrixException {
		boolean access = false;
		boolean role = false;
		boolean hasAccess = false;
		boolean childLead = false;
		boolean childExternalLead = false;
		String loginPersonId = PersonUtil.getPersonObjectID(context);
		Vector roleVector = new Vector();
		ProjectSpace projectSpace = (ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
		childLead = PersonUtil.hasAssignment(context, ROLE_PROJECT_LEAD);
		childExternalLead = PersonUtil.hasAssignment(context, ROLE_EXTERNAL_PROJECT_LEAD);
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId"); 	        
			if(ProgramCentralUtil.isNotNullString(loginPersonId)) {
				roleVector  = PersonUtil.getUserRoles(context);    	
			}	       
			for(int k=0;k<roleVector.size()-1;k++) {
				String strRole = (String)roleVector.get(k);
				if(ProgramCentralUtil.isNotNullString(strRole)) {					
					if(ROLE_PROJECT_LEAD.equals(strRole)||ROLE_EXTERNAL_PROJECT_LEAD.equals(strRole)||childLead || childExternalLead) {
						role = true;
					}						
				}
			}
			if(ProgramCentralUtil.isNotNullString(objectId)) {
				projectSpace.setId(objectId);
				String strAccess = projectSpace.getAccess(context);
				if(ProgramCentralUtil.isNotNullString(strAccess)) {			        
					if(ACCESS_PROJECT_LEAD.equals(strAccess) || ACCESS_PROJECT_OWNER.equals(strAccess)||childLead || childExternalLead) {
						access = true;
					}			        
				}
			}	    	
			if(role && access) {
				hasAccess = true;
			}		    	
		}
		catch(Exception e) {
			throw new MatrixException(e);
		}
		return hasAccess;
	}		

	/**
	 * Get if column cells are editable or not.
	 * 
	 * @param context The matrix context object
	 * @param args The arguments, it contains requestMap 
	 * @return The vector with details shall return
	 * @throws Exception if operation fails
	 * @since FCA 2012 IR-100988V6R2012
	 * @author NZF
	 */
	public StringList isCellsEditable  (Context context, String[] args) throws Exception
	{
		try 
		{
			StringList strList = new StringList();
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");  

			for(int iterator = 0; iterator < objectList.size(); iterator++)
			{
				Map map = (Map)objectList.get(iterator);
				String strId = (String) map.get("id");

				if(strId!=null)
				{
					DomainObject domObj = DomainObject.newInstance(context,strId);
					StringList slForecastIds = new StringList();
					if(domObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE))
					{
						strList.add(false);
					}
					else 
					{
						strList.add(true);
					}
				}
			}
			return strList;
		}
		catch(Exception e)
		{
			throw e; 
		}
	}
	private Map calculatePhaseFTEReverse(Context context, FTE fte, MapList mlPhaseList)
	throws MatrixException 
	{
		final String ATTRIBUTE_PHASE_START_DATE = "attribute["+ATTRIBUTE_TASK_ESTIMATED_START_DATE+"]";
		final String ATTRIBUTE_PHASE_FINISH_DATE = "attribute["+ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE+"]";
		String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
		Map mapFTE = fte.getAllFTE();
		Map mapTimeFrameCount = new HashMap();
		Map mapPhaseTimeframe = new HashMap();
		Map mapPhaseFTEValue = new HashMap();
		StringList slTimeFrameList = null;
		for (Iterator iter = mlPhaseList.iterator(); iter.hasNext();) 
		{
			Map mapPhaseInfo = (Map)iter.next();            
			String strPhaseId = (String)mapPhaseInfo.get(SELECT_ID);
			Date fromDate=eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_START_DATE));
			Date toDate=eMatrixDateFormat.getJavaDate((String)mapPhaseInfo.get(ATTRIBUTE_PHASE_FINISH_DATE));
			MapList TimeframesList = fte.getTimeframes(fromDate,toDate);
			slTimeFrameList = new StringList();
			Double value= 0d;
			for(Iterator innerIter = TimeframesList.iterator(); innerIter.hasNext();)
			{
				Map strInnerTimeFrame = (Map)innerIter.next();
				String strMonth = DomainConstants.EMPTY_STRING+strInnerTimeFrame.get("timeframe");
				String strYear = DomainConstants.EMPTY_STRING+strInnerTimeFrame.get("year");
				String strColumnname=strMonth+"-"+strYear;
				if(null!=mapTimeFrameCount.get(strColumnname))
				{
					value=(Double)mapTimeFrameCount.get(strColumnname);
					value = value+1d;
				}
				else
				{
					value = 1d;
				}
				mapTimeFrameCount.put(strColumnname, value);
				slTimeFrameList.add(strColumnname);
			}
			mapPhaseTimeframe.put(strPhaseId, slTimeFrameList);
		}
		NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, false);
		for (Iterator iter = mapPhaseTimeframe.keySet().iterator(); iter.hasNext();) 
		{
			String strPhaseId = (String)iter.next();            
			slTimeFrameList = (StringList)mapPhaseTimeframe.get(strPhaseId);
			double nTotalPhaseFTE = 0;
			for(int i=0; i<slTimeFrameList.size();i++)
			{
				String strTimeFrame = (String)slTimeFrameList.get(i);
				Double nTimeFrameCount =(Double)mapTimeFrameCount.get(strTimeFrame);            	
				if(null!=mapFTE && mapFTE.containsKey(strTimeFrame))
				{
					double valFTE= Task.parseToDouble(mapFTE.get(strTimeFrame).toString());
					double val = 0;
					if(nTimeFrameCount!=0)
					{
						val  = valFTE/nTimeFrameCount;
					} 
					nTotalPhaseFTE = nTotalPhaseFTE+val;
				}                    
			}
			mapPhaseFTEValue.put("PhaseOID"+"-"+strPhaseId, numberFormat.format(nTotalPhaseFTE));
			if(nTotalPhaseFTE>0 && slTimeFrameList.size()>0 && "FTE".equalsIgnoreCase(numberofPeopleUnit))
			{
				mapPhaseFTEValue.put("PhaseOID"+"-"+strPhaseId, numberFormat.format(nTotalPhaseFTE/slTimeFrameList.size()));
			}
		}
		return mapPhaseFTEValue;
	}

	/**
	 * This method is used to get business skill for person (users).
	 * 
	 * @param	context			the eMatrix <code>Context</code> object
	 * @param 	args			holds the following input arguments: objectList - Contains a person id.
	 * @return 	Vector 			containing the Business Skills for a person.
	 * @throws 	MatrixException	if the operation fails
	 * @since  	release version V6R2011x
	 */ 
	public Vector getPersonBusinessSkill(Context context, String[] args) throws MatrixException	{
		try {
			return getColumnSkillCompetencyData(context, args);
		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}

	public String getMonthName(Context context,int nCurrentMonthValue)  throws Exception 
	{
		return ProgramCentralUtil.getMonthName(context, nCurrentMonthValue);
	}

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createResourcePlanByPhase(Context context, String[] args)  throws Exception 
	{
		ResourceRequest request = new ResourceRequest();
		request.createResourcePlanByPhase(context, args);
	}	

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createResourcePlan(Context context, String[] args)  throws Exception 
	{
		ResourceRequest request = new ResourceRequest();
		request.createResourcePlan(context, args);
	}	

	/**
	 * Program to get the cell level access for object's Resource pool and Project Role field
	 * Meeting etc.
	 * 
	 * @param context
	 *            the eMatrix Context object
	 * @@param String array contains Meetings Ids for edit
	 * @throws Matrix
	 *             Exception if the operation fails
	 * @grade 0
	 */

	public StringList getResourcePlanColumnEditAccess(Context context,
			String args[]) throws Exception {
		Map inputMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)inputMap.get("requestMap");
		MapList objectMap = (MapList) inputMap.get("objectList");
		int listSize=objectMap.size();
		boolean isAccessible=!showCurrencyFilter(context,JPO.packArgs(requestMap));
		StringList returnStringList = new StringList(listSize);
		DomainObject dmoObject = null;
		StringList slCheckAccess = getFTEViewEditAccess(context, args);
		for(int iterator = 0; iterator < objectMap.size(); iterator++)
		{
			boolean checkAccess = (Boolean)slCheckAccess.get(iterator);
			Map map = (Map)objectMap.get(iterator);
			String strObjectId = (String)map.get(SELECT_ID);
			dmoObject = DomainObject.newInstance(context, strObjectId);
			StringList slSelectable = new StringList();
			slSelectable.add(SELECT_IS_RESOURCE_REQUEST);
			slSelectable.add(SELECT_CURRENT);
			Map mapObjInfo = dmoObject.getInfo(context,slSelectable);
			String sIsResourceRequestType = (String) mapObjInfo.get(SELECT_IS_RESOURCE_REQUEST);
			boolean isResourceRequest = ProgramCentralUtil.isNotNullString(sIsResourceRequestType) && "true".equalsIgnoreCase(sIsResourceRequestType)?true:false;
			if(isResourceRequest)
			{
				String strCurrentState = (String) mapObjInfo.get(SELECT_CURRENT);
				boolean isStateAccess = strCurrentState.equalsIgnoreCase(DomainConstants.STATE_RESOURCE_REQUEST_REJECTED) || strCurrentState.equalsIgnoreCase(DomainConstants.STATE_RESOURCE_REQUEST_CREATE); 
				if(checkAccess && isStateAccess)
				{
					returnStringList.add(true);
				}
				else
				{
					returnStringList.add(false);
				}
			}
			else
			{
				returnStringList.add(false);
			}
		}
		return returnStringList;
	}

	//Added:07-Jan-2011:hp5:R211 PRG:IR-026179V6R2012
	public void updateFTEforRequestStartEndDate(Context context, String[] args) throws MatrixException 
	{
		try
		{
			//This map gives the  updated FTE values
			Map programMap                      = (Map) JPO.unpackArgs(args);
			Map mpParamMap                      = (Map)programMap.get("paramMap");
			Map mpColumnMap                     = (Map)programMap.get("columnMap");
			Map mpRequestMap                    = (Map)programMap.get("requestMap");

			String strStartDateNewValue = (String)mpParamMap.get("Request Start Date");
			String strEndDateNewValue = (String)mpParamMap.get("Request End Date");

			String strRequestStartDate = (String)mpParamMap.get("Request Start DatefieldValue");
			String strRequestEndDate = (String)mpParamMap.get("Request End DatefieldValue");

			String strRequestObjectId  = (String)mpParamMap.get("objectId");
			String strResourcePlanRelId  = (String)mpParamMap.get("relId");

			String strTimeZone = (String)mpRequestMap.get("timeZone");
			double clientTimeZone = Task.parseToDouble(strTimeZone);

			strStartDateNewValue = eMatrixDateFormat.getFormattedInputDate(context, strStartDateNewValue, clientTimeZone, (Locale)mpRequestMap.get("localeObj"));
			strEndDateNewValue = eMatrixDateFormat.getFormattedInputDate(context, strEndDateNewValue, clientTimeZone, (Locale)mpRequestMap.get("localeObj"));

			Calendar calRequestNewStartDate = getFormattedDate(context, strStartDateNewValue);
			Calendar calRequestNewFinishDate = getFormattedFinishDate(context, strEndDateNewValue,12,0,0);

			Date dtRequestNewStartDate = calRequestNewStartDate.getTime();
			Date dtRequestNewFinishDate = calRequestNewFinishDate.getTime();

			Calendar calRequestStartDate = getFormattedDate(context, strRequestStartDate);
			Calendar calRequestFinishDate = getFormattedFinishDate(context, strRequestEndDate,12,0,0);

			Date dtRequestStartDate = calRequestStartDate.getTime();
			Date dtRequestFinishDate = calRequestFinishDate.getTime();

			boolean updateEndDate = false;
			boolean updateStartDate = false;

			if(!dtRequestNewStartDate.equals(dtRequestStartDate))
			{
				updateStartDate = true;
			}

			if(!dtRequestNewFinishDate.equals(dtRequestFinishDate))
			{
				updateEndDate = true;
			}

			if(updateEndDate || updateStartDate)
			{
				final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to[" + RELATIONSHIP_RESOURCE_PLAN + "].attribute[" + ATTRIBUTE_FTE + "]";
				DomainObject dmoRequest     = newInstance(context,strRequestObjectId);
				StringList slBusSelect = new StringList();
				slBusSelect.add(SELECT_PROJECT_SPACE_ID);
				slBusSelect.add(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
				Map objectinfo = dmoRequest.getInfo(context, slBusSelect);
				String strProjectId = (String)objectinfo.get(SELECT_PROJECT_SPACE_ID);

				FTE fteRequest = FTE.getInstance(context);
				MapList mlNewMonthYearList = null;  
				MapList mlOldMonthYearList = null;

				checkRequestDate(context, strProjectId,
						calRequestNewStartDate,
						calRequestNewFinishDate); 

				if (ProgramCentralUtil.isNotNullString(strStartDateNewValue)&& ProgramCentralUtil.isNotNullString(strEndDateNewValue))
				{
					mlNewMonthYearList = fteRequest.getTimeframes(dtRequestNewStartDate,dtRequestNewFinishDate);
				}
				if (ProgramCentralUtil.isNotNullString(strRequestStartDate)&& ProgramCentralUtil.isNotNullString(strRequestEndDate))
				{
					mlOldMonthYearList = fteRequest.getTimeframes(dtRequestStartDate,dtRequestFinishDate);
				}
				String strOldFTE = "";
				strOldFTE = (String)objectinfo.get(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
				StringList slMonthYear = new StringList();
				int iNewMonthYearSize = mlNewMonthYearList.size();
				int iOldMonthYearSize = mlOldMonthYearList.size();
				Map mapFTE = new HashMap();
				MapList mlFTE = new MapList();
				Map mapNewObject = new HashMap();
				Iterator objectNewListIterator = mlNewMonthYearList.iterator();
				String strFTE = "";

				for (int i =0;i<iNewMonthYearSize ;i++)
				{
					mapNewObject = (Map) objectNewListIterator.next();
					int nNewTimeFrame = (Integer)mapNewObject.get("timeframe");
					int nNewTimeYear = (Integer)mapNewObject.get("year");
					mapFTE = checkForExistingFTEValue(context,mlOldMonthYearList,nNewTimeFrame,nNewTimeYear,strOldFTE);
					mlFTE.add(mapFTE);
				}

				String strFTEValue = "";
				int nRequestNewStartMonth = 0;
				int nRequestNewStartYear = 0;
				int nRequestFinishMonth = 0;
				int nRequestFinishYear = 0;
				FTE fte = FTE.getInstance(context);
				double nFTEValue = 0;

				nRequestNewStartMonth = calRequestNewStartDate.get(Calendar.MONTH) + 1; //0=January
				nRequestNewStartYear = calRequestNewStartDate.get(Calendar.YEAR);
				nRequestFinishMonth = calRequestFinishDate.get(Calendar.MONTH) + 1;//0=January
				nRequestFinishYear = calRequestFinishDate.get(Calendar.YEAR);

				if(mlFTE!=null)
				{
					String strMonthYear = "";
					double nFTE = 0;
					String[] strMonthYearSpilt = null;
					int nMonth = 0;
					int nYear  = 0;
					Iterator objectListIterator = mlFTE.iterator();
					while (objectListIterator.hasNext())
					{
						mapFTE = (Map) objectListIterator.next();
						for (Iterator iter = mapFTE.keySet().iterator(); iter.hasNext();) 
						{
							strMonthYear = (String) iter.next();
							nFTE = Task.parseToDouble((String)mapFTE.get(strMonthYear));
							strMonthYearSpilt = strMonthYear.split("-");
							nRequestNewStartMonth = Integer.parseInt(strMonthYearSpilt[0]);
							nRequestNewStartYear = Integer.parseInt(strMonthYearSpilt[1]);
							fte.setFTE(nRequestNewStartYear, nRequestNewStartMonth, nFTE);
						}
					}
				}
				strFTEValue = fte.getXML();
				DomainRelationship dmoResourcePlanRel = new DomainRelationship(strResourcePlanRelId);
				dmoResourcePlanRel.setAttributeValue(context,ATTRIBUTE_FTE,strFTEValue);
				if(updateStartDate)
				{
					dmoRequest.setAttributeValue(context,ATTRIBUTE_START_DATE,strStartDateNewValue);
				}
				if(updateEndDate)
				{
					dmoRequest.setAttributeValue(context,ATTRIBUTE_END_DATE,strEndDateNewValue);
				}
			}
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}


	private void checkRequestDate(Context context, String strProjectSpaceId,
			Calendar calRequestNewStartDate, Calendar calRequestNewFinishDate)
	throws FrameworkException, Exception, MatrixException 
	{
		final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_START_DATE  + "]";
		final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute[" + ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE  + "]";

		StringList slBusSelect1 = new StringList();
		slBusSelect1.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		slBusSelect1.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

		DomainObject dmoProjectSpace = DomainObject.newInstance(context,strProjectSpaceId);
		Map mapObjInfo = dmoProjectSpace.getInfo(context, slBusSelect1);
		String strProjectStartDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		String strProjectFinishtDate = (String) mapObjInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);

		Calendar calStartDate = getFormattedDate(context, strProjectStartDate);
		Calendar calFinishDate = getFormattedDate(context, strProjectFinishtDate);

		if(((calRequestNewStartDate.before(calStartDate))&&(calRequestNewFinishDate.before(calStartDate)))
				||((calRequestNewStartDate.after(calFinishDate))&&(calRequestNewFinishDate.after(calFinishDate))))            	
		{             		
			String strLanguage = context.getSession().getLanguage();
			String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ResourceRequest.RequestStartDateOrEndDateShouldBeWithinProjectPeriod", strLanguage);
			throw new MatrixException(sErrMsg);            		
		}
	}

	public Calendar getFormattedDate(Context context, String  date) throws MatrixException 
	{
		return getFormattedFinishDate(context, date, 0, 0, 0);

	}

	public Calendar getFormattedFinishDate(Context context, String  date, int hour, int min, int sec) throws MatrixException 
	{
		Date dtDate = eMatrixDateFormat.getJavaDate(date);
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(dtDate);
		calDate.set(Calendar.HOUR_OF_DAY,hour);
		calDate.set(Calendar.MINUTE,min);
		calDate.set(Calendar.SECOND,sec);
		return calDate;

	}
	
}

