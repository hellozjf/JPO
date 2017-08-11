/*
 *  emxResourcePlanBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: $
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.program.FTE; 

public class emxResourcePlanBase_mxJPO extends emxDomainObject_mxJPO
{

	protected static final String SELECT_RESOURCE_POOL_ID = "from["+RELATIONSHIP_RESOURCE_POOL+"].to.id";
	protected static final String SELECT_RESOURCE_ID = "from["+RELATIONSHIP_ALLOCATED+"].to.id";
	protected static final String SELECT_SKILLS_ID = "from["+RELATIONSHIP_RESOURCE_REQUEST_SKILL+"].to.id";
	protected static final String SELECT_PROJECT_ROLE = "attribute_ProjectRole";
	protected static final String SELECT_RESOURCE_FTE = "from["+RELATIONSHIP_ALLOCATED+"].attribute["+ATTRIBUTE_FTE+"]";
	protected static final String STRING_COMMENT = "Comment";


	/**
	 * Constructs a new emxResourcePlan JPO object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments:
	 * @throws Exception if the operation fails
	 */
	public emxResourcePlanBase_mxJPO (Context context, String[] args) throws Exception
	{
		// Call the super constructor
		super(context,args);
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return int 0 for success and non-zero for failure
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public int mxMain(Context context, String[] args)
	throws Exception
	{
		if (!context.isConnected())
			throw new Exception("not supported on desktop client");
		return 0;
	}

	/**
	 * This method creates the new request.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */

	public String createResourcePlan(Context context, String[] args)  throws Exception 
	{
		try 
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String strProjectSpaceId = (String)programMap.get("projectObjectId");
			String strProjectRole = (String)programMap.get("ProjectRole");
			String strComment = (String)programMap.get(STRING_COMMENT);
			String strResourcePoolName = (String)programMap.get("ResourcePoolName");
			String strPreferredPersonID = (String)programMap.get("PreferredPersonDisplay");

			MapList mlFTE = (MapList)programMap.get("FTEValue");

			MapList mlSkillCompetency = (MapList)programMap.get("Skill");

			String strResourceRequestId = FrameworkUtil.autoName(context,
					"type_ResourceRequest",
					"policy_ResourceRequest"
			);

			DomainObject dmoProjectSpace = DomainObject.newInstance(context,
					strProjectSpaceId);

			String strRelationship = PropertyUtil.getSchemaProperty(context,
					"relationship_ResourcePlan");

			DomainObject dmoResourceRequest = DomainObject.newInstance(context,
					strResourceRequestId);

			//Setting attribute Project role on type Resource request
			dmoResourceRequest.setAttributeValue(context,DomainConstants.ATTRIBUTE_PROJECT_ROLE,strProjectRole);

			DomainRelationship dmrResourcePlan = DomainRelationship.connect(context,dmoProjectSpace,strRelationship,dmoResourceRequest);
			//Setting attribute FTE on relationship Resource Plan
			FTE fte = FTE.getInstance(context);
			String strFTEValue = "";
			if(mlFTE!=null)
			{
				HashMap mapFTE = (HashMap)mlFTE.get(0);
				String strMonthYear = "";
				double nFTE = 0;
				String[] strMonthYearSpilt = null;
				int nMonth = 0;
				int nYear  = 0;
				for (Iterator iter = mapFTE.keySet().iterator(); iter.hasNext();) 
				{
					strMonthYear = (String) iter.next();
					nFTE = Task.parseToDouble((String)mapFTE.get(strMonthYear));
					strMonthYearSpilt = strMonthYear.split("-");
					nMonth = Integer.parseInt(strMonthYearSpilt[0]);
					nYear = Integer.parseInt(strMonthYearSpilt[1]);
					fte.setFTE(nYear, nMonth, nFTE);
				}
			}

			strFTEValue = fte.getXML();

			dmrResourcePlan.setAttributeValue(context,ATTRIBUTE_FTE,strFTEValue);

			//Connecting Resource Request Object to Business Skill object with ResourceRequestskill relationship
			if(mlSkillCompetency!=null)
			{
				HashMap mapSkills = (HashMap)mlSkillCompetency.get(0);
				String strSkill = "";
				String strCompetency = "";
				for (Iterator iter = mapSkills.keySet().iterator(); iter.hasNext();) 
				{
					strSkill = (String) iter.next();
					strCompetency = (String)mapSkills.get(strSkill);
					DomainObject dmoSkill = DomainObject.newInstance(context,strSkill);
					String strResourceRequestSkillRel = PropertyUtil.getSchemaProperty(context,
							"relationship_ResourceRequestSkill");
					DomainRelationship dmrResourceRequestSkill= DomainRelationship.connect(context,dmoResourceRequest,strResourceRequestSkillRel,dmoSkill);
					dmrResourceRequestSkill.setAttributeValue(context,ATTRIBUTE_COMPETENCY,strCompetency);
				}
			}

			//Connecting Resource Request Object to Resource Pool(Organization) object with ResourcePool relationship
			if(strResourcePoolName!= null && !"".equals(strResourcePoolName) && !"null".equals(strResourcePoolName) )
			{
				DomainObject dmoResourcePool = DomainObject.newInstance(context,
						strResourcePoolName);
				String strResourcePoolRel = PropertyUtil.getSchemaProperty(context,
						"relationship_ResourcePool");
				DomainRelationship dmrResourcePool= DomainRelationship.connect(context,dmoResourceRequest,strResourcePoolRel,dmoResourcePool);
			}

			//Connecting Resource Request Object to Person object with Allocated relationship
			if(strPreferredPersonID!= null && !"".equals(strPreferredPersonID) && !"null".equals(strPreferredPersonID) )
			{
				DomainObject dmoResourcePerson = DomainObject.newInstance(context,
						strPreferredPersonID);
				String strAllocated= PropertyUtil.getSchemaProperty(context,
						"relationship_Allocated");
				DomainRelationship dmrResourcePerson= DomainRelationship.connect(context,dmoResourceRequest,strAllocated,dmoResourcePerson);

			}
			String strSuccess = "success" ;
			return strSuccess ; 

		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}


	/**
	 * This method clones the requests.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */

	public String cloneResourcePlan(Context context, String[] args)  throws Exception {
		try {

			Map programMap 			= (Map) JPO.unpackArgs(args);

			String strprojectFromId	= (String)programMap.get("projectFromId");
			String strprojectToId 	= (String)programMap.get("projectToId");
			String strResource 		= (String)programMap.get("Resource");
			String strBSkill 		= (String)programMap.get("BSkill");
			String strPRole 		= (String)programMap.get("PRole");
			String standardCost 	= (String)programMap.get("standardCost");

			final String ATTRIBUTE_STANDARD_COST = PropertyUtil.getSchemaProperty(context, "attribute_StandardCost");

			//Creating domain object of project to connect request to 
			//DomainObject domprojectFromId = DomainObject.newInstance(context, strprojectFromId);
			DomainObject dom = DomainObject.newInstance(context, strprojectToId);

			String strRelationship = DomainConstants.RELATIONSHIP_RESOURCE_PLAN;
			String strType = TYPE_RESOURCE_REQUEST;

			final String SELECT_REL_ATTRIBUTE_FTE = DomainRelationship.getAttributeSelect(ATTRIBUTE_FTE);

			StringList busSelect = new StringList();
			busSelect.add(DomainConstants.SELECT_ID);
			busSelect.add(SELECT_RESOURCE_POOL_ID);
			busSelect.add(SELECT_RESOURCE_ID);
			busSelect.add(SELECT_RESOURCE_FTE);
			busSelect.add(SELECT_SKILLS_ID);
			busSelect.add(DomainRelationship.getAttributeSelect(ATTRIBUTE_PROJECT_ROLE));
			busSelect.add(DomainRelationship.getAttributeSelect(ATTRIBUTE_STANDARD_COST));
			busSelect.add("attribute["+ ATTRIBUTE_STANDARD_COST + "].inputunit");
			StringList relSelect = new StringList();
			relSelect.add(SELECT_REL_ATTRIBUTE_FTE);

			//Getting all the request of the project to be clonned
			MapList mlRequests = dom.getRelatedObjects(
					context,
					strRelationship,
					strType,
					busSelect,
					relSelect,
					false,
					true,
					(short)1,
					DomainConstants.EMPTY_STRING,
					DomainConstants.EMPTY_STRING);           

			Map mapReq = null;
			String strReqID = null;
			String strRequestName = null;
			emxResourceRequest_mxJPO emxResourceRequest_mxJPO = null;
			String strResourcePoolId = "";
			StringList slResourceId = null;
			StringList slResourceFTE = null;
			StringList slSkillsId = null;
			String strSkillsId = null;
			String strRequestFTE = "";
			String[] arrJPOArguments = new String[1];
			Map mapParam= new HashMap();
			for(int i =0 ; i<mlRequests.size(); i++) {

				mapReq = (Map)mlRequests.get(i);            	
				strReqID = (String) mapReq.get("id");
				strResourcePoolId = (String)mapReq.get(SELECT_RESOURCE_POOL_ID);

				strRequestFTE = (String)mapReq.get(SELECT_REL_ATTRIBUTE_FTE);

				Object objSkillype = null;
				objSkillype = mapReq.get(SELECT_SKILLS_ID);

				if("null".equals(strResource) || strResource == null){
					slResourceId = EMPTY_STRINGLIST;
					slResourceFTE = EMPTY_STRINGLIST;
				}else{
					Object objResourceId = (Object)mapReq.get(SELECT_RESOURCE_ID);  
					Object objResourceFTE = (Object)mapReq.get(SELECT_RESOURCE_FTE);  
					if(objResourceId instanceof String)
					{
						slResourceId = new StringList((String)objResourceId);
						slResourceFTE = new StringList((String)objResourceFTE);
					}
					else if(objResourceId instanceof StringList)
					{
						slResourceId = (StringList)mapReq.get(SELECT_RESOURCE_ID);  
						slResourceFTE = (StringList)mapReq.get(SELECT_RESOURCE_FTE);  
					}
					else
					{
						slResourceId = EMPTY_STRINGLIST;
						slResourceFTE = EMPTY_STRINGLIST;
					}
				}
				if (objSkillype instanceof String) {
					if("null".equals(strBSkill) || strBSkill == null){
						strSkillsId = EMPTY_STRING;
					}else{
						strSkillsId = (String)mapReq.get(SELECT_SKILLS_ID); 
					}
					mapParam.put("Skill", strSkillsId);
				}else{
					if("null".equals(strBSkill) || strBSkill == null){
						slSkillsId = EMPTY_STRINGLIST;
					}else{
						slSkillsId = (StringList)mapReq.get(SELECT_SKILLS_ID); 
					}
					mapParam.put("Skill", slSkillsId);
				}
				String strProjRole = "";
				if("null".equals(strPRole) || strPRole == null){
					strProjRole = EMPTY_STRING;
				}else{
					//Modified:18-Aug-10:S4E:R210:IR-068611V6R2011x				  
					strProjRole = (String)mapReq.get(DomainRelationship.getAttributeSelect(ATTRIBUTE_PROJECT_ROLE));
					//End:Modified:18-Aug-10:S4E:R210:IR-068611V6R2011x
				}
				String stdCost = "";
				String stdCostCurrencyUnit = "";
				if("null".equals(standardCost) || standardCost == null){
					stdCost = EMPTY_STRING;
					stdCostCurrencyUnit = EMPTY_STRING;
				}else{
					stdCost = (String)mapReq.get(DomainRelationship.getAttributeSelect(ATTRIBUTE_STANDARD_COST));
					stdCostCurrencyUnit = (String)mapReq.get("attribute["+ ATTRIBUTE_STANDARD_COST + "].inputunit");					
				}                  

				mapParam.put("reqId", strReqID);
				mapParam.put("RequestFTE", strRequestFTE);
				mapParam.put("relationshipName", strRelationship);
				mapParam.put("projectFromId", strprojectFromId);
				mapParam.put("ResourcePool", strResourcePoolId);
				mapParam.put("Resource", slResourceId);
				mapParam.put("ResourceFTE", slResourceFTE);
				mapParam.put("PRole", strProjRole);
				mapParam.put("stdCost", stdCost+" "+stdCostCurrencyUnit);
				arrJPOArguments = JPO.packArgs(mapParam);
				emxResourceRequest_mxJPO = new emxResourceRequest_mxJPO(context,arrJPOArguments);
				emxResourceRequest_mxJPO.cloneRequest(context, arrJPOArguments);
			}
			String strSuccess = "success" ;
			return strSuccess ; 

		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}    

}

