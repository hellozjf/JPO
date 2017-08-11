/*
 * emxVariantConfigurationFeatureFindObjectsBase.java program to get all document type Object Ids.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/emxVariantConfigurationFeatureFindObjectsBase.java 1.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import matrix.db.Context;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;


public class emxVariantConfigurationFeatureFindObjectsBase_mxJPO
{
    BufferedWriter writer = null;
    String documentDirectory = "";
    String type = "";
    String name = "*";
    String revision = "*";
    String migrationProgramName = "emxVariantConfigurationFeatureMigration";
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxVariantConfigurationFeatureFindObjectsBase_mxJPO (Context context, String[] args)
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

    if(!context.isConnected())
    {
        throw new Exception("not supported on desktop client");
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
        if( "*".equals(type) || "".equals(type) )
        {
            throw new IllegalArgumentException("Type can't be '*' or ''");
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


        writer.write("=======================================================\n\n");
        writer.write("                Querying for Type: " + type + " Name: " + name +"  Revision: "+ revision +"  Objects...\n");
        writer.write("                ("+ chunkSize + ") Objects per File\n");
        writer.write("                Writing files to: " + documentDirectory + "\n\n");
        writer.write("=======================================================\n\n");
        writer.flush();


        ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
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
        writer.write("                Querying for Document Objects  COMPLETE\n");
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
        // Written using temp query to optimize performance in anticipation of
        // large 1m+ documents in system.
        // Utilize query limit to use different algorithim in memory allocation

        String vaultList = "";

        try
        {
            EnoviaResourceBundle.getProperty(context,"emxComponents.CommonMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }

        if(vaultList == null || "null".equals(vaultList) || "".equals(vaultList))
        {
            vaultList = "*";
        }

        //String command = "temp query bus '" + type + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where 'program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";
        String strWhere = "program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true";
		String command = "temp query bus $1 $2 $3  vault $4 limit 1 where $5";
        
        //reset/set static variabless back to original values every time this JPO is run
        
        emxVariantConfigurationFeatureMigration_mxJPO._counter  = 0;
        emxVariantConfigurationFeatureMigration_mxJPO._sequence  = 1;
        emxVariantConfigurationFeatureMigration_mxJPO._oidsFile = null;
        emxVariantConfigurationFeatureMigration_mxJPO._fileWriter = null;
        emxVariantConfigurationFeatureMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxVariantConfigurationFeatureMigration_mxJPO._fileWriter == null)
        {
            try
            {
                emxVariantConfigurationFeatureMigration_mxJPO.documentDirectory = documentDirectory;
                emxVariantConfigurationFeatureMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
                emxVariantConfigurationFeatureMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxVariantConfigurationFeatureMigration_mxJPO._oidsFile));
                emxVariantConfigurationFeatureMigration_mxJPO._chunk = chunkSize;
                emxVariantConfigurationFeatureMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
            //MqlUtil.mqlCommand(context, command);
        	MqlUtil.mqlCommand(context, command, type, name, revision, vaultList, strWhere);
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxVariantConfigurationFeatureMigration_mxJPO.cleanup();
    }
}
