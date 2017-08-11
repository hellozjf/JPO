/*
 * emxCommonDocumentFindObjectsBase.java program to get all document type Object Ids.
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

public class emxCommonDocumentFindObjectsBase_mxJPO
{
    BufferedWriter writer = null;
    String documentDirectory = "";
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxCommonDocumentFindObjectsBase_mxJPO (Context context, String[] args)
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
        throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
    }

    try
    {
        writer = new BufferedWriter(new MatrixWriter(context));
        if (args.length < 2 )
        {
            throw new IllegalArgumentException();
        }

        chunkSize = Integer.parseInt(args[0]);
        if (chunkSize == 0 || chunkSize < 1 )
        {
            throw new IllegalArgumentException();
        }
    }
    catch (IllegalArgumentException iExp)
    {
        writer.write("=================================================================\n");
        writer.write("Wrong number of arguments Or Invalid number of Oids per file\n");
        writer.write("Step 1 of Migration     : FAILED \n");
        writer.write("=================================================================\n");
        writer.close();
        return 0;
    }

    try
    {
        long startTime = System.currentTimeMillis();
        documentDirectory = args[1];
        // documentDirectory does not ends with "/" add it
        String fileSeparator = java.io.File.separator;
        if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
        {
            documentDirectory = documentDirectory + fileSeparator;
        }

        writer.write("=======================================================\n\n");
        writer.write("                Querying for Document Objects...\n");
        writer.write("                ("+ chunkSize + ") Objects per File\n");
        writer.write("                Writing files to: " + documentDirectory + "\n\n");
        writer.write("=======================================================\n\n");
        writer.flush();

        ContextUtil.pushContext(context);
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
        String TYPE_DOCUMENTS  = PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");

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

        String result ="";

        //reset/set static variabless back to original values every time this JPO is run
        emxCommonDocumentMigration_mxJPO._counter  = 0;
        emxCommonDocumentMigration_mxJPO._sequence  = 1;
        emxCommonDocumentMigration_mxJPO._oidsFile = null;
        emxCommonDocumentMigration_mxJPO._fileWriter = null;
        emxCommonDocumentMigration_mxJPO._objectidList = null;

        //set statics
        //create BW and file first time
        if (emxCommonDocumentMigration_mxJPO._fileWriter == null)
        {
            try
            {
                emxCommonDocumentMigration_mxJPO.documentDirectory = documentDirectory;
                emxCommonDocumentMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "documentobjectids_1.txt");
                emxCommonDocumentMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxCommonDocumentMigration_mxJPO._oidsFile));
                emxCommonDocumentMigration_mxJPO._chunk = chunkSize;
                emxCommonDocumentMigration_mxJPO._objectidList = new StringList(chunkSize);
            }
            catch(FileNotFoundException eee)
            {
                throw eee;
            }
        }

        try
        {
            result  = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 vault $4 limit $5 where $6", TYPE_DOCUMENTS, "*", "*", vaultList, "1", "program[emxCommonDocumentMigration -method writeOID ${OBJECTID} \"${TYPE}\"] == true");
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxCommonDocumentMigration_mxJPO.cleanup();
    }
}
