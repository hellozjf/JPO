/*
 **   emxTeamFileBase.java
 **
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 */

import matrix.db.*;
import matrix.util.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.framework.ui.*;

public class emxTeamFileBase_mxJPO extends emxCommonFile_mxJPO
{
    protected static boolean append = false;
    protected static boolean unlock = true;
    protected static String defaultFormat = DomainConstants.FORMAT_GENERIC;


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
    public emxTeamFileBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxTeamCentral.Generic.MethodOnTeamFile", context.getLocale().getLanguage()));
        }
        return 0;
    }


    /**
     * This method is executed to create a document object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return String new Document Id
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
    public String checkinCreate(Context context, String[] args) throws Exception
    {
        try
        {
            if (args == null || args.length < 1)
            {
                throw (new IllegalArgumentException());
            }
            Map map = (Map) JPO.unpackArgs(args);
            String description   = (String) map.get("description");
            String fileName      = (String) map.get("fileName");
            String language      = (String) map.get("language");
            String objectAction  = (String) map.get("objectAction");
            String folderId      = (String) map.get("folderId");
            String parentId      = (String) map.get("parentId");
            String parentRelName = (String) map.get("parentRelName");
            String documentId    = null;
            String routeId       = null;

            // when coming from Route Create wizard, a dummy parentId is sent
            // cause at that time, Route is not created yet, use the folderId to chekin
            // connecting to Route takes place in the last step of the create
            // Route wizard process
            if( "RouteWizard".equalsIgnoreCase(parentId))
            {
                parentId =  folderId;
            }

            // get the BusinessType of parent
            DomainObject parentObject = DomainObject.newInstance(context, parentId);
            String parentType         = parentObject.getInfo(context, "type");


            // folderId is passed explicitly when file upload is done into a route
            // in this case, first we upload the file into folder, in the
            // post checkin, Document will be connected to Route
            // since parentId passed is of Route Object, set this to routeId
            // then set the parentId to folderId, cause we want first upload and connect
            // to folder first
            if( parentType.equals(DomainConstants.TYPE_ROUTE))
            {
                routeId  = parentId;
                parentId = folderId;
            }

            if ( parentRelName != null )
            {
                parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
            }

            if (!DocumentUtil.isDocumentExists(context,parentId,fileName) )
            {
                documentId = checkinCreate(context, null, null, null, description,
                                           fileName, language, parentId, parentRelName);

                postCheckin( context, args, documentId );
            }
            return documentId;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * This method is executed to create a document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return String new Document Id
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
    public String mcsCheckinCreate(Context context, String[] args) throws Exception
    {

        try
        {
            if (args == null || args.length < 1)
            {
                throw (new IllegalArgumentException());
            }
            Map map = (Map) JPO.unpackArgs(args);
            String description   = (String) map.get("description");
            String fileName      = (String) map.get("fileName");
            String store         = (String) map.get("store");
            String language      = (String) map.get("language");
            String parentId      = (String) map.get("parentId");
            String parentRelName = (String) map.get("parentRelName");
            String workspaceId   = (String) map.get("workspaceId");
            String folderId      = (String) map.get("folderId");
            String routeId       = null;
            String documentId    = null;

            // when coming from Route Create wizard, a dummy parentId is sent
            // cause at that time, Route is not created yet, use the folderId to chekin
            // connecting to Route takes place in the last step of the create
            // Route wizard process
            if( "RouteWizard".equalsIgnoreCase(parentId))
            {
                parentId =  folderId;
            }

            // get the BusinessType of parent
            DomainObject parentObject = DomainObject.newInstance(context, parentId);
            String parentType         = parentObject.getInfo(context, "type");

            if( parentType.equals(DomainConstants.TYPE_WORKSPACE_VAULT))
            {
                folderId = parentId;
            }

            // folderId is passed explicitly when file upload is done into a route
            // in this case, first we upload the file into folder, in the
            // post checkin, Document will be connected to Route
            // since parentId passed is of Route Object, set this to routeId
            // then set the parentId to folderId, cause we want first upload and connect
            // to folder first
            if( parentType.equals(DomainConstants.TYPE_ROUTE))
            {
                routeId  = parentId;
                parentId = folderId;
            }

            if ( parentRelName != null )
            {
                parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
            }

            StringList fileList = new StringList(1);
            fileList.add(fileName);

            if (!DocumentUtil.isDocumentExists(context,parentId,fileName) )
            {
                documentId = mcsCheckinCreate(context, fileList, null, fileName, description,
                                              store, append, defaultFormat, unlock, language,
                                              parentId, parentRelName);

                postCheckin( context, args, documentId );
            }
            return documentId;

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * This method is executed to revise the document object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return String new Document Id
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
    public String checkinRevise(Context context, String[] args) throws Exception
    {

        try
        {
            if (args == null || args.length < 1)
            {
                throw (new IllegalArgumentException());
            }
            Map map = (Map) JPO.unpackArgs(args);
            String description = (String) map.get("description");
            String reason      = (String) map.get("reason");
            String language    = (String) map.get("language");
            String objectAction= (String) map.get("objectAction");
            String objectId    = (String) map.get("objectId");
            String documentId  = null;

            Document doc = (Document)DomainObject.newInstance(context, objectId, DomainConstants.SOURCING);
            Document revisedDoc = doc.revise(context);
            documentId = revisedDoc.getObjectId();

            if ( description != null )
            {
                revisedDoc.setDescription(context, description);
            }
            if ( language != null )
            {
                revisedDoc.setAttributeValue(context,DomainObject.ATTRIBUTE_LANGUAGE, language);
            }
            if ( reason != null )
            {
                revisedDoc.setAttributeValue(context,DomainObject.ATTRIBUTE_CHECKIN_REASON, reason);
            }

            postCheckin( context, args, documentId);

            return documentId;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }

    }


    /**
     * This method is executed to revise the document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return String new Document Id
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
    public String mcsCheckinRevise(Context context, String[] args) throws Exception
    {

        try
        {
            if (args == null || args.length < 1)
            {
                throw (new IllegalArgumentException());
            }
            Map map = (Map) JPO.unpackArgs(args);
            String description = (String) map.get("description");
            String reason      = (String) map.get("reason");
            String fileName    = (String) map.get("fileName");
            String store       = (String) map.get("store");
            String language    = (String) map.get("language");
            String objectAction= (String) map.get("objectAction");
            String objectId    = (String) map.get("objectId");
            String documentId  = null;

            Document doc = (Document)DomainObject.newInstance(context, objectId, DomainConstants.SOURCING);
            StringList fileList = new StringList(1);
            fileList.add(fileName);

            Document revisedDoc = doc.checkinRevise(context, fileList, description, store,  append, defaultFormat, unlock, language);
            documentId = revisedDoc.getObjectId();

            if ( reason != null )
            {
                revisedDoc.setAttributeValue(context,DomainObject.ATTRIBUTE_CHECKIN_REASON, reason);
            }

            java.util.Date vDate = new java.util.Date();
            String  strDate  = vDate.toString();
            Date versionDate= new Date();
            SimpleDateFormat  sFormat  = new SimpleDateFormat("MM/dd/yy");

            Attribute routeTaskScheduledDateAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_VERSION_DATE) ,sFormat.format(versionDate).toString());

            AttributeList  attrList = new AttributeList();
            attrList.addElement(routeTaskScheduledDateAttribute);
            revisedDoc.setAttributes(context,attrList);

            postCheckin( context, args, documentId);
            return documentId;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }

    }

    /**
     * This method is executed after the create of document object
     * to notify Subscriptions.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds :
     *     String  the workspaceId
     *     String  the folderId
     *     String  the routeId
     * @param String  the documentId
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
    public void postCheckin(Context context, String[] args, String documentId) throws Exception
    {
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        Map map = (Map) JPO.unpackArgs(args);
        String objectAction= (String) map.get("objectAction");
        String workspaceId   = (String) map.get("workspaceId");
        String folderId      = (String) map.get("folderId");
        String routeId       = (String) map.get("routeId");
        String parentId      = (String) map.get("parentId");
        Workspace      workspace = (Workspace)DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE, DomainConstants.TEAM);
        WorkspaceVault folder    = (WorkspaceVault)DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT, DomainConstants.TEAM);
        Route          route     = (Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE, DomainConstants.TEAM);
        Document       document  = (Document)DomainObject.newInstance(context, DomainConstants.TYPE_DOCUMENT,DomainConstants.TEAM);

        String  treeMenu = null;
        SubscriptionManager subscriptionMgr = null;

        if ( workspaceId != null && ("".equals(workspaceId.trim()) || "null".equals(workspaceId.trim()) ))
        {
            workspaceId = null;
        }
        else if ( folderId != null && ("".equals(folderId.trim()) || "null".equals(folderId.trim()) ))
        {
            folderId = null;
        }
        else if ( routeId != null && ("".equals(routeId.trim()) || "null".equals(routeId.trim()) ))
        {
            routeId = null;
        }
        else if ( parentId != null && ("".equals(parentId.trim()) || "null".equals(parentId.trim()) ))
        {
            parentId = null;
        }
        else if ( documentId != null && ("".equals(documentId.trim()) || "null".equals(documentId.trim()) ))
        {
            documentId = null;
        }

        if( objectAction != null && "create".equals(objectAction))
        {

            // when coming from Route Create wizard, a dummy parentId is sent
            // cause at that time, Route is not created yet, use the folderId to chekin
            // connecting to Route takes place in the last step of the create
            // Route wizard process

            if( "RouteWizard".equalsIgnoreCase(parentId))
            {
                parentId =  folderId;
            }

            // get the BusinessType of parent
            DomainObject parentObject = DomainObject.newInstance(context, parentId);
            String parentType         = parentObject.getInfo(context, "type");

            if( parentType.equals(DomainConstants.TYPE_WORKSPACE_VAULT))
            {
                folderId = parentId;
            }

            // folderId is passed explicitly when file upload is done into a route
            // in this case, first we upload the file into folder, in the
            // post checkin, Document will be connected to Route
            // since parentId passed is of Route Object, set this to routeId
            // then set the parentId to folderId, cause we want first upload and connect
            // to folder first
            if( parentType.equals(DomainConstants.TYPE_ROUTE))
            {
                routeId  = parentId;
                parentId = folderId;
            }

            // Connect to Route with ObjectRoute relationship
            // routeId will be set only when file upload is done into a route
            if( documentId != null && routeId != null)
            {
                String parentRelName = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");

                document.setId(documentId);
                document.addToObject(context, new RelationshipType(parentRelName), routeId);
            }

            if( workspaceId != null && folderId != null)
            {
                workspace.setId(workspaceId);
                subscriptionMgr = new SubscriptionManager(workspace);
                treeMenu = EnoviaResourceBundle.getProperty(context,"eServiceSuiteTeamCentral.emxTreeAlternateMenuName.type_ProjectVault");

                if(treeMenu != null)
                {
                    MailUtil.setTreeMenuName(context, treeMenu );
                }

                subscriptionMgr.publishEvent(context, workspace.EVENT_FOLDER_CONTENT_MODIFIED, folderId);
            }

            if( folderId != null && documentId != null)
            {
                folder.setId(folderId);
                subscriptionMgr = new SubscriptionManager(folder);

                treeMenu = EnoviaResourceBundle.getProperty(context,"eServiceSuiteTeamCentral.emxTreeAlternateMenuName.type_Document");

                if(treeMenu != null)
                {
                    MailUtil.setTreeMenuName(context, treeMenu );
                }

                subscriptionMgr.publishEvent(context, folder.EVENT_CONTENT_ADDED, documentId);
            }

            if( routeId != null && documentId != null)
            {
                route.setId(routeId);
                subscriptionMgr = new SubscriptionManager(route);

                treeMenu = EnoviaResourceBundle.getProperty(context,"eServiceSuiteTeamCentral.emxTreeAlternateMenuName.type_Document");

                if(treeMenu != null)
                {
                    MailUtil.setTreeMenuName(context, treeMenu );
                }

                subscriptionMgr.publishEvent(context, route.EVENT_CONTENT_ADDED, documentId);
            }
        }
        else if(objectAction != null && "revise".equals(objectAction))
        {
            if( documentId != null)
            {
                document.setId(documentId);
                subscriptionMgr = new SubscriptionManager(document);

                treeMenu = EnoviaResourceBundle.getProperty(context,"eServiceSuiteTeamCentral.emxTreeAlternateMenuName.type_Document");

                if(treeMenu != null)
                {
                    MailUtil.setTreeMenuName(context, treeMenu );
                }

                subscriptionMgr.publishEvent(context, document.EVENT_FILE_CHECKED_IN, documentId);
            }
        }

      return;
    }

    /**
     * This method is executed to create an  object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return Map
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public Map multiFileCheckinCreate(Context context, String[] args) throws Exception
     {
          Map objectMap = new HashMap();

          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

          String strCounnt = (String) uploadParamsMap.get("noOfFiles");
          int count = new Integer(strCounnt).intValue();
          StringList objectIds = new StringList(count);
          StringList formats = new StringList(count);
          StringList fileNames = new StringList(count);
          objectMap.put("format", formats);
          objectMap.put("fileName", fileNames);
          objectMap.put("objectId", objectIds);
          String parentId       = (String) uploadParamsMap.get("parentId");
          String parentRelName  = (String) uploadParamsMap.get("parentRelName");
          if ( parentRelName != null )
          {
              parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
          }

          for( int i=0; i<count; i++ )
          {
              String format  = (String)uploadParamsMap.get("format" + i);
              String title  = (String)uploadParamsMap.get("title" + i);
              String language  = (String)uploadParamsMap.get("language" + i);
              String description  = (String)uploadParamsMap.get("description" + i);
              if ( title != null && !"".equals(title) && !"null".equals(title)) {
                if (!DocumentUtil.isDocumentExists(context,parentId,title) )
                {
                  preCheckin(context, args);
                  String objectId = checkinCreate(context, null, null, null, description, title, language, parentId, parentRelName);
                  postCheckin(context, args, objectId);
                  objectIds.addElement(objectId);
                  formats.addElement(format);
                  fileNames.addElement(title);
                }
              }
          }

          return objectMap;
    }

    /**
     * This method is executed to create a document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return Map 
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
     public Map mcsMultiFileCheckinCreate(Context context, String[] args) throws Exception
     {

          Map objectMap = new HashMap();

          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

          String parentId       = (String) uploadParamsMap.get("parentId");
          String parentRelName  = (String) uploadParamsMap.get("parentRelName");
          if ( parentRelName != null )
          {
              parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
          }

          String store      = (String) uploadParamsMap.get("store");
          String strCounnt = (String) uploadParamsMap.get("noOfFiles");
          int count = new Integer(strCounnt).intValue();
          StringList objectIds = new StringList(count);
          StringList formats = new StringList(count);
          StringList fileNames = new StringList(count);
          objectMap.put("format", formats);
          objectMap.put("fileName", fileNames);
          objectMap.put("objectId", objectIds);

          for( int i=0; i<count; i++ )
          {
              String format  = (String)uploadParamsMap.get("format" + i);
              String title  = (String)uploadParamsMap.get("title" + i);
              String description  = (String)uploadParamsMap.get("description" + i);
              String language  = (String)uploadParamsMap.get("language" + i);
              if (  title != null && !"".equals(title) && !"null".equals(title) ) {
                if (!DocumentUtil.isDocumentExists(context,parentId,title) )
                {

                  StringList fileList = new StringList(1);
                  fileList.addElement(title);
                  preCheckin(context, args);
                  String objectId = mcsCheckinCreate(context, fileList, null, title,
                                                      description, store, defaultAppend,
                                                      format, defaultUnlock, language,
                                                      parentId, parentRelName);

                  postCheckin(context, args, objectId);
                  objectIds.addElement(objectId);
                  formats.addElement(format);
                  fileNames.addElement(title);
                }
              }
          }

          return objectMap;
     }

}
