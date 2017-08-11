/*
 * enoConfiguredEBOMPendingMigrationBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 */


import java.io.FileWriter;
import java.util.*;

import matrix.db.*;
import matrix.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

public class enoConfiguredEBOMPendingMigrationBase_mxJPO  extends emxCommonMigration_mxJPO
{


	/**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public enoConfiguredEBOMPendingMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
      }


      /**
       * This method does the migration work.  New Part Master objects
       * are created for those that do not have any.  All revisions of a Part are
       * connected to a single Part Master with the Part Revision relationship.
       * Only parts with Production or Development policies are migrated.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public void  migrateObjects(Context context, StringList objectIdList)
                                                          throws Exception
      {

          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }

     	 String oId="";
          
         try{
        	 String newRelType=DomainConstants.RELATIONSHIP_EBOM;
        	 StringList conObjectIdList=new StringList();
        	 Iterator itr = objectIdList.iterator(); 
        	 String EBOMPending=PropertyUtil.getSchemaProperty("relationship_EBOMPending");
        	 while(itr.hasNext())
        	 {
        		 oId=(String)itr.next();
        		 DomainRelationship rel=new DomainRelationship(oId);
        		 rel.open(context);
        		 String existingRelType=rel.getRelationshipType().toString();
        		 if(EBOMPending.equals(existingRelType)){
        			 DomainRelationship.setType(context, oId, newRelType);
        			 conObjectIdList.add(oId);
        		 }
        	 }
    		 loadMigratedOidsList(conObjectIdList);
          }
    	  catch(Exception ex)
            {	
    		  writeUnconvertedOID(oId+","+ex.toString().replace(',', ';')+"\n");
              ex.printStackTrace();
              throw ex;
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
        writer.write(" EBOM Pending Migration is a two step process  \n");
        writer.write(" Step1: Find Rel IDs with type EBOM Pending and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program enoConfiguredEBOMPendingFindObjects 1000 C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program enoConfiguredEBOMPendingMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
    
    public void createLogs()throws Exception{
    	errorLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".csv", false);
    	unconvertedFileCount ++;
    	errorLog.write("REL. ID.,ERROR DETAILS \n");
    	errorLog.flush();
    	convertedOidsLog    = new FileWriter(documentDirectory + "convertedIds.txt", false);
    	warningLog = new FileWriter(documentDirectory + "migration.log", false);
    }
}
