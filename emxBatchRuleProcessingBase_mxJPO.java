/*
 *  emxBatchRuleProcessingBase.java
 *
 * Copyright (c) 2004-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.9.2.2.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 *
 * Last Updated On: 20th October 2005.
 * Modified for optionCompatibility rule validation to check for FL id and not feature id.
 */



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.FeatureHolder;
import com.matrixone.apps.configuration.ProductConfiguration;
import com.matrixone.apps.configuration.ProductConfigurationHolder;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineConstants;


/**
 * This JPO class has some methods pertaining to Rule type.
 * @version ProductCentral 10.0.0.0 - Copyright (c) 2004-2016 Dassault Systemes.
 * @author Enovia MatrixOne
 */

public class emxBatchRuleProcessingBase_mxJPO extends emxDomainObject_mxJPO {
    /**
    * Alias used for Type,Name,Seperator .
    */
    protected final static String FEATURE_TNR_DELIMITER = "::";
    /**
    * Alias used for Feature option delimiter.
    */
    protected final static String FEATURE_OPTION_DELIMITER = "~";
    /**
    * Alias used for opening brace.
    */
    protected final static String EXPRESSION_OPEN = "[";
    /**
    * Alias used for closing brace.
    */
    protected final static String EXPRESSION_CLOSE = "]";
    /**
    * Alias used for double quotes.
    */
    protected final static String STR_QUOTES = "\"";
    /**
    * Alias used for string True.
    */
    protected final static String STR_TRUE = "TRUE";
    /**
    * Alias used for string False.
    */
    protected final static String STR_FALSE = "FALSE";
    /**
    * Alias used for string UNKNOWN.
    */
    protected final static String STR_UNKNOWN = "UNKNOWN";
    /*
    * A string constant with the value "emxProduct.Alert.CurrentConfigurationValidationFailed".
    */
    public static final String CHECK_CURRENTCONFIGURATION = "emxProduct.Alert.CurrentConfigurationValidationFailed";
    public static final String SUITE_KEY = "Configuration";
    public static final String RESOURCEBUNDLE = "emxConfigurationStringResource";
    /*
    * A string constant with the value ",".
    */
    public static final String STR_COMMA = ",";
    
    // Added by Enovia MatrixOne for Bug # 306380 Date 06/22/2005
    private static final char OPEN_BRACE = '(';
    private static final char CLOSE_BRACE = ')';

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
     * Constructs an emxBatchRuleProcessingBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */

    public emxBatchRuleProcessingBase_mxJPO (Context context, String[] args) throws Exception {
       super(context, args);
    }

    /**
     * Main entry point in the JPO emxBatchRuleProcessingBase.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success and 1 = failure)
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */

    public int mxMain(Context context, String[] args)
    throws Exception
    {
        String strLanguage = context.getSession().getLanguage();
        String strDesktopClientFailed = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.DesktopClientFailed",strLanguage);
        if (!context.isConnected())
            throw  new Exception(strDesktopClientFailed);
        return  0;
    }

    /**
    * This method will be called as part of a promotion check trigger to check if all rules are validated.
    *
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args     a String array holding the arguments from the calling method.
     *                   It contains a string representing the strProductConfigurationID.
     * @return int 0 or 1
     * @throws Exception when the operation fails.
     * @since R212
     */
    public int validateBatchRulesForPromoteCheck(Context context, String[] args) throws Exception{
    	
         int iFlag = 0;

         //Check to see if the validation has already been done.
         //The RPE variable is only set when the promote action is set to
         //begin upon a successful PC validation.
         boolean marketingRulesPassed = false;
         String skipTriggerCheck = PropertyUtil.getRPEValue(context, "MX_SKIP_PC_VALIDATION_IN_PROMOTE_CHECK", false);
         if(skipTriggerCheck != null && "true".equals(skipTriggerCheck))
         {
             //reset the RPE variable just in case it gets called from some where else, very unlikely.
             PropertyUtil.setRPEValue(context, "MX_SKIP_PC_VALIDATION_IN_PROMOTE_CHECK", "false", false);
         }
         else {
             String strProductConfigurationID = args[0];
             ProductConfiguration productConf = new ProductConfiguration();
             productConf.setId(strProductConfigurationID);
             //Marketing Rules Evaluation
             productConf.initContext(context);
             productConf.loadContextStructure(context, productConf.getContextId(), productConf.getParentProductId());
             productConf.loadSelectedOptions(context);
             marketingRulesPassed = productConf.isValidProductConfiguration(context);
             if (marketingRulesPassed)
             {
            	 setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_PASSED);
     
             }else
             {
            	 setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_DESIGN_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_FAILED);
                 iFlag = 1;
             }
             
             //Design Rule Evaluation
             productConf.clearStructure();
             productConf.loadContextLogicalStructure(context, productConf.getContextId(), productConf.getParentProductId());
             StringList conflictingRules = productConf.validateDesignRules(context);
             if (conflictingRules.size() > 0)
             {
            	 	setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_DESIGN_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_FAILED);
                    iFlag = 1;
             }else 
             {
            	 setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_DESIGN_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_PASSED);
             }
             
             if(iFlag == 1)
             {
                 String strLanguage = context.getSession().getLanguage();
                String strPromoteStateError = EnoviaResourceBundle.getProperty(context,SUITE_KEY,CHECK_CURRENTCONFIGURATION, strLanguage);
                emxContextUtilBase_mxJPO.mqlNotice(context, strPromoteStateError);
             }
         }
         return iFlag;
    }
    
    
     
     /**
      *  This method evaluates the expression for the sub features..
      *
      * @param context the eMatrix <code>Context</code> object
      * @param strPositionString a string containing the positing string of the feature.
      * @param errorHolder         a ProductConfigurationHolder object
      * @param strFeatureType      a String giving the type of the feature
      * @param strFeatureName      a String giving the name of the feature.
      * @param strFeatureRevision  a String giving the revision of the feature.
      * @param strOptionType       a String giving the type of the option.
      * @param strOptionName       a String giving the name of the option.
      * @param strOptionRevision   a String goving the revision of the option.
      * @return String "TRUE" or "FALSE"
      * @throws Exception if the operation fails
      * @since ProductCentral 10.0.0.0
      * this could not be deprecated as referenced from migration JPO
      */
      protected static String evaluateExpressionForSubFeatures(Context context,
                                       String strPositionString,
                                       ProductConfigurationHolder errorHolder,
                                       String strFeatureType,String strFeatureName,String strFeatureRevision,
                                       String strOptionType,String strOptionName,String strOptionRevision)
             throws Exception {

             String strResult = STR_FALSE;
             String strLanguage = context.getSession().getLanguage();

             String strGeneralErrorMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                "emxProduct.Error.UnknownException",strLanguage);

             try {

                 String strFType     = null;
                 String strFName     = null;
                 String strFRevision = null;
                 String strOType     = null;
                 String strOName     = null;
                 String strOREvision = null;
                 boolean bFeatureInExp = false;
                 boolean bByPass = false;

                 FeatureHolder feature = null;
                 StringTokenizer st = new StringTokenizer(strPositionString);
                 boolean bFirstToken = true;
                 while (st.hasMoreTokens()) {
                     String strToken = st.nextToken();
                     int iToken = Integer.parseInt(strToken);
                     if(bFirstToken) {
                         feature = (FeatureHolder) (errorHolder.getFeatureList()).get(iToken);
                         strFType = feature.getType();
                         strFName = feature.getName();
                         strFRevision = feature.getRevision();
                         if(strFType.equals(strFeatureType) && strFName.equals(strFeatureName) && strFRevision.equals(strFeatureRevision)) {
                             bFeatureInExp = true;
                         }
                         // test for top level features for the case when top level feature itself is in expression
                         if(strFType.equals(strOptionType) && strFName.equals(strOptionName) && strFRevision.equals(strOptionRevision)) {
                             strResult = STR_TRUE;
                             break;
                         }
                     } else {
                         java.util.List subFeatureList = new ArrayList();
                         subFeatureList = feature.getSubFeatureList();
                         feature = null;
                         feature = (FeatureHolder) subFeatureList.get(iToken);
                         if(bFeatureInExp) {
                             strOType = feature.getType();
                             strOName = feature.getName();
                             strOREvision = feature.getRevision();
                             if(strOType.equals(strOptionType) && strOName.equals(strOptionName) && strOREvision.equals(strOptionRevision)) {
                                 strResult = STR_TRUE;
                                 bByPass = true;
                                 break;
                             }
                         }
                         if(!bByPass) {
                             strFType = feature.getType();
                             strFName = feature.getName();
                             strFRevision = feature.getRevision();
                             if(strFType.equals(strFeatureType) && strFName.equals(strFeatureName) && strFRevision.equals(strFeatureRevision)) {
                                 bFeatureInExp = true;
                             }
                         }
                     }// end of else
                     bFirstToken = false;
                 } // end of while

             } catch (Exception exp) {
                 //System.out.println("Exception in evaluateExpressionForSubFeatures-"+exp.toString());
                 throw new Exception(strGeneralErrorMessage + exp);
             }
             return strResult;
         }

     /**
      * Returns the Position String of the feature/options.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param strFeatureOptionID string containg the id of feture/option
      * @param errorHolder the ProductConfiguration holder object.
      * @return String Position String of the feature option.
      * @throws Exception if the operation fails
      * @since ProductCentral 10.0.0.0
      * this could not be deprecated as referenced from migration JPO
      */
      protected static String getFeatureOptionPositionString(Context context,
                                       String strFeatureOptionID,
                                       ProductConfigurationHolder errorHolder)
             throws Exception {

             String strSelectedOptionPositionString = null;
             String strLanguage = context.getSession().getLanguage();

             String strGeneralErrorMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                "emxProduct.Error.UnknownException",strLanguage);

             try {

                 FeatureHolder Allfeature = null;
                 FeatureHolder AllsubFeature = null;
                 List AllfeatureOptions = errorHolder.getFeatureOptionsForFlatViewWizard();
                 int iAllSize = AllfeatureOptions.size();
                 List AllsubFeatureList  = null;
                 for(int iAllCount = 0; iAllCount < iAllSize; iAllCount++) {
                     Allfeature = (FeatureHolder)AllfeatureOptions.get(iAllCount);
                     String strAllfeatureID = Allfeature.getId();

                     if(strAllfeatureID.equals(strFeatureOptionID)) {
                         strSelectedOptionPositionString = Allfeature.getPositionAsString();
                         break;
                     } else {
                         AllsubFeatureList = Allfeature.getOptionsForWizard();
                         int iSubFeatureSize = AllsubFeatureList.size();
                         for(int iAll = 0; iAll < iSubFeatureSize; iAll++) {
                             AllsubFeature = (FeatureHolder)AllsubFeatureList.get(iAll);
                             String strAllsubfeatureID = AllsubFeature.getId();
                             if(strAllsubfeatureID.equals(strFeatureOptionID)) {
                                 strSelectedOptionPositionString = AllsubFeature.getPositionAsString();
                                 break;
                             }
                         } // end of sub feature for
                     }
                 } // end of for

             } catch (Exception exp) {
                 //System.out.println("Exception in getFeatureOptionPositionString-"+exp.toString());
                 throw new Exception(strGeneralErrorMessage + exp);
             }
             return strSelectedOptionPositionString;
         }
      
      /**
       * isOptionSelected() method returns the boolean true/false depending on whether the feature/option
       * which is passed to this function as strpassString is a selected option for the productConfiguration
       * @param context context
       * @param strpassString
       * @param strProductConfigurationID
       * @return boolean
       * @since ProductCentral 10.0.5.0
       * this could not be deprecated as referenced from migration JPO
       */
           public static boolean isOptionSelected(Context context,String strpassString,String strProductConfigurationID)
               throws Exception
              {

                  boolean bReturnFlag=false;
                  String strLanguage = context.getSession().getLanguage();
                  String strGeneralErrorMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                                                                     "emxProduct.Error.UnknownException",strLanguage);

                  try
                      {
                          DomainObject productConfiguration = new DomainObject(strProductConfigurationID);

                          //getting the type,name,revision attributes of the feature object connected to
                          //productConfiguration through selectedOptions relationship

                          String strFeatType="relationship["
                                              + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO
                                              + "].businessobject."
                                              + DomainConstants.SELECT_TYPE;

                          String strFeatRev="relationship["
                                              + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO
                                              + "].businessobject."
                                              + DomainConstants.SELECT_REVISION;

                          String strFeatName="relationship["
                                              + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO
                                              + "].businessobject."
                                              + DomainConstants.SELECT_NAME;

                          String strParentFeatType="to["
                                              + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM
                                              + "].from."
                                              + DomainConstants.SELECT_TYPE;

                          String strParentFeatRev="to["
                                              + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM
                                              + "].from."
                                              + DomainConstants.SELECT_REVISION;

                          String strParentFeatName="to["
                                              + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM
                                              + "].from."
                                              + DomainConstants.SELECT_NAME;

                          StringList attributeList = new StringList();

                          StringList objSelects = new StringList();
                          objSelects.add(DomainConstants.SELECT_TYPE);
                          objSelects.add(DomainConstants.SELECT_ID);
                          attributeList.add(strFeatType);
                          attributeList.add(strFeatRev);
                          attributeList.add(strFeatName);
                          attributeList.add(strParentFeatType);
                          attributeList.add(strParentFeatRev);
                          attributeList.add(strParentFeatName);

                          StringList relationshipAttributeList = new StringList(1);

                          relationshipAttributeList.add("attribute["
                                                          + ProductLineConstants.ATTRIBUTE_QUANTITY
                                                          + "]");

//                          MapList lstSelectedOptions =
//                          productConfiguration.getRelatedObjects(
//                                                                  context,
//                                                          ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS,
//                                                                  "*",
//                                                                  attributeList,
//                                                                  relationshipAttributeList,
//                                                                  false,
//                                                                  true,
//                                                                  (short) 1,
//                                                                  "",
//                                                                  "");
                          String contextFeature = productConfiguration.getInfo(context,"to["+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
                                          + "].from."
                                          + ProductLineConstants.SELECT_ID);
                          DomainObject domProductConfigurationObject = new DomainObject(contextFeature);
                          
                          MapList lstSelectedOptionsFeature =
                              productConfiguration.getRelatedObjects(
                                                                      context,
                                                              ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS,
                                                                      "*",
                                                                      objSelects,
                                                                      null,
                                                                      false,
                                                                      true,
                                                                      (short) 1,
                                                                      "",
                                                                      "", 0);
                          MapList lstSelectedOptions = new MapList();
                          for (int i=0;i<lstSelectedOptionsFeature.size() ;i++ )
                          {
                              Map m=new HashMap();
                              HashMap hmTmp = new HashMap();
                              String strType=""; 
                              String level = "";
                              String relationship = "";
                              String id = "";
                              m=(Map)lstSelectedOptionsFeature.get(i);
                               strType=(String)m.get(DomainConstants.SELECT_TYPE);
                               id=(String)m.get(DomainConstants.SELECT_ID);
                               level = (String)m.get("level");
                               relationship = (String)m.get("relationship");
                               DomainObject objFL = new DomainObject(id);
                               if(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_FEATURES)){
                                   hmTmp.put("attribute[Quantity]","0.0");
                                   hmTmp.put("relationship[Feature List To].businessobject.revision",objFL.getInfo(context,"revision"));
                                   hmTmp.put("relationship[Feature List To].businessobject.name",objFL.getInfo(context,"name"));
                                   hmTmp.put("relationship[Feature List To].businessobject.type",strType);
                                   hmTmp.put("to[Feature List From].from.name",domProductConfigurationObject.getInfo(context,"name"));
                                   hmTmp.put("to[Feature List From].from.revision",domProductConfigurationObject.getInfo(context,"revision"));
                                   hmTmp.put("to[Feature List From].from.type",domProductConfigurationObject.getInfo(context,"type"));
                                   hmTmp.put("relationship",relationship);
                                   hmTmp.put("level",level);
                                   lstSelectedOptions.add(hmTmp);
                               }else if(strType.equalsIgnoreCase(ProductLineConstants.TYPE_FEATURE_LIST)){
                                   hmTmp.put("attribute[Quantity]",objFL.getInfo(context,"attribute[Quantity]"));
                                   hmTmp.put("relationship[Feature List To].businessobject.revision",objFL.getInfo(context,strFeatRev));
                                   hmTmp.put("relationship[Feature List To].businessobject.name",objFL.getInfo(context,strFeatName));
                                   hmTmp.put("relationship[Feature List To].businessobject.type",objFL.getInfo(context,strFeatType));
                                   hmTmp.put("to[Feature List From].from.name",objFL.getInfo(context,strParentFeatName));
                                   hmTmp.put("to[Feature List From].from.revision",objFL.getInfo(context,strParentFeatRev));
                                   hmTmp.put("to[Feature List From].from.type",objFL.getInfo(context,strParentFeatType));
                                   hmTmp.put("relationship",relationship);
                                   hmTmp.put("level",level);
                                   lstSelectedOptions.add(hmTmp);
                               }
                          }
                  //seperating the Type,Name,Revision fields from the strpassString which is a parameter to this method

                      String strFeatureOption      = strpassString;

                      String strFeatureType        = DomainConstants.EMPTY_STRING;
                      String strFeatureName        = DomainConstants.EMPTY_STRING;
                      String strintermediate       = DomainConstants.EMPTY_STRING;
                      String strFeatureRevision    = DomainConstants.EMPTY_STRING;
                      String strOptionType         = DomainConstants.EMPTY_STRING;
                      String strOptionName         = DomainConstants.EMPTY_STRING;
                      String strOptionRevision     = DomainConstants.EMPTY_STRING;


                      //to check if the expression is a featue option pair or just a feature.
                      // bOnlyFeature = false signifies that it is a feature~option pair.
                      boolean bOnlyFeature = true;
                      int iFOPair = strFeatureOption.length();
                      for(int i=0;i<iFOPair;i++) {
                              if(strFeatureOption.charAt(i)==FEATURE_OPTION_DELIMITER.charAt(0)) {
                                   bOnlyFeature = false;
                              }
                      }

                      // StringTokenizer is used to obtain type name revision of feature option pairs
                      // from strFeatureOption using FEATURE_TNR_DELIMITER and FEATURE_OPTION_DELIMITER.
                      StringTokenizer strTokenizer = new StringTokenizer(strFeatureOption, FEATURE_TNR_DELIMITER);

                      if (strTokenizer.hasMoreTokens()) {
                                     strFeatureType = (strTokenizer.nextToken()).trim();
                      }
                      if (strTokenizer.hasMoreTokens()) {
                                      strFeatureName = (strTokenizer.nextToken()).trim();
                      }
                      if (strTokenizer.hasMoreTokens()) {
                                      strintermediate = strTokenizer.nextToken();
                      }
                      if (strTokenizer.hasMoreTokens()) {
                                      strOptionName = (strTokenizer.nextToken()).trim();
                      }
                      if (strTokenizer.hasMoreTokens()) {
                                      strOptionRevision = (strTokenizer.nextToken()).trim();
                      }

                      strTokenizer = new StringTokenizer(strintermediate, FEATURE_OPTION_DELIMITER);

                      if (strTokenizer.hasMoreTokens()) {
                                      strFeatureRevision = (strTokenizer.nextToken()).trim();
                      }
                      if (strTokenizer.hasMoreTokens()) {
                                      strOptionType = (strTokenizer.nextToken()).trim();
                      }

                  //Feature TNR and Option TNR formed by concatenating
                  //seperate strings got by the above operations

                      String strFeatureTNR = strFeatureType+strFeatureName+strFeatureRevision;
                      String strOptionTNR = strOptionType+strOptionName+strOptionRevision;
                      Vector vecCommonValues = new Vector();
                      if(strFeatureType.equalsIgnoreCase("CommonGroup")){
                          //String strMqlCommand = "print connection \""+strFeatureName+"\" select frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].to."+"from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to."+DomainConstants.SELECT_ID+"  dump |";
                          String strCommonValues = MqlUtil.mqlCommand(context ,"print connection $1 select $2 dump $3",strFeatureName,"frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].to."+"from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to."+DomainConstants.SELECT_ID,ConfigurationConstants.DELIMITER_PIPE);
                          if (strCommonValues == null)
                          {
                              strCommonValues = "";
                          }
                          StringTokenizer strTokens = new StringTokenizer(strCommonValues,"|");
                          
                          while(strTokens.hasMoreTokens()){
                              
                              vecCommonValues.add((String)strTokens.nextToken()); 
                          }
                      }
                  //This Map m is used in the for loop below to traverse throught the Maplist
                  //lstSelectedOptions to get TNR details of each feature/options connected to the
                  //ProductConfiguration

                      Map m=null;
                      String strType="";
                      String strName="";
                      String strRev="";
                      String strTNR="";
                      String strParentType="";
                      String strParentName="";
                      String strParentRev="";
                      String strParentTNR="";

                  for (int i=0;i<lstSelectedOptions.size() ;i++ )
                  {
                       m=(Map)lstSelectedOptions.get(i);
                       strType=(String)m.get(strFeatType);
                       strName=(String)m.get(strFeatName);
                       strRev=(String)m.get(strFeatRev);
                       strTNR=strType+strName+strRev;

                       strParentType=(String)m.get(strParentFeatType);
                       strParentName=(String)m.get(strParentFeatName);
                       strParentRev=(String)m.get(strParentFeatRev);
                       strParentTNR=strParentType+strParentName+strParentRev;
                       if(strFeatureType.equalsIgnoreCase("CommonGroup")){
                           for(int k=0;k<vecCommonValues.size();k++){
                               String strFeature = (String)vecCommonValues.get(k);
                               DomainObject objFeature = new DomainObject(strFeature);
                               String strtype = objFeature.getInfo(context,DomainConstants.SELECT_TYPE);
                               String strname = objFeature.getInfo(context,DomainConstants.SELECT_NAME);
                               String strev = objFeature.getInfo(context,DomainConstants.SELECT_REVISION);
                               strFeatureTNR = strtype+strname+strev;
                               if (bOnlyFeature)
                               {
                                   if(strFeatureTNR.equals(strTNR)){
                                       bReturnFlag=true;
                                       break;
                                   }
                                   else{
                                       bReturnFlag=false;
                                   }

                               }
                               else
                               {
                                   if(strOptionTNR.equals(strTNR) && strFeatureTNR.equals(strParentTNR)){
                                           bReturnFlag=true;
                                           break;
                                   }
                                   else{
                                           bReturnFlag=false;
                                   }
                               }
                           }
                           
                       }
                       else{
                      if (bOnlyFeature)
                      {
                          if(strFeatureTNR.equals(strTNR)){
                              bReturnFlag=true;
                              break;
                          }
                          else{
                              bReturnFlag=false;
                          }

                      }
                      else
                      {
                          if(strOptionTNR.equals(strTNR) && strFeatureTNR.equals(strParentTNR)){
                                  bReturnFlag=true;
                                  break;
                          }
                          else{
                                  bReturnFlag=false;
                          }
                      }
                       }
                       if(bReturnFlag){
                           break;
                      }
                  } // ending for loop

              } // ending try
              catch(Exception exp) {
                      throw new Exception(strGeneralErrorMessage + exp);
                  }

              return bReturnFlag;
          }

    
    
    /**
     * calculateExpression() method evaluates the input expression containing feature/option pairs to True or false
     * This method does the function of separating each feature option pairs
     * from the expression obtained from the attribute of a Feature List
     * in the format fType::fName::fRevision~oType::oName::oRevision.
     * The expression is then send to findFeatureOptions() method which returns a
     * boolean depending on wheather user has selected this feature option
     * or not.The boolean obtained is used to form a mql command by replacing it back in the expression with its feature/options
     * which is then evaluated.The result obtained is returned
     * back as a String depending upon if the entire expression was true then "TRUE" else "FALSE".
     *
     * @param context context for this request
     * @param strObjectID Object ID on which the expression is to be evaluated
     * @param strExpression Expression containing feature/option pairs which are to be evaluated
     * @param errorHolder The bean instance for the product
     * @return String "TRUE" or "FALSE"
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * this could not be deprecated as referenced from migration JPO
     */

     public static String calculateExpression( Context context,
                                               String strObjectID,
                                               String strExpression,
                                               String strProductConfigurationID,
                                               ProductConfigurationHolder errorHolder)
        throws Exception {
        String strResult = STR_TRUE;
        String strLanguage = context.getSession().getLanguage();

        String strGeneralErrorMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
           "emxProduct.Error.UnknownException",strLanguage);

        try {
            String strFinalExpressionString = DomainConstants.EMPTY_STRING;
            String strTempExpression = strExpression;

            // check for knowing the expression is null or empty string
            // if not then proceed for validation
            if (strTempExpression != null && !strTempExpression.equals(DomainConstants.EMPTY_STRING)) {

                // the logic used here for validating the expression is based on the assumption that
                // expression obtained is valid and all feature option pairs are within "".
                // the expression is read character by charector untill it meets a " to obtain
                // a substring . if this substring is not null and not an empty string then it is passed
                // to the method findFeatureOptions() for verifying if this perticular feature option
                // pair is selected by the user or not.the boolean returned by that method is used to form
                // a mql command.

                int itemp = 0;
                int icount = 0;
                int ienterPoint = 0;
                int iTempExpressionLength = strTempExpression.length();
/**********Begin of Add by Yukthesh, Enovia MatrixOne for Bug #310587 on 20th Oct 2005********/
                boolean notflag=false;
                String strnotString = "NOT ";
/**********End of Add by Yukthesh, Enovia MatrixOne for Bug #310587 on 20th Oct 2005********/
                for (itemp = 0; itemp < iTempExpressionLength; itemp++) {
                // Modified by Enovia MatrixOne for Bug # 306380 Date 06/22/2005
                    if ((strTempExpression.charAt(itemp)
                        == STR_QUOTES.charAt(0))
                        || (strTempExpression.charAt(itemp)
                            == OPEN_BRACE)
                        || (strTempExpression.charAt(itemp)
                            == CLOSE_BRACE)
                        || (itemp == (iTempExpressionLength - 1))) {

                        // ienterPoint is used as a mark point . althgough there can't be a case where
                        // there will an expression with just one feature option pair without "" since
                        // i am assuming the expression to be completely valid. in that case this part
                        // of code wont run .if it happens it is handelled using this mark point.
                        // we have one if condition below to handle this.
                        ienterPoint = 1;

                        // Feature option pair is obtained here from the expression as strpassString.

                        String strpassString =
                            strTempExpression.substring(icount, itemp).trim();
                       if(strTempExpression.charAt(0)==strnotString.charAt(0))
                        {if(strpassString.equalsIgnoreCase("NOT"))
                         {
                            notflag=true;
                         }
                        }

                        if (!strpassString.equals(DomainConstants.EMPTY_STRING)
                            && strpassString != null) {


                            // these two booleans are used to check if strpassString obtained has valid
                            // feature option pairs.
                            boolean bfCheckPoint = false;
                            boolean boCheckPoint = false;
                            int iCount = strpassString.length();
                            for(int i=0;i<iCount;i++) {
                                  if(strpassString.charAt(i)==FEATURE_TNR_DELIMITER.charAt(0)) {
                                         bfCheckPoint=true;
                                  }
                                  // since feature without an option is also possible .
                                 // if(strpassString.charAt(i)==FEATURE_OPTION_DELIMITER.charAt(0)) {
                                         boCheckPoint=true;
                                 // }
                            }
                            if(bfCheckPoint==true && boCheckPoint==true) {

                            // calling isOptionSelected returns true/false depending on strpassString is a selectedOption
                            // of the Productconfiguration or not.

                            boolean bcheckResult;

                            if(strProductConfigurationID.equals("")) {
                                bcheckResult = findFeatureOptions(context,strpassString,errorHolder);
                            }
                            else {
                                 bcheckResult = isOptionSelected(context,strpassString,strProductConfigurationID);
                            }

                                // these two if conditions are used to make strFinalExpressionString used
                                // in the mql command to be used in evaluate expression.

                                if(strFinalExpressionString!=null && !strFinalExpressionString.equals(DomainConstants.EMPTY_STRING)) {
                                  for(int j=0;j<strFinalExpressionString.length();j++) {
                                       if(strFinalExpressionString.charAt(j)==STR_QUOTES.charAt(0)) {
                                             strFinalExpressionString = strFinalExpressionString.substring(0,j);

                                       }
                                  }
                                }

                                // the boolean returned above is used to make strFinalExpressionString.
                                if(itemp<iTempExpressionLength-1) {
                                    if (bcheckResult == true) {
                                                strFinalExpressionString =  strFinalExpressionString.concat(STR_TRUE)+
                                                                    strTempExpression.substring(itemp+1, iTempExpressionLength);
                                     } else if (bcheckResult == false) {
                                     strFinalExpressionString =  strFinalExpressionString.concat(STR_FALSE)+
                                                                    strTempExpression.substring(itemp+1, iTempExpressionLength);
                                    }
                                } else {
                                    if (bcheckResult == true) {
                                         strFinalExpressionString =  strFinalExpressionString.concat(STR_TRUE);
                                    } else if (bcheckResult == false) {
                                         strFinalExpressionString =  strFinalExpressionString.concat(STR_FALSE);
                                    }
                                }

                            }// end of checkpoint if
                        } // end of strpassString if
                        icount = itemp + 1;
                    } // End of strTempExpression if
                } // End of for

                // this is the part of code which will be used in case we have an expression
                // with just one feature option pair that too without "" or braces. although
                // this will never happen.
                if (ienterPoint == 0) {
                    boolean bcheckResult =
                        findFeatureOptions(context,
                            strTempExpression,
                            errorHolder);
                    if (bcheckResult == true) {
                        strFinalExpressionString =
                            strFinalExpressionString.concat(STR_TRUE);
                    } else if (bcheckResult == false) {
                        strFinalExpressionString =
                            strFinalExpressionString.concat(STR_FALSE);
                    }
                }

   /**********Begin of Add by Yukthesh, Enovia MatrixOne for Bug #310587 on 20th Oct 2005********/
                int lenFinalString= strFinalExpressionString.length();
                int countClosingBraces=0;
                int countOpeningBraces=0;
                String strUpdatedFinalExpression = "";
                String strSymbOpenBraces = "(";
                String strSymbCloseBraces = ")";
                StringBuffer sbFinalUpdatedExpression = new StringBuffer();

                if(notflag)
                {
                    sbFinalUpdatedExpression.append(strnotString);
                }

                for(int i=0;i<lenFinalString;i++)
                {
                    if(strFinalExpressionString.charAt(i)==strSymbOpenBraces.charAt(0))
                    {
                        //Modified by Vibhu for Bug 311446 on 11/2/2005
                        countOpeningBraces++;
                    }
                    else if(strFinalExpressionString.charAt(i)==strSymbCloseBraces.charAt(0))
                    {
                        countClosingBraces++;
                    }

                }
                if(countOpeningBraces<countClosingBraces)
                {
                    StringBuffer sbFinalExpression = new StringBuffer();
                    String strsymb = "(";
                    sbFinalExpression.append(strsymb)
                                     .append(strFinalExpressionString);
                    strUpdatedFinalExpression = sbFinalExpression.toString();
                }
                else
                {
                    strUpdatedFinalExpression= strFinalExpressionString;
                }

                sbFinalUpdatedExpression.append(strUpdatedFinalExpression);
                strFinalExpressionString =sbFinalUpdatedExpression.toString();

/**********End of Add by Yukthesh, Enovia MatrixOne for Bug #310587 on 20th Oct 2005********/

                // Execute the MQL command to evaluate the expression strFinalExpressionString

                MQLCommand mqlCommand = new MQLCommand();
                /*String strcommand =
                    "evaluate expression \""
                        + strFinalExpressionString
                        + "\" on bus "
                        + strObjectID;*/

                String strcommand ="evaluate expression $1 on bus $2";

                boolean bMQLResult = false;
                //bMQLResult =  mqlCommand.executeCommand(context, strcommand);
                bMQLResult =  mqlCommand.executeCommand(context, strcommand, strFinalExpressionString, strObjectID);
                if (bMQLResult == true) {
                    String strgetResult = mqlCommand.getResult();
                    if (strgetResult.trim().equalsIgnoreCase(STR_FALSE)) {
                        strResult = STR_FALSE;
                    } else if (strgetResult.trim().equalsIgnoreCase(STR_TRUE)) {
                        strResult = STR_TRUE;
                    }
                } else {
                    String strEvaluateExpressionFailure = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                       "emxProduct.Error.EvaluateExpressionFailure",strLanguage);
                    throw new Exception(strEvaluateExpressionFailure);
                }

            } // End of outer if braces
        } catch (Exception exp) {
            //System.out.println("Exception in calculate expression -"+exp.toString());
            throw new Exception(strGeneralErrorMessage + exp);
        }
         return strResult;
    }

    
     /**
      * This method finds wheather the Feature or Option in an expression are available in the Selected Options.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param strFeatureOption String containing the TNR for the Feature Option to be found out
      * @param errorHolder the bean instance
      * @return boolean true or false
      * @throws Exception if the operation fails
      * @since ProductCentral 10.0.0.0
      * this could not be deprecated as referenced from migration JPO
      *
      */
      public static boolean findFeatureOptions( Context context,
                                                   String strFeatureOption,
                                                   ProductConfigurationHolder errorHolder)
             throws Exception {
             boolean bResult = false;
             String strLanguage = context.getSession().getLanguage();

             String strGeneralErrorMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                "emxProduct.Error.UnknownException",strLanguage);

             try {
                 String strFeatureType        = DomainConstants.EMPTY_STRING;
                 String strFeatureName        = DomainConstants.EMPTY_STRING;
                 String strintermediate       = DomainConstants.EMPTY_STRING;
                 String strFeatureRevision    = DomainConstants.EMPTY_STRING;
                 String strOptionType         = DomainConstants.EMPTY_STRING;
                 String strOptionName         = DomainConstants.EMPTY_STRING;
                 String strOptionRevision     = DomainConstants.EMPTY_STRING;

                 //to check if the expression is a featue option pair or just a feature.
                 // bOnlyFeature = false signifies that it is a feature~option pair.
                 boolean bOnlyFeature = true;
                 int iFOPair = strFeatureOption.length();
                 for(int i=0;i<iFOPair;i++) {
                   if(strFeatureOption.charAt(i)==FEATURE_OPTION_DELIMITER.charAt(0)) {
                      bOnlyFeature = false;
                   }
                 }

                 // StringTokenizer is used to obtain type name revision of feature option pairs
                 // from strFeatureOption using FEATURE_TNR_DELIMITER and FEATURE_OPTION_DELIMITER.
                 StringTokenizer strTokenizer =
                     new StringTokenizer(strFeatureOption, FEATURE_TNR_DELIMITER);

                 if (strTokenizer.hasMoreTokens()) {
                     strFeatureType = (strTokenizer.nextToken()).trim();
                 }
                 if (strTokenizer.hasMoreTokens()) {
                     strFeatureName = (strTokenizer.nextToken()).trim();
                 }
                 if (strTokenizer.hasMoreTokens()) {
                     strintermediate = strTokenizer.nextToken();
                 }

                 if (strTokenizer.hasMoreTokens()) {
                     strOptionName = (strTokenizer.nextToken()).trim();
                 }
                 if (strTokenizer.hasMoreTokens()) {
                     strOptionRevision = (strTokenizer.nextToken()).trim();
                 }

                 strTokenizer =
                     new StringTokenizer(strintermediate, FEATURE_OPTION_DELIMITER);
                 if (strTokenizer.hasMoreTokens()) {
                     strFeatureRevision = (strTokenizer.nextToken()).trim();
                 }
                 if (strTokenizer.hasMoreTokens()) {
                     strOptionType = (strTokenizer.nextToken()).trim();
                 }

                 //modified to use the method available in bean
                 ArrayList arrSelectedOptionsArrayList = (ArrayList) errorHolder.getAllSelectedOptionsForInteractiveRules();

                 int iSelectedOptionsize = arrSelectedOptionsArrayList.size();
                 int iCount;

                 for (iCount = 0; iCount < iSelectedOptionsize; iCount++) {
                     ArrayList arrVelement =
                         (ArrayList) arrSelectedOptionsArrayList.get(iCount);
                     String strSelectedFeatureType =
                         arrVelement.get(0).toString().trim();
                     String strSelectedFeatureName =
                         arrVelement.get(1).toString().trim();
                     String strSelectedFeatureRevision =
                         arrVelement.get(2).toString().trim();
                     String strSelectedOptionType =
                         arrVelement.get(14).toString().trim();
                     String strSelectedOptionName =
                         arrVelement.get(15).toString().trim();
                     String strSelectedOptionRevision =
                         arrVelement.get(16).toString().trim();
                     String strSelectedOptionId =
                         arrVelement.get(17).toString().trim();

                     // to check if user have selected the feature option present in
                     // the perticular expression of a rule.
                     if(bOnlyFeature) {
                         // since we have only feature and no option hence only feature type
                         // name revision is to be checked.
                         if (strSelectedFeatureType.equals(strFeatureType)
                         && strSelectedFeatureName.equals(strFeatureName)
                         && strSelectedFeatureRevision.equals(strFeatureRevision)) {
                            bResult = true;
                            break;
                         }
                     } else {
                         if (strSelectedFeatureType.equals(strFeatureType)
                         && strSelectedFeatureName.equals(strFeatureName)
                         && strSelectedFeatureRevision.equals(strFeatureRevision)
                         && strSelectedOptionType.equals(strOptionType)
                         && strSelectedOptionName.equals(strOptionName)
                         && strSelectedOptionRevision.equals(strOptionRevision)) {
                            bResult = true;
                            break;
                         }
                     }

                     // Code for considering the fact that subfeatrues and top level features
                     // can also be in expression.Hence these features/subfeatures also needs
                     // to be evaluated.but since they are not available in arrSelectedOptionsArrayList
                     // we need to track them from pconf bean by going down the tree upto selected
                     // option using OptionId.
                     String strSelectedOptionPositionString = getFeatureOptionPositionString(context,
                                                 strSelectedOptionId,errorHolder);

                     // if strSelectedOptionPositionString is null then it means the selected option is a
                     // Technical feature/option hence for it the entire root/tree need not be checked.
                     if(strSelectedOptionPositionString!=null && !strSelectedOptionPositionString.equals("") && !strSelectedOptionPositionString.equals("null")) {
                         String strExpressionEvaluation = evaluateExpressionForSubFeatures(context,
                                                     strSelectedOptionPositionString,
                                                     errorHolder,
                                                     strFeatureType,strFeatureName,strFeatureRevision,
                                                     strOptionType,strOptionName,strOptionRevision);
                         if(strExpressionEvaluation.equals(STR_TRUE)) {
                             bResult = true;
                             break;
                         }
                     }//end of if for strSelectedOptionPositionString!=null

                 }//end of for

             } catch (Exception exp) {
                 //System.out.println("Exception in findFeatureOptions"+exp.toString());
                 throw new Exception (strGeneralErrorMessage + exp );
             }
             return bResult;
         }
 

 /*
        * getting the selected options for the product Configuration
        */
        /**
         * this could not be deprecated as referenced from migration JPO
         */
    public static MapList getSelectedOptions(Context context, String objectId)
 throws Exception{
             String strComma = ",";
             String relationshipName =
               ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS;
             String strType = ProductLineConstants.TYPE_FEATURE_LIST + strComma
 + ProductLineConstants.TYPE_FEATURES;
             StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
             objectSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");
             objectSelects.add(DomainConstants.SELECT_NAME);
             StringList relSelects =
               new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
             short shRecursionLevel = 1;

             DomainObject domainObject = new DomainObject(objectId);

             MapList relBusObjPageList =
               domainObject.getRelatedObjects(
                 context,
                 relationshipName,
                 strType,
                 objectSelects,
                 relSelects,
                 false,
                 true,
                 shRecursionLevel,
                 DomainConstants.EMPTY_STRING,
                 DomainConstants.EMPTY_STRING, 0);



             return relBusObjPageList;
         }



  /**
  * This method is for Evaluating the Part Inclusion Rules
  * It gets the Inclusion Rule from the GBOM object and fetches the expression
  * for it. Then it evaluates the expression to true or false.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param strObjectID - ObjectID of the GBOM object
  * @param strProductConfigurationID - ProductConfiguration ID of the ProductConfiguration for which the GBOM is generated.
  * @param errorHolder - the bean instance
  *
  * @return String "TRUE" or "FALSE"
  * @throws Exception if the operation fails
  * @since ProductCentral 10.6
  * this could not be deprecated as referenced from migration JPO
  */
 public static String calculateExpression( Context context,
                                                String strObjectID,
                                                String strProductConfigurationID,
                                                ProductConfigurationHolder errorHolder)throws Exception {

   String commonvaluesSelectable ="frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].to.id";
   DomainObject.MULTI_VALUE_LIST.add(commonvaluesSelectable);

         String strResult = STR_TRUE;
         String strLanguage = context.getSession().getLanguage();
         String strGeneralErrorMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
            "emxProduct.Error.UnknownException",strLanguage);

        try {
          //getting the selected options on the product configuration and putting it in the map
            HashMap selectedOptions = new HashMap();

            MapList selectedOptionsMaplist = getSelectedOptions( context,  strProductConfigurationID);
            Iterator it = selectedOptionsMaplist.iterator();
            while (it.hasNext()) {
            	Map so_Map = (Map) it.next();
                selectedOptions.put(so_Map.get(DomainConstants.SELECT_ID),"");
                selectedOptions.put(so_Map.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id"),"");

            }
            //Start IR-042898V6R2011 - need to add context object id in selected options list to evaluate rule 
            DomainObject domPC = new DomainObject(strProductConfigurationID);
            String strContextId = domPC.getInfo(context, "to["
                    + ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
                    + "].from.id");
            selectedOptions.put(strContextId, "");
            //End IR-042898V6R2011 - need to add context object id in selected options list to evaluate rule 
            //gettiing the right expression attribute on inclusion rule which is connected to GBOM

         DomainObject dom = DomainObject.newInstance(context);
         dom.setId(strObjectID);
         String strAttRESlectable ="to["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION + "].from.attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]";


         String strRightExpression= dom.getInfo(context, strAttRESlectable);

         String boolleanExpression=strRightExpression;
        if (!(strRightExpression == null || ("".equals(strRightExpression)) || ("null".equals(strRightExpression)))) {

            StringList tempList = FrameworkUtil.splitString(strRightExpression," ");
            String  b_rIDS="";
          for(int k=0;k<tempList.size();k++) {
              b_rIDS = ((String)tempList.get(k)).trim();
            //Modified For Bug No. IR-042422V6R2011
            if(b_rIDS.length()>0 && !(b_rIDS.equals(AND)||b_rIDS.equals(OR)||b_rIDS.equals(NOT)||b_rIDS.charAt(0)==OPEN_BRACE||b_rIDS.charAt(0)==CLOSE_BRACE)){
            //if it starts with r then it is common group relationship
            if(b_rIDS.startsWith("R")){

             MapList maplist  = DomainRelationship.getInfo(context,new String[]{b_rIDS.substring(1)},new StringList(commonvaluesSelectable));
              StringList CG_FL_ids = (StringList)((Map)maplist.get(0)).get(commonvaluesSelectable);

              for(int j=0;j<CG_FL_ids.size();j++){
                if(selectedOptions.keySet().contains(CG_FL_ids.get(j))){
                   boolleanExpression=boolleanExpression.replace(b_rIDS, STR_TRUE);
                break;
               }
            }
          //if  c_id is not yet replaced it will be replaced by false
            boolleanExpression=boolleanExpression.replace(b_rIDS, STR_FALSE);

          }else{
        //obIDSList.add(b_rIDS.substring(1));
        // if BUS id  present in selected options replace it with TRUE else FALSE
            if(selectedOptions.keySet().contains(b_rIDS.substring(1))){
                 boolleanExpression=boolleanExpression.replace(b_rIDS, STR_TRUE);
              }else{
              boolleanExpression= boolleanExpression.replace(b_rIDS, STR_FALSE);
            }
          }
       }



       }


                 MQLCommand mqlCommand = new MQLCommand();

                 StringBuffer strBuffer = new StringBuffer("");
                 strBuffer = strBuffer.append("evaluate expression \"")
                             .append(boolleanExpression)
                             .append("\" on bus ")
                             .append(strObjectID);

                 String strCommand = strBuffer.toString();
                 boolean bMQLResult = false;
                 bMQLResult =  mqlCommand.executeCommand(context, strCommand);

                 if (bMQLResult == true) {
                     String strgetResult = mqlCommand.getResult();
                     if (strgetResult.trim().equalsIgnoreCase(STR_FALSE)) {
                         strResult = STR_FALSE;
                     } else if (strgetResult.trim().equalsIgnoreCase(STR_TRUE)) {
                         strResult = STR_TRUE;
                     }
                 } else {
                     String strEvaluateExpressionFailure = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                        "emxProduct.Error.EvaluateExpressionFailure",strLanguage);
                     throw new Exception(strEvaluateExpressionFailure);
                 }
             }

         }
         catch (Exception exp) {
             //exp.printStackTrace();
             throw new Exception(strGeneralErrorMessage + exp);
         }

         return strResult;
    }


/**
     * This method is called from gbomParseRulesIdBased().
     * It does the actual evaluation of part Inclusion Rules against the selected options
     * & returns True or False.
     * 
     * @param context
     * @param strObjectID
     * @param strRightExpression
     * @param selectedOptions
     * @return
     *      returns True or False
     * @throws Exception
     * this could not be deprecated as referenced from migration JPO
     */
    public static String calculateExpressionIdBased(Context context,
            String strObjectID, String strRightExpression, HashMap selectedOptions) throws Exception {

        String commonvaluesSelectable = "frommid["
                + ConfigurationConstants.RELATIONSHIP_COMMON_VALUES + "].to.id";
        DomainObject.MULTI_VALUE_LIST.add(commonvaluesSelectable);

        String strResult = STR_TRUE;
        String strLanguage = context.getSession().getLanguage();
        String strGeneralErrorMessage = EnoviaResourceBundle.getProperty(
                context,SUITE_KEY,"emxProduct.Error.UnknownException",strLanguage);
        try {            
            String boolleanExpression = strRightExpression;
            if (!(strRightExpression == null || ("".equals(strRightExpression)) || ("null"
                    .equals(strRightExpression)))) {

                StringList tempList = FrameworkUtil.splitString(
                        strRightExpression, " ");
                String b_rIDS = "";
                for (int k = 0; k < tempList.size(); k++) {
                    b_rIDS = ((String) tempList.get(k)).trim();
                  //Modified For Bug No. IR-042422V6R2011
                    if (b_rIDS.length() > 0
                            && !(b_rIDS.equals(AND) || b_rIDS.equals(OR)
                                    || b_rIDS.equals(NOT)
                                    || b_rIDS.charAt(0) == OPEN_BRACE || b_rIDS.charAt(0) == CLOSE_BRACE)) {
                        // if it starts with r then it is common group
                        // relationship
                        if (b_rIDS.startsWith("R")) {

                            MapList maplist = DomainRelationship.getInfo(
                                    context,
                                    new String[] { b_rIDS.substring(1) },
                                    new StringList(commonvaluesSelectable));
                            //START - Modified For Bug No. IR-042422V6R2011
                            if(((Map) maplist.get(0)).get(commonvaluesSelectable) instanceof StringList)
                            {
                            	StringList CG_FL_ids = (StringList) ((Map) maplist.get(0)).get(commonvaluesSelectable);
                            for (int j = 0; j < CG_FL_ids.size(); j++) {
                                if (selectedOptions.keySet().contains(
                                        CG_FL_ids.get(j))) {
                                    boolleanExpression = boolleanExpression
                                            .replace(b_rIDS, STR_TRUE);
                                    break;
                                }
                            }
                            	
                            }else
                            {
	                            String CG_FL_ids = (String) ((Map) maplist.get(0)).get(commonvaluesSelectable);
	                            	if (selectedOptions.keySet().contains(CG_FL_ids)) {
	                                    boolleanExpression = boolleanExpression
	                                            .replace(b_rIDS, STR_TRUE);
	                                    break;
	                                }
	                        }
                            //END - Modified For Bug No. IR-042422V6R2011
                            // if c_id is not yet replaced it will be replaced
                            // by false
                            boolleanExpression = boolleanExpression.replace(
                                    b_rIDS, STR_FALSE);

                        } else {
                            // obIDSList.add(b_rIDS.substring(1));
                            // if BUS id present in selected options replace it
                            // with TRUE else FALSE
				// Added code to fix IR-021265V6R2010x - IVU
                        	String strOID =  b_rIDS.substring(1);
                        	DomainObject domObj = new  DomainObject(strOID);
                        	StringList strFeatureList = new StringList();
                        	if(mxType.isOfParentType(context, domObj.getInfo(context,DomainObject.SELECT_TYPE),ConfigurationConstants.TYPE_FEATURES)){
                        		strFeatureList = domObj.getInfoList(context,"to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.id");
                        	}
                        	
                        	Iterator itr = selectedOptions.keySet().iterator();
                        	boolean isFeature = false;
                        	for (Iterator iterator = itr; iterator
									.hasNext();) {
								String object = (String) iterator.next();
								if(strFeatureList.contains(object)){
									isFeature = true;
									break;
								}
							}
                        	
                            if (selectedOptions.keySet().contains(
                                    b_rIDS.substring(1)) 
                                    || isFeature) {
                                boolleanExpression = boolleanExpression
                                        .replace(b_rIDS, STR_TRUE);
                            } else {
                                boolleanExpression = boolleanExpression
                                        .replace(b_rIDS, STR_FALSE);
                            }
                        }
                    }

                }

                MQLCommand mqlCommand = new MQLCommand();

                StringBuffer strBuffer = new StringBuffer("");
                strBuffer = strBuffer.append("evaluate expression \"").append(
                        boolleanExpression).append("\" on bus ").append(
                        strObjectID);

                String strCommand = strBuffer.toString();
                boolean bMQLResult = false;
                bMQLResult = mqlCommand.executeCommand(context, strCommand);

                if (bMQLResult == true) {
                    String strgetResult = mqlCommand.getResult();
                    if (strgetResult.trim().equalsIgnoreCase(STR_FALSE)) {
                        strResult = STR_FALSE;
                    } else if (strgetResult.trim().equalsIgnoreCase(STR_TRUE)) {
                        strResult = STR_TRUE;
                    }
                } else {
                    String strEvaluateExpressionFailure = EnoviaResourceBundle.getProperty(
                            context,SUITE_KEY,
                            "emxProduct.Error.EvaluateExpressionFailure",strLanguage);
                    throw new Exception(strEvaluateExpressionFailure);
                }
            }

        } catch (Exception exp) {
            // exp.printStackTrace();
            throw new Exception(strGeneralErrorMessage + exp);
        }

        return strResult;
    }

}
