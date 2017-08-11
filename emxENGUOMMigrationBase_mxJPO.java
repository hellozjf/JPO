
/*
 * emxENGUOMMigrationBase.java
 * Program to update the Unit of Measure Type and values on Parts and 
 * EBOM, EBOM Pending, EBOM History, EBOM Substiture relationships
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.*;
import matrix.util.*;

import java.util.*;
import java.util.Set;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.framework.ui.UIUtil;


public class emxENGUOMMigrationBase_mxJPO extends emxCommonMigration_mxJPO
{

	boolean firstTime = true;
	String ATTRIBUTE_UOM_TYPE = PropertyUtil.getSchemaProperty("attribute_UOMType");
	String ATTRIBUTE_UOM_VALUE = PropertyUtil.getSchemaProperty("attribute_UnitofMeasure");//DomainConstants.ATTRIBUTE_UNITOFMEASURE;
	
	String SELECT_ATTRIBUTE_UOM_TYPE = "attribute["+ATTRIBUTE_UOM_TYPE+"].value";
	String SELECT_ATTRIBUTE_UOM_VALUE = "attribute["+ATTRIBUTE_UOM_VALUE+"].value";
	String TYPE_PART=PropertyUtil.getSchemaProperty("type_Part");
	//String SELECT_REL_EBOM_SUBSTITUTE_REL_ID = "frommid[EBOM Substitute].id";
	String SELECT_EBOM_SUBSTITUTE_REL_ID = "to[EBOM Substitute].id";
	
	String WILD_CARD_ASTERESK = "*";
	String ATTRIBUTE_WHERE_UOM_TYPE_PROPORTION = "attribute["+ATTRIBUTE_UOM_TYPE+"] == Proportion";

	
	/**
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @grade 0
	 */
	public emxENGUOMMigrationBase_mxJPO (Context context, String[] args)
	throws Exception
	{
		
		super(context, args);
	}

	
	/**
	 *
	 * Method to retrieve relIDs - EBOM, EBOM Pending, EBOM History, EBOM Substitute for each object Id
	 * Then update the UOM value on these relationships
	 * @param context the eMatrix <code>Context</code> object
	 * @param MapList MapList containing information related to relationship and their Ids
	 * @param sUOMValue The UOM Value that need to be updated
	 * @throws Exception if the operation fails
	 * @grade 0
	 */
	
	
	private void updateRelsWithUOMValue(Context context, MapList mlAllEBOMRels, String sUOMValue) throws Exception
	{
		
		try{
			
			Iterator itr = mlAllEBOMRels.iterator();
			
			while(itr.hasNext())
			{
				Map dataMap = (Map)itr.next();
				String sRelId = (String)dataMap.get(DomainRelationship.SELECT_ID);
				new DomainRelationship(sRelId).setAttributeValue(context, ATTRIBUTE_UOM_VALUE, sUOMValue);
				
				String sEBOMSubsRel = (String)dataMap.get(SELECT_EBOM_SUBSTITUTE_REL_ID);
				if(null != sEBOMSubsRel && !"".equals(sEBOMSubsRel))
					new DomainRelationship(sEBOMSubsRel).setAttributeValue(context, ATTRIBUTE_UOM_VALUE, sUOMValue);
				
			}
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		
	}
	
	
	/**
	 * This method does the migration work.  Get the corresponding UOM types based on 
	 * UOM Values for old date. Update the UOM Type based on UOM Values on Part objects and EBOM relationships. 
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectIdList StringList holds list objectids to migrate
	 * @returns nothing
	 * @throws Exception if the operation fails
	 */
	@SuppressWarnings("unchecked")
	public void  migrateObjects(Context context, StringList objectIdList) throws Exception {

		if (scan) {
			return;
		}

		int listSize = (objectIdList == null) ? 0 : objectIdList.size();
 		if (listSize < 0) 
 			return;
 		
 		Map<String, String> UOMValuesAndTypesMap = new HashMap<String, String>();
		UOMValuesAndTypesMap = EngineeringUtil.getUOMTypesForValues(context);
 		
		ContextUtil.startTransaction(context, true);
		try {
			String partObjId = null;
			for (int i = 0; i < listSize; i++) {

				partObjId = (String)objectIdList.get(i);
				DomainObject domObj = DomainObject.newInstance(context, partObjId);
				
				String sUOMValue = domObj.getInfo(context, SELECT_ATTRIBUTE_UOM_VALUE);
				StringList relSelects = new StringList();
				relSelects.add(DomainRelationship.SELECT_ID);
				relSelects.add(SELECT_ATTRIBUTE_UOM_VALUE);
				relSelects.add(SELECT_EBOM_SUBSTITUTE_REL_ID);
				//relSelects.add("to[EBOM Substitute].id");
				
				Pattern relPattern = new Pattern("EBOM"); 
				relPattern.addPattern("EBOM Pending");
				relPattern.addPattern("EBOM History");
				
				MapList mlAllEBOMRels = domObj.getRelatedObjects(context, 
																relPattern.getPattern(), 
																TYPE_PART, 
																null, 
																relSelects, 
																true, 
																false, 
																(short) 1, 
																null, 
																null, 
																0);
				
				
				
				String sUOMType = UOMValuesAndTypesMap.get(sUOMValue);
				if(UIUtil.isNullOrEmpty(sUOMType)){
					writeUnconvertedOID( "The Unit of Measure value for the object ID "+partObjId+" has not been mapped to any UOM Type",partObjId);
				}
				if(!"Proportion".equals(sUOMType) && UIUtil.isNotNullAndNotEmpty(sUOMType)){
					sUOMType=sUOMType.trim();
					domObj.setAttributeValue(context, ATTRIBUTE_UOM_TYPE, sUOMType);
					loadMigratedOids(partObjId);
				}
					
				updateRelsWithUOMValue(context, mlAllEBOMRels, sUOMValue);
				
			}
			ContextUtil.commitTransaction(context);

		}catch(Exception ex)
		{
			ContextUtil.abortTransaction(context);
			ex.printStackTrace();
			throw ex;
		}
	}
}

