/*
 ** emxModelBasedMigrationFindProductObjectsBase.java
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import com.matrixone.apps.domain.util.*;

public class emxModelBasedMigrationFindProductObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    private static final String migrationProgramName = "emxModelBasedTopLevelPartMigration";


    /**
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @grade 0
    */
    public emxModelBasedMigrationFindProductObjectsBase_mxJPO (Context context, String[] args)
            throws Exception
    {
     super(context, args);
    }

    /**
     * Evalutes a temp query to get all the PRODUCT objects in the system
     * @param context the eMatrix <code>Context</code> object
     * @param chunksize has the no. of objects to be stored in file.
     * @return void
     * @exception Exception if the operation fails.
     */
    public void getIds(Context context, int chunkSize) throws Exception
    {
        // Written using temp query to optimize performance in anticipation of
        // large 1m+ documents in system.
        // Utilize query limit to use different algorithim in memory allocation

        String RELATIONSHIP_ASSIGNED_PART   = PropertyUtil.getSchemaProperty(context,"relationship_AssignedPart");
    	String vaultList = "*";

        try
        {
        	EnoviaResourceBundle.getProperty(context,"emxComponents.CommonMigration.VaultList");
        }	
        catch(Exception e)
        {
            //do nothing
        }
       

        //reset/set static variabless back to original values every time this JPO is run
        emxModelBasedTopLevelPartMigration_mxJPO._counter  = 0;
        emxModelBasedTopLevelPartMigration_mxJPO._sequence  = 1;
        emxModelBasedTopLevelPartMigration_mxJPO._oidsFile = null;
        emxModelBasedTopLevelPartMigration_mxJPO._fileWriter = null;
        emxModelBasedTopLevelPartMigration_mxJPO._objectidList = null;

        //create BW and file first time
        if (emxModelBasedTopLevelPartMigration_mxJPO._fileWriter == null)
        {
            try
            {
            	emxModelBasedTopLevelPartMigration_mxJPO.documentDirectory = documentDirectory;
            	emxModelBasedTopLevelPartMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
            	emxModelBasedTopLevelPartMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxModelBasedTopLevelPartMigration_mxJPO._oidsFile));
            	emxModelBasedTopLevelPartMigration_mxJPO._chunk = chunkSize;
            	emxModelBasedTopLevelPartMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
			//Evaluates temp query for each object and in where expression calls writeOID method in commonMigration which return false so that query executes again for all objects in db
			String strPolicyProduct = PropertyUtil.getSchemaProperty(context,"policy_Product");
			String whereClause = "(policy == \"" + strPolicyProduct + "\") && (revision == last) && (from["+RELATIONSHIP_ASSIGNED_PART+"] == True) && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true";
			 
			String command = "temp query bus $1 $2 $3 vault $4 limit $5 where $6"; 

			writer.write("command = "+ command + "\n");
			MqlUtil.mqlCommand(context, command, type, name, revision, vaultList, "1", whereClause);
		}
        catch(Exception me)
        {
            throw me;
        }
  
        // call cleanup to write the left over oids in the static object list to a file
        emxModelBasedTopLevelPartMigration_mxJPO.cleanup();
    }
}
