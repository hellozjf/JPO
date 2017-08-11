
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
  
  public class emxENGUOMMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
  {
	  
	  String ATTRIBUTE_UOM_TYPE = PropertyUtil.getSchemaProperty("attribute_UOMType");
	  String WILD_CARD_ASTERESK = "*";
	  String ATTRIBUTE_WHERE_UOM_TYPE_PROPORTION = "attribute["+ATTRIBUTE_UOM_TYPE+"] == Proportion";
	  
	  String migrationProgramName = "emxENGUOMMigration";

	  
	    /**
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds no arguments
	     * @throws Exception if the operation fails
	     * @grade 0
	     */
	    public emxENGUOMMigrationFindObjectsBase_mxJPO (Context context, String[] args)
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
	            writer.write("Step 1 of Migration emxENGUOMMigrationFindObjectsBase_mxJPO :   " + iExp.toString() + "   : FAILED \n");
	            writer.write("=================================================================\n");
	            writer.close();
	            return 0;
	        }
	    }
	    
	    /**
	     * Evalutes a temp query to get all the Part objects in the system which has Unit of Measure Type set to 'Proportion'
	     * All the Parts that need to be migrated will have UOM Type set to Proportion
	     * @param context the eMatrix <code>Context</code> object
	     * @param chunksize has the no. of objects to be stored in file.
	     * @return void
	     * @exception Exception if the operation fails.
	     */
	    public void getIds(Context context, int chunkSize) throws Exception
	    {

	        String vaultList = WILD_CARD_ASTERESK;

	        String whereclause = /*"policy == '"+DomainConstants.POLICY_EC_PART+"'" + " && "+*/ATTRIBUTE_WHERE_UOM_TYPE_PROPORTION;
	        
	        writer.write("whereclause = "+ whereclause + "\n");
	       
	        //reset/set static variables back to original values every time this JPO is run
	        emxENGUOMMigration_mxJPO._counter  = 0;
	        emxENGUOMMigration_mxJPO._sequence  = 1;
	        emxENGUOMMigration_mxJPO._oidsFile = null;
	        emxENGUOMMigration_mxJPO._fileWriter = null;
	        emxENGUOMMigration_mxJPO._objectidList = null;

	        //create BW and file first time
	        if (emxENGUOMMigration_mxJPO._fileWriter == null)
	        {
	            try
	            {
	            	emxENGUOMMigration_mxJPO.documentDirectory = documentDirectory;
	                emxENGUOMMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
	                emxENGUOMMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxENGUOMMigration_mxJPO._oidsFile));
	                emxENGUOMMigration_mxJPO._chunk = chunkSize;
	                emxENGUOMMigration_mxJPO._objectidList = new StringList(chunkSize);
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
	        emxENGUOMMigration_mxJPO.cleanup();
	    }
	}
