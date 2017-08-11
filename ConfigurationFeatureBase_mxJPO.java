/*
 ** ${CLASSNAME}
 **
 **Copyright (c) 1993-2016 Dassault Systemes.
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import com.matrixone.apps.common.EngineeringChange;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationFeature;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.InclusionRule;
import com.matrixone.apps.configuration.Model;
import com.matrixone.apps.configuration.Product;
import com.matrixone.apps.configuration.RuleProcess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.effectivity.EffectivityFramework;
import com.matrixone.apps.framework.ui.UITableCommon;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.jdom.Element;


/**
 * This JPO class has some methods pertaining to Configuration Features
 *
 * @author XOG
 * @since R210
 */
public class ConfigurationFeatureBase_mxJPO extends emxDomainObject_mxJPO {

	protected static final String SYMB_WILD = "*";
	protected static final String SYMB_NOT_EQUAL = " != ";
	protected static final String SYMB_QUOTE = "'";
	protected static final String strSymbReleaseState = "state_Release";
	 /** A string constant with the value field_display_choices. */
    protected static final String FIELD_DISPLAY_CHOICES = "field_display_choices";

    public static final String POLICY_CONFIGURATION_FEATURE                       = PropertyUtil.getSchemaProperty("policy_ConfigurationFeature");
    public static final String POLICY_CONFIGURATION_OPTION                       = PropertyUtil.getSchemaProperty("policy_ConfigurationOption");
    
    /** A string constant with the value field_choices. */
    protected static final String FIELD_CHOICES = "field_choices";
    
    private static final String SUITE_KEY = "Configuration";
	/**
	 * Default Constructor.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @return noting,constructor
	 * @throws Exception
	 *             if the operation fails
	 * @author XOG
	 * @since R210
	 */
	public ConfigurationFeatureBase_mxJPO(Context context, String[] args)
	throws Exception {
		super(context, args);
	}

	/**
	 * Main entry point.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception
	 *             if the operation fails
	 * @author XOG
	 * @since R210
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		if (!context.isConnected())
			throw new Exception("Not supported on desktop client");
		return 0;
	}

	/**
	 * This Methods is used to get the Configuration Feature Structure.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the Configuration Feature Structure Details
	 * @throws FrameworkException
	 * @author A69
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getConfigurationFeatureStructure(Context context, String[] args)	
	          throws FrameworkException {
	  	
		MapList mapConfigurationStructure = null;
		StringBuffer strObjWhere = new StringBuffer();
		
		try{			
		  	Map programMap = (Map) JPO.unpackArgs(args);
		  	String strObjectId = (String) programMap.get("objectId");
		  	// call method to get the level details
			int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
			int limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(contLimit"));
					  	
		  	String sNameFilterValue = (String) programMap.get("FTRConfigurationFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRConfigurationFeatureLimitFilterCommand");
		  	boolean isCalledFromRule=false;
			if(sNameFilterValue==null){
		  		sNameFilterValue = (String) programMap.get("FTRConfigurationFeatureNameFilterForRuleDialog");
		  		if(sNameFilterValue!=null) isCalledFromRule= true;
		  	}
            if(sLimitFilterValue==null)
            	sLimitFilterValue = (String) programMap.get("FTRConfigurationFeatureLimitFilterForRuleDialog");
			if (sLimitFilterValue != null
					&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
				if (sLimitFilterValue.length() > 0) {
					limit = (short) Integer.parseInt(sLimitFilterValue);	
					if (limit < 0) {
						limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxConfiguration.ExpandLimit"));
					}
				}
			}
			
			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {				
				strObjWhere.append("attribute[");
				strObjWhere.append(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME);
				strObjWhere.append("] ~~ '");
				strObjWhere.append(sNameFilterValue);
				strObjWhere.append("'");
			}
			
			
			//if this is called from Rule, then add object where, to prevent invalid state object being seen in Rule context Tree
			if(isCalledFromRule){
				if(!strObjWhere.toString().trim().isEmpty()){
					strObjWhere.append(" && ");
					strObjWhere.append(RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE));
				}					
				else{
					strObjWhere.append(RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE));
				}						
			}
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");
			String strTypePattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURES;
            String strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES;
            StringList slObjSelects = new StringList();
            slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
            slObjSelects.addElement(ConfigurationConstants.SELECT_TYPE);
            slObjSelects.addElement(ConfigurationConstants.SELECT_NAME);
            slObjSelects.addElement(ConfigurationConstants.SELECT_REVISION);
            slObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]");
			slObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+"]");
            slObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].from.name");
            slObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS+"].from.name");
            StringList slRelSelects = new StringList();
            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_TYPE);
            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);
            slRelSelects.addElement(SELECT_FROM_NAME);
                        
            ConfigurationFeature cfBean = new ConfigurationFeature(strObjectId);
            slRelSelects.addElement("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.id");
            slRelSelects.addElement("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.type");

            slRelSelects.addElement(DomainRelationship.SELECT_FROM_ID);
            slRelSelects.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
            slRelSelects.addElement("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
			    				+ "].from.attribute["
			    				+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY + "]");
            slRelSelects.addElement("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "]");
            slRelSelects.addElement("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]");
            slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA+ "]");
            
            mapConfigurationStructure = (MapList)cfBean.getConfigurationFeatureStructure(context,strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
					true,iLevel,limit, strObjWhere.toString(), DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,filterExpression);
            
            for (int i = 0; i < mapConfigurationStructure.size(); i++) {			
				Map tempMAp = (Map) mapConfigurationStructure.get(i);
				if(tempMAp.containsKey("expandMultiLevelsJPO")){
					mapConfigurationStructure.remove(i);
					i--;
				}
				if(tempMAp.containsKey("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
	    				+ "].from.attribute["
	    				+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "]")){
					
					tempMAp.put("attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]", tempMAp.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
		    				+ "].from.attribute["
		    				+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "]"));
				}
				
				if(tempMAp.containsKey("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
	    				+ "].from.attribute["
	    				+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]")){
					
					tempMAp.put("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]",tempMAp.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
		    				+ "].from.attribute["
		    				+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]") );
				}
			}
			
			if (mapConfigurationStructure != null) {
				HashMap hmTemp = new HashMap();
				hmTemp.put("expandMultiLevelsJPO", "true");
				mapConfigurationStructure.add(hmTemp);
			}
			
		}catch (Exception e) {
			throw new FrameworkException(e);
		}
	  	return mapConfigurationStructure;
	  }
	
	/**
	 * This Methods is used to get the  top level Configuration Features.
	 * This is used while launching the Configuration Features from My Desk
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Top Level Configuration Features
	 * @throws FrameworkException
	 *             if the operation fails
	 * @author A69
	 * @since FTR R212
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTopLevelConfigurationFeatures(Context context, String[] args)
	throws FrameworkException {
		
		MapList mapTop = null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String queryName = DomainObject.EMPTY_STRING;
		
		try {			
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String sNameFilterValue = (String) programMap.get("FTRConfigurationFeatureNameFilterCommand");
		String sLimitFilterValue = (String) programMap.get("FTRConfigurationFeatureLimitFilterCommand");
		
		// emxConfiguration.Search.QueryLimit
		if (sLimitFilterValue != null
				&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
			if (sLimitFilterValue.length() > 0) {
				limit = (short) Integer.parseInt(sLimitFilterValue);
				if (limit < 0) {
					limit = 32767;
				}
			}
		}
				
		StringBuffer strWhere = new StringBuffer();
		strWhere.append("(to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"]=='False' && to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS+"]=='False')");
		strWhere.append(" || ");
		strWhere.append("(to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].from.type.kindof["+
				ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"]!=TRUE)");

		if (sNameFilterValue != null
				&& !(sNameFilterValue.equalsIgnoreCase("*"))) {
			strWhere.insert(0, " ( ");
			strWhere.append(" ) ");
			strWhere.append(" && ");
			strWhere.append("attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+ "] ~~ '"+sNameFilterValue+ "'");
		}
					
		mapTop = DomainObject.findObjects(context,
										ConfigurationConstants.TYPE_CONFIGURATION_FEATURE,"*", "*","*","*",
										strWhere.toString(),queryName,true,ConfigurationUtil.getBasicObjectSelects(context), (short)limit);

		} catch (Exception e) {
			throw new FrameworkException(e);
		}

		return mapTop;
	
	}
	/**
	 * This Methods is used to get the  top level Configuration Features owned by user.
	 * This is used while launching the Configuration Features from My Desk
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Top Level Configuration Features
	 * @throws FrameworkException
	 *             if the operation fails
	* @since FTR R213
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTopLevelOwnedConfigurationFeatures(Context context, String[] args)
	throws FrameworkException {
		
		MapList mapTop = null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String queryName = DomainObject.EMPTY_STRING;
		
		try {			
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String sNameFilterValue = (String) programMap.get("FTRConfigurationFeatureNameFilterCommand");
		String sLimitFilterValue = (String) programMap.get("FTRConfigurationFeatureLimitFilterCommand");
		
		// emxConfiguration.Search.QueryLimit
		if (sLimitFilterValue != null
				&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
			if (sLimitFilterValue.length() > 0) {
				limit = (short) Integer.parseInt(sLimitFilterValue);
				if (limit < 0) {
					limit = 32767;
				}
			}
		}
				
		StringBuffer strWhere = new StringBuffer();
		strWhere.append("(to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"]=='False' && to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS+"]=='False')");
		strWhere.append(" || ");
		strWhere.append("(to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].from.type.kindof["+
				ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"]!=TRUE)");

		if (sNameFilterValue != null
				&& !(sNameFilterValue.equalsIgnoreCase("*"))) {
			strWhere.insert(0, " ( ");
			strWhere.append(" ) ");
			strWhere.append(" && ");
			strWhere.append("attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+ "] ~~ '"+sNameFilterValue+ "'");
		}
					
		mapTop = DomainObject.findObjects(context,
										ConfigurationConstants.TYPE_CONFIGURATION_FEATURE,"*", "*",context.getUser(),"*",
										strWhere.toString(),queryName,true,ConfigurationUtil.getBasicObjectSelects(context), (short)limit);

		} catch (Exception e) {
			throw new FrameworkException(e);
		}

		return mapTop;
	
	}
	
	/**
	 * This Method is used to get the range HREF for second ProductLine
	 * in Structure Compare
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - HashMap containing the following arguments
	 *               Object Id
	 *               Field Name
	 * @return String  - Range Href for Second Object
	 * @throws Exception if the operation fails
	 * @author KXB
	 * @since FTR R212
	 */
    public String getProductLineTwoRangeHref(Context context, String[] args) throws Exception
    {        
        String strTypes = EnoviaResourceBundle.getProperty(context,"Configuration.ConfigurationCompareTypesForObject2.type_ProductLine");
		return "TYPES="+strTypes;
    }

	
	/**
	 * This Method is used to get the range HREF for second Product
	 * in Structure Compare
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - HashMap containing the following arguments
	 *               Object Id
	 *               Field Name
	 * @return String  - Range Href for Second Object
	 * @throws Exception if the operation fails
	 * @author KXB
	 * @since FTR R212
	 */
    public String getProductTwoRangeHref(Context context, String[] args) throws Exception
    {        
        String strTypes = EnoviaResourceBundle.getProperty(context,"Configuration.ConfigurationCompareTypesForObject2.type_Products");
		return "TYPES="+strTypes;
    }

	/**
	 * This Method is used to get the range HREF for second Product Variant
	 * in Structure Compare
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the following arguments 0 - HashMap containing the following arguments
	 *               Object Id
	 *               Field Name
	 * @return String  - Range Href for Second Object
	 * @throws Exception if the operation fails
	 * @author KXB
	 * @since FTR R212
	 */
    public String getProductVariantTwoRangeHref(Context context, String[] args) throws Exception
    {        
        String strTypes = EnoviaResourceBundle.getProperty(context,"Configuration.ConfigurationCompareTypesForObject2.type_ProductVariant");
		return "TYPES="+strTypes;
    }

	/**
     * This method gives the list of match based on criteria for comparison.
     * @param context the eMatrix <code>Context</code> object.
	 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
	 *                   Feature Type
	 *                   Match Based On
     * @returns an Object containing hashmap of match based on criteria for comparison.
     * @throws Exception if the operation fails.
     * @author KXB
	 * @since FTR R212
     */
    public Object getMatchBasedOnCriteria(Context context, String[] args)throws Exception 
	{
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String strLanguage = context.getSession().getLanguage();
	    MapList columns = null;
	    Map TableMap = null;
	    Map TableSettingMap = null;
	    String strMBO = null;
	    String sLabel = null;
	    String sStrResourceValue = null;
	    StringBuffer strBuf = new StringBuffer();
	    UITableCommon uiTable = new UITableCommon();
	    String[] strvalues = null;
	    // to get all the table columns and assign it to map
	    String strFeatureType = (String) requestMap.get("featureType");	
	    String strMatchBasedOn = (String) requestMap.get("MatchBasedOn");	
	    if (!"".equals(strMatchBasedOn) && !"null".equals(strMatchBasedOn)
	            && strMatchBasedOn != null) {
	        strvalues = strMatchBasedOn.split(",");
	    }	
	    if (strFeatureType.equalsIgnoreCase("ConfigurationFeature")) {
	        columns = uiTable
	                .getColumns(
	                        context,
	                        PropertyUtil
	                                .getSchemaProperty(context,"table_FTRConfigurationFeaturesStructureCompareTable"),
	                        null);
	    } else if (strFeatureType.equalsIgnoreCase("LogicalFeature")||strFeatureType.equalsIgnoreCase("BOMCompare")
	    		|| strFeatureType.equalsIgnoreCase("PVLogicalFeature")||strFeatureType.equalsIgnoreCase("PVBOMCompare")
	    		|| strFeatureType.equalsIgnoreCase("ManufacturingFeature")) {
	        columns = uiTable
            .getColumns(
                    context,
                    PropertyUtil
                            .getSchemaProperty(context,"table_FTRLogicalFeaturesStructureCompareTable"),
                    null);
        }
	    int MapListSize = columns.size();	
	    if (MapListSize > 0) {
	        strBuf.append("<table name=\"MatchBasedOn\" border=\"0\"><tr><td>");
	        String sStrNameLabel = (EnoviaResourceBundle.getProperty(context,SUITE_KEY, "emxFramework.Basic.Name",strLanguage)).trim();
	        String sStrResourceActualValue = null;
	        int strValueIndex = 0;
	        for (int i = 0; i < MapListSize; i++) {
	            // To get the setting of table columns
	            TableMap = (Map) columns.get(i);
	            TableSettingMap = (Map) TableMap.get("settings");
	            strMBO = (String) TableSettingMap.get("MatchBasedOn");
	            sLabel = (String) TableMap.get("label");
	            // Obtaining Value from String Resource File
	            sStrResourceValue = EnoviaResourceBundle.getProperty(context,SUITE_KEY, sLabel,strLanguage);
	            sStrResourceActualValue = EnoviaResourceBundle.getProperty(context,SUITE_KEY, sLabel, "en-us");
	            String sSelected = "";
	            if (sStrResourceValue != null
	                    && sStrResourceValue.equalsIgnoreCase(sStrNameLabel) && strvalues == null) {
	                sSelected = "checked=\"true\"";
	            }	
	            if (strvalues != null) {
	                for (int j = 0; j < strvalues.length; j++) {
	                    if (strvalues[j] != null || "".equals(strvalues[j])
	                            || "null".equals(strvalues[j])) {
	                        if (strvalues[j].equals(sStrResourceActualValue)) {
	                            sSelected = "checked=\"true\"";
	                            break;
	                        }
	                    }
	                }
	            }	
	            if ("true".equalsIgnoreCase(strMBO)) {
	
	                String sVal = sStrResourceValue.replace(" ", "_");
	                sVal = sVal.replace("_+_", "_");
	                strBuf
	                        .append("<input type=\"checkbox\" "
	                                + sSelected
	                                + "  id=\"MatchBasedOn\" name=\"MatchBasedOn\" value=\""
	                                + XSSUtil.encodeForHTMLAttribute(context, sStrResourceActualValue)
	                                + "\" onclick=\"checkMatchBasedOnAndDisableCheckBoxForCompareBy("+strValueIndex+");\"  />"
	                                + XSSUtil.encodeForXML(context, sStrResourceValue));
	                strBuf
	                        .append("<input type=\"hidden\" id=\"MatchBasedOnActual\" name=\"MatchBasedOnActual\" value=\""
	                                + XSSUtil.encodeForHTMLAttribute(context, sStrResourceActualValue) + "\" />");
	                strBuf.append("</td></tr><tr><td>");
	                strValueIndex++;
	            }
	        }
	        strBuf.append("</td></tr></table>");
	    }	
	    return strBuf.toString();
	}

	/**
	 * This method gives the list of Compare By criteria for comparison.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
	 *                   Feature Type
	 *                   compare By
	 * @returns an Object containing HTML output for Compare By
	 * @throws Exception if the operation fails.
	 * @author KXB
	 * @since FTR R212
	 */	
	public Object getCompareByCriteria(Context context, String[] args)throws Exception 
	{	
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");	
	    String strLanguage = context.getSession().getLanguage();
	    MapList columns = null;
	    Map TableMap = null;
	    Map TableSettingMap = null;
	    String strComparable = "";
	    String strFieldName = "";
	    String strFieldActualName = "";
	    String sStrResourceValue = "";
	    StringBuffer strBuf = new StringBuffer();
	    UITableCommon uiTable = new UITableCommon();
	    String[] strvalues = null;
	    // to get all the table columns and assign it to map	
	    String strFeatureType = (String) requestMap.get("featureType");
	    String strCompareBy = (String) requestMap.get("compareBy");	
	    if (!"".equals(strCompareBy) && !"null".equals(strCompareBy)
	            && strCompareBy != null) {
	        strvalues = strCompareBy.split(",");
	    }	
	    //table_FTRConfigurationFeaturesStructureCompareTable,table_FTRProductLineConfigurationFeatureTable
	    if (strFeatureType.equalsIgnoreCase("ConfigurationFeature")) {
	        columns = uiTable
	                .getColumns(
	                        context,
	                        PropertyUtil
	                                .getSchemaProperty(context,"table_FTRConfigurationFeaturesStructureCompareTable"),
	                        null);
	    } else if (strFeatureType.equalsIgnoreCase("LogicalFeature")||strFeatureType.equalsIgnoreCase("BOMCompare")
	    		|| strFeatureType.equalsIgnoreCase("PVLogicalFeature")||strFeatureType.equalsIgnoreCase("PVBOMCompare")
	    		|| strFeatureType.equalsIgnoreCase("ManufacturingFeature")) {
	        columns = uiTable
	                .getColumns(
	                        context,
	                        PropertyUtil
	                                .getSchemaProperty(context,"table_FTRLogicalFeaturesStructureCompareTable"),
	                        null);
	    }
	    int MapListSize = columns.size();	
	    if (MapListSize > 0) {
	        String sStrDefaults = EnoviaResourceBundle
	                .getProperty(context,
	                        SUITE_KEY, "emxConfiguration.StructureCompare.ReportDifferencesDefault",strLanguage);
	        String sStrNameLabel = (EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxFramework.Basic.Name",strLanguage)).trim();
	        String sStrResourceActualValue = null;
	        strBuf.append("<table name=\"CompareBy\" border=\"0\"><tr><td>");
	        for (int i = 0; i < MapListSize; i++) {	
	            // To get the setting of the table columns
	            TableMap = (Map) columns.get(i);
	            TableSettingMap = (Map) TableMap.get("settings");
	            strComparable = (String) TableSettingMap.get("Comparable");
	            strFieldName = (String) TableMap.get("label");
	            strFieldActualName = (String) TableMap.get("name"); // for IR-370910-3DEXPERIENCER2016x - 'Single/ Multiple' and 'Sequence Number' should be displayed in Red color 
	            if(null != strFieldName && !"".equals(strFieldName) && !"null".equals(strFieldName)){
	            // Obtaining Value from String Resource File
	            sStrResourceValue = EnoviaResourceBundle.getProperty(context,
	                    SUITE_KEY, strFieldName,strLanguage);
	            sStrResourceActualValue = EnoviaResourceBundle.getProperty(context,
	                    SUITE_KEY, new Locale("en-us"),strFieldName);
	            String sSelected = "";
	            StringTokenizer strToken = new StringTokenizer(sStrDefaults,
	                    ",");
	            while (strToken.hasMoreTokens()) {
	                strToken.nextToken();
	                if (sStrResourceValue != null
	                        && sStrResourceValue.equals(sStrNameLabel)) {
	                    sSelected = "disabled";
	                }
	            }
	            if (strvalues != null) {
	                for (int j = 0; j < strvalues.length; j++) {
	                    if (strvalues[j] != null || "".equals(strvalues[j])
	                            || "null".equals(strvalues[j])) {
	                        if (strvalues[j].equals(strFieldActualName)) { //sStrResourceActualValue changes to strFieldActualName for IR-370903-3DEXPERIENCER2016x 
	                            sSelected = "checked=\"true\"";
	                            break;
	                        }
	                    }
	                }
	            }	
	         //changes for IR-370910-3DEXPERIENCER2016x - 'Single/ Multiple' and 'Sequence Number' should be displayed in Red color
	            if (strComparable != null
	                    && "true".equalsIgnoreCase(strComparable)) {
	
	                // Adding the HTML code with obtained values
	                strBuf
	                        .append("<input id=\"CompareBy\" type=\"checkbox\" "
	                                + sSelected
	                                + " onclick=\"disableCheckBoxForMatchBasedOn();\"  name=\"CompareBy\" value=\""
	                                + strFieldActualName//XSSOK
	                                + "\"  />"
	                                + XSSUtil.encodeForXML(context, sStrResourceValue));
	                strBuf
	                        .append("<input id=\"CompareByActual\" type=\"hidden\" name=\"CompareByActual\" value=\""
	                                + XSSUtil.encodeForHTMLAttribute(context, sStrResourceActualValue) + "\"  />");
	                strBuf.append("</td></tr><tr><td>");
	            }
	        }
	    }
	        strBuf.append("</td></tr></table>");
	        strBuf.append("<script type=\"text/javascript\">");
	        strBuf
	                .append("disableCheckBoxForMatchBasedOn();disableCheckBoxForCompareBy();");
	        strBuf.append("</script>");
	    }
	    return strBuf.toString();	
	}

	/**
	 * This method defines the Expand Levels.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
	 *                   Expand Level
	 * @return expandLevels
	 * @throws Exception if the operation fails. Since Feature Configuration X3
	 * @author KXB
	 * @since FTR R212
	 */	
	public Object getExpandLevel(Context context, String[] args)throws Exception 
	{
	    StringBuffer strBuf = new StringBuffer();
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String strExpandLevel = (String) requestMap.get("ExpandLevel");	
	    String strLanguage = context.getSession().getLanguage();
	    String sOne = EnoviaResourceBundle.getProperty(context,
                "Framework", "emxFramework.FreezePane.1",strLanguage);
        String sTwo = EnoviaResourceBundle.getProperty(context,
                "Framework", "emxFramework.FreezePane.2",strLanguage);
        String sThree = EnoviaResourceBundle.getProperty(context,
                "Framework", "emxFramework.FreezePane.3",strLanguage);
        String sFour = EnoviaResourceBundle.getProperty(context,
                "Framework", "emxFramework.FreezePane.4",strLanguage);
        String sFive = EnoviaResourceBundle.getProperty(context,
                "Framework", "emxFramework.FreezePane.5",strLanguage);
        String sAll = EnoviaResourceBundle.getProperty(context,
                "Framework", "emxFramework.FreezePane.All",strLanguage);
	    // the Expand level drop down
	    strBuf.append("<table><tr><td>");
	    strBuf.append("<SELECT name=\"ExpandLevel\">");	
	    if ("1".equals(strExpandLevel))
	        strBuf
	                .append("<OPTION value=\"1\" SELECTED>"+XSSUtil.encodeForXML(context, sOne)+"</OPTION><OPTION value=\"2\">"+XSSUtil.encodeForXML(context, sTwo)+"<OPTION value=\"3\">"+XSSUtil.encodeForXML(context, sThree)+"<OPTION value=\"4\">"+XSSUtil.encodeForXML(context, sFour)+"<OPTION value=\"5\">"+XSSUtil.encodeForXML(context, sFive)+"<OPTION value=\"All\">"+XSSUtil.encodeForXML(context, sAll)+"</SELECT>");
	    else if ("2".equals(strExpandLevel))
	        strBuf
	                .append("<OPTION value=\"1\">"+XSSUtil.encodeForXML(context, sOne)+"</OPTION><OPTION value=\"2\" SELECTED>"+XSSUtil.encodeForXML(context, sTwo)+"<OPTION value=\"3\">"+XSSUtil.encodeForXML(context, sThree)+"<OPTION value=\"4\">"+XSSUtil.encodeForXML(context, sFour)+"<OPTION value=\"5\">"+XSSUtil.encodeForXML(context, sFive)+"<OPTION value=\"All\">"+XSSUtil.encodeForXML(context, sAll)+"</SELECT>");
	    else if ("3".equals(strExpandLevel))
	        strBuf
	                .append("<OPTION value=\"1\">"+XSSUtil.encodeForXML(context, sOne)+"</OPTION><OPTION value=\"2\">"+XSSUtil.encodeForXML(context, sTwo)+"<OPTION value=\"3\" SELECTED>"+XSSUtil.encodeForXML(context, sThree)+"<OPTION value=\"4\">"+XSSUtil.encodeForXML(context, sFour)+"<OPTION value=\"5\">"+XSSUtil.encodeForXML(context, sFive)+"<OPTION value=\"All\">"+XSSUtil.encodeForXML(context, sAll)+"</SELECT>");
	    else if ("4".equals(strExpandLevel))
	        strBuf
	                .append("<OPTION value=\"1\">"+XSSUtil.encodeForXML(context, sOne)+"</OPTION><OPTION value=\"2\">"+XSSUtil.encodeForXML(context, sTwo)+"<OPTION value=\"3\">"+XSSUtil.encodeForXML(context, sThree)+"<OPTION value=\"4\" SELECTED>"+XSSUtil.encodeForXML(context, sFour)+"<OPTION value=\"5\">"+XSSUtil.encodeForXML(context, sFive)+"<OPTION value=\"All\">"+XSSUtil.encodeForXML(context, sAll)+"</SELECT>");
	    else if ("5".equals(strExpandLevel))
	        strBuf
	                .append("<OPTION value=\"1\">"+XSSUtil.encodeForXML(context, sOne)+"</OPTION><OPTION value=\"2\">"+XSSUtil.encodeForXML(context, sTwo)+"<OPTION value=\"3\">"+XSSUtil.encodeForXML(context, sThree)+"<OPTION value=\"4\">"+XSSUtil.encodeForXML(context, sFour)+"<OPTION value=\"5\" SELECTED>"+XSSUtil.encodeForXML(context, sFive)+"<OPTION value=\"All\">"+XSSUtil.encodeForXML(context, sAll)+"</SELECT>");
	    else if ("All".equals(strExpandLevel))
	        strBuf
	                .append("<OPTION value=\"1\">"+XSSUtil.encodeForXML(context, sOne)+"</OPTION><OPTION value=\"2\">"+XSSUtil.encodeForXML(context, sTwo)+"<OPTION value=\"3\">"+XSSUtil.encodeForXML(context, sThree)+"<OPTION value=\"4\">"+XSSUtil.encodeForXML(context, sFour)+"<OPTION value=\"5\">"+XSSUtil.encodeForXML(context, sFive)+"<OPTION value=\"All\" SELECTED>"+XSSUtil.encodeForXML(context, sAll)+"</SELECT>");
	    else
	        strBuf
	                .append("<OPTION value=\"1\">"+XSSUtil.encodeForXML(context, sOne)+"</OPTION><OPTION value=\"2\">"+XSSUtil.encodeForXML(context, sTwo)+"<OPTION value=\"3\">"+XSSUtil.encodeForXML(context, sThree)+"<OPTION value=\"4\">"+XSSUtil.encodeForXML(context, sFour)+"<OPTION value=\"5\">"+XSSUtil.encodeForXML(context, sFive)+"<OPTION value=\"All\">"+XSSUtil.encodeForXML(context, sAll)+"</SELECT>");

	    strBuf.append("</td></tr></table>");
	    return strBuf.toString();
	}
	
	/**
	 * Gives the list of formats for comparison.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
	 *                   Format
	 * @return an Object contanining HTML content for formats used in comparison.
	 * @throws Exception if the operation fails. Since Feature Configuration X3
	 * @author KXB
	 * @since FTR R212
	 */	
	public Object getFormats(Context context, String[] args) throws Exception 
	{
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String strFormat = (String) requestMap.get("Format");	
	    String strLanguage = context.getSession().getLanguage();
	    String strSuiteKey = (String) requestMap.get("suiteKey");
	    StringBuffer strBuf = new StringBuffer();
	    strBuf.append("<table name=\"Format\"border=\"0\"><tr><td>");	
	    if ("Difference_Only_Report".equals(strFormat)) {
	        String sCompleteSummary = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.CompleteSummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\"  name=\"Format\" value=\"Complete_Summary_Report\" />"
	                        + XSSUtil.encodeForXML(context, sCompleteSummary));
	        strBuf.append("</td></tr><tr><td>");
	
	        String sDiffOnlySummary = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.DiffOnlySummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Difference_Only_Report\" checked=\"true\" />"
	                        + XSSUtil.encodeForXML(context, sDiffOnlySummary));
	        strBuf.append("</td></tr><tr><td>");
	
	        String sObj1Unique = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.Obj1Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\" name=\"Format\" value=\"Unique_toLeft_Report\" />"
	                        + XSSUtil.encodeForXML(context, sObj1Unique));
	        strBuf.append("</td></tr><tr><td>");
	
	        String sObj2Unique = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.Obj2Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Unique_toRight_Report\" />"
	                        + XSSUtil.encodeForXML(context, sObj2Unique));
	        strBuf.append("</td></tr><tr><td>");
	
	        String sCommonComponents = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.CommonComponents",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\" name=\"Format\" value=\"Common_Report\" />"
	                        + XSSUtil.encodeForXML(context, sCommonComponents));
	    } else if ("Unique_toLeft_Report".equals(strFormat)) {
	        String sCompleteSummary = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.CompleteSummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\"  name=\"Format\" value=\"Complete_Summary_Report\" />"
	                        + XSSUtil.encodeForXML(context, sCompleteSummary));
	        strBuf.append("</td></tr><tr><td>");

	        String sDiffOnlySummary = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.DiffOnlySummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Difference_Only_Report\" />"
	                        + XSSUtil.encodeForXML(context, sDiffOnlySummary));
	        strBuf.append("</td></tr><tr><td>");

	        String sObj1Unique = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.Obj1Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\" name=\"Format\" value=\"Unique_toLeft_Report\" checked=\"true\" />"
	                        + XSSUtil.encodeForXML(context, sObj1Unique));
	        strBuf.append("</td></tr><tr><td>");

	        String sObj2Unique = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.Obj2Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Unique_toRight_Report\" />"
	                        + XSSUtil.encodeForXML(context, sObj2Unique));
	        strBuf.append("</td></tr><tr><td>");

	        String sCommonComponents = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.CommonComponents",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\" name=\"Format\" value=\"Common_Report\" />"
	                        + XSSUtil.encodeForXML(context, sCommonComponents));
	    } else if ("Unique_toRight_Report".equals(strFormat)) {
	        String sCompleteSummary = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.CompleteSummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\"  name=\"Format\" value=\"Complete_Summary_Report\" />"
	                        + XSSUtil.encodeForXML(context, sCompleteSummary));
	        strBuf.append("</td></tr><tr><td>");

	        String sDiffOnlySummary = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.DiffOnlySummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Difference_Only_Report\" />"
	                        + XSSUtil.encodeForXML(context, sDiffOnlySummary));
	        strBuf.append("</td></tr><tr><td>");

	        String sObj1Unique = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.Obj1Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\" name=\"Format\" value=\"Unique_toLeft_Report\" />"
	                        + XSSUtil.encodeForXML(context, sObj1Unique));
	        strBuf.append("</td></tr><tr><td>");

	        String sObj2Unique = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.Obj2Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Unique_toRight_Report\" checked=\"true\" />"
	                        + XSSUtil.encodeForXML(context, sObj2Unique));
	        strBuf.append("</td></tr><tr><td>");

	        String sCommonComponents = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.CommonComponents",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\" name=\"Format\" value=\"Common_Report\" />"
	                        + XSSUtil.encodeForXML(context, sCommonComponents));
	    } else if ("Common_Report".equals(strFormat)) {
	        String sCompleteSummary = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.CompleteSummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\"  name=\"Format\" value=\"Complete_Summary_Report\" />"
	                        + XSSUtil.encodeForXML(context, sCompleteSummary));
	        strBuf.append("</td></tr><tr><td>");

	        String sDiffOnlySummary = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.DiffOnlySummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Difference_Only_Report\" />"
	                        + XSSUtil.encodeForXML(context, sDiffOnlySummary));
	        strBuf.append("</td></tr><tr><td>");

	        String sObj1Unique = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.Obj1Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\" name=\"Format\" value=\"Unique_toLeft_Report\" />"
	                        + XSSUtil.encodeForXML(context, sObj1Unique));
	        strBuf.append("</td></tr><tr><td>");

	        String sObj2Unique = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.Obj2Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Unique_toRight_Report\" />"
	                        + XSSUtil.encodeForXML(context, sObj2Unique));
	        strBuf.append("</td></tr><tr><td>");

	        String sCommonComponents = EnoviaResourceBundle.getProperty(context,
	                SUITE_KEY, "emxConfiguration.StructureCompare.CommonComponents",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\" name=\"Format\" value=\"Common_Report\" checked=\"true\" />"
	                        + XSSUtil.encodeForXML(context, sCommonComponents));
	    } else {
	        String sCompleteSummary = EnoviaResourceBundle.getProperty(context,
	        		strSuiteKey,"emxConfiguration.StructureCompare.CompleteSummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\"  name=\"Format\" value=\"Complete_Summary_Report\" checked=\"true\" />"
	                        + XSSUtil.encodeForXML(context, sCompleteSummary));
	        strBuf.append("</td></tr><tr><td>");

	        String sDiffOnlySummary = EnoviaResourceBundle.getProperty(context,
	        		strSuiteKey,"emxConfiguration.StructureCompare.DiffOnlySummary",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Difference_Only_Report\" />"
	                        + XSSUtil.encodeForXML(context, sDiffOnlySummary));
	        strBuf.append("</td></tr><tr><td>");

	        String sObj1Unique = EnoviaResourceBundle.getProperty(context,
	        		strSuiteKey,"emxConfiguration.StructureCompare.Obj1Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\"  id=\"Format\" name=\"Format\" value=\"Unique_toLeft_Report\" />"
	                        + XSSUtil.encodeForXML(context, sObj1Unique));
	        strBuf.append("</td></tr><tr><td>");

	        String sObj2Unique = EnoviaResourceBundle.getProperty(context,
	        		strSuiteKey,"emxConfiguration.StructureCompare.Obj2Unique",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\"  name=\"Format\" value=\"Unique_toRight_Report\" />"
	                        + XSSUtil.encodeForXML(context, sObj2Unique));
	        strBuf.append("</td></tr><tr><td>");

	        String sCommonComponents = EnoviaResourceBundle.getProperty(context,
	        		strSuiteKey,"emxConfiguration.StructureCompare.CommonComponents",strLanguage);
	        strBuf
	                .append("<input type=\"radio\" id=\"Format\" name=\"Format\" value=\"Common_Report\" />"
	                        + XSSUtil.encodeForXML(context, sCommonComponents));
	    }
	    strBuf.append("<input type=\"hidden\" name=\"Section\" value=\"\">");
	    strBuf.append("</td></tr></table>");
	    strBuf.append("</td></tr>");
	    return strBuf.toString();
	}

	/**
	 * This Method is used to include only context Model's Products in the search Results
	 * @param context- the eMatrix <code>Context</code> object
	 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
	 *                   Object ID
	 * @return StringList- consisting of the context object ids to be included
	 * @throws Exception if the operation fails
	 * @author KXB
	 * @since FTR R212
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeContextProducts(Context context, String[] args) throws Exception
	{
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    String strObjectId = (String)  programMap.get("objectId");
	    StringList tempStrList = new StringList();
	    MapList mapProductObjsList = null;
	    DomainObject prodObj = new DomainObject(strObjectId);
	    
	    StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        
        short sLevel = 1;
        
        String strRelName = ConfigurationConstants.RELATIONSHIP_PRODUCTS;
        String strTypeName = ConfigurationConstants.TYPE_MODEL;
        MapList relBusObjPageList = prodObj.getRelatedObjects(context, strRelName, strTypeName,
                objectSelects, relSelects, true, false, sLevel, "", "", 0);
        
        String strModelId = (String)((Map)relBusObjPageList.get(0)).get(DomainConstants.SELECT_ID);
	    //String strModelId = prodObj.getInfo(context, "to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
	    int limit = 0;
	    DomainObject modelObj = new DomainObject(strModelId);
		StringBuffer stbRelPattern = new StringBuffer(50);
		stbRelPattern = stbRelPattern.append(ConfigurationConstants.RELATIONSHIP_PRODUCTS);
	    StringList selectStmts = new StringList(15);
	    selectStmts.add(DomainObject.SELECT_ID);
	    selectStmts.add(DomainObject.SELECT_NAME);
	    selectStmts.add(DomainObject.SELECT_REVISION);
	    
	    StringBuffer stbTypePattern = new StringBuffer(50);
	    stbTypePattern = stbTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
		StringBuffer stbObjWhere = new StringBuffer(50);
	    mapProductObjsList = modelObj.getRelatedObjects(context,
	    		stbRelPattern.toString(), stbTypePattern.toString(),
	           selectStmts, relSelects, false, true, (short)1,
	           stbObjWhere.toString(), null, (short)limit);
	    for(int i=0;i<mapProductObjsList.size();i++){
	        Map temp = new HashMap();
	        temp=(Map)mapProductObjsList.get(i);
	        String objectId = (String)temp.get("id");
	        if(!objectId.equalsIgnoreCase(strObjectId))
	        tempStrList.add(objectId);
	    }
	    return tempStrList;
	}

	/**
	 * This Method is used to include only context Product Variants in the search Results
	 * @param context- the eMatrix <code>Context</code> object
	 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
	 *                   Object ID
	 * @return StringList- consisting of the context object ids to be included
	 * @throws Exception if the operation fails
	 * @author KXB
	 * @since FTR R212
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeContextProductVariants(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    String strObjectId = (String)  programMap.get("objectId");
	    StringList tempStrList = new StringList();
	    MapList mapProductVariantsObjsList = null;
	    DomainObject prodVarObj = new DomainObject(strObjectId);
	    String strProdId = prodVarObj.getInfo(context, "to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.id");
	    tempStrList.add(strProdId);
	    int limit = 0;
	    DomainObject prodObj = new DomainObject(strProdId);
		StringBuffer stbRelPattern = new StringBuffer(50);
		stbRelPattern = stbRelPattern.append(ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION);
	    StringList selectStmts = new StringList(15);
	    selectStmts.add(DomainObject.SELECT_ID);
	    selectStmts.add(DomainObject.SELECT_NAME);
	    selectStmts.add(DomainObject.SELECT_REVISION);
	    StringList relSelects = new StringList(1);
	    relSelects.add(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
	    StringBuffer stbTypePattern = new StringBuffer(50);
	    stbTypePattern = stbTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
		StringBuffer stbObjWhere = new StringBuffer(50);
		mapProductVariantsObjsList = prodObj.getRelatedObjects(context,
	    		stbRelPattern.toString(), stbTypePattern.toString(),
	           selectStmts, relSelects, false, true, (short)1,
	           stbObjWhere.toString(), null, (short)limit);
	    for(int i=0;i<mapProductVariantsObjsList.size();i++){
	        Map temp = new HashMap();
	        temp=(Map)mapProductVariantsObjsList.get(i);
	        String objectId = (String)temp.get("id");
	        if(!objectId.equalsIgnoreCase(strObjectId))
	        tempStrList.add(objectId);
	    }

		// get all the revisions of the product
		DomainObject domObj = newInstance(context, strProdId);
		StringList singleValueSelects = new StringList(DomainObject.SELECT_ID);
		singleValueSelects.add(ConfigurationConstants.SELECT_PHYSICAL_ID);
		StringList multiValueSelects = new StringList();
		List lstTemp = domObj.getRevisionsInfo(context, singleValueSelects,multiValueSelects);

	    for(int i=0;i<lstTemp.size();i++){
	    	  Map temp = new HashMap();
		        temp=(Map)lstTemp.get(i);
		        String objectId = (String)temp.get("id");
		        if(!objectId.equalsIgnoreCase(strObjectId))
		        tempStrList.add(objectId);
	    }
	    return tempStrList;
	}

	/**
	 * This method is used to get the Owning Parent of Feature while doing Structure Compare.
	 * @param context- the eMatrix <code>Context</code> object
	 * @param args 	   - Holds the following arguments 0 - HashMap containing the following arguments
	 *                   Object List
	 * @return List Value containing Owning Parent of Feature.
	 * @author KXB
	 * @since FTR R212
	 */
	public StringList getOwningParent(Context context, String[] args)throws Exception
	{
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    MapList lstObjectIdsList = (MapList) programMap.get("objectList");
	    Map tempMap;
	    StringList lstPar = new StringList();
	    StringList lstOwningPar = new StringList();
	    for (int j = 0; j < lstObjectIdsList.size(); j++) {
	        tempMap = (Map)lstObjectIdsList.get(j);
	        String strTypeConnection = (String)tempMap.get("relationship");
	        if(tempMap.get("Root Node")!=null && !tempMap.get("Root Node").toString().equals("") 
	        		&& tempMap.get("Root Node").toString().equalsIgnoreCase("true")){
	        	lstOwningPar.addElement("");
	        	return lstOwningPar;
	        }
	        if((strTypeConnection!=null && !strTypeConnection.equals("")) && 
	        		(strTypeConnection.equals(ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES) || strTypeConnection.equals(ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES))
	        		|| strTypeConnection.equals(ConfigurationConstants.RELATIONSHIP_EBOM)|| 
	        		ProductLineCommon.isKindOfRel(context, strTypeConnection, ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES)
	        		|| strTypeConnection.equals(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS))
	        {
	        	String owningParent = (String)tempMap.get("id[parent]");
	        	if (owningParent != null) {
	        		lstPar.addElement(owningParent);
	            }
	        }
	    }
    	String[] oidsArray = new String[lstPar.size()];
        MapList mapList = new MapList();
        if (lstPar.size() > 0 ) {
            mapList = DomainObject.getInfo(context,
                    (String[]) lstPar.toArray(oidsArray), ConfigurationUtil.getBasicObjectSelects(context));
        }
        if(mapList!=null && mapList.size()>0){
        	for(int j = 0; j < mapList.size(); j++){
        		Map parntMap = (Map)mapList.get(j);	
        		String owningParent = (String)parntMap.get(DomainObject.SELECT_NAME);
        		lstOwningPar.addElement(owningParent);
        	}
        }
	    return lstOwningPar;
	}

	/**
	 * This method is used to return the value of Mandatory column,
	 * 		which depends upon the relationship with which feature is connected to with the context
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments
	 * @return List- the List of Strings containing Mandatory/Not Mandatory values.
	 * @throws FrameworkException
	 *
	 * @since R212
	 * @author A69
	 */
	public List getMandatoryValue(Context context,
            String[] args) throws FrameworkException {

		Map tempMap;
	    matrix.util.List lstMandatory = new StringList();
	    String dispFeatureType = "";
	    String strLanguage=context.getSession().getLanguage();
	    
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList lstObjectIdsList = (MapList) programMap.get("objectList");
            String strNonMandType=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxConfiguration.MandatoryFeatures.NotMandatory",strLanguage);
            String strMandType=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxConfiguration.MandatoryFeatures.Mandatory",strLanguage);
            //String strInvalidStateCheck = EnoviaResourceBundle.getProperty(context,SUITE_KEY, "emxProduct.Alert.FeatureAddOptionNotAllowed",strLanguage);
		    for (int j = 0; j < lstObjectIdsList.size(); j++) {
		        tempMap = (Map)lstObjectIdsList.get(j);
		        String strRelationship = (String)tempMap.get("relationship");
		        if(strRelationship==null || strRelationship.trim().isEmpty()){
			        if (tempMap
							.containsKey(DomainRelationship.SELECT_ID)
							&& ProductLineCommon
									.isNotNull((String) tempMap
											.get(DomainRelationship.SELECT_ID))){
			        	String strRelID=(String) tempMap
			        			.get(DomainRelationship.SELECT_ID);
			        	MapList mlRelInfo = DomainRelationship.getInfo(context, new String[]{strRelID}, new StringList(DomainRelationship.SELECT_NAME));
			        	Map mapReInfo = (Map)mlRelInfo.get(0);
			        	strRelationship=(String)mapReInfo.get(DomainRelationship.SELECT_NAME);
			        }
		        	
		        }

		        if(strRelationship == null || "null".equals(strRelationship)||strRelationship.equals(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS)){
		        	dispFeatureType = ""; //for context row
		        }
		        else if(strRelationship!=null && !strRelationship.equals("") && strRelationship.equals(ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES))
		        {
		        	dispFeatureType = strMandType;
		        }
		        else if(strRelationship!=null && !strRelationship.equals("") && strRelationship.equals(ConfigurationConstants.RELATIONSHIP_VARIES_BY))
		        {
		        	String attrConfigurationType = (String)tempMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_TYPE );		        	
		        	if(attrConfigurationType!=null && attrConfigurationType.equals(ConfigurationConstants.RANGE_VALUE_MANDATORY)){
		        		dispFeatureType = strMandType;
		        	}
		        	else
			        {
			        	dispFeatureType = strNonMandType;
			        }
		        }
		        else
		        {
		        	dispFeatureType = strNonMandType;
		        }
		        lstMandatory.add(dispFeatureType);
		    }
		}
		catch (Exception e) {
			throw new FrameworkException(e);
		}

	    return lstMandatory;
	}

	/**
	 * This method is used to get the candidate Configuration Features of a Context Model
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments
	 * @return mapConfigurationStructure- MapList Containing the Maps of Configuration Features. Each Map will consist of:
	 * 					 id= Configuration Feature ID,
	 * 					 type = Configuration Feature or Sub-types of Configuration Features
	 * 					 name = name of Configuration feature
	 * 					 revision = revision of  Configuration feature
	 * 					 name[connection]= name of the relationship
	 * 					 id[connection] = relationship id
	 * @throws FrameworkException
	 * @author A69
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCandidateConfigurationFeatures(Context context, String[] args)
	throws Exception {

		MapList mapConfigurationStructure =null;
		StringList slObjSelects = new StringList("physicalid");

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);

			String strObjectid = (String)programMap.get("objectId");
			String parentOID = (String)programMap.get("parentOID");
			if(strObjectid.equals(parentOID)){
				int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
				String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

				Model configModel = new Model(strObjectid);
				mapConfigurationStructure = configModel.getCandidateConfigurationFeatures(context, DomainObject.EMPTY_STRING,
						DomainObject.EMPTY_STRING, slObjSelects, null, false,	true, iLevel, 0, DomainObject.EMPTY_STRING,
						DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM, filterExpression);
			}
			else{
				mapConfigurationStructure = getConfigurationFeatureStructure(context, args);
			}

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
		return mapConfigurationStructure;
	}

	/**
	 * This method is used to get the Mandatory Configuration Features of a Context Model
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments
	 * @return mapConfigurationStructure -  Maplist Containing the Maps of Configuration Features. Each Map will consist of:
	 * 					 id= Configuration Feature ID,
	 * 					 type = Configuration Feature or Sub-types of Configuration Features
	 * 					 name = name of Configuration feature
	 * 					 revision = revision of  Configuration feature
	 * 					 name[connection]= name of the relationship
	 * 					 id[connection] = relationship id
	 * @throws FrameworkException
	 * @author A69
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMandatoryConfigurationFeatures(Context context, String[] args)
	throws FrameworkException {

		MapList mapConfigurationStructure =null;
		StringList slObjSelects = new StringList("physicalid");
		StringList slRelSelects = new StringList("frommid["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+ "].to.id");

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);

			String strObjectid = (String)programMap.get("objectId");
			String parentOID = (String)programMap.get("parentOID");
			if(strObjectid.equals(parentOID)){
			// call method to get the level details
			int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

			Model configModel = new Model(strObjectid);
			mapConfigurationStructure = configModel.getMandatoryConfigurationFeatures(context, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, slObjSelects, slRelSelects, false,	true, iLevel, 0, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM, filterExpression);
			}
			else{
				mapConfigurationStructure = getConfigurationFeatureStructure(context, args);
			}

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
		return mapConfigurationStructure;
	}

	/**
	 * This method is used to get the Committed Configuration Features of a Context Model
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments
	 * @return mapConfigurationStructure -  Maplist Containing the Maps of Configuration Features. Each Map will consist of:
	 * 					 id= Configuration Feature ID,
	 * 					 type = Configuration Feature or Sub-types of Configuration Features
	 * 					 name = name of Configuration feature
	 * 					 revision = revision of  Configuration feature
	 * 					 name[connection]= name of the relationship
	 * 					 id[connection] = relationship id
	 * @throws FrameworkException
	 * @author A69
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCommittedConfigurationFeatures(Context context, String[] args)throws FrameworkException {

		MapList mapConfigurationStructure =null;
		StringList slObjSelects = new StringList("physicalid");
		StringList slRelSelects = new StringList("frommid["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+ "].to.id");

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectid = (String)programMap.get("objectId");

			// call method to get the level details
			int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

			Model configModel = new Model(strObjectid);
			mapConfigurationStructure = configModel.getCommittedConfigurationFeatures(context, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, slObjSelects, slRelSelects, false,	true, iLevel, 0, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM, filterExpression);

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
		return mapConfigurationStructure;
	}


	 /**
	    * Method to search features need to exclude from emxFullSearchPage
	    *
	    * @param context - the eMatrix <code>Context</code> object
	    * @param args
	    *            holds arguments
	    * @return featureList - String list of Object IDs to exclude
	    * @throws Exception
	    * @author A69
	    * @since R211
	    */
	   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	   public StringList excludeAvailableConfigurationFeature(Context context, String [] args)
	   throws FrameworkException
	   {
		   HashSet featureList = new HashSet();

		   try{
			   Map programMap = (Map) JPO.unpackArgs(args);
		       String strSourceObjectId = (String) programMap.get("objectId");
		       DomainObject domContextObj = new DomainObject(strSourceObjectId);
		       String txtType = domContextObj.getInfo(context,DomainConstants.SELECT_TYPE);
		       String strWhereExp = DomainConstants.EMPTY_STRING;
		       String strRelPattern = "";
		       short level = 1;

		      String strObjectPattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURES;

		       if(mxType.isOfParentType(context, txtType, ConfigurationConstants.TYPE_PRODUCT_LINE)){
		    	   strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES;

		       }
		       else if(mxType.isOfParentType(context, txtType, ConfigurationConstants.TYPE_MODEL)){
		    	   strRelPattern = ConfigurationConstants.RELATIONSHIP_CANDIDTAE_CONFIGURATION_FEATURES+","
		    	   					+ConfigurationConstants.RELATIONSHIP_COMMITTED_CONFIGURATION_FEATURES+","
		    	   					+ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES;

		       }
		       else if(mxType.isOfParentType(context, txtType, ConfigurationConstants.TYPE_PRODUCTS) || mxType.isOfParentType(context, txtType, ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
		    	   strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+","
  					+ConfigurationConstants.RELATIONSHIP_VARIES_BY;

		       }
		       else if(mxType.isOfParentType(context, txtType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE)){
		    	   strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+","
		    	   					+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS;

		       }

		       StringList objectSelects = new StringList(DomainObject.SELECT_ID);
		       StringList relSelects = new StringList(DomainRelationship.SELECT_ID);

		       short limit = 0;
		       MapList relatedFromFeatureList = new MapList();

		       relatedFromFeatureList = domContextObj.getRelatedObjects(context,
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

		       //add the context Feature
		       featureList.add(strSourceObjectId);

		       for(int i=0;i<relatedFromFeatureList.size();i++)
		       {
		           Map mapFeatureObj = (Map) relatedFromFeatureList.get(i);
		           if(mapFeatureObj.containsKey(objectSelects.get(0)))
		           {
		               Object idsObject = mapFeatureObj.get(objectSelects.get(0));
		               if(idsObject.getClass().toString().contains("StringList"))
		               {
		                   featureList.addAll((StringList)idsObject);
		               }
		               else
		               {
		                   featureList.add((String)idsObject);
		               }
		           }
		       }
		   }
		   catch (Exception e) {
				throw new FrameworkException(e);
		   }

		   ArrayList featureListArray = new ArrayList(featureList);
	       return new StringList(featureListArray);
	   }

	   /**
		 * Method to refresh the candidate feature page in model context after committing selected candidate features
		 *
		 * @param context - the eMatrix <code>Context</code> object
		 * @param args
	     *            holds arguments
		 * @return returnMap -
		 * @throws FrameworkException
		 * @author A69
		 * @since R211
		 */
	   @com.matrixone.apps.framework.ui.PostProcessCallable
	   public Map refreshCandidateFeaturePageOnApply(Context context, String[] args) throws FrameworkException {

		    Map returnMap = new HashMap();
		        returnMap.put ("Action", "execScript");
		        returnMap.put("Message", "{ main:function()  {getTopWindow().close();var listFrame = findFrame(getTopWindow().opener.getTopWindow(),'detailsDisplay');listFrame.location.href=listFrame.location.href;}}");
		    return returnMap;
		}


	   /**
		 * This method is used to commit the Candidate Configuration Features to the selected Product Revisions.
		 *
		 * @param context - Matrix Context Object
		 * @param args
	     *            holds arguments
	     *
		 * @throws FrameworkException
		 * @author A69
		 * @since R212
		 */
		@com.matrixone.apps.framework.ui.ConnectionProgramCallable
		public void commitCandidateFeatureToProduct (Context context, String[] args) throws FrameworkException {
			try{
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				HashMap paramMap = (HashMap) programMap.get("paramMap");
				String strProductObjId= (String)programMap.get("parentOID");
				String strModelId  = (String)paramMap.get("objectId");

				Element elem = (Element) programMap.get("contextData");
				MapList chgRowsMapList = com.matrixone.apps.framework.ui.UITableIndented
				.getChangedRowsMapFromElement(context, elem);

				StringList selectedCandidateFeatures = new StringList(chgRowsMapList.size());

				for (int i = 0; i < chgRowsMapList.size(); i++) {
					HashMap tempMap = (HashMap) chgRowsMapList.get(i);

					selectedCandidateFeatures.addElement((String) tempMap.get("childObjectId"));
				}

				StringList selectedProductRevision = new StringList(strProductObjId);

				Model confModel = new Model(strModelId);
				confModel.commitCandidateConfigurationFeatures(context, selectedCandidateFeatures, selectedProductRevision);
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
		}

		/**
		 * connects the newly created Configuration Feature to the Context Object with approriate relationship
		 *
		 * @param context
		 *            the eMatrix <code>Context</code> object
		 * @param args
	     *            holds arguments
		 * @return void
		 * @throws FrameworkException
		 *             if the operation fails
		 * @since R212
		 */
		public void connectConfigurationFeatureToContext(Context context, String[] args) throws FrameworkException {
					try{
						HashMap programMap = (HashMap)JPO.unpackArgs(args);
						HashMap paramMap   = (HashMap)programMap.get("paramMap");
						HashMap requestMap   = (HashMap)programMap.get("requestMap");

						String[] strarrayparentOID = (String[])requestMap.get("parentOID");
						if(strarrayparentOID != null){
							String strParentID = strarrayparentOID[0];
							DomainObject parentObj = new DomainObject(strParentID);

							String strParentType = parentObj.getInfo(context, DomainConstants.SELECT_TYPE);
							String newObjID = (String)paramMap.get("objectId");

							if(strParentType != null && !"".equals(strParentType) &&  mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_MODEL)){
								Model confModel = new Model(strParentID);
								confModel.connectCandidateConfigurationFeature(context, newObjID);
							}
							else if(strParentType != null && !"".equals(strParentType) && mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_GENERAL_CLASS)){

								DomainRelationship.connect(context, strParentID, ConfigurationConstants.RELATIONSHIP_CLASSIFIED_ITEM, newObjID,false);
							}
						}
					}
					catch(Exception e){
						throw new FrameworkException(e);
					}

		}


		/**
		 * This is a dummy method to provide the feasibility of adding configuration feature mark-up under a product to commit, without showing already committed features
		 *
		 * @param context
		 *            the eMatrix <code>Context</code> object
		 * @param args
	     *            holds arguments
		 * @return Object - empty MapList
		 *
		 * @author IVU
		 * @since FTR R212
		 */

		@com.matrixone.apps.framework.ui.ProgramCallable
		public MapList getTempCommittedConfigurationFeatures(Context context, String[] args){
			return new MapList();
		}

		/**
		 * This is a Access Function to show the Committed To column in the Committed/Mandatory Configuration Features view in the Model Context.
		 *
		 * @param context - the eMatrix <code>Context</code> object
		 * @param args Holds ParamMap having the key "context" which will determine if the columns need to be displayed or not
		 * @return bResult - true - if the context is Committed Features
		 * 					 false - if the Context is not Committed Features
		 * @throws FrameworkException  - throws Exception if any operation fails.
		 * @author A69
		 * @since R212
		 */
		public Object isCommittedToAcceessible(Context context, String[] args)
        throws FrameworkException
	    {
			try {
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
		        String[] arrCTX = (String[])requestValuesMap.get("context");

		        if(arrCTX != null && (arrCTX[0].equals("Mandatory") || arrCTX[0].equals("Committed"))){
		        	return Boolean.valueOf(true);
		        }else{
		        	return Boolean.valueOf(false);
		        }
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
	    }

		/**
		 * This is a Access Function to show the Inheritance Type column in the Mandatory Configuration Features view in the Model Context.
		 *
		 * @param context - the eMatrix <code>Context</code> object
		 * @param args Holds ParamMap having the key "context" which will determine if the columns need to be displayed or not
		 * @return bResult - true - if the context is Mandatory Features
		 * 					 false - if the Context is not Mandatory Features
		 * @throws FrameworkException  - throws Exception if any operation fails.
		 * @author A69
		 * @since R212
		 */
		public Object isInheritanceTypeAcceessible(Context context, String[] args)
        throws FrameworkException
	    {
			try {
				HashMap programMap = (HashMap)JPO.unpackArgs(args);
				HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
		        String[] arrCTX = (String[])requestValuesMap.get("context");
		        String inheritanceTypeAccessible = null;

		        String objectId = (String)programMap.get("objectId");

		        if(objectId!=null && !objectId.equals("null")){
		        	DomainObject dom = new DomainObject(objectId);
		        	String inheritanceTypeSelectable = "to["+ConfigurationConstants.RELATIONSHIP_SUB_PRODUCT_LINES+"].from.type.kindof["+ConfigurationConstants.TYPE_PRODUCT_LINE+"]";
		        	inheritanceTypeAccessible = dom.getInfo(context, inheritanceTypeSelectable);
		        }

		        if((arrCTX != null && (arrCTX[0].equals("Mandatory"))) || (inheritanceTypeAccessible!=null && inheritanceTypeAccessible.equalsIgnoreCase("TRUE"))){
		        	return Boolean.valueOf(true);
		        }else{
		        	return Boolean.valueOf(false);
		        }
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
	    }

		/**
		 * This is a "Committed To" column JPO function to show the Product Revisions to which the Configuration Feature is Committed To.
		 *
		 * @param context - the eMatrix <code>Context</code> object
		 * @param args - contain the ParamMap with the ObjectList of all the Committed Configuration Features
		 * @return committedProd - StringList of the Product Revision with Name and Revsisions of the Committed Configuration Features.
		 * @throws FrameworkException - throws Exception if any operation fails.
		 * @author A69
		 * @since R212
		 */
		public StringList getCommittedToProductList(Context context, String[] args)
	    throws FrameworkException
	    {
			StringList committedProd = new StringList();

			try{
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				MapList lstObjectIdsList = (MapList)programMap.get("objectList");
				HashMap paramMap = (HashMap)programMap.get("paramList");
				String suiteDir = (String) paramMap.get("SuiteDirectory");
		        String suiteKey = (String) paramMap.get("suiteKey");
		        String reportFormat = (String)paramMap.get("reportFormat");


				String strProductName = "";
				String strProductRev = "";
				String strProductID = "";
				for (int i = 0 ;i <lstObjectIdsList.size() ;i++ )
			    {
					String output = "";
					Map map = (Map)lstObjectIdsList.get(i) ;
		            Object objPRIds = (Object)map.get("frommid["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+ "].to.id");

					if(objPRIds instanceof StringList){
						//committedProd.addAll((StringList) objPRIds);
						StringList strTempList = new StringList();
						strTempList = (StringList) objPRIds;
						String[] strPRIds = new String[strTempList.size()];
						for(int j=0; j<strTempList.size();j++ ){
							strPRIds[j] = (String) strTempList.get(j);
						}
						MapList mapPRDetails = DomainObject.getInfo(context,strPRIds ,ConfigurationUtil.getBasicObjectSelects(context));

						for(int k=0; k<mapPRDetails.size();k++){
							Map tempMap = (Map)mapPRDetails.get(k);
							strProductName = tempMap.get(DomainObject.SELECT_NAME).toString();
							strProductRev = tempMap.get(DomainObject.SELECT_REVISION).toString();
							strProductID =  tempMap.get(DomainObject.SELECT_ID).toString();
							StringBuffer temp = new StringBuffer();
							if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
								temp.append(XSSUtil.encodeForHTML(context,strProductName+" "+strProductRev));
							}else{
								temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
								temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
								temp.append("&amp;suiteKey=");
								temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
								temp.append("&amp;objectId=");
								temp.append(XSSUtil.encodeForHTMLAttribute(context, strProductID));
								temp.append("', '450', '300', 'true', 'popup')\">");
								temp.append(XSSUtil.encodeForHTML(context,strProductName+" "+strProductRev));
								temp.append(" <img border=\"0\" src=\"");
								temp.append("images/iconSmallProduct.gif");
								temp.append("\" /> ");
								temp.append("</a>");
							}

							output = output + "," + temp;
						}
						output = output.replaceFirst(",", "");
						committedProd.add(output);
					}else if (objPRIds instanceof String ){
						DomainObject domProdRev = new DomainObject((String)objPRIds);
						Map mapPRDetails = domProdRev.getInfo(context, ConfigurationUtil.getBasicObjectSelects(context));
						strProductName = mapPRDetails.get(DomainObject.SELECT_NAME).toString();
						strProductRev = mapPRDetails.get(DomainObject.SELECT_REVISION).toString();
						strProductID =  mapPRDetails.get(DomainObject.SELECT_ID).toString();
						StringBuffer temp = new StringBuffer();
						if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
							temp.append(strProductName+" "+strProductRev);
						}else{
							temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
							temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
							temp.append("&amp;suiteKey=");
							temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
							temp.append("&amp;objectId=");
							temp.append(XSSUtil.encodeForHTMLAttribute(context, strProductID));
							temp.append("', '450', '300', 'true', 'popup')\">");
							temp.append(XSSUtil.encodeForHTML(context,strProductName+" "+strProductRev));
							temp.append(" <img border=\"0\" src=\"");
							temp.append("images/iconSmallProduct.gif");
							temp.append("\" /> ");
							temp.append("</a>");
						}
						committedProd.add(temp.toString());
					}else if (objPRIds==null ){
						committedProd.add("");
					}
		        }
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
			return committedProd;
	    }

		/**
		 * This is a Access Function to show or hide the Column ConfigurationSelectionType and Edit command in property page
		 *  depends upon the param context=ConfigurationFeature passed in request parameter
		 *
		 * @param context - the eMatrix <code>Context</code> object
		 * @param args Holds ParamMap having the key "context", value of which will determine if the columns need to be displayed or not
		 * @return bResult - true - if the context is Committed Features
		 * 					 false - if the Context is not Committed Features
		 * @throws FrameworkException  - throws Exception if any operation fails.
		 * @author A69
		 * @since R212
		 */
		public Object isConfigurationFeatureContext(Context context, String[] args)
	    throws FrameworkException
	    {
			try{
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				String[] ctx = null;
				if(programMap != null){
				HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
				
				if(requestValuesMap!=null){
					ctx = (String[])requestValuesMap.get("context");
				}
				else{
					if(programMap.containsKey("context")){
					ctx = new String[]{programMap.get("context").toString()};
					}
				}
				}

				if(ctx!= null && ctx[0] != null && ctx[0].equals("ConfigurationFeature")){
					return Boolean.valueOf("true");
				}else{
					return Boolean.valueOf("false");
				}
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
	    }

		/**
		 * This is a Access Function to show or hide Edit command in property page
		 *  depends upon the param context=ConfigurationOption passed in request parameter
		 *
		 * @param context - the eMatrix <code>Context</code> object
		 * @param args Holds ParamMap having the key "context", value of which will determine if the columns need to be displayed or not
		 * @return bResult - true - if the context is Committed Features
		 * 					 false - if the Context is not Committed Features
		 * @throws FrameworkException  - throws Exception if any operation fails.
		 * @author A69
		 * @since R212
		 */
		public Object isConfigurationOptionContext(Context context, String[] args)
	    throws FrameworkException
	    {
			try{
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				String[] ctx = null;
				if(programMap != null){
				HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
				
				if(requestValuesMap!=null){
					ctx = (String[])requestValuesMap.get("context");
				}
				else{
					if(programMap.containsKey("context")){
					ctx = new String[]{programMap.get("context").toString()};
					}
				}
				}

				if(ctx!= null && ctx[0] != null && ctx[0].equals("ConfigurationOption")){
					return Boolean.valueOf(true);
				}else{
					return Boolean.valueOf(false);
				}
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
	    }

		/**
		 *
		 * This method is used to copy the Configuration Feature Structure of a product to another product
		 *
		 * @param context - Matrix context
		 * @param sourceObjectId - Source product ID from which the structure need to be copy
		 * @param destinationObjectId - Destination product ID to which the structure need to be copy
		 * @throws FrameworkException - - when operation fails
		 * @author A69
		 * @since R212
		 */
		public void copyConfigurationFeature(Context context, String[] args)
		throws FrameworkException
		{
			try{
				ArrayList programMap = (ArrayList)JPO.unpackArgs(args);

	            String sourceObjectId = (String) programMap.get(0);
	            String destinationObjectId = (String) programMap.get(1);

				ConfigurationFeature.copyConfigurationFeature(context, sourceObjectId, destinationObjectId);
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
		}

		/**
	     * This method is used to control the access for Configuration Features commands
	     * @param context The ematrix context object
	     * @param String The args
	     * @return Boolean
	     * @throws FrameworkException - - when operation fails
		 * @author A69
	     * @since R212
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

	    /** This method gets the object Structure List for the context Configuration Feature.This method gets invoked
	     * by settings in the command which displays the Structure Navigator for Configuration Feature type objects
	     *  @param context the eMatrix <code>Context</code> object
	     *  @param args    holds the following input arguments:
	     *      		   contextObjId - String having context object Id
	     *  @return MapList containing the object list to display in Product Line structure navigator
	     *  @throws FrameworkException - if the operation fails
	     *  @author A69
	     *  @since R212
	     */

	    public static MapList getStructureList(Context context, String[] args)
	        throws FrameworkException {
	    	MapList confFeatureStructList = new MapList();

	    	try{
		    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
		        HashMap paramMap   = (HashMap)programMap.get("paramMap");
		        String contextObjId    = (String)paramMap.get("objectId");
		        String strParentSymType = "";
		        String strParentType = "";
		        String strRelPattern = "";
		        String[] arrRel = null;
		        DomainObject domContextObj = new DomainObject(contextObjId);
			    if(domContextObj.isKindOf(context, ConfigurationConstants.TYPE_PRODUCT_LINE)){
			    	strParentType = ConfigurationConstants.TYPE_PRODUCT_LINE;
			    }else if(domContextObj.isKindOf(context, ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
			    	strParentType = ConfigurationConstants.TYPE_PRODUCT_VARIANT;
			    }else if(domContextObj.isKindOf(context, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE)){
			    	strParentType = ConfigurationConstants.TYPE_CONFIGURATION_FEATURE;
			    }else if(domContextObj.isKindOf(context, ConfigurationConstants.TYPE_CONFIGURATION_OPTION)){
			    	strParentType = ConfigurationConstants.TYPE_CONFIGURATION_OPTION;
			    }else if(domContextObj.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS)){
			    	strParentType = ConfigurationConstants.TYPE_PRODUCTS;
			    }else if(domContextObj.isKindOf(context, ConfigurationConstants.TYPE_MODEL)){
			    	strParentType = ConfigurationConstants.TYPE_MODEL;
			    }

			    strParentSymType = FrameworkUtil.getAliasForAdmin(context,ConfigurationConstants.SELECT_TYPE,strParentType,true);

			    String strAllowedSTRel = EnoviaResourceBundle.getProperty(context, "emxConfiguration.StructureTree.SelectedRel."+strParentSymType);
			    if(strAllowedSTRel!=null && !strAllowedSTRel.equals("")){
			    	arrRel = strAllowedSTRel.split(",");

				    for(int i=0; i< arrRel.length; i++){
				    	strRelPattern = strRelPattern + "," + PropertyUtil.getSchemaProperty(context,arrRel[i]);
				    }
				    strRelPattern = strRelPattern.replaceFirst(",", "");
			    }
			    
			    if(strAllowedSTRel!=null && !strAllowedSTRel.equals("")){
			       	StringList objectSelects = new StringList(3);
		            objectSelects.add(ConfigurationConstants.SELECT_ID);
		            objectSelects.add(ConfigurationConstants.SELECT_TYPE);
		            objectSelects.add(ConfigurationConstants.SELECT_NAME);

					ConfigurationUtil confUtil = new ConfigurationUtil(contextObjId);
					confFeatureStructList = confUtil.getObjectStructure(context,"*",strRelPattern,
							objectSelects, null ,false, true, (short) 1,0,
							"", "",DomainObject.FILTER_STR_AND_ITEM, DomainObject.EMPTY_STRING);
		        	
		        }
			}
	    	catch(Exception e){
				throw new FrameworkException(e);
			}
	        return confFeatureStructList;
	    }

	    /** This method is used to get the first Revision according to the selected Policy
	     *
	     *  @param context the eMatrix <code>Context</code> object
	     *  @param args    holds the following input arguments:
	     *      		   contextObjId - String having context object Id
	     *  @return HashMap containing Map of display and actual value of Revision
	     *  @throws FrameworkException - if the operation fails
	     *  @author A69
	     *  @since R212
	     */

	    public HashMap getRevision (Context context,String[] args)throws FrameworkException
	    {
	    	HashMap returnMap = new HashMap();

	    	try{
	    		HashMap hmProgramMap = (HashMap) JPO.unpackArgs(args);
				HashMap fieldValues = (HashMap) hmProgramMap.get( "fieldValues" );
				String policy = (String)fieldValues.get("Policy");
				Policy policyObj = new Policy(policy);
				String revision = policyObj.getFirstInSequence(context);

			    returnMap.put("SelectedValues", revision);
			    returnMap.put("SelectedDisplayValues", revision);
	    	}
	    	catch(Exception e){
				throw new FrameworkException(e);
			}

			return returnMap;
	    }


/**
* This method filters out the Common groups based on name and limit filters
* @param commonGroupList - Maplist
* @param strNameValue - String
* @param limit - int
* @return Maplist
*  @since R212
*/

 private MapList getFilteredCGList(MapList commonGroupList, String strNameValue, int limit) {

	 //If the name filter is * or null return all the records.
	 if((strNameValue == null || strNameValue.equalsIgnoreCase("*")) && limit ==0) {
	            return commonGroupList;
	 }

	 //variable to handle the limit filter
	 int count = 0;
	 if(limit == 0) {

     //initialize the limit to max if limit is 0
	            limit = commonGroupList.size();
	 }

	 MapList tempMaplist = new MapList();
	 Iterator iterator = commonGroupList.iterator();
	 while(iterator.hasNext()) {
	 Map m = (Map) iterator.next();
	 String strName = (String) m.get("Common Group Name");

     //if the name matches and the limit is not reached add the record to templist
	 if(hasMatchingCGName(strName,strNameValue) && count < limit) {
		 tempMaplist.add(m);
	     count++;
	 }
	}
  return tempMaplist;
}


/**
* This method is a helper method for getFilteredCGList. This matches the name with common group name
* @param strName - String
* @param strPattern - String
* @return boolean
* @since R212
*/
private boolean hasMatchingCGName(String strName, String strPattern) {

	//If the naming pattern is null or * return true
	        if(strPattern == null || strPattern.equalsIgnoreCase("*")) {
	            return true;
	        }
	        int iTokenStartPos = strPattern.indexOf("*");
	        int iTokenEndPos = strPattern.lastIndexOf("*");
	        int iSubStrStartPos=0;
	        String subStr = "";
	        StringTokenizer strCGNameTokenizer =  new StringTokenizer(strPattern , "*");
	        //loop thru the tokens of naming filter
	        for (int i = 0; strCGNameTokenizer.hasMoreTokens(); i++) {
	            subStr = strCGNameTokenizer.nextToken();
	            //If * doesn't appear in the first position then the name starts with first token
	            if(i==0 && iTokenStartPos != 0 && !strName.startsWith(subStr)){
	                return false;
	            }//if * doesn't appear at the end then name ends with the last token.
	            else if(!strCGNameTokenizer.hasMoreTokens()&& iTokenEndPos != strPattern.length()-1 && !strName.endsWith(subStr)){
	                return false;
	            }else if(strName.indexOf(subStr,iSubStrStartPos)==-1){
	                return false;
	            }
	            //position to start search for next token.
	            iSubStrStartPos = strName.indexOf(subStr);
	        }
	        return true;
	    }

		/**
	     * In case Configuration Option is getting created under a Configuration Feature, it display Context Configuration Feature name in the WebForm
	     * In case stand-alone Configuration Option is getting created (Global Action), it Provite facility to choose context Configuration Feature
	     *
	     * @param context the Matrix Context
	     * @param args contains-
	     * 				-- programMap -> requestMap -> String objectId/ String UIContext
	     * 				-- programMap -> fieldMap -> String name
	     * @returns String
	     * @throws FrameworkException if the operation fails
	     * @author A69
	     * @since R212
	     */
	 public String displayContextConfigurationFeature (Context context, String[] args) throws FrameworkException
	 { 
		 StringBuffer sb = new StringBuffer();
		 
		 try{
			 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		      HashMap requestMap = (HashMap) programMap.get("requestMap");
		      
		      String strObjectID = (String)requestMap.get("objectId");
		      String strUIContext = (String)requestMap.get("UIContext");

		      if(!ProductLineCommon.isNotNull(strObjectID) ){
		    	  strObjectID = "";
		      }

		      if( (ProductLineCommon.isNotNull(strUIContext) && !(strUIContext.equals("GlobalActions")||strUIContext.equals("Classification"))) || strObjectID.length()!= 0){
		    	  DomainObject objContext = DomainObject.newInstance(context, strObjectID);

		    	  StringList newList = new StringList();
		    	  newList.addElement(ProductLineConstants.SELECT_NAME);
		    	  newList.addElement(ProductLineConstants.SELECT_REVISION);	
		    	  newList.addElement(ProductLineConstants.SELECT_TYPE);
		    	  Map strContextMap = objContext.getInfo(context, newList);
					
		    	  String strTemp = "<a TITLE=";
		    	  String strEndHrefTitle = ">";
		    	  String strEndHref = "</a>";
		    	  sb =  sb.append(strTemp);
		    	  String strTypeIcon= ProductLineCommon.getTypeIconProperty(context, (String) strContextMap.get(ProductLineConstants.SELECT_TYPE));

		    	  sb =  sb.append("\"\"")
		    	  .append(strEndHrefTitle)
		    	  .append("<img border=\'0\' src=\'../common/images/"+strTypeIcon+"\'/>")
		    	  .append( strEndHref)
		    	  .append(" ");
		    	  sb =  sb.append(XSSUtil.encodeForXML(context,strContextMap.get(ProductLineConstants.SELECT_NAME).toString())+" "+strContextMap.get(ProductLineConstants.SELECT_REVISION));
		      }
		      
		      
		 }
		 catch(Exception e){
				throw new FrameworkException(e);
		}

	    return sb.toString();
	}

	 /**
		 * connects the newly created Configuration Option to the Context Object with approriate relationship
		 *
		 * @param context - Matrix Context Object
		 * @param args - Holds the following arguments 0 - HashMap containing the following arguments
		 * 					objectId
		 * 					ConfigurationFeatureOID
		 * @throws FrameworkException if the operation fails
		 *
		 * @since R212
		 */

		public void connectConfigurationOption(Context context, String[] args)throws FrameworkException {

				try{
					HashMap programMap = (HashMap)JPO.unpackArgs(args);
				    HashMap paramMap   = (HashMap)programMap.get("paramMap");
				    HashMap requestMap   = (HashMap)programMap.get("requestMap");
				    String  strConfigurationOptionOID = (String)paramMap.get("objectId");
				    String[] strConfigurationFeatureOIDs = (String[])requestMap.get("ConfigurationFeature");
				    String strConfigurationFeatureOID = "";
				    if(strConfigurationFeatureOIDs != null && strConfigurationFeatureOIDs[0]!= null && !strConfigurationFeatureOIDs[0].equals("")&& !strConfigurationFeatureOIDs[0].equals("null")){

					    strConfigurationFeatureOID = strConfigurationFeatureOIDs[0];

						boolean bInvalidState = false;

						DomainObject fromDomObj = new DomainObject(strConfigurationFeatureOID);
				        DomainObject toDomObj = new DomainObject(strConfigurationOptionOID);

						bInvalidState = ConfigurationUtil.isFrozenState(context, strConfigurationFeatureOID);

						if(bInvalidState == true){
							String strLanguage = context.getSession().getLanguage();
							String strInvalidStateCheck = EnoviaResourceBundle.getProperty(context,SUITE_KEY, "emxProduct.Alert.FeatureAddOptionNotAllowed",strLanguage);
							throw new FrameworkException(strInvalidStateCheck);
						}
						else{
							DomainRelationship.connect(context, fromDomObj, ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS, toDomObj);
							String[] strarrayparentOID = (String[])requestMap.get("parentOID");
								if(strarrayparentOID != null){
									String strParentID = strarrayparentOID[0];
									DomainObject parentObj = new DomainObject(strParentID);

									String strParentType = parentObj.getInfo(context, DomainConstants.SELECT_TYPE);
									String newObjID = (String)paramMap.get("objectId");
									if(strParentType != null && !"".equals(strParentType) &&  mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_GENERAL_CLASS)){

										DomainRelationship.connect(context, strParentID, ConfigurationConstants.RELATIONSHIP_CLASSIFIED_ITEM, newObjID,false);
									}
								}
						}
				    }else{
						String[] strarrayparentOID = (String[])requestMap.get("parentOID");
						if(strarrayparentOID != null){
							String strParentID = strarrayparentOID[0];
							DomainObject parentObj = new DomainObject(strParentID);

							String strParentType = parentObj.getInfo(context, DomainConstants.SELECT_TYPE);
							String newObjID = (String)paramMap.get("objectId");

							if(strParentType != null && !"".equals(strParentType) &&  mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_MODEL)){
								Model confModel = new Model(strParentID);
								confModel.connectCandidateConfigurationFeature(context, newObjID);
							}
							else if(strParentType != null && !"".equals(strParentType) && mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_GENERAL_CLASS)){

								DomainRelationship.connect(context, strParentID, ConfigurationConstants.RELATIONSHIP_CLASSIFIED_ITEM, newObjID,false);
							}
						}
				    }
				}
				catch(Exception e){
					throw new FrameworkException(e);
				}
		}


		/**
	     * Method call to get the list of all related Products of a given context.
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args - Holds parameters passed from the calling method
	     * @return - Maplist of bus ids of candidate Products
	     * @throws FrameworkException - if the operation fails
	     * @since R212
	     * @Owner A69
	     */
	    @com.matrixone.apps.framework.ui.ProgramCallable
	    public MapList getRelatedProductsForContext (Context context, String[] args) throws FrameworkException {
	    	MapList relBusObjPageList = new MapList();

	    	try{
	    		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		    	String parentOID = (String)programMap.get("parentOID");
		    	relBusObjPageList = ConfigurationUtil.getRelatedProductsForContext(context, parentOID);
	    	}
	    	catch(Exception e){
				throw new FrameworkException(e);
			}
	        return  relBusObjPageList;
	    }

	    /**
		 * This is an Utility method to check if the passed Configuration Feature business object is used in any Rule Expression
		 * @param context
		 * @param args
		 * @return If used in any Rule Expression return true, else false.
		 * @throws FrameworkException
		 * @since R212
		 * @author A69
		 */
		public static int isUsedInRulesForTypeCheck(Context context, String []args) throws FrameworkException{

			try{
				String strLanguage = context.getSession().getLanguage();
		    	  String strSubjectKey = EnoviaResourceBundle.getProperty(context,
		    			SUITE_KEY, "emxProduct.Alert.UsedInRuleExpression",strLanguage);

				  String ContextID = args[0];
		       	  DomainObject domContextBus = new DomainObject(ContextID);

		          StringList selectables = new StringList();

		          String strObjBCRLE = "to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
		          String strObjBCRRE = "to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
		          String strObjMPRLE = "to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
		          String strObjMPRRE = "to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
		          String strObjQRLE = "to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
		          String strObjQRRE = "to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
		          String strObjIRRE = "to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_INCLUSION_RULE+"]";
		          String strRelBCRLE = "to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
		          String strRelBCRRE = "to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
		          String strRelMPRLE = "to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
		          String strRelMPRRE = "to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
		          String strRelQRLE = "to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
		          String strRelQRRE = "to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
		          String strRelIRRE = "to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_INCLUSION_RULE+"]";

		          selectables.add(strObjBCRLE);
		          selectables.add(strObjBCRRE);
		          selectables.add(strObjMPRLE);
		          selectables.add(strObjMPRRE);
		          selectables.add(strObjQRLE);
		          selectables.add(strObjQRRE);
		          selectables.add(strObjIRRE);
		          selectables.add(strRelBCRLE);
		          selectables.add(strRelBCRRE);
		          selectables.add(strRelMPRLE);
		          selectables.add(strRelMPRRE);
		          selectables.add(strRelQRLE);
		          selectables.add(strRelQRRE);
		          selectables.add(strRelIRRE);

		          DomainConstants.MULTI_VALUE_LIST.add(strObjBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjQRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjQRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strObjIRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelQRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelQRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelIRRE);

		          Map mapUsedResult = domContextBus.getInfo(context, selectables);

		          DomainConstants.MULTI_VALUE_LIST.remove(strObjBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjQRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjQRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strObjIRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelQRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelQRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelIRRE);

		          StringList slUsage = ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjBCRLE));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjBCRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjMPRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjMPRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjQRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjQRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strObjIRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelBCRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelBCRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelMPRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelMPRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelQRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelQRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelIRRE)));

		          if(slUsage.contains("TRUE") || slUsage.contains("True")){
		        	  throw new FrameworkException(strSubjectKey);
		          }
			}
			catch(Exception e){
				throw new FrameworkException(e.getMessage());
			}
	        return 0;
		}

		/**
		 * This is an Utility method to check if the passed Configuration Feature relationship is used in any Rule Expression
		 * @param context
		 * @param args
		 * @return If used in any Rule Expression return 0
		 * @throws FrameworkException
		 * @since R212
		 * @author A69
		 */
		public static int isUsedInRulesForRelCheck(Context context, String []args) throws FrameworkException{
			try{
				String strLanguage = context.getSession().getLanguage();
		    	  String strSubjectKey = EnoviaResourceBundle.getProperty(context,
		    			SUITE_KEY, "emxProduct.Alert.UsedInRuleExpression",strLanguage);

				  String relID = args[0];

		          StringList selectables = new StringList();

	              String strRelBCRLE = "tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
	              String strRelBCRRE = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
	              String strRelMPRLE = "tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
	              String strRelMPRRE = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
	              String strRelQRLE = "tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
	              String strRelQRRE = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
	              String strRelIRRE = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_INCLUSION_RULE+"]";

	              selectables.add(strRelBCRLE);
	              selectables.add(strRelBCRRE);
	              selectables.add(strRelMPRLE);
	              selectables.add(strRelMPRRE);
	              selectables.add(strRelQRLE);
	              selectables.add(strRelQRRE);
	              selectables.add(strRelIRRE);

		          DomainConstants.MULTI_VALUE_LIST.add(strRelBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelQRLE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelQRRE);
		          DomainConstants.MULTI_VALUE_LIST.add(strRelIRRE);

		          MapList mlUsedResult = DomainRelationship.getInfo(context, new String[]{relID}, selectables);

		          DomainConstants.MULTI_VALUE_LIST.remove(strRelBCRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelBCRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelMPRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelMPRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelQRLE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelQRRE);
		          DomainConstants.MULTI_VALUE_LIST.remove(strRelIRRE);

		          Map mapUsedResult = (Map)mlUsedResult.get(0);

		          StringList slUsage = ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelBCRLE));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelBCRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelMPRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelMPRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelQRLE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelQRRE)));
		          slUsage.addAll(ConfigurationUtil.convertObjToStringList(context,mapUsedResult.get(strRelIRRE)));

		          if(slUsage.contains("TRUE") || slUsage.contains("True")){
		        	  throw new FrameworkException(strSubjectKey);
		          }
			}
			catch(Exception e){
				throw new FrameworkException(e.getMessage());
			}
	        return 0;
		}


	    /**
	     * This method is used to return the status icon of an object
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds arguments
	     * @return List- the List of Strings in the form of 'Name Revision'
	     * @throws FrameworkException - if the operation fails
	     * @since R212
	     * @author A69
	    **/

	    public List getStatusIcon (Context context, String[] args) throws FrameworkException{
	    	List lstNameRev = new StringList();
	        try{
	        	Map programMap = (HashMap) JPO.unpackArgs(args);
		        List lstobjectList = (MapList) programMap.get("objectList");
		        Iterator objectListItr = lstobjectList.iterator();
		        Map paramList = (HashMap)programMap.get("paramList");
		        String reportFormat = (String)paramList.get("reportFormat");
		        
		        //initialise the local variables
		        Map objectMap = new HashMap();
		        String strObjId = DomainConstants.EMPTY_STRING;
		        String strObjState = DomainConstants.EMPTY_STRING;
		        String strIcon = DomainConstants.EMPTY_STRING;
		        String strObjPolicy = DomainConstants.EMPTY_STRING;
		        String strObjPolicySymb = DomainConstants.EMPTY_STRING;
		        String strObjStateSymb = DomainConstants.EMPTY_STRING;
		        StringBuffer sbStatePolicyKey = new StringBuffer();
		        boolean flag = false;

		        StringBuffer stbNameRev = new StringBuffer(100);
		        DomainObject domObj = null;
		        //loop through all the records
		        while(objectListItr.hasNext())
		        {
		            objectMap = (Map) objectListItr.next();
		            strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
		            domObj = DomainObject.newInstance(context, strObjId);
		            strObjState = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
		            strObjPolicy = domObj.getInfo(context, DomainConstants.SELECT_POLICY);

		            // Getting symbolic names for both policy & state
		            strObjPolicySymb = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_POLICY,strObjPolicy,true);
		            strObjStateSymb = FrameworkUtil.reverseLookupStateName(context, strObjPolicy, strObjState);

		            // Forming the key which is to be looked up
		            sbStatePolicyKey = new StringBuffer("emxProduct.LCStatusImage.");
		            sbStatePolicyKey.append(strObjPolicySymb)
		                            .append(".")
		                            .append(strObjStateSymb);

		            // Getting the value for the corresponding key, if not catching it to set flag = false
		            try{
		                strIcon = EnoviaResourceBundle.getProperty(context,sbStatePolicyKey.toString());
		                flag = true;
		            }
		            catch(Exception ex)
		            {
		                flag = false;
		            }

		            if(flag)
		            {
		                strObjState = FrameworkUtil.findAndReplace(strObjState," ", "");
		                strObjPolicy = FrameworkUtil.findAndReplace(strObjPolicy," ", "_");
		                StringBuffer sbStateKey = new StringBuffer("emxFramework.State.");		   
		                sbStateKey.append(strObjState);
		                strObjState = EnoviaResourceBundle.getProperty(context,
		                            SUITE_KEY, sbStateKey.toString(),context.getSession().getLanguage());
		                stbNameRev.delete(0, stbNameRev.length());
		                if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
		            		lstNameRev.add(strObjState);
		            	}else{
			                stbNameRev = stbNameRev.append("<img src=\"../common/images/")
			                                .append(strIcon)
			                                .append("\" border=\"0\"  align=\"middle\" ")
			                                .append("TITLE=\"")
			                                .append(" ")
			                                .append(XSSUtil.encodeForXML(context, strObjState))
			                                .append("\"")
			                                .append("/>");
			                lstNameRev.add(stbNameRev.toString());
		            	}
		            }
		            else
		            {
		                lstNameRev.add(DomainConstants.EMPTY_STRING);
		            }
		        }
	        }
	        catch(Exception e){
				throw new FrameworkException(e);
			}
	        return lstNameRev;
	    }


		/**
		 * This is an Utility method to check if the passed Configuration Feature is Inherited
		 * @param context
		 * @param args
		 * @return If inherited return 0
		 * @throws FrameworkException
		 * @since R212
		 * @author A69
		 */
		public static int isInherited(Context context, String []args) throws FrameworkException{
			try{
				String strContextRemove = PropertyUtil.getGlobalRPEValue(context,"ContextRemoveCheckForMCF");

				if(strContextRemove!=null && "TRUE".equals(strContextRemove)){
					return 0;
				}

				String strLanguage = context.getSession().getLanguage();
		    	  String strSubjectKey = EnoviaResourceBundle.getProperty(context,
		    			SUITE_KEY, "emxProduct.Alert.CanNotRemoveDeleteInherited",strLanguage);

				  String relID = args[0];

				  StringList selectables = new StringList();
				  selectables.addElement(DomainRelationship.SELECT_TYPE);
				  selectables.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_INHERITED+"]");

				  MapList mlSelectable = DomainRelationship.getInfo(context, new String[]{relID}, selectables);

				  Map mapSelectable = (Map)mlSelectable.get(0);

				  String relationshipType = (String)mapSelectable.get(DomainRelationship.SELECT_TYPE);

				  if(relationshipType != null && relationshipType.equals(ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES)){
					  String strInherited = (String)mapSelectable.get("attribute["+ConfigurationConstants.ATTRIBUTE_INHERITED+"]");
					  	if(strInherited != null && strInherited.equals("True")){
							throw new FrameworkException(strSubjectKey);
						}
				  }
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
			return 0;
		}
 /**
		 * This Methods is used to get the Configuration Options.
		 * This is used while
		 * 1)launching the Design Variants of a Logical Feature in Product Context
		 * 2)launching Split View from ProductLine Context
		 * @param context
		 *            the eMatrix <code>Context</code> object
		 * @param args -
		 *            Holds the following arguments 0 - HashMap containing the
		 *            following arguments
		 * @return Object - MapList containing the id of  Configuration Options
		 * @throws Exception
		 *             if the operation fails
		 * @author WKU
		 * @since FTR R212
		 */
		@com.matrixone.apps.framework.ui.ProgramCallable
		public MapList expandConfigurationOptions(Context context, String[] args)
		throws Exception {

			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");
			MapList mapList = new MapList();
			String strType = "";
			if(ProductLineCommon
					.isNotNull(strObjectId))
			{
				DomainObject dom = new DomainObject(strObjectId);
				strType = dom.getInfo(context, DomainConstants.SELECT_TYPE);
			}			
			if(mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES))
			{			
			String strExpandLevel = (String) programMap.get("expandLevel");
			String strObjWhere = "";
			int level = 1;
			if(strExpandLevel != null || "".equalsIgnoreCase(strExpandLevel) || "null".equalsIgnoreCase(strExpandLevel))
				level = Integer.parseInt(strExpandLevel);
			int limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxConfiguration.ExpandLimit"));
			
			String sNameFilterValue = (String) programMap.get("FTRDesignVariantNameFilterCommand");
			if ((null == sNameFilterValue || "null".equals(sNameFilterValue)|| "".equals(sNameFilterValue))) 
            {
				sNameFilterValue = (String) programMap.get("FTRDesignVariantNonContextNameFilterCommand");
            }  
			
			String sLimitFilterValue = (String) programMap.get("FTRDesignVariantLimitFilterCommand");
			if ((null == sLimitFilterValue || "null".equals(sLimitFilterValue)|| "".equals(sLimitFilterValue))) 
            {
				sLimitFilterValue = (String) programMap.get("FTRDesignVariantNonContextLimitFilterCommand");
            } 
			
			if (sLimitFilterValue != null
					&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
				if (sLimitFilterValue.length() > 0) {
					limit = (short) Integer.parseInt(sLimitFilterValue);
					if (limit < 0) {
						limit = 32767;
					}
				}
			}
			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {

				strObjWhere = "attribute["
					+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
					+ "] ~~ '" + sNameFilterValue + "'";
			}
				String strTypePattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURES;
	            String strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+","+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS;
	            StringList slObjSelects = new StringList();
	            slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
	            slObjSelects.addElement(ConfigurationConstants.SELECT_TYPE);
	            slObjSelects.addElement(ConfigurationConstants.SELECT_NAME);
	            slObjSelects.addElement(ConfigurationConstants.SELECT_REVISION);
	            slObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]");
	            StringList slRelSelects = new StringList();
	            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
	            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_TYPE);
	            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);
	            ConfigurationUtil utilObj = new ConfigurationUtil(strObjectId);

				mapList = utilObj.getObjectStructure(context,
	        			strTypePattern, strRelPattern,
	        			slObjSelects, slRelSelects, false,
	        			true, level, limit, strObjWhere,
	        			ConfigurationConstants.EMPTY_STRING, (short)0,
	        			null);
			}

			return mapList;
		}


		/** Method call as a trigger to check if the sub-features associated with the
	     * Configuration Feature are in Release state.
	     *
	     * @param context
	     *            the eMatrix <code>Context</code> object
	     * @param args -
	     *            Holds the parameters passed from the calling method
	     * @return int - Returns 0 in case of Check trigger is success and 1 in case
	     *         of failure
	     * @throws Exception
	     *             if operation fails
	     * @since R212
	     * @author A69
	     */
	    public int checkSubFeaturesForConfigurationFeaturePromote(Context context, String args[])
	            throws Exception {
	        // return value of the function
	        int iReturn = 0;
	        // The feature object id sent by the emxTriggerManager is retrieved here.
	        String objectId = args[0];
	        String strSelect = DomainConstants.SELECT_CURRENT;
	        // ObjectSelects StringList is initialized
	        StringList objectSelects = new StringList(strSelect);
	        ConfigurationFeature configurationFeature = new  ConfigurationFeature(objectId);

	        StringList tempStringList = new StringList();
	        //Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
	        MapList relBusObjList = configurationFeature.getConfigurationFeatureStructure(context, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE,
	        		ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES, objectSelects, tempStringList , false, true, 0, 0,
	        		DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short)1, DomainConstants.EMPTY_STRING);

	        int iNumberOfObjects = relBusObjList.size();
	        boolean isFrozen = false;

	        if (iNumberOfObjects > 0) {

	            for (int i = 0; i < iNumberOfObjects; i++) {
	            	Map relBusObjMap = (Map)relBusObjList.get(i);
	            	String strSubFeatureID = (String)relBusObjMap.get(DomainConstants.SELECT_ID);
	            	isFrozen = ConfigurationUtil.isFrozenState(context,strSubFeatureID);
	                if (!isFrozen) {
	                    String strAlertMessage = EnoviaResourceBundle.getProperty
	                            (context,SUITE_KEY,
	                                    "emxProduct.Alert.ConfigurationFeaturesPromoteFailedStateNotRelease",context.getSession().getLanguage());
	                    emxContextUtilBase_mxJPO
	                            .mqlNotice(context, strAlertMessage);
	                    iReturn = 1;
	                    break;
	                }

	            }
	        }
	        // Return 0 is validation is passed
	        return iReturn;
	    }

	    /**
	     * Method call as a trigger to promote all the sub Features to the Release state if
	     * they already are not in that.
	     *
	     * @param context
	     *            the eMatrix <code>Context</code> object
	     * @param args -
	     *            Holds the parameters passed from the calling method
	     * @return int - Returns 0 in case of Check trigger is success and 1 in case
	     *         of failure
	     * @throws Exception
	     *             if operation fails
	     * @since R212
	     * @author A69
	     */
	    public int promoteSubFeatures(Context context, String args[]) throws Exception {
	        // The Parent Feature object id sent by the emxTriggerManager
	        String objectId = args[0];
	        String state = args[1];

	        // The object id is set to the context
	        setId(objectId);

	        // Set up for query
        	String strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES;
	        String strTypePattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURE;
	        StringList objectSelects = new StringList(ConfigurationConstants.SELECT_ID);

	        // Object where condition to retrieve the sub feature objects that are not already in Release state.
	        StringBuffer strObjectWhere = new StringBuffer(128);
	        strObjectWhere.append("current");
	        strObjectWhere.append(" != '");
	        strObjectWhere.append(state);
	        strObjectWhere.append("'");

	        ConfigurationFeature configurationFeature = new  ConfigurationFeature(objectId);

	        StringList tempStringList = new StringList();

	        //Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
	        MapList relBusObjList = configurationFeature.getConfigurationFeatureStructure(context, strTypePattern, strRelationshipPattern,
	        		objectSelects, tempStringList, false, true, 0, 0, strObjectWhere.toString(),
	        		DomainConstants.EMPTY_STRING, (short)1, DomainConstants.EMPTY_STRING);

	        // The number of objects connected is obtained.
	        int iNumberOfObjects = relBusObjList.size();
	        // The promotion is to happen only if there are any connected objects
	        if (iNumberOfObjects > 0) {
	            // Processing for each of the object connected to the product
	            for (int i = 0; i < iNumberOfObjects; i++) {
	                // Each id of the object connected to product is obtained.
	                String strTempObjectId = (String) ((Hashtable) relBusObjList
	                        .get(i)).get(objectSelects.get(0));
	                // The context is set with the object id obtained.
	                setId(strTempObjectId);
	                // The state of the context object is set to release.
	                setState(context, state);
	            }
	        }

	        // 0 returned just to indicate the end of processing.
	        return 0;
	    }

	    /**
	     * Method call as a trigger to promote all the rules to the Release state if they already are not in that state.
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args - Holds the parameters passed from the calling method
	     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
	     * @throws Exception if the operation fails
	     * @since R212
	     * @author WKU
	     */
	     public int promoteRules(Context context, String args[]) throws Exception
	     {
	       //The product object id sent by the emxTriggerManager is retrieved here.
	       String objectId = args[0];
	       //The object id is set to the context
	       setId(objectId);
	       DomainObject domObject = new DomainObject(objectId);
	       String strComma = ",";
	       String strRelationshipPattern ="";
	       if(domObject.isKindOf(context, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))
	    	   strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE + strComma;
	       strRelationshipPattern = strRelationshipPattern + ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE + strComma

	       + ConfigurationConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION + strComma
	       + ConfigurationConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
	       //Object where condition to retrieve the objects that are not already in Release state.
	       String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " != \""+ConfigurationConstants.STATE_RELEASE+"\")";
	       //Type to fetched is all types returned by the relationship.
	       String strTypePattern = ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE + strComma
	       + ConfigurationConstants.TYPE_QUANTITY_RULE + strComma
	       + ConfigurationConstants.TYPE_RULE_EXTENSION + strComma
	       + ConfigurationConstants.TYPE_FIXED_RESOURCE;
	       //ObjectSelects retreives the parameters of the objects that are to be retreived.
	       StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
	       ConfigurationUtil util = new ConfigurationUtil(objectId);

	       //The objects connected to the product based on the relationships defined are obtained.
	       StringList tempStringList = new StringList();
	       //Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
	       MapList relBusObjList = util.getObjectStructure(context, strTypePattern, strRelationshipPattern,
					objectSelects, tempStringList , true, true, 0, 0, objectWhere,
					   DomainConstants.EMPTY_STRING, (short)0, DomainConstants.EMPTY_STRING);
	       //The number of objects connected is obtained.
	       int iNumberOfObjects = relBusObjList.size();
	       //The promotion is to happen only if there are any connected objects
	       if (iNumberOfObjects > 0)
	       {
	         //Processing for each of the object connected to the product
	         for (int i = 0;i < iNumberOfObjects ; i++)
	         {
	           //Each id of the object connected to product is obtained.
	           String strTempObjectId = (String)((Map)relBusObjList.get(i)).get(DomainConstants.SELECT_ID);
	           //The context is set with the object id obtained.
	           setId(strTempObjectId);
	           //The state of the context object is set to release.
	           setState(context,ConfigurationConstants.STATE_RELEASE);
	         }
	       }
	       //0 returned just to indicate the end of processing.
	       return 0;
	     }

	     /**
		     * Method call as a trigger to demote all the rules to the PRELIMINARY state if they already are not in that state.
		     *
		     * @param context the eMatrix <code>Context</code> object
		     * @param args - Holds the parameters passed from the calling method
		     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
		     * @throws Exception if the operation fails
		     * @since R212
		     * @author WKU
		     */
		     public int demoteRules(Context context, String args[]) throws Exception
		     {
		       //The product object id sent by the emxTriggerManager is retrieved here.
		       String objectId = args[0];
		       //The object id is set to the context
		       setId(objectId);
		       DomainObject domObject = new DomainObject(objectId);
		       String strComma = ",";
		       String strRelationshipPattern ="";
		       if(domObject.isKindOf(context, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))
		    	   strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE + strComma;
		       strRelationshipPattern = strRelationshipPattern + ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE + strComma

		       + ConfigurationConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION + strComma
		       + ConfigurationConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
		       //Object where condition to retrieve the objects that are not already in Release state.
		       String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " == \""+ConfigurationConstants.STATE_RELEASE+"\")";
		       //Type to fetched is all types returned by the relationship.
		       String strTypePattern = ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE + strComma
		       + ConfigurationConstants.TYPE_QUANTITY_RULE + strComma
		       + ConfigurationConstants.TYPE_RULE_EXTENSION + strComma
		       + ConfigurationConstants.TYPE_FIXED_RESOURCE;
		       //ObjectSelects retreives the parameters of the objects that are to be retreived.
		       StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		       ConfigurationUtil util = new ConfigurationUtil(objectId);

		       StringList tempStringList = new StringList();
		       //The objects connected to the product based on the relationships defined are obtained.
		       //Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
		       MapList relBusObjList = util.getObjectStructure(context, strTypePattern, strRelationshipPattern,
						objectSelects, tempStringList, true, true, 0, 0, objectWhere,
						   DomainConstants.EMPTY_STRING, (short)0, DomainConstants.EMPTY_STRING);
		       //The number of objects connected is obtained.
		       int iNumberOfObjects = relBusObjList.size();
		       //The promotion is to happen only if there are any connected objects
		       if (iNumberOfObjects > 0)
		       {
		         //Processing for each of the object connected to the product
		         for (int i = 0;i < iNumberOfObjects ; i++)
		         {
		           //Each id of the object connected to product is obtained.
		           String strTempObjectId = (String)((Map)relBusObjList.get(i)).get(DomainConstants.SELECT_ID);
		           //The context is set with the object id obtained.
		           setId(strTempObjectId);
		           //The state of the context object is set to release.
		           setState(context,ConfigurationConstants.STATE_PRELIMINARY);
		         }
		       }
		       //0 returned just to indicate the end of processing.
		       return 0;
		     }

		     /**
			     * Method call as a trigger to increase the subFeature count by 1 while creating relationship type CONFIGURATION STRUCTURES
			     *
			     * @param context the eMatrix <code>Context</code> object
			     * @param args - args[0] - holding newly created relationship id
			     * 				 args[1] - holding parent feature id
			     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
			     * @throws Exception if the operation fails
			     * @since R212
			     * @author A69
			     */
				public static int updateSubFeatureCountForAddition(Context context, String []args) throws FrameworkException{
					try{
						  String fromObjectID = args[1];

				          StringList selectables = new StringList();
				          selectables.add(ConfigurationConstants.SELECT_TYPE);
				          selectables.add(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT );

				          DomainObject fromObject = new DomainObject(fromObjectID);
				          Map selectableList = fromObject.getInfo(context,selectables);

				          String strType = (String)selectableList.get(ConfigurationConstants.SELECT_TYPE);
				          String subFeatureCount = "";

				          if(strType!=null && mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE)){
				        	  subFeatureCount = (String)selectableList.get(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT);
				        	  int count = Integer.parseInt(subFeatureCount);
				        	  count = count + 1;
				        	  fromObject.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT, String.valueOf(count));
				          }
					}
					catch(Exception e){
						throw new FrameworkException(e);
					}
			        return 0;
				}

				/**
			     * Method call as a trigger to decrease the subFeature count by 1 while deleting relationship type CONFIGURATION STRUCTURES
			     *
			     * @param context the eMatrix <code>Context</code> object
			     * @param args - args[0] - holding newly created relationship id
			     * 				 args[1] - holding parent feature id
			     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
			     * @throws Exception if the operation fails
			     * @since R212
			     * @author A69
			     */
					public static int updateSubFeatureCountForDeletion(Context context, String []args) throws FrameworkException{
						try{
							  String fromObjectID = args[1];

					          StringList selectables = new StringList();
					          selectables.add(ConfigurationConstants.SELECT_TYPE);
					          selectables.add(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT);

					          DomainObject fromObject = new DomainObject(fromObjectID);
					          Map selectableList = fromObject.getInfo(context,selectables);

					          String strType = (String)selectableList.get(ConfigurationConstants.SELECT_TYPE);
					          String subFeatureCount = "";

					          if(strType!=null && mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE)){
					        	  subFeatureCount = (String)selectableList.get(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT);
					        	  int count = Integer.parseInt(subFeatureCount);
					        	  count = count - 1;
					        	  fromObject.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT, String.valueOf(count));
					          }
						}
						catch(Exception e){
							throw new FrameworkException(e);
						}
				        return 0;
					}
           /**
		 	 * Label program for  Tree structure
		 	 * @param context
		 	 * @param args
		 	 * @return
		 	 * @throws Exception
		      * @since R212
		      */
		     public String getDisplayNameForNavigator(Context context,String[] args) throws Exception
		     {	 		
			 		String strTreeName = ConfigurationUtil.getDisplayNameForFeatureNavigator(context, args);

			 		return strTreeName;
			 }
/**
				 	 * It is a check trigger method. Fires when a relationship Configuration Features is getting created
				 	 * It is used to check the cyclic condition
				 	 * @param context
				 	 *            The ematrix context object.
				 	 * @param String[]
				 	 *            The args .
				 	 * @return zero when cyclic condition is false, throws exception if it is true
				 	 * @author A69
				 	 * @since R212
				 	 */
				 	public int multiLevelRecursionCheck(Context context, String[] args)throws FrameworkException
				 	{
				 		 String strRemovedLFId = PropertyUtil.getGlobalRPEValue(context,"CyclicCheckRequired");
				 		 int iResult = 0;
				 		 if(strRemovedLFId==null || strRemovedLFId.length()==0 || strRemovedLFId.equalsIgnoreCase("")) {
					    	 String fromObjectId = args[0];
					    	 String toObjectId = args[1];
					    	 String relType = args[2];
					    	 try {
					    		 boolean recursionCheck =  ConfigurationUtil.multiLevelRecursionCheck(context,fromObjectId,toObjectId,relType);
					    		 if (recursionCheck) {
			    					String strAlertMessage = EnoviaResourceBundle.getProperty(context,
			    							SUITE_KEY, 
			    							"emxConfiguration.Add.CyclicCheck.Error",context.getSession().getLanguage());
			    					throw new FrameworkException(strAlertMessage);
					    		 } else {
					    			 iResult= 0;
					    		 }
					    	 }catch (Exception e) {
					    		 iResult=1;
					    	 }
				 		 }
				 		 else if(strRemovedLFId.equalsIgnoreCase("False")){
				 			iResult=0;
				 		 }
				 		 return iResult;
				 	}


				 	 /**
				 	 * It is a trigger method. Fires when a "Configuration Feature" promoted  from review state to release state.
				 	 * If the promoted CF have previous revision than it will replace with the latest revision in context which are not in Release state.
				 	 *
				 	 * @param context
				 	 *            The ematrix context object.
				 	 * @param String[]
				 	 *            The args .
				 	 * @return
				 	 * @author A69
				 	 * @since FTR R212
				 	 */
				 	public void configurationFeatureFloatOnRelease(Context context, String[] args)throws FrameworkException
				 	{
				 		try{
				 			//Configuration Feature ID
					 		String configurationFeatureID = args[0];
					 		//Domain object of Configuration Feature
					 		DomainObject domObject = new DomainObject(configurationFeatureID);

							//Getting previous revision businessObject
					 		BusinessObject boPreviousRevision= domObject.getPreviousRevision(context);
					 		if(boPreviousRevision.exists(context))
					 		{
					 			boolean bApplicabilityApplies = false;
					 			//Getting previous revision ID
						 		String strPreviousRevID = boPreviousRevision.getObjectId(context);

						 		//Get the applicable item
						 		String RELATIONSHIP_APPLICABLE_ITEM = PropertyUtil.getSchemaProperty(context,"relationship_ApplicableItem");
				 				String strApplicableItem = "to["
				 					+ ProductLineConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM + "].from.from["
				 					+ RELATIONSHIP_APPLICABLE_ITEM + "].to."
				 					+ DomainConstants.SELECT_ID ;

				 				DomainConstants.MULTI_VALUE_LIST.add(strApplicableItem);
				 				StringList applicableItemsList = domObject.getInfoList(context, strApplicableItem);
				 				DomainConstants.MULTI_VALUE_LIST.remove(strApplicableItem);
				 				if (applicableItemsList != null && !"null".equals(applicableItemsList) && applicableItemsList.size() > 0)
				 					bApplicabilityApplies = true;

						 		//Type Pattern
								String strTypePattern = DomainConstants.QUERY_WILDCARD;
								//Rel PAttern
								String strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+","+
														ConfigurationConstants.RELATIONSHIP_VARIES_BY+","+
														ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY;

								//where expression
								String strRelease = FrameworkUtil.lookupStateName(context,
					                    ProductLineConstants.POLICY_PRODUCT, strSymbReleaseState);
								StringBuffer sbBuffer = new StringBuffer();
								sbBuffer.append(DomainConstants.SELECT_CURRENT);
					            sbBuffer.append(SYMB_NOT_EQUAL);
					            sbBuffer.append(SYMB_QUOTE);
					            sbBuffer.append(strRelease);
					            sbBuffer.append(SYMB_QUOTE);

					            String strBusWhereClause = sbBuffer.toString();

								ConfigurationUtil util = new ConfigurationUtil(strPreviousRevID);
								//Traversing to one level Up

								StringList objSelect = new StringList(ConfigurationConstants.SELECT_ID);
								objSelect.add(ConfigurationConstants.SELECT_TYPE);
								StringList relSelect = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);

								MapList configurationFeaturesRelIDListUp = util.getObjectStructure(context,strTypePattern,strRelPattern,
										objSelect, relSelect, true, false, 1, 0, strBusWhereClause,
										DomainConstants.EMPTY_STRING,(short) 1,DomainConstants.EMPTY_STRING);


								for(int i=0;i<configurationFeaturesRelIDListUp.size();i++)
								{
									Map configurationFeaturesRelIDMapUp = (Map)configurationFeaturesRelIDListUp.get(i);
									String strRelID = (String)configurationFeaturesRelIDMapUp.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
									String strParentObjId = (String)configurationFeaturesRelIDMapUp.get(ConfigurationConstants.SELECT_ID);
									String strParentObjType = (String)configurationFeaturesRelIDMapUp.get(ConfigurationConstants.SELECT_TYPE);

									if ((bApplicabilityApplies && !applicableItemsList.contains(strParentObjId))
											|| mxType.isOfParentType(context, strParentObjType, ConfigurationConstants.TYPE_PRODUCT_LINE)
											|| mxType.isOfParentType(context, strParentObjType, ConfigurationConstants.TYPE_MODEL))
		                            {
		                                continue; //skip this one
		                            }
									else if(strRelID!=null){
										//Changing the to side object, Previsous Revision to Latest Revision
										DomainRelationship.setToObject(context, strRelID,domObject);
									}
								}
					 		}
				 		}
				 		catch (Exception e) {
				 			throw new FrameworkException(e.toString());
						}
				 	}

				 	/**
				     * This method is used to restrict from displaying the row
				     * in Configuration Feature Property page
				     * if Library Central is not installed
				     *
				     * @param context the eMatrix <code>Context</code> object
				     * @param args:
				     *          args for the method
				     *
				     * @throws Exception if the operation fails
				     * @return boolean
				     * @since R212
				     */
				    public boolean showClassificationPath(Context context,String[] args) throws Exception
				    {
				        boolean isLCInstalled = ConfigurationUtil.isLCInstalled(context);

				        return isLCInstalled;
				    }
				/**
				 * This is a trigger method called when the Varied By relationship is deleted,
				 * this method handle the delete check at the Product context.
				 *
				 * @param context
				 * @param args
				 * @return If inherited return 0
				 * @throws FrameworkException
				 * @since R212
				 * @author IVU
				 */
				public static int canRemoveDesignVariant(Context context, String []args) throws FrameworkException{
					try{
						String strDVRemoveContext = PropertyUtil.getGlobalRPEValue(context,"RemoveDVContext");
						if(strDVRemoveContext==null || strDVRemoveContext.equals("")){
							  String strToType = args[4];
							  if(mxType.isOfParentType(context,strToType,ConfigurationConstants.TYPE_PRODUCTS)){
									String strLanguage = context.getSession().getLanguage();
							    	String strSubjectKey = EnoviaResourceBundle.getProperty(context,
							    			SUITE_KEY, "emxConfiguration.Alert.CanNotRemoveDeleteVariesBy",strLanguage);
							    	throw new FrameworkException(strSubjectKey);
							  }
						}
					}
					catch(Exception e){
						throw new FrameworkException(e);
					}
					return 0;
				}

				/**
			     * Method call as a trigger to update the context structure for Mandatory Configuration Feature removal/deletion
			     *
			     * @param context the eMatrix <code>Context</code> object
			     * @param args - args[0] - From side id of Mandatory Configuration Features relationship
			     * 				 args[1] - To side id of Mandatory Configuration Features relationship
			     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
			     * @throws Exception if the operation fails
			     * @since R212
			     * @author A69
			     */
				public static int updateStructureForMandatoryFeatureDeletion(Context context, String []args) throws FrameworkException{
					try{
						  String fromObjectID = args[0];
						  String toObjectID = args[1];

						  StringList toObjList = new StringList(toObjectID);
						  ConfigurationFeature.removeMandatoryConfigurationFeature(context, fromObjectID, toObjList);
					}
					catch(FrameworkException e){
						throw new FrameworkException(e);
					}
			        return 0;
				}

				/**
			     * Method call as a trigger to update the structure of added Object for all the Mandatory Configuration Feature
			     * connected to context
			     *
			     * @param context the eMatrix <code>Context</code> object
			     * @param args - args[0] - From side id of Mandatory Configuration Features relationship
			     * 				 args[1] - To side id of Mandatory Configuration Features relationship
			     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
			     * @throws Exception if the operation fails
			     * @since R212
			     * @author A69
			     */
				public static int inheritMandatoryConfigurationFeatures(Context context, String []args) throws FrameworkException{

					try{
						  StringList toObjList = new StringList();
						  String fromObjectID = args[0];
						  String fromType = args[1];
						  String toType = args[2];
						  
						  //skip processing in case of product version
						  if(mxType.isOfParentType(context,fromType,ConfigurationConstants.TYPE_PRODUCTS) &&
								  !mxType.isOfParentType(context,toType,ConfigurationConstants.TYPE_PRODUCT_VARIANT)){
							  return 0;
						  }
						  else{							  
							  String strTypePattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURE;
							  String strRelPattern = ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES+","+ConfigurationConstants.RELATIONSHIP_VARIES_BY;

							  StringList objSelect =  new StringList(DomainObject.SELECT_ID);

							  StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_TYPE);
							  relSelects.add("attribute["+ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE+"].value");

							  ConfigurationUtil confUtil = new ConfigurationUtil(fromObjectID);

							  MapList mapConfigurationStructure = confUtil.getObjectStructure(context, strTypePattern,
										strRelPattern, objSelect, relSelects, false,true, 1, 0, DomainObject.EMPTY_STRING ,
										DomainObject.EMPTY_STRING,	DomainObject.FILTER_STR_AND_ITEM, DomainObject.EMPTY_STRING);

							  for(int count=0; count < mapConfigurationStructure.size(); count++){
								  if(mapConfigurationStructure!=null && mapConfigurationStructure.size()>0){
									  String relType = (String)((Map)mapConfigurationStructure.get(count)).get(DomainConstants.SELECT_RELATIONSHIP_TYPE);
									  String configurationType = (String)((Map)mapConfigurationStructure.get(count)).get("attribute["+ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE+"].value");
									  String confFtrID = (String)((Map)mapConfigurationStructure.get(count)).get(DomainObject.SELECT_ID);

									  	if(relType != null && (relType.equals(ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES)|| (relType.equals(ConfigurationConstants.RELATIONSHIP_VARIES_BY) && ConfigurationConstants.RANGE_VALUE_MANDATORY.equals(configurationType)))){
									  		toObjList.add(confFtrID);
								    	}
								  }
							  }

							  if(toObjList.size() > 0){
								  ConfigurationFeature.makeMandatoryConfigurationFeature(context, fromObjectID, toObjList);
							  }
						  }						  
					}
					catch(Exception e){
						throw new FrameworkException(e);
					}
			        return 0;
				}

				/**
			     * Method call as a trigger to flag all Active Design variant Inactive if context Configuration Feature is getting used
			     * This will get fire when Configuration Feature will be promoted to Obsolete state
			     *
			     * @param context
			     *            the eMatrix <code>Context</code> object
			     * @param args -
			     *            Holds the parameters passed from the calling method
			     * @return int - Returns 0 in case of Check trigger is success and 1 in case
			     *         of failure
			     * @throws Exception
			     *             if operation fails
			     * @since R212
			     * @author A69
			     */
			    public int InactiveDesignVariants(Context context, String args[]) throws Exception {
			        // The Parent Feature object id sent by the emxTriggerManager
			        String objectId = args[0];

		        	String strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_VARIES_BY;

			        String strTypePattern = ConfigurationConstants.TYPE_PRODUCTS+","+ ConfigurationConstants.TYPE_LOGICAL_FEATURE;

			        StringList objectSelects = new StringList(ConfigurationConstants.SELECT_ID);
			        StringList relSelects = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);

			        ConfigurationFeature configurationFeature = new  ConfigurationFeature(objectId);

			        MapList relBusObjList = configurationFeature.getConfigurationFeatureStructure(context, strTypePattern,
			        		strRelationshipPattern, objectSelects, relSelects, true, false, 1, 0, DomainConstants.EMPTY_STRING,
			        		DomainConstants.EMPTY_STRING, (short)1, DomainConstants.EMPTY_STRING);

			        if(relBusObjList!=null){
			        	int iNumberOfObjects = relBusObjList.size();

			        	if (iNumberOfObjects > 0) {
			        		//Push context as no changeType access to Product Manager in release state in Configuration Feature policy.
			        		ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),"","");

				            for (int i = 0; i < iNumberOfObjects; i++) {

				                String strRelId = (String) ((Hashtable) relBusObjList
				                        .get(i)).get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);

				                DomainRelationship.setType(context, strRelId, ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY);
				            }
				            ContextUtil.popContext(context);
				        }
			        }

			        return 0;
			    }
				   
				   /**
					  * It is used in product version/copy to copy ConfigurationFeature to the newly created product
					  * @param context
					  * @param args
					  * @return Map containing old created Configuration Feature rel id as key and pipe separated 
					  * old rel physical id, new rel physical id, new rel id as value
					  * @throws FrameworkException
					  * @exclude
					  */
			    public Map copyConfigurationFeatureStructure(Context context, String[] args) 
				throws FrameworkException
				{
			    	Map clonedObjIDMap = null;
			    	
					try{
						ArrayList programMap = (ArrayList)JPO.unpackArgs(args);
			            
			            String sourceObjectId = (String) programMap.get(0);
			            String destinationObjectId = (String) programMap.get(1);
			            
						clonedObjIDMap = ConfigurationFeature.copyConfigurationFeatureStructure(context, sourceObjectId, destinationObjectId);
					}
					catch(Exception e){
						throw new FrameworkException(e);
					}	
					return clonedObjIDMap;
				}
			    /**
			     * Method call as a update function setting of column, to check if the changed object has Configuration Feature
			     * type subfeatures and blocked the update even if check failed, else update Key-In Type attribute.
			     *
			     * @param context
			     *            the eMatrix <code>Context</code> object
			     * @param args -
			     *            Holds the parameters passed from the calling method
			     * @return int - Returns 0 in case of Check trigger is success and 1 in case
			     *         of failure
			     * @throws Exception
			     *             if operation fails
			     * @since R212
			     * @author A69
			     */
			    public static void updateKeyInType(Context context, String []args) throws FrameworkException{
					try{
						  String strLanguage = context.getSession().getLanguage();
				    	  String strSubjectKey = EnoviaResourceBundle.getProperty(context,
									    			SUITE_KEY, "emxConfiguration.Feature.Modify.KeyInTypeCheck",strLanguage);

				    	  HashMap programMap = (HashMap) JPO.unpackArgs(args);
				    	  HashMap paramMap = (HashMap) programMap.get("paramMap");
				    	  String strObjectId = (String) paramMap.get("objectId");
				    	  String strNewAttributeValue = (String) paramMap.get("New Value");

						  DomainObject dom = new DomainObject(strObjectId);

						  String strSelectable = "from["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].to.id";
						  StringList selectables = new StringList(strSelectable);

						  DomainConstants.MULTI_VALUE_LIST.add(strSelectable);

						  Map selectableMap = dom.getInfo(context, selectables);

						  DomainConstants.MULTI_VALUE_LIST.remove(strSelectable);

						  StringList slUsage = ConfigurationUtil.convertObjToStringList(context,selectableMap.get(strSelectable));

						  if(!strNewAttributeValue.equals(ConfigurationConstants.RANGE_VALUE_BLANK) && slUsage != null && slUsage.size() > 0){
							  throw new FrameworkException(strSubjectKey);
						  }
						  else{
							  dom.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE, strNewAttributeValue);
						  }
					}
					catch(Exception e){
						throw new FrameworkException(e.getMessage());
					}
				}
			    
			    /**
				 * Method which does not do anything, But require as Update Program for revision field in Create Page of Logical Feature
				 * @param context
				 * @param args
				 * @throws FrameworkException
				 */
				public void emptyProgram(Context context, String []args) throws FrameworkException{
					
				}
				
				/**
			     * Method returns the First Sequence revision for the policy Configuration Feature.
			     * It is called for the create page of Configuration Feature
			     * @param context the eMatrix <code>Context</code> object
			     * @param args - Holds parameters passed from the calling method
			     * @return - returns the First Sequence revision
			     * @throws Exception if the operation fails
			     */
			    public String getDefaultRevision (Context context,String[] args)throws Exception
			    {
			    	//TODO -Policy in combobox is not in order in which getPolicies return
					String strType=ConfigurationConstants.TYPE_CONFIGURATION_FEATURE;
					MapList policyList = com.matrixone.apps.domain.util.mxType.getPolicies(context,strType,false);
					String strDefaultPolicy = (String)((HashMap)policyList.get(0)).get(DomainConstants.SELECT_NAME);
					
			    	Policy policyObject = new Policy(strDefaultPolicy);
			    	String strRevision = policyObject.getFirstInSequence(context);	        
					return strRevision;
			    }

    /**
     * This method is used to retrieve the Context Product Line.
     *
     * @param context
     *            The ematrix context object.
     * @param String[]
     *            The args .
     * @return Vector
     * @exclude
     */

    public Vector getContextProductLine(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        MapList lstObjectIdsList = (MapList) programMap.get("objectList");
        HashMap paramList = (HashMap)programMap.get("paramList");
        Vector contextPL = new Vector();
        String strParentId = DomainConstants.EMPTY_STRING;
        DomainObject domObjProdRev = null;
        StringBuffer sIncLink = new StringBuffer(260);
        StringBuffer sb = new StringBuffer();
        sIncLink = sIncLink
                .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert&amp;emxSuiteDirectory=configuration&amp;treeMenu=type_PLCProductLine&amp;jsTreeID=");

        String strEndPart1 = "', '690', '640', 'true', 'popup')\">";
        String strEndPart2 = "</a>";
        String strJsTreeID = (String) programMap.get("jsTreeID");
        sIncLink = sIncLink.append(strJsTreeID);
        for (int i = 0; i < lstObjectIdsList.size(); i++) {
            sb = new StringBuffer(260);
            sb = sb.append(sIncLink.toString());
            strParentId = (String) ((Map) lstObjectIdsList.get(i))
                    .get("id[parent]");
            StringTokenizer objIDs = new StringTokenizer(strParentId, ",");
            String featureID = "";
            String prodID = "";
            //if loop added by ixe
            if(objIDs.hasMoreTokens()){
            	featureID = objIDs.nextToken().trim();
            }
            if(objIDs.hasMoreTokens()){
            	prodID = objIDs.nextToken().trim();
            }else{
            	DomainObject domObj = new DomainObject(featureID);
            	if(!domObj.isKindOf(context, ConfigurationConstants.TYPE_HARDWARE_PRODUCT)){
            		prodID = (String)paramList.get("parentOID");
            	}else{
            		prodID = featureID;
            	}
            }
            DomainObject domObjProd = new DomainObject(prodID);
            String strProductRev = domObjProd.getInfo(context, "to["
                    + ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION
                    + "].from.id");
            if (strProductRev != null && !(("").equals(strProductRev))
                    && !(("null").equals(strProductRev)))
                domObjProdRev = new DomainObject(strProductRev);
            else
                domObjProdRev = domObjProd;
            
			 final String SELECT_PARENT_PRODUCT = ("to["+ ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
			 final String SELECT_PARENT_MAIN_PRODUCT = ("to["+ ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");
			 String strParentProductId="";
			 Map  mapContext = domObjProdRev.getInfo(context, new StringList(SELECT_PARENT_PRODUCT));
			 if(mapContext.containsKey(SELECT_PARENT_PRODUCT))
				 strParentProductId = (String)mapContext.get(SELECT_PARENT_PRODUCT);
			 else if(mapContext.containsKey(SELECT_PARENT_MAIN_PRODUCT))
				 strParentProductId = (String)mapContext.get(SELECT_PARENT_MAIN_PRODUCT);
            DomainObject domObjModel = new DomainObject(strParentProductId);

            String strPLId = domObjModel.getInfo(context, "to["
                    + ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS
                    + "].from.id");

            if (strPLId != null && !(("").equals(strPLId))
                    && !(("null").equals(strPLId))) {
                String strPL = new DomainObject(strPLId).getInfo(context,
                        DomainConstants.SELECT_NAME);
                sb = sb.append("&amp;objectId=").append(strPLId);
                sb = sb.append(strEndPart1).append(XSSUtil.encodeForHTMLAttribute(context,strPL)).append(strEndPart2);
                contextPL.add(sb.toString());
            } else
                contextPL.add(DomainConstants.EMPTY_STRING);

        }
        return contextPL;
    }
/**
 * It is a trigger method. Fires when a "Configuration Feature" promoted  from review state to release state.
 * If the promoted CF have previous revision than it will replace with the latest revision in context which are not in Release state.
 *  @param context           The ematrix context object.
 * @param String[]           The args .
 * @return
 * @author GN1
 * @since FTR R212				 	
 */
	public void  connectREToConfigurationFeature(Context context, String[] args)
	throws Exception {
		// configuration feature Id
		String ObjectId = args[0];
		DomainObject domObject = new DomainObject(ObjectId);
		BusinessObject boPreviousRevision = domObject
				.getPreviousRevision(context);
		// Getting previous revision ID
		String strPreviousRevID = boPreviousRevision.getObjectId(context);

		if (boPreviousRevision.exists(context)) {

			DomainObject prevObj = new DomainObject(strPreviousRevID);
			String strNewId = domObject.getInfo(context,
					ConfigurationConstants.SELECT_PHYSICAL_ID);
			String strOldId = prevObj.getInfo(context,
					ConfigurationConstants.SELECT_PHYSICAL_ID);
			StringList selectables = new StringList();
			selectables.add(DomainObject.SELECT_ID);
			selectables
					.add(ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION);

			// IR connected with Object
			MapList irList = prevObj.getRelatedObjects(context,
					ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION,
					ConfigurationConstants.TYPE_INCLUSION_RULE, selectables,
					new StringList(DomainRelationship.SELECT_ID), true, false,
					(short) 1, DomainConstants.EMPTY_STRING,
					DomainConstants.EMPTY_STRING, 1);

			if (irList.size() != 0) {
				for (int i = 0; i < irList.size(); i++) {
					Map connectIdMap = (Map) irList.get(i);
					StringBuffer sb_relid = new StringBuffer();
					sb_relid.append(SELECT_RELATIONSHIP_ID);
					String strrelId = sb_relid.toString();
					StringBuffer sb_id = new StringBuffer();
					sb_id.append(SELECT_ID);
					String strId = sb_id.toString();
					StringBuffer sb_attr = new StringBuffer();
					sb_attr
							.append(ConfigurationConstants.SELECT_ATTRIBUTE_RIGHT_EXPRESSION);
					String strAttr = sb_attr.toString();
					String rightexpression = (String) connectIdMap.get(strAttr);

					StringList newRElID = ConfigurationUtil
							.convertObjToStringList(context,
									(Object) connectIdMap.get(strrelId));
					for (int j = 0; j < newRElID.size(); j++) {
						String relIdconnect = (String) newRElID.get(j);
						DomainRelationship.setToObject(context, relIdconnect,
								domObject);
					}
					StringList newRuleID = ConfigurationUtil
							.convertObjToStringList(context,
									(Object) connectIdMap.get(strId));
					for (int j = 0; j < newRuleID.size(); j++) {
						DomainObject dom = new DomainObject((String) newRuleID
								.get(j));

						String finalRightexpression = rightexpression.replace(
								"B" + strOldId, "B" + strNewId);
						dom
								.setAttributeValue(
										context,
										ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION,
										finalRightexpression);
					}
				}
			}
//			StringBuffer sb = new StringBuffer();
//			sb.append("tomid[");
//			sb.append(ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);
//			sb.append("].from.id");
//			String coIR = sb.toString();
			StringBuffer sb1 = new StringBuffer();
			sb1.append("tomid[");
			sb1.append(ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);
			sb1.append("].from["+ConfigurationConstants.TYPE_INCLUSION_RULE+"].id");
			String cfIR = sb1.toString();

			StringList objSel = new StringList();
			objSel.addElement(DomainObject.SELECT_ID);
			StringList relSel = new StringList();
//			relSel.add(coIR);
			relSel.add(cfIR);
			// when Ir connected with Relationship
			MapList newList = prevObj
					.getRelatedObjects(
							context,
							ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES,
							ConfigurationConstants.TYPE_CONFIGURATION_FEATURES,
							objSel, relSel, false, true, (short) 0,
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING, 1);
			if (newList.size() != 0) {
				for (int i = 0; i < newList.size(); i++) {
					Map connectIdMap = (Map) newList.get(i);
					StringList resultruleIds = new StringList();
					StringList resultruleIds_1 = new StringList();

//					resultruleIds = ConfigurationUtil.convertObjToStringList(
//							context, (Object) connectIdMap.get(coIR));
					resultruleIds_1 = ConfigurationUtil.convertObjToStringList(
							context, (Object) connectIdMap.get(cfIR));

					//resultruleIds.addAll(resultruleIds_1);
					if (resultruleIds.size() != 0) {
						for (int j = 0; j < resultruleIds.size(); j++) {
							DomainObject dom = new DomainObject(
									(String) resultruleIds.get(j));
							dom
									.setAttributeValue(
											context,
											ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY,
											ConfigurationConstants.RANGE_VALUE_COMPLEX);

						}
					}
				}

			}

		}

	}
	
	/**
	 * This method is used to give access at column level for Configuration Features and Options under Configuration features in CF structure browser
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.
	 * @since Feature Configuration X+5
	 * @throws Exception
	 */
	public static StringList isConfigurationObjectEditable (Context context, String[] args) throws FrameworkException{
	    try{
	        HashMap inputMap = (HashMap)JPO.unpackArgs(args);
	        MapList objectMap = (MapList) inputMap.get("objectList");

	        String objectType = "";
	        Map requestMap = (Map) inputMap.get("requestMap");
	        String dataStatus =(String) requestMap.get("dataStatus");
	      
	        StringList returnStringList = new StringList (objectMap.size());
			String [] strCFIDs = new String[objectMap.size()];
	    	for(int i=0;i<objectMap.size();i++)
	    	{
	    		Map objectMap1 = (Map) objectMap.get(i);
	    		String strCFID = (String)objectMap1.get(DomainConstants.SELECT_ID);
	    		strCFIDs[i] = strCFID;
	    	}
			StringList selectList = new StringList(DomainConstants.SELECT_TYPE);
			MapList mpCFParentDetails = new MapList();
			if("pending".equalsIgnoreCase(dataStatus)){
				mpCFParentDetails = DomainObject.getInfo(context, strCFIDs, selectList);
			}
	    	
	        StringList cfSubTypes = ProductLineUtil.getChildTypesIncludingSelf(context, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE);
	        
	        for (int i = 0; i < objectMap.size(); i++) {
	            String strfeatureId = (String) ((Map) objectMap.get(i)).get("id");
	            String strfeatureType = (String) ((Map) objectMap.get(i)).get("type");
	            
	            if("pending".equalsIgnoreCase(dataStatus))
		        {
	            	//DomainObject domObject = new DomainObject(strfeatureId);
		            //objectType = domObject.getInfo(context, DomainConstants.SELECT_TYPE);
 	            	objectType = (String)((Map)mpCFParentDetails.get(i)).get(DomainConstants.SELECT_TYPE);
		        }else{
		        	objectType = strfeatureType;
		        }            
	            // Code commented for IR-187958V6R2014	            
	            //if ("Configuration Feature".equalsIgnoreCase(objectType))
	            if(objectType!=null &&  cfSubTypes.contains(objectType))
	            {
	            	returnStringList.add("true");
	           	}else {
	           		returnStringList.add("false");
	           	}
	        }
	        return returnStringList;
	    }catch(Exception e) {
	        e.printStackTrace();
	        throw new FrameworkException(e.getMessage());
	    }
	}
	
	/**
	 * This method is used to give access at column level for Configuration Features and Options under Configuration features in CF structure browser
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return StringList.	 
	 * @throws Exception
	 */
	public static StringList getSelectionCriteria (Context context, String[] args) throws FrameworkException{
		try{
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String strSelectionCriteria = "";
			String strLanguage = context.getSession().getLanguage();
		    String strSelectionCriteriaMay=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Configuration_Selection_Criteria.May",strLanguage)).trim();
		    String strSelectionCriteriaMust=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Configuration_Selection_Criteria.Must",strLanguage)).trim();
		    //String strSelectionCriteriaMust=(i18nNow.getI18nString("emxFramework.Range.Configuration_Selection_Criteria.Must","emxFrameworkStringResource", strLanguage)).trim();
			
			StringList returnStringList = new StringList (objectMap.size());
			
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map)objectMap.get(i);
				//strSelectionCriteria =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA);
				String strI18Value ="";
				if (outerMap
						.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_TYPE)
						&& ProductLineCommon
						.isNotNull((String) outerMap
								.get(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_TYPE))){
					strSelectionCriteria =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA);
				}else if (outerMap
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
						.isNotNull((String) outerMap
								.get(DomainRelationship.SELECT_ID))){
					DomainRelationship domRel = new DomainRelationship((String) outerMap
							.get(DomainRelationship.SELECT_ID));
					strSelectionCriteria=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA);
				} 
					if (strSelectionCriteria!=null && strSelectionCriteria
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MUST)) {
						strI18Value = strSelectionCriteriaMust;

					} else if (strSelectionCriteria!=null &&strSelectionCriteria
							.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MAY)) {
						strI18Value = strSelectionCriteriaMay;
				}
					
				if(!("".equalsIgnoreCase(strI18Value) || "null".equalsIgnoreCase(strI18Value)|| strI18Value == null))
				{
					returnStringList.addElement(strI18Value);
				}else {
					returnStringList.addElement("");
				}
			}
			return returnStringList;
		}catch(Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	
	  /**
     * Returns the Range Values for "Selection Criteria" attribute.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception
     *             if the operation fails
     * 
     */
    public Map getRangeValuesForSelectionCriteria(Context context,String[] args) throws Exception 
    {
        String strAttributeName = ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA;
        HashMap rangeMap = new HashMap();
        matrix.db.AttributeType attribName = new matrix.db.AttributeType(
                strAttributeName);
        attribName.open(context);

        List attributeRange = attribName.getChoices();
        List attributeDisplayRange = i18nNow
                .getAttrRangeI18NStringList(
                        ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA,
                        (StringList) attributeRange, context.getSession()
                                .getLanguage());
        rangeMap.put(FIELD_CHOICES, attributeRange);
        rangeMap.put(FIELD_DISPLAY_CHOICES, attributeDisplayRange);
        return rangeMap;
    }
	
    /**
	 * This method is an Update Function on edit of Selection Criteria
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateSelectionCriteria(Context context, String[] args) throws Exception 
	{

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strRelId = (String) paramMap.get("relId");
			String strNewAttributeValue = (String) paramMap.get("New Value");
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA,
					strNewAttributeValue);
		} catch (FrameworkException e) {			
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}	
	}
	
	/**
	 * This method is used to return the value of Inheritance Type column,
	 * 		which depends upon the relationship with which feature is connected to with the context
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments
	 * @return List- the List of Strings containing Mandatory/Not Mandatory values.
	 * @throws FrameworkException
	 *
	 * @since R212
	 * @author A69
	 */
	public List getInheritanceTypeValue(Context context,String[] args) throws FrameworkException {

		Map tempMap;
	    matrix.util.List lstMandatory = new StringList();
	    //String dispInheritanceType = "";
	    String strLanguage = context.getSession().getLanguage();

		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList lstObjectIdsList = (MapList) programMap.get("objectList");
			String strMandatory = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxConfiguration.MandatoryFeatures.Mandatory",strLanguage);
			String strRolledUp = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Table.RolledUp",strLanguage);	
		    for (int j = 0; j < lstObjectIdsList.size(); j++) {
		    	String dispInheritanceType = "";
		        tempMap = (Map)lstObjectIdsList.get(j);
		        String strRelationship = (String)tempMap.get("relationship");
		        if(strRelationship==null || strRelationship.trim().isEmpty()){
			        if (tempMap
							.containsKey(DomainRelationship.SELECT_ID)
							&& ProductLineCommon
									.isNotNull((String) tempMap
											.get(DomainRelationship.SELECT_ID))){
			        	String strRelID=(String) tempMap
			        			.get(DomainRelationship.SELECT_ID);
			        	MapList mlRelInfo = DomainRelationship.getInfo(context, new String[]{strRelID}, new StringList(DomainRelationship.SELECT_NAME));
			        	Map mapReInfo = (Map)mlRelInfo.get(0);
			        	strRelationship=(String)mapReInfo.get(DomainRelationship.SELECT_NAME);
			        }
		       }
		        if(ProductLineCommon.isNotNull(strRelationship)) {
		        	if (strRelationship.equals(ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES)) {
		        		dispInheritanceType = strMandatory;
		        	}else if(strRelationship.equals(ConfigurationConstants.RELATIONSHIP_VARIES_BY)){		        	
		        		dispInheritanceType = strRolledUp;
		        	}
		        }else{
		        	dispInheritanceType = "";
		        }
		        lstMandatory.add(dispInheritanceType);
		    }
		} catch (Exception e) {
			throw new FrameworkException(e);
		}

	    return lstMandatory;
	}
	
	/**
     * This method is used to connect the derived Configuration Feature revisions to the Parent Feature.
     *
     * @param context :
     *              The eMatrix Context object
     * @param featureId :
     *              The source feature Object id
     * @exception Exception :
     *              Throws exception if operation fails
     * @since R213
     * @author A69
     */
    public void connectConfigurationFeatureOnRevise(Context context, String args[])
        throws FrameworkException {
        String featureId = args[0];
        DomainObject contextFeature = null;
        BusinessObject nextRevision = null;
        try {
            contextFeature = new DomainObject(featureId);
            nextRevision = contextFeature.getNextRevision(context);
            DomainRelationship.connect(context, new DomainObject(nextRevision),
                   ConfigurationConstants.RELATIONSHIP_DERIVED,
                   contextFeature);
        } catch (Exception e) {
			throw new FrameworkException(e);
		}
    }
    
    /**
     * Method call as a trigger to decrease the  count attribute of Common Group by 1 while deleting relationship type CONFIGURATION STRUCTURES
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - args[0] - holding relationship id to be deleted
     * 				 args[1] - holding parent feature id
     * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
     * @throws Exception if the operation fails
     */
	
	public static int updateCountForDeletion(Context context, String []args) throws FrameworkException{
		try{
			  String relID = args[0];
			  	  
		      StringList RelationshipSelect = new StringList(DomainConstants.SELECT_ID);
	          String strWhere = "torel.id=="+relID;
	       	 	
	          MapList mLCVRelId = ProductLineCommon.queryConnection(context,
									ConfigurationConstants.RELATIONSHIP_COMMON_VALUES,
									RelationshipSelect,
									strWhere);
	        	  
			  StringList RelationshipSelectForCG = new StringList(DomainConstants.SELECT_ID);
		      String strWhereForCG = "";
		      String strCVRelId ="";
		      
			  for(int i=0; i<mLCVRelId.size(); i++ )
			  {
				  		strCVRelId =(String)((Map)mLCVRelId.get(i)).get(ConfigurationConstants.SELECT_ID);
						strWhereForCG = "frommid.id=="+strCVRelId;
						
						MapList mLCGRelId = ProductLineCommon.queryConnection(context,
								ConfigurationConstants.RELATIONSHIP_COMMON_GROUP,
								RelationshipSelectForCG,
								strWhereForCG);
					
						strCVRelId = (String)((Map)mLCGRelId.get(i)).get(ConfigurationConstants.SELECT_ID);
						
						DomainRelationship dRel = new DomainRelationship(strCVRelId);
						String Count = dRel.getAttributeValue(context, strCVRelId,DomainConstants.ATTRIBUTE_COUNT);
						
						int iCount = Integer.parseInt(Count);
						iCount = iCount-1;

						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),"","");
						dRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_COUNT, Integer.toString(iCount));
						ContextUtil.popContext(context);
				}
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}
        return 0;
	}
	
	
	/**
	 * This is a Access Function to show the read only context Configuration Feature field on form, 
	 * while creating Configuration Option under a selected context
	 * 
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args Holds ParamMap having the key "context", value of which will determine if the columns need to be displayed or not
	 * @return bResult - true - if the context is Committed Features
	 * 					 false - if the Context is not Committed Features
	 * @throws FrameworkException  - throws Exception if any operation fails.
	 * @author A69
	 * @exclude
	 */
	public Object noShowContextChooser(Context context, String[] args)
    throws FrameworkException
    {	
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestValuesMap = (HashMap)programMap.get("requestMap");
			String ctx = null;
			if(requestValuesMap!=null){
				ctx = (String)requestValuesMap.get("UIContext");				
			}
			else{
				ctx = (String)programMap.get("UIContext");
			}
			
			if(ctx.equals("myDesk") || ctx.equals("context")){
				return Boolean.valueOf("true");
			}else{
				return Boolean.valueOf("false");
			}
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}
    }
	
	/**
	 * This is a Access Function to show the editable context Configuration Feature field on form, 
	 * while creating Configuration Option from Global Actions
	 * 
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args Holds ParamMap having the key "context", value of which will determine if the columns need to be displayed or not
	 * @return bResult - true - if the context is Committed Features
	 * 					 false - if the Context is not Committed Features
	 * @throws FrameworkException  - throws Exception if any operation fails.
	 * @author A69
	 * @exclude
	 */
	public Object showContextChooser(Context context, String[] args)
    throws FrameworkException
    {	
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestValuesMap = (HashMap)programMap.get("requestMap");
			String ctx = null;
			if(requestValuesMap!=null){
				ctx = (String)requestValuesMap.get("UIContext");				
			}
			else{
				ctx = (String)programMap.get("UIContext");
			}
			
			if(ctx.equals("GlobalActions") || ctx.equals("Classification")){
				return Boolean.valueOf("true");
			}else{
				return Boolean.valueOf("false");
			}
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}
    }


	/** Is a column program for showing the Parent Configuration Feature.
	 * 
	 * @param context
	 * @param args
	 * @return  Name of the Parent Configuration Feature.
	 * @throws FrameworkException
	 * @exclude
	 */
	public StringList parentConfigurationFeatureName(Context context, String []args) throws FrameworkException{

		StringList displayNameList = new StringList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMapList = (MapList)programMap.get("objectList");

			String [] strCFIDs = new String[objectMapList.size()];
	    	for(int i=0;i<objectMapList.size();i++)
	    	{
	    		Map objectMap = (Map) objectMapList.get(i);
	    		String strLogicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);
	    		strCFIDs[i] = strLogicalFeatureID;
	    	}
	    	
			StringList selectList = new StringList();
			DomainObject.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].from["+ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"].name");
			DomainObject.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS+"].from["+ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"].name");
			
			selectList.add("to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].from["+ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"].name");
			selectList.add("to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS+"].from["+ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"].name");
			
			MapList mpCFParentDetails = DomainObject.getInfo(context, strCFIDs, selectList);
			
			DomainObject.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].from["+ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"].name");
			DomainObject.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS+"].from["+ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"].name");
			
	    	for(int i=0;i<mpCFParentDetails.size();i++)
	    	{
	    		Map objectMap = (Map) mpCFParentDetails.get(i);
	    		StringList strParentCFNameofCO = ConfigurationUtil.convertObjToStringList(context,objectMap.get("to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].from["+ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"].name"));
	    		StringList strParentCFNameofCF = ConfigurationUtil.convertObjToStringList(context,objectMap.get("to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS+"].from["+ConfigurationConstants.TYPE_CONFIGURATION_FEATURE+"].name"));

	    		if(strParentCFNameofCO!=null && strParentCFNameofCO.size()>0){
	    			displayNameList.addElement(ConfigurationUtil.convertStringListToString(context, strParentCFNameofCO));
	    		}
	    		if(strParentCFNameofCF!=null && strParentCFNameofCF.size()>0){
	    			displayNameList.addElement(ConfigurationUtil.convertStringListToString(context, strParentCFNameofCF));
	    		}
	    		
	    		if(strParentCFNameofCF.size()==00 && strParentCFNameofCO.size()==0){
	    			displayNameList.addElement("");
	    		}
	    	}
		}
		catch (Exception e) {
			throw new FrameworkException(e);
		}
		return displayNameList;
	}
    /**
 	 * column  program for  Search Table for CopyTo/CopyFrom in Configuration Context
 	 * @param context
 	 * @param args
 	 * @return
 	 * @throws Exception
      * @since R214
      */
	public StringList displayNameForSearchTable(Context context, String[] args)
			throws Exception {
		try {
			StringList displayNameList = ConfigurationUtil
					.displayNameForSearchTable(context, args);
			// for IR-379084-3DEXPERIENCER2016x - hyperlink for global search
			// and hyperlink not for add existing/chooser

			StringList displayNameHyperlinkList = new StringList();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramList");
			String isParentIDAvailable = (String) paramMap.get("parentOID");

			if (!ProductLineCommon.isNotNull(isParentIDAvailable)) {
				String strName = "";
				String strRev = "";
				String strDisplayId = "";
				String suiteDir = (String) paramMap.get("SuiteDirectory");
				String suiteKey = (String) paramMap.get("suiteKey");
				MapList objectList = (MapList) programMap.get("objectList");
				int iNumOfObjects = objectList.size();
				Map tempMap = null;

				for (int iCnt = 0; iCnt < iNumOfObjects; iCnt++) {
					tempMap = (Map) objectList.get(iCnt);
					StringBuffer temp = new StringBuffer();
					strName = (String) displayNameList.get(iCnt);
					strRev = (String) tempMap.get("revision");
					strDisplayId = (String) tempMap
							.get(DomainConstants.SELECT_ID);
					temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
					temp.append(suiteDir);
					temp.append("&amp;suiteKey=");
					temp.append(suiteKey);
					temp.append("&amp;objectId=");
					temp.append(strDisplayId);
					temp.append("', '450', '300', 'true', 'content')\">");
					temp.append(XSSUtil.encodeForXML(context, strName));
					temp.append("</a>");
					displayNameHyperlinkList.add(temp.toString());
				}
				return displayNameHyperlinkList;
			} else {
				return displayNameList;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
     
		/**
	     * Method called as a trigger to set the Usage Effectivity date to date of promotion to Obsolete
	     * for Configuration Feature.
	     * @param context
	     *            the eMatrix <code>Context</code> object
	     * @param args -
	     *            Holds the parameters passed from the calling method
	     * @return int - Returns 0 in case of Check trigger is success and 1 in case
	     *         of failure
	     * @throws Exception
	     *             if operation fails
	     * @since R214
	     * @author XOG
	     */
	    public int updateUsageEffectivityOnPromote(Context context, String args[]) throws Exception {
	        // The Parent Feature object id sent by the emxTriggerManager
	    String objectId = args[0];
	    EffectivityFramework EFF = new EffectivityFramework();
		String strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES;
		StringBuffer validEffectivityExpression = new StringBuffer();

		StringBuffer sbTypePattern = new StringBuffer(50); 
		sbTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
		sbTypePattern.append(",");
		sbTypePattern.append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE);
		sbTypePattern.append(",");
		sbTypePattern.append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);
		
		
		StringList objectSelects = new StringList(ConfigurationConstants.SELECT_ID);
		StringList relSelects = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);

		ConfigurationFeature configurationFeature = new ConfigurationFeature(
				objectId);

		MapList relBusObjList = configurationFeature
				.getConfigurationFeatureStructure(context, sbTypePattern.toString(),
						strRelationshipPattern, objectSelects, relSelects,
						true, false, 1, 0, DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING, (short) 1,
						DomainConstants.EMPTY_STRING);

		if (relBusObjList != null) {
			int iNumberOfObjects = relBusObjList.size();
			if (iNumberOfObjects > 0) {
				for(Iterator relListItr = relBusObjList.iterator();relListItr.hasNext();)
				{
					Map tempList = (Hashtable)relListItr.next();
					String relObjId = (String)tempList.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
					MapList preEffectivityExprList = (MapList)EFF.getRelExpression(context,relObjId);
					String preEffectivityExpression = (String)((HashMap)preEffectivityExprList.get(0)).get(EffectivityFramework.ACTUAL_VALUE);
					//Case : Effective Date was not set initially
					if(UIUtil.isNullOrEmpty(preEffectivityExpression))
					{	
						validEffectivityExpression.delete(0, validEffectivityExpression.length());
						
						String sTodaysDate = ConfigurationUtil.getTodaysDate(context, "display");
				     	TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
						int tzoff = tz.getRawOffset();
						Double timezone = new Double((tzoff )/(3600000));						
						Map expressionMap = EFF.formatExpression(context, "Date", sTodaysDate, timezone);
						validEffectivityExpression.append((String)expressionMap.get(EffectivityFramework.ACTUAL_VALUE));
				
					}else
					{
						StringTokenizer expressionTokens = new StringTokenizer(preEffectivityExpression.trim(),EffectivityFramework.OPERATOR_OR);
						String processedTokens = ConfigurationConstants.EMPTY_STRING;
						int tokenCount =expressionTokens.countTokens(); 
						while(expressionTokens.hasMoreTokens())
						{
							String tokens = (String)expressionTokens.nextToken();
							processedTokens = processEffectivityExpression(context,tokens.trim(),"Promote");
							if(tokenCount > 1)
							{
								validEffectivityExpression.append(processedTokens);
								validEffectivityExpression.append(" ");
								validEffectivityExpression.append(EffectivityFramework.OPERATOR_OR);
								validEffectivityExpression.append(" ");
							}else{
								validEffectivityExpression.append(processedTokens);
							}
						}
						if(tokenCount > 1)
						{
							int lIndex  = validEffectivityExpression.lastIndexOf(EffectivityFramework.OPERATOR_OR);
							validEffectivityExpression.delete(lIndex, validEffectivityExpression.length());
						}	
					}
					
					String strCFFExpression = validEffectivityExpression.toString();
					EFF.setRelExpression(context, relObjId, strCFFExpression);
				}//End:for(Iterator...
			}//End:if (iNumberOfObjects...
		}

		return 0;
	}

	    /**
	     * Method called as a trigger to reset the Usage Effectivity on Configuration Feature demote
	     * from Obsolete.
	     * @param context
	     *            the eMatrix <code>Context</code> object
	     * @param args -
	     *            Holds the parameters passed from the calling method
	     * @return int - Returns 0 in case of Check trigger is success and 1 in case
	     *         of failure
	     * @throws Exception
	     *             if operation fails
	     * @since R214
	     * @author XOG
	     */
		public int resetUsageEffectivityOnDemote(Context context,String args[]) throws Exception
		{
	        // The Parent Feature object id sent by the emxTriggerManager
		    String objectId = args[0];
		    EffectivityFramework EFF = new EffectivityFramework();
			StringBuffer validEffectivityExpression = new StringBuffer();
			String strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES;
			
			StringBuffer sbTypePattern = new StringBuffer(50); 
			sbTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
			sbTypePattern.append(",");
			sbTypePattern.append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE);
			
			
			StringList objectSelects = new StringList(ConfigurationConstants.SELECT_ID);
			StringList relSelects = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);

			ConfigurationFeature configurationFeature = new ConfigurationFeature(
					objectId);

			MapList relBusObjList = configurationFeature
					.getConfigurationFeatureStructure(context, sbTypePattern.toString(),
							strRelationshipPattern, objectSelects, relSelects,
							true, false, 1, 0, DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING, (short) 1,
							DomainConstants.EMPTY_STRING);

			if (relBusObjList != null) {
				int iNumberOfObjects = relBusObjList.size();
				if (iNumberOfObjects > 0) {
					for(Iterator relListItr = relBusObjList.iterator();relListItr.hasNext();){
						Map tempList = (Hashtable)relListItr.next();
						String relObjId = (String)tempList.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
						MapList preEffectivityExprList = (MapList)EFF.getRelExpression(context,relObjId);
						String preEffectivityExpression = (String)((HashMap)preEffectivityExprList.get(0)).get(EffectivityFramework.ACTUAL_VALUE);

						validEffectivityExpression.delete(0, validEffectivityExpression.length());
						if(!UIUtil.isNullOrEmpty(preEffectivityExpression))
						{
							StringTokenizer expressionTokens = new StringTokenizer(preEffectivityExpression.trim(),EffectivityFramework.OPERATOR_OR);
							String processedTokens = ConfigurationConstants.EMPTY_STRING;
							int tokenCount = expressionTokens.countTokens();
							while(expressionTokens.hasMoreTokens())
							{
								String tokens = (String)expressionTokens.nextToken();
								processedTokens = processEffectivityExpression(context,tokens.trim(),"Demote");
								if(tokenCount > 1)
								{
									validEffectivityExpression.append(processedTokens);
									validEffectivityExpression.append(" ");
									validEffectivityExpression.append(EffectivityFramework.OPERATOR_OR);
									validEffectivityExpression.append(" ");
								}

							}
							if(tokenCount > 1)
							{
								int lIndex  = validEffectivityExpression.lastIndexOf(EffectivityFramework.OPERATOR_OR);
								validEffectivityExpression.delete(lIndex, validEffectivityExpression.length());
							}	
							
							String strCFFExpression = validEffectivityExpression.toString();
							EFF.setRelExpression(context, relObjId, strCFFExpression);
						}
					}
				}
			}
			return 0;
		}
		/**
		 * It process the Effectivity Expression on Configuration Feature on its Promotion/Demotion
		 * 
		 * @param context
		 * @param dateExpression
		 * @param mode
		 * @return
		 * @throws Exception
		 */
		private  String processEffectivityExpression(Context context,String dateExpression,String mode) throws Exception
		{
			boolean openBrace = false;
			StringBuffer validDateExpression = new StringBuffer();
			StringBuffer validFinalDateExpression = new StringBuffer();
			EffectivityFramework EFF = new EffectivityFramework();
			Map effectivitySettings = EFF.getEffectivityTypeSettings(context);
			String dateKeyword = ConfigurationConstants.EMPTY_STRING;
			String validDate = ConfigurationConstants.EMPTY_STRING;
			if(effectivitySettings != null && effectivitySettings.containsKey("Date"))
			{
				Map dateMap = (HashMap)effectivitySettings.get("Date");
				if(dateMap != null)
				{
					dateKeyword = (String)dateMap.get("keyword");
				}
			}
			StringBuffer sbCFFDateKeyword = new StringBuffer();
			sbCFFDateKeyword.append(EffectivityFramework.KEYWORD_PREFIX);
			sbCFFDateKeyword.append(dateKeyword);
			if(!dateExpression.isEmpty() && dateExpression.substring(0,1).equals("("))
			{
				openBrace = true;
			}
			dateExpression = dateExpression.replaceAll("\\(","");
			dateExpression = dateExpression.replaceAll("\\)","");
			dateExpression = dateExpression.replaceAll(sbCFFDateKeyword.toString(),"");
			dateExpression = dateExpression.trim();
			String [] dateComponent = dateExpression.split(EffectivityFramework.VALUE_SEPARATOR_STORAGE);
			validDateExpression.append(sbCFFDateKeyword.toString());
			validDateExpression.append(EFF.KEYWORD_OPEN_BRACKET);
			for(Object dateObj:dateComponent){
				String unchekedDate = (String)dateObj;
				if(mode.equalsIgnoreCase("Promote")){
					validDate = getValidDateOnPromote(context,unchekedDate);
				}else{
					validDate = getValidDateOnDemote(context,unchekedDate);
				}
				
				if(validDate.indexOf("EFF_INVALID") > -1){
					validDateExpression.delete(0, validDateExpression.length());
					validDateExpression.append(validDate);
					break;
				} else if(!validDate.isEmpty()){
					validDateExpression.append(validDate).append(EffectivityFramework.VALUE_SEPARATOR_STORAGE);
				}else {
					validDateExpression.delete(0, validDateExpression.length());
					break;
				}
			}
			if(validDate.indexOf("EFF_INVALID") > -1){
				validFinalDateExpression.append(validDateExpression);
			}
			else if(!validDate.isEmpty()){
				validDateExpression.deleteCharAt(validDateExpression.length()-1);
				validDateExpression.append(EFF.KEYWORD_CLOSE_BRACKET);
				if(openBrace){
					validFinalDateExpression.append("( ");
					validFinalDateExpression.append(validDateExpression);
					validFinalDateExpression.append(" )");
					
				}else{
					validFinalDateExpression.append(validDateExpression);
				}
			}
			return validFinalDateExpression.toString();
		}
	     /*
	      * Validate the date and returns date as per the date of promotion
	      * 
	      */
	     private  String getValidDateOnPromote(Context context,String dateRange)throws Exception
	     {
	    	 StringBuffer validDate 	= new StringBuffer();
	    	 String startEffectivity	= ConfigurationConstants.EMPTY_STRING;
	    	 String endEffectivity		= ConfigurationConstants.EMPTY_STRING;
	    	 String sTodaysDateInput 	= ConfigurationUtil.getTodaysDate(context,"input");
	    	 StringTokenizer dateTokens = new StringTokenizer(dateRange,EffectivityFramework.RANGE_SEPARATOR_STORAGE);
	    	 
	    	 while(dateTokens.hasMoreTokens())
	    	 {
	    		startEffectivity = dateTokens.nextToken().trim();
	    		if(dateTokens.hasMoreTokens()){
	    			endEffectivity = dateTokens.nextToken().trim();
	    		}
	     		Date dtStartDate	 		= eMatrixDateFormat.getJavaDate(startEffectivity);
	    		Date dtTodaysDate	 		= eMatrixDateFormat.getJavaDate(sTodaysDateInput);
	    		DateFormat df 				= new SimpleDateFormat("yyyy-MM-dd");
	    		Date dtStartEffectivityDate = df.parse(df.format(dtStartDate));
	    		Date dtNewTodaysDate  		= df.parse(df.format(dtTodaysDate));
	    		boolean isStartDateinFuture 	= dtNewTodaysDate.before(dtStartEffectivityDate);
	    		boolean areDatesEqual 		= dtNewTodaysDate.equals(dtStartEffectivityDate);
	    		if(isStartDateinFuture || areDatesEqual ) 
	    				
	    		{
	    			validDate.append("EFF_INVALID");//To make the Feature invalid for all cases since it was valid in future but
													//is promoted to obsolescence
	    		} else
	    		{
	    			validDate.append(startEffectivity);
	    			if(!endEffectivity.equals(ConfigurationConstants.EMPTY_STRING))
	    			{
	    				validDate.append(EffectivityFramework.RANGE_SEPARATOR_STORAGE);
	    				if(endEffectivity.equals(EffectivityFramework.INFINITY_SYMBOL_STORAGE))	{
	    					validDate.append(sTodaysDateInput);
	    	        	}else{
	    	        		Date dtEndDate = eMatrixDateFormat.getJavaDate(endEffectivity);
	    	        		Date dtEndEffectivityDate  = df.parse(df.format(dtEndDate));
	    	        		boolean endDateInValid = dtNewTodaysDate.before(dtEndEffectivityDate);
	    	        		if(endDateInValid)
	    					{
	    	        			validDate.append(sTodaysDateInput);
	    					}else{
	    						validDate.append(endEffectivity);
	    					}
	    	        	}
	    		 }//End:if(!endEffectivity
	    	  }//End:else
	    	 }
	    	 return validDate.toString();
	     }		
	     /**
	      * Validate the date and returns date as per the date of demotion
	      * @param context
	      * @param dateRange
	      * @return
	      * @throws Exception
	      */
	     private  String getValidDateOnDemote(Context context,String dateRange)throws Exception
	     {
	    	 StringBuffer validDate 	= new StringBuffer();
	    	 String startEffectivity	= ConfigurationConstants.EMPTY_STRING;
	    	 boolean isRange 			= false;
	    	 StringTokenizer dateTokens = new StringTokenizer(dateRange,EffectivityFramework.RANGE_SEPARATOR_STORAGE);
	    	 
	    	 while(dateTokens.hasMoreTokens())
	    	 {
	    		startEffectivity = dateTokens.nextToken().trim();
	    		if(!startEffectivity.equals(EffectivityFramework.INFINITY_SYMBOL_STORAGE)){
	    			validDate.append(startEffectivity);
	    			
	    		}else {
	    			validDate.append(ConfigurationConstants.EMPTY_STRING);
	    			break;
	    		}
	    		if(dateTokens.hasMoreTokens()){
	    			isRange = true;
	    			dateTokens.nextToken();
	    			validDate.append(EffectivityFramework.RANGE_SEPARATOR_STORAGE);
	    			validDate.append(EffectivityFramework.INFINITY_SYMBOL_STORAGE);
	    		}
	    		if(!isRange){
	    			validDate.delete(0, validDate.length());
	    			validDate.append(ConfigurationConstants.EMPTY_STRING);
	    		}
	    	 }	
	    	 return validDate.toString();
	     }
	     /**
	      * Returns true if the LibraryCentral is installed otherwise false.
	      * @mx.whereUsed This method will be called from part property pages
	      * @mx.summary   This method check whether LibraryCentral is installed or not, this method can be used as access program to show/hide the ClassificationAttributes
	      * @param context the eMatrix <code>Context</code> object.
	      * @return boolean true or false based condition.
	      * @throws Exception if the operation fails.
	      * @since R214
	      */
	     public boolean isLBCInstalled(Context context,String[] args) throws Exception
	     {
	     boolean lcInstalled= FrameworkUtil.isSuiteRegistered(context,"appVersionLibraryCentral",false,null,null);
	     if (lcInstalled)
	           return true;
	     else
	           return false;
	     }
	     
	 	/**
	 	 * This Methods is used to get the Configuration Feature Structure.
	 	 *
	 	 * @param context
	 	 *            the eMatrix <code>Context</code> object
	 	 * @param args -
	 	 *            Holds the following arguments 0 - HashMap containing the
	 	 *            following arguments
	 	 * @return Object - MapList containing the Configuration Feature Structure Details
	 	 * @throws FrameworkException
	 	 * @since R212
	 	 */
	 	public MapList getModConfigurationFeatureStructure(Context context, String[] args)	
	 	          throws FrameworkException {
			MapList mapConfigurationStructure = null;
			
			try{			
			  	Map programMap = (Map) JPO.unpackArgs(args);
			  	String strObjectId = (String) programMap.get("objectId");
			  	StringList objectSelects = new StringList();
				objectSelects.add(DomainObject.SELECT_ID);
				objectSelects.add(DomainObject.SELECT_TYPE);
				objectSelects.add(DomainObject.SELECT_NAME);
				objectSelects.add(DomainObject.SELECT_CURRENT);
				
				StringList relationshipSelects = new StringList();
				relationshipSelects.add(DomainRelationship.SELECT_ID);
				relationshipSelects.add(DomainRelationship.SELECT_TYPE);
				
				StringBuffer strRelPattern = new StringBuffer(ConfigurationConstants.RELATIONSHIP_PRODUCTS);
				strRelPattern.append(",");
				strRelPattern.append(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES);
				
			  	DomainObject domContextObj = new DomainObject(strObjectId);
				mapConfigurationStructure = domContextObj.getRelatedObjects(context, strRelPattern.toString(), DomainObject.QUERY_WILDCARD,
						  objectSelects, relationshipSelects,
		 				  false, true, (short) 0, 
		 				  DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
				
			}catch(Exception e){
				throw new FrameworkException(e.getMessage());
			}
	 		return mapConfigurationStructure;
	 	}
	 	 
	    @com.matrixone.apps.framework.ui.ProgramCallable
	 	public MapList getModelConfigurationDictionary(Context context, String[] args) throws Exception
	     {
	    	 HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	    	 String strModelId = (String)paramMap.get("objectId");
	    	 String strExpLvl = (String)paramMap.get("Expand Level");
	    	 Model model = new Model(strModelId);
	    	 MapList returnList = model.getModelConfigurationDictionary(context,strModelId,strExpLvl,true);
	    	 return returnList;
	    	 
	     }
	     /**
	      * Relationship trigger invoked on modifying attribute on relationship Configuration Structures to not allow 
	      * setting of Effectivity while structure is in frozen state.
	      * @param context
	      * @param args
	      * @return
	      * @throws Exception
	      */
	     public int checkAccessForSettingEffectivity(Context context, String [] args) throws Exception
	     {
	    	 int iResult = 0;
			 String strNoAccessMessage = EnoviaResourceBundle.getProperty(context,
						SUITE_KEY, "emxConfiguration.DateEffectivity.ObjectFrozen",context.getSession().getLanguage());
			 boolean isCFFAttribute = false;
			 String fromObjectID = args[0];
			 String attrName = args[1];
			 StringList _attrCFFList = new StringList();
			 _attrCFFList.add(ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_TYPES);
			 _attrCFFList.add(ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_COMPILED_FORM);
			 _attrCFFList.add(ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_EXPRESSION);
			 _attrCFFList.add(ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_VARIABLE_INDEXES);
			 _attrCFFList.add(ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_ORDERED_CRITERIA);
			 _attrCFFList.add(ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_EXPRESSION_BINARY);
			 if(_attrCFFList.contains(attrName))
			 {
				 isCFFAttribute = true;
			 }
			 if(isCFFAttribute && ConfigurationUtil.isFrozenState(context, fromObjectID))
				 throw new FrameworkException(strNoAccessMessage);	
				 
	    	 return iResult;
	    	 
	     }		 
	     /**
	      * This method is 
	      * @param context
	      * @param args
	      * @return
	      * @throws Exception
	      * @author G98
	      * @since R214
	      */
	     public static int  deleteSelectedOptionsRelOnConfigOptions(Context context, String[] args) throws Exception
	     {
	    	 try {


	    		 String relID = args[0];
	    		 String toObjectID = args[2];

	    		 StringList lfIdsFromGBOM =  new StringList();
	    		 Set set = new HashSet();
	    		 StringList pcIdList =  new StringList();
	    		 DomainRelationship domRel = new DomainRelationship(relID);
	    		 String pcFromRelSelects = "tomid[Selected Options].from.id";
	    		 Hashtable relData = (Hashtable)domRel.getRelationshipData(context, new StringList(pcFromRelSelects));
	    		 pcIdList  = (StringList)relData.get(pcFromRelSelects);

	    		 String strTypePattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURES;
	    		 String strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES;	    		 	  
	    		 String relSelectedOptionsSelect = "tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].id";
	    		 String pcIDfromCfRelSelect = "tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.id";
	    		 String selectedOptionsRelIdSelect = "tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].id";
	    		 String prodIdFromRelContextSelect = "tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+ "].from.to[Product Configuration].from.id";
	    		 String logFeatFromInclusionBOM = "tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION + 
	    		 "].from.from["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +
	    		 "].torel[" +  ConfigurationConstants.RELATIONSHIP_GBOM + "].from.id";
	    		 //"tomid[Right Expression].from.from[Left Expression].torel[GBOM].from.id";

	    		 StringList slObjSelects = new StringList();
	    		 slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
	    		 slObjSelects.addElement(ConfigurationConstants.SELECT_TYPE);
	    		 slObjSelects.addElement(ConfigurationConstants.SELECT_NAME);
	    		 slObjSelects.addElement(ConfigurationConstants.SELECT_REVISION);    		

	    		 StringList slRelSelects = new StringList();
	    		 slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
	    		 slRelSelects.addElement(SELECT_FROM_NAME);
	    		 slRelSelects.addElement(SELECT_FROM_ID);	    		 	    		 
	    		 slRelSelects.addElement(relSelectedOptionsSelect);
	    		 slRelSelects.addElement(pcIDfromCfRelSelect);
	    		 slRelSelects.addElement(prodIdFromRelContextSelect);
	    		 slRelSelects.addElement(selectedOptionsRelIdSelect);
	    		 slRelSelects.addElement(logFeatFromInclusionBOM);

	    		 ConfigurationFeature removedConfigFeat = new ConfigurationFeature(toObjectID);			 
	    		 MapList mapCFStructure = (MapList)removedConfigFeat.getConfigurationFeatureStructure(context,
	    				 strTypePattern, 
	    				 strRelPattern, 
	    				 slObjSelects, 
	    				 slRelSelects, 
	    				 false,	    			 									
	    				 true,
	    				 0,0, 
	    				 DomainConstants.EMPTY_STRING, 
	    				 DomainObject.EMPTY_STRING, 
	    				 DomainObject.FILTER_STR_AND_ITEM,
	    				 DomainConstants.EMPTY_STRING);
	    		 
	    		 StringList deleteSelOptionRelId = new StringList();
	    		 for (int i = 0; i < mapCFStructure.size(); i++) {
	    			 Map tempMap =(Map)mapCFStructure.get(i);
	    			 StringList selectedOptionsRelIdList =  ConfigurationUtil.convertObjToStringList(context,tempMap.get(selectedOptionsRelIdSelect));	    			 
	    			 StringList pcIDfromCfRelContext = ConfigurationUtil.convertObjToStringList(context,tempMap.get(pcIDfromCfRelSelect));

	    			 if(selectedOptionsRelIdList!=null)
	    			 {
	    				 for (int index = 0; index < pcIDfromCfRelContext.size(); index++){
	    					 if (pcIdList.contains(pcIDfromCfRelContext.get(index)))
	    					 {
	    						 deleteSelOptionRelId.add(selectedOptionsRelIdList.get(index));
	    					 }
	    				 }
	    			 }
	    			 set.addAll(ConfigurationUtil.convertObjToStringList(context,tempMap.get(logFeatFromInclusionBOM)));

	    		 }
	    		 String [] oidsArray = new String[deleteSelOptionRelId.size()];
	    		 if(deleteSelOptionRelId.size()>0)
	    		 {
	    			 DomainRelationship.disconnect(context, (String[])deleteSelOptionRelId.toArray(oidsArray));
	    		 }

	    		 lfIdsFromGBOM.addAll(set);
	    		 for (int count = 0; count < pcIdList.size(); count++){

	    			 Job job = new Job();
	    			 String[] arrJPOArgs = new String[3];
	    			 arrJPOArgs[0] = (String)pcIdList.get(count);
	    			 arrJPOArgs[1] = lfIdsFromGBOM.toString();
	    			 arrJPOArgs[2] = "GBOMUpdate";
	    			 job = new Job("emxProductConfigurationEBOM","deltaUpdateBOMXMLForProductConfiguration",arrJPOArgs);
	    			 job.setContextObject((String)pcIdList.get(count));
	    			 job.setTitle("Delta Update BOM XML");
	    			 job.setDescription("Updating the BOM XML for all related Product Configurations");
	    			 job.createAndSubmit(context);

	    		 }

	    	 } catch (Exception e) {				
	    		 e.printStackTrace();
	    	 }

	    	 return 0;
	     }

	/**
	 * Wrapper method which will call method to check Product delete
	 * to restrict removal of Product if any of the Product's
	 * Configuration Feature/Option structure is used in FO Effectivity
	 * Expression.
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int checkIfProductStructureUsedInFOEffectivity(Context context,
			String args[]) throws Exception {
		String strPRDId = args[0];
		int iReturn = 0;
		DomainObject dom = new DomainObject(strPRDId);
		// Code modified for IR-179749V6R2013x
		if(dom.exists(context) && dom.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS)){		
			try {

				Product prdBean = new Product(strPRDId);
				iReturn = prdBean.isChildStructureUsedInFOEffectivity(context);
				if(iReturn==1){
					String strLanguage = context.getSession().getLanguage();
					// Code commented for IR-179749V6R2013x
					//String strErrorMsg = i18Now.GetString("emxConfigurationStringResource", strLanguage,"emxConfiguration.Alert.RestrictRemoveFOEffectivityConnected");
					//throw new FrameworkException(strErrorMsg);	
					String errorMessage = (EnoviaResourceBundle.getProperty(context,"Effectivity", "Effectivity.Error.EffectivityUsageCannotDelete",strLanguage)).trim();
			        emxContextUtil_mxJPO.mqlError(context, errorMessage);
				}

			} catch (Exception e) {
				e.printStackTrace();
				iReturn = 1;
			}
		}
		return iReturn;
	}	     

	/**
	 * Wrapper method which will call method on CONFIGURATION STRUCTURES rel delete
	 * to restrict removal of Product if any of the Product's
	 * Configuration Feature/Option structure is used in FO Effectivity
	 * Expression.
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int checkIfCFStructureUsedInFOEffectivity(Context context,
			String args[]) throws Exception {
		String _sRelTypeToDisconnect=args[0];
		String _sToSideObjectId=args[1];
		String _sFromSideObjectId=args[2];
		int iReturn = 0;
		try {
		if(UIUtil.isNotNullAndNotEmpty(_sRelTypeToDisconnect) && ConfigurationUtil.isOfParentRel(context,
                                                            _sRelTypeToDisconnect,
                                                            ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES)){
			ConfigurationFeature cfBean = new ConfigurationFeature();
			iReturn = cfBean.isChildStructureUsedInFOEffectivity(context,_sRelTypeToDisconnect,_sToSideObjectId,_sFromSideObjectId);
			if(iReturn==1){
				String strLanguage = context.getSession().getLanguage();
				// Code commented for IR-179749V6R2013x 
				//String strErrorMsg = i18Now.GetString("emxConfigurationStringResource", strLanguage,"emxConfiguration.Alert.RestrictRemoveFOEffectivityConnected");
				//throw new FrameworkException(strErrorMsg);
				String errorMessage = (EnoviaResourceBundle.getProperty(context,"Effectivity", "Effectivity.Error.EffectivityUsageCannotDelete",strLanguage)).trim();
		        emxContextUtil_mxJPO.mqlError(context, errorMessage);
			}
		}	
		} catch (FrameworkException fe) {			
			iReturn = 1;
			// Code commented for IR-179749V6R2013x
			//throw new FrameworkException(e.getMessage());	
		}catch (Exception e) {			
			iReturn = 1;
			// Code commented for IR-179749V6R2013x
			//throw new FrameworkException(e.getMessage());	
		}
		return iReturn;
	}	
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getConfigurationFeatureStructureForProductContext(Context context, String[] args)	
    throws FrameworkException {	
	// System.out.println("${CLASSNAME}.getConfigurationFeatureStructureForProductContext()....%%%%%%%%%% ......");
		MapList mapConfigurationStructure = null;
		StringBuffer strObjWhere = new StringBuffer();	
		try{	
			
		  	Map programMap = (Map) JPO.unpackArgs(args);
		  	String strObjectId = (String) programMap.get("objectId");
		  	// call method to get the level details
			int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
			int limit = 0;// Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.ExpandLimit"));
					  	
		  	String sNameFilterValue = (String) programMap.get("FTRConfigurationFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRConfigurationFeatureLimitFilterCommand");
		  	boolean isCalledFromRule=false;
			if(sNameFilterValue==null){
		  		sNameFilterValue = (String) programMap.get("FTRConfigurationFeatureNameFilterForRuleDialog");
		  		if(sNameFilterValue!=null) isCalledFromRule= true;
		  	}
         if(sLimitFilterValue==null)
         	sLimitFilterValue = (String) programMap.get("FTRConfigurationFeatureLimitFilterForRuleDialog");
			if (sLimitFilterValue != null
					&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
				if (sLimitFilterValue.length() > 0) {
					limit = (short) Integer.parseInt(sLimitFilterValue);	
					if (limit < 0) {
						limit = 0;//Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.ExpandLimit"));
					}
				}
			}
			
			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {				
				strObjWhere.append("attribute[");
				strObjWhere.append(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME);
				strObjWhere.append("] ~~ '");
				strObjWhere.append(sNameFilterValue);
				strObjWhere.append("'");
			}
			
			
			//if this is called from Rule, then add object where, to prevent invalid state object being seen in Rule context Tree
			if(isCalledFromRule){
				if(!strObjWhere.toString().trim().isEmpty()){
					strObjWhere.append(" && ");
					strObjWhere.append(RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE));
				}					
				else{
					strObjWhere.append(RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE));
				}						
			}
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");
			String strTypePattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURES;
         String strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES;
         StringList slObjSelects = new StringList();
         //Object Select
         slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
         slObjSelects.addElement(ConfigurationConstants.SELECT_TYPE);
         slObjSelects.addElement(ConfigurationConstants.SELECT_NAME);
         slObjSelects.addElement(ConfigurationConstants.SELECT_REVISION);
         slObjSelects.addElement(ConfigurationConstants.SELECT_CURRENT);
         slObjSelects.addElement(ConfigurationConstants.SELECT_POLICY);
         slObjSelects.addElement(ConfigurationConstants.SELECT_OWNER);
         slObjSelects.addElement("next");
         slObjSelects.addElement("altowner1");
         
        slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
        slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
        slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_TYPE);
        slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_KEYIN_TYPE);
        slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT);
        //slObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name");
         StringList slRelSelects = new StringList();
         slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
         slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_TYPE);
         slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);                     
         ConfigurationFeature cfBean = new ConfigurationFeature(strObjectId); 
         // rel select
         slRelSelects.addElement("tomid["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"].from["+ConfigurationConstants.TYPE_INCLUSION_RULE+"].id");
         slRelSelects.addElement("tomid["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"].from["+ConfigurationConstants.TYPE_INCLUSION_RULE+"].attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"].value");
         slRelSelects.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
         slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA+ "]");
         slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE+ "]");
         slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_LIST_PRICE+"]");
         slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]");
         slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY+"]");
         slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY+"]");
         slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");
         
         
      
         
         mapConfigurationStructure = (MapList)cfBean.getConfigurationFeatureStructure(context,strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
					true,iLevel,limit, strObjWhere.toString(), DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,filterExpression);			
			if (mapConfigurationStructure != null) {
				HashMap hmTemp = new HashMap();
				hmTemp.put("expandMultiLevelsJPO", "true");
				mapConfigurationStructure.add(hmTemp);
			}
			
		}catch (Exception e) {
			throw new FrameworkException(e);
		}
		return mapConfigurationStructure;
	  }
	 /**
	  * This method is used to return the status icon of an object
	  * @param context the eMatrix <code>Context</code> object
	  * @param args holds arguments
	  * @return List- the List of Strings in the form of 'Name Revision'
	  * @throws FrameworkException - if the operation fails
	 **/

	 public List getStatusIconForProductContext (Context context, String[] args) throws FrameworkException{
	 	List lstNameRev = new StringList();	    
	     try{
	     	Map programMap = (HashMap) JPO.unpackArgs(args);
		        List lstobjectList = (MapList) programMap.get("objectList");
		        Iterator objectListItr = lstobjectList.iterator();
		        Map paramList = (HashMap)programMap.get("paramList");
		        String reportFormat = (String)paramList.get("reportFormat");
		    	String strLanguage = context.getSession().getLanguage();		    	
		        //initialise the local variables
		        Map objectMap = new HashMap();
		        String strObjId = DomainConstants.EMPTY_STRING;
		        String strObjState = DomainConstants.EMPTY_STRING;
		        String strIcon = DomainConstants.EMPTY_STRING;
		        String strObjPolicy = DomainConstants.EMPTY_STRING;
		        String strObjPolicySymb = DomainConstants.EMPTY_STRING;
		        String strObjStateSymb = DomainConstants.EMPTY_STRING;
		        StringBuffer sbStatePolicyKey = new StringBuffer();
		        boolean flag = false;
		        StringBuffer stbNameRev = new StringBuffer(100);
		        DomainObject domObj = null;		        
		        Map cacheMap=new HashMap();	
		      // selectable for root node  
		   	 StringList objectSelects = new StringList();	    
	 	 objectSelects.add(DomainConstants.SELECT_CURRENT);
	 	 objectSelects.add(DomainConstants.SELECT_POLICY);    	 
	 	//I18 conversion 
		       String strStateCFPreliminary=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxFramework.State.Configuration_Feature.Preliminary", strLanguage);
		       String strStateCFReview=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxFramework.State.Configuration_Feature.Review", strLanguage);
		       String strStateCFRelease=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxFramework.State.Configuration_Feature.Release", strLanguage);
		       String strStateCFObsolete=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxFramework.State.Configuration_Feature.Obsolete", strLanguage);
		       String strStateCFExists=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxFramework.State.Configuration_Option.Exists", strLanguage);
		       //String strStateCFExists=i18nNow.getI18nString("emxFramework.State.Configuration_Feature.Exists","emxConfigurationStringResource", strLanguage);
		       
		       
		        //loop through all the records
		        while(objectListItr.hasNext())
		        {
		            objectMap = (Map) objectListItr.next();
		            String isRootNode= (String)objectMap.get("Root Node");
		         if(ProductLineCommon.isNotNull(isRootNode)){
		            if(isRootNode.equalsIgnoreCase("true")){
		            	 strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strObjState=(String)rootMap.get(DomainConstants.SELECT_CURRENT);
				            strObjPolicy=(String)rootMap.get(DomainConstants.SELECT_POLICY);
				            
		            }
		         }
		     	else if(objectMap.containsKey("parentLevel")){
		     		strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
		            domObj = DomainObject.newInstance(context, strObjId);				      
		            Map rootMap= domObj.getInfo(context,objectSelects);
		            strObjState=(String)rootMap.get(DomainConstants.SELECT_CURRENT);
		            strObjPolicy=(String)rootMap.get(DomainConstants.SELECT_POLICY);
				}else{
		            strObjState = (String)objectMap.get(DomainConstants.SELECT_CURRENT);
		            strObjPolicy = (String)objectMap.get(DomainConstants.SELECT_POLICY);
		            }
		         
		            if(cacheMap.containsKey(strObjPolicy)){
		            	strObjPolicySymb=(String)cacheMap.get(strObjPolicy);         
		            }
		            else{
		            	 strObjPolicySymb = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_POLICY,strObjPolicy,true);
		            	 cacheMap.put(strObjPolicy, strObjPolicySymb);
		            }
		            if(cacheMap.containsKey(strObjState)){
		            	strObjStateSymb=(String)cacheMap.get(strObjState);         
		            }
		            else{
		            	strObjStateSymb = FrameworkUtil.reverseLookupStateName(context, strObjPolicy, strObjState);
		            	cacheMap.put(strObjState, strObjStateSymb);
		            }
		            // Forming the key which is to be looked up
		            sbStatePolicyKey = new StringBuffer("emxProduct.LCStatusImage.");
		            sbStatePolicyKey.append(strObjPolicySymb)
		                            .append(".")
		                            .append(strObjStateSymb);

		            // Getting the value for the corresponding key, if not catching it to set flag = false
		            try{
		                strIcon = EnoviaResourceBundle.getProperty(context,sbStatePolicyKey.toString());
		                flag = true;
		            }
		            catch(Exception ex)
		            {
		                flag = false;
		            }

		            if(flag)
		            {
		                if(ProductLineCommon.isNotNull(isRootNode)){
		                	strObjPolicy = FrameworkUtil.findAndReplace(strObjPolicy," ", "");
		                StringBuffer sbStateKey = new StringBuffer("emxFramework.State.");
		                sbStateKey.append(strObjPolicy);
		                sbStateKey.append(".");
		                sbStateKey.append(strObjState);
		                strObjState = EnoviaResourceBundle.getProperty(context,"Framework",sbStateKey.toString(), strLanguage);
		 		       
		                }else{
		            	if(strObjPolicy.equalsIgnoreCase(POLICY_CONFIGURATION_FEATURE)){
		            		//TODO HARDCODED STATES
		            		if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_PRELIMINARY)){
		            			strObjState=strStateCFPreliminary;	
		            		}else if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_REVIEW)){
		            			strObjState=strStateCFReview;
		            		}else if(strObjState.equalsIgnoreCase(ConfigurationConstants.STATE_RELEASE)){
		            			strObjState=strStateCFRelease;
		            		}else{
		            			strObjState=strStateCFObsolete;
		            		}	
		            	}else{
		            		strObjState=strStateCFExists;
		            	}
		              }
		                stbNameRev.delete(0, stbNameRev.length());
		                if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
		            		lstNameRev.add(strObjState);
		            	}else{
			              stbNameRev=stbNameRev.append("<img src=\"../common/images/")
	                     .append(strIcon)
	                     .append("\" border=\"0\"  align=\"middle\" TITLE=\" ")                                           
	                     .append(XSSUtil.encodeForXML(context,strObjState))
	                     .append("\"/>");                            
			                lstNameRev.add(stbNameRev.toString());
		            	}
		            }
		            else
		            {
		                lstNameRev.add(DomainConstants.EMPTY_STRING);
		            }
		            
		        
		        }
		        }
	     catch(Exception e){
				throw new FrameworkException(e);
			}
		
	     return lstNameRev;
	 }
		/**
		 * This method is return the value for selection type  column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
	 public static StringList getSelectionType (Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strSelectionType = "";
			    String strLanguage = context.getSession().getLanguage();
			    String strSelectionCriteriaSingle=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Configuration_Selection_Type.Single", strLanguage)).trim();
			    String strSelectionCriteriaMultiple=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Configuration_Selection_Type.Multiple", strLanguage)).trim();
			    
				StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					//strSelectionType =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_TYPE);
					String strI18Value ="";
					String strType = (String)outerMap.get(ConfigurationConstants.SELECT_TYPE);
					StringList cfSubTypes = ProductLineUtil.getChildTypesIncludingSelf(context, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE);
					if (outerMap
							.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_TYPE)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_TYPE))){
						
						if(ProductLineCommon.isNotNull(strType) && cfSubTypes.contains(strType)){
						strSelectionType =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_TYPE);
						}
						else{
						strSelectionType =  ConfigurationConstants.EMPTY_STRING;
						}
					}else if (outerMap
							.containsKey(DomainObject.SELECT_ID)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(DomainObject.SELECT_ID))){
						
						if(ProductLineCommon.isNotNull(strType) && cfSubTypes.contains(strType)){
						DomainObject domObj = DomainObject.newInstance(context, (String) outerMap
									.get(DomainObject.SELECT_ID));
						strSelectionType=domObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_TYPE);
						}
						else{
						strSelectionType =  ConfigurationConstants.EMPTY_STRING;
						}
					}
					if (strSelectionType != null) {
						if (strSelectionType
								.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_SINGLE)) {
							strI18Value = strSelectionCriteriaSingle;

						} else if (strSelectionType
								.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MULTIPLE)) {
							strI18Value = strSelectionCriteriaMultiple;
						}
					}
					if(!("".equalsIgnoreCase(strI18Value) || "null".equalsIgnoreCase(strI18Value)|| strI18Value == null))
					{
						returnStringList.addElement(strI18Value);
					}else {
						returnStringList.addElement("");
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
	 public Map getRangeValuesForSelectionType(Context context,String[] args) throws Exception 
	 {
	     String strAttributeName = ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_TYPE;
	     HashMap rangeMap = new HashMap();
	     matrix.db.AttributeType attribName = new matrix.db.AttributeType(
	             strAttributeName);
	     attribName.open(context);

	     List attributeRange = attribName.getChoices();
	     List attributeDisplayRange = i18nNow
	             .getAttrRangeI18NStringList(
	                     ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_TYPE,
	                     (StringList) attributeRange, context.getSession()
	                             .getLanguage());
	     rangeMap.put(FIELD_CHOICES, attributeRange);
	     rangeMap.put(FIELD_DISPLAY_CHOICES, attributeDisplayRange);
	     return rangeMap;
	 }
	 /**
		 * This method is an Update Function on edit of Selection Type
		 * @param context
		 * @param args
		 * @throws Exception
		 */
		public void updateSelectionType(Context context, String[] args) throws Exception 
		{

			try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				HashMap paramMap = (HashMap) programMap.get("paramMap");
				String strObjId = (String) paramMap.get("objectId");
				String strNewAttributeValue = (String) paramMap.get("New Value");
				DomainObject domObj = new DomainObject(strObjId);
				domObj.setAttributeValue(context,
						ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_TYPE,
						strNewAttributeValue);
			} catch (FrameworkException e) {			
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}	
		}
		/**
		 * This method is return the value for Key-In type  column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
		public static StringList getKeyInType (Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strKeyInType = "";
			    String strLanguage = context.getSession().getLanguage();
			    String KeyInTypeDate=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Key-In_Type.Date", strLanguage)).trim();
			    String KeyInTypeTestArea=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Key-In_Type.Text_Area", strLanguage)).trim();
			    String KeyInTypeInteger=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Key-In_Type.Integer", strLanguage)).trim();
			    String KeyInTypeReal=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Key-In_Type.Real", strLanguage)).trim();
			    String KeyInTypeInput=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Key-In_Type.Input", strLanguage)).trim();
			    String KeyInTypeBlank=(EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Key-In_Type.Blank", strLanguage)).trim();
			    
				StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					//strKeyInType =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_KEYIN_TYPE);
					outerMap = (Map)objectMap.get(i);
					if (outerMap
							.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_KEYIN_TYPE)
							&& ProductLineCommon
							.isNotNull((String) outerMap
									.get(ConfigurationConstants.SELECT_ATTRIBUTE_KEYIN_TYPE))){					
						strKeyInType =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_KEYIN_TYPE);
					}else if (outerMap
							.containsKey(DomainObject.SELECT_ID)
							&& ProductLineCommon
							.isNotNull((String) outerMap
									.get(DomainObject.SELECT_ID))){
						DomainObject domObj = DomainObject.newInstance(context, (String) outerMap
								.get(DomainObject.SELECT_ID));
						strKeyInType=domObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE);
					}
					String strI18Value ="";
					if (strKeyInType != null) {
						if (strKeyInType
								.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_DATE)) {
							strI18Value = KeyInTypeDate;

						} else if (strKeyInType
								.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_TEXTAREA)) {
							strI18Value = KeyInTypeTestArea;
						}
						else if (strKeyInType
								.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_INTEGER)) {
							strI18Value = KeyInTypeInteger;
						}
						else if (strKeyInType
								.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REAL)) {
							strI18Value = KeyInTypeReal;
						}
						else if (strKeyInType
								.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_INPUT)) {
							strI18Value = KeyInTypeInput;
						}
						else if (strKeyInType
								.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_BLANK)) {
							strI18Value = KeyInTypeBlank;
						}

					}
					if(!("".equalsIgnoreCase(strI18Value) || "null".equalsIgnoreCase(strI18Value)|| strI18Value == null))
					{
						returnStringList.addElement(strI18Value);
					}else {
						returnStringList.addElement("");
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
	 public Map getRangeValuesForKeyInType(Context context,String[] args) throws Exception 
	 {
	     String strAttributeName = ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE;
	     HashMap rangeMap = new HashMap();
	     matrix.db.AttributeType attribName = new matrix.db.AttributeType(
	             strAttributeName);
	     attribName.open(context);

	     List attributeRange = attribName.getChoices();
	     List attributeDisplayRange = i18nNow
	             .getAttrRangeI18NStringList(
	                     ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE,
	                     (StringList) attributeRange, context.getSession()
	                             .getLanguage());
	     rangeMap.put(FIELD_CHOICES, attributeRange);
	     rangeMap.put(FIELD_DISPLAY_CHOICES, attributeDisplayRange);
	     return rangeMap;
	 }
	 /**
		 * This method is an Update Function on edit of Key-In Type Value
		 * @param context
		 * @param args
		 * @throws Exception
		 */
		public void updateKeyInTypeValue(Context context, String[] args) throws Exception 
		{

			try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				HashMap paramMap = (HashMap) programMap.get("paramMap");
				String strObjId = (String) paramMap.get("objectId");
				String strNewAttributeValue = (String) paramMap.get("New Value");
				DomainObject domObj = new DomainObject(strObjId);
				domObj.setAttributeValue(context,
						ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE,
						strNewAttributeValue);
			} catch (FrameworkException e) {			
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}	
		}
		/**
		 * This method is return the value for RDO  column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
		public static StringList getRDO(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				
				String strObjId = "";	
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	     objectSelects.add("altowner1");
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					String strRDO = "";	
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strRDO =  (String)outerMap.get("altowner1");
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strRDO=(String)rootMap.get("altowner1");
			            returnStringList.add(strRDO);
					}
					else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strRDO=(String)rootMap.get("altowner1");
			            returnStringList.add(strRDO);
					}
					returnStringList.add(strRDO);
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		/**
		 * This method is return the value for Sequence Number  column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
		public static StringList getSequenceNumber(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strSQOrder ="" ;
				StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
				if (outerMap
						.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(ConfigurationConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER))){
					strSQOrder =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
				}else if (outerMap
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) outerMap
										.get(DomainRelationship.SELECT_ID))){
					DomainRelationship domRel = new DomainRelationship((String) outerMap
							.get(DomainRelationship.SELECT_ID));
					strSQOrder=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER);
				}
					returnStringList.add(strSQOrder);
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		  /**
		 * This method is an Update Function on edit of Sequence Number
		 * @param context
		 * @param args
		 * @throws Exception
		 */
		public void updateSequenceNumber(Context context, String[] args) throws Exception 
		{

			try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				HashMap paramMap = (HashMap) programMap.get("paramMap");
				String strRelId = (String) paramMap.get("relId");
				String strNewAttributeValue = (String) paramMap.get("New Value");
				DomainRelationship domRel = new DomainRelationship(strRelId);
				domRel.setAttributeValue(context,
						ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER,
						strNewAttributeValue);
			} catch (FrameworkException e) {			
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}	
		}
		/**
		 * This method is return the value for Default Selection  column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
		public static StringList getDefaultSelection(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strDefaultSelection ="" ;
			    StringList returnStringList = new StringList (objectMap.size());
			    String i18nValueDefaultSelectionYes=EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Range.Dafault_Selection.Yes",context.getSession().getLanguage());
			    String i18nValueDefaultSelectionNo=EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Range.Dafault_Selection.No",context.getSession().getLanguage());
			    
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);
					String strfeatureId = (String) outerMap.get(DomainConstants.SELECT_ID);
		            String strfeatureType = (String) outerMap.get(DomainConstants.SELECT_TYPE);
		            StringList cfSubTypes = ProductLineUtil.getChildTypesIncludingSelf(context, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE);
					if (outerMap
							.containsKey(ConfigurationConstants.SELECT_DEFAULT_SELECTION)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(ConfigurationConstants.SELECT_DEFAULT_SELECTION))){
						
						
			             if(strfeatureType!=null && cfSubTypes.contains(strfeatureType))
			             {
			            	 strDefaultSelection =  " ";
			             }
			             else
			             {
			            	 strDefaultSelection =  (String)outerMap.get(ConfigurationConstants.SELECT_DEFAULT_SELECTION);
			            	 //set i18n Value
			            	 if(ConfigurationConstants.RANGE_VALUE_DEFAULT_SELECTION_YES.equalsIgnoreCase(strDefaultSelection))
			            		 strDefaultSelection=i18nValueDefaultSelectionYes;
			            	 else
			            		 strDefaultSelection=i18nValueDefaultSelectionNo;
			             }
					}else if (outerMap
							.containsKey(DomainRelationship.SELECT_ID)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(DomainRelationship.SELECT_ID))){
						DomainRelationship domRel = new DomainRelationship((String) outerMap
								.get(DomainRelationship.SELECT_ID));
						strDefaultSelection=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION);
		            	 //set i18n Value
		            	 if(ConfigurationConstants.RANGE_VALUE_DEFAULT_SELECTION_YES.equalsIgnoreCase(strDefaultSelection))
		            		 strDefaultSelection=i18nValueDefaultSelectionYes;
		            	 else
		            		 strDefaultSelection=i18nValueDefaultSelectionNo;
						
					}
					returnStringList.add(strDefaultSelection);
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		 public Map getRangeValuesForDefaultSelection(Context context,String[] args) throws Exception 
		 {
		     String strAttributeName = ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION;
		     HashMap rangeMap = new HashMap();
		     matrix.db.AttributeType attribName = new matrix.db.AttributeType(
		             strAttributeName);
		     attribName.open(context);

		     List attributeRange = attribName.getChoices();
		     List attributeDisplayRange = i18nNow
		             .getAttrRangeI18NStringList(
		                     ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION,
		                     (StringList) attributeRange, context.getSession()
		                             .getLanguage());
		     rangeMap.put(FIELD_CHOICES, attributeRange);
		     rangeMap.put(FIELD_DISPLAY_CHOICES, attributeDisplayRange);
		     return rangeMap;
		 }
		    /**
			 * This method is an Update Function on edit of Selection Criteria
			 * @param context
			 * @param args
			 * @throws Exception
			 */
			public void updateDefaultSelection(Context context, String[] args) throws Exception 
			{

				try {
					HashMap programMap = (HashMap) JPO.unpackArgs(args);
					HashMap paramMap = (HashMap) programMap.get("paramMap");
					String strRelId = (String) paramMap.get("relId");
					String strNewAttributeValue = (String) paramMap.get("New Value");
					DomainRelationship domRel = new DomainRelationship(strRelId);
					domRel.setAttributeValue(context,
							ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION,
							strNewAttributeValue);
				} catch (FrameworkException e) {			
					e.printStackTrace();
					throw new FrameworkException(e.getMessage());
				}	
			}
			/**
			 * This method is return the value for List Price  column 
			 * @param context
			 *            The ematrix context object.
			 * @param String[]
			 *            The args .
			 * @return StringList.	 
			 * @throws Exception
			 */
		public static StringList getListPrice(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strListPrice ="" ;
				StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					//strListPrice =  (String)outerMap.get(ConfigurationConstants.SELECT_LIST_PRICE);
					if (outerMap
							.containsKey(ConfigurationConstants.SELECT_LIST_PRICE)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(ConfigurationConstants.SELECT_LIST_PRICE))){
						strListPrice =  (String)outerMap.get(ConfigurationConstants.SELECT_LIST_PRICE);
					}else if (outerMap
							.containsKey(DomainRelationship.SELECT_ID)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(DomainRelationship.SELECT_ID))){
						DomainRelationship domRel = new DomainRelationship((String) outerMap
								.get(DomainRelationship.SELECT_ID));
						strListPrice=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_LIST_PRICE);
					}	
					returnStringList.add(strListPrice);
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		  /**
		 * This method is an Update Function on edit of List Price
		 * @param context
		 * @param args
		 * @throws Exception
		 */
		public void updateListPrice(Context context, String[] args) throws Exception 
		{

			try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				HashMap paramMap = (HashMap) programMap.get("paramMap");
				String strRelId = (String) paramMap.get("relId");
				String strNewAttributeValue = (String) paramMap.get("New Value");
				DomainRelationship domRel = new DomainRelationship(strRelId);
				domRel.setAttributeValue(context,
						ConfigurationConstants.ATTRIBUTE_LIST_PRICE,
						strNewAttributeValue);
			} catch (FrameworkException e) {			
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}	
		}
		/**
		 * This method is return the value for Minimum Quantity  column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
		public static StringList getMinimumQuantity(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strMinimumQuantity ="" ;
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					//strMinimumQuantity =  (String)outerMap.get(ConfigurationConstants.SELECT_MINIMUM_QUANTITY);
					if (outerMap
							.containsKey(ConfigurationConstants.SELECT_MINIMUM_QUANTITY)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(ConfigurationConstants.SELECT_MINIMUM_QUANTITY))){
						strMinimumQuantity =  (String)outerMap.get(ConfigurationConstants.SELECT_MINIMUM_QUANTITY);
					}else if (outerMap
							.containsKey(DomainRelationship.SELECT_ID)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(DomainRelationship.SELECT_ID))){
						DomainRelationship domRel = new DomainRelationship((String) outerMap
								.get(DomainRelationship.SELECT_ID));
						strMinimumQuantity=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY);
					}
					returnStringList.add(strMinimumQuantity);
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		  /**
		 * This method is an Update Function on edit of Minimum Quantity
		 * @param context
		 * @param args
		 * @throws Exception
		 */
		public void updateMinimumQuantity(Context context, String[] args) throws Exception 
		{

			try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				HashMap paramMap = (HashMap) programMap.get("paramMap");
				String strRelId = (String) paramMap.get("relId");
				String strNewAttributeValue = (String) paramMap.get("New Value");
				DomainRelationship domRel = new DomainRelationship(strRelId);
				domRel.setAttributeValue(context,
						ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY,
						strNewAttributeValue);
			} catch (FrameworkException e) {			
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}	
		}
		/**
		 * This method is return the value for Maximum  Quantity  column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
		public static StringList getMaximumQuantity(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strMaximumQuantity ="" ;
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					//strMaximumQuantity =  (String)outerMap.get(ConfigurationConstants.SELECT_MAXIMUM_QUANTITY);
					if (outerMap
							.containsKey(ConfigurationConstants.SELECT_MAXIMUM_QUANTITY)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(ConfigurationConstants.SELECT_MAXIMUM_QUANTITY))){
						strMaximumQuantity =  (String)outerMap.get(ConfigurationConstants.SELECT_MAXIMUM_QUANTITY);
					}else if (outerMap
							.containsKey(DomainRelationship.SELECT_ID)
							&& ProductLineCommon
									.isNotNull((String) outerMap
											.get(DomainRelationship.SELECT_ID))){
						DomainRelationship domRel = new DomainRelationship((String) outerMap
								.get(DomainRelationship.SELECT_ID));
						strMaximumQuantity=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY);
					}
					returnStringList.add(strMaximumQuantity);
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		  /**
		 * This method is an Update Function on edit of Maximum Quantity
		 * @param context
		 * @param args
		 * @throws Exception
		 */
		public void updateMaximumQuantity(Context context, String[] args) throws Exception 
		{

			try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				HashMap paramMap = (HashMap) programMap.get("paramMap");
				String strRelId = (String) paramMap.get("relId");
				String strNewAttributeValue = (String) paramMap.get("New Value");
				DomainRelationship domRel = new DomainRelationship(strRelId);
				domRel.setAttributeValue(context,
						ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY,
						strNewAttributeValue);
			} catch (FrameworkException e) {			
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}	
		}
		/**
		 * This method is return the value for Display Text column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
		public static StringList getDisplayText(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strDisplayText ="" ;	
				String strObjId="";
				//DomainObject domObj =null;
			    // selectable for root node  
			   	StringList objectSelects = new StringList();	    
		 	    objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					if (outerMap.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT) && ProductLineCommon.isNotNull((String) outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT))){
						strDisplayText =  (String)outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT);
					}else if (outerMap.containsKey(DomainConstants.SELECT_ID) && ProductLineCommon.isNotNull((String) outerMap.get(DomainConstants.SELECT_ID))){
						DomainObject domObj = new DomainObject((String) outerMap.get(DomainConstants.SELECT_ID));
						strDisplayText= domObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT);
					}
					returnStringList.add(strDisplayText);
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		  /**
		 * This method is an Update Function on edit of Display Text
		 * @param context
		 * @param args
		 * @throws Exception
		 */
		public void updateDisplayText(Context context, String[] args) throws Exception 
		{

			try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				HashMap paramMap = (HashMap) programMap.get("paramMap");
				String strObjId = (String) paramMap.get("objectId");
				String strNewAttributeValue = (String) paramMap.get("New Value");
				DomainObject domObj = new DomainObject(strObjId);
				domObj.setAttributeValue(context,
						ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT,
						strNewAttributeValue);
			} catch (FrameworkException e) {			
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}	
		}
		/**
		 * This method is return the value for Display Name column 
		 * @param context
		 *            The ematrix context object.
		 * @param String[]
		 *            The args .
		 * @return StringList.	 
		 * @throws Exception
		 */
		public static StringList getDisplayName(Context context, String[] args) throws FrameworkException{
			try {
				HashMap inputMap = (HashMap) JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strDisplayName 	= ConfigurationConstants.EMPTY_STRING;
				String strObjId			=ConfigurationConstants.EMPTY_STRING;
				String strRev			=ConfigurationConstants.EMPTY_STRING;
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
				objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
				objectSelects.add(ConfigurationConstants.SELECT_REVISION);
				StringList returnStringList = new StringList(objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map) objectMap.get(i);
					strDisplayName 		= (String) outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
					String strMarkName	=(String) outerMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
					strRev				= (String) outerMap.get(ConfigurationConstants.SELECT_REVISION);
					String isRootNode = (String) outerMap.get("Root Node");
					if ("true".equals(isRootNode) || isRootNode==null && strDisplayName==null && strMarkName==null ){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strDisplayName=(String)rootMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
						String strMarketingName = (String) rootMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
						strRev				= (String) rootMap.get(ConfigurationConstants.SELECT_REVISION);
						if (strDisplayName == null || strDisplayName.equals("")) {
							strDisplayName = strMarketingName;
						}
						returnStringList.add(strDisplayName + " " + strRev);
						
					}
					else{			
						returnStringList.add(strDisplayName + " " + strRev);
					}
				}
				return returnStringList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		 /** 
		  * Method to get the  "Name" column 
		  * @param context
		  * @param args
		  * @return
		  * @throws FrameworkException
		  */
		public static StringList getName(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strName 	= ConfigurationConstants.EMPTY_STRING;
				String strObjId	= ConfigurationConstants.EMPTY_STRING;
				String strRev	= ConfigurationConstants.EMPTY_STRING;
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(DomainConstants.SELECT_NAME);
				objectSelects.add(DomainConstants.SELECT_REVISION);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strName =  (String)outerMap.get(ConfigurationConstants.SELECT_NAME);
					strRev	=  (String)outerMap.get(ConfigurationConstants.SELECT_REVISION);
					String isRootNode = (String) outerMap.get("Root Node");
					if ("true".equalsIgnoreCase(isRootNode) || isRootNode==null && strName==null ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strName=(String)rootMap.get(DomainConstants.SELECT_NAME);
			            strRev	=  (String)rootMap.get(ConfigurationConstants.SELECT_REVISION);
			            returnStringList.add(strName + " " + strRev);					
					} 
					else{
					returnStringList.add(strName + " " + strRev);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		 /** 
		  * Method to get the  "Revision"  column 
		  * @param context
		  * @param args
		  * @return
		  * @throws FrameworkException
		  */
		public static StringList getRevisionForProductContext(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strRevision ="" ;
				String strObjId="";
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(DomainConstants.SELECT_REVISION);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strRevision =  (String)outerMap.get(ConfigurationConstants.SELECT_REVISION);
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strRevision=(String)rootMap.get(DomainConstants.SELECT_REVISION);
				            returnStringList.add(strRevision);
					}else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strRevision=(String)rootMap.get(DomainConstants.SELECT_REVISION);
			            returnStringList.add(strRevision);
					}else{
					returnStringList.add(strRevision);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		/** 
		 * Method to get the type column 
		 * @param context
		 * @param args
		 * @return
		 * @throws FrameworkException
		 */
		public static StringList getType(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strType ="" ;
				String strObjId="";
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(DomainConstants.SELECT_TYPE);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strType =  (String)outerMap.get(ConfigurationConstants.SELECT_TYPE);
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strType=(String)rootMap.get(DomainConstants.SELECT_TYPE);
				            returnStringList.add(strType);
					}else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strType=(String)rootMap.get(DomainConstants.SELECT_TYPE);
			            returnStringList.add(strType);
					}
					else{
					returnStringList.add(strType);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		 /** 
		  * Method to get the  "State"  column 
		  * @param context
		  * @param args
		  * @return
		  * @throws FrameworkException
		  */
		public static StringList getState(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strLanguage = context.getSession().getLanguage();
				String strState ="" ;	
				String strPolicy ="" ;	
				String strObjId="";
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	     objectSelects.add(DomainConstants.SELECT_CURRENT);
		 	     objectSelects.add(DomainConstants.SELECT_POLICY);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strState =  (String)outerMap.get(ConfigurationConstants.SELECT_CURRENT);
					strPolicy =  (String)outerMap.get(ConfigurationConstants.SELECT_POLICY);
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strState=(String)rootMap.get(DomainConstants.SELECT_CURRENT);
				            strPolicy=(String)rootMap.get(DomainConstants.SELECT_POLICY);
				            strState= EnoviaResourceBundle.getStateI18NString(context, strPolicy, strState, strLanguage);
				            returnStringList.add(strState);
					}else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strState=(String)rootMap.get(DomainConstants.SELECT_CURRENT);
			            strPolicy=(String)rootMap.get(DomainConstants.SELECT_POLICY);
			            strState= EnoviaResourceBundle.getStateI18NString(context, strPolicy, strState, strLanguage);
			            returnStringList.add(strState);
					}else{
						strState= EnoviaResourceBundle.getStateI18NString(context, strPolicy, strState, strLanguage);
						returnStringList.add(strState);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
		 /** 
		  * Method to get the  "Owner"  column 
		  * @param context
		  * @param args
		  * @return
		  * @throws FrameworkException
		  */
		public static StringList getOwner(Context context, String[] args) throws FrameworkException{
			try{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				MapList objectMap = (MapList) inputMap.get("objectList");
				String strOwner ="" ;
				String strObjId="";
				DomainObject domObj=null;
			      // selectable for root node  
			   	 StringList objectSelects = new StringList();	    
		 	      objectSelects.add(DomainConstants.SELECT_OWNER);
			    StringList returnStringList = new StringList (objectMap.size());
				for (int i = 0; i < objectMap.size(); i++) {
					Map outerMap = new HashMap();
					outerMap = (Map)objectMap.get(i);							
					strOwner =  (String)outerMap.get(ConfigurationConstants.SELECT_OWNER);
					String isRootNode = (String) outerMap.get("Root Node");
					if(isRootNode!=null && isRootNode.equalsIgnoreCase("true") ){
		            	 strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
				            domObj = DomainObject.newInstance(context, strObjId);				      
				            Map rootMap= domObj.getInfo(context,objectSelects);
				            strOwner=(String)rootMap.get(DomainConstants.SELECT_OWNER);
				        	returnStringList.add(strOwner);
					}
					else if(outerMap.containsKey("parentLevel")){
						strObjId = (String)outerMap.get(DomainConstants.SELECT_ID);
			            domObj = DomainObject.newInstance(context, strObjId);				      
			            Map rootMap= domObj.getInfo(context,objectSelects);
			            strOwner=(String)rootMap.get(DomainConstants.SELECT_OWNER);
			            returnStringList.add(strOwner);
					}else{
					returnStringList.add(strOwner);
					}
				}
				return returnStringList;
			}catch(Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e.getMessage());
			}
		}
	/**
	 * Trigger will be invoked on Configuration Features Delete Action. It will remove the Committed Context relationship
	 * and Change the Committed Configuration Features relation to Candidate Configuration Features relation.
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 */
	 public void removeCommittedContextForConfigurationFeatures(Context context, String args[]) throws FrameworkException {
		 String objectID=args[1];
		 String configurationFeatureID=args[2];
		 MapList mapModelStructure = null;
		 String modelID = null;
		 List committedContextRelList = new StringList();
		 StringList relList = new StringList();
		 StringList relIDList = new StringList();
		 
		 try{		 
		 	StringList objectSelects = new StringList();
			objectSelects.add(DomainObject.SELECT_ID);
			objectSelects.add(DomainObject.SELECT_TYPE);
			objectSelects.add(DomainObject.SELECT_NAME);			
			
			StringList relationshipSelects = new StringList();
			relationshipSelects.add(DomainRelationship.SELECT_ID);
			relationshipSelects.add(DomainRelationship.SELECT_TYPE);
			relationshipSelects.add("frommid["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+ "].id");			
			
			StringBuffer strModelRelPattern = new StringBuffer(ConfigurationConstants.RELATIONSHIP_COMMITTED_CONFIGURATION_FEATURES);
			StringBuffer strwherePattern = new StringBuffer("id=="+configurationFeatureID);		  	
			
			DomainObject domContextObj = new DomainObject(objectID);
			
			if(domContextObj.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS)){
				
				if( ! ProductLineUtil.isCommittedContextOnProduct(context, domContextObj, ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT) )
					return;
				
			 final String SELECT_PARENT_PRODUCT = ("to["+ ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
			 final String SELECT_PARENT_MAIN_PRODUCT = ("to["+ ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");			 
			 final String SELECT_COMMITTED_CONTEXT_REL = ("to["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+"].id");

			 StringList selectables = new StringList();
			 selectables.add(SELECT_PARENT_PRODUCT);
			 selectables.add(SELECT_PARENT_MAIN_PRODUCT);
			 selectables.add(SELECT_COMMITTED_CONTEXT_REL);
			 selectables.add(DomainObject.SELECT_TYPE);
			 
			 DomainConstants.MULTI_VALUE_LIST.add(SELECT_COMMITTED_CONTEXT_REL);
			 Map mapContext = domContextObj.getInfo(context, selectables);
			 DomainConstants.MULTI_VALUE_LIST.remove(SELECT_COMMITTED_CONTEXT_REL);			 
			
			 if(mapContext.containsKey(SELECT_PARENT_PRODUCT))
		
					modelID = (String)mapContext.get(SELECT_PARENT_PRODUCT);
		
			 else if(mapContext.containsKey(SELECT_PARENT_MAIN_PRODUCT))
		
					modelID = (String)mapContext.get(SELECT_PARENT_MAIN_PRODUCT);
			 
			 if(modelID == null)
				 return;
		
			 if(mapContext.containsKey(SELECT_COMMITTED_CONTEXT_REL))
			 {
						relList=  (StringList) mapContext.get(SELECT_COMMITTED_CONTEXT_REL);
			 }
					
			 DomainObject modelContextObj = new DomainObject(modelID);
			 mapModelStructure = modelContextObj.getRelatedObjects(context, strModelRelPattern.toString(), DomainObject.QUERY_WILDCARD,
							  	objectSelects, relationshipSelects,
							  	false, true, (short) 0, 
							  	strwherePattern.toString(), DomainConstants.EMPTY_STRING, 0);
			 
			for(int i=0; i<mapModelStructure.size(); i++){
					
					String relationshipId = (String)((Map)mapModelStructure.get(i)).get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
						if(relationshipId!=null && !relationshipId.equals("")){
							
							  Object obj = (Object)((Map)mapModelStructure.get(i)).get("frommid["+ ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+ "].id");
							  if(obj instanceof String){
								  committedContextRelList.add((String)obj);
							  }else if(obj instanceof StringList){
								  committedContextRelList.addAll((StringList) obj);
							  }						 
							  int commitConfFeatureCount = committedContextRelList.size();							 
							  for(int j=0 ; j<commitConfFeatureCount ; j++){
								  if(relList.contains(committedContextRelList.get(j))){
									  relIDList.add(committedContextRelList.get(j));							  
								  }
							  }
							  if(commitConfFeatureCount==1){		
									DomainRelationship.setType(context, relationshipId, ConfigurationConstants.RELATIONSHIP_CANDIDTAE_CONFIGURATION_FEATURES);
							  }
							  String[] relIDs = new String[relIDList.size()]; 
							  for(int k=0; k<relIDList.size() ;k++){
									relIDs[k]=(String) relIDList.get(k);
							  }
							  DomainRelationship.disconnect(context, relIDs);
						}
					}
				}
		 	}catch(Exception ex){
				throw new FrameworkException(ex);
		 	}
	 	}
	 /**
	     * This method is used to  copy inclusion rule  to new LF revision
	     *
	     * @param context :
	     *              The eMatrix Context object
	     * @param featureId :
	     *              The source feature Object id
	     * @exception Exception :
	     *              Throws exception if operation fails
	     * @since R417	    
	     */
	    public void connectIROnRevise(Context context, String args[])
	        throws FrameworkException {
	        String featureId = args[0];
	        
	        BusinessObject nextRevision 	= null;
	        
	        DomainObject contextFeature 	= null;
	        DomainObject newRevision 		= null;

	        HashMap Ohm = new HashMap();	
	        HashMap Nhm = new HashMap();	

	        Map tempMap 			= new HashMap();
	        MapList tempMapList 	= null;
	        MapList tempMapListOld = null;
	        MapList tempMapListNew = null;
	        Map tempMapOld 		= null;
	        Map tempMapNew 		= null;

	        String strOldIRId 		= null;
	        String strOldId 	= null;
	        String stNewRelId = null;
	        
	        StringList slRelSelects 	= new StringList();
	        StringList slObjSelects 	= new StringList();

	        RelationshipType reltype = new RelationshipType(ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION);
	        
	        // Relationship selectables
	        slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
	        slRelSelects.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.id");
	        slRelSelects.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type");
	        
	        // Object selectables
	        slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
	        
	        try {
	       	 contextFeature = new DomainObject(featureId);	           
	       	 nextRevision = contextFeature.getNextRevision(context);
	          newRevision = new DomainObject(nextRevision.getObjectId());
	            
	            ContextUtil.startTransaction(context, true);
				  //relPattern need to update for child level LF rels IR and GBOM IR
					String strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES
						+ "," + ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS;
	            
	            MapList oldIROnCFList 	= 	contextFeature.getRelatedObjects(context,
										strRelPattern, 	// Rel pattern
										DomainConstants.QUERY_WILDCARD,				// Type pattern
										slObjSelects, 								// Object selectables
										slRelSelects,								// Relationship selectables
										false,										// From side
										true, 										// To side
										(short) 1, 	 									// Expand level
										DomainConstants.EMPTY_STRING,				// Object where
										DomainConstants.EMPTY_STRING,
										0);				// Relationship where
				
	            MapList newIROnCFList = 	newRevision.getRelatedObjects(context,
	           						strRelPattern,	// Rel pattern
	           					 	DomainConstants.QUERY_WILDCARD,				// Type pattern
	           					 	slObjSelects,								// Object selectables
	           					 	slRelSelects, 								// Relationship selectables
	           					 	false,										// From side
	           					 	true, 										// To side
	           					 	(short) 1,									// Expand level
	           					 	DomainConstants.EMPTY_STRING,				// Object where
	           					 	DomainConstants.EMPTY_STRING,
									0);				// Relationship where

	            Iterator itrOld = oldIROnCFList.iterator();
	            
	            while (itrOld.hasNext()){	            	 
	           	 tempMap = (Map) itrOld.next();
	           	 strOldId 	= (String)tempMap.get(ConfigurationConstants.SELECT_ID); 
	           	 if(Ohm.containsKey((strOldId))){
	           		 tempMapList = (MapList)Ohm.get(strOldId);
	           		 tempMapList.add(tempMap);
	           		 Ohm.put(strOldId, tempMapList);
	           	 }
	           	 else{
	           		 tempMapList = new MapList();
	           		 tempMapList.add(tempMap);
	           		 Ohm.put(strOldId, tempMapList);
	           	 }
	            }
	           		 
	            Iterator itrNew = newIROnCFList.iterator();
	            while (itrNew.hasNext()){	            	 
	           	 tempMap = (Map) itrNew.next();
	           	 strOldId 	= (String)tempMap.get(ConfigurationConstants.SELECT_ID); 
	           	 if(Nhm.containsKey((strOldId))){
	           		 tempMapList = (MapList)Nhm.get(strOldId);
	           		 tempMapList.add(tempMap);
	           		 Nhm.put(strOldId, tempMapList);
	           	 }
	           	 else{
	           		 tempMapList = new MapList();
	           		 tempMapList.add(tempMap);
	           		 Nhm.put(strOldId, tempMapList);
	           	 }
	            }
	            
	            assert(Nhm.size() == Ohm.size());
	            
	            Set partKeySet = Ohm.keySet();
	            Iterator setItr = partKeySet.iterator();
	            
		             while(setItr.hasNext()){
					  strOldIRId="";
		            	 String Id = (String)setItr.next();
		            	 
		            	 tempMapListOld = (MapList)Ohm.get(Id);
		            	 tempMapListNew = (MapList)Nhm.get(Id);
						             	 
		            	 if(tempMapListOld!= null && tempMapListNew!= null){
		            		 for(int i=0; i< tempMapListOld.size(); i++){
		            			 tempMapOld = (Map)tempMapListOld.get(i);
		            			 tempMapNew = (Map)tempMapListNew.get(i);
								 
								  Object objOldIRId = tempMapOld.get("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.id");
		            			 StringList slIds=ConfigurationUtil.convertObjToStringList(context,objOldIRId);
		            			 Object objOldIRType = tempMapOld.get("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type");
		            			 StringList slType=ConfigurationUtil.convertObjToStringList(context,objOldIRType);

		            			 for(int j=0; j< slType.size();j++){
		            				 String strType=(String)slType.get(j);
		            				 if(mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_INCLUSION_RULE)){
		            					 strOldIRId=(String)slIds.get(j);
		            					 break;
		            				 }
		            			 }
		            			 if(ProductLineCommon.isNotNull(strOldIRId)){
		            				 stNewRelId = (String)tempMapNew.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
		            			 InclusionRule IR = new InclusionRule(strOldIRId);
				            	 IR.copyInclusionRules(context, stNewRelId);
								 }
		            		 }
		            	 }
		             }
	        	} catch (Exception e) {
	        		ContextUtil.abortTransaction(context);
	        		throw new FrameworkException(e);
	        	}
	    	}

	    /**
	     * Method shows Active EC Icon if an object has an EC associated with it.
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds no arguments
	     * @return List - returns the program HTML output
	     * @throws Exception if the operation fails
	     * @since R418 CFs Active EC Icons
	     */
	    public List getActiveECIcon(Context context, String[] args) throws Exception{

	    	Map programMap = (HashMap) JPO.unpackArgs(args);
	    	MapList relBusObjPageList = (MapList) programMap.get("objectList");

	    	Map paramList = (HashMap)programMap.get("paramList");
	    	String reportFormat = (String)paramList.get("reportFormat");

	    	int iNumOfObjects = relBusObjPageList.size();
	    	// The List to be returned
	    	List lstActiveECIcon= new Vector(iNumOfObjects);
	    	
	    	String strIcon = EnoviaResourceBundle.getProperty(context,"emxComponents.ActiveECImage");
	    	// Retrieving  relationship,Policy,Policy states using symbolic names
	    	String strClose = FrameworkUtil.lookupStateName(context, EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_CLOSE);
	    	String strReject = FrameworkUtil.lookupStateName(context,EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_REJECT);
	    	String strComplete = FrameworkUtil.lookupStateName(context,EngineeringChange.POLICY_ENGINEERING_CHANGE_STANDARD, EngineeringChange.EC_STATE_COMPLETE);
	    	//List of selectables
	    	String strStateSelect ="to["+ DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM +"].from."+ DomainConstants.SELECT_CURRENT;

	    	//Reading the tooltip from property file.
	    	String strTooltipActiveECIcon = EnoviaResourceBundle.getProperty(context,
	    			"emxComponentsStringResource",
	    			context.getLocale(),"emxComponents.EngineeringChange.ToolTipActiveECExists");

	    	StringList returnStringList = new StringList (relBusObjPageList.size());
	    	for (int i = 0; i < relBusObjPageList.size(); i++) {
	    		Map outerMap = new HashMap();
	    		outerMap = (Map)relBusObjPageList.get(i);
	    		String strActiveECIconTag = "";
	    		if(outerMap.containsKey(strStateSelect)){
	    			StringList strStateList = ConfigurationUtil.convertObjToStringList(context,outerMap.get(strStateSelect));
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
	    		}
	    		lstActiveECIcon.add(strActiveECIconTag);
	    	}
	    	return lstActiveECIcon;
	    }
	    /**
	     * Expand Program called in PL-> CF context, refined Selectables which are only used in PL context.
	     * @param context
	     * @param args
	     * @return
	     * @throws FrameworkException
	     */
		@com.matrixone.apps.framework.ui.ProgramCallable
		public MapList getConfigurationFeatureStructureForPLContext(Context context, String[] args)	
				throws FrameworkException {	
			MapList mapConfigurationStructure = null;
			StringBuffer strObjWhere = new StringBuffer();	
			try{	

				Map programMap = (Map) JPO.unpackArgs(args);
				String strObjectId = (String) programMap.get("objectId");
				// call method to get the level details
				int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
				int limit = 0;// Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.ExpandLimit"));

				String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");
				String strTypePattern = ConfigurationConstants.TYPE_CONFIGURATION_FEATURES;
				String strRelPattern = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES;
				StringList slObjSelects = new StringList();
				//Object Select
				slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
				slObjSelects.addElement(ConfigurationConstants.SELECT_TYPE);
				slObjSelects.addElement(ConfigurationConstants.SELECT_NAME);
				slObjSelects.addElement(ConfigurationConstants.SELECT_REVISION);
				slObjSelects.addElement(ConfigurationConstants.SELECT_CURRENT);
				slObjSelects.addElement(ConfigurationConstants.SELECT_POLICY);
				slObjSelects.addElement(ConfigurationConstants.SELECT_OWNER);
				slObjSelects.addElement("next");
				slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
				slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
				slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_SELECTION_TYPE);
				slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_KEYIN_TYPE);
				slObjSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_VARIANT_DISPLAY_TEXT);
				slObjSelects.addElement("altowner1");
				StringList slRelSelects = new StringList();
				slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
				slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_TYPE);
				slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);                     
				ConfigurationFeature cfBean = new ConfigurationFeature(strObjectId); 
				// rel select
				slRelSelects.addElement("tomid["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"].from["+ConfigurationConstants.TYPE_INCLUSION_RULE+"].id");
				slRelSelects.addElement("tomid["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"].from["+ConfigurationConstants.TYPE_INCLUSION_RULE+"].attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"].value");
				slRelSelects.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
				slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA+ "]");
				slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE+ "]");
				slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]");
				slRelSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");

				String ecICONSelect ="to["+ DomainConstants.RELATIONSHIP_EC_AFFECTED_ITEM +"].from."+ DomainConstants.SELECT_CURRENT;
				slObjSelects.addElement(ecICONSelect);



				mapConfigurationStructure = (MapList)cfBean.getConfigurationFeatureStructure(context,strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
						true,iLevel,limit, null, null, DomainObject.FILTER_STR_AND_ITEM,filterExpression);			

			}catch (Exception e) {
				throw new FrameworkException(e);
			}
			return mapConfigurationStructure;
		}	
       
		/**
		 * This method is used to keep Default Selection Editable For Configuation Options and Non-Editable For Configuration Features.
		 * @param context
		 * @param String[]
		 * @return StringList
		 * @throws FrameworkException
		 */
		
		public static StringList isDefaultSelectionEditableForConfigurationObject(Context context, String[] args) throws FrameworkException 
		{
			try
			{
				HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		        MapList objectMap = (MapList) inputMap.get("objectList");
				StringList strReturnStringList = new StringList(objectMap.size());
				
				for(int i = 0; i < objectMap.size(); i++)
				{
					String strfeatureId = (String) ((Map) objectMap.get(i)).get(DomainConstants.SELECT_ID);
		            String strfeatureType = (String) ((Map) objectMap.get(i)).get(DomainConstants.SELECT_TYPE);
		            StringList cfSubTypes = ProductLineUtil.getChildTypesIncludingSelf(context, ConfigurationConstants.TYPE_CONFIGURATION_FEATURE);
		            if(strfeatureType!=null && cfSubTypes.contains(strfeatureType))
		            {
		            	strReturnStringList.add("false");
		            }
		            else
		            {
		            	strReturnStringList.add("true");
		            }
				}
				return strReturnStringList;
			}
			catch(Exception e)
			{
				throw new FrameworkException(e.getMessage());
			}
		}

		/**
		 * This method is used to keep Default Selection Editable For Configuation Options and Non-Editable For Configuration Features.
		 * @param context
		 * @param String[]
		 * @return StringList
		 * @throws FrameworkException
		 */
		
		public Object isSearchConfigurationFeatureContext(Context context, String[] args)
	    throws FrameworkException
	    {
			try{
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				String[] ctx = null;
				if(programMap != null){
				HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
				
				if(requestValuesMap!=null){
					ctx = (String[])requestValuesMap.get("context");
				}
				else{
					if(programMap.containsKey("context")){
					ctx = new String[]{programMap.get("context").toString()};
					}
				}
				}

				if(ctx!= null && ctx[0] != null && ctx[0].equals("ConfigurationFeature")){
					return Boolean.valueOf(true);
				}else{
					return Boolean.valueOf(false);
				}
			}
			catch(Exception e){
				throw new FrameworkException(e);
			}
	    }
		/**
	     * Returns a MapList of selected Ids from full search results when using relational effectivity Relational View
	     *   This will expand the selected objects and return a row for each parent/child pair
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds a packed hashmap with the following arguments
	     *        RequestValuesMap HashMap of request values - emxTableRowId
	     *
	     * @throws Exception if the operation fails
	     */
		@com.matrixone.apps.framework.ui.ProgramCallable	    
	    public MapList getModelConfigurationDictionaryForEffectivity(Context context, String[]args)
	        throws Exception
	    {
	        HashMap programMap = (HashMap)JPO.unpackArgs(args);
	        HashMap requestMap=(HashMap)programMap.get("RequestValuesMap");
	        MapList mlSearchResults = new MapList();
	        
	        String strExpandLevelFilter = "";
	        if(programMap.get("emxExpandFilter")!=null){
	        	strExpandLevelFilter = (String)programMap.get("emxExpandFilter");
	        }
	        if(strExpandLevelFilter.equals("All")){
	        	strExpandLevelFilter = "0";
	        }   
	        String strObjectId = (String)programMap.get("objectId");        
	        if(!programMap.keySet().contains("parentId")){
	            String[] emxTableRowId = (String[]) requestMap.get("EFFTableRowId");
	            String selectedId = "";
	            for (int i=0; i < emxTableRowId.length; i++ )
	            {
	              //if this is coming from the Full Text Search, have to parse out |objectId|relId|
	              StringTokenizer strTokens = new StringTokenizer(emxTableRowId[i],"|");
	              if ( strTokens.hasMoreTokens())
	              {
	                  selectedId = strTokens.nextToken();
	                  Map idMap = new HashMap();
	                  idMap.put(DomainConstants.SELECT_ID, selectedId);
	                  idMap.put("level", "0");              
	                  mlSearchResults.add(idMap);
	              }
	            }
		    	 Model model = new Model(selectedId);
		    	 MapList returnList = new MapList();
		   		 returnList=model.getModelConfigurationDictionaryForFO(context,selectedId,strExpandLevelFilter,true);
		    	 mlSearchResults.addAll(returnList);
	        }else{
		    	 Model model = new Model(strObjectId);
		    	 MapList returnList = new MapList();
		   		 returnList=model.getModelConfigurationDictionaryForFO(context,strObjectId,strExpandLevelFilter,true);
		    	 mlSearchResults.addAll(returnList);
	        }

	        return mlSearchResults;
	    }
		
		/**
		* This method is used to refresh the Structure Tree On Add/Remove for Configuration Features,Configuration Options.
		* 
		* @param context
		* @param String[]
		* @return Map
		* @throws FrameworkException
		* @since R419.HF5
		*/
		
		@com.matrixone.apps.framework.ui.PostProcessCallable
		public static Map refreshTree(Context context, String[] args) throws Exception
		{
			Map returnMap = new HashMap();
			returnMap.put("Action", "execScript");
			returnMap.put("Message","{ main:function __main(){refreshTreeForAddObj(xmlResponse,'Configuration Feature')}}");
			return returnMap;
		}
}


