/*
 * emxProductConfigurationEBOMBase.java
 *
 * Copyright (c) 2004-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.53.2.4.1.1.1.3.1.1 Wed Jan 07 13:05:24 2009 GMT ds-shbehera Experimental${CLASSNAME}.java 1.19 Thu Nov 01 14:40:05 2007 GMT ds-dpathak Experimental${CLASSNAME}.java 1.11 Fri Oct 12 11:29:34 2007 GMT ds-dpathak Experimental${CLASSNAME}.java 1.4 Thu Aug 23 07:55:30 2007 GMT ds-pvoggu Experimental$
 *
 */

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.configuration.Product;
import com.matrixone.apps.configuration.ProductConfiguration;
import com.matrixone.apps.configuration.ProductConfigurationHolder;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.Job.BackgroundJob;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.jdom.output.XMLOutputter;
import com.matrixone.jdom.xpath.XPath;
import com.matrixone.util.MxXMLUtils;

/**
 * This JPO class has some method pertaining to Precise BOM relationship.
 *
 * @author WIPRO
 * @version PRC 10.5.1.2 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxProductConfigurationEBOMBase_mxJPO extends
        emxDomainObject_mxJPO implements BackgroundJob {

    /**
     * Alias used for empty string.
     */
    protected final static String STR_EMPTY = "";

    /**
     * Alias used for string attribute.
     */
    protected final static String STR_ATTRIBUTE_OPEN_BRACE = "attribute[";

    /**
     * Alias used for closing brace.
     */
    protected final static String EXPRESSION_CLOSE = "]";

    /**
     * Alias used for string TRUE.
     */
    protected final static String STR_TRUE = "TRUE";

    /**
     * Alias used for string FALSE.
     */
    protected final static String STR_FALSE = "FALSE";

    /**
     * Product Configuration Object Id to make it aviable to complete JPO.
     */
    protected String strProductConfigID = STR_EMPTY;

    /**
     * Part Family Object Id to make it aviable to complete JPO.
     */
    protected String strPartFamilyID = STR_EMPTY;

    /**
     * Alias used for string Yes.
     */
    protected final static String STR_YES = "Yes";

    /**
     * Alias used for string No.
     */
    protected final static String STR_NO = "No";

    /** Default Part Type, Default Part Policy attribute selects */
    public static String attrDefaultPartType = "attribute["
            + PropertyUtil.getSchemaProperty("attribute_DefaultPartType") + "]";

    public static String attrDefaultPartPolicy = "attribute["
            + PropertyUtil.getSchemaProperty("attribute_DefaultPartPolicy")
            + "]";

    /**
     * Feature List Object Expression to make it aviable to complete JPO.
     */
    protected String strFeatureListExpression = STR_EMPTY;

    // variavle to determing the leaf level while generating EBOM
    boolean bottomLevel = false;

    boolean atleastOnePartCreated = false;

    ArrayList arrSOptionId = new ArrayList();

    String strFeatureType = "";
    
    protected static final String STR_COMMA = ",";

    public static final String OBJECT_ID = "objectId";

    protected static Job _job = new Job();

    protected String _jobId = null;
    protected String SUITE_KEY ="Configuration";

    /**
     * Default constrctor.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     * @since PRC 10.5.1.2
     * @grade 0
     */

    public emxProductConfigurationEBOMBase_mxJPO(Context context, String[] args)
            throws Exception {
        super(context, args);

    }
    @Deprecated
    public void setJob(Job job) {
        _job = job;
    }
    @Deprecated
    public Job getJob() {
        return _job;
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
     * @since PRC 10.5.1.2
     * @grade 0
     */

    public int mxMain(Context context, String[] args) throws Exception {
        String strLanguage = context.getSession().getLanguage();
        String strDesktopClientFailed = EnoviaResourceBundle.getProperty(context, "Configuration","emxProduct.Alert.DesktopClientFailed",strLanguage);
        if (!context.isConnected())
            throw new Exception(strDesktopClientFailed);
        return 0;
    }


    /**
     *
     * This method is called to avoid the rule evaluation when there is any
     * inactive design variant in the rule expression. This situation arises
     * only in case of Product Variant.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param strGBOMID
     *            the GBOM id <code>String</code> object
     * @param errorHolder
     *            the ProductConfigurationHolder
     *            <code>ProductConfigurationHolder</code> object
     * @param strGBOMID
     *            the rule expression <code>String</code> object
     * @return Returns a <code>String</code> object. The return object is true
     *         or false string based on the evaluation.
     * @throws Exception
     *             if the operation fails
	* modified access specifier for 377168
     * @since PRC 10.5.1.2
     * this could not be deprecated as referenced from migration JPO
     */
    public String checkDesignVariantsValidity(Context context,
            String strGBOMID, String strContextId) throws Exception {
        DomainObject domGbom = new DomainObject(strGBOMID);
        //added for the bug 377168 - start
        StringList objSelects = new StringList();
        objSelects.addElement("to["
                + ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].from.id");
        objSelects.addElement("to["
                + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
                + "].from.attribute["
                + ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS + "].value");

       Map objSelectsMap =  (Map)domGbom.getInfo(context,objSelects);
/*        String strFeatureId = domGbom.getInfo(context, "to["
                + ProductLineConstants.RELATIONSHIP_GBOM_FROM + "].from.id");
        String strAtrDesignVariants = domGbom.getInfo(context, "to["
                + ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
                + "].from.attribute["
                + ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS + "].value");*/
       String strFeatureId = (String)objSelectsMap.get("to["
               + ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].from.id");
       String strAtrDesignVariants = (String)objSelectsMap.get("to["
               + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
               + "].from.attribute["
               + ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS + "].value");
        strAtrDesignVariants = strAtrDesignVariants.trim();

        ArrayList alDvFromIrAtr = new ArrayList();
        StringTokenizer stk = new StringTokenizer(strAtrDesignVariants, ",");
        while (stk.hasMoreTokens()) {
            alDvFromIrAtr.add(stk.nextToken());
        }

        DomainObject domFeature = new DomainObject(strFeatureId);
        StringList slRelSelects = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
        slRelSelects.add("attribute["
                + ConfigurationConstants.ATTRIBUTE_INACTIVE_CONTEXTS + "]");
        slRelSelects.add("attribute["
                + ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_STATUS + "]");

        objSelects.clear();
        objSelects.add(ConfigurationConstants.SELECT_ID);

        StringBuffer sbDVRel = new StringBuffer();
        sbDVRel.append(ConfigurationConstants.SELECT_EFFECTIVITY_STATUS);
        sbDVRel.append("!= \"Inactive\"");

        /*StringList lstFeaturesChildren = ProductLineUtil.getChildrenTypes(
                context, ProductLineConstants.TYPE_FEATURES);

        StringBuffer sbChildren = new StringBuffer();
        for (int ctr = 0; ctr < lstFeaturesChildren.size(); ctr++) {
            sbChildren.append(lstFeaturesChildren.get(ctr));
            sbChildren.append(",");
        }
        String strTypePattern = sbChildren.toString();*/


        MapList dvMapList = domFeature.getRelatedObjects(context,
                                                        ConfigurationConstants.RELATIONSHIP_VARIES_BY,
                                                        ConfigurationConstants.TYPE_FEATURES,
                                                        objSelects,
                                                        slRelSelects,
                                                        false,
                                                        true,
                                                        (short) 1,
                                                        null,
                                                        sbDVRel.toString(),0);

        /*MapList dvMapList = domFeature.getRelatedObjects(context,
                ConfigurationConstants.RELATIONSHIP_VARIES_BY, "*", false,
                true, (short) 1, null, slRelSelects, null, sbDVRel.toString(),
                null, strTypePattern, null);*/

        //added for the bug 377168
        List invalidDVList = new MapList();
        Map invalidMap = null;
        String strRuleDVRelId = "";
        String strMqlCmd = "";

        String strResult = STR_TRUE;
        
        ////
        for (int i = 0; i < dvMapList.size(); i++)
        {

            invalidMap = new HashMap();
            Hashtable mp = (Hashtable) dvMapList.get(i);

            //commented for the bug 377168

            //String strICAttrValue = (String) mp.get("attribute["
            //        + ConfigurationConstants.ATTRIBUTE_INACTIVE_CONTEXTS + "]");
            //strICAttrValue = strICAttrValue.trim();

            String strDvId = (String) mp.get(ConfigurationConstants.SELECT_ID);

            //added for the bug 377168 - start
            String strDvRelId = (String) mp.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
            if(alDvFromIrAtr.contains(strDvId))
            {
                invalidMap.put("strDvId",strDvId);
                invalidMap.put("strDvRelId",strDvRelId);
                invalidDVList.add(invalidMap);
            }
        }

        for (int i = 0; i < invalidDVList.size(); i++)
        {
            strRuleDVRelId = (String) ((Map)invalidDVList.get(i)).get("strDvRelId");
            
            strMqlCmd = "print connection $1 select $2 dump $3";
            StringBuffer selectable = new StringBuffer();
            selectable.append("tomid[").append(ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY)
            		.append("].id");

            String strIVBRelIds = MqlUtil.mqlCommand(context, strMqlCmd, true,strRuleDVRelId,selectable.toString(),ConfigurationConstants.DELIMITER_PIPE);

            if (strIVBRelIds != null && !(strIVBRelIds.equals(""))
                    && !("null".equalsIgnoreCase(strIVBRelIds)))

            {
                List PVList = new StringList();
                StringTokenizer st = new StringTokenizer(strIVBRelIds, "|");

                while (st.hasMoreTokens()) {
                    String strIVBRelId = st.nextToken();

                    strMqlCmd = "print connection $1 select $2 dump";
                    String strFromRelSelect = "fromrel.id";
                    String strProdFeatListRelId = MqlUtil.mqlCommand(context,
                            strMqlCmd, true,strIVBRelId,strFromRelSelect);
                    strProdFeatListRelId = strProdFeatListRelId.trim();

                    strMqlCmd = "print connection $1 select $2 dump";
                    String strFromIdSelect = "from.id ";
                    String strPVId = MqlUtil.mqlCommand(context, strMqlCmd,
                            true,strProdFeatListRelId,strFromIdSelect);
                    strPVId = strPVId.trim();

                    PVList.add(strPVId);
                }
                if (PVList.contains(strContextId))
                {
                    strResult = STR_FALSE;
                    break;
                }
            }
        }
        return strResult;
    }



  /**
     * This method is called from getEvaluatedListsIdBased().It
       takes all the parts to evaluate and returns final part object id's
     *
     * @param context
     * @param objectsToEvaluate
     * @param calledFrom
     * @param isVariantExists
     * @param isForPart
     * @return
     * @throws Exception
     *
     */

	protected ArrayList gbomParseRulesIdBased(Context context,
            ArrayList objectsToEvaluate,
            String calledFrom,
            boolean isVariantExists, boolean isForPart) throws Exception {

        ArrayList evaluatedObject = new ArrayList();
        ArrayList partsWithoutExpression = new ArrayList();
        boolean ruleEvaluationRequired = true;
        String strExpResult = "";
        ArrayList finalReturnParts = new ArrayList();
        ArrayList temp = new ArrayList();

        //added for Bug no. 376243
        String strProdContextId = "";
        String strProdContextType = "";
        
        DomainObject domPC = new DomainObject(getId(context));

        String strContextId = domPC.getInfo(context, "to["
                + ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION
                + "].from.id");
        DomainObject domContext = new DomainObject(strContextId);
        String strContextType = domContext.getInfo(context, "type");

        //Added to make id based
        HashMap selectedOptions = new HashMap();
        MapList selectedOptionsMaplist = emxBatchRuleProcessing_mxJPO.getSelectedOptions(context,
                getId(context));
        Iterator it = selectedOptionsMaplist.iterator();
        while (it.hasNext())
        {
            selectedOptions.put(((Map) it.next())
                    .get(ConfigurationConstants.SELECT_ID), "");
        }
        //IR-042898V6R2011 - need to add context object id in selected options list to evaluate rule
        selectedOptions.put(strContextId, "");
        //End IR-042898V6R2011 - need to add context object id in selected options list to evaluate rule
        //end of add

        //added for Bug no. 376243 - start
        if(mxType.isOfParentType(context, strContextType,ConfigurationConstants.TYPE_FEATURES))
        {
           strProdContextId = domPC.getInfo(context, "to["
                    + ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION
                    + "].from.id");

           //check if there is any prodcut context associated
           if(strProdContextId != null && ! "null".equalsIgnoreCase(strProdContextId) && ! "".equalsIgnoreCase(strProdContextId))

               strProdContextType = new DomainObject(strProdContextId).getInfo(context, "type");

           //check if the context is a Product Variant
           if(strProdContextType.equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_VARIANT))
           {
               strContextId = strProdContextId;
               strContextType = strProdContextType;
           }

        }

        if (objectsToEvaluate != null && objectsToEvaluate.size() > 0) {
            for (int count = 0; count < objectsToEvaluate.size(); count++) {
                strExpResult = "";
                Hashtable objectDetails = (Hashtable) objectsToEvaluate
                        .get(count);
                String strGBOMID = (String) objectDetails.get("gbomObjectId");
                String strRuleType = (String) objectDetails.get("ruleType");
                String strExpression = (String) objectDetails.get("expression");
                String partId = (String) objectDetails.get("partId");
                List listActiveDVs = (StringList) objectDetails.get("listActiveDVs");

                if (strExpression == null || strExpression.length() <= 0)
                {
                    partsWithoutExpression.add(partId);
                    //ruleEvaluationRequired = false;
                } else {

                    /*getting the design variants referred in the
                     * inclusion rule
                     */
                    int DVCount1 = 0;
                    DomainObject domGBOM = new DomainObject(strGBOMID);
                    String strSelectExpress = "to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from." +
                            "attribute["+ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS+"]";

                    String strDVs = domGBOM.getInfo(context,strSelectExpress);

                    if(strDVs != null && ! "null".equals(strDVs) && ! "".equals(strDVs))
                    {
                        StringTokenizer st = new StringTokenizer(strDVs,",");
                        DVCount1 = st.countTokens();
                    }

                    if(listActiveDVs != null)
                    {
                        if(DVCount1 < listActiveDVs.size())
                            ruleEvaluationRequired = false;
                    }
                    if(ruleEvaluationRequired)
                    {
                    if (strContextType
                            .equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_VARIANT)
                            && isVariantExists
                            && isForPart
                                && calledFrom.equalsIgnoreCase("Part"))
                        {
                        strExpResult = checkDesignVariantsValidity(context,
                                strGBOMID, strContextId);
                        if (strExpResult.equalsIgnoreCase(STR_FALSE)) {
                            ruleEvaluationRequired = false;
                        }
                    }
                }
                }

                //modified to make id based
                if (ruleEvaluationRequired)
                {
                	//Modified For 377168
                	String strUseDesignVariant = "true";
                	DomainObject dom = new DomainObject(strGBOMID);
                	String strAttrRExp = "to["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]";
                	strAttrRExp = dom.getInfo(context, strAttrRExp);
                	//Modified for 377168
                	if(strUseDesignVariant != null && !strUseDesignVariant.equals("true"))
                    {
                    	if(strAttrRExp != null)
                    	{
                    		//
                            // Call the calculate expression to find whether to Include or Exclude the
                            // part to the "Object"
                            //
                    		strExpResult = emxBatchRuleProcessing_mxJPO
                            .calculateExpressionIdBased(context, strGBOMID,
                                    strExpression, selectedOptions);

                    	}
                    	else if(isVariantExists)
                    	{
                    		strExpResult = STR_FALSE;
                    	}
                    	else
                    	{
                    		strExpResult = STR_TRUE;
                    		partsWithoutExpression.remove(partId);
                    	}
                    }else{
                    	if(strAttrRExp != null)
                    	{
                    		//
                            // Call the calculate expression to find whether to Include or Exclude the
                            // part to the "Object"
                            //
                    		strExpResult = emxBatchRuleProcessing_mxJPO
                            .calculateExpressionIdBased(context, strGBOMID,
                                    strExpression, selectedOptions);
                    	}
                    	else if(isVariantExists && strAttrRExp == null)
                    	{
                    		strExpResult = STR_TRUE;
                    		partsWithoutExpression.remove(partId);
                    	}
                    	if(!isVariantExists && strAttrRExp == null)
                    	{
                    		//
                            // Call the calculate expression to find whether to Include or Exclude the
                            // part to the "Object"
                            //
                    		strExpResult = emxBatchRuleProcessing_mxJPO
                            .calculateExpressionIdBased(context, strGBOMID,
                                    strExpression, selectedOptions);
                    	}

                    }

/*                    strExpResult = ${CLASS:emxBatchRuleProcessing}
                            .calculateExpressionIdBased(context, strGBOMID,
                                    strExpression, selectedOptions);*/
                }
                else
                {
                	strExpResult = STR_FALSE;
                }
                //end of modify

                ruleEvaluationRequired = true;

                if ((strExpResult.equals(STR_TRUE) && strRuleType
                        .equals(ConfigurationConstants.RANGE_VALUE_INCLUSION))
                        || (strExpResult.equals(STR_FALSE) && strRuleType
                                .equals(ConfigurationConstants.RANGE_VALUE_EXCLUSION))) {
                    temp.add(partId);
                    evaluatedObject.add(partId);
                }
            }
        }
        temp.removeAll(partsWithoutExpression);
        if (calledFrom.equalsIgnoreCase("Part")) {
            if (temp.size() > 0) {
                finalReturnParts = temp;
            } else {
                finalReturnParts = evaluatedObject;
            }
        } else {
            finalReturnParts = evaluatedObject;
        }
        return finalReturnParts;
    }

	/**
	 *
	 * @param context
	 * @param strObjectID
	 * @return
	 * @throws Exception
	 * this could not be deprecated as referenced from migration JPO
	 */
	    public String getExpressionForQuantityRule(Context context,
	            String strObjectID) throws Exception {
	        String strRuleExp = ConfigurationConstants.EMPTY_STRING;
	        Map mapTemp = new HashMap();
	        MapList objectList = new MapList();
	        Map paramList = new HashMap();

	        Map programMap = new HashMap();
	        String[] arrJPOArguments = new String[1];

	        mapTemp.put(ConfigurationConstants.SELECT_ID, strObjectID.trim());
	        objectList.add(mapTemp);

	        paramList.put("intermediate", "true");
	        programMap.put("objectList", objectList);
	        programMap.put("paramList", paramList);
	        programMap.put("ruleType", ConfigurationConstants.TYPE_QUANTITY_RULE);
	        arrJPOArguments = JPO.packArgs(programMap);

	        StringList strRightExpressionList = new StringList();
	        strRightExpressionList = (StringList) (JPO.invoke(context,
	                "emxBooleanCompatibility", null, "getRightExpressionforQuantityEdit",
	                arrJPOArguments, StringList.class));
	        // Rule Expression associated with the Object
	        strRuleExp = (String) strRightExpressionList.get(0);
	        return strRuleExp;
	    }


    /**
     * Gets EBOMs with selectables
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds objectId.
     * @return MapList of EBOM Objects.
     * @throws Exception
     *             if the operation fails.
     * @since PRC 10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getEBOMWithSelects(Context context, String[] args)
            throws Exception {
        MapList ebomList = new MapList();
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String productConfigId = (String) paramMap.get("objectId");
        DomainObject productConfigObject = new DomainObject(productConfigId);
        StringList selectStmts = new StringList(2);
        selectStmts.addElement(ConfigurationConstants.SELECT_ID);
        selectStmts.addElement(ConfigurationConstants.SELECT_NAME);

        // Bug 377085/377612 :Bug Expand All level Issue
        short recurseLevel;
        String strExpandLevel = (String) paramMap.get("expandLevel");
        // if expnad level is not available
        if (strExpandLevel == null || ("".equals(strExpandLevel)) || ("null".equals(strExpandLevel))) {
        	recurseLevel = (short) 1;
        } else if (strExpandLevel.equalsIgnoreCase((ConfigurationConstants.RANGE_VALUE_ALL))){
            recurseLevel = (short) 0;
        } else {
        	recurseLevel = (short)Short.parseShort(strExpandLevel);
        }


        StringList selectRelStmts = new StringList(6);
        selectRelStmts.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_QUANTITY);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_USAGE);

        String strTopLevelObject = "";
        if ((ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)
                .equalsIgnoreCase(productConfigObject.getInfo(context,
                        ConfigurationConstants.SELECT_TYPE))) {
            strTopLevelObject = productConfigObject.getInfo(context, "from["
                    + ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART
                    + "].to.id");
            if (strTopLevelObject != null && strTopLevelObject.length() > 0) {
                try {
                    ebomList = productConfigObject.getRelatedObjects(context,
                            ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART, // relationship
                            // pattern
                            ConfigurationConstants.TYPE_PART, // object pattern
                            selectStmts, // object selects
                            selectRelStmts, // relationship selects
                            false, // to direction
                            true, // from direction
                            //(short) 1, // recursion level
                            recurseLevel, // Bug 377085/377612 :to expand based on the level
                            null, // object where clause
                            null, 0); // relationship where clause
                } catch (FrameworkException Ex) {
                    throw Ex;
                }
            }
        } else {
            try {

                ebomList = productConfigObject.getRelatedObjects(context,
                        ConfigurationConstants.RELATIONSHIP_EBOM, // relationship
                        // pattern
                        ConfigurationConstants.TYPE_PART, // object pattern
                        selectStmts, // object selects
                        selectRelStmts, // relationship selects
                        false, // to direction
                        true, // from direction
                        //(short) 1, // recursion level
                        recurseLevel, // Bug 377085/377612 :to expand based on the level
                        null, // object where clause
                        null, 0); // relationship where clause
            } catch (FrameworkException Ex) {
                throw Ex;
            }
        }

        return ebomList;
    }





    /**
     * This method is called from updateBomXmlMigration().
     * It returns the list of parts which evaluates to true as a result of the IR/ER evaluation
     * which is done based on Id comparison & not TNR against selected options
     *
     * @param context
     * @param arrPartObjectIds
     *              array of parts to be evaluated
     * @param isVariantExists
     *
     * @return
     *         Returns HshMap of parts which evaluates to true
     * @throws Exception
     * this could not be deprecated as referenced from migration JPO
     */
     protected HashMap getEvaluatedListsIdBased(Context context,
            ArrayList arrPartObjectIds, boolean isVariantExists)throws Exception
      {

        HashMap retMap = new HashMap();

        // ArrayList to store Parts connected to Product or feature
        ArrayList arrProductPartId = new ArrayList();

        // ArrayList to store Part Familys connected to Product or Feature
        ArrayList arrProductPartFamilyId = new ArrayList();

        // ArrayList to store part which are not evaluated. after evaluation
        // result will be stored in arrProductPartId
        ArrayList arrPartsToEvaluate = new ArrayList();

        // ArrayList to store PartFamily which are not evaluated. after
        // evaluation result will be stored in arrProductPartFamilyId
        ArrayList arrPartFamilysToEvaluate = new ArrayList();

        for (int l = 0; l < arrPartObjectIds.size(); l++)
        {
            MapList mapListProductPartId = (MapList) arrPartObjectIds.get(l);

            for (int mapListCount = 0; mapListCount < mapListProductPartId
                    .size(); mapListCount++)
            {
                Map mapProductPartId = (Map) mapListProductPartId
                        .get(mapListCount);

                String type = (String) mapProductPartId.get("from["
                        + ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].to."
                        + ConfigurationConstants.SELECT_TYPE);
                String partId = (String) mapProductPartId.get("from["
                        + ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].to."
                        + ConfigurationConstants.SELECT_ID);

                String gbomObjectId = (String) mapProductPartId
                        .get(ConfigurationConstants.SELECT_ID);
                String ruleType = (String) mapProductPartId
                        .get(STR_ATTRIBUTE_OPEN_BRACE
                                + ConfigurationConstants.ATTRIBUTE_RULE_TYPE
                                + EXPRESSION_CLOSE);
                //added for Bug No. 376243
                List listActiveDVs = (StringList) mapProductPartId
                .get("listActiveDVs");

                DomainObject dom = DomainObject.newInstance(context);
                dom.setId(gbomObjectId);
                String strAttRESlectable = "to["
                        + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
                        + "].from.attribute["
                        + ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]";

                String strRuleExp = dom.getInfo(context, strAttRESlectable);
                if(strRuleExp == null)
                {
                    strRuleExp = "";
                }

                //String strRuleExp = getExpression(context, gbomObjectId);

                Hashtable objectdetails = new Hashtable();
                objectdetails.put("gbomObjectId", gbomObjectId);
                objectdetails.put("ruleType", ruleType);
                objectdetails.put("expression", strRuleExp);
                objectdetails.put("partId", partId);

                //added for Bug No. 376243
                if(listActiveDVs != null)
                    objectdetails.put("listActiveDVs", listActiveDVs);

                if (type.equals(ConfigurationConstants.TYPE_PARTFAMILY)) {
                    arrPartFamilysToEvaluate.add(objectdetails);
                } else if (isOfParentType(context, type,
                        ConfigurationConstants.TYPE_PART)) {
                    arrPartsToEvaluate.add(objectdetails);
                }
            }
        }

        /*arrProductPartId = gbomParseRules(context, arrPartsToEvaluate,
                errorHolder, "Part", isVariantExists, true);*/

        arrProductPartId = gbomParseRulesIdBased(context, arrPartsToEvaluate,
                "Part", isVariantExists, true);

        if (isVariantExists) {
            ArrayList tempPFList = new ArrayList();
            for (int i = 0; i < arrPartFamilysToEvaluate.size(); i++) {
                Hashtable objectDetails = (Hashtable) arrPartFamilysToEvaluate
                        .get(i);
                String partId = (String) objectDetails.get("partId");
                tempPFList.add(i, partId);
            }
            arrProductPartFamilyId = tempPFList;
        } else {
            arrProductPartFamilyId = gbomParseRulesIdBased(context,
                    arrPartFamilysToEvaluate, "Part",
                    isVariantExists, false);
        }
        retMap.put("partIds", arrProductPartId);
        retMap.put("partFamilyIds", arrProductPartFamilyId);
        return retMap;
    }








/**
 *
 * @param context
 * @param childType
 * @param parentType
 * @return
 * @throws FrameworkException
 * this could not be deprecated as referenced from migration JPO
 */
    public static boolean isOfParentType(Context context, String childType,
            String parentType) throws FrameworkException {
        try {
        	String strMqlCommand = "print type $1 select $2 dump $3";
        	String selectable = "derivative";
            String subtypes = MqlUtil.mqlCommand(context, strMqlCommand,parentType,selectable,ConfigurationConstants.DELIMITER_PIPE);
            StringList projectTypes = FrameworkUtil.split(subtypes, "|");
            projectTypes.add(0, parentType);
            return (projectTypes.indexOf(childType) != -1);
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
    }

    // -------------CODE FOR PreviewBOM START
    /**
     * This method is used for the updation of the BOM XML attribute on product
     * configuration
     *
     * @param context
     *            the eMatrix Context object
     * @param args
     *            holds the Product configuration id
     * @throws Exception
     *             if the operation fails
     *             this could not be deprecated as referenced from migration JPO
     */
    public String updateBomXmlMigration(Context context, String[] args)
            throws Exception {
    	return "";
    }



    /**
     * This method is used by the background job (which gets fired on done of
     * PC to update the BOM XML attribute on product configuration
     *
     * @param context the eMatrix Context object
     * @param args holds the Product configuration id
     * @throws Exception if the operation fails
     * @author KXB
     * @since R212
     */
    public void updateBOMXMLForProductConfiguration(Context context, String[] args) throws Exception {

    }

    /**
     * This method expands featues from Product Configuration which will be
     * displayed in Preview BOM dialog.
     *
     * @param context the eMatrix Context object
     * @param args holds the following input arguments: paramMap a HashMap
     *             requestMap a HashMap
     * @return MapList Object which holds following parameter id of feature
     *         evaluatedParts a MapList of evaluated parts and part family
     * @throws Exception if the operation fails
     * @author KXB
     * @since R212
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPreviewBOMForProductConfiguration(Context context,String args[]) throws Exception{
    	List features = new MapList();
    	ProductConfiguration pc = new ProductConfiguration();
    	features = pc.getPreviewBOMForProductConfiguration(context, args);
    	return (MapList)features;
    }

	   /**
     * This method displays Inclusion Rule link in Selected Option column.
     *
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *             requestMap a HashMap
     * @return Vector which holds following parameter Inclusion Rule text
     * @throws Exception if the operation fails
     * @author KXB
     * @since  R212
     */
    public Vector getInclusionRuleLinkForDisplay(Context context, String args[])throws Exception {
    	HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
    	MapList objectList = (MapList) parametersMap.get("objectList");
    	String strObjectId = "";
    	Vector vInclusionRuleLink = new Vector();

    	String strLanguage = context.getSession().getLanguage();
    	String strRuleType = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Table.PreviewEBOM.EffectivityExpressionLink",strLanguage);
    	
    	for (int i = 0; i < objectList.size(); i++) {
    		Map objectMap = (Map) objectList.get(i);
    		strObjectId = (String) objectMap.get(ConfigurationConstants.SELECT_ID);
    		String strPCID = (String) objectMap.get("parentOID");

    		if(objectMap.get(ConfigurationConstants.SELECT_LEVEL).toString().equals("0")){
    			vInclusionRuleLink.add("");
    		}else{
	    		String strGBOMRelID = "";
	    		HashMap partsDetails = (HashMap) objectMap.get("PartDetails");
	    		if(partsDetails!=null){
	    			strGBOMRelID =  partsDetails.get("PartRelId").toString();
	    		}

	    		StringBuffer sIncLink = new StringBuffer();
	    		StringBuffer sb = new StringBuffer();
	    		sIncLink = sIncLink.append("<a href=\"javascript:showDialog('")
	    		.append("../configuration/GBOMRuleExpressionDialog.jsp?")
	    		.append("modetype=createnull&amp;PCID="+XSSUtil.encodeForHTMLAttribute(context,strPCID))
	    		.append("&amp;LFId=")
	    		.append(XSSUtil.encodeForHTMLAttribute(context,strObjectId))
	    		.append("&amp;PartRelId=")
	    		.append(XSSUtil.encodeForHTMLAttribute(context,strGBOMRelID));
	    		String strEndPart1 = "', '', '', 'true', 'Small')\">";
	    		String strEndPart2 = "</a>";
	    		sb = sb.append(sIncLink.toString());
	    		// XSS OK
	    		sb = sb.append(strEndPart1).append(strRuleType).append(strEndPart2);
	    		vInclusionRuleLink.add(sb.toString());
    		}
    	}
    	return vInclusionRuleLink;
    }

   /**
     * This method displays Selected Actions in Selected Actions column.
     *
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *             requestMap a HashMap
     * @return List
     * @throws Exception if the operation fails
     * @author KXB
     * @since  R212
     */
     public List getSelectedActionForDisplay(Context context, String[] args)throws Exception {
		 List lstNameRev = new StringList();
		 HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
		 MapList objectList = (MapList) parametersMap.get("objectList");
		 for (int i = 0; i < objectList.size(); i++) {
		     lstNameRev.add("");
		 }
		 return lstNameRev;
 	 }

   /**
     * This method displays Part number in Preview BOM dialog.
     *
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *             requestMap a HashMap
     * @return Vector Object which holds following parameter part number
     * @throws Exception if the operation fails
     * @author KXB
     * @since  R212
     */
     public Vector getPartNumberForDisplay(Context context, String[] args)throws Exception {
        Vector vPartName = new Vector();
        try
	    {
	        HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
	        MapList objectList = (MapList) parametersMap.get("objectList");
	        StringBuffer stbPartName = new StringBuffer();
	        StringBuffer stbPartImage = new StringBuffer();
	        String strObjectId = ConfigurationConstants.EMPTY_STRING;
	        boolean isECInstalled = false;
	        isECInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMEngineering", false, null, null);
	        boolean hasChildreninObjectList = false;
	        String strProductConfigId = ConfigurationConstants.EMPTY_STRING;
	        boolean isOnlyProductConfiguration = false;
	        String strProductConfID = ConfigurationConstants.EMPTY_STRING;
	        String strProductConfType = ConfigurationConstants.EMPTY_STRING;
	        //For export to csv
	        String exportFormat = null;
            boolean exportToExcel = false;

      		HashMap requestMap = (HashMap)parametersMap.get("paramList");
      		if(requestMap!=null && requestMap.containsKey("reportFormat")){
      			exportFormat = (String)requestMap.get("reportFormat");
      		}
      		if("CSV".equals(exportFormat)){
      			exportToExcel = true;
      		}
	        if(objectList.size()==1)
	        {
	            for (int i = 0; i < objectList.size(); i++)
	            {
	                Map objectMap = (Map) objectList.get(i);
	                strProductConfID = (String) objectMap.get("id");
	                if(strProductConfID != null && !strProductConfID.isEmpty())
	                	strProductConfType = (String) (new DomainObject(strProductConfID)).getInfo(context, ConfigurationConstants.SELECT_TYPE);

	                if(strProductConfType.equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION))
	                {
		                isOnlyProductConfiguration = true;
		                strProductConfigId = strProductConfID;
	                }
	            }
	        }
	        if(objectList.size()>0 &&!isOnlyProductConfiguration)
	        {
	            for (int i = 0; i < objectList.size(); i++)
	            {
	                Map objectMap = (Map) objectList.get(i);
	                String strParentId = (String) objectMap.get("parentOID");
	                String strBOMXML = (String) objectMap.get("strBOMXML");
	                if(strParentId== null && strBOMXML ==null )
	                {
	                    hasChildreninObjectList = true;
	                }
	            }
	        }
	        // Added for Top level part
	        if(isOnlyProductConfiguration)
	        {
	            DomainObject objID = new DomainObject(strProductConfigId);
	            StringBuffer sIncLink = new StringBuffer();
	            if (isECInstalled)
	            {
	                sIncLink = sIncLink.append("<a href=\"JavaScript:showDialog('../common/emxIndentedTable.jsp?expandProgramMenu=ENCBOMDisplayFormat&amp;table=ENCEBOMIndentedSummary&amp;header=emxEngineeringCentral.Part.ConfigTableBillOfMaterials&amp;reportType=BOM&amp;sortColumnName=Find Number&amp;sortDirection=ascending&amp;HelpMarker=emxhelppartbom&amp;suiteKey=EngineeringCentral&amp;PrinterFriendly=true&amp;toolbar=ENCBOMToolBar&amp;objectId=");
	            } else
	            {
	                sIncLink = sIncLink.append("<a href=\"JavaScript:showDialog('../common/emxIndentedTable.jsp?expandProgram=emxProductConfigurationEBOM:getAllEBOMWithSelects&amp;table=FTRBOMSummary&amp;header=emxComponents.BOM.TableHeading&amp;sortColumnName=Name&amp;sortDirection=ascending&amp;HelpMarker=emxhelpebom&amp;PrinterFriendly=true&amp;relationship=relationship_EBOM&amp;direction=from&amp;suiteKey=Configuration&amp;objectId=");
	            }
	            String strEndPart1 = "')\">";
	            String strEndPart2 = "</a>";
	            stbPartName.delete(0, stbPartName.length());
	            stbPartImage.delete(0, stbPartImage.length());
	            stbPartImage.append("<img src=\"../common/images/iconSmallPart.gif").append("\"").append("/>");
	            StringBuffer sb = new StringBuffer();
	            if (ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION.equalsIgnoreCase(objID.getInfo(context,ConfigurationConstants.SELECT_TYPE)))
	            {
	                String strTopLevelObject = objID.getInfo(context, "from["
									                         + ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART
									                         + "].to.id");
	                if (strTopLevelObject != null && strTopLevelObject.length() > 0)
	            	{
	                    String strTopLevelObjectName = objID.getInfo(context,
								                                     "from["+ ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART
								                                            + "].to.name");
	                    if(exportToExcel){
	                    	vPartName.add(strTopLevelObjectName);
	                    }
	                    else{
	                    stbPartName = stbPartName.append(" "+ XSSUtil.encodeForXML(context,strTopLevelObjectName));
	                    sIncLink = sIncLink.append(strTopLevelObject);
	                    sb = sb.append(sIncLink.toString());
	                    sb = sb.append(strEndPart1).append(stbPartName).append(strEndPart2);
	                    vPartName.add(stbPartImage + sb.toString());
	                    }
	                }else
	                {
	                    vPartName.add("");
	                }
	            }
	        }
	        if(objectList.size()>0 && !isOnlyProductConfiguration)
	        {
	        	for (int i = 0; i < objectList.size(); i++)
		        {
		            StringBuffer sIncLink = new StringBuffer();
		            if (isECInstalled)
		            {
		                sIncLink = sIncLink.append("<a href=\"JavaScript:showDialog('../common/emxIndentedTable.jsp?expandProgramMenu=ENCBOMDisplayFormat&amp;table=ENCEBOMIndentedSummary&amp;header=emxEngineeringCentral.Part.ConfigTableBillOfMaterials&amp;reportType=BOM&amp;sortColumnName=Find Number&amp;sortDirection=ascending&amp;HelpMarker=emxhelppartbom&amp;suiteKey=EngineeringCentral&amp;PrinterFriendly=true&amp;toolbar=ENCBOMToolBar&amp;objectId=");
		            } else {
		                sIncLink = sIncLink.append("<a href=\"JavaScript:showDialog('../common/emxIndentedTable.jsp?expandProgram=emxProductConfigurationEBOM:getAllEBOMWithSelects&amp;table=FTRBOMSummary&amp;header=emxComponents.BOM.TableHeading&amp;sortColumnName=Name&amp;sortDirection=ascending&amp;HelpMarker=emxhelpebom&amp;PrinterFriendly=true&amp;relationship=relationship_EBOM&amp;direction=from&amp;suiteKey=Configuration&amp;objectId=");
		            }
		            String strEndPart1 = "')\">";
		            String strEndPart2 = "</a>";
		            stbPartName.delete(0, stbPartName.length());
		            stbPartImage.delete(0, stbPartImage.length());
		            stbPartImage.append("<img src=\"../common/images/iconSmallPart.gif").append("\"").append("/>");
		            StringBuffer sb = new StringBuffer();
		            Map objectMap = (Map) objectList.get(i);

 String strLevel = (String)objectMap.get("level");
		            
		            if("0".equals(strLevel)){
		            	vPartName.add("");
		           	 continue;
		            }

		            String isLeafLF = (String) objectMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_LEAF_LEVEL);
		            MapList objectMaptemp = new MapList();
		            if(hasChildreninObjectList)
		            {
		                objectMaptemp = (MapList)objectMap.get("children");
		                if(objectMaptemp!=null){

			                for (int j = 0; j < objectMaptemp.size(); j++)
			                {
			                    Map objectMapChildren = (Map) objectMaptemp.get(j);
			                    strObjectId = (String) objectMapChildren.get(ConfigurationConstants.SELECT_ID);
			                }
		                }
		            }
		            else
		            {
		                strObjectId = (String) objectMap.get(ConfigurationConstants.SELECT_ID);
		            }

		            if(ProductLineCommon.isNotNull(strObjectId)){
		            	//DomainObject objID = new DomainObject(strObjectId);
		            	if (!ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION.equalsIgnoreCase((String)objectMap.get(ConfigurationConstants.SELECT_TYPE))) {
			            	// process the part for leaf level feature, no BOM XML data, get the realtime data from the object list.
			            	// IVU - BOM XML
			            	// Filter the Parts from the Object List which were retrieved from Real- time Filter


				            HashMap hmPart = (HashMap)objectMap.get("evaluatedParts");
				            HashMap hmPartDetails = (HashMap)objectMap.get("PartDetails");

						 String strGBOMRelID = null;
						 if(hmPartDetails!=null){
			    			strGBOMRelID = hmPartDetails.get("GBOMRelId").toString();
						 }


				            // IVU - BOM XML
				            // After Filter code is available only one part will be available as part of this Array List, which will be resolved
				            ArrayList arrPartIds = (ArrayList)hmPart.get("partIds");
				            String partId =  "";
				            /// replace this getInfo once the BOM XML is in place
				            // BOM XML needs to be loaded to get the Pending / Resolved Parts
				            String partName = "";
			            	if(isLeafLF.equalsIgnoreCase("Yes")){
					            if(hmPartDetails!=null && hmPartDetails.size()>0){
					            	// Need to replace this code with the Part Name
					            	 partId = (String)hmPartDetails.get(ConfigurationConstants.SELECT_ID);
					            	 partName = (String)hmPartDetails.get(DomainObject.SELECT_NAME);
					            	 sIncLink = sIncLink.append(partId);
		                             sb.append(sIncLink.toString());
		                             if(exportToExcel){
		                             	vPartName.add(partName);
		                             }
		                             else{
			                             sb.append(strEndPart1).append(XSSUtil.encodeForXML(context,partName)).append(strEndPart2);
			                             String strPartIDTag = "<input type='hidden' name='PartID' value='"+partId +"'/>";
			                             sb.append(strPartIDTag);
			                             String strPartRelIDTag = "<input type='hidden' name='GBOMRelId' value='"+strGBOMRelID +"'/>";
			                             sb.append(strPartRelIDTag);
			                             vPartName.add(stbPartImage + sb.toString());
		                             }

					            }else{
					            	 partName = "???";
		                             sIncLink = sIncLink.append(partId);
		                             sb.append(sIncLink.toString());
		                             if(exportToExcel){
		                             	vPartName.add(partName);
		                             }
		                             else{
			                             sb.append(strEndPart1).append(XSSUtil.encodeForXML(context,partName)).append(strEndPart2);
			                             vPartName.add(stbPartImage + " " + XSSUtil.encodeForXML(context,partName));
		                             }
					            }

			            	}else{
					            if(arrPartIds!=null & arrPartIds.size()>0){
					            	// Need to replace this code with the Part Name
					            	 partId = (String)hmPartDetails.get(ConfigurationConstants.SELECT_ID);
					            	 partName = (String)hmPartDetails.get(DomainObject.SELECT_NAME);
					            	 sIncLink = sIncLink.append(partId);
		                             sb.append(sIncLink.toString());
		                             if(exportToExcel){
		                             	vPartName.add(partName);
		                             }
		                             else{
			                             sb.append(strEndPart1).append(XSSUtil.encodeForXML(context,partName)).append(strEndPart2);
			                             String strPartIDTag = "<input type='hidden' name='PartID' value='"+XSSUtil.encodeForHTMLAttribute(context,partId) +"'/>";
			                             sb.append(strPartIDTag);
			                             String strPartRelIDTag = "<input type='hidden' name='GBOMRelId' value='"+XSSUtil.encodeForHTMLAttribute(context,strGBOMRelID) +"'/>";
			                             sb.append(strPartRelIDTag);
			                             vPartName.add(stbPartImage + sb.toString());
		                             }


					            }else{
					            	 partName = "???";
		                             sIncLink = sIncLink.append(partId);
		                             sb.append(sIncLink.toString());
		                             if(exportToExcel){
		                             	vPartName.add(partName);
		                             }
		                             else{
		                             sb.append(strEndPart1).append(XSSUtil.encodeForXML(context,partName)).append(strEndPart2);
		                             vPartName.add(stbPartImage + " " + XSSUtil.encodeForXML(context,partName));
		                             }
					            }

			            	}
			            }
			        }else{
                         sb.append(sIncLink.toString());
                         sb.append(strEndPart1).append(XSSUtil.encodeForXML(context,"???")).append(strEndPart2);
                         vPartName.add(stbPartImage + " " + XSSUtil.encodeForXML(context,"???"));
			        }
		        }
	        }
	        else{
	            vPartName.add("");
	        }
	    }catch (Exception e)
	    {
            e.printStackTrace();
        }

        return vPartName;
     }

   /**
     * This method displays Action Icons column.
     *
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *             requestMap a HashMap
     * @return Vector which holds following parameter html code to display icons
     * @throws Exception if the operation fails
     * @author KXB
     * @since  R212
     */
     public Vector getActionIconsForDisplay(Context context, String args[])throws Exception {
         ProductConfiguration pc =  new ProductConfiguration();
         HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
         MapList objectList = (MapList) parametersMap.get("objectList");
         ArrayList parts = new ArrayList();
         ArrayList Duplicateparts = new ArrayList();
         ArrayList partFamily = new ArrayList();
         StringBuffer stbNameRev = new StringBuffer();
         Vector vNameRev = new Vector();
         String strObjectId = "";
         boolean displayGenerateIcon = false;
         boolean isUsedInBOM = false;

         boolean isECInstalled = false;
         isECInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMEngineering", false, null, null);
         
         String i18ReplabeByGenerate = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.ReplabeByGenerate",context.getSession().getLanguage());
         String i18ReplabeByAddExisting = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.ReplabeByAddExisting",context.getSession().getLanguage());
         String i18ReplabeByCreateNew = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.ReplabeByCreateNew",context.getSession().getLanguage());
         String i18Generate = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.Generate",context.getSession().getLanguage());
         String i18AddExisting = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.AddExisting",context.getSession().getLanguage());
         String i18CreateNew = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.CreateNew",context.getSession().getLanguage());


         for (int i = 0; i < objectList.size(); i++)
         {
             boolean isQuantityInvalid = false;
             stbNameRev.delete(0, stbNameRev.length());
             Map objectMap = (Map) objectList.get(i);

 String strLevel = (String)objectMap.get("level");
             
             if("0".equals(strLevel)){
            	 vNameRev.add("");
            	 continue;
             }

             strObjectId = (String) objectMap.get(ConfigurationConstants.SELECT_ID);
             String strObjectLevel = (String) objectMap.get("id[level]");
             HashMap evaluatedParts = (HashMap) objectMap.get("evaluatedParts");
             String parentId = (String) objectMap.get("parentOID");
             ArrayList featureId = new ArrayList();
             featureId.add(strObjectId);
             // get list of parts for which part inclusion rule is evaluated to true
             if(evaluatedParts!=null)
             parts = (ArrayList) evaluatedParts.get("partIds");
             if(evaluatedParts!=null)
             Duplicateparts = (ArrayList) evaluatedParts.get("duplicatePartIds");

             if(!(parts.size()>0))
             {
            	 if(evaluatedParts!=null)
                 parts = (ArrayList) evaluatedParts.get("partIds");
             }
                          if(evaluatedParts!=null)
             partFamily = (ArrayList) evaluatedParts.get("partFamilyIds");
             // check whether quantity is invalid or not this will be used to
             // display visual cue icon in case of create/replace/generate
             
             String strQuantity = (String) objectMap.get("attribute[" + ConfigurationConstants.ATTRIBUTE_QUANTITY + "]");

             if (strQuantity != null) {
                 double quantity = Double.parseDouble(strQuantity);
                 if (quantity < 0) {
                     isQuantityInvalid = true;
                 }
             }

             if (partFamily.size() == 1)
             {
                 displayGenerateIcon = true;
             } else
             {
                 displayGenerateIcon = false;
             }


             // if inclusion rule for single part is evaluated true then display that part
			 // IVU - BOM XML
			 // get the Parent Logical Feature for the  Duplicate
			 // Also get the attribute if the LF is leaf level
			 String strLFParentId = (String) objectMap.get("id[parent]");
			 String strForcePartReuse  = (String)objectMap.get("attribute[" + ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE + "]");
			 String strLFisLeaf = (String) objectMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_LEAF_LEVEL);
			 String strResolvedPart = null;
			 String strGBOMRelID = null;
			 HashMap partsDetails = (HashMap) objectMap.get("PartDetails");
			 if(partsDetails!=null){
    			strResolvedPart =  partsDetails.get(ConfigurationConstants.SELECT_ID).toString();
    			strGBOMRelID = partsDetails.get("GBOMRelId").toString();
			 }

             isUsedInBOM = pc.checkForUsedInBOM(context, strObjectId, strResolvedPart, strLFParentId, parentId);

             //boolean isECInstalled = false;
             //isECInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMEngineering", false, null, null);

             if (evaluatedParts!=null && parts.size() == 0)
             {
                 if (isECInstalled)
                 {
                     stbNameRev = stbNameRev
                             .append("<a><img src=\"../common/images/iconActionCreateNewPart.gif")
                             .append("\" border=\"0\"  align=\"middle\" ")
                             .append("TITLE=\"")
                             .append(i18CreateNew)
                             .append("\"")
                             .append(" onclick=\"javascript:showDialog('../configuration/PreviewBOMProcess.jsp?featureId="
                                             + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
                                             + "&amp;level="
                                             + XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)
                                             + "&amp;isQuantityInvalid="
                                             + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isQuantityInvalid))
											 + "&amp;LFParentID="
											 + XSSUtil.encodeForHTMLAttribute(context,strLFParentId)
											 + "&amp;isLFLeaf="
											 + XSSUtil.encodeForHTMLAttribute(context,strLFisLeaf)
											 + "&amp;ForcePartReuse="
											 + XSSUtil.encodeForHTMLAttribute(context,strForcePartReuse)
                                             + "&amp;displayGenerateIcon="
                                             + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(displayGenerateIcon))
                                             + "&amp;isUsedInBOM="
                                             + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isUsedInBOM))
                                             + "&amp;DuplicateParts="
                                             + Duplicateparts
                                             + "&amp;mode=createpart"
                                             + "&amp;duplicate=false"
                                             + "&amp;parentId="
                                             + XSSUtil.encodeForHTMLAttribute(context,parentId)
                                             + "&amp;generate=false" + "');\"")
                             .append("/></a>");
                 }
                 stbNameRev = stbNameRev
                         .append("  <a><img src=\"../common/images/iconActionAddExistingPart.gif")
                         .append("\" border=\"0\"  align=\"middle\" ")
                         .append("TITLE=\"")
                         .append(i18AddExisting)
                         .append("\"")
                         .append(" onclick=\"javascript:showModalDialog('../common/emxFullSearch.jsp?field=TYPES=type_Part:CURRENT!=policy_ECPart.state_Obsolete:POLICY!=policy_DevelopmentPart,policy_StandardPart,policy_ConfiguredPart&amp;table=FTRFeatureSearchResultsTable"
                        		         + "&amp;HelpMarker=emxhelpfullsearch&amp;showInitialResults=false&amp;hideHeader=true&amp;featureId="
                                         + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
                                         + "&amp;Object="
                                         + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
                                         + "&amp;level="
                                         + XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)
                                         + "&amp;isQuantityInvalid="
                                         + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isQuantityInvalid))
                                         + "&amp;displayGenerateIcon="
                                         + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(displayGenerateIcon))
										 + "&amp;LFParentID="
										 + XSSUtil.encodeForHTMLAttribute(context,strLFParentId)
										 + "&amp;ForcePartReuse="
										 + XSSUtil.encodeForHTMLAttribute(context,strForcePartReuse)
                                         + "&amp;DuplicateParts="
                                         + Duplicateparts
										 + "&amp;isLFLeaf="
										 + XSSUtil.encodeForHTMLAttribute(context,strLFisLeaf)
                                         + "&amp;isUsedInBOM="
                                         + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isUsedInBOM))
                                         + "&amp;suiteKey=Configuration"
                                         + "&amp;selection=single"
                                         + "&amp;mode=addexisting"
                                         + "&amp;duplicate=false"
                                         + "&amp;parentId="
                                         + XSSUtil.encodeForHTMLAttribute(context,parentId)
                                         + "&amp;ResolvedPart="
						                 + XSSUtil.encodeForHTMLAttribute(context,strResolvedPart)
                                         + "&amp;ResolvedPartRelID=" 
                                         + XSSUtil.encodeForHTMLAttribute(context,strGBOMRelID)	
                                         + "&amp;generate=false" 
                                         + "&amp;submitURL=../configuration/PreviewBOMProcess.jsp?"
                                         + "&amp;PartMode=null"
                                         + "&amp;displayGenerateIcon=true"
                                         + "', '700', '600', 'true', 'Large');\"")
                         .append("/></a>");
                 if (partFamily.size() == 1)
                 {
                     String strPartFamily = (String) partFamily.get(0);
                     DomainObject objPartFamily = new DomainObject(strPartFamily);
                     String strPartFamilyState = objPartFamily.getInfo(context,ConfigurationConstants.SELECT_CURRENT);
                     if (!("obsolete").equalsIgnoreCase(strPartFamilyState))
                     {
                         stbNameRev = stbNameRev.append("  <a href=\"javaScript:emxTableColumnLinkClick('");
									  stbNameRev.append("../configuration/PreviewBOMProcess.jsp?featureId=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strObjectId));
									  stbNameRev.append("&amp;level=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strObjectLevel));
									  stbNameRev.append("&amp;isQuantityInvalid=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(isQuantityInvalid)));
									  stbNameRev.append("&amp;displayGenerateIcon=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(displayGenerateIcon)));
									  stbNameRev.append("&amp;DuplicateParts=");
									  stbNameRev.append(Duplicateparts);
									  stbNameRev.append("&amp;LFParentID=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strLFParentId));
									  stbNameRev.append("&amp;ForcePartReuse=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strForcePartReuse));
									  stbNameRev.append("&amp;isLFLeaf=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strLFisLeaf));
									  stbNameRev.append("&amp;isUsedInBOM=" );
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(isUsedInBOM)));
									  stbNameRev.append("&amp;mode=generate");
									  stbNameRev.append("&amp;duplicate=false");
									  stbNameRev.append("&amp;parentId=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, parentId));
									  stbNameRev.append("&amp;generate=true");
									  stbNameRev.append("&amp;partFamilyId=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamily));
									  stbNameRev.append("', '700', '600', 'true', 'listHidden', '')\">");
									  stbNameRev.append("<img border=\"0\" align=\"middle\" src=\"../common/images/iconActionGenerateFromPartFamily.gif\" title=\""+i18Generate+"\"/></a>");
                     }
                 }
             }
             if (parts.size()>0)
             {
            	 //IF ENGINEERING CENTRAL IS INSTALLED
                 if (isECInstalled)
                 {
                     stbNameRev = stbNameRev.append("<a><img src=\"../common/images/iconActionReplaceWithNewPart.gif")
				                            .append("\" border=\"0\"  align=\"middle\" ")
				                            .append("TITLE=\"")
				                            .append(i18ReplabeByCreateNew)
				                            .append("\"")
				                            .append(" onclick=\"javascript:showDialog('../configuration/PreviewBOMProcess.jsp?featureId="
				                                             + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
				                                             + "&amp;level="
				                                             + XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)
				                                             + "&amp;isQuantityInvalid="
				                                             + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isQuantityInvalid))
			    											 + "&amp;LFParentID="
			    											 + XSSUtil.encodeForHTMLAttribute(context,strLFParentId)
			    											 + "&amp;isLFLeaf="
			    											 + XSSUtil.encodeForHTMLAttribute(context,strLFisLeaf)
			    											 + "&amp;ForcePartReuse="
			    											 + XSSUtil.encodeForHTMLAttribute(context,strForcePartReuse)
				                                             + "&amp;displayGenerateIcon="
				                                             + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(displayGenerateIcon))
				                                             + "&amp;isUsedInBOM="
				                                             + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isUsedInBOM))
			                                                 + "&amp;DuplicateParts="
			                                                 + Duplicateparts
				                                             + "&amp;mode=replacebycreatepart"
				                                             + "&amp;duplicate=false"
				                                             + "&amp;parentId="
				                                             + XSSUtil.encodeForHTMLAttribute(context,parentId)
				                                             + "&amp;ResolvedPart="
				                                             + XSSUtil.encodeForHTMLAttribute(context,strResolvedPart)
				                                             + "&amp;ResolvedPartRelID="
				                                             + XSSUtil.encodeForHTMLAttribute(context,strGBOMRelID)				                                             
				                                             + "&amp;generate=false" + "');\"")
				                            .append("/></a>");
                 }
                 stbNameRev = stbNameRev.append("  <a><img src=\"../common/images/iconActionReplaceWithExistingPart.gif")
				                        .append("\" border=\"0\"  align=\"middle\" ")
				                        .append("TITLE=\"")
				                        .append(i18ReplabeByAddExisting)
				                        .append("\"")
				                        .append(" onclick=\"javascript:showModalDialog('../common/emxFullSearch.jsp?field=TYPES=type_Part:CURRENT!=policy_ECPart.state_Obsolete:POLICY!=policy_DevelopmentPart,policy_StandardPart,policy_ConfiguredPart&amp;table=FTRFeatureSearchResultsTable"
                        		         + "&amp;HelpMarker=emxhelpfullsearch&amp;showInitialResults=false&amp;hideHeader=true&amp;featureId="
                                         + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
                                         + "&amp;Object="
                                         + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
                                         + "&amp;level="
                                         + XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)
                                         + "&amp;isQuantityInvalid="
                                         + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isQuantityInvalid))
                                         + "&amp;displayGenerateIcon="
                                         + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(displayGenerateIcon))
										 + "&amp;LFParentID="
										 + XSSUtil.encodeForHTMLAttribute(context,strLFParentId)
										 + "&amp;ForcePartReuse="
										 + XSSUtil.encodeForHTMLAttribute(context,strForcePartReuse)
                                         + "&amp;DuplicateParts="
                                         + Duplicateparts
										 + "&amp;isLFLeaf="
										 + XSSUtil.encodeForHTMLAttribute(context,strLFisLeaf)
                                         + "&amp;isUsedInBOM="
                                         + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isUsedInBOM))
                                         + "&amp;suiteKey=Configuration"
                                         + "&amp;selection=single"
                                         + "&amp;mode=replacebyaddexisting"
                                         + "&amp;duplicate=false"
                                         + "&amp;parentId="
                                         + XSSUtil.encodeForHTMLAttribute(context,parentId)
                                         + "&amp;ResolvedPart="
						                 + XSSUtil.encodeForHTMLAttribute(context,strResolvedPart)
                                         + "&amp;ResolvedPartRelID=" 
                                         + XSSUtil.encodeForHTMLAttribute(context,strGBOMRelID)	
                                         + "&amp;generate=false" 
                                         + "&amp;submitURL=../configuration/PreviewBOMProcess.jsp?"
                                         + "&amp;PartMode=null"
                                         + "&amp;displayGenerateIcon=true"
                                         + "', '700', '600', 'true', 'Large');\"")
				                        .append("/></a>");
                 //IF PART FAMILY SIZE == 1
                 if (partFamily.size() == 1)
                 {
                     String strPartFamily = (String) partFamily.get(0);
                     DomainObject objPartFamily = new DomainObject(strPartFamily);
                     String strPartFamilyState = objPartFamily.getInfo(context,ConfigurationConstants.SELECT_CURRENT);
                     if (!("obsolete").equalsIgnoreCase(strPartFamilyState))
                     {
                         stbNameRev = stbNameRev.append("  <a href=\"javaScript:emxTableColumnLinkClick('");
									  stbNameRev.append("../configuration/PreviewBOMProcess.jsp?featureId=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strObjectId));
									  stbNameRev.append("&amp;level=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strObjectLevel));
									  stbNameRev.append("&amp;isQuantityInvalid=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(isQuantityInvalid)));
									  stbNameRev.append("&amp;displayGenerateIcon=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(displayGenerateIcon)));
									  stbNameRev.append("&amp;LFParentID=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strLFParentId));
									  stbNameRev.append("&amp;DuplicateParts=");
									  stbNameRev.append(Duplicateparts);
									  stbNameRev.append("&amp;isLFLeaf=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strLFisLeaf));
									  stbNameRev.append("&amp;ForcePartReuse=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strForcePartReuse));
									  stbNameRev.append("&amp;isUsedInBOM=" );
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(isUsedInBOM)));
									  stbNameRev.append("&amp;mode=generatebyreplace");
									  stbNameRev.append("&amp;duplicate=false");
									  stbNameRev.append("&amp;generate=true");
									  stbNameRev.append("&amp;parentId=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, parentId));
									  stbNameRev.append("&amp;ResolvedPart=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strResolvedPart));
									  stbNameRev.append("&amp;ResolvedPartRelID=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strGBOMRelID));
									  stbNameRev.append("&amp;partFamilyId=");
									  stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamily));
									  stbNameRev.append("', '700', '600', 'true', 'listHidden', '')\">");
									  stbNameRev.append("<img border=\"0\" align=\"middle\" src=\"../common/images/iconActionReplaceWithGeneratedpart.gif\" title=\""+i18ReplabeByGenerate+"\"/></a>");
                     }
                 }
             }
             vNameRev.add(stbNameRev.toString());
         }
         return vNameRev;
     }

   /**
     * This method displays visual cue column in Preview BOM page
     *
     * @param  context the eMatrix Context object
     * @param  args holds ObjectList
     * @return Vector
     * @throws Exception if the operation fails
     * @since  R212
     * @author KXB
     */
     public Vector getVisualCueForDisplay(Context context, String args[])throws Exception {
    	 ProductConfiguration pc = new ProductConfiguration();
    	 HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
           
    	 MapList objectList = (MapList) parametersMap.get("objectList");
    	 ArrayList parts = new ArrayList();
    	 ArrayList partFamily = new ArrayList();
    	 boolean displayGenerateIcon = false;
    	 StringBuffer stbVisualCue = new StringBuffer();
    	 Vector visualCue = new Vector();
    	 String strObjectId = "";
    	 String strLFState = "";
    	 final String strSymbObsoleteState = "state_Obsolete";
    	 String strObsolete = FrameworkUtil.lookupStateName(context,ConfigurationConstants.POLICY_EC_PART, strSymbObsoleteState);
    	 //LF Obsolete state
    	 String strLFObsolete = FrameworkUtil.lookupStateName(context,ConfigurationConstants.POLICY_LOGICAL_FEATURE, strSymbObsoleteState);
    	 //Added for Top Level Part
    	 String strProductConfigId = ConfigurationConstants.EMPTY_STRING;
    	 boolean isOnlyProductConfiguration = false;
    	 String strProductConfID = ConfigurationConstants.EMPTY_STRING;
    	 String strProductConfType = ConfigurationConstants.EMPTY_STRING;
    	 try {
	        
	        String strToolTip = "";
		   String strCustomPartMode = EnoviaResourceBundle.getProperty(context,"emxConfiguration.PreviewBOM.EnableCustomPartMode");
		   if (ProductLineCommon.isNotNull(strCustomPartMode)
				   && strCustomPartMode.equalsIgnoreCase("true")) {
			   strToolTip = "emxProduct.ToolTip.VisualCue.OptionalChoices";
		   } else {   						
			   strToolTip = "emxProduct.ToolTip.VisualCue.Duplicate";
		   }
			   
		   String i18ToolTip = EnoviaResourceBundle.getProperty(context, SUITE_KEY,strToolTip,context.getSession().getLanguage());
		   String i18EBOMDifferent = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.VisualCue.EBOMDifferent",context.getSession().getLanguage());
		   String i18HigherRevision = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.VisualCue.HigherRevision",context.getSession().getLanguage());
		   String i18ObsoletePart = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.VisualCue.ObsoletePart",context.getSession().getLanguage());
		   //String i18InvalidQuantity = i18nnow.GetString("emxConfigurationStringResource", strLanguage, "emxProduct.ToolTip.VisualCue.InvalidQuantity");
		   String i18UsedInBOM = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.VisualCue.UsedInBOM",context.getSession().getLanguage());
		   String i18ObsoleteLF = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.VisualCue.ObsoleteLF",context.getSession().getLanguage());
				   
    		 if(objectList.size()==1)
    		 {
    			 for (int i = 0; i < objectList.size(); i++)
    			 {
    				 Map objectMap = (Map) objectList.get(i);
    				 strProductConfID = (String) objectMap.get("id");
    				 if(strProductConfID != null && !strProductConfID.isEmpty())
    					 strProductConfType = (String) (new DomainObject(strProductConfID)).getInfo(context, ConfigurationConstants.SELECT_TYPE);

    				 if(strProductConfType.equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION))
    				 {
    					 isOnlyProductConfiguration = true;
    					 strProductConfigId = strProductConfID;
    				 }
    			 }
    		 }

    		 if(isOnlyProductConfiguration)
    		 {
    			 DomainObject domPC = new DomainObject(strProductConfigId);
    			 String strTLPId = (String)domPC.getInfo(context, "from["+ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART+"].to.id");
    			 Boolean isBOMDifferent =  false ;
    			 MapList relatedParts = new MapList();
    			 StringList partIdFromEbom = new StringList();
    			 StringList partIdFromPC = new StringList();
    			 
    			 if(!("".equals(strTLPId) || "null".equals(strTLPId) || null==strTLPId))
    			 {
    				 String[] argsTemp = new String[3];
    				 argsTemp[0] = strTLPId;
    				 argsTemp[1] = "to";
    				 argsTemp[2] = ConfigurationConstants.RELATIONSHIP_EBOM;
    				 boolean hasEBOMStructureTo = Boolean.parseBoolean(hasRelationship(context,argsTemp));
    				 ConfigurationUtil confUtil = new ConfigurationUtil(strTLPId);
    				 if (hasEBOMStructureTo)
    				 {
    					 isBOMDifferent = true;
    				 }else {
    					 argsTemp[1] = "from";
    					 boolean hasEBOMStructureFrom = Boolean.parseBoolean(hasRelationship(context,argsTemp));
    					 if (hasEBOMStructureFrom)
    					 {

    						 String strTypePattern  = ConfigurationConstants.TYPE_PART;
    						 String strRelPattern = ConfigurationConstants.RELATIONSHIP_EBOM;
    						 StringList slObjSelects = new StringList();
    						 StringList slRelSelects = new StringList();
    						 slObjSelects.add(ConfigurationConstants.SELECT_ID);
    						 slRelSelects.add(ConfigurationConstants.SELECT_RELATIONSHIP_ID);

    						 relatedParts = confUtil.getObjectStructure(context,strTypePattern,strRelPattern,
    								 slObjSelects, slRelSelects,false, true, (short)0,0,"", "",(short)0,"");

    						 if(relatedParts.size()>0)
    						 {
    							 for(int index =0;index < relatedParts.size();index++)
    							 {
    								 Map tempMap = (Map)relatedParts.get(index);
    								 partIdFromEbom.add((String)tempMap.get(ConfigurationConstants.SELECT_ID));
    								 //relIdFromEbom.add((String)tempMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID));
    							 }
    						 }
    						 DomainObject productConfId = new DomainObject(strProductConfID);

    						 String strBOMXML = productConfId.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_BOMXML);
    						 SAXBuilder saxb = new SAXBuilder();
    						 saxb.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
							 saxb.setFeature("http://xml.org/sax/features/external-general-entities", false);
							 saxb.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    						 Document document = saxb.build(new StringReader(strBOMXML));
    						 XPath xPath = com.matrixone.jdom.xpath.XPath.newInstance("//EngineeringBOM/Feature");
    						 List elementFeatureList = xPath.selectNodes(document, "//EngineeringBOM/Feature");

    						 for (int ix = 0; ix < elementFeatureList.size(); ix++)
    						 {
    							 Element featureElement = (Element) elementFeatureList.get(ix);
    							 Element resolvedElement = (Element) featureElement.getChild("Resolved");

    							 if(null!=resolvedElement)
    							 {
    								 String resolvedPartId = resolvedElement.getAttributeValue("id");
    								 partIdFromPC.add(resolvedPartId);
    							 }
    							 Element pendingElement = (Element) featureElement.getChild("Pending");
    							 if(null!=pendingElement)
    							 {
    								 String pendingpartId = pendingElement.getAttributeValue("id");
    								 partIdFromPC.add(pendingpartId);
    							 }

    						 }
    						 TreeSet partSetFromEBOM = new TreeSet(partIdFromEbom);
    						 TreeSet partSetFromPC = new TreeSet(partIdFromPC);
    						 isBOMDifferent = (!partSetFromPC.equals(partSetFromEBOM));

    					 }
    					 else{
    						 isBOMDifferent = true;
    					 }
    				 }

    				 // int noOfEbomStructure = numberOfEbomStructure(context,argsTemp);
    				 if(isBOMDifferent)
    				 {
    					 StringBuffer stbTLPVisualCue = new StringBuffer();
    					 stbTLPVisualCue = stbTLPVisualCue
    					 .append(
    					 "<img src=\"../common/images/iconStatusValidationError.gif")
    					 .append("\" border=\"0\"  align=\"middle\" ")
    					 .append("TITLE=\"")
    					 .append(i18EBOMDifferent)
    							 .append("\"").append("/>");

    					 visualCue.add(stbTLPVisualCue.toString());
    				 }

    			 }else{
    				 visualCue.add("");
    			 }
    		 }

    		 if(objectList.size()>0 && !isOnlyProductConfiguration)
    		 {
    			 
    			 StringList partInfoSelectable = new StringList();
    			 partInfoSelectable.add(ConfigurationConstants.SELECT_CURRENT);
    			 partInfoSelectable.add(ConfigurationConstants.SELECT_NEXT);
    			 for (int i = 0; i < objectList.size(); i++)
    			 {
    				 boolean isQuantityInvalid = false;
    				 stbVisualCue.delete(0, stbVisualCue.length());
    				 Map objectMap = (Map) objectList.get(i);

	 String strLevel = (String)objectMap.get("level");
 		            
 		            if("0".equals(strLevel)){
 		            	visualCue.add("");
 		           	 continue;
 		            }

    				 strObjectId = (String) objectMap.get(ConfigurationConstants.SELECT_ID);
    				 strLFState = (String) objectMap.get(ConfigurationConstants.SELECT_CURRENT);
    				 HashMap evaluatedParts = (HashMap) objectMap.get("evaluatedParts");
    				 if(evaluatedParts!=null){

    				 partFamily = (ArrayList) evaluatedParts.get("partFamilyIds");



    					 // IVU - BOM XML
    				 // get the Parent Logical Feature for the  Duplicate
    				 // Also get the attribute if the LF is leaf level

    				 String strLFParentId = (String) objectMap.get("id[parent]");
    				 String strLFisLeaf = (String) objectMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_LEAF_LEVEL);
    				 String strForcePartReuse  = (String)objectMap.get("attribute[" + ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE + "]");


    				 if (partFamily.size() == 1)
    				 {
    					 displayGenerateIcon = true;
    				 } else
    				 {
    					 displayGenerateIcon = false;
    				 }
    				 String parentId = (String) objectMap.get("parentOID");
    				 String strObjectLevel = (String) objectMap.get("id[level]");
    	
    				 parts = (ArrayList) evaluatedParts.get("duplicatePartIds");
    				 // IVU - BOM XML
    				 // Here is where all the un resolved parts or the Duplicate Parts Go.

    				 HashMap partDetails = (HashMap) objectMap.get("PartDetails");
    				 String strResolvedPart = null;
    				 String strGBOMRelID = null;
    				 if(partDetails!=null){
    					 strResolvedPart = (String)partDetails.get(ConfigurationConstants.SELECT_ID);
    					 strGBOMRelID = partDetails.get("GBOMRelId").toString();
    				 }

    				 if (parts.size() >= 1)
    				 {
    					 //Added for IR-341141 - Updating variable for custom part
    					 boolean bNotDuplicate = false;
    					 if(parts.size() == 1)
    						 bNotDuplicate = false;
    					 else
    						 bNotDuplicate = true;
    					 
    					 //***************DUPLICATE PART CHECK***************
    					 stbVisualCue = stbVisualCue
    					 .append(
    					 "<a><img src=\"../common/images/iconSmallHigherRevision.gif")
    					 .append("\" border=\"0\"  align=\"middle\" ")
    					 .append("TITLE=\"")

    					 .append(i18ToolTip)
    									 .append("\"")
    									 .append(
    											 " onclick=\"javascript:showDialog('../configuration/PreviewBOMProcess.jsp?featureId="
    											 + strObjectId
    											 + "&amp;displayGenerateIcon="
    											 + displayGenerateIcon
    											 + "&amp;level="
    											 + strObjectLevel
    											 + "&amp;ResolvedPart="
    											 + strResolvedPart
	                                             + "&amp;ResolvedPartRelID="
	                                             + strGBOMRelID
    											 + "&amp;LFParentID="
    											 + strLFParentId
    											 + "&amp;isLFLeaf="
    											 + strLFisLeaf
	 											 + "&amp;ForcePartReuse="
												 + strForcePartReuse
    											 + "&amp;isQuantityInvalid="
    											 + isQuantityInvalid
    											 + "&amp;mode=duplicate"
    											 + "&amp;duplicate="+ bNotDuplicate
    											 + "&amp;parentId=" + parentId + "');\"")
    											 .append("/></a>");

    				 }
    				 if(strResolvedPart!=null){
    					 //***************EBOM IS DIFFERENT CHECK***************
    						String[] argsTemp = new String[3];
    						argsTemp[0] = strResolvedPart;
    	    				argsTemp[1] = "to";
    	    				argsTemp[2] = ConfigurationConstants.RELATIONSHIP_EBOM;
    	    				boolean hasEBOMStructureTo = Boolean.parseBoolean(hasRelationship(context,argsTemp));

    					 if(hasEBOMStructureTo){
        					 if (pc.checkForEBOMDifferent(context, strResolvedPart, strObjectId, parentId))
        					 {
        						 stbVisualCue = stbVisualCue
        						 .append(
        						 "<img src=\"../common/images/iconStatusValidationError.gif")
        						 .append("\" border=\"0\"  align=\"middle\" ")
        						 .append("TITLE=\"")
        						 .append(i18EBOMDifferent)
        										 .append("\"").append("/>");
        					 }
    					 }
    					 //DomainObject part = new DomainObject(strResolvedPart);
    					 boolean isLastRev = true;
    					 String current = ConfigurationConstants.EMPTY_STRING;
    					 if(ProductLineCommon.isNotNull(strResolvedPart)){
    						 DomainObject objPart = new DomainObject(strResolvedPart);
    						 Map partInfoMap = objPart.getInfo(context, partInfoSelectable);
    						 String nextRev = (String)partInfoMap.get("next");
    						 current = (String)partInfoMap.get(ConfigurationConstants.SELECT_CURRENT);
    						 if(ProductLineCommon.isNotNull(nextRev)){
    							 isLastRev = false;
    						 }
    					 }
    					 //***************HIGHER REVISION EXISTS CHECK***************
    					 if (!isLastRev)
    					 {
    						 stbVisualCue = stbVisualCue
    						 .append(
    						 "<img src=\"../common/images/iconSmallHigherRevision.gif")
    						 .append("\" border=\"0\"  align=\"middle\" ")
    						 .append("TITLE=\"")
    						 .append(i18HigherRevision)
    										 .append("\"").append("/>");
    					 }
    					 //***************OBSOLETE PART CHECK***************
    					 if ((current).equalsIgnoreCase(strObsolete))
    					 {
    						 stbVisualCue = stbVisualCue
    						 .append(
    						 "<img src=\"../common/images/iconStatusError.gif")
    						 .append("\" border=\"0\"  align=\"middle\" ")
    						 .append("TITLE=\"")
    						 .append(i18ObsoletePart)
    										 .append("\"").append("/>");
    					 }
    				 }
    				 //***************USED IN BOM CHECK***************
    				 if (pc.checkForUsedInBOM(context, strObjectId,strResolvedPart,strLFParentId, parentId))
    				 {
    					 stbVisualCue = stbVisualCue
    					 .append(
    					 "<img src=\"../common/images/iconStatusUsedInEBOM.gif")
    					 .append("\" border=\"0\"  align=\"middle\" ")
    					 .append("TITLE=\"")
    					 .append(i18UsedInBOM)
    									 .append("\"").append("/>");
    				 }
    				 //******************Check for LF obsolete state********************
    				 if(strLFState.equals(strLFObsolete))
    				 {
    					 stbVisualCue = stbVisualCue
    					 .append(
    					 "<img src=\"../common/images/iconStatusError.gif")
    					 .append("\" border=\"0\"  align=\"middle\" ")
    					 .append("TITLE=\"")
    					 .append(i18ObsoleteLF)
    									 .append("\"").append("/>");
    				 }
    				 visualCue.add(stbVisualCue.toString());
    			 }else{
    				 visualCue.add("");
    			 }
    			 }
    		 }else
    		 {
    			 visualCue.add("");
    		 }
    	 } catch (Exception e) {
    		 // TODO Auto-generated catch block
    		 e.printStackTrace();
    	 }
    	 return visualCue;
     }

   /**
     * This method displays visual cue column in Preview BOM page
     *
     * @param  context the eMatrix Context object
     * @param  args holds ObjectList
     * @return Vector
     * @throws Exception if the operation fails
     * @since  R212
     * @author KXB
     */
     public Vector getPartRevisionForDisplay(Context context, String[] args)throws Exception{
    		HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
    		MapList objectList = (MapList) parametersMap.get("objectList");
    		ArrayList parts = new ArrayList();
    		Vector vRevision = new StringList();
    		StringList objListIDs = new StringList(objectList.size());
        	Set<Integer> lst_gaps = new HashSet<Integer>( objectList.size() );
        	//String gaps = "";
        	//IR-148613V6R2013
    		for (int i = 0; i < objectList.size(); i++)
    		{
    		    Map objectMap = (Map) objectList.get(i);
String strLevel = (String)objectMap.get("level");
    		    
    		    if("0".equals(strLevel)){
    		    	vRevision.addElement("");
    		    	return vRevision;
                }
    		    HashMap evaluatedParts = (HashMap) objectMap.get("evaluatedParts");
    		    // get evaluated parts
    		    if(evaluatedParts!=null)
    		    parts = (ArrayList) evaluatedParts.get("partIds");
    		    // if there is only one part then display Unit of Measeure attribute
    		    // of part else display blank
    		    if (parts.size() == 1) {
    		        objListIDs.add((String) parts.get(0));
    		    } else {
    		    	//gaps += i + ",";
        			//IR-148613V6R2013
        			lst_gaps.add( i );
    		    }
    		}
    		MapList mapList = new MapList();
    		if (objListIDs.size() > 0) {
    		    String[] oidsArray = new String[objListIDs.size()];
    		    mapList = DomainObject.getInfo(context, (String[]) objListIDs
    		            .toArray(oidsArray), new StringList(
    		            ConfigurationConstants.SELECT_REVISION));
    		}
    		Iterator itr = mapList.iterator();
    		Map tempMap;
    		for (int j = 0; j < objectList.size(); j++) {
    			boolean b_isAGap = lst_gaps.contains(j);
        		//int index = gaps.indexOf(""+j+"");
        		//if (index >= 0) {
        		if (b_isAGap)  {
    		        vRevision.addElement("");
    		    } else if (itr.hasNext()) {
    		        tempMap = (Map) itr.next();
    		        vRevision.addElement((String) tempMap.get(ConfigurationConstants.SELECT_REVISION));
    		    } else {
    		        vRevision.addElement("");
    		    }
    		}
    		return vRevision;
     }

   /**
     * This method displays Part revision in Preview BOM dialog.
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *              requestMap a HashMap
     * @return Vector Object which holds following parameter state of part
     * @throws Exception if the operation fails
     * @author KXB
     * @since  R212
     */
    public Vector getPartStateForDisplay(Context context, String[] args) throws Exception {
            HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) parametersMap.get("objectList");
            ArrayList parts = new ArrayList();
            Vector vState = new StringList();
            StringList objListIDs = new StringList(objectList.size());
            String gaps = "";      
            
            for (int i = 0; i < objectList.size(); i++) 
            {
                Map objectMap = (Map) objectList.get(i);
                
                String strLevel = (String)objectMap.get("level");
    		    
    		    if("0".equals(strLevel)){
    		    	vState.addElement("");
    		    	return vState;
                }
    		    
                HashMap evaluatedParts = (HashMap) objectMap.get("evaluatedParts");
                // get evaluated parts
                parts = (ArrayList) evaluatedParts.get("partIds");
                // if there is only one part then display Unit of Measeure attribute
                // of part else display blank
                if (parts.size() == 1) {
                    objListIDs.add((String) parts.get(0));
                } else {
                    gaps += i + ",";
                }
            }
                        
            MapList mapList = new MapList();
            if (objListIDs.size() > 0) 
            {
                String[] oidsArray = new String[objListIDs.size()];
                mapList = DomainObject.getInfo(context, (String[]) objListIDs
                        .toArray(oidsArray), new StringList(ConfigurationConstants.SELECT_CURRENT));
            }
            Iterator itr = mapList.iterator();
            Map tempMap;
            for (int j = 0; j < objectList.size(); j++) 
            {
                int index = gaps.indexOf(""+j+"");
                if (index >= 0) 
                {
                	vState.addElement("");
                } else if (itr.hasNext()) 
                {
                    tempMap = (Map) itr.next();
                    vState.addElement((String) tempMap.get(ConfigurationConstants.SELECT_CURRENT));
                } else 
                {
                	vState.addElement("");
                }
            }
            return vState;
     }

   /**
     * This method displays Part type in Preview BOM dialog.
     *
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *              requestMap a HashMap
     * @return Vector Object which holds following parameter state of part
     * @throws Exception if the operation fails
     * @author KXB
     * @since  R212
     */
     public Vector getPartTypeForDisplay(Context context, String[] args) throws Exception {
             HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
             MapList objectList = (MapList) parametersMap.get("objectList");
             ArrayList parts = new ArrayList();
             Vector vType = new StringList();
             StringList objListIDs = new StringList(objectList.size());
             Set<Integer> lst_gaps = new HashSet<Integer>( objectList.size() );
         	//String gaps = "";
         	//IR-148613V6R2013
             for (int i = 0; i < objectList.size(); i++)
             {
                 Map objectMap = (Map) objectList.get(i);

String strLevel = (String)objectMap.get("level");
    		    
    		    if("0".equals(strLevel)){
    		    	vType.addElement("");
    		    	return vType;
                }

                 HashMap evaluatedParts = (HashMap) objectMap.get("evaluatedParts");
                 // get evaluated parts
                 if(evaluatedParts!=null)
                 parts = (ArrayList) evaluatedParts.get("partIds");
                 // if there is only one part then display Unit of Measeure attribute
                 // of part else display blank
                 if (parts.size() == 1) {
                     objListIDs.add((String) parts.get(0));
                 } else {
                	//gaps += i + ",";
         			//IR-148613V6R2013
         			lst_gaps.add( i );
                 }
             }
             MapList mapList = new MapList();
             if (objListIDs.size() > 0)
             {
                 String[] oidsArray = new String[objListIDs.size()];
                 mapList = DomainObject.getInfo(context, (String[]) objListIDs
                         .toArray(oidsArray), new StringList(ConfigurationConstants.SELECT_TYPE));
             }
             Iterator itr = mapList.iterator();
             Map tempMap;
             for (int j = 0; j < objectList.size(); j++)
             {
            	 boolean b_isAGap = lst_gaps.contains(j);

         		//int index = gaps.indexOf(""+j+"");
         		//if (index >= 0) {
         		if (b_isAGap) {
                     vType.addElement("");
                 } else if (itr.hasNext())
                 {
                     tempMap = (Map) itr.next();
                     vType.addElement((String) tempMap.get(ConfigurationConstants.SELECT_TYPE));
                 } else
                 {
                     vType.addElement("");
                 }
             }
             return vType;
     }

   /**
     * This method displays "Unit Of Measure" in Preview BOM dialog.
     *
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *              requestMap a HashMap
     * @return Vector Object which holds following parameter state of part
     * @throws Exception if the operation fails
     * @author KXB
     * @since  R212
     */
     public Vector getUOMForDisplay(Context context, String[] args) throws Exception {
            HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) parametersMap.get("objectList");
            ArrayList parts = new ArrayList();
            Vector vUOM = new StringList();
            StringList objListIDs = new StringList(objectList.size());
            Set<Integer> lst_gaps = new HashSet<Integer>( objectList.size() );
        	//String gaps = "";
        	//IR-148613V6R2013
            for (int i = 0; i < objectList.size(); i++)
            {
                Map objectMap = (Map) objectList.get(i);
	String strLevel = (String)objectMap.get("level");

            	if("0".equals(strLevel)){
            		vUOM.addElement("");
            		return vUOM;
            	}

                HashMap evaluatedParts = (HashMap) objectMap.get("evaluatedParts");
                // get evaluated parts
                if(evaluatedParts!=null)
                parts = (ArrayList) evaluatedParts.get("partIds");
                // if there is only one part then display Unit of Measeure attribute
                // of part else display blank
                if (parts.size() == 1)
                {
                    objListIDs.add((String) parts.get(0));
                }else
                {
                	//gaps += i + ",";
        			//IR-148613V6R2013
        			lst_gaps.add( i );
                }
            }
            MapList mapList = new MapList();
            if (objListIDs.size() > 0) {
                String[] oidsArray = new String[objListIDs.size()];
                mapList = DomainObject.getInfo(context,
                		                      (String[])objListIDs.toArray(oidsArray),
                		                      new StringList("attribute["+ ConfigurationConstants.ATTRIBUTE_UNIT_OF_MEASURE + "]"));
            }
            Iterator itr = mapList.iterator();
            Map tempMap;
            for (int j = 0; j < objectList.size(); j++)
            {
            	//Modified for IR-148613V6R2013
        		boolean b_isAGap = lst_gaps.contains(j);

        		//int index = gaps.indexOf(""+j+"");
        		//if (index >= 0) {
        		if (b_isAGap) {
                    vUOM.addElement("");
                } else if (itr.hasNext())
                {
                    tempMap = (Map) itr.next();
                    vUOM.addElement((String) tempMap
                                    .get("attribute["
                                            + ConfigurationConstants.ATTRIBUTE_UNIT_OF_MEASURE
                                            + "]"));
                } else
                {
                    vUOM.addElement("");
                }
            }
            return vUOM;
     }

   /**
     * This utility method returns MapList which contains featureId, PartId, and
     * quantity as key.
     *
     * @param  strResolvedParts
     *            holds the value of Resolved Parts attribute of product
     *            configuration
     * @return MapList Object which holds following parameter
     * @throws Exception
     * @author KXB
     * @since  R212
     */
     public MapList getResolvedPartsData(String strResolvedParts) throws Exception {
             MapList resolvedParts = new MapList();
             if (strResolvedParts != null && (strResolvedParts.length() > 0)) {
                 StringTokenizer stResolvedParts = new StringTokenizer(strResolvedParts, ",");
                 while (stResolvedParts.hasMoreTokens()) {
                     HashMap hmDuplicatePart = new HashMap();
                     String strFeatureData = stResolvedParts.nextToken();
                     int indexForFeature = strFeatureData.indexOf("|");
                     if (indexForFeature != 1) {
                         String strFeature = strFeatureData.substring(0,indexForFeature);
                         hmDuplicatePart.put("featureId", strFeature);
                         String strPartData = strFeatureData.substring(indexForFeature + 1, strFeatureData.length());
                         int indexForPart = strPartData.indexOf("|");
                         if (indexForPart != (-1)) {
                             String strPart = strPartData.substring(0, indexForPart);
                             hmDuplicatePart.put("partId", strPart);
                             String strQuantity = strPartData.substring(indexForPart + 1, strPartData.length());
                             hmDuplicatePart.put("quantity", strQuantity);
                         } else {
                             hmDuplicatePart.put("partId", strPartData);
                         }
                     }
                     resolvedParts.add(hmDuplicatePart);
                 }
             }
             return resolvedParts;
      }


   /**
     * Gets EBOMs with selectables
     *
     * @param  context the eMatrix <code>Context</code> object.
     * @param  args holds objectId.
     * @return MapList of EBOM Objects.
     * @throws Exception if the operation fails
     * @author KXB
     * @since  FTR R212
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllEBOMWithSelects(Context context, String[] args)throws Exception{
        MapList ebomList = new MapList();
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String productConfigId = (String) paramMap.get("objectId");
        DomainObject domObject = new DomainObject(productConfigId);
        StringList objectSelects = new StringList(2);
        objectSelects.addElement(ConfigurationConstants.SELECT_ID);
        objectSelects.addElement(ConfigurationConstants.SELECT_NAME);
        short recurseLevel;
        String strExpandLevel = (String) paramMap.get("expandLevel");
        // if expnad level is not available
        if (strExpandLevel == null || ("".equals(strExpandLevel)) || ("null".equals(strExpandLevel))) {
        	recurseLevel = (short) 1;
        } else if (strExpandLevel.equalsIgnoreCase((ConfigurationConstants.RANGE_VALUE_ALL))){
            recurseLevel = (short) 0;
        } else {
        	recurseLevel = (short)Short.parseShort(strExpandLevel);
        }
        ConfigurationUtil confUtil = null;
        StringList relSelects = new StringList(6);
        relSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
        relSelects.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        relSelects.addElement(SELECT_ATTRIBUTE_QUANTITY);
        relSelects.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
        relSelects.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        relSelects.addElement(SELECT_ATTRIBUTE_USAGE);
        String strTopLevelObject = "";
        String strCtxtObjType = domObject.getInfo(context,ConfigurationConstants.SELECT_TYPE);
        if(mxType.isOfParentType(context, strCtxtObjType,ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)){
            strTopLevelObject = domObject.getInfo(context, "from["+ ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART
                                                           + "].to.id");
            if (strTopLevelObject != null && strTopLevelObject.length() > 0) {
                try {
                	confUtil = new ConfigurationUtil(productConfigId);
                    ebomList = (MapList)confUtil.getObjectStructure(context,
                                                                    ConfigurationConstants.TYPE_PART,
                                                                    ConfigurationConstants.RELATIONSHIP_TOP_LEVEL_PART,
                                                                    objectSelects,
                                                                    relSelects,
                                                                    false,
                                                                    true,
                                                                    recurseLevel,
                                                                    0,
                                                                    ConfigurationConstants.EMPTY_STRING,
                                                                    DomainObject.EMPTY_STRING,
                                                                    (short) 0,"");
                }catch (FrameworkException Ex) {
                    throw Ex;
                }
            }
        }else{
            try{
            	confUtil = new ConfigurationUtil(productConfigId);
                ebomList = (MapList)confUtil.getObjectStructure(context,
                                                                ConfigurationConstants.TYPE_PART,
                                                                ConfigurationConstants.RELATIONSHIP_EBOM,
                                                                objectSelects,
                                                                relSelects,
                                                                false,
                                                                true,
                                                                recurseLevel,
                                                                0,
                                                                ConfigurationConstants.EMPTY_STRING,
                                                                DomainObject.EMPTY_STRING,
                                                                (short) 0,"");
               }catch (FrameworkException Ex)
               {
                  throw Ex;
               }
        }
        return ebomList;
    }


    /**
     * Method to return the Technical feature structure of Product Variant.
     *
     * @param context
     * @param strProductID
     * @param strProductVariantID
     * @return MapList - Technical feature structure of Product Variant.
     * @throws Exception
     * this could not be deprecated as referenced from migration JPO
     */
    public MapList getProductVariantsTechnicalStructre(Context context,
            String strProductID, String strProductVariantID) throws Exception {
        MapList mlFinalTechStructure = new MapList();
        DomainObject productDomObject = new DomainObject(strProductID);
        String strType = productDomObject.getInfo(context,
                DomainObject.SELECT_TYPE);

        StringList lstPVObjectSelects = new StringList(
                ConfigurationConstants.SELECT_ID);

        String strRelType = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM
                + "," + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO;

        StringList selectStmts = new StringList();

        selectStmts.addElement("from["
                + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO + "].to."
                + ConfigurationConstants.SELECT_ID);
        selectStmts.addElement("from["
                + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO + "].to."
                + ConfigurationConstants.SELECT_NAME);
        selectStmts.addElement("from["
                + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO + "].to."
                + ConfigurationConstants.SELECT_TYPE);
        selectStmts.addElement("from["
                + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO + "].to."
                + ConfigurationConstants.SELECT_REVISION);
        selectStmts.addElement(ConfigurationConstants.SELECT_ID);
        selectStmts.addElement(ConfigurationConstants.SELECT_TYPE);
        selectStmts.addElement(STR_ATTRIBUTE_OPEN_BRACE
                + ConfigurationConstants.ATTRIBUTE_RULE_TYPE + EXPRESSION_CLOSE);
        selectStmts.addElement(STR_ATTRIBUTE_OPEN_BRACE
                + ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE
                + EXPRESSION_CLOSE);
        selectStmts.addElement(STR_ATTRIBUTE_OPEN_BRACE
                + ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE
                + EXPRESSION_CLOSE);
        selectStmts.addElement(STR_ATTRIBUTE_OPEN_BRACE
                + ConfigurationConstants.ATTRIBUTE_USAGE + EXPRESSION_CLOSE);
        selectStmts.addElement(STR_ATTRIBUTE_OPEN_BRACE
                + ConfigurationConstants.ATTRIBUTE_QUANTITY + EXPRESSION_CLOSE);

        StringBuffer sbWhereClause = new StringBuffer(200);
        StringBuffer sbPVWhereClause = new StringBuffer(100);
        sbPVWhereClause.append("(type== \"");
        sbPVWhereClause.append(ConfigurationConstants.TYPE_FEATURE_LIST);
        sbPVWhereClause.append("\" && (");
        sbPVWhereClause.append("attribute[");
        sbPVWhereClause.append(ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE);
        sbPVWhereClause.append("]==");
        sbPVWhereClause.append("\"");
        sbPVWhereClause.append(ConfigurationConstants.RANGE_VALUE_TECHNICAL);
        sbPVWhereClause.append("\")");
        sbPVWhereClause.append(")");

        sbWhereClause.append("(" + sbPVWhereClause.toString());

        StringList lstChildren = ProductLineUtil.getChildrenTypes(context,
                ConfigurationConstants.TYPE_FEATURES);
        for (int i = 0; i < lstChildren.size(); i++) {
            sbWhereClause.append("|| (");
            sbWhereClause.append("type== \"");
            sbWhereClause.append((String) lstChildren.elementAt(i));
            sbWhereClause.append("\"");
            sbWhereClause.append(")");
        }
        lstChildren = ProductLineUtil.getChildrenTypes(context,
                ConfigurationConstants.TYPE_PRODUCTS);
        for (int i = 0; i < lstChildren.size(); i++) {
            sbWhereClause.append("|| (");
            sbWhereClause.append("type== \"");
            sbWhereClause.append((String) lstChildren.elementAt(i));
            sbWhereClause.append("\"");
            sbWhereClause.append(")");
        }
        sbWhereClause.append(")");

        try {
            if (!strType
                    .equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_LINE)) {
                if (strProductVariantID != null
                        && !("null".equals(strProductVariantID))
                        && !("".equals(strProductVariantID))) {
                    DomainObject pvDomObject = new DomainObject(
                            strProductVariantID);
                    String strTypeTemp = pvDomObject.getInfo(context,
                            DomainObject.SELECT_TYPE);

                    if (strTypeTemp
                            .equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_VARIANT)
                            && ((!strType
                                    .equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_VARIANT)))) {

                        MapList mlPVTechStructure = pvDomObject
                                .getRelatedObjects(
                                        context,
                                        ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST,
                                        ConfigurationConstants.TYPE_FEATURE_LIST,
                                        lstPVObjectSelects, new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID), false, true,
                                        (short) 1, sbPVWhereClause.toString(),
                                        "", 0);

                        MapList mlProductTechStructure = productDomObject
                                .getRelatedObjects(context, strRelType, "*",
                                        false, true, (short) 0, selectStmts,
                                        new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID), sbWhereClause.toString(), null,
                                        null,
                                        ConfigurationConstants.TYPE_FEATURE_LIST,
                                        null);

                        String strId = null;

                        for (int i = 0; i < mlProductTechStructure.size(); i++) {
                            Hashtable mpTechFtr = (Hashtable) mlProductTechStructure
                                    .get(i);
                            strId = mpTechFtr.get(ConfigurationConstants.SELECT_ID)
                                    .toString();
                            strRelType = mpTechFtr.get("relationship")
                                    .toString();
                            for (int j = 0; j < mlPVTechStructure.size(); j++) {
                                if (strId.equals(((Map) mlPVTechStructure
                                        .get(j)).get(ConfigurationConstants.SELECT_ID)
                                        .toString())
                                        && strRelType
                                                .equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM)) {
                                    mlFinalTechStructure
                                            .add((Map) mlProductTechStructure
                                                    .get(i));
                                }
                            }
                        }
                        return mlFinalTechStructure;
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return mlFinalTechStructure;
    }

    /**
     *
     * This method will be called to get the part related data for features
     * or products and also effective list of DVs on a tech feature
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param arrFeatureIds
     *            the ArrayList <code>ArrayList</code> object
     * @param strProdVariantId
     *            the String <code>String</code> object
     * @return Returns a <code>ArrayList</code> object. The return object is
     *         ArrayList, which will contain all the Part ObjectId for a given
     *         list of Features.
     * @throws Exception
     *             if the operation fails
     * @since PRC 10.5.1.2
     * this could not be deprecated as referenced from migration JPO
     */
   //Method added for Bug No. 377168
    public ArrayList getPartRuleData(Context context,ArrayList arrFeatureIds, String strProdVariantId) throws Exception
     {
        ArrayList arrPartObjectIds = new ArrayList();
        MapList relObjects = new MapList();
        boolean isVariant =  false;
        List listActiveDV = new StringList();
        MapList listPartObjects = new MapList();

        if(strProdVariantId != null && ! "null".equalsIgnoreCase(strProdVariantId) && ! "".equals(strProdVariantId))
        {
            if((new DomainObject(strProdVariantId).getInfo(context,
                    ConfigurationConstants.SELECT_TYPE)).equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_VARIANT))
                isVariant = true;
        }
        for (int arrCount = 0; arrCount < arrFeatureIds.size(); arrCount++) {
            try {
                String strFeatureObjectId = (String) arrFeatureIds
                        .get(arrCount);
                DomainObject domFeatureObject = new DomainObject(
                        strFeatureObjectId);
                StringList objectSelects = new StringList();
                StringList relSelects = new StringList();

                relSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
                relSelects.addElement(ConfigurationConstants.SELECT_EFFECTIVITY_STATUS);

                String strRelPattern = ConfigurationConstants.RELATIONSHIP_GBOM_FROM +","+ ConfigurationConstants.RELATIONSHIP_VARIES_BY;
                String strObjPattern = ConfigurationConstants.TYPE_GBOM +","+ConfigurationConstants.TYPE_FEATURES;

                // to get option id, type, name, revision.
                objectSelects.addElement("from["
                        + ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].to."
                        + ConfigurationConstants.SELECT_ID);
                objectSelects.addElement("from["
                        + ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].to."
                        + ConfigurationConstants.SELECT_TYPE);
                objectSelects.addElement("from["
                        + ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].to."
                        + ConfigurationConstants.SELECT_NAME);
                objectSelects.addElement("from["
                        + ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].to."
                        + ConfigurationConstants.SELECT_REVISION);

                objectSelects.addElement(ConfigurationConstants.SELECT_ID);
                objectSelects.addElement(STR_ATTRIBUTE_OPEN_BRACE
                        + ConfigurationConstants.ATTRIBUTE_RULE_TYPE
                        + EXPRESSION_CLOSE);
                objectSelects.addElement(STR_ATTRIBUTE_OPEN_BRACE
                        + ConfigurationConstants.ATTRIBUTE_EXPRESSION
                        + EXPRESSION_CLOSE);

                short sRecurseLevel = 1;
                // getting the parts related to one feature
                relObjects = domFeatureObject.getRelatedObjects(context,
                                                                strRelPattern,
                                                                strObjPattern,
                                                                objectSelects,
                                                                relSelects,
                                                                false,
                                                                true,
                                                                sRecurseLevel,
                                                                "",
                                                                "", 0);

                for(int i = 0; i < relObjects.size(); i++ )
                {
                    Map tempMap = (Map)relObjects.get(i);
                    String strRelName = (String)tempMap.get("relationship");
                    if(strRelName.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_VARIES_BY))
                    {
                        String strEffStatus = (String)tempMap.get(ConfigurationConstants.SELECT_EFFECTIVITY_STATUS);
                        if(strEffStatus.equalsIgnoreCase(ConfigurationConstants.EFFECTIVITY_STATUS_ACTIVE))
                        {
                            String strRelId = (String)tempMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
                            String strDVId = (String)tempMap.get(ConfigurationConstants.SELECT_ID);
                            if(isVariant)
                            {
                                boolean isInvalid = isInvalid(context,strRelId,strProdVariantId);

                                if(!isInvalid)
                                    listActiveDV.add(strDVId);
                            }else
                                listActiveDV.add(strDVId);

                        }
                    }else
                        listPartObjects.add(tempMap);
                }

                for(int i = 0; i < listPartObjects.size(); i++ )
                {
                    Map tempMap = (Map)listPartObjects.get(i);

                    tempMap.put("listActiveDVs",listActiveDV);
                }


               arrPartObjectIds.add(listPartObjects);
            } catch (Exception e) {
                throw e;
            }
        }
        return arrPartObjectIds;
    }
    //moved from Feature.java to here for getPartRuleData(Context context,ArrayList arrFeatureIds, String strProdVariantId) method
    public boolean isInvalid(Context context, String strRelId, String strProdId)throws Exception
    {

        String strMqlCmd = DomainConstants.EMPTY_STRING;
        
        strMqlCmd = "print connection \"" + strRelId + "\" select tomid["
                + ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY
                + "].id dump |";
        String strIVBRelIds = MqlUtil.mqlCommand(context, strMqlCmd, true);

        if (strIVBRelIds != null && !(strIVBRelIds.equals(""))
                && !("null".equalsIgnoreCase(strIVBRelIds)))

        {
            List PVList = new StringList();
            StringTokenizer st = new StringTokenizer(strIVBRelIds, "|");

            while (st.hasMoreTokens()) {
                String strIVBRelId = st.nextToken();

                strMqlCmd = "print connection \"" + strIVBRelId
                        + "\" select fromrel.id dump";
                String strProdFeatListRelId = MqlUtil.mqlCommand(context,
                        strMqlCmd, true);
                strProdFeatListRelId = strProdFeatListRelId.trim();

                strMqlCmd = "print connection \"" + strProdFeatListRelId
                        + "\" select from.id dump";
                String strPVId = MqlUtil.mqlCommand(context, strMqlCmd,
                        true);
                strPVId = strPVId.trim();

                PVList.add(strPVId);
            }
            if (PVList.contains(strProdId))
            {
              return true;
            }
        }

         return false;
    }



    //---------------------------------------
    /**
     * This is background job executed when part is attached/removed/replaced from equipment feature
     * @param context
     * @param args
     * @throws Exception
     * @since R212
     */
    public void modifyEquipmentListReportXML(Context context, String[] args) throws Exception
    {
        String productID = args[1];
        try {
        		DomainObject domProduct = new DomainObject(productID);
                StringList objectSelects = new StringList();
                objectSelects.add(DomainObject.SELECT_ID);
                objectSelects.add(DomainObject.SELECT_TYPE);

                domProduct.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_EQUIPMENT_LIST_REPORT_XML,"");

                Product product= new Product(productID);
                MapList mlEquipmentFeature= product.getLogicalFeatureStructure(context, ConfigurationConstants.TYPE_EQUIPMENT_FEATURE, ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES, objectSelects, new StringList(), false, true, -1, 0, ConfigurationConstants.EMPTY_STRING, ConfigurationConstants.EMPTY_STRING, DomainObject.FILTER_ITEM, DomainObject.EMPTY_STRING);

                // Iterate the MapList of Equipment Features and call the actuall Method to generate the XML for Equipment Report.
                if(mlEquipmentFeature!=null && mlEquipmentFeature.size()>0){
                    String[] arrEqp = new String[4];
                    String tempId ="";
                    arrEqp[1] = "";
                    arrEqp[2] = "add";
                    arrEqp[3] = productID;
                    
                    for (int i = 0; i < mlEquipmentFeature.size(); i++) {
                       String eqpId = (String) ((Map) mlEquipmentFeature.get(i)).get(DomainObject.SELECT_ID);
                       tempId= tempId + eqpId +"|";
                    }
                    arrEqp[0] = tempId;
                    modifyEquipmentListReportXMLGeneration(context,arrEqp);
                 }

            } catch (Exception e) {
            e.printStackTrace();
            throw new FrameworkException(e);
       }
    }

    /**
	 * This method will call for each Equipment Feature and get its Part,
	 * depending on the Inclusion Rule , List will be populated which will be
	 * finally used to create XML
	 *
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since R212
	 */

    private void modifyEquipmentListReportXMLGeneration(Context context, String[] args) throws Exception
    {
    	String strEqpFeatureId = args[0];
        String strConnect = args[2];
        String productID = args[3];

        String STR_DELETE = "delete";
        
        String STR_Check_Token = "|";

        StringList objectSelects = new StringList("physicalid");
        StringList relSelects = new StringList(DomainRelationship.SELECT_ID);
    	StringList selectStmtEquip = new StringList();
    	selectStmtEquip.add(ConfigurationConstants.SELECT_TYPE);
    	selectStmtEquip.add(ConfigurationConstants.SELECT_NAME);
    	selectStmtEquip.add(ConfigurationConstants.SELECT_REVISION);
    	selectStmtEquip.add("to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES+"].id");

        List mapListReport = null;

        try {

        	DomainObject domObject = new DomainObject(productID);
        	String productPhyID=domObject.getInfo(context,"physicalid");
        	mapListReport = new MapList();

        	Product product= new Product(productID);
        	//This will get the complete structure of the product,will extract Logical and Configuration Structure
        	MapList mlCompleteFeatureStructure= product.getCompleteFeatureStructure(context, DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING, objectSelects,relSelects, false, true, 0, 0, ConfigurationConstants.EMPTY_STRING, ConfigurationConstants.EMPTY_STRING, DomainObject.FILTER_ITEM, DomainObject.EMPTY_STRING);
        	//extract Physical IDs of OBJ and REL
        	StringList slAllFeaturePhysicalIDs = getObjectRelPhyIdsFromMap(context, mlCompleteFeatureStructure);


        	StringTokenizer stTempNew = new StringTokenizer(strEqpFeatureId,
					STR_Check_Token);
            //iterate for all Equipment Feature

        	while (stTempNew.hasMoreTokens()) {
        		List eqpListNew = new MapList();
        		String newEqpFTRID = (String) stTempNew.nextToken();

        		DomainObject equipObj = new DomainObject(newEqpFTRID);
        		Map equipBasics = equipObj.getInfo(context, selectStmtEquip);

        		HashMap hmFtrTemp = new HashMap();
        		hmFtrTemp.put(ConfigurationConstants.SELECT_ID, newEqpFTRID);
        		hmFtrTemp.putAll(equipBasics);

        		//Add equipment Feature Info
        		eqpListNew.add(hmFtrTemp);

        		List al = new ArrayList();
        		al.add(newEqpFTRID);

        		// partList has list of parts attached to feature
        		List partList = getPartObjectDetails(context, al);

        		// is to check if the Part has Inclusion/Exclusion rule defined.
        		// if there is no rule defined on the part then it will be added to the Equipment report of the product
        		// if there is a inclusion rule defined then there will be check to see if the the inclusion rule includes the parent feature

        		if (partList.size() > 0) {
        			//list will hold all active GBOM Parts
        			MapList mapList = (MapList) partList.get(0);

        			//for each Part connected to current Equiment Feature
        			for (int jx = 0; jx < mapList.size(); jx++) {
        				Map map = (Map) mapList.get(jx);
        				HashMap newPartTemp = new HashMap();

        				String strRightExpression ="";
        				String strLeftExpression="";
        				String strInclusionRuleID="";

        				String strInclusionExclusion = (String) map.get("attribute["+ConfigurationConstants.ATTRIBUTE_RULE_TYPE+"]");
        				if( map.get("tomid["
        						+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        						+ "].from.attribute["
        						+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
        						+ "]")!=null){
        					strRightExpression = (String) map.get("tomid["
        							+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        							+ "].from.attribute["
        							+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
        							+ "]");
        				}
        				if(map.get("tomid["
        						+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        						+ "].from.attribute["
        						+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
        						+ "]")!=null){
        					strLeftExpression =(String) map.get("tomid["
        							+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        							+ "].from.attribute["
        							+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
        							+ "]");

        				}
        				if( map.get("tomid["
        						+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        						+ "].from.id")!=null){
        					strInclusionRuleID=(String) map.get("tomid["
        							+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
        							+ "].from.id");
        				}

        				if(strInclusionExclusion!=null && strInclusionExclusion.equalsIgnoreCase("Inclusion")){
							//---------------------------------------------------------------------------
    						//----------------------IF IT HAS INCLUSION RULE-----------------------------
							//---------------------------------------------------------------------------
        					if(strRightExpression!=null && !strRightExpression.trim().isEmpty()){

        						strRightExpression = strRightExpression.replaceAll("\\(","");
        						strRightExpression = strRightExpression.replaceAll("\\)","");
        						strRightExpression = strRightExpression.trim();
                                //TODO- Need to replace B with T/O
        						StringTokenizer strRightExpressionToken = new StringTokenizer(strRightExpression," ");
        						if(strRightExpressionToken!=null){
        							boolean hasToIncludePart = false;

        							//---------------------------------------------------------------------------
        							// if the complex inclusion rule contains the Both AND and OR conditions
        							//---------------------------------------------------------------------------
        							if(strRightExpression.contains("AND") && strRightExpression.contains("OR")){
        								// tokenizing for Object IDs with OR
        								String[] strArrayORSplit=strRightExpression.split("OR");
        								int iTotalAndCount = 0;
        								int iIncludeCount = 0;
        								int iObjectsInProducts = 0;

        								int iTotalObjectsInRuleForOR = strArrayORSplit.length;
        								// iterate with OR and check if there are any AND conditions in the Rule
        								for(int j=0;j<iTotalObjectsInRuleForOR;j++){
        									String strRightExpressionID = strArrayORSplit[j].trim();
        									// check if there is any AND condition
        									if(strRightExpressionID.contains("AND")){
        										String[] strArrayANDSplit=strRightExpression.split("AND");
        										int iTotalObjectsInRule = strArrayANDSplit.length;
        										iTotalAndCount = iTotalAndCount + iTotalObjectsInRule;
        										for(int k =0;k<strArrayANDSplit.length;k++){
        											String strRightExpressionIDTemp = strArrayANDSplit[k].trim();
        											String strRightExpressionIDTokenized = strRightExpressionIDTemp.substring(1);
        											DomainObject domTempExpression = new DomainObject(strRightExpressionIDTokenized);
        											if (!domTempExpression.exists(context)) {
        												//it is relationship, check if with PFL as well this is connected
        												if(slAllFeaturePhysicalIDs.contains(strRightExpressionIDTokenized)){
        													iIncludeCount ++;
        												}
        											} else {
        												String strObjectType = (String) domTempExpression.getInfo(context,DomainObject.SELECT_TYPE);
        												if(mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_PRODUCTS)
        														&& strRightExpressionID.equals(productPhyID)){
        													iIncludeCount ++;
        												}
        												else{
        													// If the Rule is connected to the feature then get the Rel ID of the feature and check if the rel ID
        													// id connected to PRD
        													if(isFeatureConnectedToProduct(context,domTempExpression,slAllFeaturePhysicalIDs)){
        														iIncludeCount ++;
        													}
        												}
        											}

        										}
        									}else{
        										String strRightExpressionIDTokenized = strRightExpressionID.substring(1);
        										DomainObject domTempExpression = new DomainObject(strRightExpressionIDTokenized);
        										if (!domTempExpression.exists(context)) {
        											if(slAllFeaturePhysicalIDs.contains(strRightExpressionIDTokenized)){
        												iObjectsInProducts ++;
        											}
        										}else{
        											String strObjectType = (String) domTempExpression.getInfo(context,DomainObject.SELECT_TYPE);

        											if(mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_PRODUCTS)
        													&& strRightExpressionID.equals(productPhyID)){
        												iObjectsInProducts ++;
        											}
        											else{
        												// If the Rule is connected to the feature then get the Rel ID of the feature and check if the rel ID
        												// id connected to PRD
        												if(isFeatureConnectedToProduct(context,domTempExpression,slAllFeaturePhysicalIDs)){
        													iObjectsInProducts ++;
        												}
        											}
        										}

        									}
        								}
        								if(iObjectsInProducts>0 || iTotalAndCount ==iIncludeCount){
        									hasToIncludePart = true;
        								}

        								if(hasToIncludePart){
        									// if the inclusion rule had the feature then add the part.
        									newPartTemp.put(ConfigurationConstants.SELECT_ID, map.get(ConfigurationConstants.SELECT_ID));
        									newPartTemp.put(ConfigurationConstants.SELECT_TYPE, map.get(ConfigurationConstants.SELECT_TYPE));
        									newPartTemp.put(ConfigurationConstants.SELECT_NAME, map.get(ConfigurationConstants.SELECT_NAME));
        									newPartTemp.put(ConfigurationConstants.SELECT_REVISION, map.get(ConfigurationConstants.SELECT_REVISION));
        									newPartTemp.put("GBOMRELID", map.get(DomainRelationship.SELECT_ID));
        									newPartTemp.put("iRuleID", strInclusionRuleID);
        									newPartTemp.put("LeftExpr", strLeftExpression);
        									newPartTemp.put("RightExp", strRightExpression);
        									eqpListNew.add(newPartTemp);
        								}
        							}
        							//---------------------------------------------------------------------------
        							// if the complex inclusion rule contains the only AND conditions
        							//---------------------------------------------------------------------------
        							else if(strRightExpression.contains("AND")){
        								int iTotalObjectsInRule = 0;
        								int iObjectsInProducts = 0;
        								while(strRightExpressionToken.hasMoreTokens()){
        									String rightExpressionPhyID= strRightExpressionToken.nextToken().trim();
        									if(!rightExpressionPhyID.equalsIgnoreCase("AND")){
        										iTotalObjectsInRule++;
        										String strRightExpressionID = rightExpressionPhyID.substring(1);
        										DomainObject domTempExpression = new DomainObject(strRightExpressionID.trim());
        										if (!domTempExpression.exists(context)) {
        											//it is relationship, check if with PFL as well this is connected
        											if(slAllFeaturePhysicalIDs.contains(strRightExpressionID)){
        												iObjectsInProducts ++;
        											}
        										}else{
        											String strObjectType = (String) domTempExpression.getInfo(context,DomainObject.SELECT_TYPE);
        											if(mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_PRODUCTS)
        													&& strRightExpressionID.equals(productPhyID)){
        												iObjectsInProducts ++;
        											}
        											else{
        												// If the Rule is connected to the feature then get the Rel ID of the feature and check if the rel ID
        												// id connected to PRD
        												if(isFeatureConnectedToProduct(context,domTempExpression,slAllFeaturePhysicalIDs)){
        													iObjectsInProducts ++;
        												}
        											}
        										}
        									}
        								}
        								if(iTotalObjectsInRule == iObjectsInProducts){
        									hasToIncludePart = true;
        								}
        								// if the objects used in Inclusion rule are all connected to the product then
        								// include the part
        								if(hasToIncludePart){
        									// if the inclusion rule had the feature then add the part.
        									newPartTemp.put(ConfigurationConstants.SELECT_ID, map.get(ConfigurationConstants.SELECT_ID));
        									newPartTemp.put(ConfigurationConstants.SELECT_TYPE, map.get(ConfigurationConstants.SELECT_TYPE));
        									newPartTemp.put(ConfigurationConstants.SELECT_NAME, map.get(ConfigurationConstants.SELECT_NAME));
        									newPartTemp.put(ConfigurationConstants.SELECT_REVISION, map.get(ConfigurationConstants.SELECT_REVISION));
        									newPartTemp.put("GBOMRELID", map.get(DomainRelationship.SELECT_ID));
        									newPartTemp.put("iRuleID", strInclusionRuleID);
        									newPartTemp.put("LeftExpr", strLeftExpression);
        									newPartTemp.put("RightExp", strRightExpression);
        									eqpListNew.add(newPartTemp);
        								}
        							}
        							//---------------------------------------------------------------------------
        							// if the complex inclusion rule contains the only OR conditions
        							//---------------------------------------------------------------------------
        							else if(strRightExpression.contains("OR")){
        								int iObjectsInProducts = 0;
        								while(strRightExpressionToken.hasMoreTokens()){
        									String rightExpressionPhyID= strRightExpressionToken.nextToken().trim();
        									if(!rightExpressionPhyID.equalsIgnoreCase("OR")){
        										String strRightExpressionID = rightExpressionPhyID.substring(1);
        										DomainObject domTempExpression = new DomainObject(strRightExpressionID.trim());
        										if (!domTempExpression.exists(context)) {
        											if(slAllFeaturePhysicalIDs.contains(strRightExpressionID)){
        												iObjectsInProducts ++;
        											}
        										}else{
        											String strObjectType = (String) domTempExpression.getInfo(context,DomainObject.SELECT_TYPE);
        											if(mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_PRODUCTS)
        													&& strRightExpressionID.equals(productPhyID)){
        												iObjectsInProducts ++;
        											}
        											else{
        												// If the Rule is connected to the feature then get the Rel ID of the feature and check if the rel ID
        												// id connected to PRD
        												if(isFeatureConnectedToProduct(context,domTempExpression,slAllFeaturePhysicalIDs)){
        													iObjectsInProducts ++;
        												}
        											}
        										}
        									}
        								}
        								if(iObjectsInProducts>0){
        									hasToIncludePart = true;
        								}

        								if(hasToIncludePart){
        									// if the inclusion rule had the feature then add the part.
        									newPartTemp.put(ConfigurationConstants.SELECT_ID, map.get(ConfigurationConstants.SELECT_ID));
        									newPartTemp.put(ConfigurationConstants.SELECT_TYPE, map.get(ConfigurationConstants.SELECT_TYPE));
        									newPartTemp.put(ConfigurationConstants.SELECT_NAME, map.get(ConfigurationConstants.SELECT_NAME));
        									newPartTemp.put(ConfigurationConstants.SELECT_REVISION, map.get(ConfigurationConstants.SELECT_REVISION));
        									newPartTemp.put("GBOMRELID", map.get(DomainRelationship.SELECT_ID));
        									newPartTemp.put("iRuleID", strInclusionRuleID);
        									newPartTemp.put("LeftExpr", strLeftExpression);
        									newPartTemp.put("RightExp", strRightExpression);
        									eqpListNew.add(newPartTemp);
        								}
        							}
        							//---------------------------------------------------------------------------
        							// If the Complex Inclusion Rule doesn't contain AND or OR condition
        							//---------------------------------------------------------------------------
        							else{
        								while(strRightExpressionToken.hasMoreTokens()){
        									String strRightExpressionID = strRightExpressionToken.nextToken().trim();
        									strRightExpressionID = strRightExpressionID.substring(1);
        									DomainObject domTempExpression = new DomainObject(strRightExpressionID);
        									if (!domTempExpression.exists(context)) {
        										if(slAllFeaturePhysicalIDs.contains(strRightExpressionID)){
        											hasToIncludePart = true;
        										}
        									}else{
        										String strObjectType = (String) domTempExpression.getInfo(context,DomainObject.SELECT_TYPE);
        										if(mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_PRODUCTS)
        												&& strRightExpressionID.equals(productPhyID)){
        											newPartTemp.put(ConfigurationConstants.SELECT_ID, map.get(ConfigurationConstants.SELECT_ID));
        											newPartTemp.put(ConfigurationConstants.SELECT_TYPE, map.get(ConfigurationConstants.SELECT_TYPE));
        											newPartTemp.put(ConfigurationConstants.SELECT_NAME, map.get(ConfigurationConstants.SELECT_NAME));
        											newPartTemp.put(ConfigurationConstants.SELECT_REVISION, map.get(ConfigurationConstants.SELECT_REVISION));
        											newPartTemp.put("GBOMRELID", map.get(DomainRelationship.SELECT_ID));
        											newPartTemp.put("iRuleID", strInclusionRuleID);
        											newPartTemp.put("LeftExpr", strLeftExpression);
        											newPartTemp.put("RightExp", strRightExpression);
        											eqpListNew.add(newPartTemp);
        										}else{
        											// If the Rule is connected to the feature then get the Rel ID of the feature and check if the rel ID
        											// id connected to PRD
        											if(isFeatureConnectedToProduct(context,domTempExpression,slAllFeaturePhysicalIDs)){
        												// if the inclusion rule had the feature then add the part.
        												newPartTemp.put(ConfigurationConstants.SELECT_ID, map.get(ConfigurationConstants.SELECT_ID));
        												newPartTemp.put(ConfigurationConstants.SELECT_TYPE, map.get(ConfigurationConstants.SELECT_TYPE));
        												newPartTemp.put(ConfigurationConstants.SELECT_NAME, map.get(ConfigurationConstants.SELECT_NAME));
        												newPartTemp.put(ConfigurationConstants.SELECT_REVISION, map.get(ConfigurationConstants.SELECT_REVISION));
        												newPartTemp.put("GBOMRELID", map.get(DomainRelationship.SELECT_ID));
        												newPartTemp.put("iRuleID", strInclusionRuleID);
        												newPartTemp.put("LeftExpr", strLeftExpression);
        												newPartTemp.put("RightExp", strRightExpression);
        												eqpListNew.add(newPartTemp);
        											}
        										}
        									}
        									if(hasToIncludePart){
        										// if the inclusion rule had the feature then add the part.
        										newPartTemp.put(ConfigurationConstants.SELECT_ID, map.get(ConfigurationConstants.SELECT_ID));
        										newPartTemp.put(ConfigurationConstants.SELECT_TYPE, map.get(ConfigurationConstants.SELECT_TYPE));
        										newPartTemp.put(ConfigurationConstants.SELECT_NAME, map.get(ConfigurationConstants.SELECT_NAME));
        										newPartTemp.put(ConfigurationConstants.SELECT_REVISION, map.get(ConfigurationConstants.SELECT_REVISION));
        										newPartTemp.put("GBOMRELID", map.get(DomainRelationship.SELECT_ID));
        										newPartTemp.put("iRuleID", strInclusionRuleID);
        										newPartTemp.put("LeftExpr", strLeftExpression);
        										newPartTemp.put("RightExp", strRightExpression);
        										eqpListNew.add(newPartTemp);
        									}
        								}
        							}
        						}
        					}
        					else{
    							//---------------------------------------------------------------------------
        						//----------------------IF THERE IS NO INCLUSION RULE------------------------
    							//---------------------------------------------------------------------------
        						newPartTemp.put(ConfigurationConstants.SELECT_ID, map.get(ConfigurationConstants.SELECT_ID));
        						newPartTemp.put(ConfigurationConstants.SELECT_TYPE, map.get(ConfigurationConstants.SELECT_TYPE));
        						newPartTemp.put(ConfigurationConstants.SELECT_NAME, map.get(ConfigurationConstants.SELECT_NAME));
        						newPartTemp.put(ConfigurationConstants.SELECT_REVISION, map.get(ConfigurationConstants.SELECT_REVISION));
        						newPartTemp.put("GBOMRELID", map.get(DomainRelationship.SELECT_ID));
        						newPartTemp.put("iRuleID", strInclusionRuleID);
        						newPartTemp.put("LeftExpr", strLeftExpression);
        						newPartTemp.put("RightExp", strRightExpression);
        						eqpListNew.add(newPartTemp);
        					}
        				}
        			}
        		}
        		mapListReport.add(eqpListNew);
        	}

        	equipmentListReportXMLUpdate(context, mapListReport, productID);
        	// TODO - Need to check this usecase his Block is executed if equipment feature is deleted.
        	if (strConnect.equalsIgnoreCase(STR_DELETE)) {/*
                        Feature featureBean = (Feature) com.matrixone.apps.domain.DomainObject
                                .newInstance(context,
                                        ConfigurationConstants.TYPE_FEATURES,
                                        "Configuration");
                        StringTokenizer stTempDel = new StringTokenizer(
                                strEqpFeatureId, STR_Check_Token);
                        List al = new ArrayList();
                        while (stTempDel.hasMoreTokens()) {
                            al.add((String) stTempDel.nextToken());
                        }
                        String[] strObjectIdNew = new String[al.size()];
                        for (int i = 0; i < al.size(); i++) {
                            strObjectIdNew[i] = (String) al.get(i);
                        }

                        if(deleteCheck.equalsIgnoreCase(STR_LAST)){
                           featureBean.delete(context, strObjectIdNew);
                        }*/
        	}//End of : "This Block is executed if equipment feature is deleted."

        	//Here though Job gets finished but its relationship with product is removed by trigger

        } catch (Exception e) {
        	e.printStackTrace(System.out);
        	throw new FrameworkException(e);
        }
    }
    /**
     * Util method, check  get the Rel ID of the feature and check if the rel ID id connected with PRD
     * @param context
     * @param rightExpObject
     * @param slAllFeaturePhysicalIDs
     * @return
     * @throws Exception
     * @since R212
     */
    private boolean isFeatureConnectedToProduct(Context context,
			DomainObject rightExpObject, StringList slAllFeaturePhysicalIDs)
			throws Exception {
		StringList objSel = new StringList("to["
				+ ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES
				+ "].physicalid");
		objSel.add("to["
				+ ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES
				+ "].physicalid");
		DomainObject.MULTI_VALUE_LIST.add("to["
				+ ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES
				+ "].physicalid");
		DomainObject.MULTI_VALUE_LIST.add("to["
				+ ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES
				+ "].physicalid");

		Map mpRelIds = rightExpObject.getInfo(context, objSel);

		DomainObject.MULTI_VALUE_LIST.remove("to["
				+ ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES
				+ "].physicalid");
		DomainObject.MULTI_VALUE_LIST.remove("to["
				+ ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES
				+ "].physicalid");

		StringList allRelIDs = new StringList();
		for (int j = 0; j < mpRelIds.size(); j++) {
			StringList slLFRelIDs = (StringList) (mpRelIds.get("to["
					+ ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES
					+ "].physicalid"));
			allRelIDs.add(slLFRelIDs);
			StringList slCFRelIDs = (StringList) (mpRelIds
					.get("to["
							+ ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES
							+ "].physicalid"));
			allRelIDs.add(slCFRelIDs);
		}
		int lengthBefore = allRelIDs.size();
		allRelIDs.removeAll(slAllFeaturePhysicalIDs);
		int lengthAfter = allRelIDs.size();
		return (lengthBefore != lengthAfter);
	}
    /**
     * This method will be used input reportList and create XML, which will be set to ATTRIBUTE_EQUIPMENT_LIST_REPORT_XML
     * @param context
     * @param strFeatureId
     * @throws Exception
     * @since R212
     */
    private void equipmentListReportXMLUpdate(Context context, List reportList, String parentId) throws Exception
    {

    	String eqpXML = "";
    	String XML_ROOT_NAME = "EquipmentListReport";
    	String XML_EQUIPMENT_FEATURE = "EquipmentFeature";
    	String XML_ID = "id";
    	String XML_TYPE = "type";
    	String XML_NAME = "name";
    	String XML_REVISION = "revision";
    	String XML_PART = "Part";
    	String XML_LOGICAL_FEATURE_REL_ID = "logicalFeatureRelID";

    	try{
    		// Define the XML Elements
    		//ADD <EquipmentListReport> element to root
    		Element rootElement = new Element(XML_ROOT_NAME);
    		Document DPCDocument = new Document(rootElement);

    		for(int i = 0; i < reportList.size(); i++ )
    		{
    			List ftrList = (List)reportList.get(i);

    			Element equipmentElement = new Element(XML_EQUIPMENT_FEATURE);
    			Map tempMap = (Map)ftrList.get(0);
    			equipmentElement.setAttribute(XML_ID,(String)tempMap.get(ConfigurationConstants.SELECT_ID));
    			equipmentElement.setAttribute(XML_TYPE,(String)tempMap.get(ConfigurationConstants.SELECT_TYPE));
    			equipmentElement.setAttribute(XML_NAME,(String)tempMap.get(ConfigurationConstants.SELECT_NAME));
    			equipmentElement.setAttribute(XML_REVISION,(String)tempMap.get(ConfigurationConstants.SELECT_REVISION));
    			equipmentElement.setAttribute(XML_LOGICAL_FEATURE_REL_ID,(String)tempMap.get("to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+"].id"));

    			// Generate the GBOM element to be added to the root
    			for(int j = 1; j < ftrList.size(); j++ )
    			{
    				Element partElement = new Element(XML_PART);
    				String pId = (String)((Map)ftrList.get(j)).get(ConfigurationConstants.SELECT_ID);
    				String pType = (String)((Map)ftrList.get(j)).get(ConfigurationConstants.SELECT_TYPE);
    				String pName = (String)((Map)ftrList.get(j)).get(ConfigurationConstants.SELECT_NAME);
    				String pRevision = (String)((Map)ftrList.get(j)).get(ConfigurationConstants.SELECT_REVISION);
    				String strGBOMID = (String)((Map)ftrList.get(j)).get("GBOMRELID");
    				String iRuleID = (String)((Map)ftrList.get(j)).get("iRuleID");
    				String leftExpr = (String)((Map)ftrList.get(j)).get("LeftExpr");
    				String rightExp = (String)((Map)ftrList.get(j)).get("RightExp");

    				partElement.setAttribute(XML_ID,pId);
    				partElement.setAttribute(XML_TYPE,pType);
    				partElement.setAttribute(XML_NAME,pName);
    				partElement.setAttribute(XML_REVISION,pRevision);
    				partElement.setAttribute("GBOMRELID",strGBOMID);
    				partElement.setAttribute("iRuleID",iRuleID);
    				partElement.setAttribute("LeftExpr",leftExpr);
    				partElement.setAttribute("RightExp",rightExp);

    				// Add the GBOM element to the root
    				equipmentElement.addContent(partElement);
    			} // end of inner for loop
    			rootElement.addContent(equipmentElement);
    		}
    		int numberGBOMElementsUnderRoot = rootElement.getChildren(XML_EQUIPMENT_FEATURE).size();

    		if(numberGBOMElementsUnderRoot>0){
    			try {
    				XMLOutputter  outputter = MxXMLUtils.getOutputter(true);
    				eqpXML = outputter.outputString(DPCDocument);
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}else{
    			eqpXML = "";
    		}


    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	DomainObject objPCID = new DomainObject(parentId);

    	objPCID.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_EQUIPMENT_LIST_REPORT_XML,eqpXML);
    }


    /**
    *
    * This method will be called to get the part objectId's for context
    *
    * @param context
    *            the eMatrix <code>Context</code> object
    * @param arrFeatureIds
    *            the ArrayList <code>ArrayList</code> Logical object
    * @return Returns a <code>ArrayList</code> object. The return object is
    *         ArrayList, which will contain all the Part ObjectId for a given
    *         list of Features.
    * @throws Exception
    *             if the operation fails
    * @since R212
    */

   protected List getPartObjectDetails(Context context,
		   List arrFeatureIds) throws Exception {
	   List arrPartObjectIds = new ArrayList();
	   MapList relObjects = new MapList();
	   try {
		   for (int arrCount = 0; arrCount < arrFeatureIds.size(); arrCount++) {
			   String strFeatureObjectId = (String) arrFeatureIds
			   .get(arrCount);
			   LogicalFeature domFeatureObject = new LogicalFeature(
					   strFeatureObjectId);
			   StringList objectSelects = new StringList();
			   StringList relSelects = new StringList();
			   relSelects.add("tomid["
					   + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					   + "].from.attribute["
					   + ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
					   + "]");
			   relSelects.add("tomid["
					   + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					   + "].from.attribute["
					   + ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
					   + "]");
			   relSelects.add("tomid["
					   + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					   + "].from.id");
			   relSelects.addElement("attribute["
					   + ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
			   StringBuffer sbTypePattern=new StringBuffer();
			   sbTypePattern.append(ConfigurationConstants.TYPE_PART);
			   sbTypePattern.append(",");
			   sbTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
			   String typePattern=sbTypePattern.toString();

			   // getting the parts related to one feature
			   relObjects = domFeatureObject.getActiveGBOMStructure(context,
					   typePattern,
					   ConfigurationConstants.EMPTY_STRING, objectSelects,
					   relSelects, false, true, 1, 0,
					   ConfigurationConstants.EMPTY_STRING,
					   ConfigurationConstants.EMPTY_STRING, DomainObject.FILTER_ITEM,
					   ConfigurationConstants.EMPTY_STRING);
			   arrPartObjectIds.add(relObjects);
		   }
	   } catch (Exception e) {
		   throw e;
	   }

	   return arrPartObjectIds;
   }
   /**
    * Util method will return the physical ID from the input Maplist, will retrieve Physical IDs for Relationship and Objects in MapList.
    * @param mapListFeature
    * @return
    * @throws FrameworkException
    * @since R212
    * TODO-need to get separate selectable to relationship
    */
   private StringList getObjectRelPhyIdsFromMap(Context context,MapList mapListFeature) throws FrameworkException
   {
	   // StringList to return
	   StringList slTemp = new StringList();
	   if(mapListFeature!=null && !mapListFeature.isEmpty())
	   {
		   // Iterate through each map in MapList
		   for(int i=0;i<mapListFeature.size();i++)
		   {
			   Map tempMap = (Map)mapListFeature.get(i);
			   slTemp.add(tempMap.get("physicalid"));
			   //TODO - IMP-need to remove this-need to get separate selectable to relationship
			   MapList mlrelIDs=DomainRelationship.getInfo(context, new String[]{tempMap.get(DomainRelationship.SELECT_ID).toString()},new StringList("physicalid"));
			   if(mlrelIDs.size()>0){
				   slTemp.add(((Map)(mlrelIDs.get(0))).get("physicalid"));
			   }
		   }
	   }
	   // Return StringList
	   return slTemp;
   }

   /**
    * This method displays Cutom Action Icons column.
    *
    * @param  context the eMatrix Context object
    * @param  args holds the following input arguments: paramMap a HashMap
    *             requestMap a HashMap
    * @return Vector which holds following parameter html code to display icons
    * @throws Exception if the operation fails
    * @since  R212
    */
   public Vector getCustomActionIconsForDisplay(Context context, String args[])throws Exception {
	   ProductConfiguration pc =  new ProductConfiguration();
	   HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
	   MapList objectList = (MapList) parametersMap.get("objectList");
	   ArrayList parts = new ArrayList();
	   ArrayList Duplicateparts = new ArrayList();
	   ArrayList partFamily = new ArrayList();
	   StringBuffer stbNameRev = new StringBuffer();
	   Vector vNameRev = new Vector();
	   String strObjectId = "";
	   String strLanguage = context.getSession().getLanguage();
	   boolean displayGenerateIcon = false;
	   boolean isUsedInBOM = false;
        boolean isECInstalled = false;
        isECInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMEngineering", false, null, null);
        String i18Generate = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.Generate",strLanguage);
        String i18AddExisting = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.AddExisting",strLanguage);
        String i18CreateNew = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.CreateNew",strLanguage);
        String i18ReplabeByGenerate = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.ReplabeByGenerate",context.getSession().getLanguage());
        String i18ReplabeByAddExisting = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.ReplabeByAddExisting",context.getSession().getLanguage());
        String i18ReplabeByCreateNew = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.ToolTip.ReplabeByCreateNew",context.getSession().getLanguage());    
        
	   for (int i = 0; i < objectList.size(); i++)
	   {
		   boolean isQuantityInvalid = false;
		   stbNameRev.delete(0, stbNameRev.length());
		   Map objectMap = (Map) objectList.get(i);

                   String strLevel = (String)objectMap.get("level");
            
                   if("0".equals(strLevel)){
           	           vNameRev.add("");
           	           continue;
                   }
		   strObjectId = (String) objectMap.get(ConfigurationConstants.SELECT_ID);
		   String strObjectLevel = (String) objectMap.get("id[level]");
		   HashMap evaluatedParts = (HashMap) objectMap.get("evaluatedParts");
		   String parentId = (String) objectMap.get("parentOID");
		   ArrayList featureId = new ArrayList();
		   featureId.add(strObjectId);
		   // get list of parts for which part inclusion rule is evaluated to true
		   if(evaluatedParts!=null)
			   parts = (ArrayList) evaluatedParts.get("partIds");
		   if(evaluatedParts!=null)
			   Duplicateparts = (ArrayList) evaluatedParts.get("duplicatePartIds");
		   if(!(parts.size()>0))
		   {
			   if(evaluatedParts!=null)
				   parts = (ArrayList) evaluatedParts.get("partIds");
		   }
		   if(evaluatedParts!=null)
			   partFamily = (ArrayList) evaluatedParts.get("partFamilyIds");
		   // check whether quantity is invalid or not this will be used to
		   // display visual cue icon in case of create/replace/generate
		   
                  String strQuantity = (String) objectMap.get("attribute[" + ConfigurationConstants.ATTRIBUTE_QUANTITY + "]");

             if (strQuantity != null) {
                 double quantity = Double.parseDouble(strQuantity);
                 if (quantity < 0) {
                     isQuantityInvalid = true;
                 }
             }
		   if (partFamily.size() == 1)
		   {
			   displayGenerateIcon = true;
		   } else
		   {
			   displayGenerateIcon = false;
		   }

		   // IVU - BOM XML
		   // get the Parent Logical Feature for the  Duplicate
		   // Also get the attribute if the LF is leaf level
		   String strLFParentId = (String) objectMap.get("id[parent]");
		   String strLFisLeaf = (String) objectMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_LEAF_LEVEL);
 		   String strForcePartReuse  = (String)objectMap.get("attribute[" + ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE + "]");


		   String strResolvedPart = null;
		   String strGBOMRelID = null;

		   HashMap partsDetails = (HashMap) objectMap.get("PartDetails");
		   if(partsDetails!=null){
			   strResolvedPart =  partsDetails.get(ConfigurationConstants.SELECT_ID).toString();
			   strGBOMRelID = partsDetails.get("GBOMRelId").toString();
		   }


		   isUsedInBOM = pc.checkForUsedInBOM(context, strObjectId,strResolvedPart, strLFParentId,parentId);
		   //boolean isECInstalled = false;
		   //isECInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMEngineering", false, null, null);
		   // if inclusion rule for single part is evaluated true then display that part
		   if (evaluatedParts!=null && parts.size() == 0)
		   {
			   if (isECInstalled)
			   {
				   stbNameRev = stbNameRev
				   .append("<a><img src=\"../common/images/iconActionCreateNewPart.gif")
				   .append("\" border=\"0\"  align=\"middle\" ")
				   .append("TITLE=\"")
                   .append(i18CreateNew)
				   .append("\"")
				   .append(" onclick=\"javascript:showDialog('../configuration/PreviewBOMProcess.jsp?featureId="
						   + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
						   + "&amp;level="
						   + XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)
						   + "&amp;isQuantityInvalid="
						   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isQuantityInvalid))
						   + "&amp;displayGenerateIcon="
						   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(displayGenerateIcon))
						   + "&amp;LFParentID="
						   + XSSUtil.encodeForHTMLAttribute(context,strLFParentId)
						   + "&amp;isLFLeaf="
						   + XSSUtil.encodeForHTMLAttribute(context,strLFisLeaf)
						   + "&amp;ForcePartReuse="
						   + XSSUtil.encodeForHTMLAttribute(context,strForcePartReuse)
						   + "&amp;isUsedInBOM="
						   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isUsedInBOM))
						   + "&amp;DuplicateParts="
						   + Duplicateparts                                            
						   + "&amp;mode=createpart"
						   + "&amp;PartMode=custom"
						   + "&amp;duplicate=false"
						   + "&amp;parentId="
						   + XSSUtil.encodeForHTMLAttribute(context,parentId)
						   + "&amp;generate=false" + "');\"")
						   .append("/></a>");
			   }
			   stbNameRev = stbNameRev
			   .append("  <a><img src=\"../common/images/iconActionAddExistingPart.gif")
			   .append("\" border=\"0\"  align=\"middle\" ")
			   .append("TITLE=\"")
               .append(i18AddExisting)
			   .append("\"")
			   .append(" onclick=\"javascript:showModalDialog('../common/emxFullSearch.jsp?field=TYPES=type_Part:CURRENT!=policy_ECPart.state_Obsolete:POLICY!=policy_DevelopmentPart,policy_StandardPart,policy_ConfiguredPart"
			           + "&amp;table=FTRFeatureSearchResultsTable&amp;HelpMarker=emxhelpfullsearch&amp;showInitialResults=false&amp;hideHeader=true&amp;featureId="
					   + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
					   + "&amp;level="
					   + XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)
					   + "&amp;isQuantityInvalid="
					   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isQuantityInvalid))
					   + "&amp;displayGenerateIcon="
					   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(displayGenerateIcon))
					   + "&amp;LFParentID="
					   + XSSUtil.encodeForHTMLAttribute(context,strLFParentId)
					   + "&amp;isLFLeaf="
					   + XSSUtil.encodeForHTMLAttribute(context,strLFisLeaf)
						 + "&amp;ForcePartReuse="
						 + XSSUtil.encodeForHTMLAttribute(context,strForcePartReuse)					   
					   + "&amp;isUsedInBOM="
					   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isUsedInBOM))
					   + "&amp;DuplicateParts="
					   + Duplicateparts                                        
					   + "&amp;mode=addexisting"
					   + "&amp;PartMode=custom"
					   + "&amp;duplicate=false"
					   + "&amp;suiteKey=Configuration"
					   + "&amp;selection=single"
					   + "&amp;objectId="
					   + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
					   + "&amp;parentId="
					   + XSSUtil.encodeForHTMLAttribute(context,parentId)
					   + "&amp;generate=false"
					   + "&amp;submitURL=../configuration/PreviewBOMProcess.jsp?"
					   + "&amp;PartMode=custom"
			           + "&amp;displayGenerateIcon=true"
					   + "', '700', '600', 'true', 'Large');\"")
					   .append("/></a>");
			   if (partFamily.size() == 1)
			   {
				   String strPartFamily = (String) partFamily.get(0);
				   DomainObject objPartFamily = new DomainObject(strPartFamily);
				   String strPartFamilyState = objPartFamily.getInfo(context,ConfigurationConstants.SELECT_CURRENT);
				   if (!("obsolete").equalsIgnoreCase(strPartFamilyState))
				   {
					   stbNameRev = stbNameRev.append("  <a href=\"javaScript:emxTableColumnLinkClick('");
									stbNameRev.append("../configuration/PreviewBOMProcess.jsp?featureId=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strObjectId));
									stbNameRev.append("&amp;level=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strObjectLevel));
									stbNameRev.append("&amp;isQuantityInvalid=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(isQuantityInvalid)));
									stbNameRev.append("&amp;LFParentID=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strLFParentId));
									stbNameRev.append("&amp;isLFLeaf=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strLFisLeaf));
									stbNameRev.append("&amp;ForcePartReuse=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strForcePartReuse));
									stbNameRev.append("&amp;displayGenerateIcon=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(displayGenerateIcon)));
									stbNameRev.append("&amp;isUsedInBOM=" );
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(isUsedInBOM)));
									stbNameRev.append("&amp;DuplicateParts=");
									stbNameRev.append(Duplicateparts);
									stbNameRev.append("&amp;mode=generate");
									stbNameRev.append("&amp;duplicate=false");
									stbNameRev.append("&amp;PartMode=custom");
									stbNameRev.append("&amp;generate=true");
									stbNameRev.append("&amp;parentId=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, parentId));
									stbNameRev.append("&amp;partFamilyId=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamily));
									stbNameRev.append("', '700', '600', 'true', 'listHidden', '')\">");
									stbNameRev.append("<img border=\"0\" align=\"middle\" src=\"../common/images/iconActionGenerateFromPartFamily.gif\"  title=\""+i18Generate+"\"/></a>");
				   }
			   }
		   }
		   if (parts.size() > 0)
		   {
			   //IF ENGINEERING CENTRAL IS INSTALLED
			   if (isECInstalled)
			   {
				   stbNameRev = stbNameRev.append("<a><img src=\"../common/images/iconActionReplaceWithNewPart.gif")
				   .append("\" border=\"0\"  align=\"middle\" ")
				   .append("TITLE=\"")
				                            .append(i18ReplabeByCreateNew)
				   .append("\"")
				   .append(" onclick=\"javascript:showDialog('../configuration/PreviewBOMProcess.jsp?featureId="
						   + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
						   + "&amp;level="
						   + XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)
						   + "&amp;isQuantityInvalid="
						   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isQuantityInvalid))
						   + "&amp;displayGenerateIcon="
						   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(displayGenerateIcon))
						   + "&amp;LFParentID="
						   + XSSUtil.encodeForHTMLAttribute(context,strLFParentId)
						   + "&amp;isLFLeaf="
						   + XSSUtil.encodeForHTMLAttribute(context,strLFisLeaf)
							 + "&amp;ForcePartReuse="
							 + XSSUtil.encodeForHTMLAttribute(context,strForcePartReuse)						   
						   + "&amp;isUsedInBOM="
						   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isUsedInBOM))
						   + "&amp;mode=replacebycreatepart"
						   + "&amp;PartMode=custom"
						   + "&amp;duplicate=false"
						   + "&amp;parentId="
						   + XSSUtil.encodeForHTMLAttribute(context,parentId)
						   + "&amp;DuplicateParts="
						   + Duplicateparts	
						   + "&amp;ResolvedPart="
						   + XSSUtil.encodeForHTMLAttribute(context,strResolvedPart)
                           + "&amp;ResolvedPartRelID="
                           + XSSUtil.encodeForHTMLAttribute(context,strGBOMRelID)						   
						   + "&amp;parentId="
						   + XSSUtil.encodeForHTMLAttribute(context,parentId)
						   + "&amp;generate=false" + "');\"")
						   .append("/></a>");
			   }
			   stbNameRev = stbNameRev.append("  <a><img src=\"../common/images/iconActionReplaceWithExistingPart.gif")
			   .append("\" border=\"0\"  align=\"middle\" ")
			   .append("TITLE=\"")
			   .append(i18ReplabeByAddExisting)
			   .append("\"")
			   .append(" onclick=\"javascript:showModalDialog('../common/emxFullSearch.jsp?field=TYPES=type_Part:CURRENT!=policy_ECPart.state_Obsolete:POLICY!=policy_DevelopmentPart,policy_StandardPart,policy_ConfiguredPart"
			           + "&amp;table=FTRFeatureSearchResultsTable&amp;HelpMarker=emxhelpfullsearch&amp;showInitialResults=false&amp;hideHeader=true&amp;featureId="
					   + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
					   + "&amp;level="
					   + XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)
					   + "&amp;isQuantityInvalid="
					   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isQuantityInvalid))
					   + "&amp;displayGenerateIcon="
					   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(displayGenerateIcon))
					   + "&amp;LFParentID="
					   + XSSUtil.encodeForHTMLAttribute(context,strLFParentId)
					   + "&amp;isLFLeaf="
					   + XSSUtil.encodeForHTMLAttribute(context,strLFisLeaf)
						 + "&amp;ForcePartReuse="
						 + XSSUtil.encodeForHTMLAttribute(context,strForcePartReuse)					   
					   + "&amp;isUsedInBOM="
					   + XSSUtil.encodeForHTMLAttribute(context,String.valueOf(isUsedInBOM))
					   + "&amp;DuplicateParts="
					   + Duplicateparts   
					   + "&amp;ResolvedPart="
					   + XSSUtil.encodeForHTMLAttribute(context,strResolvedPart)
                       + "&amp;ResolvedPartRelID="
                       + XSSUtil.encodeForHTMLAttribute(context,strGBOMRelID)	
					   + "&amp;mode=replacebyaddexisting"
					   + "&amp;PartMode=custom"
					   + "&amp;duplicate=false"
					   + "&amp;suiteKey=Configuration"
					   + "&amp;selection=single"
					   + "&amp;objectId="
					   + XSSUtil.encodeForHTMLAttribute(context,strObjectId)
					   + "&amp;parentId="
					   + XSSUtil.encodeForHTMLAttribute(context,parentId)
					   + "&amp;generate=false"
					   + "&amp;submitURL=../configuration/PreviewBOMProcess.jsp?"
					   + "&amp;PartMode=custom"
			           + "&amp;displayGenerateIcon=true"
					   + "', '700', '600', 'true', 'Large');\"")
					   .append("/></a>");
			   //IF PART FAMILY SIZE ==
			   if (partFamily.size() == 1)
			   {
				   String strPartFamily = (String) partFamily.get(0);
				   DomainObject objPartFamily = new DomainObject(strPartFamily);
				   String strPartFamilyState = objPartFamily.getInfo(context,ConfigurationConstants.SELECT_CURRENT);
				   if (!("obsolete").equalsIgnoreCase(strPartFamilyState))
				   {
					   stbNameRev = stbNameRev.append("  <a href=\"javaScript:emxTableColumnLinkClick('");

									stbNameRev.append("../configuration/PreviewBOMProcess.jsp?featureId=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strObjectId));
									stbNameRev.append("&amp;level=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strObjectLevel));
									stbNameRev.append("&amp;isQuantityInvalid=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(isQuantityInvalid)));
									stbNameRev.append("&amp;LFParentID=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strLFParentId));
									stbNameRev.append("&amp;isLFLeaf=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strLFisLeaf));
									stbNameRev.append("&amp;ForcePartReuse=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strForcePartReuse));
									stbNameRev.append("&amp;displayGenerateIcon=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(displayGenerateIcon)));
									stbNameRev.append("&amp;isUsedInBOM=" );
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, String.valueOf(isUsedInBOM)));
									stbNameRev.append("&amp;DuplicateParts=");
									stbNameRev.append(Duplicateparts);
									stbNameRev.append("&amp;mode=generatebyreplace");
									stbNameRev.append("&amp;duplicate=false");
									stbNameRev.append("&amp;generate=true");
									stbNameRev.append("&amp;PartMode=custom");
									stbNameRev.append("&amp;parentId=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, parentId));
									stbNameRev.append("&amp;ResolvedPart=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strResolvedPart));
									stbNameRev.append("&amp;ResolvedPartRelID=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strGBOMRelID));
									stbNameRev.append("&amp;partFamilyId=");
									stbNameRev.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamily));
									stbNameRev.append("', '700', '600', 'true', 'listHidden', '')\">");
									stbNameRev.append("<img border=\"0\" align=\"middle\" src=\"../common/images/iconActionReplaceWithGeneratedpart.gif\"   title=\""+i18ReplabeByGenerate+"\"/></a>");  
				   }
			   }
		   }
		   vNameRev.add(stbNameRev.toString());
	   }
	   return vNameRev;
   }

    /**
     * This method displays Cutom Action Icons column.
     *
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *             requestMap a HashMap
     * @return Vector which holds following parameter html code to display icons
     * @throws Exception if the operation fails
     * @since  R212
     */
     public Vector getPartUsageForDisplay (Context context, String args[])throws Exception {
         HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
         MapList objectList = (MapList) parametersMap.get("objectList");
         Vector vectPartUsage = new Vector();
         String strPartUsage = "";
         String strLanguage = context.getSession().getLanguage();
         String strStandard = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                 "emxConfiguration.Range.Part_Usage.Standard",strLanguage);
         String strCustom = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                 "emxConfiguration.Range.Part_Usage.Custom",strLanguage);

         for (int i = 0; i < objectList.size(); i++)
         {
        	 Map objectMap = (Map) objectList.get(i);
             strPartUsage = (String) objectMap.get(ConfigurationConstants.PARTUSAGE);
            
             if(ProductLineCommon.isNotNull(strPartUsage)){
            	 if(strPartUsage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_CUSTOM)){
                	 vectPartUsage.add(strCustom);
                 }else{
                	 vectPartUsage.add(strStandard);
                 }
             }else {
    			 String strRelationship = (String) objectMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);
    			 if( ProductLineCommon.isNotNull(strRelationship)
    					 && strRelationship.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_GBOM))
    			 {
    				 vectPartUsage.add(strStandard);
    			 }
    			 else if(ProductLineCommon.isNotNull(strRelationship)
    					 && strRelationship.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM))
    			 {
    				 vectPartUsage.add(strCustom);
    			 }else{
                	 vectPartUsage.add("");
    			 }
             }
         }
         return vectPartUsage;
     }

     /**
		 * This is a Access Function to Standard/Custom actions column in Preview BOM page.
		 *
		 * @param context - the eMatrix <code>Context</code> object
		 * @return bResult - true  : if emxConfiguration.PreviewBOM.EnableCustomPartMode =true
		 * 					 false
		 * @throws FrameworkException  - throws Exception if any operation fails.
		 * @since R212
		 */
	public Object checkCustomPartMode(Context context, String[] args)
			throws FrameworkException {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap settingMap = (HashMap) programMap.get("SETTINGS");
			String strColumnName = (String) settingMap.get("ColumnName"); //setting[ColumnName]=DefaultActions}
			String strCustomPartMode = EnoviaResourceBundle.getProperty(context,
					"emxConfiguration.PreviewBOM.EnableCustomPartMode");
			
			if(ProductLineCommon.isNotNull(strColumnName) && (strColumnName.equalsIgnoreCase("StandardActions")||strColumnName.equalsIgnoreCase("CustomActions"))){
			    boolean isFTRUser = ConfigurationUtil.isFTRUser(context);
				boolean isCMMUser = ConfigurationUtil.isCMMUser(context);
				if(isFTRUser||isCMMUser){
					return Boolean.valueOf(true);
				}else{
					return Boolean.valueOf(false);
				}
				}
			if(ProductLineCommon.isNotNull(strColumnName) && strColumnName.equalsIgnoreCase("DefaultActions")){
				if (ProductLineCommon.isNotNull(strCustomPartMode)
						&& strCustomPartMode.equalsIgnoreCase("true")) {
					return Boolean.valueOf(false);
				} else {
					return Boolean.valueOf(true);
				}
			}else{
				if (ProductLineCommon.isNotNull(strCustomPartMode)
						&& strCustomPartMode.equalsIgnoreCase("true")) {
					return Boolean.valueOf(true);
				} else {
					return Boolean.valueOf(false);
				}
			}
		} catch (Exception e) {
			return Boolean.valueOf(false);
		}
	}
      /**
    *
    * @param context
    * @param args
    * @throws Exception
    */
	public void deltaUpdateBOMXMLForProductConfiguration(Context context,
			String[] args) throws Exception {
		try {/*
			DomainObject domProductConfigurationObject = new DomainObject();
			String strObjPcId = args[0];
			String strFeatureId = args[1];
			String deltaContext = args[2];
			StringTokenizer strTknFeatureId = new StringTokenizer(strFeatureId, "][,");
			StringList slFeatures = new StringList();
			while (strTknFeatureId.hasMoreTokens()) {
		        String strFeaId = strTknFeatureId.nextToken().trim();
		        slFeatures.addElement(strFeaId);
			}
			ProductConfiguration pc = new ProductConfiguration();
			pc.setId(strObjPcId);
			if (deltaContext.equalsIgnoreCase("AddFeatureUpdate")) {
				pc.updateBOMXMLForAddFeature(context, strObjPcId,slFeatures);
			}else if (deltaContext.equalsIgnoreCase("RemoveFeatureUpdate")) {
				pc.updateBOMXMLForRemoveFeature(context, strObjPcId,slFeatures);
			} else if (deltaContext.equalsIgnoreCase("GBOMUpdate")) {
				pc.updateBOMXMLForGBOM(context, strObjPcId, slFeatures);
			} else if (deltaContext.equalsIgnoreCase("QuantityUpdate")|| deltaContext.equalsIgnoreCase("UsageUpdate")) {
				pc.updateBOMXMLForUsageAndQuantity(context, strObjPcId,	slFeatures);
			}
		*/} catch (Exception e) {
			throw new FrameworkException(e.getLocalizedMessage());
		}
	}

public Vector displayQuantity(Context context, String args[])
            throws Exception {
        Vector vQuantity = new Vector();
        try {
            HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) parametersMap.get("objectList");
            StringBuffer stbQuantity = new StringBuffer();
            for (int i = 0; i < objectList.size(); i++) {
                stbQuantity.delete(0, stbQuantity.length());
                Map objectMap = (Map) objectList.get(i);
                String strQuantity = (String) objectMap.get("attribute[" + ConfigurationConstants.ATTRIBUTE_QUANTITY + "]");

                if(strQuantity != null){
                	stbQuantity.append(strQuantity);
                	vQuantity.add(stbQuantity.toString());
                	
                }else{
                	vQuantity.add("");
                }
            }
        } catch (Exception e) {
            throw new FrameworkException(e.getMessage());
        }
        return vQuantity;
    }


 @com.matrixone.apps.framework.ui.ProgramCallable
 public Object getDuplicateParts(Context context, String[] args)throws Exception {
	String featureId = "";
	String parentId = "";

	MapList vDuplicateParts = new MapList();
	
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);

	featureId = (String) paramMap.get("featureId");
	parentId = (String) paramMap.get("parentId");
	String strResolvedPart = (String) paramMap.get("ResolvedPart");
	String strResolvedPartRelId = (String) paramMap.get("ResolvedPartRelID");

	vDuplicateParts = getDuplicatePartsFromXML( context, parentId,featureId,strResolvedPart,strResolvedPartRelId);

	return vDuplicateParts;
}

 /**
  * This method is used to get list of duplicateparts assocaited with any feature from BOMXML
  * @param context
  * @param args - parentId is the object id of Product Configuration,
  *             - strFeatureId is the object id of feature whose duplicate parts are to be retrived.
  * @throws Exception
  * @author
  */
public MapList getDuplicatePartsFromXML(Context context, String parentId,String strFeatureId, String strResolvedPart, String strResolvedPartRelId)
throws FrameworkException {

	MapList evaluatedParts = new MapList();
	try {
		DomainObject objPCID = new DomainObject(parentId);
		// IVU - BOM XML
		// Retrieve the GBOM of the Logical Feature.
		StringList selList = new StringList();
		selList.add(ConfigurationConstants.SELECT_ATTRIBUTE_BOMXML);
		selList.add("attribute["+PropertyUtil.getSchemaProperty(context, "attribute_FilterCompiledForm")+"]");


		Map dataMap = objPCID.getInfo(context,selList);
		String strBOMXML = (String)dataMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_BOMXML);
		String strFilterCompiledForm =(String)dataMap.get("attribute["+PropertyUtil.getSchemaProperty(context, "attribute_FilterCompiledForm")+"]");

			String strCustomPartMode = EnoviaResourceBundle.getProperty(context,
 			"emxConfiguration.PreviewBOM.EnableCustomPartMode");

		StringBuffer strRelSel = new StringBuffer(ConfigurationConstants.RELATIONSHIP_GBOM);
		if (ProductLineCommon.isNotNull(strCustomPartMode)
				&& strCustomPartMode.equalsIgnoreCase("true")) {
			strRelSel.append(",");
			strRelSel.append(ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM);
		}
		StringList objSelects = new StringList();
		objSelects.addElement(ConfigurationConstants.SELECT_PHYSICAL_ID);
		objSelects.addElement(DomainObject.SELECT_NAME);
		objSelects.addElement(DomainObject.SELECT_ID);
		objSelects.addElement(DomainObject.SELECT_REVISION);

		LogicalFeature lfbean = new LogicalFeature(strFeatureId);
		// call the API to retrieve the evaluated Parts of the Logical Feature.
		// get the expression
		evaluatedParts = lfbean.getActiveGBOMStructure(context, ConfigurationConstants.TYPE_PART, strRelSel.toString(),objSelects,
				ConfigurationUtil.getBasicRelSelects(context), false, true, 1, 0,
				null, null, DomainObject.FILTER_STR_AND_ITEM, strFilterCompiledForm);

		// get the parts from the BOM XML
		Element root = null;
		Document document = null;
		List listFeature = new MapList();
		if(ProductLineCommon.isNotNull(strBOMXML)){
			SAXBuilder saxb = new SAXBuilder();
			saxb.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			saxb.setFeature("http://xml.org/sax/features/external-general-entities", false);
			saxb.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			document = saxb.build(new StringReader(strBOMXML));
			root = document.getRootElement();
			listFeature = (List) root.getChildren("Feature");
		}
		StringList strResolvedParts = new StringList();
		StringList strResolvedPartsRelIds = new StringList();

		for(int k=0;k<listFeature.size();k++){
			Element featureElement = (Element)listFeature.get(k);
			String strFeatureObjID = featureElement.getAttributeValue("id").toString();
			if(strFeatureId.equals(strFeatureObjID)){
				List listResolved = (List) featureElement.getChildren("Resolved");
				List listPending = (List) featureElement.getChildren("Pending");
				List lstparts = null;
				if(listResolved!=null && listResolved.size() > 0){
					lstparts = listResolved;
				}else if(listPending!=null && listPending.size() > 0){
					lstparts = listPending;
				}
				for(int x=0;x<lstparts.size();x++){
					Element elementPending = (Element) lstparts.get(x);
					strResolvedParts.add(elementPending.getAttributeValue("id"));
					strResolvedPartsRelIds.add(elementPending.getAttributeValue("GBOMRelId"));
				}
			}
		}
		// iterate the evaulated parts and remove the Parts which are already resolved.
		for(int j=0;j<evaluatedParts.size();j++){
			Map gbom = (Map)evaluatedParts.get(j);
			String partrelId = (String) gbom.get(DomainRelationship.SELECT_ID);
			if(strResolvedPartsRelIds.contains(partrelId)){
				evaluatedParts.remove(j);
				j--;
			}else if(strResolvedPartsRelIds.isEmpty()
					&& (strResolvedPartRelId!=null && strResolvedPartRelId.equals(partrelId))){
				evaluatedParts.remove(j);
				j--;
			}
		}
	} catch (Exception e) {
		throw new FrameworkException(e.getMessage());
	}
	return evaluatedParts;
}







public boolean callIsEBOMDifferent(Context context, String args[])
        throws Exception {
    HashMap parametersMap = (HashMap) JPO.unpackArgs(args);

    MapList objectList = (MapList) parametersMap.get("objectList");
    String strObjectId = "";
    String featureId = "";
    String productConfigurationId = "";

    Map objectMap = (Map) objectList.get(0);
    strObjectId = (String) objectMap.get(ConfigurationConstants.SELECT_ID);
    featureId = (String) objectMap.get("featureId");
    productConfigurationId = (String) objectMap.get("parentId");

    return isEBOMDifferent(context, strObjectId, featureId,
            productConfigurationId);

}

/**
 *
 * @param context
 * @param PartID
 * @param FeatureID
 * @param parentID
 * @return
 * @throws Exception
 * this could not be deprecated as referenced from migration JPO
 */
private boolean isEBOMDifferent(Context context, String PartID,
        String FeatureID, String parentID) throws Exception {
    boolean isEBOMDifferent;
    ArrayList alSubFeatures = new ArrayList();
    ArrayList GBOMParts = new ArrayList();

        DomainObject objPCID = new DomainObject(parentID);
        String strBOMXML = objPCID.getInfo(context,
                ConfigurationConstants.SELECT_ATTRIBUTE_BOMXML);
        SAXBuilder saxb = new SAXBuilder();
        saxb.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		saxb.setFeature("http://xml.org/sax/features/external-general-entities", false);
		saxb.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = saxb.build(new StringReader(strBOMXML));
        Element root = document.getRootElement();
        List listFeature = (List) root.getChildren("Feature");
        inner: for (int ix = 0; ix < listFeature.size(); ix++) {
            Element elementFeature = (Element) listFeature.get(ix);
            String featureId = elementFeature.getAttributeValue("id");
            if (FeatureID.equalsIgnoreCase(featureId)) {

                List listDuplicate = (List) elementFeature
                        .getChildren("SubFeatures");

                if (listDuplicate.size() > 0) {
                    Element elementDuplicate = (Element) listDuplicate
                            .get(0);
                    List listSubFeature = elementDuplicate
                            .getChildren("Feature");
                    for (int p = 0; p < listSubFeature.size(); p++) {
                        Element elementPart = (Element) listSubFeature
                                .get(p);
                        String subFeatureId = elementPart
                                .getAttributeValue("id");
                        alSubFeatures.add(subFeatureId);
                    }

                }

                break inner;
            }
        }
        for (int q = 0; q < alSubFeatures.size(); q++) {
            String sfId = (String) alSubFeatures.get(q);
            inner1: for (int ix = 0; ix < listFeature.size(); ix++) {
                // Fetching the attributes from the GBOM Element under root
                Element elementFeature = (Element) listFeature.get(ix);
                String featureId = elementFeature.getAttributeValue("id");
                String partId = "";
                if (sfId.equalsIgnoreCase(featureId)) {

                    List listPending = (List) elementFeature
                            .getChildren("Resolved");
                    if (listPending.size() > 0) {
                        Element elementPending = (Element) listPending
                                .get(0);
                        partId = elementPending.getAttributeValue("id");
                        GBOMParts.add(partId);

                    } else {
                        List listResolved = (List) elementFeature
                                .getChildren("Pending");
                        List listDuplicate = (List) elementFeature
                                .getChildren("Duplicate");
                        if (listResolved.size() > 0) {
                            Element elementResolved = (Element) listResolved
                                    .get(0);
                            partId = elementResolved
                                    .getAttributeValue("id");
                            GBOMParts.add(partId);
                        } else {
                            if (listDuplicate.size() > 0) {
                                Element elementDuplicate = (Element) listDuplicate
                                        .get(0);
                                List listPart = elementDuplicate
                                        .getChildren("Part");
                                for (int p = 0; p < listPart.size(); p++) {
                                    Element elementPart = (Element) listPart
                                            .get(p);
                                    partId = elementPart
                                            .getAttributeValue("id");
                                    GBOMParts.add(partId);
                                }

                            }

                        }
                    }
                    break inner1;
                }
            }
        }


    // get part associated wiht feature

    DomainObject objPartOfFeature = new DomainObject(PartID);
    StringList objSelects = new StringList();
    objSelects.add(ConfigurationConstants.SELECT_ID);
    MapList mapListEBOMRelatedObject = objPartOfFeature.getRelatedObjects(
            context, ConfigurationConstants.RELATIONSHIP_EBOM,
            ConfigurationConstants.TYPE_PART, objSelects, new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID), false, true,
            (short) 1, null, null, 0);
    Iterator itrEBOM = mapListEBOMRelatedObject.iterator();
    Hashtable hsEBOM = new Hashtable();
    ArrayList EBOMPartID = new ArrayList();
    while (itrEBOM.hasNext()) {
        hsEBOM = (Hashtable) itrEBOM.next();
        String objFeatureId = (String) hsEBOM
                .get(ConfigurationConstants.SELECT_ID);
        EBOMPartID.add(objFeatureId);
    }

    if (EBOMPartID.equals(GBOMParts)) {
        isEBOMDifferent = false;
    } else {
        isEBOMDifferent = true;
    }

    return isEBOMDifferent;
}



/**
 * It is a programHTML method, used to display Duplicate and Remove Action Icons in Preview BOM
 * @param context
 *            The ematrix context object.
 * @param String[]
 *            The args .
 * @return Duplicate,and Remove icons will return
 * @since R215
 */
public Vector renderActionIconsOnPreviewBOMForLeafLF(Context context, String[] args) throws FrameworkException {
	try{
		HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) parametersMap.get("objectList");
		StringBuffer stbNameRev = new StringBuffer();
		Vector vNameRev = new Vector();
		String strObjectId = "";
		for (int i = 0; i < objectList.size(); i++)
		{
			stbNameRev.delete(0, stbNameRev.length());
			Map objectMap = (Map) objectList.get(i);
			strObjectId = (String) objectMap.get(ConfigurationConstants.SELECT_ID);
			String strObjectLevel = (String) objectMap.get("id[level]");

			String isLeafLF = (String) objectMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_LEAF_LEVEL);
			if(objectList.size()==1 && isLeafLF==null && strObjectId!=null){
				isLeafLF = new DomainObject(strObjectId).getInfo(context,ConfigurationConstants.SELECT_ATTRIBUTE_LEAF_LEVEL);
			}

			if(ProductLineCommon.isNotNull(isLeafLF)&& isLeafLF.equalsIgnoreCase("Yes")){
				stbNameRev = stbNameRev.append("<a><img src=\"../common/images/iconActionDuplicateRow.gif")
				.append("\" border=\"0\"  align=\"middle\" ")
				.append("TITLE=\"")
				.append(XSSUtil.encodeForHTMLAttribute(context,EnoviaResourceBundle.getProperty(context,"Configuration","emxConfiguration.ToolTip.Duplicate",context.getSession().getLanguage())))
				.append("\"")
				.append(" onclick=\"javascript:addDuplicateRowForLF('"+XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)+"');\"")
				.append("/></a>&#160;&#160;");

				stbNameRev = stbNameRev.append("<a><img src=\"../common/images/iconActionRemoveRow.gif")
				.append("\" border=\"0\"  align=\"middle\" ")
				.append("TITLE=\"")
				.append(XSSUtil.encodeForHTMLAttribute(context,EnoviaResourceBundle.getProperty(context,"Configuration","emxConfiguration.ToolTip.Remove",context.getSession().getLanguage())))
				.append("\"")
				.append(" onclick=\"javascript:removeLeafLevelLF('"+XSSUtil.encodeForHTMLAttribute(context,strObjectLevel)+"');\"")
				.append("/></a>");
				vNameRev.add(stbNameRev.toString());
			}else{
				vNameRev.add("");
			}
		}
		return vNameRev;
	}
	catch (Exception e) {
		throw new FrameworkException(e.toString());
	}
}

	/**
	 * Column Function to show the Back Annotate Value.
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 */
	public static StringList getBackAnnotateForPartOnPreviewBOM(Context context, String[] args)
	throws FrameworkException{
		try{
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			HashMap paramList = (HashMap)inputMap.get("paramList");
			String strPCID = (String) paramList.get("parentOID");
    		Document document =  null;
    		Element root = null;
			String strBOMXML = null;
			if(strPCID!=null){
				strBOMXML = new DomainObject(strPCID).getInfo(context,ConfigurationConstants.SELECT_ATTRIBUTE_BOMXML);
				if(strBOMXML!=null && !strBOMXML.equals("")){
	    			SAXBuilder saxb = new SAXBuilder();
	    			saxb.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					saxb.setFeature("http://xml.org/sax/features/external-general-entities", false);
					saxb.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	    			document = saxb.build(new StringReader(strBOMXML));
	    			root = document.getRootElement();
	    		}
			}
			StringList returnStringList = new StringList (objectMap.size());
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map)objectMap.get(i);
				String strLevel = (String)outerMap.get(DomainObject.SELECT_LEVEL);
				String strFeatureId = (String)outerMap.get(DomainObject.SELECT_ID);
	    		String strGBOMRelID = "";
	    		HashMap partsDetails = (HashMap) outerMap.get("PartDetails");
	    		if(partsDetails!=null){
	    			strGBOMRelID =  partsDetails.get("PartRelId").toString();
	    		}
				if(strLevel!=null && strLevel.equals("0")){
					returnStringList.addElement("");

				}else if(strBOMXML==null || strBOMXML.equals("")){
					String strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxConfiguration.PreviewBOM.UpdateBOMDetails.Range.Global",context.getSession().getLanguage()));
					returnStringList.addElement(strI18Value);
				}else{
	    			 XPath xPath = XPath.newInstance("//Feature[@id='"+strFeatureId+"']");
	    			 Element elementFeature = (Element)xPath.selectSingleNode(root.getDocument());
	    			 if(elementFeature!=null){
	    				 List listResolved = (List) elementFeature.getChildren("Resolved");
	    				 List listPending = (List) elementFeature.getChildren("Pending");
	    				 List listParts = new MapList();
	    				 if (listResolved!=null && listResolved.size() > 0)
	    				 {
	    					 listParts.addAll(listResolved);
	    				 }else if(listPending!=null && listPending.size() > 0){
	    					 listParts.addAll(listPending);
	    				 }
	    				 for(int k=0;k<listParts.size();k++){
	    					 Element elementPart = (Element) listParts.get(k);
	    					 String strPartRelId  = elementPart.getAttributeValue("GBOMRelId");
	    					 if(strGBOMRelID.equals(strPartRelId)){
	    						 Element bomAttributes = elementPart.getChild("BOMAttributes");
	    						 if(bomAttributes!=null){
		    						 String strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxConfiguration.PreviewBOM.UpdateBOMDetails.Range.Local",context.getSession().getLanguage()));
		    						 returnStringList.addElement(strI18Value);
	    						 }else{
		    						 String strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxConfiguration.PreviewBOM.UpdateBOMDetails.Range.Global",context.getSession().getLanguage()));
		    						 returnStringList.addElement(strI18Value);
	    						 }
	    					 }
	    				 }
	    			 }else{
						 String strI18Value = (EnoviaResourceBundle.getProperty(context,"Configuration", "emxConfiguration.PreviewBOM.UpdateBOMDetails.Range.Global",context.getSession().getLanguage()));
						 returnStringList.addElement(strI18Value);

	    			 }
				}
			}
			return returnStringList;
		}catch(Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}


	  /**
     * Returns the Range Values for "Back Annotate" attribute.
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
    public Map getRangeValuesForBackAnnotate(Context context,String[] args) throws Exception
    {
        HashMap rangeMap = new HashMap();
        StringList sLst = new StringList();
        sLst.addElement("Global");
        sLst.addElement("Local");

        StringList sLstRangeDisplay = new StringList();
        sLstRangeDisplay.addElement(EnoviaResourceBundle.getProperty(context,"Configuration", "emxConfiguration.PreviewBOM.UpdateBOMDetails.Range.Global",context.getSession().getLanguage()));
        sLstRangeDisplay.addElement(EnoviaResourceBundle.getProperty(context,"Configuration", "emxConfiguration.PreviewBOM.UpdateBOMDetails.Range.Local",context.getSession().getLanguage()));

        rangeMap.put("field_choices", sLst);
        rangeMap.put("field_display_choices", sLstRangeDisplay);
        return rangeMap;
    }


	  /**
     * Returns the Range Values for "BOM" Usage attribute.
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
    public Map getRangeValuesForBOMAttributeUsage(Context context,String[] args) throws Exception
    {
        String strAttributeName = ConfigurationConstants.ATTRIBUTE_USAGE;
        HashMap rangeMap = new HashMap();
        matrix.db.AttributeType attribName = new matrix.db.AttributeType(
                strAttributeName);
        attribName.open(context);

        List attributeRange = attribName.getChoices();
        List attributeDisplayRange = i18nNow
                .getAttrRangeI18NStringList(
                        ConfigurationConstants.ATTRIBUTE_USAGE,
                        (StringList) attributeRange, context.getSession()
                                .getLanguage());
        rangeMap.put("field_choices", attributeRange);
        rangeMap.put("field_display_choices", attributeDisplayRange);
        return rangeMap;
    }


    /**
     * This method is used to give access at column level for BOM Attributes on GBOM Part table.
     * @param context
     *            The ematrix context object.
     * @param String[]
     *            The args .
     * @return StringList.
     * @throws Exception
     */
    public static StringList isBOMColumnEditable(Context context, String[] args) throws FrameworkException{
        try{
   		 	HashMap programMap = (HashMap) JPO.unpackArgs(args);
   		 	MapList objectMap = (MapList) programMap.get("objectList");
   		 	Map requestMap = (HashMap) programMap.get("requestMap");
   		 	String contextId = (String) requestMap.get("prodId");

            StringList returnStringList = new StringList (objectMap.size());
   		 	if(contextId!=null){
   	            for (int i = 0; i < objectMap.size(); i++) {
                    returnStringList.add(new Boolean(true));
   	            }
   		 	}else{
   	            for (int i = 0; i < objectMap.size(); i++) {
                    returnStringList.add(new Boolean(false));
   	            }
   		 	}
            return returnStringList;
        }catch(Exception e) {
            e.printStackTrace();
            throw new FrameworkException(e.getMessage());
        }
    }


    
} // -------------CODE FOR PreviewBOM END

