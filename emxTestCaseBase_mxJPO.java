/*
 *  emxTestCaseBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */

 /**
 * @quickreview JX5 QYG	13:06:19 : IR-231962V6R2014x It cannot edit Test Case when browser language is Japanese
 * @quickreview T25 DJH 13:09:24 : IR-255996V6R2014x "The estimated completion date in the Test Case is not taking into account." Modified validateEstimateDate Method.
 * @quickreview T25 DJH 14:03:18 : HL Parameter under Test Case.Add method getAssociatedParameters.
 * @quickreview ZUD DJH 14:07:21 : Modified method getAssociatedParameters for IR-298238-3DEXPERIENCER2015x and IR-298340-3DEXPERIENCER2015x.
 * @quickreview KIE1 ZUD 15:04:06 : IR-352105-3DEXPERIENCER2016x R417-STP: Infinite loop is displayed for single node of Testcase in tree structure.
 * @quickreview JX5  QYG 15:07:07 : Add getStateColumnHTML to display color code for State column
 * @quickreview HAT1 ZUD 16:02:03 : HL -  To enable Content column for Test Cases.
 * @quickreview HAT1 ZUD 16:02:03 : HL -  (xHTML editor for Use case.) To enable Content column for Test Cases. function name changes from getTestCasesContentData to getContentDataForTable. 
 * @quickreview HAT1 ZUD 16:06:14 : IR-439329-3DEXPERIENCER2017x: R419-STP:Test Case is getting promoted to Release state when sub testcase inside it is still in "In Work" state. Function added: 
 * @quickreview HAT1 ZUD 16:07:28 : IR-390003-3DEXPERIENCER2017xR2016x_ConfigurationFeature_TestCase: Allowing to create Test Case with Estimated Completion date as Backdate. method added validateCreateFormEstimateDate().  
 * @quickreview ZUD      16:08:17 : Reserve/UnReserve Command for RMT Types in attribute Tab  
 */


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import  com.matrixone.apps.domain.DomainConstants;
import  com.matrixone.apps.domain.DomainObject;
import  com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import  com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
//HAT1 ZUD (RMT): To enable content column for Test Case.
import com.matrixone.apps.domain.util.MqlUtil;
//import com.matrixone.apps.requirements.RequirementsUtil;
//import com.matrixone.apps.requirements.convertor.engine.util.ConvertedDataDecorator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
//import org.apache.axis.encoding.Base64;
import matrix.db.BusinessObject;

import  com.matrixone.apps.domain.util.PropertyUtil;
import  com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.TestCase;
import  java.util.HashMap;
import  java.util.Hashtable;
import  java.util.Iterator;
import java.util.List;
import java.util.Locale;
import  java.util.Map;
import  java.util.Vector;

import  matrix.db.Context;
import  matrix.db.JPO;
import matrix.util.MatrixException;
import  matrix.util.StringList;
import  matrix.util.Pattern;

/**
 * The <code>emxTestCase</code> class contains methods related to Test Case admin type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxTestCaseBase_mxJPO extends emxDomainObject_mxJPO {

    /** A string constant with the value ".". */
    public static final String SYMB_DOT                  = ".";

    /**
     *A string constant with the value emxFrameworkStringResource.
     */
    public static final String RESOURCE_BUNDLE_FRAMEWORK_STR = "emxFrameworkStringResource";

    // ++ HAT1 ZUD IR-439329-3DEXPERIENCER2017x fix
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
    public static final String SYMBOLIC_STATE_COMPLETE = "state_Complete";
    /**
     * Variable for Obsolete state
     */
    public static final String SYMBOLIC_STATE_OBSOLETE = "state_Obsolete";
    
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
    // -- HAT1 ZUD IR-439329-3DEXPERIENCER2017x fix
    
    /**
     * Default constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF
     * @grade 0
     */
    public emxTestCaseBase_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context context for this request
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String language = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context,"ProductLine",
                   "emxProduct.Alert.FeaturesCheckFailed",language);
            throw  new Exception(strContentLabel);
        }
        return  0;
    }

    /**
     * Get the list of all TestCases on the context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing the object id.
     * @return bus ids  and rel ids of Test Cases
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedTestCases (Context context, String[] args) throws Exception {
        MapList relBusObjPageList = new MapList();
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        setId(strObjectId);
        String strRel = (String)programMap.get("rel");
        short sRecursionLevel = 1;
        String strType = ProductLineConstants.TYPE_TEST_CASE;
        String strRelName = PropertyUtil.getSchemaProperty(context,strRel);
        relBusObjPageList = getRelatedObjects(context, strRelName, strType,
                objectSelects, relSelects, false, true, sRecursionLevel, "",
                "");
        return  relBusObjPageList;
    }

    /**
     * Get the list of all parent objects of the contextTestCase context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing the object id.
     * @return bus ids  of parent objectsand rel ids of Test Cases
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    public MapList getTestCasesWhereUsed (Context context, String[] args) throws Exception {
        MapList relBusObjPageList = new MapList();
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        setId(strObjectId);
        String strTestCaseSubTestCaseReln = ProductLineConstants.RELATIONSHIP_SUB_TEST_CASE;
        String strTestCaseUseCaseReln = ProductLineConstants.RELATIONSHIP_USE_CASE_VALIDATION;
        String strTestCaseIncidentReln = ProductLineConstants.RELATIONSHIP_INCIDENT_VALIDATION;
        String strTestCaseRequirementReln = ProductLineConstants.RELATIONSHIP_REQUIREMENT_VALIDATION;
        String strTestCaseFeatureReln = ProductLineConstants.RELATIONSHIP_FEATURE_TEST_CASE;
        String strComma = ",";
        String strRelationshipPattern = strTestCaseSubTestCaseReln + strComma
                + strTestCaseUseCaseReln + strComma + strTestCaseIncidentReln
                + strComma + strTestCaseRequirementReln + strComma + strTestCaseFeatureReln;
        short sRecursionLevel = 1;
        relBusObjPageList = getRelatedObjects(context, strRelationshipPattern,
                "*", objectSelects, relSelects, true, false, sRecursionLevel,
                "", "");
        return  relBusObjPageList;
    }

    /** This method gets the object Structure List for the context Test Case object.This method gets invoked
      * by settings in the command which displays the Structure Navigator for Test Case type objects
      * @param context the eMatrix <code>Context</code> object
      * @param args    holds the following input arguments:
      *      paramMap   - Map having object Id String
      * @return MapList containing the object list to display in Test Case structure navigator
      * @throws Exception if the operation fails
      * @since Product Central 10-6
      */
    public static MapList getStructureList(Context context, String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap)programMap.get("paramMap");
        String objectId    = (String)paramMap.get("objectId");

        MapList testCaseStructList = new MapList();

        Pattern tcTypePattern      = new Pattern(ProductLineConstants.TYPE_TEST_CASE);
        Pattern subTCRelPattern    = new Pattern(ProductLineConstants.RELATIONSHIP_SUB_TEST_CASE);
        //Parameter under Test Case HL: Added plm parameter type and relationship 
        tcTypePattern.addPattern(PropertyUtil.getSchemaProperty(context, "type_PlmParameter"));
        subTCRelPattern.addPattern(PropertyUtil.getSchemaProperty(context, "relationship_ParameterAggregation"));
        DomainObject testCaseObj   = DomainObject.newInstance(context, objectId);
        String objectType          = testCaseObj.getInfo(context, DomainConstants.SELECT_TYPE);
      //Added mxType for IR-092856V6R2012 
        if(objectType != null && mxType.isOfParentType(context, objectType,ProductLineConstants.TYPE_TEST_CASE)){
            try {
                //START:LX6 JX5 14:06:06 : IR-302553-3DEXPERIENCER2015x
                // expand for all connected Sub Test Cases, Parameters and Test Execution
                DomainObject domObj = DomainObject.newInstance(context, objectId);

                StringList objectSelects = new StringList(3);
                objectSelects.add(DomainConstants.SELECT_ID);
                objectSelects.add(DomainConstants.SELECT_TYPE);
                objectSelects.add(DomainConstants.SELECT_NAME);
                StringList relSelects = new StringList(1);
                relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

                //expand on the relationships in the passed relPattern object
                //fix for IR-352105-3DEXPERIENCER2016x
                testCaseStructList = domObj.getRelatedObjects(context,                  // matrix context
                											  subTCRelPattern.getPattern(),  // all relationships to expand
                											  tcTypePattern.getPattern(), // all types required from the expand
                                                              objectSelects,            // object selects
                                                              relSelects,               // relationship selects
                                                              false,                    // to direction
                                                              true,                     // from direction
                                                              (short) 1,                // recursion level
                                                              "",                       // object where clause
                                                              "");                      // relationship where clause
                // return expanded object connections
                //END : LX6 JX5 14:06:06 : IR-302553-3DEXPERIENCER2015x
            }
            catch(Exception ex){
                throw new FrameworkException(ex);
            }
        } else {
            testCaseStructList = (MapList) emxPLCCommon_mxJPO.getStructureListForType(context, args);
        }
        return testCaseStructList;
    }

    /**
     * Gets the list of all TestCases on the context Test Execution that have the
     * 'Validation Status' attribute value on the connecting relationship as 'Validation Passed'. This
     *  method will be able to show flat view of all such Test Cases connected to Test Execution
     * as during Test Execution create all Test Cases from parent object are collected into a flat structure
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing the object id.
     * @return        a <code>MapList</code> object having the list of all Test Cases connected
     *                to the context Test Execution with 'Validation Passed' value for 'Validation Status'
     * @throws        Exception if the operation fails
     * @since         ProductCentral 10-6
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPassedTestExecutionTestCases (Context context, String[] args)
        throws Exception {

        MapList relBusObjPageList = new MapList();

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        String strRel      = (String)programMap.get("rel");
        String strRelName  = PropertyUtil.getSchemaProperty(context,strRel);

        StringBuffer sbRelWhere  = new StringBuffer(getAttributeSelect(DomainConstants.ATTRIBUTE_VALIDATION_STATUS));
        sbRelWhere.append("== \"");
        sbRelWhere.append(getAttributeRangeValue(context,DomainConstants.ATTRIBUTE_VALIDATION_STATUS, TestCase.TEST_CASE_VALIDATION_PASSED));
        sbRelWhere.append("\"");

        relBusObjPageList = getTestExecutionTestCases(context,
                                                      strObjectId,
                                                      strRelName,
                                                      EMPTY_STRING,
                                                      sbRelWhere.toString());
        return  relBusObjPageList;
    }

    /**
     * Gets the list of all TestCases on the context Test Execution that have the
     * 'Validation Status' attribute value on the connecting relationship as 'Validation Failed'. This
     *  method will be able to show flat view of all such Test Cases connected to Test Execution
     * as during Test Execution create all Test Cases from parent object are collected into a flat structure
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing the object id.
     * @return        a <code>MapList</code> object having the list of all Test Cases connected
     *                to the context Test Execution with 'Validation Failed' value for 'Validation Status'
     * @throws        Exception if the operation fails
     * @since         ProductCentral 10-6
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getFailedTestExecutionTestCases (Context context, String[] args)
        throws Exception {

        MapList relBusObjPageList = new MapList();

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        String strRel      = (String)programMap.get("rel");

        String strRelName   = PropertyUtil.getSchemaProperty(context,strRel);

        StringBuffer sbRelWhere = new StringBuffer(getAttributeSelect(DomainConstants.ATTRIBUTE_VALIDATION_STATUS));
        sbRelWhere.append("== \"");
        sbRelWhere.append(getAttributeRangeValue(context,DomainConstants.ATTRIBUTE_VALIDATION_STATUS, TestCase.TEST_CASE_VALIDATION_FAILED));
        sbRelWhere.append("\"");

        relBusObjPageList = getTestExecutionTestCases(context,
                                                      strObjectId,
                                                      strRelName,
                                                      EMPTY_STRING,
                                                      sbRelWhere.toString());
        return  relBusObjPageList;
    }

    /**
     * Gets the list of all TestCases on the context Test Execution that have the
     * 'Validation Status' attribute value on the connecting relationship as 'Not Validated'. This
     * method will be able to show flat view of all such Test Cases connected to Test Execution
     * as during Test Execution create all Test Cases from parent object are collected into a flat structure
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing the object id.
     * @return        a <code>MapList</code> object having the list of all Test Cases connected
     *                to the context Test Execution with 'Not Validated' value for 'Validation Status'
     * @throws        Exception if the operation fails
     * @since         ProductCentral 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getNotValidatedTestExecutionTestCases (Context context, String[] args)
        throws Exception {

        MapList relBusObjPageList = new MapList();

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        String strRel      = (String)programMap.get("rel");

        String strRelName        = PropertyUtil.getSchemaProperty(context,strRel);

        StringBuffer sbRelWhere  = new StringBuffer(getAttributeSelect(DomainConstants.ATTRIBUTE_VALIDATION_STATUS));
        sbRelWhere.append("== \"");
        sbRelWhere.append(getAttributeRangeValue(context,DomainConstants.ATTRIBUTE_VALIDATION_STATUS, TestCase.TEST_CASE_NOT_VALIDATED));
        sbRelWhere.append("\"");

        relBusObjPageList = getTestExecutionTestCases(context,
                                                      strObjectId,
                                                      strRelName,
                                                      EMPTY_STRING,
                                                      sbRelWhere.toString());
        return  relBusObjPageList;
    }

    /**
     * Gets the list of all TestCases on the context Test Execution based on the 'bus where'
     * and relationship 'where' search clauses passed in as parameters for the object
     *
     * @param context              the eMatrix <code>Context</code> object
     * @param strParentId          id of the object for which Test Case relationship expansion is done
     * @param strRelName           the relationship that connects the parent object and child Test Cases
     * @param strBusWhereCondition the 'business object where' clause to be applied while expanding for 'Test Case' connections
     * @param strRelWhereCondition the 'relationship where' clause to be applied while expanding for 'Test Case' connections
     * @return                     a <code>MapList</code> object having the list of all connected Test Cases meeting search criteria
     * @throws                     Exception if the operation fails
     * @since                      ProductCentral 10-6
     */

    protected MapList getTestExecutionTestCases (Context context, String strParentId,
                                                 String strRelName, String strBusWhereCondition,
                                                 String strRelWhereCondition)
        throws Exception {

        MapList relBusObjPageList = new MapList();

        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

        setId(strParentId);

        short sRecursionLevel = 1;
        String strType        = ProductLineConstants.TYPE_TEST_CASE;

        relBusObjPageList = getRelatedObjects(context,
                                              strRelName,
                                              strType,
                                              objectSelects,
                                              relSelects,
                                              false,
                                              true,
                                              sRecursionLevel,
                                              strBusWhereCondition,
                                              strRelWhereCondition);
        return  relBusObjPageList;
    }

    /**
     * The method gets the Status Column in Test Execution Test Case Summary Table.
     * Returns the Status icon gif depending on the relationship attribute 'Validation Status'.
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *          0 -   MapList containing Relationship id list
     * @return        a <code>Vector</code> object to return Status gif in Test Execution Test Case Summary.
     * @throws        Exception if the operation fails
     * @since         Product Central 10-6
     **
     */
    public Vector showTestCaseStatusGif(Context context, String[] args) throws Exception {
    	//XSSOK
        Vector returnVector   = new Vector();
        HashMap programMap    = (HashMap)JPO.unpackArgs(args);
        MapList objectList    = (MapList)programMap.get("objectList");
        int objectListSize    = 0 ;

        if(objectList != null) {
            objectListSize = objectList.size();
        }

        //Constructing the Object ids String []
        String stridsArray[]    = new String[objectListSize];
        for (int i = 0; i < objectListSize; i++) {
            try {
                   stridsArray[i] = (String)((HashMap)objectList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
            } catch (Exception ex) {
                   stridsArray[i] = (String)((Hashtable)objectList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }
        }

        //Getting the Relationship attribute, Validation Status
        String selectAttrValidationStatus = "attribute["+DomainConstants.ATTRIBUTE_VALIDATION_STATUS+"]";
        StringList listSelect = new StringList(selectAttrValidationStatus);
        MapList attributeMapList = DomainRelationship.getInfo(context,stridsArray,listSelect);
        Iterator attLstItr = attributeMapList.iterator();
        //Iterating through the attribute maplist and constructing the return vector.
        while(attLstItr.hasNext()) {
            Map tmpAtrMap = (Map)attLstItr.next();
            String AttrValidationStatus = (String)( tmpAtrMap.get(selectAttrValidationStatus) );
            if(AttrValidationStatus != null && AttrValidationStatus.equals(TestCase.TEST_CASE_VALIDATION_PASSED)) {
                returnVector.add("<img border='0' src='../common/images/iconStatusGreen.gif' />");
            } else if(AttrValidationStatus != null && AttrValidationStatus.equals(TestCase.TEST_CASE_VALIDATION_FAILED)) {
                returnVector.add("<img border='0' src='../common/images/iconStatusRed.gif' />");
            } else if(AttrValidationStatus != null && AttrValidationStatus.equals(TestCase.TEST_CASE_NOT_VALIDATED)) {
            	returnVector.add("&#160;");
            } else {
            	returnVector.add("&#160;");
            }
        } //end of while
        return returnVector;
    }

    /**
     * This method returns the value for a perticular range of a attribute
     * reading from emxFrameworkStringResource.propeties file. The key value is
     * dynamically generated using the passed attribute name and range
     * "name.emxFramework.Range. <Attribute Name>. <Range Name>". This value can
     * not be used for display purpose as the language string in GetString
     * method is passed as blank and so always engish string will be returned.
     *
     * @param strAttributeName String holding the actual name of the attribute.
     * @param strRange String holding the range of the attribbute.
     * @return        String The property file value of the range.
     * @throws        Exception if the operation fails
     * @since         Product Central 10-6
     */
    protected String getAttributeRangeValue(Context context,
            String strAttributeName, String strRange) throws Exception {

        //Form the property file key using the passed attribute name and range
        // name.The white spaces in the attribute and range name will be
        // replaced by underscore(_).
        StringBuffer sbKey = new StringBuffer();
        sbKey.append("emxFramework.Range.");
        sbKey.append(strAttributeName.replace(
                ' ', '_'));
        sbKey.append(SYMB_DOT);
        sbKey.append(strRange.replace(
                ' ', '_'));

        //Read the property file value by passing the generated key and
        // language string as blank.
        String strRangeValue = EnoviaResourceBundle.getProperty(context, "Framework",
                sbKey.toString(),DomainConstants.EMPTY_STRING);

        return strRangeValue;
    }
    
    /**
     * To obtain the list of Object IDs to be excluded from the search for Add Existing Actions
     *
     * @param context- the eMatrix <code>Context</code> object
     * @param args- holds the HashMap containing the following arguments
     * @return  StringList- consisting of the object ids to be excluded from the Search Results
     * @throws Exception if the operation fails
     * @author OEP:R208:Bug 370645
     */
    
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeSubTestCases(Context context, String[] args) throws Exception
    {
	        Map programMap = (Map) JPO.unpackArgs(args);    
	        String strObjectIds = (String)programMap.get("objectId");
	        String strRelationship=(String)programMap.get("relName");
	        StringList excludeList= new StringList();  
	        DomainObject domObjTestCase  = new DomainObject(strObjectIds);
	        

	        // Code for removing the Parent Id's
	        MapList parentObjects=domObjTestCase.getRelatedObjects(context, 
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
	            excludeList.add(tempMap.get(DomainConstants.SELECT_ID));
	        }
	        
	        // Code use to remove those objects which are already added in list.
	       MapList currentObjectIDs=domObjTestCase.getRelatedObjects(context, 
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
    
    /**
     * Estimated Completion Date field make mandatory or not based on a configuration property entry.
     * Called from WebForm type_TestCase
     * @param context- the eMatrix <code>Context</code> object
     * @param args- holds the HashMap containing the following arguments
     */
    
    public static boolean isEstimatedDateRequired(Context context, String[] args) throws Exception
	{
    	try
    	{
		String strEnforceMandatoryEstimatedCompletionDate = EnoviaResourceBundle 
					.getProperty(context,
							"emxProduct.TestCase.EnforceEstimatedCompletionDate");

			if ("true".equals(strEnforceMandatoryEstimatedCompletionDate)) {
				return true;
			} else {
				return false;
			}
    	}  catch(Exception ex){
            throw new FrameworkException(ex);
        }
	}
    
    /**
     * Estimated Completion Date field make mandatory or not based on a configuration property entry
     * Called from WebForm type_TestCase
     * @param context- the eMatrix <code>Context</code> object
     * @param args- holds the HashMap containing the following arguments
     */
    
    public static boolean isEstimatedDateNotRequired(Context context, String[] args) throws Exception
	 	{
    	try {
			String strEnforceMandatoryEstimatedCompletionDate = EnoviaResourceBundle
					.getProperty(context,
							"emxProduct.TestCase.EnforceEstimatedCompletionDate");
			if ("false".equals(strEnforceMandatoryEstimatedCompletionDate)) {
				return true;
			} else {
				return false;
			}
    	}  catch(Exception ex){
            throw new FrameworkException(ex);
        }
	}
    
    /**
     * Method is used to validate the Estimated Completion Date field
     * 
     * @param context the eMatrix <code>Context</code> object 
     * @param args hold the hashMap containing the following argument
     * @returns void 
     * @throws Exception if operation fails.
     * @since R212
     */
    public void validateEstimateDate(Context context, String[] args) throws Exception
    {
    	try{
    	  HashMap hashMap = (HashMap)JPO.unpackArgs(args);
          HashMap paramMap = (HashMap) hashMap.get("paramMap");
          String strObjectId = (String) paramMap.get("objectId");
          String strNewValue = (String) paramMap.get("New Value");
          //JX5 IR-231962V6R2014x
          HashMap requestMap = (HashMap) hashMap.get("requestMap");
          DateFormat df = new SimpleDateFormat();
          Calendar calendar =  Calendar.getInstance();
          
          java.util.Date strEstimatedCompletionDate =null;
          
		 //START: T25 DJH 2013:09:24:IR IR-255996V6R2014x .Correction done by JX5 for IR 231962V6R2014x is taken out of else block.
    	  double iClientTimeOffset = (new Double((String) requestMap.get("timeZone"))).doubleValue();
    	  Locale locale = (Locale)requestMap.get("localeObj");
    	  strNewValue = eMatrixDateFormat.getFormattedInputDate(context, strNewValue,
                  iClientTimeOffset,
                  locale);
    	  //END T25 DJH
          strEstimatedCompletionDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strNewValue);
          //Start KIE1 HAT1 : IR-296927-3DEXPERIENCER2016
          java.util.Date strOldEstimatedCompletionDate =null;
          String strOldValue = (String) paramMap.get("Old value");
          strOldEstimatedCompletionDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strOldValue);
          if(!strOldEstimatedCompletionDate.equals(strEstimatedCompletionDate)){
          //End KIE1 HAT1
        	  calendar.setTime(strEstimatedCompletionDate);
              Date newCompletionDate = calendar.getTime();
              int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
              
              Date dtTodaysDate = df.parse(df.format(new Date()));
              calendar.setTime(dtTodaysDate);
              int tday_of_month = calendar.get(Calendar.DAY_OF_MONTH);
              
              if(newCompletionDate.before(dtTodaysDate) && day_of_month != tday_of_month)
              {
                  String language = context.getSession().getLanguage();
                  String strInvalidEstimateDate = EnoviaResourceBundle.getProperty(context,"ProductLine",
                		  "emxProduct.TestCase.Alert.InvalidEstimatedDate",language);
                
                  throw new Exception(strInvalidEstimateDate);
              }
              else
              {
            	  DomainObject dom = new DomainObject(strObjectId);
            	  dom.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_ESTIMATED_COMPLETION_DATE , strNewValue);
              }  
          }
         
    	}catch(Exception ex)
    	{
    		throw new FrameworkException(ex.getMessage());
    	}
    }
    
    
    // ++ HAT1 ZUD: IR-390003-3DEXPERIENCER2017x fix
    /**
     * Method is used to validate the Estimated Completion Date field
     * 
     * @param context the eMatrix <code>Context</code> object 
     * @param args hold the hashMap containing the following argument
     * @returns void 
     * @throws Exception if operation fails.
     * @since R212
     */
    public void validateCreateFormEstimateDate(Context context, String[] args) throws Exception
    {
    	try{
    	  HashMap hashMap = (HashMap)JPO.unpackArgs(args);
          HashMap paramMap = (HashMap) hashMap.get("paramMap");
          String strObjectId = (String) paramMap.get("objectId");
          String strNewValue = (String) paramMap.get("New Value");
          //JX5 IR-231962V6R2014x
          DateFormat df = new SimpleDateFormat();
          Calendar calendar =  Calendar.getInstance();
          
          java.util.Date strEstimatedCompletionDate =null;

          strEstimatedCompletionDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strNewValue);

    	  calendar.setTime(strEstimatedCompletionDate);
          Date newCompletionDate = calendar.getTime();
          int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
          
          Date dtTodaysDate = df.parse(df.format(new Date()));
          calendar.setTime(dtTodaysDate);
          int tday_of_month = calendar.get(Calendar.DAY_OF_MONTH);
          
          if(newCompletionDate.before(dtTodaysDate) && day_of_month != tday_of_month)
          {
              String language = context.getSession().getLanguage();
              String strInvalidEstimateDate = EnoviaResourceBundle.getProperty(context,"ProductLine",
            		  "emxProduct.TestCase.Alert.InvalidEstimatedDate",language);
            
              throw new Exception(strInvalidEstimateDate);
          }
          else
          {
        	  DomainObject dom = new DomainObject(strObjectId);
        	  dom.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_ESTIMATED_COMPLETION_DATE , strNewValue);
          }  
         
    	}catch(Exception ex)
    	{
    		throw new FrameworkException(ex.getMessage());
    	}
    }
    // -- HAT1 ZUD: IR-390003-3DEXPERIENCER2017x fix

    //getAssociatedParameters() added for Parameter under Test Case HL: 
    @com.matrixone.apps.framework.ui.ProgramCallable
 	  public MapList getAssociatedParameters(Context context, String args[]) throws Exception
    {
 		  try
      {
 
 			HashMap programMap = (HashMap) JPO.unpackArgs(args);
 			String objectId = (String)programMap.get("objectId");
 			
 			String toTypeName = PropertyUtil.getSchemaProperty(context, "type_PlmParameter");
			  String relationships = PropertyUtil.getSchemaProperty(context, "relationship_ParameterAggregation");
            
           DomainObject dom = new DomainObject(objectId);
            int sRecurse = 0;
            
            StringList objSelects = new StringList(1);
            objSelects.addElement(DomainConstants.SELECT_LEVEL);
            objSelects.addElement("id[connection]");
            objSelects.addElement(DomainConstants.SELECT_ID);
            objSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
            
            StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            
            MapList relBusObjPageList = new MapList();
            
			//Modified method getAssociatedParameters for IR-298238-3DEXPERIENCER2015x and IR-298340-3DEXPERIENCER2015x.
            relBusObjPageList = dom.getRelatedObjects(context,
            		relationships,
            		toTypeName,
            		objSelects,
            		relSelects,
                    false,
                    true,
                    (short)0,
                    null,
                    null);

           
            MapList result = new MapList(relBusObjPageList.size());
            for(Object t: relBusObjPageList ) { //IR-486617-3DEXPERIENCER2018x: convert from HashTable to HashMap, to accept null cell values
            	Map<String, String> m = new HashMap<>();
            	m.putAll((Map<String, String>)t);
            	result.add(m);
            }
           return result;
 			
 		}
 		catch (Exception ex){
  			System.out.println("getAssociatedParameters - exception " + ex.getMessage());
   			throw ex;
 		} 
 	}
    
    //JX5 display NLS stats with color code
    @com.matrixone.apps.framework.ui.ProgramCallable
    /**
     * get the column html data for an icon column or styled column
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return a List of string which represent the column html data
     * @throws Exception if the operation fails
     * @since R2016x
     */
    public List getStateColumnHTML(Context context, String[] args) throws Exception {
        String OBJECT_LIST = "objectList";
        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
        
        HashMap paramList = (HashMap) programMap.get("paramList");
        String strExport = (String) paramList.get("exportFormat");
        boolean toExport = false;
        if (strExport != null) 
                toExport = true;
        
        int iNumOfObjects = relBusObjPageList.size();
        String arrObjId[] = new String[iNumOfObjects];
        List columnTags = new Vector(iNumOfObjects);
        int iCount;
        // Getting the bus ids for objects in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            arrObjId[iCount] = (String) ((Map) relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
        }
        
        HashMap columnMap = (HashMap) programMap.get("columnMap");

        String select = (String) (columnMap.containsKey("expression_businessobject")?
                columnMap.get("expression_businessobject"):
                columnMap.get("expression_relationship"));
        StringList selects = new StringList();
        selects.addElement(select);   

        MapList columnData = DomainObject.getInfo(context, arrObjId, selects);

        // Iterating through the list of objects to generate the program HTML
        // output for each object in the table
        StringList policySelect = new StringList();
        policySelect.addElement(DomainObject.SELECT_POLICY);
        MapList policyData  = DomainObject.getInfo(context, arrObjId, policySelect);
       
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            Map cell = (Map)columnData.get(iCount);
            HashMap policyMap = (HashMap)policyData.get(iCount);
            String policy	 = (String)policyMap.get(DomainObject.SELECT_POLICY);
            String strValue = (String)cell.get(select);
            String strHTMLTag = "";
            if (strValue != null && strValue.length() > 0) {

                policy = policy.replaceAll(" ", "_");
                strValue = strValue.replaceAll(" ", "_");
                String i18nProperty = "emxFramework.State."+policy+"."+strValue;

                	
                String strDisplayValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",
                            context.getLocale(), i18nProperty);
               if(toExport){
                    strHTMLTag = strDisplayValue;
                }
                else{

                    String propertyKey = "";
                    String Style = "";

                    propertyKey = "emxProduct.Range.State." + strValue.replaceAll(" ", "_");
                    Style = EnoviaResourceBundle.getProperty(context, propertyKey);
                    Style = "font-weight:bold;color:"+Style;

                           
                    strHTMLTag = "<span style=\"" + Style + "\">" + strDisplayValue + "</span>"; 

               }

            }
            columnTags.add(strHTMLTag);
        }
        return columnTags;
    }
    
    public boolean isLastRevision(Context context, String objectId)
            throws FrameworkException
        {
       	 boolean isContextPushed = false;
            try
            {
                ContextUtil.pushContext(context);
                isContextPushed = true;
                DomainObject lastRevision = TestCase.getLastRevision(context, objectId);
                String lastObjectId = lastRevision.getId(context);
                return objectId.equals(lastObjectId) || objectId.equals(lastRevision.getInfo(context, "physicalid"));
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
    
    
    public List getHigherAndActualRevisionIcon(Context context, String[] args) throws Exception
    {
   	 String ICON_TOOLTIP_HIGHER_REVISION_EXISTS = "emxProductLine.Form.Label.HigherRev";
        String RESOURCE_BUNDLE_PRODUCTS_STR = "emxProductLineStringResource";
        String OBJECT_LIST = "objectList";
        String PARAM_LIST = "paramList";

        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
        
        HashMap paramList = (HashMap) programMap.get(PARAM_LIST);
        String exportFormat = (String)paramList.get("exportFormat");
        int iNumOfObjects = relBusObjPageList.size();
        // The List to be returned
        List lstHigherRevExists= new Vector(iNumOfObjects);
        String arrObjId[] = new String[iNumOfObjects];

        int iCount;
        //Getting the bus ids for objects in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            Object obj = relBusObjPageList.get(iCount);
            arrObjId[iCount] = (String)((Map)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
        }

        //Reading the tooltip from property file.
        String strTooltipHigherRevExists = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_PRODUCTS_STR, context.getLocale(), ICON_TOOLTIP_HIGHER_REVISION_EXISTS);
            String strHigherRevisionIconTag= "";
            String strIcon = EnoviaResourceBundle.getProperty(context,
                            "emxComponents.HigherRevisionImage");
        //Iterating through the list of objects to generate the program HTML output for each object in the table
            for (iCount = 0; iCount < iNumOfObjects; iCount++) {
           	 	DomainObject objId = DomainObject.newInstance(context, arrObjId[iCount]);
           	 	String Revision = objId.getRevision(context);
                    if(!isLastRevision(context, arrObjId[iCount]) && exportFormat==null){
                    strHigherRevisionIconTag =
                   		 	 Revision+"&#160;"
                   			 +"<img src=\"../common/images/"
                                + strIcon
                                + "\" border=\"0\"  align=\"baseline\" "
                                + "TITLE=\""
                                + " "
                                + strTooltipHigherRevExists
                                + "\""
                                + "/>";
                    }else{
                    strHigherRevisionIconTag = Revision;
                    }
                lstHigherRevExists.add(strHigherRevisionIconTag);
            }
        return lstHigherRevExists;
    }
    
    // ++ HAT1 ZUD: HL -  To enable Content column for Test Cases.
    
    /**
     * Returns a Content Data list of all the test case in TC listing page.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - None
     * @return the link
     * @throws Exception
     */
    public List getContentDataForTable(Context context, String[] args) throws Exception {

        Map programMap = (HashMap) JPO.unpackArgs(args);
        String OBJECT_LIST = "objectList";
        String PARAM_LIST  = "paramList";
        String contentType = "";
        String contentData = "";
        String contentText = "";
        
        String strContentData = "";
        String relId       = "";
        String lud         = "";
        
        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
        Map paramMap = (Map) programMap.get(PARAM_LIST);
        relId        = (String) paramMap.get("relId");
        
        int iNumOfObjects = relBusObjPageList.size();
        // The List to be returned
        String arrObjId[] = new String[iNumOfObjects];
        List testCasesContentDataList = new Vector(iNumOfObjects);

        int iCount;
        //Getting the bus ids for objects in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            Object obj = relBusObjPageList.get(iCount);
            arrObjId[iCount] = (String)((Map)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
        }
        
      //Iterating through the list of objects to generate the program HTML output for each object in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) 
        {
        	if(arrObjId[iCount] != null) 
        	{
                String modifed = DomainObject.newInstance(context, arrObjId[iCount]).getInfo(context, DomainConstants.SELECT_MODIFIED);
                lud = eMatrixDateFormat.getJavaDate(modifed).getTime() + "";
            }
        	
       	 	DomainObject dmoObject = DomainObject.newInstance(context, arrObjId[iCount]);
            if(arrObjId[iCount] != null) 
            {            
                contentType    = dmoObject.getAttributeValue(context, "Content Type");
                contentText    = dmoObject.getAttributeValue(context, "Content Text");
                contentData = dmoObject.getAttributeValue(context, "Content Data");

                
                if(contentData.contains("rcowidget"))
                {
    		         
    				 String imageTag = "<img alt='RCO image' src='../productline/scripts/ckeditor/plugins/rcowidget/images/defaultIcon.png' />";
                	 contentData = imageTag + " " + contentText;
                }
                
                //contentData    = ConvertedDataDecorator.putRichTextEditorDivForRTF(context, arrObjId[iCount], strContentData, "None");
            }
            //Unnecessary HTML code is removed. 
            testCasesContentDataList.add(contentData.replaceAll("&nbsp;", "&#160;"));
        }
        
        return testCasesContentDataList;
    }
    // -- HAT1 ZUD: HL -  To enable Content column for Test Cases
    public static HashMap commandReserveTree(Context context, String[] args)
		      throws Exception
		   {
		      // unpack the incoming arguments into a HashMap called 'programMap'
		      HashMap programMap = (HashMap) JPO.unpackArgs(args);
//		      System.out.println("Inside ReserveTree: programMap = \n  " + programMap);

		      // get the 'requestMap' HashMap from the programMap
		      HashMap requestMap = (HashMap) programMap.get("requestMap");
		      Object rparam = requestMap.get("rowIds");
		      String[] rowIds = new String[1];
		      if (rparam instanceof String[])
		         rowIds = (String[]) rparam;
		      else
		         rowIds[0] = (String) rparam;

		      // The emxForm.jsp UpdateProgram passes all rowIds in a comma-separated list
		      if (rowIds != null && rowIds.length == 1 && rowIds[0].indexOf(",") > 0)
		         rowIds = rowIds[0].split(",");

		      // ZUD Fix for Reserve command for attributes
		      if(rowIds[0].compareTo("") == 0)
		      {
		    	  rowIds[0] = (String)requestMap.get("objectId");
		      }
		      // Get the comments from the Reserve form paramMap
		      Object cparam = requestMap.get("comments");
		      String[] comments = new String[1];
		      if (cparam instanceof String[])
		         comments = (String[]) cparam;
		      else
		         comments[0] = (String) cparam;

		      String comment = (comments == null || comments.length == 0? null: comments[0]);
		      return modifyTreeReserveOrUnreserve(context, rowIds, comment, true);
		   }
	
	/**
	    *  Reserve or Unreserve a Requirement Specification structure.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param rowIds Array of Requirement Specification tableRowIds.
	    * @param comment reserve comment
	    * @param flag true for Reserve, false for Unreserve
	    * @param includeSub true to include Sub Requirements in the operation
	    * @param includeDerived true to include Derived Requirements in the operation
	    * @param includeParam true to include Parameters in the operation
	    * @return HashMap a HashMap contains Action of "continue" or "ERROR".
	    * @throws Exception if the operation fails
	    */
	   private static HashMap modifyTreeReserveOrUnreserve(Context context, String[] rowIds, String comment, boolean flag)
	      throws FrameworkException
	   {
	      HashMap status = new HashMap();
	      status.put("Action", "continue");
	      int success = 0;
	      if (rowIds != null)
	      {
	         for (int ii = 0; ii < rowIds.length; ii++)
	         {
	            String objId = rowIds[ii];

	            // The list of rowIds from the indentedTable.jsp is of the form: relId|objId|parId|x,y
	            if (objId.indexOf("|") >= 0)
	            {
	               // Extract the objectId and parentId from the emxTableRowId:
	               String[] tokens = rowIds[ii].split("[|]");
	               objId = tokens[1];
	            }

	            try
	            {
	               // Begin a transaction frame, in case part of the tree cannot be removed.
	               ContextUtil.startTransaction(context, true);

	               // Set the Reserve flag on the selected object and any unreserved children...
	               DomainObject selObject = DomainObject.newInstance(context, objId);
	               modifyTreeSetReserveFlag(context, selObject, comment, flag);

	               // Commit the transaction, since there were no problems.
	               ContextUtil.commitTransaction(context);
	               success++;
	            }
	            catch (MatrixException mex)
	            {
	               // Rollback the whole transaction:
	               ContextUtil.abortTransaction(context);

	               // Send the status message back to the UI...
	               String mess = mex.getMessage();
	               status.put("Action", "ERROR");
	               status.put("Message", mess == null? mex.toString(): mess);
	            }
	            catch (Exception ex)
	            {
	               // Rollback the whole transaction:
	               ContextUtil.abortTransaction(context);
	               throw new FrameworkException(ex.getMessage());
	            }
	         }
	         if(success > 0)
	         {
	        	 status.put("Refresh", "true");
	         }
	      }

	      return(status);
	   }
	   
	   public static HashMap commandUnreserveExtendedTree(Context context, String[] args)
			   throws Exception
			{
					HashMap programMap = (HashMap) JPO.unpackArgs(args);
					HashMap requestMap = (HashMap) programMap.get("requestMap");
					 
					Object rparam = requestMap.get("rowIds");
					String[] rowIds = new String[1];
					if (rparam instanceof String[])
						rowIds = (String[]) rparam;
					else
						rowIds[0] = (String) rparam;

					// The emxForm.jsp UpdateProgram passes all rowIds in a comma-separated list
					if (rowIds != null && rowIds.length == 1 && rowIds[0].indexOf(",") > 0)
						rowIds = rowIds[0].split(",");
					
					// ZUD Fix for Reserve command for attributes
				      if(rowIds[0].compareTo("") == 0)
				      {
				    	  rowIds[0] = (String)requestMap.get("objectId");
				      }
				      
					return modifyTreeReserveOrUnreserve(context, rowIds, null, false);
			}
	   /**
	    * @param context
	    * @param treeObject
	    * @param comment
	    * @param resFlag
	    * @throws FrameworkException
	    * @throws MatrixException
	    */
	   public static final String RESOURCE_BUNDLE_PLC_STR = "emxProductLineStringResource";
	   public static final String SELECT_RESERVED = "reserved";
	   public static final String SELECT_RESERVED_BY = "reservedby";
	   private static void modifyTreeSetReserveFlag(Context context, DomainObject treeObject, String comment, boolean resFlag)
	      throws FrameworkException, MatrixException, Exception
	   {
	      String objId = treeObject.getInfo(context, SELECT_ID);
	      String objType = treeObject.getInfo(context, SELECT_TYPE);
	      String resAtt = treeObject.getInfo(context, SELECT_RESERVED);
	      String resBy = treeObject.getInfo(context, SELECT_RESERVED_BY);
	      String resStr = ("" + resFlag).toUpperCase();
	      
	      String project = treeObject.getInfo(context, "project");
	      
	      String sc = context.getRole(); //context.getSession().getRole();
	      
	      String role = "";
	      String contextProject = "";
	      if(sc != null && sc.startsWith("ctx::")) {
	    	  sc = sc.substring(5);
	    	  String[] roles = sc.split("[\\.]");
	    	  if(roles.length == 3 ) {
	    		  role = roles[0];
	    		  contextProject = roles[2];
	    	  }
	      }
	      
	      boolean isAdmin = "VPLMAdmin".equals(role);
	      boolean isProjAdmin =  "VPLMProjectAdministrator".equals(role);
	      

	      // Stop right now if the selected object is reserved by someone else...
	      if (resAtt.equals("TRUE") && !resBy.equals(context.getUser()) && !(isProjAdmin && contextProject.equals(project)) && !isAdmin)
	      {
	         String[] errorArgs = new String[] {resBy};
	         String errorMsg = MessageUtil.getMessage(context, null, "emxProductLine.Alert.ObjectReservedBy",
	               errorArgs, null, context.getLocale(), RESOURCE_BUNDLE_PLC_STR);

	         System.err.println("Object is reserved by: " + resBy);
	         throw(new MatrixException(errorMsg));
	      }
	      // BUG: #368981: Stop if Unreserve is called on an object that is NOT reserved.
	      else if (resAtt.equals("FALSE") && !resFlag)
	      {
	         //IR-035064V6R2011: do not throw error message when unreserving a non-reserved object
	      }

	      // Unreserve the selected object only if it is reserved...
	      if (!resFlag && "TRUE".equals(resAtt))
	         treeObject.unreserve(context);
	      // Reserve (or re-Reserve) the object with the new comment...
	      if(resFlag)
	         treeObject.reserve(context, comment);	      
	 }
	   
	   public static String getLockIcon(Context context, String[] args) throws Exception
	    {
	    	Map programMap = (HashMap) JPO.unpackArgs(args);
		    Map paramMap = (HashMap)programMap.get("paramMap");
		    String strObjectId = (String)paramMap.get("objectId");
		    StringList selectStmts = new StringList("reserved");
		    selectStmts.addElement("reservedby");
		    selectStmts.addElement("reservedcomment");
		    selectStmts.addElement("reservedstart");
		    DomainObject domObj = DomainObject.newInstance(context, strObjectId);
		    Map ReservedInfo = domObj.getInfo(context,selectStmts);
		    String reserved = (String)ReservedInfo.get("reserved");
		    String strImage="";
		    String strDifficultyIconTag = "";
		    String User = context.getUser();
		    if(reserved.equalsIgnoreCase("true"))
		    {
		    	String reservedby = (String)ReservedInfo.get("reservedby");
		    	if(User.equalsIgnoreCase(reservedby))
		        {
		    		strImage= EnoviaResourceBundle.getProperty(context,"emxProductLine.Icon.padLockReservedByMySelf");
		        }
	        	else
	        	{
	        		strImage= EnoviaResourceBundle.getProperty(context,"emxProductLine.Icon.padLockReservedByOther");
	        	}    	
		    	String strLockedBy = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_PLC_STR, context.getLocale(), "emxFramework.Basic.ReservedBy");
		    	String toolTip = strLockedBy + " " +reservedby+ " \n";
		    	strDifficultyIconTag = 
		        	"<img src=\"" + strImage + "\""
		            + " border=\"0\"  align=\"middle\" "
		            + "title=\""
		            + " "
		            + toolTip
		            + "\""
		            + "/>";
		    }
		    else
		    { 

		    	strDifficultyIconTag = "";
		    }
	        return strDifficultyIconTag;
	    }
	    
	     
    
    //++ HAT ZUD: IR-439329-3DEXPERIENCER2017x: R419-STP:Test Case is getting promoted to Release state when sub testcase inside it is still in "In Work" state.
    /** Util Function for Trigger Methods to check if the child Requirements are in Released(Complete)/Obsolete state
     *  before promoting the Parent requirement to Released(Complete)/Obsolete state.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @param relationship - holds the the name of the relationship.
     * @return - integer value 0 if success
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public int checkChildTestCaseComplete(Context context, String[] args)
        throws Exception
    {
        int iFlag = 0;
        String strObjectId = args[0];

        try
        {   
            String relationship = ProductLineConstants.RELATIONSHIP_SUB_TEST_CASE;
            String strRelPattern  = "";
            String strTypePattern = "";
            String STATE_COMPLETE = "";
            String STATE_OBSOLETE = ""; 
            strRelPattern = relationship;
            	
            strTypePattern = ProductLineConstants.TYPE_TEST_CASE;
        	STATE_COMPLETE = FrameworkUtil.lookupStateName(context, "Test Case", SYMBOLIC_STATE_COMPLETE);
        	STATE_OBSOLETE = FrameworkUtil.lookupStateName(context, "Test Case", SYMBOLIC_STATE_OBSOLETE);
        	if(null == STATE_COMPLETE)
            {
        		STATE_COMPLETE = "Complete";
            }
        	if(null == STATE_OBSOLETE)
            {
        		STATE_OBSOLETE = "Obsolete";
            }

            List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
            List lstRelSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            boolean bGetTo = false;
            boolean bGetFrom = true;
            short sRecursionLevel = 1;
            String strBusWhereClause = "";
            String strRelWhereClause = "";
            //mql expand bus 34757.3190.27704.19254 relationship "Sub Requirement" select bus current where "! current matchlist 'Release,Obsolete' ','";
            StringBuffer sbWhereBus = new StringBuffer();
            //sbWhereBus.append("policy != 'ParameterAggregation' and ");
            sbWhereBus.append(SYMB_NOT).append(DomainConstants.SELECT_CURRENT);
            sbWhereBus.append(SYMB_SPACE);
            sbWhereBus.append(SYMB_MATCHLIST).append(SYMB_SPACE);
            sbWhereBus.append(SYMB_QUOTE).append(STATE_COMPLETE).append(SYMB_COMMA).append(STATE_OBSOLETE).append(SYMB_QUOTE).append(SYMB_SPACE);
            sbWhereBus.append(SYMB_QUOTE).append(SYMB_COMMA).append(SYMB_QUOTE);
            strBusWhereClause = sbWhereBus.toString();

            DomainObject domReq = DomainObject.newInstance(context,strObjectId);
            @SuppressWarnings("deprecation")
            
			MapList mapSubRequirements = domReq.getRelatedObjects(context, strRelPattern, strTypePattern,
                    (StringList)lstObjectSelects, (StringList)lstRelSelects, bGetTo, bGetFrom,
                    sRecursionLevel, strBusWhereClause, strRelWhereClause);

            if (mapSubRequirements.isEmpty())
                iFlag = 0;
            else
			{
                iFlag = 1;
                String strAlertMessage = EnoviaResourceBundle.getProperty(context, "emxProductLineStringResource", context.getLocale(),"emxProductLine.Alert.NonCompletedChildTestCase"); 
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
    //-- HAT ZUD: IR-439329-3DEXPERIENCER2017x: R419-STP:Test Case is getting promoted to Release state when sub testcase inside it is still in "In Work" state.
}


