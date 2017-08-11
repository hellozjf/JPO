/*
** emxProductBase
**
** Copyright (c) 1992-2016 Dassault Systemes.
**
** All  Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
** static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.17.2.9.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
*/

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Hashtable;
import java.util.HashSet;
import java.text.ParseException;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Set;

/*Start of Add by Sandeep, Enovia MatrixOne for Bug # 310542*/

import matrix.db.BusinessTypeList;

/*End of Add by Sandeep, Enovia MatrixOne for Bug # 310542*/


import matrix.db.Context;
import matrix.db.Policy;
import matrix.db.State;
import matrix.db.BusinessObject;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.BusinessType;
import matrix.util.StringList;
import matrix.util.Pattern;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.productline.Product;
import com.matrixone.apps.productline.DerivationUtil;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;

/**
 * The <code>emxProductBase</code> class contains methods related to the admin type Products.
 * This includes methods for the Filter, Create, Delete, Remove and Copy Products.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxProductBase_mxJPO extends emxDomainObject_mxJPO
{

	public static final String SELECT_PHYSICALID ="physicalid";
    /**
  * Alias used for key emxProduct.Error.UnsupportedClient.
  */
  public static final String CHECK_FAIL = "emxProduct.Error.UnsupportedClient";
  /**
     * Alias used for the wild character(*).
     */
    protected static final String WILD_CHAR = "*";

    /**
     * Alias used for the string objectId.
     */
    protected static final String STR_OBJECTID = "objectId";

    /**
     * Alias used for the string objectList.
     */
    protected static final String STR_OBJECTLIST = "objectList";
        private final static String STR_ATTRIBUTE = "attribute";

        /**
    * Alias used for open brace.
    */
        protected static final String OPEN_BRACE = "[";
    /**
    * Alias used for close brace.
    */
    protected static final String CLOSE_BRACE = "]";

   //Begin of add by Enovia MatrixOne for bug #300692, 03/30/2005
    protected static final String STR_FROM = "from";
    protected static final String STR_TO = "to";
    protected static final String SUITE_KEY = "ProductLine";
    //End of add by Enovia MatrixOne for bug #300692, 03/30/2005


        /**



    * Alias used for double quotes.
    */
    protected static final String DOUBLE_QUOTES = "\"";

        /**
    * Alias used for Feature TNR Delimiter.
    */

        private final static String PRODUCT_TNR_DELIMITER = "::";

        /**
    * Alias used for Blank Space.
    */
    protected static final String SPACE = " ";

        /**
        *Alias for OR operator.
        */
        protected static final String strOROperator = "OR";

    /**
     * Alias used for the blank string.
     */
    protected static final String STR_BLANK = "";

        /** A string constant with the value COMMA:",". */
    public static final String STR_COMMA = ",";

        /** A string constant with the value ".". */
    public static final String STR_DOT = ".";

        /** A string constant with the value "1". */
    public static final String STR_ONE = "1";

    /**
     * Alias used for select expression of attribute Actual Finish Date.
     */
    protected static final String SELECT_TASK_ACTUAL_FINISH_DATE = "attribute["
            + DomainConstants.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE + "]";

    /**
     * Alias used for select expression of attribute Estimated Finish Date.
     */
    protected static final String SELECT_TASK_ESTIMATED_FINISH_DATE = "attribute["
            + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]";

    /**
     * Alias used for select expression of attribute Baseline Initial End Date.
     */
    protected static final String SELECT_BASELINE_INITIAL_END_DATE = "attribute["
            + DomainConstants.ATTRIBUTE_BASELINE_INITIAL_END_DATE + "]";

    /**
     * Alias used for select expression of attribute Baseline Current End Date.
     */
    protected static final String SELECT_BASELINE_CURRENT_END_DATE = "attribute["
            + DomainConstants.ATTRIBUTE_BASELINE_CURRENT_END_DATE + "]";
     /**
     *Alias used for string constant with the value field_display_choices.
     */
    protected static final String FIELD_DISPLAY_CHOICES = "field_display_choices";
    /**
     *Alias used for string constant with the value field_choices.
     */
    protected static final String FIELD_CHOICES = "field_choices";
    /**
     *A string constant with the value objectList.
     */
    protected static final String PARAM_MAP = "paramMap";
    /**
    * Default Constructor.
    *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    emxProductBase_mxJPO (Context context, String[] args) throws Exception
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
    * @since ProductCentral 10.0.0.0
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
    * This method is used to get All the Products, connected to the context users company.
    * This is invoked, when the Filter is set to All in the Products list page.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList - MapList containing the id of Product objects
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllProducts(Context context, String[] args) throws Exception
    {
        //The where condition is set to fetch all products that are not version.
    	StringList childTypes= ProductLineUtil.getChildrenTypes(context,PropertyUtil.getSchemaProperty(context,"type_ProductVariant"));
        StringBuffer whereCondition = new StringBuffer();
        whereCondition.append("(attribute[");
        whereCondition.append(ProductLineConstants.ATTRIBUTE_IS_VERSION);
        whereCondition.append("]==\"FALSE\" )");
        whereCondition.append("&& !(type matchlist (\"");
        whereCondition.append(PropertyUtil.getSchemaProperty(context,"type_ProductVariant"));
        for (int i = 0; i < childTypes.size(); i++) {
        	whereCondition.append(",");
        	whereCondition.append(childTypes.get(i));
		}
        whereCondition.append("\")\",\")");
        //Call to the common method that fetches the products irrespective of who owns it
        MapList objectList = findProducts(context,whereCondition.toString(),null);
        return objectList;
    }

    /**
    * This method is used to get All the Products owned by the context user,
    * connected to the context users company.
    * This is invoked, when the Filter is set to Owned in the Products list page.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return Object - MapList containing the id of Product objects
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOwnedProducts(Context context, String[] args) throws Exception
    {
    	StringList childTypes= ProductLineUtil.getChildrenTypes(context,PropertyUtil.getSchemaProperty(context,"type_ProductVariant"));
        //The where condition will be that the owner should be the context user
        StringBuffer whereCondition = new StringBuffer();
        whereCondition.append("(attribute[");
        whereCondition.append(ProductLineConstants.ATTRIBUTE_IS_VERSION);
        whereCondition.append("]==\"FALSE\" )");
        whereCondition.append("&& !(type matchlist (\"");
        whereCondition.append(PropertyUtil.getSchemaProperty(context,"type_ProductVariant"));
        for (int i = 0; i < childTypes.size(); i++) {
        	whereCondition.append(",");
        	whereCondition.append(childTypes.get(i));
		}
        whereCondition.append("\")\",\")");
        //Setting the owner condition to the context user.
        String ownerCondition = context.getUser();
        //Call to the common method that fetches the products owned by the context user.
        MapList objectList = findProducts(context,whereCondition.toString(),ownerCondition);
      
      
        return objectList;
    }

    /**
    * This method accepts WHERE condition and OWNER condition as parameters and retrives Products
    * connected to the context users company from the database based on the conditions.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param whereCondition - String value containing the condition based on which results are to be
    *                         filtered.
    * @param ownerCondition - String value containing the user name for owner of the Product object.
    * @return MapList - MapList containing the id of Product objects
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    protected MapList findProducts(Context context, String whereCondition, String ownerCondition)  throws Exception
    {
        //String list initialized for the object selects of the Product being retrieved.
        StringList objectList= new StringList(DomainConstants.SELECT_ID);
        objectList.add(DomainConstants.SELECT_TYPE);
        objectList.add(SELECT_PHYSICALID);
        //The actual name of the base and abstract type of product is retrieved for querying purpose.
        String strType = ProductLineConstants.TYPE_PRODUCTS;
        //String buffer initialized to store the where condition.
        StringBuffer sbWhereExp = new StringBuffer("");
        //Person object is retrieved from context, for getting the company the person is associated with.
        Person person = Person.getPerson(context);
        //Company to which the person is associated is retrieved for querying.
        String strCompany = person.getCompanyId(context);

        //Where company is formulated with all the pattern and added with the company condition
        if ((strCompany != null) && !(strCompany.equals(""))) {
            sbWhereExp.append("(");
            sbWhereExp.append(whereCondition);
            sbWhereExp.append(")");
            sbWhereExp.append(" && ");
            sbWhereExp.append("((");
            sbWhereExp.append("to[");
            sbWhereExp.append(ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT);
            sbWhereExp.append("].from." );
            sbWhereExp.append(DomainConstants.SELECT_ID);
            sbWhereExp.append("=='");
            sbWhereExp.append(strCompany);
            sbWhereExp.append("') ||(");
            sbWhereExp.append("to[");
            sbWhereExp.append(ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
      sbWhereExp.append("].from." );
      sbWhereExp.append(DomainConstants.SELECT_ID);
      sbWhereExp.append("=='");
      sbWhereExp.append(strCompany);
            sbWhereExp.append("'))");
        }
        //The findobjects method is invoked to get the list of products based on the owner and where condition.
        MapList relBusObjList = findObjects(context,strType,null,null,ownerCondition,null,sbWhereExp.toString(),true,objectList);
        //The list of object information retrieved by the object selects is returned back in the form of MapList
        return relBusObjList;
    }

    /**
    * This method retrives all the revision (including the context product's revision) of the Product.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - Has the packed Hashmap having information of the object in context.
    * @return MapList - MapList containing the id of revisions of the Product object
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRevisionProducts(Context context, String args[])  throws Exception
    {
        StringList multiSelect = new StringList();
        //The packed argument send from the JPO invoke method is unpacked to retrive the HashMap.
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        //The object id of the context object is retrieved from the HashMap using the appropriate key.
        String strObjectId = (String)  programMap.get("objectId");
        //The domain object class is initialized using the context object id.
        setId(strObjectId);
        //String list initialized for the object selects of the Product being retrieved.
        StringList objectList= new StringList(DomainConstants.SELECT_ID);
        //Function call to retrive the information of the revisions of context object, based on the object selects.
        MapList relBusObjList = getRevisionsInfo(context,objectList,multiSelect);
        //The MapList containing the information (id) of the revision object is returned.
        return relBusObjList;
    }

    /**
    * This method is used to retrieve all the Products connected with the Requirement object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList - MapList containing the id of Product objects connected with Requirement.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public MapList getRequirementProducts(Context context, String args[])  throws Exception
    {
        //Actual name of the relationship used to connect Product and Requirement is retrieved for querying.
        String relationshipName = ProductLineConstants.RELATIONSHIP_PRODUCT_REQUIREMENT;
        //Where condition is initialized to empty string
        String strWhereCondition = DomainConstants.EMPTY_STRING;
        //Function call to retrieve all the Products connected to the context object with the specific relationship name.
        MapList relBusObjList = expandForProducts(context,args,relationshipName,strWhereCondition);
        //The MapList containing the information (id) of the Product connected to the Requirement is returned.
        return relBusObjList;
    }


    //Begin of add by Enovia MatrixOne for bug #301382, 03/31/2005

        /**
        * This method is used to create a Product.
        * This method is invoked from the Bean.
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the FormBean contents
        * @throws Exception if the operation fails
        * @since ProductCentral 10.6
        */

        public String createProduct(Context context, String args[]) throws Exception
        {
            ArrayList programMap = (ArrayList)JPO.unpackArgs(args);
            String parentObjId = (String)programMap.get(0);
            String strName = (String)programMap.get(1);
            String strType = (String)programMap.get(2);
            String strRevision = (String)programMap.get(3);
            String strModelId = (String)programMap.get(4);
            String strDesignResId = (String)programMap.get(5);
            String strOwner = (String)programMap.get(6);
            String strMarketingName = (String)programMap.get(7);
            String strMarketingText = (String)programMap.get(8);
            String strDescription = (String)programMap.get(9);
            String strBasePrice = (String)programMap.get(10);
            String strStartEffectivity = (String)programMap.get(11);
            String strEndEffectivity = (String)programMap.get(12);
            String strWebAvailability = (String)programMap.get(13);
            String strPolicy = (String)programMap.get(14);
            String strVault = (String)programMap.get(15);
            String strCompanyId =  (String)programMap.get(16);

            //The date needs to be formmated before putting into database
            String timeZone = (String)programMap.get(17);
            //Modified by Enovia MatrixOne on 18-May-05 for Bug#304697
            Locale Local = (Locale) programMap.get(18);
			String strModelPrefix = (String)programMap.get(19);
                    double iClientTimeOffset = (new Double(timeZone)).doubleValue();

                    if (strStartEffectivity != null && strStartEffectivity.length() > 0) {
                                            strStartEffectivity = strStartEffectivity.trim();
                                            // If the field is date, get the value converted to input format
                                            strStartEffectivity = eMatrixDateFormat.getFormattedInputDate(strStartEffectivity,
                                                                    iClientTimeOffset, Local);

                                            }
                     if (strEndEffectivity != null && strEndEffectivity.length() > 0) {
                                            strEndEffectivity = strEndEffectivity.trim();
                                            // If the field is date, get the value converted to input format
                                            strEndEffectivity = eMatrixDateFormat.getFormattedInputDate(strEndEffectivity,
                                                                    iClientTimeOffset, Local);
                    }
                //HashMap to store the attribute name value pairs.
                //The attribute values to be set for the object are put in the map.
                //
                HashMap attributeMap = new HashMap();
                attributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_NAME,
                        strMarketingName);
                attributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_TEXT,
                        strMarketingText);
                attributeMap.put(ProductLineConstants.ATTRIBUTE_BASE_PRICE,
                        strBasePrice);
                attributeMap.put(ProductLineConstants.ATTRIBUTE_END_EFFECTIVITY,
                        strEndEffectivity);
                attributeMap.put(ProductLineConstants.ATTRIBUTE_START_EFFECTIVITY,
                        strStartEffectivity);
                attributeMap.put(ProductLineConstants.ATTRIBUTE_WEB_AVAILABILITY,
                        strWebAvailability);

	    		// Create the node for the Root product and set the attributes.
	    		HashMap nodeAttributeMap = DerivationUtil.createDerivedNode (context, null, DerivationUtil.DERIVATION_LEVEL0, strType);
	    		if (nodeAttributeMap != null && !nodeAttributeMap.isEmpty()) {
	    			// Add the map values to the attributeMap.
	    			attributeMap.putAll(nodeAttributeMap);
	    		}
                
                
                //
                //  The create method is called with the required parameters.
                //
                String CreateModelContext = "FALSE";
                if (!(strModelId == null || "".equalsIgnoreCase(strModelId) || "null".equalsIgnoreCase(strModelId) )) {
                	CreateModelContext = "TRUE";
                }
                PropertyUtil.setGlobalRPEValue(context,"CreateModelContext",CreateModelContext);

               ProductLineCommon pcBean = new ProductLineCommon();
               String productId = pcBean.create(context, strType, strName, strRevision,
                        strDescription, strPolicy, strVault, attributeMap, strOwner,
                        parentObjId, DomainConstants.EMPTY_STRING, true);
                //The context is set with the created product id.
                this.setId(productId);

                //The RelationshipType will be used in the connect method.
                RelationshipType rtRelObject = new RelationshipType();
                //
                //If the Model field in not empty then the Product is connected
                //to the model id obtained.
                //
                if (!(strModelId == null || "".equalsIgnoreCase(strModelId) || "null".equalsIgnoreCase(strModelId) )) {
                    rtRelObject = new RelationshipType(ProductLineConstants.RELATIONSHIP_PRODUCTS);
                    DomainObject modelObject = newInstance(context,strModelId);
                    connect(context, rtRelObject, false, modelObject);
                }
                //
                //If the Design Responsibility field in not empty then the Product is connected
                //to the Organization id obtained.
                //
                if (!(strDesignResId == null || "".equalsIgnoreCase(strDesignResId) || "null".equalsIgnoreCase(strDesignResId))) {
                    rtRelObject = new RelationshipType(ProductLineConstants.RELATIONSHIP_PRODUCT_ORGANIZATION);
                    DomainObject organizationObject = newInstance(context,strDesignResId);
                    boolean bHasFromConnectAccess = (organizationObject
                                                     .getInfo(
                                                            context,
                                                            "current.access[fromconnect]"))
                                                     .equalsIgnoreCase("true")?true:false;
                    if(!bHasFromConnectAccess){
                         ContextUtil.pushContext(context);
                    }
                    connect(context, rtRelObject, false, organizationObject);
                    if(!bHasFromConnectAccess){
                         ContextUtil.popContext(context);
                    }
                }

                if (strCompanyId == null || "".equalsIgnoreCase(strCompanyId) || "null".equalsIgnoreCase(strCompanyId)) {
                  //The domain object of the context user is obtained
                  DomainObject personObject = PersonUtil.getPersonObject(context);
                  //The select list for the values to be retrived from query is formulated
                  StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
                  //Id of the context person object is obtained
                  String personId = personObject.getId();
                  //Context is set to the person id
                  this.setId(personId);
                  //The company (id) to which the the context user is connected to is obtained
                  Map companyList = getRelatedObject(context, DomainConstants.RELATIONSHIP_EMPLOYEE,
                          false, objectSelects, null);
                  //The id of the company is obtained from the map
                  strCompanyId = (String)companyList.get(DomainConstants.SELECT_ID);
                }
                //Domain Object is instantiated with the company id.
                DomainObject companyObject = newInstance(context,strCompanyId);
                //Relationship type initialized to the suitable relationship name
                rtRelObject = new RelationshipType(ProductLineConstants.RELATIONSHIP_PRODUCT_COMPANY);
                this.setId(productId);
                //Connects the product object with the company object.
                connect(context, rtRelObject, false, companyObject);

                DomainObject domProduct = newInstance(context, productId);
                Map mpModel = domProduct.getRelatedObject(
                                        context,
                                        ProductLineConstants.RELATIONSHIP_PRODUCTS,
                                        false,
                                        new StringList(DomainConstants.SELECT_ID),
                                        new StringList(DomainRelationship.SELECT_ID));
                // Begin of modify by Enovia MatrixOne for bug no. 304222, dated 05/11/2005
				//Changes made to fix Bug 336260 on 12/4/2007
                if( mpModel != null  && (strModelId == null || strModelId.equals("null") || strModelId.equals(""))  )
                {
                    DomainObject domModelObj = newInstance(context, (String)mpModel.get(DomainConstants.SELECT_ID));

                    // Begin of Modify by Enovia MatrixOne for Bug # 301807 Dated 04/04/2005
                    attributeMap.clear();
                    attributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_NAME,
                            strMarketingName);
                    attributeMap.put(ProductLineConstants.ATTRIBUTE_ORIGINATOR,
                            context.getUser());
					attributeMap.put("Prefix",
                            strModelPrefix);
                    domModelObj.setAttributeValues(context, attributeMap);

                    // Begin of modify by Enovia MatrixOne for bug 304670 on 19-May-2005
                    domModelObj.setOwner(context,
                                                    strOwner);
                    // End of modify by Enovia MatrixOne for bug 304670 on 19-May-2005
                }
                // End of modify by Enovia MatrixOne for bug no. 304222, dated 05/11/2005

                // End of Modify by Enovia MatrixOne for Bug # 301807 Dated 04/04/2005
                return  productId;
        }

        //End of add by Enovia MatrixOne for bug #301382, 03/31/2005


/**
        * This method is used to create a Model on creation of a new Product.
        * This method is invoked on Trigger Action of Product Creation.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds object id of the product as argument
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
        public void createModelOnCreate(Context context,String args[]) throws Exception
        {
        	
			String strIPMLCommandName=PropertyUtil.getGlobalRPEValue(context,"IPMLCommandName");
    		
    		
    		if (strIPMLCommandName == null|| "".equalsIgnoreCase(strIPMLCommandName)|| "null".equalsIgnoreCase(strIPMLCommandName)){
			String strProductId = args[0];
        	String strModelType = args[1];

        	DomainObject domProductObj = DomainObject.newInstance(context,strProductId);
        	String strProductName = domProductObj.getInfo(context, ProductLineConstants.SELECT_NAME);
        	String vaultPattern = domProductObj.getVault();

        	//Modified for IR-030868V6R2011
        	//Turn off the matrix triggers
        	//MqlUtil.mqlCommand(context, "trigger off", true);
        	String strCreateModel = PropertyUtil.getGlobalRPEValue(context,"CreateModelTrigger");
        	PropertyUtil.setGlobalRPEValue(context,"CreateModelTrigger", "FALSE");

        	if(strCreateModel!=null && !"TRUE".equals(strCreateModel))
        	{
        		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        		String revPattern = domProductObj.getDefaultRevision(context, ProductLineConstants.POLICY_MODEL);
        		String strCreateModelContext = PropertyUtil.getGlobalRPEValue(context,"CreateModelContext");

        		/*MapList mpObjList = DomainObject.findObjects(context,
                            PropertyUtil.getSchemaProperty(context,strModelType),
                            strProductName,
                            "",
                            WILD_CHAR,
                            vaultPattern,
                            null,
                            true,
                            objectSelects);
	                if(mpObjList.size() == 0){*/
        		if(strCreateModelContext!=null && !"TRUE".equals(strCreateModelContext)){
                	String strReltoConnect = ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT;
        			DomainObject domModelObj = new DomainObject();
        			PropertyUtil.setGlobalRPEValue(context,"CreateProductTrigger", "TRUE");
        			//removed the code for get realted and size check for IR-079616V6R2012
        			DomainRelationship domRel = domModelObj.createAndConnect(
        					context,
        					PropertyUtil.getSchemaProperty(context,strModelType),
        					strProductName,
        					null,
        					null,
        					null,
		                    strReltoConnect,
        					domProductObj,
        					false);
        		}
        	}
		  }
	    }

    /**
    * This method is used to retrieve all the Products connected with the Model object.
    * This method is invoked, when the Filter value is set to All under Models.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList - MapList containing the id of Product objects connected with Model.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getModelProducts(Context context, String args[])  throws Exception
    {
        //Actual name of the relationship used to connect Product and Model is retrieved for querying.
        String relationshipName = ProductLineConstants.RELATIONSHIP_PRODUCTS;
        //Where condition is initialized to empty string
        String strWhereCondition = DomainConstants.EMPTY_STRING;
        //Function call to retrieve all the Products connected to the context object with the specific relationship name.
        MapList relBusObjList = expandForProducts(context,args,relationshipName,strWhereCondition);
        //The MapList containing the information (id) of the Product connected to the Model is returned.
        return relBusObjList;
    }

    /**
    * This method is used to retrieve all the latest revisions of Products connected with the Model object.
    * This method is invoked, when the Filter value is set to Latest under Models.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList - MapList containing the id of latest Product objects connected with Model.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public MapList getModelLatestProducts(Context context, String args[])  throws Exception
    {
        //Actual name of the relationship used to connect Product and Model is retrieved for querying.
        String relationshipName = ProductLineConstants.RELATIONSHIP_PRODUCTS;
        //Where condition is initialized to retrieve the latest revision
        String strWhereCondition = "("+DomainConstants.SELECT_REVISION+"=="+ProductLineConstants.SELECT_LAST+")";
        //Function call to retrieve all the latest Products connected to the context object with the specific relationship name.
        MapList relBusObjList = expandForProducts(context,args,relationshipName,strWhereCondition);
        //The MapList containing the information (id) of the latest Product connected to the Model is returned.
        return relBusObjList;
    }

    /**
    * This method retrieves all the products connected to the context product as version.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList - MapList containing the id of Product(Version) objects connected to the context product.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getVersionProducts(Context context, String args[])  throws Exception
    {
        //Sets the relationship name to the one connecting Product and Version (Product)
        String relationshipName = ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION;
        //Where condition is set to filter the Product Variants to be shown in the
        //Versions summary Page
        StringBuffer strWhereCondition = new StringBuffer();
        
        //strWhereCondition.append("(type!=\"");
        //strWhereCondition.append(PropertyUtil.getSchemaProperty(context,"type_ProductVariant"));
        //strWhereCondition.append("\")");
        
        //Function call to retrieve all the Products(versions) connected to the context object with the specific relationship name.
        MapList relBusObjList = expandForProducts(context,args,relationshipName,strWhereCondition.toString());
        //The MapList containing the information (id) of the Product(versions) connected to the context product is returned.
        
        MapList actualVersionList = new MapList();
        for(int i=0; i < relBusObjList.size(); i++){
        	String type = (String)((Map)relBusObjList.get(i)).get(DomainConstants.SELECT_TYPE);
        	if(type!=null && !mxType.isOfParentType(context,type,ProductLineConstants.TYPE_PRODUCT_VARIANT)){
        		actualVersionList.add(relBusObjList.get(i));        		
        	}
        }        
        return actualVersionList;
    }

    /**
    * This method accepts Relationship name and WHERE condition and expands the context object based
    * on the speicified relationship, to retrievel all the Products matching the where condition.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - Has the packed Hashmap having information of the object in context.
    * @param relationshipName - Name of the relationship that has to be traversed to get the Products
    * @param strWhereCondition - Where Condition, that needs to be used to filter the Product objects.
    * @return MapList - MapList containing the id of Product objects and the relationships
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    protected MapList expandForProducts(Context context, String args[],String relationshipName, String strWhereCondition)  throws Exception
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
        String strType = ProductLineConstants.TYPE_PRODUCTS;
        //The getRelatedObjects method is invoked to get the list of products connected to the context object with the specific relationship.
        MapList relBusObjPageList = getRelatedObjects(context , relationshipName, strType, objectSelects, relSelects, true, true, (short)1, strWhereCondition, DomainConstants.EMPTY_STRING);
        //The MapList containing the information (id) of the Product and the relationship.
        return relBusObjPageList;
    }
    /**
    * This method is called by the Check trigger to verify if the there is a
      model connected to the product.
    * value is returned to indicate the failure of Check trigger.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *   0 - String containing the object id of the context Product
    * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    public int checkforRelatedModel(Context context, String args[]) throws Exception
    {
        String relationshipName = ProductLineConstants.RELATIONSHIP_PRODUCTS;
        String strFromType = ProductLineConstants.TYPE_MODEL;
        //The Product object id sent by the emxTriggerManager is retrieved here.
        String objectId = args[0];
        //The object id is set to the context
        setId(objectId);
        String strType = (String) this.getInfo(context,SELECT_TYPE);
        String selIsVersion = "attribute[" + ProductLineConstants.ATTRIBUTE_IS_VERSION + "]";
        String strIsVersion = (String) this.getInfo(context,selIsVersion);

        if (com.matrixone.apps.domain.util.mxType.isOfParentType(context,strType,ProductLineConstants.TYPE_PRODUCT_VARIANT) || strIsVersion.equalsIgnoreCase("TRUE")){
        	return 0;
        }

        MapList mL=getRelatedObjects(context ,relationshipName,strFromType,new StringList(),new StringList(), true,false, (short)1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING,0);
        if(mL.size()==0)
        {
            // Begin of modify by Enovia MatrixOne for bug 304670 on 19-May-2005
            //Alert message formulated to display the error message.
            String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.NoRelatedModel",context.getSession().getLanguage());
            // End of modify by Enovia MatrixOne for bug 304670 on 19-May-2005
            //Alert message is explicitly thrown to the front end.
            emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
            //Non zero value is returned to indicate the failure of check trigger.
            return 1;
        }
        else
        {
            return 0;
        }
    }

    /**
    * This method is called by the Check trigger to verify if the first level Features connected with
    * Product are in Release state. If any of the top level Feature is not in Release state, then a non zero
    * value is returned to indicate the failure of Check trigger.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *   0 - String containing the object id of the context Product
    * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public int checkFeatureForProductPromote(Context context, String args[]) throws Exception
    {
        //The Product object id sent by the emxTriggerManager is retrieved here.
        String objectId = args[0];
        //The object id is set to the context
        setId(objectId);
        //Relationship name is set for the query on Feature objects.
        String strRelationshipName = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM;
        //Type name is set for the query to retrieve the Feature objects.
        String strType = ProductLineConstants.TYPE_FEATURE_LIST;
        //Select statement to get the state of by navigating to the Feature object from the intermediate Feature List object obtained in the query
        String strSelect = "from["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.current";
        //StringList for retrieving the object information (state) is initialized.
        StringList objectSelects = new StringList(strSelect);
        //The context object is expanded and the state of the First level features are obtained in the MapList
        MapList relBusObjList = getRelatedObjects(context,strRelationshipName,strType,objectSelects,null,true,true,(short)1,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
        //The number of feature/feature list connected is obtained.
        int iNumberOfObjects = relBusObjList.size();
        //Validation check only if there is any Feature List conected.
        if (iNumberOfObjects > 0)
        {
            /*For each of the feature that is connected, check is made on its state.
             *Validation fails if it not in Release state
             */
            for (int i = 0;i < iNumberOfObjects ; i++)
            {
                //The state is retreived from the MapList obtained.
                String strState = (String)((Hashtable)relBusObjList.get(i)).get(strSelect);
                //The state is current state of the Feature is compare to the Actual name of the Release state.
                if (!strState.equalsIgnoreCase(ProductLineConstants.STATE_RELEASE))
                {
                    //Alert message is formulated to display the error message
                    String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.FeaturesCheckFailed",context.getSession().getLanguage());
                    //Explicit alert message is thrown to the front end.
                    emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
                    //Non zero value is returned to the trigger manager to denote failure.
                    return 1;
                }
            }
        }
        //Return 0 is validation is passed
        return 0;
    }

    /**
    * This method is called by the Check trigger to verify if the parts connected with
    * Product are in Complete state. If any of the Part is not in Complete state, then a non zero
    * value is returned to indicate the failure of Check trigger.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *   0 - String containing the object id of the context Product
    * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public int checkPartForProductPromote(Context context, String args[]) throws Exception
    {
        //The product object id sent by the emxTriggerManager is retrieved here.
        String objectId = args[0];
        //The object id is set to the context
        setId(objectId);
        //Relationship name is set for the query on Part objects.
        String strRelationshipName = ProductLineConstants.RELATIONSHIP_GBOM_FROM;
        //Type name is set for the query on Part objects.
        String strType = ProductLineConstants.TYPE_GBOM;
        //Select statement to get the state of by navigating to the Part object from the intermediate GBOM object obtained in the query
        String strSelect = "from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.current";

        // Begin of add by: Bhagyashree, Enovia MatrixOne for bug no.301313 ,Date: 24-Mar-05
        StringBuffer sbBuffer = new StringBuffer(100);
        sbBuffer.append("from[");
        sbBuffer.append(ProductLineConstants.RELATIONSHIP_GBOM_TO);
        sbBuffer.append("].to.");
        sbBuffer.append(DomainConstants.SELECT_TYPE);

        String strSelectType = sbBuffer.toString();
        // End of add by: Bhagyashree, Enovia MatrixOne for bug no.301313 ,Date: 24-Mar-05

        //StringList for retrieving the object information (state) is initialized.
        StringList objectSelects = new StringList(strSelect);

        // Begin of add by: Bhagyashree, Enovia MatrixOne for bug no.301313 ,Date: 24-Mar-05
        objectSelects.addElement(strSelectType);
        // End of add by: Bhagyashree, Enovia MatrixOne for bug no.301313 ,Date: 24-Mar-05

        //The context object is expanded and the state of the Parts are obtained in the MapList
        MapList relBusObjList = getRelatedObjects(context,strRelationshipName,strType,objectSelects,null,true,true,(short)1,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
        //The number of Parts connected is obtained.
        int iNumberOfObjects = relBusObjList.size();
        //Validation check only if there is any part conected.
        if (iNumberOfObjects > 0)
        {
            /*For each of the part that is connected, check is made on its state.
            *Validation fails if it not in Complete state
            */
            for (int i = 0;i < iNumberOfObjects ; i++)
            {
                //The state is retreived from the MapList obtained.
                String strState = (String)((Hashtable)relBusObjList.get(i)).get(strSelect);

                // Begin of Modify by: Bhagyashree, Enovia MatrixOne for bug no.301313 ,Date: 24-Mar-05
                String strObjectType = (String)((Hashtable)relBusObjList.get(i)).get(strSelectType);

                //If the object is of type Part Issuer no check will be done for the state.
                if(!strObjectType.equals(ProductLineConstants.TYPE_PARTFAMILY))
                {
                    //The state of the Part object is compared with the actual name of the state.
                    if (!strState.equalsIgnoreCase(ProductLineConstants.STATE_RELEASE))
                    {
                        String language = context.getSession().getLanguage();
                        //Alert message is formulated to display the error message
                        String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.PartsCheckFailed",language);
                        //Explicit alert message is thrown to the front end.
                        emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
                        //Non zero value is returned to the trigger manager to denote failure.
                        return 1;
                    }
                }
                // End of Modify by: Bhagyashree, Enovia MatrixOne for bug no.301313 ,Date: 24-Mar-05
            }
        }
        //Return 0 is validation is passed
        return 0;
    }

    /**
    * This method is called by the Action trigger to promote the Rules and Feature List associated to the Release state.
    * when the Product is promoted to the Release state.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - args holds the following input arguments:
    *   0 - String containing the object id of the context Product
    * @return int - Returns 0 in case of action trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
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
        + ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM + strComma
        + ProductLineConstants.RELATIONSHIP_GBOM_FROM + strComma
        + ProductLineConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION + strComma
        + ProductLineConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
        //Object where condition to retrieve the objects that are not already in Release state.
        String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " != \""+ProductLineConstants.STATE_RELEASE+"\")";
        //Type to fetched is all types returned by the relationship.
        String strType = "*";
        //ObjectSelects retreives the parameters of the objects that are to be retreived.
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        //The objects connected to the product based on the relationships defined are obtained.
        MapList relBusObjList = getRelatedObjects(context,strRelationshipPattern,strType,objectSelects,null,true,true,(short)1,objectWhere,DomainConstants.EMPTY_STRING);
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
    * This method is called by the Check trigger to verify if the Previous releases of the product
    * are in Frozen state. If any of the previous releases is not in Frozen state, then a non zero
    * value is returned to indicate the failure of Check trigger.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - args holds the following input arguments:
    *   0 - String containing the object id of the context Product
    * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public int checkPreviousRevisionForProductPromote(Context context, String args[])  throws Exception
    {
        //The product object id sent by the emxTriggerManager is retrieved here.
        String objectId = args[0];

       	// Get the parent of the current product.
       	String strParentId = DerivationUtil.getParentId(context, objectId);
       	// Check for root product
       	if (ProductLineCommon.isNotNull(strParentId)) {
        	if (!Product.isFrozenState(context, strParentId)) {
                String language = context.getSession().getLanguage();
                //Alert message formulated to display the error message.
                String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.DerivationStateCheckFailed",language);
                //Alert message is explicitly thrown to the front end.
                emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
                //Non zero value is returned to indicate the failure of check trigger.
                return 1;
        	}
       	}
        //Zero is returned to indicate the success of the check trigger.
        return 0;
    }

    /**
    * This method is invoked, when the Company to which the Product is associated with is changed.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - Has the packed Hashmap having information of the object in context.
    * @return int - Returns 0 in case of the updation process is successful
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public int updateCompany(Context context, String[] args) throws Exception
    {
        //HashMap is defined to retrieve the arguments sent by the form after unpacking.
        HashMap programMap     = (HashMap) JPO.unpackArgs(args);
        //HashMap is defined to retrieve another HashMap in the unpacked list, that has the object information
        HashMap paramMap       = (HashMap) programMap.get("paramMap");
        //Object Id of the context object is obtained from the Map.
        String  strObjectId    = (String) paramMap.get("objectId");
        //The new object id of the company that has to be used to connect with the product in context is obtained
        String  strNewValue    = (String) paramMap.get("New OID");//update to accomadte typaahead/fullsearch in 2012
        //The connection between Product and Company is updated with the new value.
        updateConnection(context,strObjectId,ProductLineConstants.RELATIONSHIP_PRODUCT_COMPANY,strNewValue,false);
        return 0;
    }

    /**
    * This method is invoked, when the Model to which the Product is associated with is changed.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - Has the packed Hashmap having information of the object in context.
    * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public int updateModel(Context context, String[] args) throws Exception
    {
        //HashMap is defined to retrieve the arguments sent by the form after unpacking.
        HashMap programMap     = (HashMap) JPO.unpackArgs(args);
        //HashMap is defined to retrieve another HashMap in the unpacked list, that has the object information
        HashMap paramMap       = (HashMap) programMap.get("paramMap");
        //Object Id of the context object is obtained from the Map.
        String  strObjectId    = (String) paramMap.get("objectId");
        //The new object id of the Model that has to be used to connect with the product in context
        String  strNewValue    = (String) paramMap.get("New Value");
        //The connection between Product and Model is updated with the new value.
        updateConnection(context,strObjectId,ProductLineConstants.RELATIONSHIP_PRODUCTS,strNewValue,false);
        return 0;
    }

    /**
    * This method is invoked, when the Design Responsibility to which the Product is associated with is changed.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - Has the packed Hashmap having information of the object in context.
    * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public int updateDesignResponsibility(Context context, String[] args) throws Exception
    {
        //HashMap is defined to retrieve the arguments sent by the form after unpacking.
        HashMap programMap     = (HashMap) JPO.unpackArgs(args);
        //HashMap is defined to retrieve another HashMap in the unpacked list, that has the object information
        HashMap paramMap       = (HashMap) programMap.get("paramMap");
        //Object Id of the context object is obtained from the Map.
        String  strObjectId    = (String) paramMap.get("objectId");
        //The new object id of the organization that has to be used to connect with the product in context
        String  strNewValue    = (String) paramMap.get("New Value");
        //The connection between Product and organization is updated with the new value.
        updateConnection(context,strObjectId,ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,strNewValue,false);
        return 0;
    }

    /**
    * This method is used to edit the Governing Project of the Product from the
     * Edit Details page. If the selected Project is already connected to the
     * Product then it is connected to the Product by Governing Project
     * Relationship but if it is not connected to Product then first it is
     * connectded to the Product by Related Projects relationship and then by
     * Governing Project relationship. If Product already has a Governing
     * Project then it is disconnected.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - Has the packed Hashmap having information of the object in context.
    * @return int - Returns 0 in case of Check trigger is success and 1 in case of failure
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public int updateGoverningProject(Context context, String[] args) throws Exception
    {
        //HashMap is defined to retrieve the arguments sent by the form after unpacking.
        HashMap programMap     = (HashMap) JPO.unpackArgs(args);
        //HashMap is defined to retrieve another HashMap in the unpacked list, that has the object information
        HashMap paramMap       = (HashMap) programMap.get("paramMap");
        //Object Id of the context object is obtained from the Map.
        String  strObjectId    = (String) paramMap.get("objectId");
        //The new object id of the Project space that has to be used to connect with the product in context
        String  strNewValue    = (String) paramMap.get("New OID");
        if(strNewValue==null)
        {
        	  strNewValue    = (String) paramMap.get("New Value");
        }

      //Start of Add by Enovia MatrixOne for Bug # 311803 on 09-Dec-05
        String strOldProjectId = "";
        String strObjID = (String) paramMap.get("objectId");
        DomainObject domObj = DomainObject.newInstance(context, strObjID);
        StringList slBusTypes = new StringList();
        slBusTypes.addElement(DomainConstants.SELECT_ID);

        Map mGoverningProject = domObj.getRelatedObject(context,
                                                        ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT,
                                                        true,
                                                        slBusTypes,
                                                        null
                                                        );
        if (!((mGoverningProject == null) || (mGoverningProject.equals(null))) )
            {
                String strGovProj = (mGoverningProject.get(DomainConstants.SELECT_ID)).toString();
                strOldProjectId = strGovProj;
            }
        else {
               strOldProjectId = (String) paramMap.get("Old OID");
             }

    boolean bHasReadAccess;

    if( (strOldProjectId==null)||strOldProjectId.equals(null)||strOldProjectId.equals("") )
    {
        bHasReadAccess = true;
    }
    else
    {
        bHasReadAccess = emxProduct_mxJPO.hasAccessOnProject(context,strOldProjectId);
    }

    if (bHasReadAccess) //if the context user has access n the Project, then operations performed else nothing done.
    {
        //End of Add by Enovia MatrixOne for Bug # 311803 on 09-Dec-05
        //Begin of Add by Vibhu,Enovia MatrixOne for Bug 311803 on 11/23/2005
        if (strNewValue == null || "".equalsIgnoreCase(strNewValue) || "null".equalsIgnoreCase(strNewValue))
        {
            DomainObject domProduct = new DomainObject(strObjectId);
            String strProjRelId = (String) domProduct.getInfo(context,"from["+ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT+"].id");
            if(strProjRelId!=null && !"".equalsIgnoreCase(strProjRelId) && !"null".equalsIgnoreCase(strProjRelId))
            {
                DomainRelationship.disconnect(context,strProjRelId);
            }
            return 0;
        }
        //End of Add by Vibhu,Enovia MatrixOne for Bug 311803 on 11/23/2005
        //Check whether this Project is connected to the Product by Related
        // Project relationship
        DomainObject domProject = DomainObject.newInstance(
                                                           context,
                                                           strNewValue);

        //Form the object select
        StringBuffer sbProductSelect = new StringBuffer();
        sbProductSelect.append("to[");
        sbProductSelect
                .append(ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS);
        sbProductSelect.append("].from.");
        sbProductSelect.append(DomainConstants.SELECT_ID);
        //Get all the Products which are connected to this Project by Related
        // Projects relationship
        List slProducts = (StringList) domProject
                .getInfoList(
                             context,
                             sbProductSelect.toString());

        ContextUtil.startTransaction(
                                     context,
                                     true);
        try {
            //Check whether context Product is present in the List
            if (slProducts == null || slProducts.isEmpty()
                    || !slProducts.contains(strObjectId)) {
                //If this Project is not connected to the context product by
                // Related Projects relationship then connect it first
                String[] arrProjIds = new String[1];
                arrProjIds[0] = strNewValue;
                DomainObject domProduct = DomainObject.newInstance(
                                                           context,
                                                           strObjectId);
                domProduct.addFromObjects(
                                        context,
                                        new RelationshipType(
                                              ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS),
                                        arrProjIds);
            }

           //The connection between Product and Project space is updated with the new value.
           updateConnection(context,strObjectId,ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT,strNewValue,true);
           ContextUtil.commitTransaction(context);
           return 0;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw new FrameworkException(e.getMessage());
        }
    //Start of Add by Enovia MatrixOne for Bug # 311803 on 09-Dec-05
    }//end of IF for bHasReadAccess check
    else
    {
        return 0;
    }
    //End of Add by Enovia MatrixOne for Bug # 311803 on 09-Dec-05

    }

    /**
    * This method is invoked internally to update the connetion between the Product and any related object that gets modified.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param parentObjectId - Object id of the context object, in this case it is Product
    * @param strRelationshipName - Relationship name that is getting modified.
    * @param strNewObjectId - The new object id that is to be connected with the above mentioned relationship, after replacing the old one.
    * @param bIsFrom - Denotes if the new object id mentioned is to be connected in the from side or the to side.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    protected void updateConnection(Context context, String parentObjectId, String strRelationshipName, String strNewObjectId, boolean bIsFrom) throws Exception
    {
        //Relationship selects is formulated to retrieve the relationship id between Product and the concerned object.
        StringList relSelect = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Context set with the product id
        setId(parentObjectId);
        //Relationship id of the previous object connected with product is fetched for disconnecting it.
        Map objectMap = getRelatedObject(context,strRelationshipName,bIsFrom,null,relSelect);
        if(objectMap != null)
        {
          //The relationship id of the existing connection is retrieved.
          String strRelId = (String)objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
          //The existing relationship is disconnected.
          DomainRelationship.disconnect(context,strRelId);
        }
        //Function loop to avoid the connection from being established if no objects id is being passed.
        if (   !( strNewObjectId.equals("")  ||  strNewObjectId.equals("null")  || strNewObjectId == null ))
        {
          //Business Object instantiated with the new object id, to connect it with the Product
          BusinessObject tempBO = new BusinessObject(strNewObjectId);
          //The new object id is connected to the context product id
          connect(context,new RelationshipType(strRelationshipName),bIsFrom,tempBO);
        }

    }

    /**
    * Method to get the name of the Owner in last name, first name format.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - Has the packed Hashmap having information of the object in context.
    * @return StringList - StringList of owner names in Last Name, First Name format
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */

    public StringList getNameForOwner(Context context, String[] args) throws Exception
    {
     try
        {
            //HashMap is defined to retreive the information that is packed and sent to the JPO
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            //Maplist is defined to retreive the object information sent across from the Table.
            MapList relBusObjPageList = (MapList)programMap.get("objectList");
            //Array containing object ids  of the objects in the table
            String arrObjId[] = new String[relBusObjPageList.size()];
            //Processing for each of the object id frmo the table
            for (int i = 0; i < relBusObjPageList.size(); i++)
            {
                //The object is retreived from the MapList, this can be either as Hashtable or HashMap
                Object obj = relBusObjPageList.get(i);
                //Check is made to check the instance type of the object and the value is obtained from it accordingly.
                if (obj instanceof HashMap)
                {
                    arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                }
                else if (obj instanceof Hashtable)
                {
                    arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                }
            }

            //HashSet to contain the owner name
            HashSet ownerSet = new HashSet();
            //String to contain the owner name
            String strOwner = null;
            //String list containing the formatted names (this will be returned)
            StringList slFormattedNames = new StringList();
            //HashMap containing the ids of owners
            HashMap ownerMap = new HashMap();
            //Object selects formulated to get the id.
            StringList busSelects = new StringList(DomainConstants.SELECT_ID);
            //String containing the owner(person) object id
            String strPersonId = null;
            //StringBuffer for the formatted name
            StringBuffer sbFormattedName = new StringBuffer();

      StringList listSelect = new StringList(1);
      String strAttrb1 = DomainConstants.SELECT_OWNER;
      listSelect.addElement(strAttrb1);

      //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
      BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

            //Traversing through the object ids to get the firstname and lastname
            for (int j = 0; j < relBusObjPageList.size(); j++)
            {
                //Owner of the context object is obtained.
                strOwner = attributeList.getElement(j).getSelectData(strAttrb1);
                //The owner set or list is updated with the value of Owner obtained.
                ownerSet.add(strOwner);
                //The id of the Person object, based on the Owner name is obtained.
                ownerMap = (HashMap)Person.getPersonsFromNames(context,ownerSet,busSelects);
                //Id of the the person object is obtained if the ownerMap is not empty
                if(!ownerMap.isEmpty())
                {
                    strPersonId = (String)((HashMap)ownerMap.get(strOwner)).get(DomainConstants.SELECT_ID);
                    //Setting the context to the person's id
                    setId(strPersonId);
                    //Forming the formatted name
                    sbFormattedName = sbFormattedName.append(getAttributeValue(context,ProductLineConstants.ATTRIBUTE_LAST_NAME));
                    sbFormattedName = sbFormattedName.append(", ");
                    sbFormattedName = sbFormattedName.append(getAttributeValue(context,ProductLineConstants.ATTRIBUTE_FIRST_NAME));
                    //Clearing the owner set
                    ownerSet.clear();
                }
                //Adding the formatted name to the return stringlist
                slFormattedNames.addElement(sbFormattedName.toString());
                //Clearing the stringbuffer
                sbFormattedName.delete(0,sbFormattedName.length());
            }
            //Returning the formatted name
            return slFormattedNames;
        }catch(Exception e)
        {
            e.printStackTrace();
            throw new FrameworkException(e);
        }
    }

    /**
     * This column JPO method is used to get the name of the Current Phase of
     * the Governing Project of the product.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap with following information:
     *        objectList - List of object ids of Product in the table
     * @return List
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    public List getCurrentPhase(
                                Context context,
                                String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        //Get the reportformat for removeing the hyperlink in export
        //mode
        HashMap paramList = (HashMap)programMap.get("paramList");
        String strReportFormat = (String) paramList.get("reportFormat");

        List lstPhaseList = (MapList)currentPhaseProcess(
                                                    context,
                                                    args,
                                                    DomainConstants.SELECT_NAME);
        List lstNameList = new Vector();
        StringBuffer sbHref = new StringBuffer();
        //Added by Vibhu,Enovia MatrixOne for bug 311803 on 11/22/2005
        String strProjectId;
        boolean bhasAccess;
        for(int i=0;i<lstPhaseList.size();i++){
            //Added by Vibhu,Enovia MatrixOne for bug 311803 on 11/22/2005
            bhasAccess = false;
            strProjectId = DomainConstants.EMPTY_STRING;
            String strPhaseName = (String)((Map)lstPhaseList.get(i)).
                                                  get(DomainConstants.SELECT_NAME);
            if(strPhaseName!=null&&
                !strPhaseName.equals(DomainConstants.EMPTY_STRING)&&
                !strPhaseName.equalsIgnoreCase("null")){
                //Added by Vibhu,Enovia MatrixOne for Bug 311803 on 11/22/2005
                strProjectId = (String)((Map)lstPhaseList.get(i)).get(DomainConstants.TYPE_PROJECT_SPACE);


               try
                          {

                            bhasAccess = hasAccessOnProject(context,strProjectId);

                          }
                          catch (Exception e)

                          {


                          bhasAccess = false;
               }
                sbHref = new StringBuffer();
                if(!bhasAccess || (strReportFormat!=null
                        &&!strReportFormat.equals("null")
                        &&!strReportFormat
                            .equals(DomainConstants.EMPTY_STRING))){
                    sbHref.append(strPhaseName);
                }else{
                    sbHref.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                    sbHref.append(XSSUtil.encodeForHTMLAttribute(context,(String)((Map)lstPhaseList.get(i))
                                            .get(DomainConstants.SELECT_ID)));
                    sbHref.append("&amp;mode=replace");
                    sbHref.append("&amp;AppendParameters=true");
                    sbHref.append("&amp;reloadAfterChange=true");
                     sbHref.append("')\">");            
                    sbHref.append(XSSUtil.encodeForHTML(context,strPhaseName));
                    sbHref.append("</A>");
                }
                lstNameList.add(sbHref.toString());
            }else{
                lstNameList.add(DomainConstants.EMPTY_STRING);
            }
        }
        return lstNameList;
    }

    /**
     * This column JPO method is used to get the Phase Completion date of the
     * Current Phase of the Governing Project of the product.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap with following information:
     *        objectList - List of object ids of Product in the table
     * @return List
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    public List getPhaseCompletion(
                                   Context context,
                                   String[] args) throws Exception {
        List lstPhaseList = (MapList)currentPhaseProcess(
                                                               context,
                                                               args,
                                                               "date");
        List lstDateList = new Vector();
        for(int i=0;i<lstPhaseList.size();i++){
            lstDateList.add((String)((Map)lstPhaseList.get(i)).get("date"));
        }
        return lstDateList;
    }

    /**
     * This method is used to retrieve perticular details the current phase of
     * the Governing Project of the Product. The current phase of Project is
     * decided as follows:
     * 1.If all the tasks under the Governing Project are in
     * Complete State then the Task with last "Actual Finish Date" will be the
     * Current Phase of the Project. In ideal case if the Task is finished then
     * it's Actual Finish Date should not be empty or null but if such situation
     * arises then complete task with maximum Actual Finish Date which is not
     * null will be the current phase.If Actual finish date is null for all the
     * Tasks then empty string is returned.
     * 2.If there are some incomplete tasks under then incomplete task with the
     * earliest "Estimated Finish Date" will be the current phase.In ideal case
     * Estimated Finish Date for any Task should not be empty or null but if
     * such situation arises then incomplete task with least Estimated Finish
     * Date which is not null will be the current phase.If Estimeted finish date
     * is null for all incomplete Tasks then empty string is returned.
     * 3.If there are no tasks under the Governing Project of the Product then
     * empty string is returned.
     * 4.If there is no Governing Project associated with the Product then blnk
     * string is returned.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap with following information:
     *        objectList - List of object ids of Product in the table
     * @param strSelect The actual name of attribute of the current phase that
     *             that has to be selected.
     *             The word "date" is reserved. If this passed then either actual or
     *             estimated finish date is selected.
     * @return List of Maps containing the following key value pair:
     *              strSelect - The required field of the current phase
     *              id - The object id of the current phase
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    protected List currentPhaseProcess(
                                       Context context,
                                       String[] args,
                                       String strSelect) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        List lstProdList = (MapList) programMap.get("objectList");

        String arrObjId[] = new String[lstProdList.size()];

        //Getting the bus ids for objects in the table
        for (int i = 0; i < lstProdList.size(); i++) {
            arrObjId[i] = (String) ((Map) lstProdList.get(i))
                    .get(DomainConstants.SELECT_ID);
        }

        //Form the select expression to get the Governing Project of the
        // Product
        StringBuffer sbGovProjSelect = new StringBuffer("from[");
        sbGovProjSelect
                .append(ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT);
        sbGovProjSelect.append("].to.id");

        List lstProdSelects = new StringList(sbGovProjSelect.toString());

        //Get the Governing Project for all the Products
        ContextUtil.pushContext(context);
        BusinessObjectWithSelectList lstProductData = BusinessObject
                .getSelectBusinessObjectData(
                                             context,
                                             arrObjId,
                                             (StringList) lstProdSelects);

        String strGoverningProject = DomainConstants.EMPTY_STRING;
        List lstReturnList = new MapList();

        List lstTaskObjSelects = new StringList();
        lstTaskObjSelects.add(DomainConstants.SELECT_ID);
        lstTaskObjSelects.add(DomainConstants.SELECT_NAME);
        lstTaskObjSelects.add(SELECT_TASK_ACTUAL_FINISH_DATE);
        lstTaskObjSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
        lstTaskObjSelects.add(DomainConstants.SELECT_CURRENT);
        lstTaskObjSelects.add(DomainConstants.SELECT_POLICY);

        StringBuffer sbRelPattern = new StringBuffer();
        sbRelPattern.append(DomainConstants.RELATIONSHIP_SUBTASK);
        sbRelPattern.append(",");
        sbRelPattern.append(DomainConstants.RELATIONSHIP_DELETED_SUBTASK);

        List lstTasksList = new MapList();
        List lstCompleteTasksList = null;
        List lstIncompleteTasksList = null;

        for (int i = 0; i < lstProdList.size(); i++) {
    //Begin of add by Enovia MatrixOne for bug #301812, 04/04/2005
        Map returnMap = new HashMap();
        //End of add by Enovia MatrixOne for bug #301812, 04/04/2005
            strGoverningProject = lstProductData
                    .getElement(i).getSelectData(sbGovProjSelect.toString());

            if (strGoverningProject != null
                    && !strGoverningProject
                            .equals(DomainConstants.EMPTY_STRING)
                    && !strGoverningProject.equalsIgnoreCase("null")) {
                //Added by Vibhu,Enovia MatrixOne for Bug 311803 on 11/22/2005
                returnMap.put(DomainConstants.TYPE_PROJECT_SPACE,strGoverningProject);
                //Get all the tasks connected to this Project by Subtask
                // relationship
                DomainObject domProject = DomainObject
                        .newInstance(
                                     context,
                                     strGoverningProject);
                lstTasksList = (MapList)domProject
                        .getRelatedObjects(
                                           context,
                                           sbRelPattern.toString(),
                                           DomainConstants.QUERY_WILDCARD,
                                           (StringList)lstTaskObjSelects,
                                           null,
                                           true,
                                           true,
                                           (short) 1,
                                           DomainConstants.EMPTY_STRING,
                                           DomainConstants.EMPTY_STRING);

                lstCompleteTasksList = new MapList();
                lstIncompleteTasksList = new MapList();
                //Devide the Tasks into Complete and Incomplete Tasks
                for (int j = 0; j < lstTasksList.size(); j++) {
                    String strCompleteState = FrameworkUtil
                            .lookupStateName(
                                             context,
                                             (String) ((Map) lstTasksList
                                                     .get(j))
                                                     .get(DomainConstants.SELECT_POLICY),
                                             "state_Complete");
                    if (strCompleteState.equals((String) ((Map) lstTasksList
                            .get(j)).get(DomainConstants.SELECT_CURRENT))) {
                        lstCompleteTasksList.add((Map) lstTasksList.get(j));
                    } else {
                        lstIncompleteTasksList.add((Map) lstTasksList.get(j));
                    }
                }
                //if the incomplete tasks list is not empty then the current
                // phase of the Project will be the task having earliest
                // "Estimeted Finish Date".
                if (lstIncompleteTasksList != null
                        && !lstIncompleteTasksList.isEmpty()) {
                    //sort the incomplete tasks list in ascending order of
                    // Estimeted finish date
                    int index = getMinDatekey(
                                              (MapList) lstIncompleteTasksList,
                                              SELECT_TASK_ESTIMATED_FINISH_DATE);
                    if (index != -1) {
                        if (strSelect.equals("date")) {
                            returnMap.put(strSelect,
                                                (String) ((Map) lstIncompleteTasksList
                                                .get(index))
                                                .get(SELECT_TASK_ESTIMATED_FINISH_DATE));
                        } else {
                            returnMap
                                    .put(strSelect,
                                           (String) ((Map) lstIncompleteTasksList
                                            .get(index)).get(strSelect));
                        }
                        returnMap.put(DomainConstants.SELECT_ID,
                                         (String) ((Map) lstIncompleteTasksList
                                            .get(index)).get(DomainConstants.SELECT_ID));
                    } else {
                        returnMap.put(strSelect,
                                             DomainConstants.EMPTY_STRING);
                    }
                    lstReturnList.add(returnMap);
                } else if (lstCompleteTasksList != null
                        && !lstCompleteTasksList.isEmpty()) {
                    //if the incomplete tasks list is empty and complete tasks
                    // list is not empty then the current
                    // phase of the Project will be the task having last
                    // "Actual Finish Date".
                    //sort the complete tasks list in descending order of
                    // Actual finish date
                    int index = getMaxDatekey(
                                              (MapList) lstCompleteTasksList,
                                              SELECT_TASK_ACTUAL_FINISH_DATE);
                    if (index != -1) {
                        //The first tasks in the maplist is the current phase
                        if (strSelect.equals("date")) {
                            returnMap.put(strSelect,
                                                (String) ((Map) lstCompleteTasksList
                                                .get(index))
                                                .get(SELECT_TASK_ACTUAL_FINISH_DATE));
                        } else {
                            returnMap
                                    .put(strSelect,
                                           (String) ((Map) lstCompleteTasksList
                                            .get(index)).get(strSelect));
                        }
                        returnMap.put(DomainConstants.SELECT_ID,
                                         (String) ((Map) lstCompleteTasksList
                                            .get(index)).get(DomainConstants.SELECT_ID));
                    } else {
                        returnMap.put(strSelect,
                                             DomainConstants.EMPTY_STRING);
                    }
                    lstReturnList.add(returnMap);
                } else {
                    returnMap.put(strSelect,
                                             DomainConstants.EMPTY_STRING);
                    lstReturnList.add(returnMap);
                }

            } else {
                //If the Product doesn't have a Governing Project then return
                // Empty String
                    returnMap.put(strSelect,
                                             DomainConstants.EMPTY_STRING);
                    lstReturnList.add(returnMap);
            }
        }
    ContextUtil.popContext(context);
    return lstReturnList;

    }

    /**
     * This method is used to get the index of the Map in a MapList for which
     * the value of the passed key is maximum amoung the other maps in the list.
     * The passed key should contain the date string in ematrix format.
     *
     * @param lstMaps The input maplist.
     * @param strKey The key.
     * @return The index of the map.
     *         -1 - If all the Maps doesn't contain the passed key.
     * @throws ParseException If the passed key contains string which is not in
     *          ematrix date format.
     * @since ProductCentral10.6
     */
    private int getMaxDatekey(
                              MapList lstMaps,
                              String strKey) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);

        String strMaxDate = (String) ((Map) lstMaps.get(0)).get(strKey);
        int iMaxKeyIndex = 0;
        boolean bNotNull = false;

        for (int i = 0; i < lstMaps.size(); i++) {
            String strTaskDate = (String) ((Map) lstMaps.get(i)).get(strKey);
            if (strTaskDate != null&& !strTaskDate
                            .equals(DomainConstants.EMPTY_STRING)
                    && !strTaskDate.equalsIgnoreCase("null")) {
                bNotNull = true;
                Date dtTaskDate = dateFormat.parse(strTaskDate);
                if (strMaxDate != null&& !strMaxDate
                            .equals(DomainConstants.EMPTY_STRING)
                    && !strMaxDate.equalsIgnoreCase("null")) {
                    Date dtMaxDate = dateFormat.parse(strMaxDate);
                    if (dtTaskDate.after(dtMaxDate)) {
                        strMaxDate = strTaskDate;
                        iMaxKeyIndex = i;

                    }
                } else {
                    strMaxDate = strTaskDate;
                    iMaxKeyIndex = i;
                }
            }
        }
        if (bNotNull) {
            return iMaxKeyIndex;
        } else {
            return -1;
        }
    }

    /**
     * This method is used to get the index of the Map in a MapList for which
     * the value of the passed key is minimum amoung the other maps in the list.
     * The passed key should contain the date string in ematrix format.
     *
     * @param lstMaps The input maplist.
     * @param strKey The key.
     * @return The index of the map.
     *         -1 - If all the Maps doesn't contain the passed key.
     * @throws ParseException If the passed key contains string which is not in
     *          ematrix date format.
     * @since ProductCentral10.6
     */
    private int getMinDatekey(
                              MapList lstMaps,
                              String strKey) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);

        String strMinDate = (String) ((Map) lstMaps.get(0)).get(strKey);
        int iMinKeyIndex = 0;
        boolean bNotNull = false;

        for (int i = 0; i < lstMaps.size(); i++) {
            String strTaskDate = (String) ((Map) lstMaps.get(i)).get(strKey);
            if (strTaskDate != null&& !strTaskDate
                            .equals(DomainConstants.EMPTY_STRING)
                    && !strTaskDate.equalsIgnoreCase("null")) {
                bNotNull = true;
                Date dtTaskDate = dateFormat.parse(strTaskDate);
                if (strMinDate != null&& !strMinDate
                            .equals(DomainConstants.EMPTY_STRING)
                    && !strMinDate.equalsIgnoreCase("null")) {
                    Date dtMaxDate = dateFormat.parse(strMinDate);
                    if (dtTaskDate.before(dtMaxDate)) {
                        strMinDate = strTaskDate;
                        iMinKeyIndex = i;
                    }
                } else {
                    strMinDate = strTaskDate;
                    iMinKeyIndex = i;
                }
            }
        }
        if (bNotNull) {
            return iMinKeyIndex;
        } else {
            return -1;
        }
    }

  /**
     * This column JPO method is used to get the Governing Project name of
     * the Product.
     *
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return List containing the hyperlinked names of Governing Project.
     * @throws Exception
     * @since ProductCentral10.6
     */
    public List getGoverningProject(
                            Context context,
                            String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        List lstProdList = (MapList) programMap.get("objectList");

        //Get the reportformat for removeing the hyperlink in export
        //mode
        HashMap paramList = (HashMap)programMap.get("paramList");
        String strReportFormat = (String) paramList.get("reportFormat");

        String arrObjId[] = new String[lstProdList.size()];

        //Getting the bus ids for objects in the table
        for (int i = 0; i < lstProdList.size(); i++) {
            arrObjId[i] = (String) ((Map) lstProdList.get(i))
                    .get(DomainConstants.SELECT_ID);
        }

        //Form the select expression to get the Governing Project of the
        // Product
        StringBuffer sbGovProjIdSelect = new StringBuffer("from[");
        sbGovProjIdSelect
                .append(ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT);
        sbGovProjIdSelect.append("].to.");
        sbGovProjIdSelect.append(DomainConstants.SELECT_ID);

        StringBuffer sbGovProjNameSelect = new StringBuffer("from[");
        sbGovProjNameSelect
                .append(ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT);
        sbGovProjNameSelect.append("].to.");
        sbGovProjNameSelect.append(DomainConstants.SELECT_NAME);

        List lstProdSelects = new StringList(sbGovProjIdSelect.toString());
        lstProdSelects.add(sbGovProjNameSelect.toString());

        //Get the Governing Project for all the Products

        ContextUtil.pushContext(context);
       BusinessObjectWithSelectList lstProductData = BusinessObject
                .getSelectBusinessObjectData(
                                             context,
                                             arrObjId,
                                             (StringList) lstProdSelects);

        ContextUtil.popContext(context);
        String strGoverningProjectId = DomainConstants.EMPTY_STRING;
        String strGoverningProjectName = DomainConstants.EMPTY_STRING;
        List lstGovProjList = new Vector();
        StringBuffer sbHref = new StringBuffer();
        //Added by Vibhu,Enovia MatrixOne for Bug 311803 on 11/22/2005
        boolean bhasAccess = false;

        for (int i = 0; i < lstProdList.size(); i++) {
            bhasAccess = false;
            strGoverningProjectId = lstProductData
                    .getElement(i).getSelectData(sbGovProjIdSelect.toString());
            strGoverningProjectName = lstProductData
                    .getElement(i).getSelectData(sbGovProjNameSelect.toString());

            if (strGoverningProjectId != null
                    && !strGoverningProjectId
                            .equals(DomainConstants.EMPTY_STRING)
                    && !strGoverningProjectId.equalsIgnoreCase("null")) {
                sbHref = new StringBuffer();
                //Modified by Vibhu,Enovia MatrixOne for bug 311803 on 11/22/2005
               try
               {

                bhasAccess = hasAccessOnProject(context,strGoverningProjectId);

               }
               catch (Exception e)

               {

               bhasAccess = false;
               }

    //Added By Sandeep, Enovia MatrixOne for Bug # 301408
                String strLanguage = context.getSession().getLanguage();
                String strTipGoverningProj = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.ToolTip.GoverningProject",strLanguage);
                StringBuffer sbGoverningIconTag = new StringBuffer();
                sbGoverningIconTag.append("<img src=\"../common/images/iconSmallProject.gif\" border=\"0\" alt=\"");
                sbGoverningIconTag.append(strTipGoverningProj);
                sbGoverningIconTag.append("\" /> ");
    //End of Add By Sandeep, Enovia MatrixOne for Bug # 301408

                if(!bhasAccess || (strReportFormat!=null
                        &&!strReportFormat.equals("null")
                        &&!strReportFormat
                            .equals(DomainConstants.EMPTY_STRING))){
                    sbHref.append(sbGoverningIconTag);
                    sbHref.append("<p>");
                    sbHref.append(strGoverningProjectName);
                    sbHref.append("</p>");
                }else{
    //Added By Sandeep, Enovia MatrixOne for Bug # 301408
                    sbHref.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                    sbHref.append(strGoverningProjectId);
                    sbHref.append("&mode=replace");
                    sbHref.append("&AppendParameters=true");
                    sbHref.append("&reloadAfterChange=true");
                    sbHref.append("')\">");
                    sbHref.append(sbGoverningIconTag);
                    sbHref.append("</A>");
    //End of Add By Sandeep, Enovia MatrixOne for Bug # 301408

                    sbHref.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                    sbHref.append(strGoverningProjectId);
                    sbHref.append("&mode=replace");
                    sbHref.append("&AppendParameters=true");
                    sbHref.append("&reloadAfterChange=true");
                    sbHref.append("')\">");
                    sbHref.append(strGoverningProjectName);
                    sbHref.append("</A>");
                }
                lstGovProjList.add(sbHref.toString());
            }else{
                lstGovProjList.add(DomainConstants.EMPTY_STRING);
            }
        }
        return lstGovProjList;
    }


    /**
     * This method is used to get the selected Products and return their Ids.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList - MapList containing the id of latest Product objects
     *         connected with Model.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getProductsForRoadmap(
                                         Context context,
                                         String args[]) throws Exception {
        //The packed argument send from the JPO invoke method is unpacked to
        // retrive the HashMap.
        Map programMap = (HashMap) JPO.unpackArgs(args);
        //The RequestValuesMap is got through programMap.
        Map requestValuesMap = (HashMap) programMap.get("RequestValuesMap");

        //Getting the Selected Product Ids
        String[] strProductIds = (String[]) requestValuesMap
                .get("emxTableRowId");
        //Calling the method to separate the ObjectIds from Relationship Ids.
        Map reqMap = ProductLineUtil.getObjectIdsRelIds(strProductIds);
        //Getting the selected Object Ids into an array.
        String[] strSourceObjectId = (String[]) reqMap.get("ObjId");

        //Initializing the MapList where the ids to be returned to the
        // emxTable.jsp are stored.
        MapList mlSelectedProductIds = new MapList();
        //Temporary variables

        Object obTemp = null;

        //Getting each Object Id from String Array and storing them in the
        // MapList object.
        for (int i = 0; i < strSourceObjectId.length; i++) {
            HashMap hmTemp = new HashMap();
            obTemp = hmTemp.put(
                                DomainConstants.SELECT_ID,
                                strSourceObjectId[i]);
            mlSelectedProductIds.add(
                                     i,
                                     hmTemp);
        }
        return mlSelectedProductIds;
    }

    /**
     * Method to get the Label for the Product Roadmap Table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap having information of the object in
     *        context.
     * @return StringList - StringList of owner names in Last Name, First Name
     *         format
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    public List getLabel1(
                          Context context,
                          String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        List lstProductList = (MapList) programMap.get("objectList");

        //Begin of Add by Enovia MatrixOne for Bug 304312 on 5/18/2005
        Map paramMap = (Map) programMap.get("paramList");
        final Locale localeObj = (Locale) paramMap.get("localeObj");
        final double dblTZOffset = (new Double((String) paramMap.get("timeZone"))).doubleValue();
        //End of Add by Enovia MatrixOne for Bug 304312 on 5/18/2005

        String strRangeLabel = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Roadmap_Label.Label_1","");

        return getDisplayLabel(
                               context,
                               lstProductList,
                               strRangeLabel,localeObj,dblTZOffset);

    }

    /**
     * Method to get the Label for the Product Roadmap Table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap having information of the object in
     *        context.
     * @return StringList - StringList of owner names in Last Name, First Name
     *         format
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    public List getLabel2(
                          Context context,
                          String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        List lstProductList = (MapList) programMap.get("objectList");

        //Begin of Add by Enovia MatrixOne for Bug 304312 on 5/18/2005
        Map paramMap = (Map) programMap.get("paramList");
        final Locale localeObj = (Locale) paramMap.get("localeObj");
        final double dblTZOffset = (new Double((String) paramMap.get("timeZone"))).doubleValue();
        //End of Add by Enovia MatrixOne for Bug 304312 on 5/18/2005
        String strRangeLabel = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Roadmap_Label.Label_2","");

        return getDisplayLabel(
                               context,
                               lstProductList,
                               strRangeLabel,localeObj,dblTZOffset);
    }

    /**
     * Method to get the Label for the Product Roadmap Table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap having information of the object in
     *        context.
     * @return StringList - StringList of owner names in Last Name, First Name
     *         format
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    public List getLabel3(
                          Context context,
                          String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        List lstProductList = (MapList) programMap.get("objectList");

        //Begin of Add by Enovia MatrixOne for Bug 304312 on 5/18/2005
        Map paramMap = (Map) programMap.get("paramList");
        final Locale localeObj = (Locale) paramMap.get("localeObj");
        final double dblTZOffset = (new Double((String) paramMap.get("timeZone"))).doubleValue();
        //End of Add by Enovia MatrixOne for Bug 304312 on 5/18/2005
        String strRangeLabel = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Roadmap_Label.Label_3","");

        return getDisplayLabel(
                               context,
                               lstProductList,
                               strRangeLabel,localeObj,dblTZOffset);
    }

    /**
     * Method to get the Label for the Product Roadmap Table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap having information of the object in
     *        context.
     * @return StringList - StringList of owner names in Last Name, First Name
     *         format
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    public List getLabel4(
                          Context context,
                          String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        List lstProductList = (MapList) programMap.get("objectList");

        //Begin of Add by Enovia MatrixOne for Bug 304312 on 5/18/2005
        Map paramMap = (Map) programMap.get("paramList");
        final Locale localeObj = (Locale) paramMap.get("localeObj");
        final double dblTZOffset = (new Double((String) paramMap.get("timeZone"))).doubleValue();
        //End of Add by Enovia MatrixOne for Bug 304312 on 5/18/2005
        String strRangeLabel = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Roadmap_Label.Label_4","");

        return getDisplayLabel(
                               context,
                               lstProductList,
                               strRangeLabel,localeObj,dblTZOffset);
    }

    /**
     * Method to get the Label for the Product Roadmap Table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap having information of the object in
     *        context.
     * @return StringList - StringList of owner names in Last Name, First Name
     *         format
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

    public List getDisplayLabel(
                                Context context,
                                List lstProductList,
                                String strRangeLabel,Locale localeObj, double dblTZOffset) throws Exception {

        //Form the Relationship selects list
        List lstRelSelects = new StringList();
        lstRelSelects
                .add(ProductLineConstants.SELECT_ATTRIBUTE_REGION);
        lstRelSelects
                .add(ProductLineConstants.SELECT_ATTRIBUTE_SEASON);
        lstRelSelects
                .add(ProductLineConstants.SELECT_ATTRIBUTE_SEGMENT);

        //Form the object selects list
        List lstObjSelects = new StringList(DomainConstants.SELECT_ID);
        lstObjSelects.add(DomainConstants.SELECT_NAME);
        lstObjSelects.add(SELECT_TASK_ACTUAL_FINISH_DATE);
        lstObjSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);

        //Form the where expression
        StringBuffer sbRelWhereExp = new StringBuffer();
        sbRelWhereExp
                .append(ProductLineConstants.SELECT_ATTRIBUTE_ROADMAP_LABEL);
        sbRelWhereExp.append("==");
        sbRelWhereExp.append("'");
        sbRelWhereExp.append(strRangeLabel);
        sbRelWhereExp.append("'");

        DomainObject domProduct = new DomainObject();
        List lstTaskList = new MapList();
        List lstLabels = new Vector();
        StringBuffer sbReturnString = null;
        String strSelectValue = EMPTY_STRING;
        for (int i = 0; i < lstProductList.size(); i++) {
            domProduct.setId((String)((Map) lstProductList.get(i)).get(DomainConstants.SELECT_ID));
            lstTaskList = (MapList) domProduct
                    .getRelatedObjects(
                                       context,
                                       ProductLineConstants.RELATIONSHIP_ROADMAP_TASK,
                                       WILD_CHAR,
                                       (StringList) lstObjSelects,
                                       (StringList) lstRelSelects,
                                       false,
                                       true,
                                       (short) 1,
                                       null,
                                       sbRelWhereExp.toString());
            if (lstTaskList != null && !lstTaskList.isEmpty()) {
                //Begin of add by Enovia MatrixOne for Bug# 302972 on 25 Apr 05
                sbReturnString = new StringBuffer();
                for(int j=0;j<lstTaskList.size();j++){
                //End of add by Enovia MatrixOne for Bug# 302972 on 25 Apr 05
                //Begin of modify by Enovia MatrixOne for Bug# 302972 on 25 Apr 05
                    String strTaskName = (String) ((Map) lstTaskList.get(j))
                            .get(DomainConstants.SELECT_NAME);
                    String strTaskEstDate = (String) ((Map) lstTaskList.get(j))
                            .get(SELECT_TASK_ESTIMATED_FINISH_DATE);
                    String strTaskActDate = (String) ((Map) lstTaskList.get(j))
                            .get(SELECT_TASK_ACTUAL_FINISH_DATE);
                    String strTaskId = (String) ((Map) lstTaskList.get(j))
                            .get(DomainConstants.SELECT_ID);
                    sbReturnString.append(strTaskName);
                    sbReturnString.append("\n (");
                    /*Begin of Modify:Raman,Enovia MatrixOne for Bug#302847 on 4/21/2005*/
                    String strAttrRegion = (String) ((Map) lstTaskList.get(j))
                                .get(ProductLineConstants.SELECT_ATTRIBUTE_REGION);
                    String strAttrSeason = (String) ((Map) lstTaskList.get(j))
                                .get(ProductLineConstants.SELECT_ATTRIBUTE_SEASON);
                    String strAttrSegment = (String) ((Map) lstTaskList.get(j))
                                .get(ProductLineConstants.SELECT_ATTRIBUTE_SEGMENT);
                    //End of modify by Enovia MatrixOne for Bug# 302972 on 25 Apr 05
                    if (strAttrRegion != null
                                && !strAttrRegion.equals(EMPTY_STRING)
                                && !strAttrRegion.equalsIgnoreCase("null")) {
                            sbReturnString.append(strAttrRegion);
                            sbReturnString.append("|");
                        }
                    if (strAttrSeason != null
                                && !strAttrSeason.equals(EMPTY_STRING)
                                && !strAttrSeason.equalsIgnoreCase("null")) {
                            //Added by Vibhu,Enovia MatrixOne for Bug301154 on 5/3/2005
                            strAttrSeason = i18nNow.getRangeI18NString(ProductLineConstants.ATTRIBUTE_SEASON,
                                                                                  strAttrSeason,
                                                                                  context.getSession().getLanguage());
                            sbReturnString.append(strAttrSeason);
                            sbReturnString.append("|");
                        }
                    if (strAttrSegment != null
                                && !strAttrSegment.equals(EMPTY_STRING)
                                && !strAttrSegment.equalsIgnoreCase("null")) {
                            //Added by Vibhu,Enovia MatrixOne for Bug301154 on 5/3/2005
                            strAttrSegment = i18nNow.getRangeI18NString(ProductLineConstants.ATTRIBUTE_SEGMENT,
                                                              strAttrSegment,
                                                              context.getSession().getLanguage());
                            sbReturnString.append(strAttrSegment);
                            sbReturnString.append("|");
                        }
                    /*End of Add:Raman,Enovia MatrixOne for Bug#302847 on 4/21/2005*/
                    if (strTaskActDate != null
                            && !strTaskActDate.equals(EMPTY_STRING)
                            && !strTaskActDate.equalsIgnoreCase("null")){
                        //Added by Vibhu,Enovia MatrixOne for Bug301154 on 5/3/2005
                        strTaskActDate = getDisplayDate(context, strTaskActDate,localeObj,dblTZOffset);
                        sbReturnString.append(strTaskActDate);
                    }else if(strTaskEstDate != null
                            && !strTaskEstDate.equals(EMPTY_STRING)
                            && !strTaskEstDate.equalsIgnoreCase("null")){
                        //Added by Vibhu,Enovia MatrixOne for Bug301154 on 5/3/2005
                        strTaskEstDate = getDisplayDate(context, strTaskEstDate,localeObj,dblTZOffset);
                        sbReturnString.append(strTaskEstDate);
                    }
                    sbReturnString.append(")");
                    //Begin of add by Enovia MatrixOne for Bug# 302972 on 25 Apr 05
                    sbReturnString.append("<br></br>");
             }
            //End of add by Enovia MatrixOne for Bug# 302972 on 25 Apr 05
                lstLabels.add(sbReturnString.toString());
            }else{
                lstLabels.add(EMPTY_STRING);
            }
        }
        return lstLabels;
    }

    /**
     * This method returns all the Projects connected to the Product with
     * "Related Projects" relationship.
     *
     * @param context The ematrix context of the request.
     * @param args The packed arguments that contains the Map with following key
     *            value pair: objectId - Object id of the context product
     * @return MapList containing the information of projects connected to the
     *         Product
     * @throws Exception
     * @throws FrameworkException
     * @since ProductCentral10.6
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedProjects(Context context, String[] args)
    throws Exception, FrameworkException {
    	MapList lstProjectsList = new MapList();
		//Get the object id of the context Product and create a domain object
		Map mapPogram = (HashMap) JPO.unpackArgs(args);
		String strProductId = (String) mapPogram.get(STR_OBJECTID);
		
		if(null != strProductId && !"null".equals(strProductId) && !"".equals(strProductId)){
		DomainObject domProduct = DomainObject.newInstance(
		        context, strProductId);
		
		//Form the objects and relationship select list
		List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
		lstObjectSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
		lstObjectSelects.add(SELECT_BASELINE_CURRENT_END_DATE);
		lstObjectSelects.add(DomainConstants.SELECT_CURRENT);
		lstObjectSelects.add(DomainConstants.SELECT_TYPE);
		
		List lstRelSelects = new StringList(
		        DomainConstants.SELECT_RELATIONSHIP_ID);
		
		
		//Get the projects connected to the Product by "Related Projects"
		// relationship
		 lstProjectsList = domProduct.getRelatedObjects(
		        context,
		        ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS,
		        ProductLineConstants.TYPE_PROJECT_MANAGEMENT, (StringList) lstObjectSelects,
		        (StringList) lstRelSelects, false, true, (short) 1, null, null,0);
		}
		return lstProjectsList;
    }


    /**
     * This column JPO method is invoked while displaying the Projets list page
     * under products and is used to display the "Governing Project" icon.
     *
     * @param context The ematrix context of the request
     * @param args The packed arguments containing Map with following key value
     *            pairs: objectId - The objec id of the context product
     *            objectList - The maplist containing the related projects
     *            information
     * @return Vector containing the HTML tag to generate the Governing Project
     *         icon.
     * @throws Exception
     * @throws FrameworkException
     * @since ProductCentral10.6
     */
    public Vector getGoverningProjectIcon(Context context, String[] args)
            throws Exception, FrameworkException {
    	
        //Unpack the arguments
        Map mapProgram = (HashMap) JPO.unpackArgs(args);

        //Get the object id of the Product and related project list
        String strProductId = (String) ((Map) mapProgram.get("paramList"))
                .get(STR_OBJECTID);
        List lstProjectList = (MapList) mapProgram.get(STR_OBJECTLIST);

        //Get the governing project for the Product connected to it by
        // "Governing Project" relationship
        StringBuffer sbSelectGoverning = new StringBuffer();
        sbSelectGoverning.append("from[");
        sbSelectGoverning
                .append(ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT);
        sbSelectGoverning.append("].to.");
        sbSelectGoverning.append(DomainConstants.SELECT_ID);

        List lstObjectSelects = new StringList(sbSelectGoverning.toString());

        DomainObject domProduct = DomainObject.newInstance(
                context, strProductId);
        Map mapGoverningProject = domProduct.getInfo(
                context, (StringList) lstObjectSelects);
        String strGoverningProject = (String) mapGoverningProject
                .get(sbSelectGoverning.toString());

        Vector vtrGoverningProject = new Vector();

        //For the HTML tag to show the governing project icon
        String strLanguage = context.getSession().getLanguage();
        String strTipGoverningProj = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.ToolTip.GoverningProject",strLanguage);
        
        
        StringBuffer sbGoverningIconTag = new StringBuffer();
        sbGoverningIconTag
                .append("<img src=\"../common/images/iconSmallPrimary.gif\" border=\"0\" align=\"middle\" TITLE=\"");
        sbGoverningIconTag.append(XSSUtil.encodeForXML(context,strTipGoverningProj));
        sbGoverningIconTag.append("\"/>");

        for (int i = 0; i < lstProjectList.size(); i++) {
            if (strGoverningProject != null
                    && strGoverningProject
                            .equals((String) ((Map) lstProjectList.get(i))
                                    .get(DomainConstants.SELECT_ID))) {
                vtrGoverningProject.add(sbGoverningIconTag.toString());
            } else {
                vtrGoverningProject.add(DomainConstants.EMPTY_STRING);
            }
        }
        return vtrGoverningProject;
    }

    /**
     * This column JPO method is invoked while displaying the Projets list page
     * under products and is used to display the slip days icon.The logic to
     * display the icon is as follows: Green Icon - The Project is in Complete
     * State Red Icon - The Project is not complete and the difference between
     * the current system date and esteemated finish date is greater than
     * threshold specified in property file Yellow Icon - The Project is not
     * complete and the difference between the current system date and
     * esteemated finish date is less than or equal to the threshold specified
     * in property file. If the Baseline Current End Date is specified for the
     * project then it is taken into consideration to determine the slip days
     * otherwise Task Esteemated End Date is used.
     *
     * @param context The ematrix context of the request
     * @param args The packed arguments containing Map with following key value
     *            pairs: objectList - The maplist containing the related
     *            projects information
     * @return Vector containing the HTML tag to generate the Slip days icon.
     * @throws Exception
     * @throws FrameworkException
     * @since ProductCentral10.6
     */
    public Vector getSlipDaysIcon(Context context, String[] args)
            throws Exception, FrameworkException {
        //Unpack the arguments
        Map mapProgram = (HashMap) JPO.unpackArgs(args);

        //Get the object id of the Product and related project list
        List lstProjectList = (MapList) mapProgram.get(STR_OBJECTLIST);

        //Get the current system date for the comparision
        Date dtTmpDate = new Date();
        Date dtSysDate = new Date(dtTmpDate.getYear(), dtTmpDate.getMonth(),
                dtTmpDate.getDate());
        SimpleDateFormat sdfDateFormat = new SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);
        Date dtFinishDate = new Date();

        String strBlEndDate = STR_BLANK;
        String strEstEndDate = STR_BLANK;
        String strState = STR_BLANK;
        String strType = STR_BLANK;
        StringBuffer sbHtmlTag = null;
        Vector vtrSlipDays = new Vector();
        Map mapProject = new HashMap();
        long lDaysRemain = 0;

        //Get the yellow red threshold from the properties file
        int iThreshold = Integer
                .parseInt(EnoviaResourceBundle.getProperty(context,"emxProduct.Roadmap.SlipDaysThreshold"));
        //Get the actual name of the state complete
        String strStateComplete = FrameworkUtil
                .lookupStateName(
                        context, DomainConstants.POLICY_PROJECT_SPACE,
                        "state_Complete");

        //Get the tooltips to be displayed along with the slip days icon from
        // the property file
        String strLanguage = context.getSession().getLanguage();
        String strLegendOnTime =EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.ProjectStatus.OnTime",strLanguage);
        String strLegendLate = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.ProjectStatus.Late",strLanguage);
        String strLegendBehind = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.ProjectStatus.BehindSchedule",strLanguage);

        //Traverse through the project list and determine the slip days icon
        // for each project
        for (int i = 0; i < lstProjectList.size(); i++) {
            //Get the value of the attributes Estimated Finish Date & Baseline
            // Current Finish Date and current state and type of the project
            mapProject = (Map) lstProjectList.get(i);
            strBlEndDate = (String) mapProject
                    .get(SELECT_BASELINE_CURRENT_END_DATE);
            strEstEndDate = (String) mapProject
                    .get(SELECT_TASK_ESTIMATED_FINISH_DATE);
            strState = (String) mapProject.get(DomainConstants.SELECT_CURRENT);
            strType = (String) mapProject.get(DomainConstants.SELECT_TYPE);

            if (strBlEndDate != null && !strBlEndDate.equals(STR_BLANK)
                    && !strBlEndDate.equalsIgnoreCase("null")) {
                dtFinishDate = sdfDateFormat.parse(strBlEndDate);
            } else if (strEstEndDate != null
                    && !strEstEndDate.equals(STR_BLANK)
                    && !strEstEndDate.equalsIgnoreCase("null")) {
                dtFinishDate = sdfDateFormat.parse(strEstEndDate);
            }

            //Get the diiference between the BaseLine Current End date or
            // Estimated End Date and current system date
            //
            // Modified by Enovia MatrixOne for bug no. 303522, dated 05/24/2005
            if (!dtSysDate.after(dtFinishDate)) {
                lDaysRemain = DateUtil.computeDuration(
                        dtSysDate,dtFinishDate);
            }

            //If the type of the project is Concept Project then no slip days
            // icon will be displayed otherwise determine the slipdays icon
            // depending upon the current state of the project and estimated end
            // date
            
            if (strType.equals(DomainConstants.TYPE_PROJECT_CONCEPT)) {
                vtrSlipDays.add(DomainConstants.EMPTY_STRING);
            } else {
                sbHtmlTag = new StringBuffer();
                if (strState.equals(strStateComplete)) {
                    sbHtmlTag
                            .append("<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" alt=\"");
                    sbHtmlTag.append(XSSUtil.encodeForXML(context,strLegendOnTime));
                    sbHtmlTag.append("\"/>");
                }
                // Begin of Modify by Enovia MatrixOne for bug no. 303522, dated 05/24/2005
                else if (dtSysDate.after(dtFinishDate)) {
                    sbHtmlTag
                            .append("<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"");
                    sbHtmlTag.append(XSSUtil.encodeForXML(context,strLegendLate));
                    sbHtmlTag.append("\"/>");
                } else if (lDaysRemain <= iThreshold) {
                    sbHtmlTag
                            .append("<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" alt=\"");
                    sbHtmlTag.append(XSSUtil.encodeForXML(context,strLegendBehind));
                    sbHtmlTag.append("\"/>");
                } else {
                    sbHtmlTag.append(DomainConstants.EMPTY_STRING);
                }
                vtrSlipDays.add(sbHtmlTag.toString());
                // End of Modify by Enovia MatrixOne for bug no. 303522, dated 05/24/2005
            }
        }
        return vtrSlipDays;
    }

        /**
     * Get the list of all products which are connected to the context product.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap having information of the object in context.
     * @return MapList -  contains level,name,type,revision and Index of the Products
         * which are connected to the context product
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6
     */

        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getConnectedProducts (Context context, String args[]) throws Exception
        {

    //The packed argument send from the JPO invoke method is unpacked to retrive the HashMap.
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strMode = (String)  programMap.get("mode");
        String strObjectId = null;
        //The object id of the context object is retrieved from the HashMap using the
        //appropriate key depending on the whether method is called from List page or
        //property page.
        if(strMode.equals("PropertyPage")){
                strObjectId = (String) programMap.get("objectId");
        }
        else if(strMode.equals("ListPage")){
                strObjectId = (String) programMap.get("emxTableRowId");

        }

        //String is initialized to store the value of relationship name

        StringBuffer sbBuffer = new StringBuffer(200);
        sbBuffer.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
        sbBuffer.append(STR_COMMA);
        sbBuffer.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM);
        String strRelationshipType = sbBuffer.toString();

        //short is initialized to store the value the level till which object will be searched
    //Stringlist for querying is formulated.

        List slObjectSelects = new StringList();
        slObjectSelects.add(SELECT_ID);
    slObjectSelects.add(SELECT_TYPE);
    slObjectSelects.add(SELECT_NAME);
    slObjectSelects.add(SELECT_REVISION);

        List lstProductChildTypes = new StringList();

    DomainObject dom = newInstance(context, strObjectId);

        //The associated object details are retreived onto a MapList
    List lstAllObjList = (MapList)dom.getRelatedObjects(
        context,
        strRelationshipType,
        DomainConstants.QUERY_WILDCARD,
        (StringList)slObjectSelects,
        null,
        false,
        true,
        (short) 0,
        DomainConstants.EMPTY_STRING,
        DomainConstants.EMPTY_STRING);

    try{
                // Calling the setLevelsInMapList() method which modifies the maplist
                // (lstAllObjList) returned by the method getRelatedObjects().It loops
                // through the maplist and searches for the intermediate objects ,if it
                // finds one it decrements the "level" of the corresponding object(one
                // connected through the intermediate object)by 1.
                // This manipulation is needed becuase intermediate objects are supposedly
                // transparent to the user.

                lstAllObjList = emxPLCCommonBase_mxJPO.setLevelsInMapList((MapList)lstAllObjList,ProductLineConstants.TYPE_FEATURE_LIST);
                        //setLevelsInMapList((MapList)lstAllObjList,ProductCentralDomainConstants.TYPE_FEATURE_LIST);

                lstProductChildTypes = (StringList)ProductLineUtil.getChildrenTypes(context,ProductLineConstants.TYPE_PRODUCTS);
                //System.out.println("lstProductChildTypes = "+slProductChildTypes);

                //Removing the Feature List Object from the maplist
                for(int i =0; i< lstAllObjList.size(); i++)
                {
                        if(((String)((Map)lstAllObjList.get(i)).get(SELECT_TYPE)).equals(ProductLineConstants.TYPE_FEATURE_LIST))
                        {
                                lstAllObjList.remove(i);
                        }
                }
                Map mapTemp = null;
                int iCurrentLevel = 1;
                int iPrevLevel = 1;
                String strIndex = STR_ONE;
                int iTemp = 0;
                int iLastNumber = 0;

                //For setting the levels of products and features in the Product Structure
                //depending on their occurences in the structure by adding a new key-value
                //pair to the maplist as "Index"-level
                for(int i =0; i< lstAllObjList.size(); i++)
                {
                        iCurrentLevel = Integer.parseInt((String)((Map)lstAllObjList.get(i)).get(SELECT_LEVEL));
                        mapTemp = (Map)lstAllObjList.get(i);
                        if(i == 0)
                        {
                                iPrevLevel = 1;
                        }
                        else
                        {
                                iPrevLevel = Integer.parseInt((String)((Map)lstAllObjList.get(i-1)).get(SELECT_LEVEL));
                        }

                        if(iCurrentLevel > iPrevLevel)
                        {
                                strIndex = strIndex + STR_DOT + STR_ONE;
                                mapTemp.put("Index" , strIndex);
                        }
                        else

                        {
                                iTemp = Math.abs(iCurrentLevel - iPrevLevel)*2;
                                strIndex = strIndex.substring(0,strIndex.length()-iTemp);
                                mapTemp.put("Index" , strIndex);

                                if(i!=0)
                                {
                                        iLastNumber = Integer.parseInt(String.valueOf(strIndex.charAt(strIndex.length()-1))) + 1;
                                        strIndex = strIndex.substring(0,strIndex.length() - 1);
                                        strIndex = strIndex + String.valueOf(iLastNumber);
                                        mapTemp.put("Index" , strIndex);
                                }

                        }
                }

           }
    catch(Exception e)
        {
            e.printStackTrace();
            throw new FrameworkException(e);
        }

        MapList objectList = new MapList();
    Map objMap = null;

        String strNewLevel = DomainConstants.EMPTY_STRING;
    String strType = DomainConstants.EMPTY_STRING;
    String strNextLevel = DomainConstants.EMPTY_STRING;

        int iNextLevel = 0;
    int iPrevLevel = 0;
    int iNewLevel = 0;
    int iTmp = 0;

        boolean bContains = false;
    //Removing Features from the maplist and adding only the Products to the new Maplist
    for(int i=0; i < lstAllObjList.size(); i++)
    {
        objMap = (Map) lstAllObjList.get(i);
        strNewLevel = (String) objMap.get(SELECT_LEVEL);
        iNewLevel = Integer.parseInt(strNewLevel);

                if(i<lstAllObjList.size()-1)
        {
            strNextLevel = (String)((Map) lstAllObjList.get(i+1)).get(SELECT_LEVEL);
            iNextLevel = Integer.parseInt(strNextLevel);
        }
        strType = ((String) objMap.get(SELECT_TYPE)).trim();
        bContains = false;
        for(int iCount=0; iCount < lstProductChildTypes.size(); iCount++)
        {
            if (strType.equals(lstProductChildTypes.get(iCount)))
            {
                                bContains=true;
            }

        } //end of for loop

        if(bContains)
        {
            objectList.add(objMap);
        }
    }// end of outer for loop

        return objectList;
  }

     /**
      * This method is used to get the level for all the Products
      * which are associated with context Product.
      * @param context the eMatrix <code>Context</code> object
      * @param args - Holds the parameters passed from the calling method
      * When this array is unpacked, arguments corresponding to the following
      * String keys are found:-
      * objectList- MapList Containing the objectIds.
      * @return Vector
      * @throws Exception if the operation fails
      * @since ProductCentral 10.6
    **/

        public List getLevel(Context context,String[] args) throws Exception
            {
                List lstLevel = new StringList();

                    Map programMap = (Map) JPO.unpackArgs(args);
                    List objectList = (MapList)programMap.get(STR_OBJECTLIST);
                    Iterator objectListItr = objectList.iterator();
                    String strLevel = DomainConstants.EMPTY_STRING;

                    Map objectMap = new HashMap();
                    //loop through all the records
                    while(objectListItr.hasNext())
                    {
                        objectMap = (Map) objectListItr.next();
                        strLevel = (String) objectMap.get("Index");
                        lstLevel.add(strLevel);
                    } //End of while loop

                    return lstLevel;

            } //End of the method

        /**
     * This method is called by a check trigger on Delete of 'Product'.
     * This method checks whether the product being deleted has connected product compatibility rules.
     * In case it has then the trigger will return 1 and stop the deletion of that product.
     * @param context the eMatrix <code>Context</code> object
         * @param args
     *        0 - contains the objectID of the product to be deleted.
     * @return int - an integer (0) if the operation is successful
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
         public int checkForCompatibilityRules(Context context, String[] args) throws Exception
    {
        // this is the objectId of the product to be deleted
        String strProductId = args[0];

                StringBuffer sbBuffer = new StringBuffer(200);
                sbBuffer.append(ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION);
                sbBuffer.append(STR_COMMA);
                sbBuffer.append(ProductLineConstants.RELATIONSHIP_RIGHT_EXPRESSION);
                String strRelPattern = sbBuffer.toString();

                List lstObjectSelects = new StringList();
                lstObjectSelects.add(SELECT_ID);
                lstObjectSelects.add(SELECT_TYPE);

                String strTypePatten = ProductLineConstants.TYPE_PRODUCT_COMPATIBILITY_RULE;

                DomainObject domobj = newInstance(context, strProductId);

                List mlRuleList  = (MapList)domobj.getRelatedObjects(context,
                                                                                        strRelPattern,
                                                                                        strTypePatten,
                                                                                        (StringList)lstObjectSelects,
                                                                                        null,
                                                                                        true,
                                                                                        false,
                                                                                        (short) 0,
                                                                                        DomainConstants.EMPTY_STRING,
                                                                                        DomainConstants.EMPTY_STRING);

                //mlRuleList will be empty if the context Product is not connected to
                //any compatibility rule.

                if (! (mlRuleList.isEmpty()))
                {
                        String language = context.getSession().getLanguage();
                        //Alert message is formulated to display the error message
                        String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.Delete.ProductAttachedRules",language);
                        //Explicit alert message is thrown to the front end.
                        emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
                        //Non zero value is returned to the trigger manager to denote failure.
                        return 1;
                }
                //Zero is returned to the trigger manager to denote success
                return 0;

        }//end of method

    /**
     * This method is used to get the list of top level Projects and Programs
     *  The method returns
     * the MapList contains Map with following key value pairs:
     * id - The object id
     * name - The object name
     * type - The object type
     * Child The MapList containing the information about the
     *                      child of this Program.
     * Selectable The string(true/false) indicating whether this Object is already connected to
     *                   Product
     * @param context The ematrix context object.
     * @param args The String array containing Packed arguments as follows:
     *                      HashMap containing the object id
     * @return MapList
     * @throws Exception
     */
         @com.matrixone.apps.framework.ui.ProgramCallable
         public MapList getAllProjects(
                 Context context,
                 String[] args) throws Exception {
        	 
        		MapList projList = new MapList();
        		try{
        			//Unpack the arguments to get the object id of the Product
        	        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        	        String strProductId = (String) programMap.get(STR_OBJECTID);
        	        
        	        
        	        StringBuffer sbProjectTypes = new StringBuffer();
        	        sbProjectTypes.append(PropertyUtil.getSchemaProperty(context,"type_ProjectSpace"));
        	        sbProjectTypes.append(",");
        	        sbProjectTypes.append(PropertyUtil.getSchemaProperty(context,"type_ProjectConcept"));
        	        sbProjectTypes.append(",");
        	        sbProjectTypes.append(DomainConstants.TYPE_PROGRAM);
        	       
        	        //where clause that filters out the subprojects, and projects that are connect under a program
        	        StringBuffer sbWhereExpression = new StringBuffer();
        	        sbWhereExpression.append("( to[");
        	        sbWhereExpression.append(ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS);
        	        sbWhereExpression.append("] =='False' || to[");
        	        sbWhereExpression.append(ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS);
        	        sbWhereExpression.append("].from.type.kindof != '");					
        	        sbWhereExpression.append(ProductLineConstants.TYPE_PROJECT_MANAGEMENT);
        	      //  sbWhereExpression.append(", ");
        	        //sbWhereExpression.append(ProductLineConstants.TYPE_HARDWARE_PRODUCT);
        	        sbWhereExpression.append("') && ");
        	        sbWhereExpression.append("to[");
        	        sbWhereExpression.append(ProductLineConstants.RELATIONSHIP_PROGRAM_PROJECT);
        	        sbWhereExpression.append("]=='False'");
        	        

        	        //Form the object select list for the Product
        	        List lstObjectSelects = new StringList();
        	        lstObjectSelects.add(DomainConstants.SELECT_ID);
        	        lstObjectSelects.add(DomainConstants.SELECT_NAME);
        	        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
        	        lstObjectSelects.add("to[" +ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS + "].from.id");
        	        lstObjectSelects.add("to["+ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS+"].from.type");
        	        MULTI_VALUE_LIST.add("to[" +ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS + "].from.type");
        	        
        	        
        	        if(!MULTI_VALUE_LIST.contains("to[" +ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS + "].from.id")){
						MULTI_VALUE_LIST.add("to[" +ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS + "].from.id");						
					}
        	        //Get the Projects satisfying the given criteria
        	        MapList tempList = DomainObject
        	                .findObjects(
        	                             context,
        	                             sbProjectTypes.toString(),
        	                             DomainConstants.QUERY_WILDCARD,
        	                             sbWhereExpression.toString(),							//whereclause
        	                             (StringList) lstObjectSelects);

        	        
        	       // if a subproject is connected to Product, it has "related projects relationship" so it will comming in result
        	        //as top level object, so need to filter it out
        			StringList connectedTypes = new StringList(1);
        			boolean isNotsubproject = true;
        			for (Iterator iterator = tempList.iterator(); iterator
							.hasNext();) {
						Map objMap = (Map) iterator.next();
						isNotsubproject = true;
						if(objMap.get("to[" +ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS + "].from.type") !=null){
							connectedTypes = (StringList)objMap.get("to[" +ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS + "].from.type");
							for (Iterator iterator2 = connectedTypes.iterator(); iterator2.hasNext();) {
								//Object strType = (String) iterator2.next();
								if(mxType.isOfParentType(context, (String) iterator2.next(), ProductLineConstants.TYPE_PROJECT_SPACE)){
									isNotsubproject=false;
									break;
								}
								
							}
						}
						if(isNotsubproject){
							projList.add(objMap);
						}
						
					} 	
        			//if any of the project are already connected to product or the program need to be disabled to avoid further selection
        	        disabledSelectedProjects(context, projList, strProductId);
        	        //cleaning the global Maplist
        	        if (MULTI_VALUE_LIST.contains("to["+ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS+"].from.type")){
    					MULTI_VALUE_LIST.remove("to["+ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS+"].from.type");
    					
    				}
        		} catch(Exception e){
         				throw new FrameworkException(e);
         			}
            return projList;
        }
         /**
          * This method is used to get the list of  Projects that are conencted to the Programs and projects
          *  The method returns
          * the MapList contains Map with following key value pairs:
          * id - The object id
          * name - The object name
          * type - The object type
          * Child The MapList containing the information about the
          *                      child of this Program.
          * Selectable The string(true/false) indicating whether this Object is already connected to
          *                   Product
          * @param context The ematrix context object.
          * @param args The String array containing Packed arguments as follows:
          *                      HashMap containing the object id
          * @return MapList
          * @throws Exception
          */
         @com.matrixone.apps.framework.ui.ProgramCallable
         public MapList expandProjects(
                 Context context,
                 String[] args) throws Exception {
        		MapList projList = new MapList();
        		try{
        			//Unpack the arguments to get the object id of the Product
        			Map programMap = (HashMap) JPO.unpackArgs(args);
        			String strObjectId = (String) programMap.get(STR_OBJECTID);
        		String strProductId = (String) programMap.get("parentOID");
        			
        			StringBuffer sbrelPattern = new StringBuffer();
        			sbrelPattern.append(ProductLineConstants.RELATIONSHIP_PROGRAM_PROJECT);
        			sbrelPattern.append(",");
        			sbrelPattern.append(ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS);
        	       
        			StringList lstObjectSelects = new StringList();
        	        lstObjectSelects.add(DomainConstants.SELECT_ID);
        	        lstObjectSelects.add(DomainConstants.SELECT_NAME);
        	        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
        	        lstObjectSelects.add("to["+ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS+"].from.id");
        	        
        	        MULTI_VALUE_LIST.add("to[" +ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS + "].from.id"); 
					
        	        
        	        StringList slRelSelects = new StringList(ProductLineConstants.SELECT_RELATIONSHIP_NAME);
        			slRelSelects.add(ProductLineConstants.SELECT_RELATIONSHIP_ID);
        	        
        			projList = new DomainObject(strObjectId).getRelatedObjects( context,
        																					sbrelPattern.toString(),
																                            WILD_CHAR,
																                            lstObjectSelects,
																                            slRelSelects,
																                           false,
																                            true,
																                            (short)1,
																                            null,
																                            null,
																                            0);
        			
        			
        			//if any of the project are already connected to product or the program need to be disabled to avoid further selection        			
        			disabledSelectedProjects(context, projList,strProductId);
                            
        		} catch(Exception e){
         				throw new FrameworkException(e);
         			}
            return projList;
        }
         /**
          * This method is used disable the programs , projects that are already connected to context Product
          *  and updates teh Map with key value pair
          *  key: disableSelection , value :True/false
          *  
          **/
         
         private void disabledSelectedProjects(
                 Context context,
                 MapList relatedobjMap, String strProductId) throws Exception {
        	try{
        		StringList relatedType=null;
        	 for (Iterator iterator = relatedobjMap.iterator(); iterator.hasNext();) {
 				Map objectMap = (Map) iterator.next();
 				if(mxType.isOfParentType(context, (String)objectMap.get(DomainConstants.SELECT_TYPE), ProductLineConstants.TYPE_PROGRAM)){
					  objectMap.put("disableSelection", "True");
					  continue;
				} 
 				if(objectMap.get("to["+ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS+"].from.id") !=null){
 					 relatedType = new StringList(1);
 					 Object tmpObj = (Object)objectMap.get("to["+ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS+"].from.id");
	 					if(tmpObj instanceof String) {	 						
	 						relatedType.add((String) tmpObj);	 						
						}else{
							relatedType.addAll((StringList)tmpObj);
						}
	 					
	 					if(relatedType.contains(strProductId)){
	 					 objectMap.put("disableSelection", "True");
	 					} 									
					 		  
	 				}
			
		       }
        	 
        	 if (MULTI_VALUE_LIST.contains("to["+ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS+"].from.id")){
					MULTI_VALUE_LIST.remove("to["+ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS+"].from.id");
					
				}
        	}catch (Exception e) {
        		throw new FrameworkException(e);
			}
         
         }
        
    
    /**
     * This method is used to convert the MapList into StringList by passing
     * perticular key.
     *
     * @param lstProjectList The MapList.
     * @param strKey The key.:
     * @return StringList
     * @throws Exception
     * @since ProductCentral10.6
     */
    protected static List getStringList(MapList lstProjectList,String strKey){
        List lstIdList  = new StringList();
        Iterator itr = lstProjectList.iterator();
        while(itr.hasNext())
        {
            lstIdList.add((String)((Map)itr.next()).get(strKey));
        }
        return lstIdList;
    }

        /**
     * This method returns all the Tasks connected to the Product with
     * "Roadmap Task" relationship.
     *
     * @param context The ematrix context of the request.
     * @param args The packed arguments that contains the Map with following key
     *            value pair: objectId - Object id of the context product
     * @return MapList containing the information of Tasks connected to the
     *         Product
     * @throws Exception
     * @throws FrameworkException
     * @since ProductCentral10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public List getRelatedTasks(Context context, String[] args)
            throws Exception, FrameworkException {
        //Modified this method by Enovia MatrixOne for the bug about Task List page
        //performance and Program & Project name in the wizard page.
        //Deleted some lines of code from this method on 24-May-2005

        //Get the object id of the context Product and create a domain object
        Map mapProgram = (HashMap) JPO.unpackArgs(args);
        String strProductId = (String) mapProgram.get(STR_OBJECTID);
        DomainObject domProduct = DomainObject.newInstance(
                context, strProductId);

        //Form the objects and relationship select list

        //objselects
        List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
        lstObjectSelects.add(DomainConstants.SELECT_CURRENT);
        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
        lstObjectSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
        lstObjectSelects.add(SELECT_BASELINE_CURRENT_END_DATE);
        lstObjectSelects.add(SELECT_TASK_ACTUAL_FINISH_DATE);


        //Relselects
        List lstRelSelects = new StringList(
                DomainConstants.SELECT_RELATIONSHIP_ID);
        lstRelSelects.add(ProductLineConstants.SELECT_ATTRIBUTE_REGION);
        lstRelSelects.add(ProductLineConstants.SELECT_ATTRIBUTE_SEGMENT);
        lstRelSelects.add(ProductLineConstants.SELECT_ATTRIBUTE_ROADMAP_LABEL);
        lstRelSelects.add(ProductLineConstants.SELECT_ATTRIBUTE_SEASON);
        lstRelSelects.add(ProductLineConstants.SELECT_ATTRIBUTE_DATE);
        lstRelSelects.add(ProductLineConstants.SELECT_ATTRIBUTE_MILESTONE_TYPE);
        /*Get the Tasks connected to the Product by "Roadmap Task"
         relationship*/
        List lstTasksList = domProduct.getRelatedObjects(
                context,
                ProductLineConstants.RELATIONSHIP_ROADMAP_TASK,
                DomainConstants.QUERY_WILDCARD, (StringList) lstObjectSelects,
                (StringList) lstRelSelects, false, true, (short) 1, null, null);

        return lstTasksList;
  }

    /**
     * Method to generate the HTML to display Project name(Hyperlinked)
       for the Product Tasks Table.
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap having information of the object in
     *        context.
     * @return Vector - HTML for hyperlinked Project name
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

        public List getParentProjectForTask(
                            Context context,
                            String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        List lstTaskList = (MapList) programMap.get("objectList");
        Map paramMap = (Map) programMap.get("paramList");
        //strReportFormat indicates whether method is called from table or report.
        String strReportFormat=(String)paramMap.get("reportFormat");
        String strProjectId = "";
        String strProjectName = "";
        StringBuffer sbHref = new StringBuffer();
        Vector vtrProjectIcon = new Vector();
        Map mpTaskMap = new HashMap();

        //Begin of Add by Enovia MatrixOne on 24-May-05 for the bug about Task List page
        String [] arrObjId = new String[lstTaskList.size()];
        for (int i = 0; i < lstTaskList.size(); i++){
                 mpTaskMap = (Map)lstTaskList.get(i);
                 arrObjId[i] = (String)(mpTaskMap.get(DomainConstants.SELECT_ID));
        }
        StringBuffer sbSelProjId = new StringBuffer("to[");
        sbSelProjId.append(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY);
        sbSelProjId.append("].from.from[");
        sbSelProjId.append(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST);
        sbSelProjId.append("].to.");
        sbSelProjId.append(SELECT_ID);

        StringBuffer sbSelProjName = new StringBuffer("to[");
        sbSelProjName.append(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY);
        sbSelProjName.append("].from.from[");
        sbSelProjName.append(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST);
        sbSelProjName.append("].to.");
        sbSelProjName.append(SELECT_NAME);

        List lstProjSelects = new StringList();
        lstProjSelects.add(sbSelProjId.toString());
        lstProjSelects.add(sbSelProjName.toString());

        BusinessObjectWithSelectList lstProjects =
                        getSelectBusinessObjectData(context,
                                                                arrObjId,
                                                                (StringList)lstProjSelects);
        //End of Add by Enovia MatrixOne on 24-May-05 for the bug about Task List page

         //Getting the bus ids for objects in the table
        for (int i = 0; i < lstTaskList.size(); i++)
           {
                //Begin of Modify by Enovia MatrixOne on 24-May-05 for the bug about Task List page
                strProjectId =  (String)(lstProjects.
                                                    getElement(i).
                                                        getSelectData(sbSelProjId.
                                                                                    toString()));
                strProjectName =  (String)(lstProjects.
                                                        getElement(i).
                                                            getSelectData(sbSelProjName.
                                                                                    toString()));
                //End of Modify by Enovia MatrixOne on 24-May-05 for the bug about Task List page

                 //clear the buffer on every iteration
                 sbHref.delete(0,sbHref.length());
                   if (strProjectId != null
                            && !strProjectId
                                    .equals(DomainConstants.EMPTY_STRING)
                                    
                            && !strProjectId.equalsIgnoreCase("null")) {
                       //if in table mode
                         if(!(strReportFormat!=null&&
                             strReportFormat.equals("null")==false&&
                              strReportFormat.equals("")==false))
                          {
                            sbHref.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                            sbHref.append(XSSUtil.encodeForHTMLAttribute(context,strProjectId));
                            sbHref.append("&amp;mode=replace");
                            sbHref.append("&amp;AppendParameters=true");
                            sbHref.append("&amp;reloadAfterChange=true");
                            sbHref.append("')\">");
                            sbHref.append(XSSUtil.encodeForHTML(context,strProjectName));
                            sbHref.append("</A>");
                            vtrProjectIcon.add(sbHref.toString());
                          }
                          else//if called from report
                          {
                           vtrProjectIcon.add(strProjectName);
                          }
                      }else{
                        vtrProjectIcon.add(DomainConstants.EMPTY_STRING);
                    }
            }//end of the for loop
          return vtrProjectIcon;
           }//end of the program

    /**
     * Method to generate the HTML to display Program name(Hyperlinked)
       for the PRCProducttaskList Table.
     * @param context the eMatrix <code>Context</code> object
     * @param args - Has the packed Hashmap having information of the object in
     *        context.
     * @return Vector - HTML for hyperlinked Program name
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */

        public Vector getParentProgramForTask(
                            Context context,
                            String[] args) throws Exception
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            List lstTaskList = (MapList) programMap.get("objectList");
            Map paramMap = (Map) programMap.get("paramList");
            //strReportFormat indicates whether method is called from table or report.
            String strReportFormat=(String)paramMap.get("reportFormat");
            String strProgramId = "";
            String strProgramName = "";
            StringBuffer sbHref = new StringBuffer();
            Vector vtrProgramIcon = new Vector();
            Map mpTaskMap = new HashMap();

            //Begin of Modify by Enovia MatrixOne on 24-May-05 for the bug about Task List page
            String [] arrObjId = new String[lstTaskList.size()];
            for (int i = 0; i < lstTaskList.size(); i++){
                     mpTaskMap = (Map)lstTaskList.get(i);
                     arrObjId[i] = (String)(mpTaskMap.get(DomainConstants.SELECT_ID));
            }
            StringBuffer sbSelProgId = new StringBuffer("to[");
            sbSelProgId.append(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY);
            sbSelProgId.append("].from.from[");
            sbSelProgId.append(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST);
            sbSelProgId.append("].to.to[");
            sbSelProgId.append(ProductLineConstants.RELATIONSHIP_PROGRAM_PROJECT);
            sbSelProgId.append("].from.");
            sbSelProgId.append(SELECT_ID);

            StringBuffer sbSelProgName = new StringBuffer("to[");
            sbSelProgName.append(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY);
            sbSelProgName.append("].from.from[");
            sbSelProgName.append(DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST);
            sbSelProgName.append("].to.to[");
            sbSelProgName.append(ProductLineConstants.RELATIONSHIP_PROGRAM_PROJECT);
            sbSelProgName.append("].from.");
            sbSelProgName.append(SELECT_NAME);

            List lstProgSelects = new StringList();
            lstProgSelects.add(sbSelProgId.toString());
            lstProgSelects.add(sbSelProgName.toString());

            BusinessObjectWithSelectList lstPrograms =
                                                    getSelectBusinessObjectData(
                                                                                            context,
                                                                                            arrObjId,
                                                                                            (StringList)lstProgSelects);
            //End of Modify by Enovia MatrixOne on 24-May-05 for the bug about Task List page

            for (int i = 0; i < lstTaskList.size(); i++)
               {
                 //Begin of Modify by Enovia MatrixOne on 24-May-05 for the bug about Task List page
                 strProgramId =  (String)(lstPrograms.
                                                    getElement(i).
                                                        getSelectData(sbSelProgId.
                                                                                    toString()));
                 strProgramName =  (String)(lstPrograms.
                                                        getElement(i).
                                                            getSelectData(sbSelProgName.
                                                                                    toString()));
                 //End of Modify by Enovia MatrixOne on 24-May-05 for the bug about Task List page

                 //clear the buffer on the every iteration
                 sbHref.delete(0,sbHref.length());
                   if (strProgramId != null
                            && !strProgramId
                                    .equals(DomainConstants.EMPTY_STRING)
                            && !strProgramId.equalsIgnoreCase("null")) {
                         //if in table mode
                         if(!(strReportFormat!=null&&
                             strReportFormat.equals("null")==false&&
                              strReportFormat.equals("")==false))
                          {
                            sbHref.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                            sbHref.append(XSSUtil.encodeForHTMLAttribute(context,strProgramId));
                            sbHref.append("&amp;mode=replace");
                            sbHref.append("&amp;AppendParameters=true");
                            sbHref.append("&amp;reloadAfterChange=true");
                            sbHref.append("')\">");
                            sbHref.append(XSSUtil.encodeForHTML(context,strProgramName));
                            sbHref.append("</A>");
                            vtrProgramIcon.add(sbHref.toString());
                          }
                          else//if called from report
                          {
                           vtrProgramIcon.add(strProgramName);
                          }
                    }else{
                        vtrProgramIcon.add(DomainConstants.EMPTY_STRING);
                    }
               }//end of the for loop
            return vtrProgramIcon;
        }//end of the program
        
        /**
         * This method is used to get the list of  Projects taht are connected to Product
         * and The method returns MapList contianing Map with following key value pairs:
         * id - The object id
         * name - The object name
         * type - The object type
         * 
         * Selectable The string(true/false) indicating whether this Object is already connected to
         *                   Product
         * @param context The ematrix context object.
         * @param args The String array containing Packed arguments as follows:
         *                      HashMap containing the object id
         * @return MapList
         * @throws Exception
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getConnectedProjects(
                Context context,
                String[] args) throws Exception {
        	MapList projList = new MapList();
    		try{
    			HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	        String strObjectId = (String)  programMap.get("objectId");
    	        
    	     //  StringList lstTasksIdList =  new DomainObject(strObjectId).getInfoList(context, "from["+ProductLineConstants.RELATIONSHIP_ROADMAP_TASK+"].to.id");
    	      
    	        StringList lstObjectSelects = new StringList();
    	        lstObjectSelects.add(DomainConstants.SELECT_ID);
    	        lstObjectSelects.add(DomainConstants.SELECT_NAME);
    	        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
    	        
    	        
    	        StringList slRelSelects = new StringList(ProductLineConstants.SELECT_RELATIONSHIP_NAME);
    			slRelSelects.add(ProductLineConstants.SELECT_RELATIONSHIP_ID);

    	     
    	         projList = new DomainObject(strObjectId).getRelatedObjects( context,
    	    		   ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS,
    	    		   ProductLineConstants.TYPE_PROJECT_SPACE,
                       lstObjectSelects,
                       slRelSelects,
                      false,
                       true,
                       (short)1,
                       null,//objewhere
                       null,//relwhere
                       0);
    	         
    	         
    	        
    	         Map mapObject = null;
    	         for (Iterator iterator = projList.iterator(); iterator.hasNext();) {
					 mapObject = (Map) iterator.next();		
				     	 mapObject.put("disableSelection", "True");              
    	         }
    		
    		}catch (Exception e) {
    			throw new FrameworkException(e);
			}
    		return projList;
        }
   
     /**
     * This method is used to get the list of  Tasks that are connected to the Project
     * and The method returns MapList contianing Map with following key value pairs:
     * id - The object id
     * name - The object name
     * type - The object type
     * 
     * Selectable The string(true/false) indicating whether this Object is already connected to
     *                   Product
     * @param context The ematrix context object.
     * @param args The String array containing Packed arguments as follows:
     *                      HashMap containing the object id, parent object id
     * @return MapList
     * @throws Exception
     * @since ProductCentral10.6
     */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getAllProjectTasks(
                Context context,
                String[] args) throws Exception {
        	MapList lstTasksList = new MapList();
        	try{
			//Unpack the arguments to get the object id of the Product
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String)  programMap.get("objectId");
			String strProductId = (String) programMap.get("parentOID");
			//Get the list of all the Tasks already connected to this Product
			StringList lstTasksIdList =  new DomainObject(strProductId).getInfoList(context, "from["+ProductLineConstants.RELATIONSHIP_ROADMAP_TASK+"].to.id");
					
			StringList lstObjectSelects = new StringList();
	        lstObjectSelects.add(DomainConstants.SELECT_ID);
	        lstObjectSelects.add(DomainConstants.SELECT_NAME);
	        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
	        
	        
	        StringList slRelSelects = new StringList(ProductLineConstants.SELECT_RELATIONSHIP_NAME);
			slRelSelects.add(ProductLineConstants.SELECT_RELATIONSHIP_ID);
			
			 lstTasksList = new DomainObject(strObjectId).getRelatedObjects( context,
  	    		   ProductLineConstants.RELATIONSHIP_SUBTASK,
  	    		   ProductLineConstants.TYPE_TASK_MANAGEMENT,
                     lstObjectSelects,
                     slRelSelects,
                    false,
                     true,
                     (short)1,
                     "current.access[toconnect]==TRUE",//objewhere
                     null,//relwhere
                     0);
									
			Iterator itrTasks = lstTasksList.iterator();
			Map mapObject = null;
			while (itrTasks.hasNext()) {
			mapObject = (Map) itrTasks.next();
			/*If this Task is already connected to the context product through
			 relationship Roadmap Task then make 'Selectable' as false*/
			if(lstTasksIdList.contains(mapObject
			     .get(DomainObject.SELECT_ID))){
				 mapObject.put("disableSelection", "True");              
			}
			}//end while
        	}catch (Exception e) {
        		throw new FrameworkException(e);
			}
			return  (MapList)lstTasksList;
        }
        
     /**
     * This method returns all the Tasks connected to the Product with
     * "Roadmap Task" relationship.It is used as Table JPO method for table
     *  PRCProductTaskList when opened in Edit mode for "Associate Tasks"
     *
     * @param context The ematrix context of the request.
     * @param args The packed arguments that contains the Map with following key
     *            value pair: objectId - Object id of the context product
     * @return MapList containing the information of Tasks connected to the
     *         Product
     * @throws Exception
     * @throws FrameworkException
     */
    public List getSelectedTasks(Context context, String[] args)
            throws Exception, FrameworkException {
        //Get the object id of the context Product and create a domain object
        Map mapProgram = (HashMap) JPO.unpackArgs(args);
        String strTaskIds = (String)mapProgram.get("taskIds");
        List lstObjIdList = new MapList();
        String strObjId = "";
        String strRelId = "";
        String strToken = "";
        Map mpId = new HashMap();
        Map mpObj = new HashMap();
        List lstObjSelects = new StringList(DomainConstants.SELECT_TYPE);
        lstObjSelects.add(DomainConstants.SELECT_CURRENT);
        lstObjSelects.add(SELECT_BASELINE_CURRENT_END_DATE);
        lstObjSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
        lstObjSelects.add(DomainConstants.SELECT_CURRENT);

        /*strTaskIds contains objIds and RelIds in the following format:
          objId1|relId1,objId2|relId2,objId3|relId3 and so on.*/
        StringTokenizer stTaskIds = new StringTokenizer(strTaskIds,",");

        for(int i = 0 ; stTaskIds.hasMoreTokens() ; i++)
            {
             strToken = stTaskIds.nextToken();
             strObjId = strToken.substring(0,strToken.indexOf("|"));
             strRelId = strToken.substring(strToken.indexOf("|")+1,strToken.length());


             //Get the value of the attributes Estimated Finish Date & Baseline
             // Current Finish Date and current state and type of the Task
             DomainObject domObj = newInstance(context, strObjId);
             mpObj = domObj.getInfo(context,(StringList)lstObjSelects);
             //putting all the information in Map
             mpId = new HashMap();
             mpId.put(SELECT_ID,strObjId);
             mpId.put(SELECT_RELATIONSHIP_ID,strRelId);
             mpId.put(SELECT_BASELINE_CURRENT_END_DATE,(String) mpObj
                    .get(SELECT_BASELINE_CURRENT_END_DATE));
             mpId.put(SELECT_TASK_ESTIMATED_FINISH_DATE,(String) mpObj
                    .get(SELECT_TASK_ESTIMATED_FINISH_DATE));
             mpId.put(SELECT_CURRENT,(String) mpObj.get(SELECT_CURRENT));
             mpId.put(SELECT_TYPE,(String) mpObj.get(SELECT_TYPE));

             //add the Map to the MapList
             lstObjIdList.add(mpId);
            }//end of the for loop
        return (MapList)lstObjIdList;
   }//end of the Method

   /**
     * Method to get range values for "Region" attribute.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - HashMap containing one Map entry for the key "programMap"
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
 public Map getRegionRange(Context context,String[]args)throws Exception
    {
        //Begin of add:By Ramandeep,Enovia MatrixOne for Bug#299948 on 3/9/2005
        Map rangeMap = new HashMap();
        String strComapnyId = PersonUtil.getUserCompanyId(context);
        //Get the object id of the context context user's Company and create a domain object
        DomainObject domCompany = DomainObject.newInstance(context,strComapnyId);
       //select for Region
        StringBuffer sbSelRegion = new StringBuffer("from[");
        sbSelRegion.append(DomainConstants.RELATIONSHIP_ORGANIZATION_REGION);
        sbSelRegion.append("].to.");
        sbSelRegion.append(SELECT_NAME);
        List lstObjList= (StringList)domCompany.getInfoList(context,sbSelRegion.toString());
        rangeMap.put(FIELD_CHOICES,lstObjList);
        //Added for IR-070350V6R2011x
        rangeMap.put(FIELD_DISPLAY_CHOICES,lstObjList);
        //End of IR-070350V6R2011x
        return  rangeMap;
        //End of add:By Ramandeep,Enovia MatrixOne for Bug#299948 on 3/9/2005
    }

    /**
     * Method to get range values for "Segment" attribute.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - HashMap containing one Map entry for the key "programMap"
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
  public Map getSegmentRange(Context context,String[]args) throws Exception
    {
       return getRelAttribsRange(context,args,ProductLineConstants.ATTRIBUTE_SEGMENT);
    }

    /**
     * Method to get range values for "Season" attribute.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - HashMap containing one Map entry for the key "programMap"
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
  public Map getSeasonRange(Context context,String[]args) throws Exception
    {
     return getRelAttribsRange(context,args,ProductLineConstants.ATTRIBUTE_SEASON);
    }

    /**
     * Method to get range values for "RoadMap Label" attribute.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - HashMap containing one Map entry for the key "programMap"
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
  public Map getRoadmapLabelRange(Context context,String[]args) throws Exception
    {
     return getRelAttribsRange(context,args,ProductLineConstants.ATTRIBUTE_ROADMAP_LABEL);
    }

    /**
     * Method to get range values for "Date" attribute.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - HashMap containing one Map entry for the key "programMap"
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
  public Map getDateRange(Context context,String[]args)throws Exception
    {
      return getRelAttribsRange(context,args,ProductLineConstants.ATTRIBUTE_DATE);
    }

    /**
     * Method to get range values for "MilestoneType" attribute.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - HashMap containing one Map entry for the key "programMap"
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
  public Map getMilestoneTypeRange(Context context,String[]args)throws Exception
    {
      return getRelAttribsRange(context,args,ProductLineConstants.ATTRIBUTE_MILESTONE_TYPE);
    }

    /**
     * Method to get range values for "year" attribute.
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *        0 - HashMap containing one Map entry for the key "programMap"
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception if operation fails
     * @since ProductCentral 10-6
     */
 public Map getYearRange(Context context,String[]args)throws Exception
    {
       return getRelAttribsRange(context,args,ProductLineConstants.ATTRIBUTE_YEAR);
    }

    /**
     * Returns the Range Values for Relationship attributes.
     * This method is used in Table PRCProductTaskList(in edit mode).
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return Map - Map of Display Range values and actual range values
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6
     */

  public Map getRelAttribsRange(Context context, String[] args,String strAttributeName) throws Exception
    {

        Map rangeMap = new HashMap();
        matrix.db.AttributeType attribName =
            new matrix.db.AttributeType(strAttributeName);
        attribName.open(context);
        // get its ranges
        List attributeRange = attribName.getChoices();
        //return attributeRange;
        List attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(strAttributeName, (StringList)attributeRange,context.getSession().getLanguage());
        rangeMap.put(FIELD_CHOICES , attributeRange);
        rangeMap.put(FIELD_DISPLAY_CHOICES , attributeDisplayRange);
        return  rangeMap;

    }//end of the method

   /** This method gets the object Structure List for the context Product object.This method gets invoked
     * by settings in the command which displays the Structure Navigator for Product type objects
     *  @param context the eMatrix <code>Context</code> object
     *  @param args    holds the following input arguments:
     *      paramMap   - Map having object Id String
     *  @return MapList containing the object list to display in Product structure navigator
     *  @throws Exception if the operation fails
     *  @since Product Central 10-6
     */
    public static MapList getStructureList(Context context, String[] args)
        throws Exception
    {
        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap          = (HashMap)programMap.get("paramMap");
        String objectId           = (String)paramMap.get("objectId");
        MapList productStructList = new MapList();
            Pattern typePattern     = new Pattern("*");
            DomainObject product = DomainObject.newInstance(context, objectId);
            String objectType     = product.getInfo(context, DomainConstants.SELECT_TYPE);
            String strSymbolicName = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, objectType, true);
            String strAllowedSTRel = "";
            try{
            	strAllowedSTRel = EnoviaResourceBundle.getProperty(context, "emxConfiguration.StructureTree.SelectedRel."+strSymbolicName);
            }catch (Exception e) {
    		}
            if(strAllowedSTRel.equals("")){
            	strAllowedSTRel = EnoviaResourceBundle.getProperty(context, "emxConfiguration.StructureTree.SelectedRel.type_Products");
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
 	    
  	    String derivationType = null;
   	    if (objectType != null && strRelPattern.equals(ProductLineConstants.RELATIONSHIP_PRODUCTS)) {

    	   	try {
	   	    	// Get the info for the Root Product
	    		StringList slObjSelects = new StringList(DomainObject.SELECT_ID);
	    		slObjSelects.add(DomainObject.SELECT_NAME);
	    		slObjSelects.add(DomainObject.SELECT_REVISION);
	    		slObjSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
	    		slObjSelects.add(DomainObject.SELECT_RELATIONSHIP_NAME);
	
	    		// Get the information for the root object.
	    		DomainObject domObject = new DomainObject(objectId);
	    		Map mpSingleObject = domObject.getInfo(context, slObjSelects);
	    		derivationType = domObject.getInfo(context, DomainObject.SELECT_TYPE);
	    				
	    		// Get the rest of the Derivations from the Root Product
	    		productStructList = DerivationUtil.getAllDerivations(context, objectId);
	    		if (productStructList != null && mpSingleObject != null) {
	    			productStructList.add(mpSingleObject);
	                // Sort the List
	    			productStructList.addSortKey(DomainObject.SELECT_NAME, "ascending", "String");
	    			productStructList.addSortKey(DomainObject.SELECT_REVISION, "ascending", "String");
	    			productStructList.sort();
	    		}
    	   	} catch(Exception ex) {
    	   		throw new FrameworkException(ex);
    	   	}
    	    
    	} else if(objectType != null && mxType.isOfParentType(context, objectType,ProductLineConstants.TYPE_PRODUCTS)){
            try {
                // get Requirements, Product Configuration component objects of the Product
            	Pattern relPatternTemp     = new Pattern(strRelPattern);
    	   		productStructList = ProductLineCommon.getObjectStructureList(context, objectId, relPatternTemp, typePattern);
    	   	} catch(Exception ex) {
                throw new FrameworkException(ex);
             }
        } else {
             productStructList = (MapList) emxPLCCommon_mxJPO.getStructureListForType(context, args);
        }
        return productStructList;
    }

      /**
     * This column JPO method is used to get the Governing Project name of
     * the Product.
     *
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return List containing the hyperlinked names of Governing Project.
     * @throws Exception
     * @since ProductCentral10.6
     */
    public List getProjectProgram(
                            Context context,
                            String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        List lstProjList = (MapList) programMap.get("objectList");

        //Get the reportformat for removeing the hyperlink in export
        //mode
        HashMap paramList = (HashMap)programMap.get("paramList");
        String strReportFormat = (String) paramList.get("reportFormat");

        String arrObjId[] = new String[lstProjList.size()];

        //Getting the bus ids for objects in the table
        for (int i = 0; i < lstProjList.size(); i++) {
            arrObjId[i] = (String) ((Map) lstProjList.get(i))
                    .get(DomainConstants.SELECT_ID);
        }

        //Form the select expression to get the Program of the
        // Project
        StringBuffer sbProgIdSelect = new StringBuffer("to[");
        sbProgIdSelect
                .append(ProductLineConstants.RELATIONSHIP_PROGRAM_PROJECT);
        sbProgIdSelect.append("].from.");
        sbProgIdSelect.append(DomainConstants.SELECT_ID);

        StringBuffer sbProgNameSelect = new StringBuffer("to[");
        sbProgNameSelect
                .append(ProductLineConstants.RELATIONSHIP_PROGRAM_PROJECT);
        sbProgNameSelect.append("].from.");
        sbProgNameSelect.append(DomainConstants.SELECT_NAME);

        List lstProjSelects = new StringList(sbProgIdSelect.toString());
        lstProjSelects.add(sbProgNameSelect.toString());

        //Get the Governing Project for all the Products
        BusinessObjectWithSelectList lstProductData = BusinessObject
                .getSelectBusinessObjectData(
                                             context,
                                             arrObjId,
                                             (StringList) lstProjSelects);

        String strProgramId = DomainConstants.EMPTY_STRING;
        String strProgramName = DomainConstants.EMPTY_STRING;
        List lstProgramList = new Vector();
        StringBuffer sbHref = new StringBuffer();

        for (int i = 0; i < lstProjList.size(); i++) {
            strProgramId = lstProductData
                    .getElement(i).getSelectData(sbProgIdSelect.toString());
            strProgramName = lstProductData
                    .getElement(i).getSelectData(sbProgNameSelect.toString());
            if (strProgramId != null
                    && !strProgramId
                            .equals(DomainConstants.EMPTY_STRING)
                    && !strProgramId.equalsIgnoreCase("null")) {
                sbHref = new StringBuffer();
                if(strReportFormat!=null
                        &&!strReportFormat.equals("null")
                        &&!strReportFormat
                            .equals(DomainConstants.EMPTY_STRING)){
                    sbHref.append(strProgramName);
                }else{
                    sbHref.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                    sbHref.append(XSSUtil.encodeForHTMLAttribute(context,strProgramId));
                    sbHref.append("&amp;mode=replace");
                    sbHref.append("&amp;AppendParameters=true");
                    sbHref.append("&amp;reloadAfterChange=true");
                    sbHref.append("')\">");
                    sbHref.append(XSSUtil.encodeForHTML(context,strProgramName));
                    sbHref.append("</A>");
                }
                lstProgramList.add(sbHref.toString());
            }else{
                lstProgramList.add(DomainConstants.EMPTY_STRING);
            }
        }
        return lstProgramList;
    }


        //Begin of add by Enovia MatrixOne for bug #300692, 03/30/2005

        /**
         * This method is called by an action trigger on Revise of 'Product' to connect
         * the new revision of the product to all the Featres/sub Features under it.
         * @param context the eMatrix <code>Context</code> object
         * @param args
         *        0 - contains the objectID of the context product
         * @throws Exception if operation fails
         * @since ProductCentral 10-6
         */

        public void copyStructureOnRevise(Context context,
                                            String args[]) throws Exception{
            //ObjectId of the context product
            String strSourceObjectId = args[0];
            //To get the latest revision of the context product
            DomainObject domObject = newInstance(context,strSourceObjectId);
            //Added for 373021
            // Variable to hold the name of the relationship between Model and
            // Products
            String strProductModelReln = ProductLineConstants.RELATIONSHIP_PRODUCTS;

            // Initialising the query expression
            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_NAME);

            // Query to get the Model related to the product
            MapList objModel = domObject.getRelatedObjects(context, strProductModelReln,
                    DomainConstants.QUERY_WILDCARD, objectSelects, null, true,
                    false, (short) 1, DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING);

            // To get the Model Id from Query result.
            String strModelId = (String) ((Hashtable) objModel.get(0))
                    .get(DomainConstants.SELECT_ID);

            //End - Bug 373021
            BusinessObject boNewRevision = domObject.getLastRevision(context);
            String strDestinationObjectId = boNewRevision.getObjectId();

            // Start of the Code to connect the Product Variant to new Revision of Product Revision
            boolean isConfigurationInstall = FrameworkUtil.isSuiteRegistered(
                    context, "appVersionVariantConfiguration", false, null,
                    null);
            if (isConfigurationInstall) {
                ArrayList arrArgs = new ArrayList();
                arrArgs.add(0, "true");
                arrArgs.add(1, strSourceObjectId);
                arrArgs.add(2, strDestinationObjectId);

                String[] arrPacked = (String[]) JPO.packArgs(arrArgs);
                JPO.invoke(context, "emxProductVariant", arrPacked,
                        "cloneProductVariantOnProductReviseAndCopy", arrPacked,
                        null);


            // Start of Code to connect Rules
            //Old relid and New relids are required for LF/CF/LF

            StringBuffer sb = new StringBuffer(250);
            String strComma = ",";
            String relationshipPattern = sb.append(ProductLineConstants.RELATIONSHIP_LOGICAL_STRUCTURES).append(strComma)
                    					   .append(ProductLineConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES).append(strComma)
                    					   .append(ProductLineConstants.RELATIONSHIP_MANUFACTURING_STRUCTURES).append(strComma).toString();

            sb = new StringBuffer(250);
            String typePattern = sb.append(ProductLineConstants.TYPE_LOGICAL_STRUCTURES).append(strComma)
            						.append(ProductLineConstants.TYPE_PRODUCTS).append(strComma)
   									.append(ProductLineConstants.TYPE_CONFIGURATION_FEATURES).append(strComma).toString();

            StringList relationshipSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            relationshipSelects.addElement(SELECT_PHYSICALID);

            DomainObject dom = new DomainObject(strSourceObjectId);
            MapList sourceObjFeatureList = dom.getRelatedObjects(context, relationshipPattern, typePattern,
           		 					false, true, (short) 1,objectSelects, relationshipSelects,
   					                 DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short) 0,
   					                 null, null, null);
            DomainObject domDestination = new DomainObject(strDestinationObjectId);

            MapList destinationeObjFeatureList = domDestination.getRelatedObjects(context, relationshipPattern, typePattern,
	 					false, true, (short) 1,objectSelects, relationshipSelects,
		                 DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short) 0,
		                 null, null, null);
            HashMap relIDMapOnProduct = new HashMap();
            Map relIDMapOnFeature = new HashMap();
            for(int i=0;i<destinationeObjFeatureList.size();i++)
            {
            	Map destinationeObjFeatureMap = (Map)destinationeObjFeatureList.get(i);
            	Map sourceObjFeatureMap = (Map)sourceObjFeatureList.get(i);
            	String newRelID = (String)destinationeObjFeatureMap.get(SELECT_RELATIONSHIP_ID);
            	String newRelPhysicalID = (String)destinationeObjFeatureMap.get(SELECT_PHYSICALID);
            	String oldRelID = (String)sourceObjFeatureMap.get(SELECT_RELATIONSHIP_ID);
            	String oldRelPhysicalID = (String)sourceObjFeatureMap.get(SELECT_PHYSICALID);
              	relIDMapOnFeature.put(oldRelID, oldRelPhysicalID+"|"+newRelPhysicalID+"|"+newRelID);
                }
            relIDMapOnProduct.putAll(relIDMapOnFeature);

            //copy Rule Structure - START
            ArrayList arrArgument = new ArrayList();
            arrArgument.add(0, strSourceObjectId);
            arrArgument.add(1, strDestinationObjectId);
            arrArgument.add(2, relIDMapOnProduct);
			arrArgument.add(3, "reviseProduct");
            String[] arrPackedArgument = (String[]) JPO.packArgs(arrArgument);
            arrPackedArgument = (String[]) JPO.packArgs(arrArgument);
            JPO.invoke(context, "emxConfigurableRules", arrPackedArgument,
                    "copyRuleStructure", arrPackedArgument,
                        null);
            }
          //copy Rule Structure - END
          // End of the Code to connect the Product Variant to new Revision of Product Revision
        }//end of function

           /**
             * This method is called to show the higher revision
             * column for product list pages other than revisions list page .
             * @param context the eMatrix <code>Context</code> object
             * @param args
             *        0 - contains the objectID of the context product
             * @throws Exception if operation fails
             * @since ProductCentral 10-6
             */
        public boolean showHigherRevisionColumn(Context context, String[] args)throws Exception
        {
            boolean bShowHigherRevisionColumn = true;
            HashMap argumentMap = (HashMap) JPO.unpackArgs(args);
            String strShowColumn = (String) argumentMap.get("showColumn");
            if(strShowColumn != null
                && !(strShowColumn.equalsIgnoreCase(""))
                && !("null".equalsIgnoreCase(strShowColumn))
                && strShowColumn.equals("false"))
            {
                bShowHigherRevisionColumn = false;
            }
            return bShowHigherRevisionColumn;
        }

    //End of add by Enovia MatrixOne for bug #301886, 04/06/2005


    //Begin of Add by Vibhu,Enovia MatrixOne for Bug 301154 on 5/3/2005
    /**
     * This method is used to return the Formatted date corrosponding to date display setting in the
     * properties file.
     * @param context The ematrix context object.
     * @param String The Date string to be format.
     * @return Formatted String for the Date depending upon Ematrix Date Format
     * @since ProductCentral10.6
     */
    public static String getDisplayDate(Context context, String strNormalDate, Locale localeObj, double dblTZOffset)
    {
        try
        {
            int iDateFrmt = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            //Modified by Vibhu,Enovia MatrixOne for Bug 304312 on 5/18/2005
            String strFormatDate="";
            strFormatDate=eMatrixDateFormat.getFormattedDisplayDateTime(context, strNormalDate, false, iDateFrmt, dblTZOffset, localeObj);
            return strFormatDate;

        }
        catch(Exception ex)
        {
            //Return the normal date value coming to method if there is any exception in formatting the date.
            return strNormalDate;
        }
    }
    //End of Add by Vibhu,Enovia MatrixOne for Bug 301154 on 5/3/2005

    /**
     * This method is used to return the Features in each level under the Product.
     * @param context The ematrix context object.
     * @param String[] The args .
     * @return MapList of all the Feature objects
     * @since ProductCentral10.6 SP1
     */
    public MapList expandStatusObjects (Context context,String[] args) throws Exception
    {
        List featureList = new MapList();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String) paramMap.get(STR_OBJECTID);
        short recurseLevel;
        String strExpandLevel = (String) paramMap.get("expandLevel");
        if(strExpandLevel.equalsIgnoreCase((ProductLineConstants.RANGE_VALUE_ALL)))
            recurseLevel = (short)0;
        else
        recurseLevel = (short)(Short.parseShort(strExpandLevel)*2);
        DomainObject domObject = new DomainObject(objectId);
        //setId(objectId);
        //Check domObject's type
        String strType = (String) domObject.getInfo(context,SELECT_TYPE);

        MapList productVariantMarFtrStructure = null;
        MapList productVariantTechFtrStructure = null;

        //added for 358261
        if (mxType.isOfParentType(context,strType,ProductLineConstants.TYPE_PRODUCT_VARIANT))
        {


                     String strObjPattern =  ProductLineConstants.TYPE_FEATURE_LIST+ "," + ProductLineConstants.TYPE_FEATURES;
                     String strRelPattern = ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST;

                     StringList objSelects = new StringList();
                     //objSelects.add("relationship["+ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].to.id");
                     objSelects.add(DomainConstants.SELECT_ID);
                     objSelects.add("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");

                     productVariantTechFtrStructure = domObject.getRelatedObjects(context,
                                                                     strRelPattern,
                                                                     strObjPattern,
                                                                     objSelects,
                                                                     null,
                                                                     false,
                                                                     true,
                                                                     (short)0,
                                                                     DomainConstants.EMPTY_STRING,
                                                                     DomainConstants.EMPTY_STRING);


                     StringBuffer strTechFtrMatchList = new StringBuffer();
                     strTechFtrMatchList.append("\"");
                     for(int iCount = 0; iCount < productVariantTechFtrStructure.size(); iCount++)
                     {
                         strTechFtrMatchList.append ((String)(DomainConstants.SELECT_ID));
                         strTechFtrMatchList.append(",");
                         strTechFtrMatchList.append ((String)("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id"));


                         if(iCount < (productVariantTechFtrStructure.size()-1))
                         {
                             strTechFtrMatchList.append(",");
                         }
                     }
                     strTechFtrMatchList.append("\"");


                     //get the children types of Product and make it a comma seperated string
                     StringBuffer sbBuffer1 = new StringBuffer(200);
                     List lstProductChildTypes = ProductLineUtil.getChildrenTypes(
                                             context,
                                             ProductLineConstants.TYPE_PRODUCTS);
                     for (int i=0; i < lstProductChildTypes.size(); i++)
                     {
                         sbBuffer1 = sbBuffer1.append(lstProductChildTypes.get(i));
                         if (i != lstProductChildTypes.size()-1)
                         {
                             sbBuffer1 = sbBuffer1.append(STR_COMMA);
                         }
                     }

                     //get the children types of Feature and make it a comma seperated string
                     List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(
                                             context,
                                             ProductLineConstants.TYPE_FEATURES);
                     sbBuffer1 = sbBuffer1.append(STR_COMMA);
                     for (int i=0; i < lstFeatureChildTypes.size(); i++)
                     {
                         sbBuffer1 = sbBuffer1.append(lstFeatureChildTypes.get(i));
                         // Modified by Enovia MatrixOne for Bug # 309224 Date 07 Nov, 2005
                         if (i != lstFeatureChildTypes.size()-1)
                         {
                             sbBuffer1 = sbBuffer1.append(STR_COMMA);
                         }
                     }

//                   Append the Marketing Feat List
                     StringList selectStmts1 = new StringList(2);
                     selectStmts1.addElement(DomainConstants.SELECT_ID);
                     selectStmts1.addElement(DomainConstants.SELECT_NAME);
                     selectStmts1.addElement("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");

                     StringList selectRelStmts1 = new StringList(6);
                     selectRelStmts1.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

                     StringBuffer stbTypeSelect1 = new StringBuffer(50);
                     stbTypeSelect1 = stbTypeSelect1.append(ProductLineConstants.TYPE_FEATURE_LIST)
                                     .append(STR_COMMA)
                                     .append(ProductLineConstants.TYPE_FEATURES)
                                     .append(STR_COMMA)
                                     .append(ProductLineConstants.TYPE_PRODUCTS);
                     StringBuffer stbRelSelect1 = new StringBuffer(50);
                     stbRelSelect1 = stbRelSelect1.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)
                                     .append(STR_COMMA)
                                     .append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);


                     productVariantMarFtrStructure = domObject.getRelatedObjects(context,
                                                          stbRelSelect1.toString(),
                                                          stbTypeSelect1.toString(),
                                                          false,
                                                          true,
                                                          recurseLevel,
                                                          selectStmts1,
                                                          selectRelStmts1,
                                                          null,
                                                          null,
                                                          null,
                                                          sbBuffer1.toString(),
                                                          null);





                   String strProductVariantParentId = domObject.getInfo(context,"to["+ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.id");
                   domObject = new DomainObject(strProductVariantParentId);

        }





        //if(domType is PV)
        //PFL..expand 1,Tech..... List

        //

        try {
            //get the children types of Product and make it a comma seperated string
            StringBuffer sbBuffer = new StringBuffer(200);
            List lstProductChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_PRODUCTS);
            for (int i=0; i < lstProductChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstProductChildTypes.get(i));
                if (i != lstProductChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }

            //get the children types of Feature and make it a comma seperated string
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_FEATURES);
            sbBuffer = sbBuffer.append(STR_COMMA);
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstFeatureChildTypes.get(i));
                // Modified by Enovia MatrixOne for Bug # 309224 Date 07 Nov, 2005
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }

            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            selectStmts.addElement("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");

            StringList selectRelStmts = new StringList(6);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

            StringBuffer stbTypeSelect = new StringBuffer(50);
            stbTypeSelect = stbTypeSelect.append(ProductLineConstants.TYPE_FEATURE_LIST)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_FEATURES)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_PRODUCTS);
            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
            featureList = domObject.getRelatedObjects(context,
                                                 stbRelSelect.toString(),
                                                 stbTypeSelect.toString(),
                                                 false,
                                                 true,
                                                 recurseLevel,
                                                 selectStmts,
                                                 selectRelStmts,
                                                 null,
                                                 null,
                                                 null,
                                                 null,//sbBuffer.toString(),
                                                 null);
            //Added for Bug No. 372374
            MapList listMF = addPlatformFeature(context,domObject,recurseLevel,stbRelSelect,stbTypeSelect,selectStmts,selectRelStmts);
            featureList.addAll(listMF);
            //End for Bug No. 372374
            String FeatureListFromId = "";
            String FeatureListId = "";
            MapList removedList = new MapList();

			if (productVariantMarFtrStructure !=null)
            {


               featureList.addAll(productVariantMarFtrStructure);

               /*Map mapChildMap = new HashMap();

               for(int iCount = 0; iCount < productVariantTechFtrStructure.size(); iCount++)
               {
                   mapChildMap.put(DomainObject.SELECT_ID, productVariantMarFtrStructure.get(iCount));

                   featureList.addll(mapChildMap);

               }*/

            }

            for(int i = 0 ; i < featureList.size(); i++ )
            {
              Map featMap = (Map)featureList.get(i);
              if(((String)featMap.get("relationship")).equals(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO))
              {
              featMap.put("hasChildren",(String)featMap.get("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]"));
              featMap.remove("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");
              featMap.put("level",Integer.toString(Integer.parseInt((String)featMap.get("level"))/2));
                featMap.put("FeatureListFromId", FeatureListFromId);
                featMap.put("FeatureListId", FeatureListId);
                FeatureListFromId = "";
                FeatureListId = "";
              } else {
                  FeatureListFromId =  (String)featMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                  FeatureListId = (String)featMap.get(DomainConstants.SELECT_ID);
                  removedList.add(featMap);
              }
            }
            if(featureList !=null && !featureList.isEmpty())
            {
                if( removedList != null && !removedList.isEmpty())
                {
                    featureList.removeAll(removedList);
                }
                HashMap hmTemp = new HashMap();
                hmTemp.put("expandMultiLevelsJPO","true");
                featureList.add(hmTemp);
            }
        }
        catch (FrameworkException Ex) {
            throw Ex;
        }
            
            return (MapList)featureList;
    }
    /**
     * This method is used to return the Features in each level under the Product.
     * @param context The ematrix context object.
     * @param String[] The args .
     * @return MapList of all the Feature objects
     * @since ProductCentral10.6 SP1
     */
    public MapList expandSelectionObjects (Context context,String[] args) throws Exception
    {
        List featureList = new MapList();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String) paramMap.get(STR_OBJECTID);
        short recurseLevel;
        String strExpandLevel = (String) paramMap.get("expandLevel");
        if(strExpandLevel.equalsIgnoreCase((ProductLineConstants.RANGE_VALUE_ALL)))
            recurseLevel = (short)0;
        else
        recurseLevel = (short)(Short.parseShort(strExpandLevel)*2);
        DomainObject domObject = new DomainObject(objectId);
        try {
            //get the children types of Product and make it a comma seperated string
            StringBuffer sbBuffer = new StringBuffer(200);
            List lstProductChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_PRODUCTS);
            for (int i=0; i < lstProductChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstProductChildTypes.get(i));
                if (i != lstProductChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }
            //get the children types of Feature and make it a comma seperated string
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_FEATURES);
            sbBuffer = sbBuffer.append(STR_COMMA);
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstFeatureChildTypes.get(i));
                // Modified by Enovia MatrixOne for Bug # 309224 Date 07 Nov, 2005
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }

     /*       if ((lstFeatureChildTypes.contains(domObject.getInfo(context, DomainConstants.SELECT_TYPE))) ||
                (strLevel == null || strLevel.equalsIgnoreCase("null") || strLevel.equalsIgnoreCase("")))
            {
            */
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            selectStmts.addElement("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");

            StringList selectRelStmts = new StringList(6);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

            StringBuffer stbTypeSelect = new StringBuffer(50);
            stbTypeSelect = stbTypeSelect.append(ProductLineConstants.TYPE_FEATURE_LIST)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_FEATURES)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_PRODUCTS);
            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
            featureList = domObject.getRelatedObjects(context,
                                                 stbRelSelect.toString(),
                                                 stbTypeSelect.toString(),
                                                 false,
                                                 true,
                                                 recurseLevel,
                                                 selectStmts,
                                                 selectRelStmts,
                                                 null,
                                                 null,
                                                 null,
                                                 sbBuffer.toString(),
                                                 null);
            for(int i = 0 ; i < featureList.size(); i++ )
            {
              Map featMap = (Map)featureList.get(i);
              featMap.put("hasChildren",(String)featMap.get("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]"));
              featMap.remove("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");
              featMap.put("level",Integer.toString(Integer.parseInt((String)featMap.get("level"))/2));
              featMap.put("FeatureListFromId", featMap.get(DomainConstants.SELECT_RELATIONSHIP_ID));
             featMap.put("FeatureListId", featMap.get(DomainConstants.SELECT_ID));
            }
            if(featureList !=null && !featureList.isEmpty())
            {
                HashMap hmTemp = new HashMap();
                hmTemp.put("expandMultiLevelsJPO","true");
                featureList.add(hmTemp);
            }
        }
        catch (FrameworkException Ex) {
            throw Ex;
        }
            //System.out.println("featureList: "+ featureList);
            return (MapList)featureList;
    }
    /**
     * This method is used to return the Features in each level under the Product.
     * @param context The ematrix context object.
     * @param String[] The args .
     * @return MapList of all the Feature objects
     * @since ProductCentral10.6 SP1
     */
    public MapList expandRuleObjects (Context context,String[] args) throws Exception
    {
        List featureList = new MapList();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);

         String strSelectedTable = (String) paramMap.get("selectedTable");
        String objectId = (String) paramMap.get(STR_OBJECTID);
        String strLevel = (String) paramMap.get("level");
        short recurseLevel;
        String strExpandLevel = (String) paramMap.get("expandLevel");
        if(strExpandLevel.equalsIgnoreCase((ProductLineConstants.RANGE_VALUE_ALL)))
            recurseLevel = (short)0;
        else
        recurseLevel = (short)(Short.parseShort(strExpandLevel)*2);
        DomainObject domObject = new DomainObject(objectId);
        try {
            //get the children types of Product and make it a comma seperated string
            StringBuffer sbBuffer = new StringBuffer(200);
            List lstProductChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_PRODUCTS);
            for (int i=0; i < lstProductChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstProductChildTypes.get(i));
                if (i != lstProductChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }
            //get the children types of Feature and make it a comma seperated string
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_FEATURES);
            sbBuffer = sbBuffer.append(STR_COMMA);
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstFeatureChildTypes.get(i));
                // Modified by Enovia MatrixOne for Bug # 309224 Date 07 Nov, 2005
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }
            //get the children types of Feature and make it a comma seperated string
            List lstRuleChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_RULE);
            sbBuffer = sbBuffer.append(STR_COMMA);
            for (int i=0; i < lstRuleChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstRuleChildTypes.get(i));
                // Modified by Enovia MatrixOne for Bug # 309224 Date 07 Nov, 2005
                if (i != lstRuleChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }
            //add fixed resource and rule extension
            sbBuffer = sbBuffer.append(STR_COMMA)
                        .append(ProductLineConstants.TYPE_FIXED_RESOURCE)
                        .append(STR_COMMA)
                        .append(ProductLineConstants.TYPE_RULE_EXTENSION);

          /*  if ((lstFeatureChildTypes.contains(domObject.getInfo(context, DomainConstants.SELECT_TYPE))) ||
                (strLevel == null || strLevel.equalsIgnoreCase("null") || strLevel.equalsIgnoreCase("")))
            {
            */
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            selectStmts.addElement("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");

            StringList selectRelStmts = new StringList(6);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

            StringBuffer stbTypeSelect = new StringBuffer(50);
            stbTypeSelect = stbTypeSelect.append(ProductLineConstants.TYPE_FEATURE_LIST)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_FEATURES)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_PRODUCTS)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_RULE)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_FIXED_RESOURCE)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_RULE_EXTENSION);

            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE);

            featureList = domObject.getRelatedObjects(context,
                                                 stbRelSelect.toString(),
                                                 stbTypeSelect.toString(),
                                                 false,
                                                 true,
                                                 recurseLevel,
                                                 selectStmts,
                                                 selectRelStmts,
                                                 null,
                                                 null,
                                                 null,
                                                 null, //sbBuffer.toString(),
                                                 null);
            //Added for Bug No. 372374
            MapList listMF = addPlatformFeature(context,domObject,recurseLevel,stbRelSelect,stbTypeSelect,selectStmts,selectRelStmts);
            featureList.addAll(listMF);
            //End for Bug No. 372374
           /* }else {
                featureList = new MapList();
            }*/
        }
        catch (FrameworkException Ex) {
            throw Ex;
        }
            //System.out.println("featureList: "+ featureList);
        MapList finalList  = new MapList();


        if(strSelectedTable.equalsIgnoreCase("FTRFeatureOptionGBOMTable") || strSelectedTable.equalsIgnoreCase("FTRFeatureOptionSelectionTable"))
        {
            for (int i = 0 ;i <featureList.size();i++){
                Hashtable map = (Hashtable)featureList.get(i);
                String strType  = (String)map.get("type");
                if(! mxType.isOfParentType(context,strType,ProductLineConstants.TYPE_FEATURES)){
                    map.put("RowEditable" , "readonly");
                }
                finalList.add(map);
            }
        }

        if(finalList.size() ==0 ){
            finalList = (MapList)featureList;
        }
        String FeatureListFromId = "";
        String FeatureListId = "";
        MapList removedList = new MapList();

        for(int i = 0 ; i < finalList.size(); i++ )
        {
          Map featMap = (Map)finalList.get(i);
			  String FeatureListFromIdValue = "";
		      FeatureListFromIdValue =  (String)featMap.get("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");

          if(((String)featMap.get("relationship")).equals(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO))

          {
              featMap.put("hasChildren",(String)featMap.get("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]"));
              featMap.remove("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");
              featMap.put("level",Integer.toString(Integer.parseInt((String)featMap.get("level"))/2));
              featMap.put("FeatureListFromId", FeatureListFromId);
              featMap.put("FeatureListId", FeatureListId);
              FeatureListFromId = "";
              FeatureListId = "";
			   }
			   else if(((String)featMap.get("relationship")).equals(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM) && FeatureListFromIdValue.equalsIgnoreCase("True"))
			   {

                  FeatureListFromId =  (String)featMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                  FeatureListId = (String)featMap.get(DomainConstants.SELECT_ID);
                  removedList.add(featMap);

			   }
			  else if(!FeatureListFromIdValue.equalsIgnoreCase("False"))
			  {
                  removedList.add(featMap);
          }

        }
        if(finalList !=null && !finalList.isEmpty())
        {
            if( removedList != null && !removedList.isEmpty())
            {
                finalList.removeAll(removedList);
            }
            HashMap hmTemp = new HashMap();
            hmTemp.put("expandMultiLevelsJPO","true");
            finalList.add(hmTemp);
        }
            //return (MapList)featureList;
        return finalList ;
    }
    /**
     * This method is used to return the Features in each level under the Product.
     * @param context The ematrix context object.
     * @param String[] The args .
     * @return MapList of all the Feature objects
     * @since ProductCentral10.6 SP1
     */
    public MapList expandGBOMObjects (Context context,String[] args) throws Exception
    {
        List featureList = new MapList();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String) paramMap.get(STR_OBJECTID);
        short recurseLevel;
        String strExpandLevel = (String) paramMap.get("expandLevel");
        if(strExpandLevel.equalsIgnoreCase((ProductLineConstants.RANGE_VALUE_ALL)))
            recurseLevel = (short)0;
        else
        recurseLevel = (short)(Short.parseShort(strExpandLevel)*2);
        DomainObject domObject = new DomainObject(objectId);

/*Start of Add by Sandeep, Enovia MatrixOne for Bug # 310542*/
        BusinessTypeList btlTemp = new BusinessTypeList();
        RelationshipType rstGBOMto = new RelationshipType ("GBOM To");
        btlTemp = rstGBOMto.getToTypes(context, true);

/*End of Add by Sandeep, Enovia MatrixOne for Bug # 310542*/

        try {
            //get the children types of Product and make it a comma seperated string
            StringBuffer sbBuffer = new StringBuffer(300);
            List lstProductChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_PRODUCTS);
            for (int i=0; i < lstProductChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstProductChildTypes.get(i));
                if (i != lstProductChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }
            //get the children types of Feature and make it a comma seperated string
            List lstFeatureChildTypes = ProductLineUtil.getChildrenTypes(
                                    context,
                                    ProductLineConstants.TYPE_FEATURES);
            sbBuffer = sbBuffer.append(STR_COMMA);
            for (int i=0; i < lstFeatureChildTypes.size(); i++)
            {
                sbBuffer = sbBuffer.append(lstFeatureChildTypes.get(i));
                // Modified by Enovia MatrixOne for Bug # 309224 Date 07 Nov, 2005
                if (i != lstFeatureChildTypes.size()-1)
                {
                    sbBuffer = sbBuffer.append(STR_COMMA);
                }
            }

/*Start of Add by Sandeep, Enovia MatrixOne for Bug # 310542*/
            for (int i=0; i<btlTemp.size(); i++)
                {
                    sbBuffer.append(STR_COMMA).append(btlTemp.elementAt(i));
                }
/*End of Add by Sandeep, Enovia MatrixOne for Bug # 310542*/




  /*          if ((lstFeatureChildTypes.contains(domObject.getInfo(context, DomainConstants.SELECT_TYPE))) ||
                (strLevel == null || strLevel.equalsIgnoreCase("null") || strLevel.equalsIgnoreCase("")))
            {*/
            StringList selectStmts = new StringList(2);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            selectStmts.addElement("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");
            selectStmts.addElement("relationship["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"]");

            StringList selectRelStmts = new StringList(6);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

            StringBuffer stbTypeSelect = new StringBuffer(300);
            stbTypeSelect = stbTypeSelect.append(ProductLineConstants.TYPE_FEATURE_LIST)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_FEATURES)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_PRODUCTS)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.TYPE_GBOM);

/*Start of Add by Sandeep, Enovia MatrixOne for Bug # 310542*/
            for (int i=0; i<btlTemp.size(); i++)
            {
                stbTypeSelect.append(STR_COMMA).append(btlTemp.elementAt(i));
            }
/*End of Add by Sandeep, Enovia MatrixOne for Bug # 310542*/

            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_GBOM_FROM)
                            .append(STR_COMMA)
                            .append(ProductLineConstants.RELATIONSHIP_GBOM_TO);
            featureList = domObject.getRelatedObjects(context,
                                                 stbRelSelect.toString(),
                                                 stbTypeSelect.toString(),
                                                 false,
                                                 true,
                                                 recurseLevel,
                                                 selectStmts,
                                                 selectRelStmts,
                                                 null,
                                                 null,
                                                 null,
                                                 null, //sbBuffer.toString(),
                                                 null);
            //Added for Bug No. 372374
            MapList listMF = addPlatformFeature(context,domObject,recurseLevel,stbRelSelect,stbTypeSelect,selectStmts,selectRelStmts);
            featureList.addAll(listMF);
            //End for Bug No. 372374
            String FeatureListFromId = "";
            String FeatureListId = "";
            MapList removedList = new MapList();
            for(int i = 0 ; i < featureList.size(); i++ )
            {
              Map featMap = (Map)featureList.get(i);
              String GBOMFromValue =  (String)featMap.get("relationship["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"]");
              if(((String)featMap.get("relationship")).equals(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO))
              {
                  if(GBOMFromValue.equalsIgnoreCase("false"))
                  {
                  featMap.put("hasChildren",(String)featMap.get("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]"));
                  }
                  else{
						featMap.put("hasChildren",(String)featMap.get("relationship["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"]"));
					  }
                  featMap.put("FeatureListFromId", FeatureListFromId);
                  featMap.put("FeatureListId", FeatureListId);
                  FeatureListFromId = "";
                  FeatureListId = "";
              }
              else if(((String)featMap.get("relationship")).equals(ProductLineConstants.RELATIONSHIP_GBOM_TO))
              {
                  featMap.put("hasChildren",(String)featMap.get("relationship["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"]"));

              } else if(((String)featMap.get("relationship")).equals(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM))
              {
                  FeatureListFromId =  (String)featMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                  FeatureListId = (String)featMap.get(DomainConstants.SELECT_ID);
                  removedList.add(featMap);
              } else {
                  removedList.add(featMap);
              }
              featMap.remove("relationship["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");
              featMap.remove("relationship["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"]");
              featMap.put("level",Integer.toString(Integer.parseInt((String)featMap.get("level"))/2));
            }
            if(featureList !=null && !featureList.isEmpty())
            {
                if( removedList != null && !removedList.isEmpty())
                {
                    featureList.removeAll(removedList);
                }
                HashMap hmTemp = new HashMap();
                hmTemp.put("expandMultiLevelsJPO","true");
                featureList.add(hmTemp);
            }
        }
        catch (FrameworkException Ex) {
            throw Ex;
        }
            //System.out.println("featureList: "+ featureList);
            return (MapList)featureList;
    }
    //Begin of Add by Vibhu,Enovia MatrixOne for Bug 311803 on 11/22/2005
    /**
     * This method is used to return the boolean True or False for the Access on the Project of the context user.
     * @param context The ematrix context object.
     * @param strProjectId String containing the ID of the Project.
     * @return boolean true or false
     * @since ProductCentral10.6 SP1
     */
    public static boolean hasAccessOnProject(Context context,String strProjectId) throws Exception
    {

        String strUser = context.getUser();
        StringBuffer sbWhere = new StringBuffer();
        sbWhere.append(DomainConstants.SELECT_NAME);
        sbWhere.append("==\"");
        sbWhere.append(strUser);
        sbWhere.append("\"");

        DomainObject domTemp = new DomainObject(strProjectId);
        MapList mlMemberList = domTemp.getRelatedObjects(context,
                                                        DomainConstants.RELATIONSHIP_MEMBER,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        null,
                                                        null,
                                                        false,
                                                        true,
                                                        (short)1,
                                                        sbWhere.toString(),
                                                        null);

        if (mlMemberList.size()>0) {
            return true;
        } else {
            return false;
        }
    }


   /** This column JPO method is used to get the HTML Output for the Governing Project.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - String array containing following packed HashMap
    *                       with following elements:
    *                       paramMap - The HashMap containig the object id.
    * @return String - The program HTML output containing the Governing Project Name.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6.SP1
    */
    public String getHTMLForGovProject(Context context, String[] args) throws Exception{
        //Get the object id of the context object
        Map programMap = (HashMap) JPO.unpackArgs(args);
        Map relBusObjPageList = (HashMap) programMap.get("paramMap");
        Map mpRequest = (HashMap) programMap.get("requestMap");
        String strObjectId = (String)relBusObjPageList.get("objectId");
        String strMode = (String)mpRequest.get("mode");

        Map fieldMap = (HashMap) programMap.get("fieldMap");
        String strFieldName = (String)fieldMap.get("name");

        //Form the select expressions for getting the Governing Project Attributes.
        StringBuffer sbGovProjNameSelect  = new StringBuffer("from[");
        sbGovProjNameSelect.append(ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT);
        sbGovProjNameSelect.append("].to.");
        sbGovProjNameSelect.append(DomainConstants.SELECT_NAME);
        StringBuffer sbGovProjIdSelect  = new StringBuffer("from[");
        sbGovProjIdSelect.append(ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT);
        sbGovProjIdSelect.append("].to.");
        sbGovProjIdSelect.append(DomainConstants.SELECT_ID);
        StringBuffer sbGovProjTypeSelect  = new StringBuffer("from[");
        sbGovProjTypeSelect.append(ProductLineConstants.RELATIONSHIP_GOVERNING_PROJECT);
        sbGovProjTypeSelect.append("].to.");
        sbGovProjTypeSelect.append(DomainConstants.SELECT_TYPE);

        StringList lstObjSelects = new StringList();
        lstObjSelects.add(sbGovProjNameSelect.toString());
        lstObjSelects.add(sbGovProjIdSelect.toString());
        lstObjSelects.add(sbGovProjTypeSelect.toString());

        String strGovProjId = DomainConstants.EMPTY_STRING;
        String strGovProjName = DomainConstants.EMPTY_STRING;
        String strGovProjType =  DomainConstants.EMPTY_STRING;
        StringBuffer sbHref  = new StringBuffer();
        StringBuffer sbBuffer  = new StringBuffer();
        String strTypeIcon = DomainConstants.EMPTY_STRING;

        //Get the GovProject Attributes by changing the context to super user
        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
        ContextUtil.pushContext(context);
        Map mapGovProject = (Map)domObj.getInfo(context,lstObjSelects);
        ContextUtil.popContext(context);

        // If the mode is edit , display the governing project field as textbox with a chooser.

        if(strMode!=null && !strMode.equals("") &&
            !strMode.equalsIgnoreCase("null") && strMode.equalsIgnoreCase("edit"))
        {

            if(mapGovProject!=null && mapGovProject.size()>0) {
                strGovProjName = (String) mapGovProject.get(sbGovProjNameSelect.toString());
                if (mapGovProject.get(sbGovProjIdSelect.toString()) instanceof StringList){
                    StringList strGovProjListId = (StringList) mapGovProject.get(sbGovProjIdSelect.toString());
                    strGovProjId =  (String)strGovProjListId.get(0);
                } else {
                    strGovProjId = (String) mapGovProject.get(sbGovProjIdSelect.toString());
                }
                strGovProjType = (String) mapGovProject.get(sbGovProjTypeSelect.toString());
            }

            if(strGovProjName==null || strGovProjName.equalsIgnoreCase("null") || strGovProjName.equals("")){
                strGovProjName = "";
                strGovProjId = "";
                strGovProjType = "";
            }
            boolean bHasReadAccess;
            try
            {
                if (strGovProjId==null || strGovProjId.equals("null") || strGovProjId.equals("") )
                    bHasReadAccess = true;
                else
                    bHasReadAccess = emxProduct_mxJPO.hasAccessOnProject(context,strGovProjId);
            } catch (Exception e) {
                    bHasReadAccess = false;
            }
            if (!bHasReadAccess)
            {
                if (strGovProjId!=null && !strGovProjId.equals("") && !strGovProjId.equalsIgnoreCase("null"))
                {
                    strTypeIcon= getTypeIconProperty(context, strGovProjType);
                }
                if (strTypeIcon!=null && !strTypeIcon.equals("") && !strTypeIcon.equalsIgnoreCase("null")){
                    strTypeIcon = "images/"+strTypeIcon;
                }
                sbBuffer.append("<img border=\"0\" src=\"");
                sbBuffer.append(strTypeIcon);
                sbBuffer.append("\"</img>");
                sbBuffer.append(SPACE);
                sbBuffer.append(XSSUtil.encodeForXML(context,strGovProjName));
            } else {
                sbBuffer.append("<input type=\"text\" READONLY ");
                sbBuffer.append("name=\"");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
                sbBuffer.append("Display\" id=\"\" value=\"");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strGovProjName));
                sbBuffer.append("\">");
                sbBuffer.append("<input type=\"hidden\" name=\"");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
                sbBuffer.append("\" value=\"");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strGovProjId));
                sbBuffer.append("\">");
                sbBuffer.append("<input type=\"hidden\" name=\"");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
                sbBuffer.append("OID\" value=\"");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strGovProjId));
                sbBuffer.append("\">");
                sbBuffer.append("<input ");
                sbBuffer.append("type=\"button\" name=\"btnGoverningProject\" ");
                sbBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
                sbBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace");
                sbBuffer.append("&table=PLCSearchProjectsTable");
                sbBuffer.append("&selection=single");
                sbBuffer.append("&submitAction=refreshCaller&hideHeader=true");
                sbBuffer.append("&submitURL=../productline/SearchUtil.jsp?");
                sbBuffer.append("&mode=Chooser");
                sbBuffer.append("&chooserType=FormChooser");
                sbBuffer.append("&fieldNameActual=");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
                sbBuffer.append("OID");
                sbBuffer.append("&fieldNameDisplay=");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
                sbBuffer.append("Display");
//                sbBuffer.append("&fieldNameOID=");
//                sbBuffer.append(strFieldName);
//                sbBuffer.append("OID");
                //sbBuffer.append("&searchmode=chooser");
                sbBuffer.append("&suiteKey=Configuration");
                //sbBuffer.append("&searchmenu=SearchAddExistingChooserMenu");
                //sbBuffer.append("&searchcommand=PLCSearchProjectsCommand");
                sbBuffer.append("&objectId=");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strObjectId));
                sbBuffer.append("&HelpMarker=emxhelpfullsearch','850','630')\">");
                sbBuffer.append("&nbsp;&nbsp;");
                sbBuffer.append("<a href=\"javascript:basicClear('");
                sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strFieldName));
                sbBuffer.append("')\">");
                String strClear =EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Button.Clear",context.getSession().getLanguage());
                sbBuffer.append(strClear);
                sbBuffer.append("</a>");
            }
            return sbBuffer.toString();
        }else{

            if(mapGovProject!=null && mapGovProject.size()>0) {
                strGovProjName = (String) mapGovProject.get(sbGovProjNameSelect.toString());
                if (mapGovProject.get(sbGovProjIdSelect.toString()) instanceof StringList)
                {
                    StringList strGovProjListId = (StringList) mapGovProject.get(sbGovProjIdSelect.toString());
                    strGovProjId =  (String)strGovProjListId.get(0);
                } else {
                    strGovProjId = (String) mapGovProject.get(sbGovProjIdSelect.toString());
                }
                strGovProjType = (String) mapGovProject.get(sbGovProjTypeSelect.toString());
                if (strGovProjId!=null && !strGovProjId.equals("") && !strGovProjId.equalsIgnoreCase("null"))
                {
                    strTypeIcon= getTypeIconProperty(context, strGovProjType);
                }
            } else {
                strGovProjName = "";
                strGovProjId = "";
                strGovProjType = "";
            }
            if (strTypeIcon!=null && !strTypeIcon.equals("") && !strTypeIcon.equalsIgnoreCase("null")) {
                strTypeIcon = "images/"+strTypeIcon;
            }
            if(strGovProjName!=null && !strGovProjName.equals("") && !strGovProjName.equalsIgnoreCase("null")) {

                boolean bHasReadAccess;
                try
                {
                    if (strGovProjId==null || strGovProjId.equals("null") || strGovProjId.equals("") )
                      bHasReadAccess = true;
                    else
                      bHasReadAccess = emxProduct_mxJPO.hasAccessOnProject(context,strGovProjId);
                } catch (Exception e) {
                    bHasReadAccess = false;
                }

                if(bHasReadAccess) {
                    sbHref.append("<A HREF=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                    sbHref.append(XSSUtil.encodeForHTMLAttribute(context,strGovProjId));
                    sbHref.append("&mode=replace");
                    sbHref.append("&AppendParameters=true");
                    sbHref.append("&reloadAfterChange=true");
                    sbHref.append("')\"class=\"object\">");
                    sbHref.append("<img border=\"0\" src=\"");
                    sbHref.append(strTypeIcon);
                    sbHref.append("\"</img>");
                    sbHref.append("</A>");
                    sbHref.append("&nbsp");
                    sbHref.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                    sbHref.append(XSSUtil.encodeForHTMLAttribute(context,strGovProjId));
                    sbHref.append("&mode=replace");
                    sbHref.append("&AppendParameters=true");
                    sbHref.append("&reloadAfterChange=true");
                    sbHref.append("')\"class=\"object\">");
                    sbHref.append(XSSUtil.encodeForXML(context,strGovProjName));
                    sbHref.append("</A>");
                    return sbHref.toString();
                } else {
                    sbBuffer.delete(0, sbBuffer.length());
                    sbBuffer.append("<img border=\"0\" src=\"");
                    sbBuffer.append(strTypeIcon);
                    sbBuffer.append("\"</img>");
                    sbBuffer.append(SPACE);
                    sbBuffer.append(XSSUtil.encodeForXML(context,strGovProjName));
                    return sbBuffer.toString();
                }
            } else {
                return "";
            }
        }
    }
    //End of Add by Vibhu,Enovia MatrixOne for Bug 311803 on 11/22/2005

   


   public int checkForConnectedRules(Context context, String[] args)throws Exception
   {

        String strProductId = args[0];
        String[] arrObjId = new String [1];
        arrObjId[0] = strProductId;
        boolean areRulesConnected = false;

            DomainObject domProduct = new DomainObject(strProductId);
            areRulesConnected = domProduct.hasRelatedObjects(context,ProductLineConstants.RELATIONSHIP_RIGHT_EXPRESSION, false);
            if (areRulesConnected)
            {
                String language = context.getSession().getLanguage();
                String strAlertMessage =EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.ProductRemoveDelete",language);
                throw new FrameworkException(strAlertMessage);
            }
            else
            {
                areRulesConnected = domProduct.hasRelatedObjects(context,ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION, false);
                if(areRulesConnected)
                {
                    String language = context.getSession().getLanguage();
                    String strAlertMessage =EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.ProductRemoveDelete",language);

                    throw new FrameworkException(strAlertMessage);
               }
            }
           return 0;
    }

   public Boolean getFeatureActivePCs(Context context, String[] args) throws Exception
   {
       HashMap methodMap = (HashMap)JPO.unpackArgs(args);
       String objectId = (String)methodMap.get("objectId");
       String featureId = (String)methodMap.get("featureId");
       String featureListToRelId = (String)methodMap.get("featureListToRelId");
       DomainRelationship domFLTRel = new DomainRelationship(featureListToRelId);
       StringList relSelects = new StringList(1);
       relSelects.addElement("from.id");
       Hashtable htbFLId = (Hashtable)domFLTRel.getRelationshipData(context, relSelects);
       StringList slflId = (StringList)htbFLId.get("from.id");
       String flId = (String)slflId.get(0);
       String relationshipPatternPC = null;
       relationshipPatternPC = ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION;
       StringList objectSelects = new StringList(2);
       objectSelects.addElement(DomainConstants.SELECT_ID);

       Boolean featureInActivePC = false;
       DomainObject domProd = new DomainObject(objectId);
       MapList tempMapListPC = domProd.getRelatedObjects(context, relationshipPatternPC,
               DomainConstants.QUERY_WILDCARD, objectSelects, null, false,
               true, (short) 1, DomainConstants.EMPTY_STRING,
               DomainConstants.EMPTY_STRING);
       int iNumberOfPCList = tempMapListPC.size();
       Hashtable htbActivePCs = new Hashtable();
       //Get All the Active and beyon active PCs
       if (iNumberOfPCList > 0) {

           for (int j = 0; j < iNumberOfPCList; j++)
           {
               String strPCId = ((String) ((Map) tempMapListPC.get(j)).get(DomainConstants.SELECT_ID));
               try
               {
                   DomainObject domPC = new DomainObject(strPCId);
                   List lstPCStates = domPC.getStates(context);

                   String strCurrentState = domPC.getInfo(context,DomainConstants.SELECT_CURRENT);
                   Boolean bActive = false;
                   //Logic to restrict the deletion of Product when connected PC is in Active state or beyond Active State
                   for(int icnt=0;icnt<lstPCStates.size();icnt++)
                   {

                       State stState = (State)lstPCStates.get(icnt);
                       String strState = stState.getName();

                       if(strState.equals(ProductLineConstants.STATE_ACTIVE))
                       {
                           bActive = true;
                       }

                       if(strState.equals(strCurrentState))
                       {
                           if(bActive)
                           {
                               htbActivePCs.put(j+"",strPCId);
                               break;
                           }
                           else
                           {
                              break;
                           }

                       }
                   }
               }catch(Exception e)
               {

               }

           }
           Enumeration htbActivePCKeys = htbActivePCs.keys();
           String strKey = null;
           String strPCId = null;
           DomainObject domPC = null;
           relationshipPatternPC = ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS;
           while(htbActivePCKeys.hasMoreElements())
           {
               strKey = (String)htbActivePCKeys.nextElement();
               strPCId = (String)htbActivePCs.get(strKey);
               domPC = new DomainObject(strPCId);
               MapList mpSoList = domPC.getRelatedObjects(context,
                       relationshipPatternPC,
                       "*",
                       objectSelects,
                       null,
                       false,
                       true,
                       (short)1,
                       "",
                       "");
               String strFeatId = null;
               Map mpSo = null;
               for(int iSize = 0; iSize < mpSoList.size();iSize++)
               {
                   mpSo= (Map)mpSoList.get(iSize);
                   strFeatId = (String)mpSo.get(DomainConstants.SELECT_ID);
                   if(strFeatId.equals(flId) || strFeatId.equals(featureId))
                   {
                       featureInActivePC = true;
                   }
               }

           }
       }
       return featureInActivePC;
   }

   /* This Method Added to Include the Feature from Platform to the Product Feature Structure
   *  Created for Bug No. 372374
   *
   * @param context the eMatrix <code>Context</code> object
   * @param DomainObject - Current Object
   * @return String - The program HTML output containing the Governing Project Name.
   * @throws Exception if the operation fails
   */


  private MapList addPlatformFeature(Context context, DomainObject domObject,
           short recurseLevel, StringBuffer stbRelSelect,
           StringBuffer stbTypeSelect, StringList selectStmts,
           StringList selectRelStmts) throws Exception {

      String strObjType = domObject.getInfo(context,DomainConstants.SELECT_TYPE);
      StringList strObjSelect = new StringList(4);
      strObjSelect.add(DomainObject.SELECT_ID);
      strObjSelect.add(ProductLineConstants.SELECT_FEATURE_TYPE);

      MapList prodRelObjs = domObject.getRelatedObjects(
              context,
              ProductLineConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST,
              ProductLineConstants.TYPE_FEATURE_LIST,
              strObjSelect, null, false, true, recurseLevel,
              "", ProductLineConstants.EMPTY_STRING);

              StringBuffer strMatchList = new StringBuffer();
              strMatchList.append("\"");
              String strPVFLId = "";
              for (int j = 0; j < prodRelObjs.size(); j++) {
                  strPVFLId = (String) ((Map) prodRelObjs.get(j))
                          .get(DomainObject.SELECT_ID);
                  strMatchList.append(strPVFLId);
                  if (j < prodRelObjs.size() - 1)
                      strMatchList.append(STR_COMMA);
              }
              strMatchList.append("\"");


              StringBuffer sbWhereClause = new StringBuffer(200);
              if(strObjType!=null && !strObjType.equals("") && !strObjType.equals("null") && strObjType.equals(ProductLineConstants.TYPE_PRODUCT_VARIANT))
              {
                  String tempProdId = domObject.getInfo(context,"to["+ProductLineConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.id");
                  if(tempProdId!=null && !tempProdId.equals("") && !tempProdId.equals("null"))
                  {
                      DomainObject tempDomObj = new DomainObject(tempProdId);
                      domObject = tempDomObj;
                  }
              }
              sbWhereClause.append("(relationship["+ ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].to.id matchlist("+ strMatchList.toString()+")\",\")");
              //Get the Product Platform
              String strPlatFormId = domObject.getInfo(context, "to["
                      + ProductLineConstants.RELATIONSHIP_PRODUCTS
                      + "].from.from["
                      + ProductLineConstants.RELATIONSHIP_PRODUCT_PLATFORM
                      + "].to.id");

              MapList productPlatformRelObjs = new MapList();
              if(strPlatFormId!=null && !("").equals(strPlatFormId))
              {
              //Create it's domain object
              DomainObject objPlatForm = new DomainObject(strPlatFormId);
              //Get Platfrom Structre related to matchlist
                  productPlatformRelObjs = objPlatForm.getRelatedObjects(context,
                      stbRelSelect.toString(), stbTypeSelect.toString(),
                      selectStmts, selectRelStmts, false, true, recurseLevel,
                      sbWhereClause.toString(), "");
              }

    return productPlatformRelObjs;
  }//End... of method for Bug No. 372374

  /**
   * Relationship create and delete check trigger  to block operation if From object is in Release State.
   * @param context - Matrix context
   * @param args - FROMOBJID /ToOBJECTID
   * @return int - Return 0 is From object is not in release/beyond state.
   * @throws Exception
   */
  public int checkFromObjectIsReleased(Context context, String args[])
  throws Exception {
	  int result =0;
	  String strFromObjId = args[0];
	  String strToObjId = args[1];
	  String strrelType = args[2];
  
	  Boolean isRelationship = false;
	  DomainObject domFromObj = new DomainObject(strFromObjId);
	  if (domFromObj.exists(context)) {   
		  
		  	String strObjCurrent = domFromObj.getInfo(context,ProductLineConstants.SELECT_CURRENT);	 
		    
		  if(ProductLineConstants.STATE_RELEASE.equals(strObjCurrent)){
			  String strLanguage = context.getSession().getLanguage();
			  String strErrMssg = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.ReleasedorBeyond",strLanguage);
			  throw new FrameworkException(strErrMssg);
		  }
	  }
	  return result;
  }

  /**
   *  This function gets the Icon file name for any given type
   *  from the emxSystem.properties file
   *  NOTE:This method has been copied from UINavigatorUtil.java because
   *  when we use this method through UI navigator we are getting the compile error in "adele mkmk"
   *   "cannot access javax.servlet.ServletContext".
   *   (this is due to the overloaded method using this ServletContext as parameter).
   * @param context  the eMatrix <code>Context</code> object
   * @param type     object type name
   * @return         String - icon name
   * @since          PLC209 (AEF 9.5.0.0)
   */
   public static String getTypeIconProperty(Context context, String type)
   {
       String icon = EMPTY_STRING;
       String typeRegistered = EMPTY_STRING;

       try {
           if (type != null && type.length() > 0 )
           {
               String propertyKey = EMPTY_STRING;
               String propertyKeyPrefix = "emxFramework.smallIcon.";
               String defaultPropertyKey = "emxFramework.smallIcon.defaultType";

               // Get the symbolic name for the type passed in
               typeRegistered = FrameworkUtil.getAliasForAdmin(context, "type", type, true);

               if (typeRegistered != null && typeRegistered.length() > 0 )
               {
                   propertyKey = propertyKeyPrefix + typeRegistered.trim();

                   try {
                       icon = EnoviaResourceBundle.getProperty(context,propertyKey);
                   } catch (Exception e1) {
                       icon = EMPTY_STRING;
                   }
                   if( icon == null || icon.length() == 0 )
                   {
                       // Get the parent types' icon
                       BusinessType busType = new BusinessType(type, context.getVault());
                       if (busType != null)
                       {
                           StringList parentBusTypesList = busType.getParents(context);
                           String parentBusType = "";
                           if(parentBusTypesList.size()>0) {
                               parentBusType = (String)parentBusTypesList.elementAt(0);
                           }

                           if (parentBusType != null)
                               icon = getTypeIconProperty(context, parentBusType);
                       }

                       // If no icons found, return a default icon for propery file.
                       if (icon == null || icon.trim().length() == 0 )
                           icon = EnoviaResourceBundle.getProperty(context,defaultPropertyKey);
                   }
               }
           }
       } catch (Exception ex) {
           System.out.println("Error getting type icon name : " + ex.toString());
       }

       return icon;
   }

	/**
    * Method returns the First Sequence revision for the policy Product.
    * @param context the eMatrix <code>Context</code> object
    * @param args - Holds parameters passed from the calling method
    * @return - returns the First Sequence revision
    * @throws Exception if the operation fails
    */
    public String getDefaultRevision (Context context) throws Exception {
    	//TODO -Policy in combobox is not in order in which getPolicies return
        String strType = ProductLineConstants.TYPE_PRODUCTS;
		MapList policyList = com.matrixone.apps.domain.util.mxType.getPolicies(context,strType,false);
		String strDefaultPolicy = (String)((HashMap)policyList.get(0)).get(DomainConstants.SELECT_NAME);
   	    Policy policyObject = new Policy(strDefaultPolicy);
   	    String strRevision = policyObject.getFirstInSequence(context);	        
		return strRevision;
    }

    /**
    * Method returns the First Sequence revision for the policy Product.
    * @param context the eMatrix <code>Context</code> object
    * @param args - Holds parameters passed from the calling method
    * @return - returns the First Sequence revision
    * @throws Exception if the operation fails
    */
    public String getDefaultRevision (Context context, String[] args)throws Exception {
		return getDefaultRevision(context);
    }
    
    
	/**
     * Method returns the Next Sequence revision for the policy Product.
     * @param context the eMatrix <code>Context</code> object
     * @param strObjectId - string
     * @param isRootProduct - boolean
     * @return - returns the Next Sequence revision
     * @throws Exception if the operation fails
     */
    public String getNextRevisionInSequence (Context context, String strObjectId, boolean isRootProduct) throws Exception {

       String strDefaultRevision = "";
       if (isRootProduct) {
    	   strDefaultRevision = getDefaultRevision(context);
       } else {
    	   ProductLineCommon commonBean = new ProductLineCommon();
    	   //This method returns a map containing information about Type, name next revision sequence and description.
    	   //it is used to populate the default values in the dialog box
    	   Map mpObjectInfo = (HashMap)commonBean.getRevisionInfo(context, strObjectId);
    	   if (mpObjectInfo != null && mpObjectInfo.size() != 0) {
    	       strDefaultRevision = (String) mpObjectInfo.get(DomainConstants.SELECT_REVISION);
    	   }
       }
	   return strDefaultRevision;
    }
    
	/**
     * Method returns the Next Sequence revision for the policy Product.
     * @param context the eMatrix <code>Context</code> object
     * @param args - Holds parameters passed from the calling method
     * @return - returns the First Sequence revision
     * @throws Exception if the operation fails
     */
    public String getNextRevisionInSequence (Context context,String[] args) throws Exception {

	   HashMap programMap = (HashMap)JPO.unpackArgs(args);
	   HashMap requestMap = (HashMap) programMap.get("requestMap");
	   String strObjectId = (String)requestMap.get("objectID");
       String strRootProduct = (String) requestMap.get("isRootProduct");
       boolean isRootProduct = Boolean.parseBoolean(strRootProduct);
       
       return getNextRevisionInSequence(context, strObjectId, isRootProduct);
    }

    
   /**
	 * Method which does not do anything, But require as Update Program for revision field in Create Page of PL
	 * @param context
	 * @param args
	 * @throws FrameworkException
	 */
	public void emptyProgram(Context context, String []args) throws FrameworkException{
		
	}
	/**
	 * Update function which set the model prefix for product
	 * @param context
	 * @param args
	 * @throws Exception
	 */
 public void setModelPrefix(Context context, String []args) throws Exception{	
	  HashMap programMap = (HashMap) JPO.unpackArgs(args);
	  HashMap paramMap = (HashMap) programMap.get("paramMap");
	  String strObjectId = (String) paramMap.get("objectId");
	  String newPrefix=(String)paramMap.get("New Value");	  
		DomainObject domObj = DomainObject.newInstance(context, strObjectId);		
		StringList slBusTypes = new StringList();
		slBusTypes.addElement(DomainConstants.SELECT_ID);	
		Map structure = domObj.getRelatedObject(context,
				ProductLineConstants.RELATIONSHIP_PRODUCTS, false,slBusTypes, null);		
	 String modelId=(String)structure.get(DomainConstants.SELECT_ID);
	 DomainObject mod = DomainObject.newInstance(context, modelId);
	 mod.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_PREFIX, newPrefix);
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
	 * This method copies the related data from newly created Product to associated Model
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @exclude
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void copyProductAttributesToModel(Context context,String[] args) throws Exception {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap   = (HashMap)programMap.get("paramMap");
		String newObjID = (String)paramMap.get("objectId");

		// Get the new Product object
		DomainObject domProd = new DomainObject(newObjID);
		String strType = domProd.getType(context);
		
		// Create the node for the Root product and set the attributes.
		HashMap nodeAttributeMap = DerivationUtil.createDerivedNode (
				context, null, DerivationUtil.DERIVATION_LEVEL0, strType);
		if (nodeAttributeMap != null && !nodeAttributeMap.isEmpty()) {
			// Add the map values to the attributeMap.
			domProd.setAttributeValues(context, nodeAttributeMap);
		}

		// Find the model by looking at the Main Product relationship, since this is from the Create Product
		// routine, which always creates a root node.
		Map mpProd = domProd.getRelatedObject(
		          context,
		          ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT,
		          false,
		          new StringList(DomainConstants.SELECT_ID),
		          new StringList(DomainRelationship.SELECT_ID));

		// If the Model is not available then don't do further processing.
		if (mpProd!=null && mpProd.size()!=0) {
			// code to copy the Attributes of the Product to Model
			String strAttributes = EnoviaResourceBundle.getProperty(context,"emxProduct.Product.CopyToModel.Attributes");
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
		   
			Map mapModAttr = domProd.getInfo(context, strListCopyDetails);
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
		    
			// Get the Model to copy the Marketing Name and Text
			DomainObject domModel = newInstance(context, (String)mpProd.get(DomainConstants.SELECT_ID));
			domModel.setAttributeValues(context, attributeMap);
			
			// Copy the basic Attributes of the Product to Model
			String strBasics = EnoviaResourceBundle.getProperty(context,"emxProduct.Product.CopyToModel.Basics");
		    strListCopyDetails.clear();
		    strListCopyDetails.addElement(strBasics);
		    Map mapModBasics = domProd.getInfo(context, strListCopyDetails);
		    mapModBasics.remove(DomainObject.SELECT_ID);
		    mapModBasics.remove(DomainObject.SELECT_TYPE);

		   	Set setBasic = mapModBasics.keySet();
		    Iterator setBasicItr = setBasic.iterator();
		    while (setBasicItr.hasNext()) {
		         String strBasic = (String) setBasicItr.next();
		         // Check for each basic attribute and calling appropriate method as the 
		         // setAttributeValues doesn't work for basic attributes
		         if(strBasic.equalsIgnoreCase(DomainObject.SELECT_DESCRIPTION)){
		        	 domModel.setDescription(context,mapModBasics.get(strBasic).toString());	 
		         }
		         else if(strBasic.equalsIgnoreCase(DomainObject.SELECT_OWNER)){
		        	 domModel.setOwner(context, mapModBasics.get(strBasic).toString());
		         }	         
		         else if(strBasic.equalsIgnoreCase(DomainObject.SELECT_NAME)){
		        	 domModel.setName(context,mapModBasics.get(strBasic).toString());	 
		         }
		         else if(strBasic.equalsIgnoreCase(DomainObject.SELECT_POLICY)){
		        	 domModel.setPolicy(context,mapModBasics.get(strBasic).toString());	 
		         }
		         else if(strBasic.equalsIgnoreCase(DomainObject.SELECT_CURRENT)){
		        	 domModel.setState(context,mapModBasics.get(strBasic).toString());	 
		         }
 		    }
		    
		    // code to copy the Relationship data to the Model from Product
			String strRelationships = EnoviaResourceBundle.getProperty(context,"emxProduct.Product.CopyToModel.Relationships");
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
		    
		    MapList mLstModelRelatedData = domProd.getRelatedObjects(
		    		context, 
		    		strRels.toString(), 
		    		"*",
		    		new StringList(DomainObject.SELECT_ID),
		    		strRelSelect,
		    		true, 
		    		true, 
		    		(short) 1,
		    		DomainConstants.EMPTY_STRING,
		    		DomainConstants.EMPTY_STRING,
		    		0);
			
			for (int j=0; j<mLstModelRelatedData.size();j++) {
				Map mapData = (Map) mLstModelRelatedData.get(j);
				String strObjId = (String)mapData.get(DomainObject.SELECT_ID);
				String strRelName = (String)mapData.get(DomainRelationship.SELECT_NAME);
				String strDirection = (String)mapData.get(DomainRelationship.SELECT_DIRECTION);
				
				RelationshipType rtConRelType = new RelationshipType(strRelName);
				if (strDirection.equalsIgnoreCase("to")) {
					domModel.connect(context , rtConRelType , false, new DomainObject(strObjId));
				} else {
					domModel.connect(context , rtConRelType , true, new DomainObject(strObjId));
				}
			}

		}
	}

	/**
	 * Connect the Product Design Responsibility on create of the Product
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Has the packed Hashmap having information of the object in context.
	 * @return int - Returns 0 in case of the updation process is successful
	 * @throws Exception if the operation fails
	 */
	public int connectDesignResponsibility(Context context, String[] args)
	throws Exception{
		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map paramMap = (HashMap) programMap.get("paramMap");
		Map requestMap = (HashMap) programMap.get("requestMap");
		String strProductId = (String) paramMap.get("objectId");
		String strNewOrganizationId = (String) paramMap.get("New Value");
		DomainObject domainObjectToType = newInstance(context, strProductId);
		String defaultProj=PersonUtil.getDefaultProject(context, context.getUser());
		if(strNewOrganizationId != null && !"null".equalsIgnoreCase(strNewOrganizationId) && !"".equals(strNewOrganizationId))
		{
			setId(strNewOrganizationId);
			//Added for RDO Fix
			//Changing the context to super user
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), "", "");

			DomainRelationship.connect(
					context,
					this,
					ProductLineConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
					domainObjectToType);

			//Added for RDO Fix
			//Changing the context back to the context user
			ContextUtil.popContext(context);
			//In the case of Product, we will leave the relationship based code as-is and will do the attribute thing as an additional thing.
			//RDO convergence - R215
			DomainObject domObjOrgnization = new DomainObject(strNewOrganizationId);
			//TODO- Assumption Oranization Name is same as Role name 
			String strNewOrganizationName = domObjOrgnization.getInfo(context, DomainObject.SELECT_NAME);
			domainObjectToType.setPrimaryOwnership(context, defaultProj, strNewOrganizationName);
		}else{
			String defaultOrg=PersonUtil.getDefaultOrganization(context, context.getUser());
			domainObjectToType.setPrimaryOwnership(context,defaultProj,defaultOrg);
		}

		return 0;
	}

	/**
	 * Connect the Company to the Product on create of the Product
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Has the packed Hashmap having information of the object in context.
	 * @return int - Returns 0 in case of the updation process is successful
	 * @throws Exception if the operation fails
	 */
	public int connectCompanyProduct(Context context, String[] args) throws Exception
	{
		//HashMap is defined to retrieve the arguments sent by the form after unpacking.
		HashMap programMap     = (HashMap) JPO.unpackArgs(args);
		//HashMap is defined to retrieve another HashMap in the unpacked list, that has the object information
		HashMap paramMap       = (HashMap) programMap.get("paramMap");
		//Object Id of the context object is obtained from the Map.
		String  strObjectId    = (String) paramMap.get("objectId");
		//The new object id of the company that has to be used to connect with the product in context is obtained
		String  strNewValue    = (String) paramMap.get("New Value");
		//The connection between Product and Company is updated with the new value.
		if(strNewValue != null && !"null".equalsIgnoreCase(strNewValue) && !"".equals(strNewValue))
		{		
			
			boolean companyExists = FrameworkUtil.isObjectId(context,strNewValue);
			if (!companyExists)
			{
				//if the field was pre-populated with persons company, then we only have the name, need to get the id
				com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
				if(strNewValue.equals(person.getCompany(context).getName())){
					strNewValue=person.getCompanyId(context);
				}		
				
			}
			setId(strNewValue);
			DomainObject domainObjectToType = newInstance(context, strObjectId);
			
		
			//Added for RDO Fix
			//Changing the context to super user
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), "", "");

			DomainRelationship.connect(
					context,
					this,
					ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT,
					domainObjectToType);

			//Added for RDO Fix
			//Changing the context back to the context user
			ContextUtil.popContext(context);
		}
		return 0;
	}
	
    /**
     * This COLUMN JPO method is used to get the Name and Revision of the
     * Product from which the Product is derived from.
     *
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return Vector containing Name and Revision of Parents
     * @throws Exception
     */
     public Vector getColumnModelValues(Context context, String[] args) throws Exception {
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         List lstProductList = (MapList) programMap.get("objectList");
         HashMap paramList = (HashMap) programMap.get("paramList"); 
         String arrObjId[] = new String[lstProductList.size()];
         Vector columnValues = new Vector(lstProductList.size());

         String reportFormat = (String)paramList.get("reportFormat");
         if (reportFormat == null || reportFormat.isEmpty()) {
       	    reportFormat = "DEFAULT";
         }

         // Process for each row in the table
         for (int i = 0; i < lstProductList.size(); i++) {
             String strProductId = (String) ((Map) lstProductList.get(i)).get(DomainConstants.SELECT_ID);
             Product productBean = new Product(strProductId);
             String strModelId = productBean.getModelId(context);
             if (UIUtil.isNotNullAndNotEmpty(strModelId)) {
             DomainObject domModel = new DomainObject(strModelId);
             String strModelName = domModel.getInfo(context, DomainConstants.SELECT_NAME);
             if (strModelName == null) strModelName = "";
             columnValues.add(strModelName);
             }
             else{
            	 columnValues.add("");	 
             }
         }

         return columnValues;
      }



    // --------------------------------------------------------------------------------------
    // Derivations JPO variables
    // --------------------------------------------------------------------------------------
   
    private String RELATIONSHIP_MainDerived = PropertyUtil.getSchemaProperty("relationship_MainDerived");
    private String RELATIONSHIP_DERIVED = PropertyUtil.getSchemaProperty("relationship_DERIVED_ABSTRACT");
    private String RELATIONSHIP_Derived = PropertyUtil.getSchemaProperty("relationship_Derived");
    private String RELATIONSHIP_MAIN_PRODUCT = PropertyUtil.getSchemaProperty("relationship_MainProduct");
    
    // Used by two methods which retrieve Derived From
    // Form the select expression to get the Derived From ID, Marketing Name, and Revision
    // Use the "DERIVED" relationship to get the data, since this will retrieve both "Derived" and 
    // "Main Derived" relationships.
    
    private String strDERIVEDPrefix = "to[" + RELATIONSHIP_DERIVED + "].from.";
    private String strDERIVEDIdSelect = strDERIVEDPrefix + DomainConstants.SELECT_ID;
    private String strDERIVEDNameSelect = strDERIVEDPrefix + "attribute[" + ProductLineConstants.ATTRIBUTE_MARKETING_NAME + "]";
    private String strDERIVEDRevisionSelect = strDERIVEDPrefix + DomainConstants.SELECT_REVISION;
    private String strDERIVEDDisplayNameSelect = strDERIVEDPrefix + DomainConstants.SELECT_NAME;

    // Used by two methods which retrieve Derived From
    // The following two blocks comprise the data we will be comparing the return data against, since
    // we really don't know whether we have a Main Derivation or a Derivation.
    
    private String strDerivedPrefix = "to[" + RELATIONSHIP_Derived + "].from.";
    private String strDerivedIdSelect = strDerivedPrefix + DomainConstants.SELECT_ID;
    private String strDerivedNameSelect = strDerivedPrefix + "attribute[" + ProductLineConstants.ATTRIBUTE_MARKETING_NAME + "]";
    private String strDerivedRevisionSelect = strDerivedPrefix + DomainConstants.SELECT_REVISION;
    private String strDerivedDisplayNameSelect = strDerivedPrefix + DomainConstants.SELECT_NAME;
    
    private String strMainDerivedPrefix = "to[" + RELATIONSHIP_MainDerived + "].from.";
    private String strMainDerivedIdSelect = strMainDerivedPrefix + DomainConstants.SELECT_ID;
    private String strMainDerivedNameSelect = strMainDerivedPrefix + "attribute[" + ProductLineConstants.ATTRIBUTE_MARKETING_NAME + "]";
    private String strMainDerivedRevisionSelect = strMainDerivedPrefix + DomainConstants.SELECT_REVISION;
    private String strMainDerivedDisplayNameSelect = strMainDerivedPrefix + DomainConstants.SELECT_NAME;

    // --------------------------------------------------------------------------------------
    // Derivations JPO methods
    // --------------------------------------------------------------------------------------

	/**
	 * Are Product Evolutions enabled?
	 *
	 * @param context 
	 * 			the eMatrix <code>Context</code> object
	 * @param args 
	 * 			string array containing packed arguments.
	 * @return boolean
	 * @throws FrameworkException 
	 * 			If the operation fails
	 */
	 public boolean isProductEvolutionsEnabled (Context context, String args[]) throws FrameworkException {
		 return Product.isProductEvolutionsEnabled(context); 
	 }

	/**
	 * Are Product Evolutions disabled?
	 *
	 * @param context 
	 * 			the eMatrix <code>Context</code> object
	 * @param args 
	 * 			string array containing packed arguments.
	 * @return boolean
	 * @throws FrameworkException 
	 * 			If the operation fails
	 */
	 public boolean isProductEvolutionsDisabled (Context context, String args[]) throws FrameworkException {
		 return Product.isProductEvolutionsDisabled(context);
	 }
	 
	/**
	 * Used to get data for the Product Evolutions Product Context Structure Browser
	 * 
	 * Note that the Product Display should be fully expanded on display.
	 * 
	 * @param context 
	 * 			the eMatrix <code>Context</code> object
	 * @param args 
	 * 			string array containing packed arguments.
	 * @return MapList
	 * 			MapList containing the id of Product objects.
	 * @throws Exception 
	 * 			If the operation fails
	 */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	 public MapList getProductDerivations(Context context, String args[]) throws Exception
	 {
	     //The packed argument send from the JPO invoke method is unpacked to retrieve the HashMap.
	     HashMap programMap = (HashMap) JPO.unpackArgs(args);
	     
	     //The object id of the context object is retrieved from the HashMap using the appropriate key.
	     String strObjectId = (String)programMap.get("objectId");

	     // Get the consolidated list of Main Derivations, including the root.
	     MapList mlDerivations = Product.getMainDerivations(context, strObjectId);

	     // Make sure we have the levels all set correctly to the first level, since these are revisions and the root.
	     mlDerivations = DerivationUtil.fixLevelInMapList(context, mlDerivations, "1");
	     
	     return mlDerivations;
	 }

    /**
     * This COLUMN JPO method is used to get the Derivation Type of the Product
     *
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return Vector containing Derivation types.
     * @throws Exception
     */
     public Vector getColumnDerivationType(Context context, String[] args) throws Exception {
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         List lstProductList = (MapList) programMap.get("objectList");
         HashMap paramList = (HashMap)programMap.get("paramList");
         Vector columnValues = new Vector(lstProductList.size());

         String strDerivationType = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Derivation.Type.Derivation", context.getSession().getLanguage());
    			
         String strRevisionType =EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Derivation.Type.Revision",context.getSession().getLanguage());

    	    // Process for each row in the table
         for (int i = 0; i < lstProductList.size(); i++) {
     	    // Get the object ID
             String objectID = (String) ((Map) lstProductList.get(i))
                    .get(DomainConstants.SELECT_ID);
             // Call the bean to get the Derivation Type
             String derType = DerivationUtil.getDerivationType(context, objectID);
            
            // Internationalize
            if (derType.equals(DerivationUtil.DERIVATION_TYPE_DERIVATION)) {
         	   columnValues.add(strDerivationType);
            } else if (derType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) {
         	   columnValues.add(strRevisionType);
            } else {
         	   columnValues.add("");
            }
        }
        return columnValues;
     }

     /**
      * This COLUMN JPO method is used to get the Derivation Cue image of the Product
      *
      * @param context The ematrix context of the request.
      * @param args string array containing packed arguments.
      * @return Vector containing Derivation types.
      * @throws Exception
      */
      public Vector getColumnDerivationCue(Context context, String[] args) throws Exception {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          List lstProductList = (MapList) programMap.get("objectList");
          HashMap paramList = (HashMap)programMap.get("paramList");
   	      String reportFormat = (String)paramList.get("reportFormat");
          Vector columnValues = new Vector(lstProductList.size());
          
          // Define the icon
          String strIconDerivationCue = "iconStatusRevisionOrDerivation.png";
          
  	      //Reading the tooltip from property file.
    	  String strTooltipDerivationCue = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Derivation.Tooltip.DerivationCue",context.getSession().getLanguage());

    	  // Process for each row in the table
          for (int i = 0; i < lstProductList.size(); i++) {
      	    // Get the object ID
              String objectID = (String) ((Map) lstProductList.get(i)).get(DomainConstants.SELECT_ID);
              // Call the bean to get the Derivation Type
              String derType = DerivationUtil.getDerivationType(context, objectID);
             
             // Internationalize
             if (derType.equals(DerivationUtil.DERIVATION_TYPE_DERIVATION)) {
            	 String strNewIcon = "";
                 if (reportFormat != null && "CSV".equalsIgnoreCase(reportFormat)) {
         			  strNewIcon = strTooltipDerivationCue;
 	             } else {
 	            	  strNewIcon =
 	                         "<img src=\"../common/images/"
 	                                + XSSUtil.encodeForHTMLAttribute(context,strIconDerivationCue)
 	                                + "\" border=\"0\"  align=\"middle\" "
 	                                + "TITLE=\""
 	                                + " "
 	                                + XSSUtil.encodeForHTMLAttribute(context,strTooltipDerivationCue)
 	                                + "\""
 	                                + "/>";
 	             }
                 columnValues.add(strNewIcon);
             } else if (derType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) {
          	     columnValues.add("");
             } else {
          	     columnValues.add("");
             }
         }
         return columnValues;
      }

   /**
    * This COLUMN JPO method is used to get the Latest Cue image of the Product
    *
    * @param context The ematrix context of the request.
    * @param args string array containing packed arguments.
    * @return Vector containing Derivation types.
    * @throws Exception
    */
    public Vector getColumnLatestCue(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        List lstProductList = (MapList) programMap.get("objectList");
        HashMap paramList = (HashMap)programMap.get("paramList");
    	String reportFormat = (String)paramList.get("reportFormat");
        Vector columnValues = new Vector(lstProductList.size());
           
        // Define the icons 
        String strIconLatestCue = "iconStatusLatestRevision.png";
        String strIconLatestReleasedCue = "iconStatusLatestReleasedRevision.png";
           
   	    //Reading the tooltip from property file.
     	String strTooltipLatestCue = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Table.ProductEvolutions.Cue.Latest",context.getSession().getLanguage());
     	String strTooltipLatestReleasedCue = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Table.ProductEvolutions.Cue.LatestReleased",context.getSession().getLanguage());

     	// Process for each row in the table
        for (int i = 0; i < lstProductList.size(); i++) {
       	    // Get the object ID
            String objectID = (String) ((Map) lstProductList.get(i)).get(DomainConstants.SELECT_ID);
               
            boolean isLatest = false;
            boolean isLatestReleased = false;
            
            // If the current object is a Derivation, it does not get Latest or Latest Released Flag.
            String strDerivationType = DerivationUtil.getDerivationType(context, objectID);
            if (strDerivationType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) {
	            // Check for Latest.  If there are no Revisions on the forward chain, then it is the Latest.
	            MapList mlRevisionsOnChain = DerivationUtil.getAllDerivationsOfRelationship(
	            	   context, objectID, DerivationUtil.RELATIONSHIP_MAIN_DERIVED);
	            if (mlRevisionsOnChain == null || mlRevisionsOnChain.size() == 0) {
	             	isLatest = true;
	            }
	               
	            // Check for Latest Released.  If the current object is in State Released, then check the forward
	            // Revision Chain.  If any of those Products are in state released, then current product is not Latest Released.
	            DomainObject productObj = new DomainObject(objectID);
	            String strCurrent = productObj.getInfo(context, DomainObject.SELECT_CURRENT);
	            if (strCurrent.equals(ProductLineConstants.STATE_RELEASE)) {
	            	isLatestReleased = true;
	            	if (mlRevisionsOnChain != null && mlRevisionsOnChain.size() > 0) {
		            	for (int j = 0; j < mlRevisionsOnChain.size(); j++) {
		            		String revisionCurrent = (String) ((Map) mlRevisionsOnChain.get(j)).get(DomainObject.SELECT_CURRENT);
		            		if (revisionCurrent.equals(ProductLineConstants.STATE_RELEASE)) {
		            			isLatestReleased = false;
		            			break;
		            		}
		            	}
	            	}
	            }
            }
              
            // Internationalize
            String strNewIcon = "";
            if (isLatestReleased) {
                if (reportFormat != null && "CSV".equalsIgnoreCase(reportFormat)) {
          		    strNewIcon = strTooltipLatestReleasedCue;
  	            } else {
                    strNewIcon =
  	                       "<img src=\"../common/images/"
  	                               + XSSUtil.encodeForHTMLAttribute(context,strIconLatestReleasedCue)
  	                               + "\" border=\"0\"  align=\"middle\" "
  	                               + "TITLE=\""
  	                               + " "
  	                               + XSSUtil.encodeForHTMLAttribute(context,strTooltipLatestReleasedCue)
  	                               + "\""
  	                               + "/>";
                }
                columnValues.add(strNewIcon);
            } else if (isLatest) {
                if (reportFormat != null && "CSV".equalsIgnoreCase(reportFormat)) {
       		        strNewIcon = strTooltipLatestCue;
                } else {
           	        strNewIcon =
                           "<img src=\"../common/images/"
                                   + XSSUtil.encodeForHTMLAttribute(context,strIconLatestCue)
                                   + "\" border=\"0\"  align=\"middle\" "
                                   + "TITLE=\""
                                   + " "
                                   + XSSUtil.encodeForHTMLAttribute(context,strTooltipLatestCue)
                                   + "\""
                                   + "/>";
                }
                columnValues.add(strNewIcon);
            } else {
       	        columnValues.add("");
            }
        }
        return columnValues;
    }

   /**
    * This COLUMN JPO method is used to get the Derivation Level of the Product
    *
    * @param context The ematrix context of the request.
    * @param args string array containing packed arguments.
    * @return Vector containing Derivation Levels.
    * @throws Exception
    */
    public Vector getColumnDerivationLevel(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        List lstProductList = (MapList) programMap.get("objectList");
        HashMap paramList = (HashMap)programMap.get("paramList");
  		String strResourceFile = (String)paramList.get("StringResourceFileId");
  		String strSuiteKey = (String)paramList.get("suiteKey");
        Vector columnValues = new Vector(lstProductList.size());
        String arrObjId[] = new String[lstProductList.size()];

        // Process for each row in the table
        for (int i = 0; i < lstProductList.size(); i++) {
            arrObjId[i] = (String)((Map) lstProductList.get(i)).get(DomainConstants.SELECT_ID);
        }

        // Add to list of Object Selectables.
        List lstSelect = new StringList(2);
        lstSelect.add(DerivationUtil.SELECT_ATTRIBUTE_DERIVATION_LEVEL);
        lstSelect.add(DomainConstants.SELECT_TYPE);

        //Get the Derived From for all the Products
        BusinessObjectWithSelectList lstProductData = BusinessObject
                 .getSelectBusinessObjectData(context, arrObjId, (StringList)lstSelect);

        for (int j = 0; j < lstProductList.size(); j++) {
      	    // Get the associated Derivation Level for each product.
      	    String strDerivationLevel = lstProductData.getElement(j).getSelectData(DerivationUtil.SELECT_ATTRIBUTE_DERIVATION_LEVEL);

            // Get the symbolic type that we will need to build the I18N string.
      	    String strType = lstProductData.getElement(j).getSelectData(DomainConstants.SELECT_TYPE);
            String strSymbolicType = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, strType, true);
      	    
      	    // Internationalize by forming the string according to Derivation Level.
            String strMessage = "";
      	    if (ProductLineCommon.isNotNull(strDerivationLevel)) {
	              String stringKeyLevel   = "DerivationLevel." + strSymbolicType + ".Display." + strDerivationLevel;
	
	              // TODO:(OEO) Do we need to look up a default value in the string file for String Type not found?
	              // Get the internationalized string to send back.
	              
            	  strMessage = EnoviaResourceBundle.getProperty(context,strSuiteKey,stringKeyLevel.toString(), context.getSession().getLanguage());
            }
            columnValues.add(strMessage);
        }        
        return columnValues;
    }
     
   /**
    * This COLUMN JPO method is used to get the Name and Revision of the
    * Product from which the Product is derived from.
    *
    * @param context The ematrix context of the request.
    * @param args string array containing packed arguments.
    * @return Vector containing Name and Revision of Parents
    * @throws Exception
    */
    public Vector getColumnDerivedFrom(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        List lstProductList = (MapList) programMap.get("objectList");
        HashMap paramList = (HashMap) programMap.get("paramList"); 
        String arrObjId[] = new String[lstProductList.size()];
        Vector columnValues = new Vector(lstProductList.size());

        String reportFormat = (String)paramList.get("reportFormat");
        if (reportFormat == null || reportFormat.isEmpty()) {
      	    reportFormat = "DEFAULT";
        }

        // Process for each row in the table
        for (int i = 0; i < lstProductList.size(); i++) {
            arrObjId[i] = (String) ((Map) lstProductList.get(i))
                    .get(DomainConstants.SELECT_ID);
        }

        // Add to list of Object Selectables.
        List lstDerivedSelects = new StringList(4);
        lstDerivedSelects.add(DomainObject.SELECT_TYPE);
        lstDerivedSelects.add(strDERIVEDIdSelect);
        lstDerivedSelects.add(strDERIVEDNameSelect);
        lstDerivedSelects.add(strDERIVEDRevisionSelect);
        lstDerivedSelects.add("to[Main Derived].from.attribute[Title]");
        lstDerivedSelects.add("to[Derived].from.name");
        //Get the Derived From for all the Products
        BusinessObjectWithSelectList lstProductData = BusinessObject
                 .getSelectBusinessObjectData(context, arrObjId, (StringList) lstDerivedSelects);

        String strType = "";
        String strDFId = "";
        String strDFName = "";
        String strDFRevision = "";
        
        for (int j = 0; j < lstProductList.size(); j++) {
        	// Check the type to make sure we have a product!
        	strType = lstProductData.getElement(j).getSelectData(DomainObject.SELECT_TYPE);
            if (!mxType.isOfParentType(context,strType,ProductLineConstants.TYPE_PRODUCTS) && !mxType.isOfParentType(context,strType,ProductLineConstants.TYPE_MANUFACTURING_PLAN) ) {
                columnValues.add("");
                continue;
            }
        	
      	    // Get the ID.  We check the Derived relationship first, and if not found, we'll
      	    // check the Main Derived select string.
      	    strDFId = lstProductData.getElement(j).getSelectData(strDerivedIdSelect);
      	    if (strDFId.isEmpty()) {
          	    strDFId = lstProductData.getElement(j).getSelectData(strMainDerivedIdSelect);
      	    }

      	    // Get the Name.  We check the Derived relationship first, and if not found, we'll
      	    // check the Main Derived select string.
      	    strDFName = lstProductData.getElement(j).getSelectData(strDerivedNameSelect);
      	    if (strDFName.isEmpty()) {
          	    strDFName = lstProductData.getElement(j).getSelectData(strMainDerivedNameSelect);
      	    }
      	    if(strDFName.isEmpty()){
      	    	strDFName = lstProductData.getElement(j).getSelectData("to[Main Derived].from.attribute[Title]");
      	    }
      	     if(strDFName.isEmpty()){
    	    	strDFName = lstProductData.getElement(j).getSelectData("to[Derived].from.name");
    	    }

      	    // Get the Revision.  We check the Derived relationship first, and if not found, we'll
      	    // check the Main Derived select string.

      	    strDFRevision = lstProductData.getElement(j).getSelectData(strDerivedRevisionSelect);
      	    if (strDFRevision.isEmpty()) {
      		    strDFRevision = lstProductData.getElement(j).getSelectData(strMainDerivedRevisionSelect);
      	    }

      	    // If the Name is null, we didn't return any data.  This means it is the Root node, otherwise, we
      	    // want to make a link of the Name, Revision and Type Icon or just a string display based on the
      	    // passed in parameters.

            if (strDFName != null && !strDFName.isEmpty() && !strDFName.equalsIgnoreCase("null")) {

                if (reportFormat.equals("DEFAULT")) {
  	    		    // Get the Type Icon.
  	    		    //String strTypeIcon = "";
  	                //strTypeIcon = getTypeIconProperty(context, 
  	            	//	   lstProductData.getElement(j).getSelectData(DomainObject.SELECT_TYPE));
  	                //if (strTypeIcon != null && !strTypeIcon.equals("") && !strTypeIcon.equalsIgnoreCase("null"))
  		            //       strTypeIcon = "../common/images/"+strTypeIcon.trim();
  	
  	    	        // The start to the HTML is always the same, for both the Type Icon 
  	    	        // and the hyperlinked Name and Revision.
  	    	        StringBuffer sbHead = new StringBuffer();
  	    	        StringBuffer sbHref = new StringBuffer();
  	    	       
  	    	        sbHead.append("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert");
  	    	        sbHead.append("&amp;objectId=" + XSSUtil.encodeForHTMLAttribute(context,strDFId) + "'");
  	    	        sbHead.append(", '800', '700', 'true', 'popup')\">");
  		    	       
   	    	        // Part 1: Type Icon Link first
  	    	        //sbHref.append(sbHead);
  	                //sbHref.append("<img src=\"");
  	                //sbHref.append(strTypeIcon);
  	                //sbHref.append("\" border=\"0\" /></a> ");
  		               
  	                // Part 2: Name and Revision
  	      	        sbHref.append(sbHead);
  	                sbHref.append(XSSUtil.encodeForXML(context, strDFName));
  	                sbHref.append(" ");
  	                sbHref.append(XSSUtil.encodeForXML(context, strDFRevision));
  	                sbHref.append("</a>");
  	               
  	                // Part 3: Add finished product to outgoing variable.
    	            columnValues.add(sbHref.toString());

      		    } else {

      			    // Part 1: Print Plain Text of the name and revision.
      			    StringBuffer plainText = new StringBuffer();
      			    plainText.append(XSSUtil.encodeForXML(context, strDFName));
      			    plainText.append(" ");
      		 	    plainText.append(XSSUtil.encodeForXML(context, strDFRevision));
      			   
  	                // Part 2: Add finished product to outgoing variable.
      			    columnValues.add(plainText.toString());
                }
      	   } else {
               String strLanguage = context.getSession().getLanguage();
               String strRootLabel = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Derivation.Root",strLanguage);
               columnValues.add(strRootLabel);
           }
       }
       return columnValues;
     }
     

     /**
     * This FORM JPO method is used to get the Name and Revision of the
     * Product from which the Product is derived from.
     *
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return String containing Name and Revision of Parents
     * @throws Exception
     */
    public String getFormDerivedFromFieldValue(Context context, String[] args) throws Exception {
        String derivedFromValue = "";
        try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strObjectId = (String)requestMap.get("objectId");
			String suiteDir = (String)requestMap.get("SuiteDirectory");
			String suiteKey = (String)requestMap.get("suiteKey");
					
			
			Map parentInfo = DerivationUtil.getParentInfo(context, strObjectId);
			
		    if ( parentInfo == null ) {
	             String strLanguage = context.getSession().getLanguage();
		           String strRootLabel = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Derivation.Root",strLanguage);
		           derivedFromValue = strRootLabel; 
			} else {
				String strId = (String)parentInfo.get(DomainObject.SELECT_ID);
  		        String strName = (String)parentInfo.get(DomainObject.SELECT_NAME);
				String strRevision = (String)parentInfo.get(DomainObject.SELECT_REVISION);
			    StringBuffer derivedFromName = new StringBuffer();

			    derivedFromName.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
			    derivedFromName.append(XSSUtil.encodeForHTMLAttribute(context,suiteDir));
			    derivedFromName.append("&amp;suiteKey=");
			    derivedFromName.append(XSSUtil.encodeForHTMLAttribute(context,suiteKey));
			    derivedFromName.append("&amp;objectId=");
			    derivedFromName.append(XSSUtil.encodeForHTMLAttribute(context,strId));
			    derivedFromName.append("', '800', '700', 'true', 'popup')\">");

  				derivedFromName.append(XSSUtil.encodeForHTML(context, strName));
  				derivedFromName.append(" ");
  				derivedFromName.append(XSSUtil.encodeForHTML(context, strRevision));
  				derivedFromName.append("</a>");
			    
			    derivedFromName.append(" ");
   		        derivedFromValue = derivedFromName.toString();
            }
	    } catch (Exception e) {
            throw new FrameworkException(e.getMessage());
	    }
   	    return derivedFromValue;
    }

   /**
    * This FORM JPO method is used to get the Name and Revision of the
    * Product from which the Product is derived from.
    *
    * @param context The ematrix context of the request.
    * @param args string array containing packed arguments.
    * @return String containing Name and Revision of Parents
    * @throws Exception
    */
    public String getFormDerivedFromField(Context context, String[] args) throws Exception {
       //XSSOK
	   String finalName = "";
	   try {
		   HashMap programMap = (HashMap)JPO.unpackArgs(args);
		   HashMap requestMap = (HashMap) programMap.get("requestMap");
		   String strObjectId = (String)requestMap.get("objectID");
           String strRootProduct = (String) requestMap.get("isRootProduct");
           boolean isRootProduct = Boolean.parseBoolean(strRootProduct);
		   
		   if ( isRootProduct ) {
               String strLanguage = context.getSession().getLanguage();
	           String strRootLabel = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Derivation.Root",strLanguage);
	           finalName = strRootLabel; 
		   } else {
			   StringList objectSelects = new StringList();
			   objectSelects.add(DomainObject.SELECT_NAME);
			   objectSelects.add(DomainObject.SELECT_REVISION);
		    
			   // Make a domain object and get the Information.
			   DomainObject domObject = new DomainObject(strObjectId);
			   Map mapProducts = domObject.getInfo(context, objectSelects);
	        
			   if (mapProducts != null) {
				   String strName = (String)mapProducts.get(DomainObject.SELECT_NAME);
				   String strRevision = (String)mapProducts.get(DomainObject.SELECT_REVISION);
				   StringBuffer derivedFromName = new StringBuffer();
				   derivedFromName.append(XSSUtil.encodeForXML(context, strName));
				   derivedFromName.append(" ");
				   derivedFromName.append(XSSUtil.encodeForXML(context, strRevision));
				   finalName = derivedFromName.toString();
			   }
		   }
	   } catch (Exception e) {
           throw new FrameworkException(e.getMessage());
	   }
       return finalName;
    }
   
    /**
     * This FORM JPO method is used to get Derivation Type readonly value.
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return String containing HTML for combo box
     * @throws FrameworkException
     */
    public String getFormDerivationTypeFieldValue(Context context, String[] args) throws FrameworkException {
        //XSSOK
        String derivationTypeValue = "";
        try {
 	        HashMap programMap = (HashMap) JPO.unpackArgs(args);

 	  		// Get the required parameter values from the REQUEST map
 			HashMap requestMap = (HashMap) programMap.get("requestMap");
 			String objectId = (String) requestMap.get("objectId");
 	  		String languageStr = (String) requestMap.get("languageStr");
  	  	    String strSuiteKey = (String)requestMap.get("suiteKey");
 	  		// String for "Derivation"
 	  		String strDerivationType = EnoviaResourceBundle.getProperty(context,strSuiteKey,"emxProduct.Derivation.Type.Derivation",languageStr);
 	  		// String for "Revision"
 	  		String strRevisionType = EnoviaResourceBundle.getProperty(context,strSuiteKey,"emxProduct.Derivation.Type.Revision",languageStr);

 	  		String derivationType = DerivationUtil.getDerivationType(context, objectId);
 	  		if (derivationType.equals(DerivationUtil.DERIVATION_TYPE_DERIVATION)) {
 	  			derivationTypeValue = strDerivationType;
 	  		} else {
 	  			derivationTypeValue = strRevisionType;
 	  		}
        } catch (Exception e) {
 			throw new FrameworkException(e.getMessage());
 		}
    	return derivationTypeValue;
     }

   /**
    * This FORM JPO method is used to get Derivation Type field.
    * This is now a read only field, based on whether the user selected to create a Revision or Derivation.
    * @param context The ematrix context of the request.
    * @param args string array containing packed arguments.
    * @return String containing HTML for combo box
    * @throws FrameworkException
    */
    public String getFormDerivationTypeField(Context context, String[] args) throws FrameworkException {

 		StringBuffer displayField = new StringBuffer();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

	  		// Get the required parameter values from the FIELD map
			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");

	  		// Get the required parameter values from the REQUEST map
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String objectId = (String) requestMap.get("objectID");
	  		String languageStr = (String) requestMap.get("languageStr");
	  		String strSuiteKey = (String)requestMap.get("suiteKey");
 	  		String strDerivationType = (String)requestMap.get("DerivationType");
			boolean isRootProduct = Boolean.parseBoolean((String)requestMap.get("isRootProduct"));

	  		// String for "Derivation"
	  		String strDerivation =  EnoviaResourceBundle.getProperty(context,strSuiteKey,"emxProduct.Derivation.Type.Derivation",languageStr);
	  		// String for "Revision"
	  		String strRevision =  EnoviaResourceBundle.getProperty(context,strSuiteKey,"emxProduct.Derivation.Type.Revision", languageStr);

	  		// NOTE: If this is the root product, the JSP sends in type "Derivation" to control other functionality that
	  		// must occur, however, we know this is a revision, so make sure we set that here.
	  		
	  		if (isRootProduct || strDerivationType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) { 
	  			// We want a single readonly field here 
	  			displayField.append(strRevision);
	  			displayField.append("<input type=\"hidden\" name=\"");
	  			displayField.append(fieldName);
	  			displayField.append("\" id=\"");
	  			displayField.append(fieldName);
	  			displayField.append("\" value=\"");
	  			displayField.append(strRevision);
	  			displayField.append("\">");
	  			displayField.append("</input>");
	  		} else {
	  			// We want a single readonly field here 
	  			displayField.append(strDerivation);
	  			displayField.append("<input type=\"hidden\" name=\"");
	  			displayField.append(fieldName);
	  			displayField.append("\" id=\"");
	  			displayField.append(fieldName);
	  			displayField.append("\" value=\"");
	  			displayField.append(strDerivation);
	  			displayField.append("\">");
	  			displayField.append("</input>");
	  		}
	  			
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return displayField.toString();

    }   

    /**
     * This FORM JPO method is used to get Derivation Level read only field
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return String containing HTML for combo box
     * @throws FrameworkException
     */
     public String getFormDerivationLevelFieldValue(Context context, String[] args) throws FrameworkException {
    	 String derivationLevelValue = "";
    	 try {
  	        HashMap programMap = (HashMap) JPO.unpackArgs(args);

  	  		// Get the required parameter values from the REQUEST map
  			HashMap requestMap = (HashMap) programMap.get("requestMap");
  			String objectId = (String) requestMap.get("objectId");
  			String strSuiteKey = (String)requestMap.get("suiteKey");
   	  		
	 	    String strType = getFormDerivedAttribute(context, objectId, ProductLineConstants.SELECT_TYPE);
            String strSymbolicType = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, strType, true);
	 	    String strDerivationLevel = getFormDerivedAttribute(context, objectId, ProductLineConstants.SELECT_ATTRIBUTE_DERIVATION_LEVEL);
	 	    
       	    // Internationalize by forming the string according to Derivation Level.
       	    if (ProductLineCommon.isNotNull(strDerivationLevel)) {
 	              String stringKeyLevel   = "DerivationLevel." + strSymbolicType + ".Display." + strDerivationLevel;
             	  derivationLevelValue = EnoviaResourceBundle.getProperty(context,strSuiteKey,stringKeyLevel.toString(),context.getSession().getLanguage());
            }
    	 } catch (Exception e) {
    		 throw new FrameworkException(e.getMessage());
    	 }
    	 
    	 return derivationLevelValue;
     }

    /**
     * This FORM JPO method is used to get Derivation Level combo box or read only field
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return String containing HTML for combo box
     * @throws FrameworkException
     */
     public String getFormDerivationLevelField(Context context, String[] args) throws FrameworkException {
        //XSSOK properties files
  		String displayHTML = "";
 		try {
 			HashMap programMap = (HashMap) JPO.unpackArgs(args);

	  		// Get the required parameter values from the FIELD map
 			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
 			String fieldName = (String) fieldMap.get("name");

 			// Get the required parameter values from the REQUEST map
 			HashMap requestMap = (HashMap) programMap.get("requestMap");
 			String objectId = (String) requestMap.get("objectID");
 	  		String strSuiteKey = (String)requestMap.get("suiteKey");
 	  		String strSymbolicType = (String)requestMap.get("type");
 	  		String strDerivationType = (String)requestMap.get("DerivationType");
 	  		String strType = "";
 	  		
 	  		displayHTML = DerivationUtil.getDerivationLevelFormField 
 	  				(context, objectId, strSymbolicType, fieldName, strSuiteKey, strDerivationType);

 		} catch (Exception e) {
 			throw new FrameworkException(e.getMessage());
 		}
 		return displayHTML;

     }   

     /**
      * This FORM JPO method will return a Revision suggestion if the type is Revision or will return nothing
      * for a Derivation
      * @param context The ematrix context of the request.
      * @param args string array containing packed arguments.
      * @return String containing Revision Value
      * @throws FrameworkException
      */
      public String getFormDerivationRevisionValue(Context context, String[] args) throws FrameworkException {

   		String strRevision = "";
  		try {
  			HashMap programMap = (HashMap) JPO.unpackArgs(args);
  			HashMap requestMap = (HashMap) programMap.get("requestMap");
  			String strObjectId = (String) requestMap.get("objectID");
  	  		String strDerivationType = (String)requestMap.get("DerivationType");
			boolean isRootProduct = Boolean.parseBoolean((String)requestMap.get("isRootProduct"));

			if (isRootProduct || strDerivationType.equalsIgnoreCase(DerivationUtil.DERIVATION_TYPE_REVISION)) {
				strRevision = getNextRevisionInSequence (context, strObjectId, isRootProduct);
			}
  		} catch (Exception e) {
  			throw new FrameworkException(e.getMessage());
  		}
  		return strRevision;
      }   
     
   /**
    * This FORM JPO method (PRIVATE) is used to get a requested attribute
    * @param context The ematrix context of the request.
    * @param String objectID
    * @return String that contains attribute value.
    * @throws Exception
    */
    private String getFormDerivedAttribute(Context context, String objectID, String strSelectable) throws Exception {
  		// Retrieve the information from the Object ID
        DomainObject selectionObj =  new DomainObject(objectID);
        StringList selectList = new StringList();
        selectList.addElement(strSelectable);
        Hashtable objTable = (Hashtable) selectionObj.getInfo(context, selectList);
        String strReturnValue = (String)objTable.get(strSelectable);   

        // Return the Name
  		return strReturnValue;
    }

	/**
	 * This Access Expression JPO method decides to show the Type Chooser.
	 * 
	 * @param Context context
	 * @param String[] args
	 * @return Boolean
	 * @throws FrameworkException
	 */
	public static Boolean showDerivationTypeChooserField(Context context, String[] args) throws FrameworkException {
		boolean showTypeChooser = false;
		try {
			HashMap requestMap = (HashMap) JPO.unpackArgs(args);
			String isRootProduct = (String) requestMap.get("isRootProduct");
			showTypeChooser = Boolean.parseBoolean(isRootProduct);
			String UIContext = (String) requestMap.get("UIContext");
			String objectId = (String) requestMap.get("objectID");
			if (UIContext.equals("product") && objectId == null)
				showTypeChooser = true;
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return showTypeChooser;
	}

	/**
	 * Access Expression to hide type chooser field insted show harcoded type
	 * 
	 * @param Context context
	 * @param String[] args
	 * @return Boolean
	 * @throws FrameworkException
	 */
	public static Boolean showDerivationTypeField(Context context, String[] args) throws FrameworkException {
		boolean showTypeChooser = false;
		try {
			showTypeChooser = showDerivationTypeChooserField(context, args);
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return !showTypeChooser;
	}

	/**
	 * This FORM JPO will populate the type field on the Product Derivations FORM
	 * 
	 * @param Context context
	 * @param String[] args
	 * @return Boolean
	 * @throws FrameworkException
	 */
	public String getProductDerivationFormTypeField(Context context, String[] args) throws FrameworkException {
		StringBuffer strTypeChooser = new StringBuffer(200);
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String objectId = (String) requestMap.get("objectID");
			String strReturn = null;

			if (objectId != null) {
				String strSelectable = ProductLineConstants.SELECT_TYPE;
		 	    String strType = getFormDerivedAttribute(context, objectId, strSelectable);
				strTypeChooser.append(XSSUtil.encodeForHTML(context, strType));
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return strTypeChooser.toString();
	}

	/**
	 * This FORM JPO will populate the name field on the Product Derivations FORM IF Product Evolutions
	 * is turned OFF
	 * 
	 * @param Context context
	 * @param String[] args
	 * @return Boolean
	 * @throws FrameworkException
	 */
    public String getFormPreviousNameForNameField(Context context, String[] args) throws FrameworkException {
        //XSSOK
        String finalName = "";
        String finalMarkName = "";
        boolean isRootProduct;
        try {
        	HashMap programMap = (HashMap)JPO.unpackArgs(args);
        	HashMap requestMap = (HashMap) programMap.get("requestMap");
        	String strObjectId = (String)requestMap.get("objectID");
        	String strModelID = (String)requestMap.get("modelID");
        	String strRootProduct = (String) requestMap.get("isRootProduct");
        	isRootProduct = Boolean.parseBoolean(strRootProduct);
		
        	
        	//modified for IR-324015-3DEXPERIENCER2016 - all ?create? new revisions the field Name should default to the same name as the Model name
        	
			if (isRootProduct) {
	            StringList objectSelects = new StringList();
	            objectSelects.add(DomainObject.SELECT_NAME);
	            objectSelects.add(ProductLineConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
	            
	            DomainObject domObject = new DomainObject(strModelID);
	            Map mapModel = domObject.getInfo(context, objectSelects);
		        
				if (mapModel != null) {
					finalName = (String)mapModel.get(DomainObject.SELECT_NAME);
					finalMarkName = (String)mapModel.get(ProductLineConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
				}

			}else{
	            StringList objectSelects = new StringList();
	            objectSelects.add(DomainObject.SELECT_NAME);
			    
	            // Make a domain object and get the Information.
	            DomainObject domObject = new DomainObject(strObjectId);
	            Map mapProducts = domObject.getInfo(context, objectSelects);
		        
				if (mapProducts != null) {
					finalName = (String)mapProducts.get(DomainObject.SELECT_NAME);
				}
			}
        } catch (Exception e) {
            throw new FrameworkException(e.getMessage());
	    }

        StringBuffer dispField = new StringBuffer(); 
		dispField.append("<input type=\"hidden\" name=\"");
		dispField.append("DerivedName");
		dispField.append("\" value=\"");
		
		// For Fixing IR-232426V6R2014x ,dispField.append(XSSUtil.encodeForHTML(context,finalName)) replaced with dispField.append(XSSUtil.encodeForXML(context,finalName)).
		dispField.append(XSSUtil.encodeForXML(context,finalName));
		dispField.append("\">");
		dispField.append("</input>");
		if (isRootProduct) {
			dispField.append("<input type=\"hidden\" name=\"");
			dispField.append("DerivedMarkName");
			dispField.append("\" value=\"");

			// For Fixing IR-232426V6R2014x ,dispField.append(XSSUtil.encodeForHTML(context,finalName)) replaced with dispField.append(XSSUtil.encodeForXML(context,finalName)).
			dispField.append(XSSUtil.encodeForXML(context,finalMarkName));
			dispField.append("\">");
			dispField.append("</input>");
		}
        return dispField.toString();
	}

	/**
	 * Used when the Engineering Change needs to automatically revise an affected item.
	 * Creates the next Product Revision in this chain.
	 * 
	 * @param context
	 *            	The eMatrix <code>Context</code> object
	 * @param args 
	 *            	Holds the arguments 
	 * @return String
	 * 				The objectId of the new Product Revision
	 * @throws FrameworkException
	 */
	public String createProductRevisionForEC(Context context, String[] args) throws FrameworkException {
		String strProductId = null;
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			// Values we will carry over from the old product
            String strDerivedFromID = (String)programMap.get("strDerivedFromId");
			String strType = (String) programMap.get("strType");
			String strName = (String) programMap.get("strName");
			String strOwner = (String) programMap.get("strOwner");

			// Values we can calculate
			String strDerivationType = DerivationUtil.DERIVATION_TYPE_REVISION;
			String strRevision = getNextRevisionInSequence(context, strDerivedFromID, false);

			// Values we need to get from the domain object, to copy to the new object.
			DomainObject derivedFromObj = new DomainObject(strDerivedFromID);

			// Get attributes and values from the parent object.
			StringList getAttributes = new StringList();
			getAttributes.add("attribute[" + ProductLineConstants.ATTRIBUTE_MARKETING_TEXT + "]");
			getAttributes.add("attribute[" + ProductLineConstants.ATTRIBUTE_MARKETING_NAME + "]");
			getAttributes.add("attribute[" + ProductLineConstants.ATTRIBUTE_DERIVATION_LEVEL + "]");
			getAttributes.add(ProductLineConstants.SELECT_VAULT);
			getAttributes.add(ProductLineConstants.SELECT_POLICY);
			getAttributes.add(ProductLineConstants.SELECT_DESCRIPTION);

			Map productMap = derivedFromObj.getInfo(context, getAttributes);
			if (productMap != null && productMap.size() > 0) {
				String strMarketingName = (String) productMap.get("attribute[" + ProductLineConstants.ATTRIBUTE_MARKETING_NAME + "]");
				String strDerivationLevel = (String) productMap.get("attribute[" + ProductLineConstants.ATTRIBUTE_DERIVATION_LEVEL + "]");
				String strMarketingText = (String) productMap.get("attribute[" + ProductLineConstants.ATTRIBUTE_MARKETING_TEXT + "]");
				String strDescription = (String) productMap.get(ProductLineConstants.SELECT_DESCRIPTION);
				String strPolicy = (String) productMap.get(ProductLineConstants.SELECT_POLICY);
				String strVault = (String) productMap.get(ProductLineConstants.SELECT_VAULT);
	
				// Add the attributes we will need.
				HashMap objAttributeMap = new HashMap();
				objAttributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_NAME, strMarketingName);
				objAttributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_TEXT, strMarketingText);
				
				// Create the attributes to be sent to create by calling the Create Derived Node function.  This will fill the
				// Map with the attributes we will need for the new nodes.
				HashMap nodeAttrs = DerivationUtil.createDerivedNode(context, strDerivedFromID, strDerivationLevel, strType);
				if (nodeAttrs != null && nodeAttrs.size() > 0) {
					objAttributeMap.putAll(nodeAttrs);
				}
				
				// Create the new Product Derivation.
				Product productBean = new Product();
				strProductId = productBean.createProductDerivation(context, strType, strName, strRevision, 
					   strPolicy, strVault, strOwner, strDescription, objAttributeMap, strDerivationType, 
					   strDerivedFromID);
				
				// Connect it correctly to the Model. Model Id will be calculated if sent in as null.
				connectProductDerivationToModel(context, strProductId, null, strDerivedFromID, false);
			}

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
		return strProductId;
	}
    
    
	/**
	 * Used as the CreateJPO parameter of the Create New Derivation action, this method
	 * creates the new Product Derivation.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Map containing the ID of the new Product.
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map createProductDerivation(Context context, String[] args) throws FrameworkException {
		HashMap returnMap = new HashMap();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

            String strDerivedFromID = (String)programMap.get("copyObjectId");
			String strType0 = (String) programMap.get("TypeActual");
			String strType1 = (String) programMap.get("Type1");
			String strName = (String) programMap.get("Name");
            String strType = (strType0 == null ? strType1 : strType0);
			String strRevision = (String) programMap.get("Revision");
			String strMarketingName = (String) programMap.get("MarketingName");
			String strDerivationType = (String) programMap.get("DerivationType");
			String strDerivationLevel = (String) programMap.get("DerivationLevel");
			String strDescription = (String) programMap.get("Description");
			String strMarketingText = (String) programMap.get("MarketingText");
			String strPolicy = (String) programMap.get("Policy");

			String strVault = (String) programMap.get("Vault");
			String strOwner = (String) programMap.get("Owner");

			// Add the attributes we will need.
			HashMap objAttributeMap = new HashMap();
			objAttributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_NAME, strMarketingName);
			objAttributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_TEXT, strMarketingText);
			
			// If Product Evolutions are not enabled, we do not have a Derivation Level, so let's make sure to set that.
			if (Product.isProductEvolutionsDisabled(context)) {
				strDerivationLevel = DerivationUtil.DERIVATION_LEVEL0;
			}

			// Create the attributes to be sent to create by calling the Create Derived Node function.  This will fill the
			// Map with the attributes we will need for the new nodes.
			HashMap nodeAttrs = DerivationUtil.createDerivedNode(context, strDerivedFromID, strDerivationLevel, strType);
			if (nodeAttrs != null && nodeAttrs.size() > 0) {
				objAttributeMap.putAll(nodeAttrs);
			}
			
			// Create the new Product Derivation.
			Product productBean = new Product();
			String strProductId = productBean.createProductDerivation(context, strType, strName, strRevision, 
				   strPolicy, strVault, strOwner, strDescription, objAttributeMap, strDerivationType, 
				   strDerivedFromID);
			returnMap.put("id", strProductId);

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
		return returnMap;
	}

	/**
	 * Used as the PostProcessJPO parameter of the Create New Derivation action, this method
	 * connects the Product Derivation with the correct derivation relationships. 
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public String connectProductDerivationToModel(Context context, String[] args)
			throws Exception {
		String strResult = null;
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strObjectId = (String) paramMap.get("objectId");
			String strModelId = (String) requestMap.get("modelID");
			String strCopyObjId = (String) requestMap.get("copyObjectId");
            String strRootProduct = (String)requestMap.get("isRootProduct");
    		boolean isRootProduct = Boolean.parseBoolean(strRootProduct);
    		
    		strResult = connectProductDerivationToModel(
    				context, strObjectId, strModelId, strCopyObjId, isRootProduct);
		} catch (Exception e) {
			throw (new FrameworkException(e));
		}
		return strResult;
	}

    /**
	 * Used as the PostProcessJPO parameter of the Create New Derivation action, this method
	 * connects the Product Derivation with the correct derivation relationships. 
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public String connectProductDerivationToModel(Context context, 
			String strObjectId, String strModelId, String strCopyObjId, boolean isRootProduct)
			throws Exception {
		try {
    		String strRelExists = null;
    		String[] argsTemp = new String[3];
			String strCompanyId = null;
			StringList companyList = new StringList(); 

			String relModelToConnect = ProductLineConstants.RELATIONSHIP_PRODUCTS;
			
			// We need to connect to the model, so determine the Model Id of the parent.
			// THe product will use the same model.
			if (strModelId == null) {
				Product productBean = new Product(strCopyObjId);
				strModelId = productBean.getModelId(context);
			}

			if (isRootProduct) {
				// Set relationship to connect below.
				relModelToConnect = ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT;

				//Getting the company details of the context only for Root Product
	    		if(!context.getUser().equals("creator") && !context.getUser().equals("User Agent")){
	    			companyList = ProductLineUtil.getUserCompanyIdName(context);
	    		}
	    		if(companyList.size() >= 1){
	    			strCompanyId = (String)companyList.elementAt(0);
	    		}

	            argsTemp[0] = strObjectId;
	            argsTemp[1] = "to";
	            argsTemp[2] = PropertyUtil.getSchemaProperty(context,"relationship_CompanyProduct");
	            strRelExists = (String)hasRelationship(context,argsTemp);
	            
	            // ------------------------------------------------------------------------------------
	            // COMPANY PRODUCT
	            // ------------------------------------------------------------------------------------

	            String relCompanyToConnect = ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT;
	            if (strRelExists.equalsIgnoreCase("false") && ProductLineCommon.isNotNull(strModelId)) {
			        //Domain Object is instantiated with the from object id
			        DomainObject fromObject = DomainObject.newInstance(context, strCompanyId);
			        //Domain Object is instantiated with the to object id
			        DomainObject toObject = DomainObject.newInstance(context, strObjectId);
			        //The method connect of DomainRelationship class is called to connect the objects
			        DomainRelationship.connect(context, fromObject, relCompanyToConnect, toObject);
	            }
			
			}

            argsTemp[0] = strObjectId;
            argsTemp[1] = "to";
            argsTemp[2] = PropertyUtil.getSchemaProperty(context,"relationship_Products");
            strRelExists = (String)hasRelationship(context,argsTemp);
	            
            // ------------------------------------------------------------------------------------
            // PRODUCTS (or) MAIN PRODUCT
            // ------------------------------------------------------------------------------------

            if (strRelExists.equalsIgnoreCase("false") && ProductLineCommon.isNotNull(strModelId)) {
		        //Domain Object is instantiated with the from object id
		        DomainObject fromObject = DomainObject.newInstance(context, strModelId);
		        //Domain Object is instantiated with the to object id
		        DomainObject toObject = DomainObject.newInstance(context, strObjectId);
		        //The method connect of DomainRelationship class is called to connect the objects
		        DomainRelationship.connect(context, fromObject, relModelToConnect, toObject);
            }
			
			return strObjectId;
		} catch (Exception e) {
			throw (new FrameworkException(e));
		}
	}
    
	/**
	 * This will be used to called from post process URL in create Product case
	 * @param context the eMatrix <code>Context</code> object
	 * @param args JPO argument
	 * @return String XML Message
	 * @throws Exception if operation fails
	 */
	public static String postCreateGetRowXML(Context context, String[] args)
	throws FrameworkException
    {
		StringBuffer xmlString = new StringBuffer();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objId = (String) programMap.get("objectId");
			String selId = (String) programMap.get("selId");
			String strLevel = (String) programMap.get("Level");
			String strDerivationType = (String) programMap.get("DerivationType");

			DomainObject obj = new DomainObject(objId);
			Product productBean = new Product(obj);
			Map mapParentDetail = productBean.getProductParent(context);
			String relId = (String) mapParentDetail.get(DomainRelationship.SELECT_ID);

			xmlString.append("<item oid=\"");
			xmlString.append(objId);
			xmlString.append("\" relId=\"");
			xmlString.append(relId);
			xmlString.append("\" pid=\"");
			xmlString.append(selId);
		
			// If we have a Revision, we are adding below the selected row, not as a child of the selected row.
			if (strDerivationType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) {
		    	xmlString.append("\" pasteBelowToRow=\"");
		    	xmlString.append(strLevel);
			}

			xmlString.append("\">");
			xmlString.append("</item>");

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return xmlString.toString();
	}
	
   	/**
	 * This will be used to refresh the SB after a Product has been inserted in the 
     * Derivation Chain.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            JPO argument
	 * @return String XML Message
	 * @throws Exception
	 *             if operation fails
	 */
	public static String postInsertGetRowXML(Context context, String[] args)
			throws FrameworkException {
		StringBuffer xmlString = new StringBuffer();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String objId = (String) programMap.get("objectId");
			String selId = (String) programMap.get("selId");
			String selParentId = (String) programMap.get("selParentId");
			String derivedToLevel = (String) programMap.get("derivedToLevel");

			String strDerivationType = (String) programMap.get("DerivationType");
			String location = "insertBefore";
            
			// Object ID of newly created Product
			DomainObject obj = new DomainObject(objId);

			Product productBean = new Product(obj);
			Map mapParentDetail = productBean.getProductParent(context);
			String relId = (String) mapParentDetail.get(DomainRelationship.SELECT_ID);

			xmlString
					.append("<item location=\"" + location + "\" oid=\""
							+ objId + "\" relId=\"" + relId + "\" pid=\""
							+ selParentId);
			xmlString.append("\" pasteAboveToRow=\"" + derivedToLevel);
			xmlString.append("\">");
			xmlString.append("</item>");
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return xmlString.toString();
	}
	
	
	/**
	 * This will populate the Derived To Field in Insert Before form
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getInsertFormDerivedToField(Context context, String[] args) throws FrameworkException {
		StringBuffer dispField = new StringBuffer();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");
			String objectId = (String) requestMap.get("objectID");
			String strLanguage = context.getSession().getLanguage();

			Product productBean = new Product(objectId);
			Map mapSel = productBean.getProductDetail(context);
			String strRevisionSelectable = DomainObject.SELECT_REVISION;
			String strNameSelectable = DomainObject.SELECT_NAME;
			String strRevision = (String) mapSel.get(strRevisionSelectable);
			String strName = (String) mapSel.get(strNameSelectable);

			dispField.append(XSSUtil.encodeForHTML(context, strName));
			dispField.append(" ");
			dispField.append(XSSUtil.encodeForHTML(context,strRevision));
			dispField.append("<input type=\"hidden\" name=\"");
			dispField.append(XSSUtil.encodeForHTMLAttribute(context,fieldName));
			dispField.append("\" value=\"");
			dispField.append(XSSUtil.encodeForHTMLAttribute(context,objectId));
			dispField.append("\">");
			dispField.append("</input>");
			dispField.append("<input type=\"hidden\" id=\"");
			dispField.append(XSSUtil.encodeForHTMLAttribute(context,fieldName));
			dispField.append("Display\" value=\"");
			dispField.append(XSSUtil.encodeForHTMLAttribute(context,strName));
			dispField.append("\">");
			dispField.append("</input>");

		} catch (Exception e) {
			throw (new FrameworkException(e.getMessage()));
		}
		return dispField.toString();
	}

	/**
	 * This will populate the Derived From Field in Insert Before form
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getInsertFormDerivedFromField(Context context, String[] args) throws FrameworkException {
		StringBuffer dispField = new StringBuffer();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");
			String objectId = (String) requestMap.get("derivedFromID");
			String strLanguage = context.getSession().getLanguage();

			Product productBean = new Product(objectId);
			Map mapSel = productBean.getProductDetail(context);
			String strRevisionSelectable = DomainObject.SELECT_REVISION;
			String strNameSelectable = DomainObject.SELECT_NAME;
			String strRevision = (String) mapSel.get(strRevisionSelectable);
			String strName = (String) mapSel.get(strNameSelectable);

			dispField.append(XSSUtil.encodeForHTML(context, strName));
			dispField.append(" ");
			dispField.append(XSSUtil.encodeForHTML(context,strRevision));
			dispField.append("<input type=\"hidden\" name=\"");
			dispField.append(XSSUtil.encodeForHTMLAttribute(context,fieldName));
			dispField.append("\" value=\"");
			dispField.append(XSSUtil.encodeForHTMLAttribute(context,objectId));
			dispField.append("\">");
			dispField.append("</input>");
			dispField.append("<input type=\"hidden\" id=\"");
			dispField.append(XSSUtil.encodeForHTMLAttribute(context,fieldName));
			dispField.append("Display\" value=\"");
			dispField.append(XSSUtil.encodeForHTMLAttribute(context,strName));
			dispField.append("\">");
			dispField.append("</input>");

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return dispField.toString();
	}

	/**
	 * This will populate the Derivation Type Field in Insert Before form
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getInsertFormDerivationTypeField(Context context, String[] args) throws FrameworkException {
		StringBuffer displayField = new StringBuffer();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");
			String objectId = (String) requestMap.get("objectID");

			String strLanguage = context.getSession().getLanguage();

	  		// I18N String for "Derivation"
	  		String strDerivationType = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Derivation.Type.Derivation", strLanguage);
	  		// I18N String for "Revision"
	  		String strRevisionType = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Derivation.Type.Revision",strLanguage);
                            
            String derivationType = DerivationUtil.getDerivationType(context, objectId);
            String derivationDisplay = "";
            if (derivationType.equals(DerivationUtil.DERIVATION_TYPE_DERIVATION)) {
                derivationDisplay = strDerivationType;
            } else if (derivationType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) {
                derivationDisplay = strRevisionType;
            } 
  			// We want a single readonly field here
            displayField.append(derivationDisplay);
  			displayField.append("<input type=\"hidden\" name=\"");
  			displayField.append(XSSUtil.encodeForHTMLAttribute(context,fieldName));
  			displayField.append("\" value=\"");
  			displayField.append(XSSUtil.encodeForHTMLAttribute(context,derivationType));
  			displayField.append("\">");
  			displayField.append("</input>");

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return displayField.toString();
	}
    
    /**
	 * This FORM JPO method will populate the Derivation Level Field in Insert Before form
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return String containing HTML for combo box
     * @throws FrameworkException
     */
     public String getInsertFormDerivationLevelField(Context context, String[] args) throws FrameworkException {

  		StringBuffer displayField = new StringBuffer();
 		try {
 			HashMap programMap = (HashMap) JPO.unpackArgs(args);

	  		// Get the required parameter values from the FIELD map
 			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
 			String fieldName = (String) fieldMap.get("name");

 			// Get the required parameter values from the REQUEST map
 			HashMap requestMap = (HashMap) programMap.get("requestMap");
 			String toObjectId = (String) requestMap.get("objectID");
			String fromObjectId = (String) requestMap.get("derivedFromID");
 	  		String languageStr = (String) requestMap.get("languageStr");
 	  		String strSuiteKey = (String)requestMap.get("suiteKey");
 	  		
 	  		// To hold the available levels:
 	  		StringList slDerivationLevels = new StringList();
 	  		
			// Get the Derivation Info and Type from the Parent object Id
			DomainObject parentObj = new DomainObject(fromObjectId);
 			String strParentType = parentObj.getInfo(context, DomainConstants.SELECT_TYPE);
			String strSymbolicType = FrameworkUtil.getAliasForAdmin(context, "type", strParentType, true);

	  		// Get the derivation level for the parent, the string list will contain all the levels that will be
 	  		// allowed to be chosed by the user or displayed as a single read-only field.
 	  		slDerivationLevels = DerivationUtil.getAvailableDerivationLevelsForInsert
 	  				(context, fromObjectId, toObjectId, strParentType);
 	  		if (slDerivationLevels.size() == 0) {
 	  			return "";
 	  		}
 	  		
 			// Translate the list into its I18N equivalents.
 	  		String strKey = "";
 	  		String strI18N = "";
 			StringList slLevelsTranslated = new StringList();
 			for (int j = 0; j < slDerivationLevels.size(); j++) {
 				
                // TODO:(OEO) Do we need to look up a default value in the string file for String Type not found?
 				
 				String strValue = (String)slDerivationLevels.get(j);
 	 			strKey = "DerivationLevel." + strSymbolicType +".Display." + strValue;
 				strI18N = EnoviaResourceBundle.getProperty(context,strSuiteKey,strKey, languageStr);
 	 			slLevelsTranslated.add(strI18N);
 			}

 	  		// Prepare the HTML for display
 	  		if (slDerivationLevels.size() == 1) {
 	  			// We want a single readonly field here
 	  			String strValue = (String)slDerivationLevels.get(0);
 	 	  		String strValueTranslated = (String)slLevelsTranslated.get(0);
 	  			
 	  			displayField.append(strValueTranslated);
 	  			displayField.append("<input type=\"hidden\" name=\"");
 	  			displayField.append(XSSUtil.encodeForHTMLAttribute(context,fieldName));
 	  			displayField.append("\" value=\"");
 	  			displayField.append(XSSUtil.encodeForHTMLAttribute(context,strValue));
 	  			displayField.append("\">");
 	  			displayField.append("</input>");
 	  			
 	  		} else {

 	  			// Combo Box Header
 				displayField.append("<select id=\"");
 				displayField.append(XSSUtil.encodeForHTMLAttribute(context,fieldName));
 				displayField.append("\" "); 
 				displayField.append("name=\"");
 				displayField.append(XSSUtil.encodeForHTMLAttribute(context,fieldName));
 				displayField.append("\">");

 	  			for (int i = 0; i < slDerivationLevels.size(); i++) {
 	  				// Combo Box Options
 	 	  			String strValue = (String)slDerivationLevels.get(i);
 	 	 	  		String strValueTranslated = (String)slLevelsTranslated.get(i);

 	 		  		// Add Option
 	 				displayField.append("<option value=\"");
 	 				displayField.append(XSSUtil.encodeForHTMLAttribute(context,strValue));
 	 				displayField.append("\" >");
 	 				displayField.append(strValueTranslated);
 	 				displayField.append( "</option>");
 	  			}

 				// Combo Box End 
 				displayField.append("</select>");
 	  		}

 		} catch (Exception e) {
 			throw new FrameworkException(e.getMessage());
 		}
 		return displayField.toString();
     }   

	/**
	 * To insert the Product from create component, and insert before the 
     * selected Product.
	 * 
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map insertProductDerivation(Context context, String[] args) throws FrameworkException {

		HashMap returnMap = new HashMap();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestValue = (HashMap) programMap.get("RequestValuesMap");

			String strProductId = "";
			String strType = (String) programMap.get("TypeActual");
			String strName = (String) programMap.get("Name");
			String strRevision = (String) programMap.get("Revision");
			String strPolicy = (String) programMap.get("Policy");
			String strVault = (String) programMap.get("Vault");
			String strOwner = (String) programMap.get("Owner");
			String strDescription = (String) programMap.get("Description");

			String strDerivationLevel = (String) programMap.get("DerivationLevel");
			String strDerivedFromID = (String) programMap.get("derivedFromID");
			String strDerivedToID = (String) programMap.get("objectID");

			String strMarketingName = (String) programMap.get("MarketingName");
			String strMarketingText = (String) programMap.get("MarketingText");

			// Add the attributes we will need.
			HashMap objAttributeMap = new HashMap();
			objAttributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_NAME, strMarketingName);
			objAttributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_TEXT, strMarketingText);

			// Create the attributes to be sent to create by calling the Create Derived Node function.  This will fill the
			// Map with the attributes we will need for the new nodes.
			HashMap nodeAttrs = DerivationUtil.insertDerivedNode 
					(context, strDerivedFromID, strDerivedToID, strDerivationLevel, strType);
			if (nodeAttrs != null && nodeAttrs.size() > 0) {
				objAttributeMap.putAll(nodeAttrs);
			}

			try {
				Product productBean = new Product();
				strProductId = productBean.insertProductDerivation(context,
						strType, strName, strRevision, strPolicy, strVault,
						strOwner, strDescription, objAttributeMap,
						strDerivedFromID, strDerivedToID);
				returnMap.put("id", strProductId);

			} catch (Exception e) {
				throw new FrameworkException(e);
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return returnMap;
	}
    
   /**
    *  Trigger program to enforce the delete Rules for a Product Derivation.  Product cannot be 
	* frozen and product can have NO children!
	* 
	* @param Context context
	* @param String[] args
	* @return int
	* @throws Exception
	*/
    public int checkProductDerivationDeleteRules (Context context, String[] args) throws Exception,FrameworkException
	{
    	String strProductId = args[0];
	    String strAlertMessage = "";
	    String strLanguage = context.getSession().getLanguage();
	        
		// First make sure the product is not Frozen.
	    if (Product.isFrozenState(context, strProductId)) {
	    	strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Delete.FrozenState",strLanguage);
	        throw new FrameworkException(strAlertMessage);
	    }

	    // Now Make sure it has no Derivations.
	    MapList mlDerivations = DerivationUtil.getAllDerivations(context, strProductId);
	    if (mlDerivations != null && mlDerivations.size() > 0) {
	    	strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Delete.NonLastNode",strLanguage);
	        throw new FrameworkException(strAlertMessage);
	    }

	    return 0;
	}

   /**
    *  This will populate the Product Line field in the type_Products form.
    * 
    * @param Context context
    * @param String[] args
    * @return String
    * @throws Exception
    */
    public String getProductFormProductLineField (Context context, String[] args) throws FrameworkException {
	    String strProductLine = "";
	    try {
	    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
	        HashMap paramMap = (HashMap) programMap.get("paramMap"); 

	        String objectId = (String) requestMap.get("objectId");
	        String reportFormat = (String)paramMap.get("reportFormat");
	        if (reportFormat == null || reportFormat.isEmpty()) {
	      	    reportFormat = "DEFAULT";
	        }
			
			// If this is the root Product we'll need to look up on Main Product, otherwise Products.
			String strLookupRel = null;
			if (DerivationUtil.isRootNode(context, objectId)) {
				strLookupRel = ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT;
			} else {
				strLookupRel = ProductLineConstants.RELATIONSHIP_PRODUCTS;
			}
			
			// Now find the ProductLine
			String strRootSelectable = "to[" + strLookupRel + "].from.to[" + 
					ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS + "].from.";
			String strNameSelectable = strRootSelectable + "name";
			String strIdSelectable = strRootSelectable + "id";
			String strTypeSelectable = strRootSelectable + "type";
			
			StringList slObjectSelects = new StringList(2);
			slObjectSelects.add(strNameSelectable);
			slObjectSelects.add(strTypeSelectable);
			slObjectSelects.add(strIdSelectable);
			
			DomainObject doProductObject = newInstance(context, objectId);
			Map mObjectMap = doProductObject.getInfo(context, slObjectSelects);
			
			String strProductLineName = (String)mObjectMap.get(strNameSelectable);
			String strProductLineType = (String)mObjectMap.get(strTypeSelectable);
			String strProductLineId = (String)mObjectMap.get(strIdSelectable);

			// Get the values in HTML to return.  If we are in export mode, return just a string, otherwise, we need the
			// Type Icon, and ProductLine name hyperlinked to the ProductLine Tree.
			if (ProductLineCommon.isNotNull(strProductLineId)) {
	            if (reportFormat.equals("DEFAULT")) {
		  	        String strTypeIcon = getTypeIconProperty(context, strProductLineType);
		  	        if (ProductLineCommon.isNotNull(strTypeIcon)) {
		  	        	strTypeIcon = "../common/images/"+strTypeIcon.trim();
		  	        }
		  	
		  	    	// The start to the HTML is always the same, for both the Type Icon 
		  	    	// and the hyperlinked Name and Revision.
		  	    	StringBuffer sbHead = new StringBuffer();
		  	    	StringBuffer sbHref = new StringBuffer();
		  	    	       
		  	    	sbHead.append("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert");
		  	    	sbHead.append("&amp;objectId=");
		  	    	sbHead.append(XSSUtil.encodeForHTMLAttribute(context,strProductLineId));
		  	    	sbHead.append("'");
		  	    	sbHead.append(", '800', '700', 'true', 'popup')\">");
		  		    	       
  	    	        // Part 1: Type Icon Link first
  	    	        sbHref.append(sbHead);
  	                sbHref.append("<img src=\"");
  	                sbHref.append(strTypeIcon);
  	                sbHref.append("\" border=\"0\" /></a> ");
		  		               
  	                // Part 2: Name
  	      	        sbHref.append(sbHead);
  	                sbHref.append(XSSUtil.encodeForHTML(context, strProductLineName));
  	                sbHref.append("</a>");
		  	               
  	                // Part 3: Add finished product to outgoing variable.
    	            strProductLine = sbHref.toString();
	
		        } else {
	
		      	    // Part 1: Print Plain Text of the name and revision.
		      	    StringBuffer plainText = new StringBuffer();
		      	    plainText.append(XSSUtil.encodeForHTML(context, strNameSelectable));
		      	    strProductLine = plainText.toString();
                }
	       }

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return strProductLine;
	}
	
    /**
     *  This will populate the Model field in the type_Products form.
	 * 
	 * @param Context context
	 * @param String[] args
	 * @return String
	 * @throws Exception
	 */
	public String getProductFormModelField (Context context, String[] args) throws FrameworkException {
	    String strModel = "";
	    try {
	    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
	        HashMap paramMap = (HashMap) programMap.get("paramMap"); 

	        String objectId = (String) requestMap.get("objectId");
	        String reportFormat = (String)paramMap.get("reportFormat");
	        if (reportFormat == null || reportFormat.isEmpty()) {
	      	    reportFormat = "DEFAULT";
	        }
			
			// If this is the root Product we'll need to look up on Main Product, otherwise Products.
			String strLookupRel = null;
			if (DerivationUtil.isRootNode(context, objectId)) {
				strLookupRel = ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT;
			} else {
				strLookupRel = ProductLineConstants.RELATIONSHIP_PRODUCTS;
			}
			
			// Now find the Model
			String strRootSelectable = "to[" + strLookupRel + "].from.";
			String strNameSelectable = strRootSelectable + "name";
			String strIdSelectable = strRootSelectable + "id";
			String strTypeSelectable = strRootSelectable + "type";
			
			StringList slObjectSelects = new StringList(2);
			slObjectSelects.add(strNameSelectable);
			slObjectSelects.add(strTypeSelectable);
			slObjectSelects.add(strIdSelectable);
			
			DomainObject doProductObject = newInstance(context, objectId);
			Map mObjectMap = doProductObject.getInfo(context, slObjectSelects);
			
			String strModelName = (String)mObjectMap.get(strNameSelectable);
			String strModelType = (String)mObjectMap.get(strTypeSelectable);
			String strModelId = (String)mObjectMap.get(strIdSelectable);

			// Get the values in HTML to return.  If we are in export mode, return just a string, otherwise, we need the
			// Type Icon, and Model name hyperlinked to the Model Tree.
			if (ProductLineCommon.isNotNull(strModelId)) {
	            if (reportFormat.equals("DEFAULT")) {
		  	        String strTypeIcon = getTypeIconProperty(context, strModelType);
		  	        if (ProductLineCommon.isNotNull(strTypeIcon)) {
		  	        	strTypeIcon = "../common/images/"+strTypeIcon.trim();
		  	        }
		  	
		  	    	// The start to the HTML is always the same, for both the Type Icon 
		  	    	// and the hyperlinked Name and Revision.
		  	    	StringBuffer sbHead = new StringBuffer();
		  	    	StringBuffer sbHref = new StringBuffer();
		  	    	       
		  	    	sbHead.append("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert");
		  	    	sbHead.append("&amp;objectId=");
		  	    	sbHead.append(XSSUtil.encodeForHTMLAttribute(context,strModelId));
		  	    	sbHead.append("'");
		  	    	sbHead.append(", '800', '700', 'true', 'popup')\">");
		  		    	       
  	    	        // Part 1: Type Icon Link first
  	    	        sbHref.append(sbHead);
  	                sbHref.append("<img src=\"");
  	                sbHref.append(strTypeIcon);
  	                sbHref.append("\" border=\"0\" /></a> ");
		  		               
  	                // Part 2: Name
  	      	        sbHref.append(sbHead);
  	                sbHref.append(XSSUtil.encodeForHTML(context, strModelName));
  	                sbHref.append("</a>");
		  	               
  	                // Part 3: Add finished product to outgoing variable.
    	            strModel = sbHref.toString();
	
		        } else {
	
		      	    // Part 1: Print Plain Text of the name and revision.
		      	    StringBuffer plainText = new StringBuffer();
		      	    plainText.append(XSSUtil.encodeForHTML(context, strNameSelectable));
		      	    strModel = plainText.toString();
                }
	       }

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return strModel;
	}

    /**
     *  This will populate the Model Prefix field in the type_Products form.
	 * 
	 * @param Context context
	 * @param String[] args
	 * @return String
	 * @throws Exception
	 */

    public String getProductFormModelPrefixField (Context context, String[] args) throws FrameworkException {
	    String strModelPrefix = "";
	    try {
	    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String objectId = (String) requestMap.get("objectId");
			
			// If this is the root Product we'll need to look up on Main Product, otherwise Products.
			String strLookupRel = null;
			if (DerivationUtil.isRootNode(context, objectId)) {
				strLookupRel = ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT;
			} else {
				strLookupRel = ProductLineConstants.RELATIONSHIP_PRODUCTS;
			}
			
			// Now find the ProductLine
			String strNameSelectable = "to[" + strLookupRel + "].from.attribute[" + ProductLineConstants.ATTRIBUTE_PREFIX + "]";
			DomainObject doProductObject = newInstance(context, objectId);
			strModelPrefix = doProductObject.getInfo(context, strNameSelectable);
			if (strModelPrefix == null || strModelPrefix.equals("null")) {
				strModelPrefix = "";
			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return strModelPrefix;
	}
	   
   // --------------------------------------------------------------------------------------
   // End of Derivations JPO methods
   // --------------------------------------------------------------------------------------

    /**
     * Updates the composition binary when a Product Build relationship
     * is created or deleted.  This overrides the generic Composition Binary trigger in order to check 
     * if UNT is installed. 
     * 
     * @param context the ENOVIA <code>Context</code> object
     * @param args String array of arguments in the following order      
     *   			[0] = relationship id
     * 				[1] = relationship type
     * 				[2] = from object id
     * 				[3] = to object id 
     * @return int 0 for success and 1 for failure
     * @throws Exception throws exception if the operation fails
     */
    public int updateCompositionBinary(Context context, String[]args) throws Exception
    {
        int returnStatus = 0;

        boolean isUNTInstalled = FrameworkUtil.isSuiteRegistered(context,"appInstallTypeUnitTracking",false,null,null); 
        if (isUNTInstalled)
        {
        	String strFromObjId = args[2];
        	boolean strFromObjType = false;
        	if(ProductLineCommon.isNotNull(strFromObjId)){
        	DomainObject domainObj = DomainObject.newInstance(context, strFromObjId);
        	strFromObjType = domainObj.isKindOf(context, ProductLineConstants.TYPE_PRODUCT_VARIANT);
        	}
        	if(!strFromObjType){
        	returnStatus = JPO.invoke(context, "emxCompositionBinary", args, "updateCompositionBinary", args);
        	}
        }
     	
    	return returnStatus;    
    }

    /**
     * Updates the composition binary when Feature Allocation Type attribute on Product Feature List relationship
     * is modified.  
     * 
     * @param context the ENOVIA <code>Context</code> object
     * @param args String array of arguments in the following order      
     *   			[0] = relationship id
     * 				[1] = relationship type
     * 				[2] = from object id
     * 				[3] = to object id 
     * @return int 0 for success and 1 for failure
     * @throws Exception throws exception if the operation fails
     */
    public int updateCompositionBinaryOnModifyAttribute(Context context, String[]args) throws Exception
    {
        int returnStatus = 0;

        //need to modify the input args as this is coming from a Product Feature List relationship
        String relId = args[0];
        String relType = args[1];
        String fromObjId = args[2];
        String toRelId = args[3];
        
        String[] relIds = new String[]{toRelId};
        StringList relSelects = new StringList();
        relSelects.addElement("type");
        relSelects.addElement("torel.id");
        relSelects.addElement("from.id");
        relSelects.addElement("to.id");
        
        //get Logical Feature connection info
        MapList relList = DomainRelationship.getInfo(context, relIds, relSelects);
        Map relData = (Map)relList.get(0);
        args[0] = toRelId; //Logical Features rel id
        args[1] = (String)relData.get("type"); //Logical Features rel type
        args[2] = (String)relData.get("from.id");  //from object id
        args[3] = (String)relData.get("to.id");  //to object id        
        returnStatus = JPO.invoke(context, "emxCompositionBinary", args, "updateCompositionBinary", args);       
     	
    	return returnStatus;    
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
   	 
   	 String strProductId = (String) arguMap.get("objectId");
   	 
   	 DomainObject domProduct = new DomainObject(strProductId);
   	
   	 StringList SELECT_MILESTONE_TRACK = new StringList() ;
   	 
   	 StringList objectSelects = new StringList(2);
   	 objectSelects.add(SELECT_ID);
   	 objectSelects.add(SELECT_NAME);
   	objectSelects.add(ProductLineConstants.SELECT_ATTRIBUTE_MILESTONE_DISCIPLINE);
   	 StringList relationshipSelects = new StringList();
   	 
   	 
   	 MapList mlMilestoneTrack = domProduct.getRelatedObjects(context,
   			ProductLineConstants.RELATIONSHIP_CONFIGURATION_CRITERIA,
   			 DomainConstants.QUERY_WILDCARD,
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
   		 
   		 String busWhereClause = "";
   		 //Project Milestones connected with Product Level Milestones Track object
   		 MapList mlResult = domMilestoneTrack.getRelatedObjects(context,
   				ProductLineConstants.RELATIONSHIP_CONFIGURATION_ITEM,
   				 DomainConstants.QUERY_WILDCARD,
   				 objectSelects1,
   				 relationshipSelects1,
   				 false,	//to relationship - chagned
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
   			 mapMT.put("disableSelection", "true"); 
   			 mlMilestones.add(mapMilestone);
   		 }
   		
   	 }
   	 }catch(Exception ex){
   		 ex.printStackTrace();
   	 }
   	 return mlMilestones;

        
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
    public MapList getMilestoneTracks(Context context, String[] args) throws Exception {

    	MapList mlProductMilestoneTrack = new MapList();
    	try{

    		HashMap arguMap = (HashMap)JPO.unpackArgs(args);

    		String strProductId = (String) arguMap.get("objectId");

    		String strMode = (String) arguMap.get("mode");

    		DomainObject domProduct = new DomainObject(strProductId);

    		StringList SELECT_MILESTONE_TRACK = new StringList() ;
    		final String ATTRIBUTE_MILESTONE_DISCIPLINE = PropertyUtil.getSchemaProperty("attribute_MilestoneDiscipline");
    		final String SELECT_ATTRIBUTE_MILESTONE_DISCIPLINE = "attribute["+ATTRIBUTE_MILESTONE_DISCIPLINE+"]";

    		StringList objectSelects = new StringList(2);
    		objectSelects.add(SELECT_ID);
    		objectSelects.add(SELECT_NAME);
    		objectSelects.add(SELECT_CURRENT);
    		objectSelects.add(SELECT_ATTRIBUTE_MILESTONE_DISCIPLINE);
    		StringList relationshipSelects = new StringList();


    		mlProductMilestoneTrack = domProduct.getRelatedObjects(context,
    				ProductLineConstants.RELATIONSHIP_CONFIGURATION_CRITERIA,
    				DomainConstants.QUERY_WILDCARD,
    				objectSelects,
    				relationshipSelects,
    				false,	//to relationship
    				true,	//from relationship
    				(short)1,
    				DomainConstants.EMPTY_STRING,
    				DomainConstants.EMPTY_STRING,
    				0); 

    		//if the Milestone tracks are published to Model level, the discipline will be disabled for publishing.
    		String SELECT_MAIN_PRODUCT_FROM_ID =  "to[" + ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT + "].from.id";
    		String strModelId = "";
    		strModelId= domProduct.getInfo(context, SELECT_MAIN_PRODUCT_FROM_ID);
    		if(null == strModelId ||  "".equals(strModelId)){
    			String SELECT_PRODUCTS_FROM_ID =  "to[" + ProductLineConstants.RELATIONSHIP_PRODUCTS+ "].from.id"; 
    			strModelId= domProduct.getInfo(context, SELECT_PRODUCTS_FROM_ID);
    		}
    		DomainObject domModelId = new DomainObject(strModelId);

    		MapList mlModelMT = domModelId.getRelatedObjects(context,
    				ProductLineConstants.RELATIONSHIP_CONFIGURATION_CRITERIA,
    				DomainConstants.QUERY_WILDCARD,
    				objectSelects,
    				relationshipSelects,
    				false,	//to relationship
    				true,	//from relationship
    				(short)1,
    				DomainConstants.EMPTY_STRING,
    				DomainConstants.EMPTY_STRING,
    				0); 

    		for(int p=0;p<mlProductMilestoneTrack.size();p++){
    			Map mapProductMT = (Map)mlProductMilestoneTrack.get(p);
    			String strProductMT = (String)mapProductMT.get(SELECT_ID);
    			
    			for(int m=0;m<mlModelMT.size();m++){
    				Map mapModelMT = (Map)mlModelMT.get(m);
    				String strModelMT = (String)mapModelMT.get(SELECT_ID);
    				DomainObject domModelMT = new DomainObject(strModelMT);
    				MapList mlMT = domModelMT.getRelatedObjects(context,
    						ProductLineConstants.RELATIONSHIP_TRACK_TRACEABILITY,
    						ProductLineConstants.TYPE_MILESTONE_TRACK,
    						objectSelects,
    						relationshipSelects,
    						true,	//to relationship
    						false,	//from relationship
    						(short)1,
    						DomainConstants.EMPTY_STRING,
    						DomainConstants.EMPTY_STRING,
    						0); 
    				for(int s=0;s<mlMT.size();s++){
    					Map mapMT = (Map)mlMT.get(s);
    					String strMT = (String)mapMT.get(SELECT_ID);
    					if(strMT.equals(strProductMT)){
    						mapProductMT.put("disableSelection", "true"); 
    						break;
    					}
    				}
    			}
    		}

    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return mlProductMilestoneTrack;
    }
    public static void setCandidateFeatures(Context context, String args[]) throws FrameworkException {
   	 try {
   		 boolean isConfigurationInstall = FrameworkUtil.isSuiteRegistered(context, "appVersionVariantConfiguration", false, null, null);
   		 if (isConfigurationInstall) {
   			 String prodId = args[0];
   			 DomainObject prodDomainObj = new DomainObject(prodId);

   			 final String RELATIONSHIP_COMMITTED_CONTEXT = PropertyUtil.getSchemaProperty("relationship_CommittedContext");
   			 if( ! ProductLineUtil.isCommittedContextOnProduct(context, prodDomainObj, RELATIONSHIP_COMMITTED_CONTEXT) )
   				 return;

   			 String modelId = ProductLineUtil.getModelIdFromProduct(context, prodDomainObj);

   			 setCandidateFeaturesOnModelForProduct(context, modelId, prodId, RELATIONSHIP_COMMITTED_CONTEXT);
   		 }		
   	 } catch (Exception ex) {
   		 throw new FrameworkException(ex);
   	 }		
    }

    private static void setCandidateFeaturesOnModelForProduct(Context context, String modelId, String prodId, final String RELATIONSHIP_COMMITTED_CONTEXT) 
    throws Exception {
   	 String committedContextSelectable = "frommid["+ RELATIONSHIP_COMMITTED_CONTEXT+ "].id";
   	 String relatedProductSelectable = "frommid["+ RELATIONSHIP_COMMITTED_CONTEXT+ "].to.id";

   	 final String RELATIONSHIP_COMMITTED_CONFIGURATION_FEATURES = PropertyUtil.getSchemaProperty("relationship_CommittedConfigurationFeatures");
   	 final String RELATIONSHIP_COMMITTED_LOGICAL_FEATURES       = PropertyUtil.getSchemaProperty("relationship_CommittedLogicalFeatures");
   	 final String RELATIONSHIP_COMMITTED_MANUFACTURING_FEATURES = PropertyUtil.getSchemaProperty("relationship_CommittedManufacturingFeatures");

   	 String relPattern = RELATIONSHIP_COMMITTED_CONFIGURATION_FEATURES + "," +
   	 RELATIONSHIP_COMMITTED_LOGICAL_FEATURES + "," + 
   	 RELATIONSHIP_COMMITTED_MANUFACTURING_FEATURES;

   	 StringList objSelectables = new StringList();
   	 StringList relSelectables = new StringList();
   	 relSelectables.add(ProductLineConstants.SELECT_RELATIONSHIP_ID);
   	 relSelectables.add(ProductLineConstants.SELECT_RELATIONSHIP_TYPE);
   	 relSelectables.add(committedContextSelectable);
   	 relSelectables.add(relatedProductSelectable);		

   	 MapList commitedFeaturesMapList = new DomainObject(modelId).getRelatedObjects(context, relPattern, DomainObject.QUERY_WILDCARD, 
   			 objSelectables, relSelectables, false, true, (short) 1, null, null, 0);

   	 for (Object committedFeatureObj : commitedFeaturesMapList) {
   		 Map committedFeatureMap = (Map)committedFeatureObj;
   		 String committedFeatureRelId = (String)committedFeatureMap.get(ProductLineConstants.SELECT_RELATIONSHIP_ID);
   		 if(UIUtil.isNotNullAndNotEmpty(committedFeatureRelId))
   		 {
   			 Object committedContextObj = committedFeatureMap.get(committedContextSelectable);
   			 //if it's the only committed context
   			 if(committedContextObj instanceof String) 
   			 {
   				 //if it's for the current product
   				 String relatedProductId = (String)committedFeatureMap.get(relatedProductSelectable);
   				 if(prodId.equals(relatedProductId))
   				 {
   					 String committedFeatureRelType = (String)committedFeatureMap.get(ProductLineConstants.SELECT_RELATIONSHIP_TYPE);

   					 if(ProductLineCommon.isKindOfRel(context, committedFeatureRelType, RELATIONSHIP_COMMITTED_CONFIGURATION_FEATURES))
   					 {
   						 final String RELATIONSHIP_CANDIDTAE_CONFIGURATION_FEATURES = PropertyUtil.getSchemaProperty("relationship_CandidateConfigurationFeatures");
   						 DomainRelationship.setType(context, committedFeatureRelId, RELATIONSHIP_CANDIDTAE_CONFIGURATION_FEATURES);
   					 }
   					 if(ProductLineCommon.isKindOfRel(context, committedFeatureRelType, RELATIONSHIP_COMMITTED_LOGICAL_FEATURES))
   					 {
   						 final String RELATIONSHIP_CANDIDTAE_LOGICAL_FEATURES = PropertyUtil.getSchemaProperty("relationship_CandidateLogicalFeatures");
   						 DomainRelationship.setType(context, committedFeatureRelId, RELATIONSHIP_CANDIDTAE_LOGICAL_FEATURES);
   					 }
   					 if(ProductLineCommon.isKindOfRel(context, committedFeatureRelType, RELATIONSHIP_COMMITTED_MANUFACTURING_FEATURES))
   					 {
   						 final String RELATIONSHIP_CANDIDTAE_MANUFACTURING_FEATURES = PropertyUtil.getSchemaProperty("relationship_CandidateManufacturingFeatures");
   						 DomainRelationship.setType(context, committedFeatureRelId, RELATIONSHIP_CANDIDTAE_MANUFACTURING_FEATURES);
   					 }
   				 }
   			 }
   		 }
   	 }			
    }
    
    /**
     *This method is used to obtain the object ID of the newly created cloned Product and call createModelOnCreate()
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds Type, Name, Revision and Vault of the newly created cloned Product.
     * @throws Exception if the operation fails
     * @since R217
     */
     public void createModelOnClone(Context context,String args[]) throws Exception
     {
    	try{
    		String cloneContext = PropertyUtil.getGlobalRPEValue(context,"FromCloneWithReplicateContext");
    		if(!cloneContext.equals("") && cloneContext!=null && cloneContext.equalsIgnoreCase("TRUE")){
		    		String objectType = args[0];
		    		String objectNewName = args[1];
		    		String objectRev = args[2];
		    		String objectVault = args[3];
		    		StringList selectId = new StringList(DomainObject.SELECT_ID);
		    		String strTypeModel = "type_Model";
		    		int i;
		    		MapList mlNewObjectId = new DomainObject().findObjects(context, objectType, objectNewName, objectRev, "*", objectVault, "", false, selectId);
		    		    	
		    		String [] strNewObjectId = new String[mlNewObjectId.size()+1];
		    		  Map mapObject = new HashMap();
		
		            for (i=0; i < mlNewObjectId.size(); i++){
		            		 mapObject = (Map)mlNewObjectId.get(i);
		                     strNewObjectId[i] = (String)(mapObject.get("id"));
		            }
		            strNewObjectId[i] = strTypeModel;
		    		createModelOnCreate(context, strNewObjectId);
    		}
    	}catch(Exception ex){
    		throw new FrameworkException(ex.getMessage());
    	}
     }
  
  @com.matrixone.apps.framework.ui.PostProcessCallable
  public Map createModelAndConnectToProductLine(Context context, String[] args) throws Exception {
      Map returnMap = new HashMap<String, String>(1);
      try {
            Map<?, ?> programMap = (Map<?, ?>) JPO.unpackArgs(args);
    			
            Map paramMap = (Map) programMap.get("paramMap");
            String strProductId = (String) paramMap.get("newObjectId");       
    		Map requestMap = (Map) programMap.get("requestMap");
            String parentOID = (String) requestMap.get("parentOID");
            String objectId = (String) requestMap.get("objectId");

            StringList newSelectables = new StringList();
            StringBuilder typeKind = new StringBuilder(50);
            typeKind.append("type.kindof[");
            typeKind.append(ProductLineConstants.TYPE_PRODUCT_LINE);
            typeKind.append("]");
            newSelectables.add(typeKind.toString());

			StringBuilder modelSelect = new StringBuilder(50);
			modelSelect.append("to[");
			modelSelect.append(ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT);
			modelSelect.append("].from.id");
			newSelectables.add(modelSelect.toString());
			if(objectId == null && objectId.isEmpty()){
                objectId = parentOID;
            }
            MapList resultMaps = DomainObject.getInfo(context, new String[]{objectId,strProductId}, newSelectables);
            final String isPL = (String)((Map)resultMaps.get(0)).get((String)typeKind.toString());
            
             if(Boolean.valueOf(isPL)) {
    			String modelId = (String)((Map)resultMaps.get(1)).get((String)modelSelect.toString());
                DomainRelationship.connect(context, objectId, ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS, modelId,false);
    			
    			Map mArgs = new HashMap();
    			Map pMap = new HashMap();
    			pMap.put("objectId",strProductId);
    			mArgs.put("paramMap",pMap);
    			String[] newArgs = JPO.packArgs(mArgs);
    			copyProductAttributesToModel(context, newArgs);
             }
		}
		catch (Exception e) {
		e.printStackTrace();
	  }
      return returnMap;
   }

    @com.matrixone.apps.productline.PLCExecuteCallable
    public String getProductCreateLink(Context context, String[] args) throws Exception {
        try {
            Map<?, ?> programMap = (HashMap<?, ?>) JPO.unpackArgs(args);
            String[] emxTableRowId = (String[]) programMap.get("emxTableRowId");
            String[] mode = (String[]) programMap.get("mode");
            StringList emxTableRowIds = FrameworkUtil.split(emxTableRowId[0], "|");
            String objectId = DomainConstants.EMPTY_STRING;
            switch (emxTableRowIds.size()) {
            case 3:
                objectId = (String) emxTableRowIds.get(0);
                break;

            case 4:
                objectId = (String) emxTableRowIds.get(1);
                break;
            }
            DomainObject parentObj = new DomainObject(objectId);
            StringList objectSelectList = new StringList();
            StringBuilder typeKind = new StringBuilder(50);
            typeKind.append("type.kindof[");
            typeKind.append(ProductLineConstants.TYPE_PRODUCT_LINE);
            typeKind.append("]");
            objectSelectList.addElement(typeKind.toString());
            Map parentInfoTable = parentObj.getInfo(context, objectSelectList);
            String type = (String) parentInfoTable.get(typeKind.toString());

            return encodeFunctionForJavaScript(context, false, "addProduct", type, objectId);
        }
        catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * Method to encode the arguments and form the function for calling in
     * JavaScript
     * 
     * @param parameter
     *            is the argument to be encoded
     * @return encoded argument
     */
    private String encodeFunctionForJavaScript(Context context, Boolean isMarkUp, String functionName, String... args) {
        StringBuilder jsFunCall = new StringBuilder(functionName);
        List<String> argList = new ArrayList<String>();
        // Prepare JSFunctionCall
        jsFunCall.append("(");

        for (int i = 0; i < args.length; i++) {
            StringBuilder tempArgs = new StringBuilder();
            tempArgs.append("\"");

            if (i == 0 && isMarkUp) {
                tempArgs.append(args[i]);
            }
            else {
                tempArgs.append(XSSUtil.encodeForJavaScript(context, args[i]));
            }
            tempArgs.append("\"");
            argList.add(tempArgs.toString());
        }
        jsFunCall.append(FrameworkUtil.join(argList.toArray(new String[0]), ","));
        jsFunCall.append(")");
        return jsFunCall.toString();
    }
}//end of class
