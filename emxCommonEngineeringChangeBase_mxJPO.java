/*
 ** emxCommonEngineeringChangeBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 **
 */



import java.util.ArrayList;
import  java.util.HashMap;
import  java.util.Hashtable;
import  java.util.Iterator;
import java.util.Locale;
import  java.util.Map;
import  java.util.Vector;
import  java.util.List;
import  java.util.StringTokenizer;

import  matrix.db.BusinessObject;
import  matrix.db.BusinessObjectWithSelectList;
import  matrix.db.RelationshipType;

import  matrix.db.Context;
import  matrix.db.JPO;
import  matrix.db.Policy;
import  matrix.db.State;
import  matrix.db.StateList;
import  matrix.db.Signature;
import  matrix.db.SignatureList;

import matrix.util.MatrixException;
import  matrix.util.Pattern;
import  matrix.util.StringList;

import  com.matrixone.apps.domain.DomainObject;
import  com.matrixone.apps.domain.DomainConstants;
import  com.matrixone.apps.domain.DomainRelationship;

import  com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import  com.matrixone.apps.domain.util.FrameworkException;
import  com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import  com.matrixone.apps.domain.util.i18nNow;
import  com.matrixone.apps.domain.util.MessageUtil;
import  com.matrixone.apps.domain.util.MapList;
import  com.matrixone.apps.domain.util.MqlUtil;
import  com.matrixone.apps.domain.util.PersonUtil;
import  com.matrixone.apps.domain.util.PropertyUtil;

import  com.matrixone.apps.common.Company;
import  com.matrixone.apps.common.EngineeringChange;
import  com.matrixone.apps.common.Issue;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.HierarchicalChangeSupport;
import  com.matrixone.apps.common.Person;
import  com.matrixone.apps.common.Route;
import  com.matrixone.apps.common.RouteTemplate;
import  com.matrixone.apps.common.Search;
import com.matrixone.apps.common.util.ComponentsUtil;


/**
 * The <code>emxCommonEngineeringChangeBase</code> class contains methods for executing JPO operations related
 * to objects of the admin type Engineering Change.
 * @author Wipro
 * @version Common 10-6 - Copyright (c) 2004, MatrixOne, Inc.
 **
 */
public class emxCommonEngineeringChangeBase_mxJPO extends emxDomainObject_mxJPO {

    /** A string constant with the value (. */
    public static final String SYMB_OPEN_PARAN           = "(";
    /** A string constant with the value *. */
    public static final String SYMB_WILD                 = "*";
    /** A string constant with the value attribute. */
    public static final String SYMB_ATTRIBUTE            = "attribute";
    /** A string constant with the value ). */
    public static final String SYMB_CLOSE_PARAN          = ")";
    /** A string constant with the value ~~. */
    public static final String SYMB_MATCH                = " ~~ ";
    /** A string constant with the value "'". */
    public static final String SYMB_QUOTE                = "'";
    /** A string constant with the value && . */
    public static final String SYMB_AND                  = " && ";
    /** A string constant with the value ||. */
    public static final String SYMB_OR                   = " || ";
    /** A string constant with the value !. */
    public static final String SYMB_NOT                  = "!";
    /** A string constant with the value !=. */
    public static final String SYMB_NOT_EQUAL            = " != ";
    /** A string constant with the value ".". */
    public static final String SYMB_DOT                  = ".";
    /** A string constant with the value "to". */
    public static final String SYMB_TO                   = "to";
    /** A string constant with the value [. */
    public static final String SYMB_OPEN_BRACKET         = "[";
    /** A string constant with the value ]. */
    public static final String SYMB_CLOSE_BRACKET        = "]";
    /** A string constant with the value "from". */
    public static final String SYMB_FROM                 = "from";
    /** A string constant with the value ==. */
    public static final String SYMB_EQUAL                = " == ";
    /** A string constant with the value state_Implement */
    public static final String EC_STATE_IMPLEMENT        = "state_Implement";
    /** A string constant with the value state_Review */
    public static final String EC_STATE_REVIEW           = "state_Review";
    /** A string constant with the value state_Validate */
    public static final String EC_STATE_VALIDATE         = "state_Validate";
    /** A string constant with the value state_FormalApproval */
    public static final String EC_STATE_FORMAL_APPROVAL  = "state_FormalApproval";
    /** A string constant with the value state_Complete */
    public static final String TE_STATE_COMPLETE         = "state_Complete";
    /** A string constant with the value Test Execution policy */
    // Modified by Infosys for Bug # 302309 Date 04/13/2005
    public static final String POLICY_TEST_EXECUTION     = PropertyUtil.getSchemaProperty("policy_TestExecution");
    /** A string constant with the value state_Closed*/
    public static final String ISSUE_STATE_CLOSED        = "state_Closed";
    /** constant for mode "Enterprise". */
    public static final String ENTERPRISE                = "Enterprise";
    /** constant for mode "Personal". */
    public static final String PERSONAL                  = "Personal";
    /** constant for mode "Region". */
    public static final String REGION                    = "Region";
    /** constant for mode "User". */
    public static final String USER                      = "User";
    /** Added by Infosys for EC-search bug, 08 June 2005
    A string constant with the value const */
    public static final String STR_CONST                 = "const";

    /** relationship "Business Unit Owns". */
    public static final String RELATIONSHIP_BUSINESS_UNIT_OWNS =
        PropertyUtil.getSchemaProperty("relationship_BusinessUnitOwns");

    /** relationship "Assigned EC" */
    public static final String strRelationAssignedEC     = DomainConstants.RELATIONSHIP_ASSIGNED_EC;

    /** constant for vault search option "All". */
    public static final String ALL                       = "All";
    /** constant for vault search option "Default". */
    public static final String DEFAULT                   = "Default";
    /** constant for vault search option "Selected". */
    public static final String SELECTED                  = "Selected";

    /** A string constant with the value vaultOption. */
    public static final String VAULT_OPTION              = "vaultOption";

    /** A string constant with the value emxComponentsStringResource. */
    public static final String RESOURCE_BUNDLE_COMPONENTS_STR = "emxComponentsStringResource";

    /**
     *A string constant with the value emxFrameworkStringResource.
     */
    public static final String RESOURCE_BUNDLE_FRAMEWORK_STR = "emxFrameworkStringResource";
    /**
     * Alias for the range value "Promote Connected Object" of attribute Route
     * Completion Object.
     */
    public static final String RANGE_PROMOTE_CONNECTED_OBJECT = "Promote Connected Object";
    /**
     * Alias for the range value "For Revise" of attribute Requested Change.
     */
    public static final String RANGE_FOR_REVISE = "For Revise";
    /**
     * Alias for the range value "For Release" of attribute Requested Change.
     */
    public static final String RANGE_FOR_RELEASE = "For Release";
    /**
     * Alias for the range value "For Obsolescence" of attribute Requested
     * Change.
     */
    public static final String RANGE_FOR_OBSOLESCENCE = "For Obsolescence";
    /**
     * Alias for range value Yes of attribute Validation Required.
     */
    public static final String RANGE_YES = "Yes";
    /**
     * Alias for the range value "Review" of attribute Route Base Purpose.
     */
    public static final String RANGE_REVIEW = "Review";
    /**
     * Alias for the range value "Approval" of attribute Route Base Purpose.
     */
    public static final String RANGE_APPROVAL = "Approval";
    /**
     * Alias for space.
     */
    public static final String STR_SPACE = " ";
    /**
     * Alias for string true.
     */
    public static final String STR_TRUE = "true";
    /** A string constant for "Y". */
    public static final String STR_Y   = "emxComponents.Common.Y" ;

    /** A string constant for "N". */
    public static final String STR_N   = "emxComponents.Common.N";

    /** A string constant for Higher Revision icon path*/
    public static final String HIGHER_REVISION_ICON =
            "<img src=\"../common/images/iconSmallHigherRevision.gif\" border=\"0\" align=\"middle\"/>";

    /** A string constant for Tool Tip on Higher Revision Icon. */
    public static final String ICON_TOOLTIP_HIGHER_REVISION_EXISTS = "emxComponents.EngineeringChange.HigherRevisionExists";

    /** A string constant for Tool Tip on Active EC Icon. */
    public static final String ICON_TOOLTIP_ACTIVE_EC_EXISTS = "emxComponents.EngineeringChange.ToolTipActiveECExists";

    /** A string constant with the value objectList. */
    public static final String OBJECT_LIST = "objectList";

    /** A string constant for Active EC icon path*/
    public static final String ACTIVE_EC_ICON =
                    "<img src=\"../common/images/iconSmallECRO.gif\" border=\"0\" align=\"middle\">";

    // Added by Infosys for RouteTemplate search results page on 6/14/2005
    public static final String SCOPE_ENTERPRISE                ="emxComponents.SearchTemplate.Enterprise";
    public static final String SCOPE_USER                      ="emxComponents.SearchTemplate.User";

	    /** relationship "Affected Item". */
    public static final String RELATIONSHIP_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_AffectedItem");

    /**
     * Alias for the "Revise" and "Release" state for Requirement Central
     */
    public static final String OPERATION_REVISE = "Revise";
    public static final String OPERATION_RELEASE = "Release";

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public emxCommonEngineeringChangeBase_mxJPO (Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @return        an integer status code (0 = success)
     * @throws        Exception when problems occurred in the Common Components
     * @since         Common 10-6
     **
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String strContentLabel = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Error.UnsupportedClient");

            throw  new Exception(strContentLabel);
        }
        return  0;
    }

    /**
     * Get the list of all Active Engineering Change objects that are owned by or assigned to the context user
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return         a <code>MapList</code> object having the list of Active Engineering Change objects
     *                 owned by or assigned to context user
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllActiveEC (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap           = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId           = (String)programMap.get("objectId");
        //initializing return type
        MapList relBusObjPageList    = new MapList();

        // forming the where clause
        String strWhereExpression    = "";

        StringBuffer sbWhere         = new StringBuffer(150);
        //getting Context user
        String strOwner              = context.getUser();

        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(DomainConstants.SELECT_OWNER);
        sbWhere.append(SYMB_EQUAL);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(strOwner);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(SYMB_CLOSE_PARAN);
        sbWhere.append(SYMB_OR);
        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(SYMB_TO);
        sbWhere.append(SYMB_OPEN_BRACKET);
        sbWhere.append(strRelationAssignedEC);
        sbWhere.append(SYMB_CLOSE_BRACKET);
        sbWhere.append(SYMB_DOT);
        sbWhere.append(SYMB_FROM);
        sbWhere.append(SYMB_DOT);
        sbWhere.append(DomainConstants.SELECT_NAME);
        sbWhere.append(SYMB_EQUAL);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(strOwner);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(SYMB_CLOSE_PARAN);
        sbWhere.append(SYMB_CLOSE_PARAN);
        sbWhere.append(SYMB_AND);

        // add condition that EC object is Active - i.e. not Close, Complete or Reject
        sbWhere.append(getActiveECWhereExpression(context));

        strWhereExpression = sbWhere.toString();

        relBusObjPageList = getECList(context, strParentId, strWhereExpression);

        //returns list of Engineering Change Objects
        return  relBusObjPageList;
    }

    /**
     * Get the list of Active Engineering Change objects owned by the context user
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return         a <code>MapList</code> object having the list of owned Active Engineering Change objects
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOwnedActiveEC (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap           = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId           = (String)programMap.get("objectId");
        //initializing return type
        MapList relBusObjPageList    = new MapList();

        // forming the where clause
        String strWhereExpression    = "";

        StringBuffer sbWhere         = new StringBuffer(150);
        //getting Context user
        String strOwner              = context.getUser();

        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(DomainConstants.SELECT_OWNER);
        sbWhere.append(SYMB_EQUAL);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(strOwner);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(SYMB_CLOSE_PARAN);
        sbWhere.append(SYMB_AND);

        // add condition that EC object is Active - i.e. not Close, Complete or Reject
        sbWhere.append(getActiveECWhereExpression(context));
        sbWhere.append(SYMB_CLOSE_PARAN);

        strWhereExpression = sbWhere.toString();

        relBusObjPageList = getECList(context, strParentId, strWhereExpression);

        //returns list of Engineering Change Objects
        return  relBusObjPageList;
    }

    /**
     * Get the list of Active Engineering Change objects for which context user is assignee
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return         a <code>MapList</code> object having the list of Active Engineering Change objects
     *                 for which context user is assignee
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAssignedActiveEC (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap           = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId           = (String)programMap.get("objectId");
        //initializing return type
        MapList relBusObjPageList    = new MapList();

        // forming the where clause
        String strWhereExpression    = "";

        StringBuffer sbWhere         = new StringBuffer(150);
        //getting Context user
        String strOwner              = context.getUser();

        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(SYMB_TO);
        sbWhere.append(SYMB_OPEN_BRACKET);
        sbWhere.append(strRelationAssignedEC);
        sbWhere.append(SYMB_CLOSE_BRACKET);
        sbWhere.append(SYMB_DOT);
        sbWhere.append(SYMB_FROM);
        sbWhere.append(SYMB_DOT);
        sbWhere.append(DomainConstants.SELECT_NAME);
        sbWhere.append(SYMB_EQUAL);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(strOwner);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(SYMB_CLOSE_PARAN);
        sbWhere.append(SYMB_AND);

        // add condition that EC object is Active - i.e. not Close, Complete or Reject
        sbWhere.append(getActiveECWhereExpression(context));
        sbWhere.append(SYMB_CLOSE_PARAN);

        strWhereExpression = sbWhere.toString();

        relBusObjPageList = getECList(context, strParentId, strWhereExpression);

        //returns list of Engineering Change Objects
        return  relBusObjPageList;
    }

    /**
     * Get the list of Inactive Engineering Change objects for which context user is assignee
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return         a <code>MapList</code> object having the list of Inactive Engineering Change objects
     *                 for which context user is assignee
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAssignedInActiveEC (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap           = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId           = (String)programMap.get("objectId");
        //initializing return type
        MapList relBusObjPageList    = new MapList();

        // forming the where clause
        String strWhereExpression    = "";

        StringBuffer sbWhere         = new StringBuffer(150);
        //getting Context user
        String strOwner              = context.getUser();

        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(SYMB_TO);
        sbWhere.append(SYMB_OPEN_BRACKET);
        sbWhere.append(strRelationAssignedEC);
        sbWhere.append(SYMB_CLOSE_BRACKET);
        sbWhere.append(SYMB_DOT);
        sbWhere.append(SYMB_FROM);
        sbWhere.append(SYMB_DOT);
        sbWhere.append(DomainConstants.SELECT_NAME);
        sbWhere.append(SYMB_EQUAL);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(strOwner);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(SYMB_CLOSE_PARAN);
        sbWhere.append(SYMB_AND);

        // add condition that EC object is Active - i.e. not Close, Complete or Reject
        sbWhere.append(getInActiveECWhereExpression(context));
        sbWhere.append(SYMB_CLOSE_PARAN);

        strWhereExpression = sbWhere.toString();
        relBusObjPageList = getECList(context, strParentId, strWhereExpression);

        //returns list of Engineering Change Objects
        return  relBusObjPageList;
    }

    /**
     * Get the list of Inactive Engineering Change objects owned by the context user
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return         a <code>MapList</code> object having the list of owned InActive Engineering Change objects
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOwnedInActiveEC (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap           = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId           = (String)programMap.get("objectId");
        //initializing return type
        MapList relBusObjPageList    = new MapList();

        // forming the where clause
        String strWhereExpression    = "";

        StringBuffer sbWhere         = new StringBuffer(150);
        //getting Context user
        String strOwner              = context.getUser();

        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(SYMB_OPEN_PARAN);
        sbWhere.append(DomainConstants.SELECT_OWNER);
        sbWhere.append(SYMB_EQUAL);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(strOwner);
        sbWhere.append(SYMB_QUOTE);
        sbWhere.append(SYMB_CLOSE_PARAN);
        sbWhere.append(SYMB_AND);

        // add condition that EC object is Active - i.e. not Close, Complete or Reject
        sbWhere.append(getInActiveECWhereExpression(context));
        sbWhere.append(SYMB_CLOSE_PARAN);

        strWhereExpression = sbWhere.toString();
        relBusObjPageList  = getECList(context, strParentId, strWhereExpression);

        //returns list of Engineering Change Objects
        return  relBusObjPageList;
    }

    /**
     * the method constructs where clause for filtering and getting Active EC i.e. not in Close, Complete or Reject state
     *
     * @param context  the eMatrix <code>Context</code> object
     * @return         a String having the where clause to get Active EC
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     *
     */
    protected String getActiveECWhereExpression(Context context) throws Exception{

        StringBuffer sActiveECWhere = new StringBuffer(100);

        // Getting and representing the state 'Close' of policy EC
        String strClose    = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_CLOSE);

        // Getting and representing the state 'Complete' of policy EC
        String strComplete = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_COMPLETE);

        // Getting and representing the state 'Reject' of policy EC
        String strReject   = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_REJECT);

        sActiveECWhere.append(SYMB_OPEN_PARAN);
        sActiveECWhere.append(DomainConstants.SELECT_CURRENT);
        sActiveECWhere.append(SYMB_NOT_EQUAL);
        sActiveECWhere.append(SYMB_QUOTE);
        sActiveECWhere.append(strClose);
        sActiveECWhere.append(SYMB_QUOTE);
        sActiveECWhere.append(SYMB_AND);
        sActiveECWhere.append(DomainConstants.SELECT_CURRENT);
        sActiveECWhere.append(SYMB_NOT_EQUAL);
        sActiveECWhere.append(SYMB_QUOTE);
        sActiveECWhere.append(strComplete);
        sActiveECWhere.append(SYMB_QUOTE);
        sActiveECWhere.append(SYMB_AND);
        sActiveECWhere.append(DomainConstants.SELECT_CURRENT);
        sActiveECWhere.append(SYMB_NOT_EQUAL);
        sActiveECWhere.append(SYMB_QUOTE);
        sActiveECWhere.append(strReject);
        sActiveECWhere.append(SYMB_QUOTE);
        sActiveECWhere.append(SYMB_CLOSE_PARAN);

        return sActiveECWhere.toString();
    }

    /**
     * the method constructs where clause for filtering and getting non - Active EC i.e. EC objects in Close or Complete or Reject state
     *
     * @param context  the eMatrix <code>Context</code> object
     * @return         a String having the where clause to get Inactive EC
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     *
     */
    protected String getInActiveECWhereExpression(Context context) throws Exception{

        StringBuffer sInActiveECWhere = new StringBuffer(100);

        // Getting and representing the state 'Close' of policy EC
        String strClose    = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_CLOSE);

        // Getting and representing the state 'Complete' of policy EC
        String strComplete = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_COMPLETE);

        // Getting and representing the state 'Reject' of policy EC
        String strReject   = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_REJECT);

        sInActiveECWhere.append(SYMB_OPEN_PARAN);
        sInActiveECWhere.append(DomainConstants.SELECT_CURRENT);
        sInActiveECWhere.append(SYMB_EQUAL);
        sInActiveECWhere.append(SYMB_QUOTE);
        sInActiveECWhere.append(strClose);
        sInActiveECWhere.append(SYMB_QUOTE);
        sInActiveECWhere.append(SYMB_OR);
        sInActiveECWhere.append(DomainConstants.SELECT_CURRENT);
        sInActiveECWhere.append(SYMB_EQUAL);
        sInActiveECWhere.append(SYMB_QUOTE);
        sInActiveECWhere.append(strComplete);
        sInActiveECWhere.append(SYMB_QUOTE);
        sInActiveECWhere.append(SYMB_OR);
        sInActiveECWhere.append(DomainConstants.SELECT_CURRENT);
        sInActiveECWhere.append(SYMB_EQUAL);
        sInActiveECWhere.append(SYMB_QUOTE);
        sInActiveECWhere.append(strReject);
        sInActiveECWhere.append(SYMB_QUOTE);
        sInActiveECWhere.append(SYMB_CLOSE_PARAN);

        return sInActiveECWhere.toString();
    }

   /**
    * Method to get the Engineering Change objects based on passed in where clause.
    *
    * @param context             the eMatrix <code>Context</code> object
    * @param strParentId         the id of the parent context object,if any
    * @param strWhereCondition   the String holds the where expression for query
    * @return Maplist            the eMatrix <code>MapList</code> object having queried EngineeringChange objects list
    * @throws Exception          if the operation fails
    * @since                     Common 10-6
    */
    protected MapList getECList(Context context, String strParentId, String strWhereCondition)
        throws Exception {

        // return list for this method
        MapList relBusObjPageList    = new MapList();
        StringList objectSelects     = new StringList(DomainConstants.SELECT_ID);
        //getting type name from PropertyUtil
        String strTypeEC             = DomainConstants.TYPE_ENGINEERING_CHANGE;

        if (!(strParentId == null || strParentId.equals(DomainConstants.EMPTY_STRING))) {
            //getting the 'EC Affected Item' relationship from PropertyUtil
            String strRelECAffectedItem  = DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM;

            //setting Context Id for the Business Object
            this.setId(strParentId);

            //Relationships are selected by its Ids
            StringList relSelects        = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

            //the number of levels to expand, 1 equals expand one level
            short recurseToLevel     = 1;
            //retrieving Engineering Change List from Context object
            relBusObjPageList        = getRelatedObjects(context,
                                                         strRelECAffectedItem,      // 'Affected Items' relationship
                                                         strTypeEC,                 // get type EC objects
                                                         objectSelects,
                                                         relSelects,
                                                         true,
                                                         false,
                                                         recurseToLevel,
                                                         strWhereCondition,
                                                         DomainConstants.EMPTY_STRING);
        } else {
            //retrieving all Active Engineering Change object List in database
            relBusObjPageList        = findObjects(context,
                                                   strTypeEC,
                                                   DomainConstants.QUERY_WILDCARD,
                                                   DomainConstants.QUERY_WILDCARD,
                                                   DomainConstants.QUERY_WILDCARD,
                                                   DomainConstants.QUERY_WILDCARD,
                                                   strWhereCondition,
                                                   true,
                                                   objectSelects);

        }
        return relBusObjPageList;
    }

    /**
     * Get the list of all Objects that can be returned based on search criteria passed in as argument
     *
     * @param context     the eMatrix <code>Context</code> object
     * @param args        contains a Map with the following input arguments or entries:
     *    queryLimit      limit for displaying search query results
     *    hdnType         the admin 'type'of the object to search for
     *    txtName         the 'name' or 'name pattern' to search for
     *    txtRevision     the 'revision' pattern to search for
     *    txtDescription  the 'description' pattern to search for
     *    txtOwner        the 'owner' pattern to search for
     *    txtState.       the object 'state' pattern to search for
     *    vaultOption     the 'vault' search option
     *    srcDestRelName  the name of the destination relationship to expand for
     * @return            a <code>MapList</code> object having the list of objects satisfying the search criteria
     * @throws            Exception if the operation fails
     * @since             Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getGeneralSearchResults(Context context, String[] args)
        throws Exception {

        MapList mapList = null;
        try {
            Map programMap    = (Map) JPO.unpackArgs(args);
            short sQueryLimit =
                    (short) (java
                            .lang
                            .Integer
                            .parseInt((String) programMap.get("queryLimit")));

            String strType = (String) programMap.get("hdnType");

            if (strType == null
                    || strType.equals("")
                    || "null".equalsIgnoreCase(strType)) {
                    strType = DomainConstants.QUERY_WILDCARD;
            }

            String strName = (String) programMap.get("txtName");

            if (strName == null
                    || strName.equals("")
                    || "null".equalsIgnoreCase(strName)) {
                    strName = DomainConstants.QUERY_WILDCARD;
            }

            String strRevision = (String) programMap.get("txtRevision");

            if (strRevision == null
                    || strRevision.equals("")
                    || "null".equalsIgnoreCase(strRevision)) {
                    strRevision = DomainConstants.QUERY_WILDCARD;
            } else {
                    strRevision = strRevision.trim();
            }

            String strDesc = (String) programMap.get("txtDescription");

            String strOwner = (String) programMap.get("txtOwner");

            if (strOwner == null
                    || strOwner.equals("")
                    || "null".equalsIgnoreCase(strOwner)) {
                    strOwner = DomainConstants.QUERY_WILDCARD;
            } else {
                    strOwner = strOwner.trim();
            }

            String strState = (String) programMap.get("txtState");

            if (strState == null
                    || strState.equals("")
                    || "null".equalsIgnoreCase(strState)) {
                    strState = DomainConstants.QUERY_WILDCARD;
            } else {
                    strState = strState.trim();
            }

            String strVault = "";
            String strVaultOption = (String) programMap.get(VAULT_OPTION);

            if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)) {
                strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
            } else {
                strVault = (String)programMap.get("vaults");
            }

            StringList slSelect = new StringList(1);
            slSelect.addElement(DomainConstants.SELECT_ID);
            //boolean variable which specifies not to include EC 'Close','Complete' and 'Reject' state EC objects and 'Closed' state Issue objects in Search Results
            boolean bInactiveECAndIssue = false;
            boolean bStateSearch        = false;
            boolean bSatisfiedItemSearch= false;
            boolean bStart              = true;
            StringBuffer sbWhereExp = new StringBuffer(150);

            if (strDesc != null
                    && (!strDesc.equals(DomainConstants.QUERY_WILDCARD))
                    && (!strDesc.equals(""))
                    && !("null".equalsIgnoreCase(strDesc))) {
                    if (bStart) {
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        bStart = false;
                    }
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
                    sbWhereExp.append(SYMB_MATCH);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strDesc);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
            }

            String strRelName = (String) programMap.get("srcDestRelName");
            // get Actual relationship name
            strRelName        = (String) PropertyUtil.getSchemaProperty(context,strRelName);

            bSatisfiedItemSearch = (strRelName != null && strRelName.equals(DomainConstants.RELATIONSHIP_RESOLVED_TO));

            // Getting and representing the state 'Close' of policy EC
            String strECStateClose    = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_CLOSE);

            // Getting and representing the state 'Complete' of policy EC
            String strECStateComplete = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_COMPLETE);

            // Getting and representing the state 'Reject' of policy EC
            String strECStateReject   = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_REJECT);

            //To get Issue policy Name
            String strIssuePolicy =PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_policy_Issue);

            // Getting and representing the state 'Closed' of policy Issue
            String strIssueStateClosed   = FrameworkUtil.lookupStateName(context,strIssuePolicy,ISSUE_STATE_CLOSED);

               if(strState != null && (strState.equals(strECStateClose)
                                   || strState.equals(strECStateReject)
                                   || strState.equals(strECStateComplete)
                                   || strState.equals(strIssueStateClosed))) {
                   bInactiveECAndIssue = true;
               }

            // set this flag true when state based, non-wildcard search is done for non-satisfied items search
            if(!bSatisfiedItemSearch && (strState != null)
                  && (!strState.equals(DomainConstants.QUERY_WILDCARD))
                  && (!"".equals(strState))
                  && (!"null".equalsIgnoreCase(strState))) {
                    bStateSearch = true;
             } else if(bSatisfiedItemSearch && !bInactiveECAndIssue){
                 // for Satisfied Items wildcard search, make conditional state search for cases of Active EC / Issue search
                 // this exceptional search is done to prevent Closed / Rejected EC or Issue objects in search results
                 bStateSearch = true;
             }

            if (bStateSearch) {
                if (bStart) {
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    bStart = false;
                } else {
                    sbWhereExp.append(SYMB_AND);
                }

                if(bSatisfiedItemSearch) {
                // 'Resolved To' search results restricts Inactive EC / Issue objects
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strECStateClose);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_AND);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strECStateComplete);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_AND);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strECStateReject);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_AND);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strIssueStateClosed);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }

                if(!strState.equals(DomainConstants.QUERY_WILDCARD)) {

                    if(bSatisfiedItemSearch) {
                        sbWhereExp.append(SYMB_AND);
                    }
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_MATCH);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strState);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }
            }

            String strFilteredExpression = getFilteredExpression(context,programMap);

            if ((strFilteredExpression != null)
                    && !("null".equalsIgnoreCase(strFilteredExpression))
                    && !strFilteredExpression.equals("")) {
                    if (bStart) {
                            sbWhereExp.append(SYMB_OPEN_PARAN);
                            bStart = false;
                    } else {
                            sbWhereExp.append(SYMB_AND);
                    }
                    sbWhereExp.append(strFilteredExpression);
            }
            if (!bStart) {
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
            }

           // returning empty maplist since 'Close' 'Complete' and 'Reject' state EC objects
           //and 'Closed' state Issue objects should not be added as a Satisfied Items to an Engineering Change
           if(bInactiveECAndIssue){
               mapList = new MapList();
           } else {
                   mapList = DomainObject.findObjects(context,
                                                      strType,
                                                      strName,
                                                      strRevision,
                                                      strOwner,
                                                      strVault,
                                                      sbWhereExp.toString(),
                                                      "",
                                                      true,
                                                      (StringList) slSelect,
                                                      sQueryLimit);
           }
        } catch (Exception excp) {
                excp.printStackTrace(System.out);
                throw excp;
        }
        return mapList;
    }

    /**
     * The function to filter the object selection and apppend the default query in the
     * where clause.
     * @param context     the eMatrix <code>Context</code> object
     * @param programMap  a Map with input for constructing where clause filter
     * @return            String after constructing the Where clause appropriately
     * @throws            Exception when problems occurred in the AEF
     * @since             Common 10-6
     */
    protected static String getFilteredExpression(Context context,Map programMap)
        throws Exception {

        String strMode = (String) programMap.get(Search.REQ_PARAM_MODE);
        String strObjectId =
                (String) programMap.get(Search.REQ_PARAM_OBJECT_ID);
        String strSrcDestRelNameSymb =
                (String) programMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME);
        String strIsTo = (String) programMap.get(Search.REQ_PARAM_IS_TO);
        String strDQ = (String) programMap.get(Search.REQ_PARAM_DEFAULT_QUERY);
        String strMidDestRelNameSymb =
                (String) programMap.get(Search.REQ_PARAM_MID_DEST_REL_NAME);
        String strSrcMidRelNameSymb =
                (String) programMap.get(Search.REQ_PARAM_SRC_MID_REL_NAME);

        String strMidDestRelName = "";
        if (strMidDestRelNameSymb != null
               && !strMidDestRelNameSymb.equals("")
               && !("null".equalsIgnoreCase(strMidDestRelNameSymb))) {
               strMidDestRelName = PropertyUtil.getSchemaProperty(context,strMidDestRelNameSymb);
        }

        String strSrcMidRelName = "";
        if (strSrcMidRelNameSymb != null
              && !strSrcMidRelNameSymb.equals("")
              && !("null".equalsIgnoreCase(strSrcMidRelNameSymb))) {
              strSrcMidRelName = PropertyUtil.getSchemaProperty(context,strSrcMidRelNameSymb);
        }

        String strSrcDestRelName = "";
        if (strSrcDestRelNameSymb != null
              && !strSrcDestRelNameSymb.equals("")
              && !("null".equalsIgnoreCase(strSrcDestRelNameSymb))) {
              strSrcDestRelName = PropertyUtil.getSchemaProperty(context,strSrcDestRelNameSymb);
        }

        StringBuffer sbWhereExp = new StringBuffer(50);
        //sbWhereExp.append(SYMB_OPEN_PARAN);
        boolean bStart = true;

        String strCommand = (String) programMap.get(Search.REQ_PARAM_COMMAND);
        // If add exisitng Object of type other that Part
        if ((strCommand != null)
                && !strCommand.equals("")
                && !("null".equalsIgnoreCase(strCommand))) {

                if ((strMode.equals(Search.ADD_EXISTING))
                    && (strObjectId != null)
                    && (!strObjectId.equals(""))
                    && !("null".equalsIgnoreCase(strObjectId))) {
                    bStart = false;
                    sbWhereExp.append(SYMB_OPEN_PARAN);

                    /* Case where we don't have an intermediate relationship */

                    if (strIsTo.equalsIgnoreCase("TRUE")) {
                            //sbWhereExp.append("!('to[");
                            sbWhereExp.append(SYMB_NOT);
                            sbWhereExp.append(SYMB_OPEN_PARAN);
                            sbWhereExp.append(SYMB_QUOTE);
                            sbWhereExp.append(SYMB_TO);
                            sbWhereExp.append(SYMB_OPEN_BRACKET);
                            sbWhereExp.append(strSrcDestRelName);
                            //sbWhereExp.append("].from.");
                            sbWhereExp.append(SYMB_CLOSE_BRACKET);
                            sbWhereExp.append(SYMB_DOT);
                            sbWhereExp.append(SYMB_FROM);
                            sbWhereExp.append(SYMB_DOT);
                            sbWhereExp.append(DomainConstants.SELECT_ID);
                            //sbWhereExp.append("'==");
                            sbWhereExp.append(SYMB_QUOTE);
                            sbWhereExp.append(SYMB_EQUAL);
                            sbWhereExp.append(strObjectId);
                            //sbWhereExp.append(")");
                            sbWhereExp.append(SYMB_CLOSE_PARAN);
                    } else {
                            //sbWhereExp.append("!('from[");
                            sbWhereExp.append(SYMB_NOT);
                            sbWhereExp.append(SYMB_OPEN_PARAN);
                            sbWhereExp.append(SYMB_QUOTE);
                            sbWhereExp.append(SYMB_FROM);
                            sbWhereExp.append(SYMB_OPEN_BRACKET);
                            sbWhereExp.append(strSrcDestRelName);
                            //sbWhereExp.append("].to.");
                            sbWhereExp.append(SYMB_CLOSE_BRACKET);
                            sbWhereExp.append(SYMB_DOT);
                            sbWhereExp.append(SYMB_TO);
                            sbWhereExp.append(SYMB_DOT);
                            sbWhereExp.append(DomainConstants.SELECT_ID);
                            //sbWhereExp.append("'==");
                            sbWhereExp.append(SYMB_QUOTE);
                            sbWhereExp.append(SYMB_EQUAL);
                            sbWhereExp.append(strObjectId);
                            //sbWhereExp.append(")");
                            sbWhereExp.append(SYMB_CLOSE_PARAN);

                    }

                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                    /* To remove the duplicate object ids, from Add Existing sub types... */
                    sbWhereExp.append(SYMB_AND);
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_ID);
                    //sbWhereExp.append("!='");
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strObjectId);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);

                }
            }
            if (strDQ != null
                && !strDQ.equals("")
                && !("null".equalsIgnoreCase(strDQ))) {
                if (!bStart) {
                    sbWhereExp.append(SYMB_AND);
                    bStart = false;
                }
                sbWhereExp.append(SYMB_OPEN_PARAN);
                sbWhereExp.append(strDQ);
                sbWhereExp.append(SYMB_CLOSE_PARAN);
            }

        String strFilteredExp = "";
        String strWhereExp = sbWhereExp.toString();

        if (strWhereExp != null
            && !strWhereExp.equals("")
            && !("null".equalsIgnoreCase(strWhereExp))) {
            strFilteredExp = strWhereExp;
        }
        return strFilteredExp;
    }


    /**
     * Gets the Member Lists according to the search criteria.
     * @param context           the eMatrix <code>Context</code> object
     * @param args              contains a Map with the following input arguments or entries:
     *    type                  the admin 'type'of the object to search for
     *    Name                  the 'name' or 'name pattern' to search for
     *    Description           the 'description' pattern to search for
     *    MemberListOwner       the 'owner' pattern to search for
     *    queryLimit.           the search query limit
     *    vaultOption           the vault search option
     *    vaultName             the vault 'name' pattern
     * @return                  a MapList containing Member List
     * @throws                  Exception if the operation fails.
     * @since                   Common 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMemberLists(matrix.db.Context context, String[] args) throws Exception {
        StringBuffer whereClause = null;
        MapList lists            = new MapList();
        try {
            if (args.length == 0 ) {
                throw new IllegalArgumentException();
            }
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            //Retrieve search criteria
            String type           = (String)paramMap.get("type");
            String listName       = (String)paramMap.get("Name");
            String strDesc        = (String)paramMap.get("Description");
            String owner          = (String)paramMap.get("MemberListOwner");
            String sQueryLimit    = (String)paramMap.get("queryLimit");
            String strVaultOption    = (String)paramMap.get("vaultOption");
            String sChangeResponsibilityId  = (String)paramMap.get("objectId");
            String sRelMemberListName       = PropertyUtil.getSchemaProperty(context, "relationship_MemberList");
            String sECRECOUI                = (String)paramMap.get("ecrEcoUi");
            /**  Newly added To get Plant Id */
            String sPlantId                =(String)paramMap.get("PlantId");
			/** End */
            whereClause = new StringBuffer(250);

            if(listName == null || "".equals(listName)) {
                listName = DomainConstants.QUERY_WILDCARD;
            }
            if(owner == null || "".equals(owner)) {
                owner = DomainConstants.QUERY_WILDCARD;
            }

            if(strDesc != null && !(strDesc.equalsIgnoreCase("*")) && (!strDesc.equals(""))
                                    && !("null".equalsIgnoreCase(strDesc))) {
                if (whereClause.length() > 0) {
                    whereClause.append(" && ");
                }
                whereClause.append(SYMB_OPEN_PARAN);
                whereClause.append(DomainConstants.SELECT_DESCRIPTION);
                whereClause.append(SYMB_MATCH);
                whereClause.append(SYMB_QUOTE);
                whereClause.append(strDesc);
                whereClause.append(SYMB_QUOTE);
                whereClause.append(SYMB_CLOSE_PARAN);
            }

            if (sECRECOUI != null && sECRECOUI.equalsIgnoreCase("true") && sChangeResponsibilityId != null && !sChangeResponsibilityId.equals("") && !sChangeResponsibilityId.equals("null") && !sChangeResponsibilityId.equals(" "))
            {
                if (whereClause.length() > 0) {
                    whereClause.append(" && ");
                }
                whereClause.append(SYMB_OPEN_PARAN);
                whereClause.append("to["+sRelMemberListName+"].from.id");
                whereClause.append(SYMB_EQUAL);
                whereClause.append(SYMB_QUOTE);
                whereClause.append(sChangeResponsibilityId);
                whereClause.append(SYMB_QUOTE);
                whereClause.append(SYMB_CLOSE_PARAN);
            }
           /**  Newly added To get MemberList Based On Manufacturing Responsibility */
            if(sPlantId!= null && sPlantId.length()!=0)
			{
			      if (whereClause.length() > 0) {
				   whereClause.append(" && ");
                }
				 whereClause.append(SYMB_OPEN_PARAN);
                whereClause.append("to["+sRelMemberListName+"].from.id");
                whereClause.append(SYMB_EQUAL);
                whereClause.append(SYMB_QUOTE);
                whereClause.append(sPlantId);
                whereClause.append(SYMB_QUOTE);
                whereClause.append(SYMB_CLOSE_PARAN);
			}
/** End */
            // build the object selects
            StringList objectSelects = new StringList(1);
            objectSelects.addElement(SELECT_ID);
            // selecting the appropriate vaults depending uppon the options selected
            String strVaults = "";
            if(strVaultOption != null && (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)||
                    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||
                    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)) ) {
                strVaults = PersonUtil.getSearchVaults(context,false,strVaultOption);
            } else if(strVaultOption != null && strVaultOption.equalsIgnoreCase(SELECTED)) {
                strVaults = (String)paramMap.get("vaultName");
            }
            lists =  DomainObject.findObjects(context,                             // eMatrix context
                                              EngineeringChange.TYPE_MEMBER_LIST,  // type pattern
                                              listName,                            // name pattern
                                              DomainConstants.QUERY_WILDCARD,      // revision pattern
                                              owner,                               // owner pattern
                                              strVaults,                           // Vault Pattern
                                              whereClause.toString(),              // where expression
                                              null,
                                              false,                               // expand type
                                              objectSelects,
                                              Short.parseShort(sQueryLimit));

        } catch (Exception ex){
            ex.printStackTrace(System.out);
        }
        return lists;
    }

    /**
     *The method to get Organization of the Route Template type
     * @param context   the eMatrix <code>Context</code> object
     * @param args      contains a Map with Object Id as input argument
     * @return          StringList containing Organization of the Route Template object
     * @throws          Exception if the operation fails.
     * @since           Common 10-6
     */
    public StringList getRouteTemplateOrganization(Context context,String[] args) throws Exception {
        return(getOrganization(context,args,RELATIONSHIP_ROUTE_TEMPLATES));
    }

    /**
     *The method to get Organization of the Member List type
     * @param context   the eMatrix <code>Context</code> object
     * @param args      contains a Map with object id as an input argument
     * @return          StringList containing Organization of the Member List object
     * @throws          Exception if the operation fails.
     * @since           Common 10-6
     */
    public StringList getMemberListOrganization(Context context,String[] args) throws Exception {
        return(getOrganization(context,args,EngineeringChange.RELATIONSHIP_MEMBER_LIST));
    }


    /**
     * The method to get the Organization of the specified object
     * @param context   the eMatrix <code>Context</code> object
     * @param args      contains a MapList with object id as an input argument
     * @param relName   the relationship name for which the Organization name required
     * @return          StringList containing Organization
     * @throws          Exception if the operation fails.
     * @since           Common 10-6
     */
    protected StringList getOrganization(Context context,String[] args, String relName) throws Exception {
        StringList organization = new StringList();
        MapList orgNameList = new MapList();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)paramMap.get("objectList");
        String strOrgName = "to["+relName+"].from."+SELECT_NAME;
        String strOrgType = "to["+relName+"].from."+SELECT_TYPE+".derived";
        Map objMap = null;
        String name = "", typeName="";
        Map orgNameMap = null;
        DomainObject boGeneric = null;
        StringList objectSelects = new StringList();
        objectSelects.add(strOrgName);
        objectSelects.add(strOrgType);
        objectSelects.add(DomainConstants.SELECT_ID);
        int intSize = objList.size();
        String[] strObjectIds = new String[intSize];
        int counter = 0;
        for(int i = 0;i < objList.size();i++) {
            objMap = (Map)objList.get(i);
            String objectId = (String)objMap.get(SELECT_ID);
            strObjectIds[counter] = objectId;
            counter++;
        }
        boGeneric = DomainObject.newInstance(context);
        MapList mapListValues = DomainObject.getInfo(context,strObjectIds,objectSelects);
        Iterator mapIteratorValues = mapListValues.iterator();

        while(mapIteratorValues.hasNext()) {
            objMap = (Map)mapIteratorValues.next();
            String objectId = (String)objMap.get(DomainConstants.SELECT_ID);
            boGeneric.setId(objectId);
            if(objMap != null) {
                typeName = (String)objMap.get(strOrgType);
                if(typeName !=null && typeName.trim().length()!=0) {
                    if(typeName.equals(DomainConstants.TYPE_ORGANIZATION)) {
                    name = (String)objMap.get(strOrgName);
                    organization.addElement(name);
                } else {
                    organization.addElement(EMPTY_STRING);
                    }
                }else {
                    organization.addElement(EMPTY_STRING);
                    }
            }
        }
        return organization;
    }

   /**
     * Updates the Reported Against field in Engineering change WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args     contains a MapList with the following as input arguments or entries:
     *   objectId      holds the context Engineering Change object Id
     *   New Value     holds the newly selected Reported Against Object Id
     * @throws         Exception if the operations fails
     * @since          Common 10-6
     **
     */
    public void updateReportedAgainstEC (Context context, String[] args) throws Exception {
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");

            //Relationship name
            String strRelationshipReportedAgainstEC    = DomainConstants.RELATIONSHIP_REPORTED_AGAINST_EC;

            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");
            String strNewReportedAgainstObjId = (String)paramMap.get("New Value");

            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList();
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);


            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //setting the context to the Engineering Change object id
            setId(strECObjectId);

            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = getRelatedObjects(context,
                                                    strRelationshipReportedAgainstEC,
                                                    DomainConstants.QUERY_WILDCARD,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);

            String strRelationshipId     = "";
            String strReportedAgainstId  = "";
            int ecReportedAgainst = relationshipIdList.size();
            if(ecReportedAgainst>0) {
                //Getting the realtionship ids from the list
                strRelationshipId = (String)((Hashtable)relationshipIdList.get(0)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                //Getting the Reported against id from the list
                strReportedAgainstId = (String)((Hashtable)relationshipIdList.get(0)).get(DomainConstants.SELECT_ID);
            }

            //Connecting the Engineering Change with the new Reported Against object with relationship Reported Against EC
            // update if old and new ids are not equal
            if(!strNewReportedAgainstObjId.equals(strReportedAgainstId)){
                if(ecReportedAgainst>0){
                    //Disconnecting the existing relationship
                    DomainRelationship.disconnect(context, strRelationshipId);
                }
                //Instantiating DomainObject with the new Reported Against Item's object id
                DomainObject domainObjectToType = newInstance(context, strNewReportedAgainstObjId);
                DomainRelationship.connect(context,
                                           this,
                                           strRelationshipReportedAgainstEC,
                                           domainObjectToType);
             }
        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
    }

    /**
     * Gets the MapList containing Route Template according to the search criteria.
     * @param context      the eMatrix <code>Context</code> object
     * @param args         contains a MapList with the following as input arguments or entries:
     *     Name            'name' or 'name pattern' to search for
     *     Availability    'availability' to search for
     *     queryLimit      the search querylimit
     *     Purpose         'Purpose' to earch for
     *     vaultOption     the search vault Option
     *     vaultName       the 'name' of vault to search for
     *     State           the 'state' to search for
     *     orgTemplates    the 'owning organization' to search for
     * @return             MapList containing Route Template
     * @throws             Exception if the operation fails.
     * @since              Common 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  MapList getRouteTemplate(matrix.db.Context context, String[] args) throws Exception {
        MapList routeTemplate = new MapList();
        try {
            if (args.length == 0 ){ throw new IllegalArgumentException();}
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            //Retrieve search criteria
            String routeTemplateName = (String)paramMap.get("Name");
            String availability      = (String)paramMap.get("Availability");
            String sQueryLimit       = (String)paramMap.get("queryLimit");
            String purpose           = (String)paramMap.get("Purpose");
            String strVaultOption    = (String)paramMap.get("vaultOption");
            String state             = (String)paramMap.get("State");
            String orgTemplates      = (String)paramMap.get("orgTemplates");
            String sObjectId         = (String)paramMap.get("objectId");
            String sECRECOUI                = (String)paramMap.get("ecrEcoUi");
            /**  Newly added To get Plant Id */
              String sPlantId          = (String)paramMap.get("PlantId");
			/** End */
            if(routeTemplateName == null || "".equals(routeTemplateName)) {
                routeTemplateName = QUERY_WILDCARD;
            }

            StringBuffer busWhere = new StringBuffer(250);
            //check for state
            if(state !=null && state.trim().length()!=0 && !state.trim().equals(DomainConstants.QUERY_WILDCARD) && !state.equals("All")) {
                busWhere.append("&& (current == \"");
                busWhere.append(state).append("\")");
            }

            // need to build a where clause for the options selected
            if(availability.equals(ENTERPRISE)) {
                busWhere.append("&& to[");
                busWhere.append(DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES);
                busWhere.append("].from.type.derived == ").append(DomainConstants.TYPE_ORGANIZATION);
            } else if(availability.equals(USER)) {
                busWhere.append("&& to[").append(DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES).append("].from.type == ").append(DomainConstants.TYPE_PERSON);
            } else if(availability.equals(REGION)) {
                busWhere.append("&& to[").append(DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES).append("].from.type == ").append(DomainConstants.TYPE_REGION);
            }

            if(purpose != null && purpose.trim().length() != 0 && !purpose.equals(DomainConstants.QUERY_WILDCARD)) {
                busWhere.append("&& (attribute[").append(DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE).append("]==\"").append(purpose).append("\")");
            }

            // build the object selects
            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_NAME);

            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
            com.matrixone.apps.common.Company company = person.getCompany(context);

            if(orgTemplates != null && "true".equalsIgnoreCase(orgTemplates)) {
                String objectId = (String) paramMap.get("objectId");
                if(objectId != null) {
                    DomainObject objSCO =  DomainObject.newInstance(context,objectId);
                    String busid = objSCO.getInfo(context,"to["+RELATIONSHIP_BUSINESS_UNIT_OWNS+"].from.id");
                    if(busid != null && busid.length() > 0) {
                        busWhere.append(" && ");
                        busWhere.append("(to[").append(DomainConstants.RELATIONSHIP_OWNING_ORGANIZATION).append("].from.id == ").append(busid).append(")");
                    }

                }
            } else {
                if (busWhere.length() > 0) {
                    busWhere.append(" && ");
                }
                if (sECRECOUI != null && sECRECOUI.equalsIgnoreCase("true") && sObjectId != null && !sObjectId.equals("") && !sObjectId.equals(" ") && !sObjectId.equals("null"))
                {
                    busWhere.append("(to[").append(DomainConstants.RELATIONSHIP_OWNING_ORGANIZATION).append("].from.id == ").append(sObjectId).append(")");
                }
                /**  Newly added To get Route Templates Based on ManufacturingResponsibility */
								if (sPlantId != null && !sPlantId.equals(""))
								{
											 busWhere.append("(to[").append(DomainObject.RELATIONSHIP_OWNING_ORGANIZATION).append("].from.id == ").append(sPlantId).append(")");
								}
								/**  End */
								else {
												busWhere.append("(to[").append(DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES).append("].from.id == ").append(person.getId()).append(" || to[").append(DomainObject.RELATIONSHIP_ROUTE_TEMPLATES).append("].from.id == ").append(company.getId()).append(")");
										}
            }

            String strVaults = "";
            if(strVaultOption != null && (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)||
                    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||
                    strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)) ) {
                strVaults = PersonUtil.getSearchVaults(context,false,strVaultOption);
            } else if(strVaultOption != null && strVaultOption.equalsIgnoreCase(SELECTED)) {
                strVaults = (String)paramMap.get("vaultName");
            }
            //375788
            String stateActive  = DomainConstants.STATE_ROUTE_TEMPLATE_ACTIVE;
            busWhere.append(" && latest == 'true' && current == '"+stateActive+"'");
            //End
            String strBusWhere = busWhere.toString().trim();

            if (strBusWhere.startsWith("&&")) {
                strBusWhere = strBusWhere.substring(3,strBusWhere.length());
            }
            routeTemplate = DomainObject.findObjects(context,       // eMatrix context
                                            TYPE_ROUTE_TEMPLATE,    // type pattern
                                            routeTemplateName,      // name pattern
                                            QUERY_WILDCARD,         // revision pattern
                                            QUERY_WILDCARD,         // owner pattern
                                            strVaults,              // Vault Pattern
                                            strBusWhere,               // where expression
                                            null,
                                            false,                  // expand type
                                            objectSelects,
                                            Short.parseShort(sQueryLimit));

        }catch (Exception ex){
            ex.printStackTrace(System.out);
        }
        return routeTemplate;
    }

    /**
     * Gets the Scope of Route Template.
     * @param context      the eMatrix <code>Context</code> object
     * @param args         holds a Maplist with object Id of route template as argument
     * @return             StringList containing Scope
     * @throws             Exception if the operation fails.
     * @since              Common 10-6
     */
    public StringList getRouteTemplateScope(Context context,String[] args) throws Exception {
        return(getScope(context,args,RELATIONSHIP_ROUTE_TEMPLATES));
    }

    /**
     * Gets the Scope of Member List.
     * @param context      the eMatrix <code>Context</code> object
     * @param args         holds a Maplist with object Id of Member List as argument
     * @return             StringList containing Scope
     * @throws             Exception if the operation fails.
     * @since              Common 10-6
     */
     public StringList getMemberListScope(Context context,String[] args) throws Exception {
        return(getScope(context,args,EngineeringChange.RELATIONSHIP_MEMBER_LIST));
     }


    /**
     * Gets the Scope of the object.
     * @param context      the eMatrix <code>Context</code> object
     * @param args         contains a MapList with object Id
     * @param relName      contains the relationship name for which the scope is required
     * @return             StringList containing Scope
     * @throws             Exception if the operation fails.
     * @since              Common 10-6
     */
     protected StringList getScope(Context context,String[] args, String relName) throws Exception {
         StringList scope = new StringList();
         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         MapList objList  = (MapList)paramMap.get("objectList");
         Map objMap       = null;

         String relType   = null;
         String relFromTypes = null;
         StringBuffer objectscopes = new StringBuffer();
         //Added by Infosys for RouteTemplate results page internationalization on 6/14/2005

         String strUser =          EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), SCOPE_USER);
         String strEnterprise =    EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), SCOPE_ENTERPRISE);

         int intSize = objList.size();
         String[] strObjectIds = new String[intSize];

         String str = "to["+relName+"].from.type";
         StringList strList = new StringList(str);
         for(int i = 0; i < intSize; i++) {
             objMap = (Map)objList.get(i);
             strObjectIds[i] = (String)objMap.get(SELECT_ID);
         }
         MapList mapList = DomainObject.getInfo(context,strObjectIds,strList);
         Iterator iterator = mapList.iterator();
         while(iterator.hasNext()) {
             objMap = (Map)iterator.next();
             relType = (String)objMap.get(str);
             objectscopes.setLength(0);
             if(relType != null) {
                 if(relType.indexOf(DomainConstants.TYPE_PERSON) != -1) {
                     //Modified by Infosys for RouteTemplate results page internationalization on 6/14/2005
                     objectscopes.append(strUser);
                 } else {
                     objectscopes.append(strEnterprise);
                 }
             }
             scope.addElement(objectscopes.toString());
         }
         return scope;
     }

     /**
       * Updates the Distribution List field in Engineering change WebForm.
       * @param context      the eMatrix <code>Context</code> object
       * @param args         holds the following input arguments:
       *    object Id        object Id of context Engineering Change.
       *    New Value        object Id of updated Distribution List Object
       * @throws             Exception if the operations fails
       * @since              Common 10-6
       */
     public void updateECDistributionList (Context context, String[] args) throws Exception {
         try{
             //unpacking the Arguments from variable args
             HashMap programMap = (HashMap)JPO.unpackArgs(args);
             HashMap paramMap = (HashMap)programMap.get("paramMap");

             //Relationship name
             String strRelationship    = DomainConstants.RELATIONSHIP_EC_DISTRIBUTION_LIST;

             //Getting the EC Object id and the new product object id
             String strECObjectId = (String)paramMap.get("objectId");
             String strNewToTypeObjId = (String)paramMap.get("New Value");

             //Business Objects are selected by its Ids
             StringList objectSelects = new StringList(2);
             objectSelects.addElement(DomainConstants.SELECT_NAME);
             objectSelects.addElement(DomainConstants.SELECT_ID);


             //Stringlist containing the relselects
             StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
             //setting the context to the Engineering Change object id
             setId(strECObjectId);

             //Maplist containing the relationship ids
             MapList relationshipIdList = new MapList();
             //Calling getRelatedObjects to get the relationship ids
             relationshipIdList = getRelatedObjects(context,
                                                     strRelationship,
                                                     DomainConstants.QUERY_WILDCARD,
                                                     objectSelects,
                                                     relSelectsList,
                                                     false,
                                                     true,
                                                     (short)1,
                                                     DomainConstants.EMPTY_STRING,
                                                     DomainConstants.EMPTY_STRING);

             String strRelationshipId = "";
             String strDistribListId  = "";
             int ecDistListSize = relationshipIdList.size();
             if(ecDistListSize > 0) {
                 //Getting the realtionship ids from the list
                 strRelationshipId = (String)((Hashtable)relationshipIdList.get(0)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                 //Getting the Distribution List id from the list
                 strDistribListId = (String)((Hashtable)relationshipIdList.get(0)).get(DomainConstants.SELECT_RELATIONSHIP_ID);

             }
             // update if old and new ids are not equal
             if(!strDistribListId.equals(strNewToTypeObjId)){
                 if(ecDistListSize >0){
                     //Disconnecting the existing relationship
                     DomainRelationship.disconnect(context, strRelationshipId);
                 }
                 //Instantiating DomainObject with the new Reported Against Item's object id
                 DomainObject domainObjectToType = newInstance(context, strNewToTypeObjId);
                 //Connecting the Engineering Change with the new Reported Against object with relationship Reported Against EC
                 DomainRelationship.connect(context,
                                            this,
                                            strRelationship,
                                            domainObjectToType);
             }
         }catch(Exception ex){
             throw  new FrameworkException((String)ex.getMessage());
         }
     }


     /**
       * Program HTML Output for Review and approval list
       * @param context   the eMatrix <code>Context</code> object
       * @param args      holds a Map with the following input arguments :
       *    mode          the mode in which a field need to be displayed
       *    name          the field name to be displayed
       *    PFmode        flag to find if in Printer friendly mode or not
       *    objectId      context Engineering Change objectId
       * @throws          Exception if the operations fails
       * @return          String which contains the HTML code for displaying field
       * @since           Common 10-6
       */
    public String buildReviewApproveFields (Context context, String[] args) throws Exception {

        StringBuffer outPut = new StringBuffer();
        try {
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            HashMap fieldMap   = (HashMap) programMap.get("fieldMap");
            String strLanguage =  context.getSession().getLanguage();
            String TYPE_MECO = PropertyUtil.getSchemaProperty(context,"type_MECO");

            //Getting mode parameter
            String mode        = (String)requestMap.get("mode");
			if(mode==null){
                mode="view";
            }
            String fieldName   = (String)fieldMap.get("name");
            String strPFmode   = (String)requestMap.get("PFmode");
            String strPDFrender = (String)requestMap.get("PDFrender");
			 //Start of  IR-015218
            String reportFormat = (String)requestMap.get("reportFormat");
            StringBuffer strBufNamesForExport = new StringBuffer();
            //ends

            String strURL = "../components/emxCommonSearch.jsp?formName=editDataForm&frameName=formEditDisplay&searchmode=chooser&suiteKey=Components&searchmenu=APPECSearchAddExistingChooser&searchcommand=APPSearchECRouteTemplatesCommand&restrictToPurpose=";
            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");
            //Relationship name
            String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            String strType         = DomainConstants.TYPE_ROUTE_TEMPLATE;

			//IR-044514
            String strClear = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(),"emxComponents.Button.Clear");

			DomainObject doEcrEco = new DomainObject(strECObjectId);

			String strChangeType = doEcrEco.getInfo(context, DomainConstants.SELECT_TYPE);

			String strOwningOrg = "to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id";

			if (DomainConstants.TYPE_ECR.equals(strChangeType))
			{
				strOwningOrg = "to[" + PropertyUtil.getSchemaProperty(context,"relationship_ChangeResponsibility") + "].from.id";
			}

			String owningOrgid = doEcrEco.getInfo(context,strOwningOrg);


            StringList objectSelects = new StringList();
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //setting the context to the Engineering Change object id
            setId(strECObjectId);
            //375788

            String stateActive  = DomainConstants.STATE_ROUTE_TEMPLATE_ACTIVE;
            String objWhere = "(latest == 'true') && (current == '"+stateActive+"')";
            //End
            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            //Calling getRelatedObjects to get the relationship ids

           //375788
           /* relationshipIdList = getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    objWhere, //375788
                                                    DomainConstants.EMPTY_STRING);*/

            StringList objectSelects1 = new StringList();
            objectSelects1.addElement(DomainConstants.SELECT_ID);

            String relWhere1 = "to.relationship["+RELATIONSHIP_INITIATING_ROUTE_TEMPLATE+"].to.attribute["+DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE+"]==";
            if(fieldName.equalsIgnoreCase("ApprovalList"))
            {
                relWhere1 = relWhere1+RANGE_APPROVAL;
            }
            else if(fieldName.equalsIgnoreCase("ReviewerList"))
                relWhere1 = relWhere1+RANGE_REVIEW;

            //Added to Exclude the Adhoc routes
            relWhere1 = relWhere1+ "&& to.relationship["+RELATIONSHIP_INITIATING_ROUTE_TEMPLATE+"].to.relationship["+strRelationship+"].from.id == "+strECObjectId.trim();

            MapList routeList = getRelatedObjects(context,
                    strRelationship,
                    DomainConstants.TYPE_ROUTE,
                    objectSelects1,
                    null,
                    false,
                    true,
                    (short)1,
                    DomainConstants.EMPTY_STRING,
                    relWhere1);
            if(routeList.size()>0){
                Iterator itrTemplates = routeList.iterator();

                while (itrTemplates.hasNext()) {
                    Map mpRoutes = (Map) itrTemplates.next();
                    String strTemplaeID = (String) mpRoutes.get(SELECT_ID);
                    DomainObject objRoute = (DomainObject) DomainObject.newInstance(context);
                    objRoute.setId(strTemplaeID);// Template object

                relationshipIdList = objRoute.getRelatedObjects(context,
                        RELATIONSHIP_INITIATING_ROUTE_TEMPLATE,
                        strType,
                        objectSelects,
                        relSelectsList,
                        false,
                        true,
                        (short)1,
                        DomainConstants.EMPTY_STRING,
                        "");
               }
            }
             else{
                relationshipIdList = getRelatedObjects(context,
                        strRelationship,
                        strType,
                        objectSelects,
                        relSelectsList,
                        false,
                        true,
                        (short)1,
                        objWhere, //375788
                        DomainConstants.EMPTY_STRING);
            }

            //End
            StringList relIdStrList = new StringList();
            StringList routeTemplateIdStrList = new StringList();
            StringList routeTemplateNameStrList = new StringList();
			if (DomainConstants.TYPE_ECO.equals(strChangeType)||TYPE_MECO.equals(strChangeType)){

			    if(!"true".equalsIgnoreCase(strPDFrender)) {
				outPut.append(" <script> ");
				outPut.append("function showECApprovalList() { ");
				//  359889
				/*
				outPut.append("var designResName = document.editDataForm.RDODisplay.value;");
	            outPut.append(" if (designResName == null || designResName == \"\" || designResName == \" \") {");
	            outPut.append(" alert (\""+EnoviaResourceBundle.getProperty(context,"emxComponents.Common.DesignResponsibilityAlert",RESOURCE_BUNDLE_COMPONENTS_STR,strLanguage)    +"\");");
	            outPut.append(" return false;");
	            outPut.append("}");
                outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:REL_ROUTETEMPLATE_OWNINGORGANIZATION=\" + designResName + \":ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ApprovalList&fieldNameDisplay=ApprovalListDisplay&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                */
                //375788 : added LATESTREVISION=true
                //378423 : Modified from LATESTREVISION=true to LATESTREVISION=TRUE
                outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ApprovalListOID&fieldNameDisplay=ApprovalListDisplay&submitURL=../productline/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");


                outPut.append("}");

	            outPut.append("function showECReviewerList() { ");

                // 359889
				/*
				outPut.append("var designResName = document.editDataForm.RDODisplay.value;");
	            outPut.append(" if (designResName == null || designResName == \"\" || designResName == \" \") {");
	            outPut.append(" alert (\""+EnoviaResourceBundle.getProperty(context,"emxComponents.Common.DesignResponsibilityAlert",RESOURCE_BUNDLE_COMPONENTS_STR,strLanguage)    +"\");");
	            outPut.append(" return false;");
	            outPut.append("}");
	            outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:REL_ROUTETEMPLATE_OWNINGORGANIZATION=\" + designResName + \":ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=true&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ReviewerList&fieldNameDisplay=ReviewerListDisplay&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                */
                //375788 : added LATESTREVISION=true
                //378423 : Modified from LATESTREVISION=true to LATESTREVISION=TRUE
                outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ReviewerListOID&fieldNameDisplay=ReviewerListDisplay&submitURL=../productline/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                outPut.append("}");
				outPut.append(" </script> ");
			    }
            } else {

                outPut.append(" <script> ");
                if (fieldName.equalsIgnoreCase("ApprovalList"))
                {
		            outPut.append("function showECApprovalList() { ");
		            //375788 : added LATESTREVISION=true
		            //378423 : Modified from LATESTREVISION=true to LATESTREVISION=TRUE
		            outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ApprovalListOID&fieldNameDisplay=ApprovalListDisplay&submitURL=../productline/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                }
                else
                {
                    outPut.append("function showECReviewerList() { ");
                    //375788 : added LATESTREVISION=true
                    //378423 : Modified from LATESTREVISION=true to LATESTREVISION=TRUE
		            outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ReviewerListOID&fieldNameDisplay=ReviewerListDisplay&submitURL=../productline/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                }

	            outPut.append("}");
	            outPut.append(" </script> ");
			}

            if(relationshipIdList.size()>0) {// if 1:If there is any relationship object route
                DomainRelationship relationObject = null;
                String strRoutePurposeVal = "";
                //Getting the realtionship ids and relationship names from the list
                for(int i=0;i<relationshipIdList.size();i++) {
                    relIdStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID));
                    routeTemplateIdStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID));
                    routeTemplateNameStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_NAME));
                }

                if(relIdStrList.size()>0){ //if 2: Checking for non empty relId list
                    for(int i=0;i<relIdStrList.size();i++) {// Iterating through the relationship ids
                        relationObject = new DomainRelationship((String)relIdStrList.get(i));
                       //375788
                        if(routeList.size()>0 && fieldName.equalsIgnoreCase("ApprovalList"))
                            strRoutePurposeVal = RANGE_APPROVAL;
                        else if (routeList.size()>0 && fieldName.equalsIgnoreCase("ReviewerList"))
                            strRoutePurposeVal = RANGE_REVIEW;
                        else
                            strRoutePurposeVal = relationObject.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
                        //End

                        //Modified for bug#302310,302308 by Infosys on 21 Apr 05
                        if ( fieldName.equalsIgnoreCase("ApprovalList") && strRoutePurposeVal.equalsIgnoreCase(RANGE_APPROVAL) ) {
                            if( mode==null || mode.equalsIgnoreCase("view") ) {
                                if(strPFmode != null && strPFmode.equalsIgnoreCase("true")) {
                                        outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                        outPut.append("&nbsp;");
                                        outPut.append(routeTemplateNameStrList.get(i));
                                } else {

                                    if("true".equalsIgnoreCase(strPDFrender)) {
                                        outPut.append(routeTemplateNameStrList.get(i));
                                    }
                                    else {
                                        outPut.append("<a href=\"javascript:showModalDialog('emxTree.jsp?objectId=" + routeTemplateIdStrList.get(i) + "',500,700);\">");
                                        outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                        outPut.append("&nbsp;");
                                        outPut.append(routeTemplateNameStrList.get(i));
                                        outPut.append("</a>");
                                    }
									//Start of IR-015218
                                    if(reportFormat != null && reportFormat.length() > 0){
                                        strBufNamesForExport.append(routeTemplateNameStrList.get(i));
                                    }
                                    //ends

                                }
                            }else if( mode.equalsIgnoreCase("edit") ) {
                                outPut.append("<input type=\"text\" name=\"ApprovalListDisplay");
                                outPut.append("\"size=\"20\" value=\"");
                                outPut.append(routeTemplateNameStrList.get(i));
                                outPut.append("\" readonly=\"readonly\">&nbsp;");
                                outPut.append("<input class=\"button\" type=\"button\"");
                                outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");
                                outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECApprovalList()\">");

                                outPut.append("<input type=\"hidden\" name=\"ApprovalListOID\" value=\""+ routeTemplateIdStrList.get(i) +"\"></input>");
outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ApprovalList')\">"+strClear+"</a>");
                            }
                        //Modified for bug#302310,302308 by Infosys on 21 Apr 05
                        } else if( fieldName.equalsIgnoreCase("ReviewerList") && strRoutePurposeVal.equalsIgnoreCase(
                                    RANGE_REVIEW) ) {
                            if( mode==null || mode.equalsIgnoreCase("view") ) {
                                if(strPFmode != null && strPFmode.equalsIgnoreCase("true")) {
                                    outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                    outPut.append("&nbsp;");
                                    outPut.append(routeTemplateNameStrList.get(i));
                                } else {

                                    if("true".equalsIgnoreCase(strPDFrender)) {
                                        outPut.append(routeTemplateNameStrList.get(i));
                                    }
                                    else {
                                        outPut.append("<a href=\"javascript:showModalDialog('emxTree.jsp?objectId=" + routeTemplateIdStrList.get(i) + "',500,700);\">");
                                        outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                        outPut.append("&nbsp;");
                                        outPut.append(routeTemplateNameStrList.get(i));
                                        outPut.append("</a>");
                                    }
									 //Start of IR-015218
                                    if(reportFormat != null && reportFormat.length() > 0){
                                        strBufNamesForExport.append(routeTemplateNameStrList.get(i));
                                    }
                                    //ends

                                }
                            } else if( mode.equalsIgnoreCase("edit") ) {
                                outPut.append("<input type=\"text\" name=\"ReviewerListDisplay");
                                outPut.append("\"size=\"20\" value=\"");
                                outPut.append(routeTemplateNameStrList.get(i));
                                outPut.append("\" readonly=\"readonly\">&nbsp;");
                                outPut.append("<input class=\"button\" type=\"button\"");
                                outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");
                                outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECReviewerList()\">");
                                outPut.append("<input type=\"hidden\" name=\"ReviewerListOID\" value=\""+ routeTemplateIdStrList.get(i) +"\"></input>");
outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ReviewerList')\">"+strClear+"</a>");
                            }
                        } else if(relIdStrList.size() == 1) {//When any one of Review or Approval List is present
                            if ( mode.equalsIgnoreCase("edit") ) {
                                if(fieldName.equalsIgnoreCase("ReviewerList")) {
                                    outPut.append("<input type=\"text\" name=\"ReviewerListDisplay");
                                    outPut.append("\"size=\"20\" value=\"");
                                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                                    outPut.append("<input class=\"button\" type=\"button\"");
                                    outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");

                                    outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECReviewerList()\">");
                                    //Modified by Infosys on 13 Apr 05 for Bug# 300080
                                    outPut.append("<input type=\"hidden\" name=\"ReviewerListOID\" value=\"\"></input>");
outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ReviewerList')\">"+strClear+"</a>");
                                } else if (fieldName.equalsIgnoreCase("ApprovalList")) {
                                    outPut.append("<input type=\"text\" name=\"ApprovalListDisplay");
                                    outPut.append("\"size=\"20\" value=\"");
                                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                                    outPut.append("<input class=\"button\" type=\"button\"");
                                    outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");

                                    outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECApprovalList()\">");
                                    outPut.append("<input type=\"hidden\" name=\"ApprovalListOID\" value=\"\"></input>");
outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ApprovalList')\">"+strClear+"</a>");
                                }
                            }
                        }
                    }//End of for
                }//End of if 2
            } else { //if there are no relationships fields are to be dispalyed only in edit mode
                if (fieldName.equalsIgnoreCase("ApprovalList") && mode.equalsIgnoreCase("edit") ) {
                    outPut.append("<input type=\"text\" name=\"ApprovalListDisplay");
                    outPut.append("\"size=\"20\" value=\"");
                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                    outPut.append("<input class=\"button\" type=\"button\"");
                    outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");

                    outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECApprovalList()\">");
                    outPut.append("<input type=\"hidden\" name=\"ApprovalListOID\" value=\"\"></input>");
outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ApprovalList')\">"+strClear+"</a>");
                } else if(fieldName.equalsIgnoreCase("ReviewerList") && mode.equalsIgnoreCase("edit") ) {
                    outPut.append("<input type=\"text\" name=\"ReviewerListDisplay");
                    outPut.append("\"size=\"20\" value=\"");
                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                    outPut.append("<input class=\"button\" type=\"button\"");
                    outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");

                    outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECReviewerList()\">");
                    outPut.append("<input type=\"hidden\" name=\"ReviewerListOID\" value=\"\"></input>");
outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ReviewerList')\">"+strClear+"</a>");

                }
            }

		  //Start of IR-015218
            if((strBufNamesForExport.length() > 0 )|| (reportFormat != null && reportFormat.length() > 0))
            {
                outPut = strBufNamesForExport;
            }
            //IR-015218 end
        } catch(Exception ex) {
            throw  new FrameworkException((String)ex.getMessage());
        }
        return outPut.toString();
    }

    /**
      * Updates the Review list field values in Engineering change WebForm.
      * @param context         the eMatrix <code>Context</code> object
      * @param args            holds a Map with the following input arguments:
      *    objectId            objectId of the context Engineering Change
      *    New Value           objectId of updated Review List value
      * @throws                Exception if the operations fails
      * @since                 Common 10-6
      */
    public void updateObjectRouteReview (Context context, String[] args) throws Exception {
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");

            //Relationship name
            String strRelationship              = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            String strType                      = DomainConstants.TYPE_ROUTE_TEMPLATE;
            DomainRelationship oldRelationship = null;
            DomainObject domainObjectToType = null;
            String strNewToTypeObjId        = "";
            String strRouteBasePurpose = "";
            String strTempRelRouteBasePurpose = "";
            DomainRelationship newRelationship = null;

            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");

              // modified for IR-016954
              // strNewToTypeObjId = (String)paramMap.get("New Value");
                 strNewToTypeObjId = (String)paramMap.get("New OID");
              /*commented the loop for the fix IR-016954
              if(strNewToTypeObjId == null || "".equals(strNewToTypeObjId)){
               strNewToTypeObjId = (String) paramMap.get("New OID");
              }*/

            if (strNewToTypeObjId != null && !strNewToTypeObjId.equalsIgnoreCase("")) {
                //Instantiating DomainObject with the new Route Template object id
                domainObjectToType = newInstance(context, strNewToTypeObjId);
                strRouteBasePurpose = domainObjectToType.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            }


            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //setting the context to the Engineering Change object id
            setId(strECObjectId);

            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);

            if(relationshipIdList.size()>0){
                for (int i=0;i<relationshipIdList.size();i++) {
                    //Getting the realtionship ids from the list
                    String strRelationshipId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Getting Route Object Id from the list
                    String strRouteId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID);

                    oldRelationship = new DomainRelationship(strRelationshipId);
                    strTempRelRouteBasePurpose = oldRelationship.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
                    //Modified for bug#302310,302308 by Infosys on 21 Apr 05
                    if(strTempRelRouteBasePurpose.equalsIgnoreCase(RANGE_REVIEW)) {
                        //Checking if the selected Object id is the same as the selected one and exiting the program.
                        if(strRouteId.equals(strNewToTypeObjId)) {
                            return;
                        }
                        //Disconnecting the existing relationship
                        try{
                            DomainRelationship.disconnect(context, strRelationshipId);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (domainObjectToType != null) {
                //Connecting the Engineering Change with the new Route Template object with relationship Object Route
                newRelationship = DomainRelationship.connect(context,
                                                             this,
                                                             strRelationship,
                                                             domainObjectToType);
                newRelationship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strRouteBasePurpose);
            }
        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
    }


    /**
      * Updates the Review list field values in Engineering change WebForm.
      * @param context         the eMatrix <code>Context</code> object
      * @param args            holds a Map with the following input arguments:
      *    objectId            objectId of the context Engineering Change
      *    New Value           objectId of updated Approval List value
      * @throws                Exception if the operations fails
      * @since                 Common 10-6
      */
    public void updateObjectRouteApproval (Context context, String[] args) throws Exception {
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap = (HashMap)programMap.get("paramMap");

            //Relationship name
            String strRelationship      = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            String strType              = DomainConstants.TYPE_ROUTE_TEMPLATE;
            DomainRelationship oldRelationship = null;
            DomainObject domainObjectToType = null;
            String strNewToTypeObjId = "";
            String strRouteBasePurpose = "";
            String strTempRelRouteBasePurpose = "";
            DomainRelationship newRelationship = null;

            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");
            //modified for IR-016954
            //strNewToTypeObjId = (String)paramMap.get("New Value");
            strNewToTypeObjId = (String)paramMap.get("New OID");
               /*commented the loop for the fix IR-016954
                if(strNewToTypeObjId == null || "".equals(strNewToTypeObjId)){
                   strNewToTypeObjId = (String) paramMap.get("New OID");
               }fix IR-016954 ends*/

            if (strNewToTypeObjId != null && !strNewToTypeObjId.equalsIgnoreCase("")) {
                //Instantiating DomainObject with the new Route Template object id
                domainObjectToType = newInstance(context, strNewToTypeObjId);
                strRouteBasePurpose = domainObjectToType.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            }

            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //setting the context to the Engineering Change object id
            setId(strECObjectId);

            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);

            if(relationshipIdList.size()>0){
                for (int i=0;i<relationshipIdList.size();i++) {
                    //Getting the realtionship ids from the list
                    String strRelationshipId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Getting Route Object Id from the list
                    String strRouteId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID);


                    oldRelationship = new DomainRelationship(strRelationshipId);
                    strTempRelRouteBasePurpose = oldRelationship.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
                    //Modified for bug#302310,302308 by Infosys on 21 Apr 05
                    if(strTempRelRouteBasePurpose.equalsIgnoreCase(RANGE_APPROVAL)) {
                        //Checking if the selected Object id is the same as the selected one and exiting the program.
                        if(strRouteId.equals(strNewToTypeObjId)) {
                            return;
                        }
                        //Disconnecting the existing relationship
                        DomainRelationship.disconnect(context, strRelationshipId);
                    }
                }
            }

            if (domainObjectToType != null) {
                //Connecting the Engineering Change with the new Route Template object with relationship Object Route
                newRelationship = DomainRelationship.connect(context,
                                                             this,
                                                             strRelationship,
                                                             domainObjectToType);
                newRelationship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strRouteBasePurpose);
            }

        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
    }

    /**
     * Closing an EC from the Properties Page .
     * @param context         the eMatrix <code>Context</code> object
     * @param args            contains a Map with the following as Input arguments:
     *    ECReasonForClosure  comments for closing the Engineering Change
     *    objectId            context Engineering change object Id
     * @return                String  for setting the state of an EC to 'Close' state
     * @throws                FrameworkException if the operation fails
     * @since                 Common 10-6
     */
    public String closeEngineeringChange (Context context,String[] args) throws FrameworkException {

        String strFinalState = "";
        String objectId = "";
        boolean bException = false;
        try {

            //change context to user agent
            ContextUtil.pushContext(context);
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strECReasonForClosure = (String)programMap.get("ECReasonForClosure");
            objectId = (String)programMap.get("objectId");

            setId(objectId);
            //Get the policy of the current business object
            Policy currentPolicy = getPolicy(context);
            //Getting the policy's name from the Policy object
            String strCurrentPolicyName = currentPolicy.getName();

            // Start of write transaction
            ContextUtil.startTransaction(context, true);

            //creating Map for the attribute 'Reason For Closure'
            HashMap mapECAttribClose = new HashMap();
            mapECAttribClose.put(DomainConstants.ATTRIBUTE_REASON_FOR_CLOSURE,
                    strECReasonForClosure);
            //setting the atributes for the Object
            setAttributeValues(context, mapECAttribClose);

            //Fetching the current state of the EC
            String strCurState = getInfo(context, DomainConstants.SELECT_CURRENT);
            //Instantiating a SignatureList() object to hold the list of signatures
            SignatureList signList = new SignatureList();
            //Setting the current posn to 0
            int iCurStatePosn = 0;
            //Variable to hold the next state
            String strNextState = null;
            //MQL to set "trigger off"
            MqlUtil.mqlCommand(context, "trigger $1", true, "off");

	    try {
		//Calling getStates() which returns a StateList
            StateList stateList = getStates(context);
            String strIgnoreComment = null;
            int iSignatureListSize = 0;
            int iTempIndex = 0;
            String strAutoPromoteChk = null;

            //getting the string for close state
            String strCloseState=((State)stateList.elementAt(stateList.size()-2)).getName();
            //Looping till the close state to ignore signatures and promote

            while (!strCurState.equalsIgnoreCase(strCloseState)) {
                //Finding the position of the current state in the stateList
                for (int i = iCurStatePosn; i < stateList.size(); i++) {
                    if ((((State)stateList.elementAt(i)).getName()).equalsIgnoreCase(strCurState))
                        iCurStatePosn = i;
                }
                //Getting the next state
                strNextState = (String)((State)stateList.elementAt(iCurStatePosn
                        + 1)).getName();
                //Getting a list of Signatures between the current state and the next state
                signList = getSignatures(context, strCurState, strNextState);
                //Promoting if there are no signatures or ignoring signatures if there are
                if (signList.isEmpty()) {
                    promote(context);
                } else {
                    iSignatureListSize = signList.size();
                    strAutoPromoteChk = MqlUtil.mqlCommand(context,"print policy $1 select $2 dump", true, strCurrentPolicyName, "state[" +  strCurState + "].autopromote");
                    //Looping through the list of Signatures to ignore all the signatures
                    Signature signature = null;
                    for (int iCount = 0; iCount < iSignatureListSize; iCount++) {
                        //Getting the Signature
                        signature = (Signature)signList.get(iCount);
                        //getting Ignore comment from String Resource properties file
                        strIgnoreComment = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Signature.IgnoreComment");
                        //Ignoring this signature
                        ignoreSignature(context, signature, strIgnoreComment);
                    }
                    if (!strAutoPromoteChk.equalsIgnoreCase("TRUE")) {
                        promote(context);
                    }
                }
                //Getting the current state again
                strCurState = getInfo(context, DomainConstants.SELECT_CURRENT);
            }//end of while

	    } finally {
		//Command to set "trigger on"
		MqlUtil.mqlCommand(context, "trigger $1", true, "on");
	    }

            //Commit of transaction
            ContextUtil.commitTransaction(context);
            strFinalState = getInfo(context, DomainConstants.SELECT_CURRENT);

        } catch (Exception ex) {
            // Transaction will be aborted in case of any exception
            ContextUtil.abortTransaction(context);
            bException=true;
            /*
             * The exception ex is thrown back as FrameworkException. This can be
             * modified to handle some specific message from back end and do
             * internationalization.
             */
            throw  new FrameworkException(ex);
        }
        finally{
            //change context back to original user in any case
            ContextUtil.popContext(context);
        }

        try {
            // if there is no exception generated while promoting it the final state then notify to owner & memeberList
            if(!bException) {
                notifyEngineeringChangePromote(context,objectId,strFinalState);
            }
        }
        catch(Exception e) {
        }

        // Returning the current state
        return  strFinalState;

    }


    /**
     * Rejecting an EC from the Properties Page .
     * @param context         the eMatrix <code>Context</code> object
     * @param args            contains a Map with the following as Input arguments:
     *    ECReasonForReject   comments for closing the Engineering Change
     *    objectId            context Engineering change object Id
     * @return                String  for setting the state of an EC to 'Reject' state
     * @throws                FrameworkException if the operation fails
     * @since                 Common 10-6
     */

    public String rejectEngineeringChange (Context context,String[] args) throws FrameworkException {

        String strFinalState = "";
        String objectId = "";
        boolean bException = false;
        try {
               //change context to user agent
               ContextUtil.pushContext(context);
               //unpacking the Arguments from variable args
               HashMap programMap = (HashMap)JPO.unpackArgs(args);
               String strECReasonForRejection  = (String)programMap.get("ECReasonForRejection");
               objectId    = (String)programMap.get("objectId");

               //PDCM Check for the sub changes
               boolean isECHInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionEnterpriseChange",false,null,null);
               if(isECHInstalled) {
                   Boolean canCancel = (Boolean)JPO.invoke(context, "emxChangeTask", null, "canCancelChange", JPO.packArgs(programMap), Boolean.class);
                   if (!canCancel.booleanValue()) {
                       String strNotice = EnoviaResourceBundle.getProperty(context,
                                       "emxEnterpriseChangeStringResource",
                                       context.getLocale(),
                                       "emxEnterpriseChange.Pdcm.CanNotCancelChangeProcess");
                       throw new FrameworkException(strNotice);
                   }
               }

               setId(objectId);
               //Get the policy of the current business object
               Policy currentPolicy = getPolicy(context);
               //Getting the policy's name from the Policy object
               String strCurrentPolicyName = currentPolicy.getName();

               // Start of write transaction
               ContextUtil.startTransaction(context, true);

               //creating Map for the attributes 'Reason for Rejection'
               HashMap mapECAttribReject = new HashMap();
               mapECAttribReject.put(DomainConstants.ATTRIBUTE_REASON_FOR_REJECTION,
                       strECReasonForRejection);
               //setting the atributes for the Object
               setAttributeValues(context, mapECAttribReject);

               //Fetching the current state of the EC
               String strCurState = getInfo(context, DomainConstants.SELECT_CURRENT);
               //Instantiating a SignatureList() object to hold the list of signatures
               SignatureList signList = new SignatureList();
               //Setting the current posn to 0
               int iCurStatePosn = 0;
               //Variable to hold the next state
               String strNextState = null;
               //MQL to set "trigger off"
               MqlUtil.mqlCommand(context, "trigger $1", true, "off");

               try{ // Added for IR-053899V6R2011x
                   //Calling getStates() which returns a StateList()
                   StateList stateList = getStates(context);
                   String strIgnoreComment = null;
                   int iSignatureListSize = 0;
                   int iTempIndex = 0;
                   String strAutoPromoteChk = null;

                   //getting the string for Reject state
                   String strRejectState=((State)stateList.elementAt(stateList.size()-1)).getName();
                   //Looping till the Reject state to ignore signatures and promote

                   while (!strCurState.equalsIgnoreCase(strRejectState)) {
                       //Finding the position of the current state in the stateList
                       for (int i = iCurStatePosn; i < stateList.size(); i++) {
                           if ((((State)stateList.elementAt(i)).getName()).equalsIgnoreCase(strCurState))
                               iCurStatePosn = i;
                       }
                       //Getting the next state
                       strNextState = (String)((State)stateList.elementAt(iCurStatePosn
                               + 1)).getName();
                       //Getting a list of Signatures between the current state and the next state
                       signList = getSignatures(context, strCurState, strNextState);
                       //Promoting if there are no signatures or ignoring signatures if there are
                       if (signList.isEmpty()) {
                           promote(context);
                       } else {
                           iSignatureListSize = signList.size();
                           strAutoPromoteChk = MqlUtil.mqlCommand(context,"print policy $1 select $2 dump", true, strCurrentPolicyName, "state[" +  strCurState + "].autopromote");
                           //Looping through the list of Signatures to ignore all the signatures
                           Signature signature = null;
                           for (int iCount = 0; iCount < iSignatureListSize; iCount++) {
                               //Getting the Signature
                               signature = (Signature)signList.get(iCount);
                               //getting Ignore comment from String Resource properties file
                               strIgnoreComment = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Signature.IgnoreComment");
                               //Ignoring signature
                               ignoreSignature(context, signature, strIgnoreComment);
                           }
                           if (!strAutoPromoteChk.equalsIgnoreCase("TRUE")) {
                               promote(context);
                           }
                       }
                       //Getting the current state again
                       strCurState = getInfo(context, DomainConstants.SELECT_CURRENT);
                   }//end of while
               } finally { // Added for IR-053899V6R2011x
                   //Command to set "trigger on"
                   MqlUtil.mqlCommand(context, "trigger $1", true, "on");
               } // Added for IR-053899V6R2011x
               //Commit of transaction
               ContextUtil.commitTransaction(context);
               strFinalState = getInfo(context, DomainConstants.SELECT_CURRENT);

           } catch (Exception ex) {
               // Transaction will be aborted in case of any exception
               ContextUtil.abortTransaction(context);
               bException=true;
               /*
                * The exception ex is thrown back as FrameworkException. This can be
                * modified to handle some specific message from back end and do
                * internationalization.
                */
               throw  new FrameworkException(ex);
           }
        finally{
            //change context back to original user in any case
            ContextUtil.popContext(context);
        }

        try {
            // if there is no exception generated while promoting it the final state then notify to owner & memeberList
            if(!bException) {
                notifyEngineeringChangePromote(context,objectId,strFinalState);
            }
        }
        catch(Exception e) {
        }

        // Returning the current state
        return  strFinalState;
    }

    /**
      * Access program for showing 'Close' action link to Owner and Approval List only
      * @param context the eMatrix <code>Context</code> object
      * @param args    holds the following input arguments:
      *            0 - HashMap containing one String entry for key "objectId"
      * @return        a boolean value to display Close link in EC properties page
      * @throws        Exception if the operations fails
      * @since         Common 10-6
      */
    public boolean showCloseToApprovalList (Context context, String[] args) throws Exception {

        boolean showCloseCommand      = false;
        boolean closeStateExists      = false;
        try{
            HashMap programMap        = (HashMap)JPO.unpackArgs(args);
            String objectId           = (String)programMap.get("objectId");
            DomainObject contextECObj = DomainObject.newInstance(context,objectId);
            String contextECOwner     = contextECObj.getInfo(context,DomainConstants.SELECT_OWNER);
            int recursionLevel        = 1;
            Pattern typePattern       = new Pattern(DomainConstants.TYPE_ROUTE_TEMPLATE);
            Pattern relPattern        = new Pattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
            StringBuffer whereExpn    = new StringBuffer();
            whereExpn.append("attribute[");
            whereExpn.append(DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            whereExpn.append("]==");
            //Modified for bug#302310,302308 by Infosys on 21 Apr 05
            whereExpn.append(RANGE_APPROVAL);
            StringList objectSelects  = new StringList(1);
            objectSelects.add(DomainConstants.SELECT_ID);

            // querying for 'Close' state in context object policy
            String strContextPolicy   = contextECObj.getPolicy(context).getName();
            String strClose           = FrameworkUtil.lookupStateName(context, strContextPolicy,EngineeringChange.EC_STATE_CLOSE);

            //Checking if close state exists in the current policy
            if( strClose != null && !"".equalsIgnoreCase(strClose)){
                closeStateExists = true;
            }

            //Getting the related Approval Route Templates
            MapList relatedApprovalList = contextECObj.getRelatedObjects(context,
                                                                         relPattern.getPattern(),
                                                                         typePattern.getPattern(),
                                                                         false,
                                                                         true,
                                                                         recursionLevel,
                                                                         objectSelects,
                                                                         new StringList(),
                                                                         whereExpn.toString(),
                                                                         DomainConstants.EMPTY_STRING,
                                                                         DomainConstants.EMPTY_STRING,
                                                                         DomainConstants.EMPTY_STRING,
                                                                         null);

            String strContextUser = context.getUser();

            if (closeStateExists && strContextUser.equals(contextECOwner)){
                showCloseCommand = true;
            } else if (closeStateExists && relatedApprovalList.size()>0){
                //Checking if there is any Approval Route Template connected
                String approvRouteId = (String)((Hashtable)relatedApprovalList.get(0)).get(DomainConstants.SELECT_ID);

                StringList rtObjectSelects = new StringList(1);
                rtObjectSelects.add(DomainConstants.SELECT_NAME);

                RouteTemplate RouteTemplateObj = (RouteTemplate)DomainObject.newInstance(context,approvRouteId,DomainConstants.TYPE_ROUTE_TEMPLATE);
                MapList routeTemplateMembers   = RouteTemplateObj.getRouteTemplateMembers(context,rtObjectSelects,new StringList(),false);

                Iterator routeTemplateMembersItr = routeTemplateMembers.iterator();
                Map routeTemplateMembersMap      = null;
                String userName                  = null;

                while (routeTemplateMembersItr.hasNext()) {
                    routeTemplateMembersMap = (Map) routeTemplateMembersItr.next();
                    userName = (String)routeTemplateMembersMap.get("name");
                    if (strContextUser.equals(userName)){
                        showCloseCommand = true;
                        break;
                    }
                }
            }

        } catch (Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
        //returning the acces boolean value
        return showCloseCommand;
    }

    /**
      * Access program for showing 'Reject' action link to Owner and Approval List only
      * @param context the eMatrix <code>Context</code> object
      * @param args    holds the following input arguments:
      *            0 - HashMap containing one String entry for key "objectId"
      * @throws        Exception if the operations fails
      * @since         Common 10-6
      */
    public boolean showRejectToApprovalList (Context context, String[] args) throws Exception {

        boolean rejectStateExists     = false;
        boolean showRejectCommand     = false;

        try{
            HashMap programMap        = (HashMap)JPO.unpackArgs(args);
            String objectId           = (String)programMap.get("objectId");
            DomainObject contextECObj = DomainObject.newInstance(context,objectId);
            String contextECOwner     = contextECObj.getInfo(context,DomainConstants.SELECT_OWNER);

            int recursionLevel        = 1;
            Pattern typePattern       = new Pattern(DomainConstants.TYPE_ROUTE_TEMPLATE);
            Pattern relPattern        = new Pattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
            StringBuffer whereExpn    = new StringBuffer();
            whereExpn.append("attribute[");
            whereExpn.append(DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            whereExpn.append("]==");
            //Modified for bug#302310,302308 by Infosys on 21 Apr 05
            whereExpn.append(RANGE_APPROVAL);
            StringList objectSelects  = new StringList(1);
            objectSelects.add(DomainConstants.SELECT_ID);

            // querying for 'Reject' state in context object policy
            String strContextPolicy   = contextECObj.getPolicy(context).getName();
            String strReject          = FrameworkUtil.lookupStateName(context, strContextPolicy,EngineeringChange.EC_STATE_REJECT);

            //Checking if reject state exists in the current policy
            if( strReject != null && !"".equalsIgnoreCase(strReject) ){
                rejectStateExists = true;
            }

            //Getting the related Approval Route Templates
            MapList relatedApprovalList = contextECObj.getRelatedObjects(context,
                                                                         relPattern.getPattern(),
                                                                         typePattern.getPattern(),
                                                                         false,
                                                                         true,
                                                                         recursionLevel,
                                                                         objectSelects,
                                                                         new StringList(),
                                                                         whereExpn.toString(),
                                                                         DomainConstants.EMPTY_STRING,
                                                                         DomainConstants.EMPTY_STRING,
                                                                         DomainConstants.EMPTY_STRING,
                                                                         null);
            String strContextUser = context.getUser();

            //Checking if there is any Approval Route Template connected
            if (rejectStateExists && strContextUser.equals(contextECOwner)){
                showRejectCommand = true;
            } else if (rejectStateExists && relatedApprovalList.size()>0 ){
                String approvRouteId = (String)((Hashtable)relatedApprovalList.get(0)).get(DomainConstants.SELECT_ID);

                StringList rtObjectSelects = new StringList(1);
                rtObjectSelects.add(DomainConstants.SELECT_NAME);

                RouteTemplate RouteTemplateObj = (RouteTemplate)DomainObject.newInstance(context,approvRouteId,DomainConstants.TYPE_ROUTE_TEMPLATE);
                MapList routeTemplateMembers = RouteTemplateObj.getRouteTemplateMembers(context,rtObjectSelects,new StringList(),false);

                Iterator routeTemplateMembersItr = routeTemplateMembers.iterator();
                Map routeTemplateMembersMap      = null;
                String userName                  = null;

                while (!showRejectCommand && routeTemplateMembersItr.hasNext()) {
                    routeTemplateMembersMap = (Map) routeTemplateMembersItr.next();
                    userName = (String)routeTemplateMembersMap.get("name");
                    if (strContextUser.equals(userName)){
                        showRejectCommand = true;
                    }
                }
            }
        } catch (Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
        //returning the acces boolean value
        return showRejectCommand;
    }

    /**
     * Get the list of checked Engineering Change objects in the desktop
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing entry for key "RequestValuesMap", having the selected object ids in the UI3 table.
     * @return         a <code>MapList</code> having the object Ids of selected Engineering Change objects
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getECReportObjects (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap   = (HashMap)JPO.unpackArgs(args);
        Map RequestValuesMap = (Map)programMap.get("RequestValuesMap");

        //Getting the emxTableRowId String [] from the RequestValuesMap
        String[] emxTableRowId  = (java.lang.String[])RequestValuesMap.get("emxTableRowId");

        // sort out and get only object id list and leave out rel ids. emxTableRowId will have object id and rel ids
        // pipe delimited under any context object EC summary and object id alone in Desk summary
        Map mapObjIdRelId    = EngineeringChange.getObjectIdsRelIds(emxTableRowId);
        String[] arrObjIds   = (String[])mapObjIdRelId.get("ObjId");

        //Initializing list of Engineering Change Objects selected for Effort Report viewing
        MapList effortBusObjList = new MapList();
        Map ecEffortIdsMap       = new HashMap();
        String sRowObjId         = "";

        for(int i=0; i< arrObjIds.length; i++){
            sRowObjId = arrObjIds[i];
            if(sRowObjId != null && !"null".equals(sRowObjId) && !"".equals(sRowObjId)) {
                ecEffortIdsMap= new HashMap();
                ecEffortIdsMap.put(DomainConstants.SELECT_ID, sRowObjId);
                effortBusObjList.add(ecEffortIdsMap);
            }
        }

        //returns list of Engineering Change Objects selected for Report viewing
        return  effortBusObjList;
    }

    /**
     * The method is used to find the total Impact Analysis Effort for an EC to be used by EC Effort Report
     * The total effort is summation of 'Impact Analysis Effort' attribute values of all connected Impact Analysis objects
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return the total Impact Analysis Effort for selected object ids
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Vector getImpactAnalysisEffort(Context context, String[] args) throws Exception {
        //unpacking the arguments from variable args

        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        MapList objectList  = (MapList)programMap.get("objectList");
        int objectListSize  = 0 ;

        if(objectList != null) {
            objectListSize  = objectList.size();
        }
        // the object to return to display in UI table column
        Vector impactEffort  = new Vector();
        String oidsArray[]   = new String[objectListSize];

        for (int i = 0; i < objectListSize; i++) {
            try {
                oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
            } catch (Exception ex) {
                oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
            }
        }
        // Impact Analysis Effort attribute select
        String iaEffort    = "from["+ DomainConstants.RELATIONSHIP_EC_IMPACT_ANALYSIS+"].to.attribute[" + DomainConstants.ATTRIBUTE_IMPACT_ANALYSIS_EFFORT + "]";
        // declare it as a possible multi value select- result returned as StringList
        DomainObject.MULTI_VALUE_LIST.add(iaEffort);

        StringList objectSelects  = new StringList(iaEffort);

        MapList mlist             = DomainObject.getInfo(context, oidsArray, objectSelects);

        Iterator itr            = mlist.iterator();

        String iaTotal          = "0";
        String tempIAEffort     = "0";
        StringList iaEffortList = new StringList();
        Map objectMap           = null;

        while( itr.hasNext()) {
            objectMap    = (Map)itr.next();
            iaEffortList = (StringList) objectMap.get(iaEffort);

            int iaTemp   = 0;
            int iaTot    = 0;

            if(iaEffortList != null){
                for (int j=0; j<iaEffortList.size(); j++) {
                    tempIAEffort = (String)iaEffortList.get(j);
                    try{
                        iaTemp = Integer.parseInt(tempIAEffort);
                    }catch(Exception ex){
                        // force 0 value when retrieved effort value is not Int parseable
                        iaTemp = 0;
                    }
                    // sum up Efforts for each connected Impact Analysis object
                    iaTot = iaTot + iaTemp;
                }
            } else {
                // no connected Impact Analysis object. Make Impact Analysis effort 0
                iaTot = 0;
            }
            iaTotal = Integer.toString(iaTot);
            impactEffort.add(iaTotal);
        }
        return  impactEffort;
     }

    /**
     * The method is used to find the total Implementation Effort for an EC to be used by EC Effort Report
     * The total effort is summation of 'Implementation Effort' attribute values of all connected Impact Analysis objects
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return the total Implementation Effort for selected object ids
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Vector getImplementationEffort(Context context, String[] args) throws Exception {
        //unpacking the arguments from variable args

        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        MapList objectList  = (MapList)programMap.get("objectList");
        int objectListSize  = 0;

        if(objectList != null) {
            objectListSize = objectList.size();
        }

        Vector implementEffort = new Vector();
        StringList routeIds    = new StringList();
        String oidsArray[]     = new String[objectListSize];

        for (int i = 0; i < objectListSize; i++) {
            try {
                oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
            } catch (Exception ex) {
                oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
            }
        }

        String iaImplementEffort   = "from["+ DomainConstants.RELATIONSHIP_EC_IMPACT_ANALYSIS+"].to.attribute[" + DomainConstants.ATTRIBUTE_IMPLEMENTATION_EFFORT + "]";
        // declare it as a possible multi value select- result returned as StringList
        DomainObject.MULTI_VALUE_LIST.add(iaImplementEffort);

        StringList objectSelects = new StringList(iaImplementEffort);

        MapList mlist            = DomainObject.getInfo(context, oidsArray, objectSelects);

        Iterator itr            = mlist.iterator();
        String impTotal         = "0";
        String tempIAEffort     = "0";
        StringList iaEffortList = new StringList();
        Map objectMap           = null;

        while( itr.hasNext()) {
            objectMap    = (Map)itr.next();
            iaEffortList = (StringList) objectMap.get(iaImplementEffort);

            int impTemp  = 0;
            int iImpTot  = 0;

            if(iaEffortList != null){

                for (int j=0; j<iaEffortList.size(); j++) {
                    tempIAEffort = (String)iaEffortList.get(j);
                    try{
                        impTemp = Integer.parseInt(tempIAEffort);
                    }catch(Exception ex){
                        impTemp = 0;
                    }
                    iImpTot = iImpTot + impTemp;
                }
            } else {
                iImpTot = 0;
            }
            impTotal = Integer.toString(iImpTot);
            implementEffort.add(impTotal);
        }
        return  implementEffort;
     }

     /**
      * The method is used to find the total Impact Analysis Validation Effort for an EC to be used by EC Effort Report
      * The total effort is summation of 'Validation Effort' attribute values of all connected Impact Analysis objects
      * @param context the eMatrix <code>Context</code> object
      * @param args    holds the following input arguments:
      *           0 -  HashMap containing object id list
      * @return        a <code>Vector</code> object to return the total Validation Effort for selected object ids
      * @throws        Exception if the operation fails
      * @since         Common 10-6
      **
      */
    public Vector getValidationEffort(Context context, String[] args) throws Exception {
        //unpacking the arguments from variable args

        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        MapList objectList  = (MapList)programMap.get("objectList");
        Map objectMap       = null;
        int objectListSize  = 0 ;

        if(objectList != null) {
            objectListSize = objectList.size();
        }

        // the object to return to display in UI table column
        Vector validateEffort = new Vector();
        String oidsArray[]    = new String[objectListSize];

        for (int i = 0; i < objectListSize; i++) {
           try {
               oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
           } catch (Exception ex) {
               oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
           }
        }
        // Validation Effort attribute select
        String valEffortSelect    = "from["+ DomainConstants.RELATIONSHIP_EC_IMPACT_ANALYSIS +"].to.attribute[" + DomainConstants.ATTRIBUTE_VALIDATION_EFFORT + "]";
        // declare it as a possible multi value select- result returned as StringList
        DomainObject.MULTI_VALUE_LIST.add(valEffortSelect);

        StringList objectSelects  = new StringList(valEffortSelect);
        MapList mlist             = DomainObject.getInfo(context, oidsArray, objectSelects);

        Iterator itr            = mlist.iterator();
        String valTotal         = "0";
        String tempIAEffort     = "0";
        StringList iaEffortList = new StringList();

        while( itr.hasNext()) {
            objectMap    = (Map)itr.next();
            iaEffortList = (StringList) objectMap.get(valEffortSelect);

            int valTemp  = 0;
            int iValTot  = 0;

            if(iaEffortList != null){
                for (int j=0; j<iaEffortList.size(); j++) {
                    tempIAEffort = (String)iaEffortList.get(j);
                    try{
                        valTemp = Integer.parseInt(tempIAEffort);
                    }catch(Exception ex){
                        // force 0 value when retrieved effort value is not Int parseable
                        valTemp = 0;
                    }
                    // sum up Efforts for each connected Impact Analysis object
                    iValTot = iValTot + valTemp;
                }
            } else {
                // no connected Impact Analysis object. Make Validation effort 0
                iValTot = 0;
            }
            valTotal = Integer.toString(iValTot);
            validateEffort.add(valTotal);
        }
        return  validateEffort;
    }
    /**
      * The method is used to find the total Effort spent on doing Impact Analysis of an EC to be used by EC Effort Report
      * The total effort is summation of 'Impact Analysis Effort','Validation Effort' and 'Implemetation Effort' attribute
      * values of all connected Impact Analysis objects; it will be displayed in Effort Report page
      * @param context the eMatrix <code>Context</code> object
      * @param args    holds the following input arguments:
      *           0 -  HashMap containing object id list
      * @return        a <code>Vector</code> object to return the total Effort spent for each selected object ids
      * @throws        Exception if the operation fails
      * @since         Common 10-6
      **
      */
    public Vector getTotalEffort(Context context, String[] args) throws Exception {

        // the object to return to display in UI table column
        Vector totalValidationEffort     = new Vector();
        Vector totalImplementationEffort = new Vector();
        Vector totalImpactAnalysisEffort = new Vector();
        Vector grandTotalEffort          = new Vector();
        String valTotalEffort            = "";
        int size                         = 0;
        int itrSize                      = 0;
        int iTotEffort                   = 0;

        totalImpactAnalysisEffort = getImpactAnalysisEffort(context,args);
        totalImplementationEffort = getImplementationEffort(context,args);
        totalValidationEffort     = getValidationEffort(context,args);

        if(totalImpactAnalysisEffort != null) {
            itrSize = totalImpactAnalysisEffort.size();
        }

        for (int i = 0; i < itrSize; i++) {
            //sum up Impact Analysis,Implementation and Validation efforts for each EC object
            try {
                iTotEffort = Integer.parseInt((String)totalImpactAnalysisEffort.elementAt(i)) +
                             Integer.parseInt((String)totalImplementationEffort.elementAt(i)) +
                             Integer.parseInt((String)totalValidationEffort.elementAt(i));
            }catch(Exception ex){
                // force 0 value when any of retrieved effort value is not Int parseable and exception occurs
                iTotEffort = 0;
           }
           valTotalEffort = Integer.toString(iTotEffort);

           // add all to return Vector
           grandTotalEffort.add(valTotalEffort);
        }
        return grandTotalEffort;
    }

    /**
     * The method is used to fill the Files column in EC Status Report Table with 'Y' or 'N' depending on any files connection exists or not.
     * Returns the column values for 'Files' column depending on the presence of Reference Document fo the EC.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return 'Y' or 'N' according to the presence of Reference Document for selected object ids
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Vector getFilesColumn(Context context, String[] args) throws Exception {
        //unpacking the arguments from variable args
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        MapList objectList    = (MapList)programMap.get("objectList");
        int objectListSize    = 0 ;
        String i18NStrY       = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(),STR_Y);
        String i18NStrN       = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(),STR_N);
        if(objectList != null) {
           objectListSize = objectList.size();
        }

        String oidsArray[]    = new String[objectListSize];
        for (int i = 0; i < objectListSize; i++) {
           try {
               oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
           } catch (Exception ex) {
               oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
           }
        }
        // this return object has 'Y'/'N' values for showing file connections of all selected EC objects
        Vector returnVector       = new Vector();
        StringList toRefDocIdList = new StringList();
        Map toObjectMap           = null;
        String SelectToRefDocId   = "from["+ DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+"].to."+DomainConstants.SELECT_ID;
        // declare it as a possible multi value select- result returned as StringList
        DomainObject.MULTI_VALUE_LIST.add(SelectToRefDocId);

        StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
        objectSelects.add(SelectToRefDocId);

        try{
            MapList mlist    = DomainObject.getInfo(context, oidsArray, objectSelects);
            Iterator itr = mlist.iterator();

            while( itr.hasNext() ) {
               toObjectMap       = (Map)itr.next();
               toRefDocIdList    = (StringList) toObjectMap.get(SelectToRefDocId);
               if(toRefDocIdList != null && toRefDocIdList.size()>0){
                   returnVector.add(i18NStrY);
               } else {
                   returnVector.add(i18NStrN);
               }
            }
        } catch (Exception ex){
            throw ex;
        }

        return returnVector;
    }

    /**
     * The method is used to fill the Assigned column in EC Status Report Table with 'Y' /'N' depending on EC has any persons assigned.
     * Returns the column values for 'Assigned' column depending on the presence of Assignees to the Engineering Change object.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return 'Y' or 'N' according to the presence of Assignees for selected Engineering Change object ids.
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Vector getAssignedColumn(Context context, String[] args) throws Exception {
        //unpacking the arguments from variable args
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        MapList objectList    = (MapList)programMap.get("objectList");
        int objectListSize    = 0 ;
        String i18NStrY       = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(),STR_Y);
        String i18NStrN       = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(),STR_N);

        if(objectList != null) {
           objectListSize = objectList.size();
        }

        String oidsArray[]    = new String[objectListSize];
        for (int i = 0; i < objectListSize; i++) {
           try {
               oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
           } catch (Exception ex) {
               oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
           }
        }

        Vector returnVector         = new Vector();
        StringList fromPersonIdList = new StringList();
        Map fromObjectMap           = null;
        String SelectfromPersonId   = "to["+ DomainConstants.RELATIONSHIP_ASSIGNED_EC+"].from."+DomainConstants.SELECT_ID;
        // declare it as a possible multi value select- result returned as StringList
        DomainObject.MULTI_VALUE_LIST.add(SelectfromPersonId);

        StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
        objectSelects.add(SelectfromPersonId);

        try{
            MapList mlist    = DomainObject.getInfo(context, oidsArray, objectSelects);
            Iterator itr = mlist.iterator();

            while( itr.hasNext() ) {
               fromObjectMap    = (Map)itr.next();
               fromPersonIdList    = (StringList) fromObjectMap.get(SelectfromPersonId);
               if(fromPersonIdList != null && fromPersonIdList.size()>0){
                   returnVector.add(i18NStrY);
               } else {
                   returnVector.add(i18NStrN);
               }
            }
        } catch (Exception ex){
            throw ex;
        }

        return returnVector;
    }

    /**
     * The method is used to fill the Impact Analysis column in EC Status Report Table.
     * Returns the column value 'Y'/'N' for 'Impact Analysis' column depending on the presence
     * of Impact Analysis object connection in the Engineering Change object.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return 'Y' or 'N' according to the presence
     *                of Impact Analysis for selected Engineering Change object ids.
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Vector getImpactAnalysisColumn(Context context, String[] args) throws Exception {
        //unpacking the arguments from variable args
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        MapList objectList    = (MapList)programMap.get("objectList");
        int objectListSize    = 0 ;
        String i18NStrY       = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(),STR_Y);
        String i18NStrN       = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(),STR_N);
        if(objectList != null) {
           objectListSize = objectList.size();
        }

        String oidsArray[]    = new String[objectListSize];
        for (int i = 0; i < objectListSize; i++) {
           try {
               oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
           } catch (Exception ex) {
               oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
           }
        }


        Vector returnVector   = new Vector();
        StringList toIAIdList = new StringList();
        Map toObjectMap       = null;
        String SelectToIAId   = "from["+ DomainConstants.RELATIONSHIP_EC_IMPACT_ANALYSIS +"].to."+DomainConstants.SELECT_ID;
        // declare it as a possible multi value select- result returned as StringList
        DomainObject.MULTI_VALUE_LIST.add(SelectToIAId);

        StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
        objectSelects.add(SelectToIAId);

        try{
            MapList mlist    = DomainObject.getInfo(context, oidsArray, objectSelects);
            Iterator itr = mlist.iterator();

            while( itr.hasNext() ) {
               toObjectMap    = (Map)itr.next();
               toIAIdList    = (StringList) toObjectMap.get(SelectToIAId);
               if(toIAIdList != null && toIAIdList.size()>0){
                   returnVector.add(i18NStrY);
               } else {
                   returnVector.add(i18NStrN);
               }
            }
        } catch (Exception ex){
            throw ex;
        }

        return returnVector;
    }

    /**
     * The method is used to fill the Test Case column in EC Status Report Table.
     * Returns the column values for 'Test Case' column depending on the number of
     * Test Case objects connected to each Engineering Change object.at any level under it
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return number of Test Cases connected to each Engineering Change Object.
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Vector getTestCaseColumn(Context context, String[] args) throws Exception {

        //unpacking the arguments from variable args
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        MapList objectList    = (MapList)programMap.get("objectList");
        int objectListSize    = 0 ;

        //Checking for objectList
        if(objectList != null) {
           objectListSize = objectList.size();
        }else {
            throw new Exception();
        }

        String oidsArray[]    = new String[objectListSize];
        //Retrieving the object Ids from the Map
        for (int i = 0; i < objectListSize; i++) {
           try {
               oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
           } catch (Exception ex) {
               oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
           }
        }

        Vector returnVector         = new Vector();
        // relationship pattern
        Pattern relPattern          = new Pattern(DomainConstants.RELATIONSHIP_EC_TEST_CASE);
        relPattern.addPattern(DomainConstants.RELATIONSHIP_SUB_TEST_CASE);
        // type pattern
        Pattern typePattern         = new Pattern(DomainConstants.TYPE_TEST_CASE);
        int noOfTCs                 = 0;
        int recurseToLevel          = 0; //Recurse level set to get all sub Test Cases
        MapList relBusObjPageList   = new MapList();
        StringList objectSelects    = new StringList(DomainConstants.SELECT_ID);

        try{
            for(int i=0;i<oidsArray.length;i++) {//Iterating thro each EC object Id and geting all the related TC. At all levels.
                setId(oidsArray[i]);
                relBusObjPageList     = getRelatedObjects(context,
                                                relPattern.getPattern(),   // 'EC Test Case'and 'Sub Test Case' relationships
                                                typePattern.getPattern(),  // get type Test Case objects
                                                false,
                                                true,
                                                recurseToLevel,
                                                objectSelects,
                                                new StringList(),
                                                DomainConstants.EMPTY_STRING,
                                                DomainConstants.EMPTY_STRING,
                                                DomainConstants.EMPTY_STRING,
                                                DomainConstants.EMPTY_STRING,
                                                null);
                if(relBusObjPageList != null){
                    noOfTCs = relBusObjPageList.size();
                    returnVector.add(Integer.toString(noOfTCs));
                }else{
                    returnVector.add(Integer.toString(noOfTCs));
                }
                noOfTCs = 0;
            }//end of for

        }catch (Exception ex){
            throw ex;
        }

        return returnVector;
    }

    /**
     * The method is used to fill the 'Percentage Passed' column in EC Status Report Table.
     * Returns the column values for 'Percentage Passed' column depending on the status of
     * 'Percentage Passed' attribute value of last connected Test Execution
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id list
     * @return        a <code>Vector</code> object to return 'Percentage Passed' of latest
     *                related Test Execution.for all selected Engineering Change objects
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Vector getPercentPassColumn(Context context, String[] args) throws Exception {
        //unpacking the arguments from variable args
        HashMap programMap        = (HashMap)JPO.unpackArgs(args);
        MapList objectList        = (MapList)programMap.get("objectList");
        String strTEStateComplete = FrameworkUtil.lookupStateName(context, POLICY_TEST_EXECUTION,TE_STATE_COMPLETE);
        int objectListSize        = 0 ;
        if(objectList != null) {
           objectListSize = objectList.size();
        }

        String oidsArray[]    = new String[objectListSize];
        for (int i = 0; i < objectListSize; i++) {
           try {
               oidsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_ID);
           } catch (Exception ex) {
               oidsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_ID);
           }
        }

        Vector returnVector              = new Vector();
        Map toObjectMap                  = null;
        String selectToTEOriginated     = "from["+ DomainConstants.RELATIONSHIP_EC_TEST_EXECUTION +"].to."+DomainConstants.SELECT_ORIGINATED;
        String selectToTEId             = "from["+ DomainConstants.RELATIONSHIP_EC_TEST_EXECUTION +"].to."+DomainConstants.SELECT_ID;
        String selectToTEState          = "from["+ DomainConstants.RELATIONSHIP_EC_TEST_EXECUTION +"].to."+DomainConstants.SELECT_CURRENT;
        String selectToTEAttPercentPass = "from["+ DomainConstants.RELATIONSHIP_EC_TEST_EXECUTION +"].to.attribute["+DomainConstants.ATTRIBUTE_PERCENTAGE_PASSED+"]";
        DomainObject.MULTI_VALUE_LIST.add(selectToTEOriginated);
        DomainObject.MULTI_VALUE_LIST.add(selectToTEId);
        DomainObject.MULTI_VALUE_LIST.add(selectToTEState);
        DomainObject.MULTI_VALUE_LIST.add(selectToTEAttPercentPass);

        StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
        objectSelects.add(selectToTEOriginated);
        objectSelects.add(selectToTEId);
        objectSelects.add(selectToTEState);
        objectSelects.add(selectToTEAttPercentPass);
        try{
            MapList mlist    = DomainObject.getInfo(context, oidsArray, objectSelects);

            StringList tmpToTEOriginatedList     = new StringList();
            StringList tmpToTEIDList             = new StringList();
            StringList tmpToTEStateList          = new StringList();
            StringList tmpToTEAttPercentPassList = new StringList();
            Iterator itr = mlist.iterator();

            while( itr.hasNext() ) {
                toObjectMap                     = (Map)itr.next();
                String attToDisplay             = "";
                MapList sortedOriginatedMapList = new MapList();//Sorted MapList to hold the sorted "Date" values
                tmpToTEOriginatedList          = (StringList) toObjectMap.get(selectToTEOriginated);
                tmpToTEIDList                  = (StringList) toObjectMap.get(selectToTEId);
                tmpToTEStateList               = (StringList) toObjectMap.get(selectToTEState);
                tmpToTEAttPercentPassList      = (StringList) toObjectMap.get(selectToTEAttPercentPass);

                //Checking if there is a Test Execution connected to the EC object
                if(tmpToTEIDList != null){
                    HashMap dateMap = new HashMap();
                    for(int i=0; i< tmpToTEOriginatedList.size(); i++) {
                        //Checking if the selected Test Execution is in 'Complete' state and then populating the sort maplist.
                        if(strTEStateComplete.equals((String)tmpToTEStateList.get(i))) {
                            dateMap = new HashMap();
                            dateMap.put("date", (String)tmpToTEOriginatedList.get(i));
                            dateMap.put("percentagePassed", (String)tmpToTEAttPercentPassList.get(i));
                            dateMap.put(DomainConstants.SELECT_ID, (String)tmpToTEIDList.get(i));
                            sortedOriginatedMapList.add(dateMap);
                        }
                    }
                    //Checking if the sort maplist has elements.
                    if(sortedOriginatedMapList != null && sortedOriginatedMapList.size()>0) {
                        // sort based on 'Originated' Date value of the related Test Execution
                        sortedOriginatedMapList.sort("date","descending","date");
                        // get last connected Test Execution i.e. the object with latest 'Originated' value
                        attToDisplay = (String)( ( (HashMap)sortedOriginatedMapList.get(0) ).get("percentagePassed") );
                        if(attToDisplay != null ){
                            returnVector.add(attToDisplay);
                        } else {
                            attToDisplay = "0.0";
                            returnVector.add(attToDisplay);
                        }

                    } else { //If there are no related Test Executions In 'Complete' State.
                        attToDisplay = "0.0";
                        returnVector.add(attToDisplay);
                    }
                } else {// If no related Test Execution present for the EC object, printing 0
                    attToDisplay = "0.0";
                    returnVector.add(attToDisplay);
                }
            }//End of while
        } catch (Exception ex){
            throw ex;
        }
        return returnVector;
    }

    /**
     * The method is used to show / hide 'Add Existing'/'Remove' actions links in Engineering Change Affected Items
     * summary page. The links will be enabled till all states upto 'Validate' state
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Boolean showAddRemoveAffectedItemsLink(Context context, String[] args)
        throws Exception {
        return(showActionsLink(context,args,EC_STATE_VALIDATE));
        }

    /**
     * The method is used to show / hide 'Add Existing'/'Remove' actions links in Engineering Change Implemented Items
     * summary page. The links will be enabled till all states upto 'Implement' state
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disable link
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Boolean showAddRemoveImplementedItemsLink(Context context, String[] args) throws Exception {
        return(showActionsLink(context,args,EC_STATE_IMPLEMENT));
        }

    /**
     * This trigger method notifies the Assignee about the EC assignment when a
     * new relationship "Assigned EC" is created between EC and Person object.
     * The notification is sent only if EC is not in Submit state.
     *
     * @param context The ematrix context of the request
     * @param args The string array containing following elements:
     *          0 - The object id of Person
     *          1 - The object id of the context EC
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public void notifyAssignee(Context context, String[] args)
            throws Exception, FrameworkException {
        //Get the objectId of the Engineering Change and the Person object
        String strPersonId = args[0];
        String strECId = args[1];

        //Get the current state of the Engineering Change object and if it is
        // in submit then do not notify the Assignee
        DomainObject domEC = DomainObject.newInstance(
                context, strECId);
        List lstObjectSelects = new StringList(DomainConstants.SELECT_CURRENT);
        lstObjectSelects.add(DomainConstants.SELECT_NAME);

        DomainObject domPerson = DomainObject.newInstance(
                context, strPersonId);
        Map mapPersonInfo = domPerson.getInfo(
                context, (StringList) lstObjectSelects);

        //Form the reciepient list to send the message
        List lstAssigneeList = new StringList((String) mapPersonInfo
                .get(DomainConstants.SELECT_NAME));

        //Form the subject and the message body
        String strLanguage = context.getSession().getLanguage();
        Locale strLocale = context.getLocale();
        String strSubjectKey =  "emxComponents.Message.Subject.EngineeringChangeAssigned";
        String strMessageKey = "emxComponents.Message.Description.EngineeringChangeAssigned";

        String[] subjectKeys = {};
        String[] subjectValues = {};
        String[] messageKeys = {};
        String[] messageValues = {};

        //Form the message attachment
        List lstAttachments = new StringList();
        lstAttachments.add(strECId);

        //Send the notification to all the Assignees

        emxMailUtil_mxJPO.sendNotification( context, strECId, 
        (StringList)lstAssigneeList, null, null, strSubjectKey, subjectKeys,
        subjectValues, strMessageKey, messageKeys, messageValues,
        (StringList)lstAttachments, null, RESOURCE_BUNDLE_COMPONENTS_STR);
    }

    /* This trigger method sends the notiifcation to the object owners when the
     * new Implemented Item, Affetcted Item or Reported Against Item is added to
     * the Engineeering Change object. The type of the relationship is passed as
     * an argument to this method depending upon which the notification is sent.
     *
     * @param context The ematrix context of the request.
     * @param args This string array contains following arguments:
     *          0 - The object id of Engineering Change object.
     *          1 - The object id of Implemented Item, Reported Against Item or
     *              Affected Item
     *          2 - The relariohsip name(Reported Against EC or EC Affetcted Item
     *              or EC Implemented Item)
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public void notifyItemOwners(Context context, String[] args)
            throws Exception, FrameworkException {
        //Get the parameters passed from the trigger object
        String strECId = args[0];
        String strItemId = args[1];
        String strRelName = args[2];

        //Get the type, name and revision of the Engineering Change and the
        // connected Implemented or Affected Item
        String[] objectIds = new String[2];
        objectIds[0] = strECId;
        objectIds[1] = strItemId;

        List lstObjectSelects = new StringList(DomainConstants.SELECT_NAME);
        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
        lstObjectSelects.add(DomainConstants.SELECT_REVISION);
        lstObjectSelects.add(DomainConstants.SELECT_OWNER);

        BusinessObjectWithSelectList selectedobjectList = DomainObject
                .getSelectBusinessObjectData(
                        context, objectIds, (StringList) lstObjectSelects);

        String strItemOwner = (selectedobjectList.getElement(1))
                .getSelectData(DomainConstants.SELECT_OWNER);
        String strECOwner = (selectedobjectList.getElement(0))
                .getSelectData(DomainConstants.SELECT_OWNER);

        //If the item owner is different from the EC owner then send the
        //notification to the item owner
        if (!strItemOwner.equals(strECOwner)) {

            String strECType = (selectedobjectList.getElement(0))
                    .getSelectData(DomainConstants.SELECT_TYPE);
            String strECName = (selectedobjectList.getElement(0))
                    .getSelectData(DomainConstants.SELECT_NAME);
            String strECRevision = (selectedobjectList.getElement(0))
                    .getSelectData(DomainConstants.SELECT_REVISION);

            String strItemType = (selectedobjectList.getElement(1))
                    .getSelectData(DomainConstants.SELECT_TYPE);
            String strItemName = (selectedobjectList.getElement(1))
                    .getSelectData(DomainConstants.SELECT_NAME);
            String strItemRevision = (selectedobjectList.getElement(1))
                    .getSelectData(DomainConstants.SELECT_REVISION);

            //Form the Message Subject and Message Body depending upon the
            // relationship type
            String strSubjectKey = DomainConstants.EMPTY_STRING;
            String strMessageKey = DomainConstants.EMPTY_STRING;
            String[] formatArgs = new String[6];
            String strLanguage = context.getSession().getLanguage();

            if (strRelName.equals(DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM)) {
                strSubjectKey = i18nNow
                        .getI18nString(
                                "emxComponents.Message.Subject.EngineeringChangeAffectedItem",
                                RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
                formatArgs[0] = strItemType;
                formatArgs[1] = strItemName;
                formatArgs[2] = strItemRevision;
                formatArgs[3] = strECType;
                formatArgs[4] = strECName;
                formatArgs[5] = strECRevision;
                strMessageKey = MessageUtil
                        .getMessage(
                                context,
                                null,
                                "emxComponents.Message.Description.EngineeringChangeAffectedItem",
                                formatArgs, null, context.getLocale(),
                                RESOURCE_BUNDLE_COMPONENTS_STR);
            } else if (strRelName.equals(DomainConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM)) {
                strSubjectKey = i18nNow
                        .getI18nString(
                                "emxComponents.Message.Subject.EngineeringChangeImplementedItem",
                                RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
                formatArgs[0] = strItemType;
                formatArgs[1] = strItemName;
                formatArgs[2] = strItemRevision;
                formatArgs[3] = strECType;
                formatArgs[4] = strECName;
                formatArgs[5] = strECRevision;
                strMessageKey = MessageUtil
                        .getMessage(
                                context,
                                null,
                                "emxComponents.Message.Description.EngineeringChangeImplementedItem",
                                formatArgs, null, context.getLocale(),
                                RESOURCE_BUNDLE_COMPONENTS_STR);
            } else if (strRelName.equals(DomainConstants.RELATIONSHIP_REPORTED_AGAINST_EC)) {
                strSubjectKey = i18nNow
                        .getI18nString(
                                "emxComponents.Message.Subject.EngineeringChangeReportedAgainstItem",
                                RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
                formatArgs[0] = strECType;
                formatArgs[1] = strECName;
                formatArgs[2] = strECRevision;
                formatArgs[3] = strItemType;
                formatArgs[4] = strItemName;
                formatArgs[5] = strItemRevision;
                strMessageKey = MessageUtil
                        .getMessage(
                                context,
                                null,
                                "emxComponents.Message.Description.EngineeringChangeReportedAgainstItem",
                                formatArgs, null, context.getLocale(),
                                RESOURCE_BUNDLE_COMPONENTS_STR);
            }
            String[] subjectKeys = {};
            String[] subjectValues = {};
            String[] messageKeys = {};
            String[] messageValues = {};

            //Form the owner list to send the message
            List lstOwnerList = new StringList();
            lstOwnerList.add(strItemOwner);

            //Form the message attachment
            List lstAttachments = new StringList();
            lstAttachments.add(strECId);
            lstAttachments.add(strItemId);

            //Send the notification to the owner

            emxMailUtil_mxJPO.sendNotification( context,
            (StringList)lstOwnerList, null, null, strSubjectKey, subjectKeys,
            subjectValues, strMessageKey, messageKeys, messageValues,
            (StringList)lstAttachments, null);
        }

    }

    /**
     * This trigger method notifies all the distribution list members connected
     * to EC by "EC Distribution List" relationship when the EC is promoted to
     * the Complete state.
     *
     * @param context The ematrix context of the request.
     * @param args The string array containing following arguments:
     *          0 - The objectId of the context EC
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public void notifyDistributionList(Context context, String args[])
            throws Exception, FrameworkException {
        //Get the object id of the context Engineering Change object and form
        // the domain object
        String strObjectId = args[0];
        DomainObject domEC = DomainObject.newInstance(
                context, strObjectId);

        //Get the distribution list connected to the context Engineering Change
        // object by EC Distribution List relationship.
        StringBuffer sbSelectMember = new StringBuffer();
        sbSelectMember.append(SYMB_FROM);
        sbSelectMember.append(SYMB_OPEN_BRACKET);
        sbSelectMember.append(DomainConstants.RELATIONSHIP_LIST_MEMBER);
        sbSelectMember.append(SYMB_CLOSE_BRACKET);
        sbSelectMember.append(SYMB_DOT);
        sbSelectMember.append(SYMB_TO);
        sbSelectMember.append(SYMB_DOT);
        sbSelectMember.append(DomainConstants.SELECT_NAME);

        List lstObjectSelects = new StringList();
        lstObjectSelects.add(sbSelectMember.toString());

        List lstMemberList = domEC.getRelatedObjects(
                context, DomainConstants.RELATIONSHIP_EC_DISTRIBUTION_LIST,
                DomainConstants.QUERY_WILDCARD, (StringList) lstObjectSelects,
                null, false, true, (short) 1, null, null);

        //Cast the output to either String or List depending upon the type.
        if (lstMemberList != null && lstMemberList.size() > 0) {
            List lstListMembers = new StringList();
            Object listMembers = (Object) ((Map) lstMemberList.get(0))
                    .get(sbSelectMember.toString());
            if (listMembers instanceof List) {
                lstListMembers = (StringList) listMembers;
            } else if (listMembers instanceof String) {
                lstListMembers.add((String) listMembers);
            }

            if (lstListMembers != null && lstListMembers.size() > 0) {
                String strLanguage = context.getSession().getLanguage();
                String strSubjectKey = i18nNow
                        .getI18nString(
                                "emxComponents.Message.Subject.EngineeringChangeCompleted",
                                RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
                String strMessageKey = i18nNow
                        .getI18nString(
                                "emxComponents.Message.Description.EngineeringChangeCompleted",
                                RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
                strMessageKey = MessageUtil.substituteValues(
                        context, strMessageKey, strObjectId, strLanguage);

                String[] subjectKeys = {};
                String[] subjectValues = {};
                String[] messageKeys = {};
                String[] messageValues = {};

                //Form the message attachment
                List lstAttachments = new StringList();
                lstAttachments.add(strObjectId);

                //Send the notification to the owner

                emxMailUtil_mxJPO.sendNotification( context,
                (StringList)lstListMembers, null, null, strSubjectKey,
                subjectKeys, subjectValues, strMessageKey, messageKeys,
                messageValues, (StringList)lstAttachments, null);

            }
        }

    }

    /**
     * This trigger method notifies the owner of the Engineering Change when EC
     * is promoted from Validate State if the promotion is not done by the owner
     * himself.
     *
     * @param context The ematrix context of the request.
     * @param args The string array containing following arguments
     *          0 - Object Id of the context EC
     *          1 - Owner of the EC
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public void notifyOwneronValidate(Context context, String[] args)
            throws Exception, FrameworkException {

        //Get the object id and owner of the context Engineering Change object
        String strObjectId = args[0];
        String strECOwner = args[1];

        String strContextUser = context.getUser();

        //Check if the context user is the owner of the Engineering Change
        if (!strContextUser.equals(strECOwner)) {
            String strLanguage = context.getSession().getLanguage();
            String strSubjectKey = i18nNow
                    .getI18nString(
                            "emxComponents.Message.Subject.EngineeringChangeFormallyApproved",
                            RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
            String strMessageKey = i18nNow
                    .getI18nString(
                            "emxComponents.Message.Description.EngineeringChangeFormallyApproved",
                            RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
            strMessageKey = MessageUtil.substituteValues(
                    context, strMessageKey, strObjectId, strLanguage);

            StringBuffer sbMessage = new StringBuffer(strMessageKey);
            sbMessage.append(STR_SPACE);
            sbMessage.append(strContextUser);
            sbMessage.append(SYMB_DOT);

            String[] subjectKeys = {};
            String[] subjectValues = {};
            String[] messageKeys = {};
            String[] messageValues = {};

            //Form the message attachment
            List lstAttachments = new StringList();
            lstAttachments.add(strObjectId);

            //Form the owner list to send the message
            List lstOwnerList = new StringList();
            lstOwnerList.add(strECOwner);

            //Send the notification to the owner

            emxMailUtil_mxJPO.sendNotification( context,
            (StringList)lstOwnerList, null, null, strSubjectKey, subjectKeys,
            subjectValues, sbMessage.toString(), messageKeys, messageValues,
            (StringList)lstAttachments, null);

        }

    }

    /**
     * This trigger method notifies all the assignees connected to the
     * Engineering Change object by Assigned EC relationship when EC is promoted
     * to Approved state. If the promotion is not done by the owner himself then
     * the notification will be sent to owner also.
     *
     * @param context The ematrix context of the request
     * @param args string array which contains follwing elements:
     *          0 - The object id of the context EC
     *          2 - The owner of the context EC
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public void notifyOnApproval(Context context, String[] args)
            throws Exception, FrameworkException {
        //Get the Object Id and owner of the context Engineering Change object.
        String strObjectId = args[0];
        String strOwner = args[1];

        //Get the list of all the assignees connectd to this EC by "Assigned
        // EC" relationship
        List lstAssigneeList = getAssignees(
                context, strObjectId);

        //Get the context user and if it is different from the owner then add
        // the owner to the receiver's list
        String strContextUser = context.getUser();
        if (!strContextUser.equals(strOwner)) {
            lstAssigneeList.add(strOwner);
        }

        //Form the subject and the message body
        String strLanguage = context.getSession().getLanguage();
        String strSubjectKey = i18nNow
                .getI18nString(
                        "emxComponents.Message.Subject.EngineeringChangeApprovedForImplemetation",
                        RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
        String strMessageKey = i18nNow
                .getI18nString(
                        "emxComponents.Message.Description.EngineeringChangeApprovedForImplemetation",
                        RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
        strMessageKey = MessageUtil.substituteValues(
                context, strMessageKey, strObjectId, strLanguage);
        String[] subjectKeys = {};
        String[] subjectValues = {};
        String[] messageKeys = {};
        String[] messageValues = {};

        //Form the message attachment
        List lstAttachments = new StringList();
        lstAttachments.add(strObjectId);

        //Begin of Add for Bug#300683_Reopened by Infosys
        //Unsetting the environmental variable MX_TREE_MENU
        //before sending the notification. This is a workaroound for
        //the framework bug in the notification code.
        emxMailUtil_mxJPO.unsetTreeMenuName(context,
                                                                         new String[1]);
        //End of Add for Bug#300683_Reopened by Infosys

        //Send the notification to all the Assignees

        emxMailUtil_mxJPO.sendNotification( context,
        (StringList)lstAssigneeList, null, null, strSubjectKey, subjectKeys,
        subjectValues, strMessageKey, messageKeys, messageValues,
        (StringList)lstAttachments, null);

    }

    /**
     * This trigger method notifies the EC owner when EC is promoted to the
     * Complete state.
     *
     * @param context The ematrix context of the request.
     * @param args The string array containing the following arguments
     *          0 - The object id of the context Engineering Change Object
     *          1 - The owner of the context Engineering Change Object
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public void notifyOnComplete(Context context, String[] args)
            throws Exception, FrameworkException {
        //Get the Object Id and owner of the context Engineering Change object.
        String strObjectId = args[0];
        String strOwner = args[1];

        String strLanguage = context.getSession().getLanguage();
        Locale strLocale = context.getLocale();
        String strSubjectKey = EnoviaResourceBundle.getProperty(context,
                RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,
                "emxComponents.Message.Subject.EngineeringChangeCompleted");
        String strMessageKey = EnoviaResourceBundle.getProperty(context,
                RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,
                "emxComponents.Message.Description.EngineeringChangeCompleted");
        strMessageKey = MessageUtil.substituteValues(
                context, strMessageKey, strObjectId, strLanguage);

        String[] subjectKeys = {};
        String[] subjectValues = {};
        String[] messageKeys = {};
        String[] messageValues = {};

        //Form the message attachment
        List lstAttachments = new StringList();
        lstAttachments.add(strObjectId);

        //Form the owner list to send the message
        List lstOwnerList = new StringList();
        lstOwnerList.add(strOwner);

        //Begin of Add for Bug#300683_Reopened by Infosys
        //Unsetting the environmental variable MX_TREE_MENU
        //before sending the notification. This is a workaroound for
        //the framework bug in the notification code.
        emxMailUtil_mxJPO.unsetTreeMenuName(context,
                                                                         new String[1]);
        //End of Add for Bug#300683_Reopened by Infosys

        //Send the notification to the owner

        emxMailUtil_mxJPO.sendNotification( context,
        (StringList)lstOwnerList, null, null, strSubjectKey, subjectKeys,
        subjectValues, strMessageKey, messageKeys, messageValues,
        (StringList)lstAttachments, null);

    }

    /**
     * This method returns names of all the assignees(Person Objects) connected
     * to the Engineering Change object by "Assigned EC" relationship.
     *
     * @param context The ematrix context of the request
     * @param strECId The object id of Engineering Change
     * @return List containig names of all assignees.
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    protected List getAssignees(Context context, String strECId)
            throws Exception, FrameworkException {
        //Create a new domain object for context EC
        DomainObject domEC = DomainObject.newInstance(
                context, strECId);

        //Get all the objects of type "Person" connected to EC by "Assigned EC"
        // relationship
        List lstObjectSelects = new StringList(DomainConstants.SELECT_NAME);
        List lstAssigneeMapList = (MapList) domEC.getRelatedObjects(
                context, DomainConstants.RELATIONSHIP_ASSIGNED_EC,
                DomainConstants.TYPE_PERSON,(StringList) lstObjectSelects,
                null, true, false, (short) 1, null, null);

        //Form the stringlist of all the assignee names
        List lstAssigneeList = new StringList();
        for (int i = 0; i < lstAssigneeMapList.size(); i++) {
            lstAssigneeList.add((String) ((Map) lstAssigneeMapList.get(i))
                    .get(DomainConstants.SELECT_NAME));
        }

        return lstAssigneeList;
    }

    /**
     * This trigger method creates a Reviewer or Approval Route when EC is
     * promoted to Review/Formal Approval state using the Route Tempalate
     * Connected to Engineering Change object by "Object Route" relationship. If
     * no "Route Template" is connected then default Reviewer or Approval Route
     * is created with no tasks.
     *
     * @param context The ematrix context of the request
     * @param args String Array with following arguments:
     *          0 - The Object Id of the context Engineering Change Object.
     *          1 - The next state of the route ontext Engineering Change Object.
     *          2 - The "Route Base Purpose"(Approve/Review)
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public void createRoute(Context context, String[] args) throws Exception,
            FrameworkException {
        //Get the Object Id of the context Engineering Change object.
        String strObjectId = args[0];
        String strBaseState = args[1];
        String strRouteBasePurpose = args[2];

        //Get the route template id
        String strRouteTemplateId = getRouteTemplateId(
                context, strObjectId, strRouteBasePurpose);
        Route routeBean = (Route) DomainObject.newInstance(
                context, DomainConstants.TYPE_ROUTE);

        //Create the new Route Object and connect it to the EC.
        Map mpRelAttributeMap = new Hashtable();
        mpRelAttributeMap.put(
                "routeBasePurpose", strRouteBasePurpose);
        mpRelAttributeMap.put(
                strObjectId, strBaseState);

        Map routeMap = Route.createRouteWithScope(
                context, strObjectId, null, null, true,
                (Hashtable) mpRelAttributeMap);
        String strRouteId = (String) routeMap.get("routeId");
        routeBean.setId(strRouteId);

        //Set the attributes of the new Route Object
        String strAttrRouteCompletionAction = PropertyUtil.getSchemaProperty(
                context, "attribute_RouteCompletionAction");
        Map mpRouteAttributeMap = new HashMap();
        mpRouteAttributeMap.put(
                DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,
                strRouteBasePurpose);
        //Modified for bug#302310,302308 by Infosys on 21 Apr 05
        mpRouteAttributeMap.put(
                strAttrRouteCompletionAction, RANGE_PROMOTE_CONNECTED_OBJECT);
        routeBean.setAttributeValues(
                context, mpRouteAttributeMap);

        //If the route template id is not null then connect the route to the
        // route template
        if (strRouteTemplateId != null
                && !strRouteTemplateId.equalsIgnoreCase("null")
                && !strRouteTemplateId
                        .equalsIgnoreCase(DomainConstants.EMPTY_STRING)) {
            routeBean.connectTemplate(
                    context, strRouteTemplateId);
            routeBean.addMembersFromTemplate(
                    context, strRouteTemplateId);
        }
    }

    /**
     * This method returns the object Id of the route template connected to the
     * context Engineeing Change object for which Route Base Purpose is
     * strBasePurpose. The base purpose for the Route Template is decided by
     * retrieiving the attribute "Route Base Purpose" of the relationship
     * "Object Route" between Engineering Change and Route Template.
     *
     * @param context The ematrix context of the request
     * @param strObjectId The Object Id of the context Engineering Change
     *            object.
     * @param strBasePurpose The "Route Base Purpose"
     * @return String Object Id of the Route Template mathching the criterion
     *         otherwise empty string.
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    protected String getRouteTemplateId(Context context, String strObjectId,
            String strBasePurpose) throws Exception, FrameworkException {
        //Create a new DomainObject representing the Engineering Change object
        DomainObject domEC = DomainObject.newInstance(
                context, strObjectId);

        //Form the where expression about the Route Base Purpose. The
        // relationship Attribute "Route Base Purpose" between EC and Route
        // Template should be same as strBasePurpose.
        StringBuffer sbWhereExpression = new StringBuffer();
        sbWhereExpression.append(SYMB_ATTRIBUTE);
        sbWhereExpression.append(SYMB_OPEN_BRACKET);
        sbWhereExpression.append(DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
        sbWhereExpression.append(SYMB_CLOSE_BRACKET);
        sbWhereExpression.append(SYMB_EQUAL);
        sbWhereExpression.append(SYMB_QUOTE);
        sbWhereExpression.append(strBasePurpose);
        sbWhereExpression.append(SYMB_QUOTE);

        //Form the objects select list
        List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);

        //Get the "Route Template" connected to the EC by "Object Route"
        // relationship satsfying the where condition
        List lstRouteTemplateList = (MapList) domEC.getRelatedObjects(
                context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                DomainConstants.TYPE_ROUTE_TEMPLATE,
                (StringList) lstObjectSelects, null, false, true, (short) 1,
                null, sbWhereExpression.toString());
        if (lstRouteTemplateList != null && lstRouteTemplateList.size() > 0) {
            return (String) ((Map) lstRouteTemplateList.get(0))
                    .get(DomainConstants.SELECT_ID);
        } else {
            return DomainConstants.EMPTY_STRING;
        }
    }

    /**
         * When the EC is promoted to "Implemented" State this trigger method
         * automatically revise the "Affected Items" connected to EC by "EC Affected
         * Item" relationship and connect it to EC by "EC Implemented Item"
         * relationship if following two conditions are met:
         *      1.The value of attribute "Requested Change" on the relationship EC Affected
         *        Item is "For Revise".
         *      2.The affected item is revisionable and next revision does not exist.
         *
         * @param context The ematrix context of the request
         * @param args String Array with following arguments:
         *          0 - The Object Id of the context Engineering Change Object.
         * @throws Exception
         * @throws FrameworkException
         * @since AEF10.6
         */
        public void autoReviseAffectedItems(Context context, String[] args)
                throws Exception, FrameworkException {
            //Form the DomainObject representing context Engineerin Change object
            String strObjectId = args[0];
            DomainObject domEC = DomainObject.newInstance(
                    context, strObjectId);

            //Form the relationship where expression to get the related "Affected
            // Items" for which the value of the relationship attribute "Requested
            // Change" is
            // "For Revise".
            i18nNow i18nnow = new i18nNow();

            StringBuffer sbRelWhereExpression = new StringBuffer();
            sbRelWhereExpression.append(SYMB_ATTRIBUTE);
            sbRelWhereExpression.append(SYMB_OPEN_BRACKET);
            sbRelWhereExpression.append(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE);
            sbRelWhereExpression.append(SYMB_CLOSE_BRACKET);
            sbRelWhereExpression.append(SYMB_EQUAL);
            sbRelWhereExpression.append(SYMB_QUOTE);
            //Modified for bug#302310,302308 by Infosys on 21 Apr 05
            sbRelWhereExpression.append(RANGE_FOR_REVISE);
            sbRelWhereExpression.append(SYMB_QUOTE);

            //Begin of Modify by Infosys for Bug 300664 on 25-Mar-05
            StringBuffer sbRevSelect = new StringBuffer();
            sbRevSelect.append("current.revisionable");

            //Get all the related Affected Items satisfying the where expression
            List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
            //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
            lstObjectSelects.add(DomainConstants.SELECT_OWNER);
            lstObjectSelects.add(DomainConstants.SELECT_TYPE);
            lstObjectSelects.add(DomainConstants.SELECT_NAME);
            lstObjectSelects.add(DomainConstants.SELECT_REVISION);
            lstObjectSelects.add(sbRevSelect.toString());
            lstObjectSelects.add("current.majorrevisionable");
            //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
            //End of Modify by Infosys for Bug 300664 on 25-Mar-05

            MapList lstAffectedItemsList = (MapList) domEC.getRelatedObjects(
                    context, DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM,
                    DomainConstants.QUERY_WILDCARD,(StringList) lstObjectSelects,
                    null, false, true, (short) 1, null,
                    sbRelWhereExpression.toString());

            //Revise all the revisionable Affected Items and store the ids of new
            // revisions in a StringList
            if (lstAffectedItemsList != null && lstAffectedItemsList.size() > 0) {
                StringList lstRevisedItemsList = new StringList();
                int iCount = 0;
                String strAffectedItemId = DomainConstants.EMPTY_STRING;
                //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                String strAffectedItemOwner = DomainConstants.EMPTY_STRING;
                String strAffectedItemType = DomainConstants.EMPTY_STRING;
                String strAffectedItemName = DomainConstants.EMPTY_STRING;
                String strAffectedItemRev = DomainConstants.EMPTY_STRING;
                StringBuffer sbHistoryAction = new StringBuffer();
                //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05

                //Begin of Add by Infosys for Bug#300664 on 25 Mar 05
                boolean bShowAlert = false;
                String strRevisionable = DomainConstants.EMPTY_STRING;
                //End of Add by Infosys for Bug#300664 on 25 Mar 05
                String strMajorRevisionable = DomainConstants.EMPTY_STRING;

                //Begin of Add by Infosys for Bug#300693 on 12 Apr 05
                List lstNotReviseType = new StringList();
                List lstNotReviseName = new StringList();
                //End of Add by Infosys for Bug#300693 on 12 Apr 05

                try{
                    //Set the context to the super user
                    ContextUtil.pushContext(
                        context, PropertyUtil.getSchemaProperty(
                                context, "person_UserAgent"),
                        DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

                   	// For each item of the Affected Item, this will method will be executed to get the immediate parent group
                    	lstAffectedItemsList = (MapList) accountForHierarchicalChangeSupport(context, lstAffectedItemsList, OPERATION_REVISE); // For Revise

                     // Start of write transaction
                    ContextUtil.startTransaction(context, true);

                    for (int i = 0; i < lstAffectedItemsList.size(); i++) {
                        strAffectedItemId = (String) ((Map) lstAffectedItemsList.get(i))
                                .get(DomainConstants.SELECT_ID);
                        //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                        strAffectedItemType = (String) ((Map) lstAffectedItemsList.get(i))
                                .get(DomainConstants.SELECT_TYPE);
                        strAffectedItemName = (String) ((Map) lstAffectedItemsList.get(i))
                                .get(DomainConstants.SELECT_NAME);
                        strAffectedItemRev = (String) ((Map) lstAffectedItemsList.get(i))
                                .get(DomainConstants.SELECT_REVISION);
                        strAffectedItemOwner = (String) ((Map) lstAffectedItemsList.get(i))
                                .get(DomainConstants.SELECT_OWNER);
                        //Added by Infosys for Bug#300664 on 25 Mar 05
                        strRevisionable = (String) ((Map) lstAffectedItemsList.get(i))
                                .get(sbRevSelect.toString());

                        strMajorRevisionable = (String) ((Map) lstAffectedItemsList.get(i)).get("current.majorrevisionable");

                        //Set the context to the owner of the Affected Item
                        ContextUtil.pushContext(
                                                    context,
                                                    strAffectedItemOwner,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);
                        //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05

                        DomainObject domItem = DomainObject.newInstance(context, strAffectedItemId);

                        //Modified by Infosys for Bug#300664 on 25 Mar 05
                        if (isLastRevision(context, domItem) &&
                                isMajorPolicy(context, domItem) ? strMajorRevisionable.equalsIgnoreCase(STR_TRUE) : strRevisionable.equalsIgnoreCase(STR_TRUE)) {

                        	// If we have a Product or one of its sub-types, we have to do things a bit differently.
                        	BusinessObject busNewRevision;
                        	boolean isPLCInstalled = FrameworkUtil.isSuiteRegistered (
                        			context,"appVersionProductLine",false, null, null);
        	    			if (isPLCInstalled && domItem.isKindOf (
        	    					context, PropertyUtil.getSchemaProperty(context, "type_Products"))) {

        	    				HashMap paramMap = new HashMap();
        	    				paramMap.put("strDerivedFromId", strAffectedItemId);
        	    				paramMap.put("strType", strAffectedItemType);
        	    				paramMap.put("strName", strAffectedItemName);
        	    				paramMap.put("strOwner", strAffectedItemOwner);

        	    				String[] plcArgs = JPO.packArgs(paramMap);
        	    				String strNewRevisionId = (String)JPO.invoke(
        	    						context, "emxProduct", null, "createProductRevisionForEC", plcArgs, String.class);

        	    				busNewRevision = new BusinessObject(strNewRevisionId);
        	    			} else {
                                busNewRevision = revise(context, domItem, true);
        	    			}

                            // Start of code modification for IR-011432
                            // if FTR Installed and the Object being revised is feature then call the method from Featurebase
                            // to connect to Master feature if one exist.
                            // added by Praveen

                            // Call the emxFeatureBase if the FTR is installed.
                            boolean isConfigurationInstall = FrameworkUtil.isSuiteRegistered(
                                    context, "appVersionVariantConfiguration", false, null, null);
                            if (isConfigurationInstall) {
                                ArrayList arrArgs = new ArrayList();
                                arrArgs.add(0, domItem.getId());
                                arrArgs.add(1, busNewRevision.getObjectId());

                                String[] arrPacked = (String[]) JPO.packArgs(arrArgs);
                                /*JPO.invoke(context, "emxFeature", arrPacked,
                                        "connectRevisedFeatureToMasterFeature", arrPacked,
                                        null);*/
                            }
                            // END of code modification for IR-011432

                            lstRevisedItemsList.addElement((String) busNewRevision
                                    .getObjectId());

                            //Begin of Add for EC Lifecycle Bug on 18-Mar-05
                            //Updating the History of Engineering Change Object
                            sbHistoryAction.delete(0,sbHistoryAction.length());
                            sbHistoryAction.append("Revised ");
                            sbHistoryAction.append(strAffectedItemType);
                            sbHistoryAction.append(" ");
                            sbHistoryAction.append(strAffectedItemName);
                            sbHistoryAction.append(" ");
                            sbHistoryAction.append(strAffectedItemRev);
                            modifyHistory(context,
                                                strObjectId,
                                                sbHistoryAction.toString(),
                                                " ");
                            //End of Add for EC Lifecycle Bug on 18-Mar-05
                        }
                        //Added by Infosys for Bug#300664 on 25 Mar 05
                        else{
                            bShowAlert = true;
                            //Added by Infosys for Bug#300693 on 14 Apr 05
                            lstNotReviseType.add(strAffectedItemType);
                            lstNotReviseName.add(strAffectedItemName);
                        }
                        //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                        //set the context back to the User Agent
                        ContextUtil.popContext(context);
                        //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                    }
                    //Connect new Revisions of the affected items as Implemented Items
                    // to
                    // the EC
                    if (lstRevisedItemsList != null && lstRevisedItemsList.size() > 0) {
                        String strRelationshipImplementedItem =
                                        DomainConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM;
                        domEC
                                .addRelatedObjects(
                                        context, new RelationshipType(
                                                strRelationshipImplementedItem), true,
                                        (String[]) lstRevisedItemsList
                                                .toArray(new String[lstRevisedItemsList
                                                        .size()]));
                    }
                    //Commit write transaction
                    ContextUtil.commitTransaction(context);

                    //Begin of Add by Infosys for Bug#300664 on 25 Mar 05
                    if(bShowAlert){
                        String strAlertMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Alert.ItemsNotRevised");
                        //Begin of add for Bug# 300693 on 14 Apr 05 by Infosys
                        StringBuffer sbAlert = new StringBuffer(strAlertMessage);
                        for(int i=0;i<lstNotReviseType.size();i++){
                            sbAlert.append("\n");
                            sbAlert.append((String)lstNotReviseType.get(i));
                            sbAlert.append("    ");
                            sbAlert.append((String)lstNotReviseName.get(i));
                        }
                        //End of add for Bug# 300693 on 14 Apr 05 by Infosys
                        //Modified for Bug# 300693 on 14 Apr 05 by Infosys
                        emxContextUtil_mxJPO.mqlNotice(context, sbAlert.toString());
                    }
                    //End of Add by Infosys for Bug#300664 on 25 Mar 05
                }catch(Exception e){
                    //Commit write transaction
                    ContextUtil.abortTransaction(context);
                    throw new FrameworkException(e.getMessage());
                }
                //Begin of Add by Vibhu,Infosys for Bug 303269 on 28 April, 05
                finally{
                    //change context back to original user in any case
                    ContextUtil.popContext(context);
                }
                //End of Add by Vibhu,Infosys for Bug 303269 on 28 April, 05
            }
    }

    /**
     * For each item of the Affected Item / Implemented Item set,
     * this will method will be executed to get the immediate parent group
     * Logic for Sorting TOPDOWN (Revise), DOWNUP(Release), NONE(Other)
     *
     * @param context
     * @param lstAffectedItemsList All the object from Affected Item / Implemente Item of selected EC
     * @param strOperation Either for Revise, Release or None
     * @return Sorted list
     * @throws MatrixException
     * @since R210
     */
    private List accountForHierarchicalChangeSupport(Context context, List lstAffectedItemsList, String strOperation)
    throws MatrixException {
        MapList orderedItems = new MapList(); //output
        String sApplicationName = null;
        List lsOfHierarchicalAffectedItemList = new ArrayList();

        //Move out items that do not implement HirarchicalChangeSupport interface
        //or those that do not care about order
        for(int i = lstAffectedItemsList.size() - 1; i >= 0 ; i--)
        {
        	 String id = (String) ((Map) lstAffectedItemsList.get(i)).get(DomainConstants.SELECT_ID);
        	 String strAffectedItemType = (String) ((Map) lstAffectedItemsList.get(i)).get(DomainConstants.SELECT_TYPE);

        	 sApplicationName = FrameworkUtil.getTypeApplicationName(context, strAffectedItemType);

        	 DomainObject obj = DomainObject.newInstance(context, strAffectedItemType, sApplicationName);
	    	 obj.setId(id);
	    	 obj.open(context);

	    	 if(!(obj instanceof HierarchicalChangeSupport) ||
                     ((HierarchicalChangeSupport)obj).getOrderForOperation(strOperation) == HierarchicalChangeSupport.NONE) // NONE - 2
             {
	    		 	Object item = lstAffectedItemsList.get(i);
           	  	    lstAffectedItemsList.remove(i);
           	  	    orderedItems.add(item);
             }
	    	 else
	    	 {
	    		 lsOfHierarchicalAffectedItemList.add(id);
	    	 }
        }

        Map parentsMap = new HashMap();
        Map childrenMap = new HashMap();

        //Fill in parentsMap and childrenMap to store expansion result
        for(int i = 0; i < lstAffectedItemsList.size(); i++)
        {
        	String objId = (String) ((Map) lstAffectedItemsList.get(i)).get(DomainConstants.SELECT_ID);
        	String strAffectedItemType = (String) ((Map) lstAffectedItemsList.get(i)).get(DomainConstants.SELECT_TYPE);

        	HierarchicalChangeSupport hirarchicalChangeSupport = null;
        	sApplicationName = FrameworkUtil.getTypeApplicationName(context, strAffectedItemType);
         	DomainObject obj = DomainObject.newInstance(context, strAffectedItemType, sApplicationName);

	    	obj.setId(objId);
	    	obj.open(context);
	    	hirarchicalChangeSupport = (HierarchicalChangeSupport)obj;

              //In List collecting the Parents ids
              List parents = hirarchicalChangeSupport.getHierarchicalParent(context);
              List lsparentsIdList = new ArrayList();
              for(int ii = 0; ii < parents.size(); ii++)
              {
              	 String strId = (String) ((Map) parents.get(ii)).get(DomainConstants.SELECT_ID);
              	 lsparentsIdList.add(strId);
              }

              //Remove the parent Ids not in the Item list
              for(int n = lsparentsIdList.size() - 1; n >=0; n--)
              {
                    if(!lsOfHierarchicalAffectedItemList.contains(lsparentsIdList.get(n)))
                    {
                    	lsparentsIdList.remove(n);
                    }
              }

              //Filling the Parents Map
              parentsMap.put(objId, lsparentsIdList);

              //Filling the Childrens Map
              for(int n = 0; n < lsparentsIdList.size(); n++)
              {
                    String parent = (String)lsparentsIdList.get(n);
                    List childrenList = (List)childrenMap.get(parent);

                    if(childrenList == null)
                    {
                          childrenList = new ArrayList();
                    }
                    if(!childrenList.contains(objId))
                    {
                          childrenList.add(objId);
                    }
                    childrenMap.put(parent, childrenList);

              }

        }

        //Empty the AffectedItems
        while(lsOfHierarchicalAffectedItemList.size() > 0)
        {
             int lastIndex = lsOfHierarchicalAffectedItemList.size() - 1;
              boolean progress = false;

                while(lastIndex >= 0)
            	  	{
            		  String objId = (String) lsOfHierarchicalAffectedItemList.get(lastIndex);
            		  DomainObject domObj = DomainObject.newInstance(context,objId);
            		  String strAffectedItemType = domObj.getInfo(context, SELECT_TYPE);

            		  HierarchicalChangeSupport hirarchicalChangeSupport = null;
                	 sApplicationName = FrameworkUtil.getTypeApplicationName(context, strAffectedItemType);
               	  	 DomainObject obj = DomainObject.newInstance(context, strAffectedItemType, sApplicationName);
        	    	 obj.setId(objId);
        	    	 obj.open(context);
        	    	 hirarchicalChangeSupport = (HierarchicalChangeSupport)obj;
        	    	 if(((HierarchicalChangeSupport)obj).getOrderForOperation(strOperation) == HierarchicalChangeSupport.TOPDOWN) { //TOPDOWN 0 - Revise
                         List parents = (List) parentsMap.get(objId);
                         if(parents == null || parents.size() == 0)
                         {
                               //No more parents, output it in orderedItems Maplist
                        	 for(int i=0;i<lstAffectedItemsList.size();i++){
                        		 Map map = (Map)lstAffectedItemsList.get(i);
                        		 String id = (String)map.get(DomainConstants.SELECT_ID);
                        		 if(id.equals(objId)){
                        			 orderedItems.add(map);
                        		 }
                        	 }
                               //Remove from the Items list
                               lsOfHierarchicalAffectedItemList.remove(lastIndex);

                               //Fix the parentsMap of this Item's children
                               List children = (List) childrenMap.get(objId);
                               if(children != null)
                               {
		                           for(int ii = 0; ii < children.size(); ii++)
		                           {
		                                 Object child = children.get(ii);
		                                 List p = (List)parentsMap.get(child);
		                                 p.remove(objId);
		                                 parentsMap.put(child, p);
		                           }
                               }
                               progress = true;
                         }
                   }
        	    	 else //BOTTOMUP - 1 - Release and Obsolescence
                     {
                           List children = (List)childrenMap.get(objId);
                           if(children == null || children.size() == 0)
                           {
                                 //No more children, output it in orderItems MapList
                        	   for(int i=0;i<lstAffectedItemsList.size();i++){
                            		 Map map = (Map)lstAffectedItemsList.get(i);
                            		 String id = (String)map.get(DomainConstants.SELECT_ID);
                            		 if(id.equals(objId)){
                            			 orderedItems.add(map);
                            		 }
                            	 }
                                 //Remove from the items list
                                 lsOfHierarchicalAffectedItemList.remove(lastIndex);

                                 //Fix the childrenMap of this item's parents
                                 List parents = (List) parentsMap.get(objId);
                                 for(int ii = 0; ii < parents.size(); ii++)
                                 {
                                       Object parent = parents.get(ii);
                                       List c = (List)childrenMap.get(parent);
                                       c.remove(objId);
                                       childrenMap.put(parent, c);
                                 }
                           }

                           progress = true;
                     }
        	    	  lastIndex--;
            	  }


            	  if(!progress)
                  {
                        throw new MatrixException(ComponentsUtil.i18nStringNow("emxComponents.CommonEngineeringChangeBase.EndLessLoop", context.getLocale().getLanguage()));
                  }
             }

            // deleting the unnecessary code and returung the final Output : IR-059697V6R2011x
        	return orderedItems;
    }

	/**
     * This common check trigger method checks if there is incomplete route
     * connected to the EC when the EC is manually promoted by owner from Review
     * or Formal Approval state.
     *
     * @param context The ematrix context of the request
     * @param args String Array with following arguments:
     *          0 - The Object Id of the context Engineering Change Object.
     *          1 - The policy of the context EC
     *          2 - The current state of the EC
     *          3 - The owner of the EC
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public int checkIncompleteRoute(Context context, String[] args)
            throws Exception, FrameworkException {
        //Get the Object Id of the context Engineering Change object.
        String strECId = args[0];
        String strECPolicy = args[1];
        String strECState = args[2];
        String strECOwner = args[3];

        String strContextUser = context.getUser();

        //Check if the promotion is done manually
        if (strContextUser.equals(strECOwner)) {
            //Get the symbolic name of the current state of the EC
            String strSybState = FrameworkUtil.reverseLookupStateName(
                    context, strECPolicy, strECState);

            //Get the actual name of the state complete for Route
            String strStateComplete = FrameworkUtil.lookupStateName(
                    context, DomainConstants.POLICY_ROUTE, "state_Complete");

            //Form the where expression to Get all the Routes not in complete state
            // for which the base state is
            // strECState
            StringBuffer sbRelWhereExp = new StringBuffer();
            sbRelWhereExp.append(SYMB_ATTRIBUTE);
            sbRelWhereExp.append(SYMB_OPEN_BRACKET);
            sbRelWhereExp.append(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE);
            sbRelWhereExp.append(SYMB_CLOSE_BRACKET);
            sbRelWhereExp.append(SYMB_EQUAL);
            sbRelWhereExp.append(strSybState);

            StringBuffer sbObjWhereExp = new StringBuffer();
            sbObjWhereExp.append(DomainConstants.SELECT_CURRENT);
            sbObjWhereExp.append(SYMB_NOT_EQUAL);
            sbObjWhereExp.append(strStateComplete);

            List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);

            DomainObject domEC = DomainObject.newInstance(
                    context, strECId);

            //Get the Routes satisfying the where criteria
            List lstIncomleteRouteList = (MapList) domEC.getRelatedObjects(
                    context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                    DomainConstants.TYPE_ROUTE, (StringList) lstObjectSelects,
                    null, false, true, (short) 1, sbObjWhereExp.toString(),
                    sbRelWhereExp.toString());

            //If the incomplete route list is not empty then block the trigger else
            // return 0
            if (lstIncomleteRouteList != null && lstIncomleteRouteList.size() > 0){
                String strAlertMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Alert.IncompleteRoute");
                emxContextUtil_mxJPO.mqlNotice(context, strAlertMessage);
                return 1;
            }
        }
        return 0;
    }

    /**
     * This trigger method checks for the connected Test Cases to the context
     * Engineering Change object when the EC is promoted from Validate state. If
     * the "Validation Required" flag attribute of the EC is set to Yes and there
     * is no Test Case connected to EC then promotion is blocked and appropriate
     * alert message is shown to the user.
     *
     * @param context The ematrix context of the request.
     * @param args The string array contains following arguments:
     *          0 - The object id of the context EC
     * @return integer 1 - If the check trigger fails
     *                 0 - If the check trigger succeeds
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public int checkTestCase(Context context, String[] args) throws Exception,
            FrameworkException {
        //Get the object id of the context Engineering Change Object
        String strObjectId = args[0];

        //If the value of the Validation Required attribute is true then check for
        // the Test Case connected to the EC by "EC Test Case" relatioship
        if (isValidationRequiredEC(
                context, strObjectId)) {
            List lstTestCaseSelects = new StringList(DomainObject.SELECT_ID);
            DomainObject domEC = DomainObject.newInstance(
                    context, strObjectId);
            List lstTestCaseList = (MapList) domEC.getRelatedObjects(
                    context, DomainConstants.RELATIONSHIP_EC_TEST_CASE,
                    DomainConstants.QUERY_WILDCARD,
                    (StringList) lstTestCaseSelects, null, false, true,
                    (short) 1, null, null);
            if (lstTestCaseList != null && lstTestCaseList.size() > 0) {
                return 0;
            } else {
                String strAlertMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Alert.NoAttachedTestCase");
                emxContextUtil_mxJPO.mqlNotice(context, strAlertMessage);
                return 1;
            }
        }
        return 0;
    }

    /**
     * This method checks the attribute "Percentage Passed" of the recent "Test
     * Execution" connected to the Engineeing Change by "EC Test
     * Execution" relationship when EC is promoted from Validate state and the
     * value of the "Validation Flag" attribute of Engineering Change is set to
     * Yes. If the percentage is less than 100% or if there is no "Test
     * Execution" connected to EC then alert message is shown to the user
     * blocking the execution.
     *
     * @param context The ematrix context of the request.
     * @param args string array containing followig elements:
     *          0 - The object id of contetx EC.
     * @return integer 1 - If the check trigger fails
     *                 0 - If the check trigger succeeds
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public int checkValidationStatus(Context context, String[] args)
            throws Exception, FrameworkException {

        //Get the object id of the context Engineering Change Object
        String strObjectId = args[0];

        //If the value of the Validation Required attribute is true then check for
        // latest Test Execution
        if (isValidationRequiredEC(
                context, strObjectId)) {

            //Get the originated date of the latest test execution
            // connected to the EC
            StringBuffer whereExprStr = new StringBuffer();
            whereExprStr.append(SYMB_TO);
            whereExprStr.append(SYMB_OPEN_BRACKET);
            whereExprStr.append(DomainConstants.RELATIONSHIP_EC_TEST_EXECUTION);
            whereExprStr.append(SYMB_CLOSE_BRACKET);
            whereExprStr.append(SYMB_DOT);
            whereExprStr.append(SYMB_FROM);
            whereExprStr.append(SYMB_DOT);
            whereExprStr.append(DomainConstants.SELECT_ID);
            whereExprStr.append("==");
            whereExprStr.append(strObjectId);

            String strOriginatedDate = MqlUtil.mqlCommand(context, "eval expr $1 on temp query bus $2 $3 $4 where $5", "maximum originated", DomainConstants.TYPE_TEST_EXECUTION, DomainConstants.QUERY_WILDCARD, DomainConstants.QUERY_WILDCARD, whereExprStr.toString());

            //Get the latest Test Execution using the date obtained by
            // the above query and retrieve it's passed percentage
            StringBuffer sbAttributeSelect = new StringBuffer();
            sbAttributeSelect.append(SYMB_ATTRIBUTE);
            sbAttributeSelect.append(SYMB_OPEN_BRACKET);
            sbAttributeSelect.append(DomainConstants.ATTRIBUTE_PERCENTAGE_PASSED);
            sbAttributeSelect.append(SYMB_CLOSE_BRACKET);

            List lstObjectSelects = new StringList(DomainObject.SELECT_ID);
            lstObjectSelects.add(sbAttributeSelect.toString());

            //Forming the where expression
            StringBuffer sbObjWhereExpression = new StringBuffer();
            sbObjWhereExpression.append("originated");
            sbObjWhereExpression.append(SYMB_EQUAL);
            sbObjWhereExpression.append(SYMB_QUOTE);
            sbObjWhereExpression.append(strOriginatedDate);
            sbObjWhereExpression.append(SYMB_QUOTE);

            DomainObject domEC = DomainObject.newInstance(
                    context, strObjectId);
            List lstTestCaseList = (MapList) domEC.getRelatedObjects(
                    context, DomainConstants.RELATIONSHIP_EC_TEST_EXECUTION,
                    DomainConstants.QUERY_WILDCARD,
                    (StringList) lstObjectSelects, null, false, true,
                    (short) 1, sbObjWhereExpression.toString(), null);

            //If there is no Test Execution connectd to the EC then fire
            // the appropriate alert otherwise check the Pass Percentage of the
            // latest Test Execution
            if (lstTestCaseList != null && lstTestCaseList.size() > 0) {
                String strPassedPercent = (String) ((Map) lstTestCaseList
                        .get(0)).get(sbAttributeSelect.toString());
                String strTestExecutionId = (String) ((Map) lstTestCaseList
                        .get(0)).get(DomainConstants.SELECT_ID);
                if (Float.parseFloat(strPassedPercent) < 100.0) {
                    String strLanguage = context.getSession().getLanguage();
                    String strAlertMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Alert.FailedTestExecution");
                    strAlertMessage = MessageUtil.substituteValues(
                            context, strAlertMessage, strTestExecutionId,
                            strLanguage);
                    emxContextUtil_mxJPO.mqlNotice(context,
                     strAlertMessage);
                    return 1;
                }
            } else {
                String strAlertMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Alert.NoAttachedTestExecution");
                emxContextUtil_mxJPO.mqlNotice(context, strAlertMessage);
                return 1;
            }
        }
        return 0;
    }

    /**
     * This method checks the value of the "Validation Required" attribute of EC
     * and returns true if value is "Yes" otherwise returns false.
     *
     * @param context The ematrix context of the request.
     * @param strECId The object id of EC.
     * @return boolean true - if value of the atribute "Validation Required" is Yes
     *         otherwise returns false.
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    protected boolean isValidationRequiredEC(Context context, String strECId)
            throws Exception, FrameworkException {

        //Form the object select list
        StringBuffer sbAttributeSelect = new StringBuffer();
        sbAttributeSelect.append(SYMB_ATTRIBUTE);
        sbAttributeSelect.append(SYMB_OPEN_BRACKET);
        sbAttributeSelect.append(DomainConstants.ATTRIBUTE_VALIDATION_REQUIRED);
        sbAttributeSelect.append(SYMB_CLOSE_BRACKET);
        List lstObjectSelects = new StringList(sbAttributeSelect.toString());
        Map mpObjectInfo = new HashMap();

        //Get the value of the attribute and return true if value is "Yes"
        // otherwise return false
        DomainObject domEC = DomainObject.newInstance(
                context, strECId);
        mpObjectInfo = (Map) domEC.getInfo(
                context, (StringList) lstObjectSelects);

        String strAttribValue = (String) mpObjectInfo.get(sbAttributeSelect
                .toString());
        //Modified for bug#302310,302308 by Infosys on 21 Apr 05
        if (strAttribValue.equalsIgnoreCase(RANGE_YES)) {
            return true;
        } else {
            return false;
        }
    }

   /**
        * This trigger method promotes all the Implemented Items connected to the
        * EC by EC Implemented Item relationship to the Release state when EC is
        * promoted to the Complete state.If Release state doesn'e exists in the
        * lifecycle of the implemented item then no action is taken.
        *
        * @param context The ematrix context object.
        * @param args The string array containing following arguments:
        *          0 - The object id of the context EC.
        * @throws Exception
        * @throws FrameworkException
        * @since AEF10.6
        */

       public void releaseImplementedItems(Context context, String[] args)
               throws Exception, FrameworkException {
           //Get the object id of the context Ec
           String strObjectId = args[0];

           //Get all the implemented items connected to the context Engineering
           // Change by EC Implemented Item relationship
           DomainObject domEC = DomainObject.newInstance(
                   context, strObjectId);
           List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
           //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
           lstObjectSelects.add(DomainConstants.SELECT_OWNER);
           lstObjectSelects.add(DomainConstants.SELECT_TYPE);
           lstObjectSelects.add(DomainConstants.SELECT_NAME);
           lstObjectSelects.add(DomainConstants.SELECT_REVISION);
           //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05

           List lstImplementedtemsList = (MapList) domEC.getRelatedObjects(
                   context, DomainConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM,
                   DomainConstants.QUERY_WILDCARD,(StringList) lstObjectSelects,
                   null, false, true, (short) 1, null, null);

           try{
               //Set the context to the super user
               ContextUtil.pushContext(
                       context, PropertyUtil.getSchemaProperty(
                               context, "person_UserAgent"),
                       DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

               //Promote all the implemented items to the release state
               if (lstImplementedtemsList != null && lstImplementedtemsList.size() > 0) {

                   DomainObject domItem = DomainObject.newInstance(context);
                   String strStateRelease = DomainConstants.EMPTY_STRING;

                   //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                   String strOwner = DomainConstants.EMPTY_STRING;
                   String strName = DomainConstants.EMPTY_STRING;
                   String strType = DomainConstants.EMPTY_STRING;
                   String strRev = DomainConstants.EMPTY_STRING;
                   StringBuffer sbHistoryAction = new StringBuffer();
                   //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05

               	// For each item of the Implemented Item, this will method will be executed to get the immediate parent group
                   lstImplementedtemsList = accountForHierarchicalChangeSupport(context, lstImplementedtemsList, OPERATION_RELEASE); // For Release
                   //Start write transaction
                   ContextUtil.startTransaction(context, true);

                   //Promote all the implemented items to the Release state if
                   // "Release" state exits in the lifecycle of that feature
                   for (int i = 0; i < lstImplementedtemsList.size(); i++) {
                       domItem.setId((String) ((Map) lstImplementedtemsList.get(i))
                               .get(DomainObject.SELECT_ID));
                       strStateRelease = FrameworkUtil.lookupStateName(
                               context, domItem.getPolicy(
                                       context).getName(), "state_Release");

                        //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                       strOwner = (String) ((Map) lstImplementedtemsList.get(i))
                               .get(DomainConstants.SELECT_OWNER);
                       strType = (String) ((Map) lstImplementedtemsList.get(i))
                               .get(DomainConstants.SELECT_TYPE);
                       strName = (String) ((Map) lstImplementedtemsList.get(i))
                               .get(DomainConstants.SELECT_NAME);
                       strRev = (String) ((Map) lstImplementedtemsList.get(i))
                               .get(DomainConstants.SELECT_REVISION);
                        //Set the context to the owner of the Implemented Item
                        ContextUtil.pushContext(
                                                    context,
                                                    strOwner,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);
                       //End of Add by Infosys for EC lifecycle bug on 18-Mar-05

                       if (strStateRelease != null
                               && !strStateRelease
                                       .equals(DomainConstants.EMPTY_STRING)
                               && !strStateRelease.equalsIgnoreCase("null")) {
                           while (!strStateRelease.equals(domItem.getInfo(
                                   context, DomainConstants.SELECT_CURRENT))) {
                               domItem.promote(context);
                           }
                       }
                       //Begin of Add by Infosys for EC lifecycle bug on 18-Mar-05
                        sbHistoryAction.delete(0,sbHistoryAction.length());
                        sbHistoryAction.append("Released ");
                        sbHistoryAction.append(strType);
                        sbHistoryAction.append(" ");
                        sbHistoryAction.append(strName);
                        sbHistoryAction.append(" ");
                        sbHistoryAction.append(strRev);
                        modifyHistory(context,
                                            strObjectId,
                                            sbHistoryAction.toString(),
                                            " ");
                       //set the context back to the user agent
                       ContextUtil.popContext(context);
                      //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                   }
                   //Commit write transaction
                   ContextUtil.commitTransaction(context);
               }
           }catch(Exception e){
                   //Abort write transaction
                   ContextUtil.abortTransaction(context);
                   emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
                   throw new FrameworkException(e.getMessage());
           }
        //Begin of Add by Vibhu,Infosys for Bug 303269 on 28 April, 05
        finally{
            //change context back to original user in any case
            ContextUtil.popContext(context);
        }
        //End of Add by Vibhu,Infosys for Bug 303269 on 28 April, 05
    }

   /**
        * This trigger method promotes the affected items connected to the EC by
        * "EC Affected Item" relationship to Release or Obsolete state depending
        * upon the value of the "Requested Change" attribute on relationship.
        *
        * @param context The ematrix context of the request
        * @param args The string array containgin the following areguments:
        *          0 - The object id of the context EC
        * @throws Exception
        * @throws FrameworkException
        * @since AEF10.6
        */
       public void promoteAffectedItems(Context context, String[] args)
               throws Exception, FrameworkException {
           //Get the object id of the context EC
           String strObjectId = args[0];

           //Get all the Affected Items connected to the context EC by Affected
           // Item relationship for which the value of the attribute Requested
           // Change is either "For Release" or "For Obsolensce"
           DomainObject domEC = DomainObject.newInstance(
                   context, strObjectId);

           List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
           //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
           lstObjectSelects.add(DomainConstants.SELECT_OWNER);
           lstObjectSelects.add(DomainConstants.SELECT_TYPE);
           lstObjectSelects.add(DomainConstants.SELECT_NAME);
           lstObjectSelects.add(DomainConstants.SELECT_REVISION);
           //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05

           StringBuffer sbAttribSelect = new StringBuffer();
           sbAttribSelect.append(SYMB_ATTRIBUTE);
           sbAttribSelect.append(SYMB_OPEN_BRACKET);
           sbAttribSelect.append(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE);
           sbAttribSelect.append(SYMB_CLOSE_BRACKET);

           List lstRelSelects = new StringList(sbAttribSelect.toString());

           StringBuffer sbRelWhereExpression = new StringBuffer();
           sbRelWhereExpression.append(SYMB_ATTRIBUTE);
           sbRelWhereExpression.append(SYMB_OPEN_BRACKET);
           sbRelWhereExpression.append(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE);
           sbRelWhereExpression.append(SYMB_CLOSE_BRACKET);
           sbRelWhereExpression.append(SYMB_EQUAL);
           sbRelWhereExpression.append(SYMB_QUOTE);
           //Modified for bug#302310,302308 by Infosys on 21 Apr 05
           sbRelWhereExpression.append(RANGE_FOR_RELEASE);
           sbRelWhereExpression.append(SYMB_QUOTE);
           sbRelWhereExpression.append(STR_SPACE);
           sbRelWhereExpression.append(SYMB_OR);
           sbRelWhereExpression.append(STR_SPACE);
           sbRelWhereExpression.append(SYMB_ATTRIBUTE);
           sbRelWhereExpression.append(SYMB_OPEN_BRACKET);
           sbRelWhereExpression.append(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE);
           sbRelWhereExpression.append(SYMB_CLOSE_BRACKET);
           sbRelWhereExpression.append(SYMB_EQUAL);
           sbRelWhereExpression.append(SYMB_QUOTE);
           //Modified for bug#302310,302308 by Infosys on 21 Apr 05
           sbRelWhereExpression.append(RANGE_FOR_OBSOLESCENCE);
           sbRelWhereExpression.append(SYMB_QUOTE);

           List lstAffectedItemsList = (MapList) domEC.getRelatedObjects(
                   context, DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM,
                   DomainConstants.QUERY_WILDCARD,(StringList) lstObjectSelects,
                   (StringList) lstRelSelects,false, true, (short) 1, null,
                   sbRelWhereExpression.toString());
           try{
                  //Set the context to the super user
                  ContextUtil.pushContext(
                           context, PropertyUtil.getSchemaProperty(
                                   context, "person_UserAgent"),
                           DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                   // Start of write transaction
                   ContextUtil.startTransaction(context, true);

                //Start of Add By Infosys for Bug # 317987 on 10-Apr-06
                   StringBuffer sbAlertObjectsList = new StringBuffer(20);
                   boolean bshowAlert = false;
                //End of Add By Infosys for Bug # 317987  on 10-Apr-06

               if (lstAffectedItemsList != null && lstAffectedItemsList.size() > 0) {
                   DomainObject domItem = DomainObject.newInstance(context);
                   String strState = DomainConstants.EMPTY_STRING;
                   String strReqChange = DomainConstants.EMPTY_STRING;

                   //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                   String strOwner = DomainConstants.EMPTY_STRING;
                   String strName = DomainConstants.EMPTY_STRING;
                   String strType = DomainConstants.EMPTY_STRING;
                   String strRev = DomainConstants.EMPTY_STRING;
                   StringBuffer sbHistoryAction = new StringBuffer();
                   //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05

               	// For each item of the Affected Item, this will method will be executed to get the immediate parent group
                   lstAffectedItemsList = accountForHierarchicalChangeSupport(context, lstAffectedItemsList, OPERATION_RELEASE); // For Release
                   for (int i = 0; i < lstAffectedItemsList.size(); i++) {
                       domItem.setId((String) ((Map) lstAffectedItemsList.get(i))
                               .get(DomainConstants.SELECT_ID));
                       strReqChange = (String) ((Map) lstAffectedItemsList.get(i))
                               .get(sbAttribSelect.toString());

                       //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                       strOwner = (String) ((Map) lstAffectedItemsList.get(i))
                               .get(DomainConstants.SELECT_OWNER);
                       strType = (String) ((Map) lstAffectedItemsList.get(i))
                               .get(DomainConstants.SELECT_TYPE);
                       strName = (String) ((Map) lstAffectedItemsList.get(i))
                               .get(DomainConstants.SELECT_NAME);
                       strRev = (String) ((Map) lstAffectedItemsList.get(i))
                               .get(DomainConstants.SELECT_REVISION);

                        //Set the context to the owner of the Affected Item
                        ContextUtil.pushContext(
                                                    context,
                                                    strOwner,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);
                        //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                       //Modified for bug#302310,302308 by Infosys on 21 Apr 05
                       if (strReqChange.equals(RANGE_FOR_RELEASE)) {
                           strState = FrameworkUtil.lookupStateName(
                                   context, domItem.getPolicy(
                                           context).getName(), "state_Release");
                         if (strState != null
                                   && !strState.equals(DomainConstants.EMPTY_STRING)
                                   && !strState.equalsIgnoreCase("null")) {
                               while (!strState.equals(domItem.getInfo(
                                       context, DomainConstants.SELECT_CURRENT))) {
                                   domItem.promote(context);
                               }
                           //History Item should be added only if the object gets successfully promoted to the release state, else no addition to history.
                           //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                            sbHistoryAction.delete(0,sbHistoryAction.length());
                            sbHistoryAction.append("Released ");
                            sbHistoryAction.append(strType);
                            sbHistoryAction.append(" ");
                            sbHistoryAction.append(strName);
                            sbHistoryAction.append(" ");
                            sbHistoryAction.append(strRev);
                            modifyHistory(context,
                                                strObjectId,
                                                sbHistoryAction.toString(),
                                                " ");
                            //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05

                           }
                            //Start of Add By Infosys for Bug # 317987 on 10-Apr-06
                           else
                           {
                               bshowAlert = true;
                               sbAlertObjectsList.append("\n");
                               sbAlertObjectsList.append(strType);
                               sbAlertObjectsList.append("  ");
                               sbAlertObjectsList.append(strName);
                           }
                           //End of Add By Infosys for Bug # 317987 on 10-Apr-06
                       } else {
                           strState = FrameworkUtil.lookupStateName(
                                   context, domItem.getPolicy(
                                           context).getName(), "state_Obsolete");
                           if (strState != null
                                   && !strState.equals(DomainConstants.EMPTY_STRING)
                                   && !strState.equalsIgnoreCase("null")) {
                               while (!strState.equals(domItem.getInfo(
                                       context, DomainConstants.SELECT_CURRENT))) {
                                   domItem.promote(context);
                               }

                           //History Item should be added only if the object gets successfully promoted to the release state, else no addition to history.
                           //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                            //Modified By Infosys for Bug # 317987 on 10-Apr-06
                            sbHistoryAction.delete(0,sbHistoryAction.length());
                            sbHistoryAction.append("Promoted to Obsolete ");
                            sbHistoryAction.append(strType);
                            sbHistoryAction.append(" ");
                            sbHistoryAction.append(strName);
                            sbHistoryAction.append(" ");
                            sbHistoryAction.append(strRev);
                            modifyHistory(context,
                                                strObjectId,
                                                sbHistoryAction.toString(),
                                                " ");
                            //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                           }
                           //Start of Add By Infosys for Bug # 317987 on 10-Apr-06
                           else
                           {
                            bshowAlert = true;
                            sbAlertObjectsList.append("\n");
                            sbAlertObjectsList.append(strType);
                            sbAlertObjectsList.append(" ");
                            sbAlertObjectsList.append(strName);
                           }
                        //End of Add By Infosys for Bug # 317987 on 10-Apr-06
                       }

                        //Begin of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                       //set the context back to the user agent
                       ContextUtil.popContext(context);
                      //End of Add by Infosys for EC Lifecycle Bug on 18-Mar-05
                   }

    //Start of Add By Infosys for Bug # 317987 on 10-Apr-06
         if (bshowAlert)
         {
            String strAlertMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Alert.ItemsNotPromotedToRelOrObs");
            StringBuffer sbAlert = new StringBuffer(strAlertMessage);
           sbAlert.append(sbAlertObjectsList.toString());
           emxContextUtil_mxJPO.mqlNotice(context, sbAlert.toString());
        }//end of if for bShowAlert.
    //End of Add By Infosys for Bug # 317987 on 10-Apr-06

               }
               // Commit of write transaction
               //Modfied by Infosys for EC Lifecycle Bug on 18-Mar-05
               ContextUtil.commitTransaction(context);
           }catch(Exception e){
               // Commit of write transaction
               //Modfied by Infosys for EC Lifecycle Bug on 18-Mar-05
               //Modified by Infosys for Bug 303269 on 28 April, 05
               ContextUtil.abortTransaction(context);
               emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());

               throw new FrameworkException(e.getMessage());
           }
        //Begin of Add by Vibhu,Infosys for Bug 303269 on 28 April, 05
        finally{
            //change context back to original user in any case
            ContextUtil.popContext(context);
        }
        //End of Add by Vibhu,Infosys for Bug 303269 on 28 April, 05

    }
    /**
     * Get the list of all Objects which are connected to the context Engineering Change
     * object as Affected Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of Affected Items for this EC
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllAffectedItems(Context context, String[] args) throws Exception {
        //unpacking the arguments from variable args
        HashMap programMap         = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId         = (String)programMap.get("objectId");
        //getting the relationship for Affected Items
        String strRelAffectedItems = DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM;

        //Initializing the return type
        MapList relBusObjPageList = new MapList();
        //Business Objects are selected by its Ids
        StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
        //Relationships are selected by its Ids
        StringList relSelects     = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

        //the number of levels to expand, 1 equals expand one level, 0 equals expand all
        short recurseToLevel = 1;
        //retrieving Affected Items list from context EC object
        this.setId(strParentId);

        relBusObjPageList = getRelatedObjects(context,
                                              strRelAffectedItems,
                                              DomainConstants.QUERY_WILDCARD,
                                              objectSelects,
                                              relSelects,
                                              false,
                                              true,
                                              recurseToLevel,
                                              DomainConstants.EMPTY_STRING,
                                              DomainConstants.EMPTY_STRING);
        //returns list of object ids of expanded Affected Items
        return  relBusObjPageList;
    }

    /**
     * Get the list of all Objects which are connected to the context Engineering Change object
     * as Implemented Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix<code>MapList</code> object having the list of Implemented Items for the context EC
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     *
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllImplementedItems (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId = (String)programMap.get("objectId");
        //getting the relationship for Implemented Items
        String strRelECImplementedItem = DomainConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM;
        //Initializing the return type
        MapList relBusObjPageList      = new MapList();
        //Business Objects are selected by its Ids
        StringList objectSelects       = new StringList(DomainConstants.SELECT_ID);
        //Relationships are selected by its Ids
        StringList relSelects          = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //the number of levels to expand, 1 equals expand one level, 0 equals expand all
        short recurseToLevel = 1;
        //retrieving ImplementedItems List from EC Context
        this.setId(strParentId);
        relBusObjPageList = getRelatedObjects(context,
                                              strRelECImplementedItem,
                                              DomainConstants.QUERY_WILDCARD,
                                              objectSelects,
                                              relSelects,
                                              false,
                                              true,
                                              recurseToLevel,
                                              DomainConstants.EMPTY_STRING,
                                              DomainConstants.EMPTY_STRING);
        //returns list of object ids of expanded Implemented Items
        return  relBusObjPageList;
    }

    /**
     * Get the list of all Objects connected to the context Engineering Change object as
     * Satisfied Items using 'Resolved To' relationship
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return        a <code>MapList</code> object having the list of Implemented Items for this EC
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllSatisfiedItems (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId = (String)programMap.get("objectId");
        //getting the relationship for Satisfied Items
        String strRelResolvedTo = DomainConstants.RELATIONSHIP_RESOLVED_TO;
        //Initializing the return type
        MapList relBusObjPageList = new MapList();
        //Business Objects are selected by its Ids
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        //Relationships are selected by its Ids
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //the number of levels to expand, 1 equals expand one level, 0 equals expand all
        short recurseToLevel = 1;
        //retrieving ImplementedItems List from EC Context
        this.setId(strParentId);
        relBusObjPageList = getRelatedObjects(context,
                                              strRelResolvedTo,
                                              DomainConstants.QUERY_WILDCARD,
                                              objectSelects,
                                              relSelects,
                                              true,
                                              false,
                                              recurseToLevel,
                                              DomainConstants.EMPTY_STRING,
                                              DomainConstants.EMPTY_STRING);
        return  relBusObjPageList;
    }

       /**
         * Method call to get the Name in the Last Name, First Name format.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the HashMap containing the following arguments
         *      objectList - MapList containn the list of busines objetcs
         *      paramList - HashMap containg the arguments like reportFormat,ObjectId, SuiteDirectory, TreeId
         * @return Object - Vector containing names in last name, first name format
         * @throws Exception if the operation fails
         * @since          Common 10-6
         *
         */
        public Vector getCompleteName (Context context, String[] args) throws Exception {
             //Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            //Gets the objectList from args
            MapList relBusObjPageList = (MapList)programMap.get("objectList");
            HashMap paramList = (HashMap)programMap.get("paramList");
            String strReportFormat = (String) paramList.get("reportFormat");

            //Used to construct the HREF
            String strSuiteDir = (String)paramList.get("SuiteDirectory");
            String strJsTreeID = (String)paramList.get("jsTreeID");
            String strParentObjectId = (String)paramList.get("objectId");
            Vector vctCompleteName = new Vector();
            //No of objects
            int iNoOfObjects = relBusObjPageList.size();
            String strObjId = null;
            String strRelId = null;
            String strFirstName = null;
            String strLastName = null;
            String arrObjId[] = new String[iNoOfObjects];
            String arrRelId[] = new String[iNoOfObjects];
            //Getting the bus ids for objects in the table
            for (int i = 0; i < iNoOfObjects; i++) {
                Object obj = relBusObjPageList.get(i);
                if (obj instanceof HashMap) {
                    arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                    arrRelId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                }
                else if (obj instanceof Hashtable)
                {
                    arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                    arrRelId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                }
            }
            StringList listSelect = new StringList(2);
            String strAttrb1 = "attribute[" + DomainConstants.ATTRIBUTE_FIRST_NAME+ "]";
            String strAttrb2 = "attribute[" + DomainConstants.ATTRIBUTE_LAST_NAME+ "]";
            listSelect.addElement(strAttrb1);
            listSelect.addElement(strAttrb2);

            //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
            BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

            for (int i = 0; i < iNoOfObjects; i++) {
                strObjId = arrObjId[i];
                strRelId = arrRelId[i];
                strFirstName = attributeList.getElement(i).getSelectData(strAttrb1);
                strLastName = attributeList.getElement(i).getSelectData(strAttrb2);
                //Constructing the HREF
                String strFullName = null;
                if(strReportFormat!=null&&strReportFormat.equals("null")==false&&strReportFormat.equals("")==false)
                {
                      strFullName = strLastName + " " + strFirstName;
                }
                else
                {
                    /*Begin of modify : by Infosys for Bug#301835 on 4/13/2005*/
                    strFullName = "<img src = \"images/iconSmallPerson.gif\"></img><b><A HREF=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?mode=insert&amp;name="+XSSUtil.encodeForJavaScript(context,strLastName)+XSSUtil.encodeForJavaScript(context,strFirstName)+"&amp;treeMenu=type_Person&amp;emxSuiteDirectory="//Modified By Infosys for Bug # 304580 Date 05/18/2005
                    /*End of modify : by Infosys for Bug#301835 on 4/13/2005*/
                        + strSuiteDir + "&amp;relId=" + strRelId + "&amp;parentOID="
                        + strParentObjectId + "&amp;jsTreeID=" + strJsTreeID + "&amp;objectId="
                        + strObjId + "', 'null', 'null', 'false', 'content')\" class=\"object\">"
                        + strLastName + ", " + strFirstName + "</A></b>";
                }
                //Adding into the vector
                vctCompleteName.add(strFullName);
            }
            return  vctCompleteName;
        }

       /**
         * Method call to get the email as an link to send mails using the client.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the HashMap containing the following arguments
         *      objectList - MapList containn the list of busines objetcs
         *      paramList - HasMap containg the argument reportFormat
         * @return Object - Vector of email ids
         * @throws Exception if the operation fails
         * @since          Common 10-6
         */
        public Vector getAssigneeEmail (Context context, String[] args) throws Exception {
            //Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            //Gets the objectList from args
            MapList relBusObjPageList = (MapList)programMap.get("objectList");
            Vector vctAssigneeMailList = new Vector();
            HashMap paramList = (HashMap)programMap.get("paramList");
            String strReportFormat=(String)paramList.get("reportFormat");

            if (!(relBusObjPageList != null)){
                throw  new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonEngineeringChangeBase.ContextNoObjects", context.getLocale().getLanguage()));
            }
            //Number of objects
            int iNoOfObjects = relBusObjPageList.size();
            String arrObjId[] = new String[iNoOfObjects];
            //Getting the bus ids for objects in the table
            for (int i = 0; i < iNoOfObjects; i++) {
                Object obj = relBusObjPageList.get(i);
                if (obj instanceof HashMap) {
                    arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                }
                else if (obj instanceof Hashtable)
                {
                    arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                }
            }

            StringList listSelect = new StringList(1);
            String strAttrb1 = "attribute[" + DomainConstants.ATTRIBUTE_EMAIL_ADDRESS+ "]";
            listSelect.addElement(strAttrb1);

            //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
            BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

            for (int i = 0; i < iNoOfObjects; i++) {
                //Getting the email ids from the Map
                String strEmailId = attributeList.getElement(i).getSelectData(strAttrb1);
                String strEmailAdd = null;
               if(strReportFormat!=null&&strReportFormat.equals("null")==false&&strReportFormat.equals("")==false){
                    vctAssigneeMailList.add(XSSUtil.encodeForHTML(context, strEmailId));
                } else {
                    strEmailAdd = "<B><A HREF=\"mailto:" + strEmailId + "\">" + XSSUtil.encodeForHTML(context, strEmailId)+ "</A></B>";
                    vctAssigneeMailList.add(strEmailAdd);
                }
            }
            return  vctAssigneeMailList;
        }

        /**
         * Gets all the persons with which this Engineering Change object is connected with
         * Assigned EC relationship
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args    holds the following input arguments:
         *           0 -  HashMap containing one String entry for key "objectId"
         * @return        a <code>MapList</code> object having the list of Assignees for this EC
         * @throws        Exception if the operation fails
         * @since         Common 10-6
         **
         */

        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getEngineeringChangeAssignees(Context context, String[] args) throws Exception {
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            //getting parent object Id from args
            String strParentId = (String)programMap.get("objectId");
            //getting the symbolic relationship from PropertyUtil
            String strRelAssignedEC= DomainConstants.RELATIONSHIP_ASSIGNED_EC;
            //Initializing the return type
            MapList relBusObjPageList = new MapList();
            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
            //Relationships are selected by its Ids
            StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //the number of levels to expand, 1 equals expand one level, 0 equals expand all
            short recurseToLevel = 1;
            //retrieving Assignees from EC Context
            this.setId(strParentId);
            relBusObjPageList = getRelatedObjects(context,
                                                  strRelAssignedEC,
                                                  DomainConstants.QUERY_WILDCARD,
                                                  objectSelects,
                                                  relSelects,
                                                  true,
                                                  false,
                                                  recurseToLevel,
                                                  DomainConstants.EMPTY_STRING,
                                                  DomainConstants.EMPTY_STRING);
            return  relBusObjPageList;
    }

    /**
     * Get the list of all Impact Analysis connected to Engineering Change in the List Page.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return ObjectList ofImpact Analysis
     * @throws Exception if the operation fails
     * @since         Common 10-6
     **
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getEngineeringChangeImpactAnalysis (Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //getting parent object Id from args
        String strParentId = (String)programMap.get("objectId");
        //getting symbolic object Type from PropertyUtil
        String strImpactType = DomainConstants.TYPE_IMPACT_ANALYSIS;
        //getting the symbolic relationship from PropertyUtil
        String strRelImpactAnalysis = DomainConstants.RELATIONSHIP_EC_IMPACT_ANALYSIS;
        //initializing return type
        MapList relBusObjPageList = new MapList();
        //Impact Analysis Objects is selected by its Ids
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        //Relationships are selected by its Ids
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //the number of levels to expand, 1 equals expand one level, 0 equals expand all
        short recurseToLevel = 1;
        //retrieving Impact Analysis List from Product  Context
        this.setId(strParentId);
        relBusObjPageList = getRelatedObjects(context,
                                              strRelImpactAnalysis,
                                              strImpactType,
                                              objectSelects,
                                              relSelects,
                                              false,
                                              true,
                                              recurseToLevel,
                                              DomainConstants.EMPTY_STRING,
                                              DomainConstants.EMPTY_STRING);
        //returns list of Objects
        return  relBusObjPageList;
    }

    /**
     * To obtain all the Impact Analysis from the database that matches the search criteria.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the HashMap containing the following arguments
     *            queryLimit
     *            TypeImpactAnalysis
     *            Name
     *            State
     *            OwnerDiaplay
     *            Description
     *            Vault Option
     *            Vaults
     * @return MapList , the Impact Analysis Object ids matching the search criteria
     * @throws Exception if the operation fails
     * @since  Common 10-6
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getAllImpactAnalysis(Context context, String[] args)throws Exception{

        Map programMap = (Map) JPO.unpackArgs(args);
        short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));
        String strType = (String)programMap.get("TypeImpactAnalysis");
        if (strType==null || strType.equals(""))
        {
            strType = SYMB_WILD;
        }
        String strName = (String)programMap.get("Name");
        if (strName==null || strName.equals(""))
        {
            strName = SYMB_WILD;
        }

        String strState = (String)programMap.get("State");
        String strOwner = (String)programMap.get("OwnerDisplay");
        if ( strOwner == null || strOwner.equals("") )
        {
            strOwner = SYMB_WILD;
        }

        String strDesc = (String)programMap.get("Description");

        String strVault = null;
        String strVaultOption = (String)programMap.get("vaultOption");

        if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                                strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
         else
                strVault = (String)programMap.get("vaults");

        StringList select = new StringList(1);
        select.addElement(DomainConstants.SELECT_ID);

        boolean start = true;
        StringBuffer sbWhereExp = new StringBuffer(120);

        if ( strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
        if (start) {
          sbWhereExp.append(SYMB_OPEN_PARAN);
          start = false;
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
        sbWhereExp.append(SYMB_MATCH);
        sbWhereExp.append(SYMB_QUOTE);
        sbWhereExp.append(strDesc);
        sbWhereExp.append(SYMB_QUOTE);
        sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) ) {
            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
            } else {
                sbWhereExp.append(SYMB_AND);
              }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(DomainConstants.SELECT_CURRENT);
            sbWhereExp.append(SYMB_MATCH);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(strState);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        String strFilteredExpression = getFilteredExpression(context,programMap);

        if ( (strFilteredExpression != null) ) {
            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
            } else {
                sbWhereExp.append(SYMB_AND);
              }
            sbWhereExp.append(strFilteredExpression);
        }

        if (!start) {
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        MapList mapList = null;
        mapList = DomainObject.findObjects(context,
                                           strType,
                                           strName,
                                           SYMB_WILD,
                                           strOwner,
                                           strVault,
                                           sbWhereExp.toString(),
                                           "",
                                           true,
                                           select,
                                           sQueryLimit);
        return mapList;
  }

    /**
     * Get the list of all Engineering Change objects that are connected to the PRC object through EC Affected Items relationship
     *
     * @param context  the eMatrix <code>Context</code> object
     * @param args     holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return         a <code>MapList</code> object having the list of Engineering Change objects
     *                  connected through EC Affected Items relationship
     * @throws         Exception if the operation fails
     * @since          Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getObjectECList(Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap           = (HashMap)JPO.unpackArgs(args);

        //getting parent object Id from args
        String strParentId           = (String)programMap.get("objectId");

        //initializing return type
        MapList relBusObjPageList    = new MapList();

        // forming the where clause
        String strWhereExpression    = "";

        relBusObjPageList = getECList(context, strParentId, strWhereExpression);

        //returns list of Engineering Change Objects
        return  relBusObjPageList;
    }

    /**
     * Get the list of all Engineering Change Objects that can be returned based on search criteria passed in as argument
     *
     * @param context         the eMatrix <code>Context</code> object
     * @param args            contains a Map with the following input arguments or entries:
     *    queryLimit          limit for displaying search query results
     *    hdnType             the admin 'type'of the object to search for
     *    txtName             the 'name' or 'name pattern' to search for
     *    txtRevision         the 'revision' pattern to search for
     *    txtDescription      the 'description' pattern to search for
     *    txtOwner            the 'owner' pattern to search for
     *    txtState.           the object 'state' pattern to search for
     *    txtCategoryOfChange the object 'CategoryOfChange' pattern to search for
     *    txtSeverity         the object 'Severity' pattern to search for
     *    vaultOption         the vault search option
     * @return                a <code>MapList</code> object having the list of objects satisfying the search criteria
     * @throws                Exception if the operation fails
     * @since                 Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getEngineeringChangeSearchResults(Context context, String[] args)
        throws Exception {

        MapList mapList = null;
        try {
            Map programMap    = (Map) JPO.unpackArgs(args);
            short sQueryLimit =
                    (short) (java
                            .lang
                            .Integer
                            .parseInt((String) programMap.get("queryLimit")));

            String strType = (String) programMap.get("hdnType");

            if (strType == null
                    || strType.equals("")
                    || "null".equalsIgnoreCase(strType)) {
                strType = DomainConstants.QUERY_WILDCARD;
            }

            String strName = (String) programMap.get("txtName");

            if (strName == null
                    || strName.equals("")
                    || "null".equalsIgnoreCase(strName)) {
                strName = DomainConstants.QUERY_WILDCARD;
            }

            String strRevision = (String) programMap.get("txtRevision");

            if (strRevision == null
                    || strRevision.equals("")
                    || "null".equalsIgnoreCase(strRevision)) {
                strRevision = DomainConstants.QUERY_WILDCARD;
            } else {
                strRevision = strRevision.trim();
            }

            String strDesc = (String) programMap.get("txtDescription");

            String strOwner = (String) programMap.get("txtOwner");

            if (strOwner == null
                    || strOwner.equals("")
                    || "null".equalsIgnoreCase(strOwner)) {
                strOwner = DomainConstants.QUERY_WILDCARD;
            } else {
                strOwner = strOwner.trim();
            }

            String strState = (String) programMap.get("txtState");

            if (strState == null
                    || strState.equals("")
                    || "null".equalsIgnoreCase(strState)) {
                strState = DomainConstants.QUERY_WILDCARD;
            } else {
                strState = strState.trim();
            }

            String strCategoryOfChange = (String) programMap.get("txtCategoryOfChange");

            if (strCategoryOfChange == null
                    || strCategoryOfChange.equals("")
                    || "null".equalsIgnoreCase(strCategoryOfChange)) {
                strCategoryOfChange = DomainConstants.QUERY_WILDCARD;
            } else {
                strCategoryOfChange = strCategoryOfChange.trim();
            }

            String strSeverity = (String) programMap.get("txtSeverity");

            if (strSeverity == null
                    || strSeverity.equals("")
                    || "null".equalsIgnoreCase(strSeverity)) {
                strSeverity = DomainConstants.QUERY_WILDCARD;
            } else {
                strSeverity = strSeverity.trim();
            }

            String strVault = "";
            String strVaultOption = (String) programMap.get(VAULT_OPTION);

            if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)) {
                strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
            } else {
                strVault = (String)programMap.get("vaults");
            }

            StringList slSelect = new StringList(1);
            slSelect.addElement(DomainConstants.SELECT_ID);

            boolean bStart = true;
            StringBuffer sbWhereExp = new StringBuffer(150);

            if (strDesc != null
                    && (!strDesc.equals(DomainConstants.QUERY_WILDCARD))
                    && (!strDesc.equals(""))
                    && !("null".equalsIgnoreCase(strDesc))) {
                if (bStart) {
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    bStart = false;
            }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
            sbWhereExp.append(SYMB_MATCH);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(strDesc);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
            }
            if (strState != null
                    && (!strState.equals(DomainConstants.QUERY_WILDCARD))
                    && (!strState.equals(""))
                    && !("null".equalsIgnoreCase(strState))) {
                if (bStart) {
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    bStart = false;
                } else {
                    sbWhereExp.append(SYMB_AND);
                    }
                sbWhereExp.append(SYMB_OPEN_PARAN);
                sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                sbWhereExp.append(SYMB_MATCH);
                //Added by Infosys for EC-search bug, 08 June 2005
                sbWhereExp.append(STR_CONST);
                sbWhereExp.append(SYMB_QUOTE);
                sbWhereExp.append(strState);
                sbWhereExp.append(SYMB_QUOTE);
                sbWhereExp.append(SYMB_CLOSE_PARAN);
            }
            //Added by Infosys for Bug#304580 on 17 May 2005
            else if(strState.equals(DomainConstants.QUERY_WILDCARD))
            {
                String strECValidStatespacked = EnoviaResourceBundle.getProperty(context,"emxComponents.EngineeringChange.ValidStates");
                int nextIndex,prevIndex;
                nextIndex=prevIndex=0;
                String strValidState=null;
                if (bStart) {
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    bStart = false;
                } else {
                    sbWhereExp.append(SYMB_AND);
                    }

                nextIndex=strECValidStatespacked.indexOf(',',prevIndex);
                strValidState=strECValidStatespacked.substring(prevIndex,nextIndex);
                prevIndex=nextIndex+1;
                sbWhereExp.append(SYMB_OPEN_PARAN);
                sbWhereExp.append(SYMB_OPEN_PARAN);
                sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                sbWhereExp.append(SYMB_MATCH);
                //Added by Infosys for EC-search bug, 08 June 2005
                sbWhereExp.append(STR_CONST);
                sbWhereExp.append(SYMB_QUOTE);
                sbWhereExp.append(strValidState);
                sbWhereExp.append(SYMB_QUOTE);
                sbWhereExp.append(SYMB_CLOSE_PARAN);
                while(-1!=nextIndex)
                {
                    nextIndex=strECValidStatespacked.indexOf(',',prevIndex);
                    if(-1==nextIndex)
                    {
                        strValidState=strECValidStatespacked.substring(prevIndex);
                    }
                    else
                    {
                        strValidState=strECValidStatespacked.substring(prevIndex,nextIndex);
                        prevIndex=nextIndex+1;
                    }
                    sbWhereExp.append(SYMB_OR);
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_MATCH);
                    //Added by Infosys for EC-search bug, 08 June 2005
                    sbWhereExp.append(STR_CONST);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strValidState);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }
                sbWhereExp.append(SYMB_CLOSE_PARAN);
            }
            //End of Add by Infosys for Bug#304580

           String strAttribSeverity = DomainConstants.ATTRIBUTE_SEVERITY;

            if (strSeverity != null
                    && (!strSeverity.equals(SYMB_WILD))
                    && (!strSeverity.equals(""))
                    && !("null".equalsIgnoreCase(strSeverity))) {
                if (bStart) {
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    bStart = false;
                } else {
                    sbWhereExp.append(SYMB_AND);
                  }
                sbWhereExp.append(SYMB_OPEN_PARAN);
                sbWhereExp.append(SYMB_ATTRIBUTE);
                sbWhereExp.append(SYMB_OPEN_BRACKET);
                sbWhereExp.append(strAttribSeverity);
                sbWhereExp.append(SYMB_CLOSE_BRACKET);
                sbWhereExp.append(SYMB_MATCH);
                sbWhereExp.append(SYMB_QUOTE);
                sbWhereExp.append(strSeverity);
                sbWhereExp.append(SYMB_QUOTE);
                sbWhereExp.append(SYMB_CLOSE_PARAN);
            }

           String strAttribCategoryofChange = DomainConstants.ATTRIBUTE_CATEGORY_OF_CHANGE;

           if (strCategoryOfChange != null
                   && (!strCategoryOfChange.equals(SYMB_WILD))
                   && (!strCategoryOfChange.equals(""))
                   && !("null".equalsIgnoreCase(strCategoryOfChange))) {
               if (bStart) {
                   sbWhereExp.append(SYMB_OPEN_PARAN);
                   bStart = false;
               } else {
                   sbWhereExp.append(SYMB_AND);
                 }
               sbWhereExp.append(SYMB_OPEN_PARAN);
               sbWhereExp.append(SYMB_ATTRIBUTE);
               sbWhereExp.append(SYMB_OPEN_BRACKET);
               sbWhereExp.append(strAttribCategoryofChange);
               sbWhereExp.append(SYMB_CLOSE_BRACKET);
               sbWhereExp.append(SYMB_MATCH);
               sbWhereExp.append(SYMB_QUOTE);
               sbWhereExp.append(strCategoryOfChange);
               sbWhereExp.append(SYMB_QUOTE);
               sbWhereExp.append(SYMB_CLOSE_PARAN);
            }

           String strFilteredExpression = getFilteredExpression(context,programMap);

            if ((strFilteredExpression != null)
                    && !("null".equalsIgnoreCase(strFilteredExpression))
                    && !strFilteredExpression.equals("")) {
                if (bStart) {
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    bStart = false;
                } else {
                    sbWhereExp.append(SYMB_AND);
                  }
                sbWhereExp.append(strFilteredExpression);
            }
            if (!bStart) {
                sbWhereExp.append(SYMB_CLOSE_PARAN);
            }
           mapList = DomainObject.findObjects(context,
                                               strType,
                                               strName,
                                               strRevision,
                                               strOwner,
                                               strVault,
                                               sbWhereExp.toString(),
                                               "",
                                               true,
                                               (StringList) slSelect,
                                               sQueryLimit);

        } catch (Exception excp) {
                excp.printStackTrace(System.out);
                throw excp;
        }

        return mapList;
    }

    /**
     * The method is used to show / hide 'Create New'/'Add Existing'/'Remove'/'Delete' actions links in Engineering Change ImpactAnalysis
     * summary page. The links will be enabled in states upto 'Review' state
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */
    public Boolean hasImpactAnalysisLink(Context context, String[] args) throws Exception {
        return(showActionsLink(context,args,EC_STATE_REVIEW));
        }

    /**
     * To Get the list of all Persons that can be returned based on search criteria passed in as argument.
     *
     * @param         context the eMatrix <code>Context</code> object
     * @param         args holds the HashMap containing the following arguments
     *  queryLimit    limit for displaying search query results
     *  User Name     the user name or name pattern of the Person search for
     *  First Name    the first name or name pattern of the Person search for
     *  Last Name     the last name or name pattern of the Person search for
     *  Company       the company name or name pattern of the Person search for
     *  Vault Option  the vault search option
     *  Vault Display the vault display search option
     * @return        MapList , the Object ids matching the search criteria
     * @throws        Exception if the operation fails
     * @@since        Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getPersons(Context context, String[] args)
      throws Exception
      {

        Map programMap = (Map) JPO.unpackArgs(args);

		String strECAssign = (String)programMap.get("ECAssign");
		String strSplitDelegateAssignment = (String) programMap.get("splitDelegateAssignment");
		MapList mlPersons = null;
		if ((strECAssign != null && strECAssign.equalsIgnoreCase("Assign")) ||  (strSplitDelegateAssignment != null && strSplitDelegateAssignment.equalsIgnoreCase("splitDelegateAssignment")))
		{
			if (strSplitDelegateAssignment != null && strSplitDelegateAssignment.equalsIgnoreCase("splitDelegateAssignment"))
			{
				mlPersons = getContextPersons(context, args, false);
			} else if (strECAssign != null && strECAssign.equalsIgnoreCase("Assign")) {
				mlPersons = getContextPersons(context, args, true);
			}
			if (!(mlPersons.size() <= 0) && strECAssign != null && strECAssign.equalsIgnoreCase("Assign"))
			{
				return mlPersons;
			} else if (strSplitDelegateAssignment != null && strSplitDelegateAssignment.equalsIgnoreCase("splitDelegateAssignment") && !(mlPersons.size() <= 1))
			{
				return mlPersons;
			}
		}

        short sQueryLimit =
                    (short) (java
                            .lang
                            .Integer
                            .parseInt((String) programMap.get("queryLimit")));

        String strType = DomainConstants.TYPE_PERSON;

        String strName = (String)programMap.get("User Name");

        if ( strName==null || strName.equals("") ) {
            strName = SYMB_WILD;
        }

        String strFirstName = (String)programMap.get("First Name");

        String strLastName = (String)programMap.get("Last Name");

        String strCompany = (String)programMap.get("Company");

        String strVault = null;
        String strVaultOption = (String)programMap.get("vaultOption");

            if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)
                    || strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)
                    ||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)) {
                strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
            } else {
                strVault = (String)programMap.get("vaults");
              }

        StringList select = new StringList(1);
        select.addElement(DomainConstants.SELECT_ID);

        boolean start = true;
        StringBuffer sbWhereExp = new StringBuffer(100);

        if (strFirstName!=null && (!strFirstName.equals(SYMB_WILD)) && (!strFirstName.equals("")) ) {
            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
        }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_ATTRIBUTE);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(DomainConstants.ATTRIBUTE_FIRST_NAME);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_MATCH);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(strFirstName);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        if ( strLastName!=null && (!strLastName.equals(SYMB_WILD)) && (!strLastName.equals("")) ) {
            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
            } else {
                sbWhereExp.append(SYMB_AND);
        }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_ATTRIBUTE);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(DomainConstants.ATTRIBUTE_LAST_NAME);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_MATCH);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(strLastName);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        if (strCompany!=null && (!strCompany.equals(SYMB_WILD)) && (!strCompany.equals("")) ) {

			DomainObject companyDom = DomainObject.newInstance(context,strCompany);
			String sCompanyType = companyDom.getInfo(context,DomainConstants.SELECT_TYPE);
			String sRelToUse = DomainConstants.RELATIONSHIP_EMPLOYEE;
			if (sCompanyType!= null && sCompanyType.equals(DomainConstants.TYPE_BUSINESS_UNIT))
			{
				sRelToUse = DomainConstants.RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE;
			}

            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
            } else {
                sbWhereExp.append(SYMB_AND);
        }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_TO);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(sRelToUse);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_DOT);
            sbWhereExp.append(SYMB_FROM);
            sbWhereExp.append(SYMB_DOT);
            sbWhereExp.append(DomainConstants.SELECT_ID);
            sbWhereExp.append(SYMB_MATCH);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(strCompany);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }
		if (strSplitDelegateAssignment != null && !strSplitDelegateAssignment.equalsIgnoreCase("splitDelegateAssignment"))
		{
        String strFilteredExpression = getFilteredExpression(context,programMap);
        if (strFilteredExpression != null && strFilteredExpression.length() >0) {
            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
          } else {
              sbWhereExp.append(SYMB_AND);
          }
            sbWhereExp.append(strFilteredExpression);
        }
		}

        if (!start) {
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        MapList mapList = null;
        mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, SYMB_WILD, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
        return mapList;

    }

    /**
     * This method notifies owner and all the distribution list members connected
     * to EC by "EC Distribution List" relationship when the EC is promoted to
     * the Closed Or Rejected.
     *
     * @param context The ematrix context of the request.
     * @param strObjectId The string containing object id of Engineering Change Object:
     * @param strState The string containing promoted state of Engineering Change Object:
     * @throws Exception
     * @throws FrameworkException
     * @since AEF10.6
     */
    public void notifyEngineeringChangePromote(Context context, String strObjectId,String strState)
            throws Exception, FrameworkException {

        // Get the object id of the context Engineering Change object and form
        // the domain object
        DomainObject domEC = DomainObject.newInstance(
                context, strObjectId);

        // Get the distribution list connected to the context Engineering Change
        // object by EC Distribution List relationship.
        StringBuffer sbSelectMember = new StringBuffer();
        sbSelectMember.append(SYMB_FROM);
        sbSelectMember.append(SYMB_OPEN_BRACKET);
        sbSelectMember.append(DomainConstants.RELATIONSHIP_LIST_MEMBER);
        sbSelectMember.append(SYMB_CLOSE_BRACKET);
        sbSelectMember.append(SYMB_DOT);
        sbSelectMember.append(SYMB_TO);
        sbSelectMember.append(SYMB_DOT);
        sbSelectMember.append(DomainConstants.SELECT_NAME);

        List lstObjectSelects = new StringList();
        lstObjectSelects.add(sbSelectMember.toString());

        List lstMemberList = domEC.getRelatedObjects(
                context, DomainConstants.RELATIONSHIP_EC_DISTRIBUTION_LIST,
                DomainConstants.QUERY_WILDCARD, (StringList) lstObjectSelects,
                null, false, true, (short) 1, null, null);
        List lstListMembers = new StringList();

        //Cast the output to either String or List depending upon the type.
        if (lstMemberList != null && lstMemberList.size() > 0) {
            Object listMembers = (Object) ((Map) lstMemberList.get(0))
                    .get(sbSelectMember.toString());
            if (listMembers instanceof List) {
                lstListMembers = (StringList) listMembers;
            } else if (listMembers instanceof String) {
                lstListMembers.add((String) listMembers);
            }
        }

        lstListMembers.add((String)domEC.getInfo(context,DomainConstants.SELECT_OWNER));
        String strLanguage = context.getSession().getLanguage();
        String strSubjectKey = i18nNow
                .getI18nString(
                        "emxComponents.Message.Subject.EngineeringChange"+strState,
                        RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);
        String strMessageKey = i18nNow
                .getI18nString(
                        "emxComponents.Message.Description.EngineeringChange"+strState,
                        RESOURCE_BUNDLE_COMPONENTS_STR, strLanguage);

        strMessageKey = MessageUtil.substituteValues(
                context, strMessageKey, strObjectId, strLanguage);

        String[] subjectKeys = {};
        String[] subjectValues = {};
        String[] messageKeys = {};
        String[] messageValues = {};

        //Form the message attachment
        List lstAttachments = new StringList();
        lstAttachments.add(strObjectId);

        //Send the notification to the owner & memberList
        emxMailUtil_mxJPO.sendNotification( context,
        (StringList)lstListMembers, null, null, strSubjectKey,
        subjectKeys, subjectValues, strMessageKey, messageKeys,
        messageValues, (StringList)lstAttachments, null);
    }

   /**
    * Method shows higher revision Icon if a higher revision of the object exists
    * @param context the eMatrix <code>Context</code> object
    * @param args holds arguments
    * @return List - returns the program HTML output
    * @throws Exception if the operation fails
    * @since Common 10-6
    */
    public List getHigherRevisionIcon(Context context, String[] args) throws Exception{

        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);

        int iNumOfObjects = relBusObjPageList.size();
        // The List to be returned
        List lstHigherRevExists= new Vector(iNumOfObjects);
        String arrObjId[] = new String[iNumOfObjects];

        int iCount;
        //Getting the bus ids for objects in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            Object obj = relBusObjPageList.get(iCount);
            if (obj instanceof HashMap) {
                arrObjId[iCount] = (String)((HashMap)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
            }
            else if (obj instanceof Hashtable)
            {
                arrObjId[iCount] = (String)((Hashtable)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
            }
        }

        //Reading the tooltip from property file.
        String strTooltipHigherRevExists =
                                EnoviaResourceBundle.getProperty(context,
                                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                context.getLocale(),
                                ICON_TOOLTIP_HIGHER_REVISION_EXISTS);

        String strHigherRevisionIconTag= "";
        DomainObject domObj = new DomainObject();

        //Iterating through the list of objects to generate the program HTML output for each object in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
        domObj = newInstance(context,arrObjId[iCount]);
                if(!isLastRevision(context, domObj)){
                strHigherRevisionIconTag =
                        "<a HREF=\"#\" TITLE=\""
                                + " "
                                + strTooltipHigherRevExists
                                + "\">"
                                + HIGHER_REVISION_ICON
                                + "</a>";
                }else{
                strHigherRevisionIconTag = " ";
                }
            lstHigherRevExists.add(strHigherRevisionIconTag);
        }
        return lstHigherRevExists;
    }

    /**
     * Method call to get the Name in the Last Name, First Name format.
     *
     * @param context - the eMatrix <code>Context</code> object
     * @param args - Holds the parameters passed from the calling method
                objectList - Maplist containing Object id Relationship id Maps
     * @return Vector - Vector containing names in last name, first name format
     * @throws Exception if the operation fails
     * @since Common 10-6
     */
    public Vector getCompleteOwnerName(Context context, String[] args) throws Exception {
        //Unpacking the args
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        //Gets the objectList from args
        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
        Vector vctCompleteName = new Vector();
        //No of objects
        int iNoOfObjects = relBusObjPageList.size();
        String strFirstName = null;
        String strLastName = null;
        String arrObjId[] = new String[iNoOfObjects];
        //Getting the bus ids for objects in the table
        for (int i = 0; i < iNoOfObjects; i++) {
          Object obj = relBusObjPageList.get(i);
          if (obj instanceof HashMap) {
            arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
          }
          else if (obj instanceof Hashtable)
          {
            arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
          }
        }

        StringList listSelect = new StringList(1);
        listSelect.addElement(DomainConstants.SELECT_OWNER);

        MapList mapList = DomainObject.getInfo(context,arrObjId,listSelect);
        Iterator mListItr = mapList.iterator();
        Person person = null;
        String strOwner = "";
        String strOwnerDisplay = "";
        while (mListItr.hasNext()) {
            strOwner = (String)( ((Map)mListItr.next()).get(DomainConstants.SELECT_OWNER) );
            person = Person.getPerson(context,strOwner);
            strFirstName    = person.getInfo(context,Person.SELECT_FIRST_NAME);
            strLastName     = person.getInfo(context,Person.SELECT_LAST_NAME);
            strOwnerDisplay = strLastName + ", " + strFirstName;
            //Adding into the vector
            vctCompleteName.add(strOwnerDisplay);
        }

        return vctCompleteName;
    }

     /**
       * Program HTML Output for Owner field
       * @param context   the eMatrix <code>Context</code> object
       * @param args      holds a Map with the following input arguments :
       *    mode          the mode in which a field need to be displayed
       *    objectId      context Engineering Change objectId
       * @throws          Exception if the operations fails
       * @return          String which contains the HTML code for displaying field
       * @since           Common 10-6
       */
    public String buildOwnerField (Context context, String[] args) throws Exception {

        StringBuffer outPut = new StringBuffer();
        try {
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            //Getting mode parameter
            String mode        = (String)requestMap.get("mode");

            //Getting the EC Object id
            String strECObjectId = (String)paramMap.get("objectId");
            String strURL = "../components/emxCommonSearch.jsp?formName=editDataForm&frameName=formEditDisplay&searchmode=PersonChooser&suiteKey=Components&searchmenu=APPMemberSearchInMemberList&searchcommand=APPFindPeople&fieldNameDisplay=OwnerDisplay&fieldNameActual=Owner";

            //setting the context to the Engineering Change object id
            setId(strECObjectId);
            String ECOwner = getInfo(context,DomainConstants.SELECT_OWNER);
            Person person = Person.getPerson(context,ECOwner);
            String strFirstName     = person.getInfo(context,Person.SELECT_FIRST_NAME);
            String strLastName      = person.getInfo(context,Person.SELECT_LAST_NAME);
            String strOwnerDisplay  = strLastName + ", " + strFirstName;

            if( mode==null || mode.equalsIgnoreCase("view") ) {
                outPut.append(strOwnerDisplay);
            } else if( mode.equalsIgnoreCase("edit") ) {
                outPut.append("<input type=\"text\" name=\"OwnerDisplay");
                outPut.append("\"size=\"20\" value=\"");
                outPut.append(strOwnerDisplay);
                outPut.append("\" readonly=\"readonly\">&nbsp;");
                outPut.append("<input class=\"button\" type=\"button\"");
                outPut.append(" name=\"btnECOwnerChooser\" size=\"200\" ");
                outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
                outPut.append(strURL);
                outPut.append("', 700, 500)\">");
                outPut.append("<input type=\"hidden\" name=\"Owner\" value=\""+ECOwner+"\"></input>");
            }
        } catch(Exception ex) {
            throw  new FrameworkException((String)ex.getMessage());
        }
        return outPut.toString();
    }

    /**
      * Updates the Owner field in Engineering change WebForm.
      * @param context         the eMatrix <code>Context</code> object
      * @param args            holds a Map with the following input arguments:
      *    objectId            objectId of the context Engineering Change
      *    New Value           new owner id selected
      * @throws                Exception if the operations fails
      * @since                 Common 10-6
      */
    public void updateOwner (Context context, String[] args) throws Exception {
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap = (HashMap)programMap.get("paramMap");
            String strECObjectId = (String)paramMap.get("objectId");
            String strNewOwnerId = (String)paramMap.get("New Value");
            DomainObject contextECObj = DomainObject.newInstance(context,strECObjectId);
            contextECObj.setOwner(context,strNewOwnerId);
        } catch (Exception ex) {
            throw  new FrameworkException((String)ex.getMessage());
        }
    }

    /**
     * Method to get the Originator Name in the Last Name, First Name format.
     *
     * @param context - the eMatrix <code>Context</code> object
     * @param args - Holds the parameters passed from the calling method
                objectList - Maplist containing Object id Relationship id Maps
     * @return Vector - Vector containing names in last name, first name format
     * @throws Exception if the operation fails
     * @since Common 10-6
     */
    public Vector getCompleteOriginatorNameColumn(Context context, String[] args) throws Exception {
        //Unpacking the args
        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        //Gets the objectList from args
        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
        Vector vctCompleteName    = new Vector();
        //No of objects
        int iNoOfObjects          = relBusObjPageList.size();
        String strFirstName       = null;
        String strLastName        = null;
        String arrObjId[]         = new String[iNoOfObjects];
        //Getting the bus ids for objects in the table
        for (int i = 0; i < iNoOfObjects; i++) {
            Object obj = relBusObjPageList.get(i);
            if (obj instanceof HashMap) {
                arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
            }
            else if (obj instanceof Hashtable)
            {
                arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
            }
        }

        StringBuffer sbSelect = new StringBuffer();
        sbSelect.append("attribute[");
        sbSelect.append(DomainConstants.ATTRIBUTE_ORIGINATOR);
        sbSelect.append("]");
        StringList listSelect = new StringList(sbSelect.toString());

        MapList mapList             = DomainObject.getInfo(context,arrObjId,listSelect);
        Iterator mListItr           = mapList.iterator();
        Person person               = null;
        String strOriginator        = "";
        String strOriginatorDisplay = "";
        while (mListItr.hasNext()) {
            strOriginator           = (String)( ((Map)mListItr.next()).get(sbSelect.toString()) );
            person                  = Person.getPerson(context,strOriginator);
            strFirstName            = person.getInfo(context,Person.SELECT_FIRST_NAME);
            strLastName             = person.getInfo(context,Person.SELECT_LAST_NAME);
            strOriginatorDisplay    = strLastName + ", " + strFirstName;
            //Adding into the vector
            vctCompleteName.add(strOriginatorDisplay);
        }

        return vctCompleteName;
    }

    /**
     * Program HTML Output for Originator field
     * @param context   the eMatrix <code>Context</code> object
     * @param args      holds a Map with the following input arguments :
     *    objectId      context  object Id
     * @throws          Exception if the operations fails
     * @return          String which contains the full originator name
     * @since           Common 10-6
     */
    public String getCompleteOriginatorName (Context context, String[] args) throws Exception {

        String strOriginatorDisplay  = "";
        try {
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");

            //Getting the EC Object id
            String strContextObjectId = (String)paramMap.get("objectId");
            String strOriginatorSelect = "attribute["+DomainConstants.ATTRIBUTE_ORIGINATOR+"]";

            //setting the context to the Context object id
            setId(strContextObjectId);
            String strContextObjOriginator = getInfo(context,strOriginatorSelect);
            Person person = Person.getPerson(context,strContextObjOriginator);
            String strFirstName     = person.getInfo(context,Person.SELECT_FIRST_NAME);
            String strLastName      = person.getInfo(context,Person.SELECT_LAST_NAME);
            strOriginatorDisplay  = strLastName + ", " + strFirstName;
        } catch(Exception ex) {
            throw  new FrameworkException((String)ex.getMessage());
        }
        return strOriginatorDisplay;
    }

   /**
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */

    protected Boolean showActionsLink(Context context, String[] args, String restrictState) throws Exception {
            //unpacking the arguments from variable args
            HashMap programMap           = (HashMap)JPO.unpackArgs(args);

            //getting parent object Id from args
            String strObjectId           = (String)programMap.get("objectId");
            strObjectId = strObjectId.trim();
            this.setId(strObjectId);

            StringList objectSelects         = new StringList(3);
            objectSelects.add(DomainConstants.SELECT_CURRENT);
            objectSelects.add(DomainConstants.SELECT_POLICY);
            objectSelects.add(DomainConstants.SELECT_OWNER);

            //list that holds the assignees of the context EC
            List assigneesList = getAssignees(context,strObjectId);

            Map objMap = getInfo(context,objectSelects);

            String strCurrentState      = (String)objMap.get(DomainConstants.SELECT_CURRENT);
            String strObjPolicy         = (String)objMap.get(DomainConstants.SELECT_POLICY);
            String strOwner             = (String)objMap.get(DomainConstants.SELECT_OWNER);

            // flag decides whether to show links 'Create New'/'Add Existing'/'Remove'/'Delete'  depending on the restricted state
            Boolean showLink            = Boolean.valueOf(false);
            int currentStatePos = -1;
            int restrictStatePos = -1;

            // getting and representing the restricted state of policy EC
            String strRestrictState         = FrameworkUtil.lookupStateName(context,strObjPolicy,restrictState);

           // no state based restrictions for policies not having the specified state
            if((strRestrictState== null) || "".equals(strRestrictState)) {
                 showLink = Boolean.valueOf(true);
            // show links 'Create New'/'Add Existing'/'Remove'/'Delete' upto the restricted state
            } else {
                StateList stateList         = getStates(context);
                for (int i = 0; i < stateList.size(); i++) {
                    String strState = (((State)stateList.elementAt(i)).getName());
                    if(strState.equals(strCurrentState)) {
                        currentStatePos = i;
                    }
                    if(strState.equals(strRestrictState)) {
                        restrictStatePos = i;
                    }
                }
                if((currentStatePos <= restrictStatePos)&&(context.getUser().equals(strOwner) || assigneesList.contains(context.getUser()))) {
                    showLink = Boolean.valueOf(true);
                } else {
                    showLink = Boolean.valueOf(false);
                  }
             }
            return showLink;
    }

  /**
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */

    public Boolean hasReferenceDocumentsLink(Context context, String[] args) throws Exception {
            return(showActionsLink(context,args,EC_STATE_FORMAL_APPROVAL));
    }

  /**
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */

    public Boolean hasSatisfiedItemsLink(Context context, String[] args) throws Exception {
                    return(showActionsLink(context,args,EC_STATE_FORMAL_APPROVAL));
    }

    /**
     * The method is used to show / hide 'Add Existing'/'Remove' actions links in Engineering Change Assignees
     * summary page. The links will be enabled till all states prior to 'Formal Approval' state
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         Common 10-6
     **
     */

    public Boolean showAddRemoveAssigneesLink(Context context, String[] args)
        throws Exception {
        return(showActionsLink(context,args,EC_STATE_VALIDATE));
        }

    /**
      * Trigger Method to send notification after functionality Change Owner.
      *
      * @param context - the eMatrix <code>Context</code> object
      * @param args - Object Id of Issue
      * @return int - Returns int status code
                0 - in case the trigger is successfull
      * @throws Exception if the operation fails
      * @since Common 10-6
      */
    public int notifyOwner(Context context, String[] args) throws Exception {
        try {
            //Getting the object ID
            String strObjectId = args[0];

            DomainObject domEC = newInstance(context, strObjectId);
            //Getting the Changed Owner
            String strOwner = domEC.getInfo(context, DomainConstants.SELECT_OWNER);
            //Getting the Originator
            String strOriginator = domEC.getInfo(context, DomainConstants.SELECT_ORIGINATOR);
            String strLanguage = context.getSession().getLanguage();
            Locale strLocale = context.getLocale();

            StringList ccList = new StringList();
            StringList bccList = new StringList();
            StringList toList = new StringList();
            StringList lstAttachments = new StringList();

            String[] subjectKeys = {};
            String[] subjectValues = {};
            String[] messageKeys = {};
            String[] messageValues = {};

            toList.add(strOwner);
            lstAttachments.add(strObjectId);
            String strSubject = EnoviaResourceBundle.getProperty(context,
                    RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,
                    "emxComponents.Message.Subject.EngineeringChangeChangeOwner");
            String strMessage = EnoviaResourceBundle.getProperty(context,
                            RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,
                            "emxComponents.Message.Body.EngineeringChangeChangeOwner");

            //Check if Originator is himself a owner. If yes then the message should be blank.
            if (strOwner.equals(strOriginator)) {
                    strMessage = "";
            }else {
                    //Expanding the macros used in the message body
                    strMessage = MessageUtil.substituteValues(context, strMessage,
                                                                strObjectId, strLanguage);
            }

            //Sending mail to the owner
            emxMailUtil_mxJPO.sendNotification( context,toList, null, null, strSubject,
                                subjectKeys, subjectValues, strMessage, messageKeys,
                                messageValues, lstAttachments, null);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            throw new FrameworkException((String) ex.getMessage());
        }
        return 0;
    }


   /**
    * Method shows Active EC Icon if an object has an EC associated with it.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return List - returns the program HTML output
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    public List getActiveECIcon(Context context, String[] args) throws Exception{

    Map programMap = (HashMap) JPO.unpackArgs(args);
    MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
    //IR-109331V6R2012x
    Map paramList = (HashMap)programMap.get("paramList");
    String reportFormat = (String)paramList.get("reportFormat");

    int iNumOfObjects = relBusObjPageList.size();
    // The List to be returned
    List lstActiveECIcon= new Vector(iNumOfObjects);
    String strActiveECIconTag = "";
    String strIcon = EnoviaResourceBundle.getProperty(context,
                        "emxComponents.ActiveECImage");
    String arrObjId[] = new String[iNumOfObjects];
    int iCount;
    //Getting the bus ids for objects in the table
    for (iCount = 0; iCount < iNumOfObjects; iCount++) {
        Object obj = relBusObjPageList.get(iCount);
        if (obj instanceof HashMap) {
            arrObjId[iCount] = (String)((HashMap)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
        }
        else if (obj instanceof Hashtable)
        {
            arrObjId[iCount] = (String)((Hashtable)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
        }
    }
    // Retrieving  relationship,Policy,Policy states using symbolic names
    String strClose = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_CLOSE);
    String strReject = FrameworkUtil.lookupStateName(context,EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_REJECT);
    String strComplete = FrameworkUtil.lookupStateName(context,EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_COMPLETE);
    //List of selectables
    StringList lstSelect = new StringList();
    String strStateSelect ="to["+ DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM +"].from."+ DomainConstants.SELECT_CURRENT;
    DomainObject.MULTI_VALUE_LIST.add(strStateSelect);
    lstSelect.addElement(strStateSelect);
    //Reading the tooltip from property file.
    String strTooltipActiveECIcon = EnoviaResourceBundle.getProperty(context,
                                            RESOURCE_BUNDLE_COMPONENTS_STR,
                    context.getLocale(),ICON_TOOLTIP_ACTIVE_EC_EXISTS);

    //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  Relationships of the objectids
    BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, lstSelect);

    //Iterating through the list of objects and comparing their statesto generate the program HTML output for each object in the table
    for (iCount = 0; iCount < iNumOfObjects; iCount++) {
        StringList strStateList = attributeList.getElement(iCount).getSelectDataList(strStateSelect);
        boolean activeEC = false;
        if(strStateList != null && strStateList.size()>0) {
            Iterator stListItr = strStateList.iterator();
            while(stListItr.hasNext()) {
                String strTmpState = (String)stListItr.next();
                if (strTmpState == null||strTmpState.equals("")||"null".equals(strTmpState) || "#DENIED!".equals(strTmpState)
                    ||strTmpState.equals(strClose)||strTmpState.equals(strReject)||strTmpState.equals(strComplete)){
                    activeEC = false;
                } else {
                    activeEC = true;
                    break;
                }
            }
        }
        if(activeEC) {
        	// added if statement for IR-109331V6R2012x
        	if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
        		lstActiveECIcon.add(strTooltipActiveECIcon);
            }else{
            	strActiveECIconTag =
                        "<img src=\"../common/images/"
                            + strIcon
                            + "\" border=\"0\"  align=\"middle\" "
                            + "TITLE=\""
                            + " "
                            + strTooltipActiveECIcon
                            + "\""
                            + "/>";
            }
        } else {
            strActiveECIconTag = " ";
        }
        lstActiveECIcon.add(strActiveECIconTag);
    }
 return lstActiveECIcon;
}

  /**
    * Method shows Active EC Icon in the object property page if the context object has an EC associated with it
    * @param context the eMatrix <code>Context</code> object
    * @return String - returns the program HTML output
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    public String getActiveECIconProperty(Context context, String[] args) throws Exception{

    Map programMap = (HashMap) JPO.unpackArgs(args);
    Map relBusObjPageList = (HashMap) programMap.get("paramMap");
    String strObjectId = (String)relBusObjPageList.get("objectId");
    // Added for fixing IR-151386V6R2013x
    HashMap requestMap = (HashMap) programMap.get("requestMap");
    boolean isCSVExport = requestMap.get("reportFormat") != null && "CSV".equalsIgnoreCase((String)requestMap.get("reportFormat") );
    //String Buffer to display the Higher revision field in Req property page.
    StringBuffer sbActiveECIcon = new StringBuffer(100);
    String strActiveECIcon = "";
    String strStateSelect = "";

    // Retrieving  relationship,Policy,Policy states using symbolic names
    String strClose = FrameworkUtil.lookupStateName(context,EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_CLOSE);
    String strReject = FrameworkUtil.lookupStateName(context,EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_REJECT);
    String strComplete = FrameworkUtil.lookupStateName(context,EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_COMPLETE);
    //START:R210:IR-060285V6R2011x
    String strIcon = EnoviaResourceBundle.getProperty(context,"emxComponents.ActiveECImage");
    //END:R210:IR-060285V6R2011x

    StringList relSelects = new StringList();
    StringList busSelects = new StringList();
    busSelects.add(DomainConstants.SELECT_CURRENT);
    relSelects.add(DomainConstants.SELECT_TO_ID);

    StringBuffer objWhere = new StringBuffer();
    objWhere.append("((");
    objWhere.append(DomainConstants.SELECT_CURRENT);
    objWhere.append(" != \"");
    objWhere.append(strClose);
    objWhere.append("\")&&(");
    objWhere.append(DomainConstants.SELECT_CURRENT);
    objWhere.append(" != \"");
    objWhere.append(strReject);
    objWhere.append("\")&&(");
    objWhere.append(DomainConstants.SELECT_CURRENT);
    objWhere.append(" != \"");
    objWhere.append(strComplete);
    objWhere.append("\"))");

    DomainObject domObj = DomainObject.newInstance(context, strObjectId);
    MapList relIdList = domObj.getRelatedObjects(context,
                                                 DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM,
                                                 DomainConstants.TYPE_ENGINEERING_CHANGE,
                                                 busSelects,
                                                 relSelects,
                                                 true,
                                                 false,
                                                 (short)1,
                                                 objWhere.toString(),
                                                 DomainConstants.EMPTY_STRING);

    //Reading the tooltip from property file.
    String strTooltipActiveECIcon = EnoviaResourceBundle.getProperty(context,
                                            RESOURCE_BUNDLE_COMPONENTS_STR,
                                            context.getLocale(),
                                            ICON_TOOLTIP_ACTIVE_EC_EXISTS);
    String strNo                  = EnoviaResourceBundle.getProperty(context,
                                            RESOURCE_BUNDLE_COMPONENTS_STR,
                                            context.getLocale(),
                                            "emxComponents.Common.No");
    String strYes                 = EnoviaResourceBundle.getProperty(context,
                                            RESOURCE_BUNDLE_COMPONENTS_STR,
                                            context.getLocale(),
                                            "emxComponents.Common.Yes");

    String strActiveECIconTag= "";

    //To generate the program HTML output for the context object

    if(relIdList.size() > 0) {

    	//R210:START:IR-060285V6R2011x
    	strActiveECIconTag =
           "<img src=\"../common/images/"
            + strIcon
            + "\" border=\"0\"  align=\"middle\" "
            + "TITLE=\""
            + " "
            + strTooltipActiveECIcon
            + "\""
            + "/>";
        //R210:END:IR-060285V6R2011x

    	if (!isCSVExport){
        sbActiveECIcon.append(strActiveECIconTag);
    	}
        sbActiveECIcon.append(strYes);
        strActiveECIcon = sbActiveECIcon.toString();

    } else {
    	strActiveECIconTag =  "&nbsp;";
        sbActiveECIcon.append(strNo);
    	if (!isCSVExport){
        sbActiveECIcon.append(strActiveECIconTag);
    	}
        strActiveECIcon = sbActiveECIcon.toString();
    }
     return strActiveECIcon;
    }

    //Begin of Add by Infosys on 21 Mar 05 for Bug# 300086

    /**
    * This method is used to check whether the context user is owner or
    * Assignee of some Engineering Change object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args[] - String Array.
    * @return String - 0 - If the context user is owner or assignee of
    *                              some EC
    *                         1- Otherwise.
    * @throws Exception if the operation fails
    * @since AEF 10.6
    */
        public String hasAccess(Context context,String args[]) throws Exception{
  			//Performace Issue ---14x -----
              String strReturn="1";
  			//get the context user
  			String strContextUser = context.getUser();
  			//Get related EC that is assigned to the user with limit as 1
  			String userId = PersonUtil.getPersonObjectID(context);
  			String strRelECAffectedItem  = DomainConstants.RELATIONSHIP_ASSIGNED_EC;
  			String strTypeEC  =DomainConstants.TYPE_ENGINEERING_CHANGE;

  			//Relationships are selected by its Ids
  			StringList relSelects        = new StringList(DomainRelationship.SELECT_ID);
  			StringList objectSelects        = new StringList(DomainObject.SELECT_ID);
  			//the number of levels to expand, 1 equals expand one level
  			short recurseToLevel     = 1;
  			//retrieving Engineering Change List from Person
  			//setting Person ID for the Business Object
  			this.setId(userId);
  			MapList relBusObjPageList = getRelatedObjects(
  					context,
  					strRelECAffectedItem,
  					strTypeEC,
  					objectSelects, relSelects, false, true, recurseToLevel,
  					getActiveECWhereExpression(context),
  					DomainConstants.EMPTY_STRING, 1);
  			if(relBusObjPageList.size()==0){
  				//Get owned EC that is in Active condition with limit as 1
  		        //Check whether context user is owner of any EC
                  StringBuffer cmd = new StringBuffer(150);
                  // forming the where clause-----
                  String strWhereExpression    = "";
                  StringBuffer sbWhere         = new StringBuffer(150);
                  sbWhere.append(SYMB_OPEN_PARAN);
                  sbWhere.append(SYMB_OPEN_PARAN);
                  sbWhere.append(DomainConstants.SELECT_OWNER);
                  sbWhere.append(SYMB_EQUAL);
                  sbWhere.append(SYMB_QUOTE);
                  sbWhere.append(strContextUser);
                  sbWhere.append(SYMB_QUOTE);
                  sbWhere.append(SYMB_CLOSE_PARAN);
                  sbWhere.append(SYMB_AND);
                  // add condition that EC object is Active - i.e. not Close, Complete or Reject
                  sbWhere.append(getActiveECWhereExpression(context));
                  sbWhere.append(SYMB_CLOSE_PARAN);

                  strWhereExpression = sbWhere.toString();
                  cmd.append("temp query bus $1 $2 $3 where $4 limit $5 select $6 dump $7");
  		        //If the number of owned EC by this user is greater than 0 then return 0
                  String output = MqlUtil.mqlCommand(context, cmd.toString(), DomainConstants.TYPE_ENGINEERING_CHANGE,
                  		DomainConstants.QUERY_WILDCARD, DomainConstants.QUERY_WILDCARD, strWhereExpression, "1", "id", "|");
                  StringList list = FrameworkUtil.split(output, "|");
  		        if(list.size()>0){
  		        	strReturn= "0";
  		        }else{
  		        	strReturn= "1";
  		        }

  			}else{
  				strReturn= "0";
  			}
          return strReturn;
          //Performace Issue ---14x -----
    }

/**
    * This method is used to modify the History of the object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param strObjectId - The object id.
    * @return strAction - The action to be added in the history.
    * @return strComment - The comment to be added in the history
    * @throws Exception if the operation fails
    * @since AEF 10.6
    */
    protected void modifyHistory(Context context, String strObjectId, String strAction, String strComment) throws Exception {
        MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3",true, strObjectId, strAction, strComment);
    }
//End of Add by Infosys on 21 Mar 05 for Bug# 300086

// Begin of Add by Infosys for Bug# 300086 Date 04/26/2005

         /**To Obtain All the company vaults for the wild card search.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @return String containing all the vaults of the company
         * @throws Exception if the operation fails
         * @since AEF 10.6
         */

         protected static String getAllCompanyVaults(Context context) throws Exception {
           Person person = Person.getPerson(context, context.getUser());
           return person.getCompany(context).getAllVaults(context,false);
         }

         /**
         * Method to return the Vaults.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @return - String containg the vaults
         * @throws Exception if the operation fails
         * @since AEF 10.6
         */
         protected static String getAllVaults(Context context) throws Exception {
           return getAllCompanyVaults(context);
         }

         /**
         * The method to get default vault.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds no arguments
         * @return String containing Default vault.
         * @throws Exception if the operation fails
         * @since AEF 10.6
         */
         public static String getDefaultVaults(Context context, String args[])
                throws Exception {

                StringBuffer radioOption = null;
                try {
                        String strVaults = getAllVaults(context);

                        radioOption = new StringBuffer(150);

                        String strLocale = context.getSession().getLanguage();
                        i18nNow i18nNowInstance = new i18nNow();

                                                String vaultDefaultSelection = PersonUtil.getSearchDefaultSelection(context);

                                                String strAll = i18nNowInstance.GetString("emxFrameworkStringResource",strLocale,"emxFramework.Preferences.AllVaults");
                                                String strDefault = i18nNowInstance.GetString("emxFrameworkStringResource",strLocale,"emxFramework.Preferences.UserDefaultVault");
                                                String strSelected = i18nNowInstance.GetString("emxFrameworkStringResource",strLocale,"emxFramework.Preferences.SelectedVaults");
                                                String strLocal = i18nNowInstance.GetString("emxFrameworkStringResource",strLocale,"emxFramework.Preferences.LocalVaults");
                                                String checked = "";
                                                if (  PersonUtil.SEARCH_DEFAULT_VAULT.equals(vaultDefaultSelection) )
                                                {
                                                                          checked = "checked";
                                                }

                                          radioOption.append("&nbsp;<input type=\"radio\" value=\"");
                                          radioOption.append(PersonUtil.SEARCH_DEFAULT_VAULT);
                                          radioOption.append("\" name=\"vaultOption\" ");
                                          radioOption.append(checked);
                                          radioOption.append(">");
                                          radioOption.append(XSSUtil.encodeForHTML(context,strDefault));
                                          radioOption.append("<br>");

                                          checked = "";
                                          if (  PersonUtil.SEARCH_LOCAL_VAULTS.equals(vaultDefaultSelection) )
                                          {
                                                                  checked = "checked";
                                          }
                                          radioOption.append("&nbsp;<input type=\"radio\" value=\"");
                                          radioOption.append(PersonUtil.SEARCH_LOCAL_VAULTS);
                                          radioOption.append("\" name=\"vaultOption\" ");
                                          radioOption.append(checked);
                                          radioOption.append(">");
                                          radioOption.append(XSSUtil.encodeForHTML(context,strLocal));
                                          radioOption.append("<br>");

                                                checked = "";
                                                 String vaults = "";
                                                 String selVault = "";
                                                 String selDisplayVault = "";
                                                 if (!PersonUtil.SEARCH_DEFAULT_VAULT.equals(vaultDefaultSelection) &&
                                                                   !PersonUtil.SEARCH_LOCAL_VAULTS.equals(vaultDefaultSelection) &&
                                                                   !PersonUtil.SEARCH_ALL_VAULTS.equals(vaultDefaultSelection) )
                                                 {
                                                                  checked = "checked";
                                                                  selVault = vaultDefaultSelection;
                                                                  selDisplayVault = i18nNow.getI18NVaultNames(context, vaultDefaultSelection, strLocale);
                                           }
                                          radioOption.append("&nbsp;<input type=\"radio\" value=\"");
                                          radioOption.append(XSSUtil.encodeForHTMLAttribute(context,selVault));
                                          radioOption.append("\" name=\"vaultOption\" ");
                                          radioOption.append(checked);
                                          radioOption.append(">");
                                          radioOption.append(XSSUtil.encodeForHTML(context,strSelected));
                                          radioOption.append("&nbsp;&nbsp;<input type=\"text\" READONLY name=\"vaultsDisplay\" value =\""+selDisplayVault+"\" id=\"\" size=\"20\" onFocus=\"this.blur();\">");
                                          radioOption.append("<input type=\"button\" name=\"VaultChooseButton\" value=\"...\" onclick=\"document.forms[0].vaultOption[2].checked=true;javascript:getTopWindow().showChooser('../common/emxVaultChooser.jsp?fieldNameActual=vaults&fieldNameDisplay=vaultsDisplay&multiSelect=true&isFromSearchForm=true')\">");
                                          radioOption.append("<input type=\"hidden\" name=\"vaults\" value=\"");
                                          radioOption.append(selVault);
                                          radioOption.append("\" size=15>");
                                          radioOption.append("<br>");
                                          checked = "";
                                           if (  PersonUtil.SEARCH_ALL_VAULTS.equals(vaultDefaultSelection) )
                                           {
                                                                  checked = "checked";
                                           }
                                          radioOption.append("&nbsp;<input type=\"radio\" value=\"");
                                          radioOption.append(PersonUtil.SEARCH_ALL_VAULTS);
                                          radioOption.append("\" name=\"vaultOption\" ");
                                          radioOption.append(checked);
                                          radioOption.append(">");
                                          radioOption.append(strAll);

                } catch (Throwable excp) {

                        excp.printStackTrace(System.out);
                }

                return radioOption.toString();
         }

         /**
         * Method to get the Company Name through chooser.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds no arguments
         * @return - string containing Organization name
         * @throws Exception if the operation fails
         * @since AEF 10.6
         */
         public String getCompanyOrganizationChooserHTML(
                Context context,
                String[] args)
                throws Exception {
                String strLocale = context.getSession().getLanguage();

                String strOrganization =
                        i18nNow.getTypeI18NString(
                                DomainConstants.TYPE_ORGANIZATION,
                                strLocale);
                String strCompany =
                        i18nNow.getTypeI18NString(DomainConstants.TYPE_COMPANY, strLocale);
                String strOrganizationHidden = DomainConstants.TYPE_ORGANIZATION;
                String strOrganizationHiddenField = strOrganizationHidden.trim();
                String strCompanyHidden = DomainConstants.TYPE_COMPANY;
                String strCompanyHiddenField = strCompanyHidden.trim();
                try {
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);

                        StringBuffer strTypeChooser = new StringBuffer(150);

                        String strTypes = "type_Company";
                        strTypeChooser.append(
                                "<img src=\"images/iconSmallCompany.gif\" border=\"0\" alt=\"Company\"></img><b>"
                                        + XSSUtil.encodeForHTML(context,strCompany));
                        strTypeChooser.append(
                                "<input type=\"hidden\" name=\"Type\" value=\""
                                        + XSSUtil.encodeForHTMLAttribute(context,strCompanyHiddenField)
                                        + "\">");

                        return strTypeChooser.toString();
                } catch (Exception ex) {
                        ex.printStackTrace(System.out);
                        throw ex;
                }
         }

         /**
         * To obtain the Companies.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - Holds the Hashmap containing following arguments
                              queryLimit
                              Type
                              Name
         * @return MapList containing the Object ids matching the search criteria
         * @throws Exception if the operation fails
         * @since AEF 10.6
         */
         @com.matrixone.apps.framework.ui.ProgramCallable
         public static MapList getCompanies(Context context, String[] args)
                throws Exception {
                MapList mapList = null;
                try {

                        Map programMap = (Map) JPO.unpackArgs(args);
                        short sQueryLimit =
                                (short) (java
                                        .lang
                                        .Integer
                                        .parseInt((String) programMap.get("queryLimit")));

                        String strType = (String) programMap.get("Type");
                        if (strType == null
                                || strType.equals("")
                                || "null".equalsIgnoreCase(strType)) {
                                strType = SYMB_WILD;
                        }

                        String strName = (String) programMap.get("Name");

                        String strVault = "";
                        String strVaultOption = (String) programMap.get(VAULT_OPTION);

                          if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                                                                        strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
                                                 else
                                                        strVault = (String)programMap.get("vaults");

                        StringList slSelect = new StringList(1);
                        slSelect.addElement(DomainConstants.SELECT_ID);

                        boolean bStart = true;
                        StringBuffer sbWhereExp = new StringBuffer(100);

                        String strAttribOrgName =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_OrganizationName);

                        if (strName != null
                                && (!strName.equals(SYMB_WILD))
                                && (!strName.equals(""))
                                && !("null".equalsIgnoreCase(strName))) {
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                sbWhereExp.append(SYMB_ATTRIBUTE);
                                sbWhereExp.append(SYMB_OPEN_BRACKET);
                                sbWhereExp.append(strAttribOrgName);
                                sbWhereExp.append(SYMB_CLOSE_BRACKET);
                                sbWhereExp.append(SYMB_MATCH);
                                sbWhereExp.append(SYMB_QUOTE);
                                sbWhereExp.append(strName);
                                sbWhereExp.append(SYMB_QUOTE);
                                sbWhereExp.append(SYMB_CLOSE_PARAN);
                        }

                        String strFilteredExpression = getFilteredExpression(context,programMap);
                        if ((strFilteredExpression != null)
                                && !("null".equalsIgnoreCase(strFilteredExpression))
                                && !strFilteredExpression.equals("")) {
                                if (bStart) {
                                        sbWhereExp.append(SYMB_OPEN_PARAN);
                                        bStart = false;
                                } else {
                                        sbWhereExp.append(SYMB_AND);
                                }
                                sbWhereExp.append(strFilteredExpression);
                        }
                        if (!bStart) {
                                sbWhereExp.append(SYMB_CLOSE_PARAN);
                        }

                        mapList =DomainObject.findObjects(
                                        context,
                                        strType,
                                        strName,
                                        SYMB_WILD,
                                        SYMB_WILD,
                                        strVault,
                                        sbWhereExp.toString(),
                                        "",
                                        true,
                                        slSelect,
                                        sQueryLimit);
                } catch (Exception excp) {
                        excp.printStackTrace(System.out);
                        throw excp;
                }
                return mapList;
         }

// End of Add by Infosys for Bug# 300086 Date 04/26/2005

    public static MapList getContextPersons(Context context, String[] args, boolean bECAssign)
      throws Exception
      {
        Map programMap = (Map) JPO.unpackArgs(args);
        short sQueryLimit =
                    (short) (java
                            .lang
                            .Integer
                            .parseInt((String) programMap.get("queryLimit")));

        String strType = DomainConstants.TYPE_PERSON;

        String strName = (String)programMap.get("User Name");

        if ( strName==null || strName.equals("") ) {
            strName = SYMB_WILD;
        }

        String strFirstName = (String)programMap.get("First Name");

        String strLastName = (String)programMap.get("Last Name");

		String selectedObjIds = (String) programMap.get("arrObjIds1");
		String selectedRelIds = (String) programMap.get("arrRelIds1");
		selectedObjIds = selectedObjIds.replace('[',' ');
		selectedObjIds = selectedObjIds.replace(']',' ');
		selectedRelIds = selectedRelIds.replace('[',' ');
		selectedRelIds = selectedRelIds.replace(']',' ');
		StringTokenizer objToken = new StringTokenizer(selectedObjIds,",");
		StringTokenizer relToken = new StringTokenizer(selectedRelIds,",");
		int objTokenCount = objToken.countTokens();
		int relTokenCount = relToken.countTokens();
		String [] selectedObjIdsArray = new String [objTokenCount];


        String strVault = null;
        String strVaultOption = (String)programMap.get("vaultOption");

            if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)
                    || strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)
                    ||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)) {
                strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
            } else {
                strVault = (String)programMap.get("vaults");
              }

        StringList select = new StringList(1);
        select.addElement(DomainConstants.SELECT_ID);

        boolean start = true;
        StringBuffer sbWhereExp = new StringBuffer(100);

        if (strFirstName!=null && (!strFirstName.equals(SYMB_WILD)) && (!strFirstName.equals("")) ) {
            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
        }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_ATTRIBUTE);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(DomainConstants.ATTRIBUTE_FIRST_NAME);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_MATCH);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(strFirstName);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        if ( strLastName!=null && (!strLastName.equals(SYMB_WILD)) && (!strLastName.equals("")) ) {
            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
            } else {
                sbWhereExp.append(SYMB_AND);
        }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_ATTRIBUTE);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(DomainConstants.ATTRIBUTE_LAST_NAME);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_MATCH);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(strLastName);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        String sRelId = null;
        String sToObjectId = null;
		MapList objectMap = null;
        if (selectedRelIds != null)
        {
            for (int i=0;i< relTokenCount ;i++ )
            {
                if (i==1)
                {
                    break;
                }
                sRelId = (relToken.nextToken()).trim();
            }
        }

            for (int i=0;i< objTokenCount ;i++ )
            {
                selectedObjIdsArray[i] = (objToken.nextToken()).trim();
            }
        if (sRelId != null && !sRelId.equals("") && !sRelId.equals(" ") && !sRelId.equals("null"))
        {
				StringList selects = new StringList(1);
				if (bECAssign)
				{
					selects.addElement(DomainConstants.SELECT_TO_ID);
				}else {
					selects.addElement(DomainConstants.SELECT_FROM_ID);

				}

                objectMap = DomainRelationship.getInfo(context,new String [] {sRelId},selects);
				Iterator objectMapIterator = objectMap.iterator();
				while (objectMapIterator.hasNext()) {
                    Map iteratedObjectMap = (Map) objectMapIterator.next();
					if (bECAssign)
					{
                        sToObjectId = (String)iteratedObjectMap.get(DomainConstants.SELECT_TO_ID);
					}else {
                        sToObjectId = (String)iteratedObjectMap.get(DomainConstants.SELECT_FROM_ID);

					}
				}

        }

        if (sToObjectId!=null && (!sToObjectId.equals("")) ) {
            if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
            } else {
                sbWhereExp.append(SYMB_AND);
        }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_FROM);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(strRelationAssignedEC);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_DOT);
            sbWhereExp.append(SYMB_TO);
            sbWhereExp.append(SYMB_DOT);
            sbWhereExp.append(DomainConstants.SELECT_ID);
            sbWhereExp.append(SYMB_EQUAL);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(sToObjectId);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        if (!start) {
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }

        MapList mapList = null;
        mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, SYMB_WILD, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);

		if (!bECAssign && mapList.size() > 1)
		{
			return mapList;
		}

        MapList returnMapList = new MapList();
                Iterator mapListIterator = mapList.iterator();
                Map iteratedMap          = null;
                String sPersonObjectId   = null;

                while (mapListIterator.hasNext()) {
                    iteratedMap = (Map) mapListIterator.next();
                    sPersonObjectId = (String)iteratedMap.get(DomainConstants.SELECT_ID);
                    boolean addToReturnList = true;
                    for (int i=0;i< selectedObjIdsArray.length ;i++ )
                    {
                        if (sPersonObjectId != null && selectedObjIdsArray[i] != null && selectedObjIdsArray[i].equals(sPersonObjectId))
                        {
                            addToReturnList = false;
                            break;
                        }
                    }
                    if (addToReturnList)
                    {
                        returnMapList.add(iteratedMap);
                    }
        }
        return returnMapList;

    }
/**
     * Get the list of all Objects that can be returned based on search criteria passed in as argument
     *
     * @param context     the eMatrix <code>Context</code> object
     * @param args        contains a Map with the following input arguments or entries:
     *    queryLimit      limit for displaying search query results
     *    hdnType         the admin 'type'of the object to search for
     *    txtName         the 'name' or 'name pattern' to search for
     *    txtRevision     the 'revision' pattern to search for
     *    txtDescription  the 'description' pattern to search for
     *    txtOwner        the 'owner' pattern to search for
     *    txtState.       the object 'state' pattern to search for
     *    vaultOption     the 'vault' search option
     *    srcDestRelName  the name of the destination relationship to expand for
     * @return            a <code>MapList</code> object having the list of objects satisfying the search criteria
     * @throws            Exception if the operation fails
     * @since             Common 10-6
     **
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getAffectedItemsGeneralSearchResults(Context context, String[] args)
        throws Exception {

        MapList mapList = null;
		MapList finalmap = null;

        try {
            Map programMap    = (Map) JPO.unpackArgs(args);
            short sQueryLimit =
                    (short) (java
                            .lang
                            .Integer
                            .parseInt((String) programMap.get("queryLimit")));

            String strType = (String) programMap.get("hdnType");
            String strObjectId = (String) programMap.get("objectId");

            if (strType == null
                    || strType.equals("")
                    || "null".equalsIgnoreCase(strType)) {
                    strType = DomainConstants.QUERY_WILDCARD;
            }

            String strName = (String) programMap.get("txtName");

            if (strName == null
                    || strName.equals("")
                    || "null".equalsIgnoreCase(strName)) {
                    strName = DomainConstants.QUERY_WILDCARD;
            }

            String strRevision = (String) programMap.get("txtRevision");

            if (strRevision == null
                    || strRevision.equals("")
                    || "null".equalsIgnoreCase(strRevision)) {
                    strRevision = DomainConstants.QUERY_WILDCARD;
            } else {
                    strRevision = strRevision.trim();
            }

            String strDesc = (String) programMap.get("txtDescription");

            String strOwner = (String) programMap.get("txtOwner");

            if (strOwner == null
                    || strOwner.equals("")
                    || "null".equalsIgnoreCase(strOwner)) {
                    strOwner = DomainConstants.QUERY_WILDCARD;
            } else {
                    strOwner = strOwner.trim();
            }

            String strState = (String) programMap.get("txtState");

            if (strState == null
                    || strState.equals("")
                    || "null".equalsIgnoreCase(strState)) {
                    strState = DomainConstants.QUERY_WILDCARD;
            } else {
                    strState = strState.trim();
            }

            String strVault = "";
            String strVaultOption = (String) programMap.get(VAULT_OPTION);

            if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)) {
                strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
            } else {
                strVault = (String)programMap.get("vaults");
            }

            StringList slSelect = new StringList(1);
            slSelect.addElement(DomainConstants.SELECT_ID);

            //boolean variable which specifies not to include EC 'Close','Complete' and 'Reject' state EC objects and 'Closed' state Issue objects in Search Results
            boolean bInactiveECAndIssue = false;
            boolean bStateSearch        = false;
            boolean bSatisfiedItemSearch= false;
            boolean bStart              = true;
            StringBuffer sbWhereExp = new StringBuffer(150);

            if (strDesc != null
                    && (!strDesc.equals(DomainConstants.QUERY_WILDCARD))
                    && (!strDesc.equals(""))
                    && !("null".equalsIgnoreCase(strDesc))) {
                    if (bStart) {
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        bStart = false;
                    }
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
                    sbWhereExp.append(SYMB_MATCH);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strDesc);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
            }

            String strRelName = (String) programMap.get("srcDestRelName");
            // get Actual relationship name
            strRelName        = (String) PropertyUtil.getSchemaProperty(context,strRelName);

            bSatisfiedItemSearch = (strRelName != null && strRelName.equals(DomainConstants.RELATIONSHIP_RESOLVED_TO));

            // Getting and representing the state 'Close' of policy EC
            String strECStateClose    = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_CLOSE);

            // Getting and representing the state 'Complete' of policy EC
            String strECStateComplete = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_COMPLETE);

            // Getting and representing the state 'Reject' of policy EC
            String strECStateReject   = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD,EngineeringChange.EC_STATE_REJECT);

            //To get Issue policy Name
            String strIssuePolicy =PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_policy_Issue);

            // Getting and representing the state 'Closed' of policy Issue
            String strIssueStateClosed   = FrameworkUtil.lookupStateName(context,strIssuePolicy,ISSUE_STATE_CLOSED);

               if(strState != null && (strState.equals(strECStateClose)
                                   || strState.equals(strECStateReject)
                                   || strState.equals(strECStateComplete)
                                   || strState.equals(strIssueStateClosed))) {
                   bInactiveECAndIssue = true;
               }

            // set this flag true when state based, non-wildcard search is done for non-satisfied items search
            if(!bSatisfiedItemSearch && (strState != null)
                  && (!strState.equals(DomainConstants.QUERY_WILDCARD))
                  && (!"".equals(strState))
                  && (!"null".equalsIgnoreCase(strState))) {
                    bStateSearch = true;
             } else if(bSatisfiedItemSearch && !bInactiveECAndIssue){
                 // for Satisfied Items wildcard search, make conditional state search for cases of Active EC / Issue search
                 // this exceptional search is done to prevent Closed / Rejected EC or Issue objects in search results
                 bStateSearch = true;
             }

            if (bStateSearch) {
                if (bStart) {
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    bStart = false;
                } else {
                    sbWhereExp.append(SYMB_AND);
                }

                if(bSatisfiedItemSearch) {
                // 'Resolved To' search results restricts Inactive EC / Issue objects
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strECStateClose);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_AND);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strECStateComplete);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_AND);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strECStateReject);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_AND);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_NOT_EQUAL);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strIssueStateClosed);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }

                if(!strState.equals(DomainConstants.QUERY_WILDCARD)) {

                    if(bSatisfiedItemSearch) {
                        sbWhereExp.append(SYMB_AND);
                    }
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_MATCH);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strState);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }
            }

            String strFilteredExpression = getFilteredExpression(context,programMap);

            if ((strFilteredExpression != null)
                    && !("null".equalsIgnoreCase(strFilteredExpression))
                    && !strFilteredExpression.equals("")) {
                    if (bStart) {
                            sbWhereExp.append(SYMB_OPEN_PARAN);
                            bStart = false;
                    } else {
                            sbWhereExp.append(SYMB_AND);
                    }
                    sbWhereExp.append(strFilteredExpression);
            }
            if (!bStart) {
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
            }

           // returning empty maplist since 'Close' 'Complete' and 'Reject' state EC objects
           //and 'Closed' state Issue objects should not be added as a Satisfied Items to an Engineering Change
           if(bInactiveECAndIssue){
               mapList = new MapList();
           } else {

			finalmap = new MapList();
			StringTokenizer stz = new StringTokenizer(strType,",");

			String sAnd = "&&";
			String sDevPart = PropertyUtil.getSchemaProperty(context,"policy_DevelopmentPart");
			String buswhere="policy!='"+sDevPart+"'";
			String buswhere1 = "policy!='"+PropertyUtil.getSchemaProperty(context,"policy_ManufacturingPart")+"'";
			sbWhereExp.append(sAnd).append(" ").append(buswhere).append(sAnd).append(" ").append(buswhere1);

			if(strObjectId != null) {

				DomainObject objSCO =  DomainObject.newInstance(context,strObjectId);
				String busid = objSCO.getInfo(context,"to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id");
				if(busid != null && busid.length() > 0) {
					sbWhereExp.append(" && ");
					sbWhereExp.append("(((to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == True ) && (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id == " + busid + " )) || (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == False))");
				}
			}

			while(stz.hasMoreTokens())
			{
				strType = stz.nextToken();
				mapList = DomainObject.findObjects(context,
                                                      strType,
                                                      strName,
                                                      strRevision,
                                                      strOwner,
                                                      strVault,
                                                      sbWhereExp.toString(),
                                                      "",
                                                      true,
                                                      (StringList) slSelect,
                                                      sQueryLimit);

			finalmap.addAll(mapList);
			}
				mapList.clear();
				mapList.addAll(finalmap);
			}
        } catch (Exception excp) {
                excp.printStackTrace(System.out);
                throw excp;
        }
        return mapList;
    }
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getDelegateAssigneesExcludePersonList(Context context, String args[]) throws Exception
    {

        // List of Person Ids to be excluded from Search results
        StringList excludePersonList = new StringList();
        // list of Person Ids to be included in Search Results
        StringList includePersonList = new StringList();
        StringList personSelects = new StringList(1);
        personSelects.add(SELECT_ID);
        StringList strlMemberRelnSelects = new StringList(1);
        // Get all the persons
		strlMemberRelnSelects.add("attribute[" + ATTRIBUTE_PROJECT_ROLE + "]");

        Map programMap    = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        String strCount = (String) programMap.get("count");
        int selectCount = Integer.parseInt(strCount);
        String loginPersonId = (String) programMap.get("loginPersonId");

		DomainObject changeObj = new DomainObject(objectId);

		String strPersonWhereClause = "current == \"" + STATE_PERSON_ACTIVE + "\"";

		String strCompId = null;

		if (changeObj.isKindOf(context, DomainConstants.TYPE_ECR))
		{
			strCompId = changeObj.getInfo(context, "to["+ PropertyUtil.getSchemaProperty(context,"relationship_ChangeResponsibility") + "].from.id");
		}
		else if (changeObj.isKindOf(context, DomainConstants.TYPE_ECO))
		{
			strCompId = changeObj.getInfo(context, "to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id");
		}

		if (strCompId == null || strCompId.length() == 0)
		{
			strCompId = Company.getHostCompany(context);
		}

		DomainObject doComp = new DomainObject(strCompId);

        MapList allPersonMapList = doComp.getRelatedObjects(
                                context,               // context
                                RELATIONSHIP_MEMBER, // relationship pattern
                                TYPE_PERSON,        // object pattern
                                personSelects,      // object selects
                                strlMemberRelnSelects, // relationship selects
                                false,              // to direction
                                true,               // from direction
                                (short) 1,          // recursion level
                                null,        // object where clause
                                null);              // relationship where clause


        int personListSize = allPersonMapList.size();
        StringList allPersonList = new StringList();
        for(int i=0;i<personListSize;i++)
        {
            Map allPersonMap = (Map) allPersonMapList.get(i);
            String allPersonId = (String) allPersonMap.get(SELECT_ID);
            String strPersonRole = (String) allPersonMap.get("attribute[" + ATTRIBUTE_PROJECT_ROLE + "]");
            if ((strPersonRole.indexOf("role_DesignEngineer") != -1) || (strPersonRole.indexOf("role_SeniorDesignEngineer") != -1))
            {
            	allPersonList.add(allPersonId);
			}
        }
        int intNumPersons = allPersonList.size();
        StringList assignees = new StringList();
        String typePattern = TYPE_PERSON;
        String relPattern = RELATIONSHIP_ASSIGNED_EC;
        StringList busSelects = new StringList(1);
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_ID);
        // get all the assignees for the ECR
        MapList assigneeList = changeObj.getRelatedObjects(context,relPattern,typePattern,busSelects,null,true,false,(short) 1,null,null);
        int size = assigneeList.size();
        boolean hasAssignees = true;
        for(int i=0;i<size;i++)
        {
            Map assigneeMap = (Map) assigneeList.get(i);
            String assignee = (String) assigneeMap.get(SELECT_NAME);
            String relationship = (String) assigneeMap.get("relationship");
            if(relationship.equals(RELATIONSHIP_ASSIGNED_EC))
            {
                 String assigneeId = (String) assigneeMap.get(SELECT_ID);
                 assignees.add(assigneeId);
            }
        }
        int intNumAssignees = assignees.size();

        // If there is only one assignee and that too context user
         if(intNumAssignees == 1)
         {
             String assignedPersonId = (String) assignees.get(0);
            if(assignedPersonId.equals(loginPersonId))
            {
                hasAssignees = false;
            }
         }

         // if there are no assignees other than context user, and if all asignees are not selected
        if (hasAssignees && intNumAssignees != selectCount)
        {

			for(int count = 0;count < intNumPersons;count++)
			{
				String id = (String) allPersonList.get(count);
				if(!assignees.contains(id))
				{
					excludePersonList.add(id);
				}
			}
		}
        return excludePersonList;
    }
    /**
     * Get the list of all Objects to be excluded while Adding Engineering Change to Products
     *
     * @param context     the eMatrix <code>Context</code> object
     * @param args        contains a Map with the following input arguments or entries:
     * @return            a <code>MapList</code> object having the list of objects already connected.
     * @throws            Exception if the operation fails
     * @since             R207
     **
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeConnectedEC(Context context, String args[]) throws Exception
    {
        StringList objectList=new StringList();
        Map programMap = (HashMap) JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("parentOID");
		// Start:R210. Handled for Structure Browser
        if(strObjectId == null || "null".equals(strObjectId))
        {
        	strObjectId  = (String) programMap.get("emxTableRowId");
       	 	if(strObjectId != null && strObjectId.indexOf("|") >= 0)
            {
       		 strObjectId = (String)FrameworkUtil.split(strObjectId, "|").get(1);
            }
        }
        // End:R210
        DomainObject domObject = new DomainObject(strObjectId);
        String typePattern = TYPE_ENGINEERING_CHANGE;
        String relPattern = RELATIONSHIP_EC_AFFECTED_ITEM;
        StringList objSelect = new StringList(1);
        objSelect.addElement(SELECT_ID);
        String objString = null;


        MapList mapConnectedEC = domObject.getRelatedObjects(context,relPattern,typePattern,
                                   objSelect,null,true,false,
                                   (short) 1,null,null);

        if(mapConnectedEC.size() > 0){

            for(int cnt=0;cnt<mapConnectedEC.size();cnt++)
            {
                Map objMap = (Map)mapConnectedEC.get(cnt);
                objString = (String)objMap.get(SELECT_ID);
                objectList.add(objString);
            }
        }
        return objectList;
    }

    /* includeAssigneeOIDs() method returns OIDs of Person having Senior Design Engineer (or) Design Engineer role
	      * @param context Context : User's Context.
	      * @param args String array
	      * @return The StringList value of PersonIds
	      * @throws Exception if operation fails.
	      * @since R208
	      */
	     @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	     public StringList includeAssigneeOIDs(Context context, String[] args)
	          throws Exception
	          {
	                  StringList strlPersonsIDs = new StringList();
	                  String strRole1 = PropertyUtil.getSchemaProperty(context,"role_SeniorDesignEngineer");
	                  String strRole2 = PropertyUtil.getSchemaProperty(context,"role_DesignEngineer");
	                  StringList strlRoles = new StringList(2);
	                  strlRoles.addElement(strRole1);
	                  strlRoles.addElement(strRole2);
	                  try {
	                      Iterator itr = strlRoles.iterator();
	                      while(itr.hasNext()){
	                          String strTempRole = (String)itr.next();
	                          String strRolePersonIds = MqlUtil.mqlCommand(context,"print role $1 select $2 dump $3",strTempRole, "person.object.id", ",");
	                          StringList strlRolePersonIds = FrameworkUtil.split(strRolePersonIds, ",");
	                          Iterator itr1 = strlRolePersonIds.iterator();
	                          //check n Add this persons ids to final list
	                          while(itr1.hasNext()){
	                              String strTempPersonIds = (String)itr1.next();
	                              if(strlPersonsIDs.size()>0){
	                                  if(!strlPersonsIDs.contains(strTempPersonIds))
	                                      strlPersonsIDs.add(strTempPersonIds);
	                              }else{
	                                  strlPersonsIDs.add(strTempPersonIds);
	                              }
	                          }
	                      }
                  //Modified for TBE - IR-067404V6R2011x. If TBE is installed all same context users should be visible in search result
                  if(FrameworkUtil.isSuiteRegistered(context,"appVersionEngineeringSMB",false,null,null)
                		  && context.getRole() != null && !"".equals(context.getRole())) {
                	  strlPersonsIDs.addAll((StringList)JPO.invoke(context, "emxTeamUtils", null, "getContextProjectUsers", null, StringList.class));
                  }
              } catch (FrameworkException e) {
                  e.printStackTrace();
	             }
	             return strlPersonsIDs;
        }
	     /**
	      * Check to see if this is the last revision of the object.
	      *
	      * @param context the eMatrix <code>Context</code> object
	      * @return a boolean indicating whether it is true or false
	      * @throws FrameworkException if the operation fails
	      * @since R214
	      */
	     protected boolean isLastRevision(Context context, DomainObject object)
	         throws FrameworkException
	     {
	    	 boolean isContextPushed = false;
	         try
	         {
	             ContextUtil.pushContext(context);
	             isContextPushed = true;
	             DomainObject lastRevision = this.getLastRevision(context, object);
	             String lastObjectId = lastRevision.getId(context);
	             return object.getId(context).equals(lastObjectId);
	         }
	         catch (Exception e)
	         {
	             throw (new FrameworkException(e));
	         }
	         finally
	         {
	             if(isContextPushed)
	             {
	                ContextUtil.popContext(context);
	             }
	         }
	     }
	 	/**
	 	 * Checks whether the policy of a particular object has only major sequence defined
	 	 * @param context Matrix context
	 	 * @param object policy of which to check against
	 	 * @return true if the associated policy has major sequence defined
	 	 * @throws MatrixException
	 	 * @since R214
	 	 *
	 	 */
	 	protected boolean isMajorPolicy(Context context, DomainObject object) throws MatrixException
	 	{
			return !object.getPolicy(context).hasMinorSequence(context);
	 	}

	 	/**
	 	 * Get last major revision of an object
	 	 * @param context Matrix context
	 	 * @param object the domain object
	 	 * @return last major revision
	 	 * @throws FrameworkException
	 	 * @since R214
	 	 */
	 	protected DomainObject getLastMajorRevision(Context context, DomainObject object) throws FrameworkException
	 	{
	 		String strLastId = object.getInfo(context,"majorid.lastmajorid.bestsofar.id");

	 		return DomainObject.newInstance(context, strLastId);
	 	}


	 	/**
	 	 * Return last major revision if the object policy has major sequence defined, otherwise return last minor revisoin
	 	 * @param context Matrix context
	 	 * @param object the domain object
	 	 * @return last major revision or minor revision depending on the policy
	 	 * @throws MatrixException
	 	 * @since R214
	 	 */
	 	protected DomainObject getLastRevision(Context context, DomainObject object) throws MatrixException
	 	{
	         boolean isMajor = this.isMajorPolicy(context, object);
	         return getLastRevision(context, object, isMajor);
	 	}

	 	/**
	 	 * Return last major or minor revision
	 	 * @param context Matrix context
	 	 * @param object the domain object
	 	 * @param major true to return major revision
	 	 * @return last major or minor revision
	 	 * @throws MatrixException
	 	 * @since R214
	 	 */
	 	protected DomainObject getLastRevision(Context context, DomainObject object, boolean major) throws MatrixException
	 	{
	 		BusinessObject bo;
	        if(major){
	           	 bo = this.getLastMajorRevision(context, object);
	        }
	        else{
	           	 bo = object.getLastRevision(context);
	        }
	        return newInstance(context, bo);
	 	}

	 	/**
	 	 * Major revise or minor revise an object based on its policy
	 	 * @param context Matrix context
	 	 * @param object the domain object
	 	 * @param inheritFiles inherit file or not
	 	 * @return new revision
	 	 * @throws MatrixException
	 	 * @since R214
	 	 */
	 	protected BusinessObject revise(Context context, DomainObject object, boolean inheritFiles) throws Exception
	 	{
	 		boolean isMajorPolicy = isMajorPolicy(context, object);
	 		String ReqSpecType = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_SoftwareRequirementSpecification);
	 		if(isMajorPolicy){
	 			DomainObject lastRev = this.getLastMajorRevision(context, object);
	 			String nextSequence = lastRev.getNextMajorSequence(context);
	 			String vault = lastRev.getInfo(context, DomainObject.SELECT_VAULT);
		 		String physicalId = com.matrixone.jsystem.util.UUID.getNewUUIDHEXString();

		 		BusinessObject nextRev = lastRev.revise(context, null, nextSequence, vault, physicalId, inheritFiles, !isMajorPolicy);
		 		if( object.isKindOf(context, ReqSpecType)){
		 			String strIsVCDoc = object.getInfo(context,CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
		 			if(strIsVCDoc == null || !strIsVCDoc.equalsIgnoreCase("true")){
		 				new CommonDocument(object).copyDocumentFiles(context, lastRev.getObjectId(context), nextRev, inheritFiles);
		 			}
		 		}

		 		return nextRev;
	 		}
	 		else{
	 	        String lastId = object.getInfo(context, SELECT_LAST_ID);
	 			BusinessObject nextRev = object.reviseObject(context, inheritFiles);
		 		if(object.isKindOf(context, ReqSpecType)){
		 			String strIsVCDoc = object.getInfo(context,CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
		 			if(strIsVCDoc == null || !strIsVCDoc.equalsIgnoreCase("true")){
		 				new CommonDocument(object).copyDocumentFiles(context, lastId, nextRev, inheritFiles);
		 			}
		 		}

		 		return nextRev;
	 		}

	 	}
	 	
	 	 /**
	     * Get the list of all Objects to be excluded while Adding affected items to EC
	     *
	     * @param context     the eMatrix <code>Context</code> object
	     * @param args        contains a Map with the following input arguments or entries:
	     * @return            a <code>StirngList</code> object having the list of objects already connected.
	     * @throws            Exception if the operation fails
	     * @since             R417
	     **
	     */
	 	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
		public StringList excludeConnectedAffectedItems(Context context, String[] args) throws Exception
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strECId = (String)programMap.get("objectId");
			StringList tempStrList = new StringList();
			tempStrList.addAll(new DomainObject(strECId).getInfoList(context, "from["+DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM+"].to.id"));
			return tempStrList;
		}
		 /**
	     * This method checks whether 3DPlay command is enabled or not.
	     * Returns true if all the following conditions are satisfied
	     *   1)Property key emxComponents.Toggle.3DViewer is set to true by default
	     *    If the property key emxComponents.Toggle.3DViewer is set to true 3D Play command is shown and 3DLive Examine command will be hidden
	     * @param context the matrix context
	     * @param args
	     * @return true if all the above mentioned conditions are satisfied
	     * @throws Exception
	     */
		public boolean showOrHide3DPlay(Context context,String[] args)throws Exception{
			return is3DViewerEnabled(context, args);
		}
		  /**
	     * This method checks whether 3DLiveExamine command is enabled or not.
	     * Returns true if all the following conditions are satisfied
	     *   1)Property key emxComponents.show.3DPlay is set to true by default
	     *    If the property key emxComponents.Toggle.3DViewer is set to true 3DLive Examine command is shown and 3D Play command will be hidden
	     * @param context the matrix context
	     * @param args
	     * @return true if all the above mentioned conditions are satisfied
	     * @throws Exception
	     */
		public boolean showOrHide3DLiveExamine(Context context,String[] args)throws Exception{
			return !is3DViewerEnabled(context,args);
		}
		 /**
	     * This method is added to toggle between 3dPlay and 3dLiveExamine viewers based on below property
	     * Returns true if all the following conditions are satisfied
	     *   1)Property key emxComponents.show.3DPlay is set to true by default
	     * @param context the matrix context
	     * @param args
	     * @return true if all the above mentioned conditions are satisfied
	     * @throws Exception
	     */
		private boolean is3DViewerEnabled(Context context,String[] args)throws Exception {
		String s3DPlayshow  = "true";
		try {
			s3DPlayshow = EnoviaResourceBundle.getProperty(context, "emxComponents.Toggle.3DViewer");	
		} catch (Exception e) {
			s3DPlayshow = "true";
		} 
		return ("false".equalsIgnoreCase(s3DPlayshow)) ? false : true;
	}	
}
