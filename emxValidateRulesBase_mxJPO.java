/*
 *  emxValidateRulesBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 */

import java.lang.String;
import java.lang.StringBuffer;
import java.util.List;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Integer;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.InclusionRule;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;


/**
 * This JPO class has some method pertaining to Rules.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */

public class emxValidateRulesBase_mxJPO extends emxDomainObject_mxJPO
{

  /**
  *Alias for Type, name & revision seperator.
  */
  protected static final String strTNRSeparator = "::";
  /**
  *Alias used for single colon.
  */
  protected static final String SINGLE_COLON = ":";
  /**
  *Alias for feature-option seperator.
  */
  protected static final String strFOSeparator = "~";
  /**
  *Alias for AND operator.
  */
  protected static final String strANDOperator = "AND";
  /**
  *Alias for OR operator.
  */
  protected static final String strOROperator = "OR";
  /**
  *Alias for NOT operator.
  */
  protected static final String strNOTOperator = "NOT";
 /**
  *Alias for value.
  */
  protected static final String VALUE = "value";
  /**
  *Alias for EFFECTIVE_SEQ_ORDER.
  */
  protected static final String EFFECTIVE_SEQ_ORDER = "EFFECTIVE_SEQ_ORDER";
  /**
  *Alias for pipe(|) operator.
  */
  protected static final String PIPE = "|";
  /**
  *Alias for space(" ").
  */
  protected static final String SPACE = " ";
  /**
  *Alias for TNR_FEATURE_OPTION.
  */
  protected static final String TNR_FEATURE_OPTION = "TNR_FEATURE_OPTION";
  /**
  *Alias for FEATURE_SUBFEATURE_NAME.
  */
  protected static final String FEATURE_SUBFEATURE_NAME = "FEATURE_SUBFEATURE_NAME";
  /**
  *Alias for RULE_EXPRESSION.
  */
  protected static final String RULE_EXPRESSION = "RULE_EXPRESSION";
  /**
  *Alias for comparision(==) operator.
  */
  protected static final String EQUALS = " == ";
  /**
  *Alias for not equal to (!=) operator.
  */
  protected static final String NOT_EQUALS = " != ";
  /**
  *Alias for AMPERSAND(&&) operator.
  */
  protected static final String AMPERSAND = " && ";
  /**
  *Alias for single quote(') operator.
  */
  protected static final String SINGLE_QUOTE = "'";
  /**
  *Alias for double quote(") operator.
  */
  protected static final char   DOUBLE_QUOTES = '"';
  /**
  *Alias for open brace([) operator.
  */
  protected static final String OPEN_BRACE = "[";
  /**
  *Alias for close brace(]) operator.
  */
  protected static final String CLOSE_BRACE = "]";
   /**
  *Alias for false.
  */
  protected static final String FALSE = "False";
   /**
  *Alias for type Products.
  */
  protected static final String PRODUCTS = ProductLineConstants.TYPE_PRODUCTS;
  /**
  *Alias for type Features.
  */
  protected static final String FEATURES = ProductLineConstants.TYPE_FEATURES;
  /**
  *Alias for type FeatureList.
  */
  protected static final String FEATURE_LIST = ProductLineConstants.TYPE_FEATURE_LIST;
   /**
  *Alias for relationship FeatureListFrom.
  */
  protected static final String FEATURE_LIST_FROM = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM;
   /**
  *Alias for relationship FeatureListTo.
  */
  protected static final String FEATURE_LIST_TO = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO;
   /**
  *Alias for type BCR.
  */
  protected static final String BCR_TYPE = ProductLineConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE;
   /**
  *Alias for relationship BCR.
  */
  protected static final String BCR_REL = ProductLineConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE;
   /**
  *Alias for attribute LeftExpression.
  */
  protected static final String LEFT_EXP = "Left Expression";
   /**
  *Alias for attribute RightExpression.
  */
  protected static final String RIGHT_EXP = "Right Expression";
   /**
  *Alias for relationship LeftExpression.
  */
  protected static final String LEFT_EXP_REL = ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION;
   /**
  *Alias for relationship RightExpression.
  */
  protected static final String RIGHT_EXP_REL = ProductLineConstants.RELATIONSHIP_RIGHT_EXPRESSION;
   /**
  *Alias for attribute SequenceOrder.
  */
  protected static final String SEQ_ORDER = ProductLineConstants.ATTRIBUTE_SEQUENCE_ORDER;
   /**
  *Alias for validation failed.
  */
  protected static final String VALIDATION_FAILED = ProductLineConstants.RANGE_VALUE_VALIDATION_FAILED;
   /**
  *Alias for validation passed.
  */
  protected static final String VALIDATION_PASSED = ProductLineConstants.RANGE_VALUE_VALIDATION_PASSED;
   /**
  *Alias for validation status.
  */
  protected static final String VALIDATION_STATUS = ProductLineConstants.ATTRIBUTE_VALIDATION_STATUS;
   /**
  *Alias for attribute Expression.
  */
  protected static final String ATTRIB_EXP = ProductLineConstants.ATTRIBUTE_EXPRESSION;
   /**
  *Alias for type.
  */
  protected static final String TYPE = DomainConstants.SELECT_TYPE;
   /**
  *Alias for name.
  */
  protected static final String NAME = DomainConstants.SELECT_NAME;
   /**
  *Alias for revision.
  */
  protected static final String REVISION = DomainConstants.SELECT_REVISION;
   /**
  *Alias for attribute.
  */
  protected static final String ATTRIBUTE = "attribute";
   /**
  *Alias for object id.
  */
  protected static final String ID = DomainConstants.SELECT_ID;
   /**
  *Alias for operator dot(.).
  */
  protected static final String DOT = ".";
   /**
  *Alias for to.
  */
  protected static final String TO = "to";
   /**
  *Alias for from.
  */
  protected static final String FROM = "from";
   /**
  *Alias for product id.
  */
  protected static final String PROD_ID = "productId";
   /**
  *Alias for IncRuleObject id.
  */
  protected static final String INC_RULE_ID = "IncRuleObjectId";
   /**
  *Alias for parent object id.
  */
  protected static final String PARENT_OID = "parentOID";
   /**
  *Alias for expression.
  */
  protected static final String EXPRESSION = "expression";
   /**
  *Alias for vault.
  */
  protected static final String VAULT = "eService Administration";
   /**
  *Alias for changeValidityFlag.
  */
  protected static final String CHANGE_VALIDITY_FLAG = "changeValidityFlag";
   /**
  *Alias for alert message fired when no rules are present.
  */
  protected static final String NO_RULE_TO_VALIDATE = "emxProduct.Alert.NoBCRtoValidate";
   /**
  *Alias for alert message fired when invalid feature options present in the rule.
  */
  protected static final String BCR_INVALID_FO_START = "emxProduct.Alert.OverAllBooleanCompatibilityRuleInvalidFeatureOptionReferenceStart";
   /**
  *Alias for alert message fired when inclusion rule refers invalid feature options.
  */
  protected static final String INVALID_INC_RULE_FOR_FO = "emxProduct.Alert.OverAllInclusionRuleInvalidFeatureOptionReference";
   /**
  *Alias for alert message fired when sequence order is invalid in inclusion rule.
  */
  protected static final String INVALID_INC_RULE_FOR_SEQ_ORDER = "emxProduct.Alert.OverAllInclusionRuleInvalidSequenceOrder";
   /**
  *Alias for alert message fired when there is invalid option compatibility rule.
  */
  protected static final String OCR_INVALID_FO = "emxProduct.Alert.InvalidOptionCompatibilityRule";
   /**
  *Alias for level.
  */
  protected static final String LEVEL = "level";
   /**
  *Alias for string FeatureType.
  */
  protected static final String F_TYPE = "FeatureType";
   /**
  *Alias for string FeatureName.
  */
  protected static final String F_NAME = "FeatureName";
   /**
  *Alias for string FeatureRevision.
  */
  protected static final String F_REVISION = "FeatureRevision";
   /**
  *Alias for string OptionType.
  */
  protected static final String O_TYPE = "OptionType";
   /**
  *Alias for string OptionName.
  */
  protected static final String O_NAME = "OptionName";
   /**
  *Alias for string OptionRevision.
  */
  protected static final String O_REVISION = "OptionRevision";
   /**
  *Alias for attribute Marketing Name.
  */
  protected static final String MKTG_NAME = ProductLineConstants.ATTRIBUTE_MARKETING_NAME;
   /**
  *Alias for string MARKETING_NAME.
  */
  protected static final String MARKETING_NAME = "MARKETING_NAME";
   /**
  *Alias for string BCRNAME.
  */
  protected static final String BCRNAME = "BCRNAME";
   /**
  *Alias for string INVALID_LIST.
  */
  protected static final String INVALID_LIST = "INVALID_LIST";
   /**
  *Alias for string LEFT_EXP_LIST.
  */
  protected static final String LEFT_EXP_LIST = "LEFT_EXP_LIST";
   /**
  *Alias for string RIGHT_EXP_LIST.
  */
  protected static final String RIGHT_EXP_LIST = "RIGHT_EXP_LIST";
   /**
  *Alias for string LEFT_EXP_CONN_LIST.
  */
  protected static final String LEFT_EXP_CONN_LIST = "LEFT_EXP_CONN_LIST";
   /**
  *Alias for string RIGHT_EXP_CONN_LIST.
  */
  protected static final String RIGHT_EXP_CONN_LIST = "RIGHT_EXP_CONN_LIST";
   /**
  *Alias for string BCR_RELATED_VALUES.
  */
  protected static final String BCR_RELATED_VALUES = "BCR_RELATED_VALUES";

  /**
  *Member variable for holding expression.
  */
  protected static String strExp = null;
  /**
  *Member variable for holding sequence order.
  */
  protected static String strSeqOrder = null;
  /**
  *Member variable for holding connected object name.
  */
  protected static String strConnObjectName = null;
  /**
  *Member variable for holding connected feature type.
  */
  protected static String strConnFeatureType = null;
  /**
  *Member variable for holding connected feature name.
  */
  protected static String strConnFeatureName = null;
  /**
  *Member variable for holding connected feature revision.
  */
  protected static String strConnFeatureRevision = null;
  /**
  *Member variable for holding connected option type.
  */
  protected static String strConnOptionType = null;
  /**
  *Member variable for holding connected option name.
  */
  protected static String strConnOptionName = null;
  /**
  *Member variable for holding connected option revision.
  */
  protected static String strConnOptionRevision = null;
  /**
  *Member variable for holding marketing name.
  */
  protected static String strMktgName = null;
  /**
  *Member variable for holding left expression feature option id.
  */
  protected static String strLeftExpFO_ID = null;
  /**
  *Member variable for holding right expression feature option id.
  */
  protected static String strRightExpFO_ID = null;






/**
  * Default Constructor.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  */
  emxValidateRulesBase_mxJPO (Context context, String[] args) throws Exception
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
    if (!context.isConnected()){
    	String sContentLabel = EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
        throw  new Exception(sContentLabel);
      }
    return 0;
  }

 
//=======================================================================================================================================================
 /**
  * Method to get all feature Lists which is connected to Feature Option.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param objectid holds the object id
  * @return Object - MapList containing the id of Feature List objects
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * referenced for migration
  */
protected MapList getAllFeatureLists(Context context, String objectid) throws Exception{
    objectid = objectid.trim();
    //String is initialized to store the value of relationship name
    String strRelationshipType = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+ ","+ ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM;
    //short is initialized to store the value the level till which object will be searched

    //Stringlist for querying is formulated.
    StringList objectSelects = new StringList();
    objectSelects.addElement(ID);
    objectSelects.addElement(TYPE);
    objectSelects.addElement(NAME);
    objectSelects.addElement(REVISION);
    objectSelects.addElement(strSeqOrder);

    DomainObject dom = newInstance(context,objectid);
    //The associated object details are retreived onto a MapList
    MapList allObjList = dom.getRelatedObjects(context,strRelationshipType,"*",objectSelects,null,false,true,(short)0,"","", 0);
    ArrayList objectList = new ArrayList();
    //creation of product MAP
    String strProductType = dom.getInfo(context,TYPE);
    String strProductName = dom.getInfo(context,NAME);
    String strProductRevision = dom.getInfo(context,REVISION);
    Map productMap = new HashMap();
    productMap.put(ID,objectid);
    productMap.put(TYPE,strProductType);
    productMap.put(NAME,strProductName);
    productMap.put(REVISION,strProductRevision);
    productMap.put(LEVEL,"0");
    objectList.add(productMap);
    //end of creation of product MAP

    String strNewLevel = "";
    int iPrevLevel = 0;
    int iNewLevel = 0;
    MapList featureListMapList = new MapList();
    for (int i=0;i<allObjList.size();i++){
        Map objMap = (Map)allObjList.get(i);
        strNewLevel =   (String)objMap.get(LEVEL);
        iNewLevel = Integer.parseInt(strNewLevel);
        if(iNewLevel>iPrevLevel){
            objectList.add(objMap);
        }else if(iNewLevel<iPrevLevel){
            int iCount = objectList.size();
            while(iCount>iNewLevel){
                Map featureOptionMap = (Map)processList(objectList);
                if(featureOptionMap!=null){
                    featureListMapList.add(featureOptionMap);
                }
                objectList.subList(iCount-2,iCount).clear();
                iCount = objectList.size();
            }
            objectList.add(objMap);
        }
        iPrevLevel = iNewLevel;
    }
        while(objectList.size()>=3){
            Map featureOptionMap = (Map)processList(objectList);
            if(featureOptionMap!=null){
                featureListMapList.add(processList(objectList));
            }
            objectList.subList(objectList.size()-2,(objectList.size())).clear();
        }
    return featureListMapList;
}


/**
   * To Obtain the Feature and Option TNR form the Array List.
   *
   * @param objList list of objects
   * @return Objecta Map , consisting of Feature List id, Feature and Option TNR
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   * referenced for migration
   */
protected  Object processList(ArrayList objList) throws Exception{
    Map returnMap = new HashMap();
    int iSize = objList.size();
    Map objMap = (Map)objList.get(iSize-2);
    if(((String)objMap.get(TYPE)).equals(FEATURE_LIST)){
        returnMap.put(ID,objMap.get(ID));
        objMap = (Map)(objList.get(iSize-3));
        StringBuffer sFO_TNR = new StringBuffer(512);
        sFO_TNR = sFO_TNR.append((String)objMap.get(TYPE));
        sFO_TNR = sFO_TNR.append(strTNRSeparator);
        sFO_TNR = sFO_TNR.append((String)objMap.get(NAME));
        sFO_TNR = sFO_TNR.append(strTNRSeparator);
        sFO_TNR = sFO_TNR.append((String)objMap.get(REVISION));
        sFO_TNR = sFO_TNR.append(strFOSeparator);
        objMap = (Map)(objList.get(iSize-1));
        sFO_TNR = sFO_TNR.append((String)objMap.get(TYPE));
        sFO_TNR = sFO_TNR.append(strTNRSeparator);
        sFO_TNR = sFO_TNR.append((String)objMap.get(NAME));
        sFO_TNR = sFO_TNR.append(strTNRSeparator);
        sFO_TNR = sFO_TNR.append((String)objMap.get(REVISION));
        returnMap.put(TNR_FEATURE_OPTION,sFO_TNR.toString());
        int i=1;
        StringBuffer sb = new StringBuffer(512);
        while(i<=(iSize-2)){
            objMap = (Map)objList.get(i);
            sb = sb.append((String)objMap.get(strSeqOrder));
            sb = sb.append(PIPE);
            i = i+2;
        }
        String strSeqOrder = sb.toString();
        strSeqOrder = strSeqOrder.substring(0,((strSeqOrder.length())-1));
        returnMap.put(EFFECTIVE_SEQ_ORDER,strSeqOrder);
        return returnMap;
    }else{
        return null;
    }

}





 /**
  * Method to separate out the Feature Option Pairs T::N::R~T::N::R from the Expression.
  *
  * @param strExp The expression from which the Feature Option pairs
  *                need to be separated out
  * @param cDelimiter The delimiter on the basis of which the Expression will be Tokenized
  * @return List The List containig the Strings of TNR of Feature Option
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * referenced for migration
  */

protected List getFeatureOptionFromExp(String strExp,char cDelimiter){
  int iIndex = strExp.indexOf(cDelimiter);
    String strTemp = strExp;
    StringBuffer sEvalExpression = new StringBuffer(512);
    List featureOptionList = new ArrayList();
    while (iIndex != -1) {
      strTemp = strTemp.substring(iIndex + 1, strTemp.length());
        iIndex = strTemp.indexOf(cDelimiter);
        if (iIndex != -1) {
          featureOptionList.add(strTemp.substring(0,iIndex));
          strTemp = strTemp.substring(iIndex + 1, strTemp.length());
          iIndex = strTemp.indexOf(cDelimiter);
          if(iIndex==0){
        sEvalExpression = sEvalExpression.append(SPACE);
      }
        }
    }

    return featureOptionList;
}



 /**
   * Method to get all the Left and Right Expressions of BCR objects.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param objectId String  Product ObjectId
   * @return Map contains key value pairs for all the BCR objects found related to the Product
   *              key-> String:name of BCR object
   *              value->ArrayList:contains 2 strings for Left and Right Expressions
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   * referenced for migration
   */

 protected Map getAllBCRExpressions(Context context, String objectId) throws Exception{

   HashMap expressionsMap = new HashMap();
   StringBuffer sWhereExp =  new StringBuffer(512);
   sWhereExp = sWhereExp.append(TYPE).append(EQUALS);
   sWhereExp = sWhereExp.append(SINGLE_QUOTE).append(BCR_TYPE).append(SINGLE_QUOTE);
   sWhereExp = sWhereExp.append(AMPERSAND).append(TO).append(OPEN_BRACE);
   sWhereExp = sWhereExp.append(BCR_REL).append(CLOSE_BRACE).append(DOT);
   sWhereExp = sWhereExp.append(FROM).append(DOT).append(ID).append(EQUALS);
   sWhereExp = sWhereExp.append(SINGLE_QUOTE).append(objectId).append(SINGLE_QUOTE);
   sWhereExp = sWhereExp.append(AMPERSAND).append(ATTRIBUTE).append(OPEN_BRACE);
   sWhereExp = sWhereExp.append(LEFT_EXP);
   sWhereExp = sWhereExp.append(CLOSE_BRACE).append(DOT).append(VALUE).append(NOT_EQUALS).append(SINGLE_QUOTE).append(SINGLE_QUOTE);
   sWhereExp = sWhereExp.append(AMPERSAND).append(ATTRIBUTE).append(OPEN_BRACE);
   sWhereExp = sWhereExp.append(RIGHT_EXP);
   sWhereExp = sWhereExp.append(CLOSE_BRACE).append(DOT).append(VALUE).append(NOT_EQUALS).append(SINGLE_QUOTE).append(SINGLE_QUOTE);


   StringBuffer sLeftExpression = new StringBuffer(512);
   sLeftExpression = sLeftExpression.append(ATTRIBUTE).append(OPEN_BRACE);
   sLeftExpression = sLeftExpression.append(LEFT_EXP).append(CLOSE_BRACE);

   StringBuffer sRightExpression = new StringBuffer(512);
   sRightExpression = sRightExpression.append(ATTRIBUTE).append(OPEN_BRACE);
   sRightExpression = sRightExpression.append(RIGHT_EXP).append(CLOSE_BRACE);

   StringList objectSelects =new StringList();
   objectSelects.addElement(ID);
   objectSelects.addElement(NAME);
   objectSelects.addElement(sLeftExpression.toString());
   objectSelects.addElement(sRightExpression.toString());

   MapList  bcrMaplist= DomainObject.findObjects(context, "*","*", sWhereExp.toString(),objectSelects);
   String strKey = "";
   String strName = "";
   String strLeftExpressionValue = "";
   String strRightExpressionValue = "";

   Map retValuesMap = null;
   for(int i=0;i<bcrMaplist.size();i++){
     retValuesMap =  (Map)bcrMaplist.get(i);
     strKey = (String)retValuesMap.get(ID);
     strName = (String)retValuesMap.get(NAME);
     strLeftExpressionValue = (String)retValuesMap.get(sLeftExpression.toString());
     strRightExpressionValue = (String)retValuesMap.get(sRightExpression.toString());
     List expressionValueList = new ArrayList();
     expressionValueList.add(strName);
     expressionValueList.add(strLeftExpressionValue);
     expressionValueList.add(strRightExpressionValue);
     expressionsMap.put(strKey,expressionValueList);
   }

   return expressionsMap;

 }


/**
  * Method to check whether all the feature Option pairs List obtained from the expression.
  * is present in the All feature Option List.
  *
  * @param expList List containing Strings of feature Option TNR
  * @param allFOMapList MapList containing Maps.
  *                      The Map contains 2 elements : effective Seq num and TNR of Feature Option
  * @return boolean: true : if all the Strings of expList are present in the Maps of allFOMapList
  *                  false : else false
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * referenced for migration
  */

protected List isExpFOpresentInAllFOList(List expList,MapList allFOMapList) throws Exception{
  List invalidList = new ArrayList();
  int iCount = 0;
  for(;iCount<expList.size();iCount++){
    boolean bMatchFound = false;
    String strFOofExp = (String)expList.get(iCount);
    if(!strFOofExp.equals("")){
      for(int i=0;i<allFOMapList.size();i++){
        Map allFOmap = (Map)allFOMapList.get(i);
        String strFO = (String)allFOmap.get(TNR_FEATURE_OPTION);
        if(strFOofExp.equals(strFO)){
          bMatchFound = true;
          break;
        }
      }
      if(bMatchFound==false){
          if(!invalidList.contains(getNameFromTNR(strFOofExp))){
              invalidList.add(getNameFromTNR(strFOofExp));
          }
      }
    }
  }
  return invalidList;
}

/**
  * Method to get the name of fetaure Option in the Feature~Option nams format.
  *
  * @param strTNR String The string having T::N::R~T::N::R format
  * @return String  The N~N formatted string
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * referenced for migration
  */

protected String getNameFromTNR(String strTNR) throws Exception{
        //the method is suuposed to work even if the names of Feature/Option contains double colon in them
        String strNew = "";
        StringTokenizer st = new StringTokenizer(strTNR,strTNRSeparator);
        String strFirstToken = st.nextToken();
        String strLastToken = "";
        while(st.hasMoreTokens()){
            strLastToken = st.nextToken();
        }
        strNew = strTNR.substring(strFirstToken.length()+2,strTNR.length()-strLastToken.length()-2);
        StringTokenizer st1 = new StringTokenizer(strNew,strTNRSeparator);
        String strTildePart = "";
        while(st1.hasMoreTokens()){
            strTildePart = st1.nextToken();
            if(strTildePart.indexOf(strFOSeparator)!=-1){
                break;
            }
        }
        int iStartIndex = strNew.indexOf(strTildePart);
        int iEndIndex = strTildePart.length();
        StringBuffer sb = new StringBuffer(512);
        sb = sb.append(strNew.substring(0,iStartIndex-2));
        sb = sb.append(strFOSeparator);
        sb = sb.append(strNew.substring(iStartIndex+iEndIndex+2,strNew.length()));
        return sb.toString();
}


/**
  * Method to get an unique list of Strings from 2 Lists.
  *
  * @param l1 List containing Strings of feature Option TNR
  * @param l2 List containing Strings of feature Option TNR
  * @return List List of unique strings
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * referenced for migration
  */

protected List getUniqueList(List l1,List l2) throws Exception{
        List returnList = new ArrayList();
        String str1 = "";
        String str2 = "";
        boolean bmatchFound = false;

        for(int k=0;k<l2.size();k++){
            returnList.add(l2.get(k));
        }

        for(int i=0;i<l1.size();i++){
            str1 = (String)l1.get(i);
            for(int j=0;j<l2.size();j++){
                str2 = (String)l2.get(j);
                if(str2.equals(str1)){
                    bmatchFound = true;
                    break;
                }
            }
            if(!bmatchFound){
                returnList.add(str1);
            }
        }
return returnList;
}



/**
  * Method to check whether all the feature Options present in each of the BCR are present.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args packed argument
  * @return ArrayList : List containing strings :names of the BCRs which have invalid references
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * referenced for migration
  */
public List validateBCRsforExistingFOreference(Context context, String[] args) throws Exception{

 List invalidReferenceList = new ArrayList();
  Map argMap = (Map)JPO.unpackArgs(args);
  
 String strProductObjectId = (String)(argMap.get("id"));


  DomainObject domObj = new DomainObject(strProductObjectId);
  String strParentType = domObj.getInfo(context,DomainConstants.SELECT_TYPE);
  
  Boolean changeValFlag = (Boolean)(argMap.get(CHANGE_VALIDITY_FLAG));
  MapList allFOMapList = getAllFeatureLists(context,strProductObjectId);
  HashMap bcrExpressions =  (HashMap)getAllBCRExpressions(context,strProductObjectId);
  
  if(bcrExpressions.size()==0&&(changeValFlag.booleanValue())){
    String strLocale = context.getSession().getLanguage();
    String strNoBCRtoValidate = EnoviaResourceBundle.getProperty(context, "Configuration",NO_RULE_TO_VALIDATE,strLocale);
    throw new FrameworkException(strNoBCRtoValidate);

  }else{

     StringList slProductChildTypes = ProductLineUtil.getChildrenTypes(context, PRODUCTS);
     StringList slFeatureChildTypes = ProductLineUtil.getChildrenTypes(context, FEATURES);


    Map.Entry me =  null;
    List  expressionValue = new ArrayList();
    String strLeftExpression = "";
    String strRightExpression = "";
    String strName = "";
    String strKey_BCRId = "";
    Collection expressionSet = bcrExpressions.entrySet();
    Iterator it = expressionSet.iterator();
    if (slProductChildTypes.contains(strParentType))
    {

     while(it.hasNext()) {
      me = (Map.Entry)it.next();
      strKey_BCRId = (String)me.getKey();
      expressionValue = (ArrayList)me.getValue();
      strName = (String)expressionValue.get(0);
      strLeftExpression = (String)expressionValue.get(1);
      strRightExpression = (String)expressionValue.get(2);
      List featureOptionListForLeftExp = getFeatureOptionFromExp(strLeftExpression,DOUBLE_QUOTES);
      List featureOptionListForRightExp = getFeatureOptionFromExp(strRightExpression,DOUBLE_QUOTES);
      List leftExpInvalidFOreference = isExpFOpresentInAllFOList(featureOptionListForLeftExp,allFOMapList);
      List rightExpInvalidFOreference = isExpFOpresentInAllFOList(featureOptionListForRightExp,allFOMapList);
      if((leftExpInvalidFOreference.size()!=0)||(rightExpInvalidFOreference.size()!=0)){
        Map invalidReferenceMap = new HashMap();
        invalidReferenceMap.put(BCRNAME,strName);
        invalidReferenceMap.put(INVALID_LIST,getUniqueList(leftExpInvalidFOreference,rightExpInvalidFOreference));
        invalidReferenceMap.put("Change","false");
        invalidReferenceList.add(invalidReferenceMap);
        if(changeValFlag.booleanValue()){
            changeValidityofBCR(context,strKey_BCRId,VALIDATION_FAILED);
        }
      }else{
        if(changeValFlag.booleanValue()){
            changeValidityofBCR(context,strKey_BCRId,VALIDATION_PASSED);
        }
      }
    }
    invalidReferenceList=checkConnectedFO(context,invalidReferenceList,strProductObjectId,changeValFlag,true);
   }
   else if (slFeatureChildTypes.contains(strParentType))
   {
        invalidReferenceList =checkConnectedFO(context,invalidReferenceList,strProductObjectId,changeValFlag,false);
    }
  }
  return invalidReferenceList;

}

/**
  * Method to check whether all the feature Options connected to each of BCR are valid or not.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param invalidReferenceList list containing invalid feature options.
  * @param strObjectId object id of product or feature..
  * @param changeValFlag flag indicating to change the validity status of the BCR .
  * @param fromProductFlag flag indicating whether the object is product or feature.
  * @return List containing names of the BCRs which have invalid references
  * @throws Exception if the operation fails
  * @since ProductCentral 10.5
  * @grade 0
  * referenced for migration
  */
protected List checkConnectedFO(Context context,List invalidReferenceList,String strObjectId,Boolean changeValFlag,boolean fromProductFlag) throws Exception
{
    List lstTry=getAllBooleanCompRulesInfo(context,strObjectId);

        for(int i=0;i<lstTry.size();i++)
       {
            Map BCRMap =(Map)lstTry.get(i);
           boolean bBreakFlag = false;
            Map BCRValuesMap = (Map)BCRMap.get("BCR_RELATED_VALUES");
            String strName = (String)BCRMap.get(NAME);
            String strBCRId = (String) BCRMap.get(ID);

             for(int k=0;k<invalidReferenceList.size();k++)
            {
                HashMap invalidMap = (HashMap)invalidReferenceList.get(k);
                String strPresentName = (String)invalidMap.get("BCRNAME");
                if(strPresentName.equals(strName))
               {
                    bBreakFlag = true;
                    break;
                }

            }
            if(bBreakFlag == true)
           {
                continue;
            }

            List lstLeftExpConnList = (List)BCRValuesMap.get("LEFT_EXP_CONN_LIST");
            List lstRightExpConnList = (List)BCRValuesMap.get("RIGHT_EXP_CONN_LIST");

            List lstLeftExpList = (List)BCRValuesMap.get("LEFT_EXP_LIST");
            List lstRightExpList = (List)BCRValuesMap.get("RIGHT_EXP_LIST");

            List leftExpInvalidFOreference = isExpFOpresentInConnectedFOList(lstLeftExpList,lstLeftExpConnList);

            List rightExpInvalidFOreference = isExpFOpresentInConnectedFOList(lstRightExpList,lstRightExpConnList);
            if((leftExpInvalidFOreference.size()!=0)||(rightExpInvalidFOreference.size()!=0)){
                    Map invalidReferenceMap = new HashMap();
                    invalidReferenceMap.put(BCRNAME,strName);
                    invalidReferenceMap.put(INVALID_LIST,getUniqueList(leftExpInvalidFOreference,rightExpInvalidFOreference));
                     if(fromProductFlag==true)
                     {
                           invalidReferenceMap.put("Change","true");
                     }
                     else
                    {
                           invalidReferenceMap.put("Change","false");
                     }
                    invalidReferenceList.add(invalidReferenceMap);
                    if(changeValFlag.booleanValue()){
                        changeValidityofBCR(context,strBCRId,VALIDATION_FAILED);
                    }
            }else{
                        if(changeValFlag.booleanValue()&&fromProductFlag==false){
                            changeValidityofBCR(context,strBCRId,VALIDATION_PASSED);
                }
      }

       }
       return(invalidReferenceList);
}

/**
  * Method to check whether all the feature Options present in the expressions of BCR are present in the list of connected objects of that BCR.
  *
  * @param expList List containing the expressions of BCR
  * @param allFOMapList list containing all feature options.
  * @return List containing names of the invalid feature options referances.
  * @throws Exception if the operation fails
  * @since ProductCentral 10.5
  * @grade 0
  * referenced for migration
  */
protected List isExpFOpresentInConnectedFOList(List expList,List allFOMapList) throws Exception{

  List invalidList = new ArrayList();
  int iCount = 0;
  for(;iCount<expList.size();iCount++){

    boolean bMatchFound = false;
    String strFOofExp = (String)expList.get(iCount);
    if(!strFOofExp.equals("")){
      for(int i=0;i<allFOMapList.size();i++){
        String strFO = (String)allFOMapList.get(i);
        if(strFOofExp.equals(strFO)){
          bMatchFound = true;
          break;

        }
      }
      if(bMatchFound==false){
          if(!invalidList.contains(getNameFromTNR(strFOofExp))){
              invalidList.add(getNameFromTNR(strFOofExp));
          }
      }
    }
  }
  return invalidList;
}

/**
   * This mehtod returns all the boolean compatibility rules connected to particular feature.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param strFeatureID - Object id of the feature
   * @return List containing all the boolean compatibility rules and related values.
   * @throws Exception if the operation fails
   * @since ProductCentral 10.5
   * referenced for migration
   */
protected List getAllBooleanCompRulesInfo(Context context,String strFeatureID) throws Exception{

    //The structure of this Hash Map will be as follows:
    //Key will be a String representing the name of the BCR Object
    //Corresponding to this kep the value will be a Map (Hashmap) which will contain the values related to that particular BCR
    //no in the Vlue Map here are the Key And Value pairs
    //1.LEFT_EXP_FO a List containing Strings of T::N::R~T::N::R
    //2.RIGHT_EXP_FO a List containing Strings of T::N::R~T::N::R
    //3.LEFT_CONNECTED_FO a List containing Strings of T::N::R~T::N::R
    //4.RIGHT_CONNECTED_FO a List containing Strings of T::N::R~T::N::R
    List booleanCompRulesAndrelatedValuesList = new ArrayList();
    StringBuffer sWhereExp =  new StringBuffer(512);
    sWhereExp = sWhereExp.append(TYPE).append(EQUALS);
    sWhereExp = sWhereExp.append(SINGLE_QUOTE).append(BCR_TYPE).append(SINGLE_QUOTE);
    sWhereExp = sWhereExp.append(AMPERSAND).append(TO).append(OPEN_BRACE);
    sWhereExp = sWhereExp.append(BCR_REL).append(CLOSE_BRACE).append(DOT);
    sWhereExp = sWhereExp.append(FROM).append(DOT).append(ID).append(EQUALS);
    sWhereExp = sWhereExp.append(SINGLE_QUOTE).append(strFeatureID).append(SINGLE_QUOTE);
    sWhereExp = sWhereExp.append(AMPERSAND).append(ATTRIBUTE).append(OPEN_BRACE);
    sWhereExp = sWhereExp.append(LEFT_EXP);
    sWhereExp = sWhereExp.append(CLOSE_BRACE).append(NOT_EQUALS).append(SINGLE_QUOTE).append(SINGLE_QUOTE);
    sWhereExp = sWhereExp.append(AMPERSAND).append(ATTRIBUTE).append(OPEN_BRACE);
    sWhereExp = sWhereExp.append(RIGHT_EXP);
    sWhereExp = sWhereExp.append(CLOSE_BRACE).append(NOT_EQUALS).append(SINGLE_QUOTE).append(SINGLE_QUOTE);


    StringBuffer sLeftExpression = new StringBuffer(512);
    sLeftExpression = sLeftExpression.append(ATTRIBUTE).append(OPEN_BRACE);
    sLeftExpression = sLeftExpression.append(LEFT_EXP).append(CLOSE_BRACE);

    StringBuffer sRightExpression = new StringBuffer(512);
    sRightExpression = sRightExpression.append(ATTRIBUTE).append(OPEN_BRACE);
    sRightExpression = sRightExpression.append(RIGHT_EXP).append(CLOSE_BRACE);

    StringBuffer sFeatureListIDConnectedByLeftExp = new StringBuffer(512);
    sFeatureListIDConnectedByLeftExp = sFeatureListIDConnectedByLeftExp.append(FROM).append(OPEN_BRACE).append(LEFT_EXP_REL).append(CLOSE_BRACE);
    sFeatureListIDConnectedByLeftExp = sFeatureListIDConnectedByLeftExp.append(DOT).append(TO).append(DOT).append(ID);
//System.out.println("sFeatureListIDConnectedByLeftExp->"+sFeatureListIDConnectedByLeftExp);

    StringBuffer sFeatureListIDConnectedByRightExp = new StringBuffer(512);
    sFeatureListIDConnectedByRightExp = sFeatureListIDConnectedByRightExp.append(FROM).append(OPEN_BRACE).append(RIGHT_EXP_REL).append(CLOSE_BRACE);
    sFeatureListIDConnectedByRightExp = sFeatureListIDConnectedByRightExp.append(DOT).append(TO).append(DOT).append(ID);
//System.out.println("sFeatureListIDConnectedByRightExp->"+sFeatureListIDConnectedByRightExp);

    StringList objectSelects =new StringList();
    objectSelects.addElement(ID);
    objectSelects.addElement(NAME);
    objectSelects.addElement(sLeftExpression.toString());
    objectSelects.addElement(sRightExpression.toString());
    //objectSelects.addElement(sFeatureListIDConnectedByLeftExp.toString());
    //objectSelects.addElement(sFeatureListIDConnectedByRightExp.toString());


    MapList  bcrMaplist= DomainObject.findObjects(context, "*","*", sWhereExp.toString(),objectSelects);

    Map bcrMap = null;
    String strBCRname = "";
    String strBCRid = "";
    String strLeftExpression = "";
    String strRightExpression = "";
    List lstLeftExpressionFeatList = null;
    List lstRightExpressionFeatList = null;

    List lstSeparatedFOfromLeftExp = null;
    List lstSeparatedFOfromRightExp = null;

    List lstFOfromLeftExpFeatList = null;
    List lstFOfromRightExpFeatList = null;


    for(int i=0;i<bcrMaplist.size();i++){
        bcrMap=(Map)bcrMaplist.get(i);
        strBCRname = (String)bcrMap.get(NAME);
        strBCRid = (String)bcrMap.get(ID);
        strLeftExpression = (String)bcrMap.get(sLeftExpression.toString());
        strRightExpression = (String)bcrMap.get(sRightExpression.toString());

        lstLeftExpressionFeatList = getConnFeatureListObjects(context,strBCRid,true);
        lstRightExpressionFeatList = getConnFeatureListObjects(context,strBCRid,false);

//System.out.println("lstLeftExpressionFeatList->"+lstLeftExpressionFeatList);

        lstSeparatedFOfromLeftExp = getFeatureOptionFromExp(strLeftExpression,DOUBLE_QUOTES);
        lstSeparatedFOfromRightExp = getFeatureOptionFromExp(strRightExpression,DOUBLE_QUOTES);

//System.out.println("lstSeparatedFOfromLeftExp->"+lstSeparatedFOfromLeftExp);
        lstFOfromLeftExpFeatList = new ArrayList();
        String strFeatListID = "";
        String strFOTNRfromFeatList = "";
        for(int j=0;j<lstLeftExpressionFeatList.size();j++){
            strFeatListID = (String)lstLeftExpressionFeatList.get(j);
            strFOTNRfromFeatList = getFeatOptionTNRStringFromFeatListID(context,strFeatListID);
            lstFOfromLeftExpFeatList.add(strFOTNRfromFeatList);
        }

        lstFOfromRightExpFeatList = new ArrayList();
        strFeatListID = "";
        strFOTNRfromFeatList = "";
        for(int k=0;k<lstRightExpressionFeatList.size();k++){
            strFeatListID = (String)lstRightExpressionFeatList.get(k);
            strFOTNRfromFeatList = getFeatOptionTNRStringFromFeatListID(context,strFeatListID);
            lstFOfromRightExpFeatList.add(strFOTNRfromFeatList);
        }
        Map bcrValuesMap = new HashMap();

        bcrValuesMap.put(LEFT_EXP_LIST,lstSeparatedFOfromLeftExp);
        bcrValuesMap.put(RIGHT_EXP_LIST,lstSeparatedFOfromRightExp);
        bcrValuesMap.put(LEFT_EXP_CONN_LIST,lstFOfromLeftExpFeatList);
        bcrValuesMap.put(RIGHT_EXP_CONN_LIST,lstFOfromRightExpFeatList);

        Map booleanCompRulesAndrelatedValuesMap = new HashMap();
        booleanCompRulesAndrelatedValuesMap.put(ID,strBCRid);
        booleanCompRulesAndrelatedValuesMap.put(NAME,strBCRname);
        booleanCompRulesAndrelatedValuesMap.put(BCR_RELATED_VALUES,bcrValuesMap);
        booleanCompRulesAndrelatedValuesList.add(booleanCompRulesAndrelatedValuesMap);
    }
//System.out.println("booleanCompRulesAndrelatedValuesList->"+booleanCompRulesAndrelatedValuesList);

    return booleanCompRulesAndrelatedValuesList;
}
/**
  * Method to change the validaton status attribute of BCR.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param objectId object id of the BCR
  * @param strValidationValue String containing validation value
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * referenced for migration
  */

protected void changeValidityofBCR(Context context,String objectId,String strValidationValue) throws Exception{
  setId(objectId);
  HashMap mapAttrib = new HashMap(1);
  mapAttrib.put(VALIDATION_STATUS,strValidationValue);
  setAttributeValues(context,mapAttrib);
}

/**
   * This mehtod returns all the connected feature list objects to perticular rule.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param strBCRID - Object id of the BCR
   * @param isLeft - boolean value indicating whether to consider left expression or right expression of the rule.
   * @return List containing all the feature list objects .
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   * referenced for migration
   */

protected List getConnFeatureListObjects(Context context,String strBCRID,boolean isLeft) throws Exception{

    List retList = new ArrayList();
    StringBuffer sWhereExp =  new StringBuffer(512);
    sWhereExp = sWhereExp.append(TYPE).append(EQUALS);
    sWhereExp = sWhereExp.append(SINGLE_QUOTE).append(FEATURE_LIST).append(SINGLE_QUOTE);
    sWhereExp = sWhereExp.append(AMPERSAND).append(TO).append(OPEN_BRACE);
    if(isLeft){
        sWhereExp = sWhereExp.append(LEFT_EXP_REL);
    }else{
        sWhereExp = sWhereExp.append(RIGHT_EXP_REL);
    }
    sWhereExp = sWhereExp.append(CLOSE_BRACE).append(DOT);
    sWhereExp = sWhereExp.append(FROM).append(DOT).append(ID).append(EQUALS);
    sWhereExp = sWhereExp.append(SINGLE_QUOTE).append(strBCRID).append(SINGLE_QUOTE);
//System.out.println("sWhereExp in getConnFeatureListObjects->"+sWhereExp);

    StringList objectSelects =new StringList();
    objectSelects.addElement(ID);
    MapList  featureListMapList= DomainObject.findObjects(context, "*","*", sWhereExp.toString(),objectSelects);
    //System.out.println("TAN featureListMapList->"+featureListMapList);

    String strFeatureListID = "";
    for(int i=0;i<featureListMapList.size();i++){
        Map flMap = (Map)featureListMapList.get(i);
        strFeatureListID = (String)flMap.get(ID);
        retList.add(strFeatureListID);
    }
    //System.out.println("retList->"+retList);
    return retList;
}

/**
   * This mehtod returns Feature option TNR from feature list id.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param strFeatureListID - Object id of the Feature list
   * @return String TNR of the Feature option. .
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   * referenced for migration
   */

protected String getFeatOptionTNRStringFromFeatListID(Context context,String strFeatureListID) throws Exception{
    //System.out.println("INSIDE getFeatOptionTNRStringFromFeatListID strFeatureListID->"+strFeatureListID);
    DomainObject dom = new DomainObject();
    dom.setId(strFeatureListID);

    StringList objectSelects =new StringList();
    objectSelects.addElement(strConnFeatureType);
    objectSelects.addElement(strConnFeatureName);
    objectSelects.addElement(strConnFeatureRevision);
    objectSelects.addElement(strConnOptionType);
    objectSelects.addElement(strConnOptionName);
    objectSelects.addElement(strConnOptionRevision);
    Map featOptionMap = null;
    featOptionMap = dom.getInfo(context,objectSelects);
//System.out.println("featOptionMap->"+featOptionMap);
    StringBuffer sFOTNR = new StringBuffer(512);
    sFOTNR = sFOTNR.append((String)featOptionMap.get(strConnFeatureType));
    sFOTNR = sFOTNR.append(strTNRSeparator);
    sFOTNR = sFOTNR.append((String)featOptionMap.get(strConnFeatureName));
    sFOTNR = sFOTNR.append(strTNRSeparator);
    sFOTNR = sFOTNR.append((String)featOptionMap.get(strConnFeatureRevision));
    sFOTNR = sFOTNR.append(strFOSeparator);
    sFOTNR = sFOTNR.append((String)featOptionMap.get(strConnOptionType));
    sFOTNR = sFOTNR.append(strTNRSeparator);
    sFOTNR = sFOTNR.append((String)featOptionMap.get(strConnOptionName));
    sFOTNR = sFOTNR.append(strTNRSeparator);
    sFOTNR = sFOTNR.append((String)featOptionMap.get(strConnOptionRevision));
//System.out.println("INSIDE getFeatOptionTNRStringFromFeatListID sFOTNR.toString()->"+sFOTNR.toString());

    return sFOTNR.toString();
    }

/**
 * Method call as a trigger to promote all the rules to the Release state if they already are not in that state.
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args - Holds the parameters passed from the calling method
 * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
 * @throws Exception if the operation fails
 * @since ProductCentral 10.0.0.0
 * @grade 0
 */
 public int promoteRules(Context context, String args[]) throws Exception
 {
   //The product object id sent by the emxTriggerManager is retrieved here.
   String objectId = args[0];
   //The object id is set to the context
   setId(objectId);
   /*The relationship pattern is initialized to obtain all the rules and feature list connected
    *to the product object. These object will be promoted to release state when the product object
    *is promoted to release state.
    */
   String strComma = ",";
   String strRelationshipPattern = ProductLineConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE + strComma
   + ProductLineConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION + strComma
   + ProductLineConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
   //Object where condition to retrieve the objects that are not already in Release state.
   String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " != \""+ProductLineConstants.STATE_RELEASE+"\")";
   //Type to fetched is all types returned by the relationship.
   String strType = "*";
   //ObjectSelects retreives the parameters of the objects that are to be retreived.
   StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
   StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
   //The objects connected to the product based on the relationships defined are obtained.
   MapList relBusObjList = getRelatedObjects(context,strRelationshipPattern,strType,objectSelects,relSelects,true,true,(short)1,objectWhere,DomainConstants.EMPTY_STRING,(short)0);
   //The number of objects connected is obtained.
   int iNumberOfObjects = relBusObjList.size();
   //The promotion is to happen only if there are any connected objects
   if (iNumberOfObjects > 0)
   {
     //Processing for each of the object connected to the product
     for (int i = 0;i < iNumberOfObjects ; i++)
     {
       //Each id of the object connected to product is obtained.
       String strTempObjectId = (String)((Hashtable)relBusObjList.get(i)).get(DomainConstants.SELECT_ID);
       //The context is set with the object id obtained.
       setId(strTempObjectId);
       //The state of the context object is set to release.
       setState(context,ProductLineConstants.STATE_RELEASE);
     }
   }
   //0 returned just to indicate the end of processing.
   return 0;
 }

 /**
   * Method call as a trigger to demote all the rules to the Release state if they already are not in that state.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args - Holds the parameters passed from the calling method
   * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   * @grade 0
   */
   public int demoteRules(Context context, String args[]) throws Exception
   {
     //The product object id sent by the emxTriggerManager is retrieved here.
     String objectId = args[0];
     //The object id is set to the context
     setId(objectId);
     /*The relationship pattern is initialized to obtain all the rules and feature list connected
      *to the product object. These object will be demoted to preliminary state when the product object
      *is promoted to release state.
      */
     String strComma = ",";
     String strRelationshipPattern = ProductLineConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE + strComma
     + ProductLineConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION + strComma
     + ProductLineConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
     //Object where condition to retrieve the objects that are in Release state.
     String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " == \""+ProductLineConstants.STATE_RELEASE+"\")";
     //Type to fetched is all types returned by the relationship.
     String strType = "*";
     //ObjectSelects retreives the parameters of the objects that are to be retreived.
     StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
     StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
     //The objects connected to the product based on the relationships defined are obtained.
     MapList relBusObjList = getRelatedObjects(context,strRelationshipPattern,strType,objectSelects,relSelects,true,true,(short)1,objectWhere,DomainConstants.EMPTY_STRING,(short)0);
     //The number of objects connected is obtained.
     int iNumberOfObjects = relBusObjList.size();
     //The promotion is to happen only if there are any connected objects
     if (iNumberOfObjects > 0)
     {
       //Processing for each of the object connected to the product
       for (int i = 0;i < iNumberOfObjects ; i++)
       {
         //Each id of the object connected to product is obtained.
         String strTempObjectId = (String)((Hashtable)relBusObjList.get(i)).get(DomainConstants.SELECT_ID);
         //The context is set with the object id obtained.
         setId(strTempObjectId);
         //The state of the context object is set to release.
         setState(context,ProductLineConstants.STATE_PRELIMINARY);
       }
     }
     //0 returned just to indicate the end of processing.
     return 0;
 }


 

}//end of class
