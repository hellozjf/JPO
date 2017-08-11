import matrix.db.Context;

import java.util.HashMap;
import java.util.Map;
import matrix.db.JPO;

import com.matrixone.apps.effectivity.CompositionBinary;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.util.StringList;

// emxCompositionBinaryBase.java
//
// Copyright (c) 1992,2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// Dassault Systemes. Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

/**
 * The <code>emxCompositionBinaryBase</code> class contains utilities to handle
 * Composition Binary.  The methods will create and update the composition binary storage
 * when a relationship is configured for Composition Binary.
 *   
 */

public class emxCompositionBinaryBase_mxJPO {

    /**
     * Default Constructor
     * @param context the ENOVIA <code>Context</code> object
     * @param args String array of arguments
     * @throws Exception
     */
    public emxCompositionBinaryBase_mxJPO(Context context, String[] args)
	    throws Exception {
		super();
    }

     /**
     * Updates the composition binary when a relationship that is part
     * of that composition is created or deleted.  This trigger should be placed
     * on any relationship that is configured to capture composition binary. 
     * 
     * @param context the ENOVIA <code>Context</code> object
     * @param args String array of arguments in the following order      
     *   			[0] = relationship id
     * 				[1] = relationship type
     * 				[2] = from object id
     * 				[3] = to object id 
     * @return int 0 for success and 1 for failure
     * @throws Exception throws exception if the operation fails
     */
    public int updateCompositionBinary(Context context, String[]args) throws Exception
    {
        int returnStatus = 0;
        
        String strIPMLCommandName=PropertyUtil.getGlobalRPEValue(context,"IPMLCommandName");
		
		
		if (strIPMLCommandName == null|| "".equalsIgnoreCase(strIPMLCommandName)|| "null".equalsIgnoreCase(strIPMLCommandName)){

    	returnStatus = CompositionBinary.updateCompositionBinary(context, args);
    	
		}
     	
    	return returnStatus;    
    }

    public MapList testExpand(Context context, String[]args) throws Exception
    {
        MapList allRequirementsList = new MapList();
        Map programMap = (HashMap) JPO.unpackArgs(args);
    	String objectId = (String)programMap.get("objectId");

    	StringList selectStmts = new StringList(1);
    	selectStmts.add("id");
    	StringList selectRelStmts = new StringList(1);
    	selectRelStmts.add("id[connection]");
    	DomainObject domObj = DomainObject.newInstance(context, objectId);
    	allRequirementsList = domObj.getRelatedObjects(context, "Sub Requirement", "*",
                          selectStmts, selectRelStmts, false, true, (short) 0, null, null);
    	return allRequirementsList;    
    }

    public MapList testExpand2(Context context, String[]args) throws Exception
    {
        MapList allRequirementsList = new MapList();
        Map programMap = (HashMap) JPO.unpackArgs(args);
    	String objectId = (String)programMap.get("objectId");

    	StringList selectStmts = new StringList(1);
    	selectStmts.add("id");
    	StringList selectRelStmts = new StringList(1);
    	selectRelStmts.add("id[connection]");
    	DomainObject domObj = DomainObject.newInstance(context, objectId);
    	allRequirementsList = domObj.getRelatedObjects(context, "Sub Requirement", "*",
                          selectStmts, selectRelStmts, true, false, (short) 0, null, null);
    	return allRequirementsList;    
    }

    public MapList getRefDoc(Context context, String[]args) throws Exception
    {
        MapList allRequirementsList = new MapList();
        Map programMap = (HashMap) JPO.unpackArgs(args);
    	String objectId = (String)programMap.get("objectId");

    	StringList selectStmts = new StringList(1);
    	selectStmts.add("id");
    	StringList selectRelStmts = new StringList(1);
    	selectRelStmts.add("id[connection]");
    	DomainObject domObj = DomainObject.newInstance(context, objectId);
    	allRequirementsList = domObj.getRelatedObjects(context, "Reference Document", "*",
                          selectStmts, selectRelStmts, false, true, (short) 0, null, null);
    	return allRequirementsList;    
    }

    public MapList getRefDocUsage(Context context, String[]args) throws Exception
    {
        MapList allRequirementsList = new MapList();
        Map programMap = (HashMap) JPO.unpackArgs(args);
    	String objectId = (String)programMap.get("objectId");

    	StringList selectStmts = new StringList(1);
    	selectStmts.add("id");
    	StringList selectRelStmts = new StringList(1);
    	selectRelStmts.add("id[connection]");
    	DomainObject domObj = DomainObject.newInstance(context, objectId);
    	allRequirementsList = domObj.getRelatedObjects(context, "Reference Document", "*",
                          selectStmts, selectRelStmts, true, false, (short) 0, null, null);
    	return allRequirementsList;    
    }

}
