
// ImpactAnalysisBase.java
// Copyright Dassault Systemes, 2007. All rights reserved
// This program is proprietary property of Dassault Systemes and its subsidiaries.
// This documentation shall be treated as confidential information and may only be used by employees or contractors
//  with the Customer in accordance with the applicable Software License Agreement

import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;

/**
 *
 * Copyright (c) 2007-2016 Dassault Systemes..
 */
public class enoECMImpactAnalysisBase_mxJPO extends DomainObject
{
	
    /**
     * Constructs a new ImpactAnalysisBase JPO object.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */

    public enoECMImpactAnalysisBase_mxJPO(Context context, String[] args) throws Exception
    {
        super();
    }
    
    /**
     * Get All the Impact Analysis connected to the context object
     * @param context
     * @param args
     * @return MapList
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getImpactAnalysisForCO (Context context, String[] args) throws Exception 
    {
    	MapList mlIAOutput = new MapList();
    	try
    	{
    		//unpacking the Arguments from variable args
    		HashMap programMap = (HashMap)JPO.unpackArgs(args);
    		//getting parent object Id from args
    		String strParentId = (String)programMap.get("objectId");
    		//Impact Analysis Objects is selected by its Ids
    		String relImpactAnalysis = PropertyUtil.getSchemaProperty(context,"relationship_ImpactAnalysis");
    		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    		objectSelects.addElement("from["+ChangeConstants.RELATIONSHIP_IMPACT_ANALYSIS+"].to.id");
    		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    		short recurseToLevel = -1;
    		this.setId(strParentId);
    		
    		mlIAOutput = getRelatedObjects(context,
    				ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "," + relImpactAnalysis,
    				TYPE_IMPACT_ANALYSIS,
    				objectSelects,
    				relSelects,
    				false,
    				true,
    				recurseToLevel,
    				"",
    				DomainConstants.EMPTY_STRING,
    				0);
    	}
    	catch(Exception Ex)
    	{
    		Ex.printStackTrace();
    		throw Ex;
    	}
    	return  mlIAOutput;
    }
	
	/**
     * Get All the Impact Analysis connected to the context object
     * @param context
     * @param args
     * @return MapList
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getImpactAnalysis (Context context, String[] args) throws Exception 
    {
    	MapList mlIAOutput = new MapList();
    	try
    	{
    		//unpacking the Arguments from variable args
    		HashMap programMap = (HashMap)JPO.unpackArgs(args);
    		//getting parent object Id from args
    		String strParentId = (String)programMap.get("objectId");
    		String strRelImpactAnalysis = PropertyUtil.getSchemaProperty(context,"relationship_ImpactAnalysis");
    		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    		short recurseToLevel = 1;
    		this.setId(strParentId);

    		mlIAOutput = getRelatedObjects(context,
    				strRelImpactAnalysis,
    				DomainConstants.TYPE_IMPACT_ANALYSIS,
    				objectSelects,
    				relSelects,
    				false,
    				true,
    				recurseToLevel,
    				"",
    				DomainConstants.EMPTY_STRING,
    				0);
    	}
    	catch(Exception Ex)
    	{
    		Ex.printStackTrace();
    		throw Ex;
    	}
    	return  mlIAOutput;
    }
    
    /**
     * Connecting the Impact Analysis Created to the context object.
     * @param context
     * @param args
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void connectImpactAnalysis(Context context, String[] args)throws Exception
 	{
 		try
 		{
 			HashMap hmParamMap = (HashMap) JPO.unpackArgs(args);
 			HashMap hmRequestMap = (HashMap)hmParamMap.get("requestMap"); 			
 			HashMap hmparamMap = (HashMap)hmParamMap.get("paramMap");
 			String strRelImpactAnalysis = PropertyUtil.getSchemaProperty(context,"relationship_ImpactAnalysis");
 			// Retriving the Context Object ID
 			String strContextObjId = (String)hmRequestMap.get("objectId");
 			// Retriving the Impact Analysis ID
 			String strImpactAnalysisId = (String)hmparamMap.get("objectId");
 			// Connection made between IA and Context Object
 			DomainRelationship.connect(context,  new DomainObject(strContextObjId), new RelationshipType(strRelImpactAnalysis),new DomainObject(strImpactAnalysisId));
 		}
 		catch(Exception Ex)
 		{
 			Ex.printStackTrace();
 			throw Ex;
 		}
 	}
    
    /**
     * Exclude OID Program - Excluding IA which are already connected to Context Objects
     * @param context
     * @param args
     * @throws Exception
     */
    public StringList excludeConnectedIAs(Context context, String[] args)throws Exception
 	{
    	StringList excludeList = new StringList(10);
 		try
 		{
 			HashMap hmParamMap = (HashMap) JPO.unpackArgs(args);
 			String strparentOID = (String) hmParamMap.get("parentOID");
 			String strRelImpactAnalysis = PropertyUtil.getSchemaProperty(context,"relationship_ImpactAnalysis");
 			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    		this.setId(strparentOID);
 			
 			MapList mlImpactAnalysis = getRelatedObjects(context,
    				strRelImpactAnalysis,
    				DomainConstants.TYPE_IMPACT_ANALYSIS,
    				objectSelects,
    				relSelects,
    				false,
    				true,
    				(short) 1,
    				"",
    				DomainConstants.EMPTY_STRING,
    				0);

 			for (Object var : mlImpactAnalysis)
 			{
 				 Map tempMap = (Map) var;
  	            excludeList.add(tempMap.get(DomainConstants.SELECT_ID));
 			}
 		}
 		catch(Exception Ex)
 		{
 			Ex.printStackTrace();
 			throw Ex;
 		}
 		return excludeList;
 	}
}

