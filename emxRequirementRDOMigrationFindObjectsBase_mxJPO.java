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

import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.requirements.RequirementsUtil;

public class emxRequirementRDOMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
    String migrationProgramName = "emxRequirementRDOMigration";

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxRequirementRDOMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
        String vaultList = "*";
                             
    	String whereClause = "(to[" + RequirementsUtil.getDesignResponsibilityRelationship(context)+ "]==true || " + 
    								"to[" + RequirementsUtil.getAssignedRequirementRelationship(context) + "]==true || " + 
    										"to[Assigned Use Case]==true ) &&" + 
    										"(program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true)";
        String cmdParameterized = "temp query bus $1 $2 $3  vault $4 limit $5 where $6";
        
        
        //reset/set static variabless back to original values every time this JPO is run
        emxRequirementRDOMigration_mxJPO._counter  = 0;
        emxRequirementRDOMigration_mxJPO._sequence  = 1;
        emxRequirementRDOMigration_mxJPO._oidsFile = null;
        emxRequirementRDOMigration_mxJPO._fileWriter = null;
        emxRequirementRDOMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxRequirementRDOMigration_mxJPO._fileWriter == null)
        {
            try
            {
            	emxRequirementRDOMigration_mxJPO.documentDirectory = documentDirectory;
            	emxRequirementRDOMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
            	emxRequirementRDOMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxRequirementRDOMigration_mxJPO._oidsFile));
            	emxRequirementRDOMigration_mxJPO._chunk = chunkSize;
            	emxRequirementRDOMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
        	String result  = MqlUtil.mqlCommand(context, cmdParameterized, type , "*", "*", vaultList, chunkSize + "", whereClause);

        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxRequirementRDOMigration_mxJPO.cleanup();
    }
}
