/*
 * emxCommonFindObjectsBase.java program to get all document type Object Ids.
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

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;

public class emxCommonFindObjectsBase_mxJPO
{
    BufferedWriter writer = null;
    String documentDirectory = "";
    String type = "";
    String name = "*";
    String revision = "*";
    boolean isType = true;
    String migrationProgramName = "emxCommonMigration";
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxCommonFindObjectsBase_mxJPO (Context context, String[] args)
        throws Exception
    {
		writer = new BufferedWriter(new MatrixWriter(context));
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
        int chunkSize = 0;
        String isRel = null;

    if(!context.isConnected())
    {
        throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
    }

    try
    {
        writer = new BufferedWriter(new MatrixWriter(context));
        if (args.length < 3 )
        {
            throw new IllegalArgumentException();
        }

        chunkSize = Integer.parseInt(args[0]);
        if (chunkSize == 0 || chunkSize < 1 )
        {
            throw new IllegalArgumentException();
        }

        type = args[1];
        
        if( args.length == 7)
        {
        	isRel = args[6];
        }
        
        if( "*".equals(type) || "".equals(type) )
        {
            throw new IllegalArgumentException(ComponentsUtil.i18nStringNow("emxComponents.CommonFindObject.TypeCannotBe", context.getLocale().getLanguage()));
        }else{
        	 isType = true;
        }

    }
    catch (IllegalArgumentException iExp)
    {
        writer.write("=================================================================\n");
        writer.write("Wrong number of arguments Or Invalid number of Oids per file\n");
        writer.write("Step 1 of Migration :   " + iExp.toString() + "   : FAILED \n");
        writer.write("=================================================================\n");
        writer.close();
        return 0;
    }

    try
    {
        long startTime = System.currentTimeMillis();
        documentDirectory = args[2];

        // documentDirectory does not ends with "/" add it
        String fileSeparator = java.io.File.separator;
        if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
        {
            documentDirectory = documentDirectory + fileSeparator;
        }

        if( args.length > 3 )
        {
            migrationProgramName = args[3];
        }
        if( args.length > 4 )
        {
            name = args[4];
        }
        if( args.length > 5 )
        {
            revision = args[5];
        }

        if(isRel==null){
            try
            {
                String result  = MqlUtil.mqlCommand(context, "list type $1", type);
                isType = type.equals(result) ? true : false;                               
            }
            catch(Exception ex)
            {
                //Do Nothing
            }
        }else if ("relationship".equalsIgnoreCase(isRel)){
        	isType = false;        	
        }

        writer.write("=======================================================\n\n");
        if( isType )
        {
        writer.write("                Querying for Type: " + type + " Name: " + name +"  Revision: "+ revision +"  Objects...\n");
        writer.write("                ("+ chunkSize + ") Objects per File\n");
        } else {
            writer.write("                Querying for Relationship : Name: " + type + "  Relationships...\n");
            writer.write("                ("+ chunkSize + ") Relationships per File\n");
        }
        writer.write("                Writing files to: " + documentDirectory + "\n\n");
        writer.write("=======================================================\n\n");
        writer.flush();


        ContextUtil.pushContext(context, null, null, null);
        ContextUtil.startTransaction(context,true);
        try
        {
            getIds(context, chunkSize);
        }
        catch(FileNotFoundException fnfExp)
        {
            throw fnfExp;
        }

        writer.write("\n=======================================================\n\n");
        if( isType )
        {
            writer.write("                Querying for Objects  COMPLETE\n");
        } else {
            writer.write("                Querying for Relationships COMPLETE\n");
        }
        writer.write("                Time:"+ (System.currentTimeMillis() - startTime) + "ms \n");
        writer.write("                Step 1 of Migration         :  SUCCESS \n\n");
        writer.write("=======================================================\n\n");
        writer.flush();
        ContextUtil.commitTransaction(context);
    }
    catch (FileNotFoundException fEx)
    {
        // check if user has access to the directory
        // check if directory exists
        writer.write("=================================================================\n");
        writer.write("Directory does not exist or does not have access to the directory\n");
        writer.write("Step 1 of Migration     : FAILED \n");
        writer.write("=================================================================\n");
        writer.flush();

        ContextUtil.abortTransaction(context);
    }
    catch (Exception ex)
    {
        // abort if getIds fail
        writer.write("=================================================================\n");
        writer.write("Find Documents Query failed \n");
        writer.write("Step 1 of Migration     : FAILED \n");
        writer.write("=================================================================\n");
        writer.flush();

        ex.printStackTrace();
        ContextUtil.abortTransaction(context);
    }
    finally
    {
        writer.close();
        ContextUtil.popContext(context);
    }

        // always return 0, even this gives an impression as success
        // this way, matrixWriter writes to console
        // else writer.write statements do not show up in Application console
        // but it works in mql console
        return 0;
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
        //reset/set static variabless back to original values every time this JPO is run
        emxCommonMigration_mxJPO._counter  = 0;
        emxCommonMigration_mxJPO._sequence  = 1;
        emxCommonMigration_mxJPO._oidsFile = null;
        emxCommonMigration_mxJPO._fileWriter = null;
        emxCommonMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxCommonMigration_mxJPO._fileWriter == null)
        {
            try
            {
                emxCommonMigration_mxJPO.documentDirectory = documentDirectory;
                emxCommonMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxCommonMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxCommonMigration_mxJPO._oidsFile));
                emxCommonMigration_mxJPO._chunk = chunkSize;
                emxCommonMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
        	// Written using temp query to optimize performance in anticipation of
            // large 1m+ documents in system.
            // Utilize query limit to use different algorithim in memory allocation
            if( isType ) {
            	MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 vault $4 limit $5 where $6",type,name,revision, "*", "1", "program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");
            } else {
            	MqlUtil.mqlCommand(context, "query connection relationship $1 limit $2 where $3", type, "1", "program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true" );
            }
            
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxCommonMigration_mxJPO.cleanup();
    }
}
