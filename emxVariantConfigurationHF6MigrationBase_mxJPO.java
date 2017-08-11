/*
 * emxVariantConfigurationHF6MigrationBase.java
 * program migrates the data into Common Document model i.e. to 10.5 schema.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = "$Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/emxVariantConfigurationHF6MigrationBase.java 1.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$"
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.SelectConstants;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.productline.ProductLineConstants;

/**
 * @version AEF 10.5 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxVariantConfigurationHF6MigrationBase_mxJPO extends
        emxCommonMigration_mxJPO {
    protected static final String selectFeatureListToName = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO + "].to.name";

    protected static final String selectFeatureListToMrktName = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO
            + "].to.attribute[" + ProductLineConstants.ATTRIBUTE_MARKETING_NAME
            + "]";

    protected static final String selectFeatureListFromName = "to["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM
            + "].from.name";

    protected static final String selectFeatureListFromMrktName = "to["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM
            + "].from.attribute["
            + ProductLineConstants.ATTRIBUTE_MARKETING_NAME + "]";

    // from[Left Expression].to[GBOM].to[GBOM From].from.from[Varies by].to.id
    protected static final String selectVariesByGBOM = "from["
            + ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "].to["
            + ConfigurationConstants.TYPE_GBOM + "].to["
            + ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].from.from["
            + ConfigurationConstants.RELATIONSHIP_VARIES_BY + "].to.id";

    // from[Right Expression].to[Feature List].to.from.id
    protected static final String selectVariesByFeatureList = "from["
            + ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "].to["
            + ConfigurationConstants.TYPE_FEATURE_LIST + "].to["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM
            + "].from.id";

    protected static final String selectInactiveVariesBy = "tomid["
            + ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY
            + "].fromrel["
            + ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST
            + "].from.id";

    static {
        DomainObject.MULTI_VALUE_LIST.add(selectVariesByGBOM);
        DomainObject.MULTI_VALUE_LIST.add(selectVariesByFeatureList);
    }

    static protected String newline = System.getProperty("line.separator");

    /**
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxVariantConfigurationHF6MigrationBase_mxJPO(Context context,
            String[] args) throws Exception {
        super(context, args);
    }

    private String isrelId = "";

    public void migrateObjects(Context context, StringList objectList)
            throws Exception {
        // "Feature List,Rule,Boolean Compatibility Rule,BOM Rules,Inclusion
        // Rule,"
        // "Product Compatibility Rule,Marketing Preference,Rule
        // Extension,Quantity Rule,Fixed Resource";
        // VariesBy

        if (isrelId.equals("")) {
            /*String cmd = "print bus " + (String) objectList.get(0)
                    + " select exists dump";
            isrelId = MqlUtil.mqlCommand(context, cmd);*/
        	
        	String strExists = "exists";
			String cmd = "print bus $1 select $2 dump";
			isrelId = MqlUtil.mqlCommand(context, cmd, (String) objectList.get(0) , strExists);
        }

        StringList selectStmts = new StringList(DomainConstants.SELECT_ID);
        selectStmts.add(DomainConstants.SELECT_TYPE);
        selectStmts.add(selectFeatureListToName);
        selectStmts.add(selectFeatureListToMrktName);
        selectStmts.add(selectFeatureListFromName);
        selectStmts.add(selectFeatureListFromMrktName);
        selectStmts.add(selectVariesByGBOM);
        selectStmts.add(selectVariesByFeatureList);

        StringList relSelectStmts = new StringList(DomainConstants.SELECT_ID);
        relSelectStmts.add(DomainConstants.SELECT_TYPE);
        relSelectStmts.add(selectInactiveVariesBy);

        MapList mapList = new MapList();

        String[] oidsArray = new String[objectList.size()];
        oidsArray = (String[]) objectList.toArray(oidsArray);

        if (isrelId.equals("TRUE")) {

            mapList.addAll(DomainObject
                    .getInfo(context, oidsArray, selectStmts));
        } else {

            mapList.addAll(DomainRelationship.getInfo(context, oidsArray,
                    relSelectStmts));
        }

        Map objMap = new HashMap();

        Iterator itr = mapList.iterator();
        while (itr.hasNext()) {
            objMap = (Map) itr.next();
            String type = (String) objMap.get(DomainConstants.SELECT_TYPE);
            String objID = (String) objMap.get(DomainConstants.SELECT_ID);

            DomainObject domObj = new DomainObject(objID);

            if (type.equals(ConfigurationConstants.TYPE_FEATURE_LIST)) {
                migrateFeatureList(context, objMap);

            } else if (type.equals(ConfigurationConstants.TYPE_INCLUSION_RULE)
                    || type.equals(ConfigurationConstants.TYPE_RULE)
                    || type
                            .equals(ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE)
                    || type.equals(PropertyUtil
                            .getSchemaProperty(context,"type_BOM_Rules"))
                    || type
                            .equals(ConfigurationConstants.TYPE_MARKETING_PREFERENCE)
                    || type.equals(ConfigurationConstants.TYPE_RULE_EXTENSION)
                    || type.equals(ConfigurationConstants.TYPE_FIXED_RESOURCE)
                    || type.equals(ConfigurationConstants.TYPE_PRODUCT_COMPATIBILITY_RULE) ) {

                migrateLeftRightExpresion(context, objID);

                if (type.equals(ConfigurationConstants.TYPE_INCLUSION_RULE)) {
                    migrateDesignVariants(context, objMap);
                    migrateRuleComplexity(context, objID);
                }
            } else if (type
                    .equals(ConfigurationConstants.RELATIONSHIP_VARIES_BY)) {
                migrateVariesBy(context, objMap);
            }
            else if(domObj.isKindOf(context, ProductLineConstants.TYPE_FEATURES)){
                String tempArgs[] = new String[1];
                tempArgs[0] = objID;
                try{
                    JPO.invoke(context,"emxFTRPart", null,"generateDuplicatePartXML", tempArgs,StringList.class);
                }
                catch (Exception e) {
                    mqlLogRequiredInformationWriter("====Exception in Migrating Duplicate Part XML============= \n"+e);
                    e.printStackTrace();
                    throw e;
               }
            } else if(type.equals(ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION)){
                try{
                    HashMap programMap = new HashMap();
                    programMap.put("objectId", objID);
                    String arrJPOArguments[] = JPO.packArgs(programMap);
                    emxProductConfigurationEBOM_mxJPO jpoObj = new emxProductConfigurationEBOM_mxJPO(context, arrJPOArguments);
                    String  strBomXml = (String)jpoObj.updateBomXmlMigration(context, arrJPOArguments);
                    domObj.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_BOMXML, strBomXml);
                }
                catch (Exception e) {
                    mqlLogRequiredInformationWriter("====Exception in Migrating BOM XML============= \n"+e);
                    e.printStackTrace();
                    throw e;
                }
            }

        }// while
    }

    public void migrateVariesBy(Context context, Map objMap) throws Exception {

        String INVALID_CONTEXT = PropertyUtil
                .getSchemaProperty(context,"attribute_InvalidContexts");
        String objID = (String) objMap.get(DomainConstants.SELECT_ID);
        if (objMap.get(selectInactiveVariesBy) != null) {
            String invalidContexts = (String) objMap
                    .get(selectInactiveVariesBy);

            new DomainRelationship(objID).setAttributeValue(context,
                    INVALID_CONTEXT, FrameworkUtil.findAndReplace(
                            invalidContexts, SelectConstants.cSelectDelimiter,
                            ","));
            convertedOidsLog.write("Varies By =" + objID + newline);
        }

    } // End Of migrating varies by()

    public void migrateFeatureList(Context context, Map objMap)
            throws Exception {

        String ATTRIBUTE_PARENT_OBJECT_NAME = PropertyUtil
                .getSchemaProperty(context,"attribute_ParentObjectName");
        String ATTRIBUTE_PARENT_MARKETING_NAME = PropertyUtil
                .getSchemaProperty(context,"attribute_ParentMarketingName");
        String ATTRIBUTE_CHILD_OBJECT_NAME = PropertyUtil
                .getSchemaProperty(context,"attribute_ChildObjectName");
        String ATTRIBUTE_CHILD_MARKETING_NAME = PropertyUtil
                .getSchemaProperty(context,"attribute_ChildMarketingName");
        try {
            String strObjId = (String) objMap.get(DomainConstants.SELECT_ID);

            HashMap attvaluesMap = new HashMap();

            // updating the map with appropriate attributes

            if (objMap.get(selectFeatureListFromName) != null) {
                attvaluesMap.put(ATTRIBUTE_PARENT_OBJECT_NAME, (String) objMap
                        .get(selectFeatureListFromName));
                attvaluesMap.put(ATTRIBUTE_PARENT_MARKETING_NAME,
                        (String) objMap.get(selectFeatureListFromMrktName));

            }
            if (objMap.get(selectFeatureListToName) != null) {
                attvaluesMap.put(ATTRIBUTE_CHILD_OBJECT_NAME, (String) objMap
                        .get(selectFeatureListToName));
                attvaluesMap.put(ATTRIBUTE_CHILD_MARKETING_NAME,
                        (String) objMap.get(selectFeatureListToMrktName));

            }

            if (attvaluesMap != null && attvaluesMap.size() > 0) {

                // updating the feature list attributes.
                new DomainObject(strObjId).setAttributeValues(context,
                        attvaluesMap);
            }

            convertedOidsLog.write("migrateFeatureList =" + strObjId + newline);

        } catch (Exception e) {
            mqlLogRequiredInformationWriter(".....migrateFeatureList....exception........."
                    + e + "\n");
            e.printStackTrace();
            throw e;
        }

    } // End Of migrating featurtelist()

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds no arguments.
     * @return int.
     * @throws Exception
     *             if the operation fails.
     * @since X3.
     */
    public void migrateDesignVariants(Context context, Map objMap)
            throws Exception {

        try {

            String strIncRuleID = (String) objMap
                    .get(DomainConstants.SELECT_ID);

            String ATTRIBUTE_DESIGNVARIANTS = PropertyUtil
                    .getSchemaProperty(context,"attribute_DesignVariants");

            if (objMap.get(selectVariesByGBOM) != null) {
                // mqlLogRequiredInformationWriter("strIncRuleID
                // ="+strIncRuleID+"\n");
                StringList listDVs = (StringList) objMap
                        .get(selectVariesByGBOM);
                // mqlLogRequiredInformationWriter("listDVs
                // ="+objMap.get(selectVariesByGBOM)+"\n");
                // StringBuffer strDVId =new StringBuffer("");
                // for(int m = 0; m < listDVs.size(); m++)
                // {
                // strDVId.append((String)listDVs.get(m));
                // if(m< (listDVs.size()-1))
                // strDVId.append(",");

                // }
                // FrameworkUtil.join(listDVs, ",");
                String dvs = FrameworkUtil.join(listDVs, ",");// strDVId.toString();
                String tempdvs = "";
                if (objMap.get(selectVariesByFeatureList) != null) {
                    StringList tmplistDV = (StringList) objMap
                            .get(selectVariesByFeatureList);
                    // mqlLogRequiredInformationWriter("tmplistDV
                    // ="+objMap.get(selectVariesByFeatureList)+"\n");
                    tempdvs = FrameworkUtil.join(tmplistDV, ",");
                    for (int i = 0; i < tmplistDV.size(); i++) {
                        String tmpDv = (String) tmplistDV.get(i);
                        if (!(dvs.indexOf(tmpDv) >= 0)) {
                            return;
                        }

                    }

                }

                new DomainObject(strIncRuleID).setAttributeValue(context,
                        ATTRIBUTE_DESIGNVARIANTS, tempdvs);

            }

            convertedOidsLog.write("DesignVariants =" + strIncRuleID + newline);
        } catch (Exception e) {
            mqlLogRequiredInformationWriter("======DesignVariants =exception========== \n"
                    + e);
            e.printStackTrace();
            throw e;
        }

    } // End Of migrated DesignVariants

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds no arguments.
     * @return int.
     * @throws Exception
     *             if the operation fails.
     * @since X3.
     */
    public void migrateRuleComplexity(Context context, String objectID)
            throws Exception {

        String ATTRIBUTE_RIGHT_EXPRESSION = PropertyUtil
                .getSchemaProperty(context,"attribute_RightExpression");
        String ATTRIBUTE_DESIGNVARIANTS = PropertyUtil
                .getSchemaProperty(context,"attribute_DesignVariants");
        String ATTRIBUTE_RULE_COMPLEXITY = PropertyUtil
                .getSchemaProperty(context,"attribute_RuleComplexity");
        try {
            StringList tempselects = new StringList(4);
            tempselects.add(DomainConstants.SELECT_NAME);
            tempselects.add(DomainConstants.SELECT_ID);
            tempselects.add("Attribute[" + ATTRIBUTE_DESIGNVARIANTS + "]");
            tempselects.add("Attribute[" + ATTRIBUTE_RIGHT_EXPRESSION + "]");

            String strIncRuleID = "";
            String dvs = "";
            String rightExp = "";

            Map incRuleMap = new DomainObject(objectID).getInfo(context,
                    tempselects);

            strIncRuleID = (String) incRuleMap.get(DomainConstants.SELECT_ID);
            dvs = (String) incRuleMap.get("attribute["
                    + ATTRIBUTE_DESIGNVARIANTS + "]");
            rightExp = (String) incRuleMap.get("attribute["
                    + ATTRIBUTE_RIGHT_EXPRESSION + "]");

            if (dvs == null || ("".equals(dvs)) || ("null".equals(dvs))) {

                // new
                // DomainObject(strIncRuleID).setAttributeValue(context,ATTRIBUTE_RULE_COMPLEXITY,"Complex");
                // mqlLogRequiredInformationWriter(" ======DDVS========
                // "+strIncRuleID+"==\n");
                return;
            } else if (dvs.length() > 0) {
                String partType = new DomainObject(strIncRuleID)
                        .getInfo(
                                context,
                                "from["
                                        + ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
                                        + "].to["
                                        + ConfigurationConstants.TYPE_GBOM
                                        + "].from["
                                        + ConfigurationConstants.RELATIONSHIP_GBOM_TO
                                        + "].to.type");
                if (partType.equals(ProductLineConstants.TYPE_PART_FAMILY)) {
                    // mqlLogRequiredInformationWriter(" ======part
                    // family======== ==\n");
                    return;
                }
            }

            if (rightExp != null && !("".equals(rightExp))
                    && !("null".equals(rightExp))) {
                if (rightExp.indexOf(" AND ") >= 0) {
                    // new
                    // DomainObject(strIncRuleID).setAttributeValue(context,ATTRIBUTE_RULE_COMPLEXITY,"Complex");
                    // mqlLogRequiredInformationWriter("
                    // ======AND========"+strIncRuleID+"== \n");
                    return;
                } else {
                    // checking for any of the ids is of type FeatureList

                    rightExp = FrameworkUtil.findAndReplace(rightExp, "(", "");
                    rightExp = FrameworkUtil.findAndReplace(rightExp, ")", "");
                    rightExp = FrameworkUtil
                            .findAndReplace(rightExp, "AND", "");
                    rightExp = FrameworkUtil.findAndReplace(rightExp, "OR", "");
                    rightExp = FrameworkUtil
                            .findAndReplace(rightExp, "NOT", "");

                    StringList tempList = FrameworkUtil.splitString(rightExp,
                            " ");

                    StringList tempObjselects = new StringList(2);
                    tempObjselects.add(DomainConstants.SELECT_ID);
                    tempObjselects.add(DomainConstants.SELECT_TYPE);

                    boolean flag = false;
                    for (Iterator iterator = tempList.iterator(); iterator
                            .hasNext();) {
                        String strid = ((String) iterator.next()).trim();
                        if (strid != null && !("".equals(strid))
                                && !("null".equals(strid))) {
                            // mqlLogRequiredInformationWriter("
                            // ======strid========"+strid+"== \n");
                            DomainObject donObjid = new DomainObject(strid);
                            Map objMap = donObjid.getInfo(context,
                                    tempObjselects);
                            String type = (String) objMap
                                    .get(DomainConstants.SELECT_TYPE);
                            // mqlLogRequiredInformationWriter("
                            // ======type========"+type+"== \n");
                            if (type.equals(ProductLineConstants.TYPE_FEATURES)
                                    || type
                                            .equals(ProductLineConstants.TYPE_PRODUCTS)) {
                                // new
                                // DomainObject(strIncRuleID).setAttributeValue(context,ATTRIBUTE_RULE_COMPLEXITY,"Complex");
                                // mqlLogRequiredInformationWriter("
                                // ======feature/product========"+strIncRuleID+"==
                                // \n");
                                flag = true;
                                break;
                            }
                            if (type
                                    .equals(ProductLineConstants.TYPE_FEATURE_LIST)) {

                                // mqlLogRequiredInformationWriter("
                                // ======feature List========"+strIncRuleID+"==
                                // \n");
                                String strFeature1 = donObjid
                                        .getInfo(
                                                context,
                                                "to["
                                                        + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM
                                                        + "].from.id");

                                if (!(dvs.indexOf(strFeature1) >= 0)) {
                                    flag = true;
                                    break;
                                }
                            }

                            if (type
                                    .equals(ProductLineConstants.RELATIONSHIP_COMMON_GROUP)) {
                                // new
                                // DomainObject(strIncRuleID).setAttributeValue(context,ATTRIBUTE_RULE_COMPLEXITY,"Complex");
                                // mqlLogRequiredInformationWriter("
                                // ======common gruop========
                                // "+strIncRuleID+"==\n");
                                // String[] ids = new String[]{strid};
                                MapList comGrplist = new DomainRelationship()
                                        .getInfo(context,
                                                new String[] { strid },
                                                new StringList("tomid.to.id"));
                                String comGrpID = (String) ((Map) comGrplist
                                        .get(0)).get("tomid.to.id");
                                if (!(dvs.indexOf(comGrpID) >= 0)) {
                                    flag = true;
                                    break;
                                }
                            }
                        }

                    }// for

                    if (flag)
                        // new
                        // DomainObject(strIncRuleID).setAttributeValue(context,ATTRIBUTE_RULE_COMPLEXITY,"Complex");
                        return;

                }
            }// if right expression

            // if comming here then rule is simple.
            // mqlLogRequiredInformationWriter("=======Simple=========="+strIncRuleID+"==\n");
            new DomainObject(strIncRuleID).setAttributeValue(context,
                    ATTRIBUTE_RULE_COMPLEXITY, "Simple");

            convertedOidsLog.write("migrateRuleComplexity =" + strIncRuleID
                    + newline);

        } catch (Exception e) {
            mqlLogRequiredInformationWriter("====migrateRuleComplexity===eexception========== \n"
                    + e);
            e.printStackTrace();
            throw e;
        }

    } // End Of migrateRuleComplexity

    public void migrateLeftRightExpresion(Context context, String strIncRuleID)
            throws Exception {

        try {
            String ATTRIBUTE_LEFT_EXPRESSION = PropertyUtil
                    .getSchemaProperty(context,"attribute_LeftExpression");
            String ATTRIBUTE_RIGHT_EXPRESSION = PropertyUtil
                    .getSchemaProperty(context,"attribute_RightExpression");

            StringList expression = new StringList("Left");
            expression.add("Right");

            Map paramList = new HashMap();
            paramList.put("intermediate", "true");
            Map programMap = new HashMap();
            programMap.put("paramList", paramList);
            programMap.put("strRuleDisplay",
                    ConfigurationConstants.RULE_DISPLAY_FULL_NAME);

            Map attvaluesMap = new HashMap();

            attvaluesMap.clear();

            Map mapTemp = new HashMap();
            MapList objList = new MapList();
            mapTemp.put(DomainConstants.SELECT_ID, strIncRuleID.trim());
            objList.add(mapTemp);

            programMap.put("objectList", objList);
            String[] arrJPOArguments = new String[1];

            for (int i = 0; i < expression.size(); i++) {
                String tmpExp = (String) expression.get(i);
                if (tmpExp.equals("Right")) {
                    programMap
                            .put(
                                    "strRelType",
                                    ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);

                } else {
                    programMap
                            .put(
                                    "strRelType",
                                    ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION);

                }

                arrJPOArguments = JPO.packArgs(programMap);

                StringList strRightExpressionList = new StringList();
                strRightExpressionList = (StringList) (JPO.invoke(context,
                        "emxBooleanCompatibility", null, "getExpression",
                        arrJPOArguments, StringList.class));
                if (strRightExpressionList != null
                        && strRightExpressionList.size() > 0) {
                    // Rule Expression associated with the Object
                    String strRuleExp = (String) strRightExpressionList.get(0);

                    if (tmpExp.equals("Right")) {
                        attvaluesMap
                                .put(ATTRIBUTE_RIGHT_EXPRESSION, strRuleExp);

                    } else {
                        attvaluesMap.put(ATTRIBUTE_LEFT_EXPRESSION, strRuleExp);

                    }

                }

            }
            if (attvaluesMap != null && attvaluesMap.size() > 0) {

                DomainObject domObject = new DomainObject(strIncRuleID);
                domObject.setAttributeValues(context, attvaluesMap);
            }

            convertedOidsLog.write("migrateLeftRightExpresion =" + strIncRuleID
                    + newline);

        } catch (Exception e) {
            mqlLogRequiredInformationWriter("====migrateLeftRightExpresion===eexception========== \n"
                    + e);
            e.printStackTrace();
            throw e;
        }

    } // End Of migrate leftand rightexp()

    public void help(Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" Migration is a two step process  \n");
        writer.write(" Step1: Find all objects and write them into flat files \n");
        writer.write(" Example 1: \n");
        writer.write(" FindObjects for variantConfiguration HF6 Migration: \n");
        writer.write(" execute program emxVariantConfigurationHF6FindObjects -method findObjects 1000  C:/Temp/oids; \n");
        writer.write(" First parameter  = 1000 indicates no of oids per file \n");
        writer.write(" Second parameter  = C:/Temp/oids is the directory where files should be written  \n");
        writer.write(" Example2: \n");
        writer.write(" Find Relationship for variantConfiguration HF6 Migration: \n");
        writer.write(" execute program emxVariantConfigurationHF6FindObjects -method findRelationship 1000  C:/Temp/relids; \n");
        writer.write(" First parameter  = 1000 indicates no of relids per file \n");
        writer.write(" Second parameter  = C:/Temp/relids is the directory where files should be written  \n");

        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxVariantConfigurationHF6Migration 'C:/Temp/oids' 1 n ; \n");
        writer.write(" First parameter  = C:/Temp/oids directory to read the files from\n");
        writer.write(" Second Parameter = 1 minimum range  \n");
        writer.write(" Third Parameter  = n maximum range  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");

        writer.write("================================================================================================\n");
        writer.write(" \n");
        writer.write(" \n");

        writer.write("================================================================================================\n");
        writer.write(" Sequence of Migration \n");
        writer.write("================================================================================================\n");
        writer.write(" \n");

        writer.write(" 1. Migrate All Attributes except Duplicate Part XML and BOM XML  \n");
        writer.write(" run the following Commands from MQL for Migration \n \n");
        writer.write(" execute program emxVariantConfigurationHF6FindObjects -method findObjects 1000  C:/Temp/oids; \n");
        writer.write(" execute program emxVariantConfigurationHF6Migration 'C:/Temp/oids' 1 n ; \n");
        writer.write(" \n");

        writer.write(" 2. Migrate Relationship Attribute  \n");
        writer.write(" run the following Commands from MQL for Migration \n \n");
        writer.write(" execute program emxVariantConfigurationHF6FindObjects -method findRelationship 1000  C:/Temp/relids; \n");
        writer.write(" execute program emxVariantConfigurationHF6Migration C:/Temp/relids 1 n ; \n");
        writer.write(" \n");


        writer.write(" 3. Migrate Duplicate Part XML and BOM XML  \n");
        writer.write(" run the following Commands from MQL for Migration \n \n");
        writer.write(" execute program emxVariantConfigurationHF6FindObjects -method findObjectsOfTypeFeaturesandProductConfiguration 1000  C:/Temp/FeatureandProductConfiguration; \n");
        writer.write(" execute program emxVariantConfigurationHF6Migration C:/Temp/FeatureandProductConfiguration 1 n ; \n");

        writer.close();
    }

}
