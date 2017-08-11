/*
 ** emxBooleanCompatibilityBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.17.2.4.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 */



import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.BooleanCompatibilityRule;
import com.matrixone.apps.configuration.ConfigurableRulesUtil;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.RuleProcess;
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
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;

/**
 * This JPO class has some method pertaining to Boolean Compatibility Rule type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxBooleanCompatibilityBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     * Alias used for AND operator.
     */
    protected static final String AND = "AND";
    
    /**
     * Alias used for OR operator.
     */
    protected static final String OR = "OR";
    
    /**
     * Alias used for NOT operator.
     */
    protected static final String NOT = "NOT";
    
    /**
     * Alias used for OPEN ROUND BRACE.
     */
    protected static final String OPEN_ROUND_BRACE = "(";
    
    /**
     * Alias used for CLOSE ROUND BRACE.
     */
    protected static final String CLOSE_ROUND_BRACE = ")";
    
    /**
    * Alias used for comparision operator.
    */
    protected static final String EQUALS = " == ";
    /**
    * Alias used for Blank Space.
    */
    protected static final String SPACE = " ";
    /**
    * Alias used for Double Quote.
    */
    protected static final String DOUBLE_QUOTE = "\"";
    /**
    * Alias used for And operator.
    */
    protected static final String AMPERSAND = " && ";
    /**
    * Alias used for single quote operator.
    */
    protected static final String SINGLE_QUOTE = "'";
    /**
    * Alias used for comma operator.
    */
    protected static final String COMMA = ",";
    /**
    * Alias used for open brace.
    */
    protected static final String OPEN_BRACE = "[";
    /**
    * Alias used for cloase brace.
    */
    protected static final String CLOSE_BRACE = "]";
    /**
    * Alias used for dot.
    */
    protected static final String DOT = ".";
    /**
    * Alias used for to.
    */
    protected static final String TO = "to";
    /**
    * Alias used for from.
    */
    protected static final String FROM = "from";
    /**
    * Alias used for false.
    */
    protected static final String FALSE = "False";
    /**
    * Alias used for Feature TNR Delimiter.
    */
    private final static String FEATURE_TNR_DELIMITER = "::";
    /**
    * Alias used for Feature Option seperator.
    */
    private static final char C_FOPT_SEPERATOR = '~';

    private final static String STR_ATTRIBUTE = "attribute";

    private final static String SUITE_KEY = "Configuration";
    private final static String STR_INDEX = "index";

    private static String strFailedTNR = DomainConstants.EMPTY_STRING;

    private final static String STR_NEWLINE = "*";
    
    // used for Rule Display User Settings based on which the Rule Expression will be displayed
    private final static String RULE_DISPLAY_FULL_NAME = ConfigurationConstants.RULE_DISPLAY_FULL_NAME;

    private final static String RULE_DISPLAY_MARKETING_NAME = ConfigurationConstants.RULE_DISPLAY_MARKETING_NAME;


    /**
     * Default Constructor.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    emxBooleanCompatibilityBase_mxJPO(Context context, String[] args) throws Exception
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
        public int mxMain(Context context, String[] args) throws Exception
        {

            if (!context.isConnected())         {
                String sContentLabel =
                    EnoviaResourceBundle.getProperty(context,
                        SUITE_KEY,
                        "emxProduct.Error.UnsupportedClient",
                        context.getSession().getLanguage());
                throw new Exception(sContentLabel);
            }
            return 0;
        }

        /**
         * Method to reframe the Right Expression for List Page (Marketing Name).
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - HashMap containing one String entry for key "objectId"
                  1 - String containing the typeName which is used to find out whether the rule is a BCR.
         * @return List containing the Right Expressions for all the Rules loaded in the
         *         table (Full Name\Marketing Name is displayed based on user preference).
         * @throws Exception if the operation fails
         * @since ProductCentral 10-6-SP1
         * @grade 0
         * this could not be deprecated as referenced from migration JPO
         */

    public List getRightExpressionforListPage(Context context, String[] args) throws Exception {
        //XSSOK_ DEPRECATED
        //String strRuleDisplay = PersonUtil.getRuleDisplay(context);
        return getExpressionForRule(context, args,ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);

        //return getRightExpression(context, args, strRuleDisplay);

    }

    
    /**
     * Method to reframe the Right Expression for Edit Page (FullName).
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one String entry for key "objectId"
              1 - String containing the typeName which is used to find out whether the rule is a BCR.
     * @return List containing the Right Expressions for all the Rules loaded in the
     *         table (format is Full Name with T::N::R).
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6-SP1
     * @grade 0
     * this could not be deprecated as referenced from migration JPO
     */

    public List getRightExpressionforQuantityEdit(Context context, String[] args) throws Exception {
        return getRightExpression(context, args, RULE_DISPLAY_FULL_NAME);
    }

        
    
    /**
         * Method to reframe the Right Expression for the Rule object.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - HashMap containing one String entry for key "objectId"
                  1 - String containing the typeName which is used to find out whether the rule is a BCR.
                  2 - String containing the user preference for Rule Display (Full Name/ Marketing Name)
         * @return List containing the Right Expressions for all the Rules loaded in the table.
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         * @grade 0
         * this could not be deprecated as referenced from migration JPO
         */

    public List getRightExpression(Context context, String[] args,String strRuleDisplay) throws Exception {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strIsIntermediate= (String)((Map) programMap.get("paramList")).get("intermediate");
        String strRuleType = (String)programMap.get("ruleType");
            List lstRightExpression = new StringList();
            if (strIsIntermediate !=null && !("".equals(strIsIntermediate))
                && !("null".equals(strIsIntermediate)) && strIsIntermediate.equals("true"))
            {
            if(!"null".equals(strRuleType) && !"".equals(strRuleType) && strRuleType != null && strRuleType.equalsIgnoreCase(ConfigurationConstants.TYPE_QUANTITY_RULE) )
            {
                programMap.put("relType", ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);
                programMap.put("ruleDisplay", strRuleDisplay);
                
                String args1[] = JPO.packArgs(programMap);
                
                lstRightExpression = getExpression(
                        context,
                        args1,
                        strRuleType
                        );
            }else
            {
                lstRightExpression = getExpression(
                                        context,
                                        args,
                                        ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION,
                                        strRuleDisplay);
            }
            }
            else
            {
                lstRightExpression = getProductRuleExpression(
                                        context,
                                        args,
                                        ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION,
                                        strRuleDisplay);
            }
            return lstRightExpression;

        }

        
        


    // this method is used to get the Left Expression for the Configurable rules based on the Attribute
/**
 * this could not be deprecated as referenced from migration JPO
 */
    public List getExpressionForRule(Context context, String[] args, String strLeftRightExpr) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        // get if the Rule Display Preference is set to Marketing Name or Object Name.
        String strRuleDisplayTemp = PersonUtil.getRuleDisplay(context);
        boolean isRuleDisplayFullname=false;

        if((strRuleDisplayTemp == null) ||(strRuleDisplayTemp.equals("null"))
            ||(strRuleDisplayTemp.equals("")) ||(strRuleDisplayTemp.equals(RULE_DISPLAY_FULL_NAME))){

            isRuleDisplayFullname=true;
        }

        MapList objectList = (MapList)programMap.get("objectList");
        List lstLeftExpression = new StringList();

        StringList strListLeftExpr = new StringList();
        Map mapTokenIds = new HashMap();
        Map mapCGTokenIds = new HashMap();

        for(int i=0; i<objectList.size();i++){
           Map mapLeftExpression = (Map) objectList.get(i);
           String strLeftExpr = (String)mapLeftExpression.get("attribute["+strLeftRightExpr+"]");
           strListLeftExpr.add(strLeftExpr);

           StringList stElmnt = FrameworkUtil.split(strLeftExpr, " ");
           String element;
            for (int m = 0;m<stElmnt.size();m++) {
              element =((String)stElmnt.get(m)).trim();
              if(!(element.equals(AND)||element.equals(OR)||element.equals(NOT)||element.equals(OPEN_ROUND_BRACE)||element.equals(CLOSE_ROUND_BRACE))){
                if(element.length()>1){
                String strObjId = element.substring(1);
              //separating objectid from Relids                
        	if(element.startsWith("R")){                	    
        	    
        	    mapCGTokenIds.put(strObjId,strObjId);
        	}else{
        	    mapTokenIds.put(strObjId,strObjId);
        	}
                }
              }
            }
        }

        // get the selectables for forming expression
        StringList lstObjectSelects=new StringList(DomainConstants.SELECT_ID);
        lstObjectSelects.add(DomainConstants.SELECT_NAME);
        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
        lstObjectSelects.add(DomainConstants.SELECT_REVISION);
        lstObjectSelects.add("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]");
        lstObjectSelects.add("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_OBJECT_NAME+"]");
        lstObjectSelects.add("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_OBJECT_NAME+"]");
        lstObjectSelects.add("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_MARKETING_NAME+"]");
        lstObjectSelects.add("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_MARKETING_NAME+"]");
        lstObjectSelects.add("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type");
        lstObjectSelects.add("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.revision");
        lstObjectSelects.add("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.type");
        lstObjectSelects.add("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.revision");

        MapList expMapList = new MapList();

        //Get the unique keys to make a DB call for geting the details to form the Expression
        List objList = Arrays.asList(mapTokenIds.keySet().toArray());
        String[] oidsArray = new String[objList.size()];
        MapList finalMap = new MapList();
        Map map;


        try {
            // make a DB call for all the Object Ids at once with the selectables
            if (mapTokenIds.size() > 0) {
            	//	IR-038061V6R2011 , to[flf].from.type was coming null in case of System Engineer;
            	ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                expMapList = DomainObject.getInfo(context,(String[]) objList.toArray(oidsArray), lstObjectSelects);
                ContextUtil.popContext(context);
            }

            StringBuffer strExpressionbuffer;
            for (int i = 0; i < expMapList.size(); i++) {


                Map mapExpression = (Map) expMapList.get(i);
                String strObjId  = (String)mapExpression.get(DomainConstants.SELECT_ID);
                String strObjType  = (String)mapExpression.get(DomainConstants.SELECT_TYPE);
                strExpressionbuffer = new StringBuffer("");
                map = new HashMap();

                // Form the expression based on the Rule Display preference. and add to the MapList
                //If the type is Feature List prepare expression as follows
                if(strObjType.equalsIgnoreCase(ConfigurationConstants.TYPE_FEATURE_LIST)){

                    if(isRuleDisplayFullname){
                        strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                        .append((String)mapExpression.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type"))
                                .append(FEATURE_TNR_DELIMITER)
                        .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_OBJECT_NAME+"]"))
                                .append(FEATURE_TNR_DELIMITER)
                        .append((String)mapExpression.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.revision"))
                                .append(C_FOPT_SEPERATOR)
                        .append((String)mapExpression.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.type"))
                        .append(FEATURE_TNR_DELIMITER)
                        .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_OBJECT_NAME+"]"))
                        .append(FEATURE_TNR_DELIMITER)
                        .append((String)mapExpression.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.revision"))
                        .append(DOUBLE_QUOTE);
                        map.put("ObjId",strObjId);
                        map.put("Expression",strExpressionbuffer.toString());
                        finalMap.add(map);

	// Start - Changes  for  IR Mx376196
                    }else if(strRuleDisplayTemp.equals(RULE_DISPLAY_MARKETING_NAME)){
                        strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                         .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_MARKETING_NAME+"]"))
                                 .append(C_FOPT_SEPERATOR)
                         .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_MARKETING_NAME+"]"))
                         .append(DOUBLE_QUOTE);
                        map.put("ObjId",strObjId);
                        map.put("Expression",strExpressionbuffer.toString());
                        finalMap.add(map);
                       }
                    else {
                    	strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                        .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_MARKETING_NAME+"]"))                        
                        .append(" ")
                        .append((String)mapExpression.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.revision"))
                        .append(C_FOPT_SEPERATOR)
                        .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_MARKETING_NAME+"]"))
                        .append(" ")
                        .append((String)mapExpression.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.revision"))
// End Changes  for  IR Mx376196
                        .append(DOUBLE_QUOTE);
                       map.put("ObjId",strObjId);
                       map.put("Expression",strExpressionbuffer.toString());
                       finalMap.add(map);
                    }
                }
                else if(mxType.isOfParentType(context,strObjType,ConfigurationConstants.TYPE_FEATURES))
                {
                  if(isRuleDisplayFullname)
                  {
                        strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                        .append((String)mapExpression.get(DomainConstants.SELECT_TYPE))
                        .append(FEATURE_TNR_DELIMITER)
                        .append((String)mapExpression.get(DomainConstants.SELECT_NAME))
                        .append(FEATURE_TNR_DELIMITER)
                        .append((String)mapExpression.get(DomainConstants.SELECT_REVISION))
                        .append(DOUBLE_QUOTE);
                        map.put("ObjId",strObjId);
                        map.put("Expression",strExpressionbuffer.toString());
                        finalMap.add(map);
	// Start - Changes  for  IR Mx376196
                  }else if(strRuleDisplayTemp.equals(RULE_DISPLAY_MARKETING_NAME)){
                      strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                        .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]"))
                        .append(DOUBLE_QUOTE);
                      map.put("ObjId",strObjId);
                      map.put("Expression",strExpressionbuffer.toString());
                      finalMap.add(map);
                  }
                  else {
                      strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                      .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]"))
                      .append(" ")
                      .append((String)mapExpression.get(DomainConstants.SELECT_REVISION))
	// End - Changes  for  IR Mx376196
                      .append(DOUBLE_QUOTE);
                    map.put("ObjId",strObjId);
                    map.put("Expression",strExpressionbuffer.toString());
                    finalMap.add(map);
                  }
                }else if(mxType.isOfParentType(context,strObjType,ConfigurationConstants.TYPE_PRODUCTS))
                {
                  if(isRuleDisplayFullname)
                  {
                        strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                        .append((String)mapExpression.get(DomainConstants.SELECT_TYPE))
                        .append(FEATURE_TNR_DELIMITER)
                        .append((String)mapExpression.get(DomainConstants.SELECT_NAME))
                        .append(FEATURE_TNR_DELIMITER)
                        .append((String)mapExpression.get(DomainConstants.SELECT_REVISION))
                        .append(DOUBLE_QUOTE);
                        map.put("ObjId",strObjId);
                        map.put("Expression",strExpressionbuffer.toString());
                        finalMap.add(map);
                  }
                  //Bug 367399: Appended Marketing Name attribute  Start Changes  for  IR Mx376196
                  else if(strRuleDisplayTemp.equals(RULE_DISPLAY_MARKETING_NAME)){
                      strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                     //  .append((String)mapExpression.get(DomainConstants.SELECT_NAME))
                         .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]"))
                        .append(DOUBLE_QUOTE);
                      map.put("ObjId",strObjId);
                      map.put("Expression",strExpressionbuffer.toString());
                      finalMap.add(map);
                  }
                  else{
                	  strExpressionbuffer = strExpressionbuffer.append(DOUBLE_QUOTE)
                          .append((String)mapExpression.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]"))
                          .append(" ")
                          .append((String)mapExpression.get(DomainConstants.SELECT_REVISION))
	// End Chnages  for  IR Mx376196
                         .append(DOUBLE_QUOTE);
                       map.put("ObjId",strObjId);
                       map.put("Expression",strExpressionbuffer.toString());
                       finalMap.add(map);
                  }
                }
        }
          //taking care of Common Group 
            if (mapCGTokenIds.size() > 0) {
        		List objListCG = Arrays.asList(mapCGTokenIds.keySet().toArray());
            
                for (int i = 0; i < objListCG.size(); i++) {            	
                    map = new HashMap();
                    String strObjId  = (String) objListCG.get(i);
                    strExpressionbuffer = new StringBuffer(20);
                     strExpressionbuffer.append(DOUBLE_QUOTE)
                     .append("CommonGroup")              
                     .append(FEATURE_TNR_DELIMITER)                
                     .append(strObjId)
                     .append(DOUBLE_QUOTE);
                    map.put("ObjId",strObjId);
                    map.put("Expression",strExpressionbuffer);
                    finalMap.add(map);
                }
            }// end Common group processibg

        String strTempToken;
        // Loop the Left Expression attribute which to form the final Expression to be returned.
        for(int k=0;k<strListLeftExpr.size();k++) {
            String token = ((String)strListLeftExpr.get(k)).trim();

            StringList tempList = FrameworkUtil.splitString(token," ");
            strTempToken = " ";
            StringBuffer strLeftExpression = new StringBuffer("");

            for(int i=0;i<tempList.size();i++) {
                strTempToken = ((String)tempList.get(i)).trim();


            if(strTempToken.length()>0 && !(strTempToken.equals(AND)
                    ||strTempToken.equals(OR)||strTempToken.equals(NOT)||strTempToken.equals(OPEN_ROUND_BRACE)||strTempToken.equals(CLOSE_ROUND_BRACE))){

                strTempToken=strTempToken.substring(1);
                for(int x=0;x<finalMap.size();x++){
                    HashMap hmInnerMap = (HashMap)finalMap.get(x);
                    String strOID = (String) hmInnerMap.get("ObjId");
                    if(strTempToken.equalsIgnoreCase(strOID)){
                        strLeftExpression.append(hmInnerMap.get("Expression").toString());
                    }
                }
            }
            else{
        	if(strTempToken.length()>0){
            	strLeftExpression.append(SPACE);
                    strLeftExpression.append(strTempToken);
                    if(i < tempList.size()-1)
                    {
                        strLeftExpression.append(SPACE);
                    }
                   }
            }
            }
            lstLeftExpression.add(strLeftExpression.toString());
        }
        }catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        return lstLeftExpression;
    }




        /**
         * Method to reframe the Expression for the Product Compatibilty Rule object.
         * This method is called by the methods getLeftExpression and getRightExpression
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - HashMap containing one String entry for key "objectId"
         * @param strRelType contains the relationship on which getRelated has to be fired (Left/Right Expression)
         * @param strRuleDisplay contains the user preference for Rule Display (Full Name/ Marketing Name)
         * @return List containing the Expressions for all the Rules loaded in the table.
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         * this could not be deprecated as referenced from migration JPO
         */

        public List getProductRuleExpression(Context context, String[] args,String strRelType,String strRuleDisplay) throws Exception {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            List lstObjectIdsList = (MapList) programMap.get("objectList");
            List lstLeftExpressionList = new StringList();

            //Get the number of objects in objectList
            int iNumOfObjects = lstObjectIdsList.size();
            String arrObjId[] = new String[iNumOfObjects];

            //Getting the bus ids for objects in the table
            for (int i = 0; i < iNumOfObjects; i++) {
                Object obj = lstObjectIdsList.get(i);
                if (obj instanceof HashMap) {
                    arrObjId[i] = (String)((HashMap)lstObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
                }
                else if (obj instanceof Hashtable) {
                    arrObjId[i] = (String)((Hashtable)lstObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
                }
            }

            List lstObjectSelects = new StringList();
            StringBuffer strBuffer = new StringBuffer(200);

            if((strRuleDisplay == null) ||
                (strRuleDisplay.equals("null")) ||
                (strRuleDisplay.equals("")) ||
                (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {

                        lstObjectSelects.add(DomainConstants.SELECT_NAME);
                        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
                        lstObjectSelects.add(DomainConstants.SELECT_REVISION);
            }

            else {
                        strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                    .append(OPEN_BRACE)
                                    .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                                    .append(CLOSE_BRACE);
                        lstObjectSelects.add(strBuffer.toString());
            }

            List lstRelationSelects = new StringList();
            strBuffer.delete(0,strBuffer.length());
            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                        .append(OPEN_BRACE)
                        .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                        .append(CLOSE_BRACE);
            lstRelationSelects.add(strBuffer.toString());

            Map mpTNR = null;
            DomainObject dom = null;
            StringBuffer strLeftExpression = new StringBuffer(400);
            List lstProductTNR = new StringList();
            List lstOperator = new StringList();
            try
            {
                // for each Rule connected to the object form the Expression by getting the Token
                // attribute from the relationships Left/Right Expression
                 for (int i = 0; i < iNumOfObjects; i++)
                 {
                        dom = new DomainObject(arrObjId[i]);
                        MapList lstTNR =
                                dom.getRelatedObjects(
                                    context,
                                    strRelType,
                                    DomainConstants.QUERY_WILDCARD,
                                    (StringList)lstObjectSelects,
                                    (StringList)lstRelationSelects,
                                    false,
                                    true,
                                    (short) 1,
                                    "",
                                    "", 0);

                        for(int j=0;j<lstTNR.size();j++)
                        {
                            mpTNR=(Map)lstTNR.get(j);

                            strBuffer.delete(0,strBuffer.length());
                            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                        .append(OPEN_BRACE)
                                        .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                                        .append(CLOSE_BRACE);

                            if(((String)mpTNR.get(strBuffer.toString())).trim().equals(""))
                            {
                                    if((strRuleDisplay == null) ||
                                        (strRuleDisplay.equals("null")) ||
                                        (strRuleDisplay.equals("")) ||
                                        (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {
                                                    strBuffer.delete(0,strBuffer.length());
                                                    strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                                .append((String)mpTNR.get(DomainConstants.SELECT_TYPE))
                                                                .append(FEATURE_TNR_DELIMITER)
                                                                .append((String)mpTNR.get(DomainConstants.SELECT_NAME))
                                                                .append(FEATURE_TNR_DELIMITER)
                                                                .append((String)mpTNR.get(DomainConstants.SELECT_REVISION))
                                                                .append(DOUBLE_QUOTE)
                                                                .append(SPACE);

                                                    lstProductTNR.add(strBuffer.toString());
                                    }
                                    else {
                                            strBuffer.delete(0,strBuffer.length());
                                            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                                        .append(OPEN_BRACE)
                                                        .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                                                        .append(CLOSE_BRACE);

                                            lstProductTNR.add((String)mpTNR.get(strBuffer.toString()));
                                    }
                            }
                            else
                            {
                                    lstOperator.add(" OR ");
                            }
                       }
                       for (int k = 0 ; k < lstProductTNR.size(); k++)
                       {
                            strLeftExpression = strLeftExpression.append(lstProductTNR.get(k));
                            strLeftExpression.append(" OR ");
                       }
                       String strLeftExpressionFinal = strLeftExpression.toString();
                       if (strLeftExpressionFinal.endsWith(" OR ")) {
                    	   int j = strLeftExpressionFinal.lastIndexOf(" OR ");
                    	   strLeftExpressionFinal = strLeftExpressionFinal.substring(0, j);
                       }
                       lstLeftExpressionList.add(strLeftExpressionFinal);
                       strLeftExpression.delete(0,strLeftExpression.length());
                       lstProductTNR.clear();
                       lstOperator.clear();
                }
            }
            catch(Exception e)
            {
               System.out.println(e);
            }
            return lstLeftExpressionList;
       }


        /**
         * Method to reframe the Expression for the Rule object.
         * This method is called by the methods getLeftExpression and getRightExpression
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - HashMap containing one String entry for key "objectId"
                  1 - String containing the typeName which is used to find out whether the rule is a BCR.
                  2 - String containing the user preference for Rule Display (Full Name/ Marketing Name)
         * @param strRelType contains the relationship on which getRelated has to be fired (Left/Right Expression)
         * @return List containing the Expressions for all the Rules loaded in the table.
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         * @grade 0
         * this could not be deprecated as referenced from migration JPO
         */
        public List getExpression(Context context, String[] args, String strRelType, String strRuleDisplay) throws Exception {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            MapList bcrObjectIdsList = (MapList) programMap.get("objectList");
            String strIsIntermediate= (String)((Map) programMap.get("paramList")).get("intermediate");

            //Get the number of objects in objectList
            int iNumOfObjects = bcrObjectIdsList.size();
            String arrObjId[] = new String[iNumOfObjects];

            //Getting the bus ids for objects in the table
            for (int i = 0; i < iNumOfObjects; i++) {
                Object obj = bcrObjectIdsList.get(i);
                if (obj instanceof HashMap) {
                    arrObjId[i] = (String)((HashMap)bcrObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
                }
                else if (obj instanceof Hashtable) {
                    arrObjId[i] = (String)((Hashtable)bcrObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
                }
            }

            // if the type connected to Left Expression/Right Expression relationship is Feature List
            // then retrieve the Feature/Option TNRs. This case will arise in case of inclusion rule and BCR.

            List lstObjectSelects = new StringList();
            StringBuffer strBuffer = new StringBuffer(200);
            String strFLFromName = DomainConstants.EMPTY_STRING;
            String strFLFromType = DomainConstants.EMPTY_STRING;
            String strFLFromRev = DomainConstants.EMPTY_STRING;
            String strFLToName = DomainConstants.EMPTY_STRING;
            String strFLToType = DomainConstants.EMPTY_STRING;
            String strFLToRev = DomainConstants.EMPTY_STRING;
            String strMarketingName = DomainConstants.EMPTY_STRING;

            if(strIsIntermediate!=null && strIsIntermediate.equals("true"))
            {
                lstObjectSelects.add(DomainConstants.SELECT_NAME);
                lstObjectSelects.add(DomainConstants.SELECT_TYPE);
                lstObjectSelects.add(DomainConstants.SELECT_REVISION);
                lstObjectSelects.add(DomainConstants.SELECT_ID);
            }
            // otherwise retrieve the TNR of the object connected to the Left/Right Expression relationship
            // directly. This case will arise incase of Product and Model Compatiblity.

            else
            {
                if((strRuleDisplay == null) ||
                    (strRuleDisplay.equals("null")) ||
                    (strRuleDisplay.equals("")) ||
                    (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {

                            lstObjectSelects.add(DomainConstants.SELECT_NAME);
                            lstObjectSelects.add(DomainConstants.SELECT_TYPE);
                            lstObjectSelects.add(DomainConstants.SELECT_REVISION);
                }
	// Start - Changes  for  IR Mx376196
                else if(strRuleDisplay.equals(RULE_DISPLAY_MARKETING_NAME)) {
                            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                        .append(OPEN_BRACE)
                                        .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                                        .append(CLOSE_BRACE);
                            strMarketingName = strBuffer.toString();
                            lstObjectSelects.add(strMarketingName);
                }else {
                	 strBuffer = strBuffer.append(STR_ATTRIBUTE)
                     .append(OPEN_BRACE)
                     .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                     .append(CLOSE_BRACE);
                	 strMarketingName = strBuffer.toString();
                	 lstObjectSelects.add(strMarketingName);
                	 lstObjectSelects.add(DomainConstants.SELECT_REVISION);
	// End -Changes  for  IR Mx376196
                }
            }

            List lstRelationSelects = new StringList();

            strBuffer.delete(0,strBuffer.length());
            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                        .append(OPEN_BRACE)
                        .append(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER)
                        .append(CLOSE_BRACE);
            lstRelationSelects.add(strBuffer.toString());

            strBuffer.delete(0,strBuffer.length());
            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                        .append(OPEN_BRACE)
                        .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                        .append(CLOSE_BRACE);
            lstRelationSelects.add(strBuffer.toString());

            
            List lstLeftExpressionList = new StringList(iNumOfObjects);

            DomainObject dom = null;

            try{

                
                // for each Rule connected to the object form the Expression by getting the Token and Sequence Order
                // attributes from the relationships Left/Right Expression

                for (int i = 0; i < iNumOfObjects; i++) 
                {
                    StringBuffer strLeftExpression = new StringBuffer(400);
                    
                        dom = new DomainObject(arrObjId[i]);

   
                    // newly added for reading the expression from attribute

                    String strExp = null;
                    if(strRelType.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION))
                    {
                        strExp = dom.getAttributeValue(context, ProductLineConstants.ATTRIBUTE_RIGHT_EXPRESSION);
                    }else{
                        strExp = dom.getAttributeValue(context, ProductLineConstants.ATTRIBUTE_LEFT_EXPRESSION);
                    }
                    StringTokenizer stElmnt = new StringTokenizer(strExp, " ", false);
                    //StringBuffer sb = new StringBuffer();
                    String arrStr[] = new String[stElmnt.countTokens()];
                    int count = 0;
                    while (stElmnt.hasMoreElements()) {
                        
                        arrStr[count] = (String)stElmnt.nextElement();
                        count++;
                    }
                    
                    String strType = null;
                    String strName = null;
                    String strRev = null;
                    String strObjId = null;
                    DomainObject domObj = new DomainObject();
                    DomainRelationship domRel = new DomainRelationship();

                    for(int j=0; j<arrStr.length; j++)
                    {
                        strBuffer.delete(0,strBuffer.length());
                        if(!(arrStr[j].equals(AND)||arrStr[j].equals(OR)||arrStr[j].equals(NOT)||arrStr[j].equals(OPEN_ROUND_BRACE)||arrStr[j].equals(CLOSE_ROUND_BRACE)))
                        {
                            strObjId = arrStr[j].substring(1, arrStr[j].length());

                            if(arrStr[j].indexOf("R") != -1)
                            {
                                domRel = new DomainRelationship(strObjId);
                                strName =  domRel.getAttributeValue(context , ConfigurationConstants.ATTRIBUTE_COMMON_GROUP_NAME); 
                                strType = "CommonGroup";

                                if(strIsIntermediate!=null && strIsIntermediate.equals("true"))
                                {
                                    if((strRuleDisplay == null) || 
                                            (strRuleDisplay.equals("null")) || 
                                            (strRuleDisplay.equals("")) || 
                                            (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) 
                                    {
                                        if(strType.contains("CommonGroup"))
                                        {
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strType)
                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strObjId)
                                            .append(DOUBLE_QUOTE);
                                            if(j < arrStr.length -1)
                                            {
                                                strBuffer.append(SPACE);
                                            }
                                        }
                                        strLeftExpression.append(strBuffer.toString());
                                    }else{
                                        if(strType.contains("CommonGroup"))
                        {

                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strType)
                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strObjId)
                                            .append(DOUBLE_QUOTE);
                                            if(j < arrStr.length -1)
                                            {
                                                strBuffer.append(SPACE);
                                            }
                                        }
                                        strLeftExpression.append(strBuffer.toString()); 
                                    }
                                }
                            }else{

                                // Incase of BCR and Inclusion rules, the Left/Right Expression
                                // relationship is connected to Feature List object.
                                // Hence we need to form the Feature/Option pair
                                // inorder to reframe the expression

                                domObj = new DomainObject(strObjId);
                                
                                strType = domObj.getInfo(context, ConfigurationConstants.SELECT_TYPE);
                                strName = domObj.getInfo(context, ConfigurationConstants.SELECT_NAME);
                                strRev = domObj.getInfo(context, ConfigurationConstants.SELECT_REVISION);
                                
                                if(strIsIntermediate!=null && strIsIntermediate.equals("true"))
                                {
                                if((strRuleDisplay == null) ||
                                    (strRuleDisplay.equals("null")) ||
                                    (strRuleDisplay.equals("")) ||
                                            (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) 
                                    {
                                        if(strType.equalsIgnoreCase(ConfigurationConstants.TYPE_FEATURE_LIST))
                                        {
                                            StringList lstObjectSel = new StringList(4);
                                            lstObjectSel.add(DomainConstants.SELECT_ID);
                                            lstObjectSel.add(DomainConstants.SELECT_TYPE);
                                            lstObjectSel.add(DomainConstants.SELECT_NAME);
                                            lstObjectSel.add(DomainConstants.SELECT_REVISION);
                                            
                                            StringList lstRelationSel = new StringList(1);
                                            lstRelationSel.add(DomainRelationship.SELECT_ID);
                                            lstRelationSel.add(DomainRelationship.SELECT_NAME);
                                            
                                            String strRelationType = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+","+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM ; 
                                            
                                            MapList mapList = (MapList) domObj.getRelatedObjects( context,
                                                    strRelationType,
                                                    DomainConstants.QUERY_WILDCARD,
                                                    (StringList)lstObjectSel,
                                                    (StringList)lstRelationSel,
                                                    true,
                                                    true,
                                                    (short) 1,
                                                    "",
                                            "", 0);
                                            
                                            
                                            Hashtable map = (Hashtable)mapList.get(0);
                                            Hashtable map1 = (Hashtable)mapList.get(1); 
                                            
                                            String strRelName = (String)map.get(DomainRelationship.SELECT_NAME);  
                                            
                                            if(strRelName.equals(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM)){
                                                
                                                strFLFromType = (String)map.get(DomainConstants.SELECT_TYPE);
                                                strFLFromName = (String)map.get(DomainConstants.SELECT_NAME);
                                                strFLFromRev = (String)map.get(DomainConstants.SELECT_REVISION);
                                                
                                                strFLToType = (String)map1.get(DomainConstants.SELECT_TYPE);
                                                strFLToName = (String)map1.get(DomainConstants.SELECT_NAME);
                                                strFLToRev = (String)map1.get(DomainConstants.SELECT_REVISION);
                                            }else {
                                                
                                                strFLFromType = (String)map1.get(DomainConstants.SELECT_TYPE);
                                                strFLFromName = (String)map1.get(DomainConstants.SELECT_NAME);
                                                strFLFromRev = (String)map1.get(DomainConstants.SELECT_REVISION);
                                                
                                                strFLToType = (String)map.get(DomainConstants.SELECT_TYPE);
                                                strFLToName = (String)map.get(DomainConstants.SELECT_NAME);
                                                strFLToRev = (String)map.get(DomainConstants.SELECT_REVISION);
                                                
                                            } 
                                                strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strFLFromType)
                                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strFLFromName)
                                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strFLFromRev)
                                                            .append(C_FOPT_SEPERATOR)
                                            .append(strFLToType)
                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strFLToName)
                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strFLToRev)
                                            .append(DOUBLE_QUOTE);
                                            if(j < arrStr.length -1)
                                            {
                                                strBuffer.append(SPACE);
                                            }
                                        }else if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_FEATURES))
                                        {
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strType)
                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strName)
                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strRev)
                                            .append(DOUBLE_QUOTE);
                                        }else if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_PRODUCTS))
                                        {
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strType)
                                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strName)
                                                            .append(FEATURE_TNR_DELIMITER)
                                            .append(strRev)
                                            .append(DOUBLE_QUOTE);
                                            if(j < arrStr.length -1)
                                            {
                                                strBuffer.append(SPACE);
                                            }
                                        }
                                        strLeftExpression.append(strBuffer.toString());
                                        
                                    }// end of RULE_DISPLAY_FULL_NAME if Modified condition  for  IR Mx376196
                                    else if(strRuleDisplay.equals(RULE_DISPLAY_MARKETING_NAME)){
                                        
                                        StringBuffer strBuffer1 = new StringBuffer();
                                        strBuffer1.append(STR_ATTRIBUTE)
                                        .append(OPEN_BRACE)
                                        .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                                        .append(CLOSE_BRACE);
                                        
                                        String strMarketName = strBuffer1.toString();
                                        
                                        StringList lstObjectSel = new StringList(2);
                                        lstObjectSel.add(strMarketName);
                                        
                                        if (strType.contains(ConfigurationConstants.TYPE_FEATURE_LIST))
                                        {
                                            StringList lstRelationSel = new StringList(1);
                                            lstRelationSel.add(DomainRelationship.SELECT_ID);
                                            lstRelationSel.add(DomainRelationship.SELECT_NAME);
                                            
                                            String strRelationType = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+","+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM ; 
                                            
                                            MapList mapList = (MapList) domObj.getRelatedObjects( context,
                                                    strRelationType,
                                                    DomainConstants.QUERY_WILDCARD,
                                                    (StringList)lstObjectSel,
                                                    (StringList)lstRelationSel,
                                                    true,
                                                    true,
                                                    (short) 1,
                                                    "",
                                            "", 0);
                                            
                                            Hashtable map = (Hashtable)mapList.get(0);
                                            Hashtable map1 = (Hashtable)mapList.get(1); 
                                            String strFLFromMarketingName ="";
                                            String strFLToMarketingName = "";
                                            String strRelName = (String)map.get(DomainRelationship.SELECT_NAME);  
                                            
                                            if(strRelName.equals(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM)){
                                                
                                                strFLFromMarketingName = (String)map.get(strMarketName);
                                                strFLToMarketingName = (String)map1.get(strMarketName);
                                            }else {

                                                strFLFromMarketingName = (String)map1.get(strMarketName);
                                                strFLToMarketingName = (String)map.get(strMarketName);
                                    }
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strFLFromMarketingName)
                                                            .append(C_FOPT_SEPERATOR)
                                            .append(strFLToMarketingName)
                                            .append(DOUBLE_QUOTE);
                                            if(j < arrStr.length -1)
                                            {
                                                strBuffer.append(SPACE);
                                            }
                                    }
                                        else if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_FEATURES))
                                        {                                                            
                                            String strFOPTMarketingName = domObj.getInfo(context,strMarketName);
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strFOPTMarketingName)
                                            .append(DOUBLE_QUOTE);
                                            
                                }
                                        else if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_PRODUCTS))
                                        {
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strName)
                                            .append(DOUBLE_QUOTE);
                                        }


                                        strLeftExpression.append(strBuffer.toString());

                                    } else { // end of RULE_DISPLAY_MARKETING_NAME else   Start changes  for  IR Mx376196

                                        
                                        StringBuffer strBuffer1 = new StringBuffer();
                                        strBuffer1.append(STR_ATTRIBUTE)
                                        .append(OPEN_BRACE)
                                        .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                                        .append(CLOSE_BRACE);
                                        
                                        String strMarketName = strBuffer1.toString();
                                        
                                        StringList lstObjectSel = new StringList(2);
                                        lstObjectSel.add(strMarketName);
                                        lstObjectSel.add(ConfigurationConstants.SELECT_REVISION);
                                        
                                        if (strType.contains(ConfigurationConstants.TYPE_FEATURE_LIST))
                                        {
                                            StringList lstRelationSel = new StringList(1);
                                            lstRelationSel.add(DomainRelationship.SELECT_ID);
                                            lstRelationSel.add(DomainRelationship.SELECT_NAME);
                                            
                                            String strRelationType = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+","+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM ; 
                                            
                                            MapList mapList = (MapList) domObj.getRelatedObjects( context,
                                                    strRelationType,
                                                    DomainConstants.QUERY_WILDCARD,
                                                    (StringList)lstObjectSel,
                                                    (StringList)lstRelationSel,
                                                    true,
                                                    true,
                                                    (short) 1,
                                                    "",
                                            "", 0);
                                            
                                            Hashtable map = (Hashtable)mapList.get(0);
                                            Hashtable map1 = (Hashtable)mapList.get(1); 
                                            String strFLFromMarketingName ="";
                                            String strFLToMarketingName = "";
                                            String strRelName = (String)map.get(DomainRelationship.SELECT_NAME);  
                                            
                                            if(strRelName.equals(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM)){
                                                
                                                strFLFromMarketingName = ((String)map.get(strMarketName))+" "+((String)map.get(ConfigurationConstants.SELECT_REVISION));
                                                strFLToMarketingName = (String)map1.get(strMarketName)+" "+((String)map1.get(ConfigurationConstants.SELECT_REVISION));
                                            }else {

                                                strFLFromMarketingName = (String)map1.get(strMarketName)+" "+((String)map1.get(ConfigurationConstants.SELECT_REVISION));
                                                strFLToMarketingName = (String)map.get(strMarketName)+" "+((String)map.get(ConfigurationConstants.SELECT_REVISION));
                                            }
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strFLFromMarketingName)
                                                            .append(C_FOPT_SEPERATOR)
                                            .append(strFLToMarketingName)
                                            .append(DOUBLE_QUOTE);
                                            if(j < arrStr.length -1)
                                            {
                                                strBuffer.append(SPACE);
                                            }
                                        }
                                        else if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_FEATURES))
                                        {                                                            
                                            String strFOPTMarketingName = domObj.getInfo(context,strMarketName);
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(strFOPTMarketingName)
                                            .append(" "+strRev)
                                            .append(DOUBLE_QUOTE);
                                        }
                                        else if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_PRODUCTS))
                                        {
                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                            .append(domObj.getInfo(context,strMarketName))
                                            .append(SPACE)
                                            .append(strRev)
                                            .append(DOUBLE_QUOTE);
                                        }


                                        strLeftExpression.append(strBuffer.toString());
                                    // End changes  for  IR Mx376196
                                    }
                                    
                                }// end of strIsIntermediate attr if
                                
                                // Incase of Product and Model Compatibility Rules, the Right/Left Expression
                                // relationship is connected to Product/Model directly.
                                // Hence we can construct the TNR directly.

                                else
                                {
                                    strBuffer.delete(0,strBuffer.length());
                                    if((strRuleDisplay == null) ||
                                        (strRuleDisplay.equals("null")) ||
                                        (strRuleDisplay.equals("")) ||
                                        (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {

                                                strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                        .append(strType)
                                                            .append(FEATURE_TNR_DELIMITER)
                                        .append(strName)
                                                            .append(FEATURE_TNR_DELIMITER)
                                        .append(strRev)
                                        .append(DOUBLE_QUOTE);
                                        if(j < arrStr.length -1)
                                        {
                                            strBuffer.append(SPACE);
                                        }
                                                strLeftExpression.append(strBuffer.toString());
                                    }
	// Start changes  for  IR Mx376196
                                    else if(strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME)){
                                        strMarketingName = domObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_MARKETING_NAME);
                                        strLeftExpression.append(strMarketingName);
                                    } else {
                                    	strMarketingName = domObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_MARKETING_NAME);
                                        strLeftExpression.append(strMarketingName)
                                        .append(" "+domObj.getInfo(context,ConfigurationConstants.SELECT_REVISION));                                        
	// End chages  for  IR Mx376196
                                    }
                                } // end of strIsIntermediate attr else
                                
                            }// end of objectId else
                            
                        }// end of first if 
                        else{
                            strLeftExpression.append(arrStr[j]);
                            if(j < arrStr.length -1)
                            {
                                strLeftExpression.append(SPACE);
                                }
                            }
                                    
                        // end of newly added
                         }
                     lstLeftExpressionList.add(strLeftExpression.toString());
                   }
                }
                catch(Exception e){

                }
            
            return lstLeftExpressionList;
        }


        /**
         * This method is used only for the Quantity Rule
         * @param context
         * @param args
         * @param strRuleType
         * @return
         * @throws Exception
         * this could not be deprecated as referenced from migration JPO
         */
        public List getExpression(Context context, String[] args, String strRuleType) throws Exception {


            HashMap programMap = (HashMap) JPO.unpackArgs(args);


            MapList bcrObjectIdsList = (MapList) programMap.get("objectList");
            String strIsIntermediate= (String)((Map) programMap.get("paramList")).get("intermediate");
            String strRuleDisplay = (String) programMap.get("ruleDisplay");
            String strRelType = (String) programMap.get("relType");

            //Get the number of objects in objectList
            int iNumOfObjects = bcrObjectIdsList.size();
            String arrObjId[] = new String[iNumOfObjects];

            //Getting the bus ids for objects in the table
            for (int i = 0; i < iNumOfObjects; i++) {
                Object obj = bcrObjectIdsList.get(i);
                if (obj instanceof HashMap) {
                    arrObjId[i] = (String)((HashMap)bcrObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
                }
                else if (obj instanceof Hashtable) {
                    arrObjId[i] = (String)((Hashtable)bcrObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
                }
            }

            // if the type connected to Left Expression/Right Expression relationship is Feature List
            // then retrieve the Feature/Option TNRs. This case will arise in case of inclusion rule and BCR.

            List lstObjectSelects = new StringList();
            StringBuffer strBuffer = new StringBuffer(200);
            String strFLFromName = DomainConstants.EMPTY_STRING;
            String strFLFromType = DomainConstants.EMPTY_STRING;
            String strFLFromRev = DomainConstants.EMPTY_STRING;
            String strFLToName = DomainConstants.EMPTY_STRING;
            String strFLToType = DomainConstants.EMPTY_STRING;
            String strFLToRev = DomainConstants.EMPTY_STRING;
            String strMarketingName = DomainConstants.EMPTY_STRING;

            if(strIsIntermediate!=null && strIsIntermediate.equals("true"))
            {
                            lstObjectSelects.add(DomainConstants.SELECT_NAME);
                            lstObjectSelects.add(DomainConstants.SELECT_TYPE);
                            lstObjectSelects.add(DomainConstants.SELECT_REVISION);
                            lstObjectSelects.add(DomainConstants.SELECT_ID);
            }
            // otherwise retrieve the TNR of the object connected to the Left/Right Expression relationship
            // directly. This case will arise incase of Product and Model Compatiblity.

            else
            {
                if((strRuleDisplay == null) || 
                    (strRuleDisplay.equals("null")) || 
                    (strRuleDisplay.equals("")) || 
                    (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {

                            lstObjectSelects.add(DomainConstants.SELECT_NAME);
                            lstObjectSelects.add(DomainConstants.SELECT_TYPE);
                            lstObjectSelects.add(DomainConstants.SELECT_REVISION);
                }
                else {
                            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                        .append(OPEN_BRACE)
                                        .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                                        .append(CLOSE_BRACE);
                            strMarketingName = strBuffer.toString();
                            lstObjectSelects.add(strMarketingName);
                }
            }

            List lstRelationSelects = new StringList();

            strBuffer.delete(0,strBuffer.length());
            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                        .append(OPEN_BRACE)
                        .append(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER)
                        .append(CLOSE_BRACE);
            lstRelationSelects.add(strBuffer.toString());

            strBuffer.delete(0,strBuffer.length());
            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                        .append(OPEN_BRACE)
                        .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                        .append(CLOSE_BRACE);
            lstRelationSelects.add(strBuffer.toString());


            List lstLeftExpressionList = new StringList(iNumOfObjects);

            Map mpTNR = null;
            DomainObject dom = null;
            StringBuffer strLeftExpression = new StringBuffer(400);
            String strMQLCommand = "" ;
            String strRes = "" ;

            try{


                // for each Rule connected to the object form the Expression by getting the Token and Sequence Order
                // attributes from the relationships Left/Right Expression

                 for (int i = 0; i < iNumOfObjects; i++) {

                Vector strExpObj = new Vector();
                Vector strAttribSeqOrder = new Vector();

                        dom = new DomainObject(arrObjId[i]);
                        
                        MapList lstTNR =
                                dom.getRelatedObjects(
                                    context,
                                    strRelType,
                                    DomainConstants.QUERY_WILDCARD,
                                    (StringList)lstObjectSelects,
                                    (StringList)lstRelationSelects,
                                    false,
                                    true,
                                    (short) 1,
                                    "",
                                    "", 0);



                            strMQLCommand = "print bus \""+arrObjId[i]+"\" select from["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].toall from["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"].value dump |;" ; 
                            strRes  = MqlUtil.mqlCommand(context , strMQLCommand);



                            StringTokenizer strTokenCGIds = new StringTokenizer(strRes , "|");
                            int iTotalNoOfRecords = strTokenCGIds.countTokens();
                            int iNoOfRecords = 0 ; 

                            if(strRes.indexOf("R") != -1 ){
        

                        
                                iNoOfRecords = iTotalNoOfRecords/2 ; 
                                int iCnt1 = 0 ; 
                                String strNextVal = "" ; 
                            
                                while(strTokenCGIds.hasMoreTokens()){
                                    
                                    while(iCnt1 < iNoOfRecords) {
                                        strNextVal = strTokenCGIds.nextToken();
                                        strExpObj.add(strNextVal);
                                        iCnt1++ ; 
                                    }

                                    while(iCnt1 < iNoOfRecords*2 ) {
                                        strNextVal = strTokenCGIds.nextToken();
                                        strAttribSeqOrder.add(strNextVal);
                                        iCnt1++ ; 
                                    }
                                }


                                String strCGId = "" ; 
                                String strSeqOrder = ""; 
                                HashMap mp = new HashMap();
                                DomainRelationship domRel = null ; 
                                String strCGName = "" ;
                                for (int count = 0 ; count <strExpObj.size() ;count++ ){                                
                                    mp = new HashMap();
                                    strCGId = (String)strExpObj.get(count);
                                    if (strCGId.indexOf("R") != -1) {
                                                                
                                        strCGId = strCGId.substring(1,strCGId.length()) ; 
                                        domRel = new DomainRelationship(strCGId);
                                        strCGName =  domRel.getAttributeValue(context , "Common Group Name"); 
                                        strSeqOrder = (String)strAttribSeqOrder.get(count);
                                        mp.put("id" , strCGId);
                                        mp.put("attribute["+ConfigurationConstants.ATTRIBUTE_TOKEN+"]" , "");
                                        mp.put("attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]" , ((new Integer(strSeqOrder)).toString()));
                                        mp.put("relationship" , ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);
                                        mp.put("type" , "CommonGroup");
                                        mp.put("name" ,strCGName );
                                        mp.put("revision" , "");
                                        mp.put("level" , "1");
                                        lstTNR.add(mp);
                                    }
                                }
                            }
                        //sort the MapList based on the Sequence Order key present in it
                        strBuffer.delete(0,strBuffer.length());
                        strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                    .append(OPEN_BRACE)
                                    .append(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER)
                                    .append(CLOSE_BRACE);

                        lstTNR.addSortKey(strBuffer.toString(), "ascending", "integer");
                        lstTNR.sort();
                        strLeftExpression.delete(0,strLeftExpression.length());

                        for(int j=0;j<lstTNR.size();j++)
                        {
                            mpTNR=(Map)lstTNR.get(j);

                            strBuffer.delete(0,strBuffer.length());
                            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                        .append(OPEN_BRACE)
                                        .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                                        .append(CLOSE_BRACE);

                            if(((String)mpTNR.get(strBuffer.toString())).trim().equals(""))
                            {
                                // Incase of BCR and Inclusion rules, the Left/Right Expression
                                // relationship is connected to Feature List object.
                                // Hence we need to form the Feature/Option pair
                                // inorder to reframe the expression

                                if(strIsIntermediate!=null && strIsIntermediate.equals("true"))
                                {
                                    strBuffer.delete(0,strBuffer.length());
                                
                                if((strRuleDisplay == null) || 
                                    (strRuleDisplay.equals("null")) || 
                                    (strRuleDisplay.equals("")) || 
                                    (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {

                                    String strType = (String)mpTNR.get(DomainConstants.SELECT_TYPE);
                                    String strName = (String)mpTNR.get(DomainConstants.SELECT_NAME);
                                    String strRev = (String)mpTNR.get(DomainConstants.SELECT_REVISION);
                                    String strObjID = (String)mpTNR.get(DomainConstants.SELECT_ID);
                                    
                                    if (strType.contains(ConfigurationConstants.TYPE_FEATURE_LIST)){

                                                DomainObject dom1 = new DomainObject(strObjID); 


                                                StringList lstObjectSel = new StringList(4);
                                                lstObjectSel.add(DomainConstants.SELECT_ID);
                                                lstObjectSel.add(DomainConstants.SELECT_TYPE);
                                                lstObjectSel.add(DomainConstants.SELECT_NAME);
                                                lstObjectSel.add(DomainConstants.SELECT_REVISION);

                                                StringList lstRelationSel = new StringList(1);
                                                lstRelationSel.add(DomainRelationship.SELECT_ID);
                                                lstRelationSel.add(DomainRelationship.SELECT_NAME);

                                                String strRelationType = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+","+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM ; 

                                                MapList mapList = (MapList) dom1.getRelatedObjects( context,
                                                                        strRelationType,
                                                                        DomainConstants.QUERY_WILDCARD,
                                                                        (StringList)lstObjectSel,
                                                                        (StringList)lstRelationSel,
                                                                        true,
                                                                        true,
                                                                        (short) 1,
                                                                        "",
                                                                        "", 0);


                                                Hashtable map = (Hashtable)mapList.get(0);
                                                Hashtable map1 = (Hashtable)mapList.get(1); 

                                                String strRelName = (String)map.get(DomainRelationship.SELECT_NAME);  
                                    
                                                if(strRelName.equals(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM)){

                                                    strFLFromType = (String)map.get(DomainConstants.SELECT_TYPE);
                                                    strFLFromName = (String)map.get(DomainConstants.SELECT_NAME);
                                                    strFLFromRev = (String)map.get(DomainConstants.SELECT_REVISION);

                                                    strFLToType = (String)map1.get(DomainConstants.SELECT_TYPE);
                                                    strFLToName = (String)map1.get(DomainConstants.SELECT_NAME);
                                                    strFLToRev = (String)map1.get(DomainConstants.SELECT_REVISION);
                                                }else {
                                                
                                                    strFLFromType = (String)map1.get(DomainConstants.SELECT_TYPE);
                                                    strFLFromName = (String)map1.get(DomainConstants.SELECT_NAME);
                                                    strFLFromRev = (String)map1.get(DomainConstants.SELECT_REVISION);

                                                    strFLToType = (String)map.get(DomainConstants.SELECT_TYPE);
                                                    strFLToName = (String)map.get(DomainConstants.SELECT_NAME);
                                                    strFLToRev = (String)map.get(DomainConstants.SELECT_REVISION);
                                                
                                                } 
                                                    strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                            .append(strFLFromType)
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append(strFLFromName)
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append(strFLFromRev)
                                                            .append(C_FOPT_SEPERATOR)
                                                            .append(strFLToType)
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append(strFLToName)
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append(strFLToRev)
                                                            .append(DOUBLE_QUOTE)
                                                            .append(SPACE);
                                            }
                                                else if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_FEATURES)){
                                                    strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                            .append(strType)
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append(strName)
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append(strRev)
                                                            .append(DOUBLE_QUOTE);
                                                }
                                                else if(strType.contains("Product")){
                                                    strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                            .append(strType)
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append(strName)
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append(strRev)
                                                            .append(DOUBLE_QUOTE);
                                                }
                                                else if(strType.contains("CommonGroup")){
                                                    
                                                    strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                                .append(strType)
                                                                .append(FEATURE_TNR_DELIMITER)
                                                                .append((String)mpTNR.get(DomainConstants.SELECT_ID))
                                                                .append(DOUBLE_QUOTE)
                                                                .append(SPACE);
                                            }
                                                

                                                strLeftExpression.append(strBuffer.toString());

                                    }
                                    else {

                                            String strObjID = (String)mpTNR.get(DomainConstants.SELECT_ID);
                                            String strType = (String)mpTNR.get(DomainConstants.SELECT_TYPE);

                                            StringBuffer strBuffer1 = new StringBuffer();
                                            strBuffer1.append(STR_ATTRIBUTE)
                                                        .append(OPEN_BRACE)
                                                        .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                                                        .append(CLOSE_BRACE);

                                            String strMarketName = strBuffer1.toString();


                                            StringList lstObjectSel = new StringList(2);
                                            lstObjectSel.add(strMarketName);

                                            DomainObject dom1 = new DomainObject(strObjID); 
            
        
                                            if (strType.contains(ConfigurationConstants.TYPE_FEATURE_LIST)){



                                                StringList lstRelationSel = new StringList(1);
                                                lstRelationSel.add(DomainRelationship.SELECT_ID);
                                                lstRelationSel.add(DomainRelationship.SELECT_NAME);

                                                String strRelationType = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+","+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM ; 

                                                MapList mapList = (MapList) dom1.getRelatedObjects( context,
                                                                                strRelationType,
                                                                                DomainConstants.QUERY_WILDCARD,
                                                                                (StringList)lstObjectSel,
                                                                                (StringList)lstRelationSel,
                                                                                true,
                                                                                true,
                                                                                (short) 1,
                                                                                "",
                                                                                "", 0);

                                                        Hashtable map = (Hashtable)mapList.get(0);
                                                        Hashtable map1 = (Hashtable)mapList.get(1); 
                                                        String strFLFromMarketingName ="";
                                                        String strFLToMarketingName = "";
                                                        String strRelName = (String)map.get(DomainRelationship.SELECT_NAME);  
                                            
                                                        if(strRelName.equals(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM)){

                                                            strFLFromMarketingName = (String)map.get(strMarketName);
                                                            strFLToMarketingName = (String)map1.get(strMarketName);
                                                        }else {
                                                        
                                                            strFLFromMarketingName = (String)map1.get(strMarketName);
                                                            strFLToMarketingName = (String)map.get(strMarketName);
                                                        
                                                        } 

                                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                                    .append(strFLFromMarketingName)
                                                                    .append(C_FOPT_SEPERATOR)
                                                                    .append(strFLToMarketingName)
                                                                    .append(DOUBLE_QUOTE)
                                                                    .append(SPACE);
                                                    }
                                            else if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_FEATURES)){                                                            
                                                            String strFOPTMarketingName = dom1.getInfo(context,strMarketName);
                                                            strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                                    .append(strFOPTMarketingName)
                                                                    .append(DOUBLE_QUOTE);
                                        }else if(strType.contains("CommonGroup")){
                                        
                                        strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                    .append(strType)
                                                    .append(FEATURE_TNR_DELIMITER)
                                                    .append((String)mpTNR.get(DomainConstants.SELECT_ID))
                                                    .append(DOUBLE_QUOTE)
                                                    .append(SPACE);
                                                    }

                                                strLeftExpression.append(strBuffer.toString()); 
                                    }
                                }
                                // Incase of Product and Model Compatibility Rules, the Right/Left Expression
                                // relationship is connected to Product/Model directly.
                                // Hence we can construct the TNR directly.

                                else
                                {
                                    strBuffer.delete(0,strBuffer.length());
                                    if((strRuleDisplay == null) || 
                                        (strRuleDisplay.equals("null")) || 
                                        (strRuleDisplay.equals("")) || 
                                        (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {

                                                strBuffer = strBuffer.append(DOUBLE_QUOTE)
                                                            .append((String)mpTNR.get(DomainConstants.SELECT_TYPE))
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append((String)mpTNR.get(DomainConstants.SELECT_NAME))
                                                            .append(FEATURE_TNR_DELIMITER)
                                                            .append((String)mpTNR.get(DomainConstants.SELECT_REVISION))
                                                            .append(DOUBLE_QUOTE)
                                                            .append(SPACE);
                                                strLeftExpression.append(strBuffer.toString());
                                    }
                                    else {
                                            strLeftExpression.append((String)mpTNR.get(strMarketingName));
                                    }
                                }
                            }
                                    // Incase of Self Loop, Token will contain "AND"/"OR"/"NOT"/"["/"]" in it.
                                    // Else it will be empty.

                                    strBuffer.delete(0,strBuffer.length());
                                    strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                                .append(OPEN_BRACE)
                                                .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                                                .append(CLOSE_BRACE);

                                    strLeftExpression.append((String)mpTNR.get(strBuffer.toString())).append(SPACE);
                         }


                    lstLeftExpressionList.add(strLeftExpression.toString());
                   }
                }
                catch(Exception e){

                }


            return lstLeftExpressionList;
        }
        
        /**
         * This method is used to update the Attributes Left Expression and Right Expression
         * 
         * @param context
         * @param args
         * @return List
         * @throws Exception
         */
        public List getExpression(Context context, String[] args) throws Exception {


            HashMap programMap = (HashMap) JPO.unpackArgs(args);


            MapList bcrObjectIdsList = (MapList) programMap.get("objectList");
            String strIsIntermediate= (String)((Map) programMap.get("paramList")).get("intermediate");
            String strRelType = (String) programMap.get("strRelType");
            String strRuleDisplay = (String) programMap.get("strRuleDisplay");
            //Get the number of objects in objectList
            int iNumOfObjects = bcrObjectIdsList.size();
            String arrObjId[] = new String[iNumOfObjects];

            //Getting the bus ids for objects in the table
            for (int i = 0; i < iNumOfObjects; i++) {
                Object obj = bcrObjectIdsList.get(i);
                if (obj instanceof HashMap) {
                    arrObjId[i] = (String)((HashMap)bcrObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
                }
                else if (obj instanceof Hashtable) {
                    arrObjId[i] = (String)((Hashtable)bcrObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
                }
            }

            // if the type connected to Left Expression/Right Expression relationship is Feature List
            // then retrieve the Feature/Option TNRs. This case will arise in case of inclusion rule and BCR.

            List lstObjectSelects = new StringList();
            StringBuffer strBuffer = new StringBuffer(200);
            String strMarketingName = DomainConstants.EMPTY_STRING;

            if(strIsIntermediate!=null && strIsIntermediate.equals("true"))
            {
                            lstObjectSelects.add(DomainConstants.SELECT_NAME);
                            lstObjectSelects.add(DomainConstants.SELECT_TYPE);
                            lstObjectSelects.add(DomainConstants.SELECT_REVISION);
                            lstObjectSelects.add(DomainConstants.SELECT_ID);
            }
            // otherwise retrieve the TNR of the object connected to the Left/Right Expression relationship
            // directly. This case will arise incase of Product and Model Compatiblity.

            else
            {
                if((strRuleDisplay == null) || 
                    (strRuleDisplay.equals("null")) || 
                    (strRuleDisplay.equals("")) || 
                    (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {

                            lstObjectSelects.add(DomainConstants.SELECT_NAME);
                            lstObjectSelects.add(DomainConstants.SELECT_TYPE);
                            lstObjectSelects.add(DomainConstants.SELECT_REVISION);
                }
                else {
                            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                        .append(OPEN_BRACE)
                                        .append(ConfigurationConstants.ATTRIBUTE_MARKETING_NAME)
                                        .append(CLOSE_BRACE);
                            strMarketingName = strBuffer.toString();
                            lstObjectSelects.add(strMarketingName);
                }
            }
            
            lstObjectSelects.add(ConfigurationConstants.SELECT_PHYSICAL_ID);

            List lstRelationSelects = new StringList();

            strBuffer.delete(0,strBuffer.length());
            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                        .append(OPEN_BRACE)
                        .append(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER)
                        .append(CLOSE_BRACE);
            lstRelationSelects.add(strBuffer.toString());

            strBuffer.delete(0,strBuffer.length());
            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                        .append(OPEN_BRACE)
                        .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                        .append(CLOSE_BRACE);
            lstRelationSelects.add(strBuffer.toString());


            List lstLeftExpressionList = new StringList(iNumOfObjects);

            Map mpTNR = null;
            DomainObject dom = null;
            StringBuffer strLeftExpression = new StringBuffer(400);
            String strMQLCommand = "" ;
            String strRes = "" ;

            try{


                // for each Rule connected to the object form the Expression by getting the Token and Sequence Order
                // attributes from the relationships Left/Right Expression

                 for (int i = 0; i < iNumOfObjects; i++) {

                Vector strExpObj = new Vector();
                Vector strAttribSeqOrder = new Vector();

                        dom = new DomainObject(arrObjId[i]);
                        
                        MapList lstTNR =
                                dom.getRelatedObjects(
                                    context,
                                    strRelType,
                                    DomainConstants.QUERY_WILDCARD,
                                    (StringList)lstObjectSelects,
                                    (StringList)lstRelationSelects,
                                    false,
                                    true,
                                    (short) 1,
                                    "",
                                    "", 0);
                            strMQLCommand = "print bus \""+arrObjId[i]+"\" select from["+strRelType+"].toall from["+strRelType+"].attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"].value dump |;" ;
                            strRes  = MqlUtil.mqlCommand(context , strMQLCommand);
                            StringTokenizer strTokenCGIds = new StringTokenizer(strRes , "|");
                            int iTotalNoOfRecords = strTokenCGIds.countTokens();
                            int iNoOfRecords = 0 ; 

                            if(strRes.indexOf("R") != -1 ){
                                iNoOfRecords = iTotalNoOfRecords/2 ; 
                                int iCnt1 = 0 ; 
                                String strNextVal = "" ; 
                               
                                while(strTokenCGIds.hasMoreTokens()){
                                    
                                    while(iCnt1 < iNoOfRecords) {
                                        strNextVal = strTokenCGIds.nextToken();
                                        strExpObj.add(strNextVal);
                                        iCnt1++ ; 
                                    }

                                    while(iCnt1 < iNoOfRecords*2 ) {
                                        strNextVal = strTokenCGIds.nextToken();
                                        strAttribSeqOrder.add(strNextVal);
                                        iCnt1++ ; 
                                    }
                                }


                                String strCGId = "" ; 
                                String strSeqOrder = ""; 
                                HashMap mp = new HashMap();
                                DomainRelationship domRel = null ; 
                                String strCGName = "" ;
                                for (int count = 0 ; count <strExpObj.size() ;count++ ){                                
                                    mp = new HashMap();
                                    strCGId = (String)strExpObj.get(count);
                                    if (strCGId.indexOf("R") != -1) {
                                                                
                                        strCGId = strCGId.substring(1,strCGId.length()) ; 
                                        domRel = new DomainRelationship(strCGId);
                                        strCGName =  domRel.getAttributeValue(context , "Common Group Name");
                                        MapList relData = DomainRelationship.getInfo(context,new String[]{strCGId}, new StringList(ConfigurationConstants.SELECT_PHYSICAL_ID));
                                        strSeqOrder = (String)strAttribSeqOrder.get(count);
                                        strCGId = (String)((Hashtable)relData.get(0)).get(ConfigurationConstants.SELECT_PHYSICAL_ID);
                                        mp.put("id" , strCGId);
                                        mp.put("attribute["+ConfigurationConstants.ATTRIBUTE_TOKEN+"]" , "");
                                        mp.put("attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]" , ((new Integer(strSeqOrder)).toString()));
                                        mp.put("relationship" , strRelType);
                                        mp.put("type" , "CommonGroup");
                                        mp.put("name" ,strCGName );
                                        mp.put("revision" , "");
                                        mp.put("level" , "1");
                                        lstTNR.add(mp);
                                    }
                                }
                            }
                        //sort the MapList based on the Sequence Order key present in it
                        strBuffer.delete(0,strBuffer.length());
                        strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                    .append(OPEN_BRACE)
                                    .append(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER)
                                    .append(CLOSE_BRACE);

                        lstTNR.addSortKey(strBuffer.toString(), "ascending", "integer");
                        lstTNR.sort();
                        strLeftExpression.delete(0,strLeftExpression.length());

                        for(int j=0;j<lstTNR.size();j++)
                        {
                            mpTNR=(Map)lstTNR.get(j);

                            strBuffer.delete(0,strBuffer.length());
                            strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                        .append(OPEN_BRACE)
                                        .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                                        .append(CLOSE_BRACE);

                            if(((String)mpTNR.get(strBuffer.toString())).trim().equals(""))
                            {
                                // Incase of BCR and Inclusion rules, the Left/Right Expression
                                // relationship is connected to Feature List object.
                                // Hence we need to form the Feature/Option pair
                                // inorder to reframe the expression

                                if(strIsIntermediate!=null && strIsIntermediate.equals("true"))
                                {
                                    strBuffer.delete(0,strBuffer.length());
                                
                                if((strRuleDisplay == null) || 
                                    (strRuleDisplay.equals("null")) || 
                                    (strRuleDisplay.equals("")) || 
                                    (strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {

                                    String strType = (String)mpTNR.get(DomainConstants.SELECT_TYPE);
                                    String strObjID = (String)mpTNR.get(ConfigurationConstants.SELECT_PHYSICAL_ID);
                                    
                                    
                                    if(mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_PRODUCTS) ||
                                    		mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES) ||
                                    		mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
                                                    strBuffer = strBuffer.append("B")
                                                    .append(strObjID)
                                                    .append(SPACE);
                                    }else if(strType.contains("CommonGroup")){
                                                    
                                                    strBuffer = strBuffer.append("R")
                                                                .append(strObjID)
                                                                .append(SPACE);
                                    }
                                    strLeftExpression.append(strBuffer.toString());

                                    }
                                    else {

                                            String strObjID = (String)mpTNR.get(ConfigurationConstants.SELECT_PHYSICAL_ID);
                                            String strType = (String)mpTNR.get(DomainConstants.SELECT_TYPE);
                                            
                                            if(mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_PRODUCTS) ||
                                            		mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES) ||
                                            		mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
                                                            strBuffer = strBuffer.append("B")
                                                            .append(strObjID)
                                                            .append(SPACE);
                                            }else if(strType.contains("CommonGroup")){
                                                            
                                                            strBuffer = strBuffer.append("R")
                                                                        .append(strObjID)
                                                                        .append(SPACE);
                                            }

                                                strLeftExpression.append(strBuffer.toString()); 
                                    }
                                }
                                // Incase of Product and Model Compatibility Rules, the Right/Left Expression
                                // relationship is connected to Product/Model directly.
                                // Hence we can construct the TNR directly.

                                else
                                {
                                    strBuffer.delete(0,strBuffer.length());
                                    

                                                strBuffer = strBuffer.append("B")
                                                            .append((String)mpTNR.get(DomainConstants.SELECT_ID))
                                                            .append(SPACE);
                                                strLeftExpression.append(strBuffer.toString());
                                    
                                }
                            }
                                    // Incase of Self Loop, Token will contain "AND"/"OR"/"NOT"/"["/"]" in it.
                                    // Else it will be empty.

                                    strBuffer.delete(0,strBuffer.length());
                                    strBuffer = strBuffer.append(STR_ATTRIBUTE)
                                                .append(OPEN_BRACE)
                                                .append(ConfigurationConstants.ATTRIBUTE_TOKEN)
                                                .append(CLOSE_BRACE);

                                    strLeftExpression.append(" AND").append(SPACE);
                         }
                        String strLeftExpressionFinal = strLeftExpression.toString();
                        if (strLeftExpressionFinal.endsWith(" AND ")) {
            				int indx = strLeftExpressionFinal.lastIndexOf(" AND ");

            				strLeftExpressionFinal = strLeftExpressionFinal.substring(0, indx);
            			} 
                    lstLeftExpressionList.add(strLeftExpressionFinal);
                   }
                }
                catch(Exception e){

                }


            return lstLeftExpressionList;
        }
        // End  - Code Added by Amarja ,3dPLM

       



        /**
         * Method to check whether an element is present in a MapList.
         *
         * @param lstIds holds the ids.
         * @param strId holds the string id
         * @return boolean
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         * 
         */

        public boolean containsId(List lstIds, Object strId)throws Exception
        {
            Map mapTemp;
            boolean bPresent = false;
            for (int i = 0; i < lstIds.size() ; i++)
            {
                mapTemp = (Map)lstIds.get(i);
                if (((String)strId).equals(mapTemp.get(DomainConstants.SELECT_ID)))
                {
                    bPresent = true;
                    break;
                }
            }
            return bPresent;
        }


/**
 * Wrapper method to display the Right Expression value in BCR List Page
 * Called from Table Column settings  
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getRightExpressionToDisplayInBCRListPage(Context context, String[] args) throws Exception {
    RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, args,ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);

}

/**
 * Wrapper method to display the Left Expression value in BCR List Page  
 * Called from Table Column settings
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getLeftExpressionToDisplayInBCRListPage(Context context, String[] args) throws Exception  {
	RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, args,ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION);
}

/**
 * Wrapper method to display the Right Expression value in PCR List Page
 * Called from Table Column settings  
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getRightExpressionToDisplayInPCRListPage(Context context, String[] args) throws Exception {
    RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, args,ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);

}

/**
 * Wrapper method to display the Left Expression value in PCR List Page  
 * Called from Table Column settings
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getLeftExpressionToDisplayInPCRListPage(Context context, String[] args) throws Exception  {
	RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, args,ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION);
}


/**
 * This method is used to return inclusion/exclusion Rule Expression.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds arguments
 * @return List
 * @throws Exception if the operation fails
 * @since R212
 * @author IXH
 * @category 
 */
public List showInclusionRuleExpression(Context context, String[] args) throws Exception
 {

		List lstRightExpression = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		MapList extractedIRs = new MapList();
		for (int i = 0; i < objectList.size(); i++) {
			Map objectMap = (Map) objectList.get(i);
			Map iRuleobjectMap = new HashMap();
			String strIncRuleRE = "";
			StringList slRuleType = new StringList();
			Object slRuleTypeObj = (Object) objectMap.get("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.type");
			slRuleType = ConfigurationUtil.convertObjToStringList(context,
					slRuleTypeObj);
			int iRuleIndex = -1;
			if (slRuleType != null
					&& slRuleType
							.contains(ConfigurationConstants.TYPE_INCLUSION_RULE)) {
				iRuleIndex = slRuleType
						.indexOf(ConfigurationConstants.TYPE_INCLUSION_RULE);
			}
			if (iRuleIndex != -1) {
				Object slREsObjects = (Object) objectMap
						.get(ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION);
				StringList slREs = new StringList();
				slREs = ConfigurationUtil.convertObjToStringList(context,
						slREsObjects);
				strIncRuleRE = (String) slREs.get(iRuleIndex);
				iRuleobjectMap
						.put(
								ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION,
								strIncRuleRE);
			} else {
				iRuleobjectMap
						.put(
								ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION,
								strIncRuleRE);
			}
			extractedIRs.add(iRuleobjectMap);
		}
		Map newProgramMap = new HashMap();
		newProgramMap.put("objectList", extractedIRs);
		RuleProcess ruleProcess = new RuleProcess();
		lstRightExpression = ruleProcess.getExpressionForRuleDisplay(context,
				JPO.packArgs(newProgramMap),
				ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);
		if (lstRightExpression.size() == 0) {
			for (int i = 0; i < objectList.size(); i++) {
				lstRightExpression.add("");
			}
		}
		return lstRightExpression;
	}

public String getRuleExpressionInBCRPropertiesPage(Context context,String[] args) throws Exception {
	Map programMap = (HashMap) JPO.unpackArgs(args);
    Map paramMap = (HashMap) programMap.get("paramMap");
    
   String strObjectId = (String)paramMap.get("objectId");
   DomainObject objDomain = new DomainObject(strObjectId);
   
   StringList objectSelects = new StringList();
   objectSelects.add(DomainConstants.SELECT_ID);
   objectSelects.add(DomainConstants.SELECT_NAME);
   objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_COMPARISION_OPERATOR);
   objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_ERROR_MESSAGE);
	 
   Map attributeMap = objDomain.getInfo(context, objectSelects);
   Map mpBooleanCompatibilityRule= new HashMap();
   mpBooleanCompatibilityRule.put(DomainConstants.SELECT_ID, strObjectId);
   mpBooleanCompatibilityRule.put(DomainConstants.SELECT_NAME, strObjectId);
   mpBooleanCompatibilityRule.put(ConfigurationConstants.SELECT_ATTRIBUTE_COMPARISION_OPERATOR, attributeMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPARISION_OPERATOR));
   mpBooleanCompatibilityRule.put(ConfigurationConstants.SELECT_ATTRIBUTE_ERROR_MESSAGE,  attributeMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_ERROR_MESSAGE));

   BooleanCompatibilityRule bcrRule = new BooleanCompatibilityRule(mpBooleanCompatibilityRule);
   bcrRule.populateRuleExpressions(context);
   return bcrRule.getCompleteRuleExpression(context);
}

/**
 * Wrapper method to display the Right Expression value in BCR Properties Page  
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getRightExpressionToDisplayInBCRPropertiesPage(Context context, String[] args) throws Exception {
    
	Map programMap2 = (HashMap) JPO.unpackArgs(args);
    Map relBusObjPageList = (HashMap) programMap2.get("paramMap");
    
    Map requestMap = (HashMap) programMap2.get("requestMap");
    String reportFormat = (String)requestMap.get("reportFormat");
    
   String strObjectId = (String)relBusObjPageList.get("objectId");
   DomainObject dom = new DomainObject(strObjectId);
   
   //getting the BCR attributes through the bean ..variables for fetching Expression
   Map mapTemp                 = new HashMap();
   MapList objectList          = new MapList();
   Map paramList               = new HashMap();
   HashMap programMap          = new HashMap();
   String[] arrJPOArguments    = new String[1];

   mapTemp.put("id", strObjectId);
   mapTemp.put("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]",dom.getInfo(context,"attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]"));
   objectList.add(mapTemp);
   paramList.put("intermediate", "true");
   programMap.put("objectList", objectList);
   programMap.put("paramList", paramList);
   programMap.put("reportFormat", reportFormat);
   arrJPOArguments= JPO.packArgs(programMap);
	
	
	RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, arrJPOArguments,ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);

}

/**
 * Wrapper method to display the Left Expression value in BCR Properties Page  
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getLeftExpressionToDisplayInBCRPropertiesPage(Context context, String[] args) throws Exception  {
	
	Map programMap2 = (HashMap) JPO.unpackArgs(args);
    Map relBusObjPageList = (HashMap) programMap2.get("paramMap");
   
    Map requestMap = (HashMap) programMap2.get("requestMap");
    String reportFormat = (String)requestMap.get("reportFormat");
   
    String strObjectId = (String)relBusObjPageList.get("objectId");
   DomainObject dom = new DomainObject(strObjectId);
   
   //getting the BCR attributes through the bean ..variables for fetching Expression
   Map mapTemp                 = new HashMap();
   MapList objectList          = new MapList();
   Map paramList               = new HashMap();
   HashMap programMap          = new HashMap();
   String[] arrJPOArguments    = new String[1];

   mapTemp.put("id", strObjectId);
   mapTemp.put("attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]",dom.getInfo(context,"attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]"));
   objectList.add(mapTemp);
   paramList.put("intermediate", "true");
   programMap.put("objectList", objectList);
   programMap.put("paramList", paramList);
   programMap.put("reportFormat", reportFormat);
   arrJPOArguments= JPO.packArgs(programMap);
	
	
	RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, arrJPOArguments,ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION);
}
/**
 * program for rendering PCRs in the PRC tab
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getProductCompatibilityRules(
        Context context,
        String[] args)
        throws FrameworkException
    {
		List lstRuleList = new MapList();
		try {
			Map ProductCompatibilityMap = (HashMap) JPO.unpackArgs(args);
			Map mapTemp = new HashMap();

			List mapBusIds = new MapList();

			String strMode = (String) ProductCompatibilityMap.get("mode");
			String strObjectId = DomainConstants.EMPTY_STRING;
			String strProdId = DomainConstants.EMPTY_STRING;
			String strRuleId = DomainConstants.EMPTY_STRING;

			if ((strMode != null) && !("".equals(strMode))
					&& !("null".equals(strMode))) {
				if (strMode.equals("ListPage")) {
					strObjectId = (String) ProductCompatibilityMap
							.get("emxTableRowId");
				}
			} else {
				strObjectId = (String) ProductCompatibilityMap.get("objectId");
			}

			// String is initialized to store the value of relationship name
			StringBuffer sbBuffer = new StringBuffer(200);
			sbBuffer
					.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES);
			String strRelationshipType = sbBuffer.toString();

			// List is initialized to store the value of id
			StringList lstobjectSelects = new StringList();
			lstobjectSelects.add(SELECT_ID);
			lstobjectSelects.add(SELECT_TYPE);

			// get the children types of Product and make it a comma seperated
			// string
			List lstProductChildTypes = ProductLineUtil.getChildrenTypes(
					context, ConfigurationConstants.TYPE_PRODUCTS);
			sbBuffer.delete(0, sbBuffer.length());
			for (int i = 0; i < lstProductChildTypes.size(); i++) {
				sbBuffer = sbBuffer.append(lstProductChildTypes.get(i));
				if (i != lstProductChildTypes.size() - 1) {
					sbBuffer = sbBuffer.append(COMMA);
				}
			}

			// Create a domainobject and get the products in the product
			// structure
			DomainObject dom = newInstance(context, strObjectId);
			// TODO- remove deprecation
			List allObjList = dom.getRelatedObjects(context,
					strRelationshipType, ConfigurationConstants.TYPE_PRODUCTS,
					false, true, (short) 0, (StringList) lstobjectSelects,
					null, DomainConstants.EMPTY_STRING,
					DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING,
					sbBuffer.toString(), null);
			// context product id is added into the maplist.
			mapTemp.put(DomainConstants.SELECT_ID, strObjectId);
			allObjList.add(mapTemp);

			StringBuffer stbBuffer = new StringBuffer(200);

			// selecting even the Comparison Operator and Compatibility Option
			// as we need it in the
			// column JPO method getCompatibilityOption, for forming the
			// Compatibility Column.
			stbBuffer = stbBuffer
					.append(STR_ATTRIBUTE)
					.append(OPEN_BRACE)
					.append(
							ConfigurationConstants.ATTRIBUTE_COMPARISION_OPERATOR)
					.append(CLOSE_BRACE);

			List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
			
			lstObjectSelects.add(DomainConstants.SELECT_TYPE);
			lstObjectSelects.add(stbBuffer.toString());
			List lstRelSelects = new StringList(DomainRelationship.SELECT_ID);
			lstRelSelects.add(stbBuffer.toString());
			stbBuffer.delete(0, stbBuffer.length());
			stbBuffer = stbBuffer
					.append(STR_ATTRIBUTE)
					.append(OPEN_BRACE)
					.append(
							ConfigurationConstants.ATTRIBUTE_COMPATIBILITY_OPTION)
					.append(CLOSE_BRACE);
			lstObjectSelects.add(stbBuffer.toString());

			StringBuffer stbTemp = new StringBuffer(50);
			stbTemp = stbTemp
					.append(ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION)
					.append(COMMA)
					.append(
							ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);

			lstObjectSelects.add(STR_ATTRIBUTE + OPEN_BRACE
					+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
					+ CLOSE_BRACE);

			lstObjectSelects.add(STR_ATTRIBUTE + OPEN_BRACE
					+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
					+ CLOSE_BRACE);

			// for fetching all the Product Compatibility Rules connected to
			// each of the Product
			// in Product structure of the context product.
			for (int k = 0; k < allObjList.size(); k++) {
				mapTemp = (Map) allObjList.get(k);
				strProdId = (String) mapTemp.get(SELECT_ID);
				DomainObject domContextBus = new DomainObject(strProdId);
				mapBusIds = domContextBus.getRelatedObjects(context, stbTemp
						.toString(),
						ConfigurationConstants.TYPE_PRODUCT_COMPATIBILITY_RULE,
						(StringList) lstObjectSelects,
						(StringList) lstRelSelects, true, false, (short) 1,
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING, (short) 0,
						DomainObject.CHECK_HIDDEN,
						DomainObject.PREVENT_DUPLICATES,
						(short) DomainObject.PAGE_SIZE, null, null, null,
						DomainObject.EMPTY_STRING, null,
						DomainObject.FILTER_ITEM);
				for (int j = 0; j < mapBusIds.size(); j++) {
					mapTemp = (Map) mapBusIds.get(j);
					strRuleId = (String) mapTemp.get(DomainConstants.SELECT_ID);
					// to avoid duplicate Rules
					if (!(containsId(lstRuleList, strRuleId))) {
						lstRuleList.add(mapTemp);
					}
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return (MapList) lstRuleList;
	}

	/**
	 * Exclude program for add existing BCR
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeConnectedBCR(Context context, String[] args) throws Exception
 {
		Map programMap = (Map) JPO.unpackArgs(args);
		// Logical Feature ID
		String relWhere = DomainObject.EMPTY_STRING;
		String objWhere = DomainObject.EMPTY_STRING;
		
		// Obj and Rel Selects
		StringList objSelects = new StringList();
		StringList relSelects = new StringList();

		String filterExpression = DomainObject.EMPTY_STRING;
		String objectId = (String) programMap.get("objectId");
		DomainObject obj = new DomainObject(objectId);
		String type = obj.getInfo(context, DomainObject.SELECT_TYPE);
		
		ConfigurationUtil confUtil = new ConfigurationUtil(objectId);
		MapList objectList = confUtil.getObjectStructure(context,
				ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE,
				ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE,
				objSelects, relSelects, false, true, (short) 1, 0, objWhere,
				relWhere, DomainObject.FILTER_ITEM, filterExpression);
		//this check is inserted to filter out all rules except design for Logical Feature context. 
		if(type.equalsIgnoreCase("Logical Feature"))
		{
			objWhere = "attribute["+ConfigurationConstants.ATTRIBUTE_RULE_CLASSIFICATION+"]!=Logical";
			objSelects = ConfigurationUtil.getBasicObjectSelects(context);
			MapList objectListForLF = confUtil.findObjects(context, 
					ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE,
					DomainConstants.QUERY_WILDCARD,objWhere, objSelects);				
				
			objectList.addAll(objectListForLF);
		}
		
		StringList bcrToExclude = new StringList();
		for (int i = 0; i < objectList.size(); i++) {
			Map mapPartObj = (Map) objectList.get(i);
			if (mapPartObj.containsKey(DomainObject.SELECT_ID)) {
				String partIDToExclude = (String) mapPartObj
						.get(DomainObject.SELECT_ID);
				bcrToExclude.add(partIDToExclude);
			}
		}
		return bcrToExclude;
}
/**
 * Method to get all the BCR objects connected to the product from the data
 * base.
 * 
 * @param context
 *            the eMatrix <code>Context</code> object
 * @param args
 *            holds the following input arguments: 0 - HashMap containing
 *            one String entry for key "objectId"
 * @return MapList containing the id of Boolean Compatibility Rule objects
 * @throws Exception
 *             if the operation fails 
 * @since R212             
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getBooleanCompatibilityRules(Context context,
		String[] args)
			throws FrameworkException {
		MapList booleanCompatibilityMapList = new MapList();
		try {
			HashMap BooleanCompatibilityMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) BooleanCompatibilityMap.get("objectId");
			StringList objectSelects = new StringList(DomainObject.SELECT_ID);
			objectSelects.add("attribute["
					+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "]");
			objectSelects.add("attribute["
					+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]");
			StringList relationsSelects = new StringList(
					DomainObject.SELECT_RELATIONSHIP_ID);
			ConfigurationUtil contextObj = new ConfigurationUtil(objectId);
			booleanCompatibilityMapList = contextObj
					.getObjectStructure(
							context,
							ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE,
							ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE,
							objectSelects, relationsSelects, false, true, 1, 0,
							DomainObject.EMPTY_STRING,
							DomainObject.EMPTY_STRING,
							DomainObject.FILTER_ITEM, DomainObject.EMPTY_STRING);
		} catch (Exception e) {
			throw new FrameworkException();
		}

		return booleanCompatibilityMapList;
}

/**
 * Method call to get the list of all related Products.
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args - Holds parameters passed from the calling method
 * @return - Maplist of bus ids of candidate Products
 * @throws Exception if the operation fails
 * @since ProductCentral 10.0.0.0
 * @grade 0
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getValidProductsForPCR (Context context, String[] args) throws Exception {
    StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    //Unpacking the args
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    String sLimitFilterValue="";
    String strObjWhere="";
    int strlimit=0;
	String sNameFilterValue = (String) programMap
	.get("FTRLogicalFeatureNameFilterCommand");
    boolean isCalledFromRule=false;
    if(programMap.containsKey("FTRLogicalFeatureNameFilterForRuleDialog")){
  		sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterForRuleDialog");
	  	if(sNameFilterValue!=null) isCalledFromRule= true;
		if (sNameFilterValue != null
				&& !(sNameFilterValue.equalsIgnoreCase("*"))) {

			strObjWhere = "attribute["
				+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME
				+ "] ~~ '" + sNameFilterValue + "'";
		}
    }
	if(isCalledFromRule){
		if(!strObjWhere.trim().isEmpty())
			strObjWhere=strObjWhere+" && "+RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
		else
			strObjWhere=RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
	}	
    if(programMap.containsKey("FTRLogicalFeatureLimitFilterForRuleDialog")){
    	sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterForRuleDialog");
		if (sLimitFilterValue != null
				&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
			if (sLimitFilterValue.length() > 0) {
				strlimit = (short) Integer.parseInt(sLimitFilterValue);
				if (strlimit < 0) {
					strlimit = 32767;
				}
			}
		}
    }
    MapList relBusObjPageList = new MapList();
    //Gets the objectids and the relation names from args
    String paramMap = (String)programMap.get("objectId");
    //Domain Object initialized with the object id.
    DomainObject dom = new DomainObject(paramMap);
    short sLevel = 1;
    //Gets the relationship name
    String strRelName = ProductLineConstants.RELATIONSHIP_PRODUCTS;
    String strTypeName = ProductLineConstants.TYPE_PRODUCTS;
    relBusObjPageList = dom.getRelatedObjects(context, strRelName, strTypeName,
            objectSelects, relSelects, false, true, sLevel, strObjWhere, "",strlimit);
    return  relBusObjPageList;
}
/**
 * Method to check the state of Rule
 * @param context
 * @param argsz
 * @return
 * @throws FrameworkException
 */
public static Boolean isFrozenState(Context context, String[] args )throws FrameworkException
{
	boolean bInvalidState = false;
	try{
    	HashMap requestMap = (HashMap) JPO.unpackArgs(args);
    	String objectId = (String) requestMap.get("objectId");
    	if(ConfigurationUtil.isNotNull(objectId)){
    		bInvalidState = ConfigurationUtil.isFrozenState(context, objectId);
    	}
	}
	catch(Exception e){
		throw new FrameworkException(e);
	}

	return !bInvalidState;
}

/**
 * Method to get all the Product Compatibility Rules connected to the product from the data base.
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - HashMap containing one String entry for key "objectId"
 * @return MapList containing the id of Compatibility Rule objects
 * @throws Exception if the operation fails
 * @since R214
 */

@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getAllProductCompatibilityRules(
    Context context,
    String[] args)
    throws Exception
{
    Map ProductCompatibilityMap = (HashMap) JPO.unpackArgs(args);
    Map mapTemp = new HashMap();

    List mapBusIds = new MapList();
    List lstRuleList = new MapList();

    String strMode = (String)  ProductCompatibilityMap.get("mode");
    String strObjectId = DomainConstants.EMPTY_STRING;
    String strProdId = DomainConstants.EMPTY_STRING;
    String strRuleId = DomainConstants.EMPTY_STRING;

    if((strMode != null) && !("".equals(strMode)) && !("null".equals(strMode)))
    {
        if(strMode.equals("ListPage"))
        {
            strObjectId = (String) ProductCompatibilityMap.get("emxTableRowId");
        }
    }
    else
    {
        strObjectId = (String) ProductCompatibilityMap.get("objectId");
    }

    //String is initialized to store the value of relationship name
    StringBuffer sbBuffer = new StringBuffer(200);
    sbBuffer.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES);
    sbBuffer.append(COMMA);
    sbBuffer.append(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES);
    String strRelationshipType = sbBuffer.toString();

    //List is initialized to store the value of id
    List lstobjectSelects = new StringList();
    lstobjectSelects.add(SELECT_ID);

    //get the children types of Product and make it a comma seperated string
    List lstProductChildTypes = ProductLineUtil.getChildrenTypes(
                            context,
                            ConfigurationConstants.TYPE_PRODUCTS);
    sbBuffer.delete(0,sbBuffer.length());
    for (int i=0; i < lstProductChildTypes.size(); i++)
    {
        sbBuffer = sbBuffer.append(lstProductChildTypes.get(i));
        if (i != lstProductChildTypes.size()-1)
        {
            sbBuffer = sbBuffer.append(COMMA);
        }
    }

    //Create a domainobject and get the products in the product structure
    DomainObject dom = newInstance(context, strObjectId);
    /*List allObjList = dom.getRelatedObjects(
                            context,
                            strRelationshipType,
                            DomainConstants.QUERY_WILDCARD,
                            false,
                            true,
                            (short) 0,
                            (StringList)lstobjectSelects,
                            null,
                            DomainConstants.EMPTY_STRING,
                            DomainConstants.EMPTY_STRING,
                            DomainConstants.EMPTY_STRING,
                            sbBuffer.toString(),
                            null);*/
    
    List allObjList = dom.getRelatedObjects(context,
              strRelationshipType, // relationshipPattern
			  DomainConstants.QUERY_WILDCARD, // typePattern
			  (StringList)lstobjectSelects, // objectSelects
			  null, // relationshipSelects
			  false, // getTo
			  true, // getFrom
			  (short) 0, // recurseToLevel
			  null, // objectWhere,
			  null, // relationshipWhere
			  (int)0); // limit
    
    //context product id is added into the maplist.
    mapTemp.put(DomainConstants.SELECT_ID, strObjectId);
    allObjList.add(mapTemp);

    StringBuffer stbBuffer = new StringBuffer(200);

    // selecting even the Comparison Operator and Compatibility Option as we need it in the
    // column JPO method getCompatibilityOption, for forming the Compatibility Column.
    stbBuffer = stbBuffer.append(STR_ATTRIBUTE)
                .append(OPEN_BRACE)
                .append(ConfigurationConstants.ATTRIBUTE_COMPARISION_OPERATOR)
                .append(CLOSE_BRACE);

    List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
    lstObjectSelects.add(stbBuffer.toString());

    stbBuffer.delete(0,stbBuffer.length());
    stbBuffer = stbBuffer.append(STR_ATTRIBUTE)
                .append(OPEN_BRACE)
                .append(ConfigurationConstants.ATTRIBUTE_COMPATIBILITY_OPTION)
                .append(CLOSE_BRACE);
    lstObjectSelects.add(stbBuffer.toString());

    StringBuffer stbTemp = new StringBuffer(50);
    stbTemp = stbTemp.append(ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION)
                     .append(COMMA)
                     .append(ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);

    lstObjectSelects.add(STR_ATTRIBUTE+OPEN_BRACE+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+CLOSE_BRACE);

    lstObjectSelects.add(STR_ATTRIBUTE+OPEN_BRACE+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+CLOSE_BRACE);

    //for fetching all the Product Compatibility Rules connected to each of the Product
    //in Product structure of the context product.
    DomainObject domObject = null;
    for (int k=0; k<allObjList.size() ;k++ )
    {
        mapTemp = (Map)allObjList.get(k);
        strProdId = (String)mapTemp.get(SELECT_ID);

        domObject = newInstance(context, strProdId);
        /*mapBusIds =
                domObject.getRelatedObjects(
                    context,
                    stbTemp.toString(),
                    ConfigurationConstants.TYPE_PRODUCT_COMPATIBILITY_RULE,
                    (StringList)lstObjectSelects,
                    DomainConstants.EMPTY_STRINGLIST,
                    true,
                    false,
                    (short) 1,
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING);*/

			mapBusIds = domObject.getRelatedObjects(context,
					stbTemp.toString(), // relationshipPattern
					ConfigurationConstants.TYPE_PRODUCT_COMPATIBILITY_RULE, // typePattern
					(StringList)lstObjectSelects, // objectSelects
					null, // relationshipSelects
					true, // getTo
					false, // getFrom
					(short)1, // recurseToLevel
					null, // objectWhere,
					null, // relationshipWhere
					(int)0); // limit

         for (int j=0; j<mapBusIds.size(); j++)
         {
             mapTemp = (Map)mapBusIds.get(j);
             strRuleId = (String)mapTemp.get(DomainConstants.SELECT_ID);
             //to avoid duplicate Rules
            if (! (containsId(lstRuleList, strRuleId)))
            {
                lstRuleList.add(mapTemp);
            }

         }

    }
    return (MapList)lstRuleList;
}

	/**
	 * Method to validate and display the validation result of the Product Compatibility Rules
	 * connected to the product.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - HashMap containing one String entry for key "objectId"
	 * @return List containing the id of Boolean Compatibility Rule objects
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public List getProductValidationResult(
			Context context,
			String[] args)
	throws Exception
	{
		//XSSOK- retrieved from Properties files
		//List containing the final result
		List lstFinalResult = new StringList();

		String strMessage = DomainConstants.EMPTY_STRING;
		String strLanguage = context.getSession().getLanguage();
		//Unpack the arguments and get the object id
		Map mapProductCompatibility = (HashMap) JPO.unpackArgs(args);

		//Begin of Add by RashmiL_Joshi, Enovia MatrixOne for Bug# 299871 Date: 3/8/2005
		String strReportFormat = (String)(
				(Map) mapProductCompatibility.get("paramList")).get("reportFormat");
		//End of Add for Bug# 299871
		String strMode = (String)(
				(Map) mapProductCompatibility.get("paramList")).get("mode");
		String strObjectId = DomainConstants.EMPTY_STRING;
		if((strMode != null) && !("".equals(strMode)) && !("null".equals(strMode)))
		{
			if(strMode.equals("ListPage"))
			{
				strObjectId = (String)(
						(Map) mapProductCompatibility.get("paramList")).get("emxTableRowId");
			}
		}
		else
		{
			strObjectId = (String)(
					(Map) mapProductCompatibility.get("paramList")).get("objectId");
		}

		//String is initialized to store the value of relationship name
		StringBuffer sbBuffer = new StringBuffer(200);
		/*       sbBuffer.append(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO);
    sbBuffer.append(COMMA);
    sbBuffer.append(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM);
    String strRelationshipType = sbBuffer.toString();  -- changing for R212 schema*/
		String strRelationshipType = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;

		//List is initialized to store the value of id
		StringList lstobjectSelects = new StringList();
		lstobjectSelects.add(SELECT_ID);

		//get the children types of Product and make it a comma seperated string
		List lstProductChildTypes = ProductLineUtil.getChildrenTypes(
				context,
				ConfigurationConstants.TYPE_PRODUCTS);
		sbBuffer.delete(0,sbBuffer.length());
		for (int i=0; i < lstProductChildTypes.size(); i++)
		{
			sbBuffer = sbBuffer.append(lstProductChildTypes.get(i));
			if (i != lstProductChildTypes.size()-1)
			{
				sbBuffer = sbBuffer.append(COMMA);
			}
		}

		//Create a domainobject and get the products in the product structure
		/*    DomainObject dom = newInstance(context, strObjectId);
    List allObjList = dom.getRelatedObjects(
        context,
        strRelationshipType,
        DomainConstants.QUERY_WILDCARD,
        false,
        true,
        (short) 0,
        (StringList)lstobjectSelects,
        null,
        DomainConstants.EMPTY_STRING,
        DomainConstants.EMPTY_STRING,
        DomainConstants.EMPTY_STRING,
        sbBuffer.toString(),
        null);
		 */
		int iLevel = 0;
		int limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxConfiguration.ExpandLimit"));
		String filterExpression = (String) mapProductCompatibility.get("CFFExpressionFilterInput_OID");

		ConfigurationUtil confUtil = new ConfigurationUtil(strObjectId);
		List allObjList = confUtil.getObjectStructure(context, sbBuffer.toString(), strRelationshipType,
				lstobjectSelects, new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID), false, true, iLevel, limit,
				DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM, filterExpression);

		//get the Rule ids
		List lstObjectIdsList = (MapList) mapProductCompatibility.get("objectList");
		int iNumOfObjects = lstObjectIdsList.size();
		String arrstrObjId[] = new String[iNumOfObjects];

		//initialise strings to attributes comparison operator/compatibility option
		sbBuffer.delete(0,sbBuffer.length());
		sbBuffer = sbBuffer.append(STR_ATTRIBUTE)
		.append(OPEN_BRACE)
		.append(ConfigurationConstants.ATTRIBUTE_COMPARISION_OPERATOR)
		.append(CLOSE_BRACE);
		String strCompOperatorkey = sbBuffer.toString();

		sbBuffer.delete(0,sbBuffer.length());
		sbBuffer = sbBuffer.append(STR_ATTRIBUTE)
		.append(OPEN_BRACE)
		.append(ConfigurationConstants.ATTRIBUTE_COMPATIBILITY_OPTION)
		.append(CLOSE_BRACE);
		String strCompOptionkey = sbBuffer.toString();

		//initialise strings to hold comparison operator, compatibility option,
		//left expression, right epression and their object ids.
		List lstCompOperator = new StringList();
		List lstCompOption = new StringList();

		String strRuleDisplay = PersonUtil.getRuleDisplay(context);
		// this check is required because the property RuleDisplay may have been not set for the user initially and
		// hence it could be null, so we can take the default value of FULLNAME in that case
		if((strRuleDisplay == null) ||
				(strRuleDisplay.equals("null")) ||
				(strRuleDisplay.equals("")) ||
				(strRuleDisplay.equals(RULE_DISPLAY_FULL_NAME))) {
			strRuleDisplay = RULE_DISPLAY_FULL_NAME;
		}

		// hard coded for FULLNAME because it is used for finding the Object ID
		List lstLeftExp = getLeftExpression(context, args,RULE_DISPLAY_FULL_NAME);
		List lstRightExp = getRightExpression(context, args,RULE_DISPLAY_FULL_NAME);
		List lstLeftExpIds = new StringList();
		List lstRightExpIds = new StringList();

		boolean bIsPresent = false;
		Object obj;
		String strMsg = DomainConstants.EMPTY_STRING;
		String strMsgInComp = DomainConstants.EMPTY_STRING;

		//Getting the bus ids for objects in the table
		for (int i = 0; i < iNumOfObjects; i++)
		{
			StringList lstFailedTNR= new StringList();
			obj = lstObjectIdsList.get(i);
			//get the object id, comparison operator and compatibility option
			if (obj instanceof HashMap)
			{
				arrstrObjId[i] = (String)(
						(HashMap)lstObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
				lstCompOperator.add((String)(
						(HashMap)lstObjectIdsList.get(i)).get(strCompOperatorkey));
				lstCompOption.add((String)(
						(HashMap)lstObjectIdsList.get(i)).get(strCompOptionkey));
			}
			else if (obj instanceof Hashtable)
			{
				arrstrObjId[i] = (String)(
						(Hashtable)lstObjectIdsList.get(i)).get(DomainConstants.SELECT_ID);
				lstCompOperator.add((String)(
						(Hashtable)lstObjectIdsList.get(i)).get(strCompOperatorkey));
				lstCompOption.add((String)(
						(Hashtable)lstObjectIdsList.get(i)).get(strCompOptionkey));
			}

			//get the left expression and right expression object ids
			lstLeftExpIds = ConfigurableRulesUtil.getObjectIdsFromExpression(
					context,
					(String)lstLeftExp.get(i));
			lstRightExpIds = ConfigurableRulesUtil.getObjectIdsFromExpression(
					context,
					(String)lstRightExp.get(i));

			//processing for requires operator
			if (lstCompOperator.get(i).equals(
					ConfigurationConstants.RANGE_VALUE_REQUIRES))
			{
				//Right expression contains context object
				if (containsIds(lstLeftExpIds,allObjList) || lstLeftExpIds.contains(strObjectId))
				{
					bIsPresent = false;
					strMsg = DomainConstants.EMPTY_STRING;
					//check whether all the products in RE are present
					//in the product structure
					for (int j=0 ; j < lstRightExpIds.size() ; j++)
					{
						if (containsId(allObjList, lstRightExpIds.get(j)))
						{
							bIsPresent = true;
						}
						else
						{
							if(strRuleDisplay.equals(ConfigurationConstants.RULE_DISPLAY_FULL_NAME)) {
								strMsg = strMsg + processIdforFullName(context, (String)lstRightExpIds.get(j));
							}
							else {
								strMsg = strMsg + processIdforMarketingName(context, (String)lstRightExpIds.get(j));
							}
						}
					}
					//If not add the reason for failure
					if (bIsPresent == false)
					{
						strMessage = EnoviaResourceBundle.getProperty(context,
								SUITE_KEY,
		                        "emxProduct.Rules.Message.Missing",
								strLanguage);
						lstFinalResult.add(strMessage + removeDuplicate(context, strMsg));
					}
				}
			}

			//processing for codependent operator
			if (lstCompOperator.get(i).equals(
					ConfigurationConstants.RANGE_VALUE_CODEPENDENT))
			{
				//Left expression contains context object
				if (containsIds(lstLeftExpIds,allObjList) || lstLeftExpIds.contains(strObjectId))
				{
					bIsPresent = false;
					strMsg = DomainConstants.EMPTY_STRING;
					//check whether all the products in RE are present
					//in the product structure
					for (int j=0 ; j < lstRightExpIds.size() ; j++)
					{
						if (containsId(allObjList,lstRightExpIds.get(j)))
						{
							bIsPresent = true;
						}
						else
						{
							if(strRuleDisplay.equals(ConfigurationConstants.RULE_DISPLAY_FULL_NAME)) {
								strMsg = strMsg + processIdforFullName(context, (String)lstRightExpIds.get(j));
							}
							else {
								strMsg = strMsg + processIdforMarketingName(context, (String)lstRightExpIds.get(j));
							}
						}
					}
					//If not add the reason for failure
					if (bIsPresent == false)
					{
						strMessage = EnoviaResourceBundle.getProperty(context,
								SUITE_KEY,
								"emxProduct.Rules.Message.Missing",
								strLanguage);
						lstFinalResult.add(strMessage + removeDuplicate(context, strMsg));
					}
				}
				//Right expression contains context object
				//Modified by Praveen, Enovia MatrixOne for Bug #300587 dated 03/14/2005
				else if (containsIds(lstRightExpIds,allObjList) || lstRightExpIds.contains(strObjectId))
				{
					bIsPresent = false;
					strMsg = DomainConstants.EMPTY_STRING;
					//check whether all the products in LE are present
					//in the product structure
					for (int j=0 ; j < lstLeftExpIds.size() ; j++)
					{
						if (containsId(allObjList, lstLeftExpIds.get(j)))
						{
							bIsPresent = true;
						}
						else
						{
							if(strRuleDisplay.equals(ConfigurationConstants.RULE_DISPLAY_FULL_NAME)) {
								strMsg = strMsg + processIdforFullName(context, (String)lstLeftExpIds.get(j));
							}
							else {
								strMsg = strMsg + processIdforMarketingName(context, (String)lstLeftExpIds.get(j));
							}
						}
					}
					//If not add the reason for failure
					if (bIsPresent == false)
					{
						strMessage = EnoviaResourceBundle.getProperty(context,
								SUITE_KEY,
								"emxProduct.Rules.Message.Missing",
								strLanguage);
						lstFinalResult.add(strMessage + removeDuplicate(context, strMsg));
					}
				}
			}
			//processing for compatible/incompatible operator
			if (lstCompOperator.get(i).equals(
					ConfigurationConstants.RANGE_VALUE_COMPATIBLE) ||
					lstCompOperator.get(i).equals(
							ConfigurationConstants.RANGE_VALUE_INCOMPATIBLE))
			{
				//Left expression contains context object
				if (containsIds(lstLeftExpIds,allObjList) || lstLeftExpIds.contains(strObjectId))
				{
					boolean bIsLowerRevPresent;
					//processing for upward option
					if (lstCompOption.get(i).equals(
							ConfigurationConstants.RANGE_VALUE_UPWARD))
					{
						bIsPresent = false;
						bIsLowerRevPresent = false;
						List lstRev = new StringList();
						//check whether higher revisions of the products in RE are
						//present in the product structure
						for (int j=0; j < lstRightExpIds.size() ; j++)
						{
							lstRev = getRevisionIndex(
									context,
									(String)lstRightExpIds.get(j),
									allObjList);
							if (isHigherRevPresentInList(
									context,
									(String)lstRightExpIds.get(j),
									lstRev, lstFailedTNR))
							{
								bIsPresent = true;
							}
						}
						//add the reason for failure
						if (bIsPresent == true &&
								lstCompOperator.get(i).equals(
										ConfigurationConstants.RANGE_VALUE_INCOMPATIBLE))
						{
							strMessage = EnoviaResourceBundle.getProperty(context,
									SUITE_KEY,
									"emxProduct.Rules.Message.HigherRevPresent",
									strLanguage);
							lstFinalResult.add(strMessage + removeDuplicate(context, strFailedTNR));
						}
						strFailedTNR = DomainConstants.EMPTY_STRING;
						lstFailedTNR.clear();
						//check whether lower revisions of the products in RE are
						//present in the product structure
						for (int j=0; j < lstRightExpIds.size() ; j++)
						{
							lstRev = getRevisionIndex(
									context,
									(String)lstRightExpIds.get(j),
									allObjList);
							if (isLowerRevPresentInList(
									context,
									(String)lstRightExpIds.get(j),
									lstRev,lstFailedTNR))
							{
								bIsLowerRevPresent = true;
							}
						}
						//add the reason for failure
						if (bIsLowerRevPresent == true &&
								lstCompOperator.get(i).equals(
										ConfigurationConstants.RANGE_VALUE_COMPATIBLE))
						{
							//check if any of the failed objects are present in the expression
							//if so remove it from the failed objects.
							String strPresent = DomainConstants.EMPTY_STRING;
							for (int m=0; m < lstFailedTNR.size(); m++)
							{
								if (lstRightExpIds.contains((String)lstFailedTNR.get(m)))
								{
									if(strRuleDisplay.equals(ConfigurationConstants.RULE_DISPLAY_FULL_NAME)) {
										strPresent = strMsg + processIdforFullName(context, (String)lstFailedTNR.get(m));
									}
									else {
										strPresent = strMsg + processIdforMarketingName(context, (String)lstFailedTNR.get(m));
									}
									strFailedTNR = FrameworkUtil.findAndReplace(
											strFailedTNR,
											strPresent,
											DomainConstants.EMPTY_STRING);
								}
							}
							if (!strFailedTNR.equals(""))
							{
								strMessage = EnoviaResourceBundle.getProperty(context,
										SUITE_KEY,
										"emxProduct.Rules.Message.LowerRevPresent",
										strLanguage);
								lstFinalResult.add(strMessage + removeDuplicate(context, strFailedTNR));
							}
						}
						strFailedTNR = DomainConstants.EMPTY_STRING;
						lstFailedTNR.clear();
					}
					//processing for downward option
					if (lstCompOption.get(i).equals(
							ConfigurationConstants.RANGE_VALUE_DOWNWARD))
					{
						bIsPresent = false;
						bIsLowerRevPresent = false;
						List lstRev = new StringList();
						//check whether lower revisions of the products in RE are
						//present in the product structure
						for (int j=0; j < lstRightExpIds.size() ; j++)
						{
							lstRev = getRevisionIndex(
									context,
									(String)lstRightExpIds.get(j),
									allObjList);
							if (isHigherRevPresentInList(
									context,
									(String)lstRightExpIds.get(j),
									lstRev,lstFailedTNR))
							{
								bIsPresent = true;
							}
						}
						//add the reason for failure
						if (bIsPresent == true &&
								lstCompOperator.get(i).equals(
										ConfigurationConstants.RANGE_VALUE_COMPATIBLE))
						{
							//check if any of the failed objects are present in the expression
							//if so remove it from the failed objects.
							String strPresent = DomainConstants.EMPTY_STRING;
							for (int m=0; m < lstFailedTNR.size(); m++)
							{
								if (lstRightExpIds.contains((String)lstFailedTNR.get(m)))
								{
									if(strRuleDisplay.equals(ConfigurationConstants.RULE_DISPLAY_FULL_NAME)) {
										strPresent = processIdforFullName(context, (String)lstFailedTNR.get(m));
									}
									else {
										strPresent = processIdforMarketingName(context, (String)lstFailedTNR.get(m));
									}
									strFailedTNR = FrameworkUtil.findAndReplace(
											strFailedTNR,
											strPresent,
											DomainConstants.EMPTY_STRING);
								}
							}
							if (!strFailedTNR.equals(""))
							{
								strMessage = EnoviaResourceBundle.getProperty(context,
										SUITE_KEY,
										"emxProduct.Rules.Message.HigherRevPresent",
										strLanguage);
								lstFinalResult.add(strMessage + removeDuplicate(context, strFailedTNR));
							}
						}
						strFailedTNR = DomainConstants.EMPTY_STRING;
						lstFailedTNR.clear();
						//check whether lower revisions of the products in RE are
						//present in the product structure
						for (int j=0; j < lstRightExpIds.size() ; j++)
						{
							lstRev = getRevisionIndex(
									context,
									(String)lstRightExpIds.get(j),
									allObjList);
							if (isLowerRevPresentInList(
									context,
									(String)lstRightExpIds.get(j),
									lstRev,lstFailedTNR))
							{
								bIsLowerRevPresent = true;
							}
						}
						//add the reason for failure
						if (bIsLowerRevPresent == true &&
								lstCompOperator.get(i).equals(
										ConfigurationConstants.RANGE_VALUE_INCOMPATIBLE))
						{
							strMessage = EnoviaResourceBundle.getProperty(context,
									SUITE_KEY,
									"emxProduct.Rules.Message.LowerRevPresent",
									strLanguage);
							lstFinalResult.add(strMessage + removeDuplicate(context, strFailedTNR));
						}
						strFailedTNR = DomainConstants.EMPTY_STRING;
						lstFailedTNR.clear();
					}
					//processing for None option
					if (lstCompOption.get(i).equals(
							ConfigurationConstants.RANGE_VALUE_NONE))
					{
						bIsPresent = true;
						boolean bInComp = false;
						strMsg = DomainConstants.EMPTY_STRING;
						strMsgInComp = DomainConstants.EMPTY_STRING;
						for (int j=0; j < lstRightExpIds.size() ; j++)
						{
							//get the list of all revisions of the products in RE that are
							//present in the product structure as well
							List lstRev = getRevisionIndex(
									context,
									(String)lstRightExpIds.get(j),
									allObjList);
							if (containsId(lstRev, (String)lstRightExpIds.get(j)))
							{
								bInComp = true;

								if(strRuleDisplay.equals(ConfigurationConstants.RULE_DISPLAY_FULL_NAME)) {
									strMsgInComp = strMsgInComp + processIdforFullName(context, (String)lstRightExpIds.get(j));
								}
								else {
									strMsgInComp = strMsgInComp + processIdforMarketingName(context, (String)lstRightExpIds.get(j));
								}
							}
							if (lstRev.size() > 1)
							{
								bIsPresent = false;
								for (int k=0 ; k < lstRev.size() ; k++)
								{
									if ((((String)lstRightExpIds.get(j)).equals(
											((String)((Map)lstRev.get(k)).get(
													DomainConstants.SELECT_ID)))))
										lstRev.remove(k);
								}
								strMsg = strMsg + processList(context, lstRev);
							}
							else if (lstRev.size()==1)
							{
								if (!(((String)lstRightExpIds.get(j)).equals(
										((String)((Map)lstRev.get(0)).get(
												DomainConstants.SELECT_ID)))))
								{
									bIsPresent = false;
									if(strRuleDisplay.equals(ConfigurationConstants.RULE_DISPLAY_FULL_NAME)) {
										strMsg = strMsg + processIdforFullName(context, (String)((Map)lstRev.get(0)).get(DomainConstants.SELECT_ID));
									}
									else {
										strMsg = strMsg + processIdforMarketingName(context, (String)((Map)lstRev.get(0)).get(DomainConstants.SELECT_ID));
									}
								}
							}

						}
						//add the reason for failure
						if (bIsPresent == false &&
								lstCompOperator.get(i).equals(
										ConfigurationConstants.RANGE_VALUE_COMPATIBLE))
						{
							strMessage = EnoviaResourceBundle.getProperty(context,
									SUITE_KEY,
									"emxProduct.Rules.Message.SpecificCompatible",
									strLanguage);
							lstFinalResult.add(strMessage + removeDuplicate(context, strMsg));
						}
						else if (bInComp == true &&
								lstCompOperator.get(i).equals(
										ConfigurationConstants.RANGE_VALUE_INCOMPATIBLE))
						{
							strMessage = EnoviaResourceBundle.getProperty(context,
									SUITE_KEY,
									"emxProduct.Rules.Message.SpecificIncompatible",
									strLanguage);
							lstFinalResult.add(strMessage + removeDuplicate(context, strMsgInComp));
						}
					}
					//processing for all option
					if (lstCompOption.get(i).equals(ConfigurationConstants.RANGE_VALUE_ALL))
					{
						bIsPresent = true;
						List lstRev = new MapList();
						strMsg = DomainConstants.EMPTY_STRING;
						for (int j=0; j < lstRightExpIds.size() ; j++)
						{
							//get the list of all revisions of the products in RE that are
							//present in the product structure as well
							lstRev = getRevisionIndex(
									context,
									(String)lstRightExpIds.get(j),
									allObjList);
							if (lstRev.size() > 0)
							{
								bIsPresent = false;
								strMsg = strMsg + processList(context, lstRev);
							}
						}
						//add the reason for failure
						if (bIsPresent == false &&
								lstCompOperator.get(i).equals(
										ConfigurationConstants.RANGE_VALUE_INCOMPATIBLE))
						{
							strMessage = EnoviaResourceBundle.getProperty(context,
									SUITE_KEY,
									"emxProduct.Rules.Message.AllIncompatible",
									strLanguage);
							lstFinalResult.add(strMessage + removeDuplicate(context, strMsg));
						}
					}
				}
			}
			//If rule passes add Pass to the final list
			if (lstFinalResult.size() == i)
			{
				strMessage = EnoviaResourceBundle.getProperty(context,
						SUITE_KEY,
						"emxProduct.Rules.Message.Pass",
						strLanguage);
				lstFinalResult.add(strMessage);
			}
		}
		//Begin of Add by RashmiL_Joshi, Enovia MatrixOne for Bug# 299871 Date: 3/8/2005
		if(strReportFormat!=null
				&&!strReportFormat.equals("null")
				&&!strReportFormat
				.equals(DomainConstants.EMPTY_STRING)){
			//Begin of Add by RashmiL_Joshi, Enovia MatrixOne for Bug# 299871 Date: 4/14/2005
			List lstReportList = new StringList();
			for (int k=0; k<lstFinalResult.size(); k++)
			{
				lstReportList.add(FrameworkUtil.findAndReplace((String)lstFinalResult.get(k), "<BR>", "\n"));
			}
			return lstReportList;
			//End of add for Bug# 299871
		}
		//End of Add for Bug# 299871
		return lstFinalResult;
	}

	/**
	 * Method to reframe the Left Expression for the Rule object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - HashMap containing one String entry for key "objectId"
          1 - String containing the typeName which is used to find out whether the rule is a BCR.
          2 - String containing the user preference for Rule Display (Full Name/ Marketing Name)
	 * @return List containing the Left Expressions for all the Rules loaded in the table.
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 * @grade 0
	 */

	public List getLeftExpression(Context context, String[] args, String strRuleDisplay) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strIsIntermediate= (String)((Map) programMap.get("paramList")).get("intermediate");
		List lstLeftExpression = new StringList();
		if (strIsIntermediate !=null && !("".equals(strIsIntermediate))
				&& !("null".equals(strIsIntermediate)) && strIsIntermediate.equals("true"))
		{
			lstLeftExpression = getExpression(
					context,
					args,
					ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION,
					strRuleDisplay);
		}
		else
		{
			lstLeftExpression = getProductRuleExpression(
					context,
					args,
					ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION,
					strRuleDisplay);
		}
		return lstLeftExpression;
	}

	/**
	 * Method to check whether a list of elements are present in a MapList.
	 *
	 * @param lstIdsTobeSearched holds the ids.
	 * @param lstIds holds the string id
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public boolean containsIds(List lstIdsTobeSearched, List lstIds)throws Exception
	{
		boolean bPresent = false;
		for (int i = 0; i < lstIdsTobeSearched.size() ; i++)
		{
			if (containsId(lstIds,lstIdsTobeSearched.get(i)))
			{
				bPresent = true;
				break;
			}
		}
		return bPresent;
	}

	/**
	 * Method to return a string in the form of T::N::R from an Object id.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param strId holds the id
	 * @return String in the form of T::N::R
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public String processIdforFullName(Context context, String strId) throws Exception
	{
		Map mapInfo;
		List lstobjectSelects = new StringList();
		lstobjectSelects.add(SELECT_REVISION);
		lstobjectSelects.add(SELECT_NAME);
		lstobjectSelects.add(SELECT_TYPE);
		StringBuffer sbTemp = new StringBuffer();
		DomainObject domObj = newInstance(
				context,
				strId);
		mapInfo = domObj.getInfo(context, (StringList)lstobjectSelects);
		sbTemp = sbTemp.append(STR_NEWLINE)
		.append((String)mapInfo.get(DomainConstants.SELECT_TYPE))
		.append(FEATURE_TNR_DELIMITER)
		.append((String)mapInfo.get(DomainConstants.SELECT_NAME))
		.append(FEATURE_TNR_DELIMITER)
		.append((String)mapInfo.get(DomainConstants.SELECT_REVISION));
		return sbTemp.toString();
	}

	/**
	 * Method to return a string in the <Marketing Name> from an Object id.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param strId holds the id
	 * @return String in the form of T::N::R
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public String processIdforMarketingName(Context context, String strId) throws Exception
	{
		String strMarketingName = "";
		StringBuffer sbTemp = new StringBuffer();
		DomainObject domObj = newInstance(
				context,
				strId);
		strMarketingName = domObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_MARKETING_NAME);
		sbTemp = sbTemp.append(STR_NEWLINE)
		.append(strMarketingName);
		return sbTemp.toString();
	}

	/**
	 * Method to remove duplicate TNRs from a string.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param strElement holds string to be checked for duplicates.
	 * @return String with duplicate TNR removed.
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	private String removeDuplicate(
			Context context,
			String strElement) throws Exception
			{
		List lstTemp = new StringList();
		StringTokenizer strTok = new StringTokenizer(strElement, STR_NEWLINE);
		StringBuffer stbTemp =  new StringBuffer();
		String strTemp = DomainConstants.EMPTY_STRING;
		while (strTok.hasMoreTokens())
		{
			strTemp = strTok.nextToken();
			if (! lstTemp.contains(strTemp))
			{
				lstTemp.add(strTemp);
				stbTemp.append("<BR>")
				.append(strTemp).append("</BR>");
			}
		}
		return stbTemp.toString();
			}

	/**
	 * Method to get the Revision Index of the revisions of the Product.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param strElement holds the Product id
	 * @param lstTobeSeached holds the Product ids of all
	 *                  the products under the context product
	 * @return List containing Revision index of the Product
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public List getRevisionIndex(
			Context context,
			String strElement,
			List lstTobeSeached) throws Exception
			{
		//initialise maps
		Map mapTemp;
		Map mapInfo;
		List lstIdFound = new MapList();
		//get all the revisions of the product
		DomainObject domObj = newInstance(context, strElement);
		StringList singleValueSelects= new StringList(DomainObject.SELECT_ID);
		StringList multiValueSelects= new StringList();
		List lstTemp = domObj.getRevisionsInfo(context,
				singleValueSelects,
				multiValueSelects);
		//the getRevisionsInfo method returns revisions in sequence.
		//So depending on the position of a rev in the list we can find out which one
		//is higher/lower revision. So add the position to the list and return.
		for (int i =0; i < lstTemp.size(); i++ )
		{
			mapTemp = (Map)lstTemp.get(i);
			for (int j=0; j < lstTobeSeached.size() ; j++)
			{
				if (((String)((Map)lstTobeSeached.get(j)).get(
						DomainConstants.SELECT_ID)).equals(
								(String)mapTemp.get(DomainConstants.SELECT_ID)))
				{
					//add the position to the map
					mapInfo = (Map)lstTobeSeached.get(j);
					mapInfo.put(STR_INDEX, new Integer(i));
					lstIdFound.add(mapInfo);
				}
			}
		}
		return lstIdFound;
			}

	/**
	 * Method to check whether a higher revision of the product is present
	 * in the given List.
	 *
	 * @param strElement holds the Product id
	 * @param lstTobeSeached holds the list of Product ids.
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public boolean isHigherRevPresentInList (
			Context context,
			String strElement,
			List lstTobeSeached, List lstFailedTNR) throws Exception
			{
		boolean bPresent = isRevPresentInList(
				context,
				strElement,
				lstTobeSeached,
				true, lstFailedTNR);
		return bPresent;
			}

	/**
	 * Method to check whether a higher or lower revision of the product is present
	 * in the given List.
	 *
	 * @param strElement holds the Product id
	 * @param lstTobeSeached holds the list of Product ids.
	 * @param bHigher determines whether to look for higher or lower revisions
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public boolean isRevPresentInList (
			Context context,
			String strElement,
			List lstTobeSeached,
			boolean bHigher , List lstFailedTNR ) throws Exception
			{
		boolean bPresent = false;
		//if list is null return false
		if (lstTobeSeached.size() == 0)
		{
			return bPresent;
		}
		//initialise two position strings
		String strPos1 = DomainConstants.EMPTY_STRING;
		String strPos2 = DomainConstants.EMPTY_STRING;
		//get the position of the element from the list
		for (int i=0; i < lstTobeSeached.size() ; i++)
		{
			if (strElement.equals(
					(String)((Map)lstTobeSeached.get(i)).get(DomainConstants.SELECT_ID)))
			{
				strPos1 = ((Map)lstTobeSeached.get(i)).get(STR_INDEX).toString();
				break;
			}
		}
		if (strPos1.equals(DomainConstants.EMPTY_STRING))
		{
			DomainObject domObj = newInstance(context, strElement);
			StringList singleValueSelects= new StringList(DomainObject.SELECT_ID);
			StringList multiValueSelects= new StringList();
			List lstTemp = domObj.getRevisionsInfo(context,
					singleValueSelects,
					multiValueSelects);
			Map mapTemp;
			for (int i =0; i < lstTemp.size(); i++ )
			{
				mapTemp = (Map)lstTemp.get(i);
				if (strElement.equals((String)mapTemp.get(DomainConstants.SELECT_ID)))
				{
					strPos1 = (new Integer(i)).toString();
				}
			}
		}
		StringBuffer sbTemp = new StringBuffer();
		List lstobjectSelects = new StringList();
		lstobjectSelects.add(SELECT_REVISION);
		lstobjectSelects.add(SELECT_NAME);
		lstobjectSelects.add(SELECT_TYPE);
		Map mapInfo;
		int iFound = 0;
		//check if there is higher/lower revision; if so return true
		for (int i=0; i < lstTobeSeached.size() ; i++)
		{
			strPos2 = ((Map)lstTobeSeached.get(i)).get(STR_INDEX).toString();
			if (bHigher)
			{
				//Modified by Praveen, Enovia MatrixOne for Bug #300587 dated 03/14/2005
				if (Integer.parseInt(strPos1) <= Integer.parseInt(strPos2))
				{
					DomainObject domObj = newInstance(
							context,
							(String)((Map)lstTobeSeached.get(i)).get(DomainConstants.SELECT_ID));
					mapInfo = domObj.getInfo(context, (StringList)lstobjectSelects);
					sbTemp = sbTemp.append(STR_NEWLINE)
					.append((String)mapInfo.get(DomainConstants.SELECT_TYPE))
					.append(FEATURE_TNR_DELIMITER)
					.append((String)mapInfo.get(DomainConstants.SELECT_NAME))
					.append(FEATURE_TNR_DELIMITER)
					.append((String)mapInfo.get(DomainConstants.SELECT_REVISION));
					if (iFound == 0)
					{
						strFailedTNR = strFailedTNR + sbTemp.toString();
						lstFailedTNR.add((String)((Map)lstTobeSeached.get(i)).get(DomainConstants.SELECT_ID));
					}
					else
					{
						strFailedTNR = sbTemp.toString();
						lstFailedTNR.add((String)((Map)lstTobeSeached.get(i)).get(DomainConstants.SELECT_ID));
					}
					iFound ++;
					bPresent = true;
				}
			}
			else
			{
				//Modified by Praveen, Enovia MatrixOne for Bug #300587 dated 03/14/2005
				if (Integer.parseInt(strPos1) >= Integer.parseInt(strPos2))
				{
					DomainObject domObj = newInstance(
							context,
							(String)((Map)lstTobeSeached.get(i)).get(DomainConstants.SELECT_ID));
					mapInfo = domObj.getInfo(context, (StringList)lstobjectSelects);
					sbTemp = sbTemp.append(STR_NEWLINE)
					.append((String)mapInfo.get(DomainConstants.SELECT_TYPE))
					.append(FEATURE_TNR_DELIMITER)
					.append((String)mapInfo.get(DomainConstants.SELECT_NAME))
					.append(FEATURE_TNR_DELIMITER)
					.append((String)mapInfo.get(DomainConstants.SELECT_REVISION));
					if (iFound == 0)
					{
						strFailedTNR = strFailedTNR + sbTemp.toString();
						lstFailedTNR.add((String)((Map)lstTobeSeached.get(i)).get(DomainConstants.SELECT_ID));
					}
					else
					{
						strFailedTNR = sbTemp.toString();
						lstFailedTNR.add((String)((Map)lstTobeSeached.get(i)).get(DomainConstants.SELECT_ID));
					}
					iFound ++;
					bPresent = true;
				}
			}
		}
		return bPresent;
			}

	/**
	 * Method to check whether a lower revision of the product is present
	 * in the given List.
	 *
	 * @param strElement holds the Product id
	 * @param lstTobeSeached holds the list of Product ids.
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public boolean isLowerRevPresentInList (
			Context context,
			String strElement,
			List lstTobeSeached, List lstFailedTNR) throws Exception
			{
		boolean bPresent = isRevPresentInList(
				context,
				strElement,
				lstTobeSeached,
				false, lstFailedTNR);
		return bPresent;
			}

	/**
	 * Method to process a List containing object ids so as to return a string
	 * in the form of T::N::R.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param lstRev holds the Product ids
	 * @return String in the form of T::N::R
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public String processList(Context context, List lstRev) throws Exception
	{
		Map mapTemp;
		Map mapInfo;
		List lstobjectSelects = new StringList();
		lstobjectSelects.add(SELECT_REVISION);
		lstobjectSelects.add(SELECT_NAME);
		lstobjectSelects.add(SELECT_TYPE);
		String strTemp = DomainConstants.EMPTY_STRING;
		StringBuffer sbTemp = new StringBuffer();
		for (int i=0 ; i < lstRev.size() ; i++)
		{
			mapTemp = (Map)lstRev.get(i);
			strTemp = (String)mapTemp.get(DomainConstants.SELECT_ID);
			DomainObject domObj = newInstance(
					context,
					strTemp);
			mapInfo = domObj.getInfo(context, (StringList)lstobjectSelects);
			sbTemp = sbTemp.append(STR_NEWLINE)
			.append((String)mapInfo.get(DomainConstants.SELECT_TYPE))
			.append(FEATURE_TNR_DELIMITER)
			.append((String)mapInfo.get(DomainConstants.SELECT_NAME))
			.append(FEATURE_TNR_DELIMITER)
			.append((String)mapInfo.get(DomainConstants.SELECT_REVISION));
		}
		return sbTemp.toString();
	}

	/**
	 * Method to form the Compatibility Option String to be displayed in the ProductCompatibility Table.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - HashMap containing one String entry for key "objectId"
	 * @return List containing the Compatibility Option string
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.6
	 */

	public List getCompatibilityOption(Context context, String[] args) throws Exception
	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList pcrObjectIdsList = (MapList) programMap.get("objectList");
		//Get the number of objects in objectList
		int iNumOfObjects = pcrObjectIdsList.size();

		List lstCmpOption = new StringList(iNumOfObjects);

		StringBuffer strBuffer = new StringBuffer(200);

		strBuffer = strBuffer.append(STR_ATTRIBUTE)
		.append(OPEN_BRACE)
		.append(ConfigurationConstants.ATTRIBUTE_COMPARISION_OPERATOR)
		.append(CLOSE_BRACE);

		String strCompOperator = strBuffer.toString();

		strBuffer.delete(0,strBuffer.length());

		strBuffer = strBuffer.append(STR_ATTRIBUTE)
		.append(OPEN_BRACE)
		.append(ConfigurationConstants.ATTRIBUTE_COMPATIBILITY_OPTION)
		.append(CLOSE_BRACE);
		String strCompOption = strBuffer.toString();

		//Begin of Add by RashmiL_Joshi, Enovia MatrixOne for Bug# 300580 Date: 3/23/2005
		String strCompOper = DomainConstants.EMPTY_STRING;
		String strCompOpt = DomainConstants.EMPTY_STRING;
		String strLocale = context.getSession().getLanguage();
		//End of Add by RashmiL_Joshi, Enovia MatrixOne for Bug# 300580 Date: 3/23/2005

		// forming the Compatibility Option String for each PRoduct Compatibility Rule.
		Object obj = null;
		for (int i = 0; i < iNumOfObjects; i++) {
			obj = pcrObjectIdsList.get(i);
			if (obj instanceof HashMap) {
				//Begin of Modify by RashmiL_Joshi, Enovia MatrixOne for Bug# 300580 Date: 3/23/2005
				strCompOper = i18nNow.getRangeI18NString(ConfigurationConstants.ATTRIBUTE_COMPATIBILITY_OPTION,
						(String)((HashMap)pcrObjectIdsList.get(i)).get(strCompOption),
						strLocale);
				strCompOpt = i18nNow.getRangeI18NString(ConfigurationConstants.ATTRIBUTE_COMPARISION_OPERATOR,
						(String)((HashMap)pcrObjectIdsList.get(i)).get(strCompOperator),
						strLocale);
				strBuffer.delete(0,strBuffer.length());
				strBuffer.append(XSSUtil.encodeForHTML(context,strCompOper))
				.append(SPACE)
				.append(XSSUtil.encodeForHTML(context,strCompOpt));
				lstCmpOption.add(strBuffer.toString());
			}
			else if (obj instanceof Hashtable) {
				strCompOper = i18nNow.getRangeI18NString(ConfigurationConstants.ATTRIBUTE_COMPATIBILITY_OPTION,
						(String)((Hashtable)pcrObjectIdsList.get(i)).get(strCompOption),
						strLocale);
				strCompOpt = i18nNow.getRangeI18NString(ConfigurationConstants.ATTRIBUTE_COMPARISION_OPERATOR,
						(String)((Hashtable)pcrObjectIdsList.get(i)).get(strCompOperator),
						strLocale);
				strBuffer.delete(0,strBuffer.length());
				strBuffer.append(XSSUtil.encodeForHTML(context,strCompOper))
				.append(SPACE)
				.append(XSSUtil.encodeForHTML(context,strCompOpt));
				lstCmpOption.add(strBuffer.toString());
				//End of Modify by RashmiL_Joshi, Enovia MatrixOne for Bug# 300580 Date: 3/23/2005
			}
		}
		return lstCmpOption;
	}


	/**
	 * This method is used to return inclusion/exclusion Rule Expression.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 * @return List
	 * @throws Exception if the operation fails
	 * @since R212 HF16
	 * @author 
	 * @category 
	 */
	public List showInclusionRuleExpressionForProductContext(Context context, String[] args) throws Exception
	 {
			List lstRightExpression = new StringList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			MapList extractedIRs = new MapList();
			
			for (int i = 0; i < objectList.size(); i++) 
			{
				Map objectMap = (Map) objectList.get(i);
				Map iRuleobjectMap = new HashMap();
				String strIncRuleRE = "";
				String isRootNode = (String) objectMap.get("Root Node");
				if (isRootNode != null) {
					iRuleobjectMap.put(ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION,strIncRuleRE);

				} else {
					strIncRuleRE = (String) objectMap.get("tomid[Left Expression].from[Inclusion Rule].attribute[Right Expression].value");
					if(strIncRuleRE == null)
						strIncRuleRE = "";
					iRuleobjectMap.put(ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION,strIncRuleRE);
				}
				extractedIRs.add(iRuleobjectMap);
			}
			
			Map newProgramMap = new HashMap();
			newProgramMap.put("objectList", extractedIRs);
			RuleProcess ruleProcess = new RuleProcess();
			lstRightExpression = ruleProcess.getExpressionForRuleDisplay(context,JPO.packArgs(newProgramMap),ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);
			
			if (lstRightExpression.size() == 0) {
				for (int i = 0; i < objectList.size(); i++) {
					lstRightExpression.add("");
				}
			}
			return lstRightExpression;
	}


}//end of class
