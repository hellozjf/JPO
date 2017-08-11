/*
 * emxEngineeringConfiguredPartMigrationFindObjectsBase.java program to get all document type Object Ids.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.text.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.unresolvedebom.UnresolvedEBOMConstants;

public class emxEngineeringConfiguredPartMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "emxEngineeringConfiguredPartMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxEngineeringConfiguredPartMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
        
        String vaultList = "";
        try
        {
        	EnoviaResourceBundle.getProperty(context,"emxComponents.CommonDocumentMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }

        if(vaultList == null || "null".equals(vaultList) || "".equals(vaultList))
        {
            vaultList = "*";
        }

        //reset/set static variabless back to original values every time this JPO is run
        emxEngineeringConfiguredPartMigrationBase_mxJPO._counter  = 0;
        emxEngineeringConfiguredPartMigrationBase_mxJPO._sequence  = 1;
        emxEngineeringConfiguredPartMigrationBase_mxJPO._oidsFile = null;
        emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter = null;
        emxEngineeringConfiguredPartMigrationBase_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter == null)
        {
            try
            {
                emxEngineeringConfiguredPartMigrationBase_mxJPO.documentDirectory = documentDirectory;
                emxEngineeringConfiguredPartMigrationBase_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxEngineeringConfiguredPartMigrationBase_mxJPO._oidsFile));
                emxEngineeringConfiguredPartMigrationBase_mxJPO._chunk = chunkSize;
                emxEngineeringConfiguredPartMigrationBase_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
			String whereclause = "policy == \"" + UnresolvedEBOMConstants.POLICY_CONFIGURED_PART + "\" && (from[" + DomainConstants.RELATIONSHIP_EBOM + "] == True || from[" + UnresolvedEBOMConstants.RELATIONSHIP_EBOM_PENDING + "] == True) && program[" + migrationProgramName + " -method writeOID ${OBJECTID} {$TYPE}] == true";
			String command = "temp query bus \"$1\" \"$2\" \"$3\" vault \"$4\" limit \"$5\" where \"$6\"";        

			writer.write("command = "+ command + "\n");
            MqlUtil.mqlCommand(context, command, type, name, revision, vaultList, "1", whereclause);
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxEngineeringConfiguredPartMigrationBase_mxJPO.cleanup();
    }
}
