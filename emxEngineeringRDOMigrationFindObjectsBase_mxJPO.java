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

public class emxEngineeringRDOMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "emxEngineeringRDOMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxEngineeringRDOMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
            FrameworkProperties.getProperty(context, "emxComponents.CommonDocumentMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }
                             
        String whereClause = "(altowner1 == \"\" && altowner2 == \"\")";     
        String cmdParameterized = "temp query bus $1 $2 $3  vault $4 limit $5 where $6";
        
        //reset/set static variabless back to original values every time this JPO is run
        emxEngineeringRDOMigration_mxJPO._counter  = 0;
        emxEngineeringRDOMigration_mxJPO._sequence  = 1;
        emxEngineeringRDOMigration_mxJPO._oidsFile = null;
        emxEngineeringRDOMigration_mxJPO._fileWriter = null;
        emxEngineeringRDOMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxEngineeringRDOMigration_mxJPO._fileWriter == null)
        {
            try
            {
            	emxEngineeringRDOMigration_mxJPO.documentDirectory = documentDirectory;
            	emxEngineeringRDOMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
            	emxEngineeringRDOMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxEngineeringRDOMigration_mxJPO._oidsFile));
            	emxEngineeringRDOMigration_mxJPO._chunk = chunkSize;
            	emxEngineeringRDOMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {        	
        	MqlUtil.mqlCommand(context, cmdParameterized, type , "*", "*", vaultList, "2", whereClause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxEngineeringRDOMigration_mxJPO.cleanup();
    }
}
