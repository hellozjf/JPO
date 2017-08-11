/*
 *  emxWorkspaceBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import matrix.db.Access;
import matrix.db.AccessList;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectItr;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectProxy;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessType;
import matrix.db.BusinessTypeList;
import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.ExpansionWithSelect;
import matrix.db.FileItr;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectItr;
import matrix.db.RelationshipWithSelectList;
import matrix.db.Role;
import matrix.db.RoleItr;
import matrix.db.RoleList;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.Meeting;
import com.matrixone.apps.common.Message;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.SubscriptionManager;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.emxGrantAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.RoleUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.team.TeamUtil;
import com.matrixone.fcs.common.ImageRequestData;
import com.matrixone.apps.domain.DomainSymbolicConstants;

/**
 * @version Common Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxWorkspaceBase_mxJPO extends emxWorkspaceConstants_mxJPO
{

    private static final HttpSession HttpServletRequest = null;

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public emxWorkspaceBase_mxJPO (Context context, String[] args)
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
     * @since AEF Rossini
     */
    public int mxMain(Context context, String[] args)
        throws FrameworkException
    {
        if (!context.isConnected())
            throw new FrameworkException(ComponentsUtil.i18nStringNow("emxTeamCentral.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }

  /**
   * Grants the Access to Workspace Folders to usr.
   *
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return void
   * @throws Exception if the operation fails
   * @since TC V3
   */
  public void grantWorkspaceAccess(matrix.db.Context context, String[] args) throws Exception
  {

    // Restore the previous context
    ContextUtil.restoreContext(context);
    String sGrantee = args[0];

    // set the grantor as Context user, sine macro doesnot filled in properly
    //String sGrantor = args[1];
    String sGrantor = context.getUser();

    // Construct grantee list
    StringList sGranteeList = new StringList();
    sGranteeList.add(sGrantee);

    // Construct Select Statemenst
    StringList listSelectStatements =  new StringList();
    String sSelProjectVaults = "from["+relProjectVaults+"].to.id";
    String sSelRouteScope = "from["+relRouteScope+"].to.id";

    listSelectStatements.add(sSelProjectVaults);

    if (sGrantor.equals(AEF_WORKSPACE_LEAD_GRANTOR_USERNAME)) {
      listSelectStatements.add(sSelRouteScope);
    }


    // get the Access for the current grantor for the passsed grantee on the current object.
    Access access = getAccessForGranteeGrantor(context, sGrantee, sGrantor);

    BusinessObjectList busList = new BusinessObjectList();

    // do Select
    BusinessObjectWithSelect boSelect = select(context, listSelectStatements);
    StringList selectProjectVaultList = boSelect.getSelectDataList(sSelProjectVaults);

    //pushContextForGrantor(context,sGrantor);
    try {
      if ((selectProjectVaultList != null) && (selectProjectVaultList.size() > 0)) {
        // Iterate thru the result list, construct BO with the selected Id and add to list.
        StringItr strResultItr = new StringItr(selectProjectVaultList);
        while(strResultItr.next()) {
          busList.add( new BusinessObject (strResultItr.obj()) );
        }

        // Grant the access for the business Object List to grantee
        matrix.db.BusinessObject.grantAccessRights(context, busList, access);
      }

      // Grant Route Read Access to Only Project Lead
      if (sGrantor.equals(AEF_WORKSPACE_LEAD_GRANTOR_USERNAME)) {
        StringList selectRoutelist = boSelect.getSelectDataList(sSelRouteScope);
        if ((selectRoutelist != null) && (selectRoutelist.size() > 0)) {
          // Iterate thru the result list, construct BO with the selected Route Id and add to list.
          StringItr strRouteItr = new StringItr(selectRoutelist);
          BusinessObjectList busRouteList = new BusinessObjectList();
          while(strRouteItr.next()) {
           busRouteList.add( new BusinessObject(strRouteItr.obj()) );
          }
          if (access.hasNoAccess()) {
           matrix.db.BusinessObject.revokeAccessRights(context, busRouteList, sGranteeList);
          } else {
           // Grant the access for the business Object List to grantee (Project Lead)
            AccessUtil accessUtil = new AccessUtil();
            accessUtil.setRead(sGrantee);
            access = (Access)((accessUtil.getAccessList()).elementAt(0));
            matrix.db.BusinessObject.grantAccessRights(context, busRouteList, access);
          }
        }
      }
      // if Access granted is empty then Revoke the access from grantee for business Object List
      if (access.hasNoAccess()) {
        revokeAccessGrantorGrantee(context, sGrantor, sGrantee);
      }

    } catch(Exception exp) {
      //ContextUtil.popContext(context);
      throw exp;
    }
  }


  /**
   * Revoke all access from Project Member when removed from the Workspace.
   *
   * @param context the eMatrix Context object
   * @param args is an empty String list - mandatory param for JPO
   * @return void
   * @throws Exception if the operation fails
   * @since TC V3
   */
  public void revokeAllAccess(matrix.db.Context context, String[] args) throws Exception
  {

    // Construct Select Statemenst
    StringList listSelectStatements =  new StringList();

    String sWsSelStatement = "to[" + relProjectMembers+"].from.id";
    String sPersonSelStatement = "to["+relProjectMembership+"].from.name";
    String sProjectAccess = "attribute["+ATTRIBUTE_PROJECT_ACCESS+"]";

    listSelectStatements.add(sWsSelStatement);
    listSelectStatements.add(sPersonSelStatement);
    listSelectStatements.add(sProjectAccess);

    // Select current Project Member's WorkspaceId and Person name
    BusinessObjectWithSelect boSelect = select(context, listSelectStatements);

    // get the grantee name and workspace id.
    String sGrantee = boSelect.getSelectData(sPersonSelStatement);
    String sWorkspaceId = boSelect.getSelectData(sWsSelStatement);
    String sProjectRole = boSelect.getSelectData(sProjectAccess);

    if(!UIUtil.isNullOrEmpty(sGrantee) && !UIUtil.isNullOrEmpty(sWorkspaceId)) {

      // Construct bo list of workspace object to grant access
      BusinessObjectList busList = new BusinessObjectList();
      busList.add( new BusinessObject (sWorkspaceId) );

      // Construct grantee list
      StringList sGranteeList = new StringList();
      sGranteeList.add(sGrantee);
      // Create new Access object with none access.
      Access access = new Access();
      access.setNoAccess(true);
      access.setUser(sGrantee);

      // Set the context to 'Workspace Lead Grantor' if the Person removed is Project Lead,
      try {
        if(sProjectRole.equals("Project Lead")) {
          // Grant the access for the business Object List to grantee
          pushContextForGrantor(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME);
          matrix.db.BusinessObject.grantAccessRights(context, busList, access);
          ContextUtil.popContext(context);
        }

        // grant the access using WAG,
        pushContextForGrantor(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME);
        matrix.db.BusinessObject.grantAccessRights(context, busList, access);
        ContextUtil.popContext(context);

        // if Access granted is empty then Revoke the access from grantee for current object
        if(access.hasNoAccess()) {
          matrix.db.BusinessObject.revokeAccessRights(context, busList, sGranteeList);
        }
      } catch (Exception exp) {
        ContextUtil.popContext(context);
        throw exp;
      }

    }
  }

  /**
   * Grant access to new Workspace member.
   *
   * @param context the eMatrix Context object
   * @param args holds id of Project Member object
   * @return void
   * @throws Exception if the operation fails
   * @since TC V3
   */
  public void grantMemberAccess(matrix.db.Context context, String[] args) throws Exception
  {
      /*
      try {
          DomainObject projMember = new DomainObject(args[0]);
          Map personInfo = projMember.getRelatedObject(context, RELATIONSHIP_PROJECT_MEMBERSHIP, false,
                  new StringList(SELECT_NAME), new StringList());

          Access access  = new Access();
          access.setReadAccess(true);
          access.setShowAccess(true);
          access.setCheckoutAccess(true);
          access.setUser((String)personInfo.get(SELECT_NAME));
          access.setGrantor(AccessUtil.WORKSPACE_MEMBER_GRANTOR);

    BusinessObjectList busList = new BusinessObjectList();
    busList.addElement(this);

          try {
    //Push context to Workspace Member grantor to give 'read' access only for Workspace.
              ContextUtil.pushContext(context, AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME, null, null);
            matrix.db.BusinessObject.grantAccessRights(context, busList, access);
          } finally {
              ContextUtil.popContext(context);
        }
      } catch (Exception e) {
          throw e;
    }*/
  }

  /**
   * get folder list for the Workspace.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   * @return Maplist of Workspace Folder names
   * @throws Exception if the operation fails
   * @since AEF Rossini
   */
  public static MapList getWorkSpaceFolderList(Context context, String[] args) throws
      Exception
  {
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       HashMap paramMap   = (HashMap)programMap.get("paramMap");

       String objectId    = (String)paramMap.get("objectId");
       DomainObject domObj = DomainObject.newInstance(context,objectId);
       String objType=domObj.getInfo(context, DomainObject.SELECT_TYPE);

       StringList sSelects = new StringList();
       MapList result = new MapList();
       if( objType != null && objType.equals(DomainObject.TYPE_WORKSPACE))
       {
           try
           {
               sSelects.add(DomainObject.SELECT_ID)  ;
               sSelects.add(DomainObject.SELECT_NAME);
               result= (MapList)domObj.getRelatedObjects(context,
                                                DomainObject.RELATIONSHIP_WORKSPACE_VAULTS,
                                                DomainObject.TYPE_PROJECT_VAULT,
                                                sSelects,
                                                null,
                                                false,
                                                true,
                                                (short)1,
                                                null,
                                                null);

           } catch ( FrameworkException e){
               throw new FrameworkException(e);
           }

      }
      else
      {
          DomainObject subFolder = DomainObject.newInstance(context , objectId);
          StringList selectRelStmts  = new StringList();
          StringList selectTypeStmts = new StringList();
          selectTypeStmts.add(subFolder.SELECT_NAME);
          selectTypeStmts.add(subFolder.SELECT_ID);
          try
          {
              result= (MapList)subFolder.getRelatedObjects(context,
                                                DomainObject.RELATIONSHIP_SUBVAULTS,
                                                DomainObject.TYPE_PROJECT_VAULT,
                                                selectTypeStmts,
                                                selectRelStmts,
                                                false,
                                                true,
                                                (short)1,
                                                null,
                                                null);
          } catch ( FrameworkException e){
              throw new FrameworkException(e);
          }
      }
      return result;

  }

  /**
   * checks to see if a workspace is archived or not
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   * @return boolean of Access
   * @throws Exception if the operation fails
   * @since AEF Rossini
   */
  public static boolean displayTreeNodeAccessCheck(Context context, String[] args) throws
      Exception
  {
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       boolean result     = true;

       StringList strSel  = new StringList();
       strSel.add(DomainConstants.SELECT_CURRENT);
       strSel.add(DomainConstants.SELECT_TYPE);
       String objectId = (String)programMap.get("objectId");

       DomainObject domObj = DomainObject.newInstance(context,objectId);
       Map objMap          = domObj.getInfo(context, strSel);
       String objType      = null;
       String objState     = null;
       if(objMap != null)
       {
          objType = (String)objMap.get(DomainConstants.SELECT_TYPE);
       }
       if(objType != null && objType.equals(DomainConstants.TYPE_PROJECT))
       {
          objState = (String)objMap.get(DomainConstants.SELECT_CURRENT);
          if(objState != null && !"".equals(objState) && objState.equals("Archive"))
          {
            result = false;
          }
       }

       return result;
  }
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getAllMyDeskWorkspace(Context context, String[] args)
      throws Exception, MatrixException
  {
      try
      {
          return getMyDeskWorkspace(context,args,"All");
      }
      catch (Exception ex)
      {
          throw ex;
      }
  }
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getActiveMyDeskWorkspace(Context context, String[] args)
      throws Exception, MatrixException
  {
      try
      {
          return getMyDeskWorkspace(context,args,"Active");
      }
      catch (Exception ex)
      {
          throw ex;
      }
  }
  public Object getMyDeskWorkspace(Context context, String[] args,String sFtr) throws Exception
  {
    Person person = Person.getPerson(context);
    String newSecurityModel = "true";
    try
    {
        newSecurityModel = EnoviaResourceBundle.getProperty(context, "emxComponents.NewSecurityModel");
    } catch(Exception ex)
    {
        //Do nothing
    }
    //build object-select statements
    StringList selectTypeStmts = new StringList();
    selectTypeStmts.add(DomainObject.SELECT_TYPE);
    selectTypeStmts.add(DomainObject.SELECT_NAME);
    selectTypeStmts.add(DomainObject.SELECT_ID);
    selectTypeStmts.add(DomainObject.SELECT_DESCRIPTION);
    selectTypeStmts.add(DomainObject.SELECT_OWNER);
    selectTypeStmts.add(DomainObject.SELECT_CURRENT);
    selectTypeStmts.add(DomainObject.SELECT_POLICY);

    String queryTypeWhere   = "";
    String expandTypeWhere  = "";


    MapList workspaceList = new MapList();
    //query selects
    StringList objectSelects = new StringList();
    objectSelects.add(DomainObject.SELECT_ID);
    //have to include SELECT_TYPE as a select since the expand has includeTypePattern
    objectSelects.add(DomainObject.SELECT_TYPE);
	objectSelects.add(DomainObject.SELECT_NAME);
    if( newSecurityModel != null && "false".equals(newSecurityModel))
    {

    // where for the query, show workspaces in the "Active" state only, and the users(roles) must have read access on the workspace.
    queryTypeWhere = "('" + DomainObject.SELECT_CURRENT + "' == 'Active')";

      // where for the expand, show workspaces in the "Active" state for members, Owner can see the Workspace in any state.
    if (sFtr.equals("Active")) {
      expandTypeWhere = "('" + DomainObject.SELECT_CURRENT + "' == 'Active')";
    } else {
      expandTypeWhere = "('" + DomainObject.SELECT_OWNER + "' == '"+person.getName()+"' || '" + DomainObject.SELECT_CURRENT + "' == 'Active')";
    }
    //build type and rel patterns
    Pattern typePattern = new Pattern(DomainConstants.TYPE_PROJECT_MEMBER);
    typePattern.addPattern(DomainConstants.TYPE_PROJECT);
    Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
    relPattern.addPattern(DomainConstants.RELATIONSHIP_PROJECT_MEMBERS);

    // type and rel patterns to include in the final resultset
    Pattern includeTypePattern = new Pattern(DomainConstants.TYPE_PROJECT);
    Pattern includeRelPattern = new Pattern(DomainConstants.RELATIONSHIP_PROJECT_MEMBERS);

    // get all workspaces that the user is a Project-Member
        workspaceList = person.getRelatedObjects(context,
                                        relPattern.getPattern(),  //String relPattern
                                        typePattern.getPattern(), //String typePattern
                                        objectSelects,            //StringList objectSelects,
                                        null,                     //StringList relationshipSelects,
                                        true,                     //boolean getTo,
                                        true,                     //boolean getFrom,
                                        (short)2,                 //short recurseToLevel,
                                        expandTypeWhere,          //String objectWhere,
                                        "",                       //String relationshipWhere,
                                        includeTypePattern,       //Pattern includeType,
                                        includeRelPattern,        //Pattern includeRelationship,
                                        null);                    //Map includeMap
    } else {
        // where for the query, show workspaces in the "Active" state only, and the users(roles) must have read access on the workspace.
        if (sFtr.equals("Active")) {
            queryTypeWhere = "('" + DomainObject.SELECT_CURRENT + "' == 'Active')";
        }
    }
    // get all workspaces that the current user is a member since one of his roles is a member
    MapList roleWorkspaceList = DomainObject.querySelect(context,
                                      DomainConstants.TYPE_PROJECT,                // type pattern
                                      DomainObject.QUERY_WILDCARD, // namePattern
                                      DomainObject.QUERY_WILDCARD, // revPattern
                                      DomainObject.QUERY_WILDCARD, // ownerPattern
                                      DomainObject.QUERY_WILDCARD,                 // get the Person Company vault
                                      queryTypeWhere,              // where expression
                                      true,                        // expandType
                                      objectSelects,               // object selects
                                      null,                        // cached list
                                      true);                       // use cache

    Iterator workspaceListItr = workspaceList.iterator();
    Iterator roleWorkspaceListItr = roleWorkspaceList.iterator();

    // get a list of workspace id's for the member
    StringList workspaceIdList = new StringList();
    String workspaceId  = null;
    while(workspaceListItr.hasNext())
    {
      Map workspaceMap = (Map)workspaceListItr.next();
      workspaceId = (String)workspaceMap.get(DomainObject.SELECT_ID);
      workspaceMap.remove("relationship");
      workspaceMap.remove("level");
      workspaceIdList.addElement(workspaceId);
    }

    while(roleWorkspaceListItr.hasNext())
    {
      Map workspaceMap = (Map)roleWorkspaceListItr.next();
      workspaceId = (String)workspaceMap.get(DomainObject.SELECT_ID);
      if(!workspaceIdList.contains(workspaceId))
      {
        workspaceIdList.addElement(workspaceId);
        workspaceList.add(workspaceMap);
      }
    }
   return workspaceList;
  }
      /**
     * showOwner - displays the owner with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
  public Vector showOwner(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector vecOwner = new Vector(objList.size());
            MapList busObjwsl = null;
            // Get the list of Selectables
            StringList strList = new StringList(1);
            strList.addElement(DomainObject.SELECT_OWNER);
            if ( objList != null)
            {
               String objIdArray[] = new String[objList.size()];
               //Get the array of Object Ids to be passed into the methods
               for (int i = 0; i < objList.size(); i++)
               {
                 Map objMap = (Map)objList.get(i);
                 objIdArray[i]  = (String)objMap.get("id");
               }
               busObjwsl = DomainObject.getInfo(context,
                                                objIdArray,
                                                strList);
               for (int i = 0; i < objList.size(); i++)
               {
                String strOwner=Person.getDisplayName(context,(String)((Map)busObjwsl.get(i)).get(DomainObject.SELECT_OWNER));
                vecOwner.add(strOwner);
               }
            }
            return vecOwner;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
          /**
     * showCheckbox - displays the owner with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
  public Vector showCheckbox(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector vecCheckbox = new Vector(objList.size());
            Person person = Person.getPerson(context);
            String spersonName = Person.getDisplayName(context, person.getName());
            MapList busObjwsl = null;
            // Get the list of Selectables
            StringList strList = new StringList(1);
            strList.addElement(DomainObject.SELECT_OWNER);
            if ( objList != null)
            {
               String objIdArray[] = new String[objList.size()];
               //Get the array of Object Ids to be passed into the methods
               for (int i = 0; i < objList.size(); i++)
               {
                 Map objMap = (Map)objList.get(i);
                 objIdArray[i]  = (String)objMap.get("id");
               }
               busObjwsl = DomainObject.getInfo(context,
                                                objIdArray,
                                                strList);
               for (int i = 0; i < objList.size(); i++)
               {
                String strOwner=((String)((Map)busObjwsl.get(i)).get(DomainObject.SELECT_OWNER));
                strOwner = Person.getDisplayName(context,strOwner);
                if(strOwner.equals(spersonName))
                  vecCheckbox.add("true");
                else
                   vecCheckbox.add("false");
               }
            }
            return vecCheckbox;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    public String getWorkspaceImage(matrix.db.Context context, String[] args) throws Exception {
        String strHTML = DomainConstants.EMPTY_STRING;
        try {
            Map programMap  = (HashMap) JPO.unpackArgs(args);
            Map requestMap  = (HashMap) programMap.get("requestMap");
            Map paramMap    = (HashMap) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");
            DomainObject doWorkspace = DomainObject.newInstance(context, objectId);

            doWorkspace.open(context);
            // Get the file and default format
            String strFileFormat = doWorkspace.getDefaultFormat(context);
            FileItr fileItr = new FileItr(doWorkspace.getFiles(context, strFileFormat));
            doWorkspace.close(context);

            String strFileName = "";
            if (fileItr.next()) {
                strFileName = fileItr.obj().getName();
            }

            // Modified for IR-037335V6R2011
            if (strFileName != null && !"null".equals(strFileName) && !"".equals(strFileName)) {
                ArrayList list = new ArrayList( );
                BusinessObjectProxy bop = new BusinessObjectProxy( objectId, strFileFormat, strFileName, false, false );
                list.add( bop );
                String[] imageURLs = ImageRequestData.getImageURLS( context, list, (HashMap) requestMap.get("ImageData") );
                if (imageURLs.length > 0) {
                    strHTML = "<img width='164' height='100' src='" + imageURLs[0] + "'>";
                }
            }
        } catch (Exception e) {
            throw e;
        }

        return strHTML;
    }

    public void updateWorkspaceBuyerDesk(matrix.db.Context context,String[] args) throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap=(HashMap)programMap.get("paramMap");
        String objectId = (String)paramMap.get("objectId");
        String strProjectId=objectId;
//modified for the bug 340154
        //   String strBuyerDeskId=(String)paramMap.get("New OID");
        HashMap requestMap=(HashMap)programMap.get("requestMap");
        String strBuyerDeskIdArr[]=(String[])requestMap.get("txtBuyerDeskId");
        String strBuyerDeskId = strBuyerDeskIdArr[0];
//till here
        Workspace workspace = (Workspace)DomainObject.newInstance(context,strProjectId,DomainConstants.TEAM);
        workspace.setId(strProjectId);
        workspace.open(context);
        BusinessObject oldBuyerDesk   = null;
        String oldBuyerDeskId         = null;
        String prevOwner = workspace.getOwner(context).getName();
        boolean bRemoveBuyer = false;
        Pattern relPat = new Pattern(DomainObject.RELATIONSHIP_WORKSPACE_BUYER_DESK);
        Pattern typePat = new Pattern(DomainObject.TYPE_BUYER_DESK);
        oldBuyerDesk = getConnectedObject(context, workspace ,relPat.getPattern(),typePat.getPattern(),false, true);
        Vector vectProjectMember  = new Vector();
        SelectList selectStmts    = new SelectList(1);
        selectStmts.addId();
        selectStmts.addName();
        ExpansionIterator personIter = null;
        try
        {
         if(strBuyerDeskId != null && !"".equals(strBuyerDeskId) && !"null".equals(strBuyerDeskId) )
         {
            boolean isBuyerDeskChanged = false;
            BusinessObject newBuyerDesk = new BusinessObject(strBuyerDeskId);
            if(oldBuyerDesk == null || "null".equals(oldBuyerDesk))
            {
                workspace.addBuyerDesk(context,strBuyerDeskId );
                isBuyerDeskChanged = true;
            } else
            {
                oldBuyerDeskId = oldBuyerDesk.getObjectId();
                // change Buyer Desk only if it is changed
                if( !oldBuyerDeskId.equals( strBuyerDeskId ))
                {
                  String[] selObjects = {oldBuyerDeskId};
                  workspace.removeBuyerDesks(context,selObjects );
                  workspace.addBuyerDesk(context,strBuyerDeskId );
                  isBuyerDeskChanged = true;
                }
            }
            if( isBuyerDeskChanged )
            {
                //Check if first time the Buyer Desk is connected
                if( oldBuyerDesk != null && !"null".equals(oldBuyerDesk) ) {
                // To remove the existing Buyer Desk person if the Buyer Desk is disconnected.
               /* personSelect = oldBuyerDesk.expandSelect(context,DomainObject.RELATIONSHIP_ASSIGNED_BUYER,DomainObject.TYPE_PERSON,
                                                   selectStmts,new StringList(),true, false, (short)1);*/
                  ContextUtil.startTransaction(context,false);
                  personIter = oldBuyerDesk.getExpansionIterator(context,DomainObject.RELATIONSHIP_ASSIGNED_BUYER,DomainObject.TYPE_PERSON,
                        selectStmts,new StringList(),true, false, (short)1,null,null,(short)0,false,false,(short)100,false);

                  try {
                      while (personIter.hasNext())
                          {
                              vectProjectMember.addElement( personIter.next().getTargetSelectData("id"));
                          }
                  } finally {
                      personIter.close();
                  }

                ContextUtil.commitTransaction(context);
               // To fetch the person connected to workspace.
               Pattern strPattern = new Pattern(DomainObject.RELATIONSHIP_PROJECT_MEMBERS);
               strPattern.addPattern(DomainObject.RELATIONSHIP_PROJECT_MEMBERSHIP);
               Pattern strTypePattern = new Pattern(DomainObject.TYPE_PROJECT_MEMBER);
               strTypePattern.addPattern(DomainObject.TYPE_PERSON);
               /*ExpansionWithSelect expMemberList = workspace.expandSelect(context,strPattern.getPattern(),strTypePattern.getPattern(),
                                                                  selectStmts, new StringList(),true, true, (short)2);
               RelationshipWithSelectItr memberItr = new RelationshipWithSelectItr(expMemberList.getRelationships());*/

               ContextUtil.startTransaction(context,false);
               ExpansionIterator expMemberList = workspace.getExpansionIterator(context,strPattern.getPattern(),strTypePattern.getPattern(),
               selectStmts, new StringList(),true, true, (short)2,null,null,(short)0,false,false,(short)100,false);
               try {
                   while (expMemberList.hasNext())
               {
                  RelationshipWithSelect relWS = expMemberList.next();
                  //if ( memberItr.obj().getTypeName().equals(DomainObject.RELATIONSHIP_PROJECT_MEMBERSHIP)) {
                  if ( relWS.getTypeName().equals(DomainObject.RELATIONSHIP_PROJECT_MEMBERSHIP)) {

                  String projectPersonId = relWS.getTargetSelectData("id");
                  BusinessObject projectMember = relWS.getTo();
                  BusinessObject projectPerson = relWS.getFrom();
                  projectPerson.open(context);
                  // To check if the person is owner Current workspace
                  boolean isOwner      = false;
                  if ( workspace.getOwner(context).getName().equals( projectPerson.getName())){
                isOwner = true;
              }
              if ( vectProjectMember.contains(projectPersonId) && canDelete(context, projectPerson, workspace ) && !isOwner ) {
                projectMember.remove(context);
              }
              projectPerson.close(context);
            }
          }
               } finally {
          expMemberList.close();
               }
          ContextUtil.commitTransaction(context);

        }
        // connect all the Buyers of new Desk to WorkSpace
        selectStmts = new SelectList(1);
        selectStmts.addId();

        ContextUtil.startTransaction(context,false);
        personIter = newBuyerDesk.getExpansionIterator(context,DomainObject.RELATIONSHIP_ASSIGNED_BUYER,DomainObject.TYPE_PERSON,
                                                  selectStmts,new StringList(),true, false, (short)1,null,null,(short)0,false,false,(short)100,false);

        while (personIter.hasNext()) {

            String projectMemberId = personIter.next().getTargetSelectData("id");

          if(projectMemberId != null) {
            BusinessObject boBuyer = new BusinessObject(projectMemberId);
            boBuyer.open(context);

            String strNewProjectMember = boBuyer.getName() + workspace.getRevision();

            BusinessObject boNewBuyerMember =  new BusinessObject( DomainObject.TYPE_PROJECT_MEMBER, strNewProjectMember, "-", getCompanyVault(context) );
            try{

              boNewBuyerMember.create(context, DomainObject.POLICY_PROJECT_MEMBER);

              // connect Person with ProjectMember
              boNewBuyerMember.open(context);
              boBuyer.connect(context, new RelationshipType(DomainObject.RELATIONSHIP_PROJECT_MEMBERSHIP), true, boNewBuyerMember);
              workspace.connect(context, new RelationshipType(DomainObject.RELATIONSHIP_PROJECT_MEMBERS), true, boNewBuyerMember);
              boNewBuyerMember.close(context);

            }catch( Exception e){
              // catch exception, this can heppen if the Buyer Desk person is alreday
              // a member of this Project
            }
            boBuyer.close(context);

          }
        }
        personIter.close();
        ContextUtil.commitTransaction(context);
      }
    } else if ( oldBuyerDesk != null && !"null".equals(oldBuyerDesk) && !"".equals(oldBuyerDesk) ) {
      oldBuyerDeskId = oldBuyerDesk.getObjectId();
      String[] selObjects = {oldBuyerDeskId};
      workspace.removeBuyerDesks(context, selObjects);
      /*personSelect = oldBuyerDesk.expandSelect(context,DomainObject.RELATIONSHIP_ASSIGNED_BUYER,DomainObject.TYPE_PERSON,
                                                       selectStmts,new StringList(),true, false, (short)1);

      relItr = new RelationshipWithSelectItr(personSelect.getRelationships());*/

      ContextUtil.startTransaction(context,false);
      personIter = oldBuyerDesk.getExpansionIterator(context,DomainObject.RELATIONSHIP_ASSIGNED_BUYER,DomainObject.TYPE_PERSON,
      selectStmts,new StringList(),true, false, (short)1,null,null,(short)0,false,false,(short)100,false);

       while (personIter.hasNext()) {
           vectProjectMember.addElement(personIter.next().getTargetSelectData("id"));
       }
       personIter.close();
       ContextUtil.commitTransaction(context);
      // To fetch the person connected to workspace.
      Pattern strPattern = new Pattern(DomainObject.RELATIONSHIP_PROJECT_MEMBERS);
      strPattern.addPattern(DomainObject.RELATIONSHIP_PROJECT_MEMBERSHIP);
      Pattern strTypePattern = new Pattern(DomainObject.TYPE_PROJECT_MEMBER);
      strTypePattern.addPattern(DomainObject.TYPE_PERSON);
      /*ExpansionWithSelect expMemberList = workspace.expandSelect(context,strPattern.getPattern(),strTypePattern.getPattern(),
                                                              selectStmts, new StringList(),true, true, (short)2);
      RelationshipWithSelectItr memberItr = new RelationshipWithSelectItr(expMemberList.getRelationships());*/
      ContextUtil.startTransaction(context,false);
      ExpansionIterator expMemberList = workspace.getExpansionIterator(context,strPattern.getPattern(),strTypePattern.getPattern(),
              selectStmts, new StringList(),true, true, (short)2,null,null,(short)0,false,false,(short)100,false);

    try {
      while (expMemberList.hasNext()) {
        /*if ( memberItr.obj().getTypeName().equals(DomainObject.RELATIONSHIP_PROJECT_MEMBERSHIP)) {
          projectMemberAttributesTable =  memberItr.obj().getTargetData();
          String projectPersonId   = (String)projectMemberAttributesTable.get("id");

          BusinessObject projectMember = memberItr.obj().getTo();
          BusinessObject projectPerson = memberItr.obj().getFrom();*/
          RelationshipWithSelect relWS = expMemberList.next();
          if ( relWS.getTypeName().equals(DomainObject.RELATIONSHIP_PROJECT_MEMBERSHIP)) {
          String projectPersonId   =  relWS.getTargetSelectData("id");
          BusinessObject projectMember = relWS.getTo();
          BusinessObject projectPerson = relWS.getFrom();
          projectPerson.open(context);

          // To check if the person is owner Current workspace
          boolean isOwner = false;
          boolean bcontextflag = false;

          if (prevOwner.equals( projectPerson.getName())){
            isOwner = true;
          }
          boolean ownerChanged=true;
          if (workspace.getOwner(context).getName().equals( prevOwner)){
            ownerChanged = false;
          }
          if ( vectProjectMember.contains(projectPersonId) && canDelete(context, projectPerson, workspace ) && isOwner ) {
            try {
              ContextUtil.pushContext(context,null, null, null);
              bcontextflag = true;
              if(ownerChanged)
              {
                workspace.removeProjectMember(context, strProjectId,projectPerson.getName(), projectMember.getObjectId());
                bRemoveBuyer=true;
                }
             } catch(Exception e ) { }
             finally {
              if(bcontextflag){
                 ContextUtil.popContext(context);
              }
             }
          }else {
            if (vectProjectMember.contains(projectPersonId) && canDelete(context, projectPerson, workspace) && !isOwner) {
              try {
                if(!workspace.getOwner(context).getName().equals(projectPerson.getName())) {
                  workspace.removeProjectMember(context, strProjectId,projectPerson.getName(), projectMember.getObjectId());
                }
              }catch(Exception ex){}
            }
           }
         projectPerson.close(context);
        }
      }
    } finally {
      expMemberList.close();
    }
      ContextUtil.commitTransaction(context);
    }
    workspace.close(context);

  } catch(MatrixException exp ) {
//    context.getSession().putValue("error.message", exp.getMessage());
  }
   //workspace.close(context);
 }

    private  boolean canDelete (Context context, BusinessObject projectPerson, BusinessObject boWorkspace ) {


    // To check if the project member is connected to any route.
    SelectList selectStmts                 = new SelectList(1);
    selectStmts.addId();
    Vector vectProjectMember               = new Vector();

    Pattern relPattern  = new Pattern(DomainObject.RELATIONSHIP_ROUTE_NODE);
    relPattern.addPattern(DomainObject.RELATIONSHIP_PROJECT_ROUTE);
    Pattern typePattern = new Pattern(DomainObject.TYPE_ROUTE);

    ExpansionIterator expIter = null;
    ExpansionIterator expWorkIter = null;


    try {

        ContextUtil.startTransaction(context,false);
        expIter = projectPerson.getExpansionIterator(context,relPattern.getPattern(),typePattern.getPattern(),
                selectStmts, new StringList(),true, true, (short)1,null,null,(short)0,false,false,(short)100,false);

        if (! expIter.hasNext()) {
            expIter.close();
            ContextUtil.commitTransaction(context);
            return true;
        } else {
            try {
                while (expIter.hasNext()) {
                    vectProjectMember.addElement( expIter.next().getTargetSelectData("id"));
                }
            } finally {
        expIter.close();
            }
        ContextUtil.commitTransaction(context);
        relPattern  = new Pattern(DomainObject.RELATIONSHIP_PROJECT_VAULTS);
        relPattern.addPattern(DomainObject.RELATIONSHIP_SUB_VAULTS);
        relPattern.addPattern(DomainObject.RELATIONSHIP_ROUTE_SCOPE);
        relPattern.addPattern(DomainObject.RELATIONSHIP_OBJECT_ROUTE);
        relPattern.addPattern(DomainObject.RELATIONSHIP_VAULTED_DOCUMENTS);
        typePattern = new Pattern(DomainObject.TYPE_PROJECT_VAULT);
        typePattern.addPattern(DomainObject.TYPE_ROUTE);
        typePattern.addPattern(DomainObject.TYPE_DOCUMENT);

        ContextUtil.startTransaction(context,false);
        expWorkIter = boWorkspace.getExpansionIterator(context,
                                                       relPattern.getPattern(),
                                                       typePattern.getPattern(),
                                                       selectStmts,
                                                       new StringList(),
                                                       false,
                                                       true,
                                                       (short)0,
                                                       null,
                                                       null,
                                                       (short)0,
                                                       false,
                                                       false,
                                                       (short)100,
                                                        false);

      if (! expWorkIter.hasNext() ) {
        expWorkIter.close();
        ContextUtil.commitTransaction(context);
        return true;
      }
      else {
          try {
              while (expWorkIter.hasNext()) {
                  RelationshipWithSelect relWorkWS = expWorkIter.next();
                  if ( relWorkWS.getTypeName().equals(DomainObject.RELATIONSHIP_ROUTE_SCOPE)) {
                      BusinessObject  busRoute = relWorkWS.getTo();
                      busRoute.open(context);
                      if ( vectProjectMember.contains(busRoute.getObjectId().trim()) ) {
                          return false;
                      }
                      busRoute.close(context);
                  }
              }
          } finally {
              expWorkIter.close();
          }
        ContextUtil.commitTransaction(context);
        return true;
      }

       /* if ( boGeneric.getRelationships().size() == 0 ) {
          return true;
        }
        else {

          relWorkItr = new RelationshipWithSelectItr(boGeneric.getRelationships());
          while (relWorkItr.next()) {
            if ( relWorkItr.obj().getTypeName().equals(DomainObject.RELATIONSHIP_ROUTE_SCOPE)) {
              BusinessObject  busRoute = relWorkItr.obj().getTo();
              busRoute.open(context);
              if ( vectProjectMember.contains(busRoute.getObjectId().trim()) ) {
                return false;
              }
              busRoute.close(context);
            }
          }
          return true;
        }*/
      }
    } catch (Exception ex) { }
    return false;
  }

      /**
     * Get an object connected to the given object based on the parameters.
     *
     * @param context The current context object.
     * @param object The object to expand.
     * @param relPattern The relationship pattern to expand along.
     * @param typePattern The type pattern to expand along.
     * @param getTo Define whether the expand should go in the "to" dir.
     * @param getFrom Define whether the expand should go in the "from" dir.
     * @return The connected object or null if an object is not connected.
     */

    private BusinessObject getConnectedObject(Context context,
            BusinessObject object, String relPattern, String typePattern,
            boolean getTo, boolean getFrom) throws Exception {

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
   /*   ExpansionWithSelect ews = object.expandSelect(context, relPattern,
              typePattern, busSelects, new SelectList(), getTo, getFrom,
              (short) 1);

      // If there is a connected object, create it from the id.
      RelationshipWithSelectItr relItr =
              new RelationshipWithSelectItr(ews.getRelationships());
      if (relItr.next()) {
        String objectId = (String) relItr.obj().getTargetData().get("id");
        connectedObject = new BusinessObject(objectId);
      }*/
      ContextUtil.startTransaction(context,false);
      ExpansionIterator expIterator = object.getExpansionIterator(context, relPattern,
              typePattern, busSelects, new SelectList(), getTo, getFrom,
              (short) 1,null,null,(short)0,false,false,(short)100,false);

      try {
          // If there is a connected object, create it from the id.
          if (expIterator.hasNext()) {
              String objectId = (String) expIterator.next().getTargetSelectData("id");
              connectedObject = new BusinessObject(objectId);
          }
      } finally {
          expIterator.close();
      }
    ContextUtil.commitTransaction(context);
      // Close the object, if necessary.
      if (closeConnection) {
        object.close(context);
      }

      // Return the connected object.
      return connectedObject;

    }

   private String getCompanyVault(matrix.db.Context context) throws MatrixException{

      Person personObj = Person.getPerson(context);
      Company compObj = personObj.getCompany(context);
      return compObj.getVault();
  }

  public boolean showSaveAsTemplateCommand(Context context,String[] args) throws Exception
  {
       HashMap programMap = (HashMap) JPO.unpackArgs(args);

       String objectId=(String)programMap.get("objectId");
       DomainObject project=new DomainObject(objectId);



       BusinessObject boPerson = getPerson(context);
       BusinessObject boMember = getProjectMember(context,objectId,boPerson);
       String sProjectLead="";
       if(boMember != null){
            boMember.open(context);
            sProjectLead  = getAttribute(context,boMember,PropertyUtil.getSchemaProperty(context, "attribute_ProjectAccess"));
            boMember.close(context);
         }


      if(sProjectLead.equals("Project Lead") &&  ! project.getInfo(context,DomainConstants.SELECT_CURRENT).equals("Archive") && (project.getOwner(context).toString()).equals(context.getUser()) )
      {
          return true;
      }
      else
      {
          return false;
      }
  }
  public BusinessObject getPerson(Context context) throws Exception
  {
         return PersonUtil.getPersonObject(context);
  }
  public BusinessObject getProjectMember( matrix.db.Context context,String projectId , BusinessObject person) throws MatrixException
  {
    StringList EMPTY_STRING_LIST = new StringList(0);
    Hashtable projectMemberHashtable = null;
    Hashtable personHashtable = null;
    projectMemberHashtable = new Hashtable();
    personHashtable = new Hashtable();
    projectMemberHashtable.put(person.getObjectId(), personHashtable);
    String sProjectMembershipRel = PropertyUtil.getSchemaProperty(context,"relationship_ProjectMembership");
    String sProjectMembersRel = PropertyUtil.getSchemaProperty(context,"relationship_ProjectMembers");
    String sProjectMemberType = PropertyUtil.getSchemaProperty(context,"type_ProjectMember");
    String typePattern = sProjectMemberType;
    String relPattern =  sProjectMembershipRel;
    String strWhereClause = "to[" + sProjectMembersRel + "].from.id ==" + projectId;

    ContextUtil.startTransaction(context,false);
    ExpansionIterator projectItr = person.getExpansionIterator(context, relPattern, typePattern, EMPTY_STRING_LIST,
            EMPTY_STRING_LIST,false, true, (short)1,strWhereClause,null,(short)0,false,false,(short)100,false);
    BusinessObject busProjectMember = null;
    try {
        if (projectItr.hasNext()) {
            busProjectMember = projectItr.next().getTo();
        }
    } finally {
    projectItr.close();
    }
    ContextUtil.commitTransaction(context);

    return busProjectMember;
  }

   public String getAttribute(matrix.db.Context context,BusinessObject busObj,String attrName) throws MatrixException
  {
    if (busObj == null){
      return "";
    }
    StringList selectStmts = new StringList();
    selectStmts.addElement("attribute[" + attrName + "]");
    BusinessObjectWithSelect _objectSelect = null;
    try {
      _objectSelect = busObj.select(context, selectStmts);
     }
    catch (MatrixException e)
    {
       busObj.open(context);
      _objectSelect = busObj.select(context, selectStmts);
      busObj.close(context);
    }
    finally {
      return _objectSelect.getSelectData("attribute[" + attrName + "]");
    }
  }

  /**
   * Returns "All" Filter Workspace Access List.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */



  public Object getAllWorkspaceAccess(Context context,String[] args) throws Exception
  {

      return constructWorkspaceAccessList(context,args,true,true,true);

  }

  /**
   * Returns "BuyerDesk" Filter Workspace Access List.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

  public Object getBuyerDeskWorkspaceAccess(Context context,String[] args) throws Exception
  {
      return constructWorkspaceAccessList(context,args,false,true,false);
  }

    /**
   * Returns "Person" Filter Workspace Access List.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

  public Object getPersonsWorkspaceAccess(Context context,String[] args) throws Exception
  {
      return constructWorkspaceAccessList(context,args,true,false,false);
  }

    /**
   * Returns "Roles" Filter Workspace Access List.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Object getRolesWorkspaceAccess(Context context,String[] args) throws Exception
  {
      return constructWorkspaceAccessList(context,args,false,false,true);
  }



  /**
   * The main method which all filter access method calls.
   * Returns Workspace Access List Based on the parameters.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Object constructWorkspaceAccessList(Context context,String[] args,boolean bPersonFlag,boolean bBuyerFlag,boolean bRoleFlag) throws Exception
  {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);

      String language = (String) programMap.get("languageStr");
      language = language == null ? context.getLocale().getLanguage() : language;

      Person PersonObj = (Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
      DomainObject BaseObject     = DomainObject.newInstance(context);
      DomainObject Workspace      = DomainObject.newInstance(context);
      DomainObject parentObj      = DomainObject.newInstance(context);
      String sAttrCreateRoute     = DomainObject.ATTRIBUTE_CREATE_ROUTE;

      String sAttrCreateFolder    = DomainObject.ATTRIBUTE_CREATE_FOLDER;

      String objectId             = (String)programMap.get("objectId");
      String workspaceId          = (String)programMap.get("workspaceId");
      String folderId             = (String)programMap.get("folderId");
      String sParentId            = "";

     //   boolean bPersonFlag = true;
     // boolean bBuyerFlag  = true;
     // boolean bRoleFlag   = true;
      HashMap i18nMap     = new HashMap();
      i18nMap.put("Read","Read");
      i18nMap.put("Read Write","ReadWrite");
      i18nMap.put("Add","Add");
      i18nMap.put("Remove","Remove");
      i18nMap.put("Add Remove","AddRemove");
      i18nMap.put("None","None");
      BaseObject.setId(objectId);
      String sTypeName = BaseObject.getInfo(context, BaseObject.SELECT_TYPE);
      /*............*/
      if(sTypeName.equals(BaseObject.TYPE_WORKSPACE))
      {
        Workspace = BaseObject;
      }
      else if(sTypeName.equals(BaseObject.TYPE_WORKSPACE_VAULT) && (workspaceId ==null ||  workspaceId.equals("")))
      {
        String sProjectId = getProjectId(context,objectId);
        Workspace.setId(sProjectId);
      }
      else
      {
        Workspace.setId(workspaceId);
      }

      if(sTypeName.equals(BaseObject.TYPE_PROJECT_VAULT) )
      {
        sParentId = BaseObject.getInfo(context,"to[" + BaseObject.RELATIONSHIP_SUBVAULTS + "].from.id");
        if(sParentId == null || sParentId.equals(""))
        {
          sParentId = BaseObject.getInfo(context,"to[" + BaseObject.RELATIONSHIP_WORKSPACE_VAULTS+ "].from.id");
        }
        parentObj.setId(sParentId);
      }
      else if (!sTypeName.equals(BaseObject.TYPE_WORKSPACE))
      {
        parentObj.setId(folderId);
      }
      /*........*/
      String sGrantee = "";
      String sAccessString ="";

      Map map                 = null;
      HashMap hashMap         = null;
      boolean bAssignedBuyer  = false;
      AccessUtil accessUtil   = new AccessUtil();
      String sBuyerDesk   = "from[" + BaseObject.RELATIONSHIP_WORKSPACE_BUYER_DESK + "].to.id";
      String sBuyerDeskId = "";
      StringList objectSelects = new StringList();
      sBuyerDeskId = Workspace.getInfo(context,sBuyerDesk);


       String sAttSelProjectAccess = "attribute[" + BaseObject.ATTRIBUTE_PROJECT_ACCESS + "].value";
      String sAttSelCreateRoute   = "attribute[" + sAttrCreateRoute + "].value";
      String sAttSelCreateFolder  = "attribute[" + sAttrCreateFolder + "].value";
      String sSelOrgName          = "";
      String sSelOrgId            = "";
      String sSelProjectMemberId  = "";
      String sSelPersonId         = "";
      String sSelPersonName       = BaseObject.SELECT_NAME;
      String sSelPersonLastName   = "";
      String sSelPersonFirstName  = "";
      String sSelPersonType       = BaseObject.SELECT_TYPE;
      String sAssignBuyerDesk     = "";
      StringList selectTypeStmts  = new StringList();
      selectTypeStmts.add(Workspace.SELECT_CURRENT);
      selectTypeStmts.add(Workspace.SELECT_OWNER);

      Pattern patternRelationship = new Pattern(BaseObject.RELATIONSHIP_ROUTE_NODE);
      Iterator granteeItr               = null;

      String sState                     = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from."+ BaseObject.SELECT_CURRENT;
      patternRelationship.addPattern(BaseObject.RELATIONSHIP_PROJECT_ROUTE);
      sSelPersonId         = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.id";
      sSelOrgName          = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.to[" + BaseObject.RELATIONSHIP_EMPLOYEE + "].from.name";
      sSelOrgId            = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.to[" + BaseObject.RELATIONSHIP_EMPLOYEE + "].from.id";
      sSelPersonName       = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.name";
      sSelPersonLastName   = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from."+ PersonObj.SELECT_LAST_NAME +".value";
      sSelPersonFirstName  = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from."+ PersonObj.SELECT_FIRST_NAME +".value";
      sAssignBuyerDesk     = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.from[" + BaseObject.RELATIONSHIP_ASSIGNED_BUYER+ "].to.id";
      sSelProjectMemberId  = BaseObject.SELECT_ID;

      StringList granteeList = new StringList();
      MapList AccessMapList  =  new  MapList();
          StringList sPersonList = new StringList();
    // Get the current and parent object Access list
    AccessList baseAccessList = BaseObject.getAccessForGrantor(context, AccessUtil.WORKSPACE_ACCESS_GRANTOR);
    AccessList parentAccessList = new AccessList() ;
    if(!sTypeName.equals(BaseObject.TYPE_WORKSPACE)) {
      parentAccessList = parentObj.getAccessForGrantor(context, AccessUtil.WORKSPACE_ACCESS_GRANTOR);
    }

    objectSelects.addElement(BaseObject.SELECT_ID);
    objectSelects.addElement(sAttSelProjectAccess);
    objectSelects.addElement(sAttSelCreateRoute);
    objectSelects.addElement(sAttSelCreateFolder);
    objectSelects.addElement(sSelOrgName);
    objectSelects.addElement(sSelProjectMemberId);
    objectSelects.addElement(sSelPersonId);
    objectSelects.addElement(sSelPersonLastName);
    objectSelects.addElement(sSelPersonFirstName);
    objectSelects.addElement(sSelPersonName);
    objectSelects.addElement(sSelOrgId);
    objectSelects.addElement(sState);
    objectSelects.addElement(BaseObject.SELECT_ID);

    BaseObject.MULTI_VALUE_LIST.add(sAssignBuyerDesk);
    objectSelects.addElement(sAssignBuyerDesk);
    //Added for Bug 353656 starts
   if(sTypeName.equals(BaseObject.TYPE_WORKSPACE_VAULT)&& sParentId !=null && !sParentId.equals(""))
    granteeItr = (parentObj.getGrantees(context)).iterator();
   else
       granteeItr = (Workspace.getGrantees(context)).iterator();
    granteeList = new StringList();
    while(granteeItr.hasNext())
    {
      String grantee = (String) granteeItr.next();
      if(!granteeList.contains(grantee))
      {
          granteeList.addElement(grantee);
      }
    }
    //Added for Bug 353656 ends
    granteeItr = BaseObject.getGrantees(context).iterator();
    while(granteeItr.hasNext()) {
      String grantee = (String) granteeItr.next();
      if(!granteeList.contains(grantee)) {
        granteeList.addElement(grantee);
      }
    }

    MapList mapList = Workspace.getRelatedObjects(context,
                                                  BaseObject.RELATIONSHIP_PROJECT_MEMBERS,
                                                  BaseObject.TYPE_PROJECT_MEMBER,
                                                  objectSelects,
                                                  null,
                                                  false,
                                                  true,
                                                  (short)1,
                                                  "",
                                                  "",
                                                  null,
                                                  null,
                                                  null);

    Iterator mapItr = mapList.iterator();
    while(mapItr.hasNext()) {
    bAssignedBuyer = false;
      map             = (Map)mapItr.next();
      sGrantee        = (String)map.get(sSelPersonName);
      String strState =  (String)map.get(sState);
      sPersonList.addElement(sGrantee);

      if (strState.equals("Active") && granteeList.contains(sGrantee) && (bPersonFlag || bBuyerFlag))
      {
      hashMap = new HashMap();
      hashMap.put("name", sGrantee);
      hashMap.put("ProjectLead", map.get(sAttSelProjectAccess));
      hashMap.put("CreateRoute", map.get(sAttSelCreateRoute));
      hashMap.put("CreateFolder", map.get(sAttSelCreateFolder));
      hashMap.put(sSelOrgName,map.get(sSelOrgName));
      hashMap.put("ProjectMemberID", map.get(sSelProjectMemberId));
      hashMap.put(sSelPersonId, map.get(sSelPersonId));
      hashMap.put("OrgId", map.get(sSelOrgId));
      hashMap.put("LastFirstName", (String)map.get(sSelPersonLastName) + ","+ (String)map.get(sSelPersonFirstName));
      hashMap.put(BaseObject.SELECT_ID,map.get(BaseObject.SELECT_ID));
      if(!((String)map.get(sAttSelProjectAccess)).equals("Project Lead")) {
          getAccess(context, baseAccessList, parentAccessList, sGrantee, sTypeName, hashMap, accessUtil);
        } else {
                    /*.....*/
                  sAccessString = accessUtil.ADD_REMOVE;
                  if(!sTypeName.equals(BaseObject.TYPE_WORKSPACE))
                  {
                    hashMap.put("InheritedAccess", sAccessString);
                    hashMap.put("SpecificAccess", sAccessString);
                  }
                  else
                  {
                    hashMap.put("Access", sAccessString);
                  }
                  /*.....*/
          //sAccessString = accessUtil.ADD_REMOVE;
          //hashMap.put("Access", sAccessString);
        }

        //to check if any Buyer Desk Attached to the Workspace
        if(sBuyerDeskId != null) {
            StringList sBuyerDeskList = new StringList();
            try {
              String sBuyerDeskIdString = (String)map.get(sAssignBuyerDesk);
              if (sBuyerDeskIdString != null) {
                sBuyerDeskList.addElement(sBuyerDeskIdString);
              }
            } catch (ClassCastException classCastEx ) {
              sBuyerDeskList = (StringList) map.get(sAssignBuyerDesk);
            }
            //To check whether the Person is the Assigned Buyer of the
            //BuyerDesk Connected to the Workspace
            bAssignedBuyer = isAssignedBuyer(sBuyerDeskList,sBuyerDeskId);
        }

        //To Display only Buyer Desk Persons
        if(bBuyerFlag) {
            if(bAssignedBuyer) {
              hashMap.put(sSelPersonType, "BuyerDesk Person");
              hashMap.put("isBuyer","true");
              AccessMapList.add(hashMap);
            }
        }

        //To display only Project Members other than Assigned Buyers
        if(bPersonFlag && (!bAssignedBuyer)) {
          hashMap.put(sSelPersonType, BaseObject.TYPE_PERSON);
          hashMap.put("isBuyer","false");
          AccessMapList.add(hashMap);
        }
      }
    }

    granteeItr = granteeList.iterator();
    while(granteeItr.hasNext()) {
      sGrantee = (String)granteeItr.next();
      if(!sPersonList.contains(sGrantee) && (bRoleFlag)) {
        hashMap = new HashMap();
        hashMap.put("name", sGrantee);
        hashMap.put("ProjectLead", "");
        hashMap.put("CreateRoute", "");
        hashMap.put("CreateFolder","");
        hashMap.put(sSelOrgName, "");
        hashMap.put("ProjectMemberID", "");
        hashMap.put(sSelPersonId, "");
        hashMap.put(sSelPersonType, "Role");
        hashMap.put("OrgId", "null");
        hashMap.put("isBuyer", "false");
        hashMap.put("LastFirstName", i18nNow.getRoleI18NString(sGrantee,language));
        // Need to include a row id entry in order to be able to select
        // the row for removal.
        hashMap.put(BaseObject.SELECT_ID, "Role:"+sGrantee);
        getAccess(context, baseAccessList, parentAccessList, sGrantee, sTypeName, hashMap, accessUtil);
        AccessMapList.add(hashMap);
      }
    }

    return AccessMapList;
  }


  /**
   * Method to show person name along with its image icon
   * Returns a Vector of person names (HTML) Output
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector showPersonName(Context context,String[] args) throws Exception {
     Vector personNames=new Vector();

     HashMap programMap=(HashMap)JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramList");
     MapList objectList = (MapList)programMap.get("objectList");

     String reportFormat = (String)paramMap.get("reportFormat");
     boolean isReportMode = !UIUtil.isNullOrEmpty(reportFormat);
     boolean isTextReport = isReportMode && ("CSV".equalsIgnoreCase(reportFormat) || "Text".equalsIgnoreCase(reportFormat));

     String jsTreeID=(String)paramMap.get("jsTreeID");

     String workspaceId = (String)paramMap.get("workspaceId");
     workspaceId = UIUtil.isNullOrEmpty(workspaceId) ? (String)paramMap.get("parentOID") : workspaceId;

     String folderId = (String)paramMap.get("folderId");

     String sSelPersonId   = "to[" + RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.id";

     String imgRole = "<img src='images/iconSmallRole.gif' border='0' id=''/>";
     String imgPerson = "<img src='images/iconSmallPerson.gif' border='0' id=''/>";

     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();) {
         HashMap mapAccess = (HashMap)objectListItr.next();

         String sDisplayPersonName  = (String)mapAccess.get("LastFirstName");
         String userType   = (String)mapAccess.get(SELECT_TYPE);
         boolean isRoleType = "Role".equals(userType);

        StringBuffer memberDisplayStrBuffer = new StringBuffer();
        if(isTextReport) {
            memberDisplayStrBuffer.append(XSSUtil.encodeForHTML(context, sDisplayPersonName));
        } else if(isRoleType || (isReportMode && !isTextReport)) {
            memberDisplayStrBuffer.append(isRoleType ? imgRole : imgPerson);
            memberDisplayStrBuffer.append("&nbsp;");
            memberDisplayStrBuffer.append(XSSUtil.encodeForHTML(context, sDisplayPersonName));
        } else {
            String sPersonId           = (String)mapAccess.get(sSelPersonId);
            String sOrgId              = (String)mapAccess.get("OrgId");

            StringBuffer buffer = new StringBuffer(200);
            buffer.append("emxTree.jsp?AppendParameters=true&mode=insert");
            buffer.append("&objectId=").append(XSSUtil.encodeForJavaScript(context, sPersonId));
            buffer.append("&jsTreeID=").append(XSSUtil.encodeForJavaScript(context, jsTreeID));
            buffer.append("&workspaceId=").append(XSSUtil.encodeForJavaScript(context, workspaceId));
            buffer.append("&folderId=").append(XSSUtil.encodeForJavaScript(context, folderId));
            buffer.append("&OrgId=").append(XSSUtil.encodeForJavaScript(context, sOrgId));

            memberDisplayStrBuffer.append("<a href=\"");
            memberDisplayStrBuffer.append(buffer);
            memberDisplayStrBuffer.append("\" target=\"content\"  class='object'>");
            memberDisplayStrBuffer.append(imgPerson);
            memberDisplayStrBuffer.append("</a>");
            memberDisplayStrBuffer.append("&nbsp;");
            memberDisplayStrBuffer.append("<a href=\"");
            memberDisplayStrBuffer.append(buffer);
            memberDisplayStrBuffer.append("\" target=\"content\"  class='object'>");
            memberDisplayStrBuffer.append(XSSUtil.encodeForHTML(context, sDisplayPersonName));
            memberDisplayStrBuffer.append("</a>");
        }
        personNames.add(memberDisplayStrBuffer.toString());
     }
     return personNames;
 }

   /**
   * Method to show Project Lead Access Image Icon
   * Returns a Vector of person names (HTML) Output
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector showProjectLeadIcon(Context context,String[] args) throws Exception
 {


        Vector projectLeadIcons=new Vector();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap paramList = (HashMap)programMap.get("paramList");
        HashMap requestValuesMap = (HashMap)paramList.get("RequestValuesMap");
        String languageStr = (String)requestValuesMap.get("languageStr");
        String stringResFileId = "emxTeamCentralStringResource";

        MapList objectList=(MapList)programMap.get("objectList");
        for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
        {
         HashMap mapAccess=(HashMap)objectListItr.next();
         String sProjectLead        = (String)mapAccess.get("ProjectLead");
         if ((sProjectLead != null) && (sProjectLead.equalsIgnoreCase("Project Lead")))
         {
            projectLeadIcons.add("<img border=\"0\" src=\"../teamcentral/images/iconProjectLead.gif\" alt=\""+EnoviaResourceBundle.getProperty(context, stringResFileId, new Locale(languageStr), "emxTeamCentral.Access.WorkspaceLead")+"\" title=\""+EnoviaResourceBundle.getProperty(context, stringResFileId, new Locale(languageStr), "emxTeamCentral.Access.WorkspaceLead")+"\" />");
         }
         else
         {
            projectLeadIcons.add("&nbsp;");
         }

        }
        //XSSOK
        return projectLeadIcons;


 }

   /**
   * Method to show Create Route image icon
   * Returns a Vector of Create Route Images (HTML) Output
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector showCreateRouteIcon(Context context,String[] args) throws Exception
 {


        Vector createRouteIcons=new Vector();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap paramList = (HashMap)programMap.get("paramList");
        HashMap requestValuesMap = (HashMap)paramList.get("RequestValuesMap");
        String languageStr = (String)requestValuesMap.get("languageStr");
        String stringResFileId = "emxTeamCentralStringResource";

        MapList objectList=(MapList)programMap.get("objectList");
        for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
        {
         HashMap mapAccess=(HashMap)objectListItr.next();
         String sCreateRoute        = (String)mapAccess.get("CreateRoute");
         if ((sCreateRoute != null)&& (sCreateRoute.equalsIgnoreCase("Yes")))
         {
            createRouteIcons.add("<img border=\"0\" src=\"images/iconSmallRoute.gif\" alt=\""+EnoviaResourceBundle.getProperty(context, stringResFileId, new Locale(languageStr), "emxTeamCentral.Access.CreateRoute")+"\" title=\""+EnoviaResourceBundle.getProperty(context,stringResFileId, new Locale(languageStr), "emxTeamCentral.Access.CreateRoute")+"\" />");
         }
         else
         {
            createRouteIcons.add("&nbsp;");
         }

        }
        //XSSOK
        return createRouteIcons;



 }


   /**
   * Method to show Create Folder Image Icon
   * Returns a Vector of images (HTML) Output
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector showCreateFolderIcon(Context context,String[] args) throws Exception
 {


        Vector createFolderIcons=new Vector();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap paramList = (HashMap)programMap.get("paramList");
        HashMap requestValuesMap = (HashMap)paramList.get("RequestValuesMap");
        String languageStr = (String)requestValuesMap.get("languageStr");
        String stringResFileId = "emxTeamCentralStringResource";

        MapList objectList=(MapList)programMap.get("objectList");
        for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
        {
         HashMap mapAccess=(HashMap)objectListItr.next();
         String sCreateFolder       = (String)mapAccess.get("CreateFolder");
         if ((sCreateFolder != null)&& (sCreateFolder.equalsIgnoreCase("Yes")))
         {
            createFolderIcons.add("<img border=\"0\" src=\"images/iconSmallFolder.gif\" alt=\""+EnoviaResourceBundle.getProperty(context,stringResFileId, new Locale(languageStr), "emxTeamCentral.CreateFolder.Tooltip")+"\" title=\""+EnoviaResourceBundle.getProperty(context, stringResFileId, new Locale(languageStr), "emxTeamCentral.CreateFolder.Tooltip")+"\" />");
         }
         else
         {
            createFolderIcons.add("&nbsp;");
         }

        }


        //XSSOK
        return createFolderIcons;
 }

   /**
   * Method to get Access Types
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector getAccessTypes(Context context,String[] args) throws Exception
 {

     HashMap programMap=(HashMap)JPO.unpackArgs(args);
     MapList objectList=(MapList)programMap.get("objectList");
     HashMap paramMap=(HashMap)programMap.get("paramList");
     String languageStr = (String)paramMap.get("languageStr");
     String objectId=(String)programMap.get("objectId");
     DomainObject BaseObject=new DomainObject(objectId);
     Vector accessTypes=new Vector();
     String sSelPersonType       = BaseObject.SELECT_TYPE;
     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
     {
        HashMap mapAccess=(HashMap)objectListItr.next();
        String sType = (String)mapAccess.get(sSelPersonType);

// Added for IR-024770V6R2011 Dated 08/12/2009 Begins.
        if ("Role".equals(sType)){
            accessTypes.add(EnoviaResourceBundle.getFrameworkStringResourceProperty(context,  "emxFramework.Common.Role", new Locale(languageStr)));
     }
        else if("Group".equals(sType)){
            accessTypes.add(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Common.Group", new Locale(languageStr)));
        }
        else {
        accessTypes.add(i18nNow.getTypeI18NString(sType,languageStr));
        }
// Added for IR-024770V6R2011 Dated 08/12/2009 Ends.

     }


     return accessTypes;
 }


   /**
   * Method to show Access Organizations
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector getAccessOrganizations(Context context,String[] args) throws Exception
{

      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      MapList objectList=(MapList)programMap.get("objectList");
      String objectId=(String)programMap.get("objectId");
      DomainObject BaseObject=new DomainObject(objectId);
      Vector accessOrganizations=new Vector();
      String sSelOrgName="to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.to[" + BaseObject.RELATIONSHIP_EMPLOYEE + "].from.name";
      for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
      {
        HashMap mapAccess=(HashMap)objectListItr.next();
        String sOrgName = (String)mapAccess.get(sSelOrgName);
         accessOrganizations.add(sOrgName);
      }


      return accessOrganizations;
}


  /**
   * Method to show Available Access (Specific Access)
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector getAvailableAccesses(Context context,String[] args) throws Exception
 {

        Vector availableAccesses=new Vector();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap paramMap=(HashMap)programMap.get("paramList");
        String languageStr = (String)paramMap.get("languageStr");
        String stringResFileId = "emxTeamCentralStringResource";

        HashMap i18nMap     = new HashMap();
          i18nMap.put("Read","Read");
          i18nMap.put("Read Write","ReadWrite");
          i18nMap.put("Add","Add");
          i18nMap.put("Remove","Remove");
          i18nMap.put("Add Remove","AddRemove");
          i18nMap.put("None","None");
        MapList objectList=(MapList)programMap.get("objectList");
        for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
        {
         HashMap mapAccess=(HashMap)objectListItr.next();
         if("None".equals((String)mapAccess.get("Access")))
         {
            availableAccesses.add(EnoviaResourceBundle.getProperty(context, stringResFileId, new Locale(languageStr), "emxTeamCentral.Access.Basic"));
         }
         else if ((AccessUtil.WORKSPACE_LEAD).equals((String)mapAccess.get("Access")))
         {
            availableAccesses.add(EnoviaResourceBundle.getProperty(context, stringResFileId, new Locale(languageStr), "emxTeamCentral.Access.AddRemove"));
         }
         else
         {
              availableAccesses.add(EnoviaResourceBundle.getProperty(context, stringResFileId, new Locale(languageStr), "emxTeamCentral.Access."+(String)i18nMap.get(mapAccess.get("Access"))));
         }


        }



        return availableAccesses;

 }


  /**
   * Access Method to Checkbox in Access Summary page
   * Returns a Vector of boolean values
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector showAccessCheckBox(Context context,String[] args) throws Exception
 {

     Vector accesssCheckBoxes=new Vector();
     HashMap programMap=(HashMap)JPO.unpackArgs(args);

     Person PersonObj = (Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
     String stateComplete = FrameworkUtil.lookupStateName(context, DomainObject.POLICY_ROUTE, "state_Complete");
     String sRouteStatus="";
     MapList objectList=(MapList)programMap.get("objectList");
     HashMap paramMap=(HashMap)programMap.get("paramList");
     String objectId=(String)paramMap.get("objectId");
     String workspaceId=(String)paramMap.get("workspaceId");
     if(workspaceId == null)workspaceId=(String)paramMap.get("parentOID");
     if(workspaceId == null)workspaceId=objectId;
     DomainObject Workspace=new DomainObject(workspaceId);
     DomainObject BaseObject=new DomainObject(objectId);
     String sOwner    = BaseObject.getInfo(context, BaseObject.SELECT_OWNER);
     String sSelPersonType       = BaseObject.SELECT_TYPE;
     String sSelPersonId         = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.id";
     String sSelOrgName          = "to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.to[" + BaseObject.RELATIONSHIP_EMPLOYEE + "].from.name";
     String sTypeWhere = "from[" + BaseObject.RELATIONSHIP_MEMBER_ROUTE + "].to.to[" + BaseObject.RELATIONSHIP_PROJECT_MEMBERS +"].from.id == " + Workspace.getId(context);
     Pattern patternRelationship = new Pattern(BaseObject.RELATIONSHIP_ROUTE_NODE);
     patternRelationship.addPattern(BaseObject.RELATIONSHIP_PROJECT_ROUTE);
     StringList selectTypeStmts  = new StringList();
     selectTypeStmts.add(Workspace.SELECT_CURRENT);
     selectTypeStmts.add(Workspace.SELECT_OWNER);

     //
     // Check whether context user is workspace lead
     //
     DomainObject projectMember = null;
     boolean isProjectLead = false;
     try {
         projectMember = DomainObject.newInstance(context,(getProjectMember(context,objectId,getPerson(context))));
     }
     catch (Exception ex) {
         projectMember = null;
     }
     if(projectMember != null ) {
        String sProjectLead = projectMember.getAttributeValue (context, DomainObject.ATTRIBUTE_PROJECT_ACCESS);
        if("Project Lead".equals(sProjectLead)) {
            isProjectLead = true;
        }
     }



     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
     {
          boolean sHasActiveRoute = false;
          boolean bBuyer          = false;
          HashMap mapAccess=(HashMap)objectListItr.next();

          //Bug 282994
          String sProjectMemberId    = (String)mapAccess.get("ProjectMemberID");
          String sPersonName         = (String)mapAccess.get("name");
          String sPersonId           = (String)mapAccess.get(sSelPersonId);
          String sOrgId              = (String)mapAccess.get("OrgId");
          String sBuyer       = (String)mapAccess.get("isBuyer");
         String sId =sPersonId +"~Company";

        if((sBuyer!= null) && (sBuyer.equals("true")))
        {
            bBuyer = true;
        }

        String sRouteOwner = "";

        if(sProjectMemberId != null && !sProjectMemberId.equals(""))
        {
              PersonObj.setId(sPersonId);
              String sMemberName = PersonObj.getInfo(context, PersonObj.SELECT_NAME);
              MapList taskMapList =  PersonObj.getRelatedObjects(context,
                                                        patternRelationship.getPattern(),
                                                        BaseObject.TYPE_ROUTE,
                                                        selectTypeStmts,
                                                        null,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        sTypeWhere,
                                                        "",
                                                        null,
                                                        null,
                                                        null);

             Iterator mapRouteItr = taskMapList.iterator();
             while(mapRouteItr.hasNext())
             {
               Map mapRoute = (Map)mapRouteItr.next();
               sRouteStatus = (String)mapRoute.get(Workspace.SELECT_CURRENT);
               sRouteOwner = (String)mapRoute.get(Workspace.SELECT_OWNER);
               if(!sRouteStatus.equals(stateComplete)|| sRouteOwner.equals(sMemberName))
               {
                  sHasActiveRoute = true;
                  break;
               }
            }
        }

        // For Bug:346795
        // As per documentation,
        // People designated as a Workspace Lead for the workspace can use the page to add and remove members and change accesses.
        if (!isProjectLead) {
            accesssCheckBoxes.add("false");
        }
        else {
            if(sOwner.equals(sPersonName) || sHasActiveRoute || bBuyer)
            {
                accesssCheckBoxes.add("false");
            }
            else
            {
                accesssCheckBoxes.add("true");
            }
        }
         }

     return accesssCheckBoxes;
 }


   /**
   * Access Method to show Toolbar Commands
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return boolean
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public boolean showWorkspaceAccessCommands(Context context,String[] args) throws Exception
 {
     HashMap programMap=(HashMap)JPO.unpackArgs(args);
     String sProjectLead = "";

     String objectId=(String)programMap.get("objectId");

     DomainObject workspace=new DomainObject(objectId);
     String sOwner=workspace.getOwner(context).toString();
     String sUser=context.getUser();
     String sAttrProjectAccess=DomainObject.ATTRIBUTE_PROJECT_ACCESS;
     //Bug 282994
     DomainObject projectMember = null;
     if(sOwner.equals(sUser))
     {
         return true;
     }
     else{
     try
     {
         projectMember = DomainObject.newInstance(context,(getProjectMember(context,objectId,getPerson(context))));
     }
     catch (Exception ex)
     {
         projectMember = null;
     }

     if(projectMember != null )
     {
        DomainObject projMemberObj = new DomainObject(projectMember);
        sProjectLead        = projMemberObj.getAttributeValue(context,sAttrProjectAccess);
     }
    return  sProjectLead.equals("Project Lead");
     }
 }

   /**
   * Method to show the PersonName in SubFolder Access  List
   * Returns a Vector of person names along with image icons
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Vector showSubFolderPersonName(Context context,String[] args) throws Exception  {
     Vector personNames = new Vector();

     HashMap programMap=(HashMap)JPO.unpackArgs(args);
     HashMap paramMap=(HashMap)programMap.get("paramList");

     MapList objectList=(MapList)programMap.get("objectList");
     String sSelPersonId  = "to[" + RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.id";

     String reportFormat = (String)paramMap.get("reportFormat");
     boolean isReportMode = !UIUtil.isNullOrEmpty(reportFormat);
     boolean isTextReport = isReportMode && ("CSV".equalsIgnoreCase(reportFormat) || "Text".equalsIgnoreCase(reportFormat));

     String jsTreeID=(String)paramMap.get("jsTreeID");
     String workspaceId=(String)paramMap.get("workspaceId");
     String folderId=(String)paramMap.get("folderId");

     String imgRole = "<img src='images/iconSmallRole.gif' border='0' id=''/>";
     String imgPerson = "<img src='images/iconSmallPerson.gif' border='0' id=''/>";

     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();) {
         HashMap mapAccess=(HashMap)objectListItr.next();

         String sDisplayPersonName  = (String)mapAccess.get("LastFirstName");
         String userType   = (String)mapAccess.get(SELECT_TYPE);
         boolean isRoleType = "Role".equals(userType);

         StringBuffer memberDisplayStrBuffer = new StringBuffer();
         if(isTextReport) {
             memberDisplayStrBuffer.append(XSSUtil.encodeForHTML(context, sDisplayPersonName));
         } else if(isRoleType || (isReportMode && !isTextReport)) {
             memberDisplayStrBuffer.append(isRoleType ? imgRole : imgPerson);
             memberDisplayStrBuffer.append("&nbsp;");
             memberDisplayStrBuffer.append(XSSUtil.encodeForHTML(context, sDisplayPersonName));
         } else {
             String sPersonId           = (String)mapAccess.get(sSelPersonId);
             String sOrgId              = (String)mapAccess.get("OrgId");

             StringBuffer buffer = new StringBuffer(200);
             buffer.append("emxTree.jsp?AppendParameters=true&mode=insert");
             buffer.append("&objectId=").append(XSSUtil.encodeForJavaScript(context, sPersonId));
             buffer.append("&jsTreeID=").append(XSSUtil.encodeForJavaScript(context, jsTreeID));
             buffer.append("&workspaceId=").append(XSSUtil.encodeForJavaScript(context, workspaceId));
             buffer.append("&folderId=").append(XSSUtil.encodeForJavaScript(context, folderId));
             buffer.append("&OrgId=").append(XSSUtil.encodeForJavaScript(context, sOrgId));

             memberDisplayStrBuffer.append("<a href=\"");
             memberDisplayStrBuffer.append(buffer);
             memberDisplayStrBuffer.append("\" target=\"content\"  class='object'>");
             memberDisplayStrBuffer.append(imgPerson);
             memberDisplayStrBuffer.append("</a>");
             memberDisplayStrBuffer.append("&nbsp;");
             memberDisplayStrBuffer.append("<a href=\"");
             memberDisplayStrBuffer.append(buffer);
             memberDisplayStrBuffer.append("\" target=\"content\"  class='object'>");
             memberDisplayStrBuffer.append(XSSUtil.encodeForHTML(context, sDisplayPersonName));
             memberDisplayStrBuffer.append("</a>");
         }
         personNames.add(memberDisplayStrBuffer.toString());
     }
     return personNames;
 }

    /**
   * Method to get Inherited Access For SubFolder
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

   public Vector getInheritedAccesses(Context context,String[] args) throws Exception
  {

     Vector inheritAccess=new Vector();
     HashMap programMap=(HashMap)JPO.unpackArgs(args);

     MapList objectList=(MapList)programMap.get("objectList");
     HashMap paramMap=(HashMap)programMap.get("paramList");
     String stringResFileId = "emxTeamCentralStringResource";
     HashMap i18nMap     = new HashMap();
     i18nMap.put("Read","Read");
     i18nMap.put("Read Write","ReadWrite");
     i18nMap.put("Add","Add");
     i18nMap.put("Remove","Remove");
     i18nMap.put("Add Remove","AddRemove");
     i18nMap.put("None","None");
     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
     {
        HashMap mapAccess=(HashMap)objectListItr.next();
        inheritAccess.add(EnoviaResourceBundle.getProperty(context, stringResFileId, context.getLocale(), "emxTeamCentral.Access."+(String)i18nMap.get(mapAccess.get("InheritedAccess"))));
     }


     return inheritAccess;
  }


   /**
   * Method to get Specific Access for SubFolder
   * Returns a Vector of boolean values
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

      public Vector getSubFolderAvailableAccesses(Context context,String[] args) throws Exception
  {

     Vector availableAccesses=new Vector();
     HashMap programMap=(HashMap)JPO.unpackArgs(args);

     MapList objectList=(MapList)programMap.get("objectList");
     HashMap paramMap=(HashMap)programMap.get("paramList");
     String stringResFileId = "emxTeamCentralStringResource";
     HashMap i18nMap     = new HashMap();
     i18nMap.put("Read","Read");
     i18nMap.put("Read Write","ReadWrite");
     i18nMap.put("Add","Add");
     i18nMap.put("Remove","Remove");
     i18nMap.put("Add Remove","AddRemove");
     i18nMap.put("None","None");

     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
     {
       HashMap mapAccess=(HashMap)objectListItr.next();


       availableAccesses.add(EnoviaResourceBundle.getProperty(context, stringResFileId, context.getLocale(), "emxTeamCentral.Access."+(String)i18nMap.get(mapAccess.get("SpecificAccess"))));
     }


     return availableAccesses;
  }

   /**
   * Access Method to display Folder Access Tree Node
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return boolean value
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

            public static boolean displayFolderAccessTreeNode(Context context, String[] args) throws Exception
        {
             displayTreeNodeAccessCheck(context,args);
             HashMap programMap=(HashMap)JPO.unpackArgs(args);
             String treeMenu = (String)programMap.get("treeMenu");
             String objectId=(String)programMap.get("objectId");
             DomainObject BaseObject=new DomainObject(objectId);
             String parentOID=(String)programMap.get("parentOID");
             String strMode = (String) programMap.get("mode");

             if(! UIUtil.isNullOrEmpty(parentOID))
             {
                     DomainObject parentObject=new DomainObject(parentOID);
                     String sTypeName = parentObject.getInfo(context, parentObject.SELECT_TYPE);
                     if(sTypeName.equals(parentObject.TYPE_WORKSPACE))
                     {
                           return true;
                     }
                     else
                     {
                           return false;
                     }
            }// Modification IR-010047V6R2010x start
             else if(BaseObject.getInfo(context, BaseObject.SELECT_TYPE).equals(BaseObject.TYPE_WORKSPACE))
             {
                 return true;
             }
             else if(! UIUtil.isNullOrEmpty(treeMenu))
             {
                 String sParent  = (String)BaseObject.getInfo(context,"to[" +DomainObject.RELATIONSHIP_PROJECT_VAULTS+ "].from.type");
                 if(! UIUtil.isNullOrEmpty(sParent) && sParent.equals(DomainObject.TYPE_PROJECT))
                 {
                     return true;
                 }
                 else
                 {
                     return false;
                 }
             }
             else if(! UIUtil.isNullOrEmpty(strMode) && "insert".equals(strMode))
             {
                 String sParent  = (String)BaseObject.getInfo(context,"to[" +DomainObject.RELATIONSHIP_PROJECT_VAULTS+ "].from.type");
                 if(! UIUtil.isNullOrEmpty(sParent) && sParent.equals(DomainObject.TYPE_PROJECT))
                 {
                     return true;
                 }
                 else
                 {
                     return false;
                 }
             }
            else
            {
                return false;
            }// Modification IR-010047V6R2010x end
        }


   /**
   * Method to display sub folder Access Tree node
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return boolean
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

  public static boolean displaySubFolderAccessTreeNode(Context context, String[] args) throws Exception
  {
     /* Commented for IR-010047V6R2010x

       displayTreeNodeAccessCheck(context,args);
       HashMap programMap=(HashMap)JPO.unpackArgs(args);
       String objectId=(String)programMap.get("objectId");
       DomainObject BaseObject=new DomainObject(objectId);
       String parentOID=(String)programMap.get("parentOID");
       if(parentOID!=null && !parentOID.equals(""))
       {
               DomainObject parentObject=new DomainObject(parentOID);
               String sTypeName = parentObject.getType(context);
               if(sTypeName.equals(parentObject.TYPE_WORKSPACE_VAULT))
               {
                     return true;
               }
               else
               {
                     return false;
               }
      }
      else
      {
           String sParent        = (String)BaseObject.getInfo(context,"to[" +DomainObject.RELATIONSHIP_PROJECT_VAULTS+ "].from.type");
           if(sParent!= null && !sParent.equals("") && sParent.equals(DomainObject.TYPE_PROJECT))
           {
                return false;
          }
          else
          {
                return true;
          }
      }*/
      return !displayFolderAccessTreeNode(context,args);
 }


    /**
   * Method to populate the paased HashMap with Access Details
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

     private void getAccess (matrix.db.Context context,
                              matrix.db.AccessList baseAccessList,
                              matrix.db.AccessList parentAccessList,
                              String  sGrantee, String sTypeName, java.util.HashMap hashMap,
                              AccessUtil accessUtil) throws Exception
    {
    String sAccessString = "";
    try {
      matrix.db.Access access = getGranteeAccess(context,baseAccessList,sGrantee );//BaseObject.getAccessForGranteeGrantor(context, sGrantee, accessUtil.WORKSPACE_ACCESS_GRANTOR);
      sAccessString = accessUtil.checkAccess(access);
    } catch(Exception exp) {
      sAccessString = accessUtil.NONE;
    }
    if(!sTypeName.equals(DomainObject.TYPE_WORKSPACE)){
      String sInheritAccess = "";
      try {
        matrix.db.Access inheritAccess = getGranteeAccess(context,parentAccessList,sGrantee);//parentObj.getAccessForGranteeGrantor(context, sGrantee, accessUtil.WORKSPACE_ACCESS_GRANTOR);
        if(inheritAccess != null) {
          sInheritAccess = accessUtil.checkAccess(inheritAccess);
          sAccessString = accessUtil.compareAccess(sAccessString,sInheritAccess);
          if(sAccessString.equals(sInheritAccess)) {
           sAccessString = "&nbsp";
          }
        } else {
          sInheritAccess = accessUtil.NONE;
          sAccessString = "&nbsp";
        }
      } catch(Exception exp){
         sInheritAccess = accessUtil.NONE;
         sAccessString = "&nbsp";
      }
       hashMap.put("InheritedAccess", sInheritAccess);
       hashMap.put("SpecificAccess", sAccessString);
    } else {
      hashMap.put("Access", sAccessString);
    }
 }

    /**
   * Method to calculat a person access with a given person Access List
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

  private  matrix.db.Access getGranteeAccess (matrix.db.Context context,
                                                 matrix.db.AccessList accessList,
                                                 String grantee) throws Exception
  {
    Iterator iterator = accessList.iterator();
    while(iterator.hasNext()) {
     matrix.db.Access access = (Access)iterator.next();
     if(grantee.equals(access.getUser()))
     {
       return access;
     }
    }
    return new matrix.db.Access();
  }

      /**
   * Method to check whether a list contains the given buyerdesk id
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

  private  boolean isAssignedBuyer(StringList BuyerDeskListId,String WSBuyerDeskId)
  {
    String  sDeskId  = WSBuyerDeskId;
    boolean bBuyer   = false;

    if( BuyerDeskListId == null){
      return false;
    }
    if( BuyerDeskListId.contains(sDeskId)){
      bBuyer = true;
    }
    return bBuyer;
  }


 /**
   * Return the projectid (WorkspaceId) for the given folderId
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

   private String getProjectId(matrix.db.Context context,String folderId) throws MatrixException {
      String strProjectVault = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults");
      String strProjectType  = PropertyUtil.getSchemaProperty(context,"type_Project");
      String strSubVaultsRel = PropertyUtil.getSchemaProperty(context,"relationship_SubVaults");
      String strProjectVaultType = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");

      //com.matrixone.framework.beans.DomainObject domainObject = new com.matrixone.framework.beans.DomainObject();
      DomainObject domainObject = DomainObject.newInstance(context);
      domainObject.setId(folderId);

      String projectId = "";

      Pattern relPattern  = new Pattern(strProjectVault);
      relPattern.addPattern(strSubVaultsRel);
      relPattern.addPattern(DomainConstants.RELATIONSHIP_THREAD);
      relPattern.addPattern(DomainConstants.RELATIONSHIP_MESSAGE);
      Pattern typePattern = new Pattern(strProjectType);
      typePattern.addPattern(strProjectVaultType);
      typePattern.addPattern(DomainConstants.TYPE_THREAD);
      typePattern.addPattern(DomainConstants.TYPE_MESSAGE);

      Pattern includeTypePattern = new Pattern(strProjectType);

      StringList objSelects = new StringList();
      objSelects.addElement(domainObject.SELECT_ID);

      //need to include Type as a selectable if we need to filter by Type
      objSelects.addElement(domainObject.SELECT_TYPE);

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
   * checkWorkspaceName() is used to check for duplicate workspace name while editing the Name. This is an update function on the Name field of the web form
   * @throws Exception if the operation fails
   * @since V6R2008-1 for Bug 341114 & 341638
   */

    public void checkWorkspaceName(Context context,String[] args)throws Exception
    {
        String MAX_LENGTH =EnoviaResourceBundle.getProperty(context,"emxComponents.MAX_FIELD_LENGTH");
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        String stringResFileId = "emxTeamCentralStringResource";
        Locale locale =  context.getSession().getLocale();
        HashMap paramMap = (HashMap)programMap.get("paramMap");

        String objectId=(String)paramMap.get("objectId");
        String strName = (String)paramMap.get("New Value");

        if(strName.length()>(Integer.parseInt(MAX_LENGTH)))
        {
            String strLengthMessage = EnoviaResourceBundle.getProperty(context,stringResFileId, locale, "emxTeamCentral.NameLength.Message");
            String strChars = EnoviaResourceBundle.getProperty(context,stringResFileId, locale, "emxTeamCentral.NameLength.NumChars");
            MqlUtil.mqlCommand(context, "notice $1",strLengthMessage + MAX_LENGTH + " " + strChars );
            return;
        }

        MapList listOfWorkspaces = new MapList();
        //Find for workspace objects having the same name as the newly given name
        ContextUtil.pushContext(context, null, null, null);
        listOfWorkspaces = DomainObject.findObjects(context,
                                                   DomainConstants.TYPE_WORKSPACE,
                                                   strName,
                                                   "*",
                                                   "*",
                                                   "*",
                                                   null,
                                                   false,
                                                   null);
        ContextUtil.popContext(context);
        if(listOfWorkspaces.size()>0)
        {
           //Found workspaces with same name

            String strMessage = EnoviaResourceBundle.getProperty(context, stringResFileId, locale, "emxTeamCentral.DuplicateWorkspaceName.Message1");
            throw new FrameworkException(strMessage);

        }
        else
        {
            //no workspaces with same name
            DomainObject domWorkspace = new DomainObject(objectId);

            domWorkspace.setName(context, strName);


        }
    }

   /**
     * Get an BUyer Desk Connected to the Workspace, Show the list of Buyerdesks
     *
     * @param context The current context object.
     * @param args holds the Workspace object Id, request map, param map
     * @return String Html in Edit, Buyer Desk Name in view mode.
     * @since V6R2008-1 for Bug 340154
     */

 public String getBuyerDesk (Context context,String[] args)throws Exception
    {
        StringBuffer output = new StringBuffer();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String strMode = (String) requestMap.get("mode");
            String workspaceId = (String) requestMap.get("objectId");
            DomainObject domWorkspace=new DomainObject();
            String strOuput ="";
            StringBuffer oldBuyerDeskName=new StringBuffer();
            StringBuffer oldBuyerDeskId=new StringBuffer();
            if(workspaceId!=null)
            {
                domWorkspace = new DomainObject(workspaceId);
                StringList selectStmts  = new StringList(2);
                selectStmts.addElement(SELECT_ID);
                selectStmts.addElement(SELECT_NAME);

                MapList mapList = domWorkspace.getRelatedObjects( context,
                        DomainConstants.RELATIONSHIP_WORKSPACE_BUYER_DESK,  // relationship
                        // pattern
                        DomainConstants.TYPE_BUYER_DESK,                    // object
                        // pattern
                        selectStmts,                 // object
                        // selects
                        null,              // relationship
                        // selects
                        false,                       // to
                        // direction
                        true,                        // from
                        // direction
                        (short) 1,                   // recursion
                        // level
                        null,                        // object
                        // where
                        // clause
                        null);                       // relationship
                // where
                // clause

               if(mapList != null && mapList.size() > 0)
                {
                    // construct array of ids
                    int mapListSize = mapList.size();
                     for(int i = 0; i < mapListSize; i++)
                    {
                        Map dataMap = (Map)mapList.get(i);
                        String name = (String)dataMap.get(SELECT_NAME);
                        String objectId =(String)dataMap.get(SELECT_ID);
                             oldBuyerDeskName.append(name+",");
                             oldBuyerDeskId.append(objectId+",");
                    }
                }
            }

            String strBuyerDeskName = "";
            String strBuyerDeskId = "";

            if(oldBuyerDeskName.length()>0)
            {
                strBuyerDeskName = oldBuyerDeskName.toString();
                strBuyerDeskName =strBuyerDeskName.substring(0,strBuyerDeskName.length()-1);
                strBuyerDeskId = oldBuyerDeskId.toString();
                strBuyerDeskId =strBuyerDeskId.substring(0,strBuyerDeskId.length()-1);
            }
            //Modify:10-Aug-09:nr2:R208:COM:Bug:377497
            //Added Since was throwing Exception when called from export command in Toolbar
            //strMode was null which was not checked.
            if(strMode == null || "".equals(strMode))
                strMode = "view";
            //End:R208:COM:Bug:377497
			
            if(strMode.equals("edit"))
            {
            DomainObject personObj = PersonUtil.getPersonObject(context);
			String  selectUserCompanyName = "to[" + PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_Employee) + "].from.name";
            String companyName = personObj.getInfo(context, selectUserCompanyName);
			
            output.append("<input type='text' name='txtBuyerDesk' value='"+XSSUtil.encodeForHTMLAttribute(context,strBuyerDeskName)+"'/>");
            output.append("<input type='hidden' name='txtBuyerDeskId' value='"+XSSUtil.encodeForHTMLAttribute(context,strBuyerDeskId)+"'/>");
            output.append("<input type='button' name='' value='...' onClick=\"");
            output.append("javascript:showChooser('../common/emxFullSearch.jsp?field=TYPE=type_BuyerDesk:ASSIGNED_ORGANIZATION="+companyName+"&amp;table=AEFGeneralSearchResults&amp;&selection=single&amp;submitURL=../components/emxCommonSelectObject.jsp&amp;formName=editDataForm&amp;fieldNameDisplay=txtBuyerDesk&amp;fieldNameActual=txtBuyerDeskId&amp;frameName=content',575,575)\" />");

            strOuput =output.toString();
            }
            else {
            output.append(strBuyerDeskName);
            output.append("<input type='hidden' name='txtBuyerDeskId' value='"+XSSUtil.encodeForHTMLAttribute(context,strBuyerDeskId)+"'/>");
            strOuput =output.toString();
            }
            return strOuput ;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
	@com.matrixone.apps.framework.ui.ProgramCallable
    public Object getActiveWorkspaces(Context context, String[] args) throws Exception
    {
        return getWorkspaces(context, args, "Active");
    }

    public Object getWorkspaces(Context context, String[] args,String sFtr) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String menuAction = (String)programMap.get("menuAction");
        Person person = Person.getPerson(context);

        String queryTypeWhere   = "";

        // where for the query, show workspaces in the "Active" state only, and the users(roles) must have read access on the workspace.
        if (sFtr.equals("Active")) {
            queryTypeWhere = "('" + DomainObject.SELECT_CURRENT + "' == 'Active')";
        } else {
            queryTypeWhere = "('" + DomainObject.SELECT_OWNER + "' == '"+ sFtr +"' && '" + DomainObject.SELECT_CURRENT + "' == 'Active')";
        }
        String selection = "single";
        String accessCheck = "current.access[toconnect]";
        if( "addLinkFolder".equals(menuAction) )
        {
            selection = "multiple";
            accessCheck = "current.access[read,show]";
        }

        //query selects
        StringList objectSelects = new StringList();
        objectSelects.add(DomainObject.SELECT_ID);
        objectSelects.addElement(accessCheck);

        // get all workspaces that the current user is a member since one of his roles is a member
        MapList workspaceList = DomainObject.querySelect(context,
                                                              DomainConstants.TYPE_PROJECT,                // type pattern
                                                              DomainObject.QUERY_WILDCARD, // namePattern
                                                              DomainObject.QUERY_WILDCARD, // revPattern
                                                              DomainObject.QUERY_WILDCARD, // ownerPattern
                                                              DomainObject.QUERY_WILDCARD, // get the Person Company vault
                                                              queryTypeWhere,              // where expression
                                                              true,                        // expandType
                                                              objectSelects,               // object selects
                                                              null,                        // cached list
                                                              false);                       // use cache

        Iterator fieldsItr = workspaceList.iterator();
        Map curField = new HashMap();
        MapList objectList = new MapList();

        while(fieldsItr.hasNext()){
            curField = (HashMap) fieldsItr.next();
            
            // Disabled as workspace objects cannot be added as a link and the folder contents can not be added into Workspace
            if("addLinkFolder".equals(menuAction) || "copyContent".equals(menuAction)){
                curField.put("disableSelection", "true");
            }
                        
            String access = (String)curField.get(accessCheck);
            if( "true".equalsIgnoreCase(access) ) {
                curField.put("selection", selection);
            } else {
                curField.put("selection", selection);
            	curField.put("disableSelection", "true");
            }
            objectList.add(curField);
        }

        return objectList;
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getWorkspaceVaults (Context context, String[] args) throws Exception
    {

        // unpack the arguments
        HashMap arguMap = (HashMap)JPO.unpackArgs(args);
        String menuAction = (String)arguMap.get("menuAction");
        String selection = "single";
        String accessCheck = "current.access[read,modify,checkout,checkin,lock,unlock,revise,fromconnect,toconnect,show]";
        if( "addLinkFolder".equals(menuAction) )
        {
            selection = "multiple";
            accessCheck = "current.access[toconnect]";
        }
        // return the object list
        return getWorkspaceFolders(context, args, accessCheck, selection);
    }


     /**
    * Access Method to show Folder Edit access for other Workspace Leads in the workspace
    * @param context the eMatrix Context object
    * @return boolean
    * @throws Exception if the operation fails
    * @since V6R2009x
    */

    public boolean showTMCWorkspaceFolderEditAccess(Context context,String[] args) throws Exception
    {
        HashMap programMap     = (HashMap)JPO.unpackArgs(args);
        String sProjectLead    = "";
        boolean bIsProjectLead = false;
        String sFolderID       = (String)programMap.get("objectId");
        String sWorkspaceID    = getProjectId(context, sFolderID);
        DomainObject workspace = new DomainObject(sWorkspaceID);
        String sOwner          = workspace.getOwner(context).toString();
        DomainObject doFolder = new DomainObject(sFolderID);
        String sFolderOwner    = doFolder.getOwner(context).toString();
        String sUser           = context.getUser();
        String sAttrProjectAccess  = DomainObject.ATTRIBUTE_PROJECT_ACCESS;
        DomainObject projectMember = null;
        try
        {
            projectMember = DomainObject.newInstance(context,(getProjectMember(context,sWorkspaceID,getPerson(context))));
        }
        catch (Exception ex)
        {
            projectMember = null;
        }

        if(projectMember != null )
        {
           DomainObject projMemberObj = new DomainObject(projectMember);
           sProjectLead               = projMemberObj.getAttributeValue(context,sAttrProjectAccess);
        }
        if(sOwner.equals(sUser) || sProjectLead.equals("Project Lead")|| sFolderOwner.equals(sUser)) {bIsProjectLead=true;}
        return bIsProjectLead;
    }

    //Added for Mx357605V6R2011
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getWorkspaceFoldersForSelection (Context context, String[] args) throws Exception
    {
        HashMap arguMap = (HashMap)JPO.unpackArgs(args);
        String type = (String)arguMap.get("type");
        String selection =  DomainConstants.TYPE_ROUTE.equals(type) ? "single" : "none";
        
        // This access check is to verify if any member having Add/Add Remove/Full access on the object
        String accessCheck = "current.access[toconnect, fromconnect]";
        return getWorkspaceFolders(context, args, accessCheck, selection);

    }

    protected MapList getWorkspaceFolders (Context context, String[] args, String accessCheck, String selection) throws Exception
    {
        HashMap arguMap = (HashMap)JPO.unpackArgs(args);
        String wsId = (String) arguMap.get("objectId");
        String sExpandLevel = (String)arguMap.get("expandLevel");
        int expLevel = 1;
        if(UIUtil.isNullOrEmpty(sExpandLevel)) {
        	expLevel = expLevel;
        } else if("All".equalsIgnoreCase(sExpandLevel)) {
        	expLevel = 0;
        } else {
        	expLevel = Integer.parseInt(sExpandLevel);
        }
        DomainObject wsObj = new DomainObject(wsId);

        MapList resultsList = new MapList();
        StringList objectSelects = new StringList();
        objectSelects.addElement(DomainConstants.SELECT_ID);
        objectSelects.addElement(accessCheck);


        resultsList = (MapList)wsObj.getRelatedObjects(context,
                                                DomainObject.RELATIONSHIP_WORKSPACE_VAULTS +","+ DomainObject.RELATIONSHIP_SUBVAULTS,
                                                DomainObject.TYPE_PROJECT_VAULT,
                                                objectSelects,
                                                null,
                                                false,
                                                true,
                                                (short)expLevel,
                                                "",
                                                null);

        Iterator fieldsItr = resultsList.iterator();
        Hashtable curField = new Hashtable();
        MapList objectList = new MapList();

        while(fieldsItr.hasNext()){
            curField = (Hashtable) fieldsItr.next();
            String access = (String)curField.get(accessCheck);
            if( "true".equalsIgnoreCase(access) )
            {
            curField.put("selection", selection);
            }
            objectList.add(curField);
        }

        // return the object list
        return objectList;
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getDisabledWorkspaces(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String type = (String)programMap.get("type");
        Person person = Person.getPerson(context);
        String contextUser = (String) person.getName();

        String queryTypeWhere   = "('" + DomainObject.SELECT_CURRENT + "' == 'Active')";

        String selection =  DomainConstants.TYPE_ROUTE.equals(type) || DomainConstants.TYPE_ROUTE_TEMPLATE.equals(type) ? "single" : "none";
        String accessCheck = "current.access[read,modify,checkout,checkin,lock,unlock,revise,fromconnect,toconnect,show]";

        //query selects
        StringList objectSelects = new StringList();
        objectSelects.add(DomainObject.SELECT_ID);
        objectSelects.add(DomainObject.SELECT_OWNER);
        objectSelects.addElement(accessCheck);
        objectSelects.add("type.kindof["+DomainConstants.TYPE_PROJECT_SPACE+"]");

        Pattern typePattern = new Pattern(DomainConstants.TYPE_PROJECT);
        typePattern.addPattern(DomainConstants.TYPE_PROJECT_SPACE);

        // get all workspaces that the current user is a member since one of his roles is a member
        MapList workspaceList = DomainObject.querySelect(context,
                                                              typePattern.getPattern(),    // type pattern
                                                              DomainObject.QUERY_WILDCARD, // namePattern
                                                              DomainObject.QUERY_WILDCARD, // revPattern
                                                              DomainObject.QUERY_WILDCARD, // ownerPattern
                                                              DomainObject.QUERY_WILDCARD, // get the Person Company vault
                                                              queryTypeWhere,              // where expression
                                                              true,                        // expandType
                                                              objectSelects,               // object selects
                                                              null,                        // cached list
                                                              false);                       // use cache

        Iterator fieldsItr = workspaceList.iterator();
        Map curField = new HashMap();
        MapList objectList = new MapList();

        while(fieldsItr.hasNext()){
            curField = (HashMap) fieldsItr.next();
            String owner = (String) curField.get(DomainObject.SELECT_OWNER);
            String access = (String)curField.get(accessCheck);
            String isProjectSpace = (String) curField.get("type.kindof["+DomainConstants.TYPE_PROJECT_SPACE+"]");
            if( "true".equalsIgnoreCase(access) || ("true".equalsIgnoreCase(isProjectSpace) && contextUser.equalsIgnoreCase(owner) ) )
            {
                curField.put("selection", selection);
            }
           // if(owner.equalsIgnoreCase(contextUser)){
            objectList.add(curField);
           // }
        }

        return objectList;
    }



    /**
     * getRevisionField - Displays the last revision only text and box for general search
     * @param context the eMatrix <code>Context</code> object
     * @return String
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */

     public static String getRevisionField(Context context, String args[])throws Exception
     {
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         String language = (String) programMap.get("languageStr");
         Locale locale = context.getLocale();
         if(language != null){
        	 locale = new Locale(language);
         }
         String strLatestRevision = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource", locale, "emxTeamCentral.Search.LatestRevOnly");
         StringBuffer strBuff = new StringBuffer();
         strBuff.append("<input type=\"text\" name=\"txtRev\" size=\"20\" readonly=\"readonly\">");
         strBuff.append("<input type=\"checkbox\" name=\"chkLastRevision\" value=\"true\" onclick=\"javascript:Update(this)\" checked/>");
         strBuff.append(strLatestRevision);
         return strBuff.toString();
     }

     /**
      * getWorkspaceSubfoldersCheckbox - Displays the checkbox to include workspace subfolders in search
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
         sb.append( EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.FindFiles.IncludeSubFolders"));
         return sb.toString();
     }




     /**
      * findContentFolderSearch - returns the file content for folder search
      * @param context the eMatrix <code>Context</code> object
      * @return MapList
      * @throws Exception if the operation fails
      * @since R210
      * @grade 0
      */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList findContentFolderSearch(Context context, String[] args) throws Exception
     {
         HashMap programMap=(HashMap)JPO.unpackArgs(args);
         String sProjectId           = (String)programMap.get("projectId");
         String sObjectId            = (String)programMap.get("objectId");
         String typeProjectVault     = "type_ProjectVault";
         String relWorkspaceVaults   = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");
         Pattern relPattern          = null;
         Pattern typePattern         = null;
         String typeVault            = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
         String selWorkspace         = "";
         String sRelWorkspaceVaults  = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");
         String sProjectVaultType    = PropertyUtil.getSchemaProperty(context, typeProjectVault );
         BusinessObject boProject    = null;
         selWorkspace = "to["+sRelWorkspaceVaults+"].from.name";

         MapList FolderMapList = new  MapList();
         if(UIUtil.isNullOrEmpty(sProjectId)) {
             sProjectId=getProjectId(context,sObjectId);
             }

         if(!"".equals(sProjectId)) {
         DomainObject workspaceObj = DomainObject.newInstance(context,sProjectId);
         typePattern   = new Pattern ( sProjectVaultType );
         relPattern    = new Pattern ( relWorkspaceVaults );

         boProject     = new BusinessObject(sProjectId );
         boProject.open(context);

         relPattern    = new Pattern(sRelWorkspaceVaults);
         typePattern   = new Pattern(typeVault);

         // type and rel patterns to include in the final resultset
         Pattern includeTypePattern = new Pattern(typeVault);
         Pattern includeRelPattern = new Pattern(sRelWorkspaceVaults);



         StringList selectTypeStmts = new StringList();
         selectTypeStmts.add(workspaceObj.SELECT_TYPE);
         selectTypeStmts.add(workspaceObj.SELECT_NAME);
         selectTypeStmts.add(workspaceObj.SELECT_ID);
         selectTypeStmts.add(workspaceObj.SELECT_DESCRIPTION);
         selectTypeStmts.add(selWorkspace);

         workspaceObj.setId(sProjectId);


         FolderMapList = workspaceObj.getRelatedObjects(context,
                                                        relPattern.getPattern(),  //String relPattern
                                                        typePattern.getPattern(), //String typePattern
                                                        selectTypeStmts,          //StringList objectSelects,
                                                        null,                     //StringList relationshipSelects,
                                                        false,                     //boolean getTo,
                                                        true,                     //boolean getFrom,
                                                        (short)1,                 //short recurseToLevel,
                                                        "",                       //String objectWhere,
                                                        "",                       //String relationshipWhere,
                                                        includeTypePattern,       //Pattern includeType,
                                                        includeRelPattern,        //Pattern includeRelationship,
                                                        null);


         return FolderMapList;
         } else {
             return new MapList();
         }
     }


     /**
      * Method to find persons for owner change
      *
      * @param context the eMatrix Context object
      * @param String array contains person Ids to display in the table
      * @return MapList
      * @throws Exception if the operation fails
      * @since V6R2011x
      * @grade 0
      */
	@com.matrixone.apps.framework.ui.ProgramCallable
     public static MapList getPersons(Context context, String[] args)throws Exception {
         MapList personList = new MapList();
         StringList typeSelects = new StringList();
         typeSelects.add(DomainConstants.SELECT_NAME);
         typeSelects.add(DomainConstants.SELECT_TYPE);
         typeSelects.add(DomainConstants.SELECT_ID);
         typeSelects.add( "attribute["+ATTRIBUTE_FIRST_NAME+"]");
         typeSelects.add( "attribute["+ATTRIBUTE_LAST_NAME+"]");

         Person busPerson=Person.getPerson(context);
         busPerson.open(context);
         String strCompanyId = busPerson.getCompanyId(context);
         personList = DomainObject.findObjects(context,DomainConstants.TYPE_PERSON,"*",null,typeSelects);
         return personList;

     }
     /**
      * showUserLastNameFirstName - returns the user name in lastname,firstname format
      * @param context the eMatrix <code>Context</code> object
      * @return Vector
      * @throws Exception if the operation fails
      * @since R210
      * @grade 0
      */
     public Vector showUserLastNameFirstName(Context context,String[] args) throws Exception
     {
        Vector personNames=new Vector();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        MapList objectList=(MapList)programMap.get("objectList");
        HashMap paramMap=(HashMap)programMap.get("paramList");
        String reportFormat = (String)paramMap.get("reportFormat");
        String firstNameStr     = PropertyUtil.getSchemaProperty( context, "attribute_FirstName");
        String lastNameStr      = PropertyUtil.getSchemaProperty( context, "attribute_LastName");
        String workPhoneNumber  = PropertyUtil.getSchemaProperty( context, "attribute_WorkPhoneNumber");
        String strfirstNameStr  = "attribute["+firstNameStr+"]";
        String strlastNameStr   = "attribute["+lastNameStr+"]";
        String strworkPhoneNumber =   "attribute["+workPhoneNumber+"]";
        for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
        {
            Map mapAccess=(Map)objectListItr.next();
            String treeUrl    = "";
            String sDisplayPersonName = "";
            String firstName  = (String)mapAccess.get(strfirstNameStr);
            String lastName  = (String)mapAccess.get(strlastNameStr);
            String LastFirstName  = (String)mapAccess.get("LastFirstName");
            if(LastFirstName != null && !LastFirstName.equals(""))
            {
             sDisplayPersonName = XSSUtil.encodeForHTML(context, LastFirstName);
            }else{
                sDisplayPersonName = XSSUtil.encodeForHTML(context, lastName)+", "+XSSUtil.encodeForHTML(context, firstName);
            }
           // Added for Bug 353788
           if(reportFormat != null && ("CSV".equalsIgnoreCase(reportFormat) || "Text".equalsIgnoreCase(reportFormat)))
           {
               treeUrl = sDisplayPersonName;
           }
           //Ended
           else
           {
               treeUrl = "<img src='images/iconSmallPerson.gif' border='0' id=''/>"+sDisplayPersonName;

           }
           personNames.add(treeUrl);
        }
        return personNames;
    }
     /**
      * getFolderFindfilesResult - method to return the files contained in the folder
      * @param context the eMatrix <code>Context</code> object
      * @return MapList
      * @throws Exception if the operation fails
      * @since R210
      * @grade 0
      */
	 @com.matrixone.apps.framework.ui.ProgramCallable 
     public MapList getFolderFindfilesResult(Context context, String[] args) throws Exception
     {
         HashMap programMap=(HashMap)JPO.unpackArgs(args);
         String language = (String) programMap.get("languageStr");
         language = language == null ? context.getLocale().getLanguage() : language;
         // Getting the parameters from request
         String objectId = (String)programMap.get("objectId");
         String topParentHolderId = (String)programMap.get("projectId");
         if(topParentHolderId==null)
         {
             topParentHolderId = getProjectId(context,objectId);
         }
         String Owner             = (String)programMap.get("Owner");// Bug No:296434


         String Type              = (String)programMap.get("Type");
         String WorkspaceVaultId  = (String)programMap.get("WorkspaceFolder");
         String IncludeSubfolders = (String)programMap.get("workspacesubfolders");
         String WorkspaceOID      = (String)programMap.get("WorkspaceOID");
         String CreateAfterDate   = (String)programMap.get("CreatedAfter");
         String CreateBeforeDate  = (String)programMap.get("CreatedBefore");
         String GeneralSearch     = (String)programMap.get("GeneralSearch");
         String DocumentType      = (String)programMap.get("TypeDisplay");
         String Title             = (String)programMap.get("Title");
         String Name              = (String)programMap.get("Name");
         com.matrixone.apps.common.Workspace project =(com.matrixone.apps.common.Workspace) DomainObject.newInstance(context,
                 DomainConstants.TYPE_PROJECT);
             com.matrixone.apps.common.Document document =
                 (com.matrixone.apps.common.Document) DomainObject.newInstance(context,
                 DomainConstants.TYPE_DOCUMENT);
             com.matrixone.apps.common.WorkspaceVault workspaceVault =
                 (com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
                 DomainConstants.TYPE_WORKSPACE_VAULT);
             String vaultType = "";

             if(!UIUtil.isNullOrEmpty((String)programMap.get("vaultsDisplay"))){
                vaultType=(String) programMap.get("vaultsDisplay");
             }else{
                vaultType = (String) programMap.get("vaultOption");
             }
         StringList vaultSL  = new StringList();
             if ("ALL_VAULTS".equals(vaultType)){
           vaultSL = GetAllVaults(context);
             } else if ("LOCAL_VAULTS".equals(vaultType)){
           vaultSL = GetAllLocalVaults(context);
             } else if ("DEFAULT_VAULT".equals(vaultType)){
           vaultSL.add(context.getVault().getName());
         } else {
           vaultSL = convertToStringList(vaultType,",");
         }



         // fix the name string so that there is an * at the front and rear
         if(Name.indexOf('*')!= 0){
           Name = "*"+Name;
         }
         if(Name.lastIndexOf('*')!=Name.length()-1){
           Name+= "*";
         }

         workspaceVault.setContentRelationshipType(workspaceVault.RELATIONSHIP_VAULTED_OBJECTS);
         String timeZone = (String)programMap.get("timeZone");
         double iClientTimeOffset = (new Double(timeZone)).doubleValue();
         //Dates must be formated so they are in same formate as above
         //if a data is entered, then convert it to new formate
         boolean createAfterDateEntered = false;
         boolean createBeforeDateEntered = false;
         String noDatekey = "emxTeamCentral.Common.NoDate";
         String noDate = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), noDatekey);
         //String noDate = i18nnow.GetString("emxTeamCentralStringResource",language, "emxTeamCentral.Common.NoDate");
         if(noDate.equals(noDatekey)){
             noDate = "";
         }

         if (!CreateAfterDate.equals(noDate)) {
           CreateAfterDate = eMatrixDateFormat.getFormattedInputDate(context,CreateAfterDate, iClientTimeOffset, context.getLocale());
           createAfterDateEntered = true;
         }
         if (!CreateBeforeDate.equals(noDate)) {
           CreateBeforeDate = eMatrixDateFormat.getFormattedInputDate(context,CreateBeforeDate, iClientTimeOffset, context.getLocale());
           createBeforeDateEntered = true;

         }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         //Step (1): Build the busSelect and busWhere clauses
         // Build the busSelects clause.
         StringList busSelects = new StringList (8);
         busSelects.add (document.SELECT_ID);
         busSelects.add (document.SELECT_TYPE);
         busSelects.add (document.SELECT_OWNER);
         busSelects.add (document.SELECT_FILE_NAME);
         busSelects.add (document.SELECT_REVISION);
         busSelects.add (document.SELECT_VERSION_DATE);
         busSelects.add (document.SELECT_VAULT);
         busSelects.add (document.SELECT_DESCRIPTION);
         busSelects.add (document.SELECT_TITLE);
         busSelects.add (document.SELECT_NAME);
         busSelects.add (document.SELECT_ORIGINATED);

         // The following section is building the busWhere clause.
         String busWhere = null;
         if ((Name != null) && !Name.equals(null) && !Name.equals("null") &&
           !Name.equals("*") && !Name.equals("")) {
           if (busWhere != null){
             busWhere += " && \"" + document.SELECT_NAME + "\" ~~ const\"" + Name + "\"";
           } else {
             busWhere = "\"" + document.SELECT_NAME + "\" ~~ const\"" + Name + "\"";
           }
         } //end if name is not null

         if ((Title != null) && !Title.equals(null) && !Title.equals("null") &&
           !Title.equals("*") && !Title.equals("")) {
           if (busWhere != null){
             busWhere += " && \"" + document.SELECT_TITLE + "\" ~~ const\"" + Title + "\"";
           } else {
             busWhere = "\"" + document.SELECT_TITLE + "\" ~~ const\"" + Title + "\"";
           }
         }


//     following code has been commented for bug no :296434  tested the impacts for 304444 also.

     /*
//     Bug 304444 - Added code to get the correct name of owner
        StringTokenizer st = new StringTokenizer(Owner, ",");
        String LastName = st.nextToken().trim();

        if(st.hasMoreTokens()){
         String FirstName =  st.nextToken().trim();
         Owner = FirstName +" "+ LastName;
        } */
//     till here



         if ((Owner != null) && !Owner.equals(null) && !Owner.equals("null") &&
           !Owner.equals("*") && !Owner.equals("")) {
           if (busWhere != null){
             busWhere += " && \"" + document.SELECT_OWNER + "\" ~~ \"" + Owner + "\"";
           } else {
             busWhere = "\"" + document.SELECT_OWNER + "\" ~~ \"" + Owner + "\"";
           }
         } //end if Owner is not null



         if (createAfterDateEntered == true) {

           //if there is no version date attribute use the originated date
           if(document.SELECT_VERSION_DATE == null){
             if (busWhere != null){
               busWhere += " && \"" + document.SELECT_ORIGINATED + "\" gt \"" + CreateAfterDate + "\"";
             } else {
               busWhere = "\"" + document.SELECT_ORIGINATED + "\" gt \"" + CreateAfterDate + "\"";
             }//ends else
           //use originated if folder search and a document only
           } else if(!"true".equals(GeneralSearch) && !DomainConstants.TYPE_DOCUMENT.equals(DocumentType)) {
             if (busWhere != null){
               busWhere += " && \"" + document.SELECT_ORIGINATED + "\" gt \"" + CreateAfterDate + "\"";
             } else {
               busWhere = "\"" + document.SELECT_ORIGINATED + "\" gt \"" + CreateAfterDate + "\"";
             }//ends else
           } else {
             if (busWhere != null){
               busWhere += " && \"" + document.SELECT_ORIGINATED + "\" gt \"" + CreateAfterDate + "\"";
             } else {
               busWhere = "\"" + document.SELECT_ORIGINATED + "\" gt \"" + CreateAfterDate + "\"";
             }//ends else
           }//ends else
         }//ends if

         if (createBeforeDateEntered == true) {
           //if there is no version date attribute use the originated date
           if(document.SELECT_VERSION_DATE == null){
             if (busWhere != null){
               busWhere += " && \"" + document.SELECT_ORIGINATED + "\" lt \"" + CreateBeforeDate + "\"";
             } else {
               busWhere = "\"" + document.SELECT_ORIGINATED + "\" lt \"" + CreateBeforeDate + "\"";
             }//ends else
           //use originated if folder search and a document only
           } else if(!"true".equals(GeneralSearch) && !DomainConstants.TYPE_DOCUMENT.equals(DocumentType)) {
             if (busWhere != null){
               busWhere += " && \"" + document.SELECT_ORIGINATED + "\" lt \"" + CreateBeforeDate + "\"";
             } else {
               busWhere = "\"" + document.SELECT_ORIGINATED + "\" lt \"" + CreateBeforeDate + "\"";
             }//ends else
           } else {
             if (busWhere != null){
               busWhere += " && \"" + document.SELECT_ORIGINATED + "\" lt \"" + CreateBeforeDate + "\"";
             } else {
               busWhere = "\"" + document.SELECT_ORIGINATED + "\" lt \"" + CreateBeforeDate + "\"";
             }//ends else
           }//ends else
         }//ends if
         //add clause to fetch only master documents
         String sAnd         = "&&";

        if (DomainConstants.TYPE_DOCUMENT.equals(DocumentType)) {

             if (busWhere != null){
                     busWhere += " && \""+CommonDocument.SELECT_IS_VERSION_OBJECT + "\" ~~ \"false\"";
             }else{
                     busWhere = "\""+CommonDocument.SELECT_IS_VERSION_OBJECT + "\" ~~ \"false\"";
             }
        }

//      This code need to be taken out once Sourcing X+3 Migration is Completed     -SC
//      Start of Pre Migration Code -SC
         else if(DocumentType.equals(DomainObject.TYPE_REQUEST_TO_SUPPLIER)){
            if ((busWhere == null) || (busWhere.equalsIgnoreCase("null")) || (busWhere.length()<=0 ))
            {
                 busWhere = " (!to[" + DomainObject.RELATIONSHIP_COMPANY_RFQ + "]) ";
            }
            else
            {
                 busWhere += sAnd + " " + " (!to[" + DomainObject.RELATIONSHIP_COMPANY_RFQ + "]) ";
            }
        } else if(DocumentType.equals(DomainObject.TYPE_PACKAGE)){
            if ((busWhere == null) || (busWhere.equalsIgnoreCase("null")) || (busWhere.length()<=0 ))
            {
                busWhere = " (!to[" + DomainObject.RELATIONSHIP_COMPANY_PACKAGE + "]) ";
            }
            else
            {
                busWhere += sAnd + " " + " (!to[" + DomainObject.RELATIONSHIP_COMPANY_PACKAGE + "]) ";
            }
        } else if(DocumentType.equals(DomainObject.TYPE_RTS_QUOTATION)){
            if ((busWhere == null) || (busWhere.equalsIgnoreCase("null")) || (busWhere.length()<=0 ))
            {
                busWhere = "  (to[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].from.to["+ DomainObject.RELATIONSHIP_COMPANY_RFQ +"]!='#DENIED!') && (!(to[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].from.to["+ DomainObject.RELATIONSHIP_COMPANY_RFQ +"]))";
            }
            else
            {
                busWhere += sAnd + " " + " (to[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].from.to["+ DomainObject.RELATIONSHIP_COMPANY_RFQ +"]!='#DENIED!') && (!(to[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].from.to["+ DomainObject.RELATIONSHIP_COMPANY_RFQ +"]))";
            }
        }
//      End of Pre Migration Code - SC

         //StringList to hold list of individual where claused to be separated by "||"
         StringList strQueryList = new StringList();

         if ((Type != null) && !Type.equals(null) && !Type.equals("null") &&
           !Type.equals("*") && !Type.equals("")) {

             String typeP = document.SELECT_TYPE + "\" ~~ \"" + Type + "\"";
             String busWhereTemp = busWhere;
             if (busWhereTemp != null){
               busWhereTemp += " && \"" + typeP;
             } else {
               busWhereTemp = "\"" + typeP;
             }
             strQueryList.addElement(busWhereTemp);
             busWhereTemp = busWhere;


             //Iterate and add for all the subtypes
             BusinessTypeList busTypeList = getAllSubTypes(context,Type,new BusinessTypeList());
             Iterator itr = busTypeList.iterator();
             while ( itr.hasNext() ) {
               BusinessType busChildType = (BusinessType) itr.next();
               // add to return list
               typeP = document.SELECT_TYPE + "\" ~~ \"" + busChildType.getName() + "\"";
               if (busWhereTemp != null){
                 busWhereTemp += " && \"" + typeP;
               } else {
                 busWhereTemp = "\"" + typeP;
               }//ends else
               strQueryList.addElement(busWhereTemp);
               busWhereTemp = busWhere;
             }//ends while

         } //end if type is not null or *

         MapList queryResultList = new MapList();

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         //Step (2): Perform the search if the page is called form Folder Search
         //StringList to hold the finally formed query list
         StringList finalQueryList = new StringList();
           MapList vaultList = new MapList();
           MapList documentList = new MapList();
           HashMap specifiedWorkspaceVaultMap = new HashMap();
           if (vaultSL.size() > 0){
             Iterator vaultItr = vaultSL.iterator();
             while (vaultItr.hasNext()) {
               String vaultName = (String) vaultItr.next();

               vaultName = vaultName.trim();

               StringItr queryListIter = new StringItr(strQueryList);
               while(queryListIter.next()){
                 String indQuery = queryListIter.obj();
                 if (indQuery == null) {
                   indQuery = "\\\"vault\\\" == \\\"" + vaultName + "\\\"";
                 } else {
                   indQuery += " && " +  "\\\"vault\\\" == \\\"" + vaultName + "\\\"";
                 }//ends else

                 finalQueryList.addElement(indQuery);

               }//ends while

             }//ends while
           }//ends if

           busWhere = "";
           for(int i=0;i<finalQueryList.size();i++){
             if(i==finalQueryList.size()-1){
               busWhere +="("+(String)finalQueryList.elementAt(i)+")";
             } else {
               busWhere +="("+(String)finalQueryList.elementAt(i)+")"+"||";
             }//ends else
           }//ends for

           //With the topParentHolderId, get a MapList of all
           //workspace vaults related to the selected project
           if (WorkspaceOID != null && !WorkspaceOID.equals("")){
             project.setId (WorkspaceOID);

             //If the user wants to search in a specific folders then
             boolean searchInSpecificWorkspaceVault = false;
             if (WorkspaceVaultId != null && ! "".equals(WorkspaceVaultId)) {
               workspaceVault.setId (WorkspaceVaultId);
               searchInSpecificWorkspaceVault = true;

               //Create Map with information from selectedVault
               specifiedWorkspaceVaultMap.put("name", workspaceVault.getInfo(context, workspaceVault.SELECT_NAME));
               specifiedWorkspaceVaultMap.put("id", workspaceVault.getInfo(context, workspaceVault.SELECT_ID));
               specifiedWorkspaceVaultMap.put("type", workspaceVault.getInfo(context, workspaceVault.SELECT_TYPE));
             }

             //If the user wants to include subfolders during search
             if ("true".equalsIgnoreCase(IncludeSubfolders)) {
               //Only search in specific workspaceVault
               if (searchInSpecificWorkspaceVault == true) {
                 //Add subVaults of selected vault to vaultList
                 vaultList = workspaceVault.getSubVaults(context, busSelects, 0);
                 //Add selected vault to vaultList
                 vaultList.add(specifiedWorkspaceVaultMap);
               } else {
                 //Search in all workspaceVaults for the project
                 vaultList = getWorkspaceVaults(context, project, busSelects, 0);
               }
             } else {
               //Do not include subfolders during search
               //Only search in specific workspaceVault
               if (searchInSpecificWorkspaceVault == true) {
                 //Add selected vault to vaultList
                 vaultList.add(specifiedWorkspaceVaultMap);
               } else {
                 //Search in all workspaceVaults for the project
                 vaultList = getWorkspaceVaults(context, project, busSelects, 1);
               }
             }  //end else include subfolders was not checked
           } //end if topParentHolderId != null
           //If user is just looking for workspace vaults, then just return the vaultList
           if (DocumentType.equals(project.TYPE_WORKSPACE_VAULT)) {
             queryResultList = vaultList;
           }
           //Else, get all files for each workspaceVault
           else if (vaultList != null){
             Iterator vaultItr = vaultList.iterator();
             while (vaultItr.hasNext()) {
               Map vaultMap   = (Map) vaultItr.next();
               String vaultId = (String) vaultMap.get(project.SELECT_ID);
               workspaceVault.setId(vaultId);
               // See if the current workspaceVault contains any document objects

               documentList = workspaceVault.getItems(context, busSelects, null, busWhere, null);
               //get map for each document and add it to the list
               Iterator documentItr = documentList.iterator();
               while (documentItr.hasNext()) {
                 Map documentMap = (Map) documentItr.next();
                  queryResultList.add(documentMap);

               }  //end while docItr has next
               //Clear the workspaceVault bean so you can set it again at top of loop
               workspaceVault.clear();
             } //end while vaultItr has next
           } //end else if vaultList != null
           return queryResultList;
     }

     /**
      * convertToStringList - Converts string to stringlist
      * @param context the eMatrix <code>Context</code> object
      * @return StringList
      * @throws Exception if the operation fails
      * @since R210
      * @grade 0
      */

         static public StringList convertToStringList(String vaultStr, String splitAt)
     throws MatrixException
    {

    StringList vaultSL = new StringList();
    StringList vaultSplit = FrameworkUtil.split(vaultStr, splitAt);
    Iterator vaultItr = vaultSplit.iterator();
    while (vaultItr.hasNext()){
    vaultSL.add(((String) vaultItr.next()).trim());
    }
    return vaultSL;
    }
         static public StringList GetAllVaults(Context context)
         throws MatrixException
         {

    // Get all vaults so that user can choose
    // this is all company's vaults not all vaults from all servers
    StringList vaultList = new StringList();

    /*Person person =
    Person.getPerson(context);
    Company company = person.getCompany(context);

    StringList selectList = new StringList(2);
    selectList.add(company.SELECT_VAULT);
    selectList.add(company.SELECT_SECONDARY_VAULTS);
    Map companyMap = company.getInfo(context,selectList);
    StringList secVaultList = FrameworkUtil.split((String)companyMap.get(company.SELECT_SECONDARY_VAULTS),null);
    Iterator itr = secVaultList.iterator();

    String vaults = (String)companyMap.get(company.SELECT_VAULT);
    vaultList.add(vaults);
    while (itr.hasNext() )
    {
    vaultList.add(PropertyUtil.getSchemaProperty((String)itr.next()));
    }*/

    // get ALL vaults
    Iterator mapItr = VaultUtil.getVaults(context).iterator();
    String vault="";
    if(mapItr.hasNext())
    {
    vault =(String)((Map)mapItr.next()).get("name");
    vaultList.add(vault);
    while (mapItr.hasNext())
    {
    Map map = (Map)mapItr.next();
    vault = (String)map.get("name");
    if(!vaultList.contains(vault))
    vaultList.add(vault);
    }
    }
    return vaultList;
    }
    /**
    * GetAllLocalVaults - method to return the list of all local vaults
    * @param context the eMatrix <code>Context</code> object
    * @return StringList
    * @throws Exception if the operation fails
    * @since R210
    * @grade 0
    */



    static public StringList GetAllLocalVaults(Context context)
                  throws MatrixException
    {
    StringList localVaultList = new StringList();
    /*StringList vaultList = GetAllVaults(context);
    StringItr itr = new StringItr(VaultUtil.getLocalVaults(context));
    while (itr.next() )
    {
    String vault = itr.obj();
    if(vaultList.contains(vault))
    {
    localVaultList.add(vault);
    }
    }*/

    // get All Local vaults
    StringList vaultList = VaultUtil.getLocalVaults(context);
    StringItr strItr = new StringItr(vaultList);
    String vault="";
    if(strItr.next()){
    vault =strItr.obj().trim();
    localVaultList.add(vault);
    }
    while(strItr.next())
    {
    vault = strItr.obj().trim();
    if(!localVaultList.contains(vault))
    localVaultList.add(vault);
    }

    return localVaultList;
    }

    /**
     * GetAllRemoteVaults - method to return the list of all the remote vaults
     * @param context the eMatrix <code>Context</code> object
     * @return StringList
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */

    static public StringList GetAllRemoteVaults(Context context)
                   throws MatrixException
    {
    StringList vaultList = GetAllVaults(context);
    StringItr itr = new StringItr(GetAllLocalVaults(context));
    while (itr.next())
    {
    vaultList.remove(itr.obj());
    }

    return vaultList;
    }

    /**
     * getWorkspaceVaults - method to return the list of folders present under the workspace
     * @param context the eMatrix <code>Context</code> object
     * @return MapList
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */

    static MapList getWorkspaceVaults(Context context, DomainObject domainobject, StringList stringlist, int i)
    throws FrameworkException
{
    String s = DomainConstants.RELATIONSHIP_WORKSPACE_VAULTS;
    if(i != 1)
        s = s + "," + DomainConstants.RELATIONSHIP_SUB_VAULTS;
    MapList maplist = domainobject.getRelatedObjects(context, s, "*", stringlist, null, false, true, (short)i, null, null);
    return maplist;
}
    /**
     * getAllSubTypes - method to return all the subtypes for available types
     * @param context the eMatrix <code>Context</code> object
     * @return BusinessTypeList
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */
    static public BusinessTypeList getAllSubTypes(Context context, String type, BusinessTypeList busTypeList)
    throws MatrixException
  {
     if (busTypeList == null)
         busTypeList = new BusinessTypeList();
     BusinessType busType = new BusinessType(type, context.getVault());
     busType.open(context);

     BusinessTypeList tempTypeList = busType.getChildren(context);
     Iterator itr = tempTypeList.iterator();
     while ( itr.hasNext() ) {
         BusinessType busChildType = (BusinessType) itr.next();
         busTypeList.addElement(busChildType);
         busTypeList = getAllSubTypes(context, busChildType.getName(),busTypeList);
     }
     return busTypeList;
  }

    /**
     * getOrganizationHTMLOutput - method to return the organization related to workspace
     * @param context the eMatrix <code>Context</code> object
     * @return String
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */

   public String getOrganizationHTMLOutput(Context context, String[] args) throws Exception
   {
       StringBuffer sb = new StringBuffer();
       Person person1 = Person.getPerson(context);
       Company company = person1.getCompany(context);
       BusinessObject myBusinessUnit = getConnectedObject(context,person1,"relationship_BusinessUnitEmployee",
                                        "type_BusinessUnit",true, false);

       BusinessObjectList organizationList = new BusinessObjectList();
       organizationList.add( company );

       // add the list of companies collaborating with your Parent Comapny
       company.open( context );
       String myOrganizationId = company.getObjectId();
     //  RelationshipItr collaborationRelItr = new RelationshipItr( myOrganization.getFromRelationship( context ));

       ExpansionWithSelect expWithSel2 = company.expandSelect(context, "Collaboration Partner", "Company,Business Unit", new StringList(), new StringList(), false, true, (short)1, "", "", false);
       RelationshipWithSelectList relWithSelList2 = expWithSel2.getRelationships();
       RelationshipWithSelectItr relWithSelItr2 = new RelationshipWithSelectItr(relWithSelList2);
       while(relWithSelItr2.next()) {
        BusinessObject relObj = relWithSelItr2.obj().getTo();
        relObj.open(context);
        organizationList.add(relObj);
        relObj.close(context);
       }

       if ( myBusinessUnit != null ){
        myBusinessUnit.open( context );
        expWithSel2 = myBusinessUnit.expandSelect(context, "Collaboration Partner", "Company,Business Unit", new StringList(), new StringList(), false, true, (short)1, "", "", false);
        relWithSelList2 = expWithSel2.getRelationships();
        relWithSelItr2 = new RelationshipWithSelectItr(relWithSelList2);
        while(relWithSelItr2.next()) {
         BusinessObject relObj = relWithSelItr2.obj().getTo();
         relObj.open(context);
         organizationList.add(relObj);
         relObj.close(context);
        }

        myBusinessUnit.close( context );
       }

               //sorting of company names disregarding case begins.
               Vector orgVect = new Vector(organizationList.size());
               Vector orgDetails = new Vector(2);
               HashMap orgMap = new HashMap();

               BusinessObjectItr orgItr = new BusinessObjectItr( organizationList );
               while (orgItr.next()) {
                 orgDetails = new Vector(2);
                 BusinessObject organization = orgItr.obj();
                 String organizationName = organization.getName();
                 //
                 // store as HashMap (String, Vector)


                 orgDetails.add(organizationName);
                 orgDetails.add(organization.getObjectId());
                 orgMap.put(organizationName,orgDetails);
                 orgVect.addElement(organizationName);

               }

               //sort Vector
               java.util.Collections.sort(orgVect);
       sb.append("<select name=\"Organization\" size=\"1\">");
       for (int i=0;i<orgVect.size();i++){
           String orgKey = (String)orgVect.elementAt(i);
           orgDetails = (Vector)orgMap.get(orgKey);
           String organizationName = (String)orgDetails.elementAt(0);
           String organizationId = (String)orgDetails.elementAt(1);

           if ( organizationId.equals( myOrganizationId ) ) {

           sb.append("<option value=\""+XSSUtil.encodeForHTMLAttribute(context,organizationId)+"\" selected>"+XSSUtil.encodeForHTML(context,organizationName)+"</option>");
           } else {
               sb.append("<option value=\""+XSSUtil.encodeForHTMLAttribute(context,organizationId)+"\" >"+XSSUtil.encodeForHTML(context,organizationName)+"</option>");
           }
       }
       return sb.toString();
   }

   /**
    * getRoleLevelsHTMLOutput - method to return the level of Roles for search
    * @param context the eMatrix <code>Context</code> object
    * @return String
    * @throws Exception if the operation fails
    * @since R210
    * @grade 0
    */


   public String getRoleLevelsHTMLOutput(Context context, String[] args) throws Exception
   {
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       HashMap paramMap = (HashMap) programMap.get("paramMap");
       String languageStr = (String) paramMap.get("languageStr");
       StringBuffer sb = new StringBuffer();
       String topLevel = EnoviaResourceBundle.getProperty(context, "eServiceSuiteTeamCentral", "emxTeamCentral.CreatePerson.TopLevel", languageStr);
       String subLevel = EnoviaResourceBundle.getProperty(context, "eServiceSuiteTeamCentral", "emxTeamCentral.CreatePerson.SubLevel", languageStr);
       sb.append("<input type=checkbox name=chkTopLevel id=chkTopLevel/>"+XSSUtil.encodeForHTML(context,topLevel)+"<br>");
       sb.append("<input type=checkbox name=chkSubLevel id=chkSubLevel />"+XSSUtil.encodeForHTML(context,subLevel));

       return sb.toString();
   }

   /**
    * getWorkspaceRoles - method to return the list of roles for search under Workspaces
    * @param context the eMatrix <code>Context</code> object
    * @return MapList
    * @throws Exception if the operation fails
    * @since R210
    * @grade 0
    */

  public MapList getWorkspaceRoles(Context context, String[] args) throws Exception
  {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String organizationId = (String)programMap.get("objectId");
      String languageStr = (String) programMap.get("languageStr");
      String sNamePattern     = (String)programMap.get("Name");
      String sTopChecked     = (String)programMap.get("chkTopLevel");
      String sSubChecked     = (String)programMap.get("chkSubLevel");
      String queryLimit = (String)programMap.get("queryLimit");
      queryLimit = UIUtil.isNullOrEmpty(queryLimit) ? (String)programMap.get("QueryLimit") : queryLimit;
      Locale locale = new Locale(languageStr);

      int intQueryLimit = Integer.MAX_VALUE;
      try {
          intQueryLimit = Integer.parseInt(queryLimit);
      } catch(Exception e){}

      StringList granteeList    = new StringList();
      BusinessObject boProject  = new BusinessObject(organizationId);
      i18nNow loc = new i18nNow();
      if (boProject != null) {
        granteeList = boProject.getGrantees(context);
      }

      Role role         = new Role();
      RoleItr roleItr   = null;
      StringList topRoleNameList  = new StringList();
      MapList templateMapList  =  new MapList();
      Pattern pattern = null;

      if(sNamePattern != null) {
        pattern = new Pattern(sNamePattern);
      } else {
        pattern = new Pattern("*");
      }

      if (sSubChecked != null) {
        if (sTopChecked == null) {
          roleItr = new RoleItr(role.getTopLevelRoles(context));
          while(roleItr.next()) {
            topRoleNameList.add(roleItr.obj().getName());
          }
        }
        RoleList roleList = role.getRoles(context);
        roleList.sort();
        roleItr = new RoleItr(roleList);
      } else {
        if (sTopChecked != null) {
          RoleList roleList = role.getTopLevelRoles(context);
          roleList.sort();
          roleItr = new RoleItr(roleList);
        } else {
          RoleList roleList = role.getRoles(context);
          roleList.sort();
          roleItr = new RoleItr(roleList);
        }
      }
      String translatedRoleTypeString = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource", locale, "emxTeamCentral.Common.Role");
      while (roleItr.next()) {
        String sRole = roleItr.obj().getName();
        //match with the Internationalized value not the actual value
        String sRoleDisplay = loc.getRoleI18NString(sRole, languageStr);
        if (!topRoleNameList.contains(sRole) && pattern.match((String) sRoleDisplay)) {
          Hashtable hashTableFinal  = new Hashtable();
          String sCheckRole = granteeList.contains(sRole) ? "true" : "false";
          hashTableFinal.put("lock",sCheckRole);
          hashTableFinal.put("RoleName",sRole);
          hashTableFinal.put("TRoleName", sRoleDisplay);
          //IR-055425V6R2011x
          hashTableFinal.put("id",sRole);
          hashTableFinal.put("RoleType", "Role");
          hashTableFinal.put("TRoleType", translatedRoleTypeString);
          hashTableFinal.put("Description", i18nNow.getRoleDescriptionI18NString(sRole, languageStr));
          templateMapList.add(hashTableFinal);
        }
      }
      templateMapList  = RoleUtil.filterRoleSearchResults(context, templateMapList, "RoleName");
      if(templateMapList.size() > intQueryLimit) {
          templateMapList.sort("TRoleName", null, null);
          String strMessage = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",  locale, "emxComponents.Warning.ObjectFindLimit") + queryLimit + EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", locale, "emxComponents.Warning.Reached");
          emxContextUtil_mxJPO.mqlNotice(context,strMessage);
          return new MapList(templateMapList.subList(0, intQueryLimit));
      }
      return templateMapList;
  }






  /**
   * getAllWorkspaceMembers - method to return the list of all workspace members
   * @param context the eMatrix <code>Context</code> object
   * @return MapList
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */

  public MapList getAllWorkspaceMembers(Context context, String[] args) throws Exception
  {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      //Retrieve Search criteria
      String strUserName      = (String)paramMap.get("User Name");
      String strLastName      = (String)paramMap.get("Last Name");
      String strFirstName     = (String)paramMap.get("First Name");
      String strOrgId         = (String)paramMap.get("Organization");
      String languageStr      = (String)paramMap.get("languageStr");
      //added for bug 299784
      String fromFile         =(String)paramMap.get("FromFile");

      DomainObject domOrgObj = new DomainObject(strOrgId);
      String strOrgType = domOrgObj.getInfo(context,DomainObject.SELECT_TYPE);

      String queryLimit = (String)paramMap.get("queryLimit");
      queryLimit = UIUtil.isNullOrEmpty(queryLimit) ? (String)paramMap.get("QueryLimit") : queryLimit;

      int intQueryLimit = Integer.MAX_VALUE;
      try {
          intQueryLimit = Integer.parseInt(queryLimit);
      } catch(Exception e){}

      strUserName = UIUtil.isNullOrEmpty(strUserName) ? "*" : strUserName;
      strLastName = UIUtil.isNullOrEmpty(strLastName) ? "*" : strLastName;
      strFirstName = UIUtil.isNullOrEmpty(strFirstName) ? "*" : strFirstName;

      String sPersonActiveState = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_PERSON, "state_Active");

      String relPattern = TYPE_BUSINESS_UNIT.equals(strOrgType) ? RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE : RELATIONSHIP_EMPLOYEE;

      String strRelWhere = "";
      //Modified for bug 336145
      if(!strUserName.equals("*") && !strUserName.equals(""))
      {
          if(strUserName.indexOf("*") == -1)
          {
              strRelWhere += "(to.name matchlist \"" + strUserName + "\" \",\")";
          } else {
              strRelWhere += "(to.name ~= \""+strUserName+"\")";
          }
      }

      if(!strLastName.equals("*") && !strLastName.equals(""))
      {
          if(strLastName.indexOf("*") == -1)
          {
              if(strRelWhere.length()>0) {strRelWhere += " && ";}
              strRelWhere += "(\"" + "to.attribute["+ATTRIBUTE_LAST_NAME+"]\" matchlist \"" + strLastName + "\" \",\")";
          } else {
              if(strRelWhere.length()>0) {strRelWhere += " && ";}
              strRelWhere += "(to.attribute["+ATTRIBUTE_LAST_NAME+"] ~= \""+strLastName+"\")";
          }
      }
      if(!strFirstName.equals("*") && !strFirstName.equals(""))
      {
          if(strFirstName.indexOf("*") == -1)
          {
              if(strRelWhere.length()>0) {strRelWhere += " && ";}
              strRelWhere += "(\"" + "to.attribute["+ATTRIBUTE_FIRST_NAME+"]\" matchlist \"" + strFirstName + "\" \",\")";
          } else {
              if(strRelWhere.length()>0) {strRelWhere += " && ";}
              strRelWhere += "(to.attribute["+ATTRIBUTE_FIRST_NAME+"] ~= \""+strFirstName+"\")";
          }
      }
      //End of modification for bug 336145

      /* To display only the Active person in Person Search Results */
      if(strRelWhere.length()>0) {
          strRelWhere += " && ";
      }
      strRelWhere += "(to.current.name == \'"+sPersonActiveState+"\')";
      strRelWhere = FrameworkUtil.findAndReplace(strRelWhere,".*",".**");
      strRelWhere = FrameworkUtil.findAndReplace(strRelWhere,"*.","**.");

      SelectList selectStmts = new SelectList(8);
      selectStmts.add(DomainObject.SELECT_ID);
      selectStmts.add(DomainObject.SELECT_NAME);
      selectStmts.add(DomainObject.SELECT_CURRENT);
      selectStmts.add(DomainObject.SELECT_TYPE);
      selectStmts.add(DomainObject.getAttributeSelect(ATTRIBUTE_FIRST_NAME));
      selectStmts.add(DomainObject.getAttributeSelect(ATTRIBUTE_LAST_NAME));
      selectStmts.add(DomainObject.getAttributeSelect(PropertyUtil.getSchemaProperty( context, "attribute_WorkPhoneNumber")));
      selectStmts.add(DomainObject.getAttributeSelect(PropertyUtil.getSchemaProperty( context, "attribute_LoginType")));

      MapList mapList = null;
      try
      {
          ContextUtil.startTransaction(context,false);
          ExpansionIterator expIter   = domOrgObj.getExpansionIterator(context,
                                                                      relPattern,
                                                                      typePerson,
                                                                      selectStmts,
                                                                      new SelectList(),
                                                                      false,
                                                                      true,
                                                                      (short)1,
                                                                      null,
                                                                      strRelWhere,
                                                                      (short)0,
                                                                      false,
                                                                      false,
                                                                      (short)100,
                                                                      false);

          try {
              mapList = FrameworkUtil.toMapList(expIter,(short)0,null,null,null,null);
          } finally {
              expIter.close();
          }
          ContextUtil.commitTransaction(context);
      }
      catch(Exception ex)
      {
          ContextUtil.abortTransaction(context);
          throw new Exception(ex.toString());
      }
      //added for 299784
      if( fromFile!=null && fromFile.length()>0 && fromFile.trim().equals("emxComponentsFindMemberDialog.jsp") )
      {
          Iterator mapItr = mapList.iterator();
          MapList constructedList = new MapList();

          String cState = "";
          while (mapItr.hasNext()) {
              Map map = (Map)mapItr.next();
              cState = (String)map.get(DomainObject.SELECT_CURRENT);
              if( cState.equals(DomainConstants.STATE_PERSON_ACTIVE)) {
                  constructedList.add(map);
              }
          }
          mapList=constructedList;
      }
      //till here

      if(mapList != null) {
          // default dir is ascending and type is string (for sorting)
          mapList.sort(DomainObject.SELECT_NAME,null,null);

          if(mapList.size() > intQueryLimit) {
        	  Locale locale = new Locale(languageStr);
              String strMessage = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", locale, "emxComponents.Warning.ObjectFindLimit") + queryLimit + EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", locale, "emxComponents.Warning.Reached");
              emxContextUtil_mxJPO.mqlNotice(context,strMessage);
              return new MapList(mapList.subList(0, intQueryLimit));
          }
      }
      return mapList;
    }


  /**
   * showUserSecureID - method to return the secure ID for users in the workspace members add search results
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */

  public Vector showUserSecureID(Context context, String[] args) throws Exception
  {
      Vector loginTypes = new Vector();
      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      MapList objectList=(MapList)programMap.get("objectList");
      String strloginType   = "attribute["+PropertyUtil.getSchemaProperty( context, "attribute_LoginType")+"]";
      for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
      {
          Map mapAccess=(Map)objectListItr.next();
          String image    = "";
          String secureID  = (String)mapAccess.get(strloginType);
          image = secureID.equals("Secure ID") ? "<img src=\"images/iconSmallAccess.gif\" alt=\"\">" : "&nbsp";
          loginTypes.add(image);
      }
      return loginTypes;
  }

  /**
   * getWorkspaceRoleNames - method to return the names of the roles
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */

  public Vector getWorkspaceRoleNames(Context context, String[] args) throws Exception
  {
      Vector rolesList = new Vector();
      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      MapList objectList=(MapList)programMap.get("objectList");
      for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
      {
          Map mapAccess=(Map)objectListItr.next();
          String translatedRoleNameString  = (String)mapAccess.get("TRoleName");
          rolesList.add("<img src=\"../common/images/iconSmallRole.gif\" name=\"imgRole\" id=\"imgRole\" alt=\""+XSSUtil.encodeForHTMLAttribute(context, translatedRoleNameString)+"\" />"+XSSUtil.encodeForHTML(context, translatedRoleNameString)+"&nbsp;");

      }
      return rolesList;
  }

  /**
   * getRolesCheckbox - method to return the checkbox for selecting the roles to add
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */

  public Vector getRolesCheckbox(Context context, String[] args) throws Exception
  {
      Vector rolesList = new Vector();
      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      MapList objectList=(MapList)programMap.get("objectList");
      for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
      {
          Map mapAccess=(Map)objectListItr.next();
          String sRoleName  = (String)mapAccess.get("RoleName");
          String sLock      = (String)mapAccess.get("lock");
          if (sLock.equals("true")) {

              rolesList.add("<img src=\"images/iconCheckoffdisabled.gif\" alt=\"\">");

                    } else {

              rolesList.add("<input type=\"checkbox\" name =\"chkItem\" id=\"chkItem\" value = \""+sRoleName+"\" onclick=\"updateCheck()\"/>");

                    }

      }
      return rolesList;
  }
  /**
   * getWorkspaceRoleDescription - method to return the description for each role in the search
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */
  public Vector getWorkspaceRoleDescription(Context context, String[] args) throws Exception
  {
      Vector rolesList = new Vector();
      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      MapList objectList=(MapList)programMap.get("objectList");
      for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
      {
          Map mapAccess=(Map)objectListItr.next();
          String sDescription = (String)mapAccess.get("Description");
          rolesList.add(sDescription);

      }
      return rolesList;
  }

   /**
   * showProjectMembersCheckbox - method to return the checkbox for selecting each workspace member
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */

  public Vector showProjectMembersCheckbox(Context context, String[] args) throws Exception
  {
      Vector membersCheckboxes = new Vector();
      Vector vectIncludedpersonList = new Vector();
      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      HashMap paramMap=(HashMap)programMap.get("paramList");
      //IR-055549V6R2011x
      String workspaceId=(String)paramMap.get("objectId");
      DomainObject templateObj=DomainObject.newInstance(context,workspaceId);
      if(workspaceId != null && !"".equals(workspaceId) && templateObj.getInfo(context, templateObj.SELECT_TYPE).equals(templateObj.TYPE_WORKSPACE)){
          vectIncludedpersonList = getAllProjectmembers(context,workspaceId );
                }
      MapList objectList=(MapList)programMap.get("objectList");
      for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
      {
          Map mapAccess=(Map)objectListItr.next();
          String personId  = (String)mapAccess.get("id");

      if (vectIncludedpersonList.contains(personId)){
          membersCheckboxes.add("false");
      }
      else{
          membersCheckboxes.add("true");
      }
      }
      return membersCheckboxes;
  }

  /**
   * getAllProjectmembers - method to return the list of all workspace members
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */
  public Vector getAllProjectmembers(Context context, String workspaceId) throws Exception
  {
      String projectMemberStr = PropertyUtil.getSchemaProperty(context, "type_ProjectMember");
      String personStr = PropertyUtil.getSchemaProperty(context, "type_Person");
      String projectMembershipStr = PropertyUtil.getSchemaProperty(context, "relationship_ProjectMembership");
      String projectMembersStr = PropertyUtil.getSchemaProperty(context, "relationship_ProjectMembers");
      Vector memberList = new Vector();
      BusinessObject activeProject = new BusinessObject( workspaceId );

      String relPattern = projectMembersStr + "," + projectMembershipStr;
      String typePattern = projectMemberStr + "," + personStr;

      SelectList selectStmts = new SelectList(1);
      selectStmts.addId();

      ExpansionWithSelect projectSelect = activeProject.expandSelect(context,relPattern ,typePattern,
                                               selectStmts,new StringList(0),true, true, (short)2);

      RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(projectSelect.getRelationships());

      Hashtable personAttributesTable = null;

      String personId = null;

      while (relItr.next()) {
        if ( relItr.obj().getTypeName().equals(projectMembersStr)) {
          relItr.next();
          if ( relItr.obj().getTypeName().equals(projectMembershipStr)) {
            personAttributesTable =  relItr.obj().getTargetData();
            personId = (String)personAttributesTable.get("id");
            memberList.add(personId);
          }
        }
      }
      return memberList;
  }
  /**
   * getDisucssionFindfilesResult - method to return the list of files to add as attachments for discussions
   * @param context the eMatrix <code>Context</code> object
   * @return MapList
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getDisucssionFindfilesResult(Context context, String[] args) throws Exception
  {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      Person PersonObject = (Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

      DomainObject domainObject    = DomainObject.newInstance(context);
      DomainObject templateObj     = DomainObject.newInstance(context);
      Company company              = (Company) DomainObject.newInstance(context,DomainConstants.TYPE_COMPANY,DomainConstants.TEAM);
      MapList templateMapList      =  new MapList();

      String sOwnedFiles           = (String)programMap.get("ownedFiles");
      //String sOwner                = (String)programMap.get("OwnerDisplay");
      String sOwner                = (String)programMap.get("Owner");
      String sFileName             = (String)programMap.get("name");
      String sKeywords             = (String)programMap.get("Keywords");
      String sWorkspaceFolder      = (String)programMap.get("WorkspaceFolderDisplay");
      String sWorkspaceFolderId    = (String)programMap.get("WorkspaceFolder");
      String sWorkspacesubfolders  = (String)programMap.get("workspacesubfolders");
      String sCreatedAfter         = (String)programMap.get("createdAfter");
      String sCreatedBefore        = (String)programMap.get("createdBefore");
      String sSelectedObjId        = (String)programMap.get("objectId");
      String sProjectId            = (String)programMap.get("projectId");
      String matchCase             = (String)programMap.get("matchCase");
      String sfromPage             = (String)programMap.get("fromPage");

      String sTypeProjectVault     = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
      String sTypeDocument         = PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");
      String sRelSubVaults         = PropertyUtil.getSchemaProperty(context,"relationship_SubVaults");
      String sRelVaultedDocuments  = PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocuments");
      String strTitleAttr          = PropertyUtil.getSchemaProperty(context,"attribute_Title" );
      String sRelObjRou            = PropertyUtil.getSchemaProperty(context, "relationship_ObjectRoute");

      double clientTZOffset           = (new Double((String) programMap.get("timeZone"))).doubleValue();
      //Formatting Date to Ematrix Date Format
      if(sCreatedAfter!=null && !"".equals(sCreatedAfter))
        sCreatedAfter = eMatrixDateFormat.getFormattedInputDate(context,sCreatedAfter,clientTZOffset,context.getLocale());
      if(sCreatedBefore!=null && !"".equals(sCreatedBefore))
        sCreatedBefore = eMatrixDateFormat.getFormattedInputDate(context,sCreatedBefore,clientTZOffset,context.getLocale());

      DomainObject fromObject    = DomainObject.newInstance(context);

      if(sWorkspaceFolder == null ){
      sWorkspaceFolder = "";
      }
      if(sfromPage == null ){
          sfromPage = "";
      }
      if( sSelectedObjId != null && (!sSelectedObjId.equals(""))){
          fromObject.setId(sSelectedObjId);
        }
      if(sWorkspaceFolderId==null || sWorkspaceFolderId.equals("*")){
        sWorkspaceFolderId="";
      }

      MapList constructedList = new MapList();
      String busWhere   = "";
      boolean bFlag     = false;

      if(sProjectId == null) {
        sProjectId=getProjectId(context,sSelectedObjId);
      }

      String loggedInUser = context.getUser();
      Person person = PersonObject.getPerson(context);

      DomainObject domainPerson    = DomainObject.newInstance(context,person,DomainConstants.TEAM);
      String myCompanyId = domainPerson.getInfo(context, "to["+domainPerson.RELATIONSHIP_EMPLOYEE+"].from.id");
      company.setId(myCompanyId);
      StringList companyVaultList = company.getExtendedVaults(context);
      String secVault             = company.getInfo(context , DomainConstants.SELECT_SECONDARY_VAULTS);
      String strVault             = null;
      String sVaultVal            = null;


      StringTokenizer vaultTokens = new StringTokenizer(secVault, "~");
      while (vaultTokens.hasMoreTokens()){
        strVault = (String)vaultTokens.nextToken();
        sVaultVal = PersonUtil.getSearchVaults(context,false,strVault);
        if(!companyVaultList.contains(sVaultVal)){
          companyVaultList.add(sVaultVal);
        }
      }
      String vaultString = "";
      Iterator vaultItr = companyVaultList.iterator();

      StringList vaultNames = new StringList();
      while(vaultItr.hasNext()) {
        if(vaultString.length() == 0) {
          vaultString = vaultString + (String)vaultItr.next();
        } else {
          String vaultName = (String)vaultItr.next();
          if((vaultString.length() + vaultName.length()) > 256) {
            vaultNames.addElement(vaultString);
            vaultString = vaultName;
          } else {
            if(vaultString.length() >0)
                vaultString = vaultString + "," + vaultName;
            else
                vaultString =  vaultName;
          }
        }
      }
      vaultNames.addElement(vaultString);

        if (sKeywords == null) {
          sKeywords = "";
        }

        //get only the latest revisions
        busWhere += "(revision == last)";

        if(null == sKeywords.trim() || ("null").equals(sKeywords.trim()) || ("").equals(sKeywords.trim()) || ("*").equals(sKeywords.trim())) {
          sKeywords = null;
        }

        if(null==sFileName || ("null").equals(sFileName.trim()) || ("").equals(sFileName.trim()) || ("*.*").equals(sFileName.trim())) {
          sFileName = "*";
        }

        boolean nameMatchCase = false;
        if("True".equals(matchCase)) {
          nameMatchCase = true;
        }

        if ( sFileName.indexOf(",") != -1 ) {

          // if "," is given for the file name then it is seperated with string tokenizer
          bFlag = true;
          StringTokenizer stToken = new StringTokenizer(sFileName, ",");
          int sTokenSize          = stToken.countTokens();
          int sSize               = 0;

          if(busWhere.length()>0) {
            busWhere+= " && ";
          }

          while (stToken.hasMoreTokens()) {
            sSize += 1;
            String sToken = stToken.nextToken();

            // if the name parameter is only file alone
            if (sFileName.indexOf("*") == -1) {
              if ( sSize == sTokenSize ) {
                if(nameMatchCase){
                  busWhere += "(attribute["+strTitleAttr+"] == \""+ sToken +"\")";
                }else{
                  busWhere += "(attribute["+strTitleAttr+"] ~~ \""+ sToken +"\")";
                }
              } else {
                if(nameMatchCase){
                  busWhere += "(attribute["+strTitleAttr+"] == \""+ sToken +"\")" + "||";
                }else{
                  busWhere += "(attribute["+strTitleAttr+"] ~~ \""+ sToken +"\")" + "||";
                }
              }
            } else {
              // if the name parameter is * and *  with some character
              if (!sFileName.trim().equals("*")) {
                if ( sSize == sTokenSize ) {
                  if(nameMatchCase){
                    busWhere += "(attribute["+strTitleAttr+"] ~= \""+ sToken +"\")";
                  }else{
                    busWhere += "(attribute["+strTitleAttr+"] ~~ \""+ sToken +"\")";
                  }
                } else {
                  if(nameMatchCase){
                    busWhere += "(attribute["+strTitleAttr+"] ~= \""+ sToken +"\")" + "||";
                  }else{
                    busWhere += "(attribute["+strTitleAttr+"] ~~ \""+ sToken +"\")" + "||";
                  }
                }
              }
            }
          }
        }
        if ( !bFlag ) {
          if(sFileName.indexOf("*") == -1) {
            if(busWhere.length()>0) {
              busWhere+= " && ";
            }
            if(nameMatchCase){
              busWhere += "(attribute["+strTitleAttr+"] == \""+sFileName+"\")";
            }else{
              busWhere += "(attribute["+strTitleAttr+"] ~~ \""+sFileName+"\")";
            }
          } else {
            if (!sFileName.trim().equals("*")) {
              if(busWhere.length()>0) {
                busWhere+= " && ";
              }
              if(nameMatchCase){
                busWhere += "(attribute["+strTitleAttr+"] ~= \""+sFileName+"\")";
              }else{
                busWhere += "(attribute["+strTitleAttr+"] ~~ \""+sFileName+"\")";
              }
            }
          }
        }
        if(null==sOwnedFiles || ("null").equals(sOwnedFiles) || ("").equals(sOwnedFiles)) {
          sOwnedFiles = "true";
        }

        String owner = "*";
        sOwner = sOwner.trim();
        if(sOwnedFiles.equals("true")) {
          owner = context.getUser();
        }

        if(sOwner.trim().equals("*")) {
          sOwner = "";
        }

        if(sOwner==null || ("null").equals(sOwner) || (",").equals(sOwner)) {
          sOwner = "";
        }

        StringTokenizer st = new StringTokenizer(sOwner,";");

        if(!sOwnedFiles.equals("true")) {
          while (st.hasMoreTokens()) {
            if(owner.equals("*")) {
              owner = "";
            }
            owner = owner + "," + st.nextToken();
          }
        }

        if(sCreatedBefore==null) {
          sCreatedBefore = "";
        }
        if(sCreatedBefore.trim().length()!=0) {
          if(busWhere.length()>0) {
          busWhere+= " && ";
        }
          busWhere += "(originated < \"" + sCreatedBefore + "\")";
        }

        if(sCreatedAfter==null) {
          sCreatedAfter = "";
        }
        if(sCreatedAfter.trim().length()!=0) {
          if(busWhere.length()>0) {busWhere+= " && ";}
          busWhere += "(originated > \"" + sCreatedAfter +"\")";
        }

        if(sWorkspacesubfolders==null) {
          sWorkspacesubfolders = "";
        }

        String folderId ="";
        BusinessObjectList boDocumentList = new BusinessObjectList();



        if(sWorkspaceFolder.equals("*")) {
          //build specific where clauses

          if(!sWorkspacesubfolders.equals("True")) {
            if(busWhere.length()>0) {busWhere+= " && ";}
            busWhere += "(!to["+sRelVaultedDocuments+"].from.to["+sRelSubVaults+"].id  !~~ \"zz\")";
          }

          if (busWhere.length()>0) {
            busWhere+= " && ";
          }

          busWhere += "((\"to["+sRelVaultedDocuments+"].from.id\" !~~ \"zz\")) && (current.access[read] == TRUE)";
          StringList selects = new StringList();
          selects.addElement(DomainObject.SELECT_NAME);
          selects.addElement(DomainObject.SELECT_ID);

          matrix.db.Query query = new matrix.db.Query();
          query.create(context);

          Iterator vaultNamesItr = vaultNames.iterator();
          while(vaultNamesItr.hasNext()) {
            String vault = (String)vaultNamesItr.next();
            //query.open(context);
            query.setBusinessObjectType(sTypeDocument);
            query.setBusinessObjectName("*");
            query.setBusinessObjectRevision("*");
            query.setVaultPattern(vault);
            query.setOwnerPattern(owner);
            query.setWhereExpression(busWhere);
            query.setSearchText(sKeywords);
            MapList docMapList = FrameworkUtil.toMapList(query.select(context, selects));
            //query.close(context);
            templateMapList.addAll(docMapList);
          }
        } else {

          // loop thro. the folders/subfolders, do expand and get the doc list
          // put the doc list into a set, and do a keyword search on the set
          // get the final results as a bo list

          if(sKeywords!=null) {
            if(busWhere.length()>0) {busWhere+= " && ";}
            busWhere += "(search["+sKeywords+"] == TRUE)";
          }

          if(owner.trim().length()>0) {
            st = new StringTokenizer(owner, ",");
            String ownerWhere = "(";
            String ownerName = "";
            while (st.hasMoreTokens()) {
              ownerName = st.nextToken();
              if (ownerName.equals("*")){
              break;
              }

              if(ownerWhere.equals("(")) {
                ownerWhere += "(owner == \""+ownerName+"\")";
              } else {
                ownerWhere += "|| (owner == \""+ownerName+"\")";
              }
            }
            ownerWhere += ")";
            if(!ownerWhere.equals("()")) {
              if (busWhere.length()>0) {
                busWhere+= " && ";
              }
              busWhere += ownerWhere;
            }
          }

          Pattern relPattern  = null;
          Pattern typePattern = null;
          st          = new StringTokenizer(sWorkspaceFolderId,",");
          relPattern  = new Pattern(sRelVaultedDocuments);
          typePattern = new Pattern(sTypeDocument);

          short expandLevel = (short)1;
          //if sub-folders are selected, modify the rel and type patterns
          if(sWorkspacesubfolders.equals("True")) {
            expandLevel = (short)0;
            relPattern.addPattern(sRelSubVaults);
            typePattern.addPattern(sTypeProjectVault);
          }

          //iterate thro. the folders
          while (st.hasMoreTokens()) {
            folderId = st.nextToken();
            Pattern includeRelPattern = new Pattern(sRelVaultedDocuments);

            StringList objSelects = new StringList();
            objSelects.addElement(DomainObject.SELECT_ID);
            objSelects.addElement(DomainObject.SELECT_NAME);
            domainObject.setId(folderId);
            MapList docMapList = domainObject.expandSelect(context,
                                                          relPattern.getPattern(),
                                                          typePattern.getPattern(),
                                                          objSelects,
                                                          new StringList(),
                                                          false,
                                                          true,
                                                          expandLevel,
                                                          null,
                                                          null,
                                                          null,
                                                          includeRelPattern,
                                                          null,
                                                          null, false);
            Iterator docListItr = docMapList.iterator();
            while(docListItr.hasNext()) {
              Map docMap = (Map)docListItr.next();
              String docId = (String)docMap.get(DomainObject.SELECT_ID);
              boDocumentList.addElement(new BusinessObject(docId));
            }
          }
          try {
            MQLCommand prMQL  = new MQLCommand();
            prMQL.open(context);
            String prMQLString        = "";
            String mqlError           = "";

            matrix.db.Set documentSet = new matrix.db.Set(".emxTempSet");
            try {
              documentSet.open(context);
              documentSet.appendList(boDocumentList);
              documentSet.setBusinessObjects(context);
              documentSet.close(context);
              boDocumentList  = new BusinessObjectList();

              //prMQLString = "add query mxdocsearch bus \"" + sTypeDocument + "\" \"*\" \"*\" where ' "+busWhere+ " ';";
              //prMQLString     = "add query mxdocsearch bus \"" + sTypeDocument + "\" \"*\" \"*\" vault \""+ vaultString +"\" where '"+busWhere.trim()+ "';";

              Person person1 = Person.getPerson(context);
              Company company1 = person1.getCompany(context);


              prMQLString     = "add query $1 bus $2 $3 $4 vault $5 where $6;";
              mqlError        = prMQL.getError();
              prMQLString     = "eval query $1 over set $2 into set $3;";
              mqlError = prMQL.getError();
              if(mqlError != null && mqlError.equals("")) {
                matrix.db.Set qSet = new matrix.db.Set(".emxResultSet");
                qSet.open(context);
                boDocumentList     = qSet.getBusinessObjects(context);
                qSet.close(context);
                prMQLString     = "delete query $1 ;";
                mqlError        = prMQL.getError();
                prMQL.close(context);
                qSet.remove(context);
              } else {
                prMQL.close(context);
              }
              documentSet.remove(context);
            } catch(Exception e) { }

          } catch(Exception e) {
            try {
              e.printStackTrace();
            } catch(Exception newE) {
              e.printStackTrace();
            }
            boDocumentList = new BusinessObjectList();
          }
          //populate the mapList before passing for pagination
          BusinessObjectItr boItr = new BusinessObjectItr(boDocumentList);
          while(boItr.next()) {
            BusinessObject boDoc = boItr.obj();
            String docId         = boDoc.getObjectId();
            Hashtable hashTableFinal = new Hashtable();
            hashTableFinal.put(DomainObject.SELECT_ID,docId);
            templateMapList.add(hashTableFinal);
          }
        }
        MapList paginatedMapList = templateMapList;
        String selAttrTitle     = "attribute["+DomainObject.ATTRIBUTE_TITLE+"]";
        String selVaultId       = "to["+sRelVaultedDocuments+"].from.id";
        String selVaultNames    = "to["+sRelVaultedDocuments+"].from.name";
        String selRouteId       = "from["+sRelObjRou+"].to.id";
        String toConnectAccess  = "current.access[toconnect]";

        Iterator paginatedMapListItr = paginatedMapList.iterator();
        StringList docIdList = new StringList();
        while(paginatedMapListItr.hasNext()) {
          Map map = (Map)paginatedMapListItr.next();
          String docId = (String)map.get(DomainObject.SELECT_ID);
          if(!docIdList.contains(docId) && !docId.equals("#DENIED!")) {
            docIdList.addElement(docId);
          }
        }

        StringList selects = new StringList();
        selects.addElement(DomainObject.SELECT_NAME);
        selects.addElement(DomainObject.SELECT_ID);
        selects.addElement(DomainObject.SELECT_REVISION);
        selects.addElement(DomainObject.SELECT_LOCKED);
        selects.addElement(DomainObject.SELECT_DESCRIPTION);
        selects.addElement(selAttrTitle);
        selects.addElement(selRouteId);
        DomainObject.MULTI_VALUE_LIST.add(selVaultId);
        selects.addElement(selVaultId);
        DomainObject.MULTI_VALUE_LIST.add(selVaultNames);
        selects.addElement(selVaultNames);
        selects.addElement(toConnectAccess);
        //get the details of the document objects for the current page
        paginatedMapList = DomainObject.getInfo(context, (String [])docIdList.toArray(new String []{}), selects);

        paginatedMapListItr = paginatedMapList.iterator();
        Hashtable projectHash = new Hashtable();

        while(paginatedMapListItr.hasNext()) {
          Hashtable hashTableFinal  = new Hashtable();
          Map map = (Map)paginatedMapListItr.next();
          String docId = (String)map.get(DomainObject.SELECT_ID);

          // get vault names for display
          StringList vaultNameList = new StringList();
          try {
            String vaultName = (String)map.get(selVaultNames);
            if (vaultName != null) {
              vaultNameList.addElement(vaultName);
            }
          } catch (ClassCastException classCastEx ) {
            vaultNameList = (StringList)map.get(selVaultNames);
          }

          // get vault ids
          StringList vaultIdList = new StringList();
          StringList workspaceNameList = new StringList();
          try {
            String folderVaultId = (String)map.get(selVaultId);
            if (folderVaultId != null) {
              vaultIdList.addElement(folderVaultId);
            }
          } catch (ClassCastException classCastEx ) {
            vaultIdList = (StringList)map.get(selVaultId);
          }

          String vaultName = "";
          String workspaceNames = "";
          String vaultId1 = "";
          Iterator vaultIdItr = vaultIdList.iterator();
          Iterator vaultNameItr = vaultNameList.iterator();

          //get the project for the documents, put the vaultId-projectNames into a hashTable
          //check for each vaultId, if it is in the hashtable, take the project name from it.
          while(vaultIdItr.hasNext()) {
            String vaultId = (String)vaultIdItr.next();
            String tempVaultName = (String)vaultNameItr.next();
            if(vaultId != null && !vaultId.equals("#DENIED!")) {
              if(vaultName.length() >0)
                  vaultName += tempVaultName + ";";
              else
                  vaultName += tempVaultName;
              vaultId1 += vaultId + ";";
              if(!projectHash.containsKey(vaultId)) {
                Map projectMap = getWorkspaceMap(context, vaultId);
                String workspaceOwner = (String)projectMap.get(DomainObject.SELECT_OWNER);
                String workspaceState = (String)projectMap.get(DomainObject.SELECT_CURRENT);
                String projectName = "";
                if(!loggedInUser.equals(workspaceOwner) && !workspaceState.equals("Active")) {
                  projectName = "";
                }
                else {
                  projectName = (String)projectMap.get(DomainObject.SELECT_NAME);
                }
                projectHash.put(vaultId, projectName);
              }
              String workspaceNameHash = (String)projectHash.get(vaultId);
              if(workspaceNameHash != null && workspaceNameHash.length() != 0) {
                if(!workspaceNameList.contains(workspaceNameHash)) {
                  workspaceNameList.addElement(workspaceNameHash);
                  if(workspaceNames.length() >0)
                      workspaceNames = workspaceNames + workspaceNameHash + "; ";
                  else
                      workspaceNames = workspaceNames + workspaceNameHash;
                }
              }
            }
          }
          try {
            if(workspaceNames != null && workspaceNames.length() != 0) {
              String locked       = (String)map.get(DomainObject.SELECT_LOCKED);
              hashTableFinal.put("lock",locked);
              hashTableFinal.put("workspaceNames",workspaceNames);
              hashTableFinal.put(templateObj.SELECT_ID,docId);
              hashTableFinal.put(templateObj.SELECT_NAME, (String)map.get(selAttrTitle));
              hashTableFinal.put("folder", vaultName);
              hashTableFinal.put("folderId", vaultId1);
              hashTableFinal.put("version", (String)map.get(DomainObject.SELECT_REVISION));
              hashTableFinal.put("desc", (String)map.get(DomainObject.SELECT_DESCRIPTION));
              hashTableFinal.put("toConnectAccess", (String)map.get(toConnectAccess));
              //check if the doc is connected to a route
              String routeId = (String)map.get(selRouteId);
              if(routeId!= null && !routeId.equals("")){
                hashTableFinal.put("inRoute", "true");
              }else{
                hashTableFinal.put("inRoute", "false");
              }
              constructedList.add(hashTableFinal);
            }
          } catch(Exception e) {
          }
        }
        //constructedList.add(genericTable);
        return constructedList;
   }
  /**
   * getWorkspaceMap - method to return the map of workspaces
   * @param context the eMatrix <code>Context</code> object
   * @return Map
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */
  static public Map getWorkspaceMap(matrix.db.Context context, String vaultId) throws MatrixException
  {
    String typeWorkspace  = PropertyUtil.getSchemaProperty(context, "type_Project");
    String typeProjectVault  = PropertyUtil.getSchemaProperty(context, "type_ProjectVault" );
    String relSubVaults = PropertyUtil.getSchemaProperty(context, "relationship_SubVaults");
    String relProjectVault = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults" );
    DomainObject domainObject    = DomainObject.newInstance(context);

    domainObject.setId(vaultId);
    Pattern relPattern  = new Pattern(relSubVaults);
    relPattern.addPattern(relProjectVault);
    Pattern typePattern = new Pattern(typeProjectVault);
    typePattern.addPattern(typeWorkspace);

    Pattern includeTypePattern = new Pattern(typeWorkspace);

    StringList objSelects = new StringList();
    objSelects.addElement(domainObject.SELECT_ID);
    objSelects.addElement(domainObject.SELECT_NAME);
    //need to include Type as a selectable if we need to filter by Type
    objSelects.addElement(domainObject.SELECT_TYPE);
    objSelects.addElement(domainObject.SELECT_OWNER);
    objSelects.addElement(domainObject.SELECT_CURRENT);

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
    Map map = null;
    while(mapItr.hasNext())
    {
      map = (Map)mapItr.next();
      //workspaceName = (String) map.get(domainObject.SELECT_NAME);
    }
    return map;
  }
  /**
   * getWorkspaceMembersType - method to return the type of the workspace member if Person or Role
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */
  public String getWorkspaceMembersType(Context context, String[] args) throws Exception
  {
      StringBuffer sb = new StringBuffer();
      sb.append("<img border=0 src=../common/images/iconSmallPeople.gif></img>&nbsp;");
      sb.append( EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.WorkSpaceAddMembersDialog.Person"));
      return sb.toString();
  }

  /**
   * getWorkspaceRolesType - method to return the type of Role under Workspace members
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */
  public String getWorkspaceRolesType(Context context, String[] args) throws Exception
  {
      StringBuffer sb = new StringBuffer();
      sb.append("<img border=0 src=../common/images/iconRole.gif></img>&nbsp;");
      sb.append(EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.WorkSpaceAddMembersDialog.Role"));
      return sb.toString();
  }

  /**
   * getRoleTypeonResult - method to return the type of Role
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */
  public Vector getRoleTypeonResult(Context context, String[] args) throws Exception
  {
      Vector roleTypes = new Vector();
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      Iterator itr = objectList.iterator();
      while(itr.hasNext())
      {
          Map map = (Map)itr.next();
          roleTypes.add(XSSUtil.encodeForHTML(context, (String)map.get("TRoleType")));
      }
      return roleTypes;
  }

  /**
   * enableRolesCheckbox - method to enable the roles checkbox in search results
   * @param context the eMatrix <code>Context</code> object
   * @return Vector
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */

  public Vector enableRolesCheckbox(Context context, String[] args) throws Exception
  {
      Vector rolechkbox = new Vector();
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      Iterator itr = objectList.iterator();
      while(itr.hasNext())
      {
          Map map = (Map)itr.next();
          String sLock = (String)map.get("lock");
          if (sLock.equals("true")) {
              rolechkbox.add("false");
          }
          else{
              rolechkbox.add("true");
          }
      }
      return rolechkbox;
  }

  /**
   * deleteWorkspaceMembers - method to delete the members from the workspace
   * @param context the eMatrix <code>Context</code> object
   * @return HashMap
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */

  public HashMap deleteWorkspaceMembers(Context context, String[] args) throws Exception
  {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String objectId = (String) programMap.get("objectId");
      String objId[] = (String[]) programMap.get("objId");
      String sSelectPersonId = (String)programMap.get("sSelectPersonId");
      StringBuffer personBuffer=new StringBuffer();
      Hashtable session = new Hashtable();
      HashMap returnMap =  new HashMap();
      String PROJECT_MEMBER_HASHTABLE = "projectMember.hashtable";
     Meeting meetingObj = new Meeting();
     objId = meetingObj.getTableRowIds(objId);
      if(sSelectPersonId==null || sSelectPersonId.length()==0)
      {
        DomainObject projectMemeber=new DomainObject();

          for(int objIdIndex=0;objIdIndex<objId.length;objIdIndex++)
        {
         //Checks if the array element is Role and skips it
            String id1 = objId[objIdIndex];
            boolean isRole = id1.startsWith("Role:");
            if (isRole)   continue;
            projectMemeber.setId(id1);
            String personId=projectMemeber.getInfo(context,"to["+DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP +"].from.id");
            personBuffer.append(personId+";");

        }
        sSelectPersonId=personBuffer.toString();
      }

      String typeRoute           = PropertyUtil.getSchemaProperty(context, "type_Route");
      String typeWorkspace       = PropertyUtil.getSchemaProperty(context, "type_Project");
      String typeMessage         = PropertyUtil.getSchemaProperty(context, "type_Message");
      String typeThread          = PropertyUtil.getSchemaProperty(context, "type_Thread");
      String typeDocument        = PropertyUtil.getSchemaProperty(context, "type_Document");
      String typeWorkspaceVault  = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
      String type_Event          = PropertyUtil.getSchemaProperty(context, "type_Event");
      String type_Pub_Subscribe  = PropertyUtil.getSchemaProperty(context, "type_PublishSubscribe");

      String sAttrAccessType = PropertyUtil.getSchemaProperty(context, "attribute_AccessType");
      String relMessage      = PropertyUtil.getSchemaProperty(context, "relationship_Message");
      String relThread       = PropertyUtil.getSchemaProperty(context, "relationship_Thread");
      String relReply  =  PropertyUtil.getSchemaProperty(context, "relationship_Reply");
      String relSubVaults  =  PropertyUtil.getSchemaProperty(context, "relationship_SubVaults");
      String relRouteScope  =  PropertyUtil.getSchemaProperty(context, "relationship_RouteScope");
      String relVaultedObjects  =  PropertyUtil.getSchemaProperty(context, "relationship_VaultedDocuments");
      String relWorkspaceVaults  =  PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");
      String relSubscribedItem  =  PropertyUtil.getSchemaProperty(context, "relationship_SubscribedItem");
      String relProjectMembership  =  PropertyUtil.getSchemaProperty(context, "relationship_ProjectMembership");
      String relProjectMembers  =  PropertyUtil.getSchemaProperty(context, "relationship_ProjectMembers");
      boolean reArrangeTasks  = false;

      DomainObject    message        =  DomainObject.newInstance(context);
      Route route                    = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE,DomainConstants.TEAM);
      Document document              = (Document)DomainObject.newInstance(context,DomainConstants.TYPE_DOCUMENT,DomainConstants.TEAM);
      Message messageBean            = (Message)DomainObject.newInstance(context,DomainConstants.TYPE_MESSAGE,DomainConstants.TEAM);
      WorkspaceVault folder          = (WorkspaceVault)DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE_VAULT,DomainConstants.TEAM);
      Workspace workspace            = (Workspace) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE,DomainConstants.TEAM);

      StringList granteeList              = new StringList();
      String conObjectId = "";
      String threadId    = "";
      String netName = "";
      AccessUtil accessUtil    = new AccessUtil();
      BusinessObject conObject = null;
      boolean isRole          = false;
      boolean flagStr          = true;

      DomainObject ObjectGeneral = new DomainObject(objectId);
      String strType = ObjectGeneral.getType(context);

      if(strType != null && strType.equals(DomainObject.TYPE_PROJECT)){

      StringList objSelects = new StringList();
      objSelects.add(DomainObject.SELECT_OWNER);

      StringList ownerList    = new StringList();
      // get all workspaces that the user is a Project-Member
      MapList routeTemplateList = ObjectGeneral.getRelatedObjects(context,
                                                          DomainObject.RELATIONSHIP_ROUTE_TEMPLATES,  //String relPattern
                                                          DomainObject.TYPE_ROUTE_TEMPLATE,           //String typePattern
                                                          objSelects,                                 //StringList objectSelects,
                                                          null,                     //StringList relationshipSelects,
                                                          true,                     //boolean getTo,
                                                          true,                     //boolean getFrom,
                                                          (short)1,                 //short recurseToLevel,
                                                          "",                       //String objectWhere,
                                                          "",                       //String relationshipWhere,
                                                          null,                     //Pattern includeType,
                                                          null,                     //Pattern includeRelationship,
                                                          null);                    //Map includeMap


       Iterator routeTemplateListItr = routeTemplateList.iterator();


       while(routeTemplateListItr.hasNext()){
           Map routeTempMap = (Map)routeTemplateListItr.next();
           String ownerName = (String)routeTempMap.get(DomainObject.SELECT_OWNER);
           if(!ownerList.contains(ownerName)){
             ownerList.add(ownerName);
           }
         }

        StringTokenizer personIdsToken = new StringTokenizer(sSelectPersonId,";",false);
        Person personObj1 = new Person();

        while (personIdsToken.hasMoreTokens()) {
        String personIdStr = personIdsToken.nextToken();

          if(personIdStr != null){
            personObj1.setId(personIdStr);
            String nameStr = personObj1.getName(context);
             if (ownerList.contains(nameStr)){
              netName = netName + nameStr + " ,";
            }
          }
        }
        if (!"".equals(netName))
        {

        flagStr = false;
        }
      }

    if(flagStr)
    {
      try{
        if(!typeMessage.equals(strType)){

          for (int i = 0; i < objId.length; i++) {

            if (typeRoute.equals(strType)){
              String rtState = ObjectGeneral.getInfo(context, DomainObject.SELECT_CURRENT);
              if(rtState != null && "Define".equals(rtState)){
                StringTokenizer tokenizer = new StringTokenizer(objId[i],"~");
                while (tokenizer.hasMoreTokens()) {
                  String rtNode = tokenizer.nextToken();
                  DomainObject rtaskUser = DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE_TASK_USER, DomainConstants.TEAM);
                  rtaskUser.createObject(context,DomainConstants.TYPE_ROUTE_TASK_USER,null,null,DomainObject.POLICY_ROUTE_TASK_USER,context.getVault().getName());
                  if (rtaskUser != null){
                    DomainRelationship.setAttributeValue(context,rtNode,DomainObject.ATTRIBUTE_ROUTE_TASK_USER,"");
                    DomainRelationship.modifyTo(context,rtNode,rtaskUser);
                  }
                }
              }
              else
              {
                StringTokenizer tokenizer = new StringTokenizer(objId[i],"~");
                while (tokenizer.hasMoreTokens()) {
                  String rtNode = tokenizer.nextToken();
                  DomainRelationship.disconnect(context , rtNode);
                }
                reArrangeTasks = true;
              }
           } else {
              isRole = objId[i].startsWith("Role:");
              StringList personList = new StringList();
              StringList subscribedItemList = new StringList();
              String persId = "";
              String personName = "";
              if (isRole){
                objId[i] = objId[i].substring(5);
                workspace.setId(objectId);
                Pattern typePattern1 = new Pattern(type_Event);
                typePattern1.addPattern(type_Pub_Subscribe);
                typePattern1.addPattern(DomainObject.TYPE_PERSON);

                Pattern relPattern1 = new Pattern(DomainObject.RELATIONSHIP_PUBLISH);
                relPattern1.addPattern(DomainObject.RELATIONSHIP_PUBLISH_SUBSCRIBE);
                relPattern1.addPattern(DomainObject.RELATIONSHIP_SUBSCRIBED_PERSON);

                StringList objectSelects = new StringList();

                objectSelects.add(DomainObject.SELECT_ID);
                objectSelects.add(DomainObject.SELECT_TYPE);

                //Retrieve all subscribed persons to all subscribed events from the workspace
                MapList tempPersonList = workspace.getRelatedObjects(context,
                                                              relPattern1.getPattern(),
                                                              typePattern1.getPattern(),
                                                              objectSelects,
                                                              null,
                                                              true,
                                                              true,
                                                              (short)0,
                                                              "",
                                                              "",
                                                              new Pattern(DomainObject.TYPE_PERSON),
                                                              new Pattern(DomainObject.RELATIONSHIP_SUBSCRIBED_PERSON),
                                                              null);

                //Retrieve all project members of the workspace
                StringList personProjMemList = workspace.getInfoList(context,"from["+relProjectMembers+"].to.to["+relProjectMembership+"].from.id");
                if(personProjMemList==null){
                  personProjMemList = new StringList();
                }
                Iterator personItr = tempPersonList.iterator();
                while(personItr.hasNext()){
                  Map personMap = (Map)personItr.next();
                  persId = (String)personMap.get(DomainObject.SELECT_ID);
                  Person person1 = (Person)DomainObject.newInstance(context,persId,DomainConstants.TEAM);
                  //Person belongs to Role selected but is not a Project member of the workspace
                  if(person1.hasRole(context, objId[i]) && !personProjMemList.contains(persId)){
                    personList.add(persId);
                  }
                }
               } else{
                 // check for subscriptions only if "SubscribedItem" Rel exists for the project member
                 DomainObject projectMemberObject = new DomainObject(objId[i]);
                 subscribedItemList = projectMemberObject.getInfoList(context, "to["+relSubscribedItem+"].from.id");
                 persId = projectMemberObject.getInfo(context, "to["+relProjectMembership+"].from.id");
                 personName = projectMemberObject.getInfo(context, "to["+relProjectMembership+"].from.name");
                 personList.add(persId);
                }

                 if(subscribedItemList.size() > 0 || personList.size() > 0){
                   for(int j=0;j<personList.size();j++){
                     // remove all subscriptions that this person has to the given object, also remove from the sub-level objects.
                     // check is done only for Workspace, Folder and Document objects

                    // check for the current object, remove all subscriptions for the user
                    String personId = (String)personList.elementAt(j);
                    workspace.setId(objectId);
                    SubscriptionManager subscriptionMgr = workspace.getSubscriptionManager();
                    subscriptionMgr.removeAllSubscriptions(context, objectId, personId, objId[i]);

                    StringList objectSelects = new StringList();
                    objectSelects.addElement(workspace.SELECT_ID);
                    objectSelects.addElement(workspace.SELECT_TYPE);

                    Pattern typePattern = new Pattern(typeWorkspaceVault);
                    typePattern.addPattern(typeDocument);
                    typePattern.addPattern(typeRoute);
                    typePattern.addPattern(typeThread);
                    typePattern.addPattern(typeMessage);

                    Pattern relPattern = new Pattern(relWorkspaceVaults);
                    relPattern.addPattern(relVaultedObjects);
                    relPattern.addPattern(relRouteScope);
                    relPattern.addPattern(relSubVaults);
                    relPattern.addPattern(relThread);
                    relPattern.addPattern(relMessage);
                    relPattern.addPattern(relReply);

                    //loop thro. and get all the objects down and check for subscriptions
                    MapList objectMapList = workspace.getRelatedObjects
                                           (context,
                                            relPattern.getPattern(),  //relationshipPattern
                                            typePattern.getPattern(), //typePattern
                                            objectSelects,            //objectSelects
                                            new StringList(),         //relationshipSelects
                                            false,                     //getTo
                                            true,                     //getFrom
                                            (short)0,                 //recurseToLevel
                                            "",                       //objectWhere
                                            ""                        //relationshipWhere
                                           );
                    Iterator objectItr = objectMapList.iterator();
                    //iterate thro. the objects and remove the subscriptions for the user
                    while(objectItr.hasNext())
                    {
                      Map objectMap = (Map) objectItr.next();
                      String subscribedId = (String) objectMap.get(workspace.SELECT_ID);
                      String subscribedType = (String) objectMap.get(workspace.SELECT_TYPE);

                      if(subscribedType.equals(typeWorkspaceVault))
                      {
                        folder.setId(subscribedId);
                        subscriptionMgr = folder.getSubscriptionManager();
                        subscriptionMgr.removeAllSubscriptions(context, subscribedId, personId, objId[i]);
                      }
                      else if(subscribedType.equals(typeDocument))
                      {
                        document.setId(subscribedId);
                        subscriptionMgr = document.getSubscriptionManager();
                        subscriptionMgr.removeAllSubscriptions(context, subscribedId, personId, objId[i]);
                      }
                      else if(subscribedType.equals(typeMessage))
                      {
                        messageBean.setId(subscribedId);
                        subscriptionMgr = messageBean.getSubscriptionManager();
                        subscriptionMgr.removeAllSubscriptions(context, subscribedId, personId, objId[i]);
                      }
                      else if(subscribedType.equals(typeRoute))
                      {
                        route.setId(subscribedId);
                        subscriptionMgr = route.getSubscriptionManager();
                        subscriptionMgr.removeAllSubscriptions(context, subscribedId, personId, objId[i]);
                      }
                    }
                  }
                }
                 // subscription checks end
              if (isRole){
                Workspace.removeProjectMember(context,objectId, objId[i], null);
              }
              else{
               //Modified for bug 364187
                  DomainObject dObj = new DomainObject(objId[i]);
                  String personID = dObj.getInfo(context, "to["+relProjectMembership+"].from.id");
                  Workspace.removeProjectMember(context,objectId, personName, objId[i]);
                  Hashtable temp = (Hashtable) session.get(PROJECT_MEMBER_HASHTABLE);
                  if(temp != null && temp.size() > 0)
                  {
                     temp.remove(personID);
                     session.put(PROJECT_MEMBER_HASHTABLE,temp);
                  }
               //Ended
              }

            }
          }

          if(reArrangeTasks)
          {
            //Reordering the Route Sequence Numbers of the Route node relationships
            Route routeObect =(Route)DomainObject.newInstance(context,objectId, DomainConstants.TEAM);
            routeObect.adjustSequenceNumber(context);
          }


          if (typeWorkspace.equals(strType)){
             workspace.setId(objectId);
             SubscriptionManager subscriptionMgr = workspace.getSubscriptionManager();
             subscriptionMgr.publishEvent(context, workspace.EVENT_MEMBER_REMOVED, "");
          }
        } else {
          message.setId(objectId);
          String sAccessType = message.getInfo(context,"to["+relMessage+"].attribute["+sAttrAccessType+"]");

          if("Specific".equals(sAccessType)){
             granteeList = message.getGrantees(context);
         threadId = message.getInfo(context,"to["+relMessage+"].from.id");
          } else {
          // getting the object id connected to Thread type
            conObjectId = message.getInfo(context,"to["+relMessage+"].from.to["+relThread+"].from.id");
            conObject   = new BusinessObject(conObjectId);
            conObject.open(context);
            granteeList = conObject.getGrantees(context);
            conObject.close(context);
            threadId = message.getInfo(context,"to["+relMessage+"].from.id");
         }
         DomainObject thread = new DomainObject(threadId);
         SelectList objectSelects = new SelectList();
         objectSelects.add(DomainConstants.SELECT_ID);
         MapList messageList = thread.getRelatedObjects(context, DomainConstants.RELATIONSHIP_MESSAGE,DomainConstants.TYPE_MESSAGE,objectSelects,null, false, true, (short)1,null,null);
         String relMessageId = message.getInfo(context,"to["+relMessage+"].id");
         DomainRelationship.setAttributeValue(context,relMessageId,sAttrAccessType,"Specific");
         if (granteeList == null){
            granteeList = new StringList();
         }


         //removing the duplicates
         boolean exist=true;

         for (int k=0;k<granteeList.size();k++){
             Object obj = granteeList.elementAt(k);
             exist=true;
             while(exist){
               if(granteeList.indexOf(obj) == granteeList.lastIndexOf(obj) ){
                 exist = false;
               } else{
                 granteeList.remove(obj);
               }
             }
         }

         BusinessObjectList objList = new BusinessObjectList();
         objList.addElement((BusinessObject)message);
         //revoking Access for the members for Thread object
         try {
          BusinessObject.revokeAccessRights(context,objList);
//    Bug No 312917 - Start
        Vector tempVector = new Vector(objId.length);
          for (int i = 0; i < objId.length; i++)
        {
              tempVector.add(objId[i]);
        }
        granteeList.removeAll(tempVector);
//    Bug No 312917 - End

         //granting ADD Access to the members for Thread object
         for (int j = 0;j < granteeList.size(); j++) {
          accessUtil.setAdd((String)granteeList.elementAt(j));
         }
          DomainObject messages = new DomainObject();
          String messageId = "";
          if (accessUtil.getAccessList().size() > 0){
             for(int i=0; i< messageList.size();i++)
              {
                 Hashtable map =(Hashtable) messageList.get(i);
                 messageId = map.get(DomainConstants.SELECT_ID).toString();
                 messages.setId(messageId);
//    Bug No 312917 - Start
           emxGrantAccess grantAccess = new emxGrantAccess(messages);
                 if(messageId.equals(objectId))
                 {
                    grantAccess.grantAccess(context, accessUtil);
                 }

             }
//    Bug No 312917 - End
          }
         } catch(Exception exp) {
           exp.printStackTrace();
         }
       }
      }catch(Exception e){
          e.printStackTrace();
      }
     }
    returnMap.put("netName", netName);
    returnMap.put("sSelectPersonId", sSelectPersonId);
    return returnMap;
  }

  @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
  public StringList getWorkspaceChangeOwnerInclusionIDs(Context context, String[] args) throws FrameworkException {
      try {
          Map programMap = (Map) JPO.unpackArgs(args);
          String workspaceId = (String) programMap.get("objectId");
          Workspace ws = (Workspace)DomainObject.newInstance(context, workspaceId, TEAM);
          MapList mapList = ws.getRelatedObjects(context,
                  RELATIONSHIP_WORKSPACE_MEMBER,                  //String relPattern
                  TYPE_PERSON,                 //String typePattern
                  new StringList(DomainConstants.SELECT_ID),                          //StringList objectSelects,
                  null,                                     //StringList relationshipSelects,
                  false,                                     //boolean getTo,
                  true,                                     //boolean getFrom,
                  (short)1,                                 //short recurseToLevel,
                  null,                             //String objectWhere,
                  EMPTY_STRING,                             //String relationshipWhere,
                  0,
                  null,                                    //Pattern includeType,
                  null,                                     //Pattern includeRelationship,
                  null);                                    //Map includeMap

          StringList ids = new StringList(mapList.size());
          for (int i = 0; i < mapList.size(); i++) {
              Map personMap = (Map) mapList.get(i);
              ids.add((String)personMap.get(DomainConstants.SELECT_ID));
          }
          return ids;
      } catch (Exception e) {
          throw new FrameworkException(e);
      }
  }

  /**
   * updateWorkspaceAndRelatedProjectMembersOwner - update the owner of the workspace along with related project member.
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   * @return void
   * @throws Exception if the operation fails
   * @since R211
   */
  public void updateWorkspaceAndRelatedProjectMembersOwner(Context context,String[] args) throws MatrixException
  {
      try{
      Map programMap = (Map) JPO.unpackArgs(args);
      HashMap paramMap   = (HashMap)programMap.get("paramMap");
      String workspaceId = (String) paramMap.get("objectId");
      String newOwner = (String)paramMap.get("New Value");
      DomainObject workspaceObject = DomainObject.newInstance(context,workspaceId);
          String oldOwner = workspaceObject.getInfo(context, SELECT_OWNER);
      workspaceObject.setOwner(context, newOwner);


          DomainAccess.createObjectOwnership(context, workspaceId, com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context, newOwner), "Full", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
          DomainAccess.createObjectOwnership(context, workspaceId, com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context, oldOwner), "Read", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP, true, true);
      }
      catch(Exception e){
          throw new FrameworkException(e);
      }
  }
  @com.matrixone.apps.framework.ui.CreateProcessCallable
  public Map createWorkspaceProcess(Context context,String[] args) throws MatrixException
  {
      try{
          Map programMap = (Map) JPO.unpackArgs(args);
          String strWSName = (String) programMap.get("Name");
          String strWSDes = (String) programMap.get("Description");
          String sTemplateId = (String) programMap.get("TemplateOID");
          String strBuyerDeskId = (String) programMap.get("txtBuyerDeskId");
          String strCompanyVault = context.getVault().getName();
          Workspace WorkspaceObj = (Workspace) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE,DomainConstants.TEAM);
          String strProjectType  = WorkspaceObj.TYPE_PROJECT;
          String MAX_LENGTH = FrameworkProperties.getProperty(context,"emxComponents.MAX_FIELD_LENGTH");
          String objectId  = "";
          String langStr = context.getLocale().getLanguage();
          HashMap retMap = new HashMap();

          if(strWSName.length()>(Integer.parseInt(MAX_LENGTH)))
            {
                String strLengthMessage = UINavigatorUtil.getI18nString("emxTeamCentral.NameLength.Message","emxTeamCentralStringResource",langStr);
                String strChars = UINavigatorUtil.getI18nString("emxTeamCentral.NameLength.NumChars","emxTeamCentralStringResource",langStr);
                retMap.put("ErrorMessage",strLengthMessage + MAX_LENGTH + " " + strChars);
                return retMap;
            }

          ContextUtil.pushContext(context, null, null, null);

          boolean isWorkspaceExists = (boolean) Workspace.isWorkspaceExists(context, FrameworkUtil.getVaultNames(context, false, true).toString(), strWSName);

          ContextUtil.popContext(context);
          if(!isWorkspaceExists){
          objectId = TeamUtil.autoRevision(context, HttpServletRequest , strProjectType, strWSName ,WorkspaceObj.POLICY_PROJECT, strCompanyVault);
          WorkspaceObj.setId(objectId);
          WorkspaceObj.open(context);
          WorkspaceObj.setDescription(context,strWSDes);
          WorkspaceObj.update(context);
          //WorkspaceObj.connectWorkspaceMember(context,com.matrixone.apps.common.Person.getPerson(context),"Project Lead","Yes","Yes",strCompanyVault);
          DomainAccess.createObjectOwnership(context, objectId, com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context), "Full", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
          if(!UIUtil.isNullOrEmpty(sTemplateId)) {
              if(sTemplateId.startsWith("B")){
                  sTemplateId = sTemplateId.substring(1);
              }
              DomainObject workspaceTemplateObj = DomainObject.newInstance(context,sTemplateId,DomainConstants.TEAM);
              WorkspaceObj.connectWorkspaceTemplate(context,workspaceTemplateObj,true,true,strCompanyVault);
            }
          if ( strBuyerDeskId != null && !"".equals(strBuyerDeskId) ) {
              WorkspaceObj.addBuyerDesk(context, strBuyerDeskId);
              WorkspaceObj.addBuyerDeskPersons(context,strCompanyVault,strBuyerDeskId);
            }
          }else
          {
              retMap.put("ErrorMessage", strProjectType + " " + strWSName + " " + i18nNow.getI18nString("emxTeamCentral.Common.AlreadyExists","emxTeamCentralStringResource",langStr));
              return retMap;
          }
          retMap.put("id", objectId);
          return retMap;

      }
      catch(Exception e){
          throw new FrameworkException(e);
      }
  }

  /**
   * Grant access to new Workspace member.
   *
   * @param context the eMatrix Context object
   * @param args holds id of Project Member object
   * @return void
   * @throws Exception if the operation fails
   * @since TC V3
   */
  public void connectMember(matrix.db.Context context, String[] args) throws Exception
  {
      try
      {
          String workspaceId = args[0];
          String project = args[1];
          String result = MqlUtil.mqlCommand(context, "list role $1 select $2 $3 dump $4", true, project, "person", "parent", "|");
          StringList resultList = StringUtil.split(result, "|");
          String personId = "";
          if( resultList.size() == 2 && "User Projects".equals(resultList.get(1)) )
          {
              personId = PersonUtil.getPersonObjectID(context, (String)resultList.get(0));
              if( personId != null)
              {
                  String memberSelects = "from["+ DomainConstants.RELATIONSHIP_WORKSPACE_MEMBER +"|to.id==" + personId +"].id";
                  
                  String relId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", true, workspaceId, memberSelects);
                  
                  if( relId == null || "".equals(relId))
                  {
                      DomainRelationship.connect(context, workspaceId, DomainConstants.RELATIONSHIP_WORKSPACE_MEMBER, personId, true);
                  }
              }
          }
          String rpe = PropertyUtil.getGlobalRPEValue(context, "RPE_MEMBER_ADDED_REMOVED");
          if("true".equalsIgnoreCase(rpe))
          {
              Workspace workspace = new Workspace(workspaceId);
              SubscriptionManager subscriptionMgr = workspace.getSubscriptionManager();
              subscriptionMgr.publishEvent(context, workspace.EVENT_MEMBER_ADDED, personId);
          }

      } catch (Exception e) {
          throw e;
      }
  }
  public void disconnectMember(matrix.db.Context context, String[] args) throws Exception
  {
      try
      {
          String workspaceId = args[0];
          String project = args[1];
          String org = args[2];
          String comment = args[3];
          String personId = "";
          String result = MqlUtil.mqlCommand(context, "list role $1 select $2 $3 dump $4", true, project, "person", "parent", "|");
          StringList resultList = StringUtil.split(result, "|");
          if( resultList.size() == 2 && "User Projects".equals(resultList.get(1)) )
          {
              String personName = (String)resultList.get(0);
              personId = PersonUtil.getPersonObjectID(context, personName);
              project = personName +"_PRJ";
              org = null;
              if( personId != null)
              {
            	  String memberSelects = "from["+ DomainConstants.RELATIONSHIP_WORKSPACE_MEMBER +"|to.id==" + personId +"].id";
                  String relId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", true, workspaceId, memberSelects);
                  if( relId != null && !"".equals(relId))
                  {
                      DomainRelationship.disconnect(context, relId);
                  }
              }
          }
          String rpe = PropertyUtil.getGlobalRPEValue(context, "RPE_MEMBER_ADDED_REMOVED");
          if("true".equalsIgnoreCase(rpe))
          {
              Workspace workspace = new Workspace(workspaceId);
              SubscriptionManager subscriptionMgr = workspace.getSubscriptionManager();
              subscriptionMgr.publishEvent(context, workspace.EVENT_MEMBER_REMOVED, personId);
              DomainObject domainObject = new DomainObject(workspaceId);
              StringList selects = new StringList(1);
              selects.add(SELECT_ID);
              ContextUtil.pushContext(context, null, null, null);
              MapList folders = getWorkspaceVaults(context, domainObject, selects, 0);
              Iterator itr = folders.iterator();
              while(itr.hasNext())
              {
                  Map m = (Map)itr.next();
                  String oid = (String)m.get(SELECT_ID);
                  DomainAccess.deleteObjectOwnership(context, oid, org, project, comment);
              }
              ContextUtil.popContext(context);
          }

      } catch (Exception e) {
          throw e;
      }
  }
  public int workspaceDeleteCheck(Context context, String []args) throws Exception {
	  String workspaceId  = args[0];

	  if(!TeamUtil.canProjectDeleted(context, null, workspaceId)){
		  String sMsg = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource", new Locale(context.getSession().getLanguage()),"emxTeamCentral.WorkspaceDelete.Msg");
		  emxContextUtil_mxJPO.mqlNotice(context,sMsg);
		  return 1;
	  }
	  return 0;
  }

	public void preventDuplicateWorkspaceName(Context context, String[] args) throws Exception
	{
		String oid = args[0];
		String newName = args[1];
		String vault = args[2];

		if(vault == null || "".equals(vault) || "null".equals(vault))
		{
			DomainObject object = new DomainObject(oid);
			vault = object.getInfo(context, SELECT_VAULT);
		}
		boolean workspaceExists  = false;
		ContextUtil.pushContext(context);
		try
		{
			workspaceExists = Workspace.isWorkspaceExists(context, vault, newName);
		} catch (Exception ex) {
			throw ex;
		}
		finally
		{
			ContextUtil.popContext(context);
		}
		if( workspaceExists )
		{
			String error = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource",
					context.getLocale(), "emxTeamCentral.Workspace.AlreadyExists");
			throw new Exception(error);
		}
	}
}

