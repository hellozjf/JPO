
/*
 * enoENGBGTPMigrationBase.java
 * Program to update the Development Part policy to EC Part with propert Release Phase.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.*;
import matrix.util.*;

import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.framework.ui.UIUtil;


public class enoENGBGTPMigrationBase_mxJPO extends emxCommonMigration_mxJPO
{
	String STATE_CONFIGURED_PART_PRELIMINARY = PropertyUtil.getSchemaProperty("policy", EngineeringConstants.POLICY_CONFIGURED_PART, "state_Preliminary");
	String STATE_DEVELOPMEN_PART_OBSOLETE = PropertyUtil.getSchemaProperty("policy", EngineeringConstants.POLICY_DEVELOPMENT_PART, "state_Obsolete");
	String STATE_EC_PART_OBSOLETE = PropertyUtil.getSchemaProperty("policy", EngineeringConstants.POLICY_EC_PART, "state_Obsolete");
	String STATE_CONFIGURED_PART_RELEASE = PropertyUtil.getSchemaProperty("policy", EngineeringConstants.POLICY_CONFIGURED_PART, "state_Release");
	/**
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @grade 0
	 */
	public enoENGBGTPMigrationBase_mxJPO (Context context, String[] args)
	throws Exception
	{
		
		super(context, args);
	}

	
	/**
	 * This method does the migration work.
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectIdList StringList holds list objectids to migrate
	 * @returns nothing
	 * @throws Exception if the operation fails
	 */
	@SuppressWarnings("unchecked")
	public void  migrateObjects(Context context, StringList objectIdList) throws Exception {

		if (scan) {
			return;
		}

		int listSize = (objectIdList == null) ? 0 : objectIdList.size();
 		if (listSize < 0) 
 			return;

 		
		ContextUtil.startTransaction(context, true);
		try {
			String partObjId;
			String sPolicy;
			String sCurrentState;
			
			Map stateMap = new HashMap();
			stateMap.put(STATE_DEVELOPMENT_PART_CREATE, EngineeringConstants.STATE_EC_PART_PRELIMINARY);
			stateMap.put(STATE_DEVELOPMENT_PART_PEER_REVIEW, EngineeringConstants.STATE_EC_PART_REVIEW);
			stateMap.put(STATE_DEVELOPMENT_PART_COMPLETE, EngineeringConstants.STATE_EC_PART_RELEASE);
			stateMap.put(STATE_DEVELOPMEN_PART_OBSOLETE, STATE_EC_PART_OBSOLETE);
			
			String sDevChgMandatory = EnoviaResourceBundle.getProperty(context,"emxTeamEngineering.TeamChange.Mandatory");
			String sISWIPMode = "true";
			boolean isXCEInstalled = FrameworkUtil.isSuiteRegistered(context, "appVersionEngineeringConfigurationCentral", false, null, null);
			if(isXCEInstalled){
				sISWIPMode = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOM.WIPBOM.Allowed");
			}
			StringList slSelectables = new StringList(2);
			slSelectables.addElement(SELECT_POLICY);
			slSelectables.addElement(SELECT_CURRENT);
			
			Map attributeMap;
			Map mResultMap;
			
			DomainObject domObj;
			
			for (int i = 0; i < listSize; i++) {
				partObjId = (String) objectIdList.get(i);
				domObj = DomainObject.newInstance(context, partObjId);
				
				mResultMap = domObj.getInfo(context,slSelectables);
				
				sPolicy = (String) mResultMap.get(SELECT_POLICY);
				sCurrentState = (String) mResultMap.get(SELECT_CURRENT);
				
				attributeMap = new HashMap();
				
				if (POLICY_DEVELOPMENT_PART.equalsIgnoreCase(sPolicy)) {
					attributeMap.put(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE, "Development");
					
					if ("false".equalsIgnoreCase(sDevChgMandatory))	{ 
						attributeMap.put(EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED, "False"); 
					}
					else{
						attributeMap.put(EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED, "True"); 
					}
					
					domObj.setAttributeValues(context, attributeMap);
					
					MqlUtil.mqlCommand(context, "mod bus $1 policy $2",partObjId,POLICY_EC_PART);
					String sECPartState = (String) stateMap.get(sCurrentState);
					domObj.setState(context, sECPartState);
					
					loadMigratedOids(partObjId);
				}
				else if(UIUtil.isNotNullAndNotEmpty(sPolicy) && POLICY_EC_PART.equalsIgnoreCase(sPolicy)){
					attributeMap.put(EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED, "True");
					domObj.setAttributeValues(context, attributeMap);
					loadMigratedOids(partObjId);
				}
				else if(EngineeringConstants.POLICY_CONFIGURED_PART.equalsIgnoreCase(sPolicy) ){

					if(STATE_CONFIGURED_PART_RELEASE.equals(sCurrentState)){
						attributeMap.put(EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED, "True");
					}
					else{
						if("true".equalsIgnoreCase(sISWIPMode)){
							attributeMap.put(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE, "Development");
							attributeMap.put(EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED, "False");
						}
						else
							attributeMap.put(EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED, "True");
					}
					domObj.setAttributeValues(context, attributeMap);
					loadMigratedOids(partObjId);
				}
				else {
					writeUnconvertedOID( "The policy of the object ID -- "+partObjId+" is neither Development Part nor Configured Part",partObjId);
				}
			}
			ContextUtil.commitTransaction(context);

		}catch(Exception ex)
		{
			ContextUtil.abortTransaction(context);
			ex.printStackTrace();
			throw ex;
		}
	}
}

