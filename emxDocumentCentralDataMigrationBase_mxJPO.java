/* emxDocumentCentralDataMigrationBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.10 Wed Oct 22 16:02:28 2008 przemek Experimental przemek $
*/

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.BufferedWriter;
import java.util.Vector;
import matrix.db.Context;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectItr;
import matrix.db.File;
import matrix.db.FileItr;
import matrix.db.FileList;
import matrix.db.MQLCommand;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.db.RelationshipList;
import matrix.db.RelationshipItr;
import matrix.db.Access;
import matrix.db.BusinessObjectList;
import matrix.db.AccessList;
import matrix.util.StringList;
import matrix.util.SelectList;
import matrix.db.Set;
import matrix.db.Person;
import matrix.db.PersonList;
import matrix.db.PersonItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.JPO;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.document.util.MxDebug;

/**
*
* @exclude
*/

public class emxDocumentCentralDataMigrationBase_mxJPO extends DomainObject
{

  String TYPE_PERSONALFOLDER = null;
  String TYPE_PUBLICFOLDER = null;
  String TYPE_PROJECTVAULT = null;
  String TYPE_DOCUMENTSHEET = null;
  String TYPE_VERSIONDOCUMENT = null;
  String RELATIONSHIP_VAULTEDDOCUMENTREV2 = null;
  String RELATIONSHIP_CONTAINS = null;
  String RELATIONSHIP_SUBVAULTS = null;
  String RELATIONSHIP_VERSION = null;
  String RELATIONSHIP_PUBLISHSUBSCRIBE = null;
  String POLICY_PROJECTVAULT = null;
  String POLICY_VERSIONDOCUMENT = null;
  String VAULT_ESERVICEPRODUCTION = null;
  String VAULT_ESERVICEADMINISTRATION = null;
  String ATTRIBUTE_GLOBALREAD = null;
  String ATTRIBUTE_FILEVERSION = null;
  String ATTRIBUTE_VERSIONDATE = null;
  String ATTRIBUTE_CHECKINREASON = null;
  String ATTRIBUTE_DCTYPENAME = null;
  String ATTRIBUTE_DCNAME = null;
  String ATTRIBUTE_DCGROUP = null;
  String ATTRIBUTE_DCROLE = null;
  String ATTRIBUTE_DCSEQUENCE = null;
  String ATTRIBUTE_DCSIZE = null;
  String ATTRIBUTE_DCREQUIRED = null;
  String ATTRIBUTE_DCPREFIX = null;
  String ATTRIBUTE_DCSEARCHABLE = null;
  String ATTRIBUTE_DCEDITABLE = null;
  String ATTRIBUTE_DCSUFFIX = null;
  String ROLE_EMPLOYEE = null;

  //~ Constructors -----------------------------------------------------------
  /**
   *  Constructs a new JPO object.
   *
   *  @param context the eMatrix <code>Context</code> object
   *  @param args holds no arguments
   *  @throws Exception if the operation fails
   *
   *  @since AEF 9.5.6.0
   */
  public emxDocumentCentralDataMigrationBase_mxJPO ( Context context, String[] args )
         throws Exception
  {
    /*
     *  Author    : Mike Terry
     *  Date      : 02/10/03
     *  Notes     :
     *  History   :
     */

     super ();

     // initialize member variables.
     TYPE_PERSONALFOLDER =
       PropertyUtil.getSchemaProperty ( context, "type_PersonalFolder" );
     TYPE_PUBLICFOLDER =
       PropertyUtil.getSchemaProperty ( context, "type_PublicFolder" );
     TYPE_PROJECTVAULT =
       PropertyUtil.getSchemaProperty ( context, "type_ProjectVault" );
     TYPE_DOCUMENTSHEET =
       PropertyUtil.getSchemaProperty ( context, "type_DocumentSheet" );
     TYPE_VERSIONDOCUMENT =
       PropertyUtil.getSchemaProperty ( context, "type_VersionDocument" );
     RELATIONSHIP_VAULTEDDOCUMENTREV2 =
       PropertyUtil.getSchemaProperty ( context, "relationship_VaultedDocumentsRev2" );
     RELATIONSHIP_CONTAINS =
       PropertyUtil.getSchemaProperty ( context, "relationship_Contains" );
     RELATIONSHIP_SUBVAULTS =
       PropertyUtil.getSchemaProperty ( context, "relationship_SubVaults" );
     RELATIONSHIP_VERSION =
       PropertyUtil.getSchemaProperty ( context, "relationship_Version" );
     RELATIONSHIP_PUBLISHSUBSCRIBE =
       PropertyUtil.getSchemaProperty ( context, "relationship_PublishSubscribe" );
     POLICY_PROJECTVAULT =
       PropertyUtil.getSchemaProperty ( context, "policy_ProjectVault" );
     POLICY_VERSIONDOCUMENT =
       PropertyUtil.getSchemaProperty ( context, "policy_VersionDocument" );
     VAULT_ESERVICEPRODUCTION =
       PropertyUtil.getSchemaProperty ( context, "vault_eServiceProduction" );
     VAULT_ESERVICEADMINISTRATION =
       PropertyUtil.getSchemaProperty ( context, "vault_eServiceAdministration" );
     ATTRIBUTE_GLOBALREAD =
       PropertyUtil.getSchemaProperty ( context, "attribute_GlobalRead" );
     ATTRIBUTE_FILEVERSION =
       PropertyUtil.getSchemaProperty ( context, "attribute_FileVersion" );
     ATTRIBUTE_VERSIONDATE =
       PropertyUtil.getSchemaProperty ( context, "attribute_VersionDate" );
     ATTRIBUTE_CHECKINREASON =
       PropertyUtil.getSchemaProperty ( context, "attribute_CheckinReason" );
     ATTRIBUTE_DCTYPENAME =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCTypeName" );
     ATTRIBUTE_DCNAME =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCName" );
     ATTRIBUTE_DCGROUP =
       PropertyUtil.getSchemaProperty( context, "attribute_DCGroup" );
     ATTRIBUTE_DCROLE =
       PropertyUtil.getSchemaProperty( context, "attribute_DCRole" );
     ATTRIBUTE_DCSEQUENCE =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCSequence" );
     ATTRIBUTE_DCSIZE =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCSize" );
     ATTRIBUTE_DCREQUIRED =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCRequired" );
     ATTRIBUTE_DCPREFIX =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCPrefix" );
     ATTRIBUTE_DCSEARCHABLE =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCSearchable" );
     ATTRIBUTE_DCEDITABLE =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCEditable" );
     ATTRIBUTE_DCSUFFIX =
       PropertyUtil.getSchemaProperty ( context, "attribute_DCSuffix" );
     ROLE_EMPLOYEE =
       PropertyUtil.getSchemaProperty ( context, "role_Employee" );

     if ( !context.isConnected() )
     {
       throw new Exception( "not supported on desktop client" );
     }
  }

   /**
    * This method is executed if a specific method is not specified.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns nothing
    * @throws Exception if the operation fails
    * @since AEF 9.5.6.0
   */
   public int mxMain( Context context, String[] args )
          throws Exception
   {
     /*
      *  Author    : Mike Terry
      *  Date      : 02/10/03
      *  Notes     :
      *  History   :
      */

      if ( !context.isConnected() )
      {
         throw new Exception( "not supported on desktop client" );
      }

      return 0;
   }


  /**
   *  Converts all type_PersonalFolder and type_PublicFolder objects to
   *  be of type_ProjectVault.  The existing objects are cloned and the types
   *  changed to be the new type.  The relations are preserved and changed
   *  to be relationships applicable for the type_ProjectVault.  The
   *  conversion happens as one transaction, all are converted or none on an
   *  exception.
   *
   *  The relationships used by type_ProjectVault are different than the
   *  previous folder relationships.  The cardinality is different for
   *  type_ProjectVault connected to a type_ProjectVault.  The relationship
   *  is one to many in release 2.  The conversion method will connect children
   *  as the relationship will allow. Folders that are connected to multiple
   *  parents, the first one will connected and the others will be caught
   *  as an exception and written to the log file.  These children relationships
   *  can't be migrated.
   *
   *  If type_PersonalFolder or type_PublicFolder are sub-typed, then
   *  the method needs modified to include the sub-types properly.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args input parameters, none expected.
   * @return <code>int</code> return 0 if no exceptions.
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  public int convertFoldersToWorkspaceVault (Context context, String[] args)
      throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    PropertyUtil.loadSymbolicNamesCache(context);


    // if objects need migration, the schema will be temporarily
    // altered.  variable stores status.
    boolean schemaAltered = false;

    //*** get a MapList of Personal Folder and Public Folder objects that
    // need to be migrated to Workspace Vault.
    SelectList busSelects = new SelectList (6);
    busSelects.add (DomainObject.SELECT_ID);
    busSelects.add (DomainObject.SELECT_TYPE);
    busSelects.add (DomainObject.SELECT_NAME);
    busSelects.add (DomainObject.SELECT_OWNER);
    busSelects.add (DomainObject.SELECT_VAULT);
    busSelects.add (DomainObject.SELECT_DESCRIPTION);

    // query for all Personal Folder and Public Folder objects.
    String searchTypes = TYPE_PERSONALFOLDER + "," + TYPE_PUBLICFOLDER;

    MapList queryResultList = DomainObject.findObjects( context,
                                searchTypes,
                                "*",
                                null,
                                busSelects );

    MxDebug.message (MxDebug.DL_3,
                     "Search Types = " + searchTypes + "\n" +
                     "\tqueryResultList.size() = " + queryResultList.size () );

    try
    {


//      startTransaction ( context, true );

      if ( queryResultList.size () > 0 )
      {
        preModifySchema ( context );

        schemaAltered = true;
      }

      //*** for each folder; create a clone of the object, change
      // the type to type_ProjectVault, update attribute values,
      // set grants for public access type_ProjectVaults,
      // preserve subscriptions, delete the existing folder.
      Iterator queryResultItr = queryResultList.iterator();
      while(queryResultItr.hasNext())
      {
        Map map = (Map)queryResultItr.next();

        // get metadata values
        String objectId = (String) map.get(DomainObject.SELECT_ID);
        String objectType = (String) map.get(DomainObject.SELECT_TYPE);
        String objectName = (String) map.get(DomainObject.SELECT_NAME);
        String objectVault = (String) map.get(DomainObject.SELECT_VAULT);
        String objectOwner = (String) map.get(DomainObject.SELECT_OWNER);
        String objectDesc = (String) map.get(DomainObject.SELECT_DESCRIPTION);

        MxDebug.message (MxDebug.DL_5,
                         "objectType  info ...\n" +
                         "\tobjectId = " + objectId + "\n" +
                         "\tobjectName = " + objectName + "\n" +
                         "\tobjectVault = " + objectVault + "\n" +
                         "\tobjectOwner = " + objectOwner + "\n" );

        DomainObject doExistingObject = new DomainObject (objectId);
        doExistingObject.open(context);

        // get unique revision value.
        String autoRev = doExistingObject.getUniqueName ();

        MxDebug.message (MxDebug.DL_7,
                         "convertFoldersToWorkspaceVault 2a ..." );

        // clone the existing folder.
        BusinessObject cloneBo = doExistingObject.clone(context, objectName,
          autoRev, objectVault);

        MxDebug.message (MxDebug.DL_7,
                         "convertFoldersToWorkspaceVault 2b ..." );

        // change the new clone to type_ProjectVault(Workspace Vault).
        BusinessObject workspaceVaultBo = cloneBo.change(context,
          TYPE_PROJECTVAULT,
          objectName,
          autoRev,
          objectVault,
          POLICY_PROJECTVAULT );

        MxDebug.message (MxDebug.DL_7,
                         "convertFoldersToWorkspaceVault 2c ..." );

        String newObjectId = workspaceVaultBo.getObjectId();

        //*** create bean instance and set other attribute info
        WorkspaceVault workspaceVault =
            (WorkspaceVault)DomainObject.newInstance(context, newObjectId);

        workspaceVault.open(context);
        workspaceVault.setOwner( objectOwner );
        workspaceVault.setDescription ( objectDesc );

        MxDebug.message (MxDebug.DL_7,
                         "convertFoldersToWorkspaceVault 2d ..." );

        if (objectType.equals( TYPE_PERSONALFOLDER ))
        {
          workspaceVault.setAttributeValue( context,
                                            ATTRIBUTE_GLOBALREAD,
                                            "False");
        }
        else
        {
          workspaceVault.setAttributeValue( context,
              ATTRIBUTE_GLOBALREAD,
              "True");

          MxDebug.message (MxDebug.DL_7,
                           "convertFoldersToWorkspaceVault 2e ..." );

          //*** public access, grant Employee role read access.
          AccessUtil accessUtil = new AccessUtil();
          Access access = accessUtil.getReadAccess();
          access.setUser(ROLE_EMPLOYEE);
          access.setGrantor(AccessUtil.WORKSPACE_ACCESS_GRANTOR);
          AccessList accessList = new AccessList();
          accessList.addElement(access);
          BusinessObjectList boList = new BusinessObjectList();
          boList.add(new BusinessObject(newObjectId));

          AccessUtil.grantAccess(context, boList, accessList,
                                 AccessUtil.WORKSPACE_ACCESS_GRANTOR );

          MxDebug.message (MxDebug.DL_7,
                           "convertFoldersToWorkspaceVault 2f ..." );
        }

        //*** preserve the any subscription to the folder
        preserveSubscriptions ( context,
                                doExistingObject,
                                workspaceVault );

        MxDebug.message (MxDebug.DL_7,
                         "convertFoldersToWorkspaceVault 2g ..." );

        // close objects
        workspaceVault.update ( context );
        workspaceVault.close ( context );

        doExistingObject.remove (context);
        doExistingObject.update (context);
        doExistingObject.close(context);

        MxDebug.message (MxDebug.DL_7,
                         "convertFoldersToWorkspaceVault 2h ..." );

      } // end of while

      commitTransaction(context);

    }
    catch (Exception e)
    {
      abortTransaction(context);
      MxDebug.exception (e, true);
      throw (new FrameworkException( e.toString() ) );
    }

    finally
    {
      // if schema was changed, set back.
      if (schemaAltered)
      {
        postModifySchema ( context );
      }
    }

    MxDebug.exit ();

    return 0;
  }

  /**
   *  Each user in release 1 has a set automatically created that is
   *  named 'My Workspace'.  The set is the root node for the tree in the
   *  user interface.  The set can contain objects created within the
   *  application.  This method converts the set to a type_ProjectVault.  There
   *  is a transaction around each set migration.
   *
   *  The method makes a copy of each set, gets the children objects,
   *  creates a type_ProjectVault that is identical to the set.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args input parameters, none expected.
   * @return <code>int</code> return 0 if no exceptions.
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  public int convertMyWorkspaceToWorkspaceVault(Context context, String[] args)
      throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    // if object need migration, the schema will be temporarily
    // altered.  variable stores status.
    boolean schemaAltered = false;

    // job should be run by user creator, get name.
    String adminUser = context.getUser();

    //*** get a list of all central users in the system.
    PersonList personList  = Person.getPersons(context, true);
    PersonItr personItr = new PersonItr(personList);

    MxDebug.message (MxDebug.DL_3,
                       "queryResultList.size() = " + personList.size () );

    MxDebug.message (MxDebug.DL_7,
                     "convertMyWorkspaceToWorkspaceVault  .. 1");

    try
    {
      if ( personList.size () > 0 )
      {
        MxDebug.message (MxDebug.DL_7,
                         "convertMyWorkspaceToWorkspaceVault  .. 2");

        preModifySchema ( context );

        schemaAltered = true;
        MxDebug.message (MxDebug.DL_7,
                         "convertMyWorkspaceToWorkspaceVault  .. 3");
      }
      else
      {
        return 0;
      }

      //*** get a MapList of TYPE_PROJECTVAULT that have the name
      // 'My  Workspace'.  Used to not prevent creating a new one for the user.
      // Set can't easily be deleted.
      SelectList busSelects = new SelectList (2);
      busSelects.add (DomainObject.SELECT_ID);
      busSelects.add (DomainObject.SELECT_OWNER);
      String whereExpression = "(name == 'My Workspace')";
      whereExpression += " && ( revision ~= auto_* )";

      MapList queryResultList = DomainObject.findObjects (
                          context,
                          TYPE_PROJECTVAULT,
                          "*",
                          whereExpression,
                          busSelects );

      MxDebug.message (MxDebug.DL_3,
                       "queryResultList.size() = " + queryResultList.size () );

      // loop through list and check if user has a 'My Workspace'
      // set.  if they do,
      // 1. copy set to a temporary set
      // 2. make type_ProjectFolder for the user
      // 3. add the 'My Workspace' children to the new
      //    type_ProjectFolder.
      // 4. delete temporary set.
      while (personItr.next())
      {

        // get person's user name.
        String userName = personItr.obj().getName();
        MxDebug.message (MxDebug.DL_5,
                         "userName = " + userName);

        // check if user already has a TYPE_PROJECTVAULT from a previous
        // run of the method.
        Iterator queryResultIter = queryResultList.iterator();

        boolean migrationAlreadyRun =  false;

        while ( queryResultIter.hasNext() )
        {

          Map attrMap = (Map) queryResultIter.next();

          String ownerName = (String) attrMap.get(DomainObject.SELECT_OWNER);
          if (userName.equals ( ownerName ) )
          {
            migrationAlreadyRun  = true;
            break;
          }
        }

        // skip user.
        if ( migrationAlreadyRun )
        {
          MxDebug.message (MxDebug.DL_5,
                         "migrationAlreadyRun  " + migrationAlreadyRun + "");
          continue;
        }


        try
        {

          startTransaction ( context, true );

          MxDebug.message (MxDebug.DL_7,
                         "convertMyWorkspaceToWorkspaceVault  .. 4");

          //*** try to create a new set and set owner as adminUser.
          String mqlError       = "";
          String [] methodArgs  = {"My Workspace","tempSet", userName, adminUser};
          String mqlString      = "copy set $1 $2 fromuser  $3 touser $4";
          MqlUtil.mqlCommand(context, mqlString, methodArgs);

          MxDebug.message (MxDebug.DL_7,
                         "convertMyWorkspaceToWorkspaceVault  .. 5");

          //*** if a set was created for user, create a type_ProjectFolder and
          // add the same children to the type_ProjectFolder.  Delete
          // the tempSet.
          if ( mqlError.equals ("") )
          {
            //
            matrix.db.Set tempSet = new matrix.db.Set ( "tempSet" );
            tempSet.open ( context );

            //*** get a list of business objects in the set.
            SelectList objectSelects = new SelectList();
            objectSelects.add(DomainObject.SELECT_ID);
            objectSelects.add(DomainObject.SELECT_TYPE);
            objectSelects.add(DomainObject.SELECT_NAME);

            BusinessObjectWithSelectList boList =
                                    tempSet.select( context, objectSelects );

            tempSet.remove ( context );
            tempSet.close ( context );

            MxDebug.message (MxDebug.DL_7,
                             "convertMyWorkspaceToWorkspaceVault  .. 6");

            //*** create new object
            DomainObject domObject = new DomainObject();
            BusinessObject newBo = new BusinessObject (
                   TYPE_PROJECTVAULT,
                   "My Workspace",
                   domObject.getUniqueName (),
                   VAULT_ESERVICEPRODUCTION );

            newBo.create ( context, POLICY_PROJECTVAULT );

            String newObjectId = newBo.getObjectId();

            MxDebug.message (MxDebug.DL_7,
                           "convertMyWorkspaceToWorkspaceVault  .. 7");

            //*** create bean instance and set other attribute info
            WorkspaceVault workspaceVault =
                (WorkspaceVault)DomainObject.newInstance(context, newObjectId);

            workspaceVault.open(context);
            workspaceVault.setOwner( userName );

            MxDebug.message (MxDebug.DL_7,
                             "convertMyWorkspaceToWorkspaceVault  .. 8");


            // propagate the child relations to the new object.
            adjustWorkspaceRelationship ( context,
                                          workspaceVault,
                                          boList );

            workspaceVault.update ( context );
            workspaceVault.close ( context );

            MxDebug.message (MxDebug.DL_7,
                             "convertMyWorkspaceToWorkspaceVault  .. 9");

          }

          commitTransaction(context);

        }
        catch (Exception e)
        {
            abortTransaction(context);
            MxDebug.exception (e, true);
            throw (new FrameworkException( e.toString() ) );
        }
      } // end of while

    }
    catch (Exception e)
    {
      MxDebug.exception (e, true);
      throw (new FrameworkException( e.toString() ) );
    }

    finally
    {

      // if schema was changed, set back.
      if (schemaAltered)
      {
        postModifySchema (context  );
      }
    }

    MxDebug.exit ();

    return 0;
  }

  /**
   *  Creates a type_VersionDocument for each file of a type_DocumentSheet that
   *  doesn't have one.  Inspect all type_DocumentSheet, create
   *  type_VersionDocument as needed.  For each file that a
   *  type_VersionDocument that can't be created display message about the
   *  file.  Transaction around the creation of the each type_VersionDocument.
   *
   *  Data migration for V10.  This is an addition beyond the original
   *  purpose of the method.  Method invocation was added to avoid changing
   *  the tcl file.  The method updateVersionDocuments sets the title attribute
   *  to contain the value of the file name that is checked into the object.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args input parameters, none expected.
   * @return <code>int</code> return 0 if no exceptions.
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  public int createVersionDocuments (Context context, String[] args)
      throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    // V10 data migration.  the file name value needs to be written in to the
    // title attribute of the object.
    //updateVersionDocuments ( context );
    updateVersionDocumentsWithTitle(context, null);

    boolean versionDocumentExists = false;

    // store a string of ids of Document Sheet object(s) that VersionDocuments
    // couldn't be create
    String dsObjectErrors = "";

    //*** get a MapList of type_DocumentSheet objects in database.
    SelectList busSelects = new SelectList (1);
    busSelects.add (DomainObject.SELECT_ID);

    MapList queryResultList = DomainObject.findObjects( context,
                        TYPE_DOCUMENTSHEET,
                        "*",
                        null,
                        busSelects );

    MxDebug.message (MxDebug.DL_3,
                     "queryResultList.size() = " + queryResultList.size () );

    //*** loop through objects,  for each type_DocumentSheet, check that it's
    // file(s) have a type_VersionDocument create for each file.  if the
    // the type_VersionDocument is missing create it.
    Iterator queryResultItr = queryResultList.iterator();
    while(queryResultItr.hasNext())
    {
      Map map = (Map)queryResultItr.next();

      String docSheetId = (String) map.get(DomainObject.SELECT_ID);

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocuments 1 ...");

      try
      {
        startTransaction(context,true);

        MxDebug.message (MxDebug.DL_7,
                         "createVersionDocuments 2 ...");

        DomainObject docSheet = new DomainObject ( docSheetId );
        docSheet.open ( context );

        // get the file(s) checked into the type_DocumentSheet.
        FileList docSheetFileList = docSheet.getFiles( context );
        FileItr docSheetFileItr =  new FileItr ( docSheetFileList );

        MxDebug.message (MxDebug.DL_7,
                           "createVersionDocuments 3 ...");

        // get type_VersionDocument object(s) connected to the type_DocumentSheet.
        SelectList childSelectList = new SelectList();
        childSelectList.addId();
        childSelectList.addName();
        childSelectList.addType();

        MapList versionDocumentsList =
             docSheet.getRelatedObjects( context,
                                         RELATIONSHIP_VERSION,
                                         "*",
                                         childSelectList,
                                         new StringList(),
                                         false,
                                         true,
                                         (short)1,
                                         "",
                                         "" );

        //*** for each file checked into the type_DocumentSheet,
        // check if there is a tpye_VersionDocument.  if there
        // isn't one, then call method to create one.
        while ( docSheetFileItr.next () )
        {

          // reset flag
          versionDocumentExists = false;

          MxDebug.message (MxDebug.DL_7,
                           "createVersionDocuments 4 ...");

          File checkinFile = (File) docSheetFileItr.obj();
          String fileName = checkinFile.getName();

          MxDebug.message (MxDebug.DL_7,
                           "createVersionDocuments 5 ...");

          // loop through type_VersionDocuments.  determine if a
          // type_VersionDocuments exists for the file, check Title attribute.
          // the title attribute should have the file name, V10 requirement.
          Iterator versionDocumentsItr = versionDocumentsList.iterator();
          while ( versionDocumentsItr.hasNext() )
          {
            Map objMap = (Map) versionDocumentsItr.next();

            String versionDocId = (String) objMap.get(SELECT_ID);

            DomainObject versionDoc = new DomainObject ( versionDocId );
            versionDoc.open (context);

            String versionDocTitle = versionDoc.getAttributeValue(context, PropertyUtil.getSchemaProperty ( context, "attribute_Title" ));

            versionDoc.close (context);

            MxDebug.message (MxDebug.DL_7,
                           "fileName / versionDocTitle = " + fileName + "/"+ versionDocTitle );

            if ( fileName.equals ( versionDocTitle ) )
            {
              versionDocumentExists = true;
              break;
            }
          }

          MxDebug.message (MxDebug.DL_7,
                           "versionDocumentExists = " + versionDocumentExists);

          // type_VersionDocument not found, create type_VersionDocument
          if ( versionDocumentExists == false)
          {

            int results = createVersionDocumentObject ( context, docSheet, fileName );

            if ( results == 1) // there was an error
            {

              MxDebug.message (MxDebug.DL_5,
                               "create VD results =" + results);

              // add any ids of that the VersionDocument operation failed.
              dsObjectErrors += "  " + docSheetId + " file: ";
              dsObjectErrors += fileName + "";
            }
          }
        }

        docSheet.close ( context );
        commitTransaction(context);

      }
      catch (Exception e)
      {
          abortTransaction(context);
          MxDebug.exception (e, true);
          throw (new FrameworkException( e.toString() +  docSheetId) );
      }
    } // end of outside while

    MxDebug.message (MxDebug.DL_3,
                     "dsObjectErrors =" + dsObjectErrors);

    //*** post a message that some type_VersionDocument couldn't be created.
    if (! dsObjectErrors.equals("") )
    {
      String warningMessage = "\n\nError: These Document Sheet object(s) ";
      warningMessage += "failed during the creation of Version Documents\n";
      warningMessage += dsObjectErrors;
      warningMessage += "The Document Sheet object probably has some  ";
      warningMessage += "kind of data corruption. Missing files, etc. ";
      warningMessage += "Repair objects or remove from database. ";

      throw new Exception(warningMessage);
    }

    MxDebug.exit ();

    return 0;
  }


  /**
   *  Creates a type_VersionDocument for a the parent object(type_DocumentSheet)
   *  connects the two objects.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param docSheet the type_DocumentSheet, should be open
   * @return <code>int</code> return 0 if no exceptions.
   * @throws Exception if the operation fails
   * @since AEF 10.0.0.0
   */
  protected int createVersionDocumentObject ( Context context,
                                              DomainObject docSheet,
                                              String fileName )
      throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 06/24/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    try
    {

      String vdName = docSheet.getUniqueName("VD_");
      boolean docSheetClose = docSheet.openObject(context);
      String objectVault = docSheet.getVault();

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 1 ...");

      //DomainObject versionDocumentDo = new DomainObject();

      //versionDocumentDo.
      createAndConnect( context,
                        TYPE_VERSIONDOCUMENT,
                        vdName,
                        "1",
                        POLICY_VERSIONDOCUMENT,
                        objectVault,
                        RELATIONSHIP_VERSION,
                        docSheet,
                        true );

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 2 ...");

      boolean closeBO = openObject(context);

      String vdObjectId = getObjectId();
      String owner = docSheet.getOwner().getName();

      String originated = getInfo ( context,DomainConstants.SELECT_ORIGINATED);

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 3 ...");

      //*** set metadata on type_VersionDocument
      setOwner (owner);

      Map attributes = new HashMap(3);
      attributes.put( ATTRIBUTE_FILEVERSION, "1" );
      attributes.put( ATTRIBUTE_VERSIONDATE, originated );
      attributes.put( ATTRIBUTE_CHECKINREASON, "Generated during installation" );
      attributes.put( ATTRIBUTE_TITLE, fileName );

      setAttributeValues(context, attributes);

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 4 ...");
      closeObject(context, closeBO);

      docSheet.closeObject(context, docSheetClose);

      MxDebug.message (MxDebug.DL_7,
                        "createVersionDocumentObject 5 ...");
    }
    catch (Exception e)
    {
        MxDebug.message (MxDebug.DL_1,
                         "Error = " + "'" + e + "'");

        return 1;
    }

    MxDebug.exit ();

    return 0;
  }


  /**
   * The V10 common file functionality requires that the name of the file
   * checked into the object be stored in the Title attribute.  This
   * method queries for all Version Documents in the system and sets the
   * attribute.
   *
   * @param context the eMatrix <code>Context</code> object
   * @return <code>int</code> return 0 if no exceptions.
   * @throws Exception if the operation fails
   * @since AEF 10.0.0.0
  */
  public int updateVersionDocuments ( Context context )
      throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 06/23/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    //*** get a MapList of all type_VersionDocument objects in database.
    SelectList busSelects = new SelectList (2);
    busSelects.add (DomainConstants.SELECT_ID);

    MapList queryResultList = DomainObject.findObjects( context,
                        TYPE_VERSIONDOCUMENT,
                        "*",
                        null,
                        busSelects );

    MxDebug.message (MxDebug.DL_3,
                     "queryResultList.size() = " + queryResultList.size () );

    //*** loop through objects,  for each type_VersionDocument
    // get the name of the file checked in and write the value to
    // object's Title attribute.
    Iterator queryResultItr = queryResultList.iterator();
    while(queryResultItr.hasNext())
    {
      Map map = (Map)queryResultItr.next();

      MxDebug.message (MxDebug.DL_7, "map = " + map );

      String objectId = (String) map.get(DomainObject.SELECT_ID);

      MxDebug.message (MxDebug.DL_7,
                       "updateVersionDocuments 1 ...");

      try
      {
        startTransaction(context,true);

        MxDebug.message (MxDebug.DL_7,
                         "updateVersionDocuments 2 ...");


        DomainObject domainObject = new DomainObject ( objectId );

        domainObject.open ( context );
        FileList versionDocFileList = domainObject.getFiles( context );
        FileItr versionDocFileItr =  new FileItr ( versionDocFileList );


        MxDebug.message (MxDebug.DL_7,
                          "versionDocFileList.size () = " + versionDocFileList.size ());

        // the type_VersionDocument should only have one file.
        if ( versionDocFileList.size() == 1 )
        {

          MxDebug.message (MxDebug.DL_7,
                           "updateVersionDocuments 4 ...");

          File checkinFile = (File) versionDocFileItr.obj();
          MxDebug.message (MxDebug.DL_7,
                           "updateVersionDocuments 4a ..."+ checkinFile);
          String fileName = checkinFile.getName();

          MxDebug.message (MxDebug.DL_7,
                           "fileName ..= '" + fileName + "'");

          domainObject.setAttributeValue(  context, ATTRIBUTE_TITLE, fileName );
          domainObject.update ( context );

          MxDebug.message (MxDebug.DL_7,
                           "updateVersionDocuments 4 ...");

        }

        MxDebug.message (MxDebug.DL_7,
                          "updateVersionDocuments 6 ...");
        domainObject.close ( context );

        commitTransaction(context);

      }
      catch (Exception e)
      {
          e.printStackTrace();
          abortTransaction(context);
          MxDebug.exception (e, true);
          throw (new FrameworkException( e.toString() +  objectId) );
      }
    } // end of while

    MxDebug.exit ();

    return 0;
  }


  /**
   *  Delete adminstration objects that are obsolete in this release.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args input parameters, none expected.
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  public void deleteOldSystemObjects (Context context, String[] args)
      throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    SelectList busSelects = new SelectList (1);
    busSelects.add (DomainObject.SELECT_ID);

    String searchTypes = "vailPDF Rendering Queue" + "," +
                         "vailSystemSettings";

    // query for vailDisplay Rules objects.
    MapList queryResultList = DomainObject.findObjects( context,
                                searchTypes,
                                "*",
                                null,
                                busSelects );

    MxDebug.message (MxDebug.DL_3,
                     "queryResultList.size() = " + queryResultList.size () );

    // Delete results from query.
    try
    {
      startTransaction ( context, true );

      DomainObject.deleteObjects( context, getStringArrayOfIds
                                  ( context, queryResultList ) );

      commitTransaction(context);

    }
    catch (Exception e)
    {
        abortTransaction(context);
        MxDebug.exception (e, true);
        throw (new FrameworkException( e.toString() ) );
    }

    MxDebug.exit ();
  }

  /**
   *  Temporarily modify the schema to allow the migration to run.
   *
   * @param context the eMatrix <code>Context</code> object
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  protected void preModifySchema ( Context context )
    throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
     */

    //*** modify schema to facilitate the migration.
    String[] methodArgs = {RELATIONSHIP_VAULTEDDOCUMENTREV2, TYPE_PERSONALFOLDER, TYPE_PUBLICFOLDER};
    String mqlString    = "modify relation $1 from clone replicate to clone replicate add type $2 add type $3";
    MqlUtil.mqlCommand(context, mqlString, methodArgs);

    methodArgs  = new String[]{RELATIONSHIP_SUBVAULTS,TYPE_PERSONALFOLDER, TYPE_PUBLICFOLDER, TYPE_PERSONALFOLDER, TYPE_PUBLICFOLDER};
    mqlString   = "modify relation $1 from add type $2 add type $3 to add type $4 add type $5";
    MqlUtil.mqlCommand(context, mqlString, methodArgs);
  }


  /**
   *  Remove temporary modifications to the schema.
   *
   * @param context the eMatrix <code>Context</code> object
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  protected void postModifySchema ( Context context )
    throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
    */

    String mqlString    = "modify relation $1 from clone none to clone none remove type $2 remove type $3";
    MqlUtil.mqlCommand(context, mqlString, RELATIONSHIP_VAULTEDDOCUMENTREV2,TYPE_PERSONALFOLDER, TYPE_PUBLICFOLDER);
    // check for an error

    mqlString   = "modify relation $1 to clone none";
    MqlUtil.mqlCommand(context, mqlString, RELATIONSHIP_CONTAINS);

    mqlString   = "modify relation $1 from remove type $2 remove type $3 to remove type $4 remove type $5";
    MqlUtil.mqlCommand(context, mqlString, RELATIONSHIP_SUBVAULTS, TYPE_PERSONALFOLDER, TYPE_PUBLICFOLDER,TYPE_PERSONALFOLDER, TYPE_PUBLICFOLDER);
  }

  /**
   *  Create new relationship object(s) connecting the children to the
   *  new object.  If the relationship can't be created a message is written
   *  to the log file.
   *
   *  If type_PersonalFolder or type_PublicFolder are sub-typed, then
   *  the method needs modified to include the sub-types properly.
   *
   *  WorkspaceVault object must be open.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param workspaceVault the business object that needs adjusted


   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  protected void adjustWorkspaceRelationship (Context context,
                                      WorkspaceVault workspaceVault,
                                      BusinessObjectWithSelectList busSelList)
    throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    String objectId = workspaceVault.getObjectId();

    RelationshipType relTypeFolder = new RelationshipType(
      RELATIONSHIP_SUBVAULTS);

    RelationshipType relTypeOther = new RelationshipType(
      RELATIONSHIP_VAULTEDDOCUMENTREV2);

    BusinessObjectWithSelectItr itr =
      new BusinessObjectWithSelectItr ( busSelList );

    //*** loop through business objects.  try to connect the objects to
    // the type_ProjectVault.  Use the appropriate relationship type give
    // child object type.
    while ( itr.next () )
    {
      BusinessObject bo = itr.obj ();

      bo.open(context);

      String toTypeNameBo = bo.getTypeName();

      try
      {
        if ( toTypeNameBo.equals ( TYPE_PERSONALFOLDER ) ||
             toTypeNameBo.equals ( TYPE_PUBLICFOLDER )   ||
             toTypeNameBo.equals ( TYPE_PROJECTVAULT ) )
        {
          workspaceVault.connect( context, relTypeFolder, true, bo);
        }
        else
        {
          workspaceVault.connect( context, relTypeOther, true, bo);
        }
      }
      catch (Exception e)
      {

        MxDebug.message (MxDebug.DL_5,
                    "Warning/Error.... = " + objectId + " '" + e + "' " +
                    "add to object " + toTypeNameBo + " " + bo.getName() );
      }

      bo.close(context);

    }

    MxDebug.exit ();
  }

  /**
   *  Preserves subscription relationships.  Connect the existing folder to the
   *  type_PublishSubscribe object.
   *
   *  WorkspaceVault object must be open.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param doExistingObject existing business object that may have
   *                         subscriptions.
   * @param workspaceVault the business object that needs adjusted
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  protected void preserveSubscriptions (Context context,
                                        DomainObject doExistingObject,
                                        WorkspaceVault workspaceVault )
    throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    String objectId = workspaceVault.getObjectId();

    RelationshipType relPublishSubscribe = new RelationshipType(
      RELATIONSHIP_PUBLISHSUBSCRIBE);

    // get all the relationships on the from side of object.
    RelationshipList relList = doExistingObject.getFromRelationship( context );
    RelationshipItr itr = new RelationshipItr ( relList );

    MxDebug.message (MxDebug.DL_3,
       "relList.size() = " + relList.size () );

    //*** loop through relationships, for all relationship_Contains and
    // relationship_PublishSubscribe, delete the relationship and create
    // a new one with the appropriate relationship type.
    while ( itr.next () )
    {
      Relationship rel = itr.obj ();

      MxDebug.message (MxDebug.DL_5,
                    "rel.getTypeName () = " + rel.getTypeName ());

      if ( rel.getTypeName ().equals (
                  RELATIONSHIP_PUBLISHSUBSCRIBE ) )
      {
        rel.open (context);
        rel.remove(context);
        rel.update (context);
        rel.close (context);

        BusinessObject bo = itr.obj().getTo();
        bo.open(context);

        try
        {
          MxDebug.message (MxDebug.DL_5,
                 "Creating rel between " +
                 "\tworkspaceVault.getName () = " + workspaceVault.getName () + "\n" +
                 "\tbo.getName () = " + bo.getName () + "\n" +
                 "\trel name = " + relPublishSubscribe );

          workspaceVault.connect( context, relPublishSubscribe, true, bo);
        }
        catch (Exception e)
        {
          MxDebug.message (MxDebug.DL_5,
                    "ERROR:  maintaining subscriptions.... = " + objectId +
                    " - " +  e );
        }

        bo.close(context);
      }

    }

    MxDebug.exit ();
  }


  /**
   *  Create a String [] of object ids that are derived from MapList object.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param mapList a MapList business object. id must be a key in object map.
   * @return <code>String []</code>
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   */
  protected String [] getStringArrayOfIds (Context context, MapList mapList)
    throws Exception
  {
    /*
     *  Author     : Mike Terry
     *  Date       : 02/12/03
     *  Notes      :
     *  History    :
     *
    */

    MxDebug.enter ();

    java.util.List idList = new ArrayList();
    Iterator mapListItr = mapList.iterator();

    // build a List of the objectIds that are in the mapList.
    while ( mapListItr.hasNext() )
    {
      Map attrMap = (Map) mapListItr.next();

      // add id to List
      idList.add ( (String) attrMap.get("id"));
    }


    // convert list into String[]
    String[] ids = new String[idList.size()];

    Iterator itr = idList.iterator();
    int iCount = 0;

    while ( itr.hasNext() )
    {
       ids[iCount++] = (String)itr.next();
    }

    MxDebug.exit ();

    return ids;
  }

 /**
   *  Creates a type_VersionDocument for a file.  Copies the type_DocumentSheet
   *  object, changes the type, removes the non-pertinent files, connects the
   *  object to the type_DocumentSheet.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param objectId the type_DocumentSheet id
   * @param fileName the file name to versioned
   * @param fileObjects a Map of FileObj objects
   * @return <code>int</code> return 0 if no exceptions.
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   * @deprecated
   */
  /*protected int createVersionDocumentObject ( Context context,
                                              String objectId,
                                              String fileName,
                                              Map fileObjects)
      throws Exception
  {

     *  Author     : Mike Terry
     *  Date       : 02/26/03
     *  Notes      :
     *  History    :
     *


    MxDebug.enter ();

    try
    {

      MxDebug.message (MxDebug.DL_5,
                       "objectId = " + objectId + "\n" +
                       "fileName = " + fileName + "\n" +
                       "fileObjects = " + fileObjects );

      DomainObject docSheet = new DomainObject (objectId);
      docSheet.open(context);

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 1 ...");

      String vdName = docSheet.getUniqueName("VD_");
      String objectVault = docSheet.getVault();

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 2 ...");

      // clone the Document Sheet
      BusinessObject cloneBo = docSheet.clone(context,
                                              vdName,
                                              "1",
                                              objectVault );

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 3 ...");

      MxDebug.message (MxDebug.DL_5,
                 "New object details: \n" +
                 "TYPE_VERSIONDOCUMENT = " + TYPE_VERSIONDOCUMENT + "\n" +
                 "vdName = " + vdName + "\n" +
                 "Revision = 1 \n" +
                 "objectVault = " + objectVault + "\n" +
                 "POLICY_VERSIONDOCUMENT = " + POLICY_VERSIONDOCUMENT + "\n" );

      // change the new clone to type_VersionDocument
      BusinessObject versionDocumentBo = cloneBo.change(
                                                  context,
                                                  TYPE_VERSIONDOCUMENT,
                                                  vdName,
                                                  "1",
                                                  objectVault,
                                                  POLICY_VERSIONDOCUMENT );

      DomainObject versionDocumentDo = new DomainObject (versionDocumentBo);

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 4 ...");

      versionDocumentDo.open(context);

      String vdObjectId = versionDocumentDo.getObjectId();
      String owner = docSheet.getOwner().getName();

      String originated = versionDocumentDo.getInfo ( context,
                            DomainConstants.SELECT_ORIGINATED);

      //*** set metadata on type_VersionDocument
      versionDocumentDo.setOwner (owner);

      Map attributes = new HashMap(3);
      attributes.put( ATTRIBUTE_FILEVERSION, "1" );
      attributes.put( ATTRIBUTE_VERSIONDATE, originated );
      attributes.put( ATTRIBUTE_CHECKINREASON, "Generated during installation" );
      attributes.put( ATTRIBUTE_TITLE, fileName );


      versionDocumentDo.setAttributeValues(context, attributes);

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 5 ...");

      // connect to type_DocumentSheet to type_VersionDocument
      DomainRelationship.connect(context,
                                docSheet,
                                RELATIONSHIP_VERSION,
                                versionDocumentDo);

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 6 ...");

      // remove other file(s) from the Version Document
      Iterator keyItr = fileObjects.keySet().iterator();
      while (keyItr.hasNext())
      {

        String keyName = (String) keyItr.next();

        // pull the FileObj object out of the Map, get name
        FileObj fileObj = (FileObj) fileObjects.get(keyName);
        String thisFileName = fileObj.getFilename();
        String thisFileFormat = fileObj.getFormat();

        // if not the desired attached file, delete from object.
        if (!thisFileName.equals(fileName))
        {
          // Create the mql command instance, delete the file,
          // and close the mql command instance
          String [] methodArgs  = {vdObjectId, thisFileName};
          String mqlString      = "delete businessobject $1 file \'$2\'";
          MqlUtil.mqlCommand(context, mqlString, methodArgs);

        }
      }

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 7 ...");

      versionDocumentDo.update(context);
      versionDocumentDo.close(context);

      docSheet.close(context);

      MxDebug.message (MxDebug.DL_7,
                       "createVersionDocumentObject 8 ...");
    }
    catch (Exception e)
    {
        MxDebug.message (MxDebug.DL_1,
                         "Error = " + "'" + e + "'");

        return 1;
    }


    MxDebug.exit ();

    return 0;
  }
*/
  /**
   *  Update a VersionDocument with title.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args Java <code>String[]</code> object
   * @return <code>int</code> return 0 if no exceptions.
   * @throws Exception if the operation fails
   * @since AEF 9.5.6.0
   * @deprecated
   */

  public int updateVersionDocumentsWithTitle(Context context, String[] args) throws Exception
  {
      try
      {
    MxDebug.enter ();
          StringList objectSelects = new StringList(2);
          objectSelects.add(DomainConstants.SELECT_ID);
          objectSelects.add(DomainConstants.SELECT_FILE_NAME);
          String whereExpression = "";
          MapList ml = DomainObject.findObjects(context, TYPE_VERSIONDOCUMENT, "*", whereExpression, objectSelects);
    MxDebug.message (MxDebug.DL_3, "Search Types = " + TYPE_VERSIONDOCUMENT + "\n" + "ml.size() = " + ml );

          Iterator itr = ml.iterator();
          DomainObject version = newInstance(context, TYPE_VERSIONDOCUMENT);
          while( itr.hasNext() )
          {
              Map m = (Map)itr.next();
              String id = (String) m.get(DomainConstants.SELECT_ID);

              String title = "";
              try {
                title = (String) m.get(DomainConstants.SELECT_FILE_NAME);
              } catch (ClassCastException cex ) {
                StringList titles = (StringList) m.get(DomainConstants.SELECT_FILE_NAME);
                if ( titles.size() > 0 )
                {
                    title = (String)titles.get(0);
                }

              }

    MxDebug.message (MxDebug.DL_3,
                     "id = " + id + "\n" +
                     "title = " + title );

              version.setId(id);
              if ( title != null && !"".equals(title) )
              {
                version.setAttributeValue(context, ATTRIBUTE_TITLE, title);
              }
          }
      } catch (Exception ex ) {
        return 1;
      }
    MxDebug.exit ();
      return 0;
  }


}
