/*
 *  emxFeatureBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.178.2.19.1.1.1.10.1.12 Fri Jan 16 14:21:52 2009 GMT ds-shbehera Experimental${CLASSNAME}.java 1.48 Tue Oct 09 10:24:51 2007 GMT ds-rjoge Experimental$
 *
 */

/**
 * this file is retained for migration purpose.There is no use for actual functions R212 onwards.
 */

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;

/**
 * The <code>emxFeatureBase</code> class holds methods for executing JPO
 * operations related to objects of the type Feature.
 *
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 * @author Enovia MatrixOne.
 */
public class emxFeatureBase_mxJPO extends emxDomainObject_mxJPO {
    /* attribute oon featurelist object */
    public static final String ATTRIBUTE_PARENT_OBJECT_NAME = PropertyUtil
            .getSchemaProperty("attribute_ParentObjectName");

    public static final String ATTRIBUTE_PARENT_MARKETING_NAME = PropertyUtil
            .getSchemaProperty("attribute_ParentMarketingName");

    public static final String ATTRIBUTE_CHILD_OBJECT_NAME = PropertyUtil
            .getSchemaProperty("attribute_ChildObjectName");

    public static final String ATTRIBUTE_CHILD_MARKETING_NAME = PropertyUtil
            .getSchemaProperty("attribute_ChildMarketingName");

    /** A string constant with the value objectId. */
    public static final String OBJECT_ID = "objectId";

    /** A string constant with the value id. */
    public static final String ID = "id";

    /** A string constant with the value parentOID. */
    public static final String PARENT_ID = "parentOID";

    /** A string constant with the value objectList. */
    public static final String OBJECT_LIST = "objectList";

    /** A string constant with the value objectList. */
    public static final String PARAM_MAP = "paramMap";

    // defining pvt. variables to hold the width of numeric fields in Edit
    // Feature window.
    /** A string constant with the value Feature.SEQ_NO_LENGTH. */
    protected static final String SEQ_NO_LENGTH = "Feature.SEQ_NO_LENGTH";

    /** A string constant with the value Feature.MAX_QTY_LENGTH. */
    protected static final String MAX_QTY_LENGTH = "Feature.MAX_QTY_LENGTH";

    /** A string constant with the value Feature.MIN_QTY_LENGTH. */
    protected static final String MIN_QTY_LENGTH = "Feature.MIN_QTY_LENGTH";

    /** A string constant with the value Feature.LIST_PRICE_LENGTH. */
    protected static final String LIST_PRICE_LENGTH = "Feature.LIST_PRICE_LENGTH";

    /** A string constant with the value field_display_choices. */
    protected static final String FIELD_DISPLAY_CHOICES = "field_display_choices";

    /** A string constant with the value field_choices. */
    protected static final String FIELD_CHOICES = "field_choices";

    /** Alias used for New Line Character. */
    protected static final String STR_NEWLINE = "\n";

    /** Alias used for Comma Character. */
    protected static final String STR_COMMA = ",";

    /** Alias used for dot. */
    protected static final String DOT = ".";

    /** Alias used for to. */
    protected static final String TO = "to";

    /** Alias used for from. */
    protected static final String FROM = "from";

    /** Alias used for open brace. */
    protected static final String OPEN_BRACE = "[";

    /** Alias used for cloase brace. */
    protected static final String CLOSE_BRACE = "]";

    /** A string constant with the value !=. */
    public static final String SYMB_NOT_EQUAL = " != ";

    /** A string constant with the value "'". */
    public static final String SYMB_QUOTE = "'";

    /** A string constant with the value symbolic name for "Release" state */
    public static final String strSymbReleaseState = "state_Release";

    public static final String SYMB_state_Release = "state_Release";

    public static final String SYMB_state_Obsolete = "state_Obsolete";
    
    /* use for connectForCopyPaste method */
    List relIdList = new StringList();
    
    public static String SELECT_PART_FAMILY_NAME_FROM_FEATURE = "from["
            + ProductLineConstants.RELATIONSHIP_GBOM_FROM + "].to.from["
            + ProductLineConstants.RELATIONSHIP_GBOM_TO + "].to.name";
    public static String SELECT_PART_FAMILY_TYPE_FROM_FEATURE = "from["
        + ProductLineConstants.RELATIONSHIP_GBOM_FROM + "].to.from["
        + ProductLineConstants.RELATIONSHIP_GBOM_TO + "].to.type";

    // Used by Propagate Applicability
    public static final String RELATIONSHIP_APPLICABLE_ITEM = PropertyUtil.getSchemaProperty("relationship_ApplicableItem");
    public static String SELECT_APPLICABLE_ITEMS_FROM_FEATURE = "to["
            + ProductLineConstants.RELATIONSHIP_EC_IMPLEMENTED_ITEM + "].from.from["
            + RELATIONSHIP_APPLICABLE_ITEM + "].to."
            + DomainConstants.SELECT_ID ;

   //Following variables added for Bug:374276
    boolean flagCmdForProductRev = false;
    boolean flagCmdForProductRevPlat = false;
    boolean flagCmdForProductRevPlatEff = false;
    boolean flagCmdForNewProductRev = false;
   //End of variables added for Bug:374276

    static {
        MULTI_VALUE_LIST.add(SELECT_PART_FAMILY_NAME_FROM_FEATURE);
        MULTI_VALUE_LIST.add(SELECT_PART_FAMILY_TYPE_FROM_FEATURE);
        MULTI_VALUE_LIST.add(SELECT_APPLICABLE_ITEMS_FROM_FEATURE);
    }

    /**
     * Default Constructor.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public emxFeatureBase_mxJPO(Context context, String[] args)
            throws Exception {
        super(context, args);
    }

    /**
     * Main entry point into the JPO class. This is the default method that will
     * be excuted for this class.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return int - An integer status code (0 = success)
     * @throws Exception
     *             if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (!context.isConnected())
            throw new Exception("Not supported on desktop client");
        return 0;
    }



/**
     * This method is used to update the feature structure or product revision / variant
     * with master features of platform
     * In Product revision Context
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return MapList
     * @throws Exception
     *             if the operation fails
     * @since Feature Configuration Module X+5
     * referenced for migration
     */
    public MapList updateListWithMasterFeatures(Context context, String args[])throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList mapToBeUpdated = (MapList)programMap.get("featureList");
        String strProductPlatformId = (String)programMap.get("strProductPlatformId");
        String parentId = (String)programMap.get("parentId");
        String calledFrom = (String)programMap.get("calledFrom");
        String strContextProdId = (String)programMap.get("objectId");
        short recurseLevel = Short.parseShort(((String)programMap.get("recurseLevel")));
        MapList featureListOfPlatform = null;
        Iterator prodMapItrOfPlatform = null;
        Hashtable prodMapOfPlatform = null;
        String strObjWhere = "";
        StringBuffer stbTypeSelect = new StringBuffer(50);
        StringBuffer stbRelSelect = new StringBuffer(50);
        stbTypeSelect = stbTypeSelect.append(ProductLineConstants.QUERY_WILDCARD);
        stbRelSelect = stbRelSelect.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM);
        stbRelSelect = stbRelSelect.append(STR_COMMA);
        stbRelSelect = stbRelSelect.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
        StringList selectStmts = new StringList(15);
        String strLevelKey = "";
        String strLevelFinal = "";
        String strExpandSelect = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO
            + "].to.from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "]";
        String strObjIdSelect = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO
            + "].to.id";
        String strObjNameSelect = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO
            + "].to.name";
        String strObjTypeSelect = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO
            + "].to.type";
        String strRelSelect = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO + "].id";
        String strObjRevSelect = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO
            + "].to.revision";
        String strObjAttSelect = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO
            + "].to.attribute["
            + ProductLineConstants.ATTRIBUTE_MARKETING_NAME + "]";
        String strObjAttSFSelect = "from["
            + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO
            + "].to.attribute["
            + ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT + "]";
        StringList relSelects = new StringList(1);
        relSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Added by KXB for IR-014476V6R2010x STARTS
        String strParentNameSelect = "attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_OBJECT_NAME+"]";
        selectStmts.addElement(strParentNameSelect);
        //Added by KXB for IR-014476V6R2010x ENDS
        selectStmts.addElement(strExpandSelect);
        selectStmts.addElement(strObjIdSelect);
        selectStmts.addElement(strObjNameSelect);
        selectStmts.addElement(strObjTypeSelect);
        selectStmts.addElement(strObjRevSelect);
        selectStmts.addElement(strObjAttSelect);
        selectStmts.addElement(strObjAttSFSelect);
        selectStmts.addElement(strRelSelect);
        selectStmts.addElement(ConfigurationConstants.SELECT_ACTIVE_COUNT);
        selectStmts.addElement(ConfigurationConstants.SELECT_INACTIVE_COUNT);
        selectStmts.add(ProductLineConstants.SELECT_FEATURE_TYPE);
        selectStmts.addElement(DomainObject.SELECT_ID);
        selectStmts.addElement(DomainObject.SELECT_TYPE);
        selectStmts.addElement("attribute["
                + ProductLineConstants.ATTRIBUTE_RULE_TYPE + "]");
        selectStmts.addElement("attribute["
                + ProductLineConstants.ATTRIBUTE_FORCE_PART_REUSE + "]");
        selectStmts.addElement("attribute["
                + ProductLineConstants.ATTRIBUTE_USAGE + "]");
        selectStmts.addElement("attribute["
                + ProductLineConstants.ATTRIBUTE_QUANTITY + "]");
        selectStmts.addElement("to["
                + ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS
                + "].from." + DomainConstants.SELECT_ID);
        String strType = (String)new DomainObject(strContextProdId).getType(context);
        if ((strProductPlatformId != null && !"".equals(strProductPlatformId)) ||(strType.equalsIgnoreCase(ConfigurationConstants.TYPE_MASTER_FEATURE))) {
            if((strType.equalsIgnoreCase(ConfigurationConstants.TYPE_MASTER_FEATURE)) ||
            (strType.equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION))){
                if(strType.equalsIgnoreCase(ConfigurationConstants.TYPE_MASTER_FEATURE)) {
                    strProductPlatformId = strContextProdId;
                }
                strContextProdId = parentId;
            }
           StringBuffer sb = new StringBuffer(30);
            sb.append("((type== \"");
            sb.append(ProductLineConstants.TYPE_FEATURE_LIST);
            sb.append("\" )&&");
            sb.append("(");
            sb.append("to[");
            sb.append(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST);
            sb.append("].from.id");
            sb.append("== '" + strContextProdId+"'");
           sb.append("))");
           StringList lstChildren = ProductLineUtil.getChildrenTypes(
                   context, ProductLineConstants.TYPE_FEATURES);
           for (int i = 0; i < lstChildren.size(); i++) {
               sb.append("|| (");
               sb.append("type== \"");
               sb.append((String) lstChildren.elementAt(i));
               sb.append("\"");
               sb.append(")");
           }
           lstChildren = ProductLineUtil.getChildrenTypes(context,
                   ProductLineConstants.TYPE_PRODUCTS);
           for (int i = 0; i < lstChildren.size(); i++) {
               sb.append("|| (");
               sb.append("type== \"");
               sb.append((String) lstChildren.elementAt(i));
               sb.append("\"");
               sb.append(")");
           }
            strObjWhere = sb.toString();
           /* strObjWhere = strObjWhere + "(";
            strObjWhere = strObjWhere
            + "to["
            + ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST
            + "].from.id"
            + "== '" + strContextProdId+"'";
            strObjWhere = strObjWhere + ")";*/

            DomainObject objProductPlatform = new DomainObject(strProductPlatformId);

            /*featureListOfPlatform = objProductPlatform.getRelatedObjects(context,
                    stbRelSelect.toString(), stbTypeSelect.toString(),
                    selectStmts, relSelects, false, true, recurseLevel,
                    strObjWhere, null);*/

            featureListOfPlatform = objProductPlatform.getRelatedObjects(context,
                    stbRelSelect.toString(), stbTypeSelect.toString(),false, true,recurseLevel,
                    selectStmts, relSelects,
                    strObjWhere, null,null,ConfigurationConstants.TYPE_FEATURE_LIST,null);
            prodMapItrOfPlatform = featureListOfPlatform.iterator();
            prodMapOfPlatform = new Hashtable();
        }

        if ((strProductPlatformId != null && !"".equals(strProductPlatformId)) ||(strType.equalsIgnoreCase(ConfigurationConstants.TYPE_MASTER_FEATURE))) {
        	while (prodMapItrOfPlatform.hasNext()) {

                Map mapOfPlatform = null;
                prodMapOfPlatform = (Hashtable) prodMapItrOfPlatform.next();
                if (((String) prodMapOfPlatform.get("relationship"))
                        .equals(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)) {
                    strLevelKey = (String) prodMapOfPlatform.get("level");
                    strLevelFinal = Integer.toString((Integer
                            .parseInt(strLevelKey) + 1) / 2);
                    mapOfPlatform = new HashMap();
                    if(calledFrom.equalsIgnoreCase("PreviewBOM"))
                    {
                    	String objOfFeatureType = (String) prodMapOfPlatform.get(strObjTypeSelect);
                        if(!mapToBeUpdated.contains(prodMapOfPlatform))
                    	{
                    	    if(!objOfFeatureType.equals(ConfigurationConstants.TYPE_MASTER_FEATURE))
                    		{
                    	    	MapList mapToBeUpdatedNew = new MapList();
                    	    	mapToBeUpdatedNew.add(prodMapOfPlatform);
                    	    	for(int i=0;i<mapToBeUpdatedNew.size();i++)
                    	    	{
                    	    		Map mOld = ((Map)mapToBeUpdatedNew.get(i));
                    	    		String strNewLevel = Integer.toString(((Integer
                                            .parseInt(strLevelKey) - 2 ) ));
                                    // Modified to fix IR IR-018418 - IVU
                    	    		// Re calculate the level context is product variant.
                    	    		if(parentId!=null
                    	    			&& ConfigurationConstants.TYPE_PRODUCT_VARIANT.equals((new DomainObject(parentId).getInfo(context,DomainObject.SELECT_TYPE)))){
                    	    			strNewLevel = Integer
                                        .toString((Integer.parseInt(strNewLevel) + 1) / 2);
                    	    		}
                                    // Modification End  for IR IR-018418 - IVU

                    	    		mOld.put("level", strNewLevel);
                       	    		mapToBeUpdated.add(mOld);
                       	}
                    }
                        }
                    }
                    //Added for bug no:372821
                    else if(calledFrom.equalsIgnoreCase("selectedOptions"))
                    {
                          mapToBeUpdated.add(prodMapOfPlatform);
                    }
                    else
                    {
                    mapOfPlatform.put("level", strLevelFinal);
                    mapOfPlatform
                    .put(
                            "relationship",
                            ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO);
                    mapOfPlatform.put("FeatureListId", (String) prodMapOfPlatform
                            .get(DomainConstants.SELECT_ID));
                    mapOfPlatform
                    .put(
                            "FeatureListFromId",
                            (String) prodMapOfPlatform
                            .get(DomainConstants.SELECT_RELATIONSHIP_ID));
                    mapOfPlatform.put("hasChildren", (String) prodMapOfPlatform
                            .get(strExpandSelect));
                    mapOfPlatform.put(DomainConstants.SELECT_RELATIONSHIP_ID,
                            (String) prodMapOfPlatform.get(strRelSelect));
                    mapOfPlatform.put(DomainConstants.SELECT_ID, (String) prodMapOfPlatform
                            .get(strObjIdSelect));
                    mapOfPlatform.put(DomainConstants.SELECT_NAME,
                            (String) prodMapOfPlatform.get(strObjNameSelect));
                    mapOfPlatform.put(DomainConstants.SELECT_TYPE,
                            (String) prodMapOfPlatform.get(strObjTypeSelect));
                    mapOfPlatform.put(DomainConstants.SELECT_REVISION,
                            (String) prodMapOfPlatform.get(strObjRevSelect));
                    mapOfPlatform
                    .put(
                            "attribute["
                            + ProductLineConstants.ATTRIBUTE_MARKETING_NAME
                            + "]", (String) prodMapOfPlatform
                            .get(strObjAttSelect));
                    mapOfPlatform
                    .put(
                            "attribute["
                            + ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT
                            + "]", (String) prodMapOfPlatform
                            .get(strObjAttSFSelect));
                  //Added by KXB for IR-014476V6R2010x STARTS
                    mapOfPlatform
                    .put("owningParent", (String) prodMapOfPlatform
                            .get(strParentNameSelect));
                  //Added by KXB for IR-014476V6R2010x ENDS
                    mapToBeUpdated.add(mapOfPlatform);
                    }

                }
            }
        }
        return mapToBeUpdated;

    }

    /**
     * This method provide command for Product Revision.
     * Method added for Bug:374276
     *
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
     public boolean commandForProductRev(Context context,String []args)  throws Exception {
          getFlagForCommand(context,args);

         return flagCmdForProductRev;
     }

     /**
      * This method provide command for Product Revision.
      * Method added for Bug:374276
      *
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
      public boolean commandForProductRevPlat(Context context,String []args)  throws Exception {
          getFlagForCommand(context,args);

         return flagCmdForProductRevPlat;
      }

     /**
      * This method provide command for Product Revision.
      * Method added for Bug:374276
      *
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
      public boolean commandForProductRevPlatEff(Context context,String []args)  throws Exception {
         getFlagForCommand(context,args);
         return flagCmdForProductRevPlatEff;
      }

      /**
       * This method provide command for Product Revision.
       * Method added for Bug:374276
       *
       * @param context
       * @param args
       * @return
       * @throws Exception
       */
       public boolean commandForNewProductRev(Context context,String []args)  throws Exception {
          getFlagForCommand(context,args);
          return flagCmdForNewProductRev;
       }


     /**
      * This method set boolean for flag.
      * Method added for Bug:374276
      *
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
      private void getFlagForCommand(Context context,String []args) throws Exception{
          try{
          Map requestMap = (Map) JPO.unpackArgs(args);
          String strModelOID = (String)requestMap.get("objectId");

          String strType = null;
          String strContextId = null;
          String  strRel="";
          String  strCreateProduct="";

          if(strModelOID != null && !strModelOID.equalsIgnoreCase(""))
          {
           DomainObject obj = new DomainObject(strModelOID);

           strType = obj.getInfo(context, DomainConstants.SELECT_TYPE);
           
           if(!(strType.equalsIgnoreCase(ConfigurationConstants.TYPE_MODEL))){
                  Map tempMap1= new HashMap();
                  StringList s2 = new StringList(2);
                  s2.addElement("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
                  tempMap1 = obj.getInfo(context,s2);
                  strModelOID=(String)tempMap1.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].from.id");
                  obj = new DomainObject(strModelOID);

                  Map tempMap= new HashMap();
                  StringList sl = new StringList(2);
                  sl.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].id");
                  sl.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_PLATFORM+"].to.id");
                  tempMap = obj.getInfo(context,sl);
                  strContextId=(String)tempMap.get("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_PLATFORM+"].to.id");

                  String[] argsTemp = new String[3];
                  argsTemp[0] = strContextId;
                  argsTemp[1] = "from";
                  argsTemp[2] = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM;
                  try{
                      strRel = (String)hasRelationship(context,argsTemp);
                  }catch (Exception e) {
                      flagCmdForProductRev = true;
                      flagCmdForProductRevPlat = false;
                      return;
                  }
            }else{

                strRel = obj.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].to.id");
                strCreateProduct = obj.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_PLATFORM+"].to.id");
                if(strRel==null && (strCreateProduct!=null ||strCreateProduct==null)){
                    flagCmdForProductRev = false;
                    flagCmdForProductRevPlat = false;
                    flagCmdForProductRevPlatEff = false;
                    flagCmdForNewProductRev = true;
                    return;
                }
                 else{

                    strContextId = strModelOID;
                    StringList slFeatureListFromId = new StringList(DomainConstants.SELECT_ID);
                    StringList slFeatureListFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                    MapList relatedObjectsTemp = obj.getRelatedObjects(context,
                            ConfigurationConstants.RELATIONSHIP_PRODUCT_PLATFORM,
                            ConfigurationConstants.TYPE_PRODUCT_PLATFORM, slFeatureListFromId,
                            slFeatureListFromRelID, false, true, (short) 1,
                            DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);

                    if(relatedObjectsTemp!=null && relatedObjectsTemp.size()>0){
                        strContextId = (String)((Map)relatedObjectsTemp.get(0)).get(DomainConstants.SELECT_ID);
                        String[] argsTemp = new String[3];
                        argsTemp[0] = strContextId;
                        argsTemp[1] = "from";
                        argsTemp[2] = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM;
                        strRel = (String)hasRelationship(context,argsTemp);
                    }
                }
            }
          }

          if(!(strType.equalsIgnoreCase(ConfigurationConstants.TYPE_MODEL))){
            if(strRel!=null && strRel.equalsIgnoreCase("true"))
              {
                  flagCmdForProductRev = false;
                  flagCmdForProductRevPlat = true;
              }else
              {
                  flagCmdForProductRev = true;
                  flagCmdForProductRevPlat = false;
              }
          }else{
              if(strRel!=null && strRel.equalsIgnoreCase("true"))
              {
                  flagCmdForProductRev = false;
                  flagCmdForProductRevPlat = false;
                  flagCmdForProductRevPlatEff = true;
              }else
              {
                  flagCmdForProductRev = true;
                  flagCmdForProductRevPlat = false;
                  flagCmdForProductRevPlatEff = false;
              }
          }
          }catch (Exception e) {
              flagCmdForProductRev = false;
              flagCmdForProductRevPlat = false;
              flagCmdForProductRevPlatEff = false;
          }
      }//End of method getFlagForCommand

      /**
       * This method is used to check the Preference for Tree Name Display
       *
       * @param context
       *            The ematrix context object.
       * @param String[]
       *            The args .
       * @return String
       * @since R208
       */

      public String checkPrefForTreeDisplayName(Context context,String[] args) throws Exception
      {
      	Map paramMap = (HashMap)((HashMap) JPO.unpackArgs(args)).get("paramMap");
   	    String strObjectId = (String) paramMap.get("objectId");
   	    DomainObject domContextObj = new DomainObject(strObjectId);

   	   StringList objSelect = new StringList(ProductLineConstants.SELECT_NAME);
   	   objSelect.addElement(ProductLineConstants.SELECT_REVISION);
   	   objSelect.addElement("attribute["+ProductLineConstants.ATTRIBUTE_MARKETING_NAME+"]");
   	   objSelect.addElement(ProductLineConstants.SELECT_TYPE);

   	   Map mapObjeDetails = domContextObj.getInfo(context, objSelect);
   	   String strName = (String) mapObjeDetails.get((String)objSelect.get(0));
   	   String strRev = (String) mapObjeDetails.get((String)objSelect.get(1));
   	   String strMarkName = (String) mapObjeDetails.get((String)objSelect.get(2));
   	   String strType = (String) mapObjeDetails.get((String)objSelect.get(3));

   	   
   	   //encoding the nodeLabel is done by BPS, so skipping from here
   	   
   	  // strMarkName=XSSUtil.encodeForHTML(context,strMarkName);
   	  // strRev=XSSUtil.encodeForHTML(context,strRev);
   	  // strType=XSSUtil.encodeForHTML(context,strType);
   	  // strName=XSSUtil.encodeForHTML(context,strName);

   	   String strTreeNameDisplay  = PersonUtil.getTreeDisplayPreference(context);
   	   String strTreeName = "";

   	   if(strTreeNameDisplay.equalsIgnoreCase(ConfigurationConstants.TREE_DISPLAY_MARKETING_NAME))
   	   {
   		   strTreeName = strMarkName;

   		}
   	   else if (strTreeNameDisplay.equalsIgnoreCase(ConfigurationConstants.TREE_DISPLAY_FULL_NAME))
   	   {
   		   strTreeName = strName+" "+strType+" "+strRev;
   	   }
   	   else if (strTreeNameDisplay.equalsIgnoreCase(ConfigurationConstants.TREE_DISPLAY_MARKETING_NAME_REV))
   	   {
   		   strTreeName = strMarkName+" "+strRev;
   	   }
   	   else if (strTreeNameDisplay.equalsIgnoreCase(ConfigurationConstants.TREE_DISPLAY_MARKETING_NAME_TYPE_REV))
   	   {
   		   strTreeName = strType+" "+strMarkName+" "+strRev;
   	   }
   	   else if (strTreeNameDisplay==null || ("").equals(strTreeNameDisplay) || strTreeNameDisplay.equalsIgnoreCase(ConfigurationConstants.TREE_DISPLAY_OBJECT_NAME))
   	   {
   		   //Modified for IR-029750V6R2011
   		   strTreeName = strName+" "+strRev;
   	   }else
   	   {
   		   strTreeName = strName+" "+strRev;
   	   }
   	   return strTreeName;
      }

      /**
       * This method is used to display the revision column in the Feature
       * Powerview
       *
       * @param context
       *            The ematrix context object
       * @param String
       *            The args
       * @return List
       * @since V6R2009-1
       */
      public List getRevisionForColumn(Context context, String[] args)
              throws Exception {
          // unpack the arguments
          Map programMap = (HashMap) JPO.unpackArgs(args);
          List lstobjectList = (MapList) programMap.get("objectList");

          // initialise the local variables
          Map objectMap = new HashMap();
          String strObjId = DomainConstants.EMPTY_STRING;
          List lstNameRev = new StringList();
          String[] objIds = new String[lstobjectList.size()];
          // loop through all the records
          for (int i = 0; i < lstobjectList.size(); i++) {
              objectMap = (Map) lstobjectList.get(i);
              strObjId = (String) objectMap.get(DomainConstants.SELECT_ID);
              objIds[i] = strObjId;

          }

          StringList selects = new StringList(4);
          selects.add(DomainConstants.SELECT_TYPE);
          selects.add(DomainConstants.SELECT_REVISION);

          MapList typeRevMapList = DomainObject.getInfo(context, objIds, selects);

          String strType = DomainConstants.EMPTY_STRING;
          for (int j = 0; j < typeRevMapList.size(); j++) {
              Map typeRevMap = (Map) typeRevMapList.get(j);
              strType = (String) typeRevMap.get(DomainConstants.SELECT_TYPE);

              if (strType
                      .equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_VARIANT)) {
                  StringBuffer productVariantRevision = new StringBuffer(
                          (String) typeRevMap
                                  .get(DomainConstants.SELECT_REVISION));
                  lstNameRev.add(productVariantRevision.substring(0, 1)
                          .toString());
              } else {
                  lstNameRev.add((String) typeRevMap
                          .get(DomainConstants.SELECT_REVISION));
              }

          }
          return lstNameRev;
      }


      /**
       * Checks whether object name of feature name display is set or not.
       *
       * @param context
       *            the eMatrix <code>Context</code> object
       * @param args
       *            holds arguments
       * @return Object - boolean true if feature name display is set to object
       *         name
       * @throws Exception
       *             if operation fails
       * @since Feature Configuration X3
       */
      public Object checkObjectNameDisplayPreference(Context context,
              String[] args) throws Exception {
          Boolean isObjectName = Boolean.valueOf(false);
//          String adminType = "person";
          String adminName = context.getUser();
          String propertyValue;
//          StringBuffer cmd = new StringBuffer(150);
//          cmd.append("print ");
//          cmd.append(adminType);
//          cmd.append(" \"");
//          cmd.append(adminName);
//          cmd.append("\" select property[");
//          cmd.append("preference_FeatureDisplay");
//          cmd.append("].value dump");
//          propertyValue = MqlUtil.mqlCommand(context, cmd.toString());
          //TODO -unreferenced can be removed
          String sCommandStatement = "print person $1 select $2 dump";
          propertyValue =  MqlUtil.mqlCommand(context, sCommandStatement, true, adminName, "property[preference_FeatureDisplay].value"); 

          if (propertyValue == null || propertyValue.equals("")
                  || propertyValue.equalsIgnoreCase("ObjectName")) {
              isObjectName = Boolean.valueOf(true);
          }
          return isObjectName;
      }

      /**
       * Checks whether marketing name of feature name display is set or not.
       *
       * @param context
       *            the eMatrix <code>Context</code> object
       * @param args
       *            holds arguments
       * @return Object - boolean true if feature name display is set to marketing
       *         name
       * @throws Exception
       *             if operation fails
       * @since ProductCentral X3
       */
      public Object checkMarketingNameDisplayPreference(Context context,
              String[] args) throws Exception {
          Boolean isMarketingName = Boolean.valueOf(false);
//          String adminType = "person";
          String adminName = context.getUser();
          String propertyValue;
//          StringBuffer cmd = new StringBuffer(150);
//          cmd.append("print ");
//          cmd.append(adminType);
//          cmd.append(" \"");
//          cmd.append(adminName);
//          cmd.append("\" select property[");
//          cmd.append("preference_FeatureDisplay");
//          cmd.append("].value dump");
//          propertyValue = MqlUtil.mqlCommand(context, cmd.toString());
          //TODO -unreferenced can be removed
          String sCommandStatement = "print person $1 select $2 dump";
          propertyValue =  MqlUtil.mqlCommand(context, sCommandStatement, true, adminName, "property[preference_FeatureDisplay].value"); 

          if (propertyValue.equals("MarketingName")) {
              isMarketingName = Boolean.valueOf(true);
          }
          return isMarketingName;
      }

      /**
       * Get the list of all features which satisfies this build.
       *
       * @param context
       *            the eMatrix <code>Context</code> object
       * @param args
       *            holds the following input arguments: 0 - HashMap containing
       *            one String entry for the key "objectId"
       * @return MapList - MapList of all features which satisfies this build
       * @throws Exception
       *             if the operation fails
       * @since ProductCentral 10-0-0-0
       */

      @com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getSatisfiedFeatures(Context context, String[] args)
              throws Exception {
          MapList relBusObjPageList = new MapList();
          StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          String paramMap = (String) programMap.get(OBJECT_ID);
          // Defining a StringList type variable to store the relationship ids.
          // Relationships are selected by its Ids
          StringList relSelects = new StringList(
                  DomainConstants.SELECT_RELATIONSHIP_ID);
          String strRelationship = ConfigurationConstants.RELATIONSHIP_BUILD_SATISFIES;
          //type has been modified in R212 release to add new FTR types
          StringBuffer sbType = new StringBuffer(100);
          sbType.append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURES).append(STR_COMMA)
          .append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);


          setId(paramMap);
          short sh = 1;
          // Setting the ID of the parent feature to obtain all options under the
          // parent feature
          setId(paramMap);
          relBusObjPageList = getRelatedObjects(context, strRelationship,
          		sbType.toString(), objectSelects, relSelects, false, true, sh,
                  DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
          return relBusObjPageList;
      }

      /**
      *
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
     public Vector getCommonGroups(Context context, String args[])
             throws Exception {
         Vector vecCGNames = new Vector();
         HashMap programMap = (HashMap) JPO.unpackArgs(args);

         MapList objectList = (MapList) programMap.get("objectList");
         int iNumOfObjects = objectList.size();
         Map tempMap = null;
         for (int iCnt = 0; iCnt < iNumOfObjects; iCnt++) {
             tempMap = (Map) objectList.get(iCnt);
             if(tempMap.containsKey("Common Group Name"))
                 vecCGNames.add((String) tempMap.get("Common Group Name"));
             else
                 vecCGNames.add("-");
         }
         return vecCGNames;
     }



     /**
      * Get the list of all features which satisfies this build and build the
      * Stringlist of IDs,to pass it to the excludeOIDProgram
      *
      * @param context
      *            the eMatrix <code>Context</code> object
      * @param args
      *            holds the following input arguments: 0 - HashMap containing
      *            one String entry for the key "objectId"
      * @return StringList - List of all features which satisfies this build
      * @throws Exception
      *             if the operation fails
      * @since FTR R212 (V6R2012x)
      */
     @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
     public StringList getSatisfiedFeaturesList(Context context, String [] args)
 	 throws Exception
 	 {
     	StringList listConnectedFeatures = new StringList();
     	MapList relBusObjList= getSatisfiedFeatures( context,  args);
     	if (relBusObjList != null && relBusObjList.size()>0){
 	    	for (Iterator iterator = relBusObjList.iterator(); iterator.hasNext();) {
 				Map  objMap= (Map) iterator.next();

 				listConnectedFeatures.add(objMap.get(DomainConstants.SELECT_ID));
 			}
     	}
     	return listConnectedFeatures;
 	 }

     /**
      * Get the list of all features which satisfies teh REQUIREMENT
      *
      * @param context
      *            the eMatrix <code>Context</code> object
      * @param args
      *            holds the following input arguments: 0 - HashMap containing
      *            one String entry for the key "objectId"
      * @return StringList - List of all features which satisfies this build
      * @throws Exception
      *             if the operation fails
      * @since FTR R212 (V6R2012x)
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getFeatuersSatisfyingRequirement (Context context, String[] args)
         throws Exception
     {
         //Unpacks the argument for processing
         HashMap programMap = (HashMap)JPO.unpackArgs(args);
         //Gets the objectId in context
         String strObjectId = (String)programMap.get("objectId");
         //String List initialized to retrieve back the data
         StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
         StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
         //Sets the relationship name to the one connecting Feature and Requirement
         String strRelationship = ProductLineConstants.RELATIONSHIP_REQUIREMENT_SATISFIED_BY;
         ////type has been modified in R212 release to add new FTR types
         StringBuffer sbType = new StringBuffer(100);
         sbType.append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURES).append(STR_COMMA)
         .append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);
         //Domain Object initialized with the object id.
         setId(strObjectId);
         short sh = 1;
         //The getRelatedObjects method is invoked to get the list of all Features connected to Requirement.
         MapList relBusObjPageList = getRelatedObjects(context, strRelationship, sbType.toString(),
                 objectSelects, relSelects, true, true, sh, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
         return(relBusObjPageList);
     }
     /**
      * Get the list of all features which satisfies the Requirement and builds the
      * Stringlist of IDs,to pass it to the excludeOIDProgram
      *
      * @param context
      *            the eMatrix <code>Context</code> object
      * @param args
      *            holds the following input arguments: 0 - HashMap containing
      *            one String entry for the key "objectId"
      * @return StringList - List of all features which satisfies this build
      * @throws Exception
      *             if the operation fails
      * @since FTR R212 (V6R2012x)
      */
     @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
     public StringList getFeatuersListSatisfyingRequirement(Context context, String [] args)
 	 throws Exception
 	 {
     	StringList listConnectedFeatures = new StringList();
     	MapList relBusObjList= getFeatuersSatisfyingRequirement( context,  args);
     	if (relBusObjList != null && relBusObjList.size()>0){
 	    	for (Iterator iterator = relBusObjList.iterator(); iterator.hasNext();) {
 				Map  objMap= (Map) iterator.next();

 				listConnectedFeatures.add(objMap.get(DomainConstants.SELECT_ID));
 			}
     	}
     	return listConnectedFeatures;
 	 }




}// end of the class
