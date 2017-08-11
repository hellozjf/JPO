/*
 ** emxPLCCommonBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.22.2.6.1.1.1.2.1.1 Fri Jan 16 14:09:32 2009 GMT ds-shbehera Experimental$
 *  @quickreview KIE1 ZUD 15:06:02 IR-339446-3DEXPERIENCER2016 : Validation Method not defined checkBadNameChars" Warning message is posted in the RSP Edit Page when opened from the Requirement Where Used page.
 *  @quickreview HAT1 ZUD 16:03:02 : HL -  To enable Content column for Test Cases.
 *  @quickreview HAT1 ZUD 16:16:02 : HL -  To enable Content column for Test Cases. Fix field size for Content Data field. 
 *  @quickreview HAT1 ZUD 16:03:05 : Populating title as per autoName of Name in Web form. 
 *  @quickreview HAT1 ZUD 16:26:05 : IR-439295-3DEXPERIENCER2017x:   R419-STP: Test Execution is not displaying "Actual Start Date" till user demote it to "Private" state and then promote it to "Release".
 *  @quickreview HAT1 ZUD 16:07:13 : IR-276718-3DEXPERIENCER2017x: NHIV6R- 041231: Test Case validation status is not updated under Test Execution.
 *  @quickreview KIE1 ZUD 16:07:26 : HL - Parameter Under Test Execution, added changes for Test Execution Preferenceses.. 
 *  @quickreview KIE1 ZUD 16:11:28 : IR-484274-3DEXPERIENCER2017x:   R4190-FUN055836: Unselection of preferences for Test Execution Is KO once it is selected.
 *  @quickreview KIE1 ZUD 16:11:28 : IR-484273-3DEXPERIENCER2017x: R419-FUN055836: Parameter Evaluation Curve is KO with preferences field selection.
 *  @quickreview KIE1 ZUD 17:01:18 : HF-486616-3DEXPERIENCER2017x_FD01: R419-STP: Wrong Valuation Type value getting copied for paramters in Test Execution from Test Cases.

*/

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.ByteArrayOutputStream;

import matrix.db.Attribute;
import matrix.db.AttributeItr;
import matrix.db.AttributeList;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectAttributes;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Person;
import matrix.db.Relationship;
import matrix.util.StringList;

import com.dassault_systemes.enovia.changeaction.constants.ActivitiesOperationConstants;
import com.dassault_systemes.enovia.changeaction.factory.ChangeActionFactory;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeActionServices;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeActionServices.Proposed;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedActivity;
import com.dassault_systemes.enovia.changeaction.interfaces.IRealizedChange;

import com.dassault_systemes.knowledge_itfs.IKweDictionary;
import com.dassault_systemes.knowledge_itfs.IKweList;
import com.dassault_systemes.knowledge_itfs.IKweUnit;
import com.dassault_systemes.knowledge_itfs.IKweValue;
import com.dassault_systemes.knowledge_itfs.IKweValueFactory;
import com.dassault_systemes.knowledge_itfs.KweInterfacesServices;
import com.dassault_systemes.knowledge_itfs.KweTypes;
import com.dassault_systemes.parameter_interfaces.IPlmParameter;
import com.dassault_systemes.parameter_interfaces.IPlmParameter.PLMParm_ValuationType;
import com.dassault_systemes.parameter_interfaces.IPlmParameterDisplay;
import com.dassault_systemes.parameter_interfaces.PLMParm_RangeStatus;
import com.dassault_systemes.parameter_interfaces.PLMParm_ValuationStatus;
import com.dassault_systemes.parameter_interfaces.ParameterInterfacesServices;
import com.dassault_systemes.parameter_interfaces.ParameterTypes;

import com.matrixone.apps.productline.UnifiedAutonamingServices;

//HAT1 ZUD (RMT): To enable content column for Test Case.
import java.io.FileInputStream;

import com.matrixone.jsystem.util.Base64Utils;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkLicenseUtil;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.productline.DerivationUtil;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;


    /**
     * The <code>emxPLCCommonBase</code> class contains common utility methods for Product Central application
     * @author Wipro,Enovia MatrixOne
     * @version ProductCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
     *
     */
    public class emxPLCCommonBase_mxJPO extends emxDomainObject_mxJPO
    {
        /** A string constant with the value objectId. */
        public static final String STR_OBJECT_ID = "objectId";
        /** A string constant with the value objectList. */
        public static final String STR_OBJECT_LIST = "objectList";
        /** A string constant with the value COMMA:",". */
        public static final String STR_COMMA = ",";
        /** A string constant with the value paramList*/
        public static final String STR_DEFAULT_INTERMEDIATE_TYPES =
                         ProductLineConstants.TYPE_GBOM
                            +STR_COMMA+ProductLineConstants.TYPE_FEATURE_LIST;
        /** A string constant with the value paramList*/
        public static final String STR_PARAM_LIST = "paramList";
        /** A string constant with the value  ExpandFilterTypes. */
        public static final String STR_FILTER_TYPE_NAMES = "ExpandFilterTypes";
        /** A string constant with the value  ExpandFilterRelationships. */
        public static final String STR_FILTER_REL_NAMES = "ExpandFilterRelationships";
        /** A string constant with the value  IntermediateFilterTypes. */
        public static final String STR_INTERMEDIATE_FILTER_TYPE_NAMES =
                                                   "IntermediateFilterTypes";
        /** A string constant with the value settings */
        public static final String STR_SETTINGS = "settings";
        /** A string constant with the value "command". */
        public static final String STR_COMMAND = "command";
        /** A string constant with the value "null". */
        public static final String STR_NULL = "null";
        /** A string constant with the value "". */
        public static final String STR_BLANK = "";
        /** A string constant with the value "emxProductLineStringResource". */
        public static final String STR_BUNDLE = "emxProductLineStringResource";
        /** A string constant with the value "relationship". */
        public static final String STR_RELATIONSHIP = "relationship";
        /** A string constant with the value Boolean Compatibility Rule */
        public static final String REL_BCR = ProductLineConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE;
        /** A string constant with the value Left Expression */
        public static final String REL_LE = ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION;
        /** A string constant with the value Right Expression */
        public static final String REL_RE = ProductLineConstants.RELATIONSHIP_RIGHT_EXPRESSION;
        /*Begin of add:Ramandeep,Enovia MatrixOne Bug#300035 4/6/2005*/
        /** A string constant with the value Rule */
        public static final String REL_RULE = ProductLineConstants.RELATIONSHIP_LOCAL_RULE;
        /*end of add:Ramandeep,Enovia MatrixOne Bug#300035 4/6/2005*/
        /** A string constant with the value emxComponentsStringResource. */
        public static final String RESOURCE_BUNDLE_COMPONENTS_STR =
                "emxComponentsStringResource";
        /** A string constant for Higher Revision icon path*/
        public static final String HIGHER_REVISION_ICON =
                "<img src=\"../common/images/iconSmallHigherRevision.gif\" border=\"0\"  align=\"middle\">";
        /** A string constant for New Derivation icon path*/
        public static final String NEW_DERIVATION_ICON =
                "<img src=\"../common/images/iconSmallHigherRevision.gif\" border=\"0\"  align=\"middle\">";
        /** A string constant with the value objectList. */
        public static final String OBJECT_LIST = "objectList";
        /** A string constant with the value objectId. */
        public static final String OBJECT_ID = "objectId";
        /** A string constant for Tool Tip on Higher Revision Icon. */
        public static final String ICON_TOOLTIP_HIGHER_REVISION_EXISTS = "emxProduct.Revision.ToolTipHigherRevExists";
        /** A string constant for Tool Tip on New Derivation Icon. */
        public static final String ICON_TOOLTIP_NEW_DERIVATION_EXISTS = "emxProduct.Revision.ToolTipNewDerivationExists";
        /** A string constant for symbolic name of Relationship Affected Item */
        public static final String SYMBOLIC_relationship_ECAffectedItem = "relationship_ECAffectedItem";
        /** A string constant for symbolic name of Type EC */
        public static final String SYMBOLIC_policy_EngineeringChange = "policy_EngineeringChangeStandard";
        /** A string constant for symbolic name of state of EC */
        public static final String SYMB_state_close = "state_Close";
        /** A string constant for symbolic name of state of EC */
        public static final String SYMB_state_Reject = "state_Reject";
        /** A string constant for symbolic name of state of EC */
        public static final String SYMB_state_Complete = "state_Complete";

        /** A string constant with the value "to[Boolean Compatibility Rule].from.id"*/
        public static final String BCR_FROM_OID = "to["+REL_BCR+"]."+DomainConstants.SELECT_FROM_ID;

//Begin of Add by Enovia MatrixOne for EC Lifecycle Bug on 18-Mar-05
        /** A string constant with the value [. */
        public static final String SYMB_OPEN_BRACKET         = "[";
        /** A string constant with the value ]. */
        public static final String SYMB_CLOSE_BRACKET        = "]";
        /** A string constant with the value ==. */
        public static final String SYMB_EQUAL                = " == ";
        /** A string constant with the value attribute. */
        public static final String SYMB_ATTRIBUTE            = "attribute";
        /** A string constant with the value "'". */
        public static final String SYMB_QUOTE                = "'";
//End of Add by Enovia MatrixOne for EC Lifecycle Bug on 18-Mar-05
//Begin of add by Rashmi, Enovia MatrixOne for bug 301411 Date: 4/13/2005
         /** A string constant with the value " ". */
        public static final String SYMB_SPACE               = " ";
//End of add for bug 301411
//Begin of Add by Vibhu, Enovia MatrixOne for Bug 303269 on 28 April 05
                public static final String SYMB_OR                  ="OR";
//End of Add by Vibhu, Enovia MatrixOne for Bug 303269 on 28 April 05
        public static final String SYMBOLIC_policy_Product = "policy_Product";
        public static final String SYMB_state_Review = "state_Review";
        public static final String SYMB_state_Release = "state_Release";
        public static final String SYMB_state_Obsolete = "state_Obsolete";
        public static final String SUITE_KEY = "ProductLine";
       
        
    /**
     * Create a new emxPLCCommonBase object from a given id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @return a emxPLCCommonBase Object
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
       public emxPLCCommonBase_mxJPO (Context context, String[] args) throws Exception
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
         * @since ProductCentral 10.6
         */
        public int mxMain (Context context, String[] args) throws Exception {
            if (!context.isConnected()) {
                String strContentLabel = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                        "emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
                throw  new Exception(strContentLabel);
            }
            return  0;
        }
    /**
     * Get the list of component objects for the object type passed in to be shown in
     * Structured Navigator expansion.The returned MapList will have component objects
     * of the object type corresponding to the node expanded in the Structure Navigator.
     * This utility method will get invoked by getStructureList method in Product Central type specific
     * JPOs when the Structured Navigator is expanding a node of object type other than the
     * type represented by the JPO.
     * Product Central type JPOs should invoke this method only when they have the method 'getStructureList'.
     * Only Product Central types whose tree menus have Structure Menu setting will invoke this method
     * For e.g. emxProductBase JPO's getStructureList method handles type Product Structure Navigator
     * expansion when a Product object is opened from Product Summary page. In the same Product
     * Structure Navigator, if object node of different type, e.g. Product Configuration is expanded,
     * the getStructureList method in emxProductBase JPO redirects the call to this method. The
     * MapList returned by this method is used to display Product Configuration object expansion in
     * the Structure Navigator.
     * @param context the eMatrix Context object.
     * @param args contains a Map with the following entries:
     *      paramMap   - Map having object Id String
     *      requestMap - Map having request parameter values
     * @return MapList with component objects of type expanded in Structure Navigator
     * @throws Exception if the operation fails
     */

    public static MapList getStructureListForType (Context context, String[] args)
        throws Exception{


        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap)programMap.get("paramMap");

        String objectId    = (String)paramMap.get("objectId");

        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        String objectType         = domainObject.getInfo(context, DomainObject.SELECT_TYPE);
        String objectParentType   = getParentType(context, objectType);

        // based on type of object, invoke getStructureList method of corresponding JPO
        MapList structList        = new MapList();

        try{
            String jpoClass = getJPOForType(context, objectType);

            // if JPO mapping for type does not exist, and top level parent type is different from the
            // type, try getting JPO mapping for the parent type
            if(jpoClass == null && !objectType.equals(objectParentType)){
                jpoClass = getJPOForType(context, objectParentType);
            }

            // if valid JPO obtained from Properties mapping, invoke the JPO
            if(jpoClass != null) {
                //invoke the getStructureList method on Product Central type specific JPO
                structList = (MapList)JPO.invoke(context,           // matrix Context object
                                                 jpoClass,          // the JPO
                                                 null,              // constructor arguments
                                                 "getStructureList",// method to return Structure List
                                                 args,              // args containing param map and object id
                                                 MapList.class);    // return class type
            } else {
                // else return empty list. This happens when no JPO mapping exists for the type and also its top level parent
                structList = new MapList();
            }

        }catch(Exception ex){
            throw new FrameworkException(ex);
        }

        return structList;
    }

    /**
     * This method returns the Product Central JPO corresponding to the PLC admin type passed in.
     * Every top level Product Central admin type has a JPO Base Wrapper pair like emxProduct, emxProductBase for type Products
     * The Product Central  type JPO mapping is retrieved from Properties file
     * e.g. emxProduct is returned for passed in objectType 'Products'
     * @param context     the eMatrix Context object.
     * @param objectType  the Product Central admin type for which JPO program name to be returned
     * @return String     the JPO program name corresponding to passed in Product Central admin type
     * @throws Exception  if the operation fails
     */
    public static String getJPOForType(Context context, String objectType){

        String prcJPOForType  = null;
        boolean bMissRsrcKey  = false;

        try{
            if(objectType != null && objectType.length() > 0){
                StringBuffer prcJPOTypeKey = new StringBuffer("emxProduct.JPO.");
                // lookup symbolic name for this admin type
                String symTypeName = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE,objectType,true);
                prcJPOTypeKey.append(symTypeName);

                // get the corresponding Product Central type JPO from resource bundle
                prcJPOForType = EnoviaResourceBundle.getProperty(context,prcJPOTypeKey.toString());
            }
        } catch(Exception ex){
            // set true if JPO mapping for this type does not exist
            bMissRsrcKey = true;
        }
        // if no properties mapping exist or is empty
        if(bMissRsrcKey || !(prcJPOForType.length() >0)){
            prcJPOForType = null;
        }
        return prcJPOForType;
    }

    /**
     * The method returns the toplevel Parent type for passed in type
     * @param context    the eMatrix Context object.
     * @param type       the admin type for which top level parent is being queried
     * @return String    the top level parent type
     * @throws Exception if the operation fails
     */
     public static String getParentType(Context context, String type)
         throws Exception{
         String result = "";
         if(type != null){
			String strMqlCmd1 = "print type $1 select $2 dump";
			String strkindof = "kindof";
			result = MqlUtil.mqlCommand(context, strMqlCmd1, true,type,strkindof);
         }
         return result.trim();
    }
        /**
         * Get the list of the Parent objects under a context.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args is a string array containing a HashMap which
            in turn holds the following objects:
             programMap - HashMap containing the Object Id
                          and symbolicname of the command which invoked the method
         * @return Object of type MapList containing the parent objects
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public static MapList getWhereUsed(Context context,String[] args)
            throws Exception {
            //Unpacking the arguments
            HashMap programMap = (HashMap)JPO.unpackArgs(args);

            //Obtaining the object id
            String strObjectId = (String)programMap.get(STR_OBJECT_ID);
            String strRelPattern=STR_BLANK;
            String strTypePattern=STR_BLANK;
            String strTypeNamesKey=STR_BLANK;
            String strRelNamesKey=STR_BLANK;
            String strIntermediateTypeNamesKey=STR_BLANK;
            String strIntermediateTypes = STR_BLANK;
            String strCommandName=STR_BLANK;
            String strDisplayRelPattern=STR_BLANK;
            StringBuffer sbTypeNames=new StringBuffer();
            StringBuffer sbRelNames=new StringBuffer();
            StringBuffer sbRelPattern=new StringBuffer();
            StringBuffer sbTypePattern=new StringBuffer();
            StringBuffer sbIntermediateTypeNames=new StringBuffer();


            //Instantiating a Maplist object that will contain the object ids
            List relObjIdList = new MapList();

            //Instantiating a Maplist object that will contain the filtered objects' ids
            List filteredMapList = new MapList();

            //get the symbolic name from the command
            String strCommandSymbolicName = (String)programMap.get(STR_COMMAND);
            //if strCommandSymbolicName is not null get the corresponding command name
            if(!( (strCommandSymbolicName == null) ||
                 (STR_BLANK.equals(strCommandSymbolicName))||
                 (STR_NULL.equals(strCommandSymbolicName))
                 )
              )
            {
              strCommandName = PropertyUtil.getSchemaProperty(context,strCommandSymbolicName);
            }

            /* if strCommandName is not null get the command settings  */
            if(! ( (strCommandName == null) ||
                  (STR_BLANK.equals(strCommandName))||
                  (STR_NULL.equals(strCommandName))
              )  )
            {
                UIMenu menu = new UIMenu();
                HashMap commandMap = menu.getCommand(context,strCommandName);
                HashMap settingsMap =(HashMap)commandMap.get(STR_SETTINGS);
                strTypeNamesKey=(String)settingsMap.get(STR_FILTER_TYPE_NAMES);
                strRelNamesKey = (String)settingsMap.get(STR_FILTER_REL_NAMES);
                strIntermediateTypeNamesKey =
                       (String)settingsMap.get(STR_INTERMEDIATE_FILTER_TYPE_NAMES);
            }
          /* if strTypeNamesKey is not null get the type pattern
             otherwise set it to a wildcard value*/
            if((strTypeNamesKey != null) &&
               (!STR_BLANK.equals(strTypeNamesKey)&&
               (!STR_NULL.equals(strTypeNamesKey))
               ))
             strTypePattern = getPattern(context,
                                         strTypeNamesKey,
                                         DomainConstants.QUERY_WILDCARD);
            else
              strTypePattern=DomainConstants.QUERY_WILDCARD;

            /* if strIntermediateTypeNamesKey is not null get
             the comma separated string containing intermediate type names
             otherwise set it to a Default value*/

            if((strIntermediateTypeNamesKey != null) &&
               (!STR_BLANK.equals(strIntermediateTypeNamesKey)&&
               (!STR_NULL.equals(strIntermediateTypeNamesKey))
               ))
                strIntermediateTypes = getPattern(context,
                                              strIntermediateTypeNamesKey,
                                              STR_DEFAULT_INTERMEDIATE_TYPES);
            else
              strIntermediateTypes = STR_DEFAULT_INTERMEDIATE_TYPES;
            /* if strRelNamesKey is not null get the relationship pattern
             otherwise set it to a wildcard value*/

             if((strRelNamesKey != null) &&
               (!STR_BLANK.equals(strRelNamesKey)&&
               (!STR_NULL.equals(strRelNamesKey))
               ))
            {
             strRelPattern = getPattern(context,
                                       strRelNamesKey,
                                       DomainConstants.QUERY_WILDCARD);
            strDisplayRelPattern =strRelPattern;
            }

            else
              strRelPattern = DomainConstants.QUERY_WILDCARD;

              /*Add the names of relationship for Intermediate type
                to the strRelPattern */
              if(!DomainConstants.QUERY_WILDCARD.equalsIgnoreCase(strRelPattern))
              {
              sbRelPattern.append(strRelPattern);
              sbRelPattern.append(STR_COMMA);
              sbRelPattern.append
                  (getIntermediateRelationships(context,strIntermediateTypes));
              strRelPattern = sbRelPattern.toString();
              }
              if(!DomainConstants.QUERY_WILDCARD.equalsIgnoreCase(strTypePattern))
              {
              sbTypePattern.append(strTypePattern);
              sbTypePattern.append(STR_COMMA);
              sbTypePattern.append(strIntermediateTypes);
              strTypePattern = sbTypePattern.toString();
              }
            //Stringlists containing the objectSelects & relationshipSelects parameters
            StringList ObjectSelectsList = new StringList();
            ObjectSelectsList.add(SELECT_ID);
            ObjectSelectsList.add(SELECT_TYPE);
            ObjectSelectsList.add(SELECT_NAME);
            ObjectSelectsList.add(BCR_FROM_OID);
                        //Begin of add by Vibhu, Enovia MatrixOne for OOC: Issue no.974 on 26 April,05
            ObjectSelectsList.add(SELECT_CURRENT);
                        //End of add by Vibhu, Enovia MatrixOne for OOC: Issue no. 974 on 26 April,05

            StringList RelSelectsList =
                new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            RelSelectsList.add(DomainConstants.SELECT_FROM_ID);
            RelSelectsList.add(DomainConstants.SELECT_TO_ID);


   //Recurse level is set to 1
            short sRecurseLevel = 0;

//Added by Enovia MatrixOne for Performance Bug- Where used, on 23 Jan 06
    strRelPattern = removeDuplicateInString(strRelPattern, STR_COMMA);

    //Instantiating DomainObject
    DomainObject domainObject = newInstance(context,strObjectId);
             relObjIdList = domainObject.getRelatedObjects(context,
                                                          strRelPattern,
                                                          strTypePattern,
                                                          ObjectSelectsList,
                                                          RelSelectsList,
                                                          true,
                                                          false,
                                                          sRecurseLevel,
                                                          DomainConstants.EMPTY_STRING,
                                                          DomainConstants.EMPTY_STRING);


            if (relObjIdList!=null && relObjIdList.size()!=0)

            {
             /* Calling the setLevelsInMapList() method which modifies the maplist
             (relObjIdList) returned by the method getRelatedObjects().It loops
             through the maplist and searches for the intermediate objects ,if it
             finds one it decrement the "level" of the corresponding object(one
             connected through the intermediate object)by 1.

             This manipulation is needed becuase intermediate objects are supposedly
             transparent to the user.*/

            relObjIdList = setLevelsInMapList(relObjIdList,strIntermediateTypes);

            /*Filter the MapList to remove the Intermediate objects */
            relObjIdList = removeIntermediateObjects(relObjIdList,strIntermediateTypes);
            }
           /*The code below fetches the to side objects(till level 1) which are
             connected to the context object through intermediate objects */

            String strInterRel = getIntermediateRelationships(context,strIntermediateTypes);
            Map mpObjSelectMap = (HashMap)getObjectsSelect(strDisplayRelPattern);
            StringList slObjSelList = (StringList)mpObjSelectMap.get("objselects");
            /*Begin of Modify:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
            StringBuffer sbBCRParentIDFromLE = new StringBuffer("to[");
            sbBCRParentIDFromLE.append(REL_LE);
            sbBCRParentIDFromLE.append("].from.to[");
            sbBCRParentIDFromLE.append(REL_BCR);
            sbBCRParentIDFromLE.append("].");
            sbBCRParentIDFromLE.append(DomainConstants.SELECT_FROM_ID);

            StringBuffer sbBCRParentIDFromRE = new StringBuffer("to[");
            sbBCRParentIDFromRE.append(REL_RE);
            sbBCRParentIDFromRE.append("].from.to[");
            sbBCRParentIDFromRE.append(REL_BCR);
            sbBCRParentIDFromRE.append("].");
            sbBCRParentIDFromRE.append(DomainConstants.SELECT_FROM_ID);


            slObjSelList.add(sbBCRParentIDFromLE.toString());
            slObjSelList.add(sbBCRParentIDFromRE.toString());
            Map mpRelName = (HashMap)mpObjSelectMap.get("relName");

            mpRelName.put(sbBCRParentIDFromLE.toString(),REL_LE);
            mpRelName.put(sbBCRParentIDFromRE.toString(),REL_RE);
            //Making another copy of Obj select list so that we keep our original selectlist with us.
            List slObjSelListCopy = new StringList();
            for(int i=0;i<slObjSelList.size();i++){
                slObjSelListCopy.add(slObjSelList.get(i));
                        }

            // Start - Modification for bug 373864
            String strBlockeTypes = EnoviaResourceBundle.getProperty(context,"emxProduct.WhereUsed.Restricted.IntermediateType");
            StringTokenizer strTypeTokenizer = new StringTokenizer(strBlockeTypes,",");
            StringList strRestrictedTypeList = new StringList(strTypeTokenizer.countTokens());

            while(strTypeTokenizer.hasMoreTokens())
            {
            	String strSymbName = (String) strTypeTokenizer.nextToken();
            	strRestrictedTypeList.add(PropertyUtil.getSchemaProperty(context,strSymbName.trim()));
            }

            MapList mlToSideObjsList = new MapList();
            String strContextObjType = domainObject.getInfo(context, ProductLineConstants.SELECT_TYPE);

            if(!strRestrictedTypeList.contains(strContextObjType))
            {
                /*Calling the getRelatedObjects() method of DomainObject
                which Returns the Maplist of Object Ids */
            	// Added if condition with logic, call domainObject.getRelatedObjects when strInterRel is not blank/null for IR-155574V6R2013 
            	if(!(strInterRel==null || strInterRel.equalsIgnoreCase("null") || strInterRel.equals("")))
            	{
                mlToSideObjsList = domainObject.getRelatedObjects(context,
                                                              strInterRel,
                                                              strTypePattern,
                                                              (StringList)slObjSelListCopy,
                                                              RelSelectsList,
                                                              false,
                                                              true,
                                                              (short)1,
                                                              DomainConstants.EMPTY_STRING,
                                                              DomainConstants.EMPTY_STRING);
            	}
            }
            // End - Modification for bug 373864

            Map mpToSideObj = new HashMap();
            Map mpToCopy = new HashMap();
            StringList slObjIdList = new StringList();
            Object objBCRParentIDFromLE = STR_BLANK;
            Object objBCRParentIDFromRE = STR_BLANK;
            for(int i=0;i<mlToSideObjsList.size();i++)
            {
                    mpToSideObj = (Map)mlToSideObjsList.get(i);
                    for(int j=0;j<slObjSelList.size();j++)
                    {
                        if (
                            ((String)slObjSelList.get(j)).equalsIgnoreCase(sbBCRParentIDFromLE.toString())||
                            ((String)slObjSelList.get(j)).equalsIgnoreCase(sbBCRParentIDFromRE.toString())
                            )
                                {
                                 continue;
                                }
                        Object objIdList = (Object)mpToSideObj.get(slObjSelList.get(j));
                        if(objIdList != null)
                        {
                           if(objIdList instanceof List)
                            {
                             slObjIdList = (StringList)objIdList;
                            }
                           else if(objIdList instanceof String)
                            {
                             slObjIdList.addElement((String)objIdList);
                            }
                            for(int k = 0;k < slObjIdList.size();k++)
                            {
                                mpToCopy = new HashMap();
                                //create a map and copy it to the maplist to be returned
                                mpToCopy.put(SELECT_ID,slObjIdList.get(k));
                                mpToCopy.put(DomainConstants.SELECT_FROM_ID,slObjIdList.get(k));
                                mpToCopy.put(DomainConstants.SELECT_TO_ID,strObjectId);
                                mpToCopy.put(DomainConstants.KEY_LEVEL,(String)mpToSideObj.get(DomainConstants.KEY_LEVEL));
                                mpToCopy.put(STR_RELATIONSHIP,(String)mpRelName.get(slObjSelList.get(j)));

                                /*Begin of add:Vibhu,Enovia MatrixOne Bug#300051 3/29/2005*/
                                DomainObject objTemp = DomainObject.newInstance(context,(String)slObjIdList.get(k));
                                String strType = objTemp.getInfo(context,DomainConstants.SELECT_TYPE);
                                mpToCopy.put(SELECT_TYPE,strType);
                                
                                String strName = objTemp.getInfo(context, DomainConstants.SELECT_NAME);
                                mpToCopy.put(SELECT_NAME, strName);
                                
                                /*End of add:Vibhu,Enovia MatrixOne Bug#300051 3/29/2005*/

                                                                //Begin of add by Vibhu, Enovia MatrixOne for Issue no.974 on 26 April,05
                                String strStateCurrent = objTemp.getInfo(context,SELECT_CURRENT);
                                                                mpToCopy.put(SELECT_CURRENT,strStateCurrent);
                                                                //End of add by Vibhu, Enovia MatrixOne for Issue no. 974 on 26 April,05

                                objBCRParentIDFromLE = (Object)mpToSideObj.get(sbBCRParentIDFromLE.toString());
                                 if (objBCRParentIDFromLE!=null)
                                  {

                                      if(objBCRParentIDFromLE instanceof List)
                                        {
                                         mpToCopy.put(BCR_FROM_OID,((StringList)objBCRParentIDFromLE).get(0));
                                        }
                                       else if(objBCRParentIDFromLE instanceof String)


                                        {
                                         mpToCopy.put(BCR_FROM_OID,(String)objBCRParentIDFromLE);
                                        }
                                  }

                                objBCRParentIDFromRE = (Object)mpToSideObj.get(sbBCRParentIDFromRE.toString());
                                  if (objBCRParentIDFromRE!=null)
                                  {

                                      if(objBCRParentIDFromRE instanceof List)
                                        {
                                         mpToCopy.put(BCR_FROM_OID,((StringList)objBCRParentIDFromRE).get(0));
                                        }
                                       else if(objBCRParentIDFromRE instanceof String)


                                        {
                                         mpToCopy.put(BCR_FROM_OID,(String)objBCRParentIDFromRE);
                                        }
                         /*End of Modify:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
                                  }
                                  relObjIdList.add(mpToCopy);
                             }//end for
                        }//end if
                    }//end for
            }//end for
        if (relObjIdList!=null && relObjIdList.size()!=0)
        {
            /*Filter the MapList to remove the Duplicate objects */
             relObjIdList = removeDuplicateObjects(context,relObjIdList);
        }
	Iterator objectListItr = relObjIdList.iterator();
        Map objectMap = new HashMap();
        //loop through all the records
        while(objectListItr.hasNext())
        {
            objectMap = (Map) objectListItr.next();
             String strLevel = (String)objectMap.get(DomainConstants.KEY_LEVEL);
             objectMap.put("sLevel", strLevel);
        } //End of while loop
 return  (MapList)relObjIdList ;
}//End of the method

        /**
         * This method creates the HTML to display the Edit Icon
         * @param context the eMatrix <code>Context</code> object
         * @param args - Holds the parameters passed from the calling method
         * @return Vector
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
    public static Vector getEditIcon(Context context,String[] args) throws Exception
        {
    	        //XSSOK- Deprecated
                /*Begin of add:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
                StringBuffer sbRuleTargetId = new StringBuffer("from[");
                sbRuleTargetId.append(REL_LE);
                sbRuleTargetId.append("].to.from[");
                sbRuleTargetId.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
                sbRuleTargetId.append("].");
                sbRuleTargetId.append(DomainConstants.SELECT_ID);

                StringBuffer sbRuleLERelId = new StringBuffer("from[");
                sbRuleLERelId.append(REL_LE);
                sbRuleLERelId.append("].to.from[");
                sbRuleLERelId.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
                sbRuleLERelId.append("].");
                sbRuleLERelId.append(DomainConstants.SELECT_TO_ID);

                StringBuffer sbIRProductId = new StringBuffer("to[");
                sbIRProductId.append(REL_RULE);
                sbIRProductId.append("].");
                sbIRProductId.append(DomainConstants.SELECT_FROM_ID);

                StringBuffer sbIRParentId = new StringBuffer("from[");
                sbIRParentId.append(REL_LE);
                sbIRParentId.append("].to.to[");
                sbIRParentId.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM);
                sbIRParentId.append("].");
                sbIRParentId.append(DomainConstants.SELECT_FROM_ID);

                StringBuffer sbGBOMRuleLERelId = new StringBuffer("from[");
                sbGBOMRuleLERelId.append(REL_LE);
                sbGBOMRuleLERelId.append("].to.from[");
                sbGBOMRuleLERelId.append(ProductLineConstants.RELATIONSHIP_GBOM_TO);
                sbGBOMRuleLERelId.append("].");
                sbGBOMRuleLERelId.append(DomainConstants.SELECT_TO_ID);

                StringBuffer sbGBOMIRParentId = new StringBuffer("from[");
                sbGBOMIRParentId.append(REL_LE);
                sbGBOMIRParentId.append("].to.to[");
                sbGBOMIRParentId.append(ProductLineConstants.RELATIONSHIP_GBOM_FROM);
                sbGBOMIRParentId.append("].");
                sbGBOMIRParentId.append(DomainConstants.SELECT_FROM_ID);

                StringBuffer sbGBOMRuleTargetId = new StringBuffer("from[");
                sbGBOMRuleTargetId.append(REL_LE);
                sbGBOMRuleTargetId.append("].to.from[");
                sbGBOMRuleTargetId.append(ProductLineConstants.RELATIONSHIP_GBOM_TO);
                sbGBOMRuleTargetId.append("].");
                sbGBOMRuleTargetId.append(DomainConstants.SELECT_ID);

                /*end of add:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/

                Vector editIconVector = new Vector();
                Map programMap = (Map) JPO.unpackArgs(args);
                List objectList = (MapList)programMap.get(STR_OBJECT_LIST);
                Map paramMap = (Map) programMap.get(STR_PARAM_LIST);

                //strReportFormat indicates whether method is called from table or report
                String strReportFormat=(String)paramMap.get("reportFormat");
                Iterator objectListItr = objectList.iterator();
                String strType = STR_BLANK;
                String strBaseType = STR_BLANK;
                String strSymbolicTypeName = STR_BLANK;
                String strId = STR_BLANK;
                String strRelId = STR_BLANK;
                /*Begin of Modify:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
                String strBCRParentId = STR_BLANK;
                /*End of Modify:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
                String strWebForm = STR_BLANK;
                String strFormName = STR_BLANK;
                String strCommand = STR_BLANK;
                String strImageSource = "<img border='0' src='../common/images/iconActionEdit.gif'>";
                String strIssueType = PropertyUtil.getSchemaProperty
                                             (context,"type_Issue");
                String strTypeBCR = PropertyUtil.getSchemaProperty
                                             (context,"type_BooleanCompatibilityRule");
                String strTypePCR = PropertyUtil.getSchemaProperty
                                             (context,"type_ProductCompatibilityRule");
                String strTypeInclusionRule = PropertyUtil.getSchemaProperty
                                              (context,"type_InclusionRule");
                String strTypePart = PropertyUtil.getSchemaProperty
                                             (context,"type_Part");
                String strHrefPart = "../components/emxCommonFS.jsp?functionality=PartEditFSInstance&PRCFSParam1=Part&suiteKey=ProductLine";
                String strHrefFeatureBCR = "../components/emxCommonFS.jsp?functionality=BooleanCompatibilityEditFSInstance&PRCFSParam1=BooleanCompatibilityRule&suiteKey=Configuration&emxSuiteDirectory=configuration";
                String strHrefProductBCR = "../components/emxCommonFS.jsp?functionality=ProductCompatibilityEditFSInstance&PRCFSParam1=ProductCompatibilityRule&suiteKey=Configuration&emxSuiteDirectory=configuration";
                //Begin of modify by Enovia MatrixOne for Inc Rule tracking bug dated 29-Apr-2005
                String strHrefInclusionRule = "../components/emxCommonFS.jsp?functionality=InclusionRuleEditFSInstance&PRCFSParam1=InclusionRule&contextPage=WhereUsed&suiteKey=Configuration&emxSuiteDirectory=configuration";
                //End of modify by Enovia MatrixOne for Inc Rule tracking bug dated 29-Apr-2005
                String strHrefWebForm = "../common/emxForm.jsp?mode=Edit&editLink=false";
                String strHrefDefault = "../common/emxDynamicAttributes.jsp";
                StringBuffer sbHref = new StringBuffer();
                StringBuffer sbEditIcon = new StringBuffer();
                Map objectMap = new HashMap();
                String strFeatureObjId = STR_BLANK;
                String strGBOMObjId = STR_BLANK;
                String strFeatureIRParentId = STR_BLANK;
                String strGBOMIRParentId = STR_BLANK;
                String strFeatureRuleTargetId = STR_BLANK;
                String strGBOMRuleTargetId = STR_BLANK;

                //loop through all the records
                while(objectListItr.hasNext())
                {
                    //Clear the buffers
                    sbHref.delete(0,sbHref.length());
                    sbEditIcon.delete(0,sbEditIcon.length());
                    objectMap = (Map) objectListItr.next();
                    strId = (String) objectMap.get(SELECT_ID);
                    strBCRParentId = (String) objectMap.get(BCR_FROM_OID);
                    strRelId = (String) objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    DomainObject dObj = new DomainObject(strId);
                    strType = (String) dObj.getInfo(context,SELECT_TYPE);
                    //get the top most parent of current type
                    strBaseType = FrameworkUtil.getBaseType
                                                      (context,strType,
                                                       context.getVault());

                    /*checks whether the context user has remove access for
                      each of the selected */
                    if (FrameworkUtil.hasAccess(context, dObj, "modify"))
                    {
                      //if called from normal mode(not report)
                      if(!(strReportFormat!=null&&
                         strReportFormat.equals("null")==false&&
                          strReportFormat.equals("")==false))
                        {
                              if (strType.equalsIgnoreCase(strTypeBCR)||
                                  strType.equalsIgnoreCase(strTypePCR))
                              {
                             /*Begin of Modify:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
                                   if (strBCRParentId==null||
                                       strBCRParentId.equalsIgnoreCase(STR_BLANK)||
                                       strBCRParentId.equalsIgnoreCase(STR_NULL))
                             /*End of Modify:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
                                      {
                                         sbHref.append(strHrefProductBCR);
                                      }
                                   else
                                      {
                                         sbHref.append(strHrefFeatureBCR);
                                      }
                                  sbHref.append("&objectId=");
                                  sbHref.append(strId);
                                  sbHref.append("&parentOID=");
                                  sbHref.append(strBCRParentId);
                              }
                              else if (strType.equalsIgnoreCase(strTypeInclusionRule))
                              {
                               /*Begin of Add:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
                               DomainObject domRule = DomainObject.newInstance(context,strId);
                               List lstRuleSelects = new StringList();
                               lstRuleSelects.add(sbRuleTargetId.toString());
                               lstRuleSelects.add(sbRuleLERelId.toString());
                               lstRuleSelects.add(sbIRProductId.toString());
                               lstRuleSelects.add(sbIRParentId.toString());
                               lstRuleSelects.add(sbGBOMRuleLERelId.toString());
                               lstRuleSelects.add(sbGBOMIRParentId.toString());
                               lstRuleSelects.add(sbGBOMRuleTargetId.toString());

                               Map mapRule = domRule.getInfo(context,(StringList)lstRuleSelects);
                               sbHref.delete(0,sbHref.length());
                               sbHref.append(strHrefInclusionRule);

                               strFeatureObjId = (String)mapRule.get(sbRuleLERelId.toString());
                               strGBOMObjId = (String)mapRule.get(sbGBOMRuleLERelId.toString());
                               if (strFeatureObjId!=null&&!strFeatureObjId.equals(STR_BLANK)
                                   &&!strFeatureObjId.equalsIgnoreCase(STR_NULL)){
                                   sbHref.append("&objectId=");
                                   sbHref.append(strFeatureObjId);
                                }else if (strGBOMObjId!=null&&!strGBOMObjId.equals(STR_BLANK)
                                   &&!strGBOMObjId.equalsIgnoreCase(STR_NULL)){
                                   sbHref.append("&objectId=");
                                   sbHref.append(strGBOMObjId);
                                }

                               strFeatureIRParentId= (String)mapRule.get(sbIRParentId.toString());
                               strGBOMIRParentId= (String)mapRule.get(sbGBOMIRParentId.toString());
                               if (strFeatureIRParentId!=null&&!strFeatureIRParentId.equals(STR_BLANK)
                                   &&!strFeatureIRParentId.equalsIgnoreCase(STR_NULL)){
                                   sbHref.append("&parentOID=");
                                   sbHref.append(strFeatureIRParentId);
                                }else if (strGBOMIRParentId!=null&&!strGBOMIRParentId.equals(STR_BLANK)
                                   &&!strGBOMIRParentId.equalsIgnoreCase(STR_NULL)){
                                   sbHref.append("&parentOID=");
                                   sbHref.append(strGBOMIRParentId);
                                }

                               strFeatureRuleTargetId = (String)mapRule.get(sbRuleTargetId.toString());
                               strGBOMRuleTargetId = (String)mapRule.get(sbGBOMRuleTargetId.toString());
                               if (strFeatureRuleTargetId!=null&&!strFeatureRuleTargetId.equals(STR_BLANK)
                                   &&!strFeatureRuleTargetId.equalsIgnoreCase(STR_NULL)){
                                   sbHref.append("&relId=");
                                   sbHref.append(strFeatureRuleTargetId);
                                }else if (strGBOMRuleTargetId!=null&&!strGBOMRuleTargetId.equals(STR_BLANK)
                                   &&!strGBOMRuleTargetId.equalsIgnoreCase(STR_NULL)){
                                   sbHref.append("&relId=");
                                   sbHref.append(strGBOMRuleTargetId);
                                }

                               sbHref.append("&productID=");
                               sbHref.append((String)mapRule.get(sbIRProductId.toString()));

                              /*End of add:Ramandeep,Enovia MatrixOne Bug#300035 3/11/2005*/
                              }
                              else if (strBaseType.equalsIgnoreCase(strTypePart))
                              {
                               sbHref.delete(0,sbHref.length());
                               sbHref.append(strHrefPart);
                               sbHref.append("&relId=");
                               sbHref.append(strRelId);
                               sbHref.append("&objectId=");
                               sbHref.append(strId);
                               sbHref.append("&parentOID=");
                               sbHref.append(strId);
                              }
                              else{

                              //get the symbolic name for the BaseType
                              strSymbolicTypeName = FrameworkUtil.getAliasForAdmin
                                                                   (context,SELECT_TYPE,
                                                                    strBaseType,true);

                             /*Here it is assumed that name of the webform for a type
                               is same as type's symbolic name eg type_Products */

                                 //check whether there is an associated webform for this type

                               strCommand = "list form \"" + strSymbolicTypeName + "\" ";
                               strFormName = MqlUtil.mqlCommand(context, strCommand);

                               if (strFormName!=null&&!strFormName.equalsIgnoreCase(STR_BLANK)
                                   &&!strFormName.equalsIgnoreCase(STR_NULL))
                                      {
                                       sbHref.delete(0,sbHref.length());
                                       sbHref.append(strHrefWebForm);
									   // Fix For IR-339446-3DEXPERIENCER2016
                                       if(strType.equals(strIssueType)|| strFormName.equals("type_DOCUMENTS"))
                                          {
                                           sbHref.append("&formHeader=emxComponents.Heading.Edit");
                                           sbHref.append("&suiteKey=Components");
                                          }
                                       else
                                          {
                                          sbHref.append("&formHeader=emxProduct.Heading.Edit");
                                          sbHref.append("&suiteKey=ProductLine");
                                          }
                                       sbHref.append("&form=");
                                       sbHref.append(strSymbolicTypeName);
                                       sbHref.append("&objectId=");
                                       sbHref.append(strId);
                                       }
                                 //otherwise re-direct to default jsp
                                 else {
                                       sbHref.delete(0,sbHref.length());
                                       sbHref.append(strHrefDefault);
                                       sbHref.append("?objectId=");
                                       sbHref.append(strId);
                                      }
                              }

                              sbEditIcon.append("<a href=\"javascript:showModalDialog('");
                              String sHref = sbHref.toString();
                              sHref = sHref.replace("&", "&amp;");
                              sbEditIcon.append(sHref);
                              sbEditIcon.append("', '570', '520')\"><img border='0' src='../common/images/iconActionEdit.gif'></img></a>");
                              editIconVector.add(sbEditIcon.toString());
                              //clear the buffer
                              sbEditIcon.delete(0,sbEditIcon.length());
                        }
                       else//if called from report show only the image
                        {
                         editIconVector.add(strImageSource);
                        }

                  }//end if
                  else//if the user does not have modify access
                    {
                      editIconVector.add(STR_BLANK);
                    }
                } //End of while loop
                return editIconVector;

        } //End of the method

       /**
         * This method is used to get the level for all the objects
           which are associated with context object.
         * @param context the eMatrix <code>Context</code> object
         * @param args - Holds the parameters passed from the calling method
            When this array is unpacked, arguments corresponding to the following
            String keys are found:-
            objectList- MapList Containing the objectIds.
         * @return Vector
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/

    public static Vector getLevel(Context context,String[] args) throws Exception
        {
            Vector levelVector = new Vector();

                Map programMap = (Map) JPO.unpackArgs(args);
                List objectList = (MapList)programMap.get(STR_OBJECT_LIST);
                Iterator objectListItr = objectList.iterator();
                String strLevel = STR_BLANK;
                String sLevel = STR_BLANK;
                Map objectMap = new HashMap();
                //loop through all the records
                while(objectListItr.hasNext())
                {
                    objectMap = (Map) objectListItr.next();
                    strLevel = (String)objectMap.get(DomainConstants.KEY_LEVEL);
                    sLevel=(String)objectMap.get("sLevel");
                    if(UIUtil.isNotNullAndNotEmpty(sLevel))
					 strLevel = sLevel;
                    //XSSOK
                    levelVector.add(strLevel);
                } //End of while loop

                return levelVector;

        } //End of the method


        /**
         * This method is used to get the string of admin names
           from a property file corresponding to the key passed.
         * @param context the eMatrix <code>Context</code> object
         * @param String strKey - The key to be Searched
         * @param String strDefault - Value to be returned if key is missing
         * @return String
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
    public static String getPattern(Context context,String strKey,String strDefault)
        throws Exception
        {
         StringBuffer sbTypeNames=new StringBuffer(30);
         String strPattern = STR_BLANK;
         String strSymbolicNames = STR_BLANK;

         try{
             strSymbolicNames = EnoviaResourceBundle.getProperty(context,strKey);
             }
           catch(Exception e)
             {
             strSymbolicNames = strKey;
             }
         if ((strSymbolicNames != null)&&
               (!STR_BLANK.equals(strSymbolicNames)&&
               (!STR_NULL.equals(strSymbolicNames))
               )
              )
            strPattern =
                 getActualNamesFromSymbolicNames(context,strSymbolicNames);

         if ((strPattern == null) ||
               (STR_BLANK.equals(strPattern)||
               (STR_NULL.equals(strPattern))
               )
              )
            strPattern = strDefault;
           return strPattern;

        }//End of the method

        /**
         * This method is used to get a comma separated string of the
           actual names for admin objects.
         * @param context the eMatrix <code>Context</code> object
         * @param String strSymbolicNamesList - a comma separated string of
            symbolic names
         * @return String
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
    public static String getActualNamesFromSymbolicNames(Context context,
                                                     String strSymbolicNamesList)
        throws Exception
        {
            int i=0;
            String strActualNames = STR_BLANK;
            String strToken = STR_BLANK;
            StringBuffer sbActualNames = new StringBuffer();
            StringTokenizer stActualNames =
            new StringTokenizer(strSymbolicNamesList,STR_COMMA);
            while(stActualNames.hasMoreTokens())
             {

                    strToken = PropertyUtil.getSchemaProperty(context,
                                                   stActualNames.nextToken());
                    if(i>0)
                    sbActualNames.append(STR_COMMA);
                    if(strToken!=null)
                       {
                        sbActualNames.append(strToken);
                        i++;
                       }
             }//end while loop
            strActualNames = sbActualNames.toString();

          return strActualNames;

        }//End of the method

        /**
         * This method is used to get the string of Relationship
           for Intermediate types in the database.
         * @param context the eMatrix <code>Context</code> object
         * @param String strIntermediateTypePattern
         * @return String
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
    public static String getIntermediateRelationships(Context context,
                                                  String strIntermediateTypePattern)
        throws Exception
        {
         int i = 0;
         StringBuffer sbIntermediateRelPattern = new StringBuffer();
         StringTokenizer stIntermediateTypePattern =
               new StringTokenizer(strIntermediateTypePattern,STR_COMMA);
         String strIntermediateRelPattern = STR_BLANK;
         String strCurrentToken = STR_BLANK;
         List lstIntermediateRelationshipList = new StringList();

            while(stIntermediateTypePattern.hasMoreTokens())
            {
                strCurrentToken = stIntermediateTypePattern.nextToken();

                //create a new BusinessType object
                BusinessType busType =
                   new BusinessType(strCurrentToken,context.getVault());

                /*calling the method getRelationshipTypes(of class BusinessType)
                  to get the list of all Relationship for the type*/
                lstIntermediateRelationshipList =
                             busType.getRelationshipTypes(context,true,true,false);
                //get the string from the list and append to stringbuffer
                if(i > 0)
                sbIntermediateRelPattern.append(STR_COMMA);
                sbIntermediateRelPattern.append
                              (getStringFromList(lstIntermediateRelationshipList));
                i++;
            }//end while loop

        return sbIntermediateRelPattern.toString();
       }//End Method

        /**
         * This method is used to get the string from a List
         * @param List lstToBeConverted - list to be converted
         * @return String
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
    public static String getStringFromList(List lstToBeConverted)
        throws Exception
        {

         StringBuffer sbConverted = new StringBuffer();
         for (int i = 0;i < lstToBeConverted.size() ;i++ )
         {
          if(i>0)
          sbConverted.append(STR_COMMA);
          sbConverted.append(lstToBeConverted.get(i));
         }
        return sbConverted.toString();
        }//End Method

        /**
         * This method is used check whether a Particular exists within
           a string.
         * @param String strParent - The source string
         * @param String strPattern -Pattern to be searched
         * @return boolean
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
    public static boolean boolContains(String strParent,String strPattern)
        throws Exception
        {
            boolean bContains = false;
            String strCurrentToken = STR_BLANK;
            StringTokenizer stParent =
            new StringTokenizer(strParent,STR_COMMA);
            while(stParent.hasMoreTokens())
            {

                strCurrentToken = stParent.nextToken();
                if (strCurrentToken.equals(strPattern))
                {
                 bContains = true;
                 break;
                }
            }//end while loop
            return bContains;


        }//End of method

        /**
         * This method is used to set the level for all the objects in a
           MapList as seen by the user by ignoring the Intermediate Objects.
         * @param List relObjIdList - contains the parent MapList
         * @param String strIntermediateTypes - comma seperated string of
           intermediate types
         * @return List- The modified MapList
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
     public static List setLevelsInMapList(List relObjIdList,
                                          String strIntermediateTypes)
         throws Exception
         {

             /* This processing is dependent upon the structure of the MapList
                returned by the method getRelatedObjects() which searches for
                the connected objects in a depth first manner. */

              int iLevelCurrent = 0;
              int iLevelNext = 0;
              String strType = STR_BLANK;
              String strLevel = STR_BLANK;
              Map mpMapCurrent= null;
              Map mpMapNext= null;
              //Loop through the whole Maplist
                for (int index = 0;index<relObjIdList.size();index++ )
                 {
                    mpMapCurrent = (Map)relObjIdList.get(index);
                    strType=(String)mpMapCurrent.get(SELECT_TYPE);
                    iLevelCurrent = Integer.parseInt(
                                      (String)mpMapCurrent.get(DomainConstants.KEY_LEVEL));

                     /* if an intermediate object is encountered loop through the maplist
                       and decrement by 1 ,the level in all the Maps having level
                       greater than it */
                    if (boolContains(strIntermediateTypes,strType))
                    {
                        for (int i = index ;i < relObjIdList.size()-1;i++ )
                        {


                          mpMapNext = (Map)relObjIdList.get(i+1);
                          iLevelNext = Integer.parseInt((String)mpMapNext.get
                                                             (DomainConstants.KEY_LEVEL));


                          if (iLevelNext > iLevelCurrent)
                           {
                                iLevelNext--;
                                Integer intObjLevel=new Integer(iLevelNext);
                                strLevel = intObjLevel.toString();
                                mpMapNext.put(DomainConstants.KEY_LEVEL,strLevel);
                           }//end inner if
                          else
                            break;
                        }//end inner for loop
                    }//end outer if
                 }//end outermost for loop
         return relObjIdList;
         }//end of the method

        /**
         * This method is used to remove the Intermediate objects
           from the MapList.
         * @param List objMapList - contains the MapList
         * @param String strIntermediateTypes - comma seperated string of
           intermediate types
         * @return List- The modified MapList
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
     public static List removeIntermediateObjects(List objMapList,
                                                 String strIntermediateTypes)
         throws Exception
         {
                 Map mpMapCurrent = null;
                 String strType = STR_BLANK;
                 for (int i = 0;i < objMapList.size();i++ )
                 {
                    mpMapCurrent = (Map)objMapList.get(i);
                    strType=(String)mpMapCurrent.get(SELECT_TYPE);
                    if (boolContains(strIntermediateTypes,strType))
                       {
                        objMapList.remove(mpMapCurrent);
                        i--;
                       }
                 }
            return objMapList;
          }//end of the method

        /**
         * This method forms a Map containing stringlist "objectselects"
           based on Display Relationships and a Map "relName" containing
           corresponding objectselects as key and Relationship name as value
         * @param String strDisplayRelPattern - display Relationships
         * @return Map
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
     public static  Map getObjectsSelect(String strDisplayRelPattern)
       throws Exception
         {

         List slRelSelect = new StringList();
         Map tmpmap = new HashMap();
         StringBuffer sbTmp = new StringBuffer();
         StringTokenizer stRelPattern= new StringTokenizer
                                         (strDisplayRelPattern,STR_COMMA);
         String strNextToken = "";
         while(stRelPattern.hasMoreTokens())
             {

              strNextToken = stRelPattern.nextToken();
              sbTmp.append("to[");
              sbTmp.append(strNextToken);
              sbTmp.append("].");
              sbTmp.append(DomainConstants.SELECT_FROM_ID);

              slRelSelect.add(sbTmp.toString());
              tmpmap.put(sbTmp.toString(),strNextToken);
              sbTmp.delete(0,sbTmp.length());
             }
             Map returnMap = new HashMap();
             returnMap.put("objselects",slRelSelect);
             returnMap.put("relName",tmpmap);

     return returnMap;
         }
        /**
         * This method is used to remove the duplicate objects
           from the MapList.
         * @param context the eMatrix <code>Context</code> object
         * @param List objMapList - contains the MapList
         * @return List- The modified MapList
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         **/
     public static List removeDuplicateObjects(Context context,List objMapList)
         throws Exception
         {
                 List filteredMapList = new MapList(objMapList);
                 Map mpMapCurrent = null;
                 Map mpMapNext = null;
                 int iLevelCurrent = 0;
                 int iLevelNext = 0;
                 int iNoOfOccurence = 0;
                 String strRelNameCurrent = STR_BLANK;
                 String strRelNameNext = STR_BLANK;
                 String strFromIdCurrent = STR_BLANK;
                 String strFromIdNext = STR_BLANK;
                 String strToIdCurrent = STR_BLANK;
                 String strToIdNext = STR_BLANK;
                 /*Begin of add:Vibhu,Enovia MatrixOne Bug#300051 3/29/2005*/
                 String strTypeNext = STR_BLANK;
                 String strTypeCurrent = STR_BLANK;
                 String strNameCurrent = STR_BLANK;
                 String strNameNext = STR_BLANK;
                 /*End of add:Vibhu,Enovia MatrixOne Bug#300051 3/29/2005*/

                 for (int i = 0;i < objMapList.size();i++ )
                 {
                    iNoOfOccurence = 0;

                    mpMapCurrent = (Map)objMapList.get(i);
                    strRelNameCurrent = (String)mpMapCurrent.get(STR_RELATIONSHIP);
                    strFromIdCurrent = (String)mpMapCurrent.get(DomainConstants.SELECT_FROM_ID);
                    strToIdCurrent = (String)mpMapCurrent.get(DomainConstants.SELECT_TO_ID);
                    strTypeCurrent = (String)mpMapCurrent.get(SELECT_TYPE);
                    strNameCurrent =(String)mpMapCurrent.get(DomainConstants.SELECT_NAME);

            for (int j = i+1;j < objMapList.size();j++ )
            {

                        mpMapNext = (Map)objMapList.get(j);
                        strRelNameNext = (String)mpMapNext.get(STR_RELATIONSHIP);
                        strFromIdNext = (String)mpMapNext.get(DomainConstants.SELECT_FROM_ID);
                        strToIdNext = (String)mpMapNext.get(DomainConstants.SELECT_TO_ID);
                        /*Begin of add:Vibhu,Enovia MatrixOne Bug#300051 3/29/2005*/
                        strTypeNext = (String)mpMapNext.get(SELECT_TYPE);
                        strNameNext =(String)mpMapNext.get(DomainConstants.SELECT_NAME);
                        /*End of add:Vibhu,Enovia MatrixOne Bug#300051 3/29/2005*/

                         /*Remove duplicate objects if from
                           side object ,to side object is same and one of the
                           following condition is met:
                           1.relationship is same
                           2.relationship is LE or RE
                           The second condition takes care of the scenerio when
                           a Feature is used in rule in left exp as well as
                           Right exp.
                           */

                 if ( strFromIdNext.equalsIgnoreCase(strFromIdCurrent)
                     &&
                      strTypeNext.equalsIgnoreCase(strTypeCurrent) && strNameCurrent.equals(strNameNext))
                            {
                                    filteredMapList.remove(mpMapNext);

                                }//end if


                     }//End of the outer for loop

               }
            return filteredMapList;

          }//End of the method



/**
    * Method retrieves All revisions of the Context Object
    * @param context the eMatrix <code>Context</code> object
    * @param args holds arguments
    * @return MapList - returns the Maplist of revisions
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllRevisions(Context context, String[] args) throws Exception{
    MapList relBusObjList = null;
    Map programMap = (HashMap) JPO.unpackArgs(args);
    String strObjectId = (String)  programMap.get(OBJECT_ID);
    setId(strObjectId);
    
    DomainObject domObj = DomainObject.newInstance(context, strObjectId);
    
    StringList busSelects = new StringList(2);
    busSelects.add(DomainObject.SELECT_ID);
    busSelects.add("physicalid");
	 boolean isMajor = ProductLineCommon.isMajorPolicy(context, domObj);

	 if(isMajor)
	 {
		 // Major
		 relBusObjList = ProductLineCommon.getMajorRevisionsInfo(context,busSelects,new StringList(0), strObjectId);
	 }
	 else
	 {
		 // Minor
		 DomainObject busObj   = DomainObject.newInstance(context, strObjectId);
		 relBusObjList = busObj.getRevisionsInfo(context,busSelects,new StringList(0));
	 }
    return relBusObjList;
    }

   /**
    * Method shows higher revision Icon if a higher revision of the object exists
    * @param context the eMatrix <code>Context</code> object
    * @param args holds arguments
    * @return List - returns the program HTML output
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    public List getHigherRevisionIcon(Context context, String[] args) throws Exception{

	    Map programMap = (HashMap) JPO.unpackArgs(args);
	    MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
	    Map paramList = (HashMap)programMap.get("paramList");
	    String reportFormat = (String)paramList.get("reportFormat");
	
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
        EnoviaResourceBundle.getProperty(context, SUITE_KEY,ICON_TOOLTIP_HIGHER_REVISION_EXISTS,context.getSession().getLanguage());

        String strHigherRevisionIconTag= "";
        String strIcon = EnoviaResourceBundle.getProperty(context,"emxComponents.HigherRevisionImage");
        DomainObject domObj = new DomainObject();

        //Iterating through the list of objects to generate the program HTML output for each object in the table
        for (int jCount = 0; jCount < iNumOfObjects; jCount++) {
        	String str = "";
        	boolean hasHigherRevision = DerivationUtil.higherRevisionExists(context, arrObjId[jCount]);
        	if (hasHigherRevision) {
        		if (reportFormat != null && "CSV".equalsIgnoreCase(reportFormat)){
        			strHigherRevisionIconTag = strTooltipHigherRevExists;
	            } else {
	            	strHigherRevisionIconTag =
                        "<img src=\"../common/images/"
                            + XSSUtil.encodeForXML(context,strIcon)
                            + "\" border=\"0\"  align=\"middle\" "
                            + "TITLE=\""
                            + " "
                            + XSSUtil.encodeForXML(context,strTooltipHigherRevExists)
                            + "\""
                            + "/>";
	            }
            } else {
               	strHigherRevisionIconTag = " ";
            }
            lstHigherRevExists.add(strHigherRevisionIconTag);
        }
        return lstHigherRevExists;
    }


    /**
    * Method shows higher revision Icon in the object property page if a higher revision of the object exists
    * @param context the eMatrix <code>Context</code> object
    * @return String - returns the program HTML output
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    public String getHigherRevisionIconProperty(Context context, String[] args) throws Exception {

    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map relBusObjPageList = (HashMap) programMap.get("paramMap");
    	String strObjectId = (String)relBusObjPageList.get("objectId");
    	
    	Map requestMap = (HashMap)programMap.get("requestMap");
    	String reportFormat = (String)requestMap.get("reportFormat");
    	
    	//String Buffer to display the Higher revision field in Req property page.
    	StringBuffer sbHigherRevisionExists = new StringBuffer(100);
    	String strHigherRevisionExists = "";

    	//Reading the tooltip from property file.
    	String strTooltipHigherRevExists =
    	EnoviaResourceBundle.getProperty(context, SUITE_KEY,ICON_TOOLTIP_HIGHER_REVISION_EXISTS,context.getSession().getLanguage());

        String strHigherRevisionIconTag= "";
        DomainObject domObj = DomainObject.newInstance(context, strObjectId);

        // Begin of Add by Enovia MatrixOne for Bug 300775 Date 03/25/2005
        String strNo                  = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                                            "emxProduct.Label.No",
                                            context.getSession().getLanguage());
        String strYes                 = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                                            "emxProduct.Label.Yes",
                                            context.getSession().getLanguage());
        // End of Add by Enovia MatrixOne for Bug 300775 Date 03/25/2005

        //To generate the program HTML output for the context object

        if (DerivationUtil.higherRevisionExists(context, strObjectId)) {
        	//check for reportFormat
        	if(ProductLineCommon.isNotNull(reportFormat))
        	{
        		strHigherRevisionIconTag = strYes;
        	}
        	else
        	{
        		//This part is commented for removing link from icon & value 'Yes'
        		/*strHigherRevisionIconTag =
                    "<a HREF=\"#\" TITLE=\""
                            + " "
                            + XSSUtil.encodeForHTMLAttribute(context,strTooltipHigherRevExists)
                            + "\">"
                            + HIGHER_REVISION_ICON
                            + XSSUtil.encodeForXML(context,strYes)
                            + "</a>";*/
        		
        		strHigherRevisionIconTag = HIGHER_REVISION_ICON + XSSUtil.encodeForXML(context,strYes);
                                
        	}
        	sbHigherRevisionExists.append(strHigherRevisionIconTag);
            strHigherRevisionExists = sbHigherRevisionExists.toString();

        } else {
            sbHigherRevisionExists.append(strNo);
            strHigherRevisionExists = sbHigherRevisionExists.toString();

        }

        return strHigherRevisionExists;
    }


    /**
     * Method shows higher revision Icon if a higher revision of the object exists
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List - returns the program HTML output
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
     public List getNewDerivationIcon(Context context, String[] args) throws Exception{

 	     Map programMap = (HashMap) JPO.unpackArgs(args);
 	     MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
 	     Map paramList = (HashMap)programMap.get("paramList");
 	     String reportFormat = (String)paramList.get("reportFormat");
 	
 	     int iNumOfObjects = relBusObjPageList.size();
 	     // The List to be returned
 	     List lstNewDerivationExists = new Vector(iNumOfObjects);
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
   	     String strTooltipNewDerivationExists =
   	     EnoviaResourceBundle.getProperty(context, SUITE_KEY,ICON_TOOLTIP_NEW_DERIVATION_EXISTS,context.getSession().getLanguage());

   	     String strNewDerivationIconTag= "";

         //Iterating through the list of objects to generate the program HTML output for each object in the table
         for (int jCount = 0; jCount < iNumOfObjects; jCount++) {
             String str = "";
         	 boolean newDerivationExists = DerivationUtil.newDerivationExists(context, arrObjId[jCount]);
         	if (newDerivationExists) {
         		if (reportFormat != null && "CSV".equalsIgnoreCase(reportFormat)){
         			strNewDerivationIconTag = strTooltipNewDerivationExists;
 	            } else {
 	               strNewDerivationIconTag =
    	                      "<a HREF=\"#\" TITLE=\""
    	                              + " "
    	                              + strTooltipNewDerivationExists
    	                              + "\">"
    	                              + NEW_DERIVATION_ICON
    	                              + "</a>";
 	            }
             } else {
            	 strNewDerivationIconTag = " ";
             }
         	lstNewDerivationExists.add(strNewDerivationIconTag);
         }
         return lstNewDerivationExists;
     }

     /**
     * This FORM JPO method is used to get Derivation Type readonly value.
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return String containing HTML for combo box
     * @throws FrameworkException
     */
     
     public String getNewDerivationExistsProperty(Context context, String[] args) throws Exception {
    	 String derivationExistsValue = "";

  		// Get the required parameter values from the REQUEST map
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String objectId = (String) requestMap.get("objectId");
   	  		
		String reportFormat = (String)requestMap.get("reportFormat"); 
		
        //String Buffer to display the Higher revision field in Req property page.
   	    StringBuffer sbNewDerivationExists = new StringBuffer(100);
   	    String strNewDerivationExists = "";

        //Reading the tooltip from property file.
   	    String strTooltipNewDerivationExists =
   	    	EnoviaResourceBundle.getProperty(context, SUITE_KEY,ICON_TOOLTIP_NEW_DERIVATION_EXISTS,context.getSession().getLanguage());

   	    String strNewDerivationIconTag= "";
        String strNo  =EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Label.No",context.getSession().getLanguage());
        String strYes =EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Label.Yes",context.getSession().getLanguage());

        //To generate the program HTML output for the context object

        if (DerivationUtil.newDerivationExists(context, objectId)) {
        	//check for reportFormat
        	if(ProductLineCommon.isNotNull(reportFormat))
        	{
        		strNewDerivationIconTag = strYes;
        	}
        	else
        	{
        		// This part is commented for removing link of the value 'Yes' and icon.
        		/*strNewDerivationIconTag =
	                      "<a HREF=\"#\" TITLE=\""
	                              + " "
	                              + XSSUtil.encodeForHTMLAttribute(context,strTooltipNewDerivationExists)
	                              + "\">"
	                              + NEW_DERIVATION_ICON
	                              + XSSUtil.encodeForXML(context,strYes)
	                              + "</a>";*/
        		
        		strNewDerivationIconTag = NEW_DERIVATION_ICON + XSSUtil.encodeForXML(context,strYes);
	                              
        	}
   	        sbNewDerivationExists.append(strNewDerivationIconTag);
   	        strNewDerivationExists = sbNewDerivationExists.toString();

   	    } else {
   	        sbNewDerivationExists.append(strNo);
   	        strNewDerivationExists = sbNewDerivationExists.toString();
   	    }
   	    return strNewDerivationExists;
   	}
   	  		
    
   /** This column JPO method is used to get the RDO for a object. If the context
    * user has read access on the RDO object then it is hyperlinked to the
    * properties page otherwise only name is returned. Also if the context user
    * doesn't have the show access on the RDo object then context is changed
    * to super user to retrieve the RDO name.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - String array containing following packed HashMap
    *                       with following elements:
    *                       paramMap - The HashMap containig the object id.
    * @return String - The program HTML output containing the RDO name.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    public String getDesignResponsibility(Context context, String[] args) throws Exception{
            String strPolicyProduct = PropertyUtil.getSchemaProperty(context,SYMBOLIC_policy_Product);

            String strStateReview = FrameworkUtil.lookupStateName(      context,
                                                                        strPolicyProduct,
                                                                        SYMB_state_Review
                                                                  );
            String strStateRelease = FrameworkUtil.lookupStateName(     context,
                                                                        strPolicyProduct,
                                                                        SYMB_state_Release
                                                                  );
            String strStateObsolete= FrameworkUtil.lookupStateName(     context,
                                                                        strPolicyProduct,
                                                                        SYMB_state_Obsolete
                                                                  );
        //Get the object id of the context object
        Map programMap = (HashMap) JPO.unpackArgs(args);
                Map relBusObjPageList = (HashMap) programMap.get("paramMap");
                Map mpRequest = (HashMap) programMap.get("requestMap");
        String strObjectId = (String)relBusObjPageList.get("objectId");
                String strMode = (String)mpRequest.get("mode");
                String strPFMode=(String)mpRequest.get("PFmode");

        //Begin of add by Enovia MatrixOne on 18-Apr-05 for Bug#300548
        Map fieldMap = (HashMap) programMap.get("fieldMap");
        String strFieldName = (String)fieldMap.get("name");
        //End of add by Enovia MatrixOne on 18-Apr-05 for Bug#300548

        //Form the select expressions for getting the RDO name and RDO id.
        StringBuffer sbRDONameSelect  = new StringBuffer("to[");
        sbRDONameSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
        sbRDONameSelect.append("].from.");
        sbRDONameSelect.append(DomainConstants.SELECT_NAME);

        StringBuffer sbRDOIdSelect  = new StringBuffer("to[");
        sbRDOIdSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
        sbRDOIdSelect.append("].from.");
        sbRDOIdSelect.append(DomainConstants.SELECT_ID);

                //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 Date:4/13/2005
                StringBuffer sbRDOTypeSelect  = new StringBuffer("to[");
        sbRDOTypeSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
        sbRDOTypeSelect.append("].from.");
        sbRDOTypeSelect.append(DomainConstants.SELECT_TYPE);
                //End of add for bug 301411

        //CODE CHANGES
        String exportFormat = "";
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        if(requestMap!=null && requestMap.containsKey("reportFormat")){
        	exportFormat = (String)requestMap.get("reportFormat");
        }

        StringList lstObjSelects = new StringList();
        lstObjSelects.add(sbRDONameSelect.toString());
        lstObjSelects.add(sbRDOIdSelect.toString());
                //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 Date:4/13/2005
                lstObjSelects.add(sbRDOTypeSelect.toString());
                //End of add for bug 301411

        String strRDOId = "";
        String strRDOName = "";
        StringBuffer sbHref  = new StringBuffer();
                StringBuffer sbBuffer  = new StringBuffer();
                //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 Date:4/13/2005
                String strTempIcon = DomainConstants.EMPTY_STRING;
                String strRDOType =  DomainConstants.EMPTY_STRING;
                String strTypeIcon = DomainConstants.EMPTY_STRING;



                //End of add for bug 301411

        //Get the RDO id and name by changing the context to super user
        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
        ContextUtil.pushContext(context);

        Map mapRDO = (Map)domObj.getInfo(context,lstObjSelects);

        ContextUtil.popContext(context);

        //If RDO is set for this object then check whether the context user has read
        //access on the RDO object. If yes then hyperlink the RDO name to its
        //properties page otherwise return the RDO name.
                // If the mode is edit , display the design responsibility field as textbox with a chooser.

        if(strMode!=null && !strMode.equals("") &&
            !strMode.equalsIgnoreCase("null") && strMode.equalsIgnoreCase("edit"))
        {

            if(mapRDO!=null&&mapRDO.size()>0){
                strRDOName = (String) mapRDO.get(sbRDONameSelect.toString());
                //Begin of modify by Enovia MatrixOne on 1-June-05 for bug 304576 reopened
                if (mapRDO.get(sbRDOIdSelect.toString()) instanceof StringList){
                    StringList strRDOListId = (StringList) mapRDO.get(sbRDOIdSelect.toString());
                    strRDOId =  (String)strRDOListId.get(0);
                } else {
                    strRDOId = (String) mapRDO.get(sbRDOIdSelect.toString());
                }
                //End of modify by Enovia MatrixOne on 1-June-05 for bug 304576 reopened

                                //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 Date:4/13/2005
                                strRDOType = (String) mapRDO.get(sbRDOTypeSelect.toString());
                                //End of Add for bug 301411
            }

            if(strRDOName==null || strRDOName.equalsIgnoreCase("null") || strRDOName.equals("")){
                strRDOName = "";
                strRDOId = "";
                                //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 Date:4/13/2005
                                strRDOType = "";
                                //End of add for bug 301411
            }
            //Begin of Add by Vibhu,Enovia MatrixOne for Bug 311803 on 11/22/2005
            boolean bHasReadAccess =false;

            /* Start - Added  by Amarpreet Singh 3dPLM for Checking the access to Edit Design Responsibility*/          String strCtxUser = context.getUser();
            String strOwner = (String)domObj.getInfo(context,DomainConstants.SELECT_OWNER);
            boolean hasRoleProductManager  = false;
            boolean hasRoleSystemEngineer  = false;
            boolean bIsOwner               = false;

            Person ctxPerson = new Person (strCtxUser);

            hasRoleProductManager = ctxPerson.isAssigned(context,"Product Manager");
            hasRoleSystemEngineer = ctxPerson.isAssigned(context,"System Engineer");

            if ( strCtxUser != null && !"".equals(strCtxUser))
            {
                if (strOwner!= null && !"".equals(strOwner))
                {
                    if (strOwner.equals(strCtxUser))
                    {
                        bIsOwner = true;
                    }
                }
            }
          /* End - Added  by Amarpreet Singh 3dPLM for Checking the access to Edit Design Responsibility*/
          /* Start  - Added  by Amarpreet Singh 3dPLM for Checking the access to Edit Design Responsibility*/
            try
            {
                if ( (strRDOId==null || strRDOId.equals("")) &&
                    ( bIsOwner || hasRoleProductManager || hasRoleSystemEngineer )){
                    bHasReadAccess = true;
                }
                else {
                    boolean hasAccessOnProject = emxProduct_mxJPO.hasAccessOnProject(context,strRDOId);
                    if (hasAccessOnProject && ( bIsOwner || hasRoleProductManager || hasRoleSystemEngineer ))
                    {
                        bHasReadAccess = true;
                    }
                }
            } catch (Exception e) {
                bHasReadAccess = false;
            }

          /* End - Added  by Amarpreet Singh 3dPLM for Checking the access to Edit Design Responsibility*/

            if (!bHasReadAccess)
            {
                if (strRDOType!=null && !strRDOType.equals("") && !strRDOType.equalsIgnoreCase("null")){
                    strTypeIcon = UINavigatorUtil.getTypeIconFromCache(strRDOType);
                    strTypeIcon = "images/"+strTypeIcon;
                }
                if ( strTypeIcon == null || ("").equals(strTypeIcon))
                {
                    strTypeIcon = "images/iconSmallCompany.gif";
                }
                sbBuffer.delete(0, sbBuffer.length());
                sbBuffer.append("<img border=\"0\" src=\"");
                sbBuffer.append(strTypeIcon);
                sbBuffer.append("\"</img>");
                sbBuffer.append(SYMB_SPACE);
                sbBuffer.append(strRDOName);
            } else {
            //End of Add by Vibhu,Enovia MatrixOne for Bug 311803 on 11/22/2005
                //Begin of modify by Enovia MatrixOne for Bug# 300548 on 18-Apr-05
                sbBuffer.append("<input type=\"text\"");
                sbBuffer.append("name=\"");
                sbBuffer.append(strFieldName);
                sbBuffer.append("Display\" id=\"\" value=\"");
                sbBuffer.append(strRDOName);
                sbBuffer.append("\">");
                sbBuffer.append("<input type=\"hidden\" name=\"");
                sbBuffer.append(strFieldName);
                sbBuffer.append("\" value=\"");
                sbBuffer.append(strRDOId);
                sbBuffer.append("\">");
                sbBuffer.append("<input type=\"hidden\" name=\"");
                sbBuffer.append(strFieldName);
                sbBuffer.append("OID\" value=\"");
                sbBuffer.append(strRDOId);
                sbBuffer.append("\">");
                sbBuffer.append("<input ");
                sbBuffer.append("type=\"button\" name=\"btnDesignResponsibility\"");
                sbBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
                sbBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace&table=PLCDesignResponsibilitySearchTable&selection=single&formName=editDataForm&submitAction=refreshCaller&hideHeader=true&typeAheadTable=PLCTypeAheadTable&submitURL=../productline/SearchUtil.jsp?&mode=Chooser&chooserType=FormChooser");
                sbBuffer.append("&frameName=formEditDisplay");
                sbBuffer.append("&fieldNameActual=");
                sbBuffer.append(strFieldName);
                //Modified for Bug: 372104
                sbBuffer.append("OID");
                sbBuffer.append("&fieldNameDisplay=");
                sbBuffer.append(strFieldName);
                sbBuffer.append("Display");
               // Commenting for Bug: 372104--
//                sbBuffer.append("&fieldNameOID=");
//                sbBuffer.append(strFieldName);
//                sbBuffer.append("OID");
                sbBuffer.append("&searchmode=chooser");
                sbBuffer.append("&suiteKey=Configuration");
                sbBuffer.append("&searchmenu=SearchAddExistingChooserMenu");
                // Begin of Modify by Praveen, Enovia MatrixOne for Bug #300094 03/15/2005
                sbBuffer.append("&searchcommand=PLCSearchCompanyCommand,PLCSearchProjectsCommand");
                // End of Modify by Praveen, Enovia MatrixOne for Bug #300094 03/15/2005
                sbBuffer.append("&PRCParam1=DesignResponsibility");
                sbBuffer.append("&objectId=");
                sbBuffer.append(strObjectId);
                sbBuffer.append("&HelpMarker=emxhelpfullsearch','850','630')\">");
                sbBuffer.append("&nbsp;&nbsp;");
                sbBuffer.append("<a href=\"javascript:ClearDesignResponsibility('");
                sbBuffer.append(strFieldName);
                sbBuffer.append("')\">");
                //End of modify by Enovia MatrixOne for Bug# 300548 on 18-Apr-05

                String strClear =
                EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Button.Clear",context.getSession().getLanguage());
                sbBuffer.append(strClear);
                sbBuffer.append("</a>");
            }
            return sbBuffer.toString();
        }else{

            if(mapRDO!=null&&mapRDO.size()>0){

                strRDOName = (String) mapRDO.get(sbRDONameSelect.toString());

                if (mapRDO.get(sbRDOIdSelect.toString()) instanceof StringList)
                {

                 StringList strRDOListId = (StringList) mapRDO.get(sbRDOIdSelect.toString());
                 strRDOId =  (String)strRDOListId.get(0);
                } else {
                 strRDOId = (String) mapRDO.get(sbRDOIdSelect.toString());

                }

                strRDOType = (String) mapRDO.get(sbRDOTypeSelect.toString());

            }else{
                strRDOName = "";
                strRDOId = "";
                                //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 date: 4/13/2005
                                strRDOType = "";
                                //End of add for bug 301411
            }

                        //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 date: 4/13/2005
            //Begin of Modify by Enovia MatrixOne for bug 301411 (reopened) on 24-May-05
            if (strRDOType!=null && !strRDOType.equals("") && !strRDOType.equalsIgnoreCase("null")){
               strTypeIcon = UINavigatorUtil.getTypeIconFromCache(strRDOType);
               strTypeIcon = "images/"+strTypeIcon;
                         }
             //End of Modify by Enovia MatrixOne for bug 301411(reopened) on 24-May-05
                          //End of add for bug 301411
            if(strRDOName!=null && !strRDOName.equals("") && !strRDOName.equalsIgnoreCase("null")){

                boolean bHasReadAccess;
                try
                {
                    //Modified by Vibhu,Enovia MatrixOne for Bug 311803 on 11/22/2005
                    bHasReadAccess = emxProduct_mxJPO.hasAccessOnProject(context,strRDOId);
                } catch (Exception e) {
                    bHasReadAccess = false;
                }

                //Begin of Modify by Rashmi, Enovia MatrixOne for bug 301411 date: 4/13/2005
				if (bHasReadAccess) {
					// CODE changes
					if ("CSV".equalsIgnoreCase(exportFormat)) {
						sbHref.append(strRDOName);
					} else if ("true".equalsIgnoreCase(strPFMode)) {
						sbHref.append("<img border=\"0\" src=\"");
						sbHref.append(strTypeIcon);
						sbHref.append("\"</img>");
						sbHref.append(strRDOName);
					} else {
						sbHref
								.append("<A HREF=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?objectId=");
						sbHref.append(strRDOId);
						sbHref.append("&mode=replace");
						sbHref.append("&AppendParameters=true");
						sbHref.append("&reloadAfterChange=true");
						sbHref.append("')\"class=\"object\">");
						sbHref.append("<img border=\"0\" src=\"");
						sbHref.append(strTypeIcon);
						sbHref.append("\"</img>");
						sbHref.append("</A>");
						sbHref.append("&nbsp");
						sbHref
								.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
						sbHref.append(strRDOId);
						sbHref.append("&mode=replace");
						sbHref.append("&AppendParameters=true");
						sbHref.append("&reloadAfterChange=true");
						sbHref.append("')\"class=\"object\">");
						sbHref.append(strRDOName);
						sbHref.append("</A>");
					}

					return sbHref.toString();
				}else{
                                        sbBuffer.delete(0, sbBuffer.length());
                                      //CODE changes
                                        if("CSV".equalsIgnoreCase(exportFormat)){
                                        	sbBuffer.append(strRDOName);
                                        }else{
                                        	sbBuffer.append("<img border=\"0\" src=\"");
                                            sbBuffer.append(strTypeIcon);
                                            sbBuffer.append("\"</img>");
                                            sbBuffer.append(SYMB_SPACE);
                                            sbBuffer.append(strRDOName);
                                        }

                    return sbBuffer.toString();
                                        //End of modify for bug 301411
                }
            }else{
                return "";
            }
        }
 }

    /**
     * Connects the design responsibility organization to a feature.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - HashMap containing one Map entry for the key PARAM_MAP
     *          This Map contains the arguments passed to the jsp which called this method.
     * @return int - an integer (0) if the operation is successful
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
	public int updateDesignResponsibility(Context context, String[] args)
			throws Exception {
		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map paramMap = (HashMap) programMap.get("paramMap");
		String strObjectId = (String) paramMap.get(OBJECT_ID);

		String strNewOrganizationOID = (String) paramMap.get("New Value");
		if (strNewOrganizationOID == null) {
			strNewOrganizationOID = "";
		}
		String strObjID = (String) paramMap.get("objectId");
		// Object for which PrimaryOwnership is to set
		DomainObject domObj = new DomainObject(strObjID);
		String defaultProj=PersonUtil.getDefaultProject(context, context.getUser());
		if (!strNewOrganizationOID.equals("")) {
			DomainObject domObjOrgnization = new DomainObject(
					strNewOrganizationOID);
			// TODO- Assumption Oranization Name is same as Role name
			String strNewOrganizationName = domObjOrgnization.getInfo(context,
					DomainObject.SELECT_NAME);
			domObj.setPrimaryOwnership(context,
					defaultProj,
					strNewOrganizationName);
		}else{
			String defaultOrg=PersonUtil.getDefaultOrganization(context, context.getUser());
			domObj.setPrimaryOwnership(context,defaultProj,defaultOrg);
		}
		return 0;

	}

//Begin of Add by Enovia MatrixOne for EC bug on 18 Mar 2005
    /**
     * This trigger method is used to check whether a valid Engineering Change
     * object in complete state is connected to the object by Implemented or
     * Affected Item relationship when it is promoted to release or obsolete state.
     *.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - The id of the object.
     *        1 -  The next state of the object (Release/Obsolete).
     * @return int - an integer (0) if the operation is successful
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
    public int checkConnectedEC (Context context, String[] args)
                                                                            throws Exception{
        //Get the object id and next state from the args
        String strObjectId = args[0];
        String strNextState = args[1];

        //Get the EC Object in Complete state connected to the context object
        //by Implemented Item or Affected Item Relationship depending upon the
        //next state
        DomainObject domItem = DomainObject.newInstance(context,
                                                                                       strObjectId);

        String strStateRelease = FrameworkUtil.lookupStateName(
                                                                context,
                                                                domItem.getPolicy(context).getName(),
                                                                "state_Release");

         List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);

        String strPolicyEC = PropertyUtil.getSchemaProperty(context,
                                                            SYMBOLIC_policy_EngineeringChange);
        String strStateComplete = FrameworkUtil.lookupStateName(
                                                                context,
                                                                strPolicyEC,
                                                                SYMB_state_Complete);

        StringBuffer sbObjWhereExpression = new StringBuffer();
        sbObjWhereExpression.append(DomainConstants.SELECT_CURRENT);
        sbObjWhereExpression.append(SYMB_EQUAL);
        sbObjWhereExpression.append(SYMB_QUOTE);
        sbObjWhereExpression.append(strStateComplete);
        sbObjWhereExpression.append(SYMB_QUOTE);

        StringBuffer sbRelWhereExpression = new StringBuffer();
        sbRelWhereExpression.append(SYMB_ATTRIBUTE);
        sbRelWhereExpression.append(SYMB_OPEN_BRACKET);
        sbRelWhereExpression.append(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE);
        sbRelWhereExpression.append(SYMB_CLOSE_BRACKET);
        sbRelWhereExpression.append(SYMB_EQUAL);

        List lstECList = new MapList();

         //If the item is being to promoted to Release state then
         //Get the EC in complete state connected to this object by Implemented
        //Item Relationship or Affected Item Relationship for which Requested Change
        //attribute is For Release
         if(strNextState.equals(strStateRelease)){
             lstECList = (MapList) domItem.getRelatedObjects(
                                                                context,
                                                                DomainConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM,
                                                                DomainConstants.QUERY_WILDCARD,
                                                                (StringList) lstObjectSelects,
                                                                null,
                                                                true,
                                                                false,
                                                                (short) 1,
                                                                sbObjWhereExpression.toString(),
                                                                null);
            if(lstECList!=null && !lstECList.isEmpty()){
                return 0;
            }else{
                //Get the EC connected to this object by Affected Item relationship
                //with proper Requested Change attribute
                sbRelWhereExpression.append("\"For Release\"");
                                //Begin of Add by Vibhu,Enovia MatrixOne for Bug 303269 on 28 April 05
                                sbRelWhereExpression.append(SYMB_SPACE);
                sbRelWhereExpression.append(SYMB_OR);
                sbRelWhereExpression.append(SYMB_SPACE);
                sbRelWhereExpression.append(SYMB_ATTRIBUTE);
                sbRelWhereExpression.append(SYMB_OPEN_BRACKET);
                sbRelWhereExpression.append(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE);
                sbRelWhereExpression.append(SYMB_CLOSE_BRACKET);
                sbRelWhereExpression.append(SYMB_EQUAL);
                sbRelWhereExpression.append("\"For Obsolescence\"");
                                //End of Add by Vibhu,Enovia MatrixOne for Bug 303269 on 28 April 05
                lstECList = (MapList) domItem.getRelatedObjects(
                                                                context,
                                                                DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM,
                                                                DomainConstants.QUERY_WILDCARD,
                                                                (StringList) lstObjectSelects,
                                                                null,
                                                                true,
                                                                false,
                                                                (short) 1,
                                                                sbObjWhereExpression.toString(),
                                                                sbRelWhereExpression.toString());
                if(lstECList!=null && !lstECList.isEmpty()){
                    return 0;
                 }else{
                     String strErrorMsg = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Alert.NoCompleteECForRelease",context.getSession().getLanguage());
                    emxContextUtil_mxJPO.mqlNotice(context,strErrorMsg);
                    return 1;
                }
            }
        }
        //If the item is being to promoted to Obsolete state then
         //Get the EC in complete state connected to this object Affected Item
         //Relationship for which Requested Change attribute is For Obsolescence
        else{
               sbRelWhereExpression.append("\"For Obsolescence\"");
               lstECList = (MapList) domItem.getRelatedObjects(
                                                                context,
                                                                DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM,
                                                                DomainConstants.QUERY_WILDCARD,
                                                                (StringList) lstObjectSelects,
                                                                null,
                                                                true,
                                                                false,
                                                                (short) 1,
                                                                sbObjWhereExpression.toString(),
                                                                sbRelWhereExpression.toString());
               if(lstECList!=null && !lstECList.isEmpty()){
                   return 0;
                }else{
                    String strErrorMsg = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Alert.NoCompleteECForObsolescence",context.getSession().getLanguage());
                    emxContextUtil_mxJPO.mqlNotice(context,strErrorMsg);
                    return 1;
                }
        }
    }
//End of Add by Enovia MatrixOne for EC bug on 18 Mar 2005

        //Begin of Add by Vibhu,Enovia MatrixOne for Bug#300051 on 29 Mar 2005

        /**
         * This method is used to remove a particular branch of maps which have
           levels higher than specified Index object. The method removed the map
           until it encounters a map of lower or equal level than the indexed Object.
         * @param MapList - filteredMapList in which the maps are removed.
         * @param Integer - Index of the base object which is taken as reference, all
                            objects are compared with the indexed object.
         * @return integer- index of the map which has to be searched next.
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
    **/

        public static int removeBranch(List filteredMapList,int iCurrentIndex) throws Exception
    {
        Map mpMap = null;
        int iNextLevel = 0;
        int iBaseLevel = 0;
        mpMap = (Map)filteredMapList.get(iCurrentIndex);
        iBaseLevel = Integer.parseInt((String)mpMap.get(DomainConstants.KEY_LEVEL));


        //This do while removes those objects whose level is higher then the base level
        do
        {
                if(iCurrentIndex == filteredMapList.size()-1)
                {
                        filteredMapList.remove(mpMap);
                        iCurrentIndex--;
                        break;
                }
                else
                {
                        filteredMapList.remove(mpMap);
                        iCurrentIndex--;
                        mpMap = (Map)filteredMapList.get(iCurrentIndex);
                        iNextLevel = Integer.parseInt((String)mpMap.get(DomainConstants.KEY_LEVEL));
                }
        }while(iNextLevel > iBaseLevel);

        return iCurrentIndex;
    }
         //End of Add by Vibhu,Enovia MatrixOne for Bug#300051 on 29 Mar 2005
    /**
     * This method is used to return the Name and Revision of an object
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List- the List of Strings in the form of 'Name Revision'
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6SP1
    **/

    public List getNameRev (Context context, String[] args) throws Exception{
        //unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) programMap.get(STR_OBJECT_LIST);
        Iterator objectListItr = lstobjectList.iterator();
        //initialise the local variables
            String oidsArray[] = new String[lstobjectList.size()];
            int i = 0;
        Map objectMap = new HashMap();
            String strObjIdtemp =DomainConstants.EMPTY_STRING;
            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();
                strObjIdtemp = (String)objectMap.get(DomainConstants.SELECT_ID);
                oidsArray[i] = strObjIdtemp;
                i++;
            }
            StringList selects = new StringList(3);
            selects.add(DomainConstants.SELECT_TYPE);
            selects.add(DomainConstants.SELECT_NAME);
            selects.add(DomainConstants.SELECT_REVISION);
            MapList list = DomainObject.getInfo(context, oidsArray, selects);
        String strObjId = DomainConstants.EMPTY_STRING;
            Map objecttempMap = new HashMap();
        List lstNameRev = new StringList();
            StringBuffer stbNameRev;
        String strType = null;
            String strName = null;
            String strRev = null;

            objectListItr = list.iterator();
        while(objectListItr.hasNext())
        {
                objecttempMap = (Map) objectListItr.next();
                strName = (String)objecttempMap.get(DomainConstants.SELECT_NAME);
                strType = (String)objecttempMap.get(DomainConstants.SELECT_TYPE);
                strRev = (String)objecttempMap.get(DomainConstants.SELECT_REVISION);

                stbNameRev = new StringBuffer(100);
                stbNameRev = stbNameRev.append(strName);
                if(strType.equalsIgnoreCase(ProductLineConstants.TYPE_PRODUCT_VARIANT)){
                    stbNameRev.append(SYMB_SPACE );
                    stbNameRev.append(strRev.substring(0,1));
                } else {
                    stbNameRev.append(SYMB_SPACE );
                    stbNameRev.append(strRev);
            }
            lstNameRev.add(stbNameRev.toString());
        }
        return lstNameRev;
    }

    /**
     * This method is used to return the status icon of an object
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List- the List of Strings in the form of 'Name Revision'
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6SP1
    **/

    public List getStatusIcon (Context context, String[] args) throws Exception{
        //unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) programMap.get(STR_OBJECT_LIST);
        Iterator objectListItr = lstobjectList.iterator();
        
        // For Fixing IR-228607V6R2014x Start
        Map paramMap = (HashMap) programMap.get("paramList");
        String strReportFormat = (String)paramMap.get("reportFormat");
        // For Fixing IR-228607V6R2014x End
        
        //initialise the local variables
        Map objectMap = new HashMap();
        String strObjId = DomainConstants.EMPTY_STRING;
        String strObjState = DomainConstants.EMPTY_STRING;
        String strIcon = DomainConstants.EMPTY_STRING;
        // Begin of Add by Enovia MatrixOne for Bug # 312021 Date Nov 16, 2005
        String strObjPolicy = DomainConstants.EMPTY_STRING;
        String strObjPolicySymb = DomainConstants.EMPTY_STRING;
        String strObjStateSymb = DomainConstants.EMPTY_STRING;
        StringBuffer sbStatePolicyKey = new StringBuffer();
        boolean flag = false;
        // End of Add by Enovia MatrixOne for Bug # 312021 Date Nov 16, 2005
        List lstNameRev = new StringList();
        StringBuffer stbNameRev = new StringBuffer(100);
        DomainObject domObj = null;
        //loop through all the records
        while(objectListItr.hasNext())
        {
            objectMap = (Map) objectListItr.next();
            strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
            domObj = DomainObject.newInstance(context, strObjId);
            strObjState = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
            // Begin of Add by Enovia MatrixOne for Bug # 312021 Date Nov 16, 2005
            strObjPolicy = domObj.getInfo(context, DomainConstants.SELECT_POLICY);

            // Getting symbolic names for both policy & state
            strObjPolicySymb = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_POLICY,strObjPolicy,true);
            strObjStateSymb = FrameworkUtil.reverseLookupStateName(context, strObjPolicy, strObjState);

            // Forming the key which is to be looked up
            sbStatePolicyKey = new StringBuffer("emxProduct.LCStatusImage.");
            sbStatePolicyKey.append(strObjPolicySymb)
                            .append(".")
                            .append(strObjStateSymb);

            // Geeting the value for the corresponding key, if not catching it to set flag = false
            try{
                strIcon = EnoviaResourceBundle.getProperty(context,sbStatePolicyKey.toString());
                flag = true;
            }
            catch(Exception ex)
            {
                flag = false;
            }
            // End of Add by Enovia MatrixOne for Bug # 312021 Date Nov 16, 2005

            //Begin of Add by Vibhu,Enovia MatrixOne for Bug 310473 on 10/11/2005
            // Begin of Add by Enovia MatrixOne for Bug # 312021 Date Nov 16, 2005
            if(flag) 
            {
                strObjState = FrameworkUtil.findAndReplace(strObjState," ", "");
                StringBuffer sbStateKey = new StringBuffer("emxFramework.State.");
                sbStateKey.append(strObjState);
                strObjState = EnoviaResourceBundle.getProperty(context, SUITE_KEY,sbStateKey.toString(),context.getSession().getLanguage());
                //End of Add by Vibhu,Enovia MatrixOne for Bug 310473 on 10/11/2005
                
                if(strReportFormat == null || (strReportFormat!=null && strReportFormat.equalsIgnoreCase(""))) // if(strReportFormat == null || strReportFormat == "") else , Added For Fixing IR-228607V6R2014x
                {
                	stbNameRev.delete(0, stbNameRev.length());
                    stbNameRev = stbNameRev.append("<img src=\"../common/images/")
                                    .append(strIcon)
                                    .append("\" border=\"0\"  align=\"middle\" ")
                                    .append("TITLE=\"")
                                    .append(" ")
                                    .append(strObjState)
                                    .append("\"")
                                    .append("/>");
                    lstNameRev.add(stbNameRev.toString());
                }
                else
                {
                	lstNameRev.add(strObjState);  
                }
                
            }
            else
            {
                lstNameRev.add(DomainConstants.EMPTY_STRING);
            }
            // End of Add by Enovia MatrixOne for Bug # 312021 Date Nov 16, 2005
        }
        return lstNameRev;
    }

//Start of Add By Enovia MatrixOne, for Performance Bug - Where Used on 23 Jan 06.
/**
     * This method is used to remove the duplicate values in a String having
     * various elements separated by some delimiting character
     * @param String to be investigated for duplicate entries
     * @param delimiting character
     * @return String having unique elements separated by the delimiting character
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6SP2
    **/

    public static String removeDuplicateInString (String str, String strDelimiting) throws Exception
    {
        StringTokenizer st = new StringTokenizer(str, strDelimiting);
        HashSet hashset = new HashSet();
        StringBuffer sb = new StringBuffer(300);
        while(st.hasMoreTokens())
            {
                hashset.add(st.nextElement());
            }

        Iterator lstItr = hashset.iterator();
        while (lstItr.hasNext())
            {
                sb.append(lstItr.next().toString());
                sb.append(STR_COMMA);
            }

        sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }
//End of Add By Enovia MatrixOne, for Performance Bug - Where Used on 23 Jan 06.
    /**
     * To obtain the list of Object IDs to be excluded from the search for Add Existing Actions
     *
     * @param context- the eMatrix <code>Context</code> object
     * @param args- holds the HashMap containing the following arguments
     * @return  StringList- consisting of the object ids to be excluded from the Search Results
     * @throws Exception if the operation fails
     * @author Sandeep Kathe(klw)
     */

    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeConnected(Context context, String[] args) throws Exception
    {
        Map programMap = (Map) JPO.unpackArgs(args);
        String strObjectIds = (String)programMap.get("objectId");
        String strRelationship=(String)programMap.get("relName");
        StringList excludeList= new StringList();
        StringTokenizer objIDs = new StringTokenizer(strObjectIds,",");
        String toType=null;
        String fromType=null;
        boolean bisTo=false;
        boolean bisFrom=false;
        DomainObject domObjFeature = new DomainObject(strObjectIds);
        toType=domObjFeature.getInfo(context,"to["+PropertyUtil.getSchemaProperty(context,strRelationship)+"].from.type");
        fromType=domObjFeature.getInfo(context,"from["+PropertyUtil.getSchemaProperty(context,strRelationship)+"].to.type");

        if(toType!=null){
            bisTo=true;
        }
        else{
            bisFrom=true;
        }
        MapList childObjects=domObjFeature.getRelatedObjects(context,
                PropertyUtil.getSchemaProperty(context,strRelationship),
                toType==null?fromType:toType,
                new StringList(DomainConstants.SELECT_ID),
                null,
                bisTo,
                bisFrom,
               (short) 1,
                DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING);
        for(int i=0;i<childObjects.size();i++){
            Map tempMap=(Map)childObjects.get(i);
            excludeList.add(tempMap.get(DomainConstants.SELECT_ID));
        }
        excludeList.add(strObjectIds);
        return excludeList;
    }

    /**
     * Method Check if Variant Configuration Application is Installed.
     *
     * @param context The ematrix context object.
     * @param String[] The args .
     * @return Boolean Object - Returns true if Variant Configuration is Installed.
     *                        - Returns false if Variant Configuration is not Installed.
     * @since ProductLine X4
     */
     public Object isVariantConfigurationInstalled(Context context, String[] args) throws Exception
     {
           boolean isConfigurationInstall = FrameworkUtil.isSuiteRegistered(context,"appVersionVariantConfiguration",false,null,null);
           if(isConfigurationInstall)
           {
               return true;
           }
           else{
               return false;
           }
     }

     /**
      * Display the LifeCycle page of the Build in the Properties Page
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args - Holds the HashMap containing the following arguments
      *          paramMap - contains ObjectId, Old Value for product name and new value
      * @return String - returns HTML to display the LifeCycle page
      * @throws Exception if the operation fails
      * @since ProductLine X+4
      */

     public String getLifecycleStates(Context context,String[] args) throws Exception
     {
         String STR_BLANK = "";

         String retString =STR_BLANK;
         String objID =STR_BLANK;
         StringBuffer output = new StringBuffer(" ");
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         HashMap paramMap = (HashMap)programMap.get("paramMap");
         objID = (String)paramMap.get("objectId");

         DomainObject PartDom = new DomainObject(objID);
         PartDom.setId(objID);

         output.append("<HTML><body bgcolor=\"gray\">");

         output.append("<span name=\"States\" id=\"States\">");

         output.append("<script language='JavaScript'>");
         output.append("var iFrameSrc=\' <iframe name=\"Lifecycle\" src=\"emxLifecycleDialog.jsp?objectId="+objID+"\" width=\"100%\" height=\"100\" marginHeight=\"0\" scrolling=\"auto\" frameborder=\"0\"> </iframe>\';");
         output.append("var vSPAN=document.getElementById('States');");
         output.append("vSPAN.innerHTML =iFrameSrc;");
         output.append("</script>");
         output.append("</span>");
         output.append("</body></HTML>");
         retString = output.toString();

         return retString;
     }

     /**
      * To obtain the list of Object IDs to be excluded from the search for Add Existing of Builds
      * under Product Configuration context
      * @param context- the eMatrix <code>Context</code> object
      * @param args- holds the HashMap containing the following arguments
      * @return  StringList- consisting of the object ids to be excluded from the Search Results
      * @throws Exception if the operation fails
      */

     @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
     public StringList filterRelatedBuilds(Context context, String[] args)
            throws Exception {
        try {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strObjectId = (String) programMap.get("objectId");
            StringList tempStrList = new StringList();

            StringList tempStrList2 = new StringList();
            StringList tempStrList3 = new StringList();
            //Added for IR-032978
            StringList tempStrProdBuildList = new StringList();
            //End
            String strBuildProductId = "";

            String strParentId = "";

            DomainObject domObject = new DomainObject(strObjectId);
            String strTypeOfObject = domObject.getInfo(context,
                    DomainConstants.SELECT_TYPE);

            // get all the Orphan builds from data base
            StringList objSelect = new StringList(2);
            objSelect.addElement(DomainConstants.SELECT_ID);
            //Added for IR-032978
            String strProdConfIdSelectable  = "to["+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD+ "].from.id";
            String strProdIdSelectable = "to["+ ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+ "].from.id";
            objSelect.addElement(strProdConfIdSelectable);
            objSelect.addElement(strProdIdSelectable);
            //End
            // Get all the Builds from the database
            MapList lstBuildList = DomainObject.findObjects(context,
                    ProductLineConstants.TYPE_BUILDS,
                    DomainConstants.QUERY_WILDCARD, "", objSelect);

            String strBuildType = "";
            String strBuildId = "";
            String strBuildParentId = "";

            if (!(mxType.isOfParentType(context, strTypeOfObject,ProductLineConstants.TYPE_PRODUCTS))) {
                // If context is PC
                strParentId = domObject.getInfo(context,
                                "to["+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
                                     + "].from.id");

                if(EnoviaResourceBundle.getProperty(context,"eServiceSuiteProductLine.ProductConfigurationBuildAddExisting.DisplayAllProductRevBuilds").equals("true")){
                    return new StringList();
                }
                DomainObject domParentProduct = new DomainObject(strParentId);
                String strParentModelID = domParentProduct.getInfo(context,
                        "to[" + ProductLineConstants.RELATIONSHIP_PRODUCTS
                                + "].from.id");

                if(strParentModelID == null || "".equals(strParentModelID)){
                                strParentModelID = domParentProduct
                                .getInfo(context,
                                        "to[" + ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT
                                        + "].from.id");
                }

                for (int i = 0; i < lstBuildList.size(); i++) {
                    Map productMap = (Map) lstBuildList.get(i);
                    strBuildId = (String) productMap.get("id");

                    // Get the Builds from the database which are not connected
                    // to PC i.e. get all orphan builds and builds under Product
                    //Modified for IR-032978 - Start
//                    DomainObject domBuildId = new DomainObject(strBuildId);
//
//                    strBuildParentId = domBuildId.getInfo(context,"to["
//                                            + ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD
//                                            + "].from.id");
                    strBuildParentId =(String) productMap.get(strProdConfIdSelectable);
                    strBuildProductId = (String) productMap.get(strProdIdSelectable);

                    if (strBuildParentId == null || strBuildParentId.length() == 0) {
                        tempStrList2.add(strBuildId);
                        if(strBuildProductId != null && strBuildProductId.equalsIgnoreCase(strParentId)){
                            tempStrProdBuildList.add(strBuildId);
                        }
                        //Modifications for IR-032978 - End
                    } else {
                        // Builds connected to PC added to the list of objects
                        // which is to be removed from global list of builds
                        tempStrList3.add(strBuildId);

                    }

                }
                // remove the bulids which are already present to other product
                // context,we should have builds only under given product
                // context and orphan
                String strBuildId1 = "";
                for (int i = 0; i < tempStrList2.size(); i++) {
                    strBuildId1 = (String) tempStrList2.get(i);
                    if (!showForProdConfig(context, strBuildId1,
                            strParentModelID, strParentId)) {
                        tempStrList3.addElement(strBuildId1);
                    }
                }
                tempStrList3.addAll(getContextIPUBuilds(context,strParentId));
                //Added for IR-032978
                tempStrList3.removeAll(tempStrProdBuildList);
                //End
                return tempStrList3;

            } else {
                // In product Context..

                // Check for the setting in property file if true return blank
                // to display all the builds..
                if (EnoviaResourceBundle.getProperty(context,"eServiceSuiteProductLine.ProductBuildAddExisting.DisplayOrphanBuilds")
                        .equals("false")) {
                    return new StringList();
                } else {
                    String strParentModelID = domObject.getInfo(context, "to["
                            + ProductLineConstants.RELATIONSHIP_PRODUCTS
                            + "].from.id");

					if(strParentModelID == null || "".equals(strParentModelID)){
                                strParentModelID = domObject
                                .getInfo(context,
                                        "to[" + ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT
                                        + "].from.id");
                    }

                    for (int i = 0; i < lstBuildList.size(); i++) {
                        Map productMap = (Map) lstBuildList.get(i);
                        strBuildId = (String) productMap.get("id");

                        if (!showBuildsInProductContext(context, strParentModelID, strBuildId)) {
                            tempStrList.add(strBuildId);
                        }
                    }
                    tempStrList.addAll(getContextIPUBuilds(context,strObjectId));
                    return tempStrList;
                }

            }

        } catch (Exception e) {
            throw new FrameworkException(e);
        }

    }
    /**
     * This method is added as part of fix for BUG: 370415 -- This will return the ID's of all product context IPUs
     * @param context - Matrix context
     * @param strProdId - Product ID
     * @return Stringlist
     * @throws Exception
     */
     public static StringList getContextIPUBuilds(Context context,  String strProdId) throws Exception {
        StringList strList = new StringList();
        DomainObject objProd = new DomainObject(strProdId);
        String selectables = "from["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].to.id";
        StringList objList = objProd.getInfoList(context,selectables);
        strList = filterIPUAndUBOMBuilds(context,objList);
        return strList;
    }
     //Below two methods added as part of fix for IR-032817
     private static StringList filterIPUAndUBOMBuilds(Context context, StringList objList)throws Exception {
         StringList lst = new StringList();
         for(int j=0;j<objList.size();j++) {
         String strBuildId = (String) objList.get(j);
             lst.addAll(expandAndGetBuilds(context,strBuildId,new StringList()));
         }
        return lst;
    }

    private static StringList expandAndGetBuilds(Context context, String strBuildId, StringList processedList) throws Exception{
       StringList lst = new StringList();
       DomainObject domBuild = new DomainObject(strBuildId);
       processedList.add(strBuildId);
       StringList selectStmts = new StringList();
       selectStmts.addElement(DomainConstants.SELECT_ID);
       MapList list = domBuild.getRelatedObjects(context,
               ProductLineConstants.RELATIONSHIP_INTENDED_PRODUCT_UNIT+","+ProductLineConstants.RELATIONSHIP_UNITBOM,  // relationship pattern
               ProductLineConstants.TYPE_HARDWARE_BUILD,                  // object pattern
                                        selectStmts  ,                 // object selects
                                        null,              // relationship selects
                                        true,                        // to direction
                                        true,                       // from direction
                                        (short)1,                    // recursion level
                                        null,                        // object where clause
                                        null);
       for (int i = 0; i < list.size(); i++) {
           Map map = (Map)list.get(i);
           String strTemp = (String) map.get(ProductLineConstants.SELECT_ID);
           if(!processedList.contains(strTemp)){
               expandAndGetBuilds(context,strTemp,processedList);
           }
           lst.addElement(strTemp);

       }
        return lst;
    }

    /**
      * Returns true if build needs to be shown in product configuration context - Add existing operation.
      * @param context the eMatrix <code>Context</code> object.
      * @param strProdModelInfo contains model Id of the selected product.
      * @param strBuildId contains Build Id
      * @param strProductId contains product id of the product configuration.
      * @return boolean.
      * @since ProductLine X5
      * @throws Exception if the operation fails.
     */
    private boolean showForProdConfig(Context context, String strBuildId,
            String strProdModelInfo, String strProductId) {

        try {
            DomainObject objBuild = new DomainObject(strBuildId);
            StringList selectables = new StringList(2);
            String strBuildProdIdSelectable = "to["
                + ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
                + "].from.id";
            String strBuildModelIdSelectable = "to["
                + ProductLineConstants.RELATIONSHIP_MODEL_BUILD
                + "].from.id";
            selectables.add(strBuildProdIdSelectable);
            selectables.add(strBuildModelIdSelectable);
            Map map = objBuild.getInfo(context, selectables);
            String strBuildProdId = (String)map.get(strBuildProdIdSelectable);
            String strBuildModelInfo = (String) map.get(strBuildModelIdSelectable);
            if(!isUNTInstalled(context,new String[2])){
                if(strBuildProdId == null || strBuildProdId.length() == 0){
                    return true;
                }else if((strBuildProdId != null && strBuildProdId.length()>0)&&strBuildProdId.equalsIgnoreCase(strProductId)){
                    return true;
                }else {
                    return false;
                }
            }
            if (strBuildModelInfo == null && strBuildProdId == null) {
                return true;
            } else if (strBuildModelInfo.equals(strProdModelInfo)
                    && (strBuildProdId == null || strBuildProdId
                            .equals(strProductId))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if build needs to be shown in product context - Add existing operation.
     * @param context the eMatrix <code>Context</code> object.
     * @param strProdModelInfo contains model Id of the selected product.
     * @param strBuildId contains Build Id
     * @return boolean.
     * @since ProductLine X5
     * @throws Exception if the operation fails.
    */
    private boolean showBuildsInProductContext(Context context, String strProdModelInfo,
            String strBuildId) {
        try {
            DomainObject objBuild = new DomainObject(strBuildId);
            StringList selectables = new StringList(2);
            selectables.add("to["
                    + ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
                    + "].from.id");
            selectables.add("to["
                    + ProductLineConstants.RELATIONSHIP_MODEL_BUILD
                    + "].from.id");
            Map map = objBuild.getInfo(context, selectables);
            String strProdInfo = (String)map.get("to["
                    + ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD
                    + "].from.id");
            String strBuildModelInfo =(String) map.get("to["
                    + ProductLineConstants.RELATIONSHIP_MODEL_BUILD
                    + "].from.id");

            if ((strProdInfo == null || strProdInfo.length() == 0)
                    && (strBuildModelInfo == null || strBuildModelInfo.length() == 0)) {
                return true;
            } else if (strProdModelInfo.equals(strBuildModelInfo)
                    && (strProdInfo == null || strProdInfo.length() == 0)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    /**
      * Returns Type of build - Top level if Unit number exists for a build or Orphan build which is "Others"
      * @param context the eMatrix <code>Context</code> object.
      * @param args contains a packed HashMap containing objectId of object
      * @return String.
      * @since ProductLine X5
      * @throws Exception if the operation fails.
     */
     public String getBuilds(Context context, String args[]) throws Exception {
         String finalReturn   = " ";
         String buildId = args[0];
         String strType = args[1];
         DomainObject domPart = new DomainObject(buildId);
         String strBuildProductId = domPart.getInfo(context,"to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.id");
         if(strBuildProductId != null && !strBuildProductId.equalsIgnoreCase("null") && strBuildProductId.length()>0){
               return "Top Level";
          }
           return "Others";
      }

     /**
      * To obtain the list of Object IDs to be excluded from the search for Add Existing Product Actions
      *
      * @param context- the eMatrix <code>Context</code> object
      * @param args- holds the HashMap containing the following arguments
      * @return  StringList- consisting of the object ids to be excluded from the Search Results
      * @throws Exception if the operation fails
      * @author 3DPLM
      */

     public StringList excludeProductRevisionConnected(Context context, String[] args) throws Exception
     {
         Map programMap = (Map) JPO.unpackArgs(args);
         String strObjectIds = (String)programMap.get("objectId");
         //String strRelationship=(String)programMap.get("relName");
         String result=null;
         String strRelationship= "relationship_Products";
         String  strManagedRelationhip = "relationship_Ma";
         StringList excludeList= new StringList();
         StringTokenizer objIDs = new StringTokenizer(strObjectIds,",");
         String toType=null;
         String fromType=null;
         boolean bisTo=false;
         boolean bisFrom=false;

         StringList objSelect = new StringList(2);
         objSelect.addElement(DomainConstants.SELECT_ID);
         DomainObject domObjFeature = new DomainObject(strObjectIds);
         String strModelID = domObjFeature.getInfo(context,"from["+ProductLineConstants.RELATIONSHIP_MANAGED_MODEL+"].to.id");
         DomainObject domObjModel = new DomainObject(strModelID);

         toType=domObjModel.getInfo(context,"to["+PropertyUtil.getSchemaProperty(context,strRelationship)+"].from.type");
         fromType=domObjModel.getInfo(context,"from["+PropertyUtil.getSchemaProperty(context,strRelationship)+"].to.type");

         if(toType!=null){
             bisTo=true;
         }
         else{
             bisFrom=true;
         }
         MapList childObjects=domObjModel.getRelatedObjects(context,
                 PropertyUtil.getSchemaProperty(context,strRelationship),
                 toType==null?fromType:toType,
                 new StringList(DomainConstants.SELECT_ID),
                 null,
                 bisTo,
                 bisFrom,
                (short) 1,
                 DomainConstants.EMPTY_STRING,
                 DomainConstants.EMPTY_STRING);
         //Get all the Products from the database
         MapList lstProductsList = DomainObject.findObjects(context,
                                                    ProductLineConstants.TYPE_PRODUCTS,
                                                    DomainConstants.QUERY_WILDCARD,"",
                                                    objSelect);

        /* for(int cnt=0;cnt<lstProductsList.size();cnt++){

             Map prodMap = (Map)lstProductsList.get(cnt);
             String prodId = (String)prodMap.get(DomainConstants.SELECT_ID);

             for(int i=0;i<childObjects.size();i++){

                 Map tempMap=(Map)childObjects.get(i);
                 String tempID = (String)tempMap.get(DomainConstants.SELECT_ID);
                 if(!tempID.equals(prodId)){
                     excludeList.add(prodId);
                 }

             }
         }*/

         StringList childList= new StringList();
         for(int iCount=0;iCount<childObjects.size();iCount++)
         {

             Map tempMap=(Map)childObjects.get(iCount);
             String tempID = (String)tempMap.get(DomainConstants.SELECT_ID);
             childList.add(tempID);

         }

         for(int icnt=0;icnt<lstProductsList.size();icnt++)
         {

             Map prodMap = (Map)lstProductsList.get(icnt);
             String prodId = (String)prodMap.get(DomainConstants.SELECT_ID);

             if(!childList.contains(prodId))
             {
                 excludeList.add(prodId);
             }

        }


         excludeList.add(strObjectIds);
         return excludeList;
     }


     /**
      * To obtain the list of Object IDs to be excluded from the search for Models
      *
      * @param context- the eMatrix <code>Context</code> object
      * @param args- holds the HashMap containing the following arguments
      * @return  StringList- consisting of the object ids to be excluded from the Search Results
      * @throws Exception if the operation fails
      * @author 3DPLM
      */

     public StringList excludeManagedModels(Context context, String[] args) throws Exception
     {
         StringList excludeList= new StringList();
         StringList objSelect = new StringList(2);
         objSelect.addElement(DomainConstants.SELECT_ID);
         Map programMap = (Map) JPO.unpackArgs(args);
         String strObjectIds = (String)programMap.get("ObjectId");


         //Get all the Products from the database
         MapList lstProductsList = DomainObject.findObjects(context,
                                                    ProductLineConstants.TYPE_MODEL,
                                                    DomainConstants.QUERY_WILDCARD,"",
                                                    objSelect);
         for(int cnt=0;cnt<lstProductsList.size();cnt++){

             Map modelMap = (Map)lstProductsList.get(cnt);
             String modelId = (String)modelMap.get(DomainConstants.SELECT_ID);

             DomainObject domModel = new DomainObject(modelId);
             String productPlatformId = domModel.getInfo(context,"to["+ProductLineConstants.RELATIONSHIP_MANAGED_MODEL+"].from.id");
             if(productPlatformId!=null){
                 excludeList.add(modelId);
             }

         }


         if(strObjectIds!=null && !strObjectIds.equals("") && !strObjectIds.equals("null"))
         {
             excludeList.add(strObjectIds);
         }

         //excludeList.add(strObjectIds);
        return excludeList;
     }


     public boolean isUNTInstalled(Context context,String args[])
     {
         return  FrameworkUtil.isSuiteRegistered(context,"appInstallTypeUnitTracking",false,null,null);
     }

     /**
      * To exclude the Models that are connected to Product Lines
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
     @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
     public StringList excludeConnectedModels(Context context, String[] args) throws Exception
     {
    	 StringList excludeList  = new StringList();
     	 StringList objSelect = new StringList(2);
         objSelect.addElement(DomainConstants.SELECT_ID);
         String strWhereExp = "to["+ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS+"].from.type == '"+ProductLineConstants.TYPE_PRODUCT_LINE+"'";

         MapList modelExcludeList = DomainObject.findObjects(context,ProductLineConstants.TYPE_MODEL,DomainConstants.QUERY_WILDCARD,strWhereExp,objSelect);
         for(int i=0;i<modelExcludeList.size();i++){
             Map tempMap=(Map)modelExcludeList.get(i);
             excludeList.add(tempMap.get(DomainConstants.SELECT_ID));
         }
         return excludeList;
     }

     /**
	     *
		 *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds the following input arguments
	     *        0 - id of the business object
	     *        1 - Expression
	     * @return String
	     * @throws Exception if the operation fails		     *
	     */

	    public String getSelecatableVal(Context context, String args[]) throws Exception
	    {
	    	//return MqlUtil.mqlCommand(context, "print bus "+ args[0] +" select "+args[1]+" dump |");
	    	return MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",args[0],args[1],ProductLineConstants.DELIMITER_PIPE);
	    }

		/**
		* Use as include context program for MP and Products Effectivity commands
		*/
	    @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getModelContexts(Context context, String[] args) throws Exception {
            try {
                MapList returnMapList = new MapList();
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                String objectId = (String) programMap.get("objectId");
                if (objectId!=null && !objectId.isEmpty()) {
                    Map<String,String> tempMap = new HashMap<String,String>();
                    tempMap.put(DomainConstants.SELECT_ID, objectId);
                    returnMapList.add(tempMap);
                }
                return returnMapList;
			} catch (Exception e) {
                throw e;
            }
        }


        /**
	     * Method for coonet the company to PL
	     * @param context
	     * @param args
	     * @return
	     * @throws Exception
	     */
	    public int connectCompanyName(Context context, String[] args)
        throws Exception {
		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map paramMap = (HashMap) programMap.get("paramMap");
		Map requestMap = (HashMap) programMap.get("requestMap");
		String strFeatureId = (String) paramMap.get(OBJECT_ID);

		String strOldCompanyName = (String) paramMap.get("Old value");
		String strNewCompanyName = (String) paramMap.get("New Value");
		String strNewCompanyId = (String) paramMap.get("New OID");
		String strOldCompanyOID = (String) paramMap.get("Old OID");
		// Added this for Bug 371941 to get the count of all the Design
		// Responsibilty Rows
		String count = (String) paramMap.get("count");

		if (strOldCompanyName == null) {
			strOldCompanyName = "";
		}
		if (strNewCompanyName == null) {
			strNewCompanyName = "";
		}
		if (strNewCompanyId == null) {
			strNewCompanyId = "";
		}
		if (strOldCompanyOID == null) {
			strOldCompanyOID = "";
		}
		if (strNewCompanyId == null || strNewCompanyId.length() == 0) {
			//strNewCompanyId = (String) paramMap.get("New Value");
			com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person
			.getPerson(context);
	        String companyId = person.getCompanyId(context);
	        if(strNewCompanyName.equals(person.getCompany(context).getName())){
			// setId(companyId);
	        	strNewCompanyId=companyId;
		     }
	        }

		if (strOldCompanyOID.equals(strNewCompanyId)) {
			strNewCompanyId = (String) paramMap.get("New Value");
		}

		if (strNewCompanyId == null) {
			strNewCompanyId = "";
		}

		// Added this condition for Bug 371941 to get the New Organization id of
		// each row based on the count
		if (strNewCompanyId.equals("") && count != null
				&& !count.equalsIgnoreCase("-1")) {
			String[] nCompanyOID = (String[]) requestMap
					.get("Company" + count);
			// Mx377923WIM -added null and empty check for nOrganizationOID
			if ((nCompanyOID != null) && (nCompanyOID.length > 0)) {
				strNewCompanyId = nCompanyOID[0];
			}
		}
		// Start of Add by Enovia MatrixOne for Bug # 311803 on 09-Dec-05
		String strOldCompanyId = "";
		String strObjID = (String) paramMap.get("objectId");
		DomainObject domObj = DomainObject.newInstance(context, strObjID);
		StringList slBusTypes = new StringList();
		slBusTypes.addElement(DomainConstants.SELECT_ID);
		ContextUtil.pushContext(context);

		Map mDesignResponsibility = domObj.getRelatedObject(context,
				ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT_LINES, false,
				slBusTypes, null);

		if (!((mDesignResponsibility == null) || (mDesignResponsibility
				.equals(null)))) {
			String strDesRespObj = (mDesignResponsibility
					.get(DomainConstants.SELECT_ID)).toString();
			strOldCompanyId = strDesRespObj;
		} else {
			strOldCompanyId = (String) paramMap.get("Old OID");
		}
		ContextUtil.popContext(context);

		boolean bHasReadAccess;

		try {
			if (strOldCompanyId == null
					|| strOldCompanyId.equals("null")
					|| strOldCompanyId.equals(""))
				bHasReadAccess = true;
			else
				bHasReadAccess = emxProduct_mxJPO.hasAccessOnProject(context,
						strOldCompanyId);
		} catch (Exception e) {
			bHasReadAccess = false;
		}

		if (bHasReadAccess) {
			// End of Add by Enovia MatrixOne for Bug # 311803 on 09-Dec-05

			// fix issue with strOldOrganizationId being null and throwing null
			// ptr errors on check for empty string
			if (strOldCompanyId == null)
				strOldCompanyId = "";

			// Begin of add by Enovia MatrixOne on 18-Apr-05 for Bug# 300548
			if (!strNewCompanyId.equals("")
					&& !strOldCompanyId.equals("")
					&& strOldCompanyId.equals(strNewCompanyId)) {
				return 0;
			} else {
				// End of add by Enovia MatrixOne on 18-Apr-05 for Bug# 300548
				String strDesignRespRelationship = ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT_LINES;

				setId(strFeatureId);
				List organizationList = new MapList();
				if (strOldCompanyName == null
						|| "null".equals(strOldCompanyName))
					strOldCompanyName = "";

				List lstObjectSelects = new StringList(
						DomainConstants.SELECT_ID);
				List lstRelSelects = new StringList(
						DomainConstants.SELECT_RELATIONSHIP_ID);

				// Begin of Modify by Praveen, Enovia MatrixOne for Bug #300094
				// 03/15/2005
				String strOrganizationType = DomainConstants.QUERY_WILDCARD;
				// End of Modify by Praveen, Enovia MatrixOne for Bug #300094
				// 03/15/2005

				// Modified by Enovia MatrixOne on 18-Apr-05 for Bug# 300548
				StringBuffer sbWhereCondition = new StringBuffer(25);
				if (strOldCompanyId != null
						&& !strOldCompanyId.equals("")) {
					sbWhereCondition = sbWhereCondition
							.append(DomainConstants.SELECT_ID);
					sbWhereCondition = sbWhereCondition.append("==");
					sbWhereCondition = sbWhereCondition.append("\"");
					sbWhereCondition = sbWhereCondition
							.append(strOldCompanyId);
					sbWhereCondition = sbWhereCondition.append("\"");
				}

				// Added for RDO Fix
				// Changing the context to super user
				ContextUtil.pushContext(context);

				organizationList = getRelatedObjects(context,
						strDesignRespRelationship, strOrganizationType,
						(StringList) lstObjectSelects,
						(StringList) lstRelSelects, true, true, (short) 1,
						sbWhereCondition.toString(),
						DomainConstants.EMPTY_STRING);

				if (organizationList != null && !organizationList.isEmpty()) {
					String strRelId = (String) ((Map) organizationList.get(0))
							.get(DomainConstants.SELECT_RELATIONSHIP_ID);
					// Begin of add by Yukthesh, Enovia MatrixOne for Bug
					// #311540 on Nov 10,2005
					// Turn off the matrix triggers
					MqlUtil.mqlCommand(context, "trigger off", true);

					try {
						// Disconnecting the existing relationship
						DomainRelationship.disconnect(context, strRelId);
					} finally {
						// Turn on the matrix triggers
						MqlUtil.mqlCommand(context, "trigger on", true);
					}
					// End of add by Yukthesh, Enovia MatrixOne for Bug #311540
					// on Nov 10,2005
				}

				// Added for RDO Fix
				// Changing the context back to the context user
				ContextUtil.popContext(context);

				if (strNewCompanyId == null
						|| "null".equals(strNewCompanyId))
					strNewCompanyId = "";

				if (!strNewCompanyId.equals("")) {
			        setId(strNewCompanyId);
					DomainObject domainObjectToType = newInstance(context,
							strFeatureId);

					// Added for RDO Fix
					// Changing the context to super user
					ContextUtil.pushContext(context);

					if(mxType.isOfParentType(context,domainObjectToType.getInfo(context,DomainObject.SELECT_TYPE),
							ProductLineConstants.TYPE_PRODUCTS)){
						strDesignRespRelationship = ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT;
					}

					DomainRelationship.connect(context, this,
							strDesignRespRelationship, domainObjectToType);


					// Added for RDO Fix
					// Changing the context back to the context user
					ContextUtil.popContext(context);
				}

				// Added by Enovia MatrixOne on 18-Apr-05 for Bug# 300548
			}
			return 0;

			// Start of Add by Enovia MatrixOne for Bug # 311803 on 09-Dec-05
		}// end of if for check of read access
		else {
			return 0;
		}
		// End of Add by Enovia MatrixOne for Bug # 311803 on 09-Dec-05

}
    /**
	     * method to exclude  the productline which are already connected
	     * @param context
	     * @param args
	     * @return
	     * @throws Exception
	     */
	    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	    public StringList excludeAvailableProductLine(Context context, String [] args)
		   throws FrameworkException
		   {
	    	   StringList finalList=new StringList();
			   StringList strList=new StringList();

			   try{
				   Map programMap = (Map) JPO.unpackArgs(args);
			       String strSourceObjectId = (String) programMap.get("objectId");
			       DomainObject domContextObj = new DomainObject(strSourceObjectId);
			       String txtType = domContextObj.getInfo(context,DomainConstants.SELECT_TYPE);
			       String strWhereExp = DomainConstants.EMPTY_STRING;
			       String strRelPattern = "";
			       short level = 1;

			      String strObjectPattern = ProductLineConstants.TYPE_PRODUCT_LINE;
			      strRelPattern = ProductLineConstants.RELATIONSHIP_SUB_PRODUCT_LINES;


			       StringList objectSelects = new StringList(DomainObject.SELECT_ID);
			       StringList relSelects = new StringList(DomainRelationship.SELECT_ID);

			       short limit = 0;
			       MapList relatedFromPlList = new MapList();

			       relatedFromPlList = domContextObj.getRelatedObjects(context,
															               strRelPattern,
															               strObjectPattern,
															               objectSelects,
															               relSelects,
															               false,
															               true,
															               level,
															               strWhereExp,
															               strWhereExp,
															               limit);

			       //add the context PL
			       finalList.add(strSourceObjectId);

			       for(int i=0;i<relatedFromPlList.size();i++)
			       {
			           Map mapFeatureObj = (Map) relatedFromPlList.get(i);
			           if(mapFeatureObj.containsKey(objectSelects.get(0)))
			           {
			               Object idsObject = mapFeatureObj.get(objectSelects.get(0));
			               strList=ProductLineCommon.convertObjToStringList(context,idsObject);
			               finalList.addAll(strList);
			           }
			       }
			   }
			   catch (Exception e) {
					throw new FrameworkException(e);
			   }

		       return finalList;
		   }
	 /**
	  * Method to check if ECHproduct is installed
	  *
	  * @param context
	  * @param args
	  * @return	true - if ECH is installed
	  * 		false - if ECH is not Installed
	  * @exception throws FrameworkException
	  * @since R213
	  */
     public boolean isECHInstalled(Context context,String args[]) throws FrameworkException
     {
         return  FrameworkUtil.isSuiteRegistered(context,"appVersionEnterpriseChange",false,null,null);
     }
     /**
    This column JPO method is used to get the RDO for a object. If the context
    * user has read access on the RDO object then it is hyperlinked
    * on SB page otherwise only name is returned. Also if the context user
    * doesn't have the show access on the RDo object then context is changed
    * to super user to retrieve the RDO name.    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - String array containing following packed HashMap
    *                       with following elements:
    *                       lstobjectList -  list containig the object id.
    * @return String - The program HTML output containing the RDO name and Link.
    * @throws Exception if the operation fails
    * @since R213
      */
     public Vector getDesignResponsibilitySB(Context context, String[] args) throws Exception{
    	 String strPolicyProduct = PropertyUtil.getSchemaProperty(context,SYMBOLIC_policy_Product);

    	 String strStateReview = FrameworkUtil.lookupStateName(      context,
    			 strPolicyProduct,
    			 SYMB_state_Review
    			 );
    	 String strStateRelease = FrameworkUtil.lookupStateName(     context,
    			 strPolicyProduct,
    			 SYMB_state_Release
    			 );
    	 String strStateObsolete= FrameworkUtil.lookupStateName(     context,
    			 strPolicyProduct,
    			 SYMB_state_Obsolete
    			 );
    	 //Get the object id of the context object
    	 Map programMap = (HashMap) JPO.unpackArgs(args);
    	 Map paramMap = (Map) programMap.get("paramList");
    	 List lstobjectList = (MapList) programMap.get("objectList");
    	 String strMode = (String)paramMap.get("mode");
    	 String suiteDir = (String) paramMap.get("SuiteDirectory");
    	 String suiteKey = (String) paramMap.get("suiteKey");
    	 Map objectMap = null;
    	 Vector result=new  Vector();
    	 Map fieldMap = (HashMap) programMap.get("columnMap");
    	 String strFieldName = (String)fieldMap.get("name");
    	 //Form the select expressions for getting the RDO name and RDO id.
    	 StringBuffer sbRDONameSelect  = new StringBuffer("to[");
    	 sbRDONameSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
    	 sbRDONameSelect.append("].from.");
    	 sbRDONameSelect.append(DomainConstants.SELECT_NAME);
    	 StringBuffer sbRDOIdSelect  = new StringBuffer("to[");
    	 sbRDOIdSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
    	 sbRDOIdSelect.append("].from.");
    	 sbRDOIdSelect.append(DomainConstants.SELECT_ID);
    	 StringBuffer sbRDOTypeSelect  = new StringBuffer("to[");
    	 sbRDOTypeSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
    	 sbRDOTypeSelect.append("].from.");
    	 sbRDOTypeSelect.append(DomainConstants.SELECT_TYPE);
    	 //CODE CHANGES
    	 String exportFormat = "";
    	 HashMap requestMap = (HashMap) programMap.get("requestMap");
    	 if(requestMap!=null && requestMap.containsKey("reportFormat")){
    		 exportFormat = (String)requestMap.get("reportFormat");
    	 } else if (paramMap!=null && paramMap.containsKey("reportFormat")){
    		 exportFormat = (String)paramMap.get("reportFormat");
    	 }
    	 StringList lstObjSelects = new StringList();
    	 lstObjSelects.add(sbRDONameSelect.toString());
    	 lstObjSelects.add(sbRDOIdSelect.toString());
    	 lstObjSelects.add(sbRDOTypeSelect.toString());
    	 String strRDOId = "";
    	 String strRDOName = "";
    	 StringBuffer sbBuffer  = new StringBuffer();
    	 //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 Date:4/13/2005
    	 String strTempIcon = DomainConstants.EMPTY_STRING;
    	 String strRDOType =  DomainConstants.EMPTY_STRING;
    	 String strTypeIcon = DomainConstants.EMPTY_STRING;
    	 for (int j=0; j<lstobjectList.size(); j++)
    	 {
    		 objectMap = (Map) lstobjectList.get(j);
    		 String strObjectId=(String)objectMap.get(DomainConstants.SELECT_ID);
    		 //Get the RDO id and name by changing the context to super user
    		 DomainObject domObj = DomainObject.newInstance(context, strObjectId);
    		 ContextUtil.pushContext(context);
    		 Map mapRDO = (Map)domObj.getInfo(context,lstObjSelects);
    		 ContextUtil.popContext(context);
    		 //If RDO is set for this object then check whether the context user has read
    		 //access on the RDO object. If yes then hyperlink the RDO name to its
    		 if(mapRDO!=null&&mapRDO.size()>0){
    			 strRDOName = (String) mapRDO.get(sbRDONameSelect.toString());
    			 if (mapRDO.get(sbRDOIdSelect.toString()) instanceof StringList)
    			 {
    				 StringList strRDOListId = (StringList) mapRDO.get(sbRDOIdSelect.toString());
    				 strRDOId =  (String)strRDOListId.get(0);
    			 } else {
    				 strRDOId = (String) mapRDO.get(sbRDOIdSelect.toString());

    			 }
    			 strRDOType = (String) mapRDO.get(sbRDOTypeSelect.toString());
    		 }else{
    			 strRDOName = "";
    			 strRDOId = "";
    			 strRDOType = "";
    		 }
    		 if (strRDOType!=null && !strRDOType.equals("") && !strRDOType.equalsIgnoreCase("null")){
    			 strTypeIcon = UINavigatorUtil.getTypeIconFromCache(strRDOType);
    			 strTypeIcon = "images/"+strTypeIcon;            
    	     }
    		 if(strRDOName!=null && !strRDOName.equals("") && !strRDOName.equalsIgnoreCase("null")){
    			 boolean bHasReadAccess =false;
    			 //Checking the access to Edit Design Responsibility
    			 String strCtxUser = context.getUser();
    			 String strOwner = (String)domObj.getInfo(context,DomainConstants.SELECT_OWNER);
    			 boolean hasRoleProductManager  = false;
    			 boolean hasRoleSystemEngineer  = false;
    			 boolean bIsOwner               = false;
    			 Person ctxPerson = new Person (strCtxUser);
    			 hasRoleProductManager = ctxPerson.isAssigned(context,"Product Manager");
    			 hasRoleSystemEngineer = ctxPerson.isAssigned(context,"System Engineer");
    			 if ( strCtxUser != null && !"".equals(strCtxUser)) {
    				 if (strOwner!= null && !"".equals(strOwner)) {
    					 if (strOwner.equals(strCtxUser)) {
    						 bIsOwner = true;
    					 }
    				 }
    			 }
    			 
    			 try {
    				 if ( (strRDOId==null || strRDOId.equals("")) &&
    						 ( bIsOwner || hasRoleProductManager || hasRoleSystemEngineer )){
    					 bHasReadAccess = true;
    				 }
    				 else {
    					 boolean hasAccessOnProject = emxProduct_mxJPO.hasAccessOnProject(context,strRDOId);
    					 if (hasAccessOnProject && ( bIsOwner || hasRoleProductManager || hasRoleSystemEngineer ))
    					 {
    						 bHasReadAccess = true;
    					 }
    				 }
    			 } catch (Exception e) {
    				 bHasReadAccess = false;
    			 }
    			 
    			 if(bHasReadAccess){
    				 //CODE changes
    				 StringBuffer sbHref  = new StringBuffer();
    				 if("CSV".equalsIgnoreCase(exportFormat)){
    					 sbHref.append(strRDOName);
    				 }else{
    					 //XSSOK- Deprecated
    					 sbHref.append("<a href=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?emxSuiteDirectory=");
    					 sbHref.append(suiteDir);
    					 sbHref.append("&amp;suiteKey=");
    					 sbHref.append(suiteKey);
    					 sbHref.append("&amp;objectId=");
    					 sbHref.append(strRDOId);
    					 sbHref.append("', '450', '300', 'true', 'popup')\">");
    					 sbHref.append(" <img border=\"0\" src=\"");
    					 sbHref.append(strTypeIcon);
    					 sbHref.append("\" /> ");
    					 sbHref.append(XSSUtil.encodeForHTML(context,strRDOName));
    					 sbHref.append("</a>");
    				 }
					 result.add(sbHref.toString());
    			 }else{
    				 sbBuffer.delete(0, sbBuffer.length());
    				 //CODE changes
    				 if("CSV".equalsIgnoreCase(exportFormat)){
    					 sbBuffer.append(strRDOName);
    				 }else{
    					 //XSSOK- Deprecated
    					 sbBuffer.append("<img border=\"0\" src=\"");
    					 sbBuffer.append(strTypeIcon);
    					 sbBuffer.append("\"></img>");
    					 sbBuffer.append(SYMB_SPACE);
    					 sbBuffer.append(strRDOName);
    				 }
    				 result.add(sbBuffer.toString());
    				 //End of modify for bug 301411
    			 }
    		 }
    		 else{
    			 result.add("");

    		 }
    	 }
    	 return result;
     }
     
     /**
      * This method used to enable the particular RDO column cell for edit on the basis of access of user
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
public StringList editAccess(Context context, String[] args)throws Exception{
	String strPolicyProduct = PropertyUtil.getSchemaProperty(context,SYMBOLIC_policy_Product);

    String strStateReview = FrameworkUtil.lookupStateName(      context,
                                                                strPolicyProduct,
                                                                SYMB_state_Review
                                                          );
    String strStateRelease = FrameworkUtil.lookupStateName(     context,
                                                                strPolicyProduct,
                                                                SYMB_state_Release
                                                          );
    String strStateObsolete= FrameworkUtil.lookupStateName(     context,
                                                                strPolicyProduct,
                                                                SYMB_state_Obsolete
                                             );
    //Get the object id of the context object
         Map programMap = (HashMap) JPO.unpackArgs(args);
         Map paramMap = (Map) programMap.get("paramList");
         List lstobjectList = (MapList) programMap.get("objectList");
        Map objectMap = null;
        StringList result=new  StringList();
//Form the select expressions for getting the RDO name and RDO id.
StringBuffer sbRDONameSelect  = new StringBuffer("to[");
sbRDONameSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
sbRDONameSelect.append("].from.");
sbRDONameSelect.append(DomainConstants.SELECT_NAME);

StringBuffer sbRDOIdSelect  = new StringBuffer("to[");
sbRDOIdSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
sbRDOIdSelect.append("].from.");
sbRDOIdSelect.append(DomainConstants.SELECT_ID);
StringBuffer sbRDOTypeSelect  = new StringBuffer("to[");
sbRDOTypeSelect.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
sbRDOTypeSelect.append("].from.");
sbRDOTypeSelect.append(DomainConstants.SELECT_TYPE);
String exportFormat = "";
HashMap requestMap = (HashMap) programMap.get("requestMap");
if(requestMap!=null && requestMap.containsKey("reportFormat")){
	exportFormat = (String)requestMap.get("reportFormat");
}

StringList lstObjSelects = new StringList();
lstObjSelects.add(sbRDONameSelect.toString());
lstObjSelects.add(sbRDOIdSelect.toString());
        lstObjSelects.add(sbRDOTypeSelect.toString());
String strRDOId = "";
String strRDOName = "";
        StringBuffer sbBuffer  = new StringBuffer();
        //Begin of Add by Rashmi, Enovia MatrixOne for bug 301411 Date:4/13/2005
        String strTempIcon = DomainConstants.EMPTY_STRING;
        String strRDOType =  DomainConstants.EMPTY_STRING;
        String strTypeIcon = DomainConstants.EMPTY_STRING;
for (int j=0; j<lstobjectList.size(); j++)
{
objectMap = (Map) lstobjectList.get(j);
String strObjectId=(String)objectMap.get(DomainConstants.SELECT_ID);
//Get the RDO id and name by changing the context to super user
DomainObject domObj = DomainObject.newInstance(context, strObjectId);
ContextUtil.pushContext(context);
Map mapRDO = (Map)domObj.getInfo(context,lstObjSelects);
ContextUtil.popContext(context);
//If RDO is set for this object then check whether the context user has read
//access on the RDO object. If yes then hyperlink the RDO name to its
    if(mapRDO!=null&&mapRDO.size()>0){
        strRDOName = (String) mapRDO.get(sbRDONameSelect.toString());
        //Begin of modify by Enovia MatrixOne on 1-June-05 for bug 304576 reopened
        if (mapRDO.get(sbRDOIdSelect.toString()) instanceof StringList){
            StringList strRDOListId = (StringList) mapRDO.get(sbRDOIdSelect.toString());
            strRDOId =  (String)strRDOListId.get(0);
        } else {
            strRDOId = (String) mapRDO.get(sbRDOIdSelect.toString());
        }
      strRDOType = (String) mapRDO.get(sbRDOTypeSelect.toString());
    }
    if(strRDOName==null || strRDOName.equalsIgnoreCase("null") || strRDOName.equals("")){
        strRDOName = "";
        strRDOId = "";
        strRDOType = "";
    }
    boolean bHasReadAccess =false;
   // Checking the access to Edit Design Responsibility
    String strCtxUser = context.getUser();
    String strOwner = (String)domObj.getInfo(context,DomainConstants.SELECT_OWNER);
    boolean hasRoleProductManager  = false;
    boolean hasRoleSystemEngineer  = false;
    boolean bIsOwner               = false;
    Person ctxPerson = new Person (strCtxUser);
    hasRoleProductManager = ctxPerson.isAssigned(context,"Product Manager");
    hasRoleSystemEngineer = ctxPerson.isAssigned(context,"System Engineer");
    if ( strCtxUser != null && !"".equals(strCtxUser))
    {
        if (strOwner!= null && !"".equals(strOwner))
        {
            if (strOwner.equals(strCtxUser))
            {
                bIsOwner = true;
            }
        }
    }
    try
    {

        if ( (strRDOId==null || strRDOId.equals("")) &&
            ( bIsOwner || hasRoleProductManager || hasRoleSystemEngineer )){
            bHasReadAccess = true;
        }
        else {
            boolean hasAccessOnProject = emxProduct_mxJPO.hasAccessOnProject(context,strRDOId);
            if (hasAccessOnProject && ( bIsOwner || hasRoleProductManager || hasRoleSystemEngineer ))
            {
                bHasReadAccess = true;
            }
        }
    } catch (Exception e) {
        bHasReadAccess = false;
    }
    // for making StringList for edit access
    if (!bHasReadAccess)
    {
       result.add(false);
    } else {
    	result.add(true);
    }
}
	return result;
}

/**
 * Returns true if the PRG is installed otherwise false.
 * @mx.whereUsed This method will be called from part property pages
 * @mx.summary   This method check whether PRG is installed or not, this method can be used as access program to show/hide the columns from Product summary page
 * @param context the eMatrix <code>Context</code> object.
 * @return boolean true or false based condition.
 * @throws Exception if the operation fails.
 * @since R215
 */
	public boolean isPRGInstalled(Context context,String[] args) throws Exception
	{     
		boolean isPRGInstalled = ProductLineUtil.isPRGInstalled(context);
	    return  isPRGInstalled;
	}

	public List getHigherRevisionIconForProductContext(Context context,
			String[] args) throws Exception {

		Map programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
		Map paramList = (HashMap) programMap.get("paramList");
		String reportFormat = (String) paramList.get("reportFormat");
		String strLanguage = context.getSession().getLanguage();
		int iNumOfObjects = relBusObjPageList.size();
		// The List to be returned
		List lstHigherRevExists = new Vector(iNumOfObjects);
		Map objectMap = new HashMap();
		Iterator objectListItr = relBusObjPageList.iterator();
		int iCount;
		// Reading the tooltip from property file.
		String strTooltipHigherRevExists =	EnoviaResourceBundle.getProperty(context, SUITE_KEY,ICON_TOOLTIP_HIGHER_REVISION_EXISTS,context.getSession().getLanguage());
		String strHigherRevisionIconTag = "";
		String strIcon = EnoviaResourceBundle.getProperty(context,"emxComponents.HigherRevisionImage");
		// Iterating through the list of objects to generate the program HTML
		// output for each object in the table
		for (iCount = 0; iCount < iNumOfObjects; iCount++) {		
			while (objectListItr.hasNext()) {
				objectMap = (Map) objectListItr.next();
				String nextRevExist = (String) objectMap.get("next");

				if (nextRevExist!=null
						&& !("".equals(nextRevExist))) {
					if (reportFormat != null
							&& !("null".equalsIgnoreCase(reportFormat))
							&& reportFormat.length() > 0) {
						lstHigherRevExists.add(strTooltipHigherRevExists);
					} else {
						strHigherRevisionIconTag = "<img src=\"../common/images/"
								+ strIcon
								+ "\" border=\"0\"  align=\"middle\" "
								+ "TITLE=\""
								+ " "
								+ XSSUtil.encodeForXML(context,strTooltipHigherRevExists)
								+ "\"" + "/>";
					}
				}else if(objectMap.containsKey("parentLevel")){
				String  id=(String)	objectMap.get(DomainConstants.SELECT_ID);
					DomainObject domObj = new DomainObject(id);
					if(!domObj.isLastRevision(context)){					
							if (reportFormat != null
									&& !("null".equalsIgnoreCase(reportFormat))
									&& reportFormat.length() > 0) {
								lstHigherRevExists.add(strTooltipHigherRevExists);
							} else {
								strHigherRevisionIconTag = "<img src=\"../common/images/"
										+ strIcon
										+ "\" border=\"0\"  align=\"middle\" "
										+ "TITLE=\""
										+ " "
										+ XSSUtil.encodeForXML(context,strTooltipHigherRevExists)
										+ "\"" + "/>";
							}	
					}
					
				} else {
					strHigherRevisionIconTag = " ";
				}
				lstHigherRevExists.add(strHigherRevisionIconTag);
			}

		}
		return lstHigherRevExists;
	}	
	
	/**
  	 * 
  	 * Checks for Mobile Mode And FTR/PLC License. Returns True if Mobile Mode is Enabled or User doesn't have either of Licenses (FTR or PLC). 
  	 * @param context 
  	 * 			the eMatrix <code>Context</code> object
  	 * @param args 
  	 * 			string array containing packed arguments.
  	 * @return boolean
  	 * @throws FrameworkException 
  	 * 			If the operation fails
  	 */
  	 public boolean isMobileModeEnabled (Context context, String args[]) throws FrameworkException { 
  		boolean isMobileModeEnabled = UINavigatorUtil.isMobile(context);
  		boolean isFTRUser=false;
  		String Licenses[] = {"ENO_FTR_TP","ENO_PLC_TP"};
	    
  		try {
  			FrameworkLicenseUtil.checkLicenseReserved(context,Licenses);
		    isFTRUser = true;
		}catch (Exception e){
			isFTRUser = false;
		}
		
  		if(isMobileModeEnabled||!isFTRUser){
  			return true;
  		}
  		return false;
  		 
  	 }

  	/**
  	 * 
  	 *Checks for Mobile Mode And FTR/PLC License. Returns True if Mobile Mode is Disabled And User has either of Licenses (FTR or PLC).
  	 * @param context 
  	 * 			the eMatrix <code>Context</code> object
  	 * @param args 
  	 * 			string array containing packed arguments.
  	 * @return boolean
  	 * @throws FrameworkException 
  	 * 			If the operation fails
  	 */
  	 public boolean isMobileModeDisabled (Context context, String args[]) throws FrameworkException { 
  		boolean isMobileModeDisabled = !UINavigatorUtil.isMobile(context);
  		boolean isFTRUser=false;
  		String Licenses[] = {"ENO_FTR_TP","ENO_PLC_TP"};
  		try {
  			FrameworkLicenseUtil.checkLicenseReserved(context,Licenses);
		    isFTRUser = true;
		}catch (Exception e){
			isFTRUser = false;
		}
		
  		if(isMobileModeDisabled&&isFTRUser){
  			return true;
  		}
  		return false;
  	 }
  	 
  	/**
   	 * 
   	 *Checks for Mobile Mode,PRG insatalled and FTR/PLC License. Returns True if PRG is installed and Mobile Mode is Enabled or
   	 *User doesn't have either of Licenses (FTR or PLC).
   	 * @param context 
   	 * 			the eMatrix <code>Context</code> object
   	 * @param args 
   	 * 			string array containing packed arguments.
   	 * @return boolean
   	 * @throws FrameworkException 
   	 * 			If the operation fails
   	 */
   	 public boolean isPRGInstalledMobileEnabled (Context context, String args[]) throws FrameworkException {
   		 try {
   			boolean bDisplay;
   			boolean isMobileModeEnabled = UINavigatorUtil.isMobile(context);
   	  		boolean isFTRUser=false;
   	  		String Licenses[] = {"ENO_FTR_TP","ENO_PLC_TP"};   		    
   	  		try {
   	  			FrameworkLicenseUtil.checkLicenseReserved(context,Licenses);
   			    isFTRUser = true;
   			}catch (Exception e){
   				isFTRUser = false;
   			}
   			
   	  		if(isMobileModeEnabled||!isFTRUser){
   	  		bDisplay = true;
   	  		}else{
   	  		   bDisplay = false;
   	  		}
   	  	  			
   	  	return isPRGInstalled(context, args) && bDisplay;
   	  	
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		} 
   	 }

   	/**
   	 * Checks for Mobile Mode,PRG insatalled and FTR/PLC License. Returns True if PRG is installed, Mobile Mode is Disabled and
   	 *User has either of Licenses (FTR or PLC).
   	 * @param context 
   	 * 			the eMatrix <code>Context</code> object
   	 * @param args 
   	 * 			string array containing packed arguments.
   	 * @return boolean
   	 * @throws FrameworkException 
   	 * 			If the operation fails
   	 */
   	 public boolean isPRGInstalledMobileDisabled (Context context, String args[]) throws FrameworkException {
   		try {
   			boolean bDisplay;
   			boolean isMobileModeDisabled = !UINavigatorUtil.isMobile(context);
   	  		boolean isFTRUser=false;
   	  		String Licenses[] = {"ENO_FTR_TP","ENO_PLC_TP"};
   	  		try {
   	  			FrameworkLicenseUtil.checkLicenseReserved(context,Licenses);
   			    isFTRUser = true;
   			}catch (Exception e){
   				isFTRUser = false;
   			}
   			
   	  		if(isMobileModeDisabled&&isFTRUser){
   	  			bDisplay = true;
   	  		}else{
   	  		    bDisplay = false;
   	  		}
   	  		   			
   	  	return isPRGInstalled(context, args) && bDisplay;
   	  	
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		} 
   	 }
   	 private int checkConnectedEC (Context context, String strObjectId,String strNextState)
   			 throws Exception{

   		 //Get the EC Object in Complete state connected to the context object
   		 //by Implemented Item or Affected Item Relationship depending upon the
   		 //next state
   		 DomainObject domItem = DomainObject.newInstance(context,
   				 strObjectId);

   		 String strStateRelease = FrameworkUtil.lookupStateName(
   				 context,
   				 domItem.getPolicy(context).getName(),
   				 "state_Release");

   		 List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);

   		 String strPolicyEC = PropertyUtil.getSchemaProperty(context,
   				 SYMBOLIC_policy_EngineeringChange);
   		 String strStateComplete = FrameworkUtil.lookupStateName(
   				 context,
   				 strPolicyEC,
   				 SYMB_state_Complete);

   		 StringBuffer sbObjWhereExpression = new StringBuffer();
   		 sbObjWhereExpression.append(DomainConstants.SELECT_CURRENT);
   		 sbObjWhereExpression.append(SYMB_EQUAL);
   		 sbObjWhereExpression.append(SYMB_QUOTE);
   		 sbObjWhereExpression.append(strStateComplete);
   		 sbObjWhereExpression.append(SYMB_QUOTE);

   		 StringBuffer sbRelWhereExpression = new StringBuffer();
   		 sbRelWhereExpression.append(SYMB_ATTRIBUTE);
   		 sbRelWhereExpression.append(SYMB_OPEN_BRACKET);
   		 sbRelWhereExpression.append(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE);
   		 sbRelWhereExpression.append(SYMB_CLOSE_BRACKET);
   		 sbRelWhereExpression.append(SYMB_EQUAL);

   		 List lstECList = new MapList();

   		 //If the item is being to promoted to Release state then
   		 //Get the EC in complete state connected to this object by Implemented
   		 //Item Relationship or Affected Item Relationship for which Requested Change
   		 //attribute is For Release
   		 if(strNextState.equals(strStateRelease)){
   			 lstECList = (MapList) domItem.getRelatedObjects(
   					 context,
   					 DomainConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM,
   					 DomainConstants.QUERY_WILDCARD,
   					 (StringList) lstObjectSelects,
   					 null,
   					 true,
   					 false,
   					 (short) 1,
   					 sbObjWhereExpression.toString(),
   					 null,
   					 0);
   			 if(lstECList!=null && !lstECList.isEmpty()){
   				 return 0;
   			 }else{
   				 //Get the EC connected to this object by Affected Item relationship
   				 //with proper Requested Change attribute
   				 sbRelWhereExpression.append("\"For Release\"");
   				 sbRelWhereExpression.append(SYMB_SPACE);
   				 sbRelWhereExpression.append(SYMB_OR);
   				 sbRelWhereExpression.append(SYMB_SPACE);
   				 sbRelWhereExpression.append(SYMB_ATTRIBUTE);
   				 sbRelWhereExpression.append(SYMB_OPEN_BRACKET);
   				 sbRelWhereExpression.append(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE);
   				 sbRelWhereExpression.append(SYMB_CLOSE_BRACKET);
   				 sbRelWhereExpression.append(SYMB_EQUAL);
   				 sbRelWhereExpression.append("\"For Obsolescence\"");
   				 lstECList = (MapList) domItem.getRelatedObjects(
   						 context,
   						 DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM,
   						 DomainConstants.QUERY_WILDCARD,
   						 (StringList) lstObjectSelects,
   						 null,
   						 true,
   						 false,
   						 (short) 1,
   						 sbObjWhereExpression.toString(),
   						 sbRelWhereExpression.toString(),
   						 0);
   				 if(lstECList!=null && !lstECList.isEmpty()){
   					 return 0;
   				 }else{
   					 return 1;
   				 }
   			 }
   		 }
   		 //If the item is being to promoted to Obsolete state then
   		 //Get the EC in complete state connected to this object Affected Item
   		 //Relationship for which Requested Change attribute is For Obsolescence
   		 else{
   			 sbRelWhereExpression.append("\"For Obsolescence\"");
   			 lstECList = (MapList) domItem.getRelatedObjects(
   					 context,
   					 DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM,
   					 DomainConstants.QUERY_WILDCARD,
   					 (StringList) lstObjectSelects,
   					 null,
   					 true,
   					 false,
   					 (short) 1,
   					 sbObjWhereExpression.toString(),
   					 sbRelWhereExpression.toString(),
   					 0);
   			 if(lstECList!=null && !lstECList.isEmpty()){
   				 return 0;
   			 }else{
   				 return 1;
   			 }
   		 }
   	 }   	 
/**
      * This trigger method is used to check whether a valid Change Action
      * object in complete state is connected to the object by Implemented Item or
      * Change Affected Item relationship when it is promoted to release or obsolete state.
      * In R418.HF3 code is refactored to use Modeler API to access schema
      * @param context the eMatrix <code>Context</code> object
      * @param args
      *        0 - The id of the object.
      *        1 - The next state of the object (Release/Obsolete).
      * @return int - an integer (0) if the operation is successful
      * @throws Exception if operation fails
      * @since R418
      */
     public int checkConnectedChangeAction (Context context, String[] args) throws Exception
     {
    	 //Get the object id and next state from the args
    	 String strObjectId = args[0];
    	 String strNextState = args[1];
    	 
    	 //to handle upgrade scenario, if object to promote is not having valid EC - will check if it has valid CA-
    	 int isECConnected=checkConnectedEC(context,strObjectId,strNextState);
    	 if(isECConnected==1){
    		 String POLICY_CHANGE_ACTION = PropertyUtil.getSchemaProperty(context,"policy_ChangeAction");
    		 String STATE_CHANGE_ACTION_COMPLETE = PropertyUtil.getSchemaProperty(context,"policy", POLICY_CHANGE_ACTION, "state_Complete");
        	 
    		 DomainObject domItem = DomainObject.newInstance(context,strObjectId);
        	 String strStateRelease = FrameworkUtil.lookupStateName(context,domItem.getPolicy(context).getName(),"state_Release");
        	 String strStateObselete = FrameworkUtil.lookupStateName(context,domItem.getPolicy(context).getName(),"state_Obsolete");
        	 
    		 //Modeler API Call to get CA info
    		 List<BusinessObject> myObject = new ArrayList<BusinessObject>();
    		 myObject.add(new BusinessObject(strObjectId));
    		 ChangeActionFactory factory = new ChangeActionFactory();
    		 IChangeActionServices changeAction = factory.CreateChangeActionFactory();

    		 Map<String,Map<IChangeAction,Proposed>> ProposedAndCaLinked = changeAction.getProposedAndCaFromListObject(context, myObject);
             System.out.println("ProposedAndCaLinked: " + ProposedAndCaLinked);
    		 //DB Call to get CA more info which is mising in Modeler API
    		 boolean activeEC = false;
    		 for(Entry <String,Map<IChangeAction,Proposed>> objectMapEntry : ProposedAndCaLinked.entrySet()){
    			 for(Entry <IChangeAction,Proposed> proposedEntry : objectMapEntry.getValue().entrySet()){
    				 DomainObject ca = new DomainObject(proposedEntry.getKey().getCaBusinessObject());
    				 //DB Call to get CA details
    				 String slCurrent = ca.getInfo(context, DomainObject.SELECT_CURRENT);
    				 Proposed objectProposedList = proposedEntry.getValue();
					 if(objectProposedList._activities!=null){
    				  for(IProposedActivity activity : objectProposedList._activities){
    					 if(activity.getWhat()!=null && activity.getWhat().equalsIgnoreCase(ActivitiesOperationConstants.operation_ChangeStatus)){
    						 for(int i=0;i<activity.getWhatArguments().size();i++){
    							 String strArgument=activity.getWhatArguments().get(i).getArgumentAsString();
    							 if(slCurrent.equalsIgnoreCase(STATE_CHANGE_ACTION_COMPLETE) && 
    									 ("Release".equalsIgnoreCase(strArgument)&& strNextState.equals(strStateRelease) 
    											 ||"Obsolete".equalsIgnoreCase(strArgument) && strNextState.equals(strStateObselete))){
    								 activeEC = true;
    								 break;
    							 }
    						 }
    					 }
    				 }
				   }    				 
    			 }
    		 }
			 // Adding code that is need to handle Revise Scenario, where Object will be connected to Realized Change + next state is release
		if(!activeEC && strNextState.equals(strStateRelease)){
		Map<String, Map<IChangeAction,List<IRealizedChange>>> mapOutputAllBo = changeAction.getRealizedAndCaFromListObjects(context, myObject, false, true, false);	
		System.out.println("mapOutputAllBo: " + mapOutputAllBo);
		for(Entry<String, Map<IChangeAction,List<IRealizedChange>>> mapOutput : mapOutputAllBo.entrySet())
		{
			for(Entry<IChangeAction,List<IRealizedChange>> mapOutput2: mapOutput.getValue().entrySet())
			{
				System.out.println("Ca id : " + mapOutput2.getKey().getCaBusinessObject().getObjectId());
				List<IRealizedChange> realizedList = mapOutput2.getValue();
				// just check if it's attached as realized
				if(realizedList!= null && realizedList.size()>0){
				    activeEC=true;
			    }
			}
		}
		} 
    		 if(activeEC){
    			 return 0;
    		 }else{
    			 String strErrorMsg =""; 
    			 if(strNextState.equals(strStateRelease)){
    				 strErrorMsg = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Alert.NoCompleteChangeActionForRelease",context.getSession().getLanguage());
    			 }else if (strNextState.equals(strStateObselete)){
    				 strErrorMsg = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Alert.NoCompleteChangeActionForObsolescence",context.getSession().getLanguage());
    			 }
    			 emxContextUtil_mxJPO.mqlNotice(context,strErrorMsg);    			 
    			 return 1;
    		 }
    	 }else{
        	 return 0;
         }
     }
    /**
     * with the ECM adoption of FTR, for Properties for LF/CF/MF - field
     * "Active Engineering Change" renamed "Active Change" will show Yes if
     * object has Change action connected with Affected Item relatonship, and
     * Change Action is not in "Complete", "On Hold" and "Cancelled"
     * 
     * other wise No
     * 
     * In R418.HF3 code is refactored to use Modeler API to access schema
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @depricated with the ECM adoption of FTR, use enoECMChangeUtilBase : getActiveChangeIconInProperty()
     */

    public String getActiveChangeIconInProperty(Context context, String[] args)
            throws Exception {
    	String strActiveECIcon = "";
    	
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map relBusObjPageList = (HashMap) programMap.get("paramMap");
			String strObjectId = (String) relBusObjPageList.get("objectId");
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			String POLICY_CHANGE_ACTION = PropertyUtil.getSchemaProperty(context,"policy_ChangeAction");
			String STATE_CHANGE_ACTION_COMPLETE = PropertyUtil.getSchemaProperty(context,"policy", POLICY_CHANGE_ACTION, "state_Complete");
			StringBuffer sbActiveECIcon = new StringBuffer(100);

			String strTooltipActiveECIcon = EnoviaResourceBundle.getProperty(context, STR_BUNDLE, context.getLocale(),"emxProduct.Change.ToolTipActiveChangeExists");
			String strNo = EnoviaResourceBundle.getProperty(context, STR_BUNDLE, context.getLocale(), "emxProductLine.ActiveChange.No");
			String strYes = EnoviaResourceBundle.getProperty(context, STR_BUNDLE, context.getLocale(), "emxProductLine.ActiveChange.Yes");
			boolean isCSVExport = requestMap.get("reportFormat") != null && "CSV".equalsIgnoreCase((String) requestMap.get("reportFormat"));

			String strActiveECIconTag = "";
			String strIcon = EnoviaResourceBundle.getProperty(context,"emxComponents.ActiveECImage");
			//Modeler API Call to get CA info
			List<BusinessObject> myObject = new ArrayList<BusinessObject>();
			myObject.add(new BusinessObject(strObjectId));
            ChangeActionFactory factory = new ChangeActionFactory();
            IChangeActionServices changeAction = factory.CreateChangeActionFactory();

			Map<String,Map<IChangeAction,Proposed>> ProposedAndCaLinked = changeAction.getProposedAndCaFromListObject(context, myObject);

			//DB Call to get CA more info which is mising in Modeler API
			StringList slCASelects=new StringList();
			String POLICY_CANCELLED = PropertyUtil.getSchemaProperty(context,"policy_Cancelled");
			String INTERFACE_CHANGE_ON_HOLD = PropertyUtil.getSchemaProperty(context,"interface_ChangeOnHold");
			slCASelects.add(DomainObject.SELECT_CURRENT);
			slCASelects.add(DomainObject.SELECT_POLICY);
			slCASelects.add("interface["+ INTERFACE_CHANGE_ON_HOLD +"]");
			boolean activeEC = false;
			for(Entry <String,Map<IChangeAction,Proposed>> objectMapEntry : ProposedAndCaLinked.entrySet()){
				for(Entry <IChangeAction,Proposed> proposedEntry : objectMapEntry.getValue().entrySet()){
					DomainObject ca = new DomainObject(proposedEntry.getKey().getCaBusinessObject());
					//DB Call to get CA details
					Map caCurrentState = ca.getInfo(context, slCASelects);
					String slCurrent = (String)caCurrentState.get(DomainObject.SELECT_CURRENT);
					String strPolicy = (String)caCurrentState.get(DomainObject.SELECT_POLICY);
					String onHold = (String)caCurrentState.get("interface["+ INTERFACE_CHANGE_ON_HOLD +"]");
					if(!strPolicy.equalsIgnoreCase(POLICY_CANCELLED) 
							&& !slCurrent.equalsIgnoreCase(STATE_CHANGE_ACTION_COMPLETE) 
							&&!onHold.equalsIgnoreCase("TRUE")){
						activeEC = true;
						break;
					}else{
						activeEC = false;
					}
				}
			}
			if (activeEC) {
				strActiveECIconTag = "<img src=\"../common/images/" + strIcon
						+ "\" border=\"0\"  align=\"middle\" " + "TITLE=\"" + " "
						+ strTooltipActiveECIcon + "\"" + "/>";
				if (!isCSVExport) {
					sbActiveECIcon.append(strActiveECIconTag);
				}
				sbActiveECIcon.append(strYes);
				strActiveECIcon = sbActiveECIcon.toString();
			} else {
				strActiveECIconTag = "&nbsp;";
				sbActiveECIcon.append(strNo);
				if (!isCSVExport) {
					sbActiveECIcon.append(strActiveECIconTag);
				}
				strActiveECIcon = sbActiveECIcon.toString();
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage()); 
		}
		return strActiveECIcon;		
    }    

    /**
     * with the ECM adoption of FTR, for Active EC Column for LF/CF/MF - 
     * "Active Engineering Change" renamed "Active Change" will show Yes if
     * object has Change action connected with Affected Item relatonship, and
     * Change Action is not in "Complete", "On Hold" and "Cancelled"
     * 
     * other wise No
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @depricated with the ECM adoption of FTR, use enoECMChangeUtilBase : getActiveChangeIconInColumn()
     */
    public List getActiveChangeIconInColumn(Context context, String[] args) throws Exception{        List lstActiveECIcon= new Vector();
        try {
        	Map programMap = (HashMap) JPO.unpackArgs(args);
        	MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
        	Map paramList = (HashMap)programMap.get("paramList");
        	String reportFormat = (String)paramList.get("reportFormat");
        	int iNumOfObjects = relBusObjPageList.size();
        	String strActiveECIconTag = "";
        	String strIcon = EnoviaResourceBundle.getProperty(context,"emxComponents.ActiveECImage");
        	String strTooltipActiveECIcon = EnoviaResourceBundle.getProperty(context,STR_BUNDLE,context.getLocale(),"emxProduct.Change.ToolTipActiveChangeExists");
        	String POLICY_CHANGE_ACTION = PropertyUtil.getSchemaProperty(context,"policy_ChangeAction");
        	String STATE_CHANGE_ACTION_COMPLETE = PropertyUtil.getSchemaProperty(context,"policy", POLICY_CHANGE_ACTION, "state_Complete");

        	//create list of BusinessObject for which CA Information required
        	List<BusinessObject> myObject = new ArrayList<BusinessObject>();
        	int iCount;
        	for (iCount = 0; iCount < iNumOfObjects; iCount++) {
        		Object obj = relBusObjPageList.get(iCount);
        		if (obj instanceof HashMap) {
        			myObject.add(new BusinessObject((String)((HashMap)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID)));
        		}
        		else if (obj instanceof Hashtable){
        			myObject.add(new BusinessObject((String)((Hashtable)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID)));
        		}
        	}

        	//Modeler API Call to get CA info        
            ChangeActionFactory factory = new ChangeActionFactory();
            IChangeActionServices changeAction = factory.CreateChangeActionFactory();
            Map<String,Map<IChangeAction,Proposed>> ProposedAndCaLinked = changeAction.getProposedAndCaFromListObject(context, myObject);

        	StringList slCASelects=new StringList();
        	String POLICY_CANCELLED = PropertyUtil.getSchemaProperty(context,"policy_Cancelled");
        	String INTERFACE_CHANGE_ON_HOLD = PropertyUtil.getSchemaProperty(context,"interface_ChangeOnHold");
        	slCASelects.add(DomainObject.SELECT_CURRENT);
        	slCASelects.add(DomainObject.SELECT_POLICY);
        	slCASelects.add("interface["+ INTERFACE_CHANGE_ON_HOLD +"]");
        	//for each object passed-DB Call to get CA more info which is mising in Modeler API
        	//TODO-- FROM MODELER SIDE- remove the implicit information by an explicite one. No CA = ObjectID + Empty Map @ ChangeModeler.
        	//TODO -- are we getting API/selectable API to get exact CA to avoid DB Calls?
        	Map objPhyIDActiveCA=new HashMap<String, String>();
        	for(Entry <String,Map<IChangeAction,Proposed>> objectMapEntry : ProposedAndCaLinked.entrySet()){
        		boolean activeEC = false;
        		String objectPhysicalID=objectMapEntry.getKey();
        		for(Entry <IChangeAction,Proposed> proposedEntry : objectMapEntry.getValue().entrySet()){
        			//iterate for each CA on Object, will chec if Active CA Exists.
        			DomainObject ca = new DomainObject(proposedEntry.getKey().getCaBusinessObject());
        			Map caCurrentState = ca.getInfo(context, slCASelects);
        			String slCurrent = (String)caCurrentState.get(DomainObject.SELECT_CURRENT);
        			String strPolicy = (String)caCurrentState.get(DomainObject.SELECT_POLICY);
        			String onHold = (String)caCurrentState.get("interface["+ INTERFACE_CHANGE_ON_HOLD +"]");
        			if(!strPolicy.equalsIgnoreCase(POLICY_CANCELLED) 
        					&& !slCurrent.equalsIgnoreCase(STATE_CHANGE_ACTION_COMPLETE) 
        					&&!onHold.equalsIgnoreCase("TRUE")){
        				activeEC = true;
        				break;
        			}else{
        				activeEC = false;
        			}
        		}
        		if(activeEC) {
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
        		//each Object with CA maintain Cell data in Map
        		objPhyIDActiveCA.put(objectPhysicalID,strActiveECIconTag);
        	}
        	//iterate again
        	int iCount2;
        	for (iCount2 = 0; iCount2 < iNumOfObjects;iCount2++) {
        		Object obj = relBusObjPageList.get(iCount2);
        		String phyid="";
        		String objID="";
        		if (obj instanceof HashMap) {
        			phyid=(String)(((HashMap)relBusObjPageList.get(iCount2)).get("physicalid"));
        			objID=(String)(((HashMap)relBusObjPageList.get(iCount2)).get(DomainObject.SELECT_ID));
        		}
        		else if (obj instanceof Hashtable){
        			phyid=(String)((Hashtable)relBusObjPageList.get(iCount2)).get("physicalid");
        			objID=(String)(((Hashtable)relBusObjPageList.get(iCount2)).get(DomainObject.SELECT_ID));
        		}
        		
        		if((phyid==null ||phyid.trim().isEmpty()) && ProductLineCommon.isNotNull(objID)){
        			//ROOT NODE- SELCTABLE PHYSICAL ID MISSING FOR EXPAND PROG CASE
        			phyid=DomainObject.newInstance(context, objID).getInfo(context, "physicalid");
        		}
        		if(objPhyIDActiveCA.containsKey(phyid)){
        			lstActiveECIcon.add((String)objPhyIDActiveCA.get(phyid));
        		}else{
        			lstActiveECIcon.add("");
        		}
        	}
        } catch (Exception e) {
        	throw new FrameworkException(e.getMessage());
        }
        return lstActiveECIcon;
    }  
    
    
    /**
     * Trigger to Auto promote the BO during creation conditionally based on a RACE VPLMAutoPromoteFirstMinorRev
     * setting.  This trigger will check the setting and auto-promote if needed to support "collaborative creation mode".
     * @param context the Enovia <code>Context</code> object
     * @param args trigger parameters which include the list of policies to promote against and trigger event.
     *             
     * @throws Exception
     */
    public void autoPromoteMajorCollaborativeMode(Context context, String args[]) throws Exception {
    	
    	if (SkipAutoPromote(context)) {
    		return;
    	}
        String autoPrmote = MqlUtil.mqlCommand(context, "list expression $1 select $2 dump",
                                "VPLMAutoPromoteFirstMinorRev", "value");
        if ("true".equalsIgnoreCase(autoPrmote)) {
            String objectId = getObjectId(context);
             String triggerPolicy = null;
            String event = args[1];
            if ("MajorRevision".equalsIgnoreCase(event)) {
            	objectId = args[2];
            }
            boolean historyOff = false;
            boolean triggerOff = false;   //HAT1 ZUD:IR-439295-3DEXPERIENCER2017x: fix 
            boolean runAsSuperUser = true;
            
            String policy = args[0]; //new DomainObject(objectId).getInfo(context, DomainConstants.SELECT_POLICY);
            String[] states = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", policy, "state", "|").split("[|]", -1);
            if(states.length > 1){
            	String signature = getSignature(context, objectId, policy);
            	if(!signature.equals("")) { //do not promote if there is a signature
            		return; 
            	}
            	MqlUtil.mqlCommand(context, historyOff, triggerOff, "promote bus $1", runAsSuperUser, objectId);
            } 
            
            if(states.length == 6) { //if extra state between Private and InWork:
            	String inWork = FrameworkUtil.lookupStateName(context, policy, "state_InWork");
            	if(states[2].equals(inWork)) {
            		MqlUtil.mqlCommand(context, historyOff, triggerOff, "promote bus $1", runAsSuperUser, objectId);
            	}
            }
        }
    }    
    
    private String getSignature(Context context, String objectId, String policy) throws FrameworkException
    {
    	String cur = "current";
		String QryStm1 = "print bus $1 select $2 dump";
		String Current = MqlUtil.mqlCommand(context, QryStm1, objectId, cur).trim();
		
		String QryStm3 = "print policy $1 select $2 dump $3";
		String SgName = "state[" + Current + "].signature.name";
		String QryResult3 = MqlUtil.mqlCommand(context, QryStm3, policy, SgName, "|").trim();
		
		return QryResult3;
    }
    
    private boolean SkipAutoPromote(Context context) throws Exception
	{
		// Exit if current user is "User Agent" (tactical solution for "Import As Reference")
		if ((context.getUser()).equalsIgnoreCase("User Agent"))
		{
			return true;
		}

		// Exit if System privileges
		if ((MqlUtil.mqlCommand(context, "list person $1 select $2 dump;", context.getUser(), "system")).equalsIgnoreCase("TRUE"))
		{
			return true;
		}

		return false;
	}

 // ++ KIE1 : HL Parameter under Test Execution
   	public StringList getParameterLastMeasuredValue(Context iContext, String[] iArgs) throws Exception
   	{
   		HashMap programMap          = (HashMap)JPO.unpackArgs(iArgs);
		StringList returnList       = new StringList();
		Map paramList               = (HashMap) programMap.get("paramList");
		String parentId             = (String) paramList.get("objectId");
		MapList testExecutionList   = getRelatedTestExecutions(iContext,iArgs);
		MapList paraList 		    = getAssociatedParameters(iContext,parentId);
		HashMap returnMap           = new HashMap();
		MapList playedTestExecList  = new MapList();
		Map lastTestExecutionObjMap = null;
		MapList lastTestExecutionObjMaplst = null;
		lastTestExecutionObjMaplst     = LastCompletedTestExecution(iContext, testExecutionList);
	
		MapList playedTEParaList = new MapList();
		String objectId          = "";
		
		if(!lastTestExecutionObjMaplst.isEmpty())
		{
			lastTestExecutionObjMap = (Map)lastTestExecutionObjMaplst.get(lastTestExecutionObjMaplst.size()-1);  
			objectId = (String)lastTestExecutionObjMap.get("id");	
			playedTEParaList = getAssociatedParameters(iContext, objectId);
		}
		
        getLastMeasurevalue(iContext, playedTEParaList, returnMap, paraList, parentId);
        
        for(int i =0 ; i<paraList.size(); i++)
        {
        	Map mapforsort   = (Map)paraList.get(i);
        	String paraValue = (String)returnMap.get(mapforsort.get("name"));
        	returnList.add(returnMap.get(mapforsort.get("id")));
        }
		return returnList;
	           
   	}
	
    public MapList LastCompletedTestExecution(Context context, MapList TestCaseTestExecutionList)
    {
    	Map<String, String> currTestExecutionObjMap = null;
    	MapList lastTestExecutionObjMaplst          = new MapList();            // Map for Last connected Test Execution to Test Case.
    	DomainObject testExecutionDmoObj 			= null;
    	DomainObject lastTestExecutionDmoObj        = null;     // Domain object for Current and Last connected Test Execution to Test Case.
    	String testExecutionObjID                   = "";
    	String testExecutionObjName                 = "";
    	String lastTestExecutionObjID               = ""; 
    	String lastTestExecutionObjName             = "";
    	//Date currentDate                            = new Date();                                 // currentDate object of type Date.
    	Date dateLastActualEndDateTE                = null;                    // Populating 'dateLastActualEndDateTE' with currentDate which will hold attribute 'Actual End Date' of Last connected Test Execution.
    	Date currActualEndDateTE                    = null;
    	MapList playedTestExecutionList             = new MapList();
    	
    	if(TestCaseTestExecutionList.size() != 0)
        {
	        String strTestCaseTestExecutionListCount = String.valueOf(TestCaseTestExecutionList.size());
	    	Iterator testExecutionItr                = TestCaseTestExecutionList.iterator();
	    	int isAnyLastTEfound                     = 0;
        	try 
        	{
				while(testExecutionItr.hasNext())
				{
					String strCurrActualEndDateTE = "";
					currTestExecutionObjMap       = (Map) testExecutionItr.next();
					testExecutionObjID            = (String) currTestExecutionObjMap.get("id");
					testExecutionObjName          = (String) currTestExecutionObjMap.get("name");
					testExecutionDmoObj           = DomainObject.newInstance(context, testExecutionObjID);
					try
					{
						strCurrActualEndDateTE  = testExecutionDmoObj.getAttributeValue(context, "Actual End Date");
						currActualEndDateTE	= eMatrixDateFormat.getJavaDate(strCurrActualEndDateTE);
					} 
					catch (Exception e) 
					{
						continue;
					}
					if(null == dateLastActualEndDateTE)
					{
						currTestExecutionObjMap.put("Date", ""+strCurrActualEndDateTE);
						lastTestExecutionObjMaplst.add(currTestExecutionObjMap);
						dateLastActualEndDateTE = currActualEndDateTE;
					}
					else
					{
						Iterator iteratorMapListMaplstItr               = lastTestExecutionObjMaplst.iterator();
						MapList sortingList = new MapList();
						while(iteratorMapListMaplstItr.hasNext())
						{
							Map ltcurrTestExecutionObjMap       		= (Map) lastTestExecutionObjMaplst.get(lastTestExecutionObjMaplst.size()-1);
							Date ltDate 								= null;
							ltDate 										= eMatrixDateFormat.getJavaDate((String)ltcurrTestExecutionObjMap.get("Date"));
							int flag 									= ltDate.compareTo(currActualEndDateTE);
							
							if(flag ==1)
							{
								sortingList.add(ltcurrTestExecutionObjMap);
								lastTestExecutionObjMaplst.remove(ltcurrTestExecutionObjMap);	
							}
							else if(flag ==-1)
							{								
								currTestExecutionObjMap.put("Date", ""+strCurrActualEndDateTE);
								lastTestExecutionObjMaplst.add(currTestExecutionObjMap);
								break;
							}
						}
						if(lastTestExecutionObjMaplst.size() == 0)
						{
							currTestExecutionObjMap.put("Date", ""+strCurrActualEndDateTE);
							lastTestExecutionObjMaplst.add(currTestExecutionObjMap);						
						}
						for(int  i =sortingList.size() -1; i>= 0; i--)
						{
							lastTestExecutionObjMaplst.add(sortingList.get(i));
						}
					}
				}
			} 
        	catch (FrameworkException e) 
        	{
				e.printStackTrace();
			}
        }
		return lastTestExecutionObjMaplst;
    }
   	
    // get Parameter Last Measured values under Test Case Context
	private void getLastMeasurevalue(Context myContext,MapList playedTEParaList, HashMap returnMap, 
			MapList paraList, String ParentId) throws Exception 
	{
		for (int i = 0; i < playedTEParaList.size(); i++) 
		{
			Map paramObj 			 = (Map) playedTEParaList.get(i);
			String paramId 			 = (String) paramObj.get("id");
			IPlmParameter plmPara    = ParameterInterfacesServices.getParameterById(myContext, paramId);
			String paraType 		 = plmPara.getDimension(myContext).getName();
			PLMParm_ValuationType valuationType   = plmPara.getValuationType(myContext);
			String paramRole 		 = plmPara.getRole(myContext);
			String paramRole_split[] = paramRole.split("_");
			if (paramRole != null && paramRole_split.length == 1)
			{
				// get Parameter value under Test Execution
				String strParameterValue = getParameterValue(myContext, plmPara);
				String strMinValue = "", strMaxValue = "",parentParamid = "", DivDecoratedText = "";
				for(int j = 0; j < paraList.size(); j++)
				{
					Map paramMap     = (Map)paraList.get(j);
					parentParamid = (String)paramMap.get("id");
					if(parentParamid.equals(paramRole_split[0]))
					{
						if(paraType.equals(ParameterTypes.Boolean))
						{
							if(!strParameterValue.equals("null"))
							{
								strParameterValue = strParameterValue.toUpperCase();
								DivDecoratedText +="<div id='contentCell' class='cke_contents cke_reset'>";
								DivDecoratedText +="<b>"+strParameterValue+"</b>";
								DivDecoratedText +="</div>";
							}
						}
						else 
						if(paraType.equals(ParameterTypes.String))
						{
							if(!strParameterValue.equals("null") && !strParameterValue.equals(""))
							{
								DivDecoratedText +="<div id='contentCell' class='cke_contents cke_reset'>";
								DivDecoratedText +="<b>"+strParameterValue+"</b>";
								DivDecoratedText +="</div>";
							}
							else
							{
								strParameterValue += "  ";
							}
				    	}
						else
				    	{
							IPlmParameter plmPar = ParameterInterfacesServices.getParameterById(myContext, parentParamid);
							strMinValue 		 = getParameterMinValue(myContext, plmPar);
							strMaxValue 		 = getParameterMaxValue(myContext, plmPar);
							IKweUnit unit		 = plmPar.getDisplayUnit(myContext);
							String displayUnit 	 = "";
							
							if(unit != null)
							{
								displayUnit = unit.getNLSName(myContext);	
							}
							if(displayUnit.trim().length() > 0 && !displayUnit.isEmpty())
							{
					    		if(paraType.equals("LENGTHParameter"))
					    		{
					    			strParameterValue = convertToDisplaysValues(myContext, strParameterValue,  "m", displayUnit);
					    		}else if(paraType.equals("ANGLEParameter"))
					    		{
					    			strParameterValue = convertToDisplaysValues(myContext, strParameterValue, "rad", displayUnit);
					    		}else if(paraType.equals("MASSParameter"))
					    		{
					    			strParameterValue = convertToDisplaysValues(myContext, strParameterValue, "kg", displayUnit);
					    		}
							}
							
							if(strMaxValue.compareTo("") == 0)
							{
								strMaxValue = "0";
							}
			
							if(strMinValue.compareTo("") == 0)
							{
								strMinValue = "0";
							}
							if(!strParameterValue.equals("null"))
							{
								if(valuationType.equals(PLMParm_ValuationType.MULTI))
								{
									String multiVal[] = strParameterValue.split(",");
									 
									StringBuffer strBuf = new StringBuffer();
									strBuf.append("<script type=\'text/javascript\'>");
									strBuf.append("$(this).val()");
									strBuf.append("</script>");
								     
				        			String displayValue ="<select id='multiValue' onchange=\"javascript:showModalDialog('../productline/PLCParameterUtil.jsp?Mode=getHighChart&amp;objectId="+parentParamid+"&amp;parentOID="+ParentId+"')\">";
				        			for(String str : multiVal){
				        				displayValue +="<option value='"+str+"'>"+str+"</option>";
				        			}
				        			displayValue +="</select>";
				        			
				        			if(Double.parseDouble(multiVal[0]) <= Double.parseDouble(strMinValue) && Double.parseDouble(multiVal[multiVal.length -1]) <= Double.parseDouble(strMaxValue))
				        			{
				        				DivDecoratedText +="<div id='contentCell' class='cke_contents cke_reset'>";
										DivDecoratedText +=displayValue;
										DivDecoratedText += " <img border='0' src='../productline/images/inbound.png'/></div>";
				        			}
				        			else if(Double.parseDouble(multiVal[0]) > Double.parseDouble(strMinValue))
									{
										DivDecoratedText +="<div id='contentCell' class='cke_contents cke_reset'>";
										DivDecoratedText +=displayValue;
										DivDecoratedText += " <img border='0' src='../productline/images/outbound.png'/></div>";
									}
									else if(Double.parseDouble(multiVal[multiVal.length -1]) < Double.parseDouble(strMaxValue))
									{
										DivDecoratedText +="<div id='contentCell' class='cke_contents cke_reset'>";
										DivDecoratedText +=displayValue;
										DivDecoratedText += " <img border='0' src='../productline/images/outbound.png'/></div>";
									}
								}
								else
								{
									if(Double.parseDouble(strParameterValue) >= Double.parseDouble(strMinValue) && Double.parseDouble(strParameterValue) <= Double.parseDouble(strMaxValue))
									{
										DivDecoratedText +="<div id='contentCell' class='cke_contents cke_reset'>";
										DivDecoratedText +="<a href=\"javascript:showModalDialog('../productline/PLCParameterUtil.jsp?Mode=getHighChart&amp;objectId="+parentParamid+"&amp;parentOID="+ParentId+"')\">"+strParameterValue+"</a>";
										DivDecoratedText += " <img border='0' src='../productline/images/inbound.png'/></div>";
									}
									else if(Double.parseDouble(strParameterValue) > Double.parseDouble(strMaxValue))
									{
										DivDecoratedText +="<div id='contentCell' class='cke_contents cke_reset'>";
										DivDecoratedText +="<a href=\"javascript:showModalDialog('../productline/PLCParameterUtil.jsp?Mode=getHighChart&amp;objectId="+parentParamid+"&amp;parentOID="+ParentId+"')\">"+strParameterValue+"</a>";
										DivDecoratedText += " <img border='0' src='../productline/images/outbound.png'/></div>";
									}
									else if(Double.parseDouble(strParameterValue) < Double.parseDouble(strMinValue))
									{
										DivDecoratedText +="<div id='contentCell' class='cke_contents cke_reset'>";
										DivDecoratedText +="<a href=\"javascript:showModalDialog('../productline/PLCParameterUtil.jsp?Mode=getHighChart&amp;objectId="+parentParamid+"&amp;parentOID="+ParentId+"')\">"+strParameterValue+"</a>";
										DivDecoratedText += " <img border='0' src='../productline/images/outbound.png'/></div>";
									}
								}
							}
						}
					}
				}
				returnMap.put(paramRole_split[0],DivDecoratedText);
			}
		}
	}
	
	// Parameter Nominal value will return
	public String getParameterValue(Context myContext, IPlmParameter plmPara)throws Exception
	{
		String value            		 	= "";
		String strParameterText 		 	= "";
		IPlmParameterDisplay parmDisplay 	= (IPlmParameterDisplay) plmPara;
		String paraType 				 	= plmPara.getDimension(myContext).getName();
		PLMParm_ValuationType valuationType = plmPara.getValuationType(myContext);
		IKweUnit unit						= plmPara.getDisplayUnit(myContext);
		String displayUnit 					= "";
		
		if(unit != null)
		{
			displayUnit = unit.getNLSName(myContext);	
		}
		
		if(paraType.equals(ParameterTypes.Boolean))
		{
			strParameterText += parmDisplay.getValueForDisplay(myContext);
		}
		else if(paraType.equals(ParameterTypes.String))
		{
			if(valuationType.equals(PLMParm_ValuationType.MULTI))
			{
				IKweList multiValues = parmDisplay.getMultiValuesForDisplay(myContext);
				
					for (int i=1; i<=multiValues.GetSize(); i++)
						strParameterText +=multiValues.GetItem(i).asString()+",";
			}
			else if(valuationType.equals(PLMParm_ValuationType.SIMPLE))
			{
				value = ""+parmDisplay.getValueForDisplay(myContext);	
			}
			
			if(!"null".equals(value) && !value.equals("")){
				strParameterText += value;
			}
			else
			{
        		strParameterText += "  ";
        	}
    	}
		else
		{
			if (valuationType.equals(PLMParm_ValuationType.SIMPLE))
			{
				value = "" + plmPara.getValue(myContext);
				if (!"null".equals(value) && !value.equals("")) {
					strParameterText += parmDisplay
							.getValueForDisplay(myContext);
				} else {
					strParameterText += value;
				}
			}
			else if(valuationType.equals(PLMParm_ValuationType.MULTI))
			{
				IKweList multiValues = parmDisplay.getMultiValuesForDisplay(myContext);
				
					for (int i=1; i<=multiValues.GetSize(); i++)
						strParameterText +=multiValues.GetItem(i).asString()+",";
			}else {
				strParameterText += null;
			}
		}
		return strParameterText;
	}
	
	// Parameter Maximal value will return
	public String getParameterMaxValue(Context myContext, IPlmParameter plmPara)throws Exception
	{
		String value 					 = "";
		String strParameterText 		 = "";
		IPlmParameterDisplay parmDisplay = (IPlmParameterDisplay) plmPara;
		String paraType 				 = plmPara.getDimension(myContext).getName();
		IKweUnit unit					 = plmPara.getDisplayUnit(myContext);
		String displayUnit 				 = "";
		
		if(unit != null)
		{
			displayUnit = unit.getNLSName(myContext);	
		}
		strParameterText += parmDisplay.getMaxValueForDisplay(myContext);
		return strParameterText;
	}
	
	// Parameter Minimal value will return
	public String getParameterMinValue(Context myContext, IPlmParameter plmPara)throws Exception
	{
		String value 					 = "";
		String strParameterText 		 = "";
		IPlmParameterDisplay parmDisplay = (IPlmParameterDisplay) plmPara;
		String paraType 			 	 = plmPara.getDimension(myContext).getName();
		IKweUnit unit					 = plmPara.getDisplayUnit(myContext);
		String displayUnit 				 = "";
		
		if(unit != null)
		{
			displayUnit = unit.getNLSName(myContext);	
		}
		strParameterText += parmDisplay.getMinValueForDisplay(myContext);
		
		return strParameterText;
	}
	
	 //Function for converting the table values in to MKS unit format for storing in DB. 
    private String convertToDisplaysValues(Context context, String strParameterText, String displayUnit, 
    		String mksUnit) throws Exception 
    {
		String args[] 		= new String[3];
		args[0] 			= displayUnit;
		args[1] 			= mksUnit;
		String retValue 	= "";
			if(!strParameterText.equals("null") && !strParameterText.isEmpty())
			{
				String strSplit[] = new String[3];
				if(strParameterText.contains(","))
				{
					strSplit = strParameterText.split(",");
					for(int j = 0; j < strSplit.length; j++)
					{
						args[2] = strSplit[j]+"";
						ArrayList<Double> ret = (ArrayList<Double>) JPO.invoke(context, "emxParameter", null,
								"getConvertedValues", args, ArrayList.class);
						retValue += ret.get(0)+",";
					}
				}else{
					args[2] = strParameterText+"";
					ArrayList<Double> ret = (ArrayList<Double>) JPO.invoke(context, "emxParameter", null,
							"getConvertedValues", args, ArrayList.class);
					retValue = ""+ret.get(0);
				}
				strParameterText = retValue;
			}
		return strParameterText;
	}
    
    //Function for converting the table values in to MKS unit format for storing in DB. 
    private String[] convertToMksValues(Context context, String[] values, String displayUnit, String mksUnit) throws Exception {
    	
		String args[] = new String[3];
		args[0] = displayUnit;
		args[1] = mksUnit;
		String retValue = "";
		for(int i = 0; i < values.length; i++)
		{
			if(!values[i].isEmpty())
			{
				String strSplit[] = new String[3];
				if(values[i].contains(","))
				{
					strSplit = values[i].split(",");
					for(int j = 0; j < strSplit.length; j++)
					{
						args[2] = strSplit[j]+"";
						ArrayList<Double> ret = (ArrayList<Double>) JPO.invoke(context, "emxParameter", null,
								"getConvertedValues", args, ArrayList.class);
						retValue += ret.get(0)+",";
					}
				}else{
					args[2] = strSplit[i]+"";
					ArrayList<Double> ret = (ArrayList<Double>) JPO.invoke(context, "emxParameter", null,
							"getConvertedValues", args, ArrayList.class);
					retValue = ""+ret.get(0);
				}
				values[i] = retValue;
			}
		}
		
		return values;
	}

	public MapList getRelatedTestExecutions(Context context,String[] args) throws Exception 
	{
		HashMap programMap 			= (HashMap) JPO.unpackArgs(args);
		Map testExeParaList 		= (HashMap) programMap.get("paramList");
		String strParentId  		= (String) testExeParaList.get("objectId");
		MapList returnMapList 		= new MapList();
		String strParentRelName 	= null;
		StringList objectSelects 	= new StringList(DomainConstants.SELECT_ID);
		objectSelects.add(DomainConstants.SELECT_NAME);
		StringList relSelects 		= new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
		DomainObject parentObj 		= DomainObject.newInstance(context, strParentId);
		short recurseToLevel 		= 1;

		try {
			String strParentObjType = parentObj.getInfo(context,ProductLineConstants.SELECT_TYPE);
			strParentRelName     	= DomainConstants.RELATIONSHIP_TEST_EXECUTION_TEST_CASE;
			returnMapList 			= parentObj.getRelatedObjects(
					context,
					strParentRelName, // Relationship name
					DomainConstants.TYPE_TEST_EXECUTION, // get type Test
															// Execution
					objectSelects, relSelects,
					true, // get To relationships for the Test Case
					false, recurseToLevel, DomainConstants.EMPTY_STRING,
					DomainConstants.EMPTY_STRING);

		} catch (Exception ex) {
			throw ex;
		}
		return returnMapList;
	}
 
	public MapList getAssociatedParameters(Context context,String objectId) throws Exception 
	{
		try {
			String toTypeName  			= PropertyUtil.getSchemaProperty(context,"type_PlmParameter");
			String relationships    	= PropertyUtil.getSchemaProperty(context,"relationship_ParameterAggregation");
			HashMap programMap 			= new HashMap();
			programMap.put("objectId", objectId);
			DomainObject dom 			= new DomainObject(objectId);
			int sRecurse 				= 0;
			StringList objSelects 		= new StringList(1);
			StringList relSelects   	= new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
			MapList relBusObjPageList   = new MapList();
			objSelects.addElement(DomainConstants.SELECT_LEVEL);
			objSelects.addElement("id[connection]");
			objSelects.addElement(DomainConstants.SELECT_NAME);
			objSelects.addElement(DomainConstants.SELECT_ID);
			objSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
			objSelects.addElement(DomainConstants.SELECT_REVISION);
			
			relBusObjPageList = dom.getRelatedObjects(context, relationships,
					toTypeName, objSelects, relSelects, false, true, (short) 0,
					null, null);
			return relBusObjPageList;
		} catch (Exception ex) 
		{
			System.out.println("getAssociatedParameters - exception "
					+ ex.getMessage());
			throw ex;
		}
	}

	// Will hide the column Last Measured value table Test Execution's parameter
	// called from PLCAssociatedParameter
	public boolean hideColumnForTestExecution(Context context, String[] args)throws Exception 
	{
		Map programMap 		= (HashMap) JPO.unpackArgs(args);
		String parentId 	= (String) programMap.get("objectId");
		DomainObject newObj = DomainObject.newInstance(context, parentId);
		boolean showCmd 	= false;
		
		if (newObj != null)
		{
			if (newObj.isKindOf(context,PropertyUtil.getSchemaProperty(context, "type_TestCase"))) 
			{
				showCmd = true;
			}
		}
		return showCmd;
	}

	// return only played Test Execution name
 	 public String getTestExecutionName(Context context, String[] args) throws Exception
 	 {
 		String parentId_childId 		= (String) JPO.unpackArgs(args);
 		String ids[] 					= parentId_childId.split(":");
 		HashMap programMap 				= new HashMap();
 		HashMap pramList 				= new HashMap();
 		pramList.put("objectId", ids[0]);
 		programMap.put("paramList",pramList);
 		DomainObject paramObj 			= DomainObject.newInstance(context, ids[1]);
		String paramName 				= paramObj.getInfo(context, DomainConstants.SELECT_NAME);
 		MapList busList    				= new MapList();
 		String testExexutionname 		= "[";
 		busList 						= getRelatedTestExecutions(context, JPO.packArgs(programMap));
		MapList lastTestExecutionObjMapList 	= null;
		
		lastTestExecutionObjMapList 	= LastCompletedTestExecution(context, busList);
		if(lastTestExecutionObjMapList != null && !lastTestExecutionObjMapList.isEmpty())
		{
			String ParEvolutionWithTitle = "";
			String ParEvolutionWithEndDate = "";
			boolean bothChecked = false;
			ParEvolutionWithTitle    =  PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_selectedEvolutionCurveTypeTitle");		
		    ParEvolutionWithEndDate  =  PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_selectedEvolutionCurveTypeActualEndDate");
			for(int i = 0; i<lastTestExecutionObjMapList.size(); i++)
			{
				Map lastTestExecutionObjMap = (Map)lastTestExecutionObjMapList.get(i);
				
				if((!("false".equalsIgnoreCase(ParEvolutionWithTitle)) && ParEvolutionWithTitle != null) || (!"false".equalsIgnoreCase(ParEvolutionWithEndDate)) && ParEvolutionWithEndDate != null)
				{
					if("ParEvolutionWithTitle".equalsIgnoreCase(ParEvolutionWithTitle)) 
					{
						bothChecked = true;
						String tseId = (String)lastTestExecutionObjMap.get("id");
					   	DomainObject tseObj = newInstance(context, tseId);
					   	if(!"false".equalsIgnoreCase(ParEvolutionWithEndDate))
					   	{
					   		testExexutionname += '"'+tseObj.getAttributeValue(context, "Title");
					   	}else
					   	{
					   		testExexutionname += '"'+tseObj.getAttributeValue(context, "Title")+'"';
					   	}
					}
					
					if("ParEvolutionWithEndDate".equalsIgnoreCase(ParEvolutionWithEndDate)) 
				    {
						String tseEndDate = (String)lastTestExecutionObjMap.get("Date");
						TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
						double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
						double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
						String strFormattedDisplayDateTime = eMatrixDateFormat.getFormattedDisplayDateTime(tseEndDate,clientTZOffset, Locale.getDefault());
						if(bothChecked)
						{
							testExexutionname +=" "+strFormattedDisplayDateTime+'"'+"\n";
						}else
						{
							testExexutionname +='"'+strFormattedDisplayDateTime+'"';	
						}
				    }
				}
				else
				{
					String tesName = (String)lastTestExecutionObjMap.get("name");
					testExexutionname += '"'+tesName+'"';
				}
				testExexutionname +=",";
			}
		}
	
		 return testExexutionname+"]";
 	 }
 	 
 	 // return parameter values for High Chart
 	 public MapList getParameterValues(Context context, String args[]) throws Exception
 	 {
		String parentId_objectId 	= (String) JPO.unpackArgs(args);
		String ids[] 				= parentId_objectId.split(":");
		HashMap hashMap 			= new HashMap();
		HashMap hashMap1 			= new HashMap();
		hashMap1.put("objectId", ids[0]);
		hashMap.put("paramList", hashMap1);
		MapList valuesMap 			= new MapList();
		DomainObject paramObj 		= DomainObject.newInstance(context, ids[1]);
		String paramName 			= paramObj.getInfo(context, DomainConstants.SELECT_NAME);
		String paramRevision 		= paramObj.getInfo(context, DomainConstants.SELECT_REVISION);
		MapList busList    			= new MapList();
		busList 					= getRelatedTestExecutions(context, JPO.packArgs(hashMap));
		StringList returnList 		= new StringList();
		StringList returnListMax 	= new StringList();
		StringList returnListMin 	= new StringList();
		StringList returnListSimple = new StringList();
		HashMap returnMap 			= new HashMap();
		MapList lastTestExecutionObjMapList = null;
		
		valuesMap.add(returnList);
		valuesMap.add(returnListSimple);
		valuesMap.add(returnListMax);
		valuesMap.add(returnListMin);
		
			lastTestExecutionObjMapList = LastCompletedTestExecution(context, busList);
			if(lastTestExecutionObjMapList != null && !lastTestExecutionObjMapList.isEmpty())
			{
				for(int i = 0; i<lastTestExecutionObjMapList.size(); i++)
				{
					
				Map lastTestExecutionObjMap = (Map) lastTestExecutionObjMapList.get(i);
				String objectid 		= (String) lastTestExecutionObjMap.get("id");
				MapList paramObjectList = getAssociatedParameters(context, objectid);
				boolean checkParameter = false;
				
				for(int j = 0; j < paramObjectList.size(); j++)
				{
					Map elem 				= (Map)paramObjectList.get(j);
					String id 				= (String)elem.get("id");
					IPlmParameter parameter = ParameterInterfacesServices.getParameterById(context, id);
					DomainObject object 	= new DomainObject(id);
					String paramRole 		= object.getAttributeValue(context, "PlmParamRole");
					
					if(paramRole.equals(ids[1]+"_"))
					{
						checkParameter = true;
						String strParameterValue 		= getParameterValue(context, parameter);
						IPlmParameter parentParameter 	= ParameterInterfacesServices.getParameterById(context, ids[1]);
						IKweUnit unit		 			= parentParameter.getDisplayUnit(context);
						String paraType 	 			= parentParameter.getDimension(context).getName();
						String displayUnit 	 			= "";
					
							if(unit != null)
							{
								displayUnit = unit.getNLSName(context);	
							}
							if(displayUnit.trim().length() > 0 && !displayUnit.isEmpty())
							{
					    		if(paraType.equals("LENGTHParameter"))
					    		{
					    			strParameterValue = convertToDisplaysValues(context, strParameterValue,  "m", displayUnit);
					    		}else if(paraType.equals("ANGLEParameter"))
					    		{
					    			strParameterValue = convertToDisplaysValues(context, strParameterValue, "rad", displayUnit);
					    		}else if(paraType.equals("MASSParameter"))
					    		{
					    			strParameterValue = convertToDisplaysValues(context, strParameterValue, "kg", displayUnit);
					    		}
							}
						returnList.add(strParameterValue);
						break;
					}
				}
				if(paramObjectList.size() == 0 || !checkParameter){
					returnList.add("0");
				}
			}
		
		IPlmParameter parameter 			= ParameterInterfacesServices.getParameterById(context, ids[1]);
		IPlmParameterDisplay parmDisplay 	= (IPlmParameterDisplay)parameter;
		PLMParm_ValuationType valuationType = parameter.getValuationType(context);
		String strParameterText 			= "";
		int countReturnList 				= returnList.size(); 
		 if(valuationType.equals(PLMParm_ValuationType.SIMPLE))
		 {
			 strParameterText = getParameterValue(context, parameter);
			 for(int i =0; i < returnList.size(); i++)
	        {
	        	returnListSimple.add(strParameterText);
	        }
	        valuesMap.add(returnListSimple);
		 }
		 else if(valuationType.equals(PLMParm_ValuationType.NONE) || valuationType.equals(PLMParm_ValuationType.MULTI))
		 {
			 PLMParm_RangeStatus minStatus = parameter.getMinStatus(context);
	         PLMParm_RangeStatus maxStatus = parameter.getMaxStatus(context);
	         
			 if(!minStatus.equals(PLMParm_RangeStatus.UNDEFINED))
			 {
	        	strParameterText = getParameterMinValue(context, parameter);
	        	for(int i =0; i < returnList.size(); i++)
	        	{
	        		returnListMin.add(strParameterText);
	        	}
        		 valuesMap.add(returnListMin);
             }
			 if(!minStatus.equals(PLMParm_RangeStatus.UNDEFINED))
			 {
				 strParameterText = getParameterMaxValue(context, parameter);
	        	for(int i =0; i < returnList.size(); i++)
	        	{
	        		returnListMax.add(strParameterText);
	        	}
				valuesMap.add(returnListMax);
             }
		 		}
			}
			return valuesMap;
 	 }
 	 
 	public String setParameterString(Context context, String[] args ) throws Exception
    { 
    	try
    	{
    		String[] Param_val = (String[]) JPO.unpackArgs(args);
    		String objectID = Param_val[0];
    		
    		String[] Param_items = Param_val[1].split("[<]");
    		String displayUnit = Param_val[2];
    		if(Param_items.length< 2 )   
    			return "Invalid Parameter Values";
    		
    		Param_items[0] = Param_items[0].trim();
    		String[] ParamMinValue = Param_items[0].split("[ ]");
    		Param_items[1] = Param_items[1].trim();
    		String[] ParamValue = Param_items[1].split("[ ]");
    		Param_items[2] = Param_items[2].trim();
    		String[] ParamMaxValue = Param_items[2].split("[ ]");
    		
    		String paramValues[] = new String[3];
    		paramValues[0] = ParamMinValue[0];
    		paramValues[1] = ParamValue[0];
    		paramValues[2] = ParamMaxValue[0];
    		
    		PLMParm_ValuationStatus status = null;
    		IPlmParameter plmPara = ParameterInterfacesServices.getParameterById(context, objectID);
    		String paraType = plmPara.getDimension(context).getName();
    		PLMParm_ValuationType valuationType = plmPara.getValuationType(context);
    		IKweDictionary kweDico = KweInterfacesServices.getKweDictionary();
    		IKweValueFactory kweFacto = KweInterfacesServices.getKweValueFactory();
    		
    		if(displayUnit.trim().length() > 0 && !displayUnit.isEmpty())
    		{
	    		if(paraType.equals("LENGTHParameter"))
	    		{
	    			paramValues = convertToMksValues(context, paramValues, displayUnit,  "m");
	    		}else if(paraType.equals("ANGLEParameter"))
	    		{
	    			paramValues = convertToMksValues(context, paramValues, displayUnit,  "rad");
	    		}else if(paraType.equals("MASSParameter"))
	    		{
	    			paramValues = convertToMksValues(context, paramValues, displayUnit,  "kg");
	    		}
    		}
    		
    		if(paraType.equals(ParameterTypes.Boolean)){
    			
    			plmPara.valuate(context, Boolean.parseBoolean(paramValues[1]));
    			
    		}else if(paraType.equals(ParameterTypes.String)){
    			String stringValue = "";
    			for(int i = 0; i < ParamValue.length; i++ )
    			{
    				stringValue +=ParamValue[i]+" ";
    			}
    			if(valuationType.equals(PLMParm_ValuationType.MULTI))
    			{
    				String[] multiVal = stringValue.split(",");
    				StringList values1 = new StringList();
    				IKweValue listValue = kweDico.findType(context, KweTypes.List).createValue(context, null);
    				IKweList listValues = (IKweList)listValue.instanceValue();
    				
    				for (int i=1; i <= multiVal.length; i++)
    					
    					if(multiVal[i-1].trim().length() > 0)
    					listValues.InsertItem(i, kweFacto.createString(context, multiVal[i-1]));

    				plmPara.setMultiValues(context, listValues);
    				
    			}else{
    				plmPara.valuate(context, stringValue);	
    			}
    		}else{
    			PLMParm_RangeStatus maxRangeStatus = plmPara.getMaxStatus(context);
        		PLMParm_RangeStatus minRangeStatus = plmPara.getMinStatus(context);
        		
        		IKweValue listValue = kweDico.findType(context, KweTypes.List).createValue(context, null);
				IKweList listValues = (IKweList)listValue.instanceValue();
				
        		if(maxRangeStatus.equals(PLMParm_RangeStatus.UNDEFINED) && ParamMaxValue[0].length() > 0)
        		{
        			maxRangeStatus = PLMParm_RangeStatus.INCLUSIVE;
        		}
        		
        		if(minRangeStatus.equals(PLMParm_RangeStatus.UNDEFINED) && ParamMinValue[0].length() > 0)
        		{
        			minRangeStatus = PLMParm_RangeStatus.INCLUSIVE;
        		}
        		
        		if(valuationType.equals(PLMParm_ValuationType.MULTI))
				{
	        		String[] multiVal = paramValues[1].split(",");
	        		for (int i=1; i <= multiVal.length; i++)
	        		{	
						if(multiVal[i-1].trim().length() > 0)
						listValues.InsertItem(i, kweFacto.createString(context, multiVal[i-1]));
	        		}
				}
        		
    			if(ParamMaxValue[0].length() > 0 && ParamMinValue[0].length() > 0)
    			{
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    				
    				if(!valuationType.equals(PLMParm_ValuationType.MULTI))
    				{
    					if(ParamValue[0].length() > 0 )
            			{
        					plmPara.valuate(context, Double.parseDouble(paramValues[1]));
            			}
        			}else{
    					plmPara.setMultiValues(context, listValues);
    				}
    				plmPara.setRange(context, Double.parseDouble(paramValues[0]), minRangeStatus, Double.parseDouble(paramValues[2]), maxRangeStatus);
    				plmPara.setRange(context, Double.parseDouble(paramValues[0]), minRangeStatus, Double.parseDouble(paramValues[2]), maxRangeStatus);
				
    			}
    			else if(ParamMaxValue[0].length() > 0)
    			{
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    				
    				if(!valuationType.equals(PLMParm_ValuationType.MULTI))
    				{
    					if(ParamValue[0].length() > 0 )
            			{
        					plmPara.valuate(context, Double.parseDouble(paramValues[1]));
            			}
        				
    				}else{
    					plmPara.setMultiValues(context, listValues);
    				}
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, Double.parseDouble(paramValues[2]), maxRangeStatus);
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, Double.parseDouble(paramValues[2]), maxRangeStatus);	
    			}
    			else if(ParamMinValue[0].length() > 0)
    			{
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    				
    				if(!valuationType.equals(PLMParm_ValuationType.MULTI))
    				{
	    				if(ParamValue[0].length() > 0)
	        			{
	    					plmPara.valuate(context, Double.parseDouble(paramValues[1]));
	        			}
	    				
    				}else{
    					plmPara.setMultiValues(context, listValues);
    				}

    				plmPara.setRange(context, Double.parseDouble(paramValues[0]), minRangeStatus, 0, PLMParm_RangeStatus.UNDEFINED);
    				plmPara.setRange(context, Double.parseDouble(paramValues[0]), minRangeStatus, 0, PLMParm_RangeStatus.UNDEFINED);
    			}
    			else
    			{
    				if(!valuationType.equals(PLMParm_ValuationType.MULTI))
    				{
    					if(ParamValue[0].length() > 0 )
            			{
        					plmPara.valuate(context,  Double.parseDouble(paramValues[1]));
            			}else{
            				plmPara.unset(context);
            			}
        				
    				}else{
    					plmPara.setMultiValues(context, listValues);
    				}
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    			}
    			
    			boolean flag = plmPara.isValueSet(context);
    			if(flag & displayUnit.trim().length() > 0){
    				
    				for (int i = 0; i < ParameterInterfacesServices.getRelatedUnits(context, plmPara.getDimension(context)).size(); i++)
    				{
    					String key = ParameterInterfacesServices.getRelatedUnits(context, plmPara.getDimension(context)).get(i).getSymbol();
    					
    					if(key.equals(displayUnit)){
    						plmPara.setDisplayUnit(context,ParameterInterfacesServices.getRelatedUnits(context, plmPara.getDimension(context)).get(i));
    					}
    				}
    			}
    		}
    		return "OK";
    	}
    	catch(Exception ex)
    	{ 
    		String error = ex.toString();
    		return error;
    	}
    }
 	// -- KIE1 ZUD : HL Parameter under Test Execution

    // ++ HAT1 ZUD: HL -  To enable Content column for Test Cases.
	/**
     * Returns a boolean value if CK editor to be display at place of normal text editor.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - None
     * @return the link
     * @throws Exception
     */
    public static boolean isDescriptionCKEditorAvailable(Context context, String[] args)
    {
    	//By-default Description field with CK editor will be displayed.
	    return true;
    }
    
	/**
     * Returns a boolean value if CK editor to be display at place of normal text editor.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - None
     * @return the link
     * @throws Exception
     */
    public static boolean isDescriptionTextEditorAvailable(Context context, String[] args)
    {
    	//By-default Description field with Text editor will be hidden.
	    return false;
    }
    // -- HAT1 ZUD: HL -  To enable Content column for Test Cases

    // ++ HAT1 ZUD: Populating title as per autoName of Name in Web form.
	/**
     * Method to fill web form Name field using autoName.
     * @param context
     * @param args
     * @return String Name field value
     * @throws Exception
     */
    public String getNameFieldValue(Context context, String[] args) throws Exception
    {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map requestMap = (HashMap) programMap.get("requestMap");
    	Map fieldMap   = (HashMap) programMap.get("fieldMap");
        String name = (String)fieldMap.get("name");
    	
        String type = (String)requestMap.get("type");

        if(type.startsWith("_selectedType:")) {
        	type = type.substring("_selectedType:".length());
        }
        type = type.split("[,]", -1)[0];

        String strName = "";
        
        if(name.equalsIgnoreCase("Name"))
        {
        	
        	if(type.startsWith("type_")){
        		type = PropertyUtil.getSchemaProperty(context, type);
        	}
            strName = UnifiedAutonamingServices.autoname(context, type);
            	
        }

        return strName;
    }
    
	/**
     * Method to set Name field of TC & TE.
     * @param context
     * @param args
     * @return Name value.
     * @throws Exception
     */
    public String SetNameFieldValue(Context context, String[] args) throws Exception 
    {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map fieldMap   = (HashMap) programMap.get("fieldMap");
    	String name = (String)fieldMap.get("name");
        Map paramMap = (HashMap) programMap.get("paramMap");
        String newValue = (String) paramMap.get("New Value");
        String strObjectId = (String) paramMap.get("objectId");
        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
        String type = domObj.getTypeName();
        ContextUtil.startTransaction(context, true);

        if(name.equalsIgnoreCase("Name"))
        	domObj.setName(context,newValue);
        
        ContextUtil.commitTransaction(context);
        
    	return newValue;
    }
    // -- HAT1 ZUD: Populating title as per autoName of Name in Web form.
    
 
	public boolean hideEditLink (Context context,String[] args) throws FrameworkException { 
  		boolean isPLCUser=false;
  		String Licenses[] = {"ENO_PLC_TP"};	    
  		try {
  			FrameworkLicenseUtil.checkLicenseReserved(context,Licenses);
  			isPLCUser = true;
		}catch (Exception e){
			isPLCUser = false;
		}		
  		if(!isPLCUser){
  			return true;
  		}
  		return false;	 
  	 }
	
	public boolean showEditLink (Context context,String[] args) throws FrameworkException { 
  		boolean isPLCUser=false;
  		String Licenses[] = {"ENO_PLC_TP"};
  		try {
  			FrameworkLicenseUtil.checkLicenseReserved(context,Licenses);
  			isPLCUser = true;
		}catch (Exception e){
			isPLCUser = false;
		}
  		if(isPLCUser){
  			return true;
  		}
  		return false;
  	 }
	
	
	// ++ HAT1 ZUD IR-276718-3DEXPERIENCER2017x fix
	/**
	 * Method to provide settings for form type_TestCase or Req-Test_Case_traceability validation status field of object test case (To show or not.)
	 * @param Context context
	 *        args[] - arguments provided
	 * @return boolean value - true - to show the field/Column, false- hide the field/column.
	 */
	public boolean isValidationStatusOfTestCase(Context context, String[] args) 
	{
		return false;
	}
	
	/**
	 * Method to provide settings for form type_TestCase or Req-Test_Case_traceability validation status field for "Test Execution Test Case" Relationship(To show or not.)
	 * @param Context context
	 *        args[] - arguments provided
	 * @return boolean value - true - to show the field, false- hide the field.
	 */
	public  boolean isValidationStatusRelTETC(Context context, String[] args) 
	{
		return true;
	}
	
	/**
     * Finds the Last completed Test Execution under Test Case.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - All the Test Executions created for a Test Case
     * @return the last completed Test Execution, null if no such TE.
     * @throws Exception
     */
    public Map getLastCompletedTestExecution(Context context, MapList TestCaseTestExecutionList)
    {
    	Map<String, String> currTestExecutionObjMap = null;
    	Map<String, String> lastTestExecutionObjMap = null;            // Map for Last connected Test Execution to Test Case.
    	
    	Date currentDate = new Date();                                 // currentDate object of type Date.
    	Date dateLastActualEndDateTE = currentDate;                    // Populating 'dateLastActualEndDateTE' with currentDate which will hold attribute 'Actual End Date' of Last connected Test Execution.
    	Date currActualEndDateTE = null;
    	    	
    	if(TestCaseTestExecutionList.size() != 0)
        {
	    	Iterator testExecutionItr = TestCaseTestExecutionList.iterator();
	    	int isAnyLastTEfound = 0;
			while(testExecutionItr.hasNext())
			{
				currTestExecutionObjMap = (Map) testExecutionItr.next();
				
				try
				{
		        	DomainObject domObj = DomainObject.newInstance(context, (String)currTestExecutionObjMap.get("id"));
					currActualEndDateTE	= eMatrixDateFormat.getJavaDate((String) domObj.getAttributeValue(context, "Actual End Date"));
				} 
				catch (Exception e) 
				{
					continue;
				}
				
				int flag = currActualEndDateTE.compareTo(dateLastActualEndDateTE);
				
				if(isAnyLastTEfound == 0 && (flag == -1))
				{
					isAnyLastTEfound = 1;
					lastTestExecutionObjMap = currTestExecutionObjMap;
					dateLastActualEndDateTE = currActualEndDateTE;
				}
				
				if(isAnyLastTEfound == 1 && flag == 1)
				{
					lastTestExecutionObjMap = currTestExecutionObjMap;
					dateLastActualEndDateTE = currActualEndDateTE;
				}
				
			}
        }
		return lastTestExecutionObjMap;
    }
	
	
	/**
	 * Method to provide Validation Status of Last Completed Test Execution.
	 * @param Context context
	 *        args[] - arguments provided
	 * @return boolean value 
	 */
	public  String lastCompletedTestExecutionStatus(Context context, String[] args) 
	{
        String TC_NO_TE_REPLAYED    = EnoviaResourceBundle.getProperty(context, "emxProductLineStringResource", context.getLocale(), "emxProductLine.TestCase.LastReplayedTestExecution.NoTEReplayed"); 
        String validationStatus = "<b>" + TC_NO_TE_REPLAYED + "</b>";
		try 
		{
			Map map = (HashMap) JPO.unpackArgs(args);
			Map paramMap = (HashMap) map.get("paramMap");

			//Creating map as per getRelatedTestExecutions() function.
			Map paramList = new HashMap();
			paramList.put("paramList", paramMap);
			String[] args1 = JPO.packArgs(paramList);

			MapList testExecutionList   = getRelatedTestExecutions(context,args1);
			
		    MapList lstTEList = LastCompletedTestExecution(context, testExecutionList);
		    Map lstTE = getLastCompletedTestExecution(context, lstTEList);
		    
	    	Map lstTEmap = new HashMap();
	    	if(lstTE.size() != 0)
	    	{
		    	lstTEmap = lstTE;
		    	
				String relId = (String) lstTEmap.get("id[connection]");
	        	Relationship rel = new DomainRelationship(relId);
	        	rel.open(context);
	        	
	            AttributeItr attrRelItrGeneric   = new AttributeItr(rel.getAttributes(context));
	
	            while (attrRelItrGeneric.next()) 
	            {
	                Attribute attrGeneric = attrRelItrGeneric.obj();
	                String sAttrValue = attrGeneric.getName();
	                
	                if(sAttrValue.equals(DomainConstants.ATTRIBUTE_VALIDATION_STATUS)) 
	                {
	                	validationStatus = attrGeneric.getValue();
	                	validationStatus = "<i>" + validationStatus + "</i>";
	                	break;
	                } 
	            }
	            rel.close(context);
	    	}

	    }
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return validationStatus;
	}
	
	
	/**
	 * Method to provide attribute value of Validation Status of relationship obj(bw Last Completed Test Execution and its Test Case).
	 * @param Context context
	 *        args[] - arguments provided
	 * @return boolean value 
	 */
	public  List lastCompletedTestExecutionsStatusList(Context context, String[] args) 
	{
		List valStatusList = null;
		try 
		{
			Map programMap     = (HashMap) JPO.unpackArgs(args);
			Map paramList      = (Map) programMap.get("paramList");
			MapList objectList = (MapList) programMap.get("objectList");
			
			String languageStr = (String)paramList.get("languageStr");
			
			Map mapForArgs1 = new HashMap();
			Map map         = new HashMap();
			
			valStatusList = new Vector(objectList.size());
			for(int i = 0; i< objectList.size(); i++)
			{
				Map m = (Map)objectList.get(i);
				
				//Creating map as per lastCompletedTestExecutionStatus() function.
				map.put("objectId", (String) m.get("to.id"));
				map.put("relId", (String) m.get("id[connection]"));
				map.put("languageStr", languageStr);
				
				mapForArgs1.put("paramMap", map);
				String[] args1 = JPO.packArgs(mapForArgs1);
				String relValStatus = lastCompletedTestExecutionStatus(context, args1); 
				valStatusList.add(relValStatus);
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valStatusList;
	}
	// -- HAT1 ZUD IR-276718-3DEXPERIENCER2017x fix
}//End of class

