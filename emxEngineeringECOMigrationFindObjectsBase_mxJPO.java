/*
 * emxEngineeringECOMigrationFindObjectsBase.java program to get all document type Object Ids.
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

public class emxEngineeringECOMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "emxEngineeringECOMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxEngineeringECOMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
        String TYPE_ECO  = PropertyUtil.getSchemaProperty(context,"type_ECO");
        String vaultList = "*";
        try
        {
        	EnoviaResourceBundle.getProperty(context, "emxComponents.CommonDocumentMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }

        String whereClause = "(policy.property[PolicyClassification] == \"StaticApproval\")";
        String command = "temp query bus '" + TYPE_ECO + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where '" + whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";

        
        writer.write("command = "+ command + "\n");
        //reset/set static variabless back to original values every time this JPO is run
        emxEngineeringECOMigration_mxJPO._counter  = 0;
        emxEngineeringECOMigration_mxJPO._sequence  = 1;
        emxEngineeringECOMigration_mxJPO._oidsFile = null;
        emxEngineeringECOMigration_mxJPO._fileWriter = null;
        emxEngineeringECOMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxEngineeringECOMigration_mxJPO._fileWriter == null)
        {
            try
            {
                emxEngineeringECOMigration_mxJPO.documentDirectory = documentDirectory;
                emxEngineeringECOMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxEngineeringECOMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxEngineeringECOMigration_mxJPO._oidsFile));
                emxEngineeringECOMigration_mxJPO._chunk = chunkSize;
                emxEngineeringECOMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
            MqlUtil.mqlCommand(context, 
                               "temp query bus $1 $2 $3 vault $4 limit $5 where $6",
                               TYPE_ECO,name,revision,vaultList,"1",
                               whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");

        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxEngineeringECOMigration_mxJPO.cleanup();
    }
}
