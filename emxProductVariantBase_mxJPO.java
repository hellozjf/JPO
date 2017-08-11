/*
**emxProductVariantBase
**Copyright (c) 1993-2016 Dassault Systemes.
**All Rights Reserved.
**This program contains proprietary and trade secret information of
**Dassault Systemes.
**Copyright notice is precautionary only and does not evidence any actual
**or intended publication of such program
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.configuration.ProductVariant;
import com.matrixone.apps.configuration.RuleProcess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UITableGrid;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;


/**
 * The <code>emxProductVariantBase</code> class contains methods related to the admin type Product Variant.
 * This includes methods for the  Create
 * @since Feature Configuration Module X3
 */
public class emxProductVariantBase_mxJPO extends emxDomainObject_mxJPO
{

    //Alias used for key emxProduct.Error.UnsupportedClient.
    public static final String CHECK_FAIL = "emxProduct.Error.UnsupportedClient";
    public static final String SUITE_KEY = "Configuration";
    /**
    * Default Constructor.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @since Feature Configuration Module X3
    */
    emxProductVariantBase_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
    * Main entry point into the JPO class. This is the default method that will be excuted for this class.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return int - An integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since Feature Configuration Module X3
    */
    public int mxMain(Context context, String[] args) throws Exception
    {
        if (!context.isConnected())
        {
            String strLanguage = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context,SUITE_KEY,CHECK_FAIL,strLanguage);
            throw new Exception(strContentLabel);
        }
        return 0;
    }

   

   
    
    
    /**
    * This method retrieves all the Product Variant connected to the context product as Variant.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList - MapList containing the id of Product Variant objects connected to the context product.
    * @throws Exception if the operation fails
    * @since Feature Configuration Module X3
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getProductVariants(Context context, String args[])  throws Exception
    {
        //Sets the relationship name to the one connecting Product and Version (Product)
        String relationshipName = ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION;
        //Where condition to filter the Versions of the Products
        StringBuffer strWhereCondition = new StringBuffer();
        
        //Function call to retrieve all the Products(versions) connected to the context object with the specific relationship name.
        MapList relBusObjList = expandForProductVariants(context,args,relationshipName,strWhereCondition.toString());
        
        return relBusObjList;
    }

    /**
    * This method accepts Relationship name and WHERE condition and expands the context object based
    * on the speicified relationship, to retrievel all the Product Variants matching the where condition.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - Has the packed Hashmap having information of the object in context.
    * @param relationshipName - Name of the relationship that has to be traversed to get the Products
    * @param strWhereCondition - Where Condition, that needs to be used to filter the Product objects.
    * @return MapList - MapList containing the id of Product objects and the relationships
    * @throws Exception if the operation fails
    * @since Feature Configuration Module X3
    */
    protected MapList expandForProductVariants(Context context, String args[],String relationshipName, String strWhereCondition)  throws Exception
    {
        //The packed argument send from the JPO invoke method is unpacked to retrive the HashMap.
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        //The object id of the context object is retrieved from the HashMap using the appropriate key.
        String strObjectId = (String)  programMap.get("objectId");
        //String List initialized to retrieve back the id from the business objects and the relationships
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_TYPE);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Domain Object initialized with context object id.
        setId(strObjectId);
        //The actual name of the base and abstract type of product is retrieved for querying purpose.
        String strType = ProductLineConstants.TYPE_PRODUCT_VARIANT;
        //The getRelatedObjects method is invoked to get the list of products connected to the context object with the specific relationship.
        MapList relBusObjPageList = getRelatedObjects(context , relationshipName, strType, objectSelects, relSelects, true, true, (short)1, strWhereCondition, DomainConstants.EMPTY_STRING, 0);
        //The MapList containing the information (id) of the Product and the relationship.
        return relBusObjPageList;
    }



        /**
        * This method is used to get all the Product Variant in the Product Context
        * This will use the Dynamic Column Behivour to show the Product Variants
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the FormBean contents
        * @return MapList - All the Product Variants/Product Revision
        * @throws Exception if the operation fails
        * @since R11
        */

    public MapList getProductVariantsSelectedFeatures(Context context, String args[])throws Exception
    {

        String revisionVariantName = "";
        String grpHeader = "";
        HashSet hashParentList = new HashSet();
        StringList strlistOtherParentPartList = new StringList();
        HashMap hash  = (HashMap) JPO.unpackArgs(args);
        HashMap paramList = (HashMap) hash.get("requestMap");
        
        String strSelIdsWhrClause = "";
        String strSelectId = (String) paramList.get("selectId");	
	    if(strSelectId != null && !"".equalsIgnoreCase(strSelectId)){
	    	StringList slSelectMPIds = new StringList();
	        StringTokenizer strSelectTok = new StringTokenizer(strSelectId, ",");
	        String strSelectIdKey = null;	        
	        StringBuffer sbWhereCondition1 = new StringBuffer(25);
	        sbWhereCondition1 = sbWhereCondition1.append("id==");
	        while(strSelectTok.hasMoreTokens())
	        {
	        	strSelectIdKey = strSelectTok.nextToken();
	            sbWhereCondition1 = sbWhereCondition1.append(strSelectIdKey);
	            sbWhereCondition1 = sbWhereCondition1.append("|| id==");
	            if(strSelectIdKey != null && !"".equalsIgnoreCase(strSelectIdKey))
	            {
	            	slSelectMPIds.add(strSelectIdKey);
	            }   
	        }
	        strSelIdsWhrClause = sbWhereCondition1.toString();
	        strSelIdsWhrClause = strSelIdsWhrClause.substring(0, strSelIdsWhrClause.lastIndexOf("||"));
	    }


        String  strEffectivityOption = EnoviaResourceBundle.getProperty(context,"emxConfiguration.ViewEffectivity.FeatureUsage");
        String callFunc = "getDynamicColumnPFLUsageForView"; 
        MapList returnMap = new MapList();
        StringList strListSelectstmts = new StringList();
        strListSelectstmts.add(DomainObject.SELECT_ID);
        strListSelectstmts.add(DomainObject.SELECT_NAME);
        strListSelectstmts.add(DomainObject.SELECT_REVISION);
        
        try
        {
           // for(int k=0;k<objList.size();k++)
           // {
                MapList mapChildParts = null;
               // Map aMap = (Map) objList.get(k);
                String strParentObjectId = (String) paramList.get("objectId");

                DomainObject dom = new DomainObject(strParentObjectId);
                grpHeader = "emxProduct.Table.ProductVariants";
                mapChildParts = dom.getRelatedObjects(context, 
                                                              ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION, 
                                                              ConfigurationConstants.TYPE_PRODUCT_VARIANT,
                                                              strListSelectstmts,
                                                              new StringList(),
                                                              false,
                                                              true,
                                                              (short)0,strSelIdsWhrClause,"", 0);
                        if(mapChildParts.isEmpty())
                        {
                            String language = context.getSession().getLanguage();
                            String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                                    "emxProduct.Error.ProductRevision.NoProductVariants",language);
                            emxContextUtil_mxJPO.mqlNotice(context,strAlertMessage);
                            throw new FrameworkException(strAlertMessage);
                        }
                

               mapChildParts.sort(DomainObject.SELECT_REVISION,"descending","String");
               hashParentList.add(mapChildParts);
            //}
            
            Iterator itr = hashParentList.iterator();
            while(itr.hasNext())
            {

                MapList parentmapList = (MapList) itr.next();
                for(int j=0;j<parentmapList.size();j++)
                {
                    Map tempMap = (Map) parentmapList.get(j);
                    String strOtherParentName = (String) tempMap.get("name");
                    String strProductVariantId = (String) tempMap.get("id"); 
                    DomainObject domProductVariant = new DomainObject(strProductVariantId);
                    
                    HashMap hashfornewColumn = new HashMap();
                    HashMap hashColumnSetting = new HashMap();
                    hashColumnSetting.put("Column Type","programHTMLOutput");
                    hashColumnSetting.put("Registered Suite","Configuration");
                    hashColumnSetting.put("function",callFunc);
                    hashColumnSetting.put("program","emxProductVariant");
                    hashColumnSetting.put("Export", "true");
                  

                    if(strEffectivityOption.equalsIgnoreCase("true") && callFunc.equalsIgnoreCase("getDynamicColumnPFLUsageForView"))
                    {
                        hashColumnSetting.put("Editable", "true");                   
                        hashColumnSetting.put("Input Type", "combobox");
                        hashColumnSetting.put("function", "getPFLUsageofProductVariant");
                        hashColumnSetting.put("program", "emxProductVariant");                        
                        hashColumnSetting.put("Range Function", "getPFLUsageRange");
                        hashColumnSetting.put("Range Program", "emxProductVariant");                  
                        hashColumnSetting.put("Update Function", "updatePFLUsage");
                        hashColumnSetting.put("Update Program", "emxProductVariant");
                        hashColumnSetting.put("Edit Access Function", "isCellValueEditable");
                        hashColumnSetting.put("Edit Access Program", "emxProductVariant");
                    } 
                    
                    String revisionVariantId = "";
                   
                        revisionVariantName = domProductVariant.getInfo(context,"attribute["+ConfigurationConstants.VARIANT_NAME+"]");
                        revisionVariantId = domProductVariant.getId(context);
                        strlistOtherParentPartList.add(strOtherParentName);
                        hashColumnSetting.put("Group Header", grpHeader);
                        hashColumnSetting.put("Width","100");                        
                        //Setting the global Column with the above settings
                        hashfornewColumn.put("settings",hashColumnSetting);
                        hashfornewColumn.put("label",revisionVariantName);
                        hashfornewColumn.put("name",revisionVariantId);
                        hashfornewColumn.put("id",revisionVariantId);
                        returnMap.add(hashfornewColumn);
		                        
		                    
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("The Exception occured while setting the column data:"+e);
            throw e;
        }finally
        {
            return returnMap;
        }
    }

       
       /**
         * Returns the columm output as HTML text box.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *    0 - HashMap containing one MapList entry for the key "objectList"
         *          This MapList is a list of HashMaps containing one String entry for the key "objectId".
         * @param strAttributeName the attribute for which drop down HTML has to be build
         * @param strSelectAttribute select expression for the attribute
         * @param iLength width of the textbox
         * @return Vector - Vector the columm output as HTML drop down.
         * @throws Exception if the operation fails
         * @since ProductCentral 10-0-0-0
         */
        protected Vector getHTMLTagsForTextBox(
            Context context,
            String[] args,
            String strAttributeName,
            String strSelectAttribute,
            int iLength)
            throws Exception
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList relBusObjPageList = (MapList) programMap.get("objectList");
            //String strObjId = (String)programMap.get(OBJECT_ID);
            //Checking if objectList obtained is null
            if (!(relBusObjPageList != null))
                throw new Exception();
            //Get the number of objects in objectList
            int iNumOfObjects = relBusObjPageList.size();
            Object object = null;
            //Getting the bus ids for objects in the table
            String arrObjId[] = new String[iNumOfObjects];
            //Getting the rel ids for the concerned objects
            String arrRelId[] = new String[iNumOfObjects];
            for (int i = 0; i < iNumOfObjects; i++)
            {
                object = relBusObjPageList.get(i);
                if (object instanceof HashMap)
                {
                    arrObjId[i] =
                        (String) (((HashMap) object)
                            .get(DomainConstants.SELECT_ID));
                    arrRelId[i] =
                        (String) (((HashMap) object)
                            .get(DomainConstants.SELECT_RELATIONSHIP_ID));
                }
                else
                {
                    arrObjId[i] =
                        (String) (((Hashtable) object)
                            .get(DomainConstants.SELECT_ID));
                    arrRelId[i] =
                        (String) (((Hashtable) object)
                            .get(DomainConstants.SELECT_RELATIONSHIP_ID));
                }
            }
            // getting the default values for object ids
            StringList strSelectAttrib = new StringList(strSelectAttribute);
            matrix.db.BusinessObjectWithSelectList businessObjectWithSelectList =
                matrix.db.BusinessObject.getSelectBusinessObjectData(
                    context,
                    arrObjId,
                    strSelectAttrib);
            // forming the HTML Tags
            Vector textBoxVector = new Vector(iNumOfObjects);
            StringBuffer strTextBoxHTML = null;
            String strSelectedAttribute = null;
            String strLength = Integer.toString(iLength);
            for (int i = 0; i < iNumOfObjects; i++)
            {
                // default value for id
                strSelectedAttribute =
                    (businessObjectWithSelectList.getElement(i)).getSelectData(
                        (String) strSelectAttrib.get(0));
                // forming the HTML tag for one row
                strTextBoxHTML =
                    new StringBuffer("<input type=\"text\" size=\"20\"");
                strTextBoxHTML.append(" maxlength=\"").append(strLength).append(
                    "\" name=\"");
                strTextBoxHTML.append(arrObjId[i]).append(":").append(
                    strAttributeName);
                strTextBoxHTML.append("\" value=\"").append(
                    strSelectedAttribute).append(
                    "\" />");
                textBoxVector.add(strTextBoxHTML.toString());
            }
            return textBoxVector;
        }

        public Vector editVariantName(Context context, String[] args)
        throws Exception
        {
            String strAttribute =  ConfigurationConstants.VARIANT_NAME;
            String strSelectAttribute = "attribute[" + strAttribute + "]";
            return getHTMLTagsForTextBox(
                context,
                args,
                strAttribute,
                strSelectAttribute,
                (short)80);
        }

        public Vector editMarketingName(Context context, String[] args)
        throws Exception
        {
            String strAttribute =  ProductLineConstants.ATTRIBUTE_MARKETING_NAME;
            String strSelectAttribute = "attribute[" + strAttribute + "]";
            return getHTMLTagsForTextBox(
                context,
                args,
                strAttribute,
                strSelectAttribute,
                (short)80);
        }

    /**
     * This method is used as a access function to show/not to show
     * Create,Clone,Edit, and Delete Commands of Product Variants 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return Object - true if the Product is not in Release state
     *                  false if the Product is in Release state 
     * @throws Exception if the operation fails
     * @since Feature Configuration Module X3
     */

    public Object showLinkForProductVariant(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objID = (String) programMap.get("objectId");
        DomainObject dom = new DomainObject(objID);
        String strState = (String)dom.getInfo(context,DomainObject.SELECT_CURRENT);
        Boolean isObjectName = Boolean.valueOf(false);
        if(!strState.equalsIgnoreCase(ProductLineConstants.STATE_OBSOLETE))
        {
           isObjectName = Boolean.valueOf(true);
        }
        return isObjectName;
    }
     /**
      * This method is used to Clone Product Variants when the Context
      * Product is Revised or Copied to Other Product 
      *
      * @param context the eMatrix <code>Context</code> object
      * @param isRevise if the Product is Revised - true otherwise false
      * @param srcProductId the Context Source Product Id
      * @param dstProductId the Context Destination Product Id
      * @throws Exception if the operation fails
      * @return void
      * @since Feature Configuration Module X3
      */

      public void cloneProductVariantOnProductReviseAndCopy(Context context, String args[]) throws Exception
      {
          try
          {
    		  ArrayList programMap = (ArrayList)JPO.unpackArgs(args);
                  String  isRevise = (String)programMap.get(0);
    		  String srcProductId = (String) programMap.get(1);
    		  String destinationObjectId = (String) programMap.get(2);
    		  ProductVariant productVariantBean = new ProductVariant();
    		  MapList productVariants = productVariantBean.getProductVariants(context,srcProductId);
    		  //If the Context Product don't have any Product Variants connected then 
    		  //this will be skipped 
    		  if(productVariants.size()>0)
    		  {
    			  DomainObject domRevisedProduct = new DomainObject(destinationObjectId);
    			  String strDestinationProductName = (String)domRevisedProduct.getInfo(context,DomainObject.SELECT_NAME);
    			  String srcProductVariantId = null;
    			  StringBuffer strBufProductVariantRevision=null;
    			  String srcNewProductVariantRevision = null;
    			  StringBuffer strBufProductVariantName=null;
    			  String strNewProductVariantName = null;
    			  String productVariantName = null;
    			  Map pvMap = new HashMap();
    			  for(int i=0;i<productVariants.size();i++)
    			  {
    				  Map tempMap = (Map) productVariants.get(i);
    				  srcProductVariantId = (String)tempMap.get("id");
    				  productVariantName = (String)tempMap.get("name");
    				  strBufProductVariantRevision = new StringBuffer(tempMap.get("revision").toString());
    				  srcNewProductVariantRevision = strBufProductVariantRevision.replace(0,1,domRevisedProduct.getInfo(context,DomainObject.SELECT_REVISION).toString()).toString();
    				  strBufProductVariantName = new StringBuffer(productVariantName);
    				  StringTokenizer strToken = new StringTokenizer(strBufProductVariantName.toString(),":");
    				  strToken.nextToken();
    				  strNewProductVariantName = strDestinationProductName+":"+strToken.nextToken();
    				  DomainObject domProductVariant = new DomainObject(srcProductVariantId);
    				  StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    				  objectSelects.add(DomainObject.SELECT_NAME);
    				  objectSelects.add(DomainObject.SELECT_REVISION);
    				  objectSelects.add(DomainObject.SELECT_TYPE);
    				  BusinessObject clonedBOProductVariant = null;
    				  if(isRevise.equalsIgnoreCase("true"))
    					  clonedBOProductVariant = domProductVariant.cloneObject(context,productVariantName,srcNewProductVariantRevision,null);
    				  else
    					  clonedBOProductVariant = domProductVariant.cloneObject(context,strNewProductVariantName,srcNewProductVariantRevision,null);
    				  DomainObject clonedObjectDOProductVariant = DomainObject.newInstance(context,clonedBOProductVariant);
    				  String strNewProductVariantID = (String) clonedObjectDOProductVariant.getInfo(context,DomainObject.SELECT_ID);
    				  com.matrixone.apps.domain.DomainRelationship.connect(context,destinationObjectId,ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION,strNewProductVariantID,true);
    				  if(isRevise.equalsIgnoreCase("true")){
    					  // background process for Inherit mandatory rules
    			    	  String strInheriteManBG = EnoviaResourceBundle.getProperty(context, "emxProduct.ProductVariant.InheritMandatoryRules.BackGroundJob");

    			          String[] argsIM = new String[2];
    			          //product ID
    			          argsIM[0] = destinationObjectId;
    			          //variant ID
    			          argsIM[1] = strNewProductVariantID;

    			          if(strInheriteManBG.equals("Yes"))
    			          {
    			        	  Job job = new Job("emxRule", "inheritMandatoryRulesToProductVariant", argsIM);
    			        	  job.setContextObject(strNewProductVariantID);
    			        	  job.setTitle("Mandatory Rule's Inheritance");
    			        	  job.setDescription("Inheriting the Mandatory Rules to the created Product Variant");
    			        	  job.createAndSubmit(context);

    			          }else{

    			        	  JPO.invoke(context, "emxRule", null,
    			        			  "inheritMandatoryRulesToProductVariant", argsIM);
    			          }
    				  }
    				  pvMap.put(srcProductVariantId, strNewProductVariantID);
    			  }    			                      
    			  //Making PFL connection
    			  //Get Logical Feature Relationship ids
    			  LogicalFeature logFeature = new LogicalFeature(destinationObjectId);
    			  StringList sLstObjeSelects = new StringList();
    			  sLstObjeSelects.addElement(SELECT_ID);
    			  StringList sLstRelSelects = new StringList();
    			  MapList mLstLogicalStructure = (MapList)logFeature.getLogicalFeatureStructure(context,null, null, sLstObjeSelects, 
    					  sLstRelSelects, false,true,0,0, DomainObject.EMPTY_STRING, null, 
    					  DomainObject.FILTER_ITEM,DomainObject.EMPTY_STRING);    			 
    			  if(mLstLogicalStructure.size()>0)
    			  {
    				  
    				  for(int i=0;i<productVariants.size();i++)
    				  {
    					  HashMap hMapPVPFLs = new HashMap();
    					  StringBuffer selectedFeaturerelid = new StringBuffer();
    					  Map tempMap = (Map) productVariants.get(i);
    					  String oldPVID = (String)tempMap.get("id");                                  
    					  Object objOldLogFeatureRelID = (Object)tempMap.get("from[" + ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST
    							  + "].torel.to.id");
    					  StringList connectedLogFeatureIDs=ConfigurationUtil.convertObjToStringList(context,objOldLogFeatureRelID);
    					  for(int k=0;k<connectedLogFeatureIDs.size();k++)
    					  {
    						  String connectedLogFeatureID = (String)connectedLogFeatureIDs.get(k);
    						  RelationshipType relation = new RelationshipType(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST);
    						  for(int j=0;j<mLstLogicalStructure.size();j++)
    						  {
    							  Map mLogicalStructure = (Map)mLstLogicalStructure.get(j);
    							  String newLogFeatureRelID = (String)mLogicalStructure.get(SELECT_RELATIONSHIP_ID);
    							  String logFeatureID = (String)mLogicalStructure.get(SELECT_ID);
    							  if(logFeatureID.equalsIgnoreCase(connectedLogFeatureID))
    							  {
        							  ProductLineCommon plCommon = new ProductLineCommon(newLogFeatureRelID);
    								  String newPVID = (String)pvMap.get(oldPVID);
    								  selectedFeaturerelid.append(newLogFeatureRelID);
    								  selectedFeaturerelid.append(",");
    								  //On type Products Clone PFL Rel Get replicated- will not require to make PFL Connection 
    								  //hMapPVPFLs.put(logFeatureID, plCommon.connectObject(context,relation,newPVID,true));
    							  }
    						  }
    					  }
    					  //createLF MAP for PFL and LF id and pass other args to roll up DV for new PV in new Prod Rev
            			  ProductVariant newPV = new ProductVariant((String)pvMap.get(oldPVID));
            			  newPV.rollUpDesignVariant(context,selectedFeaturerelid.toString(),destinationObjectId,hMapPVPFLs, true);   
    				  }				  
    			  }
    		  }
          }
          catch (Exception e) 
          {
              System.out.println("Exception while Revising Product Variant...");
          }
      }
      
      /**
       * checks if the product is in release state .
       * called as part of check trigger program, when promoting ProductVariant to release state
       * @param context the eMatrix <code>Context</code> object
       * @param args - Holds the parameters passed from the calling method
       * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
       * @throws Exception if the operation fails
       *
       */
       public int promoteProductVariants(Context context, String args[]) throws Exception
       {
           //The Product object id sent by the emxTriggerManager is retrieved here.
           String objectId = args[0];
                      
           //StringList for retrieving the object information (state) is initialized.
           StringList objectSelects = new StringList("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION+"].from."+DomainConstants.SELECT_CURRENT);
           objectSelects.add("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.id");
           
           //The context object is expanded and the state of the First level features are obtained in the MapList
           Map busObjMap = new DomainObject(objectId).getInfo(context, objectSelects);
           //The number of Product Variants connected is obtained.
          
          if(busObjMap !=null){              
                   //The state is retreived from the MapList obtained.
                   String strState = (String)busObjMap.get("to["+ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION+"].from."+DomainConstants.SELECT_CURRENT);
                   //The state is current state of the Product Variant is compare to the Actual name of the Release state.
                   if (!strState.equalsIgnoreCase(ProductLineConstants.STATE_RELEASE))
                   {
                       //Alert message is formulated to display the error message
                       String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.ProductVariantsCheckFailed",context.getSession().getLanguage());
                       //Explicit alert message is thrown to the front end.
                       emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
                       //Non zero value is returned to the trigger manager to denote failure.
                       return 1;
                   }
          }
           //Return 0 is validation is passed
           return 0;
       }

       
       
              
       
  	  /**
  	   * Used As Wrapper method for expandLogicalStructureForProductVariant method
  	   * It is used as an Expand Program Function for the structure browser expand of Logical Features
  	   *
  	   * @param context
  	   *            the eMatrix <code>Context</code> object
  	   * @param args
  	   *            holds arguments
  	   * @return MapList of all feature objects
  	   * @throws Exception
  	   *             if operation fails
  	   * @since R210
  	   */
  	@com.matrixone.apps.framework.ui.ProgramCallable
  	public MapList expandLogicalStructureForProductVariant(Context context, String[] args)
  	          throws Exception {
  	  	
  		MapList mapList = null;
  		int level = 1;
  		int limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(context, "emxConfiguration.ExpandLimit"));
  		String strObjWhere = DomainObject.EMPTY_STRING;
  		  		
  		try{			
  		  	Map programMap = (Map) JPO.unpackArgs(args);
  		    String tableName = (String) programMap.get("table");
  		  	String strObjectId = (String) programMap.get("objectId");
  		  	String strparentOID = (String) programMap.get("parentOID");
  		    String parentId = (String) programMap.get("parentId");
  		  	
  		  	String strIsStructureCompare = (String) programMap.get("IsStructureCompare");
  		  	
  		    String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");
  		  	
			if(tableName!=null && "FTRLogicalFeatureTable".equals(tableName) && !strparentOID.equals(strObjectId)){
				String strType=new DomainObject(strObjectId).getInfo(context, DomainObject.SELECT_TYPE);
				String parentIdType="";
				if(ProductLineCommon.isNotNull(parentId))
					parentIdType=new DomainObject(parentId).getInfo(context, DomainObject.SELECT_TYPE);
				if(mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_PRODUCT_VARIANT))
					strparentOID = strObjectId;
				else if(ProductLineCommon.isNotNull(parentIdType) && mxType.isOfParentType(context, parentIdType, ConfigurationConstants.TYPE_PRODUCT_VARIANT))
					strparentOID=parentId;
				
			}else{
				StringTokenizer objIDs = new StringTokenizer(strObjectId, ",");
				if (objIDs.countTokens() > 1) {
					// Context Feature ID
					objIDs.nextToken();
					// Context Product ID
					strparentOID = objIDs.nextToken().trim();
				}else{
					strparentOID = (String)programMap.get("parentOID");		

					if(strparentOID != null){
						StringTokenizer parentIDs = new StringTokenizer(strparentOID, ",");
						if (parentIDs.countTokens() > 1) {
							parentIDs.nextToken();
							// Context Product ID
							strparentOID = parentIDs.nextToken().trim();
						}
					}
				}
				if(strparentOID == null)
				{
					strparentOID = strObjectId;
				}
			}
  		  	
			if(strIsStructureCompare!=null && strIsStructureCompare.equalsIgnoreCase("true")){
				strObjectId = (String)programMap.get("objectId");
			    strparentOID = strObjectId;
			}
			
  		  	DomainObject pvObj = new DomainObject(strObjectId);
  			String strType = pvObj.getInfo(context, DomainObject.SELECT_TYPE);
  		  	String strCtxtPrdId = (String) pvObj.getInfo(context, "to["+ ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION+ "].from.id");
  		  	String strExpandLevel = (String) programMap.get("expandLevel");
  		  	String strlevel = (String) programMap.get("Expand Level");
  		  	
  		  	String sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterCommand");
  		                                                          
  			String sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterCommand");
		  	boolean isCalledFromRule=false;
			if(sNameFilterValue==null){
		  		sNameFilterValue = (String) programMap.get("FTRLogicalFeatureNameFilterForRuleDialog");
		  		if(sNameFilterValue!=null) isCalledFromRule= true;
		  	}
            if(sLimitFilterValue==null)
            	sLimitFilterValue = (String) programMap.get("FTRLogicalFeatureLimitFilterForRuleDialog");
  			if(strCtxtPrdId== null){
  				strCtxtPrdId = strObjectId;

  			}

  			if (sLimitFilterValue != null
  					&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
  				if (sLimitFilterValue.length() > 0) {
  					limit = (short) Integer.parseInt(sLimitFilterValue);	
  					if (limit < 0) {
  						limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(context, "emxConfiguration.ExpandLimit"));
  					}
  				}
  			}
  			
  			if (sNameFilterValue != null
  					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {
  				
  				strObjWhere = "attribute["
  					+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
  					+ "] ~~ '" + sNameFilterValue + "'";
  			}
  		    //if this is called from Rule, then add object where, to prevent invalid state object being seen in Rule context Tree
			if(isCalledFromRule){
				if(!strObjWhere.trim().isEmpty())
					strObjWhere=strObjWhere+" && "+RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);
				else
					strObjWhere=RuleProcess.getObjectWhereForRuleTreeExpand(context,ConfigurationConstants.TYPE_LOGICAL_FEATURE);	
			}
  			if (strlevel!=null  && strlevel.equalsIgnoreCase("All"))
  			  	level = 0;  				
  			else if(strExpandLevel!=null && strExpandLevel.equalsIgnoreCase("All"))
  				level = 0;  				
  			else{
  				if(strExpandLevel != null || "".equalsIgnoreCase(strExpandLevel) || "null".equalsIgnoreCase(strExpandLevel))
  	  		  		level = Integer.parseInt(strExpandLevel);
  			}
  		  	
  			String strTypePattern = ConfigurationConstants.TYPE_LOGICAL_STRUCTURES + "," + ConfigurationConstants.TYPE_PRODUCTS;
            String strRelPattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
            StringList slObjSelects = new StringList();
            slObjSelects.addElement(ConfigurationConstants.SELECT_ID);
            slObjSelects.addElement(ConfigurationConstants.SELECT_TYPE);
            slObjSelects.addElement(ConfigurationConstants.SELECT_NAME);
            slObjSelects.addElement(ConfigurationConstants.SELECT_REVISION);
            StringList slRelSelects = new StringList();
            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_TYPE);
            slRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);
            slRelSelects.addElement("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
            slRelSelects.addElement("tomid["+ ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].attribute["+
					ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
            slRelSelects.addElement(SELECT_FROM_NAME);
            slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.id");
            slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.type");
            slRelSelects.add("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.attribute["
					+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
					+ "]");
            slRelSelects.add("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.attribute["
					+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
					+ "]");


            
            ConfigurationUtil utilObj = new ConfigurationUtil(strCtxtPrdId);
            
            String strRelWhere = "tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id=="+ strparentOID;
            
            
            if(strIsStructureCompare!=null && strIsStructureCompare.equalsIgnoreCase("true")
            		&& (mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_LOGICAL_FEATURE))
            		 ||(mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_PRODUCTS) 
            			&& !mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_PRODUCT_VARIANT))){
            	mapList = new MapList();
            	if(!mxType.isOfParentType(context, strType, ConfigurationConstants.TYPE_PRODUCTS)){
                mapList = utilObj.getObjectStructure(context, strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
            			true, level, limit, null,null,(short)0,	filterExpression);
            	}
            }else{
                mapList = utilObj.getObjectStructure(context, strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
            			true, level, limit, strObjWhere, strRelWhere, (short)0,	filterExpression);
            }
            
			for (int i = 0; i < mapList.size(); i++) {			
				Map tempMAp = (Map) mapList.get(i);
				if(tempMAp.containsKey("expandMultiLevelsJPO")){
					mapList.remove(i);
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
			// fetching EBOM related data
			if (mxType.isOfParentType(context, strType,ConfigurationConstants.TYPE_PART)) {
				MapList partRelatedObjects = (MapList) LogicalFeature.getEBOMsWithRelSelectables(context, args);
				HashMap hmTemp = new HashMap();
				hmTemp.put("expandMultiLevelsJPO", "true");
				partRelatedObjects.add(hmTemp);
				return partRelatedObjects;
			}
            
  			if (mapList != null) {
  				HashMap hmTemp = new HashMap();
  				hmTemp.put("expandMultiLevelsJPO", "true");
  				mapList.add(hmTemp);
  			}
  			
  		}catch (Exception e) {
  			throw new FrameworkException(e.getMessage());
  		}
  	  	return mapList;
  	  }
  	
	  /**
	   * Used As Wrapper method for getPFLUsageRange method
	   * It is used to get the Usage attribute value ranges on PFL connetion  	   *
	   * @param context
	   *            the eMatrix <code>Context</code> object
	   * @param args
	   *            holds arguments
	   * @return MapHash of all feature objects
	   * @throws Exception
	   *             if operation fails
	   * @since R211
	   */
  	

    public HashMap getPFLUsageRange(Context context, String[] args)
            throws Exception {
        String languageStr = context.getSession().getLanguage();

        
        StringList strChoicesDisp = new StringList(2);
        String strFeatureUsageStandard = "emxFramework.Range.FeatureUsage.Standard";
        String strFeatureUsageOptional = "emxFramework.Range.FeatureUsage.Optional";
        String strFeatureUsageRequired = "emxConfiguration.Range.FeatureUsage.Required";

        String  strFeatureUsageStandardActual = ConfigurationConstants.RANGE_VALUE_STANDARD;
        String  strFeatureUsageOptionActual = ConfigurationConstants.RANGE_VALUE_OPTIONAL;
        String  strFeatureUsageOptionRequired = ConfigurationConstants.RANGE_VALUE_REQUIRED;
        
        String strFeatureUsageStandardDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                       strFeatureUsageStandard,languageStr);
        String strFeatureUsageOptionalDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                strFeatureUsageOptional,languageStr);
        String strFeatureUsageRequiredDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                strFeatureUsageRequired,languageStr);
        strChoicesDisp.add(strFeatureUsageStandardDisplay);
        strChoicesDisp.add(strFeatureUsageOptionalDisplay);
        strChoicesDisp.add(strFeatureUsageRequiredDisplay);
        // combobox actual values
        StringList strChoices = new StringList(2);
        //strChoices.add(strFeatureUsageMandatoryDisplay);
        strChoices.add(strFeatureUsageStandardActual);
        strChoices.add(strFeatureUsageOptionActual);
        strChoices.add(strFeatureUsageOptionRequired);
        // combobox actual values
        HashMap returnMap = new HashMap();
        returnMap.put("field_choices", strChoices);
        returnMap.put("field_display_choices", strChoicesDisp);

        return returnMap;
    }
    
	  /**
	   * Used As Wrapper method for getPFLUsage method
	   * It is used to get the Usage attribute value on PFL connetion  	   *
	   * @param context
	   *            the eMatrix <code>Context</code> object
	   * @param args
	   *            holds arguments
	   * @return List of all feature objects
	   * @throws Exception
	   *             if operation fails
	   * @since R211
	   */
    	
    
    public List getPFLUsage(Context context, String[] args)
    throws Exception {
    	matrix.util.List PFLusageList = new StringList();
    	try{			
  		  	Map programMap = (Map) JPO.unpackArgs(args);
  		  	HashMap ParamList  = (HashMap)programMap.get("paramList");
  		  	MapList ObjectList  = (MapList)programMap.get("objectList");
  		    	String languageStr = context.getSession().getLanguage();
    		String strUsageStandard = "emxFramework.Range.FeatureUsage.Standard";
    		String strUsageOptional = "emxFramework.Range.FeatureUsage.Optional";
    		String strUsageRequired = "emxConfiguration.Range.FeatureUsage.Required";

    		
    		String strCntxtProductId = ParamList.get("parentOID").toString();
    		
        		String strFeatureUsageStandardDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
    				strUsageStandard,languageStr);
        		String strFeatureUsageOptionalDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
    				strUsageOptional,languageStr);
        		String strFeatureUsageRequiredDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
        			strUsageRequired,languageStr);
    		
    		ConfigurationUtil util = new ConfigurationUtil();
    		String strPRDSelect="tomid["+
					ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].from.id";
    		String strFATSelect="tomid["+ ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].attribute["+
					ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]";
    		StringList selectables = new StringList(strPRDSelect);
    		selectables.add(strFATSelect);
  		  	for (int i = 0; i < ObjectList.size(); i++) {
    			Map mpLF = (Map) ObjectList.get(i);
    			//StringList slProductIds = util.convertObjToStringList(context, mpLF.get("tomid["+
    			//		ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].from.id"));
    			//StringList slDesignUsage = util.convertObjToStringList(context, mpLF.get("tomid["+ ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].attribute["+
    			//		ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]"));
    			StringList slProductIds= new StringList();
    			StringList slDesignUsage= new StringList();
    			if(mpLF.containsKey(strPRDSelect) &&mpLF.get(strPRDSelect)!=null &&  mpLF.containsKey(strFATSelect)&& mpLF.get(strFATSelect)!=null){
        			 slProductIds = util.convertObjToStringList(context, mpLF.get(strPRDSelect));
        			 slDesignUsage = util.convertObjToStringList(context, mpLF.get(strFATSelect));
    			}else if(mpLF
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) mpLF
										.get(DomainRelationship.SELECT_ID))){
    				DomainConstants.MULTI_VALUE_LIST.add(strPRDSelect);
    				DomainConstants.MULTI_VALUE_LIST.add(strFATSelect);

    				MapList mlUsedResult = DomainRelationship.getInfo(context, new String[]{(String) mpLF
    						.get(DomainRelationship.SELECT_ID)}, selectables);
    				DomainConstants.MULTI_VALUE_LIST.remove(strPRDSelect);    				
    				DomainConstants.MULTI_VALUE_LIST.remove(strFATSelect);
    				slProductIds = util.convertObjToStringList(context,((Map)mlUsedResult.get(0)).get(strPRDSelect));
    				slDesignUsage = util.convertObjToStringList(context,((Map)mlUsedResult.get(0)).get(strFATSelect));    				
    			}
    			String strRootNode = (String)mpLF.get("Root Node");
    			if(strRootNode!=null && strRootNode.equalsIgnoreCase("True")){
    				PFLusageList.add(DomainConstants.EMPTY_STRING);
    			}else{
	    			if(slDesignUsage!=null && slDesignUsage.size()>0){
	    				
	    				String strUsage = (String)slDesignUsage.get(slProductIds.indexOf(strCntxtProductId));
	    				
	    				
    					if(strUsage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_STANDARD))
    					{
    						PFLusageList.add(strFeatureUsageStandardDisplay);
    					}else if(strUsage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REQUIRED))
    					{
    						PFLusageList.add(strFeatureUsageRequiredDisplay);
    					}
    					else
    					{
    						PFLusageList.add(strFeatureUsageOptionalDisplay);
    					}
	    			}
	    			else{
	    				PFLusageList.add(DomainConstants.EMPTY_STRING);
	    			}
	    		}
    		 }
     	}
    	catch (Exception e) {
    		throw new FrameworkException(e.getMessage());
		}
    	return PFLusageList;
    }
    
	  /**
	   * Used As Wrapper method for updatePFLUsage method
	   * It is used to update the Usage attribute value on PFL connetion  	   *
	   * @param context
	   *            the eMatrix <code>Context</code> object
	   * @param args
	   *            holds arguments
	   * @return void
	   * @throws Exception
	   *             if operation fails
	   * @since R211
	   */

    
    public void updatePFLUsage(Context context, String[] args)
    throws Exception {
    	
    	String strFeatureAllocationType = ProductLineConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE;

    	String value ="";
    	try{			
  		  	Map programMap = (Map) JPO.unpackArgs(args);
  		  	//HashMap ParamList  = (HashMap)programMap.get("paramList");
  		  	//MapList ObjectList  = (MapList)programMap.get("objectList");
  		  	HashMap requestMap = (HashMap)programMap.get("requestMap");
  		  	HashMap paramMap  = (HashMap)programMap.get("paramMap");
  		  	String strLFRelId = (String)paramMap.get("relId");
  		  	String strProductId = (String) requestMap.get("parentOID");
  		  	value = (String)paramMap.get("New Value");
  		  	
  		  	// queryconnection R418- Replaced Query connection with GetInfo
//  		  	// TODO need to change when BPS provides the ObjectList in the Update Functions
//			String strPFLRelPattern = ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST;
//  		  	
//			StringList strSelect = ConfigurationUtil.getBasicRelSelects(context);
//			strSelect.add("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
//			
//			StringBuffer sBufWhereCond = new StringBuffer(200);
//			sBufWhereCond.append("from.id==");
//			sBufWhereCond.append(strProductId);
//			sBufWhereCond.append("&&");
//			sBufWhereCond.append("torel.id==");
//			sBufWhereCond.append(strLFRelId);
//			
//			
//			MapList mLstPFLDetails = ProductLineCommon.queryConnection(context, 
//					strPFLRelPattern,strSelect, sBufWhereCond.toString());
  		  	//----
			StringList strSelect1 = ConfigurationUtil.getBasicRelSelects(context);
			strSelect1.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id");
			strSelect1.addElement("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id");
			
			DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id");
			DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id");
			
			DomainObject domProduct = DomainObject.newInstance(context,strProductId);
			Map mPFLDetails = domProduct.getInfo(context, strSelect1);
			
			StringList featureList =  (StringList) mPFLDetails.get("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id");
			StringList pflList =  (StringList) mPFLDetails.get("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id");
			
			DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.id");
			DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id");

			
			for(int j=0; j<featureList.size();j++){
				String strPFLLFId = (String) featureList.get(j);
				if(strLFRelId.equals(strPFLLFId)){
					String strPFLId = (String) pflList.get(j);
					DomainRelationship domRel = new DomainRelationship(strPFLId);
					domRel.setAttributeValue(context,strFeatureAllocationType, value);
					break;
				}
			}
			
			//----
//			if(mLstPFLDetails.size()!=0){
//				Map mapPFLDetails = (Map)mLstPFLDetails.get(0);
//				String strPFLId = mapPFLDetails.get(DomainRelationship.SELECT_ID).toString();
//				DomainRelationship domRel = new DomainRelationship(strPFLId);
//				domRel.setAttributeValue(context,strFeatureAllocationType, value);
//			}
  		  	
  		  	/* 		  	
              variantId = (String) ParamList.get("objectId");
  		  	
  		  	for (int i = 0; i < ObjectList.size(); i++) {
                Map mp = (Map) ObjectList.get(i);
    			usage = (Object) mp.get("to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"]"+
	    		 			".tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +
    		 			"].attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
    			if(usage instanceof String){
    				usageList.add(usage.toString());

    			}else if(usage instanceof StringList){
    				usageList= (StringList)usage;
    			}
    			String[] UsageArry = (String[]) usageList.toArray(new String[0]);

    			//PFL ids
    			PFLids = (Object)mp.get("to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"]"+
    					".tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +
    					"]."+DomainConstants.SELECT_ID);
    			if(usage instanceof String){
    				strPLFId.add(PFLids.toString());

    			}else if(usage instanceof StringList){
    				strPLFId= (StringList)(PFLids);
    			}

    			String[] PLFidArry = (String[]) strPLFId.toArray(new String[0]);
    			StringList Selectables = new StringList();
    			Selectables.add(DomainConstants.SELECT_FROM_ID);
	
    			DomainRelationship domainrelation = new DomainRelationship();
    			MapList ml = domainrelation.getInfo(context, PLFidArry, Selectables);
    			
    			for (int count=0; count<ml.size(); count++)
    			{	
    				Map nmp = (Map)ml.get(count);
    				String connectedid = (String)nmp.get(DomainConstants.SELECT_FROM_ID);
    				
    				if (connectedid.equalsIgnoreCase(variantId))
    				{
    					String PFLid = (String)PLFidArry[count];
    					domainrelation.setAttributeValue(context, PFLid, strFeatureAllocationType, value);
    				}
    			}
                
                
    		  	}
  
  */  	}
    	catch (Exception e) {
    		throw new FrameworkException(e.getMessage());
		}
   
    }
    
    /**
     * This method is used to show all the Product Variant
     * This is called from getProductVariantsSelectedFeatures method
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the FormBean contents
     * @return List
     * @throws Exception if the operation fails
     * @since R211
     */
    public List getDynamicColumnPFLUsageForView(Context context, String[] args)
    throws Exception {
    	matrix.util.List PFLusageList = new StringList();
    	Object PFLids;
		StringList strPLFId = new StringList();
		String variantId ="";
    	try{			
  		  	Map programMap = (Map) JPO.unpackArgs(args);
  		  	HashMap ParamList  = (HashMap)programMap.get("paramList");
  		  	MapList ObjectList  = (MapList)programMap.get("objectList");
  		    HashMap columnMap  = (HashMap)programMap.get("columnMap");
  		    
  		   variantId = (String) columnMap.get("id");
  		   if(variantId == null || "".equalsIgnoreCase(variantId) || "null".equalsIgnoreCase(variantId))
  		   {
  			 variantId = (String) ParamList.get("objectId");
  		   }
			
  		  	for (int i = 0; i < ObjectList.size(); i++) {
  		  		boolean flag = true;
                Map mp = (Map) ObjectList.get(i);
       			//PFL ids
    			PFLids = (Object)mp.get("to["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES +"]"+
    					".tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +
    					"]."+DomainConstants.SELECT_ID);
    			if(PFLids instanceof String){
    				strPLFId.add(PFLids.toString());

    			}else if(PFLids instanceof StringList){
    				strPLFId= (StringList)(PFLids);
    			}
    			//get PFL Ids of a single Logical Feature
    			String[] PLFidArry = (String[]) strPLFId.toArray(new String[0]);
    			StringList Selectables = new StringList();
    			Selectables.add(DomainConstants.SELECT_FROM_ID);
    			//returns Products and all product Variants connected to single Logical feature
    			DomainRelationship domainobject = new DomainRelationship();
    			MapList ml = domainobject.getInfo(context, PLFidArry, Selectables);
    			
    			for (int count=0; count<ml.size(); count++)
    			{	
    				Map nmp = (Map)ml.get(count);
    				String connectedid = (String)nmp.get(DomainConstants.SELECT_FROM_ID);
    				//when the from side object id and Variant ids are equal pick up the usage value.
    				if (connectedid.equalsIgnoreCase(variantId))
    				{
    					PFLusageList.add("<center><img border=\'0\' src=\'../common/images/utilWorkflowApproved.gif\'/></center>");
    					flag = false;
    				}
    				
    			}
    			if(flag)
    			{
    				PFLusageList.add("");
    			}
                
        }

  
    	}
    	catch (Exception e) {
    		throw new FrameworkException(e.getMessage());
		}
    	return PFLusageList;
    }
    /**
     * This method is used to make the cell value editable/non editable
     * depending upon connections.
     * If PFL connection is present then its editable else its non editable.
     * @param context
     *            The ematrix context object.
     * @param String[]
     *            The args .
     * @return List.
     * @since R211
     * @throws Exception
     */
    public static List isCellValueEditable(Context context, String[] args )throws Exception{

        try{
        	matrix.util.List PFLusageList = new StringList();
        HashMap inputMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList = (MapList) inputMap.get("objectList");
        
        HashMap colMap = (HashMap)inputMap.get("columnMap");
        String strProdRevId = (String) colMap.get("id");

        PFLusageList.add(Boolean.valueOf(false));
        ConfigurationUtil confUtil = new ConfigurationUtil();
        for (int i = 1; i < objectList.size(); i++)
        {
        	boolean flag = true;
        	Map featMap = (Map)objectList.get(i);
           	StringList sl =confUtil.convertObjToStringList(context,featMap.get("tomid["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id"));
           	String[] PFLidArry = (String[]) sl.toArray(new String[0]);
           	
  			
			for (int count=0; count<PFLidArry.length; count++)
			{	
				
				String connectedid = (String)PFLidArry[count];
				//when the from side object id and Variant ids are equal pick up the usage value.
				if (connectedid.equalsIgnoreCase(strProdRevId))
				{
					PFLusageList.add(Boolean.valueOf(true));
					flag = false;
				}
			}
			if(flag)
			{
				PFLusageList.add(Boolean.valueOf(false));
			}
            
		  	}
        
        return PFLusageList;
        }catch(Exception e) {
            e.printStackTrace();
            throw new FrameworkException(e.getMessage());
        }

    }
    
    public List getPFLUsageofProductVariant(Context context, String[] args)
    throws Exception {
    	matrix.util.List PFLusageList = new StringList();
    	try{			
  		  	Map programMap = (Map) JPO.unpackArgs(args);
  		  	MapList ObjectList  = (MapList)programMap.get("objectList");
  		    HashMap columnMap  = (HashMap)programMap.get("columnMap");
    		String languageStr = context.getSession().getLanguage();
    		String strUsageStandard = "emxFramework.Range.FeatureUsage.Standard";
    		String strUsageOptional = "emxFramework.Range.FeatureUsage.Optional";
    		String strUsageRequired = "emxConfiguration.Range.FeatureUsage.Required";

    		String strFeatureUsageStandardDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
				strUsageStandard,languageStr);
    		String strFeatureUsageOptionalDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
				strUsageOptional,languageStr);
    		String strFeatureUsageRequiredDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
    			strUsageRequired,languageStr);
    		//columnID
    		String columnID=(String)columnMap.get("id");
    		ConfigurationUtil util = new ConfigurationUtil();
  		  	for (int i = 0; i < ObjectList.size(); i++) {
  		  		Map mpLF = (Map) ObjectList.get(i);
    			StringList slProductIds = util.convertObjToStringList(context, mpLF.get("tomid["+
    					ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].from.id"));
    			int columIndex=slProductIds.indexOf(columnID);
    			StringList slDesignUsage = util.convertObjToStringList(context, mpLF.get("tomid["+ ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].attribute["+
    					ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]"));

    			String strRootNode = (String)mpLF.get("Root Node");
    			
    			if(strRootNode!=null && strRootNode.equalsIgnoreCase("True")){
        			PFLusageList.add(DomainConstants.EMPTY_STRING);
    			}else{
	    			if(columIndex > -1){
	    				String Usage = (String)slDesignUsage.get(columIndex);
						if(Usage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_STANDARD))
						{
							PFLusageList.add(strFeatureUsageStandardDisplay);
						}else if(Usage.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_REQUIRED))
						{
							PFLusageList.add(strFeatureUsageRequiredDisplay);
						}
						else
						{
							PFLusageList.add(strFeatureUsageOptionalDisplay);
						}
	    			}else{
	    				PFLusageList.add(DomainConstants.EMPTY_STRING);
	    			}
    			}
    		 }
     	}
    	catch (Exception e) {
    		throw new FrameworkException(e.getMessage());
		}
    	return PFLusageList;
    }
    
    /**
	 * Label program for  Tree structure
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
     * @since R213
     */
    public String getDisplayNameForNavigator(Context context,String[] args) throws Exception
    {	 		
		String strTreeName = ProductLineUtil.getDisplayNameForFeatureNavigator(context, args);

		return strTreeName;
	 }
    /**
	 *  gets rows to view productVariant grid and pass it on to GridComponent
	 * View product Variants
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
    public MapList getLogicalStrutcureForProductVariantGrid(Context context, String[] args)
	throws Exception {

		MapList mapLogicalStructure =null;
		try{
			  String cellIdSelect ="tomid["+
				ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].from.id";
	    	  String cellvalueSelect ="tomid["+ ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].attribute["+
				ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]";
	    	  
			mapLogicalStructure =(MapList)JPO.invoke(context, "LogicalFeatureBase", args,
					"getLogicalFeatureStructure", args,
					MapList.class);
			
			Map tempmap;
			for (int i = 0; i < mapLogicalStructure.size(); i++) {
				tempmap =(Map)mapLogicalStructure.get(i);
				
				tempmap.put(UITableGrid.KEY_ROW_ID, tempmap.get(DomainConstants.SELECT_ID));
				tempmap.put(UITableGrid.KEY_ROW_REL_ID, tempmap.get(DomainConstants.SELECT_RELATIONSHIP_ID));
				if(tempmap.get(cellIdSelect) !=null){
					tempmap.put(UITableGrid.KEY_CELL_ID,tempmap.get(cellIdSelect));//cell id and column id should match
					tempmap.put(UITableGrid.KEY_CELL_VALUE,tempmap.get(cellvalueSelect));
				}
				
				
			}
		}catch (Exception e) {
			  throw new FrameworkException(e.getMessage());
		  }
		return mapLogicalStructure;
    }
    /**
	 *  gets Column to view product Variant grid and pass it on to GridComponent
	 *  Viewe Product Variants
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
    public MapList getProductVariantForGrid(Context context, String[] args)throws FrameworkException {
  	  MapList colList = new MapList();
  	    try{
      	StringList strListSelectstmts = new StringList();
    		strListSelectstmts.add(DomainObject.SELECT_ID);
    		strListSelectstmts.add(DomainObject.SELECT_NAME);
    		strListSelectstmts.add(DomainObject.SELECT_REVISION);
    		strListSelectstmts.add("attribute["+ConfigurationConstants.VARIANT_NAME+"]");
    		HashMap hash  = (HashMap) JPO.unpackArgs(args);
  		HashMap paramList = (HashMap) hash.get("requestMap");
  		
  		String strSelIdsWhrClause = "";
          String strSelectId = (String) paramList.get("selectId");	
  	    if(strSelectId != null && !"".equalsIgnoreCase(strSelectId)){
  	    	StringList slSelectMPIds = new StringList();
  	        StringTokenizer strSelectTok = new StringTokenizer(strSelectId, ",");
  	        String strSelectIdKey = null;	        
  	        StringBuffer sbWhereCondition1 = new StringBuffer(25);
  	        sbWhereCondition1 = sbWhereCondition1.append("id==");
  	        while(strSelectTok.hasMoreTokens())
  	        {
  	        	strSelectIdKey = strSelectTok.nextToken();
  	            sbWhereCondition1 = sbWhereCondition1.append(strSelectIdKey);
  	            sbWhereCondition1 = sbWhereCondition1.append("|| id==");
  	            if(strSelectIdKey != null && !"".equalsIgnoreCase(strSelectIdKey))
  	            {
  	            	slSelectMPIds.add(strSelectIdKey);
  	            }   
  	        }
  	        strSelIdsWhrClause = sbWhereCondition1.toString();
  	        strSelIdsWhrClause = strSelIdsWhrClause.substring(0, strSelIdsWhrClause.lastIndexOf("||"));
  	    }
    		
    		String stprObjectId = (String)paramList.get("objectId");
  		
  		
  			colList = new DomainObject(stprObjectId).getRelatedObjects(context, 
  					 ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION, 
  					ConfigurationConstants.TYPE_PRODUCT_VARIANT,
  					strListSelectstmts,
  					new StringList(),
  					false,
  					true,
  					(short)0,strSelIdsWhrClause,"",0);
  		
  		Map tempmap;
  		for (int i = 0; i < colList.size(); i++) {
  			tempmap =(Map)colList.get(i);
  			
  			tempmap.put(UITableGrid.KEY_COL_ID, tempmap.get(DomainConstants.SELECT_ID));
  			tempmap.put(UITableGrid.KEY_COL_VALUE,tempmap.get("attribute["+ConfigurationConstants.VARIANT_NAME+"]"));
  			tempmap.put(UITableGrid.KEY_COL_GROUP_VALUE,ConfigurationConstants.TYPE_PRODUCT_VARIANT);
  		}
  		
  		
        }catch (Exception e) {
  		  throw new FrameworkException(e.getMessage());
  	  }
        return colList;
    }
}
