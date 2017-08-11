/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.io.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.util.*;

public class emxPartMasterMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "emxPartMasterMigration";

    /**
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @grade 0
    */
    public emxPartMasterMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
        // large 1m+ documents in system.
        // Utilize query limit to use different algorithim in memory allocation

        String vaultList = "*";

        try
        {
            FrameworkProperties.getProperty(context, "emxComponents.CommonMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }
        String whereClause = "(revision == first) && (policy.property[PolicyClassification] == \"Development\" || policy.property[PolicyClassification] == \"Production\")";
        String command = "temp query bus '" + type + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where '" + whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";
        writer.write("command = "+ command + "\n");

        //reset/set static variabless back to original values every time this JPO is run
        emxPartMasterMigration_mxJPO._counter  = 0;
        emxPartMasterMigration_mxJPO._sequence  = 1;
        emxPartMasterMigration_mxJPO._oidsFile = null;
        emxPartMasterMigration_mxJPO._fileWriter = null;
        emxPartMasterMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxPartMasterMigration_mxJPO._fileWriter == null)
        {
            try
            {
                emxPartMasterMigration_mxJPO.documentDirectory = documentDirectory;
                emxPartMasterMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxPartMasterMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxPartMasterMigration_mxJPO._oidsFile));
                emxPartMasterMigration_mxJPO._chunk = chunkSize;
                emxPartMasterMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
            MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 vault $4 limit $5 where $6",
                               type,name,revision,vaultList,"1",whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxPartMasterMigration_mxJPO.cleanup();
    }
}
