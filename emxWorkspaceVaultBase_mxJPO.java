/**
 * emxWorkspaceVault.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes. All Rights
 * Reserved. This program contains proprietary and trade secret information of
 * MatrixOne,Inc.
 * Copyright notice is precautionary only and does not evidence
 * any actual or intended publication of such program.
 *
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessItr;
import matrix.db.AccessList;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.Group;
import matrix.db.GroupItr;
import matrix.db.GroupList;
import matrix.db.JPO;
import matrix.db.PersonItr;
import matrix.db.RelationshipType;
import matrix.db.Role;
import matrix.db.RoleItr;
import matrix.db.RoleList;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.SubscriptionManager;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.document.DocumentCentralConstants;
import com.matrixone.apps.document.EventConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;

import com.matrixone.apps.domain.*;
/**
 * The <code>emxWorkspaceVaultBase</code> class represents the "Workspace Vault" type.
 *
 * @exclude
 */
public class emxWorkspaceVaultBase_mxJPO  extends DomainObject
                              implements DocumentCentralConstants
{

      /** Workspace Access User name. */
      static final String AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME
                                               = "Workspace Access Grantor";
    /**
     * Creates the ${CLASSNAME} Object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxWorkspaceVaultBase_mxJPO ( Context context, String [] args ) throws Exception
    {
        super();
        if(args != null && args.length > 0)
            setId(args[0]);
    }

    /**
     * Creates a new emxWorkspaceVaultBase object given the objectId.
     *
     * @param id objectId
     * @throws Exception if the operation fails
     */
    public emxWorkspaceVaultBase_mxJPO ( String id ) throws Exception
    {
        super ( id );
    }


    /**
     * Clones the Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *          0 - objectId  the object Id
     *          1 - paramList the map contains parameters for Cloning
     * @return String objectId of the clone Workspace vault
     * @throws Exception if the operation fails
     */
    public static String cloneObject ( Context context, String [] args )
                               throws Exception
    {
        if ( ( args == null ) || ( args.length < 1 ) )
        {
            throw ( new IllegalArgumentException() );
        }

        Map map = ( Map ) JPO.unpackArgs ( args );

        String objectId = cloneObject ( context,
                                        ( String ) map.get ( "objectId" ),
                                        ( Map ) map.get ( "paramList" ) );

        return objectId;
    }

    /**
     * Creates the Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *          0 - objectId  the object Id
     *          1 - paramList the map contains parameters for creating
     * @return String objectId of the new Workspace vault
     * @throws Exception if the operation fails
     */
    public static String createObject ( Context context, String [] args )
                                throws Exception
    {
        if ( ( args == null ) || ( args.length < 1 ) )
        {
            throw ( new IllegalArgumentException() );
        }


        String objectId  = null;
        Map    map       = ( Map ) JPO.unpackArgs ( args );
        Map    paramList = ( Map ) map.get ( "paramList" );
        objectId         = createObject ( context, paramList );

        return objectId; //newly created object id
    }

    /**
     * Revise Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *          0 - objectId  the object Id
     *          1 - paramList the map with the attribute values for the revisied Work Space Object
     *          1 - paramList the map contains parameters for revising
     * @throws Exception if the operation fails
     */
    public static String reviseObject ( Context context, String [] args )
                                throws Exception
    {
        if ( ( args == null ) || ( args.length < 1 ) )
        {
            throw ( new IllegalArgumentException() );
        }

        Map    map      = ( Map ) JPO.unpackArgs ( args );
        String objectId = reviseObject ( context,
                                         ( String ) map.get ( "objectId" ),
                                         ( Map ) map.get ( "paramList" ) );

        return objectId;
    }

    /**
     * Clones Object. Should be called thro' public cloneObject method
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId Id of object to be cloned
     * @param map contains parameters for Cloning
     * @return String objectId of the clone Workspace vault
     * @throws Exception if the operation fails
     * @exclude
     */
    protected static String cloneObject (Context context,
                                         String  objectId,
                                         Map     map)
       throws Exception
    {

       String name         = (String) map.get (JPO_ARGS_NAME);
       String type         = (String) map.get (JPO_ARGS_TYPE);


       Map    attrMap      = (Map)    map.get (JPO_ARGS_ATTR_MAP);
       String description  = (String) attrMap.get (JPO_ALIAS_DESCRIPTION);
       String owner        = (String) attrMap.get (JPO_ALIAS_OWNER);
       String globalRead   = (String) attrMap.get ("attribute_GlobalRead");

       Map    specMap      = (Map)    map.get (JPO_ARGS_SPEC_MAP);
       String folderIds    = (String) specMap.get (JPO_SPEC_FOLDER_ID);

       // variable to hold Attributes to be Set

       HashMap attributeList = new HashMap();

       attributeList.put
          (PropertyUtil.getSchemaProperty (context,"attribute_GlobalRead" ),
           globalRead );

        // instantiate DomainObject class to get data about
        // original business object id

        DomainObject domainObject = new DomainObject( objectId );

        // New Object that will be created

        DomainObject newBo        = null;

        // variable to hold objectid of cloned object

        String newObjId = "";

        try
        {
            ContextUtil.startTransaction ( context, true );

            domainObject.open ( context );


            newBo = new DomainObject( domainObject.clone (
                                                   context,
                                                   name,
                                                   domainObject.getRevision (),
                                                   domainObject.getVault () ) );

            newBo.change ( context, type, name, domainObject.getRevision (),
                           domainObject.getVault (),
                           domainObject.getPolicy ().toString () );

            newBo.open ( context );


            // set owner

            if (owner != null && owner.length () > 0)  {
               newBo.setOwner (owner);
            }

            // set description

            if (description != null)   {
               newBo.setDescription ( description );
            }

            // set attributes

            newBo.setAttributeValues (context, attributeList);
            newBo.update (context);


            // Add to Folder

            if (folderIds != null && folderIds.length () > 0)
            {
               StringTokenizer st  = new StringTokenizer( folderIds,";" );
               String [] folderIdsArray = new String[ st.countTokens () ];

               for ( int i = 0; i < folderIdsArray.length; i++ )  {
                  folderIdsArray[ i ] = st.nextToken ();
               }

               DomainRelationship.connect (context, newBo,
                                           PropertyUtil.getSchemaProperty (context,
                                              "relationship_SubVaults" ),
                                           true, //from dir?
                                           folderIdsArray // from obj Ids
                  );
            }

            newObjId = newBo.getObjectId ();

            String roleEmployee
               = PropertyUtil.getSchemaProperty(context,"role_Employee");

            if (globalRead.equalsIgnoreCase("true"))
            {
                Map accessMap = new HashMap();

                accessMap.put(roleEmployee, AccessUtil.READ);

                addGrantees(context, newObjId, accessMap);
            }
            else
            {
                removeGrantees(context,
                               newObjId,
                               new String [] { roleEmployee });
            }

            ContextUtil.commitTransaction ( context );
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction ( context );
            throw new FrameworkException( e );
        }
        finally
        {
            if ( ( newBo != null ) && newBo.isOpen () )
            {
                domainObject.close ( context );
            }

            if ( ( domainObject != null ) && domainObject.isOpen () )
            {
                domainObject.close ( context );
            }
        }

        return newObjId;
    }

    /**
     * Creates Object. Should be called thro' public createObject method
     *
     * @param context the eMatrix <code>Context</code> object
     * @param map contains parameters for Creating
     * @return String objectId of the new Workspace Vault
     * @throws Exception if the operation fails
     * @exclude
     */
    protected static String createObject ( Context context, Map map )
                                   throws Exception
    {
        String         name           = ( String ) map.get (
                                                            "attribute_Name" );
        String         type           = ( String ) map.get (
                                                            "attribute_Type" );
        String         owner          = ( String ) map.get (
                                                           "attribute_Owner" );
        String         description    = ( String ) map.get (
                                                     "attribute_Description" );
        String         globalRead     = ( String ) map.get (
                                         "attribute_GlobalRead" );
        String         folderIds      = ( String ) map.get (
                                                          "attribute_Folder" );
        String []      contentIds     = ( String [] ) map.get (
                                                      "attribute_ContentIds" );
        String []      grantees       = ( String [] ) map.get (
                                                        "attribute_AccessId" );
        String []      accessType     = ( String [] ) map.get (
                                                          "attribute_Access" );

        WorkspaceVault workspaceVault = new WorkspaceVault();
        //String         policy         = workspaceVault.getDefaultPolicy (
        //                                        context, type );

        String policy = POLICY_WORKSPACE_VAULT;

        BusinessObject newBo          = null;
        String         newBoId        = null;

        try
        {
            DomainObject domObject = new DomainObject();
            newBo = new BusinessObject( type, name, domObject.getUniqueName (),
                                        context.getVault ().toString () );

            ContextUtil.startTransaction ( context, true );


            newBo.create ( context, policy );
            newBo.open ( context );

            // owner

            newBo.setOwner ( owner );

            // description

            if ( ( description != null ) && ( description.length () > 0 ) )
            {
                newBo.setDescription ( description );
            }


            // set attributes
            //
            AttributeList attribList = new AttributeList();
            Map           accessMap  = new HashMap();

            if ( ( globalRead != null ) &&
                    ( globalRead.length () > 0 ) )
            {
                if(globalRead.equalsIgnoreCase("true"))
                {
                    accessMap.put(
                            PropertyUtil.getSchemaProperty(context,"role_Employee"),
                            AccessUtil.READ );

                }

                String symbolicAttrName =
                        PropertyUtil.getSchemaProperty (context,
                                "attribute_GlobalRead" );

                if ( ( symbolicAttrName != null ) &&
                     ( symbolicAttrName.length () > 0 ) )
                {
                    attribList.addElement ( new Attribute(
                                                    new AttributeType(
                                                            symbolicAttrName ),
                                                    globalRead ) );
                }
            }

            newBo.setAttributeValues ( context, attribList );

            newBo.update ( context );

            domObject = new DomainObject( newBo );

            newBoId = newBo.getObjectId ();


            // add to folder
            //
            if ( ( folderIds != null ) && ( folderIds.length () > 0 ) )
            {
                StringTokenizer st             = new StringTokenizer( folderIds,
                                                                      ";" );
                String []       folderIdsArray =
                                                new String[ st.countTokens () ];

                for ( int i = 0; i < folderIdsArray.length; i++ )
                {
                    folderIdsArray[ i ] = st.nextToken ();

                }

                DomainRelationship.connect ( context, domObject,
                                             PropertyUtil.getSchemaProperty (context,
                                                     "relationship_SubVaults" ),
                                                                    //rel type
                                             false, //to dir?
                                             folderIdsArray // to obj Ids
                                              );

                // subscription fired for content added
                //
                for ( int i = 0; i < folderIdsArray.length; i++ )
                {
                    sendEventIconMail(context, EventConstants.EVENT_CONTENT_ADDED,
                            folderIdsArray[ i ], newBoId);
                }
            }


            workspaceVault.setId(newBoId);

            String user = context.getUser();

            //Set the access list for the folder
            AccessUtil accessUtil = new AccessUtil();
            AccessList accessList = new AccessList();
            BusinessObjectList boList = new BusinessObjectList();
            boList.add(new BusinessObject(newBoId));

            Access access = accessUtil.getAddRemoveAccess();
            if(owner!=null)
            {
              access.setUser(owner);
              accessList.add(access);
            }

            if ((owner != null) && (!owner.equalsIgnoreCase(user)))
            {

              access = accessUtil.getReadAccess();
              access.setUser(user);

              accessList.add(access);

            }

            ContextUtil.pushContext (
            context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME, null, null);

            DomainObject.grantAccessRights (
              context, boList, accessList);

            ContextUtil.popContext (context);

            // Add Content
            //
            if ( ( contentIds != null ) && ( contentIds.length > 0 ) )
            {
                workspaceVault.setContentRelationshipType (
                        PropertyUtil.getSchemaProperty (context,
                                "relationship_VaultedDocumentsRev2" ) );
                workspaceVault.addItems ( context, contentIds );
            }


            // grant access
            //
            if ( ( grantees != null ) && ( accessType != null ) &&
                 ( grantees.length == accessType.length ) )
            {
                for ( int i = 0; i < grantees.length; i++ )
                {
                    accessMap.put ( grantees[ i ], accessType[ i ] );
                }

            }

            if ( accessMap.size () > 0 )
            {
                addGrantees ( context, newBoId, accessMap );
            }


            ContextUtil.commitTransaction ( context );
        }
        catch ( Exception e )
        {
            ContextUtil.abortTransaction ( context );
            throw ( new FrameworkException( e ) );
        }
        finally
        {
            if ( newBo != null )
            {
                newBo.close ( context );
            }
        }

          return newBoId;
    }

    /**
     * Revise Object. Should be called thro' public reviseObject method
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId Id of object to be revised
     * @param map contains parameters for revising
     * @return String objectId of the new revision of the Workspace Vault
     * @throws Exception if the operation fails
     * @exclude
     */
    protected static String reviseObject ( Context context, String objectId,
                                           Map map )
                                   throws Exception
    {
        String owner       = ( String ) map.get ( "attribute_Owner" );
        String revision    = ( String ) map.get ( "attribute_Revision" );
        String description = ( String ) map.get ( "attribute_Description" );
        String folderIds   = ( String ) map.get ( "attribute_Folder" );
        String globalRead  =
                ( String ) map.get ( "attribute_GlobalRead" );

        HashMap attributesMap = new HashMap();
        attributesMap.put (
                PropertyUtil.getSchemaProperty (context,
                    "attribute_GlobalRead" ),
                    globalRead );

        // New Object

        DomainObject newBo         = null;

        // New Object Id

        String       newObjId      = null;

        try
        {
            ContextUtil.startTransaction ( context, true );

            newBo = new DomainObject( new DomainObject( objectId ).revise (
                                             context, revision,
                                             context.getVault ().getName () ) );
            newBo.open ( context );

            if ( ( owner != null ) && ( owner.length () > 0 ) )
            {
                newBo.setOwner ( owner );
            }

            if ( ( description != null ) && ( description.length () > 0 ) )
            {
                newBo.setDescription ( description );
            }

            // setting attribute values

            newBo.setAttributeValues ( context, attributesMap );
            newBo.update ( context );

            // Add to Folder

            if ( ( folderIds != null ) && ( folderIds.length () > 0 ) )
            {
                StringTokenizer st             = new StringTokenizer( folderIds,
                                                                      ";" );
                String []       folderIdsArray =
                                                new String[ st.countTokens () ];

                for ( int i = 0; i < folderIdsArray.length; i++ )
                {
                    folderIdsArray[ i ] = st.nextToken ();
                }

                DomainRelationship.connect (
                        context,
                        newBo,
                        PropertyUtil.getSchemaProperty (context,
                                        "relationship_SubVaults" //rel type
                                              ),
                        true, //from dir?
                        folderIdsArray // from obj Ids
                      );
            }

            newObjId = newBo.getObjectId ();

            String roleEmployee = PropertyUtil.getSchemaProperty(context,"role_Employee");

            if(globalRead.equalsIgnoreCase("true"))
            {
                Map accessMap = new HashMap();

                accessMap.put(roleEmployee, AccessUtil.READ);

                addGrantees(context, newObjId, accessMap);
            }
            else
            {
                removeGrantees(context, newObjId, new String [] { roleEmployee });
            }

            ContextUtil.commitTransaction ( context );
        }
        catch ( Exception e )
        {
            ContextUtil.abortTransaction ( context );

            throw ( new FrameworkException( e ) );
        }
        finally
        {
            if ( ( newBo != null ) && newBo.isOpen () )
            {
                newBo.close ( context );
            }
        }

        return newObjId;
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     * @exclude
     */
    public int mxMain ( Context context, String [] args )
                throws Exception
    {
        if ( true )
        {
            throw new Exception(
                    "Must specify method on emxWorkspaceVaultBase Invocation" );
        }

        return 0;
    }

    /**
     * Gets the access grantred to the Workspace Vault
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *      0 - objectId
     * @return MapList with the access details
     * @throws Exception if the operation fails
     */
    public static MapList getObjectAccess (Context context, String[] args)
            throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
            throw (new IllegalArgumentException ());
        }

        MapList mapList =
                getObjectAccess (context, (String) JPO.unpackArgs (args));

        return mapList;
    }

    /**
     * Adds specific access to the Workspace Vault
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *      0 - objectId the object ID
     *      1 - memberids and access details Map
     * @return int 0
     * @throws Exception if the operation fails
     */
    public static int addGrantees (Context context, String[] args)
            throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
            throw (new IllegalArgumentException ());
        }

        Map map = (Map) JPO.unpackArgs (args);

        addGrantees (
                context, (String) map.get ("objectId"),
                (Map) map.get ("memberIdsAndAccess")
        );

        return 0;
    }

    /**
     * Removes access granted earlier
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following argument
     *      0 - objectId the object ID
     *      1 - memberIds mebers whose acces has to be revoked
     * @return int 0
     * @throws Exception if the operation fails
     */
    public static int removeGrantees (Context context, String[] args)
            throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
            throw (new IllegalArgumentException ());
        }

        Map map = (Map) JPO.unpackArgs (args);

        removeGrantees (
                context, (String) map.get ("objectId"),
                (String[]) map.get ("memberIds")
        );

        return 0;
    }

    /**
     * Gets Access for the Objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the Java <code>String</code>
     * @return MapList the menber access details
     * @throws Exception if the operation fails
     */
    static protected MapList getObjectAccess (
            Context context, String objectId
            ) throws Exception
    {
        AccessUtil accessUtil = new AccessUtil();
        MapList Maplist = new MapList();

        HashMap hshMap = new HashMap();

        hshMap.put (DomainObject.SELECT_ID, objectId);
        Maplist.add (hshMap);

        BusinessObject boObject = null;
        MapList mList = new MapList();
        String PersonName = null;

        // Get The List Of Persons
        PersonItr personItr =
                new PersonItr(matrix.db.Person.getPersons (context));
        StringList personNameList = new StringList();
        StringList personList = new StringList();

        while (personItr.next ())
        {
            personList.addElement ((personItr.obj ()).toString ());
        }

        // Get The List Of Roles
        RoleItr roleItr = new RoleItr(matrix.db.Role.getRoles (context));
        StringList roleList = new StringList();

        while (roleItr.next ())
        {
            roleList.addElement ((roleItr.obj ()).toString ());
        }

        // Get The List Of Groups
        GroupItr groupItr = new GroupItr(matrix.db.Group.getGroups (context));
        StringList groupList = new StringList();

        while (groupItr.next ())
        {
            groupList.addElement ((groupItr.obj ()).toString ());
        }

        HashMap hashMap = new HashMap();

        boObject = new BusinessObject(objectId);
        boObject.open (context);

        //Access List for the Object
        AccessItr boAccItr = new AccessItr(boObject.getAccessAll (context));

        while (boAccItr.next ())
        {
            Access acc = boAccItr.obj ();
            String sUserAccess = acc.getUser ();
            String access = accessUtil.checkAccess (acc);
            String objType = null;

            if (personList.contains (sUserAccess))
            {
                // Check if the User is a Person
                objType = "Person";

               // Construct the Person List to get Details
               if (!(personNameList.contains (sUserAccess)))
               {
                   if (PersonName == null)
                   {
                       PersonName = sUserAccess;
                       personNameList.addElement (sUserAccess);
                   }
                   else
                   {
                       PersonName = PersonName + "," + sUserAccess;
                       personNameList.addElement (sUserAccess);
                    }
                }
            }
            else if (roleList.contains (sUserAccess))
            {
                // Check if the User is a Role
                objType = "Role";
            }
            else if (groupList.contains (sUserAccess))
            {
                // Check if the User is a Group
                objType = "Group";
            }

            Map accessMap = new HashMap();

            accessMap.put ("User Name", sUserAccess);
            accessMap.put ("Type", objType);
            accessMap.put ("AccessList", access);
            hashMap.put (sUserAccess, accessMap);
        }

        boObject.close (context);

        // construct the selects for person alone to get the details
        if (PersonName != null)
        {
            StringList objectSelects = new StringList();

            objectSelects.addElement (
                    com.matrixone.apps.common.Person.SELECT_ID
            );
            objectSelects.addElement (
                    com.matrixone.apps.common.Person.SELECT_NAME
            );
            objectSelects.addElement (
                    com.matrixone.apps.common.Person.SELECT_FIRST_NAME
            );
            objectSelects.addElement (
                    com.matrixone.apps.common.Person.SELECT_LAST_NAME
            );
            objectSelects.addElement (
                    com.matrixone.apps.common.Person.SELECT_COMPANY_NAME
            );

            // execute and get the details
            MapList personMapList =
                    DomainObject.findObjects (
                            context, PropertyUtil.getSchemaProperty (context,"type_Person"),
                            PersonName, "*", "*", "*", null, false, objectSelects
                    );
            Iterator personDetailsItr = personMapList.iterator ();

            // iterate through the maplist and compare the details with
            // the earlier access map and update the maplist, remove the
            // entry from the hashmap if found
            while (personDetailsItr.hasNext ())
            {
                String Name = null;
                HashMap personHash = (HashMap) personDetailsItr.next ();
                String personName =
                        (String) personHash.get (
                                com.matrixone.apps.common.Person.SELECT_NAME
                        );
                String personFirstName =
                        (String) personHash.get (
                                com.matrixone.apps.common.Person.SELECT_FIRST_NAME
                        );
                String personLastName =
                        (String) personHash.get (
                                com.matrixone.apps.common.Person.SELECT_LAST_NAME
                        );

                /*
                  Logic for Displaying the User Id of the person if both
                  the First & Last names are null or empty.Also display
                  only the Last Name if the First name is null or empty
                  and the vice-versa.
                */

                if(personFirstName == null || personFirstName.equals("null") ||
                                                 (personFirstName.trim().equals("")))
                {
                    personFirstName = "";
                }

                if(personLastName.equals(null) || personLastName.equals("null")
                                              || (personLastName.trim().equals("")))
                {
                    personLastName = "";
                }

                if((personFirstName.trim().equals("")) &&
                                                (personLastName.trim().equals("")))
                {
                    Name = personName;
                }
                else if((personFirstName.trim().equals("")) &&
                                             ! (personLastName.trim().equals("")))
                {
                    Name = personLastName;
                }
                else if(!(personFirstName.trim().equals("")) &&
                                                (personLastName.trim().equals("")))
                {
                    Name = personFirstName;
                }
                else
                {
                    Name = personLastName + "," + personFirstName;
                }

                HashMap PersonDetails = (HashMap) hashMap.get (personName);

                PersonDetails.put (
                        DomainConstants.SELECT_NAME, Name);
                PersonDetails.put (
                        "PERSON_ID",
                        personHash.get (
                                com.matrixone.apps.common.Person.SELECT_ID));
                PersonDetails.put (
                        DomainConstants.SELECT_ID,
                        personName);
                PersonDetails.put (
                        "Organization",
                        personHash.get (
                                com.matrixone.apps.common.Person.SELECT_COMPANY_NAME));
                hashMap.remove (personName);
                mList.add (PersonDetails);
            }
        }

        // Add the remaining Roles And Groups to the List
        java.util.Set set = hashMap.keySet ();
        Iterator itr = set.iterator ();

        while (itr.hasNext ())
        {
            String Name = (String) itr.next ();
            HashMap allMap = (HashMap) hashMap.get (Name);

            allMap.put (
                    DomainConstants.SELECT_NAME,
                    Name);
            allMap.put (
                    DomainConstants.SELECT_ID, Name);
            allMap.put ("Organization", "-");
            mList.add (allMap);
        }

        return mList;
    }

    /**
     * Adds grantees to a business object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the Java <code>String</code> object
     * @param memberIdsAndAccess the Java <code>Map</code>Object
     * @return int 0
     * @throws Exception if the operation fails
     */
    static protected int addGrantees (
            Context context, String objectId, Map memberIdsAndAccess
            ) throws Exception
    {
        try
        {
            AccessUtil accessUtil = new AccessUtil();

            // A Business Object List to store the Business Objects For Granting

            BusinessObjectList busList = new BusinessObjectList();

            busList.addElement (new BusinessObject(objectId));

            DomainObject domFolder = new DomainObject(objectId);
            StringList objSelects = new StringList();
            objSelects.addElement(domFolder.SELECT_ID);
            MapList contentList = domFolder.getRelatedObjects(context,
                                                              PropertyUtil.getSchemaProperty (context,"relationship_VaultedDocumentsRev2" ),
                                                              "*",
                                                              objSelects,
                                                              null,
                                                              false,
                                                              true,
                                                              (short)1,"","");

            String sContentId = "";
            Iterator mapItr = contentList.iterator();
            while(mapItr.hasNext())
            {
                Map contentMap = (Map)mapItr.next();
                sContentId = (String)contentMap.get(DomainObject.SELECT_ID);
                busList.addElement (new BusinessObject(sContentId));
            }

            String grantor = AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME;

            ContextUtil.startTransaction (context, true);

            // Build an Access List to have the Collection of Access
            // to be applied on the object

            AccessList accessList = new AccessList();

            // Get the Member Names for Whom the Access Has To Granted

            java.util.Set key = memberIdsAndAccess.keySet ();
            Iterator itr = key.iterator ();

            while (itr.hasNext ())
            {
                String memberName = (String) itr.next ();

                // Get The Access To Be Granted

                String methodAccess =
                        (String) memberIdsAndAccess.get (memberName);
                Access access = new Access();

                // Based On The Access Get The Access Object

                if (methodAccess.trim ().equals ("Read"))
                {
                    access = accessUtil.getReadAccess ();
                }
                else if (methodAccess.trim ().equals ("Read Write"))
                {
                    access = accessUtil.getReadWriteAccess ();
                }
                else if (methodAccess.trim ().equals ("Add"))
                {
                    access = accessUtil.getAddAccess ();
                }
                else if (methodAccess.trim ().equals ("Remove"))
                {
                    access = accessUtil.getRemoveAccess ();
                }
                else if (methodAccess.trim ().equals ("Add Remove"))
                {
                    access = accessUtil.getAddRemoveAccess ();
                }

                // Set the Grantor For The Access

                access.setGrantor (grantor);

                // Set the Grantee For The Access

                access.setUser (memberName);

                // Add the Access to the Access List

                accessList.addAccess (access);
            }

            // Push the Context

            ContextUtil.pushContext (
                    context, grantor, null, null
            );

            // Grant the Access to the List of Objects for the
            // List Of People.

            DomainObject.grantAccessRights (context, busList, accessList);
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction (context);
            throw new FrameworkException(e);
        }
        finally
        {
            ContextUtil.popContext (context);
            ContextUtil.commitTransaction (context);
        }

        return 0;
    }

    /**
     * Used to remove grantees from a business object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the Java <code>String</code> object
     * @param args memberIds
     * @return int 0
     * @throws Exception if the operation fails
     */
    static protected int removeGrantees (
            Context context, String objectId, String[] args
            ) throws Exception
    {
        try
        {

            // Prepare The Bus Obj List

            BusinessObjectList busList = new BusinessObjectList();

            // Add The Bus Obj To The List
            busList.addElement (new BusinessObject(objectId));
            // Start The Transaction

            ContextUtil.startTransaction (context, true);

            // Pass The Grantor

            String grantor = AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME;

            // Push The Context

            ContextUtil.pushContext (
                    context, grantor, null, null
            );

            AccessList accessList = new AccessList();

            for (int i = 0; i < args.length; i++)
            {
                // The Member Names Passed To The Object

                String memberName = args[i];

                // Create The New Access Object
                Access access = new Access();

                // Set The Grantor

                access.setGrantor (grantor);

                // Set The Grantee

                access.setUser (memberName);

                // Add To The Access List

                accessList.addAccess (access);
            }

            // Revoke The Access For The Members On The Bus Obj List

            DomainObject.revokeAccessRights (context, busList, accessList);
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction (context);
            throw new FrameworkException(e);
        }
        finally
        {
            ContextUtil.popContext (context);
            ContextUtil.commitTransaction (context);
        }

        return 0;
    }


    /**
     * Modifies the properties and attributes of the object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *      0 - objectID of the the Workspace Vault
     *      1 - paramList Map with modify details of the Workspace Vault
     * @return String the modify result
     * @throws Exception if the operation fails
     */
    public static String modifyObject (Context context, String[] args)
            throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
            throw (new IllegalArgumentException ());
        }

        Map map = (Map) JPO.unpackArgs (args);
        String result =
                modifyObject (
                        context, (String) map.get ("objectId"),
                        (Map) map.get ("paramList")
                );

        return result;
    }


    /**
     * Modifies the properties and attributes of the object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the Java <code>String<code> object
     * @param map the Map with the modify details
     * @return String the modify Result
     * @throws Exception if the operation fails
     */
    protected static String modifyObject (
            Context context, String objectId, Map map
            ) throws Exception
    {

        // Get The Basic Attributes Of The Object From The Map

        String name = (String) map.get ("attribute_Name");

        String type = (String) map.get ("attribute_Type");

        String policy = (String) map.get ("attribute_Policy");

        String owner = (String) map.get ("attribute_Owner");

        String description = (String) map.get ("attribute_Description");

        String globalRead = (String) map.get ("attribute_GlobalRead");

        String ownerBeforeObjectModify = null;

        BusinessObject bo = null;

        String result = "success";

        try
        {
            bo = new BusinessObject(objectId);
            bo.open(context);
            ownerBeforeObjectModify =  bo.getOwner().toString();

            ContextUtil.startTransaction (context, true);
            // modify The  Object

            bo.change(context,type,name,bo.getRevision(),bo.getVault(),policy);

            bo.setDescription(description);
            String globalChange = new DomainObject(bo).getInfo(context,
                                    "attribute["+
                                        PropertyUtil.getSchemaProperty(context,"attribute_GlobalRead")+
                                     "]");

            if (globalRead != null && globalRead.length() > 0 &&
                                 !(globalRead.equalsIgnoreCase(globalChange)))
            {
                // set attributes
                AttributeList attribList = new AttributeList(1);

                attribList.addElement (
                    new Attribute (
                            new AttributeType(PropertyUtil.getSchemaProperty (context,
                                    "attribute_GlobalRead" )),
                            globalRead
                    ));
                bo.setAttributeValues(context, attribList);
                // stores user (person/role/group) : accessToBeGranted
                //
                Map accessMap = new HashMap (2);

                String newObjId = bo.getObjectId ();


                String roleEmployee = PropertyUtil.getSchemaProperty(context,"role_Employee");

                // if globalRead true, grant read access to role employee
                //
                if(globalRead.equalsIgnoreCase("true"))
                {
                    accessMap.put(roleEmployee, AccessUtil.READ);
                }
                else // else remove access to employee
                {
                    removeGrantees(context, newObjId, new String [] { roleEmployee });
                }

                String user = context.getUser();

                if( accessMap != null && accessMap.size() > 0 )
                {
                    addGrantees(context, newObjId, accessMap);
                }

                    result = "success";
            }
            // Call update method to update attributes of the business object


            // Send Icon Mails to Subsribed Users

            sendEventIconMail(context, EventConstants.EVENT_FOLDER_MODIFIED,
                objectId, objectId);
            DomainObject domObj = DomainObject.newInstance(context , objectId);

            //To Send notification for Content Modified event.

            if(domObj != null && domObj.getType(context).equals(TYPE_WORKSPACE_VAULT))
            {
              String parentVaultId = domObj.getInfo(context ,  "to[" + RELATIONSHIP_SUB_VAULTS + "].from.id");
              if(parentVaultId != null && !"".equals(parentVaultId))
              {
                sendEventIconMail(context, EventConstants.EVENT_CONTENT_MODIFIED,
                parentVaultId, parentVaultId);
              }
            }

            if (owner != null && owner.length() > 0 )
            {
                bo.setOwner(owner);
            }
            bo.update(context);

            //Giving Add/Remove Access to new owner and Read Access to old owner
            Map accessMap = new HashMap (2);
            String user = context.getUser();

            accessMap.put(owner,AccessUtil.ADD_REMOVE);

            if ((owner != null) && (ownerBeforeObjectModify != null) && (!ownerBeforeObjectModify.equalsIgnoreCase(owner)))
            {
                accessMap.put(ownerBeforeObjectModify, AccessUtil.READ);
            }

            if( accessMap != null && accessMap.size() > 0 )
            {
                addGrantees(context, bo.getObjectId(), accessMap);
            }

            bo.close (context);

            ContextUtil.commitTransaction (context);
        }
        catch (Exception e)
        {
            result = "failure";

            ContextUtil.abortTransaction (context);
            throw e;
        }
        finally
        {
            if (bo != null)
            {
                bo.close (context);
            }
        return result;
        }


    }


   /**
     * Adds Contents Objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *  0 - Array containing ParentIds
     *  1 - Array containing childIds
     * @return String of containg names of objects not added
     * @throws FrameworkException if operation fails
     */
    public  static String addToFolders(Context context, String[] args)
    throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
          throw (new IllegalArgumentException());
        }

        //Unpaking the Arguments

        Map map = (Map) JPO.unpackArgs(args);

        //Getting the List from Unpaked Map

        String[] childIds   = (String[])map.get("childIds");
        String relationship = (String)map.get("relationship");
        String[] parentIds  = (String[])map.get("parentIds");

        //Decalaring Temp variables

        String objectNotAdded="";
        String strMqlCmd =null;
        String strQueryResult=null;
        DomainObject tempobj;

        //Constructing the String array of Childs Ids for which actual
        //coonect Access is there. and then preparing the objects not added
        //List



        StringList ChildIdsWithAccessList =new StringList();

        for(int i=0;i<childIds.length;i++)
        {

            strMqlCmd           = "print bus $1 select $2 dump $3";

            strQueryResult      = MqlUtil.mqlCommand(context,strMqlCmd, childIds[i], "current.access[toconnect]", "|");

            if(strQueryResult.equalsIgnoreCase("True"))
            {
                ChildIdsWithAccessList.addElement(childIds[i]);

            }
            else
            {
                tempobj=new DomainObject(childIds[i]);

                String sName = tempobj.getInfo(context,
                                                DomainObject.SELECT_NAME);
                if(objectNotAdded.length() > 0)
                {
                    objectNotAdded += ","+sName;
                }
                else
                {
                    objectNotAdded=sName;
                }
            }
        }


        if(ChildIdsWithAccessList.size()>0)
        {
            //Connecting using releveant relationship

            for(int i=0;i<parentIds.length;i++)
            {
                try
                {
                    WorkspaceVault workspaceVaultObj =
                                            new WorkspaceVault(parentIds[i]);

                    String childrenIdsSelect = "from["+relationship+"].to.id";
                    StringList existingChildren = workspaceVaultObj.getInfoList(context, childrenIdsSelect);
                    //remove items from the list to be added if they are already present in the folder
                    Vector children = new Vector(ChildIdsWithAccessList);
                    children.removeAll(existingChildren);

                    if (children.size() > 0)
                    {
                        workspaceVaultObj.setContentRelationshipType(relationship);
                        workspaceVaultObj.addItems(context,(String [])children.toArray(new String[] {}));

                        Iterator childItr = children.iterator();
                        while(childItr.hasNext())
                        {
                            //Post addtion Procedure-ie subscription

                            sendEventIconMail(context,LibraryCentralConstants.EVENT_CONTENT_ADDED,
                                                    parentIds[i],(String)childItr.next());

                        }
                    }
                }
                catch(Exception e)
                {
                    throw new FrameworkException(e);
                }

            }
        }



            //--Return list that containg names of objects Not
            //--added

            return objectNotAdded;
  }

    /**
     * Sends event Icon mail
     * @param context the eMatrix <code>Context</code> object
     * @param event the event to subscribe
     * @param objId the objectId
     * @param objAtt used to embed in the mail
     */
    protected static void sendEventIconMail (
            Context context, String event, String objId, String objAtt
            ) throws Exception
    {
        WorkspaceVault dcVault = new WorkspaceVault();
        dcVault.setId(objId);
        dcVault.open(context);

        SubscriptionManager subMgr = dcVault.getSubscriptionManager();

        subMgr.publishEvent(context, event, objAtt);

        dcVault.close(context);
    }
    /**
     * Removes Contents from the Workspace Vault
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     * @returns String of Objects Not removed
     * @throws Exception if the operation fails
     */

    public String removeToObjects(Context context, String[] args)
            throws Exception
    {
        if ((args == null) || (args.length < 1))
        {
            throw (new IllegalArgumentException());
        }


        String objectsNotRemoved="";

        //Unpaking the Arguments
        Map map = (Map) JPO.unpackArgs(args);

        //Getting the List from Unpaked Map

        String[] childIds   = (String[])map.get("childIds");
        String relationship = "";
        String parentId     = (String)map.get("objectId");

        DomainObject parentObj = new DomainObject(parentId);

        //Temprory variables for childs

        DomainObject tempChildObj;
        String sChildName="";

        for(int i=0;i<childIds.length;i++)
        {
            boolean removed =false;

            boolean folderRemoved = false;

            tempChildObj =new DomainObject(childIds[i]);

            String strType =
                    tempChildObj.getInfo(context,DomainObject.SELECT_TYPE);

            String strWorkSpaceVault =
                 PropertyUtil.getSchemaProperty(context,"type_ProjectVault");

            if (strType.equalsIgnoreCase(strWorkSpaceVault))
            {
                relationship =
                        PropertyUtil.getSchemaProperty(context,
                                "relationship_SubVaults"
                        );
                folderRemoved = true;
            }
            else
            {
                relationship =
                        PropertyUtil.getSchemaProperty(context,
                                "relationship_VaultedDocumentsRev2"
                        );
            }

            //Disconnect only if Access is there
            RelationshipType relType = new RelationshipType(relationship);

            if(checkAccess(context,parentId))
            {
                ContextUtil.pushContext(context);
                parentObj.disconnect(context,relType,true,tempChildObj);
                ContextUtil.popContext (context);
                removed=true;
            }
            else
            {

                sChildName = tempChildObj.getInfo(context,
                                                  DomainObject.SELECT_NAME);

                objectsNotRemoved+=sChildName+"";
            }

            if(removed)
            {
//When a Sub Folder or Any other Type of Event is removed from Content of a Folder  Content Removed Event will be Published And Folder Removed

                //if(folderRemoved)
                //{
                    sendEventIconMail(context,
                                    LibraryCentralConstants.EVENT_CONTENT_REMOVED,
                                    parentId ,childIds[i]);
                //    sendEventIconMail(context,
                //                     EventConstants.EVENT_FOLDER_REMOVED,
                //                    parentId ,childIds[i]);
                //}
                //else
                //{
                //    sendEventIconMail(context,
                //                  EventConstants.EVENT_CONTENT_REMOVED,
                //                  parentId ,childIds[i]);
                //}
            }
        }

        //--Return Objects Not Removed

        return objectsNotRemoved;

    }

    /**
     * Checks todisconnect Access for the Objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param childId objectId
     * @return boolean true if object has todisconnect Access
     * @throws Exception if the operation fails
     */
   protected static boolean checkAccess(Context context,String childId)
     throws Exception
   {
        boolean access = false;

        String strMqlCmd    = "print bus $1 select $2 dump $3";

        String strQueryResult = MqlUtil.mqlCommand(context,strMqlCmd, childId, "current.access[todisconnect]", "|");

        if(strQueryResult.equalsIgnoreCase("True"))
        {
            access=true;
        }

        return access;

    }

   /**
    * This method checks if the User Has Grant Access rights
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectId       - object Id
    * @return boolean - true if  user has Grant access
    *                 - false if user does not have Grant access
    * @throws Exception if a major failure occurs
    */
   public boolean hasGrantAccess (Context context,String [] args)
   throws Exception {
       Map programMap              = (Map)JPO.unpackArgs(args);
       String objectId             = (String)programMap.get(LibraryCentralConstants.OBJECT_ID);
       DomainObject doObj          = new DomainObject(objectId);
       Access access               = doObj.getAccessMask(context);
       return access.hasGrantAccess();
   }

   /**
    * This method checks if the User Has Revoke Access rights
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectId       - object Id
    * @return boolean - true if  user has Revoke access
    *                 - false if user does not have Revoke access
    * @throws Exception if a major failure occurs
    */
   public boolean hasRevokeAccess (Context context,String [] args)
   throws Exception {
       Map programMap              = (Map)JPO.unpackArgs(args);
       String objectId             = (String)programMap.get(LibraryCentralConstants.OBJECT_ID);
       DomainObject doObj          = new DomainObject(objectId);
       Access access               = doObj.getAccessMask(context);
       return access.hasRevokeAccess();
   }

   /**
    * This method checks if the User Has Revoke and Grant Access rights
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectList     - existing access list
    *    requestMap     - should have object id
    * @return  true (for all the rows) - if user has grant and revoke access
    *          false (for all the rows) - if user does not have grant and revoke access
    * @throws Exception if a major failure occurs
    */
   public StringList hasGrantAndRevokeAccess (Context context,String [] args)
   throws Exception {
       Map programMap                   = (Map)JPO.unpackArgs(args);
       MapList objList                  = (MapList)programMap.get("objectList");
       Map requestMap                   = (Map) programMap.get("requestMap");
       String objectId                  = (String)(requestMap.get(LibraryCentralConstants.OBJECT_ID));
       StringList memberAccessList      =  new StringList(objList.size());
       boolean hasEditAccess            =  new Boolean(true);
       DomainObject doObj               = new DomainObject(objectId);
       Access access                    = doObj.getAccessMask(context);
       hasEditAccess                    = (access.hasGrantAccess() &&
							               access.hasRevokeAccess())?
							               new Boolean(true):new Boolean(false);


       for(int i = 0; i < objList.size(); i++) {
           String level = (String)((Map)objList.get(i)).get("level");
           if("0".equalsIgnoreCase(level)){
               memberAccessList.add(new Boolean(false));
           } else {
               memberAccessList.add(hasEditAccess);
           }
       }
       return (memberAccessList);
   }

   /**
    * This method filters the Access Granted for the Object for the Persons, Roles or Groups
    *
    * @param context the eMatrix <code>Context</code> object
    * @param objectId - object Id of folder
    * @param filterValue - Person/Role/Group
    * @return MapList of Access Details of all Members
    * @throws Exception if a major failure occurs
    */
   public MapList filterFolderAccessDetails(Context context,String objectId,String filterValue)
   throws Exception {
       MapList accessList       = getObjectAccess(context,objectId);
       MapList memberList       = new MapList();
       Iterator memberListItr   = accessList.iterator();
       while (memberListItr.hasNext()) {
           Map map              = (Map) memberListItr.next();
           String usrType       = (String)map.get("Type");
           if(!UIUtil.isNullOrEmpty(usrType)&& (usrType.equalsIgnoreCase(filterValue))) {
               memberList.add(map);
           }
       }
       return memberList;
   }

   /**
    * This method returns existing access (Role/Group/Person) grantees
    * based on given filter value
    *
    * @param context the eMatrix <code>Context</code> object
    * @param objectId  - object Id of folder
    * @param filterValue - Role/Group/Person
    * @return StringList of grantees names
    * @throws Exception if a major failure occurs
    */
   public StringList excludeExistingAccessGrantees(Context context,String objectId,String filterValue)
   throws Exception {
       MapList accessList       = filterFolderAccessDetails(context,objectId,filterValue);
       StringList memberList    = new StringList();
       Iterator memberListItr   = accessList.iterator();
       while (memberListItr.hasNext()) {
           Map map = (Map) memberListItr.next();
           if ("Person".equals(filterValue)) {
               memberList.add(map.get("PERSON_ID"));
           } else {
               memberList.add(map.get("name"));
           }
       }
       return memberList;
   }

   /**
    * This method returns list of person object ids who have the access to the
    * context folder
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectId       - object Id
    * @return StringList of person object Ids
    * @throws Exception if a major failure occurs
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList excludeExistingPersonAccess(Context context,String [] args)
   throws Exception {
       Map programMap  = (Map)JPO.unpackArgs(args);
       String objectId = (String)programMap.get(LibraryCentralConstants.OBJECT_ID);
       return(excludeExistingAccessGrantees(context,objectId,"Person"));
   }

   /**
    * This method gets the Access Details of All members (Person/Role/Group)
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectId       - object Id
    * @return Map List of all members Access Details
    * @throws Exception if a major failure occurs
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getFolderAllAccessDetails (Context context , String[] args )
   throws Exception {
       Map programMap    = (Map)JPO.unpackArgs(args);
       String objectId   = (String)programMap.get(LibraryCentralConstants.OBJECT_ID);
       return getObjectAccess(context,objectId);
   }

   /**
    * This method gets Access Details of Person members
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectId       - object Id
    * @return Map List of only Person members Access Details
    * @throws Exception if a major failure occurs
    */
   public MapList getFolderPersonAccessDetails ( Context context , String[] args )
   throws Exception {
       Map programMap   = (Map)JPO.unpackArgs(args);
       String objectId  = (String)programMap.get(LibraryCentralConstants.OBJECT_ID);
       return filterFolderAccessDetails(context,objectId,"Person");
   }

   /**
    * This method gets the Access Details of Role members
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectId       - object Id
    * @return Map List of only Role members Access Details
    * @throws Exception if a major failure occurs
    */
   public MapList getFolderRoleAccessDetails ( Context context , String[] args )
   throws Exception {
       Map programMap   = (Map)JPO.unpackArgs(args);
       String objectId  = (String)programMap.get(LibraryCentralConstants.OBJECT_ID);
       return filterFolderAccessDetails(context,objectId,"Role");
   }


   /**
    * This method gets the Access Details of Group members
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectId       - object Id
    * @return Map List of only Group members Access Details
    * @throws Exception if a major failure occurs
    */
   public MapList getFolderGroupAccessDetails ( Context context , String[] args )
   throws Exception {
       Map programMap   = (Map)JPO.unpackArgs(args);
       String objectId  = (String)programMap.get(LibraryCentralConstants.OBJECT_ID);
       return filterFolderAccessDetails(context,objectId,"Group");
   }


   /**
    * This method gets the Member (Role/Group/Person) Name
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectList        - List of All members Details
    * @return Vector with All the member Names
    * @throws Exception if a major failure occurs
    */
   public Vector getAccessMemberName (Context context, String[] args)
   throws Exception {
       HashMap programMap  = (HashMap)JPO.unpackArgs(args);
       HashMap paramList   = (HashMap)programMap.get("paramList");
       String languageStr  = (String)paramList.get("languageStr");
       MapList objList     = (MapList)programMap.get("objectList");
       Vector columnVals   = new Vector(objList.size());
       Iterator itr        = objList.iterator();
       while (itr.hasNext()) {
           HashMap accessMap    = (HashMap)itr.next();
           String name          = (String)(accessMap).get(SELECT_NAME);
           String actualName    = (String)(accessMap).get("direction");
           if(actualName == null){
        	   actualName = name;
           }
                      
           String type          = (String)(accessMap).get("Type");
           String displayName   = actualName;
           type                 = UIUtil.isNullOrEmpty(type)?"":type;
           if (("Role".equalsIgnoreCase(type)) || ("Group".equalsIgnoreCase(type))) {
               displayName      = i18nNow.getAdminI18NString(type,actualName,languageStr);
           }
           columnVals.addElement(displayName);
       }
       return columnVals;
   }

     /**
    * This method gets the  Member Types
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectList    - List of All members Details
    *    paramList     - should have languageStr
    * @return Vector with All the member Types (Person/Role/Group)
    * @throws Exception if a major failure occurs
    */
   public Vector getAccessMemberType(Context context, String[] args)
   throws Exception
   {
       HashMap programMap  = (HashMap)JPO.unpackArgs(args);
       MapList objList     = (MapList)programMap.get("objectList");
       HashMap paramList   = (HashMap)programMap.get("paramList");
       String languageStr  = (String)paramList.get("languageStr");
       Vector columnVals   = new Vector(objList.size());
       Iterator itr        = objList.iterator();
       while (itr.hasNext()) {
           String type     = (String) ((Map)itr.next()).get("Type");
           type            = UIUtil.isNullOrEmpty(type)?"":type;
           if ("Person".equalsIgnoreCase(type)) {
               columnVals.addElement(i18nNow.getTypeI18NString(type,languageStr));
           } else if (("Role".equalsIgnoreCase(type)) || ("Group".equalsIgnoreCase(type))) {
               columnVals.addElement(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common." + type));
           } else {
               columnVals.addElement("");
           }
       }
       return columnVals;
   }


   /**
    * This method gets the  Member Type Icons
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectList    - List of All members Details
    * @return MapList with All the member Names and the Type Icons
    * @throws Exception if a major failure occurs
    */
   public MapList getAccessMemberTypeIcon(Context context, String[] args)
   throws Exception {
       HashMap programMap    = (HashMap)JPO.unpackArgs(args);
       MapList iconMapList   = new MapList();
       MapList objList       = (MapList)programMap.get("objectList");
       Vector columnVals     = new Vector(objList.size());
       Iterator itr          = objList.iterator();
       while (itr.hasNext()) {
           HashMap iconMap      = new HashMap();
           HashMap memberMap    = (HashMap)itr.next();
           String type          = (String) memberMap.get("Type");
           String name          = (String) memberMap.get(SELECT_ID);
           String actualName    = (String) memberMap.get("direction");
           if(actualName == null){
        	   actualName = name;
           }
           
           String level         = (String) memberMap.get("level");
           type                 = UIUtil.isNullOrEmpty(type)?"":type;
           boolean isPerson     = false;
           boolean isRole       = false;
           boolean isGroup      = false;

           if ("".equalsIgnoreCase(type)&& !("0".equalsIgnoreCase(level))) {
               String command       = "print user $1 select $2 $3 $4 dump $5";
               String cmd           = MqlUtil.mqlCommand(context, command, actualName, "isaperson", "isarole", "isagroup", "|");
                isPerson            = "TRUE|FALSE|FALSE".equalsIgnoreCase(cmd);
                isRole              = "FALSE|TRUE|FALSE".equalsIgnoreCase(cmd);
                isGroup             = "FALSE|FALSE|TRUE".equalsIgnoreCase(cmd);
           }
           if (isPerson || "Person".equalsIgnoreCase(type)){
               iconMap.put(name, "iconSmallPerson.gif");
           } else if (isRole || "Role".equalsIgnoreCase(type)){
               iconMap.put(name, "iconSmallRole.gif");
           } else if (isGroup || "Group".equalsIgnoreCase(type)){
               iconMap.put(name, "iconSmallGroup.gif");
           } else if("0".equalsIgnoreCase(level)){ // it is root node, folder
               iconMap.put(name, "iconSmallDocumentFolder.gif");
           }
           iconMapList.add(iconMap);
       }
       return iconMapList;
   }

   /**
    * This method gets the  Member Organization
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectList    - List of All members Details
    * @return Vector with All the member Organization
    * @throws Exception if a major failure occurs
    */
   public Vector getAccessMemberOrganization(Context context, String[] args)
   throws Exception {
       HashMap programMap   = (HashMap)JPO.unpackArgs(args);
       MapList objList      = (MapList)programMap.get("objectList");
       Vector columnVals    = new Vector(objList.size());
       Iterator itr         = objList.iterator();
       while (itr.hasNext()) {
    	   String Organisation = (String) ((Map)itr.next()).get("Organization");
    	   if (!UIUtil.isNullOrEmpty(Organisation)) {
    		   columnVals.addElement(Organisation);
    	   }
       }
       return columnVals;
   }

   /**
    * This method gets the  Member Access Granted
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectList    - List of All members Details
    * @return Vector with All the member Access Granted
    * @throws Exception if a major failure occurs
    */
   public Vector getMemberAccess(Context context, String[] args)
   throws Exception
   {
       HashMap programMap  = (HashMap)JPO.unpackArgs(args);
       HashMap paramList   = (HashMap)programMap.get("paramList");
       String languageStr  = (String)paramList.get("languageStr");
       MapList objList     = (MapList)programMap.get("objectList");
       Vector columnVals   = new Vector(objList.size());
       Iterator itr        = objList.iterator();
       while (itr.hasNext()) {
           String  strTempAccess        = (String) ((Map)itr.next()).get("AccessList");
           if(!UIUtil.isNullOrEmpty(strTempAccess)){
               strTempAccess            = FrameworkUtil.findAndReplace(strTempAccess, " ", "");
               strTempAccess            = "emxComponents.ObjectAccess."+strTempAccess.trim();
               columnVals.addElement(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(languageStr),strTempAccess));
           }

       }
       return columnVals;
   }

   /**
    * This method gets the Access Range Values
    *
    * @param args holds the following input arguments:
    *    requestMap     - should have languageStr & accessChoice (e.g. Add,Add_Remove,Read..)
    * @return HashMap with the valid Access Choices
    * @throws Exception if a major failure occurs
    */
   public HashMap getAccessRangeValues (Context context, String[] args)
   throws Exception
   {
       HashMap programMap  = (HashMap) JPO.unpackArgs(args);
       HashMap requestMap  = (HashMap)programMap.get("requestMap");
       String languageStr  = (String)requestMap.get("languageStr");
       String accessChoice = (String)requestMap.get("accessChoice");
       // Get the valid access choices list
       StringList validAccessList  = getValidAccessList(context, accessChoice);
       HashMap accessMap           = new HashMap();
       StringList displayValueList = new StringList();
       StringList actualValueList  = new StringList();
       String strAccess            = "";
       String strTempAccess        = "";
       int validAccessListSize     = validAccessList.size();
       for (int i = 0; i < validAccessListSize; i++) {
           strAccess               = (String)validAccessList.get(i);
           // Internationalize the access values for displaying
           strTempAccess           = FrameworkUtil.findAndReplace(strAccess, " ", "");
           strTempAccess           = "emxComponents.ObjectAccess."+strTempAccess.trim();
           displayValueList.addElement(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),strTempAccess));
           actualValueList.addElement(strAccess);
       }

       // Put the display and actual values
       accessMap.put("field_display_choices", displayValueList);
       accessMap.put("field_choices", actualValueList);
       return accessMap;
  }

   /**
    * This method gets the Valid Access Choices from the Property File
    *
    * @param context the eMatrix <code>Context</code> object
    * @param String access passed as comma separated values
    * @return StringList with valid Access Choices
    * @throws Exception if a major failure occurs
    */
   public StringList getValidAccessList (Context context, String access)
   throws Exception {
       StringList strListAccess     = new StringList();
       StringList validAccessList   = new StringList();
       if(!UIUtil.isNullOrEmpty(access)) {
           access = FrameworkUtil.findAndReplace(access, "_", " ");
           // This is the list of available access rights
           strListAccess            = FrameworkUtil.split(access, ",");
           int strListAccessSize    = strListAccess.size();
           // Checks if the property is defined for each access
           for (int k=0; k < strListAccessSize; k++) {
               String tempAccess    = (String)strListAccess.get(k);
               String propValue     = EnoviaResourceBundle.getProperty(context,"emxComponents.AccessMapping."+FrameworkUtil.findAndReplace(tempAccess, " ", "").trim());
               if(propValue != null && !"null".equals(propValue) && !"".equals(propValue)) {
                   validAccessList.add(tempAccess);
               }
           }
       }
       return validAccessList;
   }

   /**
    * This method is used to search the roles (in add Roles)/
    * groups( in add groups)based on given criteria
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    RequestValuesMap       - should contain the following values
    *      LBCTopLevelCheckboxFilter   - Top Level Roles
    *      LBCSublevelCheckBoxFilter   - Sublevel Roles
    *      LBCNameMatchesTextboxFilter - Name Search pattern
    * @return MapList with All the member Access Granted
    * @throws Exception if a major failure occurs
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getRoleGroupSearchResults(Context context, String[] args)
   throws FrameworkException {
       try {
           Map programMap      = (Map) JPO.unpackArgs(args);

           String action        = (String)programMap.get("useMode");
           String sTopChecked  = (String)programMap.get("LBCTopLevelCheckboxFilter");
           String sSubChecked  = (String)programMap.get("LBCSublevelCheckBoxFilter");
           String sNamePattern = (String)programMap.get("LBCNameMatchesTextboxFilter");

           String objectId     = (String)programMap.get("objectId");
           MapList allRolesList = new MapList();
           allRolesList= getAllRoleGroupList(context, objectId, sNamePattern, sSubChecked, sTopChecked,action);
           return allRolesList;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }



   /**
    * This method gets the Role for Adding Roles /Groups for Adding Groups to Folder Access
    *
    * @param context the eMatrix <code>Context</code> object
    * @param objectId
    * @param namePattern - search criteria value for name field
    * @param sSubChecked - should be true/false
    * @param sTopChecked - should be true/false
    * @return Vector with All the member Access Granted
    * @throws Exception if a major failure occurs
    */
   public MapList getAllRoleGroupList(Context context, String objectId, String namePattern, String sSubChecked, String sTopChecked,String sAction)
   throws Exception {
       Role role                = null;
       Group group              = null;
       List roleGroupList       = null;
       List topRoleGroupList    = null;
       MapList resultsList      = new MapList();
       String type              = "";
       if (!UIUtil.isNullOrEmpty(sAction) && sAction.equals("addGroup"))  {
           type                 = "Group";
           group                = new Group();
           roleGroupList        = getGroupNameList(group.getGroups(context));
           topRoleGroupList     = getGroupNameList(group.getTopLevelGroups(context));
       } else {
            type                = "Role";
            role                = new Role();
            roleGroupList       = getRoleNameList(role.getRoles(context));
            topRoleGroupList    = getRoleNameList(role.getTopLevelRoles(context));
       }
       if(!"true".equalsIgnoreCase(sTopChecked) && "true".equalsIgnoreCase(sSubChecked)) {
           roleGroupList.removeAll(topRoleGroupList);
       } else if("true".equalsIgnoreCase(sTopChecked) && !"true".equalsIgnoreCase(sSubChecked)) {
           roleGroupList        = topRoleGroupList;
       }
       Pattern pattern          = namePattern != null ? new Pattern(namePattern) : new Pattern("*");
       String command           = "print system  casesensitive";
       String caseStatus        = MqlUtil.mqlCommand(context, command);

       if(caseStatus.equals("CaseSensitive=Off")) {
           pattern.setCaseSensitive(false);
       }
       List memberRoleGroups    = excludeExistingAccessGrantees(context,objectId,type);
       for (int i = 0; i < roleGroupList.size(); i++) {
           String sRoleGroup    = (String) roleGroupList.get(i);
           if(!pattern.match(sRoleGroup) || memberRoleGroups.contains(sRoleGroup)) {
               continue;
           }
           Map map              = new HashMap();
           map.put(SELECT_ID, sRoleGroup);
           resultsList.add(map);
       }
       return resultsList;
   }

   /**
    * This method send Role Names from the given roles List
    *
    * @param rolesList
    * @return ArraryList of Roles
    * @throws Exception if a major failure occurs
    */
   protected List getRoleNameList(RoleList rolesList)
   throws Exception  {
       List arrayList       = new ArrayList(rolesList.size());
       RoleItr roleItr      = new RoleItr(rolesList);
       while (roleItr.next()) {
           String sRole       = roleItr.obj().getName();
           arrayList.add(sRole);
       }
       return arrayList;
   }

   /**
    * This method gets the Groups for Adding Roles to Folder Access
    *
    * @param groupsList
    * @return ArraryList of Group Names
    * @throws Exception if a major failure occurs
   */
   protected List getGroupNameList(GroupList groupsList)
   throws Exception {
       List arrayList      = new ArrayList(groupsList.size());
       GroupItr groupItr   = new GroupItr(groupsList);
       while (groupItr.next()) {
           String sGroup  = groupItr.obj().getName();
           arrayList.add(sGroup);
       }
       return arrayList;
   }

   /**
    * This method is Called on Clicking Apply in the Access Summary Page, For Added and Removed Members
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    parentOID     - parentOID
    *    contextData   - Will have the XML data of the newly Added or Removed Members
    * @return HashMap with Success/Error and the modified Data
    * @throws Exception if a major failure occurs
    */
   @com.matrixone.apps.framework.ui.ConnectionProgramCallable
   public HashMap inlineAddRemoveAccess(Context context, String[] args)
   throws Exception{

       HashMap doc                 = new HashMap();
       HashMap request             = (HashMap) JPO.unpackArgs(args);
       String parentOID            = (String)request.get("parentOID");
       Element elm                 = (Element) request.get("contextData");

       MapList chgRowsMapList      = UITableIndented.getChangedRowsMapFromElement(context, elm);
       MapList mlItems             = new MapList();
       HashMap hmMemberAddAccess   = new HashMap();
       HashMap hmRelAttributesMap  = null;
       HashMap columnsMap          = null;
       HashMap changedRowMap       = null;
       HashMap returnMap           = null;
       Map smbAttribMap            = null;
       StringList slMemberRevokeAccess = new StringList();

       try {
           for (int i = 0, size = chgRowsMapList.size(); i < size; i++) {
               changedRowMap           = (HashMap) chgRowsMapList.get(i);
               String sRowId           = (String) changedRowMap.get("rowId");
               String markup           = (String) changedRowMap.get("markup");
               columnsMap              = (HashMap) changedRowMap.get("columns");
               String childObjectId    = (String) columnsMap.get("Name");
               if(UIUtil.isNullOrEmpty(childObjectId)){
            	   childObjectId = (String) changedRowMap.get("childObjectId");
               }
               
               returnMap               = new HashMap();
               returnMap.put("pid", parentOID);
               returnMap.put("oid", childObjectId);
               returnMap.put("rowId", sRowId);
               returnMap.put("markup", markup);
               returnMap.put("columns", columnsMap);
               mlItems.add(returnMap);
               if("add".equalsIgnoreCase(markup)) {
                   hmMemberAddAccess.put(childObjectId,columnsMap.get("Access"));
               } else if ("cut".equalsIgnoreCase(markup)) {
                   slMemberRevokeAccess.add(childObjectId);
               }
           }
           if(hmMemberAddAccess.size() > 0) {
               addGrantees(context,parentOID,hmMemberAddAccess);
           }
           if(slMemberRevokeAccess.size()>0) {
               removeGrantees(context,parentOID,(String[])slMemberRevokeAccess.toArray(new String [slMemberRevokeAccess.size()]));
           }
           doc.put("Action", "success");
           doc.put("changedRows", mlItems);
       } catch (Exception e) {
           doc.put("Action", "ERROR");
           doc.put("Message", e.getMessage());
       }
       return  doc;
   }

  /**
    * This method is Called on Clicking Apply in the Access Summary Page for Access Edited members
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    RequestMap  - should have object id
    *    tableData   - Will have the XML data of the newly Added or Removed Members
    * @return HashMap with Success/Error and the modified Data
    * @throws Exception if a major failure occurs
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public HashMap editMemberAccess(Context context, String[] args)
   throws Exception {
       HashMap retMap          = new HashMap();
       HashMap columnsMap      = null;
       HashMap changedRowMap   = null;
       HashMap returnMap       = null;
       HashMap programMap      = (HashMap) JPO.unpackArgs(args);
       HashMap tableData       =  (HashMap)programMap.get("tableData");
       HashMap requestMap      = (HashMap)tableData.get("RequestMap");
       String parentOID        = (String)requestMap.get(LibraryCentralConstants.OBJECT_ID);
       MapList mlItems         = new MapList();
       HashMap hmMemberAddAccess = new HashMap();
       try
       {
           Document doc            = (Document) programMap.get("XMLDoc");
           Element elm = doc.getRootElement();
           MapList chgRowsMapList = UITableIndented.getChangedRowsMapFromElement(context, elm);

           for (int i = 0, size = chgRowsMapList.size(); i < size; i++) {
               changedRowMap           = (HashMap) chgRowsMapList.get(i);
               String childObjectId    = (String) changedRowMap.get("childObjectId");
               String sRowId           = (String) changedRowMap.get("rowId");
               String markup           = (String) changedRowMap.get("markup");
               columnsMap              = (HashMap) changedRowMap.get("columns");
               returnMap               = new HashMap();
               returnMap.put("pid", parentOID);
               returnMap.put("oid", childObjectId);
               returnMap.put("rowId", sRowId);
               returnMap.put("markup", markup);
               returnMap.put("columns", columnsMap);
               mlItems.add(returnMap); // returnMap having all the
               if("changed".equalsIgnoreCase(markup)) {
                   hmMemberAddAccess.put(childObjectId,columnsMap.get("Access"));
               }
           }
           if(hmMemberAddAccess.size() > 0) {
               addGrantees(context,parentOID,hmMemberAddAccess);
           }
           retMap.put("Action", "success");
           retMap.put("changedRows", mlItems);
       } catch (Exception excep) {
           retMap.put("Action", "ERROR");
           retMap.put("Message", excep.getMessage());
       }
       return retMap;
   }

   /**
    * Method to grant Global Read Access to created folder
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - HashMap Containing following value
    *        paramMap - HashMap containing following values
    *                   objectId  - String containing Object Id of the Folder
    *                   New Value - String containing GlobalReadAccess Field
    *                               value (Allowed values : "true" or "false")
    * @throws Exception if the operation fails
    */
   public void updateGlobalReadAccess(Context context, String[] args)throws Exception
   {
       try
       {
           HashMap programMap      = (HashMap)JPO.unpackArgs(args);
           HashMap paramMap        = (HashMap)programMap.get("paramMap");
           String objectId         = (String)paramMap.get(OBJECT_ID);
           String globalReadAccess = (String)paramMap.get(LibraryCentralConstants.JPO_ARGS_NEW_VALUE);
               DomainObject domainObj       = new DomainObject(objectId);
               ContextUtil.startTransaction(context,true);
               domainObj.setAttributeValue(context, ATTRIBUTE_GLOBAL_READ, globalReadAccess);
           changeSovGlobalReadAccess(context,objectId);
               ContextUtil.commitTransaction(context);
           }
       catch(Exception ex)
       {
           throw new FrameworkException(ex.toString());
       }
   }

   /**
    * Method to change the owner of the folder created
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - HashMap Containing following value
    *        paramMap - HashMap containing following values
    *                   objectId  - String containing Object Id of the Folder
    *                   New Value - String containing name of the new owner
    * @throws Exception if the operation fails
    */
   public void updateOwnerField(Context context, String[] args)throws Exception
   {
       try
       {
           HashMap programMap  = (HashMap)JPO.unpackArgs(args);
           HashMap paramMap    = (HashMap)programMap.get("paramMap");
           String objectId     = (String)paramMap.get(OBJECT_ID);
           String owner        = (String)paramMap.get(LibraryCentralConstants.JPO_ARGS_NEW_VALUE);
           String user         = context.getUser();
           if(owner != null && !owner.equalsIgnoreCase(user)){
               DomainObject domainObj       = new DomainObject(objectId);
               HashMap memberIdsAndAccess   = new HashMap();
               //add Read access to user
               memberIdsAndAccess.put(user, AccessUtil.READ);

               ContextUtil.startTransaction(context,true);
               // change the owner of the folder created
               domainObj.setOwner(context,owner);
               addGrantees(context,objectId,memberIdsAndAccess);
               ContextUtil.commitTransaction(context);
           }
       }
       catch(Exception ex)
       {
           throw new FrameworkException(ex.toString());
       }
   }

   /**
    * Method to delete the folder
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - HashMap Containing following value
    *        id - String containing Object Id of the Folder to be deleted
    * @throws Exception if the operation fails
    */
    public static String deleteObject(Context context, String[] args) throws Exception{
       if ((args == null) || (args.length < 1)){
           throw (new IllegalArgumentException ());
       }
       String strResult = "false";
       try{
           Map map = (Map) JPO.unpackArgs (args);
           String objectId = (String) map.get (SELECT_ID);
           DomainObject folderObj = new DomainObject(objectId);

           MapList folderContents = folderObj.getRelatedObjects(context,
                   DomainConstants.RELATIONSHIP_SUBVAULTS+","+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2,
                   "*",
                   new StringList("id"),
                   null,
                   false,
                   true,
                   new Short("1"),
                   "",
                   "",
                   0);
           if(folderContents.size() == 0){
               ContextUtil.startTransaction(context, true);
               sendEventIconMail(context, LibraryCentralConstants.EVENT_FOLDER_REMOVED, objectId, "");
               MapList parentFolders = folderObj.getRelatedObjects(context,
                       DomainConstants.RELATIONSHIP_SUBVAULTS,
                       DomainConstants.TYPE_WORKSPACE_VAULT,
                       new StringList("id"),
                       null,
                       true,
                       false,
                       new Short("1"),
                       "",
                       "",
                       0);
               Iterator itr = parentFolders.iterator();
               while(itr.hasNext()){
                   String parentFolderId = (String)((Map)itr.next()).get("id");
                   sendEventIconMail(context, LibraryCentralConstants.EVENT_CONTENT_REMOVED, parentFolderId, objectId);
               }
               folderObj.deleteObject(context);
               ContextUtil.commitTransaction(context);
               strResult = "true";
           }
       }catch(Exception ex){
           ContextUtil.abortTransaction(context);
           throw new FrameworkException(ex.toString());
       }
       return strResult;
   }

    /**
    * Method to add/remove Organization as SOV with Read so all the users in the organization will have Read access on this folder
    * as per Global Read access attribute
    *
    * @param context the eMatrix <code>Context</code> object
    * @param id - String containing Object Id of the Folder
    * @throws Exception if the operation fails
    */
    public void changeSovGlobalReadAccess(Context context, String objectId) throws Exception
    {
    	try
        {
            DomainObject obj      = new DomainObject(objectId);
            String attrGlobalReadValue  = obj.getAttributeValue(context, ATTRIBUTE_GLOBAL_READ);
            String Org=obj.getInfo(context, SELECT_ORGANIZATION);
            String comment = "Object Ownership of " + obj.getInfo(context, DomainObject.SELECT_NAME) + " for Global Read Access from LBC Application ";
           
            if(attrGlobalReadValue!= null && !"null".equalsIgnoreCase(attrGlobalReadValue) && attrGlobalReadValue.equalsIgnoreCase("True"))
            {
            	DomainAccess.createObjectOwnership(context, objectId, Org, null,"Read", comment);
            }
            else
            {
            	DomainAccess.deleteObjectOwnership(context, objectId, Org, null, comment);
            }
        } catch(Exception ex)
        {
            throw ex;
        }

    }

}
