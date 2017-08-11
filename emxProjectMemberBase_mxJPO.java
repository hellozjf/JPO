/*
 *  emxProjectMemberBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.12.2.2 Thu Dec  4 07:55:12 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.12.2.1 Thu Dec  4 01:53:20 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.12 Tue Oct 28 22:59:44 2008 przemek Experimental przemek $
 */

/*
Change History:
Date       Change By  Release   Bug/Functionality         Details
-----------------------------------------------------------------------------------------------------------------------------
29-Apr-09   wqy        V6R2010   373332                   Change Code for I18n
*/

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxAttr;
import com.matrixone.apps.framework.ui.ProgramCallable;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UITable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectRoleVaultAccess;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.WeeklyTimesheet;
import com.matrixone.apps.program.fiscal.CalendarType;
import com.matrixone.apps.program.fiscal.Helper;
import com.matrixone.apps.program.fiscal.Interval;
import com.matrixone.apps.program.fiscal.IntervalType;

/*****************************************************************************************
 *       New JPO for Project Member summary Config Table Conversion Task
 *******************************************************************************************/
/**
 * The <code>emxProjectMemberBase</code> class represents the Project Member JPO
 * functionality for the AEF type.
 *
 * @version PMC 10-6 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectMemberBase_mxJPO
{
    /**
     * Constructs a new emxProjectMember JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments:
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public emxProjectMemberBase_mxJPO (Context context, String[] args) throws Exception
    {
        // Call the super constructor
        super();
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public int mxMain(Context context, String[] args)
    throws Exception
    {
        if (!context.isConnected())
            throw new Exception("not supported on desktop client");
        return 0;
    }

    /**
     * hasAccess - This method verifies the user's permission for the given Project Member.
     * and Project Lead.Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * objectId   - String containing the objectId
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public boolean hasAccess(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        boolean access = false;
      try{
          if (ProgramCentralUtil.isNotNullString(objectId)){
            	DomainObject dmoTaskOrProject = DomainObject.newInstance(context, objectId);
            	access = dmoTaskOrProject.checkAccess(context, (short) AccessConstants.cModify);
            }
        }
      catch (Exception ex){
            throw ex;
        }
            return access;
//    	Old Security Impl    	
//        HashMap programMap = (HashMap) JPO.unpackArgs(args);
//        String objectId = (String) programMap.get("objectId");
//        com.matrixone.apps.program.ProjectSpace projectSpace=
//            (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
//                    DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
//
//        // if object id is passed, then we look at object permissions; otherwise
//        // this screen is being accessed from project members, which means you can update.
//        boolean access = false;
//
//        try
//        {
//            if ((objectId != null) && !objectId.equals(""))
//            {
//            	DomainObject dmoTaskOrProject = DomainObject.newInstance(context, objectId);
//            	access = dmoTaskOrProject.checkAccess(context, (short) AccessConstants.cModify);
//
//            	if (dmoTaskOrProject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)) {
//            		projectSpace.setId(objectId);
//                    String strAccess = projectSpace.getAccess(context);
//
//                    if (!ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD.equals(strAccess) &&
//                            !"Project Owner".equals(strAccess))
//                    {
//                        access = false;
//                    }
//            	}
//            }
//        }
//        catch (Exception ex)
//        {
//            throw ex;
//        }
//        finally
//        {
//            return access;
//        }
    }
    /**
     * hasRemoveAccess - This method verifies the user's permission for the given Project Member.
     * and Project Lead.Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * objectId   - String containing the objectId
     * @throws Exception if the operation fails
     * @since PMC X+2
     */
    public boolean hasRemoveAccess(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        com.matrixone.apps.program.ProjectSpace projectSpace=null;
        projectSpace =
            (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

        // if object id is passed, then we look at object permissions; otherwise
        // this screen is being accessed from project members, which means you can update.
        boolean access = false;

        try
        {
            if ((objectId != null) && !objectId.equals(""))
            {
                projectSpace.setId(objectId);
                String strAccess = projectSpace.getAccess(context);
                access =
                    projectSpace.checkAccess(context, (short) AccessConstants.cModify);
                if (!ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD.equals(strAccess)&&
                        !"Project Owner".equals(strAccess))  //PRG:RG6:R212:29-July-2011:Code Review
                {
                    access = false;
                }
                 /*commented for the bug no 337605 - Begin
                //added for the bug 310681 to restrict the display of the 'Remove Member' link to 'Project Lead' Access
                StringList objectSelects = new StringList(2);
                objectSelects.add(projectSpace.SELECT_OWNER);
                objectSelects.add(projectSpace.SELECT_PROJECT_VISIBILITY);

                Map projectMap = projectSpace.getInfo(context, objectSelects);

                String projOwner = (String) projectMap.get(projectSpace.SELECT_OWNER);
                String projVisibility =(String) projectMap.get(projectSpace.SELECT_PROJECT_VISIBILITY);
                if(strAccess.equals("Project Lead") && projVisibility != null && !"".equals(projVisibility) && !"null".equals(projVisibility) && projVisibility.equals("Members"))
                {
                    String loggedUser = context.getUser();
                    if(projOwner != null && !"".equals(projOwner) && !"null".equals(projOwner) && !loggedUser.equals(projOwner))
                    {
                        access = false;
                    }
                }

//              till here
/*commented for the bug no 337605 - End */


            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return access;
        }
    }

    /**
     * hasAccessToRoles -  This method return true if user have permissions on the roles
     * otherwise return false.Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * objectId   - String containing the objectID
     * @return Boolean set to true to retrive the project member's list othewise return false.
     * @throws Exception If the operation fails.
     * @since PMC 10-6
     */
    public boolean hasAccessToRoles(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");

        com.matrixone.apps.program.ProjectSpace projectSpace =
            (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

        // if object id is passed, then we look at object permissions; otherwise
        // this screen is being accessed from project members, which means you can update.
        boolean isAccessToRoles = false;
        boolean editFlag = false;
        try
        {
            String searchableRoles =
                EnoviaResourceBundle.getProperty(context, "eServiceProgramCentral.SearchableRoles");

            // Checking the role and access for displaying Create Link...

            if ((objectId != null) && !objectId.equals(""))
            {
                projectSpace.setId(objectId);
                String access = projectSpace.getAccess(context);
                editFlag =
                    projectSpace.checkAccess(context, (short) AccessConstants.cModify);
                if (!ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD.equals(access)&&
                        !"Project Owner".equals(access))  //PRG:RG6:R212:29-July-2011:Code Review
                {
                    editFlag = false;
                }
            }

            if (editFlag && !(searchableRoles.equals("None")))
            {
                isAccessToRoles = true;
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return isAccessToRoles;
        }
    }

    /**
     * hasAccessToGroups - This method return true if user have permissions on the groups
     * otherwise return false.Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *     objectId   - String containing the projectID
     * @return Boolean set to true to retrive the project member's list othewise return false.
     * @throws Exception If the operation fails.
     * @since PMC 10-6
     */
    public boolean hasAccessToGroups(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        com.matrixone.apps.program.ProjectSpace projectSpace =
            (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

        // if object id is passed, then we look at object permissions; otherwise
        // this screen is being accessed from project members, which means you can update.
        boolean isAccessToGroups = false;

        try
        {
            String searchableGroups =
                EnoviaResourceBundle.getProperty(context, "eServiceProgramCentral.SearchableGroups");

            // Checking the role and access for displaying Create Link...
            boolean editFlag = false;
            if ((objectId != null) && !objectId.equals(""))
            {
                projectSpace.setId(objectId);
                String access = projectSpace.getAccess(context);
                editFlag =
                    projectSpace.checkAccess(context, (short) AccessConstants.cModify);
                if (!ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD.equals(access)&&
                        !"Project Owner".equals(access))  //PRG:RG6:R212:29-July-2011:Code Review
                {
                    editFlag = false;
                }
            }

            if (editFlag && !(searchableGroups.equals("None")))
            {
                isAccessToGroups = true;
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return isAccessToGroups;
        }
    }

    /**
     * This method determines if the checkbox needs to be enabled in the column
     * of the PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        objectList - objectList Contains a MapList of Maps which contains objects.
     * @return Object of type Vector
     * @throws Exception if the operation fails
     * @since Common 10-0-0-0
     */
    public Vector showCheckbox(Context context, String[] args)
    throws Exception
    {
        Vector enableCheckbox = new Vector();
        try
        {
            com.matrixone.apps.common.Person person =
                (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PERSON);

            com.matrixone.apps.program.ProjectSpace project =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
            String objectId = (String) paramList.get("objectId");
            project.setId(objectId);
            String projectOwner =
                project.getInfo(context, project.SELECT_OWNER).toString();
            String ownerId = person.getPerson(context, projectOwner).getId();

            Iterator objectListItr = objectList.iterator();
            while (objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String currentProjectMemberId =
                    (String) objectMap.get(person.SELECT_ID);
                /*
                 Resource Loading Feature Change: Owner check box is enable for Resouce Loading.
                 Check added in process page to not to disconnect Owner from project
                 */
                /*if (currentProjectMemberId.equals("personid_" + ownerId))
                 {
                 enableCheckbox.add("false");
                 }
                 else
                 {
                 enableCheckbox.add("true");
                 }*/
                enableCheckbox.add("true");
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return enableCheckbox;
        }
    }

    /**
     * getMembers - This method gets the List of all Members added to the Project.
     * Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList contains list of project members
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMembers(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        MapList projectMemberList = null;
        MapList returnMemberList = new MapList();

        try
        {

            com.matrixone.apps.program.ProjectSpace project =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

            com.matrixone.apps.common.Person person =
                (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PERSON);

            StringList memberSelects = new StringList(6);
            memberSelects.add(person.SELECT_ID);
            memberSelects.add(person.SELECT_TYPE);
            memberSelects.add(person.SELECT_NAME);
            memberSelects.add(person.SELECT_LEVEL);
            memberSelects.add(person.SELECT_FIRST_NAME);
            memberSelects.add(person.SELECT_COMPANY_NAME);
            memberSelects.add(person.SELECT_LAST_NAME);

            StringList relSelects = new StringList(2);
            relSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
            relSelects.add(MemberRelationship.SELECT_PROJECT_ACCESS);
            relSelects.add(MemberRelationship.SELECT_ID);
            MapList membersList = null;

            project.setId(objectId);
            projectMemberList =
                project.getMembers(context, memberSelects, relSelects, null, null, true);
            Iterator projectMemberListItr = projectMemberList.iterator();
            Map objectMap = null;
            while (projectMemberListItr.hasNext())
            {
                objectMap = (Map) projectMemberListItr.next();
                String objType = (String) objectMap.get(person.SELECT_TYPE);
                String objName = (String) objectMap.get(person.SELECT_NAME);
                String objId = (String) objectMap.get(person.SELECT_ID);
                String level = (String) objectMap.get(person.SELECT_LEVEL);
                String userType = (String) objectMap.get(person.SELECT_TYPE);
                if (level == null)
                {
                    objectMap.put(person.SELECT_LEVEL, "1");
                }
                if (objType.equals(MemberRelationship.TYPE_PERSON))
                {
                    objectMap.put("id",  objId);
                }
                else
                {
                    objectMap.put("id", objName);
                }

                String lastName = (String) objectMap.get(person.SELECT_LAST_NAME);
                String firstName = (String) objectMap.get(person.SELECT_FIRST_NAME);
                String fullName = lastName + ", " + firstName;
                if ((lastName == null) && (firstName == null))
                {
                    fullName = (String) objectMap.get(person.SELECT_NAME);
                }
                objectMap.put(person.SELECT_NAME, fullName);
                returnMemberList.add((Map) objectMap);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return returnMemberList;
        }
    }

    /**
     * getAccessList - This method returns all the users that have access to the project space 
     * based on the P&O security context.
     * Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList contains list of project members
     * @throws Exception if the operation fails
     * @since PRG V6R2012x
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getSCAccessList(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        MapList projectMemberList = null;
        Map RequestValuesMap = (Map) programMap.get("RequestValuesMap");
        MapList returnMemberList = new MapList();                           
        
        try
        {
            com.matrixone.apps.program.ProjectSpace project =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
            com.matrixone.apps.common.Person person = null;

            project.setId(objectId);
                              
            String[] emxTableRowId = (String[]) RequestValuesMap.get("emxTableRowId");
            emxTableRowId = ProgramCentralUtil.parseTableRowId(context, emxTableRowId);
            if (emxTableRowId != null)
            {
                StringList objectSelects = new StringList(1);
                objectSelects.add(person.SELECT_ID);
                MapList existingMembers = project.getMembers(context, objectSelects, null, null, null, false);
                ArrayList idList = new ArrayList(existingMembers.size());
                for(int j=0; j < existingMembers.size(); j++)
                {
                    Map map = (Map) existingMembers.get(j);
                    idList.add(map.get(person.SELECT_ID));
                }
                
                Map members = new HashMap(emxTableRowId.length);
                StringList strPersonIds = new StringList();
                for(int i=0; i<emxTableRowId.length ; i++)
                {
                    String memberId = emxTableRowId[i];
                    int index = memberId.indexOf(":");
                    String role = memberId.substring(index+1);
                    memberId = memberId.substring(0, index);
                    if (idList.contains(memberId))
                    {
                        continue;
                    }
                    Map access = new HashMap(1);
                    access.put(MemberRelationship.ATTRIBUTE_PROJECT_ACCESS, role);
                    members.put(memberId, access);
                    strPersonIds.add(memberId);
                }
                if (members.size() > 0)
                {
                    project.addMembers(context, members);

                    //set folder access.
                    com.matrixone.apps.common.WorkspaceVault workspaceVault = (com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);                    
                    StringList busSelects = new StringList();
                    busSelects.add(WorkspaceVault.SELECT_NAME);
                    busSelects.add(WorkspaceVault.SELECT_ID);
                    busSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
                    workspaceVault.setContentRelationshipType(WorkspaceVault.RELATIONSHIP_VAULTED_OBJECTS_REV2);
                    MapList Vaultlist = WorkspaceVault.getWorkspaceVaults(context, project, busSelects, 0);
                    String strAccessXML = null;
                    ProjectRoleVaultAccess vaultAccess = null;

                    for(int i=0; i < Vaultlist.size(); i++)
                    {    
                        Map vaultMap = (Map) Vaultlist.get(i);    
                        String defaultAccess = (String) vaultMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
                        String strThisVaultId=(String)vaultMap.get(WorkspaceVault.SELECT_ID);

                        workspaceVault.setId(strThisVaultId);
                        Map accessMap = new HashMap();

                        for(int itr = 0; itr < strPersonIds.size(); itr++)
                        { 
                            String strObjectId = (String) strPersonIds.get(itr);
                            DomainObject domPerson = DomainObject.newInstance(context,strObjectId);
                            String strPerson= domPerson.getInfo(context,DomainConstants.SELECT_NAME);
                            accessMap.put(strPerson, defaultAccess);
                        }
                        workspaceVault.setUserPermissions(context, accessMap);
                    }  
                    String usersAddedMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
                    		"emxProgramCentral.SecurityContext.UsersAdded", context.getSession().getLanguage());                    
                    emxContextUtilBase_mxJPO.mqlNotice(context, usersAddedMsg);
                }
                else{
                    String noneAddedMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
                    		"emxProgramCentral.SecurityContext.NoneAdded", context.getSession().getLanguage());
                    MqlUtil.mqlCommand(context, "warning $1",noneAddedMsg);
                }
            }
            else
            {        
                StringList memberSelects = new StringList(6);
                memberSelects.add(person.SELECT_ID);
                memberSelects.add(person.SELECT_TYPE);
                memberSelects.add(person.SELECT_NAME);
                memberSelects.add(person.SELECT_FIRST_NAME);
                memberSelects.add(person.SELECT_LAST_NAME);
                memberSelects.add(person.SELECT_COMPANY_NAME);

                projectMemberList = project.getSCAccessList(context, memberSelects);

                Iterator projectMemberListItr = projectMemberList.iterator();
                Map objectMap = null;
                while (projectMemberListItr.hasNext())
                {
                    objectMap = (Map) projectMemberListItr.next();
                    String objType = (String) objectMap.get(person.SELECT_TYPE);
                    String objName = (String) objectMap.get(person.SELECT_NAME);
                    String objId = (String) objectMap.get(person.SELECT_ID);
                    String userType = (String) objectMap.get(person.SELECT_TYPE);

                    objectMap.put(person.SELECT_LEVEL, "1");
                    objectMap.put(person.SELECT_ID, objId + ":" + objectMap.get(MemberRelationship.SELECT_PROJECT_ACCESS));

                    String lastName = (String) objectMap.get(person.SELECT_LAST_NAME);
                    String firstName = (String) objectMap.get(person.SELECT_FIRST_NAME);
                    String fullName = lastName + ", " + firstName;
                    if ((lastName == null) && (firstName == null))
                    {
                        fullName = (String) objectMap.get(person.SELECT_NAME);
                    }
                    objectMap.put(person.SELECT_NAME, fullName);
                    returnMemberList.add((Map) objectMap);
                }
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return returnMemberList;
        }
    }

    /**
     * getMembersFor Edit - This method gets the List of all Members added to the Project.
     * Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList contains list of project members
     * @throws Exception if the operation fails
     * @since PMC X+2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMembersForEdit(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        String objectId = (String) programMap.get("objectId");
        MapList projectMemberList = null;
        MapList returnMemberList = new MapList();

        try
        {
            com.matrixone.apps.program.ProjectSpace project =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

            com.matrixone.apps.common.Person person =
                (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PERSON);

            StringList memberSelects = new StringList(6);
            memberSelects.add(DomainConstants.SELECT_ID);
            memberSelects.add(DomainConstants.SELECT_TYPE);
            memberSelects.add(DomainConstants.SELECT_NAME);
            memberSelects.add(DomainConstants.SELECT_LEVEL);
            memberSelects.add(Person.SELECT_FIRST_NAME);
            memberSelects.add(Person.SELECT_COMPANY_NAME);
            memberSelects.add(Person.SELECT_LAST_NAME);

            StringList relSelects = new StringList(2);
            relSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
            relSelects.add(MemberRelationship.SELECT_PROJECT_ACCESS);
            project.setId(objectId);
            projectMemberList =
                project.getMembers(context, memberSelects, relSelects, null, null, true);
            Iterator projectMemberListItr = projectMemberList.iterator();
            Map objectMap = null;
            while (projectMemberListItr.hasNext())
            {
                objectMap = (Map) projectMemberListItr.next();
                String objType = (String) objectMap.get(DomainConstants.SELECT_TYPE);
                String objId = (String) objectMap.get(DomainConstants.SELECT_ID);
                String level = (String) objectMap.get(DomainConstants.SELECT_LEVEL);
                if (level == null)
                {
                    objectMap.put(DomainConstants.SELECT_LEVEL, "1");
                }
                if (objType.equals(MemberRelationship.TYPE_PERSON))
                {
                    objectMap.put("id",  objId);
                }
                else
                {
                    objectMap.put("id", objectId);
                }

                String lastName = (String) objectMap.get(Person.SELECT_LAST_NAME);
                String firstName = (String) objectMap.get(Person.SELECT_FIRST_NAME);
                String fullName = lastName + ", " + firstName;
                if ((lastName == null) && (firstName == null))
                {
                    fullName = (String) objectMap.get(DomainConstants.SELECT_NAME);
                }
                objectMap.put(DomainConstants.SELECT_NAME, fullName);
                returnMemberList.add((Map) objectMap);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return returnMemberList;
        }
    }
     /**
     * getName - This Method is used to displays the person's last name and first name
     * or the name of the group or role.The person's name has a link to open a pop up
     * with the owner.Used for PMCProjectMemberSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     *    paramList  - Map containing parameters for cloning the object
     * @return Vector containing the project member name as String
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getName(Context context, String[] args)
    throws Exception
    {
        Vector equivalentLinkVector = new Vector();

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Map paramList = (Map) programMap.get("paramList");
        String viewMode=(String) paramList.get("isViewMode");
        String jsTreeID = (String) paramList.get("jsTreeID");
        String language = context.getSession().getLanguage();
        String strSuiteDir = (String) paramList.get("SuiteDirectory");
        String portalMode = (String) paramList.get("portalMode");
        String customSortDirections = (String) paramList.get("customSortDirections");
        String customSortColumns = (String) paramList.get("customSortColumns");
        if (ProgramCentralUtil.isNotNullString(customSortColumns) && ProgramCentralUtil.isNotNullString(customSortDirections)){
        	StringList saSortDirection = FrameworkUtil.splitString(customSortDirections, ",");
        	StringList saSortColumn = FrameworkUtil.splitString(customSortColumns, ",");
        	for(int i = 0; i < saSortColumn.size(); i++) {
        		customSortColumns = (String)saSortColumn.get(i);
        		customSortDirections = (String)saSortDirection.get(i);
        		if ("Name".equalsIgnoreCase(customSortColumns)){        		
        			customSortColumns = "name";        
        		}
        		else if("Access".equalsIgnoreCase(customSortColumns)) {
        			customSortColumns = "attribute["+ProgramCentralConstants.ATTRIBUTE_PROJECT_ACCESS+"].value";
        		}
        		else if(("Project Role".equalsIgnoreCase(customSortColumns)) || ("ProjectRole".equalsIgnoreCase(customSortColumns))) {
        			customSortColumns = "attribute["+ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE+"].value";
        		}
        		if ("descending".equalsIgnoreCase(customSortDirections)) {
        			objectList.sort(customSortColumns, "descending", "string");
        		} else {
        			objectList.sort(customSortColumns, "ascending", "string");
        		}
        	}
        }
        boolean isPrinterFriendly = false;
        String strPrinterFriendly = (String)paramList.get("reportFormat");
        if ( strPrinterFriendly != null ) {
            isPrinterFriendly = true;
        }
        try
        {
            Map objectMap = null;

            Iterator objectListIterator = objectList.iterator();
            while (objectListIterator.hasNext())
            {
                StringBuffer equivalentLink = new StringBuffer();
                objectMap = (Map) objectListIterator.next();

                String fullName = XSSUtil.encodeForHTML(context,(String) objectMap.get(DomainObject.SELECT_NAME));
                String memberAccess =
                    (String) objectMap.get(MemberRelationship.SELECT_PROJECT_ACCESS);
                String userType = (String) objectMap.get(DomainObject.SELECT_TYPE);

                
                boolean lead =(ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD.equals(memberAccess) ||
                			"Project Owner".equals(memberAccess)); //PRG:RG6:R212:29-July-2011:Code Review

                String preURL = "";
                String target = "";
                if("true".equals(viewMode))
                {
                    if ((jsTreeID == null) || ("null".equals(jsTreeID)))
                    {
                        preURL =
                            "../common/emxTree.jsp?mode=replace&amp;AppendParameters=false&amp;objectId=";
                        target = " target=\"content\"";
                    }
                    else
                    {
                        preURL =
                            "../common/emxTree.jsp?AppendParameters=false&amp;mode=insert&amp;jsTreeID=" +
                            jsTreeID + "&amp;objectId=";
                        target = " target=\"content\"";
                    }
                }else{

                    if ((jsTreeID == null) || ("null".equals(jsTreeID)))
                    {
                        preURL =
                            "../common/emxTree.jsp?mode=replace&amp;AppendParameters=false&amp;objectId=";
                        target = " target=\"popup\"";
                    }
                    else
                    {
                        preURL =
                            "../common/emxTree.jsp?AppendParameters=false&amp;mode=insert&amp;jsTreeID=" +
                            jsTreeID + "&amp;objectId=";
                        target = " target=\"popup\"";
                    }
                }


                String currentProjectMemberId = (String) objectMap.get(DomainObject.SELECT_ID);
                String personId = "";
                //obtain the person id
                if (!currentProjectMemberId.equals(""))
                {
                	if (currentProjectMemberId.contains("_")) {
                    int pVal = currentProjectMemberId.indexOf("_") + 1;
                    personId =
                        currentProjectMemberId.substring(pVal,
                                currentProjectMemberId.length());
                	} else if (currentProjectMemberId.contains(ProgramCentralConstants.COLON)) {
                		//This is added for AccessList tab (which comes in SMB only), member id comes with Role value.
                		//e.g. 4816.41682.52144.44478:Project Lead.
                		String[] stValueArray	= currentProjectMemberId.split(ProgramCentralConstants.COLON);
                		personId = stValueArray[0];
                	}
                	else {
                		personId = currentProjectMemberId;
                	}
                }

                // set object id in url
                String nextURL = preURL + personId;
                String imageStr = "";
                if (lead)
                {
                    imageStr = "../common/images/iconSmallProjectLead.gif";
                }
                else if(userType.equals("Role") || userType.equals("Group"))
                {
                    imageStr = "../common/images/iconSmallGroup.gif";
                }
                else
                {
                    imageStr = "../common/images/iconSmallPerson.gif";
                }

                if(!isPrinterFriendly)
                {
                    // this is just for sorting functionality, the sorting considers the entire string
                    equivalentLink.append("<input type='hidden' name='forsort' value='" +
                            fullName + "'/>");
                    if ((userType != null) && userType.equals("Role"))
                    {
                        fullName = i18nNow.getRoleI18NString(fullName, language);
                    }
                    if ((userType != null) && userType.equals("Group"))
                    {
                        fullName = i18nNow.getMXI18NString(fullName, "", language, "Group");
                    }
                    
                    if("true".equals(viewMode))
                    {
                        if (userType.equals(MemberRelationship.TYPE_PERSON))
                        {
                            equivalentLink.append("<img src=\"" + imageStr + "\" border=\"0\"/>");
                            equivalentLink.append("<a href='" + nextURL);
                            equivalentLink.append("&amp;emxSuiteDirectory=").append(strSuiteDir);
                            equivalentLink.append("&amp;defaultsortname=").append(fullName);
                            equivalentLink.append("&amp;jsTreeID=").append(jsTreeID);
                            equivalentLink.append("' class='object' target='content' >");
                            equivalentLink.append(fullName);
                            equivalentLink.append("</a>");

                        }
                        else if (userType.equals("Role") || userType.equals("Group"))
                        {
                            equivalentLink.append("<img src=\"" + imageStr + "\" border=\"0\"/>");
                            equivalentLink.append(fullName);
                        } else {
                            equivalentLink.append(fullName);
                        }

                    }
                    else{
                        if (userType.equals(MemberRelationship.TYPE_PERSON))
                        {
                            equivalentLink.append("<img src=\"" + imageStr + "\" border=\"0\"/>");
                            equivalentLink.append("<a href='" + nextURL);
                            equivalentLink.append("&amp;emxSuiteDirectory=").append(strSuiteDir);
                            equivalentLink.append("&amp;defaultsortname=").append(fullName);
                            equivalentLink.append("&amp;jsTreeID=").append(jsTreeID);
                            equivalentLink.append("' class='object' target='content' >");
                            equivalentLink.append(fullName);
                            equivalentLink.append("</a>");
                        }
                        else if (userType.equals("Role") || userType.equals("Group"))
                        {
                            equivalentLink.append("<img src=\"" + imageStr + "\" border=\"0\"/>");
                            equivalentLink.append(fullName);
                        } else {
                            equivalentLink.append(fullName);
                        }
                    }
                }else{ 
                	equivalentLink.append(fullName);
                }
                equivalentLinkVector.addElement(equivalentLink.toString());
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return equivalentLinkVector;
        }
    }

    /**
     * getCompName - This method shows the company person members belong to.
     * Used for PMCProjectMemberSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the organisation value as String
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getCompName(Context context, String[] args)
    throws Exception
    {
        Vector vectCompany = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        try
        {
            com.matrixone.apps.common.Person person =
                (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PERSON);

            Map objectMap = null;
            Iterator objectListIterator = objectList.iterator();

            while (objectListIterator.hasNext())
            {
                StringBuffer equivalentLink = new StringBuffer();
                objectMap = (Map) objectListIterator.next();

                String organization =
                    (String) objectMap.get(person.SELECT_COMPANY_NAME);

                if ((organization == null) || "null".equals(organization))
                {
                    organization = "";
                }
                vectCompany.add(organization);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vectCompany;
        }
    }

    /**
     * getTypeName - This method is used to get the type name as objects.
     * whether the member type is a person, group or role.
     * Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the type value as String
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getTypeName(Context context, String[] args)
    throws Exception
    {
        Vector vectType = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        String language = context.getSession().getLanguage();

        i18nNow i18nnow = new i18nNow();
        String propertyFile = "emxProgramCentralStringResource";

        String i18nGroup = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
        		"emxProgramCentral.PersonDialog.Group", language);
        String i18nRole = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
        		"emxProgramCentral.BasicSearch.Role", language);
        String i18nPerson = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
        		"emxProgramCentral.PersonDialog.Person", language);
        try
        {
            Map objectMap = null;

            Iterator objectListIterator = objectList.iterator();
            while (objectListIterator.hasNext())
            {
                StringBuffer equivalentLink = new StringBuffer();
                objectMap = (Map) objectListIterator.next();

                String typeStr = (String) objectMap.get(DomainConstants.SELECT_TYPE);
                if ((typeStr == null) || "null".equals(typeStr))
                {
                    typeStr = "";
                }
                else
                {
                    if (ProgramCentralConstants.TYPE_PERSON.equals(typeStr))//PRG:RG6:R212:29-July-2011:Code Review
                    {
                        typeStr = i18nPerson;
                    }
                    else if (typeStr.equals("Role"))
                    {
                        typeStr = i18nRole;
                    }
                    else
                    {
                        typeStr = i18nGroup;
                    }
                }
                vectType.add(typeStr);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vectType;
        }
    }


    /**
     * getAccessType - This method is used to get the Access type name as objects.
     * Project members have access for Project Member,Project Lead and Project Owner.
     * Used for PMCProjectMemberSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the access type value as String
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getAccessType(Context context, String[] args)
    throws Exception
    {
        com.matrixone.apps.common.MemberRelationship member = (com.matrixone.apps.common.MemberRelationship) DomainRelationship.newInstance(context, DomainConstants.RELATIONSHIP_MEMBER);
        Vector vectAccessType = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Map paramList = (Map) programMap.get("paramList");
        String language = (String) paramList.get("languageStr");
        String viewMode=(String) paramList.get("isViewMode");
        //build the choices for access
        StringList accessList = mxAttr.getChoices(context,
                member.ATTRIBUTE_PROJECT_ACCESS);
		//Modified:29-Apr-09:wqy:R207:PRG Bug :373332
        //accessList.remove("Project Owner");
        StringList i18nAccessList = new StringList();
        StringItr strItrAccess = new StringItr(accessList);
        while(strItrAccess.next()){
            i18nAccessList.addElement(i18nNow.getRangeI18NString(member.ATTRIBUTE_PROJECT_ACCESS,strItrAccess.obj(),language));
        }

        //Added:07-May-09:nzf:R207:PRG:Bug:374952
        StringList accessListDisplay = new StringList();
        accessListDisplay.addAll(accessList);
        StringList i18nAccessListDisplay = new StringList();
        i18nAccessListDisplay.addAll(i18nAccessList);
        //End:R207:PRG:Bug:374952

        //Added:29-Apr-109:wqy:R207:PRG:Bug:373332
        int nIndexAccess = accessList.indexOf("Project Owner");
        if(nIndexAccess != -1){
            accessList.remove(nIndexAccess);
            i18nAccessList.remove(nIndexAccess);
        }
        //End:R207:PRG:Bug:373332

        try
        {
            Map objectMap = null;

            Iterator objectListIterator = objectList.iterator();
            int count=0;
            while (objectListIterator.hasNext())
            {
                StringBuffer equivalentLink = new StringBuffer();
                StringBuffer returnString = new StringBuffer();
                objectMap = (Map) objectListIterator.next();
                String typeStr = (String) objectMap.get(DomainConstants.SELECT_TYPE);
                String nameStr = (String) objectMap.get(DomainConstants.SELECT_NAME);
                String currentProjectMemberId = (String) objectMap.get(DomainConstants.SELECT_ID);
                String selectId = currentProjectMemberId.replace('.','_');
                String memberAccess = (String) objectMap.get(MemberRelationship.SELECT_PROJECT_ACCESS);

                if(!"true".equals(viewMode))
                {
                    if(!memberAccess.equals("Project Owner"))
                    {
                        if(typeStr.equalsIgnoreCase("Role") || typeStr.equalsIgnoreCase("Group")){
                        	 //Modified:11-June-10:ak4:R210:PRG:Bug:047788
                             nameStr = nameStr.replace(' ','_');
                             // returnString.append("<select name=\"Access_"+nameStr+"\">");
                        	 returnString.append("<input type='hidden' name='Access_"+nameStr+"' value='"+XSSUtil.encodeForHTML(context,memberAccess)+"'/>");
                        	 int nIndex = accessListDisplay.indexOf(memberAccess);
                             String strIntMemberAccess = (String)i18nAccessListDisplay.get(nIndex);
                             returnString.append(strIntMemberAccess);
                        }
                        else{
                        //returnString.append("<select name=\"Access_"+selectId+"\">");
						//Added:29-Apr-09:nr2:R207:PRG Bug :371519
                        returnString.append("<select name=\"Access"+count+"\">");
						//End:R207:PRG Bug :371519
                       
                       for( int i =0; i<accessList.size() ; i++)
                        {
                          if(memberAccess.equals(accessList.get(i).toString())){
                            returnString.append("<option value='"+accessList.get(i)+"' selected='selected'>"+i18nAccessList.get(i)+"</option>");
                          }
                          else {
                            returnString.append("<option value='"+accessList.get(i)+"'>"+i18nAccessList.get(i)+"</option>");
                          }
                        }
                        returnString.append("</select>");
                        }
                        //End:11-June-10:ak4:R210:PRG:Bug:047788
                    }
                    else
                    {
                        //Modified:07-May-09:nzf:R207:PRG:Bug:374952

                        //returnString.append("<input type=hidden name=\"Access_"+selectId+"\" value=\""+memberAccess+"\">");
						//Added:29-Apr-09:nr2:R207:PRG Bug :371519
                        returnString.append("<input type='hidden' name='Access"+count+"' value='"+XSSUtil.encodeForHTML(context,memberAccess)+"'/>");
						//End:R207:PRG Bug :371519
						//Added:29-Apr-109:wqy:R207:PRG:Bug:373332
                        int nIndex = accessListDisplay.indexOf(memberAccess);
                        String strIntMemberAccess = (String)i18nAccessListDisplay.get(nIndex);
 						//End:R207:PRG:Bug:373332
                        returnString.append(strIntMemberAccess);

                        // End:R207:PRG:Bug:373332
                    }
                }else
                {
                    // Modified:07-May-09:wqy:R207:PRG:Bug:374952

					//Added:29-Apr-109:wqy:R207:PRG:Bug:373332
                    int nIndex = accessListDisplay.indexOf(memberAccess);
                    String strIntMemberAccess = (String)i18nAccessListDisplay.get(nIndex);
					//End:R207:PRG:Bug:373332
                    returnString.append(strIntMemberAccess);

                    // End:R207:PRG:Bug:373332
                }

                vectAccessType.add(returnString.toString());
                count++;
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vectAccessType;
        }
    }

    /**
     * getProjectRole - This Method indicates the role the person serves for the project.
     * Used for PMCProjectMemberSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector of "Project Role" values for each row
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */

    public Vector getProjectRole(Context context, String[] args)
    throws Exception
    {
        com.matrixone.apps.common.MemberRelationship member = (com.matrixone.apps.common.MemberRelationship) DomainRelationship.newInstance(context, DomainConstants.RELATIONSHIP_MEMBER);


        Map memberMap = member.getTypeAttributes(context, member.RELATIONSHIP_MEMBER);

        Map roleMap = (Map) memberMap.get(member.ATTRIBUTE_PROJECT_ROLE);
        StringList roleList = (StringList)roleMap.get("choices");

        //Added:14-OCT-10:S2E:R210:PRG TVT-Defect-Weekly_Timesheet-null_values in lookup tablemembers_edit_all-FR
        String showRDORoles = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.showRDORoles");
		//String showRDORoles = "true";
		if(showRDORoles!= null && !"true".equalsIgnoreCase(showRDORoles))
		{
			//To filter out any Role choices which have a symbolic name and start with "role_"
			Iterator roleItr = roleList.iterator();
			while(roleItr.hasNext()){
				if(((String)roleItr.next()).startsWith("role_")){
					roleItr.remove();
				}
			}
		}
		//End:14-OCT-10:S2E:R210:PRG TVT-Defect-Weekly_Timesheet-null_values in lookup tablemembers_edit_all-FR

       Map mapRoleI18nStrings = geti18nProjectRoleRDOValues(context);

        Vector vectProRole = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");

        Map paramList = (Map) programMap.get("paramList");
        String language = (String) paramList.get("languageStr");
        String viewMode=(String) paramList.get("isViewMode");
        try
        {
            Map objectMap = null;

            Iterator objectListIterator = objectList.iterator();
            int count=0;
            while (objectListIterator.hasNext())
            {
                StringBuffer equivalentLink = new StringBuffer();
                objectMap = (Map) objectListIterator.next();
                String projectAcc =(String) objectMap.get(MemberRelationship.SELECT_PROJECT_ROLE);

                String typeStr = (String) objectMap.get(DomainConstants.SELECT_TYPE);
                String currentProjectMemberId = (String) objectMap.get(DomainConstants.SELECT_ID);

                String selectedRole = "";
                 String i18nProjectRoleValue = "";

                if (projectAcc != null)
                {
                    selectedRole = (String) objectMap.get(MemberRelationship.SELECT_PROJECT_ROLE);
                	i18nProjectRoleValue = (String)mapRoleI18nStrings.get(selectedRole);

                }

                // iPlanet prints null as the string "null".  Reassign null to
                // empty string to prevent this.
                else
                {
                    i18nProjectRoleValue = "";
                }
                StringBuffer returnString= new StringBuffer();
                String selectId = currentProjectMemberId.replace('.','_');

                if(!"true".equals(viewMode))
                {
                    if(typeStr.equals(member.TYPE_PERSON))
                    {
					    //Added:29-Apr-09:nr2:R207:PRG Bug :371519
                        returnString.append("<select name=\"ProjectRole"+count+"\">");
						//End:R207:PRG Bug:371519
                        for(int i=0; i<roleList.size() ; i++)
                        {
                        	String role=(String)roleList.get(i);

                        	String strSelected = "";
                        	//Modified:3-Mar-10:VF2:R209:PRG Bug :036275
                        	if(selectedRole.equalsIgnoreCase(role))
                    		{
                    			strSelected = "selected";
                    		}
                        	//End:3-Mar-10:VF2:R209:PRG Bug :036275
                        	if(null != role && !"null".equals(role) && !"".equals(role)&& !role.startsWith("role_")){
                        		returnString.append("<option value='" + roleList.get(i) + "' selected='" + strSelected + "'>" + (String)mapRoleI18nStrings.get(roleList.get(i)) + "</option>");
                         	//Added:3-Mar-10:VF2:R209:PRG Bug :036275
                        	} else {
                        		returnString.append("<option value='" + roleList.get(i) + "' selected='" + strSelected + "'>" + (String)mapRoleI18nStrings.get(roleList.get(i)) + "</option>");
                        	}
                        	//End:3-Mar-10:VF2:R209:PRG Bug :036275                       	
                        }
                        returnString.append("</select>");
                    }
                    else
                    {
                        returnString.append(i18nProjectRoleValue);
                    }

                }
                else
                {
                	// [Modified::PRG:RG6:Jan 20, 2011::R211::not null check added for view mode]
                	if(ProgramCentralUtil.isNotNullString(i18nProjectRoleValue))
                	{
                    returnString.append(i18nProjectRoleValue);
                }
                	else
                	{
                		if(ProgramCentralUtil.isNotNullString(selectedRole))
                		{
                			returnString.append(selectedRole);
                		}
                		else
                		{
                			returnString.append("");
                		}
                	}
                }

                vectProRole.add(returnString.toString());
                count++;
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vectProRole;
        }
    }

    /**
     * Returns the translated values for range of attribute "Project Role". It also considers key emxProgramCentral.showRDORoles to
     * return RDO related roles.
     * If the translated values or normal project role and RDO roles match they are modified to have (Project Role) or (RDO) suffixes defined by
     * string resource key
     * 	emxProgramCentral.MemberRoles.Suffix.RDO
     * 	emxProgramCentral.MemberRoles.Suffix.ProjectRole
     *
     * @param context The Matrix Context Object
     * @return Map of translated values of role values, against key as role name
     * @throws MatrixException
     * @since PRG V6R2011
     */
	public Map geti18nProjectRoleRDOValues(Context context) throws MatrixException {

		try {
			AttributeType attrProjectRole = new AttributeType(DomainConstants.ATTRIBUTE_PROJECT_ROLE);
			attrProjectRole.open(context);
			StringList roleList = attrProjectRole.getChoices(context);
			attrProjectRole.close(context);

			String showRDORoles = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.showRDORoles");
			//String showRDORoles = "true";
			if(showRDORoles!= null && !"true".equalsIgnoreCase(showRDORoles))
			{
				//To filter out any Role choices which have a symbolic name and start with "role_"
				Iterator roleItr = roleList.iterator();
				while(roleItr.hasNext()){
					if(((String)roleItr.next()).startsWith("role_")){
						roleItr.remove();
					}
				}
			}

			final String STRING_SUFFIX_RDO = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.MemberRoles.Suffix.RDO", context.getSession().getLanguage());
			Map mapRoleI18nStrings = new HashMap();

			StringItr rolesList2 = new StringItr(roleList);
			while(rolesList2.next())
			{
				String sProjectRole = (String)rolesList2.obj();

				if ("".equals(sProjectRole))
				{
					mapRoleI18nStrings.put("", "");
					continue;
				}

				if (mapRoleI18nStrings.containsKey(sProjectRole)) {
					continue;
				}

				if(sProjectRole.startsWith("role_"))
				{
					String sIntProjectRole = PropertyUtil.getSchemaProperty(context, sProjectRole);

	
					if(ProgramCentralUtil.isNullString(sIntProjectRole)) //RG6: null check is modified :R211
					{
						sIntProjectRole = sProjectRole;
					}
					else
					{
						sIntProjectRole = i18nNow.getRoleI18NString(sIntProjectRole, context.getSession().getLanguage());

					}

					if (mapRoleI18nStrings.containsValue(sIntProjectRole))
					{
						for (Iterator itrKeys = mapRoleI18nStrings.keySet().iterator(); itrKeys.hasNext();)
						{
							String strKey = (String)itrKeys.next();
							String strValue = (String)mapRoleI18nStrings.get(strKey);
							if (strValue.length()!=0 && strValue.equals(sIntProjectRole))
							{
								//Added:3-Mar-10:VF2:R209:PRG Bug :036275
								//mapRoleI18nStrings.put(strKey, sIntProjectRole + STRING_SUFFIX_PROJECT_ROLE);
								mapRoleI18nStrings.put(strKey, sIntProjectRole);
								//End:3-Mar-10:VF2:R209:PRG Bug :036275
								break;
							}
						}
						if (!"".equals(sIntProjectRole))
						{
							sIntProjectRole += STRING_SUFFIX_RDO;
						}
					}

					mapRoleI18nStrings.put(sProjectRole, sIntProjectRole);
				}
				else
				{
					String sIntProjectRole = i18nNow.getRangeI18NString(DomainConstants.ATTRIBUTE_PROJECT_ROLE,sProjectRole, context.getSession().getLanguage());

					if (mapRoleI18nStrings.containsValue(sIntProjectRole))
					{
						for (Iterator itrKeys = mapRoleI18nStrings.keySet().iterator(); itrKeys.hasNext();)
						{
							String strKey = (String)itrKeys.next();
							String strValue = (String)mapRoleI18nStrings.get(strKey);
							if (strValue.length()!=0 && strValue.equals(sIntProjectRole))
							{
								mapRoleI18nStrings.put(strKey, sIntProjectRole + STRING_SUFFIX_RDO);
								break;
							}
						}
						if(!"".equals(sIntProjectRole))
						{
							//Added:3-Mar-10:VF2:R209:PRG Bug :036275
							//sIntProjectRole += STRING_SUFFIX_PROJECT_ROLE;
							sIntProjectRole = sIntProjectRole;
							//End:3-Mar-10:VF2:R209:PRG Bug :036275
						}
					}

					mapRoleI18nStrings.put(sProjectRole, sIntProjectRole);
				}
			}

			return mapRoleI18nStrings;
		}
		catch (Exception e) {
			throw new MatrixException(e);
		}
	}

    /**
     * showEditDetails - This method is used to "Edit" the details of project members.
     * Used for PMCProjectMemberSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     *    paramList  - Map containing parameters for cloning the object
     * @return Vector containing the project member value as String
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector showEditDetails(Context context, String[] args)
    throws Exception
    {
        Vector vecEditDetails = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Map paramList = (Map) programMap.get("paramList");
        //  String boolEdit=(String) paramList.get("editTableMode");
        String objectId = (String) paramList.get("objectId");
//      Added for the 310681 Begin
//      Checking access to Roles

        com.matrixone.apps.program.ProjectSpace projectSpace =
            (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
        String searchableRoles =
            EnoviaResourceBundle.getProperty(context, "eServiceProgramCentral.SearchableRoles");
        boolean editFlag = false;
        if ((objectId != null) && !objectId.equals(""))
        {
            projectSpace.setId(objectId);
            editFlag =
                projectSpace.checkAccess(context, (short) AccessConstants.cModify);
        }
//      Added for the Bug 310681 End
        boolean isPrinterFriendly = false;
        String strPrinterFriendly = (String)paramList.get("reportFormat");
        if ( strPrinterFriendly != null ) {
            isPrinterFriendly = true;
        }
        try
        {
            Map objectMap = null;
            Iterator objectListIterator = objectList.iterator();
            while (objectListIterator.hasNext())
            {
                StringBuffer equivalentLink = new StringBuffer();
                objectMap = (Map) objectListIterator.next();


                String fullName = (String) objectMap.get(DomainObject.SELECT_NAME);
                String memberAccess =
                    (String) objectMap.get(MemberRelationship.SELECT_PROJECT_ACCESS);
                String userType = (String) objectMap.get(DomainObject.SELECT_TYPE);
                String currentProjectMemberId =
                    (String) objectMap.get(DomainObject.SELECT_ID);
                String personId = "";
                String language = context.getSession().getLanguage();
                //obtain the person id
                if (!currentProjectMemberId.equals(""))
                {
                    int pVal = currentProjectMemberId.indexOf("_") + 1;
                    personId =
                        currentProjectMemberId.substring(pVal,
                                currentProjectMemberId.length());
                }
                String imageStr = "../common/images/iconActionEdit.gif";
                // used for the edit icon
                String url = null;
                if (userType.equals(MemberRelationship.TYPE_PERSON))
                {
                    url =
                        "../programcentral/emxProgramCentralMemberEditAccessDialogFS.jsp?objectId=" +
                        objectId + "&personId=" + personId + "&userType=" + userType;
                }
                else
                {
                    url =
                        "../programcentral/emxProgramCentralMemberEditAccessDialogFS.jsp?objectId=" +
                        objectId + "&personId=" + fullName + "&userType=" + userType;
                }
                if(!isPrinterFriendly) {
                    // Added for the Bug 310681 Begin
                    // Based on the editFlag value ,providing the edit access to the member list
                    if(editFlag)
                    {
                        equivalentLink.append("<a href='javascript:showDialog(\"" + url +
                        "\",930,650)'>");
                        equivalentLink.append("<img src=\"" + imageStr + "\" border=\"0\">");
                        equivalentLink.append("</a>&nbsp;");
                    } else { equivalentLink.append("<img src=\"" + imageStr + "\" border=\"0\">"); }
                    //Added for the Bug 310681 End.
                } else {
                    equivalentLink.append("<img src=\"" + imageStr + "\" border=\"0\">");
                }
                vecEditDetails.addElement(equivalentLink.toString());
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vecEditDetails;
        }


    }


    /**Start Addition for bug 315645.
     * This method gets field value for attributes Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @return a String containing the HTML content to be displayed.
     * @throws Exception if the operation fails
     * @since PMC 10-6-SP2
     */
    public String getDay(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String strMode = (String) requestMap.get("mode");
        String objectId = (String) paramMap.get("objectId");
        DomainObject dom = DomainObject.newInstance(context, objectId);
        String symval ="";
        HashMap fieldMap = (HashMap)programMap.get("fieldMap");
        String name =   (String)fieldMap.get("name");
        //Added for Bug#340197 on 08/23/2007 - Start
        String reportFormat = (String) requestMap.get("reportFormat");
        //Added for Bug#340197 on 08/23/2007 - End
        if(name.equals("Sun"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Sunday");
        }
        else if(name.equals("Mon"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Monday");
        }
        else if (name.equals("Tue"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Tuesday");
        }
        else if (name.equals("Wed"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Wednesday");
        }
        else if (name.equals("thu"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Thursday");
        }
        else if (name.equals("Fri"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Friday");
        }
        else
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Saturday");
        }
        String val = dom.getAttributeValue(context,symval);
        String dbval = "db"+name;
        StringBuffer returnString= new StringBuffer();
        if(strMode == null)
        {
            returnString.append(val);
        }
        else
        {
            returnString.append("<input type=\"text\" name=\""+name+"\" value=\""+XSSUtil.encodeForHTML(context,val)+"\" onChange=\"javascript:updateFieldsEfforts();\">");
        }
        //Added for Bug#340197 on 08/23/2007 - Start
        if(reportFormat == null)
        {
        //Added for Bug#340197 on 08/23/2007 - End
        returnString.append("<input type=\"hidden\" name=\""+dbval+"\"  value=\""+XSSUtil.encodeForHTML(context,val)+"\">");

        //COMMENTED FOR BUG : 356897
        /*returnString.append("<script language=\"javascript\">");
        returnString.append("function updateFields() {");
        returnString.append("var form_obj = document.editDataForm;  var sun = trimWhitespace(form_obj.Sun.value);");
        returnString.append("if (isNaN(sun) || sun.length==0 || sun < 0) {sun=0;  form_obj.Sun.value=sun;  }");

        returnString.append("var org_total = form_obj.originalremainingEffort.value;");
        returnString.append("var org_sun = form_obj.dbSun.value;");
        returnString.append("var org_mon = form_obj.dbMon.value;");
        returnString.append("var org_tue = form_obj.dbTue.value;");
        returnString.append("var org_wed = form_obj.dbWed.value;");
        returnString.append("var org_thu = form_obj.dbthu.value;");
        returnString.append("var org_fri = form_obj.dbFri.value;");
        returnString.append("var org_sat = form_obj.dbsat.value;");
        returnString.append("var diff_sun = parseInt(sun,10)- parseInt(org_sun,10) ;");

        returnString.append("var mon = trimWhitespace(form_obj.Mon.value);");
        returnString.append("if (isNaN(mon) || mon.length==0 || mon< 0 ) { mon=0; form_obj.Mon.value=mon;}");
        returnString.append("var diff_mon =  parseInt(mon,10)- parseInt(org_mon,10) ;");

        returnString.append("var tue = trimWhitespace(form_obj.Tue.value);  if (isNaN(tue) || tue.length==0 || tue < 0) {");
        returnString.append("tue=0;  form_obj.Tue.value=tue }");
        returnString.append("var diff_tue = parseInt(tue,10)- parseInt(org_tue,10) ;");

        returnString.append("var wed = trimWhitespace(form_obj.Wed.value);");
        returnString.append("if (isNaN(wed) || wed.length==0 || wed < 0 ) { wed=0; form_obj.Wed.value=wed; }");
        returnString.append("var diff_wed = parseInt(wed,10)- parseInt(org_wed,10) ;");

        returnString.append("var thu = trimWhitespace(form_obj.thu.value);");
        returnString.append("if (isNaN(thu) || thu.length==0 || thu < 0) {thu=0; form_obj.thu.value=thu; }");
        returnString.append("var diff_thu = parseInt(thu,10)- parseInt(org_thu,10) ;");

        returnString.append("var fri = trimWhitespace(form_obj.Fri.value);");
        returnString.append("if (isNaN(fri) || fri.length==0 || fri < 0 ) { fri=0; form_obj.Fri.value=fri; }");
        returnString.append("var diff_fri = parseInt(fri,10)- parseInt(org_fri,10) ;");

        returnString.append("var sat = trimWhitespace(form_obj.sat.value);");
        returnString.append("if (isNaN(sat) || sat.length==0 || sat < 0 )  {sat=0; form_obj.sat.value=sat; }");
        returnString.append("var diff_sat = parseInt(sat,10)- parseInt(org_sat,10) ;");

        returnString.append("var subtotal = parseInt(diff_mon,10) + parseInt(diff_tue,10) + parseInt(diff_wed,10) + ");
        returnString.append("parseInt(diff_thu,10) + parseInt(diff_fri,10) + parseInt(diff_sat,10) + parseInt(diff_sun,10);");
        returnString.append("org_total=parseInt(org_total,10);");
        returnString.append("var results = org_total-subtotal;");
        returnString.append("if(results < 0 ) { results=0; } form_obj.RemainingEffort.value=results; }");
        returnString.append("</script>");*/
        //COMMENTED FOR BUG : 356897 ENDS

        }
        return returnString.toString();
    }
    /* This method gets field value for attribute 'Remaining Effort'.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @return a String containing the HTML content to be displayed.
     * @throws Exception if the operation fails
     * @since PMC 10-6-SP2
     */
    public String getRemainingEffort(Context context, String[] args)
    throws Exception
    {
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String objectId = (String) paramMap.get("objectId");
        String reportFormat = (String) requestMap.get("reportFormat");
        StringBuffer returnString= new StringBuffer(); 
        double remainingEffort = 0;
        double totalEffort = 0;
        double plannedEffort = 0;
        String[] strEffortIDs = null;
        String strTotalEffort = null;
        Vector vecEffortId = new Vector();
        try {
        	DomainObject domObj = DomainObject.newInstance(context,objectId);
        	StringList objList = new StringList();
        	objList.add(DomainConstants.SELECT_CURRENT);
        	objList.add("to["+DomainConstants.RELATIONSHIP_HAS_EFFORTS+"].from.id");
        	objList.add(DomainConstants.SELECT_OWNER);
        	objList.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TOTAL_EFFORT);
        	Map map= domObj.getInfo(context,objList);
        	
        	String owner = (String)map.get(DomainConstants.SELECT_OWNER);
        	String ownerId= PersonUtil.getPersonObjectID(context, owner);
        	String strState = (String)map.get(DomainConstants.SELECT_CURRENT);
        	String taskId = (String)map.get("to["+DomainConstants.RELATIONSHIP_HAS_EFFORTS+"].from.id");       	
        	DomainObject taskDomObj = DomainObject.newInstance(context,taskId);      	
        	String rem_Effort = "0";        	
        		emxWeeklyTimeSheetBase_mxJPO  emxWeekTimeSheet = new emxWeeklyTimeSheetBase_mxJPO(context,args);
        		HashMap per_PersonEffort = emxWeekTimeSheet.getPersonEffortMapping(context,taskId,ownerId);
        		if(per_PersonEffort.size() > 0) {
        			Iterator keyItr = per_PersonEffort.keySet().iterator();
        			while (keyItr.hasNext()) {
        				String name = (String) keyItr.next();
        				vecEffortId = (Vector) per_PersonEffort.get(name);
        			}		            		         
        		}        		        
        		int effortVecSize = vecEffortId.size();
        		emxEffortManagementBase_mxJPO  emxEffortMgmt = new emxEffortManagementBase_mxJPO(context,args);
        		plannedEffort = emxEffortMgmt.getPlannedEffort(context,objectId); 
        		if(strState.equals(ProgramCentralConstants.STATE_EFFORT_EXISTS) && effortVecSize == 1) {	                		
        			rem_Effort = Double.toString(plannedEffort);
        		} 
        		else{
        			strEffortIDs = (String[]) vecEffortId.toArray(new String[0]);
        			MapList effortInfo = DomainObject.getInfo(context, strEffortIDs, objList);       			
        			for (Object info : effortInfo) {
						Map infoMap  = (Map)info;
						String current = (String)infoMap.get(DomainConstants.SELECT_CURRENT);
        				if(current.equalsIgnoreCase(ProgramCentralConstants.STATE_EFFORT_APPROVED)){
        					strTotalEffort = (String) infoMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TOTAL_EFFORT);
        					if(!ProgramCentralUtil.isNullString(strTotalEffort))
        						totalEffort = Task.parseToDouble(strTotalEffort) + totalEffort;
        				}						
					}
        			remainingEffort = plannedEffort - totalEffort;
        			rem_Effort = Double.toString(remainingEffort);
        		}			
        	if(reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat)){
        		returnString.append(XSSUtil.encodeForHTML(context,rem_Effort));
        	}
        	else
        	{
        		returnString.append(XSSUtil.encodeForHTML(context,rem_Effort));
        		returnString.append("<input type=\"hidden\" name=\"remain\" value=\""+XSSUtil.encodeForHTML(context,rem_Effort)+"\">");
        	}

        }catch(Exception e){
			throw new FrameworkException(e.getMessage());
		}
        return returnString.toString();
    }


    /* This method updates field value for attributes Sunday,Monday,Tuesday
     * Wednesday,Thursday,Friday,Saturday
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @throws Exception if the operation fails
     * @since PMC 10-6-SP2
     */

    public void updateDay(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String objectId = (String) paramMap.get("objectId");
        HashMap fieldMap = (HashMap)programMap.get("fieldMap");
        String name =   (String)fieldMap.get("name");
        String newDay = (String)paramMap.get("New Value");
        String symval ="";
        if(name.equals("Sun"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Sunday");
        }
        else if(name.equals("Mon"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Monday");
        }
        else if (name.equals("Tue"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Tuesday");
        }
        else if (name.equals("Wed"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Wednesday");
        }
        else if (name.equals("thu"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Thursday");
        }
        else if (name.equals("Fri"))
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Friday");
        }
        else
        {
            symval = PropertyUtil.getSchemaProperty(context,"attribute_Saturday");
        }
        DomainObject dom = DomainObject.newInstance(context, objectId);
        dom.open(context);
        dom.setAttributeValue(context,symval,newDay);
        dom.close(context);
    }

    /* This method updates field value for attribute 'Remaining Effort'.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @throws Exception if the operation fails
     * @since PMC 10-6-SP2
     */
    public void updateRemainingEffort(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String objectId = (String) paramMap.get("objectId");
        String newRemainingEffort = (String)paramMap.get("New Value");
        DomainObject dom = DomainObject.newInstance(context, objectId);
        dom.open(context);
        dom.setAttributeValue(context,PropertyUtil.getSchemaProperty(context,"attribute_RemainingEffort"),newRemainingEffort);
        dom.close(context);
    }

	/**
     * This method checks to see if Security Context Access Model is enabled based on a property.
     * This property is optional and should only be relevant if VPM Central is deployed.
     * @param context the eMatrix <code>Context</code> object
     * @param args - no args are expected.
     * @return boolean specifying whether Security Context Access model is enabled
     * @since PRG V6R2011
     */
    public boolean isSecurityContextAccessEnabled(Context context, String[] args) throws Exception {
    	//return com.matrixone.apps.program.ProjectSpace.isSCEnabled(context);
    	Map inputMap   = (Map) JPO.unpackArgs(args);
    	String objectId = (String) inputMap.get("objectId");
    	StringList selectable = new StringList();
    	selectable.add(DomainConstants.SELECT_ORGANIZATION);
    	selectable.add("project");
    	try
    	{        
    		DomainObject domainObj = DomainObject.newInstance(context,objectId);
    		Map mpObjectInfo = domainObj.getInfo(context,selectable);
   
    		String strToOrganization = (String)mpObjectInfo.get(DomainConstants.SELECT_ORGANIZATION);
    		String strToProject = (String)mpObjectInfo.get("project");
    		if(ProgramCentralUtil.isNullString(strToOrganization)|| ProgramCentralUtil.isNullString(strToProject)){
    			return false;
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return true;
    }

	/**
     * This method checks to see if Security Context Access Model is enabled based on a property.
     * This property is optional and should only be relevant if VPM Central is deployed.
     * @param context the eMatrix <code>Context</code> object
     * @param args - no args are expected.
     * @return boolean specifying whether Security Context Access model is enabled
     * @since PRG V6R2011
     */
    public boolean checkContextRole(Context context, String[] args)
      throws Exception
    {
        String role = args[0];
        try
        {
            String contextRole = context.getRole();
            if (contextRole != null && !"".equals(contextRole))
            {
            	String mqlQueryString =  "print role $1 select $2 dump $3";
            	String ancestors = MqlUtil.mqlCommand(context,mqlQueryString,contextRole,"role.ancestor","|");
                StringList ancestorList = FrameworkUtil.split(ancestors, "|");
                return (ancestorList.contains(role));
            }
        }
        catch (Exception e)
        {
        }
        return true;
    }
    
    /**
     * This method checks to see if Group Access Model is enabled based on a property.
     * Groups can be used as project members when access model is not based on security context.
     * @param context the eMatrix <code>Context</code> object
     * @param args - no args are expected.
     * @return boolean specifying whether Group Access model is enabled
     * @since PRG V6R2011
     */
    public boolean isGroupAccessEnabled(Context context, String[] args)
      throws Exception
    {
        try
        {
            String searchableGroups = EnoviaResourceBundle.getProperty(context, "eServiceProgramCentral.SearchableGroups");
            if (!"None".equalsIgnoreCase(searchableGroups))
            {
                return true;
            }
        }
        catch (Exception e)
        {
        }
        return false;
    }

    /**
     * This method checks to see if Role Access Model is enabled based on a property.
     * Roles can be used as project members when access model is not based on security context.
     * @param context the eMatrix <code>Context</code> object
     * @param args - no args are expected.
     * @return boolean specifying whether Role Access model is enabled
     * @since PRG V6R2011
     */
    public boolean isRoleAccessEnabled(Context context, String[] args)
      throws Exception
    {
        try
        {
            String searchableRoles = EnoviaResourceBundle.getProperty(context, "eServiceProgramCentral.SearchableRoles");
            if (!"None".equalsIgnoreCase(searchableRoles))
            {
                return true;
            }
        }
        catch (Exception e)
        {
        }
        return false;
    }
    
   /**
	 * This method updates Project Role attribute of Member relationship.
	 * 
	 * @param	context			
	 * 			context object which is used while fetching data related application.
     * 			
     * @param	args
     * 			Holds input arguments.
     * 
     * @throws 	Exception		
	 * 			Exception can be thrown in case of method fails to execute.
     */
    public void updateProjectRole(Context context, String[] args) throws Exception {
        HashMap programMap 	= (HashMap) JPO.unpackArgs(args);
        HashMap paramMap 	= (HashMap)programMap.get("paramMap");
        String connectionId = (String) paramMap.get("relId");
        String newProjectRole	= (String)paramMap.get("New Value");
        DomainRelationship domainRelationship 	= DomainRelationship.newInstance(context, connectionId);
        domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE,newProjectRole);
    }
    
    /**
     * This method updates Project Access attribute of Member relationship
     * 
     * @param context
     *        context object which is used while fetching data related application.
     *        
     * @param args
     *        Holds input argument.
     *        
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    public void updateProjectAccess(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String strConnectionId = (String) paramMap.get("relId");
        String newProjectAccess = (String)paramMap.get("New Value");
        DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
        domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PROJECT_ACCESS,newProjectAccess);
    }
    
    /**
     * This method returns the map containing ranges for project access.
     * 
     * @param context
     *        context object which is used while fetching data related application.
     *        
     * @param args
     *        Holds input argument.
     * @return map containing ranges for project access.
     * 
     * @throws MatrixException
     *         MatrixException can be thrown in case method fails to execute.
     */
    public Map getProjectAccessRange(Context context, String[] args) throws MatrixException {
    		
    	HashMap projectAccessMap = new HashMap();
    	StringList displayList = new StringList();
    	String language = context.getSession().getLanguage();
    	
		AttributeType projectAccessAttribute = new AttributeType(ProgramCentralConstants.ATTRIBUTE_PROJECT_ACCESS);
		projectAccessAttribute.open(context);
		StringList accessStringList = projectAccessAttribute.getChoices(context);
		projectAccessAttribute.close(context);
		accessStringList.remove(ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER);

		displayList	=i18nNow.getAttrRangeI18NStringList(ProgramCentralConstants.ATTRIBUTE_PROJECT_ACCESS,accessStringList,language);
		projectAccessMap.put("field_choices", accessStringList);
		projectAccessMap.put("field_display_choices", displayList);

		return  projectAccessMap;
    }
    
    /**
     * This method returns StringList of the edit access for Project Access column
     * puts vaulues true-If edit access; false - No edit access in the StringList
     * @param context
     *        context object which is used while fetching data related application.
     *        
     * @param args
     *        Holds input argument.
     *        
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    public StringList getProjectAccessColumnEditAccess(Context context,String args[]) throws Exception {
    	
		Map inputMap = (Map) JPO.unpackArgs(args);
		MapList objectMap = (MapList) inputMap.get("objectList");
		StringList returnStringList = new StringList(objectMap.size());
				
		for(int iterator = 0; iterator < objectMap.size(); iterator++)
		{
			
			Map map = (Map)objectMap.get(iterator);
			String strProjectAccess = (String)map.get(MemberRelationship.SELECT_PROJECT_ACCESS);
			String strMemberType = (String)map.get(ProgramCentralConstants.SELECT_TYPE);
						
			if(UIUtil.isNotNullAndNotEmpty(strProjectAccess) && strProjectAccess.equals(ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER))
			{
				returnStringList.add(false);
			}
			else if(UIUtil.isNotNullAndNotEmpty(strMemberType) && strMemberType.equals(ProgramCentralConstants.MEMBER_TYPE_ROLE) || strMemberType.equals(ProgramCentralConstants.MEMBER_TYPE_GROUP))
			{
				
				returnStringList.add(false);
			}
			else{
				returnStringList.add(true);
			}
		}
		return returnStringList;
	}
    
    /**
     * This method returns StringList of the edit access for Project Role column
     * puts vaulues true-If edit access; false - No edit access in the StringList
     * @param context
     *        context object which is used while fetching data related application.
     *        
     * @param args
     *        Holds input argument.
     *        
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    
    public StringList getProjectRoleColumnEditAccess(Context context,String args[]) throws Exception {
    	
		Map inputMap = (Map) JPO.unpackArgs(args);
		MapList objectMap = (MapList) inputMap.get("objectList");
				
		StringList returnStringList = new StringList(objectMap.size());
				
		for(int iterator = 0; iterator < objectMap.size(); iterator++)
		{
			Map map = (Map)objectMap.get(iterator);
			String strMemberType = (String)map.get(ProgramCentralConstants.SELECT_TYPE);
						
			if(UIUtil.isNotNullAndNotEmpty(strMemberType) && strMemberType.equals(ProgramCentralConstants.MEMBER_TYPE_ROLE) || strMemberType.equals(ProgramCentralConstants.MEMBER_TYPE_GROUP))
			{
				returnStringList.add(false);
			}
			else{
				returnStringList.add(true);
			}
					}
		return returnStringList;
	}
    
    /**
     * This method is called to check access for EnableEdit command
     * If user has edit access then only it will continue otherwise it will display the No edit access message 
     * @param context
     *        context object which is used while fetching data related application.
     *        
     * @param args
     *        Holds input argument.
     *        
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    @com.matrixone.apps.framework.ui.PreProcessCallable
    public HashMap preProcessCheckForEdit (Context context, String[] args) throws Exception
    {
    	ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PRG_LICENSE_ARRAY);
        HashMap inputMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) inputMap.get("paramMap");
        HashMap tableData = (HashMap) inputMap.get("tableData");
        MapList objectList = (MapList) tableData.get("ObjectList");
        String ObjectId = (String) paramMap.get("objectId");
        HashMap returnMap = null;
        
        com.matrixone.apps.program.ProjectSpace projectSpace =
            (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
        
		 projectSpace.setId(ObjectId);
		 
         String access = projectSpace.getAccess(context);
         
        if(ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER.equals(access) || ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD.equals(access)){
            returnMap = new HashMap(2);
            returnMap.put("Action","Continue");
            returnMap.put("ObjectList",objectList);
        } else {
            returnMap = new HashMap(3);
            returnMap.put("Action","Stop");
            returnMap.put("Message","emxProgramCentral.WBS.NoEdit");
            returnMap.put("ObjectList",objectList);
       }
        return returnMap;
	}

	/**
	 * Generates a grid of week columns in the members page of project.
	 * @param context the ENOVIA Context Object.
	 * @param args request parameters.
	 * @return A Map list of dynamic columns to be appended to the Members page
	 * @throws Exception if operation fails.
	 */
	public MapList getDynamicTimesheetColumns (Context context, String[] args) throws Exception
	{
		MapList mlColumns = new MapList();
		Map mapProgram = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map) mapProgram.get("requestMap");
		String projectId = (String) requestMap.get("objectId");
		Locale locale = (Locale) requestMap.get("localeObj");
		SimpleDateFormat formatter = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
		//Get project Actual dates
		ProjectSpace project = new ProjectSpace(projectId);
		StringList projectSelect = new StringList();
		projectSelect.add(Task.SELECT_TASK_ACTUAL_START_DATE);
		projectSelect.add(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
		Map projectInfo = project.getInfo(context, projectSelect);
		String strActualStartDate = (String) projectInfo.get(Task.SELECT_TASK_ACTUAL_START_DATE);
		String strActualFinishDate = (String) projectInfo.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
		
		String strCommandName = (String)requestMap.get("portalCmdName");
		if("PMCProjectMemberTimesheet".equals(strCommandName)){
		
		//If Project Actual Start is not present. Do not display timesheet grid.   
		if(ProgramCentralUtil.isNullString(strActualStartDate)){
			return mlColumns;
		}
		
		Map mapColumn = new HashMap();
		Map<String,String> mapSettings = new HashMap<String,String>();
		Map paramList = (HashMap) mapProgram.get("paramList");
		
		Calendar currentCal = Calendar.getInstance();
		int iWeekToday = currentCal.get(Calendar.WEEK_OF_YEAR);
		int currentYear = currentCal.get(Calendar.YEAR);
		StringList slTimeFrame = new StringList();
		
		Calendar fromCal = null;
		Calendar toCal = null;

		//Set From and To Dates of the Timesheet Grid
		Date actStartDate = formatter.parse(strActualStartDate);
		Date actFinishDate = null;
		fromCal = Calendar.getInstance();
		fromCal.setTime(actStartDate);
		
		//If Actual Finish Date is not null set it in toCal.
		if(ProgramCentralUtil.isNotNullString(strActualFinishDate)){
			actFinishDate = formatter.parse(strActualFinishDate);
			toCal = Calendar.getInstance();
			toCal.setTime(actFinishDate);
		}
		//If Actual Finish Date is null, set system current date in toCal.
		else{
			toCal= Calendar.getInstance();
		}

		Date fromDate = fromCal.getTime();
		Date toDate = toCal.getTime();
		ArrayList datelist = new ArrayList();
		com.matrixone.apps.program.fiscal.Calendar fiscalCalendar = Helper.getCalendar(CalendarType.ENGLISH);
		com.matrixone.apps.program.fiscal.Iterator itrFiscal = null;
		Interval[] range = null;
		itrFiscal = fiscalCalendar.getIterator(IntervalType.WEEKLY);
		fromDate = Helper.cleanTime(fromDate);
		toDate = Helper.cleanTime(toDate);
		range = itrFiscal.range(fromDate, toDate);

		//This For Loop will display the timesheet grid from 
		//actual start date to actual finish date (or current system date)
		for (Interval i:range) {
			Date startDate = i.getStartDate();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);
			int week = calendar.get(Calendar.WEEK_OF_YEAR);
			int year = calendar.get(Calendar.YEAR);
			slTimeFrame.add("" + year + "-" + week);
		}

		//This For Loop will display the timesheet grid from 
		//actual finsish date (or system current date) to actual start date. 
//		int size = range.length;
//		size--;
//		for (int index=size; index>=0; index--) {
//			Interval i = range[index];
//			Date startDate = i.getStartDate();
//			Calendar calendar = Calendar.getInstance();
//			calendar.setTime(startDate);
//			int week = calendar.get(Calendar.WEEK_OF_YEAR);
//			int year = calendar.get(Calendar.YEAR);
//			slTimeFrame.add("" + year + "-" + week);
//		}

		String strToday = "<img src='../common/images/iconActionDown.png' />";
		mapColumn = new HashMap();
		String strGroupHeader = ProgramCentralConstants.EMPTY_STRING;
		for (Iterator iterator = slTimeFrame.iterator(); iterator.hasNext();) {
			String strColumnLabel = (String)iterator.next();
			StringList weekDetails = FrameworkUtil.split(strColumnLabel, "-");
			strGroupHeader = (String)weekDetails.get(0);
			strColumnLabel = (String)weekDetails.get(1);
			String strColumnName = (String)weekDetails.get(0)+"-"+(String)weekDetails.get(1);
			NumberFormat nFormatter = NumberFormat.getNumberInstance(locale);
			strColumnLabel = nFormatter.format(Integer.parseInt(strColumnLabel));
			if(strColumnLabel.equalsIgnoreCase(""+iWeekToday) && strGroupHeader.equals(""+currentYear)){
				strColumnLabel = strColumnLabel + strToday;
			}else{
				strColumnLabel = strColumnLabel;
			}
			mapColumn = new HashMap();
			mapColumn.put("name", strColumnName);
			mapColumn.put("label", strColumnLabel);
			mapSettings = new HashMap();
			mapSettings.put("Registered Suite","ProgramCentral");
			mapSettings.put("program","emxProjectMember");
			mapSettings.put("Group Header", strGroupHeader);
			mapSettings.put("function","getTimesheetColumnData");
			mapSettings.put("Column Type","programHTMLOutput");
			mapSettings.put("Editable","false");
			mapSettings.put("Export","true");
			mapSettings.put("Field Type","Basic");
			mapSettings.put("Sortable","false");
			mapSettings.put("Width","30");
			mapColumn.put("settings", mapSettings);
			mlColumns.add(mapColumn);
		}
		}
		return mlColumns;
	}
	
	/**
	 * Returns the weekly timesheet status for each project member. 
	 * @param context the ENOVIA Context Object.
	 * @param args requested parameters
	 * @return A StringList of HTML formatted strings containing timesheet status. 
	 * @throws Exception if operation fails.
	 */
	public StringList getTimesheetColumnData(Context context, String[] args)  throws Exception 
	{
		StringList vecResult = new StringList();
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map columnMap = (Map) programMap.get("columnMap");
			Map paramMap = (Map) programMap.get("paramList");
			String projectId = (String)paramMap.get("objectId"); 
			String strWeekNumber = (String) columnMap.get(ProgramCentralConstants.SELECT_NAME);
			StringList columnInfo = FrameworkUtil.split(strWeekNumber, ProgramCentralConstants.HYPHEN);
			int year = Integer.parseInt((String)columnInfo.get(0));
			int week = Integer.parseInt((String)columnInfo.get(1));
			Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.WEEK_OF_YEAR, week);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			Date wkStartDate = calendar.getTime();
			calendar.add(Calendar.DATE, 6);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date wkEndDate = calendar.getTime();
			WeeklyTimesheet timesheet = new WeeklyTimesheet();
			String timesheetName = timesheet.getTimesheetNameByDate(context, wkEndDate);
			
			DateFormat formatter = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);

			String strWkEndDate = formatter.format(wkEndDate);
			MapList personInfoList = (MapList) programMap.get("objectList");
			Person person = null;
   			StringList objectSelect = new StringList();
   			StringList relSelect = new StringList();
			objectSelect.add(ProgramCentralConstants.SELECT_ID);
			objectSelect.add(ProgramCentralConstants.SELECT_NAME);
			objectSelect.add(ProgramCentralConstants.SELECT_TYPE);
			objectSelect.add(ProgramCentralConstants.SELECT_CURRENT);
			objectSelect.add(Task.SELECT_TASK_ACTUAL_START_DATE);
			objectSelect.add(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
			MapList objInfoList = new MapList();
   			String strWhere = "(" + Task.SELECT_TASK_ACTUAL_START_DATE + " != \"" + null + "\" && " +
				 	ProgramCentralConstants.SELECT_TASK_PROJECT_ID + " == \"" + projectId +"\"" + ") || ("+
				 	ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE + " == \"" + strWkEndDate + "\")";
			String relPattern = ProgramCentralConstants.RELATIONSHIP_WEEKLY_TIMESHEET + ProgramCentralConstants.COMMA
					+ ProgramCentralConstants.RELATIONSHIP_ASSIGNED_TASKS;
			String typeNames = ProgramCentralConstants.TYPE_WEEKLY_TIMESHEET + ProgramCentralConstants.COMMA
					+ ProgramCentralConstants.TYPE_TASK_MANAGEMENT;
			StringList taskTypeList = ProgramCentralUtil.getSubTypesList(context, ProgramCentralConstants.TYPE_TASK_MANAGEMENT);
			Calendar currentCal = Calendar.getInstance();
			Date today = currentCal.getTime();

			//Iterate project members
			for (Iterator itrTableRows = personInfoList.iterator(); itrTableRows.hasNext();){
				Map personInfo = (Map) itrTableRows.next();
				String strPersonId = (String)personInfo.get(ProgramCentralConstants.SELECT_ID);
				strPersonId = strPersonId.replace("personid_", "");
				person = new Person(strPersonId);
				
				//Get all timesheet by the person.
				objInfoList = person.getRelatedObjects(context, relPattern, typeNames, objectSelect, relSelect,
						false, true, (short)1, strWhere, "", 0, null, null, null);

				boolean tobeNotified = false; 
				boolean isTimesheetPresent = false; 
				
				//Get all assigned tasks and timesheet of the person
				String strActualStartDate = ProgramCentralConstants.EMPTY_STRING;
				String strActualFinishDate = ProgramCentralConstants.EMPTY_STRING;
				Date actualStartDate = null;
				Date actualFinishDate = null;
				int actualStartDateWeek;
				int actualFinishDateWeek;
				String strType = ProgramCentralConstants.EMPTY_STRING;
				String timesheetState = ProgramCentralConstants.EMPTY_STRING;
	
				for (Iterator itrObjInfo = objInfoList.iterator(); itrObjInfo.hasNext();) {
					strActualStartDate = ProgramCentralConstants.EMPTY_STRING;
					strActualFinishDate = ProgramCentralConstants.EMPTY_STRING;
					actualStartDate = null;
					actualFinishDate = null;
					actualStartDateWeek = -1;
					actualFinishDateWeek = -1;
					Map objInfo = (Map) itrObjInfo.next();
					strType = (String) objInfo.get(ProgramCentralConstants.SELECT_TYPE);
					if(taskTypeList.contains(strType) && !tobeNotified){
						strActualStartDate = (String) objInfo.get(Task.SELECT_TASK_ACTUAL_START_DATE);
						strActualFinishDate = (String)objInfo.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
						
						if(ProgramCentralUtil.isNotNullString(strActualStartDate)){
							actualStartDate = formatter.parse(strActualStartDate);
							calendar.setTime(actualStartDate);
							actualStartDateWeek = calendar.get(Calendar.WEEK_OF_YEAR);
						}
						
						if(ProgramCentralUtil.isNotNullString(strActualFinishDate)){
							actualFinishDate = formatter.parse(strActualFinishDate);
							calendar.setTime(actualFinishDate);
							actualFinishDateWeek = calendar.get(Calendar.WEEK_OF_YEAR);
						}else {
							tobeNotified = true;
						}
						
						//If column week falls between actual start and actual finish inclusive, tobeNotified=true.
						if(week >= actualStartDateWeek &&  week <= actualFinishDateWeek){
							tobeNotified = true;
							//Add Task Ids which will be seen in email notification
						}
					}else if(ProgramCentralConstants.TYPE_WEEKLY_TIMESHEET.equals(strType)){
						timesheetState = (String) objInfo.get(ProgramCentralConstants.SELECT_CURRENT);
					}
				}
				
				if(tobeNotified){
					vecResult.add(getTimesheetIconLink(context, strPersonId, timesheetName, timesheetState));
				}else{
					vecResult.add(ProgramCentralConstants.EMPTY_STRING);
				}
			}
			return vecResult;
		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}

	private String getTimesheetIconLink(Context context, String memberId, String timesheetName, 
			String timesheetState) throws MatrixException{
		String tooltip = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.WeeklyTimesheet.NotifyMember", 
				context.getSession().getLanguage());
		if(ProgramCentralUtil.isNullString(timesheetState) || 
				ProgramCentralConstants.STATE_EFFORT_EXISTS.equalsIgnoreCase(timesheetState) ||
				ProgramCentralConstants.STATE_EFFORT_REJECTED.equalsIgnoreCase(timesheetState) ){
			return "<a title='"+ tooltip +"' target=\"listHidden\" href='../programcentral/emxProgramCentralUtil.jsp?mode=timesheetReminder&amp;memberId=" + memberId + "&amp;timesheetName=" + timesheetName + "'><img style=\"border:0;\" src=\"../common/images/iconSmallMail.gif\" alt=\"Send reminder\"/></a>";
		}else {
			return ProgramCentralConstants.EMPTY_STRING;
		}
    }

    /**
     * Checks if context user has access on the timesheet status grid. 
     * @param context the ENOVIA Context object.
     * @param args request parameters
     * @return true if context user has access on the timesheet stautus grid.
     * @throws MatrixException if operation fails.
     */
    public boolean hasAccessToTimesheetStatusGrid(Context context, String[] args)
    throws MatrixException{
        try{
            Map programMap = (HashMap) JPO.unpackArgs(args);
            String projectId = (String) programMap.get("objectId");
            DomainObject project = DomainObject.newInstance(context, projectId);
            
            //The object type must be Project Space to see the grid.
            if(!project.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE)){
            	return false;
            }else{
            	return project.checkAccess(context, (short) AccessConstants.cModify);
            }                        
        }catch(Exception e){
            throw new MatrixException(e);
        }
    }
    
    /**
     * This method renders PMCProjectMemberSummary table columns dynamically
     * @param context
     *        context object which is used while fetching data related application.
     *        
     * @param args
     *        Holds input argument.
     *        
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    
    
    public MapList getMembersDynamicColumns(Context context, String[] args) throws Exception{

    	MapList returnList = new MapList();
    	final String SOV_TABLE = "DomainAccess";
    	String[] columsToBeIncluded = {
    			"Comments", 
    			"Project", 
    			"Organization",
    			"Access"
    	};
    	List slColumnsToBeIncluded = Arrays.asList(columsToBeIncluded);
    	MapList mlUpdatedColumns = new MapList();
    	try{
    		StringList assignments = new StringList();
    		assignments.add("all");
    		UITable table = new UITable();
    		MapList mlColumns = UITable.getColumns(context, SOV_TABLE, assignments);

    		Map column = new HashMap();

    		for (Iterator itrDomainAccessCols = mlColumns.iterator(); itrDomainAccessCols.hasNext();) {

    			Map mCol = (Map) itrDomainAccessCols.next();
    			String sColName = (String)mCol.get("name");
    			if(!slColumnsToBeIncluded.contains(sColName)){
    				continue;
    			}
    			if("Project".equals(sColName)){
    				((Map)(mCol.get("settings"))).remove("Registered Suite");
    				((Map)(mCol.get("settings"))).put("Registered Suite","ProgramCentral");
    				
    				mCol.put("label", "emxFramework.Command.Member");
    			}

    			if("Access".equals(sColName)){
                    ((Map)(mCol.get("settings"))).remove("Edit Access Program");
                    ((Map)(mCol.get("settings"))).remove("Edit Access Function");

                    ((Map)(mCol.get("settings"))).put("Edit Access Program", "emxProjectMember");
                    ((Map)(mCol.get("settings"))).put("Edit Access Function", "getCellLevelEditAccess");

    				((Map)(mCol.get("settings"))).put("Mass Update", "false");
    				((Map)(mCol.get("settings"))).put("Update Program", "emxProjectMember");
    			}
    			column.put(sColName,mCol);
    			mlUpdatedColumns.add(column);
    		}

    		Map mapColumn = new HashMap();
    		mapColumn.put("name", "ProjectRole");
    		mapColumn.put("label", "emxProgramCentral.Common.ProjectRole");
    		mapColumn.put("expression_relationship", "attribute[Project Role].value");
    		Map mapSettings = new HashMap();

    		mapSettings.put("RMB Menu","false");
    		mapSettings.put("Editable","true");
    		mapSettings.put("Range Program","emxTask"); 
    		mapSettings.put("Range Function","getProjectRoleRange"); 
    		mapSettings.put("Edit Access Function","getProjectMemberRoleColumnEditAccess"); 
    		mapSettings.put("Registered Suite","ProgramCentral");
    		mapSettings.put("Update Function","updateProjectMemberRole"); 
    		mapSettings.put("Edit Access Program","emxProjectMember"); 
    		mapSettings.put("Admin Type", PropertyUtil.getAliasForAdmin(context, "attribute", "Project Role", true));
    		mapSettings.put("Input Type","combobox");
    		mapSettings.put("Update Program","emxProjectMember");
    		mapSettings.put("Column Type","program");
    		mapSettings.put("program","emxProjectMember");
    		mapSettings.put("function","getProjectRoleColumn");

    		mapColumn.put("settings", mapSettings);
    		mlUpdatedColumns.add(mapColumn);
    		column.put("ProjectRole",mapColumn);

    		returnList.add(0,(Map)column.get("Project"));
    		returnList.add(1,(Map)column.get("Organization"));
    		returnList.add(2,(Map)column.get("Access"));
    		returnList.add(3,(Map)column.get("ProjectRole"));
    		returnList.add(4,(Map)column.get("Comments"));

    	} catch(Exception e) {
    		throw new MatrixException(e);        
    	}
    	return returnList;
    }
    /**
     * This method returns true if user have DPM license and modify access on project.
     * @param context - The eMatrix <code>Context</code> object. 
     * @param args - Hold information about objects.
     * @return returns true if user have DPM license and modify access on project.
     * @throws Exception if operation fails.
     */
    @ProgramCallable
    public boolean hasAccessToModifyProject(Context context,String[]args)throws Exception
    {
    	boolean access = true;
    	Map programMap 		= (HashMap) JPO.unpackArgs(args);
    	String projectId 	= (String)programMap.get("objectId");
    	try {
    		ComponentsUtil.checkLicenseReserved(context, ProgramCentralConstants.PRG_LICENSE_ARRAY);
    	}
    	catch(MatrixException ex) {
    		access = false;
    	}

    	if (access) {
    		DomainObject project 	= DomainObject.newInstance(context, projectId);
    		access = project.checkAccess(context, (short) AccessConstants.cModify);
    	}

    	return access;
    }

    /**
     * This method is edit access function for Access column of PMCProjectMemberSummary
     * @param context
     *        context object which is used while fetching data related application.
     *
     * @param args
     *        Holds input argument.
     *
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    @SuppressWarnings("unchecked")
    public static StringList getCellLevelEditAccess(Context context, String args[])throws Exception
    {
        HashMap<?, ?> inputMap = (HashMap<?, ?>)JPO.unpackArgs(args);
        MapList objectMap = (MapList) inputMap.get("objectList");
        StringList returnStringList = new StringList(objectMap.size());
        Iterator<?> objectItr = objectMap.iterator();
        String owner = DomainConstants.EMPTY_STRING;
        String objectProject = DomainConstants.EMPTY_STRING;
        String objectOrg = DomainConstants.EMPTY_STRING;
        String chnageOwnerAccess = DomainConstants.EMPTY_STRING;
        
        String loggedInUser = context.getUser();
        
        String license = UINavigatorUtil.isCloud(context)?
  			   "onlineinstance.product.derivative.derivative":"product.derivative.derivative";
  	   String printPersonLicenseMQL = "print person $1 select $2 dump $3";
        
        while (objectItr.hasNext()) {
            Map<?,?> curObjectMap = (Map<?,?>) objectItr.next();
            String curObjectID = (String) curObjectMap.get("id");
            String disableSelection = (String)curObjectMap.get("disableSelection");
            StringList valueList = StringUtil.split(curObjectID, ":");
            if(valueList.size() > 1 ) {
                DomainObject obj = new DomainObject((String)valueList.get(0));
                StringList selects = new StringList(3);
                selects.addElement(DomainConstants.SELECT_OWNER);
                selects.addElement("current.access[changeowner]");
                selects.addElement("organization");
                selects.addElement("project");
                Map objMap = obj.getInfo(context, selects);
                if("".equals(owner)) {
                    owner = (String)objMap.get(DomainConstants.SELECT_OWNER);
                    chnageOwnerAccess = (String)objMap.get("current.access[changeowner]");
            }
                objectOrg = (String)objMap.get("organization");
                objectProject = (String)objMap.get("project");
            }

            if("false".equalsIgnoreCase(chnageOwnerAccess)) {
                returnStringList.add(Boolean.valueOf(false));
            } else if( valueList.size() >= 4 ) {
                String project = (String)valueList.get(2);
                if( project.contains("_PRJ")) {
                	int index = project.lastIndexOf("_");
                	String personName = project.substring(0, index);
                	StringList assignProductList = new StringList();
                	try{
                		String productNameList = MqlUtil.mqlCommand(context, printPersonLicenseMQL, personName, license, "|");
                		assignProductList = FrameworkUtil.split(productNameList, "|");
                	}catch(Exception e){
                		e.printStackTrace();
                	}
                	
                    if( loggedInUser.equals(personName) || personName.equals(owner) || "true".equalsIgnoreCase(disableSelection)) {
                        returnStringList.add(Boolean.valueOf(false));
                    } else if(assignProductList.contains(ProgramCentralConstants.PRG_LICENSE_ARRAY[0])){
                        returnStringList.add(Boolean.valueOf(true));
                    }else{
                    	returnStringList.add(Boolean.valueOf(false));
                    }
                } else if(!DomainConstants.EMPTY_STRING.equals(curObjectMap.get("org")) || !DomainConstants.EMPTY_STRING.equals(curObjectMap.get("project"))) {
    
                    boolean org = ((String) curObjectMap.get("org")).equals(objectOrg);
                    boolean proj = ((String) curObjectMap.get("project")).equals(objectProject);
                    if(org && proj) {
                        returnStringList.add(Boolean.valueOf(false));
                    } else {
                        returnStringList.add(Boolean.valueOf(true));
                    }
                } else {
                    returnStringList.add(Boolean.valueOf(true));
                }
            } else {
                returnStringList.add(Boolean.valueOf(true));
            }
        }
        return returnStringList;
    }
        

    /**To update the access value for Add Ownership
     * @param context
     * @param args
     * @throws Exception
     */
	public void updateAccessValue(Context context, String[] args) throws Exception 
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		StringList valueList = StringUtil.split(objectId, ":");
		
		String projectSpaceId = ProgramCentralConstants.EMPTY_STRING;
		String memberName = ProgramCentralConstants.EMPTY_STRING;
		String connectionId = ProgramCentralConstants.EMPTY_STRING;
		String projectRole = ProgramCentralConstants.EMPTY_STRING;
		
		if (valueList.size() >= 4 && ((String) valueList.get(2)).contains("_PRJ")) {
			projectSpaceId = (String) valueList.get(0);
			memberName = (String) valueList.get(2);
			memberName = memberName.substring(0, memberName.indexOf("_PRJ"));

			/*DomainObject project = DomainObject.newInstance(context, projectSpaceId);
			connectionId = getConnectionId(context, project, memberName);

			DomainRelationship domainRelationship = DomainRelationship.newInstance(context, connectionId);
			projectRole = domainRelationship.getAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE);*/
			
			JPO.invoke(context, "emxDomainAccess", null, "updateAccessValue",args, MapList.class);

			//NOTE: This "if" block seems redundant, but actually it is not.With above call emxDomainAccess:updateAccessValue, 
			//when new ownership is created by kernel, they delete the existing relationship between project->Member 
			//and recreate the same relationship with new access bit. Hence the "project role" attribute value in 
			//relationship before edit operation get lost which is restored in below if block by the help of new relId.
			/*if(ProgramCentralUtil.isNotNullString(projectRole)){
				connectionId = getConnectionId(context, project, memberName);
				domainRelationship = DomainRelationship.newInstance(context,connectionId);
				domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE, projectRole);
			}*/
		}else{
			JPO.invoke(context, "emxDomainAccess", null, "updateAccessValue",args, MapList.class);
		}
	}
    
	/**
	 * Returns the relID between the project and a specific member.
	 * @param context
	 * @param project
	 * @param memberName
	 * @return
	 * @throws FrameworkException
	 */
	private String getConnectionId(Context context, DomainObject project , String memberName) throws FrameworkException
	{
		StringList busSelect = new StringList();

		StringList relSelect = new StringList(ProgramCentralConstants.SELECT_ID);
		relSelect.addElement("to.name");

		StringBuilder whereClause = new StringBuilder();
		whereClause.append("to.name ==\"").append(memberName).append("\"");

		MapList projectInfoList = project.getRelatedObjects(context,
				ProgramCentralConstants.RELATIONSHIP_MEMBER,
				ProgramCentralConstants.TYPE_PERSON,
				false,
				true,
				1,
				busSelect,
				relSelect,
				"",
				whereClause.toString(),
				0,
				null,
				null,
				null);

		Map projectInfoMap = (Map)projectInfoList.get(0);
		String connectionId = (String)projectInfoMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);

		return connectionId;
	}
    
    
    /**
     * This method is used to render the project role column of PMCProjectMemberSummary table
     * @param context
     *        context object which is used while fetching data related application.
     *        
     * @param args
     *        Holds input argument.
     *        
     * @throws Exception
     *         Exception can be thrown in case of method fails to execute.
     */
    public Vector getProjectRoleColumn (Context context, String[] args) throws Exception{
    	Map projectRoleMap = new HashMap();
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        HashMap paramList    = (HashMap) programMap.get("paramList");
        String projectId = (String)paramList.get("parentOID");
        Vector columnValues = new Vector(objectList.size());

        DomainObject project = DomainObject.newInstance(context, projectId);
        StringList busSelect = new StringList();
        StringList relSelect = new StringList("attribute[Project Role]");
        relSelect.addElement("to.name");
        
        MapList projectInfoList = project.getRelatedObjects(context,
                ProgramCentralConstants.RELATIONSHIP_MEMBER,
                ProgramCentralConstants.TYPE_PERSON,
                false,
                true,
                1,
                busSelect,
                relSelect,
                "",
                null,
                0,
                null,
                null,
                null);
        
        Iterator projectInfoListIterator = projectInfoList.iterator();
        
        while(projectInfoListIterator.hasNext()){
        	Map<String,String> projectInfoMap = (Map)projectInfoListIterator.next();
        	projectRoleMap.put(projectInfoMap.get("to.name"),projectInfoMap.get("attribute[Project Role]"));
        }
        
        Iterator objectListIterator = objectList.iterator();
        
        while(objectListIterator.hasNext()){
        	Map<String,String> objectMap = (Map)objectListIterator.next();
        	String name = (String)objectMap.get("name");
        	if(name.contains("_PRJ")){
        		name = name.substring(0, name.indexOf("_PRJ"));
        		columnValues.addElement(projectRoleMap.get(name));
        	}else {
        		columnValues.addElement("");
        	}
       
        }
        
        
        return columnValues;
    }
    
    
    /**
 	 * This method updates Project Role attribute of Member relationship.
 	 * 
 	 * @param	context			
 	 * 			context object which is used while fetching data related application.
      * 			
      * @param	args
      * 			Holds input arguments.
      * 
      * @throws 	Exception		
 	 * 			Exception can be thrown in case of method fails to execute.
      */
     public void updateProjectMemberRole(Context context, String[] args) throws Exception {
         HashMap programMap 	= (HashMap) JPO.unpackArgs(args);
         HashMap paramMap 	= (HashMap)programMap.get("paramMap");
         String objectId  = (String)paramMap.get("objectId");
         StringList valueList = StringUtil.split(objectId, ":");
         String projectSpaceId = ProgramCentralConstants.EMPTY_STRING;
         String memberName = ProgramCentralConstants.EMPTY_STRING;
         String connectionId = ProgramCentralConstants.EMPTY_STRING;
         if(valueList.size() >=4 && ((String)valueList.get(2)).contains("_PRJ")){
         	projectSpaceId = (String)valueList.get(0);
         	memberName = (String)valueList.get(2);
         	memberName = memberName.substring(0, memberName.indexOf("_PRJ"));
         	DomainObject project = DomainObject.newInstance(context, projectSpaceId);
         	StringList busSelect = new StringList(ProgramCentralConstants.SELECT_NAME);
         	busSelect.add("from["+ProgramCentralConstants.RELATIONSHIP_MEMBER+"].to.name");
         	
         	StringList relSelect = new StringList(ProgramCentralConstants.SELECT_ID);
         	relSelect.addElement("to.name");
         	//String whereClause = "from[Member].to.name =="+memberName;
         	
         	MapList projectInfoList = project.getRelatedObjects(context,
         			                     ProgramCentralConstants.RELATIONSHIP_MEMBER,
         			                     ProgramCentralConstants.TYPE_PERSON,
         			                     false,
         			                     true,
         			                     1,
         			                     busSelect,
         			                     relSelect,
         			                     "",
         			                     null,
         			                     0,
         			                     null,
         			                     null,
         			                     null);


         	
         	 Iterator projectInfoListIterator = projectInfoList.iterator();
         	 
         	 while(projectInfoListIterator.hasNext()){
         		 Map projectInfoMap = (Map)projectInfoListIterator.next();
         		 if(memberName.equals((String)projectInfoMap.get("to.name"))){
         			 connectionId = (String)projectInfoMap.get("id[connection]");
         			 break;
         		 }
         		 
         	 }
         	
              String newProjectRole	= (String)paramMap.get("New Value");
              DomainRelationship domainRelationship 	= DomainRelationship.newInstance(context, connectionId);
              domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE,newProjectRole);
         }
         
        
     }
     
     
     /**
      * This method returns StringList of the edit access for Project Role column
      * puts vaulues true-If edit access; false - No edit access in the StringList
      * @param context
      *        context object which is used while fetching data related application.
      *        
      * @param args
      *        Holds input argument.
      *        
      * @throws Exception
      *         Exception can be thrown in case of method fails to execute.
      */
     
     public StringList getProjectMemberRoleColumnEditAccess(Context context,String args[]) throws Exception {
     	
 		Map inputMap = (Map) JPO.unpackArgs(args);
 		MapList objectMap = (MapList) inputMap.get("objectList");
 				
 		StringList returnStringList = new StringList(objectMap.size());
 				
 		for(int iterator = 0; iterator < objectMap.size(); iterator++)
 		{
 			Map map = (Map)objectMap.get(iterator);
 			String strMemberType = (String)map.get(ProgramCentralConstants.SELECT_TYPE);
 			boolean editAccess = ((String)map.get("name")).contains("_PRJ");
 			
 			returnStringList.add(editAccess);		
 		}
 		return returnStringList;
 	}
     
     /**
      * This method returns MapList of the project members.
      * 
      * @param context
      *        context object which is used while fetching data related application.
      *        
      * @param args
      *        Holds input argument.
      *        
      * @throws Exception
      *         Exception can be thrown in case of method fails to execute.
      */
     
 	@com.matrixone.apps.framework.ui.ProgramCallable
 	static public MapList getObjectAccessList(Context context, String[] args) throws Exception {
 		Map programMap = (Map) JPO.unpackArgs(args);
 		MapList results  = (MapList) JPO.invoke(context,
 				"emxDomainAccess", 
 				null,
 				"getObjectAccessList", 
 				args,
 				MapList.class);

 			Iterator resultsIterator = results.iterator();
 			while(resultsIterator.hasNext()){
 				Map<String,String> mapObjects = (Map) resultsIterator.next();
 				String org = mapObjects.get("org");
			String project = mapObjects.get("project");

			//Disable selection for Company SOV.
			if(UIUtil.isNotNullAndNotEmpty(org) && UIUtil.isNullOrEmpty(project)){
 					mapObjects.put("disableSelection","true");
 				} 
 			}
 			return results;
 		}

 	}

