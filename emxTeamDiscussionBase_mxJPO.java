/*
 *  emxTeamDiscussionBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import com.matrixone.apps.common.Message;
import com.matrixone.apps.common.Route ;
import com.matrixone.apps.common.Workspace ;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.Document ;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.servlet.Framework;
import com.matrixone.apps.common.Person ;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import matrix.db.*;
import matrix.util.*;


public class emxTeamDiscussionBase_mxJPO
{

  /**
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since Team Central 10-7-0-0 next
   * @grade 0
   */
    public emxTeamDiscussionBase_mxJPO ()  throws Exception
    {
    }

    static final StringList EMPTY_STRING_LIST = new StringList(0).unmodifiableCopy();

  /**
   * This method returns the Discussion in a Workspace/Folder/Project
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns MapList containing discussions
   * @throws Exception if the operation fails
   * @since Team Central 10-7-0-0 next
   */
    public MapList getDiscussions(Context context,String args[])  throws Exception
    {

      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      String sLanguage = (String)context.getSession().getLanguage();
      String objectId=(String)programMap.get("objectId");
      String workspaceId = (String)programMap.get("workspaceId");
      String folderId = (String)programMap.get("folderId");
      String discussion = (String)programMap.get("discussion");
      com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
      if(objectId != null && !"".equals(objectId))
      {
        userIsAuthenticated(context,objectId , sLanguage);
      }
      Message message           = (Message) DomainObject.newInstance(context,DomainConstants.TYPE_MESSAGE,DomainConstants.TEAM);
      DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
      String sReadAccess = "current.access[read]";
      StringList baseObjectSelects = new StringList();
      baseObjectSelects.addElement(BaseObject.SELECT_TYPE);
      baseObjectSelects.addElement(sReadAccess);
      Map baseMap = BaseObject.getInfo(context, baseObjectSelects);
      String strType = (String) baseMap.get(BaseObject.SELECT_TYPE);
      String sCurrentReadAccess = (String) baseMap.get(sReadAccess);
      if(strType.equals(BaseObject.TYPE_WORKSPACE))
      {
        workspaceId = objectId;
      }
      String sDocument = PropertyUtil.getSchemaProperty(context, "type_Document");
      String sFolder = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
      String domainObjectId = null;
      StringList objectSelects = new StringList();
      //MessageManager messagemgr = null;
      DomainObject baseObject = DomainObject.newInstance(context, objectId);;
      MapList discussionList = new MapList();
      domainObjectId = objectId;
      if(sCurrentReadAccess.equalsIgnoreCase("TRUE"))
      {
            objectSelects.add(BaseObject.SELECT_ID);
            objectSelects.add(BaseObject.SELECT_RELATIONSHIP_ID);
            MapList totalresultList = null;
            totalresultList = message.list(context,objectSelects,null, null, baseObject);
            discussionList=totalresultList;
            objectSelects = new StringList();
            objectSelects.add(Message.SELECT_MESSAGE_SUBJECT);
            objectSelects.add(BaseObject.SELECT_OWNER);
            objectSelects.add(BaseObject.SELECT_MODIFIED);
            objectSelects.add(BaseObject.SELECT_ID);
            objectSelects.add(Message.SELECT_MESSAGE_COUNT);
            objectSelects.add(sReadAccess);
            Iterator discussionIdItr = discussionList.iterator();
            StringList disIdList = new StringList();
            while(discussionIdItr.hasNext())
            {
              Map disMap = (Map) discussionIdItr.next();
              String disId = (String) disMap.get(BaseObject.SELECT_ID);
              if(!disIdList.contains(disId))
              {
                disIdList.addElement(disId);
              }
            }
            discussionList = DomainObject.getInfo(context, (String [])disIdList.toArray(new String []{}), objectSelects);
      }
      return discussionList;
  }

  /**
   * This method verified whether the user is authenticated.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param objectId holds the objectId
   * @returns nothing
   * @throws Exception if the operation fails
   * @since Team Central 10-7-0-0 next
   */
  public void userIsAuthenticated(Context context, String objectId , String sLanguage) throws Exception
  {
        i18nNow loc = new i18nNow();
        String hasReadAccess = null;
        try
        {
            DomainObject BaseObject = DomainObject.newInstance(context , objectId);
            BaseObject.setId(objectId);
            hasReadAccess = BaseObject.getInfo(context, "current.access[read,checkout]");
        }
       catch(Exception e)
       {
            throw new MatrixException((String)loc.GetString("emxTeamCentralStringResource" ,"emxTeamCentral.Common.PageAccessDenied", sLanguage));
       }
       if(!hasReadAccess.equals("TRUE"))
       {
        throw new MatrixException((String)loc.GetString("emxTeamCentralStringResource" ,"emxTeamCentral.Common.PageAccessDenied", sLanguage));
       }
  }
 // End of class definition
}
