/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 */

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Access;
import matrix.db.AccessItr;
import matrix.db.AccessList;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.VaultedObjectsAccessUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxWorkspaceConstantsBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     * The constructor initializes the static _documentTypes depending on values in the properties file.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxWorkspaceConstantsBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
        synchronized(emxWorkspaceConstantsBase_mxJPO.class) {
            if (_documentTypes == null)
                {
                    _documentTypes = VaultedObjectsAccessUtil.getFolderContentTypesRequireGrants(context);
                }
        }
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
  /**
   * Route Access Grantor user name
   */
  static final String AEF_ROUTE_ACCESS_GRANTOR_USERNAME = "Route Access Grantor";

  /**
  * Type constants
  *
  **/
  /** type "Person" */
  static final String typePerson = PropertyUtil.getSchemaProperty("type_Person");
  /** type "Project Member" */
  static final String typeProjectMember = PropertyUtil.getSchemaProperty("type_ProjectMember");
  /** type "Workspace" */
  static final String typeWorkspace = PropertyUtil.getSchemaProperty("type_Project");
  /** type "Project Vault" */
  static final String typeProjectVault = PropertyUtil.getSchemaProperty("type_ProjectVault");
  /** type "Document" */
  static final String typeDocument = PropertyUtil.getSchemaProperty("type_Document");
  /** type "Request For Quote" */
  static final String typeRequestForQuote = PropertyUtil.getSchemaProperty("type_RequestForQuote");
  /** type "Package" */
  static final String typePackage = PropertyUtil.getSchemaProperty("type_Package");
  /** type "Quotation" */
  static final String typeQuotation = PropertyUtil.getSchemaProperty("type_Quotation");
  /** type "Route" */
  static final String typeRoute = PropertyUtil.getSchemaProperty("type_Route");
 /** type "Request To Supplier" */
 static final String typeRTS = PropertyUtil.getSchemaProperty("type_RequestToSupplier");
 /** type "RTSQuotation" */
 static final String typeRTSQuotation= PropertyUtil.getSchemaProperty("type_RTSQuotation");

  /**
  * Relatinship constants
  *
  **/
  /** relationship "Project Vaults" */
  static final String relProjectVaults = PropertyUtil.getSchemaProperty("relationship_ProjectVaults");
  /** relationship "Project Members" */
  static final String relProjectMembers = PropertyUtil.getSchemaProperty("relationship_ProjectMembers");
  /** relationship "Project Membership" */
  static final String relProjectMembership = PropertyUtil.getSchemaProperty("relationship_ProjectMembership");
  /** relationship "Sub Vaults" */
  static final String relSubVaults = PropertyUtil.getSchemaProperty("relationship_SubVaults");
  /** relationship "Vaulted Documents" */
  static final String relVaultedObjects = PropertyUtil.getSchemaProperty("relationship_VaultedDocuments");
  /** relationship "Vaulted Documents Rev2" */
  static final String relVaultedObjectsRev2 = PropertyUtil.getSchemaProperty("relationship_VaultedDocumentsRev2");
 /** relationship "Route Scope" */
  static final String relRouteScope = PropertyUtil.getSchemaProperty("relationship_RouteScope");


  /**
  * Attribute constants
  *
  **/
  /** attribute "Project Role" */
  static final String attProjectRole = PropertyUtil.getSchemaProperty("attribute_ProjectRole");

  /**
  * Document Type and derivatives.
  *
  **/
  static protected StringList _documentTypes = null;


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
      ContextUtil.pushContext(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME,
                           null,
                           null );
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
          MqlUtil.mqlCommand(context, "trigger $1", true,"off");
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
  * Grants Access to Project Member for the workspace and its data structure.
  *
  * @param context the eMatrix Context object
  * @param Access List of Access objects holds the access rights, grantee and grantor.
  * @return void
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
                  MqlUtil.mqlCommand(context, "trigger $1", true,"on");
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
              MqlUtil.mqlCommand(context, "trigger $1", true,"on");
          }
          ContextUtil.popContext(context);
      }
  }

  /**
  * Revokes access on all childern of a object when disconnected/removed from the Workspace
  * data structure
  *
  * @param context the eMatrix Context object
  * @param empty String array - Mandatory parameter for JPO.
  * @return void
  * @throws Exception if the operation fails
  * @since TC V3
  * @grade 0
  */
  public void revokeAccess(matrix.db.Context context, String[] args ) throws Exception
  {
      String sType = getType(context);
      String sFolderId = args[0];
      DomainObject dObj = null;
      if(sFolderId != null && !"".equals(sFolderId))
      {
          dObj = new DomainObject(sFolderId);
      } else {
          dObj = this;
      }

      String inheritAccessForTypes = EnoviaResourceBundle.getProperty(context,"emxFramework.FolderContentTypesThatRequireGrants");
      StringList inheritTypes = FrameworkUtil.split(inheritAccessForTypes, ",");
      if(inheritAccessForTypes == null)
      {
         inheritAccessForTypes = "";
      }

      //revoke access if property value is All or if the type is in the stringlist
      if(!"All".equalsIgnoreCase(inheritAccessForTypes) && !"".equals(inheritAccessForTypes))
      {
          //revoke access only for Document/Content defined in _documentTypes
          if(_documentTypes.indexOf(sType) == -1 && !sType.equals(typeProjectVault) && !sType.equals(typeWorkspace) && !sType.equals(typeRoute))
          {
              return;
          }
      }

      // Do not revoke access on Document/Contents if they connected to
      // multiple folders.
      if (!hasMultipleReferences(context))
      {
          // INCIDENT 352728 - ONLY REVOKE ACCESS ON THE REVISION BEING REMOVED
          // Construct BO list with the current object.
          //BusinessObjectList busList = new BusinessObjectList();
          //busList = getRevisions(context);

    //-- Bug 340441 - Use Case 5 - Start -------------------
          // Revoke the accesses granted by 'Workspace Access Grantor'.
          // WORKAROUND INCIDENT 346013 - USE revokeAccess INSTEAD OF revokeAccessRights

          //AccessList accessGrants = getAccessForGrantor(context,AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME );
          MapList mapList = new MapList();
          StringList selects = new StringList();
          selects.add(SELECT_ID);
          selects.add(SELECT_TYPE);
          try
          {
              ContextUtil.pushContext(context);
              mapList = dObj.getRelatedObjects(context, RELATIONSHIP_SUB_VAULTS + "," + RELATIONSHIP_VAULTED_OBJECTS + "," + relVaultedObjectsRev2,
                                                    QUERY_WILDCARD, selects, null, false,true, (short)0, null, null);
          } catch(Exception ex)
          {
              ex.printStackTrace();
          }
          finally
          {
              ContextUtil.popContext(context);
          }

          Iterator mitr;
          Map m;
          String id;
          String type;
          DomainObject obj;
          try
          {
              //pushContextForGrantor(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME );
              //revokeAccessRights(context, busList, accessGrants);
              dObj.revokeAccess(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME, null);
              if( mapList != null)
              {
                  mitr = mapList.iterator();
                  while(mitr.hasNext())
                  {
                      m = (Map)mitr.next();
                      id =  (String)m.get(SELECT_ID);
                      type =  FrameworkUtil.getAliasForAdmin(context,"type", (String)m.get(SELECT_TYPE), true);
                      obj =  new DomainObject(id);
                      if(inheritTypes.contains(type) || obj.isKindOf(context, CommonDocument.TYPE_DOCUMENTS)) {
                      obj.revokeAccess(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME, null);
                  }
              }
          }
          }
          catch(Exception exp)
          {
              throw exp;
          }
          finally{
              //ContextUtil.popContext(context);
          }


          // Revoke the accesses granted by 'Workspace Lead Grantor'.
          // WORKAROUND INCIDENT 346013 - USE revokeAccess INSTEAD OF revokeAccessRights



          //accessGrants = getAccessForGrantor(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME);

          try
          {
             // pushContextForGrantor(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME );
             // revokeAccessRights(context, busList, accessGrants);
              dObj.revokeAccess(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME, null);

              if( mapList != null)
              {
                  mitr = mapList.iterator();
                  while(mitr.hasNext())
                  {
                      m = (Map)mitr.next();
                      id =  (String)m.get(SELECT_ID);
                      type =  FrameworkUtil.getAliasForAdmin(context,"type", (String)m.get(SELECT_TYPE), true);
                      obj =  new DomainObject(id);
                      if(inheritTypes.contains(type) || obj.isKindOf(context, CommonDocument.TYPE_DOCUMENTS)) {
                      obj.revokeAccess(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME, null);
                  }
              }
          }
          }
          catch(Exception exp)
          {
              throw exp;
          }
          finally{
              //ContextUtil.popContext(context);
          }

    //-- Bug 340441 - Use Case 5 - End -------------------
      }
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
      //<Fix 372839>
      else if(sGrantor.equals(AEF_ROUTE_ACCESS_GRANTOR_USERNAME))
      {
          pushContextRouteAccessGrantor(context);
      }
      //</Fix 372839>
  }
  //<Fix 372839>
  protected void pushContextRouteAccessGrantor(Context context) throws Exception
  {
      ContextUtil.pushContext(context, AEF_ROUTE_ACCESS_GRANTOR_USERNAME, null, null);
  }
  //</Fix 372839>

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
      MqlUtil.mqlCommand(context,"mod bus $1 revoke grantor $2 grantee $3", getId(context), sGrantor, sGrantee);
  }

  /**
  * Checks whether Vaulted Object has multiple folder references.
  *
  * @param context the eMatrix Context object
  * @param grantor name to push context to.
  * @return void
  * @throws Exception if the operation fails
  * @since TC 9-5-1-0
  * @grade 0
  */

  protected boolean hasMultipleReferences(matrix.db.Context context) throws Exception
  {
      // If current Vaulted Object is connected to multiple folders then return true.
      // Since the revoke access trigger is on Delete Action, one of the Vaulted Object's
      // folder reference is already disconnected, so need to check any more references exists.
      return VaultedObjectsAccessUtil.getReferenceFolderIds(context, this).size() > 0;
  }

  /**
   * Update Count of Parent when folders are added/deleted.
   *
   * @param context the eMatrix Context object
   * @param Id of object added to Workspace data structure
   * @return void
   * @throws Exception if the operation fails
   * @since TC V3
   * @grade 0
   */
   public void updateCount(matrix.db.Context context, String[] args ) throws Exception
   {
     DomainObject contentObject = new DomainObject(args[0]);
     String event = args[1];
     String SELECT_ATTRIBUTE_COUNT = "attribute[" + ATTRIBUTE_COUNT + "]";

     try
     {
       ContextUtil.startTransaction(context, true);

       int toCountInt = 0;
       try{
         toCountInt = Integer.parseInt(contentObject.getInfo(context, SELECT_ATTRIBUTE_COUNT));
       }catch(Exception ex){}

       int fromCountInt = 0;
       try{
         fromCountInt = Integer.parseInt(this.getInfo(context, SELECT_ATTRIBUTE_COUNT));
       }catch(Exception ex){}

       if("CREATE".equalsIgnoreCase(event))
         this.setAttributeValue(context, ATTRIBUTE_COUNT, Integer.toString(fromCountInt + toCountInt));
       else
         this.setAttributeValue(context, ATTRIBUTE_COUNT, Integer.toString(fromCountInt - toCountInt));

       MapList parentFolders = this.getRelatedObjects(context, RELATIONSHIP_SUB_VAULTS,
                      TYPE_WORKSPACE_VAULT, new StringList(new String[]{SELECT_ID, SELECT_ATTRIBUTE_COUNT}),
                      null, true, false, (short)0, null, null, 0);

       for (int i=0; i < parentFolders.size(); i++) {
               Hashtable parentFolder = (Hashtable)parentFolders.get(i);
               String parentFolderId = (String)parentFolder.get(SELECT_ID);

               DomainObject parentFolderObject = new DomainObject(parentFolderId);
         int parentCountInt = 0;
         try{
           parentCountInt = Integer.parseInt((String)parentFolder.get(SELECT_ATTRIBUTE_COUNT));
         }catch(Exception ex){}

         if("CREATE".equalsIgnoreCase(event))
           parentFolderObject.setAttributeValue(context, ATTRIBUTE_COUNT, Integer.toString(parentCountInt + toCountInt));
         else
           parentFolderObject.setAttributeValue(context, ATTRIBUTE_COUNT, Integer.toString(parentCountInt - toCountInt));
       }

       ContextUtil.commitTransaction(context);

     }catch(Exception ex)
     {
       ContextUtil.abortTransaction(context);
       ex.printStackTrace();
       throw ex;
     }
   }

   public void updateVaultedObjectsAccess(Context context, String[] args ) throws Exception {
       if (!hasMultipleReferences(context) && VaultedObjectsAccessUtil.shouldUpdateAccessForContent(context, this)) {
           try
           {
               pushContextForGrantor(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME );
               revokeAccess(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME, null);
           } finally{
               ContextUtil.popContext(context);
           }

           try
           {
               pushContextForGrantor(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME );
               revokeAccess(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME, null);
           }  finally{
               ContextUtil.popContext(context);
           }
       } else {
           new VaultedObjectsAccessUtil().updateVaultedObjectsAccess(context, new String[] {getId()}, null, null, VaultedObjectsAccessUtil.REMOVE_RELATION);
       }
   }


}
