/*
 ** emxModelBasedTopLevelPartMigrationBase.java
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.*;  
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

  public class emxModelBasedTopLevelPartMigrationBase_mxJPO	extends emxCommonMigration_mxJPO
  {
      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxModelBasedTopLevelPartMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
      }

      /**
       * This method does the migration work.  All the assigned part relationships between Product
       * and the Top Level part gets replaced by corresponding Model and the Top Level part.       
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      @SuppressWarnings({ "rawtypes", "unchecked" })
	public void  migrateObjects(Context context, StringList objectIdList)
                                                          throws Exception
      {
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }
          String RELATIONSHIP_ASSIGNED_PART   = PropertyUtil.getSchemaProperty(context,"relationship_AssignedPart");
          String RELATIONSHIP_PRODUCTS   = PropertyUtil.getSchemaProperty(context,"relationship_Products");

          String SELECT_RELATIONSHIP_ASSIGNED_PART_ID = "from[" + RELATIONSHIP_ASSIGNED_PART + "].id";
          String SELECT_MODEL_ID = "to[" + RELATIONSHIP_PRODUCTS + "].from.id";
          String SELECT_NAME     = "name";
          String SELECT_REVISION = "revision";
          String SELECT_ID       = "id";
          
          MapList mapList;
          String  AssignedPartRel;
          String  ModelObjID;
          String productName;
          String productRevision;
          String prodObjID;
          
          
          try
          {
        	  ContextUtil.startTransaction(context, true);
              StringList objectSelects    = new StringList(6);

              objectSelects.add(SELECT_RELATIONSHIP_ASSIGNED_PART_ID);
              objectSelects.add(SELECT_MODEL_ID);
              objectSelects.add(SELECT_NAME);
              objectSelects.add(SELECT_REVISION);
              objectSelects.add(SELECT_ID);
              
              String[] objsArray = (String[])objectIdList.toArray(new String[objectIdList.size()]);
              mapList = DomainObject.getInfo(context, objsArray, objectSelects);

			  Iterator itr = mapList.iterator();
			  while (itr.hasNext()) {
				  HashMap objMap = (HashMap)itr.next();
				  					  
					  productName	   = (String)objMap.get(SELECT_NAME);
					  productRevision  = (String)objMap.get(SELECT_REVISION);
				    	  
					  mqlLogWriter ( "Assigned Part Relationship  Exists for Product name/revision = " + productName + " " + productRevision + "\n");
					  
					  AssignedPartRel =  (String)objMap.get(SELECT_RELATIONSHIP_ASSIGNED_PART_ID);
					  ModelObjID 	  =  (String)objMap.get(SELECT_MODEL_ID);
					  
					  MqlUtil.mqlCommand(context, "modify connection $1 from $2",
											AssignedPartRel, ModelObjID);
					  
					  mqlLogWriter ( "Modify the Assigned Part relationship with the Model on From side  = " + ModelObjID + "\n");
					  
					  prodObjID =  (String)objMap.get(SELECT_ID);
					  loadMigratedOids (prodObjID);
					  ContextUtil.commitTransaction(context);
			  }
          }
          catch(Exception ex)
          {
        	  ContextUtil.abortTransaction(context);
              ex.printStackTrace();
              throw ex;
          }
      }

    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" Top Level Migration for Model Based is a two step process  \n");
        writer.write(" Step1: Find all latest Product Revisions and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxModelBasedMigrationFindObjects 1000 Part C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxModelBasedTopLevelPartMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
