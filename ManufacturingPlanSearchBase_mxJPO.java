/*
 *  ManufacturingPlanSearchBase.java
 *
 *  JPO for fetching the relevent Business objects based on the Type selected
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */



import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.dmcplanning.ManufacturingPlan;
import com.matrixone.apps.dmcplanning.ManufacturingPlanConstants;
import com.matrixone.apps.dmcplanning.Model;
import com.matrixone.apps.dmcplanning.Product;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.DerivationUtil;

public class ManufacturingPlanSearchBase_mxJPO extends emxDomainObject_mxJPO
{

	/** A string constant with the value emxProduct.common.Marketing. */
	protected static final String FEATURE_MARKETING = "emxProduct.common.Marketing";
	//Internationalisation Constant
	/** A string constant with the value emxProductLineStringResource. */

	/**
	 * The default constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments.
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.0.0.0
.*/



	public ManufacturingPlanSearchBase_mxJPO (Context context, String[] args) throws Exception
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
	 * @since ProductCentral 10-0-0-0
	 */
	public int mxMain(Context context, String[] args)
	throws Exception
	{
		if (!context.isConnected()){
			String sContentLabel = EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
			throw  new Exception(sContentLabel);
		}
		return 0;
	}


	/**
	 * To obtain the list of Manufacturing Plans to be excluded in the Search Results
	 * and show the Manufacturing Plans related to selected Revision
	 *  * @param context- the eMatrix <code>Context</code> object
	 * @param args- holds the HashMap containing the following arguments
	 * @return  StringList- consisting of the object ids to be excluded from the Search Results
	 * @throws Exception if the operation fails
	 * @author IXH
	 */

	


	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getMPIdsForSearchList(Context context,String[] args)throws Exception
	{
		StringList sLMPIdSearchList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strContxtSelectedtId = (String)  programMap.get("objectId");
		String strParentID = (String)  programMap.get("parentID");
		String strSelMPId = (String)programMap.get("SelId");

		try
		{
			DomainObject domContxtId = new DomainObject(strContxtSelectedtId);

			//get all the MP connected to the selected Feat
			sLMPIdSearchList = domContxtId.getInfoList(context, "from["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].to.id");

			for(int i=0;i<sLMPIdSearchList.size();i++)
			{
				String strMPId = (String)sLMPIdSearchList.get(i);

				//To check if this MP is related to the selected MP with "Manufacturing Plan Break Down" or not.
    			DomainObject domMPId = new DomainObject(strMPId);
    			StringList sLCntxtBrkDownMPIds = domMPId.getInfoList(context,"to["+ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN+"].from.id");

    			for(int j=0 ;j<sLCntxtBrkDownMPIds.size();j++)
    			{
    				String strCntxtBrkDownMPId =(String)sLCntxtBrkDownMPIds.get(j);
    				if(strCntxtBrkDownMPId!=null && strCntxtBrkDownMPId.equalsIgnoreCase(strSelMPId))
    				{
    					//If this MP ID is connected to context Product or not
    					DomainObject domCntxtBrkDownMPId = new DomainObject(strCntxtBrkDownMPId);
    					String strContxtProductId = domCntxtBrkDownMPId.getInfo(context,"to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id");
        				if(strContxtProductId!=null)
	        			{
	        				if(strContxtProductId.equalsIgnoreCase(strParentID))
		        			{
	        					sLMPIdSearchList.remove(i);
	    						i++;
		        			}
	        			}
    				}
    			}
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return sLMPIdSearchList;
	}
	/**
	 * This Methods is Used to get all the plans to be displayed
	 * in search page during Create Manufacturing Plan Derived From
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author WPK
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getMPofAllRevisions(Context context,String[] args)throws Exception
	{
		StringList sLMPIdSearchList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strContxtSelectedtId = (String)  programMap.get("objectId");
		String strParentID = (String)  programMap.get("parentID");

		try
		{
			DomainObject domContxtId = new DomainObject(strContxtSelectedtId);
			
	        StringList busSelects = new StringList(1);
	        busSelects.add(DomainObject.SELECT_ID);

	        // for the Id passed, get revisions Info
	        MapList revisionsList = domContxtId.getRevisionsInfo(context,busSelects,
	                                                          new StringList(0));
			HashMap hmRevision = null;
			String strRevId = null;
			for(int i=0;i<revisionsList.size();i++){
				hmRevision = (HashMap)revisionsList.get(i);
				int iLevel = ManufacturingPlanBase_mxJPO.getLevelfromSB(context,args);
				strRevId = (String)hmRevision.get(DomainObject.SELECT_ID);
				// Create the instance of ManufacturingPlan bean and call
				// getManufacturingPlans method
				ManufacturingPlan mpBean = new ManufacturingPlan(strRevId);
				MapList mapActiveMPs = (MapList)mpBean.getManufacturingPlans(context,new StringList(),new StringList(),iLevel, 0);
				Hashtable hmPlans = null;
				String strPlanId = null;
				for(int j=0;j<mapActiveMPs.size();j++){
					hmPlans = (Hashtable)mapActiveMPs.get(j);
					sLMPIdSearchList.add((String)hmPlans.get(DomainObject.SELECT_ID));
				}
				
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return sLMPIdSearchList;
	}
    /**
     * This method is used to get the List of Master Features connected the Model and exclude them in the Search Results
     * While performing the Add Existing Master Feature under Model Contest
     * 
     * @param context :
     *             The eMatrix <code>Context</code> object
     * @param args :
     *             Holds the HashMap containing the following arguments
     * @return  StringList :
     *             Consisting of the object ids that need to be excluded, the Master Features 
     *             connected to context Model with Model Master Features relationship
     * @throws Exception :
     *             Throws Exception if the operation fails
     * @since CFP R209
     * @author IVU
     */

    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeMasterFeatureOfContextModel (Context context, String[] args) throws Exception {
        StringList excludeList = new StringList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String)  programMap.get("objectId");
        DomainObject objModel = new DomainObject(objectId);
        String strMdType = objModel.getInfo(context,DomainObject.SELECT_TYPE);
        StringList strMasterFeature = new StringList();
        //if(strMdType.equalsIgnoreCase(ManufacturingPlanConstants.TYPE_MODEL)){
        if(mxType.isOfParentType(context,strMdType,ManufacturingPlanConstants.TYPE_MODEL)){
        	strMasterFeature = objModel.getInfoList(context,"from["+ManufacturingPlanConstants.RELATIONSHIP_MODEL_MASTER_FEATURES+"].to.id");	
        }
        strMasterFeature.addElement(objectId);
        return strMasterFeature;
    }

    /**
     * This method is used to obtain the list of product revisions that needs to be excluded in search page while
     * performing add existing operations
     *
     * @param context :
     *             The eMatrix <code>Context</code> object
     * @param args :
     *             Holds the HashMap containing the following arguments
     * @return  StringList :
     *             Consisting of the object ids that need to be excluded, the Master Features Revisions
     *             connected to context Master Feature with Managed Revision relationship
     * @throws Exception :
     *             Throws Exception if the operation fails
     * @since CFP R209
     * @author IVU
     */
    public StringList filterRelatedProductRevisions(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strMasterFeatureId = (String)  programMap.get("objectId");
            DomainObject domObj = new DomainObject(strMasterFeatureId);

            StringBuffer strInfo = new StringBuffer();
            strInfo.append("from[");
            strInfo.append(ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION);
            strInfo.append("].to.id");

            StringList listFromIds = domObj.getInfoList(context,strInfo.toString() );
            if(listFromIds == null || listFromIds.size() == 0) {
                return new StringList();
            }else {
                return listFromIds;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new FrameworkException(e.getMessage());
        }
    }

/**
	 * This method returns Manufacturing Plans of current and previous revision of Products/Features
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211- Modified in R212 Derivations to get MP of the derivation chain 
	 */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getMPofCurrentAndAllPreviousRevisions(Context context,String[] args)throws FrameworkException
	{
		StringList sLMPIdSearchList = new StringList();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strContxtSelectedtId = (String)  programMap.get("objectId");
			List listLowerRevisions= new MapList();
			//For current PRD get backward derivation chains 
			listLowerRevisions= DerivationUtil.getPreviousDerivations(context, strContxtSelectedtId, null);
			Map hmRevision = null;
			String strRevId = "";
			//for all PRD in backward Derivation-- get associated MPs
			for(int i=0;i<listLowerRevisions.size();i++){
				hmRevision = (Map)listLowerRevisions.get(i);
				int iLevel = ManufacturingPlanBase_mxJPO.getLevelfromSB(context,args);
				strRevId = (String)hmRevision.get(DomainObject.SELECT_ID);
				ManufacturingPlan mpBean = new ManufacturingPlan(strRevId);
				//TODO- Can we use Single DB call to get all MPs- need to check on state of MPs
				MapList mapActiveMPs = (MapList)mpBean.getManufacturingPlans(context,new StringList(),new StringList(),iLevel, 0);
				Hashtable hmPlans = null;
				for(int j=0;j<mapActiveMPs.size();j++){
					hmPlans = (Hashtable)mapActiveMPs.get(j);
					sLMPIdSearchList.add((String)hmPlans.get(DomainObject.SELECT_ID));
				}
			}
		}catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return sLMPIdSearchList;
	}
	/**
	 * This method returns Manufacturing Plans of current revision of Products/Features
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211 -reused in R212_Derivatons
	 */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getMPofCurrentRevision(Context context,String[] args)throws Exception
	{
		StringList sLMPIdSearchList = new StringList();
		Map programMap = (HashMap) JPO.unpackArgs(args);
		String strContxtSelectedtId = (String)  programMap.get("objectId");
		try
		{
			DomainObject domContxtId = new DomainObject(strContxtSelectedtId);

			MapList currentRevisions = new MapList();
			//Add Current revision to list
			Map currentRevision = new HashMap();
			currentRevision.put("id",strContxtSelectedtId);
			currentRevision.put("ProductTNR",domContxtId.getInfo(context, DomainConstants.SELECT_TYPE)+"::"+domContxtId.getInfo(context, DomainConstants.SELECT_NAME)+"::"+domContxtId.getInfo(context, DomainConstants.SELECT_REVISION));
			currentRevisions.add(currentRevision);
			
			HashMap hmRevision = null;
			String strRevId = null;
			for(int i=0;i<currentRevisions.size();i++){
				hmRevision = (HashMap)currentRevisions.get(i);
				int iLevel = ManufacturingPlanBase_mxJPO.getLevelfromSB(context,args);
				strRevId = (String)hmRevision.get(DomainObject.SELECT_ID);
				// Create the instance of ManufacturingPlan bean and call
				// getManufacturingPlans method
				ManufacturingPlan mpBean = new ManufacturingPlan(strRevId);
				MapList mapActiveMPs = (MapList)mpBean.getManufacturingPlans(context,new StringList(),new StringList(),iLevel, 0);
				Hashtable hmPlans = null;
				for(int j=0;j<mapActiveMPs.size();j++){
					hmPlans = (Hashtable)mapActiveMPs.get(j);
					String curState = (String)hmPlans.get(DomainObject.SELECT_CURRENT);
					if(!(ManufacturingPlanConstants.STATE_ARCHIVED.equals(curState)))
					{
						sLMPIdSearchList.add((String)hmPlans.get(DomainObject.SELECT_ID));
					}
			
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}

		return sLMPIdSearchList;
	}

	/**
	 * This method returns Manufacturing Plans Breakdown of context Manufacturing Plan
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getMPBreakdown(Context context,String[] args)throws Exception{
		StringList returnStringList = new StringList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String selectedId = (String)programMap.get("SelId");

			if(selectedId!=null && !selectedId.equalsIgnoreCase("")){
				ManufacturingPlan mpBean = new ManufacturingPlan(selectedId);
				MapList mapMPBreakdowns = (MapList) mpBean.getManufacturingPlanBreakdowns(context,new StringList(),new StringList(), 1, 0);

				for(int j=0;j<mapMPBreakdowns.size();j++){
					Hashtable  hmPlans = (Hashtable)mapMPBreakdowns.get(j);
					returnStringList.add((String)hmPlans.get(DomainObject.SELECT_ID));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return returnStringList;
	}

	/**
	 * Include Program to get all Products connected with Model, for Planned For
	 * field chooser in case of Root MP Creation in Model Context, we get all Product which are
	 * assocaited with model
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 * @since CFP R212Derivations
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getManagedProducts (Context context, String[] args) throws FrameworkException {
		StringList slIncludeIds= new StringList();
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String modelID = (String)programMap.get("contextObjectId");
			Model modelBean= new Model(modelID);
			slIncludeIds=modelBean.getManagedRevisions(context);
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return  slIncludeIds;
	}

	/**
	 * include Program to get all Products which are connected  in forward derivation chain with Product, for Planned For
	 * field chooser in case of Non Root MP Creation in Product Context,
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 * @since CFP R212Derivations
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getForwardChainofContext(Context context, String[] args)
			throws FrameworkException {
		StringList slIncludeIds = new StringList();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strProductID = (String) programMap.get("contextObjectId");
			String derivedFromID = (String) programMap.get("derivedFromID");
			if(derivedFromID!=null && !derivedFromID.equalsIgnoreCase("root")){
				Map mpDetails= new ManufacturingPlan(derivedFromID).getManufacturingPlanDetail(context);
				String contextIDSelectable = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
					+ "].from.id";
				String contextProductID=(String)mpDetails.get(contextIDSelectable);
				if(!contextProductID.equalsIgnoreCase(strProductID))
					strProductID=contextProductID;
			}
//			if(Product.isDerivationEnabled(context)){
				StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
				StringList relSelects = new StringList(
						DomainConstants.SELECT_RELATIONSHIP_ID);

				MapList relBusObjPageList = new MapList();
				DomainObject dom = new DomainObject(strProductID);
				short sLevel = 0;
				// Gets the relationship name
				String strRelName = ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT;
				String strTypeName = ManufacturingPlanConstants.TYPE_PRODUCTS;
				relBusObjPageList = dom.getRelatedObjects(context, strRelName,
						strTypeName, objectSelects, relSelects, false, true,
						sLevel, "", "", 0);
				Hashtable hmPlans = null;
				for (int j = 0; j < relBusObjPageList.size(); j++) {
					hmPlans = (Hashtable) relBusObjPageList.get(j);
					slIncludeIds.add((String) hmPlans.get(DomainObject.SELECT_ID));
				}
//			}
//			else{
//				BooleanOptionCompatibility bcr = new BooleanOptionCompatibility();
//				List lstRevIdList = bcr.getAllRevisions(context, strProductID);
//				slIncludeIds = (StringList)bcr.getHigherLowerRevisions(context,
//						strProductID, lstRevIdList, true,false);
//			}
			slIncludeIds.add(strProductID);
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return slIncludeIds;
	}
    
	/**
	 * Include Program to get backward derivations of the selected Manufacturing
	 * Plan, until the Planned For product of the Derived From Manufacturing
	 * Plan, used in Insert Before case to get valid Product between Derived To and Derived From MP
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getBackwardDerivationChain (Context context, String[] args) throws FrameworkException {
		
        StringList slIncludeIds= new StringList();
        try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strMPContextID = (String)programMap.get("contextObjectId");
			//Planned for Product of selected MP's Parent        
			String strParentMPContextID = (String)programMap.get("parentMPContextID");
//			if(Product.isDerivationEnabled(context)){
				MapList mpList= DerivationUtil.getPreviousDerivations(context, strMPContextID, strParentMPContextID);
				Hashtable hmPlans = null;
				for(int j=0;j<mpList.size();j++){
					hmPlans = (Hashtable)mpList.get(j);
					slIncludeIds.add((String)hmPlans.get(DomainObject.SELECT_ID));
				}
				slIncludeIds.add(strMPContextID);
//			}else{
//				BooleanOptionCompatibility bcr = new BooleanOptionCompatibility();
//				MapList lstRevIdList = (MapList)bcr.getAllRevisions(context, strMPContextID);
//				lstRevIdList.addSortKey("index", "ascending", "integer");
//				for(int j=0;j<lstRevIdList.size();j++){
//					String strCurrentOID = ((Map) lstRevIdList.get(j)).get(
//							ManufacturingPlanConstants.SELECT_ID)
//							.toString().trim();
//					if(strCurrentOID.equalsIgnoreCase(strParentMPContextID)){
//						for(int i=j;i<lstRevIdList.size();i++){
//							String strCurrentOID1 = ((Map) lstRevIdList.get(i)).get(
//									ManufacturingPlanConstants.SELECT_ID)
//									.toString().trim();
//							slIncludeIds.add(strCurrentOID1);
//							if(strCurrentOID1.equalsIgnoreCase(strMPContextID))
//								break;
//						}
//						break;
//					}
//				}				
//			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
        return  slIncludeIds;
    }

	/**
	 * Include program , used to get valid Manufacturing Plan,from which Manufacturing Plan will be derived 
	 * from, this is case product context and no MP is selected for derived from.  This gets all Manufacturing Plans
	 * from which a new Derivation can be created.
	 * -----------------------------------------
	 * Used for Create Derivation Only
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getManufacturingPlansForDerivationCreate (Context context, String[] args) throws Exception {
		StringList slIncludeMPIds= new StringList();
		
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String contextID = (String)programMap.get("contextObjectId");
			
			// Get all Products which are in backward derivation chain from the Context Product of selected MP
			StringList slIncludeIds = new StringList(contextID);
			MapList mpList= DerivationUtil.getPreviousDerivations(context, contextID, null);
			Map hmPlans = null;
			for (int j = 0; j < mpList.size(); j++) {
				hmPlans = (Hashtable)mpList.get(j);
				slIncludeIds.add((String)hmPlans.get(DomainObject.SELECT_ID));
			}
			
			// Now get all MP which are associated with Product
			String[] productsidsArray = new String[slIncludeIds.size()];
			StringList objectSelects = new StringList();
			
			String strMPIDSelectable = 
					"from[" + ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].to.id";
			objectSelects.add(strMPIDSelectable);

			DomainObject.MULTI_VALUE_LIST.add(strMPIDSelectable);
			MapList mlMPList = DomainObject.getInfo(context, (String[])slIncludeIds.toArray(productsidsArray), objectSelects);
			DomainObject.MULTI_VALUE_LIST.remove(strMPIDSelectable);
			
			hmPlans = null;
			for (int j = 0; j < mlMPList.size(); j++) {
				hmPlans = (HashMap)mlMPList.get(j);
				if(!hmPlans.isEmpty() && hmPlans.containsKey(strMPIDSelectable))
				slIncludeMPIds.addAll((StringList)hmPlans.get(strMPIDSelectable));
			}
			
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return slIncludeMPIds;
	}

	/**
	 * Include program , used to get valid Manufacturing Plan,from which Manufacturing Plan will be derived 
	 * from, this is case product context and no MP is selected for derived from.  This gets all Manufacturing Plans
	 * from which a new Revision can be created.
	 * -----------------------------------------
	 * Used for Create Revision Only
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getManufacturingPlansForRevisionCreate (Context context, String[] args) throws Exception {
		StringList slIncludeMPIds= new StringList();
		
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String contextID = (String)programMap.get("contextObjectId");
			
			// Get all Products which are in backward derivation chain from the Context Product of selected MP
			StringList slIncludeIds = new StringList(contextID);
			MapList mpList= DerivationUtil.getPreviousDerivations(context, contextID, null);
			Map hmPlans = null;
			for (int j = 0; j < mpList.size(); j++) {
				hmPlans = (Hashtable)mpList.get(j);
				slIncludeIds.add((String)hmPlans.get(DomainObject.SELECT_ID));
			}
			
			// Now get all MP which are associated with Product
			String[] productsidsArray = new String[slIncludeIds.size()];
			StringList objectSelects = new StringList();
			
			// Selectable used to get the Object Id of the associated Manufacturing Plan.
			String strMPIDSelectable = 
					"from[" + ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].to.id";

			// Selectable used to determine whether the Manufacturing Plan has a higher Revision.  If this evaluates
			// to "false", there is no higher revision, and the user can create a revision from it.
			String strMainDerivedSelectable = 
					"from[" + ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].to.from[" + 
							ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED + "]";

			objectSelects.add(strMPIDSelectable);
			objectSelects.add(strMainDerivedSelectable);

			DomainObject.MULTI_VALUE_LIST.add(strMPIDSelectable);
			DomainObject.MULTI_VALUE_LIST.add(strMainDerivedSelectable);
			MapList mlMPList = DomainObject.getInfo(context, (String[])slIncludeIds.toArray(productsidsArray), objectSelects);
			DomainObject.MULTI_VALUE_LIST.remove(strMPIDSelectable);
			DomainObject.MULTI_VALUE_LIST.remove(strMainDerivedSelectable);
			
			hmPlans = null;
			for (int j = 0; j < mlMPList.size(); j++) {
				hmPlans = (HashMap)mlMPList.get(j);
				if (!hmPlans.isEmpty() && hmPlans.containsKey(strMPIDSelectable) && hmPlans.containsKey(strMainDerivedSelectable)) {
					StringList slIds = (StringList)hmPlans.get(strMPIDSelectable);
					StringList slHasMainDerived = (StringList)hmPlans.get(strMainDerivedSelectable);
					// We need to check each ID and make sure the MP does not already have a Revision.  If it does, we
					// cannot include it.
					for (int k = 0; k < slIds.size(); k++) {
						String strHasMainDerived = (String)slHasMainDerived.get(k);
						if (strHasMainDerived.equals("False")) {
							slIncludeMPIds.add(slIds.get(k));
						}
					}
				}
			}
			
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return slIncludeMPIds;
	}

}//End of the class



