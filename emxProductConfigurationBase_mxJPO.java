/*
 * emxProductConfigurationBase
 *
 * Copyright (c) 2004-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.30.2.8.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental${CLASSNAME}.java 1.5 Mon Oct 08 16:16:54 2007 GMT ds-mberi Experimental$
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.SelectConstants;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.configuration.ConfigurableRulesUtil;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationFeature;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.IProductConfigurationFeature;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.configuration.ProductConfiguration;
import com.matrixone.apps.configuration.RuleProcess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.effectivity.EffectivityFramework;
import com.matrixone.apps.framework.ui.UITableGrid;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;

/**
 * The <code>emxProductConfigurationBase</code> class holds methods for
 * executing JPO operations related to objects of the type ProductConfiguration.
 * @version ProductCentral 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxProductConfigurationBase_mxJPO extends emxDomainObject_mxJPO
{
  /** Alias used for dot. */
  public static final String strDot = ".";
  /**
  * Alias for the string Standard configuration.
  */
  protected static final String STANDARDCONFIGURATION = "Standard Configuration";
  /**
  * Alias for the string Custom configuration.
  */
  protected static final String CUSTOMCONFIGURATION = "Custom Configuration";
  /**
  * Alias for the string mode.
  */
  protected static final String MODE = "mode";
    /**
  * Alias for the string requestMap.
  */
  protected static final String REQUESTMAP = "requestMap";

  /** A string constant for symbolic name of state of the Product on which a configuration is based */
  public static final String SYMB_state_Release = "state_Release";

  /** A string constant for symbolic name of Policy of the Product */
  public static final String SYMB_policy_Product = "policy_Product";

  /*
  * A string constant with the value "emxProduct.Alert.CurrentConfigurationValidationFailed".
  */
  public static final String CHECK_CURRENTCONFIGURATION = "emxProduct.Alert.CurrentConfigurationValidationFailed";
  public static final String RESOURCEBUNDLE = "emxConfigurationStringResource";
  /**
   * Alias used for Comma Character.
   */
  protected static final String STR_COMMA =   ",";
  /**
   * Alias used for String Resources.
   */
  public static final String RESOURCE_BUNDLE_PRODUCTS_STR = "emxProductLineStringResource";

  public static final String TRUE = "true";
  /*
   * For PC / FO
   */

  //attributes for Filter Binary
  public static final String ATTRIBUTE_FILTER_COMPILED_FORM = PropertyUtil.getSchemaProperty("attribute_FilterCompiledForm");
  public static final String ATTRIBUTE_FILTER_EXPRESSION_BINARY = PropertyUtil.getSchemaProperty("attribute_FilterExpressionBinary");
  public static final String ATTRIBUTE_FILTER_ORDERED_CRITERIA = PropertyUtil.getSchemaProperty("attribute_FilterOrderedCriteria");
  public static final String ATTRIBUTE_FILTER_ORDERED_CRITERIA_DICTIONARY = PropertyUtil.getSchemaProperty("attribute_FilterOrderedCriteriaDictionary");

  public static final String COMPILED_BINARY_EXPR = "compiledBinary";
  public static final String BINARY_EXPR = "binaryExpr";
  public static final String CRITERIA_CFGKEY = "criteriaCfgKey";
  public static final String CRITERIA_DICTIONARY = "criteriaDictionaryKey";
  public static final String SUITE_KEY = "Configuration";
  
  public static final String CONFIGURATION_SELECTION_STATUS_CHOSEN = "Chosen";
  public static final String CONFIGURATION_SELECTION_STATUS_DEFAULT = "Default";
  public static final String CONFIGURATION_SELECTION_STATUS_REQUIRED = "Required";
  /**
   * Create a new emxProductConfigurationBase object from a given id.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since ProductCentral 10-0-0-0
   */


  public emxProductConfigurationBase_mxJPO(Context context, String[] args)
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
   * @throws Exception when problems occurred in the ProductCentral 10-0-0-0
   * @since ProductCentral 10-0-0-0
   */
  public int mxMain(Context context, String[] args) throws Exception
  {
    if (!context.isConnected())
    {
     String strContentLabel = EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
      throw new Exception(strContentLabel);
    }
    return 0;
  }

  /**
   * Get the list of all Product Configuration on the Desktop.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - HashMap containing one String entry for the key "objectId"
   * @return MapList of Product Configurations
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getAllProductConfigurations(Context context, String[] args)
    throws Exception
  {
    HashMap parameterMap = (HashMap) JPO.unpackArgs(args);
    String objectId = (String) parameterMap.get("objectId");

    MapList relBusObjPageList = new MapList();
    StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    StringList relSelects =
      new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    short shRecursionLevel = 1;

    StringBuffer sbBuffer = new StringBuffer();

    // Modified to pass rels to get the product configurations under Products & Product Lines
    sbBuffer.append(ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION);
    sbBuffer.append(STR_COMMA);
    sbBuffer.append(ProductLineConstants.RELATIONSHIP_STANDARD_ITEM);
    sbBuffer.append(STR_COMMA);
    sbBuffer.append(ProductLineConstants.RELATIONSHIP_CUSTOM_ITEM);

    String strRelPattern = sbBuffer.toString();

    if (objectId == null)
    {
      relBusObjPageList =
        DomainObject.findObjects(
          context,
          ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
          DomainConstants.QUERY_WILDCARD,
          null,
          objectSelects);
    }
    else
    {
      DomainObject domainObject = new DomainObject(objectId);
      relBusObjPageList =
        domainObject.getRelatedObjects(
          context,
          strRelPattern,
          ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
          objectSelects,
          relSelects,
          false,
          true,
          shRecursionLevel,
          DomainConstants.EMPTY_STRING,
          DomainConstants.EMPTY_STRING, 0);
    }
    return relBusObjPageList;
  }

  /**
   * Get the list of All Product Configuration that are owned by the context user.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - HashMap containing one String entry for the key "objectId"
   * @return MapList of Product Configurations
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getOwnedProductConfigurations(
    Context context,
    String[] args)
    throws Exception
  {
    HashMap parameterMap = (HashMap) JPO.unpackArgs(args);
    String objectId = (String) parameterMap.get("objectId");

    MapList relBusObjPageList = new MapList();
    StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    // Start IR-224104V6R2014x ,Added for fixing IR
    objectSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION+"].from.name");
    objectSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION+"].from.revision");
    objectSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION+"].from.id");
    objectSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.name");
    objectSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.type");
    // End IR-224104V6R2014x
    String whereExpression = DomainConstants.SELECT_OWNER + "=='";
    whereExpression = whereExpression.concat(context.getUser().concat("'"));
    StringList relSelects =
      new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    short shRecursionLevel = 1;

    StringBuffer sbBuffer = new StringBuffer();

    // Modified to pass rels to get the product configurations under Products & Product Lines
    sbBuffer.append(ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION);
    sbBuffer.append(STR_COMMA);
    sbBuffer.append(ProductLineConstants.RELATIONSHIP_STANDARD_ITEM);
    sbBuffer.append(STR_COMMA);
    sbBuffer.append(ProductLineConstants.RELATIONSHIP_CUSTOM_ITEM);

    String strRelPattern = sbBuffer.toString();

    if (objectId == null || "".equals(objectId))
    {
      relBusObjPageList =
        DomainObject.findObjects(
          context,
          ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
          DomainConstants.QUERY_WILDCARD,
          whereExpression,
          objectSelects);
    }
    else
    {
      DomainObject domainObject = new DomainObject(objectId);
      relBusObjPageList =
        domainObject.getRelatedObjects(
          context,
          strRelPattern,
          ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
          objectSelects,
          relSelects,
          false,
          true,
          shRecursionLevel,
          whereExpression,
          DomainConstants.EMPTY_STRING, 0);
    }
    return relBusObjPageList;
  }

  /**
   * Get the list of Precise BOM that belong to the Product Configuration.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - HashMap containing one String entry for the key "objectId"
   * @return MapList of Precise BOM
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   */




 public Vector getLevelOfPartsInPrecBOM(Context context, String[] args)
    throws Exception
 {
		Vector vectorLevel = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Iterator listIterator = objectList.iterator();
		String level = null;
		while (listIterator.hasNext()) {
			Hashtable ptrHashTable = (Hashtable) listIterator.next();
			level = (String) ptrHashTable.get("partLevel");
			if (level == null) {
				vectorLevel.addElement("0");
			} else {
				vectorLevel.addElement(level);
			}
		}
		return (vectorLevel);
}

 private String generatePartLevel(Context context, String currLevelValue, boolean parentNeeded){

	 String newLevel = "";
	 String firstPart = "";
	 int lastDotIndex = 0;

	 if(currLevelValue == null){
		 return newLevel;
	 }

	 if(parentNeeded){
			newLevel = currLevelValue + ".1";
	 }
	 else{
		 if(currLevelValue.contains(".")){
			 lastDotIndex = currLevelValue.lastIndexOf(".") + 1;
			 firstPart = currLevelValue.substring(0, lastDotIndex);
			 newLevel = Integer.toString(Integer.valueOf(currLevelValue.substring(lastDotIndex)) + 1);
			 newLevel = firstPart + newLevel;
		 }
		 else{
			 newLevel = Integer.toString((Integer.valueOf(currLevelValue) + 1));
		 }
	 }

	 return newLevel;
 }

/**
 * Get all the Parts connected to Product Configuration by "Precise BOM" relationship
 *
 * @param  context the eMatrix <code>Context</code> object
 * @param  objectId  holds the Product Configuration Id
 * @return MapList of Selected Options
 * @throws Exception if the operation fails
 * @author KXB
 * @since  FTR R212
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getPreciseBOMForPC(Context context, String[] args)throws Exception
 {

		MapList mplPreciseBOM = new MapList();
		MapList selectedOptionsList = new MapList();
		ProductConfiguration pcBean = new ProductConfiguration();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String pcObjId = (String) programMap.get("objectId");
		// get the Filter Compiled Form of the PC,
		DomainObject objPCID = new DomainObject(pcObjId);
		String sPBOMExists = objPCID.getInfo(context, "from["
				+ ConfigurationConstants.RELATIONSHIP_PRECISE_BOM + "]");
		MapList evaluatedPreciseParts = new MapList();

		if (ConfigurationConstants.RANGE_VALUE_TRUE
				.equalsIgnoreCase(sPBOMExists)) {
			evaluatedPreciseParts = pcBean.getAllPreciseBOMParts(context,
					pcObjId);
			mplPreciseBOM.addAll(evaluatedPreciseParts);
		} else {
			StringList selList = new StringList();
			selList.add("attribute["
					+ PropertyUtil
							.getSchemaProperty(context, "attribute_FilterCompiledForm")
					+ "]");
			selList.add("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");
			Map dataMap = objPCID.getInfo(context, selList);
			String strFilterCompiledForm = (String) dataMap.get("attribute["
					+ PropertyUtil
							.getSchemaProperty(context, "attribute_FilterCompiledForm")
					+ "]");
			String contextId = (String) dataMap
					.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");

			if (contextId!=null && !contextId.equals("")){
			MapList logicalFeatureWithGBOM = new MapList();
			ProductConfiguration pcInstance = new ProductConfiguration(pcObjId,
					ProductConfiguration.NONINTERACTIVE_MODE);

			MapList logicalFeatureList = pcInstance.getLogicalFeatureGBOMStructure(
					context, contextId,false);

			String relSelect = DomainConstants.EMPTY_STRING;
			StringList objselect = new StringList();
			objselect.add(ProductLineConstants.TYPE_PART);
			objselect.add(ConfigurationConstants.SELECT_LEVEL);			
			// get the Logical Features structure of the Context and iterate as
			// below.
			HashMap levelMap = new HashMap();
			Integer currLevel = 0;
			Integer prevLevel = 0;
			String genLevel = null;
			String tempLevel = null;
			
			for (int i = 0; i < logicalFeatureList.size(); i++) {

				// Maplist to store the Duplicate Rows
				Map feature = (Map) logicalFeatureList.get(i);
				currLevel = Integer.valueOf((String)feature.get("level"));
				if(currLevel > prevLevel){
					if(prevLevel != 0){
						tempLevel = (String)levelMap.get(currLevel-1);
						genLevel = generatePartLevel(context, tempLevel, true);
					}
					else{
						genLevel = "1";
					}
				}
				else{
					tempLevel = (String)levelMap.get(currLevel);
					genLevel = generatePartLevel(context, tempLevel, false);
				}

				prevLevel = currLevel;
				levelMap.put(currLevel, genLevel);

				String lfId = (String) feature
						.get(ConfigurationConstants.SELECT_ID);
				StringList partListIDs=ConfigurationUtil.convertObjToStringList(context, feature.get("from["+ConfigurationConstants.RELATIONSHIP_GBOM+"].to.id"));				
				StringList partListName=ConfigurationUtil.convertObjToStringList(context, feature.get("from["+ConfigurationConstants.RELATIONSHIP_GBOM+"].to.name"));		
				MapList infoTopMap=new MapList();
				for(int k=0;k<partListIDs.size();k++){
					Map infoMap = new HashMap();
					infoMap.put(ConfigurationConstants.SELECT_ID, partListIDs.get(k));
					infoMap.put(ConfigurationConstants.SELECT_NAME,partListName.get(k));					
					infoTopMap.add(infoMap);							
				}
			if (infoTopMap.size() > 0) {

					for (int k = 0; k < infoTopMap.size(); k++) {
						Map infoMap = new HashMap();
						MapList tempPartlist = new MapList();
						Map partMap = (Map) infoTopMap.get(k);					
						infoMap.put(ConfigurationConstants.SELECT_ID, partMap
								.get(ConfigurationConstants.SELECT_ID));
						infoMap.put(ConfigurationConstants.SELECT_NAME, partMap
								.get(ConfigurationConstants.SELECT_NAME));
						infoMap.put("Context LF ID", lfId);
						infoMap.put("partLevel", genLevel);					
						Hashtable ht = new Hashtable(infoMap);
						tempPartlist.add(ht);						
						mplPreciseBOM.addAll(tempPartlist);
					}
				}
			}
		
			LogicalFeature prdObj = new LogicalFeature(contextId);
			MapList prdGbomList=prdObj.getActiveGBOMStructure(context, "",
					relSelect, objselect, ConfigurationUtil
					.getBasicRelSelects(context), false, true, 1,
			0, null, null, DomainObject.FILTER_STR_AND_ITEM,
			strFilterCompiledForm);
			mplPreciseBOM.addAll(prdGbomList);

		}
		}
		return mplPreciseBOM;
	}



  /**
   * Get the list of Selected Options of the Product Configuration.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - HashMap containing one String entry for the key "objectId"
   * @return MapList of Selected Options
   * @throws Exception if the operation fails
   * @since R212
   */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getSelectedOptions(Context context, String[] args)
    throws Exception
  {
     MapList relBusObjPageList = null;

    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    String objectId = (String) programMap.get("objectId");

    relBusObjPageList = ProductConfiguration.getSelectedOptions(context, objectId,true,true);

    return relBusObjPageList;
  //}
    // END - Added/Modified for Bug No. IR-046791V6R2011
  }

  /**
   * get Standard Custom Item for the Product Configuration.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
     *        0 - HashMap containing one Map entry for the key "paramMap"
     *      This Map contains the arguments passed to the jsp which called this method.
   * @return StringList (Standard Custom Item for the Product Configuration.)
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   */

  public StringList getStandardCustomItem(Context context, String[] args)
    throws Exception
  {
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    HashMap paramMap = (HashMap) programMap.get("paramMap");
    HashMap requestMap = (HashMap)programMap.get(REQUESTMAP);
    // Get the required parameter values from  "programMap" - as required
    String objectId = (String) paramMap.get("objectId");
    String strMode = "";
    if (requestMap.containsKey(MODE))
      strMode = (String)requestMap.get(MODE);

    // Initialize the variables
    StringList standardCustomList = new StringList();
    String standardCustomTag = "";

    String language = context.getSession().getLanguage();

    String strCustomConfiguration =
      EnoviaResourceBundle.getProperty(context, SUITE_KEY,
        "emxProduct.Value.CustomConfiguration",language);
    String strStandardConfiguration =
      EnoviaResourceBundle.getProperty(context, SUITE_KEY,
        "emxProduct.Value.StandardConfiguration",
        language);

    /*
    ** Commented out... This is not required.
    String sUserName                  = context.getUser();
    com.matrixone.apps.common.Person personLoggedIn = com.matrixone.apps.common.Person.getPerson ( context,
            sUserName);
    com.matrixone.apps.common.Company userCompany   = personLoggedIn.getCompany ( context );

    String sUserCompanyId             = userCompany.getObjectId();
    */

    StringList strCompanyInfo =
      ProductLineUtil.getUserCompanyIdName(context);
    String sUserCompanyId = (String) strCompanyInfo.get(0);

    // Initialize the relationships
    String rel_StandardItem =
      ProductLineConstants.RELATIONSHIP_STANDARD_ITEM;
    String rel_CustomItem =
      ProductLineConstants.RELATIONSHIP_CUSTOM_ITEM;

    if (objectId != null) // process if not null
    {
      DomainObject dmnObject = new DomainObject(objectId);

      String strListRequiredInfo = "to[" + rel_StandardItem + "].businessobject.id";
      String sTempCompanyId =
        dmnObject.getInfo(context, strListRequiredInfo);

      if (sTempCompanyId != null)
      {
        if (!(sTempCompanyId.equals(DomainConstants.EMPTY_STRING)))
        {
          if (sTempCompanyId.equals(sUserCompanyId))
          {
            if (strMode.equalsIgnoreCase("Edit"))
              standardCustomTag = STANDARDCONFIGURATION;
            else
              standardCustomTag = strStandardConfiguration;
          }
        }
      }
      else
      {
        strListRequiredInfo = "to[" + rel_CustomItem + "].businessobject.id";
        sTempCompanyId =
          dmnObject.getInfo(context, strListRequiredInfo);
        if (sTempCompanyId != null)
        {
          if (!(sTempCompanyId.equals(DomainConstants.EMPTY_STRING)))
          {
            if (sTempCompanyId.equals(sUserCompanyId))
            {
              if (strMode.equalsIgnoreCase("Edit"))
                standardCustomTag = CUSTOMCONFIGURATION;
              else
                standardCustomTag = strCustomConfiguration;
            }
          }
        }
      }
    }
    // Adds the Custom/Standard Tag in the return "String List"
    standardCustomList.addElement(standardCustomTag);
    return standardCustomList;
  }


  /**
   * get Purpose Range for the creation of a product configuration.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @return StringList representing the Purpose Range for the creation of a product configuration.
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   */

  public Object getStandardCustomRange(Context context, String[] args)
    throws Exception
  {
    // Initialize the return variable
    HashMap rangeMap = new HashMap();
    StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();

    String language = context.getSession().getLanguage();

    String strCustomConfiguration =
      EnoviaResourceBundle.getProperty(context, SUITE_KEY,
        "emxProduct.Value.CustomConfiguration",language);
    String strStandardConfiguration =
    	EnoviaResourceBundle.getProperty(context, SUITE_KEY,
        "emxProduct.Value.StandardConfiguration",language);

    // Process the information to obtain the range values and add them to fieldRangeValues

    fieldRangeValues.addElement(CUSTOMCONFIGURATION);
    fieldRangeValues.addElement(STANDARDCONFIGURATION);
    fieldDisplayRangeValues.addElement(strCustomConfiguration);
    fieldDisplayRangeValues.addElement(strStandardConfiguration);
    rangeMap.put("field_choices", fieldRangeValues);
    rangeMap.put("field_display_choices", fieldDisplayRangeValues);
        return  rangeMap;
  }

  /**
   * updates the Purpose values of the Product Configuration.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
     *        0 - HashMap containing one Map entry for the key "paramMap"
     *      This Map contains the arguments passed to the jsp which called this method.
   * @return integer 0 if the operation is successful... and 1 if it fails.
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   */

  public int updateStandardCustomRange(Context context, String[] args)
    throws Exception
  {
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    HashMap paramMap = (HashMap) programMap.get("paramMap");

    String strObjectId = (String) paramMap.get("objectId");
    String strNewValue = (String) paramMap.get("New Value");
    String strOldValue = (String) paramMap.get("Old value");
    String strUser = context.getUser();

    String strCustomConfiguration = CUSTOMCONFIGURATION;
    String strStandardConfiguration = STANDARDCONFIGURATION;

    String strListRequiredInfo = "";
    String strOldRelationshipId = "";
    String strNewRelationshipType = "";
    String strCompanyId = "";

    int iReturnValue = 0;

    if (!(strNewValue.equals(strOldValue)))
    {
      ContextUtil.startTransaction(context, true);
      try
      {
        // Find the Company object using the Old relationship
        DomainObject dmnPCObject = newInstance(context, strObjectId);
        //check if it is already connected then only we need to disconnect.
        if (strOldValue != null && !strOldValue.equals("") && !("null".equalsIgnoreCase(strOldValue)))
        {
          if (strOldValue.equals(strStandardConfiguration))
          {
            strListRequiredInfo =
                "to["
                  + ProductLineConstants
                    .RELATIONSHIP_STANDARD_ITEM
                  + "].businessobject.id";
          }
          else
            if (strOldValue.equals(strCustomConfiguration))
            {
              strListRequiredInfo =
                  "to["
                    + ProductLineConstants
                      .RELATIONSHIP_CUSTOM_ITEM
                    + "].businessobject.id";
            }
          strCompanyId =
            dmnPCObject.getInfo(context, strListRequiredInfo);

          // Disconnect the existing relationship
          if (strNewValue.equals(strCustomConfiguration))
            // If the new value is "Custom Configuration"
          {
            strListRequiredInfo =
                "to["
                  + ProductLineConstants
                    .RELATIONSHIP_STANDARD_ITEM
                  + "].id";
          }
          else
            if (strNewValue.equals(strStandardConfiguration))
              // If the new value is "Standard Configuration"
            {
              strListRequiredInfo =
                  "to["
                    + ProductLineConstants
                      .RELATIONSHIP_CUSTOM_ITEM
                    + "].id";
            }
          strOldRelationshipId =
            dmnPCObject.getInfo(context, strListRequiredInfo);
          DomainRelationship.disconnect(context, strOldRelationshipId);
        }
        // Connect the object with new relationship
        if (strNewValue.equals(strCustomConfiguration))
          // If the new value is "Custom Configuration"
        {
          strNewRelationshipType =
              ProductLineConstants
                .RELATIONSHIP_CUSTOM_ITEM;
        }
        else
          if (strNewValue.equals(strStandardConfiguration))
            // If the new value is "Standard Configuration"
          {
            strNewRelationshipType =
                ProductLineConstants
                  .RELATIONSHIP_STANDARD_ITEM;
          }

        if (strCompanyId == null || strCompanyId.equals("") || "null".equalsIgnoreCase(strCompanyId))
        {
          strCompanyId = (String)ProductLineUtil.getUserCompanyIdName(context,strUser).get(0);
        }
        DomainObject dmnCompanyObject = newInstance(context, strCompanyId);

        DomainRelationship.connect(
                context,
                dmnCompanyObject,
                strNewRelationshipType,
                dmnPCObject);

        ContextUtil.commitTransaction(context);
        iReturnValue = 0;
      }
      catch (Exception exp)
      {
        ContextUtil.abortTransaction(context);
        iReturnValue = 1;
        throw exp;
      }
    }
    else
    {
      iReturnValue = 0;
    }

    // Process the information to set the field values for the current object
    return iReturnValue;
  }


  /**
   *  Access check for commands based on whether they are called from the Main window or the Popup.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds Map containg the following input arguments:
   *      0 - Haspmap containg SETTINGS
   *      1- string of command
   * @return boolean true if access is allowed, and false otherwise.
   * @throws Exception if the operation fails
   * @since ProductCentral 10.5
   */
  public boolean checkAccessForLinks(Context context, String args[]) throws Exception
  {
    String strNewWindow = "NewWindow";
    String strMainWindow = "MainWindow";
    String strTreeSrc = "fromCreate";
    //Getting programMap from the args
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    //Getting the Settings from the ProgramMap
    HashMap settingsMap = (HashMap)programMap.get("SETTINGS");
    //Getting the Setting "Command" from the settingsMap
    String strCommandParam = (String)settingsMap.get("Command");
    //Getting the parameter treeSource(if it exists) from the programMap
    if (programMap.containsKey("treeSource"))
      strTreeSrc = (String) programMap.get("treeSource");

    // Check for the state and Validation Status attribute value of Product Configuration,
    // links should not be shown for a Salable Configuration
    boolean bDisplay = showLinksForSalableConfig(context, args);
    //if the command and the source of the tree are newwindow return true
    if (strTreeSrc.equals(strNewWindow) && strCommandParam.equals(strNewWindow) && bDisplay)
      return true;
    //if the command is for the mainwindow and the source of the tree are newwindow return false
    else if (strTreeSrc.equals(strNewWindow) && strCommandParam.equals(strMainWindow) && !bDisplay)
      return false;
    //if the command and the source of the tree are mainwindow return true
    else if (strTreeSrc.equals(strMainWindow) && strCommandParam.equals(strMainWindow) && bDisplay)
      return true;
    else if (strTreeSrc.equals("fromCreate") && strCommandParam.equals(strMainWindow) && bDisplay)
      return true;
    else
      return false;
  }

   /**
    * gets the CustomItem/StandardItem relationship for Product Configuration table
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the arguments passed by trigger.
    * @return StringList
    * @throws Exception if the operation fails
    * @since ProductCentral 10.5.SP1-Semi
    */
    public StringList getStandardCustomItemforTable(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = new MapList();
        objectList = (MapList) programMap.get("objectList");

        StringList standardCustomList = new StringList();
        String standardCustomTag = "";

        String language = context.getSession().getLanguage();

        String strCustomConfiguration =
          EnoviaResourceBundle.getProperty(context, SUITE_KEY,
            "emxProduct.Value.CustomConfiguration",language);
        String strStandardConfiguration =
        	EnoviaResourceBundle.getProperty(context, SUITE_KEY,
            "emxProduct.Value.StandardConfiguration",language);

        StringList strCompanyInfo =
          ProductLineUtil.getUserCompanyIdName(context);
        String sUserCompanyId = (String) strCompanyInfo.get(0);

        // Initialize the relationships
        String rel_StandardItem =
          ProductLineConstants.RELATIONSHIP_STANDARD_ITEM;
        String rel_CustomItem =
          ProductLineConstants.RELATIONSHIP_CUSTOM_ITEM;

        for(int count=0;count < objectList.size(); count++)
        {
          Map objectListMap=(Map)objectList.get(count);
          String objectId=(String)objectListMap.get("id");

          if (objectId != null) // process if not null
          {
            DomainObject dmnObject = new DomainObject(objectId);

            String strListRequiredInfo = "to[" + rel_StandardItem + "].businessobject.id";
            String sTempCompanyId =
              dmnObject.getInfo(context, strListRequiredInfo);

            if (sTempCompanyId != null)
            {
              if (!(sTempCompanyId.equals(DomainConstants.EMPTY_STRING)))
              {
                if (sTempCompanyId.equals(sUserCompanyId))
                {
                    standardCustomTag = strStandardConfiguration;
                }
              }
            }
            else
            {
              strListRequiredInfo = "to[" + rel_CustomItem + "].businessobject.id";
              sTempCompanyId =
                dmnObject.getInfo(context, strListRequiredInfo);
              if (sTempCompanyId != null)
              {
                if (!(sTempCompanyId.equals(DomainConstants.EMPTY_STRING)))
                {
                  if (sTempCompanyId.equals(sUserCompanyId))
                  {
                      standardCustomTag = strCustomConfiguration;
                  }
                }
              }
            }
          }
          // Adds the Custom/Standard Tag in the return "String List"
          standardCustomList.addElement(standardCustomTag);
        }
        return standardCustomList;
    }

   /**
    * Checks access for creation of Product Configuration for an non released EC Part.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the arguments passed by trigger.
    * @return Boolean
    * @throws Exception if the operation fails
    * @since PRC 10.6
    */
    public Boolean canCreateProductConfig(Context context, String[] args)
       throws Exception
    {
        Boolean retVal=null;
        Map programMap = (Map) JPO.unpackArgs(args);
        StringList objectSelects=new StringList();

        String objectId = (String) programMap.get("objectId");

        // Bug 299362 fix: allow Prod Config creation for parts in all states.
        //objectSelects.addElement(DomainObject.SELECT_CURRENT);
        objectSelects.addElement(DomainObject.SELECT_POLICY);

        DomainObject domObject = new DomainObject(objectId);

        Map map = domObject.getInfo(context,objectSelects);

        //String strCurrentState = (String) map.get(DomainObject.SELECT_CURRENT);
        String strPolicy = (String) map.get(DomainObject.SELECT_POLICY);

        //if(strPolicy.equals(DomainConstants.POLICY_EC_PART) && DomainConstants.STATE_PART_RELEASE.equals(strCurrentState))
        if(strPolicy.equals(DomainConstants.POLICY_EC_PART))
        {
            retVal = Boolean.valueOf(true);
        }
        else
        {
            retVal = Boolean.valueOf(false);
        }
        return retVal;
     }

   /**
    * Trigger checks whether Product Configuration is created for a Salable Part
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the arguments passed by trigger. ObjectId
    * @return Boolean
    * @throws Exception if the operation fails
    * @since EC 10.5.1.2
    */
    public int isSalableProductConfiguration(Context context, String[] args)
       throws Exception
    {
        String objectId = args[0];
        boolean bSalable = isSalableProductConfiguration(context ,objectId);
        // If the Product Configuration is created for a Salable Part then do not allow to demote
        if(bSalable)
        {
            String language = context.getSession().getLanguage();

            String strSalable =
            	EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                "emxProduct.TiggerMsg.SalableProductConfiguration",language);

            throw new FrameworkException(strSalable);
        }
        return 0;

    }


    public int inactiveFeaturePromoteCheck(Context context,String[] args) throws Exception
    {
        // check for the relationship Inactive Feature List From, from Product to Feature List.
        //If any of them is present in the Selected Options, then fail
//        String stObjectId = (String) args[0];
//        boolean bFlag=false;
//        String relationshipName = ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS ;
//        String strType = ProductLineConstants.TYPE_FEATURE_LIST;
//        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
//        StringList relSelects = null;
//        short shRecursionLevel = 1;
//        DomainObject domainObject = new DomainObject(stObjectId);
//        MapList relBusObjPageList = domainObject.getRelatedObjects(
//                                    context,
//                                    relationshipName,
//                                    strType,
//                                    objectSelects,
//                                    relSelects,
//                                    false,
//                                    true,
//                                    shRecursionLevel,
//                                    DomainConstants.EMPTY_STRING,
//                                    DomainConstants.EMPTY_STRING);
//        StringList attributeList = new StringList();
//        attributeList.add(DomainConstants.SELECT_ID);
//        DomainObject productConfiguration = DomainObject.newInstance(context);
//        productConfiguration.setId(stObjectId);
//        /* Retrieve the product connected with the productConfiguration
//         */
//        Map htbProduct =(Hashtable) productConfiguration.getRelatedObject(
//                context,
//                ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION,
//                false,
//                attributeList,
//                null);
//        String productId = (String) htbProduct.get(DomainConstants.SELECT_ID);
//        DomainObject product = DomainObject.newInstance(context);
//        product.setId(productId);
//        StringBuffer sbType = new StringBuffer(20);
//        sbType.append(ProductLineConstants.TYPE_FEATURE_LIST);
//        relationshipName = ProductLineConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM;
//        shRecursionLevel = 0;
//        MapList relBusObjList = product.getRelatedObjects(
//                context,
//                relationshipName,
//                sbType.toString(),
//                objectSelects,
//                relSelects,
//                false,
//                true,
//                shRecursionLevel,
//                DomainConstants.EMPTY_STRING,
//                DomainConstants.EMPTY_STRING);
//        for(int iCount = 0; iCount < relBusObjList.size();iCount++)
//        {
//            String string1 = (String)((Map) relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
//            bFlag = false;
//            for (int j = 0; j < relBusObjList.size(); j++) {
//                String string2 = (String)((Map) relBusObjList.get(j)).get(DomainConstants.SELECT_ID);
//                if (string1.equals(string2)){
//                    bFlag = true;
//                    break;}
//            }//end of inner for
//            if(!bFlag){
//             break;}
//        }//end of outer for
//        if(bFlag){
//            //Begin of Modify Mayukh,Enovia MatrixOne Bug # 300168 Date 03/17/2005
//            //Check Trigger blocked
//            String strPromoteStateError = i18nNow.getI18nString(
//                "emxProduct.Alert.InactiveFeaturePresent",
//                RESOURCEBUNDLE,
//                context.getSession().getLanguage());
//            ${CLASS:emxContextUtil}.mqlNotice(context, strPromoteStateError);
//            return 1;
//            //End of Modify Mayukh,Enovia MatrixOne Bug # 300168 Date 03/17/2005
//        }else {
//          return 0;//Check Trigger Passed
//        }

    	//TODO this method needs to be implemented after Product Configuration Lifecycle refactoring
       return 0;
    }

       /**
     * Trigger Method to check if the selected Options in the product
     * Configuration are present in the Product Structure.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Object Id of the Product Configuration.
     * @return int 0 - succcess
     *             1 - failure.
     * @throws Exception if the operation fails
     * @since R212
     */

    public int checkSelectedOptionsExist(Context context,String[] args)
        throws Exception{

    	//TODO this method may need to be changed after Product Configuratio Lifecycle refactoring
    	String productConfigurationId = (String)args[0];

    	//1. Get the relIds of the rels connected to this Product Configuration by the Selected Options relationship
    	Vector selectedRelIds = new Vector();
    	ProductConfiguration productConfigObj = new ProductConfiguration(productConfigurationId,
				ProductConfiguration.NONINTERACTIVE_MODE);
		MapList selOptions = ProductConfiguration.getSelectedOptions(context, productConfigurationId,false,true);
        for(int i = 0; i < selOptions.size(); i++)
		{
			Map objMap = (Map)selOptions.get(i);
			String relId = (String)objMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			selectedRelIds.add(relId);
		}

        //3. Get the Context Product Id
    	DomainObject domPC =  new DomainObject(productConfigurationId);
        StringList attributeList = new StringList();
        attributeList.add(DomainConstants.SELECT_ID);
        StringList relSel = new StringList();
        relSel.add(DomainConstants.SELECT_RELATIONSHIP_ID);

        StringList objectSelects = new StringList();
        objectSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");
        objectSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_START_EFFECTIVITY+"]");

        Map mpProduct = domPC.getInfo(context, objectSelects);
        String productId = (String) mpProduct.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");
        productConfigObj.setStartEffectivity(context,
        		(String)mpProduct.get("attribute["+ConfigurationConstants.ATTRIBUTE_START_EFFECTIVITY+"]"),
    			ProductConfiguration.ACTION_PC_EDIT);

        //4. Get the Product structure of the context product
        DomainObject contextDomObj = new DomainObject(productId);
		ConfigurationUtil contextUtilObj = new ConfigurationUtil(contextDomObj);
		StringBuffer relTypes = new StringBuffer();
		relTypes.append(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES);
		StringList objAttriSel = new StringList(DomainConstants.SELECT_ID);
		StringList relAttriSel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
		StringBuffer sbEffExpr = new StringBuffer();
		String strStartEffectivity = productConfigObj.getStartEffectivity();
		EffectivityFramework EFF = new EffectivityFramework();
		Map effectivitySettings = EFF.getEffectivityTypeSettings(context);
		String dateKeyword = ConfigurationConstants.EMPTY_STRING;
		if(effectivitySettings != null && effectivitySettings.containsKey("Date"))
		{
			Map dateMap = (HashMap)effectivitySettings.get("Date");
			if(dateMap != null)
			{
				dateKeyword = (String)dateMap.get("keyword");
			}
		}
		String strEffectivityExpression = ConfigurationConstants.EMPTY_STRING;
		if(!UIUtil.isNullOrEmpty(strStartEffectivity))
		{
			sbEffExpr.append(EffectivityFramework.KEYWORD_PREFIX);
			sbEffExpr.append(dateKeyword);
			sbEffExpr.append(EffectivityFramework.KEYWORD_OPEN_BRACKET);
			sbEffExpr.append(strStartEffectivity);
			sbEffExpr.append(EffectivityFramework.KEYWORD_CLOSE_BRACKET);
	        Map binaryMap = EFF.getFilterCompiledBinary(context, sbEffExpr.toString());
	        strEffectivityExpression = (String)binaryMap.get(EFF.COMPILED_BINARY_EXPR);
		}
		MapList relatedObjectsList = contextUtilObj.getObjectStructure(context,"*", relTypes.toString(),
				objAttriSel, relAttriSel, false, true, 0, 0, ConfigurationConstants.EMPTY_STRING,
				ConfigurationConstants.EMPTY_STRING,(short)-1,
				strEffectivityExpression);
		Vector structureRelIds = new Vector();
		for(int i = 0; i < relatedObjectsList.size(); i++)
		{
			Map objMap = (Map)relatedObjectsList.get(i);
			String relId = (String)objMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			structureRelIds.add(relId);
		}
		/* Only Configuration Features check
		if(contextDomObj.isKindOf(context, ConfigurationConstants.TYPE_PRODUCT_VARIANT))
		{
			structureRelIds.addAll(contextUtilObj.getInfoList(context, "from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id"));
		}
		 */

		//4. Check if all selected rels are a part of the structure rels
		for(int i = 0; i < selectedRelIds.size(); i++)
		{
			String selectedRel = (String)selectedRelIds.elementAt(i);
			if(!structureRelIds.contains(selectedRel))
			{
				 String strPromoteStateError = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.SelectedOptionsNotPresent",
			                context.getSession().getLanguage());
			          emxContextUtil_mxJPO.mqlNotice(context, strPromoteStateError);
			          return 1;

			}

		}
		return 0;
    }

    /**
     * Trigger Method to check if dyamically filtered Logical Features exist in the Product/Product Variant/Logical Structure
     * Configuration are present in the Product Structure.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Object Id of the Product Configuration.
     * @return int 0 - succcess
     *             1 - failure.
     * @throws Exception if the operation fails
     * @since R215
     */

    public int checkSelectedLogicalFeaturesExist(Context context,String[] args)
        throws Exception {

    	String productConfigurationId = (String)args[0];

    	Vector selectedRelIds = new Vector();
    	ProductConfiguration productConfigObj = new ProductConfiguration(productConfigurationId,
				ProductConfiguration.NONINTERACTIVE_MODE);
        productConfigObj.initContext(context);
        MapList relatedLogicalFeatures = productConfigObj.getLogicalFeatureStructure(context, productConfigObj.getContextId(),true);

    	for(int i = 0; i < relatedLogicalFeatures.size(); i++)
		{
			Map objMap = (Map)relatedLogicalFeatures.get(i);
			String relId = (String)objMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			String relType = (String)objMap.get(DomainRelationship.SELECT_TYPE);
			if(ConfigurationUtil.isOfParentRel(context, relType, ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES))
				selectedRelIds.add(relId);
		}

    	DomainObject domPC =  new DomainObject(productConfigurationId);
        StringList attributeList = new StringList();
        attributeList.add(DomainConstants.SELECT_ID);
        StringList relSel = new StringList();
        relSel.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        StringList objectSelects = new StringList();
        objectSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");

        Map mpProduct = domPC.getInfo(context, objectSelects);
        String productId = (String) mpProduct.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");

        DomainObject contextDomObj = new DomainObject(productId);
		ConfigurationUtil contextUtilObj = new ConfigurationUtil(contextDomObj);
		StringBuffer relTypes = new StringBuffer();
		relTypes.append(ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES);
		StringList objAttriSel = new StringList(DomainConstants.SELECT_ID);
		StringList relAttriSel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
		String strEffectivityExpression = ConfigurationConstants.EMPTY_STRING;

		MapList relatedObjectsList = contextUtilObj.getObjectStructure(context,"*", relTypes.toString(),
				objAttriSel, relAttriSel, false, true, 0, 0, ConfigurationConstants.EMPTY_STRING,
				ConfigurationConstants.EMPTY_STRING,(short)-1,
				strEffectivityExpression);

		Vector structureRelIds = new Vector();
		for(int i = 0; i < relatedObjectsList.size(); i++)
		{
			Map objMap = (Map)relatedObjectsList.get(i);
			String relId = (String)objMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			structureRelIds.add(relId);
		}

		if(contextDomObj.isKindOf(context, ConfigurationConstants.TYPE_PRODUCT_VARIANT))
		{
			structureRelIds.addAll(contextUtilObj.getInfoList(context, "from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id"));
		}
		//4. Check if all selected rels are a part of the structure rels
		for(int i = 0; i < selectedRelIds.size(); i++)
		{
			String selectedRel = (String)selectedRelIds.elementAt(i);
			if(!structureRelIds.contains(selectedRel))
			{
				 String strPromoteStateError = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			              "emxProduct.Alert.SelectedOptionsNotPresent",
			              context.getSession().getLanguage());
			          emxContextUtil_mxJPO.mqlNotice(context, strPromoteStateError);
			          return 1;

			}

		}
		return 0;
    }


    /**
     * Trigger Method called to check if the Rules are valid.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Object Id of the Product Configuration.
     * @return int 0 - succcess
     *             1 - failure.
     * @throws Exception if the operation fails
     * @since R212
     *
     */

    public int validateRulesCheck(Context context, String[] args) throws Exception
    {
         int iFlag = 0;
         String strProductConfigurationID = args[0];
         ProductConfiguration productConf = new ProductConfiguration();
         productConf.setId(strProductConfigurationID);
         //Marketing Rules Evaluation
         productConf.initContext(context);
         productConf.loadContextStructure(context, productConf.getContextId(), productConf.getParentProductId());
         productConf.loadSelectedOptions(context);
         boolean marketingRulesPassed = productConf.isValidProductConfiguration(context);
         if (marketingRulesPassed)
         {
        	 setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_PASSED);
         }else
         {
        	 setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_DESIGN_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_FAILED);
             iFlag = 1;
         }
         if(iFlag == 1)
         {
             String strLanguage = context.getSession().getLanguage();
             String strPromoteStateError = EnoviaResourceBundle.getProperty(context, SUITE_KEY,CHECK_CURRENTCONFIGURATION, strLanguage);
             emxContextUtil_mxJPO.mqlNotice(context, strPromoteStateError);
         }

         return iFlag;
    }
    /**
     * Trigger Method called to check if the Design Rules are valid.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Object Id of the Product Configuration.
     * @return int 0 - succcess
     *             1 - failure.
     * @throws Exception if the operation fails
     * @since R215
     *
     */

    public int validateDesignRulesCheck(Context context, String[] args) throws Exception
    {
         int iFlag = 0;
         String strProductConfigurationID = args[0];
         ProductConfiguration productConf = new ProductConfiguration();
         productConf.setId(strProductConfigurationID);
         productConf.initContext(context);
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
             String strPromoteStateError = EnoviaResourceBundle.getProperty(context, SUITE_KEY,CHECK_CURRENTCONFIGURATION, strLanguage);
             emxContextUtil_mxJPO.mqlNotice(context, strPromoteStateError);
         }

         return iFlag;
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
         //The RPE variable is only set when the promote action is set to begin upon a successful PC validation.
         String skipTriggerCheck = PropertyUtil.getRPEValue(context, "MX_SKIP_PC_VALIDATION_IN_PROMOTE_CHECK", false);

         if(skipTriggerCheck != null && "true".equals(skipTriggerCheck))
         {
             //reset the RPE variable just in case it gets called from some where else, very unlikely.
             PropertyUtil.setRPEValue(context, "MX_SKIP_PC_VALIDATION_IN_PROMOTE_CHECK", "false", false);
         }
         else
         {
            String strProductConfigurationID = args[0];
            //[IR-339261:START]
            String strLanguage = context.getSession().getLanguage();
            String strAttributeRuleCompliancyStatus = ConfigurationConstants.ATTRIBUTE_RULE_COMPLIANCY_STATUS;
            String strAttributeCompletenesstatus = ConfigurationConstants.ATTRIBUTE_COMPLETENESS_STATUS;
        	 
            StringList attrNameList = new StringList();
            attrNameList.add("attribute[" + strAttributeRuleCompliancyStatus + "]");
            attrNameList.add("attribute[" + strAttributeCompletenesstatus + "]");
     		     		
            DomainObject pcObj = new DomainObject(strProductConfigurationID); 		
     		     		
            Map<String, String> attrValueMap = pcObj.getInfo(context, attrNameList);
            
            if( false == attrValueMap.isEmpty() && 
            attrValueMap.get(attrNameList.get(0)).equalsIgnoreCase("Valid")) {
                if(false == attrValueMap.get(attrNameList.get(1)).equalsIgnoreCase("Complete"))
                {
                	iFlag = 1;
                    String strPromoteStateError = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.ProductConfiguration.PromoteFailCompletenessStatusFailure", strLanguage);
                    throw new FrameworkException(strPromoteStateError);
                }
            } else {
                iFlag = 1;
                String strPromoteStateError = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.ProductConfiguration.PromoteFailRuleCompliancyStatusFailure", strLanguage);
                throw new FrameworkException(strPromoteStateError);				
            }
     		 //Commented old code below.
             /*ProductConfiguration productConf = new ProductConfiguration();
             productConf.setId(strProductConfigurationID);
             //Marketing Rules Evaluation
             productConf.initContext(context);
             productConf.loadContextStructure(context, productConf.getContextId(), productConf.getParentProductId());
             productConf.loadSelectedOptions(context);
             boolean marketingRulesPassed = productConf.isValidProductConfiguration(context);

             BackgroundProcess backgroundProcess = new BackgroundProcess();
             String strAttributeValidationStatus=ConfigurationConstants.ATTRIBUTE_VALIDATION_STATUS;
             String strAttributeValidationStatusValue="";
             if (marketingRulesPassed)
             {
           	  strAttributeValidationStatusValue=ConfigurationConstants.RANGE_VALUE_VALIDATION_PASSED;
            	  // setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_PASSED);

             }else
             {
                 strAttributeValidationStatusValue=ConfigurationConstants.RANGE_VALUE_VALIDATION_FAILED;
                 // setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_DESIGN_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_FAILED);
                 iFlag = 1;
             }
             String[] args1= new String []{strProductConfigurationID,strAttributeValidationStatus,strAttributeValidationStatusValue};
             backgroundProcess.submitJob(context.getFrameContext(context.getSession().toString()), "emxProductConfiguration", "setValidationStatusOnPromote",  args1 , (String)null);


             if(iFlag == 1)
             {
                 String strLanguage = context.getSession().getLanguage();
                 String strPromoteStateError = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxConfiguration.ProductConfiguration.PromoteFailMarketingRuleEvaluationFailure", strLanguage);
                 throw new FrameworkException(strPromoteStateError);
             }*/
             //[IR-339261:END]
         }
         return iFlag;
     }
     /**
      * This method will be called as part of a promotion check trigger to check if all design rules are validated.
      *
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args     a String array holding the arguments from the calling method.
       *                   It contains a string representing the strProductConfigurationID.
       * @return int 0 or 1
       * @throws Exception when the operation fails.
       * @since R215
       */
      public int validateBatchDesignRulesForPromoteCheck(Context context, String[] args) throws Exception{


          int iFlag = 0;
          String strProductConfigurationID = args[0];
          ProductConfiguration productConf = new ProductConfiguration();
          productConf.setId(strProductConfigurationID);
          productConf.initContext(context);

          //Design Rule Evaluation
          productConf.clearStructure();
          productConf.loadContextLogicalStructure(context, productConf.getContextId(), productConf.getParentProductId());
          StringList conflictingRules = productConf.validateDesignRules(context);

          BackgroundProcess backgroundProcess = new BackgroundProcess();
          String strAttributeValidationStatus=ConfigurationConstants.ATTRIBUTE_DESIGN_VALIDATION_STATUS;
          String strAttributeValidationStatusValue="";
          if (conflictingRules.size() > 0)
          {

              strAttributeValidationStatusValue=ConfigurationConstants.RANGE_VALUE_VALIDATION_FAILED;
              // 	setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_DESIGN_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_FAILED);
                 iFlag = 1;
          }else
          {
       	  strAttributeValidationStatusValue=ConfigurationConstants.RANGE_VALUE_VALIDATION_PASSED;
         	 //setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_DESIGN_VALIDATION_STATUS,ConfigurationConstants.RANGE_VALUE_VALIDATION_PASSED);
          }
          String[] args1=new String []{strProductConfigurationID,strAttributeValidationStatus,strAttributeValidationStatusValue};
          backgroundProcess.submitJob(context.getFrameContext(context.getSession().toString()), "emxProductConfiguration", "setValidationStatusOnPromote",  args1 , (String)null);

          if(iFlag == 1)
          {
              String strLanguage = context.getSession().getLanguage();
              String strPromoteStateError = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxConfiguration.ProductConfiguration.PromoteFailDesignRuleEvaluationFailure", strLanguage);
              throw new FrameworkException(strPromoteStateError);
          }
          return iFlag;
      }
    /**
     * Access check for commands based on the state and Validation Status attribute value of Product Configuration
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Object Id of the Product Configuration.
     * @return boolean true if access is allowed, and false otherwise.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public boolean showLinksForSalableConfig(Context context, String args[]) throws Exception
    {
      //Getting programMap from the args
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String objectId = (String)programMap.get("objectId");
      boolean bDisplay = false;
      boolean bSalable = isSalableProductConfiguration(context, objectId);
      if(!bSalable)
      {
          bDisplay = true;
      }
      return bDisplay;
    }


    /**
     * Check whether the Product Configuration is Salable
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Object Id of the Product Configuration.
     * @return boolean true if Product Configuration is Salable.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
     public boolean isSalableProductConfiguration(Context context, String objectId) throws Exception
     {

        StringList selectStmts = new StringList(2);
        selectStmts.addElement(DomainConstants.SELECT_CURRENT);
        selectStmts.addElement("attribute[" + ProductLineConstants.ATTRIBUTE_VALIDATION_STATUS + "]");

        DomainObject productConfigObj = new DomainObject(objectId);
        //Getting the current state and Validation Status attribute value
        Map productConfigInfomap = productConfigObj.getInfo(context,selectStmts);

        String curState = (String)productConfigInfomap.get(DomainConstants.SELECT_CURRENT);
        String sValidationStatus = (String)productConfigInfomap.get("attribute[" + ProductLineConstants.ATTRIBUTE_VALIDATION_STATUS + "]");

        boolean bSalable = false;
        // PRD Config is not for salable in validation status is Not Validated & status is Preliminary
        if( sValidationStatus.equals("Not Validated") &&
            !curState.equals(ProductLineConstants.STATE_PRODUCT_CONFIGURATION_PRELIMINARY))
        {
            //Getting all the states of the Policy
            //String mqlReturn=MqlUtil.mqlCommand(context,"print policy '" + ProductLineConstants.POLICY_PRODUCT_CONFIGURATION + "' select state dump |");
        	
        	String strPolicy = ProductLineConstants.POLICY_PRODUCT_CONFIGURATION;
			String strState  = "state";
			String strDumpChar = "|";
			String strMQL = "print policy $1 select $2 dump $3";
			String mqlReturn = MqlUtil.mqlCommand(context, strMQL, strPolicy, strState, strDumpChar);
			
            StringTokenizer policyStates = new StringTokenizer(mqlReturn,"|");
            Vector states = new Vector();

            while (policyStates.hasMoreTokens())
            {
                String name = policyStates.nextToken();
                states.add(name);
            }

            int iActiveState = states.indexOf("Active");
            int iCurrentState = states.indexOf(curState);
            //Donot display the link when state is Active and Validation Status equals Not Validated
            if(iCurrentState >= iActiveState)
            {
                bSalable = true;
            }
        }
        return bSalable;
    }

    // Wrapper method added to call from bean which requires return type as object
    /**
     * Check whether the Product Configuration is Salable
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Object Id of the Product Configuration.
     * @return Boolean true if Product Configuration is Salable.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
     public Boolean checkProductConfigSalable(Context context, String args[]) throws Exception
     {
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         String objectId = (String)programMap.get("id");
         return Boolean.valueOf(isSalableProductConfiguration(context, objectId));
     }

    /**
     * Show EBOM node if Product Configurations has EBOM connections
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Object Id of the Product Configuration.
     * @return boolean true if access is allowed, and false otherwise.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    public boolean showEBOM(Context context, String args[]) throws Exception
    {
        //Getting programMap from the args
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String)programMap.get("objectId");

        DomainObject productConfigObj = new DomainObject(objectId);
        boolean bDisplay = true;
        // retrun true for Non Product Config types, else check EBOM exists
        if((ProductLineConstants.TYPE_PRODUCT_CONFIGURATION).equals(productConfigObj.getType(context)))
        {
            StringList EBOMIds = productConfigObj.getInfoList(context, "from[" + ProductLineConstants.RELATIONSHIP_EBOM + "].to.id");

            //return false if No EBOM exists for configuration
            if(EBOMIds == null || EBOMIds.size() == 0)
            {
                bDisplay = false;
            }
        }
        return bDisplay;
  }
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandAllFeatureStructureBasedOnSelectedOptions(Context context, String[] args)
    throws Exception {
    	return getSelectedOptionsList(context, args, null);
    }
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandMarketingFeatureStructureBasedOnSelectedOptions(Context context, String[] args)
    throws Exception {
    	return getSelectedOptionsList(context, args, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES);
    }
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandTechnicalFeatureStructureBasedOnSelectedOptions(Context context, String[] args)
    throws Exception {
        return getSelectedOptionsList(context, args, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);
    }
    /**
     * This method  returns Configuration/Logical/Both Features for Selected Options View
     *
     * @param context
     * @param args
     * @param featureType
     * @return
     * @throws Exception
     */
	private MapList getSelectedOptionsList(Context context, String[] args, String featureType)
	throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		String productConfigId = (String) paramMap.get("parentOID");
		if( ! isTypePC(context, productConfigId) )
		{
			productConfigId = (String) paramMap.get("parentId");
			if( ! isTypePC(context, productConfigId) )
			{
				productConfigId = (String) paramMap.get("objectId");
				if( ! isTypePC(context, productConfigId) )
				{
					return new MapList();
				}
			}
		}

		int iLevel = ConfigurationUtil.getLevelfromSB(context,args);

		ProductConfiguration pConf = new ProductConfiguration(productConfigId,
				ProductConfiguration.NONINTERACTIVE_MODE);
		pConf.setUserAction(ProductConfiguration.ACTION_SELECTED_OPTIONS);
		pConf.setId(productConfigId);
		pConf.initContext(context);
		String contextId  = pConf.getContextId();
		String parentProdId  =  pConf.getParentProductId();
		pConf.loadContextStructure(context, contextId,parentProdId);
		pConf.loadContextLogicalStructure(context, contextId, parentProdId);
		pConf.loadSelectedOptions(context);
		//1.To create a vector of relationship IDs to which ProductConfiguration is connected.
		MapList _selectedOptionList = pConf.getSelectedOptions(context,
				productConfigId, true, true);
		ArrayList _selectedOptionsRelIds = new ArrayList();
		for (int i = 0; i < _selectedOptionList.size(); i++) {
			Map selectedOption = (Map) _selectedOptionList.get(i);
			String _SOFeatureRelID = (String) selectedOption
					.get(DomainRelationship.SELECT_ID);
			_selectedOptionsRelIds.add(_SOFeatureRelID);
		}
		String objectId = (String) paramMap.get("objectId");
		DomainObject objectToBeExpanded = new DomainObject(objectId);
		List<IProductConfigurationFeature> topLevelfeatures = null;
		List<IProductConfigurationFeature> childFeatures = null;
		String objType = objectToBeExpanded.getInfo(context,
				DomainConstants.SELECT_TYPE);
		MapList out_selectedFeaturesMapList = new MapList();
		if (mxType.isOfParentType(context,objType,ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)) {
			topLevelfeatures = pConf.getTopLevelFeatures(context);
			if (topLevelfeatures != null) {
					for(IProductConfigurationFeature pSelectedFeature:topLevelfeatures){
						if ((featureType == null || ProductLineUtil
										.getChildrenTypes(context, featureType)
										.contains(pSelectedFeature.getType())))
						{
							pSelectedFeature.getSelectedFeaturesForSB(context,
									_selectedOptionsRelIds, out_selectedFeaturesMapList, iLevel);
						}
					}
			}
		} else {
			String featureRelId = (String) paramMap.get("relId");
			IProductConfigurationFeature featureToBeExpanded = (IProductConfigurationFeature) pConf
					.getFeature(featureRelId).get(0);
			childFeatures = featureToBeExpanded.getChildren();
			if (childFeatures != null) {
				for (int i = 0; i < childFeatures.size(); i++) {
					IProductConfigurationFeature pSelectedFeature = (IProductConfigurationFeature) childFeatures
							.get(i);
					int level = Integer.parseInt(pSelectedFeature.getLevel());
					pSelectedFeature.getSelectedFeaturesForSB(context, _selectedOptionsRelIds, out_selectedFeaturesMapList, level);
			}
		}
		}
		HashMap hmTemp = new HashMap();
		hmTemp.put("expandMultiLevelsJPO", "true");
		out_selectedFeaturesMapList.add(hmTemp);
		return out_selectedFeaturesMapList;
	}
    private boolean isTypePC(Context context, String productConfigId) throws FrameworkException, Exception {
    	if(productConfigId != null)
    	{
    		String type =  new DomainObject(productConfigId).getInfo(context, DomainConstants.SELECT_TYPE);
    		if( mxType.isOfParentType(context, type, ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION) )
    		{
    			return true;
    		}
    	}
		return false;
	}
    public Vector getQuantity(Context context, String []args) throws Exception{
        Vector vecAttrVals = new Vector();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList lstObjectIdsList = (MapList)programMap.get("objectList");
        HashMap htbParam = (HashMap)programMap.get("paramList");
        String strProductConfigurationId = (String)htbParam.get("objectId");


        for (int i = 0 ;i <lstObjectIdsList.size() ;i++ )
        {
        	String strObjId = "";
        	String strRelId = "";
            if (lstObjectIdsList.get(i) instanceof  HashMap ) {
                HashMap map = (HashMap )lstObjectIdsList.get(i) ;
                strObjId = (String)map.get(DomainConstants.SELECT_ID);
                strRelId = (String)map.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }else if (lstObjectIdsList.get(i) instanceof  Hashtable ) {
                Hashtable htObj = (Hashtable)lstObjectIdsList.get(i) ;
                strObjId = (String)htObj.get(DomainConstants.SELECT_ID);
                strRelId = (String)htObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }

            DomainObject domFtr = new DomainObject(strObjId);
            String strDomFtrType = domFtr.getInfo(context,DomainConstants.SELECT_TYPE);
            if(mxType.isOfParentType(context,strDomFtrType,ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION))
            {
                vecAttrVals.addElement("");
            }
            else
            {
            	StringBuffer selectable = new StringBuffer();
            	String mqlCmd = "print connection $1 select $2 dump $3";
            	selectable.append("tomid[").append(ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS);
            	selectable.append("|from.id==").append(strProductConfigurationId).append("]");
            	selectable.append(".attribute[").append(ConfigurationConstants.ATTRIBUTE_QUANTITY).append("]");
            	String strQuantity = MqlUtil.mqlCommand(context , mqlCmd, true,strRelId,selectable.toString(),ConfigurationConstants.DELIMITER_PIPE);
                vecAttrVals.addElement(strQuantity);
                }
        }

        return vecAttrVals ;
    }

    public Vector getKeyInValue(Context context, String []args) throws Exception{

        Vector vecAttrVals = new Vector();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList lstObjectIdsList = (MapList)programMap.get("objectList");
        HashMap htbParam = (HashMap)programMap.get("paramList");
        String strProductConfigurationId = (String)htbParam.get("objectId");


        for (int i = 0 ;i <lstObjectIdsList.size() ;i++ )
        {

          	String strObjId = "";
          	String strRelId = "";
            if (lstObjectIdsList.get(i) instanceof  HashMap ) {
                HashMap map = (HashMap )lstObjectIdsList.get(i) ;
                strObjId = (String)map.get(DomainConstants.SELECT_ID);
                strRelId = (String)map.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }else if (lstObjectIdsList.get(i) instanceof  Hashtable ) {
                Hashtable htObj = (Hashtable)lstObjectIdsList.get(i) ;
                strObjId = (String)htObj.get(DomainConstants.SELECT_ID);
                strRelId = (String)htObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }

              DomainObject domFtr = new DomainObject(strObjId);
              String strDomFtrType = domFtr.getInfo(context,DomainConstants.SELECT_TYPE);
            if(mxType.isOfParentType(context,strDomFtrType,ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION))
            {
                vecAttrVals.addElement("");
            }
            else
            {
              	StringBuffer selectable = new StringBuffer();
              	String mqlCmd = "print connection $1 select $2 dump $3";
              	selectable.append("tomid[").append(ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS);
              	selectable.append("|from.id==").append(strProductConfigurationId).append("]");
              	selectable.append(".attribute[").append(ConfigurationConstants.ATTRIBUTE_KEY_IN_VALUE).append("]");
              	String strQuantity = MqlUtil.mqlCommand(context , mqlCmd, true,strRelId,selectable.toString(),ConfigurationConstants.DELIMITER_PIPE);
                vecAttrVals.addElement(strQuantity);
            }
        }

         return vecAttrVals ;
    }



    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void connectTopLevelPart(Context context,String[] args) throws Exception
    {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map)programMap.get("requestMap");
        String toplevelPartId = (String)requestMap.get("topLevelPartOID");
        String objectId = (String)requestMap.get("objectId");
        DomainObject pcObject = new DomainObject(objectId);
        String strTopLevelPartRelationship = ProductLineConstants.RELATIONSHIP_TOP_LEVEL_PART;
        if(objectId != null && ! ("".equals(objectId)) && ! (("null").equals(objectId)))
        {
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            java.util.Map relatedObjects = pcObject.getRelatedObject(context, strTopLevelPartRelationship, true, new StringList(ConfigurationConstants.SELECT_ID), selectRelStmts) ;
            if( relatedObjects != null && ! relatedObjects.isEmpty())
            {
                String existingRelId = (String)relatedObjects.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                DomainRelationship.disconnect(context, existingRelId);
            }
        }
        if(toplevelPartId != null && ! ("".equals(toplevelPartId)) && ! (("null").equals(toplevelPartId)))
        {
            RelationshipType relType = new RelationshipType(strTopLevelPartRelationship);
            pcObject.addRelatedObject(context, relType, false, toplevelPartId);
        }
    }

    public static String ATTRIBUTE_PARENT_MARKETING_NAME = "Parent Marketing Name";
    public static String ATTRIBUTE_CHILD_MARKETING_NAME = "Child Marketing Name";
    public static String ATTRIBUTE_MARKETING_NAME = "Marketing Name";

    /**
     * To obtain the list of Selectables for Marketing Features Selecatable in Product Configuration Search
     *
     * @param context- the eMatrix <code>Context</code> object
     * @param args- holds the HashMap containing the following arguments
     * @return  String- Marketing Features
     * @throws Exception if the operation fails
     * @author Sandeep Kathe(klw)
     */
    public String getProductConfigurationFeatures(Context context,String[] args){

        StringBuffer features = new StringBuffer();
        MapList featureListMap = new MapList();
        try{
          String prodcutConfigurationId=args[0];
          if (prodcutConfigurationId != null && !prodcutConfigurationId.equals("")) // process if not null
          {
            SelectList selectables = new SelectList(4);
            selectables.add("from."+ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
            selectables.add("to."+ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
            selectables.add("from."+ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
            selectables.add("to."+ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
            featureListMap=ProductConfiguration.getSelectedOptions(context, prodcutConfigurationId, false, true);
            String [] strRelIdArr = new String[featureListMap.size()];
            for(int i=0;i<featureListMap.size();i++){
                Map temp = (Map)featureListMap.get(i);
                strRelIdArr[i] = (String)temp.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
            }
            featureListMap = new MapList();
            featureListMap = DomainRelationship.getInfo(context, strRelIdArr, selectables);
            for(int i=0;i<featureListMap.size();i++){
                Map temp = (Map)featureListMap.get(i);

                String strPrntDspName = (String) temp.get("from."+ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
                String strChldDspName =(String) temp.get("to."+ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
                String strPrntMrkName = (String) temp.get("from."+ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
                String strChldMrkName =(String) temp.get("to."+ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);

                if(ProductLineCommon.isNotNull(strPrntDspName)){
                	features.append(strPrntDspName);
                }else{
                	features.append(strPrntMrkName);
                }
                features.append(":");
                if(ProductLineCommon.isNotNull(strChldDspName)){
                	features.append(strChldDspName);
                }else{
                	features.append(strChldMrkName);
                }
                features.append(SelectConstants.cSelectDelimiter);
            }
          }

        }catch(Exception e){
            e.printStackTrace();
        }
        return  features.toString();
    }



    /**
     * To search for top level part from the product configuration edit page
     *
     * @param context- the eMatrix <code>Context</code> object
     * @param args- holds the HashMap containing the following arguments
     * @return  String- Programed HTML for the Top Level Row.
     * @throws Exception if the operation fails
     * @author Sandeep Kathe(klw)
     */
    public String getTopLevelPartChooser(Context context,String[] args){
    StringBuffer sbBuffer  = new StringBuffer();
    try{
    Map programMap = (HashMap) JPO.unpackArgs(args);
    Map mpRequest = (HashMap) programMap.get("requestMap");
    String strMode = (String)mpRequest.get("mode");
    Map paramMap= (HashMap)programMap.get("paramMap");
    Map requestMap = (HashMap)programMap.get("requestMap");
    String strReportFormat=(String)requestMap.get("reportFormat");
    String strObjectId = paramMap.get("objectId").toString();
    DomainObject objConfiguration = new DomainObject(strObjectId);
    String strProductId=objConfiguration.getInfo(context,"to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");
    String strTopLevelPartId=objConfiguration.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART+"].to.id");
    String strTopLevelPartName=objConfiguration.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART+"].to.name");

    if (strTopLevelPartId == null || strTopLevelPartId.equalsIgnoreCase("null")) {
    	strTopLevelPartId = "";
    }
    if (strTopLevelPartName == null || strTopLevelPartName.equalsIgnoreCase("null")) {
    	strTopLevelPartName = "";
    }

    Map fieldMap = (HashMap) programMap.get("fieldMap");
    String strFieldName = (String)fieldMap.get("name");

   if(strMode!=null && !strMode.equals("") &&
            !strMode.equalsIgnoreCase("null") && strMode.equalsIgnoreCase("edit")){
    sbBuffer.append("<input type=\"text\" READONLY ");
    sbBuffer.append("name=\"");
    sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
    sbBuffer.append("Display\" id=\"\" value=\"");
    sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strTopLevelPartName));
    sbBuffer.append("\">");
    sbBuffer.append("<input type=\"hidden\" name=\"");
    sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
    sbBuffer.append("OID\" value=\"");
    sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strTopLevelPartId));
    sbBuffer.append("\">");
    sbBuffer.append("<input ");
    sbBuffer.append("type=\"button\" name=\"btnTopLevelPart\"");
    sbBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
    //Code change for IR-018831V6R2011- excluded Parts which has "Configured Part" Policy
    //R418HF2- include--> EC Part, exclude--> Development Part Configured Part Standard Part 
    sbBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Part:HAS_EBOM_CONNECTED=false:IS_TOP_LEVEL_PART=false:CURRENT!=policy_ECPart.state_Obsolete:POLICY!=policy_DevelopmentPart,policy_StandardPart,policy_ConfiguredPart&ParentProductId="+XSSUtil.encodeForHTMLAttribute(context, strProductId)+"&table=PLCSearchPartsTable&showInitialResults=false&selection=single&submitAction=refreshCaller&hideHeader=true&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=FormChooser&fieldNameActual="+XSSUtil.encodeForHTMLAttribute(context, strFieldName)+"OID&fieldNameDisplay="+XSSUtil.encodeForHTMLAttribute(context, strFieldName)+"Display&formName=editDataForm&frameName=formEditDisplay&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp");
    sbBuffer.append("','900','600')\">");
    sbBuffer.append("&nbsp;&nbsp;");
    sbBuffer.append("<a href=\"javascript:basicClear('");
    sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
    sbBuffer.append("')\">");


    String strClear =
    	EnoviaResourceBundle.getProperty(context, "ProductLine",
                                "emxProduct.Button.Clear",
                                context.getSession().getLanguage());
    sbBuffer.append(strClear);
    sbBuffer.append("</a>");
   }
   else{


       StringBuffer stbPartImage = new StringBuffer();
       StringBuffer sIncLink = new StringBuffer();
       String strEndPart1 = "";
	   String strEndPart2 = "";
       if(!"CSV".equalsIgnoreCase(strReportFormat))
       {
    	   stbPartImage = stbPartImage.append("<img src=\"../common/images/iconSmallPart.gif").append("\"").append("/>");
    	   sIncLink = sIncLink.append("<a href=\"JavaScript:showDialog('../common/emxTree.jsp?objectId=");
    	   sIncLink = sIncLink.append(XSSUtil.encodeForHTMLAttribute(context, strTopLevelPartId));
    	   strEndPart1 = "')\">";
    	   strEndPart2 = "</a>";
       }
       StringBuffer sb = new StringBuffer();
       sb = sb.append(sIncLink.toString());
       sb = sb.append(strEndPart1).append(XSSUtil.encodeForXML(context, strTopLevelPartName)).append(strEndPart2);
       if(strTopLevelPartId!=null && strTopLevelPartId.length()>0){
       sbBuffer.append(stbPartImage +" "+ sb.toString());
       }
       else{
           sbBuffer.append(" ");
       }

   }
    }catch(Exception e){
        e.printStackTrace();
    }
    return sbBuffer.toString();
}


    /**
     * To obtain the selectable if a relationship exists or not
     *
     * @param context- the eMatrix <code>Context</code> object
     * @param args 1- Hold the id of the object for which the selectable is obtained
     * @param args 2- Defines the relationship for which the check is done
     * @param args 3- Defines the direction of expansion to be used
     * @return  String- Boolean to indicate if the relationship exists or not
     * @throws Exception: Returns false in the case of exception
     */
    public String getRelExists(Context context,String[] args){
        String getRelExists = "False";
        try{
          String strObjectID = args[0];
          String strRelName = args[1];
          strRelName = PropertyUtil.getSchemaProperty(context,strRelName);
          String strRelDirection = args[2];
          String strSelect = strRelDirection + "[" + strRelName + "]";
          if (strObjectID != null && !strObjectID.equals("")) // process if not null
          {
            // create domain object of the ProductConfiguration
            DomainObject domObject = new DomainObject(strObjectID);
            // get the Featurelists connected to the particular featurelist and the slectables
            getRelExists = domObject.getInfo(context,strSelect);
          }
        }catch(Exception e){
    	getRelExists = "False";
    }
        return  getRelExists;
    }


    /**
     * This method is used to check if the context ID is not in InActive state while PC creation.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return boolean- false if the context object is in Inactive state else return true.
     * @throws Exception if the operation fails
     * @since FTR R207
    **/

    public boolean showCommandForPCCreate(Context context,String args[]) throws Exception
    {
        HashMap objectMap = (HashMap) JPO.unpackArgs(args);
        String strContextId = (String) objectMap.get("objectId");
        boolean showCommand = true;

        if(strContextId!=null && !strContextId.isEmpty())
        {
            DomainObject domCtxObj = new DomainObject(strContextId);
            String strObjectState = domCtxObj.getInfo(context,DomainObject.SELECT_CURRENT);
            String strObjectType = domCtxObj.getInfo(context, DomainObject.SELECT_TYPE);
   	     	StringList attributeList = new StringList();
   	     	attributeList.add(DomainConstants.SELECT_ID);

	   	  if(ProductLineConstants.STATE_INACTIVE.equals(strObjectState))
	   	     {
                showCommand = false;
	   	     }else
	   	     {
	   	    	showCommand =  true;
          }
	   	  
	   	  if(ProductLineConstants.TYPE_PRODUCT_CONFIGURATION.equals(strObjectType) && ProductLineConstants.STATE_ACTIVE.equals(strObjectState)){
	   		    showCommand = false;
	   	  }
        }

        return showCommand;
    }

    /**
     * Override trigger for the context object delete
     * deletes the Product Configurations attached to this context object
     * @param context
     * @param args
     * @throws Exception
     */
    public void onContextObjectDelete(Context context,String args[]) throws Exception
    {
    	try
    	{
        	String ContextID = args[0];
           	DomainObject contextDom = new DomainObject(ContextID);
        	StringBuffer sbBuffer = new StringBuffer();
        	sbBuffer.append(ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION);
            sbBuffer.append(STR_COMMA);
            sbBuffer.append(ProductLineConstants.RELATIONSHIP_STANDARD_ITEM);
            sbBuffer.append(STR_COMMA);
            sbBuffer.append(ProductLineConstants.RELATIONSHIP_CUSTOM_ITEM);
            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
            StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            short shRecursionLevel = 1;
        	MapList PCList =
                contextDom.getRelatedObjects(
                  context,
                  sbBuffer.toString(),
                  ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
                  objectSelects,
                  relSelects,
                  false,
                  true,
                  shRecursionLevel,
                  DomainConstants.EMPTY_STRING,
                  DomainConstants.EMPTY_STRING, 0);
        	String []strObjectIds = new String[PCList.size()];
        	for(int i = 0; i < PCList.size(); i++)
        	{
        		Map pcMap = (Map)PCList.get(i);
        		String PCId = (String)pcMap.get(DomainConstants.SELECT_ID);
        		strObjectIds[i] = PCId;
        	}
        	ProductConfiguration.deleteObjects(context, strObjectIds);
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    }

    /**
     * Check trigger for context object delete
     * checks whether the Product Configurations attached to this context object can abe deleted
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public int canProductConfigurationsBeDeleted(Context context, String []args) throws Exception
    {
    	String ContextID = args[0];
       	DomainObject contextDom = new DomainObject(ContextID);
    	StringBuffer sbBuffer = new StringBuffer();
    	sbBuffer.append(ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION);
        sbBuffer.append(STR_COMMA);
        sbBuffer.append(ProductLineConstants.RELATIONSHIP_STANDARD_ITEM);
        sbBuffer.append(STR_COMMA);
        sbBuffer.append(ProductLineConstants.RELATIONSHIP_CUSTOM_ITEM);
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        short shRecursionLevel = 1;
    	MapList PCList =
            contextDom.getRelatedObjects(
              context,
              sbBuffer.toString(),
              ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
              objectSelects,
              relSelects,
              false,
              true,
              shRecursionLevel,
              DomainConstants.EMPTY_STRING,
              DomainConstants.EMPTY_STRING, 0);
    	String []strObjectIds = new String[PCList.size()];
    	for(int i = 0; i < PCList.size(); i++)
    	{
    		Map pcMap = (Map)PCList.get(i);
    		String PCId = (String)pcMap.get(DomainConstants.SELECT_ID);
    		strObjectIds[i] = PCId;
    	}
    	try
    	{
    		ProductConfiguration.canBeDeleted(context, strObjectIds);
    		return 0;

    	}
    	catch(Exception ex)
    	{
    		return 1;
    	}
    }

	   /**
     * Trigger method to check for the relationship Inactive Varies By, from Product to Feature
     * If any of them is present in the Selected Options, then fail
     *
     * @param  context the eMatrix <code>Context</code> object
     * @param  args holds the object id for product configuration.
     * @return Returns a <code>int</code> object.
     * @throws Exception if the operation fails
     * @author KXB
     * @since  FTR R212
     */
     public int inactiveFeaturePromoteStateCheck(Context context,String[] args) throws Exception
     {
        //check for the relationship Inactive Feature List From, from Product to Feature List.
        //If any of them is present in the Selected Options, then fail
        String stPCId = (String) args[0];
        boolean bFlag=false;
        String relationshipName = ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS ;
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = null;
        short shRecursionLevel = 1;
        ProductConfiguration pcBean = new ProductConfiguration();
        MapList relBusObjPageList = pcBean.getSelectedOptions(context, stPCId,true,true);
        StringList attributeList = new StringList();
        attributeList.add(DomainConstants.SELECT_ID);
        DomainObject productConfiguration = DomainObject.newInstance(context);
        productConfiguration.setId(stPCId);
        //Retrieve the product connected with the Product Configuration
        Map htbProduct =(Hashtable) productConfiguration.getRelatedObject(context,
															              ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION,
															              false,
															              attributeList,
															              new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID));
        String productId = (String) htbProduct.get(DomainConstants.SELECT_ID);
        StringBuffer sbType = new StringBuffer(20);
        sbType.append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURES);
        relationshipName = ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY;
        shRecursionLevel = 1;
		LogicalFeature lfBean = new LogicalFeature(productId);
		MapList relBusObjList = lfBean.getInactiveDesignVariants(context,
																 sbType.toString(),
																 relationshipName,
																 objectSelects,
										                         relSelects,
										                         false,
										                         true,
										                         shRecursionLevel,
										                         0,
										                         null,
										                         null,
										                         (short)0,
										                         null);
        for(int iCount = 0; iCount < relBusObjList.size();iCount++)
        {
            String string1 = (String)((Map) relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
            bFlag = false;
            for (int j = 0; j < relBusObjList.size(); j++)
            {
                String string2 = (String)((Map) relBusObjList.get(j)).get(DomainConstants.SELECT_ID);
                if(string1.equals(string2))
                {
                    bFlag = true;
                    break;
                }
            }//end of inner for
            if(!bFlag)
            {
             break;
            }
        }//end of outer for
        if(bFlag)
        {
            //Check Trigger blocked
            String strPromoteStateError = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.InactiveFeaturePresent",
												                context.getSession().getLanguage());
            emxContextUtil_mxJPO.mqlNotice(context, strPromoteStateError);
            return 1;
        }else
        {
          //Check Trigger Passed
          return 0;
        }
    }

	   /**
     * Trigger method to generate the Precise BOM from the lifecycle promote "Validate Configuration -> Generate BOM"
     *
     * @param  context the eMatrix <code>Context</code> object
     * @param  args holds the object id for product configuration.
     * @return Returns a <code>int</code> object.
     * @throws Exception if the operation fails
     * @author KXB
     * @since  FTR R212
     */
     public int generatePreciseBOMPromoteStateAction(Context context,String[] args)throws Exception
     {
    	try {
            String[] apps = {"CFE"};
            ComponentsUtil.checkLicenseReserved(context, apps);
        } catch (MatrixException e) {
            throw new FrameworkException(e);
        }
        String strProductConfigurationID = args[0];
        ProductConfiguration pcBean = new ProductConfiguration();
        PropertyUtil.setGlobalRPEValue(context,  "CheckValidateState", "false");
        ArrayList resultList = pcBean.generatePreciseBOMForProductConfiguration(context, strProductConfigurationID);
        if(resultList.size()== 2)
	    {
	        Object resultVar = resultList.get(1);
	        if(resultVar.equals("0"))
	        {
	        	return 0;
	        }
	    }
    	return 1;
     }

    /**
      * Trigger Method called to check if the Parent Product on which the Product
      * Configuration is based on is in release state
      *
      * @param  context the eMatrix <code>Context</code> object
      * @param  args holds the Object Id of the Product Configuration.
      * @return int 0 - succcess
      *             1 - failure.
      * @throws Exception if the operation fails
      * @since  FTR R212
      */
      public int isProdConfContextObjectReleased(Context context,String[] args)throws Exception{
             String stObjectId = (String) args[0];
             String strState = "";
             String strRelease = "";
             DomainObject productConfiguration = DomainObject.newInstance(context);
             productConfiguration.setId(stObjectId);
             /* Retrieve the product connected with the productConfiguration */
             StringList attributeList = new StringList();
             attributeList.add(DomainConstants.SELECT_ID);
             Map htbProduct =(Hashtable) productConfiguration.getRelatedObject(context,
													                           ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION,
													                           false,
													                           attributeList,
													                           new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID));
             String productId = (String) htbProduct.get(DomainConstants.SELECT_ID);
             DomainObject product = DomainObject.newInstance(context);
             product.setId(productId);
             strState = product.getInfo(context,DomainConstants.SELECT_CURRENT);
             String strPolicy = PropertyUtil.getSchemaProperty(context,SYMB_policy_Product);
             strRelease = FrameworkUtil.lookupStateName(context, strPolicy, SYMB_state_Release);
             if(strState.equals(strRelease))
             {
            	 //Check Trigger Passed
                 return 0;
             }else
             {
                 //Check Trigger blocked
                 String strPromoteStateError = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                     "emxProduct.Alert.ParentNotInRelease",
                     context.getSession().getLanguage());
                 emxContextUtil_mxJPO.mqlNotice(context, strPromoteStateError);
                 return 1;
             }
       }

   /**
     * Trigger checks whether Product Configuration is created for a Salable Part
     *
     * @param  context the eMatrix <code>Context</code> object
     * @param  args holds the arguments passed by trigger. ObjectId
     * @return Boolean
     * @throws Exception if the operation fails
     * @since  FTR R212
     */
     public int isSalableProdConfiguration(Context context, String[] args)throws Exception{
         String objectId = args[0];
         boolean bSalable = isSalableProdConfiguration(context ,objectId);
         // If the Product Configuration is created for a Salable Part then do not allow to demote
         if(bSalable)
         {
             String language = context.getSession().getLanguage();
             String strSalable =
               EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                 "emxProduct.TiggerMsg.SalableProductConfiguration",language);

             throw new FrameworkException(strSalable);
         }
         return 0;
     }

    /**
      * Check whether the Product Configuration is Salable
      *
      * @param  context the eMatrix <code>Context</code> object
      * @param  args holds the Object Id of the Product Configuration.
      * @return boolean true if Product Configuration is Salable.
      * @throws Exception if the operation fails
      * @since  FTR R212
      */
      public boolean isSalableProdConfiguration(Context context, String objectId) throws Exception
      {
         StringList selectStmts = new StringList(2);
         selectStmts.addElement(DomainConstants.SELECT_CURRENT);
         selectStmts.addElement("attribute[" + ProductLineConstants.ATTRIBUTE_VALIDATION_STATUS + "]");

         DomainObject productConfigObj = new DomainObject(objectId);
         //Getting the current state and Validation Status attribute value
         Map productConfigInfomap = productConfigObj.getInfo(context,selectStmts);

         String curState = (String)productConfigInfomap.get(DomainConstants.SELECT_CURRENT);
         String sValidationStatus = (String)productConfigInfomap.get("attribute[" + ProductLineConstants.ATTRIBUTE_VALIDATION_STATUS + "]");

         boolean bSalable = false;
         // PRD Config is not for salable in validation status is Not Validated & status is Preliminary
         if( sValidationStatus.equals("Not Validated") &&
             !curState.equals(ProductLineConstants.STATE_PRODUCT_CONFIGURATION_PRELIMINARY))
         {
             //Getting all the states of the Policy
        	 String mqlCommand = "print policy $1 select $2 dump $3";
             String mqlReturn=MqlUtil.mqlCommand(context,mqlCommand,ProductLineConstants.POLICY_PRODUCT_CONFIGURATION,"state",ConfigurationConstants.DELIMITER_PIPE);
             StringTokenizer policyStates = new StringTokenizer(mqlReturn,"|");
             Vector states = new Vector();

             while (policyStates.hasMoreTokens())
             {
                 String name = policyStates.nextToken();
                 states.add(name);
             }

             int iActiveState = states.indexOf("Active");
             int iCurrentState = states.indexOf(curState);
             //Donot display the link when state is Active and Validation Status equals Not Validated
             if(iCurrentState >= iActiveState)
             {
                 bSalable = true;
             }
         }
         return bSalable;
     }

      /**
       * This method is includeOIDprogram for dbchooser scenario in PC
       * @param context
       * @param args
       * @return
       * @throws Exception
       */
      public StringList getMPRIncludeOIDs(Context context, String[] args) throws Exception
 {
		StringList includeOIDList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String MPRIds = (String) programMap.get("MPRIds");
		String strSelectOptions = "from["
				+ ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION
				+ "].torel.to.id";
		StringList objSelects = new StringList();
		objSelects.addElement(SELECT_ID);
		objSelects.addElement(SELECT_TYPE);
		objSelects.addElement(SELECT_NAME);
		DomainObject.MULTI_VALUE_LIST.add(strSelectOptions);
		objSelects.addElement(strSelectOptions);
		StringList relSelects = new StringList();
		relSelects.addElement(SELECT_ID);
		relSelects.addElement(SELECT_NAME);
		StringList ids = new StringList();
		ids = FrameworkUtil.split(MPRIds, ",");

		MapList mlMprResultIds = DomainObject.getInfo(context, (String[]) ids
				.toArray(new String[ids.size()]), objSelects);
		DomainObject.MULTI_VALUE_LIST.remove(strSelectOptions);
		if (mlMprResultIds != null && mlMprResultIds.size() > 0) {
			Map mprObjBase = (Map) mlMprResultIds.get(0);
			List slConfOptionsBase = (StringList) ((Map) mprObjBase)
					.get(strSelectOptions);
			// First set of CO's
			includeOIDList.addAll(slConfOptionsBase);
            //start iterating from 2nd set onwords
			for (int j = 1; j < mlMprResultIds.size(); j++) {
				Map mprObj = (Map) mlMprResultIds.get(j);
				List slConfOptions = (StringList) ((Map) mprObj)
						.get(strSelectOptions);
				includeOIDList.retainAll(slConfOptions);
				if (includeOIDList.size() == 0) {
					break;
				}
			}
		}
		return includeOIDList;
}

      /**
	 	 * This is a Access Function to find whether the object from which the commmand is called is of type Products
	 	 *
	 	 * @param context - the eMatrix <code>Context</code> object
	 	 * @param args Holds ParamMap having the key "parentOID" which will determine if the Object is of type Product, only then
	 	 * command will be dispplayed in Reports Menu
	 	 * @return bResult - true - if the Object from which the command callled is of type Product
	 	 * 					 false - if the Object from which the command callled is NOT of type Product
	 	 * @throws Exception  - throws Exception if any operation fails.
	 	 */

	 	public boolean isContextProduct (Context context, String args[]) throws Exception{
	 		boolean bResult = false;

	 		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	 		String parentOID = (String)paramMap.get("objectId");
	 		DomainObject domObject = new DomainObject(parentOID);
	 		bResult = !domObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCT_VARIANT) && domObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS);

	 		return bResult;
	 	}
	 	 /**
	 	 * This is a Access Function to find whether the object from which the commmand is called is of type Product Variant
	 	 *
	 	 * @param context - the eMatrix <code>Context</code> object
	 	 * @param args Holds ParamMap having the key "parentOID" which will determine if the Object is of type Product Variant, only then
	 	 * command will be dispplayed in Reports Menu
	 	 * @return bResult - true - if the Object from which the command callled is of type Product Variant
	 	 * 					 false - if the Object from which the command callled is NOT of type Product Variant
	 	 * @throws Exception  - throws Exception if any operation fails.
	 	 */

	 	public boolean isContextProductVariant (Context context, String args[]) throws Exception{
	 		boolean bResult = false;

	 		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	 		String parentOID = (String)paramMap.get("objectId");
	 		DomainObject domObject = new DomainObject(parentOID);
	 		bResult = domObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCT_VARIANT);

	 		return bResult;
	 	}

	 	 /**
	 	 * This is a Access Function to find whether the object from which the commmand is called is of type Logical Feature
	 	 *
	 	 * @param context - the eMatrix <code>Context</code> object
	 	 * @param args Holds ParamMap having the key "parentOID" which will determine if the Object is of type Logical Feature, only then
	 	 * command will be dispplayed in Reports Menu
	 	 * @return bResult - true - if the Object from which the command callled is of type Logical Feature
	 	 * 					 false - if the Object from which the command callled is NOT of type Logical Feature
	 	 * @throws Exception  - throws Exception if any operation fails.
	 	 */

	 	public boolean isContextLogicalFeature (Context context, String args[]) throws Exception{
	 		boolean bResult = false;

	 		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	 		String parentOID = (String)paramMap.get("objectId");
	 		DomainObject domObject = new DomainObject(parentOID);
	 		bResult = domObject.isKindOf(context, ConfigurationConstants.TYPE_LOGICAL_FEATURE);

	 		return bResult;
	 	}


	    /**
		 *  Gets Columns to view Product Configuration grid and pass it on to GridComponent
		 *  to view product configuration		 *
		 *
		 * @param context
		 *            the eMatrix <code>Context</code> object
		 * @param args
		 *            holds input arguments.
		 * @return a MapList containing the Maps for rows values
		 *
		 * @throws Exception
		 *             if the operation fails
		 *
		 */
	 	@com.matrixone.apps.framework.ui.ColJPOCallable
	    public MapList getProductConfigurationForGrid(Context context, String[] args)throws FrameworkException {
	    	MapList colList = new MapList();
	    	try{
	    		String colGroupValue =   EnoviaResourceBundle.getProperty(context,SUITE_KEY,
	    		          "emxConfiguration.Table.ColumnGroupHeader",
	    		          context.getSession().getLanguage());
	    		StringList strListSelectstmts = new StringList();
	    		strListSelectstmts.add(DomainObject.SELECT_ID);
	    		strListSelectstmts.add(DomainObject.SELECT_NAME);
	    		strListSelectstmts.add(DomainObject.SELECT_REVISION);

	    		HashMap hash  = (HashMap) JPO.unpackArgs(args);

	    		HashMap paramList = (HashMap) hash.get("requestMap");
	    		String selectIds = (String)paramList.get("selectId");
	    		String [] idArr = selectIds.split(",");
	    		if (ProductLineCommon.isNotNull(selectIds)){
	    			Map newMap ;
	    			for (int count=0; count < idArr.length; count++ )
	    			{
	    				DomainObject pcID = new DomainObject((String)idArr[count]);
	    				newMap = pcID.getInfo(context,strListSelectstmts);
	    				colList.add(newMap);
	    			}
	    		}else {
	    			String stprObjectId = (String)paramList.get("objectId");
	    			colList = new DomainObject(stprObjectId).getRelatedObjects(context,
	    					ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION,
	    					ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION,
	    					strListSelectstmts,
	    					new StringList(),
	    					false,
	    					true,
	    					(short)0,DomainConstants.EMPTY_STRING,"",0);
	    		}
	    		Map tempmap;
	    		for (int i = 0; i < colList.size(); i++) {
	    			tempmap =(Map)colList.get(i);
	    			tempmap.put(UITableGrid.KEY_COL_ID, tempmap.get(DomainConstants.SELECT_ID));
	    			tempmap.put(UITableGrid.KEY_COL_VALUE,tempmap.get(DomainConstants.SELECT_NAME));
	    			tempmap.put(UITableGrid.KEY_COL_GROUP_VALUE,colGroupValue);

	    		}
	    	}catch (Exception e) {
	    		e.printStackTrace();
	    		throw new FrameworkException(e.getMessage());
	    	}
	    	return colList;
	    }
	    /**
		 *  Gets rows to view Product Configuration grid and pass it on to GridComponent
		 *  to view product configuration
		 *
		 *  This method will fetch all the CONFIGURATION STRUCTURES for a context product and check if its connected to a product configuration by "Selected Options"
		 *  If connected to a product configuration, it will put the feature ID against CELL_VALUEE for a particular CELL_ID(Product Configuration ID)
		 *
		 * @param context
		 *            the eMatrix <code>Context</code> object
		 * @param args
		 *            holds input arguments.
		 * @return a MapList containing the Maps for rows values
		 *
		 * @throws Exception
		 *             if the operation fails
		 *
		 */
	 	@com.matrixone.apps.framework.ui.RowJPOCallable
	    public MapList getCFStructureForPCCompareGrid(Context context, String[] args)throws FrameworkException {
	    	MapList rowList = new MapList();
	    	try{

	    		String cellIdSelect ="tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id";
	    		String cellIdKeyInValue ="tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].attribute["
	    									+ConfigurationConstants.ATTRIBUTE_KEY_IN_VALUE+"]";
	    		String languageStr = context.getSession().getLanguage();
	    		String shortHandNotationForKeyIn =EnoviaResourceBundle.getProperty(context,SUITE_KEY,
	    				"emxConfiguration.Table.SubHeader.SelectedOptionsComparison_ShortNotation",languageStr);

	    		String getOptions = "from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS+"].to.id";
	    		String attrConfigSelectionStatus ="tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].attribute["
	    				+ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_STATUS+"]";
	    		
	    		Map programMap = (Map) JPO.unpackArgs(args);
	    		String strObjectId = (String) programMap.get("objectId");
	    		String line ="<hr size=\"1\" style=\"margin-top:3px; margin-bottom: 3px;\" />";
	    		boolean exportToExcel = false;
	    		Map tempMap ;
	    		String exportFormat = null;
	    		HashMap requestMap = (HashMap)programMap.get("requestMap");
	    		if(requestMap!=null && requestMap.containsKey("reportFormat")){
	    			exportFormat = (String)requestMap.get("reportFormat");
	    		}
	    		if("CSV".equals(exportFormat)){
	    			exportToExcel = true;
	    		}

	    		String strTypePattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURES;
	    		String strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES;
	    		//String configOptions = "from[Configuration Features].to.from[Configuration Options].to.id";
	    		String fromType =  "to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].from.id";
	    		StringList slObjSelects = new StringList();
	    		slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
	    		slObjSelects.addElement(ConfigurationConstants.SELECT_TYPE);
	    		slObjSelects.addElement(ConfigurationConstants.SELECT_NAME);
	    		slObjSelects.addElement(ConfigurationConstants.SELECT_REVISION);
	    		slObjSelects.addElement(getOptions);
	    		slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT);
	    		slObjSelects.addElement(fromType);
	    		slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);


	    		StringList slRelSelects = new StringList();
	    		slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
	    		slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_TYPE);
	    		slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);
	    		slRelSelects.addElement(SELECT_FROM_NAME);
	    		slRelSelects.addElement(SELECT_FROM_ID);
	    		slRelSelects.addElement(ConfigurationConstants.SELECT_SEQUENCE_ORDER);
	    		slRelSelects.addElement(cellIdSelect);
	    		slRelSelects.addElement(cellIdKeyInValue);
	    		slRelSelects.addElement(attrConfigSelectionStatus);

	    		ConfigurationFeature cfBean = new ConfigurationFeature(strObjectId);
	    		// Fetching all the Configuration Structures  for a context product.
	    		MapList mapCFStructure = (MapList)cfBean.getConfigurationFeatureStructure(context,strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
	    				true,0,0, DomainConstants.EMPTY_STRING, DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,DomainConstants.EMPTY_STRING);
	    		// Sorting the map list based on sequence number attribute on a Configuration Features
	    		mapCFStructure.addSortKey(ConfigurationConstants.SELECT_SEQUENCE_ORDER, "ascending", "integer");
	    		mapCFStructure.sortStructure();

	    		Map cellIdvaluesMap=new HashMap();
	    		Map KeyInValueMap = new HashMap();
	    		MapList returmrowList = new MapList();
	    		StringList childOptionList = new StringList();
	    		Object optionValues;
	    		int aOptionCount = 0;
	    		
	    		// For each Configuration Feature or Options
	    		for (int i = 0; i < mapCFStructure.size(); i++) {

	    			tempMap =(Map)mapCFStructure.get(i);
	    			Map mapConfigSelectionStatus = new HashMap();
	    			if(tempMap.get(attrConfigSelectionStatus) instanceof StringList){
	    				StringList slAttrConfigSelectionStatus = (StringList)tempMap.get(attrConfigSelectionStatus);
	    				StringList listSelect = (StringList)tempMap.get(cellIdSelect);
	    				for (int k = 0;k<slAttrConfigSelectionStatus.size(); k++) {
	    					String strConfigSelectionStatus = (String) slAttrConfigSelectionStatus.get(k);
	    					mapConfigSelectionStatus.put((String) listSelect.get(k), strConfigSelectionStatus);
	    				}
	    			}else{
	    				String strAttrConfigSelectionStatus = (String)tempMap.get(attrConfigSelectionStatus);
	    				String cellId = (String)tempMap.get(cellIdSelect);
	    				mapConfigSelectionStatus.put(cellId, strAttrConfigSelectionStatus);
	    			}
	    			String cfId = "";
	    			boolean isTopLevelFeature = false;
	    			int subFeatureCount = Integer.parseInt((String)tempMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT));
 	    			Object fromIdRecived = tempMap.get(fromType);
	    			if (fromIdRecived instanceof StringList)
	    			{
	    				if(((StringList) fromIdRecived).contains(strObjectId)){
	    					isTopLevelFeature  = true;
	    				}
	    			}else if (ProductLineCommon.isNotNull((String)fromIdRecived) && ((String)fromIdRecived).equals(strObjectId) ){
	    				isTopLevelFeature = true;
	    			}
	    			/**
	    			 * Checking for each feature or options, whether it qualifies for a ROW_ID
	    			 * If a feature is a top level Configuration Feature or Configuration Feature with children it qualifies for ROW_ID
	    			 * */
	    			DomainObject tempObject = new DomainObject((String)tempMap.get(SELECT_ID));
	    			//if(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE.equalsIgnoreCase(stopType) && (subFeatureCount >0 || isTopLevelFeature || isListOfNull(tempMap.get(cellIdKeyInValue)))){
	    			if(tempObject.isKindOf(context,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE) && (subFeatureCount >0 || isTopLevelFeature || isListOfNull(tempMap.get(cellIdKeyInValue)))){
	    				aOptionCount = 0;
	    				if(cellIdvaluesMap.size()>0){
	    					createFinalMapList(cellIdvaluesMap, returmrowList,exportToExcel);
	    				}
	    				cfId = (String)tempMap.get(DomainConstants.SELECT_ID);
	    				tempMap.put(UITableGrid.KEY_ROW_ID, cfId);
	    				returmrowList.add(tempMap);
	    				cellIdvaluesMap=new HashMap();

	    				optionValues = tempMap.get(getOptions);
	    				if(optionValues!=null){
	    					if(optionValues instanceof String){
	    						childOptionList.add(optionValues);
	    					}else{
	    						childOptionList = (StringList) optionValues;
	    					}
	    				}
	    				/** IF a Feature has Key In value on it , it need to be processed differently.
	    				 * All Options or features that are cell values, has to appear first on UI
	    				 * So all key in values are stored in different Map and at the end appended to the final Map of options
	    				 * */
	    				if(tempMap.get(cellIdKeyInValue) !=null)
	    				{
	    					if (tempMap.get(cellIdKeyInValue) instanceof StringList){
	    						StringList listSelect = (StringList)tempMap.get(cellIdSelect);
	    						StringList listKeyIn =  (StringList)tempMap.get(cellIdKeyInValue);
	    						for (int j = 0;j<listSelect.size(); j++) {

	    							String strobject = (String) listSelect.get(j);
	    							String cellKeyToProcess =(String) listKeyIn.get(j);
	    							if(ProductLineCommon.isNotNull(cellKeyToProcess)){
	    								processCellIdrowMap(context,KeyInValueMap,strobject,cellKeyToProcess, true,false,exportToExcel);
	    							}
	    						}
	    					}else{
	    						String cellKeyToProcess = (String)tempMap.get(cellIdKeyInValue);
	    						if(ProductLineCommon.isNotNull(cellKeyToProcess)){
	    							String cellId = (String)tempMap.get(cellIdSelect);
	    							processCellIdrowMap(context,KeyInValueMap,cellId,cellKeyToProcess, true,false,exportToExcel);
	    						}

	    					}
	    				}
	    				if(KeyInValueMap.size()>0 && (subFeatureCount==0)){
	    					createFinalMapList(KeyInValueMap, returmrowList,exportToExcel);
	    					KeyInValueMap.clear();
	    				}
	    			}

	    			/**
	    			 * For each Option or Feature that can be a CELL_VALUE need to be put against correct ROW_ID.
	    			 * If current ROW_ID is not correct Parent for a feature or option, Final Map list is traversed and
	    			 * it is updated with  "ActualRowId".
	    			 * */

	    			String currentRowId = ((Map)returmrowList.get((returmrowList.size()-1))).get(UITableGrid.KEY_ROW_ID).toString();
	    			String fromId = (String)tempMap.get(SELECT_FROM_ID);
	    			if(tempMap.get(DomainConstants.SELECT_ID) != null){
	    				if(currentRowId.equals(fromId)){
	    					if(tempMap.get(cellIdSelect) !=null){
	    						putCellValues (context,tempMap, cellIdvaluesMap,exportToExcel,mapConfigSelectionStatus);
	    					}
	    					aOptionCount++;
	    				}else {
	    					tempMap.put("ActualRowId", fromId);
	    					createFinalMapList(tempMap, returmrowList,exportToExcel);
	    				}
	    			}

	    			/**
	    			 * For each ROW_ID feature, once its all chinldren are marked as CELL_VALUE, for UI, Key In values need
	    			 * to be added after option values. Below code checks whether all options are traversed and appends Key In value
	    			 * after Option values
	    			 * Since we are using tabular HTML view all cells , HTML tags are added in CELL_VALUE itself
	    			 * */
	    			if (childOptionList!=null && (aOptionCount ==childOptionList.size())){
	    				if(cellIdvaluesMap.size()>0 || KeyInValueMap.size() > 0){
	    					Iterator iter = KeyInValueMap.keySet().iterator();
	    					while(iter.hasNext()){
	    						String key = (String)iter.next();
	    						if (cellIdvaluesMap.get(key)!=null){
	    							String options = (String)cellIdvaluesMap.get(key);
	    							String keyInValue = (String)KeyInValueMap.get(key);
	    							String valToAppned = "";
	    							if(exportToExcel){
	    								options = options.concat("\n").concat(keyInValue);
	    							}else{
	    						    //this is the case when we have CF under CF, and it has Key-in and multiselect
	    							valToAppned = keyInValue.substring(keyInValue.indexOf("<table width=\"100%\" >")+21, keyInValue.length());
	    							valToAppned =  valToAppned.replace("<tr><td width=\"100%\" >", "<tr><td width=\"100%\" >"+line);
	    							valToAppned =  valToAppned.replace("<td> "+shortHandNotationForKeyIn+"</td>", "<td> "+line+shortHandNotationForKeyIn+"</td>");
	    							options = options.replace("</table>", valToAppned);
	    							}
	    							cellIdvaluesMap.put(key, options);
	    						}else{
	    							String keyInValue = (String)KeyInValueMap.get(key);
	    							cellIdvaluesMap.put(key, keyInValue);
	    						}

	    					}
	    				}
	    				KeyInValueMap.clear();
	    				childOptionList.clear();
	    			}
	    			if(cellIdvaluesMap.size()>0 && returmrowList.size()>0){
	    				createFinalMapList(cellIdvaluesMap, returmrowList,exportToExcel);
	    			}
	    		}

	    		rowList=returmrowList;
	    	}catch (Exception e) {
	    		e.printStackTrace();
	    		throw new FrameworkException(e.getMessage());
	    	}
	    	return rowList;
	    }
	    /**
		 *  Gets rows to view Product Configuration grid and pass it on to GridComponent
		 *  to view product configuration
		 *
		 *  This method will fetch all the CONFIGURATION STRUCTURES for a context product and check if its connected to a product configuration by "Selected Options"
		 *  If connected to a product configuration, it will put the feature ID against CELL_VALUEE for a particular CELL_ID(Product Configuration ID)
		 *
		 * @param cellIdvaluesMap
		 *            This map is the feature ot option map if in case of Key In value processing or In case called for option processing
		 *            it a Map containing all the options as CELL_VALUE
		 * @param cellId
		 *            Cell Id is the Product Configuration ID which will be a columnID
 		 * @param strName
		 *            strName is Name of the feature/Option/Key In to be put in CELL_VALUE
 		 * @param keyIn
		 *            Flag used to determine if (K) need to be appended after Key In value
 		 * @param isCF
		 *           Flag used to determine if a Feature is put in CELL_VALUE
		 */
	    private void processCellIdrowMap(Context context,Map cellIdvaluesMap, String cellId, String strName, boolean keyIn, boolean isCF, boolean exportToExcel)throws FrameworkException {
	    	try {
	    		String str ="";
	    		String strFirstTag = "";
	    		String exportInKeyIn = "";
	    		String line = "<hr size=\"1\" style=\"margin-top:3px; margin-bottom: 3px;\" />";
	    		String blankSpace = "<p style=\"visibility:hidden;\">_</p>";
	    		String languageStr = context.getSession().getLanguage();
	    		String shortHandNotationForKeyIn =EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	    				"emxConfiguration.Table.SubHeader.SelectedOptionsComparison_ShortNotation",languageStr);

	    		if(keyIn){
	    			str = "<td> "+line+shortHandNotationForKeyIn+"</td>";
	    			strFirstTag = "<td> "+shortHandNotationForKeyIn+"</td>";
	    			if(exportToExcel){
	    				exportInKeyIn = shortHandNotationForKeyIn;
	    			}
	    		}else if(isCF){
	    			str = "<td> "+line+blankSpace+"</td>";

	    		}else{
	    			str = "<td> "+line+blankSpace+"</td>";
	    		}

	    		if (cellIdvaluesMap.containsKey(cellId)){
	    			String strcellval =  "";
	    			if(exportToExcel){
	    				strcellval = ((String)cellIdvaluesMap.get(cellId)).concat("\n").concat(strName).concat(exportInKeyIn);
	    			}else{
	    				strcellval = ((String)cellIdvaluesMap.get(cellId)).replace("</table>", "<tr><td style=\" vertical-align:middle;\">")+line+strName+"</td>"+str+"</tr></table>";
	    			}
	    			cellIdvaluesMap.put(cellId,strcellval);
	    		}else{
	    			if(exportToExcel){
	    				cellIdvaluesMap.put(cellId,strName+exportInKeyIn);
	    			}else{
	    				cellIdvaluesMap.put(cellId,"<table width=\"100%\" ><tr><td width=\"100%\" >"+strName+"</td>"+strFirstTag+"</tr></table>");
	    			}
	    		}
	    	} catch (Exception e) {
	    		throw new   FrameworkException(e.getMessage());
	    	}
	    }
	    /**
	     * Private method Used to arrange values to be put in CELL_VALUE
	     * */
	    private void putCellValues (Context context,Map tempMap, Map cellIdvaluesMap,boolean exportToExcel,Map mapConfigSelectionStatus) throws FrameworkException{
	    	String cellIdSelect ="tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id";
	    	
	    	try {
				if (tempMap.get(cellIdSelect) instanceof StringList){
					StringList listSelect = (StringList)tempMap.get(cellIdSelect);
					for (int j = 0;j<listSelect.size(); j++) {
						boolean isCF =  false;
						String strobject = (String) listSelect.get(j);
						if(((String)tempMap.get(ConfigurationConstants.SELECT_TYPE)).equals(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE)){
							isCF = true;
						}
						String strConfigSelectStatus = (String) mapConfigSelectionStatus.get(strobject);
						System.out.println("Actual value of Attribute Configuration Selection Status = "+strConfigSelectStatus);						
						if(CONFIGURATION_SELECTION_STATUS_CHOSEN.equalsIgnoreCase(strConfigSelectStatus)
								|| CONFIGURATION_SELECTION_STATUS_DEFAULT.equalsIgnoreCase(strConfigSelectStatus)
								|| CONFIGURATION_SELECTION_STATUS_REQUIRED.equalsIgnoreCase(strConfigSelectStatus)){
							System.out.println("Attribute Configuration Selection Status to be Processed = "+strConfigSelectStatus);								
							processCellIdrowMap(context,cellIdvaluesMap,strobject,(String)tempMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME), false,isCF,exportToExcel);
						}
					}
				}else{
					boolean isCF =  false;
					String cellId = (String)tempMap.get(cellIdSelect);
					if(((String)tempMap.get(ConfigurationConstants.SELECT_TYPE)).equals(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE)){
						isCF = true;
					}
					String strConfigSelectStatus = (String) mapConfigSelectionStatus.get(cellId);
					System.out.println("Actual value of Attribute Configuration Selection Status = "+strConfigSelectStatus);					
					if(CONFIGURATION_SELECTION_STATUS_CHOSEN.equalsIgnoreCase(strConfigSelectStatus)
							|| CONFIGURATION_SELECTION_STATUS_DEFAULT.equalsIgnoreCase(strConfigSelectStatus)
							|| CONFIGURATION_SELECTION_STATUS_REQUIRED.equalsIgnoreCase(strConfigSelectStatus)){
						System.out.println("Attribute Configuration Selection Status to be Processed = "+strConfigSelectStatus);							
						processCellIdrowMap(context,cellIdvaluesMap,cellId,(String)tempMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME), false,isCF,exportToExcel);
					}
				}
			} catch (FrameworkException e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
	    }

	    /**
	     * Private method used to create Final Maplist
	     * It will check if a Map contains a key "ActualRowID" , it will map the cell value to correct ROW_ID
	     * Else feature/option cell value is mapped against last updated ROW_ID */
	    private void createFinalMapList(Map cellIdvaluesMap, MapList returmrowList, boolean exportToExcel)
	    {
	    	String cellIdSelect ="tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id";
	    	String str ="";
	    	String strFirstTag = "";
	    	String line = "<hr size=\"1\"/>";
	    	String blankSpace = "<p style=\"visibility:hidden;\">_</p>";

	    	if (ProductLineCommon.isNotNull((String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_TYPE))){
	    		if(((String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_TYPE)).equals(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE))
	    		{
	    			str = "<td> "+line+blankSpace+"</td>";
	    			strFirstTag = "";
	    		}else if(((String)cellIdvaluesMap.get((ConfigurationConstants.SELECT_TYPE))).equals(ConfigurationConstants.TYPE_CONFIGURATION_OPTION)){
	    			str = "<td> "+line+blankSpace+"</td>";
	    		}
	    	}
	    	if(cellIdvaluesMap.containsKey("ActualRowId")){
	    		for(int rCount = 0; rCount < returmrowList.size(); rCount++)
	    		{
	    			Map internalMap = (Map)returmrowList.get(rCount);
	    			if(internalMap.get(UITableGrid.KEY_ROW_ID)!=null)
	    			{
	    				if (internalMap.get(UITableGrid.KEY_ROW_ID).toString().equals(cellIdvaluesMap.get("ActualRowId").toString()))
	    				{
	    					List listKeyCellId = new StringList();
	    					List listKeyCellValue = new StringList();
	    					if (internalMap.get(UITableGrid.KEY_CELL_ID)!=null && internalMap.get(UITableGrid.KEY_CELL_VALUE)!=null)
	    					{
	    						listKeyCellId = (List)internalMap.get(UITableGrid.KEY_CELL_ID);
	    						listKeyCellValue = (List)internalMap.get(UITableGrid.KEY_CELL_VALUE);
	    						if(cellIdvaluesMap.get(cellIdSelect) instanceof StringList)
	    						{
	    							StringList tempList = (StringList)cellIdvaluesMap.get(cellIdSelect);
	    							for(int tempCount=0; tempCount<tempList.size();tempCount++)
	    							{
	    								boolean found = false;
	    								for (int j = 0;j<listKeyCellId.size(); j++){

	    									if(listKeyCellId.get(j).equals((String)tempList.get(tempCount))){
	    										String existingFeatOptvalue = (String)listKeyCellValue.get(j);
	    										String valToAppned = (String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
	    										if(exportToExcel){
	    											existingFeatOptvalue = existingFeatOptvalue.concat("\n").concat(valToAppned);
	    										}else {
	    										existingFeatOptvalue = existingFeatOptvalue.replace("</table>", "<tr><td>")+line+valToAppned+"</td>"+str+"</tr></table>";
	    										}
	    										listKeyCellValue.set(j,existingFeatOptvalue);
	    										found = true;
	    									}
	    								}
	    								if(!found){
	    									listKeyCellId.add((String)tempList.get(tempCount));
	    									String tempStr = "";
	    									if (exportToExcel){
	    										tempStr = (String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
	    									}else{
	    									tempStr = "<table width=\"100%\" ><tr><td width=\"100%\" >"+(String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME)+"</td>"+strFirstTag+"</tr></table>";
	    									}
	    									listKeyCellValue.add(tempStr);
	    								}
	    							}
	    						}else if(listKeyCellId.contains(cellIdvaluesMap.get(cellIdSelect)))
	    						{
	    							for (int j = 0;j<listKeyCellId.size(); j++){
	    								if(listKeyCellId.get(j).equals((String)cellIdvaluesMap.get(cellIdSelect))){
	    									String existingFeatOptvalue = (String)listKeyCellValue.get(j);
	    									String valToAppned = (String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
	    									if(exportToExcel){
    											existingFeatOptvalue = existingFeatOptvalue.concat("\n").concat(valToAppned);
    										}else {
	    									existingFeatOptvalue = existingFeatOptvalue.replace("</table>", "<tr><td>")+line+valToAppned+"</td>"+str+"</tr></table>";
    										}
	    									listKeyCellValue.add(j,existingFeatOptvalue);
	    								}
	    							}
	    						}else {
	    							listKeyCellId.add((String)cellIdvaluesMap.get(cellIdSelect));
	    							String tempStr = "<table width=\"100%\" ><tr><td width=\"100%\" >"+(String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME)+"</td>"+strFirstTag+"</tr></table>";
	    							listKeyCellValue.add(tempStr);
	    						}
	    					}else{
	    						StringList tempList = new StringList();
	    						if(cellIdvaluesMap.get(cellIdSelect) != null){
	    							if(cellIdvaluesMap.get(cellIdSelect) instanceof String)
	    							{
	    								tempList.add(cellIdvaluesMap.get(cellIdSelect));
	    							}else{
	    								tempList =(StringList) cellIdvaluesMap.get(cellIdSelect);
	    							}
	    						}
	    						for(int c = 0 ; c < tempList.size(); c++){
	    							String tempStr = "";
	    							listKeyCellId.add(tempList.get(c));
	    							if(exportToExcel){
	    								tempStr = (String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
									}else {
										tempStr = "<table width=\"100%\" ><tr><td width=\"100%\" >"+(String)cellIdvaluesMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME)+"</td>"+strFirstTag+"</tr></table>";
									}
	    							listKeyCellValue.add(tempStr);
	    						}
	    					}
	    					internalMap.put(UITableGrid.KEY_CELL_ID,listKeyCellId);//cell id and column id should match
	    					internalMap.put(UITableGrid.KEY_CELL_VALUE,listKeyCellValue);
	    				}
	    			}
	    		}
	    	}else{
	    		Map updateMap =(Map)returmrowList.get((returmrowList.size()-1));
	    		updateMap.put(UITableGrid.KEY_CELL_ID,new ArrayList(cellIdvaluesMap.keySet()));//cell id and column id should match
	    		updateMap.put(UITableGrid.KEY_CELL_VALUE,new ArrayList(cellIdvaluesMap.values()));
	    	}
	    }

	    private boolean isListOfNull(Object c){
	    	StringList tempList = new StringList();
	    	boolean nullList = false;
	    	if (c!=null){
	    		if(c instanceof String){
	    			tempList.add(c);
	    		}else{
	    			tempList = (StringList) c;
	    		}
	    	}

	    	for (int count = 0; count < tempList.size(); count++){
	    		if(ProductLineCommon.isNotNull((String)tempList.get(count))){
	    			nullList = true;
	    			break;
	    		}
	    	}
	    	return nullList;
	    }

	    /**
	     * This method gives the navigation structure of PC with Selected Options and Precise BOM relationships when clicked on PC hyperlink
	     * @param context
	     * @param args
	     * @return
	     * @throws Exception
	     * @since R214
	     */
	    public static MapList getStructureList(Context context, String[] args)
        throws Exception{

	    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    	HashMap paramMap   = (HashMap)programMap.get("paramMap");
	    	String objectId    = (String)paramMap.get("objectId");
	    	MapList productconfigurationStructList = new MapList();

	    	DomainObject prodconfObj = DomainObject.newInstance(context, objectId);
	    	String objectType        = prodconfObj.getInfo(context, DomainConstants.SELECT_TYPE);

	    	// IR-167036V6R2013x (oeo)
	    	// Note for Product Configurations, selected Options and Precise bom should NOT appear.

	    	if (objectType == null || !mxType.isOfParentType(context,objectType,ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)) {
	    		productconfigurationStructList = (MapList) emxPLCCommon_mxJPO.getStructureListForType(context, args);
	    	}

	    	return productconfigurationStructList;
	    }

	    /**
	     * This method is to used to show the effectivity status for the Configuration features in the selected options view
	     *
	     * @param context
	     * @param args
	     * @return
	     * @throws Exception
	     */

	    public Vector showEffectivtyStatus(Context context, String []args) throws Exception{

	        Vector vecEffectivityStatus = new Vector();
	        String strIneffective =EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		              "emxConfiguration.ConfigurationFeature.UsageEffectivity.Ineffective",context.getSession().getLanguage());
	        HashMap programMap = (HashMap)JPO.unpackArgs(args);
	        MapList lstObjectIdsList = (MapList)programMap.get("objectList");
	        
	        StringBuffer sbOutput = new StringBuffer();
	        if(lstObjectIdsList.size() > 0)
	        {
	        	for(Iterator itrObject = lstObjectIdsList.iterator();itrObject.hasNext();)
	        	{
	        		Map featureMap = (HashMap)itrObject.next();
	        		String strValid = (String)featureMap.get(EffectivityFramework.STR_EFFECTIVITY_VALID);
	        		sbOutput.delete(0,sbOutput.length());
	        		if((ConfigurationConstants.RANGE_VALUE_FALSE).equalsIgnoreCase(strValid)){
	        			sbOutput.append("<center><img src=\"../common/images/iconStatusRed.gif").append("\"");
	        			sbOutput.append(" title=\"" + XSSUtil.encodeForHTMLAttribute(context,strIneffective) + "\" ");
	        			sbOutput.append("/></center>");
	        		}
	        		vecEffectivityStatus.addElement(sbOutput.toString());
	        	}
	        }

	         return vecEffectivityStatus ;
	    }
	      /**
	       * This method is includeOIDprogram for dbchooser scenario in PC
	       * @param context
	       * @param args
	       * @return
	       * @throws Exception
	       */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getDBChooserFeatureOptions(Context context, String[] args)
			throws Exception {
		StringList includeOIDList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String contextObjectId = (String) programMap.get("parentId");
		String strStartEffectivity = (String) programMap.get("startEffectivity");
		String MPRIds = (String)programMap.get("MPRIds");
		StringList _slMPRFilteredOptions = new StringList();
		String strEffectivityExpression = "";
		if(!UIUtil.isNullOrEmpty(contextObjectId))
		{
			StringBuffer sbEffExpr = new StringBuffer();
			EffectivityFramework EFF = new EffectivityFramework();
			Map effectivitySettings = EFF.getEffectivityTypeSettings(context);
			String dateKeyword = ConfigurationConstants.EMPTY_STRING;
			if (effectivitySettings != null
					&& effectivitySettings.containsKey("Date")) {
				Map dateMap = (HashMap) effectivitySettings.get("Date");
				if (dateMap != null) {
					dateKeyword = (String) dateMap.get("keyword");
				}
			}
			if (!UIUtil.isNullOrEmpty(strStartEffectivity)) {
				sbEffExpr.append(EffectivityFramework.KEYWORD_PREFIX);
				sbEffExpr.append(dateKeyword);
				sbEffExpr.append(EffectivityFramework.KEYWORD_OPEN_BRACKET);
				sbEffExpr.append(strStartEffectivity);
				sbEffExpr.append(EffectivityFramework.KEYWORD_CLOSE_BRACKET);
				Map binaryMap = EFF.getFilterCompiledBinary(context,
						sbEffExpr.toString());
				strEffectivityExpression = (String) binaryMap
						.get(EFF.COMPILED_BINARY_EXPR);
			}


			StringList slObjSelects = new StringList();
			slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
			StringList slRelSelects = new StringList();
			slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
			DomainObject domContextBus = DomainObject.newInstance(context,contextObjectId);

			MapList mpIncludeOIDList = domContextBus.getRelatedObjects(context,ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES,
					ConfigurationConstants.TYPE_CONFIGURATION_FEATURES,
					slObjSelects, slRelSelects,false, true, (short)0,
					ConfigurationConstants.EMPTY_STRING, ConfigurationConstants.EMPTY_STRING,(short)0, DomainObject.CHECK_HIDDEN,
					DomainObject.PREVENT_DUPLICATES,(short) DomainObject.PAGE_SIZE, null, null,
	                null, DomainObject.EMPTY_STRING, strEffectivityExpression, (short) 1);

	      	for(int i=0;mpIncludeOIDList!=null && i<mpIncludeOIDList.size();i++ )
	      	{
	      		Map mp = (Map)mpIncludeOIDList.get(i);
	      		String strId = (String)mp.get(SELECT_ID);
	      		includeOIDList.addElement(strId);
	      	}
		}
		if(UIUtil.isNotNullAndNotEmpty(MPRIds)){

			String [] jpoArgs = new String[1];
			Map mpProgramArguments = new HashMap();
			mpProgramArguments.put("MPRIds",MPRIds);
			mpProgramArguments.put("_effectiveIds", includeOIDList);
			jpoArgs = (String[])JPO.packArgs(mpProgramArguments);
			_slMPRFilteredOptions = (StringList)(JPO.invoke(context, "emxProductConfiguration", null, "getMPRIncludeOIDs",
					jpoArgs, StringList.class));

			if(_slMPRFilteredOptions.size() < 1)
			{
			includeOIDList.removeAllElements();
			includeOIDList.addAll(_slMPRFilteredOptions);
			}
		}
		if(_slMPRFilteredOptions.size() > 0)
		{
			StringList removeList =  new StringList();
			for(Object objOption:_slMPRFilteredOptions)
			{
				String tempID = (String)objOption;
				if(includeOIDList.size()>0 && !includeOIDList.contains(tempID))
				{
					removeList.addElement(tempID);
				}
			}
			includeOIDList.removeAllElements();
			_slMPRFilteredOptions.removeAll(removeList);
			includeOIDList.addAll(_slMPRFilteredOptions);
		}//END:if(_slMPRFilteredOp...

		return includeOIDList;
	   }
	/**
	 * Acces function to hidden the command PBOM
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean showLinkForPBOM(Context context, String args[])
			throws Exception {
		// Getting programMap from the args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		boolean bDisplay = false;

		StringList selectStmts = new StringList(3);
		selectStmts.addElement(DomainConstants.SELECT_CURRENT);
		selectStmts.addElement("from["
				+ ConfigurationConstants.RELATIONSHIP_PRECISE_BOM + "]");
		selectStmts.addElement("attribute[" + ProductLineConstants.ATTRIBUTE_VALIDATION_STATUS + "]");
		DomainObject productConfigObj = new DomainObject(objectId);
		// Getting the current state
		Map productConfigInfomap = productConfigObj.getInfo(context,
				selectStmts);
		String curState = (String) productConfigInfomap
				.get(DomainConstants.SELECT_CURRENT);
		String sPBOMExists = (String) productConfigInfomap.get("from["
				+ ConfigurationConstants.RELATIONSHIP_PRECISE_BOM + "]");
		String sValidationStatus=(String)productConfigInfomap.get("attribute[" + ProductLineConstants.ATTRIBUTE_VALIDATION_STATUS + "]");
		if (curState
				.equalsIgnoreCase(ConfigurationConstants.STATE_PRODUCT_CONFIGURATION_ACTIVE)
				&& ConfigurationConstants.RANGE_VALUE_FALSE
						.equalsIgnoreCase(sPBOMExists) && !sValidationStatus.equals("Not Validated")) {

			bDisplay = true;

		}
		return bDisplay;
	}
/**
 * Method to hide the preview BOM command while context is Leaf Level
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
	public boolean showLinkForPreviewBOM(Context context, String args[])
			throws Exception {
		// Getting programMap from the args
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		boolean bDisplay = true;
		StringList selectStmts = new StringList(2);
		selectStmts.addElement("attribute[" + ConfigurationConstants.ATTRIBUTE_COMPLETENESS_STATUS + "]");
		DomainObject productConfigObj = new DomainObject(objectId);
		// Getting the current state
		Map productConfigInfomap = productConfigObj.getInfo(context,
				selectStmts);
		String completenessStatus = (String) productConfigInfomap.get("attribute[" + ConfigurationConstants.ATTRIBUTE_COMPLETENESS_STATUS + "]");
	    
		if(!"Complete".equals(completenessStatus))
				bDisplay = false;
		
		return bDisplay;
	}
/**
 * Get the list of non validate Design Rules
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getNonValidateRuleConnected(Context context, String[] args)
			throws Exception {
		MapList mapLogicalStructure = new MapList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		StringList stconflictRules = (StringList) programMap
				.get("conflictRules");
		for (int i = 0; i < stconflictRules.size(); i++) {
			Map ruleMap = new HashMap();
			ruleMap.put("id", stconflictRules.get(i));
			mapLogicalStructure.add(ruleMap);
		}
		return mapLogicalStructure;
	}
/**
 * Method to get the error msg for rule
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
	public List getErrorMessage(Context context, String[] args)
			throws Exception {
		List errorList = new StringList();
		MapList mapRuleList = new MapList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map ruleIdsList = (Map) programMap.get("paramList");
		StringList stconflictRules = (StringList) ruleIdsList
				.get("conflictRules");
		mapRuleList = ConfigurableRulesUtil.getRequiredMapDesignRuleTable(
				context, stconflictRules);
		for (int i = 0; i < mapRuleList.size(); i++) {
			Map tempMAp = (Map) mapRuleList.get(i);
			String errorMsg = (String) tempMAp.get(ConfigurationConstants.SELECT_ATTRIBUTE_ERROR_MESSAGE);
			errorList.add(errorMsg);
		}
		return errorList;
	}

	public static List getRuleCondition(Context context, String[] args)
			throws FrameworkException {
		MapList mapRuleList = new MapList();
		List operatorList = new StringList();
		List leftExpList = new StringList();
		List rightExpList = new StringList();
		List finalExpList = new StringList();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Map ruleIdsList = (Map) programMap.get("paramList");
			StringList stconflictRules = (StringList) ruleIdsList
					.get("conflictRules");
			mapRuleList = ConfigurableRulesUtil.getRequiredMapDesignRuleTable(
					context, stconflictRules);
			// fetching the operator list
			for (int i = 0; i < mapRuleList.size(); i++) {
				Map tempMAp = (Map) mapRuleList.get(i);
				String operator = (String) tempMAp
						.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPARISION_OPERATOR);
				operatorList.add(operator);
			}

			Map newProgramMap = new HashMap();
			newProgramMap.put("objectList", mapRuleList);

			RuleProcess ruleProcess = new RuleProcess();
			// getting the Left expression
			leftExpList = ruleProcess.getExpressionForRuleDisplay(context, JPO
					.packArgs(newProgramMap),
					ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION);
			// getting the Right expression
			rightExpList = ruleProcess.getExpressionForRuleDisplay(context, JPO
					.packArgs(newProgramMap),
					ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);
			for (int k = 0; k < leftExpList.size(); k++) {
				StringBuffer sb = new StringBuffer();
				sb.append(leftExpList.get(k));
				sb.append(" is " + operatorList.get(k) + " with ");
				sb.append(rightExpList.get(k));
				finalExpList.add(sb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalExpList;
	}

	public List getContext(Context context, String[] args)
			throws Exception {
		List contexList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map ruleIdsList = (Map) programMap.get("paramList");
		String strReportFormat = (String) ruleIdsList.get("reportFormat");
		StringList stconflictRules = (StringList) ruleIdsList
				.get("conflictRules");
		String contextName=(String)ruleIdsList.get("contextObjname");
		String contextId = (String)ruleIdsList.get("contextId");
		DomainObject strObj = new DomainObject(contextId);
		String strType = (String) strObj.getInfo(context, ConfigurationConstants.SELECT_TYPE);
		String strTypeIcon = "";
		if(strType!=null && !strType.equalsIgnoreCase("")){
				if(( mxType
							.isOfParentType(
									context,
									strType,
									ConfigurationConstants.TYPE_PRODUCTS) ||  mxType
									.isOfParentType(
											context,
											strType,
											ConfigurationConstants.TYPE_PRODUCT_VARIANT))){
			strTypeIcon = "iconSmallProduct.gif";
		}  else if(strType
				.equalsIgnoreCase(ConfigurationConstants.TYPE_SOFTWARE_FEATURE)
				|| ProductLineUtil.getChildrenTypes(context,
						ConfigurationConstants.TYPE_SOFTWARE_FEATURE)
						.contains(strType)) {
			strTypeIcon = "iconSmallSoftwareFeature.gif";
		} else {
			strTypeIcon = "iconSmallLogicalFeature.gif";
		}
		}
		StringBuffer sbBuffer = new StringBuffer();

		if(ProductLineCommon.isNotNull(strReportFormat)){
			sbBuffer.append(contextName);
		}
		else{
			 sbBuffer = sbBuffer.append("<img src=\"../common/images/")
	        .append(strTypeIcon)
	        .append("\" border=\"0\"  align=\"middle\" ")
	        .append("/><B>")
			.append(XSSUtil.encodeForXML(context,contextName))
			.append("</B>") ; 
		}

		String strName = sbBuffer.toString();
		for (int i = 0; i < stconflictRules.size(); i++) {
			contexList.add(strName);
		}
		return contexList;
	}
/**
 * Method to get edit functionality
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
	public Vector ruleEdit(Context context, String[] args) throws Exception {
		MapList mapRuleList = new MapList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map ruleIdsList = (Map) programMap.get("paramList");
		StringList stconflictRules = (StringList) ruleIdsList
				.get("conflictRules");
		String contextId = (String) ruleIdsList.get("contextId");
		MapList mapLogicalStructure = new MapList();
		short sLevel = 0;
		StringBuffer sb = new StringBuffer();
		sb.append("to[");
		sb
				.append(ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE);
		sb.append("].from.id");
		String parentId = sb.toString();
		sb.delete(0, sb.length());
		sb.append("to[");
		sb
				.append(ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE);
		sb.append("].id");
		String ruleId = sb.toString();

		StringList selectable = new StringList();
		selectable.add(DomainConstants.SELECT_ID);
		selectable.add(DomainConstants.SELECT_NAME);
		selectable.add(DomainConstants.SELECT_TYPE);
		selectable.add(DomainConstants.SELECT_REVISION);
		selectable.add(ConfigurationConstants.SELECT_PHYSICAL_ID);
		selectable.add(parentId);
		selectable.add(ruleId);
		selectable.add(ConfigurationConstants.SELECT_ATTRIBUTE_LEFT_EXPRESSION);
		selectable
				.add(ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION);

		DomainObject dom = new DomainObject(contextId);
		StringList objSelects = new StringList();
		objSelects.add(ConfigurationConstants.SELECT_TYPE);
		objSelects.add("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.id");
		Map getInfoData = dom.getInfo(context, objSelects);
		String domType = (String)getInfoData.get(ConfigurationConstants.SELECT_TYPE);
		String parentProduct = (String)getInfoData.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.id");
		
		mapLogicalStructure = dom.getRelatedObjects(context,
				ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE+","+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES,
				ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+","+ConfigurationConstants.TYPE_LOGICAL_STRUCTURES,
				selectable, DomainConstants.EMPTY_STRINGLIST, true, true,
				sLevel,DomainConstants.EMPTY_STRING,
				DomainConstants.EMPTY_STRING,0);
		
		
		if(ConfigurationConstants.TYPE_PRODUCT_VARIANT.equalsIgnoreCase(domType))
		{
			dom.setId(parentProduct);
			MapList parentProductList = dom.getRelatedObjects(context,
					ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE+","+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES,
					ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+","+ConfigurationConstants.TYPE_LOGICAL_STRUCTURES,
					selectable, DomainConstants.EMPTY_STRINGLIST, true, true,
					sLevel,DomainConstants.EMPTY_STRING,
					DomainConstants.EMPTY_STRING,0);
			mapLogicalStructure.addAll(parentProductList);
		}
		
		for (int i = 0; i < mapLogicalStructure.size(); i++) {
			Map tempMAp = (Map) mapLogicalStructure.get(i);
			String id = (String) tempMAp.get(DomainConstants.SELECT_ID);
			if (stconflictRules.contains(id)) {
				Map ruleMap = new HashMap();
				ruleMap.put("id", id);
				ruleMap.put("name", tempMAp.get("name"));
				ruleMap.put("id[parent]", tempMAp.get(parentId));
				ruleMap.put("type", tempMAp.get(DomainConstants.SELECT_TYPE));
				ruleMap.put("revision", tempMAp
						.get(DomainConstants.SELECT_REVISION));
				ruleMap.put(ConfigurationConstants.SELECT_PHYSICAL_ID, tempMAp
						.get(ConfigurationConstants.SELECT_PHYSICAL_ID));

				Object obj=tempMAp.get(parentId);
				Object ruletype=tempMAp.get(ruleId);
				StringList filter=new StringList();
				if (obj != null) {
					if (obj instanceof StringList
							&& ruletype instanceof StringList) {
						StringList ruleIds = ConfigurationUtil
								.convertObjToStringList(context, ruletype);
						filter = (StringList) obj;
						for (int k = 0; k < filter.size(); k++) {
							String rule = (String) filter.get(k);
							if (rule.equals(contextId)) {
								ruleMap.put("id[connection]", ruleIds.get(k));
							}
						}

					} else if (ruletype instanceof String) {
						ruleMap.put("id[connection]", tempMAp.get(ruleId));
						if(!((String)obj).equalsIgnoreCase(contextId))
							contextId = (String)obj;
					}
				}



				ruleMap
						.put(
								"attribute[Left Expression]",
								tempMAp
										.get(ConfigurationConstants.SELECT_ATTRIBUTE_LEFT_EXPRESSION));
				ruleMap
						.put(
								"attribute[Right Expression]",
								tempMAp
										.get(ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION));
				ruleMap.put("{name[connection]", tempMAp.get("type"));
				mapRuleList.add(ruleMap);
			}
		}
		Map newProgramMap = new HashMap();
		newProgramMap.put("objectList", mapRuleList);
		newProgramMap.put("paramList", ruleIdsList);
		newProgramMap.put("contextId", contextId);
		String[] jpoArgs = (String[]) JPO.packArgs(newProgramMap);
		newProgramMap.put(contextId, contextId);
		Vector exper = (Vector) JPO.invoke(context, "emxRule", jpoArgs,
				"getEditColumnDisplayInTable", jpoArgs, Vector.class);

		return exper;
	}

	   /**
     * This is a Transaction trigger method to Update the Filter Binary on the Product Configuration Type
      * @param context the eMatrix <code>Context</code> object
      * @param args     a String array holding the arguments from the calling method.
      *                   It contains a string representing Transaction History.
      * @return int 0 or 1
      * @throws Exception when the operation fails. s
     */
    public int updateSelectedOptionsFilter(Context context,String[] args) throws Exception
    {
        int returnFlag = 0;
        String strPCFilterBinaryUpdate=PropertyUtil.getGlobalRPEValue(context,"UpdatePCFilterBinary");
 	   	if (strPCFilterBinaryUpdate != null && !strPCFilterBinaryUpdate.equals("")){
 	       String transHistories = args[0];
 	       String strPCId = null;
 	       int idIndex = transHistories.indexOf("id=");
 	       if(idIndex != -1)
 	       {
 	           int itypeIndex = transHistories.indexOf("type=");
 	           if(itypeIndex != -1)
 	           {
 	        	   strPCId  = transHistories.substring(idIndex+3,itypeIndex).trim();
 	               transHistories = transHistories.substring(itypeIndex+5);
 	           }
 	       }
 	       try
 	       {
 	           ProductConfiguration pcInstance = new ProductConfiguration();
 	           pcInstance.constructPCBinary(context,strPCId);

 	       }catch(Exception ep)
 	       {
 	    	returnFlag=1;
 	    	throw new Exception(ep.getMessage());
 	       }
 	   }
        return returnFlag;

    }
    /**
     * This is a  method to Update the Validation Status Attribute on the Product Configuration Property Page
     * when the PC  is to be Promoted to Active State
     * @param context the eMatrix <code>Context</code> object
     * @param args     a String array holding the arguments from the calling method.
     * @throws Exception when the operation fails.
     */
   public void setValidationStatusOnPromote(Context context, String[] args) throws Exception
   {
   	try
   	{
   		String strObjectId=args[0];
   		String strAttributeStatus=args[1];
   		String strAttributeStatusValue=args[2];
   		ProductConfiguration productConf = new ProductConfiguration();
   		productConf.setId(strObjectId);
   		productConf.setAttributeValue(context,strAttributeStatus,strAttributeStatusValue);
   }
		catch(Exception e) {
			throw new FrameworkException(e.getMessage());

		}

   }
   /**
    * This method is used in the Product Configuration Properties Page to show
    * 	the Milestones selected for Product Configuration.
    * @param context
    * @param args
    * @return
    */
   public String getProductConfigurationMilestones(Context context,String[] args){
	   StringBuffer sbBuffer  = new StringBuffer();
	   try{
		   Map programMap = (HashMap) JPO.unpackArgs(args);
		   Map mpRequest = (HashMap) programMap.get("requestMap");
		   String strMode = (String)mpRequest.get("mode");
		   Map paramMap= (HashMap)programMap.get("paramMap");
		   String strObjectId = paramMap.get("objectId").toString();
		   DomainObject objConfiguration = new DomainObject(strObjectId);

		   StringList strPCSElectable = new StringList();
		   strPCSElectable.addElement("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.name");
		   strPCSElectable.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");
		   strPCSElectable.addElement("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.physicalid");
		   strPCSElectable.addElement(DomainObject.SELECT_CURRENT);

		   Map pcDetails = objConfiguration.getInfo(context,strPCSElectable);

		   String strProductId= pcDetails.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id").toString();
		   String strPCCurrentState  = pcDetails.get(DomainObject.SELECT_CURRENT).toString();

		   DomainObject domProduct = new DomainObject(strProductId);
		   StringList strLstProductSelectables = new StringList();
		   strLstProductSelectables.addElement(DomainObject.SELECT_TYPE);
		   strLstProductSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");
		   strLstProductSelectables.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");

		   Map strProductDetails = domProduct.getInfo(context,strLstProductSelectables);

		   String strModelId = null;
		   if(mxType.isOfParentType(context,strProductDetails.get(DomainObject.SELECT_TYPE).toString(),ConfigurationConstants.TYPE_PRODUCTS)
				   && !mxType.isOfParentType(context,strProductDetails.get(DomainObject.SELECT_TYPE).toString(),ConfigurationConstants.TYPE_PRODUCT_VARIANT)
				   && !mxType.isOfParentType(context,strProductDetails.get(DomainObject.SELECT_TYPE).toString(),ConfigurationConstants.TYPE_LOGICAL_FEATURE)){
			   strModelId = (String)strProductDetails.get("to["+ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");
			   if(strModelId==null || strModelId.equals("")){
				   strModelId = (String)strProductDetails.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
			   }
		   }
		   String strMilestonID = null;
		   StringBuffer strBuffMilestone = new StringBuffer();
		   if (pcDetails.get("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.physicalid") !=null) {
			   strMilestonID =  pcDetails.get("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.physicalid").toString();
			   strBuffMilestone.append("[");
			   strBuffMilestone.append((String)pcDetails.get("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.name"));
			   strBuffMilestone.append("]");
		   }

		   Map fieldMap = (HashMap) programMap.get("fieldMap");
		   String strFieldName = (String)fieldMap.get("name");

		   if(strMode!=null && !strMode.equals("") &&
				   !strMode.equalsIgnoreCase("null") && strMode.equalsIgnoreCase("edit")
				   && strPCCurrentState.equals(ConfigurationConstants.STATE_PRODUCT_CONFIGURATION_PRELIMINARY)){
			   sbBuffer.append("<input type=\"text\" READONLY ");
			   sbBuffer.append("name=\"");
			   sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
			   sbBuffer.append("Display\" id=\"\" value=\"");
			   sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strBuffMilestone.toString()));
			   sbBuffer.append("\">");
			   sbBuffer.append("<input type=\"hidden\" name=\"");
			   sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
			   sbBuffer.append("OID\" value=\"");
			   sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strMilestonID));
			   sbBuffer.append("\">");
			   sbBuffer.append("<input ");
			   sbBuffer.append("type=\"button\" name=\"btnMilestone\"");
			   sbBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
			   sbBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Milestone&includeOIDprogram=emxProductConfiguration:includeMilestones&strModelId="+XSSUtil.encodeForHTMLAttribute(context, strModelId)+"&table=CFFMilestoneDefinitionTable&showInitialResults=true&selection=single&submitAction=refreshCaller&hideHeader=true&HelpMarker=emxhelpfullsearch&mode=Chooser&chooserType=Milestone&fieldNameActual="+XSSUtil.encodeForHTMLAttribute(context, strFieldName)+"OID&fieldNameDisplay="+XSSUtil.encodeForHTMLAttribute(context, strFieldName)+"Display&formName=editDataForm&frameName=formEditDisplay&suiteKey=Configuration&submitURL=../configuration/SearchUtil.jsp");			   
			   sbBuffer.append("','900','600')\">");
			   sbBuffer.append("&nbsp;&nbsp;");
			   sbBuffer.append("<a href=\"javascript:basicClear('");
			   sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
			   sbBuffer.append("')\">");
			   String strClear =
				   EnoviaResourceBundle.getProperty(context, "ProductLine",
						   "emxProduct.Button.Clear",
						   context.getSession().getLanguage());
			   sbBuffer.append(strClear);
			   sbBuffer.append("</a>");
		   }
		   else{
			   sbBuffer.append(strBuffMilestone.toString());
		   }
	   }catch(Exception e){
		   e.printStackTrace();
	   }
	   return sbBuffer.toString();
}



   /**
    * This method is used to include the appropriate Milestone Tracks from the Model being Passed.
    *
    * @param context
    * @param args
    * @return
    * @throws Exception
    */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeMilestones(Context context, String[] args) throws Exception
	{
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    String strModelId = (String)  programMap.get("strModelId");
	    StringList tempStrList = new StringList();
	    MapList mapProductObjsList = new MapList();
	    mapProductObjsList = com.matrixone.apps.productline.Model.getModelMilestoneTracks(context,
	    		strModelId, new StringList("Engineering"), null, null, false,false);
	    for(int i=0;i<mapProductObjsList.size();i++){
	        Map temp = new HashMap();
	        temp=(Map)mapProductObjsList.get(i);
	        MapList mLstEngineeringMilestone = (MapList)temp.get("Engineering");
		    for(int k=0;k<mLstEngineeringMilestone.size();k++){
		        Map mapMileStone = (Map)mLstEngineeringMilestone.get(k);
		        String objectId = (String)mapMileStone.get("id");
		        tempStrList.add(objectId);
		    }
	    }
	    return tempStrList;
	}

	/**
	 * Access function to show the Milestone Field only in Product Revision case.
	 * For PC in Logical Feature and Product Variant, the Milestone Field will not be visible.
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static boolean showMilestoneField(Context context, String[] args)
	throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		boolean showMilestoneField = false;
		String strContextOId = (String) paramMap.get("objectId");

		String strProductId=new DomainObject(strContextOId).getInfo(context,"to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id");
		if (ProductLineCommon.isNotNull(strProductId)) {
			DomainObject domProduct = new DomainObject(strProductId);
			StringList strLstProductSelectables = new StringList();
			strLstProductSelectables.addElement(DomainObject.SELECT_TYPE);

			Map strProductDetails = domProduct.getInfo(context,
					strLstProductSelectables);
			if (mxType.isOfParentType(context, strProductDetails.get(
					DomainObject.SELECT_TYPE).toString(),
					ConfigurationConstants.TYPE_PRODUCTS)
					&& !mxType.isOfParentType(context, strProductDetails.get(
							DomainObject.SELECT_TYPE).toString(),
							ConfigurationConstants.TYPE_PRODUCT_VARIANT)
					&& !mxType.isOfParentType(context, strProductDetails.get(
							DomainObject.SELECT_TYPE).toString(),
							ConfigurationConstants.TYPE_LOGICAL_FEATURE)) {
				showMilestoneField = true;
			}
		}
		return showMilestoneField;
	}

	/**
	 * This is the method to update the Milestone selected in the Edit Details page of the Product Configuration.
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int updateProductConfigurationMilestone(Context context, String[] args)
	throws Exception {
		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map paramMap = (HashMap) programMap.get("paramMap");
		String strMilestoneOID = (String) paramMap.get("New OID");
		if (strMilestoneOID == null) {
			strMilestoneOID = "";
		}
		String strObjID = (String) paramMap.get("objectId");
		ProductConfiguration pc = new ProductConfiguration();
		pc.setId(strObjID);
		DomainObject objConfiguration = new DomainObject(strObjID);
		StringList strPCSElectable = new StringList();
		strPCSElectable.addElement("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.name");
		strPCSElectable.addElement("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.physicalid");
		strPCSElectable.addElement("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].physicalid");
		Map pcDetails = objConfiguration.getInfo(context,strPCSElectable);
		if (pcDetails.get("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.physicalid")!=null
				&& !pcDetails.get("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].to.physicalid").equals("")) {
			// disconnect the old Milestones and connect the new values.
			DomainRelationship.disconnect(context,pcDetails.get("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].physicalid").toString());
			// set the new milestones.
			if(strMilestoneOID!=null && !strMilestoneOID.equals("")){
				pc.setProductConfigurationMilestones(context,strMilestoneOID);
			}
			PropertyUtil.setGlobalRPEValue(context,"UpdatePCFilterBinary", "TRUE");
			//new DomainObject(strObjID).setAttributeValue(context, ATTRIBUTE_FILTER_COMPILED_FORM, "recalculate");
		}else if(strMilestoneOID!=null && strMilestoneOID.equals("")
				&& pcDetails.get("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].physicalid")!=null){ // if Milestone is cleared.
			DomainRelationship.disconnect(context,pcDetails.get("from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FILTER_CRITERIA+"].physicalid").toString());
			PropertyUtil.setGlobalRPEValue(context,"UpdatePCFilterBinary", "TRUE");
			//new DomainObject(strObjID).setAttributeValue(context, ATTRIBUTE_FILTER_COMPILED_FORM, "recalculate");
		}else if(strMilestoneOID!=null && !strMilestoneOID.equals("")){
			pc.setProductConfigurationMilestones(context,strMilestoneOID);
			PropertyUtil.setGlobalRPEValue(context,"UpdatePCFilterBinary", "TRUE");
		}
		return 0;
	}

	// Fixing IR IR-224104V6R2014x ,getProductContextForProductConfiguration() method added
	/**
	 * This method is used to get Product Context of Product Configuration.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public StringList getProductContextForProductConfiguration(Context context,String[] args) throws Exception
	{
		StringList productcontextStrList = new StringList();
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");

		for(Iterator iterator = objectList.iterator(); iterator.hasNext();)
		{
			HashMap hashmapObjectId = (HashMap)iterator.next();
			String strType          = (String)hashmapObjectId.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.type");
			String strProductName   = (String)hashmapObjectId.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION+"].from.name");
			String strProductId     = (String)hashmapObjectId.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION+"].from.id");
			String strProductRev    = (String)hashmapObjectId.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION+"].from.revision");

			// Product Context is Populated only when Product Configuration is created in Logical Feature Context.
			// If Product Configuration is created in Product and Product Variant Context then Product Context is Populated as Blank.
			if(strType.equalsIgnoreCase(ConfigurationConstants.TYPE_LOGICAL_FEATURE) && ProductLineCommon.isNotNull(strProductName))
			{
				StringBuffer sbufferURL = new StringBuffer();
				StringBuffer sbufferHead = new StringBuffer();

				String strTypeIcon = "../common/images/iconSmallProduct.gif";

				sbufferHead.append("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert");
				sbufferHead.append("&amp;objectId="+strProductId+"'");
				sbufferHead.append(",'800','700','true','popup')\">");

				sbufferURL.append(sbufferHead);
				sbufferURL.append("<img src=\"");
				sbufferURL.append(strTypeIcon);
				sbufferURL.append("\" border=\"0\" /></a> ");

				sbufferURL.append(sbufferHead);
				sbufferURL.append(XSSUtil.encodeForXML(context, strProductName));
				sbufferURL.append(" ");
				sbufferURL.append(XSSUtil.encodeForXML(context, strProductRev));
				sbufferURL.append("</a>");

				productcontextStrList.add(sbufferURL.toString());
			}
			else
			{
				productcontextStrList.add("");
			}
		}
		return productcontextStrList;

	}
}
