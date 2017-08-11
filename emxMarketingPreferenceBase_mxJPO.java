/*
** emxMarketingPreferenceBase
Copyright (c) 1993-2016 Dassault Systemes.
All Rights Reserved.
This program contains proprietary and trade secret information of
Dassault Systemes.
Copyright notice is precautionary only and does not evidence any actual
or intended publication of such program
*/

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.MarketingPreference;
import com.matrixone.apps.configuration.RuleProcess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
public class emxMarketingPreferenceBase_mxJPO extends emxDomainObject_mxJPO
{

/** A string constant with the value objectId. */
    public static final String OBJECT_ID = "objectId";

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
     * Alias used for CLOSE ROUND BRACE.
     */
    protected static final String CLOSE_ROUND_BRACE = ")";
    /**
     * Alias used for OPEN ROUND BRACE.
     */
    protected static final String OPEN_ROUND_BRACE = "(";
    
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
     * Alias used for OR operator.
     */
    protected static final String OR = "OR";
    
    /**
     * Alias used for NOT operator.
     */
    protected static final String NOT = "NOT";
    
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
  * Default Constructor.
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  */
 public emxMarketingPreferenceBase_mxJPO (Context context, String[] args) throws Exception
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
	   if (!context.isConnected())
            throw new Exception("Not supported on desktop client");
        return 0;

    /*if (!context.isConnected()){
      String sContentLabel = i18nNow.getI18nString(\\\"emxProduct.Error.UnsupportedClient\\\",
        \\\"emxProductCentralStringResource\\\", context.getSession().getLanguage());
      throw  new Exception(sContentLabel);
    }
    return 0;*/
  }
 @com.matrixone.apps.framework.ui.ProgramCallable
 public MapList getMarketingPreferences(Context context, String[] args) throws Exception
{
 
        MapList relBusObjPageList = new MapList();
        // Create Select list items
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		//StringList relSelects = null;//
        StringList relSelects = new StringList();
        relSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        // Retrive the Object ID of the feature
        String strObjectId = (String) programMap.get(OBJECT_ID);
		//System.out.println("Object id is"+strObjectId);
        // Retrive the Object ID of the feature
        
        //System.out.println("Relationship is"+strRelationship);
        // set relationship pattern
          // System.out.println("Type is"+strType);
        ConfigurationUtil contextObj = new ConfigurationUtil(strObjectId);
        objectSelects.add("attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]");
        objectSelects.add("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]"); 
        //objectSelects.add(DomainConstants.SELECT_TYPE);

        relBusObjPageList = contextObj.getObjectStructure(
											context,
											ConfigurationConstants.RELATIONSHIP_MARKETING_PREFERENCE,
											ConfigurationConstants.TYPE_MARKETING_PREFERENCE,
											objectSelects, relSelects, false, true, 1, 0,
											DomainObject.EMPTY_STRING,
											DomainObject.EMPTY_STRING,
											DomainObject.FILTER_ITEM, DomainObject.EMPTY_STRING);

        return relBusObjPageList;

 }
 




/**
 * Wrapper method to display the Right Expression value in MPR List Page
 * Called from Table Column settings  
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */
public List getRightExpressionToDisplayInMarketingPreferenceListPage(Context context, String[] args) throws Exception {
    RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, args,ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);

}

/**
 * Wrapper method to display the Left Expression value in MPR List Page
 * Called from Table Column settings  
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */
public List getLeftExpressionToDisplayInMarketingPreferenceListPage(Context context, String[] args) throws Exception  {
	RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, args,ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION);
}

/**
 * Wrapper method to display the Right Expression value in MPR Properties Page
 * Called from Webform field settings 
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getRightExpressionToDisplayInMPRPropertiesPage(Context context, String[] args) throws Exception {
    
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

public String getExpressionToDisplayInMPRPropertiesPage(Context context, String[] args) throws Exception  {
	Map programMap = (HashMap) JPO.unpackArgs(args);
    Map paramMap = (HashMap) programMap.get("paramMap");
    
   String strObjectId = (String)paramMap.get("objectId"); 
   DomainObject objDomain = new DomainObject(strObjectId);
   
   StringList objectSelects = new StringList();
   objectSelects.add(DomainConstants.SELECT_ID);
   objectSelects.add(DomainConstants.SELECT_NAME);

	 
   Map attributeMap = objDomain.getInfo(context, objectSelects);
   Map mpMarketingPreferenceRule= new HashMap();
   mpMarketingPreferenceRule.put(DomainConstants.SELECT_ID, strObjectId);
   mpMarketingPreferenceRule.put(DomainConstants.SELECT_NAME, strObjectId);
   mpMarketingPreferenceRule.put(ConfigurationConstants.SELECT_ATTRIBUTE_COMPARISION_OPERATOR, attributeMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPARISION_OPERATOR));
   mpMarketingPreferenceRule.put(ConfigurationConstants.SELECT_ATTRIBUTE_ERROR_MESSAGE,  attributeMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_ERROR_MESSAGE));

   MarketingPreference mcrRule = new MarketingPreference(mpMarketingPreferenceRule);
   mcrRule.populateRuleExpressions(context);
   return mcrRule.getCompleteRuleExpression(context);
}
/**
 * Wrapper method to display the Left Expression value in MPR Properties Page  
 * Called from Webform field settings
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getLeftExpressionToDisplayInMPRPropertiesPage(Context context, String[] args) throws Exception  {
	
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
 * Exclude program for add existing MPR
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeConnectedMPR(Context context, String[] args) throws Exception
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

	ConfigurationUtil confUtil = new ConfigurationUtil(objectId);
	MapList objectList = confUtil.getObjectStructure(context,
			ConfigurationConstants.TYPE_MARKETING_PREFERENCE,
			ConfigurationConstants.RELATIONSHIP_MARKETING_PREFERENCE,
			objSelects, relSelects, false, true, (short) 1, 0, objWhere,
			relWhere, DomainObject.FILTER_ITEM, filterExpression);
	
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


}
