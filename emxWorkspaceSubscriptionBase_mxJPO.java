/*
 *  emxWorkspaceSubscriptionBase.java
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
import com.matrixone.apps.common.MessageHolder;
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

public class emxWorkspaceSubscriptionBase_mxJPO
{

  /**
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   * @grade 0
   */
    public emxWorkspaceSubscriptionBase_mxJPO ()  throws Exception
    {
    }

    static final StringList EMPTY_STRING_LIST = new StringList(0).unmodifiableCopy();
    String sLanguage="" ;
    String projectId="" ;
    static Hashtable projectMemberHashtable ;
    static final i18nNow loc = new i18nNow();
  /**
   * This method returns the subscriptions maplist in the workspace
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getSubscriptions(Context context,String args[])  throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        sLanguage = (String)context.getSession().getLanguage();
        String objectId=(String)programMap.get("objectId");
        projectId =(String) programMap.get("workspaceId");
        if(projectId == null) projectId = objectId;
        if(projectId != null && !"".equals(projectId))
        {
            userIsAuthenticated(context,objectId);
        }
        String sObjectName = "";
        String sObjectType = "";
        String sEventType = "";
        String sEventId = "";

        MapList templateMapList = new MapList();
        TreeMap projectMapEx = projectMapEx = new TreeMap();
        projectMapEx.putAll(AddProjectEvents(sLanguage));
        projectMapEx.putAll(AddRouteEvents(sLanguage));
        projectMapEx.putAll(AddTopicsEvents(sLanguage));
        projectMapEx.putAll(AddDiscussionEvents(sLanguage));
        projectMapEx.putAll(AddDocumentEvents(sLanguage));
        projectMapEx.putAll(AddMessageEvents(sLanguage));

        BusinessObjectList boList ;
        BusinessObject boPerson=null ;
        DomainObject templateObj = DomainObject.newInstance(context);

        TreeMap eventMap = new TreeMap();
        TreeMap projectMap = new TreeMap();
        Pattern typePattern1 = new Pattern(DomainConstants.TYPE_PERSON);
        Pattern relPattern1 = new Pattern(DomainConstants.RELATIONSHIP_PUSHED_SUBSCRIPTION);

        //Get person who is logged in
        boPerson = Person.getPerson(context);

        //Attain a list of all Events for this project
        boList  = new BusinessObjectList();
        MapList docRelatedList = null;
            boList = getAllRoleUserEvents(context,projectId);


        boPerson.close(context);
        BusinessObjectItr boItr = new BusinessObjectItr(boList);

        Locale loc = new Locale(sLanguage);
        String sMessageProject=EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", loc, "emxTeamCentral.TeamGetSubcriptionsByProject.TypeProject");
        String sMessageRoute=EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", loc, "emxTeamCentral.TeamGetSubcriptionsByProject.TypeRoute");
        String sMessageCategory=EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", loc, "emxTeamCentral.TeamGetSubcriptionsByProject.TypeCategory");
        String sMessageDiscussionThread=EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", loc, "emxTeamCentral.TeamGetSubcriptionsByProject.TypeDiscussionThread");
        String sMessageDocument=EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", loc, "emxTeamCentral.TeamGetSubcriptionsByProject.TypeDocument");
        while(boItr.next())
        {
            //Open list of Events
            BusinessObject boEvent = boItr.obj();
            boEvent.open(context);
            //Get the ObjectId & the EventType
            sEventId = boEvent.getObjectId();
            sEventType = FrameworkUtil.getAttribute(context,boEvent,DomainConstants.ATTRIBUTE_EVENT_TYPE);
            //Build relationship pattern for multilevel traversal
            Pattern relPattern = new Pattern (DomainConstants.RELATIONSHIP_PUBLISH);
            relPattern.addPattern(DomainConstants.RELATIONSHIP_PUBLISH_SUBSCRIBE);

            //Build type pattern for multilevel traversal
            Pattern typePattern = new Pattern (DomainConstants.TYPE_PUBLISH_SUBSCRIBE);
            typePattern.addPattern(DomainConstants.TYPE_DOCUMENT);
            typePattern.addPattern(DomainConstants.TYPE_THREAD);
            typePattern.addPattern(DomainConstants.TYPE_PROJECT_VAULT);
            typePattern.addPattern(DomainConstants.TYPE_PROJECT);
            typePattern.addPattern(DomainConstants.TYPE_ROUTE);
            typePattern.addPattern(DomainConstants.TYPE_MESSAGE);

            //Build selectlist  params
            SelectList typeSelectList       = new SelectList();
            typeSelectList.addName();
            typeSelectList.addId();
            typeSelectList.addType();

            // Perform a multilevel expand from Event to Any object
			ContextUtil.startTransaction(context,false);
            ExpansionIterator expItr = boEvent.getExpansionIterator(context,relPattern.getPattern(),typePattern.getPattern(),
                    typeSelectList, new SelectList(), true, false, (short)2,null,null,(short)0,false,false,(short)100,false);
          try {
            boEvent.close(context);
            //While Any objects exist iterate through and get data and put it into a HashTable
            while (expItr.hasNext())
            {
                Hashtable anyObjectHash = expItr.next().getTargetData();
                //If the relationship of type Publish Subscribe then get Object Name & Type
                if(!anyObjectHash.get("type").equals(DomainConstants.RELATIONSHIP_PUBLISH_SUBSCRIBE) )
                {
                    sObjectName = (String) anyObjectHash.get("name");
                    sObjectType = (String) anyObjectHash.get("type");
                    //If the object is of type document get the document id
                    if (sObjectType.equals(DomainConstants.TYPE_DOCUMENT) || sObjectType.equals(DomainConstants.TYPE_THREAD) || sObjectType.equals(DomainConstants.TYPE_MESSAGE))
                    {
                        String sDocId;
                        if (sObjectType.equals(DomainConstants.TYPE_THREAD) )
                        {
                            sDocId = getFileName(context,(String)anyObjectHash.get("id"));
                        }
                        else
                        {
                            sDocId = (String) anyObjectHash.get("id");
                        }
                        BusinessObject boDoc = new BusinessObject(sDocId);
                        boDoc.open(context);
                        sObjectName = (String) FrameworkUtil.getAttribute(context,boDoc,DomainConstants.ATTRIBUTE_TITLE);
                        if (sObjectType.equals(DomainConstants.TYPE_THREAD))
                        {
                            sObjectName = "Message Board for" + " " + sObjectName;
                        }
                        if(sObjectType.equals(DomainConstants.TYPE_MESSAGE))
                        {
                            BusinessObject Dis = new BusinessObject((String)anyObjectHash.get("id"));
                            Dis.open(context);
                            sObjectName =(String) FrameworkUtil.getAttribute(context,Dis,DomainConstants.ATTRIBUTE_SUBJECT);
                            Dis.close(context);
                        }
                    }
                    if (DomainConstants.TYPE_PROJECT.equals(sObjectType))
                    {
                        sObjectType = sMessageProject ;
                    }
                    else if (DomainConstants.TYPE_ROUTE.equals(sObjectType))
                    {
                        sObjectType = sMessageRoute ;
                    }
                    else if (DomainConstants.TYPE_PROJECT_VAULT.equals(sObjectType))
                    {
                        sObjectType = sMessageCategory ;
                    }
                    else if (DomainConstants.TYPE_THREAD.equals(sObjectType))
                    {
                        sObjectType = sMessageDiscussionThread ;
                    }
                    else if (DomainConstants.TYPE_DOCUMENT.equals(sObjectType))
                    {
                        sObjectType = sMessageDocument ;
                    }
                    if(sObjectType.equals(DomainConstants.TYPE_MESSAGE)) {
                        sObjectType = "Discussion";
                    }
                    templateObj.setId(sEventId);
                    StringList subscribeList = templateObj.getInfoList(context, "from["+DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON+"].to.id");
                    StringList pushSubscribeList = templateObj.getInfoList(context, "from["+DomainConstants.RELATIONSHIP_PUSHED_SUBSCRIPTION+"].to.id");
                    //need to display subscriptions having both "pushed subscription" and "subscribed person" rels
                    if(subscribeList.contains(boPerson.getObjectId()))
                    {
                        Hashtable hashTableFinal=new Hashtable();
                        hashTableFinal.put(templateObj.SELECT_ID, sEventId);
                        hashTableFinal.put(templateObj.SELECT_NAME, sObjectName);
                        hashTableFinal.put(templateObj.SELECT_TYPE, sObjectType);
                        hashTableFinal.put("event", projectMapEx.get(sEventType));
                        hashTableFinal.put("relName",DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON);
                        if ( templateMapList.contains(hashTableFinal)==false )  {
                            templateMapList.add(hashTableFinal);
                        }
                    }
                    if(pushSubscribeList.contains(boPerson.getObjectId()))
                    {
                        Hashtable hashTableFinal=new Hashtable();
                        hashTableFinal.put(templateObj.SELECT_ID, sEventId);
                        hashTableFinal.put(templateObj.SELECT_NAME, sObjectName);
                        hashTableFinal.put(templateObj.SELECT_TYPE, sObjectType);
                        hashTableFinal.put("event", projectMapEx.get(sEventType));
                        hashTableFinal.put("relName",DomainConstants.RELATIONSHIP_PUSHED_SUBSCRIPTION);
                        if ( templateMapList.contains(hashTableFinal)==false )  {
                            templateMapList.add(hashTableFinal);
                        }
                    }
                }
            }
          } 
          finally {
              expItr.close();
          }
			ContextUtil.commitTransaction(context);
        }
        return templateMapList ;
    }

  /**
   * This method checks whether the logged user is authenticated user or not
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public void userIsAuthenticated(Context context, String objectId) throws Exception
    {
        //check if user has access on the object
        String hasReadAccess = null;
        try
        {
            DomainObject BaseObject = DomainObject.newInstance(context , objectId);
            BaseObject.setId(objectId);
            hasReadAccess = BaseObject.getInfo(context, "current.access[read,show]");
        }  catch(Exception e) {
                                           throw new MatrixException(loc.GetString("emxTeamCentralStringResource" ,sLanguage,
                                           "emxTeamCentral.Common.PageAccessDenied"));
                                      }
        if(!hasReadAccess.equals("TRUE"))
        {
                                            throw new MatrixException(loc.GetString("emxTeamCentralStringResource" ,sLanguage,
                                            "emxTeamCentral.Common.PageAccessDenied"));
        }
    }

  /**
   * This method returns project member given person object
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   * @deprecated since BPS R216 and can be removed after 2 releases
   */
    @Deprecated
    public synchronized BusinessObject getProjectMember(Context context, BusinessObject person) throws Exception
    {
        Hashtable personHashtable = null;
        if ( projectMemberHashtable != null )
        {
            personHashtable = (Hashtable)projectMemberHashtable.get(person.getObjectId());
            if (personHashtable != null)
            {
                BusinessObject personMemberObj = (BusinessObject)personHashtable.get(projectId);
                if (personMemberObj != null)
                {
                    return new BusinessObject(personMemberObj);
                }
            }
            else
            {
                personHashtable = new Hashtable();
                projectMemberHashtable.put(person.getObjectId(), personHashtable);
            }
        }
        else
        {
            projectMemberHashtable = new Hashtable();
            personHashtable = new Hashtable();
            projectMemberHashtable.put(person.getObjectId(), personHashtable);
        }
        String relPattern = DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP;
        String strWhereClause = "to[" +DomainConstants.RELATIONSHIP_PROJECT_MEMBERS+ "].from.id ==" + projectId;
		ContextUtil.startTransaction(context,false);
        ExpansionIterator expItr = person.getExpansionIterator(context,relPattern,DomainConstants.TYPE_PROJECT_MEMBER, EMPTY_STRING_LIST,
                EMPTY_STRING_LIST,false, true, (short)1,strWhereClause,null,(short)0,false,false,(short)100,false);
        BusinessObject busProjectMember = null;
      try {
        if (expItr.hasNext())
        {
            busProjectMember = expItr.next().getTo();
        }
      } finally {
		expItr.close();
      }
		ContextUtil.commitTransaction(context);
        if (busProjectMember != null )
        {
            personHashtable.put(projectId, busProjectMember);
        }
        return busProjectMember;
    }

  /**
   * This method returns events of all the project members of given person
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   * @deprecated since BPS R216 and can be removed after 2 releases
   */
    @Deprecated
    public BusinessObjectList getAllProjectMemberEvents(Context context, BusinessObject person) throws Exception
    {
        BusinessObjectList projectEventList = new BusinessObjectList();
        BusinessObject projectEvent = null;
        //Get the project member object
        BusinessObject projectMember = getProjectMember(context, person);

        //Build Relationship and Type patterns
        Pattern relPattern    = new Pattern(DomainConstants.RELATIONSHIP_SUBSCRIBED_ITEM);
        Pattern typePattern   = new Pattern(DomainConstants.TYPE_EVENT);
        //Build selectlist  params
        SelectList typeSelectList   = new SelectList();
        typeSelectList.addName();
        typeSelectList.addId();
        typeSelectList.addAttribute(DomainConstants.ATTRIBUTE_EVENT_TYPE);

        if(projectMember != null && !("".equals(projectMember)) && !("null".equals(projectMember)))
        {
            projectMember.open(context);
			ContextUtil.startTransaction(context,false);
            ExpansionIterator expItr = projectMember.getExpansionIterator(context,relPattern.getPattern(),typePattern.getPattern(),
                    typeSelectList, new SelectList(), true, true, (short)1,null,null,(short)0,false,false,(short)100,false);
          try {
            projectMember.close(context);
            // check the Project Member relations
            for ( ; expItr.hasNext() ; projectEventList.add(projectEvent) )  {
                projectEvent    = expItr.next().getFrom();
            }
          } finally {
			expItr.close();
          }
			ContextUtil.commitTransaction(context);
        }
        return projectEventList;
    }

  /**
   * This method returns events of all role users
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public BusinessObjectList getAllRoleUserEvents(Context context, String sProjectId) throws MatrixException
    {
        Hashtable eventHash = getAllPersonEvents(context);
        BusinessObjectList projectEventList = new BusinessObjectList();
        RelationshipWithSelectItr relItr = null;
        //ExpansionWithSelect boGeneric = null;
        RelationshipWithSelect relWS = null;
        BusinessObject boProject = new BusinessObject(sProjectId);
        boProject.open(context);

        Pattern typePattern   = new Pattern(DomainConstants.TYPE_PROJECT_VAULT);
        typePattern.addPattern(DomainConstants.TYPE_DOCUMENT);
        typePattern.addPattern(DomainConstants.TYPE_THREAD);
        typePattern.addPattern(DomainConstants.TYPE_MESSAGE);
        typePattern.addPattern(DomainConstants.TYPE_PUBLISH_SUBSCRIBE);
        typePattern.addPattern(DomainConstants.TYPE_EVENT);

        Pattern  relPattern  = new Pattern(DomainConstants.RELATIONSHIP_PROJECT_VAULTS);
        relPattern.addPattern(DomainConstants.RELATIONSHIP_SUB_VAULTS);
        relPattern.addPattern(DomainConstants.RELATIONSHIP_PUBLISH_SUBSCRIBE);
        relPattern.addPattern(DomainConstants.RELATIONSHIP_PUBLISH);
        relPattern.addPattern(DomainConstants.RELATIONSHIP_THREAD);
        relPattern.addPattern(DomainConstants.RELATIONSHIP_MESSAGE);
        relPattern.addPattern(DomainConstants.RELATIONSHIP_VAULTED_DOCUMENTS);

        //Build selectlist  params
        SelectList typeSelectList   = new SelectList();
        typeSelectList.addName();
        typeSelectList.addId();
        typeSelectList.addAttribute(DomainConstants.ATTRIBUTE_EVENT_TYPE);

		ContextUtil.startTransaction(context,false);
        ExpansionIterator expItr = boProject.getExpansionIterator(context,relPattern.getPattern(),typePattern.getPattern(),
                typeSelectList,new SelectList(),true,true,(short)0,null,null,(short)0,false,false,(short)100,false);

      try {
        boProject.close(context);
        while (expItr.hasNext())
        {
            relWS = expItr.next();
            if(relWS.getTypeName().equals(DomainConstants.RELATIONSHIP_PUBLISH))
            {
                BusinessObject  obj = relWS.getTo();
                obj.open(context);
                String objId = obj.getObjectId();
                if(eventHash.contains(objId))
                {
                    projectEventList.add(obj);
                }
                obj.close(context);
            }
        }
      } finally {
		expItr.close();
      }
		ContextUtil.commitTransaction(context);
        return projectEventList;
    }

  /**
   * This method returns events of all persons
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public Hashtable getAllPersonEvents(Context context) throws MatrixException
    {
        BusinessObject boPerson   = Person.getPerson(context);
        boPerson.open(context);
        Pattern relPattern    = new Pattern(DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON);
        Pattern typePattern   = new Pattern(DomainConstants.TYPE_EVENT);

        //Build select params
        SelectList selectStmts  = new SelectList();
        selectStmts.addName();
        selectStmts.addId();

        //Build select params for Relationship
        SelectList selectRelStmts   = new SelectList();
		ContextUtil.startTransaction(context,false);
        ExpansionIterator expItr = boPerson.getExpansionIterator(context,relPattern.getPattern(),typePattern.getPattern(),
                                             selectStmts, selectRelStmts,true, true, (short)1,null,null,(short)0,false,false,(short)100,false);

            BusinessObject boEvent  = null;
            Hashtable evtHash = new Hashtable();
        try {
            
            String sEventId = null;
            
            //If the PS object doesn't exist then create PS & Event Objects
            for ( ; expItr.hasNext(); evtHash.put(sEventId, sEventId) )  {
                sEventId = (String) expItr.next().getTargetSelectData("id");
            }
        } finally {
		expItr.close();
        }
		ContextUtil.commitTransaction(context);
		boPerson.close(context);
        return evtHash;
    }

  /**
   * This method returns filename when thread-id is given
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public String getFileName(matrix.db.Context context, String ThreadId) throws MatrixException
    {
        //Get the project member object
        BusinessObject boThread   = new BusinessObject(ThreadId);
        //Build Relationship and Type patterns
        Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_THREAD);
        Pattern typePattern = new Pattern(DomainConstants.TYPE_DOCUMENT);
        //Build selectlist  params
        SelectList typeSelectList = new SelectList();
        typeSelectList.addId();
        boThread.open(context);
		ContextUtil.startTransaction(context,false);
        ExpansionIterator expItr = boThread.getExpansionIterator(context, relPattern.getPattern(),typePattern.getPattern(),
                                             typeSelectList, new SelectList(), true, true, (short)1,null,null,(short)0,false,false,(short)100,false);

        try {
            boThread.close(context);

            //Check the Project Member relations
            if (expItr != null && expItr.hasNext())
                {
                    return expItr.next().getTargetSelectData("id");
                }
        } finally {
		expItr.close();
        }

		ContextUtil.commitTransaction(context);
        return "";
    }

  /**
   * This method returns all project events
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public TreeMap AddProjectEvents(String sLanguage){
        TreeMap eventMap = new TreeMap();
        eventMap.put(Workspace.EVENT_ROUTE_STARTED,loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.RouteStarted"));
        eventMap.put(Workspace.EVENT_ROUTE_COMPLETED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.RouteCompleted"));
        eventMap.put(Workspace.EVENT_FOLDER_CREATED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.TopicCreated"));
        eventMap.put(Workspace.EVENT_FOLDER_DELETED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.TopicDeleted"));
        eventMap.put(Workspace.EVENT_MEMBER_ADDED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.MemberAdded"));
        eventMap.put(Workspace.EVENT_MEMBER_REMOVED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.MemberRemoved"));
        eventMap.put(Workspace.EVENT_FOLDER_CONTENT_MODIFIED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.CategoryModified"));
        eventMap.put(Workspace.EVENT_NEW_DISCUSSION, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.DiscussionSummary.NewDiscussion"));
        return eventMap;
    }

  /**
   * This method returns all route events
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public TreeMap AddRouteEvents(String sLanguage){
        TreeMap eventMap = new TreeMap();
        eventMap.put(Workspace.EVENT_ROUTE_STARTED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.RouteStarted"));
        eventMap.put(Workspace.EVENT_ROUTE_COMPLETED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.RouteCompleted"));
        eventMap.put(Route.EVENT_CONTENT_ADDED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.FileAdded"));
        eventMap.put(Route.EVENT_CONTENT_REMOVED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.FileRemoved"));
        eventMap.put(Route.EVENT_TASK_COMPLETED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.TaskCompleted"));
        eventMap.put(Workspace.EVENT_NEW_DISCUSSION, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.DiscussionSummary.NewDiscussion"));
        return eventMap;
    }

  /**
   * This method returns all topics events
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public TreeMap AddTopicsEvents(String sLanguage){
        TreeMap eventMap = new TreeMap();
        eventMap.put(WorkspaceVault.EVENT_CONTENT_ADDED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.FileAdded"));
        eventMap.put(WorkspaceVault.EVENT_CONTENT_REMOVED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.FileRemoved"));
        eventMap.put(WorkspaceVault.EVENT_FOLDER_CREATED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.TopicCreated"));
        eventMap.put(WorkspaceVault.EVENT_FOLDER_DELETED,loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.TopicDeleted"));
        eventMap.put(WorkspaceVault.EVENT_ROUTE_STARTED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.RouteStarted"));
        eventMap.put(WorkspaceVault.EVENT_ROUTE_COMPLETED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.RouteCompleted"));
        eventMap.put(Workspace.EVENT_NEW_DISCUSSION, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.DiscussionSummary.NewDiscussion"));
        return eventMap;
    }

  /**
   * This method returns all document events
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public TreeMap AddDocumentEvents(String sLanguage){
        TreeMap eventMap = new TreeMap();
        eventMap.put(Document.DELETED,  loc.GetString("emxComponentsStringResource",sLanguage,"emxComponents.DOCUMENTS.Event.Deleted"));
        eventMap.put(Document.REVISED,  loc.GetString("emxComponentsStringResource",sLanguage,"emxComponents.DOCUMENTS.Event.Revised"));
        eventMap.put(Document.CONTENT_MODIFIED,  loc.GetString("emxComponentsStringResource",sLanguage,"emxComponents.DOCUMENTS.Event.Content_Modified"));
        eventMap.put(Document.CONTENT_ADDED,  loc.GetString("emxComponentsStringResource",sLanguage,"emxComponents.DOCUMENTS.Event.Content_Added"));
        eventMap.put(Document.CONTENT_DELETED,  loc.GetString("emxComponentsStringResource",sLanguage,"emxComponents.DOCUMENTS.Event.Content_Deleted"));
        eventMap.put(Document.CONTENT_CHECK_OUT,  loc.GetString("emxComponentsStringResource",sLanguage,"emxComponents.DOCUMENTS.Event.Content_Checkout"));
        eventMap.put(Document.DOCUMENT_MODIFIED,  loc.GetString("emxComponentsStringResource",sLanguage,"emxComponents.DOCUMENTS.Event.Document_Modified"));
        eventMap.put(Document.EVENT_FILE_CHECK_OUT,  loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.FileCheckOut"));
        eventMap.put(Document.EVENT_FILE_CHECKED_IN, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.FileCheckedIn"));
        eventMap.put(Document.EVENT_ROUTE_STARTED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.RouteStarted"));
        eventMap.put(Document.EVENT_ROUTE_COMPLETED, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.RouteCompleted"));
        eventMap.put(Workspace.EVENT_NEW_DISCUSSION, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.DiscussionSummary.NewDiscussion"));
        return eventMap;
    }

  /**
   * This method returns all discussion events
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public TreeMap AddDiscussionEvents(String sLanguage){
        TreeMap eventMap = new TreeMap();
        eventMap.put(MessageHolder.EVENT_NEW_DISCUSSION, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.NewDiscussion"));
        return eventMap;
    }

  /**
   * This method returns all message events
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
    public TreeMap AddMessageEvents(String sLanguage){
        TreeMap eventMap = new TreeMap();
        eventMap.put(Message.EVENT_NEW_REPLY, loc.GetString("emxTeamCentralStringResource",sLanguage,"emxTeamCentral.EventFrmwrk.NewReply"));
        return eventMap;
    }

  /**
   * This method returns names of subscribed items (to be used for column "Item")
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since TC 10-7-0-0 next
   */
    public Vector getSubscriptionItems(Context context,String args[])  throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        MapList objectList=(MapList)programMap.get("objectList");
        Vector SubscriptionItems=new Vector();
        // Get the 'Hashtable' instances from 'templateMapList' object
        Hashtable eachHashtable ;
        ListIterator templateIterator=objectList.listIterator() ;
        for ( ; templateIterator.hasNext() ; SubscriptionItems.add(eachHashtable.get("name")) )  {
            eachHashtable = (Hashtable) templateIterator.next();
        }
        return SubscriptionItems ;
    }

  /**
   * This method returns types of subscribed items (to be used for column "Type")
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since TC 10-7-0-0 next
   */
    public Vector getSubscriptionTypes(Context context,String args[])  throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        MapList objectList=(MapList)programMap.get("objectList");
        Vector SubscriptionTypes=new Vector();
        // Get the 'Hashtable' instances from 'templateMapList' object
        Hashtable eachHashtable ;
        ListIterator templateIterator=objectList.listIterator() ;
        for ( ; templateIterator.hasNext() ; SubscriptionTypes.add(eachHashtable.get("type")) )  {
            eachHashtable = (Hashtable) templateIterator.next();
        }
        return SubscriptionTypes ;
    }

  /**
   * This method returns events of subscribed items (to be used for column "Event")
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since TC 10-7-0-0 next
   */
    public Vector getSubscriptionEvents(Context context,String args[])  throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        MapList objectList=(MapList)programMap.get("objectList");
        Vector SubscriptionEvents=new Vector();
        // Get the 'Hashtable' instances from 'templateMapList' object
        Hashtable eachHashtable ;
        ListIterator templateIterator=objectList.listIterator() ;
        for ( ; templateIterator.hasNext() ; SubscriptionEvents.add(eachHashtable.get("event")) )  {
            eachHashtable = (Hashtable) templateIterator.next();
        }
        return SubscriptionEvents ;
    }

  /**
   * This method determines whether checkboxes are enabled or disabled
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since TC 10.7-0-0 next
   */
    public Vector showCheckbox(Context context,String args[]) throws Exception
    {
        Vector CheckboxStates=new Vector();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        MapList objectList=(MapList)programMap.get("objectList");
        Hashtable eachHashtable ;
        String relName ;
        ListIterator templateIterator=objectList.listIterator() ;
        while ( templateIterator.hasNext() )  {
            eachHashtable = (Hashtable) templateIterator.next();
            relName = (String)eachHashtable.get("relName");
            if ( relName.equals(DomainConstants.RELATIONSHIP_PUSHED_SUBSCRIPTION) )
            {
                CheckboxStates.add("false");
            }
            else
            {
                CheckboxStates.add("true");
            }
        }
        return CheckboxStates ;
    }

 // End of class definition
    /**
     * getSubscribableEventsOnFolder - method to return the list of events subscribed by the user on folder
     * @param context the eMatrix <code>Context</code> object
     * @return void
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */  
    public String getSubscribableEventsOnFolder(Context context, String[] args) throws Exception
    {
        //preload strings
        String sTypeDocument        = PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");// Bug No: 298129
        String sTypeDiscussion      = PropertyUtil.getSchemaProperty(context,"type_Thread");
        String sTypeTopic           = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
        String sTypeProject         = PropertyUtil.getSchemaProperty(context,"type_Project");
        String sTypeRoute           = PropertyUtil.getSchemaProperty(context,"type_Route");
        String sTypeMessage         = PropertyUtil.getSchemaProperty(context,"type_Message");
        TreeMap projectMap    = new TreeMap();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap paramMap=(HashMap)programMap.get("paramMap");
        String sObjId = (String)paramMap.get("objectId");
        sLanguage = (String)paramMap.get("languageStr");
        //Open the  object and get object type & name
        BusinessObject boAny    = new BusinessObject(sObjId);
        boAny.open(context);
        String sObjectTypeStr   = boAny.getTypeName();
//       Bug No : 298129 
        Vault vault = new Vault(context.getVault().getName());
        String BaseType= FrameworkUtil.getBaseType(context,sObjectTypeStr,vault);
       // upto here
        //Create a TreeMap & populate the map with the appropriate type of events
        if (sTypeProject.equals(sObjectTypeStr)) {
          projectMap      = AddProjectEvents(sLanguage);

        } else if (sTypeRoute.equals(sObjectTypeStr)) {
          projectMap      = AddRouteEvents(sLanguage);

        } else if (sTypeTopic.equals(sObjectTypeStr)) {
          projectMap      = AddTopicsEvents(sLanguage);

        } else if (BaseType.equals(sTypeDocument) && BaseType != null && !"".equals(BaseType)) {
          projectMap      = AddDocumentEvents(sLanguage);

        } else if (sTypeDiscussion.equals(sObjectTypeStr)) {
          projectMap      = AddDiscussionEvents(sLanguage);
        }
        else if (sTypeMessage.equals(sObjectTypeStr))
        {
          projectMap = AddMessageEvents(sLanguage);
        }
    
        StringBuffer sb = new StringBuffer();
        TreeMap hashSubEvt    = getAllObjectEvents(context, sObjId);
        java.util.Set hashSubEvntKeys = hashSubEvt.keySet();

        Iterator itEvents     = hashSubEvntKeys.iterator();
        String sKeyRemoveStr    = "";

        while (itEvents.hasNext()) {
          sKeyRemoveStr     = (String) itEvents.next();
          //sb.append("<script language=\"javascript\" src=\"../components/emxComponentsUIFormValidation.js\"></script>");
          sb.append("<input type=\"checkbox\" id=chkUnSubscribeEvent name=chkUnSubscribeEvent checked onclick=\"javascript:unSubscribeEvent()\"");
          sb.append(" value=\"");
          sb.append(XSSUtil.encodeForHTMLAttribute(context,(String)hashSubEvt.get(sKeyRemoveStr)));
          sb.append("\"/>");
          sb.append(XSSUtil.encodeForHTML(context,(String)projectMap.get(sKeyRemoveStr)));
          sb.append("<br>");
          projectMap.remove(sKeyRemoveStr);
                    
    }
        sb.append("<input type=hidden name=\"sUnsubscribedEventIds\" value=\"\">");
        java.util.Set projectKeys   = projectMap.keySet();
        Iterator it       = projectKeys.iterator();
        String sKeyStr    = "";

        while (it.hasNext()) {
          sKeyStr       = (String) it.next();
          sb.append("<input type=\"checkbox\"  value=\""+XSSUtil.encodeForHTMLAttribute(context, sKeyStr)+"\"  name=chkSubscribeEvent>"+XSSUtil.encodeForHTML(context,(String)projectMap.get(sKeyStr))+"<br>");
          }
        return sb.toString();
  }
    
    /**
     * getAllObjectEvents - method to return the list of events on an object for subscription
     * @param context the eMatrix <code>Context</code> object
     * @return void
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */  
    public TreeMap getAllObjectEvents(matrix.db.Context context, String objectId) throws MatrixException
    {
      TreeMap eventList     = new TreeMap();
      Hashtable evtProjMemHash  = getAllPersonEvents(context);
      //matrix.db.Context context = getPageContext();

      String newPSName    = null;
      String newEventName   = null;


      //Preload lookup strings
      String sTypeEvent       = PropertyUtil.getSchemaProperty(context,"type_Event");
      String sTypePubSubscribe    = PropertyUtil.getSchemaProperty(context,"type_PublishSubscribe");
      String sRelPublish    = PropertyUtil.getSchemaProperty(context,"relationship_Publish");
      String sRelSubscribePerson  = PropertyUtil.getSchemaProperty(context,"relationship_SubscribedPerson");
      String sRelSubscribeItem    = PropertyUtil.getSchemaProperty(context,"relationship_SubscribedItem");
      String sRelPublishSubscribe = PropertyUtil.getSchemaProperty(context,"relationship_PublishSubscribe");
      String sPolicyPubSubscribe  = PropertyUtil.getSchemaProperty(context,"policy_PublishSubscribe");
      String sPolicyEvent       = PropertyUtil.getSchemaProperty(context,"policy_Event");
      String sAttrEventType     = PropertyUtil.getSchemaProperty(context,"attribute_EventType");

      Pattern relPattern    = new Pattern(sRelPublishSubscribe);
      Pattern typePattern   = new Pattern(sTypePubSubscribe);

      BusinessObject boAny    = new BusinessObject(objectId);
      boAny.open(context);

      //Build select params
      SelectList selectStmts  = new SelectList();
      selectStmts.addName();
      selectStmts.addId();

      //Build select params for Relationship
      SelectList selectRelStmts   = new SelectList();

      ExpansionWithSelect expandWSelectAny = boAny.expandSelect(context,relPattern.getPattern(),typePattern.getPattern(),
                                               selectStmts, selectRelStmts,true, true, (short)1);

      BusinessObject boPS   = null;

      RelationshipWithSelectItr relWSelectAnyItr = new RelationshipWithSelectItr(expandWSelectAny.getRelationships());

      Hashtable anyHash     = new Hashtable();
      Hashtable psHash    = new Hashtable();
      String sPSId    = null;

      //If the PS object doesn't exist then create PS & Event Objects
      if (relWSelectAnyItr != null && relWSelectAnyItr.next()) {
        anyHash       = relWSelectAnyItr.obj().getTargetData();
        sPSId       = (String) anyHash.get("id");
        boPS      = new BusinessObject(sPSId);
        boPS.open(context);

        Pattern relPSPattern    = new Pattern(sRelPublish);
        Pattern typePSPattern   = new Pattern(sTypeEvent);

        ExpansionWithSelect expandWSelectPS = boPS.expandSelect(context,relPSPattern.getPattern(),typePSPattern.getPattern(),
                                                           selectStmts,selectRelStmts,true, true, (short)1);

        RelationshipWithSelectItr relWSelectPSItr = new RelationshipWithSelectItr(expandWSelectPS.getRelationships());
        BusinessObject boEvent  = null;
        boolean eventFoundBool  = false;

        while (!eventFoundBool && relWSelectPSItr.next()) {
          psHash      = relWSelectPSItr.obj().getTargetData();
          String sEventId   = (String) psHash.get("id");
          boEvent     = new BusinessObject(sEventId);
          boEvent.open(context);

          String eventTypeStr = "";
          AttributeItr  attrItr   = new AttributeItr(boEvent.getAttributes(context).getAttributes());
          AttributeList attrList  = new AttributeList();

          while (attrItr.next()){
            Attribute attr  = attrItr.obj();

            if (attr.getName().equals(sAttrEventType)) {
              eventTypeStr  = attr.getValue();

              if (evtProjMemHash.get(sEventId) != null) {
                eventList.put(eventTypeStr, sEventId);
              }
            }
          }
        }
      }
      return eventList;
    }
    /**
     * updateSubscribableEventsOnFolder - method to return the list of events unsubscribed by the user on folder
     * @param context the eMatrix <code>Context</code> object
     * @return void
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */  
    @com.matrixone.apps.framework.ui.PostProcessCallable
  public void updateSubscribableEventsOnFolder(Context context, String[] args) throws Exception
  {
      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      HashMap paramMap=(HashMap)programMap.get("paramMap");
      HashMap requestValuesMap=(HashMap)programMap.get("requestValuesMap");
      String[] saObjectKey    = (String[])requestValuesMap.get("chkSubscribeEvent");
      String sObjectId        = (String)paramMap.get("objectId");
      String sUnsubEvtIds     = (String)paramMap.get("sUnsubscribedEventIds");
      String flag             = (String)paramMap.get("flag");
      String[] personIds      = (String[])paramMap.get("chkItem1");


      //Preload lookup strings
      String sTypeEvent            = PropertyUtil.getSchemaProperty(context,"type_Event");
      String sTypePublishSubscribe = PropertyUtil.getSchemaProperty(context,"type_PublishSubscribe");
      String strProjectType        = PropertyUtil.getSchemaProperty(context,"type_Project");
      String strRouteType          = PropertyUtil.getSchemaProperty(context,"type_Route");
      String strContentType        = PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");
      String strPackageType        = PropertyUtil.getSchemaProperty(context,"type_Package");
      String strRFQType            = PropertyUtil.getSchemaProperty(context,"type_RequestToSupplier");
      String strFolderType         = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
      String strMessage            = PropertyUtil.getSchemaProperty(context,"type_Message");
      String strThread             = PropertyUtil.getSchemaProperty(context,"type_Thread");

      String sRelPublish           = PropertyUtil.getSchemaProperty(context,"relationship_Publish");
      String sRelSubscribePerson   = PropertyUtil.getSchemaProperty(context,"relationship_SubscribedPerson");
      String sRelPushSubscribePerson = PropertyUtil.getSchemaProperty(context,"relationship_PushedSubscription");
      String sRelSubscribeItem     = PropertyUtil.getSchemaProperty(context,"relationship_SubscribedItem");
      String sRelPublishSubscribe  = PropertyUtil.getSchemaProperty(context,"relationship_PublishSubscribe");
      String strRouteRel           = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute" );
      String strDocumentRel        = PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocuments" );
      String strProjectFolderRel   = PropertyUtil.getSchemaProperty(context,"relationship_ProjectFolders");
      String strProjectVault       = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults" );
      String strRouteScope         = PropertyUtil.getSchemaProperty(context,"relationship_RouteScope");
      String relRouteScope         = PropertyUtil.getSchemaProperty(context,"relationship_RouteScope");
      String relThread             = PropertyUtil.getSchemaProperty(context,"relationship_Thread");
      String relMessage            = PropertyUtil.getSchemaProperty(context,"relationship_Message");
      String sAttrEventType        = PropertyUtil.getSchemaProperty(context,"attribute_EventType");
      String strProjectVaultType   = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
      String strVaultedDocumentsRel= PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocuments");

      DomainObject domainObject=DomainObject.newInstance(context);
      Workspace workspace = (Workspace)DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE, DomainConstants.TEAM);
   
      ExpansionWithSelect projectSelect = null;
      RelationshipWithSelectItr relItr  = null;

      SelectList selectStmts     = new SelectList();
      SelectList selectRelStmts  = new SelectList();
      selectStmts.addName();
      selectStmts.addId();

      Pattern relPattern  = null;
      Pattern typePattern = null;

      BusinessObject boEvent      = null;
      BusinessObject ProjectObj   = null;
      BusinessObject ConnectedObj = null;
      String sProjectId             = null;
      String threadId                   ="";

      //Open the  object and get object type
      BusinessObject boAny    = new BusinessObject(sObjectId);
      boAny.open(context);
      String sObjectTypeStr   = boAny.getTypeName();
     Vault vault = new Vault(context.getVault().getName());
      String BaseType= FrameworkUtil.getBaseType(context,sObjectTypeStr,vault);
      if(sObjectTypeStr.equals(strMessage)){

                threadId        = getThreadId(context,sObjectId);
                BusinessObject threadObj = new BusinessObject(threadId);
                threadObj.open(context);
                relPattern  = new Pattern(relThread);
                typePattern = new Pattern(strProjectType);
                if(strRFQType != null && !strRFQType.equals(null) && !strRFQType.equals("null") && !strRFQType.equals(""))
                {   
                  typePattern.addPattern(strRFQType);
                }
                ConnectedObj = getConnectedObject(context,threadObj,relPattern.getPattern(),typePattern.getPattern(),true,false);
                // If project Id is not null then the page is from workspace
                if ( ConnectedObj != null ) {
                    sProjectId  = ConnectedObj.getObjectId();
                }
                 
                // folder type
                typePattern = new Pattern(strFolderType);
                relPattern  = new Pattern(strVaultedDocumentsRel);

                ConnectedObj = getConnectedObject(context,threadObj,relPattern.getPattern(),typePattern.getPattern(),true,false);
                // If folder Id is not null then the page is from folder
                if ( ConnectedObj != null ) {
                    String FolderId = ConnectedObj.getObjectId();
                    sProjectId = getProjectId(context,FolderId);

                }

                // Content type
                typePattern = new Pattern(strContentType);
                ConnectedObj = getConnectedObject(context,threadObj,relPattern.getPattern(),typePattern.getPattern(),true,false);

                // If content Id is not null then the page is from content
                if ( ConnectedObj != null ) {
                        String contentId = ConnectedObj.getObjectId();

                        Pattern RelPattern       = new Pattern(strVaultedDocumentsRel);
                        Pattern TypePattern      = new Pattern(strProjectVaultType);
                        BusinessObject boContent = new BusinessObject(contentId);
                        BusinessObject boGeneric = getConnectedObject(context,boContent,RelPattern.getPattern(),TypePattern.getPattern(),true,false);
                        sProjectId   = getProjectId(context, boGeneric.getObjectId());
                }
                //bomsg.close(context);
                threadObj.close(context);

      }

      if (sObjectTypeStr.equals(strRouteType)) {
                BusinessObject boRoute = new BusinessObject(sObjectId);
                boRoute.open(context);

                // to get the folder Id from route Id
                relPattern  = new Pattern(strRouteScope);
                typePattern = new Pattern(strProjectType);

                ProjectObj = getConnectedObject(context,boRoute,relPattern.getPattern(),typePattern.getPattern(),true,false);

                // If project Id is not null then the page is from workspace
                if ( ProjectObj != null ) {

                    sProjectId = ProjectObj.getObjectId();
                }

                // If the projectId is null then get the project Id from Workspace Vault.
                if ( ProjectObj == null ) {
                    relPattern   = new Pattern(relRouteScope);
                    typePattern  = new Pattern(strFolderType);
                    ProjectObj   = getConnectedObject(context,boRoute,relPattern.getPattern(),typePattern.getPattern(),true,false);
                    sProjectId   = getProjectId(context,ProjectObj.getObjectId());
                }
                boRoute.close(context);

      } else if (sObjectTypeStr.equals(strProjectType)) {
            sProjectId = sObjectId;

      } else if (BaseType.equals(strContentType) || sObjectTypeStr.equals(strPackageType) || sObjectTypeStr.equals(strRFQType)) {

                BusinessObject boContent = new BusinessObject(sObjectId);
                boContent.open(context);
                BusinessObject boGeneric = null;
                // to get the project id from workspace vault.
                relPattern  = new Pattern(strDocumentRel);
                typePattern = new Pattern(strFolderType);

                boGeneric = getConnectedObject(context,boContent,relPattern.getPattern(),typePattern.getPattern(),true,false);
                // If project Id is not null then the page is from workspace
                if ( boGeneric != null ) {
                    sProjectId = getProjectId(context,boGeneric.getObjectId());
                }

                if ( boGeneric == null ) {
                    // to get the project id from workspace vault.
                    relPattern  = new Pattern(strRouteRel);
                    typePattern = new Pattern(strRouteType);

                    boGeneric = getConnectedObject(context,boContent,relPattern.getPattern(),typePattern.getPattern(),true,false);

                    // to get the project id from workspace vault.
                    relPattern  = new Pattern(strRouteRel);
                    typePattern = new Pattern(strProjectType);

                    boGeneric  = getConnectedObject(context,boContent,relPattern.getPattern(),typePattern.getPattern(),true,false);
                    sProjectId = boGeneric.getObjectId();
                }
                boContent.close(context);

      } else if (sObjectTypeStr.equals(strFolderType)) {

        // to get the project id from workspace folder.
        //This takes care of workspace sub folders to n level.

        try {
              sProjectId = getProjectId(context,sObjectId);
        } catch (MatrixException ex) {
        }

      }


      int i, j;
      String sProjectVault = null;

      //To get the Vault
      if(sProjectId != null)
      {
        BusinessObject busProject = new BusinessObject(sProjectId);
        busProject.open(context);
        sProjectVault = busProject.getVault();
        busProject.close(context);

        if (sUnsubEvtIds != null && !"".equals(sUnsubEvtIds)) {
            StringTokenizer st = new StringTokenizer(sUnsubEvtIds, ";");

            while (st.hasMoreTokens()) {
          //Process unsubscribed events and disconnect relationships

            boEvent = new BusinessObject(st.nextToken());
            boEvent.open(context);

            BusinessObject boPerson = Person.getPerson(context);
            boPerson.open(context);
            boPerson.disconnect(context, new RelationshipType(sRelSubscribePerson), false, boEvent);
            boPerson.close(context);
        }
      }
     }
      //Process subscribed events, create objects and connect relationships
      try {
      if (saObjectKey != null) {
        boAny = new BusinessObject(sObjectId);
        boAny.open(context);
        sObjectTypeStr = boAny.getTypeName();
        if(sProjectVault == null){
          sProjectVault = boAny.getVault();
        }  
        for (i = 0; i < saObjectKey.length; i++) {

          relPattern = new Pattern(sRelPublishSubscribe);
          typePattern = new Pattern(sTypePublishSubscribe);

          ExpansionWithSelect expandWSelectAny = boAny.expandSelect(context,relPattern.getPattern(),typePattern.getPattern(),
                                                 selectStmts, selectRelStmts,true, true, (short)1);
          BusinessObject boPS = null;
          RelationshipWithSelectItr relWSelectAnyItr = new RelationshipWithSelectItr(expandWSelectAny.getRelationships());

          Hashtable anyHash = new Hashtable();
          Hashtable psHash  = new Hashtable();
          String sPSId = null;
          boolean bFlag = false;
          BusinessObject busObj = null;
          //Check if the PS object exist get data & open object
          if (relWSelectAnyItr  != null && relWSelectAnyItr.next()) {
            anyHash = relWSelectAnyItr.obj().getTargetData();
            sPSId   = (String) anyHash.get("id");
            boPS    = new BusinessObject(sPSId);
            boPS.open(context);

            //If the PS object does not exist create it
          } else {
            //sPSId = autoName(context, session,"type_PublishSubscribe", "", "policy_PublishSubscribe", sProjectVault);
            busObj = new BusinessObject ( FrameworkUtil.autoName (context,
                    "type_PublishSubscribe",
                    "",
                    "policy_PublishSubscribe",
                    sProjectVault) );
            sPSId = busObj.getObjectId();
            boPS  = new BusinessObject(sPSId);
            boPS.open(context);
            //Use SubscriptionManager.initiate() method to connect Publish Subscribe Object thru Super user

            workspace.setId(sProjectId);
            workspace.getSubscriptionManager().initiate(context, sObjectId, sPSId);
          }

          Pattern relPSPattern    = new Pattern(sRelPublish);
          Pattern typePSPattern   = new Pattern(sTypeEvent);

          ExpansionWithSelect expandWSelectPS = boPS.expandSelect(context,relPSPattern.getPattern(),typePSPattern.getPattern(),
                                                           selectStmts,selectRelStmts,true, true, (short)1);
          RelationshipWithSelectItr relWSelectPSItr = new RelationshipWithSelectItr(expandWSelectPS.getRelationships());

          boolean bEventFound   = false;

          //Check if the Event object exist get data & open object
          while (!bEventFound && relWSelectPSItr.next()) {
            psHash = relWSelectPSItr.obj().getTargetData();
            String sEventId = (String) psHash.get("id");
            boEvent = new BusinessObject(sEventId);
            boEvent.open(context);

            String sEventTypeStr   = "";
            AttributeItr  attrItr  = new AttributeItr(boEvent.getAttributes(context).getAttributes());
            AttributeList attrList = new AttributeList();

            while (attrItr.next()){
              Attribute attr = attrItr.obj();

              if (attr.getName().equals(sAttrEventType)) {
                sEventTypeStr = attr.getValue();

                if (sEventTypeStr.equals(saObjectKey[i])) {
                  bEventFound = true;
                }
              }
            }
          }
          //If the event object does not exist create it
          if (!bEventFound) {
            //String sEventId = autoName(context, session, "type_Event", "", "policy_Event", sProjectVault);
              String sEventId = (new BusinessObject ( FrameworkUtil.autoName (context,
                                "type_Event",
                                "",
                                "policy_Event",
                                sProjectVault) )).getObjectId();
            boEvent = new BusinessObject(sEventId);
            boPS.connect(context, new RelationshipType(sRelPublish), true, boEvent);
            boEvent.open(context);

            //Set EventType Attribute
            AttributeItr  attrItr  = new AttributeItr(boEvent.getAttributes(context).getAttributes());
            AttributeList attrList = new AttributeList();

            while (attrItr.next()) {
              Attribute attr  = attrItr.obj();
              if (attr.getName().equals(sAttrEventType)) {
                attr.setValue(saObjectKey[i]);
                attrList.addElement(attr);
              }
            }
            boEvent.setAttributes(context, attrList);
          }

          if(flag == null || !flag.equals("pushSubscription"))
          {
            //After getting Person connectEvent with Person
            BusinessObject boPerson = Person.getPerson(context);
            boPerson.open(context);
            boEvent.connect(context, new RelationshipType(sRelSubscribePerson), true, boPerson);
            boPerson.close(context);

          }else{
            for(int count=0;count<personIds.length;count++)
            {
              BusinessObject boPerson = null;
              StringTokenizer tokenizer = new StringTokenizer(personIds[count],"~");
              String typeVar = tokenizer.nextToken();
              if(typeVar!=null && typeVar.equals("Person")){
                //After getting Person connectEvent with Person
                boPerson = new BusinessObject(tokenizer.nextToken());
              }
              boPerson.open(context);
              try {
                boEvent.connect(context, new RelationshipType(sRelPushSubscribePerson), true, boPerson);
              }catch (Exception e){
                 bFlag = true;
              }
                        boPerson.close(context);
                    }
          }
        boEvent.close(context);
       }
      }
     }catch(Exception e)
     {
       e.printStackTrace();
     }
  }
  /**
   * getConnectedObject - method to return the connected object of a type based on a relationship
   * @param context the eMatrix <code>Context</code> object
   * @return BusinessObject
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */ 
  
  static public BusinessObject getConnectedObject(Context context,
          BusinessObject object, String relPattern, String typePattern,
          boolean getTo, boolean getFrom) throws MatrixException {

    // Initialize the connected business object.
    BusinessObject connectedObject = null;

    // Open the object, if necessary.
    boolean closeConnection = false;
    if (object.isOpen() == false) {
      object.open(context);
      closeConnection = true;
    }

    // Define the business object select clause.
    SelectList busSelects = new SelectList();
    busSelects.addId();

    // Expand the object to get the connected object id.
    ExpansionWithSelect ews = object.expandSelect(context, relPattern,
            typePattern, busSelects, new SelectList(), getTo, getFrom,
            (short) 1);

    // If there is a connected object, create it from the id.
    RelationshipWithSelectItr relItr =
            new RelationshipWithSelectItr(ews.getRelationships());
    if (relItr.next()) {
      String objectId = (String) relItr.obj().getTargetData().get("id");
      connectedObject = new BusinessObject(objectId);
    }

    // Close the object, if necessary.
    if (closeConnection) {
      object.close(context);
    }

    // Return the connected object.
    return connectedObject;

  }
  
  /**
   * getProjectId - Get ProjectId by passing Project Vault Id. This will fetch ProjectId for sub Project vaults upto n level
   * @param context the eMatrix <code>Context</code> object
   * @return String
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */ 
  static public String getProjectId(matrix.db.Context context, String folderId) throws MatrixException {
    String strProjectVault = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults" );
    String strProjectType  = PropertyUtil.getSchemaProperty(context,"type_Project");
    String strSubVaultsRel = PropertyUtil.getSchemaProperty(context,"relationship_SubVaults");
    String strProjectVaultType  = PropertyUtil.getSchemaProperty(context,"type_ProjectVault" );

    //com.matrixone.framework.beans.DomainObject domainObject = new com.matrixone.framework.beans.DomainObject();
    DomainObject domainObject = DomainObject.newInstance(context);
    domainObject.setId(folderId);

    String projectId = "";

    Pattern relPattern  = new Pattern(strProjectVault);
    relPattern.addPattern(strSubVaultsRel);
    Pattern typePattern = new Pattern(strProjectType);
    typePattern.addPattern(strProjectVaultType);

    Pattern includeTypePattern = new Pattern(strProjectType);

    StringList objSelects = new StringList();
    objSelects.addElement(domainObject.SELECT_ID);
    //need to include Type as a selectable if we need to filter by Type
    objSelects.addElement(domainObject.SELECT_TYPE);

    ExpansionWithSelect projectSelect = null;
    RelationshipWithSelectItr relItr  = null;

    domainObject.open(context);
    MapList mapList = domainObject.getRelatedObjects(context,
                                             relPattern.getPattern(),
                                             typePattern.getPattern(),
                                             objSelects,
                                             null,
                                             true,
                                             false,
                                             (short)0,
                                             "",
                                             "",
                                             includeTypePattern,
                                             null,
                                             null);

    Iterator mapItr = mapList.iterator();
    while(mapItr.hasNext())
    {
      Map map = (Map)mapItr.next();
      projectId = (String) map.get(domainObject.SELECT_ID);
    }
    return projectId;
  } 
  /**
   * getThreadId - method to return the object id of the message object
   * @param context the eMatrix <code>Context</code> object
   * @return String
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */ 
  static public String getThreadId(matrix.db.Context context, String MsgId) throws MatrixException {
      String strReplyRel    = PropertyUtil.getSchemaProperty(context,"relationship_Reply" );
      String strMsgType     = PropertyUtil.getSchemaProperty(context,"type_Message");
      String strMsgRel      = PropertyUtil.getSchemaProperty(context,"relationship_Message");
      String strThreadType  = PropertyUtil.getSchemaProperty(context,"type_Thread" );

      String sThreadId = null;
      BusinessObject boGeneric  = new BusinessObject(MsgId);
      BusinessObject ThreadObj = null;

      boGeneric.open(context);

      Pattern relPattern  = new Pattern(strMsgRel);
      relPattern.addPattern(strReplyRel);
      Pattern typePattern = new Pattern(strMsgType);
      typePattern.addPattern(strThreadType);
      SelectList selectStmts = new SelectList();
      selectStmts.addName();
      selectStmts.addDescription();

      SelectList selectRelStmts = new SelectList();

      ExpansionWithSelect projectSelect = null;
      RelationshipWithSelectItr relItr  = null;

      projectSelect = boGeneric.expandSelect(context,relPattern.getPattern(),typePattern.getPattern(), selectStmts, selectRelStmts,true,false,(short)0);

      relItr = new RelationshipWithSelectItr(projectSelect.getRelationships());

      // loop thru the rels and get the folder object
      while (relItr != null && relItr.next()) {

        if (relItr.obj().getTypeName().equals(strMsgRel)) {
          ThreadObj = relItr.obj().getFrom();

        }
      }
      return ThreadObj.getObjectId();
  }
  
  /**
   * getProjectMember - method to return the person connected as project member to workspace
   * @param context the eMatrix <code>Context</code> object
   * @return BusinessObject
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   * @deprecated since BPS R216 and can be removed after 2 releases
   */ 
  
  @Deprecated
  public static synchronized BusinessObject getProjectMember( matrix.db.Context context, String projectId , BusinessObject person) throws MatrixException
  {
    Hashtable personHashtable = null;

    if ( projectMemberHashtable != null ) {
      personHashtable = (Hashtable)projectMemberHashtable.get(person.getObjectId());
      if (personHashtable != null) {
        BusinessObject personMemberObj = (BusinessObject)personHashtable.get(projectId);
        if (personMemberObj != null) {
          return new BusinessObject(personMemberObj);
        }
      } else {
        personHashtable = new Hashtable();
        projectMemberHashtable.put(person.getObjectId(), personHashtable);
      }
    } else {
      projectMemberHashtable = new Hashtable();
      personHashtable = new Hashtable();
      projectMemberHashtable.put(person.getObjectId(), personHashtable);
     }

    //matrix.db.Context context = getPageContext();

    String sProjectMembershipRel = PropertyUtil.getSchemaProperty(context,"relationship_ProjectMembership");
    String sProjectMembersRel = PropertyUtil.getSchemaProperty(context,"relationship_ProjectMembers");
    String sProjectMemberType = PropertyUtil.getSchemaProperty(context,"type_ProjectMember");
    String sProjectType = PropertyUtil.getSchemaProperty(context,"type_Project");

    String typePattern = sProjectMemberType;
    String relPattern =  sProjectMembershipRel;
    String strWhereClause = "to[" + sProjectMembersRel + "].from.id ==" + projectId;

    ExpansionWithSelect projectSelect = person.expandSelect(context, relPattern, typePattern, EMPTY_STRING_LIST,
                                                            EMPTY_STRING_LIST,false, true, (short)1,strWhereClause,null);

    RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(projectSelect.getRelationships());
    BusinessObject busProjectMember = null;
    if (relItr.next()) {
      busProjectMember = relItr.obj().getTo();
    }

    if (busProjectMember != null ) {
     personHashtable.put(projectId, busProjectMember);
    }

    return busProjectMember;
  }
  /**
   * getPushSubscriptionEventsonFolder - method to return the list of events available for push subscription
   * @param context the eMatrix <code>Context</code> object
   * @return MapList
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */ 
  public MapList getPushSubscriptionEventsonFolder(Context context, String[] args) throws Exception
  {
      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      String languageStr = (String)programMap.get("languageStr");
      String sObjId       = (String)programMap.get("objectId");
      String strDiscussion = (String)programMap.get("discussion");
      String objectFlag = (String)programMap.get("objectFlag");
      MapList objectList = new MapList();
      if (sObjId != null){
        sObjId = sObjId.trim();
      }
      /*10-7-0-0 Conversion Start*/
      if(strDiscussion==null)
      {
        strDiscussion="Discussion";
      }
      /*10-7-0-0 Conversion End*/


      //get object type name
      DomainObject domainObject   = DomainObject.newInstance(context,sObjId);


      String sObjectTypeStr   = domainObject.getType(context);
      String sTypeDocument          = PropertyUtil.getSchemaProperty(context, "type_Document");
      String sTypeEvent             = PropertyUtil.getSchemaProperty(context, "type_Event");
      String sTypePerson            = PropertyUtil.getSchemaProperty(context, "type_Person");
      String sTypeWorkspaceVault    = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
      String sTypeProject           = PropertyUtil.getSchemaProperty(context, "type_Project");
      String sTypeRoute             = PropertyUtil.getSchemaProperty(context, "type_Route");
      String sTypeMessage           = PropertyUtil.getSchemaProperty(context, "type_Message");
//    /Commented for IR-011752V6R2011
     /* if ("Discussion".equals(strDiscussion)) {
           sObjectTypeStr = "Discussion";
      }*/

      //Create a TreeMap & populate the map with the appropriate type of events
      TreeMap projectMap    = new TreeMap();
      if(sObjectTypeStr.equals("Discussion")) {

        projectMap = AddDiscussionEvents(languageStr);

      } else if (sTypeProject.equals(sObjectTypeStr)) {

        projectMap = AddProjectEvents(languageStr);

      } else if (sTypeRoute.equals(sObjectTypeStr)) {

        projectMap = AddRouteEvents(languageStr);

      } else if (sTypeWorkspaceVault.equals(sObjectTypeStr)) {

        projectMap = AddTopicsEvents(languageStr);

      } else if (sTypeDocument.equals(sObjectTypeStr)) {

        projectMap = AddDocumentEvents(languageStr);

      } else if (sTypeMessage.equals(sObjectTypeStr)) {

        projectMap = AddMessageEvents(languageStr);

      }

      if(objectFlag != null && objectFlag.equals("discussion")) {
        projectMap = AddDiscussionEvents(languageStr);
      }
      objectList.add(projectMap);
      
      return objectList;
  }
  /**
   * getAlertEvents - method to return the list of events available for subscription
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */ 
  public Vector getAlertEvents(Context context, String[] args) throws Exception
  {
      Vector alertEvents = new Vector();
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      Iterator it = objectList.iterator();
      while (it.hasNext()) {
        Map projectMap = (TreeMap)it.next();  
        java.util.Set projectKeys   = projectMap.keySet();
        Iterator itr = projectKeys.iterator();
        String sKeyStr = "";
        while (itr.hasNext()) {
          sKeyStr = (String) itr.next();
          alertEvents.add(XSSUtil.encodeForHTML(context, (String)projectMap.get(sKeyStr)));
        }
      }
      return alertEvents;
  }
  /**
   * getRecipientsOnEvents - method to return the list of recipients for an event subscription
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */ 
  public Vector getRecipientsOnEvents(Context context, String[] args) throws Exception
  {
      Vector recipientsOnEvents = new Vector();
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      HashMap paramList  = (HashMap)programMap.get("paramList");
      String sObjId      = (String)paramList.get("objectId");
      String strDiscussion = (String)paramList.get("discussion");
      Iterator it = objectList.iterator();
     
      com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
      
      if (sObjId != null){
        sObjId = sObjId.trim();
      }
      /*10-7-0-0 Conversion Start*/
      if(strDiscussion==null)
      {
        strDiscussion="Discussion";
      }
      /*10-7-0-0 Conversion End*/
      person.setToContext(context);
      

      //Get ProjectID
      String sProjectId = "";
      DomainObject domainObject   = DomainObject.newInstance(context,sObjId);

      //preload strings
      String sTypeDocument          = PropertyUtil.getSchemaProperty(context, "type_Document");
      String sTypeEvent             = PropertyUtil.getSchemaProperty(context, "type_Event");
      String sTypePerson            = PropertyUtil.getSchemaProperty(context, "type_Person");
      String sTypeWorkspaceVault    = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
      String sTypeProject           = PropertyUtil.getSchemaProperty(context, "type_Project");
      String sTypeRoute             = PropertyUtil.getSchemaProperty(context, "type_Route");
      String sTypeMessage           = PropertyUtil.getSchemaProperty(context, "type_Message");

      String sRelPushedSubscription = PropertyUtil.getSchemaProperty(context, "relationship_PushedSubscription");
      String sRelPublish            = PropertyUtil.getSchemaProperty(context, "relationship_Publish");
      String sRelPublishSubscribe   = PropertyUtil.getSchemaProperty(context, "relationship_PublishSubscribe");
      String sRelRouteScope         = PropertyUtil.getSchemaProperty(context, "relationship_RouteScope");
      String sRelWorkspaceVaults    = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");
      String sRelVaultedObjects     = PropertyUtil.getSchemaProperty(context, "relationship_VaultedDocuments");
      String sAttrEventType         = PropertyUtil.getSchemaProperty(context, "attribute_EventType");
      String sObjectTypeStr   = domainObject.getType(context);
      String SELECT_ROUTE_SCOPE_ID = "to[" + sRelRouteScope + "].from.id";
      String SELECT_ROUTE_SCOPE_TYPE = "to[" + sRelRouteScope + "].from.type";

      String SELECT_FOLDER_PROJECT_ID = "to[" + sRelWorkspaceVaults + "].from.id";

      if(sObjectTypeStr.equals(sTypeProject) || sObjectTypeStr.equals(sTypeMessage) ) {

        sProjectId = sObjId;

      } else if(sObjectTypeStr.equals(sTypeRoute)) {

        if((domainObject.getInfo(context,SELECT_ROUTE_SCOPE_TYPE)).equals(sTypeWorkspaceVault)){
          sProjectId = getProjectId(context, domainObject.getInfo(context,SELECT_ROUTE_SCOPE_ID));
        }
        else {
          sProjectId = domainObject.getInfo(context,SELECT_ROUTE_SCOPE_ID);
        }

      } else if(sObjectTypeStr.equals(sTypeDocument)||(sObjectTypeStr.equals(domainObject.TYPE_PACKAGE)) || (sObjectTypeStr.equals(domainObject.TYPE_RTS_QUOTATION)) || (sObjectTypeStr.equals(domainObject.TYPE_REQUEST_TO_SUPPLIER))) {

        sProjectId = getProjectId(context,domainObject.getInfo(context,"to[" + sRelVaultedObjects + "].from.id"));

      } else if(sObjectTypeStr.equals(sTypeWorkspaceVault)) {

        sProjectId = getProjectId(context,sObjId);

      }

          while (it.hasNext()) {
              Map projectMap = (TreeMap)it.next();  
              java.util.Set projectKeys   = projectMap.keySet();
              Iterator itr = projectKeys.iterator();
              String sKeyStr = "";
              while (itr.hasNext()) {
                sKeyStr = (String) itr.next();
                 
        String whereExpression = "(attribute[" + sAttrEventType + "] == \"" + sKeyStr + "\") && (to[" + sRelPublish + "].from.to[" + sRelPublishSubscribe + "].from.id == \"" + sObjId + "\")";
        MapList subscribedPersons = new MapList();
        StringList objectSelects = new StringList(1);
        objectSelects.add(domainObject.SELECT_ID);

        com.matrixone.apps.common.Person person1 = com.matrixone.apps.common.Person.getPerson(context);
        com.matrixone.apps.common.Company company = person1.getCompany(context);

        MapList subscribedEvent =  domainObject.querySelect(context,
                                                              sTypeEvent, // type pattern
                                                              domainObject.QUERY_WILDCARD, // namePattern
                                                              domainObject.QUERY_WILDCARD,  // revPattern
                                                              domainObject.QUERY_WILDCARD, // ownerPattern
                                                              company.getAllVaults(context,true), // vault pattern
                                                              whereExpression, // where expression
                                                              true,            // expandType
                                                              objectSelects,   // object selects
                                                              null,            // cached list
                                                              false);          // use cache


        if (subscribedEvent.size() != 0) {

          Iterator i =  subscribedEvent.iterator();
          String eventId = "";

          while( i.hasNext()){
            Map map = (Map)i.next();
            eventId = (String)map.get(domainObject.SELECT_ID);
            domainObject.setId( eventId );
            objectSelects = new StringList();
            subscribedPersons =  domainObject.expandSelect(context,
                                                                  sRelPushedSubscription,
                                                                  sTypePerson,
                                                                  objectSelects,
                                                                  null,
                                                                  true,
                                                                  true,
                                                                  (short)1,
                                                                  null,
                                                                  null,
                                                                  null,
                                                                  false);


            }
        }
        recipientsOnEvents.add("<a href=\"javascript:emxShowModalDialog('emxTeamEditPushSubscriptionDialogFS.jsp?projectId="+sProjectId+"&flag=pushSubscription&chkSubscribeEvent="+sKeyStr+"&objectId="+sObjId+"',550, 500);\" >"+subscribedPersons.size()+"</a>");
      }
     }
    
      return recipientsOnEvents;
  }
  /**
   * deleteSubscriptions - method to delete the subscriptions for a person to an object
   * @param context the eMatrix <code>Context</code> object
   * @return void
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */   
public void deleteSubscriptions(Context context,String args[]) throws Exception
{
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    String objectId = (String) programMap.get("objectId");
    String workspaceId = (String) programMap.get("workspaceId");
    String objId[] = (String[]) programMap.get("objId");
    String sSelectPersonId = (String)programMap.get("sSelectPersonId");
    StringBuffer personBuffer=new StringBuffer();
    Hashtable session = new Hashtable();
    HashMap returnMap =  new HashMap();
    BusinessObject boEvent  = null;
    Pattern relPattern      = null;
    Pattern typePattern     = null;
    relPattern              = new Pattern(DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON);
    typePattern             = new Pattern(DomainConstants.TYPE_PERSON);
    String strLoginPersonId      = "";
    boolean bDisconnect      = false;
  //Process unsubscribed events and disconnect relationships
    for ( int j=0 ; j<objId.length ; j++ )  {
  boEvent                     = new BusinessObject(objId[j]);
  
  BusinessObject boPerson     = com.matrixone.apps.common.Person.getPerson(context);
  boPerson.open(context);

  WorkspaceVault workspaceVault = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT, DomainConstants.TEAM);
  workspaceVault.setId(boEvent.getObjectId());
  StringList SelectList = new StringList();
  SelectList.add(workspaceVault.SELECT_ID);
  strLoginPersonId = boPerson.getObjectId();

MapList mapList = workspaceVault.getRelatedObjects(context,
                  relPattern.getPattern(),
                  typePattern.getPattern(),
                  SelectList, // objectSelects,
                  new SelectList(), // relationshipSelects,
                  false,             // getTo,
                  true,             // getFrom,
                  (short)0,         // recurseToLevel,
                  null,             // objectWhere,
                "");        //relationshipWhere)

for ( int i=0 ; i<mapList.size() ; i++ )  {
  Map tempmap = (Map) mapList.get(i);
  String strobjectId = (String) tempmap.get(workspaceVault.SELECT_ID);
  if(strLoginPersonId.equals(strobjectId)) {
  bDisconnect = true;
  break;
  }
}

   if(bDisconnect){
    boPerson.disconnect(context, new RelationshipType(DomainConstants.RELATIONSHIP_SUBSCRIBED_PERSON), false, boEvent);
  }else {
      boPerson.disconnect(context, new RelationshipType(DomainConstants.RELATIONSHIP_PUSHED_SUBSCRIPTION), false, boEvent);
  }

  boPerson.close(context);
}
}

}
