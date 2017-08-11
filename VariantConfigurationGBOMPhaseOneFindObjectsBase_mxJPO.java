/*
 * VariantConfigurationGBOMPhaseOneFindObjectsBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.MqlUtil;


/**
 * The <code>VariantConfigurationDerivationFindObjectsBase</code> class contains implementation code for list out all the Models in the system.
 * This is the very first step of Migration.
 *
 * @version FTR V6R2012x - Copyright (c) 2011-2016, Dassault Systemes, Inc.
 */
public class VariantConfigurationGBOMPhaseOneFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{	
	String migrationProgramName = "VariantConfigurationGBOMPhaseOneMigration";
	
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since FTR V6R2012x
     */
    public VariantConfigurationGBOMPhaseOneFindObjectsBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
    
    public void getIds(Context context, int chunkSize) throws Exception
    {
        // Written using temp query to optimize performance in anticipation of
        // large 1m+ Substitute in system.
        // Utilize query limit to use different algorithim in memory allocation    	

    	//===========================================================================================
    	// Write report only blank RDO objects.  This is a subset of the next query.
    	//===========================================================================================
    	    	
    	//===========================================================================================
    	// Find all objects for migration.
    	//===========================================================================================

    	String vaultList = "*";
                             
    	String whereClause = "'program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";

    	String cmdParameterized = "temp query bus '$1' '$2' '$3'  vault '$4' limit $5 where '$6'";
    	
        // Reset/set static variables back to original values every time this JPO is run
        VariantConfigurationGBOMPhaseOneMigration_mxJPO._counter  = 0;
        VariantConfigurationGBOMPhaseOneMigration_mxJPO._sequence  = 1;
        VariantConfigurationGBOMPhaseOneMigration_mxJPO._oidsFile = null;
        VariantConfigurationGBOMPhaseOneMigration_mxJPO._fileWriter = null;
        VariantConfigurationGBOMPhaseOneMigration_mxJPO._objectidList = null;

        // Set statics
        // Create BW and file first time
        if (VariantConfigurationGBOMPhaseOneMigration_mxJPO._fileWriter == null) {
            try {
            	VariantConfigurationGBOMPhaseOneMigration_mxJPO.documentDirectory = documentDirectory;
            	VariantConfigurationGBOMPhaseOneMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
            	VariantConfigurationGBOMPhaseOneMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(VariantConfigurationGBOMPhaseOneMigration_mxJPO._oidsFile));
            	VariantConfigurationGBOMPhaseOneMigration_mxJPO._chunk = chunkSize;
            	VariantConfigurationGBOMPhaseOneMigration_mxJPO._objectidList = new StringList(chunkSize);
            } catch(FileNotFoundException eee) {
                throw eee;
            }
        }

        try {
        	MqlUtil.mqlCommand(context, cmdParameterized, type, "*", "*", vaultList, "1", whereClause);
        } catch(Exception me) {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        VariantConfigurationGBOMPhaseOneMigration_mxJPO.cleanup();
    }
    /**
	 * Sets the migration status as a property setting.
	 * Status could be :-
	 * PreMigrationFindObjectInProgress
	 * PreMigrationFindObjectCompleted
	 * ManufacturingPlanMigrationInProgress
	 * ManufacturingPlanMigrationCompleted
	 * ProductMigrationInProgress
	 * ProductMigrationCompleted
	 * 
     * @param context the eMatrix <code>Context</code> object
 	 * @param strStatus String containing the status setting
	 * @throws Exception
	 */
	public void setAdminMigrationStatus(Context context,String strStatus) throws Exception
	{
		//String cmd = "modify program eServiceSystemInformation.tcl property MigrationR215VariantConfigurationGBOMPhase1 value "+strStatus;
		String cmd = "modify program $1 property $2 value $3 ";
		MqlUtil.mqlCommand(context, cmd, true, "eServiceSystemInformation.tcl", "MigrationR215VariantConfigurationGBOMPhase1", strStatus);
	}

	/**
	 * Gets the migration status as an integer value.  Used to enforce an order of migration.
     * @param context the eMatrix <code>Context</code> object
 	 * @return integer representing the status
	 * @throws Exception
	 */
	public int getAdminMigrationStatus(Context context) throws Exception
	{
		//String cmd = "print program eServiceSystemInformation.tcl select property[MigrationR215VariantConfigurationGBOMPhase1].value dump";
		String cmd = "print program $1 select $2 dump";
	    //String result =	MqlUtil.mqlCommand(context, mqlCommand, cmd);
		String result =	MqlUtil.mqlCommand(context, cmd, true, "eServiceSystemInformation.tcl", "property[MigrationR215VariantConfigurationGBOMPhase1].value");
	   
	    if(result.equalsIgnoreCase("PreMigrationFindObjectInProgress"))
		{
			return 1;
		}else if(result.equalsIgnoreCase("PreMigrationFindObjectCompleted"))
		{
			return 2;
		}else if(result.equalsIgnoreCase("GBOMMigrationInProgress"))
		{
			return 3;
		}else if(result.equalsIgnoreCase("GBOMMigrationCompleted"))
		{
			return 4;
		}
	    
	    return 0;
	}
	
}
