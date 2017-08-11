/*
 * ${CLASSNAME}.java
 *    Base JPO for finding the Objects to be migrated for Document Library Schema Unification
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

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

public class emxLibraryCentalDocumentSchemaMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO{
    String docLibType       = "";
    String bookshelfType    = "";
    String bookType         = "";
    String defaultChunkSize = "100";

    public emxLibraryCentalDocumentSchemaMigrationFindObjectsBase_mxJPO (Context context, String[] args)
    throws Exception
    {
        super(context, args);
        docLibType      = PropertyUtil.getSchemaProperty(context,"type_Library");
        bookshelfType   = PropertyUtil.getSchemaProperty(context,"type_Bookshelf");
        bookType        = PropertyUtil.getSchemaProperty(context,"type_Book");
    }

    /**
     * This method is executed if a specific method is not specified.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds two arguments
     *        args[0] - chunk size (i.e No of Object Ids to be written to each flat file)
     *        args[1] - Directory to where files containing ObjectIds are to be written
     *
     * @returns 0 always
     * @throws Exception if the operation fails
     */
    public int mxMain(Context context, String[] args) throws Exception{
        try{
            writer      = new BufferedWriter(new MatrixWriter(context));
            if (args.length < 1 ){
                throw new IllegalArgumentException("Wrong number of arguments");
            }
            String[] newArgs    = null;

            if(args.length == 1){
                //Only One argument is provided, i.e., DocumentDirectory
                newArgs    = new String[3];
                newArgs[0] = defaultChunkSize;
                newArgs[1] = docLibType;
                newArgs[2] = args[0];
            }else{
                //If there are more than 1 arguments , then first Argument is chunk size
                newArgs    = new String[args.length+1];
                newArgs[0] = args[0];

                //insert type argument at index 1 in newArgs array
                newArgs[1] = docLibType;

                //everything from and after index 1 in args is put to newArgs from index 2
                for(int i = args.length; i > 1; i--){
                    newArgs[i] = args[i-1];
                }
            }

            super.mxMain(context, newArgs);
        }catch (IllegalArgumentException iExp){
            writer.write("=================================================================\n");
            writer.write("Wrong number of arguments \n");
            writer.write("Step 1 of Migration :   " + iExp.toString() + "   : FAILED \n");
            writer.write("=================================================================\n");
            writer.flush();
            writer.close();
        }
        return 0;
    }

    /**
     * Evaluates temp query to get all
     *      - Objects of type 'Document Library'
     *      - Objects of type 'Bookshelf' which are not connected to any 'document Library'
     *      - Objects of type 'Book' which are not connected to any 'Bookshelf'

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

        String vaultList = "*";

        try{
            vaultList = EnoviaResourceBundle.getProperty(context,"emxComponents.CommonMigration.VaultList");
        }catch(Exception e) {
            vaultList = "*";
        }

        if(UIUtil.isNullOrEmpty(vaultList)){
            vaultList = "*";
        }


        //reset/set static variables back to original values every time this JPO is run
        emxCommonMigration_mxJPO._counter       = 0;
        emxCommonMigration_mxJPO._sequence      = 1;
        emxCommonMigration_mxJPO._oidsFile      = null;
        emxCommonMigration_mxJPO._fileWriter    = null;
        emxCommonMigration_mxJPO._objectidList  = null;

        //set statics
        //create BW and file first time
        if (emxCommonMigration_mxJPO._fileWriter == null){
            try{
                emxCommonMigration_mxJPO.documentDirectory  = documentDirectory;
                emxCommonMigration_mxJPO._oidsFile          = new java.io.File(documentDirectory + "objectids_1.txt");
                emxCommonMigration_mxJPO._fileWriter        = new BufferedWriter(new FileWriter(emxCommonMigration_mxJPO._oidsFile));
                emxCommonMigration_mxJPO._chunk             = chunkSize;
                emxCommonMigration_mxJPO._objectidList      = new StringList(chunkSize);
            }catch(FileNotFoundException fileNotFoundEx){
                throw fileNotFoundEx;
            }
        }

        try{
            String command  = "temp query bus $1 $2 $3  vault $4 limit $5 where $6";
            MqlUtil.mqlCommand(context, command, docLibType, name, revision, vaultList, "1", "program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true");

            MqlUtil.mqlCommand(context, command, bookshelfType, name, revision, vaultList, "1", "if (to[Has Bookshelves] == False) then (program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true) else false");

            MqlUtil.mqlCommand(context, command, bookType, name, revision, vaultList, "1", "if (to[Has Books] == False) then (program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true) else false");

        }catch(Exception me){
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxCommonMigration_mxJPO.cleanup();
    }

}
