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

public class emxEngineeringECRMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "emxEngineeringECRMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxEngineeringECRMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
        String TYPE_ECR  = PropertyUtil.getSchemaProperty(context, "type_ECR");
        String vaultList = "*";
        try
        {
        	EnoviaResourceBundle.getProperty(context, "emxComponents.CommonDocumentMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }
        String whereClause = "((policy.property[PolicyClassification] == \"StaticApproval\") || policy == \"ECR (Rejected)\")";
        String command = "temp query bus '" + TYPE_ECR + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where '" + whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";

        writer.write("command = "+ command + "\n");
        //reset/set static variabless back to original values every time this JPO is run
        emxEngineeringECRMigration_mxJPO._counter  = 0;
        emxEngineeringECRMigration_mxJPO._sequence  = 1;
        emxEngineeringECRMigration_mxJPO._oidsFile = null;
        emxEngineeringECRMigration_mxJPO._fileWriter = null;
        emxEngineeringECRMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxEngineeringECRMigration_mxJPO._fileWriter == null)
        {
            try
            {
                emxEngineeringECRMigration_mxJPO.documentDirectory = documentDirectory;
                emxEngineeringECRMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxEngineeringECRMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxEngineeringECRMigration_mxJPO._oidsFile));
                emxEngineeringECRMigration_mxJPO._chunk = chunkSize;
                emxEngineeringECRMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
            MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 vault $4 limit $5 where $6",
                               TYPE_ECR,name,revision,vaultList,"1",whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxEngineeringECRMigration_mxJPO.cleanup();
    }
}
