/*
 * emxConfiguredEBOMPendingFindObjectsBase.java program to get all "EBOM Pending" type Rel Ids.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 */

import java.io.*;

import matrix.db.*;
import matrix.util.*;

import com.matrixone.apps.domain.util.*;

public class enoConfiguredEBOMPendingFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "enoConfiguredEBOMPendingMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public enoConfiguredEBOMPendingFindObjectsBase_mxJPO (Context context, String[] args)
        throws Exception
    {
     super(context, args);
    }

    /**
    * This method is executed if a specific method is not specified.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns nothing
    * @throws Exception if the operation fails
    */
   public int mxMain(Context context, String[] args)
       throws Exception
   {
       try
       {
           if (args.length != 2 )
           {
               throw new IllegalArgumentException();
           }
           String[] newArgs = new String[3];
           newArgs[0] = args[0];
           newArgs[1] = "EBOM Pending";
           newArgs[2] = args[1];
           return super.mxMain(context, newArgs);
       }
       catch (IllegalArgumentException iExp)
       {
           writer.write("=================================================================\n");
           writer.write("Invalid number arguments\n");
           writer.write("Please specify:\n");
           writer.write("1.Number of objectIds to be written to a file\n");
           writer.write("2.Directory for migration script\n");
           writer.write("Step 1 of Migration enoConfiguredEBOMPendingFindObjectsBase_mxJPO :   " + iExp.toString() + "   : FAILED \n");
           writer.write("=================================================================\n");
           writer.close();
           return 0;
       }
   }
   
    /**
     * Evalutes a temp query to get all the DOCUEMENTS objects in the system
     * @param context the eMatrix <code>Context</code> object
     * @param chunksize has the no. of objects to be stored in file.
     * @return void
     * @exception Exception if the operation fails.
     */
    public void getIds(Context context, int chunkSize) throws Exception
    {
        // Written using temp query to optimize performance in anticipation of
        // large 1m+ Substitute in system.
        // Utilize query limit to use different algorithim in memory allocation
        
    	String relType  = PropertyUtil.getSchemaProperty("relationship_EBOMPending");
        
    	String command = "query connection relationship '" + relType + "' where program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true";
        
        writer.write("command = "+ command + "\n");
        //reset/set static variabless back to original values every time this JPO is run
        enoConfiguredEBOMPendingMigration_mxJPO._counter  = 0;
        enoConfiguredEBOMPendingMigration_mxJPO._sequence  = 1;
        enoConfiguredEBOMPendingMigration_mxJPO._oidsFile = null;
        enoConfiguredEBOMPendingMigration_mxJPO._fileWriter = null;
        enoConfiguredEBOMPendingMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (enoConfiguredEBOMPendingMigration_mxJPO._fileWriter == null)
        {
            try
            {
            	enoConfiguredEBOMPendingMigration_mxJPO.documentDirectory = documentDirectory;
            	enoConfiguredEBOMPendingMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
            	enoConfiguredEBOMPendingMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(enoConfiguredEBOMPendingMigration_mxJPO._oidsFile));
                enoConfiguredEBOMPendingMigration_mxJPO._chunk = chunkSize;
                enoConfiguredEBOMPendingMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
        	MqlUtil.mqlCommand(context, "query connection relationship $1 where $2", relType, "program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true" );
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        enoConfiguredEBOMPendingMigration_mxJPO.cleanup();
    }
}
