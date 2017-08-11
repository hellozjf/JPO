/*
 ** $RCSfile: ${CLASS:enoConfiguredECOMigrationBase}.java $
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.*;

import matrix.db.*;
import matrix.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

import java.io.FileWriter;
import com.matrixone.apps.effectivity.EffectivityFramework;
import com.dassault_systemes.enovia.changeaction.factory.ChangeActionFactory;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeActionServices;
import com.matrixone.apps.productline.ProductLineCommon;


public class enoConfiguredECOMigrationBase_mxJPO extends emxCommonMigration_mxJPO
{

	String attr_originator;
	String attr_catofChange; 
	String attr_priority; 
	String attr_severity;
	String attr_reasonForCancel; 
	String attr_title; 
	String attr_actStartDate; 
	String attr_actCompDate; 
	String attr_requestedChange;
	String attr_routeBasePurpose;
	String attr_BuildUnitNumber;
	String attr_PrerequisiteType;
	
	String state_DefineComponents;
	String state_Release;

	String str_originator;
	String str_catofChange;
	String str_priority;
	String str_severity;
	String str_reasonForCancel;
	String str_Title;

	String obj_NamedEffectivity;
	String obj_DesignResp;
	String obj_RespDE;
	String obj_RespME;
	
	String rel_NamedEffectivity;
	String rel_NE;
        String rel_preReq;
	String rel_PreReqFrom;
	String rel_PreReqTo;
	String rel_affectedItem;
	String rel_relatedItem;
	String rel_ChangeAffectedItem;
	String rel_ChangeAction;
	String rel_technicalAssignee;
	String rel_modelbuild;
	String rel_effectivityUsage;

	
	String sel_ChangeAction;
	String sel_NE;
	String str_actStartDate;
	String str_actCompDate;
	String str_ActualStartDate;
	String str_ActualCompDate;
	
	String type_ChangeAction;
	String type_HardwareBuild;
	StringList objectSelects;
	String str_attrRequChange;
	
	String strReqChangeDefaultValue = "For Update";
	String selChangeOrderId = "";
	String rel_reportedAgainstChange;
	String rel_ObjectRoute;
	String rel_ModelBuild;
	
	String policy_Build;
	
	String str_attrRouteBasePurpose;
	String strProductionVault; 
	String org_Default;
	String proj_Default;
	
	
	public static FileWriter CECOWithFOapplicabilityLog;
	
	/**
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @grade 0
    */
	
    public enoConfiguredECOMigrationBase_mxJPO (Context context, String[] args)
        throws Exception
    {    	
    	super(context, args);
	 
    	strProductionVault = PropertyUtil.getSchemaProperty(context,"vault_eServiceProduction");
    	
	   	attr_originator = PropertyUtil.getSchemaProperty(context,"attribute_Originator");
	   	attr_catofChange = PropertyUtil.getSchemaProperty(context,"attribute_CategoryofChange");
    	attr_priority = PropertyUtil.getSchemaProperty(context,"attribute_Priority");
    	attr_severity = PropertyUtil.getSchemaProperty(context,"attribute_Severity");
    	attr_reasonForCancel = PropertyUtil.getSchemaProperty(context,"attribute_ReasonForCancel");
    	attr_title = PropertyUtil.getSchemaProperty(context,"attribute_Synopsis");
    	attr_actStartDate = PropertyUtil.getSchemaProperty(context,"attribute_ActualStartDate");
    	attr_actCompDate = PropertyUtil.getSchemaProperty(context,"attribute_ActualCompletionDate");
    	attr_requestedChange = PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange");
    	attr_routeBasePurpose = PropertyUtil.getSchemaProperty(context,"attribute_RouteBasePurpose");
    	attr_BuildUnitNumber = PropertyUtil.getSchemaProperty(context,"attribute_BuildUnitNumber");
    	attr_PrerequisiteType = PropertyUtil.getSchemaProperty(context,"attribute_PrerequisiteType");
	 	// State Names
	 	state_DefineComponents = PropertyUtil.getSchemaProperty(context,"state_DefineComponents");
		state_Release = PropertyUtil.getSchemaProperty(context,"state_Release");

		// get the attributes from the Change object
		str_originator 	= "attribute["+ attr_originator +"]";
		str_catofChange = "attribute["+ attr_catofChange +"]";
		str_priority = "attribute["+attr_priority +"]";
		str_severity = "attribute["+ attr_severity +"]";
		str_reasonForCancel = "attribute["+ attr_reasonForCancel +"]";
		str_Title = "attribute["+ attr_title +"]";
		str_attrRequChange = "attribute["+ attr_requestedChange +"]";
		str_attrRouteBasePurpose = "attribute["+ attr_routeBasePurpose +"]";
		
		// This one is from CA
		str_actStartDate = "attribute["+ attr_actStartDate +"]";
		str_actCompDate = "attribute["+ attr_actCompDate +"]";
		
		str_ActualStartDate = "state[Define Components].start";
		str_ActualCompDate =  "state[Release].start";		
		
		// Named Effectivity Rel
		obj_NamedEffectivity = "from["+PropertyUtil.getSchemaProperty(context,"relationship_NamedEffectivity")+"].to.id";
		
		// Design Responsibility
		obj_DesignResp = "to["+PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility")+"].from.Name";
		
		// Responsibile Design and Mfg Engineer
		obj_RespDE = "from["+PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleDesignEngineer")+"].to.id";
		obj_RespME = "from["+PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleManufacturingEngineer")+"].to.id";
		
		// Relationship Named Effectivity
		rel_NE = PropertyUtil.getSchemaProperty(context,"relationship_NamedEffectivity");
		sel_NE = "from["+rel_NE+"]";
		rel_ChangeAffectedItem = PropertyUtil.getSchemaProperty(context,"relationship_ChangeAffectedItem");
		
		rel_NamedEffectivity = "from["+rel_NE+"].id";
		
		rel_ChangeAction = PropertyUtil.getSchemaProperty(context,"relationship_ChangeAction");
		rel_modelbuild = PropertyUtil.getSchemaProperty(context,"relationship_ModelBuild");
		rel_effectivityUsage = PropertyUtil.getSchemaProperty(context,"relationship_EffectivityUsage");
			
		policy_Build = PropertyUtil.getSchemaProperty(context,"policy_Build");
		
		// Relationship Chagne Action
		sel_ChangeAction = "to["+rel_ChangeAction+"].id";
		
		selChangeOrderId = "to["+rel_ChangeAction+"].from.id";

		
		// Relationship Prerequisite
		rel_preReq = PropertyUtil.getSchemaProperty(context,"relationship_Prerequisite");
		rel_PreReqFrom = "from["+rel_preReq+"].id";
		rel_PreReqTo = "to["+rel_preReq+"].id";
		
		rel_relatedItem = PropertyUtil.getSchemaProperty(context,"relationship_RelatedItem");
		
		
		rel_affectedItem = PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem");
		
		type_ChangeAction = PropertyUtil.getSchemaProperty(context,"type_ChangeAction");
		type_HardwareBuild = PropertyUtil.getSchemaProperty(context,"type_HardwareBuild");
		
		
		rel_technicalAssignee = PropertyUtil.getSchemaProperty(context,"relationship_TechnicalAssignee");
		rel_reportedAgainstChange = PropertyUtil.getSchemaProperty(context,"relationship_ReportedAgainstChange");
		rel_ObjectRoute = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");
		rel_ModelBuild = PropertyUtil.getSchemaProperty(context,"relationship_ModelBuild");
		
    	org_Default = PropertyUtil.getSchemaProperty(context, "role_CompanyName");
    	proj_Default = DomainAccess.getDefaultProject(context);	

		
		objectSelects = new StringList();
		
		objectSelects.add(DomainConstants.SELECT_POLICY);
		objectSelects.add(DomainConstants.SELECT_CURRENT);
		objectSelects.add(DomainConstants.SELECT_NAME);
		objectSelects.add(DomainConstants.SELECT_DESCRIPTION);
		objectSelects.add(DomainConstants.SELECT_ORIGINATOR);
		objectSelects.add(DomainConstants.SELECT_OWNER);
		objectSelects.add("Project");
		objectSelects.add(DomainConstants.SELECT_ORGANIZATION);
				
		objectSelects.add(str_originator);
		objectSelects.add(str_catofChange);
		objectSelects.add(str_priority);
		objectSelects.add(str_severity);
		objectSelects.add(str_reasonForCancel);
		
		objectSelects.add(str_ActualStartDate);
		objectSelects.add(str_ActualCompDate);

		objectSelects.add(obj_NamedEffectivity);
		objectSelects.add(obj_DesignResp);
		objectSelects.add(obj_RespDE);
		objectSelects.add(obj_RespME);
		
		objectSelects.add(rel_NamedEffectivity);
		objectSelects.add(sel_ChangeAction);
		objectSelects.add(rel_PreReqFrom);
		objectSelects.add(rel_PreReqTo);
		objectSelects.add(sel_NE);
		objectSelects.add(selChangeOrderId);
		
				
    }
   
   /*  Change the type and policy of the Named Effectivity object to Change Action    */
    private void changeTypePolicy(Context context,String sNEoid,String sName) throws Exception
    {
    	
    	String strTypePolicy = type_ChangeAction;
    	String sCmd = "modify bus $1 type $2 policy $3 name $4 revision $5";
    	
    	try {
    		
    		MqlUtil.mqlCommand(context, sCmd,sNEoid,strTypePolicy,strTypePolicy, sName,"-");	
    		
    	} catch (Exception e) {
    		writer.write("Exception in changeTypePolicy =" +e.getMessage());
    		e.printStackTrace();
    		throw e;
    	}	
    }
    
	/*  Updates the assignee by selecting from SDE and SME of the PUE ECO    */

    private void updateAssigne (Context context , String changeid, String sDE, String sME) throws Exception 
    {
    	
    	String sCmd = "add connection $1 from $2 to $3";
    	
    	try {
    		
    		if (sDE!= null && sDE.length()!= 0) {
    			MqlUtil.mqlCommand(context, sCmd,rel_technicalAssignee,changeid,sDE);	
    		}
    		
    		if (sME!=sDE && sME!= null && sME.length()!= 0) {
    			MqlUtil.mqlCommand(context, sCmd,rel_technicalAssignee,changeid,sME);
    		}
    		
    	} catch (Exception e) {
    		writer.write("Exception in updateAssigne =" +e.getMessage());
    		e.printStackTrace();
    		throw e;
    	}	
    }
    
	/* Copy mapped attributes */
    private void copyAttributes(Context context,HashMap attributemap, DomainObject neDO) throws Exception
    {
    	try {
    	neDO.setAttributeValues(context, attributemap);
    	} catch (Exception e) {
    		writer.write("Exception in copyAttributes =" +e.getMessage());
    		e.printStackTrace();
    		throw e;
    	}
    }
    
	/* Gets the applicability from the PUE ECO */
	
    private String getApplicabilityOnCECO(Context context,String CECOId) throws Exception
    {
    	
    	String[] changeIds = new String[1];
    	String xmlExp = "";
		
    	try {
    		
	    	changeIds[0] = CECOId;
			
	    	MapList xmlExpression = EffectivityFramework.getXMLExpressionForChange(context, changeIds);			
			HashMap xmlExpr = (HashMap) xmlExpression.get(0);
		
			String xExpr = (String)xmlExpr.get("xmlExpression");
			xmlExp =xExpr.replace("Cfg:","");
			xmlExp =xmlExp.replace("xmlns:Cfg","xmlns");
    	} catch (Exception e) {
    		writer.write("Exception in setApplicabilityOnCA() :" +e.getMessage());
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return xmlExp;
    }
 
    /* Create Hardware Build and connects to the model */
    
    private void createBuildToModel(Context context,String sModelId) throws Exception
    {
    	
    	ProductLineCommon createBuild = new ProductLineCommon();
    	String strRevision  = "";
    	String strBuildDescription = "Build created due to migration";
    	
    	try{
    		
    			HashMap<String, String> attributeMap = new HashMap<String, String>();
    			attributeMap.put(attr_BuildUnitNumber,"1");
    			
    			/* Create a new Hardware Build */
    			
    			String  strbuildId = (String) createBuild.create(context, type_HardwareBuild,
				"", strRevision, strBuildDescription, policy_Build,
				strProductionVault, attributeMap, "", "", "", false);

    			if(strbuildId != null && !"".equals(strbuildId) && strbuildId.length() != 0)
    			{
    				
    				String setProjOrg = "modify bus $1 project $2 organization $3";
    				MqlUtil.mqlCommand(context, setProjOrg, strbuildId,proj_Default,org_Default);
    				
    				String sCmd = "add connection $1 from $2 to $3";
	    			MqlUtil.mqlCommand(context, sCmd,rel_modelbuild,sModelId,strbuildId);	
    			}

    	} catch (Exception e) {
    					writer.write("Exception in createBuildToModel() :" +e.getMessage());
    					e.printStackTrace();
    					throw e;
    				}
    }
    
    /* Check if build exists for a model connected to Named Effectivity */
    
    private void checkandCreateBuilds(Context context, String NEId) throws Exception
    {
    	
    	try{
    		
    		DomainObject doboNE = DomainObject.newInstance(context, NEId);
			StringList busSelects = new StringList(1);
			String sSel = "from["+rel_modelbuild+"]";
			busSelects.add(DomainObject.SELECT_ID);
			busSelects.add(sSel);
			
			StringList relSelects = new StringList(1);
			relSelects.add(DomainRelationship.SELECT_ID);
			
			short sh = 0;
			
			MapList mList = doboNE.getRelatedObjects(context,
														rel_effectivityUsage,     			// relationship pattern
											            "*",                         		// object pattern
											            busSelects,                 		// object selects
											            relSelects,              			// relationship selects
											            true,                       		// to direction
											            true,                        		// from direction
											            (short) 1,                   		// recursion level
											            "",                        			// object where clause
											            "",									// rel where clause
											            sh,
											            false,
											            true,
											            sh,
											            null,
											            null,
											            null,
											            ""
														);				
									
			
			for (int j = 0; j < mList.size(); j++)
	        {
	           Map relMap = (Map)mList.get(j);
	           
	          String strFlag = (String) relMap.get(sSel);
	        
	           if (strFlag.equalsIgnoreCase("False")) {
	        	   String sModelId = (String) relMap.get(DomainObject.SELECT_ID);
	        	   createBuildToModel(context,sModelId);
	           }
	           
	        }
		
    } catch (Exception e) {
		writer.write("Exception in setApplicabilityOnCA() :" +e.getMessage());
		e.printStackTrace();
		throw e;
	}
		
    	
    }
    
    
	/* Sets the applicability on to the CA Object */
	
    private void setApplicabilityOnCA(Context context, String CAId, String xmlExpression, String owner) throws Exception
    {
    			
    	try {
				
    		/* Check for the builds and also create one if none exists */
    		
    		checkandCreateBuilds(context, CAId);
    		
				/* Note : IgnoreOwnershipForCAApplicability  evnrionment variable needs to be set.
				else the below set applicability get blocked because only owner or change coordinator can set the applicability.
				and with the env variable, this check is ignored.
				*/ 
    		
			IChangeActionServices caService = ChangeActionFactory.CreateChangeActionFactory();
			IChangeAction iCa = caService.retrieveChangeActionFromDatabase(context, CAId);
	
			iCa.SetApplicabilityExpression(context, xmlExpression );
	  		
    	} catch (Exception e) {
    		writer.write("Exception in setApplicabilityOnCA() :" +e.getMessage());
    		e.printStackTrace();
    		throw e;
    	}	
    }
    /* Create related item rel between CECO and CA*/
    
    private void createRelateItem(Context context,String strCAId,String sCECOId) throws Exception
    {
    	String sCmd = "add connection $1 from $2 to $3";
    	
    	try {
    		
    			MqlUtil.mqlCommand(context, sCmd,rel_relatedItem,strCAId,sCECOId);	
    		
    		} catch (Exception e) {
    			writer.write("Exception in creating related data, createRelateItem()\n");
    			e.printStackTrace();
    			throw e;
    		}
    }
  

    /* Modify the Change Action relationship to point it to the CA insted of CECO */
    
    private void modifyChangeActionRel(Context context,String CArelid, String strCAId) throws Exception
    {
    	String sCmd = "modify connection $1 to $2";
    	
    	try {
    			if (CArelid != null && CArelid.length()!= 0 ) {
    				MqlUtil.mqlCommand(context, sCmd,CArelid,strCAId);
    			}
    		
    	} catch (Exception e) {
    		writer.write("Exception in modifying Change Action rel, modifyChangeActionRel() \n");
    		e.printStackTrace();
    		throw e;
    	}
    }

    /* Set the Pre-requisites*/
    
    private void setPreRequisite(Context context,String preReqRelId,String objNE, String direction) throws Exception 
    {
    	
    	String sCmd = "modify connection $1 $2 $3 $4 $5";
    	
    	try {
    			MqlUtil.mqlCommand(context, sCmd,preReqRelId,direction,objNE, attr_PrerequisiteType ,"Mandatory");
    		
    	} catch (Exception e) {
    		writer.write("Exception in setting the Prerequisiste \n");
    		e.printStackTrace();
    		throw e;
    	}
    	
    }
	
	/* Set Reported Against Item on the CO */
	
	public void setReportedAgainstItem(Context context, String relReportedAgainstid, String COId) throws Exception
	{
		try {
			
				
			DomainObject coDO = DomainObject.newInstance(context, COId);
			StringList busSelects = new StringList(1);
			busSelects.add(DomainObject.SELECT_ID);
			
			StringList relSelects = new StringList(1);
			relSelects.add(DomainRelationship.SELECT_ID);
			
			
			MapList mList = coDO.getRelatedObjects(context,
					rel_reportedAgainstChange,     		// relationship pattern
		            "*",                         		// object pattern
		            busSelects,                 		// object selects
		            relSelects,              			// relationship selects
		            false,                       		// to direction
		            true,                        		// from direction
		            (short) 1,                   		// recursion level
		            "",                        			// object where clause
		            "",									// rel where clause
		            0);				
			
					
			if (mList.size() == 0 ) {

				String sCmd = "modify connection $1 from $2";
		    	MqlUtil.mqlCommand(context, sCmd,relReportedAgainstid,COId);
			} 		
			
		} catch (Exception e) {
			e.printStackTrace();
			writer.write("Exception in Reported Against \n");
			throw e;
		}
		
	}
	
    
  /* Delete the Named Effectivity Relationship at the end */
    
    private void deleteNERel(Context context,String neRelId) throws Exception
    {
    	String sCmd = "delete connection $1";
    	
    	try {
    			MqlUtil.mqlCommand(context, sCmd,neRelId);	
    		} catch (Exception e) {
    			writer.write("Exception in deleting Named Effectivity Relationship \n");
    			e.printStackTrace();
    			throw e;
    		}	
    }
    
   /* Get the related data from CECO*/ 

	public MapList getRelatedData(Context context,DomainObject cECOObj,StringList relSelect,StringList busSelect)  throws Exception 
	{
		MapList mapList = new MapList();
		try {
			
			
			String relPattern = rel_preReq+","+ rel_affectedItem +","+ rel_reportedAgainstChange+","+rel_ObjectRoute;
			
			String busWhere = "";
			String relWhere = "";
			
			mapList = cECOObj.getRelatedObjects(context,
					relPattern,     	// relationship pattern
		            "*",                         		// object pattern
		            busSelect,                 			// object selects
		            relSelect,              			// relationship selects
		            true,                       		// to direction
		            true,                        		// from direction
		            (short) 1,                   		// recursion level
		            busWhere,                        		// object where clause
		            relWhere,							// rel where clause
		            0);								// Limit
			
		} catch (Exception e) {
			e.printStackTrace();
			writer.write("Exception in getRelatedData \n");
			throw e;
		}
			return mapList;
	}

	/* Set the affected item proposed item */
	
	private void setAffectedItem(Context context,String strCAId,String sPartId, String attr_reqChange, String affecteItemId) throws Exception
	{
		String setChangeAffectedItem = "add connection $1 from $2 to $3 $4 $5";
		
		String deleteAffectedItem = "disconnect connection $1";
		try {
		
				MqlUtil.mqlCommand(context, setChangeAffectedItem,rel_ChangeAffectedItem,strCAId,sPartId,attr_requestedChange,attr_reqChange);
				MqlUtil.mqlCommand(context, deleteAffectedItem, affecteItemId);
			} catch (Exception e) {
				e.printStackTrace();
				writer.write("Exception in setting as affected item \n");
				throw e;
			}
	}
	
	/* Set Review Route */
	private void setReviewerRoute(Context context,String relid,String objNE, String busid) throws Exception 
	{
		
		String setReviewronCA = "add connection $1 from $2 to $3";
		String deleteReviewerRouteonCECO = "disconnect connection $1";
		
		try {
			MqlUtil.mqlCommand(context, setReviewronCA,rel_ObjectRoute,objNE,busid);
			MqlUtil.mqlCommand(context, deleteReviewerRouteonCECO, relid);
		} catch (Exception e) {
			e.printStackTrace();
			writer.write("Exception in setting setReviewerRoute \n");
			throw e;
		}
		
	}
	
	/* Set Approval Route */
	private void setApprovalRoute(Context context,String relid,String coObjId, String routetempid) throws Exception 
	{
    
		String sCmd = "modify connection $1 from $2";

		try {
			MqlUtil.mqlCommand(context, sCmd,relid,coObjId);
		} catch (Exception e) {
			e.printStackTrace();
			writer.write("Exception in setting setApprovalRoute \n");
			throw e;
		}
	}
	
	/* 
	 * Check to see if there are any other CA's connected to CO which are in state less than Approved State
	 * If yes, do not promote the CO, else promote the CO to Approved state 
	 *
	 * */
	
	private void  checkandPromoteCO(Context context,String objCOId,String sNEId) throws Exception
	{
		
		MapList mapList = new MapList();
		try {
			
			DomainObject coDO = DomainObject.newInstance(context, objCOId);
			StringList busSelects = new StringList(1);
			busSelects.add(DomainObject.SELECT_ID);
			busSelects.add(DomainObject.SELECT_CURRENT);
			busSelects.add(DomainObject.SELECT_TYPE);
			busSelects.add(DomainObject.SELECT_NAME);
			
			StringList relSelects = new StringList(1);
			relSelects.add(DomainRelationship.SELECT_ID);
			
			String busWhere = DomainObject.SELECT_ID + " != '"+sNEId+"'";
			
			MapList mList = coDO.getRelatedObjects(context,
													rel_ChangeAction,     				// relationship pattern
										            "*",                         		// object pattern
										            busSelects,                 		// object selects
										            relSelects,              			// relationship selects
										            false,                       		// to direction
										            true,                        		// from direction
										            (short) 1,                   		// recursion level
										            busWhere,                        	// object where clause
										            "",									// rel where clause
										            0);				
			
			boolean coPromoteFlag = true;
			for (int j = 0; j < mList.size(); j++)
	        {
	           Map relMap = (Map)mList.get(j);
	           
	           String caState = (String) relMap.get(DomainObject.SELECT_CURRENT);
	            
	           if (caState.equals("Prepare") || caState.equals("In Work") || caState.equals("In Approval"))
	           {
	        	   coPromoteFlag = false;
	           }
	        }  
			
			if (coPromoteFlag)
			{
				int jFlag = coDO.setState(context, "In Approval");
			}
				
		} catch (Exception e) {
			e.printStackTrace();
			writer.write("Exception in getRelatedData \n");
			throw e;
		}
		
	}
	

    /* Main program to migrate */ 
    
	public void migrateObjects(Context context, StringList objectList) throws Exception
    {
			DomainObject cecoDO;
			DomainObject neDO = new DomainObject();
			
			try {
			
				context.setCustomData("SetExpressionModeNoSynchronize","TRUE"); 
				System.out.println("SetExpressionModeNoSynchronize is Set ");
				
			for (int i=0;i<objectList.size(); i++) {

				
				String oid = (String)objectList.get(i);
				cecoDO = DomainObject.newInstance(context, oid);
				
				//cecoDO.setId(changeId);
				Map strObjSelect = (Map)cecoDO.getInfo(context, objectSelects);
				
				String sFlag = (String)strObjSelect.get(sel_NE);
				String objCOId = "";
				
				if (sFlag.equals("True"))
				{
						
				// Retriev attributes
				String sCurrentState = (String)strObjSelect.get(DomainConstants.SELECT_CURRENT);
				writer.write("~~~~~ Change ID to Convert =" +oid);

				// Change NE Object to CA type and CA policy
				String objNE = (String)strObjSelect.get(obj_NamedEffectivity);
				String objName = (String)strObjSelect.get(DomainConstants.SELECT_NAME);
				
				String xmlExpression = getApplicabilityOnCECO(context, oid);
				changeTypePolicy( context, objNE,objName);
				
				// ----------- Copy Basics ---------
				String organization = (String)strObjSelect.get(obj_DesignResp);
				String description = (String)strObjSelect.get(DomainConstants.SELECT_DESCRIPTION);
				String owner = (String)strObjSelect.get(DomainConstants.SELECT_OWNER);
				objCOId = (String)strObjSelect.get(selChangeOrderId);
				
				String org = (String)strObjSelect.get(DomainConstants.SELECT_ORGANIZATION);
				String prj = (String)strObjSelect.get("project");
				
		
				/* set Applicability on CA by copying Applicability from CECO */  
				
				setApplicabilityOnCA(context,objNE, xmlExpression, owner);
				
				// Responsible DE and ME
				String sDE = (String)strObjSelect.get(obj_RespDE);
				String sME = (String)strObjSelect.get(obj_RespME);
				
				// Set the Assignee on the NE (CA) Object 
				updateAssigne(context,objNE,sDE,sME);
				
				// Set the severity to Low if its blank
				String sSeverity = (String)strObjSelect.get(str_severity);
				sSeverity = (sSeverity.equals("")||(sSeverity.length() == 0 )) ? "Low" : sSeverity;
				
				// Copy Attributes
			 
				neDO.setId(objNE);
				if(organization!= null && organization.length()> 0 ) {
					neDO.setAltOwner1(context, organization);	
				} else {
					neDO.setAltOwner1(context, org);
				}
				
				if(prj!= null && prj.length()> 0 ) {
					neDO.setAltOwner2(context, prj);
				}
				
				neDO.setDescription(context,description);
				neDO.setOwner(context, owner);
				
				HashMap attributesMap = new HashMap();
				attributesMap.put(attr_originator, (String)strObjSelect.get(str_originator));
				attributesMap.put(attr_catofChange, (String)strObjSelect.get(str_catofChange));
				attributesMap.put(attr_priority, (String)strObjSelect.get(str_priority));
				attributesMap.put(attr_severity, sSeverity);
				attributesMap.put(attr_reasonForCancel, (String)strObjSelect.get(str_reasonForCancel));
				attributesMap.put(attr_title, objName);
				
				/* Set the Actual start and Actual Completion Date*/
				attributesMap.put(attr_actStartDate, (String)strObjSelect.get(str_ActualStartDate));
				attributesMap.put(attr_actCompDate, (String)strObjSelect.get(str_ActualCompDate));

				copyAttributes(context,attributesMap,neDO);
				
				
				/* set related item */
				createRelateItem(context,objNE,oid);
				
				/* Disconnecte Named Effectivity */
				String neRelId = (String)strObjSelect.get(rel_NamedEffectivity);
				deleteNERel(context,neRelId); 
				
				/* Modifying Change Action rel on the To side */
				
				String relChangeActionId = (String)strObjSelect.get(sel_ChangeAction);
				
				if(relChangeActionId != null && relChangeActionId.length() != 0) {
					modifyChangeActionRel(context,relChangeActionId,objNE);	
				}
				
				/* Get the related data */
				
				StringList relSelect = new StringList();
				relSelect.add("type");
				relSelect.add(DomainRelationship.SELECT_ID);
				relSelect.add(DomainRelationship.SELECT_TYPE);
				relSelect.add(str_attrRequChange);
				relSelect.add(str_attrRouteBasePurpose);
				
				StringList busSelect = new StringList();
				busSelect.add(DomainObject.SELECT_ID);
				busSelect.add("from[Prerequisite]"); 
				busSelect.add("to[Prerequisite]");
				
				String busWhere = "";
				String relWhere = "";
				
				MapList m1 = getRelatedData(context,cecoDO, relSelect,busSelect);
				
				for (int j = 0; j < m1.size(); j++)
		        {
		           Map relMap = (Map)m1.get(j);
		           
		           String relType = (String) relMap.get(DomainRelationship.SELECT_TYPE);
		           String relid = (String) relMap.get(DomainRelationship.SELECT_ID);
		           String fromPrereq = (String) relMap.get("from["+rel_preReq+"]");
		           String toPrereq = (String) relMap.get("to["+rel_preReq+"]");
		           String busid = (String) relMap.get(DomainObject.SELECT_ID);
			
		           String direction="from";
				
		           /* Manage Prerequisiste */
		           if(relType.equals(rel_preReq)) {
		 			   if(toPrereq.equals("True")) {
		 				  direction = "from";
		        	   } else if (fromPrereq.equals("True")) {
		        		   direction = "to"; 
		        	   }
		 			   setPreRequisite(context,relid,objNE,direction);
		           } else if (relType.equals(rel_affectedItem)) {
		        	   /* Affected Item */
		        	   String attr_reqChange = strReqChangeDefaultValue;
		        	   String sPartId = (String) relMap.get(DomainObject.SELECT_ID);
		        	   String rel_affecteItemId = (String)relMap.get(DomainRelationship.SELECT_ID);
		        	   setAffectedItem(context,objNE,sPartId, attr_reqChange,rel_affecteItemId);
		           } else if (relType.equals(rel_reportedAgainstChange)) {
 		        	   if (objCOId!=null && objCOId.length() > 0 ) {
        	    		   setReportedAgainstItem(context,relid,objCOId);
		              	   }
		           } else if (relType.equals(rel_ObjectRoute)) {
		           
		        	   // Get the Route info
		        	   String attrBasePurposeVal = (String) relMap.get(str_attrRouteBasePurpose);
		        	   
						if (attrBasePurposeVal.trim().equals("Review")) {
			        	   if (sCurrentState.equals("Define Components") || sCurrentState.equals("Design Work") || sCurrentState.equals("Create")) 
			        	   {
			        		   //  Temporary setting it to off
			        		   setReviewerRoute(context,relid,objNE,busid);
			        	   }
						} else if (attrBasePurposeVal.trim().equals("Approval")) {
								if (((objCOId!=null && objCOId.length() > 0 )) && (sCurrentState.equals("Define Components") || sCurrentState.equals("Design Work") || sCurrentState.equals("Create"))) 
								{
									setApprovalRoute(context,relid,objCOId,busid);
								}
		         }
		         }
		           
		        } // For Loop 
				
				/* Finally set the state */
				int iFlag;
				
				if (sCurrentState.equals("Release") || sCurrentState.equals("Implemented")) {
					/* Put these objects to Complete State of CA */
					iFlag = neDO.setState(context, "Complete");
				} else if (sCurrentState.equals("Create")) {
					// Put these objects to Prepare State of CA
					iFlag = neDO.setState(context, "Prepare");
				} else if (sCurrentState.equals("Define Components") || sCurrentState.equals("Design Work")) {
					// Put these objects to In Work State of CA
					iFlag = neDO.setState(context, "In Work");
				} else if (sCurrentState.equals("Review")) {
					/* Check to see if there are any other CA's connected to CO which are in state less than Approved State
					 * If yes, do not promote the CO, else promote the CO to Approved state 
					 * */
					
					if(objCOId != null && !"".equals(objCOId) && objCOId.length() > 0){
						checkandPromoteCO(context,objCOId,objNE);
					}
					
					// Put these objects to Approved State of CA
					iFlag = neDO.setState(context, "Approved");
				} else if (sCurrentState.equals("Cancelled")) {
					// Put these objects to Cancelled State and Cancelled Policy
					neDO.setPolicy(context, "Cancelled");
				} else {
					// No state mapping found : Exception Change objects
				}
				
				loadMigratedOids (oid);
				writer.write("Completed Migration \n");
				

			}  // SFlag

			}
			}
			catch (Exception e) {
				e.printStackTrace();
				writer.write("Exception in migrateObjects routine \n"+e.getMessage());
				throw e;
			}
			finally{
				context.setCustomData("SetExpressionModeNoSynchronize","FALSE"); 	
			}
     	   
	}
	  
	/** Overriding the method to add condition for Feature option effectivity 
	 * */
	
		public boolean writeOID(Context context, String[] args) throws Exception {
			String writeIdStr = writeObjectId(context, args);
			String[] changeIds = new String[1];
			String newLine = System.getProperty("line.separator");
			
			if (writeIdStr != null && !"".equals(writeIdStr)) {
					
					changeIds[0] = writeIdStr.trim();
					
					MapList xmlExpression = EffectivityFramework.getXMLExpressionForChange(context, changeIds);
					HashMap xmlExpr = (HashMap) xmlExpression.get(0);
					String xExpr = (String)xmlExpr.get("xmlExpression");
					int i = xExpr.indexOf("ConfigFeature");
					
					/* If the CECO is used for Feature Option Effectivity, ignore them from migration */
					if (i != -1) {
					
						StringList objSelect = new StringList(1);
						objSelect.add(DomainObject.SELECT_CURRENT);
						MapList infoMap = DomainObject.getInfo(context, changeIds, objSelect);
						Map relMap = (Map)infoMap.get(0);
						String pueState = (String)relMap.get(DomainObject.SELECT_CURRENT);
						
						/* Do not migrate only if its unreleased */	
						
						if(pueState.equals("Release") || pueState.equals("Implemented") || pueState.equals("Cancelled")) {
							fileWriter(writeIdStr);
						} else {
								CECOWithFOapplicabilityLog.write(writeIdStr+","+newLine);
						}					
					} else {
						fileWriter(writeIdStr);
					}
					CECOWithFOapplicabilityLog.flush();
			}
			return false;
		}
		
	/** This method creates the CECOWithFOApplicability.csv file and add the page headers.
	 * @throws Exception if any operation fails.
	 */
	
    public static void CECOWithFOApplicability() throws Exception{
    	CECOWithFOapplicabilityLog = new FileWriter(documentDirectory + "CECOWithFOapplicability.csv");
    	CECOWithFOapplicabilityLog.write("The following Configured ECO has the applicability set as Feature Option. These data will not be migrated and has to be completed before the migration.\n");
    	CECOWithFOapplicabilityLog.write("Configred ECO Id \n");
    	CECOWithFOapplicabilityLog.flush();
    }
    
}
