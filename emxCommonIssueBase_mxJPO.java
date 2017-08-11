/*
 ** Copyright (c) 1999-2016 Dassault Systemes.
 ** All Rights Reserved.
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.StringTokenizer;

import matrix.db.Access;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.BusinessType;
import matrix.db.BusinessTypeList;
import matrix.db.Context;
import matrix.db.FindLikeInfo;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.db.Role;
import matrix.db.State;
import matrix.db.StateList;
import matrix.db.User;
import matrix.db.UserList;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.dassault_systemes.enovia.bps.widget.UIWidget;
import com.matrixone.apps.common.Issue;
import com.matrixone.apps.common.MultipleOwner;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Search;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.ExpansionIterator;
import matrix.db.RelationshipWithSelect;


/**
 * The <code>emxIssueBase</code> class contains methods related to Issue Management.
 * @version  - AEF 10.0.5.0 Copyright (c) 2004, MatrixOne, Inc.
 * @author INFOSYS
 */
public class emxCommonIssueBase_mxJPO extends emxDomainObject_mxJPO {

		/** A string constant with the value && . */
        public static final String SYMB_AND = " && ";
        /** A string constant with the value ||. */
        public static final String SYMB_OR = " || ";
        /** A string constant with the value ==. */
        public static final String SYMB_EQUAL = " == ";
        /** A string constant with the value !. */
        public static final String SYMB_NOT = "!";
        /** A string constant with the value !=. */
        public static final String SYMB_NOT_EQUAL = " != ";
        /** A string constant with the value >. */
        public static final String SYMB_GREATER_THAN = " > ";
        /** A string constant with the value <. */
        public static final String SYMB_LESS_THAN = " < ";
        /** A string constant with the value >=. */
        public static final String SYMB_GREATER_THAN_EQUAL = " >= ";
        /** A string constant with the value <=. */
        public static final String SYMB_LESS_THAN_EQUAL = " <= ";
        /** A string constant with the value ~~. */
        public static final String SYMB_MATCH = " ~~ ";
        /** A string constant with the value !~~. */
        public static final String SYMB_NOMATCH = " !~~ ";
        /** A string constant with the value "'". */
        public static final String SYMB_QUOTE = "'";
        /** A string constant with the value *. */
        public static final String SYMB_WILD = "*";
        /** A string constant with the value (. */
        public static final String SYMB_OPEN_PARAN = "(";
        /** A string constant with the value ). */
        public static final String SYMB_CLOSE_PARAN = ")";
        /** A string constant with the value attribute. */
        public static final String SYMB_ATTRIBUTE = "attribute";
        /** A string constant with the value role_. */
        public static final String SYMB_ROLE = "role_";
        /** A string constant with the value [. */
        public static final String SYMB_OPEN_BRACKET = "[";
        /** A string constant with the value ]. */
        public static final String SYMB_CLOSE_BRACKET = "]";
        /** A string constant with the value "to". */
        public static final String SYMB_TO = "to";
        /** A string constant with the value "from". */
        public static final String SYMB_FROM = "from";
        /** A string constant with the value ".". */
        public static final String SYMB_DOT = ".";
        /** A string constant with the value state_Closed. */
        public static final String SYMB_CLOSE = "state_Closed";
        /** A string constant with the value state_Create. */
        public static final String SYMB_CREATE = "state_Create";
        /** A string constant with the value state_Assign. */
        public static final String SYMB_ASSIGN = "state_Assign";

          /** A string constant with the value - . Added by Yukthesh for Bug 30507*/
        public static final String SYMB_HYPHEN = "-";

        /** A string constant with the value comboDescriptor_. */
        public static final String COMBO_PREFIX = "comboDescriptor_";
        /** A string constant with the value txt_. */
        public static final String TXT_PREFIX = "txt_";
        /** A string constant with the value objectIDs. */
        public static final String OBJECT_IDS = "objectIDs";
        /** A string constant with the value objectList. */
        public static final String OBJECT_LIST = "objectList";
        /** A string constant with the value objectId. */
        public static final String OBJECT_ID = "objectId";
        /** A string constant with the value paramList. */
        public static final String PARAM_LIST = "paramList";
        /** A string constant with the value relationName. */
        public static final String RELATION_NAME = "relationName";
        /** A string constant with the value emxTableRowId. */
        public static final String TABLE_ROW_ID = "emxTableRowId";
        /** A string constant with the value true. */
        public static final String TRUE = "true";
        /** A string constant with the value vaultOption. */
        public static final String VAULT_OPTION = "vaultOption";
        /** A string constant with the value VaultDisplay. */
        public static final String VAULT_DISPLAY = "VaultDisplay";
        /** A string constant with the value SuiteDirectory. */
        public static final String SUITE_DIR = "SuiteDirectory";
        /** A string constant with the value jsTreeID. */
        public static final String JS_TREE_ID = "jsTreeID";
        /** A string constant with the value First Name. */
        public static final String FIRST_NAME = "First Name";
        /** A string constant with the value Last Name. */
        public static final String LAST_NAME = "Last Name";
        /** A string constant with the value User Name. */
        public static final String USER_NAME = "User Name";
        /** A string constant with the value SETTINGS. */
        public static final String SETTINGS = "SETTINGS";
        /** A string constant with the value State. */
        public static final String STATE = "State";
        /** A string constant with the value emxComponents.Form.Radio.LatestReleasedRevisionOnly. */
        public static final String DOCUMENT_LATEST_REVISION_ONLY = "emxComponents.Form.Radio.LatestReleasedRevisionOnly";
        /** A string constant with the value emxComponentsStringResource. */
        public static final String RESOURCE_BUNDLE_COMPONENTS_STR =
                "emxComponentsStringResource";
        /** A string constant with the value emxComponents. */
        public static final String RESOURCE_BUNDLE_COMPONENTS = "emxComponents";
        /** A string constant with the value emxComponents.Message.Assignment.Subject. */
        public static final String MESSAGE_ASSIGNMENT_SUBJECT =
                "emxComponents.Message.Assignment.Subject";
        /** A string constant with the value "emxComponents.Message.Assignment.Body". */
        public static final String MESSAGE_ASSIGNMENT_BODY =
                "emxComponents.Message.Assignment.Body";
        /** A string constant with the value "emxComponents.Message.Status.Subject". */
        public static final String MESSAGE_STATUS_SUBJECT =
                "emxComponents.Message.Status.Subject";
        /** A string constant with the value "emxComponents.Message.Status.Body". */
        public static final String MESSAGE_STATUS_BODY =
                "emxComponents.Message.Status.Body";
        /** A string constant with the value "emxComponents.Message.Modify.Body". */
        public static final String MESSAGE_MODIFY_BODY =
                "emxComponents.Message.Modify.Body";
        /** A string constant with the value "emxComponents.Form.Radio.All". */
        public static final String FEATURE_VAULT_ALL =
                "emxComponents.Form.Radio.All";
        /** A string constant with the value "emxComponents.Form.Radio.Default". */
        public static final String FEATURE_VAULT_DEFAULT =
                "emxComponents.Form.Radio.Default";
        /** A string constant with the value "emxComponents.Form.Radio.Selected". */
        public static final String FEATURE_VAULT_SELECTED =
                "emxComponents.Form.Radio.Selected";
        /** A string constant with the value "emxComponents.Issue.IconRedDays". */
        public static final String ICON_RED = "emxComponents.Issue.IconRedDays";
        /** A string constant with the value "emxComponents.Issue.IconGreenDays". */
        public static final String ICON_GREEN = "emxComponents.Issue.IconGreenDays";
        /** A string constant with the value "emxComponents.Error.MissingStatusIkonValue". */
        public static final String ERROR_VALUE =
                "emxComponents.Error.MissingStatusIkonValue";
        /** A string constant with the value "emxComponents.Issue.Alert.Dates". */
        public static final String EST_DATE_ERROR =
                "emxComponents.Issue.Alert.Dates";
        /** A string constant with the value "emxComponents.Issue.Alert.EstStartDate". */
        public static final String EST_START_DATE_ERROR =
                "emxComponents.Issue.Alert.EstStartDate";
        /** A string constant with the value "emxComponents.Issue.Alert.EstEndDate". */
        public static final String EST_END_DATE_ERROR =
                "emxComponents.Issue.Alert.EstEndDate";
        /** A string constant with the value "emxComponents.Error.UnsupportedClient". */
        public static final String CHECK_FAIL =
                "emxComponents.Error.UnsupportedClient";
        /** A string constant with the value "emxComponents.Issue.CloseAttributes". */
        public static final String CLOSE_ERROR =
                "emxComponents.Issue.CloseAttributes";
        /** A string constant with the value "emxComponents.Issue.ActionTaken". */
        public static final String CLOSE_ACTION_TAKEN_ERROR =
                "emxComponents.Issue.ActionTaken";
        /** A string constant with the value "emxComponents.Issue.ResolutionStatement". */
        public static final String CLOSE_RESL_STATEMENT_ERROR =
                "emxComponents.Issue.ResolutionStatement";
        /** A string constant with the value "emxComponents.Issue.ResolutionDate". */
        public static final String CLOSE_RESL_DATE_ERROR =
                "emxComponents.Issue.ResolutionDate";
        /** A string constant with the value "emxComponents.Issue.CheckAccess". */
        public static final String CHECK_ACCESS = "emxComponents.Issue.CheckAccess";
        /** A string constant with the value "emxComponents.Issue.Alert.CheckState". */
        public static final String CHECK_STATE = "emxComponents.Issue.Alert.CheckState";
                /** A string constant with the value "emxComponents.Issue.Alert.CheckPerson". */
                public static final String CHECK_PERSON = "emxComponents.Issue.Alert.CheckPerson";

        /** A string constant with the value "emxComponents.Issue.ToolTipRedDays". */
        public static final String ICON_TOOLTIP_RED = "emxComponents.Issue.ToolTipRedDays";
        /** A string constant with the value "emxComponents.Issue.ToolTipGreenDays". */
        public static final String ICON_TOOLTIP_GREEN= "emxComponents.Issue.ToolTipGreenDays";
        /** A string constant with the value "emxComponents.Issue.ToolTipYellowDays". */
        public static final String ICON_TOOLTIP_YELLOW = "emxComponents.Issue.ToolTipYellowDays";

        /** A string constant with the value All. */
        public static final String ALL = "All";
        /** A string constant with the value Default. */
        public static final String DEFAULT = "Default";
        /** A string constant with the value Selected. */
        public static final String SELECTED = "Selected";

        /** A string constant with the value "emxComponents.Form.Radio.AllStates". */
        public static final String ALL_STATES =
                "emxComponents.Form.Radio.AllStates";

        /** A string constant with the value "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"  align=\"middle\">". */
        public static final String GREEN_ICON =
                "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"  align=\"middle\">";
        /** A string constant with the value "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"  align=\"middle\">". */
        public static final String RED_ICON =
                "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"  align=\"middle\">";
        /** A string constant with the value "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"  align=\"middle\">". */
        public static final String YELLOW_ICON =
                "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"  align=\"middle\">";
        /** A string constant with the value reqMap. */
        public static final String REQ_MAP = "reqMap";
        /** A string constant with the value reqTableMap. */
        public static final String REQ_TABLE_MAP = "reqTableMap";

        //Added for Bug#280942
            /** A string constant with the value emxComponents.Warning.ObjectFindLimit. */
        public static final String WARNING1_FIND_OBJECTS=
                                   "emxComponents.Warning.ObjectFindLimit";
            /** A string constant with the value emxComponents.Warning.Reached. */
        public static final String WARNING2_FIND_OBJECTS=
                                   "emxComponents.Warning.Reached";

        //Added for Bug#282109
            /** A string constant with the value "emxComponents.Alert.NoPrivelege". */
        public static final String ALERT_NO_PREVELEGE =    "emxComponents.Alert.NoPrivelege";

        // Added for Bug# 291442
        /**
        * Alias for the string paramMap.
        */
        protected static final String PARAMMAP = "paramMap";
        /**
        * Alias for the string objectId.
        */
        protected static final String OBJECTID = "objectId";

    /** type "DOCUMENTS". */
    protected static final String TYPE_DOCUMENTS =
            PropertyUtil.getSchemaProperty("type_DOCUMENTS");

    /** type "Libraries". */
    protected static final String TYPE_LIBRARIES =
            PropertyUtil.getSchemaProperty("type_Libraries");

    /** type "Classification". */
    protected static final String TYPE_CLASSIFICATION =
            PropertyUtil.getSchemaProperty("type_Classification");

	/** policy "Version". */
	protected static final String POLICY_VERSION = 
	        PropertyUtil.getSchemaProperty("policy_Version");
    
    /** policy "Versioned Design Policy". */	
	protected static final String POLICY_VERSIONED_DESIGN_POLICY =
            PropertyUtil.getSchemaProperty("policy_VersionedDesignPolicy");

        /**
         * Create a new emxIssueBase object.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments.
         * @return a emxIssueBase object.
         * @throws Exception if the operation fails.
         * @since AEF 10.0.5.0
         */
        public emxCommonIssueBase_mxJPO(Context context, String[] args) throws Exception {
                super(context, args);
        }

        /** Simple date format. */
        protected SimpleDateFormat _mxDateFormat =
                new SimpleDateFormat(
                        eMatrixDateFormat.getEMatrixDateFormat(),
                        Locale.US);

        /**
         * Main entry point.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments
         * @return an integer status code (0 = success)
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int mxMain(Context context, String[] args) throws Exception {
                if (!context.isConnected()) {
                        i18nNow i18nnow = new i18nNow();
                        String strLanguage = context.getSession().getLanguage();
                        //      String strContentLabel = i18nnow.GetString("emxProductCentralStringResource", language, "emxProductCentral.Alert.FeaturesCheckFailed");
                        String strContentLabel =
                                EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),CHECK_FAIL);
                        throw new Exception(strContentLabel);
                }
                return 0;
        }


           /**
            * Method call to get all the Issues in the data base.
            *
            * @param context the eMatrix <code>Context</code> object
            * @param args - holds no arguments to be used by this method
            * @return MapList containing the id of all Issue objects
            * @throws Exception if the operation fails
            * @since AEF 10.0.5.0
            *
            */
            @com.matrixone.apps.framework.ui.ProgramCallable
            public MapList getAllIssues(Context context, String[] args)throws Exception {
                // forming the where clause
                String strWhereExpression = "";
                //StringBuffer to form the where expression
                StringBuffer sbOwnerCondition = new StringBuffer(100);

                //To get Actual Relationship name
                String strRelationAssignedIssue = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_relationship_AssignedIssue);
                //To get actual policy Name
                String strIssuePolicy = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_policy_Issue);
               //Getting and representing the state 'Close'
               String strClose = FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CLOSE);
               //getting Context user
               String strOwner = context.getUser();

               //Forming the where expression
               sbOwnerCondition.append(SYMB_OPEN_PARAN);
               sbOwnerCondition.append(SYMB_OPEN_PARAN);
               sbOwnerCondition.append(DomainConstants.SELECT_OWNER);
               sbOwnerCondition.append(SYMB_EQUAL);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(strOwner);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(SYMB_CLOSE_PARAN);
               sbOwnerCondition.append(SYMB_OR);
               sbOwnerCondition.append(SYMB_OPEN_PARAN);
               sbOwnerCondition.append(DomainConstants.SELECT_ORIGINATOR);
               sbOwnerCondition.append(SYMB_EQUAL);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(strOwner);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(SYMB_CLOSE_PARAN);
               sbOwnerCondition.append(SYMB_OR);

               //Added to include the Assigned Issues in ALL Filter.
               sbOwnerCondition.append(SYMB_OPEN_PARAN);
               sbOwnerCondition.append(SYMB_TO);
               sbOwnerCondition.append(SYMB_OPEN_BRACKET);
               sbOwnerCondition.append(strRelationAssignedIssue);
               sbOwnerCondition.append(SYMB_CLOSE_BRACKET);
               sbOwnerCondition.append(SYMB_DOT);
               sbOwnerCondition.append(SYMB_FROM);
               sbOwnerCondition.append(SYMB_DOT);
               sbOwnerCondition.append(DomainConstants.SELECT_NAME);
               sbOwnerCondition.append(SYMB_EQUAL);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(strOwner);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(SYMB_CLOSE_PARAN);

               //Added to include Co-Owners Condition in where clause //
               sbOwnerCondition.append(SYMB_OR);
               sbOwnerCondition.append(SYMB_OPEN_PARAN);
               sbOwnerCondition.append("attribute[" + DomainConstants.ATTRIBUTE_CO_OWNER + "]");
               sbOwnerCondition.append(SYMB_MATCH);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(SYMB_WILD);
               sbOwnerCondition.append(strOwner);
               sbOwnerCondition.append(SYMB_WILD);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(SYMB_CLOSE_PARAN);
               //End Added to include Co-Owners Condition in where clause //
               sbOwnerCondition.append(SYMB_CLOSE_PARAN);
               sbOwnerCondition.append(SYMB_AND);
               sbOwnerCondition.append(SYMB_OPEN_PARAN);
               sbOwnerCondition.append(DomainConstants.SELECT_CURRENT);
               sbOwnerCondition.append(SYMB_NOT_EQUAL);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(strClose);
               sbOwnerCondition.append(SYMB_QUOTE);
               sbOwnerCondition.append(SYMB_CLOSE_PARAN);

               strWhereExpression = sbOwnerCondition.toString();

            /***********************************************************************************
            **********************Start of Add by Yukthesh for bug 307507***********************
            ***********************************************************************************/

               String strType = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);
               //getting the symbolic relationship from PropertyUtil
               //String strRelFoundIn = RELATIONSHIP_ISSUE;
               String strRelFoundIn = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_relationship_AssignedIssue);
               MapList relIssueList = null;
               //Business Objects are selected by its Ids
               StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			   objectSelects.add("current.access[modify]");
               //the number of levels to expand, 1 equals expand one level, 0 equals expand all
               short sRecurseToLevel = 1;

               try
               {
                   BusinessObject busObj = new BusinessObject(DomainConstants.TYPE_PERSON,strOwner,SYMB_HYPHEN,null);
                   DomainObject domObj = DomainObject.newInstance(context,busObj);

                   //retrieving Issue List from Context
                   relIssueList =     (MapList)domObj.getRelatedObjects(
                                                                       context,
                                                                       strRelFoundIn,
                                                                       strType,
                                                                       objectSelects,
                                                                       null,
                                                                       false,
                                                                       true,
                                                                       sRecurseToLevel,
                                                                       DomainConstants.EMPTY_STRING,
                                                                       DomainConstants.EMPTY_STRING);
               }
               catch(Exception e)
               {
                   System.out.println("Error in getRelated of All issues= "+e);
               }

               MapList mapBusIds = getDesktopIssues(context, strWhereExpression, null);
               MapList idList = new MapList();
               Map idMap = null;
               //Making a maplist of only ids

               for(int i=0; i< relIssueList.size(); i++)
               {
                   idMap = (Map) relIssueList.get(i);
                   Map idfinalMap = new HashMap();
                   idfinalMap.put("id",idMap.get(DomainConstants.SELECT_ID));
                   idList.add(idfinalMap);
               }
               idMap = null;
               String strId = null;
               boolean doesExist = false;
               Map mapBusId = null;
               //Merging of the 2 Maplists
               for(int j=0; j< idList.size(); j++)
               {
                   idMap = (Map) idList.get(j);
                   strId = (String)idMap.get(DomainConstants.SELECT_ID);
                   for (int i = 0; i < mapBusIds.size(); i++) {
                       mapBusId = (Map)mapBusIds.get(i);
                       if(strId.equals(mapBusId.get(DomainConstants.SELECT_ID))){
                           doesExist = true;
                           break;
                       }
                   }
                   if (!doesExist){
                       mapBusIds.add(idMap);
                   }
               }
               /**********************************************************************************
               **********************End of Add by Yukthesh for bug 307507************************
               ***********************************************************************************/
               return mapBusIds;
           }


        /**
         * Method call to get all the assigned Issues to the context person.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - holds no arguments to be used by this method
         * @return MapList containing the id of all assigned Issue objects
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getAssignedIssues(Context context, String[] args)
                throws Exception {
                // forming the where clause
                String strWhereExpression = "";
                StringBuffer sbOwnerCondition = new StringBuffer(40);

                //To get Actual Relationship name
                String strRelationAssignedIssue =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_relationship_AssignedIssue);
                //To get actual policy Name
                String strIssuePolicy =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_policy_Issue);
                //      Getting and representing the state 'Close'
                String strClose =
                        FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CLOSE);

                //Calls the private method to retrieve the data
                sbOwnerCondition.append(SYMB_OPEN_PARAN);
                sbOwnerCondition.append(SYMB_TO);
                sbOwnerCondition.append(SYMB_OPEN_BRACKET);
                sbOwnerCondition.append(strRelationAssignedIssue);
                sbOwnerCondition.append(SYMB_CLOSE_BRACKET);
                sbOwnerCondition.append(SYMB_DOT);
                sbOwnerCondition.append(SYMB_FROM);
                sbOwnerCondition.append(SYMB_DOT);
                sbOwnerCondition.append(DomainConstants.SELECT_NAME);
                sbOwnerCondition.append(SYMB_EQUAL);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(context.getUser());
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(SYMB_CLOSE_PARAN);
                sbOwnerCondition.append(SYMB_AND);
                sbOwnerCondition.append(SYMB_OPEN_PARAN);
                sbOwnerCondition.append(DomainConstants.SELECT_CURRENT);
                sbOwnerCondition.append(SYMB_NOT_EQUAL);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(strClose);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(SYMB_CLOSE_PARAN);

                strWhereExpression = sbOwnerCondition.toString();
                //Calls the private method to retrieve the data
                MapList mapBusIds = getDesktopIssues(context, strWhereExpression, null);
                return mapBusIds;
        }

        /**
         * Method call to get all the owned Issues of the context person.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - holds no arguments to be used by this method
         * @return MapList containing the id of all owned Issue objects
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getOwnedIssues(Context context, String[] args)
                throws Exception {
                String strPolicy =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_policy_Issue);
                //The where condition will be that the owner should be the context user
                String strWhereExpression = "";
                //Getting and representing the state 'Close'
                String strClose =
                        FrameworkUtil.lookupStateName(context, strPolicy, SYMB_CLOSE);
                //StringBuffer variable to form the expression
                StringBuffer sbOwnerCondition = new StringBuffer(100);
                //Context User
                String strOwner = context.getUser();

                //Forming the expression
                sbOwnerCondition.append(SYMB_OPEN_PARAN);
                sbOwnerCondition.append(SYMB_OPEN_PARAN);
                sbOwnerCondition.append(DomainConstants.SELECT_OWNER);
                sbOwnerCondition.append(SYMB_EQUAL);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(strOwner);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(SYMB_CLOSE_PARAN);
                // Commented for the bug 344968
                /*
                sbOwnerCondition.append(SYMB_OR);
                sbOwnerCondition.append(SYMB_OPEN_PARAN);
                sbOwnerCondition.append(DomainConstants.SELECT_ORIGINATOR);
                sbOwnerCondition.append(SYMB_EQUAL);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(strOwner);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(SYMB_CLOSE_PARAN);
                */
//Added to include Co-Owners Condition in where clause //
                sbOwnerCondition.append(SYMB_OR);
                sbOwnerCondition.append(SYMB_OPEN_PARAN);
                sbOwnerCondition.append(
                    "attribute[" + DomainConstants.ATTRIBUTE_CO_OWNER + "]");
                sbOwnerCondition.append(SYMB_MATCH);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(SYMB_WILD);
                sbOwnerCondition.append(strOwner);
                sbOwnerCondition.append(SYMB_WILD);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(SYMB_CLOSE_PARAN);
//End Added to include Co-Owners Condition in where clause //
                sbOwnerCondition.append(SYMB_CLOSE_PARAN);
                sbOwnerCondition.append(SYMB_AND);
                sbOwnerCondition.append(SYMB_OPEN_PARAN);
                sbOwnerCondition.append(DomainConstants.SELECT_CURRENT);
                sbOwnerCondition.append(SYMB_NOT_EQUAL);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(strClose);
                sbOwnerCondition.append(SYMB_QUOTE);
                sbOwnerCondition.append(SYMB_CLOSE_PARAN);

                strWhereExpression = sbOwnerCondition.toString();
                //Calls the private method to retrieve the data
                MapList mapBusIds = getDesktopIssues(context, strWhereExpression, null);
                return mapBusIds;
        }

        /**
        * Method to get the Issues.
        *
        * @param context the eMatrix <code>Context</code> object
        * @param strWhereCondition - which holds the where expression for query
        * @param strOwnerCondition - which holds the Owner condition for query
        * @return Maplist - containing issue ids.
        * @throws Exception if the operation fails
        * @since AEF 10.0.5.0
        */
        protected MapList getDesktopIssues(
                Context context,
                String strWhereCondition,
                String strOwnerCondition)
                throws Exception {
                //String list initialized to retrieve data for the Issues
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.add("current.access[modify]");
                String strType =
                        PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);
                //The findobjects method is invoked to get the list of products
                MapList mapBusIds =
                        findObjects(
                                context,
                                strType,
                                null,
                                null,
                                strOwnerCondition,
                                DomainConstants.QUERY_WILDCARD,
                                strWhereCondition,
                                true,
                                objectSelects);
             return mapBusIds;
        }

        /**
         * Method to obtain all the issues related to context.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries:
         *                      ObjectId - The object Id of the context.
         * @return MapList containing the id of all Context Issue objects
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getAllContextIssues(Context context, String[] args)
                throws Exception {
                //unpacking the Arguments from variable args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                //getting parent object Id from args
                String strParentId = (String) programMap.get(OBJECT_ID);

                //Getting the type name
                String strType =
                        PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);
                //getting the symbolic relationship from PropertyUtil
                //String strRelFoundIn = RELATIONSHIP_ISSUE;
                String strRelFoundIn =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_relationship_Issue);
                //Initializing the return type
                MapList relIssueList = null;
                //Business Objects are selected by its Ids
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.add("current.access[modify]");
                //Relationships are selected by its Ids
                StringList relSelects =
                        new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                //the number of levels to expand, 1 equals expand one level, 0 equals expand all
                short sRecurseToLevel = 1;
                //retrieving Issue List from Context
                this.setId(strParentId);
                relIssueList =
                        getRelatedObjects(
                                context,
                                strRelFoundIn,
                                strType,
                                objectSelects,
                                relSelects,
                                true,
                                false,
                                sRecurseToLevel,
                                DomainConstants.EMPTY_STRING,
                                DomainConstants.EMPTY_STRING);
                return relIssueList;
        }

        /**
         * Method to obtain all the related issues which are in 'Closed' state.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries:
         *              ObjectId - The object Id of the context.
         * @return MapList containing the id of all Closed Context Issue objects
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getClosedIssues(Context context, String[] args)
                throws Exception {
                //unpacking the Arguments from variable args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                //To get actual policy Name
                String strIssuePolicy =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_policy_Issue);
                //Getting and representing the state 'Close'
                String strClose =
                        FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CLOSE);
                //getting parent object Id from args
                String strParentId = (String) programMap.get(OBJECT_ID);

                //Getting the type name
                String strType =
                        PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);

                String strClosedCondition = "";
                //StringBuffer variable for where expression
                StringBuffer sbClosedCondition = new StringBuffer(20);

                //Forming the where expression
                sbClosedCondition.append(SYMB_OPEN_PARAN);
                sbClosedCondition.append(DomainConstants.SELECT_CURRENT);
                sbClosedCondition.append(SYMB_EQUAL);
                sbClosedCondition.append(SYMB_QUOTE);
                sbClosedCondition.append(strClose);
                sbClosedCondition.append(SYMB_QUOTE);
                sbClosedCondition.append(SYMB_CLOSE_PARAN);
                strClosedCondition = sbClosedCondition.toString();

                //getting the symbolic relationship from PropertyUtil
                //String strRelFoundIn = RELATIONSHIP_ISSUE;
                String strRelFoundIn =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_relationship_Issue);

                //Initializing the return type
                MapList relIssueList = null;
                //Business Objects are selected by its Ids
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.add("current.access[modify]");
                //Relationships are selected by its Ids
                StringList relSelects =
                        new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                //the number of levels to expand, 1 equals expand one level, 0 equals expand all
                short sRecurseToLevel = 1;
                //retrieving Issue from Context
                this.setId(strParentId);
                relIssueList =
                        getRelatedObjects(
                                context,
                                strRelFoundIn,
                                strType,
                                objectSelects,
                                relSelects,
                                true,
                                false,
                                sRecurseToLevel,
                                strClosedCondition,
                                DomainConstants.EMPTY_STRING);
                return relIssueList;
        }

        /**
         * Method to obtain the related issues which are not in 'Closed' state.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries:
         *              ObjectId - The object Id of the context.
         * @return MapList containing the id of all Active Context Issue objects
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getActiveIssues(Context context, String[] args)
                throws Exception {
                //unpacking the Arguments from variable args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                //getting parent object Id from args
                String strParentId = (String) programMap.get(OBJECT_ID);
                //To get actual policy Name
                String strIssuePolicy =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_policy_Issue);

                //Getting the type name
                String strType =
                        PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);
                //String strProductType = ProductCentralDomainConstants.TYPE_PRODUCTS;
                String strClosedCondition = "";
                //Getting and representing the state 'Close'
                String strClose =
                        FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CLOSE);
                //StringBuffer variable to form the where expression
                StringBuffer sbClosedCondition = new StringBuffer(20);

                //Forming the expression
                sbClosedCondition.append(SYMB_OPEN_PARAN);
                sbClosedCondition.append(DomainConstants.SELECT_CURRENT);
                sbClosedCondition.append(SYMB_NOT_EQUAL);
                sbClosedCondition.append(SYMB_QUOTE);
                sbClosedCondition.append(strClose);
                sbClosedCondition.append(SYMB_QUOTE);
                sbClosedCondition.append(SYMB_CLOSE_PARAN);

                strClosedCondition = sbClosedCondition.toString();
                //getting the symbolic relationship from PropertyUtil
                //String strRelFoundIn = RELATIONSHIP_ISSUE;
                String strRelFoundIn =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_relationship_Issue);

                //Initializing the return type
                MapList relIssueList = null;
                //Business Objects are selected by its Ids
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				objectSelects.add("current.access[modify]");
                //Relationships are selected by its Ids
                StringList relSelects =
                        new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                //the number of levels to expand, 1 equals expand one level, 0 equals expand all
                short sRecurseToLevel = 1;
                //retrieving Issue List from Context
                this.setId(strParentId);
                relIssueList =
                        getRelatedObjects(
                                context,
                                strRelFoundIn,
                                strType,
                                objectSelects,
                                relSelects,
                                true,
                                false,
                                sRecurseToLevel,
                                strClosedCondition,
                                DomainConstants.EMPTY_STRING);
                return relIssueList;
        }

        /**
           * Method to obtain the Resolved to Objects.
           *
           * @param context the eMatrix <code>Context</code> object
           * @param args - args contains a Map with the following entries:
           *            ObjectId - The object Id of the context.
           * relationName - The name of the relationship 'Resolved To'
           * @return MapList containing the Object ids of Resolved To objects
           * @throws Exception if the operation fails
           * @since AEF 10.0.5.0
           */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getAllRelatedResolvedTo(Context context, String[] args)
                throws Exception {
                //Unpacks the argument for processing
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                //Gets the objectId in context
                String strObjectId = (String) programMap.get(OBJECT_ID);
                //String List initialized to retrieve back the data
                StringList relSelects =
                        new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
                //String strRelationship = RELATIONSHIP_RESOLVED_TO ;
                //Domain Object initialized with the object id.
                setId(strObjectId);
                short sh = 1;
                String relationshipName = (String) programMap.get(RELATION_NAME);
                String strActualRelName =
                        PropertyUtil.getSchemaProperty(context, relationshipName);
                RelationshipType relType = new RelationshipType(strActualRelName);
                BusinessTypeList BusTypeList = new BusinessTypeList();
                BusTypeList = relType.getToTypes(context);
                String strTypes = "";
                strTypes = getTypesList(BusTypeList);
                String strRelationship =
                        PropertyUtil.getSchemaProperty(context,relationshipName);
                MapList relBusObjPageList =
                        getRelatedObjects(
                                context,
                                strRelationship,
                                strTypes,
                                objectSelects,
                                relSelects,
                                false,
                                true,
                                sh,
                                DomainConstants.EMPTY_STRING,
                                DomainConstants.EMPTY_STRING);
                                        return relBusObjPageList;
        }

        /**
         * Method to get the Type for Reported Against Issues.
         *
         * @param BusTypeList - List of passed Business types
         * @return - string containing Type names with comma separated
         * @since AEF 10.0.5.0
         */
        protected String getTypesList(BusinessTypeList BusTypeList) {
                StringBuffer sbType = new StringBuffer(150);
                Iterator itr = BusTypeList.iterator();
                String strListTypeActual = "";
                String strFinalTypeName = "";

                while (itr.hasNext()) {
                        BusinessType busChildType = (BusinessType) itr.next();

                        strListTypeActual = "";
                        strFinalTypeName = "";
                        strListTypeActual = busChildType.toString();
                        //strFinalTypeName = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, strListTypeActual,true);
                        sbType.append(strListTypeActual);
                        sbType.append(",");
                }

                String strTypes = "";
                String strTypeTemp = "";
                strTypeTemp = sbType.toString();
                int iLen = strTypeTemp.length() - 1;
                strTypes = strTypeTemp.substring(0, iLen);
                return strTypes;
        }

        /**
           * To obtain the Reported Against Objects for Issue.
           *
           * @param context - the eMatrix <code>Context</code> object
           * @param args - args contains a Map with the following entries:
           *            ObjectId - The object Id of the context.
           * relationName - The name of the relationship 'Issue'
           * @return MapList containing the Object ids of Reported Against objects
           * @throws Exception if the operation fails
           * @since AEF 10.0.5.0
           */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getAllReportedAgainst(Context context, String[] args)
                throws Exception {
                //Unpacks the argument for processing
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                // Map requestMap = (Map)programMap.get("RequestValuesMap");
                //Gets the objectId in context
                String strObjectId = (String) programMap.get(OBJECT_ID);
                //String List initialized to retrieve back the data
                StringList relSelects =
                        new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                StringList objectSelects = new StringList(4);
                objectSelects.add(DomainConstants.SELECT_ID);
                objectSelects.add(DomainConstants.SELECT_NAME);
                objectSelects.add("vcfile");
                objectSelects.add("vcfolder");

                //Domain Object initialized with the object id.
                setId(strObjectId);
                short sh = 1;
                String relationshipName = (String) programMap.get(RELATION_NAME);
                String strActualRelName =
                        PropertyUtil.getSchemaProperty(context, relationshipName);
                RelationshipType relType = new RelationshipType(strActualRelName);
                BusinessTypeList BusTypeList = new BusinessTypeList();
                BusTypeList = relType.getToTypes(context);
                String strTypes = "";
                strTypes = getTypesList(BusTypeList);
                String strRelationship =
                        PropertyUtil.getSchemaProperty(context, relationshipName);
                //The getRelatedObjects method is invoked to get the list of Sub Requirements.
                MapList relBusObjPageList =
                        getRelatedObjects(
                                context,
                                strRelationship,
                                strTypes,
                                objectSelects,
                                relSelects,
                                false,
                                true,
                                sh,
                                DomainConstants.EMPTY_STRING,
                                DomainConstants.EMPTY_STRING);
                return relBusObjPageList;
        }
        /**
           * Method to obtain the Person Objects connected to issue.
           *
           * @param context - the eMatrix <code>Context</code> object
           * @param args - args contains a Map with the following entries:
           *            ObjectId - The object Id of the context.
           * relationName - The name of the relationship 'Assigned Issue'
           * @return MapList containing the Object ids of Assignees
           * @throws Exception if the operation fails
           * @since AEF 10.0.5.0
           */

        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getAllAssignee(Context context, String[] args)
                throws Exception {
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
                StringList relSelects =
                        new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                //Unpacking the args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                MapList relBusObjPageList = new MapList();
                //Gets the objectids and the relation names from args
                String strParamMap = (String) programMap.get(OBJECT_ID);
                String relationshipName = (String) programMap.get(RELATION_NAME);
                //Domain Object initialized with the object id.
                DomainObject dom = new DomainObject(strParamMap);
                short sLevel = 1;
                //Gets the relationship name
                String strRelName =
                        PropertyUtil.getSchemaProperty(context, relationshipName);
                //Getting actual type name - Person
                String strType =
                        PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Person);
                //The getRelatedObjects method is invoked to get the list of Assignees
                relBusObjPageList =
                        dom.getRelatedObjects(
                                context,
                                strRelName,
                                strType,
                                objectSelects,
                                relSelects,
                                true,
                                false,
                                sLevel,
                                "",
                                "");
                if (!(relBusObjPageList != null)) {
                        throw new Exception("Error!!! Context does not have any Objects.");
                }

                return relBusObjPageList;
        }

	/**
	   * Change the Owner of Issues only if the context user is having change owner access privilege
	   *
	   * @param context - the eMatrix <code>matrix.db.Context</code> object
	   * @param args - args contains a Map with the following entries:
	   *            reqMap - the request Map
	   *            reqTableMap - the table map of Issue
	   *            emxTableRowId - The table row Id of Owner
	   * @throws Exception if the operation fails
	   * @since AEF 10.0.5.0
	   */
	public void changeOwner(Context context, String[] args) throws Exception {
		
		//Check if the context user is having Change owner access 
		if(!hasAccess(context)) {		
			String strAlert =EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),ALERT_NO_PREVELEGE);
			MqlUtil.mqlCommand(context, "notice $1", strAlert);
			throw new FrameworkException(strAlert);
		}

		//Continue to change the owner only if the context user is having change owner access privilege
		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map requestMap = (HashMap) programMap.get(REQ_MAP);
		Map requestValuesMap = (HashMap) programMap.get(REQ_TABLE_MAP);
		String[] emxTableRowId = (String[]) requestMap.get(TABLE_ROW_ID);
		String strOwnerName = "";
		
		for (int i = 0; i < emxTableRowId.length; i++) {
			strOwnerName = newInstance(context, emxTableRowId[i]).getInfo(context, DomainConstants.SELECT_NAME);
		}
		
		String[] strRowId = ComponentsUIUtil.getSplitTableRowIds((String[])requestValuesMap.get(TABLE_ROW_ID));
		int iRowSize = strRowId.length;
		String[] arrBusIdList = new String[iRowSize];

		// for each table row 
		for (int i = 0; i < iRowSize; i++) {
			int iPosition = strRowId[i].indexOf("|");
			//Assume Bus id in case no '|' is found.
			if (iPosition == -1) {
				arrBusIdList[i] = strRowId[i];
			} else {
				arrBusIdList[i] = strRowId[i].substring(iPosition + 1);
			}
		}
		DomainObject dom = null;
		for (int j = 0; j < arrBusIdList.length; j++) {
			dom = newInstance(context, arrBusIdList[j]);
			dom.setOwner(context, strOwnerName);
		}
	}


	/**
	 * Access is granted only if the context user is having role Issue Manager or Analyst or OCDX/OneClick VPLMCreator. 
	 * @param context <code>matrix.db.Context</code>
	 * @return boolean true only if the context user is having change owner access privilege
	 * @throws MatrixException if resource is unavailable or configured wrongly
	 */
	private boolean hasAccess(Context context) throws MatrixException {
		boolean bValidUser = false;
		
		matrix.db.Person strContextUser = new matrix.db.Person(context.getUser());
		
		boolean bIssueManager = strContextUser.isAssigned(context, PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_role_IssueManager));
		
		boolean bAnalyst = strContextUser.isAssigned(context, PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_role_Analyst));
		
		boolean bVPLMCreator =  strContextUser.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_VPLMCreator"));         
		 
		if(bIssueManager || bAnalyst || bVPLMCreator){
			bValidUser = true;
		}
		
		return bValidUser;
	}
	
        /**
                   * Method to obtain the Persons.
                   *
                   * @param context - the eMatrix <code>Context</code> object
                   * @param args - args contains a Map with the following entries
                   *            queryLimit - The limit of the objects to be searched
                   *            User Name - The user name of the Person
                   *            First Name - The First name of the Person
                   *            Last Name - The Last name of the Person
                   *            ChangeOwner - mode passed
                   *            Company - The Company Name passed
                   *            vaultOption - The Vault option selected - All, Default, Selected
                   *            VaultDisplay - The displayed Vault name.
                   * @return MapList containing the Object ids of Persons matching the search criteria
                   * @throws Exception if the operation fails
                   * @since AEF 10.0.5.0
                    */
                @com.matrixone.apps.framework.ui.ProgramCallable
                public static MapList getPersons(Context context, String[] args)
                        throws Exception {

                        Map programMap = (Map) JPO.unpackArgs(args);


                         String strParentId = (String) programMap.get("strParentId");

                          String strParentType="" ;
                          if (strParentId != null
                                                && !strParentId.equals("")
                                && !("null".equalsIgnoreCase(strParentId))){
                           BusinessObject dom1= new BusinessObject(strParentId);
                           dom1.open(context);
                           strParentType = dom1.getTypeName();

                   }

                        short sQueryLimit =
                                (short) (java
                                        .lang
                                        .Integer
                                        .parseInt((String) programMap.get("queryLimit")));

                        String strType =
                                PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Person);

                        String strName = (String) programMap.get("User Name");

                        if (strName == null
                                || strName.equals("")
                                || "null".equalsIgnoreCase(strName)) {
                                strName = SYMB_WILD;
                        }

                        String strFirstName = (String) programMap.get("First Name");

                        String ChangeOwner = (String) programMap.get("ChangeOwner");

                        String strLastName = (String) programMap.get("Last Name");

                        String strCompany = (String) programMap.get("Company");

                        String strVault = "";
                        String strVaultOption = (String) programMap.get(VAULT_OPTION);

        if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)
                ||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)
                ||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                                                        strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
                                         else
                                                        strVault = (String)programMap.get("vaults");

                                                StringList slSelect = new StringList(1);
                        slSelect.addElement(DomainConstants.SELECT_ID);

                        //Getting the actual attribute name
                        String strAttribFirstName =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_FirstName);
                        //Getting the actual attribute name
                        String strAttribLastName =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_LastName);
                        //Getting the actual relation name
                        String strRelEmployee =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_relationship_Employee);

                        //Getting the actual relation name
                        String strRelMember =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                Issue.SYMBOLIC_relationship_Member);


                        //Getting the actual attribute name
                        String strAttribProjectRole =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_ProjectRole);
                                        //Getting the actual Type name
                        String strParentTypeName =
                                PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_type_ProjectSpace);


                        //Getting the actual attribute name
                        String strRoleIssueManager =PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_role_IssueManager);

                        boolean bStart = true;
                        StringBuffer sbWhereExp = new StringBuffer(100);
                        if (strFirstName != null
                                && (!strFirstName.equals(SYMB_WILD))
                                && (!strFirstName.equals(""))
                                && !("null".equalsIgnoreCase(strFirstName))) {
                                if (bStart) {
                                        sbWhereExp.append(SYMB_OPEN_PARAN);
                                        bStart = false;
                                }
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                sbWhereExp.append(SYMB_ATTRIBUTE);
                                sbWhereExp.append(SYMB_OPEN_BRACKET);
                                sbWhereExp.append(strAttribFirstName);
                                sbWhereExp.append(SYMB_CLOSE_BRACKET);
                                sbWhereExp.append(SYMB_MATCH);
                                sbWhereExp.append(SYMB_QUOTE);
                                sbWhereExp.append(strFirstName);
                                sbWhereExp.append(SYMB_QUOTE);
                                sbWhereExp.append(SYMB_CLOSE_PARAN);
                        }

                        if (strLastName != null
                                && (!strLastName.equals(SYMB_WILD))
                                && (!strLastName.equals(""))
                                && !("null".equalsIgnoreCase(strLastName))) {
                                if (bStart) {
                                        sbWhereExp.append(SYMB_OPEN_PARAN);
                                        bStart = false;
                                } else {
                                        sbWhereExp.append(SYMB_AND);
                                }
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                sbWhereExp.append(SYMB_ATTRIBUTE);
                                sbWhereExp.append(SYMB_OPEN_BRACKET);
                                //sbWhereExp.append(ATTRIBUTE_LAST_NAME);
                                sbWhereExp.append(strAttribLastName);
                                sbWhereExp.append(SYMB_CLOSE_BRACKET);
                                sbWhereExp.append(SYMB_MATCH);
                                sbWhereExp.append(SYMB_QUOTE);
                                sbWhereExp.append(strLastName);
                                sbWhereExp.append(SYMB_QUOTE);
                                sbWhereExp.append(SYMB_CLOSE_PARAN);

                        }

                        if(strParentType.equalsIgnoreCase(strParentTypeName)){
                            if (bStart) {
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                bStart = false;
                            } else {
                                sbWhereExp.append(SYMB_AND);
                        }
                sbWhereExp.append(SYMB_OPEN_PARAN);
                sbWhereExp.append(SYMB_QUOTE);
                sbWhereExp.append(SYMB_TO);
                sbWhereExp.append(SYMB_OPEN_BRACKET);
                sbWhereExp.append(strRelMember);
                sbWhereExp.append(SYMB_CLOSE_BRACKET);
                sbWhereExp.append(SYMB_DOT);
                sbWhereExp.append(SYMB_FROM);
                sbWhereExp.append(SYMB_DOT);
                sbWhereExp.append(DomainConstants.SELECT_ID);
                sbWhereExp.append(SYMB_QUOTE);
                sbWhereExp.append(SYMB_EQUAL);
                sbWhereExp.append(strParentId);
                sbWhereExp.append(SYMB_CLOSE_PARAN);
                        }

                        if (strCompany != null
                                && (!strCompany.equals(SYMB_WILD))
                                && (!strCompany.equals(""))
                                && !("null".equalsIgnoreCase(strCompany))) {
                                if (bStart) {
                                        sbWhereExp.append(SYMB_OPEN_PARAN);
                                        bStart = false;
                                } else {
                                        sbWhereExp.append(SYMB_AND);
                                }
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                sbWhereExp.append(SYMB_TO);
                                sbWhereExp.append(SYMB_OPEN_BRACKET);
                                //sbWhereExp.append(RELATIONSHIP_EMPLOYEE);
                                sbWhereExp.append(strRelEmployee);
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



                        String strFilteredExpression = getFilteredExpression(context,programMap);

                        if ((strFilteredExpression != null)
                                && !("null".equalsIgnoreCase(strFilteredExpression)) && !strFilteredExpression.equals("")) {
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

                        // This condition is added to check for the inactive persons
                        // Only the Active Persons will be displayed in the Search result
                        if(sbWhereExp.length() > 0){
                        sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        sbWhereExp.append("current==Active");
                        sbWhereExp.append(SYMB_CLOSE_PARAN);

                        MapList mapList = null;
            if(!ChangeOwner.equals("true"))
            {
                                mapList =
                                                    DomainObject.findObjects(
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
                    return mapList;
            }
            else{

                    mapList =
                                                DomainObject.findObjects(
                                context,
                                strType,
                                strName,
                                SYMB_WILD,
                                SYMB_WILD,
                                strVault,
                                sbWhereExp.toString(),
                                true,
                                slSelect);

                    Role role= new Role(strRoleIssueManager);
                                    UserList userList = role.getAssignments(context);
/* Added for Including Analyst Role Users  */
            Role roleAnalyst = new Role(PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_role_Analyst));
            UserList userAnalystList = roleAnalyst.getAssignments(context);

            Iterator analystIterator =  userAnalystList.iterator();
 /* Added for Including Analyst Role Users  */
                                    Iterator Useriterator =  userList.iterator();
                                    MapList mapListIssueManager = new MapList();
                                    int iCount=1;
                    while (Useriterator.hasNext())
                                    {
                                              User personId = (User) Useriterator.next();
                                              String strPersonName =personId.getName();
                          BusinessObject dom= new BusinessObject(strType,strPersonName,"-","");
                          dom.open(context);
                          String strPersonId = dom.getObjectId();
                          String typeName=dom.getTypeName();
                                          Map map = new HashMap();
                          map.put(DomainConstants.SELECT_TYPE,typeName);
                                          map.put(DomainConstants.SELECT_ID,strPersonId);
                          if(mapList.contains(map))
                          {
                              if(iCount<=sQueryLimit) {
                                    mapListIssueManager.add(map);
                                    iCount++;
                                                   }
                              else
                               {
                                        String [ ] formatArgs = new String[1] ;
                                        formatArgs[0] = String.valueOf(sQueryLimit);
                                        String strWarning = MessageUtil.getMessage(context,null,"emxComponents.Warning.ObjectFindLimitReached",formatArgs,null,context.getLocale(),"emxComponentsStringResource");
                                        emxContextUtil_mxJPO.mqlNotice(context, strWarning);
                                    break;
                              }
                          }
                           }
                   return mapListIssueManager;
                        }
}

        /**
         * The function to filter the object selection and apppend the default query in the where clause.
         *
                 * @param context - the eMatrix <code>Context</code> object
         * @param programMap - with the following entries
         *              object Id - Object Id of the context object
         * @return - String after constructing the Where clause appropriately
         * @throws Exception when problems occurred in the AEF
         * @since AEF 10.0.5.0
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
                        strMidDestRelName =
                                PropertyUtil.getSchemaProperty(context,strMidDestRelNameSymb);
                }

                String strSrcMidRelName = "";
                if (strSrcMidRelNameSymb != null
                        && !strSrcMidRelNameSymb.equals("")
                        && !("null".equalsIgnoreCase(strSrcMidRelNameSymb))) {
                        strSrcMidRelName =
                                PropertyUtil.getSchemaProperty(context,strSrcMidRelNameSymb);
                }

                String strSrcDestRelName = "";
                if (strSrcDestRelNameSymb != null
                        && !strSrcDestRelNameSymb.equals("")
                        && !("null".equalsIgnoreCase(strSrcDestRelNameSymb))) {
                        strSrcDestRelName =
                                PropertyUtil.getSchemaProperty(context,strSrcDestRelNameSymb);
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

                                if (strIsTo.equalsIgnoreCase(TRUE)) {
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

/**To Obtain All the company vaults for the wild card search.
 *
 * @param context - the eMatrix <code>Context</code> object
 * @return String containing all the vaults of the company
 * @throws Exception if the operation fails
 * @since AEF 10.0.5.0
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
         * @since AEF 10.0.5.0
         */
       protected static String getAllVaults(Context context) throws Exception {
          //  Person person = Person.getPerson(context, context.getUser());
           return getAllCompanyVaults(context);
  }

        /**
         * The method to get default vault.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds no arguments
         * @return String containing Default vault.
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
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
                                          radioOption.append("&nbsp;&nbsp;<input id=\"displayVault\" type=\"text\" READONLY name=\"vaultsDisplay\" value =\""+selDisplayVault+"\" id=\"\" size=\"20\" onFocus=\"this.blur();\">");
                                          radioOption.append("<input type=\"button\" name=\"VaultChooseButton\" value=\"...\" onclick=\"document.forms[0].vaultOption[2].checked=true;javascript:getTopWindow().showChooser('../common/emxVaultChooser.jsp?fieldNameActual=vaults&fieldNameDisplay=vaultsDisplay&multiSelect=true&isFromSearchForm=true')\">");
                                          radioOption.append("<input id=\"vaults\" type=\"hidden\" name=\"vaults\" value=\"");
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
                                          radioOption.append(XSSUtil.encodeForHTML(context,strAll));

                } catch (Throwable excp) {

                }

                return radioOption.toString();
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
           * @since AEF 10.0.5.0
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

                    //Bug 337556 - Start- Commented below code

                        //boolean bStart = true;

                    //Bug 337556 - End

                        StringBuffer sbWhereExp = new StringBuffer(100);

                    //Bug 337556 - Start- Commented below code

                        /*String strAttribOrgName =
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
                        }*/

                    //Bug 337556 - End

                        String strFilteredExpression = getFilteredExpression(context,programMap);
                        if ((strFilteredExpression != null)
                                && !("null".equalsIgnoreCase(strFilteredExpression))
                                && !strFilteredExpression.equals("")) {

                            //Bug 337556 - Start

                                /*if (bStart) {
                                        sbWhereExp.append(SYMB_OPEN_PARAN);
                                        bStart = false;
                                } else {
                                        sbWhereExp.append(SYMB_AND);
                                }*/

                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                sbWhereExp.append(strFilteredExpression);
                                sbWhereExp.append(SYMB_CLOSE_PARAN);
                            //Bug 337556 - End
                        }

                    //Bug 337556 - Start- Commented below code

                        /*if (!bStart) {
                                sbWhereExp.append(SYMB_CLOSE_PARAN);
                        }*/

                    //Bug 337556 - End

                        mapList =
                                DomainObject.findObjects(
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
                        throw excp;
                }

                return mapList;
        }

        /**
        * Method to get Resolved to or Reported Against objects for Issues.
        *
        * @param context - the eMatrix <code>Context</code> object
        * @param args - Holds the HashMap containing following arguments
                    queryLimit
                        hdnType
                        txtName
                        txtDescription
                        txtOwner
                        txtState.
        * @return - Maplist containing the Reported Against or Resolved To objects
        * @throws Exception if the operation fails
        * @since AEF 10.0.5.0
        */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public static MapList findResolvedToReportedAgainst(
                Context context,
                String[] args)
                throws Exception {
                MapList mapList = null;
                try {

                        Map programMap = (Map) JPO.unpackArgs(args);
                        short sQueryLimit =
                                (short) (java
                                        .lang
                                        .Integer
                                        .parseInt((String) programMap.get("queryLimit")));

                        String strType = (String) programMap.get("hdnType");

                        if (strType == null
                                || strType.equals("")
                                || "null".equalsIgnoreCase(strType)) {
                                strType = SYMB_WILD;
                        }

                        String strName = (String) programMap.get("txtName");

                        if (strName == null
                                || strName.equals("")
                                || "null".equalsIgnoreCase(strName)) {
                                strName = SYMB_WILD;
                        }

                        String strRevision = (String) programMap.get("txtRevision");

                        if (strRevision == null
                                || strRevision.equals("")
                                || "null".equalsIgnoreCase(strRevision)) {
                                strRevision = SYMB_WILD;
                        } else {
                                strRevision = strRevision.trim();
                        }

                        String strDesc = (String) programMap.get("txtDescription");

                        String strOwner = (String) programMap.get("txtOwner");

                        if (strOwner == null
                                || strOwner.equals("")
                                || "null".equalsIgnoreCase(strOwner)) {
                                strOwner = SYMB_WILD;
                        } else {
                                strOwner = strOwner.trim();
                        }

                        String strState = (String) programMap.get("txtState");

                        if (strState == null
                                || strState.equals("")
                                || "null".equalsIgnoreCase(strState)) {
                                strState = SYMB_WILD;
                        } else {
                                strState = strState.trim();
                        }

                        String strVault = "";
                        String strVaultOption = (String) programMap.get(VAULT_OPTION);

                          if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                                                                        strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
                                                 else
                                                                        strVault = (String)programMap.get("vaults");

                        StringList slSelect = new StringList(1);
                        slSelect.addElement(DomainConstants.SELECT_ID);

                        boolean bStart = true;
                        StringBuffer sbWhereExp = new StringBuffer(150);

                        if (strDesc != null
                                && (!strDesc.equals(SYMB_WILD))
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
                                && (!strState.equals(SYMB_WILD))
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
                                sbWhereExp.append(SYMB_QUOTE);
                                sbWhereExp.append(strState);
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
                        sbWhereExp.append(DomainConstants.SELECT_POLICY);
                        sbWhereExp.append(SYMB_NOT_EQUAL);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(POLICY_VERSION);
                        sbWhereExp.append(SYMB_QUOTE);
						sbWhereExp.append(SYMB_AND);
						sbWhereExp.append(DomainConstants.SELECT_POLICY);
						sbWhereExp.append(SYMB_NOT_EQUAL);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(POLICY_VERSIONED_DESIGN_POLICY);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(SYMB_CLOSE_PARAN);
	
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


                        if(strType.equalsIgnoreCase(TYPE_DOCUMENTS))
                        {
                        	if (!bStart) {
                        		sbWhereExp.append(SYMB_AND);
                        	}
                        	 sbWhereExp.append(SYMB_OPEN_PARAN);
                        	 sbWhereExp.append("attribute["+DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT+"] ~~ \"*"+ "false" +"*\"");
                        	 sbWhereExp.append(SYMB_CLOSE_PARAN);
                        }
                        if (!bStart) {
                            sbWhereExp.append(SYMB_CLOSE_PARAN);
                        }


                        mapList =
                                DomainObject.findObjects(
                                        context,
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
                        throw excp;
                }

                return mapList;
        }
        /**
                * Method call to get the email as an link to send mails using the client.
                *
                * @param context the eMatrix <code>Context</code> object
                * @param args args contains a Map with the following entries
                *       objectList - list of assignee objects
                * @return Vector - containing email ids
                * @throws Exception if the operation fails
                * @since AEF 10.0.5.0
                * @deprecated @since R211
                * This method is deprecated in R211,
                * just need to add the format = email in the column/ field no need to have program html column/field
                */
        public Vector getAssigneeEmail(Context context, String[] args)
                throws Exception {
                //Unpacking the args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                //Gets the objectList from args
                MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);

//              Begin : Bug 346997 code modification
                HashMap paramList = (HashMap) programMap.get(PARAM_LIST);
                boolean isExporting = (paramList.get("reportFormat") != null);
//              End : Bug 346997 code modification

                Vector vctAssigneeMailList = new Vector();
                String strAttribEmail =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_attribute_AttributeEmailAddress);

                if (!(relBusObjPageList != null)) {
                        throw new Exception("Error!!! Context does not have any Objects.");
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

                StringBuffer sbAttribEmail = new StringBuffer(30);
                sbAttribEmail.append(SYMB_ATTRIBUTE);
                sbAttribEmail.append(SYMB_OPEN_BRACKET);
                sbAttribEmail.append(strAttribEmail);
                sbAttribEmail.append(SYMB_CLOSE_BRACKET);

                String strAttrb1 = sbAttribEmail.toString();

                StringList listSelect = new StringList(1);
                listSelect.addElement(strAttrb1);

                //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
                BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

                String strEmailAdd = "";
                for (int i = 0; i < iNoOfObjects; i++) {
                        //Getting the email ids from the Map
                        String strEmailId = attributeList.getElement(i).getSelectData(strAttrb1);
//                      Begin : Bug 346997 code modification
                        if (!isExporting) {
                            strEmailAdd =
                                "<B><A HREF=\"mailto:"
                                        + strEmailId
                                        + "\">"
                                        + strEmailId
                                        + "</A>";
                        }
                        else {
                            strEmailAdd = strEmailId;
                        }
//                      End : Bug 346997 code modification

                        vctAssigneeMailList.add(strEmailAdd);
                }
                return vctAssigneeMailList;
        }
                /**
            * Method call to get the Name in the Last Name, First Name format.
            *
            * @param context - the eMatrix <code>Context</code> object
                * @param args - Holds the parameters passed from the calling method
                            objectList
                                paramList.
            * @return Object - Vector containing names in last name, first name format
            * @throws Exception if the operation fails
            * @since AEF 10.0.5.0
            * @deprecated @since R211
            * Just use format = user for column or field instead of having programHTMLOutput
            */
        public Vector getCompleteName(Context context, String[] args)
                throws Exception {
                //Unpacking the args
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                //Gets the objectList from args
                MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
                HashMap paramList = (HashMap) programMap.get(PARAM_LIST);

//              Begin : Bug 346997 code modification
                boolean isExporting = (paramList.get("reportFormat") != null);
//              End : Bug 346997 code modification

                //Used to construct the HREF
                String strSuiteDir = (String) paramList.get(SUITE_DIR);
                String strJsTreeID = (String) paramList.get(JS_TREE_ID);
                String strParentObjectId = (String) paramList.get(OBJECT_ID);
                //Begin of modify by Infosys for bug 297901, 04/19/2005
                String strPortalMode = (String) paramList.get("portalMode");
                String strLaunchMode = (String) paramList.get("launched");
                //End of modify by Infosys for bug 297901, 04/19/2005
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

                //Getting the actual attribute name
                String strAttribFirstName =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_attribute_FirstName);
                //Getting the actual attribute name
                String strAttribLastName =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_attribute_LastName);

                StringBuffer sbFirstName = new StringBuffer(20);
                sbFirstName.append(SYMB_ATTRIBUTE);
                sbFirstName.append(SYMB_OPEN_BRACKET);
                sbFirstName.append(strAttribFirstName);
                sbFirstName.append(SYMB_CLOSE_BRACKET);

                StringBuffer sbLastName = new StringBuffer(20);
                sbLastName.append(SYMB_ATTRIBUTE);
                sbLastName.append(SYMB_OPEN_BRACKET);
                sbLastName.append(strAttribLastName);
                sbLastName.append(SYMB_CLOSE_BRACKET);

                StringList listSelect = new StringList(2);
                String strAttrb1 = sbFirstName.toString();
                String strAttrb2 = sbLastName.toString();
                listSelect.addElement(strAttrb1);
                listSelect.addElement(strAttrb2);

                //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
                BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

                String strFullName = "";

                for (int i = 0; i < iNoOfObjects; i++) {
                       strObjId = arrObjId[i];
                       strRelId = arrRelId[i];
                       strFirstName = attributeList.getElement(i).getSelectData(strAttrb1);
                       strLastName = attributeList.getElement(i).getSelectData(strAttrb2);
                       //Begin of modify by Infosys for bug 297901, 04/19/2005
                       if (((strPortalMode != null) && !("".equals(strPortalMode)) &&
                           !("null".equals(strPortalMode)) && (strPortalMode.equals("true"))) ||
                            ((strLaunchMode != null) && !("".equals(strLaunchMode)) &&
                           !("null".equals(strLaunchMode)) && (strLaunchMode.equals("true"))))
                       {
                        //Constructing the HREF
                        strFullName =
                               "<img src = \"images/iconSmallPerson.gif\"><b><A HREF=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?mode=insert&name="+strLastName+strFirstName+"&treeMenu=type_Person&emxSuiteDirectory="//Modified by Infosys for Bug # 304580 Date 05/18/2005
                                    + strSuiteDir
                                    + "&relId="
                                    + strRelId
                                    + "&parentOID="
                                    + strParentObjectId
                                    + "&jsTreeID="
                                    + strJsTreeID
                                    + "&objectId="
                                    + strObjId
                                    + "', 'null', 'null', 'false', 'popup')\" class=\"object\">"
                                    + strLastName
                                    + ", "
                                    + strFirstName
                                    + "</A>";
                       }
//                     Begin : Bug 346997 code modification
                       else if (!isExporting){
//                     End : Bug 346997 code modification
                        //Constructing the HREF
                        strFullName =
                                  /*Begin of modify : by Infosys for Bug#301835 on 4/13/2005*/
                                   "<img src = \"images/iconSmallPerson.gif\"><b><A HREF=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?mode=insert&name="+strLastName+strFirstName+"&treeMenu=type_Person&emxSuiteDirectory="//Modified by Infosys for Bug # 304580 Date 05/18/2005
                                   /*End of modify : by Infosys for Bug#301835 on 4/13/2005*/
                                        + strSuiteDir
                                        + "&relId="
                                        + strRelId
                                        + "&parentOID="
                                        + strParentObjectId
                                        + "&jsTreeID="
                                        + strJsTreeID
                                        + "&objectId="
                                        + strObjId
                                        + "', 'null', 'null', 'false', 'content')\" class=\"object\">"
                                        + strLastName
                                        + ", "
                                        + strFirstName
                                        + "</A>";
                       }
//                     Begin : Bug 346997 code modification
                       else {
                           strFullName = strLastName + ", " + strFirstName;
                       }
//                     End : Bug 346997 code modification

                        //End of modify by Infosys for bug 297901, 04/19/2005
                        //Adding into the vector
                        vctCompleteName.add(strFullName);
                }
                return vctCompleteName;
        }
        /**
          * Method call to update the Reporting Organization of the Issue as chosen by the user in the form page.
          *
          * @param context - the eMatrix <code>Context</code> object
          * @param args - args contains a Map with the following entries:
          *             paramMap - the Map of the parameters
          *             New Value - The new Value of Organization Name
          * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
          * @throws Exception if the operation fails
          * @since AEF 10.0.5.0
          */
        public int updateCompany(Context context, String[] args) throws Exception
        {

                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                HashMap paramMap = (HashMap) programMap.get("paramMap");
                String strObjectId = (String) paramMap.get(OBJECT_ID);
                this.setId(strObjectId);

                // Begin of add for Bug #311484 by Amrut, Infosys. 10 Nov 2005
                //To get actual policy Name
                String strIssuePolicy =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_policy_Issue);
                //Getting and representing the state 'Create'
                String strCreate =
                        FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CREATE);


                //Getting the current state
                String strCurrentState =
                        getInfo(context, DomainConstants.SELECT_CURRENT);

                if(!strCurrentState.equals(strCreate))
                {
                // End of add for Bug #311484 by Amrut, Infosys. 10 Nov 2005
                    //The new object id of the Organization that has to be used to connect with the issue in context
                    String strNewValue = (String) paramMap.get("New OID");
                    String strRelReportingOrganization =
                            PropertyUtil.getSchemaProperty(
                                    context,
                                    Issue.SYMBOLIC_relationship_ReportingOrganization);
                    updateConnection(
                            context,
                            strObjectId,
                            strRelReportingOrganization,
                            strNewValue,
                            false);
                }
                return 0;
        }


        /**
          * Method call to update the relationship.
          *
          * @param context - the eMatrix <code>Context</code> object
          * @param parentObjectId - Id of Issue
          * @param strRelationshipName - Relationship Name passed by the calling method
          * @param strNewObjectId - New Id of Organization
          * @param bIsFrom - boolean value to indicate 'From' type
          * @throws Exception if the operation fails
          * @since AEF 10.0.5.0
          */
        protected void updateConnection(
                Context context,
                String parentObjectId,
                String strRelationshipName,
                String strNewObjectId,
                boolean bIsFrom)
                throws Exception {
                StringList relSelect =
                        new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                //Context set with the product id
                this.setId(parentObjectId);

                //Relationship id of the previous Organization and Issue is fetched for disconnecting it.
                Map objectMap =
                        this.getRelatedObject(
                                context,
                                strRelationshipName,
                                bIsFrom,
                                null,
                                relSelect);

                if (objectMap != null) {
                        String strRelId =
                                (String) objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);

                        //The relationship is disconnected
                        DomainRelationship.disconnect(context, strRelId);
                }
                //Function loop to avoid the connection from being established if no objects is being passed.
                if (!(strNewObjectId.equals("")
                        || strNewObjectId.equals("null")
                        || strNewObjectId == null)) {
                        BusinessObject tempBO = new BusinessObject(strNewObjectId);
                        //The new Organization Id id is connected to the context product id
                        this.connect(
                                context,
                                new RelationshipType(strRelationshipName),
                                bIsFrom,
                                tempBO);
                }

        }
        /**
         * This method returns the default policy states of the type "Issue".
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args holds no arguments
         * @return MapList containing the Object ids matching the search criteria
         * @throws Exception if the operation fails
         * @since AEF 10-0-5-0
         */
        public static Object getIssueStates(Context context, String[] args)
                throws Exception {
                String strType =
                        PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);
                return getStates(context, strType);
        }

        /**
             * This method returns the default policy states.
             *
             * @param context - the eMatrix <code>Context</code> object
             * @param strType - Type name for getting states
             * @return String containing the default policy state.
             * @throws Exception if the operation fails
             * @since AEF 10.0.5.0
         */
        protected static String getStates(Context context, String strType)
                throws Exception {

                Map defaultMap = mxType.getDefaultPolicy(context, strType, false);
                String strPolicy = (String)defaultMap.get(DomainConstants.SELECT_NAME);

                BusinessType btType = new BusinessType(strType, context.getVault());
                btType.open(context, false);

                //To get the Find Like information of the business type selected
                FindLikeInfo fLikeObj = btType.getFindLikeInfo(context);
                List list = fLikeObj.getStates();

                String strLocale = context.getSession().getLanguage();
                i18nNow i18nNowInstance = new i18nNow();
                String strAllStates =
                        i18nNowInstance.GetString(
                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                strLocale,
                                ALL_STATES);
                StringBuffer sb = new StringBuffer(70);

                sb.append("<select name=\"State\"> <option value=\"*\">");
                sb.append(strAllStates);
                sb.append("</option>");
                String strState = "";
                int iCount = list.size();
                //If only one state is there in policy.
                if (iCount == 1)
                {
                        strState = (String) list.get(0);
                        sb.append("<option value=\"*\">");
                        sb.append(strState);
                        sb.append("</option>");
                }
                else
                {
                        for (int i = 0; i < list.size(); i++) {
                                strState = (String) list.get(i);
                                sb.append("<option value=\"");
                                sb.append(strState + "\">");
                                sb.append(i18nNow.getStateI18NString(strPolicy, strState ,strLocale));
                                sb.append("</option>");
                        }
                }
                sb.append("</select>");

                return sb.toString();

        }
        /**  This method Obtains the HTML format of Priority Choices.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds no arguments
         * @return MapList containing the Object ids matching the search criteria
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public static String getPriorityChoicesHTML(Context context, String args[])
                throws Exception {
                String strRetValue = "";
                try {
                        String strAttribPriority =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_Priority);

                        AttributeType atType = new AttributeType(strAttribPriority);
                        String strLocale = context.getSession().getLanguage();
                        i18nNow i18nNowInstance = new i18nNow();

                        atType.open(context);
                        List list = atType.getChoices();
                        atType.close(context);

                        if (list != null) {

                                StringBuffer sb = new StringBuffer(70);
                                sb.append(
                                        "<select name=\"Priority\"> <option value=\"*\">*</option>");

                                String strChoice = "";
                                for (int i = 0; i < list.size(); i++) {
                                        strChoice = (String) list.get(i);
                                        sb.append("<option value=\"");
                                        sb.append(XSSUtil.encodeForHTMLAttribute(context,strChoice) + "\">");
                                        sb.append(i18nNow.getRangeI18NString(strAttribPriority, strChoice ,strLocale));
                                        sb.append("</option>");
                                }
                                sb.append("</select>");

                                strRetValue = sb.toString();
                        }
                } catch (Exception excp) {
                        throw excp;
                }

                return strRetValue;
        }

        /**
         * Method to get the Company Name through chooser.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds no arguments
         * @return - string containing Organization name
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
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
                        throw ex;
                }
        }
                /**
         * Method to get the Default Company Name of teh User.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds no arguments
         * @return - string containing User Organization name
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public String getDefaultCompanyNameHTML(
                Context context,
                String[] args)
                throws Exception {
                String strUserName = context.getUser();
                Person personObj =      Person.getPerson(context,strUserName);
                String strUserCompanyId= personObj.getCompanyId(context);

                DomainObject companyObject = newInstance(context, strUserCompanyId);
                String strUserCompanyName =
                                                        companyObject.getInfo(context, DomainConstants.SELECT_NAME);

                        StringBuffer strDefaultCompanyName = new StringBuffer(150);


                        strDefaultCompanyName.append(
                                "<input type=\"text\" READONLY name=\"OrganizationDisplay\" id=\"\" value=\""
                                        + XSSUtil.encodeForHTMLAttribute(context,strUserCompanyName)
                                        +"\"");

                        strDefaultCompanyName.append(
                                " maxlength=\"\" size=\"\"><input type=\"hidden\" name=\"Organization\"  value=\""
                                        + XSSUtil.encodeForHTMLAttribute(context,strUserCompanyId)
                                        + "\">");
                        strDefaultCompanyName.append(
                                "<input type=\"hidden\" name=\"OrganizationOID\" value=\""
                                        + XSSUtil.encodeForHTMLAttribute(context,strUserCompanyId)
                                        + "\">");
                        strDefaultCompanyName.append("<input type=\"button\" value=\"...\" onclick=\"javascript:getTopWindow().showChooser('../components/emxCommonSearch.jsp?formName=editDataForm&frameName=searchPane&searchmode=chooser&suiteKey=Components&searchmenu=SearchIssueAddExistingChooser&searchcommand=IssueSearchCompanyCommand&fieldNameActual=Organization&fieldNameDisplay=OrganizationDisplay','700','500')\">");
                        return strDefaultCompanyName.toString();

        }

        /**
         * To obtain the Issues.
         *
         * @param context - the eMatrix <code>Context</code> object
                 * @param args - Holds the Hashmap conatining the following arguments
                                 queryLimit
                                 Type
                                 Name
                                 Description
                                 Priority
                                 OriginatorDisplay
                                 IssueCategoryClassificationOID
                                 OwnerDisplay
                                 Organization
                                 State.
                * @return MapList containing the Object ids matching the search criteria
                * @throws Exception if the operation fails
                * @since AEF 10-0-5-0
                */

        @com.matrixone.apps.framework.ui.ProgramCallable
        public static MapList getFindIssues(Context context, String[] args)
                throws Exception {

                Map programMap = (Map) JPO.unpackArgs(args);

                short sQueryLimit =
                        (short) (java
                                .lang
                                .Integer
                                .parseInt((String) programMap.get("queryLimit")));

                //to get actual name of Type Organization
                String strTypeOrganization =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_type_Organization);

                String strType = (String) programMap.get("Type");

                if (strType == null
                        || strType.equals("")
                        || "null".equalsIgnoreCase(strType)) {
                        strType = SYMB_WILD;
                }

                String strName = (String) programMap.get("Name");
                if (strName == null
                        || strName.equals("")
                        || "null".equalsIgnoreCase(strName)) {
                        strName = SYMB_WILD;
                }

                String strState = (String) programMap.get("State");

                String strPriority = (String) programMap.get("Priority");

                String strOwner = (String) programMap.get("OwnerDisplay");
                if (strOwner == null
                        || strOwner.equals("")
                        || "null".equalsIgnoreCase(strOwner)) {
                        strOwner = SYMB_WILD;
                }
                String strOrganization = (String) programMap.get("Organization");
                if (strOrganization == null
                        || strOrganization.equals("")
                        || "null".equalsIgnoreCase(strOrganization)) {
                        strOrganization = SYMB_WILD;
                }
                String strOriginator = (String) programMap.get("OriginatorDisplay");
                if (strOriginator == null
                        || strOriginator.equals("")
                        || "null".equalsIgnoreCase(strOriginator)) {
                        strOriginator = SYMB_WILD;
                }
                String strDesc = (String) programMap.get("Description");

                String strIssueCategoryClassification =
                        (String) programMap.get("IssueCategoryClassificationOID");
                if (strIssueCategoryClassification == null
                        || strIssueCategoryClassification.equals("")
                        || "null".equalsIgnoreCase(strIssueCategoryClassification)) {
                        strIssueCategoryClassification = SYMB_WILD;
                }

                String strCategory = SYMB_WILD;
                String strClassification = SYMB_WILD;
                //to separate Category and Classification
                if (!strIssueCategoryClassification.equals(SYMB_WILD)) {
                        int iIndex = strIssueCategoryClassification.indexOf('/');
                        strCategory = strIssueCategoryClassification.substring(0, iIndex);

                        strClassification =
                                strIssueCategoryClassification.substring(
                                        iIndex + 1,
                                        strIssueCategoryClassification.length());

                }

                String strVault = "";
                String strVaultOption = (String) programMap.get(VAULT_OPTION);

                        if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                                        strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
                         else
                                        strVault = (String)programMap.get("vaults");

                StringList select = new StringList(1);
                select.addElement(DomainConstants.SELECT_ID);

                boolean bStart = true;
                StringBuffer sbWhereExp = new StringBuffer(120);

                String strAttribIssueCategory =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_attribute_IssueCategory);
                String strAttribIssueClassification =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_attribute_IssueClassification);
                String strAttribPriority =
                        PropertyUtil.getSchemaProperty(
                                context,
                                Issue.SYMBOLIC_attribute_Priority);
                                String strRelReportingOrg =
                                                        PropertyUtil.getSchemaProperty(
                                                                context,
                                Issue.SYMBOLIC_relationship_ReportingOrganization);

                if (strDesc != null
                        && (!strDesc.equals(SYMB_WILD))
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

                if (strPriority != null
                        && (!strPriority.equals(SYMB_WILD))
                        && (!strPriority.equals(""))
                        && !("null".equalsIgnoreCase(strPriority))) {
                        if (bStart) {
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                bStart = false;
                        } else {
                                sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        sbWhereExp.append(SYMB_ATTRIBUTE);
                        sbWhereExp.append(SYMB_OPEN_BRACKET);
                        //sbWhereExp.append(ATTRIBUTE_PRIORITY);
                        sbWhereExp.append(strAttribPriority);
                        sbWhereExp.append(SYMB_CLOSE_BRACKET);
                        sbWhereExp.append(SYMB_MATCH);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(strPriority);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(SYMB_CLOSE_PARAN);
                }

                if (strCategory != null
                        && (!strCategory.equals(SYMB_WILD))
                        && (!strCategory.equals(""))
                        && !("null".equalsIgnoreCase(strCategory))) {
                        if (bStart) {
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                bStart = false;
                        } else {
                                sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        sbWhereExp.append(SYMB_ATTRIBUTE);
                        sbWhereExp.append(SYMB_OPEN_BRACKET);
                        //sbWhereExp.append(ATTRIBUTE_ISSUE_CATEGORY);
                        sbWhereExp.append(strAttribIssueCategory);
                        sbWhereExp.append(SYMB_CLOSE_BRACKET);
                        sbWhereExp.append(SYMB_MATCH);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(strCategory);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(SYMB_CLOSE_PARAN);

                }
                if (strClassification != null
                        && (!strClassification.equals(SYMB_WILD))
                        && (!strClassification.equals(""))
                        && !("null".equalsIgnoreCase(strClassification))) {
                        if (bStart) {
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                bStart = false;
                        } else {
                                sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        sbWhereExp.append(SYMB_ATTRIBUTE);
                        sbWhereExp.append(SYMB_OPEN_BRACKET);
                        //sbWhereExp.append(ATTRIBUTE_ISSUE_CLASSIFICATION);
                        sbWhereExp.append(strAttribIssueClassification);
                        sbWhereExp.append(SYMB_CLOSE_BRACKET);
                        sbWhereExp.append(SYMB_MATCH);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(strClassification);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(SYMB_CLOSE_PARAN);
                }

                if (strState != null
                        && (!strState.equals(SYMB_WILD))
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
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(strState);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(SYMB_CLOSE_PARAN);
                }
                if ((strOriginator != null)
                        && (!strOriginator.equals(SYMB_WILD))
                        && (!strOriginator.equals(""))
                        && !("null".equalsIgnoreCase(strOriginator))) {
                        if (bStart) {
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                bStart = false;
                        } else {
                                sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        sbWhereExp.append(DomainConstants.SELECT_ORIGINATOR);
                        sbWhereExp.append(SYMB_MATCH);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(strOriginator);
                        sbWhereExp.append(SYMB_QUOTE);
                        sbWhereExp.append(SYMB_CLOSE_PARAN);
                }
                if ((strOrganization != null)
                        && (!strOrganization.equals(SYMB_WILD))
                        && (!strOrganization.equals(""))
                        && !("null".equalsIgnoreCase(strOrganization))) {
                        if (bStart) {
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                bStart = false;
                        } else {
                                sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                                                        sbWhereExp.append(SYMB_TO);
                                                        sbWhereExp.append(SYMB_OPEN_BRACKET);
                                                        sbWhereExp.append(strRelReportingOrg);
                                                        sbWhereExp.append(SYMB_CLOSE_BRACKET);
                                                        sbWhereExp.append(SYMB_DOT);
                                                        sbWhereExp.append(SYMB_FROM);
                                                        sbWhereExp.append(SYMB_DOT);
                                                        sbWhereExp.append(DomainConstants.SELECT_ID);
                                                        sbWhereExp.append(SYMB_MATCH);
                                                        sbWhereExp.append(SYMB_QUOTE);
                                                        sbWhereExp.append(strOrganization);
                                                        sbWhereExp.append(SYMB_QUOTE);
                                                        sbWhereExp.append(SYMB_CLOSE_PARAN);

                }
                String strFilteredExpression = getFilteredExpression(context,programMap);
                if(!UIUtil.isNullOrEmpty(strFilteredExpression)){
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

                MapList mapList = null;
                mapList =
                        DomainObject.findObjects(
                                context,
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
              //  return mapList;
                //Getting the size of the MapList
                        int iSize = mapList.size();
                        String strObjectId = "";
                        //Initializing the new MapList object
                        MapList mapListIssues = new MapList();

                        for (int i=0;i < iSize;i++)
                        {
                                strObjectId = (String)((HashMap)mapList.get(i)).get(DomainConstants.SELECT_ID);
                                //Initializing the Business Object with Issue ID.
                                BusinessObject boIssue = new BusinessObject(strObjectId);
                                //Getting the Access Mask for the Issue Object.
                                Access accIssueState = boIssue.getAccessMask(context);

                                //To Check whether the Person has FromConnect Access for Issue Object
                                if(accIssueState.hasFromConnectAccess()) {
                                        Map tempMap = new HashMap();
                                        //If the Person has From Connect Object Adding it to MapList object
                                        tempMap.put(DomainConstants.SELECT_ID, strObjectId);
                                        mapListIssues.add(tempMap);
                                }
                        }
                        //returning the MapList
                                return mapListIssues;
        }

        /**
         * A trigger method to notify Originator of the Issue wile promoting from create to assign.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds the Hashmap containing the object id.
         * @return - integer value 0 if success
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int notifyOriginator(Context context, String[] args)
                throws Exception {

                try {
                        String strObjectId = args[0];
                        this.setId(strObjectId);

                        //Getting the originator
                        String strOriginator =
                                getInfo(context, DomainConstants.SELECT_ORIGINATOR);

                        StringList toList = new StringList(strOriginator);
                        StringList ccList = new StringList();
                        StringList bccList = new StringList();
                        StringList objectList = new StringList(strObjectId);

                        MailUtil mail = new MailUtil();
                        String subject =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_STATUS_SUBJECT);
                        String message =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_STATUS_BODY);

                        //Expanding the macros used in the message subject and body
                        subject = Issue.replaceMacroWithValues(context,strObjectId,subject);
                        message = Issue.replaceMacroWithValues(context,strObjectId,message);

                        //Sending mail to the originator
                        mail.sendMessage(
                                context,
                                toList,
                                ccList,
                                bccList,
                                subject,
                                message,
                                objectList);

                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return 0;
        }

        /**
        * A trigger method to set the actual start date when the issue is promoted from Assign to Active state.
        *
        * @param context - the eMatrix <code>Context</code> object
        * @param args - holds the following argument
                        0 - string containg the Object Id
        * @return - Integer value 0 indicating success
        * @throws Exception if the operation fails
        * @since AEF 10.0.5.0
        */
        public int setActualStartDate(Context context, String[] args)
                throws Exception {

                try {
                        String strObjectId = args[0];
                        //Getting the System Date

                        this.setId(strObjectId);
                        //To get the system date
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.SECOND, -1);
                        Date date = cal.getTime();
                        String strDate = _mxDateFormat.format(date);

                        String strAttribActStartDate =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_ActualStartDate);
                        //Setting the Actual Start Date
                        setAttributeValue(context, strAttribActStartDate, strDate);


                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return 0;
        }

        /**
         * A trigger method to reset the Actual Start Date when the issue is demoted from Active to Assign state.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args holds the following arguments:
         *              0 - string containing the Object Id
         * @return - Integer value 0 indicating success
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int removeActualStartDate(Context context, String[] args)
                throws Exception {

                try {
                        String strObjectId = args[0];

                        this.setId(strObjectId);

                        //resetting the Actual Start Date
                        String strDate = "";
                        String strAttribActStartDate =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_ActualStartDate);

                        setAttributeValue(context, strAttribActStartDate, strDate);


                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return 0;
        }
        /**
         * A trigger method to notify Assignees of the Issue.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args holds the following arguments:
         *                       0 - string containing the Object Id
         *                       1 - string containing the Assignee Id
         * @return - Integer value 0 indicating success
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int notifyAssignees(Context context, String[] args)

                throws Exception {
                try {
                        String strObjectId = args[0];
                        String strIssueId = args[1];
                        this.setId(strObjectId);
                        String strName = getInfo(context, DomainConstants.SELECT_NAME);

                        StringList toList = new StringList(strName);
                        StringList ccList = new StringList();
                        StringList bccList = new StringList();
                        StringList objectList = new StringList(strIssueId);

                        MailUtil mail = new MailUtil();
                        String subject =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_ASSIGNMENT_SUBJECT);
                        String message =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_ASSIGNMENT_BODY);

                        //Expanding the macros used in the message subject and body
                        subject = Issue.replaceMacroWithValues(context,strIssueId,subject);
                        message = Issue.replaceMacroWithValues(context,strIssueId,message);

                        //Sending mail to the originator
                        mail.sendMessage(
                                context,
                                toList,
                                ccList,
                                bccList,
                                subject,
                                message,
                                objectList);

                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return 0;
        }

        /**
         * A trigger method to set the Actual End Date when the issue is promoted to Close state.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args holds the following arguments
         *                      0 - String containing the Object Id
         * @return - Integer value 0 indicating success
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int setActualEndDate(Context context, String[] args)
                throws Exception {
                try {
                        String strObjectId = args[0];
                        //Getting the System Date
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.SECOND, -1);
                        Date date = cal.getTime();
                        String strDate = _mxDateFormat.format(date);

                        String strAttribActEndDate =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_ActualEndDate);

                        //Setting the Actual End Date
                        setAttributeValue(context, strAttribActEndDate, strDate);


                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return 0;
        }

        /**
         * A trigger method to reset the Actual End Date and to notify Originator of the Issue wile demoting from Close state.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args holds the following arguments:
         *              0 - String containing the Object Id
         * @return - integer value 0 indicating success
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int removeActualEndDateNotifyOriginator(
                Context context,
                String[] args)
                throws Exception {

                try {
                        String strObjectId = args[0];

                        this.setId(strObjectId);

                        //Resetting the Actual End Date
                        String strDate = "";
                        String strAttribActEndDate =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_ActualEndDate);

                        setAttributeValue(context, strAttribActEndDate, strDate);

                        //Getting the originator
                        String strOriginator =
                                getInfo(context, DomainConstants.SELECT_ORIGINATOR);

                        StringList toList = new StringList(strOriginator);
                        StringList ccList = new StringList();
                        StringList bccList = new StringList();
                        StringList objectList = new StringList(strObjectId);

                        MailUtil mail = new MailUtil();
                        String subject =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_STATUS_SUBJECT);
                        String message =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_STATUS_BODY);

                        //Expanding the macros used in the message subject and body
                        subject = Issue.replaceMacroWithValues(context,strObjectId,subject);
                        message = Issue.replaceMacroWithValues(context,strObjectId,message);

                        //Sending mail to the originator
                        mail.sendMessage(
                                context,
                                toList,
                                ccList,
                                bccList,
                                subject,
                                message,
                                objectList);


                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return 0;
        }

        /**
        * A trigger method check whether the Estimated Start Date and Estimated End Date are filled or not.
        *
        * @param context - the eMatrix <code>Context</code> object
        * @param args holds the following arguments:
        *               0 - String containing the Object Id
        * @return - 1 in case of blocking and 0 in case of success.
        * @throws Exception if the operation fails
        * @since AEF 10.0.5.0
        */
        public int checkEstimatedDates(Context context, String[] args)
                throws Exception {
                int iFlag = 0;
                try {
                        String strObjectId = args[0];
                        this.setId(strObjectId);


                        String strAttribEstStartDate =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_EstimatedStartDate);
                        String strEstimatedStartDate =
                                getAttributeValue(context, strAttribEstStartDate);

                        String strAttribEstEndDate =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_EstimatedEndDate);
                        String strEstimatedEndDate =
                                getAttributeValue(context, strAttribEstEndDate);

                        String strLanguage = context.getSession().getLanguage();
                        Locale strLocale = context.getLocale();
                        if (UIUtil.isNullOrEmpty(strEstimatedStartDate)
                                && UIUtil.isNullOrEmpty(strEstimatedEndDate)) {
                                String strDateError =
                                        EnoviaResourceBundle.getProperty(context,
                                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                                strLocale,
                                                EST_DATE_ERROR);
                                emxContextUtil_mxJPO.mqlNotice(context, strDateError);
                                iFlag = 1;
                        } else if (UIUtil.isNullOrEmpty(strEstimatedStartDate)) {
                                String strDateError =
                                        EnoviaResourceBundle.getProperty(context,
                                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                                strLocale,
                                                EST_START_DATE_ERROR);
                                emxContextUtil_mxJPO.mqlNotice(context, strDateError);
                                iFlag = 1;
                        } else if (UIUtil.isNullOrEmpty(strEstimatedEndDate)) {
                                String strDateError =
                                        EnoviaResourceBundle.getProperty(context,
                                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                                strLocale,
                                                EST_END_DATE_ERROR);
                                emxContextUtil_mxJPO.mqlNotice(context, strDateError);
                                iFlag = 1;
                        }

                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return iFlag;
        }

        /**
        * A trigger method check whether the attributes 'Action Taken', 'Resolution Statement' and Resolution Date are filled or not
        * before promoting from Review state to Closed state.
        *
        * @param context - the eMatrix <code>Context</code> object
        * @param args holds the following arguments:
        *               0 - String containing the Object Id
        * @return - 1 in case of blocking and 0 in case of success.
        * @throws Exception if the operation fails
        * @since AEF 10.0.5.0
        */
        public int checkCloseAttributes(Context context, String[] args)
                throws Exception {
                int iFlag = 0;
                try {
                        String strObjectId = args[0];
                        this.setId(strObjectId);


                        String strAttribActionTaken =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_ActionTaken);
                        String strActionTaken =
                                getAttributeValue(context, strAttribActionTaken);

                        String strAttribResolutionStatement =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_ResolutionStatement);
                        String strResolutionStatement =
                                getAttributeValue(context, strAttribResolutionStatement);

/* Resolution Date in Close Attributes */
                        String strAttribResolutionDate =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_ResolutionDate);
                        String strResolutionDate =
                                getAttributeValue(context, strAttribResolutionDate);

/* Resolution Date in Close Attributes */
                        String strLanguage = context.getSession().getLanguage();
                        Locale strLocale = context.getLocale();
                        if (UIUtil.isNullOrEmpty(strActionTaken)
                                && UIUtil.isNullOrEmpty(strResolutionStatement)) {
                                String strCloseError =
                                        EnoviaResourceBundle.getProperty(context,
                                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                                strLocale,
                                                CLOSE_ERROR);
                                emxContextUtil_mxJPO.mqlNotice(context, strCloseError);
                                iFlag = 1;
                        } else if (UIUtil.isNullOrEmpty(strActionTaken)) {
                                String strCloseError =
                                        EnoviaResourceBundle.getProperty(context,
                                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                                strLocale,
                                                CLOSE_ACTION_TAKEN_ERROR);
                                emxContextUtil_mxJPO.mqlNotice(context, strCloseError);
                                iFlag = 1;
                        } else if (UIUtil.isNullOrEmpty(strResolutionStatement)) {
                                String strCloseError =
                                        EnoviaResourceBundle.getProperty(context,
                                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                                strLocale,
                                                CLOSE_RESL_STATEMENT_ERROR);
                                emxContextUtil_mxJPO.mqlNotice(context, strCloseError);
                                iFlag = 1;
                        }
/* check for Resolution Date while closing */
                        else if (UIUtil.isNullOrEmpty(strResolutionDate)) {
                                String strCloseError =
                                        EnoviaResourceBundle.getProperty(context,
                                                RESOURCE_BUNDLE_COMPONENTS_STR,
                                                strLocale,
                                                CLOSE_RESL_DATE_ERROR);
                                emxContextUtil_mxJPO.mqlNotice(context, strCloseError);
                                iFlag = 1;
/* check for Resolution Date while closing */
                        }
                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return iFlag;
        }

        /**
         *  Closing an Issue from the Properties Page.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         *                      IssueActionTaken - The Value to be filled in attribute 'Action Taken'
         *                      IssueResolutionStatement - The Value to be filled in attribute 'Resolution Statement'
         *                      objectId - The Object Id of the Context
         * @return String for setting the state of an Issue to 'Close' state
                 * @throws FrameworkException if the operation fails
         * @since AEF 10.0.5.0
         **
         */
        public String closeIssue(Context context, String[] args)
                throws FrameworkException {
                try {
                        ContextUtil.startTransaction(context, true);

                        //unpacking the Arguments from variable args
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);
                        String strIssueActionTaken = (String) programMap.get("IssueActionTaken");
                        String strIssueResolutionStatement = (String) programMap.get("IssueResolutionStatement");

/* Start Adding for New Attributes in Raise Issue feature */
                        String strIssueResolutionDate =  (String) programMap.get("IssueResolutionDate");

/* Start Adding for New Attributes in Raise Issue feature */

                        String objectId = (String) programMap.get(OBJECT_ID);

                        String strAttribActionTaken =  PropertyUtil.getSchemaProperty(context,
                                        Issue.SYMBOLIC_attribute_ActionTaken);
                        String strAttribReslStatement = PropertyUtil.getSchemaProperty(context,
                                        Issue.SYMBOLIC_attribute_ResolutionStatement);

/* Start Adding for New Attributes in Raise Issue feature */
                        String strAttribReslDate = PropertyUtil.getSchemaProperty(context,
                                        Issue.SYMBOLIC_attribute_ResolutionDate);

/* Start Adding for New Attributes in Raise Issue feature */

                        this.setId(objectId);

                        //creating Map for the attributs 'Closed By' and 'Closed Date'
                        HashMap mapIssueAttribClose = new HashMap();

                        mapIssueAttribClose.put(strAttribActionTaken, strIssueActionTaken);
                        mapIssueAttribClose.put(strAttribReslStatement, strIssueResolutionStatement);
                        mapIssueAttribClose.put(strAttribReslDate, strIssueResolutionDate);

                        closeIssuePostProcess(context, mapIssueAttribClose, context.getSession().getLanguage());
                        ContextUtil.commitTransaction(context);
                        return this.getInfo(context, DomainConstants.SELECT_CURRENT);
                } catch (Exception ex) {
                        // Transaction will be aborted in case of any exception
                        ContextUtil.abortTransaction(context);
                        /*
                         * The exception ex is thrown back as FrameworkException. This can be
                         * modified to handle some specific message from back end and do
                         * internationalization.
                         */
                        throw new FrameworkException(ex);
                }
        }

        /**
         * Method to calculate the Slip Days based on Actual End Dates or Estimated End Dates.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds the following arguments:
         *                      objectList - Map containing Issue Ids
         *                      paramList - Map containing parameters
         * @return List containing the icon list for slip days.
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         * @modified V6R2014x for refactoring
         */
        public List slipDaysIcon(Context context, String[] args)
                throws Exception {
                //Unpacking the arguments
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);

                //Get the number of objects in objectList
                int iNumOfObjects = relBusObjPageList.size();

            //Initialising a vector based on the number of objects.
                List StatusIconTagList = new Vector(iNumOfObjects);
                String arrObjId[] = new String[relBusObjPageList.size()];
                Object obj = null;

                //Getting the bus ids for objects in the table
                for (int i = 0; i < iNumOfObjects; i++) {
                        obj = relBusObjPageList.get(i);
                        if (obj instanceof HashMap) {
                        arrObjId[i] = (String) ((HashMap) relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                        } else if (obj instanceof Hashtable) {
                        arrObjId[i] = (String) ((Hashtable) relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                        }
                }

            String strAttribEstEndDate = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_attribute_EstimatedEndDate);
            String strAttribActEndDate = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_attribute_ActualEndDate);

                StringList listSelect = new StringList(3);
                String strAttrb1 = "attribute["+strAttribEstEndDate+"]";
                String strAttrb2 = "attribute["+strAttribActEndDate+"]";
                String strAttrb3 = DomainConstants.SELECT_CURRENT;
                listSelect.addElement(strAttrb1);
                listSelect.addElement(strAttrb2);
                listSelect.addElement(strAttrb3);

                //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
                BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

                for (int i = 0; i < iNumOfObjects; i++) {
            	String strEstimatedEndDate = attributeList.getElement(i).getSelectData(strAttrb1);
            	String strActualDate = attributeList.getElement(i).getSelectData(strAttrb2);
            	String strState = attributeList.getElement(i).getSelectData(strAttrb3);
            	//XSSOK
                StatusIconTagList.add(getStatusIconTag(context, strEstimatedEndDate, strActualDate, strState, "../"));
                }
                return StatusIconTagList;
        }

        /**
         * Method to get the name of the Owner in last name, first name format.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         *                      objectList - The List containing the Objects
         * @return StringList - containing owner names in Last Name, First Name format
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */

        public StringList getNameForOwner(Context context, String[] args)
                throws Exception {
                //String list containing the formatted names(this will be returned)
                StringList slFormattedNames = new StringList();
                try {
                        // Unpacking the arguments
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);
                        //Maplist containing information about objects in the table
                        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
                        //Array containing object ids  of the objects in the table
                        String arrObjId[] = new String[relBusObjPageList.size()];
                        //getting the object ids from the Maplist

                        for (int i = 0; i < relBusObjPageList.size(); i++) {

                                Object obj = relBusObjPageList.get(i);

                                if (obj instanceof HashMap) {
                                        arrObjId[i] =
                                                (String) ((HashMap) relBusObjPageList.get(i)).get(
                                                        DomainConstants.SELECT_ID);
                                } else if (obj instanceof Hashtable) {
                                        arrObjId[i] =
                                                (String) ((Hashtable) relBusObjPageList.get(i)).get(
                                                        DomainConstants.SELECT_ID);
                                }
                        }

                        //HashSet to contain the owner name
                        HashSet ownerSet = new HashSet();
                        //String to contain the owner name
                        String strOwner = "";

                        //HashMap containing the ids of owners
                        HashMap ownerMap = new HashMap();
                        //Stringlist containing busSelects
                        StringList busSelects = new StringList(DomainConstants.SELECT_ID);
                        //String containing the owner(person) object id
                        String strPersonId = "";
                        //StringBuffer for the formatted name
                        StringBuffer sbFormattedName = new StringBuffer(100);
                        //Getting the actual attribute name
                        String strAttribFirstName =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_FirstName);
                        //Getting the actual attribute name
                        String strAttribLastName =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_attribute_LastName);

                        StringList listSelect = new StringList(1);
                        String strAttrb1 = DomainConstants.SELECT_OWNER;
                        listSelect.addElement(strAttrb1);

                        //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
                        BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

                        //Traversing through the objexct ids to get the firstname and lastname
                        for (int j = 0; j < relBusObjPageList.size(); j++) {
                                //Getting the owner for the object
                                strOwner = attributeList.getElement(j).getSelectData(strAttrb1);
                                //adding the owner name to the owner set
                                ownerSet.add(strOwner);
                                //Getting the ids of the owners
                                ownerMap =
                                        (HashMap) Person.getPersonsFromNames(
                                                context,
                                                ownerSet,
                                                busSelects);

                                //getting the id from the ownerMap
                                if (!ownerMap.isEmpty()) {
                                        strPersonId =
                                                (String) ((HashMap) ownerMap.get(strOwner)).get(
                                                        DomainConstants.SELECT_ID);
                                        //Setting the context to the person's id
                                        this.setId(strPersonId);
                                        //Forming the formatted name
                                        sbFormattedName =
                                                sbFormattedName.append(
                                                        this.getAttributeValue(context, strAttribLastName));
                                        sbFormattedName = sbFormattedName.append(", ");
                                        sbFormattedName =
                                                sbFormattedName.append(
                                                        this.getAttributeValue(
                                                                context,
                                                                strAttribFirstName));
                                        //Clearing the owner set
                                        ownerSet.clear();
                                }
                                //Adding the formatted name to the return stringlist
                                slFormattedNames.addElement(sbFormattedName.toString());
                                //Clearing the stringbuffer
                                sbFormattedName.delete(0, sbFormattedName.length());

                        }
                } catch (Exception e) {
                        throw e;
                }

                //Returning the formatted name
                return slFormattedNames;
        }

        /**
         * Method to check for display for the Date field with Date Picker based on Lifecycle States.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         *                      objectId - Object Id of the Context object
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public boolean bDateDisplay(Context context, String[] args) throws Exception {
                boolean bFlag;
                try{
                        //Unpacking the arguments
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);
                        //Maplist containing information about objects in the table
                        String strObjectId = (String) programMap.get(OBJECT_ID);
                        this.setId(strObjectId);
                        //To get actual policy Name
                        String strIssuePolicy =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_policy_Issue);
                        //Getting and representing the state 'Create'
                        String strCreate =
                                FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CREATE);

                        //Getting and representing the state 'Assign'
                        String strAssign =
                                FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_ASSIGN);

                        //Getting the current state
                        String strCurrentState =
                                getInfo(context, DomainConstants.SELECT_CURRENT);

                        if((strCurrentState.equals(strCreate)) || (strCurrentState.equals(strAssign))){
                                bFlag = true;
                        } else {
                                bFlag = false;
                        }


                }catch(Exception ex){
                        throw ex;
                }
                return bFlag;
        }

        /**
         * Method to check for display for the Date field without Date Picker (non editable) based on Lifecycle States.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         *                      objectId - The object Id of Context object
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public boolean bDateNotDisplay(Context context, String[] args) throws Exception {
                boolean bFlag;
                try{
                        //Unpacking the arguments
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);
                        //Maplist containing information about objects in the table
                        String strObjectId = (String) programMap.get(OBJECT_ID);
                        this.setId(strObjectId);
                        //To get actual policy Name
                        String strIssuePolicy =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_policy_Issue);
                        //Getting and representing the state 'Create'
                        String strCreate =
                                FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CREATE);

                        //Getting and representing the state 'Assign'
                        String strAssign =
                                FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_ASSIGN);

                        //Getting the current state
                        String strCurrentState =
                                getInfo(context, DomainConstants.SELECT_CURRENT);

                        if((strCurrentState.equals(strCreate)) || (strCurrentState.equals(strAssign))){
                                bFlag = false;
                        } else {
                                bFlag = true;
                        }


                }catch(Exception ex){
                        throw ex;
                }
                return bFlag;
        }

        /**
         * Method to check for displaying the chooser for a field in the Webform based on Lifecycle States.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         *                      objectId - The object Id of Context object
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public boolean bChooserDisplay(Context context, String[] args) throws Exception {
                boolean bFlag;
                try{
                        //Unpacking the arguments
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);
                        //Maplist containing information about objects in the table
                        String strObjectId = (String) programMap.get(OBJECT_ID);
                        this.setId(strObjectId);
                        //To get actual policy Name
                        String strIssuePolicy =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_policy_Issue);
                        //Getting and representing the state 'Create'
                        String strCreate =
                                FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CREATE);


                        //Getting the current state
                        String strCurrentState =
                                getInfo(context, DomainConstants.SELECT_CURRENT);

                        if(strCurrentState.equals(strCreate)){
                                bFlag = false;
                        } else {
                                bFlag = true;
                        }

                }catch(Exception ex){
                        throw ex;
                }
                return bFlag;
        }

        /**
         * Method to check for not displaying the chooser for a field in the Webform based on Lifecycle States.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         *                      objectId - The object Id of Context object
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public boolean bChooserNotDisplay(Context context, String[] args) throws Exception {
                boolean bFlag;
                try{
                        //Unpacking the arguments
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);
                        //Maplist containing information about objects in the table
                        String strObjectId = (String) programMap.get(OBJECT_ID);
                        this.setId(strObjectId);
                        //To get actual policy Name
                        String strIssuePolicy =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_policy_Issue);
                        //Getting and representing the state 'Create'
                        String strCreate =
                                FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CREATE);


                        //Getting the current state
                        String strCurrentState =
                                getInfo(context, DomainConstants.SELECT_CURRENT);

                        if(strCurrentState.equals(strCreate)){
                                bFlag = true;
                        } else {
                                bFlag = false;
                        }

                }catch(Exception ex){
                        throw ex;
                }
                return bFlag;
        }

        /**
         * Method to check whether the state is 'Closed' or not
         *.
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         *              objectId - The object Id of Context object
         *      settings - hashmap containing the settings
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public boolean bLinkDisplay(Context context, String[] args) throws Exception {
                boolean bFlag;
                try{
                        //Unpacking the arguments
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);
                        //Getting the Settings of Command
                        Map settingMap = (HashMap) programMap.get(SETTINGS);
                        //Getting the value of State
                        String strState = (String) settingMap.get(STATE);
                        //Maplist containing information about objects in the table
                        String strObjectId = (String) programMap.get(OBJECT_ID);
                        this.setId(strObjectId);
                        //To get actual policy Name
                        String strIssuePolicy =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_policy_Issue);
                        //Getting and representing the state 'Create'
                        String strActualStateName =
                                FrameworkUtil.lookupStateName(context, strIssuePolicy, strState);

                        //Getting the current state
                        String strCurrentState =
                                getInfo(context, DomainConstants.SELECT_CURRENT);

                        if(strCurrentState.equals(strActualStateName)){
                                bFlag = false;
                        } else {
                                bFlag = true;
                        }

                }catch(Exception ex){
                        throw ex;
                }
                return bFlag;
        }

        /**
         * Trigger Method to check whether the state of the Issue object is 'Create' and also the accesses for relationship Assigned Issue.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains the Ids of Context Issue Object, Selected Person object and Relation
         * @return - int (0 or 1) 0 - If success and 1 - If blocked.
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int checkPerson(Context context, String[] args) throws Exception {
                int iFlag = 1;
                try{

                        //Instantiating the Domain Object for Issue
                        DomainObject domCategoryState = newInstance(context, args[1]);
                        //To get actual policy Name
                        String strIssuePolicy =
                                                        PropertyUtil.getSchemaProperty(
                                                                context,
                                                                Issue.SYMBOLIC_policy_Issue);
                        //Getting and representing the state 'Create'
                        String strActualStateName =
                                        FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CREATE);


                        //Getting the current state and checking if it is in Create state.
                        String strCurrentState = domCategoryState.getInfo(context, DomainConstants.SELECT_CURRENT);
                                String strLanguage = context.getSession().getLanguage();
                        Locale strLocale = context.getLocale();
                        if(strCurrentState.equals(strActualStateName)){

                                String strAssignedStateError =
                                                                                EnoviaResourceBundle.getProperty(context,
                                                                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                                                                        strLocale,
                                                                                        CHECK_STATE);
                                emxContextUtil_mxJPO.mqlNotice(context, strAssignedStateError);
                        }

                        else{

                                //Getting the Issue Id
                                String strObjectId = args[1];
                                //Getting the Context User
                                String strContextUser = context.getUser();

                                //Instantiating the new Person object
                                matrix.db.Person pUser = new matrix.db.Person(strContextUser);
                                String strRoleIssueManager =
                                        PropertyUtil.getSchemaProperty(
                                                context,
                                                Issue.SYMBOLIC_role_IssueManager);

                                //To Check whether the user is assigned the role Issue Manager...
                                boolean bIssueManager = pUser.isAssigned(context, strRoleIssueManager);
/* Added for Analyst Role */
                                String strRoleAnalyst =
                                        PropertyUtil.getSchemaProperty(
                                                context,
                                                Issue.SYMBOLIC_role_Analyst);

                                //To Check whether the user is assigned the role Issue Manager...
                                boolean bAnalyst = pUser.isAssigned(context, strRoleAnalyst);

/* Added for Analyst Role */
// Modified condition for checking analyst role //
                                if(bIssueManager || bAnalyst){
                                        //...If Yes
                                        iFlag = 0;
                                }
                                else{
                                        //...If No
                                        iFlag = 1;
                                }

                                //To check whether the context user is Assignee
                                if(iFlag == 1)
                                {

                                        //Where Items of query
                                        StringList slObjSelects = new StringList(DomainConstants.SELECT_ID);
                                        slObjSelects.add(DomainConstants.SELECT_NAME);

                                        //Initiating the Map List
                                        MapList mlAssigneeList = null;

                                        //Getting the actual Type Name 'Person'
                                        String strType =
                                                PropertyUtil.getSchemaProperty(
                                                        context,
                                                        Issue.SYMBOLIC_type_Person);
                                        //Getting the actual relationship name 'Assigned Issue'
                                        String strReln = PropertyUtil.getSchemaProperty(
                                                                context,
                                                                Issue.SYMBOLIC_relationship_AssignedIssue);

                                        //Running the Query to get Assignees
                                        mlAssigneeList =
                                                        domCategoryState.getRelatedObjects(
                                                                context,
                                                                strReln,
                                                                strType,
                                                                slObjSelects,
                                                                null,
                                                                true,
                                                                false,
                                                                (short) 1,
                                                                DomainConstants.EMPTY_STRING,
                                                                DomainConstants.EMPTY_STRING);

                                        int iNumberOfObjects = mlAssigneeList.size();

                                        String strName = "";
                                        //Loop to check the Context user is an Assignee or not.
                                        for (int i = 0; i < iNumberOfObjects; i++) {
                                                strName = (String)((Hashtable)mlAssigneeList.get(i)).get(DomainConstants.SELECT_NAME);

                                                if(strName.equals(strContextUser))
                                                {
                                                        iFlag = 0;
                                                }
                                        }
                                }

                                //If the person is not having the access then the alert comes.
                                if (iFlag == 1)
                                {

                                        String strAccessError =
                                                EnoviaResourceBundle.getProperty(context,
                                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                                        strLocale,
                                                        CHECK_ACCESS);
                                        emxContextUtil_mxJPO.mqlNotice(context, strAccessError);
                                }
                        }

                }catch(Exception ex){
                        throw ex;
                }

                return iFlag;
        }


 /**
   * To obtain the Reference Document (Document).
   *
   * @param context context for this request
   * @param args - Holds the parameters passed from the calling method
                 queryLimit
                 Type
                 Name
                 Description
                 Owner
                 State
                 latestOnly
                 TypeDocument
                 Revision
                 vaultOption
                 VaultDisplay.
   * @return MapList , the Object ids matching the search criteria
   * @throws Exception if the operation fails
   * @since AEF 10.0.5.0
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public static Object getDocuments(Context context, String[] args)
  throws Exception
  {

    Map programMap = (Map) JPO.unpackArgs(args);
    short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

    String strType = (String)programMap.get("TypeDocument");

    if (strType==null || strType.equals("") ) {
      strType = SYMB_WILD;
    }

    String strName = (String)programMap.get("Name");

    if (strName==null || strName.equals("") ) {
      strName = SYMB_WILD;
    }

    String strRevision = (String)programMap.get("Revision");

    String strLatestRevisionOnly = (String)programMap.get("latestOnly");

    boolean bLatestRevisionOnly = false;
    if (strLatestRevisionOnly != null) {
      bLatestRevisionOnly = true;
    }

    String strTitle = (String)programMap.get("Title");

    String strDesc = (String)programMap.get("Description");

    String strState = (String)programMap.get("State");

    if (strState != null) {
      strState = strState.trim();
    }
    else {
      strState = "";
    }

    String strPolicy = (String)programMap.get("Policy");

    //Begin of modify by Yukthesh, Infosys for Bug#311161 on 10 Nov,2005.
    // Here the key was changed from Owner to OwnerDisplay as below
    String strOwner = (String)programMap.get("OwnerDisplay");
    //End of modify by Yukthesh, Infosys for Bug#311161 on 10 Nov,2005
        if ( strOwner==null || strOwner.equals("") ) {
      strOwner = SYMB_WILD;
    }

    String strVault = null;
    String strVaultOption = (String)programMap.get("vaultOption");

      if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)||
            strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||
            strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                                strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
         else
                strVault = (String)programMap.get("vaults");

    StringList select = new StringList(1);
    select.addElement(DomainConstants.SELECT_ID);

    boolean start = true;
    StringBuffer sbWhereExp = new StringBuffer(120);

    if (strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
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

    if (strPolicy!=null && (!strPolicy.equals(SYMB_WILD)) && (!strPolicy.equals("")) ) {
      if (start) {
        sbWhereExp.append(SYMB_OPEN_PARAN);
        start = false;
      } else {
        sbWhereExp.append(SYMB_AND);
      }
      sbWhereExp.append(SYMB_OPEN_PARAN);
      sbWhereExp.append(DomainConstants.SELECT_POLICY);
      sbWhereExp.append(SYMB_MATCH);
      sbWhereExp.append(SYMB_QUOTE);
      sbWhereExp.append(strPolicy);
      sbWhereExp.append(SYMB_QUOTE);
      sbWhereExp.append(SYMB_CLOSE_PARAN);
    }

        String strAttrTitle = PropertyUtil.getSchemaProperty(
                                                        context,
                                                        Issue.SYMBOLIC_attribute_Title);

        if (strTitle!=null && (!strTitle.equals(SYMB_WILD)) && (!strTitle.equals("")) ) {
      if (start) {
        sbWhereExp.append(SYMB_OPEN_PARAN);
        start = false;
      } else {
        sbWhereExp.append(SYMB_AND);
      }
      sbWhereExp.append(SYMB_OPEN_PARAN);
      sbWhereExp.append(SYMB_ATTRIBUTE);
      sbWhereExp.append(SYMB_OPEN_BRACKET);
      sbWhereExp.append(strAttrTitle);
      sbWhereExp.append(SYMB_CLOSE_BRACKET);
      sbWhereExp.append(SYMB_MATCH);
      sbWhereExp.append(SYMB_QUOTE);
      sbWhereExp.append(strTitle);
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


        if (bLatestRevisionOnly) {
      if (start) {
        sbWhereExp.append(SYMB_OPEN_PARAN);
        start = false;
      } else {
        sbWhereExp.append(SYMB_AND);
      }
      sbWhereExp.append(SYMB_OPEN_PARAN);
      sbWhereExp.append(DomainConstants.SELECT_REVISION);
      sbWhereExp.append(SYMB_EQUAL);
      sbWhereExp.append("last");
      sbWhereExp.append(SYMB_CLOSE_PARAN);
    }

        //Adding the clause attribute[attribute_IsVersionObject] != True
        String strAttrIsVersionObject = PropertyUtil.getSchemaProperty(
                                                        context,
                                                        Issue.SYMBOLIC_attribute_IsVersionObject);
        if(start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
        } else {
                sbWhereExp.append(SYMB_AND);
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(SYMB_ATTRIBUTE);
        sbWhereExp.append(SYMB_OPEN_BRACKET);
        sbWhereExp.append(strAttrIsVersionObject);
        sbWhereExp.append(SYMB_CLOSE_BRACKET);
    sbWhereExp.append(SYMB_NOT_EQUAL);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append("True");
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(SYMB_CLOSE_PARAN);


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

	//to check whether the document has the to connect access or not.
	sbWhereExp.append(SYMB_AND);
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append("current.access[toconnect]==\"TRUE\"");
    sbWhereExp.append(SYMB_CLOSE_PARAN);

    MapList mapList = null;

        String strObjId = (String)programMap.get("objectId");
        DomainObject domObj = newInstance(context, strObjId);
        mapList = domObj.findObjects(
                    context, strType,strName, strRevision, strOwner, strVault,
                    sbWhereExp.toString(), "", true, select, sQueryLimit);

        return mapList;
  }


        /**
     * Method call to get the Name Depending upon the Mode.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Holds the parameters passed from the calling method
                             objectList
                         paramList.
     * @return Object - Vector containing names in last name, first name format
     * @throws Exception if the operation fails
     * @since AEF 10.0.5.0
     * @grade 0
     */
    public Vector getObjectNameOnMode (Context context, String[] args) throws Exception {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      //Gets the objectList from args
      MapList relBusObjPageList = (MapList)programMap.get("objectList");
      HashMap paramList = (HashMap)programMap.get("paramList");
      //Used to construct the HREF
      String strSuiteDir = (String)paramList.get("SuiteDirectory");
      String strJsTreeID = (String)paramList.get("jsTreeID");
      String strParentObjectId = (String)paramList.get("objectId");
      String strMode = (String)paramList.get(Search.REQ_PARAM_MODE);
      String strFullName = null;
      Vector vctObjectName = new Vector();

      //No of objects
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

      StringList listSelect = new StringList(2);
      String strAttrb1 = DomainConstants.SELECT_ID;
      String strAttrb2 = DomainConstants.SELECT_NAME;
      listSelect.addElement(strAttrb1);
      listSelect.addElement(strAttrb2);

      //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
      BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

      String strObjIdArray = "";
      for (int i = 0; i < iNoOfObjects; i++) {
          String strObjId = attributeList.getElement(i).getSelectData(strAttrb1);

        //Constructing the HREF
        if(strMode!=null && strMode.equals(Search.GLOBAL_SEARCH)) {
          strFullName = "<A HREF=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?mode=insert&emxSuiteDirectory="
          + strSuiteDir + "&defaultsortname=" +  attributeList.getElement(i).getSelectData(strAttrb2) + "&parentOID="
          + strParentObjectId + "&jsTreeID=" + strJsTreeID + "&objectId="
          + strObjId + "', 'null', 'null', 'false', 'content')\" class=\"object\">"
          + attributeList.getElement(i).getSelectData(strAttrb2)
          + "</A>";
        }
        else {
          strFullName = attributeList.getElement(i).getSelectData(strAttrb2);
        }
        //Adding into the vector
        vctObjectName.add(strFullName);
      }
      return  vctObjectName;
    }

        /**
         * Method to check whether the person is 'Issue Manager' or 'Originator' or 'Assignee' for Create New Documents Link.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         *                      objectId - The object Id of Context object
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
     * @since AEF 10.0.5.0
         */
        public boolean bCommandDisplay(Context context, String[] args) throws Exception {
                boolean bFlag;
                try{
                        //Unpacking the arguments
                        HashMap programMap = (HashMap) JPO.unpackArgs(args);
                        //Maplist containing information about objects in the table
                        String strObjectId = (String) programMap.get(OBJECT_ID);

                        //Instantiating the Domain Object for Issue
                        DomainObject domIssueObj = newInstance(context, strObjectId);

                        //Getting the Context User
                        String strContextUser = context.getUser();

                        //Instantiating the new Person object
                        matrix.db.Person pUser = new matrix.db.Person(strContextUser);
                        String strRoleIssueManager =
                                PropertyUtil.getSchemaProperty(
                                        context,
                                        Issue.SYMBOLIC_role_IssueManager);

                        //To Check whether the user is assigned the role Issue Manager...
                        boolean bIssueManager = pUser.isAssigned(context, strRoleIssueManager);

                        if(bIssueManager){
                                //...If Yes
                                bFlag = true;
                        }
                        else{
                                //...If No
                                bFlag = false;
                        }

                        //To check whether the context user is Originator or Assignee
                        if(!bFlag)
                        {
                                //Getting the name of the Originator
                                String strOriginator = domIssueObj.getInfo(context, DomainConstants.SELECT_ORIGINATOR);
                                //Comparing with Context user
                                if(strOriginator.equals(strContextUser))
                                {
                                        bFlag = true;
                                }

                                //Check for Assignee
                                if(!bFlag)
                                {
                                        //Where Items of query
                                        StringList slObjSelects = new StringList(DomainConstants.SELECT_ID);
                                        slObjSelects.add(DomainConstants.SELECT_NAME);
                                        //Initiating the Map List
                                        MapList mlAssigneeList = null;

                                        //Getting the actual Type Name 'Person'
                                        String strType =
                                                PropertyUtil.getSchemaProperty(
                                                        context,
                                                        Issue.SYMBOLIC_type_Person);
                                        //Getting the actual relationship name 'Assigned Issue'
                                        String strReln = PropertyUtil.getSchemaProperty(
                                                                context,
                                                                Issue.SYMBOLIC_relationship_AssignedIssue);

                                        //Running the Query to get Assignees
                                        mlAssigneeList =
                                                        domIssueObj.getRelatedObjects(
                                                                context,
                                                                strReln,
                                                                strType,
                                                                slObjSelects,
                                                                null,
                                                                true,
                                                                false,
                                                                (short) 1,
                                                                DomainConstants.EMPTY_STRING,
                                                                DomainConstants.EMPTY_STRING);

                                        int iNumberOfObjects = mlAssigneeList.size();

                                        String strName = "";
                                        //Loop to check the Context user is an Assignee or not.
                                        for (int i = 0; i < iNumberOfObjects; i++) {
                                                strName = (String)((Hashtable)mlAssigneeList.get(i)).get(DomainConstants.SELECT_NAME);

                                                if(strName.equals(strContextUser))
                                                {
                                                        bFlag = true;
                                                }
                                        }
                                }
                        }
                }catch(Exception ex){
                        throw ex;
                }
                return bFlag;
        }


/**
* A trigger method to notify Originator of the Issue wile promoting from reveiw to closed state.
*
* @param context - the eMatrix <code>Context</code> object
* @param args - contains the string Object Id
* @return - integer value 0 if success
* @throws Exception if the operation fails
* @since AEF 10.0.5.0
*/


public int sendClosingMail(Context context, String[] args)
                throws Exception {

                try {
                        String strObjectId = args[0];

                        //Getting the originator
                        String strOriginator =
                                getInfo(context, DomainConstants.SELECT_ORIGINATOR);



                        StringList toList = new StringList(strOriginator);
                        StringList ccList = new StringList();
                        StringList bccList = new StringList();
                        StringList objectList = new StringList(strObjectId);

                        MailUtil mail = new MailUtil();
                        String subject =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_STATUS_SUBJECT);
                        String message =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_STATUS_BODY);

                        //Expanding the macros used in the message subject and body
                        subject = Issue.replaceMacroWithValues(context,strObjectId,subject);
                        message = Issue.replaceMacroWithValues(context,strObjectId,message);

                        //Sending mail to the originator
                        mail.sendMessage(
                                context,
                                toList,
                                ccList,
                                bccList,
                                subject,
                                message,
                                objectList);

                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return 0;
        }

        /**
          * Trigger Method to send notification after functionality Change Owner.
          *
          * @param context - the eMatrix <code>Context</code> object
          * @param args - Object Id of Issue
          * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
          * @throws Exception if the operation fails
          * @since AEF 10.0.5.0
          */
        public int notifyOwner(Context context, String[] args) throws Exception {
                try {
                        //Getting the object ID
                        String strObjectId = args[0];

                        DomainObject domIssue = newInstance(context, strObjectId);
                        //Getting the Changed Owner
                        String strOwner = domIssue.getInfo(context, DomainConstants.SELECT_OWNER);
                        //Getting the Originator
                        String strOriginator = domIssue.getInfo(context, DomainConstants.SELECT_ORIGINATOR);

                        MailUtil mail = new MailUtil();
                        StringList ccList = new StringList();
                        StringList bccList = new StringList();
                        StringList toList = new StringList();
                        StringList objectList = new StringList();

                        toList.add(strOwner);
                        objectList.add(strObjectId);
                        String subject =
                                EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_ASSIGNMENT_SUBJECT);
                        String message =
                                EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_ASSIGNMENT_BODY);

                        //Expanding the macros used in the message subject and body
                        subject = Issue.replaceMacroWithValues(context,strObjectId,subject);

                        //Check if Originator is himself a owner. If yes then the message should be blank. Otherwise message should contain
                        //all details
                        if (strOwner.equals(strOriginator)) {
                                message = "";
                        }else {
                                message = Issue.replaceMacroWithValues(context,strObjectId,message);
                        }


                        //Sending mail to the owner
                        mail.sendMessage(
                                context,
                                toList,
                                ccList,
                                bccList,
                                subject,
                                message,
                                objectList);
                } catch (Exception ex) {
                        throw new FrameworkException((String) ex.getMessage());
                }
                return 0;
        }




        /**
         * Trigger Method to check whether the context user is IssueManager or not.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains the Ids of Context Issue Object, Selected Person object and Relation
         * @return - int (0 or 1) 0 - If success and 1 - If blocked.
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        public int checkPersonForResolveTo(Context context, String[] args) throws Exception {
                int iFlag = 0;

        		// bug # 303907 on 06 May,05
                //Domain Object of Issue
                DomainObject domCategoryState = newInstance(context, args[0]);

                DomainObject domToObject = newInstance(context,args[1]);
                String strType = domToObject.getInfo(context, DomainConstants.SELECT_TYPE);
                
                String strECType = PropertyUtil.getSchemaProperty(context,"type_EngineeringChange");

                //To get actual policy Name
                String strIssuePolicy =PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_policy_Issue);

                //Getting and representing the state 'Create'
                String strActualStateName = FrameworkUtil.lookupStateName(context, strIssuePolicy, SYMB_CREATE);

                //Check if the current is Create.
                String strCurrentState = domCategoryState.getInfo(context, DomainConstants.SELECT_CURRENT);
                
                if(!strType.equals(strECType)) {
                    if(strCurrentState.equals(strActualStateName)){                                                                        
                            if(hasAccess(context)) {
                            	iFlag = 0;
                            } else {
                                    String strAssignedStateError = EnoviaResourceBundle.getProperty(context,
                                    		RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),CHECK_PERSON);
                                    
                                    emxContextUtil_mxJPO.mqlNotice(context, strAssignedStateError);
                                    iFlag = 1;
                            }
                    }
                }
                return iFlag;
        }

        /**
         * Returns the IssueCategory.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args contains a Map with the following entries:
                strObjectId - string containing the object id.
         * @return String of IssueCategory
         * @throws Exception if the operation fails
         * @since ProductCentral 10-5-SP1
         */
        public static Object getIssueCategory (Context context, String[] args) throws Exception {
              // Unpack the arguments and get the object id
              HashMap programMap = (HashMap)JPO.unpackArgs(args);
              HashMap paramMap = (HashMap)programMap.get(PARAMMAP);
              String strObjectId = (String)paramMap.get(OBJECTID);
              /*
              *  check whether there is an entry in the property file
              *  if there is an entry return the internationalised value
              *  else return the value as it is
              */
              StringList strIssueCategory = new StringList();
              String strSelect = "attribute[" + PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_IssueCategory) + "]";
              DomainObject domObj = newInstance(context, strObjectId);
              String strTempIssueCategory = domObj.getInfo(context, strSelect);
              StringBuffer sbCategoryStr = new StringBuffer("emxComponents.Common.").append(strTempIssueCategory.replace(' ', '_'));
              String strMXIssueCategory = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),sbCategoryStr.toString());
              if (strMXIssueCategory.equalsIgnoreCase(sbCategoryStr.toString()))
                    strIssueCategory.addElement(strTempIssueCategory);
              else
                    strIssueCategory.addElement(strMXIssueCategory);
              return strIssueCategory;
        }

        /**
         * Returns the IssueClassification.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args contains a Map with the following entries:
                strObjectId - string containing the object id.
         * @return String of IssueCategory
         * @throws Exception if the operation fails
         * @since ProductCentral 10-5-SP1
         */
        public static Object getIssueClassification (Context context, String[] args) throws Exception {
              // Unpack the arguments and get the object id
              HashMap programMap = (HashMap)JPO.unpackArgs(args);
              HashMap paramMap = (HashMap)programMap.get(PARAMMAP);
              String strObjectId = (String)paramMap.get(OBJECTID);
              /*
              *  check whether there is an entry in the property file
              *  if there is an entry return the internationalised value
              *  else return the value as it is
              */
              StringList strIssueClassification = new StringList();
              String strSelect = "attribute[" + PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_IssueClassification) + "]";
              DomainObject domObj = newInstance(context, strObjectId);
              String strTempIssueClassification = domObj.getInfo(context, strSelect);
                 StringBuffer sbClassificationStr = new StringBuffer("emxComponents.Common.").append(strTempIssueClassification.replace(' ', '_'));
              String strMXIssueClassification = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),sbClassificationStr.toString());
              if (strMXIssueClassification.equalsIgnoreCase(sbClassificationStr.toString()))
                    strIssueClassification.addElement(strTempIssueClassification);
              else
                    strIssueClassification.addElement(strMXIssueClassification);
              return strIssueClassification;
        }

        /**
         * Method to display the Classified Item column or not if called from Rollup Issues.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         * objectId - Object Id of the Context object
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
         * @since Common 11.0
         */
        public boolean checkClassifiedItem(Context context, String[] args)
            throws Exception {
            // Unpack the arguments and get the object id
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) programMap.get(OBJECT_ID);

            if(!(strObjectId == null || "".equals(strObjectId)))
            {
                String strParentType = getParentType(context,strObjectId);
                if (!strParentType.equals(TYPE_DOCUMENTS))
                {
                    return true;
                }
            }
            return false;
        }

        /**
        * Method call to get all the Issues under the context Library Hierarchy Tree Level from the data base.
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args - holds HashMap programMap which has Object Id to be used by this method
        * @return MapList containing the id of all Issue objects
        * @throws Exception if the operation fails
         * @since Common 11.0
         *
        */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getRollupIssues (Context context, String[] args) throws Exception {
               // Unpack the arguments and get the object id
               HashMap programMap = (HashMap)JPO.unpackArgs(args);
               String strObjectId = (String) programMap.get(OBJECT_ID);

               MapList resIssueList = new MapList();

               String strRelationIssue = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_relationship_Issue);

               String issueIdSelect      = "to["+strRelationIssue+"].from.id";
               MULTI_VALUE_LIST.add(issueIdSelect);
               String issueNameSelect      = "to["+strRelationIssue+"].from.name";
               MULTI_VALUE_LIST.add(issueNameSelect);

               boolean addTransaction = false;
               if( !ContextUtil.isTransactionActive(context) ){
                   addTransaction = true;
               }
               String strIssueId = "";
               Map tmpMap = new HashMap();

               DomainObject domObj = new DomainObject(strObjectId);

               // relationship pattern
               StringBuffer sbRelNames = new StringBuffer();
               sbRelNames.append(DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM);

               // Object Selects
               StringList objectSelects = new StringList();
               objectSelects.addElement(issueIdSelect);
               objectSelects.addElement(issueNameSelect);

               //get the expansion iterator

               if(addTransaction ){
                   ContextUtil.startTransaction(context,false);
               }


               ExpansionIterator expItr = domObj.getExpansionIterator(context, sbRelNames.toString(), "*",
                       objectSelects, new StringList(), false, true, (short)1,
                       "", "", (short)0,
                       true, false, (short)1000, true);

               try{
                   RelationshipWithSelect relWithSelect = null;
                   StringList issueIds = new StringList();
                   StringList issueNames = new StringList();
                   while(expItr.hasNext()){
                       relWithSelect = expItr.next();
                       issueIds = relWithSelect.getTargetSelectDataList(issueIdSelect);
                       issueNames = relWithSelect.getTargetSelectDataList(issueNameSelect);
                       if(issueIds!=null){
                           for(int i=0; i<issueIds.size(); i++){
                               strIssueId = (String)issueIds.get(i);
                               tmpMap = new HashMap(1);
                               tmpMap.put("id", strIssueId);
                               tmpMap.put("Name", issueNames.get(i));
                               resIssueList.add(tmpMap);
                           }
                       }
                   }

                   Map contextObjIssues = domObj.getInfo(context, objectSelects);
                   StringList contextObjIssuesIds = (StringList)contextObjIssues.get(issueIdSelect);
                   StringList contextObjIssuesNames = (StringList)contextObjIssues.get(issueNameSelect);

                   if(contextObjIssuesIds != null){
                       for(int i=0; i<contextObjIssuesIds.size(); i++){
                           strIssueId = (String)contextObjIssuesIds.get(i);
                           tmpMap = new HashMap(1);
                           tmpMap.put("id", strIssueId);
                           tmpMap.put("Name", contextObjIssuesNames.get(i));
                           resIssueList.add(tmpMap);
                       }
                   }
               }catch(Exception ex){
                   ex.printStackTrace();
                   throw ex;
               }finally{
                   expItr.close();
               }

               if(addTransaction ){
                   ContextUtil.commitTransaction(context);
               }

               return resIssueList;
           }

        /**
        * Method call to get Issues connected to Reference Documents from the data base.
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args - holds HashMap programMap which has Object Id to be used by this method
        * @return MapList containing the id of all Issue objects
        * @throws Exception if the operation fails
         * @since Common 11.0
         *
        */

        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getRelatedIssues (Context context, String[] args)
            throws Exception {

            // Unpack the arguments and get the object id
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) programMap.get(OBJECT_ID);

            //To get Actual Relationship name
            String strRelationIssue = PropertyUtil.getSchemaProperty(context,
                    Issue.SYMBOLIC_relationship_Issue);

            //To get Symbolic Issue
            String strType = PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_type_Issue);

            MapList relIssueList = null;
            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);

            DomainObject domObj = new DomainObject(strObjectId);

                //retrieving Issue List from Context
            relIssueList = (MapList)domObj.getRelatedObjects(
                                context,
                                strRelationIssue,
                                strType,
                                objectSelects,
                                null,
                                true,
                                false,
                                (short)1,
                                DomainConstants.EMPTY_STRING,
                                DomainConstants.EMPTY_STRING);

            MapList retIssueList = new MapList(relIssueList.size());
            Map idMap = null;
            //Making a maplist of only ids
           for(int i=0; i< relIssueList.size(); i++)
            {
                idMap = (Map) relIssueList.get(i);
                Map idfinalMap = new HashMap(1);
                idfinalMap.put("id",idMap.get(DomainConstants.SELECT_ID));
                retIssueList.add(idfinalMap);
            }
            return retIssueList;
        }

          /**
         * Updates the Resolution Date.
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap containing the following entries:
         * paramMap - a HashMap containing the following keys, "objectId", "old RDOIds", "New OID", "Old RDO Rel Ids".
         * @return Object - boolean true if the operation is successful
         * @throws Exception if operation fails
         * @since Common 11.0
         */

        public void updateResolutionDate(Context context, String[] args)
                throws Exception
        {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      HashMap paramMap = (HashMap) programMap.get("paramMap");
      HashMap requestMap = (HashMap) programMap.get("requestMap");
      HashMap mode = (HashMap) programMap.get("mode");
      String strObjId = (String) paramMap.get("objectId");
      String flddspValue = ((String[])requestMap.get("ResolutionDate"))[0];
      if(flddspValue != null && !"".equals(flddspValue))
      {
        double iClientTimeOffset = (new Double((String) requestMap.get("timeZone"))).doubleValue();
        int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();;
        Locale locale = (Locale)requestMap.get("localeObj");
        try {
                    flddspValue = eMatrixDateFormat.getFormattedInputDate(context, flddspValue,
                                                                        iClientTimeOffset,
                                                                        locale);
          String strAttribResolutionDate = PropertyUtil.getSchemaProperty(context,
                  Issue.SYMBOLIC_attribute_ResolutionDate);
          DomainObject dom = new DomainObject(strObjId);
          dom.setAttributeValue(context, strAttribResolutionDate, flddspValue);

        } catch(Exception e){
          // do nothing
        }
      }
    }


/**
         * Gets the Resolution Date.
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap containing the following entries:
         * paramMap - a HashMap containing the following keys, "objectId", "old RDOIds", "New OID", "Old RDO Rel Ids".
         * @return Object - boolean true if the operation is successful
         * @throws Exception if operation fails
         * @since Common 11.0
         */

        public String getResolutionDate(Context context, String[] args)
                throws Exception
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            HashMap fieldMap = (HashMap) programMap.get("fieldMap");
            String strObjId = (String) paramMap.get("objectId");
            String mode = (String) requestMap.get("mode");

            // Added for the Bug 345286 1

            String formattedDisplayDateValue = "";
            double iClientTimeOffset = (new Double((String) requestMap.get("timeZone"))).doubleValue();
            int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            Locale locale1 = (Locale) requestMap.get("localeObj");

            // Added for the Bug 345286 1 end

            DomainObject dom = new DomainObject(strObjId);
            StringList sl = new StringList(2);
            sl.add("attribute[" + PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_attribute_ResolutionDate) + "]");
            sl.add("originated");
            Map map = dom.getInfo(context, sl);
            String fieldValueDisplay = (String) map.get("attribute[" + PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_attribute_ResolutionDate) + "]");

            // Added for the Bug 345286 1

            if (fieldValueDisplay != null && fieldValueDisplay.length() > 0) {
                formattedDisplayDateValue = eMatrixDateFormat.getFormattedDisplayDateTime(context, fieldValueDisplay,
                                false, iDateFormat, iClientTimeOffset, locale1);
            }

            // Added for the Bug 345286 1 end

            // Added for IR-053232V6R2011x : start
            String originatedDate_formattedvalue = "";
            String strOriginated = (String) map.get("originated");
            if (strOriginated != null && strOriginated.length() > 0) {
                originatedDate_formattedvalue = eMatrixDateFormat.getFormattedDisplayDateTime(context, strOriginated,
                                false, iDateFormat, iClientTimeOffset, locale1);
            }
            // Added for IR-053232V6R2011x : end

            String fieldName = (String) fieldMap.get("name");
            StringBuffer outStr = new StringBuffer();
            String strReturn = "";

            // Added for the Bug 345286 1

            if ((mode == null || mode.equalsIgnoreCase("view"))
                    && (formattedDisplayDateValue != null && formattedDisplayDateValue.length() > 0)) {
                strReturn = formattedDisplayDateValue;

                // Added for the Bug 345286 1 end

            }
            //TP2: Added check for mode == "" and checking with OR condition instead of AND for bug # IR-023865V6R2011 Start

            //if ((mode == "" || mode == null || mode.equalsIgnoreCase("view"))

            //|| (formattedDisplayDateValue != null && formattedDisplayDateValue.length() > 0)) {

            //strReturn = formattedDisplayDateValue;
            //}

            //TP2: Added check for mode == "" for bug # IR-023865V6R2011 End
			//Modified for IR-072793V6R2012 - reversed the strings in equalsIgnoreCase
            else if ("edit".equalsIgnoreCase(mode)) {
                outStr.append("<input type=\"text\" readonly=\"readonly\" ");
                outStr.append(" onChange=\"saveFieldObj(this)");
                outStr.append("\" name=\"");
                outStr.append(fieldName);

                // Modified for the Bug 345286 1

                outStr.append("\" value='" + XSSUtil.encodeForHTMLAttribute(context, formattedDisplayDateValue) + "'\"\" id=\"");
                // Modified for the Bug 345286 1 end

                outStr.append(fieldName).append("\">&nbsp;");

                // <Fix 368588 0>

                // Need to render anchor tag to invoke showCalendar as below
                // <a href="javascript:;" onclick="javascript:showCalendar('editDataForm', 'ResolutionDate', '2/17/2009 12:00:00 PM', '',
                //                      saveFieldObjByName('EstimatedStartDate')); return false;"
                //                      name="ResolutionDate_date" >
                //      <img src="../common/images/iconSmallCalendar.gif" alt="Date Picker" border="0">
                // </a>
                outStr.append("<a href=\"javascript:;\" onclick=\"javascript:showCalendar('editDataForm', ");
                outStr.append("'").append(fieldName).append("', ");
                outStr.append("'").append(formattedDisplayDateValue).append("', '', ");
                outStr.append("saveFieldObjByName('").append(fieldName).append("')); return false;\" name=\"").append(fieldName).append("_date\">");
                outStr.append("<img src=\"../common/images/iconSmallCalendar.gif\" alt=\"Date Picker\" border=\"0\"> </a>");
                // </Fix 368588 0>

                outStr.append("<input type=\"hidden\" name=\"");
                outStr.append(fieldName).append("_msvalue");

                // Modified for the Bug 345286 1
                outStr.append("\"  value='" + XSSUtil.encodeForHTMLAttribute(context, formattedDisplayDateValue) + "'>");
                // Modified for the Bug 345286 1 end

                outStr.append("<input type=hidden name='OriginatedDate' value='" + map.get("originated") + "'>");
                outStr.append("<input type=hidden name='originatedDate_formattedvalue' value='" + XSSUtil.encodeForHTMLAttribute(context, originatedDate_formattedvalue) + "'>");//Added for IR-053232V6R2011x
                outStr.append("<input type=hidden name='ServerCurrentDate' value='" + Calendar.getInstance().getTime().getTime() + "'>");
                strReturn = outStr.toString();
            }

            return strReturn;
        }

          /**
         * Connects the Design Responsibility Organization to the Issue Object.
         * This method is called for Issue objects as update function in their respective Edit pages.
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap containing the following entries:
         * paramMap - a HashMap containing the following keys, "objectId", "old RDOIds", "New OID", "Old RDO Rel Ids".
         * @return Object - boolean true if the operation is successful
         * @throws Exception if operation fails
         * @since Common 11.0
         */

        public void connectResponsibleOrganization(Context context, String[] args)
                throws Exception
        {
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                HashMap paramMap = (HashMap) programMap.get("paramMap");
                String strObjId = (String) paramMap.get("objectId");

                Map requestMap = (Map)programMap.get("requestMap");

                String []strNewID = (String[])requestMap.get("RDOOID");
                String strNewOrganizationId = strNewID[0];

                String [] oldRDOIds = (String[])requestMap.get("OLDRDOID");
                String [] oldRDORelIds = (String[])requestMap.get("RDORELID");

                String strOldRDOIds = "";
                String strOldRDORelIds = "";
                boolean contextPushed = false;

                if(oldRDOIds != null && oldRDOIds.length > 0)
                {
                    strOldRDOIds = oldRDOIds[0];
                    strOldRDORelIds = oldRDORelIds[0];
                }

            try
            {
                ContextUtil.pushContext(context);
                contextPushed = true;
                if(!strOldRDOIds.equals(strNewOrganizationId))
                {
                    StringList strListOldRDOIds = FrameworkUtil.split(strOldRDOIds,",");
                    StringList strListOldRDORelIds = FrameworkUtil.split(strOldRDORelIds,",");
                    int newOIDIndex = strListOldRDOIds.indexOf(strNewOrganizationId);
                    int size = strListOldRDOIds.size();

                    for(int i = 0 ; i < size ; i++)
                    {
                        if(i != newOIDIndex || "".equals(strNewOrganizationId))
                        {
                            DomainRelationship.disconnect(context,
                              (String)strListOldRDORelIds.get(i));
                        }
                    }
                    if(newOIDIndex == -1 && !"".equals(strNewOrganizationId))
                    {
                        // connect the ecr object to new RDO
                        setId(strNewOrganizationId);
                        DomainRelationship.connect(context,this,
                              DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
                              new DomainObject(strObjId));
                    }
                }

            }
            catch (Exception exp)
            {
                throw new FrameworkException(exp);
            }
                if( contextPushed)
                    ContextUtil.popContext(context);
        }

        /**
         * Gets the Organizations connected to Issue Objects with the relationship Design Responsibility.
         * This is used in the display of properties page of these objects, normally called from Webform.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the object id.
     * @return String of Organizations connected to the Object with the relationship Design Responsibility in HTML format.
     * @throws Exception if the operation fails.
         * @since Common 11.0
     */

        public String getResponsibleOrganization(Context context,String[] args)
             throws Exception
        {
             HashMap programMap = (HashMap) JPO.unpackArgs(args);
             HashMap requestMap = (HashMap) programMap.get("requestMap");
             String strObjId = (String) requestMap.get("objectId");
             String suiteKey = (String) requestMap.get("suiteKey");
             String languageStr = (String) requestMap.get("languageStr");
             String printFormat = (String)requestMap.get("PFmode");
             String reportFormat = (String)requestMap.get("reportFormat");

             setId(strObjId);
             String strOwner = this.getInfo(context,
                          DomainConstants.SELECT_OWNER);
             String strLoginUser = context.getUser();
             String strDisable ="";
             if(strOwner !=null && strLoginUser !=null && !strOwner.equals(strLoginUser))
             {
                 strDisable ="disabled";
             }
             java.util.List organizationList = new MapList();

             StringList ObjectSelectsList = new StringList(
                                             DomainConstants.SELECT_NAME);
             ObjectSelectsList.add (DomainConstants.SELECT_ID);

             StringList relSelectList = new StringList(
                                             DomainConstants.SELECT_RELATIONSHIP_ID);

             ContextUtil.pushContext(context);

             organizationList = getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
                        "*",
                        ObjectSelectsList,
                        relSelectList,
                        true,
                        true,
                        (short) 1,
                        DomainConstants.EMPTY_STRING,
                        DomainConstants.EMPTY_STRING);
             ContextUtil.popContext(context);

             String strMode = (String) requestMap.get("mode");

             StringBuffer returnString=new StringBuffer();

             StringBuffer strBufRDONames = new StringBuffer();
             StringBuffer strBufRDOIds      = new StringBuffer();
             StringBuffer strBufRELIds       = new StringBuffer();
             StringBuffer strBufRDONamesForView = new StringBuffer();
             StringBuffer strBufRDONamesForPrint = new StringBuffer();
             StringBuffer strBufRDONamesForExport = new StringBuffer();

             if (!organizationList.isEmpty())
             {
                 Iterator mapItr = organizationList.iterator();
                 Map mapOrg = null;

             while(mapItr.hasNext())
             {
                  mapOrg = (Map)mapItr.next();
                  if("edit".equalsIgnoreCase(strMode))
                  {
                      strBufRDOIds.append(mapOrg.get(DomainConstants.SELECT_ID));
                      strBufRDOIds.append(",");
                      strBufRELIds.append(mapOrg.get(DomainConstants.SELECT_RELATIONSHIP_ID));
                      strBufRELIds.append(",");
                      strBufRDONames.append(mapOrg.get(DomainConstants.SELECT_NAME));
                      strBufRDONames.append(",");
                  }
                  else
                  {
                      String strAccess = "";
                      try
                      {
                          DomainObject domObj =
                               new DomainObject((String)mapOrg.get(
                                            DomainConstants.SELECT_ID));
                          strAccess = domObj.getInfo(context,
                                           "current.access[read]");
                      }
                      catch (Exception e)
                      {
                          strAccess = "FALSE";
                      }
                      if(strAccess.equalsIgnoreCase("TRUE"))
                      {
                          String URLToShow = "../common/emxTree.jsp?objectId="
                                          + mapOrg.get(DomainConstants.SELECT_ID);
                          strBufRDONamesForView.append(
                                "<a href=\"javascript:showModalDialog(\'" +
                                URLToShow + "\',700,600,false)\">");
                          strBufRDONamesForView.append(
                                mapOrg.get(DomainConstants.SELECT_NAME)+"</a>");
                             strBufRDONamesForView.append(",");
                      }
                      else
                      {
                        strBufRDONamesForView.append(mapOrg.get(
                                            DomainConstants.SELECT_NAME));
                            strBufRDONamesForView.append(",");
                      }
                       if(null!=reportFormat && !reportFormat.equals("null") && (reportFormat.length() > 0))
                       {
                           strBufRDONamesForExport.append(
                               mapOrg.get(DomainConstants.SELECT_NAME));
                       }
                       if(null!=printFormat && !printFormat.equals("null") && (printFormat.length() > 0))
                       {
                           strBufRDONamesForPrint.append(
                               mapOrg.get(DomainConstants.SELECT_NAME));
                       }
                }
             }
        }

            if("edit".equalsIgnoreCase(strMode))
            {
                if(strBufRDOIds.length() > 0)
                {
                    strBufRDOIds.setLength(strBufRDOIds.length() -1);
                }
                if(strBufRELIds.length() > 0)
                {
                    strBufRELIds.setLength(strBufRELIds.length() -1);
                }
                if(strBufRDONames.length() > 0)
                {
                    strBufRDONames.setLength(strBufRDONames.length() -1);
                }

                returnString.append(
                    "<input type=\"text\" readonly=\"readonly\"  name=\"RDODisplay\"  value=\""+
                                            XSSUtil.encodeForHTMLAttribute(context,strBufRDONames.toString())+"\">");
                returnString.append(
                    "<input type=\"hidden\" name=\"RDO\" value=\""+
                    						XSSUtil.encodeForHTMLAttribute(context,strBufRDONames.toString())+"\">");
                returnString.append(
                    "<input type=\"hidden\" name=\"RDOOID\" value=\""+
                    						XSSUtil.encodeForHTMLAttribute(context,strBufRDOIds.toString())+"\">");
                returnString.append(
                    "<input type=\"hidden\" name=\"RDORELID\" value=\""+
                    						XSSUtil.encodeForHTMLAttribute(context,strBufRELIds.toString())+"\">");
                returnString.append(
                    "<input type=\"hidden\" name=\"OLDRDOID\" value=\""+
                    						XSSUtil.encodeForHTMLAttribute(context,strBufRDOIds.toString())+"\">");
                returnString.append(
                    "<input type=\"button\" name=\"btnRDO\" value='...'  "+
                    						XSSUtil.encodeForHTMLAttribute(context,strDisable)+" onClick=\"");
               returnString.append(
                    "javascript:showChooser(" +
            		   "'../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace&chooserType=CustomChooser&submitAction=refreshCaller&suiteKey=Components&submitURL=AEFSearchUtil.jsp&table=AEFGeneralSearchResults&fieldNameDisplay=RDODisplay&fieldNameActual=RDO&selection=single&mode=Chooser");
                returnString.append(
                                            "&suiteKey="+XSSUtil.encodeForJavaScript(context,suiteKey)+",600,600')\">");

                if(!strDisable.equals("disabled"))
                {
                    returnString.append(
                        "&nbsp;&nbsp;<a href=\"JavaScript:basicClear('RDO')\">"+
                              ComponentsUtil.i18nStringNow(
                              "emxComponents.Common.Clear",languageStr)+"</a>");
                }
            }
            else
            {
                if(strBufRDONamesForView.length() > 0)
                {
                    strBufRDONamesForView.setLength(strBufRDONamesForView.length() -1);
                    returnString = strBufRDONamesForView;
                }
                if(strBufRDONamesForPrint.length() > 0)
                {
                    returnString = strBufRDONamesForPrint;
                }
                if(strBufRDONamesForExport.length() > 0)
                {
                    returnString = strBufRDONamesForExport;
                }
            }
        return returnString.toString();
     }

   /**
     * sets the Co Owners with the new list, normally called from Webform while editing.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds a HashMap containing the following entries:
     * programMap - a HashMap containing the following keys, "requestMap" "paramMap".
     * @throws Exception if operation fails
     * @since Common 11.0
     */
    public void setCoOwners(Context context,String[] args)
             throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        String[] strObjId = (String[]) requestMap.get(OBJECTID);

        HashMap paramMap = (HashMap) programMap.get(PARAMMAP);
        String strOldCoOwners = (String)paramMap.get("Old value");
        String strNewCoOwners = (String)paramMap.get("New Value");

        StringList newCoOwners = FrameworkUtil.split(strNewCoOwners,"|");

        if (!(strOldCoOwners.equals(strNewCoOwners)))
        {
            String strIssueID = strObjId[0];
            this.setId(strIssueID);

            Issue issue = (Issue)DomainObject.newInstance(context,strIssueID);

            MultipleOwner mulOwner = new MultipleOwner(issue);
            mulOwner.setCoOwners(context, newCoOwners);

        }
    }

        /**
         * Method to display the Create Issue command. If the type is Classified Part or Document which connected to Classified Part.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         * objectId - Object Id of the Context object
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
         * @since Common 11.0
         */
        public boolean showCreateIssue(Context context, String[] args)
            throws Exception {
            // Unpack the arguments and get the object id
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) programMap.get(OBJECT_ID);

            if(!(strObjectId == null || "".equals(strObjectId)))
            {
                String strParentType = getParentType(context, strObjectId);
                DomainObject dom = new DomainObject(strObjectId);
                if (strParentType.equals(TYPE_DOCUMENTS))
                {
                    StringBuffer sbRelRefDoc = new StringBuffer("to[");
                        sbRelRefDoc.append(RELATIONSHIP_REFERENCE_DOCUMENT);
                        sbRelRefDoc.append("].from.to[");
                        sbRelRefDoc.append(RELATIONSHIP_CLASSIFIED_ITEM);
                        sbRelRefDoc.append("].id");

                    StringBuffer sbRelPartSpec = new StringBuffer("to[");
                        sbRelPartSpec.append(RELATIONSHIP_PART_SPECIFICATION);
                        sbRelPartSpec.append("].from.to[");
                        sbRelPartSpec.append(RELATIONSHIP_CLASSIFIED_ITEM);
                        sbRelPartSpec.append("].id");

                    StringList slDocRel = new StringList(2);
                    slDocRel.add(sbRelRefDoc.toString());
                    slDocRel.add(sbRelPartSpec.toString());

                    Map mapRelRefDoc = dom.getInfo(context, slDocRel);
                    String strRelRefDoc = (String) mapRelRefDoc.get(sbRelRefDoc.toString());
                    String strRelSpeDoc = (String) mapRelRefDoc.get(sbRelPartSpec.toString());

                    if ((!(strRelRefDoc == null || "".equals(strRelRefDoc) || "null".equals(strRelRefDoc))  ||
                                        !(strRelSpeDoc == null || "".equals(strRelSpeDoc) || "null".equals(strRelSpeDoc))
                                        ))
                    {
                        return true;
                    }
                }
                else if(strParentType.equals(DomainConstants.TYPE_PART))
                {
                    String strRelClassPart = dom.getInfo(context,"to[" +
                                DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM + "].id");
                    if (!(strRelClassPart == null || "".equals(strRelClassPart)))
                    {
                        return true;
                    }
                }
                else if(strParentType.equals(TYPE_CLASSIFICATION))
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
            return false;
        }

        /**
         * Method to display the Issues command in the Tree Category. In Library or Class Level if the Logged in user is Librarian
         * or in Parts or Documents, if the type is Classified Part or Document which connected to Classified Part.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - args contains a Map with the following entries
         * objectId - Object Id of the Context object
         * @return - boolean (true or false)
         * @throws Exception if the operation fails
         * @since Common 11.0
         */
        public boolean showRollupIssue(Context context, String[] args)
            throws Exception {
            // Unpack the arguments and get the object id
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) programMap.get(OBJECT_ID);

            if(!(strObjectId == null || "".equals(strObjectId)))
            {
                String strParentType = getParentType(context, strObjectId);
                if (strParentType.equals(TYPE_LIBRARIES)||strParentType.equals(TYPE_CLASSIFICATION))
                {
                    String strContextUser = context.getUser();

                    //Instantiating the new Person object
                    matrix.db.Person pUser = new matrix.db.Person(strContextUser);
                    String strRoleLibrarian = PropertyUtil.getSchemaProperty( context, Issue.SYMBOLIC_role_Librarian);
                    String role_VPLMProjectLeader=PropertyUtil.getSchemaProperty(context,"role_VPLMProjectLeader");
                    String role_VPLMProjectAdministrator=PropertyUtil.getSchemaProperty(context,"role_VPLMProjectAdministrator");

                    //To Check whether the user is assigned the role Librarian,VPLMProjectLeader,VPLMProjectAdministrator...
                    return pUser.isAssigned(context, strRoleLibrarian)
                    || pUser.isAssigned(context, role_VPLMProjectLeader)
                    || pUser.isAssigned(context, role_VPLMProjectAdministrator);
                }
                else {
                    DomainObject dom = new DomainObject(strObjectId);
                    if (strParentType.equals(TYPE_DOCUMENTS))
                    {
                         StringBuffer sbRelRefDoc = new StringBuffer("to[");
                         sbRelRefDoc.append(RELATIONSHIP_REFERENCE_DOCUMENT);
                         sbRelRefDoc.append("].from.to[");
                         sbRelRefDoc.append(RELATIONSHIP_CLASSIFIED_ITEM);
                         sbRelRefDoc.append("].id");

                         StringBuffer sbRelPartSpec = new StringBuffer("to[");
                         sbRelPartSpec.append(RELATIONSHIP_PART_SPECIFICATION);
                         sbRelPartSpec.append("].from.to[");
                         sbRelPartSpec.append(RELATIONSHIP_CLASSIFIED_ITEM);
                         sbRelPartSpec.append("].id");

                         StringList slDocRel = new StringList(2);
                         slDocRel.add(sbRelRefDoc.toString());
                         slDocRel.add(sbRelPartSpec.toString());

                         Map mapRelRefDoc = dom.getInfo(context, slDocRel);
                         String strRelRefDoc = (String) mapRelRefDoc.get(sbRelRefDoc.toString());
                         String strRelSpeDoc = (String) mapRelRefDoc.get(sbRelPartSpec.toString());

                         if ((!(strRelRefDoc == null || "".equals(strRelRefDoc) || "null".equals(strRelRefDoc))  ||
                              !(strRelSpeDoc == null || "".equals(strRelSpeDoc) || "null".equals(strRelSpeDoc))
                              ))
                         {
                              return true;
                         }
                    }
                    else if(strParentType.equals(DomainConstants.TYPE_PART))
                    {
                        /*String strRelClassPart = dom.getInfo(context,"to[" +
                                    DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM + "].id");
                        return (!(strRelClassPart == null ||
                                    "".equals(strRelClassPart) || "null".equals(strRelClassPart)));
                        */
                    	return false;
                    }
                }
            }
            return false;
        }
  /**
     * Added for getting the Parent Type of the given objectId.
     * @param Context context - framework context
     * @param String objectId - string object Id
     * @return String parent - returns Kindof
     * @since Common 11.0
     */
    private String getParentType(Context context, String objectId) throws Exception {
        try {
            DomainObject dom = new DomainObject(objectId);
            String objType = dom.getInfo(context,DomainConstants.SELECT_TYPE);
            return MqlUtil.mqlCommand(context, "print type $1 select $2 dump $3", objType, "kindof", "|");
        } catch (Exception e) {
            return "";
        }
    }
        /**
         * Shows the Reported Against as Type Name Revision.
         * This is used to display in Rollup Issues
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the object id.
     * @return String of Organizations connected to the Object with the relationship Design Responsibility in HTML format.
     * @throws Exception if the operation fails.
         * @since Common 11.0
     */

        public Vector getReportedAgainst(Context context,String[] args)
             throws Exception
        {

            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map) programMap.get("paramList");
            MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);

            Vector vec = new Vector(relBusObjPageList.size());
            for(int i=0; i<relBusObjPageList.size(); i++)
            {

                Map map = (Map)relBusObjPageList.get(i);
                String strObjectId = (String) map.get(SELECT_ID);

                String strShowLink = null;
                if(!(strObjectId == null || "".equals(strObjectId)))
                {

                    DomainObject dom = new DomainObject(strObjectId);
                    String strRelationIssue = PropertyUtil.getSchemaProperty(context,
                                                      Issue.SYMBOLIC_relationship_Issue);
                    StringList slRepAgainstIds = dom.getInfoList(context,"from[" + strRelationIssue + "].to.id");
                    Iterator repAgainstIdItr = slRepAgainstIds.iterator();
                    while(repAgainstIdItr.hasNext()) {
                        String strRepAgainstId = (String)repAgainstIdItr.next();
                        StringList slRepAgainst = new StringList(3);
                        slRepAgainst.add(DomainConstants.SELECT_TYPE);
                        slRepAgainst.add(DomainConstants.SELECT_NAME);
                        slRepAgainst.add(DomainConstants.SELECT_REVISION);
                        DomainObject domIssue = new DomainObject(strRepAgainstId);
                        Map mapRepAgainst = domIssue.getInfo(context, slRepAgainst);
                        String strRepAgainstType = (String) mapRepAgainst.get(DomainConstants.SELECT_TYPE);
                        String strRepAgainstName = (String) mapRepAgainst.get(DomainConstants.SELECT_NAME);
                        String strRepAgainstRev = (String) mapRepAgainst.get(DomainConstants.SELECT_REVISION);

                        String URLToShow = "../common/emxTree.jsp?objectId="
                                                  + XSSUtil.encodeForJavaScript(context,strRepAgainstId);

                        if((paramList.get("reportFormat")) != null)
                		{
                        	strShowLink = strRepAgainstType +
                            "," + strRepAgainstName + "," + strRepAgainstRev ;
                		}else{
                        strShowLink = (strShowLink == null) ? "" : (strShowLink + "<br/>");
                        strShowLink += "<a href=\"javascript:showModalDialog(\'" +
                                        URLToShow + "\',700,600,false)\">" + XSSUtil.encodeForXML(context,strRepAgainstType) +
                                        " " + XSSUtil.encodeForXML(context,strRepAgainstName) + " " + XSSUtil.encodeForXML(context,strRepAgainstRev) + "</a>";
                		}
                    }
                }
                else {
                    strShowLink ="";
                }
                vec.add(strShowLink);
            }
                return vec;
        }

        /**
         * Method to get the Notification Message when Issue is modified.
         *
         * @param context - the eMatrix <code>Context</code> object
         * @param args - holds Hashmap Program Map
         * @return - string containing Organization name
         * @throws Exception if the operation fails
         * @since Common 11.0
         */
        public String getNotificationHTML(
                Context context,
                String[] args)
                throws Exception {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            //getting parent object Id from args
            String objectId = (String) programMap.get(DomainConstants.SELECT_ID);

            String message =
                        EnoviaResourceBundle.getProperty(context,
                                        RESOURCE_BUNDLE_COMPONENTS_STR,
                                        context.getLocale(),
                                        MESSAGE_MODIFY_BODY);

                        //Expanding the macros used in the message subject and body
                        message = Issue.replaceMacroWithValues(context,objectId,message);

            StringBuffer urlsBuffer = new StringBuffer();

            String baseURL = emxMailUtil_mxJPO.getBaseURL(context, null);
            if (baseURL != null && !"".equals(baseURL)) {
                urlsBuffer.append("\n");
                urlsBuffer.append("\n");
                urlsBuffer.append(baseURL);
                urlsBuffer.append("?objectId=");
                urlsBuffer.append(objectId);
            }
            baseURL = message + "<a href = " + urlsBuffer.toString() + ">" + urlsBuffer.toString() + "</a>" ;
            return baseURL;
        }

        /**
         * To obtain the Reference Document (Document).
         *
         * @param context context for this request
         * @param args - Holds the parameters passed from the calling method
                       queryLimit
                       Type
                       Name
                       Description
                       Owner
                       State
                       latestOnly
                       TypeDocument
                       Revision
                       vaultOption
                       VaultDisplay.
         * @return MapList , the Object ids matching the search criteria
         * @throws Exception if the operation fails
         * @since AEF 10.0.5.0
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public static Object getVCDocuments (Context context, String[] args)
        throws Exception
        {

          Map programMap = (Map) JPO.unpackArgs(args);
          short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

          String strType = (String)programMap.get("TypeDocument");
          boolean inDesignSync = false;
          if (strType==null || strType.equals("") ) {
            strType = SYMB_WILD;
          }

          String strName = (String)programMap.get("Name");

          if (strName==null || strName.equals("") ) {
            strName = SYMB_WILD;
          }


          String strTitle = (String)programMap.get("Title");

          String strDesc = (String)programMap.get("Description");
          String DesignSyncFile = (String)programMap.get("DesignSyncFile");

          String DesignSyncFolder = (String)programMap.get("DesignSyncFolder");
          String DesignSyncModule = (String)programMap.get("DesignSyncModule");
          String server = (String)programMap.get("server");

           //Begin of modify by Yukthesh, Infosys for Bug#311161 on 10 Nov,2005.
          // Here the key was changed from Owner to OwnerDisplay as below
          String strOwner = (String)programMap.get("OwnerDisplay");
          //End of modify by Yukthesh, Infosys for Bug#311161 on 10 Nov,2005
              if ( strOwner==null || strOwner.equals("") ) {
            strOwner = SYMB_WILD;
          }

          StringList select = new StringList(1);
          select.addElement(DomainConstants.SELECT_ID);
          select.addElement(DomainConstants.SELECT_NAME);
          select.addElement(DomainConstants.SELECT_TYPE);
          select.addElement("attribute[Title]");
          select.addElement("vcfile");
          select.addElement("vcfolder");
          select.addElement("vcmodule");

          boolean start = true;
          StringBuffer sbWhereExp = new StringBuffer(120);

          if (strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
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
           String strAttrTitle = PropertyUtil.getSchemaProperty(
                                                              context,
                                                              Issue.SYMBOLIC_attribute_Title);

              if (strTitle!=null && (!strTitle.equals(SYMB_WILD)) && (!strTitle.equals("")) ) {
            if (start) {
              sbWhereExp.append(SYMB_OPEN_PARAN);
              start = false;
            } else {
              sbWhereExp.append(SYMB_AND);
            }
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_ATTRIBUTE);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(strAttrTitle);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_MATCH);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(strTitle);
            sbWhereExp.append(SYMB_QUOTE);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
          }
          if (DesignSyncFile!=null &&  (!DesignSyncFile.equals("")) ) {
              inDesignSync = true;
              if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
              } else {
                sbWhereExp.append(SYMB_AND);
              }
              sbWhereExp.append(SYMB_OPEN_PARAN);
              sbWhereExp.append("vcfile");
              sbWhereExp.append(SYMB_MATCH);
              sbWhereExp.append(SYMB_QUOTE);
              sbWhereExp.append("true");
              sbWhereExp.append(SYMB_QUOTE);
              if(!"None".equalsIgnoreCase(server)){
              sbWhereExp.append(SYMB_AND);
              sbWhereExp.append("vcfile.store");
             sbWhereExp.append(SYMB_MATCH);
             sbWhereExp.append(SYMB_QUOTE);
             sbWhereExp.append(server);
             sbWhereExp.append(SYMB_QUOTE);
              }
             sbWhereExp.append(SYMB_CLOSE_PARAN);
            }
          if (DesignSyncFolder!=null &&  (!DesignSyncFolder.equals("")) ) {
              if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                inDesignSync = true;
                start = false;
              } else {
                  if(inDesignSync)
                      sbWhereExp.append(SYMB_OR);
                  else
                     sbWhereExp.append(SYMB_AND);
              }
              sbWhereExp.append(SYMB_OPEN_PARAN);
              sbWhereExp.append("vcfolder");
              sbWhereExp.append(SYMB_MATCH);
              sbWhereExp.append(SYMB_QUOTE);
              sbWhereExp.append("true");
              sbWhereExp.append(SYMB_QUOTE);
              if(!"None".equalsIgnoreCase(server)){
              sbWhereExp.append(SYMB_AND);
               sbWhereExp.append("vcfolder.store");
              sbWhereExp.append(SYMB_MATCH);
              sbWhereExp.append(SYMB_QUOTE);
              sbWhereExp.append(server);
              sbWhereExp.append(SYMB_QUOTE);
              }
              sbWhereExp.append(SYMB_CLOSE_PARAN);
            }
          if (DesignSyncModule!=null &&  (!DesignSyncModule.equals("")) ) {
              if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
              } else {
                  if(inDesignSync)
                      sbWhereExp.append(SYMB_OR);
                  else
                     sbWhereExp.append(SYMB_AND);
              }
              sbWhereExp.append(SYMB_OPEN_PARAN);
              sbWhereExp.append("vcmodule");
              sbWhereExp.append(SYMB_MATCH);
              sbWhereExp.append(SYMB_QUOTE);
              sbWhereExp.append("true");
              sbWhereExp.append(SYMB_QUOTE);
              if(!"None".equalsIgnoreCase(server)){
              sbWhereExp.append(SYMB_AND);
               sbWhereExp.append("vcmodule.store");
              sbWhereExp.append(SYMB_MATCH);
              sbWhereExp.append(SYMB_QUOTE);
              sbWhereExp.append(server);
              sbWhereExp.append(SYMB_QUOTE);
              }
              sbWhereExp.append(SYMB_CLOSE_PARAN);
            }
           String strFilteredExpression = getFilteredExpression(context,programMap);
           if ( (strFilteredExpression != null && strFilteredExpression.length() >0) ) {
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
          MapList typeFinalMap=new MapList();

          mapList = DomainObject.findObjects(
                      context, strType,strName, "*", strOwner, "*",
                      sbWhereExp.toString(), "", true, select, sQueryLimit);

          for(int i=0;i<mapList.size();i++){

              Map typeMap = (Map) mapList.get(i);

              if( (DesignSyncFolder!=null &&  (!DesignSyncFolder.equals(""))) &&  (DesignSyncModule!=null &&
                      (!DesignSyncModule.equals("")) )) {

                  String type=(String)typeMap.get(DomainConstants.SELECT_TYPE);

                  if(type.equals(PropertyUtil.getSchemaProperty(context,"type_mxsysDSFAHolder"))){

                      String storeName=(String)typeMap.get(DomainConstants.SELECT_NAME);
                      String storePath=MqlUtil.mqlCommand(context, "print store $1 select $2 dump", storeName, "path");

                      if((storePath.equals("/") || storePath.equals("") ) ){

                          String id=(String)typeMap.get(DomainConstants.SELECT_ID);
                          String mxsysRevision=MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", id, "revision");

                          if (!mxsysRevision.equals("Modules")){
                              typeFinalMap.add(mapList.get(i));
                          }
                      }

                      else{
                          typeFinalMap.add(mapList.get(i));
                      }

                  }
                  else{
                      typeFinalMap.add(mapList.get(i));
                  }
              }

          else{
                  typeFinalMap.add(mapList.get(i));
             }

          }

         return typeFinalMap;
    }

        /** added for the Bug 360573
         * issueDeleteCheck - gets the list of Discussion objects connected to the context Issue
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - Route Object Id
         * @returns int
         * @throws Exception if the operation fails
         * @since Common V6R2010x
         */

           public int issueDiscussionDeleteCheck(Context context, String[] args)
           throws Exception
           {
              try
                {
                    StringList objectSelects=new StringList(1);
                    objectSelects.addElement(SELECT_ID);
                    Pattern relPattern=new Pattern(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_Thread));
                    relPattern.addPattern(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_Message));
                    Pattern typePattern=new Pattern(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Thread));
                    typePattern.addPattern(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Message));
                    DomainObject issueObj=new DomainObject(args[0]);
                    MapList list=issueObj.getRelatedObjects(context,
                    				 relPattern.getPattern(),
                                     typePattern.getPattern(),
                                     objectSelects,
                                     null,
                                     false,
                                     true,
                                     (short)0,
                                     null,
                                     null);
                    System.out.println("List..."+list);
                    String objectIds[]=new String[list.size()];

                  for(int i=0;i<list.size();i++)
                    {
                       objectIds[i]=(String)((Map)list.get(i)).get(SELECT_ID);
                    }
                     DomainObject.deleteObjects(context,objectIds);
               }
                catch (Exception ex)
                {
                  throw ex;
                }
              return 0;
            }
// IR-060833V6R2011x modification -Starts
           /**
            * The method to get html string for type field in workspace content page.
            *
            * @param context - the eMatrix <code>Context</code> object
            * @param args - holds no arguments
            * @return String containing html for type field in workspace content page.
            * @throws Exception if the operation fails
            * @since R210
            */
           public String getTypesForWorkspaceContent(Context context, String args[]) throws Exception
           {
               StringBuffer strTypeChooser = null;
               try {
                   strTypeChooser = new StringBuffer(150);
                   String strTypes = "";
                   String relType = DomainObject.RELATIONSHIP_VAULTED_OBJECTS;
                   try
                   {
                       strTypes = MqlUtil.mqlCommand(context, "print relationship $1 select $2 dump", relType, "totype");
                   }
                   catch (FrameworkException fe)
                   {
                       strTypes = " ";
                   }
                   strTypeChooser.append("<input type=\"text\" READONLY name=\"TypeDisplay\" value =\""+DomainConstants.TYPE_DOCUMENT+"\" id=\"\" size=\"20\" onFocus=\"this.blur();\">");
                   strTypeChooser.append("<input type=\"button\" name=\"TypeChooseButton\" value=\"...\" onclick=\"javascript:getTopWindow().showChooser('../common/emxTypeChooser.jsp?frameName=searchPane&formName=editDataForm&SelectType=singleselect&SelectAbstractTypes=true&InclusionList="+strTypes+"&observeHidden=false&ShowIcons=true&fieldNameActual=Type&fieldNameDisplay=TypeDisplay','400','400')\">");
                   strTypeChooser.append("<input type=\"hidden\" name=\"Type\" value=\"");
                   strTypeChooser.append(strTypes);
                   strTypeChooser.append("\" size=15>");
                   strTypeChooser.append("<br>");
               } catch (Throwable excp) {

               }
               return strTypeChooser.toString();
           }
// IR-060833V6R2011x modification -Ends

           @com.matrixone.apps.framework.ui.PostProcessCallable
           public Map closeIssuePostProcess(Context context, String[] args) throws FrameworkException {
               Map returnMap = new HashMap();
               try {
	                   Map programMap 		= (Map)JPO.unpackArgs(args);
	                   Map paramMap   		= (Map)programMap.get("paramMap");
	                   Map requestValuesMap = (Map) programMap.get("requestValuesMap");
	                   HashMap requestMap 	= (HashMap)programMap.get("requestMap");

	                   String languageStr   = (String) requestValuesMap.get("languageStr");
	                   String issueId 		= (String) paramMap.get("objectId");
	                   this.setId(issueId);

	                   closeIssuePostProcess(context, new HashMap(), languageStr);

               		} catch (Exception e){
               			returnMap.put("Message", e.getMessage());
               			returnMap.put("Action", "Error");
               		}
               return returnMap;
           }



           protected void closeIssuePostProcess(Context context, Map attributes, String language) throws FrameworkException {
               try {
                   StringList selectables = new StringList();
                   selectables.add(getAttributeSelect(ATTRIBUTE_ACTUAL_START_DATE));
                   selectables.add(SELECT_ORIGINATOR);

                   Calendar cal = Calendar.getInstance();
                   cal.add(Calendar.SECOND, -1);
                   Date date = cal.getTime();
                   String strDate = _mxDateFormat.format(date);

                   Map issueDetails = getInfo(context, selectables);

                   String strActualStartDate = (String) issueDetails.get(getAttributeSelect(ATTRIBUTE_ACTUAL_START_DATE));
                   if (UIUtil.isNullOrEmpty(strActualStartDate)) {
                       attributes.put(ATTRIBUTE_ACTUAL_START_DATE, strDate);
                   }
                   attributes.put(ATTRIBUTE_ACTUAL_END_DATE, strDate);

                   StateList lstStates = getStates(context);
                   State lastState = (State) lstStates.get((lstStates.size() - 1));
                   String strFinalState = (String) lastState.getName();
                   try {
                       ContextUtil.pushContext(context, null, null, null);
                       MqlUtil.mqlCommand(context, "trigger $1", true, "off");
                       setAttributeValues(context, attributes);
                       setState(context, strFinalState);
                   } finally {
                       //Command to set "trigger on"
                       MqlUtil.mqlCommand(context, "trigger $1", true, "on");
                        ContextUtil.popContext(context);
                   }

                   if(!getInfo(context, SELECT_CURRENT).equals(strFinalState)) {
                       throw new FrameworkException(ComponentsUtil.i18nStringNow("emxComponents.Alert.CloseIssueUnSuccessfull", language));
                   }

                   StringList toList = new StringList((String) issueDetails.get(SELECT_ORIGINATOR));
                   StringList objectList = new StringList(this.getId(context));

                   String subject = ComponentsUtil.i18nStringNow(MESSAGE_STATUS_SUBJECT, language);
                   String message = ComponentsUtil.i18nStringNow(MESSAGE_STATUS_BODY, language);

                   //Expanding the macros used in the message subject and body
                   subject = Issue.replaceMacroWithValues(context, this.getId(context), subject);
                   message = Issue.replaceMacroWithValues(context, this.getId(context), message);

                   MailUtil mail = new MailUtil();
                   //Sending mail to the originator
                   mail.sendMessage(context,
                           toList, EMPTY_STRINGLIST, EMPTY_STRINGLIST,
                           subject, message,
                           objectList);

               } catch (Exception e) {
                   throw new FrameworkException(e);
               }
           }

           @com.matrixone.apps.framework.ui.ProgramCallable
           public MapList issueCategoryChooser(Context context, String[] args) throws FrameworkException
           {
            MapList mapBusIds = new MapList();
            MapList objectList = new MapList();
            try {
                Map programMap = (Map)JPO.unpackArgs(args);

            String strWhereExpression = null;
            String strType = PropertyUtil.getSchemaProperty( context,SYMBOLIC_type_IssueCategory);

            StringList busSelects = new StringList(DomainConstants.SELECT_ID);
            busSelects.add(DomainConstants.SELECT_NAME);
            //running the query
            mapBusIds =   findObjects(
                                    context,
                                    strType,
                                    null,
                                    null,
                                    null,
                                    DomainConstants.QUERY_WILDCARD,
                                    strWhereExpression,
                                    false,
                                    busSelects);

            Iterator fieldsItr = mapBusIds.iterator();
            Map curField = new HashMap();

            while(fieldsItr.hasNext()){
                curField = (HashMap) fieldsItr.next();
                curField.put("selection", "none");
                objectList.add(curField);
            }
       }
            catch(Exception e)
            {
                throw new FrameworkException(e);
            }
           return objectList;
           }

           public StringList issueCategoryChooserName(Context context, String[] args) throws FrameworkException {
               StringList colData ;
               try {
                   Map programMap = (Map)JPO.unpackArgs(args);
                   MapList issueCategoryList =  (MapList) programMap.get("objectList");
                   Iterator mainCategoryItr = issueCategoryList.iterator();
                   colData = new StringList(issueCategoryList.size());

                   while(mainCategoryItr.hasNext())
                   {
                       Map currentMap =  (Map) mainCategoryItr.next();
                       colData.add((String)currentMap.get(DomainConstants.SELECT_NAME));
                   }

               }
               catch(Exception e)
               {
                   throw new FrameworkException(e);
               }

               return colData;

           }

           public StringList issueCategoryChooserDescription (Context context, String[] args) throws FrameworkException {
               MapList classlist = null;
               StringList colData ;
               try{
                   Map programMap = (Map)JPO.unpackArgs(args);
                   MapList issueCategoryList =  (MapList) programMap.get("objectList");
                   Iterator mainCategoryItr = issueCategoryList.iterator();
                   colData = new StringList(issueCategoryList.size());

                   while(mainCategoryItr.hasNext())
                   {
                       Map currentMap =  (Map) mainCategoryItr.next();
                       String objectId = (String)currentMap.get(DomainConstants.SELECT_ID);
                       DomainObject domCategory = newInstance(context, objectId);
                       colData.add((String)domCategory.getDescription(context));
                   }

               }
               catch(Exception e)
               {
                   throw new FrameworkException(e);
               }

               return colData;
       }

           @com.matrixone.apps.framework.ui.ProgramCallable
           public MapList issueCategoryExpand(Context context, String[] args) throws FrameworkException {
               MapList classlist = null;
               MapList objectList = new MapList();
               try {
                   Map programMap = (Map)JPO.unpackArgs(args);
                   String strObjectId = (String) programMap.get("objectId");

                   StringList busSelects = new StringList(DomainConstants.SELECT_ID);
                   busSelects.add(DomainConstants.SELECT_NAME);
                   DomainObject domCategory = newInstance(context, strObjectId);
                   String strType =
                    PropertyUtil.getSchemaProperty(
                        context,
                        SYMBOLIC_type_IssueClassification);
                classlist =
                    domCategory.getRelatedObjects(
                        context,
                        PropertyUtil.getSchemaProperty(
                            context,
                            SYMBOLIC_relationship_IssueCategoryClassification),
                        strType,
                        busSelects,
                        null,
                        false,
                        true,
                        (short) 1,
                        DomainConstants.EMPTY_STRING,
                        DomainConstants.EMPTY_STRING);

                Iterator fieldsItr = classlist.iterator();
                Map curField = new HashMap();

                while(fieldsItr.hasNext()){
                    curField = (Map) fieldsItr.next();
                    curField.put("selection","single");
					curField.put("hasChildren", "False");
                    objectList.add(curField);
                }

               }
               catch(Exception e)
               {
                   throw new FrameworkException(e);
               }

               return objectList;

           }

	/**
	 * Method returns the updated MapList containing status icon for status field in widgets
	 * @param context
	 * @param args
	 * @return Maplist containing the status icon for all the issues
	 * @throws Exception
	 * @since V6R2014x
	 */
	public MapList getSlipDaysIconForWidgets(Context context, String[] args)
	throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList widgetDataMapList = (MapList) programMap.get(UIWidget.JPO_WIDGET_DATA);
        String fieldKey = (String) programMap.get(UIWidget.JPO_WIDGET_FIELD_KEY);
        Map<String, String> widgetArgs = (Map<String, String>) programMap.get(UIWidget.JPO_WIDGET_ARGS);

        String baseURI = widgetArgs.get(UIWidget.ARG_BASE_URI);

		for (int i = 0; i < widgetDataMapList.size(); i++) {
			HashMap collMap = (HashMap)widgetDataMapList.get(i);
			String strEstimatedEndDate = (String)collMap.get("attribute["+DomainConstants.ATTRIBUTE_ESTIMATED_END_DATE+"]");
			String strActualEndDate = (String)collMap.get("attribute["+DomainConstants.ATTRIBUTE_ACTUAL_END_DATE+"]");
			String strState = (String)collMap.get(DomainConstants.SELECT_CURRENT);

			collMap.put(fieldKey, getStatusIconTag(context, strEstimatedEndDate, strActualEndDate, strState, baseURI));
		}
		return widgetDataMapList;
	}

	/**
	 * Method returns Status Image of the Issue object
	 * @param context
	 * @param strEstimatedEndDate
	 * @param strActualDate
	 * @param strState
     * @param baseURI - the ENOVIA URI path.
	 * @return String Status image of the Issue object
	 * @throws Exception
	 * @since V6R2014x
	 */
	private String getStatusIconTag(Context context, String strEstimatedEndDate, String strActualEndDate, String strState, String baseURI)
	throws Exception {
		String strClose = DomainObject.STATE_ISSUE_CLOSE;

		String strStatusRed = "";
		String strStatusGreen = "";
		try {
			strStatusRed = EnoviaResourceBundle.getProperty(context,ICON_RED);
			strStatusGreen = EnoviaResourceBundle.getProperty(context,ICON_GREEN);
		} catch (Exception e) {
			String strContentLabel = EnoviaResourceBundle.getProperty(context,ERROR_VALUE);
			throw new Exception(strContentLabel);
		}
		//Parsing the strThreshold from String to int
		int iRedIconDays = Integer.parseInt(strStatusRed);
		int iGreenIconDays = Integer.parseInt(strStatusGreen);

		//Reading the tooltip from property file.
		String strTooltipRed = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), ICON_TOOLTIP_RED);
		String strTooltipGreen = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), ICON_TOOLTIP_GREEN);
		String strTooltipYellow = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),ICON_TOOLTIP_YELLOW);

		String strStatusIconTag = "&#160;";
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
		String strTodaysDate = "";
		int iDuration;
		int iDurationAbs;
		Date date = new Date();

		if (!(strState.equalsIgnoreCase(strClose)) && !(strState.equalsIgnoreCase(""))) {
			if (strEstimatedEndDate.equalsIgnoreCase(DomainConstants.EMPTY_STRING)) {
				strStatusIconTag = "&#160;";
			}else if ( !(strEstimatedEndDate.equalsIgnoreCase(DomainConstants.EMPTY_STRING))) {
				strTodaysDate = sdf.format(date);
				iDuration = Issue.daysBetween(strTodaysDate, strEstimatedEndDate);
				String title = null;
				String image = null;

				if (iDuration >= iRedIconDays) {
					title = iDuration + " "  + strTooltipRed;
					image = baseURI + "common/images/iconStatusRed.gif";
				} else if ( (iDuration < iRedIconDays) && (iDuration >= iGreenIconDays)) {
					title = strTooltipYellow;
					image = baseURI + "common/images/iconStatusYellow.gif";
				} else {
					iDurationAbs = -iDuration;
					title = iDurationAbs + " " + strTooltipGreen;
					image = baseURI + "common/images/iconStatusGreen.gif";
				}
				//XSSOK
				strStatusIconTag = "<img src=\"" + image + "\" border=\"0\"  align=\"middle\" TITLE=\"" + title + "\"/>";
			} else {
				strStatusIconTag = "&#160;";
			}
		} else if (strState.equalsIgnoreCase(strClose)) {
			if (strEstimatedEndDate.equalsIgnoreCase(DomainConstants.EMPTY_STRING) || strActualEndDate.equalsIgnoreCase(DomainConstants.EMPTY_STRING)) {
				strStatusIconTag = "&#160;";
			}else if ( !(strEstimatedEndDate.equalsIgnoreCase(DomainConstants.EMPTY_STRING)) && !(strActualEndDate.equalsIgnoreCase(DomainConstants.EMPTY_STRING))) {
				iDuration = Issue.daysBetween(strActualEndDate, strEstimatedEndDate);
				String title = null;
				String image = null;

				if (iDuration >= iRedIconDays) {
					title = iDuration + " " + strTooltipRed;
					image = baseURI + "common/images/iconStatusRed.gif";
				} else if ( (iDuration < iRedIconDays) && (iDuration >= iGreenIconDays)) {
					title = strTooltipYellow;
					image = baseURI + "common/images/iconStatusYellow.gif";
				} else {
					iDurationAbs = -iDuration;
					title = iDurationAbs + " " + strTooltipGreen;
					image = baseURI + "common/images/iconStatusGreen.gif";
				}
				//XSSOK
				strStatusIconTag = "<img src=\"" + image + "\" border=\"0\"  align=\"middle\" TITLE=\"" + title + "\"/>";
			} else {
				strStatusIconTag = "&#160;";
			}
		}
		return strStatusIconTag;
	}
	@com.matrixone.apps.framework.ui.ProgramCallable
	    public MapList getIssuescockpitItems(Context context, String[] args) throws Exception {

	        HashMap paramMap            = (HashMap) JPO.unpackArgs(args);
	        String sOID                 = (String) paramMap.get("objectId");
	        String sFilterGlobal        = (String) paramMap.get("filterGlobal");
	        String sFilterPriority      = (String) paramMap.get("filterPriority");
	        String sFilterProblemType   = (String) paramMap.get("filterProblemType");
	        String sFilterStatus        = (String) paramMap.get("filterStatus");
	        String sRelationships       = (String) paramMap.get("relationships");

	        if(null == sRelationships)      { sRelationships        = "*"; }
	        if(null == sFilterPriority)     { sFilterPriority       = ""; }
	        if(null == sFilterProblemType)  { sFilterProblemType    = ""; }
	        if(null == sFilterStatus)       { sFilterStatus         = ""; }

	        StringBuilder sbWhere = new StringBuilder();
	        StringList busSelects = new StringList();
	        busSelects.add(DomainConstants.SELECT_ID);
	        busSelects.add(DomainConstants.SELECT_POLICY);
			busSelects.add("current.access[modify]");

	        sbWhere.append("(("+DomainConstants.SELECT_OWNER+" == '").append(context.getUser()).append("') ||");
	        sbWhere.append(" (to[").append(PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_relationship_AssignedIssue));
	        sbWhere.append("].from == '").append(context.getUser()).append("') ||");
	        sbWhere.append(" attribute[" + DomainConstants.ATTRIBUTE_CO_OWNER + "]").append("~~'*").append(context.getUser()).append("*')");
	        
	        if(UIUtil.isNotNullAndNotEmpty(sFilterStatus)) {
	            busSelects.add(DomainConstants.SELECT_CURRENT);
	            sbWhere.append(" && ("+DomainConstants.SELECT_CURRENT+" == '").append(sFilterStatus).append("')");
	        }
	        if(UIUtil.isNotNullAndNotEmpty(sFilterProblemType)) {
	            busSelects.add("attribute["+PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_attribute_ProblemType)+"]");
	            sbWhere.append(" && ("+"attribute["+PropertyUtil.getSchemaProperty(context,Issue.SYMBOLIC_attribute_ProblemType)+"]"+" == '").append(sFilterProblemType).append("')");
	        }
	        if(UIUtil.isNotNullAndNotEmpty(sFilterPriority)) {
	            busSelects.add("attribute["+PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_attribute_Priority)+"]");
	            sbWhere.append(" && ("+"attribute["+PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_attribute_Priority)+"]"+" == '").append(sFilterPriority).append("')");
	        }
	        
	        if(UIUtil.isNotNullAndNotEmpty(sFilterGlobal)){
	            if("In Work".equals(sFilterGlobal)){
	            	sbWhere.append(" && (("+DomainConstants.SELECT_CURRENT+" == "+DomainConstants.STATE_ISSUE_ASSIGN+")");
	            	sbWhere.append(" || ("+DomainConstants.SELECT_CURRENT+" == "+DomainConstants.STATE_ISSUE_ACTIVE+"))") ;
	            } else if("Not Complete".equals(sFilterGlobal)){
	            	sbWhere.append(" &&("+DomainConstants.SELECT_CURRENT+" != "+DomainConstants.STATE_ISSUE_CLOSE+")"); 
	            } else if("Completed".equals(sFilterGlobal)){ 
	            	sbWhere.append(" && ("+DomainConstants.SELECT_CURRENT+" == "+DomainConstants.STATE_ISSUE_CLOSE+")"); 
	            }
	        }

	        MapList mlResults = Issue.retrieveRelatedIssues(context, sbWhere, busSelects, sOID, sRelationships);
	        return mlResults;
	    }


   /**
   	 * Method returns Objectids to exclude in FTS
   	 * @param context
        * @param Objectid
        * @param srcDestRelName
        * @param field_actual
        * @param isTo
   	 * @return Stringlist
   	 * @throws Exception
   	 * @since V6R2015x
   	 */

  @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   	public static Object excludeIssueRelatedObjects(Context context, String[] args ) throws Exception
   	{
   		StringList excludeOID = new StringList();
   		try
   		{
   			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
   			String objectId = (String)paramMap.get("objectId");
   			String strRelationship = (String)paramMap.get("srcDestRelName");
   			String strFieldtype = (String)paramMap.get("field_actual");
   			strFieldtype=strFieldtype.replace("TYPES=","");
   			strFieldtype=strFieldtype.trim();
   			StringTokenizer strToken = new StringTokenizer(strFieldtype,",");
   			int i =0;
   			String type[] = new String[strToken.countTokens()];
   	       StringBuffer strtype = new StringBuffer();
   	       int k =strToken.countTokens();
   			while(strToken.hasMoreTokens())
   			{

   				type[i] =strToken.nextToken();
   				String [] temp =type[i].split(":");

   				strtype.append(PropertyUtil.getSchemaProperty(context,temp[0]));
   				if(k!=++i)
   				{
   					strtype.append(",");
   				}

   			}
   			DomainObject domainObject = new DomainObject(objectId);
   			StringList selectList = new StringList();
   			selectList.add(DomainConstants.SELECT_ID);
   			 boolean from =false;
   			 boolean to=false;
   			if("true".equalsIgnoreCase((String)paramMap.get("isTo")))
   			{
   				 from =false;
   				 to =true;
   			}else
   			{
   				 from =true;
   				 to =false;
   			}
   			domainObject.open(context);
   			boolean c =domainObject.isOpen();
   			String rel =PropertyUtil.getSchemaProperty(context,strRelationship);

   			MapList mlist = domainObject.getRelatedObjects(context,
   					rel,
   					strtype.toString(),
   					selectList,
   					null,
   					from,
   					to,
   					(short)1,
   					null,
   					null
   					);
   			if ( mlist.size() >0)
      			{
      				Iterator itr = mlist.iterator();
      				Map map;
      				String str ="";
      				while (itr.hasNext())
      				{
      					map = (Map) itr.next();


      					   excludeOID.add((String)map.get(DomainConstants.SELECT_ID));


   		}
      			  excludeOID.add(objectId);
   		}
   			return excludeOID;
   		}
   		catch (Exception ex)
   		{

   			throw ex;
   		}
   	}

		/**
* This method is field program to retrieve the Company Name  
*
* @param context - the eMatrix <code>Context</code> object
* @param args holds request Map
* @return String - Company Name 
* @throws Exception if the operation fails
* @since AEF 418.HF5
*/
   	
@com.matrixone.apps.framework.ui.ProgramCallable
public String getCompanyName(Context context, String[] args)
   			throws Exception {
	String strBaseType = PropertyUtil.getSchemaProperty(context,
			Issue.SYMBOLIC_type_Issue);
	// Issuee bean instantiated for processing
	Issue issueBean = (Issue) DomainObject
			.newInstance(context, strBaseType);

	StringList companyList = issueBean.getUserCompanyIdName(context);
	String strCompanyId = "";
	strCompanyId = (String) companyList.elementAt(0);
	String strCompanyName = "";
	strCompanyName = (String) companyList.elementAt(1);
	return strCompanyName;
}

/**
* This method is field program to retrieve the Context object as Reported Against  
*
* @param context - the eMatrix <code>Context</code> object
* @param args holds request Map
* @return Object - Context object 
* @throws Exception if the operation fails
* @since AEF 418.HF5
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public Object getContextObjAsReportedAgainst(Context context, String[] args)
		throws Exception {
	StringBuffer sbReturnString = new StringBuffer();
	String strReturnContextObj = "";
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	HashMap paramMap = (HashMap) programMap.get("paramMap");
	HashMap requestMap = (HashMap) programMap.get("requestMap");
	String strContextObjectId = (String) requestMap.get("objectId");
	String strResolvedTo = (String) requestMap.get("resolvedTo");
	if (UIUtil.isNotNullAndNotEmpty(strContextObjectId)
			&& UIUtil.isNullOrEmpty(strResolvedTo)) {
		DomainObject dom = new DomainObject(strContextObjectId);
		strReturnContextObj = dom.getInfo(context,
				DomainConstants.SELECT_NAME);
		sbReturnString.append("<input type=\"text\" name=\"Reported Against1Display\" id=\"\" readonly=\"true\" value=\"" +XSSUtil.encodeForHTMLAttribute(context,strReturnContextObj)+ "\" maxlength=\"\" size=\"\"></input>");
		sbReturnString.append("<input type=\"hidden\" name=\"Reported Against1\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strReturnContextObj)+"\"></input>");
		sbReturnString.append("<input type=\"hidden\" name=\"Reported Against1OID\" value=\""+XSSUtil.encodeForHTMLAttribute(context, strContextObjectId)+"\"></input>");
	}	
	return sbReturnString.toString();
}

/**
* This is access program to check under which context Issue creation is triggered 
*
* @param context - the eMatrix <code>Context</code> object
* @param args holds request Map
* @return boolean - true/false - true if Issue is created under an Issue using 'Resolved By', false otherwise 
* @throws Exception if the operation fails
* @since AEF 418.HF5
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static boolean checkIssueCreateContext(Context context, String args[]) throws Exception
{
	boolean bFlag = false;
	HashMap programMap         = (HashMap) JPO.unpackArgs(args);
	HashMap requestMap = (HashMap) programMap.get("requestMap");
	String strContextObjectId = (String) programMap.get("parentOID");
	String strResolvedTo = (String) programMap.get("resolvedTo");
	if (UIUtil.isNotNullAndNotEmpty(strContextObjectId)
			&& UIUtil.isNullOrEmpty(strResolvedTo)) {
		bFlag = true;
	}
	return bFlag;
}

/**
* This is access program to check under which context Issue creation is triggered 
*
* @param context - the eMatrix <code>Context</code> object
* @param args holds request Map
* @return boolean - true/false - true if Issue is created under an Issue using 'Resolved By', false otherwise 
* @throws Exception if the operation fails
* @since AEF 418.HF5
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static boolean checkContextForCreateIssue(Context context, String args[]) throws Exception
{
	return !checkIssueCreateContext(context, args);
}

/**
* This is update function to connect Reporting Organization to a Issue 
*
* @param context - the eMatrix <code>Context</code> object
* @param args holds request Map
* @return void 
* @throws Exception if the operation fails
* @since AEF 418.HF5
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public void updateReportingOrganization(Context context, String[] args)
		throws Exception {
	HashMap progMap = (HashMap) JPO.unpackArgs(args);
	HashMap paramMap = (HashMap) progMap.get("paramMap");

	String sIssueId = (String) paramMap.get("objectId");
	String strCompanyId = (String) paramMap.get("New OID");
	DomainObject issueDomObj = DomainObject.newInstance(context, sIssueId);

	if (UIUtil.isNotNullAndNotEmpty(strCompanyId)) {
		String strRelReportingOrganization = PropertyUtil
				.getSchemaProperty(context,
						Issue.SYMBOLIC_relationship_ReportingOrganization);
		DomainRelationship.connect(context, new DomainObject(strCompanyId),
				strRelReportingOrganization, issueDomObj);
	}
}

/**
* This is update function to connect Reported Against to a Issue
*
* @param context - the eMatrix <code>Context</code> object
* @param args holds request Map
* @return void 
* @throws Exception if the operation fails
* @since AEF 418.HF5
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public void updateReportedAgainst(Context context, String[] args)
		throws Exception {
	HashMap progMap = (HashMap) JPO.unpackArgs(args);
	HashMap paramMap = (HashMap) progMap.get("paramMap");

	String sIssueId = (String) paramMap.get("objectId");
	String strReportedObj = (String) paramMap.get("New OID");
	DomainObject issueDomObj = DomainObject.newInstance(context, sIssueId);

	if (UIUtil.isNotNullAndNotEmpty(strReportedObj)) {
		String strRelIssue = PropertyUtil.getSchemaProperty(context,
				Issue.SYMBOLIC_relationship_Issue);
		DomainRelationship.connect(context, issueDomObj, strRelIssue,
				new DomainObject(strReportedObj));
		}
}

/**
* This is post Process method for Issue creation  .
*
* @param context - the eMatrix <code>Context</code> object
* @param args holds request Map
* @return void 
* @throws Exception if the operation fails
* @since AEF 418.HF5
*/
@com.matrixone.apps.framework.ui.PostProcessCallable
public void updateIssueDetails(Context context, String[] args)
		throws Exception {
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	HashMap paramMap = (HashMap) programMap.get("paramMap");
	HashMap requestMap = (HashMap) programMap.get("requestMap");
	String sIssueId = (String) paramMap.get("objectId");
	String strIssueCategoryClassification = (String) requestMap.get("Category Classification");
	String strIssueCategoryClassificationId = (String) requestMap.get("Category ClassificationOID");
	
	com.matrixone.apps.common.Issue issObj = new com.matrixone.apps.common.Issue();
	issObj.setId(sIssueId);
	
	if (UIUtil.isNotNullAndNotEmpty(strIssueCategoryClassification)) {
		updateIssueWithCategoryClassification(context, issObj, strIssueCategoryClassification, strIssueCategoryClassificationId);	
	}
	String strAffectedItem = (String) requestMap.get("Reported Against1OID");
	if (UIUtil.isNullOrEmpty(strAffectedItem)) {
		strAffectedItem = (String) requestMap.get("Reported Against2OID");
	}
	if (UIUtil.isNotNullAndNotEmpty(strAffectedItem)) {
		issObj.connectResponsibleOrganization(context, strAffectedItem);
	}
	String strCoOwners = (String) requestMap.get("CoOwners");
	if (UIUtil.isNotNullAndNotEmpty(strCoOwners)) {
		setCoOwnersToIssue(context, strCoOwners, issObj);
	}
	String strParentOID = (String) requestMap.get("parentOID");
	String strResolvedTo = (String) requestMap.get("resolvedTo");
	connectIfResolvedTo(context, strResolvedTo, strParentOID, issObj);
}


private void updateIssueWithCategoryClassification(Context context, Issue issObj, String strIssueCategoryClassification, String strIssueCategoryClassificationId) throws Exception{

	String strCategory = "";
	String strClassification = "";
	String strCategoryId = "";
	String strClassificationId = "";
	//To seperate the category and classification
	int iIndex = strIssueCategoryClassification.indexOf('/');
	strCategory = strIssueCategoryClassification.substring(0, iIndex);
	strClassification = strIssueCategoryClassification.substring(iIndex + 1, strIssueCategoryClassification.length());

	Map attributeMap = new HashMap();
	attributeMap.put(PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_IssueCategory),strCategory);
	attributeMap.put(PropertyUtil.getSchemaProperty(context,SYMBOLIC_attribute_IssueClassification),strClassification);

	issObj.setAttributeValues(context, attributeMap);

	// To fetch category and classification object ids
	iIndex = strIssueCategoryClassificationId.indexOf('|');
	strCategoryId = strIssueCategoryClassificationId.substring(0, iIndex);
	strClassificationId = strIssueCategoryClassificationId.substring(iIndex + 1, strIssueCategoryClassificationId.length());

	MapList mlResults = new MapList();
	String strOwner = "";
	if (UIUtil.isNotNullAndNotEmpty(strCategoryId)) {
		String attrIssueOwnedBy = "attribute[" + DomainConstants.ATTRIBUTE_ISSUEOWNEDBY + "].value";
		StringList relselects = new StringList(attrIssueOwnedBy);


		String busWhere = DomainConstants.SELECT_ID + "==" + strClassificationId;
		DomainObject categoryObj = newInstance(context, strCategoryId);
		
		mlResults = categoryObj.getRelatedObjects(context, RELATIONSHIP_ISSUECATEGORYCLASSIFICATION, TYPE_ISSUECLASSIFICATION, true, true, 1, null,
				relselects, busWhere, null, 0, null, null, null);

		Map map = (Hashtable) mlResults.get(0);
		strOwner = (String) map.get(attrIssueOwnedBy);
		issObj.setOwner(context, strOwner);
	}
}

private void setCoOwnersToIssue(Context context, String strCoOwners, Issue issue) throws Exception{
	StringList newCoOwners = FrameworkUtil.split(strCoOwners, "|");
	MultipleOwner mulOwner = new MultipleOwner(issue);
	mulOwner.setCoOwners(context, newCoOwners);
}

private void connectIfResolvedTo(Context context, String strResolvedTo, String strParentOID, Issue issueDomObj) throws Exception{
	if ("true".equalsIgnoreCase(strResolvedTo)) {
		DomainObject domParentObj = null;
		// Initializing the Parent Object
		if (UIUtil.isNotNullAndNotEmpty(strParentOID)) {
			domParentObj = DomainObject.newInstance(context, strParentOID);
		}

		DomainRelationship.connect(context, domParentObj, RELATIONSHIP_RESOLVED_TO, issueDomObj);
	}
}

		/**
 * This method returns the List categories for Category Summary page..
 *
 * @param context - the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @return MapList containing the Object ids matching the search criteria(Categories)
 * @throws Exception if the operation fails
 * @since AEF 418.HF3
 */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getIssueCategoryList(Context context, String[] args) throws FrameworkException
   {
	    MapList mapBusIds = new MapList();
	    MapList objectList = new MapList();
	    try {
	    	Map programMap = (Map)JPO.unpackArgs(args);
	
	    	String strWhereExpression = null;
	    	String strType = PropertyUtil.getSchemaProperty( context,SYMBOLIC_type_IssueCategory);
	
	    	StringList busSelects = new StringList(DomainConstants.SELECT_ID);
	    	busSelects.add(DomainConstants.SELECT_NAME);
	    	busSelects.add(DomainConstants.SELECT_TYPE);
	    	busSelects.add(DomainConstants.SELECT_DESCRIPTION);
	    	//running the query
	        mapBusIds =   findObjects(
	                                context,
	                                strType,
	                                null,
	                                null,
	                                null,
	                                DomainConstants.QUERY_WILDCARD,
	                                strWhereExpression,
	                                false,
	                                busSelects);	

	    }
	    catch(Exception e)
	    {
	    	throw new FrameworkException(e);
	    }
	    return mapBusIds;
}

           
  /**
 * This method returns field criteria for members with Issue Manger role .
 *
 * @param context - the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @return MapList containing the Object ids matching the search criteria
 * @throws Exception if the operation fails
 * @since AEF 418.HF3
 */
   @com.matrixone.apps.framework.ui.ProgramCallable
public String getIssueMangersList(Context context, String[] args) throws Exception {
		return "USERROLE="+ PropertyUtil.getSchemaProperty(context, Issue.SYMBOLIC_role_IssueManager) + "," + PropertyUtil.getSchemaProperty(context,"role_VPLMCreator");

}
       	


       	
/**
 * This method returns Id's to exclude from the search .
 *
 * @param context - the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @return StringList containing the Object ids to exclude from teh search
 * @throws Exception if the operation fails
 * @since AEF R418.HF3
 */
   @com.matrixone.apps.framework.ui.ProgramCallable
public MapList getExcludeIssueClassificationList(Context context, String[] args) throws Exception {
	   MapList returnList = new MapList();
	StringList tempList = new StringList();
	try {
		// unpacking the Arguments from variable args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);		
		String strObjectId = (String) programMap.get("emxTableRowId");
		
		StringList idList = com.matrixone.apps.domain.util.StringUtil.split(strObjectId, "|");
		strObjectId = (String)idList.get(0);
		DomainObject rowId = newInstance(context, strObjectId);
				

		StringList selectList = new StringList();
		selectList.add(DomainObject.SELECT_NAME);
		selectList.add("from["+DomainConstants.RELATIONSHIP_ISSUECATEGORYCLASSIFICATION+"].to.id");
		MULTI_VALUE_LIST.add("from["+DomainConstants.RELATIONSHIP_ISSUECATEGORYCLASSIFICATION+"].to.id");
		
		Map relIdMap = rowId.getInfo(context, selectList);	
		
		Object obj = relIdMap.get("from["+DomainConstants.RELATIONSHIP_ISSUECATEGORYCLASSIFICATION+"].to.id");		
		if(null != obj){
			StringList classList = new StringList();
			if (obj instanceof String) {		
				classList.add((String)(obj));
			}else{		
				classList = (StringList) obj;
			}   				
			
			for(int i = 0 ;i < classList.size();i++){
				tempList.add((String)classList.get(i) );
			}
		}
		
				
    	String strWhereExpression = null;
    	String strType = PropertyUtil.getSchemaProperty( context,SYMBOLIC_type_IssueClassification);

    	StringList busSelects = new StringList(DomainConstants.SELECT_ID);
    	busSelects.add(DomainConstants.SELECT_NAME);
    	busSelects.add(DomainConstants.SELECT_TYPE);
    	busSelects.add(DomainConstants.SELECT_DESCRIPTION);
    	//running the query
    	MapList mapBusIds =   findObjects(
                                context,
                                strType,
                                null,
                                null,
                                null,
                                DomainConstants.QUERY_WILDCARD,
                                strWhereExpression,
                                false,
                                busSelects);
    	
    	Iterator fieldsItr = mapBusIds.iterator();
    	Map curField = new HashMap();

    	while(fieldsItr.hasNext()){
    		curField = (Map)fieldsItr.next() ;
    		if(!tempList.contains((String)curField.get(DomainConstants.SELECT_ID))){
    			returnList.add(curField);
			}
		}

		return returnList;

	}
	catch (Exception Ex) {
		Ex.printStackTrace();
		throw Ex;
	}
}
       	
/**
 * This method returns edit accesss for each row .
 *
 * @param context - the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @return StringList containing the Object ids to exclude from teh search
 * @throws Exception if the operation fails
 * @since AEF R418.HF3
 */
   @com.matrixone.apps.framework.ui.ProgramCallable
public static StringList getCellLevelEditAccessforIssueCategory(Context context, String args[])throws Exception
{
	HashMap inputMap = (HashMap)JPO.unpackArgs(args);
	MapList objectMap = (MapList) inputMap.get("objectList");
	
	StringList returnStringList = new StringList (objectMap.size());
	Iterator objectItr = objectMap.iterator();
	while (objectItr.hasNext())
	{
		Map curObjectMap = (Map) objectItr.next();
		String objectType = (String) curObjectMap.get(DomainObject.SELECT_TYPE);
            
		if(objectType.equalsIgnoreCase(DomainConstants.TYPE_ISSUECLASSIFICATION))
		{
			returnStringList.add(Boolean.valueOf(true));
		}else
		{
			returnStringList.add(Boolean.valueOf(false));
		}
	}
	return returnStringList;
}
       	
 
 
/**
 * This method returns expanded rows information.
 *
 * @param context - the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @return MapList containing the expanded Object List
 * @throws Exception if the operation fails
 * @since AEF R418.HF3
 */
 @com.matrixone.apps.framework.ui.ProgramCallable
 public MapList issueCategoryClassificationExpand(Context context, String[] args) throws FrameworkException {
	 MapList classlist = null;
	 MapList objectList = new MapList();
	 try {
		 Map programMap = (Map)JPO.unpackArgs(args);
		 String strObjectId = (String) programMap.get("objectId");
		 
		 StringList busSelects = new StringList(DomainConstants.SELECT_ID);
		 busSelects.add(DomainConstants.SELECT_NAME);
		 
		 StringList relSelects = new StringList(SELECT_RELATIONSHIP_ID);
		 
		 
		 DomainObject domCategory = newInstance(context, strObjectId);
		 String strType = PropertyUtil.getSchemaProperty( context, SYMBOLIC_type_IssueClassification);
		 classlist = domCategory.getRelatedObjects(context, PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_IssueCategoryClassification), strType, busSelects, relSelects, false, true, (short) 1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

		 Iterator fieldsItr = classlist.iterator();
		 Map curField = new HashMap();

		 while(fieldsItr.hasNext()){
			 curField = (Map) fieldsItr.next();
			 curField.put(SELECT_RELATIONSHIP_ID, curField.get(SELECT_RELATIONSHIP_ID));
			 curField.put("hasChildren", "False");
			 
			 objectList.add(curField);
		 }

	 }
	 catch(Exception e)
	 {
		 throw new FrameworkException(e);
	 }	 
	 return objectList;
 }
 
	public Vector getEditIcon(Context context, String[] args) throws Exception {
	   HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
	   MapList mapList = ( MapList )programMap.get( "objectList" );
	   HashMap paramMap = (HashMap) programMap.get("paramList");
	   boolean bPrintMode = false;
	   boolean bExport = false;
	   String reportFormat = (String) paramMap.get("reportFormat");
	   
	   if("ExcelHTML".equals(reportFormat)  || "CSV".equals(reportFormat)) {
		   bExport = true;
	   } else if("HTML".equals(reportFormat) || "true".equalsIgnoreCase((String)paramMap.get("editTableMode"))) {
		   bPrintMode = true;
	   }
	
	   Vector vec = new Vector(mapList.size());
	   for(int i=0; i<mapList.size(); i++) {
		   Map map = (Map)mapList.get(i);
		   String objectId=(String) map.get("id");
		   String modifyAccess=(String) map.get("current.access[modify]");
	    
		   if(bExport ||  "false".equalsIgnoreCase(modifyAccess)) {
			   vec.add("");
		   } else if(bPrintMode) {
			   StringBuffer sbEditIcon=new StringBuffer();
			   sbEditIcon.append("<a href=\"javascript:getTopWindow().showSlideInDialog('");
			   String sHref ="../common/emxForm.jsp?form=type_Issue&mode=Edit&formHeader=emxComponents.Heading.Edit&HelpMarker=emxhelpissueedit&submitAction=refreshCaller&suiteKey=Components&objectId=";
			   sHref = sHref.replace("&", "&amp;");
			   sbEditIcon.append(sHref);
			   sbEditIcon.append(XSSUtil.encodeForJavaScript(context, objectId));
			   sbEditIcon.append("', '570', '520')\"><img border='0' src='../common/images/iconActionEdit.gif'></img></a>");
			   vec.add(sbEditIcon.toString());
		   }
	   }
	   return vec;
	}

}
