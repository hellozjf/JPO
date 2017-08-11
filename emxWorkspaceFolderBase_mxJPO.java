/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 */
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessList;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.ExpansionWithSelect;
import matrix.db.JPO;
import matrix.util.List;
import matrix.db.RelationshipType;
import matrix.db.RelationshipWithSelectItr;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.SubscriptionManager;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.VaultedObjectsAccessUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxWorkspaceFolderBase_mxJPO extends emxWorkspaceConstants_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxWorkspaceFolderBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
        sbProjectMember.append("name");       	
        sbAttrCreateFolder.append(attributeBracket);
        sbAttrCreateFolder.append(DomainObject.ATTRIBUTE_CREATE_FOLDER);
        sbAttrCreateFolder.append("].value");
        sbAttrProjectAccess.append(attributeBracket);
        sbAttrProjectAccess.append(DomainObject.ATTRIBUTE_PROJECT_ACCESS);
        sbAttrProjectAccess.append("].value");
        sbContentAttr.append(attributeBracket);
        sbContentAttr.append(DomainObject.ATTRIBUTE_COUNT);
        sbContentAttr.append(closeBracket);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
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

    /**
    * Grant access to Workspace Folder Contents for usr.
    *
    * @param context the eMatrix Context object
    * @param holds grantee and grantor name
    * @return void
    * @throws Exception if the operation fails
    * @since TC V3
    * @grade 0
    */
    public void grantWorkspaceFolderAccess(matrix.db.Context context, String[] args) throws Exception
    {
        // Restore the previous context
        ContextUtil.restoreContext(context);
        String sGrantee = args[0];

        // set the grantor as Context user, sine macro doesnot filled in properly
        //String sGrantor = args[1];
        String sGrantor = context.getUser();
        // Added for Bug 372630 starts
        // push the context for Super user to turn off trigger.
        ContextUtil.pushContext(context, null, null, null);
        // Turn off all triggers
        MqlUtil.mqlCommand(context, "trigger $1", true,"off");
        // Added for Bug 372630 ends

      try {

        // Construct grantee list
        StringList sGranteeList = new StringList();
        sGranteeList.add(sGrantee);

        // select statement
        StringList listObjectSelect = new StringList();
        listObjectSelect.add(SELECT_ID);

        // Constuct Type Pattern
        Pattern typePattern = new Pattern(typeProjectVault);
        typePattern.addPattern(typeDocument);
        typePattern.addPattern(typePackage);
        typePattern.addPattern(typeRTSQuotation);
        typePattern.addPattern(typeRTS);

        // Constuct Rel Pattern
        Pattern relPattern = new Pattern(relSubVaults);
        relPattern.addPattern(relVaultedObjects);
        relPattern.addPattern(relVaultedObjectsRev2);
        
        boolean isWAG = sGrantor.equals(AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME);
        boolean isWLG = sGrantor.equals(AEF_WORKSPACE_LEAD_GRANTOR_USERNAME);

        if (isWLG)
        {
            listObjectSelect.add(SELECT_TYPE);
            typePattern.addPattern(typeRoute);
            relPattern.addPattern(relRouteScope );
        }
        
        MapList mapObjectList = getObjects( context,
                                            relPattern.getPattern(),
                                            typePattern.getPattern(),
                                            false,
                                            true,
                                            (short)1,
                                            listObjectSelect,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            false);
        
        StringList contentIds = null;

        // get the Access for the current grantor for the passsed grantee on the current object.
        Access access = getAccessForGranteeGrantor(context, sGrantee, sGrantor);

        //pushContextForGrantor(context,sGrantor);
        BusinessObjectList busList = new BusinessObjectList();
        BusinessObjectList busRouteList = new BusinessObjectList();

            // If retrived list size > 0, then construct the bo list and grant access
            if (mapObjectList.size() != 0)
            {
                Iterator itrObjects = mapObjectList.iterator();
                if(isWLG)
                {
                    contentIds  = new StringList(mapObjectList.size());
                    while(itrObjects.hasNext())
                    {
                        Map mapObjects = (Map) itrObjects.next();
                        // add Route objects to seperate BO list to grant READ access to Project Leador.
                        if (((String)mapObjects.get(SELECT_TYPE)).equals(typeRoute))
                        {
                            busRouteList.add(new BusinessObject((String)mapObjects.get(SELECT_ID)) );
                        }
                        else
                        {
                            contentIds.add((String)mapObjects.get(SELECT_ID));
                        }
                    }
                    // If passed access object has none access then revoke the access for Project Leador from
                    // Workspace Vault Routes else grant READ access
                    if (access.hasNoAccess())
                    {
                        matrix.db.BusinessObject.revokeAccessRights(context, busRouteList, sGranteeList);
                    }
                    else
                    {
                        // Grant the READ access for the Routes to Project Lead.
                        AccessUtil accessUtil = new AccessUtil();
                        accessUtil.setRead(sGrantee);
                        matrix.db.BusinessObject.grantAccessRights(context, busRouteList,
                                        (Access)((accessUtil.getAccessList()).elementAt(0)));
                    }
                } else if(isWAG) {
                    contentIds  = new StringList(mapObjectList.size());
                    while(itrObjects.hasNext())
                    {
                        contentIds.add( (String)((Map)itrObjects.next()).get(SELECT_ID));
                    }
                }
                else
                {
                    while(itrObjects.hasNext())
                    {
                        busList.add(new BusinessObject((String)((Map)itrObjects.next()).get(SELECT_ID) ));
                    }
                }
                if(isWAG || isWLG) {
                    if(contentIds.size() > 0) {
                        String[] grantees = new String[] {sGrantee};
                        new VaultedObjectsAccessUtil().updateVaultedObjectsAccess(context,
                                (String[]) contentIds.toArray(new String[]{}), 
                                isWAG ? grantees : new String[]{}, 
                                isWLG ? grantees : new String[]{},
                                VaultedObjectsAccessUtil.UPDATE_ACCESS);
                    }                    
                } else {
                    matrix.db.BusinessObject.grantAccessRights(context, busList, access);
                }
            }
            // if Access granted is empty then Revoke the access from grantee for current object
            if (access.hasNoAccess())
            {
                revokeAccessGrantorGrantee(context, sGrantor, sGrantee);
            }
        }
        catch (Exception exp)
        {
            //popContext(context);
            throw exp;
        }
//      Added for Bug 372630 starts
        finally
        {
            MqlUtil.mqlCommand(context, "trigger $1", true,"on");
            ContextUtil.popContext(context);
        }
        // Added for Bug 372630 ends
        //popContext(context);
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getWorkspaceFolders(Context context,String args[]) throws Exception
    {
		String RELATIONSHIP_LINKED_FOLDERS = PropertyUtil.getSchemaProperty(context, "relationship_LinkedFolders");
		HashMap programMap         = (HashMap) JPO.unpackArgs(args);
		String objectId            = (String) programMap.get("objectId");
		Workspace workspace = (Workspace)DomainObject.newInstance(context,objectId,DomainConstants.TEAM);
		MapList totalresultList  = null;

		StringList objectSelects = new StringList(4);
		objectSelects.add(DomainObject.SELECT_ID);
		objectSelects.add(DomainObject.SELECT_NAME);
		objectSelects.add(DomainObject.SELECT_OWNER);
		objectSelects.add(sbContentAttr.toString());
		StringList relSelects = new StringList(4);
		relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
      	totalresultList =  workspace.getRelatedObjects(context,
													  DomainObject.RELATIONSHIP_WORKSPACE_VAULTS+","+RELATIONSHIP_LINKED_FOLDERS,
                                                      DomainObject.TYPE_WORKSPACE_VAULT,
                                                      objectSelects,
													  relSelects,
                                                      false,
                                                      true,
                                                      (short)1,
                                                      null,
                                                      null);
     	return totalresultList;
    }

	@com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getSubFolders(Context context,String args[]) throws Exception
    {
		String RELATIONSHIP_LINKED_FOLDERS = PropertyUtil.getSchemaProperty(context, "relationship_LinkedFolders");
		HashMap programMap         = (HashMap) JPO.unpackArgs(args);
		String objectId            = (String) programMap.get("objectId");
		WorkspaceVault workspaceVault = (WorkspaceVault)DomainObject.newInstance(context,objectId);
		MapList totalresultList  = null;
		StringList objectSelects = new StringList(4);
		objectSelects.add(DomainObject.SELECT_ID);
		objectSelects.add(DomainObject.SELECT_NAME);
		objectSelects.add(DomainObject.SELECT_OWNER);
		objectSelects.add(sbContentAttr.toString());
		String sWhere   = "owner == '" + context.getUser() + "' || current.access[read] == true";
		StringList relSelects = new StringList(4);
		relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);

		totalresultList =  workspaceVault.getRelatedObjects(context,
												DomainObject.RELATIONSHIP_SUBVAULTS+","+RELATIONSHIP_LINKED_FOLDERS,
												DomainObject.TYPE_PROJECT_VAULT,
												objectSelects,
												relSelects,
												false,
												true,
												(short)1,
												sWhere,
												null);
		return totalresultList;
    }
   public Vector showContent(Context context, String[] args)
     throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector vecContent = new Vector(objList.size());
            MapList busObjwsl = null;
            // Get the list of Selectables
            StringList strList = new StringList(1);
            strList.addElement(DomainObject.SELECT_OWNER);
            if ( objList != null)
            {
               for (int i = 0; i < objList.size(); i++)
               {
                String content=(String)(((Map)objList.get(i)).get(sbContentAttr.toString()));
                vecContent.add(content);
               }
            }
            return vecContent;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
     public Vector showOwner(Context context, String[] args)
     throws Exception
    {
        try
        {
            Vector vecOwner = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
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
               busObjwsl = DomainObject.getInfo(context,objIdArray,strList);
               for (int i = 0; i < objList.size(); i++)
               {
                String strOwner=com.matrixone.apps.common.Person.getDisplayName(context,(String)((Map)busObjwsl.get(i)).get(DomainObject.SELECT_OWNER));
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
           HashMap paramList = (HashMap)programMap.get("paramList");
           String objectId            = (String) paramList.get("objectId");
           MapList objList = (MapList)programMap.get("objectList");
           Vector vecCheckBox = new Vector(objList.size());
           boolean check= false;
           if(checkAccess(context,objectId)){
             check = true;
           }
           if ( objList != null)
           {
               for (int i = 0; i < objList.size(); i++)
               {
                 if(check)
                   vecCheckBox.add("true");
                 else
                   vecCheckBox.add("false");
               }
           }
           return vecCheckBox;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
    private boolean checkAccess(Context context, String objectId) throws Exception
    {
          try{
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
            boolean checkBox = false;
            Workspace workspace = null;
            String workspaceId = "";
            StringList sList = new StringList();
            sList.add(sbAttrCreateFolder.toString());
            sList.add(sbAttrProjectAccess.toString());
            sList.add(DomainObject.SELECT_OWNER);
            sList.add(sbProjectMember.toString());
            StringBuffer sbWhere = new StringBuffer(sbProjectMember.length()+24);
            sbWhere.append(sbProjectMember.toString());
            sbWhere.append("=='");
            sbWhere.append(person.getName(context));
            sbWhere.append("'");
            DomainObject dom = new DomainObject(objectId);
            if(!dom.getType(context).equals("Workspace"))
            {
              workspaceId = getProjectId(context,objectId);
              workspace = (Workspace)DomainObject.newInstance(context,workspaceId,DomainConstants.TEAM);
            }
            else
            {
              workspace = (Workspace)DomainObject.newInstance(context,objectId,DomainConstants.TEAM);
            }
            Access access = workspace.getAccessMask(context);
            MapList memberList = workspace.getWorkspaceMembers(context,sList,sbWhere.toString());
            Iterator memberItr     = memberList.iterator();
            String sCreateFolder   = "";
            String sProjectAccess  = "";
            String sOwner          = "";
            Map memberMap          = null;
            while(memberItr.hasNext()) {
               memberMap       = (Map)memberItr.next();
               sCreateFolder   = (String)memberMap.get(sbAttrCreateFolder.toString());
               sProjectAccess  = (String)memberMap.get(sbAttrProjectAccess.toString());
               sOwner          = (String)memberMap.get(DomainObject.SELECT_OWNER);
             }
             if(sOwner.equals(person.getName(context))
                     || "Project Lead".equals(sProjectAccess)
                     || ("Yes".equals(sCreateFolder) && (AccessUtil.hasRemoveAccess(access)|| AccessUtil.hasAddRemoveAccess(access) || AccessUtil.hasAddAccess(access)))) {
              return true;
             }
             else
              return false;
            }catch(Exception e){throw e;}
    }
    public boolean linkAccessCheck(Context context, String []args) throws Exception
    {
       try{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId            = (String) programMap.get("objectId");
        return checkAccess(context,objectId);
       }catch(Exception e){throw e;}
    }


    public String getProjectId(matrix.db.Context context, String[] args) throws Exception {
   	 String[] objectId=JPO.unpackArgs(args);
   	 return getProjectId(context,objectId[0]);
   	
   }
    static public String getProjectId(matrix.db.Context context, String folderId) throws MatrixException {
      String strProjectVault = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults" );
      String strProjectType  = PropertyUtil.getSchemaProperty(context,"type_Project");
      String strPersonalWorkspaceType  = PropertyUtil.getSchemaProperty(context,"type_PersonalWorkspace");
      String strProjectSpaceType  = PropertyUtil.getSchemaProperty(context,"type_ProjectSpace");
      String strProjectTemplateType  = PropertyUtil.getSchemaProperty(context,"type_ProjectTemplate");
      String strProjectConceptType  = PropertyUtil.getSchemaProperty(context,"type_ProjectConcept");
      String strSubmissionType  = PropertyUtil.getSchemaProperty(context,"type_Submission");
      String strSubmissionTemplateType  = PropertyUtil.getSchemaProperty(context,"type_SubmissionTemplate");

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
      typePattern.addPattern(strPersonalWorkspaceType);
      typePattern.addPattern(strProjectSpaceType); 
      typePattern.addPattern(strProjectTemplateType); 
      typePattern.addPattern(strProjectConceptType); 
      typePattern.addPattern(strSubmissionType); 
      typePattern.addPattern(strSubmissionTemplateType); 
      

      Pattern includeTypePattern = new Pattern(strProjectType);
	  includeTypePattern.addPattern(strPersonalWorkspaceType);
	  includeTypePattern.addPattern(strProjectSpaceType); 
	  includeTypePattern.addPattern(strProjectTemplateType); 
	  includeTypePattern.addPattern(strProjectConceptType); 
	  includeTypePattern.addPattern(strSubmissionType); 
	  includeTypePattern.addPattern(strSubmissionTemplateType); 
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
      domainObject.close(context);
      Iterator mapItr = mapList.iterator();
      while(mapItr.hasNext())
      {
        Map map = (Map)mapItr.next();
        projectId = (String) map.get(domainObject.SELECT_ID);
      }
      return projectId;
    }

    public boolean showEditAndSubscriptionCommand(matrix.db.Context context,String[] args)throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        String folderId=(String)programMap.get("objectId");
        String workspaceId=(String)programMap.get("workspaceId");

        // Bug fix-330176 . Added below condition to get workspace id.
        if(workspaceId == null || "".equals(workspaceId) || "null".equals(workspaceId))
        {
            workspaceId = getProjectId(context,folderId);
        }

        DomainObject workspaceFolder=new DomainObject(folderId);
        DomainObject workspace=new DomainObject(workspaceId);
        String loggedInPerson=context.getUser().toString();
        if(loggedInPerson.equals(workspace.getOwner(context).toString()) || loggedInPerson.equals(workspaceFolder.getOwner(context).toString()))
        {
            return true;
        }
        else
        {
            return false;
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

  public Object getAllWorkspaceFolderAccess(Context context,String[] args) throws Exception
  {

      return constructWorkspaceFolderAccessList(context,args,true,true,true);

  }


  /**
   * Returns "BuyerDesk" Filter Workspace Access List.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

  public Object getBuyerDeskWorkspaceFolderAccess(Context context,String[] args) throws Exception
  {
      return constructWorkspaceFolderAccessList(context,args,false,true,false);
  }

     /**
   * Returns "Person" Filter Workspace Access List.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

  public Object getPersonsWorkspaceFolderAccess(Context context,String[] args) throws Exception
  {
      return constructWorkspaceFolderAccessList(context,args,true,false,false);
  }

  /**
   * Returns "Roles" Filter Workspace Access List.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Object getRolesWorkspaceFolderAccess(Context context,String[] args) throws Exception
  {
      return constructWorkspaceFolderAccessList(context,args,false,false,true);
  }


  /**
   * The main method which all filter access method calls.
   * Returns Workspace Folder Access List Based on the parameters.
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return MapList
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */

 public Object constructWorkspaceFolderAccessList(Context context,String[] args,boolean bPersonFlag,boolean bBuyerFlag,boolean bRoleFlag) throws Exception
  {

      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      
      String language = (String) programMap.get("languageStr");
      language = language == null ? context.getLocale().getLanguage() : language;
      
     com.matrixone.apps.common.Person PersonObj = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

      DomainObject BaseObject     = DomainObject.newInstance(context);
      DomainObject Workspace      = DomainObject.newInstance(context);
      DomainObject parentObj      = DomainObject.newInstance(context);

      String sAttrCreateRoute     = DomainObject.ATTRIBUTE_CREATE_ROUTE;
      String sAttrProjectAccess   = DomainObject.ATTRIBUTE_PROJECT_ACCESS;
      String sAttrCreateFolder    = DomainObject.ATTRIBUTE_CREATE_FOLDER;
      String relRouteNode       = DomainObject.RELATIONSHIP_ROUTE_NODE;

      String jsTreeID             = (String)programMap.get("jsTreeID");
      String objectId             = (String)programMap.get("objectId");
      String workspaceId          = (String)programMap.get("workspaceId");
      String folderId             = (String)programMap.get("folderId");
      String WSIdList             = (String)programMap.get("WSIdList");
      String sPersonName          = "";
      String sOrgName             = "";
      String sProjectMemberId     = "";
      String sPersonId            = "";
      String sProjectLead         = "";
      String sCreateRoute         = "";
      String sCreateFolder        = "";
      String sOrgId               = "";
      String sRouteStatus         = "";
      String sParentId            = "";
      String sDisplayPersonName   = "";


      HashMap i18nMap     = new HashMap();
      i18nMap.put("Read","Read");
      i18nMap.put("Read Write","ReadWrite");
      i18nMap.put("Add","Add");
      i18nMap.put("Remove","Remove");
      i18nMap.put("Add Remove","AddRemove");
      i18nMap.put("None","None");
      BaseObject.setId(objectId);

      StringList strSel = new StringList();
      strSel.add(DomainConstants.SELECT_TYPE);
      strSel.add(DomainConstants.SELECT_OWNER);
      MapList AccessMapList=new MapList();
      Map infoMap         = BaseObject.getInfo(context , strSel);
      String sTypeName    = (String)infoMap.get(DomainConstants.SELECT_TYPE);
      String sOwner       = (String)infoMap.get(DomainConstants.SELECT_OWNER);

      if(sTypeName.equals(DomainConstants.TYPE_WORKSPACE))
      {
        Workspace = BaseObject;
      }
      else
      {
        if(workspaceId == null || "".equals(workspaceId) || "null".equals(workspaceId))
        {
          workspaceId     = getProjectId(context,objectId);
        }
        Workspace.setId(workspaceId);
      }
      if(sTypeName.equals(DomainConstants.TYPE_PROJECT_VAULT) )
      {
        sParentId     = BaseObject.getInfo(context,"to[" + BaseObject.RELATIONSHIP_SUBVAULTS + "].from.id");
        if(sParentId == null || sParentId.equals(""))
        {
          sParentId   = BaseObject.getInfo(context,"to[" + BaseObject.RELATIONSHIP_WORKSPACE_VAULTS+ "].from.id");
        }
        parentObj.setId(sParentId);
      }
      else if (!sTypeName.equals(BaseObject.TYPE_WORKSPACE))
      {
        parentObj.setId(folderId);
      }

      Map map                     = null;
      HashMap hashMap             = null;
      String strAttr = "attribute[";
      String strVal  = "].value";
      String strFrom = "].from.";
      String strTo   = "to[";

      StringBuffer sAttSelProjectAccessTemp =  new StringBuffer(strAttr);
      sAttSelProjectAccessTemp.append(DomainConstants.ATTRIBUTE_PROJECT_ACCESS);
      sAttSelProjectAccessTemp.append(strVal);
      String sAttSelProjectAccess = sAttSelProjectAccessTemp.toString();

      StringBuffer sAttSelCreateRouteTemp   = new StringBuffer(strAttr);
      sAttSelCreateRouteTemp.append(sAttrCreateRoute);
      sAttSelCreateRouteTemp.append(strVal);
      String sAttSelCreateRoute = sAttSelCreateRouteTemp.toString();

      StringBuffer sAttSelCreateFolderTemp  = new StringBuffer(strAttr);
      sAttSelCreateFolderTemp.append(sAttrCreateFolder);
      sAttSelCreateFolderTemp.append(strVal);
      String sAttSelCreateFolder = sAttSelCreateFolderTemp.toString();

      StringBuffer sSelOrgNameTemp          = new StringBuffer(strTo);
      sSelOrgNameTemp.append(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
      sSelOrgNameTemp.append("].from.to[");
      sSelOrgNameTemp.append(DomainConstants.RELATIONSHIP_EMPLOYEE);
      sSelOrgNameTemp.append("].from.name");
      String sSelOrgName = sSelOrgNameTemp.toString();

      StringBuffer sSelOrgIdTemp            = new StringBuffer(strTo);
      sSelOrgIdTemp.append(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
      sSelOrgIdTemp.append("].from.to[");
      sSelOrgIdTemp.append(DomainConstants.RELATIONSHIP_EMPLOYEE);
      sSelOrgIdTemp.append("].from.id");
      String sSelOrgId = sSelOrgIdTemp.toString();

      String sSelProjectMemberId  = DomainConstants.SELECT_ID;
      StringBuffer sSelPersonIdTemp         = new StringBuffer(strTo);
      sSelPersonIdTemp.append(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
      sSelPersonIdTemp.append("].from.id");
      String sSelPersonId = sSelPersonIdTemp.toString();

      StringBuffer sSelPersonLastNameTemp   = new StringBuffer(strTo);
      sSelPersonLastNameTemp.append(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
      sSelPersonLastNameTemp.append(strFrom);
      sSelPersonLastNameTemp.append(com.matrixone.apps.common.Person.SELECT_LAST_NAME);
      sSelPersonLastNameTemp.append(".value");
      String sSelPersonLastName = sSelPersonLastNameTemp.toString();

      StringBuffer sSelPersonFirstNameTemp  = new StringBuffer(strTo);
      sSelPersonFirstNameTemp.append(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
      sSelPersonFirstNameTemp.append(strFrom);
      sSelPersonFirstNameTemp.append(com.matrixone.apps.common.Person.SELECT_FIRST_NAME);
      sSelPersonFirstNameTemp.append(".value");

      String sSelPersonFirstName = sSelPersonFirstNameTemp.toString();
      StringBuffer sSelPersonNameTemp       = new StringBuffer(strTo);
      sSelPersonNameTemp.append(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
      sSelPersonNameTemp.append("].from.name");

      String sSelPersonName = sSelPersonNameTemp.toString();
      StringBuffer sAssignBuyerDeskTemp     = new StringBuffer(strTo);
      sAssignBuyerDeskTemp.append(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
      sAssignBuyerDeskTemp.append("].from.from[");
      sAssignBuyerDeskTemp.append(DomainConstants.RELATIONSHIP_ASSIGNED_BUYER);
      sAssignBuyerDeskTemp.append("].to.id");
      String sAssignBuyerDesk = sAssignBuyerDeskTemp.toString();

      StringBuffer sStateTemp               = new StringBuffer(strTo);
      sStateTemp.append(DomainConstants.RELATIONSHIP_PROJECT_MEMBERSHIP);
      sStateTemp.append(strFrom);
      sStateTemp.append(DomainConstants.SELECT_CURRENT);
      String sState = sStateTemp.toString();

      StringBuffer sBuyerDeskTemp           = new StringBuffer("from[");
      sBuyerDeskTemp.append(DomainConstants.RELATIONSHIP_WORKSPACE_BUYER_DESK);
      sBuyerDeskTemp.append("].to.id");
      String sBuyerDesk = sBuyerDeskTemp.toString();

      String sBuyerDeskId         = Workspace.getInfo(context,sBuyerDesk);

      AccessList baseAccessList = BaseObject.getAccessForGrantor(context, AccessUtil.WORKSPACE_ACCESS_GRANTOR);
      AccessList parentAccessList = parentObj.getAccessForGrantor(context, AccessUtil.WORKSPACE_ACCESS_GRANTOR);
      StringList objectSelects = new StringList();
      objectSelects.addElement(sAttSelProjectAccess);
      objectSelects.addElement(sAttSelCreateRoute);
      objectSelects.addElement(sAttSelCreateFolder);
      objectSelects.addElement(sSelOrgName);
      objectSelects.addElement(sSelProjectMemberId);
      objectSelects.addElement(sSelPersonId);
      objectSelects.addElement(sSelOrgId);
      objectSelects.addElement(sSelPersonLastName);
      objectSelects.addElement(sSelPersonFirstName);
      objectSelects.addElement(sSelPersonName);
      objectSelects.addElement(sState);
      BaseObject.MULTI_VALUE_LIST.add(sAssignBuyerDesk);
      objectSelects.addElement(sAssignBuyerDesk);
      objectSelects.addElement(BaseObject.SELECT_ID);

      String sGrantee       = "";
      String sAccessString  = "";
      StringList sPersonList = new StringList();
      AccessUtil accessUtil = new AccessUtil();

      Iterator granteeItr = (Workspace.getGrantees(context)).iterator();
      StringList granteeList = new StringList();
      while(granteeItr.hasNext())
      {
        String grantee = (String) granteeItr.next();
        if(!granteeList.contains(grantee))
        {
            granteeList.addElement(grantee);
        }
      }
      granteeItr = (BaseObject.getGrantees(context)).iterator();
      while(granteeItr.hasNext())
      {
          String grantee = (String) granteeItr.next();
          if(!granteeList.contains(grantee))
          {
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

      while(mapItr.hasNext())
     {
      boolean bAssignedBuyer = false;
      map = (Map)mapItr.next();
      sGrantee = (String)map.get(sSelPersonName);
      sPersonList.addElement(sGrantee);
      String strState =  (String)map.get(sState);
      if(strState.equals("Active") && (bPersonFlag || bBuyerFlag)) {
        hashMap = new HashMap();
        hashMap.put("Name", sGrantee);
        hashMap.put("ProjectLead", map.get(sAttSelProjectAccess));
        hashMap.put("CreateRoute", map.get(sAttSelCreateRoute));
        hashMap.put("CreateFolder", map.get(sAttSelCreateFolder));
        hashMap.put("Organization",map.get(sSelOrgName));
        hashMap.put("ProjectMemberId", map.get(sSelProjectMemberId));
        hashMap.put("PersonId", map.get(sSelPersonId));
        hashMap.put("OrgId", map.get(sSelOrgId));
        hashMap.put("LastFirstName", (String)map.get(sSelPersonLastName) + ","+ (String)map.get(sSelPersonFirstName));

        if(!((String)map.get(sAttSelProjectAccess)).equals("Project Lead"))
        {
          getAccess(context, baseAccessList, parentAccessList, sGrantee, sTypeName, hashMap, accessUtil);
        } else
        {
          sAccessString = accessUtil.ADD_REMOVE;
          hashMap.put("InheritedAccess", sAccessString);
          hashMap.put("SpecificAccess", sAccessString);
        }
        //to check if any Buyer Desk Attached to the Workspace
        if(sBuyerDeskId != null)
        {
          StringList sBuyerDeskList = new StringList();
          try
          {
            String sBuyerDeskName = (String)map.get(sAssignBuyerDesk);
            if (sBuyerDeskName != null) {
              sBuyerDeskList.addElement(sBuyerDeskName);
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
            hashMap.put("Type", "BuyerDesk Person");
            hashMap.put("isBuyer","true");
            AccessMapList.add(hashMap);
          }
        }

        //To display only Project Members other than Assigned Buyers
        if(bPersonFlag && (!bAssignedBuyer)) {
          hashMap.put("Type", BaseObject.TYPE_PERSON);
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
        hashMap.put("Name", sGrantee);
        hashMap.put("ProjectLead", "");
        hashMap.put("CreateRoute", "");
        hashMap.put("CreateFolder", "");
        hashMap.put("Organization", "");
        hashMap.put("ProjectMemberId", "");
        hashMap.put("PersonId", "");
        hashMap.put("Type", "Role");
        hashMap.put("OrgId", "null");
        hashMap.put("isBuyer", "false");
        hashMap.put("LastFirstName", i18nNow.getRoleI18NString(sGrantee,language));
        getAccess(context, baseAccessList, parentAccessList, sGrantee, sTypeName, hashMap, accessUtil);
        AccessMapList.add(hashMap);
      }
    }


    return AccessMapList;
  }


   /**
   * Method to show the PersonName in Folder Access  List
   * Returns a Vector of person names along with image icons
   * @param context the eMatrix Context object
   * @param args holds the grantee and grantor name
   * @return Vector
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */


 public Vector showPersonName(Context context,String[] args) throws Exception {
     
     Vector personNames=new Vector();
     HashMap programMap=(HashMap)JPO.unpackArgs(args);
     HashMap paramMap=(HashMap)programMap.get("paramList");
     
     MapList objectList=(MapList)programMap.get("objectList");
     
     String languageStr = (String)paramMap.get("languageStr");
     String workspaceId =(String)paramMap.get("workspaceId");
     String folderId =(String)paramMap.get("folderId");
     String jsTreeID =(String)paramMap.get("jsTreeID");
     
     String reportFormat = (String)paramMap.get("reportFormat");
     boolean isReportMode = !UIUtil.isNullOrEmpty(reportFormat);
     boolean isTextReport = isReportMode && ("CSV".equalsIgnoreCase(reportFormat) || "Text".equalsIgnoreCase(reportFormat));
     
     String imgRole = "<img src='images/iconSmallRole.gif' border='0' id=''/>";
     String imgPerson = "<img src='images/iconSmallPerson.gif' border='0' id=''/>";
     
     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)  {
         
         HashMap mapAccess = (HashMap)objectListItr.next();
         
         String sProjectMemberId        = (String)mapAccess.get("ProjectMemberId");
         String sDisplayPersonName      = (String)mapAccess.get("LastFirstName");
         boolean isRoleType = UIUtil.isNullOrEmpty(sProjectMemberId);
         
         StringBuffer memberDisplayStrBuffer = new StringBuffer();
         if(isTextReport) {
             memberDisplayStrBuffer.append(XSSUtil.encodeForHTML(context, sDisplayPersonName));
         } else if(isRoleType || (isReportMode && !isTextReport)) {
             memberDisplayStrBuffer.append(isRoleType ? imgRole : imgPerson);
             memberDisplayStrBuffer.append("&nbsp;");
             memberDisplayStrBuffer.append(XSSUtil.encodeForHTML(context, sDisplayPersonName));
         } else {
             String sPersonId               = (String)mapAccess.get("PersonId");
             String sOrgId                  = (String)mapAccess.get("OrgId");
             
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
   * Method to get Inherited Access For Folder
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
     String languageStr = (String)paramMap.get("languageStr");
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
        inheritAccess.add(EnoviaResourceBundle.getProperty(context,stringResFileId,new Locale(languageStr),"emxTeamCentral.Access."+(String)i18nMap.get(mapAccess.get("InheritedAccess"))));
     }


     return inheritAccess;
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

     MapList objectList=(MapList)programMap.get("objectList");
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

     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
     {
        HashMap mapAccess=(HashMap)objectListItr.next();

    String stCheckNull = (String)i18nMap.get(mapAccess.get("SpecificAccess"));
    if (stCheckNull != null && !stCheckNull.equals("null") && !stCheckNull.equals(""))
    {
      availableAccesses.add(EnoviaResourceBundle.getProperty(context,stringResFileId,new Locale(languageStr),"emxTeamCentral.Access."+(String)i18nMap.get(mapAccess.get("SpecificAccess"))));
    }
    else
    {
      availableAccesses.add("");
    }
     }


     return availableAccesses;
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
      for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
      {
        HashMap mapAccess=(HashMap)objectListItr.next();
        String sOrgName                = (String)mapAccess.get("Organization");
        String sOrgId                  = (String)mapAccess.get("OrgId");
        accessOrganizations.add(sOrgName);
      }


      return accessOrganizations;
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
     for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
     {
        HashMap mapAccess=(HashMap)objectListItr.next();
        String sType = (String)mapAccess.get("Type");
        i18nNow loc = new i18nNow();
        if ("Role".equals(sType)){
        	accessTypes.add(loc.GetString("emxFrameworkStringResource", languageStr, "emxFramework.Common.Role"));
     }
        else if("Group".equals(sType)){
        	accessTypes.add(loc.GetString("emxFrameworkStringResource", languageStr, "emxFramework.Common.Group"));
        }
        else {
        	accessTypes.add(i18nNow.getTypeI18NString(sType, languageStr));

        }
     }
     return accessTypes;

 }


 /**
   * Method to populate the paased HashMap with Access Details
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */


  private void getAccess (Context context,
                              AccessList baseAccessList,
                              AccessList parentAccessList,
                              String  sGrantee, String sTypeName, HashMap hashMap,
                              AccessUtil accessUtil)
{
    String sAccessString = "";
    try {
      matrix.db.Access  access = getGranteeAccess(context,baseAccessList,sGrantee );
      sAccessString = accessUtil.checkAccess(access);
    } catch(Exception exp) {
      sAccessString = accessUtil.NONE;
    }
    String sInheritAccess = "";
    try {
      matrix.db.Access inheritAccess = getGranteeAccess(context,parentAccessList,sGrantee);
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
       if(sAccessString == null || sAccessString.equals("") || sAccessString.equals(accessUtil.NONE)){
         sAccessString = "&nbsp";
       }
    }
     hashMap.put("InheritedAccess", sInheritAccess);
     hashMap.put("SpecificAccess", sAccessString);
 }


    /**
   * Method to calculat a person access with a given person Access List
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   */
private matrix.db.Access getGranteeAccess (matrix.db.Context context,
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



  private boolean isAssignedBuyer(StringList BuyerDeskListId,String WSBuyerDeskId) {
    StringList sList = BuyerDeskListId ;
    String  sDeskId = WSBuyerDeskId;
    boolean bBuyer = false;
    if( BuyerDeskListId == null){
      return false;
    }

    if( BuyerDeskListId.contains(sDeskId)){
      bBuyer = true;
    }

    return bBuyer;
  }

     /**
     * This method is used to show Push subscription link or not
     * Args Array takes objectid, event, boolean if object is a version
     * @param context the eMatrix <code>Context</code> object
     * @param args String array of parameters
     * @throws Exception if the operation fails
     * @since Common 10-0-5-0
     */
    public boolean isNotVersionObject (matrix.db.Context context, String[] args) throws Exception
    {
        // Equ Access Expression:
        // {settings[Access Expression]=($<attribute[attribute_IsVersionObject].value> != True)}
        HashMap paramsMap = (HashMap)JPO.unpackArgs(args);
        String objectId       = (String) paramsMap.get("objectId");
        String selectParentId = "last.to[" + CommonDocument.RELATIONSHIP_LATEST_VERSION + "].from.id";
        StringList selects = new StringList(3);
        selects.add(DomainConstants.SELECT_ID);
        selects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        selects.add(selectParentId);
        DomainObject parentObject = DomainObject.newInstance(context, objectId);
        Map objectSelectMap = parentObject.getInfo(context,selects);
        String objIsVersion = (String)objectSelectMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
        return !(objIsVersion.equalsIgnoreCase("true"));
    }


    protected StringBuffer sbProjectMember           = new StringBuffer(64);
    protected static final String attributeBracket          = "attribute[";
    protected static final String closeBracket              = "]";
    protected StringBuffer sbAttrCreateFolder        = new StringBuffer(64);
    protected StringBuffer sbAttrProjectAccess       = new StringBuffer(64);
    protected StringBuffer sbContentAttr             = new StringBuffer(64);

    public MapList getFolders(Context context, String[] args) throws Exception
    {
/*
        DomainObject object = DomainObject.newInstance(context, objId);
        StringList selects = new StringList(3);
        selects.add(SELECT_TYPE);
        selects.add("to[Vaulted Objects].from.id");
        Map objMap = object.getInfo(context, selects);
        String folderId = (String)objMap
*/
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId             = (String)programMap.get("objectId");
        Map reqMap = (Map)programMap.get("RequestValuesMap");
        String[] tids = (String[])reqMap.get("emxTableRowId");
        reqMap.put("TESTING_REQ_MAP", "WORKING");
        programMap.put("TESTING_PROGRAM_MAP", "WORKING");
        String tableRowId          = (String)programMap.get("emxTableRowId");
        String action             = (String)programMap.get("action");
        String strProjectVaultType  = PropertyUtil.getSchemaProperty(context,"type_ProjectVault" );
        StringList selects = new StringList(3);
        selects.add(SELECT_ID);
        String sWhere = "id !=" + tableRowId + " && (current.access[read,modify,checkout,checkin,lock,unlock,revise,fromconnect,toconnect,show] == TRUE)";
        MapList folderList = DomainObject.findObjects(context, strProjectVaultType, "*", sWhere, selects);
        return folderList;
    }

   public Vector getWorkspaceName(Context context, String[] args)
     throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector vecContent = new Vector(objList.size());
            if ( objList != null)
            {
               for (int i = 0; i < objList.size(); i++)
               {
                String workspaceName=getWorkspaceName(context,(String)(((Map)objList.get(i)).get(SELECT_ID)));
                vecContent.add(workspaceName);
               }
            }
            return vecContent;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    static public String getWorkspaceName(matrix.db.Context context, String folderId) throws MatrixException
    {
      String strProjectVault = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults" );
      String strProjectType  = PropertyUtil.getSchemaProperty(context,"type_Project");
      String strSubVaultsRel = PropertyUtil.getSchemaProperty(context,"relationship_SubVaults");
      String strProjectVaultType  = PropertyUtil.getSchemaProperty(context,"type_ProjectVault" );
      DomainObject domainObject = DomainObject.newInstance(context);
      domainObject.setId(folderId);

      String projectName = "";

      Pattern relPattern  = new Pattern(strProjectVault);
      relPattern.addPattern(strSubVaultsRel);
      Pattern typePattern = new Pattern(strProjectType);
      typePattern.addPattern(strProjectVaultType);

      StringList objSelects = new StringList();
      objSelects.addElement(DomainConstants.SELECT_NAME);

      MapList mapList = domainObject.getRelatedObjects(context,
                                               relPattern.getPattern(),
                                               typePattern.getPattern(),
                                               objSelects,
                                               null,
                                               true,
                                               false,
                                               (short)-1,
                                               "",
                                               "",
                                               null,
                                               null,
                                               null);
      Iterator mapItr = mapList.iterator();
      if(mapItr.hasNext())
      {
        Map map = (Map)mapItr.next();
        projectName = (String) map.get(DomainConstants.SELECT_NAME);
      }
      return projectName;
    }

  /**
   * hasMoveAccess - to check whether to display the Move command.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasMoveAccess(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");
      //Added for Bug 371163 starts
      String sCreateFolder       = "";
      String sProjectAccess      = "";
      String sOwner              = "";
      boolean boolChecking = false;
      Workspace workspace  = null;
      String sUser       = context.getUser();

      DomainObject BaseObject     = DomainObject.newInstance(context,objectId);
      String sTypeName = BaseObject.getType(context);
      if(sTypeName.equals(BaseObject.TYPE_WORKSPACE)) {
          workspace = (Workspace)DomainObject.newInstance(context, objectId);
        } else if(sTypeName.equals(BaseObject.TYPE_WORKSPACE_VAULT)) {
          String sProjectId = getProjectId(context, objectId);
          workspace = (Workspace)DomainObject.newInstance(context, sProjectId);

        }

      StringList sList = new StringList();
      sList.add("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
      sList.add("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
      sList.add("to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name");
      sList.add(DomainObject.SELECT_OWNER);

      MapList memberList = workspace.getWorkspaceMembers(context,sList,"to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name=='"+sUser+"'");
      Iterator memberItr = memberList.iterator();
      while(memberItr.hasNext()) {

          Map memberMap    = (Map)memberItr.next();
          sCreateFolder    = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
          sProjectAccess   = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
          sOwner   = (String)memberMap.get(DomainObject.SELECT_OWNER);
         }
      if(sOwner.equals(sUser) || "Project Lead".equals(sProjectAccess)||"Yes".equals(sCreateFolder))
      {
          boolChecking = true;
      }
      //Added for Bug 371163 starts
     return boolChecking;
  }

  /**
   * hasLinkAccess - to check whether to display the Link command.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasLinkAccess(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");
      //Added for Bug 371163 starts
      String sCreateFolder       = "";
      String sProjectAccess      = "";
      String sOwner              = "";
      boolean boolChecking = false;
      Workspace workspace  = null;
      String sUser       = context.getUser();

      DomainObject BaseObject     = DomainObject.newInstance(context,objectId);
      String sTypeName = BaseObject.getType(context);
      if(sTypeName.equals(BaseObject.TYPE_WORKSPACE)) {
          workspace = (Workspace)DomainObject.newInstance(context, objectId);
        } else if(sTypeName.equals(BaseObject.TYPE_WORKSPACE_VAULT)) {
          String sProjectId = getProjectId(context, objectId);
          workspace = (Workspace)DomainObject.newInstance(context, sProjectId);

        }

      StringList sList = new StringList();
      sList.add("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
      sList.add("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
      sList.add("to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name");
      sList.add(DomainObject.SELECT_OWNER);

      MapList memberList = workspace.getWorkspaceMembers(context,sList,"to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name=='"+sUser+"'");
      Iterator memberItr = memberList.iterator();
      while(memberItr.hasNext()) {

          Map memberMap    = (Map)memberItr.next();
          sCreateFolder    = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
          sProjectAccess   = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
          sOwner   = (String)memberMap.get(DomainObject.SELECT_OWNER);
         }
      if(sOwner.equals(sUser) || "Project Lead".equals(sProjectAccess)||"Yes".equals(sCreateFolder))
      {
          boolChecking = true;
      }
      //Added for Bug 371163 starts
     return boolChecking;
  }

  /**
   * hasUnlinkAccess - to check whether to display the UnLink command.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasUnlinkAccess(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");
      //Added for Bug 371163 starts
      String sCreateFolder       = "";
      String sProjectAccess      = "";
      String sOwner              = "";
      boolean boolChecking = false;
      Workspace workspace  = null;
      String sUser       = context.getUser();

      DomainObject BaseObject     = DomainObject.newInstance(context,objectId);
      String sTypeName = BaseObject.getType(context);
      if(sTypeName.equals(BaseObject.TYPE_WORKSPACE)) {
          workspace = (Workspace)DomainObject.newInstance(context, objectId);
        } else if(sTypeName.equals(BaseObject.TYPE_WORKSPACE_VAULT)) {
          String sProjectId = getProjectId(context, objectId);
          workspace = (Workspace)DomainObject.newInstance(context, sProjectId);

        }

      StringList sList = new StringList();
      sList.add("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
      sList.add("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
      sList.add("to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name");
      sList.add(DomainObject.SELECT_OWNER);

      MapList memberList = workspace.getWorkspaceMembers(context,sList,"to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name=='"+sUser+"'");
      Iterator memberItr = memberList.iterator();
      while(memberItr.hasNext()) {

          Map memberMap    = (Map)memberItr.next();
          sCreateFolder    = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
          sProjectAccess   = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
          sOwner   = (String)memberMap.get(DomainObject.SELECT_OWNER);
         }
      if(sOwner.equals(sUser) || "Project Lead".equals(sProjectAccess)||"Yes".equals(sCreateFolder))
      {
          boolChecking = true;
      }
      //Added for Bug 371163 starts
     return boolChecking;
  }

	public static String RELATIONSHIP_LINKED_FOLDERS = PropertyUtil.getSchemaProperty("relationship_LinkedFolders");
	public static String SELECT_ATTRIBUTE_COUNT =  getAttributeSelect(ATTRIBUTE_COUNT);

	public static String getVaultDisplayName(Context context, String[] args) throws Exception
	{
		String lable = "";
		try
		{

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap=(HashMap)programMap.get("paramMap");
			String oid = (String)paramMap.get("objectId");
			String relId = (String)paramMap.get("relId");
			DomainObject obj = DomainObject.newInstance(context, oid);
			StringList selects = new StringList(4);
			selects.add(SELECT_ATTRIBUTE_COUNT);
			selects.add(SELECT_NAME);
			Map objMap = obj.getInfo(context, selects);

			String languageStr = (String)paramMap.get("languageStr");
			String stringResFileId = "emxTeamCentralStringResource";
			String linked = EnoviaResourceBundle.getProperty(context,stringResFileId,new Locale(languageStr),"emxTeamCentral.Common.Linked");

			String name = (String) objMap.get(SELECT_NAME);
			String count = (String) objMap.get(SELECT_ATTRIBUTE_COUNT);
			lable = name + "(" + count +")";
			if(! UIUtil.isNullOrEmpty(relId) && ! relId.equalsIgnoreCase("undefined")) 
			{
				DomainRelationship rel = new DomainRelationship(relId);
				rel.open(context);
				String relName = rel.getRelationshipType().getName();
				if( RELATIONSHIP_LINKED_FOLDERS.equals(relName) )
				{
					lable = lable + "("+ linked +")";
				}
				rel.close(context);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return lable;
  	}

	public static Vector isLinkedFolder(Context context, String[] args) throws Exception
	{
		Vector linkedVector = new Vector();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList=(MapList)programMap.get("objectList");
     		HashMap paramMap=(HashMap)programMap.get("paramList");
			String languageStr = (String)paramMap.get("languageStr");
			String stringResFileId = "emxTeamCentralStringResource";
			String linked = EnoviaResourceBundle.getProperty(context,stringResFileId,new Locale(languageStr),"emxTeamCentral.Common.Linked");

			for (int i=0; i<objectList.size(); i++)
			{
				Map objMap = (Map)objectList.get(i);
				String relName = (String)objMap.get(KEY_RELATIONSHIP);
				if( RELATIONSHIP_LINKED_FOLDERS.equals(relName) )
				{
					linkedVector.add("<img border='0' src='../common/images/iconSmallLinkedObject.gif' alt='"+linked+"'>");
				} else {
					linkedVector.add("");
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		//XSSOK
		return linkedVector;
  	}

      /**
       * hasCreateAccess - to check whether to display Create New command.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the objectId
       * @return boolean true if has the access else returns false
       * @throws Exception if the operation fails
       * @since TC X+3
       */
      public boolean hasCreateAccess(Context context, String args[]) throws Exception
      {
          try {
              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              String strObjectId = (String) programMap.get("objectId");


              Workspace workspace = null;
              String workspaceId = "";

              StringList slBusSelect = new StringList();
              slBusSelect.add(sbAttrCreateFolder.toString());
              slBusSelect.add(sbAttrProjectAccess.toString());
              slBusSelect.add(DomainObject.SELECT_OWNER);
              slBusSelect.add(sbProjectMember.toString());

              StringBuffer sbWhere = new StringBuffer(sbProjectMember.length()+24);
              sbWhere.append(sbProjectMember.toString());
              sbWhere.append("=='");
              sbWhere.append(context.getUser());
              sbWhere.append("'");

              DomainObject dom = new DomainObject(strObjectId);

              if( !DomainConstants.TYPE_WORKSPACE.equals(dom.getInfo(context, DomainConstants.SELECT_TYPE)) )
              {
                workspaceId = getProjectId(context,strObjectId);
                workspace = (Workspace)DomainObject.newInstance(context,workspaceId,DomainConstants.TEAM);
              }
              else
              {
                workspace = (Workspace)DomainObject.newInstance(context,strObjectId,DomainConstants.TEAM);
              }

              Access access = workspace.getAccessMask(context);

              MapList memberList = workspace.getWorkspaceMembers(context, slBusSelect, sbWhere.toString());
              Iterator memberItr     = memberList.iterator();
              String sCreateFolder   = "";
              String sProjectAccess  = "";
              String sOwner          = "";
              Map memberMap          = null;

              while (memberItr.hasNext()) {
                 memberMap       = (Map)memberItr.next();
                 sCreateFolder   = (String)memberMap.get(sbAttrCreateFolder.toString());
                 sProjectAccess  = (String)memberMap.get(sbAttrProjectAccess.toString());
                 sOwner          = (String)memberMap.get(DomainObject.SELECT_OWNER);
              }

              if(sOwner.equals(context.getUser()) || "Project Lead".equals(sProjectAccess) || "Yes".equals(sCreateFolder)) {
                  return true;
              }
              else {
                  return false;
              }
          }
          catch(Exception e)
          {
              throw e;
          }      }
      /**
       * hasCopyAccess - to check whether to display the Copy command.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the objectId
       * @return boolean type
       * @throws Exception if the operation fails
       * @since Team R207
       */
      public static boolean hasCopyAccess(Context context, String args[]) throws Exception
      {
          HashMap programMap         = (HashMap) JPO.unpackArgs(args);
          String objectId            = (String) programMap.get("objectId");
          //Added for Bug 371163 starts
          String sCreateFolder       = "";
          String sProjectAccess      = "";
          String sOwner              = "";
          boolean boolChecking = false;
          Workspace workspace  = null;
          String sUser       = context.getUser();

          DomainObject BaseObject     = DomainObject.newInstance(context,objectId);
          String sTypeName = BaseObject.getType(context);
          if(sTypeName.equals(BaseObject.TYPE_WORKSPACE)) {
              workspace = (Workspace)DomainObject.newInstance(context, objectId);
            } else if(sTypeName.equals(BaseObject.TYPE_WORKSPACE_VAULT)) {
              String sProjectId = getProjectId(context, objectId);
              workspace = (Workspace)DomainObject.newInstance(context, sProjectId);

            }

          StringList sList = new StringList();
          sList.add("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
          sList.add("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
          sList.add("to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name");
          sList.add(DomainObject.SELECT_OWNER);

          MapList memberList = workspace.getWorkspaceMembers(context,sList,"to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name=='"+sUser+"'");
          Iterator memberItr = memberList.iterator();
          while(memberItr.hasNext()) {

              Map memberMap    = (Map)memberItr.next();
              sCreateFolder    = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
              sProjectAccess   = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
              sOwner   = (String)memberMap.get(DomainObject.SELECT_OWNER);
             }
          if(sOwner.equals(sUser) || "Project Lead".equals(sProjectAccess)||"Yes".equals(sCreateFolder))
          {
              boolChecking = true;
          }
          //Added for Bug 371163 starts
         return boolChecking;
      }

      /**
       * hasCloneStructureAccess - to check whether to display the Clone Structure command.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the objectId
       * @return boolean type
       * @throws Exception if the operation fails
       * @since Team R207
       */
      public static boolean hasCloneStructureAccess(Context context, String args[]) throws Exception
      {
          HashMap programMap         = (HashMap) JPO.unpackArgs(args);
          String objectId            = (String) programMap.get("objectId");
          //Added for Bug 371163 starts
          String sCreateFolder       = "";
          String sProjectAccess      = "";
          String sOwner              = "";
          boolean boolChecking = false;
          Workspace workspace  = null;
          String sUser       = context.getUser();

          DomainObject BaseObject     = DomainObject.newInstance(context,objectId);
          String sTypeName = BaseObject.getType(context);
          if(sTypeName.equals(BaseObject.TYPE_WORKSPACE)) {
              workspace = (Workspace)DomainObject.newInstance(context, objectId);
            } else if(sTypeName.equals(BaseObject.TYPE_WORKSPACE_VAULT)) {
              String sProjectId = getProjectId(context, objectId);
              workspace = (Workspace)DomainObject.newInstance(context, sProjectId);

            }

          StringList sList = new StringList();
          sList.add("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
          sList.add("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
          sList.add("to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name");
          sList.add(DomainObject.SELECT_OWNER);

          MapList memberList = workspace.getWorkspaceMembers(context,sList,"to["+workspace.RELATIONSHIP_PROJECT_MEMBERSHIP+"].from.name=='"+sUser+"'");
          Iterator memberItr = memberList.iterator();
          while(memberItr.hasNext()) {

              Map memberMap    = (Map)memberItr.next();
              sCreateFolder    = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_CREATE_FOLDER+"].value");
              sProjectAccess   = (String)memberMap.get("attribute["+workspace.ATTRIBUTE_PROJECT_ACCESS+"].value");
              sOwner   = (String)memberMap.get(DomainObject.SELECT_OWNER);
             }
          if(sOwner.equals(sUser) || "Project Lead".equals(sProjectAccess)||"Yes".equals(sCreateFolder))
          {
              boolChecking = true;
          }
          //Added for Bug 371163 starts
         return boolChecking;
      }
      
//    Added:08-Apr-2010:lvc:V6R2011x:TMC Wellness Plan
      /**
       * Used to connect the newly created workspace vault to Workspace type
       * 
       * @param context Object
       * @param args String array
       * @throws Exception
       */
         
      public void folderCreateProcess(Context context, String[] args)throws Exception
      {
          String MAX_LENGTH =FrameworkProperties.getProperty(context,"emxComponents.MAX_FIELD_LENGTH");
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          HashMap requestMap = (HashMap)programMap.get("requestMap");
          String language = (String) programMap.get("languageStr");
          language = language == null ? context.getLocale().getLanguage() : language;
          String objectId = (String)requestMap.get("objectId");
          String strFolderName = (String)requestMap.get("NameDisplay");
          String strDesc = (String)requestMap.get("Description");
		  Locale strLocale = new Locale(language);
         
          String strPolicyProjectVault    = PropertyUtil.getSchemaProperty(context, "policy_ProjectVault");
          String strRelProjectVaults      = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");
          String strTypefolder            = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");

          boolean isDSCInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionDesignerCentral",false,null,null);

          boolean bExists = false;
          String folderId = "";

          try{
            BusinessObject boProject            = new BusinessObject(objectId);
            boProject.open(context);
            //change
            Workspace workspace        = (Workspace) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE,DomainConstants.TEAM);
            workspace.setId(objectId);
           // String strProjectRevision           = boProject.getRevision();
           // strProjectRevision                 += "-" + boProject.getName();
            String strProjectVault              = boProject.getVault();

            String strProjectRevision = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", objectId, "physicalid", "|" );
            
            RelationshipType projectVaultsRelType = new RelationshipType(strRelProjectVaults);
            // Creating a Category and connect this to the Current Project
            BusinessObject boNewCategory = new BusinessObject(strTypefolder, strFolderName, strProjectRevision, strProjectVault);

            if(strFolderName.length()>(Integer.parseInt(MAX_LENGTH)))
            {
                String strLengthMessage = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",strLocale, "emxTeamCentral.FolderNameLength.Message");
                String strChars = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",strLocale, "emxTeamCentral.NameLength.NumChars");
                MqlUtil.mqlCommand(context, "notice $1", strLengthMessage + " " + MAX_LENGTH + " " + strChars);
                return;
            }
            
            if (workspace.isFolderExists(context, strFolderName)) {
              bExists = true;
              String i18NCategory = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(context.getSession().getLanguage()),"emxTeamCentral.AddCategories.Category");
              String i18NNotUnique = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(context.getSession().getLanguage()),"emxTeamCentral.AddCategories.NotUnique");
              String strMessage = i18NCategory +"  "+ boNewCategory.getName() +"  "+ i18NNotUnique;
              emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            }
            if(!bExists) {
              boNewCategory.create(context, strPolicyProjectVault);
              folderId = boNewCategory.getObjectId();
              SubscriptionManager subscriptionMgr = workspace.getSubscriptionManager();
              
              String treeMenu = "type_ProjectVault";
              if(  treeMenu  != null && !"null".equals( treeMenu  ) && !"".equals( treeMenu )) {
                MailUtil.setTreeMenuName(context, treeMenu );
              }

              subscriptionMgr.publishEvent(context, workspace.EVENT_FOLDER_CREATED, boNewCategory.getObjectId());

              if (strDesc != null && !strDesc.equals("")) {
                boNewCategory.setDescription(strDesc);
                boNewCategory.update(context);
              }
              //connecting the new Category to the Project
              boNewCategory.connect( context, projectVaultsRelType, false, boProject);
            }
            boNewCategory.close(context);
            boProject.close(context);
            //Code for Tracking if the Folder is created in Route Wizard or not
            /*  Hashtable hashRouteWizFirst = (Hashtable)session.getValue("hashRouteWizFirst");
              if (hashRouteWizFirst != null)
              {
                hashRouteWizFirst.put("newFolderIds" , folderId);
                session.putValue("hashRouteWizFirst" , hashRouteWizFirst);
              }*/
      
      }
       
       catch (Exception ex){
          bExists = true;
         throw ex;
       }
   
     
       }
//     Added:12-Apr-2010:lvc:V6R2011x:TMC Wellness Plan
       /**
        * Used to connect the newly created workspace vault to parent workspace vault type
        * 
        * @param context Object
        * @param args String array
        * @throws Exception
        */
          
       public void subfolderCreateProcess(Context context, String[] args)throws Exception
       {
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String language = (String) programMap.get("languageStr");
           language = language == null ? context.getLocale().getLanguage() : language;
           String objectId = (String)requestMap.get("objectId");
           String strSubcategoryName = (String)requestMap.get("Name");
           String strSubcategoryDesc = (String)requestMap.get("Description");
           String strProjectId                 = "";
           String strPolicyProjectVault    = PropertyUtil.getSchemaProperty(context, "policy_ProjectVault");
           String strRelProjectVaults      = PropertyUtil.getSchemaProperty(context, "relationship_SubVaults");
           String strTypefolder            = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
           String i18NCategory                 = "";
           String i18NNotUnique                = "";
           String treeUrl                      = null;
           String objID                        = "";
           boolean isDSCInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionDesignerCentral",false,null,null);

           boolean bExists                     = false;

           BusinessObject boNewSubCategory     = null;
           SubscriptionManager subscriptionMgr = null;

           try {
             //To get the workspace object
             strProjectId                       = getProjectId(context,objectId);
             Workspace workspace                = (Workspace) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE,DomainConstants.TEAM);
             workspace.setId(strProjectId);
             WorkspaceVault workspaceVault      = (WorkspaceVault) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE_VAULT,DomainConstants.TEAM);
             workspaceVault.setId(objectId);

             workspace.open(context);
             workspaceVault.open(context);

          
             String strProjectRevision = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", objectId, "physicalid", "|" );
            
             // Creating a Sub Category and connect this to Parent Category with Sub Vault Relationship
                //<--!Added for the Bug No:339243 08/06/2007 10:00AM Start-->
            String folderName = workspaceVault.getInfo(context,DomainConstants.SELECT_NAME);
            //<--!Ended for the Bug No:339243 08/06/2007 10:00AM End-->
             boNewSubCategory = new BusinessObject(workspace.TYPE_WORKSPACE_VAULT, strSubcategoryName, strProjectRevision, workspace.getVault());

            //<--!Modifieded for the Bug No:339243 08/06/2007 10:00AM Start-->
            String i18NSubFolderName = EnoviaResourceBundle.getProperty(context,"emxTeamCentral",new Locale(language),"emxTeamCentral.Common.CheckforSubFolderName");
            
            if(i18NSubFolderName.equalsIgnoreCase("true"))
            {
             if (strSubcategoryName.equals(folderName) || (workspaceVault.isSubFolderExists(context, strSubcategoryName))) {
                 bExists = true;
                 i18NCategory = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(language),"emxTeamCentral.AddCategories.SubCategory");
                 i18NNotUnique = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(language),"emxTeamCentral.AddCategories.NotUnique");
                 String strMessage = i18NCategory +"  "+ boNewSubCategory.getName() +"  "+ i18NNotUnique;
                 emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                } 
            }
            
            //<--!Modied for the Bug No:339243 08/06/2007 10:00AM End-->
            else {
               boNewSubCategory.create(context, workspace.POLICY_PROJECT_VAULT);
               boolean boolTree = false;
               String treeMenu = FrameworkProperties.getProperty(context,"eServiceSuiteTeamCentral.emxTreeAlternateMenuName.type_ProjectVault");
               if(  treeMenu  != null && !"null".equals( treeMenu  ) && !"".equals( treeMenu )){
                   boolTree = true;
               }
               if(boolTree == true){
                 MailUtil.setTreeMenuName(context, treeMenu );
               }
               // Publish workspace  Event 'Folder Created'.
               subscriptionMgr = workspace.getSubscriptionManager();
               subscriptionMgr.publishEvent(context, workspace.EVENT_FOLDER_CREATED, boNewSubCategory.getObjectId());

               if(boolTree == true){
                 MailUtil.setTreeMenuName(context, treeMenu );
               }
               // Publish workspace Vault  Event 'Folder Created'.
               subscriptionMgr = workspaceVault.getSubscriptionManager();
               subscriptionMgr.publishEvent(context, workspaceVault.EVENT_FOLDER_CREATED, boNewSubCategory.getObjectId());

               DomainObject subVault =  DomainObject.newInstance(context,boNewSubCategory);
               workspaceVault.connectTo(context, workspaceVault.RELATIONSHIP_SUBVAULTS, subVault);
               subVault.setDescription(context, strSubcategoryDesc);
               objID = subVault.getInfo(context, subVault.SELECT_ID);
               //treeUrl = UINavigatorUtil.getCommonDirectory(application) + "/emxTree.jsp?objectId=" + objID +"&emxSuiteDirectory="+appDirectory+"&mode=insert&AppendParameters=true&folderId=" + strObjectId + "&workspaceId=" + workspaceId;
             }
           
       }catch(Exception e)
       {
           throw e;
       }
       
      }
       
          
//     Added:12-Apr-2010:lvc:V6R2011x:TMC Wellness Plan
       /**
        * Used to connect the newly created workspace vault to parent workspace vault type
        * 
        * @param context Object
        * @param args String array
        * @throws Exception
        */
          
       public String getParentForSubVault(Context context, String[] args)throws Exception
       {
           
           String strParent = "";
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String objectId = (String)requestMap.get("objectId");
           BusinessObject boFolder = new BusinessObject(objectId);
           boFolder.open(context);
           String strCategoryName = boFolder.getName();
           boFolder.close(context);
               
           strParent = "<img src=\"images/iconSmallFolder.gif\" border=\"0\" id=\"\"/>"+ XSSUtil.encodeForHTML(context,strCategoryName);
           
           
           return strParent;
       }
       
//     Added:08-Apr-2010:lvc:V6R2011x:TMC Wellness Plan
       /**
        * Used to clone an existing folder under a workspace
        * 
        * @param context Object
        * @param args String array
        * @throws Exception
        */
          
       public void folderCloneProcess(Context context, String[] args)throws Exception
       {
           
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String language = (String) programMap.get("languageStr");
           language = language == null ? context.getLocale().getLanguage() : language;
           String sFolderId              = (String)requestMap.get("objectId");
           String strFolderName          = (String)requestMap.get("Name");
           String strFolderDesc          = (String)requestMap.get("Description");
           String strTypefolder          = PropertyUtil.getSchemaProperty( context, "type_ProjectVault");
           String attrCount              = PropertyUtil.getSchemaProperty( context, "attribute_Count" );
           String strProjectRevision     = "";
           String strProjectVault        = "";
           String strProjectId           = "";
           String newFolderId            = "";
           String originalFolderName     = "";
           BusinessObject boProject      = null;
           boolean bExists               = false;
           String sTempId                = "";
           strFolderName                 = strFolderName.trim();

           Workspace ParentObject         = (Workspace) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE, DomainConstants.TEAM);
           WorkspaceVault BaseObject      = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT, DomainConstants.TEAM);

           BaseObject.setId(sFolderId);
           String  sParentId = BaseObject.getInfo(context,"to[" + BaseObject.RELATIONSHIP_SUBVAULTS + "].from.id");

           if(sParentId == null || sParentId.equals("")) {
               sParentId = BaseObject.getInfo(context,"to[" + BaseObject.RELATIONSHIP_WORKSPACE_VAULTS+ "].from.id");
               sTempId = sParentId;
               ParentObject.setId(sParentId);
           } else {
             BaseObject.setId(sParentId);
           }
           if(sTempId == null){
             sTempId = "";
           }  

           if((strFolderName==null) || strFolderName.equals("")) {
             BusinessObject boFolder = new BusinessObject(sFolderId);
             boFolder.open(context);
             strFolderName="Copy of "+boFolder.getName();
             boFolder.close(context);
           }

           if((strFolderDesc==null) || strFolderDesc.equals("")) {
               strFolderDesc ="";
           }

           BusinessObject ObjFolder = new BusinessObject(sFolderId);
           ObjFolder.open(context);
           originalFolderName = ObjFolder.getName();
           //To get the project id to get the project revision and vault
           strProjectId =getProjectId(context,sFolderId);
           boProject= new BusinessObject(strProjectId);
           boProject.open(context);

           strProjectRevision   = boProject.getRevision();

           BusinessObject ParentObj = new BusinessObject(sParentId);
           ParentObj.open(context);


           if(ParentObj.getTypeName().equals(strTypefolder)) {
             strProjectRevision += "-" + ParentObj.getName();
           } else {
             strProjectRevision += "-" + boProject.getName();
           }

           ParentObj.close(context);
           strProjectVault = boProject.getVault();
           boProject.close(context);


           if(!sTempId.equals("") && ParentObject.getType(context).equals(BaseObject.TYPE_PROJECT)) {
             ParentObject.open(context);
             bExists =ParentObject.isFolderExists(context, strFolderName);
             ParentObject.close(context);

           } else {
             BaseObject.open(context);
             bExists = BaseObject.isSubFolderExists(context, strFolderName);
             BaseObject.close(context);
           }

           if (bExists) 
           {
             String i18NCategory =  EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(language),"emxTeamCentral.AddCategories.Category");
             String i18NNotUnique = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(language),"emxTeamCentral.AddCategories.NotUnique");
             String strMessage = i18NCategory +"  "+ strFolderName +"  "+ i18NNotUnique;
             emxContextUtil_mxJPO.mqlNotice(context,strMessage);
             
           }

           if(!bExists) {

             //Clone the folder object
             BusinessObject boCloneObj = ObjFolder.clone(context,strFolderName.trim(),strProjectRevision,strProjectVault);
             boCloneObj.open(context);
             newFolderId   = boCloneObj.getObjectId();
             //to Update the Descriprion
             boCloneObj.setDescription(strFolderDesc);
             boCloneObj.update(context);
             //To clone the subfolders and connect them to the cloned object
             CloneFolders(context,sFolderId,boCloneObj.getObjectId(),strFolderName);
             //Cloned Folder should be "0". (Count is Attribute name of DB)
             AttributeList attrList = new AttributeList();
             attrList.addElement( new Attribute( new AttributeType(attrCount), "0"));
             boCloneObj.setAttributes(context, attrList);

             //get Workspace object for Subscriptions
             ParentObject.setId(strProjectId);
             ParentObject.open(context);
             SubscriptionManager subscriptionMgr = ParentObject.getSubscriptionManager();
             ParentObject.close(context);
             String treeMenu = FrameworkProperties.getProperty(context,"eServiceSuiteTeamCentral.emxTreeAlternateMenuName.type_ProjectVault");
             if(  treeMenu  != null && !"null".equals( treeMenu  ) && !"".equals( treeMenu )) {
                  MailUtil.setTreeMenuName(context, treeMenu );
             }
             subscriptionMgr.publishEvent(context, ParentObject.EVENT_FOLDER_CREATED, newFolderId);
             boCloneObj.close(context);
           }

    
      
        } 
       /**
        * getFolderNames - method to return the name of predefined folders
        * @param context the eMatrix <code>Context</code> object
        * @return Vector
        * @throws Exception if the operation fails
        * @since R210
        * @grade 0
        */  
       public String getCloneName(Context context, String[] args)throws Exception
       {
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String language = (String) programMap.get("languageStr");
           language = language == null ? context.getLocale().getLanguage() : language;
           String objectId = (String)requestMap.get("objectId");
           BusinessObject boFolder  = new BusinessObject(objectId);
           boFolder.open(context);
           String strCloneName = boFolder.getName();
           boFolder.close(context);
           String strName = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(language),"emxTeamCentral.Common.CopyOf");
           strCloneName = strName + " "+strCloneName;
           return strCloneName;
       }
       /**
        * getFolderNames - method to return the name of predefined folders
        * @param context the eMatrix <code>Context</code> object
        * @return Vector
        * @throws Exception if the operation fails
        * @since R210
        * @grade 0
        */  
       public String getCloneDescription(Context context, String[] args)throws Exception
       {
           
                    
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String language = (String) programMap.get("languageStr");
           language = language == null ? context.getLocale().getLanguage() : language;
           String objectId = (String)requestMap.get("objectId");
           BusinessObject boFolder  = new BusinessObject(objectId);
           boFolder.open(context);
           String strDesc        = boFolder.getDescription();
           boFolder.close(context);
           return strDesc;
       }
       /**
        * getFolderNames - method to return the name of predefined folders
        * @param context the eMatrix <code>Context</code> object
        * @return Vector
        * @throws Exception if the operation fails
        * @since R210
        * @grade 0
        */  
       static public void CloneFolders(matrix.db.Context context, String sfolderId,String sCloneId,String strFolderName) throws MatrixException {

           String sFolderObjId = sfolderId;
           String sOrgFolName  = strFolderName;
           String sCloneObjId  = sCloneId;
           String strTypeProjectVault = "type_ProjectVault";
           String strProjectVaultType = PropertyUtil.getSchemaProperty(context, strTypeProjectVault );
           String copyOf="";
           String strSubVaultsRel = PropertyUtil.getSchemaProperty( context, "relationship_SubVaults");


           BusinessObject ObjFolder        = new BusinessObject(sFolderObjId);
           ObjFolder.open(context);
           String orgFolderName            = ObjFolder.getName();
           String strProjectId             = getProjectId(context,sFolderObjId);
           BusinessObject boProject        = new BusinessObject(strProjectId);
           boProject.open(context);
           String strProjectRevision       = boProject.getRevision();
           String strProjectVault          = boProject.getVault();
           boProject.close(context);


           //To check whether folder has any subfolders to clone
           Pattern relSubVaultPattern = new Pattern(strSubVaultsRel);
           Pattern typeCategoryPattern = new Pattern(strProjectVaultType);
           SelectList typeCategorySelectList = new SelectList();
           typeCategorySelectList.addName();
           typeCategorySelectList.addId();
           typeCategorySelectList.addOwner();

           SelectList selListRelationship = new SelectList();
           selListRelationship.addName();
           selListRelationship.addRevision();
           selListRelationship.addOwner();
           selListRelationship.addId();



           ExpansionWithSelect expandWSelectCategory = ObjFolder.expandSelect(context,relSubVaultPattern.getPattern(),typeCategoryPattern.getPattern(),
                                                    typeCategorySelectList,selListRelationship,false, true, (short)1);
           if((expandWSelectCategory.getRelationships()).size() ==0){
             return ;
           }  
           else {
           BusinessObject ObjPntCloneFolder = new BusinessObject(sCloneObjId);
           ObjPntCloneFolder.open(context);

           RelationshipWithSelectItr relWSelectSubCategoryItr = new RelationshipWithSelectItr(expandWSelectCategory.getRelationships());
             //Used For naming the cloned foleders

           //get Workspace object for Subscriptions
           Workspace ws = null;
           try{
               ws = (Workspace)DomainObject.newInstance(context, strProjectId, DomainConstants.TEAM);
            }catch(Exception ex)
           { }

           while(relWSelectSubCategoryItr.next()) {
             String parentVaultName = "";
             strProjectRevision = boProject.getRevision();
             //String folderAutoNameId = autoName(context, session, strProjectVaultType,strProjectRevision, "policy_ProjectVault", strProjectVault);
             BusinessObject obSubVault = relWSelectSubCategoryItr.obj().getTo();
             obSubVault.open(context);
             if((sOrgFolName.equals(orgFolderName)))
             {
             copyOf="Copy of ";
             }

             String folderAutoNameId= copyOf+sOrgFolName+"-"+obSubVault.getName();
             String sFolderId=obSubVault.getObjectId();

             DomainObject domObj           = DomainObject.newInstance(context, ObjPntCloneFolder);
             parentVaultName               = domObj.getName(context);
             strProjectRevision           += "-" + parentVaultName;

             BusinessObject boChildCloneObj = obSubVault.clone(context,folderAutoNameId,strProjectRevision,strProjectVault);
             boChildCloneObj.open(context);

             String sChdCloneId= boChildCloneObj.getObjectId();
             try {

               boChildCloneObj.disconnect(context, new RelationshipType(strSubVaultsRel),false, ObjFolder);
               boChildCloneObj.connect(context, new RelationshipType(strSubVaultsRel), false, ObjPntCloneFolder);
             }    catch(Exception exp) { }

                 AttributeList attrList = new AttributeList();
                 String attrCount  = PropertyUtil.getSchemaProperty( context, "attribute_Count" );
                 attrList.addElement( new Attribute( new AttributeType(attrCount), "0"));
                 boChildCloneObj.setAttributes(context, attrList);

              SubscriptionManager subscriptionMgr = ws.getSubscriptionManager();

              String treeMenu = FrameworkProperties.getProperty(context,"eServiceSuiteTeamCentral.emxTreeAlternateMenuName.type_ProjectVault");
              if(  treeMenu  != null && !"null".equals( treeMenu  ) && !"".equals( treeMenu )) {
                   MailUtil.setTreeMenuName(context, treeMenu );
              }
              subscriptionMgr.publishEvent(context, Workspace.EVENT_FOLDER_CREATED, sChdCloneId);

             //To clone the subfolders and connect them to the cloned object
             CloneFolders(context,sFolderId,sChdCloneId,sOrgFolName);
           }
           return;
         }
         }
       /**
        
        * updateFolderName method updates the cloned folder name
        *
        * @param context Context : User's Context.
        * @param String[] args
        * @return void.
        * @throws Exception if the operation fails.
        */
       public void  updateFolderName(Context context,String[] args)throws Exception {
           
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           HashMap requestMap = (HashMap) programMap.get("requestMap");
           HashMap paramMap   = (HashMap)programMap.get("paramMap");
           paramMap   = (HashMap)programMap.get("paramMap");
           String strFolderId = (String)paramMap.get("objectId");
           String[] array = (String[])paramMap.get("New Values");
           DomainObject doFolder = DomainObject.newInstance(context,strFolderId);
           doFolder.setName(context,array[0]);
         }
       
       /**
       
        * updateFolderDescription method updates the cloned folder name
        *
        * @param context Context : User's Context.
        * @param String[] args
        * @return void.
        * @throws Exception if the operation fails.
        */
       public void  updateFolderDescription(Context context,String[] args)throws Exception {
           
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           HashMap requestMap = (HashMap) programMap.get("requestMap");
           HashMap paramMap   = (HashMap)programMap.get("paramMap");
           paramMap   = (HashMap)programMap.get("paramMap");
           String strFolderId = (String)paramMap.get("objectId");
           String[] array = (String[])paramMap.get("New Values");
           DomainObject doFolder = DomainObject.newInstance(context,strFolderId);
           doFolder.setDescription(context,array[0]);
         }
 
 /**
       
        * getPredefinedFolderName method updates the name of the predefined folder
        *
        * @param context Context : User's Context.
        * @param String[] args
        * @return void.
        * @throws Exception if the operation fails.
        */
       @com.matrixone.apps.framework.ui.ProgramCallable
       public MapList getPredefinedFolderName(Context context,String[] args)throws Exception {
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           String language = (String) programMap.get("languageStr");
           language = language == null ? context.getLocale().getLanguage() : language;
           DomainObject templateObj      = DomainObject.newInstance(context);
           MapList templateMapList       = new MapList();

           String strFolderId = (String)programMap.get("objectId");
           String strRelProjectVaults    = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");
           String strTypeProjectVault    = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
           BusinessObject boProject      = new BusinessObject(strFolderId);

           Hashtable hashCategoriesTable = new Hashtable();
           Enumeration categoriesEnum    = null;
           boolean isSelected            = false;

           String sParams                = "";
           boolean bFolderListEmpty      = false;
             // Take the Categories and its Descriptions from a Properties File
             // String catList = centralProperties.getProperty("emxTeamCentral.DefaultCategories");
             String catList = FrameworkProperties.getProperty(context,"emxTeamCentral.DefaultCategories");
          // if (emxPage.isNewQuery()) {
             StringTokenizer catToken = new StringTokenizer(catList, ";");
             String cat = "";
             String catName = "";
             String catDesc = "";
             while (catToken.hasMoreTokens())
             {
               cat     = catToken.nextToken();
               catName = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(language),"emxTeamCentral.AddCategoriesDialog."+cat);
               catDesc = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(language),"emxTeamCentral.AddCategoriesDialog."+cat+"Desc");
               hashCategoriesTable.put(catName, catDesc);
             }
             Pattern relPattern  = new Pattern(strRelProjectVaults);
             Pattern typePattern = new Pattern(strTypeProjectVault);
             SelectList typeselectList = new SelectList();
             typeselectList.addId();
             typeselectList.addName();
             typeselectList.addDescription();

             boProject.open(context);
             ExpansionWithSelect expandWSelectProject = boProject.expandSelect(context,relPattern.getPattern(),typePattern.getPattern(),
                                                        typeselectList,new StringList(),false, true, (short)1);
             boProject.close(context);

             RelationshipWithSelectItr relWSelectProjectVaultItr = new RelationshipWithSelectItr(expandWSelectProject.getRelationships());

             while(relWSelectProjectVaultItr.next()) {
               Hashtable vaultHashTable    = relWSelectProjectVaultItr.obj().getTargetData();
               String strVaultName         = (String)vaultHashTable.get("name");
               String strVaultDescription  = (String)vaultHashTable.get("description");

               if(hashCategoriesTable.containsKey(strVaultName)) {
                 hashCategoriesTable.remove(strVaultName);
               }
             }

             templateObj.setId(strFolderId);


             if ( hashCategoriesTable != null ){
               categoriesEnum = hashCategoriesTable.keys();
             }
             String strFolderName="";
        
             bFolderListEmpty = true;
               // Getting all the Folderds in the Which is connected to the project
             while( categoriesEnum.hasMoreElements()){
               bFolderListEmpty = false;
               String strCategoryName = (String)categoriesEnum.nextElement();
               String strCategoryDescription  = (String)hashCategoriesTable.get(strCategoryName);

               Hashtable hashCategoriesTableFinal = new Hashtable();
               hashCategoriesTableFinal.put(templateObj.SELECT_NAME, strCategoryName);
               hashCategoriesTableFinal.put(templateObj.SELECT_ID, strCategoryName+"|"+strCategoryDescription);
               hashCategoriesTableFinal.put(templateObj.SELECT_DESCRIPTION, strCategoryDescription);

               templateMapList.add(hashCategoriesTableFinal);
             }

             sParams = "objectId="+strFolderId;
        

           // pass the resultList to the following method
          // emxPage.getTable().setObjects(templateMapList);
           // pass in the selectables to the following method
          // emxPage.getTable().setSelects(new StringList());
         //}

        // templateMapList = emxPage.getTable().evaluate(context, emxPage.getCurrentPage());
             return templateMapList;
       }
       /**
        * getFolderNames - method to return the name of predefined folders
        * @param context the eMatrix <code>Context</code> object
        * @return Vector
        * @throws Exception if the operation fails
        * @since R210
        * @grade 0
        */  
   public Vector getFolderNames(Context context, String[] args) throws Exception
   {
       Vector folderNames = new Vector();
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Iterator itr = objectList.iterator();
       String folderIcon = "<img src='images/iconSmallFolder.gif' border='0' id=''/>&nbsp;";
       while(itr.hasNext()){
           Map map = (Map)itr.next();
           StringBuffer buffer = new StringBuffer(100);
           buffer.append(folderIcon).append(XSSUtil.encodeForHTML(context,(String)map.get("name")));
           folderNames.add(buffer.toString());
       }
       return folderNames;
   }
   /**
    * getFolderDesc - method to return the description of the folder in a workspace
    * @param context the eMatrix <code>Context</code> object
    * @return Vector
    * @throws Exception if the operation fails
    * @since R210
    * @grade 0
    */
   public Vector getFolderDesc(Context context, String[] args) throws Exception
   {
       Vector folderNames = new Vector();
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList objectList = (MapList)programMap.get("objectList");
       Iterator itr = objectList.iterator();
       while(itr.hasNext())
       {
           Map map = (Map)itr.next();
           String name = (String)map.get("description");
           folderNames.add(XSSUtil.encodeForHTML(context, name));
       }
       return folderNames;
   }
   @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
   public StringList getWorkspaceFolderChangeOwnerInclusionIDs(Context context, String[] args) throws FrameworkException {
       try {
           Map programMap = (Map) JPO.unpackArgs(args);
           String folderId = (String) programMap.get("objectId");
           String workspaceId = getProjectId(context,folderId);
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
    * getContentRemoveOptionRange - method to return range values for team delete content Action Form
    * @param context the eMatrix <code>Context</code> object
    * @return Map
    * @throws Exception if the operation fails
    * @since R212
    * @grade 0
    */
   
   public Map getContentRemoveOptionRange(Context context, String[] args) throws FrameworkException {

       try {
           Map programMap = (Map)JPO.unpackArgs(args);
           Map paramMap   = (Map)programMap.get("paramMap");
           String sLanguage = (String) paramMap.get("languageStr");
           
           StringList values = new StringList(3);
           StringList displayValues = new StringList(3);
           
           StringBuffer message = null;
           
           boolean bActivateDSFA= FrameworkUtil.isSuiteRegistered(context,"ActivateDSFA",false,null,null);
           i18nNow i18Now = new i18nNow();
           
           values.add("leaveIn");
           String bundle = "emxTeamCentralStringResource";
           displayValues.add(i18Now.getI18nString("emxTeamCentral.DeleteContent.FolderDeleteMessage", bundle, sLanguage));
           
           values.add("takeOut");
           message = new StringBuffer(100);
           message.append(i18Now.getI18nString("emxTeamCentral.DeleteContent.DatabaseMessage", bundle, sLanguage));
           if(bActivateDSFA) {
               message.append(" ");
               message.append(i18Now.getI18nString("emxTeamCentral.DeleteContent.VCDatabaseMessage", bundle, sLanguage));
           }
           displayValues.add(message.toString());
           
           values.add("prior");
           message = new StringBuffer(100);
           message.append(i18Now.getI18nString("emxTeamCentral.DeleteContent.RestoreMessage", bundle, sLanguage));
           if(bActivateDSFA) {
               message.append(" ");
               message.append(i18Now.getI18nString("emxTeamCentral.DeleteContent.VCRestoreMessage", bundle, sLanguage));
           }
           displayValues.add(message.toString());
           
           
           HashMap resultMap = new HashMap();
           resultMap.put("field_choices", values);
           resultMap.put("field_display_choices", displayValues);
           return resultMap;
           
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   
   /**
    * removeContentFromFolderPreProcess - method to return range values for team delete content Action Form
    * @param context the eMatrix <code>Context</code> object
    * @return Map
    * @throws Exception if the operation fails
    * @since R212
    * @grade 0
    */
   @com.matrixone.apps.framework.ui.PreProcessCallable
   
   public Map removeFolderContentPreProcess(Context context, String[] args) throws FrameworkException {
       
       try {
           Map programMap = (Map)JPO.unpackArgs(args);
           Map paramMap   = (Map)programMap.get("paramMap");
           Map requestMap   = (Map)programMap.get("requestMap");
           String sLanguage = (String) requestMap.get("languageStr");
           Map actionMap = new HashMap();
           String documentIds   = (String)requestMap.get("rowIds");          
           String documentId [] = ComponentsUIUtil.stringToArray(documentIds, ",");
           
           StringBuffer sbSelRtId   = new StringBuffer("from[");
           sbSelRtId.append(DomainObject.RELATIONSHIP_OBJECT_ROUTE);
           sbSelRtId.append("].to.id");
           
           StringBuffer sbLockedSelect = new StringBuffer(56);
           sbLockedSelect.append("relationship[");
           sbLockedSelect.append(CommonDocument.RELATIONSHIP_ACTIVE_VERSION);
           sbLockedSelect.append("].to.locked");
           
           StringList  selDocumentList   = new StringList();
           selDocumentList.add(sbSelRtId.toString());
           selDocumentList.add(sbLockedSelect.toString());
           selDocumentList.add(CommonDocument.SELECT_VCFILE_LOCKED);
           selDocumentList.add(DomainObject.SELECT_ID);
           selDocumentList.add(DomainObject.SELECT_CURRENT);
           
           DomainConstants.MULTI_VALUE_LIST.add(sbSelRtId.toString());           
           MapList accessMapList = DomainObject.getInfo(context,documentId,selDocumentList);
           DomainConstants.MULTI_VALUE_LIST.remove(sbSelRtId.toString());  
           
           Iterator accessMapItr = accessMapList.iterator();
           while(accessMapItr.hasNext())
           {
               
               Map accessMap          = (Map)accessMapItr.next();
               StringList docRouteId  = (StringList)accessMap.get(sbSelRtId.toString());
               String docLocked       = (String)accessMap.get(sbLockedSelect.toString());
               
               String vcDocLocked   = (String)accessMap.get(CommonDocument.SELECT_VCFILE_LOCKED);
               
               String errorMsgKey = null;
               
               String currentState = (String)accessMap.get(DomainObject.SELECT_CURRENT);
               
               if("RELEASED".equalsIgnoreCase(currentState)){
         		   errorMsgKey = "emxTeamCentral.ContentSummary.AlertCannotRemoveReleasedStateObject";
         	   } else if(!UIUtil.isNullOrEmpty(docLocked) && docLocked.indexOf("TRUE")!=-1 || !UIUtil.isNullOrEmpty(vcDocLocked) && vcDocLocked.indexOf("TRUE")!=-1) {
                   errorMsgKey = "emxTeamCentral.ContentSummary.AlertCannotRemoveLockedContent";                 
               } else if(isContentWithRouteScope(context, docRouteId)){
                   errorMsgKey = "emxTeamCentral.ContentSummary.AlertCannotRemoveContent";
               } 
               
               if(errorMsgKey != null){
                   errorMsgKey = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(sLanguage),errorMsgKey);
                   actionMap.put("Action","STOP");
                   actionMap.put("Message",errorMsgKey);  
                   return actionMap;
               }
               
           }
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
       return null;
   }

 
   private boolean isContentWithRouteScope(Context context, StringList routeIdList) throws FrameworkException {
       if(routeIdList == null || routeIdList.size()<=0){
           return false;
       }
       
       String []routeObjectArray = ComponentsUIUtil.stringToArray(FrameworkUtil.join(routeIdList,","), ",");
       
       StringList selectRouteList = new StringList();
       String routeStatus = PropertyUtil.getSchemaProperty(context,"attribute_RouteStatus");
       selectRouteList.add("to["+DomainObject.RELATIONSHIP_ROUTE_SCOPE+"]");
       selectRouteList.add("attribute["+routeStatus+"]");
       MapList routeObjectMapList = DomainObject.getInfo(context,routeObjectArray,selectRouteList);
       
       Iterator routeObjectMapListItr = routeObjectMapList.iterator();
       while(routeObjectMapListItr.hasNext()){
           Map strResult = (Map)routeObjectMapListItr.next();
           routeStatus = (String) strResult.get("attribute["+routeStatus+"]");
           String relRouteScope = (String) strResult.get("to["+DomainObject.RELATIONSHIP_ROUTE_SCOPE+"]");
           if(relRouteScope != null && "TRUE".equalsIgnoreCase(relRouteScope) && !("Finished".equalsIgnoreCase(routeStatus))){
               return true;
           }
       }
       return false;
   }
   
   @com.matrixone.apps.framework.ui.CreateProcessCallable
   public Map createWorkspaceFolderProcess(Context context,String[] args) throws Exception
   {
	   HashMap retMap = new HashMap();
	   Map programMap = (Map) JPO.unpackArgs(args);
       HashMap paramMap   = (HashMap)programMap.get("paramMap");
       String strProjectId   = (String) programMap.get("objectId");
       String accessType   = (String) programMap.get("AccessType");
       String strFolderName  = (String) programMap.get("NameDisplay");
       String strFolderDesp  = (String) programMap.get("Description");
       String languageStr    = (String) programMap.get("languageStr");

       String strPolicyProjectVault    = PropertyUtil.getSchemaProperty(context, "policy_ProjectVault");
       String strRelProjectVaults      = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");
       String strTypefolder            = PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
       boolean isDSCInstalled		   = FrameworkUtil.isSuiteRegistered(context,"appVersionDesignerCentral",false,null,null);

       boolean bExists = false;       
       String folderId = "";

       try{
    	 ContextUtil.startTransaction(context, true);
         BusinessObject boProject            = new BusinessObject(strProjectId);
         boProject.open(context);
         Workspace workspace        = (Workspace) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE,DomainConstants.TEAM);
         workspace.setId(strProjectId);
         StringList selects = new StringList(3);
         selects.add("physicalid");
         selects.add("project");
         selects.add("organization");
         selects.add("vault");
         Map workspaceData = workspace.getInfo(context, selects);
         String strProjectVault = (String)workspaceData.get("vault");
         String strProjectRevision = (String)workspaceData.get("physicalid");
         String project = (String)workspaceData.get("project");
         String org = (String)workspaceData.get("organization");
         String strFolderOwnership = "context"; 
         if (workspace.isFolderExists(context, strFolderName)) {
           bExists = true;
           String i18NCategory = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource", new Locale(languageStr),"emxTeamCentral.AddCategories.Category");
           String i18NNotUnique = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource", new Locale(languageStr),"emxTeamCentral.AddCategories.NotUnique");
           retMap.put("ErrorMessage", i18NCategory +"  "+ strFolderName +"  "+ i18NNotUnique);
           return retMap;
         }
         if(!bExists) {
        	 WorkspaceVault wVault =  new WorkspaceVault();
        	 wVault.createObject(context, strTypefolder, strFolderName, strProjectRevision, strPolicyProjectVault, context.getVault().getName());
        	 try {
        		 strFolderOwnership = EnoviaResourceBundle.getProperty(context,"enoFolderManagement.FolderOwnership");
        	 } catch (Exception ex)
        	 {
        		 //Do Nothing
        	 }
        	 if(!"context".equalsIgnoreCase(strFolderOwnership))        		 
        	 {
        		 if(UIUtil.isNullOrEmpty(project) && UIUtil.isNullOrEmpty(org) ) {
        			 wVault.removePrimaryOwnership(context);
        		 } else {
        		     wVault.setPrimaryOwnership(context, project, org);
        		 }
        	 }
        		 
        	 
        	 if(UIUtil.isNotNullAndNotEmpty(accessType)){
            	 wVault.setAttributeValue(context, DomainConstants.ATTRIBUTE_ACCESS_TYPE,accessType);
        	 }
        	 folderId = wVault.getObjectId();
        	 String sPersonId = com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context);
        	 
        	 if(WorkspaceVault.ATTRIBUTE_ACCESSTYPE_SPECIFIC.equals(accessType)){
	        	 //Removing since the ownership will get created automatically when the DataVault rel is created between the Workspace and the folder at line 2812.
				 //DomainAccess.createObjectOwnership(context, folderId, sPersonId, DomainAccess.getOwnerAccessName(context, folderId), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
	             if(WorkspaceVault.isDefaultWSOaccessGrantEnabled(context)){
		     		 String workspaceOwner = workspace.getInfo(context,DomainConstants.SELECT_OWNER);
		     		String sUserId = com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context, workspaceOwner);
	            	 if(UIUtil.isNotNullAndNotEmpty(workspaceOwner) && !(workspaceOwner.equals(context.getUser()))){
	            	 DomainAccess.createObjectOwnership(context, folderId,sUserId, DomainAccess.getOwnerAccessName(context, folderId), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);	             
	            	 }
	             }
        	 }
        	 wVault.connect(context, strRelProjectVaults, (DomainObject) workspace, true);
        	 SubscriptionManager subscriptionMgr = workspace.getSubscriptionManager();
        	 String treeMenu = "type_ProjectVault";
             subscriptionMgr.publishEvent(context, workspace.EVENT_FOLDER_CREATED, folderId);
         }
         boProject.close(context);
         ContextUtil.commitTransaction(context);
       }  
       catch(Exception e){
    	   ContextUtil.abortTransaction(context);
    	   String errorMsg = e.getMessage();
    	   if(errorMsg.contains("ORA-12899") || errorMsg.contains("ORA-01461")){
    		   throw new FrameworkException(errorMsg);
    	   }   	  
       }
       retMap.put("id", folderId);
	   return retMap;
   }
   
   /**
    * To get the range values for Inherit Access field in WorkspaceVault create page
    * @param context the eMatrix <code>Context</code> object
    * @param args
    * @return Map
    * @throws Exception if the operation fails
    * @since R214.HF12
    * @grade 0
    */ 
	public Map getRangeValuesFolderAccessTypeCreateForm(Context context, String[] args)throws Exception {
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String languageStr = (String) requestMap.get("languageStr");
	    
		Map returnMap = new HashMap();
	    StringList rangeAccessTypeDisplay = new StringList();
	    StringList rangeAccessTypeActual = new StringList();
	    //StringList selectedValues = new StringList();
	    
	    String strAttributeName = DomainConstants.ATTRIBUTE_ACCESS_TYPE;
	    matrix.db.AttributeType attribName = new matrix.db.AttributeType(strAttributeName);
	    attribName.open(context);
	    // actual range values
	    List attributeRange = attribName.getChoices();
	    for (int i = 0; i < attributeRange.size(); i++) {
	        if(WorkspaceVault.ATTRIBUTE_ACCESSTYPE_SPECIFIC.equals(attributeRange.get(i))){
	        	rangeAccessTypeDisplay.add(EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(languageStr),"emxTeamCentral.Common.No"));
	        	
	        	rangeAccessTypeActual.add(attributeRange.get(i));     
	        }else{
	        	rangeAccessTypeDisplay.add(EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(languageStr),"emxTeamCentral.Common.Yes"));
	        	rangeAccessTypeActual.add(attributeRange.get(i));     
	        	//selectedValues.add(attributeRange.get(i));
	        }                  
	    }    
	    returnMap.put("field_choices", rangeAccessTypeActual);
	    returnMap.put("field_display_choices", rangeAccessTypeDisplay);
	    //returnMap.put("field_value", selectedValues);
	    attribName.close(context);
	    return returnMap;
	}
	
	   /**
	    * To get the Inherit Access field value for properties page
	    * @param context the eMatrix <code>Context</code> object
	    * @param args
	    * @return String
	    * @throws Exception if the operation fails
	    * @since R214.HF12
	    * @grade 0
	    */ 
	public String getFolderAccessTypeValue(Context context, String[] args) throws Exception
	{
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String strObjectId = (String) requestMap.get("objectId");
	    String languageStr = (String) requestMap.get("languageStr");
	
	    // String mode = (String) requestMap.get("mode"); Not required Currently
	    String sAccessType = "";
	    StringList busSelect = new StringList(1);
	    busSelect.add(WorkspaceVault.SELECT_ACCESS_TYPE);
	
	    if(strObjectId != null){
	    DomainObject dom = DomainObject.newInstance(context, strObjectId);
	    Map objectMap = dom.getInfo(context, busSelect);
	
	    String strAccessType =(String) objectMap.get(WorkspaceVault.SELECT_ACCESS_TYPE);
	
		    if (WorkspaceVault.ATTRIBUTE_ACCESSTYPE_SPECIFIC.equalsIgnoreCase(strAccessType))
		    {
		    	sAccessType = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(languageStr),"emxTeamCentral.Common.No");

		    }else{
		    	sAccessType = EnoviaResourceBundle.getProperty(context,"emxTeamCentralStringResource",new Locale(languageStr),"emxTeamCentral.Common.Yes");

		    }
	    }
	    return sAccessType;
	}
	/*
	 * IncludeOID method for MOP AddMember action command
	 * */
	   @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
 public StringList getWorkspaceVaultAddMemberIncludeIDs(Context context, String[] args) throws Exception 
	{
     HashMap programMap         = (HashMap) JPO.unpackArgs(args);
     String objectId            = (String) programMap.get("objectId");
     
     return emxDomainAccessBase_mxJPO.getInclusionList(context, WorkspaceVault.getWorkspaceVaultParentId(context,objectId), "Person");
	}
	/*
	 * IncludeOID method for MOP AddProject action command
	 * */
 @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
 public StringList getWorkspaceVaultAddProjectIncludeIDs(Context context, String[] args) throws Exception 
	{
     HashMap programMap         = (HashMap) JPO.unpackArgs(args);
     String objectId            = (String) programMap.get("objectId");
     
     return emxDomainAccessBase_mxJPO.getInclusionList(context, WorkspaceVault.getWorkspaceVaultParentId(context,objectId), "CollabSpace");
	}
	/*
	 * IncludeOID method for MOP AddOrganization action command
	 * */
 @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
 public StringList getWorkspaceVaultAddOrganizationIncludeIDs(Context context, String[] args) throws Exception 
	{
     HashMap programMap         = (HashMap) JPO.unpackArgs(args);
     String objectId            = (String) programMap.get("objectId");

     return emxDomainAccessBase_mxJPO.getInclusionList(context, WorkspaceVault.getWorkspaceVaultParentId(context,objectId), "Org");   

	}
	/*
	 * IncludeOID method for MOP AddSecurityContext action command
	 * */
 @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
 public StringList getWorkspaceVaultAddSecurityContextIncludeIDs(Context context, String[] args) throws Exception 
	{
     HashMap programMap         = (HashMap) JPO.unpackArgs(args);
     String objectId            = (String) programMap.get("objectId");
  
     return emxDomainAccessBase_mxJPO.getInclusionSecurityContextList(context, WorkspaceVault.getWorkspaceVaultParentId(context,objectId));   
	}   
 
 
 
 /**To remove a Security context ownership from WorkspaceVault
  * @param context
  * @param args
  * @since BPSHF 2013x.HF12
  * @throws Exception
  */

     @SuppressWarnings("deprecation")
 public void deleteWorkspaceFolderAccess(Context context, String[] args) throws Exception
 {

     try
     {
         HashMap<?, ?> paramMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
         String[] ids = (String[])paramMap.get("emxTableRowIds");
         for(int i =0; i<ids.length;i++)
         {
             StringList idList = com.matrixone.apps.domain.util.StringUtil.split(ids[i], "|");
             if( idList.size() >2 )
             {
                 String busId = (String)idList.get(1);
                 String ownerName = "";
                 String strProject = "";
                 String strOrganization = "";
                 DomainObject domainObject = new DomainObject();
                 if (busId !=null && !"".equals(busId))
                 {
                 	domainObject.setId(busId);
                 }
                 domainObject.open(context);
                 ownerName = domainObject.getOwner(context).getName();
                 String typeName = domainObject.getTypeName();
                 domainObject.close(context);
                 String secContextId = (String)idList.get(0);
                 int fIndex = -1,sIndex = -1,tIndex = -1;
                 fIndex =secContextId.indexOf(":");
                 if (fIndex != -1)
                 {
                   sIndex =secContextId.indexOf(":", fIndex+1);
                 }
                 if (sIndex != -1)
                 {
                   tIndex =secContextId.indexOf(":", sIndex+1);
                 }
                 if (fIndex != -1 && sIndex != -1)
                 {
                   strOrganization = secContextId.substring(fIndex+1, sIndex);
                 }
                 if (sIndex != -1 && tIndex != -1)
                 {
                   strProject = secContextId.substring(sIndex+1, tIndex);
                 }
                 boolean ownerCheck = false;
                 if (strProject.length() !=0)
                 {
                     ownerCheck = strProject.contains(ownerName);
                 }
                 if(strProject != null && !"".equals(strProject) )
                 {
                     String cmd = "print role $1 select $2 dump";
                     String result = MqlUtil.mqlCommand(context, cmd, strProject,"person");
                     if( context.getUser().equals(result))
                     {
                       ownerCheck = true;
                   }
                 }
                 if (ownerCheck)
       {
         String languageStr = context.getSession().getLanguage();
         String exMsg = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(languageStr),"emxFramework.Message.CannotDeletetheOwner");
         throw new Exception(exMsg);
                 } else {
                 String strComments = secContextId.substring(tIndex+1);
                 	WorkspaceVault.updateSpecificWorkspaceVaultOwnership(context, busId, strOrganization, strProject, strComments, "DELETE");
                 }
             }
         }
     }
     catch(Exception e)
     {
         emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
     }
 }
     /**
      * Update Folder Path and Folder Classification attributes from Parent folder.
      *
      * @param context the eMatrix Context object
      * @param args[] of From Object Id as first parameter and To Object Id as second parameter
      * @return void
      * @throws Exception if the operation fails
      * @since TMC V6R2015x
      * @grade 0
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void updateDisplayOnFilter(matrix.db.Context context, String[] args ) throws Exception
     {
    	 try
    	 {
    		 ContextUtil.startTransaction(context, true);
    		 String contentType = args[3];
    		 String command = "print type $1 select $2 dump";
    		 String primaryRels = MqlUtil.mqlCommand(context, command, contentType, "property[Primary Relationships].value");
    		 if(primaryRels != null && !"".equals(primaryRels) && !"null".equals(primaryRels) )
    		 {
    			 String ATTRIBUTE_DISPLAY_ON_FILTER = PropertyUtil.getSchemaProperty(context, "attribute_DisplayOnFilter");
    			 String relationshipVaultedObejcts = PropertyUtil.getSchemaProperty(context, "relationship_VaultedDocuments");
    			 DomainObject folderObject = new DomainObject(args[0]);
    			 DomainObject contentObject = new DomainObject(args[1]);
    			 String relId = args[2];
    			 StringList selects = new StringList(2);
    			 selects.addElement(SELECT_ID);
    			 selects.addElement(SELECT_ID);
    			 StringList relSelects = new StringList(2);
    			 relSelects.addElement(SELECT_RELATIONSHIP_ID);
    			 String totalRels = "";
    			 StringList primaryRelList = StringUtil.split(primaryRels, ",");
    			 String sRelWhere = "";
    			 for (int i=0; i<primaryRelList.size(); i++)
    			 {
    				 String relName = (String)primaryRelList.get(i);

    				 String relWhere = null;
    				 if( relName.contains("|"))
    				 {
    					 StringList relDetails = StringUtil.split(relName, "|");
    					 relName = (String)relDetails.get(0);
    					 relWhere = (String)relDetails.get(1);
    				 }
    				 if( !"".equals(totalRels) )
    				 {
    					 totalRels +=  "," + relName;
    					 if(relWhere != null) {
    						 sRelWhere += "|| (name == \"" + relName + "\" && "+ relWhere +")";
    					 } else {
    						 sRelWhere += " || name == \"" + relName + "\"";
    					 }
    				 } else {
    					 totalRels += relName;
    					 if(relWhere != null) {
    						 sRelWhere += "(name == \"" + relName + "\" && "+ relWhere +")";
    					 } else {
    						 sRelWhere += "name == \"" + relName + "\"";
    					 }
    				 }
    			 }
    			 MapList childMapList = contentObject.getRelatedObjects(context, totalRels, "*",  selects, null,
    					 false, true, (short)0, null, sRelWhere, (int)0, null, null, null);
    			 StringList childIds = new StringList(childMapList.size());
    			 Iterator childItr = childMapList.iterator();
    			 while(childItr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)childItr.next();
    				 childIds.add(m.get(SELECT_ID));
    			 }
    			 MapList parentMapList = contentObject.getRelatedObjects(context, totalRels, "*",  selects, null,
    					 true, false, (short)0, null, sRelWhere, (int)0, null, null, null);
    			 StringList parentIds = new StringList(childMapList.size());
    			 Iterator parentItr = parentMapList.iterator();
    			 while(parentItr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)parentItr.next();
    				 parentIds.add(m.get(SELECT_ID));
    			 }
    			 MapList contentMapList = folderObject.getRelatedObjects(context, relationshipVaultedObejcts, "*",  selects, relSelects,
    					 false, true, (short)0, null, null, (int)0, null, null, null);
    			 Iterator contentItr = contentMapList.iterator();
    			 while(contentItr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)contentItr.next();
    				 String contentId = m.get(SELECT_ID);
    				 if( parentIds.contains(contentId))
    				 {
    					 DomainRelationship.setAttributeValue(context, relId, ATTRIBUTE_DISPLAY_ON_FILTER, "False");
    				 } else if( childIds.contains(contentId))
    				 {
    					 DomainRelationship.setAttributeValue(context, m.get(SELECT_RELATIONSHIP_ID), ATTRIBUTE_DISPLAY_ON_FILTER, "False");
    				 }
    			 }
    		 }
    		 ContextUtil.commitTransaction(context);
    	 }catch(Exception ex)
    	 {
    		 ContextUtil.abortTransaction(context);
    		 ex.printStackTrace();
    		 throw ex;
    	 }
     }


     /**
      * Update Folder Path and Folder Classification attributes from Parent folder.
      *
      * @param context the eMatrix Context object
      * @param args[] of From Object Id as first parameter and To Object Id as second parameter
      * @return void
      * @throws Exception if the operation fails
      * @since TMC V6R2015x
      * @grade 0
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void updateDisplayOnFilterOnDisconnect(matrix.db.Context context, String[] args ) throws Exception
     {
    	 try
    	 {
    		 ContextUtil.startTransaction(context, true);
    		 String contentType = args[2];
    		 String command = "print type $1 select $2 dump";
    		 String primaryRels = MqlUtil.mqlCommand(context, command, contentType, "property[Primary Relationships].value");
    		 if(primaryRels != null && !"".equals(primaryRels) && !"null".equals(primaryRels) )
    		 {
    			 String ATTRIBUTE_DISPLAY_ON_FILTER = PropertyUtil.getSchemaProperty(context, "attribute_DisplayOnFilter");
    			 String relationshipVaultedObejcts = PropertyUtil.getSchemaProperty(context, "relationship_VaultedDocuments");
    			 DomainObject folderObject = new DomainObject(args[0]);
    			 DomainObject contentObject = new DomainObject(args[1]);
    			 StringList selects = new StringList(2);
    			 selects.addElement(SELECT_ID);
    			 selects.addElement(SELECT_NAME);
    			 StringList relSelects = new StringList(2);
    			 relSelects.addElement(SELECT_RELATIONSHIP_ID);

    			 String totalRels = "";
    			 StringList primaryRelList = StringUtil.split(primaryRels, ",");
    			 String sRelWhere = "";
    			 for (int i=0; i<primaryRelList.size(); i++)
    			 {
    				 String relName = (String)primaryRelList.get(i);

    				 String relWhere = null;
    				 if( relName.contains("|"))
    				 {
    					 StringList relDetails = StringUtil.split(relName, "|");
    					 relName = (String)relDetails.get(0);
    					 relWhere = (String)relDetails.get(1);
    				 }
    				 if( !"".equals(totalRels) )
    				 {
    					 totalRels +=  "," + relName;
    					 if(relWhere != null) {
    						 sRelWhere += "|| (name == \"" + relName + "\" && "+ relWhere +")";
    					 } else {
    						 sRelWhere += " || name == \"" + relName + "\"";
    					 }
    				 } else {
    					 totalRels += relName;
    					 if(relWhere != null) {
    						 sRelWhere += "(name == \"" + relName + "\" && "+ relWhere +")";
    					 } else {
    						 sRelWhere += "name == \"" + relName + "\"";
    					 }
    				 }
    			 }



    			 MapList childMapList = contentObject.getRelatedObjects(context, totalRels, "*",  selects, null,
    					 false, true, (short)0, null, sRelWhere, (int)0, null, null, null);
    			 MapList contentMapList = folderObject.getRelatedObjects(context, relationshipVaultedObejcts, "*",  selects, relSelects,
    					 false, true, (short)0, null, null, (int)0, null, null, null);
    			 Map<String, String> contentMap = new HashMap<String, String>();
    			 StringList contentIds = new StringList(contentMapList.size());
    			 Iterator contentItr = contentMapList.iterator();
    			 while(contentItr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)contentItr.next();
    				 String contentId = m.get(SELECT_ID);
    				 String relId = m.get(SELECT_RELATIONSHIP_ID);
    				 contentMap.put(contentId, relId);
    				 contentIds.addElement(contentId);
    			 }
    			 Iterator childItr = childMapList.iterator();
    			 int baseLevel = 1;
    			 boolean changeRel = true;
    			 while(childItr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)childItr.next();
    				 String oid = m.get(SELECT_ID);
    				 String olevel = (String)m.get("level");
    				 int objLevel = Integer.parseInt(olevel);
    				 if( baseLevel == objLevel)
    				 {
    					 changeRel = true;
    				 }
    				 if( contentIds.contains(oid))
    				 {
    					 if(changeRel)
    					 {
    						 changeRel = false;
    						 DomainRelationship.setAttributeValue(context, contentMap.get(oid), ATTRIBUTE_DISPLAY_ON_FILTER, "True");
    					 }
    				 }
    			 }
    		 }
    		 ContextUtil.commitTransaction(context);
    	 }catch(Exception ex)
    	 {
    		 ContextUtil.abortTransaction(context);
    		 ex.printStackTrace();
    		 throw ex;
    	 }
     }

     /**
      * Update Folder Path and Folder Classification attributes from Parent folder.
      *
      * @param context the eMatrix Context object
      * @param args[] of From Object Id as first parameter and To Object Id as second parameter
      * @return void
      * @throws Exception if the operation fails
      * @since TMC V6R2015x
      * @grade 0
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void updateDisplayOnFilterOnMove(matrix.db.Context context, String[] args ) throws Exception
     {
    	 try
    	 {
    		 ContextUtil.startTransaction(context, true);
    		 String contentType = args[4];
    		 String command = "print type $1 select $2 dump";
    		 String primaryRels = MqlUtil.mqlCommand(context, command, contentType, "property[Primary Relationships].value");
    		 if(primaryRels != null && !"".equals(primaryRels) && !"null".equals(primaryRels) )
    		 {
    			 String ATTRIBUTE_DISPLAY_ON_FILTER = PropertyUtil.getSchemaProperty(context, "attribute_DisplayOnFilter");
    			 String relationshipVaultedObejcts = PropertyUtil.getSchemaProperty(context, "relationship_VaultedDocuments");
    			 DomainObject newFolderObject = new DomainObject(args[0]);
    			 DomainObject oldFolderObject = new DomainObject(args[1]);
    			 DomainObject contentObject = new DomainObject(args[2]);
    			 String relId = args[3];
    			 StringList selects = new StringList(2);
    			 selects.addElement(SELECT_ID);
    			 selects.addElement(SELECT_NAME);
    			 StringList relSelects = new StringList(2);
    			 relSelects.addElement(SELECT_RELATIONSHIP_ID);


    			 String totalRels = "";
    			 StringList primaryRelList = StringUtil.split(primaryRels, ",");
    			 String sRelWhere = "";
    			 for (int i=0; i<primaryRelList.size(); i++)
    			 {
    				 String relName = (String)primaryRelList.get(i);

    				 String relWhere = null;
    				 if( relName.contains("|"))
    				 {
    					 StringList relDetails = StringUtil.split(relName, "|");
    					 relName = (String)relDetails.get(0);
    					 relWhere = (String)relDetails.get(1);
    				 }
    				 if( !"".equals(totalRels) )
    				 {
    					 totalRels +=  "," + relName;
    					 if(relWhere != null) {
    						 sRelWhere += "|| (name == \"" + relName + "\" && "+ relWhere +")";
    					 } else {
    						 sRelWhere += " || name == \"" + relName + "\"";
    					 }
    				 } else {
    					 totalRels += relName;
    					 if(relWhere != null) {
    						 sRelWhere += "(name == \"" + relName + "\" && "+ relWhere +")";
    					 } else {
    						 sRelWhere += "name == \"" + relName + "\"";
    					 }
    				 }
    			 }


    			 MapList childMapList = contentObject.getRelatedObjects(context, totalRels, "*",  selects, null,
    					 false, true, (short)0, null, sRelWhere, (int)0, null, null, null);
    			 StringList childIds = new StringList(childMapList.size());
    			 Iterator itr = childMapList.iterator();
    			 while(itr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)itr.next();
    				 childIds.add(m.get(SELECT_ID));
    			 }
    			 MapList parentMapList = contentObject.getRelatedObjects(context, totalRels, "*",  selects, null,
    					 true, false, (short)0, null, sRelWhere, (int)0, null, null, null);
    			 StringList parentIds = new StringList(childMapList.size());
    			 itr = parentMapList.iterator();
    			 while(itr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)itr.next();
    				 parentIds.add(m.get(SELECT_ID));
    			 }
    			 MapList newContentMapList = newFolderObject.getRelatedObjects(context, relationshipVaultedObejcts, "*",  selects, relSelects,
    					 false, true, (short)0, null, null, (int)0, null, null, null);
    			 itr = newContentMapList.iterator();
    			 while(itr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)itr.next();
    				 String contentId = m.get(SELECT_ID);
    				 if( parentIds.contains(contentId))
    				 {
    					 DomainRelationship.setAttributeValue(context, relId, ATTRIBUTE_DISPLAY_ON_FILTER, "False");
    				 } else if( childIds.contains(contentId))
    				 {
    					 DomainRelationship.setAttributeValue(context, m.get(SELECT_RELATIONSHIP_ID), ATTRIBUTE_DISPLAY_ON_FILTER, "False");
    				 }
    			 }
    			 MapList oldContentMapList = oldFolderObject.getRelatedObjects(context, relationshipVaultedObejcts, "*",  selects, relSelects,
    					 false, true, (short)0, null, null, (int)0, null, null, null);
    			 Map<String, String> contentMap = new HashMap<String, String>();
    			 StringList contentIds = new StringList(oldContentMapList.size());
    			 itr = oldContentMapList.iterator();
    			 while(itr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)itr.next();
    				 String contentId = m.get(SELECT_ID);
    				 String rid = m.get(SELECT_RELATIONSHIP_ID);
    				 contentMap.put(contentId, rid);
    				 contentIds.addElement(contentId);
    			 }
    			 itr = childMapList.iterator();
    			 int baseLevel = 1;
    			 boolean changeRel = true;
    			 while(itr.hasNext())
    			 {
    				 Map<String,String> m = (Map<String,String>)itr.next();
    				 String oid = m.get(SELECT_ID);
    				 String olevel = (String)m.get("level");
    				 int objLevel = Integer.parseInt(olevel);
    				 if( baseLevel == objLevel)
    				 {
    					 changeRel = true;
    				 }
    				 if( contentIds.contains(oid))
    				 {
    					 if(changeRel)
    					 {
    						 changeRel = false;
    						 DomainRelationship.setAttributeValue(context, contentMap.get(oid), ATTRIBUTE_DISPLAY_ON_FILTER, "True");
    					 }
    				 }
    			 }
    		 }
    		 ContextUtil.commitTransaction(context);
    	 }catch(Exception ex)
    	 {
    		 ContextUtil.abortTransaction(context);
    		 //ex.printStackTrace();
    		 throw ex;
    	 }
     }
     public void preventChangeOwnerForPersonalFolders(Context context, String[] args) throws Exception
     {
    	 String oid = args[0];
         DomainObject object = new DomainObject(oid);
         String  folderClassification = object.getInfo(context, "attribute[Folder Classification]");
         if("Personal".equals(folderClassification))
         {
        	 String error = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource",
 					context.getLocale(), "emxTeamCentral.PersonalFolders.ChangeOwnerError");
        	 throw new Exception(error);
    	 }
     }

}
