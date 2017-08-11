
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
import com.matrixone.apps.engineering.EngineeringConstants;
  
  public class enoENGBGTPMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
  {
	  
	  String WILD_CARD_ASTERESK = "*";
	  
	  String migrationProgramName = "enoENGBGTPMigration";

	  
	    /**
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds no arguments
	     * @throws Exception if the operation fails
	     * @grade 0
	     */
	    public enoENGBGTPMigrationFindObjectsBase_mxJPO (Context context, String[] args)
	        throws Exception
	    {
	     super(context, args);
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
	            writer.write("Step 1 of Migration enoENGBGTPMigrationFindObjectsBase_mxJPO :   " + iExp.toString() + "   : FAILED \n");
	            writer.write("=================================================================\n");
	            writer.close();
	            return 0;
	        }
	    }
	    
	    /**
	     * Evalutes a temp query to get all the Part objects in the system which has policy "Development Part".
	     * All the Parts that need to be migrated will have policy "Development Part"
	     * @param context the eMatrix <code>Context</code> object
	     * @param chunksize has the no. of objects to be stored in file.
	     * @return void
	     * @exception Exception if the operation fails.
	     */
	    public void getIds(Context context, int chunkSize) throws Exception
	    {

	        String vaultList = WILD_CARD_ASTERESK;

	        String whereclause = "(policy == '"+DomainConstants.POLICY_DEVELOPMENT_PART+"' || policy == '"+DomainConstants.POLICY_EC_PART+"' || policy == '"+EngineeringConstants.POLICY_CONFIGURED_PART+"')";
	        
	        writer.write("whereclause = "+ whereclause + "\n");
	       
	        //reset/set static variables back to original values every time this JPO is run
	        enoENGBGTPMigration_mxJPO._counter  = 0;
	        enoENGBGTPMigration_mxJPO._sequence  = 1;
	        enoENGBGTPMigration_mxJPO._oidsFile = null;
	        enoENGBGTPMigration_mxJPO._fileWriter = null;
	        enoENGBGTPMigration_mxJPO._objectidList = null;

	        //create BW and file first time
	        if (enoENGBGTPMigration_mxJPO._fileWriter == null)
	        {
	            try
	            {
	            	enoENGBGTPMigration_mxJPO.documentDirectory = documentDirectory;
	                enoENGBGTPMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
	                enoENGBGTPMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(enoENGBGTPMigration_mxJPO._oidsFile));
	                enoENGBGTPMigration_mxJPO._chunk = chunkSize;
	                enoENGBGTPMigration_mxJPO._objectidList = new StringList(chunkSize);
	            }
	            catch(FileNotFoundException ex)
	            {
	                throw ex;
	            }
	        }

	        try
	        {

		    String result  = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 vault $4 limit $5 where $6",
		    		type,WILD_CARD_ASTERESK,WILD_CARD_ASTERESK,vaultList,"1",whereclause + " && program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");
					     

	        }
	        catch(Exception ex)
	        {
	            throw ex;
	        }

	        // call cleanup to write the left over oids to a file
	        enoENGBGTPMigration_mxJPO.cleanup();
	    }
	}
