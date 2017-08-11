/*
 *  emxDiscussionBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessItr;
import matrix.db.AccessList;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.SelectList;
import matrix.util.StringList;
import matrix.util.Pattern;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Message;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxDiscussionBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxDiscussionBase_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return nothing
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }

    /*****
    * Constants for grantor User Name
    *
    ******/
    /** Workspace Access User name. */
    static final String AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME = "Workspace Access Grantor";
    /** Workspace Access User name. */
    static final String AEF_WORKSPACE_LEAD_GRANTOR_USERNAME = "Workspace Lead Grantor";
    /** Workspace Access User name. */
    static final String AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME = "Workspace Member Grantor";
    /** Workspace Access User name. */
    static final String AEF_ROUTE_DELEGATION_GRANTOR_USERNAME = "Route Delegation Grantor";

    
    static final String ROLE_EMPLOYEE = PropertyUtil.getSchemaProperty ("role_Employee");

    /**
     * getDiscussionMembersList - gets the list of Members the user has access to the Discussion.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getDiscussionMembersList(Context context, String[] args)
        throws Exception
    {
        try {
            pushContextAccessGrantor(context);
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            MapList discussionMembers = new MapList();
            String objectId = (String) programMap.get("objectId");
            String SELECT_ROUTE_ID = "to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.id";
            String SELECT_ACCESS_TYPE = "to["+RELATIONSHIP_MESSAGE+"].attribute["+ATTRIBUTE_ACCESS_TYPE+"].value";
            DomainObject message = newInstance(context);
            message.setId(objectId);
            StringList objSelects = new StringList();
            objSelects.add(SELECT_ROUTE_ID);
            objSelects.add(SELECT_ACCESS_TYPE);

            Map resultMap = message.getInfo(context,objSelects);

            String accessType = (String)resultMap.get(SELECT_ACCESS_TYPE);
            String routeId = (String)resultMap.get(SELECT_ROUTE_ID);
            String SELECT_ROUTE_MEMBER_ID = "from["+DomainObject.RELATIONSHIP_ROUTE_NODE+"].to.id";

            DomainObject route = newInstance(context);
            route.setId(routeId);
            StringList routeMembers = route.getInfoList(context,SELECT_ROUTE_MEMBER_ID);

            HashMap member =  null;
            DomainObject person = newInstance(context);
            if(accessType.equals("Inherited")) {

              for(int i=0;i<routeMembers.size();i++) {
                member = new HashMap();
                member.put(SELECT_ID,(String)routeMembers.get(i));
                member.put(KEY_LEVEL,"1");
                discussionMembers.add(member);
              }
            } else {
            StringList grantees = message.getGrantees(context);
              for(int i=0;i<routeMembers.size();i++) {
                member = new HashMap();
                person.setId((String)routeMembers.get(i));
                if(grantees != null) {
                  if(grantees.indexOf((String)person.getName(context)) != -1) {
                    member.put(SELECT_ID,(String)person.getId());
                    member.put(KEY_LEVEL,"1");
                    discussionMembers.add(member);
                  }
                }
              }
            }
            return discussionMembers;
        } catch (Exception ex) {
            throw ex;
        } finally {
      ContextUtil.popContext(context);
    }
     }

  /**
  * Grants Access to Project Member for the workspace and its data structure.
  *
  * @param context the eMatrix Context object
  * @param Access List of Access objects holds the access rights, grantee and grantor.
  * @throws Exception if the operation fails
  * @since TC V3
  * @grade 0
  */
  public void grantAccess(matrix.db.Context context, String[] args) throws Exception
  {
      boolean hasAccess = false;
      try
      {
          // check if the logged in user has access to edit the access for the object
          hasAccess = AccessUtil.isOwnerWorkspaceLead(context, this.getId());
      }
      catch(Exception fex)
      {
          throw(fex);
      }
      if(!hasAccess)
      {
          return;
      }

      // Access Iterator of the Access list passed.
      AccessItr accessItr = new AccessItr((AccessList)JPO.unpackArgs(args));
      Access access = null;
      // BO list of the current object
      BusinessObjectList objList = new BusinessObjectList();
      objList.addElement(this);

      while (accessItr.next())
      {
          access = (Access)accessItr.obj();
          // push the context for grantor.
          pushContextForGrantor(context, access.getGrantor());
          try
          {
              grantAccessRights(context, objList, access);
          }
          catch(Exception exp)
          {
              if(access.getGrantor().equals(AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME))
              {
                  ContextUtil.popContext(context);
                  MqlUtil.mqlCommand(context, "trigger $1", true, "on");
              }
              ContextUtil.popContext(context);
              throw exp;
          }

          // if Access granted is empty then Revoke the access from grantee for business Object List
          if(access.getGrantor().equals(AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME))
          {
              if (access.hasNoAccess())
              {
                  revokeAccessGrantorGrantee(context, access.getGrantor(), access.getUser());
              }
              ContextUtil.popContext(context);
              MqlUtil.mqlCommand(context, "trigger $1", true, "on");
          }
          ContextUtil.popContext(context);
      }
  }

    /**
     * getDiscussionList - gets the list of Discussions attached to the object.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  Object getDiscussionList(Context context, String[] args) throws Exception
    {
         MapList list = new MapList();
        if (args.length == 0 )
        {
            throw new IllegalArgumentException();
        }
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String)paramMap.get("objectId");
         // added for the Bug 339013 
        String strPolicy = PropertyUtil.getSchemaProperty(context,"policy_PrivateMessage");
        String strOwner = context.getUser();
        String roleEmployee =   PropertyUtil.getSchemaProperty ( context, "role_Employee" );
        boolean hasEmpRole =false;
        Vector assignments = new Vector();
        assignments = PersonUtil.getAssignments(context);
         if(assignments.contains(roleEmployee))
        {
             hasEmpRole= true;
        }
        // Till Here
        if(objectId.indexOf("~") != -1)
        {
            objectId = objectId.substring(0,objectId.indexOf("~"));
        }
        if(FrameworkUtil.isObjectId(context,objectId))
        {
            DomainObject messageHolder = DomainObject.newInstance(context,objectId);
            StringList objectSelects = new StringList();
            objectSelects.add(SELECT_ID);
            objectSelects.add(SELECT_OWNER);
            objectSelects.add(SELECT_POLICY);
            
            // added for the Bug 339013 
            String objectWhere = "((policy =="+"\""+DomainConstants.POLICY_MESSAGE+"\")||((policy =="+"\""+strPolicy+"\") && ("+ Boolean.valueOf(hasEmpRole) +" || (owner =="+"\""+strOwner+"\"))))";
            list = Message.getMessages(context, messageHolder, objectSelects,objectWhere, 1);
        }
        return list;
    }

    /**
     * showCommand - Returns true of the context person is the owner of message object.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns boolean
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
     public boolean showCommand(Context context, String[] args) throws Exception
     {
       if (args.length == 0 ) throw new IllegalArgumentException();
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       boolean showCommand = false;
       String objectId = (String)paramMap.get("objectId");
       DomainObject obj = newInstance(context);
       obj.setId(objectId);
       com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
       String owner = obj.getInfo(context,SELECT_OWNER);
       if(owner.equals(contextPerson.getName())) {
         showCommand = true;
       }
       return showCommand;
     }
  /**
  * Revoke access on Object b/w particular Grantor and Grantee
  *
  * @param context the eMatrix Context object
  * @param grantor name to push context to.
  * @return void
  * @throws Exception if the operation fails
  * @since TC V3
  * @grade 0
  */
  protected void revokeAccessGrantorGrantee(matrix.db.Context context, String sGrantor, String sGrantee) throws Exception
  {
      MqlUtil.mqlCommand(context,"modify bus $1 revoke grantor $2 grantee $3", getId(context), sGrantor, sGrantee);
  }
  /**
  * Change context to Workspace Access Grantor
  *
  * @param context the eMatrix Context object
  * @return void
  * @throws Exception if the operation fails
  * @since TC V3
  * @grade 0
  */
  protected void pushContextAccessGrantor(Context context) throws Exception
  {
      ContextUtil.pushContext(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME, null, null);
  }

  /**
  * Push the context for the respective grantor.
  *
  * @param context the eMatrix Context object
  * @param grantor name to push context to.
  * @return void
  * @throws Exception if the operation fails
  * @since TC V3
  * @grade 0
  */
  protected void pushContextForGrantor(matrix.db.Context context, String sGrantor) throws Exception
  {
      // Check for grantor.
      if(sGrantor.equals(AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME))
      {
          pushContextAccessGrantor(context);
      }
      else if (sGrantor.equals(AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME))
      {
          pushContextMemberGrantor(context);
      }
      else if (sGrantor.equals(AEF_WORKSPACE_LEAD_GRANTOR_USERNAME))
      {
          pushContextLeadGrantor(context);
      }
  }

  /**
  * Change context to Workspace Member Grantor
  *
  * @param context the eMatrix Context object
  * @return void
  * @throws Exception if the operation fails
  * @since TC V3
  * @grade 0
  */
  protected void pushContextMemberGrantor(Context context) throws Exception
  {
      // Puch context to super user to turn off triggers
      ContextUtil.pushContext(context, null, null, null);
      try
      {
          // Turn off all triggers
          MqlUtil.mqlCommand(context, "trigger $1", true, "off");
      }
      catch (Exception exp)
      {
          ContextUtil.popContext(context);
          throw exp;
      }
      ContextUtil.pushContext(context, AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME, null, null);
  }

  /**
  * Change context to Workspace Lead Grantor
  *
  * @param context the eMatrix Context object
  * @return void
  * @throws Exception if the operation fails
  * @since TC V3
  * @grade 0
  */
  protected void pushContextLeadGrantor(Context context) throws Exception
  {
      ContextUtil.pushContext(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME, null, null);
  }


    /**
     * showCheckbox - determines if the checkbox needs to be enabled in the column of the Discussion Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since Common 10-0-0-0
     * @grade 0
     */
    public Vector showCheckbox(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Vector enableCheckbox = new Vector();
            String user = context.getUser();

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String owner = (String)objectMap.get(SELECT_OWNER);
                if(user.equals(owner))
                {
                    enableCheckbox.add("true");
                }
                else
                {
                    enableCheckbox.add("false");
                }
            }
            return enableCheckbox;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }


    /**
     * getDiscussionAttachmentsList - gets the list of Attachments for the
     *                                 Discussion.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns MapList
     * @throws Exception if the operation fails
     * @since APP 10-5
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getDiscussionAttachmentsList(Context context, String[] args)
        throws Exception
    {
        MapList attachmentList = new MapList();
        try
        {
            HashMap programMap   = (HashMap) JPO.unpackArgs(args);
            String objectId      = (String) programMap.get("objectId");

            Pattern relPattern  = new Pattern(DomainObject.RELATIONSHIP_MESSAGE_ATTACHMENTS);
            relPattern.addPattern(DomainObject.RELATIONSHIP_REPLY);
            
            Pattern includeRelPattern  = new Pattern(DomainObject.RELATIONSHIP_MESSAGE_ATTACHMENTS);

            StringList typeSelects = new StringList(10);
            typeSelects.add(CommonDocument.SELECT_ID);
            typeSelects.add(CommonDocument.SELECT_TYPE);
            typeSelects.add(CommonDocument.SELECT_FILE_NAME);
            typeSelects.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
            typeSelects.add(CommonDocument.SELECT_TITLE);
            typeSelects.add(CommonDocument.SELECT_REVISION);
            typeSelects.add(CommonDocument.SELECT_NAME);
            typeSelects.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION);
            typeSelects.add(CommonDocument.SELECT_HAS_ROUTE);
            typeSelects.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            typeSelects.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            typeSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            typeSelects.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);

            StringList relSelects = new StringList(1);
            relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

            DomainObject discussionObj = DomainObject.newInstance(context);
            discussionObj.setId(objectId);
            attachmentList = discussionObj.getRelatedObjects(context,
            												 relPattern.getPattern(),
                                                             "*",
                                                             typeSelects,
                                                             relSelects,
                                                             false,
                                                             true,
                                                             (short)2,
                                                             null,
                                                             null,
                                                             null,
                                                             includeRelPattern,
                                                             null);


        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return attachmentList;
        }
    }

/////////////////// My Discussions /////////////////////////////////////////////////////////////////
    /**
     * getFilteredDiscussionList - filters the list of Discussions present in the system based on the where expression
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */

    protected Object getFilteredDiscussionList(Context context,String whereExpression) throws Exception
    {
        MapList finalMapList = new MapList();
        try
        {
             com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
             Company company = person.getCompany(context);
             
    		BufferedReader in = new BufferedReader(new StringReader(MqlUtil.mqlCommand(context,"temp query bus $1 $2 $3 vault $4 where $5 select $6 $7 dump $8",TYPE_MESSAGE, "*", "*", company.getAllVaults(context,true), whereExpression, "id", "policy", "|" )));
            String line;
            while ((line = in.readLine()) != null)
            {
                int lastIdxOfSeperator = line.lastIndexOf("|");
                String policy = line.substring(lastIdxOfSeperator + 1);
                String id = line.substring(line.lastIndexOf("|", lastIdxOfSeperator - 1)+1, lastIdxOfSeperator);
                Map objMap = new HashMap();
                objMap.put(SELECT_ID, id);
                objMap.put(SELECT_POLICY, policy);
                finalMapList.add(objMap);
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
            return finalMapList;
    }


   /**
     * showSubscriptionStatus - shows a visual cue if the discussion is subscribed or not
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector
     * @throws Exception if the operation fails
     * @since AEF V11
     * @grade 0
     */

    public  Vector getSubscriptionIcon(Context context, String[] args) throws Exception
    {

        if (args.length == 0 )
        {
            throw new IllegalArgumentException();
        }
        
        Vector vSubscribed = new Vector();
        
        String contextUser = context.getUser();
        String relPublishSubscribe = PropertyUtil.getSchemaProperty(context,SYMBOLIC_relationship_PublishSubscribe);
        String relPublish = PropertyUtil.getSchemaProperty(context,SYMBOLIC_relationship_Publish);
        String relSubscribedPerson = PropertyUtil.getSchemaProperty(context,SYMBOLIC_relationship_SubscribedPerson);
        
        
        String selSubPersonName = new StringBuffer().append("from[").append(relPublishSubscribe).append("].to.from[").append(relPublish).
                                  append("].to.from[").append(relSubscribedPerson).append("].to.name").toString();

        String selRootObjId = new StringBuffer().append("to[").append(PropertyUtil.getSchemaProperty(context,"relationship_Message")).
                                    append("].from.to[").append(PropertyUtil.getSchemaProperty(context,"relationship_Thread")).append("].from.id").toString();
        
        String selEventIdFromRootObj = new StringBuffer("from[").append(relPublishSubscribe).append("].to.from["). append(relPublish).append("].to.id").toString();
        String selEventTypeFromEvent = new StringBuffer("attribute[").append(PropertyUtil.getSchemaProperty(context,"attribute_EventType")).append("]").toString();
        String selEventSubscirbedPersonFromEvent = new StringBuffer("from[").append(relSubscribedPerson).append("].to.name").toString();
        
        
        DomainObject.MULTI_VALUE_LIST.add(selSubPersonName);
        StringList selectList = new StringList();
        selectList.addElement(selSubPersonName);
        selectList.addElement(selRootObjId);
        
        StringList eventSelList = new StringList();
        eventSelList.add(selEventTypeFromEvent);
        eventSelList.add(selEventSubscirbedPersonFromEvent);
        
        try
        {
            HashMap programMap   = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objItr = objectList.iterator();
            String strSubscribed = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(context.getSession().getLanguage()),"emxComponents.Common.Subscribed");
            strSubscribed = FrameworkUtil.findAndReplace(strSubscribed, "\'", "&#39;");
            strSubscribed = FrameworkUtil.findAndReplace(strSubscribed, "\"", "&quot;");
            //XSSOK
            String imgSrc ="<img src='images/iconSmallSubscription.gif' title=\'"+strSubscribed+"\' ></img>";
            
            String objIds[] = new String[objectList.size()];
            int i=0;
            while(objItr.hasNext())
            {
                Map messageObj = (Map) objItr.next();
                objIds[i] = (String)messageObj.get(SELECT_ID);
                i++;
            }
            BusinessObjectWithSelectList bosWithSelect  = BusinessObject.getSelectBusinessObjectData(context,objIds,selectList);
            for(int j=0;j<bosWithSelect.size();j++)
            {
                BusinessObjectWithSelect bows = bosWithSelect.getElement(j);
                StringList subscriptionStatus =  bows.getSelectDataList(selSubPersonName);
                //Default show blank, if the context user is subscribed then show imange.
                String subscriptionICON = "&#160;";
                if(subscriptionStatus!=null && subscriptionStatus.contains(contextUser))
                {
                    subscriptionICON = imgSrc;
                }
                else
                {
                    String rootId = bows.getSelectData(selRootObjId);
                    DomainObject rootObj = new DomainObject(rootId);
                    StringList eventIds = rootObj.getInfoList(context, selEventIdFromRootObj);
                    BusinessObjectWithSelectList eventInfo  = 
                        BusinessObject.getSelectBusinessObjectData(context, (String[])eventIds.toArray(new String []{}), eventSelList);
                    for (Iterator iter = eventInfo.iterator(); iter.hasNext();) {
                        BusinessObjectWithSelect event = (BusinessObjectWithSelect) iter.next();
                        String eventType = event.getSelectData(selEventTypeFromEvent);
                        if(eventType.equals("New Reply") || eventType.equals("New Discussion")) {
                            StringList subscriberList = event.getSelectDataList(selEventSubscirbedPersonFromEvent);
                            if(subscriberList != null && subscriberList.contains(contextUser)) {
                                subscriptionICON = imgSrc;
                                break;
                            }
                        }
                    }
                }
                vSubscribed.addElement(subscriptionICON);
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        return vSubscribed;
    }

/**
 * getSubscribedDiscussionList - gets the list of subscribed discussion objects
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @return Object
 * @throws Exception if the operation fails
 * @since AEF V11
 * @grade 0
 */

@com.matrixone.apps.framework.ui.ProgramCallable
public Object getSubscribedDiscussionList(Context context, String[] args) throws Exception
{
    MapList subscribedMsgList = new MapList();
    StringBuffer whereCond = new StringBuffer();
    whereCond.append("attribute[");
    whereCond.append(ATTRIBUTE_EVENT_TYPE);
    whereCond.append("] == 'New Reply' || attribute[");
    whereCond.append(ATTRIBUTE_EVENT_TYPE);
    whereCond.append("] == 'New Discussion'");
    String strOutput = MqlUtil.mqlCommand(context,"expand bus $1 $2 $3 to relationship $4 type $5 select bus $6 where $7 dump $8", "Person", context.getUser(), "-", RELATIONSHIP_SUBSCRIBED_PERSON, TYPE_EVENT, "id", whereCond.toString(), "|");

    StringTokenizer strLineToken = new StringTokenizer(strOutput,"\n");
    String strRecToken ="";
    String objIds[] = new String[strLineToken.countTokens()];
    ArrayList objList = new ArrayList();
    int i=0;
    while(strLineToken.hasMoreTokens())
    {
        strRecToken = strLineToken.nextToken();
        strRecToken = strRecToken.substring(strRecToken.lastIndexOf("|")+1,strRecToken.length());

        objIds[i] = strRecToken;
        objList.add(strRecToken);
        i++;
    }

    StringBuffer selectBuf = new StringBuffer();
    selectBuf.append("to[").append(RELATIONSHIP_PUBLISH).append("].from.to[").append(RELATIONSHIP_PUBLISH_SUBSCRIBE).append("].businessobject.");
    StringList selectList = new StringList();
    selectList.add(selectBuf.toString() + "id");
    selectList.add(selectBuf.toString() + "type");
    selectList.add(selectBuf.toString() + "policy");

    BusinessObjectWithSelectList bosWithSelect  = BusinessObject.getSelectBusinessObjectData(context, objIds, selectList);
    for(int j=0;j<bosWithSelect.size();j++)
    {
        BusinessObjectWithSelect bows = bosWithSelect.getElement(j);
        String id = bows.getSelectData((String)selectList.elementAt(0));
        String type = bows.getSelectData((String)selectList.elementAt(1));
        String policy = bows.getSelectData((String)selectList.elementAt(2));
        if (type == null || id == null || policy == null || "".equals(type) || "".equals(id) || "".equals(policy)) {
            continue;
        }
        if(TYPE_MESSAGE.equals(type))
        {
            HashMap messageMap = new HashMap();
            messageMap.put(SELECT_ID, id);
            messageMap.put(SELECT_POLICY, policy);
            subscribedMsgList.add(messageMap);
        }
        else
        {
            DomainObject bs = new DomainObject(id);
            SelectList selects = new SelectList();
            selects.addId();
            selects.addPolicy();
            
            MapList discussions = bs.getRelatedObjects(context,
                                                       RELATIONSHIP_THREAD + "," + RELATIONSHIP_MESSAGE,
                                                       TYPE_MESSAGE,
                                                       selects, null,
                                                       false, true,
                                                       (short)-1,
                                                       null, null,
                                                       0,
                                                       null, null,  null);
            for(int k=0; k<discussions.size(); k++)
            {
                Map objInfo = (Map)discussions.get(k);
                String objId = (String)objInfo.get(SELECT_ID);
                if (objId != null && !objList.contains(objId)) {
                    HashMap messageMap = new HashMap();
                    messageMap.put(SELECT_ID, objId);
                    messageMap.put(SELECT_POLICY, objInfo.get(SELECT_POLICY));
                    subscribedMsgList.add(messageMap);
                }
            }
        }
    }

    return subscribedMsgList;
}

/**
 * getLast30DaysDiscussions - gets the list of subscribed discussion objects
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @returns Object
 * @throws Exception if the operation fails
 * @since AEF V11
 * @grade 0
 */

@com.matrixone.apps.framework.ui.ProgramCallable
public Object getLast30DaysDiscussions(Context context,String[] args) throws Exception
{
    MapList objectList=  new MapList();

    try
    {

        StringBuffer whereExp = new StringBuffer();
        java.text.SimpleDateFormat mxDateFrmt = new java.text.SimpleDateFormat (eMatrixDateFormat.getEMatrixDateFormat());

        java.util.GregorianCalendar gc = new java.util.GregorianCalendar();
        String currDate = mxDateFrmt.format(gc.getTime());
        String toDate="";

        gc.add(java.util.GregorianCalendar.DAY_OF_YEAR,-30);
        toDate = mxDateFrmt.format(gc.getTime());
        String selRootObjId = new StringBuffer().append("to[").append(PropertyUtil.getSchemaProperty(context,"relationship_Message")).
        append("].from.to[").append(PropertyUtil.getSchemaProperty(context,"relationship_Thread")).append("]").toString();

        whereExp.append("modified >='");
        whereExp.append(toDate);
        // whereExp.append("' and modified <='");
        // whereExp.append(currDate);
        whereExp.append("' and to[");
        whereExp.append(PropertyUtil.getSchemaProperty(context,SYMBOLIC_relationship_Message));
        whereExp.append("]=='True'");
        whereExp.append("and "+selRootObjId+"=='True'");

        objectList = (MapList)getFilteredDiscussionList(context,whereExp.toString());
    }
    catch(Exception ex)
    {
        throw ex;
    }
    return objectList;

}

/**
 * getLast90DaysDiscussions - gets the list of subscribed discussion objects
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @return Object
 * @throws Exception if the operation fails
 * @since AEF V11
 * @grade 0
 */

@com.matrixone.apps.framework.ui.ProgramCallable
public Object getLast90DaysDiscussions(Context context,String[] args) throws Exception
{

    MapList objectList = new MapList();
    try
    {

        StringBuffer whereExp = new StringBuffer();
        java.text.SimpleDateFormat mxDateFrmt = new java.text.SimpleDateFormat (eMatrixDateFormat.getEMatrixDateFormat());

        java.util.GregorianCalendar gc = new java.util.GregorianCalendar();
        String currDate = mxDateFrmt.format(gc.getTime());
        String toDate="";

        gc.add(java.util.GregorianCalendar.DAY_OF_YEAR,-90);
        toDate = mxDateFrmt.format(gc.getTime());
        String selRootObjId = new StringBuffer().append("to[").append(PropertyUtil.getSchemaProperty(context,"relationship_Message")).
        append("].from.to[").append(PropertyUtil.getSchemaProperty(context,"relationship_Thread")).append("]").toString();

        whereExp.append("modified >='");
        whereExp.append(toDate);
        // whereExp.append("' and modified <='");
        // whereExp.append(currDate);
        whereExp.append("' and to[");
        whereExp.append(PropertyUtil.getSchemaProperty(context,SYMBOLIC_relationship_Message));
        whereExp.append("]=='True'");
        whereExp.append("and "+selRootObjId+"=='True'");

        objectList = (MapList)getFilteredDiscussionList(context,whereExp.toString());
    }
    catch(Exception ex)
    {
        throw ex;
    }
    return objectList;

}
/**
 * getMessageType - gets policy of the message and decides whether the message is public or private
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @returns Object
 * @throws Exception if the operation fails
 * @since AEF 11-0
 * @grade 0
 */

public static Vector getMessageType(Context context,String [] args) throws Exception
{

    Vector vMessageTypeVec = new Vector();
    try
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        String lang = context.getSession().getLanguage();
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map discusion = (Map) iter.next();
            String policy = (String) discusion.get(SELECT_POLICY);
    		policy = policy == null ? MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", (String)discusion.get(SELECT_ID), policy) : policy;
            vMessageTypeVec.addElement(
            		EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(lang),POLICY_MESSAGE.equals(policy) ? "emxComponents.Discussion.Public" : "emxComponents.Discussion.Private"
                    ));
        }
    }
    catch(Exception ex)
    {
        throw ex;
    }


    return vMessageTypeVec;

}


/**
 * getLatestMessageOriginatedDate - gets the l
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @returns Object
 * @throws Exception if the operation fails
 * @since AEF 11-0
 * @grade 0
 */
public static Vector getLatestMessageOriginatedDate(Context context, String[] args) throws Exception
{
    Vector vModifiedDateVec = new Vector();
    try
    {
        Map objMap = null;
        String lastModifiedDate="";
        String objId = "";
        StringBuffer mqlString=null;

        StringList objectSelects = new StringList();
        objectSelects.addElement(SELECT_ORIGINATED);
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        Iterator objItr = objectList.iterator();
        MapList leafNodeList = new MapList();
        while(objItr.hasNext())
        {
            objMap = (Map)objItr.next();
            objId = (String)objMap.get(SELECT_ID);
            DomainObject messageObject = new DomainObject(objId);
            leafNodeList = messageObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_REPLY , DomainConstants.TYPE_MESSAGE, objectSelects, null, false, true, (short)0 , null, null, (short)0);

            leafNodeList.addSortKey(SELECT_ORIGINATED,"descending","date");
            leafNodeList.sort();
            if(leafNodeList.size()>0)
            {
                lastModifiedDate = (String)(((Map)leafNodeList.get(0)).get(SELECT_ORIGINATED));
                vModifiedDateVec.addElement(lastModifiedDate);
            }
            else
            {
                lastModifiedDate = (String)messageObject.getInfo(context,SELECT_ORIGINATED);
                vModifiedDateVec.addElement(lastModifiedDate);
            }

        }
    }
    catch(Exception ex)
    {
                throw ex;
    }
    return vModifiedDateVec;


}

/**
 * getReplyCountForDiscussion - gets the No.of replies given for each discussion
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @returns Object
 * @throws Exception if the operation fails
 * @since AEF 11-0
 * @grade 0
 */
public Vector getReplyCountForDiscussion(Context context,String args[]) throws Exception
{
    Map objMap = null;
    String objId="";
    String count = "";
    Vector vReplyVec = new Vector();
    try
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        Iterator objItr = objectList.iterator();

        while(objItr.hasNext())
        {
            objMap  = (Map) objItr.next();
            objId   = (String) objMap.get(SELECT_ID);
            count = MqlUtil.mqlCommand(context, "eval expr $1 on expand bus $2 from rel $3 recurse to all", "count TRUE", objId, PropertyUtil.getSchemaProperty(context,SYMBOLIC_relationship_Reply));
            vReplyVec.addElement(count);
        }
    }
    catch(Exception ex)
    {
        throw ex;
    }

return vReplyVec;
}
/**
 * showSubscribeCommand - Returns true if the selected filter is other than Subscribed.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @return boolean
 * @throws Exception if the operation fails
 * @since AEF 11-0
 * @grade 0
 */

public boolean showSubscribeCommand(Context context,String[] args) throws Exception
{
   HashMap programMap = (HashMap)JPO.unpackArgs(args);
   boolean showCommand = true;
   try
   {
       String selectedFilter = (String)programMap.get("selectedFilter");
       if(selectedFilter!=null && !"null".equals(selectedFilter) && !"".equals(selectedFilter) && "emxDiscussion:getSubscribedDiscussionList".equals(selectedFilter))
       {
           showCommand = false;
       }
   }
   catch(Exception ex)
   {
       throw ex;
   }
    return showCommand;
}
/**
 * showVisibilityColumn - Returns true if the user is a part of Private Discussion association.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @returns Object
 * @throws Exception if the operation fails
 * @since AEF 11-0
 * @grade 0
 */
public boolean showVisibilityColumn(Context context, String[] args) throws Exception
{
    return hasAccessToPrivateDiscussion(context, null);
}
/////////////////// My Discussions /////////////////////////////////////////////////////////////////
/**
 * isPrivateUser - returns true if user has the Autherity to
 *                 access Private Message else false.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList/ Object Id of parent
 * @returns boolean value
 * @throws Exception if the operation fails
 * @since AEF V11
 * @grade 0
 */
public boolean isPrivateUser(Context context,String args[]) throws Exception
{
    String objectId = null;
    if(args.length > 1){
        HashMap programMap   = (HashMap) JPO.unpackArgs(args);
        objectId = (String) programMap.get("objectId");
    } else{
        objectId   = args [0];
    }
    return hasAccessToPrivateDiscussion(context, objectId);
}
           
private boolean hasAccessToPrivateDiscussion(Context context, String objectId) throws Exception, FrameworkException {
 	com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
	person = (Person)newInstance(context, person.getPerson(context).getId(context));
	String orgId = person.getInfo(context, "relationship["+RELATIONSHIP_EMPLOYEE+"].from.id");
	String strHostCompanyId= Company.getHostCompany(context);
	Boolean hasAccessFlag = false;
	if(PersonUtil.getAssignments(context).contains(ROLE_EMPLOYEE)|| orgId.equals(strHostCompanyId))
	{
		hasAccessFlag = true;
	}
	return hasAccessFlag;
}

/**
 * updateRootMessageObject - Updates the Root Messag Object Count attribute with the no.of child messages.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - ObjectId of the Created Reply
 * @return int
 * @throws Exception if the operation fails
 * @since AEF 11-0
 * @grade 0
 */
public static int updateRootMessageObject (Context context, String args[])throws Exception
{
    int updated=0;
    try
    {
        String objectId = args[0];
        String parentObjId = args[1];

        String rootMessageObjectId = emxMessageUtil_mxJPO.getRootMessageId(context,objectId);
        String strCount = MqlUtil.mqlCommand(context, "eval expr $1 on expand bus $2 from rel $3 recurse to all", "count TRUE", rootMessageObjectId, PropertyUtil.getSchemaProperty(context,SYMBOLIC_relationship_Reply));

        DomainObject rootMsgobj = new DomainObject(rootMessageObjectId);
        rootMsgobj.setAttributeValue(context,ATTRIBUTE_COUNT,strCount);
        if(!rootMessageObjectId.equals(parentObjId))
        {
            //set the value to immediate parent
            rootMsgobj.setId(parentObjId);
            int count;
               try
               {
        		count = Integer.parseInt(rootMsgobj.getAttributeValue(context, ATTRIBUTE_COUNT));
               }
               catch (NumberFormatException ex)
               {
                  count = 0;
               }
               count++;
            rootMsgobj.setAttributeValue(context,ATTRIBUTE_COUNT,Integer.toString(count));
        }
    }
    catch(Exception e)
    {
        updated=1;
    }
    return updated;

}
//2011x Team Wellness Plan feature
/**
 * updateRootMessageObject - Updates the Root Messag Object Count attribute with the no.of child messages.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - ObjectId of the Created Reply
 * @return int
 * @throws Exception if the operation fails
 * @since AEF 11-0
 * @grade 0
 */
public String getOwnedFilesRadiobuttons(Context context, String[] args) throws Exception
{
    StringBuffer sb = new StringBuffer();
    sb.append("<input type=\"radio\" name=\"ownedFiles\" id=\"ownedFiles\" value=\"true\" checked>&nbsp;&nbsp;");
    sb.append(EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(context.getSession().getLanguage()),"emxTeamCentral.FindFiles.FileIOwn"));
    sb.append("<br>");
    sb.append("<input type=\"radio\" name=\"ownedFiles\" id=\"ownedFiles\" value=\"false\">&nbsp;&nbsp;");
    sb.append(EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(context.getSession().getLanguage()),"emxTeamCentral.FindFiles.AllFiles"));
    return sb.toString();
}
/**
 * getWorkspaceSubfoldersCheckbox - Displays the workspace subfolders checkbox for find files search
 * @param context the eMatrix <code>Context</code> object
 * @return String
 * @throws Exception if the operation fails
 * @since R210
 * @grade 0
 */
public String getWorkspaceSubfoldersCheckbox(Context context, String[] args) throws Exception
{
    StringBuffer sb = new StringBuffer();
    sb.append("<input type=\"checkbox\" name=\"workspacesubfolders\" id=\"workspacesubfolders\" value=\"True\">&nbsp;&nbsp;");
    sb.append(EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(context.getSession().getLanguage()),"emxTeamCentral.FindFiles.IncludeSubFolders"));
    return sb.toString();
}
/**
 * getDiscussionMatchcase - Displays the checkbox for Match case
 * @param context the eMatrix <code>Context</code> object
 * @return String
 * @throws Exception if the operation fails
 * @since R210
 * @grade 0
 */
public String getDiscussionMatchcase(Context context, String[] args) throws Exception
{
    StringBuffer sb = new StringBuffer();
    sb.append("<input type=\"text\" name=\"matchCase\" id=\"matchCase\" value=\"*\">&nbsp;&nbsp;");
    sb.append("<input type=\"checkbox\" name=\"matchCase\" value=\"True\" id=\"matchCase\" checked>&nbsp;");
    sb.append(EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(context.getSession().getLanguage()),"emxTeamCentral.FindFiles.MatchCase"));
    return sb.toString();
}
/**
 * showFilesWorkspaceName - Displays the workspace name to search files under
 * @param context the eMatrix <code>Context</code> object
 * @return Vector
 * @throws Exception if the operation fails
 * @since R210
 * @grade 0
 */
public Vector showFilesWorkspaceName(Context context, String[] args) throws Exception
{
    Vector workspacenames = new Vector();
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objectList = (MapList)programMap.get("objectList");
    Iterator wrkspaceItr = objectList.iterator();
    while(wrkspaceItr.hasNext())
    {
        Map map = (Map)wrkspaceItr.next();
        String strWorkspaceName = (String)map.get("workspaceNames");
        workspacenames.add(XSSUtil.encodeForHTML(context, strWorkspaceName));
    }
    return workspacenames;
}
/**
 * showFilesFolderName - Displays the folder under which the files are present 
 * @param context the eMatrix <code>Context</code> object
 * @return Vector
 * @throws Exception if the operation fails
 * @since R210
 * @grade 0
 */
public Vector showFilesFolderName(Context context, String[] args) throws Exception
{
    Vector foldernames = new Vector();
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objectList = (MapList)programMap.get("objectList");
    Iterator folderItr = objectList.iterator();
    while(folderItr.hasNext())
    {
        Map map = (Map)folderItr.next();
        String strFolderName = (String)map.get("folder");
        foldernames.add(XSSUtil.encodeForHTML(context, strFolderName));
    }
    return foldernames;
}
/**
 * showFilesVersion - Displays the version of the files in search results
 * @param context the eMatrix <code>Context</code> object
 * @return Vector
 * @throws Exception if the operation fails
 * @since R210
 * @grade 0
 */
public Vector showFilesVersion(Context context, String[] args) throws Exception
{
    Vector fileVersions = new Vector();
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objectList = (MapList)programMap.get("objectList");
    Iterator versionItr = objectList.iterator();
    while(versionItr.hasNext())
    {
        Map map = (Map)versionItr.next();
        String strFileVersion = (String)map.get("version");
        fileVersions.add(XSSUtil.encodeForHTML(context, strFileVersion));
    }
    return fileVersions;
}
/**
 * showDocFilesCheckbox - Displays the document files checkbox
 * @param context the eMatrix <code>Context</code> object
 * @return Vector
 * @throws Exception if the operation fails
 * @since R210
 * @grade 0
 */
public Vector showDocFilesCheckbox(Context context, String[] args) throws Exception
{
    Vector docCheckboxes = new Vector();
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objectList = (MapList)programMap.get("objectList");
    HashMap requestMap = (HashMap)programMap.get("paramList");         
    String sSelectedObjId     = (String)requestMap.get("objectId");
    String sWorkspaceFolder     = (String)requestMap.get("WorkspaceFolderDisplay");
    String sWorkspaceFolderId     = (String)requestMap.get("WorkspaceFolder");
    String sTypeProjectVault     = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
    String sRelVaultedDocuments  = PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocuments");
    
    String sTypeProject          = PropertyUtil.getSchemaProperty(context,"type_Project");
    String sRelProjectVaults     = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults");
    String strRouteType          = PropertyUtil.getSchemaProperty(context, "type_Route");
    String sRelObjRou            = PropertyUtil.getSchemaProperty(context, "relationship_ObjectRoute");
    String sRelRouteScope        = PropertyUtil.getSchemaProperty(context,"relationship_RouteScope");
    String DocumentsinMultipleRoutes = EnoviaResourceBundle.getProperty(context,"emxTeamCentral.DocumentsinMultipleRoutes");
    String canRouteCheckoutDocuments = EnoviaResourceBundle.getProperty(context,"emxTeamCentral.CanRouteCheckoutDocuments");
    DomainObject fromObject    = DomainObject.newInstance(context);
    StringList connectedFileList = new StringList();

    if(sWorkspaceFolder == null ){
    sWorkspaceFolder = "";
    }
    if( sSelectedObjId != null && (!sSelectedObjId.equals(""))){
        fromObject.setId(sSelectedObjId);
      }
    if(sWorkspaceFolderId==null || sWorkspaceFolderId.equals("*")){
      sWorkspaceFolderId="";
    }

    String contentFromType = fromObject.getInfo(context, fromObject.SELECT_TYPE);
    String routeScopeId = "";
    boolean bfromMeetorDis = false;

    DomainObject scopeObject    = DomainObject.newInstance(context);
    if(strRouteType.equals(contentFromType)) {
      sWorkspaceFolder = "";
      //sWorkspaceFolderId = "";
      routeScopeId = fromObject.getInfo(context, "to["+sRelRouteScope+"].from.id");
      scopeObject.setId(routeScopeId);
      if(sTypeProject.equals((scopeObject.getInfo(context, fromObject.SELECT_TYPE)))) {
        // build the list of folders
       if(sWorkspaceFolderId.equals("")){
      StringList folderList = scopeObject.getInfoList(context, "from["+sRelProjectVaults+"].to.id");
        Iterator folderItr = folderList.iterator();
        while(folderItr.hasNext()) {
           sWorkspaceFolderId = sWorkspaceFolderId + folderItr.next() + ",";
        }
     }
      } else {
           sWorkspaceFolderId = routeScopeId + ",";
      }
      connectedFileList = fromObject.getInfoList(context, "to["+sRelObjRou+"].from.attribute["+DomainObject.ATTRIBUTE_TITLE+"]");
     } else if((sTypeProject.equals(contentFromType) || sTypeProjectVault.equals(contentFromType))) {
        sWorkspaceFolder = "";
        sWorkspaceFolderId = "";
        if(sTypeProject.equals(contentFromType)) {
          scopeObject.setId(sSelectedObjId);
          StringList folderList = scopeObject.getInfoList(context, "from["+sRelProjectVaults+"].to.id");
          Iterator folderItr = folderList.iterator();
          while(folderItr.hasNext()) {
            sWorkspaceFolderId = sWorkspaceFolderId + folderItr.next() + ",";
          }
        } else {
          sWorkspaceFolderId = sSelectedObjId + ",";
        }
    } else if(sTypeProjectVault.equals(contentFromType)) {
      connectedFileList = fromObject.getInfoList(context, "from["+sRelVaultedDocuments+"].to.attribute["+DomainObject.ATTRIBUTE_TITLE+"]");
    } else if((fromObject.TYPE_MEETING).equals(contentFromType)) {
      bfromMeetorDis = true;
      connectedFileList = fromObject.getInfoList(context, "from["+fromObject.RELATIONSHIP_MEETING_ATTACHMENTS+"].to.attribute["+DomainObject.ATTRIBUTE_TITLE+"]");
    } else if((fromObject.TYPE_MESSAGE).equals(contentFromType)) {
      bfromMeetorDis = true;
      connectedFileList = fromObject.getInfoList(context, "from["+fromObject.RELATIONSHIP_MESSAGE_ATTACHMENTS+"].to.attribute["+DomainObject.ATTRIBUTE_TITLE+"]");
    }
    Iterator itr = objectList.iterator();
    while(itr.hasNext())
    {
        Map map = (Map)itr.next();
        String sObjName = (String)map.get("name");
        String toConnAccess = (String)map.get("toConnectAccess");
        String inRoute = (String)map.get("inRoute");
        String sConnected = "";
        if(connectedFileList != null && connectedFileList.contains(sObjName)){
            sConnected = "true";
           }
        String sLocked = (String)map.get("lock");
        if(contentFromType.equals(sTypeProjectVault)){
            if ((sLocked.equals("true")) || "true".equals(sConnected)) {
 
             docCheckboxes.add("false");
 
           } else {
 
             docCheckboxes.add("true");
 
           }

          }else if(contentFromType.equals(DomainObject.TYPE_MESSAGE)){
            if("FALSE".equals(toConnAccess) || "true".equals(sConnected)){
 
                docCheckboxes.add("false");

            }else{

                docCheckboxes.add("true");

            }
          }else{

           if (DocumentsinMultipleRoutes.equals("false") && canRouteCheckoutDocuments.equals("false") ) {

             if(inRoute.equals("true") || sLocked.equals("true") || "true".equals(sConnected) ){

                 docCheckboxes.add("false");

              }else{

                  docCheckboxes.add("true");

              }


           } else if(DocumentsinMultipleRoutes.equals("false") && canRouteCheckoutDocuments.equals("true")) {

              if(inRoute.equals("true") || "true".equals(sConnected)){


                  docCheckboxes.add("false");

              }else{


                  docCheckboxes.add("true");
  
              }
           }else if(DocumentsinMultipleRoutes.equals("true") && canRouteCheckoutDocuments.equals("false")) {
                  if( (sLocked.equals("false") && "false".equals(sConnected))){

                      docCheckboxes.add("false");

                  }else{

                      docCheckboxes.add("true");

                  }
           }else{
              if (("true".equals(sConnected))) {

                  docCheckboxes.add("false");

              } else {

                  docCheckboxes.add("true");

           }

          }
        }

           if (inRoute.equals("true")) {
               if(sLocked.equals("true")) {
               docCheckboxes.add("false");
           } else {
               docCheckboxes.add("true");
           }
    }
    }       
    return docCheckboxes;
  }
/**
 * getFileType - Displays the file type and icon for discussion find files search form
 * @param context the eMatrix <code>Context</code> object
 * @return String
 * @throws Exception if the operation fails
 * @since R210
 * @grade 0
 */
 public String getFileType(Context context, String[] args) throws Exception
 {
     StringBuffer sb = new StringBuffer();
     sb.append("<img border=0 src=../teamcentral/images/iconFile.gif></img>&nbsp;");
     sb.append(EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(context.getSession().getLanguage()),"emxTeamCentral.FindFiles.File"));
     return sb.toString();
 }
 /**
  * getDiscussionVisibility - Displays the visibility for discussion object either Public or Private
  * @param context the eMatrix <code>Context</code> object
  * @return String[] args containing parammap and requestmap
  * @throws Exception if the operation fails
  * @since R212
  */
 public String getDiscussionVisibility(Context context, String[] args) throws Exception {
     HashMap programMap = (HashMap)JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String languageStr = (String) paramMap.get("languageStr");
     
     Map requestMap = (Map)programMap.get("requestMap");
     String discType = (String)requestMap.get("DiscType");
     discType = "Public".equals(discType) ? EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Discussion.Public") :
                "Private".equals(discType) ? EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Discussion.Private") : discType;

     return discType;
 }
 /**
  * getDiscussionSubjectValue - Displays the Subject for discussion object
  * @param context the eMatrix <code>Context</code> object
  * @return String[] args containing parammap and requestmap
  * @throws Exception if the operation fails
  * @since R212
  */
 public String getDiscussionSubjectValue(Context context, String[] args) throws Exception {
     HashMap programMap = (HashMap)JPO.unpackArgs(args);
     Map requestMap = (Map)programMap.get("requestMap");
     String objectId = (String)requestMap.get("objectId");
     
     String subject = "";
     if(!UIUtil.isNullOrEmpty(objectId)) {
         Message message = (Message) DomainObject.newInstance(context,DomainConstants.TYPE_MESSAGE);
         message.setId(objectId);
         if(DomainConstants.TYPE_MESSAGE.equals(message.getInfo(context, DomainConstants.SELECT_TYPE))){
             String existingSub = (String)message.getInfo(context, message.SELECT_MESSAGE_SUBJECT);
             if (existingSub != null && existingSub.startsWith("Re ")){
                 subject = existingSub;
             } else {
                 subject += "Re ";
                 subject += existingSub;
             }
         }
     }
     return subject;
 }
 public void setDiscussionSubject(Context context, String[] args) throws Exception {
	 HashMap programMap = (HashMap)JPO.unpackArgs(args);
     Map requestMap = (Map)programMap.get("requestMap");
     Map paramMap = (Map)programMap.get("paramMap");
     String objectId = (String)paramMap.get("objectId");
     String[] subject = (  String[]) requestMap.get("Subject");
     
     DomainObject message    = DomainObject.newInstance(context);
     message.setId(objectId);
     /* Temporarily fixed as part of IR-185089V6R2013x
        Whole function is to be removed but since we are using edit form update function is a must. 
        Public and Private discussions creation to be migrated to create component to avoid
        the usage of update function and program. As this existing Update function is also a dummy function */
 }
 
 @com.matrixone.apps.framework.ui.CreateProcessCallable
 public Map createDiscussionPostProcess(Context context, String[] args) throws Exception {
	 HashMap programMap = (HashMap)JPO.unpackArgs(args);
	 String strSubject   = (String) programMap.get("Subject");
	 String strMessage   = (String) programMap.get("Message");
	 String objectId   = (String) programMap.get("objectId");
	 String visibility   = (String) programMap.get("DiscType"); 
	 strSubject = FrameworkUtil.findAndReplace(strSubject, "\n", "<br />");
	 strMessage = FrameworkUtil.findAndReplace(strMessage, "\n", "<br />");
	 Message message  = (Message) DomainObject.newInstance(context,DomainConstants.TYPE_MESSAGE);	   
	 Route route = (Route)DomainObject.newInstance(context,Route.TYPE_ROUTE);
	 DomainObject domainObject = DomainObject.newInstance(context,objectId);
	 String strType = (String)domainObject.getInfo(context,domainObject.SELECT_TYPE);
	 
	 HashMap retMap = new HashMap();
		//Depending on the Private and Public message visibility, corresponding create() method from the Message class should be called
		String strPrivate = "Private";
		String strPublic  = "Public";
		if(visibility != null){
		    //Code to convert visibility from "Private Message"/"Public Message"
		    if(visibility.contains(strPrivate)){
		        //Private message
		    	message.create(context, strSubject, strMessage,strPrivate,domainObject);
		    }
		    else{
		        //Public message
		        message.create(context, strSubject, strMessage,strPublic,domainObject);
		    }
		}
		else{
		    //Public message without visibility parameter
		    message.create(context, strSubject, strMessage,domainObject);
		}   
		
	    retMap.put("id", message.getId());
		return retMap;
 
 }
 
}
