/*
 * enoConfiguredEBOMExplodeFindObjectsBase.java
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

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.*;

public class enoConfiguredEBOMExplodeFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "enoConfiguredEBOMExplodeMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public enoConfiguredEBOMExplodeFindObjectsBase_mxJPO (Context context, String[] args)
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
           newArgs[1] = "EBOM,EBOM Pending";
           newArgs[2] = args[1];
           return super.mxMain(context, newArgs);
       }
       catch (IllegalArgumentException iExp)
       {
           writer.write("=================================================================\n");
           writer.write("Invalid number arguments\n");
           writer.write("Please specify:\n");
           writer.write("1.Number of Rel Ids to be written to a file\n");
           writer.write("2.Directory for migration script\n");
           writer.write("Step 1 of Migration enoConfiguredEBOMExplodeFindObjectsBase_mxJPO :   " + iExp.toString() + "   : FAILED \n");
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
        
    	String relType  = PropertyUtil.getSchemaProperty("relationship_EBOMPending")+","+DomainConstants.RELATIONSHIP_EBOM;

        String whereClause = "interface == 'Effectivity Framework' && attribute[Unit of Measure].value == 'EA (each)' && attribute[Quantity].value > 1";
        String command = "query connection relationship '" + relType +  "' where +"+whereClause+" && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true";
        
        writer.write("command = "+ command + "\n");

        //reset/set static variabless back to original values every time this JPO is run
        enoConfiguredEBOMExplodeMigration_mxJPO._counter  = 0;
        enoConfiguredEBOMExplodeMigration_mxJPO._sequence  = 1;
        enoConfiguredEBOMExplodeMigration_mxJPO._oidsFile = null;
        enoConfiguredEBOMExplodeMigration_mxJPO._fileWriter = null;
        enoConfiguredEBOMExplodeMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (enoConfiguredEBOMExplodeMigration_mxJPO._fileWriter == null)
        {
            try
            {
            	enoConfiguredEBOMExplodeMigration_mxJPO.documentDirectory = documentDirectory;
            	enoConfiguredEBOMExplodeMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
            	enoConfiguredEBOMExplodeMigration_mxJPO.createIncorrectQtyValuesFile();
            	enoConfiguredEBOMExplodeMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(enoConfiguredEBOMExplodeMigration_mxJPO._oidsFile));
            	enoConfiguredEBOMExplodeMigration_mxJPO._chunk = chunkSize;
            	enoConfiguredEBOMExplodeMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
        	MqlUtil.mqlCommand(context, "query connection relationship $1 where $2", relType, whereClause+" && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true" );
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        enoConfiguredEBOMExplodeMigration_mxJPO.cleanup();
        writer.write("=================================================================\n");
    	writer.write("All the relationships with Unit of Measue as \"EA (each)\" and having\n");
    	writer.write("decimal value for the Attribute Quantity cannot be migrated.\n");
    	writer.write("All such Relationship IDs will be written to IncorrectQtyValues.csv file.\n");
    	writer.write("Please correct those values before migration\n");
        writer.write("=================================================================\n");
        enoConfiguredEBOMExplodeMigration_mxJPO.ObjectsToBeCorrectedOidsLog.close();
    }
}
