/*
 * ${CLASSNAME}.java
 *    Base JPO for migrating Objects for Document Library Schema Unification
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
import java.io.FileWriter;
import java.util.Map;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

public class emxLibraryCentalDocumentSchemaMigrationBase_mxJPO extends emxCommonMigration_mxJPO{
    String TYPE_DOCUMENT_LIBRARY         = "";
    String TYPE_BOOKSHELF                = "";
    String TYPE_BOOK                     = "";
    String TYPE_LIBRARIES                = "";
    String TYPE_DOCUMENT_FAMILY          = "";
    String RELATIONSHIP_HAS_BOOKSHELVES  = "";
    String RELATIONSHIP_HAS_BOOKS        = "";
    String RELATIONSHIP_HAS_DOCUMENTS    = "";
    String RELATIONSHIP_SUBCLASS         = "";


    final String docLibInterface         = "DocumentLibraryMigrartion";
    int flatFileNoUnderProcess           = 1;
    String physicalId                    = "physicalid";

    String program_SystemInformation     = "eServiceSystemInformation.tcl";

    String migrationRunNoProperty        = "LibraryCentralDocumentSchemaMigrationR213RunNo";
    String migrationStatusProperty       = "LibraryCentralDocumentSchemaMigrationR213";

    String state_PreMigrationInProcess   = "PreMigrationInProcess";
    String state_PreMigrationComplete    = "PreMigrationComplete";
    String state_MigrationInProcess      = "MigrationInProcess";
    String state_MigrationComplete       = "MigrationComplete";
    String state_PostMigrationInProcess  = "PostMigrationInProcess";
    String state_PostMigrationComplete   = "PostMigrationComplete";

    String migrationlogName              = "migration.log";
    String migratedObjectIdsLogName      = "convertedIds.txt";
    String fileStatusLogName             = "fileStatus.csv";
    String failedObjectIdsLogNamePrefix  = "unConvertedObjectIds_";
    String failedObjectIdsLogNameSuffix  = ".csv";

    public emxLibraryCentalDocumentSchemaMigrationBase_mxJPO (Context context, String[] args)
    throws Exception
    {
        super(context, args);
        TYPE_DOCUMENT_LIBRARY         = PropertyUtil.getSchemaProperty(context,"type_Library");
        TYPE_BOOKSHELF                = PropertyUtil.getSchemaProperty(context,"type_Bookshelf");
        TYPE_BOOK                     = PropertyUtil.getSchemaProperty(context,"type_Book");
        TYPE_LIBRARIES                = PropertyUtil.getSchemaProperty(context,"type_Libraries");
        TYPE_DOCUMENT_FAMILY          = PropertyUtil.getSchemaProperty(context,"type_DocumentFamily");
        RELATIONSHIP_HAS_BOOKSHELVES  = PropertyUtil.getSchemaProperty(context,"relationship_HasBookshelves");
        RELATIONSHIP_HAS_BOOKS        = PropertyUtil.getSchemaProperty(context,"relationship_HasBooks");
        RELATIONSHIP_HAS_DOCUMENTS    = PropertyUtil.getSchemaProperty(context,"relationship_HasDocuments");
        RELATIONSHIP_SUBCLASS         = PropertyUtil.getSchemaProperty(context,"relationship_Subclass");

    }



    /**
     * This method is executed if a specific method is not specified
     * @param context the eMatrix <code>Context</code> object
     * @param args holds three arguments
     *        args[0] - Directory where files containing ObjectIds are to be read
     *        args[1] - minimum Range (ie. Flat file to start migration from)
     *        args[2] - maximum Range (ie. Flat file till which migration is to be run)
     *
     * @returns 0 always
     * @throws Exception if the migration fails
     */
    public int mxMain(Context context,String[] args)throws Exception{
        setAdminMigrationStatus(context,migrationRunNoProperty,"increment");
        // write Run No. to log Files
        writeRunNoToLogs(context);

        preMigration(context, args);

        setAdminMigrationStatus(context,migrationStatusProperty, state_MigrationInProcess);
        // call Main Migration
        writer = new BufferedWriter(new MatrixWriter(context));
        super.mxMain(context, args);

        libraryCentralCreateLogs(context);
        String noOfUnConvertedDocLibraries = MqlUtil.mqlCommand(context, "eval expr $1 on temp query bus $2 $3 $4 where $5", "count TRUE", TYPE_DOCUMENT_LIBRARY, "*", "*", "interface["+docLibInterface+"] == FALSE");
        try{
            if(Integer.parseInt(noOfUnConvertedDocLibraries) == 0){
                migrateDocumentLibraryType(context,args);
                mqlLogRequiredInformationWriter("=======================================================\n");
                mqlLogRequiredInformationWriter("                Migrating  type "+TYPE_DOCUMENT_LIBRARY+"  COMPLETED\n");
                mqlLogRequiredInformationWriter(" \n");
                mqlLogRequiredInformationWriter("Step 3 of Migration :     COMPLETE \n");
                mqlLogRequiredInformationWriter(" \n");
                mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: "+migrationlogName+"\n\n");
                mqlLogRequiredInformationWriter("=======================================================\n");
                setAdminMigrationStatus(context,migrationStatusProperty, state_MigrationComplete);
            }else{
                throw new Exception("INCOMPLETE_STRUCTURE_MIGRATION");
            }
        }catch(Exception e){
            mqlLogRequiredInformationWriter("=======================================================\n");
            mqlLogRequiredInformationWriter("                Migrating  type "+TYPE_DOCUMENT_LIBRARY+"  FAILED\n");
            mqlLogRequiredInformationWriter(" \n");
            mqlLogRequiredInformationWriter("Step 3 of Migration :     FAILED \n");
            mqlLogRequiredInformationWriter(" \n");
            if(e.getMessage().equals("INCOMPLETE_STRUCTURE_MIGRATION")){
                // Document Library type can not be migrated since all 'Document Library' objects are not migarted
                mqlLogRequiredInformationWriter("                there are "+noOfUnConvertedDocLibraries+" '"+TYPE_DOCUMENT_LIBRARY+"' objects which are not Migrated\n");
            }else{
                // Error in migrating Document Library type
                mqlLogRequiredInformationWriter("=="+e.getMessage()+"==\n");
            }
            mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: "+migrationlogName+"\n\n");
            mqlLogRequiredInformationWriter("=======================================================\n");
        }finally{
            libraryCentralCloseLogs();
        }

        return 0;
    }

    /**
     * This method is executed for each Flat file
     *      this method migrates whole structure under each 'Document Library' object in objectList
     * @param context the eMatrix <code>Context</code> object
     * @param objectList - contains the list of ObjectIds in a specific Flat file
     * @returns nothing
     * @throws Exception if operation fails
     */
    public void migrateObjects(Context context, StringList objectList) throws Exception{
        int lengthObjectList = objectList.size();
        for(int docLibItr = 0; docLibItr < lengthObjectList; docLibItr++){
            String objectId_ObjectUnderProcess  = (String)objectList.get(docLibItr);
            try{
                DomainObject objectUnderProcess      = new DomainObject(objectId_ObjectUnderProcess);

                // getting Object type and physical Id
                StringList selectables = new StringList();
                selectables.add(DomainObject.SELECT_TYPE);
                selectables.add(physicalId);
                Map info_ObjectUnderProcess          = objectUnderProcess.getInfo(context,selectables);

                String type_ObjectUnderProcess       = (String)info_ObjectUnderProcess.get(DomainObject.SELECT_TYPE);
                String physicalId_ObjectUnderProcess = (String)info_ObjectUnderProcess.get(physicalId);


                //orphan bookshelves /books
                if(type_ObjectUnderProcess.equals(TYPE_BOOKSHELF)){
                    String[] args = new String[4];
                    args[0]       = objectId_ObjectUnderProcess;
                    args[3]       = "false";

                    migrateBookshelf(context, args);
                    continue;
                } else if(type_ObjectUnderProcess.equals(TYPE_BOOK)){
                    String[] args = new String[4];
                    args[0]       = objectId_ObjectUnderProcess;
                    args[3]       = "false";
                    migrateBook(context, args);
                    continue;
                }

                //If not Orphan Bookshelves / Books, Migrate as Document Library
                String objectId_DLUnderProcess   = objectId_ObjectUnderProcess;
                DomainObject DLUnderProcess      = objectUnderProcess;
                String physicalId_DLUnderProcess = physicalId_ObjectUnderProcess;

                //mxsysInterface name is same as the physicalId of the Object
                String mxsysInterfaceName_DLUnderProcess = physicalId_DLUnderProcess;

                //debug information
                mqlLogWriter("Migrating: Document Library: OBJECT_ID = " + objectId_DLUnderProcess + "\n");


                // Add interface with attributes 'Approver', 'Created On','Count' and 'mxsysInterface' to each Document Library
                // to retain these attributes
                MqlUtil.mqlCommand(context,"modify bus $1 add Interface $2", objectId_DLUnderProcess, docLibInterface);

                // create and attach mxsysInterface Interface
                createClassificationInterface(context,objectId_DLUnderProcess,mxsysInterfaceName_DLUnderProcess,"Classification Taxonomies",true);

                //find all child Bookshelf objects
                MapList childBookshelfs = DLUnderProcess.getRelatedObjects(context,
                                                                           RELATIONSHIP_HAS_BOOKSHELVES,
                                                                           "*",
                                                                           new StringList("id"),
                                                                           null,
                                                                           false,
                                                                           true,
                                                                           new Short("1"),
                                                                           "",
                                                                           "",
                                                                           0);

                int noOfClassifiedItems     = 0;
                int noOfChildBookshelfOIDs  = childBookshelfs.size();

                for(int bookshlefItr = 0; bookshlefItr < noOfChildBookshelfOIDs; bookshlefItr++){
                    //for each child Bookshelf, Migrate Bookshelf and Sum 'number of ClassifiedItems' under each Bookshelf
                    String objectId_BookshelfUnderProcess = (String)((Map)childBookshelfs.get(bookshlefItr)).get("id");

                    String[] newArgs = new String[4];
                    newArgs[0]       = objectId_BookshelfUnderProcess;
                    newArgs[1]       = objectId_DLUnderProcess;
                    newArgs[2]       = physicalId_DLUnderProcess;
                    newArgs[3]       = "true";

                    migrateBookshelf(context, newArgs);
                    int noOfClassifiedItemsUnderBookshelf = 0;
                    try{
                        DomainObject BookshelfUnderProcess = new DomainObject(newArgs[0]);
                        noOfClassifiedItemsUnderBookshelf  = Integer.parseInt(BookshelfUnderProcess.getInfo(context, "attribute[Count].value"));
                    }catch(NumberFormatException nfe){
                        // Do nothing
                    }
                    noOfClassifiedItems += noOfClassifiedItemsUnderBookshelf;
                }

                // Update Count attribute of DLUnderProcess
                String mqlQuery = "modify bus $1 Count $2";
                MqlUtil.mqlCommand(context, mqlQuery, objectId_DLUnderProcess, String.valueOf(noOfClassifiedItems));

                //log Success
                loadMigratedOids(objectId_ObjectUnderProcess);
            }catch(Exception e){
                writeUnconvertedOID(context,docLibItr,objectId_ObjectUnderProcess,e.getMessage());
                throw e;
            }
        }
    }

    /**
     * this method migrates whole structure below 'BookshelfUnderProcess'
     * @param context the eMatrix <code>Context</code> object
     * @param args - String array containing
     *        args[0] - objectId_BookshelfUnderProcess - OID of the BookshelfUnderProcess of migration
     *        args[1] - objectId_DLUnderProcess - OID of the DLUnderProcess of migration
     *        args[2] - physicalId_DLUnderProcess - physical Id of the DLUnderProcess of migration
     *        args[3] - hasParent - "true" if BookshelfUnderProcess of migration is connected to atleast one 'Document Library'
     * @returns 0
     * @throws Exception if operation fails
     */
    public int migrateBookshelf(Context context,String[] args) throws Exception{
        if(args.length < 4){
            throw new IllegalArgumentException("Lesser number of arguments");
        }
        String objectId_BookshelfUnderProcess  = args[0];
        String objectId_DLUnderProcess         = args[1];
        String physicalId_DLUnderProcess       = args[2];
        boolean hasParent                      = args[3].equalsIgnoreCase("true")?true:false;

        DomainObject BookshelfUnderProcess     = new DomainObject(objectId_BookshelfUnderProcess);

        //debug information
        mqlLogWriter("    Migrating: Bookshelf: OBJECT_ID = " + objectId_BookshelfUnderProcess + "\n");

        // getting name revision and physical Id of BookshelfUnderProcess
        StringList selectables = new StringList();
        selectables.add(DomainObject.SELECT_NAME);
        selectables.add(DomainObject.SELECT_REVISION);
        selectables.add(physicalId);
        selectables.add(DomainObject.SELECT_OWNER);
        selectables.add(DomainObject.SELECT_CURRENT);

        Map info_BookshelfUnderProcess          = BookshelfUnderProcess.getInfo(context,selectables);

        String name_BookshelfUnderProcess       = (String)info_BookshelfUnderProcess.get(DomainObject.SELECT_NAME);
        String revision_BookshelfUnderProcess   = (String)info_BookshelfUnderProcess.get(DomainObject.SELECT_REVISION);
        String physicalId_BookshelfUnderProcess = (String)info_BookshelfUnderProcess.get(physicalId);
        String owner_BookshelfUnderProcess      = (String)info_BookshelfUnderProcess.get(DomainObject.SELECT_OWNER);
        String current_BookshelfUnderProcess    = (String)info_BookshelfUnderProcess.get(DomainObject.SELECT_CURRENT);

        //mxsysInterface name is same as the physicalId of the Object
        String mxsysInterfaceName_BookshelfUnderProcess = physicalId_BookshelfUnderProcess;
        String mxsysInterfaceName_DLUnderProcess        = physicalId_DLUnderProcess;

        if(hasParent){
            //Find all the parent 'Document Library' of BookshelfUnderProcess
            MapList parentOIDs_BookshelfUnderProcess     = BookshelfUnderProcess.getRelatedObjects(context,
                                                                                                   RELATIONSHIP_HAS_BOOKSHELVES,
                                                                                                   "*",
                                                                                                   new StringList("id"),
                                                                                                   null,
                                                                                                   true,
                                                                                                   false,
                                                                                                   new Short("1"),
                                                                                                   "",
                                                                                                   "",
                                                                                                   0);

            int noOfParentDLs = parentOIDs_BookshelfUnderProcess.size();

            if(noOfParentDLs > 1){
                //if BookshelfUnderProcess is connected to more than one parent then clone it
                MqlUtil.mqlCommand(context, "copy bus $1 to $2 $3 history", objectId_BookshelfUnderProcess, name_BookshelfUnderProcess+"."+revision_BookshelfUnderProcess, physicalId_DLUnderProcess);
                String clonedObjectID = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", TYPE_BOOKSHELF, name_BookshelfUnderProcess+"."+revision_BookshelfUnderProcess, physicalId_DLUnderProcess, "id");

                //debug information
                mqlLogWriter("    Cloning: Bookshelf: OBJECT_ID = " + objectId_BookshelfUnderProcess + ",  CLONED_OBJECT_ID = "+clonedObjectID+"\n");

                //restoring owner and current state in cloned object
                MqlUtil.mqlCommand(context, "mod bus $1 owner $2",clonedObjectID,owner_BookshelfUnderProcess);
                MqlUtil.mqlCommand(context, "mod bus $1 current $2",clonedObjectID,current_BookshelfUnderProcess);

                //disconnect cloned Object from All parents except DLUnderProcess
                for(int docLibItr = 0;docLibItr < noOfParentDLs; docLibItr++){
                    String objectId_parentDL = (String)((Map)parentOIDs_BookshelfUnderProcess.get(docLibItr)).get("id");
                    if(!(objectId_parentDL.equals(objectId_DLUnderProcess))){
                        MqlUtil.mqlCommand(context, "disconnect bus $1 relationship $2 from $3", clonedObjectID, RELATIONSHIP_HAS_BOOKSHELVES, objectId_parentDL);
                    }
                }

                //disconnect BookshelfUnderProcess from DLUnderProcess
                MqlUtil.mqlCommand(context, "disconnect bus $1 relationship $2 from $3", objectId_BookshelfUnderProcess, RELATIONSHIP_HAS_BOOKSHELVES, objectId_DLUnderProcess);

                //consider clonedObject as BookshelfUnderProcess
                objectId_BookshelfUnderProcess           = clonedObjectID;
                BookshelfUnderProcess                    = new DomainObject(clonedObjectID);
                physicalId_BookshelfUnderProcess         = BookshelfUnderProcess.getInfo(context, "physicalid");
                mxsysInterfaceName_BookshelfUnderProcess = physicalId_BookshelfUnderProcess;

                // 0th element of args array is updated with the Object Id of the cloned object
                // this is to access cloned object from calling method
                args[0] = objectId_BookshelfUnderProcess;
            }

            MqlUtil.mqlCommand(context, "modify bus $1 type $2", objectId_BookshelfUnderProcess,TYPE_DOCUMENT_FAMILY);

            if(!(noOfParentDLs > 1)){
                //if BookshelfUnderProcess is not cloned object then Append revision to its name and
                //change revision to physicalid of the DLUnderProcess
                MqlUtil.mqlCommand(context, "modify bus $1 name $2 revision $3", objectId_BookshelfUnderProcess, name_BookshelfUnderProcess+"."+revision_BookshelfUnderProcess, physicalId_DLUnderProcess);
            }

            //change the 'Has Bookshelves' relationship to Subclass
            String relationshipId = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", objectId_BookshelfUnderProcess, "to[Has Bookshelves].id", "|");
            MqlUtil.mqlCommand(context,"modify connection $1 type $2", relationshipId, RELATIONSHIP_SUBCLASS);

            // create mxsysInterface interface have name as physicalId of BookshelfUnderProcess and attach to BookshelfUnderProcess
            createClassificationInterface(context,objectId_BookshelfUnderProcess,mxsysInterfaceName_BookshelfUnderProcess,mxsysInterfaceName_DLUnderProcess,true);
        } else {
            //orphan bookshelves
            MqlUtil.mqlCommand(context, "modify bus $1 type $2", objectId_BookshelfUnderProcess,TYPE_DOCUMENT_FAMILY);

            // create mxsysInterface interface have name as physicalId of BookshelfUnderProcess and attach to BookshelfUnderProcess
            createClassificationInterface(context,objectId_BookshelfUnderProcess,mxsysInterfaceName_BookshelfUnderProcess,null,false);
        }

        //find all child 'Book' Objects
        MapList childBooks          = BookshelfUnderProcess.getRelatedObjects(context,
                                                                              RELATIONSHIP_HAS_BOOKS,
                                                                              "*",
                                                                              new StringList("id"),
                                                                              null,
                                                                              false,
                                                                              true,
                                                                              new Short("1"),
                                                                              "",
                                                                              "",
                                                                              0);

        int noOfClassifiedItems     = 0;
        int noOfChildBookOIDs       = childBooks.size();

        for(int bookItr = 0;bookItr < noOfChildBookOIDs; bookItr++){
            //for each child Book, Migrate Book and Sum 'number of ClassifiedItems' under each Book
            String objectId_BookUnderProcess = (String)((Map)childBooks.get(bookItr)).get("id");

            String[] newArgs = new String[4];
            newArgs[0]       = objectId_BookUnderProcess;
            newArgs[1]       = objectId_BookshelfUnderProcess;
            newArgs[2]       = physicalId_BookshelfUnderProcess;
            newArgs[3]       = "true";

            migrateBook(context, newArgs);
            int noOfClassifiedItemsUnderBook = 0;
            try{
                DomainObject BookUnderProcess = new DomainObject(newArgs[0]);
                noOfClassifiedItemsUnderBook = Integer.parseInt(BookUnderProcess.getInfo(context, "attribute[Count].value"));
            }catch(NumberFormatException nfe){
                // Do nothing
            }
            noOfClassifiedItems += noOfClassifiedItemsUnderBook;
        }

        // Update Count attribute of BookshelfUnderProcess
        MqlUtil.mqlCommand(context,"modify bus $1 Count $2", objectId_BookshelfUnderProcess, noOfClassifiedItems+"");

        return 0;
    }

    /**
     * this method migrates whole structure below 'BookUnderProcess'
     * @param context the eMatrix <code>Context</code> object
     * @param objectId_BookUnderProcess - OID of the BookUnderProcess of migration
     * @param objectId_BookshelfUnderProcess - OID of the BookshelfUnderProcess of migration
     * @param physicalId_BookshelfUnderProcess - Physical Id of the BookshelfUnderProcess of migration
     * @param hasParent - true if BookUnderProcess of migration is connected to atleast one 'Bookshelf'
     * @returns 0
     * @throws Exception if operation fails
     */
    public int migrateBook(Context context,String[] args) throws Exception{
        if(args.length < 4){
            throw new IllegalArgumentException("Lesser number of arguments");
        }
        String objectId_BookUnderProcess         = args[0];
        String objectId_BookshelfUnderProcess    = args[1];
        String physicalId_BookshelfUnderProcess  = args[2];
        boolean hasParent                        = args[3].equalsIgnoreCase("true")?true:false;

        DomainObject BookUnderProcess       = new DomainObject(objectId_BookUnderProcess);

        //debug information
        mqlLogWriter("        Migrating: Book: OBJECT_ID = " + objectId_BookUnderProcess + "\n");

        // getting name revision and physical Id of BookshelfUnderProcess
        StringList selectables = new StringList();
        selectables.add(DomainObject.SELECT_NAME);
        selectables.add(DomainObject.SELECT_REVISION);
        selectables.add(physicalId);
        selectables.add(DomainObject.SELECT_OWNER);
        selectables.add(DomainObject.SELECT_CURRENT);
        Map info_BookUnderProcess           = BookUnderProcess.getInfo(context,selectables);

        String name_BookUnderProcess        = (String)info_BookUnderProcess.get(DomainObject.SELECT_NAME);
        String revision_BookUnderProcess    = (String)info_BookUnderProcess.get(DomainObject.SELECT_REVISION);
        String physicalId_BookUnderProcess  = (String)info_BookUnderProcess.get(physicalId);
        String owner_BookUnderProcess       = (String)info_BookUnderProcess.get(DomainObject.SELECT_OWNER);
        String current_BookUnderProcess     = (String)info_BookUnderProcess.get(DomainObject.SELECT_CURRENT);

        //mxsysInterface name is same as the physicalId of the Object
        String mxsysInterfaceName_BookUnderProcess      = physicalId_BookUnderProcess;
        String mxsysInterfaceName_BookshelfUnderProcess = physicalId_BookshelfUnderProcess;

        if(hasParent) {
            //Find all the parent Bookshelf of BookUnderProcess
            MapList parentOIDs_BookUnderProcess = BookUnderProcess.getRelatedObjects(context,
                                                                                     RELATIONSHIP_HAS_BOOKS,
                                                                                     "*",
                                                                                     new StringList("id"),
                                                                                     null,
                                                                                     true,
                                                                                     false,
                                                                                     new Short("1"),
                                                                                     "",
                                                                                     "",
                                                                                     0);

            int noOfParentBookShelfs = parentOIDs_BookUnderProcess.size();

            if(noOfParentBookShelfs>1){
                //if BookUnderProcess is connected to more than one parent then clone it
                MqlUtil.mqlCommand(context, "copy bus $1 to $2 $3 history", objectId_BookUnderProcess, name_BookUnderProcess+"."+revision_BookUnderProcess, physicalId_BookshelfUnderProcess);
                String clonedObjectID = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", TYPE_BOOK, name_BookUnderProcess+"."+revision_BookUnderProcess, physicalId_BookshelfUnderProcess, "id");

                //debug information
                mqlLogWriter("        Cloning: Book: OBJECT_ID = " + objectId_BookUnderProcess + ",  CLONED_OBJECT_ID = "+clonedObjectID+"\n");

                //restoring owner and current state in cloned object
                MqlUtil.mqlCommand(context, "mod bus $1 owner $2",clonedObjectID,owner_BookUnderProcess);
                MqlUtil.mqlCommand(context, "mod bus $1 current $2",clonedObjectID,current_BookUnderProcess);

                //disconnect cloned Object from All parents except BookshelfUnderProcess
                for(int bookshelfItr = 0;bookshelfItr < noOfParentBookShelfs; bookshelfItr++){
                    String objectId_parentBookshelf = (String)((Map)parentOIDs_BookUnderProcess.get(bookshelfItr)).get("id");
                    if(!(objectId_parentBookshelf.equals(objectId_BookshelfUnderProcess))){
                        MqlUtil.mqlCommand(context, "disconnect bus $1 relationship $2 from $3", clonedObjectID, RELATIONSHIP_HAS_BOOKS, objectId_parentBookshelf);
                    }
                }

                //disconnect BookUnderProcess from BookshelfUnderProcess
                MqlUtil.mqlCommand(context, "disconnect bus $1 relationship $2 from $3", objectId_BookUnderProcess, RELATIONSHIP_HAS_BOOKS, objectId_BookshelfUnderProcess);

                //consider clonedObject as BookUnderProcess
                objectId_BookUnderProcess           = clonedObjectID;
                BookUnderProcess                    = new DomainObject(clonedObjectID);
                physicalId_BookUnderProcess         = BookUnderProcess.getInfo(context, "physicalid");
                mxsysInterfaceName_BookUnderProcess = physicalId_BookUnderProcess;

                // 0th element of args array is updated with the Object Id of the cloned object
                // this is to access cloned object from calling method
                args[0] = objectId_BookUnderProcess;
            }

            MqlUtil.mqlCommand(context, "modify bus $1 type $2", objectId_BookUnderProcess,TYPE_DOCUMENT_FAMILY);

            if(!(noOfParentBookShelfs>1)){
                //if BookUnderProcess is not cloned object then Append revision to its name and
                //change revision to physicalid of the BookshelfUnderProcess
                MqlUtil.mqlCommand(context, "modify bus $1 name $2 revision $3", objectId_BookUnderProcess, name_BookUnderProcess+"."+revision_BookUnderProcess, physicalId_BookshelfUnderProcess);
            }

            //change the 'Has Books' relationship to Subclass
            String relationshipId = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", objectId_BookUnderProcess, "to[Has Books].id", "|");

            MqlUtil.mqlCommand(context,"modify connection $1 type $2", relationshipId, RELATIONSHIP_SUBCLASS);
            // create mxsysInterface interface and attach to BookUnderProcess
            createClassificationInterface(context,objectId_BookUnderProcess,mxsysInterfaceName_BookUnderProcess,mxsysInterfaceName_BookshelfUnderProcess,true);
        } else {
            //orphan books
            MqlUtil.mqlCommand(context, "modify bus $1 type $2", objectId_BookUnderProcess,TYPE_DOCUMENT_FAMILY);

            // create mxsysInterface interface and attach to BookUnderProcess
            createClassificationInterface(context,objectId_BookUnderProcess,mxsysInterfaceName_BookUnderProcess,null,false);
        }

        //find all child 'Generic Documents' and 'Has Documents' relationships
        selectables      = new StringList();
        selectables.add("id");
        selectables.add("to.id");
        MapList mapListChildGenDocs = BookUnderProcess.getRelatedObjects(context,
                                                                         RELATIONSHIP_HAS_DOCUMENTS,
                                                                         "*",
                                                                         null,
                                                                         selectables,
                                                                         false,
                                                                         true,
                                                                         new Short("1"),
                                                                         "",
                                                                         "",
                                                                         0);

        int noOfChildGenDocs     = mapListChildGenDocs.size();
        for(int hasDocRelItr = 0; hasDocRelItr < noOfChildGenDocs; hasDocRelItr++){
            //change the 'Has Documents' relationship to 'Classified Item'
            MqlUtil.mqlCommand(context,"modify connection $1 type $2", (String)((Map)mapListChildGenDocs.get(hasDocRelItr)).get("id"), "Classified Item");

            // add mxsysInterface of BookUnderProcess to 'Generic Document'
            MqlUtil.mqlCommand(context,"modify bus $1 add interface $2", (String)((Map)mapListChildGenDocs.get(hasDocRelItr)).get("to.id"), mxsysInterfaceName_BookUnderProcess);
        }

        // Update the count of child 'Generic Documents' to Count attribute of BookUnderProcess
        MqlUtil.mqlCommand(context,"modify bus $1 Count $2", objectId_BookUnderProcess, noOfChildGenDocs+"");

        return 0;
    }


    /**
     * This method is executed for pre-migration changes required before starting actual Migration
     * pre migration is executed only once
     * @param context
     * @param args holds nothing
     * @return 0 always
     * @throws Exception is operation Fails
     */
    public int preMigration(Context context, String[] args)throws Exception{
        if(getAdminMigrationStatus(context,migrationStatusProperty) < 2){
            try{
                libraryCentralCreateLogs(context);
                //creating interface 'Document Classification Migration' with attributes 'Approver', 'Created On','Count' and 'mxsysInterface'
                //Update Subclass relationship from types (Document Library,Bookshelf) to types (Bookshelf,Book)
                setAdminMigrationStatus(context,migrationStatusProperty, state_PreMigrationInProcess);
                MqlUtil.mqlCommand(context, "add interface $1 attribute $2 attribute $3 attribute $4 attribute $5 type $6", docLibInterface, "Approver", "Created On", "Count", "mxsysInterface", "All");
                MqlUtil.mqlCommand(context, "modify relationship $1 from add type $2 ", RELATIONSHIP_SUBCLASS, TYPE_DOCUMENT_LIBRARY);
                setAdminMigrationStatus(context,migrationStatusProperty, state_PreMigrationComplete);
                mqlLogRequiredInformationWriter("PreMigration Successfull\n");
            }catch(Exception e){
                mqlLogRequiredInformationWriter("====================================================================\n");
                mqlLogRequiredInformationWriter("PreMigration Failed     \n");
                mqlLogRequiredInformationWriter("Step 2 of Migration :     FAILED \n");
                mqlLogRequiredInformationWriter("====================================================================\n");
                throw e;
            }finally{
                libraryCentralCloseLogs();
            }
        }
        return 0;
    }

    /**
     * this method modifies parent type of 'Document Library' to Libraries
     * and adds attributes 'Approver' and 'Created On' to type 'Document Library'
     * @param context the eMatrix <code>Context</code> object
     * @param args holds nothing
     * @return 0 always
     * @throws Exception is operation Fails
     */
    public int migrateDocumentLibraryType(Context context, String[] args)throws Exception{
        //changing the parentType of 'Document Library' to 'Libraries'
        //adding attributes 'Approver' and 'Created On' to type 'Document Library'
        String result = MqlUtil.mqlCommand(context, "modify type $1 derived $2 add attribute $3 add attribute $4", TYPE_DOCUMENT_LIBRARY, TYPE_LIBRARIES, "Approver", "Created On");

        return 0;
    }

    /**
     * This method creates Classification Interface and
     * updates attribute[mxsysInterface] with the name of the created Interface
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId_objectUnderProcess        objectId of the Object to which interface should be created
     * @param interfaceName_objectUnderProcess   interface name of the interface to be created
     * @param name_parentInterface               name of the parent interface , if present
     * @param hasParent                          true if parent interface is present, false otherwise
     * @returns nothing
     * @throws Exception if the operation fails
     */
    public void createClassificationInterface(Context context, String objectId_objectUnderProcess, String interfaceName_objectUnderProcess, String name_parentInterface,boolean hasParent)throws Exception{
        if(hasParent){
            MqlUtil.mqlCommand(context,"add interface $1 derived $2 type $3", interfaceName_objectUnderProcess, name_parentInterface, "All");
        }else{
            MqlUtil.mqlCommand(context,"add interface $1 type $2", interfaceName_objectUnderProcess, "All");
        }
        MqlUtil.mqlCommand(context,"modify bus $1 $2 $3", objectId_objectUnderProcess, "mxsysInterface", interfaceName_objectUnderProcess);
    }

    /**
     * Sets the migration status as a property setting.
     * @param context the eMatrix <code>Context</code> object
     * @param strStatus String containing the status setting
     * @throws Exception
     */
    public void setAdminMigrationStatus(Context context,String name, String strStatus) throws Exception
    {
        if(name.equals(migrationRunNoProperty)){
            if(strStatus.equalsIgnoreCase("increment")){
                // If name is LibraryCentalDocumentSchemaMigrationR213RunNo and strStatus increment then value of LibraryCentalDocumentSchemaMigrationR213RunNo is incremented
                int lastRunNo = getAdminMigrationStatus(context, name);
                MqlUtil.mqlCommand(context, "modify program $1 property $2 value $3", program_SystemInformation, name, (lastRunNo+1)+"");
            }else{
                MqlUtil.mqlCommand(context, "modify program $1 property $2 value $3", program_SystemInformation, name, "0");
            }
        }else{
            MqlUtil.mqlCommand(context, "modify program $1 property $2 value $3", program_SystemInformation, name, strStatus);
        }
    }

    /**
     * Gets the migration status as an integer value.  Used to enforce an order of migration.
     * @param context the eMatrix <code>Context</code> object
     * @return integer representing the status
     * @throws Exception
     */
    public int getAdminMigrationStatus(Context context,String name) throws Exception
    {
        if(name.equals(migrationRunNoProperty)){
            String result = MqlUtil.mqlCommand(context, "print program $1 select $2 dump", program_SystemInformation, "property["+name+"].value");
            try{
                return Integer.parseInt(result);
            }catch(NumberFormatException nfe){
                return 0;
            }
        }

        String result   = MqlUtil.mqlCommand(context, "print program $1 select $2 dump", program_SystemInformation, "property["+name+"].value");

        if(result.equalsIgnoreCase(state_PreMigrationInProcess))
        {
            return 1;
        }else if(result.equalsIgnoreCase(state_PreMigrationComplete))
        {
            return 2;
        }else if(result.equalsIgnoreCase(state_MigrationInProcess))
        {
            return 3;
        }else if(result.equalsIgnoreCase(state_MigrationComplete))
        {
            return 4;
        }else if(result.equalsIgnoreCase(state_PostMigrationInProcess))
        {
            return 4;
        }else if(result.equalsIgnoreCase(state_PostMigrationComplete))
        {
            return 4;
        }
        return 0;

    }

    /**
     * this method Opens all the log files Required
     * @return nothing
     * @throws Exception if the operation fails
     */

    public void createLogs() throws Exception{
        errorLog          = new FileWriter(documentDirectory + failedObjectIdsLogNamePrefix+ unconvertedFileCount +failedObjectIdsLogNameSuffix, true);
        convertedOidsLog  = new FileWriter(documentDirectory + migratedObjectIdsLogName, true);
        warningLog        = new FileWriter(documentDirectory + migrationlogName, true);
    }

    public void libraryCentralCreateLogs(Context context)throws Exception{
        writer            = new BufferedWriter(new MatrixWriter(context));
        warningLog        = new FileWriter(documentDirectory + migrationlogName, true);
        statusLog         = new FileWriter(documentDirectory + fileStatusLogName, true);
        convertedOidsLog  = new FileWriter(documentDirectory + migratedObjectIdsLogName, true);
        errorLog          = new FileWriter(documentDirectory + failedObjectIdsLogNamePrefix+ unconvertedFileCount +failedObjectIdsLogNameSuffix, true);
    }

    /**
     * this method Closes all the logs created
     * @return nothing
     * @throws Exception if the operation fails
     */

    public void libraryCentralCloseLogs()throws Exception{
        convertedOidsLog.flush();
        convertedOidsLog.close();

        statusLog.flush();
        statusLog.close();

        warningLog.flush();
        warningLog.close();

        writer.flush();
        writer.close();

        errorLog.flush();
        errorLog.close();
    }

    /**
     * this method writes Error Log
     * @param context the eMatrix <code>Context</code> object
     * @param objectId object Id of the object that failed
     * @param reason specifies the Reason for failure
     * @return nothing
     * @throws Exception if the operation fails
     */
    public void writeUnconvertedOID(Context context,int rowNo, String objectId, String reason) throws Exception
    {
        int currentRunNo     = getAdminMigrationStatus(context, migrationRunNoProperty);
        StringBuffer command = new StringBuffer();
        command.append(currentRunNo+",");
        command.append(objectId+",");
        command.append("objectids_"+flatFileNoUnderProcess+".txt,");
        command.append((rowNo+1)+",");

        reason = reason.replaceAll(",", ".");
        reason = reason.replaceAll("\\n", "|");
        reason = reason.replaceAll("\\r", "");
        command.append(reason);
        command.append("\n");
        failureId = objectId;

        if(unconvertedObjectCount < unconvertedChunkSize)
        {
            errorLog.write(command.toString());
            errorLog.flush();
            unconvertedObjectCount++;
        } else {
            errorLog.close();
            unconvertedObjectCount = 1;
            unconvertedFileCount ++;
            errorLog   = new FileWriter(documentDirectory + failedObjectIdsLogNamePrefix+ unconvertedFileCount +failedObjectIdsLogNameSuffix, true);
            errorLog.write("RUN NO,OBJECTID,FILE NAME,ROW NO,REASON \n");
            errorLog.write(command.toString());
            errorLog.flush();
        }
    }

    public void writeRunNoToLogs(Context context)throws Exception{
        libraryCentralCreateLogs(context);
        int currentRunNo = getAdminMigrationStatus(context, migrationRunNoProperty);

        mqlLogRequiredInformationWriter("==================================================================\n");
        mqlLogRequiredInformationWriter("Run No: "+currentRunNo+"\n");
        mqlLogRequiredInformationWriter("==================================================================\n");

        if(currentRunNo == 1){
            errorLog.write("RUN NO,OBJECTID,FILE NAME,ROW NO,REASON \n");
        }

        statusLog.write("\nRun No: "+currentRunNo+"\n");
        convertedOidsLog.write("\nRun No: "+currentRunNo+"\n");

        libraryCentralCloseLogs();

    }

    /**
     * this is a help method which displays help about migration on console
     * command to be executed to get help on console is
     *      execute program emxLibraryCentalDocumentSchemaMigration -method help;
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds nothing
     * @return nothing
     * @throws Exception
     */
    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        }

        writer.write("================================================================================================\n");
        writer.write(" Migration is a Two step process  \n");
        writer.write(" Step1: Find all \n");
        writer.write("        objects of type 'Document Library', \n");
        writer.write("        Objects of type 'Bookshelf' which are not connected to any 'document Library', \n");
        writer.write("        Objects of type 'Book' which are not connected to any 'Bookshelf' \n");
        writer.write("        and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write("   execute program emxLibraryCentalDocumentSchemaMigrationFindObjects 1000 'C:/Temp/oids/' \n");
        writer.write("   First parameter  = [OPTIONAL] 1000 indicates no of oids per file. By default 100, if not specified  \n");
        writer.write("   Second Parameter = C:/Temp/oids/ is the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Run Migration Procedure\n");
        writer.write(" Example: \n");
        writer.write("   execute program emxLibraryCentalDocumentSchemaMigration 'C:/Temp/oids/' 1 n; \n");
        writer.write("   First parameter  = C:/Temp/oids/ is the directory where files containing ObjectIds are to be read \n");
        writer.write("   Second Parameter = 1 minimum range  \n");
        writer.write("   Third Parameter  = n maximum range  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write(" \n");
        writer.write("================================================================================================\n");
        writer.close();
    }

    /**
     * This method sets flatFileNoUnderProcess and
     * Calls super method which Reads the contents of the file and puts in Arraylist.
     *
     * @param args i holds the suffux of filename to identify the file.
     * @returns ArrayList of objectIds present in the file
     * @throws Exception if the operation fails
     */
    public StringList readFiles(int i)throws Exception{
        flatFileNoUnderProcess = i;
        return super.readFiles(i);
    }
}
