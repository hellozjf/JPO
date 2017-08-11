/*
 ** ${CLASSNAME}
 **
 **Copyright (c) 1993-2016 Dassault Systemes.
 ** All Rights Reserved.getActiveManufacturingPlans
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.configuration.ManufacturingFeature;
import com.matrixone.apps.configuration.Model;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.jdom.Element;

/**
 * This JPO class has some methods pertaining to Logical Feature.
 *
 * @author IVU
 * @since FTR R211
 */
public class ManufacturingFeatureBase_mxJPO extends emxDomainObject_mxJPO {

	public static final String SYMB_NOT_EQUAL = " != ";
    /** A string constant with the value "'". */
    public static final String SYMB_QUOTE = "'";
    public static final String strSymbReleaseState = "state_Release";
    public static final String SUITE_KEY = "Configuration";
	
    List relIdList = new StringList();


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
	 * @author IVU
	 * @since FTR R211
	 */
	public ManufacturingFeatureBase_mxJPO(Context context, String[] args)
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
	 * @author IVU
	 * @since FTR R211
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		if (!context.isConnected())
			throw new Exception("Not supported on desktop client");
		return 0;
	}


	/**
	 * This Methods is used to get the Logical Feature Structure.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the Logical Feature Structure Details
	 * @throws Exception
	 *             if the operation fails
	 * @author IVU
	 * @since FTR R211
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getManufacturingFeatureStructure(Context context, String[] args)
	throws Exception {

		MapList mapManufacturingStructure =null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String strObjWhere = DomainObject.EMPTY_STRING;

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//Gets the objectids and the relation names from args
			String strObjectid = (String)programMap.get("objectId");
			String sNameFilterValue = (String) programMap.get("FTRManufacturingFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRManufacturingFeatureLimitFilterCommand");

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

			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {

				strObjWhere = "attribute["
					+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
					+ "] ~~ '" + sNameFilterValue + "'";
			}

			// call method to get the level details
			int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

			// @To DO
			// need to revisit the selectables
			ManufacturingFeature cfBean = new ManufacturingFeature(strObjectid);
			StringList relSelect = new StringList();
			relSelect.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.id");
			relSelect.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.type");
			relSelect.add("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.attribute["
					+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
					+ "]");


			relSelect.add("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.attribute["
					+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
					+ "]");
			relSelect.addElement(SELECT_FROM_NAME);

			StringList objSelect = new StringList();
			objSelect.addElement("attribute["
					+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME
					+ "]");
			objSelect.addElement(DomainConstants.SELECT_REVISION);
			objSelect.addElement("attribute["
					+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME
					+ "]");
			objSelect.addElement("physicalid");
			
			mapManufacturingStructure = (MapList)cfBean.getManufacturingFeatureStructure(context,"", null, objSelect, relSelect, false,
					true,iLevel,limit, strObjWhere, DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,filterExpression);
			
			for (int i = 0; i < mapManufacturingStructure.size(); i++) {
				Map tempMAp = (Map) mapManufacturingStructure.get(i);
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
			
			for (int i = 0; i < mapManufacturingStructure.size(); i++) {
				Map tempMAp = (Map) mapManufacturingStructure.get(i);
				if(tempMAp.containsKey("expandMultiLevelsJPO")){
					mapManufacturingStructure.remove(i);
					i--;
				}
			}

			if (mapManufacturingStructure != null) {
				HashMap hmTemp = new HashMap();
				hmTemp.put("expandMultiLevelsJPO", "true");
				mapManufacturingStructure.add(hmTemp);
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mapManufacturingStructure;
	}

	/**
	 * This Methods is used to get the  top level Logical Features.
	 * This is used while launching the Logical Features from My Desk
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Top Level Logical Features
	 * @throws Exception
	 *             if the operation fails
	 * @author IVU
	 * @since FTR R211
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTopLevelManufacturingFeatures(Context context, String[] args)
	throws Exception {

		MapList mapTop = null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String queryName = DomainObject.EMPTY_STRING;

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String sNameFilterValue = (String) programMap.get("FTRManufacturingFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRManufacturingFeatureLimitFilterCommand");

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
			strWhere.append("(to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"]=='False'");
			strWhere.append(" || ");
			strWhere.append("to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].from.type.kindof["+
					ConfigurationConstants.TYPE_MANUFACTURING_FEATURE+"]!=TRUE)");

			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {
				strWhere.append(" && ");
				strWhere.append("attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+ "] ~~ '"+sNameFilterValue+ "'");
			}

			mapTop = DomainObject.findObjects(context,
					ConfigurationConstants.TYPE_MANUFACTURING_FEATURE,"*", "*","*","*",
					strWhere.toString(),queryName,true,ConfigurationUtil.getBasicObjectSelects(context), (short)limit);

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return mapTop;
	}

	/**
	 * This Methods is used to get the  top level Logical Features Owned by the user.
	 * This is used while launching the Logical Features from My Desk
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Top Level Logical Features
	 * @throws Exception
	 *             if the operation fails 
	 * @since FTR R213
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTopLevelOwnedManufacturingFeatures(Context context, String[] args)
	throws Exception {

		MapList mapTop = null;
		int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
		String queryName = DomainObject.EMPTY_STRING;

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String sNameFilterValue = (String) programMap.get("FTRManufacturingFeatureNameFilterCommand");
			String sLimitFilterValue = (String) programMap.get("FTRManufacturingFeatureLimitFilterCommand");

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
			strWhere.append("(to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"]=='False'");
			strWhere.append(" || ");
			strWhere.append("to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].from.type.kindof["+
					ConfigurationConstants.TYPE_MANUFACTURING_FEATURE+"]!=TRUE)");

			if (sNameFilterValue != null
					&& !(sNameFilterValue.equalsIgnoreCase("*"))) {
				strWhere.append(" && ");
				strWhere.append("attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME+ "] ~~ '"+sNameFilterValue+ "'");
			}

			mapTop = DomainObject.findObjects(context,
					ConfigurationConstants.TYPE_MANUFACTURING_FEATURE,"*", "*",context.getUser(),"*",
					strWhere.toString(),queryName,true,ConfigurationUtil.getBasicObjectSelects(context), (short)limit);

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return mapTop;
	}

	/**
	 * This is wrapper method to load the Candidate Manufacturing Features of the Context Model
	 *
	 * @param context -  the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the Map containing the Context Model Id.
	 * @return mapManufacturingStructure- MapList Containing the Maps of Manufacturing Features. Each Map will consist of:
	 * 					 id= Manufacturing Feature ID,
	 * 					 type = Manufacturing Feature or Sub-types of Manufacturing Features
	 * 					 name = name of logical feature
	 * 					 revision = revision of  Manufacturing Feature
	 * 					 name[connection]= name of the relationship
	 * 					 id[connection] = relationship id
	 * @throws Exception
	 * @author A69
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCandidateManufacturingFeatures(Context context, String[] args)
	throws Exception {

		MapList mapManufacturingStructure =null;
		StringList slObjSelects = new StringList("physicalid");

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);

			String strObjectid = (String)programMap.get("objectId");
			String parentOID = (String)programMap.get("parentOID");
			if(strObjectid.equals(parentOID)){
				// call method to get the level details
				int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
				String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

				Model configModel = new Model(strObjectid);
				mapManufacturingStructure = configModel.getCandidateManufacturingFeatures(context, DomainObject.EMPTY_STRING,
						DomainObject.EMPTY_STRING, slObjSelects, null, false,	true, iLevel, 0, DomainObject.EMPTY_STRING,
						DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM, filterExpression);
			}
			else{
				mapManufacturingStructure = getManufacturingFeatureStructure(context, args);
			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mapManufacturingStructure;
	}


	/**
	 * This is wrapper method to load the Committed Manufacturing Features of the Context Model
	 *
	 * @param context -  the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the Map containing the Context Model Id.
	 * @return mapManufacturingStructure- MapList Containing the Maps of Manufacturing Features. Each Map will consist of:
	 * 					 id= Manufacturing Feature ID,
	 * 					 type = Manufacturing Feature or Sub-types of Manufacturing Features
	 * 					 name = name of logical feature
	 * 					 revision = revision of  Manufacturing Feature
	 * 					 name[connection]= name of the relationship
	 * 					 id[connection] = relationship id
	 * @throws Exception
	 * @author A69
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCommittedManufacturingFeatures(Context context, String[] args)throws Exception {

		MapList mapManufacturingStructure =null;
		StringList slObjSelects = new StringList("physicalid");
		StringList slRelSelects = new StringList("frommid["+ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT+"].to.id");

		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectid = (String)programMap.get("objectId");

			// call method to get the level details
			int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
			String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");

			Model configModel = new Model(strObjectid);
			mapManufacturingStructure = configModel.getCommittedManufacturingFeatures(context, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, slObjSelects, slRelSelects, false,	true, iLevel, 0, DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM, filterExpression);

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mapManufacturingStructure;
	}

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeAvailableManufacturingFeature(Context context, String [] args)
	   throws Exception
	   {
	       Map programMap = (Map) JPO.unpackArgs(args);
	       String strSourceObjectId = (String) programMap.get("objectId");
	       DomainObject domContextObj = new DomainObject(strSourceObjectId);
	       String txtType = domContextObj.getInfo(context,DomainConstants.SELECT_TYPE);
	       String strWhereExp = DomainConstants.EMPTY_STRING;
	       String strObjectPattern = ConfigurationConstants.TYPE_LOGICAL_STRUCTURES;

	       String strRelPattern = ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES;

	       if(txtType.equalsIgnoreCase(ProductLineConstants.TYPE_MODEL)){
	    	   strRelPattern += "," + ConfigurationConstants.RELATIONSHIP_CANDIDTAE_MANUFACTURING_FEATURES+","+
										ConfigurationConstants.RELATIONSHIP_COMMITTED_MANUFACTURING_FEATURES;
	       }

	       StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
	       short level = 1;
	       short limit = 0;
	       MapList relatedFromFeatureList = new MapList();
	       
	       relatedFromFeatureList = domContextObj.getRelatedObjects(context,
													               strRelPattern,
													               strObjectPattern,
													               objectSelects,
													               null,
													               false,
													               true,
													               level,
													               strWhereExp,
													               strWhereExp,
													               limit);

	       HashSet featureList = new HashSet();
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

	       ArrayList featureListArray = new ArrayList(featureList);

	       return new StringList(featureListArray);
	   }


	/**
	 * This is a refresh JPO method to refresh the Candidate Configuration Features page once the Commit of the Logical Features is done
	 *
	 * @param context - Matrix context
	 * @param args - contains the Program Map.
	 * @return returnMap - Map consisting of the Javascript code to refresh the Page.
	 * @throws Exception
	 * @author A69
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map refreshCandidateFeaturePageOnApply(Context context, String[] args) throws Exception {

		Map returnMap = new HashMap();

		returnMap.put ("Action", "execScript");
		returnMap.put("Message", "{ main:function()  {getTopWindow().close();var listFrame = findFrame(getTopWindow().opener.getTopWindow(),'detailsDisplay');listFrame.location.href=listFrame.location.href;}}");
		return returnMap;
	}

	/**
	 * This is a public method used to commit the Candidate Logical Feature to selected Product Revisions of a Context Model
	 *
	 * @param context - Matrix context
	 * @param args - contains the Maps with Context Model Id, Selected product revisions and the Candidate Logical Features to commit to.
	 * @throws Exception
	 * @author A69
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public void commitCandidateFeatureToProduct (Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strProductObjId= (String)programMap.get("parentOID");
		String strModelId  = (String)paramMap.get("objectId");

		Element elem = (Element) programMap.get("contextData");
		MapList chgRowsMapList = com.matrixone.apps.framework.ui.UITableIndented.getChangedRowsMapFromElement(context, elem);

		StringList strLogicalFeatureIds  = new StringList(chgRowsMapList.size());

		for (int i = 0; i < chgRowsMapList.size(); i++) {
			HashMap tempMap = (HashMap) chgRowsMapList.get(i);
			strLogicalFeatureIds.addElement((String) tempMap.get("childObjectId"));
		}
		StringList strProductRevisionIds = new StringList(strProductObjId);

		// call the Model API to commit the selected Logical features to the selected Product Revisions.
		Model confModel = new Model(strModelId);
		confModel.commitCandidateManufacturingFeatures(context, strLogicalFeatureIds, strProductRevisionIds);
	}


	/**
	 * connects the newly created Manufacturing feature to the Context Object with approriate relationship
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since FTR R212
	 */
	public void connectManufacturingFeatureToContext(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap   = (HashMap)programMap.get("paramMap");
		HashMap requestMap   = (HashMap)programMap.get("requestMap");

		String[] strarrayparentOID = (String[])requestMap.get("parentOID");
		if(strarrayparentOID != null){
			String strParentID = strarrayparentOID[0];
			DomainObject parentObj = new DomainObject(strParentID);

			String strParentType = parentObj.getInfo(context, DomainConstants.SELECT_TYPE);
			String newObjID = (String)paramMap.get("objectId");

			if(strParentType != null && !"".equals(strParentType) && mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_MODEL)){
				Model confModel = new Model(strParentID);
				confModel.connectCandidateManufacturingFeature(context, newObjID);
			}
			else if(strParentType != null && !"".equals(strParentType) && mxType.isOfParentType(context,strParentType,ConfigurationConstants.TYPE_GENERAL_CLASS)){

				DomainRelationship.connect(context, strParentID, ConfigurationConstants.RELATIONSHIP_CLASSIFIED_ITEM, newObjID,false);
			}
		}
	}
	/**
	 * This is a dummy method to provide the feasibility of adding configuration feature markup under a product to commit, without showing already committed features
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the Logical Feature Structure Details
	 * @throws Exception
	 *             if the operation fails
	 * @author IVU
	 * @since FTR R211
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTempCommittedManufacturingFeatures(Context context, String[] args)
	throws Exception {

		return new MapList();
	}

	/**
	 * This is a Access Function to show the Committed To column in the Committed Manufacturing Features view in the Model Context.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args Holds ParamMap having the key "fromContext" which will determine if the columns need to be displayed or not
	 * @return bResult - true - if the context is Committed Features
	 * 					 false - if the Context is not Committed Features
	 * @throws Exception  - throws Exception if anyoperation fails.
	 * @author IVU
	 * @since R212
	 */

	public boolean isCommittedToAcceessible (Context context, String args[]) throws Exception{
		boolean bResult = false;

        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String strFromContext = (String)paramMap.get("fromContext");
        if(strFromContext!=null && !strFromContext.equals("")){
        	if(strFromContext.equalsIgnoreCase("Committed")){
        		bResult = true;
        	}
        }
		return bResult;
	}

	/**
	 * This is a "Committed To" column JPO function to show the Product Revisions to which the Manufacturing Feature is Committed To.
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - contain the ParamMap with the ObjectList of all the Committed Logical Features
	 * @return committedProd - StringList of the Product Revision with Name and Revsisions of the Committed Logical Features.
	 * @throws Exception - throws Exception if anyoperation fails.
	 * @author g98
	 * @since R212
	 */
	public StringList getCommittedToProductList(Context context, String[] args)
    throws Exception
    {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList lstObjectIdsList = (MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String suiteDir = (String) paramMap.get("SuiteDirectory");
        String suiteKey = (String) paramMap.get("suiteKey");
		StringList committedProd = new StringList();

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

					temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
					temp.append(XSSUtil.encodeForHTMLAttribute(context,suiteDir));
					temp.append("&amp;suiteKey=");
					temp.append(XSSUtil.encodeForHTMLAttribute(context,suiteKey));
					temp.append("&amp;objectId=");
					temp.append(XSSUtil.encodeForHTMLAttribute(context,strProductID));
					temp.append("', '450', '300', 'true', 'popup')\">");
					temp.append(" <img border=\"0\" src=\"");
					temp.append("images/iconSmallProduct.gif");
					temp.append("\" /> ");
					temp.append(XSSUtil.encodeForHTML(context,strProductName)+" "+XSSUtil.encodeForHTML(context,strProductRev));
					temp.append("</a>");

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

				temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
				temp.append(XSSUtil.encodeForHTMLAttribute(context,suiteDir));
				temp.append("&amp;suiteKey=");
				temp.append(XSSUtil.encodeForHTMLAttribute(context,suiteKey));
				temp.append("&amp;objectId=");
				temp.append(XSSUtil.encodeForHTMLAttribute(context,strProductID));
				temp.append("', '450', '300', 'true', 'popup')\">");
				temp.append(" <img border=\"0\" src=\"");
				temp.append("images/iconSmallProduct.gif");
				temp.append("\" /> ");
				temp.append(XSSUtil.encodeForHTML(context,strProductName)+" "+XSSUtil.encodeForHTML(context,strProductRev));
				temp.append("</a>");
				committedProd.add(temp.toString());
			}else if (objPRIds==null ){
				committedProd.add("");
			}
        }

		return committedProd;
    }

	/**
	 * This is a Access Function to show or hide the Column LogicalSelectionType
	 *  depends upon the param context=Logical passed in request parameter
	 *
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args Holds ParamMap having the key "context", value of which will determine if the columns need to be displayed or not
	 * @return bResult - true - if the context is Committed Features
	 * 					 false - if the Context is not Committed Features
	 * @throws Exception  - throws Exception if any operation fails.
	 * @author A69
	 * @since R212
	 */
	public Object isLogicalFeatureContext(Context context, String[] args)
    throws Exception
    {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
		String[] ctx = null;
		if(requestValuesMap!=null){
			ctx = (String[])requestValuesMap.get("context");
		}
		else{
			ctx = new String[]{programMap.get("context").toString()};
		}

		if(ctx!= null && ctx[0] != null && ctx[0].equals("LogicalFeature")){
			return Boolean.valueOf(true);
		}else{
			return Boolean.valueOf(false);
		}
    }

    /**
     * This method is used to restrict from displaying the row
     * in Products and Features Property page
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
        boolean isLCInstalled = false;
        boolean flDisplayField = false;
        isLCInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionLibraryCentral", false, null, null);
        if(isLCInstalled)
        {
            flDisplayField = true;
        }
        return flDisplayField;
    }
    /**
     * This is structure function for Manufacturing feature
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @since R212
     */
    public static MapList getStructureList(Context context, String[] args)
    throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String contextObjId = (String) paramMap.get("objectId");
		MapList manFeatureStructList = new MapList();

		String strTypePattern = ConfigurationConstants.TYPE_MANUFACTURING_FEATURE
				+ ","
				+ ConfigurationConstants.TYPE_MANUFACTURING_FEATURE
				+ ","
				+ ConfigurationConstants.TYPE_PRODUCTS;
		String strRelPattern = ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES
				+ ","
				+ ConfigurationConstants.RELATIONSHIP_CANDIDTAE_MANUFACTURING_FEATURES
				+ ","
				+ ConfigurationConstants.RELATIONSHIP_COMMITTED_MANUFACTURING_FEATURES;

		StringList objectSelects = new StringList(4);
		objectSelects.add(DomainConstants.SELECT_ID);
		objectSelects.add(DomainConstants.SELECT_TYPE);
		objectSelects.add(DomainConstants.SELECT_NAME);
		objectSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME);
		ConfigurationUtil confUtil = new ConfigurationUtil(contextObjId);
		manFeatureStructList = confUtil.getObjectStructure(context,
				strTypePattern, strRelPattern, objectSelects, null, false,
				true, (short) 1, 0, "", "", DomainObject.FILTER_STR_AND_ITEM,
				DomainObject.EMPTY_STRING);

		return manFeatureStructList;
  }
    /**
	 * Label program for manufacturing Tree structure
	 *
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
    
	public void setPartFamily(Context context, String[] args) throws Exception
	{
		try {
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");
			String strObjId = (String)paramMap.get("objectId");
			String strNewOId = (String) paramMap.get("New OID");
			if(strNewOId==DomainConstants.EMPTY_STRING){
				 strNewOId = (String) paramMap.get("New Value");
				 }
			if(!strNewOId.equals("")){
				DomainObject dom = new DomainObject(strObjId);
				DomainRelationship.connect(context, dom, ConfigurationConstants.RELATIONSHIP_GBOM, new DomainObject(strNewOId));
			}




		}catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

	}
    /**
     * This method is used to return the status icon of an object
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List- the List of Strings in the form of 'Name Revision'
     * @throws Exception if the operation fails
     * @since R212
     *
    **/

    public List getStatusIcon (Context context, String[] args) throws Exception{
    	//XSSOK
        //unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) programMap.get("objectList");
        Iterator objectListItr = lstobjectList.iterator();
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
        List lstNameRev = new StringList();
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
                StringBuffer sbStateKey = new StringBuffer("emxFramework.State.");
                sbStateKey.append(strObjState);
                strObjState = EnoviaResourceBundle.getProperty(context,"ProductLine", sbStateKey.toString(),context.getSession().getLanguage());
                stbNameRev.delete(0, stbNameRev.length());
                stbNameRev = stbNameRev.append("<img src=\"../common/images/")
                                .append(XSSUtil.encodeForXML(context,strIcon))
                                .append("\" border=\"0\"  align=\"middle\" ")
                                .append("TITLE=\"")
                                .append(" ")
                                .append(XSSUtil.encodeForXML(context,strObjState))
                                .append("\"")
                                .append("/>");
                lstNameRev.add(stbNameRev.toString());
            }
            else
            {
                lstNameRev.add(DomainConstants.EMPTY_STRING);
            }
        }
        return lstNameRev;
    }
	/**
     * This method is used to control the access for Manufacturing Features commands
     * @param context The ematrix context object
     * @param String The args
     * @return Boolean
     * @throws Exception - - when operation fails
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
        
    /**
     * This method is used to return the Name of the part family with Hyper link of Manufacturing Feature Table
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return Vector- the List of Strings containing part families name.
     * @throws Exception
     *             if the operation fails
     * @since R212
     */
    public Vector getPartFamilyLink(Context context, String[] args) throws Exception {
    	HashMap programMap = (HashMap)JPO.unpackArgs(args);
    	MapList objectMapList = (MapList)programMap.get("objectList");
    	HashMap paramMap = (HashMap)programMap.get("paramList");
    	String reportFormat = (String)paramMap.get("reportFormat");
    	String suiteDir = (String) paramMap.get("SuiteDirectory");
    	String suiteKey = (String) paramMap.get("suiteKey");
    	Vector partList = new Vector();
    	String[] objectsarray = new String[objectMapList.size()];
    	for(int i=0;i<objectMapList.size();i++)
    	{
    		Map objectMap = (Map) objectMapList.get(i);
    		String logicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);
    		objectsarray[i]= logicalFeatureID;
    	}
    	StringList objectSelectables = new StringList();
    	String strID = "from["+ConfigurationConstants.RELATIONSHIP_GBOM+"]."+DomainConstants.SELECT_TO_ID;
    	DomainConstants.MULTI_VALUE_LIST.add(strID);
    	objectSelectables.add(strID);
    	MapList logicalFeaturesMapList = DomainObject.getInfo(context, objectsarray, objectSelectables);
    	DomainConstants.MULTI_VALUE_LIST.remove(strID);
    	String strPartFamilyName = "";
    	String strPartFamilyID = "";
    	for(int i=0;i<logicalFeaturesMapList.size();i++)
    	{
    		Map objectMap = (Map) logicalFeaturesMapList.get(i);
    		String output = "";
    		if(objectMap.size()>0){
    			Object objGBOMds = (Object)objectMap.get(strID);

    				StringList strTempList = new StringList();
    				strTempList = (StringList) objGBOMds;
    				String[] strPRIds = new String[strTempList.size()];
    				for(int j=0; j<strTempList.size();j++ ){
    					strPRIds[j] = (String) strTempList.get(j);
    				}
    				MapList mapGBOMDetails = DomainObject.getInfo(context,strPRIds ,ConfigurationUtil.getBasicObjectSelects(context));
    				for(int k=0; k<mapGBOMDetails.size();k++){
    					Map tempMap = (Map)mapGBOMDetails.get(k);
    					strPartFamilyName = tempMap.get(DomainObject.SELECT_NAME).toString();
    					strPartFamilyID =  tempMap.get(DomainObject.SELECT_ID).toString();
    					String objectType =  tempMap.get(DomainObject.SELECT_TYPE).toString();
    					if(objectType.equalsIgnoreCase(ConfigurationConstants.TYPE_PART_FAMILY))
    					{
    						StringBuffer temp = new StringBuffer();
    						if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
    							temp.append(strPartFamilyName);
    						}else{		    						
	    						temp.append(" <img border=\"0\" src=\"");
	    						temp.append("images/iconSmallPartFamily.gif");
	    						temp.append("\" /> ");
	    						temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
	    						temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
	    						temp.append("&amp;suiteKey=");
	    						temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
	    						temp.append("&amp;objectId=");
	    						temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyID));
	    						temp.append("', '450', '300', 'true', 'popup')\">");
	    						temp.append(XSSUtil.encodeForXML(context, strPartFamilyName));
	    						temp.append("</a>");
    						}
    						output = output + "," + temp;
    					}

    				}
    				if(output.length()>0){
    					output = output.replaceFirst(",", "");
    					partList.add(output);
    				}
    				else{
    					partList.add(DomainConstants.EMPTY_STRING);
    				}

    		}

    		else{
    			partList.add(DomainConstants.EMPTY_STRING);
    		}

    	}

    	return  partList;
    }

	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeManufacturingFeature(Context context, String [] args)
	throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		String strSourceObjectId = (String) programMap.get("strProductId");
		String strObjectPattern = ConfigurationConstants.TYPE_LOGICAL_FEATURE+","+ConfigurationConstants.TYPE_PRODUCTS+","+ConfigurationConstants.TYPE_MANUFACTURING_FEATURE;
		String strRelPattern = ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES;
		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		MapList relatedFromFeatureList = new MapList();
		ConfigurationUtil util = new ConfigurationUtil(strSourceObjectId);
		StringList tempStringList = new StringList();
		//Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
		relatedFromFeatureList = util.getObjectStructure(context, strObjectPattern, strRelPattern,
				objectSelects, tempStringList , true, true, 0, 0, DomainConstants.EMPTY_STRING,
				   DomainConstants.EMPTY_STRING, (short)0, DomainConstants.EMPTY_STRING);

		HashSet featureList = new HashSet();
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

		ArrayList featureListArray = new ArrayList(featureList);

		return new StringList(featureListArray);
	}

    /**
     * Update program,which will update the revision on policy Change
     * @param context the eMatrix <code>Context</code> object
     * @param args - Holds parameters passed from the calling method
     * @return - returns the First Sequence revision
     * @throws Exception if the operation fails
     * @since R212
     */
    public HashMap getRevision (Context context,String[] args)throws Exception
    {
		HashMap hmProgramMap = (HashMap) JPO.unpackArgs(args);
		HashMap fieldValues = (HashMap) hmProgramMap.get( "fieldValues" );
		String policy = (String)fieldValues.get("Policy");
		Policy policyObj = new Policy(policy);
		String revision = policyObj.getFirstInSequence(context);

		HashMap returnMap = new HashMap();

	    returnMap.put("SelectedValues", revision);
	    returnMap.put("SelectedDisplayValues", revision);

		return returnMap;
    }

    /**
     * Method returns the First Sequence revision for the policy Manufacturing Feature.
     * It is called for the create page of Manufacturing Feature
     * @param context the eMatrix <code>Context</code> object
     * @param args - Holds parameters passed from the calling method
     * @return - returns the First Sequence revision
     * @throws Exception if the operation fails
     * @since R212
     */
    public String getDefaultRevision (Context context,String[] args)throws Exception
    {
    	//TODO -Policy in combobox is not in order in which getPolicies return
		String strType=ConfigurationConstants.TYPE_MANUFACTURING_FEATURE;
		MapList policyList = com.matrixone.apps.domain.util.mxType.getPolicies(context,strType,false);
		String strDefaultPolicy = (String)((HashMap)policyList.get(0)).get(DomainConstants.SELECT_NAME);

    	Policy policyObject = new Policy(strDefaultPolicy);
    	String strRevision = policyObject.getFirstInSequence(context);
		return strRevision;
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
     * This method is used to return the Name of the part family with Hyper link of Manufacturing Feature Properties Page
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds arguments
     * @return Vector- the List of Strings containing part families name.
     * @throws FrameworkException
     *             if the operation fails
     * @since R212
     * @author g98
     */
	public String getPartFamilyLinkForForm(Context context, String[] args) throws FrameworkException {
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String strMode = (String)requestMap.get("mode");
			String suiteDir = (String) requestMap.get("SuiteDirectory");
			String suiteKey = (String) requestMap.get("suiteKey");
			Map fieldMap = (HashMap) programMap.get("fieldMap");
			String strFieldName = (String)fieldMap.get("name");
			String partList = null;
			String strobjectId = (String)requestMap.get("objectId");
			setId(strobjectId);
			ConfigurationUtil util = new ConfigurationUtil(strobjectId);
			
			//CODE CHANGES
	        String exportFormat = "";
			if(requestMap!=null && requestMap.containsKey("reportFormat")){
	        	exportFormat = (String)requestMap.get("reportFormat");
	        }
			
			//LogicalFeature logicalFeature = new LogicalFeature(strobjectId);
			StringList objectSelectables = new StringList();
			String strID = DomainConstants.SELECT_ID;
			String strName = DomainConstants.SELECT_NAME;
			String strRevision = DomainConstants.SELECT_REVISION;
			objectSelectables.add(strID);
			objectSelectables.add(strName);
			objectSelectables.add(strRevision);
			StringBuffer excludePFOID = new StringBuffer();
			//Type Pattern
			String strTypePattern = ConfigurationConstants.TYPE_PART_FAMILY;
			//Rel PAttern
			String strRelPattern = ConfigurationConstants.RELATIONSHIP_GBOM +"," +ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM;

			StringList tempStringList = new StringList();
			//Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
			MapList manufacturingFeaturePFList = util.getObjectStructure(context,strTypePattern,strRelPattern,
					objectSelectables, tempStringList,false, true, 1,0,
					DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING,(short) 1,DomainConstants.EMPTY_STRING);
			String strPartFamilyName = "";
			String strPartFamilyID = "";

			String output = "";
			boolean editMode=true;
			if(strMode!=null && !strMode.equals("") &&
					!strMode.equalsIgnoreCase("null") && strMode.equalsIgnoreCase("edit"))
			{
				if(editMode){
					if(manufacturingFeaturePFList.size()>0){

							for(int i=0;i<manufacturingFeaturePFList.size();i++)
						{
								Map manufacturingFeaturePFMap = (Map) manufacturingFeaturePFList.get(i);
								strPartFamilyName = (String) manufacturingFeaturePFMap.get(strName);

								strPartFamilyID =  (String) manufacturingFeaturePFMap.get(strID);
								excludePFOID .append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyID));
								excludePFOID .append(",");
							StringBuffer temp = new StringBuffer();
								temp.append(" <img border=\"0\" src=\"");
								temp.append("images/iconSmallPartFamily.gif");
								temp.append("\" /> ");
									temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
									temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
									temp.append("&amp;suiteKey=");
									temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
									temp.append("&amp;objectId=");
							temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyID));
									temp.append("', '450', '300', 'true', 'popup')\">");
									temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyName));
									temp.append("</a>");
									output = output + "<BR/>" + temp;

								output = output + "<BR/>";
							}
						}

					StringBuffer strBuffer = new StringBuffer();
					if(excludePFOID.length()>0)
					excludePFOID.deleteCharAt(excludePFOID.length()-1);
					strBuffer.append("<input type=\"text\" READONLY ");
					strBuffer.append("name=\"");
					strBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
					strBuffer.append("Display\" id=\"\" value=\"");
					strBuffer.append("\">");
					strBuffer.append("<input type=\"hidden\" name=\"");
					strBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
					strBuffer.append("\" value=\"");
					strBuffer.append("\">");
					strBuffer.append("<input type=\"hidden\" name=\"");
					strBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
					strBuffer.append("OID\" value=\"");
					strBuffer.append("\">");
					strBuffer.append("<input ");
					strBuffer.append("type=\"button\" name=\"btnPartFamily\"");
					strBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
					strBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_PartFamily:CURRENT!=policy_Classification.state_Obsolete&excludeOID=" +excludePFOID+
							"&table=FTRFeatureSearchResultsTable&showInitialResults=false&selection=single&showSavedQuery=true&hideHeader=true&HelpMarker=emxhelpfullsearch&submitURL=../configuration/LogicalFeatureSearchUtil.jsp&mode=Chooser&chooserType=SlideInFormChooser&fieldNameActual=PartFamilyOID&fieldNameDisplay=PartFamilyDisplay");
					strBuffer.append("&HelpMarker=emxhelpfullsearch','850','630')\">");
					strBuffer.append("&nbsp;&nbsp;");
					strBuffer.append("<a href=\"javascript:ClearPartFamily('");
					strBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
					strBuffer.append("')\">");

							String strClear =
								EnoviaResourceBundle.getProperty(context,"ProductLine",
										"emxProduct.Button.Clear",
										context.getSession().getLanguage());
					strBuffer.append(strClear);
					strBuffer.append("</a>");

					output = output + strBuffer;
								partList=output;
							}
			}

			else{
				editMode=false;
			}
			if(!editMode){

				if(manufacturingFeaturePFList.size()>0){
					for(int i=0;i<manufacturingFeaturePFList.size();i++){
					Map manufacturingFeaturePFMap = (Map) manufacturingFeaturePFList.get(i);
					strPartFamilyName = (String) manufacturingFeaturePFMap.get(strName);
					strPartFamilyID =  (String) manufacturingFeaturePFMap.get(strID);

								StringBuffer temp = new StringBuffer();
								if("CSV".equalsIgnoreCase(exportFormat)){        
									temp.append(strPartFamilyName);
									output = output + "," + temp;
									
                                }else{
                                	temp.append(" <img border=\"0\" src=\"");
    								temp.append("images/iconSmallPartFamily.gif");
    								temp.append("\" /> ");
    								temp.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
    								temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteDir));
    								temp.append("&amp;suiteKey=");
    								temp.append(XSSUtil.encodeForHTMLAttribute(context, suiteKey));
    								temp.append("&amp;objectId=");
    								temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyID));
    								temp.append("', '450', '300', 'true', 'popup')\">");
    								temp.append(XSSUtil.encodeForHTMLAttribute(context, strPartFamilyName));
    								temp.append("</a>");
    								output = output + "<BR/>" + temp;
                                }

								
								
						}
					}
					if(output.length()>0){
						if("CSV".equalsIgnoreCase(exportFormat)){  
							output = output.replaceFirst(",", "");
							partList=output;
						}else{
							output = output.replaceFirst("<BR/>", "");
							partList=output;
						}
					}
					else{
						partList=DomainConstants.EMPTY_STRING;
					}
				}

			return  partList;
		}
		catch (Exception e) {
			throw new FrameworkException(e.toString());
		}

	}

	/** It is used to display "Display Name" Column in Structure  Browser of Manufacturing Feature
	 * @param context
	 * @param args
	 * @return  Marketing Name For Products and Display Name for Manufacturing Feature
	 * @throws FrameworkException
	 * @author WKU
	 * @since R212
	 */
	public StringList displayNameForManufacturingFeature(Context context, String []args) throws FrameworkException{

		StringList displayNameList = new StringList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMapList = (MapList)programMap.get("objectList");
			String strName = null;
			String strRevision = null;
			String strDisplayName = null;
			String attrMarketName = "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
			String attrRevision = DomainConstants.SELECT_REVISION;
			String attrDisplayName = "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME	+"]";

	    	for(int i=0;i<objectMapList.size();i++)
	    	{
	    		Map objectMap = (Map) objectMapList.get(i);
	    		String strLogicalFeatureID = (String)objectMap.get(DomainConstants.SELECT_ID);
	    		DomainObject domObject = new DomainObject(strLogicalFeatureID);
	    		if(domObject.exists(context) && domObject.isKindOf(context, ConfigurationConstants.TYPE_PRODUCTS))
	    		{

	    			strName = (String) objectMap.get(attrMarketName);
	    			strRevision = (String) objectMap.get(attrRevision);
	    			strDisplayName = strName + " " + strRevision;
	    			if(!(strName!=null && !strName.equals("")))
	    			{
	    				strDisplayName = getDisplayName(context,strLogicalFeatureID,false);
	    			}
	    		}
	    		else
	    		{
	    			strName = (String) objectMap.get(attrDisplayName);
	    			strRevision = (String) objectMap.get(attrRevision);
	    			strDisplayName = strName + " " + strRevision;
	    			if(!(strName!=null && !strName.equals("")))
	    			{
	    				strDisplayName = getDisplayName(context,strLogicalFeatureID,true);
	    			}
	    		}
	    		displayNameList.addElement(strDisplayName);
	    	}
		}
		catch (Exception e) {
			throw new FrameworkException(e);
		}
		return displayNameList;
	}
	 /** Its is used to get the display name for Manufacturing Feature and Market name for Products
	 * @param context
	 * @param objectID -- Manufacturing Feature ID/Product ID
	 * @param displayName - boolean, true for MF and false for Products
	 * @return DisplayName for MF and Market Name for Products
	 * @throws FrameworkException
	 * @author WKU
	 * @since R212
	 */
	private String getDisplayName (Context context, String objectID, boolean displayName)throws FrameworkException{
		String strDisplayName = null;
		try{
			DomainObject domObject = new DomainObject(objectID);
			String strName = null;
			String strRevision = null;
			String attrMarketName = "attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]";
			String attrRevision = DomainConstants.SELECT_REVISION;
			String attrDisplayName = "attribute["+ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME	+"]";
			StringList selectables = new StringList();
			selectables.addElement(attrRevision);
			if(displayName)
				selectables.addElement(attrDisplayName);
			else
				selectables.addElement(attrMarketName);
			Map tempMap = domObject.getInfo(context, selectables);
			if(displayName)
				strName = (String) tempMap.get(attrDisplayName);
			else
				strName = (String) tempMap.get(attrMarketName);
			strRevision = (String) tempMap.get(attrRevision);
			strDisplayName = strName + " " + strRevision;

		}
		catch (Exception e) {
			throw new FrameworkException(e);
		}
		return strDisplayName;
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
	 * @author WKU
	 * @since FTR R212
	 */
	public String getProductTwoRangeHref(Context context, String[] args) throws Exception
	{
		String strTypes = EnoviaResourceBundle.getProperty(context,"Configuration.ManufacturingCompareTypesForObject2.type_Products");
		return "TYPES="+strTypes;
	}

	 /** This is an Utility method to check if the passed Manufacturing Feature relationship is used in any Rule Expression
	 * @param context
	 * @param strObjectIdList -- Business Objects ID List
	 * @return If used in any Rule Expression return true, else false.
	 * @throws FrameworkException
	 * @since R212
	 * @author WKU
	 */
	public static int isUsedInRulesForRelCheck(Context context, String []args) throws Exception{

		  String strLanguage = context.getSession().getLanguage();
		  String strSubjectKey = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
    			"emxProduct.Alert.UsedInRuleExpression",strLanguage);

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
          return 0;
	}
	 /** This is an Utility method to check if the passed Manufacturing Feature business object is used in any Rule Expression
	 * @param context
	 * @param strObjectIdList -- Business Objects ID List
	 * @return If used in any Rule Expression return true, else false.
	 * @throws FrameworkException
	 * @since R212
	 * @author WKU
	 */
	public static int isUsedInRulesForTypeCheck(Context context, String []args) throws Exception{

		  String strLanguage = context.getSession().getLanguage();
    	  String strSubjectKey = EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
    			"emxProduct.Alert.UsedInRuleExpression",strLanguage);

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
          String strRelBCRLE = "to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
          String strRelBCRRE = "to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE+"]";
          String strRelMPRLE = "to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
          String strRelMPRRE = "to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_MARKETING_PREFERENCE+"]";
          String strRelQRLE = "to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
          String strRelQRRE = "to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_QUANTITY_RULE+"]";
          String strRelIRRE = "to["+ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].from.type.kindof["+ConfigurationConstants.TYPE_INCLUSION_RULE+"]";

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
          return 0;
	}

 	/**
	 * This trigger method and it calls an Utility method to check if the passed business object is in frozen state or not
	 * It compare the current state and policy pair with the state and policy pair defined as frozen in configuration file
	 *
	 * @param context
	 * @param args
	 * @return - iReturn - 1 if object found in frozen state
	 * 				     - 0 otherwise.
	 * @throws FrameworkException
	 * @author WKU
	 * @since FTR R212
	 */
 	public int checkForFrozenStateRelationship(Context context, String[] args)throws Exception
	{
		String fromobjectid = args[1];
		ConfigurationUtil util = new ConfigurationUtil();
		int iReturn = 0;
		boolean frozen = util.isFrozenState(context, fromobjectid);

		if(frozen)
		{
			String language = context.getSession().getLanguage();
			String strAlertMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
					"emxProduct.Alert.LogicalFeatureReleased",language);
			emxContextUtilBase_mxJPO.mqlNotice(context,
					strAlertMessage);
			iReturn = 1;
		}
		return iReturn;
	}

 	/**
	 * This trigger method and it calls an Utility method to check if the passed business object is in frozen state or not
	 * It compare the current state and policy pair with the state and policy pair defined as frozen in configuration file
	 *
	 * @param context
	 * @param args
	 * @return - iReturn - 1 if object found in frozen state
	 * 				     - 0 otherwise.
	 * @throws FrameworkException
	 * @author WKU
	 * @since FTR R212
	 */
 	public int checkForFrozenStateType(Context context, String[] args)throws Exception
	{
		String objectid = args[0];

		ConfigurationUtil util = new ConfigurationUtil(objectid);
		int iReturn = 0;
		boolean frozen = util.isFrozenState(context, objectid);

		if(frozen)
		{
			String language = context.getSession().getLanguage();
			String strAlertMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
					"emxProduct.Alert.LogicalFeatureReleased",language);
			emxContextUtilBase_mxJPO.mqlNotice(context,
					strAlertMessage);
			iReturn = 1;
		}
		return iReturn;
	}

    /**
     * Method call as a trigger to check if the Parts associated with the
     * Manufacturing Feature are in Release state.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the parameters passed from the calling method
     * @return int - Returns 0 in case of Check trigger is success, all parts in
     *         Release state and 1 in case of failure, any of the parts not
     *         released
     * @throws Exception
     *             if operation fails
     * @since R212
     * @author WKU
     */
    public int checkPartsForManufacturingFeaturePromote(Context context, String args[])
    throws Exception {
    	// return value of the function
    	int iReturn = 0;
    	// The feature object id sent by the emxTriggerManager is retrieved
    	// here.
    	String objectId = args[0];
    	String strSelectState = DomainConstants.SELECT_CURRENT;
    	String strSelectType = DomainConstants.SELECT_TYPE;
    	// ObjectSelects StringList is initialized
    	StringList objectSelects = new StringList(strSelectState);
    	objectSelects.add(strSelectType);
    	LogicalFeature logicalFeature = new  LogicalFeature(objectId);
    	StringList tempStringList = new StringList();
    	MapList relBusObjList = logicalFeature.getActiveGBOMStructure(context,
    			DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, objectSelects,
    			tempStringList , false,true, 0, 0, DomainConstants.EMPTY_STRING,
    			DomainConstants.EMPTY_STRING, (short) 1, DomainConstants.EMPTY_STRING);

    	// The number of Parts connected is obtained.
    	int iNumberOfObjects = relBusObjList.size();
    	// Validation check only if there is any part conected.
    	if (iNumberOfObjects > 0) {

    		for (int i = 0; i < iNumberOfObjects; i++) {
    			// The state is retreived from the MapList obtained.
    			Map relBusObjMap = (Map)relBusObjList.get(i);
    			String strState = (String)relBusObjMap.get(strSelectState);
    			String strObjectType = (String)relBusObjMap.get(strSelectType);
    			// If the object is of type PartFamily no check will be done for
    			// the state.
    			if (!strObjectType.equals(ConfigurationConstants.TYPE_PARTFAMILY)) {

    				if (!strState
    						.equalsIgnoreCase(ConfigurationConstants.STATE_RELEASE)) {
    					String language = context.getSession().getLanguage();
    					String strAlertMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
    					"emxProduct.Alert.PartsCheckFailed",language);
    					emxContextUtilBase_mxJPO.mqlNotice(context,
    							strAlertMessage);
    					iReturn = 1;
    					break;

    				}
    			}
    		}
    	}
    	// Return 0 is validation is passed
    	return iReturn;
    }
	   /** Method call as a trigger to check if the sub-features associated with the
     * Manufacturing Feature are in. Release state.
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
     * @author WKU
     */
    public int checkSubFeaturesForManufacturingFeaturePromote(Context context, String args[])
    throws Exception {
    	// return value of the function
    	int iReturn = 0;
    	// The feature object id sent by the emxTriggerManager is retrieved
    	// here.
    	String objectId = args[0];
    	String strSelect = DomainObject.SELECT_CURRENT;
    	// ObjectSelects StringList is initialized
    	StringList objectSelects = new StringList(strSelect);
    	ConfigurationUtil util = new ConfigurationUtil();
    	ManufacturingFeature manufacturingFeature = new  ManufacturingFeature(objectId);
    	MapList relBusObjList =   manufacturingFeature.getManufacturingFeatureStructure(context,DomainObject.EMPTY_STRING,
    			DomainObject.EMPTY_STRING,objectSelects, null , false,true, 0, 0,
    			DomainObject.EMPTY_STRING,DomainObject.EMPTY_STRING, (short) 1,DomainObject.EMPTY_STRING);

    	boolean isFrozen = false;
    	int iNumberOfObjects = relBusObjList.size();
    	if (iNumberOfObjects > 0) {

    		for (int i = 0; i < iNumberOfObjects; i++) {
    			Map relBusObjMap = (Map)relBusObjList.get(i);
    			String strSubFeatureID = (String)relBusObjMap.get(DomainConstants.SELECT_ID);
    			isFrozen = util.isFrozenState(context,strSubFeatureID);
    			if (!isFrozen) {
    				String language = context.getSession().getLanguage();
    				String strAlertMessage = EnoviaResourceBundle.getProperty(context, "Configuration",
    				"emxProduct.Alert.ManufacturingFeaturesPromoteFailedStateNotRelease",language);
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
    /** Method call as a trigger to promote all the rules to the Release state if they already are not in that state.
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
    	String strComma = ",";
    	String strRelationshipPattern ="";
    	strRelationshipPattern = ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE + strComma;
    	strRelationshipPattern = strRelationshipPattern + ConfigurationConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE + strComma
    	+ ConfigurationConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION + strComma
    	+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
    	//Object where condition to retrieve the objects that are not already in Release state.
    	String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " != \""+ConfigurationConstants.STATE_RELEASE+"\")";
    	//Type to fetched is all types returned by the relationship.
    	String strTypePattern ="";
    	strTypePattern = ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE + strComma;
    	strTypePattern =strTypePattern + ConfigurationConstants.TYPE_QUANTITY_RULE + strComma
    	+ ConfigurationConstants.TYPE_RULE_EXTENSION + strComma
    	+ ConfigurationConstants.TYPE_FIXED_RESOURCE;
    	//ObjectSelects retreives the parameters of the objects that are to be retreived.
    	StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    	ConfigurationUtil util = new ConfigurationUtil(objectId);
    	StringList tempStringList = new StringList();
    	//The objects connected to the product based on the relationships defined are obtained.
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
    			setState(context,ConfigurationConstants.STATE_RELEASE);
    		}
    	}
    	//0 returned just to indicate the end of processing.
    	return 0;
    }
   /** Method call as a trigger to promote all the sub Features to the Release state if
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
    * @author WKU
    */
    public int promoteSubFeatures(Context context, String args[]) throws Exception {
    	// The Parent Feature object id sent by the emxTriggerManager
    	String objectId = args[0];
    	String state = ConfigurationConstants.STATE_RELEASE;
    	// The object id is set to the context
    	setId(objectId);
    	//boolean isFrozen = ConfigurationUtil.isFrozenState(context,objectId);


    	String strSelect = DomainConstants.SELECT_ID;
    	// ObjectSelects StringList is initialized
    	StringList objectSelects = new StringList(strSelect);
    	ManufacturingFeature manufacturingFeature = new  ManufacturingFeature(objectId);
    	// Object where condition to retrieve the sub feature objects that are not already in Release state.
    	StringList tempStringList = new StringList();
    	String objectWhere = "("+DomainConstants.SELECT_CURRENT+ " != \""+ConfigurationConstants.STATE_RELEASE+"\")";
    	//Replaced DomainConstants.EMPTY_STRINGLIST with tempStringList for stale object issue
    	MapList relBusObjList =   manufacturingFeature.getManufacturingFeatureStructure(context,DomainConstants.EMPTY_STRING,
    			DomainConstants.EMPTY_STRING,objectSelects, tempStringList , false,true, 0, 0,
    			objectWhere,DomainConstants.EMPTY_STRING, (short) 1,DomainConstants.EMPTY_STRING);

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
	 /** It is a trigger method. Fires when a "Manufacturing Feature" promoted  from review state to release state.
 	 * If the promoted LF have previous revision than it will replace with the latest revision in Products/Manufacturing Feature
 	 * which are not in Release state.
 	 * @param context
 	 *            The ematrix context object.
 	 * @param String[]
 	 *            The args .
 	 * @return
 	 * @author WKU
 	 * @since FTR R212
 	 */
 	public void ManufacturingFeatureFloatOnRelease(Context context, String[] args)throws FrameworkException
 	{
 		try{
 			//startTransaction(context, true);
 			//Manufacturing Feature ID
 			String manufacturingFeatureID = args[0];
 			//Domain objet of Manufacturing Feature
 			DomainObject domObject = new DomainObject(manufacturingFeatureID);

 			//Getting previous revision businessObject
 			BusinessObject boPreviousRevision= domObject.getPreviousRevision(context);
 			if(boPreviousRevision.exists(context))
 			{
 				//Getting previous revision ID
 				String strPreviousRevID = boPreviousRevision.getObjectId(context);
 				//Type Pattern
 				String strTypePattern = DomainObject.QUERY_WILDCARD;
 				//Rel PAttern
 				String strRelPattern = ConfigurationConstants.RELATIONSHIP_MANUFACTURING_STRUCTURES;
 				//where expression
 				String strRelease = FrameworkUtil.lookupStateName(context,
 						ProductLineConstants.POLICY_PRODUCT, strSymbReleaseState);
 				StringBuffer sbBuffer = new StringBuffer();
 				sbBuffer.append(DomainObject.SELECT_CURRENT);
 				sbBuffer.append(SYMB_NOT_EQUAL);
 				sbBuffer.append(SYMB_QUOTE);
 				sbBuffer.append(strRelease);
 				sbBuffer.append(SYMB_QUOTE);
 				String strBusWhereClause = sbBuffer.toString();
 				ConfigurationUtil util = new ConfigurationUtil(strPreviousRevID);
 				StringList tempStringList = new StringList();
 				//Traversing to one level Up
 				MapList manufacturingFeaturesRelIDListUp = util.getObjectStructure(context,strTypePattern,strRelPattern,
 						tempStringList, tempStringList,true, false, 1,0,
 						strBusWhereClause, DomainObject.EMPTY_STRING,(short) 1,DomainObject.EMPTY_STRING);
 				if(manufacturingFeaturesRelIDListUp.size()>0)
 				{
 					for(int i=0;i<manufacturingFeaturesRelIDListUp.size();i++)
 					{
 						Map manufacturingFeaturesRelIDMapUp = (Map)manufacturingFeaturesRelIDListUp.get(i);
 						String lfRelID = (String)manufacturingFeaturesRelIDMapUp.get(DomainObject.SELECT_RELATIONSHIP_ID);
 						//Changing the to side object, Previsous Revision to Latest Revision
 						if(lfRelID!=null)
 							DomainRelationship.setToObject(context, lfRelID,domObject);
 					}
 				}

 			}
 		}
 		catch (Exception e) {
 			throw new FrameworkException(e.toString());
 		}
 	}
    /** Method call as a trigger to demote all the rules to the PRELIMINARY state if they already are not in that state.
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
    	String strComma = ",";
    	String strRelationshipPattern ="";
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
 	 * It is a check trigger method. Fires when a relationship Manufacturing Features is getting created
 	 * It is used to check the cyclic condition
 	 * @param context
 	 *            The ematrix context object.
 	 * @param String[]
 	 *            The args .
 	 * @return zero when cyclic condition is false, throws exception if it is true
 	 * @author g98
 	 * @since R212
 	 */
 	public int multiLevelRecursionCheck(Context context, String[] args)throws Exception
 	{
		 String strRemovedLFId = PropertyUtil.getGlobalRPEValue(context,"CyclicCheckRequired");
		 int iResult = 0;
	 	 if(strRemovedLFId==null || strRemovedLFId.equalsIgnoreCase("")) {
	    	 String fromObjectId = args[0];
	    	 String toObjectId = args[1];
	    	 String relType = args[2];
	    	 try {
	    		 boolean recursionCheck =  ConfigurationUtil.multiLevelRecursionCheck(context,fromObjectId,toObjectId,relType);
	    		 if (recursionCheck) {
					String language = context.getSession().getLanguage();
					String strAlertMessage = EnoviaResourceBundle.getProperty(context, "Configuration",
							"emxConfiguration.Add.CyclicCheck.Error",language);
					throw new FrameworkException(strAlertMessage);
	    		 } else {
	    			 iResult = 0;
	    		 }
	    	 }catch (Exception e) {
					throw new FrameworkException(e.getMessage());
	    	 }
	 	}else if(strRemovedLFId.equalsIgnoreCase("False")){
			iResult = 0;
		}
	 	return iResult;


 	}
 	
 	/**
	  * It is used in product version/copy to copy ManufacturingFeature to the newly created product
	  * @param context
	  * @param args
	  * @return Map containing old created Manufacturing Feature rel id as key and pipe separated 
	  * old rel physical id, new rel physical id, new rel id as value
	  * @throws FrameworkException
	  * @exclude
	  */
 	 public Map copyManufacturingFeature(Context context, String[] args) 
		throws FrameworkException
		{
 		 Map clonedObjIDMap = null;

 		 try{
 			 ArrayList programMap = (ArrayList)JPO.unpackArgs(args);

 			 String sourceObjectId = (String) programMap.get(0);
 			 String destinationObjectId = (String) programMap.get(1);

 			 clonedObjIDMap = ManufacturingFeature.copyManufacturingFeatureStructure(context, sourceObjectId, destinationObjectId);
 		 }
 		 catch(Exception e){
 			 throw new FrameworkException(e);
 		 }	
 		 return clonedObjIDMap;
		}
 	 
 	/**
      * This method is used to connect the derived Manufacturing Feature revisions to the Parent Feature.
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
     public void connectManufacturingFeatureOnRevise(Context context, String args[])
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
	 	 * Trigger will be invoked on Manufacturing Features Delete Action. It will remove the Committed Context relationship
	 	 * and Change the Committed Manufacturing Features relation to Candidate Manufacturing Features relation.
	 	 * 
	 	 * @param context
	 	 * @param args
	 	 * @return
	 	 * @throws FrameworkException
	 	 */
  public void removeCommittedContextForManufacturingFeatures(Context context, String args[]) throws FrameworkException {
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
			
			StringBuffer strModelRelPattern = new StringBuffer(ConfigurationConstants.RELATIONSHIP_COMMITTED_MANUFACTURING_FEATURES);
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
									DomainRelationship.setType(context, relationshipId, ConfigurationConstants.RELATIONSHIP_CANDIDTAE_MANUFACTURING_FEATURES);
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
		* This method is used to refresh the Structure Tree On Add/Remove for Manufacturing Features.
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
			returnMap.put("Message","{ main:function __main(){refreshTreeForAddObj(xmlResponse,'Manufacturing Feature')}}");
			return returnMap;
		}
 }

