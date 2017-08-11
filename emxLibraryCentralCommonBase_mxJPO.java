/*
 *  emxLibraryCentralCommonBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessList;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.PolicyItr;
import matrix.db.PolicyList;
import matrix.db.RelationshipType;
import matrix.db.StateRequirementItr;
import matrix.db.StateRequirementList;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;
import matrix.util.StringResource;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Subscribable;
import com.matrixone.apps.common.SubscriptionManager;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.document.DCWorkspaceVault;
import com.matrixone.apps.document.DocumentCentralConstants;
import com.matrixone.apps.document.EventConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.Libraries;
import com.matrixone.apps.library.LibraryCentralCommon;
import com.matrixone.apps.library.LibraryCentralConstants;

/**
 * The <code>emxLibraryCentralCommonBase.java</code> class contains utility methods for
 * getting data using configurable tables  in Library Central.
 *
 * @version Common 10-5 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxLibraryCentralCommonBase_mxJPO extends emxDomainObject_mxJPO
implements LibraryCentralConstants
{
    /**
     * Creates a emxLibraryCentralCommonBase Object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     * @throws Exception if the operation fails
     */

    public emxLibraryCentralCommonBase_mxJPO (Context context,
                         String[] args) throws Exception
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
public int mxMain(Context context, String[] args) throws Exception
{
    if (!context.isConnected())
        throw new Exception("not supported on desktop client");
    return 0;
}

 /**
     * Entry point for business object creation.  The required
     * arguments are defined in <code>getRequiredCreateArgKeys ()</code>
     * Warning:  do to the static nature of this system if sub-classes
     * need to modified implementation details, this function must be
     * overridden.
     *
     * @param context the eMatrix <code>Context</code> user's context
     * @param args contains a Map with the following entries:
     *        type   - a String of the Object Type
     *        name   - a String of the Object Name
     *        policy - a String of the Object Policy
     * @return string with created object ID
     * @throws Exception if a major failure occurs
     */
    public String createObject (Context context, String[] args)
      throws IllegalArgumentException, FrameworkException
    {
       String type = null;
       String name = null;
       String revision = null;
       String policy = null;
       String vault = null;
       Map attributeMap = null;
       Map specificationMap = null;

       try
       {
          Map argMap = ( Map ) JPO.unpackArgs ( args );

          String[] argKeys = getRequiredCreateArgKeys ();

          if ( !isArgMapValid(context, argKeys, argMap) )
          {
             throw new IllegalArgumentException(); // missing argument key
          }

          type = (String)argMap.get(LibraryCentralConstants.JPO_ARGS_TYPE );
          name = (String) argMap.get (LibraryCentralConstants.JPO_ARGS_NAME );
          revision =(String)argMap.get(LibraryCentralConstants.JPO_ARGS_REVISION );
          policy =(String)argMap.get(LibraryCentralConstants.JPO_ARGS_POLICY );
          vault = (String)argMap.get(LibraryCentralConstants.JPO_ARGS_VAULT );
          attributeMap =(Map)argMap.get(LibraryCentralConstants.JPO_ARGS_ATTR_MAP );
          specificationMap =(Map)argMap.get(LibraryCentralConstants.JPO_ARGS_SPEC_MAP );
       }
       catch ( Exception e )
       {

          // missing argument key or bad cast
          throw new IllegalArgumentException ();
       }
       String objectId = null;

       objectId = createObject(context,type,name,revision,policy,vault,attributeMap,specificationMap);

       return objectId;    //newly created object id
    }

    /**
     * This method performs the overall create operation using "native"
     * arguments.
     *
     * @param context          - user's context
     * @param type             - type of object to create
     * @param name             - name of object to create
     * @param revision         - revision of object to create
     * @param policy           - policy of object to create
     * @param vault            - vault of object to create
     * @param attributeMap     - custom attributes, description, & owner
     * @param specificationMap - other required information for create
     * @return object id created
     * @throws com.matrixone.framework.util.FrameworkException if a major
     *          failure occurs
     */
    protected String createObject (Context context,
                                   String  type,
                                   String  name,
                                   String  revision,
                                   String  policy,
                                   String  vault,
                                   Map     attributeMap,
                                   Map     specificationMap)
            throws FrameworkException
    {

       BusinessObject busObj = null;

       try
        {
          ContextUtil.startTransaction ( context , true );
          //allow customization on the pre-create
            String description =(String) attributeMap.get (JPO_ALIAS_DESCRIPTION);
            String owner =(String) attributeMap.get (JPO_ALIAS_OWNER);

            busObj = createBusObject (context,
                                      type,
                                      name,
                                      revision,
                                      policy,
                                      vault,
                                      owner,
                                      description);

          String originator = (String) attributeMap.get ("attribute_Originator");
          if("".equals(originator))
          {
            attributeMap.put("attribute_Originator",context.getUser());
          }

            finishBusObject (context,
                             busObj,
                             owner,
                             attributeMap,
                             specificationMap);

            ContextUtil.commitTransaction ( context );
            ContextUtil.startTransaction ( context , true );

                        handleNotifyUsers (context,
                               busObj,
                               specificationMap);

            ContextUtil.commitTransaction ( context );
        }
        catch ( Exception e )
        {

           ContextUtil.abortTransaction ( context );
           throw new FrameworkException ( e );
        }
        finally
        {
            if ( busObj != null )
            {
                try
                {
                    busObj.close ( context );
                }
                catch ( Exception ex )
                {
                    throw new FrameworkException ( ex.getMessage () );
                }
            }
        }


        return busObj.getObjectId ();
    }
    /**
     * Create Bus Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param type             - type of object to create
     * @param name             - name of object to create
     * @param revision         - revision of object to create
     * @param policy           - policy of object to create
     * @param vault            - vault of object to create
     * @param owner            - owner of object to create
     * @param description      - description of object to create
     * @return BusinessObject  - returns newly created BusinessObject
     * @throws Exception if the operation fails
     */
    protected BusinessObject createBusObject (Context context,
                                              String type,
                                              String name,
                                              String revision,
                                              String policy,
                                              String vault,
                                              String owner,
                                              String description)
            throws Exception
    {

       BusinessObject busObj = null;

       // Create using auto name if name is null
       if ( name == null || name.length() < 1)
       {
          String aliasType = FrameworkUtil.getAliasForAdmin (context,
                                                             "type",
                                                             type,
                                                             true);

          String aliasPolicy = FrameworkUtil.getAliasForAdmin (context ,
                                                               "policy" ,
                                                               policy ,
                                                               true);


          busObj = new BusinessObject ( FrameworkUtil.autoName (context,
                                                                aliasType,
                                                                "",
                                                                aliasPolicy,
                                                                vault,
                                                                revision) );
       }
       else
       {
          busObj = new BusinessObject (type,
                                       name,
                                       revision,
                                       vault);
          busObj.create (context, policy);
       }

       busObj.open (context);
       busObj.setOwner (owner);

       // Seting The Description

       if ( (description != null) && (description.length () > 0) )
       {
          busObj.setDescription (description);
       }


       return busObj;
    } //createBusObject

    /**
     *  Performs additional operations here.After basic object creation or changes are complete.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param busObj
     * @param owner            - owner of object to create
     * @param attributeMap     - map containing custom attributes, description, & owner
     * @param specificationMap - map containing other required information for clone
     * @throws Exception if a major failure occurs
     */
    protected void finishBusObject (Context        context,
                                    BusinessObject busObj,
                                    String         owner,
                                    Map            attributeMap,
                                    Map            specificationMap)
       throws Exception
    {



       setCustomAttributes(context,
                           busObj,
                           attributeMap);

       DomainObject domainObj = new DomainObject ( busObj );

       String[] containingObjId = null;

       Object obj =
          specificationMap.get(LibraryCentralConstants.JPO_SPEC_CONTAINING_ID);

       if ( obj != null )
       {
          if ( obj.getClass ().isArray () )
          {
             containingObjId = ( String[] ) obj;
          }
          else
          {
             containingObjId = new String[ 1 ];
             containingObjId[ 0 ] = ( String ) obj;
          }

          String containingRel =
             (String)specificationMap.get
             (LibraryCentralConstants.JPO_SPEC_CONTAINING_RELATIONSHIP);

          addToContainingObject(context,
                                domainObj,
                                containingObjId,
                                containingRel);
       }
       String folderObjId =(String)specificationMap.get(LibraryCentralConstants.JPO_SPEC_FOLDER_ID);
       addToFolder (context, domainObj, folderObjId);
       busObj.update (context);
       grantOwnerAccess (context, busObj, owner);

    } //finishNewBusObject

    /**
     * Grants Access to Owner
     *
     * @param context the eMatrix <code>Context</code> object
     * @param busObj
     * @param owner
     * @throws FrameworkException  if the operation fails
     */
    protected void grantOwnerAccess (Context        context,
                                     BusinessObject busObj,
                                     String         owner)
       throws Exception
    {

      String user = context.getUser ();

      if ( (owner != null) && (!owner.equalsIgnoreCase (user)) )
      {
        //Grant Access To The User

        // Push The Context

        ContextUtil.pushContext (context);
        BusinessObjectList boList = new BusinessObjectList ();
        boList.add ( busObj );
        AccessUtil accessUtil = new AccessUtil ();
        Access access = accessUtil.getReadAccess ();
        access.setUser ( user );

        AccessList accessList = new AccessList ();
        accessList.add ( access );

        // Grant The Access For The List Of Grantees On The
        // Bus Object List

        DomainObject.grantAccessRights(context, boList, accessList);

        ContextUtil.popContext ( context );
      } //if


    } //grantOwnerAccess

    /**
     * Sets Attributes on the newly crated Business Obejct
     *
     * @param context the eMatrix <code>Context</code> object
     * @param busObj
     * @param attrMap
     * @throws Exception if the operation fails
     */
    protected void setCustomAttributes (Context        context,
                                        BusinessObject busObj,
                                        Map            attrMap)
      throws Exception
    {


      // Setting The Attributes
      java.util.Set set = null;
      AttributeList attribList = new AttributeList ();

      set = attrMap.keySet ();

      Iterator itr = set.iterator ();
      String key = null;

      // Looping thro' attribute lists which can be set by
      // the user thro' Display Rule Setting
      String symbolicAttrName = null;

      // Iterate Through The List

      while (itr.hasNext ())
      {
        key = ( String ) itr.next ();
        if ( key.equals ("attribute_Description")
             || key.equals ("attribute_Owner"))
          continue;  // wacky basic's in custom area, don't process

        String value = ( String ) attrMap.get (key);

        if (value == null )
        {
           value = "";
        }

        symbolicAttrName = PropertyUtil.getSchemaProperty (context,key);

        if (symbolicAttrName != null
            && symbolicAttrName.length () > 0)
        {
           AttributeType tmpAttrType = new AttributeType (symbolicAttrName);
           attribList.addElement (new Attribute (tmpAttrType, value));
        }

        symbolicAttrName = null;

      }

      // Set The Attributes On The Bus Obj
      busObj.setAttributeValues (context, attribList);
    }

    /**
     * Entry point for connecting object to one or more parents.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectid          - objectid
     *    id list           - list of ids to connect to
     * @throws Exception if a major failure occurs
     */
    public void addToContainingObject ( Context context , String[] args )
            throws Exception
    {
       try
       {
          Map specificationMap = ( Map ) JPO.unpackArgs ( args );

          String objectId = ( String ) specificationMap.get (
             LibraryCentralConstants.OBJECT_ID );

          DomainObject domainObj = new DomainObject ( objectId );

          String[] containingObjId = null;
          Object obj = specificationMap.get(LibraryCentralConstants.JPO_SPEC_CONTAINING_ID);

          if ( obj != null )
          {
             if ( obj.getClass ().isArray () )
             {
                containingObjId = ( String[] ) obj;
             }
             else
             {
                containingObjId = new String[ 1 ];
                containingObjId[ 0 ] = ( String ) obj;
             }

             String containingRel =
                (String)specificationMap.get
                (LibraryCentralConstants.JPO_SPEC_CONTAINING_RELATIONSHIP);

             addToContainingObject(context,
                                   domainObj,
                                   containingObjId,
                                   containingRel);
          }

       }
       catch ( Exception e )
       {
          // missing argument key or bad cast

          throw new IllegalArgumentException ();
       }

    }

    /**
     * Adds to containing Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param domainObj
     * @param containingObjId
     * @param containingRel
     * @throws Exception if the operation fails
     */
    protected void addToContainingObject (Context      context,
                                          DomainObject domainObj,
                                          String[]     containingObjIds,
                                          String       containingRel)
       throws Exception
   {


      if ( containingRel != null
           && ! containingRel.equals("null")
           && containingObjIds != null
           && containingObjIds.length > 0)
      {
         for (int i=0; i<containingObjIds.length; i++)
         {
            if ( ( containingObjIds[i] != null )
                 && ( containingObjIds[i].length() > 0 ) )
            {
               DomainObject newDO = new DomainObject (containingObjIds[i]);
               DomainRelationship.connect (context,
                                           newDO,
                                           containingRel ,
                                           domainObj);
            } //if : we have something to process
         } //for : loop through all the containing object IDs
      }


   } //addToContainingObject

   /**
    * Entry point for connecting object to a folder.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    objectId          - objectId
    *    folderId          - folder to connect to
    *    user list         - users to send messages to
    * @throws Exception if a major failure occurs
    */
   public void addToFolder ( Context context , String[] args )
           throws Exception
   {
      try
      {
         Map specificationMap = (Map)JPO.unpackArgs(args);
         String objectId = (String)specificationMap.get(LibraryCentralConstants.OBJECT_ID);
         String folderId =(String)specificationMap.get(LibraryCentralConstants.JPO_SPEC_FOLDER_ID);
         DomainObject domainObj = new DomainObject ( objectId );
         addToFolder (context,domainObj,folderId);
      }
      catch ( Exception e )
      {
         // missing argument key or bad cast

         throw new IllegalArgumentException ();
      }

   }

    /**
     * Adds content to Folder
     *
     * @param context the eMatrix <code>Context</code> object
     * @param domainObj
     * @param folderId   - folder id.
     * @throws Exception if the operation fails
     */
   protected void addToFolder (Context context, DomainObject domainObj,String  folderId)
   throws Exception
   {

        String strType              = domainObj.getInfo(context,DomainConstants.SELECT_TYPE);
        String folderRelationship   = "";

        if(PropertyUtil.getSchemaProperty (context,"type_ProjectVault").equalsIgnoreCase(strType)){
           folderRelationship       = PropertyUtil.getSchemaProperty(context,"relationship_SubVaults");
       } else {
            folderRelationship       = PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocumentsRev2");
       }
       if (folderId != null&& folderId.length () > 0 && ! folderId.equals("null"))
       {
            StringTokenizer st = new StringTokenizer ( folderId , "|" );

            while ( st.hasMoreTokens () )
            {
            String temp = st.nextToken ();

			WorkspaceVault workspaceVaultObj = new WorkspaceVault(temp);
			workspaceVaultObj.setContentRelationshipType(folderRelationship);
			workspaceVaultObj.addItems(context,(new String[] {domainObj.getId(context)}));
            sendEventIconMail(context, LibraryCentralConstants.EVENT_CONTENT_ADDED, new DCWorkspaceVault(temp), domainObj.getObjectId());
            
            } //while
        } //if

}

    /* Pulls from the specification map the required values, and then invokes
     *   necessary method for notifying the users.
     *
     * @param context A Matrix DB context
     * @param objectId The Matrix ObjectId of the object for which
     * notification should be sent.
     * @param specificationMap A specification map, containing the keys for
     * the message (JPO_SPEC_NOTIFY_MESSAGE) and the users to whom the
     * notification is to be sent (JPO_SPEC_NOTIFY_TO_USERS).
     * @throws Exception if the operation fails
     */
    public void handleNotifyUsers (Context        context,
                                   BusinessObject busObject,
                                   Map            specificationMap)
       throws Exception
    {
       String subject =(String)specificationMap.get (JPO_SPEC_NOTIFY_SUBJECT);
       String message =(String) specificationMap.get (JPO_SPEC_NOTIFY_MESSAGE);
       String toUsersList =(String) specificationMap.get (JPO_SPEC_NOTIFY_TO_USERS);

       notifyUsers (context,
                    busObject,
                    subject,
                    message,
                    toUsersList);


    } //handleNotifyUsers


    /**
     * Entry point for notifying users that a object is being created.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    subject           - subject
     *    message           - message to send
     *    user list         - users to send messages to
     * @throws Exception if a major failure occurs
     */
    public void notifyUsers ( Context context , String[] args )
            throws Exception
    {
       try
       {
          Map specificationMap = ( Map ) JPO.unpackArgs ( args );

          String objectId = (String)specificationMap.get(LibraryCentralConstants.OBJECT_ID );
          String subject =(String)specificationMap.get (JPO_SPEC_NOTIFY_SUBJECT);
          String message =(String)specificationMap.get (JPO_SPEC_NOTIFY_MESSAGE);
          String toUsersList =(String)specificationMap.get (JPO_SPEC_NOTIFY_TO_USERS);

          BusinessObject busObject = new BusinessObject (objectId);

          notifyUsers (context,
                       busObject,
                       subject,
                       message,
                       toUsersList);
       }
       catch ( Exception e )
       {
          // missing argument key or bad cast

          throw new IllegalArgumentException ();
       }
    }

    /**
     * Notifies the Users
     *
     * @param context the eMatrix <code>Context</code> object
     * @param busObj
     * @param message
     * @param toUsersList
     * @throws Exception if the operation fails
     */
    protected void notifyUsers (Context        context,
                                BusinessObject busObj,
                                String         subject,
                                String         message,
                                String         toUsersList)
            throws Exception
    {

      if ( toUsersList != null && toUsersList.length() > 0 && ! toUsersList.equals("null"))
      {
        String delimiter    = ";";
        if (toUsersList.indexOf("|") >= 0)
        {
            delimiter   = "|";
        }

        StringTokenizer st = new StringTokenizer ( toUsersList , delimiter );
        StringList toList  = new StringList ();

        // Iterate Through The User List
        //
        while ( st.hasMoreTokens () )
        {
          String temp = st.nextToken ();
          toList.addElement ( temp );
        }


        Locale locale = emxMailUtil_mxJPO.getLocale ( context );

        // Reading Property Values from Application Property files
        //
        StringList objList = new StringList ( 1 );

        busObj.open (context);

        String type     = busObj.getTypeName ();
        String name     = busObj.getName ();
        String revision = busObj.getRevision ();
        String objectId = busObj.getObjectId ();
        busObj.close (context);

        objList.addElement (objectId);

        Person person      = Person.getPerson(context);
        String companyName = person.getCompany(context).getName();

        if (subject == null || subject.length () == 0)
        {
           String strNotificationSub =
              "emxDocumentCentral.DocumentCentralCommon.NotificationSubject";
           subject =
              emxMailUtil_mxJPO.getString("emxDocumentCentralStringResource",
                                             strNotificationSub,
                                             null,
                                             locale );
        }

        if (message == null || message.length () == 0)
        {
          String [] msgKeys = new String[] {"type",
                                            "name",
                                            "revision",
                                            "user",
                                            "type" };

          type = i18nNow.getTypeI18NString(type,context.getSession().getLanguage());
          String[] msgValues = new String[] {type,
                                             name,
                                             revision,
                                             context.getUser(),
                                             type };

          String strNotificationMessage =
            "emxDocumentCentral.DocumentCentralCommon.NotificationMessage";
          String msg =
            emxMailUtil_mxJPO.getString ("emxDocumentCentralStringResource",
                                            strNotificationMessage,
                                            null,
                                            locale);

          message = StringResource.format (msg, msgKeys, msgValues);
        } //if
        else
        {
          String strAutoName =
            "emxDocumentCentral.DocumentCentralCommonWizard.AutoName";

          String autoName =
             emxMailUtil_mxJPO.getString("emxDocumentCentralStringResource",
                                           strAutoName,
                                           null,
                                           locale);

          int autoNameIndex = message.indexOf(autoName);

          if( autoNameIndex != -1 )
          {
            message = message.substring (0, autoNameIndex)
               + name + message.substring (autoNameIndex + autoName.length());
          }
        } //else

        MailUtil.sendNotification (context,
                                   toList,
                                   null,
                                   null,
                                   subject,
                                   null,
                                   null,
                                   message,
                                   null,
                                   null,
                                   objList,
                                   companyName);
      }
    }

    /**

    /**
     * Subscribes the Event Icon mail
     *
     * @param context the eMatrix <code>Context</code> object
     * @param event
     * @param objId - parentOId
     * @param objAtt - objectId
     * @throws Exception if the operation fails
     */
    protected void sendEventIconMail (Context      context,
                                      String       event,
                                      Subscribable subscribedObj,
                                      String       objAtt)
    {


       try {
          SubscriptionManager subMgr
             = new SubscriptionManager (subscribedObj);

          subMgr.publishEvent (context, event, objAtt);

       }
       catch (Exception e)
       {

          String objId = "";
          try {
             objId = ((DomainObject) subscribedObj).getId ();

          }
          catch (Exception eWeird) { }
       }
    }

    /**
     * Helper function which provides all required argument keys for
     * the create operation.  Specifically, the following Strings are
     * required: <code>LibraryCentralConstants.JPO_ARGS_TYPE,
     *                     LibraryCentralConstants.JPO_ARGS_NAME,
     *                     LibraryCentralConstants.JPO_ARGS_REVISION,
     *                     LibraryCentralConstants.JPO_ARGS_POLICY,
     *                     LibraryCentralConstants.JPO_ARGS_VAULT,
     *                     LibraryCentralConstants.JPO_ARGS_ATTR_MAP,
     *                     LibraryCentralConstants.JPO_ARGS_SPEC_MAP </code>
     *
     * @return String[] of required arquments for create
     * @see com.matrixone.apps.document.LibraryCentralConstants
     * @exclude
     */
    static public String[] getRequiredCreateArgKeys ()
    {
        String[] reqdArgKeys
                = new String[]{LibraryCentralConstants.JPO_ARGS_TYPE,
                               LibraryCentralConstants.JPO_ARGS_REVISION,
                               LibraryCentralConstants.JPO_ARGS_POLICY,
                               LibraryCentralConstants.JPO_ARGS_VAULT,
                               LibraryCentralConstants.JPO_ARGS_ATTR_MAP,
                               LibraryCentralConstants.JPO_ARGS_SPEC_MAP};

        return reqdArgKeys;
    }


    /**
     * This method validates that all required argument keys are
     * contained in the argMap.
     *
     * @param argMap  - argument passed from bean to JPO.
     * @return true if bare-minimum for a valid argument map has been met.
     */
    protected boolean isArgMapValid (Context  context,
                                     String[] argKeys,
                                     Map      mapToValidate)
       throws Exception
    {

       boolean bIsValid = false;

       bIsValid = mapToValidate != null ? true : false;

       for (int i = 0; bIsValid && i < argKeys.length; i++) {

          bIsValid &= mapToValidate.containsKey(argKeys[i]); //missing key=false
       }
      return bIsValid;
    }

    /**
     * Adds child objects to the parent Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *    childIds      - a String array of children object ids
     *    relationship  - the relationship to connect the new object with
     *    parentId      - the id of the parent Object to connect with
     * @returns nothing
     * @throws Exception if the operation fails
     */


    public void addChildren (Context context, String[] args)
       throws Exception
    {

        if ((args == null) || (args.length < 1))
        {
          throw (new IllegalArgumentException());
        }

        //Unpaking the Arguments
        Map map = (Map) JPO.unpackArgs(args);

        //Getting the List from Unpaked Map

        String[] childIds   = (String[]) map.get ("childIds");
        String relationship = (String)   map.get ("relationship");
        String parentId     = (String)   map.get ("objectId");
        DomainObject doObj = new DomainObject(parentId);
        String strHasDocument = PropertyUtil.getSchemaProperty(context,"relationship_HasDocuments");
        DomainRelationship rel = new DomainRelationship();
        rel.connect(context, doObj, relationship, true, childIds);

    }
    /**
     * Deletes  objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *    childIds      - a String array of children object ids
     *    relationship  - the relationship to connect the new object with
     *    parentId      - the id of the parent Object to connect with
     * @returns String with the result of the delete.
     * @throws Exception if the operation fails
     */
    public String deleteLCObjects(Context  context,String[]  packedArgs) throws Exception
      {
        HashMap mapObjsToDelete = null;
        String strResult = "false";
        String strMQL = " ";
        try
        {
            StringBuffer strRelation = new StringBuffer();
            strRelation.append(LibraryCentralConstants.RELATIONSHIP_SUBCLASS);
            strRelation.append(",");
            strRelation.append(LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM);
            Pattern patternRelation    = new Pattern (strRelation.toString());
            String  strRelationPattern = patternRelation.getPattern ();
            mapObjsToDelete = (HashMap)JPO.unpackArgs(packedArgs);
            String strObjectID = (String)mapObjsToDelete.get(DomainObject.SELECT_ID);
            String strTypeName=new DomainObject(strObjectID).getInfo(context, SELECT_TYPE);
            StringList selSelect = new StringList();
            selSelect.addElement(DomainConstants.SELECT_ID);
            DomainObject doObj = new DomainObject(strObjectID);

            MapList mapResultList = doObj.getRelatedObjects(context,strRelationPattern,LibraryCentralConstants.QUERY_WILDCARD,selSelect,null,false,true,(short)1,null,null);
            if(mapResultList.size() == 0){
                ContextUtil.startTransaction(context,true);
                strResult = "true";
                DomainObject doObj1 = new DomainObject(strObjectID);
                String strInterface = doObj1.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);
                Access access = doObj.getAccessMask(context);
                if(access.hasDeleteAccess())
                {
                ContextUtil.pushContext(context);
                //To delete Library Feature Reference-
                if(strTypeName.equalsIgnoreCase(TYPE_GENERAL_LIBRARY)){
                	MapList referenceFeatureList=doObj.getRelatedObjects(context,
        	        		LibraryCentralConstants.RELATIONSHIP_LIBRARY_REFERENCE_FEATURE,
        	                LibraryCentralConstants.TYPE_LIBRARY_FEATURE_REFERENCE,
        	                selSelect,
        	                null,
        	                false,//boolean getTo,
        	                true,//boolean getFrom,
        	                (short)1,
        	                null,
        	                null,
        	                0);
                	if(referenceFeatureList != null && referenceFeatureList.size()>0){
                		Map referenceIdList=(Map) referenceFeatureList.get(0);
                		new DomainObject((String)referenceIdList.get(SELECT_ID)).deleteObject(context);
                	}

                }
				// Context popping and pushing done for IR-498809-3DEXPERIENCER2017x(PSA11).
				ContextUtil.popContext(context);
                doObj.deleteObject(context);
				ContextUtil.pushContext(context);
                if(strInterface != null && !strInterface.equals("")){
                    strMQL    = "delete interface $1";
                    strResult = MqlUtil.mqlCommand(context, strMQL, strInterface);
                }
                ContextUtil.popContext(context);
                }else
                {
                	throw new Exception();	
                }
                ContextUtil.commitTransaction(context);
            }
        }
        catch (Exception eUnpack)
        {
        	eUnpack.printStackTrace();
            strResult= "false";
            ContextUtil.abortTransaction(context);
            //throw eUnpack;

        }
      //TO invalidate VPLM Cache
        try{
            invalidateVPLMCatalogueCache(context);
        }catch (MatrixException e){
        	e.printStackTrace();
            throw (new FrameworkException(e));
        }
        return strResult;
      }

    /**
     * Removes  objects from a class
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *    childIds      - a String array of children object ids
     *    relationship  - the relationship to connect the new object with
     *    parentId      - the id of the parent Object to connect with
     * @returns String with the result of the delete.
     * @throws Exception if the operation fails
     */
 public String removeObjects(Context context,String[]  packedArgs ) throws Exception {

        HashMap mapObjsToDelete = null;
        HashMap childIds = null;
        String objectsNotRemoved = "";
        String parentId ="";
        String relationship ="";
        String strObjectID="";
        String sRelSubclass = LibraryCentralConstants.RELATIONSHIP_SUBCLASS;
        String sRelClassifiedItem = LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM;
        StringBuffer strRelationhipPattern = new StringBuffer();
        strRelationhipPattern.append(sRelSubclass);
        strRelationhipPattern.append(",");
        strRelationhipPattern.append(sRelClassifiedItem);
        String strChildIds[] = null;
        String searchType="";
        String strQuery = null;
        try
        {
            mapObjsToDelete = (HashMap)JPO.unpackArgs(packedArgs);
            parentId = (String)mapObjsToDelete.get("parentId");
            relationship = (String)mapObjsToDelete.get("relationship");
            childIds = (HashMap)mapObjsToDelete.get("childIds");
            searchType = (String)mapObjsToDelete.get("searchType");



            String strChildId = "";
            DomainObject parentObj = new DomainObject(parentId);
            RelationshipType relType = new RelationshipType(relationship);
            // Temporary variables for children

            DomainObject tempChildObj;
            int iSize = childIds.size();
            StringList slSelAttrs     = new StringList ();
            slSelAttrs.add (SELECT_ID);
            for(int i = 0; i < iSize; i ++) 
            {
            	boolean removed = false;
            	strChildId = (String)childIds.get("childIds["+i+"]");
            	tempChildObj = new DomainObject((String)childIds.get("childIds["+i+"]"));
            	try
            	{
                MapList resultList = (MapList)tempChildObj.getRelatedObjects(context,strRelationhipPattern.toString(),LibraryCentralConstants.QUERY_WILDCARD,slSelAttrs,null,false,true, (short)1,null,null);
                if(resultList.size() == 0) 
	                {
	                   parentObj.disconnect(context,relType,true,tempChildObj);
	                   removed = true;
	                   if(searchType != null && searchType.equalsIgnoreCase("All Levels"))
	                   {
	                        String strResultID  = "";
	                        String strTemp      = "";
	                        strQuery            = "expand bus $1 from relationship $2,$3 recurse to all select bus $4 where $5 == $6 dump $7";
	                        String strResult    = MqlUtil.mqlCommand(context,strQuery,
	                                                                        parentId,
	                                                                        sRelSubclass,
	                                                                        sRelClassifiedItem,
	                                                                        "id",
	                                                                        "from["+sRelClassifiedItem+"].to.id",
	                                                                        strChildId.trim(),
	                                                                        ","
	                                                                );
	                        StringTokenizer stResult    = new StringTokenizer(strResult,"\n");
	                        while(stResult.hasMoreTokens())
	                        {
	                            strTemp = (String)stResult.nextToken();
	                            strResultID = strTemp.substring(strTemp.lastIndexOf(",")+1);
	                            parentObj.setId(strResultID);
	                            parentObj.disconnect(context,relType,true,tempChildObj);
	                        }
	                    }
	                } else
	                {
                    objectsNotRemoved += (String)childIds.get("childIds["+i+"]") + ",";
	                }

            	}catch(Exception e)
		        {
            		objectsNotRemoved += (String)childIds.get("childIds["+i+"]") + ",";
		        }
            }

        strQuery            = "expand bus $1 to relationship $2 recurse to all select bus $3 dump $4";
        objectsNotRemoved+="|"+(String)MqlUtil.mqlCommand(context,strQuery, parentId, sRelSubclass, "id", ",");
    }
    catch (Exception eUnpack)
    {
    	throw eUnpack;

    }
       //--Return Objects Not Removed
        return objectsNotRemoved;
   }

    /**
     * Gets all States for a type
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *    type      - the object type
     * @returns Hashtable with Policy as key and states for that policy as value
     * @throws Exception if the operation fails
     */
    public Hashtable getAllPolicyStatesForType(Context context,String[]  packedArgs ) throws Exception{

        HashMap requestMap  = new HashMap();
        String sType = "";
        Set policySet = null ;
        Hashtable hashPolicies = new Hashtable();
        try
        {
            requestMap = (HashMap)JPO.unpackArgs(packedArgs);
            sType = (String)requestMap.get("type");

            policySet = new HashSet();
            BusinessType tmpBusType =  new BusinessType (sType, context.getVault ());

            PolicyItr policyItr = new PolicyItr(tmpBusType.getPolicies (context));

            while ( policyItr.next () ) {
                policySet.add (policyItr.obj().getName());
            }

            Iterator itr = policySet.iterator();
            Set stateSet = new HashSet();

            while(itr.hasNext())
            {
                String policyName =(String)itr.next();
                Policy policy = new Policy(policyName);
                Vector vecAddesStates = new Vector();
                policy.open(context);
                StateRequirementList stateRequirementList = new StateRequirementList();
                stateRequirementList =(StateRequirementList)policy.getStateRequirements(context);
                int iCount = stateRequirementList.size();
                StateRequirementItr stateRequirementItr = new StateRequirementItr(stateRequirementList);
                while (stateRequirementItr.next ())
                {
                        String state = stateRequirementItr.obj().getName();
                        if(!vecAddesStates.contains(state)){
                            vecAddesStates.addElement(state);
                        }
                }
                hashPolicies.put(policyName,vecAddesStates);
            }
        }
        catch (Exception eUnpack)
        {
        }
        return hashPolicies;
   }

    /**
     * This method used to Send Subscription notification to the user.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param holds the following input arguments:
     *      1 - object ID
     *      2 - event the event name
     * @throws Exception if the operation fails
     */
  public void classifiedItemContentModify(matrix.db.Context context, String[] args)
    throws Exception
  {
            String objectId = args[0];
            String event = args[1];
            String strParentObjId = "" ;
            StringList slSelAttrs   = new StringList ();
            Hashtable mapParent = new Hashtable();
            DomainObject domainObj = new DomainObject();
            MapList mListParent  = new MapList();

            slSelAttrs.add (DomainConstants.SELECT_ID);

          try
          {
            if ( args == null || args.length == 0)
            {
              throw new IllegalArgumentException();
            }


            if (objectId != null && !"".equals(objectId) && !"null".equals(objectId)){

                domainObj.setId(objectId);
                mListParent = (MapList)domainObj.getRelatedObjects(context,DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM,LibraryCentralConstants.QUERY_WILDCARD,slSelAttrs,null,true,false, (short)0,null,null);
                int iParentListSize = mListParent.size();
                String[] oids = new String[iParentListSize];
                if( mListParent.size() != 0) {
                    for(int iParentCnt = 0 ; iParentCnt <mListParent.size() ;iParentCnt++){

                        mapParent = (Hashtable)mListParent.get(iParentCnt);
                        strParentObjId =(String)mapParent.get(DomainConstants.SELECT_ID);
                        oids[iParentCnt] = strParentObjId ;
                    }
                    emxSubscriptionManager_mxJPO subMgr = new emxSubscriptionManager_mxJPO(context, oids);
                    subMgr.publishEvent (context, event,objectId);
              }

            } //end check for empty objectid param.
          }catch(Exception e){
            throw e;
          }
      }

  /**
   * To restrict connecting any from item on Subclass relation to have only limited to types
   * e.g
   * Part Library can have Part Family(and its derivatives) as subclass
   * Any classification type (General Class, Part Family and Manfacturing Partfamily) can have a same type or its derivatives as Subclass
   *    i.e. General Class can have an other General Class as subclass
   *         Part Family can have Part Family and Manfacturing Partfamily as subclass
   *         Manfacturing Partfamily can have an other Manfacturing Partfamily as subclass
   * Libraries (General Library and  Part Library) can have any classification type as subclass
   * So no need to put 'Libraries' in return Map,
   * i.e. if any from item is not included in return Map all items on to side can connect as Subclass to that from side item.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @return Map with key = fromside item (symbolic name) on subclass relation and value = set (of symbolic names) of types that can be subclass for that from item
   * @throws Exception if the operation fails
   */

  public Map getConditionalAllowedClassesInSubclassRelation(Context context, String[] args) throws Exception {
      Map relFromTypes = LibraryCentralCommon.getTypesFromRelation(context, LibraryCentralConstants.RELATIONSHIP_SUBCLASS, true);

      Map allowedTypeMap = new HashMap();
      for (Iterator iter = relFromTypes.keySet().iterator(); iter.hasNext();) {
          String from = (String) iter.next();
          BusinessType bu = (BusinessType) relFromTypes.get(from);
          String buName = bu.getName();
          //For General Class, Part Family and Manfacturing Partfamily only the same class can be added as allowed class
          //i.e. For General Class only General Class can be added.
          if(LibraryCentralCommon.isClassificationType(context, buName)) {
              allowedTypeMap.put(from, LibraryCentralCommon.getDerivativesSymbolicNames(context, buName, true));
          } else if(buName.equals(LibraryCentralConstants.TYPE_PART_LIBRARY)){
              //For Part Library only Part Family is allowed.
              allowedTypeMap.put(from, LibraryCentralCommon.getDerivativesSymbolicNames(context, LibraryCentralConstants.TYPE_PART_FAMILY, true));
          } else if(buName.equals(LibraryCentralConstants.TYPE_LIBRARY)){
              //For Document Library only Document Family is allowed.
              allowedTypeMap.put(from, LibraryCentralCommon.getDerivativesSymbolicNames(context, LibraryCentralConstants.TYPE_DOCUMENT_FAMILY, true));
          }
      }
      return allowedTypeMap;
  }



  /**
   * To restrict connecting any from item on Classified Item relation to have only limited to types
   * e.g
   * Part Family (and its derivates) can have only Part (and its derivatives) as its end items
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no argument
   * @return Map with key = fromside item (symbolic name) on ClassifiedItem relation and value = set (of symbolic names) of types that can be subclass for that from item
   * @throws Exception if the operation fails
   */

  public Map getConditionalAllowedEndItemsInClassifiedItemRelation(Context context, String[] args) throws Exception
  {
      Map relFromTypes = LibraryCentralCommon.getTypesFromRelation(context, LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, true);
      Map allowedTypeMap = new HashMap();
      for (Iterator iter = relFromTypes.keySet().iterator(); iter.hasNext();) {
          String from = (String) iter.next();
          BusinessType bu = (BusinessType) relFromTypes.get(from);
          String buName = bu.getName();
          if(buName.equals(LibraryCentralConstants.TYPE_PART_FAMILY) ||
             mxType.isOfParentType(context, buName, LibraryCentralConstants.TYPE_PART_FAMILY))
          {
              String SUPPLIER_EQUIVALENT_PART   = PropertyUtil.getSchemaProperty(context,"type_SupplierEquivalentPart");
              Set derivativeSymbolicNames       = LibraryCentralCommon.getDerivativesSymbolicNames(context, LibraryCentralConstants.TYPE_PART, true);
              derivativeSymbolicNames.addAll(LibraryCentralCommon.getDerivativesSymbolicNames(context, SUPPLIER_EQUIVALENT_PART, true));
              allowedTypeMap.put(from, derivativeSymbolicNames);
          } else if (buName.equals(LibraryCentralConstants.TYPE_DOCUMENT_FAMILY) ||
                     mxType.isOfParentType(context, buName, LibraryCentralConstants.TYPE_DOCUMENT_FAMILY)) {
              Set derivativeSymbolicNames = LibraryCentralCommon.getDerivativesSymbolicNames(context, LibraryCentralConstants.TYPE_GENERIC_DOCUMENT, true);
              derivativeSymbolicNames.addAll(LibraryCentralCommon.getDerivativesSymbolicNames(context, LibraryCentralConstants.TYPE_DOCUMENT, true));
              //changes for LSA to support Quality System Document
              String type_ControlledDocument=PropertyUtil.getSchemaProperty(context,"type_CONTROLLEDDOCUMENTS");
              derivativeSymbolicNames.addAll(LibraryCentralCommon.getDerivativesSymbolicNames(context, type_ControlledDocument, true));
              allowedTypeMap.put(from, derivativeSymbolicNames);
          } else if (buName.equals(LibraryCentralConstants.TYPE_GENERAL_CLASS) ||
                  mxType.isOfParentType(context, buName, LibraryCentralConstants.TYPE_GENERAL_CLASS)) {
              Map relToTypes = LibraryCentralCommon.getTypesFromRelation(context, LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, false);
              Set toTypeNamesOnRel = relToTypes.keySet();
              toTypeNamesOnRel.removeAll(LibraryCentralCommon.getDerivativesSymbolicNames(context, LibraryCentralConstants.TYPE_DOCUMENT_SHEET, true));
              allowedTypeMap.put(from, toTypeNamesOnRel);
          }

      }
      return allowedTypeMap;
  }


    /**
     * Returns default revision for a given policy
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - policy
     * @return String - the default revision
     * @throws Exception if the operation fails
     */
    public String getRevisionForPolicy(Context context, String[] args)
    throws Exception
    {
        String sRevision  = null;
        String sPolicy    = null;

        try
        {
            Map argMap             = ( Map ) JPO.unpackArgs ( args );
            sPolicy                = (String)argMap.get(LibraryCentralConstants.JPO_ARGS_POLICY );
            matrix.db.Policy policy= new matrix.db.Policy(sPolicy);
            policy.open(context);
            sRevision              = policy.getFirstInSequence();
            policy.close(context);
        }
        catch ( Exception e )
        {
           throw new Exception ();
        }
        return sRevision;
    }

    /**
     * This method checks whether a given type is Document Central Type
     * i.e. Document library/Book shelf/ Book
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - type
     * @return boolean true - if the given type is of Library Central Type
     * @throws Exception if the operation fails
     */
    public boolean isDocumentCentalType (Context context, String args[])
    throws Exception
    {
        String selectedType     = "";

        try
        {
            Map argMap          = (Map) JPO.unpackArgs (args);
            String type         = (String)argMap.get(DomainConstants.SELECT_TYPE);

            if (type != null && !"null".equals(type) && type.length() > 0)
            {
                StringList slTypes  = FrameworkUtil.split(type, ",");
                selectedType        = (String)slTypes.get(0);
                // If a type is selected from type chooser, then the value will have
                // the prefix "selected type"
                if (selectedType.indexOf("selectedType") >= 0)
                {
                    selectedType    = selectedType.substring(selectedType.indexOf(":") + 1);
                }
                else
                {
                    selectedType    = PropertyUtil.getSchemaProperty(context, selectedType);
                }
            } else {
                String objectId         = (String)argMap.get(OBJECT_ID);
                if (!UIUtil.isNullOrEmpty(objectId)) {
                    DomainObject doObj  = new DomainObject(objectId);
                    selectedType = (String)doObj.getInfo(context, DomainObject.SELECT_TYPE);
                }
            }
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }

        return isDocumentCentalType(context,selectedType);
    }


    /**
     * This method checks whether a given type is Document Central Type
     * i.e. Document library/Book shelf/ Book
     *
     * @param context the eMatrix <code>Context</code> object
     * @param type the type
     * @return boolean true - if the given type is of Library Central Type
     * @throws Exception if the operation fails
     */
    public boolean isDocumentCentalType(Context context, String type)
    throws Exception
    {
        return (mxType.isOfParentType(context,type,TYPE_DOCUMENT_FAMILY) ||
                mxType.isOfParentType(context,type,TYPE_DOCUMENT_LIBRARY));
    }

    /**
     * This method checks whether orphan object can be created or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return boolean true - if orphan is allowed
     * @throws Exception if the operation fails
     */
    public boolean isOrphanAllowed(Context context, String[] args)
    throws Exception
    {
        String OrphanSettings   = EnoviaResourceBundle.getProperty(context,"eServiceLibraryCentral.Schema.AllowCreateOrphanObjects");
        return (OrphanSettings != null && !"null".equals(OrphanSettings) && "TRUE".equalsIgnoreCase(OrphanSettings));
    }

    /**
     * This method checks whether orphan object can be created or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return boolean true - if orphan is not allowed
     * @throws Exception if the operation fails
     */
    public boolean isOrpahanNotAllowed(Context context, String[] args)
    throws Exception
    {
        return !isOrphanAllowed(context, args);
    }

    /**
     * This method returns true if orphan object can be created and
     * the given type is DocumentCentralType
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return boolean - true if orphan is allowed & if it DC type
     * @throws Exception if the operation fails
     */

    public boolean isOrphanAllowedAndIsDCType(Context context, String[] args)
    throws Exception
    {
        return(isOrphanAllowed(context,args) && isDocumentCentalType(context, args));
    }

    /**
     * This method returns true if orphan object can be created and
     * the given type is not a DocumentCentralType
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return boolean - true if orphan is allowed & if it is not DC type
     * @throws Exception if the operation fails
     */

    public boolean isOrphanAllowedAndIsNotDCType(Context context, String[] args)
    throws Exception
    {
        return(isOrphanAllowed(context,args) && !isDocumentCentalType(context, args));
    }

    /**
     * This method returns true if orphan object can not be created and
     * the given type is DocumentCentralType
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return boolean - true if orphan is not allowed & if it is DC type
     * @throws Exception if the operation fails
     */

    public boolean isOrphanNotAllowedAndIsDCType(Context context, String[] args)
    throws Exception
    {
        return(!isOrphanAllowed(context,args) && isDocumentCentalType(context, args));
    }



    /**
     * This method returns true if orphan object can not be created and
     * the given type is not a DocumentCentralType
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return boolean - true if orphan is not allowed & if it is not DC type
     * @throws Exception if the operation fails
     */

    public boolean isOrphanNotAllowedAndIsNotDCType(Context context, String[] args)
    throws Exception
    {
        return(!isOrphanAllowed(context,args) && !isDocumentCentalType(context, args));
    }

    /**
     * This method returns Create In Type list for a given type
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - type
     * @return String - Type comma separated.
     * @throws Exception if the operation fails

     */

    public String getCreateInTypes(Context context,String[] args)
    throws Exception
    {
        String type     = "";
        String baseType = "";
        try
        {
            HashMap programMap          = (HashMap) JPO.unpackArgs(args);
            HashMap fieldValuesMap      = (HashMap)programMap.get("fieldValues");
            HashMap allowedParentsMap   = (HashMap)LibraryCentralCommon.getAllowedParentsMap(context);
            type                        = (String)fieldValuesMap.get("TypeActual");
            type                        = FrameworkUtil.getAliasForAdmin (context,"type",type,true);
            baseType                    = ((String)allowedParentsMap.get(type)).trim();

            if(null != baseType && "null".equalsIgnoreCase(baseType)&&  baseType.lastIndexOf(",") == baseType.length())
            {
                baseType = baseType.substring(0,baseType.length()-1);
            }
        } catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }
        return "TYPES="+baseType;
    }

    /**
     * This method returns exclude OIDs (for which user does not have
     * form connect access)
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - field - create In type and it should be like TYPE=Document Library
     * @return StringList - exclude oids.
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getCreateInExcludeOIDs(Context context,String[] args)
    throws Exception
    {
        StringList mlExcludeOIDs    = new StringList();
        try
        {
            HashMap programMap      = (HashMap) JPO.unpackArgs(args);
            String createInType     = (String)programMap.get("field");
            createInType            = createInType.substring(createInType.indexOf("=")+1,createInType.length());
            String whereClause      = "current.access[fromconnect]==\"FALSE\"";
            MapList mapList         = DomainObject.findObjects(context,
                                                    createInType,
                                                    null,
                                                    null,
                                                    null,
                                                    "*",
                                                    whereClause,
                                                    true,
                                                    new StringList(DomainObject.SELECT_ID));

            for (int i = 0; i < mapList.size(); i++) {
                Map map     = (Map) mapList.get(i);
                mlExcludeOIDs.add(map.get(DomainObject.SELECT_ID));
            }
        } catch(Exception ex){
            throw new FrameworkException(ex.toString());
        }

        return mlExcludeOIDs;
    }


    /**
     * This method returns vault list for a given type
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - type
     * @return HashMap - vault list for a given type
     * @throws Exception if the operation fails
     */
    public HashMap getVaultList(Context context, String[] args)
    throws Exception
    {
        HashMap rangeMap            = new HashMap();
        try
        {
            HashMap programMap              = (HashMap)JPO.unpackArgs(args);
            HashMap requestMap              = (HashMap)programMap.get("requestMap");
            String type                     = (String)requestMap.get(DomainConstants.SELECT_TYPE);
            StringList fieldChoices         = new StringList();
            StringList fieldDisplayChoices  = new StringList();
            if (type != null && !"null".equals(type) && type.length() > 0)
            {
                type    = PropertyUtil.getSchemaProperty(context, type);
            }
            String[] vaultsArray            = LibraryCentralCommon.getVaults(context, type);
            for (int i = 0; i < vaultsArray.length ; i++)
            {
                fieldChoices.add(vaultsArray[i]);
                fieldDisplayChoices.add(UINavigatorUtil.getAdminI18NString(JPO_ARGS_VAULT, vaultsArray[i], context.getSession().getLanguage()));
            }
            rangeMap.put("field_choices", fieldChoices);
            rangeMap.put("field_display_choices", fieldDisplayChoices);
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }
        return rangeMap;
    }

    /**
     * This method returns the notification message
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - type
     *        1 - languagestr
     * @return String - notification Message
     * @throws Exception if the operation fails
     */
    public String getNotificationMessage(Context context,String[] args)
    throws Exception
    {
        Map relaodMap       = new HashMap();
        HashMap paramMap    = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap  = (HashMap)paramMap.get("requestMap");
        String type         = (String)requestMap.get(DomainConstants.SELECT_TYPE);
        String languagestr  = (String)requestMap.get(JPO_ARGS_LANGUAGESTR);
        String user         = context.getUser();
        String revision     = "";
        String name         = "";
        if (type != null && !"null".equals(type) && type.indexOf(",") > 0) {
            type            = ((String[])type.split(","))[0];
        }
        if(type.indexOf("_selectedType:") != -1) {
            type = type.substring(type.indexOf(":")+1);
        } else {
            type                = PropertyUtil.getSchemaProperty(context, type);
        }
        if(isReviseMode(context,args)){
            revision                = getObjectRevision(context,args);
            name                    = getObjectName(context, args);
        }  else {
            Map policyMap           = LibraryCentralCommon.getPolicies(context, type);
            String sPolicy          = (String)policyMap.get("default");
            matrix.db.Policy policy = new matrix.db.Policy(sPolicy);
            policy.open(context);
            revision                = policy.getFirstInSequence();
            policy.close(context);
        }
        type                = i18nNow.getTypeI18NString(type,languagestr);
        String [] keys      = new String[] {DomainConstants.SELECT_TYPE,DomainConstants.SELECT_NAME,USER,DomainConstants.SELECT_TYPE};
        String [] values    = new String[] {type,name,user,type };
        String notMessage   = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(languagestr),"emxDocumentCentral.DocumentCentralCommon.NotificationMessage");
        notMessage          = StringResource.format(notMessage, keys, values);

        return notMessage;
    }

    /**
     * This method relaods the notification message
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - TypeActual
     *        1 - Name
     *        2 - Owner
     *        3 - Policy
     * @return Map - notification Message
     * @throws Exception if the operation fails
     */
    public Map reloadNotificationMessage(Context context,String[] args)
    throws Exception
    {
        Map relaodMap       = new HashMap();
        HashMap paramMap    = (HashMap)JPO.unpackArgs(args);
        HashMap fieldValues = (HashMap)paramMap.get("fieldValues");
        HashMap requestMap  = (HashMap)paramMap.get("requestMap");
        String languagestr  = (String)requestMap.get(JPO_ARGS_LANGUAGESTR);
        String name         = (String)fieldValues.get(JPO_ARGS_NAME);
        String user         = (String)fieldValues.get(JPO_ARGS_OWNER);
        String sPolicy      = (String)fieldValues.get(JPO_ARGS_POLICY);
        String type         = (String)fieldValues.get(JPO_ARGS_TYPE_ACTUAL);
        String mode         = (String)requestMap.get("mode");
        String revision     = "";
        if(!UIUtil.isNullOrEmpty(mode) && "copy".equalsIgnoreCase(mode)){
        String objectId     = (String)requestMap.get(OBJECT_ID);
            if(!UIUtil.isNullOrEmpty(objectId)){
                DomainObject domObj     = new DomainObject(objectId);
                sPolicy                 = domObj.getInfo(context , SELECT_POLICY);
            }
        }
        if(!UIUtil.isNullOrEmpty(sPolicy)){
            matrix.db.Policy policy = new matrix.db.Policy(sPolicy);
            policy.open(context);
            revision     = policy.getFirstInSequence();
            policy.close(context);
        }
        type                = i18nNow.getTypeI18NString(type,languagestr);
        String [] keys      = new String[] {DomainConstants.SELECT_TYPE,DomainConstants.SELECT_NAME,USER,DomainConstants.SELECT_TYPE};
        String [] values    = new String[] {type,name,user,type };
        String notMessage   = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(languagestr),"emxDocumentCentral.DocumentCentralCommon.NotificationMessage");
        notMessage          = StringResource.format(notMessage, keys, values);
        relaodMap.put("SelectedValues", notMessage);
        relaodMap.put("SelectedDisplayValues", notMessage);
        return relaodMap;
    }

    /**
     * This method returns the lock status of a given object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     *        1 - launguagestr
     * @return html of lock status
     * @throws Exception if the operation fails
     */
    public String getLockStatus(Context context, String[] args)
    throws Exception
    {
        StringBuffer sbReturnString     = new StringBuffer();
        try
        {
            HashMap programMap      = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap        = (HashMap)programMap.get("paramMap");
            String objectId         = (String)paramMap.get(OBJECT_ID);
            String languagestr      = (String)paramMap.get(JPO_ARGS_LANGUAGESTR);
            DomainObject domainObj  = new DomainObject(objectId);
            StringList selects      = new StringList(2);
            selects.add(DomainObject.SELECT_LOCKED);
            selects.add(DomainObject.SELECT_LOCKER);
            Map resultMap           = domainObj.getInfo(context, selects);
            String locked           = (String)resultMap.get(DomainObject.SELECT_LOCKED);
            String lockedBy         = (String)resultMap.get(DomainObject.SELECT_LOCKER);
            String lockedByMessage  = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(languagestr),"emxDocumentCentral.Common.LockedBy");
            if (locked != null && !"null".equals(locked) && "true".equalsIgnoreCase(locked))
            {
                sbReturnString.append("<img border='0' src='../common/images/iconStatusLocked.gif' alt='");
                sbReturnString.append(lockedByMessage).append(" ").append(XSSUtil.encodeForHTML(context, lockedBy)).append("'>");
                sbReturnString.append("</img>");
            }

        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }
        return sbReturnString.toString();
    }
    /**
     * This method sets the Create In Default value when
     * the form is launched from any parent
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - objectId
     * @return html which sets the create in default values
     * @throws Exception if the operation fails
     */
    public String setCreateInDefaultValue(Context context,String[] args)
    throws Exception
    {
        StringBuffer sbReturnString = new StringBuffer();

        try
        {
            HashMap programMap      = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap      = (HashMap)programMap.get("requestMap");
            String objectId         = (String)requestMap.get(OBJECT_ID);
            String objectName       = "";
            if (objectId != null && !"null".equals(objectId) && objectId.length() > 0) {
                DomainObject dobjParent = new DomainObject(objectId);
                objectName          = (String)dobjParent.getInfo(context, DomainObject.SELECT_NAME);
            }
            sbReturnString.append(" <script language=\"javascript\"> ");
            sbReturnString.append("  document.forms[\'emxCreateForm\'].elements[\'Create In\'].value =\""+XSSUtil.encodeForJavaScript(context,objectName)+"\"; ");
            sbReturnString.append("  document.forms[\'emxCreateForm\'].elements[\'Create InDisplay\'].value =\""+XSSUtil.encodeForJavaScript(context,objectName)+"\"; ");
            sbReturnString.append("  document.forms[\'emxCreateForm\'].elements[\'Create InOID\'].value =\""+XSSUtil.encodeForJavaScript(context,objectId)+"\"; ");
            sbReturnString.append(" </script> ");

        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }
        return sbReturnString.toString();
    }


    /**
     * This method updates Create In Field
     * connects the created object with seleceted container Id
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     *        1 - New OID (i.e value of Create In Field)
     * @throws Exception if the operation fails
     */
    public void updateCreateInField(Context context, String[] args)
    throws Exception
    {
        try
        {
            HashMap programMap              = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap                = (HashMap)programMap.get("paramMap");
            String objectId                 = (String)paramMap.get(OBJECT_ID);
            String containingObjectId       = (String)paramMap.get(JPO_ARGS_NEW_OID);
            String containingRelationship   = LibraryCentralConstants.RELATIONSHIP_SUBCLASS;
            DomainObject dObj               = new DomainObject(objectId);

            StringList slContainingObjIds   = (StringList) (FrameworkUtil.split(containingObjectId, "|"));
            String[] containingObjIdsArray  = (String[]) slContainingObjIds.toArray(new String[slContainingObjIds.size()]);
            addToContainingObject(context,dObj,containingObjIdsArray, containingRelationship);
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.getMessage());
        }
    }

    /**
     * This method updates Create In Field
     * connects the created object with seleceted container Id
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     *        1 - New OID (i.e value of Create In Field)
     * @throws Exception if the operation fails
     */
    public void updateCreateInFieldForGenericDocument(Context context, String[] args)
    throws Exception
    {
        try
        {
            HashMap programMap              = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap                = (HashMap)programMap.get("paramMap");
            String objectId                 = (String)paramMap.get(OBJECT_ID);
            String containingObjectId       = (String)paramMap.get(LibraryCentralConstants.JPO_ARGS_NEW_OID);
            String containingRelationship   = LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM;;
            DomainObject domainObj          = new DomainObject(objectId);
            StringList slContainingObjIds   = (StringList) (FrameworkUtil.split(containingObjectId, "|"));
            String[] containingObjIdsArray  = (String[]) slContainingObjIds.toArray(new String[slContainingObjIds.size()]);
            if  (containingObjIdsArray != null) {
                  for (int i=0; i<containingObjIdsArray.length; i++)
                  {
                      String ContainingObjectId         =  containingObjIdsArray[i];
                      if ( !UIUtil.isNullOrEmpty(ContainingObjectId)){
                          DomainObject dContainingObj   = new DomainObject(ContainingObjectId);
                          String type                   = dContainingObj.getType(context);
                          DomainRelationship.connect (context,dContainingObj,containingRelationship ,domainObj);
                     }
                  } //for : loop through all the containing object IDs
               }

        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.getMessage());
        }
    }


    /**
     * This method updates Folder field
     * connects the created object with selected folder
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     *        1 - New OID (i.e value of fodler Id)
     * @throws Exception if the operation fails

     */
    public void updateFolderField(Context context, String[] args)
    throws Exception
    {
        try
        {
            HashMap programMap      = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap        = (HashMap)programMap.get("paramMap");
            String objectId         = (String)paramMap.get(OBJECT_ID);
            String folderId         = (String)paramMap.get(JPO_ARGS_NEW_OID);
            DomainObject domainObj  = new DomainObject(objectId);
            addToFolder(context, domainObj, folderId);
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }
    }

    /**
     * This method updates notify field
     * Sends the message to selected notify users
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     *        1 - New Value (i.e notify users)
     *        2 - launguagestr
     *        3 - Message
     * @throws Exception if the operation fails
     */
    public void updateNotifyField(Context context, String[] args)
    throws Exception
    {
        try
        {
            HashMap programMap      = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap        = (HashMap)programMap.get("paramMap");
            HashMap requestMap      = (HashMap)programMap.get("requestMap");
            String objectId         = (String)paramMap.get(OBJECT_ID);
            String notifyUsers      = (String)paramMap.get(JPO_ARGS_NEW_VALUE);
            String languagestr      = (String)paramMap.get(JPO_ARGS_LANGUAGESTR);
            String[] messageArray   = (String[])requestMap.get("Message");
            DomainObject domainObj  = new DomainObject(objectId);
            String subject          = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(languagestr),"emxDocumentCentral.Notify.Create.Subject");
            Map specificationMap    = new HashMap();
            String message          = DomainConstants.EMPTY_STRING;

            if (messageArray != null && messageArray.length > 0 )
            {
                message =   messageArray[0];
            }

            specificationMap.put(JPO_SPEC_NOTIFY_SUBJECT, subject);
            specificationMap.put(JPO_SPEC_NOTIFY_MESSAGE, message);
            specificationMap.put (JPO_SPEC_NOTIFY_TO_USERS,notifyUsers );
            StringList objectIdList = new StringList(1);
            objectIdList.add(objectId);
            StringList toList       = FrameworkUtil.split(notifyUsers, "|");
            MailUtil.sendMessage(context, toList, null, null, subject, message, objectIdList);
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }
    }
    /**
     * This method updates Title field If it is Empty with Object Name
     * The updation of Title field is done in updateNotifyField
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @throws Exception if the operation fails
     */
    public void updateTitleField(Context context, String[] args)
    throws Exception
    {
        try
        {
            HashMap programMap       = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap         = (HashMap)programMap.get("paramMap");
            HashMap requestMap       = (HashMap)programMap.get("requestMap");
            String titleName         = PropertyUtil.getSchemaProperty(context, "attribute_Title");
            String title             = ((String[])requestMap.get(titleName))[0];
            String objectId          = (String)paramMap.get(OBJECT_ID);
            DomainObject domainObj   = new DomainObject(objectId);
           if(UIUtil.isNullOrEmpty(title)) {
                title  = domainObj.getName(context);
            }
           domainObj.setAttributeValue(context, titleName, title);
        }

        catch(Exception ex) {
            throw new FrameworkException(ex.toString());
        }
    }
    /**
     * This method updates Message field
     * The updation of Message field is done in updateNotifyField
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @throws Exception if the operation fails
     */
    public void updateMessageField(Context context, String[] args)
    throws Exception
    {
        // do nothing
    }

    /**
     * This method checks whether the user has modify access or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     * @return boolean true - if the user has modify access
     * @throws Exception if the operation fails
     */
    public boolean hasModifyAccess(Context context,String[] args)
    throws Exception
    {

        HashMap paramMap    = (HashMap)JPO.unpackArgs(args);
        String objectId     = (String) paramMap.get(OBJECT_ID);
        DomainObject doObj  = new DomainObject(objectId);
        Access access       = doObj.getAccessMask(context);
        return access.hasModifyAccess();

    }

    /**
     * This method checks whether the user has Revise access or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     * @return boolean true - if the user has revise access
     * @throws Exception if the operation fails
     */
    public boolean hasReviseAccess(Context context,String[] args)
    throws Exception
    {

        HashMap paramMap    = (HashMap)JPO.unpackArgs(args);
        String objectId     = (String) paramMap.get(OBJECT_ID);
        return LibraryCentralCommon.hasReviseAccess(context, objectId);
    }

    /**
     * This method checks whether the user has Lock access or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     * @return boolean true - if the user has lock access
     * @throws Exception if the operation fails
     */
    public boolean hasLockAccess(Context context,String[] args)
    throws Exception
    {

        HashMap paramMap    = (HashMap)JPO.unpackArgs(args);
        String objectId     = (String) paramMap.get(OBJECT_ID);
        DomainObject doObj  = new DomainObject(objectId);
        Access access       = doObj.getAccessMask(context);
        return (isDocumentCentalType(context, args) && access.hasLockAccess());

    }

    /**
     * This method checks whether the user has UnLock access or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     * @return boolean true - if the user has un lock access
     * @throws Exception if the operation fails
     */
    public boolean hasUnLockAccess(Context context,String[] args)
    throws Exception
    {

        HashMap paramMap    = (HashMap)JPO.unpackArgs(args);
        String objectId     = (String) paramMap.get(OBJECT_ID);
        DomainObject doObj  = new DomainObject(objectId);
        Access access       = doObj.getAccessMask(context);
        return (isDocumentCentalType(context, args) && access.hasUnlockAccess());

    }

    /**
     * This method checks whether the user has Read Write access or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     * @return boolean true - if the user has modify access
     * @throws Exception if the operation fails
     */
    public boolean hasReadWriteAccess(Context context,String[] args)
    throws Exception
    {

        HashMap paramMap    = (HashMap)JPO.unpackArgs(args);
        String objectId     = (String) paramMap.get(OBJECT_ID);
        DomainObject doObj  = new DomainObject(objectId);
        Access access       = doObj.getAccessMask(context);
        return (AccessUtil.hasReadWriteAccess(access));

    }

    /**
     * This method creates the Copy of the Objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object ID
     *        1 - vault
     *        2 - name
     *        3 - type
     * @return Map - object ID
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createClone(Context context,String[] args)
    throws Exception
    {
        HashMap requestMap  = (HashMap) JPO.unpackArgs(args);
        Map returnMap       = new HashMap();
        try {
            Libraries lib   = new Libraries();
            String objectId = lib.cloneLBCObject(context, requestMap);
            returnMap.put("id", objectId);

        } catch (Exception e) {
            throw new FrameworkException(e);
        }
        return (returnMap);
    }

    /**
     * This method creates the Revision of the Objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object ID
     *        1 - vault
     *        2 - name
     *        3 - type
     * @return Map - object ID
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createRevision(Context context,String[] args)
    throws Exception
    {
       HashMap requestMap   = (HashMap) JPO.unpackArgs(args);
        Map returnMap       = new HashMap();
        try {
            Libraries lib   = new Libraries();
            String objectId = lib.reviseLBCObject(context, requestMap);
            returnMap.put("id", objectId);

        } catch (Exception e) {
            throw new FrameworkException(e);
        }
        return (returnMap);
    }

    /**
     * This method creates the List of Folder Objects To be Displayed in Add To Folder Option
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return StringList - Folders
     * @throws Exception if the operation fails
     */

    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getFolders(Context context,String[] args)
    throws Exception
    {
        HashMap mapResults      = new HashMap();
        HashMap paramMap        = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap      = (HashMap)paramMap.get("RequestValuesMap");
        StringList strList      = new StringList(8);
        String globalRead       = PropertyUtil.getSchemaProperty(context,"attribute_GlobalRead");
        String objectOwner      = context.getUser();
        strList.add(DomainConstants.SELECT_ID);
        StringBuffer where      = new StringBuffer(DomainConstants.EMPTY_STRING);
        WorkspaceVault myVault  = new WorkspaceVault();
        where.append("((("+DomainConstants.SELECT_OWNER+"==\""+objectOwner+"\") || ");
        where.append("(attribute["+globalRead+"]==\"True\")) && ");
        where.append("(current.access[fromconnect]==\"True\"))");
        MapList mpList          = myVault.findObjects(context,PropertyUtil.getSchemaProperty(context,"type_ProjectVault"),"*","","*","*",where.toString(),false,strList);
        StringList filterdList  = new StringList();
        Iterator it             = mpList.iterator();
        while(it.hasNext()){
            Map map=(Map)it.next();
            String id =(String)map.get(DomainConstants.SELECT_ID);
            filterdList.add(id);
        }
        return filterdList;
    }


    /**
     * This method checks if the Object has Connection directly or indirectly with WorkSpace Vault
     * @param context the eMatrix <code>Context</code> object
     * @param id holds the Object Id
     * @return boolean
     * @throws Exception if the operation fails
     */

    public boolean isDCFolder(Context context, String id)
    throws Exception {
      boolean retVal     = false;
      DomainObject dmObj = DomainObject.newInstance(context,id);
      String vaultId     =(String)dmObj.getInfo(context,"to["+DomainObject.RELATIONSHIP_WORKSPACE_VAULTS +"].from.id" );
      if(vaultId==null){
          //Not connected with Workspace or Project but may be connected with other
          //workspacevault which may be connected with Workspace or project
          vaultId=(String)dmObj.getInfo(context,"to["+DomainObject.RELATIONSHIP_SUB_VAULTS +"].from.id" );
          if(vaultId==null){
              retVal=true;
          }
          else{
              retVal = isDCFolder(context,vaultId);
          }
      }
      return retVal;
   }

    /**
     * This method gets the attribute Range value for Gloabal read and returns the Corresponding Value ad the Display Value
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return Map - Global Read Value  Range and Display Value Range
     * @throws Exception if the operation fails
     */

    public HashMap getFolderSecurityOptions(Context context, String[] args)
    throws Exception
    {
        HashMap rangeMap            = new HashMap();
        try
        {
            StringList fieldChoices         = new StringList();
            StringList fieldDisplayChoices  = new StringList();
            fieldChoices.add("True");
            fieldDisplayChoices.add(EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxDocumentCentral.CreateFolder.GlobalReadAccess"));
            fieldChoices.add("False");
            fieldDisplayChoices.add(EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxDocumentCentral.CreateFolder.Private"));
            rangeMap.put("field_choices", fieldChoices);
            rangeMap.put("field_display_choices", fieldDisplayChoices);
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }
        return rangeMap;
    }

    /**
     * This method gets the attribute value for Gloabal read and returns the Corresponding String for Display
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object ID
     *        1 - languageStr
     * @return String - Global Read Value
     * @throws Exception if the operation fails
     */

    public static String  getFolderSecurityValue(Context context, String[] args)
    throws Exception
    {

        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap            = (HashMap) programMap.get("paramMap");
        String objectId             = (String)paramMap.get(OBJECT_ID);
        String languageStr          = (String) paramMap.get("languageStr");
        DomainObject domObject      = new DomainObject(objectId);
        String attrGlobalReadValue  = domObject.getAttributeValue(context, ATTRIBUTE_GLOBAL_READ);

        if(attrGlobalReadValue!= null && !"null".equalsIgnoreCase(attrGlobalReadValue) && attrGlobalReadValue.equalsIgnoreCase("True"))
        {
            attrGlobalReadValue = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxDocumentCentral.CreateFolder.Global");
        } else {
            attrGlobalReadValue = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxDocumentCentral.CreateFolder.Private");
        }

        return attrGlobalReadValue;
    }

    /**
     * This method checks is the Form is used in Edit Mode
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - mode
     * @return boolean - Edit Mode
     * @throws Exception if the operation fails
     */
    public static boolean isEditMode(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String mode        = (String)programMap.get("mode");
        return (null!= mode && !"null".equalsIgnoreCase(mode) && "edit".equalsIgnoreCase(mode));
    }

    /**
     * This method checks is the Form is used in View Mode
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - mode
     * @return boolean - View Mode
     * @throws Exception if the operation fails
     */

    public static boolean isViewMode(Context context, String[] args)
    throws Exception
    {
        return(!isEditMode(context,args));
    }


    /**
     * This method checks if the Page is in  Design Sync Selector
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - ObjectId
     * @return boolean - isVersionable
     * @throws Exception if the operation fails
     */
    public static String getVCSelector(Context context, String[] args)
    throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap            = (HashMap) programMap.get("paramMap");
        String objectId             = (String)paramMap.get(OBJECT_ID);
        DomainObject dObject    = new DomainObject(objectId);
        StringList selectList   = new StringList(2);
        selectList.add("vcfile");
        selectList.add("vcfolder");
        selectList.add("vcfile.specifier");
        selectList.add("vcfolder.config");
        Map objectDetailsMap    = dObject.getInfo(context,selectList);
        String vcfile           = (String)objectDetailsMap.get("vcfile");
        String vcfolder         = (String)objectDetailsMap.get("vcfolder");
        return("true".equalsIgnoreCase(vcfile)?(String)objectDetailsMap.get("vcfile[1].specifier"):(String)objectDetailsMap.get("vcfolder[1].config"));
    }

    /**
     * This method checks if the Page is in  Design Sync Server
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - ObjectId
     * @return boolean - isVersionable
     * @throws Exception if the operation fails
     */
    public static String getVCServer(Context context, String[] args)
    throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap            = (HashMap) programMap.get("paramMap");
        String objectId             = (String)paramMap.get(OBJECT_ID);
        DomainObject dObject = new DomainObject(objectId);
        StringList selectList = new StringList(2);
        selectList.add("vcfile");
        selectList.add("vcfolder");
        selectList.add("vcfile.store");
        selectList.add("vcfolder.store");
        Map objectDetailsMap  = dObject.getInfo(context,selectList);
        String vcfile = (String)objectDetailsMap.get("vcfile");
        String vcfolder = (String)objectDetailsMap.get("vcfolder");
        return("true".equalsIgnoreCase(vcfile)?(String)objectDetailsMap.get("vcfile[1].store"):(String)objectDetailsMap.get("vcfolder[1].store"));
    }

    /**
     * This method checks if the Page is in  Design Sync Path
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - ObjectId
     * @return boolean - isVersionable
     * @throws Exception if the operation fails
     */
    public static String getVCPath(Context context, String[] args)
    throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap            = (HashMap) programMap.get("paramMap");
        String objectId             = (String)paramMap.get(OBJECT_ID);
        DomainObject dObject    = new DomainObject(objectId);
        StringList selectList   = new StringList(2);
        selectList.add("vcfile");
        selectList.add("vcfolder");
        selectList.add("vcfolder.path");
        selectList.add("vcfile.path");
        Map objectDetailsMap    = dObject.getInfo(context,selectList);
        String vcfile           = (String)objectDetailsMap.get("vcfile");
        String vcfolder         = (String)objectDetailsMap.get("vcfolder");
        String path             = "";
        if ("true".equalsIgnoreCase(vcfile)) {
            String tmpPath  = (String)objectDetailsMap.get("vcfile[1].path");
            int index       = tmpPath.lastIndexOf('/');
            if (index < 0) {
                path        = java.io.File.separator;
            } else {
                path        = tmpPath.substring(0, index);
            }
        } else {
            path            = (String)objectDetailsMap.get("vcfolder[1].path");
        }
        return(path);
    }

    /**
     * This method Connects the Newly Created Document Sheet to Generic Document
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - ObjectId
     *        1 - newObjectId
     * @return boolean - isVersionable
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public static void connectDocumentSheetToGenericDocument(Context context, String[] args)
    throws Exception
    {
        try {
            HashMap programMap          = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap            = (HashMap) programMap.get("paramMap");
            HashMap requestMap          = (HashMap) programMap.get("requestMap");
            String objectId             = (String)paramMap.get("newObjectId");
            String parentObjectId       = (String)requestMap.get(OBJECT_ID);
            if (!UIUtil.isNullOrEmpty(objectId)&&!UIUtil.isNullOrEmpty(parentObjectId)){
                DomainObject dNewObject     = new DomainObject(objectId);
                DomainObject dparentObject  = new DomainObject(parentObjectId);
                DomainRelationship.connect (context,dparentObject, LibraryCentralConstants.RELATIONSHIP_DOCUMENT_SHEETS ,dNewObject);
            }
        }
        catch (Exception ex) {
            throw new FrameworkException(ex.toString());
        }
    }

    /**
     * To create a Library Central object like Library, Class etc..
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *      0 - requestMap
     * @return Map contains created objectId
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createLBCObject(Context context, String[] args)
    throws Exception {

        HashMap requestMap  = (HashMap) JPO.unpackArgs(args);
        Map returnMap       = new HashMap();

        try {
            Libraries lib   = new Libraries();
            String objectId = lib.createLBCObject(context, requestMap);
            returnMap.put("id", objectId);

        } catch (Exception e) {
            throw new FrameworkException(e);
        }

        return returnMap;
    }


    /**
     * This method returns The next Revision Sequence name while Creating Revisions
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - ObjectId
     * @return boolean - isVersionable
     * @throws Exception if the operation fails
     */
    public String getObjectRevision(Context context, String[] args)
    throws Exception
    {
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("requestMap");
        String objectId         = (String)paramMap.get(OBJECT_ID);
        DomainObject dObject    = new DomainObject(objectId);
        return(dObject.getNextSequence(context));
    }

    /**
     * This method returns The Object name while Creating Revisions
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - ObjectId
     * @return boolean - isVersionable
     * @throws Exception if the operation fails
     */
    public String getObjectName(Context context, String[] args)
    throws Exception
    {
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("requestMap");
        String objectId         = (String)paramMap.get(OBJECT_ID);
        DomainObject dObject    = new DomainObject(objectId);
        return (dObject.getInfo(context,DomainObject.ATTRIBUTE_DETAILS_NAME));
    }
    /**
     * This method checks is the Form is used in Revise Mode
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - mode
     * @return boolean - Revise Mode
     * @throws Exception if the operation fails
     */
    public boolean isReviseMode(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String mode        = (String)programMap.get("mode");
        if (UIUtil.isNullOrEmpty(mode)) {
            HashMap requestMap = (HashMap)programMap.get("requestMap");
            mode        = (String)requestMap.get("mode");
        }
        return (!UIUtil.isNullOrEmpty(mode) && "revise".equalsIgnoreCase(mode));
    }

    /**
     * This method checks is the Form is Not Used in Revise Mode
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - mode
     * @return boolean - Is Not Revise Mode
     * @throws Exception if the operation fails
     */

    public boolean isNotReviseMode(Context context, String[] args)
    throws Exception
    {
        return(!isReviseMode(context,args));
    }

  /**
   * To get the derivatives if Type Classification
   * @param context
   * @param args holds no arguments
   * @return StringList with the Classification types
   * @throws Exception
   */

  public static StringList getAllowedClasificationTypes (Context context, String[] args)
  throws FrameworkException
  {
      StringList slAllowedClasificationTypes    = new  StringList();
      String sCmd                               = "print type  $1 select derivative dump";
      String sResult                            = MqlUtil.mqlCommand(context, sCmd, LibraryCentralConstants.TYPE_CLASSIFICATION);
      StringTokenizer stDerivatives             = new StringTokenizer(sResult, ",");
      while(stDerivatives.hasMoreTokens()) {
          slAllowedClasificationTypes.add(stDerivatives.nextToken());
      }
      return slAllowedClasificationTypes;
  }

    /**
     * This method sends object modify notification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        1 - object Is
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void sendObjectModifyNotification (Context context, String args[])
    throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap          = (HashMap) programMap.get("requestMap");
        String objectId             = (String) requestMap.get("objectId");

        DomainObject domObj = new DCWorkspaceVault(objectId);
        if(domObj != null && domObj.getInfo(context,DomainConstants.SELECT_TYPE).equals(TYPE_WORKSPACE_VAULT)){
            sendEventIconMail (context,
                                  EventConstants.EVENT_FOLDER_MODIFIED,
                                  (Subscribable)domObj,
                                  objectId);
        }
    }


    /** Method for connecting Contents to a folder.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *    contentIDs          - contentIds
    *    folderId          - folder to connect to
    * @throws Exception if a major failure occurs
    */
   public void updateAddContentField ( Context context , String[] args )
           throws Exception
   {
      try
      {
    	  Map specificationMap           = (Map)JPO.unpackArgs(args);
    	  Map paramMap                   = (Map)specificationMap.get("paramMap");
    	  Map requestMap                 = (Map)specificationMap.get("requestMap");
    	  String folderId                = (String)paramMap.get(LibraryCentralConstants.OBJECT_ID);
    	  String contentId               = (String)paramMap.get("New OID");
    	  StringList slContentids        = new StringList();
    	  String objectsNotAdded		 = "";
    	  boolean popUpFlag				 = false;
    	  if (!UIUtil.isNullOrEmpty(contentId)){
    		  WorkspaceVault workspaceVault  = new WorkspaceVault();
    		  workspaceVault.setId(folderId);
    		  workspaceVault.setContentRelationshipType (PropertyUtil.getSchemaProperty (context,"relationship_VaultedDocumentsRev2" ) );
    		  StringTokenizer stContents     = new StringTokenizer ( contentId , "|" );
    		  String strMqlCmd           = "print bus $1 select $2 dump";
    		  while ( stContents.hasMoreTokens ()){
    			  String objId = stContents.nextToken ();
    			  String strQueryResult      = MqlUtil.mqlCommand(context,strMqlCmd, objId, "current.access[toconnect]");
    			  if(strQueryResult.equalsIgnoreCase("true"))
    			  {
    				  slContentids.add(objId);
    			  }
    			  else
    			  {
    				  popUpFlag = true;
    				  DomainObject tempobj=new DomainObject(objId);
    				  String sName = tempobj.getInfo(context,DomainObject.SELECT_NAME);
    				  if(objectsNotAdded.length() > 0)
    				  {
    					  objectsNotAdded += ", "+sName;
    				  }
    				  else
    				  {
    					  objectsNotAdded=sName;
    				  }
    			  }
    		  }
    		  if(!popUpFlag)
    		  {
    			  workspaceVault.addItems(context,(String[])slContentids.toArray(new String[slContentids.size()]));
    		  }
    	  }
    	  if(popUpFlag)
    	  {
    		  throw new FrameworkException("No Access to object(s)  "+objectsNotAdded+". Transaction Aborted");
    	  }
      }
      catch(FrameworkException fe)
      {
    	  throw fe;
      }
      catch ( Exception e )
      {
         // missing argument key or bad cast

         throw new IllegalArgumentException ();
      }

   }

    /** Method for displaying the policies for a given type.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    type                -  type
     * @throws Exception if a major failure occurs
    */
    public HashMap getPolicies ( Context context , String[] args )
    throws Exception
    {
        HashMap hmPolicyMap = new HashMap();
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap  = (HashMap) programMap.get("requestMap");
        String typeString   = (String) requestMap.get("type");
        if (!UIUtil.isNullOrEmpty(typeString) && typeString.indexOf(",") > 0) {
            typeString            = ((String[])typeString.split(","))[0];
        }
        if(typeString.startsWith("type_")){
            typeString = PropertyUtil.getSchemaProperty(context, typeString);
        }else if(typeString.startsWith("_selectedType")){
            typeString = typeString.substring(typeString.indexOf(':')+1);
        }

        BusinessType boType     = new BusinessType(typeString, context.getVault());
        PolicyList allPolicyList= boType.getPoliciesForPerson(context, false);
        Map policyInfo          = mxType.getDefaultPolicy(context, typeString, false);
        String defaultPolicy    = "";
        if (policyInfo != null) {
            defaultPolicy       = (String) policyInfo.get("name");
        }
        PolicyItr policyItr     = new PolicyItr(allPolicyList);

        String languageStr      = context.getSession().getLanguage();
        Policy policyValue      = null;
        String policyName       = "";
        StringList display      = new StringList();
        StringList actualVal    = new StringList();
        String TYPE_MANUFACTURING_PART_FAMILY = PropertyUtil.getSchemaProperty(context, "type_ManufacturingPartFamily");
        String POLICY_PARTFAMILY= PropertyUtil.getSchemaProperty(context, "policy_PartFamily");
        while (policyItr.next()) {
            policyValue         = (Policy) policyItr.obj();
            policyName          = policyValue.getName();
            if((typeString.equals(TYPE_LIBRARY) && policyName.equals(POLICY_LIBRARIES)) ||
               (typeString.equals(TYPE_DOCUMENT_FAMILY) && policyName.equals(LibraryCentralConstants.POLICY_CLASSIFICATION)) ||
               (typeString.equals(TYPE_MANUFACTURING_PART_FAMILY) && policyName.equals(POLICY_PARTFAMILY)) ||
               (typeString.equals(TYPE_GENERIC_DOCUMENT) && policyName.equals(POLICY_DOCUMENT))) {
                continue;
            }
            display.addElement(i18nNow.getAdminI18NString("Policy",policyName, languageStr));
            actualVal.addElement(policyName);
        }
        int position    = actualVal.indexOf(defaultPolicy);
        if (position > 0) {
            String positionDisplay  = (String) display.get(position);
            String positionActual   = (String) actualVal.get(position);
            display.setElementAt(display.get(0), position);
            actualVal.setElementAt(actualVal.get(0), position);
            display.setElementAt(positionDisplay, 0);
            actualVal.setElementAt(positionActual, 0);
        }

        hmPolicyMap.put("field_choices", actualVal);
        hmPolicyMap.put("field_display_choices", display);
        return hmPolicyMap;
    }
    /***
     * Trigger method, which would be called for every create of Subclass relationship.
     * During the connection, child Objects revision would be compared with the parent objects physicalid,
     * if its same, then the revision would not be changed to physicalid of the parent. Otherwise it would
     * be changed. During creation of object, if there exists a parent, the object revision would be changed
     * to physicalid of the parent,if no parent exist, the revision would not be changed.
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public int allowDuplicateNames(Context context,String args[])throws FrameworkException{
        try{
            String[] constructor = {null};
            String childObjectId=args[1];
            String parentObjectId=args[0];
            Map aMap = new HashMap();
            aMap.put ("objectId", parentObjectId);
            aMap.put ("relationship", args[2]);
            String objectName=new DomainObject(childObjectId).getInfo(context,DomainConstants.SELECT_NAME);
            String objectRevision=new DomainObject(childObjectId).getInfo(context,DomainConstants.SELECT_REVISION);
            String parentPhysicalId=LibraryCentralCommon.getPhysicalIdforObject(context,parentObjectId);
            if(!(objectRevision.equals(parentPhysicalId))){
                changeRevisiontoPhysicalId(context,childObjectId,objectName,parentPhysicalId);
            }
            return 0;
        }catch(Exception err){
            err.printStackTrace();
            return 1;
        }

    }
    /***
     * Changes the revision of the object to the PhysicalId of the parent object. Since changing
     * the revision alone is not permitted, we are changing the name of the object also with a "~"
     * and reverting back to the original name.
     * @param context the eMatrix <code>Context</code> object
     * @param childId, the objectId whose revision needs to be changed to PhysicalId
     * @param objectName, the name of the object
     * @param parentPhysicalId, physicalId of the parent Object
     * @throws FrameworkException,if the operation fails
     */
    public void changeRevisiontoPhysicalId(Context context, String childId,String objectName,String parentPhysicalId) throws FrameworkException,Exception{
        String errorMessage=EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.AddExisting.Error");
        try{
            String mqlQuery="modify bus $1 revision $2 name $3 ";
            ContextUtil.pushContext(context);
            MqlUtil.mqlCommand(context, mqlQuery, childId,parentPhysicalId,objectName);
            ContextUtil.popContext(context);
        }catch(FrameworkException err){
        	String mqlerrorQuery ="error $1 $2 $3";
        	MqlUtil.mqlCommand(context, mqlerrorQuery, errorMessage, objectName, new DomainObject(childId).getInfo(context, SELECT_TYPE));
            err.printStackTrace();
        }
    }
/**
 * Show export command in properties page for type Libraries only.
 * @param context the eMatrix <code>Context</code> object
 * @param args argsMap
 * @return boolean
 * @throws FrameworkException,if the operation fails
 * @throws Exception,if the operation fails
 */
    public boolean showExportforLibraries(Context context,String[] args )throws FrameworkException,Exception{
        HashMap argsMap=(HashMap)JPO.unpackArgs(args);
        return (new DomainObject((String)argsMap.get(OBJECT_ID)).isKindOf(context, TYPE_LIBRARIES));
    }

    /**
     * This method returns the vault associated with the context's Person
     * this is temporarily added to replace ${CLASS:emxDocumentCentralRootBase}:systemVaults()
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String[] getVaults(Context context, String[] args) throws Exception{
    	Company company = Person.getPerson(context).getCompany(context);
    	String strDefaultVault = (new DomainObject().getDefaultVault(context,company)).toString();
        String[] vaults = {strDefaultVault};
    	return vaults;
    }
   /***
    *  This method return all the Subclasses for a Library. Used during classify or Reclassify
    *  on End Items
    * @param context
    * @param args
    * @return
    * @throws FrameworkException
    */

    public String getAllSubClassforLibrary(Context context,String args[]) throws FrameworkException{
    	String res = new String();
    	try{

    	        StringList objectSelects = new StringList(6);
    	        objectSelects.addElement("id");
    	        objectSelects.addElement("type");
    	        objectSelects.addElement("name");
    	        objectSelects.addElement("revision");
    	        StringList relselects = new StringList();
    	        String objectId=args[0];
    	        DomainObject domObj = new DomainObject(objectId);
    	        MapList mList = null;
    	        mList = domObj.getRelatedObjects(context,
    	        		LibraryCentralConstants.RELATIONSHIP_SUBCLASS,
    	                "*",
    	                objectSelects,
    	                relselects,
    	                true,//boolean getTo,
    	                false,//boolean getFrom,
    	                (short)0,
    	                null,
    	                null,
    	                0);
    	        if(mList.size() > 0){
	    	        Map lastMap = (Map)mList.get(mList.size()-1);
	    	        res = (String)lastMap.get("id");
	    	     }
    	}catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
		}
        return res;
    }

    /**
     *  This method checks if the Document Object is locked
     *  Used as check trigger in Generic Document lifecycle changes
     *
     *  @param context the eMatrix <code>Context</code> object
     *  @param args an array of String arguments for this method
     *         0 - objectId, the id of this Generic Document
     *  @return either 0 or exception
     *  @throws Exception if the operation fails
     */
    public int isLocked(Context context, String[] args)throws Exception{
    	try{
    		String objectId = args[0];
            String rel_ActiveVersion = PropertyUtil.getSchemaProperty(context, "relationship_ActiveVersion");
    		DomainObject domainObj  = new DomainObject(objectId);
            StringList objectSelects      = new StringList(2);
            objectSelects.add(DomainObject.SELECT_LOCKED);
            objectSelects.add("attribute[Title].value");
            MapList activeVersionsList = domainObj.getRelatedObjects(context,
            		rel_ActiveVersion,
                    "*",
                    objectSelects,
                    null,
                    false,
                    true,
                    new Short("1"),
                    "locked == true",
                    "",
                    0);
            if (activeVersionsList != null && activeVersionsList.size() != 0)
            {
            	StringBuffer lockedFileNames = new StringBuffer();
            	Iterator itr = activeVersionsList.iterator();

            	while(itr.hasNext()){
            		Map lockedActiveVersion = (Map)itr.next();
            		String lockedFileName   = (String)lockedActiveVersion.get("attribute[Title].value");
            		lockedFileNames.append("\\n");
            		lockedFileNames.append(lockedFileName);
            	}

    			String errorString   = "\\n"+EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxLibraryCentral.Document.FilesLockedMsg");
    			errorString         += lockedFileNames.toString();

    			throw (new MatrixException( errorString ) );
    		}
    		else
    		{
    			return 0;
    		}

    	}
    	catch (Exception e)
    	{
    		throw new MatrixException (e.toString());
    	}



    }
   /***
    * This is a dummy Update Function used for Displaying Classification Attributes during Create
    * @param context
    * @param args
    * @throws Exception
    */
    public void dummyUpdateFunction(Context context, String[] args)
    throws Exception
    {
        // do nothing
    }
    /***
     * This Function returns True if ENG is installed, otherwise False
     * @param context
     * @param args
     * @throws Exception
     */
     public Boolean isENGInstalled(Context context,String[] args){
         boolean engInstalled= FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMEngineering",false,null,null);
         return engInstalled;
     }
     /** Invalidates VPLM Cache, API provided by VPLM Catalog Team
 	 * @param context
 	 * @throws Exception
 	 * @throws MatrixException
 	 */
 	private void invalidateVPLMCatalogueCache(Context context)
 			throws FrameworkException {
 		try{
             String mqlString="list program $1";
             String output=MqlUtil.mqlCommand(context, mqlString,"emxPLMDictionaryProgram");
             if(UIUtil.isNotNullAndNotEmpty(output)){
                 Map argsHash = new HashMap();
                 String[] packArgs = JPO.packArgs(argsHash);
                 JPO.invoke(context, "emxPLMDictionaryProgram",null,"invalidateCache",packArgs,Integer.class);
			}
         }catch(Exception e){
         	try {
 				throw (new MatrixException (e));
 			} catch (MatrixException e1) {
 				throw new FrameworkException(e1);
 			}
         }
 	}

 	 /**
     * Checks if the context user has the Library User or similar role or not.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap
     * @return Boolean depending on if the context user has the Library User or similar role or not.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */
    public Boolean hasLibraryViewerRole (Context context, String[] args)
        throws Exception
    {
        matrix.db.Person personObj = new matrix.db.Person(context.getUser());
        // Check if the person is assigned the Library user role
        boolean isLibraryUser = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_LibraryUser"));
        boolean isAuthor = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_Author"));
        boolean isLimitedAuthor = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_LimitedAuthor"));
        boolean isReleaseManager = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_ReleaseManager"));
        boolean isReviewer = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_Reviewer"));
        boolean isVPLMAdmin = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_VPLMAdmin"));
        boolean isVPLMViewer = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_VPLMViewer"));
               	
        return Boolean.valueOf( isLibraryUser || isAuthor || isLimitedAuthor || isReleaseManager || isReviewer || isVPLMAdmin || isVPLMViewer );
    }
    
    /**
     * Checks if the context user has the Record Retention Manager Role.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap
     * @return Boolean depending on if the context user has the Record Retention Manager Role.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */  
    public Boolean hasRetentionRecordRole (Context context, String[] args)
            throws Exception
        {
            matrix.db.Person personObj = new matrix.db.Person(context.getUser());
            // Check if the person is assigned the Record Retention Manager Role.
            boolean isRententionUser = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_RecordRetentionManager"));
                   	
            return Boolean.valueOf( isRententionUser);
        }
    
     /**
      * This Method returns the field parameter to emxFullSearch.jsp to choose Valid Approvers
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
     public String getApproverRoles(Context context,String args[]) throws Exception {
         String dyanmicQuery = "USERROLE="+DocumentCentralConstants.APPROVER_ROLES;
         return dyanmicQuery;
     }


     /**	RESTful Web Service Method
      *
      * @param context	:
      * @param args		:	contains HashMap Object which in turn has either a Library or Class Info
      *
      * @return physical id of the newly created object
      * @throws Exception
      */
     public String createLBCObjectUsingREST(Context context, String[] args) throws Exception
     {
         try
         {
          	String physicalId ="";
          	String policy = "";
         	String type = "";
         	String name = "";
         	String revision = "";
         	String vault = "";

         	HashMap<String,String> objMap = (HashMap) JPO.unpackArgs(args);

         	LibraryCentralCommon obj = new Libraries();
         	if(obj.isKindof(context,objMap.get(LibraryCentralConstants.TYPE_ATTRIBUTE),LibraryCentralConstants.TYPE_LIBRARIES))
          	{
          		type = objMap.get(LibraryCentralConstants.TYPE_ATTRIBUTE);
          		name = objMap.get(LibraryCentralConstants.NAME);
          		vault = objMap.get(LibraryCentralConstants.VAULT);

          		if(obj.isKindof(context,type,LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY))
             	{
             		policy		= getDefaultPolicy(context, LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY);
             		revision	= getDefaultRevision(context, policy);
             	}
             	else
             	{
             		policy		= getDefaultPolicy(context, LibraryCentralConstants.TYPE_LIBRARIES);
             		revision	= getDefaultRevision(context, policy);
             	}
          	}
          	else if(obj.isKindof(context,objMap.get(LibraryCentralConstants.TYPE_ATTRIBUTE),LibraryCentralConstants.TYPE_CLASSIFICATION))
          	{
          		type = objMap.get(LibraryCentralConstants.TYPE_ATTRIBUTE);
          		name = objMap.get(LibraryCentralConstants.NAME);
          		vault = objMap.get(LibraryCentralConstants.VAULT);

          		if(obj.isKindof(context,type,LibraryCentralConstants.TYPE_DOCUMENT_FAMILY))
             	{
             		policy		= getDefaultPolicy(context, LibraryCentralConstants.TYPE_DOCUMENT_FAMILY);
             		revision	= getDefaultRevision(context, policy);
             	}
             	else
             	{
             		policy		= getDefaultPolicy(context, LibraryCentralConstants.TYPE_CLASSIFICATION);
             		revision	= getDefaultRevision(context, policy);
             	}
          	}

         	//create a new object given information
         	String appName 			= FrameworkUtil.getTypeApplicationName(context, type);
         	DomainObject createBO 	= DomainObject.newInstance(context, type, appName);
         	BusinessObject bo = new BusinessObject(type, name, revision, vault);
         	if(!bo.exists(context))
         	{
         		createBO.createObject(context, type, name, revision, policy, vault);
         		String objectId 			= createBO.getObjectId(context);
         		DomainObject domObj = new DomainObject(objectId);
         		physicalId = domObj.getInfo(context, LibraryCentralConstants.SELECT_PHYSICALID);
         	}
         	else
         	{
             	throw new MatrixException("Creation of Object Failed");
            }

         	return physicalId;
         }
         catch (Exception e)
         {
             throw new FrameworkException(e);
         }
     }


     /**	RESTful Web Service Method
      *
      * @param context	:
      * @param args		:	contains Object ID
      *
      * @return
      * @throws Exception
      */
     public void updateLBCObject (Context context, String[] args) throws Exception
     {
         try
         {
        	 HashMap<String,String> libMap = JPO.unpackArgs(args);

        	 if(null != libMap.get(LibraryCentralConstants.SELECT_ID) && !libMap.get(LibraryCentralConstants.SELECT_ID).isEmpty())
        	 {
        		 DomainObject obj = new DomainObject(libMap.get(LibraryCentralConstants.SELECT_ID));

        		 if(null != libMap.get(LibraryCentralConstants.SELECT_OWNER))
        		 {
        			 /*Person person = new Person();
        			 boolean exists = person.doesPersonExists(context, libMap.get("owner"));
        			 if(exists)
        			 {*/
        				 obj.setOwner(context, libMap.get(LibraryCentralConstants.SELECT_OWNER));
        			 //}
        		 }
        		 if(null != libMap.get(LibraryCentralConstants.NAME))
        		 {
        			 obj.setName(context, libMap.get(LibraryCentralConstants.NAME));
        		 }
        		 if(null != libMap.get(LibraryCentralConstants.TITLE))
        		 {
        			 obj.setAttributeValue(context, LibraryCentralConstants.SELECT_TITLE, libMap.get(LibraryCentralConstants.TITLE));
        		 }
        		 if(null != libMap.get(LibraryCentralConstants.DESCRIPTION_ATTRIBUTE))
        		 {
        			 obj.setDescription(context, libMap.get(LibraryCentralConstants.DESCRIPTION_ATTRIBUTE));
        		 }
        	 }
        	 else
        	 {
        		 throw new MatrixException("Object ID not specified");
        	 }
         }
         catch(Exception exp)
         {
             throw exp;
         }
     }

     /**	RESTful Web Service Method
      *
      * @param context	:
      * @param ids		:	contains Object IDs
      *
      * @return			:	"true" Or "false" depending upon Success OR Failure respectively
      * @throws Exception
      */
     public String deleteLBCObjects(Context  context,String[] ids) throws Exception
     {
    	 try
    	 {
    		 String result = "";

    		 for(String id : ids)
    		 {
    			 Map<String, String> idMap = new HashMap<String, String>();
    			 idMap.put(LibraryCentralConstants.SELECT_ID, id);
    			 String[] packedArgs = JPO.packArgs(idMap);
    			 result = deleteLCObjects(context, packedArgs);
    		 }

    		 if(result.isEmpty() || result.equals(""))
    		 {
    			 result = "true";
    			 return result;
    		 }
    		 else
    		 {
    			 result = "false";
    			 return result;
    		 }
    	 }
    	 catch (Exception e)
         {
             throw new FrameworkException(e);
         }
     }

   //test for allowed end item map
     public static int checkAllowedItem(Context context, String[] args) throws Exception
 	{
 	    int strResult = 1;
 	    try{
 	    if(args.length > 0)
 	    {
 	        String strToObjectId = args[1];
 	        String strFromObjectId = args[0];
 	        String relationName = args[2];
 	        Map alloweditem = new HashMap();
 	        DomainObject toobj = new DomainObject(strToObjectId);
 	        DomainObject fromobj = new DomainObject(strFromObjectId);
 	        String totype = UICache.getSymbolicName(context, toobj.getInfo(context, DomainConstants.SELECT_TYPE), "type");
 	        String fromtype = UICache.getSymbolicName(context, fromobj.getInfo(context, DomainConstants.SELECT_TYPE), "type");
 	        //fromobj.isKindOf(context, type);
 	        if(relationName.equals(LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM))
 	        {
 	        	alloweditem = LibraryCentralCommon.getAllowedEndItemsMap(context);
 	        }else
 	        {
 	        	alloweditem = LibraryCentralCommon.getAllowedClassesMap(context);
 	        }
 	        if(alloweditem.containsKey(fromtype))
 	        {
 	        	String allowedtype = (String)alloweditem.get(fromtype);
 	        	StringList allowedtypelist = new StringList(allowedtype.split(","));
 	        	if(allowedtypelist.contains(totype))
 	        	{
 	        		strResult = 0;
 	        		return strResult;
 	        	}else
 	        	{
 	        		for(Iterator iter = allowedtypelist.iterator(); iter.hasNext();)
 	        		{
 	        			String allowedname = (String)iter.next();
 	        			String allowednameactual = PropertyUtil.getSchemaProperty(context,allowedname);
 	        			if(toobj.isKindOf(context, allowednameactual))
 	        			{
 	        				strResult = 0;
 	        				return strResult;
 	        			}
 	        		}
 	        	}
 	        }
 	    }
 	    }catch(Exception ex)
 	    {
 	    	ex.printStackTrace(System.out);
 	    	throw ex;
 	    }
 	    return strResult;
 	}
     
     /***
      * This Function returns True showGranteeAccessCategory is set to true in emxLibraryCentral.properties, otherwise False
      * @param context
      * @param args
      * @throws Exception
      */
     public Boolean showGranteeAccessCategory(Context context,String[] args){
    	 String showGranteeAccessCategory = null;
    	 try {
    		 showGranteeAccessCategory = EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.showGranteeAccessCategory");
		 } catch (FrameworkException e) {
			// Don't do anything here
		 }
    	 
    	 if(showGranteeAccessCategory != null && showGranteeAccessCategory.toUpperCase().equals("TRUE")){
    		 return true;
    	 }
    	 return false;
      }
}
