/*
 *  emxWorkspaceTemplateBase.java
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
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.db.RelationshipItr;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIRTEUtil;
import com.matrixone.apps.team.WorkspaceTemplate;
import com.matrixone.apps.domain.util.PersonUtil;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxWorkspaceTemplateBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public emxWorkspaceTemplateBase_mxJPO (Context context, String[] args)
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
     * Returns a MapList of Workspace Template Ids for the Logged in User.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return MapList
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMyWorkspaceTemplates(Context context, String[] args)
        throws FrameworkException
    {
        try
        {
            MapList templateMapList = new MapList();
            MapList templateUserMapList = new MapList();
            StringList typeSelects = new StringList();

            typeSelects.add(SELECT_ID);
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
            //Get Logged in Person's company
            Company company = person.getCompany(context);
            //Construct Relationship and Type Patterns
            Pattern relPattern = new Pattern(RELATIONSHIP_ORGANIZATION_TEMPLATE);
            Pattern typePattern = new Pattern(TYPE_WORKSPACE_TEMPLATE);
            String sWhere = "(revision == last)";

            if(company != null)
            {
                //Retrieve all Enterprise Scoped Workspace Templates
                templateMapList = company.getRelatedObjects(context,
                                                                  relPattern.getPattern(),
                                                                  typePattern.getPattern(),
                                                                  typeSelects,
                                                                  null,
                                                                  false,
                                                                  true,
                                                                  (short)1,
                                                                  sWhere,
                                                                  "",
                                                                  null,
                                                                  null,
                                                                  null);
            }
            Iterator templateMapListItr = templateMapList.iterator();

            //Remove the relationship and level key-value pairs from each map
            while(templateMapListItr.hasNext())
            {
                Map templateMap = (Map)templateMapListItr.next();
                templateMap.remove("relationship");
                templateMap.remove("level");
            }
            //Retrieve all Workspace Templates in all Vaults of which the logged in Person is owner
            templateUserMapList = DomainObject.findObjects(context,
                                                          typePattern.getPattern(),
                                                          "*",//namePattern
                                                          "*",//revPattern
                                                          person.getName(),//ownerPattern
                                                          null,//vaultPattern
                                                          sWhere,//whereExpression
                                                          false,//expandType
                                                          typeSelects//objectSelects
                                                          );
            Iterator tempMapItr = templateUserMapList.iterator();
            //Add those maps to Enterprise Scoped MapList which are not already present
            while(tempMapItr.hasNext())
            {
                Map map = (Map)tempMapItr.next();
                if(!templateMapList.contains(map))
                {
                    templateMapList.add(map);
                }
            }
            return templateMapList;
        }
        catch (Exception ex)
        {
            throw new FrameworkException(ex);
        }
    }


    /**
     * Determines if each Workspace Template is User Scoped or Enterprise Scoped.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
      public Vector getAvailability(Context context, String[] args)
        throws FrameworkException
      {
          try
          {
              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              MapList objList = (MapList)programMap.get("objectList");
              Vector scopeVector = new Vector(objList.size());
              MapList busObjwsl = null;
              StringBuffer strbuff = new StringBuffer("to[");
              strbuff.append(RELATIONSHIP_ORGANIZATION_TEMPLATE);
              strbuff.append("].from.id");
              // Get the list of Selectables 
              StringList strList = new StringList(1); 
              //strList.addElement("to["+RELATIONSHIP_ORGANIZATION_TEMPLATE+"].from.id"); 
              strList.addElement(strbuff.toString()); 
              String strEnterprise = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.WorkspaceTemplate.Enterprise");
              String strUser = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.WorkspaceTemplate.User");

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
                  String companyId=(String)((Map)busObjwsl.get(i)).get(strbuff.toString()); 
                  if(companyId != null && !"".equals(companyId))
                  {
                      scopeVector.add(strEnterprise);
                  }
                  else
                  {
                    scopeVector.add(strUser);
                  }
              }
            }
            return scopeVector;
          }
          catch (Exception ex)
          {
              throw new FrameworkException(ex);
          }
     }

    /**
     * getWorkspaceTemplateSubFolders.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return MapList
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
      @com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getWorkspaceTemplateSubFolders(Context context, String[] args)
        throws FrameworkException
      {
          try
          {
              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              String parentId = (String) programMap.get("objectId");

              DomainObject WorkspaceTemplate = new DomainObject(parentId);

              StringList typeSelects = new StringList();
              typeSelects.add(WorkspaceTemplate.SELECT_ID);

              Pattern typePattern = new Pattern(TYPE_PROJECT_VAULT);
              Pattern relPattern = new Pattern(RELATIONSHIP_SUB_VAULTS);
              MapList folderList = WorkspaceTemplate.getRelatedObjects (context,
                                                  relPattern.getPattern(),  //String relPattern
                                                  typePattern.getPattern(), //String typePattern
                                                  typeSelects,
                                                  null,
                                                  false,
                                                  true,
                                                  (short)1,
                                                  "",
                                                  "",
                                                  null,
                                                  null,
                                                  null);
              return  folderList;
          }
          catch (Exception ex)
          {
              throw new FrameworkException(ex);
          }
     }
    /**
     * getWorkspaceTemplateFolders.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return MapList
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
      @com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getWorkspaceTemplateFolders(Context context, String[] args)
        throws FrameworkException
      {
          try
          {
              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              String parentId = (String) programMap.get("objectId");
              DomainObject WorkspaceTemplate = DomainObject.newInstance(context,parentId);

              StringList typeSelects = new StringList();
              typeSelects.add(WorkspaceTemplate.SELECT_ID);

              Pattern typePattern = new Pattern(TYPE_PROJECT_VAULT);
              Pattern relPattern = new Pattern(RELATIONSHIP_WORKSPACE_VAULTS);
              MapList folderList = WorkspaceTemplate.getRelatedObjects (context,
                                                  relPattern.getPattern(),  //String relPattern
                                                  typePattern.getPattern(), //String typePattern
                                                  typeSelects,
                                                  null,
                                                  false,
                                                  true,
                                                  (short)1,
                                                  "",
                                                  "",
                                                  null,
                                                  null,
                                                  null);
              return  folderList;
          }
          catch (Exception ex)
          {
              throw new FrameworkException(ex);
          }
     }
    /**
     * getWorkspaceTemplateFolders.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector getWorkspaceTemplateFoldersSecurity(Context context, String[] args)
        throws FrameworkException
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector securityVector = new Vector(objList.size());
            MapList busObjwsl = null;
            StringBuffer strbuff = new StringBuffer("attribute[");
            strbuff.append(ATTRIBUTE_GLOBAL_READ);
            strbuff.append("]");
            // Get the list of Selectables 
            StringList strList = new StringList(1); 
            //strList.addElement("attribute[" + ATTRIBUTE_GLOBAL_READ + "]"); 
            strList.addElement(strbuff.toString()); 
              String strGlobalRead = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.TeamEditFolders.GlobalReadAccess");
              
              String strWorkspaceAccess = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.TeamEditFolders.WorkspaceAccess");
            
            if ( objList != null) 
            { 
               String objIdArray[] = new String[objList.size()]; 
               //Get the array of Object Ids to be passed into the methods 
               for (int i = 0; i < objList.size(); i++) 
               { 
                 Map objMap = (Map)objList.get(i); 
                 objIdArray[i]  = (String)objMap.get(SELECT_ID); 
               } 
               busObjwsl = DomainObject.getInfo(context, 
                                                objIdArray, 
                                                strList);
               for (int i = 0; i < objList.size(); i++)
               { 
                String security=(String)((Map)busObjwsl.get(i)).get(strbuff.toString());
                String dispSecurity = strWorkspaceAccess;
                if(security!=null)
                {
                    if("True".equalsIgnoreCase(security))
                    {
                        dispSecurity=strGlobalRead;
                    }
                }
                securityVector.addElement(dispSecurity);
               }
             }
            return securityVector;
        }
        catch (Exception ex)
        {
            throw new FrameworkException(ex);
        }
    }

    /**
     * getWorkspaceTemplateMembers.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return MapList
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public MapList getWorkspaceTemplateMembers(Context context, String[] args)
        throws FrameworkException
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String parentId = (String) programMap.get("objectId");
            DomainObject WorkspaceTemplate = DomainObject.newInstance(context,parentId);

            StringList typeSelects = new StringList();
            typeSelects.add(WorkspaceTemplate.SELECT_ID);
            typeSelects.add(WorkspaceTemplate.SELECT_CURRENT);
            StringList relSelect = new StringList();
            relSelect.add(WorkspaceTemplate.SELECT_RELATIONSHIP_ID);

            Pattern typePattern = new Pattern(TYPE_PERSON);
            Pattern relPattern = new Pattern(RELATIONSHIP_WORKSPACE_TEMPLATE_MEMBER);
            String sWhere = WorkspaceTemplate.SELECT_CURRENT+" == Active";
            MapList memberList = WorkspaceTemplate.getRelatedObjects (context,
                                                relPattern.getPattern(),  //String relPattern
                                                typePattern.getPattern(), //String typePattern
                                                typeSelects,
                                                relSelect,
                                                false,
                                                true,
                                                (short)1,
                                                sWhere,
                                                "",
                                                null,
                                                null,
                                                null);

            return  memberList;
        }
        catch (Exception ex)
        {
            throw new FrameworkException(ex);
        }
    }

    /**
     * showProjectLeadGif.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector showProjectLeadGif(Context context, String[] args)
       throws FrameworkException
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector projectLead = new Vector(objList.size());
            MapList busObjwsl = null;
            StringBuffer strbuff = new StringBuffer("attribute[");
            strbuff.append(ATTRIBUTE_PROJECT_ACCESS);
            strbuff.append("]");
            // Get the list of Selectables 
            StringList strList = new StringList(1); 
            //strList.addElement("attribute["+ATTRIBUTE_PROJECT_ACCESS+"]"); 
            strList.addElement(strbuff.toString()); 
            if ( objList != null) 
            { 
               String relIdArray[] = new String[objList.size()]; 
               //Get the array of Object Ids to be passed into the methods 
               for (int i = 0; i < objList.size(); i++) 
               { 
                 Map objMap = (Map)objList.get(i); 
                 relIdArray[i]  = (String)objMap.get(SELECT_RELATIONSHIP_ID); 
               } 
               busObjwsl = DomainRelationship.getInfo(context, 
                                                relIdArray, 
                                                strList);
               for (int i = 0; i < objList.size(); i++)
               { 
                  String sProjectLead=(String)((Map)busObjwsl.get(i)).get(strbuff.toString());
                  String imageString="";
                  if(sProjectLead !=null)
                  {
                    if("Project Lead".equalsIgnoreCase(sProjectLead))
                    {
                        imageString = "<img border='0' src='../teamcentral/images/iconProjectLead.gif' name='iconProjectLead' id='iconProjectLead'  />";
                    }
                  }
                projectLead.add(imageString);
            } 
          }
            //XSSOK
            return projectLead;
        }
        catch (Exception ex)
        {
            throw new FrameworkException(ex);
        }
    }

    /**
     * showCreateRouteGif.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector showCreateRouteGif(Context context, String[] args)
        throws FrameworkException
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector createRoute = new Vector(objList.size());
            MapList busObjwsl = null;
            StringBuffer strbuff = new StringBuffer("attribute[");
            strbuff.append(ATTRIBUTE_CREATE_ROUTE);
            strbuff.append("]");
            // Get the list of Selectables 
            StringList strList = new StringList(1); 
            //strList.addElement("attribute["+ATTRIBUTE_CREATE_ROUTE+"]"); 
            strList.addElement(strbuff.toString()); 
            if ( objList != null) 
            { 
               String relIdArray[] = new String[objList.size()]; 
               //Get the array of Object Ids to be passed into the methods 
               for (int i = 0; i < objList.size(); i++) 
               { 
                 Map objMap = (Map)objList.get(i); 
                 relIdArray[i]  = (String)objMap.get(SELECT_RELATIONSHIP_ID); 
               } 
               busObjwsl = DomainRelationship.getInfo(context, 
                                                relIdArray, 
                                                strList);
               for (int i = 0; i < objList.size(); i++)
               { 
                  String sCreateRoute=(String)((Map)busObjwsl.get(i)).get(strbuff.toString());
                  String imageString="";
                  if(sCreateRoute !=null)
                  {
                    if("Yes".equalsIgnoreCase(sCreateRoute))
                    {
                        imageString = "<img border='0' src='../teamcentral/images/iconSmallRoute.gif' name='iconSmallRoute' id='iconSmallRoute'  />";
                    }
                  }
                createRoute.add(imageString);
              }
            }
            //XSSOK
            return createRoute;
        }
        catch (Exception ex)
        {
            throw new FrameworkException(ex);
        }
    }

    /**
     * emxWorkspaceTemplateRev
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return MapList
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getWorkspaceTemplateRev(Context context, String[] args)
        throws FrameworkException
    {
        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            String parentId           = (String) programMap.get("objectId");
            DomainObject connectedObject = DomainObject.newInstance(context,parentId);

            StringList typeSelects = new StringList();
            StringList sList = new StringList();

            sList.addElement(SELECT_ID);
            MapList templateMapList = connectedObject.getRevisionsInfo(context,sList,typeSelects);
            return templateMapList;
        }
        catch (Exception ex)
        {
            throw new FrameworkException(ex);
        }
    }


    /**
     * getAvailibility
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector getAvailibility(Context context, String[] args)
        throws FrameworkException
    {
        try
        {
            
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector availibilityVector = new Vector(objList.size());
            MapList busObjwsl = null; 
            StringBuffer strbuff = new StringBuffer("to[");
            strbuff.append(RELATIONSHIP_ORGANIZATION_TEMPLATE);
            strbuff.append("].from.id");
            // Get the list of Selectables 
            StringList strList = new StringList(1); 
            //strList.addElement("to["+RELATIONSHIP_ORGANIZATION_TEMPLATE+"].from.id"); 
            strList.addElement(strbuff.toString());
            i18nNow i18nnow = new i18nNow();
            String strLanguage = context.getSession().getLanguage();
            String strEnterprise = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.WorkspaceTemplate.Enterprise");
            
            String strUser = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", context.getLocale(), "emxTeamCentral.WorkspaceTemplate.User");
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
                String companyId=(String)((Map)busObjwsl.get(i)).get(strbuff.toString()); 
                if(companyId==null)
                {
                    availibilityVector.add(strUser);
                }
                else
                {
                    availibilityVector.add(strEnterprise);
                }
              }
		  }
            return availibilityVector;
        }
        catch (Exception ex)
        {
            throw new FrameworkException(ex);
        }
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
     * showSummaryCheckBox - method to display the checkbox in workspace template summary
     * @param context the eMatrix <code>Context</code> object
     * @return Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */ 
    public Vector showSummaryCheckBox(Context context,String[] args) throws Exception {
    	Vector summaryCheckBoxes = new Vector();
    	HashMap programMap=(HashMap)JPO.unpackArgs(args);
    	
    	MapList objectList=(MapList)programMap.get("objectList");
    	String contextUser = context.getUser();
    	
    	for(Iterator objectListItr=objectList.iterator();objectListItr.hasNext();)
    	{
    		Map idMapList=(Map)objectListItr.next();
    		String wsTemplateId = (String)idMapList.get(SELECT_ID);
    		
    		DomainObject boWorkspaceTemplate = new DomainObject(wsTemplateId);
    		boWorkspaceTemplate.open(context);
    		if( boWorkspaceTemplate.getOwner().getName().equals(contextUser)) {
    			summaryCheckBoxes.add("true");
    		} else {
    			summaryCheckBoxes.add("false");
    		}
    		boWorkspaceTemplate.close(context);
    	}
    	
    	return summaryCheckBoxes;
    }
   
    /**
     * getWorkspaceTemplateAvailability - displays the availability for the workspace template
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @return Vector
     * @throws Exception if the operation fails
     * @since V6R2011x
     */
    public String getWorkspaceTemplateAvailability(Context context,String[] args) throws Exception 
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        String relOrgTemplate     = DomainObject.RELATIONSHIP_ORGANIZATION_TEMPLATE;
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        String mode=(String)requestMap.get("mode");
        WorkspaceTemplate boTemplate  = new WorkspaceTemplate(objectId);
        boTemplate.open(context);
        String language = (String) programMap.get("languageStr");
        language = language == null ? context.getLocale().getLanguage() : language;
        String strScope             = "";
        boTemplate.close(context);
        String userscope= i18nNow.getI18nString("emxTeamCentral.WorkspaceTemplate.User","emxTeamCentralStringResource",language);
        String enterprisescope= i18nNow.getI18nString("emxTeamCentral.WorkspaceTemplate.Enterprise","emxTeamCentralStringResource",language);

        String strAvailability = (String)requestMap.get("Availability");
        StringBuffer sb = new StringBuffer();
        if(mode.equalsIgnoreCase("edit"))
        {
            String sCreationDate = null;


            String sName         = null;
            String sRevision     = null;
            String sOwner        = null;
            String sProjectDesc  = null;
            String sStateUser         = "";
            String sStateEnterprise   = "";

            if(objectId != null)
            {
              WorkspaceTemplate wsTemplateObj = new WorkspaceTemplate();
              wsTemplateObj.setId(objectId);
              sName         = wsTemplateObj.getName(context);
              sRevision     = wsTemplateObj.getRevision(context);
              sOwner        = wsTemplateObj.getOwner(context).getName();



              sCreationDate       = wsTemplateObj.getCreated();


              sProjectDesc  = wsTemplateObj.getDescription(context);
              String orgId  = wsTemplateObj.getInfo(context,"to["+ DomainObject.RELATIONSHIP_ORGANIZATION_TEMPLATE +"].from.id");
              if (orgId != null && !"".equals(orgId))
              {
                sStateEnterprise = "checked";
              }
              else
              {
                sStateUser = "checked";
              }
            }
   
           sb.append("<input type=\"radio\" "+sStateUser+" value=\"User\" name=\"Availability\" id=\"Availability\" />");
           sb.append("&nbsp;"+XSSUtil.encodeForHTML(context,userscope));
           sb.append("<br>");
           if(isCompanyRepresentative(context,com.matrixone.apps.common.Person.getPerson(context))){
               sb.append("<input type=\"radio\" "+sStateEnterprise+" value=\"Enterprise\" name=\"Availability\" id=\"Availability\" />");
               sb.append("&nbsp;"+XSSUtil.encodeForHTML(context,enterprisescope));
           }
           strScope = sb.toString();
        }
        else{
            String companyId=boTemplate.getInfo(context,"to["+relOrgTemplate+"].from.id");
            if(companyId==null)
            {
                strScope = userscope;
            }
            else
            {
                strScope = enterprisescope;

            }
        }
        return strScope;
    }
    /**
     * updateTemplateAvailability - method to update the selected availability for template - User or Enterprise
     * @param context the eMatrix <code>Context</code> object
     * @return boolean
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */ 
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public static void updateTemplateAvailability(Context context, String args[])throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        String relOrgTemplate     = DomainObject.RELATIONSHIP_ORGANIZATION_TEMPLATE;
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String workspaceTemplateId = (String)requestMap.get("objectId");
        String strName = (String)requestMap.get("Name");
        String strDesc = (String)requestMap.get("Description");
        String availability = (String)requestMap.get("Availability");
        boolean errFlag            = false;
        boolean bNameAlreadyExists = false;
        String sProjectDesc        = "";
        String sPrevName           = "";
        String sTypeName           = "";
        String sScope              = "";
        WorkspaceTemplate wsTemplateObj = (WorkspaceTemplate)DomainObject.newInstance(context ,DomainConstants.TYPE_WORKSPACE_TEMPLATE ,DomainConstants.TEAM);
        if (workspaceTemplateId != null) {
            wsTemplateObj.setId(workspaceTemplateId);
            sTypeName     = wsTemplateObj.getType(context);
            sProjectDesc  = wsTemplateObj.getDescription(context);
            sPrevName     = wsTemplateObj.getName(context);

            String orgRelId  = wsTemplateObj.getInfo(context,"to["+relOrgTemplate+"].id");

            if (orgRelId != null && !"".equals(orgRelId)){
              sScope = "Enterprise";
            }else{
              sScope = "User";
            }

            //check for the existance of the workspace template with the changed name if template name has changed
            if (!sPrevName.equals(strName)) {
              bNameAlreadyExists = WorkspaceTemplate.isWorkspaceTemplateExists(context, strName);
            }

            try{

              //change the name of the current workspace template
              if (!bNameAlreadyExists) {
                wsTemplateObj.change(context, sTypeName, strName, wsTemplateObj.getRevision(), wsTemplateObj.getVault(), wsTemplateObj.getPolicy().getName());
              } else {
                errFlag = true;
                throw new Exception();
                //session.putValue("error.message", sTypeName + " " + strName + " " + getI18NString("emxTeamCentralStringResource","emxTeamCentral.Common.AlreadyExists",language));
              }

            if (!errFlag) {
              if(!sScope.equals(availability)){
                if(sScope.equals("Enterprise")){
                  DomainRelationship.disconnect(context , orgRelId);
                }else{
                  com.matrixone.apps.common.Person perObj = com.matrixone.apps.common.Person.getPerson(context);
                  DomainObject orgObj = perObj.getCompany(context);
                  DomainRelationship.connect(context, orgObj, relOrgTemplate, wsTemplateObj);
                }
              }

              if(!sProjectDesc.equals(strDesc)){
                wsTemplateObj.setDescription(context,strDesc);
              }
             }
           }catch(Exception e){
                 e.printStackTrace();
                 errFlag        = true;
           }
          }
        
    }
    /**
     * isCompanyRepresentative - method to check whether a user is a Company Representative or not
     * @param context the eMatrix <code>Context</code> object
     * @return boolean
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */ 
    public static boolean isCompanyRepresentative ( matrix.db.Context context, BusinessObject person ) throws MatrixException
    {
      //matrix.db.Context context = getPageContext();
      boolean companyRep = false;

      String companyRepresentativeRelStr = PropertyUtil.getSchemaProperty(context,"relationship_CompanyRepresentative" );
      String relName = null;

      RelationshipItr companyRepRelItr = new RelationshipItr(person.getToRelationship(context));
      Relationship companyRepRel = null;
      while ( companyRepRelItr.next()){
        companyRepRel = companyRepRelItr.obj();
        relName = companyRepRel.getTypeName();
        if ( relName.equals( companyRepresentativeRelStr )){
          companyRep = true;
          break;
        }
      }
      return companyRep;
    }
    /**
     * displayTemplateAvailability - method to display the options for availability - User or Enterprise
     * @param context the eMatrix <code>Context</code> object
     * @return String
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */ 
    public static String displayTemplateAvailability(Context context, String args[]) throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        Map paramMap = (HashMap)programMap.get("paramMap");
        String language = (String) paramMap.get("languageStr");
        String ROLE_VPLMProjectLeader = PropertyUtil.getSchemaProperty(context, "role_VPLMProjectLeader" );
        language = language == null ? (String)((HashMap)programMap.get("requestMap")).get("languageStr") : language;
        String relOrgTemplate     = DomainObject.RELATIONSHIP_ORGANIZATION_TEMPLATE;
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        DomainObject dob = DomainObject.newInstance(context, objectId);
        String workspaceTemplateId  = dob.getInfo(context,"to[Workspace Template].from.id");
        String mode=(String)requestMap.get("mode");
        String userscope= i18nNow.getI18nString("emxTeamCentral.WorkspaceTemplate.User","emxTeamCentralStringResource",language);
        String enterprisescope= i18nNow.getI18nString("emxTeamCentral.WorkspaceTemplate.Enterprise","emxTeamCentralStringResource",language);
        String strScope = "";
        DomainObject boTemplate = null;
        String strUserChecked = "checked";
        String strEnterpriseChecked = "";
        com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
        Company company = person.getCompany(context);
        StringBuffer sb = new StringBuffer();
        if(workspaceTemplateId!= null && !"".equals(workspaceTemplateId))
        {
        boTemplate=DomainObject.newInstance(context,workspaceTemplateId,DomainConstants.TEAM);
            
            String companyId=boTemplate.getInfo(context,"to["+boTemplate.RELATIONSHIP_ORGANIZATION_TEMPLATE+"].from.id");
            if(companyId==null)
            {
                strScope = "User";
                strUserChecked="checked";
            }
            else
            {
                strEnterpriseChecked="checked";
                strUserChecked="";
            }
            sb.append("<input type=radio name='Availability' value='User' "+strUserChecked+" />");
            sb.append("&nbsp;"+XSSUtil.encodeForHTML(context,userscope));
            sb.append("</br>");
            if(isCompanyRepresentative(context,com.matrixone.apps.common.Person.getPerson(context)) || PersonUtil.hasAssignment(context, ROLE_VPLMProjectLeader)){
                sb.append("<input type=radio name='Availability' value = \"Enterprise\" "+strEnterpriseChecked+" />");
                sb.append("&nbsp;"+XSSUtil.encodeForHTML(context,enterprisescope));
            }
            strScope = sb.toString();
        }
        else
        {
            sb.append("<input type=radio name='Availability' value='User' "+strUserChecked+" />");
            sb.append("&nbsp;"+XSSUtil.encodeForHTML(context,userscope));
            sb.append("</br>");
            if(isCompanyRepresentative(context,com.matrixone.apps.common.Person.getPerson(context)) || PersonUtil.hasAssignment(context, ROLE_VPLMProjectLeader)){
                sb.append("<input type=radio name='Availability' value = \"Enterprise\" />");
                sb.append("&nbsp;"+XSSUtil.encodeForHTML(context,enterprisescope));
            }
            strScope = sb.toString();
        }
        return strScope;
    }
    /**
     * displaySaveOptions - method to display the save options for workspace template - User or Enterprise
     * @param context the eMatrix <code>Context</code> object
     * @return String
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */ 
    public static String displaySaveOptions(Context context, String args[]) throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        Map paramMap = (HashMap)programMap.get("paramMap");
        String language = (String) paramMap.get("languageStr");
        language = language == null ? (String)((HashMap)programMap.get("requestMap")).get("languageStr") : language;
        String relOrgTemplate     = DomainObject.RELATIONSHIP_ORGANIZATION_TEMPLATE;
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        DomainObject dob = DomainObject.newInstance(context, objectId);
        String sworkspaceTemplateId  = dob.getInfo(context,"to[Workspace Template].from.id");
        String mode=(String)requestMap.get("mode");
        String strSaveTemplate= i18nNow.getI18nString("emxTeamCentral.SaveTemplateDialog.SavenewTemp","emxTeamCentralStringResource",language);
        String reviseTemp= i18nNow.getI18nString("emxTeamCentral.SaveTemplateDialog.ReviseTemp","emxTeamCentralStringResource",language);
        StringBuffer strOptions = new StringBuffer();
        com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
        Company company = person.getCompany(context);
        String strCompanyId = company.getObjectId();
        String strWorkspaceTempName = "";
        String strWorkspaceTempDesc = "";
        String strUserChecked = "";
        String strEnterpriseChecked = "";
        String workspaceTemplateId = "";
        String strScope             = (String)requestMap.get("rb");
        boolean bRevise           = false;
        String reviseChecked="";
        String newChecked="checked";
       DomainObject boTemplate = null;
        String disable = "";
        
        if(sworkspaceTemplateId!=null&&!"".equals(sworkspaceTemplateId))
            {
               
               
                
                String rel = "to[Workspace Template].from.id";
            
                workspaceTemplateId = dob.getInfo(context,rel);
            
            boTemplate=DomainObject.newInstance(context,workspaceTemplateId,DomainConstants.TEAM);
           
              boTemplate.open(context);
              strWorkspaceTempName=boTemplate.getName(context);
              strWorkspaceTempDesc=boTemplate.getInfo(context,boTemplate.SELECT_DESCRIPTION);
              String companyId=boTemplate.getInfo(context,"to["+boTemplate.RELATIONSHIP_ORGANIZATION_TEMPLATE+"].from.id");
              if(companyId==null)
              {
                  strScope = "User";
                  strUserChecked="checked";
              }
              else
              {
                  strEnterpriseChecked="checked";
                strUserChecked="";
              }
              
              String hasReviseAccess = boTemplate.getInfo(context, "current.access[revise]");
              if(hasReviseAccess.equals("TRUE")) {
                bRevise=true;
              }
              if(bRevise){
                  newChecked="";
                  reviseChecked="checked";
                  strOptions.append("<input type = \"radio\" name = \"SaveOptions\" id = \"SaveOptions\" value = \"yes\""+XSSUtil.encodeForHTMLAttribute(context,newChecked)+">"+XSSUtil.encodeForHTML(context,strSaveTemplate)+"</br>");
                  strOptions.append("<input type = \"radio\" name = \"SaveOptions\" id = \"SaveOptions\" value = \"no\""+XSSUtil.encodeForHTMLAttribute(context,disable)+""+XSSUtil.encodeForHTML(context,reviseChecked)+">");
                  
                  strOptions.append(reviseTemp+strWorkspaceTempName);
                }else{
                	 strOptions.append("<input type = \"radio\" name = \"SaveOptions\" id = \"SaveOptions\" value = \"yes\""+newChecked+">"+strSaveTemplate+"</br>");
                }
            }
        else
        {
    
    if(!bRevise)disable = "disabled";
    strOptions.append("<input type = \"radio\" name = \"SaveOptions\" id = \"SaveOptions\" value = \"yes\""+XSSUtil.encodeForHTMLAttribute(context,newChecked)+">"+XSSUtil.encodeForHTML(context,strSaveTemplate)+"</br>");
        }
       
            
           
        return strOptions.toString();
    }
    /**
     * displayTemplateData - method to display the template data like folders and members
     * @param context the eMatrix <code>Context</code> object
     * @return String
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */ 
    public static String displayTemplateData(Context context, String args[]) throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        Map paramMap = (HashMap)programMap.get("paramMap");
        String language = (String) paramMap.get("languageStr");
        language = language == null ? (String)((HashMap)programMap.get("requestMap")).get("languageStr") : language;
        String strMembers= i18nNow.getI18nString("emxTeamCentral.WorkspaceTemplateSaveDialog.Members","emxTeamCentralStringResource",language);
        String strFolderStructure= i18nNow.getI18nString("emxTeamCentral.WorkspaceTemplateSaveDialog.FolderStructure","emxTeamCentralStringResource",language);
        StringBuffer strTemplateData = new StringBuffer();
        strTemplateData.append("<input type=checkbox name=\"addMembers\" id=\"addMembers\" value=\"\" checked onclick=\"javascript:templateDataSelected(this)\">"+XSSUtil.encodeForHTML(context,strMembers)+"</br>");
        strTemplateData.append("<input type=checkbox name=\"addFolders\" id=\"addFolders\" value=\"\" checked onclick=\"javascript:templateDataSelected(this)\">"+XSSUtil.encodeForHTML(context,strFolderStructure));
        strTemplateData.append("<input type=hidden name=\"TemplateData\" id=\"TemplateData\" value=\"true|true\" >");
                
        return strTemplateData.toString();
    }
    /**
     * saveAsTemplateProcess - method to save the selected workspace as a workspace template
     * @param context the eMatrix <code>Context</code> object
     * @return void
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */ 
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public HashMap saveAsTemplateProcess(Context context, String[] args)throws Exception
    {
        try
        {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String language = (String) requestMap.get("languageStr");
        language = language == null ? context.getLocale().getLanguage() : language;
        String objectId = (String)requestMap.get("objectId");
        String strTemplateName      = (String)requestMap.get("TemplateName");
        String strTemplateDesc      = (String)requestMap.get("TemplateDescription");
        String strScope             = (String)requestMap.get("Availability");
        String strOption            = (String)requestMap.get("SaveOptions");
        String WorkspaceId          = (String)requestMap.get("objectId");
        String templateData        = (String)requestMap.get("TemplateData");
        String workspaceTemplateId  = "";
        String strAddMembers  = "";
        String strAddFolders  = "";
        HashMap resultMap           = new HashMap();
        boolean addMembers =false;
        boolean addFolders =false;
        boolean bRevise = false;
        boolean bError=false;
        String strAlreadyExists =i18nNow.getI18nString("emxTeamCentral.WorkspaceTemplateSaveDialog.AlreadyExists","emxTeamCentralStringResource",language); 
        String strLargeValue = i18nNow.getI18nString("emxTeamCentral.WorkspaceTemplateSaveDialog.NameLength","emxTeamCentralStringResource",language);
    
        if(objectId!=null && !"".equals(objectId))
        {
            DomainObject dob = DomainObject.newInstance(context, objectId);
            workspaceTemplateId = dob.getInfo(context,"to["+DomainConstants.TYPE_WORKSPACE_TEMPLATE+"].from.id");
        }


        WorkspaceTemplate workspaceTemplate =(WorkspaceTemplate)DomainObject.newInstance(context,DomainObject.TYPE_WORKSPACE_TEMPLATE,DomainObject.TEAM);
        StringList strTempData = FrameworkUtil.split(templateData, "|");
        strAddMembers = (String)strTempData.get(0);
        strAddFolders = (String)strTempData.get(1);
        if("true".equals(strAddMembers)){
          addMembers=true;
        }
        if("true".equals(strAddFolders)){
            addFolders=true;
        }



        
        if(strOption != null && strOption.equals("yes")) {
          bRevise=false;
        }
        else
        {
          bRevise=true;
        }
        boolean addBuyerDeskMember=false;
        String addBuyerDeskMemberStr = FrameworkProperties.getProperty(context,"emxTeamCentral.ConvertBuyerDeskMembersToWorkspaceMembers");
        if( addBuyerDeskMemberStr == null ) {
            addBuyerDeskMember = false;
        }else if("true".equalsIgnoreCase(addBuyerDeskMemberStr.trim())){
          addBuyerDeskMember=true;
        }
        if(!bRevise)
        {
          try{
        	  boolean exists =  WorkspaceTemplate.isWorkspaceTemplateExists(context, strTemplateName);
        	if(exists)
        	{
        		throw new Exception("not unique");
        	}
        	
            workspaceTemplateId=workspaceTemplate.createWorkspaceTemplate(context,WorkspaceId,strTemplateName,addMembers,addBuyerDeskMember,addFolders,strScope);
            }
            catch(Exception ex)
            {
              bError=true;
              String errorMessage=ex.getMessage();
              if(errorMessage.indexOf("not unique")!=-1){
                  resultMap.put("Message", strAlreadyExists);
              }else if(errorMessage.indexOf("inserted value too large for column")!=-1) {
                  emxContextUtil_mxJPO.mqlNotice(context,strLargeValue);
              }
              else{
                  emxContextUtil_mxJPO.mqlNotice(context,errorMessage);
              }
//              throw new Exception(ex.getMessage());
            }

        }
        else
        {
          workspaceTemplateId=workspaceTemplate.reviseWorkspaceTemplate(context,workspaceTemplateId, WorkspaceId,strTemplateName,addMembers,addBuyerDeskMember ,addFolders, strScope);
        }

        if(!bError)
        {
          //set Description
          if(workspaceTemplateId !=null && !"".equals(workspaceTemplateId) && !"null".equals(workspaceTemplateId)){
            workspaceTemplate.setId(workspaceTemplateId);
            workspaceTemplate.open(context);
            AttributeList templateAttrList = new AttributeList();
            if(UIRTEUtil.isRTEEnabled(context,"Workspace Template", "description")){
            	AttributeType attrType = new AttributeType("description_RTE");
        		Attribute attr = new Attribute(attrType, strTemplateDesc);
        		templateAttrList.add(attr);
            }else{
            workspaceTemplate.setDescription(strTemplateDesc);
            } 
            workspaceTemplate.setAttributes(context,templateAttrList);
            workspaceTemplate.setVault(context,context.getVault());
            workspaceTemplate.update(context);
            workspaceTemplate.close(context);
          }
        }

        return resultMap;
    }
     
     catch(Exception ex){
       
       throw ex;
     }
}
    /**
     * getTemplateName - method to return the workspace template name if already connected to workspace
     * @param context the eMatrix <code>Context</code> object
     * @return String
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */ 
    public String getTemplatename(Context context, String[] args) throws Exception
    {
        StringBuffer templateNames = new StringBuffer();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String objectId = (String)paramMap.get("objectId");
        
        DomainObject dob = DomainObject.newInstance(context, objectId);
        String strTempId = dob.getInfo(context,"to[Workspace Template].from.id");
        String rel = "to[Workspace Template].from.id";
        if(strTempId!=null)
        {
            String workspaceTemplateId = dob.getInfo(context,rel);
            DomainObject boTemplate=DomainObject.newInstance(context,workspaceTemplateId,DomainConstants.TEAM);
            boTemplate.open(context);
            String strWorkspaceTempName=boTemplate.getName(context);
            templateNames.append("<input type=\"text\" name=\"TemplateName\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strWorkspaceTempName)+"\">");
        }
        else
        {
            templateNames.append("<input type=\"text\" name=\"TemplateName\" value=\"\">");
        }
        return templateNames.toString();
    }
    /**
     * getTemplateDescription - method to return the workspace template description if already connected to workspace
     * @param context the eMatrix <code>Context</code> object
     * @return String
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */  
    public String getTemplateDescription(Context context, String[] args) throws Exception
    {
        StringBuffer templateDesc = new StringBuffer();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String objectId = (String)paramMap.get("objectId");
        DomainObject dob = DomainObject.newInstance(context, objectId);
        String strTempId = dob.getInfo(context,"to[Workspace Template].from.id");
        String rel = "to[Workspace Template].from.id";
        boolean enableRichTextEditor = UIRTEUtil.isRTEEnabled(context,"Workspace Template", "description");
        String rteClass = enableRichTextEditor? "class=\"rte\"" : "";
        
        if(strTempId!=null)
        {
          String workspaceTemplateId = dob.getInfo(context,rel);
          DomainObject boTemplate=DomainObject.newInstance(context,workspaceTemplateId,DomainConstants.TEAM);
          boTemplate.open(context);
          String strWorkspaceTempDesc=boTemplate.getInfo(context,boTemplate.SELECT_DESCRIPTION);
          templateDesc.append("<textarea ").append(rteClass).append(" name=\"TemplateDescription\" rows = \"5\" cols = \"25\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strWorkspaceTempDesc)+"\">"+XSSUtil.encodeForHTML(context,strWorkspaceTempDesc)+"</textarea>");
        }
        else
        {
            templateDesc.append("<textarea ").append(rteClass).append(" name=\"TemplateDescription\" rows = \"5\" cols = \"25\" value=\"\"></textarea>"); 
        }
        return templateDesc.toString();
    }      
   
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getWorkspaceTemplateIncludeIDs(Context context, String[] args) throws FrameworkException 
	{
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
        
			MapList WorkspaceTemplateList = (MapList)getMyWorkspaceTemplates(context,null);   
			StringList ids = new StringList(WorkspaceTemplateList.size());
        
			for (int i = 0; i < WorkspaceTemplateList.size(); i++) {
				Map workspaceTemplate = (Map) WorkspaceTemplateList.get(i);
				ids.add(workspaceTemplate.get(SELECT_ID));
			}        
			return ids;
			
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}

}
