/*
 *  emxModelBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.3.2.4.1.1.1.3.1.2 Fri Jan 16 14:49:59 2009 GMT ds-shbehera Experimental$
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.DerivationUtil;
import com.matrixone.apps.productline.Model;
import com.matrixone.apps.productline.Product;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;




/**
 * This JPO class has some methods pertaining to Model type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxModelBase_mxJPO extends emxDomainObject_mxJPO {

    /** Alias used for Comma Character. */
    protected static final String STR_COMMA =   ",";

    /**

    	/**
    	* Alias used for the wild character(*).
    	*/
    	    protected static final String WILD_CHAR = "*";
    	    protected static final String SUITE_KEY = "ProductLine";


    /**
     * Create a new emxModel object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public emxModelBase_mxJPO (Context context, String[] args) throws Exception
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
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String language = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxProduct.Alert.FeaturesCheckFailed",language);
            throw  new Exception(strContentLabel);
        }
        return  0;
    }

 /**
     * Get the list of all Models on the context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Maplist containing the object ids of models
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getModels (Context context, String[] args) throws Exception {

        MapList relBusObjPageList = new MapList();
        StringList objectSelects = new StringList("id");
        StringList relSelects = new StringList("id[connection]");
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        setId(strObjectId);
        short sRecursionLevel = 1;
        String strType = ProductLineConstants.TYPE_MODEL;
        String strRelName = ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS;
        //Getting the related Models
        relBusObjPageList = getRelatedObjects(context, strRelName, strType,
                objectSelects, relSelects, false, true, sRecursionLevel, "",
                "");
        return  relBusObjPageList;
    }

    /**
     * Get the list of all CommittedFeatures.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Maplist containing the list of commited feature ids.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCommitedFeatures (Context context, String[] args) throws Exception
    {
        MapList mpCommittedFeatures = new MapList();
        String sObjectType = DomainConstants.EMPTY_STRING;
        String sRelationshipName = DomainConstants.EMPTY_STRING;
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    	objectSelects.add(DomainConstants.SELECT_TYPE);
    	objectSelects.add(DomainConstants.SELECT_REVISION);
    	    	objectSelects.add("to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from" +
    			".to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.name");
    	objectSelects.add("to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from" +
    			".to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.revision");
    	objectSelects.add("to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from" +
    			".to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type");

        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_NAME);
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = new MapList();
        //Gets the objectids and the relation names from args
        String paramMap = (String)programMap.get("objectId");
        //Domain Object initialized with the object id.
        DomainObject dom = new DomainObject(paramMap);
        short sLevel = 0;
        //Gets the relationship name
        StringBuffer sbRelName = new StringBuffer(20);
        sbRelName.append(ProductLineConstants.RELATIONSHIP_COMMITED_ITEM);
        sbRelName.append(",");
        sbRelName.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);



        StringBuffer sbTypeName = new StringBuffer(20);
        sbTypeName.append(ProductLineConstants.TYPE_FEATURE_LIST);
        sbTypeName.append(",");
        sbTypeName.append(ProductLineConstants.TYPE_FEATURES);



        //The getRelatedObjects method is invoked to get the list of Assignees
        relBusObjPageList = dom.getRelatedObjects(context, sbRelName.toString(), sbTypeName.toString(),
                objectSelects, relSelects, false, true, sLevel, "", "",0);

        if(!relBusObjPageList.isEmpty())
        {
        	for(int cn = 0;cn < relBusObjPageList.size(); cn++)
        	{
        		Map tempMap = (Hashtable)relBusObjPageList.get(cn);
        		sObjectType = (String)tempMap.get(DomainConstants.SELECT_TYPE);
        		sRelationshipName = (String)tempMap.get(DomainConstants.SELECT_RELATIONSHIP_NAME);
        		if(mxType.isOfParentType(context, sObjectType, ProductLineConstants.TYPE_FEATURES)
        				&& ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO.equals(sRelationshipName))
        		{
        			mpCommittedFeatures.add(tempMap);
        		}
        	}
        }
        return  mpCommittedFeatures;
    }

    /**
     * Get the list of all CommittedRequirements.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Maplist containing the list of commited requirements ids.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public MapList getCommitedRequirements (Context context, String[] args) throws Exception {
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = new MapList();
        //Gets the objectids and the relation names from args
        String paramMap = (String)programMap.get("objectId");
        //Domain Object initialized with the object id.
        DomainObject dom = new DomainObject(paramMap);
        short sLevel = 1;
        //Gets the relationship name
        String strRelName = ProductLineConstants.RELATIONSHIP_COMMITED_ITEM;
        String strTypeName = ProductLineConstants.TYPE_REQUIREMENT;
        //The getRelatedObjects method is invoked to get the list of Assignees
        relBusObjPageList = dom.getRelatedObjects(context, strRelName, strTypeName,
                objectSelects, relSelects, false, true, sLevel, "", "");
        return  relBusObjPageList;
    }

    /**
     * Method call to get the list of all candidate features.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Holds the following arguments
     *              0 - Hashmap containing the Object Id.
     * @return - Maplist of bus ids of candidate features
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public MapList getCandidateFeatures (Context context, String[] args) throws Exception {
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = new MapList();
        //Gets the objectids and the relation names from args
        String paramMap = (String)programMap.get("objectId");
        //Domain Object initialized with the object id.
        DomainObject dom = new DomainObject(paramMap);
        short sLevel = 1;
        //Gets the relationship name
        String strRelName = ProductLineConstants.RELATIONSHIP_CANDIDATE_ITEM;
        String strTypeName = ProductLineConstants.TYPE_FEATURES;
        relBusObjPageList = dom.getRelatedObjects(context, strRelName, strTypeName,
                objectSelects, relSelects, false, true, sLevel, "", "");
        if (!(relBusObjPageList != null))
            throw  new Exception("Error!!! Context does not have any Objects.");
        return  relBusObjPageList;
    }

    /**
     * Method call to get the list of all candidate Requirements.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Holds parameters passed from the calling method
     * @return - Maplist of bus ids of candidate Requirements
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCandidateRequirements (Context context, String[] args) throws Exception {
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = new MapList();
        //Gets the objectids and the relation names from args
        String paramMap = (String)programMap.get("objectId");
        //Domain Object initialized with the object id.
        DomainObject dom = new DomainObject(paramMap);
        short sLevel = 1;
        //Gets the relationship name
        String strRelName = ProductLineConstants.RELATIONSHIP_CANDIDATE_ITEM;
        String strTypeName = ProductLineConstants.TYPE_REQUIREMENT;
        relBusObjPageList = dom.getRelatedObjects(context, strRelName, strTypeName,
                objectSelects, relSelects, false, true, sLevel, "", "");
        if (!(relBusObjPageList != null))
            throw  new Exception("Error!!! Context does not have any Objects.");
        return  relBusObjPageList;
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
    public MapList getRelatedProducts (Context context, String[] args) throws Exception {
		StringList objectSelects = new StringList("physicalid");
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
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
                objectSelects, relSelects, false, true, sLevel, "", "");
        for(int i=0;i<relBusObjPageList.size();i++){
        	Map temMap=(Map)relBusObjPageList.get(i);
        	Object obj = temMap.remove("physicalid");
        	temMap.put(DomainConstants.SELECT_ID, obj);
        }
        return  relBusObjPageList;
    }

    //Begin of add by Enovia MatrixOne for bug #301382, 03/31/2005

    /**
    * This method is used to create a Model.
    * This method is invoked from the Bean.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the FormBean contents
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */

    public String createModel(Context context, String args[]) throws Exception
    {
        ArrayList programMap = (ArrayList)JPO.unpackArgs(args);
        String strModelType = (String)programMap.get(0);
        String strModelName = (String)programMap.get(1);
        String strProductLineId = (String)programMap.get(2);
        String strModelDescription = (String)programMap.get(3);
        String strModelOwner = (String)programMap.get(4);
        String strModelMarketingText = (String)programMap.get(5);
        String strModelMarketingName =  (String)programMap.get(6);
        String strProductLineObjId = (String)programMap.get(7);
        String strRelationshipName =  DomainConstants.EMPTY_STRING;
        String strVaultName = (String)programMap.get(8);
        String strRevision = DomainConstants.EMPTY_STRING;
        String strPolicy = (String)programMap.get(9);
        String strModelPrefix = (String)programMap.get(10);
        String [] forProductCreate = new String[2];
        //Instantiating a HashMap for storing attributes
        HashMap attributeMap = new HashMap();
        attributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_TEXT,
                strModelMarketingText);
        attributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_NAME,
                strModelMarketingName);
        attributeMap.put(ProductLineConstants.ATTRIBUTE_PREFIX,
                strModelPrefix);

        String strNewObjId = null;
        //Calling the create method of ProductCentralCommon.java
        ProductLineCommon pcBean = new ProductLineCommon();
        DomainObject domModelObj = null;
        strNewObjId = pcBean.create(context, strModelType, strModelName, strRevision,
                strModelDescription, strPolicy, strVaultName, attributeMap,
                strModelOwner, strProductLineObjId, ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS,
                true);
              domModelObj = newInstance(context, strNewObjId);
        if (!(strProductLineId == null || "".equals(strProductLineId) || "null".equals(strProductLineId))) {
            RelationshipType rtRelObject = new RelationshipType(ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS);
            DomainObject productLineObj = newInstance(context, strProductLineId);
            domModelObj.connect(context, rtRelObject, false, productLineObj);
        }
        Map mpProd = domModelObj.getRelatedObject(
                                    context,
                                    ProductLineConstants.RELATIONSHIP_PRODUCTS,
                                    true,
                                    new StringList(DomainConstants.SELECT_ID),
                                    new StringList(DomainRelationship.SELECT_ID));




        // Begin of modify by Enovia MatrixOne for bug no. 304222, dated 05/11/2005

		if( mpProd != null )
		{
			DomainObject domProductObj = newInstance(context, (String)mpProd.get(DomainConstants.SELECT_ID));
			// Begin of Modify by Enovia MatrixOne for Bug # 301807 Dated 04/04/2005
			attributeMap.remove(ProductLineConstants.ATTRIBUTE_MARKETING_TEXT);
			 //To fix Warning "Business object has no attribute Prefix" - Starts
            attributeMap.remove(ProductLineConstants.ATTRIBUTE_PREFIX);

            //To fix Warning "Business object has no attribute Prefix" - Ends
			attributeMap.put(ProductLineConstants.ATTRIBUTE_ORIGINATOR,
					context.getUser());
			domProductObj.setAttributeValues(context, attributeMap);
            // Begin of modify by Enovia MatrixOne for bug 304670 on 19-May-2005
            domProductObj.setOwner(context,
                                              strModelOwner);
            // End of modify by Enovia MatrixOne for bug 304670 on 19-May-2005
		}
		// End of modify by Enovia MatrixOne for bug no. 304222, dated 05/11/2005

        // End of Modify by Enovia MatrixOne for Bug # 301807 Dated 04/04/2005
        return  (strNewObjId);
    }

    //End of add by Enovia MatrixOne for bug #301382, 03/31/2005

    /**
    * This method is used to create a Product on creation of a new Model.
    * This method is invoked on Trigger Action of Model Creation.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds object id of the model as argument
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    	public void createProductOnCreate(Context context,String args[]) throws Exception {
			String strIPMLCommandName=PropertyUtil.getGlobalRPEValue(context,"IPMLCommandName");
    	    if (strIPMLCommandName == null|| "".equalsIgnoreCase(strIPMLCommandName)|| "null".equalsIgnoreCase(strIPMLCommandName)){

			String strModelId = args[0];
    		String strProductType = args[1];
    		String strCompanyId = null;

    		DomainObject domModelObj = DomainObject.newInstance(context,strModelId);
    		String strModelName = domModelObj.getInfo(context, ProductLineConstants.SELECT_NAME);
    		String vaultPattern = domModelObj.getVault();
    		//Getting the company details of the context
    		StringList companyList = new StringList();
    		if(!context.getUser().equals("creator") && !context.getUser().equals("User Agent")){
    			companyList = ProductLineUtil.getUserCompanyIdName(context);
    		}
    		if(companyList.size() >= 1){
    			strCompanyId = (String)companyList.elementAt(0);
    		}
    		String strCreateProduct=PropertyUtil.getGlobalRPEValue(context,"CreateProductTrigger");
    		PropertyUtil.setGlobalRPEValue(context,"CreateProductTrigger", "FALSE");
    		if(strCreateProduct!=null && !"TRUE".equals(strCreateProduct))
    		{

    			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    			String revPattern = domModelObj.getDefaultRevision(context, ProductLineConstants.POLICY_PRODUCT);

    			MapList mpObjList = DomainObject.findObjects(context,
    					PropertyUtil.getSchemaProperty(context,strProductType),
    					strModelName,
    					"",
    					WILD_CHAR,
    					vaultPattern,
    					null,
    					true,
    					objectSelects);
    			if(mpObjList.size() == 0){
	            	String strReltoConnect = ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT;
    				DomainObject domProductObj = new DomainObject();
    				PropertyUtil.setGlobalRPEValue(context,"CreateModelTrigger", "TRUE");
    				DomainRelationship domRel = domProductObj.createAndConnect(
    						context,
    						PropertyUtil.getSchemaProperty(context,strProductType),
    						strModelName,
    						null,
    						null,
    						null,
		    				strReltoConnect,
    						domModelObj,
    						true);

    				BusinessObject busObj = domRel.getTo();
    				String strProdId = busObj.getObjectId();
    				domProductObj.setId(strProdId);

		    		// Create the node for the Root product and set the attributes.
    				String strType = domProductObj.getInfo(context, DomainConstants.SELECT_TYPE);
			    	HashMap attributeMap = DerivationUtil.createDerivedNode (context, null, DerivationUtil.DERIVATION_LEVEL0, strType);
			    	if (attributeMap != null && !attributeMap.isEmpty()) {
			    		domProductObj.setAttributeValues(context, attributeMap);
			    	}

    				if(strCompanyId!=null){
    					RelationshipType relType = new RelationshipType(
    							ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT);
    					domRel = domProductObj.addFromObject(context, relType, strCompanyId);
    				}
    			}
    		}

			}
   		}

     /**
     *Method to add Candidate Requirements to a Model.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Holds the following parameters
                emxTableRowId - table row id
                parentOID - parent object id
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    public void addExistingCandidateRequirements  (Context context, String[] args) throws Exception {
        try {
          //Start of write transaction
          ContextUtil.startTransaction(context, true);
          //Unpacking the args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          //Gets the objectList from args
          HashMap reqMap = (HashMap)programMap.get("reqMap");
          //Get all TableRowIds
          String[] strNewObjIdArr = (String[])reqMap.get("emxTableRowId");
          //Get the parent ID
          String[] strParentOIdArr = (String[])reqMap.get("parentOID");
          String strParentOId = strParentOIdArr[0];
          //String is initialized to store the value of relationship name
          String strRelationshipName = ProductLineConstants.RELATIONSHIP_CANDIDATE_ITEM;
          //String is initialized to store the value of ObjectID
          String strNewObjId = "";
          for (int a = 0; a < strNewObjIdArr.length; a++) {
              //retriving individual ObjectID from the String Array
              strNewObjId = strNewObjIdArr[a];
              DomainObject dom = newInstance(context, strParentOId);
              String strComittedRelationshipName = ProductLineConstants.RELATIONSHIP_COMMITED_ITEM;
              String strType = ProductLineConstants.TYPE_REQUIREMENT;
              StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_ID);
              String strObjectWhere = DomainConstants.SELECT_ID + "==" + strNewObjId;
              short sh = 1;
              MapList relBusObjPageList = dom.getRelatedObjects(context, strComittedRelationshipName,
                strType, ObjectSelectsList, null, true, true, sh, strObjectWhere, "");
              int iNoOfObjects = relBusObjPageList.size();
              if (iNoOfObjects == 1 ) {
                String language = context.getSession().getLanguage();
                String strAlertMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Alert.CandidateRequirementAddExistingAlreadyCommited",language);
                //${CLASS:emxContextUtilBase}.mqlNotice(context, strAlertMessage);
                //Transaction aborted in case of exception
                ContextUtil.abortTransaction(context);
                //break;
                throw new Exception(strAlertMessage);
              }
              //Connecting the objects with a relation
              modelConnect(context, strParentOId, strNewObjId, strRelationshipName);
          }
        //End transaction
        ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            //Transaction aborted in case of exception
            ContextUtil.abortTransaction(context);
            //The exception with appropriate message is thrown to the caller.
            throw  new FrameworkException(e);
        }
    }

     /**
     * Method to add Candidate Features to a Model.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Holds the following parameters
                emxTableRowId - table row id
                parentOID - parent object id
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */

    public void addExistingCandidateFeatures  (Context context, String[] args) throws Exception {
        try {
          //Start of write transaction
          ContextUtil.startTransaction(context, true);
          //Unpacking the args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          //Gets the objectList from args
          HashMap reqMap = (HashMap)programMap.get("reqMap");
          //Get all TableRowIds
          String[] strNewObjIdArr = (String[])reqMap.get("emxTableRowId");
          //Get the parent ID
          String[] strParentOIdArr = (String[])reqMap.get("parentOID");
          String strParentOId = strParentOIdArr[0];
          //String is initialized to store the value of relationship name
          String strRelationshipName = ProductLineConstants.RELATIONSHIP_CANDIDATE_ITEM;
          //String is initialized to store the value of ObjectID
          String strNewObjId = "";
          int iAbort = 0;
          for (int a = 0; a < strNewObjIdArr.length; a++) {
            //retriving individual ObjectID from the String Array
            strNewObjId = strNewObjIdArr[a];
            //String is initialized to store the value of relationship name
            String strRelationshipType = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO
                    + ","
                    + ProductLineConstants.RELATIONSHIP_COMMITED_ITEM;
            //String is initialized to store the value of type name
            //short is initialized to store the value the level till which object will be searched
            //Stringlist for querying is formulated.
            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            //Domain Object is instantiated with parent object id of the image object
            DomainObject dom = newInstance(context,strParentOId);
            //The associated object details are retreived onto a MapList
            MapList relBusObjPageList = dom.getRelatedObjects(
                context,
                strRelationshipType,
                "*",
                objectSelects,
                null,
                false,
                true,
                (short) 2,
                "",
                "");
            int iNoOfObjects = relBusObjPageList.size();
            for (int i = 0; i < iNoOfObjects; i++) {
              String strLevel = (String)((Hashtable)relBusObjPageList.get(i)).get("level");
              if (strLevel.equals("2"))
              {
                String strId = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                if (strId.equals(strNewObjId))
                {
                  String language = context.getSession().getLanguage();
                  String strAlertMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.Alert.CandidateFeatureAddExistingAlreadyCommited",language);
                  //${CLASS:emxContextUtilBase}.mqlNotice(context, strAlertMessage);
                  //Transaction aborted in case of exception
                  ContextUtil.abortTransaction(context);
                  iAbort = 1;
                  //break;
                  throw new Exception(strAlertMessage);
                }
              }
            }
            if (iAbort == 1)
            {
              break;
            }
            //Connecting the objects with a relation
            modelConnect(context, strParentOId, strNewObjId, strRelationshipName);
          }
        //End transaction
        ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            //Transaction aborted in case of exception
            ContextUtil.abortTransaction(context);
            //The exception with appropriate message is thrown to the caller.
            throw  new FrameworkException(e);
        }
    }

    /**
     * Method to connect to model object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strFromObjectId the from object id
     * @param strToObjectId the to object id
     * @param strRelationshipName the relationship name
     * @throws FrameworkException if the operation fails
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
    protected void modelConnect (Context context, String strFromObjectId, String strToObjectId,
            String strRelationshipName) throws FrameworkException, Exception {
        //Domain Object is instantiated with the from object id
        DomainObject fromObject = newInstance(context, strFromObjectId);
        //Domain Object is instantiated with the to object id
        DomainObject toObject = newInstance(context, strToObjectId);
        //DomainObject toObject = new DomainObject(strToObjectId);
        //The method connect of DomainRelationship class is called to connect the objects
        DomainRelationship.connect(context, fromObject, strRelationshipName,
                toObject);
    }

    /**
    * Method call to update the model of the ProductLine as chosen by the user in the form page.
    * @param context the eMatrix <code>Context</code> object
    * @param args - Holds the parameters passed from the calling method
    * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    * @grade 0
    */
    public int updateModel(Context context, String[] args) throws Exception
    {
      HashMap programMap     = (HashMap) JPO.unpackArgs(args);
      HashMap paramMap       = (HashMap) programMap.get("paramMap");
      String  strObjectId    = (String) paramMap.get("objectId");
      //The new object id of the ProductLine that has to be used to connect with the Model in context
      String  strNewValue    = (String) paramMap.get("New OID");//this has been changed to new oid by pratima for type ahead changes
      updateConnection(context,strObjectId,ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS,strNewValue,false);
      return 0;
    }

    /**
    * Method to help in the updation of the model of the ProductLine as chosen by the user in the form page.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param parentObjectId - Holds parent object id
    * @param strRelationshipName - Holds the relationship name
    * @param strNewObjectId - Holds new Productline id
    * @param bIsFrom - if the Productline is at the to side or the from side of the relationship RELATIONSHIP_PRODUCTLINE_MODELS
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    * @grade 0
    */

    protected void updateConnection(Context context, String parentObjectId, String strRelationshipName, String strNewObjectId, boolean bIsFrom) throws Exception
    {
        StringList relSelect = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Context set with the product id
        setId(parentObjectId);
        //Relationship id of the previous company and product is fetched for disconnecting it.
        Map objectMap = getRelatedObject(context,strRelationshipName,bIsFrom,null,relSelect);
        // Begin of Modify by Enovia MatrixOne for Bug # 300610 Date 03/21/2005
        String strRelId = "";

		if (objectMap != null)
		{
			strRelId = (String)objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);

		    if ((strRelId != null) && !("".equals(strRelId)) && !("null".equals(strRelId)))
			{
			   	//The relationship is disconnected
				DomainRelationship.disconnect(context,strRelId);
			}
		}
		// End of Modify by Enovia MatrixOne for Bug # 300610 Date 03/21/2005
        // Begin of Modify by Enovia MatrixOne for Bug # 300612 Date 03/22/2005
       if ((strNewObjectId != null) && !("".equals(strNewObjectId)) && !("null".equals(strNewObjectId)))
        {
         BusinessObject tempBO = new BusinessObject(strNewObjectId);
         //The new company id is connected to the context product id
         connect(context,new RelationshipType(strRelationshipName),bIsFrom,tempBO);
        }
      //End of Modify by Enovia MatrixOne for Bug # 300612 Date 03/22/2005
    }

    /** This method gets the object Structure List for the context Model object.This method gets invoked
     *  by settings in the command which displays the Structure Navigator for Model type objects
     *  @param context the eMatrix <code>Context</code> object
     *  @param args    holds the following input arguments:
     *     0 - HashMap containing one String entry for key "objectId"
     *  @return MapList containing the object list to display in Model structure navigator
     *  @throws Exception if the operation fails
     *  @since Product Central 10-6
     */
    public static MapList getStructureList(Context context, String[] args)
        throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap)programMap.get("paramMap");
        String objectId    = (String)paramMap.get("objectId");
        MapList modelStructList = new MapList();
        // include type 'Products, Requirement and Features' in Builds structure navigation list
        Pattern typePattern     = new Pattern("*");
        DomainObject modelObj = DomainObject.newInstance(context, objectId);
        String objectType     = modelObj.getInfo(context, DomainConstants.SELECT_TYPE);
        String strSymbolicName = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, objectType, true);
        String strAllowedSTRel = "";
        try {
        	strAllowedSTRel = EnoviaResourceBundle.getProperty(context, "emxConfiguration.StructureTree.SelectedRel."+strSymbolicName);
        } catch (Exception e) {}

        if(strAllowedSTRel.equals("")){
        	strAllowedSTRel = EnoviaResourceBundle.getProperty(context, "emxConfiguration.StructureTree.SelectedRel.type_Model");
        }
        String[] arrRel = null;
        String strRelPattern = "";
	    if(strAllowedSTRel!=null && !strAllowedSTRel.equals("")){
	    	arrRel = strAllowedSTRel.split(",");
		    for(int i=0; i< arrRel.length; i++){
		    	strRelPattern = strRelPattern + "," + PropertyUtil.getSchemaProperty(context,arrRel[i]);
		    }
		    strRelPattern = strRelPattern.replaceFirst(",", "");
	    }

	    if (objectType != null && mxType.isOfParentType(context, objectType, ProductLineConstants.TYPE_MODEL)) {
        	if (strRelPattern.equals(ProductLineConstants.RELATIONSHIP_PRODUCTS)) {
        		// Get the root product of the model
       	     	Model objModel = new Model();
        	    String strRootId = objModel.getDerivationRoot(context, objectId);

        	    // Check for no products under the model.
        	    if (ProductLineCommon.isNotNull(strRootId)) {
	        	    // Get the info for the Root Product
	    			StringList slObjSelects = new StringList(DomainObject.SELECT_ID);
	    			slObjSelects.add(DomainObject.SELECT_NAME);
	    			slObjSelects.add(DomainObject.SELECT_REVISION);
	    			slObjSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
	    			slObjSelects.add(DomainObject.SELECT_RELATIONSHIP_NAME);

	    			// Get the information for the root object.
	    			DomainObject domObject = new DomainObject(strRootId);
	    			Map mpSingleObject = domObject.getInfo(context, slObjSelects);

	    			// Get the rest of the Derivations from the Root Product
	    			if (mpSingleObject != null && mpSingleObject.size() > 0) {
	    				modelStructList.add(mpSingleObject);
	    				MapList tempStructList = DerivationUtil.getAllDerivations(context, strRootId);
	    				if (tempStructList != null && tempStructList.size() > 0) {
	    					modelStructList.addAll(tempStructList);
	    				}
	    			}
        	    }
        	} else {
	            try{
	            	Pattern relPatternTemp = new Pattern(strRelPattern);
		                modelStructList = ProductLineCommon.getObjectStructureList(context, objectId, relPatternTemp, typePattern);
		            } catch(Exception ex) {
		                throw new FrameworkException(ex);
	            }
            }
        } else {
            modelStructList = (MapList) emxPLCCommon_mxJPO.getStructureListForType(context, args);
        }
        return modelStructList;
    }


    /**
      * get All Catalogs for a Model.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - HashMap containing one Map entry for the key "parameterMap"
      *      This Map contains the objectId
      * @return MapList
      * @throws Exception if the operation fails
      * @since ProductCentral 10.6
      */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllCatalogs(Context context, String[] args) throws Exception
    {
        MapList objectList = getCatalogs(context,args);

        return objectList;
    }


    /**
     * get All Active Catalogs for the Model.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one Map entry for the key "parameterMap"
     *      This Map contains the object Id.
     * @return MapList of all Active Catalogs for a Model
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllActiveCatalogs(Context context, String[] args) throws Exception
    {
        MapList retProdConfigList = new MapList();

        MapList objectList = getCatalogs(context,args);

        Iterator itrProdConfig = objectList.iterator();
        while (itrProdConfig.hasNext())
        {
            Map mapProdConfig = (Map) itrProdConfig.next();
            String strState = (String)mapProdConfig.get(DomainConstants.SELECT_CURRENT);

            if (strState.equalsIgnoreCase(ProductLineConstants.STATE_PRODUCT_CONFIGURATION_ACTIVE))
            {
                retProdConfigList.add(mapProdConfig);
            }
        }

        return retProdConfigList;
    }


    /**
     * get Effective Catalogs for the Model.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one Map entry for the key "parameterMap"
     *      This Map contains the objectId.
     * @return MapList of Effective Catalogs
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getEffectiveCatalogs(Context context, String[] args) throws Exception
    {
        java.util.Date sysDate = new java.util.Date();


        MapList retProdConfigList = new MapList();

        MapList objectList = getCatalogs(context,args);

        Iterator itrProdConfig = objectList.iterator();
        while (itrProdConfig.hasNext())
        {
            Map mapProdConfig = (Map) itrProdConfig.next();
            String strStartEffDate = (String) mapProdConfig.get("attribute[" + ProductLineConstants.ATTRIBUTE_START_EFFECTIVITY+ "]");
            String strEndEffDate   = (String) mapProdConfig.get("attribute[" + ProductLineConstants.ATTRIBUTE_END_EFFECTIVITY+ "]");

            // Started for bug no 360951
            java.util.Date newStartDate =null;
            java.util.Date newEndDate = null;

            if(!"".equals(strStartEffDate)&& strStartEffDate!=null)
                newStartDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strStartEffDate);
            if(!"".equals(strEndEffDate) && strEndEffDate!=null)
                newEndDate   = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strEndEffDate);
            // End for bug no 360951

            if ((newStartDate == null || "".equals(newStartDate)) && (newEndDate == null || "".equals(newEndDate)))
            {
                //Product Config is effective if dates are empty
                retProdConfigList.add(mapProdConfig);
            }
            else if (newStartDate == null || "".equals(newStartDate))
            {
                //Product Config is effective if start date is null && end date is after current date
                if (newEndDate.after(sysDate))
                {
                    retProdConfigList.add(mapProdConfig);
                }
            }
            else if (newEndDate == null || "".equals(newEndDate))
            {
                //Product Config is effective if end date is null && start date is before current date
                if (newStartDate.before(sysDate))
                {
                retProdConfigList.add(mapProdConfig);
                }
            }
            else if ((newStartDate.before(sysDate)) && (newEndDate.after(sysDate)))
            {
                // Product Config is effective if current date is between start & end dates
                retProdConfigList.add(mapProdConfig);
            }

        }

        return retProdConfigList;
    }


    /**
      * Method call to get all the Product Configurations.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args - Holds the parameters passed from the calling method
      * @param strWhereCondition - string containing the where condition
      * @return Object - MapList containing the id of Model objects
      * @throws Exception if the operation fails
      * @since ProductCentral 10.6
      */

     protected MapList getCatalogs(Context context, String args[])  throws Exception

        {

            HashMap parameterMap = (HashMap) JPO.unpackArgs(args);

            String objectId = (String) parameterMap.get("objectId");

            MapList relBusObjPageList = new MapList();

            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
            objectSelects.add("attribute[" + ProductLineConstants.ATTRIBUTE_START_EFFECTIVITY +"]");
            objectSelects.add("attribute[" + ProductLineConstants.ATTRIBUTE_END_EFFECTIVITY +"]");
            objectSelects.add("to["+ProductLineConstants.RELATIONSHIP_STANDARD_ITEM+"].from.type");
            objectSelects.add(DomainConstants.SELECT_CURRENT);

            DomainObject modelObj = new DomainObject(objectId);

            /* Getting the parameters for the getRelatedObjects method to retrieve the parent products(not in release state) connected to the previous revision of the context feature */

            StringBuffer sbBuffer = new StringBuffer();

            sbBuffer.append(ProductLineConstants.RELATIONSHIP_PRODUCTS);
            sbBuffer.append(STR_COMMA);
            sbBuffer.append(ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION);

            String strRelPattern = sbBuffer.toString();
            sbBuffer.delete(0,sbBuffer.length());

            sbBuffer.append(ProductLineConstants.TYPE_PRODUCTS);
            sbBuffer.append(STR_COMMA);
            sbBuffer.append(ProductLineConstants.TYPE_PRODUCT_CONFIGURATION);

            String strTypePattern = sbBuffer.toString();

            sbBuffer.delete(0,sbBuffer.length());

            sbBuffer.append(ProductLineConstants.TYPE_PRODUCT_CONFIGURATION);
            String strPostTypePattern = sbBuffer.toString();

            relBusObjPageList = modelObj.getRelatedObjects( context,
                                                            strRelPattern,
                                                            strTypePattern,
                                                            false,
                                                            true,
                                                            2,
                                                            objectSelects,
                                                            null,
                                                            null,
                                                            DomainConstants.EMPTY_STRING,
                                                            null,
                                                            strPostTypePattern,
                                                            null);


            // return only standard Product Configs
            Iterator itrProdConfig = relBusObjPageList.iterator();


            MapList finalMapList = new MapList();
            while (itrProdConfig.hasNext())

            {
                Map map = (Map) itrProdConfig.next();
                String sStandardItem = (String)map.get("to["+ProductLineConstants.RELATIONSHIP_STANDARD_ITEM+"].from.type");

                if(sStandardItem !=null && !"null".equals(sStandardItem) && !"".equals(sStandardItem))
                {
                    finalMapList.add(map);
                }

            }

            return finalMapList;
        }

     /** This method updates the Last Unit Number of the Model when a Build gets added to any of it Product revisions
      *  @param context the eMatrix <code>Context</code> object
      *  @param args holds the Model Id
      *  @return int value of the updated Last Unit Number
      *  @throws Exception if the operation fails
      *  @since Product Line X+4
      */
     public int getNextUnitNumber(Context context, String arg)
       throws Exception
    {
       String strModelId = arg;
       DomainObject objModel = newInstance(context, arg);
       String strCurrentLastUnitNumber = objModel.getAttributeValue(context, ProductLineConstants.ATTRIBUTE_LAST_BUILD_UNIT_NUMBER);
       int iLastUnitNumber = Integer.parseInt(strCurrentLastUnitNumber);
       String strUpdatedLastUnitNumber = String.valueOf(iLastUnitNumber + 1);
       objModel.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_LAST_BUILD_UNIT_NUMBER, strUpdatedLastUnitNumber);
       int iUpdatedLastUnitNumber = Integer.parseInt(strUpdatedLastUnitNumber);
       return iUpdatedLastUnitNumber;
    }
     /** This method returns the Last Unit Number of the Model
      *  @param context the eMatrix <code>Context</code> object
      *  @param arg holds the Model Id
      *  @return int value of the updated Last Unit Number
      *  @throws Exception if the operation fails
      *  @since Product Line X+4
      */
     public int getLastUnitNumber(Context context, String arg)
       throws Exception
    {
       String strModelId = arg;
       DomainObject objModel = newInstance(context, arg);
       String strCurrentLastUnitNumber = objModel.getAttributeValue(context, ProductLineConstants.ATTRIBUTE_LAST_BUILD_UNIT_NUMBER);
       int iLastUnitNumber = Integer.parseInt(strCurrentLastUnitNumber);
       return iLastUnitNumber;
    }
     /** This method updates the Last Unit Number of the Model
      *  @param context the eMatrix <code>Context</code> object
      *  @param args holds the Model Id, Updated Last Unit Number
      *  @return void
      *  @throws Exception if the operation fails
      *  @since Product Line X+4
      */
     public void setLastUnitNumber(Context context, String[] args)
       throws Exception
    {
       String strModelId = args[0];
       String strLastUnitNumber = args[1];
       DomainObject objModel = newInstance(context, strModelId);
       objModel.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_LAST_BUILD_UNIT_NUMBER, strLastUnitNumber);
       return;
    }
     /**
      * Method call to get the list of all master features.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args - Holds the following arguments
      *              0 - Hashmap containing the Object Id.
      * @return - Maplist of bus ids of master features
      * @throws Exception if the operation fails
      * @since ProductLine R207
      * @grade 0
      */
     public MapList getMasterFeatures (Context context, String[] args) throws Exception {
         MapList relBusObjPageList = new MapList();
         StringBuffer stbRelSelect = new StringBuffer(50);
         HashMap paramMap = (HashMap) JPO.unpackArgs(args);
         String objectId = (String) paramMap.get("objectId");
         String parentId = (String) paramMap.get("parentOID");
         DomainObject objModel = new DomainObject(objectId);
         String strObjectType = objModel.getInfo(context,DomainConstants.SELECT_TYPE);
         String strProductPlatformId = objModel.getInfo(context,"from["+ProductLineConstants.RELATIONSHIP_PRODUCTPLATFORM+"].to.id");
         if(!strObjectType.equalsIgnoreCase(ProductLineConstants.TYPE_MASTER_FEATURE)&& (strProductPlatformId ==null || "null".equals(strProductPlatformId) || "".equals(strProductPlatformId))) {
             return  (MapList)relBusObjPageList;

         }else if(strProductPlatformId!=null){
             objectId = strProductPlatformId;
         }
         DomainObject domProductPlatformObject = new DomainObject(objectId);
         String strRelType = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM;
         String strRelType1 = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO;
         String strExpandSelect = "from["
             + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO
             + "].to.from["
             + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM + "]";
         stbRelSelect = stbRelSelect
         .append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM);
         stbRelSelect = stbRelSelect.append(STR_COMMA);
         stbRelSelect = stbRelSelect
         .append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
         StringList selectStmts = new StringList();
         selectStmts.addElement(ProductLineConstants.SELECT_ID);
         selectStmts.addElement("from["
                 + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO + "].to."
                 + ProductLineConstants.SELECT_ID);
         selectStmts.addElement("from["
                 + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO + "].id");
         selectStmts.addElement(strExpandSelect);
         MapList featureList = domProductPlatformObject.getRelatedObjects(
                 context, strRelType, ProductLineConstants.QUERY_WILDCARD,
                 false, true, 0, selectStmts, null, null, null,
                 null, null, null);
         Iterator itrFeatures = featureList.iterator();
         Map hsFeatures = null;
         while (itrFeatures.hasNext()) {
             hsFeatures = (Map) itrFeatures.next();
             String strLevelKey = (String) hsFeatures.get("level");
             String objFeatureId = (String) hsFeatures.get("from["
                     + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO
                     + "].to." + ProductLineConstants.SELECT_ID);
             String strFeatureList = (String) hsFeatures
                     .get(ProductLineConstants.SELECT_ID);
             String strFeatureListToId = (String) hsFeatures
             .get("from["
                     + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO + "].id");
             HashMap hmpFeature = new HashMap();
             hmpFeature.put("level", strLevelKey);

             hmpFeature.put(ProductLineConstants.SELECT_ID,
                     objFeatureId);
             hmpFeature.put(ProductLineConstants.SELECT_RELATIONSHIP_ID,
                     strFeatureListToId);
             hmpFeature.put("FeatureListId", strFeatureList);

             hmpFeature.put("parentOID", parentId);
             hmpFeature.put("relationship", ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);

             relBusObjPageList.add(hmpFeature);

         }
         return  relBusObjPageList;
     }

     /** Added for Bug 369959
      * This method is used to control the access levels for Model Creation
      *
      * @param context
      *            The ematrix context object
      * @param args
      *            String array of the args
      * @return boolean
      * @since Variant Configuration V6R2010
      */
     public static boolean checkProductLineState(Context context, String[] args)
             throws Exception {
         String strPLState = "";
         boolean bAccess = true;
         HashMap requestMap = (HashMap) JPO.unpackArgs(args);
         String objectId = (String) requestMap.get("objectId");

         DomainObject domObj = new DomainObject(objectId);

         strPLState = domObj.getInfo(context,DomainConstants.SELECT_CURRENT);
         if(ProductLineConstants.STATE_INACTIVE.equalsIgnoreCase(strPLState)){
             bAccess=false;
         }
         return bAccess;

     }

     /**
      * This is a trigger method used to update the Revision Count attribute on the Model Object,
      * when the Product Revision object is connected to Model with Products relationship.
      * This method is called on the create trigger on "Products" relationship.
      *
      * @param Context context - Matrix Context Object
      * @param String args  - holds the FromID on the relationship which is Model.
 	 * @return int iResult - 0 if operation is successful
 	 * 					   - 1 if operation fails.
      * @throws Exception
      * @author IVU
      * @since PLC R209
      */
     public int updateRevisionCountOnModel(Context context, String args[])
 	throws Exception {
     	int iResult = 0;
 		try {
 		    String strModelId = args[0];
 			iResult = updateRevisionCount(context,strModelId,ProductLineConstants.RELATIONSHIP_PRODUCTS);
 		 } catch (Exception e) {
 			iResult = 1;
 		    throw new FrameworkException(e);
 		}
 		 return iResult;
     }

 	/**
      * This is a trigger method used to update the Revision Count attribute on the Model Object,
      * when the Product object is revised and connected to Model with Products relationship.
      * This method is called on the ModifyTo trigger on "Products" relationship.
      *
      * @param Context context - Matrix Context Object
      * @param String args  - holds the FromID on the relationship which is Model.
 	 * @return int iResult - 0 if operation is successful
 	 * 					   - 1 if operation fails.
 	 * @throws Exception
      * @author IVU
      * @since PLC R209
 	 */
     public int updateRevisionCountOnModelModifyTo(Context context, String args[])
 	throws Exception {
     	int iResult = 0;
 		try {
 		    String strModelId = args[0];
             iResult = updateRevisionCount(context,strModelId,ProductLineConstants.RELATIONSHIP_PRODUCTS);
 		 } catch (Exception e) {
 				iResult = 1;
 		    throw new FrameworkException(e);
 		}
 		 return iResult;
     }

     /**
      * This is a private method which does the actual setting of the Series Count attribute on the Model Object
      *
      * @param Context context - Matrix Context Object
      * @param String args  - holds the FromID on the relationship which is Model.
      * @param String strRelName - relationship name to find the number of objects connected through this relationship.
 	 * @return int iResult - 0 if operation is successful
 	 * 					   - 1 if operation fails.
      * @throws Exception
      * @author IVU
      * @since PLC R209
      */
     private int updateRevisionCount(Context context, String strObjId, String strRelName)
 	 throws Exception {
     	int bResult = 0;
 		try {
 			// Query the DB to get the count by expanding the Model object with Products relationship.
 		    String strModelId = strObjId;
 		  //  String strMQLQuery = "eval expr \"Count TRUE\"  on expand bus "+strObjId+" rel \""+strRelName+"\" dump";
 		   // String strCount = MqlUtil.mqlCommand(context, strMQLQuery);
 		    String strCount = MqlUtil.mqlCommand(context, "eval expr $1 on expand bus $2 rel $3 dump","Count TRUE",strObjId,strRelName);
 		    DomainObject domParent = new DomainObject(strModelId);
 		    domParent.setAttributeValue(context,PropertyUtil.getSchemaProperty(context,"attribute_SeriesCount"), strCount);
 		 } catch (Exception e) {
 			 bResult = 1;
 		    throw new FrameworkException(e);
 		}
 		 return bResult;
     }


/** This method would be used to get the Product to which the Feature is committed
 * @param context the eMatrix <code>Context</code> object
 * @param args   Holds parameters passed from the calling method
 * @return StringList -
 * @throws Exception
 * @since R211
 */
public StringList getCommittedProductForFeature(Context context,String[]args) throws Exception
{
	StringList slProducts = new StringList();;
	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	HashMap paramList = (HashMap)programMap.get("paramList");
	Model modelBean = new Model();
	StringTokenizer stTokens = null;
	Object objProductName =ProductLineConstants.EMPTY_STRING;
	Object objProductRevision =ProductLineConstants.EMPTY_STRING;
	Object objProductType =ProductLineConstants.EMPTY_STRING;
	StringList slProductName = ProductLineConstants.EMPTY_STRINGLIST;
	StringList slProductRevision =ProductLineConstants.EMPTY_STRINGLIST;
	StringList slProductType =ProductLineConstants.EMPTY_STRINGLIST;
	String sType = ProductLineConstants.EMPTY_STRING;


	List objectList = (MapList)programMap.get("objectList");
	String strParentOID = (String)paramList.get("parentOID");
	StringList slCommitedProduct = new StringList();

	if(!objectList.isEmpty()){
		for(int j = 0;j < objectList.size();j++){
			Map tempMap = (Map)objectList.get(j);
			objProductName = tempMap.get("to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from" +
	    			".to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.name");
			objProductRevision = tempMap.get("to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from" +
	    			".to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.revision");
			objProductType = tempMap.get("to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from" +
	    			".to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type");
			if(objProductName instanceof StringList){
				String tempString = ProductLineConstants.EMPTY_STRING;
				slProductName = (StringList) objProductName;
				slProductRevision = (StringList) objProductRevision;
				slProductType = (StringList) objProductType;
				for(int c=0;c<slProductName.size();c++){
					sType = (String)slProductType.get(c);
					if(!ProductLineConstants.TYPE_PRODUCTLINE.equalsIgnoreCase(sType)&& !ProductLineConstants.TYPE_PRODUCT_VARIANT.equalsIgnoreCase(sType)){
					tempString += slProductName.get(c)+" "+slProductRevision.get(c);
					if(c != slProductName.size()-1)
						tempString+=",";
					}
				}
				if(tempString.endsWith(","))
					tempString = tempString.substring(0, tempString.length()-1);
				slCommitedProduct.add(tempString);
			}else{
				String strProductName = (String) objProductName;
				String strProductRevision = (String) objProductRevision;
				sType = (String) objProductType;
				if(!ProductLineConstants.TYPE_PRODUCTLINE.equalsIgnoreCase(sType) && !ProductLineConstants.TYPE_PRODUCT_VARIANT.equalsIgnoreCase(sType))
				slCommitedProduct.add(strProductName+" "+strProductRevision);
			}


		}
	}
	//StringList slCommitedProduct = modelBean.getCommittedFeaturesWithProduct(context,strParentOID);
	for(int i=0;i<slCommitedProduct.size();i++)
	{
		StringBuffer sbProductIconTag = new StringBuffer();
		sbProductIconTag.append("<img src=\"../common/images/iconSmallProduct.gif\"/><h6>");
		stTokens = new StringTokenizer((String)slCommitedProduct.get(i),"|");


		while(stTokens.hasMoreElements()){

			if(stTokens.countTokens() == 1)
			{
				sbProductIconTag.append(stTokens.nextToken());
				sbProductIconTag.append("</h6>");
				slProducts.add(sbProductIconTag.toString());
			}else{
				stTokens.nextToken();
			}
		}
	}

	return slProducts;
}



public StringList getOwner(Context context, String args[]) throws Exception
{
	StringList slOwnerList = new StringList();
	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	Map paramList = (HashMap)programMap.get("paramList");
	String parentOID = (String)paramList.get("parentOID");
	List objectList = (MapList)programMap.get("objectList");
	String tempObID = ProductLineConstants.EMPTY_STRING;
	Map tempMap = null;
	DomainObject domFeatureID = new DomainObject();
	String strOwner = ProductLineConstants.EMPTY_STRING;
	StringTokenizer stTokens = new StringTokenizer(strOwner);
	String firstName =  ProductLineConstants.EMPTY_STRING;;
	String lastName =  ProductLineConstants.EMPTY_STRING;;

	if(objectList!=null){
		for(int i=0;i<objectList.size();i++){
			tempMap = (Hashtable)objectList.get(i);
			tempObID = (String)tempMap.get(DomainConstants.SELECT_ID);
			domFeatureID.setId(tempObID);
			strOwner = domFeatureID.getInfo(context, DomainConstants.SELECT_OWNER);
			int lastIndex = strOwner.lastIndexOf(" ");
			lastName = strOwner.substring(lastIndex+1,strOwner.length());
			firstName =  strOwner.substring(0,lastIndex);
			slOwnerList.add(lastName+", "+firstName);
		}
	}
	return slOwnerList;
}

public Map refreshCandidateFeatureListPageOnApply(Context context, String[] args) throws Exception {
    Map programMap = (Map) JPO.unpackArgs(args);

    Map returnMap = new HashMap();
        returnMap.put ("Action", "execScript");
        returnMap.put("Message", "{ main:function()  {getTopWindow().close();var listFrame = findFrame(getTopWindow().opener.getTopWindow(),'detailsDisplay');listFrame.location.href=listFrame.location.href;}}");
    return returnMap;
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
	 * This method connects the newly created model to the context Product Line or the product line chosen
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 */
	public void connectToProdutLine(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap   = (HashMap)programMap.get("paramMap");
		HashMap requestMap   = (HashMap)programMap.get("requestMap");
		String strParentID = null;

		// below code is applicable when the Model is created from Global Actions
		String[] strarrayPL = (String[])requestMap.get("ProductLine");
		if(strarrayPL != null){
			strParentID = strarrayPL[0];
		}
		// below code is applicable when the Model is created from Product Line Context
		String[] strarrayparentOID = (String[])requestMap.get("objectId");
		if(strarrayparentOID != null){
			strParentID = strarrayparentOID[0];
		}
		// Below code connects the newly created model to the  Product Line.
		if(strParentID!=null && !strParentID.equals("")){

			DomainObject parentObj = new DomainObject(strParentID);
			String newObjID = (String)paramMap.get("objectId");
			// Connect the Model to the Context Product Line
			DomainRelationship.connect(context, strParentID, ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS, newObjID,false);
		}
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
     */
	public String displayContextProductLine (Context context, String[] args) throws Exception
	{
		//XSSOK
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String strContextId = (String)requestMap.get("parentOID");
		StringBuffer sb = new StringBuffer();
		if(strContextId!=null && !strContextId.equals("") ){
			DomainObject objContext =  new DomainObject(strContextId);

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
		return sb.toString();
	}

	/**
	 * Method will determine if the Product Line Field should be displayed or not
	 *
     * @param context the Matrix Context
     * @param args contains-
     * 				-- programMap -> requestMap -> String objectId/ String UIContext
     * 				-- programMap -> fieldMap -> String name
	 * @return bResult true: Field will be displayed when the create Model is from Global Actions
	 * 				   false: Field will not be displayed when the create Model is from Product Line Context
	 * @throws FrameworkException
	 */
	public boolean showProductLineChooser(Context context, String[] args)
    throws FrameworkException
    {
		boolean bResult = false;
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Object strObjectID = programMap.get("objectId");

			if(strObjectID == null){
				bResult = true;
			}else{
				bResult = false;
			}
			return bResult;
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}
    }

	/**
	 * Method will determine if the Product Line Field should be displayed or not
	 *
     * @param context the Matrix Context
     * @param args contains-
     * 				-- programMap -> requestMap -> String objectId/ String UIContext
     * 				-- programMap -> fieldMap -> String name
	 * @return bResult true: Field will be displayed when the create Model is from Product Line Context
	 * 				   false: Field will not be displayed when the create Model is from Global Actions
	 * @throws FrameworkException
	 */
	public Object noShowProductLineChooser(Context context, String[] args)
    throws FrameworkException
    {
		boolean bResult = false;
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Object strObjId = programMap.get("objectId");
			if(strObjId != null){
				bResult = true;
			}else{
				bResult = false;
			}
			return bResult;
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}
    }



	/**
	 * This method copies the related data from Model to Product
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void copyModelAttributesToProduct(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap   = (HashMap)programMap.get("paramMap");
		String newObjID = (String)paramMap.get("objectId");

		// code to copy the Attributes of the Model to Product
   		String strAttributes = EnoviaResourceBundle.getProperty(context,"emxProduct.Model.CopyToProduct.Attributes");
   		StringTokenizer strAttrToken = new StringTokenizer(strAttributes, ",");
   		String strAttributeOpen = "attribute[";
   		String strAttributeClose = "]";
   		StringList strListCopyDetails = new StringList();
   		HashMap hMap = new HashMap();

   	    while (strAttrToken.hasMoreTokens())
   	    {
   	    	String strAttrKey = PropertyUtil.getSchemaProperty(context,strAttrToken.nextToken());
   	    	StringBuffer strBuf = new StringBuffer(300);
   	    	strBuf.append(strAttributeOpen);
   	    	strBuf.append(strAttrKey);
   	    	strBuf.append(strAttributeClose);
   	    	strListCopyDetails.add(strBuf.toString());
   	    	hMap.put(strBuf.toString(),strAttrKey);
   	    }

   	    DomainObject domModel = new DomainObject(newObjID);
   		Map mapModAttr = domModel.getInfo(context, strListCopyDetails);
   		mapModAttr.remove(DomainObject.SELECT_ID);
   		mapModAttr.remove(DomainObject.SELECT_TYPE);

   		Map attributeMap = new HashMap();
	   	Set setAttribute = mapModAttr.keySet();
	    Iterator setItr = setAttribute.iterator();
	    while (setItr.hasNext()) {
	         String strAttribute = (String) setItr.next();
	         String strActualAttr = (String) hMap.get(strAttribute);
	         attributeMap.put(strActualAttr, mapModAttr.get(strAttribute));
	    }

		//get the product to copy the Marketing Name and Text
		Map mpProd = domModel.getRelatedObject(
                   context,
                   ProductLineConstants.RELATIONSHIP_PRODUCTS,
                   true,
                   new StringList(DomainConstants.SELECT_ID),
                   new StringList(DomainRelationship.SELECT_ID));
		//the if statement is added for IR-146316V6R2013 avoid null pointer exception when create product trigger is diabled.
		if(mpProd != null){
			DomainObject domProductObj = newInstance(context, (String)mpProd.get(DomainConstants.SELECT_ID));
			domProductObj.setAttributeValues(context, attributeMap);

			// code to copy the basic Attributes of the Model to Product
	   		String strBasics = EnoviaResourceBundle.getProperty(context,"emxProduct.Model.CopyToProduct.Basics");
		    strListCopyDetails.clear();
		    strListCopyDetails.addElement(strBasics);
		    Map mapModBasics = domModel.getInfo(context, strListCopyDetails);
		    mapModBasics.remove(DomainObject.SELECT_ID);
		    mapModBasics.remove(DomainObject.SELECT_TYPE);

		   	Set setBasic = mapModBasics.keySet();
		    Iterator setBasicItr = setBasic.iterator();
		    while (setBasicItr.hasNext()) {
		         String strBasic = (String) setBasicItr.next();
		         // code to check for each basic attribute and calling appropriate method as the
		         // setAttributeValues doesn't work for basic attributes
		         if(strBasic.equalsIgnoreCase(DomainObject.SELECT_DESCRIPTION)){
		        	 domProductObj.setDescription(context,mapModBasics.get(strBasic).toString());
		         }
		         else if(strBasic.equalsIgnoreCase(DomainObject.SELECT_OWNER)){
		        	 domProductObj.setOwner(context, mapModBasics.get(strBasic).toString());
		         }
		         else if(strBasic.equalsIgnoreCase(DomainObject.SELECT_NAME)){
		        	 domProductObj.setName(context,mapModBasics.get(strBasic).toString());
		         }
		         else if(strBasic.equalsIgnoreCase(DomainObject.SELECT_POLICY)){
		        	 domProductObj.setPolicy(context,mapModBasics.get(strBasic).toString());
		         }
		         else if(strBasic.equalsIgnoreCase(DomainObject.SELECT_CURRENT)){
		        	 domProductObj.setState(context,mapModBasics.get(strBasic).toString());
		         }
		    }

		    // code to copy the Relationship data to the Product from Model
	   		String strRelationships = EnoviaResourceBundle.getProperty(context,"emxProduct.Model.CopyToProduct.Relationships");
	  		StringTokenizer strRelToken = new StringTokenizer(strRelationships, ",");
	  		StringBuffer strRels = new StringBuffer();

	   	    while (strRelToken.hasMoreTokens())
	   	    {
	   	    	strRels.append(PropertyUtil.getSchemaProperty(context,strRelToken.nextToken()));
	   	    	strRels.append(",");
	   	    }
	   	    StringList strRelSelect = new StringList();
	   	    strRelSelect.addElement(DomainRelationship.SELECT_NAME);
	   	    strRelSelect.addElement(DomainRelationship.SELECT_DIRECTION);

	   		MapList mLstModelRelatedData = domModel.getRelatedObjects(context,
	                strRels.toString(),
	                "*",
	                new StringList(DomainObject.SELECT_ID),
	                strRelSelect,
	                true, true, (short) 1,
	                DomainConstants.EMPTY_STRING,
	                DomainConstants.EMPTY_STRING,
	                0);

	   		for(int j=0; j<mLstModelRelatedData.size();j++){
	   			Map mapData = (Map) mLstModelRelatedData.get(j);
	   			String strObjId = (String)mapData.get(DomainObject.SELECT_ID);
	   			String strRelName = (String)mapData.get(DomainRelationship.SELECT_NAME);
	   			String strDirection = (String)mapData.get(DomainRelationship.SELECT_DIRECTION);

	   			RelationshipType rtConRelType = new RelationshipType(strRelName);
	   			if(strDirection.equalsIgnoreCase("to")){
	   				domProductObj.connect(context , rtConRelType , false, new DomainObject(strObjId));
	   			}else{
	   				domProductObj.connect(context , rtConRelType , true, new DomainObject(strObjId));
	   			}
	   		}
	    }
	}

// --------------------------------------------------------------------------------------
// Derivations JPO methods
// --------------------------------------------------------------------------------------

/**
 * This method is used to retrieve all the Products connected to the Model on the
 * Main Derivation Chain.  Used to get data for the Summary table Model Context.
 * @param context the eMatrix <code>Context</code> object
 * @param args string array containing packed arguments.
 * @return MapList - MapList containing the id of Product objects.
 * @throws Exception if the operation fails
 */
 @com.matrixone.apps.framework.ui.ProgramCallable
 public MapList getProductDerivations(Context context, String args[]) throws Exception
 {
     // The packed argument send from the JPO invoke method is unpacked to retrieve the HashMap.
     HashMap programMap = (HashMap) JPO.unpackArgs(args);

     // The object id of the context object is retrieved from the HashMap using the appropriate key.
     String strObjectId = (String)  programMap.get("objectId");

     // Get the Root Product of the Derivation chain based on the Model.
     Model objModel = new Model();
     String strRootId = objModel.getDerivationRoot(context, strObjectId);

     // Get the consolidated list of Main Derivations, including the root.
     MapList mlDerivations = Product.getMainDerivations(context, strRootId);

     // Make sure we have the levels all set correctly to the first level, since these are revisions and the root.
     mlDerivations = DerivationUtil.fixLevelInMapList(context, mlDerivations, "1");

     return mlDerivations;
 }

 /**
  * This expand method is used to get the Objects derived from the
  * ObjectId, called when a user expands a node on the Products Derivations
  * SB from the MODEL or PRODUCT.
  *
  * @param context The ematrix context of the request.
  * @param args string array containing packed arguments.
  * @return MapList containing ObjectIds of derived products
  * @throws Exception
  */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandProductDerivationStructure (Context context, String[] args) throws Exception {

    	// MapList to use for the return structure.
        MapList mlDerivationStructure = new MapList();

        try {
	        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	    	String objectID = (String) paramMap.get("objectId");

	    	// Level for recursion
	    	int level = ProductLineUtil.getLevelfromSB(context, args);
	    	// Now get the Expand Limit
	    	int limit = Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxProduct.ExpandLimit"));

	    	// Set level for recursive function
	    	int firstLevel = 1;

	    	mlDerivationStructure = DerivationUtil.getDerivationStructure
	    		(context, objectID, ProductLineConstants.TYPE_PRODUCTS, firstLevel, level, limit);

	    } catch (Exception e) {
	    	throw new FrameworkException(e);
	    }

	    return mlDerivationStructure;
    }

    /**
     * Returns product's derivation structure. It is called
     * when a user expands a node on the Products Derivations
     * SB from the MODEL or PRODUCT.
     *
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return MapList containing derived products
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getEffProdDerivationChain(Context context,
			String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String contextObjId = (String) programMap.get("objectId");
		String expandLevel = (String) programMap.get("expandLevel");
		String strLevel = (String) programMap.get("level");

		// If the ExpandLevel is all then set the recurse level to 0
		if(expandLevel == null){
			expandLevel = "1";
		} else if(ProductLineConstants.RANGE_VALUE_ALL.equalsIgnoreCase(expandLevel)){
			expandLevel = "0";
		}

		MapList mlDerivationStructure = new MapList();
		try {
			short recurseLevel = Short.parseShort(expandLevel);
			int limit = Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxProduct.ExpandLimit"));
			DomainObject domContextObj= DomainObject.newInstance(context, contextObjId);
			if(domContextObj.isKindOf(context,ProductLineConstants.TYPE_MODEL)){
				if(recurseLevel == 1){//get only Main Derived derivations - Revisions
					mlDerivationStructure = getProductDerivations(context,args);
				} else {
					//go through the list of product revisions of model and get entire model's structure
					MapList mainDervML =  getProductDerivations(context, args);
					if(mainDervML != null && mainDervML.size() > 0){
						for(int i=0; i < mainDervML.size(); i++){
							Map dervMap = (Map)mainDervML.get(i);
							String dervId = (String)dervMap.get(DomainObject.SELECT_ID);
							//get entire context product's structure including itself
							if(!expandLevel.equalsIgnoreCase("0")){
								recurseLevel = (short) (recurseLevel-1);
							}
							MapList tmpML = DerivationUtil.getDerivationStructure(context, dervMap, ProductLineConstants.TYPE_PRODUCTS, 1, recurseLevel, limit, true);
							mlDerivationStructure.addAll(tmpML);
						}
					}
				}
			}else{
				mlDerivationStructure = DerivationUtil.getDerivationStructure(context, contextObjId, ProductLineConstants.TYPE_PRODUCTS, 1, recurseLevel, limit);
			}
		} catch (Exception e) {
	    	throw new FrameworkException(e);
		}

		return mlDerivationStructure;
	}

	 /**
     * gets the milestones track objects at Product level to display in the Product Milestone Track table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @returns Object
     * @throws Exception if the operation fails
     * @since R215
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllMilestones(Context context, String[] args) throws Exception {

   	 MapList mlMilestones = new MapList();
   	 try{

   	 HashMap arguMap = (HashMap)JPO.unpackArgs(args);

   	 String strModelId = (String) arguMap.get("objectId");

   	 DomainObject domModel = new DomainObject(strModelId);

   	 StringList SELECT_MILESTONE_TRACK = new StringList() ;

   	 StringList objectSelects = new StringList(2);
   	 objectSelects.add(SELECT_ID);
   	 objectSelects.add(SELECT_NAME);
   	 StringList relationshipSelects = new StringList();
   	 String strTypePattern = ProductLineConstants.TYPE_MILESTONE_TRACK; 

   	 MapList mlMilestoneTrack = domModel.getRelatedObjects(context,
   			 ProductLineConstants.RELATIONSHIP_CONFIGURATION_CRITERIA,
   			 strTypePattern,
   			 objectSelects,
   			 relationshipSelects,
   			 false,	//to relationship
   			 true,	//from relationship
   			 (short)1,
   			 DomainConstants.EMPTY_STRING,
   			 DomainConstants.EMPTY_STRING,
   			 0);

   	 for(int m=0;m<mlMilestoneTrack.size();m++){
   		 Map mapMilestoneTrack = (Map)mlMilestoneTrack.get(m);
   		 String strMilestoneTrack = (String)mapMilestoneTrack.get(SELECT_ID);
   		 DomainObject domMilestoneTrack = new DomainObject(strMilestoneTrack);

   		 StringList objectSelects1 = new StringList(2);
   		 objectSelects1.add(SELECT_ID);
   		 objectSelects1.add(SELECT_NAME);
   		 StringList relationshipSelects1 = new StringList();
   		 String strtypePattern = ProductLineConstants.TYPE_MILESTONE;
   		 String busWhereClause = "";
   		 //Project Milestones connected with Model Level Milestones Track object
   		 MapList mlResult = domMilestoneTrack.getRelatedObjects(context,
   				ProductLineConstants.RELATIONSHIP_CONFIGURATION_ITEM,
   				 strtypePattern,
   				 objectSelects1,
   				 relationshipSelects1,
   				 false,	//to relationship - changed
   				 true,	//from relationship
   				 (short)1,
   				 DomainConstants.EMPTY_STRING,
   				 DomainConstants.EMPTY_STRING,
   				 0);


   		Map mapMT = (Map)domMilestoneTrack.getAttributeMap(context,true);
		String strDiscipline =(String)mapMT.get(ProductLineConstants.SELECT_ATTRIBUTE_MILESTONE_DISCIPLINE);

   		 for(int n=0;n<mlResult.size();n++){
   			 Map mapMilestone = (Map)mlResult.get(n);
   			 mapMilestone.put("MilestoneDiscipline", strDiscipline);
   		 }

   		 for(int n=0;n<mlResult.size();n++){
   			 Map mapMilestone = (Map)mlResult.get(n);
   			 String strRelationship = (String)mapMilestone.get("relationship");
   			 if(ProductLineConstants.RELATIONSHIP_CONFIGURATION_ITEM.equals(strRelationship)){
   				 mlMilestones.add(mapMilestone);
   			 }
   		 }

   	 }
   	 }catch(Exception ex){
   		 ex.printStackTrace();
   	 }
   	 return mlMilestones;


    }

    /**
     * gets the Feature Option objects at Product level to display in the Model Feature Option table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @returns Object
     * @throws Exception if the operation fails
     * @since R215
     * @grade 0
     */
    public MapList getAllFeatureOption(Context context, String[] args) throws Exception {

   	 MapList mlConfigurationFeatures = new MapList();
   	 try{

   	 HashMap arguMap = (HashMap)JPO.unpackArgs(args);

   	 String strModelId = (String) arguMap.get("objectId");

   	 DomainObject domModel = new DomainObject(strModelId);

   	 StringList objectSelects = new StringList(2);
   	 objectSelects.add(SELECT_ID);
   	 objectSelects.add(SELECT_NAME);
   	 StringList relationshipSelects = new StringList();

   	 String strRel = ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT + "," + ProductLineConstants.RELATIONSHIP_PRODUCTS;

   	 MapList mlProducts = domModel.getRelatedObjects(context,
   			 strRel,
   			 DomainConstants.QUERY_WILDCARD,
   			 objectSelects,
   			 relationshipSelects,
   			 false,	//to relationship
   			 true,	//from relationship
   			 (short)1,
   			 DomainConstants.EMPTY_STRING,
   			 DomainConstants.EMPTY_STRING,
   			 0);

   	 for(int m=0;m<mlProducts.size();m++){
   		 Map mapProduct = (Map)mlProducts.get(m);
   		 String strProduct = (String)mapProduct.get(SELECT_ID);
   		 DomainObject domProduct = new DomainObject(strProduct);

   		 StringList objectSelects1 = new StringList(2);
   		 objectSelects1.add(SELECT_ID);
   		 objectSelects1.add(SELECT_NAME);
   		 StringList relationshipSelects1 = new StringList();

   		 String busWhereClause = "";
   		 //Project Milestones connected with Model Level Milestones Track object
   		 MapList mlResult = domProduct.getRelatedObjects(context,
   				 ProductLineConstants.RELATIONSHIP_CONFIGURATION_FEATURES,
   				 DomainConstants.QUERY_WILDCARD,
   				 objectSelects1,
   				 relationshipSelects1,
   				 false,	//to relationship
   				 true,	//from relationship
   				 (short)1,
   				 DomainConstants.EMPTY_STRING,
   				 DomainConstants.EMPTY_STRING,
   				 0);


   		 for(int n=0;n<mlResult.size();n++){
   			 Map mapCF = (Map)mlResult.get(n);
   			 mlConfigurationFeatures.add(mapCF);
   		 }
   	 }
   	 }catch(Exception ex){
   		 ex.printStackTrace();
   	 }
   	 return mlConfigurationFeatures;
    }

    /**
     * gets the Feature Option objects at Product level to display in the Model Feature Option table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
	 *		modelId the id of the model milestone tracks attached to
     *      disciplinesList the list of milestone tracks' discipline to retrieve(e.g. Engineering, Manufacturing)
     *                If empty string or null is provided, all milestone tracks will be retrieved
     *      busSelects the business selects of milestone objects to return
     *                If not provided, only milestones' id will be returned
     *      relSelects the relationship selects of milestone objects to return
     *                If not provided, no information on relationships returned
     *      bInclMilestoneTrack If "true", milestone tracks will be included in returned MapList
     * @returns MapList of milestonetrack\milestone objects
     * @throws Exception if the operation fails
     */
    public MapList getModelMilestoneTracks(Context context, String[] args) throws Exception
    {
    	HashMap arguMap = (HashMap)JPO.unpackArgs(args);
    	String modelId = (String) arguMap.get("objectId");
    	StringList disciplinesList = (StringList) arguMap.get("disciplinesList");
    	StringList busSelects = (StringList) arguMap.get("busSelectsList");
    	StringList relSelects = (StringList) arguMap.get("relSelectsList");
    	String inclMilestoneTrack = (String) arguMap.get("bIncludeMilestoneTrack");
    	String includeCompleteMilestone = (String) arguMap.get("bIncludeCompleteMilestone");
    	boolean bInclMilestoneTrack = false;
    	boolean bIincludeCompleteMilestone = false;

	   	MapList milestoneTrackML = null;
	   	try{
	   		if(inclMilestoneTrack != null && "true".equalsIgnoreCase(inclMilestoneTrack)){
	   			bInclMilestoneTrack = true;
	   		}

	   		if(includeCompleteMilestone != null && "true".equalsIgnoreCase(includeCompleteMilestone)){
	   			bIincludeCompleteMilestone = true;
	   		}

	   		milestoneTrackML = Model.getModelMilestoneTracks(context, modelId, disciplinesList, busSelects, relSelects, bInclMilestoneTrack, bIincludeCompleteMilestone);
	   	} catch(Exception e){
	   		e.printStackTrace();
	   		throw new Exception(e.getMessage());
	   	}

	   	return milestoneTrackML;
   	}
    /**
     * This method will be executed through PLC only when UNT is not installed.
	 * This method creates the build for specified Model and connects the same
	 * with model
	 *
	 * @param context
	 *            - Matrix context
	 * @param args
	 *            - Holds the parameter map, request map for creating the builds
	 * @return void.
	 * @throws Exception
	 *             if the operation fails
	 * @since R216FD00
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createMultipleBuildsForModel(Context context, String[] args)
			throws Exception {
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String objectId = (String)paramMap.get("objectId");
			requestMap.put("newObjectId", objectId);
			//connect the First build created to Model
			connectToModel(context, args);
			//create the N-1 builds. This process further connects the builds to the model
			StringList buildIds = Model.createMultipleUnits(context, requestMap);
		} catch (Exception e) {
			e.printStackTrace();
			throw (new FrameworkException(e));
		}
	}
	/**
	 * This method will be executed through PLC only when UNT is not installed.
	 * This method connects the Build to a Model under which it is created.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the parammap, requestmap
	 * @return void
	 * @throws Exception
	 *             if operation fails
	 * @since R216FD00
	 */
	public void connectToModel(Context context, String[] args) throws Exception {
		try {
			
			// unpacking the Arguments from variable args
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strModelID = "";
			String strProductOID = (String) requestMap.get("ProductOID");
			if (strProductOID != null && strProductOID.length() > 0) {
				DomainObject domPorduct = new DomainObject(strProductOID);
				strModelID = domPorduct.getInfo(context, "to["
						+ ProductLineConstants.RELATIONSHIP_PRODUCTS
						+ "].from.id");
				return;
			} else {
				String strParentOID = (String) requestMap.get("parentOID");
				if (strParentOID != null && strParentOID.length() > 0) {
					DomainObject domObj = new DomainObject(strParentOID);
					if (mxType.isOfParentType(context, domObj.getInfo(context,
							ProductLineConstants.SELECT_TYPE),
							ProductLineConstants.TYPE_MODEL)) {
						strModelID = strParentOID;
					}
				}
			}
			// String strModelID = strModelOID;
			String strbuildId = (String) paramMap.get("objectId");
			// DomainObject domBuild = new DomainObject(strbuildId);

			if ((strModelID != null) && !(strModelID.equals(""))
					|| "Unassigned".equalsIgnoreCase(strModelID)
					|| "null".equalsIgnoreCase(strModelID)) {

				setId(strModelID);
				DomainRelationship.connect(context, strModelID,
						ProductLineConstants.RELATIONSHIP_MODEL_BUILD,
						strbuildId, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		} 
	}
	/**
	 * This method will be executed through PLC only when UNT is not installed.
	 * This method updates the Build Unit Number based on the Model it is
	 * connected to It is called as a create trigger when the Model Build
	 * relationship is established 
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the Model Id, Build Id and Relationship Id
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since R216FD00
	 */

	public void assignBuildUnitNumberForModelContext(Context context,
			String[] args) throws Exception {
		try {
			String strModelBuildRelId = args[0];
			String[] arrRelId = new String[1];
			arrRelId[0] = strModelBuildRelId;
			StringList relSelects = new StringList(2);
			relSelects.add(DomainRelationship.SELECT_FROM_ID);
			relSelects.add(DomainRelationship.SELECT_TO_ID);
			MapList objRelMapList = DomainRelationship.getInfo(context,
					arrRelId, relSelects);
			Map objRelMap = (Map) objRelMapList.get(0);
			String strModelID = (String) objRelMap
					.get(DomainRelationship.SELECT_FROM_ID);
			String strBuildID = (String) objRelMap
					.get(DomainRelationship.SELECT_TO_ID);
			if (strModelID == null || strModelID.length() == 0
					|| strBuildID == null || strBuildID.length() == 0) {
				return;
			}
			DomainObject objModel = newInstance(context, strModelID);
			if (!objModel.getPolicy(context).getName()
					.equals(ProductLineConstants.POLICY_MODEL)) {
				return;
			}
			DomainObject objBuild = newInstance(context, strBuildID);
			String strUnitNumber = objBuild.getAttributeValue(context,
					ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER);
			// Check if the Unit number exists for the build...if exists then
			// return.
			if (ProductLineCommon.isNotNull(strUnitNumber)
					&& !strUnitNumber.equalsIgnoreCase("0")) {
				return;
			}

			String[] arg = new String[1];
			arg[0] = strModelID;
			
			int iUnitNumber = getLastUnitNumber(context, strModelID);

			String strUpdatedUnitNumber = String.valueOf(iUnitNumber + 1);
			String[] arrParameters = new String[2];
			arrParameters[0] = strModelID;
			arrParameters[1] = strUpdatedUnitNumber;
			setLastUnitNumber(context, arrParameters);
			HashMap attributeMap = new HashMap();
			attributeMap.put(ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER,
					strUpdatedUnitNumber);

			objBuild.setAttributeValues(context, attributeMap);
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		} 
	}

	/**
	 * Method to get related Model Change Projects connected through Related Projects relationship
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - List of connected Change Projects
	 * @throws Exception if the operation fails
	 * 
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getModelRelatedChangeProjects(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			boolean isECHInstalled =  FrameworkUtil.isSuiteRegistered(context,
	  				"appVersionEnterpriseChange",false,null,null);
			if(isECHInstalled){
				HashMap paramMap = (HashMap)JPO.unpackArgs(args);
				String objectId = (String) paramMap.get("objectId");

				if(objectId!=null && !objectId.equalsIgnoreCase("")){
					DomainObject domObject = new DomainObject(objectId);
					//Get Model related Change Projects
					MapList relatedModelChangeProjects = domObject.getRelatedObjects(context,
							ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS,
							ProductLineConstants.TYPE_CHANGE_PROJECT,
							new StringList(DomainConstants.SELECT_ID),
							new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
							false,	//to relationship
							true,	//from relationship
							(short)1,
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING,
							0);

					returnMapList.addAll(relatedModelChangeProjects);
				}
			}			
		}catch (Exception e){
			throw e;
		}finally{
			return returnMapList;
		}
	}

}
