/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

public class emxTBEPartMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO {
    String migrationProgramName = "emxTBEPartMigration";

    /**
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @grade 0
    */
    public emxTBEPartMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
        	EnoviaResourceBundle.getProperty(context, "emxComponents.CommonMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }

        String designPartPolicy = PropertyUtil.getSchemaProperty(context,"policy_DesignPart");
        String engPartPolicy = PropertyUtil.getSchemaProperty(context,"policy_EngineeringPart");
        
        String whereClause = "(policy == \""+ designPartPolicy +"\" || policy == \"" + engPartPolicy + "\")";
        String command = "temp query bus '" + type + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where '" + whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";
        writer.write("command = "+ command + "\n");

        //reset/set static variabless back to original values every time this JPO is run
        emxTBEPartMigration_mxJPO._counter  = 0;
        emxTBEPartMigration_mxJPO._sequence  = 1;
        emxTBEPartMigration_mxJPO._oidsFile = null;
        emxTBEPartMigration_mxJPO._fileWriter = null;
        emxTBEPartMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxTBEPartMigration_mxJPO._fileWriter == null)
        {
            try
            {
                emxTBEPartMigration_mxJPO.documentDirectory = documentDirectory;
                emxTBEPartMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxTBEPartMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxTBEPartMigration_mxJPO._oidsFile));
                emxTBEPartMigration_mxJPO._chunk = chunkSize;
                emxTBEPartMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
            MqlUtil.mqlCommand(context,"temp query $1 $2 $3 vault $4 limit $5 where $6",
                               type,name,revision,vaultList,"1",whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true"); 
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxTBEPartMigration_mxJPO.cleanup();
    }
}
