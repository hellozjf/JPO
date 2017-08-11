/*
 ** emxModelBasedMigrationFindPUEObjectsBase.java
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

public class emxModelBasedMigrationFindPUEObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO 

{

	private static final String migrationProgramName = "emxModelBasedPUEMigration";

    /**
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @grade 0
    */
    public emxModelBasedMigrationFindPUEObjectsBase_mxJPO (Context context, String[] args)
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

        String vaultList = "*";

        try
        {
            //FrameworkProperties.getProperty("emxComponents.CommonMigration.VaultList");
        	EnoviaResourceBundle.getProperty(context,"emxComponents.CommonMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }
              
        //reset/set static variabless back to original values every time this JPO is run
        emxModelBasedPUEMigration_mxJPO._counter  = 0;
        emxModelBasedPUEMigration_mxJPO._sequence  = 1;
        emxModelBasedPUEMigration_mxJPO._oidsFile = null;
        emxModelBasedPUEMigration_mxJPO._fileWriter = null;
        emxModelBasedPUEMigration_mxJPO._objectidList = null;

        //create BW and file first time
        if (emxModelBasedPUEMigration_mxJPO._fileWriter == null)
        {
            try
            {
            	emxModelBasedPUEMigration_mxJPO.documentDirectory = documentDirectory;
            	emxModelBasedPUEMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
            	emxModelBasedPUEMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxModelBasedPUEMigration_mxJPO._oidsFile));
            	emxModelBasedPUEMigration_mxJPO._chunk = chunkSize;
            	emxModelBasedPUEMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
			//Evaluates temp query for each object and in where expression calls writeOID method in commonMigration which return false so that query executes again for all objects in db
			String POLICY_PUE_ECO = PropertyUtil.getSchemaProperty(context, "policy_PUEECO");
			String implement = PropertyUtil.getSchemaProperty(context,"policy",POLICY_PUE_ECO,"state_Implemented");
			String cancelled = PropertyUtil.getSchemaProperty(context,"policy",POLICY_PUE_ECO,"state_Cancelled");
        
			String whereClause  = "((policy == \"" + POLICY_PUE_ECO + "\") && (current != "+implement+") && (current != "+cancelled+") && program[" + migrationProgramName +" -method writeOID ${OBJECTID} ${TYPE}] == true)";
			String command = "temp query bus \"$1\" \"$2\" \"$3\"  vault \"$4\" limit \"$5\" where \"$6\""; 

			writer.write("command = "+ command + "\n");
			MqlUtil.mqlCommand(context, command, type, name, revision, vaultList, "1", whereClause);
        }
        catch(Exception me)
        {
            throw me;
        }
  
        // call cleanup to write the left over oids in the static object list to a file
        emxModelBasedPUEMigration_mxJPO.cleanup();
    }
}

