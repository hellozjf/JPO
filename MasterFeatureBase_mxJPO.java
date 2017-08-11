/*
 ** ${CLASSNAME}
 **
 **Copyright (c) 1993-2016 Dassault Systemes.
 ** All Rights Reserved.
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.dmcplanning.ManufacturingPlanUtil;
import com.matrixone.apps.dmcplanning.ManufacturingPlanConstants;
import com.matrixone.apps.dmcplanning.MasterFeature;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineConstants;


/**
 * This JPO class has some methods pertaining to Master Feature.
 * 
 * @author IVU
 * @since DMCPlanning R209
 */
public class MasterFeatureBase_mxJPO extends emxDomainObject_mxJPO {
	protected String SUITE_KEY="DMCPlanning";
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
     * @since DMCPlanning R209
     * @author IVU
     */
    public MasterFeatureBase_mxJPO(Context context, String[] args)
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
     * @since DMCPlanning R209
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (!context.isConnected())
            throw new Exception("Not supported on desktop client");
        return 0;
    }

    
    /**
     * This is method is used  to retrieve the context Master Feature/Model.
     * Master Feature of the  Technical feature if present. 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments
     * @return Object - MapList containing the id of Master Feature if it is present for the given Technical Feature
     * @throws Exception
     *             if the operation fails
     * @since R209
     * @author IXH
     */

  	public Vector getContextModelTemplate(Context context , String[] args )throws Exception {
        //XSSOK
  		HashMap programMap = (HashMap) JPO.unpackArgs(args);
          MapList lstObjectIdsList = (MapList) programMap.get("objectList");
  		Vector vCntxtMastFeatValues = new Vector(lstObjectIdsList.size());
  		
  		String exportFormat = null;
  		boolean exportToExcel = false;
  		HashMap requestMap = (HashMap)programMap.get("paramList");
  		if(requestMap!=null && requestMap.containsKey("reportFormat")){
  			exportFormat = (String)requestMap.get("reportFormat");
  		}
  		if("CSV".equals(exportFormat)){
  			exportToExcel = true;
  		}
  		
          try{
  	        StringBuffer sb;
  	        String strStartHrefTitle = "<a TITLE=\"";
  	        String strEndTitle = "\">";
  	        String strStartImage = "<img src=";
  	        String strImageURL = "";
  	        String strImageEnd = " />";
  	        String strEndHref = "</a>";
  	        
  	        String strFeatId = DomainConstants.EMPTY_STRING;     
  	        HashMap paramMap = (HashMap) programMap.get("paramList");
  	        String strParentId = (String) paramMap.get("parentOID");
  	        //IR-030916V6R2011WIM copy to case in step 2 parentOID is null, in that case it is retrieved from objectId
  	        if(strParentId == null){
  	        	strParentId=(String)paramMap.get("objectId");
  	        }
  	        
  	        DomainObject domParentID = new DomainObject(strParentId);
  	        //Get the Model ID
  	        String strModelID = "";
  	        StringList selectList = new StringList();
  	        selectList.addElement(DomainConstants.SELECT_TYPE);
  	        selectList.addElement("to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].from.id");
  	        selectList.addElement("to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].from.id");
  	        Map mp = domParentID.getInfo(context, selectList);
  	        String strParentType = (String)mp.get(DomainConstants.SELECT_TYPE);
  	        if(strParentType.equalsIgnoreCase(ManufacturingPlanConstants.TYPE_PRODUCT_VARIANT))
  	        {
  	        	strModelID = (String)mp.get("to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_VERSION+"].from.to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].from.id");
  	        }
  	        else
  	        {
  	        	strModelID = (String)mp.get("to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].from.id");
  	        }
  	        Map tempMap = null;
  				        
  			if(lstObjectIdsList.size() != 0)
  			{
  				for (int iCnt = 0 ; iCnt < lstObjectIdsList.size();iCnt++ )
  				{				
  					sb = new StringBuffer();
  			        sb = sb.append(strStartHrefTitle);
  			        
  					tempMap = (Map) lstObjectIdsList.get(iCnt);
  					
  					// getting the context parent ids (Feature and Product)
  	                strFeatId = (String) tempMap.get("id");    
  	                String strRootNode = (String) tempMap.get("Root Node");
  	                
  	                DomainObject domFeatId = new DomainObject(strFeatId);                 
  	               
  	                String strType = domFeatId.getInfo(context, DomainConstants.SELECT_TYPE);
  	                if (( mxType.isOfParentType(context, strType,ManufacturingPlanConstants.TYPE_LOGICAL_FEATURE)
  	                	  || mxType.isOfParentType(context, strType,ManufacturingPlanConstants.TYPE_PRODUCTS)) 
  	                	  && (strRootNode == null || (strRootNode.equals("")) || ("null".equalsIgnoreCase(strRootNode))))
  	                {
  	                	String relPattern = "";
  	         	        String objPattern = "";
  	         	        StringList objSelects = new StringList();
  	         	        StringList relSelects = new StringList();
  	         	        String objWhere = "";
  	         	        String relWhere = "";
  	         	        
  	         	        relPattern = ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION 
  	         	                     + "," +
  	         	                     ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS;
  	         	        			
  	         	        objPattern = ManufacturingPlanConstants.TYPE_MASTER_FEATURE
  	         	        				+","+
  	         	        				ManufacturingPlanConstants.TYPE_MODEL;
  	                	
  	                    objSelects.add(DomainConstants.SELECT_ID);
  	                    objSelects.add(DomainConstants.SELECT_TYPE);
  	                    objSelects.add(DomainConstants.SELECT_NAME);
  	                    
  		                MapList mLCntxtMasterFeatures = domFeatId.getRelatedObjects(context,
  		                															relPattern,
  		                															objPattern,
  											                	             		objSelects,
  											                	             		null,
  											                	             		true,
  											                	             		false,
  											                	             		(short) 1, 
  											                	             		null,
  											                	             		null,
  											                	             		0);
  		               String strFATValue = null;
  		               
  		               if(!mLCntxtMasterFeatures.isEmpty())
  		               {
  		            	   for(int i=0;i<mLCntxtMasterFeatures.size();i++)
  		                   {
  		                	   Map mtempMap = new HashMap();
  		                	   mtempMap = (Map) mLCntxtMasterFeatures.get(i);
  		                	   String strCntxtMasterFeatId =(String) mtempMap.get(DomainConstants.SELECT_ID);
  		                	   String strCntxtMasterFeatName =(String) mtempMap.get(DomainConstants.SELECT_NAME);
  		                	   String strCntxtMasterFeatType =(String) mtempMap.get(DomainConstants.SELECT_TYPE);
  		                	   if(strCntxtMasterFeatType!=null)
  		                	   {
  		                		 if(exportToExcel)
    		                	   {
  		                			vCntxtMastFeatValues.add(strCntxtMasterFeatName);
    		                	   }
  		                		 else{
  		                		   //START - Modified for bug no. IR-034765V6R2011
  		                		   sb = sb.append(XSSUtil.encodeForXML(context,strCntxtMasterFeatName))
  		   		                      	  .append(strEndTitle)
  		   		                      	  .append(strStartImage);
  		                		   //Check if the Product is under a Model Context
  		                		   //if(strCntxtMasterFeatType.equalsIgnoreCase(ManufacturingPlanConstants.TYPE_MODEL))
  		                		 if(mxType.isOfParentType(context,strCntxtMasterFeatType,ManufacturingPlanConstants.TYPE_MODEL))
  		                		   {
  		                			   sb = sb.append("\"../common/images/iconSmallProduct.gif\" border=\"0\"");
  		   		                   }
  		                		   else
  			    	    		   {
  		                			   sb = sb.append("\"../common/images/iconSmallMasterFeature.gif\" border=\"0\"");
  			   		               }
  		                		   sb = sb.append(strImageEnd)
  		                      		  	  .append(XSSUtil.encodeForXML(context,strCntxtMasterFeatName))
  		                      		  	  .append(strEndHref);
  		                		   vCntxtMastFeatValues.add(sb.toString());
  		                		   //END - Modified for bug no. IR-034765V6R2011
  		                	   }
  		                	   }
  		                	   else
  		    	    		   {
  		                		   vCntxtMastFeatValues.add(" "); 
  		    	    		   }
  		         	       } 
  		                }
  		                else
  	 	    		    {
  	             		   vCntxtMastFeatValues.add(" "); 
  	 	    		    }
  	                  }
  	                  else
   	    		      {
               		    vCntxtMastFeatValues.add(" "); 
   	    		      }
  		          }
  		      }
  			  else
  		      {
      		    vCntxtMastFeatValues.add(" "); 
  		      }
  		}
  		catch (Exception e)
  		{
  			// TODO: handle exception
  			throw new FrameworkException(e.getMessage());
  		}
  				return vCntxtMastFeatValues ;
  	}
      /**
       * This is a trigger method used to update the Revision Count attribute on the Master Feature Object, 
       * when the Feature Revision object is connected to Master Feature with Feature Revisions relationship. 
       * This method is called on the create trigger on "Feature Revisions" relationship.
       *  
       * @param Context context - Matrix Context Object
       * @param String args  - holds the FromID on the relationship which is Master Feature.
  	 * @return int iResult - 0 if operation is successful
  	 * 					   - 1 if operation fails.
       * @throws Exception
       * @author IVU
       * @since CFP R209
       */
      public int updateRevisionCountOnMasterFeature(Context context, String args[])
  	throws Exception {
      	int iResult = 0;
  		try {
  		    String strMasterFeatureId = args[0];
  			iResult = updateRevisionCount(context,strMasterFeatureId,ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION);
  		 } catch (Exception e) {
  			iResult = 1;
  		    throw new FrameworkException(e);
  		}
  		 return iResult;
      }
      
  	/**
       * This is a trigger method used to update the Revision Count attribute on the Master Feature Object, 
       * when the Feature Revision object is revised and connected to Master Feature with Feature Revisions relationship. 
       * This method is called on the ModifyTo trigger on "Feature Revisions" relationship.
       * 
       * @param Context context - Matrix Context Object
       * @param String args  - holds the FromID on the relationship which is Master Feature.
  	 * @return int iResult - 0 if operation is successful
  	 * 					   - 1 if operation fails.
  	 * @throws Exception
       * @author IVU
       * @since CFP R209
  	 */
      public int updateRevisionCountOnMasterFeatureModifyTo(Context context, String args[])
  	throws Exception {
      	int iResult = 0;
  		try {
  		    String strMasterFeatureId = args[0];
              iResult = updateRevisionCount(context,strMasterFeatureId,ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION);
  		 } catch (Exception e) {
  				iResult = 1;
  		    throw new FrameworkException(e);
  		}
  		 return iResult;
      }

      /**
       * This is a private method which does the actual setting of the Revision Count attribute on the Master Feature Object
       * 
       * @param Context context - Matrix Context Object
       * @param String args  - holds the FromID on the relationship which is Master Feature.
       * @param String strRelName - relationship name to find the number of objects connected through this relationship.
  	 * @return int iResult - 0 if operation is successful
  	 * 					   - 1 if operation fails.
       * @throws Exception
       * @author IVU
       * @since CFP R209
       */
      private int updateRevisionCount(Context context, String strObjId, String strRelName)
  	throws Exception {
      	int bResult = 0;
  		try {
  			// Query the DB to get the count by expanding the master feature object with Feaure Revision relationship.
  		    String strMasterFeatureId = strObjId;
  		    ContextUtil.pushContext(context, PropertyUtil
                      .getSchemaProperty(context, "person_UserAgent"),
                      DomainConstants.EMPTY_STRING,
                      DomainConstants.EMPTY_STRING);
  		   /*
  		    String strMQLQuery = "eval expr \"Count TRUE\"  on expand bus "+strObjId+" rel \""+strRelName+"\" dump";
  		    String strCount = MqlUtil.mqlCommand(context, strMQLQuery);
  		    */
  		      String strMQLQuery ="eval expr $1 on expand bus $2 rel $3 dump";
  		    String strCount=MqlUtil.mqlCommand(context,strMQLQuery,"Count TRUE",strObjId,strRelName);
  		    ContextUtil.popContext(context);

  		    DomainObject domParent = new DomainObject(strMasterFeatureId);
  		    domParent.setAttributeValue(context,ManufacturingPlanConstants.ATTRIBUTE_REVISION_COUNT, strCount);
  		 } catch (Exception e) {
  			 bResult = 1;
  		    throw new FrameworkException(e);
  		}
  		 return bResult;
      }
      /**
       * This method is called to show managed revisons in in search page when dbchooser is clicked.
       * @param context - Holds the matrix Context
       * @param args - holds model tempalte ID and context Product ID
       * @return StringList - retuns the IDs of managed revisions of model template
       * @throws Exception
       */
      @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
      public StringList getManagedSeriesForDB(Context context,String[] args)throws Exception
  	{
  		StringList managedRevisionsIdList = new StringList();
  		HashMap programMap = (HashMap) JPO.unpackArgs(args);
  		String strParentID = (String)  programMap.get("parentID");
  		String strProductId = (String)  programMap.get("contextBusId");

  		try
  		{
  			DomainObject domObj = new DomainObject(strParentID);
  			StringList objSelects = new StringList();
  			objSelects.addElement(DomainConstants.SELECT_ID);
  			StringList relSelects = new StringList();
  			StringBuffer stbRelName = new StringBuffer(50);
  	        StringBuffer stbTypeName = new StringBuffer(50);
  	        stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION);
              stbRelName.append(",");
              stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS);
              stbTypeName.append(ManufacturingPlanConstants.TYPE_PRODUCTS);
              stbTypeName.append(",");
              stbTypeName.append(ManufacturingPlanConstants.TYPE_LOGICAL_STRUCTURES);
              
              StringList strProductStructure = new DomainObject(strProductId).getInfoList(context,"from["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].torel.to.id");
              MapList mapList = domObj.getRelatedObjects(context, stbRelName.toString(), stbTypeName.toString(), objSelects, relSelects, false, true, (short)1, "", "", 0, null, null, null);
  			Map tempMap = new HashMap();
  			String strId = "";
  	        for(int i=0;i<mapList.size();i++)
  	          {
  	          	tempMap = (Map)mapList.get(i);
  	          	strId = (String)tempMap.get(DomainConstants.SELECT_ID);
  	          	if(!strProductStructure.contains(strId))
  	          	{
  	          	    managedRevisionsIdList.addElement(strId);
  	          	}
  	          }
  			
  		}
  		catch (Exception e) {
  			// TODO: handle exception
  			e.printStackTrace();
  		}

  		return managedRevisionsIdList;
  	}  
      /**
       * Method to delete the Master Feature Object when the last revision of the feature is deleted.
       * @param context
       * @param args
       * @return
       * @throws Exception
       * @author IVU
       * @since CFP R209
       */
      public int deleteMasterFeature(Context context, String args[])
  	throws Exception {
      	int iResult = 0;
  		try {
  		    String strFeatureId = args[0];
  		    if(strFeatureId!=null && !strFeatureId.equals("")){
  			    DomainObject dom = new DomainObject(strFeatureId);
  			    String strMasterFeatureId = dom.getInfo(context, "to["+ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION+"].from.id");
  			    if(strMasterFeatureId!=null && !strMasterFeatureId.equals("")){
 				 /*   String strMQLQuery = "eval expr \"Count TRUE\"  on expand bus "+strMasterFeatureId+" rel \""+ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION+"\" dump";
  				    String strCount = MqlUtil.mqlCommand(context, strMQLQuery);*/
  			    	String strMQLQuery="eval expr $1 on expand bus $2 rel $3 dump";
  			    	String strCount = MqlUtil.mqlCommand(context,strMQLQuery ,"Count TRUE",strMasterFeatureId,ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION);
  				    if(Integer.parseInt(strCount)==1){
  				    	DomainObject domMasterFeature = new DomainObject(strMasterFeatureId);
  				    	domMasterFeature.deleteObject(context);
  				    }
  			    }
  		    }
  		 } catch (Exception e) {
  				iResult = 1;
  		    throw new FrameworkException(e);
  		}
  		 return iResult;
      }


    /**
     * This method is used to display the Managed Revisions of the Master Feature 
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments
     * @return MapList - 
     * 			  Contains the Products, Features objects
     * @throws Exception
     *             if the operation fails
     * @author IVU
     * @since DMCPlanning R209
     * @grade 0
     */
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getManagedRevisions(Context context, String[] args)
    throws Exception {
    	
    	MapList relBusObjPageList =  new MapList();
	    try {
	        //Unpacking the args
	        HashMap programMap = (HashMap)JPO.unpackArgs(args);
	        //Gets the objectid and the relation names from args
	        String strObjectid = (String)programMap.get("objectId");
	        String filterExpression = (String)programMap.get("CFFExpressionFilterInput_OID");
	        
	        //get the Level and Limit 
	        int iLevel =ManufacturingPlan_mxJPO.getLevelfromSB(context,args);
	        int iLimit = ManufacturingPlan_mxJPO.getLimit(context,args);
	        
	        
	        // call the bean to get the Managed Revisions of the Master Feature.
	        MasterFeature masterFeatureBean = new MasterFeature(strObjectid);
	        relBusObjPageList = masterFeatureBean.getManagedRevisions(context,new StringList(), new StringList(),iLevel,iLimit,filterExpression);
	        
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
        return  relBusObjPageList;
    }

    /**
     * This method is called from the Model context to get the Master Features associated to Model
     * and the Managed Revisions of the corresponding Master Feature 
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments
     * @return MapList - 
     * 			Contain the Master Feature, Products, Feature
     * @throws Exception
     *             if the operation fails
     * @author IVU
     * @since DMCPlanning R209
     * @grade 0
     */
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMasterFeatureStructure(Context context, String[] args)
    throws Exception {
    	
    	MapList relBusObjPageList =  new MapList();
	    try {
	        //Unpacking the args
	        HashMap programMap = (HashMap)JPO.unpackArgs(args);
	        //Gets the objectid and the relation names from args
	        String strObjectid = (String)programMap.get("objectId");
	        
	        // form the Level and Limit filter for the query
	        int iLevel =ManufacturingPlan_mxJPO.getLevelfromSB(context,args);
	        int iLimit = ManufacturingPlan_mxJPO.getLimit(context,args);

	        // Add Relationship selectable to get the Feature Allocation Type attribute
	        // call the bean to get the Managed Revisions of the Master Feature.
	        StringList strLstRelSelect = new StringList(1);
	        strLstRelSelect.addElement("attribute["+ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
	        
	        MasterFeature masterFeatureBean = new MasterFeature(strObjectid);
	        relBusObjPageList = masterFeatureBean.getMasterFeatureStructure(context,new StringList(), strLstRelSelect,iLevel,iLimit);
	        
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
        return  relBusObjPageList;
    }
	
	/**
     * This is a public method, which call createAndConnectFeatureRevision to create the Logical Feature
 	 * and connect to the context Master Feature with Managed Revision Relationship.
     * @param context
     *          Holds the eMatrix <code>Context</code> object    
     * @return void
     * @throws Exception
     * @author WKU
     * @since CFP R211
     */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createLogicalFeature(Context context, String[] args)
	throws FrameworkException {
		try{
			//TODO - Need to clean up
		}
		catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

	}
	/**
	 * Used As Wrapper method for getMasterFeatureAndManagedRevisionStructure method
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments
	 * @return MapList of all Logical Feature objects
	 * @throws Exception
	 *             if operation fails
	 * @author WKU
	 * @since CFP R211
	 */

	public MapList expandLogicalFeatureStructure(Context context, String[] args)throws FrameworkException 
	{
		MapList featureList = new MapList();
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);

			//Get the context ID i.e. Model
			String strObjectID = (String)programMap.get("objectId");
			String viewReport = (String) programMap.get("viewReport");
			String viewEff = (String) programMap.get("viewEffectivity");
			String strParentID = (String)programMap.get("parentId");
			StringList strLstObjSelects = new StringList(1);
			StringList strLstRelSelects = new StringList(1);

			DomainObject domObjectID = new DomainObject(strObjectID);

			// form the Level and Limit filter for the query
			int iLevel =ManufacturingPlan_mxJPO.getLevelfromSB(context,args);
			int iLimit = ManufacturingPlan_mxJPO.getLimit(context,args);
			MasterFeature masterFeatureBean = new MasterFeature(strObjectID);

			if((viewEff!=null && !viewEff.equals("")))
			{
				if(strParentID!=null && !strParentID.equals(""))
				{
					featureList =masterFeatureBean.expandTechnicalStructure(context, strLstObjSelects, strLstRelSelects, "ManagedRevisions", iLevel, iLimit);

				}
				else
				{
					featureList =masterFeatureBean.expandTechnicalStructure(context, strLstObjSelects, strLstRelSelects, "MasterFeatureStructure", iLevel, iLimit);

				}
			}
			else if((viewReport!=null && !viewReport.equals("")))
			{	
				if(strParentID!=null && !strParentID.equals(""))
				{
					//featureList =masterFeatureBean.expandTechnicalStructure(context, strLstObjSelects, strLstRelSelects, "ManagedRevisions", iLevel, iLimit);

				}
				else
				{
					featureList =masterFeatureBean.expandTechnicalStructure(context, strLstObjSelects, strLstRelSelects, "ViewReport", iLevel, iLimit);

				}
			}

		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return featureList;
	}
       
    
	/**
	 * This method is used to for the Dynamic Column of Edit effectivity of Product Revisions
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the String args[]
	 * @return MapList - All the Product Revision
	 * @throws Exception if the operation fails
	 * @author WKU 
	 *  @since CFP R211
	 */
	public MapList getProductRevisionsWithSelectedFeatures(Context context, String args[])throws Exception
	{
		boolean isModel = false;
		String productRevision = "";
		String revisionName = "";
		String grpHeader = "";
		int countModel = 0;

		HashSet hashParentList = new HashSet();
		StringList strlistOtherParentPartList = new StringList();
		HashMap hash  = (HashMap) JPO.unpackArgs(args);
		String ObjectId = (String)hash.get("objectId");
		HashMap paramList = (HashMap) hash.get("requestMap");
		MapList ColumnsList = (MapList) hash.get("tableColumns");
		MapList objList = (MapList)hash.get("objectList");

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
		// Added for view Report
		String viewRep = (String)paramList.get("viewReport");
		String callFunc = "";
		boolean isReport = false;
		if(viewRep!=null && !("").equals(viewRep) && viewRep.equals("true"))
		{
			isReport = true;
			callFunc = "getDynamicColumnDataForReport";
		}

		MapList returnMap = new MapList();
		StringList strListSelectstmts = new StringList();
		strListSelectstmts.add(DomainObject.SELECT_ID);
		strListSelectstmts.add(DomainObject.SELECT_NAME);
		strListSelectstmts.add(DomainObject.SELECT_REVISION);
		String strParentPartName = "";
		try
		{
			//for(int k=0;k<objList.size();k++)
		//	{
			String strParentObjectId = (String)paramList.get("objectId");
			
				MapList mapChildParts = null;
				String objType = "";
				//Map aMap = (Map) objList.get(k);
				//String strParentObjectId = (String) aMap.get("id");

				DomainObject dom = new DomainObject(strParentObjectId);
				strParentPartName = dom.getInfo(context,"name");
				objType = dom.getInfo(context,"type");

				if(objType!=null && (mxType.isOfParentType(context, objType,ManufacturingPlanConstants.TYPE_MODEL)))
				{
					isModel = true;
					grpHeader = "DMCPlanning.EffectivityMatrix.GroupHeading.ProductRevisions";
					countModel++;
				}
				if(objType!=null && (isModel))
				{
					mapChildParts = dom.getRelatedObjects(context, 
							ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS, 
							ManufacturingPlanConstants.TYPE_PRODUCTS,
							strListSelectstmts,
							new StringList(),
							false,
							true,
							(short)0,strSelIdsWhrClause,"",0);
					if(mapChildParts.isEmpty())
					{
						String language = context.getSession().getLanguage();
						String strAlertMessage = EnoviaResourceBundle.getProperty(context, "Configuration",
						"emxProduct.Error.Model.NoProductRevision",language);
						emxContextUtil_mxJPO.mqlNotice(context,strAlertMessage);
						throw new FrameworkException(strAlertMessage);
					}

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
					String strOtherParentRev = (String) tempMap.get("revision");
					String strProdNameRev = strOtherParentName +"-"+ strOtherParentRev;
					String strProductVariantId = (String) tempMap.get("id"); 
					DomainObject domProductVariant = new DomainObject(strProductVariantId);

					HashMap hashfornewColumn = new HashMap();
					HashMap hashColumnSetting = new HashMap();
					hashColumnSetting.put("Column Type","program");
					hashColumnSetting.put("Registered Suite","DMCPlanning");
					hashColumnSetting.put("function",callFunc);
					hashColumnSetting.put("program","MasterFeature");
					hashColumnSetting.put("Export", "true");

					if(!isReport)
					{
						hashColumnSetting.put("Editable", "true");                   
						hashColumnSetting.put("Input Type", "combobox");
						hashColumnSetting.put("function", "getMasterFeatureUsage");
						hashColumnSetting.put("program", "MasterFeature");                        
						hashColumnSetting.put("Range Function", "getFeatureUsageRange");
						hashColumnSetting.put("Range Program", "MasterFeature");                  
						hashColumnSetting.put("Update Function", "updateFeatureUsage");
						hashColumnSetting.put("Update Program", "MasterFeature");
						hashColumnSetting.put("Edit Access Function", "isCellValueEditable");
						hashColumnSetting.put("Edit Access Program", "MasterFeature");
					} 

					String revisionId = "";

					if(isModel)
					{
						productRevision = domProductVariant.getInfo(context,DomainObject.SELECT_REVISION);
						revisionName = domProductVariant.getName();
						revisionId = domProductVariant.getId();

						if(!strlistOtherParentPartList.contains(strProdNameRev) && !strProdNameRev.equals(strParentPartName))
						{
							strlistOtherParentPartList.add(strOtherParentName);

							hashColumnSetting.put("Group Header",grpHeader);
							hashColumnSetting.put("Width","100");                        

							//Setting the global Column with the above settings
							hashfornewColumn.put("settings",hashColumnSetting);
							hashfornewColumn.put("label",strProdNameRev);
							hashfornewColumn.put("name",revisionId);
							hashfornewColumn.put("id",revisionId);

							returnMap.add(hashfornewColumn);
						}
					}
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
	   * This method is used to show name of Logical feature connected to Products in Model context
	   * @param context the eMatrix <code>Context</code> object
	   * @param args holds the String args[]
	   * @return List Usage values of Product Feature List
	   * @throws Exception if the operation fails
	   * @author WKU 
	   * @since CFP R211
	   */
	@Deprecated
	  public List getDynamicColumnDataForReport(Context context,String args[])throws Exception
	  {

		  matrix.util.List usageList = new StringList();
		  HashMap programMap = (HashMap) JPO.unpackArgs(args);
		  MapList objectList = (MapList) programMap.get("objectList");
		  HashMap paramList = (HashMap) programMap.get("paramList");

		  HashMap Columnshashmap = (HashMap) programMap.get("columnMap");
		  String strProdId = (String)Columnshashmap.get("id");

		  if(strProdId == null)
		  {
			  strProdId = (String)paramList.get("parentOID");
		  }
		  String strObjType = null;
		  String languageStr = context.getSession().getLanguage();
		  String strFeatureUsageStandard = "DMCPlanning.Range.FeatureUsage.Standard";
		  String strFeatureUsageOptional = "DMCPlanning.Range.FeatureUsage.Optional";
		 // String strFeatureUsageRequired = "DMCPlanning.Range.FeatureUsage.Mandatory";
		  String strFeatureStandard = ManufacturingPlanConstants.RANGE_VALUE_STANDARD;
		  String strFeatureOptional = ManufacturingPlanConstants.RANGE_VALUE_OPTIONAL;
		  //String strFeatureRequired = ManufacturingPlanConstants.RANGE_VALUE_REQUIRED;
		  String strFeatureUsageStandardDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
				  strFeatureUsageStandard,languageStr);
		  String strFeatureUsageOptionalDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
				  strFeatureUsageOptional,languageStr);
		//  String strFeatureUsageRequiredDisplay = i18nnow.GetString(
				//  "dmcplanningStringResource",
				//  languageStr,
				//  strFeatureUsageRequired);

		  MapList mapList = null;
		  int level = 0;
		  int limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxConfiguration.ExpandLimit"));
		  StringList slRelSelects = new StringList();
		  String strObjWhere = DomainObject.EMPTY_STRING;
		  String strTypePattern = ManufacturingPlanConstants.TYPE_LOGICAL_STRUCTURES+ "," + ManufacturingPlanConstants.TYPE_PRODUCTS;

		  String strRelPattern = ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES;
		  StringList slObjSelects = new StringList();
		  //Selectables on Type		
		  slObjSelects.addElement(ManufacturingPlanConstants.SELECT_NAME);
		  slObjSelects.addElement(ManufacturingPlanConstants.SELECT_REVISION);
		  slObjSelects.addElement("to["+ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION+"].from.id");
		  slObjSelects.addElement("to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].from.id");
		  //Selectables on Relationship
		  slRelSelects.addElement("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
		  ManufacturingPlanUtil utilObj = new ManufacturingPlanUtil(strProdId);
		  //Where clause on relationship
		  String strRelWhere = "tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id=="+ strProdId;

		  mapList = utilObj.getObjectStructure(context, strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
				  true, level, limit, strObjWhere, strRelWhere, (short)0,	null);
		  Object pflusageobject;
		  StringList pflusagelist = new StringList();
		  Object pflconnectedidsobject;
		  StringList pflconnectedidslist = new StringList();
		  HashMap feautureallusage = new HashMap();


		  for(int i=0; i<mapList.size();i++)
		  {
			  Map newmap = (Map)mapList.get(i);
			  String masterfeatureid = (String)newmap.get("to["+ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION+"].from.id");
			  if(masterfeatureid==null)
			  {
				  masterfeatureid = (String)newmap.get("to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].from.id");
			  }
			  String logicalfeaturename = (String)newmap.get(DomainConstants.SELECT_NAME);
			  String logicalfeaturerevision = (String)newmap.get(DomainConstants.SELECT_REVISION);


			  pflconnectedidsobject = (Object)newmap.get("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
					  "]."+DomainConstants.SELECT_FROM_ID);
			  pflconnectedidslist=	  ManufacturingPlanUtil.convertObjToStringList(context, newmap.get("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
					  "]."+DomainConstants.SELECT_FROM_ID));
//			  if(pflconnectedidsobject instanceof String){
//				  pflconnectedidslist.add(pflconnectedidsobject.toString());
//
//			  }else if(pflconnectedidsobject instanceof StringList){
//				  pflconnectedidslist= (StringList)pflconnectedidsobject;
//			  }

			  for(int j=0;j<pflconnectedidslist.size();j++)
			  {
				  String tempproductid = (String)pflconnectedidslist.get(j);
				  if(tempproductid.equalsIgnoreCase(strProdId))
				  {
					  String tempname = logicalfeaturename + " " + logicalfeaturerevision;
					  if(feautureallusage.containsKey(masterfeatureid)){
						  String keyValue=(String) feautureallusage.get(masterfeatureid);
						  String productValue=keyValue+","+tempname;
						  feautureallusage.put(masterfeatureid, productValue);
					  }
					  else{					
					  feautureallusage.put(masterfeatureid, tempname);
					  }
				  }
			  }


		  }

		  for (int i = 0; i < objectList.size(); i++)
		  {
			  Map tempMap = (Map) objectList.get(i);
			  String masterfeaturename = (String)tempMap.get(DomainConstants.SELECT_NAME);

			  String connectedid = (String)tempMap.get(DomainConstants.SELECT_ID);
			  String usage = (String)feautureallusage.get(connectedid);
			  if(usage==null)
			  {
				  usageList.addElement("");
			  }
			  else
			  {
				  String fullname = masterfeaturename + ":" + usage;
				  usageList.addElement(fullname);
			  }
		  }

		  return usageList;
	  }
  
	  /**
	   * This method is used to show Usage attribute values of Product Feature List relationship
	   * @param context the eMatrix <code>Context</code> object
	   * @param args holds the String args[]
	   * @return List Usage values of Product Feature List
	   * @throws Exception if the operation fails
	   * @author WKU 
	   * @since CFP R211
	   */

	  public List getMasterFeatureUsage(Context context, String[] args)throws Exception {

		  matrix.util.List usageList = new StringList();
		  HashMap programMap = (HashMap) JPO.unpackArgs(args);
		  MapList objectList = (MapList) programMap.get("objectList");
		  HashMap paramList = (HashMap) programMap.get("paramList");
		  HashMap Columnshashmap = (HashMap) programMap.get("columnMap");

		  try
		  {
			  String strProdId = (String)Columnshashmap.get("id");

			  if(strProdId == null)
			  {
				  strProdId = (String)paramList.get("parentOID");
			  }

			  String strObjType = null;
			  String languageStr = context.getSession().getLanguage();
			  String strFeatureUsageStandard = "DMCPlanning.Range.FeatureUsage.Standard";
			  String strFeatureUsageOptional = "DMCPlanning.Range.FeatureUsage.Optional";
			  //String strFeatureUsageRequired = "DMCPlanning.Range.FeatureUsage.Mandatory";
			  String strFeatureStandard = ManufacturingPlanConstants.RANGE_VALUE_STANDARD;
			  String strFeatureOptional = ManufacturingPlanConstants.RANGE_VALUE_OPTIONAL;
			  //String strFeatureRequired = ManufacturingPlanConstants.RANGE_VALUE_REQUIRED;
			  String strFeatureUsageStandardDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,strFeatureUsageStandard,languageStr);
			  String strFeatureUsageOptionalDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,strFeatureUsageOptional,languageStr);
			//  String strFeatureUsageRequiredDisplay = i18nnow.GetString(
					 // "dmcplanningStringResource",languageStr,strFeatureUsageRequired);

			  MapList mapList = null;
			  int level = 0;
			  int limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxConfiguration.ExpandLimit"));
			  String strObjWhere = DomainObject.EMPTY_STRING;
			  String strTypePattern = ManufacturingPlanConstants.TYPE_LOGICAL_STRUCTURES+ ","+ ManufacturingPlanConstants.TYPE_PRODUCTS;

			  String strRelPattern = ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES;
			  StringList slObjSelects = new StringList();
			  //Selectables on Type	
			  slObjSelects.addElement(ManufacturingPlanConstants.SELECT_NAME);
			  slObjSelects.addElement(ManufacturingPlanConstants.SELECT_REVISION);
			  slObjSelects.addElement("to["+ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES +"]"+
					  ".tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +
					  "].attribute["+ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
			  //Selectables on Relationship
			  StringList slRelSelects = new StringList();
			  slRelSelects.addElement("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
			  slRelSelects.addElement("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +
					  "].attribute["+ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
			  ManufacturingPlanUtil utilObj = new ManufacturingPlanUtil(strProdId);
			  //Where clause on relationship
			  String strRelWhere = "tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id=="+ strProdId;
			  mapList = utilObj.getObjectStructure(context, strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
					  true, level, limit, strObjWhere, strRelWhere, (short)0,	null);
			  Object pflusageobject;
			  
			  Object pflconnectedidsobject;
			  
			  HashMap feautureallusage = new HashMap();
			  for(int i=0; i<mapList.size();i++)
			  {
				  StringList pflusagelist = new StringList();
				  StringList pflconnectedidslist = new StringList();
				  Map newmap = (Map)mapList.get(i);
				  String logicalfeatureid = (String)newmap.get(DomainConstants.SELECT_ID);
				  pflusageobject = (Object)newmap.get("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST +
					  "].attribute["+ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
				  if(pflusageobject instanceof String){
					  pflusagelist.add(pflusageobject.toString());

				  }else if(pflusageobject instanceof StringList){
					  pflusagelist= (StringList)pflusageobject;
				  }

				  pflconnectedidsobject = (Object)newmap.get("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
						  "]."+DomainConstants.SELECT_FROM_ID);
				  if(pflconnectedidsobject instanceof String){
					  pflconnectedidslist.add(pflconnectedidsobject.toString());

				  }else if(pflconnectedidsobject instanceof StringList){
					  pflconnectedidslist= (StringList)pflconnectedidsobject;
				  }

				  for(int j=0;j<pflconnectedidslist.size();j++)
				  {
					  String tempproductid = (String)pflconnectedidslist.get(j);
					  if(tempproductid.equalsIgnoreCase(strProdId))
					  {
						  feautureallusage.put(logicalfeatureid, (String)pflusagelist.get(j));
					  }
				  }


			  }

			  for (int i = 0; i < objectList.size(); i++)
			  {
				  Map tempMap = (Map) objectList.get(i);
				  String connectedtype = (String)tempMap.get(DomainConstants.SELECT_TYPE);
				  String connectedid = (String)tempMap.get(DomainConstants.SELECT_ID);
				  DomainObject domobj = new DomainObject(connectedid);

				  if(domobj.isKindOf(context,ManufacturingPlanConstants.TYPE_LOGICAL_FEATURE)||
						  domobj.isKindOf(context, ManufacturingPlanConstants.TYPE_PRODUCTS))
				  {

					  String usage = (String)feautureallusage.get(connectedid);
					  if(usage!=null)
					  {
						  if(usage.equalsIgnoreCase(strFeatureStandard))
						  {
							  usageList.addElement(strFeatureUsageStandardDisplay);
						  }
						  if(usage.equalsIgnoreCase(strFeatureOptional))
						  {
							  usageList.addElement(strFeatureUsageOptionalDisplay);
						  }
						 /* if(usage.equalsIgnoreCase(strFeatureRequired))
						  {
							  usageList.addElement(strFeatureUsageRequiredDisplay);
						  }*/
					  }
					  else
					  {
						  usageList.addElement("");
					  }


				  }
				  else
				  {
					  usageList.addElement("");
				  }
			  }

		  }

		  catch(Exception e)
		  {
			  // TODO: handle exception
			  e.printStackTrace();			
		  }
		  return usageList;
	  }
     

	/**
	 * This method gets the range of values for the displayed
	 * Product Feature List  Usage feild.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a StringList containing the range option values to display
	 *         Product Feature List Usage combo box.
	 * @throws Exception
	 *             if the operation fails
	 * @author WKU 
	 * @since CFP R211
	 */

	public HashMap getFeatureUsageRange(Context context, String[] args)throws FrameworkException {

		HashMap returnMap = new HashMap();
		try
		{
			String languageStr = context.getSession().getLanguage();

			StringList strChoicesDisp = new StringList(3);
			String strFeatureUsageStandard = "DMCPlanning.Range.FeatureUsage.Standard";
			String strFeatureUsageOptional = "DMCPlanning.Range.FeatureUsage.Optional";
			//String strFeatureUsageRequired = "DMCPlanning.Range.FeatureUsage.Mandatory";

			String strFeatureUsageStandardDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,strFeatureUsageStandard,languageStr);
			String strFeatureUsageOptionalDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,strFeatureUsageOptional,languageStr);
			//String strFeatureUsageRequiredDisplay = i18nnow.GetString("dmcplanningStringResource",
					//languageStr,strFeatureUsageRequired);

			strChoicesDisp.add(strFeatureUsageStandardDisplay);
			strChoicesDisp.add(strFeatureUsageOptionalDisplay);
			//strChoicesDisp.add(strFeatureUsageRequiredDisplay);

			// combobox actual values
			StringList strChoices = new StringList(3);
			strChoices.add(strFeatureUsageStandardDisplay);
			strChoices.add(strFeatureUsageOptionalDisplay);
			//strChoices.add(strFeatureUsageRequiredDisplay);
			returnMap.put("field_choices", strChoices);
			returnMap.put("field_display_choices", strChoicesDisp);

		}
		catch (Exception e) 
		{
			// TODO: handle exception
			e.printStackTrace();
		}
		return returnMap;
	}

	/**
	 * This method is used to update the Prodcut Feature List Usage value
	 * @param context
	 *            The ematrix context object.
	 * @param String[]
	 *            The args .
	 * @return void
	 * @since CFP R211
	 */

	public void updateFeatureUsage(Context context, String[] args)
	throws Exception {

		try
		{

			String language=context.getSession().getLanguage();
			String strIntmdtFeatureListId = "";
			String strTempFeatureListFromRelID;
			String strParentId = null;
			String strParentFLId = null;
			String strParentFeatureSelType = null;
			String strParentType = null;
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap reqMap = (HashMap)programMap.get("requestMap");
			HashMap columnMap = (HashMap)programMap.get("columnMap");
			String strrelId = (String) paramMap.get("relId");
			String strNewValue = (String) paramMap.get("New Value");
			String strProdId =(String)reqMap.get("productID");
			String strRelName = null;
			String strFeatureAllocationType = ProductLineConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE;

			String logicalfeatureid = (String)paramMap.get("objectId");
			String productid =(String) columnMap.get("name");
			ManufacturingPlanUtil utilObj = new ManufacturingPlanUtil(logicalfeatureid);
			Object productidsObject;
			StringList productidsList =new StringList();
			Object pflidsObject;
			StringList pflidsList =new StringList();
			String[] pflidsArry = null;
			String pflid =null;
			//Type pattern
			String strTypePattern = ManufacturingPlanConstants.TYPE_HARDWARE_PRODUCT +","+ManufacturingPlanConstants.TYPE_PRODUCTS;
			//Relation Pattern
			String strRelPattern = ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES;
			StringList slObjSelects = new StringList();
			StringList slRelSelects = new StringList();
			slRelSelects.addElement("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");

			slRelSelects.addElement("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id");
			String strRelWhere = "from.id=="+ productid;

			MapList mapList = mapList = utilObj.getObjectStructure(context, strTypePattern, strRelPattern, slObjSelects, slRelSelects,
					true,false, (short)0, (short)0, null, strRelWhere, (short)0,	null);

			for(int i=0; i<mapList.size();i++)
			{
				Map newmp = (Map) mapList.get(i);
				productidsObject = (Object)newmp.get("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");

				if(productidsObject instanceof String){
					productidsList.add(productidsObject.toString());

				}else if(productidsObject instanceof StringList){
					productidsList= (StringList)(productidsObject);
				}

				pflidsObject = (Object)newmp.get("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id");

				if(pflidsObject instanceof String){
					pflidsList.add(pflidsObject.toString());

				}else if(pflidsObject instanceof StringList){
					pflidsList= (StringList)(pflidsObject);
				}

				for(int z=0; z<productidsList.size();z++)
				{
					String connectedproductid = (String)productidsList.get(z);
					if(connectedproductid.equalsIgnoreCase(productid))
					{
						pflid=(String)pflidsList.get(z);
					}
				}	    	   
			}
			DomainRelationship domainrelation = new DomainRelationship();
			domainrelation.setAttributeValue(context, pflid, strFeatureAllocationType, strNewValue);
		}
		catch (Exception e) 
		{
			// TODO: handle exception
			e.printStackTrace();
		}
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
   * @since CFP R211
   * @throws Exception
   */
  public static List isCellValueEditable(Context context, String[] args )throws Exception{

	  matrix.util.List usageList = new StringList();
	  HashMap programMap = (HashMap) JPO.unpackArgs(args);
	  MapList objectList = (MapList) programMap.get("objectList");
	  HashMap paramList = (HashMap) programMap.get("paramList");
	  HashMap Columnshashmap = (HashMap) programMap.get("columnMap");
	  try
	  {
		  String strProdId = (String)Columnshashmap.get("id");

		  if(strProdId == null)
		  {
			  strProdId = (String)paramList.get("parentOID");
		  }

		  String strObjType = null;
		  MapList mapList = null;
		  int level = 0;
		  int limit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxConfiguration.ExpandLimit"));
		  String strObjWhere = DomainObject.EMPTY_STRING;
		  StringBuffer sbType= new StringBuffer();
		  sbType.append(ManufacturingPlanConstants.TYPE_LOGICAL_STRUCTURES);
		  sbType.append(",");
		  sbType.append(ManufacturingPlanConstants.TYPE_PRODUCTS);
		  String strTypePattern = sbType.toString();
		  String strRelPattern = ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES;
		  StringList slObjSelects = new StringList();
		  String[] PLFidArry;
		  slObjSelects.addElement(ManufacturingPlanConstants.SELECT_NAME);
		  slObjSelects.addElement(ManufacturingPlanConstants.SELECT_REVISION);
		  StringList slRelSelects = new StringList();
		  slRelSelects.addElement("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
		  ManufacturingPlanUtil utilObj = new ManufacturingPlanUtil(strProdId);
		  String strRelWhere = "tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id=="+ strProdId;
		  mapList = utilObj.getObjectStructure(context, strTypePattern, strRelPattern, slObjSelects, slRelSelects, false,
				  true, level, limit, strObjWhere, strRelWhere, (short)0,	null);
		  Object pflusageobject;
		  StringList pflusagelist = new StringList();
		  Object pflconnectedidsobject;
		  StringList pflconnectedidslist = new StringList();
		  HashMap feautureallusage = new HashMap();


		  for(int i=0; i<mapList.size();i++)
		  {
			  Map newmap = (Map)mapList.get(i);
			  String logicalfeatureid = (String)newmap.get(DomainConstants.SELECT_ID);

			  pflconnectedidsobject = (Object)newmap.get("tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+
					  "]."+DomainConstants.SELECT_FROM_ID);
			  if(pflconnectedidsobject instanceof String){
				  pflconnectedidslist.add(pflconnectedidsobject.toString());

			  }else if(pflconnectedidsobject instanceof StringList){
				  pflconnectedidslist= (StringList)pflconnectedidsobject;
			  }

			  for(int j=0;j<pflconnectedidslist.size();j++)
			  {
				  String tempproductid = (String)pflconnectedidslist.get(j);
				  if(tempproductid.equalsIgnoreCase(strProdId))
				  {
					  feautureallusage.put(logicalfeatureid, "CONNECTED" );
				  }
			  }


		  }
		  //For Model
		  usageList.addElement(new Boolean(false));
		  for (int i = 1; i < objectList.size(); i++)
		  {
			  Map tempMap = (Map) objectList.get(i);
			  String connectedtype = (String)tempMap.get(DomainConstants.SELECT_TYPE);
			  String connectedid = (String)tempMap.get(DomainConstants.SELECT_ID);
			  if(mxType.isOfParentType(context, connectedtype, ManufacturingPlanConstants.TYPE_PRODUCTS) ||
					  mxType.isOfParentType(context, connectedtype, ManufacturingPlanConstants.TYPE_LOGICAL_STRUCTURES))
			  {

				  String usage = (String)feautureallusage.get(connectedid);
				  if(usage==null)
				  {
					  usageList.addElement(new Boolean(false));
				  }
				  else
				  {
					  usageList.addElement( new Boolean(true));
				  }
			  }
			  else
			  {
				  usageList.addElement(new Boolean(false));
			  }
		  }

	  }

	  catch(Exception e)
	  {
		  e.printStackTrace();
		  throw new FrameworkException(e.getMessage());
	  }
	  return usageList;

  }

/**
   * This is a public method used to create the Master Feature  and connect to the context Logical Feature with Managed Revision Relationship.
   * @param context
   * @param args
   * @author WKU
   * @since CFP R211
   * @throws FrameworkException
   */
  public void creationmasterfeature(Context context, String[] args)
  throws FrameworkException {
	  try{
		  String objectid = args[0];
		  String strCreateLogicalFeature = PropertyUtil.getGlobalRPEValue(context,"CreateLogicalFeatureMFContext");
		  String strCreateMasterFeatureLFContextInheritedTrigger = PropertyUtil.getGlobalRPEValue(context,
		  "CreateMasterFeatureLFContextInheritedTrigger");
		  if(strCreateLogicalFeature!=null && !"TRUE".equals(strCreateLogicalFeature) && 
				  strCreateMasterFeatureLFContextInheritedTrigger!=null && !"TRUE".equals(strCreateMasterFeatureLFContextInheritedTrigger))
		  {
			  PropertyUtil.setGlobalRPEValue(context,"CreateMasterFeatureLFContext","TRUE");
			  DomainObject domobject = new DomainObject(objectid);
			  StringList seletables = new StringList();
			  seletables.addElement(DomainConstants.SELECT_NAME);
			  seletables.addElement(DomainConstants.SELECT_DESCRIPTION);
			  seletables.addElement(DomainConstants.SELECT_VAULT);
			  //TODO unable to get Display Name 
			  seletables.addElement("attribute[" + ManufacturingPlanConstants.ATTRIBUTE_DISPLAY_NAME + "]");
			  seletables.addElement(ManufacturingPlanConstants.SELECT_OWNER);
			  Map mpLogicalFeatureselectables = domobject.getInfo(context, seletables);
			  
			  String strType = ManufacturingPlanConstants.TYPE_MASTER_FEATURE;
			  String strName = (String)mpLogicalFeatureselectables.get(DomainConstants.SELECT_NAME);
			  String strRevision = "";
			  String strDescription = (String)mpLogicalFeatureselectables.get(DomainConstants.SELECT_DESCRIPTION);

			  String strPolicy = ManufacturingPlanConstants.POLICY_MANAGED_SERIES;
			  String strVault = (String)mpLogicalFeatureselectables.get(DomainConstants.SELECT_VAULT);
			  //Attributes
			  String strMarketName = (String)mpLogicalFeatureselectables.get(ManufacturingPlanConstants.ATTRIBUTE_DISPLAY_NAME);
			  HashMap AttributeMap = new HashMap();
			  AttributeMap.put(ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME, strMarketName);
			  //Owner
			  String strOwner = (String)mpLogicalFeatureselectables.get(ManufacturingPlanConstants.SELECT_OWNER);
			  //Relationship
			  String relManagedSeries = ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION;
			  String strModelId = "";
			  HashMap hmManagedRevisionDetails = new HashMap();
			  MasterFeature masterFeature = new MasterFeature(objectid);
			  String strMasterrFeatureID = masterFeature.create(context,  strModelId,  strType,  strName,
					  strRevision,  strPolicy,  strVault,  strOwner,  strDescription,
					  AttributeMap,  hmManagedRevisionDetails);
			  //Connecting Logical And Master Feature
			  com.matrixone.apps.domain.DomainRelationship.
			  			connect(context,strMasterrFeatureID,ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION,objectid,false);
			  
		  }
	  }
	  catch (Exception e) {
		  throw new FrameworkException(e.getMessage());
	  }

  }


  /**
   * This is a public method used to create the Logical Feature  and connect to the context Master Feature with Managed Revision Relationship.
   * @param context
   *          Holds the eMatrix <code>Context</code> object
   * @param args[0]
   *          Holds Master Feature Id
   * @throws Exception
   * @author WKU
   * @since CFP R211
   */
  public void triggerToCreateLogicalFeature(Context context, String[] args) 
  throws Exception{
	  //TODO remove Buisness referececes as well
  }
  /**
   * This is a public method used to display list of Policies of selected type in create page of Master Feature .
   * It is Reload program.
   * @param context
   *          Holds the eMatrix <code>Context</code> object
   * @param args
   * @throws Exception
   * @author WKU
   * @since CFP R211
   */
  public HashMap getLogicalFeaturePolicies(Context context, String[] args) 
  throws Exception{

	  HashMap programMap = (HashMap) JPO.unpackArgs(args);
	  HashMap fieldValuesMap = (HashMap)programMap.get("fieldValues"); 	
	  String featuretype = (String)fieldValuesMap.get("LogicalFeatureType");
	  Map fieldMap = (HashMap) programMap.get("fieldMap");
	  String strFieldName = (String)fieldMap.get("name");

	  HashMap policyMap = new HashMap();
	  MapList featurepolicy = new MapList();
	  String language = context.getSession().getLanguage();
	  String strDisplayLogicalFeature ="";
	  String strActualLogicalFeature = "";
	  StringBuffer sbBuffer  = new StringBuffer();
	  featurepolicy = com.matrixone.apps.domain.util.mxType.getPolicies(context, featuretype, false);
	  for(int i=0; i< featurepolicy.size();i++)
	  {
		  Map map = (Map)featurepolicy.get(i);
		  String strPolicy = (String)map.get(DomainConstants.SELECT_NAME);
		  //Logical Feature		  
		  if(strPolicy.equalsIgnoreCase(ManufacturingPlanConstants.POLICY_LOGICAL_FEATURE))
		  {
			  strDisplayLogicalFeature = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Policy.Logical_Feature",language);
			  strActualLogicalFeature = ManufacturingPlanConstants.POLICY_LOGICAL_FEATURE;
		  }	
		  else{
			  strDisplayLogicalFeature=strPolicy;
			  strActualLogicalFeature=strPolicy;
		  }
		  //Customized Policy
		  sbBuffer.append("<select style=\"\" ");
		  sbBuffer.append("name=\"");
		  sbBuffer.append(strFieldName);      
		  sbBuffer.append("\">");
		  sbBuffer.append("<option selected=\"selected\"");
		  sbBuffer.append(" value=\"");
		  sbBuffer.append(strActualLogicalFeature);
		  sbBuffer.append("\">");
		  sbBuffer.append(strDisplayLogicalFeature);
		  sbBuffer.append("</option>");
		  sbBuffer.append("</select>");

	  }


	  policyMap.put("SelectedValues",sbBuffer.toString());
	  policyMap.put("SelectedDisplayValues",sbBuffer.toString());

	  return policyMap;
	  }
  
  /**
   * This is a public method used to display list of Policies of selected type in create page of Master Feature .
   * @param context
   *          Holds the eMatrix <code>Context</code> object
   * @param args
   * @throws Exception
   * @author WKU
   * @since CFP R211
   */
  
  public String logicalFeaturePolicyHTML(Context context, String[] args)
  throws Exception { 
	  
	  HashMap programMap = (HashMap) JPO.unpackArgs(args);
	  String language = context.getSession().getLanguage();
	  String strDisplayLogicalFeature = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Policy.Logical_Feature",language);
	  String strActualLogicalFeature = ManufacturingPlanConstants.POLICY_LOGICAL_FEATURE;
	  Map fieldMap = (HashMap) programMap.get("fieldMap");
      String strFieldName = (String)fieldMap.get("name");
	  StringBuffer sbBuffer  = new StringBuffer();
	  sbBuffer.append("<select style=\"\" ");
      sbBuffer.append("name=\"");
      sbBuffer.append(strFieldName);      
      sbBuffer.append("\">");
      sbBuffer.append("<option selected=\"selected\"");
      sbBuffer.append(" value=\"");
      sbBuffer.append(strActualLogicalFeature);
      sbBuffer.append("\">");
      sbBuffer.append(strDisplayLogicalFeature);
      sbBuffer.append("</option>"); 
      sbBuffer.append("</select>");
	  return sbBuffer.toString();
      
  }
/**
   * This is a public method used to create the Master Feature  and connect to the context Software Feature with Managed Revision Relationship.
   * @param context
   * @param args
   * @author WKU
   * @since CFP R211
   * @throws FrameworkException
   */
  public void creationOfMasterFeatureInheritedTrigger(Context context, String[] args)
  throws FrameworkException {
	  try{
		  String objectid = args[0];
		  String strCreateMasterFeatureLFContext = PropertyUtil.getGlobalRPEValue(context,"CreateMasterFeatureLFContext");
		
		  if(strCreateMasterFeatureLFContext!=null && !"TRUE".equals(strCreateMasterFeatureLFContext))
		  {
			  PropertyUtil.setGlobalRPEValue(context,"CreateMasterFeatureLFContext","TRUE");
			  PropertyUtil.setGlobalRPEValue(context,"CreateMasterFeatureLFContextInheritedTrigger" , "TRUE");
			  DomainObject domobject = new DomainObject(objectid);
			  StringList seletables = new StringList();
			  seletables.addElement(DomainConstants.SELECT_NAME);
			  seletables.addElement(DomainConstants.SELECT_DESCRIPTION);
			  seletables.addElement(DomainConstants.SELECT_VAULT);
			  //TODO unable to get Display Name 
			  seletables.addElement("attribute[" + ManufacturingPlanConstants.ATTRIBUTE_DISPLAY_NAME + "]");
			  seletables.addElement(ManufacturingPlanConstants.SELECT_OWNER);
			  Map mpLogicalFeatureselectables = domobject.getInfo(context, seletables);
			  
			  String strType = ManufacturingPlanConstants.TYPE_MASTER_FEATURE;
			  String strName = (String)mpLogicalFeatureselectables.get(DomainConstants.SELECT_NAME);
			  String strRevision = "";
			  String strDescription = (String)mpLogicalFeatureselectables.get(DomainConstants.SELECT_DESCRIPTION);

			  String strPolicy = ManufacturingPlanConstants.POLICY_MANAGED_SERIES;
			  String strVault = (String)mpLogicalFeatureselectables.get(DomainConstants.SELECT_VAULT);
			  //Attributes
			  String strMarketName = (String)mpLogicalFeatureselectables.get(ManufacturingPlanConstants.ATTRIBUTE_DISPLAY_NAME);
			  HashMap AttributeMap = new HashMap();
			  AttributeMap.put(ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME, strMarketName);
			  //Owner
			  String strOwner = (String)mpLogicalFeatureselectables.get(ManufacturingPlanConstants.SELECT_OWNER);
			  //Relationship
			  String relManagedSeries = ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION;
			  String strModelId = "";
			  HashMap hmManagedRevisionDetails = new HashMap();
			  MasterFeature masterFeature = new MasterFeature(objectid);
			  String strMasterrFeatureID = masterFeature.create(context,  strModelId,  strType,  strName,
					  strRevision,  strPolicy,  strVault,  strOwner,  strDescription,
					  AttributeMap,  hmManagedRevisionDetails);
			  //Connecting Logical And Master Feature
			  com.matrixone.apps.domain.DomainRelationship.
			  			connect(context,strMasterrFeatureID,ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION,objectid,false);
			  
		  }
	  }
	  catch (Exception e) {
		  throw new FrameworkException(e.getMessage());
	  }

  }
}




