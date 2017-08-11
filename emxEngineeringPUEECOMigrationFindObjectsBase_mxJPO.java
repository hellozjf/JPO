/*
 * emxEngineeringPUEECOMigrationFindObjectsBase.java program to get all document type Object Ids.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.io.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.unresolvedebom.UnresolvedEBOMConstants;

public class emxEngineeringPUEECOMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "emxEngineeringPUEECOMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxEngineeringPUEECOMigrationFindObjectsBase_mxJPO (Context context, String[] args)
        throws Exception
    {
     super(context, args);
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
        
        String vaultList = "*";
        try
        {
            //FrameworkProperties.getProperty("emxComponents.CommonDocumentMigration.VaultList");
        	EnoviaResourceBundle.getProperty(context,"emxComponents.CommonDocumentMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }

        //reset/set static variabless back to original values every time this JPO is run
        emxEngineeringPUEECOMigrationBase_mxJPO._counter  = 0;
        emxEngineeringPUEECOMigrationBase_mxJPO._sequence  = 1;
        emxEngineeringPUEECOMigrationBase_mxJPO._oidsFile = null;
        emxEngineeringPUEECOMigrationBase_mxJPO._fileWriter = null;
        emxEngineeringPUEECOMigrationBase_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxEngineeringPUEECOMigrationBase_mxJPO._fileWriter == null)
        {
            try
            {
                emxEngineeringPUEECOMigrationBase_mxJPO.documentDirectory = documentDirectory;
                emxEngineeringPUEECOMigrationBase_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxEngineeringPUEECOMigrationBase_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxEngineeringPUEECOMigrationBase_mxJPO._oidsFile));
                emxEngineeringPUEECOMigrationBase_mxJPO._chunk = chunkSize;
                emxEngineeringPUEECOMigrationBase_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
			String whereClause = "(policy==\"" + UnresolvedEBOMConstants.POLICY_PUE_ECO + "\")";
			String command = "temp query bus '" + type + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where '" + whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";

			writer.write("command = "+ command + "\n");
			MqlUtil.mqlCommand(context, 
								"temp query bus $1 $2 $3 vault $4 limit 1 where (policy==$5) && program[$6 -method writeOID ${OBJECTID} ${TYPE}] == true",
								type, name, revision, vaultList, UnresolvedEBOMConstants.POLICY_PUE_ECO, migrationProgramName);
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxEngineeringPUEECOMigration_mxJPO.cleanup();
    }
}
