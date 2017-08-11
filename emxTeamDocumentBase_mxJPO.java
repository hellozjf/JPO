/*
**   emxTeamDocumentBase.java
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of
**   MatrixOne, Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.*;
import matrix.util.*;
import java.util.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.*;


public class emxTeamDocumentBase_mxJPO extends emxCommonDocument_mxJPO
{
    /**
    * The default constructor.
    * @since Team 10.5
    */
    public emxTeamDocumentBase_mxJPO (Context context, String[] args) throws Exception
    {
      super(context, args);
    }

    /**
     * This method is executed before create of document object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param uploadParamsMap HashMap
     * @param objectId String
     * @return Map containing new Parent Id
     * @throws Exception if the operation fails
     * @since Team 10-5
     */
    public Map preCheckin(Context context, HashMap uploadParamsMap, String objectId) throws Exception
    {

        Map preCheckinMap = new HashMap();
        return preCheckinMap;
    }

    public void postCheckinDND(Context context, String[] args)throws Exception{
    	
    	HashMap map = (HashMap)JPO.unpackArgs(args);    	
    	HashMap uploadParamsMap=(HashMap) map.get("uploadParamsMap");    	
    	StringList objectIds=(StringList) map.get("objectIds");    	
    	postCheckin( context,  uploadParamsMap,  objectIds);
    	
    }
    /**
     * This method is executed after create of document object
     *  to handle subscriptions.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param uploadParamsMap HashMap
     * @param objectId String
     * @return void
     * @throws Exception if the operation fails
     * @since Team 10-5
     */
    public void postCheckin(Context context, HashMap uploadParamsMap, StringList objectIds) throws Exception
    {
        String objectAction = (String) uploadParamsMap.get("objectAction");
        String folderId = (String) uploadParamsMap.get("folderId");
        String parentId = (String) uploadParamsMap.get("parentId");
        String folderParentType = "";
        MapList routeList = null;
        String objectId = "";
        String holdId = "";

        String wsRelType = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults");
        String folderType = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
        String wsType = PropertyUtil.getSchemaProperty(context,"type_Project");
        StringBuffer selWSId = new StringBuffer(64);
        selWSId.append("relationship[");
        selWSId.append(wsRelType);
        selWSId.append("].from.id");
        if ((objectAction != null) && (objectAction.indexOf("create") >= 0) || (objectAction.indexOf("Create") >= 0) || "update".equals(objectAction) )
       {
          if ((folderId == null) || 
              ("".equals(folderId)) || 
              ("null".equals(folderId)))
          {
            if ((parentId != null)      &&
                (!"".equals(parentId))  &&
                (!"null".equals(parentId)))
             {
               DomainObject domainObject = DomainObject.newInstance(context);
               domainObject.setId(parentId);
               String rteRelType = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");
			   String routeType = PropertyUtil.getSchemaProperty(context,"type_Route");
			   StringList objectSelects = new StringList(1);
			   objectSelects.addElement(DomainObject.SELECT_ID);
			   String objectWhere = "";
			   routeList = domainObject.getRelatedObjects(context, rteRelType, routeType, objectSelects, null, false, true, (short) 1, objectWhere , null, 0);
               StringBuffer selWSType = new StringBuffer(64);
               selWSType.append("relationship[");
               selWSType.append(wsRelType);
               selWSType.append("].from.type");


               StringList selects = new StringList(4);
               selects.addElement(DomainObject.SELECT_TYPE);
               selects.addElement(selWSType.toString());
               selects.addElement(selWSId.toString());

               Map objMap = (Map)domainObject.getInfo(context, selects);
               String objType = (String)objMap.get(DomainObject.SELECT_TYPE);

               if (objType.equals(folderType))
               {
                   folderId = parentId;
               }
            }
          }
           
          if ((folderId != null)      &&
              (!"".equals(folderId))  &&
              (!"null".equals(folderId)))
          {
              WorkspaceVault folder = (WorkspaceVault)DomainObject.newInstance(context, folderId);
              Workspace workspace = null;
              SubscriptionManager wsSubMgr = null;

              StringList wsSelects = new StringList(1);
              wsSelects.addElement(DomainObject.SELECT_ID);
              Map folderMap = folder.getTopLevelVault(context, wsSelects);
              String topFolderId = (String)folderMap.get(DomainObject.SELECT_ID);
              
              WorkspaceVault topFolder = (WorkspaceVault)DomainObject.newInstance(context, topFolderId);
              String parentVaultId = (String)topFolder.getInfo(context, selWSId.toString());
              DomainObject vault = (DomainObject)DomainObject.newInstance(context, parentVaultId);
              folderParentType = vault.getInfo(context, DomainObject.SELECT_TYPE);
              if (folderParentType.equals(wsType))
              {
                workspace = (Workspace)DomainObject.newInstance(context, parentVaultId);
                wsSubMgr = new SubscriptionManager(workspace);
              }

              Iterator i = objectIds.iterator();
              while (i.hasNext())
              {
                objectId = (String)i.next();
                if ((!"".equals(objectId)) && (!holdId.equals(objectId)))
                {
                   CommonDocument document = (CommonDocument)DomainObject.newInstance(context, objectId);
                   SubscriptionManager subscriptionMgr = new SubscriptionManager(folder);
                   subscriptionMgr.publishEvent(context,
                                                folder.EVENT_CONTENT_ADDED,
                                                objectId);
                 
                   holdId = objectId;
                   if (wsSubMgr != null)
                   {
                     wsSubMgr.publishEvent(context,
                                           workspace.EVENT_FOLDER_CONTENT_MODIFIED,
                                           objectId);
                   }
                }
             }
          }
          if (routeList != null && routeList.size() > 0)
          {
            holdId = "";
            Iterator i = objectIds.iterator();
            while (i.hasNext())
            {
               objectId = (String)i.next();
               if ((!"".equals(objectId)) && (!holdId.equals(objectId)))
               {
				   Iterator rtItr = routeList.iterator();
					while (rtItr.hasNext())
					{
						Map m = (Map)rtItr.next();
						String routeId = (String)m.get(DomainObject.SELECT_ID);
						Route route = (Route)DomainObject.newInstance(context, routeId);
                  SubscriptionManager rteSubMgr = new SubscriptionManager(route);
                  rteSubMgr.publishEvent(context,
                                         route.EVENT_CONTENT_ADDED,
                                         objectId);
					}
                  holdId = objectId;
               }
            }
          }
       }
       else if(objectAction != null && "update".equals(objectAction))
       {
          Iterator i = objectIds.iterator();
          while (i.hasNext())
          {
             objectId = (String)i.next();
             if ((!"".equals(objectId)) && (!holdId.equals(objectId)))
             {
                CommonDocument document = (CommonDocument)DomainObject.newInstance(context, objectId);
                String docName = document.getInfo(context, DomainObject.SELECT_NAME);
                SubscriptionManager docSubMgr = new SubscriptionManager(document);
                docSubMgr.publishEvent(context,
                                       document.EVENT_FILE_CHECKED_IN,
                                       objectId);
                holdId = objectId;
             }
          }
       }
    }
}
