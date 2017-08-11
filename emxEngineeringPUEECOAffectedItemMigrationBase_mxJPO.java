/*
 * emxEngineeringPUEECOAffectedItemMigrationBase.java
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



import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.framework.ui.UIUtil;

  public class emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO  extends emxCommonMigration_mxJPO {
      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
	  
	  
      public emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO (Context context, String[] args)
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
      public void  migrateObjects(Context context, StringList objectIdList) throws Exception {
    	  
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
    	  
          if (scan) {
              return;
          }
          
          int listSize = (objectIdList == null) ? 0 : objectIdList.size();
          
          if (listSize > 0) {
        	  ContextUtil.startTransaction(context, true);
	         try {
	        	          	 
	        	  StringList strECOIdList = new StringList();
	        	  
	        	  String state_Release = EngineeringConstants.STATE_ECO_RELEASE;
        		  String state_Implemented = EngineeringConstants.STATE_ECO_IMPLEMENTED;
        		  String state_Cancelled = EngineeringConstants.STATE_ECO_CANCELLED;	              
	              
	              Iterator itr = objectIdList.iterator();
	              Map affMap;
	              MapList affItems;
	              
	              while (itr.hasNext()) {
		        	  String cecoId = (String) itr.next();
					  DomainObject dObj =  DomainObject.newInstance(context,cecoId);
					  String current = dObj.SELECT_CURRENT;
					
					  if ((current.equals(state_Release)) || (current.equals(state_Implemented)) || (current.equals(state_Cancelled))) {
						writeUnconvertedOID( " The Unconverted CECO's are: "+cecoId,cecoId);
						continue;
					  }
						
	            	  String strRelWhere = "attribute[" + EngineeringConstants.ATTRIBUTE_REQUESTED_CHANGE + "] == 'For Revise'";
	
				      affItems =dObj.getRelatedObjects(context, 
								EngineeringConstants.RELATIONSHIP_AFFECTED_ITEM, 
								"*", 
								null, 
								new StringList(DomainConstants.SELECT_ID), 
								false, true, (short)1, null,strRelWhere, 0);

  					 if (affItems.size()>0) {
			            Iterator affItemsItr = affItems.iterator();
						while(affItemsItr.hasNext()){
							affMap = (Map) affItemsItr.next();
							String relId = (String)affMap.get(DomainConstants.SELECT_ID);
							if(UIUtil.isNotNullAndNotEmpty(relId)){
								DomainRelationship.newInstance(context,relId).setAttributeValue(context, DomainConstants.ATTRIBUTE_REQUESTED_CHANGE, "For Update");
								strECOIdList.add(cecoId);
							}
						}
					}
							
					else{
						writeUnconvertedOID( " The Unconverted CECO's are: "+cecoId,cecoId);
					}
	              }

		          loadMigratedOidsList(strECOIdList);
	              ContextUtil.commitTransaction(context);
	          } catch (Exception ex) {
	        	  ContextUtil.abortTransaction(context);
	              ex.printStackTrace();
	              throw ex;
	          }
          }
      }
      private void loadMigratedOidsList (StringList objectIdList) throws Exception
      {
          Iterator itr = objectIdList.iterator();
          String objectId=null;
          while (itr.hasNext())
          {
              objectId = (String) itr.next();
              loadMigratedOids(objectId);
          }
      }
    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" ECO Migration is a two step process  \n");
        writer.write(" Step1: Find PUE ECO objects with policy PUE ECO and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringPUEECOAffectedItemMigrationFindObjects 1000 'PUE ECO' C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringPUEECOAffectedItemMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
