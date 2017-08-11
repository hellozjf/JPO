/*
 * emxModelBasedUEBOMMigrationBase.java
 * program migrates Existing PUEECO and its appicability attribute to NE object and set the expression
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.util.*;  

import matrix.db.*;
import matrix.util.*;  

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.effectivitymigration.EffectivityFramework;
import com.matrixone.apps.unresolvedebom.UnresolvedEBOMConstants;

  public class emxModelBasedUEBOMMigrationBase_mxJPO  extends emxCommonMigration_mxJPO {
 	  EffectivityFramework _effectivityFramework = new EffectivityFramework();
      private static final String KEY_PREFIX = "@EF_UT(PHY@EF:";
      private static final String KEY_SUFFIX = "])";

	  private String _actualValue = EffectivityFramework.ACTUAL_VALUE;
	  Iterator _itr; String _sProdObjId;String _modelPhyId;String _applValue;
 	  
      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxModelBasedUEBOMMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
      }



      /**
       * This method does the migration work.  Existing PUEECO and its 
       * applicability attribute will migrated to NE object and the expression
       * will be set for 2011x env.
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      @SuppressWarnings("deprecation")
	public void  migrateObjects(Context context, StringList objectIdList) throws Exception {
    	  
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
    	  
          if (scan) {
              return;
          }
          String RELATIONSHIP_EBOMPENDING = PropertyUtil.getSchemaProperty(context,"relationship_EBOMPending");
          String RELATIONSHIP_NAMED_EFFECTIVITY_USAGE = PropertyUtil.getSchemaProperty(context,"relationship_NamedEffectivityUsage");
          
          StringList wipBOMIdList = new StringList ();
          
          int listSize = (objectIdList == null) ? 0 : objectIdList.size();
          
          if (listSize <= 0) 
        	  return;
          
        	 ContextUtil.startTransaction(context, true);        	   
        	  
		try {
			MapList listEBOMAndEBOMPendingData;
			MapList allEBOMRelIds = new MapList();
			MapList allEBOMOrEBOMPendingRelIds = new MapList();

			HashSet uniqueObjectIdsSet = new HashSet();
			
			
			DomainObject domObj;

			String relationship = UnresolvedEBOMConstants.RELATIONSHIP_EBOM
					+ "," + RELATIONSHIP_EBOMPENDING;
			String objectId;

			StringList relSelect = new StringList(3);
			relSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
			relSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);

			Iterator iterator;
			Map relMap;
			String relId;
			String relName;
			String isContextChangeCase;

			for (int i = 0; i < listSize; i++) {

				objectId = (String) objectIdList.get(i);
				
				if(!uniqueObjectIdsSet.add(objectId))
					 	continue;
				
				domObj = DomainObject.newInstance(context, objectId);

				listEBOMAndEBOMPendingData = domObj.getRelatedObjects(context,
											 relationship, DomainConstants.QUERY_WILDCARD, null,
											 relSelect, false, true, (short) 1, null, null, null,
											 null, null);

				iterator = listEBOMAndEBOMPendingData.iterator();
				while (iterator.hasNext()) {
					relMap = (Map) iterator.next();

					relId = (String) relMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
					relName = (String) relMap.get(DomainConstants.SELECT_RELATIONSHIP_NAME);

					isContextChangeCase = MqlUtil.mqlCommand(context,
										  "print connection $1 select $2 dump",
										  relId, "frommid[" + RELATIONSHIP_NAMED_EFFECTIVITY_USAGE + "]");

					
					if (relName.equals(UnresolvedEBOMConstants.RELATIONSHIP_EBOM)) {
						// if Config BOM created through normal change process,
						// goes in this loop and adds rel id into EBOM as well
						// as EBOMPending MapList
						if ("True".equalsIgnoreCase(isContextChangeCase)) {
							addRelIdToMapList(allEBOMRelIds, relId);
							addRelIdToMapList(allEBOMOrEBOMPendingRelIds, relId);
						}
						// For all WIP BOM related rel IDs goes in this loop
						else {
							wipBOMIdList.addElement(relId);
						}
					} else {
						addRelIdToMapList(allEBOMOrEBOMPendingRelIds, relId);
					}
					loadMigratedOids(objectId);
				}
			}

			if (allEBOMRelIds.size() > 0) {
				_effectivityFramework.refreshCurrentRelEffectivity(context,
						allEBOMRelIds);
			}

			if (allEBOMOrEBOMPendingRelIds.size() > 0) {
				_effectivityFramework.setChangeId("");
				int idx = 0;
				for (; idx < allEBOMOrEBOMPendingRelIds.size(); idx++) {
					EffectivityFramework.setRelEffectivityTypes(context,
										(String) ((Map) allEBOMOrEBOMPendingRelIds.get(idx)).get(DomainConstants.SELECT_RELATIONSHIP_ID),
										 "Unit");
				}
				_effectivityFramework.refreshProposedEffectivity(context,allEBOMOrEBOMPendingRelIds);
			}
			// handles the wip bom migration from here onwards
			if (wipBOMIdList.size() > 0) {
				doWipBOMMigration(context, wipBOMIdList);
			}
			ContextUtil.commitTransaction(context);
		} catch (Exception ex) {
			ContextUtil.abortTransaction(context);
			ex.printStackTrace();
			throw ex;
		}
	}  	      	  
 /**This method does the WIP BOM Migration for the given stringlist of rel ids
  * 
  * @param context eMatrix code context objet
  * @param wipBOMIdList StringList of rel ids whose undergone wip bom
  * @throws Exception if the operation fails
  */
   private void doWipBOMMigration(Context context, StringList wipBOMIdList) throws Exception {
	   
	   MapList exprMapList = new MapList();
	   
	   String relId; HashMap exprMap; HashMap prodAppMap; String actualExpr; String sProdObjId; String modelExpr;
	   HashMap<String,String> modelAppMap = new HashMap<String,String>();
	   
 	   String RELATIONSHIP_PRODUCTS  = PropertyUtil.getSchemaProperty(context,"relationship_Products");
	   String SELECT_MODEL_ID = "to[" + RELATIONSHIP_PRODUCTS + "].from.physicalid";
	   
	   for (int idx =0;idx<wipBOMIdList.size();idx++) {
       
		   relId = wipBOMIdList.get(idx).toString();
		   exprMapList = _effectivityFramework.getRelExpression(context, relId);
       
		    mqlLogWriter ( "Effectivity Maplist with for the given REL ID " + exprMapList + "\n");
              
     	    exprMap    = (HashMap) exprMapList.get(0);
     	    actualExpr = (String)exprMap.get(_actualValue);            	  
 		    prodAppMap = (HashMap)_effectivityFramework.getExpressionSequence(context, actualExpr);
 		  
     		  // This Loop prepares Models and Effectivities as key/value pairs for a given expression
			  _itr  = prodAppMap.keySet().iterator();
			  while (_itr.hasNext()) {
				    sProdObjId 	= (String)_itr.next();
				    _modelPhyId	= (String)DomainObject.newInstance(context, sProdObjId).getInfo(context, SELECT_MODEL_ID);
				    _applValue   = (String)prodAppMap.get(sProdObjId);
			    	//if expression involves two product revisions from same model id then make union of those applicabilities
			    	if(modelAppMap.containsKey(_modelPhyId)) {
			    		_applValue = (String)JPO.invoke(context, "emxModelBasedPUEMigrationBase", null, "getUnionOfApplicabilities", new String[]{modelAppMap.get(_modelPhyId), _applValue}, String.class);
			    	}
			    	modelAppMap.put(_modelPhyId, _applValue);
			       }
 			  		//Get the Required actual expression for a Model Effectivity Map
			  		  modelExpr = getModelBasedExpression(modelAppMap);
			  		 _effectivityFramework.setRelExpression(context, relId, modelExpr);
			    	 modelAppMap = new HashMap();
          }             

	}

   /**
    * Prepares a CFF based expression with Model physical id and with corresponding effectivity
    * @param modelAppMap contains Model physical id and effectivity  as key value pairs
    * @return CFF actual expression format contains Model id involved
    */
 	  private String getModelBasedExpression(HashMap modelAppMap) throws Exception {
 		 mqlLogWriter ( "A Model Applicability Map " + modelAppMap + "Results in an Expression");
 	  	 StringBuffer exprBuffer = new StringBuffer();
 	  	_itr  = modelAppMap.keySet().iterator();
 	  	 while (_itr.hasNext()) {
 	  		_modelPhyId = (String)_itr.next();
 	  		_applValue = (String)modelAppMap.get(_modelPhyId);
 	  		 exprBuffer = exprBuffer.length() > 0 ?exprBuffer.append(" ").append("OR").append(" "):exprBuffer;
 	  		 exprBuffer.append(KEY_PREFIX)
 	  		 		   .append(_modelPhyId)
 	  		 		   .append('[')
 	  		 		   .append(_applValue)
 	  		 		   .append(KEY_SUFFIX);
 	  	 }
 	  	mqlLogWriter (exprBuffer.toString()+"\n");
 		return exprBuffer.toString();
 		
 	}  


private void addRelIdToMapList(MapList addEBOMOrEBOMPendingRelId, String relId) {
			  Map map = new HashMap(1);
			  map.put(DomainConstants.SELECT_RELATIONSHIP_ID, relId);
			  addEBOMOrEBOMPendingRelId.add(map);
    	  }

    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" ECO Migration is a two step process  \n");
        writer.write(" Step1: Find parent part objects which are involved in EBOM/EBOM Pending relationships and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringConfiguredPartMigrationFindObjects 1000 Part C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxModelBasedUEBOMMigrationBase 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
