/*
 *  emxUseCaseBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENORequirementsManagementBase/CNext/Modules/ENORequirementsManagementBase/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:20:01 2008 GMT przemek Experimental$
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.productline.ProductLineCommon;


/**
 * The <code>emxBuildBase</code> class contains methods related to Use Case admin type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 *
 */

public class emxUseCaseBase_mxJPO extends emxDomainObject_mxJPO 
{

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public emxUseCaseBase_mxJPO  (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            i18nNow i18nnow = new i18nNow();
            String language = context.getSession().getLanguage();
            String strContentLabel = i18nnow.GetString("emxRequirementsStringResource",
                    language, "emxRequirements.Alert.FeaturesCheckFailed");
            throw  new Exception(strContentLabel);
        }
        return  0;
    }

    /**
     * Get the list of all  Use Cases on the context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return bus ids of feature
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedUseCases (Context context, String[] args) throws Exception {
        MapList relBusObjPageList = new MapList();
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Unpacks the argument for processing
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectId in context
        String strObjectId = (String)programMap.get("objectId");
        //Domain Object initialized with the object id.
        setId(strObjectId);
        String strRel = (String)programMap.get("rel");
        short sRecursionLevel = 1;
        String strType = ReqSchemaUtil.getUseCaseType(context);
        String strRelName = PropertyUtil.getSchemaProperty(context,strRel);
        //The getRelatedObjects method is invoked
        relBusObjPageList = getRelatedObjects(context, strRelName, strType,
                objectSelects, relSelects, false, true, sRecursionLevel, DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING);
        return  relBusObjPageList;
    }

    /**
     * Get the list of all parent objects of the context Use Case context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return bus ids  of parent objectsand rel ids of Use Cases
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    public MapList getUseCasesWhereUsed (Context context, String[] args) throws Exception {
        MapList relBusObjPageList = new MapList();
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Unpacks the argument for processing
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectId in context
        String strObjectId = (String)programMap.get("objectId");
        //Domain Object initialized with the object id.
        setId(strObjectId);
        //Sets the relationship name
        String strUseCaseSubUseCaseReln = ReqSchemaUtil.getSubUseCaseRelationship(context);
        String strUseCaseRequirementReln = ReqSchemaUtil.getRequirementUseCaseRelationship(context);
        String strUseCaseFeatureReln = ReqSchemaUtil.getFeatureUseCaseRelationship(context);
        String strComma = ",";
        String strRelationshipPattern = strUseCaseSubUseCaseReln + strComma
                + strUseCaseRequirementReln + strComma + strUseCaseFeatureReln;
        short sRecursionLevel = 1;
        //The getRelatedObjects method is invoked
        relBusObjPageList = getRelatedObjects(context, strRelationshipPattern,
                DomainConstants.QUERY_WILDCARD, objectSelects, relSelects,
                true, false, sRecursionLevel, DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING);
        return  relBusObjPageList;
    }

   /** This method gets the object Structure List for the context Use Case object.This method gets invoked
     * by settings in the command which displays the Structure Navigator for Use Case type objects
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *      paramMap   - Map having object Id String
     * @return MapList containing the object list to display in Use Case structure navigator
     * @throws Exception if the operation fails
     * @since Product Central 10-6
     */

    public static MapList getStructureList(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap)programMap.get("paramMap");
        String objectId    = (String)paramMap.get("objectId");

        MapList useCaseStructList = new MapList();

        Pattern relPattern = new Pattern(ReqSchemaUtil.getSubUseCaseRelationship(context));
        relPattern.addPattern(ReqSchemaUtil.getUseCaseValidationRelationship(context));

        // include type 'Use Case, Test Case' in Use Case structure navigation list
        Pattern typePattern = new Pattern(ReqSchemaUtil.getUseCaseType(context));
        typePattern.addPattern(ReqSchemaUtil.getTestCaseType(context));

        DomainObject useCaseObj = DomainObject.newInstance(context, objectId);
        String objectType       = useCaseObj.getInfo(context, DomainConstants.SELECT_TYPE);
        if(objectType != null && objectType.equals(ReqSchemaUtil.getUseCaseType(context))){
            try{
                useCaseStructList = ProductLineCommon.getObjectStructureList(context,
                                                                                objectId,
                                                                                relPattern,
                                                                                typePattern);
            }
            catch(Exception ex){
                throw new FrameworkException(ex);
            }
        } else {
            useCaseStructList = (MapList) emxPLCCommon_mxJPO.getStructureListForType(context, args);
        }
        return useCaseStructList;
    }

    /**
     * To obtain the list of Object IDs to be excluded from the search for Add Existing Actions
     *
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the HashMap containing the following arguments
     * @return  StringList - consisting of the object ids to be excluded from the Search Results
     * @throws Exception if the operation fails
     * @author OEP:R208:IR-013439V6R2011
     */
    
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeSubUseCases(Context context, String[] args) throws Exception
    {
	        Map programMap = (Map) JPO.unpackArgs(args);    
	        String strObjectIds = (String)programMap.get("objectId");
	        String strRelationship=(String)programMap.get("relName");
	        StringList excludeList= new StringList();  
	        DomainObject domObjUseCase  = new DomainObject(strObjectIds);
	        

	        // Code for removing the Parent Id's
	        MapList parentObjects=domObjUseCase.getRelatedObjects(context, 
	                PropertyUtil.getSchemaProperty(context,strRelationship),
	                "*",
	                new StringList(DomainConstants.SELECT_ID), 
	                null, 
	                true, 
	                false, 
	               (short) 0,
	                DomainConstants.EMPTY_STRING, 
	                DomainConstants.EMPTY_STRING);
	         
	        for(int i=0;i<parentObjects.size();i++){
	            Map tempMap=(Map)parentObjects.get(i);
	            excludeList.add((String)tempMap.get(DomainConstants.SELECT_ID));
	        }
	        
	        // Code use to remove those objects which are already added in list.
	       MapList currentObjectIDs=domObjUseCase.getRelatedObjects(context, 
	        	PropertyUtil.getSchemaProperty(context,strRelationship),
	        	"*",
	        	new StringList(DomainConstants.SELECT_ID), 
	                null, 
	                false, true,
	                (short)1, 
	                null, 
	                null,0);
	        
	        for(int iCount=0;iCount<currentObjectIDs.size();iCount++)
	        {
	            Map tempMap=(Map)currentObjectIDs.get(iCount);
	            String tempID = (String)tempMap.get(DomainConstants.SELECT_ID);
	            excludeList.add(tempID);
	        }
	        
	        excludeList.add(strObjectIds);
	        return excludeList;
    }
}
