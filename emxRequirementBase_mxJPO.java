/*
** emxRequirementBase
**
** Copyright (c) 1992-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
** static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.12.2.6.1.1.1.8 Thu Dec 18 16:42:30 2008 GMT ds-qyang Experimental$
**
** formatted with JxBeauty (c) johann.langhofer@nextra.at
*/

/* 
Change History:

Date			Change By	Review	Release	Bug/Functionality	Details
-----------------------------------------------------------------------------------------------------------------------------------------
8/10/2012		OEP			DJH		2013x	IR-181257V6R2013x	Modified expandDocumentationObjectsForRequirement() - HashMap containing 
																the value of "Expand Level", which get the number of levels to be expanded. 
10/03/2012		LX6			QYG		2014	IR-187552V6R2014	NHI:V6R214:Function_026281: Requirement is getting promoted to release 
																state when associated parameter is not in release state. 
12/17/2012		JX5			QYG		2013x	V6 Security			Added method getGrantedRequirements
02/05/2013		QYG					2014	IR-213737V6R2014	workaround regression with effectivity toolbar
03/19/2013      LX6		    QYG     2014    UI enhancement      This delivery consists in UI enhancement on tables and forms
05/07/2013		JX5			QYG		2014	UI enhancement		Add relationship type information in getSub_DerivedRequirments
05/13/2013      ZUD         T25     2014    IR-234292V6R2014     NHIV6R215-039037 : Creation of Sub/Derived Requirement is displaying error message after applying filter on requirement structur  
03/13/2014      ZUD         DJH     2015x  HL Parameter Under Requirement: Creation of  function getParam_TestCaseRequirements
07/08/2014      QYG                 2015x   migrating committed requirements list into Structure Browser
02/02/2015      KIE1        ZUD     2016x   IR-338259-3DEXPERIENCER2016  	HTML code displayed for Requirement specificaiotn in exported excel sheet.
06/11/2015      LX6         ZUD     2016x   IR-374998-3DEXPERIENCER2016x  	When refining requirements, Change of relationship status of requirement in drop down menu is not getting saved properly
07/01/2015      QYG                 2016x   in checkChildParameterRelease, ignore objects with policy ParameterAggregation
06/31/2017      KIE1        ZUD     2017x   IR-510602-3DEXPERIENCER2017x: Requirement Specifications - ALL Option does not work and gives the error
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.dassault_systemes.requirements.ReqConstants;
import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIToolbar;
import com.matrixone.apps.productline.Model;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.apps.requirements.RequirementsCommon;
import com.matrixone.apps.requirements.RequirementsUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;


/**
 * This JPO class has some methods pertaining to Requirement type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxRequirementBase_mxJPO extends emxDomainObject_mxJPO
{
    /**
     * Variable for Not equal to
     */
    public static final String SYMB_NOT_EQUAL = " != ";
    
	/**
	 * Variable for Quote
	 */
    public static final String SYMB_QUOTE = "'";
    /**
     * Variable for Open Parameter
     */
    public static final String SYMB_OPEN_PARAN = "(";
	/**
	 * Variable for Close Parameter
	 */
    public static final String SYMB_CLOSE_PARAN = ")";
    /**
     * Variable for OR Operator
     */
	public static final String SYMB_OR = " || ";
	/**
	 * Variable for And Operator
	 */
    public static final String SYMB_AND = " && ";
    protected static final String SYMB_EQUAL = " == ";
    protected static final String SYMB_COMMA = ",";
    protected static final String SYMB_SPACE = " ";
    protected static final String SYMB_OBJECT_ID = "objectId";
    protected static final String SYMB_FULFILLED = "Fulfilled";
    protected static final String SYMB_NOT_FULFILLED = "NotFulfilled";
    protected static final String SYMB_DONT_CARE = "DontCare";
    protected static final String STR_OBJECT_LIST = "objectList";

//  Added:7-May-09:kyp:R207:RMT Bug 331464
    /**
     *  Variable for Not operator
     */
    public static final String SYMB_NOT = "!";
    /**
     *  Variable for MatchList
     */
    public static final String SYMB_MATCHLIST = "matchlist";
    /**
     * Variable for Release state
     */
    public static final String SYMBOLIC_STATE_RELEASE = "state_Release";
    /**
     * Variable for Obsolete state
     */
    public static final String SYMBOLIC_STATE_OBSOLETE = "state_Obsolete";
// End:R207:RMT Bug 331464

    /**
     * Variable for Reserved By
     */
    public static final String SELECT_RESERVED_BY = "reservedby";

    /* Strings to read values from properties files */
    /**
     * Variable for Requirement String Resources
     */
    public static final String RESOURCE_BUNDLE_PRODUCTS_STR = "emxRequirementsStringResource";
    protected static final String SYMB_INVALID_FEATURE = "emxRequirements.RequirementStructureBrowser.InvalidFeature";
    protected static final String SYMB_INVALID_REQUIREMENT = "emxRequirements.RequirementStructureBrowser.InvalidRequirement";
    protected static final String SYMB_INVALID_OBJECT = "emxRequirements.RequirementStructureBrowser.NotReleased";
    protected static final String SYMB_VALID_OBJECT = "emxRequirements.RequirementStructureBrowser.Release";
    //Mar 13, 2006 - added for bug 316691 by Enovia MatrixOne
    protected static final String SYMB_OBJECT_FULFILLED = "emxRequirements.RequirementStructureBrowser.Fulfilled";
    //end of add.

    /**
     * Create a new emxRequirementBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a emxRequirementBase object.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public emxRequirementBase_mxJPO(Context context, String[] args)
        throws Exception
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
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public int mxMain (Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
        {
            String language = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed");
            throw  new Exception(strContentLabel);
        }
        return(0);
    }

    /**
     * Method call to get all the requirements in the data base.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args  Holds the parameters passed from the calling method
     * @return Object - MapList containing the id of all Requirement objects
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllRequirements (Context context, String[] args)
        throws Exception
    {
        // IR-265854: remove version object using post processing to avoid access issue
        String strWhereExpression = null; 
        MapList Reqs = getDesktopRequirements(context, strWhereExpression, null);
        MapList list;
        ContextUtil.pushContext(context);
        try{
            String[] objectIds = new String[Reqs.size()];
            for(int i = 0; i < Reqs.size(); i++){
                objectIds[i] = (String)((Map<?,?>)Reqs.get(i)).get(SELECT_ID);
            }
            StringList selects = new StringList(SELECT_POLICY);
            selects.addElement(SELECT_ID);
            list = DomainObject.getInfo(context, objectIds, selects);
        }finally{
            ContextUtil.popContext(context);
        }
        for(int i = list.size() - 1; i >= 0; i--){
            String policy = (String)((Map<?,?>)list.get(i)).get(SELECT_POLICY);
            if(ReqSchemaUtil.getRequirementVersionPolicy(context).equals(policy)){
                list.remove(i);
            }
        }
        return list;
    }

    /**
     * Get the list of all owned requirements.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args  Holds the parameters passed from the calling method
     * @return MapList - MapList containing the id of all owned Requirement objects.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOwnedRequirements (Context context, String[] args)
        throws Exception
    {
        // forming the Owner Pattern clause
        String strOwnerCondition = context.getUser();
		String strWhereExpression = "policy != '" + ReqSchemaUtil.getRequirementVersionPolicy(context) + "'";
        //Calls the protected method to retrieve the data
        MapList mapBusIds = getDesktopRequirements(context, strWhereExpression, strOwnerCondition);
        return(mapBusIds);
    }

	/**
     * Get the list of all granted requirements.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args  Holds the parameters passed from the calling method
     * @return MapList - MapList containing the id of all delegated Requirement objects.
     * @throws Exception if the operation fails
     * @since ProductCentral R2014
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getGrantedRequirements (Context context, String[] args)
        throws Exception
    {
        // forming the where clause
        String strWhereExpression = "ownership.project=='";
        strWhereExpression = strWhereExpression.concat(context.getUser().concat("_PRJ'"));
        
        strWhereExpression = SYMB_OPEN_PARAN + strWhereExpression + SYMB_CLOSE_PARAN + SYMB_AND + SYMB_OPEN_PARAN + "policy != '" + 
                                           ReqSchemaUtil.getRequirementVersionPolicy(context) + "'" + SYMB_CLOSE_PARAN;

        //Calls the protected method to retrieve the data
        MapList mapBusIds = getDesktopRequirements(context, strWhereExpression, null);
        return(mapBusIds);
    }

    /**
     * Get the list of all assignend requirements.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args  Holds the parameters passed from the calling method
     * @return MapList - MapList containing the id of all assigned Requirement objects.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAssignedRequirements (Context context, String[] args)
        throws Exception
    {
        // forming the where clause
        String strRelationship = RequirementsUtil.getAssignedRequirementRelationship(context);
        String strWhereExpression = "to[" + strRelationship + "].from."+ DomainConstants.SELECT_NAME +"=='";
        strWhereExpression = strWhereExpression.concat(context.getUser().concat("'"));
        //Calls the protected method to retrieve the data
        MapList mapBusIds = getDesktopRequirements(context, strWhereExpression,null);
        return(mapBusIds);
    }

    /**
     * Get the list of all related requirements.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - Hashmap containing the object id.
     * @return MapList - MapList containing the id of all related requirements connecting that Requirement.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedRequirements (Context context, String[] args)
        throws Exception
    {
        //Unpacks the argument for processing
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectId in context
        String strObjectId = (String)programMap.get("objectId");
        //String List initialized to retrieve back the data
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        //Sets the relationship name to the one connecting Sub Requirement,Feature and Product to Requirement
        String strSubRequirementReln = ReqSchemaUtil.getSubRequirementRelationship(context);
        String strFeatureRequirementReln = ReqSchemaUtil.getRequirementSatisfiedByRelationship(context);
        String strProductRequirementReln = ReqSchemaUtil.getProductRequirementRelationship(context);
        String strComma = ",";
        String strRelationship = strSubRequirementReln + strComma + strFeatureRequirementReln
                + strComma + strProductRequirementReln;
        //Domain Object initialized with the object id.
        setId(strObjectId);
        short sh = 1;
        String strType = ReqSchemaUtil.getRequirementType(context);
        //The getRelatedObjects method is invoked to get the list of Sub Requirements.
        MapList relBusObjPageList = getRelatedObjects(context, strRelationship, strType, objectSelects,
                relSelects, false, true, sh, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
        return(relBusObjPageList);
    }

    /**
     * Get the list of all Parent requirements.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - Hashmap containing the object id.
     * @return MapList - MapList containing the id of all Parent requirements of that Requirement.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public MapList getParentRequirements (Context context, String[] args)
        throws Exception
    {
        //Unpacks the argument for processing
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectId in context
        String strObjectId = (String)programMap.get("objectId");
        //String List initialized to retrieve back the data
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        //Sets the relationship name to the one connecting Parent Requirement and Requirement
        String strRelationship = ReqSchemaUtil.getSubRequirementRelationship(context);
        String strType = ReqSchemaUtil.getRequirementType(context);
        //Domain Object initialized with the object id.
        setId(strObjectId);
        short sh = 1;
        //The getRelatedObjects method is invoked to get the list of Parent Requirements.
        MapList relBusObjPageList = getRelatedObjects(context, strRelationship, strType, objectSelects,
                relSelects, true, false, sh, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
        return(relBusObjPageList);
    }

    /**
     * Get the list of all Affected items.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - Hashmap containing the object id.
     * @return MapList - MapList containing the id of all affected Items connecting that Requirement.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public MapList getAffectedItems (Context context, String[] args)
        throws Exception
    {
        //Unpacks the argument for processing
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectId in context
        String strObjectId = (String)programMap.get("objectId");
        //String List initialized to retrieve back the data
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        //Sets the relationship name to the one connecting Feature and Requirement
        String strRelationship = ReqSchemaUtil.getRequirementSatisfiedByRelationship(context);
        //Domain Object initialized with the object id.
        setId(strObjectId);
        short sh = 1;
        //The getRelatedObjects method is invoked to get the list of all Features connected to Requirement.
        MapList relBusObjPageList = getRelatedObjects(context, strRelationship, DomainConstants.QUERY_WILDCARD,
                objectSelects, relSelects, true, true, sh, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
        return(relBusObjPageList);
    }

    /**
     * Get the list of Desktop Requirements.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strWhereCondition - String value containing the condition based on which results are to be filtered.
     * @param strOwnerCondition - String value containing the owner condition based on which results are to be filtered.
     * @return MapList - MapList containing the id of Desktop Requirement objects based on whereCondition .
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    protected MapList getDesktopRequirements (Context context, String strWhereCondition, String strOwnerCondition)
        throws Exception
    {
        //String list initialized to retrieve data for the Requirements
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        String strType = ReqSchemaUtil.getRequirementType(context);
     // KIE1 added for IR-510602-3DEXPERIENCER2017x
        int searchLimit = 0;
  		try {
  			String property = EnoviaResourceBundle.getProperty(context,"emxRequirements.RequirementsObject.SearchLimit");
  			if (property != null && property.trim().length() > 0) {
  				searchLimit = Integer.valueOf(property.trim());
  			}
  		} catch (Exception ex) {
  		}
        
        //The findobjects method is invoked to get the list of products
        MapList mapBusIds = findObjects(context, strType, null,null,strOwnerCondition,DomainConstants.QUERY_WILDCARD,strWhereCondition,null,true, objectSelects, (short)searchLimit);
        return(mapBusIds);
    }

    /** This method gets the object Structure List for the context Requirement object.This method gets invoked
     *  by settings in the command which displays the Structure Navigator for Requirement type objects
     *  @param context the eMatrix <code>Context</code> object
     *  @param args    holds the following input arguments:
     *      paramMap   - Map having object Id String
     *  @return MapList containing the object list to display in Requirement structure navigator
     *  @throws Exception if the operation fails
     *  @since Product Central 10-6
     */
    public static MapList getStructureList(Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap)programMap.get("paramMap");
        String objectId    = (String)paramMap.get("objectId");

        MapList requirementStructList = new MapList();

        Pattern relPattern = new Pattern(ReqSchemaUtil.getSubRequirementRelationship(context));
        relPattern.addPattern(ReqSchemaUtil.getDerivedRequirementRelationship(context));
        relPattern.addPattern(ReqSchemaUtil.getRequirementValidationRelationship(context));
        relPattern.addPattern(ReqSchemaUtil.getRequirementUseCaseRelationship(context));

        // include type 'Requirement, Test Case' and 'Use Case' in Requirement structure navigation list
        Pattern typePattern = new Pattern(ReqSchemaUtil.getRequirementType(context));
        typePattern.addPattern(ReqSchemaUtil.getUseCaseType(context));
        typePattern.addPattern(ReqSchemaUtil.getTestCaseType(context));
        //relPattern.addPattern(ReqSchemaUtil.getParameterUsageRelationship(context));
        //typePattern.addPattern(ReqSchemaUtil.getParameterType(context));

        DomainObject requirementObj = DomainObject.newInstance(context, objectId);
        String objectType           = requirementObj.getInfo(context, DomainConstants.SELECT_TYPE);

        String objectParentType     = emxPLCCommon_mxJPO.getParentType(context, objectType);

        if (objectParentType != null && objectParentType.equals(ReqSchemaUtil.getRequirementType(context)))
        {
            try
            {
               requirementStructList = ProductLineCommon.getObjectStructureList(context, objectId, relPattern, typePattern);
            }
            catch(Exception ex)
            {
               throw new FrameworkException(ex);
            }
        }
        else
        {
            requirementStructList = emxPLCCommon_mxJPO.getStructureListForType(context, args);
        } 
        //MapList requirementStructList = new MapList();
        return(requirementStructList);
    }

    /** Trigger Method to check if the Sub Requirements are in release state
     *  before promoting the Parent requirement to Release state.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - integer value 0 if success
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public int checkSubRequirementRelease(Context context, String[] args)
        throws Exception
    {
        return checkChildRequirementRelease(context, args, ReqSchemaUtil.getSubRequirementRelationship(context));
	}

    /** Trigger Method to check if the Derived Requirements are in release state
     *  before promoting the Parent requirement to Release state.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - integer value 0 if success
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public int checkDerivedRequirementRelease(Context context, String[] args)
        throws Exception
    {
        return checkChildRequirementRelease(context, args, ReqSchemaUtil.getDerivedRequirementRelationship(context));
	}
//Start IR-187552V6R2014 LX6
    /** Trigger Method to check if the Parameters are in release state
     *  before promoting the Parent requirement to Release state.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - integer value 0 if success
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public int checkChildParameterRelease(Context context, String[] args)
        throws Exception
    {	String RelTypes = ReqSchemaUtil.getParameterUsageRelationship(context) + "," + ReqSchemaUtil.getParameterAggregationRelationship(context);
        return checkChildRequirementRelease(context, args, RelTypes);
	}
//End IR-187552V6R2014 LX6
    /** Util Function for Trigger Methods to check if the child Requirements are in release state
     *  before promoting the Parent requirement to Release state.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @param relationship - holds the the name of the relationship.
     * @return - integer value 0 if success
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public int checkChildRequirementRelease(Context context, String[] args, String relationship)
        throws Exception
    {
        int iFlag = 0;
        String strObjectId = args[0];

        try
        {
            String strRelPattern = relationship;
            String strTypePattern = "";
//Start IR-187552V6R2014 LX6            
            String STATE_RELEASE = "";
            String STATE_OBSOLETE = "";
            if(relationship.contains(ReqSchemaUtil.getParameterUsageRelationship(context))||
               relationship.contains(ReqSchemaUtil.getParameterAggregationRelationship(context)))
            {
            	strTypePattern = ReqSchemaUtil.getParameterType(context);
            	STATE_RELEASE = FrameworkUtil.lookupStateName(context, "Parameter", SYMBOLIC_STATE_RELEASE);
            	STATE_OBSOLETE = FrameworkUtil.lookupStateName(context, "Parameter", SYMBOLIC_STATE_OBSOLETE);
            	if(null == STATE_RELEASE)
                {
                	STATE_RELEASE = "Released";
                }
            	if(null == STATE_OBSOLETE)
                {
            		STATE_OBSOLETE = "Obsolete";
                }
            }
            else
            {
            	STATE_RELEASE = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementPolicy(context), SYMBOLIC_STATE_RELEASE);
            	STATE_OBSOLETE = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementPolicy(context), SYMBOLIC_STATE_OBSOLETE);
            	strTypePattern = ReqSchemaUtil.getRequirementType(context);
            }
//End IR-187552V6R2014 LX6            
            List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
            List lstRelSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            boolean bGetTo = false;
            boolean bGetFrom = true;
            short sRecursionLevel = 1;
            String strBusWhereClause = "";
            String strRelWhereClause = "";
            //mql expand bus 34757.3190.27704.19254 relationship "Sub Requirement" select bus current where "! current matchlist 'Release,Obsolete' ','";
            StringBuffer sbWhereBus = new StringBuffer();
            sbWhereBus.append("policy != 'ParameterAggregation' and ");
            sbWhereBus.append(SYMB_NOT).append(DomainConstants.SELECT_CURRENT);
            sbWhereBus.append(SYMB_SPACE).append(SYMB_MATCHLIST).append(SYMB_SPACE);
            sbWhereBus.append(SYMB_QUOTE).append(STATE_RELEASE).append(SYMB_COMMA).append(STATE_OBSOLETE).append(SYMB_QUOTE).append(SYMB_SPACE);
            sbWhereBus.append(SYMB_QUOTE).append(SYMB_COMMA).append(SYMB_QUOTE);
            strBusWhereClause = sbWhereBus.toString();

            DomainObject domReq = DomainObject.newInstance(context,strObjectId);
            MapList mapSubRequirements = domReq.getRelatedObjects(context, strRelPattern, strTypePattern,
                    (StringList)lstObjectSelects, (StringList)lstRelSelects, bGetTo, bGetFrom,
                    sRecursionLevel, strBusWhereClause, strRelWhereClause);

            if (mapSubRequirements.isEmpty())
                iFlag = 0;
            else
			{
//Start IR-187552V6R2014 LX6            	
                iFlag = 1;
                String strAlertMessage = "";
                if(relationship.contains(ReqSchemaUtil.getParameterUsageRelationship(context))||
                   relationship.contains(ReqSchemaUtil.getParameterAggregationRelationship(context)))
                {
                	strAlertMessage = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(),"emxRequirements.Alert.NonReleasedChildParemeter");
                }
                else
                {
                	strAlertMessage = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(),"emxRequirements.Alert.NonReleasedChildRequirement"); 
                }
//End IR-187552V6R2014 LX6				
				emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
			}
        }
        catch(Exception e)
        {
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }

        return(iFlag);
    }


    /** Trigger Method for Float on Release function.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public void updateParentRequirement(Context context,String[] args)
        throws Exception
    {
        String strObjectId = args[0];
        try
        {
            String strSubRequirementReln = ReqSchemaUtil.getSubRequirementRelationship(context);
            String strType = ReqSchemaUtil.getRequirementType(context);
            StringList lstObjSelects = new StringList(DomainConstants.SELECT_ID);
            //Begin of Add by Enovia MatrixOne for Bug# 300088 on 30-Mar-05
            lstObjSelects.add(DomainConstants.SELECT_TYPE);
            lstObjSelects.add(DomainConstants.SELECT_NAME);
            lstObjSelects.add(DomainConstants.SELECT_REVISION);
            lstObjSelects.add(DomainConstants.SELECT_OWNER);
            //End of Add by Enovia MatrixOne for Bug# 300088 on 30-Mar-05

            StringList lstRelSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            boolean bGetTo = true;
            boolean bGetFrom = false;
            short sRecurseToLevel = 1;
            String strBusWhereClause = "";
            String strRelWhereClause = DomainConstants.EMPTY_STRING;
            String strRelease = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementPolicy(context), SYMBOLIC_STATE_RELEASE);
            String strObsolete = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementPolicy(context), SYMBOLIC_STATE_OBSOLETE);

            StringBuffer sbWhereBus = new StringBuffer();
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(DomainConstants.SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strRelease);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(DomainConstants.SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strObsolete);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            strBusWhereClause = sbWhereBus.toString();

            DomainObject domReq = DomainObject.newInstance(context,strObjectId);

            //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
            //Form the selectlist to get the type, name and revsion of the
            //latest and previous requiremet revision
            StringList lstReqSelects = new StringList(DomainConstants.SELECT_TYPE);
            lstReqSelects.add(DomainConstants.SELECT_NAME);
            lstReqSelects.add(DomainConstants.SELECT_REVISION);

            //Get the type name and revision of the latest revision
            Map mapLatestRevInfo = domReq.getInfo(context, lstReqSelects);
            //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

            BusinessObject boReq = domReq.getPreviousRevision(context);
            String strPrevRevId = boReq.getObjectId();
            if (!(strPrevRevId == null || strPrevRevId.equalsIgnoreCase("") || "null".equalsIgnoreCase(strPrevRevId)))
            {
                DomainObject domPrevRev = new DomainObject(boReq);

                //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                //Get the Type Name Revision ot previous requiremet Revision
                Map mapPrevRevInfo = domPrevRev.getInfo(context, lstReqSelects);
                //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

                MapList mlParentReqList = domPrevRev.getRelatedObjects(context, strSubRequirementReln, strType,
                        lstObjSelects, lstRelSelects, bGetTo, bGetFrom, sRecurseToLevel, strBusWhereClause, strRelWhereClause);

                int iSize = mlParentReqList.size();
                String strRelId = null;

                //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                String strLanguage = context.getSession().getLanguage();
                String strSubjectKey = EnoviaResourceBundle.getProperty(context, 
                														"emxRequirementsStringResource", 
                														context.getLocale(),
                														"emxRequirements.Message.Subject.ObjectReplacedInStructure"); 
                String strMessageKey = DomainConstants.EMPTY_STRING;
                String[] arrFormatMessageArgs = new String[9];

                String[] subjectKeys = {};
                String[] subjectValues = {};
                String[] messageKeys = {};
                String[] messageValues = {};

                arrFormatMessageArgs[0] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_TYPE);
                arrFormatMessageArgs[1] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_NAME);
                arrFormatMessageArgs[2] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_REVISION);

                arrFormatMessageArgs[3] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_TYPE);
                arrFormatMessageArgs[4] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_NAME);
                arrFormatMessageArgs[5] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_REVISION);
                //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

                for(int i=0 ; i<iSize ; i++)
                {
                    strRelId = (String)((Hashtable)mlParentReqList.get(i)).
                        get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    DomainRelationship.setToObject(context,strRelId,domReq);
                    //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                    arrFormatMessageArgs[6] = (String)((Map)mlParentReqList.get(i)).get(DomainConstants.SELECT_TYPE);
                    arrFormatMessageArgs[7] = (String)((Map)mlParentReqList.get(i)).get(DomainConstants.SELECT_NAME);
                    arrFormatMessageArgs[8] = (String)((Map)mlParentReqList.get(i)).get(DomainConstants.SELECT_REVISION);

                    strMessageKey =  MessageUtil.getMessage(context, null,
                            "emxRequirements.Message.Description.ObjectReplacedInStructure",
                            arrFormatMessageArgs, null, context.getLocale(),
                            "emxRequirementsStringResource");

                    //Form the message attachment
                    List lstAttachments = new StringList();
                    lstAttachments.add(strObjectId);
                    lstAttachments.add(strPrevRevId);
                    lstAttachments.add(((Map)mlParentReqList.get(i)).get(DomainConstants.SELECT_ID));

                    //Form the owner list to send the message
                    List lstOwnerList = new StringList();
                    lstOwnerList.add(((Map)mlParentReqList.get(i)).get(DomainConstants.SELECT_OWNER));

                    //Send the notification to the owner
                    emxMailUtilBase_mxJPO.sendNotification( context,
                    (StringList)lstOwnerList, null, null, strSubjectKey, subjectKeys,
                    subjectValues, strMessageKey, messageKeys, messageValues,
                    (StringList)lstAttachments, null);
                    //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
    }

    /** Trigger Method for Float on Release function.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
     public void updateProductRequirement(Context context,String[] args)
         throws Exception
     {
        String strObjectId = args[0];
        try
        {
            String strProdRequirementReln = ReqSchemaUtil.getProductRequirementRelationship(context);
            String strType = ReqSchemaUtil.getProductsType(context);
            List lstObjSelects = new StringList(DomainConstants.SELECT_ID);
            //Begin of Add by Enovia MatrixOne for Bug# 300088 on 30-Mar-05
            lstObjSelects.add(DomainConstants.SELECT_TYPE);
            lstObjSelects.add(DomainConstants.SELECT_NAME);
            lstObjSelects.add(DomainConstants.SELECT_REVISION);
            lstObjSelects.add(DomainConstants.SELECT_OWNER);
            //End of Add by Enovia MatrixOne for Bug# 300088 on 30-Mar-05
            List lstRelSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            boolean bGetTo = true;
            boolean bGetFrom = false;
            short sRecurseToLevel = 1;
            String strBusWhereClause = "";
            String strRelWhereClause = DomainConstants.EMPTY_STRING;
            String strRelease = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getProductPolicy(context), SYMBOLIC_STATE_RELEASE);

            StringBuffer sbWhereBus = new StringBuffer();
            sbWhereBus.append(DomainConstants.SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strRelease);
            sbWhereBus.append(SYMB_QUOTE);
            strBusWhereClause = sbWhereBus.toString();

            DomainObject domReq = DomainObject.newInstance(context,strObjectId);

            //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
             //Form the selectlist to get the type, name and revsion of the
             //latest and previous requiremet revision
             StringList lstReqSelects = new StringList(DomainConstants.SELECT_TYPE);
             lstReqSelects.add(DomainConstants.SELECT_NAME);
             lstReqSelects.add(DomainConstants.SELECT_REVISION);

             //Get the type name and revision of the latest revision
             Map mapLatestRevInfo = domReq.getInfo(context, lstReqSelects);
             //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

            BusinessObject boReq = domReq.getPreviousRevision(context);
            String strPrevRevId = boReq.getObjectId();
            if (!(strPrevRevId == null || strPrevRevId.equalsIgnoreCase("") || "null".equalsIgnoreCase(strPrevRevId)))
            {
                DomainObject domPrevRev = new DomainObject(boReq);

                //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                //Get the Type Name Revision ot previous requiremet Revision
                Map mapPrevRevInfo = domPrevRev.getInfo(context, lstReqSelects);
                //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

                MapList mlParentProductList = domPrevRev.getRelatedObjects(context, strProdRequirementReln, strType,
                        (StringList)lstObjSelects, (StringList)lstRelSelects, bGetTo, bGetFrom,
                        sRecurseToLevel, strBusWhereClause, strRelWhereClause);

                int iSize = mlParentProductList.size();
                String strRelId = null;

                //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                String strLanguage = context.getSession().getLanguage();
                String strSubjectKey =EnoviaResourceBundle.getProperty(context, 
                														"emxRequirementsStringResource", 
                														context.getLocale(),
                														"emxRequirements.Message.Subject.ObjectReplacedInStructure"); 
                String strMessageKey = DomainConstants.EMPTY_STRING;
                String[] arrFormatMessageArgs = new String[9];

                String[] subjectKeys = {};
                String[] subjectValues = {};
                String[] messageKeys = {};
                String[] messageValues = {};

                arrFormatMessageArgs[0] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_TYPE);
                arrFormatMessageArgs[1] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_NAME);
                arrFormatMessageArgs[2] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_REVISION);

                arrFormatMessageArgs[3] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_TYPE);
                arrFormatMessageArgs[4] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_NAME);
                arrFormatMessageArgs[5] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_REVISION);
                //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

                for(int i=0 ; i<iSize ; i++)
                {
                    strRelId = (String)((Hashtable)mlParentProductList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    DomainRelationship.setToObject(context,strRelId,domReq);

                    //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                    arrFormatMessageArgs[6] = (String)((Map)mlParentProductList.get(i)).get(DomainConstants.SELECT_TYPE);
                    arrFormatMessageArgs[7] = (String)((Map)mlParentProductList.get(i)).get(DomainConstants.SELECT_NAME);
                    arrFormatMessageArgs[8] = (String)((Map)mlParentProductList.get(i)).get(DomainConstants.SELECT_REVISION);

                    strMessageKey =  MessageUtil.getMessage(context, null, "emxRequirements.Message.Description.ObjectReplacedInStructure",
                            arrFormatMessageArgs, null, context.getLocale(), "emxRequirementsStringResource");

                    //Form the message attachment
                    List lstAttachments = new StringList();
                    lstAttachments.add(strObjectId);
                    lstAttachments.add(strPrevRevId);
                    lstAttachments.add(((Map)mlParentProductList.get(i)).get(DomainConstants.SELECT_ID));

                    //Form the owner list to send the message
                    List lstOwnerList = new StringList();
                    lstOwnerList.add(((Map)mlParentProductList.get(i)).get(DomainConstants.SELECT_OWNER));

                    //Send the notification to the owner
                    emxMailUtilBase_mxJPO.sendNotification( context,
                    (StringList)lstOwnerList, null, null, strSubjectKey, subjectKeys,
                    subjectValues, strMessageKey, messageKeys, messageValues,
                    (StringList)lstAttachments, null);
                    //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
    }

    /** Trigger Method for Float on Release function.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
     public void updateFeatureRequirement(Context context,String[] args)
         throws Exception
     {
        String strObjectId = args[0];
        try
        {
            String strSatisfiedRequirementReln = ReqSchemaUtil.getRequirementSatisfiedByRelationship(context);
            String strType = ReqSchemaUtil.getFeatureType(context);
            List lstObjSelects = new StringList(DomainConstants.SELECT_ID);
            //Begin of Add by Enovia MatrixOne for Bug# 300088 on 30-Mar-05
            lstObjSelects.add(DomainConstants.SELECT_TYPE);
            lstObjSelects.add(DomainConstants.SELECT_NAME);
            lstObjSelects.add(DomainConstants.SELECT_REVISION);
            lstObjSelects.add(DomainConstants.SELECT_OWNER);
            //End of Add by Enovia MatrixOne for Bug# 300088 on 30-Mar-05

            List lstRelSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            boolean bGetTo = true;
            boolean bGetFrom = false;
            short sRecurseToLevel = 1;
            String strBusWhereClause = "";
            String strRelWhereClause = DomainConstants.EMPTY_STRING;
            String strRelease = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getProductFeaturePolicy(context), SYMBOLIC_STATE_RELEASE);

            StringBuffer sbWhereBus = new StringBuffer();
            sbWhereBus.append(DomainConstants.SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strRelease);
            sbWhereBus.append(SYMB_QUOTE);
            strBusWhereClause = sbWhereBus.toString();

            DomainObject domReq = DomainObject.newInstance(context,strObjectId);

            //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
            //Form the selectlist to get the type, name and revsion of the
            //latest and previous requiremet revision
            List lstReqSelects = new StringList(DomainConstants.SELECT_TYPE);
            lstReqSelects.add(DomainConstants.SELECT_NAME);
            lstReqSelects.add(DomainConstants.SELECT_REVISION);

            //Get the type name and revision of the latest revision
            Map mapLatestRevInfo = domReq.getInfo(context, (StringList)lstReqSelects);
            //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

            BusinessObject boReq = domReq.getPreviousRevision(context);
            String strPrevRevId = boReq.getObjectId();
            if (!(strPrevRevId == null || strPrevRevId.equalsIgnoreCase("") || "null".equalsIgnoreCase(strPrevRevId)))
            {
                DomainObject domPrevRev = new DomainObject(boReq);

                //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                //Get the Type Name Revision ot previous requiremet Revision
                Map mapPrevRevInfo = domPrevRev.getInfo(context, (StringList)lstReqSelects);
                //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

                MapList mlParentFeatureList = domPrevRev.getRelatedObjects(context, strSatisfiedRequirementReln, strType,(StringList)lstObjSelects,
                        (StringList)lstRelSelects, bGetTo, bGetFrom, sRecurseToLevel, strBusWhereClause, strRelWhereClause);

                int iSize = mlParentFeatureList.size();
                String strRelId=null;

                //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                String strLanguage = context.getSession().getLanguage();
                String strSubjectKey = EnoviaResourceBundle.getProperty(context, 
																		"emxRequirementsStringResource", 
																		context.getLocale(),
																		"emxRequirements.Message.Subject.ObjectReplacedInStructure");  
                String strMessageKey = DomainConstants.EMPTY_STRING;
                String[] arrFormatMessageArgs = new String[9];

                String[] subjectKeys = {};
                String[] subjectValues = {};
                String[] messageKeys = {};
                String[] messageValues = {};

                arrFormatMessageArgs[0] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_TYPE);
                arrFormatMessageArgs[1] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_NAME);
                arrFormatMessageArgs[2] = (String)mapPrevRevInfo.get(DomainConstants.SELECT_REVISION);

                arrFormatMessageArgs[3] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_TYPE);
                arrFormatMessageArgs[4] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_NAME);
                arrFormatMessageArgs[5] = (String)mapLatestRevInfo.get(DomainConstants.SELECT_REVISION);
                //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05

                for(int i=0 ; i<iSize ; i++)
                {
                    strRelId = (String)((Hashtable)mlParentFeatureList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    DomainRelationship.setToObject(context,strRelId,domReq);

                    //Begin of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                    arrFormatMessageArgs[6] = (String)((Map)mlParentFeatureList.get(i)).get(DomainConstants.SELECT_TYPE);
                    arrFormatMessageArgs[7] = (String)((Map)mlParentFeatureList.get(i)).get(DomainConstants.SELECT_NAME);
                    arrFormatMessageArgs[8] = (String)((Map)mlParentFeatureList.get(i)).get(DomainConstants.SELECT_REVISION);

                    strMessageKey =  MessageUtil.getMessage(context, null, "emxRequirements.Message.Description.ObjectReplacedInStructure",
                            arrFormatMessageArgs, null, context.getLocale(), "emxRequirementsStringResource");

                    //Form the message attachment
                    List lstAttachments = new StringList();
                    lstAttachments.add(strObjectId);
                    lstAttachments.add(strPrevRevId);
                    lstAttachments.add(((Map)mlParentFeatureList.get(i)).get(DomainConstants.SELECT_ID));

                    //Form the owner list to send the message
                    List lstOwnerList = new StringList();
                    lstOwnerList.add(((Map)mlParentFeatureList.get(i)).get(DomainConstants.SELECT_OWNER));

                    //Send the notification to the owner
                    emxMailUtilBase_mxJPO.sendNotification( context,
                    (StringList)lstOwnerList, null, null, strSubjectKey, subjectKeys,
                    subjectValues, strMessageKey, messageKeys, messageValues,
                    (StringList)lstAttachments, null);
                    //End of Add by Enovia MatrixOne for Bug# 300088 on 28-Mar-05
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
    }

    /** Trigger Method for Float on Release function.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public void requirementFloatOnRelease(Context context,String[] args)
        throws Exception
    {
        try
        {
            updateParentRequirement(context,args);
            updateProductRequirement(context,args);
            updateFeatureRequirement(context,args);
        }
        catch(Exception e)
        {
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
    }

    /**
     * Updates the Design Responsibility field in Requirement WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args     contains a MapList with the following as input arguments or entries:
     *   objectId      holds the context Requirement object Id
     *   New Value     holds the newly selected Design Responsibility Object Id
     * @throws         Exception if the operations fails
     * @since          Common 10-6
     **
     */
    public void updateDesignResponsibility(Context context, String[] args)
        throws Exception
    {
        try
        {
          //unpacking the Arguments from variable args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          HashMap paramMap   = (HashMap)programMap.get("paramMap");

          //Relationship name
          String strDesignResponsibilty = RequirementsUtil.getDesignResponsibilityRelationship(context);

          //Getting the Requirement Object id and the modified Design Responsibilty Object id
          String strReqObjectId = (String)paramMap.get("objectId");
          String strNewDesignResObjectId = (String)paramMap.get("New Value");

          //objectSelects containing Name and Ids
          StringList objectSelects = new StringList();
          objectSelects.addElement(DomainConstants.SELECT_NAME);
          objectSelects.addElement(DomainConstants.SELECT_ID);

          //relSelects containing the relationship Id
          StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
          //setting the context to the Requirement object id
          setId(strReqObjectId);

          //Maplist containing the relationship ids
          MapList relationshipIdList = new MapList();
          //Calling getRelatedObjects to get the relationship ids
          relationshipIdList = getRelatedObjects(context, strDesignResponsibilty, DomainConstants.QUERY_WILDCARD,
                  objectSelects, relSelectsList, true, false, (short)1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

          String strRelationshipId = "";
          String strDesignResId = "";
          int designRes = relationshipIdList.size();
          if (designRes > 0)
          {
              //Getting the realtionship ids from the list
              strRelationshipId = (String)((Hashtable)relationshipIdList.get(0)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
              //Getting the Design Responsibility id from the list
              strDesignResId = (String)((Hashtable)relationshipIdList.get(0)).get(DomainConstants.SELECT_ID);
          }

          // Connecting the Requirement with the new Design Responsibility object
          // using relationship Design Responsibility. Update if old and new ids are not equal.
          if (strNewDesignResObjectId != null && !strNewDesignResObjectId.equals(strDesignResId))
          {
              if (designRes>0)
              {
                  //Disconnecting the existing relationship
                  DomainRelationship.disconnect(context, strRelationshipId);
              }
              if (strNewDesignResObjectId.length() > 0 && !"null".equalsIgnoreCase(strNewDesignResObjectId))
              {
                  //Instantiating DomainObject with the new Design Responsibility 's object id
                  DomainObject domainObjectFromType = newInstance(context, strNewDesignResObjectId);
                  DomainObject domainObjectToType = newInstance(context, strReqObjectId);
                  DomainRelationship.connect(context, domainObjectFromType, strDesignResponsibilty, domainObjectToType);
              }
          }
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
    }


    /** Method for getting status view in the requirement structure browser.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - MapList
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public MapList expandStatusObjects(Context context, String[] args)
        throws Exception
    {
        MapList requirementList=new MapList();
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject = new DomainObject(strObjectId);

            //type post filter
            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(
                                            context,
                                ReqSchemaUtil.getRequirementType(context));
            StringBuffer sbBuffer = new StringBuffer(100);
            sbBuffer.append(ReqSchemaUtil.getRequirementType(context));
            sbBuffer.append(SYMB_COMMA);
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(SYMB_COMMA);
                }
            }
            //object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            //relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            //type filter
            StringBuffer stbTypeSelect = new StringBuffer(50);
            stbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
            stbTypeSelect.append(SYMB_COMMA);
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
                stbTypeSelect = stbTypeSelect.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                    stbTypeSelect = stbTypeSelect.append(SYMB_COMMA);
                }
            }
            //relationship filter
            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                       .append(SYMB_COMMA)
                                       .append(ReqSchemaUtil.getSubRequirementRelationship(context));

            requirementList = domObject.getRelatedObjects(context, stbRelSelect.toString(), stbTypeSelect.toString(),
                    false, true, (short)1, selectStmts, selectRelStmts, null, null, null, sbBuffer.toString(), null);
        }
        catch (Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
        return(requirementList);
    }

    /** Method for getting Documentation view in the requirement structure browser.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - MapList
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public MapList expandDocumentationObjects(Context context, String[] args)
        throws Exception
    {
        MapList documentationList=new MapList();
        try
        {

            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject = new DomainObject(strObjectId);
            String strType=domObject.getInfo(context,DomainConstants.SELECT_TYPE);
            StringBuffer sbTypeSelect=new StringBuffer(60);
            StringBuffer sbRelSelect=new StringBuffer(60);
            boolean bRefDoc = true;
            //object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);

            //relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);


            List lstProductChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getProductsType(context));
            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));
            //Added for Candidate Requirement Structure Browser
            List lstModelChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getModelType(context));
            lstModelChildTypes.add(ReqSchemaUtil.getModelType(context));
            //end of add

            if (lstProductChildTypes.contains(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i=0; i < lstReqChildTypes.size(); i++)
                {
                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getSubRequirementRelationship(context));
                bRefDoc=false;
            }
            ////Added for Candidate Requirement Structure Browser
            else if (lstModelChildTypes.contains(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i=0; i < lstReqChildTypes.size(); i++)
                {
                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getCandidateItemRelationship(context));
                bRefDoc=false;

            }
            //end of add
            else if (lstReqChildTypes.contains(strType) || ReqSchemaUtil.getRequirementType(context).equals(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i=0; i < lstReqChildTypes.size(); i++)
                {
                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }
                sbTypeSelect = sbTypeSelect.append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getTestCaseType(context))
                                           .append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getUseCaseType(context))
                                           .append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getRequirementSpecificationType(context) )
                                           .append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getDocumentType(context));


                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getSubRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getRequirementUseCaseRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getRequirementValidationRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(RequirementsUtil.getRequirementSpecificationRelationship(context));

            }
            else if (strType.equals(ReqSchemaUtil.getTestCaseType(context)))
            {
                sbTypeSelect = sbTypeSelect.append(ReqSchemaUtil.getTestCaseType(context));
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubTestCaseRelationship(context));
            }
            else if (strType.equals(ReqSchemaUtil.getUseCaseType(context)))
            {
                sbTypeSelect = sbTypeSelect.append(ReqSchemaUtil.getTestCaseType(context))
                                           .append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getUseCaseType(context));
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubUseCaseRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getUseCaseValidationRelationship(context));
            }
            documentationList=domObject.getRelatedObjects(context, sbRelSelect.toString(), sbTypeSelect.toString(),
                    selectStmts, selectRelStmts, false, true, (short)1, null, null);

            if (bRefDoc)
            {
                MapList mapTDocuments=domObject.getRelatedObjects(context, ReqSchemaUtil.getReferenceDocumentRelationship(context),
                        DomainConstants.QUERY_WILDCARD, selectStmts, selectRelStmts, false, true, (short)1, null, null);
                if (!mapTDocuments.isEmpty())
                        documentationList.addAll(mapTDocuments);
            }
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
        return(documentationList);
        
    }


    /** Method for getting Documentation view in the requirement structure browser.This will display derived & sub requirement along with test cases, use cases and reference documents connected to the requirement.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - MapList
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandDocumentationObjectsForRequirement(Context context, String[] args)
        throws Exception
    {
        MapList documentationList=new MapList();
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            
            //Start:OEP:10:08:2012:IR-181257V6R2013x
            String expLevel = (String)paramMap.get("expandLevel");
            if("All".equalsIgnoreCase(expLevel)){
          	  expLevel = "0";
            }
            if(expLevel == null || expLevel.length() == 0)
            {
          	  expLevel = "1";
            }
            int maxLevels = Integer.parseInt(expLevel);
            //END:OEP:10:08:2012:IR-181257V6R2013x
            
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject = new DomainObject(strObjectId);
            String strType=domObject.getInfo(context,DomainConstants.SELECT_TYPE);
            StringBuffer sbTypeSelect=new StringBuffer(60);
            StringBuffer sbRelSelect=new StringBuffer(60);
            boolean bRefDoc = true;
            // object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);

            // relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

            List lstProductChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getProductsType(context));
            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));
            // Added for Candidate Requirement Structure Browser
            List lstModelChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getModelType(context));
            lstModelChildTypes.add(ReqSchemaUtil.getModelType(context));
            // end of add

            if (lstProductChildTypes.contains(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i=0; i < lstReqChildTypes.size(); i++)
                {
                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getSubRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getDerivedRequirementRelationship(context));
                bRefDoc=false;
            }
            // //Added for Candidate Requirement Structure Browser
            else if (lstModelChildTypes.contains(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i=0; i < lstReqChildTypes.size(); i++)
                {
                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getCandidateItemRelationship(context));
                bRefDoc=false;

            }
            // end of add
            else if (lstReqChildTypes.contains(strType) || ReqSchemaUtil.getRequirementType(context).equals(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i=0; i < lstReqChildTypes.size(); i++)
                {
                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }
                sbTypeSelect = sbTypeSelect.append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getTestCaseType(context))
                                           .append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getUseCaseType(context))
                                           .append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getDocumentType(context));

                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getSubRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getRequirementUseCaseRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getRequirementValidationRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getDerivedRequirementRelationship(context));

            }
            else if (strType.equals(ReqSchemaUtil.getTestCaseType(context)))
            {
                sbTypeSelect = sbTypeSelect.append(ReqSchemaUtil.getTestCaseType(context));
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubTestCaseRelationship(context));
            }
            else if (strType.equals(ReqSchemaUtil.getUseCaseType(context)))
            {
                sbTypeSelect = sbTypeSelect.append(ReqSchemaUtil.getTestCaseType(context))
                                           .append(SYMB_COMMA)
                                           .append(ReqSchemaUtil.getUseCaseType(context));
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubUseCaseRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getUseCaseValidationRelationship(context));
            }
            
            //IR-181257V6R2013x: Added maxLevels recursion level.
            documentationList = domObject.getRelatedObjects(context, sbRelSelect.toString(), sbTypeSelect.toString(),
            selectStmts, selectRelStmts, false, true, (short)maxLevels, null, null);
            
           
            //IR-181257V6R2013x: Added maxLevels recursion level.
            if (bRefDoc)
            {
                MapList mapTDocuments = domObject.getRelatedObjects(context, ReqSchemaUtil.getReferenceDocumentRelationship(context),
                DomainConstants.QUERY_WILDCARD, selectStmts, selectRelStmts, false, true, (short)maxLevels, null, null);
                if (!mapTDocuments.isEmpty())
                    documentationList.addAll(mapTDocuments);
            }
            
            //Start:OEP:10:08:2012:IR-181257V6R2013x
            documentationList.sortStructure(context, "attribute[Sequence Order]", "", "integer", "");
            RequirementsUtil.markLeafNodes(documentationList, maxLevels);
            //END:OEP:10:08:2012:IR-181257V6R2013x
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
        return(documentationList);
        
    }


    /** method to get the objects in fulfillment view of the Requirement Structure Browser
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - MapList (This maplist contains the objects to be displayed in the fulfillment view)
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public MapList expandFulfillmentObjects(Context context, String[] args)
        throws Exception
    {
        MapList requirementFeatureList=new MapList();
        try
        {
            boolean bReqTypeFlag=false;
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject = new DomainObject(strObjectId);

            String strType=domObject.getInfo(context,DomainConstants.SELECT_TYPE);
            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

            if (lstReqChildTypes.contains(strType) || ReqSchemaUtil.getRequirementType(context).equals(strType))
            {
                bReqTypeFlag=true;
            }

            //post type filter
            StringBuffer sbBuffer = new StringBuffer(100);
            sbBuffer.append(ReqSchemaUtil.getRequirementType(context));
            sbBuffer.append(SYMB_COMMA);
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(SYMB_COMMA);
                }
            }
            //object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);

            //relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

            //type filter
            StringBuffer stbTypeSelect = new StringBuffer(50);
            stbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
            stbTypeSelect.append(SYMB_COMMA);
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
                stbTypeSelect = stbTypeSelect.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                    stbTypeSelect = stbTypeSelect.append(SYMB_COMMA);
                }
            }
            //relationship filter
            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                       .append(SYMB_COMMA)
                                       .append(ReqSchemaUtil.getSubRequirementRelationship(context));

            requirementFeatureList=domObject.getRelatedObjects(context, stbRelSelect.toString(), stbTypeSelect.toString(),
                    false, true, (short)1, selectStmts, selectRelStmts, null, null, null, sbBuffer.toString(), null);

            //if req get features.
            if (bReqTypeFlag)
            {
                StringBuffer sbBufferFeature=new StringBuffer(100);
                List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getFeaturesType(context));
                for (int i=0; i < lstFeatureChildTypes.size(); i++)
                {
                    sbBufferFeature = sbBufferFeature.append(lstFeatureChildTypes.get(i));
                    if (i != lstFeatureChildTypes.size()-1)
                    {
                        sbBufferFeature = sbBufferFeature.append(SYMB_COMMA);
                    }
                }
                MapList tMaplist=new MapList();
                tMaplist=domObject.getRelatedObjects(context, ReqSchemaUtil.getRequirementSatisfiedByRelationship(context),
                        sbBufferFeature.toString(), selectStmts, selectRelStmts, true, false, (short)1, null, null);

                if (!tMaplist.isEmpty())
                    requirementFeatureList.addAll(tMaplist);
            }
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
        return(requirementFeatureList);
    }

    /**
     * This method is used to return the fulfillment status icon of an object
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List- the List of Strings in the form of 'Name Revision'
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public List getFulfillmentStatusIcon(Context context, String[] args)
        throws Exception
    {
        List lstNameRev = new StringList();
        try
        {
            //unpack the arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            List lstobjectList = (MapList) programMap.get("objectList");
            Map map=(Map)programMap.get("paramList");
            String strRootObjId=(String)map.get("parentOID");
            Iterator objectListItr = lstobjectList.iterator();

            //initialise the local variables
            Map objectMap = new HashMap();
            String strObjId = DomainConstants.EMPTY_STRING;
            String strObjState = DomainConstants.EMPTY_STRING;
            String strIcon = DomainConstants.EMPTY_STRING;
            String strObjType = DomainConstants.EMPTY_STRING;
            String strAppendString = DomainConstants.EMPTY_STRING;
            String strLabelString = DomainConstants.EMPTY_STRING;

            StringBuffer sbStatePolicyKey = new StringBuffer();
            boolean flag = false;
            StringBuffer stbNameRev = new StringBuffer(100);
            DomainObject domObj = null;
            StringBuffer sbBufferType=new StringBuffer(100);
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getFeaturesType(context));
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBufferType = sbBufferType.append(lstFeatureChildTypes.get(i));
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBufferType = sbBufferType.append(SYMB_COMMA);
                }
            }

            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));
            //loop through all the records
            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();
                strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
                domObj = DomainObject.newInstance(context, strObjId);
                strObjState = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
                strObjType = domObj.getInfo(context, DomainConstants.SELECT_TYPE);
                //If Feature
                if (lstFeatureChildTypes.contains(strObjType))
                {
                    if (checkFeatureValidity(context,strRootObjId,strObjId))
                    {
                        if (strObjState.equals(ProductLineConstants.STATE_RELEASE))
                        {
                            strAppendString=SYMB_FULFILLED;
                            //Mar 13, 2006 - Modified for bug 316691 by Enovia MatrixOne
                            strLabelString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_OBJECT_FULFILLED);
                            //End of Modify
                        }
                        else
                        {
                            strAppendString=SYMB_NOT_FULFILLED;
                            strLabelString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_INVALID_OBJECT);
                        }
                    }
                    else
                    {
                        strAppendString=SYMB_DONT_CARE;
                        strLabelString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_INVALID_FEATURE);
                    }
                }
                else if (lstReqChildTypes.contains(strObjType) || ReqSchemaUtil.getRequirementType(context).equals(strObjType))
                {
                    if (checkRequirementValidity(context,strObjId))
                    {
                        if (strObjState.equals(ProductLineConstants.STATE_RELEASE))
                        {
                            strAppendString=SYMB_FULFILLED;
                            //Mar 13, 2006 - Modified for bug 316691 by Enovia MatrixOne
                            strLabelString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_OBJECT_FULFILLED);
                            //End of Modify
                        }
                        else
                        {
                            strAppendString=SYMB_NOT_FULFILLED;
                            strLabelString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_INVALID_OBJECT);
                        }
                    }
                    else
                    {
                        strAppendString=SYMB_NOT_FULFILLED;
                        strLabelString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_INVALID_REQUIREMENT);

                    }
                }
                // Forming the key which is to be looked up
                sbStatePolicyKey = new StringBuffer("emxRequirements.RSBFulfillment.");
                sbStatePolicyKey.append(strAppendString);
                try
                {
                    strIcon = EnoviaResourceBundle.getProperty(context, sbStatePolicyKey.toString());
                    flag = true;
                }
                catch(Exception ex)
                {
                    flag = false;
                }
                if (flag)
                {
                    stbNameRev.delete(0, stbNameRev.length());
                    stbNameRev = stbNameRev.append("<img src=\"../common/images/")
                                           .append(strIcon)
                                           .append("\" border=\"0\"  align=\"middle\" ")
                                           .append("TITLE=\"")
                                           .append(" ")
                                           .append(strLabelString)
                                           .append("\"")
                                           .append("/>");
                    lstNameRev.add(stbNameRev.toString());
                }
                else
                {
                    lstNameRev.add(DomainConstants.EMPTY_STRING);
                }
            }
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }

        return(lstNameRev);
        
    }


    /**
     * This method is used to get all the fulfilled objects(Requirements and Features)
     * under product context
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return MapList
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getFulfilledObjects(Context context, String[] args)
        throws Exception
    {
        MapList mapListFulfilledObjects = new MapList();
        MapList mapRequirements=new MapList();
        try
        {
            //unpack the arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            String strObjectID=(String)programMap.get(SYMB_OBJECT_ID);
            DomainObject domObject=DomainObject.newInstance(context,strObjectID);
            //type selects
            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

            StringBuffer sbBuffer = new StringBuffer(100);
            sbBuffer.append(ReqSchemaUtil.getRequirementType(context));
            sbBuffer.append(SYMB_COMMA);
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(SYMB_COMMA);
                }
            }

            //get sub-types of Features
            StringBuffer sbBufferFeature=new StringBuffer(100);
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getFeaturesType(context));
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBufferFeature = sbBufferFeature.append(lstFeatureChildTypes.get(i));
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBufferFeature = sbBufferFeature.append(SYMB_COMMA);
                }
            }
            //object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            //rel Select
            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                       .append(SYMB_COMMA)
                                       .append(ReqSchemaUtil.getSubRequirementRelationship(context));
            mapRequirements=domObject.getRelatedObjects(context, stbRelSelect.toString(), sbBuffer.toString(),
                     selectStmts, null, false, true, (short)0, null, null);

            int iMapSize=mapRequirements.size();
            String strLevel="";
            String strObjId="";
            MapList mapFeatures=new MapList();
            for(int i=0;i<iMapSize;i++)
            {
                Map mapT=(Map)mapRequirements.get(i);
                strLevel=(String)mapT.get(DomainConstants.KEY_LEVEL);
                strObjId=(String)mapT.get(DomainConstants.SELECT_ID);
                DomainObject domInter=DomainObject.newInstance(context,strObjId);
                String strObjState=domInter.getInfo(context, DomainConstants.SELECT_CURRENT);
                //if requirement status green then put in final list
                if (checkRequirementValidity(context,strObjId))
                {
                    if (strObjState.equals(ProductLineConstants.STATE_RELEASE))
                        mapListFulfilledObjects.add(mapT);
                }

                int iValue = Integer.parseInt(strLevel)+1;
                String strVal = Integer.toString(iValue);
                mapFeatures = domInter.getRelatedObjects(context, ReqSchemaUtil.getRequirementSatisfiedByRelationship(context),
                        sbBufferFeature.toString(), selectStmts, null, true, false, (short)1, null, null);
                for(int j=0;j<mapFeatures.size();j++)
                {
                    Map mapTF=(Map)mapFeatures.get(j);
                    String strFId=(String)mapTF.get(DomainConstants.SELECT_ID);
                    DomainObject domF=DomainObject.newInstance(context,strFId);
                    String strFState=domF.getInfo(context,DomainConstants.SELECT_CURRENT);
                    if (checkFeatureValidity(context,strObjectID,strFId) && strFState.equals(ProductLineConstants.STATE_RELEASE))
                    {
                        mapTF.remove(DomainConstants.KEY_LEVEL);
                        mapTF.put(DomainConstants.KEY_LEVEL,strVal);
                        mapListFulfilledObjects.add(mapTF);
                    }
                }
            }

        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }

        return(mapListFulfilledObjects);
        
    }

    /**
     * This method is used to get all the not fulfilled objects(Requirements and Features)
     * under product context
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return MapList
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getNotFulfilledObjects(Context context, String[] args)
        throws Exception
    {
        MapList mapListNotFulfilledObjects = new MapList();
        MapList mapRequirements=new MapList();
        try
        {
            //unpack the arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            String strObjectID=(String)programMap.get(SYMB_OBJECT_ID);
            DomainObject domObject=DomainObject.newInstance(context,strObjectID);
            //type selects
            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

            StringBuffer sbBuffer = new StringBuffer(100);
            sbBuffer.append(ReqSchemaUtil.getRequirementType(context));
            sbBuffer.append(SYMB_COMMA);
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(SYMB_COMMA);
                }
            }

            //Sub types of features
            StringBuffer sbBufferFeature=new StringBuffer(100);
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getFeaturesType(context));
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBufferFeature = sbBufferFeature.append(lstFeatureChildTypes.get(i));
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBufferFeature = sbBufferFeature.append(SYMB_COMMA);
                }
            }
            //object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            //rel Select
            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                       .append(SYMB_COMMA)
                                       .append(ReqSchemaUtil.getSubRequirementRelationship(context));
            mapRequirements = domObject.getRelatedObjects(context, stbRelSelect.toString(), sbBuffer.toString(),
                    selectStmts, null, false, true, (short)0, null, null);
            int iMapSize=mapRequirements.size();
            String strLevel="";
            String strObjId="";
            MapList mapFeatures=new MapList();
            for(int i=0;i<iMapSize;i++)
            {
                Map mapT=(Map)mapRequirements.get(i);
                strLevel=(String)mapT.get(DomainConstants.KEY_LEVEL);
                strObjId=(String)mapT.get(DomainConstants.SELECT_ID);
                DomainObject domInter=DomainObject.newInstance(context,strObjId);
                String strObjState=domInter.getInfo(context,DomainConstants.SELECT_CURRENT);
                //If req status not green then put in final list.
                if (!checkRequirementValidity(context,strObjId) || !strObjState.equals(ProductLineConstants.STATE_RELEASE))
                {
                    mapListNotFulfilledObjects.add(mapT);
                }
                int iValue=Integer.parseInt(strLevel)+1;
                String strVal=Integer.toString(iValue);
                mapFeatures=domInter.getRelatedObjects(context, ReqSchemaUtil.getRequirementSatisfiedByRelationship(context),
                        sbBufferFeature.toString(), selectStmts, null, true, false, (short)1, null, null);
                for(int j=0;j<mapFeatures.size();j++)
                {
                    Map mapTF=(Map)mapFeatures.get(j);
                    String strFId=(String)mapTF.get(DomainConstants.SELECT_ID);
                    DomainObject domF=DomainObject.newInstance(context,strFId);
                    String strFState=domF.getInfo(context,DomainConstants.SELECT_CURRENT);
                    if (!checkFeatureValidity(context,strObjectID,strFId) || !strFState.equals(ProductLineConstants.STATE_RELEASE))
                    {
                        mapTF.remove(DomainConstants.KEY_LEVEL);
                        mapTF.put(DomainConstants.KEY_LEVEL,strVal);
                        mapListNotFulfilledObjects.add(mapTF);
                    }
                }
            }
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
        return(mapListNotFulfilledObjects);
        
    }

    /**
     * This method is used check the fulfillment status of the requirement
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param requirementId requirement object Id
     * @return boolean
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public boolean checkRequirementValidity(Context context,String requirementId)
        throws Exception
    {
        boolean flag=false;
        try
        {
            DomainObject domObj = DomainObject.newInstance(context, requirementId);
            StringBuffer sbBufferFeature=new StringBuffer(100);
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getFeaturesType(context));
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBufferFeature = sbBufferFeature.append(lstFeatureChildTypes.get(i));
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBufferFeature = sbBufferFeature.append(SYMB_COMMA);
                }
            }

            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));
            StringBuffer sbBuffer = new StringBuffer(100);
            sbBuffer.append(ReqSchemaUtil.getRequirementType(context));
            sbBuffer.append(SYMB_COMMA);
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(SYMB_COMMA);
                }
            }

            MapList tMList = domObj.getRelatedObjects(context, ReqSchemaUtil.getSubRequirementRelationship(context),
                    sbBuffer.toString(), new StringList(DomainConstants.SELECT_ID), null, false, true, (short)1, null, null);

            if (tMList.isEmpty())
            {
                tMList=domObj.getRelatedObjects(context, ReqSchemaUtil.getRequirementSatisfiedByRelationship(context),
                        sbBufferFeature.toString(), new StringList(DomainConstants.SELECT_ID), null, true, false, (short)1, null, null);

                if (!tMList.isEmpty())
                    flag=true;
            }
            else
            {
                flag=true;
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        return(flag);
    
    }

    /**
     * This method is used check the fulfillment status of the feature
     * @param context the eMatrix <code>Context</code> object
     * @param productId the product objectId
     * @param featureId the feature objectId
     * @return boolean
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public boolean checkFeatureValidity(Context context,String productId,String featureId)
        throws Exception
    {
        boolean flag=false;
        try
        {
            StringBuffer sbBufferProduct=new StringBuffer(100);
            List lstProductChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getProductsType(context));
            for (int i=0; i < lstProductChildTypes.size(); i++)
            {
                sbBufferProduct = sbBufferProduct.append(lstProductChildTypes.get(i));
                if (i != lstProductChildTypes.size()-1)
                {
                    sbBufferProduct = sbBufferProduct.append(SYMB_COMMA);
                }
            }
            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ReqSchemaUtil.getFeatureListFromRelationship(context))
                                       .append(SYMB_COMMA)
                                       .append(ReqSchemaUtil.getFeatureListToRelationship(context));
            StringBuffer sbBufferType=new StringBuffer(100);
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getFeaturesType(context));
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBufferType = sbBufferType.append(lstFeatureChildTypes.get(i));
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBufferType = sbBufferType.append(SYMB_COMMA);
                }
            }
            sbBufferType.append(SYMB_COMMA)
                        .append(ReqSchemaUtil.getFeatureListType(context))
                        .append(SYMB_COMMA)
                        .append(sbBufferProduct.toString());
            StringList selectStmts = new StringList(1);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            DomainObject domObj=DomainObject.newInstance(context,featureId);
            MapList tMaplist = domObj.getRelatedObjects(context, stbRelSelect.toString(), sbBufferType.toString(),
                    true, false, (short)0, selectStmts, null, null, null, null, sbBufferProduct.toString(), null);

            List lstOnlyProductIds=new MapList();
            int intProductCount = tMaplist.size();
            if (intProductCount > 0)
            {
                for(int j=0;j<intProductCount;j++)
                {
                    Map tempMap=(Map)tMaplist.get(j);
                    String strId=(String)tempMap.get(DomainConstants.SELECT_ID);
                    lstOnlyProductIds.add(strId);
                }
            }
            if (lstOnlyProductIds.contains(productId))
            {
                flag=true;
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        
        return(flag);
        
    }

    /**
     * This method is used to get the Remarks for the fulfillment report
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public List getRemarks(Context context,String[] args)
        throws Exception
    {
        List lstNameRev = new StringList();
        try
        {
            //unpack the arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            List lstobjectList = (MapList) programMap.get("objectList");
            Map map=(Map)programMap.get("paramList");
            String strRootObjId=(String)map.get("parentOID");
            Iterator objectListItr = lstobjectList.iterator();

            //initialise the local variables
            Map objectMap = new HashMap();
            String strObjId = DomainConstants.EMPTY_STRING;
            String strObjState = DomainConstants.EMPTY_STRING;
            String strObjType = DomainConstants.EMPTY_STRING;
            String strRemarkString = DomainConstants.EMPTY_STRING;

            DomainObject domObj = null;
            StringBuffer sbBufferType=new StringBuffer(100);
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getFeaturesType(context));
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBufferType = sbBufferType.append(lstFeatureChildTypes.get(i));
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBufferType = sbBufferType.append(SYMB_COMMA);
                }
            }
            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));
            //loop through all the records
            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();
                strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
                domObj = DomainObject.newInstance(context, strObjId);
                strObjState = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
                strObjType = domObj.getInfo(context, DomainConstants.SELECT_TYPE);
                //If Feature
                if (lstFeatureChildTypes.contains(strObjType))
                {
                    if (checkFeatureValidity(context,strRootObjId,strObjId))
                    {
                        if (strObjState.equals(ProductLineConstants.STATE_RELEASE))
                        {
                            strRemarkString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_OBJECT_FULFILLED);
                            //"Object in Release State";
                        }
                        else
                        {
                            strRemarkString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_INVALID_OBJECT);
                            //"Object not in Release State";
                        }
                    }
                    else
                    {
                        strRemarkString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_INVALID_FEATURE);
                        //"Feature not connected to the context Product";
                    }
                }
                else if (lstReqChildTypes.contains(strObjType) || ReqSchemaUtil.getRequirementType(context).equals(strObjType))
                {
                    if (checkRequirementValidity(context,strObjId))
                    {
                        if (strObjState.equals(ProductLineConstants.STATE_RELEASE))
                            strRemarkString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_OBJECT_FULFILLED);
                        else
                            strRemarkString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_INVALID_OBJECT);
                    }
                    else
                    {
                        strRemarkString=EnoviaResourceBundle.getProperty(context,"emxRequirementsStringResource",context.getLocale(),SYMB_INVALID_REQUIREMENT);
                        //"No Feature under Requirement";
                    }
                }
                lstNameRev.add(strRemarkString);
            }
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
        return(lstNameRev);
    }

    /**
     * This method is used to get the Level for the fulfillment report
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public List getLevel(Context context,String[] args)
        throws Exception
    {
        Vector returnList=new Vector();
        try
        {
            // program arguments
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList mapObjList=getAllObjects(context,args);
            MapList objList = (MapList)programMap.get("objectList");
            List lstObjIdList=new ArrayList();
            List lstAllObjIdList=new ArrayList();

            for(int k=0;k<objList.size();k++)
            {
                Map map=(Map)objList.get(k);
                String strID=(String)map.get(DomainConstants.SELECT_ID);
                lstObjIdList.add(strID);
            }
            for(int k=0;k<mapObjList.size();k++)
            {
                Map map=(Map)mapObjList.get(k);
                String strID=(String)map.get(DomainConstants.SELECT_ID);
                lstAllObjIdList.add(strID);
            }

            ArrayList levelIndex = new ArrayList();
            int  previous = 0;
            Iterator itr = mapObjList.iterator();
            while (itr.hasNext())
            {
                Map map = (Map) itr.next();
                String sLevel = (String)map.get(DomainConstants.KEY_LEVEL);
                int level = Integer.valueOf(sLevel).intValue() -1;
                String wbs = "";
                String count = "1";
                int size = levelIndex.size();
                if (level != 0)
                {
                    //used to determine the wbs prefix for the current item
                    Map wbsMap1 = (Map) levelIndex.get(level-1);
                    wbs = (String) wbsMap1.get("wbs");
                    wbs = wbs + ".";
                }
                //used to determine the suffix or the count at the current level
                Map wbsMap2 = null;
                if (size > level)
                {
                    wbsMap2 = (Map) levelIndex.get(level);
                    count = (String) wbsMap2.get("count");
                    //incr count for next sequence number
                    int sequence = Integer.parseInt(count);
                    count = String.valueOf(++sequence);
                   if (level > previous ) count = "1" ;
                }
                else
                {
                   wbsMap2 = new HashMap();
                }

                wbs = wbs + count;
                previous = level;
                map.put("wbs", wbs);
                wbsMap2.put("wbs", wbs);
                wbsMap2.put("count", count);
                if (size > level)
                {
                    levelIndex.set(level, wbsMap2);
                }
                else
                {
                    levelIndex.add(wbsMap2);
                }
                String wbs2 = wbs;
                String sHREF = "";
                if (level == 0)
                {
                    wbs2 = wbs + ".0";
                }
                int i = level;
                while (i > 0 ) {
                    //Mar 13, 2006 - Commented by Enovia MatrixOne for Bug 316672
                    //sHREF += "&nbsp;&nbsp;";
                    //end of comment
                    i--;
                }
                wbs2 = sHREF + wbs2;
                returnList.add(wbs2);
            }
            int iSizeOfObjList=lstAllObjIdList.size();
            int j=0;
            for(int i=0;i<iSizeOfObjList;i++)
            {
                if (lstObjIdList.contains(lstAllObjIdList.get(i)))
                {
                    j++;
                }
                else
                {
                    returnList.remove(j);
                }
            }
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
        return(returnList);
        
    }

    /**
     * This method is used to get all the objects(Requirements and Features) connected to the Product
     * at all levels
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return MapList
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public MapList getAllObjects(Context context, String[] args)
        throws Exception
    {
        MapList mapListAllObjects = new MapList();
        MapList mapRequirements=new MapList();
        try
        {
            //unpack the arguments
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap= (HashMap)programMap.get("paramList");
            String strObjectID=(String)paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject=DomainObject.newInstance(context,strObjectID);
            //type selects
            List lstReqChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

            StringBuffer sbBuffer = new StringBuffer(100);
            sbBuffer.append(ReqSchemaUtil.getRequirementType(context));
            sbBuffer.append(SYMB_COMMA);
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(SYMB_COMMA);
                }
            }
            //Sub types of features
            StringBuffer sbBufferFeature=new StringBuffer(100);
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getFeaturesType(context));
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBufferFeature = sbBufferFeature.append(lstFeatureChildTypes.get(i));
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBufferFeature = sbBufferFeature.append(SYMB_COMMA);
                }
            }
            //object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            //rel Select
            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ReqSchemaUtil.getProductRequirementRelationship(context))
                                       .append(SYMB_COMMA)
                                       .append(ReqSchemaUtil.getSubRequirementRelationship(context));
            mapRequirements=domObject.getRelatedObjects(context, stbRelSelect.toString(), sbBuffer.toString(),
                    selectStmts, null, false, true, (short)0, null, null);
            int iMapSize=mapRequirements.size();
            String strLevel="";
            String strObjId="";
            MapList mapFeatures=new MapList();
            for(int i=0;i<iMapSize;i++)
            {
                Map mapT=(Map)mapRequirements.get(i);
                strLevel=(String)mapT.get(DomainConstants.KEY_LEVEL);
                strObjId=(String)mapT.get(DomainConstants.SELECT_ID);
                DomainObject domInter=DomainObject.newInstance(context,strObjId);

                //If req status not green then put in final list.
                mapListAllObjects.add(mapT);
                int iValue=Integer.parseInt(strLevel)+1;
                String strVal=Integer.toString(iValue);
                mapFeatures=domInter.getRelatedObjects(context, ReqSchemaUtil.getRequirementSatisfiedByRelationship(context),
                        sbBufferFeature.toString(), selectStmts, null, true, false, (short)1, null, null);
                for(int j=0;j<mapFeatures.size();j++)
                {
                    Map mapTF=(Map)mapFeatures.get(j);
                    mapTF.remove(DomainConstants.KEY_LEVEL);
                    mapTF.put(DomainConstants.KEY_LEVEL,strVal);
                    mapListAllObjects.add(mapTF);
                }
            }
        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }

        return(mapListAllObjects);
        
    }


    /**
     * Get whether the business object is locked or not.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object of type Vector
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6SP2
     */
    public Vector getLockedStatus(Context context, String[] args)
        throws Exception
    {
        Vector showLock= new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map objectMap = null;
            Map paramList = (Map)programMap.get("paramList");
            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null)
            {
               isprinterFriendly = true;
            }

            StringBuffer baseURLBuf = new StringBuffer(256);
            baseURLBuf.append("emxTable.jsp?program=emxCommonFileUI:getFiles&amp;table=APPFileSummary&amp;sortColumnName=Name&amp;");
            baseURLBuf.append("popup=true&amp;sortDirection=ascending&amp;popup=true&amp;header=emxComponents.Menu.Files&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;");
            baseURLBuf.append("HelpMarker=emxhelpcommondocuments&amp;suiteKey=Components&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1");
            StringBuffer nonVersionableBaseURLBuf = new StringBuffer(256);
            nonVersionableBaseURLBuf.append("emxTable.jsp?program=emxCommonFileUI:getNonVersionableFiles&amp;table=APPNonVersionableFileSummary&amp;sortColumnName=Name&amp;");
            nonVersionableBaseURLBuf.append("popup=true&amp;sortDirection=ascending&amp;popup=true&amp;header=emxComponents.Menu.Files&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;");
            nonVersionableBaseURLBuf.append("HelpMarker=emxhelpcommondocuments&amp;suiteKey=Components&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1");

            StringList files = new StringList();
            StringList locked = new StringList();
            String  objectType = "";
            String parentType = "";
            String lock = "";
            int lockCount = 0;
            int fileCount = 0;
            String objectId = "";
            String file ="";

            //Added for VC Document Checking
            String vcInterface="";
            boolean vcDocument=false;
            StringBuffer urlBuf = new StringBuffer(256);
            boolean moveFilesToVersion = false;
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               }
               catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }
            StringList selects = new StringList(10);
            selects.add(CommonDocument.SELECT_TYPE);
            selects.add(CommonDocument.SELECT_ID);
            selects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            selects.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            selects.add(CommonDocument.SELECT_FILE_NAME);
            selects.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
            selects.add(CommonDocument.SELECT_LOCKED);
            selects.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);

            MapList mlist = DomainObject.getInfo(context, oidsArray, selects);
            Iterator mItr = mlist.iterator();
            while( mItr.hasNext() )
            {
                urlBuf = new StringBuffer(256);
                lockCount = 0;
                fileCount = 0;
                files = new StringList();
                locked = new StringList();
                objectMap = (Map)mItr.next();
                //Added for VC Lock Status
                vcInterface = (String)objectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
                vcDocument = "TRUE".equalsIgnoreCase(vcInterface)?true:false;
                if (vcDocument)
                {
                  showLock.add("--");
                  continue;
                }
                objectId = (String) objectMap.get(CommonDocument.SELECT_ID);
                objectType = (String) objectMap.get(CommonDocument.SELECT_TYPE);
                moveFilesToVersion = (Boolean.valueOf((String) objectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
                parentType = CommonDocument.getParentType(context, objectType);

                boolean isVersionableType = CommonDocument.checkVersionableType(context, objectType);
                if ( parentType.equals(ReqSchemaUtil.getDocumentsType(context)) )
                {
                    if ( moveFilesToVersion )
                    {
                        try
                        {
                            files = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
                        }
                        catch(ClassCastException cex )
                        {
                            files.add((String)objectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION));
                        }
                    }
                    else
                    {
                        try
                        {
                            files = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAME);
                        }
                        catch(ClassCastException cex )
                        {
                            files.add((String)objectMap.get(CommonDocument.SELECT_FILE_NAME));
                        }
                    }
                    if (files != null)
                    {
                        fileCount = files.size();
                        if ( fileCount == 1 )
                        {
                            file = (String)files.get(0);
                            if ( file == null || "".equals(file) || "null".equals(file) )
                            {
                                fileCount = 0;
                            }
                        }
                    }
                    try
                    {
                        locked = (StringList)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
                    }
                    catch(ClassCastException cex)
                    {
                        locked.add((String)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED));
                    }
                    if (locked != null)
                    {
                        Iterator itr = locked.iterator();
                        while (itr.hasNext())
                        {
                            lock = (String)itr.next();
                            if (lock.equalsIgnoreCase("true"))
                                lockCount ++;
                        }
                    }
                    if (!isVersionableType)
                    {
                        lock = (String)objectMap.get(CommonDocument.SELECT_LOCKED);
                        if (lock.equalsIgnoreCase("true"))
                            lockCount = fileCount;
                    }
                    if (!isprinterFriendly)
                    {
                        urlBuf.append("<a href =\"javascript:emxTableColumnLinkClick('");
                        if ( !isVersionableType )
                            urlBuf.append(nonVersionableBaseURLBuf.toString());
                        else
                            urlBuf.append(baseURLBuf.toString());

                        urlBuf.append("&amp;objectId=");
                        urlBuf.append(objectId);
                        urlBuf.append("','730','450','true','popup')\">");
                    }
                    urlBuf.append(lockCount + "/" + fileCount);
                    if (!isprinterFriendly)
                    {
                        urlBuf.append("</a>");
                    }
                    showLock.add(urlBuf.toString());
                }
                else
                {
                    showLock.add("");
                }
            }
            return(showLock);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     *  Get Vector of Strings for Document Action Icons
     *
     *  @param context the eMatrix <code>Context</code> object
     *  @param args an array of String arguments for this method
     *  @return Vector object that contains a vector of html code to
     *        construct the Actions Column.
     *  @throws Exception if the operation fails
     *
     *  @since Common 10-6SP2
     */
    public static Vector getDocumentActions(Context context, String[] args)
        throws Exception
    {
        Vector vActions = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList      = (Map)programMap.get("paramList");
            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null)
            {
                isprinterFriendly = true;
            }
            String languageStr = (String)paramList.get("languageStr");
            StringBuffer strActionURL = null;
            String objectId    = null;
            Map objectMap      = null;

            String sTipDownload = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.DocumentSummary.ToolTipDownload");
            String sTipCheckout = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.DocumentSummary.ToolTipCheckout"); 
            String sTipCheckin  = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.DocumentSummary.ToolTipCheckin");
            String sTipUpdate   = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.DocumentSummary.ToolTipUpdate");
            String sTipSubscriptions   = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.Command.Subscriptions");

            String objectType = "";
            String isVersionable = "true";
            StringList files = new StringList();
            String file ="";
            int fileCount = 0;
            StringList locked = new StringList();
            String lock ="";
            int lockCount = 0;
            boolean hasCheckoutAccess = true;
            boolean hasCheckinAccess = true;
            boolean moveFilesToVersion = false;
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               }
               catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }

            StringList selects = new StringList(10);
            selects.add(CommonDocument.SELECT_TYPE);
            selects.add(CommonDocument.SELECT_ID);
            selects.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            selects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            selects.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            selects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            selects.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            selects.add(CommonDocument.SELECT_FILE_NAME);
            selects.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
            selects.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
            selects.add("vcfile");

            MapList mlist = DomainObject.getInfo(context, oidsArray, selects);
            Iterator mItr = mlist.iterator();
            while( mItr.hasNext() )
            {
                files = new StringList();
                file = "";
                fileCount = 0;
                locked = new StringList();
                lock = "";
                lockCount = 0;

                objectMap = (Map) mItr.next();
                objectId = (String)objectMap.get(CommonDocument.SELECT_ID);
                objectType = (String) objectMap.get(CommonDocument.SELECT_TYPE);
                isVersionable = (String) objectMap.get(CommonDocument.SELECT_SUSPEND_VERSIONING);
                hasCheckoutAccess = (Boolean.valueOf((String) objectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS))).booleanValue();
                hasCheckinAccess = (Boolean.valueOf((String) objectMap.get(CommonDocument.SELECT_HAS_CHECKIN_ACCESS))).booleanValue();
                StringBuffer strBuf = new StringBuffer();
                String vcInterface = (String)objectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
                String vcfile = (String)objectMap.get("vcfile");
                boolean vcDocument = "TRUE".equalsIgnoreCase(vcInterface)? true: false;

                /* vcFileLock and vcFileLocker selects are taken out
                for performance reasons these will be done in
                PreCheckin and PreCheckout pages */
                boolean vcFileLock= false;
                String vcFileLocker= "";

                String parentType = CommonDocument.getParentType(context, objectType);
                moveFilesToVersion = (Boolean.valueOf((String) objectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
                if ( moveFilesToVersion )
                {
                    try
                    {
                        files = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
                    }
                    catch(ClassCastException cex )
                    {
                        files.add((String)objectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION));
                    }
                }
                else
                {
                    try
                    {
                        files = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAME);
                    }
                    catch(ClassCastException cex )
                    {
                        files.add((String)objectMap.get(CommonDocument.SELECT_FILE_NAME));
                    }
                }

                if ( files != null )
                {
                    fileCount = files.size();
                    if ( fileCount == 1 )
                    {
                        file = (String)files.get(0);
                        if ( file == null || "".equals(file) || "null".equals(file) )
                        {
                            fileCount = 0;
                        }
                    }
                }
                try
                {
                    locked = (StringList)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
                }
                catch(ClassCastException cex)
                {
                    locked.add((String)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED));
                }

                if ( locked != null )
                {
                    Iterator itr = locked.iterator();
                    while (itr.hasNext())
                    {
                        lock = (String)itr.next();
                        if (lock.equalsIgnoreCase("true"))
                            lockCount ++;
                    }
                }

                if (ReqSchemaUtil.getDocumentsType(context).equals(parentType) )
                {
                    if (!isprinterFriendly)
                    {
                        strActionURL = new StringBuffer("../components/emxCommonFS.jsp?functionality=DiscussionsSubscribe&amp;suiteKey=Components&amp;objectId=");
                        strActionURL.append(objectId);
                        strBuf.append("<a href=\"javascript:emxTableColumnLinkClick('"+strActionURL.toString()+"','730','450','true','popup')\">");
                        strBuf.append("<img border=\"0\" src=\"../common/images/iconSmallSubscription.gif\" alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"/></a> ");
                    }
                    else
                    {
                        strBuf.append("<img border=\"0\" src=\"../common/images/iconSmallSubscription.gif\" alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"/> ");
                    }
                    strActionURL = new StringBuffer("../components/emxCommonDocumentPreCheckout.jsp?objectId=");
                    if ((vcDocument || fileCount != 0) && hasCheckoutAccess)
                    {
                        // Show download, checkout for all type of files.
                        if (!isprinterFriendly)
                        {
                            strActionURL = new StringBuffer("../components/emxCommonDocumentPreCheckout.jsp?objectId=");
                            strActionURL.append(objectId);
                            strActionURL.append("&amp;action=download");
                            strBuf.append("<a href=\"javascript:emxTableColumnLinkClick('"+strActionURL.toString()+"','730','450','true','popup')\">");
                            strBuf.append("<img border=\"0\" src=\"../common/images/iconActionDownload.gif\" alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"/></a> ");
                        }
                        else
                        {
                            strBuf.append("<img border=\"0\" src=\"../common/images/iconActionDownload.gif\" alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"/> ");
                        }
                    }
                    //checkout w/lock
                    if ( "false".equalsIgnoreCase(isVersionable) )
                    {
                        if ( (( vcDocument && !vcFileLock) || (!vcDocument && fileCount != 0 && lockCount != fileCount)) && (hasCheckoutAccess && hasCheckinAccess ))
                        {
                            if (!isprinterFriendly)
                            {
                                strActionURL = new StringBuffer("../components/emxCommonDocumentPreCheckout.jsp?objectId=");
                                strActionURL.append(objectId);
                                strActionURL.append("&amp;action=checkout");
                                strBuf.append("<a href=\"javascript:emxTableColumnLinkClick('"+strActionURL.toString()+"','730','450','true','popup')\">");
                                strBuf.append("<img border=\"0\" src=\"../common/images/iconActionCheckOut.gif\" alt=\""+sTipCheckout+"\" title=\""+sTipCheckout+"\"/></a> ");
                            }
                            else
                            {
                                strBuf.append("<img border=\"0\" src=\"../common/images/iconActionCheckOut.gif\" alt=\""+sTipCheckout+"\" title=\""+sTipCheckout+"\"/>");
                            }
                        }
                        if (hasCheckinAccess)
                        {
                            //checkin
                            if ( !vcDocument )
                            {
                                if (!isprinterFriendly)
                                {
                                    strBuf.append("<a href=\"javascript:emxTableColumnLinkClick('../components/emxCommonDocumentPreCheckin.jsp?objectId="+objectId+"&amp;showComments=true&amp;showFormat=true&amp;objectAction=checkin','780','570','true','popup');\">");
                                    strBuf.append("<img border=\"0\" src=\"../common/images/iconActionAppend.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"/></a>");
                                }
                                else
                                {
                                    strBuf.append("<img border=\"0\" src=\"../common/images/iconActionAppend.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"/>");
                                }
                            }
                            //update
                            if ( lockCount > 0 &&  !vcDocument)
                            {
                                if (!isprinterFriendly)
                                {
                                    strBuf.append("<a href=\"javascript:emxTableColumnLinkClick('../components/emxCommonDocumentPreCheckin.jsp?objectId="+objectId+"&amp;showFormat=readonly&amp;showComments=required&amp;objectAction=update&amp;allowFileNameChange=true','730','450','true','popup');\">");
                                    strBuf.append("<img border=\"0\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipUpdate+"\" title=\""+sTipUpdate+"\"/></a> ");
                                }
                                else
                                {
                                    strBuf.append("<img border=\"0\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipUpdate+"\" title=\""+sTipUpdate+"\"/> ");
                                }
                            }
                            else if (((vcDocument && !vcFileLock) || (vcDocument && vcFileLock && vcFileLocker.equals(context.getUser()))))
                            {
                                if (!isprinterFriendly)
                                {
                                    if (vcfile.equalsIgnoreCase("true"))
                                        strBuf.append("<a href=\"javascript:emxTableColumnLinkClick('../components/emxCommonDocumentPreCheckin.jsp?objectId="+objectId+"&amp;showFormat=readonly&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=false&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate','730','450','true','popup');\">");
                                    else
                                        strBuf.append("<a href=\"javascript:emxTableColumnLinkClick('../components/emxCommonDocumentPreCheckin.jsp?objectId="+objectId+"&amp;showFormat=readonly&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=true&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate','730','450','true','popup');\">");

                                    strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipUpdate+"\" title=\""+sTipUpdate+"\"/></a>");
                                }
                                else
                                {
                                    strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipUpdate+"\" title=\""+sTipUpdate+"\"/>");
                                }
                            }
                        }
                    }
                }
                else
                {
                    strBuf.append("");
                }
                vActions.add(strBuf.toString());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return(vActions);
        
    }

    /**
     * getRevisionStatus- This method is used to show the revision status.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector Object 
     * @throws Exception if the operation fails
     * @since 10-6SP2
     */
    public Vector getRevisionStatus(Context context, String[] args)
        throws Exception
    {
        Vector showRev = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map objectMap = null;
            Map paramList = (Map)programMap.get("paramList");
            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null)
            {
               isprinterFriendly = true;
            }

            String objectType ="";
            String parentType ="";
            String objectId = "";
            StringBuffer baseURLBuf = new StringBuffer(250);
            baseURLBuf.append("emxTable.jsp?program=emxCommonDocumentUI:getRevisions&amp;popup=true&amp;table=APPDocumentRevisions&amp;header=emxComponents.Common.RevisionsPageHeading&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;suiteKey=Components");
            String revHref = "";
            StringBuffer urlBuf = new StringBuffer(250);
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               }
               catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }
            StringList selects = new StringList(3);
            selects.add(CommonDocument.SELECT_TYPE);
            selects.add(CommonDocument.SELECT_ID);
            selects.add(CommonDocument.SELECT_REVISION);
            MapList mlist = DomainObject.getInfo(context, oidsArray, selects);
            Iterator itr = mlist.iterator();
            while (itr.hasNext())
            {
                urlBuf = new StringBuffer(250);
                revHref = "";
                objectMap = (Map) itr.next();
                objectType = (String) objectMap.get(CommonDocument.SELECT_TYPE);
                objectId = (String) objectMap.get(CommonDocument.SELECT_ID);
                parentType = CommonDocument.getParentType(context, objectType);
                if ( parentType.equals(ReqSchemaUtil.getDocumentsType(context)) )
                {
                    revHref = (String)objectMap.get(CommonDocument.SELECT_REVISION);
                    if (!isprinterFriendly)
                    {
                        urlBuf.append("<a ");
                        //urlBuf.append(revHref);
                        urlBuf.append(" href =\"javascript:emxTableColumnLinkClick('");
                        urlBuf.append(baseURLBuf.toString());
                        urlBuf.append("&amp;objectId=");
                        urlBuf.append(objectId);
                        urlBuf.append("','730','450','true','popup')\">");
                    }
                    urlBuf.append(revHref);
                    if (!isprinterFriendly)
                    {
                        urlBuf.append("</a>");
                    }
                    revHref = urlBuf.toString();
                }
                else
                {
                    //Mar 13, 2006 - Modified for Bug 316688 by Enovia MatrixOne
                    revHref = "";
                    //(String)objectMap.get(CommonDocument.SELECT_REVISION);
                    //end of modify
                }
                showRev.add(revHref);
            }
            return(showRev);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * getVersionStatus- This method is used to show the version status.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector Object 
     * @throws Exception if the operation fails
     * @since 10-6SP2
     */
    public Vector getVersionStatus(Context context, String[] args)
        throws Exception
    {
        Vector showVer = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map objectMap = null;
            Map paramList = (Map)programMap.get("paramList");
            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null)
            {
               isprinterFriendly = true;
            }

            String languageStr = (String)paramList.get("languageStr");
            String sTipFileVersion = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.Common.Alt.FileVersions");
            String sTipVCFolder = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.Alt.VCFolder");
            String sTipVCFile = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.Alt.VCFile");

            String objectType ="";
            String parentType ="";
            StringList versions = new StringList();
            String version = "";
            String objectId = "";

            //Added for VC Document Checking
            String vcInterface="";
            boolean vcDocument=false;
            boolean vcFolder = false;

            StringBuffer baseURLBuf = new StringBuffer(250);
            baseURLBuf.append("emxTable.jsp?program=emxCommonFileUI:getFileVersions&amp;popup=true&amp;table=APPFileVersions&amp;header=emxComponents.Common.DocumentVersionsPageHeading&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;HelpMarker=emxhelpcommondocuments&amp;disableSorting=true&amp;suiteKey=Components&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1");
            StringBuffer urlBuf = new StringBuffer(250);
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               }
               catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }
            StringList selects = new StringList(5);
            selects.add(CommonDocument.SELECT_TYPE);
            selects.add(CommonDocument.SELECT_ID);
            selects.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION);
            selects.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
            selects.add(CommonDocument.SELECT_VCFOLDER);

            MapList mlist = DomainObject.getInfo(context, oidsArray, selects);
            Iterator itr = mlist.iterator();
            while( itr.hasNext() )
            {
                urlBuf = new StringBuffer(250);
                objectMap = (Map) itr.next();
                //Added for VC Lock Status
                vcInterface = (String)objectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
                vcDocument = "TRUE".equalsIgnoreCase(vcInterface)?true:false;
                vcFolder = "TRUE".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_VCFOLDER))?true:false;
                if (vcDocument)
                {
                    if (vcFolder)
                        version = "<img border='0' src='../common/images/iconSmallFolder.gif' alt=\"" + sTipVCFolder +"\" title=\"" + sTipVCFolder +"\" />";
                    else
                        version = "<img border='0' src='../common/images/iconSmallFile.gif' alt=\"" + sTipVCFile +"\" title=\"" + sTipVCFile +"\" />";

                    showVer.add(version);
                    continue;
                }

                objectType = (String) objectMap.get(CommonDocument.SELECT_TYPE);
                objectId = (String) objectMap.get(CommonDocument.SELECT_ID);
                parentType = CommonDocument.getParentType(context, objectType);

                if ( parentType.equals(ReqSchemaUtil.getDocumentsType(context)) )
                {
                    versions = (StringList)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION);
                    if ( versions == null || versions.size() == 0)
                    {
                        version = "";
                    }
                    else if ( versions.size() == 1 )
                    {
                        version = (String)versions.get(0);
                    }
                    else
                    {
                        version = "<img border=\"0\" src=\"../common/images/iconSmallFiles.gif\" alt=\"" + sTipFileVersion +"\" title=\"" + sTipFileVersion +"\"/>";
                    }
                    if (!isprinterFriendly && !"".equals(version) )
                    {
                        urlBuf.append("<a href =\"javascript:emxTableColumnLinkClick('");
                        urlBuf.append(baseURLBuf.toString());
                        urlBuf.append("&amp;objectId=");
                        urlBuf.append(objectId);
                        urlBuf.append("','730','450','true','popup')\">");
                    }
                    urlBuf.append(version);
                    if (!isprinterFriendly && !"".equals(version))
                    {
                        urlBuf.append("</a>");
                    }
                    showVer.add(urlBuf.toString());
                }
                else
                {
                    showVer.add("");
                }
            }
            return(showVer);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw(ex);
        }
    }


    /**
     * getDerivedRequirements- This method is used to show only those requirements that are linked by Derived Requirement relationship.
     *
     * @mx.whereUsed Invoked when the user selects 'Derived Requirements Only' expansion filter option present on the Requirement Structure Browser.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            holds the Hashmap containing the object id.
     * @return MapList Object of type MapList
     * @throws Exception
     *             if the operation fails
     * @since X3
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getDerivedRequirements(Context context, String[] args)throws Exception
    {
        MapList derivedRequirementsList=new MapList();
        try
        {
        	boolean toDirection = false;
        	boolean fromDirection = true;
            // unpack the arguments
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String expLevel = (String)paramMap.get("expandLevel");          
            if("All".equalsIgnoreCase(expLevel)){
            	  expLevel = "0";
            }
            if(expLevel == null || expLevel.length() == 0)
            {
          	  expLevel = "1";
            }
            int maxLevels = Integer.parseInt(expLevel);
            //START : LX6 IR-374998-3DEXPERIENCER2016x When refining requirements, Change of relationship status of requirement 
            //in drop down menu is not getting saved properly.
            String reportDirection  = (String)paramMap.get("reportDirection");
            if("to".equalsIgnoreCase(reportDirection)){
        		toDirection=true;
        		fromDirection = false;
        		
        	}else if("from".equalsIgnoreCase(reportDirection)){
        		toDirection=false;
        		fromDirection = true;
        	}else{
        		//NOP
        	}
            //END : LX6 IR-374998-3DEXPERIENCER2016x When refining requirements, Change of relationship status of requirement 
            //in drop down menu is not getting saved properly.
            // get the object id from the param map.
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject = new DomainObject(strObjectId);
            // get the type of the business object
            String strType = domObject.getInfo(context,DomainConstants.SELECT_TYPE);
            StringBuffer sbTypeSelect = new StringBuffer(60);
            StringBuffer sbRelSelect = new StringBuffer(60);

            // object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            // add attributes below to support rich text edtior
            selectStmts.addElement(DomainConstants.SELECT_TYPE);
            selectStmts.addElement(DomainConstants.SELECT_REVISION);
            selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
            selectStmts.addElement(DomainConstants.SELECT_MODIFIED);
            selectStmts.addElement(SELECT_RESERVED_BY);

            // relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            // add level to support rich text edtior
            selectRelStmts.addElement(DomainConstants.SELECT_LEVEL);
            selectRelStmts.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
			// ++ ZUD Fix For IR IR-234292  ++
            selectRelStmts.addElement(DomainRelationship.SELECT_FROM_ID);
			// -- ZUD Fix For IR IR-234292  --

            // calling 'getChildrenTypes' method of ProductLineUtil bean to get all the childs of type 'Requirement'
            List lstReqChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

            // construct the type list
            if (lstReqChildTypes.contains(strType) || ReqSchemaUtil.getRequirementType(context).equals(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i = 0; i < lstReqChildTypes.size(); i++)
                {
                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }

                // construct the relationship list
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getDerivedRequirementRelationship(context));

                // get all the related requirement object based on type & relationship list.
				String  effectivityFilter = (String) paramMap.get("CFFExpressionFilterInput_OID");
                //BPS regression, temp fix
                if("undefined".equalsIgnoreCase(effectivityFilter)){
               	 effectivityFilter = null;
                }
				if(effectivityFilter != null && effectivityFilter.length() != 0)
				{//performed configured expand.
					derivedRequirementsList = domObject.getRelatedObjects(context,
							sbRelSelect.toString(),     // relationship pattern
							sbTypeSelect.toString(),                     // object pattern
							selectStmts,                      // object selects
							selectRelStmts,                 // relationship selects
							toDirection,                               // to direction
							fromDirection,                               // from direction
						  (short) maxLevels,                 // recursion level
						  null,                              // object where clause
						  null,                              // relationship where clause
						  (short)0,                        	 // limit
						  CHECK_HIDDEN,            				 // check hidden
						  PREVENT_DUPLICATES,   			// prevent duplicates
						  PAGE_SIZE,                   		// pagesize
						  null,                              // includeType
						  null,                              // includeRelationship
						  null,                              // includeMap
						  null,                              // relKeyPrefix
						  effectivityFilter);            // Effectivity filter expression from the SB toolbar
				}
				else
				{
					derivedRequirementsList = domObject.getRelatedObjects(context, sbRelSelect.toString(), sbTypeSelect.toString(),
	                        selectStmts, selectRelStmts, toDirection, fromDirection, (short) maxLevels, null, null);
				}
                
                int objCount = derivedRequirementsList.size();
                String usrName = context.getUser();
                for (int jj = 0; jj < objCount; jj++)
                {
                   Map mapObject = (Map) derivedRequirementsList.get(jj);
                   String strObjId = (String) mapObject.get(SELECT_ID);
                   String objResBy = (String) mapObject.get(SELECT_RESERVED_BY);

                   // Check that the object is not reserved by another, and that it is in a "Modifyable" state...
                   Access objAccess = DomainObject.newInstance(context, strObjId).getAccessMask(context);
                   boolean objEditable = (("".equals(objResBy) || usrName.equals(objResBy)) && objAccess.hasModifyAccess());

                   // Mark the row as non-editable if someone else has this object reserved.
                   mapObject.put("RowEditable", (objEditable? "show": "readonly"));

                }
                
            
            }
            
            derivedRequirementsList.sortStructure(context, "attribute[Sequence Order]", "", "integer", "");
            
            RequirementsUtil.markLeafNodes(derivedRequirementsList, maxLevels);

            HashMap hmTemp = new HashMap();
        	hmTemp.put("expandMultiLevelsJPO","true");
        	derivedRequirementsList.add(hmTemp);
        }
        catch(Exception ex)
        {
            throw(new FrameworkException(ex.getMessage()));
        }
        return(derivedRequirementsList);

    }

    /**
     * getSubRequirements- This method is used to show only those requirements that are linked by Sub Requirement relationship.
     *
     * @mx.whereUsed Invoked when the user selects 'Sub Requirements Only' expansion filter option present on the Requirement Structure Browser.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            holds the Hashmap containing the object id.
     * @return Maplist type Object 
     * @throws Exception
     *             if the operation fails
     * @since X3
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getSubRequirements(Context context, String[] args)throws Exception
    {
        MapList subRequirementsList=new MapList();
        try
        {
            // unpack the arguments
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            String expLevel = (String)paramMap.get("expandLevel");
            if("All".equalsIgnoreCase(expLevel)){
            	  expLevel = "0";
            }
            if(expLevel == null || expLevel.length() == 0)
            {
          	  expLevel = "1";
            }
            int maxLevels = Integer.parseInt(expLevel);

            // get the object id from the param map.
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject = new DomainObject(strObjectId);
            // get the type of the business object
            String strType = domObject.getInfo(context,DomainConstants.SELECT_TYPE);
            StringBuffer sbTypeSelect = new StringBuffer(60);
            StringBuffer sbRelSelect = new StringBuffer(60);

            // object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            // add attributes below to support rich text editor
            selectStmts.addElement(DomainConstants.SELECT_TYPE);
            selectStmts.addElement(DomainConstants.SELECT_REVISION);
            selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
            selectStmts.addElement(DomainConstants.SELECT_MODIFIED);
            selectStmts.addElement(SELECT_RESERVED_BY);

            // relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            // add level to support rich text editor
            selectRelStmts.addElement(DomainConstants.SELECT_LEVEL);
            selectRelStmts.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
           	// ++ ZUD Fix For IR IR-234292  ++
            selectRelStmts.addElement(DomainRelationship.SELECT_FROM_ID);
			// -- ZUD Fix For IR IR-234292  -- 

            // calling 'getChildrenTypes' method of ProductLineUtil bean to get all the childs of type 'Requirement'
            List lstReqChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

            // construct the type list
            if (lstReqChildTypes.contains(strType) || ReqSchemaUtil.getRequirementType(context).equals(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i = 0; i < lstReqChildTypes.size(); i++)
                {
                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }

                // construct the relationship list
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubRequirementRelationship(context));

                // get all the related requirement object based on type & relationship list.
                String  effectivityFilter = (String) paramMap.get("CFFExpressionFilterInput_OID");
                //BPS regression, temp fix
                if("undefined".equalsIgnoreCase(effectivityFilter)){
               	 effectivityFilter = null;
                }
        	    if(effectivityFilter != null && effectivityFilter.length() != 0)
        	    {//performed configured expand.
        	    	subRequirementsList = domObject.getRelatedObjects(context,
        	    			sbRelSelect.toString(),     // relationship pattern
        	    			sbTypeSelect.toString(),                     // object pattern
        	    			selectStmts,                      // object selects
        	    			selectRelStmts,                 // relationship selects
						  false,                               // to direction
						  true,                               // from direction
						  (short) maxLevels,                 // recursion level
						  null,                              // object where clause
						  null,                              // relationship where clause
						  (short)0,                        	 // limit
						  CHECK_HIDDEN,            				 // check hidden
						  PREVENT_DUPLICATES,   			// prevent duplicates
						  PAGE_SIZE,                   		// pagesize
						  null,                              // includeType
						  null,                              // includeRelationship
						  null,                              // includeMap
						  null,                              // relKeyPrefix
						  effectivityFilter);            // Effectivity filter expression from the SB toolbar
        	    }
        	    else
        	    {//perform standard expand
                    subRequirementsList = domObject.getRelatedObjects(context, sbRelSelect.toString(), sbTypeSelect.toString(),
                            selectStmts, selectRelStmts, false, true, (short) maxLevels, null, null);
        	    	
        	    }



                int objCount = subRequirementsList.size();
                String usrName = context.getUser();
                for (int jj = 0; jj < objCount; jj++)
                {
                   Map mapObject = (Map) subRequirementsList.get(jj);
                   String strObjId = (String) mapObject.get(SELECT_ID);
                   String objResBy = (String) mapObject.get(SELECT_RESERVED_BY);

                   // Check that the object is not reserved by another, and that it is in a "Modifyable" state...
                   Access objAccess = DomainObject.newInstance(context, strObjId).getAccessMask(context);
                   boolean objEditable = (("".equals(objResBy) || usrName.equals(objResBy)) && objAccess.hasModifyAccess());

                   // Mark the row as non-editable if someone else has this object reserved.
                   mapObject.put("RowEditable", (objEditable? "show": "readonly"));

                }
            
            }

            subRequirementsList.sortStructure(context, "attribute[Sequence Order]", "", "integer", "");
            
            RequirementsUtil.markLeafNodes(subRequirementsList, maxLevels);

            HashMap hmTemp = new HashMap();
        	hmTemp.put("expandMultiLevelsJPO","true");
        	subRequirementsList.add(hmTemp);
        }
        catch(Exception ex)
        {
            throw(new FrameworkException(ex.getMessage()));
        }
        return(subRequirementsList);
        
    }


    /**
     * getSub_DerivedRequirements- This method is used to show those requirements that are linked by both Sub & Derived Requirement relationship.
     *
     * @mx.whereUsed Invoked when the user selects 'Sub and Derived Requirements' expansion filter option present on the Requirement Structure
     *               Browser.By Default this method is called in the expansion filter.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            holds the Hashmap containing the object id.
     * @return Maplist type of Object.
     * @throws Exception
     *             if the operation fails
     * @since X3
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getSub_DerivedRequirements(Context context, String[] args)throws Exception
    {
        MapList allRequirementsList = new MapList();
        try
        {
            // unpack the arguments
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            String expLevel = (String)paramMap.get("expandLevel");
            if("All".equalsIgnoreCase(expLevel)){
          	  expLevel = "0";
            }
            if(expLevel == null || expLevel.length() == 0)
            {
          	  expLevel = "1";
            }
            int maxLevels = Integer.parseInt(expLevel);

            // get the object id from the param map.
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject = new DomainObject(strObjectId);
            // get the type of the business object
            String strType = domObject.getInfo(context,DomainConstants.SELECT_TYPE);
            StringBuffer sbTypeSelect = new StringBuffer(60);
            StringBuffer sbRelSelect = new StringBuffer(60);

            // object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            // add attributes below to support rich text editor
            selectStmts.addElement(DomainConstants.SELECT_TYPE);
            selectStmts.addElement(DomainConstants.SELECT_REVISION);
            selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
            selectStmts.addElement(DomainConstants.SELECT_MODIFIED);
            selectStmts.addElement(SELECT_RESERVED_BY);

            // relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            //JX5
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
            //
            // add level to support rich text editor
            selectRelStmts.addElement(DomainConstants.SELECT_LEVEL);
            selectRelStmts.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
            selectRelStmts.addElement(DomainRelationship.SELECT_FROM_ID);
            

                        // calling 'getChildrenTypes' method of ProductLineUtil bean to get all the childs of type 'Requirement'
            List lstReqChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

            // construct the type list
            if (lstReqChildTypes.contains(strType) || ReqSchemaUtil.getRequirementType(context).equals(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i = 0; i < lstReqChildTypes.size(); i++)
                {

                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }

                // construct the relationship list
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getDerivedRequirementRelationship(context));

                // get all the related requirement object based on type & relationship list. 
        	    String  effectivityFilter = (String) paramMap.get("CFFExpressionFilterInput_OID");
                //BPS regression, temp fix
                if("undefined".equalsIgnoreCase(effectivityFilter)){
               	 effectivityFilter = null;
                }

                if(effectivityFilter != null && effectivityFilter.length() != 0)
        	    {//performed configured expand.
        	    	allRequirementsList = domObject.getRelatedObjects(context,
        	    			sbRelSelect.toString(),     // relationship pattern
        	    			sbTypeSelect.toString(),                     // object pattern
        	    			selectStmts,                      // object selects
        	    			selectRelStmts,                 // relationship selects
						  false,                               // to direction
						  true,                               // from direction
						  (short) maxLevels,                 // recursion level
						  null,                              // object where clause
						  null,                              // relationship where clause
						  (short)0,                        	 // limit
						  CHECK_HIDDEN,            				 // check hidden
						  PREVENT_DUPLICATES,   			// prevent duplicates
						  PAGE_SIZE,                   		// pagesize
						  null,                              // includeType
						  null,                              // includeRelationship
						  null,                              // includeMap
						  null,                              // relKeyPrefix
						  effectivityFilter);            // Effectivity filter expression from the SB toolbar
        	    }
        	    else
        	    {//perform standard expand.
        	    	allRequirementsList = domObject.getRelatedObjects(context, sbRelSelect.toString(), sbTypeSelect.toString(),
                            selectStmts, selectRelStmts, false, true, (short) maxLevels, null, null);
        	    }
                
                int objCount = allRequirementsList.size();
                String usrName = context.getUser();
                for (int jj = 0; jj < objCount; jj++)
                {
                   Map mapObject = (Map) allRequirementsList.get(jj);
                   String strObjId = (String) mapObject.get(SELECT_ID);
                   String objResBy = (String) mapObject.get(SELECT_RESERVED_BY);

                   // Check that the object is not reserved by another, and that it is in a "Modifyable" state...
                   Access objAccess = DomainObject.newInstance(context, strObjId).getAccessMask(context);
                   boolean objEditable = (("".equals(objResBy) || usrName.equals(objResBy)) && objAccess.hasModifyAccess());

                   // Mark the row as non-editable if someone else has this object reserved.
                   mapObject.put("RowEditable", (objEditable? "show": "readonly"));

                }
            
            }

            allRequirementsList.sortStructure(context, "relationship,attribute[Sequence Order]", ",", "string,integer", ",");
            
            RequirementsUtil.markLeafNodes(allRequirementsList, maxLevels);

            HashMap hmTemp = new HashMap();
        	hmTemp.put("expandMultiLevelsJPO","true");
        	allRequirementsList.add(hmTemp);
        }
        catch(Exception ex)
        {
            throw(new FrameworkException(ex.getMessage()));
        }

        return(allRequirementsList);
        
    }

    /**
     * getRequirementChildren- This method returns all child objects of a Requirement (e.g. Sub Requirements, Derived requirements, Parameters, Test Cases)
     *
     * @mx.whereUsed Invoked when the user selects 'Paremeter and TestCase' expansion filter option present on the Requirement Structure
     *               Browser.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            holds the Hashmap containing the object id.
     * @return Maplist type of Object.
     * @throws Exception
     *             if the operation fails
     * @since X3
     */
   
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRequirementChildren(Context context, String[] args)throws Exception
    {
        MapList allRequirementsList = new MapList();
        try
        {
            // unpack the arguments
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            String expLevel = (String)paramMap.get("expandLevel");
            if("All".equalsIgnoreCase(expLevel))
            {
          	  expLevel = "0";
            }
            if(expLevel == null || expLevel.length() == 0)
            {
          	  expLevel = "1";
            }
            int maxLevels = Integer.parseInt(expLevel);

            // get the object id from the param map.
            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            DomainObject domObject = new DomainObject(strObjectId);

            // get the type of the business object
            String strType = domObject.getInfo(context,DomainConstants.SELECT_TYPE);
            StringBuffer sbTypeSelect = new StringBuffer(60);
            StringBuffer sbRelSelect = new StringBuffer(60);

            // attributes to fetch, for selected object(s), from database
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);

            // add following attributes to support rich text editor
            selectStmts.addElement(DomainConstants.SELECT_TYPE);
            selectStmts.addElement(DomainConstants.SELECT_REVISION);
            selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
            selectStmts.addElement(DomainConstants.SELECT_MODIFIED);
            selectStmts.addElement(SELECT_RESERVED_BY);

            // relationships to fetch, for selected object(s), from database
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
         
            // added  to support rich text editor
            selectRelStmts.addElement(DomainConstants.SELECT_LEVEL);
            selectRelStmts.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
            selectRelStmts.addElement(DomainRelationship.SELECT_FROM_ID);
            

            // calling 'getChildrenTypes' method of ProductLineUtil bean to get all the childs of type 'Requirement'
            List lstReqChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

            //Adding Parameter
            lstReqChildTypes.add(ReqSchemaUtil.getParameterType(context));
            //Adding Test Case
			lstReqChildTypes.add(ReqSchemaUtil.getTestCaseType(context));
            //Code to be added

            // construct the type list
            if (lstReqChildTypes.contains(strType) || ReqSchemaUtil.getRequirementType(context).equals(strType))
            {
                sbTypeSelect.append(ReqSchemaUtil.getRequirementType(context));
                sbTypeSelect.append(SYMB_COMMA);
                for (int i = 0; i < lstReqChildTypes.size(); i++)
                {

                    sbTypeSelect = sbTypeSelect.append(lstReqChildTypes.get(i));
                    if (i != lstReqChildTypes.size()-1)
                    {
                        sbTypeSelect = sbTypeSelect.append(SYMB_COMMA);
                    }
                }

                // construct the relationship list
                sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getDerivedRequirementRelationship(context))
                                         .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getParameterAggregationRelationship(context))
                						 .append(SYMB_COMMA)
                                         .append(ReqSchemaUtil.getParameterUsageRelationship(context))
										 .append(SYMB_COMMA)
										 .append(ReqSchemaUtil.getRequirementValidationRelationship(context))
										 .append(SYMB_COMMA)
										 .append(ReqSchemaUtil.getSubTestCaseRelationship(context));

                // get all the related objects based on type & relationship list. 
        	      String  effectivityFilter = (String) paramMap.get("CFFExpressionFilterInput_OID");
                //BPS regression, temp fix
                if("undefined".equalsIgnoreCase(effectivityFilter))
                {
               	 effectivityFilter = null;
                }

                if(effectivityFilter != null && effectivityFilter.length() != 0)
        	      {
                  //performed configured expand.
        	      	allRequirementsList = domObject.getRelatedObjects(context,
        	    			sbRelSelect.toString(),     // relationship pattern
        	    			sbTypeSelect.toString(),                     // object pattern
        	    			selectStmts,                      // object selects
        	    			selectRelStmts,                 // relationship selects
					  	      false,                               // to direction
						        true,                               // from direction
						        (short) maxLevels,                 // recursion level
						        null,                              // object where clause
						        null,                              // relationship where clause
						        (short)0,                        	 // limit
						        CHECK_HIDDEN,            				 // check hidden
						        PREVENT_DUPLICATES,   			// prevent duplicates
						        PAGE_SIZE,                   		// pagesize
						        null,                              // includeType
						        null,                              // includeRelationship
						        null,                              // includeMap
						        null,                              // relKeyPrefix
						        effectivityFilter);            // Effectivity filter expression from the SB toolbar
        	      }
        	      else
        	      {
                  //perform standard expand.
        	    	  allRequirementsList = domObject.getRelatedObjects(context, sbRelSelect.toString(), sbTypeSelect.toString(),
                            selectStmts, selectRelStmts, false, true, (short) maxLevels, null, null);
        	      }
                
                int objCount = allRequirementsList.size();
                String usrName = context.getUser();
                for (int jj = 0; jj < objCount; jj++)
                {
                   Map mapObject = (Map) allRequirementsList.get(jj);
                   String strObjId = (String) mapObject.get(SELECT_ID);
                   String objResBy = (String) mapObject.get(SELECT_RESERVED_BY);

                   // Check that the object is not reserved by another, and that it is in a "Modifyable" state...
                   Access objAccess = DomainObject.newInstance(context, strObjId).getAccessMask(context);
                   boolean objEditable = (("".equals(objResBy) || usrName.equals(objResBy)) && objAccess.hasModifyAccess());

                   // Mark the row as non-editable if someone else has this object reserved.
                   mapObject.put("RowEditable", (objEditable? "show": "readonly"));

                }
            
            }

            allRequirementsList.sortStructure(context, "relationship,attribute[Sequence Order]", ",", "string,integer", ",");
            
            RequirementsUtil.markLeafNodes(allRequirementsList, maxLevels);

            HashMap hmTemp = new HashMap();
        	  hmTemp.put("expandMultiLevelsJPO","true");
        	  allRequirementsList.add(hmTemp);
        }
        catch(Exception ex)
        {
            throw(new FrameworkException(ex.getMessage()));
        }

        return(allRequirementsList);
        
    }
    /**
     * This method is used to return the Name and Revision of an object
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return List- the List of Strings in the form of 'Name Revision'
     * @throws Exception
     *             if the operation fails
     * @since RequirementManagement X3
     */
    public List getNameRev (Context context, String[] args) throws Exception
    {
        // unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) programMap.get(STR_OBJECT_LIST);
        Iterator objectListItr = lstobjectList.iterator();

        // initialise the local variables
        Map objectMap = new HashMap();
        String strObjId = DomainConstants.EMPTY_STRING;
        List lstNameRev = new StringList();
        StringBuffer stbNameRev = new StringBuffer(100);
        DomainObject domObj = null;
        // loop through all the records
        while(objectListItr.hasNext())
        {
            objectMap = (Map) objectListItr.next();
            strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
            domObj = DomainObject.newInstance(context, strObjId);
            stbNameRev.delete(0, stbNameRev.length());
            stbNameRev = stbNameRev.append(domObj.getInfo(context, DomainConstants.SELECT_NAME))
                                   .append(SYMB_SPACE )
                                   .append(domObj.getInfo(context, DomainConstants.SELECT_REVISION));
            lstNameRev.add(stbNameRev.toString());
        }

        return(lstNameRev);
    }


    /**
     * This method is used to return the list of an connected Specification object
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return List- the List of Specificaation Object names with hyperlink
     * @throws Exception
     *             if the operation fails
     * @since RequirementManagement X3
     */
    public List getRelatedSpecifications (Context context, String[] args) throws Exception
    {
        // unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        
        Map paramList = (Map)programMap.get("paramList");
       // boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));
 	    String strExportFormat = (String) paramList.get("exportFormat");
 	   String strReportFormat = (String)paramList.get("reportFormat");
 	   boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
 	   
 	   // Changes for IR-338259-3DEXPERIENCER2016
 	  boolean isExportFormat = "CSV".equalsIgnoreCase(strExportFormat);
 	  
        List lstobjectList = (MapList) programMap.get(STR_OBJECT_LIST);
        Iterator objectListItr = lstobjectList.iterator();
        Map objectMap = new HashMap();
        String strObjId = DomainConstants.EMPTY_STRING;
        List lstNameRev = new StringList();
        String typeIcon = "";
        String defaultTypeIcon = "";
        StringList derList = new StringList();
        StringList specList;
        StringBuffer sbTypeSelect=new StringBuffer(60);
        sbTypeSelect.append(ReqSchemaUtil.getSpecificationType(context));
        sbTypeSelect.append(SYMB_COMMA);
        sbTypeSelect.append(ReqSchemaUtil.getChapterType(context));

        DomainObject domObj = null;
        // mqlcommand retrieves all the derivatives of type Specification
        String strQuery = "print type $1 select $2 dump $3";
        String strResult = MqlUtil.mqlCommand(context, strQuery, ReqSchemaUtil.getSpecificationType(context), "derivative", ",");
        // derList contains all the derivatives of type Specification
        if (!"".equals(strResult) && !"null".equals(strResult) && strResult!=null)
        {
            derList =  FrameworkUtil.split(strResult, ",");
        }
        // loop through all the records and returns all the
        //  related specifications with comma separated
        while(objectListItr.hasNext())
        {
            specList = new StringList();
            StringBuffer stbNameRev = new StringBuffer();
            objectMap = (Map) objectListItr.next();
            strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
            domObj = DomainObject.newInstance(context, strObjId);
            StringList objSelect = new StringList();
            objSelect.add("id");
            objSelect.add("name");
            objSelect.add("type");

            StringList relSelect = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            // getting all related Specification Objects which are connected directly
            // or connected through the chapters */
            MapList specObjMapList = domObj.getRelatedObjects(context,ReqSchemaUtil.getSpecStructureRelationship(context),
                sbTypeSelect.toString(), objSelect, relSelect, true, false, (short)0, null, null);

            Iterator itr = specObjMapList.iterator();
            // Iterating through each Specification Object and making it hyperlink
            while(itr.hasNext())
            {
                Map reqObjMap = (Map)itr.next();
                String strSpecObjId = (String)reqObjMap.get("id");
                String strObjName = (String)reqObjMap.get("name");
                String strObjType = (String)reqObjMap.get("type");
                typeIcon = UINavigatorUtil.getTypeIconProperty(context, strObjType);
                defaultTypeIcon = "<img src=\"../common/images/" +typeIcon+ "\" border=\"0\" />";
                // Adding to the maplist only derivatives of Specification
                // and ignoring the Chapter types
                if (derList.contains(strObjType) && !specList.contains(strSpecObjId))
                {
                	if(isExportFormat || isPrinterFriendly)
                	{
                		 if(isExportFormat && strExportFormat!= null && "CSV".equalsIgnoreCase(strExportFormat))
                		 {
                			 stbNameRev.append(",");
                			 stbNameRev.append(strObjName);
    	    	         }
                		 else
                		 {
                			 stbNameRev.append(", <b> ");
                			 stbNameRev.append(defaultTypeIcon);
                			 stbNameRev = stbNameRev.append( strObjName);
                			 stbNameRev.append("</b> ");
                			 
                		 }
                	}
                	else
                	{
	                    specList.add(strSpecObjId);
	                    stbNameRev.append(", <a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
	                    stbNameRev.append("objectId=");
	                    stbNameRev.append(strSpecObjId);
	                    stbNameRev.append("', '875', '550', 'false', 'popup', '')\">");
	                    stbNameRev.append(defaultTypeIcon);
	                    stbNameRev = stbNameRev.append( strObjName);
	                    stbNameRev.append("</a> ");
                	}
                }
            }

            if (stbNameRev.length()>0)
                stbNameRev.deleteCharAt(0);

            lstNameRev.add(stbNameRev.toString());
        }
        return(lstNameRev);
    }


    /**
     * This method is used to return the list of an Requirement objects
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return List- the List of Requirement Object names with hyperlink
     * @throws Exception
     *             if the operation fails
     * @since RequirementManagement X3
     */
    public List getOtherRelatedRequirements (Context context, String[] args) throws Exception
    {
        // unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) programMap.get(STR_OBJECT_LIST);
        HashMap ParamMap = (HashMap) programMap.get("paramList");
        Iterator objectListItr = lstobjectList.iterator();
        Map objectMap = new HashMap();
        String strObjId = DomainConstants.EMPTY_STRING;
        String parentObjId =(String)ParamMap.get(SYMB_OBJECT_ID);
        List lstNameRev = new StringList();
        DomainObject domObj = null;
        String typeIcon = "";
        String defaultTypeIcon = "";

        // loop through all the records and getting all connected parent Requirement objects
        while(objectListItr.hasNext())
        {
            StringBuffer stbNameRev = new StringBuffer();
            objectMap = (Map) objectListItr.next();
            strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
            String relId = (String)objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            domObj = DomainObject.newInstance(context, strObjId);
            StringList objSelect = new StringList();
            objSelect.add(DomainConstants.SELECT_ID);
            objSelect.add(DomainConstants.SELECT_NAME);
            objSelect.add(DomainConstants.SELECT_TYPE);

            StringList relSelect = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            StringBuffer relNamesBuffer=new StringBuffer();
            relNamesBuffer.append(ReqSchemaUtil.getSubRequirementRelationship(context));
            relNamesBuffer.append(SYMB_COMMA);
            relNamesBuffer.append(ReqSchemaUtil.getDerivedRequirementRelationship(context));
            relNamesBuffer.append(SYMB_COMMA);
            relNamesBuffer.append("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
            relNamesBuffer.append(SYMB_COMMA);
            relNamesBuffer.append(SELECT_LEVEL);
            relNamesBuffer.append(SYMB_COMMA);
            relNamesBuffer.append(DomainRelationship.SELECT_FROM_ID);
            relNamesBuffer.append(SYMB_COMMA);

            StringBuffer relWhereBuffer=new StringBuffer();

            if (relId != null && relId.isEmpty())
                relId = null;
            
            relWhereBuffer.append(DomainConstants.SELECT_RELATIONSHIP_ID).append("!=").append(relId);
            
            MapList specObjMapList = domObj.getRelatedObjects(context, relNamesBuffer.toString(), ReqSchemaUtil.getRequirementType(context),
                    objSelect, relSelect, true, false, (short)1, null, relWhereBuffer.toString(), null, null, null);

            Iterator itr = specObjMapList.iterator();
            // Iterating through each  Requirement Object and making it hyperlink
            while(itr.hasNext())
            {
                Map reqObjMap = (Map)itr.next();
                String strSpecObjId = (String)reqObjMap.get(DomainConstants.SELECT_ID);
                String strObjName = (String)reqObjMap.get(DomainConstants.SELECT_NAME);
                String strObjType = (String)reqObjMap.get(DomainConstants.SELECT_TYPE);
                if (!parentObjId.equals(strSpecObjId))
                {
                    typeIcon = UINavigatorUtil.getTypeIconProperty(context,strObjType);
                    defaultTypeIcon = "<img src=\"../common/images/" +typeIcon+ "\" border=\"0\" />";
                    stbNameRev.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
                    stbNameRev.append("objectId=");
                    stbNameRev.append(strSpecObjId);
                    stbNameRev.append("', '875', '550', 'false', 'popup', '')\">");
                    stbNameRev.append(defaultTypeIcon);
                    stbNameRev = stbNameRev.append(strObjName);
                    stbNameRev.append("</a>");
                    if(itr.hasNext()){
                    	stbNameRev.append(", ");
                    }
                }
            }
            lstNameRev.add(stbNameRev.toString());
        }

        return(lstNameRev);
    }


    /**
     * This method is used to expand the Specification Object
     *  to show connected Chapters in Target Specification Search results page
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return List- the List of Chapter Objects
     * @throws Exception
     *             if the operation fails
     * @since RequirementManagement X3
     */
    public MapList expandSpecificationObjects(Context context, String[] args)throws Exception
    {
        MapList specList=new MapList();
        try
        {
            // unpack the arguments
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) paramMap.get("objectId");
            DomainObject domObject = new DomainObject(strObjectId);

            //object selects
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);

            //relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            // get all the Chapter Objects which are connected to the Specification object
            specList = domObject.getRelatedObjects(context,  ReqSchemaUtil.getSpecStructureRelationship(context),
                    ReqSchemaUtil.getChapterType(context), selectStmts, selectRelStmts, false, true, (short)1, null, null);

        }
        catch(Exception ex)
        {
            throw  new FrameworkException(ex.getMessage());
        }
        return(specList);
    }

    /**
     * This method is used to update the relationship attribute value for revised Object
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return int- return 0
     * @throws Exception
     *             if the operation fails
     * @since RequirementManagement V6R2009-1
     */
    public int revisedParentRequirement(Context context, String[] args)throws Exception
    {
        String strObjectId = args[0];
        String strRevisedObjectId;
        String relId="";
        String strRelName="";
        String strLinkStatusVal="";
        StringBuffer sbAttrKey;
        MapList connReqObjList = new MapList();
        try
        {
            String event = MqlUtil.mqlCommand(context, "get env $1","EVENT");
    		
            // getting the revised object id
            DomainObject domRevisedObject = RequirementsCommon.getLastRevision(context, strObjectId, "MajorRevision".equalsIgnoreCase(event));

            StringBuffer relNamesBuffer=new StringBuffer();
            relNamesBuffer.append(ReqSchemaUtil.getSubRequirementRelationship(context));
            relNamesBuffer.append(SYMB_COMMA);
            relNamesBuffer.append(ReqSchemaUtil.getDerivedRequirementRelationship(context));

            //relationship selects
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
            // get all the Requirement Objects which are connected to the revised Requirement object
            connReqObjList=domRevisedObject.getRelatedObjects(context, relNamesBuffer.toString(), ReqSchemaUtil.getRequirementType(context),
                    null, selectRelStmts, false, true, (short)1, null, null);

            
            String attLinkStatus = ReqSchemaUtil.getLinkStatusAttrubite(context);
            // Iterating through each  Requirement Object and making it hyperlink
            Iterator itr = connReqObjList.iterator();
            while(itr.hasNext())
            {
                relId="";
                Map map = (Map) itr.next();
                relId=(String)map.get("id[connection]");
                strRelName=(String)map.get("type[connection]");
                sbAttrKey = new StringBuffer(80);
                sbAttrKey.append("emxRequirements.");
                sbAttrKey.append(strRelName.toString());
                sbAttrKey.append(".");
                sbAttrKey.append("Attribute");
                sbAttrKey.append(".");
                sbAttrKey.append(attLinkStatus);

                strLinkStatusVal = EnoviaResourceBundle.getProperty(context, sbAttrKey.toString().replace(' ', '_'));
                DomainRelationship.setAttributeValue(context, relId, attLinkStatus,strLinkStatusVal.trim());
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.out);
            throw  new FrameworkException(ex.getMessage());
        }
        return(0);
    }

    
    /**
     * Method to find Requirement Specification list
     * 
     * @param context  the eMatrix <code>Context</code> object
     * @param args  holds arguments
     * @return MapList list of Specification objects
     * @throws Exception if operation fails
     * @since X3
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRequirementSpecifications (Context context, String[] args) throws Exception
    {
        //Unpacks the argument for processing
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectId in context
        String strObjectId = (String)programMap.get("objectId");
        //String List initialized to retrieve back the data
        //StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);

        String strSpecStructReln = ReqSchemaUtil.getSpecStructureRelationship(context);
        //String strReqSpecReln = ProductLineContants.RELATIONSHIP_REQUIREMENT_SPECIFICATION;

        //Domain Object initialized with the object id.
        setId(strObjectId);

        boolean bGetTo = true;
        boolean bGetFrom = false;
        short sRecursionLevel = 0;
        String strBusWhereClause = "";
        String strRelWhereClause = "";

        List lstSpecChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getSpecificationType(context));
        StringBuffer sbBuffer = new StringBuffer(100);
        sbBuffer.append(ReqSchemaUtil.getSpecificationType(context));
        sbBuffer.append(SYMB_COMMA);
        for (int i=0; i < lstSpecChildTypes.size(); i++)
        {
            sbBuffer = sbBuffer.append(lstSpecChildTypes.get(i));
            if (i != lstSpecChildTypes.size()-1)
            {
                sbBuffer = sbBuffer.append(SYMB_COMMA);
            }
        }

        MapList mapSpecs = getRelatedObjects(context, strSpecStructReln, DomainConstants.QUERY_WILDCARD,
            bGetTo, bGetFrom, sRecursionLevel, objectSelects, null, strBusWhereClause, strRelWhereClause,
            null, sbBuffer.toString(), null);

        //IR Mx377290
        //the presence of "level" attribute causes table sorting to fail
        for(int i = 0; i < mapSpecs.size(); i++)
        {
        	Map m = (Map)mapSpecs.get(i);
        	if(m != null)
        	{
        		m.remove("level");
        	}
        }
        //end of IR Mx377290

        return(mapSpecs);
    }


	 /**
     * This method is used to return the list of an Decision objects linked directly to an object
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return List- the List of Decision Object names with hyperlink
     * @throws Exception
     *             if the operation fails
     * @since RequirementManagement X3
     */
    public Vector getObjectDecisionList (Context context, String[] args) throws Exception
    {
		//String selectString = "from["+ReqSchemaUtil.getRequirementDecisionRelationship(context)+"].to.id";
		String selectString = "to["+ReqSchemaUtil.getDecisionRelationship(context)+"].from.id";
		return getDecisionList(context, args, selectString);
	}

	/**
     * This method is used to return the list of an Decision objects linked from a relationship
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return List- the List of Decision Object names with hyperlink
     * @throws Exception
     *             if the operation fails
     * @since RequirementManagement X3
     */
    public Vector getLinkDecisionList (Context context, String[] args) throws Exception
    {
		//String selectString = "to.frommid.to.id"; //
		String selectString = "to.tomid.from.id";
		return getDecisionList(context, args, selectString);
	}


    /**
     * This method is used to return the list of an Decision objects
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @param iSelect Decision selection
     * @return List- the List of Decision Object names with hyperlink
     * @throws Exception
     *             if the operation fails
     * @since RequirementManagement X3
     */
    public Vector getDecisionList (Context context, String[] args, String iSelect) throws Exception
    {
        Vector Decision = new Vector();
        
        // unpack the arguments
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap ParamMap   = (HashMap) programMap.get("paramList");
        String parentObjId =(String)ParamMap.get(SYMB_OBJECT_ID);
        List lstobjectList = (MapList) programMap.get("objectList");

		String strObjectId = (String)programMap.get("objectId");
		System.out.println(strObjectId);
		//System.out.println(lstobjectList);

        String uniqueDecision  = "";
        Iterator objectListItr = lstobjectList.iterator();

        Map objectMap   = new HashMap();
        String strObjId = DomainConstants.EMPTY_STRING;

        String typeIcon        = "";
        String defaultTypeIcon = "";
        StringList objectselects = new StringList(DomainConstants.SELECT_NAME);
        objectselects.add(DomainConstants.SELECT_ID);
        objectselects.add(DomainConstants.SELECT_TYPE);

        // loop through all the records and getting all Decisions
        while(objectListItr.hasNext())
        {
            StringList DecisionList = new StringList();
            StringBuffer sbDecisionsName = new StringBuffer();

            objectMap = (Map) objectListItr.next();
            strObjId = (String)objectMap.get("id");

            //if (!parentObjId.equals(strObjId))
            //{
                String buffQuery = "print bus $1 select $2 dump $3";
                //execute the mql command with & assign the result to a variable.
                String result = MqlUtil.mqlCommand(context, buffQuery, strObjId, iSelect, "|");
                StringList tempDecisionList = FrameworkUtil.split(result,"|");

                java.util.Iterator itrDecision = tempDecisionList.iterator();
                while(itrDecision.hasNext())
                {
                    uniqueDecision = (String)itrDecision.next();
                    if (!(DecisionList.contains(uniqueDecision)))
                        DecisionList.add(uniqueDecision);
                }

                String[] arrDecisionIds = (String[])DecisionList.toArray(new String[]{});
                MapList decisionObjMapList = DomainObject.getInfo(context,arrDecisionIds,objectselects);
                Iterator itr = decisionObjMapList.iterator();

                // Iterating through each  Decision Object and making it hyperlink
                while(itr.hasNext())
                {
                    Map decObjMap = (Map)itr.next();
                    String strDecisionObjId = (String)decObjMap.get(DomainConstants.SELECT_ID);
                    String strDecisionObjName = (String)decObjMap.get(DomainConstants.SELECT_NAME);
                    String strDecisionObjType = (String)decObjMap.get(DomainConstants.SELECT_TYPE);

                    typeIcon = UINavigatorUtil.getTypeIconProperty(context, strDecisionObjType);

					defaultTypeIcon = "<img src=\"../common/images/" +typeIcon+ "\" border=\"0\" />";

					//form the html to display the decisions in the decisions column of Requirement Structure Browser.
					sbDecisionsName.append(defaultTypeIcon);
					sbDecisionsName.append(" ");
					sbDecisionsName.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=");
					sbDecisionsName.append(strDecisionObjId);
					sbDecisionsName.append("', '875', '550', 'false', 'popup', '')\">");
					sbDecisionsName.append(XSSUtil.encodeForHTML(context, strDecisionObjName));
					sbDecisionsName.append("</a>");
                }
            //}
             
             
            Decision.add(sbDecisionsName.toString());
        }
        
        return(Decision);
    }

    /**
     * Method is returing specification objects which are connects to Requirement objects.
     * @param context  the eMatrix <code>Context</code> object
     * @param args  holds arguments
     * @return Maplist specification objects
     * @throws Exception if operation fails
     * @since X3
     */
       public MapList getSpecificationsConnectedRequirements (Context context, String[] args) throws Exception
    {
        //Unpacks the argument for processing
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectId in context
        String strObjectId = (String)programMap.get("objectId");
        //String List initialized to retrieve back the data
        //StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        String strSpecStructReln = RequirementsUtil.getRequirementSpecificationRelationship(context);

        //Domain Object initialized with the object id.
        setId(strObjectId);

        boolean bGetTo = false;
        boolean bGetFrom = true;
        short sRecursionLevel = 0;
        String strBusWhereClause = "";
        String strRelWhereClause = "";

        List lstSpecChildTypes=ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getSpecificationType(context));
        StringBuffer sbBuffer = new StringBuffer(100);
        sbBuffer.append(ReqSchemaUtil.getSpecificationType(context));
        sbBuffer.append(SYMB_COMMA);
        for (int i=0; i < lstSpecChildTypes.size(); i++)
        {
            sbBuffer = sbBuffer.append(lstSpecChildTypes.get(i));
            if (i != lstSpecChildTypes.size()-1)
            {
                sbBuffer = sbBuffer.append(SYMB_COMMA);
            }
        }

        MapList mapSpecifications = getRelatedObjects(context, strSpecStructReln, DomainConstants.QUERY_WILDCARD,
            bGetTo, bGetFrom, sRecursionLevel, objectSelects, null, strBusWhereClause, strRelWhereClause,
            null, sbBuffer.toString(), null);

        return(mapSpecifications);
    }


/** Trigger Method to check if a Requirement is able to be demoted from release
     *  (i.e. it must not contain any requirement parents that are released)
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - nothing
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2009x
     */
    public int demoteFromReleaseReqStructureChildrenCheck(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];

        try
        {
            if (doReleasedReqParentsExist(context,strObjectId))
            {
                //push error about released parents
                String errorString = EnoviaResourceBundle.getProperty(context,
                													"emxRequirementsStringResource",
                														context.getLocale(),
                														"emxRequirements.Error.ReleasedParents"); 
                MqlUtil.mqlCommand(context, "error $1",errorString);
                return 1;
            }
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
        return 0;
    }



/** Utility method for revision triggers
     * @param context - the eMatrix <code>Context</code> object
     * @param objectId - holds the object id.
     * @return - MapList of non-released parent objects
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2009x
     */
    public boolean doReleasedReqParentsExist(Context context, String objectId)
        throws Exception
    {
        try
        {
            String strRelPattern = ReqSchemaUtil.getSubRequirementRelationship(context) + "," + ReqSchemaUtil.getDerivedRequirementRelationship(context);

            StringList lstRelSelects = new StringList(SELECT_RELATIONSHIP_ID);
            boolean bGetTo = true;
            boolean bGetFrom = false;
            short sRecursionLevel = 1;
            String strBusWhereClause = "";
            String strRelWhereClause = "";

            String strReleaseReq = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementPolicy(context), "state_Release");
            String strObsoleteReq = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementPolicy(context), "state_Obsolete");

            StringBuffer sbWhereBus = new StringBuffer();

            // ((policy == 'Requirement') AND ((current == 'Release') OR (current == 'Obsolete')))
			sbWhereBus.append(SYMB_OPEN_PARAN);
			sbWhereBus.append(SYMB_OPEN_PARAN);
			sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(ReqSchemaUtil.getRequirementPolicy(context));
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseReq);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_OR);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strObsoleteReq);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);

            strBusWhereClause = sbWhereBus.toString();

            DomainObject domReq = DomainObject.newInstance(context,objectId);
            MapList mapParentObjects = domReq.getRelatedObjects(context, strRelPattern, QUERY_WILDCARD,
                  null, lstRelSelects, bGetTo, bGetFrom, sRecursionLevel, strBusWhereClause, strRelWhereClause);

            if (mapParentObjects.size() > 0)
                return true;
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
        return false;
    }

    /** Utility method to get the category tree objects for SCE
     * @param context - the eMatrix <code>Context</code> object
     * @param args - needs either selectedObjects (semi-colon separated tableRowIds) or selectedRelationship (symbolic Rel for the expansion)
     * @return - MapList of category tree objects
     * @throws Exception if the operation fails
     * @since X3
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCategoryTreeObjects(Context context, String[] args)throws Exception
    {
        MapList requirementVersionsList = new MapList();
        try
        {
            // unpack the arguments
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            String selectedObjects = (String)paramMap.get("selectedObjects");
            if(selectedObjects != null){
         	   String[] tableRowIds = selectedObjects.split("[;]");
         	   for(int ii = 0; ii < tableRowIds.length; ii++){
         		   Map objMap = new HashMap();
         		   String[] ids = tableRowIds[ii].split("[|]");
         		   String relId = ids[0];
         		   String objId = ids[1];
 	        	   DomainObject obj = DomainObject.newInstance(context, objId);
 	        	   objMap.put(DomainConstants.SELECT_ID,  objId);
 	        	   objMap.put(DomainConstants.SELECT_TYPE,  obj.getInfo(context, DomainConstants.SELECT_TYPE));
 	        	   objMap.put(DomainConstants.SELECT_NAME,  obj.getInfo(context, DomainConstants.SELECT_NAME));
 	        	   objMap.put(DomainConstants.SELECT_REVISION,  obj.getInfo(context, DomainConstants.SELECT_REVISION));
 	        	   objMap.put(DomainConstants.SELECT_MODIFIED,  obj.getInfo(context, DomainConstants.SELECT_MODIFIED));
 	        	   objMap.put(DomainConstants.SELECT_DESCRIPTION,  obj.getInfo(context, DomainConstants.SELECT_DESCRIPTION));
 	        	   objMap.put(SELECT_RESERVED_BY,  obj.getInfo(context, SELECT_RESERVED_BY));
 	        	   objMap.put(DomainConstants.SELECT_RELATIONSHIP_ID,  relId);
 	        	   objMap.put(DomainConstants.SELECT_LEVEL,  "1");

 	        	   requirementVersionsList.add(objMap);
         	   }
            }else{

 	           int maxLevels = 1;

 	           // get the object id from the param map.
 	           String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
 	           DomainObject domObject = new DomainObject(strObjectId);
 	           // get the type of the business object
 	           String strType = domObject.getInfo(context,DomainConstants.SELECT_TYPE);

 	           // object selects
 	           StringList selectStmts = new StringList(2);
 	           selectStmts.addElement(DomainConstants.SELECT_ID);
 	           selectStmts.addElement(DomainConstants.SELECT_NAME);
 	           // add attributes below to support rich text editor
 	           selectStmts.addElement(DomainConstants.SELECT_TYPE);
 	           selectStmts.addElement(DomainConstants.SELECT_REVISION);
 	           selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
 	           selectStmts.addElement(DomainConstants.SELECT_MODIFIED);
 	           selectStmts.addElement(SELECT_RESERVED_BY);

 	           // relationship selects
 	           StringList selectRelStmts = new StringList(1);
 	           selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
 	           // add level to support rich text editor
 	           selectRelStmts.addElement(DomainConstants.SELECT_LEVEL);

 	           StringBuffer sbTypeSelect = new StringBuffer(60);
 	           StringBuffer sbRelSelect = new StringBuffer(60);

               sbTypeSelect.append("*");

               String selectedRelationship = (String)paramMap.get("selectedRelationship");
               // construct the relationship list
               sbRelSelect = sbRelSelect.append(PropertyUtil.getSchemaProperty(context,selectedRelationship));

               // get all the related requirement object based on type & relationship list.
               requirementVersionsList = domObject.getRelatedObjects(context, sbRelSelect.toString(), sbTypeSelect.toString(),
                       selectStmts, selectRelStmts, false, true, (short) maxLevels, null, null);
 	       }
        }
        catch(Exception ex)
        {
            throw(new FrameworkException(ex.getMessage()));
        }
        return(requirementVersionsList);

    }
    /**
     * Return the RMB menu settings for Requirement structure.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args JPO arguments
     * @throws Exception
     *             if the operation fails
     * @return HashMap get Requirement specification objects in HashMap 
     * @since RequirementCentral X4
     */
    public HashMap getRequirementStructureRMBMenu(Context context,String[] args )throws Exception
	{
		HashMap hmpInput  = (HashMap)JPO.unpackArgs(args);

		HashMap requestMap = (HashMap) hmpInput.get("requestMap");
		String strlanguage = (String) requestMap.get("languageStr");

		HashMap commandMap = (HashMap) hmpInput.get("commandMap");

		HashMap paramMap = (HashMap) hmpInput.get("paramMap");
		String objectId = (String)paramMap.get("objectId");

		//use rmbTableRowId to override objectId
		String tableRowId = (String)paramMap.get("rmbTableRowId");
		if(tableRowId != null){
			String[] tokens = tableRowId.split("[|]", -1);
			objectId = tokens[1];
		}

		String menuName = null;
		if(RequirementsUtil.isRequirement(context, objectId)){
			menuName = "RMTReqirementStructureStaticRMB";
		}

		commandMap = UIToolbar.getToolbar(context, menuName, PersonUtil.getAssignments(context), objectId, requestMap, strlanguage);

		return commandMap;

	}


	/**
	 * Get all the Requirement Versions for a Requirement
	 *
	 * @param context
	*            the eMatrix <code>Context</code> object
	* @param args JPO arguments
	 * @throws Exception
	 *             if the operation fails
	 * @return MapList Requirement Version list
	 * 
	 * @since RequirementCentral R207
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRequirementVersions(Context context, String[] args) throws Exception
	{
		MapList relBusObjPageList = new MapList();
		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");

		//Domain Object initialized with the object id.
		setId(strObjectId);

		short sRecursionLevel = 1;
		String strType = ReqSchemaUtil.getRequirementType(context);
		String strRelName = ReqSchemaUtil.getRequirementVersionRelationship(context);

		relBusObjPageList = getRelatedObjects(context, strRelName, strType, objectSelects, relSelects, false, true,
				sRecursionLevel, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

		return relBusObjPageList;
	}


	/**
	 * To find the List of selected object having latest Version 
	 * 
	 * @param context  the eMatrix <code>Context</code> object
	 * @param args JPO arguments
	 * @return Version objects
	 * @throws Exception if operation fails
	 * @since X3
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getVersionRels (Context context, String[] args)
        throws Exception
    {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);

		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

		String strRollUpMode = (String)programMap.get("mode");

		String strBusWhereClause = "";

		//Modified:24-Mar-09:kyp:R207:RMT Bug 371866
        String strRelWhereClause = "type != '" + ReqSchemaUtil.getRequirementVersionRelationship(context) + "' && type != '" + ReqSchemaUtil.getThreadRelationship(context) + "' && type != '" + ReqSchemaUtil.getPublishSubscribeRelationship(context) + "'";
        //End:R207:RMT Bug 371866

		String strObjectId = (String)programMap.get("objectId");
        setId(strObjectId);
        short nRecurseLevel = 1;
        MapList mapRelObjs = getRelatedObjects(context, "*", "*",
                objectSelects, relSelects, true, true, nRecurseLevel, strBusWhereClause, strRelWhereClause);

        //Added:24-Mar-09:kyp:R207:RMT Bug 370824
//        Proper solution to bug is to have Prevent Duplicate enabled for relationship Decision, which will then automatically
//        prevent duplicate connection between the Decision object and any other object. But this is schema change which is
//        discouraged at this point in release cycle.
//        I referred other common objects' relationships, which are provided as selectable options for rollup, they all have
//        this above change.
//        Ex. Reference Document, EC Affected Item and Issue relationships.
//
//        Also I don't find it OK to solve it as per Vijay's comments in the bug, though the relationship (schema) allows multiple
//        connections, having the same decision connected to the requirement multiple times is meaning-less.
//
//        So the resolution is to prevent the display/selection of Decision objects (which are already connected to parent requirement)
//        from requirement roll up interface.
//        For this purpose filtering out the unwanted Decision objects.

        // Find the parent requirement object
        String strParentReqId = getInfo(context, "to[" + ReqSchemaUtil.getRequirementVersionRelationship(context) + "].from.id");
        if (strParentReqId != null && !"".equals(strParentReqId)) {
            DomainObject dmoParentReq = DomainObject.newInstance(context, strParentReqId);
            MapList mlParentDecisions = dmoParentReq.getRelatedObjects(context,
                                                                       ReqSchemaUtil.getDecisionType(context),
                                                                       ReqSchemaUtil.getDecisionRelationship(context),
                                                                       objectSelects,
                                                                       null,
                                                                       true,
                                                                       false,
                                                                       nRecurseLevel,
                                                                       null,
                                                                       null);

            // For easy and optimized comparision, separate the ids in a list structure
            List listParentDecision = new ArrayList();
            for (Iterator itrParentDecisions = mlParentDecisions.iterator(); itrParentDecisions.hasNext();) {
                Map mapParentDecision = (Map) itrParentDecisions.next();
                listParentDecision.add(mapParentDecision.get(DomainConstants.SELECT_ID));
            }

            // Filter the parent decision objects
            MapList mlFilteredRels = new MapList();
            for (Iterator itrRelObjects = mapRelObjs.iterator(); itrRelObjects.hasNext();) {
                Map mapRelObjInfo = (Map) itrRelObjects.next();
                if (!listParentDecision.contains(mapRelObjInfo.get(DomainConstants.SELECT_ID))) {
                    mlFilteredRels.add(mapRelObjInfo);
                }
            }
            mapRelObjs = mlFilteredRels;
        }
        //End:R207:RMT Bug 370824

        return(mapRelObjs);
    }


	/**
	 * Check whether a requriement is a version object
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param objectId id of the object
     * @return true if it is a version object
	 * @throws Exception
	 *             if the operation fails
	 * @since RequirementCentral R207
	 */
    private boolean isVersionObject(Context context, String objectId) throws Exception
    {
	    DomainObject obj = DomainObject.newInstance(context, objectId);

        String policy = obj.getInfo(context, DomainConstants.SELECT_POLICY);
        return ReqSchemaUtil.getRequirementVersionPolicy(context).equals(policy);

    }

	/**
	 * create a new requirement version and update the SCE UI
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
     * @param args - needs "paramMap"(which contains the xmlMsg from SCE), "requestMap"
     * @return a Map that contains "jsCode" to update the SCE UI
	 * @throws Exception
	 *             if the operation fails
	 * @since RequirementCentral R207
	 */
    public Map createNewVersionWithUIUpdate(Context context, String[] args) throws Exception
    {
    	return createNewVersionInternal(context, args, true);
    }

	/**
	 * create a new requirement version without updating the SCE UI
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
     * @param args - needs "paramMap"(which contains the xmlMsg from SCE), "requestMap"
     * @return a blank Map
	 * @throws Exception
	 *             if the operation fails
	 * @since RequirementCentral R207
	 */
    public Map createNewVersion(Context context, String[] args) throws Exception
    {
    	return createNewVersionInternal(context, args, false);
    }

	/**
	 * create a new requirement version and update the SCE UI if required
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
     * @param args - needs "paramMap"(which contains the xmlMsg from SCE), "requestMap"
     * @param updateUI - if true, return the javascript code to update the SCE UI
     * @return a Map that contains "jsCode" to update the SCE UI, if required
	 * @throws Exception
	 *             if the operation fails
	 * @since RequirementCentral R207
	 */
    protected Map createNewVersionInternal(Context context, String[] args, boolean updateUI) throws Exception
    {
    	Map retMap = new HashMap();

    	if(!"true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context, "emxRequirements.Requirement.SCE.SaveToCreateVersion"))){
    		return retMap;
    	}

    	String BUNDLE = "emxRequirementsStringResource";
    	Map programMap = (Map)JPO.unpackArgs(args);
    	Map paramMap = (Map)programMap.get("paramMap");
    	Map requestMap = (Map)programMap.get("requestMap");
    	String language = (String)requestMap.get("languageStr");

    	Document doc = (Document)paramMap.get("xmlMsg");
    	if(doc != null){
            Element root = doc.getRootElement();
            try
            {
                //ContextUtil.startTransaction(context, true);
                String relId = null;
               	String richtext = null, format = null, plaintext = null;

            	Element elem = root.getChild("object");

            	if(elem == null){
                    String message = EnoviaResourceBundle.getProperty(context, BUNDLE, context.getLocale(), "emxRequirements.SCE.Message.NoChange"); 
            		throw new FrameworkException(message); //this happens when user make no changes and commit.
            	}

        		//objectId and relId
        		String objectId = elem.getAttributeValue("objectId");
        		relId = elem.getAttributeValue("relId");

        		if(objectId == null){
        			throw new FrameworkException("Missing object id.");
        		}

    			boolean isRequirement = RequirementsUtil.isRequirement(context, objectId);

        		if(isRequirement && !isVersionObject(context, objectId)){

        			RequirementsCommon versionObj = new RequirementsCommon();
        			DomainRelationship dr = versionObj.createRequirementVersion(context, objectId, objectId);
        			String versionOid = versionObj.getObjectId();

                    boolean closeRel = dr.openRelationship(context);
                    StringList relSelects = new StringList(DomainConstants.SELECT_ID);
                    Map details = dr.getRelationshipData(context, relSelects);
                    StringList newRelId = (StringList) details.get(DomainConstants.SELECT_ID);
                    dr.closeRelationship(context, closeRel);

	        		if(updateUI){
		        		String javascript = "emxEditableTable.addToSelected(\"<mxRoot><action>add</action><data  status='committed' ><item oid='" +
		        		versionOid + "' relId='" + newRelId.elementAt(0) + "' pid='" + objectId + "'/></data></mxRoot>\");";

		        		retMap.put("jsCode", javascript);
	        		}
            	}

            } catch (FrameworkException fe) {
                throw fe;
            } catch (Exception ex) {
                //ContextUtil.abortTransaction(context);
                throw (new FrameworkException(ex.toString()));
            }
    	}
    	return retMap;
    }

    /**
     * Function is used to exclude Decision object Id's which are in Released/Superceded state
     * IR-083126V6R2012
     * @param context   the eMatrix <code>Context</code> object
     * @param args objectId id of the object
     * @return StringList- consisting of the object ids to be excluded from the Search Results
     * @throws Exception if the operation fails
     * @since X3
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeDecisionObjects(Context context, String[] args)
    throws Exception
    {
    	 StringList excludeList = new StringList();
    	  StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
          String strType = ReqSchemaUtil.getDecisionType(context);
    	 //The findobjects method is invoked to get the list of products
          MapList mapBusIds = findObjects(context, strType, null,null,null,DomainConstants.QUERY_WILDCARD,null,true, objectSelects);
          
          for(int itr = 0; itr < mapBusIds.size(); itr++){
				Map map = (Map)mapBusIds.get(itr);
				String strId = (String)map.get(DomainConstants.SELECT_ID);
				DomainObject domObject = DomainObject.newInstance(context,strId);
				String strState = domObject.getInfo(context,SELECT_CURRENT);
				if(null != strState && !strState.equalsIgnoreCase(ReqConstants.STATE_ACTIVE)){
					excludeList.add((String)map.get(DomainConstants.SELECT_ID));
				}
          }
          return excludeList;
    }
    
    /** Display the attribute value of Designater User in type_Requirement webform
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments: 0 - objectId 
     * @return Object Designated User list as object
     * @throws Exception if the operation fails
     * @since X3
     */
    
    public static Object getDesignatedUser(Context context, String args[]) throws Exception
    {
        StringList slDesignaterUserValues = new StringList();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");

            String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);
            
            DomainObject domObj = DomainObject.newInstance(context, strObjectId);
            String strDesignatedUser = domObj.getInfo(context, "attribute[" + ReqSchemaUtil.getDesignatedUserAttribute(context) + "]");
            
            if(strDesignatedUser != null && "Unassigned".equalsIgnoreCase(strDesignatedUser))
            {
            	slDesignaterUserValues.add("");
            }
            else
            {
            	slDesignaterUserValues.add(strDesignatedUser.toString());
            }
            
        } //end of try
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return slDesignaterUserValues;
    }  
    
    /** update the Designated User Value of the requirement from type_Requirement web form
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments: 0 - objectId 
     * @throws Exception if the operation fails
     * @since X3
     */
    
    public static void updateDesignatedUser(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String newValue = (String)paramMap.get("New Value");

        String strObjectId = (String) paramMap.get(SYMB_OBJECT_ID);

        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
        domObj.setAttributeValue(context, ReqSchemaUtil.getDesignatedUserAttribute(context), newValue); 
    } 
    
    /**
     * This method returns an empty MapList, used by SCE when launched from properties page.
     * @param context the eMatrix <code>Context</code> object
     * @param args input arguments
     * @return an empty MapList
     * @throws Exception
     */
    public MapList getEmptyChildrenList(Context context, String[] args)throws Exception
    {
        MapList emptyList=new MapList();
        return emptyList;
    }
    
    /**
     * return list of committed requirements of a Model
     * @param context the eMatrix <code>Context</code> object
     * @param args objectId of the Model
     * @return list of committed requirements of the Model
     * @throws Exception
     * @since R417
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCommittedRequirementsList(Context context, String[] args)throws Exception
    {

        MapList reqList = new MapList();
        Map programMap = (Map)JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        Model modelBean = (Model)DomainObject.newInstance(context,ReqSchemaUtil.getModelType(context),"PRODUCTLINE");
        return modelBean.getCommittedRequirementsListWithProduct(context, objectId);
    }

    /**
     * return the product where the Requirement has been committed
     * @param context the eMatrix <code>Context</code> object
     * @param args MapList of committed Requirements
     * @return List of products
     * @throws Exception
     * @since R417
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public List getCommittedRequirementProduct(Context context, String[] args)throws Exception
    {

        Map programMap = (Map)JPO.unpackArgs(args);
        MapList reqList = (MapList) programMap.get("objectList"); 
        Map paramList = (Map)programMap.get("paramList");


        List products = new Vector(reqList.size()); 
        for(int i = 0; i < reqList.size(); i++)
        {
            Map prodMap = (Map)reqList.get(i);
            String product = (String)(prodMap).get("product");
            if (paramList.get("reportFormat") != null)
            {
                products.add(product);
            }
            else
            {
                String productId = (String)(prodMap).get("productId");
                products.add("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=" + productId + "', '800', '700', 'true', 'popup')\">" + product + "</a>");
            }
            
        }
        return products;
    }
    
}//END of class
