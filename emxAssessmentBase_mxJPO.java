/* emxAssessmentBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.15.2.2 Thu Dec  4 07:55:22 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.15.2.1 Thu Dec  4 01:53:34 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.15 Wed Oct 22 15:49:18 2008 przemek Experimental przemek $
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.MemberRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.Assessment;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;

/**
 * The <code>emxAssessmentBase</code> class represents the Risk JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.3 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxAssessmentBase_mxJPO extends com.matrixone.apps.program.Assessment
{
    /** The project access list id relative to this object. */
    static protected final String SELECT_PROJECT_ACCESS_LIST_ID =
        "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";

    /** Id of the Access List Object for this Assessment. */
    protected DomainObject _accessListObject = null;

    /**
     * Constructs a new emxAssessment JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    public emxAssessmentBase_mxJPO (Context context, String[] args) throws Exception
    {
        // Call the super constructor
        super();
        if ((args != null) && (args.length > 0))
        {
            setId(args[0]);
        }
    }

    /**
     * Get the access list object for this Assessment.
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @return DomainObject of Access list Object
     * @since AEF 9.5.1.3
     */
    protected DomainObject getAccessListObject(Context context)
    throws Exception
    {
        if (_accessListObject == null)
        {
            //System.out.println("Retrieving Assessment PAL ID..." +
            //        (new Date().getTime()));
            String accessListID =
                getInfo(context, SELECT_PROJECT_ACCESS_LIST_ID);
            if ((accessListID != null) && !"".equals(accessListID))
            {
                _accessListObject = DomainObject.newInstance(context, accessListID);
            }
        }
        return _accessListObject;
    }

    /**
     * This function verifies the user's permission for the given assessment.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains the following entries:
        [PROJECT_MEMBER|PROJECT_ASSESSOR|PROJECT_LEAD|PROJECT_OWNER|PROJECT_USER]
     *   PROJECT_MEMBER to see if the context user is a project member, <BR>
     *   PROJECT_ASSESSOR to see if the context user is a project assessor, <BR>
     *   PROJECT_LEAD to see if the context user is a project lead, <BR>
     *   PROJECT_OWNER to see if the context user is a project owner.
     *   PROJECT_USER to see if the context user is a project user.
     * @return boolean true if access check passed else returns false
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    public boolean hasAccess(Context context, String[] args)
    throws Exception
    {
        //System.out.println("Start Assessment Access: " +new Date().getTime());
        //program[emxAssessment PROJECT_MEMBER -method hasAccess
        //            -construct ${OBJECTID}] == true
        boolean access = false;
        DomainObject accessListObject = getAccessListObject(context);

        if (accessListObject != null)
        {
            for (int i = 0; i < args.length; i++)
            {
                String accessType = args[i];
                int iAccess;
                if ("PROJECT_MEMBER".equals(accessType) ||
                        "PROJECT_USER".equals(accessType))
                {
                    iAccess = AccessConstants.cExecute;
                }
                else if ("PROJECT_LEAD".equals(accessType))
                {
                    iAccess = AccessConstants.cModify;
                }
                else if ("PROJECT_ASSESSOR".equals(accessType))
                {
                    iAccess = AccessConstants.cViewForm;
                }
                else if ("PROJECT_OWNER".equals(accessType))
                {
                    iAccess = AccessConstants.cOverride;
                }
                else
                {
                    continue;
                }
                if (accessListObject.checkAccess(context, (short) iAccess))
                {
                    access = true;
                    break;
                }
            }
        }

        //System.out.println("End Assessment Access: " + new Date().getTime() +
        //                   " :: " + access);
        return access;
    }

    /**
	 * Checks if the context user has access to Assessment operations.
	 * @param context the ENOVIA Context object 
	 * @param args args holds project Id
	 * @return true if user has access to Assessment operation.
	 * @throws Exception if operation fails.
     */
    public boolean hasAccessToCommand(Context context, String[] args)
	throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");

		if (ProgramCentralUtil.isNotNullString(objectId)){
			DomainObject project = DomainObject.newInstance(context, objectId);
    		String projectState = project.getInfo(context, DomainConstants.SELECT_CURRENT);
    		if("Cancel".equalsIgnoreCase(projectState) ||"Complete".equalsIgnoreCase(projectState) || "Archive".equalsIgnoreCase(projectState)) {
    			return false;
    		}
			return project.checkAccess(context, (short)AccessConstants.cModify);
        }
		return false;

//		Old Security Impl
//		HashMap programMap = (HashMap) JPO.unpackArgs(args);
//		String objectId = (String) programMap.get("objectId");
//
//		// if object id is passed, then we look at object permissions; otherwise
//		// this screen is being accessed from my risks, which means you can update.
//		boolean editFlag = false;
//		com.matrixone.apps.program.ProjectSpace projectSpace =
//			(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
//					DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
//		try
//		{
//			// Checking the role and access for displaying Create Link...
//			if ((objectId != null) && !objectId.equals(""))
//			{
//				projectSpace.setId(objectId);
//				String access = projectSpace.getAccess(context);
//				if (access.equals(ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD) ||
//						access.equals(ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER) ||
//						access.equals(ProgramCentralConstants.PROJECT_ACCESS_PROJECT_ASSESSOR))
//				{
//					editFlag = true;
//				}
//			}
//		}
//		catch (Exception ex)
//		{
//			throw ex;
//		}
//		finally
//		{
//			return editFlag;
//		}
    }

    /**
     * This method is used to get the list of projet assessment objects.
     * Used for PMCAssessmentSummary table
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectId   - String containing the object id
     * @return MapList containing the id of Assessment objects of the logged in user
     * @throws Exception if the operation fails
     * @since PMC 10.0.0.0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAssessment(Context context, String[] args)
    throws Exception
    {
        // Check license while listing Assessment, if license check fails here
        // the assessment will not be listed.
        //
        ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_PRG_LICENSE_ARRAY);

        MapList assessmentList = null;
        Map mpAssessment;
        String strAssessmentOwner = DomainConstants.EMPTY_STRING;
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            com.matrixone.apps.program.ProjectSpace projectSpace =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
            Map assessmentObjMap = null;

            //Get assessments for project
            StringList busSelects = new StringList(1);

            // assessment selectables
            busSelects.add(Assessment.SELECT_ID);
            busSelects.add(Assessment.SELECT_OWNER);

            projectSpace.setId(objectId);
            assessmentList =
                Assessment.getAssessments(context, projectSpace, busSelects,
                        null, null, null);

            for (int i=0;i<assessmentList.size();i++) {
                mpAssessment = (Map)assessmentList.get(i);
                strAssessmentOwner = (String)mpAssessment.get(SELECT_OWNER);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return assessmentList;
        }
    }

    /**
     * This method is used to get the Role of the project member's list.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     *    paramList  - Map containing parameters for cloning the object
     *    objectId   - String containing the projectID
     * @return Vector containing the role of the project member list value as String.
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
    public Vector getRole(Context context, String[] args)
    throws Exception
    {
        Vector showRole = new Vector();
        try
        {
            String objectId = DomainConstants.EMPTY_STRING;
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
            String launched = (String) paramList.get("launched");
            if(launched != null && launched.equalsIgnoreCase("true"))
            {
                objectId = (String) paramList.get("objectId");
            }
            else
            {
                objectId = (String) paramList.get("parentOID");
            }


            Map objectMap = null;
            com.matrixone.apps.program.ProjectSpace projectSpace =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            int arrayCount = 0;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[arrayCount] =
                    (String) objectMap.get(Assessment.SELECT_ID);
                arrayCount++;
            }

            // Getting the list of members of the project
            StringList busSelects = new StringList(1);
            busSelects.add(Person.SELECT_NAME);
            StringList memberSelects = new StringList(1);
            memberSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
            projectSpace.setId(objectId);

            MapList membersList =
                projectSpace.getMembers(context, busSelects, memberSelects,
                        null, null);
            Iterator membersListItr = null;

            MapList actionList =
                DomainObject.getInfo(context, objIdArr,
                        new StringList(Assessment.SELECT_OWNER));

            int actionListSize = 0;
            if (actionList != null)
            {
                actionListSize = actionList.size();
            }

            for (int i = 0; i < actionListSize; i++)
            {
                String assessRole = "&nbsp";
                String assessorRole = "";
                objectMap = (Map) actionList.get(i);
                String assessorId =
                    (String) objectMap.get(Assessment.SELECT_OWNER);

                if (!assessorId.equals(""))
                {
                    membersListItr = membersList.iterator();
                    while (membersListItr.hasNext())
                    {
                        Map Currentmember = (Map) membersListItr.next();
                        if (((String) Currentmember.get(Person.SELECT_NAME)).equals(
                                assessorId))
                        {
                            assessorRole =
                                (String) Currentmember.get(MemberRelationship.SELECT_PROJECT_ROLE);
                            break;
                        }
                    }
                    // end of while(membersListItr.hasNext())
                        assessRole =
                            i18nNow.getRoleI18NString(assessorRole,
                                    context.getSession().getLanguage());
                }
                showRole.add(assessRole);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return showRole;
        }
    }

    /**
     * This method is used to show the status icon image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names.
     * @return Vector containing the status icon value as String.
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
    public Vector getStatusIcon(Context context, String[] args)
    throws Exception
    {
        Vector showIcon = new Vector();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            int arrayCount = 0;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[arrayCount] =
                    (String) objectMap.get(Assessment.SELECT_ID);
                arrayCount++;
            }

            MapList actionList =
                DomainObject.getInfo(context, objIdArr,
                        new StringList(Assessment.SELECT_ASSESSMENT_STATUS));

            int actionListSize = 0;
            if (actionList != null)
            {
                actionListSize = actionList.size();
            }

            String red = "Red";
            String green = "Green";
            String yellow = "Yellow";
            String other = "---";

            for (int i = 0; i < actionListSize; i++)
            {
                String statusGif = "&nbsp;";
                objectMap = (Map) actionList.get(i);
                String assessmentStatus =
                    (String) objectMap.get(Assessment.SELECT_ASSESSMENT_STATUS);

                if (red.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"/>";
                }
                else if (yellow.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"/>";
                }
                else if (green.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"/>";
                }
                else if (other.equals(assessmentStatus))
                {
                    statusGif = "---";
                }

                showIcon.add(statusGif);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return showIcon;
        }
    }

    /**
     * This method is used to show the resource status icon image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the status icon value as String.
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
    public Vector getResourceStatus(Context context, String[] args)
    throws Exception
    {
        Vector showResourceStatus = new Vector();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            int arrayCount = 0;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[arrayCount] =
                    (String) objectMap.get(Assessment.SELECT_ID);
                arrayCount++;
            }

            MapList actionList =
                DomainObject.getInfo(context, objIdArr,
                        new StringList(Assessment.SELECT_RESOURCE_STATUS));

            int actionListSize = 0;
            if (actionList != null)
            {
                actionListSize = actionList.size();
            }

            String red = "Red";
            String green = "Green";
            String yellow = "Yellow";
            String other = "---";

            for (int i = 0; i < actionListSize; i++)
            {
                String statusGif = "&nbsp;";
                objectMap = (Map) actionList.get(i);
                String assessmentStatus =
                    (String) objectMap.get(Assessment.SELECT_RESOURCE_STATUS);

                if (red.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"/>";
                }
                else if (yellow.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"/>";
                }
                else if (green.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"/>";
                }
                else if (other.equals(assessmentStatus))
                {
                    statusGif = "---";
                }

                showResourceStatus.add(statusGif);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return showResourceStatus;
        }
    }

    /**
     * This method is used to show the schedule status icon image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the status icon value as String.
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
    public Vector getScheduleStatus(Context context, String[] args)
    throws Exception
    {
        Vector showScheduleStatus = new Vector();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            int arrayCount = 0;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[arrayCount] =
                    (String) objectMap.get(Assessment.SELECT_ID);
                arrayCount++;
            }

            MapList actionList =
                DomainObject.getInfo(context, objIdArr,
                        new StringList(Assessment.SELECT_SCHEDULE_STATUS));

            int actionListSize = 0;
            if (actionList != null)
            {
                actionListSize = actionList.size();
            }

            String red = "Red";
            String green = "Green";
            String yellow = "Yellow";
            String other = "---";

            for (int i = 0; i < actionListSize; i++)
            {
                String statusGif = "&nbsp;";
                objectMap = (Map) actionList.get(i);
                String assessmentStatus =
                    (String) objectMap.get(Assessment.SELECT_SCHEDULE_STATUS);

                if (red.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"/>";
                }
                else if (yellow.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"/>";
                }
                else if (green.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"/>";
                }
                else if (other.equals(assessmentStatus))
                {
                    statusGif = "---";
                }

                showScheduleStatus.add(statusGif);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return showScheduleStatus;
        }
    }

    /**
     * This method is used to show the finanace status icon image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the status icon value as String.
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
    public Vector getFinanceStatus(Context context, String[] args)
    throws Exception
    {
        Vector showFinanceStatus = new Vector();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            int arrayCount = 0;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[arrayCount] =
                    (String) objectMap.get(Assessment.SELECT_ID);
                arrayCount++;
            }

            MapList actionList =
                DomainObject.getInfo(context, objIdArr,
                        new StringList(Assessment.SELECT_FINANCE_STATUS));

            int actionListSize = 0;
            if (actionList != null)
            {
                actionListSize = actionList.size();
            }

            String red = "Red";
            String green = "Green";
            String yellow = "Yellow";
            String other = "---";

            for (int i = 0; i < actionListSize; i++)
            {
                String statusGif = "&nbsp;";
                objectMap = (Map) actionList.get(i);
                String assessmentStatus =
                    (String) objectMap.get(Assessment.SELECT_FINANCE_STATUS);

                if (red.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"/>";
                }
                else if (yellow.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"/>";
                }
                else if (green.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"/>";
                }
                else if (other.equals(assessmentStatus))
                {
                    statusGif = "---";
                }

                showFinanceStatus.add(statusGif);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return showFinanceStatus;
        }
    }

    /**
     * This method is used to show the Risk status icon image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the status icon value as String.
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
    public Vector getRiskStatus(Context context, String[] args)
    throws Exception
    {
        Vector showRiskStatus = new Vector();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            int arrayCount = 0;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[arrayCount] =
                    (String) objectMap.get(Assessment.SELECT_ID);
                arrayCount++;
            }

            MapList actionList =
                DomainObject.getInfo(context, objIdArr,
                        new StringList(Assessment.SELECT_RISK_STATUS));

            int actionListSize = 0;
            if (actionList != null)
            {
                actionListSize = actionList.size();
            }

            String red = "Red";
            String green = "Green";
            String yellow = "Yellow";
            String other = "---";

            for (int i = 0; i < actionListSize; i++)
            {
                String statusGif = "&nbsp;";
                objectMap = (Map) actionList.get(i);
                String assessmentStatus =
                    (String) objectMap.get(Assessment.SELECT_RISK_STATUS);

                if (red.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"/>";
                }
                else if (yellow.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"/>";
                }
                else if (green.equals(assessmentStatus))
                {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"/>";
                }
                else if (other.equals(assessmentStatus))
                {
                    statusGif = "---";
                }

                showRiskStatus.add(statusGif);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return showRiskStatus;
        }
    }

    /**
     * gets the owner with lastname,firstname format
     * also has a link to open a pop up with the owner
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the owner name as String
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
    public Vector getOwner(Context context, String[] args)
    throws Exception
    {
        Vector owner = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;

            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            int arrayCount = 0;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[arrayCount] =
                    (String) objectMap.get(Assessment.SELECT_ID);
                arrayCount++;
            }

            MapList actionList =
                DomainObject.getInfo(context, objIdArr,
                        new StringList(Assessment.SELECT_OWNER));

            Iterator actionsListIterator = actionList.iterator();
            while (actionsListIterator.hasNext())
            {
                objectMap = (Map) actionsListIterator.next();
                owner.add(Person.getDisplayName(context,
                        (String) objectMap.get(Assessment.SELECT_OWNER)));
            }//ends while
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return owner;
        }
    }

    /**
     * gets the descripion
     * also has a link to open a pop up with the owner
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the owner name as String
     * @throws Exception if the operation fails
     * @since PMC 11.0.0.0
     */
    public Vector getDescription(Context context, String[] args)
    throws Exception
    {
        Vector desc = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map objectMap = null;

            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr = new String[objectList.size()];
            int arrayCount = 0;
            while (objectListIterator.hasNext())
            {
                objectMap = (Map) objectListIterator.next();
                objIdArr[arrayCount] =
                    (String) objectMap.get(Assessment.SELECT_ID);
                arrayCount++;
            }

            MapList actionList =
                DomainObject.getInfo(context, objIdArr,
                        new StringList(Assessment.SELECT_ASSESSMENT_COMMENTS));

            Iterator actionsListIterator = actionList.iterator();
            while (actionsListIterator.hasNext())
            {
                objectMap = (Map) actionsListIterator.next();
                desc.add((String) objectMap.get(Assessment.SELECT_ASSESSMENT_COMMENTS));
            }//ends while
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return desc;
        }
    }

    /**
     * This method determines if the checkbox needs to be enabled if the logged in person
     * is the owner of the Assessment in PMCAssessmentSummary table.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        objectList - objectList Contains a MapList of Maps which contains objects.
     * @return Object of type Vector
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector showAssessmentCheckbox(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");

        StringList slObjSelect = new StringList();
        slObjSelect.addElement(DomainConstants.SELECT_OWNER);
        Vector enableCheckbox = new Vector();
        try
        {
            Iterator objectListItr = objectList.iterator();
            int size = objectList.size();
            String[] aAssessmentIds = new String[size];
            int index = 0;
            while (objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String assessmentId = DomainConstants.EMPTY_STRING;
                assessmentId = (String) objectMap.get(DomainConstants.SELECT_ID);
                if(ProgramCentralUtil.isNotNullString(assessmentId))
                    aAssessmentIds[index] = assessmentId;
            }

            MapList mlAssessmentObj = DomainObject.getInfo(context, aAssessmentIds, slObjSelect);
            Iterator itrAssessmentObj = mlAssessmentObj.iterator();
            while(itrAssessmentObj.hasNext())
            {
                Map mAssessment = (Map)itrAssessmentObj.next();
                String assessmentOwner = (String)mAssessment.get(DomainConstants.SELECT_OWNER);
                if (ProgramCentralUtil.isNotNullString(assessmentOwner) && assessmentOwner.equals(context.getUser()))
                {
                    enableCheckbox.add("true");
                }
                else
                {
                    enableCheckbox.add("false");
                }
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
     * Gets the Assessment Status.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "objectId".
     * @return Object - boolean true if the operation is successful
     * @throws Exception if operation fails
     * @since PMC V6R2008-1
     */
    public String getAssesmentStatusValue(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        String mode = (String) requestMap.get("mode");
        String strStatus = "";
        String reportFormat=(String) requestMap.get("reportFormat");

        StringList busSelect = new StringList(1);
        busSelect.add(Assessment.SELECT_ASSESSMENT_STATUS);

        DomainObject dom = DomainObject.newInstance(context, strObjectId);
        Map objectMap = dom.getInfo(context, busSelect);

        String assessmentStatus =
            (String) objectMap.get(Assessment.SELECT_ASSESSMENT_STATUS);

        String red = "Red";
        String green = "Green";
        String yellow = "Yellow";
        String other = "---";
        //Modified:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
        String sLanguage = context.getSession().getLanguage();
        String statusKey = "emxFramework.Range.Assessment_Status.";
        String convertedAssessmentStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                statusKey+assessmentStatus, sLanguage);

        if (mode==null || mode.equalsIgnoreCase("view")) {
            //Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
            //While exporting does not require html content
            if("CSV".equalsIgnoreCase(reportFormat))
            {
                strStatus=assessmentStatus;
            }
            else{
                String statusGif = "";
                if (red.equals(assessmentStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\">&nbsp;";
                }
                else if (yellow.equals(assessmentStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\">&nbsp;";
                }
                else if (green.equals(assessmentStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\">&nbsp;";
                }
                else {
                    statusGif = "&nbsp;";
                }
                strStatus = statusGif + convertedAssessmentStatus;
            }
            //End:Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x


        }
        else if (mode.equalsIgnoreCase("edit"))
        {
            String attrStatus  = PropertyUtil.getSchemaProperty(context,"attribute_AssessmentStatus");
            StringList assessmentRanges = FrameworkUtil.getRanges(context, attrStatus);
            int length = assessmentRanges.size();
            strStatus = "<select id=\"AssessmentStatusId\" name=\"AssessmentStatus\">";
            String options = "";

            for(int i=0;i<length;i++)
            {
                String assessmentObjStatus = (String) assessmentRanges.get(i);
                String convertedAssessmentObjStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                        statusKey+assessmentObjStatus, sLanguage);
                options += " <option value=\"" + XSSUtil.encodeForHTML(context,assessmentObjStatus) + "\"";
                if(assessmentStatus.equals(assessmentObjStatus))
                {
                    options += " selected";
                }
                options += ">";
                options += XSSUtil.encodeForHTML(context,convertedAssessmentObjStatus) + "</option> ";
                //End:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
            }
            strStatus += options + " </select> ";
        }
        return strStatus;
    }



    /**
     * Gets the Resource Status.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "objectId".
     * @return Object - boolean true if the operation is successful
     * @throws Exception if operation fails
     * @since PMC V6R2008-1
     */
    public String getResourceStatusValue(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        String mode = (String) requestMap.get("mode");
        String reportFormat=(String) requestMap.get("reportFormat");
        String strStatus = "";

        StringList busSelect = new StringList(1);
        busSelect.add(Assessment.SELECT_RESOURCE_STATUS);

        DomainObject dom = DomainObject.newInstance(context, strObjectId);
        Map objectMap = dom.getInfo(context, busSelect);

        String resourceStatus =
            (String) objectMap.get(Assessment.SELECT_RESOURCE_STATUS);

        String red = "Red";
        String green = "Green";
        String yellow = "Yellow";
        String other = "---";
        //Modified:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
        String sLanguage = context.getSession().getLanguage();
        String statusKey = "emxFramework.Range.Assessment_Status.";
        String convertedResourceStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                statusKey+resourceStatus, sLanguage);

        if (mode==null || mode.equalsIgnoreCase("view")) {
            //Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
            //While exporting does not require html content

            if("CSV".equalsIgnoreCase(reportFormat))
            {
                strStatus=resourceStatus;
            }
            else{
                String statusGif = "";
                if (red.equals(resourceStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\">&nbsp;";
                }
                else if (yellow.equals(resourceStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\">&nbsp;";
                }
                else if (green.equals(resourceStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\">&nbsp;";
                }
                else {
                    statusGif = "&nbsp;";
                }
                strStatus = statusGif + convertedResourceStatus;
            }
            //End:Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
        }
        else if (mode.equalsIgnoreCase("edit"))
        {
            String attrResourceStatus  = PropertyUtil.getSchemaProperty(context,"attribute_ResourceStatus");
            StringList resourceRanges = FrameworkUtil.getRanges(context, attrResourceStatus);
            int length = resourceRanges.size();
            strStatus = "<select id=\"ResourceStatusId\" name=\"ResourceStatus\">";
            String options = "";

            for(int i=0;i<length;i++)
            {
                String resourceObjStatus = (String)resourceRanges.get(i);
                String convertedResourceObjStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                        statusKey+resourceObjStatus, sLanguage);
                options += " <option value=\"" + XSSUtil.encodeForHTML(context,resourceObjStatus) + "\"";
                if(resourceStatus.equals(resourceObjStatus))
                {
                    options += " selected";
                }
                options += ">";
                options += XSSUtil.encodeForHTML(context,convertedResourceObjStatus) + "</option> ";
                //End:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
            }
            strStatus += options + " </select> ";
        }
        return strStatus;
    }


    /**
     * Gets the Schedule Status.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "objectId".
     * @return Object - boolean true if the operation is successful
     * @throws Exception if operation fails
     * @since PMC V6R2008-1
     */
    public String getScheduleStatusValue(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        String mode = (String) requestMap.get("mode");
        String reportFormat=(String) requestMap.get("reportFormat");


        String strStatus = "";

        StringList busSelect = new StringList(1);
        busSelect.add(Assessment.SELECT_SCHEDULE_STATUS);

        DomainObject dom = DomainObject.newInstance(context, strObjectId);
        Map objectMap = dom.getInfo(context, busSelect);

        String scheduleStatus = (String) objectMap.get(Assessment.SELECT_SCHEDULE_STATUS);

        String red = "Red";
        String green = "Green";
        String yellow = "Yellow";
        String other = "---";
        //Modified:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
        String sLanguage = context.getSession().getLanguage();
        String statusKey = "emxFramework.Range.Assessment_Status.";
        String convertedScheduleStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                statusKey+scheduleStatus, sLanguage);
        if (mode == null || "view".equalsIgnoreCase(mode))
        {
            //Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
            //While exporting does not require html content
            if("CSV".equalsIgnoreCase(reportFormat))
            {
                strStatus=scheduleStatus;
            }
            else{
                String statusGif = "";
                if(red.equals(scheduleStatus)) {
                    statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\">&nbsp;";
                }
                else if(green.equals(scheduleStatus)) {
                    statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\">&nbsp;";
                }
                else if(yellow.equals(scheduleStatus)) {
                    statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\">&nbsp;";
                }
                else {
                    statusGif = "&nbsp;";
                }
                strStatus = statusGif + convertedScheduleStatus;
            }
            //End:Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x

        }
        else if ("edit".equalsIgnoreCase(mode))
        {
            String attrScheduleStatus = PropertyUtil.getSchemaProperty(context,"attribute_ScheduleStatus");
            StringList scheduleRanges = FrameworkUtil.getRanges(context, attrScheduleStatus);
            int length = scheduleRanges.size();
            strStatus = "<select id=\"ScheduleStatusId\" name=\"ScheduleStatus\">";
            String options = "";

            for(int i=0;i<length;i++)
            {
                String scheduleObjStatus = (String)scheduleRanges.get(i);
                String convertedScheduleObjStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                        statusKey+scheduleObjStatus, sLanguage);
                options += "<option value=\"" + XSSUtil.encodeForHTML(context,scheduleObjStatus) + "\"";
                if (scheduleStatus.equals(scheduleObjStatus))
                {
                    options += " selected";
                }
                options += ">";
                options += XSSUtil.encodeForHTML(context,convertedScheduleObjStatus) + "</option>";
                //End:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
            }
            strStatus += options + "</select>";
        }
        return strStatus;
    }



    /**
     * Gets the Finance Status.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "objectId".
     * @return Object - boolean true if the operation is successful
     * @throws Exception if operation fails
     * @since PMC V6R2008-1
     */
    public String getFinanceStatusValue(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        String mode = (String) requestMap.get("mode");
        String reportFormat=(String) requestMap.get("reportFormat");
        String strStatus = "";

        StringList busSelect = new StringList(1);
        busSelect.add(Assessment.SELECT_FINANCE_STATUS);

        DomainObject dom = DomainObject.newInstance(context, strObjectId);
        Map objectMap = dom.getInfo(context, busSelect);

        String financeStatus =
            (String) objectMap.get(Assessment.SELECT_FINANCE_STATUS);

        String red = "Red";
        String green = "Green";
        String yellow = "Yellow";
        String other = "---";
        //Modified:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
        String sLanguage = context.getSession().getLanguage();
        String statusKey = "emxFramework.Range.Assessment_Status.";
        String convertedFinanceStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                statusKey+financeStatus, sLanguage);

        if (mode==null || mode.equalsIgnoreCase("view"))
        {
            //Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
            //While exporting does not require html content

            if("CSV".equalsIgnoreCase(reportFormat))
            {
                strStatus=financeStatus;
            }
            else{
                String statusGif = "";
                if (red.equals(financeStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\">&nbsp;";
                }
                else if (yellow.equals(financeStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\">&nbsp;";
                }
                else if (green.equals(financeStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\">&nbsp;";
                }
                else {
                    statusGif = "&nbsp;";
                }
                strStatus = statusGif + convertedFinanceStatus;
            }
            //End:Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x

        }
        else if (mode.equalsIgnoreCase("edit"))
        {
            String attrFinanceStatus  = PropertyUtil.getSchemaProperty(context,"attribute_FinanceStatus");
            StringList financeRanges = FrameworkUtil.getRanges(context, attrFinanceStatus);
            int length = financeRanges.size();
            strStatus = "<select id=\"FinancialStatusId\" name=\"FinancialStatus\">";
            String options = "";

            for(int i=0;i<length;i++)
            {
                String financeObjStatus = (String)financeRanges.get(i);
                String convertedFinanceObjStatus =
                        EnoviaResourceBundle.getProperty(context, "Framework",statusKey+financeObjStatus, sLanguage);
                options += " <option value=\"" +XSSUtil.encodeForHTML(context, financeObjStatus) + "\"";
                if(financeStatus.equals(financeObjStatus))
                {
                    options += " selected";
                }
                options += ">";
                options += XSSUtil.encodeForHTML(context,convertedFinanceObjStatus) + "</option> ";
                //End:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
            }
            strStatus += options + " </select> ";
        }
        return strStatus;
    }


    /**
     * Gets the Risk Status.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "objectId".
     * @return Object - boolean true if the operation is successful
     * @throws Exception if operation fails
     * @since PMC V6R2008-1
     */
    public String getRiskStatusValue(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");
        String mode = (String) requestMap.get("mode");
        String reportFormat=(String) requestMap.get("reportFormat");

        String strStatus = "";

        StringList busSelect = new StringList(1);
        busSelect.add(Assessment.SELECT_RISK_STATUS);

        DomainObject dom = DomainObject.newInstance(context, strObjectId);
        Map objectMap = dom.getInfo(context, busSelect);

        String riskStatus =
            (String) objectMap.get(Assessment.SELECT_RISK_STATUS);

        String red = "Red";
        String green = "Green";
        String yellow = "Yellow";
        String other = "---";
        //Modified:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
        String sLanguage = context.getSession().getLanguage();
        String statusKey = "emxFramework.Range.Assessment_Status.";
        String convertedRiskStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                statusKey+riskStatus, sLanguage);

        if (mode==null || mode.equalsIgnoreCase("view"))
        {
            //Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
            //While exporting does not require html content

            if("CSV".equalsIgnoreCase(reportFormat))
            {
                strStatus=riskStatus;
            }
            else{
                String statusGif = "";
                if (red.equals(riskStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\">&nbsp;";
                }
                else if (yellow.equals(riskStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\">&nbsp;";
                }
                else if (green.equals(riskStatus)) {
                    statusGif =
                        "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\">&nbsp;";
                }
                else {
                    statusGif = "&nbsp;";
                }
                strStatus = statusGif + convertedRiskStatus;
            }
            //End:Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x

        }
        else if (mode.equalsIgnoreCase("edit"))
        {
            String attrRiskStatus  = PropertyUtil.getSchemaProperty(context,"attribute_RiskStatus");
            StringList riskRanges = FrameworkUtil.getRanges(context, attrRiskStatus);
            int length = riskRanges.size();
            strStatus = "<select id=\"RiskStatusId\" name=\"RiskStatus\">";
            String options = "";

            for(int i=0;i<length;i++)
            {
                String riskObjStatus = (String)riskRanges.get(i);
                String convertedRiskObjStatus = EnoviaResourceBundle.getProperty(context, "Framework",
                        statusKey+riskObjStatus, sLanguage);
                options += " <option value=\"" + XSSUtil.encodeForHTML(context,riskObjStatus) + "\"";
                if(riskStatus.equals(riskObjStatus))
                {
                    options += " selected";
                }
                options += ">";
                options += XSSUtil.encodeForHTML(context,convertedRiskObjStatus) + "</option> ";
                //End:14-Feb-2011:hp5:R210:PRG:IR-090023V6R2012
            }
            strStatus += options + " </select> ";
        }
        return strStatus;
    }



    /* This method gets the field value for attribute 'Originator' on Create Form.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @throws Exception if the operation fails
     * @since PMC V6R2008-1
     */
    public String getOriginator(Context context, String args[]) throws Exception
    {
        return ProgramCentralUtil.getOriginator(context, args);
    }

    /* This method gets the field value for attribute 'Originated' on Create Form.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @throws Exception if the operation fails
     * @since PMC V6R2008-1
     */
    public String getOriginated(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String timezone = (String) requestMap.get("timeZone");
        double dbTimeZone = Task.parseToDouble(timezone);
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar();
        String strToday = formatter.format(cal.getTime());
        strToday = eMatrixDateFormat.getFormattedDisplayDate(strToday,dbTimeZone,Locale.getDefault());
        return strToday;
    }

    /* This method modifies the Assessment object created and also connects it.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @throws Exception if the operation fails
     * @since PMC V6R2008-1
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void createAssessment(Context context, String args[]) throws Exception
    {
        // Check license while creating Assessment, if license check fails here
        // the assessment will not be created.
        //
        ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_PRG_LICENSE_ARRAY);

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String name = (String) requestMap.get("Name");
        // Modified:15-Apr-109:nzf:R207:PRG:Bug:369788
        String objectId = (String) requestMap.get("parentOID");
        // End:R207:PRG:Bug:369788
        String assessmentId = (String) paramMap.get("objectId");
        Assessment assessment = new Assessment(assessmentId);

        String rev = assessment.getUniqueName(context);
        int start = rev.indexOf("_");
        int end = rev.length();
        rev = rev.substring(start+1,end);
        String modifyQuery = " modify bus $1 $2 $3 name $4 revision $5";
		MqlUtil.mqlCommand(context,modifyQuery,"Assessment",name,EMPTY_STRING, name,rev);

        DomainObject projectspace = DomainObject.newInstance(context, objectId);
        if (projectspace.checkAccess(context,(short) AccessConstants.cFromConnect))
        {
            DomainRelationship.connect(context,
                    objectId,
                    RELATIONSHIP_PROJECT_ASSESSMENT,
                    assessmentId,true);
        }
        else
        {
            ContextUtil.pushContext(context);
            DomainRelationship.connect(context,
                    objectId,
                    Assessment.RELATIONSHIP_PROJECT_ASSESSMENT,
                    assessmentId,true);

            ContextUtil.popContext(context);
        }



        String relationship = DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST;
        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        Map PALMap = projectspace.getRelatedObject(context,relationship,false,busSelects,null);
        if(null != PALMap && PALMap.size() > 0){      
        String PALId = (String)PALMap.get(DomainConstants.SELECT_ID);

        DomainObject projectAccessList = DomainObject.newInstance(context, PALId);
        DomainRelationship.connect(context,PALId,DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY,assessmentId,true);
        }
        //  assessment.connect(context,relProjectAccessKey,false,(BusinessObject)projectAccessList);

    }

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void editAssessment(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        String ScheduleStatus = (String) requestMap.get("ScheduleStatus");
        String AssessmentStatus = (String) requestMap.get("AssessmentStatus");
        String RiskStatus = (String) requestMap.get("RiskStatus");
        String FinancialStatus = (String) requestMap.get("FinancialStatus");
        String ResourceStatus = (String) requestMap.get("ResourceStatus");
        String Assessor = (String) requestMap.get("Assessor");
        Assessment assessment = new Assessment(objectId);
        assessment.setOwner(context,Assessor);
        HashMap attributeMap = new HashMap();
        attributeMap.put(Assessment.ATTRIBUTE_SCHEDULE_STATUS,ScheduleStatus);
        attributeMap.put(ATTRIBUTE_ASSESSMENT_STATUS,AssessmentStatus);
        attributeMap.put(ATTRIBUTE_RISK_STATUS ,RiskStatus);
        attributeMap.put(ATTRIBUTE_FINANCE_STATUS,FinancialStatus);
        attributeMap.put(ATTRIBUTE_RESOURCE_STATUS,ResourceStatus);
        assessment.setAttributeValues(context,(Map)attributeMap);
    }

    /* This method checks whether a user has access to Edit Assessment Details
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @throws Exception if the operation fails
     * @since PMC V6R2008-1
     */
    public boolean displayEditCommand(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("parentOID");
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(objectId);
        boolean editFlag = domainObject.checkAccess(context, (short) AccessConstants.cModify);
        return editFlag;
    }

    /**
     * Gets the Assessors
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, "objectId".
     * @return Object - boolean true if the operation is successful
     * @throws Exception if operation fails
     * @since PMC V6R2008-1
	 * @deprecated Not in use.	
     */
    public String getAssesor(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        String mode = (String) requestMap.get("mode");
        String strStatus = "";
        StringList busSelect = new StringList(1);
        busSelect.add(Assessment.SELECT_OWNER);
        busSelect.add("attribute[" + DomainConstants.ATTRIBUTE_ORIGINATOR + "]");

        DomainObject assessment = DomainObject.newInstance(context, objectId);
        Map objectMap = assessment.getInfo(context, busSelect);

        String originator = (String)objectMap.get("attribute[" +
                DomainConstants.ATTRIBUTE_ORIGINATOR + "]");
        String owner = (String)objectMap.get(Assessment.SELECT_OWNER);

        if (mode==null || mode.equalsIgnoreCase("view")) {
            strStatus = owner;
        }
        else if (mode.equalsIgnoreCase("edit")) {

            StringList assessors = new StringList(2);
            assessors.add(originator);

            String relationship = Assessment.RELATIONSHIP_PROJECT_ASSESSMENT;
            StringList busSelects = new StringList();
            busSelects.add(DomainConstants.SELECT_ID);

            Map projectMap = assessment.getRelatedObject(context,relationship,false,busSelects,null);

            String projectId = (String)projectMap.get(DomainConstants.SELECT_ID);
            com.matrixone.apps.program.ProjectSpace projectSpace =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

            // Getting the list of members of the project
            StringList busSelects1 = new StringList(1);
            busSelects1.add(Person.SELECT_NAME);
            StringList memberSelects = new StringList(1);
            memberSelects.add(MemberRelationship.SELECT_PROJECT_ROLE);
            // Added:14-Apr-109:nzf:R207:PRG:Bug:373027
            memberSelects.add(MemberRelationship.SELECT_PROJECT_ACCESS);
            // End:R207:PRG:Bug:373027
            projectSpace.setId(projectId);

            MapList membersList =
                projectSpace.getMembers(context, busSelects1, memberSelects,
                        null, null);
            Iterator membersListItr = null;
            membersListItr = membersList.iterator();
            while (membersListItr.hasNext())
            {
                Map Currentmember = (Map) membersListItr.next();
                String assessorRole = (String) Currentmember.get(MemberRelationship.SELECT_PROJECT_ROLE);
                // Modified:14-Apr-109:nzf:R207:PRG:Bug:373027
                String accessorAccess = (String) Currentmember.get(MemberRelationship.SELECT_PROJECT_ACCESS);
                if (ProgramCentralConstants.PROJECT_ACCESS_PROJECT_ASSESSOR.equals(accessorAccess) || ProgramCentralConstants.PROJECT_ACCESS_PROJECT_LEAD.equals(accessorAccess)|| ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER.equals(accessorAccess)) {
                    String person =(String) Currentmember.get(Person.SELECT_NAME);
                    if(!assessors.contains(person)){
                        assessors.add(person);
                    }
                }
                // End:R207:PRG:Bug:373027
            }
            int length = assessors.size();
            strStatus = "<select id=\"Assessor\" name=\"Assessor\">";

            String options = "";

            for(int i=0;i<length;i++) {
                options += " <option value=\"" + assessors.get(i) + "\"";

                if(owner.equals(assessors.get(i))){
                    options += " selected";
                }

                options += ">";
                options += assessors.get(i) + "</option> ";

            }
            strStatus += options + " </select> ";
        }
        return strStatus;
    }


    /**
     * When an assessment is created, grant the creator the proper
     * permissions on the assessment object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the assessment id
     * @throws Exception if operation fails
     */
    public void triggerCreateAction(Context context, String[] args)
        throws Exception
    {
        // get values from args.
        String assessmentId = args[0]; // Assessment ID
        String personId = com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context);
        DomainAccess.createObjectOwnership(context, assessmentId, personId, "Project Lead", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
    }

	/**
	 * Lists the persons eligible to be assessor in the project
	 * @param context the ENOVIA Context object.
	 * @param args Request arguments.
	 * @return A StringList of person ids to be displayed on the Assessor search page.
	 * @throws Exception If operation fails.
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getPeopleForAssessor(Context context,String[]args)throws Exception
	{
		StringList slPersonList = new StringList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strProjectId = (String) programMap.get("objectId");
		String contextId = PersonUtil.getPersonObjectID(context);
		
		StringList objectSelect = new StringList();
		objectSelect.add(ProgramCentralConstants.SELECT_COLLABORATIVE_SPACE);
		objectSelect.add(ProgramCentralConstants.SELECT_ORGANIZATION);
		objectSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY);
		ProjectSpace project = new ProjectSpace(strProjectId);
		Map projectInfo = project.getInfo(context, objectSelect);

		String strSecurityContext = DomainObject.EMPTY_STRING;
		StringList scPersonList = new StringList();
		String cseRole = ProgramCentralConstants.ROLE_PROJECT_LEAD;
		String vplmRole = ProgramCentralConstants.ROLE_VPLM_PROJECT_LEADER;
		String defaultProject = DomainAccess.getDefaultProject(context);
		String visibility = (String) projectInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY);

		//If company visibilty, then add all Project Leaders in the list who belong to the project's ORG.CS
		if("Company".equals(visibility)){
			String cs = (String) projectInfo.get(ProgramCentralConstants.SELECT_COLLABORATIVE_SPACE);
			String org = (String) projectInfo.get(ProgramCentralConstants.SELECT_ORGANIZATION);
			strSecurityContext = org + "." + cs;
			if(strSecurityContext.contains(defaultProject)){
				strSecurityContext = cseRole + "." + strSecurityContext;
			}else{
				strSecurityContext = vplmRole + "." + strSecurityContext;
			}
			scPersonList = getMembersBySecurityContext(context, strSecurityContext);
				if(null!=scPersonList && scPersonList.size()>0){
					slPersonList.addAll(scPersonList); 					
				}
			}
		
		//Add all project members in the person list.
		String username = DomainObject.EMPTY_STRING;
		String userId = DomainObject.EMPTY_STRING;
		MapList projectOwnershipList = project.getOwnershipAccess(context, strProjectId, true);
		for (Iterator iterator = projectOwnershipList.iterator(); iterator.hasNext();) {
			Map ownershipInfo = (Map) iterator.next();
			String isPersonOwnership = (String)ownershipInfo.get("isPersonOwnership");
			String access = (String)ownershipInfo.get("access");
			
			//If ownership is a person ownership
			if("true".equalsIgnoreCase(isPersonOwnership) && ("All".equalsIgnoreCase(access) 
					|| "Project Lead".equals(access))){
				username = (String)ownershipInfo.get("personName");
				userId = PersonUtil.getPersonObjectID(context, username);
				slPersonList.addElement(userId);
			}
			//If ownership is a security context ownership
			else if("All".equalsIgnoreCase(access) || "Project Lead".equals(access)){
				String securityContextName = (String)ownershipInfo.get("name");
				if(securityContextName.contains(defaultProject)){
					securityContextName = cseRole + "." + securityContextName;
				}else{
					securityContextName = vplmRole + "." + securityContextName;
				}
				scPersonList = getMembersBySecurityContext(context, securityContextName);
				if(null!=scPersonList && scPersonList.size()>0){
					slPersonList.addAll(scPersonList);
				}				
			}
		}
		
		//Remove current assessor Id.
		slPersonList.remove(contextId);
		
		//Remove duplicate entries
		Set<String> uniqueMembersIds = new HashSet<String>(slPersonList);  
		slPersonList.clear();
		slPersonList.addAll(uniqueMembersIds);
		return slPersonList;
	}

	/**
	 * Returns a list of person from the given security context. 
	 * @param context the ENOVIA Context object.
	 * @param strSecurityContext The Security Context string. The format of the string should be in this 
	 * format - Organizaiton_Role.Organizaiton_Name.Collaborative_Space_Name, e.g. Project Lead.MyCompany.GLOBAL
	 * @return a StringList of people connected to the given security context.
	 * @throws MatrixException if operation fails.
	 */
	private StringList getMembersBySecurityContext(Context context, String strSecurityContext) throws MatrixException{
		String defaultProject = DomainAccess.getDefaultProject(context);
		StringList members = new StringList();
		String sCommandStatement = "temp query bus $1 $2 $3 select $4 dump $5";
		String output =  MqlUtil.mqlCommand(context, sCommandStatement, 
				"Security Context", strSecurityContext, "-", "id", "|"); 
		if(ProgramCentralUtil.isNotNullString(output)){
			StringList outputList = FrameworkUtil.split(output, "|");
			String securityContextId = (String)outputList.get(3);
			DomainObject securityContext = DomainObject.newInstance(context, securityContextId);

			String relAssignedSecurityContext = "Assigned Security Context";
			String select = "to[" + relAssignedSecurityContext + "].from.id"; 
			members = securityContext.getInfoList(context, select);
		}
		return members;
	}
}
