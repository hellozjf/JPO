
/*
 * ${CLASSNAME}.java
 * Program to migrate Unit of Measure Type and values for existing data.
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

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.effectivity.EffectivityFramework;
  
  public class enoConfiguredECOMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
  {
	  
	  String WILD_CARD_ASTERESK = "*";
	  String migrationProgramName = "enoConfiguredECOMigration";
	  String rel_NamedEffectivity;
	  
	    /**
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds no arguments
	     * @throws Exception if the operation fails
	     * @grade 0
	     */
	    public enoConfiguredECOMigrationFindObjectsBase_mxJPO (Context context, String[] args)
	    throws Exception
	    {
	    	super(context, args);
	    	rel_NamedEffectivity = PropertyUtil.getSchemaProperty(context,"relationship_NamedEffectivity");
	    }
	    
		 /**
		 * This method is executed if a specific method is not specified.
		 *
		 * @param context the eMatrix <code>Context</code> object
		 * @param args holds no arguments
		 * @returns nothing
		 * @throws Exception if the operation fails
		 */
	    public int mxMain(Context context, String[] args)
	        throws Exception
	    {
	        try
	        {
	            if (args.length < 2 )
	            {
	                throw new IllegalArgumentException();
	            }
	            String[] newArgs = new String[3];
	            newArgs[0] = args[0];
	            newArgs[1] = args[1];
	            newArgs[2] = args[2];
	            return super.mxMain(context, newArgs);
	        }
	        catch (IllegalArgumentException iExp)
	        {
	            writer.write("=================================================================\n");
	            writer.write("Invalid number arguments\n");
	            writer.write("Please specify:\n");
	            writer.write("1.Number of objectIds to be written to a file\n");
				writer.write("2.Type\n");
	            writer.write("3.Directory for migration script\n");
	            writer.write("Step 1 of Migration PUEChangeMigrationFindObjects :   " + iExp.toString() + "   : FAILED \n");
	            writer.write("=================================================================\n");
	            writer.close();
	            return 0;
	        }
	    }
	    
	    /**
	     * Evalutes a temp query to get all the Configured ECO objects in the would be migrated
		 *
	     * @param context the eMatrix <code>Context</code> object
	     * @param chunksize has the no. of objects to be stored in file.
	     * @return void
	     * @exception Exception if the operation fails.
	     */
	    public void getIds(Context context, int chunkSize) throws Exception
	    {

	        String vaultList = WILD_CARD_ASTERESK;
	        String whereclause = "from["+rel_NamedEffectivity+"] == True";
	        		
	        //reset/set static variables back to original values every time this JPO is run
	        enoConfiguredECOMigration_mxJPO._counter  = 0;
	        enoConfiguredECOMigration_mxJPO._sequence  = 1;
	        enoConfiguredECOMigration_mxJPO._oidsFile = null;
	        enoConfiguredECOMigration_mxJPO._fileWriter = null;
	        enoConfiguredECOMigration_mxJPO._objectidList = null;

	        //create BW and file first time
	        if (enoConfiguredECOMigration_mxJPO._fileWriter == null)
	        {
	            try
	            {
	            	enoConfiguredECOMigration_mxJPO.documentDirectory = documentDirectory;
	            	enoConfiguredECOMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
	            	enoConfiguredECOMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxENGUOMMigration_mxJPO._oidsFile));
	            	enoConfiguredECOMigration_mxJPO._chunk = chunkSize;
	            	enoConfiguredECOMigration_mxJPO._objectidList = new StringList(chunkSize);
	            	enoConfiguredECOMigration_mxJPO.CECOWithFOApplicability();
	            }
	            catch(FileNotFoundException ex)
	            {
	                throw ex;
	            }
	        }

	        try
	        {
					// execute the following mql and output the data to the objectids file 
	        		String result  = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 vault $4 limit $5 where $6",
		    		type,WILD_CARD_ASTERESK,WILD_CARD_ASTERESK,vaultList,"1",whereclause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");

	        }
	        catch(Exception ex)
	        {
	            throw ex;
	        }

	        enoConfiguredECOMigration_mxJPO.cleanup();
	    }
	}
