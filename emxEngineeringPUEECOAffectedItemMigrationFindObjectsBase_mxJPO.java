/*
 * emxEngineeringPUEECOAffectedItemMigrationFindObjectsBase.java program to get all document type Object Ids.
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

public class emxEngineeringPUEECOAffectedItemMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "emxEngineeringPUEECOAffectedItemMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxEngineeringPUEECOAffectedItemMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
        emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._counter  = 0;
        emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._sequence  = 1;
        emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._oidsFile = null;
        emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._fileWriter = null;
        emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._fileWriter == null)
        {
            try
            {
                emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO.documentDirectory = documentDirectory;
                emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._oidsFile));
                emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._chunk = chunkSize;
                emxEngineeringPUEECOAffectedItemMigrationBase_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
			String command = "temp query bus '" + type + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where 'program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";
			
			String cmdParameterized = "temp query bus $1 $2 $3  vault $4 limit $5 where $6";
			writer.write("command = "+ command + "\n");
			MqlUtil.mqlCommand(context, 
					cmdParameterized,
								type, name, revision, vaultList, "0","program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxEngineeringPUEECOAffectedItemMigration_mxJPO.cleanup();
    }
}
