/*
 * emxChangeBase
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 *
 */


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.RouteTemplate;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxAttr;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.jsystem.util.StringUtils;
import com.matrixone.apps.framework.ui.UIUtil;
import java.util.Locale;



/**
 * The <code>ChangeBase</code> class contains methods for executing JPO operations related
 * to objects of the admin type  Change.
 * @author Cambridge
 * @version Common X3- Copyright (c) 2007, Enovia MatrixOne, Inc.
 **
 */
public class emxChangeBase_mxJPO extends emxDomainObject_mxJPO {

	/********************************* ADMIN TYPE SELECTABLES /*********************************
    /** relationship "Affected Item". */
    public static final String RELATIONSHIP_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_AffectedItem");

	/** Relationship "ECO Change Request Input". */
    public static final String RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT = PropertyUtil.getSchemaProperty("relationship_ECOChangeRequestInput");

    	/** Relationship "Part Version". */
    public static final String RELATIONSHIP_PART_VERSION = PropertyUtil.getSchemaProperty("relationship_PartVersion");

	/** Relationship "Design Responsibility" */
	public static final String RELATIONSHIP_DESIGN_RESPONSIBILITY     = PropertyUtil.getSchemaProperty("relationship_DesignResponsibility");

    /** Relationship "Assigned Affected Item". */
    public static final String RELATIONSHIP_ASSIGNEED_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_AssignedAffectedItem");

    /** Relationship "Part Specification". */
    public static final String RELATIONSHIP_PART_SPECIFICATION = PropertyUtil.getSchemaProperty("relationship_PartSpecification");

    /** Relationship "Reference Document". */
    public static final String RELATIONSHIP_REFERENCE_DOCUMENT = PropertyUtil.getSchemaProperty("relationship_ReferenceDocument");

	/** Relationship "Change Responsibility". */
    public static final String RELATIONSHIP_CHANGE_RESPONSIBILITY = PropertyUtil.getSchemaProperty("relationship_ChangeResponsibility");

	/** Attribute "Requested Change" */
	public static final String ATTRIBUTE_REQUESTED_CHANGE = PropertyUtil.getSchemaProperty("attribute_RequestedChange");

	/* Branch To Attribute */
	public static final String strBranchTo =	PropertyUtil.getSchemaProperty("attribute_BranchTo");

	/** Type "Project Space" */
	public static final String TYPE_PROJECT_SPACE              = PropertyUtil.getSchemaProperty("type_ProjectSpace");

	/** Type "Member List" */
	public static final String TYPE_MEMBER_LIST              = PropertyUtil.getSchemaProperty("type_MemberList");

	/** Type "Route" */
	public static final String TYPE_ROUTE              = PropertyUtil.getSchemaProperty("type_Route");

	 /** the "ECO" policy. */
    public static final String POLICY_ECO = PropertyUtil.getSchemaProperty("policy_ECO");

	 /** the "ECR" policy. */
    public static final String POLICY_ECR = PropertyUtil.getSchemaProperty("policy_ECR");

	 /** the "EC Part" policy. */
    public static final String POLICY_EC_PART = PropertyUtil.getSchemaProperty("policy_ECPart");

	 /** the "CAD Drawing" policy. */
    public static final String POLICY_CAD_DRAWING = PropertyUtil.getSchemaProperty("policy_CADDrawing");

    /** policy "CAD Model" */
    public static final String POLICY_CAD_MODEL =            PropertyUtil.getSchemaProperty("policy_CADModel");

    public static final String POLICY_PART_SPECIFICATION =            PropertyUtil.getSchemaProperty("policy_PartSpecification");

    /** state "Create" for the "ECO" policy. */
    public static final String STATE_ECO_CREATE = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Create");

    /** state "Define Components" for the "ECO" policy. */
    public static final String STATE_ECO_DEFINE_COMPONENTS = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_DefineComponents");

    /** state "Design Work" for the "ECO" policy. */
    public static final String STATE_ECO_DESIGN_WORK = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_DesignWork");

	/** state "Create" for the "ECR" policy. */
    public static final String STATE_ECR_CREATE = PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_Create");

	/** state "Submit" for the "ECR" policy. */
    public static final String STATE_ECR_SUBMIT = PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_Submit");

	/** state "Evaluate" for the "ECR" policy. */
    public static final String STATE_ECR_EVALUATE = PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_Evaluate");

	/** state "Review" for the "ECR" policy. */
    public static final String STATE_ECR_REVIEW = PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_Review");

    /** state "Release" for the "EC Part" policy. */
    public static final String STATE_ECPART_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_EC_PART, "state_Release");

    public static final String STATE_PARTSPECIFICATION_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_PART_SPECIFICATION, "state_Release");

	/** state "Release" for the "CAD Drawing" policy. */
	public static final String STATE_CADDRAWING_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_CAD_DRAWING, "state_Release");

	/** state "Release" for the "CAD Model" policy. */
	public static final String STATE_CADMODEL_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_CAD_MODEL, "state_Release");

	/** state "Release" for the "Drawing Print" policy. */
	public static final String STATE_DRAWINGPRINT_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_DRAWINGPRINT, "state_Release");

	/** Preference "Design Responsibility" */
	public static final String strPref_DesignResp        = PropertyUtil.getSchemaProperty("preference_DesignResponsibility");

	/** Vault "eService Production" */
	public static final String VAULT_ESERVICE_PRODUCTION =	PropertyUtil.getSchemaProperty("vault_eServiceProduction");

	/* Is Version Attribute */
	public static final String isVersion = PropertyUtil.getSchemaProperty("attribute_IsVersion");
	/* Type MECO  */
	public static final String TYPE_MECO=PropertyUtil.getSchemaProperty("type_MECO");
	/* Type DCR  */
	public static final String TYPE_DCR=PropertyUtil.getSchemaProperty("type_DCR");
	    /** A string constant with the value vaultOption. */
    public static final String VAULT_OPTION              = "vaultOption";

    /** the "Part Markup" policy. */
    public static final String POLICY_PART_MARKUP              = PropertyUtil.getSchemaProperty("policy_PartMarkup");
	/** the "Part Markup" type. */
    public static final String TYPE_PART_MARKUP                = PropertyUtil.getSchemaProperty("type_PARTMARKUP");
	/** the "Proposed Markup" relationship. */
    public static final String RELATIONSHIP_PROPOSED_MARKUP    = PropertyUtil.getSchemaProperty("relationship_ProposedMarkup");
	/** the "Applied Markup" relationship. */
    public static final String RELATIONSHIP_APPLIED_MARKUP     = PropertyUtil.getSchemaProperty("relationship_AppliedMarkup");

	public static String sGlobalTypefilterValue="";
	public static String sGlobalRequestedChangeFilter="";
	public static String sGlobalAssigneeName="";
	public static String sGlobalAssigneeIds="";

	public static final String RANGE_REVIEW = "Review";
	public static final String RANGE_APPROVAL = "Approval";
	public final static String RANGE_FOR_UPDATE="For Update";
	public final static String ROW_EDITABLE="RowEditable";
	public final static String ROW_SHOW="show";
	public final static String ROW_READ_ONLY="readonly";
	public final static String RANGE_FOR_REVISE="For Revise";
	public final static String RANGE_FOR_RELEASE="For Release";
	public final static String RANGE_FOR_OBSOLETE="For Obsolescence";
	public final static String RANGE_NONE="None";

	//HF-021127V6R2010x
	public final static String SELECT_ROUTE_ACTION    = "attribute[" + ATTRIBUTE_ROUTE_ACTION + "]";
	public final static String SELECT_APPROVAL_STATUS = "attribute[" + ATTRIBUTE_APPROVAL_STATUS + "]";
	public static final String RANGE_APPROVE          = "Approve";

	/********************************* OPERATOR SYMBOLS /*********************************
	/** A string constant with the value &&. */
	protected static final String SYMB_AND = " && ";
	/** A string constant with the value ~~. */
	protected static final String SYMB_MATCH = " ~~ ";
	/** A string constant with the value !. */
	protected static final String SYMB_NOT = "!";
	/** A string constant with the value !=. */
	protected static final String SYMB_NOT_EQUAL = "!=";
	/** A string constant with the value ==. */
	protected static final String SYMB_EQUAL = " == ";
	/** A string constant with the value '. */
	protected static final String SYMB_QUOTE = "'";
	/** A string constant with the value *. */
	protected static final String SYMB_WILD = "*";
	/** A string constant with the value (. */
	protected static final String SYMB_OPEN_PARAN = "(";
	/** A string constant with the value ). */
	protected static final String SYMB_CLOSE_PARAN = ")";
	/** A string constant with the value attribute. */
	protected static final String SYMB_ATTRIBUTE = "attribute";
	/** A string constant with the value [. */
	protected static final String SYMB_OPEN_BRACKET = "[";
	/** A string constant with the value ]. */
	protected static final String SYMB_CLOSE_BRACKET = "]";
	/** A string constant with the value to. */
	protected static final String SYMB_TO = "to";
	/** A string constant with the value from. */
	protected static final String SYMB_FROM = "from";
	/** A string constant with the value tomid. */
	protected static final String SYMB_TO_MID = "tomid";
	/** A string constant with the value frommid. */
	protected static final String SYMB_FROM_MID = "frommid";
	/** A string constant with the value fromrel. */
	protected static final String SYMB_FROM_REL = "fromrel";
	/** A string constant with the value torel. */
	protected static final String SYMB_TO_REL = "torel";
	/** A string constant with the value ".". */
	protected static final String SYMB_DOT = ".";
	/** A string constant with the value "null". */
	protected static final String SYMB_NULL = "null";
	/** A string constant with the value "string". */
	protected static final String SYMB_STRING = "string";
	/** A string constant with the value "". */
	protected static final String SYMB_EMPTY_STRING = "";
	/** A string constant with the value "descending". */
	protected static final String SYMB_DESCENDING = "descending";
	/** A string constant with the value "|". */
	protected static final String SYMB_PIPE = "|";
	/** A string constant with the value "|". */
	protected static final String SYMB_COMMA = ",";
	/** A string constant with the value " ". */
	protected static final String SYMB_SPACE = " ";
	/** A string constant with the value "true". */
	protected static final String SYMB_TRUE = "true";
	/** A string constant with the value "false". */
	protected static final String SYMB_FALSE = "false";
	/** A string constant with the value "0". */
	protected static final String SYMB_ZERO = "0";
	/** A string constant with the value ".xml". */
	protected static final String SYMB_EXT_XML = ".xml";
   /** A string constant with the value -. */
    public static final String SYMB_HYPHEN = "-" ;

	/********************************* CUSTOM FILTER FIELDS /*********************************
	/** A string constant with the value "field_choices". */
	protected static String KEY_FIELD_CHOICES = "field_choices";
	/** A string constant with the value "field_display_choices". */
	protected static String KEY_FIELD_DISPLAY_CHOICES = "field_display_choices";

	/********************************* STRING RESOURCES FIELDS /*********************************
	/** A string constant with the value "emxComponentsStringResource". */
	protected static String RESOURCE_BUNDLE_COMPONENTS_STR = "emxComponentsStringResource";

	/********************************* MAP SELECTABLES /*********************************
	/** A string constant with the value "objectId". */
	protected static final String SELECT_OBJECT_ID = "objectId";
	/** A string constant with the value "relId". */
	protected static final String SELECT_REL_ID = "relId";
	/** A string constant with the value "parentOID". */
	protected static final String SELECT_PARENT_OBJECT_ID = "parentOID";
	/** A string constant with the value "requestMap". */
	protected static final String SELECT_REQUEST_MAP = "requestMap";
	/** A string constant with the value "paramMap". */
	protected static final String SELECT_PARAM_MAP = "paramMap";
	/** A string constant with the value "fieldMap". */
	protected static final String SELECT_FIELD_MAP = "fieldMap";
	/** A string constant with the value "objectList". */
	protected static final String SELECT_OBJECT_LIST = "objectList";
	/** A string constant with the value "paramList". */
	protected static final String SELECT_PARAM_LIST = "paramList";
	/** A string constant with the value "jsTreeID". */
	protected static final String SELECT_TREE_ID = "jsTreeID";
	/** A string constant with the value "New Value". */
	protected static final String SELECT_NEW_VALUE = "New Value";
	/** A string constant with the value "process". */
	protected static final String SELECT_PROCESS = "process";
	/** A string constant with the value "srcDestRelName". */
	protected static final String SELECT_REL_NAME = "srcDestRelName";
	/** A string constant with the value "table". */
	protected static final String SELECT_TABLE = "table";
	/** A string constant with the value "emxTableRowId". */
	protected static final String SELECT_TABLE_IDS = "emxTableRowId";
	/** A string constant with the value "reqTableMap". */
	protected static final String SELECT_TABLE_MAP = "reqTableMap";
	/** A string constant with the value "reqMap". */
	protected static final String SELECT_ADD_REQUEST_MAP = "reqMap";
	/** A string constant with the value "isTo". */
	protected static final String SELECT_IS_TO = "isTo";
	/** A string constant with the value "doReConnect". */
	protected static final String SELECT_RECONNECT = "doReConnect";
	/** A string constant with the value "Name". */
	protected static final String SELECT_FIELD_NAME = "Name";
	/** A string constant with the value "All". */
	protected static final String SELECT_ALL = "All";
	/** A string constant with the value "languageStr". */
    protected static final String LANGUAGE_STR = "languageStr";

   	/********************************* REQUEST PARAMETERS /*********************************
	/** A string constant with the value "selection". */
	protected static final String SYMB_SELECTION = "selection";
	/** A string constant with the value "none". */
	protected static final String SYMB_NONE = "none";
	/** A string constant with the value "multiple". */
	protected static final String SYMB_MULTIPLE = "multiple";

   	/********************************* MQL COMMAND KEYWORDS /*********************************
	/** A string constant with the value "select". */
	protected static final String COMMAND_SELECT = "select";
	/** A string constant with the value "modify bus". */
	protected static final String COMMAND_MODIFY_BUSINESS_OBJECT = "modify bus";
	/** A string constant with the value "print connection". */
	protected static final String COMMAND_PRINT_CONNECTION = "print connection";
	/** A string constant with the value "dump". */
	protected static final String COMMAND_DUMP = "dump";

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @throws        Exception if the operation fails
     * @since         Common X3
     **
     */
    public emxChangeBase_mxJPO (Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @return        an integer status code (0 = success)
     * @throws        Exception when problems occurred in the Common Components
     * @since         Common X3
     **
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            i18nNow i18nnow = new i18nNow();
            String strContentLabel = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Error.UnsupportedClient");
            throw  new Exception(strContentLabel);
        }
        return  0;
    }


	/**
     * Checks whether the passed relationship exists or not
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @param args holds relationship name.
     * @param args holds Type pattern
     * @param args holds TO/FROM.
     * @param args Resource file name.
     * @param args alert message id.
     * @return Boolean
     * @throws Exception if the operation fails.
     * @since Common X3.
    */
    public int checkForRelationship(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        String objectId = args[0];
        setId(objectId);
        String strRelationshipName = PropertyUtil.getSchemaProperty(context,args[1]);
		String strTypeNames = args[2];
        boolean boolFrom = true;
		boolean boolTo = true;
		if(args[3].equalsIgnoreCase("TO")){
		boolTo = false;
		}
		else{
		boolFrom = false;
		}
		String strResourceFieldId = args[4];
		String strStringId = args[5];
		String strMessage = EnoviaResourceBundle.getProperty(context,strResourceFieldId,context.getLocale(), strStringId);

        StringTokenizer stz = null;
		int relationshipExists = 1;

		StringList busSelects = new StringList(1);
			busSelects.add(DomainConstants.SELECT_ID);
		StringList relSelects = new StringList(1);
			relSelects.add(DomainConstants.SELECT_ID);

        if (strTypeNames.indexOf(" ")>-1){
                stz = new StringTokenizer(strTypeNames," ");
            }
        else if (strTypeNames.indexOf(",")>-1){
                stz = new StringTokenizer(strTypeNames,",");
            }
        else if(strTypeNames.indexOf("~")>-1){
                stz = new StringTokenizer(strTypeNames,"~");
            }
        else{
                stz = new StringTokenizer(strTypeNames,"");
            }

			String strTypePattern = "";
			int counttokens = stz.countTokens();
			for(int ik = 1;ik<=counttokens;ik++)
			{
				if(ik<counttokens)
				{
					strTypePattern = strTypePattern + PropertyUtil.getSchemaProperty(context, stz.nextToken()) + ",";
				}
				else
				{
					strTypePattern = strTypePattern + PropertyUtil.getSchemaProperty(context, stz.nextToken());
				}
			}

		MapList mapPrjRole = getRelatedObjects(context,strRelationshipName,strTypePattern,busSelects,relSelects,boolTo,boolFrom, (short)1, null, null);
		if(mapPrjRole != null && mapPrjRole.size()>0){
					relationshipExists = 0;
			}
		else{
				emxContextUtil_mxJPO.mqlNotice(context,strMessage);
			}

        return relationshipExists;
    }


	/**
     * sets the Owner of the object
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args1
     *            holds objectId.
     * @param args2
     *            holds New Owner to set.
     * @return Boolean
     * @throws Exception
     *             if the operation fails.
     * @since Common X3
     */
    public int setOwner(Context context, String args[]) throws Exception {

        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        String objectId = args[0];
        setId(objectId);
        setOwner(context,args[1]);
        return 0;
    }


   /**
     * this method assigns notifies the user
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args1
     *            holds objectId.
     * @param args2
     *            holds Attribute Name.
     * @param args3
     *            holds AssignNotify/Assign.
	   * @param args4
     *            holds Notification object.
     * @return Boolean and notification is sent based on the Attribute value.
     * @throws Exception if the operation fails.
     * @since Common X3.
     */
    public int assignNotifyUserByAttribute(Context context, String args[])
            throws Exception {

        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        String objectId = args[0];
        setId(objectId);

        String strAttributeName = PropertyUtil.getSchemaProperty(context,args[1]);
        String strAssignNotify = args[2];
		String strNotification = args[3];
        String strNewOwner =getAttributeValue(context,strAttributeName);

        int inotify = 0;
			if (strAssignNotify.equalsIgnoreCase("AssignNotify")
					&& strNewOwner != null && !strNewOwner.equals("")) {

				String strnotifyargs[] = { objectId, strNotification };
				JPO.invoke(context, "emxNotificationUtil", null,
						"objectNotification", strnotifyargs, null);
						setOwner(context, strNewOwner);
			} else if (strAssignNotify.equalsIgnoreCase("Assign")
					&& strNewOwner != null && !strNewOwner.equals("")) {
				setOwner(context, strNewOwner);
			} else {
				inotify = 1;
			}

        return inotify;
    }

     /**
     * Returns whether the attribute value is assigned or not
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds objectId.
     * @param args
     *            holds attribute name.
     * @param args
     *            holds attribute value to compare
     * @return Boolean determines whether the attribute value is assigned or
     *         not.
     * @throws Exception if the operation fails.
     * @since Common X3.
     */
    public int checkForAttribute(Context context, String args[])
            throws Exception {

        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        String objectId = args[0];
        setId(objectId);
        String strAttributeName =  PropertyUtil.getSchemaProperty(context,args[1]);
        String strAttributeValue =  args[2];
		String strResourceFieldId = args[3];
		String strStringId = args[4];
        String strRDE = getAttributeValue(context,strAttributeName);
		String strDefaultAttrValue = "";
		String strMessage = EnoviaResourceBundle.getProperty(context,strResourceFieldId,context.getLocale(), strStringId);
	    int ichkvalue = 0;

		if(strAttributeValue.length() <= 0){
			AttributeType attrType = new AttributeType(strAttributeName);
			attrType.open(context);
			strDefaultAttrValue = attrType.getDefaultValue();
			attrType.close(context);
			//Modified for IR-067581V6R2012 	
			if (DomainConstants.EMPTY_STRING.equals(strRDE)||strRDE.equalsIgnoreCase(strDefaultAttrValue)) {
				emxContextUtil_mxJPO.mqlNotice(context,strMessage);
			    ichkvalue = 1;
			}
		}
		else
		{
			if(!strRDE.equalsIgnoreCase(strAttributeValue)){
		        emxContextUtil_mxJPO.mqlNotice(context,strMessage);
			    ichkvalue = 1;
			}
		}

        return ichkvalue;
    }

	/**
     * this method checks the Related object state
     * Returns Boolean determines whether the connected
     * objects are in approperiate state.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds objectId.
     * @param args
     *            holds relationship name.
     * @param args
     *            holds type name.
     * @param args
     *            holds policy name.
     * @param args
     *            holds State.
     * @param args
     *            holds TO/FROM.
     * @param args
     *            holds String Resource file name
     * @param args
     *            holds String resource filed key name.
     * @return Boolean determines whether the connected objects are in
     *         approperiate state.
     * @throws Exception if the operation fails.
     * @since Common X3.
     */
   public int checkRelatedObjectState(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        String objectId = args[0];
        setId(objectId);
        String strRelationshipName = PropertyUtil.getSchemaProperty(context,args[1]);
        String strTypeName = PropertyUtil.getSchemaProperty(context,args[2]);
        String strPolicyName = PropertyUtil.getSchemaProperty(context,args[3]);
        String strStates = args[4];
        boolean boolTo = args[5].equalsIgnoreCase("TO")?true:false;
        boolean boolFrom = args[5].equalsIgnoreCase("FROM")?true:false;
        String strResourceFieldId = args[6];
        String strStringId = args[7];
        String strMessage = EnoviaResourceBundle.getProperty(context,strResourceFieldId,context.getLocale(), strStringId);

        StringTokenizer stz = null;

        int ichkvalue = 0;
        if (strStates.indexOf(" ")>-1){
                stz = new StringTokenizer(strStates," ");
            }
        else if (strStates.indexOf(",")>-1){
                stz = new StringTokenizer(strStates,",");
            }
        else if(strStates.indexOf("~")>-1){
                stz = new StringTokenizer(strStates,"~");
            }
        else{
                stz = new StringTokenizer(strStates,"");
            }

            Vector vector = new Vector();
        while (stz.hasMoreElements()){
                String state = stz.nextToken();
                vector.addElement(PropertyUtil.getSchemaProperty(context, "policy", strPolicyName , state));
            }

		String strRelnWhereClause = null;
		if (RELATIONSHIP_AFFECTED_ITEM.equals(strRelationshipName))
		{
		strRelnWhereClause = "attribute[Requested Change] == \"For Release\"";
		}
            StringList busSelects = new StringList(2);
            busSelects.add(DomainConstants.SELECT_ID);
            busSelects.add(DomainConstants.SELECT_CURRENT);
            StringList relSelects = new StringList(2);
            relSelects.add(DomainConstants.SELECT_ID);

            MapList maplistObjects = getRelatedObjects(context,
                                          strRelationshipName,
                                          strTypeName,
                                          busSelects, // object Select
                                          relSelects, // rel Select
                                          boolFrom, // to
                                          boolTo, // from
                                          (short)1,
                                          null, // ob where
                                          strRelnWhereClause  // rel where
                                          );
            
           
       if (maplistObjects != null && (maplistObjects.size() > 0)){
                Iterator itr = maplistObjects.iterator();
       while (itr.hasNext() && ichkvalue != 1){
                    Map mapObject = (Map) itr.next();
                    ichkvalue = vector.contains(mapObject.get("current"))?0:1;
                }

            }
       if(ichkvalue == 1){
		        emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            }

        return ichkvalue;
    }
    /**
     * Get the list of all Objects which are connected to the context Change object as
     * "Affected Items" and Parts connected with relationship "Part Markups" to Markups
     * (Item and BOM)
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of Affected
     * Items for this Change object, and Parts connected with relationship "Part Markups"
     * to Markups (Item and BOM)
     * @throws        Exception if the operation fails
     * @since         Common X3
     **/

   public MapList getAffectedItemsWithRelSelectables(Context context, String strParentId, String strChangeId, String sENCAffectedItemsTypeFilter, String sENCAffectedItemsAssigneeFilter, String sENCAffectedItemsRequestedChangeFilter) throws Exception {

        //Initializing the return type
        MapList mlAffectedItemBusObjList = new MapList();
        //Business Objects are selected by its Ids
        StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
        //Relationships are selected by its Ids
        StringList relSelects     = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //retrieving Affected Items list from context Change object
		DomainObject changeObj = new DomainObject(strParentId);
		StringBuffer bufType = new StringBuffer(20);
	    String relPattern = RELATIONSHIP_AFFECTED_ITEM;
		if(sENCAffectedItemsTypeFilter.equalsIgnoreCase(SELECT_ALL))
		{
		      bufType.append(SYMB_WILD);
		}
		else
	        {
			bufType.append(sENCAffectedItemsTypeFilter);
		}

        	sGlobalAssigneeName=sENCAffectedItemsAssigneeFilter;
		String strAssigneeName = "";
		if (!sENCAffectedItemsAssigneeFilter.equalsIgnoreCase(SELECT_ALL))
		{
			DomainObject dobj = new DomainObject(sENCAffectedItemsAssigneeFilter);
			strAssigneeName = dobj.getInfo(context , DomainConstants.SELECT_NAME);
	   }

		sGlobalTypefilterValue=sENCAffectedItemsTypeFilter;
		sGlobalRequestedChangeFilter=sENCAffectedItemsRequestedChangeFilter;
		String sWhereClauseAssigneeFilter ="";
		String sWheresWhereClauseRequestedChangeFilter = "";
			sWheresWhereClauseRequestedChangeFilter = "attribute["+ATTRIBUTE_REQUESTED_CHANGE+"] == " + "\""+sENCAffectedItemsRequestedChangeFilter+"\"";
		sWhereClauseAssigneeFilter = "tomid.fromrel["+DomainConstants.RELATIONSHIP_ASSIGNED_EC+"].from.id  =='"+sENCAffectedItemsAssigneeFilter+"'";

		StringBuffer bufWhereClause = new StringBuffer(150);
		if((!SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsRequestedChangeFilter))&&(!SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsAssigneeFilter)))
		{
			bufWhereClause.append("((");
			bufWhereClause.append(sWhereClauseAssigneeFilter);
			bufWhereClause.append(") && (");
			bufWhereClause.append(sWheresWhereClauseRequestedChangeFilter);
			bufWhereClause.append("))");
		}
		else if((!SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsRequestedChangeFilter))&&(SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsAssigneeFilter)))
		{
			bufWhereClause.append(sWheresWhereClauseRequestedChangeFilter);
		}
		else if((SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsRequestedChangeFilter))&&(!SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsAssigneeFilter)))
		{
			bufWhereClause.append(sWhereClauseAssigneeFilter);
		}
		else
		{
			bufWhereClause.append("");
		}

		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");

        // 361410: display indirect as well as direct affected items when the requested change filter = For Release
        if (!RANGE_FOR_RELEASE.equals(sENCAffectedItemsRequestedChangeFilter))
            if (bufWhereClause.length()  == 0)
    		{
    			bufWhereClause.append("attribute[" + strAttrAffectedItemCategory + "] == Direct");
    		}
    		else
    		{
    			bufWhereClause.append(" && attribute[" + strAttrAffectedItemCategory + "] == Direct");
    		}

        String strType=changeObj.getInfo(context,DomainConstants.SELECT_TYPE);
        boolean bpart=mxType.isOfParentType(context,strType,DomainConstants.TYPE_PART);

        if (!bpart)

		{
        mlAffectedItemBusObjList = changeObj.getRelatedObjects(context,
												relPattern, // relationship pattern
												bufType.toString(), // object pattern
												objectSelects, // object selects
												relSelects, // relationship selects
												false, // to direction
												true, // from direction
												(short) 1, // recursion level
												null, // object where clause
												bufWhereClause.toString()); // relationship where clause
		}
		else
		{
			Pattern relPattern1 = new Pattern(RELATIONSHIP_APPLIED_MARKUP);
			relPattern1.addPattern(RELATIONSHIP_PROPOSED_MARKUP);
			String buswhere = "( policy == \""+POLICY_PART_MARKUP+"\"";
            buswhere += "|| policy == \""+POLICY_EBOM_MARKUP+"\" )";
			buswhere += "&& "+DomainConstants.SELECT_REL_EBOMMARKUP_ID+"=="+strParentId;
			changeObj.setId(strChangeId);
        	mlAffectedItemBusObjList = changeObj.getRelatedObjects(context,
												relPattern1.getPattern(), // relationship pattern
												"*", //TYPE_PART_MARKUP, // object pattern
												objectSelects, // object selects
												relSelects, // relationship selects
												false, // to direction
												true, // from direction
												(short) 1, // recursion level
												buswhere, // object where clause
												null); // relationship where clause
}
		return  mlAffectedItemBusObjList;
    }

/**
     * Get the list of all Objects which are required checkbox to be displayed in the Table List Page.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         Common X3
     **/

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAffectedItems(Context context, String[] args)
        throws Exception
    {

		//unpacking the arguments from variable args
		HashMap programMap         = (HashMap)JPO.unpackArgs(args);

		String sENCAffectedItemsTypeFilter = (String) programMap.get("ENCAffectedItemsTypeFilter");
		String sENCAffectedItemsAssigneeFilter = (String) programMap.get("ENCAffectedItemsAssigneeFilter");
		String sENCAffectedItemsRequestedChangeFilter = (String) programMap.get("ENCAffectedItemsRequestedChangeFilter");
		MapList mlAffectedItemBusObjList =null;
		if ("null".equals(sENCAffectedItemsTypeFilter) || sENCAffectedItemsTypeFilter == null || sENCAffectedItemsTypeFilter.length() == 0)
		{
			sENCAffectedItemsTypeFilter = SELECT_ALL;
		}
		if ("null".equals(sENCAffectedItemsAssigneeFilter) || sENCAffectedItemsAssigneeFilter == null || sENCAffectedItemsAssigneeFilter.length() == 0)
		{
			sENCAffectedItemsAssigneeFilter = SELECT_ALL;
		}
		if ("null".equals(sENCAffectedItemsRequestedChangeFilter) || sENCAffectedItemsRequestedChangeFilter == null || sENCAffectedItemsRequestedChangeFilter.length() == 0)
		{
			sENCAffectedItemsRequestedChangeFilter = SELECT_ALL;
		}


		//getting parent object Id from args
		String strParentId         = (String)programMap.get(SELECT_OBJECT_ID);
		HashMap RequestValuesMap          = (HashMap)programMap.get("RequestValuesMap");
		String[] strOID         = (String[])RequestValuesMap.get("objectId");
		String strChangeId = strOID[0];
		//If the Parent Id is that of an ECO and Affected Items Type Filter value is "Markup", it should display all the Markups related to the ECO. 
        if(TYPE_PART_MARKUP.equals(sENCAffectedItemsTypeFilter)&&strParentId.equals(strChangeId)){
            
            Pattern relPattern1 = new Pattern(RELATIONSHIP_APPLIED_MARKUP);
            relPattern1.addPattern(RELATIONSHIP_PROPOSED_MARKUP);
            String buswhere = "( policy == \""+POLICY_PART_MARKUP+"\"";
            buswhere += "|| policy == \""+POLICY_EBOM_MARKUP+"\" )";
            buswhere += "&& "+DomainConstants.SELECT_REL_EBOMMARKUP_ID+"=="+strParentId;
            DomainObject changeObj = new DomainObject(strChangeId);
            StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
            //Relationships are selected by its Ids
            StringList relSelects     = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //changeObj.setId(strChangeId);
            mlAffectedItemBusObjList = changeObj.getRelatedObjects(context,
                                                relPattern1.getPattern(), // relationship pattern
                                                TYPE_PART_MARKUP, //TYPE_PART_MARKUP, // object pattern
                                                objectSelects, // object selects
                                                relSelects, // relationship selects
                                                false, // to direction
                                                true, // from direction
                                                (short) 1, // recursion level
                                                null, // object where clause
                                                null); // relationship where clause  
            
        return mlAffectedItemBusObjList;
        }//If the Parent Id is not that of an ECO , it should display only the Markups related to the Parent Object	
        else{
		return getAffectedItemsWithRelSelectables (context, strParentId, strChangeId, sENCAffectedItemsTypeFilter, sENCAffectedItemsAssigneeFilter, sENCAffectedItemsRequestedChangeFilter);
        }
      }
	   /**
     * Get the list of all Objects which are connected to the context Change object as
     * "Affected Items".
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of Affected
     * Items for this Change object in Edit All Mode.
     * @throws        Exception if the operation fails
     * @since         Common X3
     **/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAffectedItemsForEdit(Context context,String[] args) throws Exception
    {
		//unpacking the arguments from variable args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String action = (String)programMap.get("AIaction");
		HashSet hsECRs = new HashSet();
        //getting parent object Id from args
        String strParentId         = (String)programMap.get(SELECT_OBJECT_ID);

		String strLanguage  = (String)programMap.get("languageStr");
		Locale strLocale = context.getLocale();

        //Initializing the return type
        MapList mlAffectedItemBusObjList = new MapList();
        //Business Objects are selected by its Ids
        StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
        //Relationships are selected by its Ids
        StringList relSelects     = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //retrieving Affected Items list from context EC object
		setId(strParentId);
		String strParentType = getInfo(context , DomainConstants.SELECT_TYPE);
		StringBuffer bufType = new StringBuffer(20);
		if(action.equalsIgnoreCase("editalldispositioncode"))
		{
			bufType.append(DomainConstants.TYPE_PART);
		}
		else
		{
			if(sGlobalTypefilterValue.equalsIgnoreCase(SELECT_ALL))
		{
		      bufType.append(SYMB_WILD);
		}
		else
		{
				bufType.append(sGlobalTypefilterValue);
			}
		}

		String strAssigneeName = "";

		if (!sGlobalAssigneeName.equalsIgnoreCase(SELECT_ALL))
		{
			DomainObject dobj = new DomainObject(sGlobalAssigneeName);
			strAssigneeName = dobj.getInfo(context , DomainConstants.SELECT_NAME);
		}

		String sWhereClauseAssigneeFilter ="";
		String sWheresWhereClauseRequestedChangeFilter = "";
		sWheresWhereClauseRequestedChangeFilter = "attribute["+ATTRIBUTE_REQUESTED_CHANGE+"] == " + "\""+sGlobalRequestedChangeFilter+"\"";
		sWhereClauseAssigneeFilter = "tomid.fromrel["+DomainConstants.RELATIONSHIP_ASSIGNED_EC+"].from.id  =='"+sGlobalAssigneeName+"'";

		StringBuffer bufWhereClause = new StringBuffer(150);
		if((!sGlobalRequestedChangeFilter.equalsIgnoreCase(SELECT_ALL))&&(!sGlobalAssigneeName.equalsIgnoreCase(SELECT_ALL)))
		{
			bufWhereClause.append("((");
			bufWhereClause.append(sWhereClauseAssigneeFilter);
			bufWhereClause.append(") && (");
			bufWhereClause.append(sWheresWhereClauseRequestedChangeFilter);
			bufWhereClause.append("))");
		}
		else if((!sGlobalRequestedChangeFilter.equalsIgnoreCase(SELECT_ALL))&&(sGlobalAssigneeName.equalsIgnoreCase(SELECT_ALL)))
		{
			bufWhereClause.append(sWheresWhereClauseRequestedChangeFilter);
		}
		else if((sGlobalRequestedChangeFilter.equalsIgnoreCase(SELECT_ALL))&&(!sGlobalAssigneeName.equalsIgnoreCase(SELECT_ALL)))
		{
			bufWhereClause.append(sWhereClauseAssigneeFilter);
		}
		else
		{
            bufWhereClause.append("");
		}

		mlAffectedItemBusObjList = getRelatedObjects(context,
														RELATIONSHIP_AFFECTED_ITEM,
														bufType.toString(),
                                              objectSelects,
                                              relSelects,
														false,
                                              true,
                                              (short) 1,
														null,
														bufWhereClause.toString());
		//For Editable  and NonEditable feature of "EditAll DispCode" functionality of affected Items.
		if(action.equalsIgnoreCase("editalldispositioncode"))
		{
			Iterator objectListItr = mlAffectedItemBusObjList.iterator();
			MapList mlFinalMapList = new MapList();
			while(objectListItr.hasNext())
			{
			Map objMap = (Map) objectListItr.next();
				String objId = (String)objMap.get("id");
				String sRelId = "";
				String strCurrentState = "";
				//check whether previous released revision is there or not .
				BusinessObject busObj = new BusinessObject(objId);
				BusinessObject prevbusObj = busObj.getPreviousRevision(context);
				if(!(prevbusObj.toString().trim().equals("..")))
				{
					String strPrevBusObjid = prevbusObj.getObjectId(context);
					DomainObject domObj = new DomainObject(strPrevBusObjid);
					strCurrentState = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
					sRelId =objMap.get("id[connection]").toString();
				}
				else
				{
					sRelId =objMap.get("id[connection]").toString();
				}
			DomainRelationship domRelObj=new DomainRelationship(sRelId);
			String sAttRequestedChange = domRelObj.getAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE);
				if((sAttRequestedChange.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", strLocale, "emxFramework.Range.Requested_Change.For_Revise")) || sAttRequestedChange.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.For_Obsolescence")))
							||((strCurrentState.equalsIgnoreCase(STATE_ECPART_RELEASE) || strCurrentState.equalsIgnoreCase(STATE_CADDRAWING_RELEASE) || strCurrentState.equalsIgnoreCase(STATE_CADMODEL_RELEASE) || strCurrentState.equalsIgnoreCase(STATE_DRAWINGPRINT_RELEASE) || strCurrentState.equalsIgnoreCase(STATE_PARTSPECIFICATION_RELEASE))&&!(sAttRequestedChange.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.None")))))
			{
			objMap.put(ROW_EDITABLE,ROW_SHOW);
			}
			else
			{
			objMap.put(ROW_EDITABLE,ROW_READ_ONLY);
			}
			mlFinalMapList.add(objMap);
			}//end of while

			mlAffectedItemBusObjList.clear();
			mlAffectedItemBusObjList.addAll(mlFinalMapList);
		return  mlAffectedItemBusObjList;
    }
		//For Editable  and NonEditable feature of "EditAll" functionality of affected Items.
	else if(action.equalsIgnoreCase("editall"))
	   {
			if(strParentType.equalsIgnoreCase(DomainConstants.TYPE_ECR))
			{
			return  mlAffectedItemBusObjList;
	   }
			else
			{
				MapList mlFinalMapList = new MapList();
				MapList mlECRObjList = getRelatedObjects(context,
													DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT,
													TYPE_ECR,
													objectSelects,
													null,
													false,
													true,
													(short) 1,
													null,
													null
													);
				Iterator objectListItr = mlAffectedItemBusObjList.iterator();
				while(objectListItr.hasNext())
				{
					Map objMap = (Map) objectListItr.next();
					String objId1 = (String)objMap.get("id");
					DomainObject doAIId = new DomainObject(objId1);
					MapList mlAIECRObjList = doAIId.getRelatedObjects( context,
														 PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem"),
														 TYPE_ECR,
														 objectSelects,
														 null,
														 true,
														 false,
														 (short)1,
														 null,
														 null);
					if(((mlAIECRObjList == null) || (mlAIECRObjList.size()< 1))||((mlECRObjList == null) || (mlECRObjList.size()< 1)))
					{
						objMap.put(ROW_EDITABLE,ROW_SHOW);
					}
					// Check for Related ECR
					else
					{
						if(mlAIECRObjList != null && mlAIECRObjList.size()>0)
						{
									Iterator itrAIECR = mlAIECRObjList.iterator();
									while(itrAIECR.hasNext())
									{
										Map mapAIECR = (Map)itrAIECR.next();
										String strECRId = mapAIECR.get(DomainConstants.SELECT_ID).toString();
										hsECRs.add(strECRId);
									}
						}
						if(mlECRObjList != null && mlECRObjList.size()>0)
						{
							Iterator itrECR = mlECRObjList.iterator();
							while(itrECR.hasNext())
							{
								Map mapECR = (Map)itrECR.next();
								String strECRId = mapECR.get(DomainConstants.SELECT_ID).toString();
								if((hsECRs.contains(strECRId)))
								{
									objMap.put(ROW_EDITABLE,ROW_READ_ONLY);
								}
								else
								{
									objMap.put(ROW_EDITABLE,ROW_SHOW);
								}
							}
						}
					}//end of else
					mlFinalMapList.add(objMap);
				}//end of while
				mlAffectedItemBusObjList.clear();
				mlAffectedItemBusObjList.addAll(mlFinalMapList);
				return  mlAffectedItemBusObjList;
			}//end of else
		}//end of else
		else
		{
			return  mlAffectedItemBusObjList;
		}
	}

	/* This  method connects the Context Person object and the context Change Object with
	 * the Assigned Affected Item Relationship.
	 * @param context The ematrix context of the request.
	 * @param args This string array contains following arguments:
	 *          0 - The object id of Change Object
	 *          1 - The object id of Affected Item
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since Common X3
	 */

	public void createAssignedAffectedItemRel(Context context, String[] args) throws Exception, FrameworkException
		{

			String strChangeObjectId = args[0];
			String strAffectedItemObjId = args[1];
			String strAffectedItemRelId = args[2];
			String sChangeType = args[3];
			
			try
			{
				DomainObject doAffectedItemObj = new DomainObject (strAffectedItemObjId);
				String current			= (String)doAffectedItemObj.getInfo(context, DomainConstants.SELECT_CURRENT);
				String strRequestedChangeValue = "";
				
				
				Locale strLocale = context.getLocale();
				setId(strChangeObjectId);

				if(sChangeType.equals(TYPE_MECO) || sChangeType.equals(TYPE_DCR))
				{
					strRequestedChangeValue = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale("en"), "emxFramework.Range.Requested_Change.For_Update");
				}
				else if(sChangeType.equals(TYPE_ECR))
				{
					strRequestedChangeValue = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale("en"), "emxFramework.Range.Requested_Change.For_Revise");
				}
				else
				{
					if((!current.equals(STATE_ECPART_RELEASE))||(!current.equals(STATE_CADDRAWING_RELEASE))||(!current.equals(STATE_CADMODEL_RELEASE))||(!current.equals(STATE_DRAWINGPRINT_RELEASE)))
					{
						strRequestedChangeValue = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale("en"), "emxFramework.Range.Requested_Change.For_Release");
					}
					else
					{
						strRequestedChangeValue = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale("en"), "emxFramework.Range.Requested_Change.For_Revise");
					}
				}

				String strAttrRequestedChange = PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange");
				DomainRelationship doAffectedItemRelObj=new DomainRelationship(strAffectedItemRelId);
				try
				{
					ContextUtil.pushContext(context);
					doAffectedItemRelObj.setAttributeValue(context,strAttrRequestedChange,strRequestedChangeValue);
				}
				catch (Exception ex)
				{
				}
				finally
				{
					ContextUtil.popContext(context);
				}
				//End//Setting Default Requested Change Attribute Value based on Part State

                String strBusObjPersonId = "";
                if (!"User Agent".equals(context.getUser())) {
				    strBusObjPersonId = PersonUtil.getPersonObjectID(context);
                } else {
                    String Originator = getInfo(context, DomainConstants.SELECT_ORIGINATOR);
                    strBusObjPersonId = PersonUtil.getPersonObjectID(context, Originator);
                }

			    String objectWhereSelects = "id=="+strBusObjPersonId;

				//Relationships are selected by its Ids

				StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

				MapList mlAssignedECRel = new MapList();

				mlAssignedECRel = getRelatedObjects( context,
													DomainConstants.RELATIONSHIP_ASSIGNED_EC,
													DomainConstants.TYPE_PERSON,
													//objectSelects,
													null,
													relSelects,
													true,
													false,
													(short) 1,
													objectWhereSelects,
													null);

				String strAssignedECRelId ="";
				if(mlAssignedECRel!=null && mlAssignedECRel.size()>0)
				{
					Iterator objItr= mlAssignedECRel.iterator();
					while(objItr.hasNext())
					{
					     Map objMap = (Map) objItr.next();
						//strAssignedECRelId = objMap.get("id[connection]").toString();
					      strAssignedECRelId = (String)objMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
					}
				}
				else
				{					
					ContextUtil.pushContext(context);
					try {
						DomainRelationship strDR = DomainRelationship.connect(context,
											   new DomainObject(strBusObjPersonId),
											   DomainConstants.RELATIONSHIP_ASSIGNED_EC,
											   new DomainObject(strChangeObjectId));
						strAssignedECRelId = strDR.toString();
					} catch (Exception e) {
						throw e;
					} finally {
						ContextUtil.popContext(context);
					}					
					
           		}
					// Creating new ReltoRel
					connect( context,
										RELATIONSHIP_ASSIGNEED_AFFECTED_ITEM,
										strAssignedECRelId,
										strAffectedItemRelId,
										false,
										false);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					throw ex;
				}
            }

	 /**
     * Update StartDate for EditAll functionality
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        an integer status code (0 = success)
     * @throws        Exception if the operation fails
     * @since         Common X3
     **/

	public int updateStartDate(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get(SELECT_PARAM_MAP);
        String sAttStartdate=PropertyUtil.getSchemaProperty(context, "attribute_StartDate");
        String objectId  = (String)paramMap.get(SELECT_REL_ID);
        String StartdateValue  = (String)paramMap.get(SELECT_NEW_VALUE);
        DomainRelationship obj=new DomainRelationship(objectId);
        
        //Modified for IR-054667V6R2011x-Ends
        //get the columnMap
        HashMap columnMap = (HashMap)programMap.get("columnMap");
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        // get the settingsMap
        HashMap settingsMap = (HashMap)columnMap.get("settings");
        String strFormat = (String) settingsMap.get("format");
        
        if (strFormat.equals("date")) {
            if(StartdateValue!=null && !StartdateValue.equalsIgnoreCase("")){
               double iClientTimeOffset = (new Double((String) requestMap.get("timeZone"))).doubleValue();
               String strTempStartDate= eMatrixDateFormat.getFormattedInputDate(StartdateValue,iClientTimeOffset,(java.util.Locale)(requestMap.get("locale")));
               obj.setAttributeValue(context, sAttStartdate, strTempStartDate);
            }else{
                obj.setAttributeValue(context, sAttStartdate, StartdateValue);
            }
            
        }
        //Modified for IR-054667V6R2011x-Ends
        return 0;
    }

	/* This method "getAssigneeOfAI" gets Assignee of the Affected Item.
	 * @param context The ematrix context of the request.
	 * @param args This string array contains following arguments:
	 *          0 - The programMap
     * @param args an array of String arguments for this method
     * @return Vector object that contains a vector of Assigneesof the Affected Items.
	 * @throws Exception
	 * @throws FrameworkException
	 * @since Common X3
	 */
	public Vector getAssigneeOfAI(Context context, String[] args) throws Exception {

		Vector vAssigneesOfAffectedItem	= new Vector();
		HashMap programMap		= (HashMap)JPO.unpackArgs(args);
		MapList objectList		= (MapList)programMap.get(SELECT_OBJECT_LIST);
		Iterator itrML = objectList.iterator();
        DomainObject sObject = new DomainObject();

		while(itrML.hasNext()) {
		    Map mAffectedItem = (Map) itrML.next();
			String sAffectedItemId = (String)mAffectedItem.get(DomainConstants.SELECT_ID);
			String sAffectedItemRelId = (String)mAffectedItem.get(DomainConstants.SELECT_RELATIONSHIP_ID);

            sObject.setId(sAffectedItemId);
			//Modified for IR-023591 - Starts
            //if(sObject.isKindOf(context, DomainConstants.TYPE_PART) || sObject.isKindOf(context, PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS")))
            if(( sObject.isKindOf(context,DomainConstants.TYPE_PART)  || sObject.isKindOf(context, PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS")))&&((!sAffectedItemId.equals("")) && (!sAffectedItemId.equals(null)))){
                //Modified for IR-023591 - Ends
                //vAssigneesOfAffectedItem.add(getTomidFromrelFromName(context, sAffectedItemRelId, DomainConstants.RELATIONSHIP_ASSIGNED_EC));
            	String strUserName = com.matrixone.apps.domain.util.PersonUtil.getFullName(context, getTomidFromrelFromName(context, sAffectedItemRelId, DomainConstants.RELATIONSHIP_ASSIGNED_EC));
				vAssigneesOfAffectedItem.add(strUserName);
            } else{
				    vAssigneesOfAffectedItem.add("");
            }
        }
		return vAssigneesOfAffectedItem;
    }



/**
  * Gets name of the object connected to the reltorel in the from side of the relationship.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param relid relationship id from which reltorel is connected
  * @param relationshipName relationship Name
  * @return the String, the name of the object for further processing
  * @throws FrameworkException if the operation fails
  * @since Common X3
*/
 	    public String getTomidFromrelFromName(Context context, String relid, String relationshipName)
	        throws FrameworkException
	    {
 	        //IR-037806 - Starts
            String strRes     = "";
            if (relid != null && !"".equals(relid)) {
            //IR-037806 - Ends    
                ContextUtil.startTransaction(context, true);
                //String strRes; //commented for IR-037806
                MqlUtil.mqlCommand(context, "verb on");
                try
                {

                    ContextUtil.pushContext(context);
                    strRes= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump", relid, "tomid.fromrel["+relationshipName+"].from.name");
                    ContextUtil.commitTransaction(context);
                }
                catch (Exception e)
                {
                    // Abort transaction.
                    ContextUtil.abortTransaction(context);
                    throw new FrameworkException(e);
                }
                finally
                {
                    ContextUtil.popContext(context);
                }
            } //Added for IR-037806

	return strRes;
       }

	/**
     * Expand program to Get the list of all Objects which are connected to the context id with EBOM relation.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         Common X3
     **/

	public MapList getAddRelated(Context context, String[] args)
        throws Exception
    {
        	MapList aiList = null;
		try{
			aiList = getAddRelatedWithRelSelectables (context, args);
			Iterator itr = aiList.iterator();
			MapList tList = new MapList();
			while(itr.hasNext())
			{
				Map newMap = (Map)itr.next();
				newMap.put(SYMB_SELECTION, SYMB_MULTIPLE);
				tList.add (newMap);
			}
			aiList.clear();
			aiList.addAll(tList);
			}
			catch (FrameworkException Ex) {
			throw Ex;
			}
            return aiList;
      }

	    /**
     * Get the list of all Objects which are connected to the context Change object as
     * "Affected Items" and Parts connected with relationship "Part Specification".
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of Affected
     * Items for this Change object.
     * @throws        Exception if the operation fails
     * @since         Common X3
     **/
  public MapList getAddRelatedWithRelSelectables (Context context, String[] args)
    throws Exception
  {
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String strParentId	=	(String) paramMap.get(SELECT_OBJECT_ID);
		 Object emxTableRowId = paramMap.get("emxTableRowId");
		 String emxTableRowIdstr = emxTableRowId.toString();
		 StringTokenizer st = null;
		 String strChangeObjId = null;
		 st = new StringTokenizer(emxTableRowIdstr, "|");
		 int j = st.countTokens();
		 for(int i=0;i<j;i++,st.nextToken())
		  {
				if (i==2)
				{
				strChangeObjId = st.nextToken();
				i++;
				}
		  }
     String relapttern=DomainConstants.RELATIONSHIP_EBOM+","+DomainConstants.RELATIONSHIP_PART_SPECIFICATION;
     // 363178
     // TODO: update once part specification moves to DomainConstants
     String typepattern=DomainConstants.TYPE_PART+","+DomainConstants.TYPE_CAD_DRAWING+","+DomainConstants.TYPE_CAD_MODEL+","+DomainConstants.TYPE_DRAWINGPRINT+","+PropertyUtil.getSchemaProperty(context,"type_PartSpecification");
        //Initializing the return type
        MapList mlBusObjPartList = new MapList();
		MapList mlBusObjAIList = new MapList();
		HashSet hs = new HashSet();
		MapList mlPartFinalList = new MapList();
        //Business Objects are selected by its Ids
        StringList selectStmts = new StringList();
        selectStmts.addElement(DomainConstants.SELECT_ID);
        selectStmts.addElement(DomainConstants.SELECT_NAME);
        StringList selectrelStmts=new StringList();
        selectrelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        selectrelStmts.addElement("to["+ RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");

		String whereClause = "";

		if(strChangeObjId != null && !"null".equals(strChangeObjId) && strChangeObjId.length() > 0) {
			DomainObject objSCO =  DomainObject.newInstance(context,strChangeObjId);
			String busid = objSCO.getInfo(context,"to["+ RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");
			if(busid != null && busid.length() > 0) {
				whereClause = "(((to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == True ) && (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id == " + busid + " )) || (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == False))";
			}
		}

        //retrieving Affected Items list from context Change object
        DomainObject domPartId=DomainObject.newInstance(context);
        domPartId.setId(strParentId);
        // Get Items related to selected part
        mlBusObjPartList = domPartId.getRelatedObjects(context,
                                              relapttern,
                                              typepattern,
                                              selectStmts,
                                              selectrelStmts,
                                              false,
                                              true,
                                              (short)1,
                                              whereClause,
                                             null);


		////Business Objects are selected by its Ids
		StringList selectStmts1	=	new StringList();
        selectStmts1.addElement(DomainConstants.SELECT_ID);
        selectStmts1.addElement(DomainConstants.SELECT_NAME);
        StringList selectrelStmts1	=	new StringList();
        selectrelStmts1.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
		//get affected items related to Change object
		DomainObject domchangeObj=DomainObject.newInstance(context);
        domchangeObj.setId(strChangeObjId);
        mlBusObjAIList = domchangeObj.getRelatedObjects(context,
												RELATIONSHIP_AFFECTED_ITEM,
												typepattern,
												selectStmts1,
												selectrelStmts1,
												false,
												true,
												(short) 1,
												null,
												null);

		if ((mlBusObjPartList == null || mlBusObjPartList.size() < 1)||(mlBusObjAIList == null || mlBusObjAIList.size() < 1))
                {
			//mlPartFinalList.add("");
				}
	    else
	    {
		if(mlBusObjAIList != null && mlBusObjAIList.size()>0)
			{
			Iterator itrAI = mlBusObjAIList.iterator();
			while(itrAI.hasNext())
				{
				Map mapAIObj = (Map)itrAI.next();
				String strChangeObjAIId = mapAIObj.get(DomainConstants.SELECT_ID).toString();
				hs.add(strChangeObjAIId);
				}
			}

		if(mlBusObjPartList != null && mlBusObjPartList.size()>0)
			{
				Iterator itrPart = mlBusObjPartList.iterator();
				while(itrPart.hasNext())
				{
					Map mapPart = (Map)itrPart.next();
					String strPartId = mapPart.get(DomainConstants.SELECT_ID).toString();
					if(!(hs.contains(strPartId)))
						mlPartFinalList.add(mapPart);
				}
			}
		}
	return mlPartFinalList;
    }


/**
	 * getRequestedChangeFilterOptions(),this method is executed to show the Requested Change Filter
	 * options in the Custom Filter Toolbar Menu in Affected Item.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap
	 * @author Sanjaya Kumar Patro
	 * @return HashMap.
     * @since Common X3
	 * @throws Exception if the operation fails.
	  */

	public static HashMap getRequestedChangeFilterOptions(Context context, String[] args) throws Exception
    {

			StringList fieldChoices 		= new StringList();
			StringList fieldDisplayChoices 	= new StringList();
			HashMap requestedChangeMap				= new HashMap();
			HashMap ProgramMap         = (HashMap)JPO.unpackArgs(args);
			HashMap ColumnMap=(HashMap)ProgramMap.get("columnMap");
			HashMap Settings          = (HashMap)ColumnMap.get("settings");
			String strAttrName		=	(String)Settings.get("Admin Type");
            Locale strLocale  = context.getLocale();
			String strAllDefaultValue=EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,"emxComponents.Filter.All.RequestedChange");
            String strAllActualValue=EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, new Locale("en"),"emxComponents.Filter.All.RequestedChange");

			if(strAttrName.startsWith("attribute_"))
			{
				strAttrName = PropertyUtil.getSchemaProperty ( context,strAttrName);
			}
			StringList slRequestedChangeFilterOptions = FrameworkUtil.getRanges(context,strAttrName);
			int Size = slRequestedChangeFilterOptions.size();
			HashMap requestMap 			= (HashMap)ProgramMap.get("requestMap");
			String strDisplay	= (String) requestMap.get("ENCAffectedItemsRequestedChangeFilter");
			String objectId=(String) requestMap.get("objectId");
			DomainObject domobj=new DomainObject(objectId);
            //IR-022930
            String strChangeState = (String)domobj.getInfo(context, DomainConstants.SELECT_CURRENT);
            

			boolean blnIsEC = false;
            //commented if condition and added new if condition for IR-035948V6R2011
			//if (domobj.isKindOf(context,DomainConstants.TYPE_ECO) || domobj.isKindOf(context,DomainConstants.TYPE_ECR))
            if (domobj.isKindOf(context,DomainConstants.TYPE_ECR))
			{
				blnIsEC = true;
			}
            
            //If the ECO is a "Team ECO" the Request For Change value "For Obsolete" will not be displayed.
            boolean blnIsTBE = false;
            String strPolicy = domobj.getInfo(context, DomainConstants.SELECT_POLICY);
            if(domobj.isKindOf(context,DomainConstants.TYPE_ECO))
            {
    			//String strPolicy = domobj.getInfo(context, DomainConstants.SELECT_POLICY);
    			String strPolicyClassification = FrameworkUtil.getPolicyClassification(context, strPolicy);
    			if("TeamCollaboration".equals(strPolicyClassification)){
    				blnIsTBE = true;
    			}
    		}
            
			if(strDisplay == null)
				strDisplay = strAllActualValue;
			fieldDisplayChoices.add(strAllDefaultValue);
			fieldChoices.add(strAllActualValue);
			for(int i=0;i<Size;i++)
			{
				String sRangeValue = (String)slRequestedChangeFilterOptions.elementAt(i);
                                // For Update is not valid option for EC
				if(blnIsEC && sRangeValue.equals(RANGE_FOR_UPDATE))
				{
					continue;
				}
				//If the ECO is a "Team ECO" the Request For Change value "For Obsolete" will not be displayed.
				if(blnIsTBE && sRangeValue.equals(RANGE_FOR_OBSOLETE))
				{
					continue;
				}
				// fieldChoices.add(sRangeValue);
				//Added to fix Bug# 351866 start
				String sRangeValuedisp = sRangeValue;
                sRangeValuedisp = StringUtils.replaceAll(sRangeValuedisp," ","");
                //Modified for IR-027704
				//sRangeValue=i18nNow.getI18nString("emxComponents.AffectedItems.Filter."+sRangeValuedisp,RESOURCE_BUNDLE_COMPONENTS_STR,strLanguage);
                sRangeValuedisp=EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,"emxComponents.AffectedItems.Filter."+sRangeValuedisp);

				//Added to fix Bug# 351866 end
				//fieldDisplayChoices.add(sRangeValue);
                fieldDisplayChoices.add(sRangeValuedisp);//for the IR-027704
				fieldChoices.add(sRangeValue);
			}
			int index = fieldChoices.indexOf(strDisplay);
			//IR-022930
			int indSize = fieldChoices.size();
			
			//TeamECO policy will not be available in OOTB schema until TBE is installed. 
			String STATE_TEAMECO_CREATE = "";
			String STATE_TEAMECO_EVALUATE = "";
			if(blnIsTBE){ //TeamECO Case
				STATE_TEAMECO_CREATE = PropertyUtil.getSchemaProperty(context, "policy", strPolicy, "state_Create");
				STATE_TEAMECO_EVALUATE = PropertyUtil.getSchemaProperty(context, "policy", strPolicy, "state_Evaluate");
			}
			
			//Modified for Request IR-142259 start
    		/*if(domobj.isKindOf(context,DomainConstants.TYPE_ECO) && 
    				!(strChangeState.equals(STATE_ECO_CREATE)||
    						strChangeState.equals(STATE_TEAMECO_CREATE)|| //TeamECO Create State
    						strChangeState.equals(STATE_TEAMECO_EVALUATE) //TeamECO Evaluate State
    				) && index != (indSize - 1)){
	               index=index+1;
	        } 
    		
			String sElementValue = (String) fieldChoices.elementAt(index);
			String sElementDisplayValue = (String) fieldDisplayChoices.elementAt(index);*/
			String symStateName=FrameworkUtil.reverseLookupStateName(context,strPolicy,strChangeState);
			String sElementValue = "";
			String sElementDisplayValue = "";

    		if(domobj.isKindOf(context,DomainConstants.TYPE_ECR)){
    			sElementValue = (String) fieldChoices.elementAt(index);
    			sElementDisplayValue = (String) fieldDisplayChoices.elementAt(index);
            }
    		else{
            	try{
    			sElementValue = EnoviaResourceBundle.getProperty(context,"emxComponents.AffectedItems.Filter.RequestChange."+symStateName);
            	}
            	catch(Exception e){
            		sElementValue = EnoviaResourceBundle.getProperty(context,"emxComponents.AffectedItems.Filter.RequestChange.Default");
            	}
            	if(!fieldChoices.contains(sElementValue))
            		sElementValue = EnoviaResourceBundle.getProperty(context,"emxComponents.AffectedItems.Filter.RequestChange.Default");
            	
    			String sElementValue1=sElementValue.replace(" ", "");
    			sElementDisplayValue =EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, strLocale,"emxComponents.AffectedItems.Filter."+sElementValue1);
    			index=getProperIndex(sElementValue, fieldChoices);    			
            }
    		//Modified for Request IR-142259 end
    		
			fieldChoices.setElementAt(fieldChoices.firstElement(), index);
			fieldChoices.setElementAt(sElementValue, 0 );
			fieldDisplayChoices.setElementAt(fieldDisplayChoices.firstElement(), index);
			fieldDisplayChoices.setElementAt(sElementDisplayValue,0);
			requestedChangeMap.put(KEY_FIELD_CHOICES, fieldChoices);
			requestedChangeMap.put(KEY_FIELD_DISPLAY_CHOICES, fieldDisplayChoices);
			return requestedChangeMap	;
    }

	/**
	 * getAssigneeFilterOptions(), Method to execute for 'Range Funtion' to populate
	 * 'Assignee' Filter Options.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap
	 * @author Sanjaya Kumar Patro
	 * @return StringList.
     * @since Common X3
	 * @throws Exception if the operation fails.
	*/
	public HashMap getAssigneeFilterOptions(Context context, String args[])
		throws Exception {
		HashMap assigneesMap				= new HashMap();
		StringList fieldChoices 		= new StringList();
		StringList fieldDisplayChoices 	= new StringList();
		HashMap paramMap 			= (HashMap)JPO.unpackArgs(args);
		HashMap requestMap 			= (HashMap)paramMap.get(SELECT_REQUEST_MAP);
		String affectedItemsAssignee = (String) requestMap.get("ENCAffectedItemsAssigneeFilter");
        String strLanguage  = context.getSession().getLanguage();
		String strAll=EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Filter.All.Assignee");
        String strAllActualValue=EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, new Locale("en"),"emxComponents.Filter.All.Assignee");
		if(affectedItemsAssignee == null)
			affectedItemsAssignee = strAllActualValue;
		fieldDisplayChoices.add(strAll);
		fieldChoices.add(strAllActualValue);
		String crId = (String) requestMap.get(SELECT_OBJECT_ID);
		setId(crId);

		String strPolicy = getInfo(context, DomainConstants.SELECT_POLICY);

		String strPolicyClassification = FrameworkUtil.getPolicyClassification(context, strPolicy);

		if ("DynamicApproval".equals(strPolicyClassification))
		{

		StringList objectSelects  = new StringList(SELECT_ID);
		objectSelects.add(SELECT_NAME);
		//Relationships are selected by its Ids
		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
		MapList assigneesList =	getRelatedObjects(context, // matrix context
										  DomainConstants.RELATIONSHIP_ASSIGNED_EC, // relationship pattern
										  DomainConstants.TYPE_PERSON, // type pattern
										  objectSelects, // object selects
										  relSelects, // relationship selects
										  true, // to direction
										  false, // from direction
										  (short) 1, // recursion level
										  DomainConstants.EMPTY_STRING,// object where clause
										  DomainConstants.EMPTY_STRING);// relationship where clause
		// Second level iteration
		Iterator itr = assigneesList.iterator();
		while(itr.hasNext()) {
			Map newMap = (Map)itr.next();
			String name = (String)newMap.get(SELECT_NAME);
			String id = (String)newMap.get(SELECT_ID);
			//fieldDisplayChoices.add(name);
			String username = com.matrixone.apps.domain.util.PersonUtil.getFullName(context, name);
			fieldDisplayChoices.add(username);
			fieldChoices.add(id);
		}
		}
		int index = fieldChoices.indexOf(affectedItemsAssignee);
		String sElementValue = (String) fieldChoices.elementAt(index);
		String sElementDisplayValue = (String) fieldDisplayChoices.elementAt(index);
		fieldChoices.setElementAt(fieldChoices.firstElement(), index);
		fieldChoices.setElementAt(sElementValue, 0 );
		fieldDisplayChoices.setElementAt(fieldDisplayChoices.firstElement(), index);
		fieldDisplayChoices.setElementAt(sElementDisplayValue,0);
		assigneesMap.put(KEY_FIELD_CHOICES, fieldChoices);
		assigneesMap.put(KEY_FIELD_DISPLAY_CHOICES, fieldDisplayChoices);
		return assigneesMap	;
   }

// Updated for Affected Items : End

	/**
     * This method is used action promote trigger on
     * different states of the policies. checks if the RouteTemplate is attached
     * for context change object or not
     *
     * @param context
     * @param args
     *            0 - String containing object id.
	 *			  1 - String Approval Or Reviewer RouteTemplate.
 	 *			  2 - String Resource file name
	 *			  3 - String Resource field key
     * @return int
     * @throws Exception if the operation fails. * *
     * @since  Common X3.
     */
    public int checkRouteTemplateForState(Context context, String[] args)
            throws Exception {

        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }
        String objectId = args[0];// Change Object Id
        String strApprovalOrReviewerRouteTemplate = args[1];

        MapList mapRouteTemplate = new MapList();

        try {
            // create change object with the context Object Id
            setId(objectId);
            StringList selectStmts = new StringList(1);
            selectStmts.addElement("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");

            String whrClause = "attribute["
                  + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE + "] match '"
                  + strApprovalOrReviewerRouteTemplate + "' && current == Active";

            // get route template objects from change object
            mapRouteTemplate = getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_OBJECT_ROUTE, DomainConstants.TYPE_ROUTE_TEMPLATE,
                    selectStmts, null, false, true, (short) 1, whrClause, null);

            Iterator mapItr = mapRouteTemplate.iterator();
            if (mapItr.hasNext()) {
                return 0; // returns true if there is any route template
                            // objects connected with change object
            } else {
				emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context, args[2],context.getLocale(), args[3]));
                return 1;
            }

        } catch (Exception ex) {
            System.out.println(ex);
            return 1;// return false
        }

    }


 /**
     * this method checks for Route object(connected to changeobject) and if not
     * present cals the method createRoute which creates route from route
     * template and attaches to change object
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args:
     *            0 - OBJECTID change id
     *            1 - change object policy
     *            2 - change
     *            object state (from state ie that is the state on which the
     *            route will be started)
     *            3 - the The kind of Route ( Route Base
     *            Purpose ) can be Approval,Review,Standard
     * @returns true for sucess and false for trigger failure
     * @throws Exception
     *             if the operation fails
     * @since Common X3
     */
   public int createRouteFromRouteTemplate(Context context, String[] args)
            throws Exception {

        String changeObjId = args[0]; // change Object
        String strApprovalOrReviewerRoute = args[3];
        try {
            SelectList objectSelects = new SelectList(2);
            StringList sListRoutes = new StringList();
            boolean boolRoutePresent = false;
            MapList mpListRoutes = null;
            MapList listRoutes = null;
            MapList listTemplates = null;

            DomainObject obj = (DomainObject) DomainObject.newInstance(context);
            obj.setId(changeObjId);// change object

            objectSelects.add(DomainConstants.SELECT_ID);
            objectSelects.add(DomainConstants.SELECT_NAME);

            // get Routes Connected to change object
			ContextUtil.pushContext(context);
            mpListRoutes = obj.getRelatedObjects(context,
                    RELATIONSHIP_OBJECT_ROUTE, TYPE_ROUTE, objectSelects, null,
                    true, true, (short) 1, null, null);
			ContextUtil.popContext(context);
            // if route present get the ids of routes in stringList
            if (mpListRoutes.size() > 0) {

                Iterator itr = mpListRoutes.iterator();
                while (itr.hasNext()) {
                    Map mpRoutes = (Map) itr.next();

                    String strRouteID = (String) mpRoutes.get(SELECT_ID);
                    sListRoutes.addElement(strRouteID);
                }
                // get Route Templates connected to Change Object
                // where clause to filter out Aproval or Reviewal Route
                String whrClause = "attribute["
                        + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE
                        + "] match '" + strApprovalOrReviewerRoute + "'";

                // get list of Templates attached to change object
                listTemplates = obj.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                        TYPE_ROUTE_TEMPLATE, objectSelects, null, false, true,
                        (short) 1,whrClause,null);

                if (listTemplates.size() > 0 && (!boolRoutePresent)) {
                    Iterator itrTemplates = listTemplates.iterator();
                    while (itrTemplates.hasNext() && (!boolRoutePresent)) {
                        Map mpTemplates = (Map) itrTemplates.next();
                        String strTemplaeID = (String) mpTemplates
                                .get(SELECT_ID);
                        DomainObject objTemplate = (DomainObject) DomainObject
                                .newInstance(context);
                        objTemplate.setId(strTemplaeID);// Template object
                        // get list of routes connected to Route Template
						ContextUtil.pushContext(context);
                        listRoutes = objTemplate.getRelatedObjects(context,
                                RELATIONSHIP_INITIATING_ROUTE_TEMPLATE,
                                TYPE_ROUTE, objectSelects, null, true, false,
                                (short) 1, null, null);
						ContextUtil.popContext(context);
                        if (listRoutes.size() > 0 && (!boolRoutePresent)) {
                            Iterator itrRoute = listRoutes.iterator();
                            while (itrRoute.hasNext() && (!boolRoutePresent)) {
                                Map mpRoutesOfTemplate = (Map) itrRoute.next();
                                String strId = (String) mpRoutesOfTemplate
                                        .get(SELECT_ID);// route id connected to
                                                        // Template
                                // check if route ids matches
                                if (sListRoutes.contains(strId)) {
                                    boolRoutePresent = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (!boolRoutePresent) {

                return (createRoute(context, args));
            }
        } catch (Exception e) {
            System.out.println("Exception :::: " + e);
            throw e;
        }
        return 0;
    }


    /**
     * this method creates route from route template and
     * attaches to change object
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args:
     *            0 - OBJECTID change id
     *            1 - change object policy
     *            2 - change object state (from state ie that is the state
     *            on which the route will be started)
     *            3 - the The kind of Route Template( Route Base
     *            Purpose ) can be Approval,Review,Standard
     * @returns true for sucess and false for trigger failure
     * @throws Exception
     *             if the operation fails
     * @since Common X3
     */
    public int createRoute(Context context, String[] args)
            throws Exception {

        String changeObjId = args[0]; // change Object
        String strChangeObjectPolicy = PropertyUtil.getSchemaProperty(context,args[1]);
        String strState1 = PropertyUtil.getSchemaProperty(context,"policy",
                                strChangeObjectPolicy, args[2]);
        String strApprovalOrReviewerRoute = args[3];

        //initialize local variabls
        String templateId = ""; // routeTemplate ID
        String sTemplateDesc = ""; // template description
        String sState = ""; // current state
        Route routeObj = null; // object route

        String strObjOwner = null;
		String strAutoStopOnRejection=""; //IR-118894
        // get Alias Names
        String RoutePolicyAdminAlias = FrameworkUtil.getAliasForAdmin(context,
                DomainObject.SELECT_POLICY, DomainObject.POLICY_ROUTE, true);
        String RouteTypeAdminAlias = FrameworkUtil.getAliasForAdmin(context,
                DomainObject.SELECT_TYPE, DomainObject.TYPE_ROUTE, true);

        String sAttrRouteBasePurpose = PropertyUtil.getSchemaProperty(context,
                "attribute_RouteBasePurpose");
     //IR-118894 starts
        
        String sAttrRestartUponTaskRejection = PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection" ); 
        String SELECT_ATTRIBUTE_AUTO_STOP_REJECTION  = "attribute[" + sAttrRestartUponTaskRejection + "]"; 
        
      //IR-118894 ends
        String sAttrRouteCompletionAction  =PropertyUtil.getSchemaProperty(context,
        "attribute_RouteCompletionAction");

        // add object selects
        SelectList objectSelects = new SelectList(1);
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(RouteTemplate.SELECT_DESCRIPTION);
        objectSelects.add(RouteTemplate.SELECT_CURRENT);
        objectSelects.add(SELECT_ATTRIBUTE_AUTO_STOP_REJECTION); //IR-118894
        objectSelects.add("attribute["
                + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE + "]");
        // add relationship selects
        SelectList relSelects = new SelectList(1);
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        // where clause to filter out Aproval or Reviewal Route
        String whrClause = "attribute["
                + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE + "] match '"
                + strApprovalOrReviewerRoute + "'";
        //375788

        String stateActive  = DomainConstants.STATE_ROUTE_TEMPLATE_ACTIVE;

         whrClause = whrClause + " && (latest == 'true') && (current == '"+stateActive+"')";
        //End
        MapList list = null; // Map List of Templates attached to change object
        try {
            DomainObject obj = (DomainObject) DomainObject.newInstance(context);
            obj.setId(changeObjId);//change object
            // get list of Templates attached to change object
			strObjOwner = obj.getOwner(context).getName();
            list = obj.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
                    TYPE_ROUTE_TEMPLATE, objectSelects, relSelects, false,
                    true, (short) 1, whrClause, null);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                Map mapTemplate = (Map) itr.next();// Template Map
                templateId = (String) mapTemplate
                        .get(DomainConstants.SELECT_ID);
                sTemplateDesc = (String) mapTemplate
                        .get(DomainConstants.SELECT_DESCRIPTION);
                sState = (String) mapTemplate
                        .get(DomainConstants.SELECT_CURRENT);
                strAutoStopOnRejection=(String) mapTemplate
                .get(SELECT_ATTRIBUTE_AUTO_STOP_REJECTION);   //IR-118894 

                if (templateId != null && !"null".equals(templateId)
                        && "Active".equals(sState)) {

                    String sProductionVault = PropertyUtil
                            .getSchemaProperty(context,"vault_eServiceProduction");
                    // get Route Object id
                    String sRouteId = FrameworkUtil.autoName(context,
                            RouteTypeAdminAlias, "", RoutePolicyAdminAlias,
                            sProductionVault);
                    routeObj = (Route) DomainObject.newInstance(context,
                            TYPE_ROUTE);
                    // create ROute object
                    routeObj.setId(sRouteId);
                    // HashMap to carry all the attribute values to be set
                    HashMap attrMap = new HashMap();
                    // rename change object object name
                    String changeObjectName = obj.getInfo(context,
                            DomainConstants.SELECT_NAME);
                    String strRouteName = routeObj.getInfo(context,
                            DomainConstants.SELECT_NAME);
                    strRouteName = "Route_"+ strRouteName + "_" + changeObjectName + "_"+ strState1;
                    // set new name to Route object
                    routeObj.setName(context, strRouteName);
                    // Set attribute values in the map
                    attrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE,
                            args[2]);
                    attrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_POLICY,
                            args[1]);
                    attrMap.put(sAttrRouteBasePurpose, "Standard");

                    routeObj.open(context);
                    routeObj.setDescription(context, sTemplateDesc);
                    //set Route Completion Action
                    routeObj.setAttributeValue(context,sAttrRouteCompletionAction,"Promote Connected Object");
                    routeObj.setAttributeValue(context,sAttrRestartUponTaskRejection,strAutoStopOnRejection); //IR-118894 
                    // Add contents to the Route i.e. ECO
                    RelationshipType relationshipType = new RelationshipType(
                            DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
                    DomainRelationship newRel = routeObj.addFromObject(context,
                            relationshipType, changeObjId);
                    // connect to Route Template
                    routeObj.connectTemplate(context, templateId);
                    // add member list from template to route
                    routeObj.addMembersFromTemplate(context, templateId);
					
					// Grant the access to the content object if any
                    if (UIUtil.isNotNullAndNotEmpty(changeObjId)) {
                    	String[] contentIdArray = new String []{changeObjId};
                    	String ObjectId[] = new String[1];
                    	ObjectId[0]= sRouteId;
                    	try {
                            JPO.invoke(context, "emxRoute", ObjectId, "inheritAccesstoContent", contentIdArray);
                        } catch(Exception exp) {
                            throw(exp);
                        }
                   }
					
                    // UPDATE OBJECT ROUTE ATTRIBUTES.
                    newRel.setAttributeValues(context, attrMap);
                    // Change Route Action Attribute to Approve
                    SelectList relProductSelects = new SelectList(1);
                    relProductSelects
                            .add(DomainConstants.SELECT_RELATIONSHIP_ID);
                    // 369474
                    String nodeTypePattern = DomainConstants.TYPE_PERSON + SYMB_COMMA + DomainConstants.TYPE_ROUTE_TASK_USER;
                    MapList routeNodeList = null;
                    DomainObject Domobj = (DomainObject) DomainObject
                            .newInstance(context);
                    Domobj.setId(sRouteId);
                    // get all the tasks in the route
                    routeNodeList = Domobj.getRelatedObjects(context,
                            DomainConstants.RELATIONSHIP_ROUTE_NODE,
                            //DomainConstants.TYPE_PERSON,   // 369474
                            nodeTypePattern, null,
                            relProductSelects, false, true, (short) 1, null,
                            null);
                    Iterator itrRouteNodeList = routeNodeList.iterator();
                    while (itrRouteNodeList.hasNext()) {
                        Map currentMap = (Map) itrRouteNodeList.next();
                        DomainRelationship routeNodeId = new DomainRelationship(
                                (String) currentMap
                                        .get(DomainConstants.SELECT_RELATIONSHIP_ID));
                        //attribute map for Route node relationship
                        HashMap attributeMap = new HashMap();
                        attributeMap.put(
                                DomainConstants.ATTRIBUTE_ROUTE_ACTION,
                                "Approve");
                        routeNodeId.setAttributeValues(context, attributeMap);
                    }
		// auto start route
	         startRoute(context, sRouteId);
					if ("User Agent".equals(Domobj.getOwner(context).getName()))
					{
						if (strObjOwner != null)
						{
							Domobj.changeOwner(context, strObjOwner);
						}
					}
                }
            }

        } catch (Exception e) {
            System.out.println("Exception :::: " + e);
            throw e;
        }
        return 0;
    }
     /**
     * Reset Owner on demotion of ChangeObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * 0 - String holding the object id.
     * @returns void.
     * @throws Exception if the operation fails
     * @since EC 10.5
     */
    public void resetOwner(Context context, String[] args)
        throws Exception
    {
        try
        {
            String objectId = args[0];//changeObject ID
            setObjectId(objectId);
            String strCurrentState = args[1]; //current state of ChangeObject

            String promotedBy = null; //previousstate owner
            StringList historyData = getHistory(context);
            for(int i=historyData.size()-1;i>=0;i--){
                String historyRecord = ((String)historyData.elementAt(i)).trim();
                if(historyRecord.startsWith("promote") && historyRecord.endsWith(strCurrentState)){
                    promotedBy = historyRecord.substring(historyRecord.indexOf("user:")+6, historyRecord.indexOf("time:")-2).trim();
                    break;
                }
            }

            if(promotedBy != null){
                setOwner(context, promotedBy); //reset owner on previous state
            }

        }catch (Exception ex)
        {
           ex.printStackTrace();
           throw ex;
        }
    }

		// Updates for EC Assignee Start
/**
 * Gets all the persons with which this Change object is connected with
 * Assigned EC relationship
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args    holds the following input arguments:
 *           0 -  HashMap containing one String entry for key "objectId"
 * @return        a <code>MapList</code> object having the list of Assignees,their relIds and rel names for this Change Object
 * @throws        Exception if the operation fails
 * @since         Common X3
 **
 */

@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getAssignees(Context context, String[] args)
	throws Exception
	{
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		//getting parent object Id from args
		String strParentId = (String)programMap.get(SELECT_OBJECT_ID);
		//getting the symbolic relationship from PropertyUtil
		String strRelAssignedEC= DomainConstants.RELATIONSHIP_ASSIGNED_EC;
		//Initializing the return type
		MapList relBusObjPageList = new MapList();
		//Business Objects are selected by its Ids
		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		//Relationships are selected by its Ids
		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
		//retrieving Assignees from EC Context
		this.setId(strParentId);
		relBusObjPageList = getRelatedObjects(context,
											  strRelAssignedEC,
											  DomainConstants.QUERY_WILDCARD,
											  objectSelects,
											  relSelects,
											  true,
											  false,
											  (short) 1,
											  DomainConstants.EMPTY_STRING,
											  DomainConstants.EMPTY_STRING);


		return  relBusObjPageList;
	}

    // Updates for ENC Assignee Start
   /**
    * Checks if the any object is connected to the context change object with AssignedAffectedItem Relationship
    * @param context the eMatrix <code>Context</code> object
    * @param args    holds the following input arguments:
    *           0 -  Context
    *           1 -  A String containg the Object Id
    *           2 -  A String Array containg the Rel Ids of Objects Selected
    *           3 -  A String Array containg the Obj Ids of Objects Selected
    *           4 -  A String containing the Change Obj Id.
    * @return        StringList of Person objects which is a combination of Stringlist of "objects which can be removed" and the "objects which have affected Items" connected to them
    * @throws        Exception if the operation fails
    * @since         Common X3
    * @deprecated since V6R2017x for Function_FUN059293 and replaced by {@link ${CLASS:enoEngChange}#checkAssignedAffectedItemRelExists(Context context, String objectId, String[] arrRelIds, String[] strObjectId)}
    */
	
   public StringList checkAssignedAffectedItemRelExists(Context context, String objectId, String[] arrRelIds, String[] strObjectId) throws Exception
   	{

		//getting the String Array of Rel Ids to be disconnected
   		String[] arrRelationIds = arrRelIds;

   		// Initializing the Stringlist for the Objects which can't be removed
   		StringList strAssigneeRetain = new StringList(arrRelationIds.length);

   		// Initializing  Lists for the name of the Objects to be removed and retained.
   		StringList strAssigneeName = new StringList(arrRelationIds.length);

   		int iDeleteCount = 0;

		// Initializing Strings for MQL Commands and its Result
   		String strMQLCommand ;
   		String strMQLCommandResult =  "";

   		// Declaring a new MQL Command
   		MQLCommand prMQL  = new MQLCommand();
   		prMQL.open(context);

		// initializing a list for the person objects which need to be deleted.
   		StringList strAssigneeDeleteList = new StringList(arrRelationIds.length);

   		for (int i=0;i< arrRelationIds.length ;i++ )
   		{
			// The MQL Command
   			strMQLCommand = "print connection $1 select $2 dump";

   			// Executing the MQL Command
   			prMQL.executeCommand(context,strMQLCommand, arrRelationIds[i], "frommid.id");

   			// getting the MQL Command result
   			strMQLCommandResult = prMQL.getResult();

			// Conditional Check on the Output received out of the MQL Command.
   			if(strMQLCommandResult.equals("\n"))
   			{
				strAssigneeDeleteList.add(iDeleteCount, arrRelationIds[i]);
   					iDeleteCount = iDeleteCount+1;

   			}
   			else
   			{
   				strAssigneeRetain.addElement(arrRelationIds[i]);
			}

  		}
   		prMQL.close(context);

   		for (int i = 0;i<strAssigneeRetain.size();i++ )
   		{
   			//getting the name of the Persons being retained for Returning purposes
   			String relIds[] = new String[1];
   			relIds[0] = (String)strAssigneeRetain.get(i);
   			StringList slSelects = new StringList();
   			slSelects.addElement("from.name");

   			DomainRelationship domrRetainAssignee = new DomainRelationship(relIds[0]);
   			MapList mlList = domrRetainAssignee.getInfo(context,relIds , slSelects);
   			String sName = "";
   			if(!mlList.isEmpty())
   			{
   				Map mName = (Map)(mlList.get(0));
   				sName = (String)mName.get("from.name");
   				strAssigneeName.addElement(sName);
   			}
   		}

		// A Combined StringList to return the List of Rel Ids to be deleted and the Names of the Persons to be retained.
		StringList strConsolidatedList = new StringList(arrRelationIds.length);
		strConsolidatedList.add(0,strAssigneeDeleteList);
		strConsolidatedList.add(1,strAssigneeName);

		return strConsolidatedList;
	}

	/**
	         * Checks if a certain List of Assignees is connected to the context change object with Assigned EC Relationship.
	         * @param context the eMatrix <code>Context</code> object
	         * @param String Array of Relationship Ids
             * @param String Array of Object Ids
             * @param parent Object ID
	         * @return        an Array of Rel Ids which are not connected to the Parent Object ID
	         * @throws        Exception if the operation fails
	         * @since         Common X3
	         **
	         */

	            public String[] checkAssignedECRelExists(Context context, String RelIds[], String ObjectIds[], String ObjectID) throws Exception
	            {

    	            //getting the symbolic relationship from PropertyUtil
    	            String strRelAssignedEC= DomainConstants.RELATIONSHIP_ASSIGNED_EC; // Currently checking with the existing rel. Later to be plugged with the Actual rel.
    	            //getting the user name of the logged in user
    	            //String strContextUser= context.getUser();
                    //Initializing the return type
    	            MapList relBusObjPageList = new MapList();
    	            //Business Objects are selected by its Ids
    	            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    	            //Relationships are selected by its Ids
    	            StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    	            //retrieving Assignees from EC Context
    	            this.setId(ObjectID);
    	            relBusObjPageList = getRelatedObjects(context,
    	                                                  strRelAssignedEC,
    	                                                  DomainConstants.QUERY_WILDCARD,
    	                                                  objectSelects,
    	                                                  relSelects,
    	                                                  true,
    	                                                  false,
    	                                                  (short) 1,
    	                                                  DomainConstants.EMPTY_STRING,
    	                                                  DomainConstants.EMPTY_STRING);


                    // Iterator for Iterating through the Maps of the Maplist "relBusObjPageList"
                    java.util.Iterator itr = relBusObjPageList.iterator();
                    // Arrays of Relationship Ids which are connected to the
                    String[] strRelIdsRetain = new String[RelIds.length]; // array of string Ids which need to be deleted
                    String[] strRelIdsDelete = new String[RelIds.length]; // array if String Ids which cannot be deleted
                    while(itr.hasNext())
                    {
                        // Iterator for iterating through the keys of the Map
                        Map mAtrribMap = (Map)itr.next();
                        //getting the relationship ID from the map "relBusObjPageList" for comparison
                        String strRelIDCompare = (String)mAtrribMap.get("RelIds");
                        for(int i=0; i< RelIds.length; i++)
                        {
                            if(strRelIDCompare.equals(RelIds[i])||strRelIDCompare.equalsIgnoreCase(RelIds[i]))
                            {
                                int iRetainCount = 0;
                                strRelIdsRetain[iRetainCount]= RelIds[i];
                                iRetainCount++ ;
                            }
                            else
                            {
                                int iDeleteCount = 0;
                                strRelIdsDelete[iDeleteCount]= RelIds[i];
                                iDeleteCount++ ;
                            }
                        }

                    }
                    return strRelIdsDelete;
	            }

   /**
     * Checks if the Logged in User should be able to see the Delegate Assignment Action Command or not.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  HashMap containing object id
     * @return        a <code>Boolean</code> object representing boolean true or false depending on whether to enable / disalbe link
     * @throws        Exception if the operation fails
     * @since         Common X3
     **
     */

    public boolean showShowDelegateAssignmentActionsLink(Context context, String[] args) throws Exception
			{
            //unpacking the arguments from variable args
            HashMap programMap           = (HashMap)JPO.unpackArgs(args);

            //getting parent object Id from args
            String strObjectId           = (String)programMap.get(SELECT_OBJECT_ID);
            strObjectId = strObjectId.trim();
            this.setId(strObjectId);

			//Boolean flag to show or hide the Delegate Assignment Action Link.
			boolean showLink = false;

            MapList mapAssigneesList = new MapList();
		    String strAssigneeArray[] = {strObjectId};
            mapAssigneesList = getAssignees(context,strAssigneeArray);

                if(mapAssigneesList.contains(context.getUser()))
                    {
                    showLink = true;
                    }
				else {
                    showLink = false;
                     }
                return showLink;
             }


 // Updates for Assignees End.


 /**
   * Updates the Distribution List field in ECR WebForm.
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   * object Id object Id of context ECR.
   * New Value object Id of updated Distribution List Object
   * @throws Exception if the operations fails
   * @since Common X3.
*/
       public void connectDistributionList (Context context, String[] args) throws Exception {
           try{
               //unpacking the Arguments from variable args
               HashMap programMap = (HashMap)JPO.unpackArgs(args);
               HashMap paramMap = (HashMap)programMap.get("paramMap");
               //Relationship name
			   String strRelationship = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");
			   //Calling the common connect method to connect objects
               connect(context,paramMap,strRelationship);

              }
           catch(Exception ex){
               throw  new FrameworkException((String)ex.getMessage());
           }

     }


/**
     * Updates the Reported Against field in ECR WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECR object Id
     * New Value holds the newly selected Reported Against Object Id
     * @throws Exception if the operations fails
     * @since Common X3

*/
    public void connectReportedAgainstChange (Context context, String[] args) throws Exception {
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");

            //Relationship name
            String strRelationship = PropertyUtil.getSchemaProperty(context,"relationship_ReportedAgainstChange");
            //Calling the common connect method to connect objects
            connect(context,paramMap,strRelationship);


        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
    }

 /**
* Updates the Review list field values in ECR WebForm.
* @param context the eMatrix <code>Context</code> object
* @param args holds a Map with the following input arguments:
* objectId objectId of the context Engineering Change
* New Value objectId of updated Review List value
* @throws Exception if the operations fails
* @since Common X3
*/
    public void connectApproverReviewerList (Context context, String[] args) throws Exception {
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
            String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            //Calling the common connect method to connect objects
			String strNewValue = (String)paramMap.get("New OID");
//			String strObjectId = (String)paramMap.get("objectId");
			DomainRelationship drship=null;
//			DomainRelationship newRelationship=null;



if (strNewValue == null || "".equals(strNewValue) || "Unassigned".equalsIgnoreCase(strNewValue) || "null".equalsIgnoreCase(strNewValue) || " ".equals(strNewValue))
			{

							strNewValue = (String)paramMap.get("New Value");
			}




			if((strNewValue != null) && !(strNewValue.equals("")) ||  "Unassigned".equalsIgnoreCase(strNewValue) || "null".equalsIgnoreCase(strNewValue)) {
//			DomainObject domObjectChangeNew =  new DomainObject(strObjectId);
			DomainObject newValue =  new DomainObject(strNewValue);
			String strAttribute = newValue.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);

			if(strAttribute.equals("Review")){
			drship = connect(context,paramMap,strRelationship);

			//	new DomainRelationship(newRelationship.connect(context,domObjectChangeNew,strRelationship,newValue)) ;
			drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
			}
			if(strAttribute.equals("Approval")){
			drship = connect(context,paramMap,strRelationship);
			//drship = new DomainRelationship(newRelationship.connect(context,domObjectChangeNew,strRelationship,newValue)) ;
			drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
			}
			}
           }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
          }
    }


 /**
* Updates the Review list field values in ECR WebForm.
   * @param context the eMatrix <code>Context</code> object
* @param args holds a Map with the following input arguments:
* objectId objectId of the context Engineering Change
* New Value objectId of updated Review List value
   * @throws Exception if the operations fails
   * @since Common X3.
*/
    public void connectAssignMoveReviewerList (Context context, String[] args) throws Exception {
		 try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
			HashMap requestMap= (HashMap)programMap.get("requestMap");
            String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
//			String strObjectId = (String)paramMap.get("objectId");
//			String[] strModCreate= (String[])requestMap.get("CreateMode");
//			String str = strModCreate[0];

			String[] strMod= (String[])requestMap.get("CreateMode");
			String strMode = strMod[0];
			if((strMode!=null && strMode.equalsIgnoreCase("AssignToECO"))||(strMode!=null && strMode.equalsIgnoreCase("MoveToECO")))
			{
			// Creating DomainObject of new ECO
				DomainRelationship drship=null;
//				DomainRelationship newRelationship=null;
//				DomainObject domObjectChangeNew =  new DomainObject(strObjectId);
				String strNewValue = (String)paramMap.get("New Value");
//				DomainObject domainNewValue = new DomainObject(strNewValue);

			if((strNewValue.equals("null"))||(strNewValue.equals(""))){
			}else{
//			String strOldToTypeObjId = (String)paramMap.get("Old OID");


			DomainObject newValue =  new DomainObject(strNewValue);
			String strAttribute = newValue.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
			if(strAttribute.equals("Review")){

			//drship = new DomainRelationship(newRelationship.connect(context,domObjectChangeNew,strRelationship,domainNewValue)) ;
			drship = connect(context,paramMap,strRelationship);
			drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
			}
			if(strAttribute.equals("Approval")){
			//drship = new DomainRelationship(newRelationship.connect(context,domObjectChangeNew,strRelationship,domainNewValue)) ;
			drship = connect(context,paramMap,strRelationship);
			drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
			}
			}

			}
           }catch(Exception ex){

            throw  new FrameworkException((String)ex.getMessage());
          }
    }




    /**
   * Connects ECR/ECO with the Passed Object.
   * @param context the eMatrix <code>Context</code> object
   * @param Hashmap holds the input arguments:
   * strRelationship holds relationship with which ECR will be connected
   * New Value is object Id of updated Object
   * @throws Exception if the operations fails
   * @since Common X3.
*/

    public DomainRelationship connect(Context context , HashMap paramMap ,String strRelationship)throws Exception {
		 try{
			 DomainRelationship drship=null;
						//Relationship name
		 				DomainObject oldListObject = null;
		 				DomainObject newListObject = null;
		                //Getting the ECR Object id and the new MemberList object id
		                String strChangeobjectId = (String)paramMap.get("objectId");
		                DomainObject changeObj =  new DomainObject(strChangeobjectId);
						//for bug 343816 and 343817 starts
						String strNewToTypeObjId = (String)paramMap.get("New OID");

		                if (strNewToTypeObjId == null || "null".equals(strNewToTypeObjId) || strNewToTypeObjId.length() <= 0 
                                || "Unassigned".equals(strNewToTypeObjId)) {
		                    strNewToTypeObjId = (String)paramMap.get("New Value");
						}
						//for bug 343816 and 343817 ends
		                String strOldToTypeObjId = (String)paramMap.get("Old OID");
		                try {
		                	ContextUtil.pushContext(context);
		                	DomainRelationship newRelationship=null;
		                	RelationshipType relType = new RelationshipType(strRelationship);
		                	if (strOldToTypeObjId != null && !"null".equals(strOldToTypeObjId) && strOldToTypeObjId.length() > 0 
		                			&& !"Unassigned".equals(strOldToTypeObjId)) {
		                		oldListObject = new DomainObject(strOldToTypeObjId);
		                		changeObj.disconnect(context,relType,true,oldListObject);
		                	}

		                	if(strNewToTypeObjId != null && !"null".equals(strNewToTypeObjId) && strNewToTypeObjId.length() > 0 
		                			&& !"Unassigned".equals(strNewToTypeObjId)) {
		                		newListObject = new DomainObject(strNewToTypeObjId);
		                		drship = new DomainRelationship(newRelationship.connect(context,changeObj,relType,newListObject)) ;
		                	}
		                } 
		                catch(Exception ex){
		                	//ex.printStackTrace();
		                }
		                finally{
		                	ContextUtil.popContext(context);
		                }
                        return drship;
         } catch(Exception ex){
             throw  new FrameworkException((String)ex.getMessage());
         }

    }



/* This  method gets the Value of the default RCO connected to the ECR
 * or the value of default connected RDO to the ECO.
 * If no RCO or RDO are connected then return
 * @param context The ematrix context of the request.
 * @param args This string array contains following arguments:
 *          0 - The object id of Change Object
 * @throws Exception
 * @throws FrameworkException
 * @since Common X3
 */


public String getResponsibleOrganization(Context context, String[] args)
	throws Exception
	{
		//unpacking the arguments from variable args
		HashMap programMap           = (HashMap)JPO.unpackArgs(args);

		//getting parent object Id from args
		String strObjectId           = (String)programMap.get("objectId");
		strObjectId = strObjectId.trim();

		this.setId(strObjectId);

		String strContextUser = context.getUser();

		BusinessObject boObjectType = new BusinessObject(strObjectId);
        boObjectType.open(context);


		if (boObjectType.getTypeName().equalsIgnoreCase(DomainConstants.TYPE_ECO))
		{
			String strMQLCommand ;
			String strMQLCommandResult =  "";

			MQLCommand prMQL  = new MQLCommand();
			prMQL.open(context);

			strMQLCommand = "print Person $1 select $2 ";

     		prMQL.executeCommand(context,strMQLCommand, strContextUser, "property["+strPref_DesignResp+"].value");

			strMQLCommandResult = prMQL.getResult();


			return strMQLCommandResult;
		}
		else
		{
			if (boObjectType.getTypeName().equalsIgnoreCase(DomainConstants.TYPE_ECR))
			{

				StringList busSelects = new StringList(DomainConstants.SELECT_ID);
				StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

				Map relBusObjPageList = getRelatedObject(context,
														  RELATIONSHIP_DESIGN_RESPONSIBILITY, // Dummy Value for the time being
														  true,
														  busSelects,
														  relSelects);

				String strUserName = (String)relBusObjPageList.get("name");

				return strUserName;

			}
			else
			{
				String strHostCompanyID = Person.getPerson(context).getCompanyId(context);
				String strHostCompanyName = com.matrixone.apps.domain.util.PersonUtil.getCompanyNameFromCompanyID(context,strHostCompanyID);
				return strHostCompanyName;
			}
		}
	}

// This  method Disconnects the assignees selected on the Assignees List Page from the Change Object.
 /** @param context The ematrix context of the request.
 * @param args This string array contains following arguments:
 *          0 - context
 *          1 - A String Array of Object Ids of Assignees to be removed
 *          2 - A String Array of relationship Ids of Assignees with the context object.
 *          3 - A String Array of Change Object Id
 * @return a StringList of Person Objects which cannot be removed
 * @throws Exception
 * @throws FrameworkException
 * @since Common X3
 * @deprecated since V6R2017x for Function_FUN059293 and replaced by {@link ${CLASS:enoEngChange}#removeAssignee(Context context, String[] args)}
 */
 public StringList removeAssignee(Context context, String[] args)
        throws FrameworkException
    {

			// Initialing a StringList for the Objects which cannot be removed.
			StringList strCombinedList = new StringList();
			StringList strAssigneeRetain = new StringList();
			StringList strAssigneeDelete = new StringList();
	        try
	        {
				//  unpacking the Arguments from variable args
				HashMap programMap = (HashMap)JPO.unpackArgs(args);

				//getting the String Array of Rel Ids to be disconnected
				String[] arrRelIds = (String[])programMap.get("arrRelIds[]");

				//getting the String Array of object Ids to be removed
				String[] strObjectIds = (String[])programMap.get("strObjectIds[]");

				//getting the String of object Id of the parent object.This will be used in check condition of Affected Item.
				String strChangeObjectId = (String)programMap.get("objectId");

				String strContextId = PersonUtil.getPersonObjectID(context);

				//Initialing the for loop over the list of object Ids to be removed
				for(int i = 0; i < strObjectIds.length; i++)
				{					
					String strObjectId = (String) FrameworkUtil.split(strObjectIds[i], "|").get(0);
					//String strAssigneeRemove = UINavigatorUtil.getI18nString("emxComponents.Common.Message.strAssigneeRemove", RESOURCE_BUNDLE_COMPONENTS_STR, (String)programMap.get("languageStr"));
					String strAssigneeRemove = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Common.Message.strAssigneeRemove");
					if(strContextId.equals(strObjectId))
					{
						emxContextUtil_mxJPO.mqlNotice(context,strAssigneeRemove);
						throw new FrameworkException(strAssigneeRemove);
				}
				}

				//getting the combined list of Rel Ids and Assignee Retaied Names.
				strCombinedList = checkAssignedAffectedItemRelExists(context, strChangeObjectId,arrRelIds, strObjectIds);

				strAssigneeDelete = (StringList)strCombinedList.get(0);
				strAssigneeRetain = (StringList)strCombinedList.get(1);

				int iAssigneeDeleteSize= strAssigneeDelete.size();
				if(iAssigneeDeleteSize != 0)
				{
			        Iterator PersonItr = strAssigneeDelete.iterator();
					while(PersonItr.hasNext())
					{
				        String strRelId = (String)PersonItr.next();
						DomainRelationship.disconnect(context,strRelId);
					}
				}
	        }
	        catch (Exception ex)
	        {
				ex.printStackTrace(System.out);
	        }

	         //returning the list of Objects' Names which cannot be removed.
	         return strAssigneeRetain;
}
 
 /** In some scenrio DomainObject.getInfo method returns String OR StringList dynamically on selectable, This method is used to get StringList in such scerios.   
 * @param map contains selected data using domainobject getInfo method.
 * @param key contains key for accessing the data from the map
 * @return StringList.
 */
private StringList getSelectedListFromGetInfoMap(Map map, String key) {
	 Object obj = map.get(key);
	 if (obj == null)
		 return new StringList(0);
	 return (obj instanceof String) ? new StringList((String) obj) : (StringList) obj;
 }


	/**
     * this method gets the List of Affected Items. Gets the List
     * of Persons to be Notified. The following steps are performed: - The
     * Affected Parts,Specifications Connected to this Pariticular ECO are
     * Disconnected. - The related ECRs are disconnected - The Related Routes
     * Connected to this Particualr ECO are Disconnected
     *
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the
     *            Change Process
     * @throws Exception
     *             if the operation fails.
     * @since Common X3.
     * @deprecated since V6R2017x for Function_FUN059293 and replaced by {@link ${CLASS:enoEngChange}#cancelChangeProcess(Context context, String[] args)}
     */

public void cancelChangeProcess(Context context, String[] args)
 throws Exception // MethodName
{

HashMap paramMap = (HashMap) JPO.unpackArgs(args);
String objectId = (String) paramMap.get("objectId");
String sReason = (String) paramMap.get("sReason");
String sDeleteAffectedItems = (String) paramMap.get("sDeleteAffectedItems");
String sDisconnectECRs = (String) paramMap.get("sDisconnectECRs");

boolean deleteAffectedItems = "true".equals(sDeleteAffectedItems);
boolean disconnectECRs = "true".equals(sDisconnectECRs);

DomainObject changeObj = DomainObject.newInstance(context, objectId,
     DomainConstants.ENGINEERING);
String ATTRIBUTE_BRANCH_TO = PropertyUtil.getSchemaProperty(context,"attribute_BranchTo");
/** attribute "Reason For Cancel". */
String ATTRIBUTE_REASON_FOR_CANCEL = PropertyUtil.getSchemaProperty(context,"attribute_ReasonForCancel");
/** Person Corporate */
String PERSON_CORPORATE = PropertyUtil.getSchemaProperty(context,"person_Corporate");

String sOwner = null;
boolean isContextPushed = false;

try {

    //PDCM Check for the sub changes
    boolean isECHInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionEnterpriseChange",false,null,null);
    if(isECHInstalled) {
        Boolean canCancel = (Boolean)JPO.invoke(context, "emxChangeTask", null, "canCancelChange", JPO.packArgs(paramMap), Boolean.class);
        if (!canCancel.booleanValue()) {
            String strNotice = EnoviaResourceBundle.getProperty(context,                             
                            "emxEnterpriseChangeStringResource",
                            context.getLocale(),"emxEnterpriseChange.Pdcm.CanNotCancelChangeProcess");
            MqlUtil.mqlCommand(context, "notice $1", strNotice);
            return;
        }
    }

ContextUtil.pushContext(context, null, null, null);

    // Send notification
 String id = changeObj.getId();

 String argsmail[] = { id, "ChangeCancelNotify" };
 JPO.invoke(context, "emxNotificationUtil", null,
         "objectNotification", argsmail);



 isContextPushed = true;
 // Disconnect affected items from this ECO
 //String relPattern = RELATIONSHIP_AFFECTED_ITEM; // Relationship Name
 // modified for 081643
 String relPattern = RELATIONSHIP_AFFECTED_ITEM + "," + RELATIONSHIP_RAISED_AGAINST_ECR;
 // ECO-PART,ECO-SPEC, shd be replaced here
 StringList selectStmts = new StringList(2);
 selectStmts.addElement(DomainConstants.SELECT_ID);
 selectStmts.addElement(DomainConstants.SELECT_TYPE);
 //HF-016433
 selectStmts.addElement(DomainConstants.SELECT_CURRENT);

 StringList selectRelStmts = new StringList(1);
 selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

 MapList affectedItems = new MapList();
 Map objMap = null;
//373461
 String policyName = changeObj.getInfo(context, DomainConstants.SELECT_POLICY);
 String policyClassification = "";
 if(policyName!=null && policyName.length() != 0){
     policyClassification = FrameworkUtil.getPolicyClassification(context, policyName);
 }

/* if (policyClassification.equals("StaticApproval")) {
     //Modified for the IR-030668V6R2011
     //relPattern =   RELATIONSHIP_NEW_PART_PART_REVISION + "," + RELATIONSHIP_MAKE_OBSOLETE + "," + RELATIONSHIP_NEW_SPECIFICATION_REVISION;
       relPattern =   RELATIONSHIP_REQUEST_PART_REVISION + "," + RELATIONSHIP_REQUEST_SPECIFICATION_REVISION + "," +RELATIONSHIP_REQUEST_PART_OBSOLESCENCE + "," +RELATIONSHIP_NEW_PART_PART_REVISION + "," + RELATIONSHIP_MAKE_OBSOLETE + "," + RELATIONSHIP_NEW_SPECIFICATION_REVISION;
     //IR-030668V6R2011 ends
  }*/
//End



 // expand ECO to get affected items
 affectedItems = changeObj.getRelatedObjects(context, relPattern, // relationship
                                                             // pattern
         DomainConstants.QUERY_WILDCARD, // object pattern
         selectStmts, // object selects
         selectRelStmts, // relationship selects
         false, // to direction
         true, // from direction
         (short) 1, // recursion level
         null, // object where clause
         null); // relationship where clause

 Iterator objItr = (Iterator) affectedItems.iterator();
 // Create an object for use in the loop.
 DomainObject object = new DomainObject();
 while (objItr.hasNext()) {
     objMap = (Map) objItr.next();
     //HF-016433
     String currentState = (String)objMap.get(DomainConstants.SELECT_CURRENT);
     // If the user has chosen to delete the affected items from the
     // database,there is no need to disconnect (it will happen through delete)

     if (deleteAffectedItems)
     {
         //Start : HF-016433 - Added a condition
         if(!((currentState.equalsIgnoreCase(STATE_ECPART_RELEASE))||
             (currentState.equalsIgnoreCase(STATE_PARTSPECIFICATION_RELEASE))||
             (currentState.equalsIgnoreCase(STATE_CADMODEL_RELEASE))||
             (currentState.equalsIgnoreCase(STATE_CADDRAWING_RELEASE))||
             (currentState.equalsIgnoreCase(STATE_DRAWINGPRINT_RELEASE)))){
         object.setId((String) objMap.get(DomainConstants.SELECT_ID));
         object.deleteObject(context);
         }
         else {
             DomainRelationship.disconnect(context, (String) objMap
                     .get(DomainConstants.SELECT_RELATIONSHIP_ID));
         }
         //End : HF-016433
     } else {
         DomainRelationship.disconnect(context, (String) objMap
                 .get(DomainConstants.SELECT_RELATIONSHIP_ID));
     }
 }



 // Check for associated ECRs
 // expand ECO to get associated ECRs
 MapList relatedECRs = changeObj.getRelatedObjects(context,
         DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, // relationship
                                                                 // pattern
         DomainConstants.TYPE_ECR, // object pattern
         selectStmts, // object selects
         selectRelStmts, // relationship selects
         false, // to direction
         true, // from direction
         (short) 1, // recursion level
         null, // object where clause
         null); // relationship where clause

 if (relatedECRs.size() > 0 && disconnectECRs) {
     objItr = (Iterator) relatedECRs.iterator();
      while (objItr.hasNext()) {
         objMap = (Map) objItr.next();
              DomainRelationship.disconnect(context, (String) objMap
                     .get(DomainConstants.SELECT_RELATIONSHIP_ID));
         }
     }



 MapList routeList = new MapList();
 
 String SELECT_INBOX_TASK_ID = "to[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].from[" + DomainConstants.TYPE_INBOX_TASK + "].id";
 StringList listRouteTaskIds = new StringList();

 // Object selects for route
 selectStmts.addElement(DomainConstants.SELECT_OWNER);
 selectStmts.addElement(SELECT_INBOX_TASK_ID);

 // expand ECO to get associated Routes
 routeList = changeObj.getRelatedObjects(context,
         DomainConstants.RELATIONSHIP_OBJECT_ROUTE, // relationship
                                                     // pattern
         DomainConstants.TYPE_ROUTE+ SYMB_COMMA + DomainConstants.TYPE_ROUTE_TEMPLATE, // object pattern
         selectStmts, // object selects
         selectRelStmts, // relationship selects
         false, // to direction
         true, // from direction
         (short) 1, // recursion level
         null, // object where clause
         null); // relationship where clause




 StringList toListRoutes = new StringList();

 String strRouteType = null;

 if (routeList.size() > 0) {

     objItr = (Iterator) routeList.iterator();
     while (objItr.hasNext()) {
         objMap = (Map) objItr.next();

         strRouteType = (String) objMap.get(DomainConstants.SELECT_TYPE);
         
         if (objMap.get(SELECT_INBOX_TASK_ID) != null) {
        	 listRouteTaskIds.addAll(getSelectedListFromGetInfoMap(objMap, SELECT_INBOX_TASK_ID));
         }

         // Disconnect this ECO from the Route
         DomainRelationship.disconnect(context, (String) objMap
                 .get(DomainConstants.SELECT_RELATIONSHIP_ID));

		// send notification in case of route objects
		if ((DomainConstants.TYPE_ROUTE).equals(strRouteType))
		{
         // Send route owner notification of ECO cancel
         if (!toListRoutes.contains((String) objMap
                 .get(DomainConstants.SELECT_OWNER)))
             toListRoutes.addElement((String) objMap
                     .get(DomainConstants.SELECT_OWNER));

         // If this is the only object connected to the route,
         // terminate the route
         try {
             Route route = (Route) DomainObject.newInstance(context,
                     (String) objMap.get(SELECT_ID), ENGINEERING);
             String routeStatus = (String) route
                     .getAttributeValue(context,
                             DomainConstants.ATTRIBUTE_ROUTE_STATUS);
             // Stop only if current status is "Started"
             if (routeStatus.equals("Started"))
                 route.setAttributeValue(context,
                         ATTRIBUTE_ROUTE_STATUS, "Stopped");
         } catch (ClassCastException ex) {

         }
     }
 }
 } 
 
 if (listRouteTaskIds.size() > 0) {
	 DomainObject.deleteObjects(context, (String[]) listRouteTaskIds.toArray(new String[listRouteTaskIds.size()]));
 }


changeObj.setAttributeValue(context, ATTRIBUTE_BRANCH_TO, "Cancel");
 // Set the reason
 changeObj.setAttributeValue(context, ATTRIBUTE_REASON_FOR_CANCEL,
         sReason);
 // Promote ECO to Cancel State

 String ChangeObjectOwner=changeObj.getOwner(context).getName();


 changeObj.promote(context);

 changeObj.setOwner(context, PERSON_CORPORATE);
 ContextUtil.popContext(context);
 isContextPushed=false;


} catch (Exception e) {

 throw e;

} finally {
 if (isContextPushed)
 {
     ContextUtil.popContext(context);
 }
}

}
    /**
     * this method gets the List of persons to be notified
     * through Icon Mail. Gets the List of Persons to be Notified. The following
     * steps are performed: - The Related ECRs Owners Connected to this ECO are
     * notified. - The Related Route Owners and Route Assignees are notified -
     * The Related Distribution List connected to this Particular ECO are
     * notified
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the
     *            Change Process
     * @throws Exception
     *             if the operation fails.
     * @since Common X3.
     */

  public StringList getToListForCancelChangeProcess(Context context, String[] args)
     throws Exception //Method name shd be replaced
    {

    Map objMap = null;
    HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    String objectId = (String) paramMap.get("id");
    StringList toList=new StringList();


    DomainObject ChangeObject=new DomainObject(objectId);

    String ChangeObjectOwner=ChangeObject.getInfo(context,"attribute["+DomainConstants.ATTRIBUTE_ORIGINATOR+"]");

    toList.addElement(ChangeObjectOwner);



    StringList selectStmts = new StringList();
    selectStmts.addElement(DomainConstants.SELECT_ID);
    selectStmts.addElement(DomainConstants.SELECT_OWNER);

    StringList selectRelStmts = new StringList();
    selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

    String relPattern= DomainConstants.RELATIONSHIP_EC_DISTRIBUTION_LIST+","+DomainConstants.RELATIONSHIP_LIST_MEMBER;
    String TYPE_MEMBERLIST=PropertyUtil.getSchemaProperty(context,"type_MemberList");

    String typePattern =TYPE_MEMBERLIST+","+DomainConstants.TYPE_PERSON;

    // Getting Related MemberListPersons
    MapList   mlMemberList=ChangeObject.getRelatedObjects(context,
            relPattern, // relationship pattern
            typePattern, // object pattern
            selectStmts, // object selects
            selectRelStmts, // relationship selects
            false, // to direction
            true, // from direction
            (short) 2, // recursion level
            null, // object where clause
            null); // relationship where clause


    Iterator itrMemberList=mlMemberList.iterator();

      while(itrMemberList.hasNext())
    {
       Map mapmemberlist=(Map)itrMemberList.next();
       String levellist=(String)mapmemberlist.get("level");
       String member=(String)mapmemberlist.get(DomainConstants.SELECT_OWNER);

       if(levellist.equalsIgnoreCase("2"))
       {
           if(!toList.contains(member))
           {
           toList.addElement(member);
           }


       }



    }

    // Getting Reviewer List Members

    relPattern=DomainConstants.RELATIONSHIP_OBJECT_ROUTE+","+DomainConstants.RELATIONSHIP_ROUTE_NODE;
    typePattern=DomainConstants.TYPE_ROUTE_TEMPLATE+","+DomainConstants.TYPE_PERSON+","+DomainConstants.TYPE_ROUTE;

    MapList mlreviewerList=ChangeObject.getRelatedObjects(context,
            relPattern, // relationship pattern
            typePattern, // object pattern
            selectStmts, // object selects
            selectRelStmts, // relationship selects
            false, // to direction
            true, // from direction
            (short) 2, // recursion level
            null, // object where clause
            null); // relationship where clause


    Iterator itrreviewerlist=mlreviewerList.iterator();

    while(itrreviewerlist.hasNext())
    {

        Map mapreviewerList=(Map)itrreviewerlist.next();
        String level=(String)mapreviewerList.get("level");
        //String reviewerlistmember=(String)mapreviewerList.get(DomainConstants.SELECT_OWNER);

        String reviewerlistmember=(String)mapreviewerList.get(DomainConstants.SELECT_OWNER);



        if(level.equalsIgnoreCase("2"))
        {

            if(!toList.contains(reviewerlistmember))
            {
            toList.addElement(reviewerlistmember);
            }


        }



    }



    // Getting the Assignee List


    relPattern=DomainConstants.RELATIONSHIP_ASSIGNED_EC;
    typePattern=DomainConstants.TYPE_PERSON;

    MapList mlAssigneeList=ChangeObject.getRelatedObjects(context,
            relPattern, // relationship pattern
            typePattern, // object pattern
            selectStmts, // object selects
            selectRelStmts, // relationship selects
            true, // to direction
            false, // from direction
            (short) 1, // recursion level
            null, // object where clause
            null); // relationship where clause




    Iterator itrAssigneeList=mlAssigneeList.iterator();

    while(itrAssigneeList.hasNext())
    {
        Map mapitrAssigneeList=(Map)itrAssigneeList.next();
        String Assignee=(String)mapitrAssigneeList.get(DomainConstants.SELECT_OWNER);

        if(!toList.contains(Assignee))
        {
            toList.addElement(Assignee);
        }


    }


    return toList;
    }


/**
*To get the Company Default Value
* @param context the eMatrix <code>Context</code> object
* @param object id
* @returns HashMap
* @throws Exception if the operation fails
* Since Common X3.
**/

private HashMap getCompanyDefaultValue(Context context, String sObjectId)
	throws Exception
{

	DomainObject doParent = new DomainObject(sObjectId);
	String sParentType = doParent.getInfo(context, DomainObject.SELECT_TYPE);

	if(sParentType.equals(DomainObject.TYPE_ECR) || sParentType.equals(DomainObject.TYPE_ECO)) {
		String sRelPattern = "";
		if(sParentType.equals(DomainObject.TYPE_ECO)) {
			sRelPattern = RELATIONSHIP_DESIGN_RESPONSIBILITY;
		}
		else {
			sRelPattern = RELATIONSHIP_CHANGE_RESPONSIBILITY;
		}

		StringList slObjSelects = new StringList();
		slObjSelects.add(DomainObject.SELECT_NAME);
		slObjSelects.add(DomainObject.SELECT_ID);

		MapList mlCompany = doParent.getRelatedObjects(context, sRelPattern, DomainObject.TYPE_COMPANY, slObjSelects, null , true, false, (short)1, null, null);

		if(mlCompany!=null && mlCompany.size()>1) {
			HashMap mCompany = new HashMap();
			Map mTemp = (Map) mlCompany.get(0);
			mCompany.put("field_display_value", mTemp.get(DomainObject.SELECT_NAME));
			mCompany.put("field_value", mTemp.get(DomainObject.SELECT_ID));
			return mCompany;
		}
	}

	String sHostCompanyId = com.matrixone.apps.common.Company.getHostCompany(context);
	DomainObject doCompany = new DomainObject(sHostCompanyId);
	HashMap mCompany = new HashMap();
	mCompany.put("field_display_value", doCompany.getInfo(context, DomainObject.SELECT_NAME));
	mCompany.put("field_value", sHostCompanyId);
	return mCompany;
}

/**
*To get the Person Default Design Responsibility
* @param context the eMatrix <code>Context</code> object
* @returns HashMap
* @throws Exception if the operation fails
* Since Common X3.
**/

private HashMap getPersonDefaultDesignResponsibility(Context context)
	throws Exception
{
	HashMap mDesignResponsibility = new HashMap();
	String sPersonDefaultDesignResponsibility = Person.getDesignResponsibility(context);
	if(sPersonDefaultDesignResponsibility!=null && !sPersonDefaultDesignResponsibility.equals("")) {

		String sTemp = StringUtils.replace(sPersonDefaultDesignResponsibility, "{","");
		String [] sPersonDefaultDR = StringUtils.split(sTemp, "}");
		mDesignResponsibility.put("field_display_value", sPersonDefaultDR[1]);

		DomainObject doCompany = new DomainObject(new BusinessObject(sPersonDefaultDR[0], sPersonDefaultDR[1], sPersonDefaultDR[2], context.getVault().toString()));
		mDesignResponsibility.put("field_value", doCompany.getInfo(context, DomainObject.SELECT_ID));
	}

	return mDesignResponsibility;
}



/**
*To get the Company Default Value
* @param context the eMatrix <code>Context</code> object
* @param args program map
* @returns String
* @throws Exception if the operation fails
* Since Common X3.
**/

public Object getCompanyDefaultValueHTML(Context context, String[] args)
	throws Exception
{
	HashMap mProgramMap = (HashMap)JPO.unpackArgs(args);
	Map mParamMap= (HashMap) mProgramMap.get("paramMap");
	String sObjectId = (String) mParamMap.get("objectId");


	String languageStr = (String)mProgramMap.get("languageStr");;
	String sCompany = EnoviaResourceBundle.getProperty(context,  RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(), "emxComponents.Common.Company");

	Map mComapny = (HashMap) getCompanyDefaultValue(context, sObjectId);
	String sDisplayValue = "";
	String sHiddenValue = "";
	MapList InfoMap = new MapList();

	if (sObjectId != null && !sObjectId.equals("") && !sObjectId.equals("null") && !sObjectId.equals(" "))
	{
		DomainObject oDomObj = DomainObject.newInstance(context,sObjectId);
		String sType = oDomObj.getInfo(context,DomainConstants.SELECT_TYPE);

		StringList busSelects = new StringList(1);
		busSelects.add(DomainConstants.SELECT_ID);
		busSelects.add(DomainConstants.SELECT_NAME);

		StringList relSelects = new StringList(1);
		relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);


		if (sType != null && sType.equals(DomainConstants.TYPE_ECO))
		{
			InfoMap = oDomObj.getRelatedObjects(context,
											RELATIONSHIP_DESIGN_RESPONSIBILITY,
											"*",
											busSelects,
											relSelects,
											true,
											false,
											(short) 1,
											null,
											null);

		} else if (sType != null && sType.equals(DomainConstants.TYPE_ECR))
		{
			InfoMap = oDomObj.getRelatedObjects(context,
											RELATIONSHIP_CHANGE_RESPONSIBILITY,
											"*",
											busSelects,
											relSelects,
											true,
											false,
											(short) 1,
											null,
											null);


		}

	}

	if(mComapny.isEmpty()) {
		Map mFieldMap= (HashMap) mProgramMap.get("fieldMap");
		Map mSettings = (HashMap) mFieldMap.get("settings");
		sDisplayValue = (String) mSettings.get("Default");
		sHiddenValue = sDisplayValue;
	}
	else {
		sDisplayValue = (String) mComapny.get("field_display_value");
		sHiddenValue = (String) mComapny.get("field_value");
	}

	if (InfoMap.size() == 1)
	{
		Iterator itrList = InfoMap.iterator();
		while(itrList.hasNext())
		{
			Map valueMap =(Map) itrList.next();
            sHiddenValue =(String)valueMap.get(DomainConstants.SELECT_ID);
			sDisplayValue = (String)valueMap.get(DomainConstants.SELECT_NAME);
		}
	}



	StringBuffer sbReturnString = new StringBuffer();
	sbReturnString.append("<input type=\"text\"  name=\"CompanyDisplay\" id=\"\" value=\"");
	sbReturnString.append(sDisplayValue);
	sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
	sbReturnString.append("<input type=\"hidden\"  name=\"Company\" value=\"");
	sbReturnString.append(sHiddenValue);
	sbReturnString.append("\">");
	sbReturnString.append("<input type=\"button\" name=\"btnCompany\" value=\"...\" onclick=\"javascript:showChooser('../components/emxCommonSearch.jsp?formName=editDataForm&frameName=searchPane&searchmode=chooser&suiteKey=Components&searchmenu=APPECSearchAddExistingChooser&searchcommand=SearchECCompanyCommand&fieldNameActual="+sCompany+"&fieldNameDisplay=CompanyDisplay&objectId=");
	sbReturnString.append(sObjectId);
	sbReturnString.append("&fieldNameOID=CompanyOID&relId=null&suiteKey=Components','700','500')\">");
	sbReturnString.append("&nbsp;");

	return sbReturnString.toString();
   }

    /**
     * disconnects the Approval,Review List and DistributionList from Change Obj.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @throws Exception if the operation fails.
     * @since Common X3.
    */
    public void disconnectRouteTemplateDistributionList(Context context,String args[])throws Exception
    {

        String objectId = (String) args[0];
        String ReviewerListrelId=null;
        String ApprovalListrelId=null;
	    String relDistributionList = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");

        String objWhere="attribute["+DomainObject.ATTRIBUTE_ROUTE_BASE_PURPOSE+"]=='"+RANGE_APPROVAL+"' || attribute["+DomainObject.ATTRIBUTE_ROUTE_BASE_PURPOSE+"]=='"+RANGE_REVIEW+"'";

        DomainObject domOObj=DomainObject.newInstance(context);
        domOObj.setId(objectId);

        StringList busSelects = new StringList(1);
        busSelects.add(DomainConstants.SELECT_ID);

        StringList relSelects = new StringList(1);
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

        String DistributionListrelid=null;

        //Get connected distribution list
        Map DistributionListMap=domOObj.getRelatedObject(context,relDistributionList,true,busSelects,relSelects) ;

        //Get connected Approval/reviewer list
        MapList TemplateListId=domOObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE,DomainConstants.TYPE_ROUTE_TEMPLATE, busSelects,relSelects,false, true, (short)1, objWhere,null);

        if(DistributionListMap!=null && !DistributionListMap.isEmpty()){
            DistributionListrelid=(String)DistributionListMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        }

        Iterator itrRTList = TemplateListId.iterator();
        DomainObject RTObj=DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE_TEMPLATE);
        String sRTId = "";
        while(itrRTList.hasNext())
        {
            Map RTMap =(Map) itrRTList.next();
            sRTId =(String)RTMap.get(DomainConstants.SELECT_ID);
            RTObj.setId(sRTId);

            //Get route base purpose (Review/Approval)
            String sRouteBasePurpose=RTObj.getAttributeValue(context,DomainObject.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            if(sRouteBasePurpose!=null && !("".equals(sRouteBasePurpose)) && RANGE_APPROVAL.equals(sRouteBasePurpose)){
                ApprovalListrelId=(String)RTMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }
            else{
                ReviewerListrelId=(String)RTMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);;
            }
        }
        try
        {
            //Disconnecting distribution/approval/review list from MCO
            if(DistributionListrelid!=null && !(DistributionListrelid.equals("")))
            {
                 DomainRelationship.disconnect(context, DistributionListrelid);
            }
            if (ReviewerListrelId!=null && !(ReviewerListrelId.equals("")))
            {
                DomainRelationship.disconnect(context, ReviewerListrelId);
            }
            if (ApprovalListrelId!=null && !(ApprovalListrelId.equals("")))
            {
                 DomainRelationship.disconnect(context, ApprovalListrelId);
            }
        }
        catch(Exception e)
        {
            throw e;
        }
    }//End of disconnectRouteTemplateDistributionList method

/**
 * Delegates the Assignments of one or more selected assignees to a single Assignee
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds - String Array of Change objectId
 *                   - String Array of New Person Id
 *                   - String Array of Old Person Id
 *                   - String Array of Relationship Id
 * Return Object
 * @throws Exception if the operation fails.
 * @since Common X3.
*/

public Object delegateAssignees(Context context, String[] args)throws Exception
    {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
	   // Obtaining the Parent Change Object Id
       String[] strChangeObjID = (String[])programMap.get("strChangeObjID");
	   // Obtaining the Object Id of the New Assignee
	   String[] strNewAssigneeID = (String[])programMap.get("strNewAssigneeID");
	   // Obtaining the Object Ids of the Assignees selected initially on the List Page
	   String[] strOldAssigneeObjID = (String[])programMap.get("strOldAssigneeObjID");
   	   // Obtaining the "Assigned EC" Rel Ids of the Assignees selected initially on the List Page
	   String[] arrRelIds1 = (String[])programMap.get("arrRelIds1");

		try{

	   // Iterating over the List of Rel Ids of the Objects initially selected on the Assignee List Page
	   for (int k=0;k<arrRelIds1.length ;k++)
		{
			 String strMQLCommand ;
			 String strMQLCommand2 ;
			 String strMQLCommand3 ;

			 String strMQLCommandResult =  "";
			 String strMQLCommandResult2 =  "";
			 String strMQLCommandResult3 =  "";

			 MQLCommand prMQL  = new MQLCommand();
			 MQLCommand prMQL2  = new MQLCommand();
			 MQLCommand prMQL3  = new MQLCommand();

			 prMQL.open(context);
			 prMQL2.open(context);
			 prMQL3.open(context);

             ContextUtil.startTransaction(context, true);

			 // Taking the Old Assignee Rel Ids in String Tokenizer for getting them in usable format. Tokenizing on the basis of "," values
			 StringTokenizer stzRelId = new StringTokenizer(arrRelIds1[k],",", false);
			 // Taking the Object Ids in String Tokenizer for getting them in usable format. Tokenizing on the basis of "," values
//			 StringTokenizer stzChangeObjID = new StringTokenizer(strOldAssigneeObjID[k],",", false);

			 while(stzRelId.hasMoreElements()) // For each Rel Id get the corresponding Obj Id
			 {
			   // Converting the tokens to String
               String strTokenRelId = stzRelId.nextToken().toString();
			   String strTempString1 = StringUtils.replace(strTokenRelId, "]", "");

			   // Obtaining the Old Assignee Object id in usable format
			   String strFinalRelId = StringUtils.replace(strTempString1, "[", "");

			   // Obtaining the Old Assignee Object id in usable format
//			   String strTokenChangeId = stzChangeObjID.nextToken().toString();
//			   String strTempString2 = strTokenChangeId.replace("]", "");

				// The MQL Command for obtaining the Rel2Rel Ids if any connected to the Old Assignee Objects
				strMQLCommand = "print connection $1 select $2 dump";
				prMQL.executeCommand(context,strMQLCommand, strFinalRelId, "frommid.id");
				strMQLCommandResult = prMQL.getResult();

				if(!strMQLCommandResult.equals("\n"))
				 {
					// If some Rel2Rel id is returned in Step3, then come in the If Loop otherwise in the else Loop.
					String strAssAffItemRelIds  = "";

					String strRelName = DomainConstants.RELATIONSHIP_ASSIGNED_EC;
					// Creating new DomainObject & DomainRelationship Objects for adding new "Assigned EC" Rel.
					DomainObject domNewPerson = new DomainObject(strNewAssigneeID[k]);
					DomainObject domChangeObj = new DomainObject(strChangeObjID[0]);
					DomainRelationship domRelID = new DomainRelationship();

					DomainRelationship domNewRel	= null;
					// start
					String strNewAssigneeRelID = getPersonAssigneeRelID(context,strChangeObjID[0],strNewAssigneeID[k]);
					if (strNewAssigneeRelID.equals(""))
					{
						domNewRel= DomainRelationship.newInstance(context);
						// connecting the New Assignee with the Change Object with the Rel "Assigned EC"
						domNewRel = domRelID.connect(context,domNewPerson,strRelName,domChangeObj);
					}
					else
					{
						domNewRel= new DomainRelationship(strNewAssigneeRelID);
					}
					// end
					// Tokenizing the List of Affected Items Receieved to get them individually
					StringTokenizer stzMQ1CommandResult = new StringTokenizer(strMQLCommandResult,",", false);
					try{

							while(stzMQ1CommandResult.hasMoreElements())
							 {
								// Getting the individual Ass Aff Item Rel Id
								strAssAffItemRelIds = stzMQ1CommandResult.nextToken().toString();

								strMQLCommand2 = "print connection $1 select $2 dump";
								prMQL2.executeCommand(context,strMQLCommand2, strAssAffItemRelIds, "torel.id");
								strMQLCommandResult2 = prMQL2.getResult();

								String strAIId = strMQLCommandResult2 ;
								//  Deleting the existing rel2rel connection.
								MqlUtil.mqlCommand(context,"delete connection $1", strAssAffItemRelIds);

								// Creating new ReltoRel START
								MqlUtil.mqlCommand(context,"Verb $1","on");
								strMQLCommand3 = "add connection \""+RELATIONSHIP_ASSIGNEED_AFFECTED_ITEM+"\" fromrel "+domNewRel+" torel "+strAIId;

								// getting the MQL Command result
								strMQLCommandResult3 = prMQL3.getResult();

								strMQLCommandResult3 = 	MqlUtil.mqlCommand(context,strMQLCommand3);
//								int iFindx                       = strMQLCommandResult3.indexOf("'");
//								int iLindx                       = strMQLCommandResult3.lastIndexOf("'");
							 }

					}catch (Exception a)
					 {
						System.out.println(a.toString());
						}
				 }
				 else
				 {
					String strAssigneeNoDelegate = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Common.Message.strAssigneeNoAffectedItem") ;
					emxContextUtil_mxJPO.mqlNotice(context,strAssigneeNoDelegate);

					// If the "strMQLCommandResult" does not return a rel2Rel Id, the code comes in here
					DomainRelationship newDelgatedAssigneeRel	= null;
			        newDelgatedAssigneeRel = DomainRelationship.newInstance(context);
				  }
			 }
		}
		ContextUtil.commitTransaction(context);
		  }catch (Exception e)
				{
					ContextUtil.abortTransaction(context);
					throw (new FrameworkException(e));
				}

    return "";
   }


	 /**
      * Notifies the Distributin List and Originator
     * @param context
     * @param args
     * 0 object Id
     * 1 notification object name
     * @return int
     * @throws Exception
     * @since Common X3
     */
    public int notifyRelatedObject(Context context,
             String[] args) throws Exception
     {
        String objectId = args[0];//ECO Object
        setId(objectId);    // notification object id
        String strDistributionListNotificationObj = args[1];
        String strnotifyargs[] = { objectId, strDistributionListNotificationObj};
        JPO.invoke(context, "emxNotificationUtil", null,
                "objectNotification", strnotifyargs);
        return 0;
     }

 	/**
     * filters persons maplist based on the role
     *
     * @param	context the eMatrix <code>Context</code> object.
     * @param	mlPersons holds person maps
     * @param	sRole holds role based on which filtering is done
     * @return	MapList, the Object ids matching the filter criteria
     * @throws	Exception if the operation fails.
     * @since	Common X3.
     */

	public static MapList filterPersonsBasedOnRole(Context context, MapList mlPersons, String sRole) throws Exception
	{
		StringList slRole = new StringList();
		slRole.addElement(sRole);
		return filterPersonsBasedOnRole(context, mlPersons, slRole);

	}

 	/**
     * filters persons maplist based on the role, overloaded method
     *
     * @param	context the eMatrix <code>Context</code> object.
     * @param	mlPersons holds person maps
     * @param	slRole holds list of roles based on which filtering is done
     * @return	MapList, the Object ids matching the filter criteria
     * @throws	Exception if the operation fails.
     * @since	Common X3.
     */

	public static MapList filterPersonsBasedOnRole(Context context, MapList mlPersons, StringList slRole) throws Exception
	{
		MapList mlFilteredPersons = new MapList();
		Iterator itrPerson = mlPersons.iterator();
		while(itrPerson.hasNext())
	    {
			Map mPerson = (Map) itrPerson.next();
			String sPeronId = (String) mPerson.get(DomainObject.SELECT_ID);
			Person objPerson = new Person(sPeronId);

			boolean bHasRole = false;
			Iterator itrRole = slRole.iterator();
			while(!bHasRole && itrRole.hasNext()) {
				String sRole = (String) itrRole.next(); //PropertyUtil.getSchemaProperty(context,(String) itrRole.next());

				if(objPerson.hasRole(context, sRole)) {
					bHasRole = true;

				}
			}
			if(bHasRole) {
				mlFilteredPersons.add(mPerson);
			}
		}
		return mlFilteredPersons;
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
     * @@since        Common X3
     **
     */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public static MapList getDesignEngineers(Context context, String [] args) throws Exception
	{
		MapList mlPersons = emxCommonEngineeringChange_mxJPO.getPersons(context, args);

		StringList slRole = new StringList();
		slRole.addElement(PropertyUtil.getSchemaProperty(context,"role_DesignEngineer"));
		slRole.addElement(PropertyUtil.getSchemaProperty(context,"role_SeniorDesignEngineer"));

		return filterPersonsBasedOnRole(context, mlPersons, slRole);
	}

	 /**
         * Connects the Design Responsibility Organization to the Engineering Object.
         * This method is called for ECO, ECR, Part, Spec objects as update function in their respective Edit pages.
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap containing the following entries:
         * paramMap - a HashMap containing the following keys, "objectId", "old RDOIds", "New OID", "Old RDO Rel Ids".
         * @return Object - boolean true if the operation is successful
         * @throws Exception if operation fails
		 * @since   Common X3
         */

       public void connectDesignResponsibility (Context context, String[] args) throws Exception {
         try{
             //unpacking the Arguments from variable args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                HashMap paramMap = (HashMap) programMap.get("paramMap");

			HashMap requestMap= (HashMap)programMap.get("requestMap");
			// New ECO Object Id
			String strObjectId = (String)paramMap.get("objectId");
			// Mode option
			String[] strModCreate= (String[])requestMap.get("CreateMode");
			String str = strModCreate[0];
             //Relationship name
             String strRelationshipDesignResponsibility = DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY;
				if(str.equals("CreateECO") || str.equals("CreateMECO") || str.equals("CreateECR")) {
             //Getting the EC Object id and the new product object id
             String strChangeobjectId = (String)paramMap.get("objectId");

 		                DomainObject changeObj =  new DomainObject(strChangeobjectId);
						String strNewToTypeObjId = (String)paramMap.get("New OID");
 		                String strOldToTypeObjId = (String)paramMap.get("Old OID");
 						//Relationship name
 		 				DomainObject oldListObject = null;
 		 				DomainObject newListObject = null;
 		                RelationshipType relType = new RelationshipType(strRelationshipDesignResponsibility);
                       if (strNewToTypeObjId == null || "".equals(strNewToTypeObjId) || "Unassigned".equalsIgnoreCase(strNewToTypeObjId) || "null".equalsIgnoreCase(strNewToTypeObjId) || " ".equals(strNewToTypeObjId))
						{
							strNewToTypeObjId = (String)paramMap.get("New Value");
						}

 		  			   if (!("".equals(strOldToTypeObjId)||(strOldToTypeObjId==null) ||  "Unassigned".equalsIgnoreCase(strOldToTypeObjId) || "null".equalsIgnoreCase(strOldToTypeObjId)))
 		 			   		{

 		 						oldListObject = new DomainObject(strOldToTypeObjId);
 		 						changeObj.disconnect(context,relType,true,oldListObject);
 		 			   	     }

 		 			  if(!("".equals(strNewToTypeObjId)||(strNewToTypeObjId==null) || "Unassigned".equalsIgnoreCase(strOldToTypeObjId) || "null".equalsIgnoreCase(strOldToTypeObjId)))
 		 					{

 		 						newListObject = new DomainObject(strNewToTypeObjId);

 		 						DomainRelationship.connect(context,newListObject,relType,changeObj) ;
 				   }
				}else{
			String[] strMod= (String[])requestMap.get("CreateMode");
			String strMode = strMod[0];
			if((strMode!=null && strMode.equalsIgnoreCase("AssignToECO"))||(strMode!=null && strMode.equalsIgnoreCase("MoveToECO"))||(strMode!=null && strMode.equalsIgnoreCase("MoveToMECO"))||(strMode!=null && strMode.equalsIgnoreCase("AddToECO")))

				{

			// Creating DomainObject of new ECO
			DomainObject domObjectChangeNew =  new DomainObject(strObjectId);
			String strNewValue = (String)paramMap.get("New Value");

if (strNewValue == null || strNewValue.trim().length() == 0)
{
strNewValue = (String)paramMap.get("New OID");
}

if(strMode.equalsIgnoreCase("MoveToMECO"))
{
    strNewValue = (String)paramMap.get("New OID");
}

			// Creating DomainObject
						if(strNewValue!=null && !"null".equals(strNewValue) && strNewValue.length() > 0)
						{
			DomainObject domainNewValue = new DomainObject(strNewValue);
			try{

                DomainRelationship.connect(context,
                                           domainNewValue,
                                            strRelationshipDesignResponsibility,
                                           domObjectChangeNew);

         }catch(Exception ex){
			 System.out.println("Exception "+ex);
			}
			}
			}
				}
        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
 	}



/**
 * DisConnects the ChangeObject with Part
 * If the Part has some Version then Purges the Part.
 * @param	context the eMatrix <code>Context</code> object
 * @param	args holds a HashMap containing the following entries:
 * paramMap - a HashMap containing the following keys, "arrTableRowIds", "arrRelIds"
 * @return	void
 * @throws	Exception if operation fails
 * @since   Common X3
 * @deprecated since V6R2017x for Function_FUN059293 and replaced by {@link ${CLASS:enoEngChange}#removeAffectedItems(Context context, String[] args)}
 */

public void removeAffectedItems(Context context, String[] args)
	                throws Exception
	        {
		try{
					String temp = "";
					String partObjectId = "";
					String attributeValue = "";
					String sObjectId = "";
					StringList objectList = null ;
					StringList relObjectList = null ;

                 	HashMap programMap = (HashMap) JPO.unpackArgs(args);

                    String[] arrTableRowIds = (String[]) programMap.get("arrTableRowIds");
                    String[] arrRelIds = (String[]) programMap.get("arrRelIds");
                    String changeID = (String) programMap.get("changeId");

                    int sRowIdsCount =arrTableRowIds.length;
                    int sRelIdsCount =arrRelIds.length;
//370192 Start
                    StringList indirectAIList = getIndirectAffectedItemRelIds(context, changeID, arrRelIds);
                    int listSize = indirectAIList.size();
                    String[] strIndirectId = new String[listSize]; //374504
                    String[] strIndirectRelIds = new String[listSize];

                    Map map = null;
                    for(int i=0; i<listSize; i++){
                        objectList = FrameworkUtil.split((String)indirectAIList.get(i), "|");
                        strIndirectId[i] = (String)objectList.elementAt(0);
                        strIndirectRelIds[i] = (String)objectList.elementAt(1);
                    }
//370192 End

//374504 Start
                    String strIsVersion = "";

                    StringList objSelects= new StringList();
                    objSelects.add(DomainConstants.SELECT_ID);
                    objSelects.add("attribute[" + isVersion +"]");

                    StringBuffer sbToDeleteIds = new StringBuffer();
                    MapList list = DomainObject.getInfo(context, strIndirectId, objSelects);
                    for(int i=0; i<list.size(); i++){
                        map = (HashMap)list.get(i);
                        strIsVersion = (String)map.get("attribute[" + isVersion +"]");
                        if(strIsVersion.equalsIgnoreCase("true")){
                            sbToDeleteIds.append((String)map.get(DomainConstants.SELECT_ID));
                            sbToDeleteIds.append("|");
                        }
                    }
//374504 End

                    //String changeID = null;
                    // Proposed markup in case of ECR
                    String relName = RELATIONSHIP_PROPOSED_MARKUP;
                    //358689 start
					boolean isMBOMInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMManufacturing",false,null,null);
                    //358689 end
                    for(int relcount=0; relcount < sRelIdsCount; relcount++)
                      {
                        StringList slSelect = new StringList("from.id");
                        slSelect.add("to.id");
                        MapList mlAffectedItem = DomainRelationship.getInfo(context, new String[]{arrRelIds[relcount]} ,slSelect);
                        Map m=(Map) mlAffectedItem.get(0);
                        String strID = (String) m.get("to.id");
                        changeID = (String) m.get("from.id");
                        DomainObject doChange = new DomainObject(changeID);
                        //358689
                        if(isMBOMInstalled)
                        {
                         String connId= null;
                         String pmId = null;
                         String fromChange="";
                         String fromValue="";
                         String changeName="";
                         changeName=(String)doChange.getInfo(context,DomainConstants.SELECT_NAME);
                         MapList mL=new MapList();
                         DomainObject dob=new DomainObject(strID);
                         StringList busSelList=new StringList();
                         busSelList.add(DomainConstants.SELECT_NAME);
                         Map mapDetails = dob.getInfo(context, busSelList);
                         String   sPartName = (String) mapDetails.get(DomainObject.SELECT_NAME);
                         mL=(MapList)dob.findObjects(context,PropertyUtil.getSchemaProperty(context,"type_PartMaster"),sPartName,"","*","*",null,false,busSelList);
                         if (mL.size()>0) {
                           Map mapData = (Map)mL.get(0);
                           pmId = (String)mapData.get(DomainConstants.SELECT_ID);
                         DomainObject pmobj=new DomainObject(pmId);
                         StringList manuRespChgIds=(StringList)pmobj.getInfoList(context,"to["+PropertyUtil.getSchemaProperty(context,"relationship_ManufacturingResponsibilityChange")+"].id");
                          for (int j = 0; j < manuRespChgIds.size(); j++) {
                               connId=(String)manuRespChgIds.get(j);
                               if(connId!=null)
                               {
                                   fromChange="print connection $1 select $2  dump";
                                   fromValue=MqlUtil.mqlCommand(context,fromChange, connId, "from.id");
                                   if(fromValue.equals(changeID))
                                   {
                                       disconnect(context,connId);
                                   }
                               }
                         }
                          StringList manuRespIds=(StringList)pmobj.getInfoList(context,"to["+DomainConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY+"].id");
                          for (int i = 0; i < manuRespIds.size(); i++) {
                              connId=(String)manuRespIds.get(i);
                              if(connId!=null)
                              {
                                  DomainRelationship dr=new DomainRelationship(connId);
                                  fromValue=dr.getAttributeValue(context,"Doc-In");
                                  if(changeName.equals(fromValue)){
                                      disconnect(context,connId);
                               }
                             }
                        }
                         }
                        }
                        //end 359689
                        if (doChange.isKindOf(context, TYPE_ECO)|| doChange.isKindOf(context, TYPE_MECO))
                        {
                            // Proposed markup in case of ECO/MECO
                        relName = RELATIONSHIP_APPLIED_MARKUP;
                        }

                        String objectWhere = "(to["+relName+"].from.id=="+changeID+")";
                        // retrieve the markups that are unique to Part and ECR/ECO.
                        MapList mapListMarkups =    new DomainObject(strID).getRelatedObjects(context,
                                                                                     RELATIONSHIP_EBOM_MARKUP,
                                                                                     "*",
                                                                                     new StringList(SELECT_ID),
                                                                                     null,
                                                                                     false,
                                                                                     true,
                                                                                     (short)1,
                                                                                     objectWhere,
                                                                                     null);
                        relObjectList = new StringList();

                        Iterator itr = mapListMarkups.iterator();
                        while(itr.hasNext())
                        {
                            Map m2=(Map) itr.next();
                            String strID1 = (String) m2.get(SELECT_ID);
                            DomainObject dobjBOMMarkup = new DomainObject(strID1);
                            dobjBOMMarkup.deleteObject(context);

                        }
                      }
 // 374504 - Commented for 374504
                    /*for(int count=0; count < sRowIdsCount; count++)
                    {
                        temp  = arrTableRowIds[count];
                        objectList = FrameworkUtil.split(temp,"|");
                        partObjectId = (String)objectList.elementAt(1) ;
                        DomainObject dobj = new DomainObject(partObjectId);
                        attributeValue =   dobj.getAttributeValue(context,isVersion) ;
                        StringList busSelects = new StringList(2);
                        busSelects.add(DomainConstants.SELECT_ID);
                        StringList relSelects = new StringList(2);
                        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

                            if(attributeValue.equalsIgnoreCase("FALSE"))
                             {
                                MapList mapList = dobj.getRelatedObjects(context,RELATIONSHIP_PART_VERSION,DomainConstants.TYPE_PART ,busSelects,relSelects,false,true,(short)1,null,null);
                                if (mapList.size() > 0)
                                {
                                    Iterator itr = mapList.iterator();
                                    while(itr.hasNext())
                                    {
                                        Map newMap = (Map)itr.next();
                                        sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);
                                        DomainObject dobjPartVersion = new DomainObject(sObjectId);
                                        dobjPartVersion.deleteObject(context);
                                    }
                                }
                             }
                     }*/
//374504 - Commented till here for 374504
//370192 Start
                    String strDestRelIds[] = new String[arrRelIds.length + strIndirectRelIds.length];
                    System.arraycopy(arrRelIds, 0, strDestRelIds, 0, arrRelIds.length);
                    System.arraycopy(strIndirectRelIds, 0, strDestRelIds, arrRelIds.length, strIndirectRelIds.length);
                    //disconnect(context,arrRelIds, false);
                    disconnect(context,strDestRelIds, false);
//370192 End
                    
                  //Added for IR-143641 start
                    //Relationship "Raised Against ECR" Needs to be removed when ECR Remove action is executed
                      String sRelId = "";
                      String ToId = "";
                      String FromId = "";
                      StringTokenizer st = null;
                        for(int i=0; i < arrTableRowIds.length; i++)
                          {
                              st = new StringTokenizer(arrTableRowIds[i], "|");
                              sRelId = st.nextToken();                                             
                              ToId =st.nextToken();                         
                              FromId=st.nextToken();                         
                          }
                                              
                      DomainObject dObj1 =  new DomainObject(ToId);
                      String whereclause = "id==\"" +FromId+"\")" ;//ECR Id
                      StringList selectRelStmts = new StringList(1);
                      selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
                  
                       MapList totalresultList = dObj1.getRelatedObjects(context,
                                          DomainObject.RELATIONSHIP_RAISED_AGAINST_ECR, // relationship pattern
                                          DomainConstants.TYPE_ECR,    // object pattern
                                          new StringList(),            // object selects
                                          selectRelStmts,              // relationship selects
                                          true,                        // to direction
                                          false,                       // from direction
                                          (short)1,                    // recursion level
                                          whereclause,                 // object where clause
                                          null);  
                         
                       if(totalresultList!=null && totalresultList.size()>0)
                       {
                    	   Map sRelatedECRNameMap = (Map)totalresultList.get(0);
                    	   sRelId = (String)sRelatedECRNameMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);             
                    	   if(sRelId != null)
                    	   {
                    		   DomainRelationship.disconnect(context, sRelId);               
                           }
                       }
                      //Added for IR-143641 end 
                                        
//374504 Start
                    String tempStr = sbToDeleteIds.toString();
                    if(!tempStr.equals("") && (tempStr!=null)){
                        String strToDeleteIds[] = StringUtils.split(tempStr, "[|]");
                        DomainObject.deleteObjects(context, strToDeleteIds);
                    }
//374504 End
            }catch (Exception e) {
                e.printStackTrace();
            }

        }

      /**
     * Creates new Revision for Affected Item
     * If the Affected Item's Requested change value is Revise
     * it will create version object
     * @param	context the eMatrix <code>Context</code> object
     * @param	args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "arrTableRowIds", "arrRelIds"
     * @return	void
     * @throws	Exception if operation fails
     * @since   Common X3
     * @deprecated since V6R2017x for Function_FUN059293 and replaced by {@link ${CLASS:enoEngChange}#createNewRevisionForAffectedItem(Context context, String[] args)}
    */
	
   public void createNewRevisionForAffectedItem(Context context, String[] args)
                throws Exception

	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String[] strObjectIds = (String[])programMap.get("strObjectIds");
		String[] arrTableRowIds = (String[])programMap.get("arrTableRowIds");
		String strChangeObjectId = (String)programMap.get("objectId");

		boolean blnIsError = false;

		String relAffectedItems = PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem");
		String attrRequestedChange = PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange");
		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
		String selectAttr="relationship[" + relAffectedItems+ "].attribute[" + attrRequestedChange+ "].value";
		String languageStr = (String)programMap.get("languageStr");
		Locale strLocale = context.getLocale();
		String attrRequestedChangeValue= EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale,"emxFramework.Range.Requested_Change.For_Revise");
		

		String strMessage=EnoviaResourceBundle.getProperty(context,  RESOURCE_BUNDLE_COMPONENTS_STR, strLocale, "emxComponents.Common.AffectedItem.CreateNewRevision") + ": ";

		DomainObject domObjAffectedItem = DomainObject.newInstance(context);

		String strErrorParts = "";

		for(int i=0;i<strObjectIds.length;i++)
		{
			int t=strObjectIds[i].indexOf('|');
			String affectedItemId=(strObjectIds[i].substring(0,t));
			int iS=arrTableRowIds[i].indexOf('|');
			String affectedItemRelId=(arrTableRowIds[i].substring(0,iS));
			DomainObject domObj=new DomainObject(affectedItemId);
			DomainRelationship domRelAI =new DomainRelationship(affectedItemRelId);
			String attrValue=domRelAI.getAttributeValue(context,attrRequestedChange);

			String[] inputArgs = new String[2];

			inputArgs[0] = affectedItemId;
			inputArgs[1] = strChangeObjectId;
			String strNewPartId = (String) getIndirectAffectedItems(context, inputArgs);

			String strAIName = domObj.getInfo(context, DomainConstants.SELECT_NAME);

			if(attrValue.equals(attrRequestedChangeValue) && strNewPartId == null)
			{
				BusinessObject lastRevObj = domObj.getLastRevision(context);
				String nextRev = lastRevObj.getNextSequence(context);
				String objectId=lastRevObj.getObjectId();
				String lastRevVault = lastRevObj.getVault();
				domObjAffectedItem.setId(objectId);
				BusinessObject revBO = domObjAffectedItem.revise(context, nextRev, lastRevVault);
				DomainObject revPart = new DomainObject(revBO);
				revPart.getBasics(context);
				String currentUser = context.getUser();
				String revPartId = revPart.getObjectId(context);
				//added
				String mqlCmd = "print connection $1 select $2 dump $3";
				MQLCommand mCmd = new MQLCommand();
				mCmd.executeCommand(context,mqlCmd, affectedItemRelId, "tomid.fromrel["+DomainConstants.RELATIONSHIP_ASSIGNED_EC+"].from.id", "|");
				String personId = mCmd.getResult().trim();
				DomainObject dobjPerson  = new DomainObject(personId);

				String strAssigneeUserName = dobjPerson.getName(context);

				context.setUser(strAssigneeUserName);
				DomainRelationship doAffectedItemRel = DomainRelationship.connect(context,
											new DomainObject(strChangeObjectId),
											RELATIONSHIP_AFFECTED_ITEM,
											new DomainObject(revPartId));
				try
				{
					ContextUtil.pushContext(context);
					doAffectedItemRel.setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");
					ContextUtil.popContext(context);
				}
				catch (Exception ex)
				{
				}
				finally
				{
					ContextUtil.popContext(context);
				}

				context.setUser(currentUser);
			}//end for if(attrValue.equals(attrRequestedChangeValue))
			else
			{
				blnIsError = true;
				if ("".equals(strErrorParts))
				{
					strErrorParts = strAIName;
				}
				else
				{
					strErrorParts = strErrorParts + ", " + strAIName;
				}

			}
		}//end for FOR loop

		if (blnIsError)
		{
			strMessage = strMessage + strErrorParts;
				emxContextUtil_mxJPO.mqlNotice(context,strMessage);
			}
}//end for method createNewRevisionForAffectedItem


/**
	 * Populates the Range Values for Attribute RequestedChange Based on Change Type and Part State
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap - a HashMap containing the following keys, "objectId"
	 * @return	StringList containing Avalable selection for Requested Change
	 * @throws	Exception if operation fails
	 * @since   Common X3
	 */
	public StringList requestedChangeValues(Context context,String[] args) throws Exception
	{
		String strLanguage  =  context.getSession().getLanguage();
		Locale strLocale = context.getLocale();

		StringList requestedChange = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");
		String mode="";
		HashMap paramList=(HashMap)programMap.get("paramList");
		String ChangeObjectId =(String) paramList.get("objectId");
		mode =(String) paramList.get("mode");
		if(ChangeObjectId!=null){
			DomainObject doChangeObj = new DomainObject(ChangeObjectId);
			String sChangeType	=	doChangeObj.getInfo(context, DomainConstants.SELECT_TYPE);
			if(sChangeType.equals(TYPE_MECO) || sChangeType.equals(TYPE_DCR))
			{
			//optionList.clear();
			//optionList.add(RANGE_FOR_UPDATE);
			mode="view";
			}
		}
		//if table is in edit mode
		Iterator objItr = objectList.iterator();
		//if table is in edit mode
		if("Edit".equals(mode))
		{
			while(objItr.hasNext())
			{
				Map objMap = (Map)objItr.next();
				String sAIObjId		=(String)objMap.get(DomainConstants.SELECT_ID);
				String sAIRelId		= (String)objMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
				String reqChangeValue = (String)objMap.get(ATTRIBUTE_REQUESTED_CHANGE);
                if((!sAIRelId.equals("")) && (!sAIRelId.equals(null)))
				{
					DomainRelationship domRelObj=new DomainRelationship(sAIRelId);

					String[] sArray			= {sAIRelId};
					StringList slSelect = new StringList(2);
					slSelect.add(DomainConstants.SELECT_FROM_ID);
					MapList mlAffectedItem = DomainRelationship.getInfo(context, sArray ,slSelect);
					Iterator itrMLAffectedItem	= mlAffectedItem.iterator();
					Map mAffectedItems			= (Map) itrMLAffectedItem.next();
					DomainObject doChangeObj = new DomainObject((String)mAffectedItems.get(DomainConstants.SELECT_FROM_ID));

					String currentChangeStatus	=	doChangeObj.getInfo(context, DomainConstants.SELECT_CURRENT);
					String currentStatus=domRelObj.getAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE);

					if((currentChangeStatus.equals(STATE_ECO_CREATE))||(currentChangeStatus.equals(STATE_ECO_DEFINE_COMPONENTS))||(currentChangeStatus.equals(STATE_ECO_DESIGN_WORK))||(currentChangeStatus.equals(STATE_ECR_CREATE))||(currentChangeStatus.equals(STATE_ECR_SUBMIT))||(currentChangeStatus.equals(STATE_ECR_EVALUATE))||(currentChangeStatus.equals(STATE_ECR_REVIEW)))
					{
					DomainObject domObj=new DomainObject(sAIObjId);
					String current			= (String)domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
					StringBuffer option=new StringBuffer();
					StringList optionList=new StringList();
					optionList.add(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.None"));
						if(!current.equals(STATE_ECPART_RELEASE))
					{
						optionList.add(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.For_Release"));
					}
					else
					{
						optionList.add(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.For_Revise"));
						optionList.add(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.For_Obsolescence"));
					}
					option.append("<select name=RequestedChange"+sAIObjId+">");

					if(currentStatus!=null && !"".equals(currentStatus) && !"null".equals(currentStatus))
					{
						for(int i=0;i<optionList.size(); i++)
						{
							option.append("<option VALUE= \"");
							option.append(optionList.get(i));
							option.append("\"");
							if(optionList.get(i).equals(currentStatus)) {
								option.append(" SELECTED  ");
							}
							option.append(">");
							option.append(optionList.get(i));
							option.append("</option>");
						}
					}
					else
					{
						for(int i=0;i<optionList.size(); i++)
						{
						   option.append("<option VALUE= \"");
						   option.append(optionList.get(i));
						   option.append("\"");
						   if(optionList.get(i).equals("None")) {
							 option.append(" SELECTED  ");
						   }
						   option.append(">");
						   option.append(optionList.get(i));
						   option.append("</option>");
						}
					}
					option.append("</select>");
					requestedChange.add(option.toString());
				}
					else
					{
						if(currentStatus!=null && !"".equals(currentStatus) && !"null".equals(currentStatus))
							requestedChange.addElement(currentStatus);
						else
							requestedChange.addElement("");
					}
				}
			}
		}
		if(mode==null || !mode.equals("Edit"))
		{//if task in view mode show the attribute value
			while(objItr.hasNext())
			{
				Map objMap = (Map)objItr.next();
				String reqChangeValue = (String)objMap.get(ATTRIBUTE_REQUESTED_CHANGE);
				//Start:Added for the Bug 359606
                //IR-037806 - Starts
                String connectionId = (String)objMap.get("id[connection]");
                if (connectionId == null || "".equals(connectionId)) {
                    requestedChange.addElement("");
                } else {
                //IR-037806 - Ends
                    if(reqChangeValue==null){
                        //String connectionId = (String)objMap.get("id[connection]"); //Commented for IR-037806
                        DomainRelationship doRel = new DomainRelationship(connectionId);
                        reqChangeValue = doRel.getAttributeValue(context,ATTRIBUTE_REQUESTED_CHANGE);
                    }
                    String strreqChangeValue = i18nNow.getRangeI18NString(ATTRIBUTE_REQUESTED_CHANGE, reqChangeValue, strLanguage);
                    //End:Added for the Bug 359606
                    String sAIRelId		= (String)objMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Start:Modified for the Bug 359606
                    if (strreqChangeValue != null && !strreqChangeValue.equals(""))
                    {
                        requestedChange.addElement(strreqChangeValue);
                    }
                    //End:Modified for the Bug 359606
                    else
                    {
                        if((!sAIRelId.equals("")) && (!sAIRelId.equals(null)))
                        {
                            DomainRelationship domRelObj=new DomainRelationship(sAIRelId);

                            domRelObj.open(context);
                            String currentStatus = domRelObj.getAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE);
                            domRelObj.close(context);
                            if(currentStatus!=null && !"".equals(currentStatus) && !"null".equals(currentStatus))
                                requestedChange.addElement(currentStatus);
                            else
                                requestedChange.addElement("");
                        }
                        else
                            requestedChange.addElement("");
                    }
                }//IR-037806
			}
		}
		return requestedChange;
    }

	/**
	 * Updates the Range Values for Attribute RequestedChange Based on User Selection
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap - a HashMap containing the following keys, "relId","RequestedChange"
	 * @return	int
	 * @throws	Exception if operation fails
	 * @since   Common X3
	 */
  public int updateRequestedChangeValues(Context context, String[] args) throws Exception
	{
		int intReturn=0;
	    String strLanguage  =  context.getSession().getLanguage();
		i18nNow i18nnow = new i18nNow();
		String strAlertMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Common.Alert.EditAll");
		String strAlertMessage2 = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.Common.Alert.EditAllWithNewRevision");
	    HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get(SELECT_PARAM_MAP);
        HashMap requestMap = (HashMap)programMap.get(SELECT_REQUEST_MAP);
		String objId = (String)paramMap.get("objectId");
		String changeObjId = (String)requestMap.get("objectId");
		DomainObject domObj = new DomainObject(objId);
		String currentState = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
        String sAttRequestedChange=PropertyUtil.getSchemaProperty(context, "attribute_RequestedChange");
        String sRelId = (String)paramMap.get(SELECT_REL_ID);
        String strNewRequestedChangeValue  = (String)paramMap.get(SELECT_NEW_VALUE);
        DomainRelationship domRelObj=new DomainRelationship(sRelId);
		strLanguage  =  (String)programMap.get("languageStr");
		Locale strLocale = context.getLocale();
		if(currentState.equalsIgnoreCase(STATE_ECPART_RELEASE))
		{
			String[] inputArgs = new String[2];

			inputArgs[0] = objId;
			inputArgs[1] = changeObjId;
			String strNewPartId = (String) getIndirectAffectedItems(context, inputArgs);
			if(domObj.isLastRevision(context) && strNewPartId == null)
			{
				if (strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale,"emxFramework.Range.Requested_Change.For_Update"))||strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale,"emxFramework.Range.Requested_Change.For_Revise"))||strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale,"emxFramework.Range.Requested_Change.None"))||strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale,"emxFramework.Range.Requested_Change.For_Obsolescence")))
			{
				domRelObj.setAttributeValue(context,sAttRequestedChange,strNewRequestedChangeValue);
				intReturn =0;
			}
			if(strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.For_Release")))
			{
				emxContextUtil_mxJPO.mqlNotice(context,strAlertMessage);
				intReturn =1;
			}
	    }
			else
			{
				emxContextUtil_mxJPO.mqlNotice(context,strAlertMessage2);
				intReturn =1;
			}
	    }

		if(!(currentState.equalsIgnoreCase(STATE_ECPART_RELEASE)))
		{
			//if(strNewRequestedChangeValue.equalsIgnoreCase(i18nNow.getI18nString("emxFramework.Range.Requested_Change.For_Release","emxFrameworkStringResource",strLocale))||strNewRequestedChangeValue.equalsIgnoreCase(i18nNow.getI18nString("emxFramework.Range.Requested_Change.None","emxFrameworkStringResource",strLocale)))
			if(strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.For_Release")))
			{
				domRelObj.setAttributeValue(context, sAttRequestedChange, strNewRequestedChangeValue);
				intReturn =0;
			}
			//if(strNewRequestedChangeValue.equalsIgnoreCase(i18nNow.getI18nString("emxFramework.Range.Requested_Change.For_Revise","emxFrameworkStringResource",strLocale))||strNewRequestedChangeValue.equalsIgnoreCase(i18nNow.getI18nString("emxFramework.Range.Requested_Change.For_Obsolescence","emxFrameworkStringResource",strLocale)))
			if(strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.For_Revise"))||strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.For_Obsolescence"))||strNewRequestedChangeValue.equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.Range.Requested_Change.None")))
			{
				emxContextUtil_mxJPO.mqlNotice(context,strAlertMessage);
				intReturn =1;
			}
		}

	   return intReturn;
  }

	/**
 * This method returns true if a user has permission on the command else return false.
 * @param	context the eMatrix <code>Context</code> object
 * @param	args holds a HashMap containing the following entries:
 * paramMap - a HashMap containing the Change Obj Id
 * @return	boolean
 * @throws	Exception if operation fails
 * @since   Common X3
 */

public boolean showDelegateCommand (Context context,String[] args) throws Exception
	{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		// Retireving the Change Object Id
		String strParentId = (String)programMap.get(SELECT_OBJECT_ID);
		String strRelAssignedEC= DomainConstants.RELATIONSHIP_ASSIGNED_EC;

		MapList relBusObjPageList = new MapList();
		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
		this.setId(strParentId);

		boolean bShowCommand = false ;
		boolean bIsOwner = false ;
		boolean bIsAssignee = false ;
		boolean bHasAffectedItems = false ;

		String strMQLCommand ;
		String strMQLCommandResult =  "";

		MQLCommand prMQL  = new MQLCommand();

		// Getting the Assignees of the Change Object
		relBusObjPageList = getRelatedObjects(context,
											  strRelAssignedEC,
											  DomainConstants.QUERY_WILDCARD,
											  objectSelects,
											  relSelects,
											  true,
											  false,
											  (short) 1,
											  DomainConstants.EMPTY_STRING,
											  DomainConstants.EMPTY_STRING);

		// Getting the Login Person and his ID
		com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
        loginPerson = com.matrixone.apps.common.Person.getPerson(context);
		String strLoginPersonID = (String)loginPerson.getObjectId();

		// Getting the name of the Logged in Person
		DomainObject domLoginPerson = new DomainObject(strLoginPersonID);
		String strLoginPersonName = (String)domLoginPerson.getInfo(context,DomainConstants.SELECT_NAME);

		// Getting the owner of the Change Object
		DomainObject domChangeObject = new DomainObject(strParentId);
		String strChangeObjOwner = (String)domChangeObject.getOwner(context).toString();

		//Added for IR-226720 start
		StringList objectSelects1 = new StringList();
		objectSelects1.add(DomainConstants.SELECT_CURRENT);             
		objectSelects1.add(DomainConstants.SELECT_POLICY);             
		
		Map map = getInfo(context, objectSelects1);                     		
		String strState =  map.get(DomainObject.SELECT_CURRENT).toString();		
		String strPolicy =  map.get(DomainObject.SELECT_POLICY).toString();	
		if((DomainConstants.POLICY_ECO.equals(strPolicy)) && (DomainConstants.STATE_ECO_REVIEW.equals(strState)
				|| DomainConstants.STATE_ECO_RELEASE.equals(strState) || DomainConstants.STATE_ECO_IMPLEMENTED.equals(strState)
					|| DomainConstants.STATE_ECO_CANCELLED.equals(strState))) {
			return false;
		}		
		//Added for IR-226720 End
		
		// Setting flag to true if the Logged in person is the Owner of Change Object
		if (strChangeObjOwner.equals(strLoginPersonName)||strChangeObjOwner.equalsIgnoreCase(strLoginPersonName))
		{
			bIsOwner = true;
			bShowCommand = true;
		}
		else
		{
			Iterator itr = relBusObjPageList.iterator();
			while (itr.hasNext())
			{
				Map mapObject = (Map) itr.next();
				// Getting the Object IDs of the Assignees Listed as of on the Asignee List Page
				String strAssigneeId = (String)mapObject.get(DomainConstants.SELECT_ID);

				if (strAssigneeId.equals(strLoginPersonID))
				{
					bIsAssignee = true;
					// getting the "Assigned EC" Rel ID
					String strAssECRelId = (String)mapObject.get(DomainConstants.SELECT_RELATIONSHIP_ID);

					prMQL.open(context);
					strMQLCommand = "print connection $1 select $2 dump";
					prMQL.executeCommand(context,strMQLCommand, strAssECRelId, "frommid.id");
					strMQLCommandResult = prMQL.getResult();
					prMQL.close(context);

					if(!strMQLCommandResult.equals("\n"))
					{
						bHasAffectedItems = true ;
					}
					// Show the command only is the Logged in Person is an Assignee and has Affected Items also
					if (bIsAssignee & bHasAffectedItems)
					{
						bShowCommand = true;
					}
				}
			}
		}
		return bShowCommand;
	}

/**
 * This method returns Vector containing true/false values determining is a user has access on a
 * specific Row of a Table.
 * @param	context the eMatrix <code>Context</code> object
 * @param	args holds a HashMap containing the following entries:
 *          paramMap - a HashMap containing the objectList, paramList and objectId
 * @return	Vector
 * @throws	Exception if operation fails
 * @since   Common X3
 */


public Vector showCheckBoxInDelegate(Context context, String[] args)
    throws Exception
    {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        // getting the MapList of the objects.
        MapList objList     = (MapList)programMap.get("objectList");
        // getting the HashMap for the key "paramList"
        HashMap paramMap    = (HashMap)programMap.get("paramList");
        // getting the objectId from the Map
        String objectId     = (String) paramMap.get("objectId");

		int listSize = objList.size();
        Vector columnVals   = new Vector(listSize);

		try
		{
			com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) com.matrixone.apps.common.Person.getPerson(context);
			String strLoginPersonID = (String)person.getObjectId();

			DomainObject domChangeObject = new DomainObject(objectId);
			String strChangeObjOwner = (String)domChangeObject.getOwner(context).toString();

			DomainObject domLoginPerson = new DomainObject(strLoginPersonID);
			String strLoginPersonName = (String)domLoginPerson.getInfo(context,DomainConstants.SELECT_NAME);

			Iterator itr = objList.iterator();
			if (strChangeObjOwner.equals(strLoginPersonName)||strChangeObjOwner.equalsIgnoreCase(strLoginPersonName))
			{
				for (int i=0;i<objList.size();i++ )
				{
					columnVals.add("true");
				}

			}else
			{
				while (itr.hasNext())
				{
					Map mapObject = (Map) itr.next();
					String strObjListIds = (String)mapObject.get("id");

					if (strLoginPersonID.equals(strObjListIds))
					{
						columnVals.add("true");
					}
					else
					{
						columnVals.add("false");
					}
				}
			}
		}catch (Exception e)
		{
			throw e;
		}
        return columnVals;
    }

  /**
 * Returns a true boolean flag if the logged in person id the owner of Change Object.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds - String of Change Object's ID
 * Return boolean
 * @throws Exception if the operation fails.
 * @since Common X3.
*/

   public String isContextPersonOwner(Context context, String[] args)
	{
		String strIsOwner = "false";

		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			// Obtaining the Parent Change Object Id
			String strChangeObjID = (String)programMap.get("strChangeObjID");

			// Getting the Login Person and his ID
			com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
			loginPerson = com.matrixone.apps.common.Person.getPerson(context);
			String strLoginPersonID = (String)loginPerson.getObjectId();

				// Getting the name of the Logged in Person
			DomainObject domLoginPerson = new DomainObject(strLoginPersonID);
			String strLoginPersonName = (String)domLoginPerson.getInfo(context,DomainConstants.SELECT_NAME);

			// Getting the owner of the Change Object
			DomainObject domChangeObject = new DomainObject(strChangeObjID);
			String strChangeObjOwner = (String)domChangeObject.getOwner(context).toString();

				// Setting flag to true if the Logged in person is the Owner of Change Object
				if (strChangeObjOwner.equals(strLoginPersonName)||strChangeObjOwner.equalsIgnoreCase(strLoginPersonName))
				{
					strIsOwner = "true";
				}

			}
			catch (Exception e )
				{
					System.out.println(e.toString());
				}
			return strIsOwner;
	}


/**
 * Returns a where clause based on the Affected Items filters.
 * @param	context the eMatrix <code>Context</code> object
 * @return	String
 * @throws	Exception if operation fails
 * @since   Common X3
 */
 public String getAffectedItemsFilterClause(Context context)
    throws Exception
    {
		String strAssigneeName = "";
		if (!sGlobalAssigneeName.equalsIgnoreCase(SELECT_ALL))
		{
			DomainObject dobj = new DomainObject(sGlobalAssigneeName);
			strAssigneeName = dobj.getInfo(context , DomainConstants.SELECT_NAME);
		}

		String sWhereClauseAssigneeFilter ="";
		String sWheresWhereClauseRequestedChangeFilter = "";
		if (!sGlobalRequestedChangeFilter.equals(SELECT_ALL))
		{
			sWheresWhereClauseRequestedChangeFilter = "attribute["+ATTRIBUTE_REQUESTED_CHANGE+"] == " + "\""+sGlobalRequestedChangeFilter+"\"";
		}
		if (!strAssigneeName.equals(""))
		{
			sWhereClauseAssigneeFilter = "tomid.fromrel.from.name  =='"+strAssigneeName+"'";
		}

		StringBuffer bufWhereClause = new StringBuffer();

		if(!sGlobalAssigneeName.equalsIgnoreCase(SELECT_ALL))
		{
			if (!sWheresWhereClauseRequestedChangeFilter.equals(""))
			{
				bufWhereClause.append("((");
				bufWhereClause.append(sWheresWhereClauseRequestedChangeFilter);
				bufWhereClause.append(") && (");
			}
			else
			{
				bufWhereClause.append("(");
			}
			bufWhereClause.append(sWhereClauseAssigneeFilter);
			bufWhereClause.append("))");
		}
		else
		{
			bufWhereClause.append(sWheresWhereClauseRequestedChangeFilter);
		}
		return bufWhereClause.toString();
    }

/*
 * Returns a true boolean flag if the logged in person id an Assignee of the Change Object.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds - String of Change Object's ID
 * Return boolean
 * @throws Exception if the operation fails.
 * @since Common X3.
*/
public String isContextPersonAssignee(Context context, String[] args)
	{
		String strIsAssignee = "false";
		try
		{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		// Obtaining the Parent Change Object Id
		String strChangeObjID = (String)programMap.get("strChangeObjID");
		//String strChangeObj = strChangeObjID[0];

			String strRelAssignedEC= DomainConstants.RELATIONSHIP_ASSIGNED_EC;

			MapList AssigneeList = new MapList();
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			this.setId(strChangeObjID);

			// Getting the Assignees of the CHange Object
			AssigneeList = getRelatedObjects(context,
												  strRelAssignedEC,
												  DomainConstants.QUERY_WILDCARD,
												  objectSelects,
												  relSelects,
												  true,
												  false,
												  (short) 1,
												  DomainConstants.EMPTY_STRING,
												  DomainConstants.EMPTY_STRING);

			// Getting the Login Person and his ID
			com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
			loginPerson = com.matrixone.apps.common.Person.getPerson(context);
			String strLoginPersonID = (String)loginPerson.getObjectId();

			Iterator itr = AssigneeList.iterator();
			while (itr.hasNext())
			{
				Map mapObject = (Map) itr.next();
				// Getting the Object IDs of the Assignees Listed as of on the Asignee List Page
				String strAssigneeId = (String)mapObject.get(DomainConstants.SELECT_ID);

				if (strAssigneeId.equals(strLoginPersonID))
				{
					strIsAssignee = "true";
				}
			}

		}
		catch (Exception e )
		{
			System.out.println(e.toString());
		}
		return strIsAssignee;
	}

/**
 * Split Delegate the Assignments of one or more selected Affected Items to a single Assignee
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds objectId.
 * @throws Exception if the operation fails.
 * @since EC-V6R2009-1.
 * @deprecated since V6R2017x for Function_FUN059293 and replaced by {@link ${CLASS:enoEngChange}#splitDelegateAssignees(Context context, String[] args)}
*/

public void splitDelegateAssignees(Context context, String[] args)throws Exception
    {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);

		// Obtaining the Change Object Id
		String[] strChangeObjID = (String[])programMap.get("strChangeObjID");

		// Obtaining the Object Id of the New Assignee
		String[] strNewAssigneeID = (String[])programMap.get("strNewAssigneeID");

		// Obtaining the "Affected Item" Rel Ids of the Affected Items selected initially on the List Page
		String[] arrAffectedItemsRelIds = (String[])programMap.get("arrAffectedItemRelID");

		// Getting the Login Person and his ID
		com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
		loginPerson = com.matrixone.apps.common.Person.getPerson(context);
		String strLoginPersonID = (String)loginPerson.getObjectId();

		try{

			// Iterating over the List of Rel Ids of the Objects initially selected on the Affected Items List Page
			for (int k=0; k<arrAffectedItemsRelIds.length; k++)
			{
				ContextUtil.startTransaction(context, true);

				// Taking the Affected Items Rel Ids in String Tokenizer for getting them in usable format. Tokenizing on the basis of "," values
				StringTokenizer stzRelId = new StringTokenizer(arrAffectedItemsRelIds[k],",", false);

				// Taking the Object Ids in String Tokenizer for getting them in usable format. Tokenizing on the basis of "," values

				while(stzRelId.hasMoreElements()) // For each Rel Id get the corresponding Obj Id
				{
					// Converting the tokens to String
					String strTokenRelId = stzRelId.nextToken().toString();
					String strTempString1 = StringUtils.replace(strTokenRelId, "]", "");
					// Obtaining the Affected Item Relationship id in usable format
					String strFinalAffectedItemRelId = StringUtils.replace(strTempString1, "[", "");

					DomainObject domChangeObj = new DomainObject(strChangeObjID[0]);

						// Getting the name of the Logged in Person
					DomainObject domLoginPerson = new DomainObject(strLoginPersonID);
					String strLoginPersonName = (String)domLoginPerson.getInfo(context,DomainConstants.SELECT_NAME);

					// Getting the owner of the Change Object
					String strChangeObjOwner = (String)domChangeObj.getOwner(context).toString();


					if (((getTomidFromRelationshipFromID(context, strFinalAffectedItemRelId, DomainConstants.RELATIONSHIP_ASSIGNED_EC).equals(strLoginPersonID)) || strChangeObjOwner.equals(strLoginPersonName))&&(!strNewAssigneeID[k].equals(strLoginPersonID)))
					{
					String whrClause	= "id"+"=='"+strNewAssigneeID[k]+"'";

					MapList mlPersons = domChangeObj.getRelatedObjects( context,
																		DomainConstants.RELATIONSHIP_ASSIGNED_EC,
																		DomainConstants.TYPE_PERSON,
																		new StringList(DomainConstants.SELECT_ID),
																		new StringList (DomainConstants.SELECT_RELATIONSHIP_ID),
																		true,
																		false,
																		(short)1,
																		whrClause,
																		"");

					Iterator mlPersonsItr = mlPersons.iterator();
					String strAssignedECRelId = "";
					while (mlPersonsItr.hasNext())
					{
						Map mapPersonObject = (Map) mlPersonsItr.next();
						strAssignedECRelId = (String)mapPersonObject.get("id[connection]");
					}

					StringList slExistingAssignedECRelId =	getTomids(	context,
																strFinalAffectedItemRelId);
					Iterator itrExistingAssignedECRelId =  slExistingAssignedECRelId.iterator();

					String strExistingAssignedECRelId= (String)itrExistingAssignedECRelId.next();
					//Disconnecting the Assigned Affected Item Rel originally.
					if(!("".equals(strExistingAssignedECRelId)))
					{
						disconnect(context,strExistingAssignedECRelId);
					}

					if("".equals(strAssignedECRelId))
					{
						DomainRelationship strDR =
						DomainRelationship.connect(context,
												   new DomainObject(strNewAssigneeID[k]),
												   DomainConstants.RELATIONSHIP_ASSIGNED_EC,
												   new DomainObject(strChangeObjID[0]));
						strAssignedECRelId = strDR.toString();
					}


					 // Creating new ReltoRel START
					connect( context,
										RELATIONSHIP_ASSIGNEED_AFFECTED_ITEM,
										strAssignedECRelId,
										strFinalAffectedItemRelId,
										false,
										false);
				 }
			}
			}
			ContextUtil.commitTransaction(context);
		   }catch (Exception e)
			{
				ContextUtil.abortTransaction(context);
				throw (new FrameworkException(e));
			}
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
    public static MapList getReleasedSearchResults(Context context, String[] args)
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

            String strState = DomainConstants.STATE_PART_RELEASE ;

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
            StringBuffer sbWhereExp = new StringBuffer(150);
            boolean bStart=true;
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
			if (bStart) {
					sbWhereExp.append(SYMB_OPEN_PARAN);
					bStart = false;
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
 * Returns a String having the Assigned EC Rel ID of the logged in person connected to the Change Object.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds - String of Change Object's ID
 * Return String
 * @throws Exception if the operation fails.
 * @since Common X3.
 */
  public String getContextPersonAssigneeRelID(Context context, String[] args)
	{
		String strAssigneeRelID = "";
		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			// Obtaining the Parent Change Object Id
			String strChangeObjID = (String)programMap.get("strChangeObjID");

			String strRelAssignedEC= DomainConstants.RELATIONSHIP_ASSIGNED_EC;

			MapList AssigneeList = new MapList();
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			this.setId(strChangeObjID);

			// Getting the Assignees of the CHange Object
			AssigneeList = getRelatedObjects(context,
											  strRelAssignedEC,
											  DomainConstants.QUERY_WILDCARD,
											  objectSelects,
											  relSelects,
											  true,
											  false,
											  (short) 1,
											  DomainConstants.EMPTY_STRING,
											  DomainConstants.EMPTY_STRING);

			// Getting the Login Person and his ID
			com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
			loginPerson = com.matrixone.apps.common.Person.getPerson(context);
			String strLoginPersonID = (String)loginPerson.getObjectId();

			Iterator itr = AssigneeList.iterator();
			while (itr.hasNext())
			{
				Map mapObject = (Map) itr.next();
				// Getting the Object IDs of the Assignees Listed as of on the Asignee List Page
				String strAssigneeId = (String)mapObject.get(DomainConstants.SELECT_ID);

				if (strAssigneeId.equals(strLoginPersonID))
				{
					strAssigneeRelID = (String)mapObject.get(DomainConstants.SELECT_RELATIONSHIP_ID);
				}
			}

		}
		catch (Exception e )
		{
			System.out.println(e.toString());
		}
		return strAssigneeRelID;
	}
	/**
	 * Checks Whether to display Requested Column in edit mode or not
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap - a HashMap containing the following keys, "mode","ObjectId"
	 * @return	Boolean
	 * @throws	Exception if operation fails
	 * @since   Common X3
	 */
  public Boolean showReqChangeForMECO(Context context, String[] args) throws Exception
	{
	    boolean status=true;
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
  		String objid = (String)programMap.get("objectId");
  		String mode = (String)programMap.get("mode");
		DomainObject domObj = new DomainObject(objid);
		String strType = domObj.getInfo(context, DomainConstants.SELECT_TYPE);
		if(TYPE_MECO.equals(strType) && "edit".equalsIgnoreCase(mode)){
         status=false;
		}

		return Boolean.valueOf(status);

       }
/**
 * Returns a String having the Assigned EC Rel ID of the logged in person connected to the Change Object.
 * @param context the eMatrix <code>Context</code> object.
 * @param String of Change Object's ID
 * @param String of passed Person's Object ID
 * Return String
 * @throws Exception if the operation fails.
 * @since Common X3.
 */
  public String getPersonAssigneeRelID(Context context, String strChangeObjID, String strPersonID)
	{
		String strAssigneeRelID = "";
		try
		{
			String strRelAssignedEC= DomainConstants.RELATIONSHIP_ASSIGNED_EC;

			MapList AssigneeList = new MapList();
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
			String strObjectWhere = "id=="+strPersonID;
			this.setId(strChangeObjID);
			// Getting the Assignees of the CHange Object
			AssigneeList =  getRelatedObjects(context,
											  strRelAssignedEC,
											  DomainConstants.QUERY_WILDCARD,
											  objectSelects,
											  relSelects,
											  true,
											  false,
											  (short) 1,
											  strObjectWhere,
											  DomainConstants.EMPTY_STRING);

			if (AssigneeList.size() > 0 )
			{
				Iterator itr = AssigneeList.iterator();
				while (itr.hasNext())
				{
					Map mapObject = (Map) itr.next();
					// Getting the Object IDs of the Assignees Listed as of on the Asignee List Page
					strAssigneeRelID = (String)mapObject.get(DomainConstants.SELECT_RELATIONSHIP_ID);
				}
			}
		}
		catch (Exception e )
		{
			System.out.println(e.toString());
		}
		return strAssigneeRelID;
	}
    /**
     * Displays the policy drop down based on the change type and property settings.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following Strings, "objectId".
     * requestMap - a HashMap containing the request.
     * @return Object - String object which contains the policy drop down.
     * @throws Exception if operation fails.
     * @since Common X3
     */

    public Object getPolicy(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) paramMap.get("objectId");
        String strSymType = (String) requestMap.get("type");
        String languageStr = (String) paramMap.get("languageStr");
        String suiteKey = (String) requestMap.get("suiteKey");

        String strType = "";

        if (strSymType != null && strSymType.length() > 0 && !"null".equals(strSymType))
        {
        	int intIndex = strSymType.indexOf(",");
        	if (intIndex != -1)
        	{
        		strSymType = strSymType.substring(intIndex + 1, strSymType.length());
			}
	 	}
        if (strSymType != null && strSymType.length()>0 && !"null".equals(strSymType))
        {
            strType = PropertyUtil.getSchemaProperty(context, strSymType);
        }
        StringBuffer sbPolicy = new StringBuffer("<select name=\"Policy\">");
        String sCurrentPolicyName = "";
        if (strObjectId!=null && strObjectId.length() > 0)
        {
            setId(strObjectId);
            sCurrentPolicyName = getInfo(context,SELECT_POLICY);
            strType = getInfo(context, SELECT_TYPE);
        }
        String strMode = (String) requestMap.get("mode");
        //if in edit mode, can only change to the same type of PolicyClassification
        String currentPolicyClassification = "";
        if(sCurrentPolicyName.equals("") || "edit".equalsIgnoreCase(strMode))
        {
        	try {
                if (!sCurrentPolicyName.equals(""))
                {
        		    currentPolicyClassification = FrameworkUtil.getPolicyClassification(context, sCurrentPolicyName);
                }
                else //create page - get default policy setting
                {
                    strMode = "create";
                    // 374591
                    String policy = "";
                    //construct property key based on type
                    //String propKey = "emx" + suiteKey + ".Create";

                    //determine kind of object type based on the symbolic name for this instance
                    //grab the key after the _ in the symbolic name
                    //propKey += strSymType.substring(strSymType.indexOf("_") + 1, strSymType.length());
                    policy = "policy_" + strSymType.substring(strSymType.indexOf("_") + 1, strSymType.length());

                    //propKey += "PolicyDefault";
                    //sCurrentPolicyName = PropertyUtil.getSchemaProperty(context,(String)FrameworkProperties.getProperty(propKey));
                    sCurrentPolicyName = PropertyUtil.getSchemaProperty(context,policy);
        		    currentPolicyClassification = FrameworkUtil.getPolicyClassification(context, sCurrentPolicyName);
                }
        	}
        	catch (Exception err){
        		// PolicyClassification not set, default to Static Approval
        		//currentPolicyClassification = "StaticApproval";
        		  currentPolicyClassification = "DynamicApproval";

        	}
        	//Get the policies associated with the ECR
            MapList policies = mxType.getPolicies(context, strType, false); //getPolicies(context);

        	Iterator listItr = policies.iterator();
        	Map object = null;
        	String strPolicyName = "";
        	String sOtherPolicyName = "";

        	//Construct the policy dropdown
        	while (listItr.hasNext())
        	{
        		object = (Map) listItr.next();
        		String sPolicySelected ="selected=\"true\"";
        		strPolicyName = (String) object.get(SELECT_NAME);
        		String policyClassification = "";
        		try {
                    policyClassification = FrameworkUtil.getPolicyClassification(context, strPolicyName);
        		}
        		catch (Exception err){
            		// PolicyClassification not set, default to Static Approval
        			policyClassification = "StaticApproval";
        		}

        		if (policyClassification.equals(currentPolicyClassification))
        		{
        			sOtherPolicyName=i18nNow.getAdminI18NString("Policy", strPolicyName ,languageStr);
        			sbPolicy.append("<option value=\""+strPolicyName+"\" "+((strPolicyName.equals(sCurrentPolicyName))?sPolicySelected:"")+">"+sOtherPolicyName+"</option>");
        		}
        	}
        	sbPolicy.append("</select>");
        }

        String strPolicy = "";
        if("create".equals(strMode) || "edit".equalsIgnoreCase(strMode)) {
            strPolicy = sbPolicy.toString();
        } else {
            strPolicy = i18nNow.getAdminI18NString("Policy", sCurrentPolicyName.trim() ,languageStr);
        }
        return strPolicy;
 }


/**
  * Gets connected object connected at from/to side of rel to rel relationship.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param relid relationship id from which reltorel is connected
  * @param isfrommid true to get to/from object from frommid else get from tomid
  * @param getFrom true to get from object id else it will get to object id
  * @return the StringList of Object Id for further processing
  * @throws FrameworkException if the operation fails
  * @since Common X3
*/
 	    public StringList getFromTomidsToFromObjid(Context context, String relid,boolean isfrommid, boolean getFrom)
 	        throws FrameworkException
 	    {

 			ContextUtil.startTransaction(context, true);
 			String strReult="";
 			StringList slFromRelId= new StringList();
 			MqlUtil.mqlCommand(context, "verb $1", "on");

             try
             {
				       ContextUtil.pushContext(context, null, null, null);
				       if(isfrommid)
                        {
									if(getFrom)
									{
										 strReult= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", relid, "frommid.from.id", "|");
										 slFromRelId				= FrameworkUtil.split(strReult,"|");


									}
								   else
									   {

										  strReult= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", relid, "frommid.to.id", "|");
										  slFromRelId				= FrameworkUtil.split(strReult,"|");


									   }
						  }
						  else
						  {
								  if(getFrom)
									{
									   strReult= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", relid, "tomid.from.id", "|");
									   slFromRelId				= FrameworkUtil.split(strReult,"|");


										}
									  else
									  {

										strReult= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", relid, "tomid.to.id", "|");
										 slFromRelId				= FrameworkUtil.split(strReult,"|");
							          }

			      			}

           			ContextUtil.commitTransaction(context);
 			}
             catch (Exception e)
             {
                 // Abort transaction.
                 ContextUtil.abortTransaction(context);
                 throw new FrameworkException(e);
             }
            finally
		    {
				ContextUtil.popContext(context);
			}

 		return slFromRelId;
       }


/**
  * Gets connected reltorel ids to the relationship.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param relid relationship id from which reltorel is connected
  * @return the StringList of reltorel id for further processing
  * @throws FrameworkException if the operation fails
  * @since Common X3
*/
 	    public StringList getTomids(Context context, String relid)
 	        throws FrameworkException
 	    {

 			ContextUtil.startTransaction(context, true);
 			String Res;
 			StringList slmidId;
            MqlUtil.mqlCommand(context, "verb $1", "on");

             try
             {
			      ContextUtil.pushContext(context, null, null, null);
                  Res= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", relid, "tomid.id", "|");
 				  slmidId				= FrameworkUtil.split(Res,"|");
                  ContextUtil.commitTransaction(context);
 			}
            catch (Exception e)
             {
                 // Abort transaction.
                 ContextUtil.abortTransaction(context);
                 throw new FrameworkException(e);
             }
            finally
		    {
				ContextUtil.popContext(context);
			}

 		return slmidId;
       }

 /**
 * Disconnect the two objects/relationship connected with the given relationship id.
 *
 * @param context the eMatrix <code>Context</code> object
 * @param relationshipId id of the relationship to remove
 * @throws FrameworkException if the operation fails
 * @since Common X3
 */
    public static void disconnect(Context context, String relationshipId)
        throws FrameworkException
    {
        ContextUtil.startTransaction(context, true);
        MqlUtil.mqlCommand(context, "verb $1", "on");

        try
        {
             ContextUtil.pushContext(context, null, null, null);
             String strCommand = "delete connection $1";
             MqlUtil.mqlCommand(context,strCommand, relationshipId);
             // commit and free transaction
             ContextUtil.commitTransaction(context);
        }
        catch (Exception e)
        {
            throw (new FrameworkException(e));
        }
       finally
	   {
		 ContextUtil.popContext(context);
	   }

    }


	/**
	* Connects two relationship using the given relationship type.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param srelationship name the object/relationship to connect from
	* @param fromId the object/relationship to connect from
	* @param toId the object/relationship to connect to
	* @param isFromObj true if from side is Object
	* @param isToObj true if to side is Object
	* @return the connection id for further processing
	* @throws FrameworkException if the operation fails
	* @since Common X3
	*/
	    public String connect(Context context, String srelationship,String fromId,String toId,boolean isFromObj,boolean isToObj)
	        throws FrameworkException
	    {
			String sFrom;
			String sTo;
			String Res;
			String strebomsubstititeId;
			ContextUtil.startTransaction(context, true);
            MqlUtil.mqlCommand(context, "verb $1", "on");

            try {
            	ContextUtil.pushContext(context, null, null, null);
            	sFrom = isFromObj ? "from": "fromrel";
            	sTo = isToObj ? "to": "torel";


            	StringBuffer cmd = new StringBuffer();
            	cmd.append("add connection $1 ");
                cmd.append(sFrom);
            	cmd.append(" $2 ");
                cmd.append(sTo);
            	cmd.append(" $3");
                Res= MqlUtil.mqlCommand(context, cmd.toString(), srelationship, fromId, toId);
				
				//Getting the created Relationship id
				int findx                       = Res.indexOf("'");
				int lindx                       = Res.lastIndexOf("'");
				strebomsubstititeId      = Res.substring(findx+1,lindx);
                // End successful transaction.
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                // Abort transaction.
                ContextUtil.abortTransaction(context);
                throw new FrameworkException(e);
            } finally {
				 ContextUtil.popContext(context);
			}
	return strebomsubstititeId;
       }

/*
* Disconnect many existing connections.
*
* @param context the eMatrix <code>Context</code> object
* @param relationshipIds a list of relationship ids to remove
* @throws FrameworkException if the operation fails
* @since Common X3
*/
    public static void disconnect(Context context, String[] relationshipIds, boolean isReltoRel)
        throws FrameworkException
    {

    // start a write transaction
        //377009:do not push context before removing affected items.
       	//ContextUtil.pushContext(context);
        ContextUtil.startTransaction(context, true);
        MqlUtil.mqlCommand(context, "verb on");
        try
        {
            if(isReltoRel)
            {
				disconnect(context,relationshipIds);
		     }
		     else
		     {

                DomainRelationship.disconnect(context,relationshipIds);
			 }

		ContextUtil.commitTransaction(context);

        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            throw new FrameworkException(e);
        }
       //finally
	   //{
		 //377009:do not push context before removing affected items.
		 //ContextUtil.popContext(context);
		 //}

    }

    /**
    * Disconnect many existing connections.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param relationshipIds a list of relationship ids to remove
    * @throws FrameworkException if the operation fails
    * @since Common X3
    */
    public static void disconnect(Context context, String[] relationshipIds)
        throws FrameworkException
    {

    // start a write transaction
        ContextUtil.startTransaction(context, true);
        MqlUtil.mqlCommand(context, "verb on");
        try
        {
            ContextUtil.pushContext(context);
            Iterator itr = Arrays.asList(relationshipIds).iterator();
            while (itr.hasNext())
            {
                String relationshipId = (String) itr.next();

                disconnect(context,relationshipId);
                String strCommand = "delete connection $1";
                MqlUtil.mqlCommand(context,strCommand, relationshipId);
            }

            // commit and free transaction
            ContextUtil.commitTransaction(context);
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            throw new FrameworkException(e);
        }
        finally
	{
	    ContextUtil.popContext(context);
	}

    }

	/**
	 * getTypeFilterOptions(), Method to execute for populating the
	 * 'Type' Filter Options.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap
	 * @return StringList.
     * @since Common X3
	 * @throws Exception if the operation fails.
	*/
	public HashMap getTypeFilterOptions(Context context, String args[])
		throws Exception {

		HashMap typeMap				= new HashMap();
		try
		{

			HashMap ProgramMap         = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap 			= (HashMap)ProgramMap.get("requestMap");
			String strDisplay	= (String) requestMap.get("ENCAffectedItemsTypeFilter");
			HashMap ColumnMap=(HashMap)ProgramMap.get("columnMap");
			HashMap Settings          = (HashMap)ColumnMap.get("settings");
			StringList fieldChoices 		= new StringList();
			StringList fieldDisplayChoices 	= new StringList();
            String strLanguage  = context.getSession().getLanguage();
            Locale strLocale = context.getLocale();
			String strAllDefaultValue=EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, strLocale, "emxComponents.Filter.All.Type");
            String strAllActualValue=EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, new Locale("en"),"emxComponents.Filter.All.Type");

			DomainObject doChange = new DomainObject((String)requestMap.get("objectId"));
			String strPolicy = doChange.getInfo(context, DomainConstants.SELECT_POLICY);
			String strPolicyClassification = FrameworkUtil.getPolicyClassification(context, strPolicy);

			if(strDisplay == null)
				strDisplay = strAllActualValue;

			fieldDisplayChoices.add(strAllDefaultValue);
			fieldChoices.add(strAllActualValue);
			String strRelName = "";
			String strAffectedItemToTypes = "";
			//if (strPolicyClassification.equals("DynamicApproval"))
			//{
				strAffectedItemToTypes = MqlUtil.mqlCommand(context, "print relationship $1 select $2 dump", RELATIONSHIP_AFFECTED_ITEM, "totype");
			//}
			/*else
			{
				if(doChange.isKindOf(context,DomainConstants.TYPE_ECO))  //checking whether the change obj is ECO or ECR
				{
					strAffectedItemToTypes =  MqlUtil.mqlCommand(context, "list relationship \"" + DomainConstants.RELATIONSHIP_NEW_SPECIFICATION_REVISION+ "," + DomainConstants.RELATIONSHIP_NEW_PART_PART_REVISION + "," + DomainConstants.RELATIONSHIP_MAKE_OBSOLETE + "\" select totype dump");
				}
				else
				{
					strAffectedItemToTypes =  MqlUtil.mqlCommand(context, "list relationship \"" + DomainConstants.RELATIONSHIP_REQUEST_PART_REVISION + "," + DomainConstants.RELATIONSHIP_REQUEST_SPECIFICATION_REVISION + "," +  DomainConstants.RELATIONSHIP_REQUEST_PART_OBSOLESCENCE + "\" select totype dump");
				}
			}*/
            if("MECO".equalsIgnoreCase(strPolicy)){
                strAffectedItemToTypes = DomainConstants.TYPE_PART;
            }

            StringTokenizer strTokToRel = new StringTokenizer(strAffectedItemToTypes,"\n");
			String strTypeName = null;
			while(strTokToRel.hasMoreElements()) {
				// Converting the tokens to String
				StringTokenizer strTokToTypes = new StringTokenizer(strTokToRel.nextToken(), SYMB_COMMA);

				while(strTokToTypes.hasMoreElements()) {
    				strTypeName = strTokToTypes.nextToken();
    				String strTypeNameDisplay = strTypeName;
                    strTypeNameDisplay = StringUtils.replaceAll(strTypeNameDisplay," ","");
    
    				if(!fieldChoices.contains(strTypeName)) {
    					fieldChoices.add(strTypeName);
    					strTypeNameDisplay = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,strLocale, "emxComponents.AffectedItems.Filter."+strTypeNameDisplay);
    					fieldDisplayChoices.add(strTypeNameDisplay);
                    }
				}
			}
            // FOR UI Enhancement
            String strPARTMarkupDisplay = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,strLocale, "emxComponents.AffectedItems.Filter.MARKUP");
            fieldDisplayChoices.add(strPARTMarkupDisplay);
            fieldChoices.add(TYPE_PART_MARKUP);
            //END
			int index = fieldChoices.indexOf(strDisplay);
			String sElementValue = (String) fieldChoices.elementAt(index);
			String sElementDisplayValue = (String) fieldDisplayChoices.elementAt(index);
			fieldChoices.setElementAt(fieldChoices.firstElement(), index);
			fieldChoices.setElementAt(sElementValue, 0 );
			fieldDisplayChoices.setElementAt(fieldDisplayChoices.firstElement(), index);
			fieldDisplayChoices.setElementAt(sElementDisplayValue,0);

			typeMap.put(KEY_FIELD_CHOICES, fieldChoices);
			typeMap.put(KEY_FIELD_DISPLAY_CHOICES, fieldDisplayChoices);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return typeMap	;
	}

	/**
	 * getIndirectAffectedItems, Method to retrieve the new revision
	 * for a given change and part context
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap
	 * @return String.
     * @since Common X3
	 * @throws Exception if the operation fails.
	 */

	public String getIndirectAffectedItems(Context context, String args[])	throws Exception
	{
		String strPartId = args[0];
		String strECOId = args[1];

		String strNewPartId = null;

		StringList strlPartSelects =  new StringList(2);
		strlPartSelects.add(SELECT_NAME);
		strlPartSelects.add(SELECT_TYPE);

		DomainObject doPart = new DomainObject(strPartId);

		Map mapPartDetails = doPart.getInfo(context, strlPartSelects);
		String strPartName = (String) mapPartDetails.get(SELECT_NAME);
		String strPartType = (String) mapPartDetails.get(SELECT_TYPE);

		DomainObject doECO = new DomainObject(strECOId);

		StringList strlObjectSelects = new StringList(1);
		strlObjectSelects.add(SELECT_ID);

		String strObjWhereclause = "name == \"" + strPartName + "\"";

		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
		String strRelWhereclause = "attribute[" + strAttrAffectedItemCategory + "] == Indirect && attribute[" + ATTRIBUTE_REQUESTED_CHANGE + "] == \"" + RANGE_FOR_RELEASE + "\"";


		MapList mapListParts = doECO.getRelatedObjects(context,
                    RELATIONSHIP_AFFECTED_ITEM, strPartType, strlObjectSelects,
                    null, false, true, (short) 1, strObjWhereclause, strRelWhereclause);

        if (mapListParts.size() > 0)
        {
			Map mapPart = (Map) mapListParts.get(0);
			strNewPartId = (String) mapPart.get(SELECT_ID);
		}

		//should consider whether comparing against revision list and version list is required here

		return strNewPartId;

	}

	/**
	 * getDirectAffectedItems, Method to retrieve the old revision
	 * for a given change and part context
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap
	 * @return String.
     * @since Common X3
	 * @throws Exception if the operation fails.
	*/
	public String getDirectAffectedItems(Context context, String args[])	throws Exception
	{
		String strPartId = args[0];
		String strECOId = args[1];

		String strOldPartId = null;

		StringList strlPartSelects =  new StringList(2);
		strlPartSelects.add(SELECT_NAME);
		strlPartSelects.add(SELECT_TYPE);

		DomainObject doPart = new DomainObject(strPartId);

		Map mapPartDetails = doPart.getInfo(context, strlPartSelects);
		String strPartName = (String) mapPartDetails.get(SELECT_NAME);
		String strPartType = (String) mapPartDetails.get(SELECT_TYPE);

		DomainObject doECO = new DomainObject(strECOId);

		StringList strlObjectSelects = new StringList(1);
		strlObjectSelects.add(SELECT_ID);

		String strObjWhereclause = "name == \"" + strPartName + "\"";

		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
		String strRelWhereclause = "attribute[" + strAttrAffectedItemCategory + "] == Direct && attribute[" + ATTRIBUTE_REQUESTED_CHANGE + "] == \"" + RANGE_FOR_REVISE + "\"";

		MapList mapListParts = doECO.getRelatedObjects(context,
                    RELATIONSHIP_AFFECTED_ITEM, strPartType, strlObjectSelects,
                    null, false, true, (short) 1, strObjWhereclause, strRelWhereclause);

        if (mapListParts.size() > 0)
        {
			Map mapPart = (Map) mapListParts.get(0);
			strOldPartId = (String) mapPart.get(SELECT_ID);
		}

		//should consider whether comparing against revision list and version list is required here

		return strOldPartId;

	}
	/**
  * Gets ID of the object connected to the reltorel in the from side of the relationship.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param relid relationship id from which reltorel is connected
  * @param relationshipName relationship Name
  * @return the String, the name of the object for further processing
  * @throws FrameworkException if the operation fails
  * @since Common X3
*/
 	    public String getTomidFromRelationshipFromID(Context context, String relid, String relationshipName)
	        throws FrameworkException
	    {

			ContextUtil.startTransaction(context, true);
			String strRes;
			MqlUtil.mqlCommand(context, "verb on");
			try
			{

				ContextUtil.pushContext(context);
				strRes= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump", relid, "tomid.fromrel["+relationshipName+"].from.id");
				ContextUtil.commitTransaction(context);
			}
			catch (Exception e)
			{
				// Abort transaction.
				ContextUtil.abortTransaction(context);
				throw new FrameworkException(e);
			}
			finally
			{
				ContextUtil.popContext(context);
			}
			return strRes;
		}

	/**
	  * Starts the route
	  * @param context the eMatrix <code>Context</code> object
	  * @param strRouteId route object id which has to be satrted
	  * @returns nothing
	  * @throws FrameworkException if the operation fails
	  * @since Common X3
	*/
	public void startRoute (Context context, String strRouteId) throws Exception
	{
		String OFFSET_FROM_ROUTE_START_DATE  = "Route Start Date";
		String OFFSET_FROM_TASK_CREATE_DATE  = "Task Create Date";

		String attrDueDateOffset      = PropertyUtil.getSchemaProperty(context, "attribute_DueDateOffset");
		String attrDueDateOffsetFrom  = PropertyUtil.getSchemaProperty(context, "attribute_DateOffsetFrom");
		String attrAssigneeDueDate    = PropertyUtil.getSchemaProperty(context, "attribute_AssigneeSetDueDate");




		String selDueDateOffset       = "attribute["+attrDueDateOffset+"]";;
		String selDueDateOffsetFrom   = "attribute["+attrDueDateOffsetFrom+"]";
		String selSequence            = "attribute["+DomainConstants.ATTRIBUTE_ROUTE_SEQUENCE+"]";
		String selRouteNodeRelId      = DomainConstants.SELECT_RELATIONSHIP_ID;

		StringList relSelects = new StringList(4);
		relSelects.addElement(selDueDateOffset);
		relSelects.addElement(selDueDateOffsetFrom);
		relSelects.addElement(selRouteNodeRelId);
		relSelects.addElement(selSequence);

		StringBuffer sWhereExp = new StringBuffer();

		DomainObject doRoute = new DomainObject(strRouteId);

		// where clause filters to all route tasks with due offset from this Route Start
		sWhereExp.append("("+selDueDateOffset+ " !~~ \"\")");
		sWhereExp.append(" && (" +selDueDateOffsetFrom + " ~~ \""+OFFSET_FROM_ROUTE_START_DATE+"\")");

		MapList routeStartOffsetList = doRoute.getRelatedObjects(context,
					   Route.RELATIONSHIP_ROUTE_NODE, //String relPattern
						"*",                          //String typePattern
					   null,                          //StringList objectSelects,
					   relSelects,                    //StringList relationshipSelects,
					   false,                         //boolean getTo,
					   true,                          //boolean getFrom,
					   (short)1,                      //short recurseToLevel,
					   "",                            //String objectWhere,
					   sWhereExp.toString(),          //String relationshipWhere,
					   null,                         //Pattern includeType,
					   null,                         //Pattern includeRelationship,
					   null);                       //Map includeMap

		// set Scheduled Due Date attribute for all delta offset Route Nodes
		setDueDatesFromOffset(context, routeStartOffsetList);

		sWhereExp.setLength(0);

		// where clause filters to First order tasks offset from their creation (i.e. this route start)
		sWhereExp.setLength(0);
		sWhereExp.append("("+selDueDateOffset+ " !~~ \"\")");
		sWhereExp.append(" && (" +selDueDateOffsetFrom + " ~~ \""+OFFSET_FROM_TASK_CREATE_DATE+"\")");
		sWhereExp.append(" && (" +selSequence + " == \"1\")");

		MapList routeFirstOrderOffsetList = doRoute.getRelatedObjects(context,
					   Route.RELATIONSHIP_ROUTE_NODE, //String relPattern
						"*",                          //String typePattern
					   null,                          //StringList objectSelects,
					   relSelects,                    //StringList relationshipSelects,
					   false,                         //boolean getTo,
					   true,                          //boolean getFrom,
					   (short)1,                      //short recurseToLevel,
					   "",                            //String objectWhere,
					   sWhereExp.toString(),          //String relationshipWhere,
					   null,                         //Pattern includeType,
					   null,                         //Pattern includeRelationship,
					   null);                       //Map includeMap
		// set Scheduled Due Date attribute for all delta offset ORDER 1 Route Nodes offset From Task create which is same as Route start

		setDueDatesFromOffset(context, routeFirstOrderOffsetList);

		doRoute.promote(context);


	}

	/**
	  * Set Scheduled Complete Date attribute for all RouteNodes constructed from the maplist
	  * The Completion Due Date is got by adding Offset days attribute to current System date-time
	  * @param context the eMatrix <code>Context</code> object
	  * @param MapList Task list for which offset date has to be set
	  * @returns nothing
	  * @throws FrameworkException if the operation fails
	  * @since Common X3
	*/
	public void setDueDatesFromOffset(Context context, MapList offsetList) throws Exception
	{

		String attrDueDateOffset      = PropertyUtil.getSchemaProperty(context, "attribute_DueDateOffset");
		String selDueDateOffset       = "attribute["+attrDueDateOffset+"]";

		Map rNodeMap                     = null;
		DomainRelationship relObjRouteNode     = null;
		Attribute scheduledDateAttribute = null;
		AttributeList timeAttrList       = new AttributeList();
		GregorianCalendar cal            = new GregorianCalendar();
		GregorianCalendar offSetCal      = new GregorianCalendar();
		SimpleDateFormat formatterTest   = new SimpleDateFormat (eMatrixDateFormat.getInputDateFormat(),Locale.US);

		Iterator nextOrderOffsetItr      = offsetList.iterator();

		// get the equivalent server time with required timezone

		cal.setTime(new Date(cal.getTime().getTime())); //modified on 8th March
		String routeTaskScheduledDateStr  = null;
		String rNodeId                    = null;
		String duedateOffset              = null;

		try
		{
			ContextUtil.pushContext(context);
			while(nextOrderOffsetItr.hasNext())
			{
				// use separate calendar objects and reset offSetCal to master calendar to ensure
				// all delta tasks are offset from same Route Start Time.
				offSetCal      = (GregorianCalendar)cal.clone();
				rNodeMap       = (Map) nextOrderOffsetItr.next();
				rNodeId        = (String)rNodeMap.get(DomainObject.SELECT_RELATIONSHIP_ID);
				duedateOffset  = (String)rNodeMap.get(selDueDateOffset);
				// construct corresponding RouteNode relationships and now set correct due-date
				// by adding delta offset to Current time (Route Start) time
				relObjRouteNode             = new DomainRelationship(rNodeId);
				offSetCal.add(Calendar.DATE, Integer.parseInt(duedateOffset));
				routeTaskScheduledDateStr   = formatterTest.format(offSetCal.getTime());
				scheduledDateAttribute      = new Attribute(new AttributeType(DomainConstants.ATTRIBUTE_SCHEDULED_COMPLETION_DATE) ,routeTaskScheduledDateStr);
				timeAttrList.add(scheduledDateAttribute);

				// set Scheduled Completion date attribute
				relObjRouteNode.setAttributes(context,timeAttrList);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			ContextUtil.popContext(context);
		}
	}

	/**
	 * Returns a StringList of the object ids for all Affected Items
	 * for a given change context.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap containing objectId of change object
	 * @return StringList.
     * @since EngineeringCentral X3
	 * @throws Exception if the operation fails.
	*/
	public StringList getAffectedItemOIDs(Context context, String args[])	throws Exception
	{

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String  parentObjectId = (String) programMap.get("objectId");
        StringList result = new StringList();
        if (parentObjectId == null)
        {
        	return (result);
        }
    	String objectId = "";

		HashMap RequestValuesMap          = (HashMap)programMap.get("RequestValuesMap");
		String[] strOID         = (String[])RequestValuesMap.get("objectId");
		String strChangeId = strOID[0];

        MapList mapList = getAffectedItemsWithRelSelectables(context, parentObjectId, strChangeId, SELECT_ALL, SELECT_ALL, SELECT_ALL);
		Iterator itr = mapList.iterator();
        Map map = null;
		for (int i = 0; itr.hasNext(); i++)
		{
			map = (Map) itr.next();
			objectId = (String) map.get(DomainConstants.SELECT_ID);
			result.addElement(objectId);
		}
		return result;
	}

	/**
	 * Returns a StringList of the object ids for all Assignees
	 * for a given change context.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap containing objectId of change object
	 * @return StringList.
     * @since EngineeringCentral X3
	 * @throws Exception if the operation fails.
	*/
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getAssigneesOIDs(Context context, String args[])	throws Exception
	{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String  parentObjectId = (String) programMap.get("objectId");
        StringList result = new StringList();
        if (parentObjectId == null)
        {
        	return (result);
        }
    	String objectId = "";

        MapList mapList = getAssignees(context, args);

		Iterator itr = mapList.iterator();
        Map map = null;
		for (int i = 0; itr.hasNext(); i++)
		{
			map = (Map) itr.next();
			objectId = (String) map.get(DomainConstants.SELECT_ID);
			result.addElement(objectId);
		}

		DomainObject doChange = new DomainObject(parentObjectId);

		String strReln = null;

		if (doChange.isKindOf(context, TYPE_ECR))
		{
			strReln = RELATIONSHIP_CHANGE_RESPONSIBILITY;
		}
		else if (doChange.isKindOf(context, TYPE_ECO))
		{
			strReln = RELATIONSHIP_DESIGN_RESPONSIBILITY;
		}

		if (strReln != null)
		{
			String strOrgId = doChange.getInfo(context, "to[" + strReln + "].from.id");

			if (strOrgId != null && strOrgId.trim().length() > 0)
			{
				StringList strlPersonSelects = new StringList(1);
				strlPersonSelects.add(DomainConstants.SELECT_ID);

				String strOrgWhereClause = "!(to[" + PropertyUtil.getSchemaProperty(context,"relationship_Member") + "].from.id == " + strOrgId + ")";


				MapList totalresultList = DomainObject.findObjects(context,
																 DomainConstants.TYPE_PERSON,
																 "*",
																 "*",
																 "*",
																 "*",
																 strOrgWhereClause,
																 null,
																 true,
																 strlPersonSelects,
																 (short) 0);

				Iterator itrPersons = totalresultList.iterator();

				while (itrPersons.hasNext())
				{
					Map mapPerson = (Map) itrPersons.next();
					result.add((String) mapPerson.get(DomainConstants.SELECT_ID));
				}
			}


		}

		return result;
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
      * @since          EngineeringCentral BX3
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
                 strFullName = "<img src = \"images/iconSmallPerson.gif\"/>&#160;<a href=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?name="+XSSUtil.encodeForURL(context, strLastName+strFirstName)+"&amp;treeMenu=type_Person&amp;emxSuiteDirectory="//Modified By Infosys for Bug # 304580 Date 05/18/2005
                 /*End of modify : by Infosys for Bug#301835 on 4/13/2005*/
                     + XSSUtil.encodeForURL(context,strSuiteDir) + "&amp;relId=" + XSSUtil.encodeForURL(context,strRelId) + "&amp;parentOID="
                     + XSSUtil.encodeForURL(context,strParentObjectId) + "&amp;jsTreeID=" + XSSUtil.encodeForURL(context,strJsTreeID) + "&amp;objectId="
                     + XSSUtil.encodeForURL(context,strObjId )+ "', 'null', 'null', 'false', 'popup')\" class=\"object\">"
                     + XSSUtil.encodeForHTML(context, strLastName) + ",&#160;" + XSSUtil.encodeForHTML(context,strFirstName) + "</a>";
             }
             //Adding into the vector
             vctCompleteName.add(strFullName);
         }
         return  vctCompleteName;
     }

    /**
      * Gets the Change object that the given user is an Assignee of.
      *   This is used by the Full Text search as a selectable.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the HashMap containing the following arguments
      *      objectId - contains the person id.
      * @return String - String containing the given change object id that the user is assigned to.
      * @throws Exception if the operation fails
      * @since          EngineeringCentral BX3
      *
      */
     public String getChangeAssignment(Context context, String[] args) throws Exception
     {
         String strPersonId = args[0];
         DomainObject doPerson = new DomainObject(strPersonId);
         String strAssignedChangeObject = "from["+DomainConstants.RELATIONSHIP_ASSIGNED_EC+"].to.id";
         String retValue = "";
         try
         {
             StringList changeObjList = doPerson.getInfoList(context, strAssignedChangeObject);
             for(int i=0; i < changeObjList.size(); i++)
             {
             	 retValue = retValue + matrix.db.SelectConstants.cSelectDelimiter + ((String)changeObjList.elementAt(i)).trim();
             }
         }
         catch (Exception err)
         {
         }
         return retValue;
     }
	 /**
	 * Displays the Range Values on Edit for Attribute Requested Change for Static and Dynamic Approval policy for ECR and ECO.
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap hold a HashMap containing the following keys, "objectId"
         * @return HashMap contains actual and display values
	 * @throws	Exception if operation fails
	 * @since   EngineeringCentral X3
	 */
     public HashMap displayRequestedChangeRangeValues(Context context,String[] args) throws Exception
	{
		String strLanguage  =  context.getSession().getLanguage();

		StringList requestedChange = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap=(HashMap)programMap.get("paramMap");
		String ChangeObjectId =(String) paramMap.get("objectId");
		DomainObject dom=new DomainObject(ChangeObjectId);

        //get all range values
        StringList strListRequestedChange = FrameworkUtil.getRanges(context , ATTRIBUTE_REQUESTED_CHANGE);

        HashMap rangeMap = new HashMap ();

        StringList listChoices = new StringList();
        StringList listDispChoices = new StringList();

        String attrValue = "";
        String dispValue = "";

        boolean blnIsEC = false;
        boolean blnIsTBE = false;

        if(dom.isKindOf(context,DomainConstants.TYPE_ECO) || dom.isKindOf(context,DomainConstants.TYPE_ECR))
        {
			blnIsEC = true;
			
			//If the ECO is a "Team ECO" the Request For Change value "For Obsolete" will not be displayed.
			String strPolicy = dom.getInfo(context, DomainConstants.SELECT_POLICY);
			String strPolicyClassification = FrameworkUtil.getPolicyClassification(context, strPolicy);
			if("TeamCollaboration".equals(strPolicyClassification)){
				blnIsTBE = true;
			}
		}

        for (int i=0; i < strListRequestedChange.size(); i++)
        {
            attrValue = (String)strListRequestedChange.get(i);

			//For Update is not a Valid Option in EC.

			if(blnIsEC && attrValue.equals(RANGE_FOR_UPDATE))
			{
				continue;
			}
			
			//If the ECO is a "Team ECO" the Request For Change value "For Obsolete" will not be displayed.
			if(blnIsTBE && attrValue.equals(RANGE_FOR_OBSOLETE)){
				continue;
			}

            dispValue = i18nNow.getRangeI18NString(ATTRIBUTE_REQUESTED_CHANGE, attrValue, strLanguage);
            listDispChoices.add(dispValue);
            listChoices.add(attrValue);
        }

        rangeMap.put("field_choices", listChoices);
        rangeMap.put("field_display_choices", listDispChoices);

		return rangeMap;
    }
    /**
    * To get all the indirect Affected Item reationship ids
    * @param context the eMatrix <code>Context</code> object.
    * @param changeId The Change Object Id.
    * @param selectedRelIds String array holding all the selected rel ids from the table.
    * @return StringList.
    * @since Common R207
    * @author ZGQ
    * @throws Exception if the operation fails.
    */

    public StringList getIndirectAffectedItemRelIds(Context context, String changeId, String selectedRelIds[]) throws Exception
    {
        String strChangeId = changeId;
        String strSelectedRelIds[] = selectedRelIds;
        StringList resultList = new StringList();

        StringList strlRelSelects = new StringList();
        strlRelSelects.add("to.name");
        strlRelSelects.add("to.type");
        //IR-069736
        strlRelSelects.add("id[connection]");

        try{
            String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
            String strRelWhereclause = "attribute[" + strAttrAffectedItemCategory + "] == Indirect";

            MapList selectedAIList = DomainRelationship.getInfo(context, strSelectedRelIds, strlRelSelects);

            strlRelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            strlRelSelects.add("to.id");

            DomainObject changeObj = new DomainObject(strChangeId);
            MapList indirectAIList = changeObj.getRelatedObjects(context,RELATIONSHIP_AFFECTED_ITEM, DomainConstants.QUERY_WILDCARD, null,
                    strlRelSelects, false, true, (short) 1, DomainConstants.EMPTY_STRING, strRelWhereclause);

            Iterator dirIterator = selectedAIList.listIterator();
            Iterator indIterator = indirectAIList.listIterator();
            Hashtable directMap = null;
            Map indirectMap = null;
            String directAIName = "";
            String directAIType = "";
            String IndirectId = "";
            String indirectAIName = "";
            String indirectAIType = "";
            String indirectAIRelId = "";
            //IR-069736
            String directRelId = "";
            String indirectRelId = "";

            while (dirIterator.hasNext()){
                directMap = (Hashtable)dirIterator.next();
                directAIName = (String)directMap.get("to.name");
                directAIType = (String)directMap.get("to.type");
                directRelId = (String)directMap.get("id[connection]");
                for(int i=0; i<indirectAIList.size(); i++)
                {
                    indirectMap = (Hashtable)indirectAIList.get(i);
                    IndirectId = (String)indirectMap.get("to.id");
                    indirectAIName = (String)indirectMap.get("to.name");
                    indirectAIType = (String)indirectMap.get("to.type");
                    indirectRelId = (String)indirectMap.get("id[connection]");
                    //IR-069736
                    if(directAIName.equals(indirectAIName) && directAIType.equals(indirectAIType) && !directRelId.equals(indirectRelId)){
                        indirectAIRelId = (String)indirectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                        resultList.add(IndirectId + "|" + indirectAIRelId);
                        break;
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        return resultList;
    }


	//HF-021127V6R2010x
    /**
     * Get the approval status based on type of Action of Inbox task
     * @param context the Matrix Context
     * @param args no args needed for this method
     * @returns StringList containing Avalable selection for approval status
     * @throws Exception if the operation fails
     * @since EC 10.6-SP1
     */
     public StringList getTaskApprovalStatus(Context context,String[] args) throws Exception
     {
         Map programMap        = (HashMap)JPO.unpackArgs(args);
         Map paramList         = (HashMap)programMap.get("paramList");
         String sEditMode      = (String)paramList.get("editTableMode");
         MapList objList       = (MapList)programMap.get("objectList");
         StringList idList     = new StringList( objList.size() );
         StringList returnList = new StringList( objList.size() );
         StringList busSelects = new StringList(4);
         Map mTemp;

         busSelects.addElement(SELECT_ID);
         busSelects.addElement(SELECT_APPROVAL_STATUS);
         if("true".equals(sEditMode)) {
             busSelects.addElement(SELECT_CURRENT);
             busSelects.addElement(SELECT_ROUTE_ACTION);
         }

         for( Iterator itr = objList.iterator(); itr.hasNext(); ) {
             mTemp = (Map) itr.next();
             idList.addElement( (String)mTemp.get((SELECT_ID)) );
         }
         MapList mlObjectInfo = DomainObject.getInfo( context, (String[]) idList.toArray( new String[ idList.size()]), busSelects );

         if("true".equals( sEditMode )) {
             StringList slRange          = mxAttr.getChoices( context, ATTRIBUTE_APPROVAL_STATUS );
             // Show only Approve, Reject, None or Abstain values in the dropdown for Approval Status
             slRange.removeElement( "Ignore" );
             slRange.removeElement( "Signature Reset" );
             if( !"true".equalsIgnoreCase( EnoviaResourceBundle.getProperty(context, "emxComponents.Routes.ShowAbstainForTaskApproval" ))) {
                 slRange.removeElement( "Abstain" );
             }
             StringList slRangeDisplay   = UINavigatorUtil.getAttrRangeI18NStringList( ATTRIBUTE_APPROVAL_STATUS, slRange, context.getSession().getLanguage() );

             int iCount = 0;
             for( Iterator itr = mlObjectInfo.iterator(); itr.hasNext(); ) {
                 mTemp                   = (Map) itr.next();
                 String sCurrent         = (String) mTemp.get( SELECT_CURRENT );
                 String sAction          = (String) mTemp.get( SELECT_ROUTE_ACTION );
                 String sApprovalStatus  = (String) mTemp.get( SELECT_APPROVAL_STATUS );
                 String sValue           = EMPTY_STRING;

                 if( STATE_INBOX_TASK_COMPLETE.equals( sCurrent ) ) {
                     // If task is in complete state, the cell will be read-only
                     sValue = sApprovalStatus;
                 } else if ( RANGE_APPROVE.equals( sAction )) {
                     if( sApprovalStatus == null || "null".equals(sAction) || "".equals(sAction) ) {
                         sApprovalStatus = "None";
                     }
                     // If the 'Route Action' of the task is 'Approve', the cell will have a dropdown
                     StringBuffer sbSelect = new StringBuffer(100);
                     sbSelect.append( "<select name=\"ApprovalStatus" + iCount + "\" > " );
                     for(int i=0, len = slRange.size();i<len; i++) {
                         sbSelect.append( "<option value=\"" );
                         sbSelect.append(slRange.get(i) + "\"");
                         if( sApprovalStatus.equals( slRange.get(i) )) {
                             sbSelect.append(" selected=\"selected\" ");
                         }
                         sbSelect.append( "> " );
                         sbSelect.append( slRangeDisplay.get(i) );
                         sbSelect.append( " </option>" );
                     }
                     sbSelect.append(" </select>");
                     sValue = sbSelect.toString();
                 } else {
                     // If the 'Route Action' of the task is not 'Approve', the cell will be blank
                     sValue = EMPTY_STRING;
                 }
                 returnList.addElement( sValue );
                 iCount++;
             }
         } else {
             for( Iterator itr = mlObjectInfo.iterator(); itr.hasNext(); ) {
                 mTemp = (Map) itr.next();
                 returnList.addElement((String) mTemp.get( SELECT_APPROVAL_STATUS) );
             }
         }

     return returnList;
   }


 	//HF-021127V6R2010x
     /**
      *Update the Aproval Status of Inbox task
      * @param context the Matrix Context
      * @param args no args needed for this method
      * @returns booloen
      * @throws Exception if the operation fails
      * @since EC 10.6-SP1
      */
     public Boolean updateTaskApprovalStatus(Context context, String[] args) throws Exception
     {

       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap paramMap = (HashMap)programMap.get("paramMap");

       String objectId  = (String)paramMap.get("objectId");
       HashMap requestMap=(HashMap)programMap.get("requestMap");
       // get the value selected for field Approval Status
       String newApprovalStatus = (String) paramMap.get("New Value");

       DomainObject obj=new DomainObject(objectId);
       StringList selectable=new StringList();
       selectable.addElement("attribute["+ATTRIBUTE_APPROVAL_STATUS+"]");
       selectable.addElement(SELECT_CURRENT);

       Hashtable resultMap=(Hashtable)obj.getInfo(context,selectable);
       String current=(String)resultMap.get(SELECT_CURRENT);
       String currentApprovalStatus = (String)resultMap.get("attribute["+ATTRIBUTE_APPROVAL_STATUS+"]");
      //if task is in complete state do not update the value
       if(!current.equals(STATE_INBOX_TASK_COMPLETE))
       {
         if( newApprovalStatus!=null && newApprovalStatus.length()>0 )
         {
           //if new value is seleted in edit page then only modify the attribute
           if(!newApprovalStatus.equals(currentApprovalStatus))
           {
             obj.setAttributeValue(context, ATTRIBUTE_APPROVAL_STATUS, newApprovalStatus);
           }
         }
       }
       return Boolean.valueOf(true);
     }
     
   //Added for Request IR-142259 start
     
    /**
 	 * getProperIndex(),this method is executed to get the index value for the selected 
 	 * Requested Change Filter option in the Custom Filter Toolbar Menu in Affected Item.
 	 * @param sElementValue contains selected Requested Change filter value.
 	 * @param fieldChoices contains list of Requested Change filter values available
 	 * @return int contains index value of the selected Request Change filter value
     * @since V6R2013
 	 * @throws Exception if the operation fails.
 	 */ 
   	public static int getProperIndex(String sElementValue,StringList fieldChoices)throws Exception{
   		
   		int Size = fieldChoices.size();
   		int index=0;
   		for(int i=0;i<Size;i++)
   		{
   			String sRangeValue = (String)fieldChoices.elementAt(i);
               if(sRangeValue.equals(sElementValue)){
               	index=i;
               	break;
               }
   		}
   		return index;
   	}
     //Added for Request IR-142259 end

  }


