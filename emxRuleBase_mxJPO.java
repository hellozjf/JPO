/**
 * This JPO class has some method pertaining to Configurable Rules type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Person;
import matrix.db.Vault;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurableRulesUtil;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.Model;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.DerivationUtil;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineUtil;
public class emxRuleBase_mxJPO extends emxDomainObject_mxJPO {

    public static final String SYMB_SPACE = " ";

    public static final String STR_OBJECT_LIST = "objectList";

    public static final String RESOURCE_BUNDLE_PRODUCTS_STR = "emxProductLineStringResource";

    /** A string constant with the value field_display_choices. */
    protected static final String FIELD_DISPLAY_CHOICES = "field_display_choices";

    /** A string constant with the value field_choices. */
    protected static final String FIELD_CHOICES = "field_choices";

    /**
     * Alias used for Feature TNR Delimiter.
     */
    private final static String FEATURE_TNR_DELIMITER = "::";
    protected final static String SUITE_KEY = "Configuration";
    /**
     * Default Constructor.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public emxRuleBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * This method will inherit Mandatory Rules from Product Line to
     * SubProductLine upon creation of a SubProductLine.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the ${FROMOBJECTID} and ${TOOBJECTID}
     * @return void
     * @throws Exception
     *             if the operation fails
     * @since since Feature Management Module X3
     */
    public void inheritMandatoryRulesToSubProductLine(Context context,
            String args[]) throws Exception {
        try {
            String prodLineId = args[0];
            String subProdLineId = args[1];
            String relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_BooleanCompatibilityRule");
            processMandatoryRulesOnChildObjCreate(context, prodLineId,
                    subProdLineId, relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_RuleExtension");
            processMandatoryRulesOnChildObjCreate(context, prodLineId,
                    subProdLineId, relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_ResourceLimit");
            processMandatoryRulesOnChildObjCreate(context, prodLineId,
                    subProdLineId, relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_MarketingPreference");
            processMandatoryRulesOnChildObjCreate(context, prodLineId,
                    subProdLineId, relName);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * This method will inherit Mandatory Rules from Product Line to Model upon
     * creation of a Model.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the ${FROMOBJECTID} and ${TOOBJECTID}
     * @return void
     * @throws Exception
     *             if the operation fails
     * @since since Feature Management Module X3
     */
    public void inheritMandatoryRulesToModel(Context context, String args[])
            throws Exception {
        try {
            String prodLineId = args[0];
            String modelId = args[1];
            String relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_BooleanCompatibilityRule");
            processMandatoryRulesOnChildObjCreate(context, prodLineId, modelId,
                    relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_RuleExtension");
            processMandatoryRulesOnChildObjCreate(context, prodLineId, modelId,
                    relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_ResourceLimit");
            processMandatoryRulesOnChildObjCreate(context, prodLineId, modelId,
                    relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_MarketingPreference");
            processMandatoryRulesOnChildObjCreate(context, prodLineId, modelId,
                    relName);
            // Code to call the inheritance logic for Product to inherit Rules.
            // IR-021028 Fix start
            //below has been modified in r211_hf4 (to account product derivations)
           String [] strPrdMdl = new String[2];
           strPrdMdl[0] = modelId;           
            //if(Product.isDerivationEnabled(context)){
            	 // Get the Root of the Derivation chain             
                String strRootId = new Model().getDerivationRoot(context, modelId);
                if(ProductLineCommon.isNotNull(strRootId)){
                	strPrdMdl[1] = strRootId;
                	inheritMandatoryRulesToProduct(context,strPrdMdl);
                	MapList MapProductlist = DerivationUtil.getAllDerivations(context, strRootId,(short)0,(short)0); 
                	if(MapProductlist!=null && MapProductlist.size()>0 ){
                		for(int i=0;i<MapProductlist.size();i++){                 		
                			strPrdMdl[1] =  (String) ((Map)MapProductlist.get(i)).get(DomainConstants.SELECT_ID);
                			inheritMandatoryRulesToProduct(context,strPrdMdl);
                		}
                	}
                }
        	/*}else{
        		 StringList strProduct =  domModel.getAllProductRevisionUnderModel(context);
        		 if(strProduct!=null && strProduct.size()>0 ){
                 	for(int i=0;i<strProduct.size();i++){
                 		strPrdMdl[1] =  (String) strProduct.get(i);
                 		inheritMandatoryRulesToProduct(context,strPrdMdl);
                 	}
                 }
        	}*/
            
            
            
            // IR-021028 Fix ends.            
            
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * This method will inherit Mandatory Rules from Model to Product to upon
     * creation of a Product.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the ${FROMOBJECTID} and ${TOOBJECTID}
     * @return void
     * @throws Exception
     *             if the operation fails
     * @since since Feature Management Module X3
     */
    public void inheritMandatoryRulesToProduct(Context context, String args[])
            throws Exception {
        try {
            String modelId = args[0];
            String productId = args[1];
            DomainObject objProd = new DomainObject(productId);
            StringList slProdVar = objProd.getInfoList(context, "from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"].to.id");
            String relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_BooleanCompatibilityRule");
            processMandatoryRulesOnChildObjCreate(context, modelId, productId,
                    relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_RuleExtension");
            processMandatoryRulesOnChildObjCreate(context, modelId, productId,
                    relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_ResourceLimit");
            processMandatoryRulesOnChildObjCreate(context, modelId, productId,
                    relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_MarketingPreference");
            processMandatoryRulesOnChildObjCreate(context, modelId, productId,
                    relName);
            ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
            for(int i=0; i<slProdVar.size();i++)
            {
            	String strVarID = (String)slProdVar.get(i);
            	inheritMandatoryRulesToProductVariant(context,new String[]{productId,strVarID});
            }
            ContextUtil.popContext(context);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * This method will inherit Mandatory Rules from Product to Product Variant
     * upon creation of a Product.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the ${FROMOBJECTID} and ${TOOBJECTID}
     * @return void
     * @throws Exception
     *             if the operation fails
     * @since since Feature Management Module X3
     */
    public void inheritMandatoryRulesToProductVariantBackgroundProcess(Context context,
            String args[]) throws Exception {
        try {


            String productId = args[0];
            String verId = args[1];

            String relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_BooleanCompatibilityRule");



            processMandatoryRulesOnChildObjCreate(context, productId, verId,
                    relName);
            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_RuleExtension");

            processMandatoryRulesOnChildObjCreate(context, productId, verId,
                    relName);


            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_ResourceLimit");

            processMandatoryRulesOnChildObjCreate(context, productId, verId,
                    relName);


            relName = PropertyUtil
                    .getSchemaProperty(context,"relationship_MarketingPreference");

            processMandatoryRulesOnChildObjCreate(context, productId, verId,
                    relName);


        } catch (Exception e) {

            throw new FrameworkException(e);
        }

    }



    // This Method is the backgroung job method this will initiate the background and  will call actuall method to start background job
    // of copying the Mandatory rules to the Product Variant.

    public void inheritMandatoryRulesToProductVariant(Context context,
            String args[]) throws Exception {

        try {
			ContextUtil.startTransaction(context, true);

            String productId = args[0];
            
            String[] arrJPOArguments = new String[2];
            arrJPOArguments[0] = args[0];
            arrJPOArguments[1] = args[1];

            Job job = new Job("emxRule", "inheritMandatoryRulesToProductVariantBackgroundProcess", arrJPOArguments);
            job.setContextObject(productId);
			job.setTitle("Rule Inheritance");
			job.setDescription("Inherit Mandatory Rules to Product Variant");
            job.createAndSubmit(context);

			ContextUtil.commitTransaction(context);
        } catch (Exception e) {
			ContextUtil.abortTransaction(context);
            e.printStackTrace();
            throw new FrameworkException(e);
        }

    }





    /**
     * This method will inherit all the Mandatory Rules that are connected to
     * Parent Object to its immediate Child Object.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the ${FROMOBJECTID} and ${TOOBJECTID}
     * @return void
     * @throws Exception
     *             if the operation fails
     * @since since Feature Management Module X3
     */
    private void processMandatoryRulesOnChildObjCreate(Context context,
            String fromId, String toId, String relName) throws Exception {

        try {
            DomainObject parentObj = new DomainObject(fromId);
            DomainObject childObj = new DomainObject(toId);
            String childRuleObject  = "";
            ConfigurationUtil configChildObject = new ConfigurationUtil(toId);
            String strChildType = childObj.getInfo(context,
                    DomainConstants.SELECT_TYPE);

            List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
            List lstRelationshipSelects = new StringList(
                    DomainConstants.SELECT_RELATIONSHIP_ID);

            // get all the Feature List objects under the parent
            // Here relName can be Candidate Item or Feature List From
            MapList objRuleList = parentObj.getRelatedObjects(context, relName,
                    DomainConstants.QUERY_WILDCARD,
                    (StringList) lstObjectSelects,
                    (StringList) lstRelationshipSelects, false, true,
                    (short) 1, DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING, 0);

            // Returns a list of Mandatory Rules
            MapList mandRules = getOnlyMandatoryRules(context, objRuleList);
            Map rulesMap = new HashMap();
            int rulesDO = mandRules.size();
            
            MapList mapRelIds = configChildObject.getObjectStructure(context,
							            		DomainConstants.QUERY_WILDCARD,	 // Type Pattern
							            		relName,  				 // Relationship patern	
							            		(StringList)lstObjectSelects,  	 	// Object selectables
							            		(StringList)lstRelationshipSelects,    // Relationship selectable
												false,					         // get To
												true, 		                     // get From
												1,			                     // level
												0,		  	                     // limit
												null,                            // where object clause
												null, 	                         // where relationship clause  
												(short)0,                        // filter flag
												null);                           // effectivity expression
            
            int mapRelIdSize = mapRelIds.size();
            Map childRulesMap = new HashMap();
            DomainRelationship domRel = new DomainRelationship();
            ArrayList<String> childRuleIdList = new ArrayList<String>(mapRelIdSize); 
            
            for(int iCnt = 0; iCnt<mapRelIdSize ; iCnt++)
        	{
        		childRulesMap = (Map)mapRelIds.get(iCnt);
        		childRuleObject = (String) childRulesMap.get(SELECT_ID);
        		childRuleIdList.add(childRuleObject);
        	}
            
        
            for (int i = 0; i < rulesDO; i++) {
                rulesMap = (Map) mandRules.get(i);
                String domId = (String) rulesMap.get(SELECT_ID);
                DomainObject objDom = new DomainObject(domId);
                if(mapRelIdSize>0)
                {
                		if(childRuleIdList.contains(domId))                		
                		{
                	for(int iCnt = 0; iCnt<mapRelIdSize ; iCnt++)
                	{
                		childRulesMap = (Map)mapRelIds.get(iCnt);
                		childRuleObject = (String) childRulesMap.get(SELECT_ID);
                	        	if(domId.equals(childRuleObject))
                		{
                			String relID = (String) childRulesMap.get(DomainRelationship.SELECT_ID);
                			domRel=new DomainRelationship(relID);
                    				
                    				break;
                	        	}
                	        }
                			
                			
                		}
                		else
                        {
                        	domRel = DomainRelationship.connect(context,
                        			childObj, relName, objDom);
                        }
                	
                }
                else
                {
                	domRel = DomainRelationship.connect(context,
                			childObj, relName, objDom);
                }
                
                /*DomainRelationship domRel = DomainRelationship.connect(context,
                        childObj, relName, objDom);*/

                domRel.openRelationship(context);
                if (!strChildType
                        .equals(ConfigurationConstants.TYPE_PRODUCT_VARIANT)) {
                    domRel.setAttributeValue(context,
                            ConfigurationConstants.ATTRIBUTE_MANDATORYRULE,
                            "Yes");
                }
                //always set Inherited attribute
                domRel.setAttributeValue(context,
                		ConfigurationConstants.ATTRIBUTE_INHERITED, "True");

                domRel.closeRelationship(context, true);

                // added for CR 138-2 -- starts
                String strParentRelId = (String) rulesMap
                        .get(DomainConstants.SELECT_RELATIONSHIP_ID);
                StringList s2RelSelects = new StringList(1);
                s2RelSelects.addElement(DomainRelationship.SELECT_ID);
                Hashtable currentRelData = domRel.getRelationshipData(context,
                        s2RelSelects);
                StringList s2RelIds = (StringList) currentRelData
                        .get(DomainRelationship.SELECT_ID);
                String currentRelId = (String) s2RelIds.get(0);
                String relInheritedFrom = (String) PropertyUtil
                        .getSchemaProperty(context,"relationship_InheritedFrom");
               
                String strMqlCmd1 = "print connection $1 select $2 dump $3 ";
                
                StringBuffer selectable = new StringBuffer();
                selectable.append("tomid[")
                		.append(relInheritedFrom).append("].id");
                
                String tomidId = MqlUtil.mqlCommand(context, strMqlCmd1, true,currentRelId,selectable.toString(),ConfigurationConstants.DELIMITER_PIPE);
                
                if ((null == tomidId || "null".equals(tomidId)|| "".equals(tomidId))) 
                {
                	String strMqlCmd2 = "add connection $1 fromrel $2 torel $3";
                	
                	MqlUtil.mqlCommand(context, strMqlCmd2, true,relInheritedFrom,strParentRelId,currentRelId);
                }              
                // added for CR 138-2 -- starts
            }
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * This method will get only the Mandatory Rules from a Maplist of Rule
     * objects.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param objRulesList
     *            holds a MapList of Rule objects
     * @return MapList
     * @throws Exception
     *             if the operation fails
     * @since since Feature Management Module X3
     */
    private MapList getOnlyMandatoryRules(Context context, MapList objRulesList)
            throws Exception {
        try {
            MapList mandRules = new MapList();
            Map rMap = new HashMap();
            int ruleObj = objRulesList.size();
            for (int i = 0; i < ruleObj; i++) {
                rMap = (Map) objRulesList.get(i);
                String relId = (String) rMap.get(SELECT_RELATIONSHIP_ID);
                DomainRelationship objDom = new DomainRelationship(relId);
                String mRule = objDom.getAttributeValue(context,
                        ConfigurationConstants.ATTRIBUTE_MANDATORYRULE);
                if (mRule.equals("Yes")) {
                    mandRules.add(rMap);
                }
            }
            return mandRules;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    public void inheritMandatoryRulesOnModifyAttribute(Context context,
            String[] args) throws Exception {
        String strRlId = args[0];
        DomainRelationship domRel = new DomainRelationship(strRlId);

        String strAttrVal = domRel.getAttributeValue(context,ConfigurationConstants.ATTRIBUTE_MANDATORYRULE);

        // Added By PVoggu
        String strCreate=PropertyUtil.getGlobalRPEValue(context,"RuleContext");
        boolean isCreate=false;


        if(strCreate !=null && !strCreate.equalsIgnoreCase("null")
                && !strCreate.equalsIgnoreCase("")){
	    	if(strCreate.equals("Create")){
                      isCreate=true;
            } else if(strCreate.equals("Edit")){
		      isCreate=false;
			}
        }

        // Added by PVoggu
		// If the Scenario is Create and the mandatory is set to No, then  there should be no action
        //Fix for Bug: 370240 -- Removed condition strAttrVal == "NO"....
        if (isCreate && (strAttrVal != null && !"".equals(strAttrVal))){
		   return;
		}
		
        // Added by PVoggu		
	   // when the the mandatory condition is changed from No to Yes	   
		if (!isCreate && (strAttrVal != null && !"".equals(strAttrVal)&& strAttrVal.equalsIgnoreCase("Yes"))){


            StringList slRelSelects = new StringList(5);
            slRelSelects.addElement(DomainRelationship.SELECT_FROM_ID);
            slRelSelects.addElement(DomainRelationship.SELECT_FROM_TYPE);
            slRelSelects.addElement(DomainRelationship.SELECT_TO_ID);
            slRelSelects.addElement(DomainRelationship.SELECT_TO_TYPE);
            slRelSelects.addElement(DomainRelationship.SELECT_TYPE);

            Hashtable htRelData = domRel.getRelationshipData(context,
                    slRelSelects);
            StringList slFromObjIds = (StringList) htRelData
                    .get(DomainRelationship.SELECT_FROM_ID);
            StringList slToObjIds = (StringList) htRelData
                    .get(DomainRelationship.SELECT_TO_ID);
            
            String strFromObjId = (String) slFromObjIds.get(0);
            String strRuleId = (String) slToObjIds.get(0);

			String[] arrJPOArgs = new String[3];
			arrJPOArgs[0] = strFromObjId;
			arrJPOArgs[1] = strRuleId;
			arrJPOArgs[2] = strRlId;

			// Get the Property setting to process the Mandatory Rule Inherirance to be background or not
			String strInheriteManBG = EnoviaResourceBundle.getProperty(context,"emxProduct.BackgroundJob.MandatoryRules.onEdit");
			
			// If the setting is Yes, the Background job will start to process the Inheritance
			if(strInheriteManBG.equalsIgnoreCase("Yes"))
			{
				try
				{
					ContextUtil.startTransaction(context, true);
					Job job = new Job("emxRule","ruleInheritanceOnEdit", arrJPOArgs);
                    job.setContextObject(strFromObjId);
					job.setTitle("Rule Inheritance");
					job.setDescription("Inherit Mandatory Rules when mandatory condition is changed from 'No' to 'Yes'");
					job.createAndSubmit(context);
					ContextUtil.commitTransaction(context);
				}catch(Exception e)
				{
					ContextUtil.abortTransaction(context);
					throw new FrameworkException(e);
				}
			}
			// If the setting is No, Inheritance will happen inline.
			else{
				ruleInheritanceOnEdit(context,arrJPOArgs);
			}

			return;
	}
       
	// When the Manatory condition if changed from Yes to No from the Edit Dialog
	 if (strAttrVal != null && !"".equals(strAttrVal) && strAttrVal.equalsIgnoreCase("No")) {
					
            StringList slRelSelects = new StringList(5);
            slRelSelects.addElement(DomainRelationship.SELECT_FROM_ID);
            slRelSelects.addElement(DomainRelationship.SELECT_FROM_TYPE);
            slRelSelects.addElement(DomainRelationship.SELECT_TO_ID);
            slRelSelects.addElement(DomainRelationship.SELECT_TO_TYPE);
            slRelSelects.addElement(DomainRelationship.SELECT_TYPE);

            Hashtable htRelData = domRel.getRelationshipData(context,
                    slRelSelects);
            StringList slFromObjIds = (StringList) htRelData
                    .get(DomainRelationship.SELECT_FROM_ID);
            StringList slFromObjTypes = (StringList) htRelData
                    .get(DomainRelationship.SELECT_FROM_TYPE);
            StringList slToObjIds = (StringList) htRelData
                    .get(DomainRelationship.SELECT_TO_ID);
            StringList slToObjTypes = (StringList) htRelData
                    .get(DomainRelationship.SELECT_TO_TYPE);
            StringList slRelTypes = (StringList) htRelData
                    .get(DomainRelationship.SELECT_TYPE);

            String strFromObjId = (String) slFromObjIds.get(0);
            String strFromObjType = (String) slFromObjTypes.get(0);
            String strRuleId = (String) slToObjIds.get(0);
            String strRuleType = (String) slToObjTypes.get(0);
            String strRelType = (String) slRelTypes.get(0);
            BusinessType bType = new BusinessType(strFromObjType, new Vault(""));
            StringList slListParents = bType.getParents(context);

            String strParentType = "";
            if (slListParents.size() != 0) {

                strParentType = (String) slListParents.get(0);
                bType = new BusinessType(strParentType, new Vault(""));
                slListParents = bType.getParents(context);
            } else {
                strParentType = strFromObjType;
            }
            String strChildType = "";
            String strChildRel = "";
            String strChildTypePl = "";
            String strChildRelPl = "";
            DomainObject domfromObj = new DomainObject(strFromObjId);
            if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_PRODUCT_LINE) ) {

                strChildType = ConfigurationConstants.TYPE_MODEL;
                strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS;
                strChildTypePl = ConfigurationConstants.TYPE_PRODUCT_LINE;
                strChildRelPl = ConfigurationConstants.RELATIONSHIP_SUB_PRODUCT_LINES;

            } else if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_MODEL) ) {

                strChildType = ConfigurationConstants.TYPE_PRODUCTS;
                strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCTS;

            } else if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_PRODUCTS)  ) {
                strChildType = ConfigurationConstants.TYPE_PRODUCTS;
                strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;
            }
            List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
            MapList childObjList = domfromObj.getRelatedObjects(context,
                    strChildRel, strChildType, (StringList) lstObjectSelects,
                    null, false, true, (short) 1, DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING, 0);
                    
	    StringList strObjectSelectablesPV = new StringList(5);
	    strObjectSelectablesPV.addElement(DomainObject.SELECT_ID);
                    
            if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_PRODUCT_LINE) ) {
                MapList childObjListPl = domfromObj.getRelatedObjects(context,
                        strChildRelPl, strChildTypePl,
                        (StringList) lstObjectSelects, null, false, true,
                        (short) 1, DomainConstants.EMPTY_STRING,
                        DomainConstants.EMPTY_STRING, 0);
                DomainObject domObj = new DomainObject();
                for (int iCnt = 0; iCnt < childObjListPl.size(); iCnt++) {

                    Hashtable htChildObjMap = (Hashtable) childObjListPl
                            .get(iCnt);
                    String strObjId = (String) htChildObjMap
                            .get(DomainConstants.SELECT_ID);

                    domObj.setId(strObjId);
                    List lstRelSelects = new StringList(
                            DomainRelationship.SELECT_ID);
                            

                    //String strWhere = "relationship[" + strRelType
                    //        + "].to.id==\"" + strRuleId + "\"";
                            
                    String strWhere = "id==\"" + strRuleId + "\"";
                            
                            
                    MapList relList = domObj.getRelatedObjects(context,
                            strRelType, strRuleType, strObjectSelectablesPV,
                            (StringList) lstRelSelects, false, true, (short) 1,
                            strWhere, DomainConstants.EMPTY_STRING, 0);
                    for (int count = 0; count < relList.size(); count++) {
                        Hashtable htTypeObjMap = (Hashtable) relList.get(count);
                        String strrelId = (String) htTypeObjMap
                                .get(DomainRelationship.SELECT_ID);

                        domRel = new DomainRelationship(strrelId);
                        domRel.openRelationship(context);

                        domRel.setAttributeValue(context,
                                ConfigurationConstants.ATTRIBUTE_INHERITED,
                                "False");
                        domRel.closeRelationship(context, true);
                        // added for CR 138-2 -- starts
                        String relInheritedFrom = (String) PropertyUtil
                                .getSchemaProperty(context,"relationship_InheritedFrom");
                        String strMqlCmd1 = "print connection $1 select $2 dump $3";
                        StringBuffer selectable = new StringBuffer();
                        selectable.append("tomid[")
                        		.append(relInheritedFrom).append("].id");
                        
                        String tomidId = MqlUtil.mqlCommand(context,
                                strMqlCmd1, true,strrelId,selectable.toString(),ConfigurationConstants.DELIMITER_PIPE);
                        String strMqlCmd2 = "disconnect connection $1";
                        MqlUtil.mqlCommand(context, strMqlCmd2, true,tomidId);
                        // added for 138-2 -- end
                    }
                }

            }
            try{
            DomainObject domObj = new DomainObject();
            for (int iCnt = 0; iCnt < childObjList.size(); iCnt++) {

                Hashtable htChildObjMap = (Hashtable) childObjList.get(iCnt);
                String strObjId = (String) htChildObjMap
                        .get(DomainConstants.SELECT_ID);

                domObj.setId(strObjId);
                List lstRelSelects = new StringList(
                        DomainRelationship.SELECT_ID);
                // String strWhere = "relationship[" + strRelType + "].to.id==\""
                //        + strRuleId + "\"";
                
				String strWhere = "id==\""+ strRuleId + "\"";                
                        
                MapList relList = domObj
                        .getRelatedObjects(context, strRelType, strRuleType,
                                strObjectSelectablesPV, (StringList) lstRelSelects, false, true,
                                (short) 1, strWhere,
                                DomainConstants.EMPTY_STRING, 0);
                for (int count = 0; count < relList.size(); count++) {
                    Hashtable htTypeObjMap = (Hashtable) relList.get(count);
                    String strrelId = (String) htTypeObjMap
                            .get(DomainRelationship.SELECT_ID);

                    domRel = new DomainRelationship(strrelId);
                    domRel.openRelationship(context);

                    domRel
                            .setAttributeValue(context,
                                    ConfigurationConstants.ATTRIBUTE_INHERITED,
                                    "False");
                    domRel.closeRelationship(context, true);
                    // added for CR 138-2 -- starts
                    String relInheritedFrom = (String) PropertyUtil
                            .getSchemaProperty(context,"relationship_InheritedFrom");
                    String strMqlCmd1 = "print connection $1 select $2 dump $3";
                    StringBuffer selectable3 = new StringBuffer();
                    selectable3.append("tomid[")
                    		.append(relInheritedFrom)
                    		.append("].id");                    
                    String tomidId = MqlUtil.mqlCommand(context, strMqlCmd1,
                            true,strrelId,selectable3.toString(),ConfigurationConstants.DELIMITER_PIPE);
                    String strMqlCmd2 = "disconnect connection $1";
                    MqlUtil.mqlCommand(context, strMqlCmd2, true,tomidId);
                    // added for CR 138-2 -- end
                }
            }
            }
            catch(Exception e){
	            e.printStackTrace();
            }

            return;
        }
        StringList slRelSelects = new StringList(5);
        slRelSelects.addElement(DomainRelationship.SELECT_FROM_ID);
        slRelSelects.addElement(DomainRelationship.SELECT_FROM_TYPE);
        slRelSelects.addElement(DomainRelationship.SELECT_TO_ID);
        slRelSelects.addElement(DomainRelationship.SELECT_TO_TYPE);
        slRelSelects.addElement(DomainRelationship.SELECT_TYPE);

        Hashtable htRelData = domRel.getRelationshipData(context, slRelSelects);

        StringList slFromObjIds = (StringList) htRelData
                .get(DomainRelationship.SELECT_FROM_ID);
        StringList slFromObjTypes = (StringList) htRelData
                .get(DomainRelationship.SELECT_FROM_TYPE);
        StringList slToObjIds = (StringList) htRelData
                .get(DomainRelationship.SELECT_TO_ID);
        StringList slToObjTypes = (StringList) htRelData
                .get(DomainRelationship.SELECT_TO_TYPE);
        StringList slRelTypes = (StringList) htRelData
                .get(DomainRelationship.SELECT_TYPE);

        String strRuleId = (String) slToObjIds.get(0);
        String strFromObjId = (String) slFromObjIds.get(0);
        String strFromObjType = (String) slFromObjTypes.get(0);

        BusinessType bType = new BusinessType(strFromObjType, new Vault(""));
        StringList slListParents = bType.getParents(context);

        String strParentType = "";
        if (slListParents.size() != 0) {

            strParentType = (String) slListParents.get(0);
            bType = new BusinessType(strParentType, new Vault(""));
            slListParents = bType.getParents(context);
        } else {
            strParentType = strFromObjType;
        }

        String strToType = (String) slToObjTypes.get(0);
        String strRelType = (String) slRelTypes.get(0);

        DomainObject domRelObj = new DomainObject(strRuleId);

        DomainObject domfromObj = new DomainObject(strFromObjId);

        String strChildType = "";
        String strChildRel = "";
        String strChildTypePl = "";
        String strChildRelPl = "";

        if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_PRODUCT_LINE) ) {

            strChildType = ConfigurationConstants.TYPE_MODEL;
            strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS;
            strChildTypePl = ConfigurationConstants.TYPE_PRODUCT_LINE;
            strChildRelPl = ConfigurationConstants.RELATIONSHIP_SUB_PRODUCT_LINES;

        } else if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_MODEL) ) {

            strChildType = ConfigurationConstants.TYPE_PRODUCTS;
            strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCTS;
        } else if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_PRODUCTS) ) {
            strChildType = ConfigurationConstants.TYPE_PRODUCTS;
            strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;
        }
        List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
        
        // added the selectables for avoiding the DB call
        lstObjectSelects.add(DomainConstants.SELECT_CURRENT);
        lstObjectSelects.add(DomainConstants.SELECT_POLICY);
        

        MapList childObjList = domfromObj.getRelatedObjects(context,
                strChildRel, strChildType, (StringList) lstObjectSelects, null,
                false, true, (short) 1, DomainConstants.EMPTY_STRING,
                DomainConstants.EMPTY_STRING, 0);
        
        
        if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_PRODUCT_LINE) ) {
            MapList childObjListPl = domfromObj.getRelatedObjects(context,
                    strChildRelPl, strChildTypePl,
                    (StringList) lstObjectSelects, null, false, true,
                    (short) 1, DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING, 0);
            DomainObject domObj = new DomainObject();

            for (int iCnt = 0; iCnt < childObjListPl.size(); iCnt++) {
                Hashtable htChildObjMap = (Hashtable) childObjListPl.get(iCnt);
                String strObjId = (String) htChildObjMap
                        .get(DomainConstants.SELECT_ID);
                
                // getting the required selectable and avoided the DB call
                String sObjState =(String) htChildObjMap
                        .get(DomainConstants.SELECT_CURRENT);
 
                String sInvalidStates = EnoviaResourceBundle
                        .getProperty(context,"emxConfiguration.Attribute.MandatoryRules.InheritanceNotAllowedStates");
                boolean bInvalidState = false;
                
                // getting the required selectable and avoided the DB call
                String sPolicy = (String) htChildObjMap
                        .get(DomainConstants.SELECT_POLICY);
                
                StringTokenizer sTokenizer = new StringTokenizer(
                        sInvalidStates, ",");
                while (sTokenizer.hasMoreTokens()) {
                    String sInvalidStateSymName = sTokenizer.nextToken();
                    String sInvalidState = FrameworkUtil.lookupStateName(
                            context, sPolicy, sInvalidStateSymName);
                    if (sInvalidState != null
                            && sInvalidState.equals(sObjState)) {
                        bInvalidState = true;
                    }
                }
                if (bInvalidState == true) {
                    continue;
                }
                domObj.setId(strObjId);

                StringList objSelects = new StringList(3);
                objSelects.add(DomainConstants.SELECT_ID);
                objSelects.add(DomainConstants.SELECT_TYPE);
                objSelects.add(DomainConstants.SELECT_NAME);

                StringList relSelects = new StringList(2);
                relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
                relSelects.add(DomainConstants.SELECT_RELATIONSHIP_NAME);

                MapList objRuleList = domObj.getRelatedObjects(context,
                        strRelType, strToType, true, true, 1, objSelects,
                        relSelects, "id==" + strRuleId, null, null, null, null);
                DomainRelationship domRelationship = null;
                String relId = ""; // added for CR 138-2

                if (objRuleList.size() > 0) {
                    Hashtable objTable = (Hashtable) objRuleList.get(0);
                    relId = (String) objTable
                            .get(DomainConstants.SELECT_RELATIONSHIP_ID); // modified
                    // for
                    // CR
                    // 138-2
                    domRelationship = new DomainRelationship(relId);
                } else {
                    domRelationship = DomainRelationship.connect(context,
                            domObj, strRelType, domRelObj);

                    // added for CR 138-2 -- starts
                    StringList s2RelSelects = new StringList(1);
                    s2RelSelects.addElement(DomainRelationship.SELECT_ID);
                    Hashtable currentRelData = domRelationship
                            .getRelationshipData(context, s2RelSelects);
                    StringList s2RelIds = (StringList) currentRelData
                            .get(DomainRelationship.SELECT_ID);
                    relId = (String) s2RelIds.get(0);
                    // added for CR 138-2 -- end

                }
                domRelationship.openRelationship(context);

                if (!strChildRelPl
                        .equals(ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION)) {
                    domRelationship.setAttributeValue(context,
                            ConfigurationConstants.ATTRIBUTE_MANDATORYRULE,
                            "Yes");
                }
                domRelationship.setAttributeValue(context,
                        ConfigurationConstants.ATTRIBUTE_INHERITED, "True");
                domRelationship.closeRelationship(context, true);
                // added for CR 138-2 -- starts
                String relInheritedFrom = (String) PropertyUtil
                        .getSchemaProperty(context,"relationship_InheritedFrom");
                /*String strMqlCmd2 = "add connection \"" + relInheritedFrom
                        + "\" fromrel \"" + strRlId + "\" torel \"" + relId;*/
                String strMqlCmd2 = "add connection $1 fromrel $2 torel $3";                
                MqlUtil.mqlCommand(context, strMqlCmd2, true,relInheritedFrom,strRlId,relId);
                // added for CR 138-2 -- end
            }

        }
        DomainObject domObj = new DomainObject();

        for (int iCnt = 0; iCnt < childObjList.size(); iCnt++) {

            Hashtable htChildObjMap = (Hashtable) childObjList.get(iCnt);
            String strObjId = (String) htChildObjMap
                    .get(DomainConstants.SELECT_ID);
            
            // getting the required selectable and avoided the DB call
            String sObjState =(String) htChildObjMap
                    .get(DomainConstants.SELECT_CURRENT);
            
            String sInvalidStates = EnoviaResourceBundle
                    .getProperty(context,"emxConfiguration.Attribute.MandatoryRules.InheritanceNotAllowedStates");
            boolean bInvalidState = false;
            
            // getting the required selectable and avoided the DB call
            String sPolicy = (String) htChildObjMap
                    .get(DomainConstants.SELECT_POLICY);
            
            StringTokenizer sTokenizer = new StringTokenizer(sInvalidStates,
                    ",");
            while (sTokenizer.hasMoreTokens()) {
                String sInvalidStateSymName = sTokenizer.nextToken();
                String sInvalidState = FrameworkUtil.lookupStateName(context,
                        sPolicy, sInvalidStateSymName);
                if (sInvalidState != null && sInvalidState.equals(sObjState)) {
                    bInvalidState = true;
                }
            }
            if (bInvalidState == true) {
                continue;
            }
            domObj.setId(strObjId);

            StringList objSelects = new StringList(3);
            objSelects.add(DomainConstants.SELECT_ID);
            objSelects.add(DomainConstants.SELECT_TYPE);
            objSelects.add(DomainConstants.SELECT_NAME);

            StringList relSelects = new StringList(2);
            relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            relSelects.add(DomainConstants.SELECT_RELATIONSHIP_NAME);

            MapList objRuleList = domObj.getRelatedObjects(context, strRelType,
                    strToType, true, true, 1, objSelects, relSelects, "id=="
                            + strRuleId, null, null, null, null);
            DomainRelationship domRelationship = null;
            String relId = ""; // added for CR 138-2

            if (objRuleList.size() > 0) {
                Hashtable objTable = (Hashtable) objRuleList.get(0);
                relId = (String) objTable
                        .get(DomainConstants.SELECT_RELATIONSHIP_ID); // modified
                // for
                // CR
                // 138-2
                domRelationship = new DomainRelationship(relId);
            } else {
                domRelationship = DomainRelationship.connect(context, domObj,
                        strRelType, domRelObj);
                // added for CR 138-2 -- starts
                StringList s2RelSelects = new StringList(1);
                s2RelSelects.addElement(DomainRelationship.SELECT_ID);
                Hashtable currentRelData = domRelationship.getRelationshipData(
                        context, s2RelSelects);
                StringList s2RelIds = (StringList) currentRelData
                        .get(DomainRelationship.SELECT_ID);
                relId = (String) s2RelIds.get(0);
                // added for CR 138-2 -- end
            }
            domRelationship.openRelationship(context);

            if (!strChildRel
                    .equals(ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION)) {
                domRelationship.setAttributeValue(context,
                        ConfigurationConstants.ATTRIBUTE_MANDATORYRULE, "Yes");
            }
            domRelationship.setAttributeValue(context,
                    ConfigurationConstants.ATTRIBUTE_INHERITED, "True");
            domRelationship.closeRelationship(context, true);
            // added for CR 138-2 -- starts
            String relInheritedFrom = (String) PropertyUtil
                    .getSchemaProperty(context,"relationship_InheritedFrom");
            String strMqlCmd2 = "add connection $1 fromrel $2 torel $3";
            MqlUtil.mqlCommand(context, strMqlCmd2, true,relInheritedFrom,strRlId,relId);
            // added for CR 138-2 -- end
        }
    }
    



    /**
     * Returns the Range Values for "Mandatory Rule" attribute.
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
    public Map getRangeValuesForMandatoryRuleAttribute(Context context,
            String[] args) throws Exception {
        String strAttributeName = ConfigurationConstants.ATTRIBUTE_MANDATORYRULE;
        HashMap rangeMap = new HashMap();
        matrix.db.AttributeType attribName = new matrix.db.AttributeType(
                strAttributeName);
        attribName.open(context);

        List attributeRange = attribName.getChoices();
        List attributeDisplayRange = i18nNow
                .getAttrRangeI18NStringList(
                        ConfigurationConstants.ATTRIBUTE_MANDATORYRULE,
                        (StringList) attributeRange, context.getSession()
                                .getLanguage());
        rangeMap.put(FIELD_CHOICES, attributeRange);
        rangeMap.put(FIELD_DISPLAY_CHOICES, attributeDisplayRange);
        return rangeMap;
    }

    /**
     * set the attribute Values for "Mandatory Rule" .
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails Exception
     */
    public void updateValuesForMandatoryRuleAttribute(Context context,
            String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");

        String strRelId = (String) paramMap.get("relId");
        String strNewAttribval = (String) paramMap.get("New Value");
        if (strRelId != null && !"".equals(strRelId)) {
            DomainRelationship domRel = new DomainRelationship(strRelId);
            domRel.setAttributeValue(context,
                    ConfigurationConstants.ATTRIBUTE_MANDATORYRULE,
                    strNewAttribval);
        }
    }

   

    /**
     * Method to return a string in the form of T::N::R from an Object id.
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception
     * 
     */
    public String getTNRForObject(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String language = context.getSession().getLanguage();
        String strId = (String) programMap.get("id");
        List lstobjectSelects = new StringList();
        lstobjectSelects.add(SELECT_REVISION);
        lstobjectSelects.add(SELECT_NAME);
        lstobjectSelects.add(SELECT_TYPE);
        StringBuffer sbTemp = new StringBuffer();
        DomainObject domObj = newInstance(context, strId);
        Map mapInfo = domObj.getInfo(context, (StringList) lstobjectSelects);

        String tempObType = (String) mapInfo.get(DomainConstants.SELECT_TYPE);
	    if(tempObType.indexOf(" ") > 0)
	    	tempObType = tempObType.replaceAll(" ","_");
	    String i18ObjectType=EnoviaResourceBundle.getProperty(context, "Framework","emxFramework.Type."+tempObType,language);
	    sbTemp = sbTemp.append(
	    		i18ObjectType).append(
                FEATURE_TNR_DELIMITER).append(
                (String) mapInfo.get(DomainConstants.SELECT_NAME)).append(
                FEATURE_TNR_DELIMITER).append(
                (String) mapInfo.get(DomainConstants.SELECT_REVISION));
        return sbTemp.toString();
    }

    /**
     * Method to return a string in the <Marketing Name> from an Object id.
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception
     * 
     */
    public String getMarketingName(Context context, String[] args)
    throws Exception {

    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	String strId = (String) programMap.get("id");
    	String strMarketingName = "";
    	DomainObject domObj = newInstance(context, strId);
    	StringList slObjSel= new StringList(ConfigurationConstants.SELECT_NAME);
    	slObjSel.add(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
    	slObjSel.add(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
    	slObjSel.add(ConfigurationConstants.SELECT_REVISION);
    	Map contextNameMap=domObj.getInfo(context, slObjSel);
    	if(contextNameMap.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME))
    		strMarketingName=(String)contextNameMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
    	if(strMarketingName==null || strMarketingName.trim().isEmpty())
    		strMarketingName=(String)contextNameMap.get(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
    	if(strMarketingName==null || strMarketingName.trim().isEmpty())
    		strMarketingName=(String)contextNameMap.get(ConfigurationConstants.SELECT_NAME);

    	return strMarketingName;
    }

    

    

    public Map getRangeValuesForRuleClassificationAttribute(Context context,
            String[] args) throws Exception {

        String strAttributeName = ConfigurationConstants.ATTRIBUTE_RULE_CLASSIFICATION;
        HashMap rangeMap = new HashMap();
        matrix.db.AttributeType attribName = new matrix.db.AttributeType(
                strAttributeName);
        attribName.open(context);

        List attributeRange = attribName.getChoices();
        
        if(attributeRange!=null){
        	  	attributeRange.remove("Manufacturing");
        	  	attributeRange.remove("Logical");
        }
        
        List attributeDisplayRange = i18nNow
                .getAttrRangeI18NStringList(
                        ConfigurationConstants.ATTRIBUTE_RULE_CLASSIFICATION,
                        (StringList) attributeRange, context.getSession()
                                .getLanguage());
        rangeMap.put(FIELD_CHOICES, attributeRange);
        rangeMap.put(FIELD_DISPLAY_CHOICES, attributeDisplayRange);

        return rangeMap;
    }

    /**
     * This method will be executed only when the Scenario is Edit of the Configurable Rule.
     * This is a background job method. 
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds no arguments.
     * @throws Exception
     *             if the operation fails.
     * @since X3.HFx
     */
	
    public void ruleInheritanceOnEdit(Context context,  String args[]) throws Exception {
		try{

				String strContextId = (String) args[0];
				String strRuleId = (String) args[1];
				String strRuleRelId = (String) args[2];
				
				// Domain Object of the Context Object
				DomainObject dom = new DomainObject(strContextId);

				// Domain Relations to get the Details of the Relationship Type and Rule Type
				DomainRelationship domRel = new DomainRelationship(strRuleRelId);

				StringList slRelSelects = new StringList(5);
				slRelSelects.addElement(DomainRelationship.SELECT_FROM_ID);
				slRelSelects.addElement(DomainRelationship.SELECT_FROM_TYPE);
				slRelSelects.addElement(DomainRelationship.SELECT_TO_ID);
				slRelSelects.addElement(DomainRelationship.SELECT_TO_TYPE);
				slRelSelects.addElement(DomainRelationship.SELECT_TYPE);

				Hashtable htRelData = domRel.getRelationshipData(context,slRelSelects);
				StringList slFromObjTypes = (StringList) htRelData.get(DomainRelationship.SELECT_FROM_TYPE);
				StringList slToObjTypes = (StringList) htRelData.get(DomainRelationship.SELECT_TO_TYPE);
				StringList slRelTypes = (StringList) htRelData.get(DomainRelationship.SELECT_TYPE);

				String strFromObjType = (String) slFromObjTypes.get(0);
				//String strRuleId = (String) slToObjIds.get(0);
				String strRuleType = (String) slToObjTypes.get(0);
				String strRelType = (String) slRelTypes.get(0);

				BusinessType bType = new BusinessType(strFromObjType, new Vault(""));
				StringList slListParents = bType.getParents(context);

				String strParentType = "";
				if (slListParents.size() != 0) {

					strParentType = (String) slListParents.get(0);
					bType = new BusinessType(strParentType, new Vault(""));
					slListParents = bType.getParents(context);
				} else {
					strParentType = strFromObjType;
				}
				
				List subTypes = new StringList() ;
				String strChildType = "";
				String strChildRel = "";

				String strChildRelsForEdit="";
				StringBuffer sbChildRelForEdit = new StringBuffer();

				if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_PRODUCT_LINE) ) {
					// for get Related to Process all the Objects at once
					strChildType = ConfigurationConstants.TYPE_PRODUCT_LINE+","+ConfigurationConstants.TYPE_MODEL +"," +ConfigurationConstants.TYPE_PRODUCTS;
					strChildRel = ConfigurationConstants.RELATIONSHIP_SUB_PRODUCT_LINES+","+ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS+","+ConfigurationConstants.RELATIONSHIP_PRODUCTS+","+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;
					
					// process for the Objects which are already connected
					subTypes.add(ConfigurationConstants.TYPE_PRODUCT_LINE);
					subTypes.add(ConfigurationConstants.TYPE_MODEL);					
					strChildRelsForEdit = ConfigurationConstants.RELATIONSHIP_SUB_PRODUCT_LINES+","+ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS;

				} else if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_MODEL) ) {
					strChildType = ConfigurationConstants.TYPE_PRODUCTS;
					strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCTS+","+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;
					
					subTypes = ProductLineUtil.getChildrenTypes(context, ConfigurationConstants.TYPE_PRODUCTS);	
					sbChildRelForEdit.append(ConfigurationConstants.RELATIONSHIP_PRODUCTS);
					sbChildRelForEdit.append(",");
					sbChildRelForEdit.append(ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT);
					strChildRelsForEdit = ConfigurationConstants.RELATIONSHIP_PRODUCTS;
					strChildRelsForEdit = sbChildRelForEdit.toString();
				

				} else if (mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_PRODUCTS) ) {
					strChildType = ConfigurationConstants.TYPE_PRODUCTS;
					strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;
					
					subTypes = ProductLineUtil.getChildrenTypes(context, ConfigurationConstants.TYPE_PRODUCTS);					
					strChildRelsForEdit = ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;
					//isLast = true;
				}

				// added the selectables for avoiding the DB call
				List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
				lstObjectSelects.add(DomainConstants.SELECT_CURRENT);
				lstObjectSelects.add(DomainConstants.SELECT_POLICY);
				lstObjectSelects.add(DomainConstants.SELECT_TYPE);
				lstObjectSelects.add(DomainConstants.SELECT_NAME);


				StringList relSelects = new StringList(2);
				relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
				relSelects.add(DomainConstants.SELECT_RELATIONSHIP_NAME);


				MapList childObjList = dom.getRelatedObjects(context,
						strChildRel, strChildType, (StringList) lstObjectSelects, relSelects,
						false, true, (short) 0, DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING,0);
				if (childObjList != null && childObjList.size() > 0) {
					childObjList.addSortKey(DomainObject.SELECT_LEVEL, "ascending", "String");
					childObjList.sort();
				}
				DomainRelationship domRelationship = null;


				String relId = ""; // added for CR 138-2
				String relInheritedFrom = (String) PropertyUtil.getSchemaProperty(context,"relationship_InheritedFrom");

				StringList strListRel = new StringList(1);
				strListRel.add(DomainConstants.SELECT_ID);
				Hashtable relData = null;
				StringList s2RelIds = null;

				DomainObject domObj = new DomainObject();

				// Domain Object of the Rule edited
				DomainObject domBCR = new DomainObject(strRuleId);

				// this map will contain all (Sub Product Line, Model, Products, Product Versions) the objects 
				// to which the Rule Objects to be connected
				for (int iCnt = 0; iCnt < childObjList.size(); iCnt++) {
					Hashtable htChildObjMap = (Hashtable) childObjList.get(iCnt);
					String strObjId = (String) htChildObjMap.get(DomainConstants.SELECT_ID);

					// Check if the Object is in Invalid State to Avoid Inheritance of the Rules
					String sObjState = (String) htChildObjMap.get(DomainConstants.SELECT_CURRENT);
					String sInvalidStates = EnoviaResourceBundle.getProperty(context,"emxConfiguration.Attribute.MandatoryRules.InheritanceNotAllowedStates");

					boolean bInvalidState = false;
					String sPolicy = (String) htChildObjMap.get(DomainConstants.SELECT_POLICY);
					StringTokenizer sTokenizer = new StringTokenizer(sInvalidStates, ",");
					while (sTokenizer.hasMoreTokens()) {
						String sInvalidStateSymName = sTokenizer.nextToken();
						String sInvalidState = FrameworkUtil.lookupStateName(context, sPolicy, sInvalidStateSymName);
						if (sInvalidState != null
								&& sInvalidState.equals(sObjState)) {
							bInvalidState = true;
						}
					}
					if (bInvalidState == true) {
						continue;
					}

					domObj.setId(strObjId);

					// Check if the Rule is already connected						
					List lstRelSelects = new StringList(DomainRelationship.SELECT_ID);
					String strWhere = "id==\""+ strRuleId + "\"";                
				
					// Get only the details of the Rule Object connected to the Chil Object 
					MapList relList = domObj.getRelatedObjects(context, strRelType, strRuleType,
									(StringList) lstObjectSelects, (StringList) lstRelSelects, false, true,
									(short) 1, strWhere,
									DomainConstants.EMPTY_STRING, 0);


					// If the Rule is not connected
					// Connect the Rule
					if(relList.size()==0){
						domRelationship = DomainRelationship.connect(context,domObj, strRelType, domBCR);
						
						String strMqlCommand = "trigger off";
		        		MqlUtil.mqlCommand(context,strMqlCommand,true);
		        		
						domRelationship.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_INHERITED, "True");
                        domRelationship.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_MANDATORYRULE, "Yes");                        

                        strMqlCommand = "trigger on";
              		    MqlUtil.mqlCommand(context,strMqlCommand,true);
              		    
						relData = (Hashtable)domRelationship.getRelationshipData(context,strListRel);

						s2RelIds = (StringList) relData.get(DomainConstants.SELECT_ID);
						relId = (String) s2RelIds.get(0);

						 //added for CR 138-2 -- starts
						/*String strMqlCmd2 = "add connection \"" + relInheritedFrom + "\" fromrel \"" + strRuleRelId + "\" torel \"" + relId;
						MqlUtil.mqlCommand(context, strMqlCmd2, true);*/
						String strMqlCmd2 = "add connection $1 fromrel $2 torel $3";
						MqlUtil.mqlCommand(context, strMqlCmd2, true,relInheritedFrom,strRuleRelId,relId);
						// added for CR 138-2 -- end

						strRuleRelId = relId;
					} 
					else{ // If the Rule is already connected then update the Rel to Rel Connection for the immediate Child and Update Inherited Attribute
						for (int j = 0; j < relList.size(); j++) {
							Hashtable htChMap = (Hashtable) relList.get(j);
							String strchildType = (String) htChildObjMap.get(DomainObject.SELECT_TYPE);
							String strchildRel = (String) htChildObjMap.get(DomainConstants.SELECT_RELATIONSHIP_NAME);
							relId = (String) htChMap.get(DomainRelationship.SELECT_ID);

							String RelWhereForContext = "tomid["+ConfigurationConstants.RELATIONSHIP_INHERITED_FROM+"].fromrel.id=="+ strRuleRelId;
					    	
							//Need to check if relationship already exists
							//DomainObject domObj1 = new DomainObject(strFromObjId);
							MapList MlRelIds = domObj.getRelatedObjects(context,
									                                   ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE,  //String relPattern
									                                   ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE,//typePatternForContext, //String typePattern
									                                   null,          //StringList objectSelects,
									                                   new StringList(SELECT_RELATIONSHIP_ID), //StringList relationshipSelects,
									                                   false,                     //boolean getTo,
									                                   true,                     //boolean getFrom,
									                                   (short)1,                 //short recurseToLevel,
									                                   null,    //String objectWhere,
									                                   RelWhereForContext,   //String relationshipWhere,
									                                   0,              //Query Limit
									                                   null,                     //Pattern includeType,
									                                   null,                     //Pattern includeRelationship,
									                                   null);
							
							
							
							if (subTypes.contains(strchildType) && strChildRelsForEdit.contains(strchildRel) 
								&& MlRelIds.isEmpty())
								//)
							{
								//relId = (String) htChMap.get(DomainRelationship.SELECT_ID);
								// Process for only the Immediate Child objects not for all

								domRelationship = new DomainRelationship(relId);
								String strMqlCommand = "trigger off";
				        		MqlUtil.mqlCommand(context,strMqlCommand,true);
								
								domRelationship.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_INHERITED, "True");
                                domRelationship.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_MANDATORYRULE, "Yes");
                                
                                strMqlCommand = "trigger on";
                      		    MqlUtil.mqlCommand(context,strMqlCommand,true);
                      		
                                String strMqlCmd2 = "add connection $1 fromrel $2 torel $3";
                                MqlUtil.mqlCommand(context, strMqlCmd2, true,relInheritedFrom,strRuleRelId,relId);
                                
                                
								// added for CR 138-2 -- starts
								/*String strMqlCmd2 = "add connection \"" + relInheritedFrom + "\" fromrel \"" + strRuleRelId + "\" torel \"" + relId;
								MqlUtil.mqlCommand(context, strMqlCmd2, true);*/
								// added for CR 138-2 -- end 
							}
						}
					}
					
					//if(!isLast)
					//{	
						//callAgainMethod(context,strObjId, strRuleId,relId );					
					//}
					//break;
					
				}
			}
			catch(Exception e){
				System.out.println("*************** Exception from Background Job method: emxRuleBase:ruleInheritanceOnEdit");
				e.printStackTrace();
			}
	}


    /**
     * This method will be executed only when the Scenario is Create of the Configurable Rule.
     * This is a background job method. 
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds no arguments.
     * @throws Exception
     *             if the operation fails.
     * @since X3.HFx
     */


    public void ruleInheritanceOnCreate(Context context,  String args[]) throws Exception {
		try{
				String strContextId = (String) args[0];
				String strRuleId = (String) args[1];
				String strRuleRelType = (String) args[2];

				// Domain Object of the Context Object in which the Rule is Created
				DomainObject dom = new DomainObject(strContextId);
				String strContextObjType = dom.getInfo(context,DomainConstants.SELECT_TYPE);

				// Domain Object of the Rule that is created
				DomainObject domBCR = new DomainObject(strRuleId);
				String strRelIdBCRContext = domBCR.getInfo(context,"to["+strRuleRelType+"].id");


				String strChildType = "";
				String strChildRel = "";

				if (mxType.isOfParentType(context,strContextObjType,ConfigurationConstants.TYPE_PRODUCT_LINE) ) {
					// for get Related to Process all the Objects at once
					strChildType = ConfigurationConstants.TYPE_PRODUCT_LINE+","+ConfigurationConstants.TYPE_MODEL +"," +ConfigurationConstants.TYPE_PRODUCTS;
					strChildRel = ConfigurationConstants.RELATIONSHIP_SUB_PRODUCT_LINES+","+ConfigurationConstants.RELATIONSHIP_PRODUCTLINE_MODELS+","+ConfigurationConstants.RELATIONSHIP_PRODUCTS+","+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;

				} else if (mxType.isOfParentType(context,strContextObjType,ConfigurationConstants.TYPE_MODEL) ) {
					strChildType = ConfigurationConstants.TYPE_PRODUCTS;
					strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCTS+","+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;
				} else if (mxType.isOfParentType(context, strContextObjType,ConfigurationConstants.TYPE_PRODUCTS) 
                        && ! strContextObjType.equals(ConfigurationConstants.TYPE_PRODUCT_VARIANT)) {
					strChildType = ConfigurationConstants.TYPE_PRODUCTS;
					strChildRel = ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION;
				}

				// Object Selects
				List lstObjectSelects = new StringList(DomainConstants.SELECT_ID);
				lstObjectSelects.add(DomainConstants.SELECT_CURRENT);
				lstObjectSelects.add(DomainConstants.SELECT_POLICY);
				lstObjectSelects.add(DomainConstants.SELECT_TYPE);
				lstObjectSelects.add(DomainConstants.SELECT_NAME);

				// Rel Selects
				StringList relSelects = new StringList(2);
				relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
				relSelects.add(DomainConstants.SELECT_RELATIONSHIP_NAME);

				// get all the Child level Objects Sub-Product Line, Models, Products and Product Versions/Variants
				MapList childObjList = dom.getRelatedObjects(context,
						strChildRel, strChildType, (StringList) lstObjectSelects, relSelects,
						false, true, (short) 0, DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING, 0);

				DomainRelationship domRelationship = null;

				// Domain Object of the Boolean Compatibity Rule which is Edited from No to Yes

				String relId = ""; // added for CR 138-2
				String relInheritedFrom = (String) PropertyUtil.getSchemaProperty(context,"relationship_InheritedFrom");


				StringList strListRel = new StringList(1);
				strListRel.add(DomainConstants.SELECT_ID);
				Hashtable relData = null;
				StringList s2RelIds = null;

				DomainObject domObj = new DomainObject();
				// Added for bug 372151
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

				for (int iCnt = 0; iCnt < childObjList.size(); iCnt++) {
					Hashtable htChildObjMap = (Hashtable) childObjList.get(iCnt);
					String strObjId = (String) htChildObjMap.get(DomainConstants.SELECT_ID);

					// Check if the Object is in Invalid State to Avoid Inheritance of the Rules
					String sObjState = (String) htChildObjMap.get(DomainConstants.SELECT_CURRENT);
					String sInvalidStates = EnoviaResourceBundle.getProperty(context,"emxConfiguration.Attribute.MandatoryRules.InheritanceNotAllowedStates");

					boolean bInvalidState = false;
					String sPolicy = (String) htChildObjMap.get(DomainConstants.SELECT_POLICY);
					StringTokenizer sTokenizer = new StringTokenizer(sInvalidStates, ",");
					while (sTokenizer.hasMoreTokens()) {
						String sInvalidStateSymName = sTokenizer.nextToken();
						String sInvalidState = FrameworkUtil.lookupStateName(context, sPolicy, sInvalidStateSymName);
						if (sInvalidState != null
								&& sInvalidState.equals(sObjState)) {
							bInvalidState = true;
						}
					}
					if (bInvalidState == true) {
						continue;
					}


					domObj.setId(strObjId);

					domRelationship = DomainRelationship.connect(context,domObj, strRuleRelType, domBCR);
					domRelationship.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_INHERITED, "True");
                    domRelationship.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_MANDATORYRULE, "Yes");

					relData = (Hashtable)domRelationship.getRelationshipData(context,strListRel);

					s2RelIds = (StringList) relData.get(DomainConstants.SELECT_ID);
					relId = (String) s2RelIds.get(0);

					 //added for CR 138-2 -- starts
					/*String strMqlCmd2 = "add connection \"" + relInheritedFrom
							+ "\" fromrel \"" + strRelIdBCRContext + "\" torel \"" + relId;
					MqlUtil.mqlCommand(context, strMqlCmd2, true);*/
					
					String strMqlCmd2 = "add connection $1 fromrel $2 torel $3";
					MqlUtil.mqlCommand(context, strMqlCmd2, true,relInheritedFrom,strRelIdBCRContext,relId);
					// added for CR 138-2 -- end
					strRelIdBCRContext = relId;
				}
				ContextUtil.popContext(context);
			}
			catch(Exception e){
				System.out.println("*************** Exception from Background Job method: emxRuleBase:ruleInheritanceOnCreate");
				e.printStackTrace();
				ContextUtil.popContext(context);
			}

	  }
	/**
	 * This is a Access function for Resource Usage command under the Resources Rules
	 * added to fix IR -- Mx377985  
	 * @param context
	 * @param args
	 * @return boolean - false if the parent object is Product Line or Model
	 * 				   - true if the above condition doesn't satisfy.
	 * @throws Exception
	 * @author IVU
	 * 
	 */
    public boolean showResourceUsage(Context context,String args[]) throws Exception
    {
        HashMap configurableRulesMap = (HashMap) JPO.unpackArgs(args);
        String strParentObjectId = (String) configurableRulesMap.get("parentOID");
        boolean showCommand = true;
        if(strParentObjectId!=null && !strParentObjectId.equals("") && !("").equals(strParentObjectId)){
        	DomainObject domParent = new DomainObject(strParentObjectId);
        	if(mxType.isOfParentType(context,domParent.getInfo(context,DomainObject.SELECT_TYPE),ConfigurationConstants.TYPE_PRODUCT_LINE) ||
        			mxType.isOfParentType(context,domParent.getInfo(context,DomainObject.SELECT_TYPE),ConfigurationConstants.TYPE_MODEL) ){
        		showCommand = false;
        	}
        }
        return showCommand;
    }
    
    /**
     * Method to return a string in the <Marketing Name Revision > from an Object id.
     * Added  for  IR Mx376196
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String getMarketingNameRev(Context context, String[] args)
    throws Exception {

    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	String strId = (String) programMap.get("id");
    	String strMarketingName = "";
    	DomainObject domObj = newInstance(context, strId);
    	StringList slObjSel = new StringList(ConfigurationConstants.SELECT_NAME);
    	slObjSel.add(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
    	slObjSel.add(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
    	slObjSel.add(ConfigurationConstants.SELECT_REVISION);
    	Map contextNameMap = domObj.getInfo(context, slObjSel);
    	if (contextNameMap
    			.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME))
    		strMarketingName = (String) contextNameMap
    		.get(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
    	if (strMarketingName == null || strMarketingName.trim().isEmpty())
    		strMarketingName = (String) contextNameMap
    		.get(ConfigurationConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
    	if (strMarketingName == null || strMarketingName.trim().isEmpty())
    		strMarketingName = (String) contextNameMap
    		.get(ConfigurationConstants.SELECT_NAME);

    	strMarketingName=strMarketingName+ " "
    	+ (String) contextNameMap
    	.get(ConfigurationConstants.SELECT_REVISION);

    	return strMarketingName;
    }
    
    public void updateExpressionAttributes(Context context, String args[])
    throws Exception {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	String strRuleId = (String) programMap.get("ruleId");
    	DomainObject objRule = new DomainObject(strRuleId);
    	String strRuleRightExp = getExpression(context,
    			ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION, strRuleId);
    	String strRuleLeftExp = getExpression(context,
    			ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION, strRuleId);
    	objRule.setAttributeValue(context,
    			ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION,
    			strRuleLeftExp);
    	objRule.setAttributeValue(context,
    			ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION,
    			strRuleRightExp);

}
    
    public String getExpression(Context context, String strType,
            String strIncRuleID) throws Exception {
        // Inclusion Rule Object Id

        // Retrieving the Rule Type Object Id
      String strRuleExp = "";
        Map mapTemp = new HashMap();
        MapList objectList = new MapList();
        Map paramList = new HashMap();

        Map programMap = new HashMap();
        String[] arrJPOArguments = new String[1];

        mapTemp.put(DomainConstants.SELECT_ID, strIncRuleID.trim());
        objectList.add(mapTemp);

        paramList.put("intermediate", "true");
        programMap.put("objectList", objectList);
        programMap.put("paramList", paramList);
        if (strType
                .equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION)) {
            programMap.put("strRelType",
                    ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION);
        } else {
            programMap.put("strRelType",
                    ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);
        }
        programMap.put("strRuleDisplay",
                ConfigurationConstants.RULE_DISPLAY_FULL_NAME);
        arrJPOArguments = JPO.packArgs(programMap);

        StringList strRightExpressionList = new StringList();
        strRightExpressionList = (StringList) (JPO.invoke(context,
                "emxBooleanCompatibility", null, "getExpression",
                arrJPOArguments, StringList.class));
        // Rule Expression associated with the Object
        strRuleExp = (String) strRightExpressionList.get(0);

        return strRuleExp;

    }
    
    /**
     * Method call to get the Edit Link for Rule Extension Summary Page.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments: 1:Relationship Id. 2:Feature Id.
     * @return Object - MapList containing the context name and ids.
     * @throws Exception
     *             if the operation fails
	 * @author IXH
	 * @since R212
	 * @category 
     */
    public Vector getEditColumnDisplayInTable(Context context, String[] args)throws Exception
    {        HashMap programMap = (HashMap) JPO.unpackArgs(args);
    MapList objList = (MapList) programMap.get("objectList");
    StringList relationshipSelects = new StringList(1);
    relationshipSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
    HashMap paramList = (HashMap) programMap.get("paramList");
    String strMqlCmd = "";
    String parentId=null;
    String contexId=(String)programMap.get("contextId");
    if(contexId==null||((contexId!=null) && contexId.equalsIgnoreCase(""))){
    	 parentId = (String) paramList.get("objectId");
    }else{
    parentId=contexId;
    }
    //String relId = (String) paramList.get("relId");
    String strLocale = context.getSession().getLanguage();
    String title = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxConfiguration.Title.NotEditableInherited",strLocale);
    
    Vector columnVals = new Vector(objList.size());
    Iterator i = objList.iterator();
    boolean isPrinterFriendly = false;
    String printerFriendly = (String) paramList.get("reportFormat");
    if (printerFriendly != null) 
    {
        isPrinterFriendly = true;
    }
    while (i.hasNext())
    {
        Map m = (Map) i.next();
        String id = (String) m.get(SELECT_ID);
        boolean isFrozen=ConfigurationUtil.isFrozenState(context, id);
        String relId = (String) m.get(SELECT_RELATIONSHIP_ID);
        String strType = (String) m.get(SELECT_TYPE);
        
        String strRelationShip = "";
        String strEditlink = "";
        if (strType.equals(ConfigurationConstants.TYPE_RULE_EXTENSION)) {
            strRelationShip = ConfigurationConstants.RELATIONSHIP_RULE_EXTENSION;
        } else if (strType.equals(ConfigurationConstants.TYPE_FIXED_RESOURCE)) {
            strRelationShip = ConfigurationConstants.RELATIONSHIP_RESOURCE_LIMIT;
        } else if (strType.equals(ConfigurationConstants.TYPE_MARKETING_PREFERENCE)) {
            strRelationShip = ConfigurationConstants.RELATIONSHIP_MARKETING_PREFERENCE;
        } else if (strType.equals(ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE)) {
            strRelationShip = ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE;
        } else if(strType.equals(ConfigurationConstants.TYPE_QUANTITY_RULE)) {
            strRelationShip = ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE;
        }
        boolean isInherited=false;
        if(!strType.equals(ConfigurationConstants.TYPE_PRODUCT_COMPATIBILITY_RULE)){
        	
        	/*strMqlCmd = "expand bus \"" + parentId + "\" from relationship \""
            + strRelationShip + "\" select rel id where to.id=='" + id
            + "' dump | ";

        	String strRelId = MqlUtil.mqlCommand(context, strMqlCmd, true);	*/
        strMqlCmd = "expand bus $1 from relationship $2 select $3 $4 where $5 dump $6 ";
        String relSelect ="rel";
        String idSelect = "id";
        String strWhere = "to.id=='" + id+"'";
        String strRelId = MqlUtil.mqlCommand(context, strMqlCmd, true,parentId,strRelationShip,relSelect,idSelect,strWhere,ConfigurationConstants.DELIMITER_PIPE);

        StringTokenizer strTokens = new StringTokenizer(strRelId, "|");
        while (strTokens.hasMoreTokens())
        {
            strRelId = strTokens.nextToken();
        }

        DomainRelationship relationshipObj = new DomainRelationship(strRelId);
        relationshipObj.open(context);
        String strInherited = relationshipObj.getAttributeValue(context,ConfigurationConstants.ATTRIBUTE_INHERITED);
        isInherited = strInherited.equals("True");
        relationshipObj.close(context);
        }
        if (strType.equals(ConfigurationConstants.TYPE_RULE_EXTENSION)) {
        	strEditlink = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxForm.jsp?form=FTRRuleExtensionForm%26mode=edit%26formHeader=emxProduct.Heading.RuleExtension.EditRule%26HelpMarker=emxhelpruleextensionedit%26suiteKey=Configuration%26StringResourceFileId=emxConfigurationStringResource%26SuiteDirectory=configuration%26submitAction=doNothing%26postProcessURL=../configuration/RuleExtensionCreateEditPostProcess.jsp%26relId="
                    + XSSUtil.encodeForHTMLAttribute(context,relId)
                    + "%26suiteKey=configuration%26SuiteDirectory=configuration%26parentOID="
                    + XSSUtil.encodeForHTMLAttribute(context,parentId)
                    + "%26objectId="
                    + XSSUtil.encodeForHTMLAttribute(context,id)
                    + "', '800', '700', 'true', 'slidein', '')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>";
        } else if (strType
                .equals(ConfigurationConstants.TYPE_MARKETING_PREFERENCE)) {
            strEditlink = "<a href=\"javascript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?modetype=edit%26commandName=FTRMarketingPreferenceRuleSettings%26ruleType=MarketingPreferenceRule%26SuiteDirectory=configuration%26relId="
                    + XSSUtil.encodeForHTMLAttribute(context,relId)
                    + "%26suiteKey=configuration%26parentOID="
                    + XSSUtil.encodeForHTMLAttribute(context,parentId)
                    + "%26objectId="
                    + XSSUtil.encodeForHTMLAttribute(context,id)
                    + "%26submitURL="
                    + "../configuration/MPREditPostProcess.jsp?mode=edit%26ruleType=MarketingPreferenceRule"
                    + "', '800', '700', 'true', 'popup', '','','false','Large')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>";
        } else if (strType
                .equals(ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE)) {
        	strEditlink = "<a href=\"javascript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?modetype=edit%26commandName=FTRBooleanCompatibilityRuleSettings%26ruleType=BooleanCompatibilityRule%26SuiteDirectory=configuration%26relId="
                    + XSSUtil.encodeForHTMLAttribute(context,relId)
                    + "%26suiteKey=configuration%26parentOID="
                    + XSSUtil.encodeForHTMLAttribute(context,parentId)
                    + "%26objectId="
                    + XSSUtil.encodeForHTMLAttribute(context,id)
                    + "%26submitURL="
                    + "../configuration/BCREditPostProcess.jsp?mode=edit%26ruleType=BooleanCompatibilityRule"
                    + "', '800', '700', 'true', 'popup', '','','false','Large')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>";
        } else if (strType
                .equals(ConfigurationConstants.TYPE_PRODUCT_COMPATIBILITY_RULE)) {
        	strEditlink = "<a href=\"javascript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?modetype=edit%26commandName=FTRProductCompatibilityRuleSettings%26ruleType=ProductCompatibilityRule%26SuiteDirectory=configuration%26relId="
                    + XSSUtil.encodeForHTMLAttribute(context,relId)
                    + "%26suiteKey=configuration%26parentOID="
                    + XSSUtil.encodeForHTMLAttribute(context,parentId)
                    + "%26objectId="
                    + XSSUtil.encodeForHTMLAttribute(context,id)
                    + "%26submitURL="
                    + "../configuration/PCREditPostProcess.jsp?mode=edit%26ruleType=ProductCompatibilityRule"
                    + "', '800', '700', 'true', 'popup', '','','false','Large')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>";

        }else if (strType
                .equals(ConfigurationConstants.TYPE_QUANTITY_RULE)) {
        	strEditlink = "<a href=\"javascript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?modetype=edit%26commandName=FTRQuantityRuleSettings%26ruleType=QuantityRule%26SuiteDirectory=configuration%26relId="
                    + XSSUtil.encodeForHTMLAttribute(context,relId)
                    + "%26suiteKey=configuration%26parentOID="
                    + XSSUtil.encodeForHTMLAttribute(context,parentId)
                    + "%26objectId="
                    + XSSUtil.encodeForHTMLAttribute(context,id)
                    + "%26submitURL="
                    + "../configuration/QuantityRuleEditPostProcess.jsp?mode=edit%26ruleType=QuantityRule"
                    + "', '800', '700', 'true', 'popup', '','','false','Large')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>";
        }else if (strType.equals(ConfigurationConstants.TYPE_FIXED_RESOURCE)) {
        	strEditlink = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxForm.jsp?form=FTRFixedResourceForm%26mode=edit%26formHeader=emxProduct.Heading.EditNoRev%26HelpMarker=emxhelpresourcerulesedit%26suiteKey=Configuration%26StringResourceFileId=emxConfigurationStringResource%26SuiteDirectory=configuration%26submitAction=doNothing%26postProcessURL=../configuration/FixedResourceRuleCreateEditPostProcess.jsp%26relId="
                + XSSUtil.encodeForHTMLAttribute(context,relId)
                + "%26suiteKey=configuration%26SuiteDirectory=configuration%26parentOID="
                + XSSUtil.encodeForHTMLAttribute(context,parentId)
                + "%26objectId="
                + XSSUtil.encodeForHTMLAttribute(context,id)
                + "', '800', '700', 'true', 'slidein', '')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>";
        }
        if (!isPrinterFriendly)
        { 
            if (!isInherited && !isFrozen )
            {
                columnVals.addElement(strEditlink);
            }
            else
            {
                columnVals.addElement("<img border=\"0\" src=\"images/iconActionLockforEdit.gif\" TITLE=\""+XSSUtil.encodeForHTMLAttribute(context,title)+"\"/>");
            }
        } 
        else
        {
            columnVals.addElement("<img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/>");
        }
    }
    return columnVals;

}

    /**
     * Method call to get the "Mandatory Column" in Rule list Page.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     * @return Object  -Vector containing Mandatory Value Yes or No.
     * @throws Exception
     *             if the operation fails
	 * @author IXH
	 * @since R212
	 * @category 
     */
 
  public Vector getMandatoryColumnInTable(Context context, String[] args)throws Exception {
		
		Vector vecAttrVals = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strMandVal = "";
		String strLanguage = context.getSession().getLanguage();
		MapList lstObjectIdsList = (MapList) programMap.get("objectList");
		String strObjId = "";
		if (lstObjectIdsList.get(0) instanceof HashMap) {
		    HashMap map = (HashMap) lstObjectIdsList.get(0);
		    strObjId = (String) map.get(DomainConstants.SELECT_ID);
		} else if (lstObjectIdsList.get(0) instanceof Hashtable) {
		    Hashtable htObj = (Hashtable) lstObjectIdsList.get(0);
		    strObjId = (String) htObj.get(DomainConstants.SELECT_ID);
		}
		
		DomainObject dom = new DomainObject(strObjId);
		String strType = (String) dom.getInfo(context,DomainConstants.SELECT_TYPE);
		HashMap paramList = (HashMap) programMap.get("paramList");
		String strObjId1 = (String) paramList.get("objectId");
		String strRel = "";
		if (strType.equals(ConfigurationConstants.TYPE_RULE_EXTENSION)) {
		    strRel = ConfigurationConstants.RELATIONSHIP_RULE_EXTENSION;
		} else if (strType.equals(ConfigurationConstants.TYPE_FIXED_RESOURCE)) {
		    strRel = ConfigurationConstants.RELATIONSHIP_RESOURCE_LIMIT;
		} else if (strType
		        .equals(ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE)) {
		    strRel = ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE;
		} else if (strType
		        .equals(ConfigurationConstants.TYPE_MARKETING_PREFERENCE)) {
		    strRel = ConfigurationConstants.RELATIONSHIP_MARKETING_PREFERENCE;
		}
		
		String strMqlCmd = "";
		for (int i = 0; i < lstObjectIdsList.size(); i++) {
		    strObjId = "";
		    if (lstObjectIdsList.get(i) instanceof HashMap) {
		        HashMap map = (HashMap) lstObjectIdsList.get(i);
		        strObjId = (String) map.get(DomainConstants.SELECT_ID);
		    } else if (lstObjectIdsList.get(i) instanceof Hashtable) {
		        Hashtable htObj = (Hashtable) lstObjectIdsList.get(i);
		        strObjId = (String) htObj.get(DomainConstants.SELECT_ID);
		    }
		
		    //TO DO:MQL command
		    /*strMqlCmd = "expand bus \"" + strObjId1 + "\" from relationship \""
		            + strRel + "\" select rel id where to.id=='" + strObjId
		            + "' dump | ";
		
		    String strRelId = MqlUtil.mqlCommand(context, strMqlCmd, true);*/
		    strMqlCmd = "expand bus $1 from relationship $2 select $3 $4 where $5 dump $6 ";
            
            String relSelect = "rel";
            String idSelect = "id";
            String strwhere = "to.id=="+strObjId;            
            String strRelId = MqlUtil.mqlCommand(context, strMqlCmd, true,strObjId1,strRel,relSelect,idSelect,strwhere,ConfigurationConstants.DELIMITER_PIPE);
		    StringTokenizer strTokens = new StringTokenizer(strRelId, "|");
		    while (strTokens.hasMoreTokens()) {
		        strRelId = strTokens.nextToken();
		    }
		    DomainRelationship domRel = new DomainRelationship(strRelId);
		
		    String strAttrib = (String) domRel.getAttributeValue(context,
		            ConfigurationConstants.ATTRIBUTE_MANDATORYRULE);
		    if (strAttrib == null) {
		        strAttrib = "";
		    }
		    if(strAttrib.equalsIgnoreCase("Yes")){
		    	strMandVal = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.attribute.Mandatory.Range.Yes",strLanguage);
		    }else{
		    	strMandVal = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxProduct.attribute.Mandatory.Range.No",strLanguage);
		    }
		    vecAttrVals.add(strMandVal);
		}
		return vecAttrVals;
 }
    
  
  
  /**
   * Method call to get the "Inherited From" in Rule list Page.
   * @param context  the eMatrix <code>Context</code> object
   * @param args -
   * @return Object - StringList containing the Inherited Values
   * @throws Exception
   *             if the operation fails
	 * @author IXH
	 * @since R212
	 * @category 
   */
  
  public StringList getInheritedFromColumnInTable(Context context, String[] args)
	  throws Exception {
	
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	MapList objList = (MapList) programMap.get("objectList");
	StringList result = new StringList();
	Map paramList = (HashMap) programMap.get("paramList");
	String suiteDir = (String) paramList.get("SuiteDirectory");
	String suiteKey = (String) paramList.get("suiteKey");
	String relInheritedFrom = (String) PropertyUtil
	      .getSchemaProperty(context,"relationship_InheritedFrom");
	Iterator i = objList.iterator();
	String exportFormat = null;
	boolean exportToExcel = false;
	if(paramList!=null && paramList.containsKey("reportFormat")){
		exportFormat = (String)paramList.get("reportFormat");
	}
	if("CSV".equals(exportFormat)){
		exportToExcel = true;
	}
	
	while (i.hasNext()) {
	  Map m = (Map) i.next();
	  String relId = (String) m
	          .get(DomainConstants.SELECT_RELATIONSHIP_ID);
	  String relType = (String) m.get("relationship");
	  String itrRelId = relId;
	  String tomidId = "";
	  int iCnt = 0;
	
	  while (true) {
		  //TO DO MQL Command
	      iCnt = iCnt + 1;
	     /* String strMqlCmd1 = "print connection \"" + itrRelId
	              + "\" select tomid[" + relInheritedFrom
	              + "].id dump | ";
	      tomidId = MqlUtil.mqlCommand(context, strMqlCmd1, true);*/
	      String strMqlCmd1 = "print connection $1 select $2 dump $3 ";
	      StringBuffer selectable = new StringBuffer();
	      selectable.append("tomid[")
	      .append(relInheritedFrom).append("].id");
	      tomidId = MqlUtil.mqlCommand(context, strMqlCmd1, true,itrRelId,selectable.toString(),ConfigurationConstants.DELIMITER_PIPE);
	      if (null == tomidId || "null".equals(tomidId)
	              || "".equals(tomidId)) {
	          break;
	      }
	      else {
	          /*String strMqlCmd2 = "print connection \"" + tomidId
	                  + "\" select fromrel[" + relType + "].id dump";
	          itrRelId = MqlUtil.mqlCommand(context, strMqlCmd2, true);*/
	    	  String strMqlCmd2 = "print connection $1 select $2 dump";
	    	  StringBuffer selectable2 = new StringBuffer();
	    	  selectable2.append("fromrel[")
	    	  			.append(relType).append("].id");
	    	  itrRelId = MqlUtil.mqlCommand(context, strMqlCmd2, true,tomidId,selectable2.toString());
	      }
	  }
	
	  if (iCnt == 1) {
	      result.add("");
	  }
	  else {
	      StringList sl = new StringList(2);
	      sl.add(DomainRelationship.SELECT_FROM_ID);
	      sl.add(DomainRelationship.SELECT_FROM_NAME);
	      String[] arr = new String[1];
	      arr[0] = itrRelId;
	      MapList parentMapList = DomainRelationship.getInfo(context, arr, sl);
	      Map parentInfoMap = (Map) parentMapList.get(0);
	      String strParentName = (String) parentInfoMap.get(DomainRelationship.SELECT_FROM_NAME);
	      String strParentId = (String) parentInfoMap.get(DomainRelationship.SELECT_FROM_ID);
	      if(exportToExcel){
	    	  result.add(strParentName);
	      }
	      else{	
	      StringBuffer output = new StringBuffer();
	      output .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
	      output.append(XSSUtil.encodeForHTMLAttribute(context,suiteDir));
	      output.append("&amp;suiteKey=");
	      output.append(XSSUtil.encodeForHTMLAttribute(context,suiteKey));
	      output.append("&amp;objectId=");
	      output.append(XSSUtil.encodeForHTMLAttribute(context,strParentId));
	      output.append("', '', '', 'false', 'popup', '')\">");
	      output.append(XSSUtil.encodeForHTML(context,strParentName));
	      output.append("</a>");
	      result.add(output.toString());
	      }	
	  }
	}
	
	return result;
}
  
  
  /**
   * Method call to get the "Inherited" in Rule list Page.
   * @param context  the eMatrix <code>Context</code> object
   * @param args -
   * @return Object - StringList containing the Inherited Values
   * @throws Exception
   *             if the operation fails
	 * @author IXH
	 * @since R212
	 * @category 
   */
  public Vector getInheritedColumnInTable(Context context, String[] args)
	  throws Exception {
	
	Vector vecAttrVals = new Vector();
	String strMandVal = "";
	String strLanguage = context.getSession().getLanguage();
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	
	MapList lstObjectIdsList = (MapList) programMap.get("objectList");
	String strObjId = "";
	if (lstObjectIdsList.get(0) instanceof HashMap) {
	  HashMap map = (HashMap) lstObjectIdsList.get(0);
	  strObjId = (String) map.get(DomainConstants.SELECT_ID);
	} else if (lstObjectIdsList.get(0) instanceof Hashtable) {
	  Hashtable htObj = (Hashtable) lstObjectIdsList.get(0);
	  strObjId = (String) htObj.get(DomainConstants.SELECT_ID);
	}
	
	DomainObject dom = new DomainObject(strObjId);
	String strType = (String) dom.getInfo(context,
	      DomainConstants.SELECT_TYPE);
	HashMap paramList = (HashMap) programMap.get("paramList");
	String strObjId1 = (String) paramList.get("objectId");
	String strRel = "";
	if (strType.equals(ConfigurationConstants.TYPE_RULE_EXTENSION)) {
	  strRel = ConfigurationConstants.RELATIONSHIP_RULE_EXTENSION;
	} else if (strType.equals(ConfigurationConstants.TYPE_FIXED_RESOURCE)) {
	  strRel = ConfigurationConstants.RELATIONSHIP_RESOURCE_LIMIT;
	} else if (strType
	      .equals(ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE)) {
	  strRel = ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE;
	} else if (strType
	      .equals(ConfigurationConstants.TYPE_MARKETING_PREFERENCE)) {
	  strRel = ConfigurationConstants.RELATIONSHIP_MARKETING_PREFERENCE;
	}
	
	String strMqlCmd = "";
	for (int i = 0; i < lstObjectIdsList.size(); i++) {
	
	  if (lstObjectIdsList.get(i) instanceof HashMap) {
	      HashMap map = (HashMap) lstObjectIdsList.get(i);
	      strObjId = (String) map.get(DomainConstants.SELECT_ID);
	  } else if (lstObjectIdsList.get(i) instanceof Hashtable) {
	      Hashtable htObj = (Hashtable) lstObjectIdsList.get(i);
	      strObjId = (String) htObj.get(DomainConstants.SELECT_ID);
	  }
	
	  //TO DO
	 /* strMqlCmd = "expand bus \"" + strObjId + "\" to relationship \""
	          + strRel + "\" select rel id where from.id=='" + strObjId1
	          + "' dump | ";
	
	  String strRelId = MqlUtil.mqlCommand(context, strMqlCmd, true);*/
	  strMqlCmd = "expand bus $1 to relationship $2 select $3 $4 where $5 dump $6 ";
	  String relSelect = "rel";
	  String idSelect = "id";
	  String strWhere = "from.id=='" + strObjId1+ "'";
	  String strRelId = MqlUtil.mqlCommand(context, strMqlCmd, true,strObjId,strRel,relSelect,idSelect,strWhere,ConfigurationConstants.DELIMITER_PIPE);
	  StringTokenizer strTokens = new StringTokenizer(strRelId, "|");
	  while (strTokens.hasMoreTokens()) {
	      strRelId = strTokens.nextToken();
	  }
	  DomainRelationship domRel = new DomainRelationship(strRelId);
	  String strAttrib = (String) domRel.getAttributeValue(context,
	          ConfigurationConstants.ATTRIBUTE_INHERITED);
	  if (strAttrib == null) {
	      strAttrib = "";
	  }
	  
	  if(strAttrib.equalsIgnoreCase("True")){
	  	strMandVal = EnoviaResourceBundle.getProperty(context,SUITE_KEY ,"emxProduct.Range.Inherited.True",strLanguage);
	  }else{
	  	strMandVal = EnoviaResourceBundle.getProperty(context,SUITE_KEY ,"emxProduct.Range.Inherited.False",strLanguage);
	  }
	  vecAttrVals.add(strMandVal);
	}
	
	return vecAttrVals;
}

  


  public List getDesignResponsibilityEditInTable(Context context, String[] args)
          throws Exception {

      // unpack the arguments
	Map programMap = (HashMap) JPO.unpackArgs(args);
	Map paramList = (HashMap) programMap.get("paramList");
	
	String strFromWhere = (String) paramList.get("fromWhere");
	
	List lstobjectList = (MapList) programMap.get(STR_OBJECT_LIST);
	Iterator objectListItr = lstobjectList.iterator();

	//initialise the local variables
	Map objectMap = new HashMap();
	String strObjectId = DomainConstants.EMPTY_STRING;
	List lstNameRev = new StringList();
	String strFieldName = "DesignResponsibility";
	String strFieldDisplayName = "DesignResponsibility";
      int count = 0;

      while (objectListItr.hasNext()) {
          strFieldName = "DesignResponsibility" + count;
	 strFieldDisplayName = "DesignResponsibility" + count + "Display";
          count++;
          objectMap = (Map) objectListItr.next();
          strObjectId = (String) objectMap.get(DomainConstants.SELECT_ID);
          StringBuffer sbRDONameSelect = new StringBuffer("to[");
          sbRDONameSelect.append(ConfigurationConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
          sbRDONameSelect.append("].from.");
          sbRDONameSelect.append(DomainConstants.SELECT_NAME);

          StringBuffer sbRDOIdSelect = new StringBuffer("to[");
          sbRDOIdSelect.append(ConfigurationConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
          sbRDOIdSelect.append("].from.");
          sbRDOIdSelect.append(DomainConstants.SELECT_ID);

          StringBuffer sbRDOTypeSelect = new StringBuffer("to[");
          sbRDOTypeSelect.append(ConfigurationConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
          sbRDOTypeSelect.append("].from.");
          sbRDOTypeSelect.append(DomainConstants.SELECT_TYPE);

          StringList lstObjSelects = new StringList();
          lstObjSelects.add(sbRDONameSelect.toString());
          lstObjSelects.add(sbRDOIdSelect.toString());
          lstObjSelects.add(sbRDOTypeSelect.toString());

          String strRDOId = "";
          String strRDOName = "";
          StringBuffer sbHref = new StringBuffer();
          StringBuffer sbBuffer = new StringBuffer();
          String strTempIcon = DomainConstants.EMPTY_STRING;
          String strRDOType = DomainConstants.EMPTY_STRING;
          String strTypeIcon = DomainConstants.EMPTY_STRING;

          // Get the RDO id and name by changing the context to super user
          DomainObject domObj = DomainObject.newInstance(context, strObjectId);
          ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

          Map mapRDO = (Map) domObj.getInfo(context, lstObjSelects);
          ContextUtil.popContext(context);

	 /* If RDO is set for this object then check whether the context user
	 	has read
	 	access on the RDO object. If yes then hyperlink the RDO name to
	 	its
	 	properties page otherwise return the RDO name.
	 	If the mode is edit , display the design responsibility field as
	  	textbox with a chooser*/
	
	     if(strFromWhere!=null && strFromWhere.equalsIgnoreCase("EditTable")){
	       if (mapRDO != null && mapRDO.size() > 0){
	    		 strRDOName = (String) mapRDO.get(sbRDONameSelect.toString());
    	         if(mapRDO.get(sbRDOIdSelect.toString()) instanceof StringList){
	             StringList strRDOListId = (StringList) mapRDO.get(sbRDOIdSelect.toString());
	             strRDOId = (String) strRDOListId.get(0);
           }else{
	             strRDOId = (String) mapRDO.get(sbRDOIdSelect.toString());
	       }
	         
           strRDOType = (String) mapRDO.get(sbRDOTypeSelect.toString());
	     }
	
	     if (strRDOName == null || strRDOName.equalsIgnoreCase("null") || strRDOName.equals("")){
	         strRDOName = "";
	         strRDOId = "";
	         strRDOType = "";
	     }
              boolean bHasReadAccess = false;

              String strCtxUser = context.getUser();
              String strOwner = (String) domObj.getInfo(context,DomainConstants.SELECT_OWNER);

              boolean hasRoleProductManager = false;
              boolean hasRoleSystemEngineer = false;
              boolean bIsOwner = false;

              Person ctxPerson = new Person(strCtxUser);

              hasRoleProductManager = ctxPerson.isAssigned(context,"Product Manager");
              hasRoleSystemEngineer = ctxPerson.isAssigned(context,"System Engineer");

              if (strCtxUser != null && !"".equals(strCtxUser)) {
                  if (strOwner != null && !"".equals(strOwner)) {
                      if (strOwner.equals(strCtxUser)) {
                          bIsOwner = true;
                      }
                  }
              }

	     try{
	       if ((strRDOId == null || strRDOId.equals(""))
	            && (bIsOwner || hasRoleProductManager || hasRoleSystemEngineer)) {
	             bHasReadAccess = true;
	       }else {
	           boolean hasAccessOnProject = emxProduct_mxJPO.hasAccessOnProject(context, strRDOId);
	
	           if (hasAccessOnProject && (bIsOwner || hasRoleProductManager || hasRoleSystemEngineer)){
	                 bHasReadAccess = true;
	           }
	       }
	     }catch (Exception e){
	         bHasReadAccess = false;
	     }
	
	    if (!bHasReadAccess) {
	      if(strRDOId != null && !strRDOId.equals("")&& !strRDOId.equalsIgnoreCase("null")) {
	        strTempIcon = FrameworkUtil.getAliasForAdmin(context,"type", strRDOType, true);
	      }
	
	      if(strTempIcon != null && !strTempIcon.equals("")&& !strTempIcon.equalsIgnoreCase("null")) {
	        strTypeIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon."+ strTempIcon);
	             strTypeIcon = "images/" + strTypeIcon;
                  }
                  sbBuffer.delete(0, sbBuffer.length());
                  sbBuffer.append("<img border=\"0\" src=\"");
                  sbBuffer.append(strTypeIcon);
                  sbBuffer.append("\"</img>");
                  sbBuffer.append(SYMB_SPACE);
                  sbBuffer.append(strRDOName);

              } else {
                  sbBuffer.append("<input type=\"text\" READONLY ");
                  sbBuffer.append("name=\"");
                  sbBuffer.append(strFieldName);
                  sbBuffer.append("Display\" id=\"\" value=\"");
                  sbBuffer.append(strRDOName);
                  sbBuffer.append("\">");
                  sbBuffer.append("<input type=\"hidden\" name=\"");
                  sbBuffer.append(strFieldName);
                  sbBuffer.append("\" value=\"");
                  sbBuffer.append(strRDOId);
                  sbBuffer.append("\">");
                  sbBuffer.append("<input type=\"hidden\" name=\"");
                  sbBuffer.append(strFieldName);
                  sbBuffer.append("OID\" value=\"");
                  sbBuffer.append(strRDOId);
                  sbBuffer.append("\">");
                  sbBuffer.append("<input ");
                  sbBuffer.append("type=\"button\" name=\"btnDesignResponsibility\"");
                  sbBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
    		      sbBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?suiteKey=Configuration");
			      sbBuffer.append("&amp;field=TYPES=type_Organization,type_ProjectSpace");
			      sbBuffer.append("&amp;table=PLCDesignResponsibilitySearchTable");
			      sbBuffer.append("&amp;Registered Suite=Configuration");
			      sbBuffer.append("&amp;selection=single&amp;showInitialResults=false");
			      sbBuffer.append("&amp;hideHeader=true");
			      sbBuffer.append("&amp;cancelLabel=emxProduct.Button.Cancel");
			      sbBuffer.append("&amp;submitLabel=emxProduct.Button.Select");
			      sbBuffer.append("&amp;submitURL=../configuration/SearchUtil.jsp?mode=Chooser");
			      sbBuffer.append("&amp;chooserType=FormChooser");
			      sbBuffer.append("&amp;fieldNameActual=");
			      sbBuffer.append(strFieldName);
			      sbBuffer.append("&amp;fieldNameDisplay=");
			      sbBuffer.append(strFieldDisplayName);
			      sbBuffer.append("&amp;HelpMarker=emxhelpfullsearch','850','630')\">");
			      sbBuffer.append("&amp;nbsp;&amp;nbsp;");
                  sbBuffer.append("<a href=\"javascript:basicClear('");
                  sbBuffer.append(strFieldName);
                  sbBuffer.append("')\">");
                  String strClear = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Button.Clear",
                          									context.getSession().getLanguage());
                  sbBuffer.append(strClear);
                  sbBuffer.append("</a>");
              }
              sbBuffer.toString();
          } else {

              if (mapRDO != null && mapRDO.size() > 0) {

                  strRDOName = (String) mapRDO.get(sbRDONameSelect.toString());

                  if (mapRDO.get(sbRDOIdSelect.toString()) instanceof StringList) {

                      StringList strRDOListId = (StringList) mapRDO.get(sbRDOIdSelect.toString());
                      strRDOId = (String) strRDOListId.get(0);
	         }else{
	             strRDOId = (String) mapRDO.get(sbRDOIdSelect.toString());
	         }
	         strRDOType = (String) mapRDO.get(sbRDOTypeSelect.toString());
	         if (strRDOId != null && !strRDOId.equals("")
	                 && !strRDOId.equalsIgnoreCase("null")) {
	
	             strTempIcon = FrameworkUtil.getAliasForAdmin(context,
	                     "type", strRDOType, true);
	         }
	     }else{
	         strRDOName = "";
	         strRDOId = "";
	         strRDOType = "";
	     }
	      
	     if (strTempIcon != null && !strTempIcon.equals("")&& !strTempIcon.equalsIgnoreCase("null")) {
	         strTypeIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon."+ strTempIcon);
	         strTypeIcon = "images/" + strTypeIcon;
	     }

	     if (strRDOName != null && !strRDOName.equals("")&& !strRDOName.equalsIgnoreCase("null")) {
	         boolean bHasReadAccess;
	         try {
	             bHasReadAccess = emxProduct_mxJPO.hasAccessOnProject(context, strRDOId);
	         } catch (Exception e) {
	             bHasReadAccess = false;
	         }

                  if (bHasReadAccess) {
                      sbBuffer.append("<p> <A HREF=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                      sbBuffer.append(strRDOId);
                      sbBuffer.append("&amp;mode=replace");
                      sbBuffer.append("&amp;AppendParameters=true");
                      sbBuffer.append("&amp;reloadAfterChange=true");
                      sbBuffer.append("')\" class=\"object\">");
                      sbBuffer.append("<img border=\"0\" src=\"");
                      sbBuffer.append(strTypeIcon);
                      sbBuffer.append("\"</img>");
                      sbBuffer.append(strRDOName);
                      sbBuffer.append("</A></p>");
                      
                      /*sbBuffer.append("&nbsp;");
                      sbBuffer.append("<A HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                      sbBuffer.append(strRDOId);
                      sbBuffer.append("&amp;mode=replace");
                      sbBuffer.append("&amp;AppendParameters=true");
                      sbBuffer.append("&amp;reloadAfterChange=true");
                      sbBuffer.append("')\"class=\"object\">");
                      sbBuffer.append("</A>");*/

                      sbHref.toString();

                  } else {
                      sbBuffer.delete(0, sbBuffer.length());
                      sbBuffer.append("<img border=\"0\" src=\"");
                      sbBuffer.append(strTypeIcon);
                      sbBuffer.append("\"</img>");
                      sbBuffer.append(SYMB_SPACE);
                      sbBuffer.append(strRDOName);

                      sbBuffer.toString();
                  }
              }
          }

          if (sbBuffer.length() != 0) {

              lstNameRev.add(sbBuffer.toString());
          }
      }
      return lstNameRev;
  }
  /**
   *  update Mandatory rule attribute on newly relationship, called from create Rule extension,Resource Rule
   * @param context
   * @param args
   * @throws FrameworkException
   */
  @com.matrixone.apps.framework.ui.PostProcessCallable
  public void updateMandatoryAttr(Context context, String[] args) throws FrameworkException
  {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			Map paramMap = (Map) programMap.get("paramMap");
			//IR-151491V6R2013
			String mandRuleAttibute = (String) requestMap
					.get(ConfigurationConstants.MANDATORY_RULE_ATTRIBUTE_NAME);
			String contextOID=  (String) requestMap.get("parentOID");
			String relationshipType=  (String) requestMap.get("relationship");
			String relationship="";
			if (relationshipType.equalsIgnoreCase("relationship_ResourceLimit")) {
				relationship = ConfigurationConstants.RELATIONSHIP_RESOURCE_LIMIT;
			}else if(relationshipType.equalsIgnoreCase("relationship_RuleExtension")){
				relationship = ConfigurationConstants.RELATIONSHIP_RULE_EXTENSION;
			}
			String resourceLimitRel = (String) paramMap.get("relId");
			String ruleID = (String) paramMap.get("objectId");
			DomainRelationship domresoLimitRel = new DomainRelationship(
					resourceLimitRel);
			domresoLimitRel.setAttributeValue(context,
					ConfigurationConstants.ATTRIBUTE_MANDATORYRULE,
					mandRuleAttibute);
			//Rule Inheritance on MPR Create
			String[] arrJPOArgs = new String[4];
			arrJPOArgs[0] = contextOID; // the Context ID from which the Rule is created
			arrJPOArgs[1] = ruleID; // Newly created Rule ID
			arrJPOArgs[2] = relationship; // Relationship
			arrJPOArgs[3] = mandRuleAttibute; // Mandatory attribute value
			
			// call the common Inheritance Rule
			ConfigurableRulesUtil.commonRuleInheritance(context,arrJPOArgs);

		} catch (Exception e) {
			throw new FrameworkException(e.toString());
		}
	}
  /**
   * Empty Update program which will be used in Copy functionality for Rule extension and FR
   * @param context
   * @param args
   * @throws Exception
   */
  public void emptyUpdateProgramForRule(Context context, String[] args)
			throws Exception {
	  //DO Nothing
	}
  /**
   * Empty Update program which will be used in Copy functionality for Rule extension and FR
   * @param context
   * @param args
   * @throws Exception
   */  
  public String getDefaultValueForMandatoryRuleAttribute(Context context,
          String[] args) throws Exception {
//      String strAttributeName = ConfigurationConstants.RANGE_VALUE_NO;
//      matrix.db.AttributeType attribName = new matrix.db.AttributeType(
//              strAttributeName);
//      attribName.open(context);
//
//      String  attributeDefault = attribName.getDefaultValue();
      return ConfigurationConstants.RANGE_VALUE_NO;// added for IR-373345-3DEXPERIENCER2016x- mandatory should be No default
  }
 /**
   * 
   * @param context
   * @param args
 * @throws Exception 
   */
  public void disconnectInheritedFromRuleIds(Context context, String[] args) throws Exception
  {
	  StringList inheritedFromRelId = new StringList();

	  try {
		  StringList programArray = new StringList();
		  for (int i=0; i<args.length; i++)
		  {
			  if (!(null == args[i] || "null".equals(args[i])|| "".equals(args[i]))) 
			  {
				  programArray.add(args[i]); 
			  }
		  }
		  MapList mapRelIds = null;
		  String strComma = ",";
		  String strRelPattern =  ConfigurationConstants.RELATIONSHIP_RULE_EXTENSION + strComma +
		  						  ConfigurationConstants.RELATIONSHIP_RESOURCE_LIMIT + strComma +
		  						  ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE + strComma+
		  						  ConfigurationConstants.RELATIONSHIP_MARKETING_PREFERENCE ;
		  String slObjSelects = DomainConstants.SELECT_ID;
		  String slRelSelects =  DomainConstants.SELECT_RELATIONSHIP_ID;
		  String relInheritedFrom = (String) PropertyUtil.getSchemaProperty(context,"relationship_InheritedFrom");

		  String tomidId = "";
		  Map relationShipMap = null;
		  String[] relIdsToDisconnect = null ;
		  Model configurationModel = new Model();

		  for (int iCnt = 0 ; iCnt<programArray.size() ; iCnt++)
		  {	
			  String ObjectId = (String) programArray.get(iCnt);
			  DomainObject contextObjId = new DomainObject(ObjectId); 
			  ConfigurationUtil confUtil = new ConfigurationUtil(contextObjId);
			  mapRelIds = confUtil.getObjectStructure(context,
					  DomainConstants.QUERY_WILDCARD,	 // Type Pattern
					  strRelPattern,  				 // Relationship patern	
					  new StringList(slObjSelects),  	 // Object selectables
					  new StringList(slRelSelects),    // Relationship selectable
					  false,					         // get To
					  true, 		                     // get From
					  1,			                     // level
					  0,		  	                     // limit
					  null,                            // where object clause
					  null, 	                         // where relationship clause  
					  (short)0,                        // filter flag
					  null);                           // effectivity expression
			  Iterator relIdsItr = mapRelIds.iterator();

			  while(relIdsItr.hasNext()) {
				  relationShipMap = (Map)relIdsItr.next();            

				  String relationshipId  = (String) relationShipMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
				  DomainRelationship domRel = new DomainRelationship(relationshipId);
				  domRel.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_INHERITED, "False");
				  /*String strMqlCmd1 = "print connection \"" + relationshipId
				  + "\" select tomid[" + relInheritedFrom
				  + "].id dump | ";
				  tomidId = MqlUtil.mqlCommand(context, strMqlCmd1, true);*/
				  String strMqlCmd1 = "print connection $1 select $2 dump $3";
				  StringBuffer selectable = new StringBuffer();
				  selectable.append("tomid[")
				  .append(relInheritedFrom).append("].id");
				  tomidId = MqlUtil.mqlCommand(context, strMqlCmd1, true,relationshipId,selectable.toString(),ConfigurationConstants.DELIMITER_PIPE);
				  if (!(null == tomidId || "null".equals(tomidId)|| "".equals(tomidId))) {
					  inheritedFromRelId.add(tomidId);
				  }
			  }

		  }
		  if(inheritedFromRelId.size()>0)
		  {	
			  relIdsToDisconnect = new String[inheritedFromRelId.size()];
			  for (int iCnt = 0 ; iCnt < inheritedFromRelId.size(); iCnt++)
			  {
				  relIdsToDisconnect[iCnt] = (String) inheritedFromRelId.get(iCnt);
			  }
		  }
		  configurationModel.disconnectObjects(context, relIdsToDisconnect);

	  } catch (Exception e) {		
		  e.printStackTrace();
		  throw new Exception(e);
	  }  

  }

  
  /**
	 * This method is called on create Left/Right Expression relationship as
	 * action trigger,this will check Varies By Relationship and Configuration
	 * Type attribute on Varies By Relationship.
	 * 
	 * @param context
	 *            The eMatrix Context object
	 * @param args
	 *            This will be the Macro {FROMOBJECTID} {FROMTYPE} {TOOBJECTID}
	 *            for Create Action Trigger for Left/Right Expression.
	 * @throws Exception
	 * @since R216
	 */
	public void updateConfigurationTypeAttributeForVBRel(Context context,
			String args[]) throws Exception {
		try {
			String ToObjectId = args[2];

			// 1.Check If TO side is Relationship Object
			if (ProductLineCommon.isNotNull(ToObjectId)
					&& !new DomainObject(ToObjectId).exists(context)) {
				DomainRelationship domRelId = new DomainRelationship(ToObjectId);
				StringList relSelects = new StringList();
				String[] relationshipIds = { ToObjectId };

				relSelects.addElement(DomainRelationship.SELECT_TYPE);
				relSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_TYPE);

				MapList domRelMapList = domRelId.getInfo(context,
						relationshipIds, relSelects);
				Map domRelMap = (Map) domRelMapList.get(0);

				// Check If Type and Relationship Attribute is present in Map
				if (domRelMap.containsKey(DomainRelationship.SELECT_TYPE)
						&& domRelMap
								.containsKey(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_TYPE)) {
					// 2.Check If Relationship is Varies By And Check If
					// Configuration Type Attribute On Varies By Relationship Is
					// System
					if (ConfigurationUtil.isKindOfRel(context,
							(String) domRelMap
									.get(DomainRelationship.SELECT_TYPE),
							ConfigurationConstants.RELATIONSHIP_VARIES_BY)
							&& ((String) domRelMap
									.get(ConfigurationConstants.SELECT_ATTRIBUTE_CONFIGURATION_TYPE))
									.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_SYSTEM)) {
						// If Configuration Type Attribute On Varies By
						// Relationship Is "System" Then Change It To
						// "Configuration"
						domRelId.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE,ConfigurationConstants.RANGE_VALUE_CONFIGURATION);
					}
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
	}
}
