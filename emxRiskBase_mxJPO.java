/**
 * emxRiskBase.java
 *
 * Copyright (c) 2002-2016 Dassault Systemes.
 * All Rights Reserved
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.15.2.2 Thu Dec  4 07:55:15 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.15.2.1 Thu Dec  4 01:53:23 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.15 Wed Oct 22 15:49:52 2008 przemek Experimental przemek $
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Risk;
import com.matrixone.apps.program.RiskRPNRelationship;
import com.matrixone.apps.program.Task;

import jdk.nashorn.internal.runtime.regexp.joni.Option;

import matrix.db.AccessConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

/**
 * The <code>emxRiskBase</code> class represents the Risk JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxRiskBase_mxJPO extends com.matrixone.apps.program.Risk
{
    //~ Static fields/initializers ---------------------------------------------
    /** The project access list id relative to project. */
    static protected final String SELECT_PROJECT_ACCESS_LIST_ID = "to["
        + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";

    /** state "Create" for the "Project Risk" policy. */
    public static final String STATE_PROJECT_RISK_CREATE = PropertyUtil
        .getSchemaProperty("policy", POLICY_PROJECT_RISK, "state_Create");

    /** state "Assign" for the "Project Risk" policy. */
    public static final String STATE_PROJECT_RISK_ASSIGN = PropertyUtil
        .getSchemaProperty("policy", POLICY_PROJECT_RISK, "state_Assign");

    /** state "Active" for the "Project Risk" policy. */
    public static final String STATE_PROJECT_RISK_ACTIVE = PropertyUtil
        .getSchemaProperty("policy", POLICY_PROJECT_RISK, "state_Active");

    /** state "Review" for the "Project Risk" policy. */
    public static final String STATE_PROJECT_RISK_REVIEW = PropertyUtil
        .getSchemaProperty("policy", POLICY_PROJECT_RISK, "state_Review");

    /** state "Complete" for the "Project Risk" policy. */
    public static final String STATE_PROJECT_RISK_COMPLETE = PropertyUtil
        .getSchemaProperty("policy", POLICY_PROJECT_RISK, "state_Complete");

    public static final String SELECT_RISK_ACTUAL_START_DATE = "attribute[Actual Start Date].value";
    public static final String SELECT_RISK_ACTUAL_END_DATE = "attribute[Actual End Date].value";
    public static final String SELECT_RISK_ESTIMATED_START_DATE = "attribute[Estimated Start Date].value";
    public static final String SELECT_RISK_ESTIMATED_END_DATE = "attribute[Estimated End Date].value";
    //~ Instance fields --------------------------------------------------------

    /** Id of the Access List Object for this Project. */
    protected DomainObject _accessListObject = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs a new emxRisk JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public emxRiskBase_mxJPO(Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super();

        if((args != null) && (args.length > 0))
        {
            setId(args[0]);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Get the access list object for this Project.
     *
     * @param context the eMatrix <code>Context</code> object
     * @return DomainObject access list object
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.2
     */
    protected DomainObject getAccessListObject(Context context)
        throws Exception
    {
        if(_accessListObject == null)
        {
            String accessListID = getInfo(context, SELECT_PROJECT_ACCESS_LIST_ID);

            if((accessListID != null) && !"".equals(accessListID))
            {
                _accessListObject = DomainObject.newInstance(context, accessListID);
            }
        }

        return _accessListObject;
    }


    /**
     * This function verifies the user's permission for the given risk.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *      PROJECT_MEMBER to see if the context user is a project member, <BR>
     *      PROJECT_LEAD to see if the context user is a project lead, <BR>
     *      RISK_ASSIGNEE to see if the context user is an assignee of this
     *                    risk, <BR>
     *      PROJECT_OWNER to see if the context user is a project owner
     * @return boolean true or false
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */
    public boolean hasAccess(Context context, String[] args)
        throws Exception
    {
        boolean access = false;

        for(int i = 0; i < args.length; i++)
        {
            String accessType = args[i];

            if("RISK_ASSIGNEE".equals(accessType))
            {
                String objectWhere = "name == \"" + context.getUser() + "\"";
                MapList mapList    = getAssignees(context, null, // objectSelects,
                        null, // relationshipSelects
                        objectWhere, null);
                access = (mapList.size() > 0) ? true : false;
            }
            else if("PROJECT_MEMBER".equals(accessType)
                    || "PROJECT_LEAD".equals(accessType)
                    || "PROJECT_OWNER".equals(accessType))
            {
                DomainObject accessListObject = getAccessListObject(context);

                if(accessListObject != null)
                {
                    int iAccess;

                    if("PROJECT_MEMBER".equals(accessType))
                    {
                        iAccess = AccessConstants.cExecute;
                    }
                    else if("PROJECT_LEAD".equals(accessType))
                    {
                        iAccess = AccessConstants.cModify;
                    }
                    else
                    {
                        iAccess = AccessConstants.cOverride;
                    }

                    if(accessListObject.checkAccess(context, (short) iAccess))
                    {
                        access = true;
                    }
                }
            }

            if(access == true)
            {
                break;
            }
        }

        return access;
    }


    /**
     * This function modifies the attributes
     * Sets the actual start date on promoting Risk to Active state
     * Resets the actual start date on demoting Risk from Active state
     *
     * Sets the actual completion date on promoting Risk to Complete state
     * Resets the completion date on demoting Risk from Complete state
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the from object id
     *        1 - String containing the from state
     *        2 - String containing the to state
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     */
    public void triggerModifyAttributes(Context context, String[] args)
        throws Exception
    {
        // get values from args.
        String objectId  = args[0];
        String fromState = args[1];
        String toState   = args[2];

        String cmd    = "get env $1";
        String sEvent = MqlUtil.mqlCommand(context, cmd, true,"EVENT");

        /** "MATRIX_DATE_FORMAT". */
        SimpleDateFormat MATRIX_DATE_FORMAT = new SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);

        setId(objectId);

        if(sEvent.equals("Promote"))
        {
            if(toState.equals(STATE_PROJECT_RISK_ACTIVE))
            {
                String actualStartDate = MATRIX_DATE_FORMAT.format(new Date());
                setAttributeValue(context, ATTRIBUTE_ACTUAL_START_DATE,
                    actualStartDate);
            }
            else if(toState.equals(STATE_PROJECT_RISK_COMPLETE))
            {
                //finish date
                Map attributes          = new HashMap(3);
                Date fDate              = new Date();
                String actualFinishDate = MATRIX_DATE_FORMAT.format(fDate);
                attributes.put(ATTRIBUTE_ACTUAL_END_DATE, actualFinishDate);
                setAttributeValues(context, attributes);
            }
        }
        else if(sEvent.equals("Demote"))
        {
            if(fromState.equals(STATE_PROJECT_RISK_COMPLETE))
            {
                setAttributeValue(context, ATTRIBUTE_ACTUAL_END_DATE, "");
            }

            if(fromState.equals(STATE_PROJECT_RISK_ACTIVE))
            {
                setAttributeValue(context, ATTRIBUTE_ACTUAL_START_DATE, "");
            }
        }

        DebugUtil.debug("Exiting triggerModifyAttributes function");
    }


    /****************************************************************************************************
     *       Methods for Config Table Conversion Task
     ****************************************************************************************************/
    /**
     * This method return true if user have permissions on the command
     * otherwise return false.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *    objectId   - String containing the projectID
     * @return Boolean set to true to retrive the project member's list othewise return false.
     * @throws Exception If the operation fails.
     * @since PMC 10-6
     */
    public boolean hasAccessToMembers(Context context, String[] args)throws Exception {

        /*HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);

        StringList taskSubTypeList = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_TASK_MANAGEMENT);
        StringList projectSubTypeList = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_SPACE);;

        boolean hasAccess = Boolean.FALSE;
        boolean modifyAccess = Boolean.TRUE;
        String strType = DomainObject.EMPTY_STRING;
        try
        {
            if(ProgramCentralUtil.isNotNullString(objectId)) {
                DomainObject domainObject = DomainObject.newInstance(context, objectId);
                modifyAccess = domainObject.checkAccess(context,(short) AccessConstants.cModify);
                strType = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
            }

            if(!modifyAccess){
                if(ProgramCentralUtil.isNotNullString(objectId) && (projectSubTypeList.contains(strType))){
                    project.setId(objectId);
                    hasAccess = project.isCtxUserProjectMember(context);
                }
            } else{
                hasAccess = true;
            }

            if(projectSubTypeList.contains(strType) && hasAccess){
                hasAccess = ProgramCentralUtil.hasDPMLicense(context);//only Project DPM members allowed.
            } else if(taskSubTypeList.contains(strType) && hasAccess){
                Task task = new Task(objectId);
                hasAccess = task.isCtxUserTaskAssignee(context);
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        } finally {
            return hasAccess;
        }*/

        return this.checkEditAccess(context, args);
    }


    /**
     * This method is used to gets the list of All Risks to context User
     * Used for PMCRisksSummary table
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getAllRisks(Context context, String[] args)
        throws Exception
    {
        return getRiskAndRpn(context, args, "");
    }


    /**
     * This method is used to gets the list of Risks in Active State to context User
     * Used for PMCRisksSummary table
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getActiveRisks(Context context, String[] args)
        throws Exception
    {
        return getRiskAndRpn(context, args,
            "current != " + STATE_PROJECT_RISK_COMPLETE);
    }


    /**
     * This method is used to gets the list of Risks in Complete State to context User
     * Used for PMCRisksSummary table
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getCompletedRisks(Context context, String[] args)
        throws Exception
    {
        return getRiskAndRpn(context, args,
            "current == " + STATE_PROJECT_RISK_COMPLETE);
    }


    /**
     * This method is used to gets the list of Risks objects owned by the user.
     * Used for PMCRisksSummary table
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @param busWhere optional business object where clause
     * @return MapList containing the id of Risks objects of the logged in user
     * @throws Exception if the operation fails
     * @deprecated use {@link #   } instead
     * @since PMC 10.0.0.0
     */
    @Deprecated
    public Object getRisks(Context context, String[] args, String busWhere)
        throws Exception
    {
        // Check license while listing Risk, if license check fails here
        // the risks will not be listed.
        //
        ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_PRG_LICENSE_ARRAY);

        DomainObject domainObject = null;
        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        String objectId           = (String) programMap.get("objectId");
        String importType         = PropertyUtil.getSchemaProperty(context,"type_Risk");
        ContextUtil.setAttribute(context, "importType", importType);

        MapList riskList = null;
        Map mpRisk;
        String strRiskState = DomainConstants.EMPTY_STRING;

        try
        {
            StringList busSelects = new StringList(1);
            busSelects.add(Risk.SELECT_ID);
            busSelects.add(Risk.SELECT_CURRENT);

            // pagination change
            if((objectId == null) || objectId.equals("")
                    || objectId.equals("null"))
            {
                com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person
                    .getPerson(context);

                //page is called from 'myDesk'
                //Retrieves a list of risks associated with an assignee (Person).
                riskList = Risk.getAssignedRisks(context, person, busSelects,
                        null, busWhere, null);
            }
            else
            {
                com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject
                    .newInstance(context, DomainConstants.TYPE_PROJECT_SPACE,
                        DomainConstants.PROGRAM);

                project.setId(objectId);

                String typeName = project.getInfo(context, project.SELECT_TYPE);

                if(typeName.equals(project.TYPE_PROJECT_SPACE)
                        || mxType.isOfParentType(context, typeName,
                            DomainConstants.TYPE_PROJECT_SPACE)) //Modified for Subtype
                {
                    //Retrieves a list of risks associated with a parent.
                    riskList = Risk.getRisks(context, project, busSelects,
                            null, busWhere);

                    //Added:PRG:I16:R212:25-08-2011:IR-108413V6R2012x Start
                    MapList mlTasksRisks = null;
                    // Get all risk associated with each WBS task in project map list
                    mlTasksRisks = Risk.getProjectAllRisks(context, project, busWhere);
                    riskList.addAll(mlTasksRisks);
                    //Added:PRG:I16:R212:25-08-2011:IR-108413V6R2012x End
                }
                else
                {
                    riskList = Risk.getRisks(context, busSelects, null,
                            busWhere, project);
                }
            }
            for (int i=0;i<riskList.size();i++)
            {
                mpRisk = (Map)riskList.get(i);
                strRiskState = (String)mpRisk.get(SELECT_CURRENT);
                if (UIUtil.isNotNullAndNotEmpty(strRiskState) && !"Create".equals(strRiskState)) {
                    mpRisk.put("disableSelection", "true");
                }
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        finally
        {
            return riskList;
        }
    }


    /**
     * This method is used to disable Checkbox of Risk when State is not Create
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - objectList contains a MapList of Maps which contains objects.
     * @return Vector containing the risk items value as String.
     * @throws Exception if the operation fails
     * @since PMC X+2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getRiskRPNs(Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId    = (String) programMap.get("objectId");
        MapList RPNList    = null;

        try
        {
            com.matrixone.apps.program.Risk risk = (com.matrixone.apps.program.Risk) DomainObject
                .newInstance(context, DomainConstants.TYPE_RISK,
                    DomainConstants.PROGRAM);
            risk.setId(objectId);

            StringList relSelects = new StringList();
            relSelects.add(RiskRPNRelationship.SELECT_ID);
            relSelects.add(RiskRPNRelationship.SELECT_NAME);
            relSelects.add(RiskRPNRelationship.SELECT_RISK_IMPACT);
            relSelects.add(RiskRPNRelationship.SELECT_RISK_PROBABILITY);
            relSelects.add(RiskRPNRelationship.SELECT_EFFECTIVE_DATE);
            relSelects.add(RiskRPNRelationship.SELECT_STATUS);
            relSelects.add(RiskRPNRelationship.SELECT_ORIGINATOR);
            relSelects.add(RiskRPNRelationship.SELECT_ORIGINATED);

            StringList objectSelects = new StringList();
            objectSelects.add(Risk.SELECT_RPN_ID);
            objectSelects.add(Risk.SELECT_ID);
            objectSelects.add("from[" + RELATIONSHIP_RISK_RPN + "].to.name");
            RPNList = risk.getRelatedObjects(context, // context.
                    RELATIONSHIP_RISK_RPN, // relationship pattern
                    TYPE_RPN, // type filter.
                    objectSelects, // business object selectables.
                    relSelects, // relationship selectables.
                    false, // expand to direction.
                    true, // expand from direction.
                    (short) 1, // level
                    EMPTY_STRING, // object where clause
                    EMPTY_STRING); // relationship where clause
        }
        catch(Exception ex)
        {
            throw ex;
        }
        finally
        {
            return RPNList;
        }
    }

    /**
     * This method is used to show the status icon image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the status icon value as String.
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getStatusIcon(Context context, String[] args)throws Exception
    {
        Vector showIcon = new Vector();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
        try{
            HashMap programMap          = (HashMap) JPO.unpackArgs(args);
            MapList objectList          = (MapList) programMap.get("objectList");
            Map objectMap               = null;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr           = new String[objectList.size()];
            int arrayCount              = 0;

            while(objectListIterator.hasNext()){
                objectMap                = (Map) objectListIterator.next();
                objIdArr[arrayCount]     = (String) objectMap.get(DomainConstants.SELECT_ID);
                arrayCount++;
            }

            StringList busSelect = new StringList(2);
            busSelect.add(Risk.SELECT_RISK_RPN_VALUES);
            busSelect.add(Risk.SELECT_EFFECTIVE_DATES);

            MapList actionList = DomainObject.getInfo(context, objIdArr,busSelect);
            int actionListSize = 0;
            if(actionList != null)
                actionListSize = actionList.size();

            for(int i = 0; i < actionListSize; i++){
                String statusGif = "";
                objectMap = (Map) actionList.get(i);
                Object rpnValue = objectMap.get(Risk.SELECT_RISK_RPN_VALUES);
                Object rpnDate  = objectMap.get(Risk.SELECT_EFFECTIVE_DATES);

                if(rpnValue != null){
                    statusGif = "&nbsp;";
                    String currentRPNValue = null;
                    if((rpnValue instanceof String) == false){
                        // many rpns exist, determine last one based on date
                        java.util.List rpnDates  = (java.util.List) rpnDate;
                        java.util.List rpnValues = (java.util.List) rpnValue;
                        Date RPNDate = new Date(0, 0, 0, 0, 0, 0);
                        int index    = -1;
                        Iterator itr = rpnDates.iterator();

                        while(itr.hasNext()){
                            index++;
                            Date effectiveDate = sdf.parse((String) itr.next());
                            if(effectiveDate.after(RPNDate)){
                                currentRPNValue     = (String) rpnValues.get(index);
                                RPNDate = effectiveDate;
                            }
                        }
                    }
                    else
                        currentRPNValue = (String) rpnValue;

                    int RPN = (int) Task.parseToDouble(currentRPNValue);
                    int yellowThreshold = Integer.parseInt(FrameworkProperties
                            .getProperty(context,"eServiceApplicationProgramCentralRPNThreshold.Yellow"));
                    int redThreshold = Integer.parseInt(EnoviaResourceBundle.getProperty(context,
                    "eServiceApplicationProgramCentralRPNThreshold.Red"));
                    int maxRPNValue = Integer.parseInt(EnoviaResourceBundle.getProperty(context,
                    "eServiceApplicationProgramCentralRPNThreshold.Max"));

                    if((RPN >= 0) && (RPN < yellowThreshold))
                        statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\"/>";
                    else if((RPN >= yellowThreshold) && (RPN < redThreshold))
                        statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\"/>";
                    else if((RPN >= redThreshold) && (RPN <= maxRPNValue))
                        statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\"/>";
                    showIcon.add(statusGif);
                }
                else
                    showIcon.add(statusGif);
            }
        }
        catch(Exception ex){
            throw ex;
        }finally{
            return showIcon;
        }
    }


    /**
     * This method is used to show the slipdays.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the slipdays value as Long.
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getSlipdays(Context context, String[] args)
        throws Exception
    {
        Vector showSlipDays = new Vector();

        try
        {
            HashMap programMap             = (HashMap) JPO.unpackArgs(args);
            MapList objectList             = (MapList) programMap.get(
                    "objectList");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat
                    .getEMatrixDateFormat(), Locale.US);

            Map objectMap               = null;
            Date sysDate                = new Date();
            int arrayCount              = 0;
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr           = new String[objectList.size()];

            while(objectListIterator.hasNext())
            {
                objectMap                = (Map) objectListIterator.next();
                objIdArr[arrayCount]     = (String) objectMap.get(DomainConstants.SELECT_ID);
                arrayCount++;
            }

            StringList busSelect = new StringList(2);
            busSelect.add(Risk.SELECT_ACTUAL_END_DATE);
            busSelect.add(Risk.SELECT_ESTIMATED_END_DATE);

            MapList actionList = DomainObject.getInfo(context, objIdArr,
                    busSelect);

            int actionListSize = 0;

            if(actionList != null)
            {
                actionListSize = actionList.size();
            }

            for(int i = 0; i < actionListSize; i++)
            {
                objectMap = (Map) actionList.get(i);

                long slipDays         = (long) 0;
                long actualFinishDate = 0;
                Date actFinishedDate  = null;

                String actFinishDate = (String) objectMap.get(Risk.SELECT_ACTUAL_END_DATE);
                Date estFinishDate = sdf.parse((String) objectMap.get(
                            Risk.SELECT_ESTIMATED_END_DATE));

                slipDays = java.lang.Math.round(DateUtil.computeDuration(
                            estFinishDate, sysDate) - 1);
                showSlipDays.add("" + slipDays);
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        finally
        {
            return showSlipDays;
        }
    }


    /**
     * This method is used to gets the list of RPN Value.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectId   - String containing the projectID
     * @return Vector containing the RPN value as String.
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public static Vector getRPNValue(Context context, String[] args) throws Exception {
		StringList finalRPNValueList = new StringList();
        try {
            Map programMap          	= (HashMap) JPO.unpackArgs(args);
            MapList objectList          = (MapList) programMap.get("objectList");
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr           = new String[objectList.size()];
            int arrayCount              = 0;
            Map objectMap               = null;
			String originatedDate = DomainObject.EMPTY_STRING;
			String RPNValue = DomainObject.EMPTY_STRING;

            while(objectListIterator.hasNext()){
                objectMap                = (Map) objectListIterator.next();
                objIdArr[arrayCount]     = (String) objectMap.get(DomainConstants.SELECT_ID);
                arrayCount++;
            }

			Risk risk = new Risk();
			StringList busSelect = new StringList(2);
			busSelect.add(risk.SELECT_RISK_RPN_VALUES);
			busSelect.add(risk.SELECT_ORIGINATED_DATES);
			MapList riskInfoList = DomainObject.getInfo(context, objIdArr,busSelect);

			for (Iterator iterator = riskInfoList.iterator(); iterator.hasNext();) {
                objectMap = (Map) iterator.next();
                StringList slOriginatedDates = new StringList();
				String sOriginatedDates = (String)objectMap.get(SELECT_ORIGINATED_DATES);
				slOriginatedDates = FrameworkUtil.split(sOriginatedDates, "");
				String latestOriginated = risk.getLatestOrignated(slOriginatedDates);
				int latestOriginatedIndex =  slOriginatedDates.indexOf(latestOriginated);

                StringList slRPNs = new StringList();
				slRPNs = (StringList)objectMap.get(SELECT_RISK_RPN_VALUES);
                RPNValue = (String) slRPNs.get(latestOriginatedIndex);
                finalRPNValueList.add(RPNValue);
            }
            return finalRPNValueList;
        }
        catch(Exception ex) {
            throw ex;
        }
        }


    // This needs to return values not strings so they can be sorted
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static Vector<String> getRiskColumnRPN(Context context, String[] args)
            throws Exception
    {
        return emxRiskBase_mxJPO.getRiskColumn(context, args, "RPN");

    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public static Vector<String> getRiskColumnImpact(Context context, String[] args)
            throws Exception
    {
        return emxRiskBase_mxJPO.getRiskColumn(context, args, "IMPACT");
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public static Vector<String> getRiskColumnProbability(Context context, String[] args)
            throws Exception
    {
        return emxRiskBase_mxJPO.getRiskColumn(context, args, "PROBABILITY");
    }


    private static Vector<String> getRiskColumn(Context context, String[] args, String colName)
            throws Exception
        {
        final Vector<String> vResults = new Vector<String>();
        final Map programMap = (HashMap) JPO.unpackArgs(args);
        final MapList objectList = (MapList) programMap.get("objectList");
        final String[] objIdArr = new String[objectList.size()];
        int arrayCount = 0;

        String ATTRIBUTE = DomainConstants.EMPTY_STRING;
        final String RISK = PropertyUtil.getSchemaProperty(context, "type_Risk");
        final String RPN = PropertyUtil.getSchemaProperty(context, "type_RPN");
        String ATTR_IMPACT = DomainConstants.EMPTY_STRING;
        String ATTR_PROB = DomainConstants.EMPTY_STRING;

        // Get the proper attribute name
        String rangeKey = DomainConstants.EMPTY_STRING;
        switch (colName) {
        case "RPN":
            ATTRIBUTE = PropertyUtil.getSchemaProperty(context, "attribute_RiskRPNValue");
            ATTR_PROB = PropertyUtil.getSchemaProperty(context, "attribute_RiskProbability");
            ATTR_IMPACT = PropertyUtil.getSchemaProperty(context, "attribute_RiskImpact");
            break;
        case "PROBABILITY":
            ATTRIBUTE = PropertyUtil.getSchemaProperty(context, "attribute_RiskProbability");
            rangeKey = "emxFramework.Range.Risk_Probability.";
            break;
        case "IMPACT":
            ATTRIBUTE = PropertyUtil.getSchemaProperty(context, "attribute_RiskImpact");
            rangeKey = "emxFramework.Range.Risk_Impact.";
            break;

        default:
            break;
        }

//      final StringList selectList = new StringList();
//      final BusinessObjectWithSelectList data = BusinessObject.getSelectBusinessObjectData(context, objectList,selectList )

        // Loop through each object treating Risk and RPN separately
        try {

            for (final Object objectMap : objectList) {

                final Map riskMap = (Map) objectMap;
                objIdArr[arrayCount] = (String) riskMap.get(DomainConstants.SELECT_ID);
                arrayCount++;

                final String objId = (String) riskMap.get(DomainConstants.SELECT_ID);
                final String relId = (String) riskMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                final DomainObject risk = DomainObject.newInstance(context, objId);
                final String objectType = risk.getInfo(context, DomainConstants.SELECT_TYPE);

                String sValue = DomainConstants.EMPTY_STRING;

                // *************************************
                // calculate RPN - this is a real (not sure why), rest are int
                // that get translated
                if (RISK.equalsIgnoreCase(objectType)) {
                    if (colName.equalsIgnoreCase("RPN")) {
                        final String sProb = risk.getAttributeValue(context, ATTR_PROB);
                        final String sImpact = risk.getAttributeValue(context, ATTR_IMPACT);
                        if (UIUtil.isNotNullAndNotEmpty(sProb) && UIUtil.isNotNullAndNotEmpty(sImpact)) {
                            // final float fRPN = Float.parseFloat(sProb) *
                            // Float.parseFloat(sImpact);
                            final int iRPN = Integer.parseInt(sProb) * Integer.parseInt(sImpact);
                            // sValue = String.format("%2.1f", fRPN);
                            sValue = String.format("%5s", iRPN);
                        } else {
                            sValue = "0.0";
                        }
                    } else {
                        sValue = risk.getAttributeValue(context, ATTRIBUTE);
                    }
                }

                // Get the information from the Rel attributes
                if (RPN.equalsIgnoreCase(objectType)) {
                    sValue = DomainRelationship.getAttributeValue(context, relId, ATTRIBUTE);
                }
                // *************************************

                // COnvert the value to the NLS value if there should be one
                String sDisplayValue = DomainConstants.EMPTY_STRING;
                if (UIUtil.isNotNullAndNotEmpty(sValue)) {
                    if (UIUtil.isNotNullAndNotEmpty(rangeKey)) {
                        sDisplayValue = EnoviaResourceBundle.getProperty(context, "Framework", rangeKey + sValue,
                                context.getSession().getLanguage());
                    } else {
                        sDisplayValue = sValue;
                    }
                }
                vResults.add(sDisplayValue);
            }
        return vResults;

        } catch(final Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }


    /**
     * This method is used to gets the list of assignees/name as objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectId   - String containing the projectID
     * @return Vector containing the assignees/name value as String.
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public static Vector getAssignList(Context context, String[] args)
        throws Exception
    {
        Vector vAssignees              = new Vector();
        SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);

        try
        {
            HashMap programMap          = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap            = (HashMap) programMap.get("paramList");
            MapList objectList          = (MapList) programMap.get("objectList");
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr           = new String[objectList.size()];
            String exportFormat         = (String) paramMap.get("exportFormat");
            boolean isPrinterFriendly   = false;
            String strPrinterFriendly   = (String) paramMap.get("reportFormat");

            if(strPrinterFriendly != null)
            {
                isPrinterFriendly = true;
            }

            int arrayCount = 0;
            Map objectMap  = null;

            while(objectListIterator.hasNext())
            {
                objectMap                = (Map) objectListIterator.next();
                objIdArr[arrayCount]     = (String) objectMap.get(DomainConstants.SELECT_ID);
                arrayCount++;
            }

            StringList busSelect = new StringList(2);
            busSelect.add(Risk.SELECT_ASSIGNEES_LAST_NAME);
            busSelect.add(Risk.SELECT_ASSIGNEES_FIRST_NAME);

            MapList actionList = DomainObject.getInfo(context, objIdArr, busSelect);

            int actionListSize = 0;

            if(actionList != null)
            {
                actionListSize = actionList.size();
            }

            Date tempDate = new Date();
            Date sysDate  = new Date(tempDate.getYear(), tempDate.getMonth(), tempDate.getDate());

            for(int i = 0; i < actionListSize; i++)
            {
                objectMap = (Map) actionList.get(i);

                // find all assignees of the risk and get their real names.
                // names in the risk map are opposite of what they should be. the first name is acutally
                // the last name, and the lastname is actually the first
                Object assigneesFirstName = objectMap.get(Risk.SELECT_ASSIGNEES_LAST_NAME);
                Object assigneesLastName = objectMap.get(Risk.SELECT_ASSIGNEES_FIRST_NAME);

                MapList assigneeList = null;
                StringBuffer sb      = new StringBuffer(100);

                if(assigneesLastName != null)
                {
                    String assigneeName = "";

                    if(assigneesLastName instanceof String)
                    {
                        assigneeName = assigneesLastName + ", "
                            + assigneesFirstName;

                        if((exportFormat != null)
                                && (exportFormat.length() > 0)
                                && ("CSV".equals(exportFormat)))
                        {
                            vAssignees.add(XSSUtil.encodeForHTML(context,assigneeName));
                        }
                        else
                        {
                            sb.append("<select name=\"State\">");
                            sb.append("<option value=\"*\">");
                            sb.append(XSSUtil.encodeForHTML(context,assigneeName));
                            sb.append("</option>");
                            sb.append("</select>");
                            vAssignees.add(sb.toString());
                        }
                    }
                    else
                    {
                        java.util.List lastNames = (java.util.List) assigneesLastName;
                        java.util.List firstNames = (java.util.List) assigneesFirstName;
                        assigneeList = new MapList(lastNames.size());

                        if(((exportFormat != null)
                                && (exportFormat.length() > 0)
                                && ("CSV".equals(exportFormat)))
                                || (isPrinterFriendly == true))
                        {
                            //if(lastNames.size() > 0)
                            //Added:ixe:7-Sept-2010: IR-059361V6R2011x
                             for(int j = 0; j < lastNames.size(); j++)
                            {
                                assigneeName = lastNames.get(j) + ", "
                                    + firstNames.get(j);

                                if(sb.length()==0){
                                sb.append(XSSUtil.encodeForHTML(context,assigneeName));
                                }else{
                                    sb.append("; ");
                                    sb.append(XSSUtil.encodeForHTML(context,assigneeName));
                                }
                            }

                             vAssignees.add(sb.toString());
                        }
                        else
                        {
                            sb.append("<select name=\"State\">");

                            for(int j = 0; j < lastNames.size(); j++)
                            {
                                assigneeName = lastNames.get(j) + ", "
                                    + firstNames.get(j);
                                sb.append("<option value=\"*\">");
                                sb.append(XSSUtil.encodeForHTML(context,assigneeName));
                                sb.append("</option>");
                            }

                            //End - for loop
                            sb.append("</select>");
                            vAssignees.add(sb.toString());
                        }
                    }

                    //End - else
                }
                else
                {
                    assigneeList = new MapList(0);
                    vAssignees.add("");
                }
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vAssignees;
        }
    }


    /**
     * This method is used to get the list of affected items as objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectId   - String containing the projectID
     * @return Vector containing the aaffected items value as String.
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public static Vector getAffectedItem(Context context, String[] args)
        throws Exception
    {
        Vector vItems                  = new Vector();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);

        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            MapList objectList        = (MapList) programMap.get("objectList");
            HashMap paramMap          = (HashMap) programMap.get("paramList");
            String exportFormat       = (String) paramMap.get("exportFormat");
            boolean isPrinterFriendly = false;
            Map paramList             = (Map) programMap.get("paramList");
            String PrinterFriendly    = (String) paramList.get("reportFormat");

            if(PrinterFriendly != null)
            {
                isPrinterFriendly = true;
            }

            Map objectMap = null;

            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr           = new String[objectList.size()];
            int i                       = 0;

            while(objectListIterator.hasNext())
            {
                objectMap       = (Map) objectListIterator.next();
                objIdArr[i]     = (String) objectMap.get(DomainConstants.SELECT_ID);
                i++;
            }

            StringList busSelect = new StringList(2);
            busSelect.add(Risk.SELECT_RISK_ITEMS_ID);
            busSelect.add(Risk.SELECT_RISK_ITEMS_NAME);

            MapList actionList = DomainObject.getInfo(context, objIdArr,
                    busSelect);

            Iterator objectListItr = actionList.iterator();

            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();

                // determine the risk item.
                Object riskItemsId   = objectMap.get(Risk.SELECT_RISK_ITEMS_ID);
                Object riskItemsName = objectMap.get(Risk.SELECT_RISK_ITEMS_NAME);

                String itemId   = null;
                String itemName = null;

                if(riskItemsId != null)
                {
                    if((riskItemsId instanceof String) == false)
                    {
                        itemId     = (String) ((java.util.List) riskItemsId).get(0);
                        itemName = (String) ((java.util.List) riskItemsName).get(0);
                    }
                }

                if("#DENIED!".equals(itemId))
                {
                    objectMap.remove(Risk.SELECT_RISK_ITEMS_ID);
                }
                else if(itemId != null)
                {
                    objectMap.put(Risk.SELECT_RISK_ITEMS_ID, itemId);
                }

                if(itemName != null)
                {
                    objectMap.put(Risk.SELECT_RISK_ITEMS_NAME, itemName);
                }

                String taskName = (String) objectMap.get(Risk.SELECT_RISK_ITEMS_NAME);
                String taskId = (String) objectMap.get(Risk.SELECT_RISK_ITEMS_ID);


                StringBuffer display = new StringBuffer("");

                if(ProgramCentralUtil.isNotNullString(taskId)) {

                    if(((exportFormat != null) && (exportFormat.length() > 0) && ("CSV".equals(exportFormat))) || (isPrinterFriendly == true))
                    {
                        display.append(taskName);
                    }
                    else
                    {
                         taskName = XSSUtil.encodeForHTML(context,taskName);
                        display.append("<a href=\"").append("../common/emxTree.jsp?objectId=").append(XSSUtil.encodeForURL(context,taskId));
                        display.append("&amp;mode=replace&amp;jsTreeID=null&amp;AppendParameters=false").append("\">");
                        display.append(taskName).append("</a>");
                    }
                }

                vItems.add(display.toString());
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vItems;
        }
    }


    /**
     * This method is used to disable Checkbox of Risk when State is not Create
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - objectList contains a MapList of Maps which contains objects.
     * @return Vector containing the risk items value as String.
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector showRiskCheckbox(Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");

        Vector enableCheckbox = new Vector();

        try
        {
            com.matrixone.apps.program.Risk risk = (com.matrixone.apps.program.Risk) DomainObject
                .newInstance(context, DomainConstants.TYPE_RISK,
                    DomainConstants.PROGRAM);

            String strRiskStateCreate = FrameworkUtil.lookupStateName(context,
                    Risk.POLICY_PROJECT_RISK, "state_Create");

            Iterator objectListItr = objectList.iterator();

            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String riskId = (String) objectMap.get(DomainConstants.SELECT_ID);

                if((riskId != null) && !riskId.equals("null")
                        && !riskId.equals(""))
                {
                    risk.setId(riskId);

                    String riskState = risk.getInfo(context, DomainConstants.SELECT_CURRENT)
                                           .toString();

                    if((riskState != null) && !riskState.equals("null")
                            && !riskState.equals("")
                            && riskState.equals(strRiskStateCreate))
                    {
                        enableCheckbox.add("true");
                    }
                    else
                    {
                        enableCheckbox.add("false");
                    }
                }
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        finally
        {
            return enableCheckbox;
        }
    }


    /**
     * Gets the Slip Days Count.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: paramMap - a
     *            HashMap containing the following keys, "objectId".
     * @return Object - boolean true if the operation is successful
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public String getSlipdaysCount(Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) requestMap.get("objectId");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);

        StringList busSelect = new StringList(1);
        busSelect.add(Risk.SELECT_ESTIMATED_END_DATE);

        DomainObject dom = DomainObject.newInstance(context, strObjectId);
        Map objectMap    = dom.getInfo(context, busSelect);

        Date sysDate       = new Date();
        Date estFinishDate = sdf.parse((String) objectMap.get(
                    Risk.SELECT_ESTIMATED_END_DATE));

        long slipDays = java.lang.Math.round(DateUtil.computeDuration(
                    estFinishDate, sysDate) - 1);

        return slipDays + "";
    }


    /**
     * Returns TRUE if context user is Task Assignee or Project DPM Member, else returns FALSE.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries:
     *            programMap - a HashMap containing the following keys,
     *            "objectId".
     * @return - boolean true if the user has Access
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public boolean checkEditAccess(Context context, String[] args)throws Exception {

        boolean editFlag    = false;
        ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
		Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
        try{
        	HashMap programMap  = (HashMap) JPO.unpackArgs(args);
            String objectId     = (String) programMap.get("parentOID");
            if(ProgramCentralUtil.isNullString(objectId)){
                objectId        = (String) programMap.get("objectId");
            }
            DomainObject domObj	= DomainObject.newInstance(context, objectId);
            editFlag            = domObj.checkAccess(context,(short) AccessConstants.cModify);

            StringList selects = new StringList(DomainObject.SELECT_CURRENT);
            selects.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
            selects.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
            selects.add(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);

            Map objInfoMap = domObj.getInfo(context, selects);
            String state = (String) objInfoMap.get(DomainObject.SELECT_CURRENT);
            String isKindOfProjectSpace = (String) objInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
            String isKindOfTaskMgmt = (String) objInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
            String taskParentProjectId = (String) objInfoMap.get(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);

            if(ProgramCentralConstants.STATE_PROJECT_SPACE_COMPLETE.equalsIgnoreCase(state)){
                editFlag = false;
            }

            if(ProgramCentralUtil.isNotNullString(taskParentProjectId)){ //When Risk is in context of Task
                project.setId(taskParentProjectId);
            } else {
                project.setId(objectId);
            }
            task.setId(objectId);
            boolean isCtxUserProjectMember = project.isCtxUserProjectMember(context);
            boolean ctxUserHasDPMLicense = ProgramCentralUtil.hasDPMLicense(context);
            boolean isCtxUserTaskAssignee = task.isCtxUserTaskAssignee(context);
            if(Boolean.valueOf(isKindOfProjectSpace) && editFlag){//When Risk is in context of Project.
                editFlag = (isCtxUserProjectMember && ctxUserHasDPMLicense);//Only Project DPM members allowed.
            } else if(Boolean.valueOf(isKindOfTaskMgmt) && editFlag){ //When Risk is in context of Task, Task Assignees or Project DPM members are allowed.
                editFlag = (isCtxUserTaskAssignee || (isCtxUserProjectMember && ctxUserHasDPMLicense));
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            return editFlag;
        }
    }

    /**
     * Called from categories of Risk itself.
     *
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean hasAccessOnRisk(Context context, String[] args) throws Exception {

    	HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        String objectId     = (String) programMap.get("parentOID");
        if(ProgramCentralUtil.isNullString(objectId)){
            objectId        = (String) programMap.get("objectId");
        }
    	String isKindOfRisk = "type.kindof["+DomainObject.TYPE_RISK+"]";
    	String isKindOfTask = "type.kindof["+DomainObject.TYPE_TASK_MANAGEMENT+"]";
    	StringList busSelectList = new StringList(2);
        busSelectList.add(isKindOfRisk);
        busSelectList.add(isKindOfTask);
    	DomainObject Obj = DomainObject.newInstance(context, objectId);
    	Map infoMap = Obj.getInfo(context, busSelectList);

        isKindOfRisk = (String)infoMap.get(isKindOfRisk);
        isKindOfTask = (String)infoMap.get(isKindOfTask);

        if("True".equalsIgnoreCase(isKindOfRisk)){
    	Risk risk = new Risk(objectId);
        return risk.hasAccessOnRisk(context, args);
    }
        if("True".equalsIgnoreCase(isKindOfTask)){
            return true;
        }
        return false;
    }


    /**
     * gets the value for field Probability on Create Risk Form
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries:
     *            programMap - a HashMap containing the following keys,
     *            "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public String getProbability(Context context, String[] args) throws Exception {
        final StringBuffer options = new StringBuffer();

        try {
            final String attrProbability = PropertyUtil.getSchemaProperty(context, "attribute_RiskProbability");
            final StringList probabilitytRanges = FrameworkUtil.getRanges(context, attrProbability);
            final int length = probabilitytRanges.size();

            final String rangeKey = "emxFramework.Range.Risk_Probability.";

            options.append("<select name='Probability' onChange='modifyRPNValueForRisk()'>");

            // NX5 - FUN071553
            for (int i = 0; i < length; i++) {
                final String rangeValue = (String) probabilitytRanges.get(i);
                final String convertedProbabilityRange = EnoviaResourceBundle.getProperty(context, "Framework",rangeKey + rangeValue, context.getLocale());
                options.append(" <option value=\"" + FrameworkUtil.encodeURL(rangeValue) + "\"");
                options.append(">");
                options.append(convertedProbabilityRange + "</option> ");
            }
            options.append("</select>");
            return options.toString();

        } catch (final Exception ex) {
            ex.printStackTrace();
            System.out.println(options.toString());
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    /**
     * gets the value for field Impact on Create Risk Form
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries:
     *            programMap - a HashMap containing the following keys,
     *            "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public String getImpact(Context context, String[] args)
        throws Exception
    {
        final StringBuffer options = new StringBuffer();

        try {
            final String attrImpact = PropertyUtil.getSchemaProperty(context, "attribute_RiskImpact");
            final StringList impactRanges = FrameworkUtil.getRanges(context, attrImpact);
            final int length = impactRanges.size();

            final String rangeKey = "emxFramework.Range.Risk_Impact.";

            options.append("<select name='Impact' onChange='modifyRPNValueForRisk()'>");

            // NX5 - FUN071553
            for (int i = 0; i < length; i++) {
                final String rangeValue = (String) impactRanges.get(i);
                final String convertedImpactRange = EnoviaResourceBundle.getProperty(context, "Framework",rangeKey + rangeValue, context.getLocale());
                options.append(" <option value=\"" + FrameworkUtil.encodeURL(rangeValue) + "\"");
                options.append(">");
                options.append(convertedImpactRange + "</option> ");
            }
            options.append("</select>");
            return options.toString();

        } catch (final Exception ex) {
            ex.printStackTrace();
            System.out.println(options.toString());
            System.out.println(ex.getMessage());
            throw ex;
        }
    }


    /**
     * gets the value for field RPN on Create Risk Form
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries:
     *            programMap - a HashMap containing the following keys,
     *            "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public String getRPN(Context context, String[] args)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append(
            "<input name='RPN' size='2' value='1' maxlength='2' readonly='readonly'/>");

        String textbox = sb.toString();

        return textbox;
    }


    /**
     * gets the value for field EffectiveDate on Create Risk Form
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries:
     *            programMap - a HashMap containing the following keys,
     *            "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public String getEffectiveDate(Context context, String[] args)
        throws Exception
    {
        Date date                      = new Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);
        String effectiveDate           = sdf.format(date);
        HashMap programMap             = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap               = (HashMap) programMap.get("paramMap");
        HashMap requestMap             = (HashMap) programMap.get("requestMap");
        String timezone                = (String) requestMap.get("timeZone");
        double dbTimeZone              = Double.parseDouble(timezone);
//Added:24-Apr-09:nr2:R207:PRG Bug :373419
        Locale locale                  = (Locale) requestMap.get("localeObj");
        effectiveDate                  = eMatrixDateFormat
                                         .getFormattedDisplayDate(effectiveDate, dbTimeZone,locale);
//End:R207:PRG Bug :373419
/*
        effectiveDate                  = eMatrixDateFormat
            .getFormattedDisplayDate(effectiveDate, dbTimeZone,
                Locale.getDefault());
*/

        StringBuffer sb                = new StringBuffer();
        sb.append(
            "<script language=\"javascript\" type=\"text/javascript\" src=\"../common/scripts/emxUICalendar.js\"></script>");
        sb.append(
            "<input type=\"text\" name=\"EffectiveDate\" size=\"20\" value=\""
            + XSSUtil.encodeForXML(context,effectiveDate) + "\" readonly=\"readonly\"/>");
        sb.append(
            "<a id=\"formDateChooser\" href=\"javascript:showCalendar('emxCreateForm','EffectiveDate','')\" >");
        sb.append(
            "<img src=\"../common/images/iconSmallCalendar.gif\" border=\"0\" valign=\"absmiddle\" name=\"img5\"/>");
        sb.append("</a>");
        effectiveDate = sb.toString();

        return effectiveDate;
    }


    /**
     * gets the value for field Status on Create Risk Form
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries:
     *            programMap - a HashMap containing the following keys,
     *            "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public String getStatus(Context context, String[] args)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<textarea rows='5' name='Status' cols='45'></textarea>");

        String textarea = sb.toString();

        return textarea;
    }


    /**
     * Modifies the Risk object name and connects it to Parent.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries:
     *            programMap - a HashMap containing the following keys,
     *            "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void createRisk(Context context, String[] args) throws Exception {
    	Map programMap    = (Map) JPO.unpackArgs(args);
    	Risk risk = new Risk();
    	Risk riskObject = risk.createRisk(context, programMap);
    }


    /**
     * This method is used to disable Checkbox of Risk when State is not Create
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - objectList contains a MapList of Maps which contains objects.
     * @return Vector containing the risk items value as String.
     * @throws Exception if the operation fails
     * @since PMC X+2
     */
    public Object getRPNName(Context context, String[] args)
        throws Exception
    {
        HashMap programMap                   = (HashMap) JPO.unpackArgs(args);
        Map paramList                        = (Map) programMap.get("paramList");
        String strLanguage                   = (String) paramList.get("languageStr");
        String objectId                      = (String) paramList.get("objectId");
        String jsTreeID                      = (String) paramList.get("jsTreeID");
        com.matrixone.apps.program.Risk risk = (com.matrixone.apps.program.Risk) DomainObject
            .newInstance(context, DomainConstants.TYPE_RISK,
                DomainConstants.PROGRAM);
        String key = "emxFramework.Relationship.Risk_RPN";
        //Added for Bug#338897 - Start
        boolean isPrinterFriendly = false;
        String strPrinterFriendly = (String)paramList.get("reportFormat");
        if ( strPrinterFriendly != null ) {
            isPrinterFriendly = true;
        }
        //Added for Bug#338897 - End

        MapList objectList = (MapList) programMap.get("objectList");
        Vector rpnName     = new Vector();
        String latestId    = "";
        boolean latest     = true;

        try
        {
            risk.setId(objectId);
            objectList.sort(RiskRPNRelationship.SELECT_ORIGINATED,
                "descending", "date");

            Map tempMap = (Map) objectList.get(0);
            latestId = (String) tempMap.get(RiskRPNRelationship.SELECT_ID);

            for(int i = 0; i < objectList.size(); i++)
            {
                Map objectMap = (Map) objectList.get(i);
                String riskRPNrelId = (String)objectMap.get(RiskRPNRelationship.SELECT_ID);
                String name = (String)objectMap.get(RiskRPNRelationship.SELECT_NAME);
                String convertedName =  EnoviaResourceBundle.getProperty(context, "Framework",
                        key, strLanguage);
                if(latestId.equals(
                            (String) objectMap.get(
                                RiskRPNRelationship.SELECT_ID)))
                {
                    latest = true;
                }
                else
                {
                    latest = false;
                }

                if(!latest)
                {
                    //Modified:07-Mar-2011:hp5:R211:PRG:IR-098667V6R2012
                    rpnName.add(XSSUtil.encodeForHTML(context,convertedName));
                    //End:07-Mar-2011:hp5:R211:PRG:IR-098667V6R2012
                }
                else
                {
                    //Added for Bug#338897 - Start
                    if(!isPrinterFriendly)
                    {
                        //Modified:07-Mar-2011:hp5:R211:PRG:IR-098667V6R2012
                    String nextURL = "../common/emxTree.jsp?objectId="
                        + risk.getInfo(context, Risk.SELECT_RPN_ID)
                        + "&amp;mode=insert&amp;jsTreeID=" + jsTreeID
                        + "&amp;AppendParameters=true" + "&amp;rpnId="
                        + riskRPNrelId
                        + "&amp;riskId=" + objectId;
                    rpnName.add("<a href='" + nextURL
                        + "' target='content' onclick='var tree = top.objDetailsTree;tree.deleteObject(\""
                        + risk.getInfo(context, Risk.SELECT_RPN_ID)
                        + "\",true);'>"
                        + XSSUtil.encodeForHTML(context,convertedName)
                        + "</a>");
                    }
                    else
                    {
                        rpnName.add(XSSUtil.encodeForHTML(context,convertedName));
                        //End:07-Mar-2011:hp5:R211:PRG:IR-098667V6R2012
                    }
                    //Added for Bug#338897 - End
                }
            }

        }
        catch(Exception ex)
        {
            throw ex;
        }
        finally
        {
            return rpnName;
        }
    }


    /**
     * Gets the RPN Name.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: programMap - a
     *            HashMap containing the following keys, "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public boolean showField(Context context, String[] args)
        throws Exception
    {
        boolean showFields = false;
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String mode        = (String) programMap.get("mode");

        if("view".equalsIgnoreCase(mode))
        {
            showFields = true;
        }

        return showFields;
    }


    /**
     * Checks whether owner field to be displayed or not
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: programMap - a
     *            HashMap containing the following keys, "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public boolean showOwner(Context context, String[] args)
        throws Exception
    {
        boolean showFields = true;
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String mode        = (String) programMap.get("mode");

        if("view".equalsIgnoreCase(mode))
        {
            showFields = false;
        }

        return showFields;
    }


    /**
     * Gets the Owner.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: programMap - a
     *            HashMap containing the following keys, "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public String getOwner(Context context, String[] args)
        throws Exception
    {
        String output                           = "";
        HashMap programMap                      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap                        = (HashMap) programMap.get(
                "paramMap");
        String objectId                         = (String) paramMap.get(
                "objectId");
        HashMap requestMap                      = (HashMap) programMap.get(
                "requestMap");
        String strMode                          = (String) requestMap.get(
                "mode");
        String jsTreeID                         = (String) requestMap.get(
                "jsTreeID");
        com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject
            .newInstance(context, DomainConstants.TYPE_PERSON);
        com.matrixone.apps.program.Risk risk = (com.matrixone.apps.program.Risk) DomainObject
            .newInstance(context, DomainConstants.TYPE_RISK,
                DomainConstants.PROGRAM);
        risk.setId(objectId);

        StringList busSelects = new StringList(2);
        busSelects.add(DomainConstants.SELECT_OWNER);

        Map riskMap      = risk.getInfo(context, busSelects);
        String riskOwner = (String) riskMap.get(DomainConstants.SELECT_OWNER);
        String ownerId   = person.getPerson(context, riskOwner).getId();
        StringBuffer sb  = new StringBuffer();

        if(strMode.equals("edit"))
        {
            String userName = risk.getInfo(context, DomainConstants.SELECT_OWNER);
            person = person.getPerson(context, userName);
            busSelects.clear();
            busSelects.add(Person.SELECT_LAST_NAME);
            busSelects.add(Person.SELECT_FIRST_NAME);
          //Added:09-June-2010:vm3:R210 PRG:2011x
            Map personFullNameMap = person.getInfo(context, busSelects);
            String strLastName    = (String) personFullNameMap.get(Person.SELECT_LAST_NAME);
            String strFirstName   = (String) personFullNameMap.get(Person.SELECT_FIRST_NAME);
            String personName     = strLastName + ", " + strFirstName;
            sb.append(
                "<input type=\"text\" name=\"PersonName\" size=\"36\" value=\""
                + personName + "\" readonly=\"readonly\"/>");
            sb.append("<input type=\"hidden\" name=\"Owner\" value=\""
                + userName + "\"/>");
            sb.append(
                //"<input type=\"button\" name=\"bType\" size=\"200\" value=\"...\" alt=\"\" onClick=\"performPersonSearch();\"");
        //"<input type=\"button\" name=\"bType\" size=\"200\" value=\"...\" alt=\"\" onClick=\"javascript:showModalDialog('../common/emxFullSearch.jsp?field=TYPES=type_Person&amp;table=PMCCommonPersonSearchTable&amp;form=PMCCommonPersonSearchForm&amp;selection=multiple&amp;objectId="+objectId+ "&amp;submitURL=../programcentral/emxProgramCentralCommonPersonSearchUtil.jsp&amp;mode=addRiskAssignee')\"");
                    "<input type=\"button\" name=\"bType\" size=\"200\" value=\"...\" alt=\"\" onClick=\"javascript:chooseRiskOwner()\"/>");
                    //outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ApprovalListOID&fieldNameDisplay=ApprovalListDisplay&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
            output = sb.toString();
          //End:Added:09-June-2010:vm3:R210 PRG:2011x
        }

        return output;
    }


    /**
     * Update Owner
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: programMap - a
     *            HashMap containing the following keys, "objectId".
     * @return - String
     * @throws Exception
     *             if operation fails
     * @since PMC V6R2008-1
     */
    public boolean updateOwner(Context context, String[] args)
        throws Exception
    {
        HashMap programMap                   = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap                     = (HashMap) programMap.get(
                "paramMap");
        HashMap requestMap                   = (HashMap) programMap.get(
                "requestMap");
        String[] personArray                 = (String[]) requestMap.get(
                "Owner");
     // [Modified::Jan 19, 2011:S4E:R211:TypeAhead::Start]
        String personName                    = (String)paramMap.get("New Value");
    // [Modified::Jan 19, 2011:S4E:R211:TypeAhead::End]
        String objectId                      = (String) paramMap.get("objectId");
        com.matrixone.apps.program.Risk risk = (com.matrixone.apps.program.Risk) DomainObject
            .newInstance(context, DomainConstants.TYPE_RISK,
                DomainConstants.PROGRAM);
        risk.setId(objectId);
        risk.setOwner(context, personName);

        return true;
    }



    //Added:6-Mar-09:wqy:R207:PRG Bug :37046
    /**
     * This method is used to get the Policy for the Risk in Edit Form.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @returns StringList - Policy list
     * @throws Exception
     */
    public Map getPolicy(Context context, String[] args)
    throws Exception
    {
        Map mapPolicy = new HashMap();
        String strOuput="";
        StringBuffer output = new StringBuffer();
        StringList vecRange=new StringList();
        StringList vecRangeDisplay = new StringList();
        String sLanguage = context.getSession().getLanguage();

        try{
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap)programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");
            DomainObject dom=DomainObject.newInstance(context, objectId);
            StringList slSelectList = new StringList();
            slSelectList.add(DomainConstants.SELECT_TYPE);
            slSelectList.add(DomainConstants.SELECT_CURRENT);
            slSelectList.add(DomainConstants.SELECT_POLICY);
            Map mapGetInfo=dom.getInfo(context,slSelectList);
            String strStateComplete = PropertyUtil.getSchemaProperty(context,"policy", (String)mapGetInfo.get(DomainConstants.SELECT_POLICY), "state_Complete");
            if(strStateComplete.equals((String)mapGetInfo.get(DomainConstants.SELECT_CURRENT)))
            {
                vecRange.add((String)mapGetInfo.get(DomainConstants.SELECT_POLICY));
                vecRangeDisplay.add(i18nNow.getAdminI18NString("Policy",(String) mapGetInfo.get(DomainConstants.SELECT_POLICY), sLanguage));
            }
            else
            {
                MapList policyList =mxType.getPolicies(context, (String)mapGetInfo.get(DomainConstants.SELECT_TYPE), true);
                Iterator itr = policyList.iterator();
                while (itr.hasNext())
                {
                    Map policyMap = (Map) itr.next();
                    String strPolicy=(String)policyMap.get("name");
                    vecRange.add(strPolicy);
                    vecRangeDisplay.add(i18nNow.getAdminI18NString("Policy", strPolicy, sLanguage));
                }
            }
            mapPolicy.put("field_choices", vecRange);
            mapPolicy.put("field_display_choices", vecRangeDisplay);
        }catch(Exception e)
        {
            throw (e);
        }
        return mapPolicy;
    }
    //End:R207:PRG Bug :370465


    //Added:24-Feb-2011:hp5:R211:PRG:IR-030875V6R2012
    //This method is used when we edit start & end date of risk
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void updateFinishDate(Context context, String[] args)
    throws Exception
    {
        HashMap programMap    = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap      = (HashMap) programMap.get("paramMap");
        HashMap requestMap    = (HashMap) programMap.get("requestMap");
        String timeZone       = (String) requestMap.get("timeZone");
        String strEstimatedEndDate  = (String) requestMap.get("EstimatedEndDate");
        String strEstimatedStartDate  = (String) requestMap.get("EstimatedStartDate");
        double clientTZOffset = new Double(timeZone).doubleValue();
        String strLanguage       = (String) requestMap.get("languageStr");
        Locale locale         = (Locale) requestMap.get("localeObj");

        strEstimatedEndDate = eMatrixDateFormat.getFormattedInputDate(context, strEstimatedEndDate, clientTZOffset, locale);
        strEstimatedStartDate = eMatrixDateFormat.getFormattedInputDate(context, strEstimatedStartDate, clientTZOffset, locale);

        Date dtEstimatedEndDate = eMatrixDateFormat.getJavaDate(strEstimatedEndDate);
        Date dtEstimatedStartDate = eMatrixDateFormat.getJavaDate(strEstimatedStartDate);

        if(dtEstimatedEndDate.before(dtEstimatedStartDate))
        {
            String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    "emxProgramCentral.Import.EndDateBeforeStartDate", strLanguage);
            throw new MatrixException(sErrMsg);
        }

      }
    //End:24-Feb-2011:hp5:R211:PRG:IR-030875V6R2012

    public void triggerSendRiskAssignNotificationEmail(Context context, String[] args) throws Exception {

        String baseURL      =   emxMailUtilBase_mxJPO.getBaseURL(context, args);
        String objectId     =   args[0];
        String fromState    =   args[1];
        String toState      =   args[2];
        i18nNow i18n        =   new i18nNow();
        StringList mailCcList   =   null;

        if (ProgramCentralUtil.isNotNullString(objectId)) {
            if (ProgramCentralUtil.isNotNullString(toState) && STATE_PROJECT_RISK_ASSIGN.equalsIgnoreCase(toState)) {
                Risk riskObject =   (Risk)DomainObject.newInstance(context,ProgramCentralConstants.TYPE_RISK,ProgramCentralConstants.PROGRAM);
                riskObject.setId(objectId);

                StringList selectList   =   new StringList();
                selectList.add(ProgramCentralConstants.SELECT_ID);
                selectList.add(ProgramCentralConstants.SELECT_NAME);

                MapList riskAssigneeMapList =   riskObject.getAssignees(context, selectList, null, null, null);

                if (riskAssigneeMapList != null && !riskAssigneeMapList.isEmpty()) {


                    Map projectInfoMap  =   riskObject.getProjectInfo(context,selectList);
                    String projectId    =   (String)projectInfoMap.get(ProgramCentralConstants.SELECT_ID);
                    String projectName  =   (String)projectInfoMap.get(ProgramCentralConstants.SELECT_NAME);

                    Map<String,Boolean> projectAssigneeMap  =   getProjectAssigneeMap(context,projectId,riskAssigneeMapList);

                    String[] messageValues  =   new String[2];
                    messageValues[0]        =   riskObject.getName();
                    messageValues[1]        =   projectName;
                    Locale locale           =   new Locale(context.getSession().getLanguage());
                    String emailSubject     =   i18n.GetString(ProgramCentralConstants.RESOURCE_BUNDLE,
                                                               context.getSession().getLanguage(),
                                                               "emxProgramCentral.RiskAssignment.Subject");
                    String emailMessage     =   MessageUtil.getMessage(context,null,"emxProgramCentral.RiskAssignment.BodyMessage",
                                                                       messageValues,null,locale,
                                                                       ProgramCentralConstants.RESOURCE_BUNDLE);

                    Iterator<String> iterator   =   projectAssigneeMap.keySet().iterator();

                    while(iterator.hasNext()) {
                        StringList  objectList  =   new StringList();
                        StringList mailToList   =   new StringList();
                        String assigneeName =   iterator.next();
                        //If assinee is project member, add risk id into objectList to form the hyperLink.
                        if (projectAssigneeMap.get(assigneeName)) {
                            objectList.add(objectId);
                        }
                        mailToList.add(assigneeName);
                        MailUtil.sendMessage(context,mailToList,mailCcList,null,emailSubject,emailMessage,objectList);
                    }
                }
            }
        }
    }
    private Map getProjectAssigneeMap(Context context,String projectId,MapList riskAssigneeMapList) throws MatrixException {
        Map<String,Boolean> projectAssigneeMap  =   new HashMap();
        String mqlString        =   "print bus $1 select $2 dump $3";
        String memberIdString   =   MqlUtil.mqlCommand(context,mqlString,projectId,ProgramCentralConstants.SELECT_MEMBER_ID,"|");

        List<String> projectMemberList = FrameworkUtil.split(memberIdString,"|");

        for(int i=0;i<riskAssigneeMapList.size();i++) {
            boolean isMember    =   false;
            Map riskAssigneeMap =   (Map)riskAssigneeMapList.get(i);
            String assigneeId   =   (String)riskAssigneeMap.get(ProgramCentralConstants.SELECT_ID);
            String assigneeName =   (String)riskAssigneeMap.get(ProgramCentralConstants.SELECT_NAME);

            if (projectMemberList.contains(assigneeId)) {
                isMember    =   true;
            }
            projectAssigneeMap.put(assigneeName,isMember);
        }
        return projectAssigneeMap;
    }

    /**
     * Update title of risk object.
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @throws Exception if operation fails.
     */
    public void updateRiskTitle(Context context,String[]args)throws Exception
    {
    	com.matrixone.apps.program.Risk risk =
                  (com.matrixone.apps.program.Risk) DomainObject.newInstance(context,
                  DomainConstants.TYPE_RISK, "PROGRAM");

    	Map inputMap = JPO.unpackArgs(args);
		Map paramMap = (Map) inputMap.get("paramMap");
		Map requestMap = (Map) inputMap.get("requestMap");
		String objectId = (String) paramMap.get("objectId");
		String newAttrValue = (String) paramMap.get("New Value");

        if(ProgramCentralUtil.isNotNullString(objectId) &&
                ProgramCentralUtil.isNotNullString(newAttrValue)){
            risk.setId(objectId);
			risk.setAttributeValue(context, risk.ATTRIBUTE_TITLE, newAttrValue);
        }
    }

    /**
     * Update estimated start date of risk object.
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @throws Exception if operation fails.
     */
    public void updateRiskEstStartDate(Context context,String[]args)throws Exception
    {
    	com.matrixone.apps.program.Risk risk =
                (com.matrixone.apps.program.Risk) DomainObject.newInstance(context,
                        DomainConstants.TYPE_RISK, "PROGRAM");

    	Map inputMap = JPO.unpackArgs(args);
    	Map paramMap = (Map) inputMap.get("paramMap");
    	Map requestMap = (Map) inputMap.get("requestMap");

    	String objectId = (String) paramMap.get("objectId");
    	String newAttrValue = (String) paramMap.get("New Value");

    	String timeZone       = (String) requestMap.get("timeZone");
        double clientTZOffset = new Double(timeZone).doubleValue();
        Locale locale = context.getLocale();

        if(ProgramCentralUtil.isNotNullString(objectId) &&
                ProgramCentralUtil.isNotNullString(newAttrValue)){
            risk.setId(objectId);
    		String riskEstEndDate = risk.getInfo(context,SELECT_RISK_ESTIMATED_END_DATE);
    		String riskEstStartDate = eMatrixDateFormat.getFormattedInputDate(context,newAttrValue,clientTZOffset,locale);

    		Date dtEstimatedEndDate = eMatrixDateFormat.getJavaDate(riskEstEndDate);
            Date dtEstimatedStartDate = eMatrixDateFormat.getJavaDate(riskEstStartDate);

            if(dtEstimatedEndDate.before(dtEstimatedStartDate)){
                String sErrMsg = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,
                        "emxProgramCentral.Import.EndDateBeforeStartDate", context.getSession().getLanguage());
                throw new MatrixException(sErrMsg);
            }else{
            	risk.setAttributeValue(context, risk.ATTRIBUTE_ESTIMATED_START_DATE, riskEstStartDate);
            }
        }
    }

    /**
     * Update estimated end date of risk object.
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @throws Exception if operation fails.
     */
    public void updateRiskEstEndDate(Context context,String[]args)throws Exception
    {
    	com.matrixone.apps.program.Risk risk =
                (com.matrixone.apps.program.Risk) DomainObject.newInstance(context,
                        DomainConstants.TYPE_RISK, "PROGRAM");

    	Map inputMap = JPO.unpackArgs(args);
    	Map paramMap = (Map) inputMap.get("paramMap");
    	Map requestMap = (Map) inputMap.get("requestMap");

    	String objectId = (String) paramMap.get("objectId");
    	String newAttrValue = (String) paramMap.get("New Value");

    	String timeZone       = (String) requestMap.get("timeZone");
        double clientTZOffset = new Double(timeZone).doubleValue();
        Locale locale = context.getLocale();

        if(ProgramCentralUtil.isNotNullString(objectId) &&
                ProgramCentralUtil.isNotNullString(newAttrValue)){
            risk.setId(objectId);
    		String riskEstStartDate = risk.getInfo(context,SELECT_RISK_ESTIMATED_START_DATE);
    		String riskEstEndDate = eMatrixDateFormat.getFormattedInputDate(context,newAttrValue,clientTZOffset,locale);

    		Date dtEstimatedEndDate = eMatrixDateFormat.getJavaDate(riskEstEndDate);
            Date dtEstimatedStartDate = eMatrixDateFormat.getJavaDate(riskEstStartDate);

            if(dtEstimatedEndDate.before(dtEstimatedStartDate)){
                String sErrMsg = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,
                        "emxProgramCentral.Import.EndDateBeforeStartDate", context.getSession().getLanguage());
                throw new MatrixException(sErrMsg);
            }else{
            	risk.setAttributeValue(context, risk.ATTRIBUTE_ESTIMATED_END_DATE, riskEstEndDate);
            }
        }
    }

    /**
     * Update actual start date of risk object.
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @throws Exception if operation fails.
     */
    public void updateRiskActStartDate(Context context,String[]args)throws Exception
    {
    	com.matrixone.apps.program.Risk risk =
                (com.matrixone.apps.program.Risk) DomainObject.newInstance(context,
                        DomainConstants.TYPE_RISK, "PROGRAM");

    	Map inputMap = JPO.unpackArgs(args);
    	Map paramMap = (Map) inputMap.get("paramMap");
    	Map requestMap = (Map) inputMap.get("requestMap");

    	String objectId = (String) paramMap.get("objectId");
    	String newAttrValue = (String) paramMap.get("New Value");

    	String timeZone       = (String) requestMap.get("timeZone");
        double clientTZOffset = new Double(timeZone).doubleValue();
        Locale locale = context.getLocale();

        if(ProgramCentralUtil.isNotNullString(objectId) &&
                ProgramCentralUtil.isNotNullString(newAttrValue)){
            risk.setId(objectId);
    		String riskActStartDate = eMatrixDateFormat.getFormattedInputDate(context,newAttrValue,clientTZOffset,locale);
    		String riskActEndDate = risk.getInfo(context,SELECT_RISK_ACTUAL_END_DATE);

            if(ProgramCentralUtil.isNullString(riskActEndDate)){
                risk.setState(context, "Active");
    			risk.setAttributeValue(context, risk.ATTRIBUTE_ACTUAL_START_DATE, riskActStartDate);
            }else{
    			Date dtEstimatedEndDate = eMatrixDateFormat.getJavaDate(riskActEndDate);
    			Date dtEstimatedStartDate = eMatrixDateFormat.getJavaDate(riskActStartDate);

                if(dtEstimatedEndDate.before(dtEstimatedStartDate)){
    				String sErrMsg = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,
                            "emxProgramCentral.Import.EndDateBeforeStartDate", context.getSession().getLanguage());
                    throw new MatrixException(sErrMsg);
                }else{
    				risk.setAttributeValue(context, risk.ATTRIBUTE_ACTUAL_START_DATE, riskActStartDate);
                }
            }
        }
    }

    /**
     * Update actual end date of risk object.
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @throws Exception if operation fails.
     */
    public void updateRiskActEndDate(Context context,String[]args)throws Exception
    {
    	com.matrixone.apps.program.Risk risk =
                (com.matrixone.apps.program.Risk) DomainObject.newInstance(context,
                        DomainConstants.TYPE_RISK, "PROGRAM");

    	Map inputMap = JPO.unpackArgs(args);
    	Map paramMap = (Map) inputMap.get("paramMap");
    	Map requestMap = (Map) inputMap.get("requestMap");

    	String objectId = (String) paramMap.get("objectId");
    	String newAttrValue = (String) paramMap.get("New Value");

    	String timeZone       = (String) requestMap.get("timeZone");
        double clientTZOffset = new Double(timeZone).doubleValue();
        Locale locale = context.getLocale();

        if(ProgramCentralUtil.isNotNullString(objectId) &&
                ProgramCentralUtil.isNotNullString(newAttrValue)){
            risk.setId(objectId);
    		String riskActEndDate = eMatrixDateFormat.getFormattedInputDate(context,newAttrValue,clientTZOffset,locale);
    		String riskActStartDate = risk.getInfo(context,SELECT_RISK_ACTUAL_START_DATE);

            if(ProgramCentralUtil.isNullString(riskActStartDate)){
    			SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);

    			Calendar todayCalender = Calendar.getInstance(Locale.US);
                todayCalender.set(Calendar.HOUR, 0);
                todayCalender.set(Calendar.MINUTE, 0);
                todayCalender.set(Calendar.SECOND, 0);
                todayCalender.set(Calendar.MILLISECOND, 0);
                todayCalender.set(Calendar.AM_PM, Calendar.AM);

                riskActStartDate = sdf.format(todayCalender.getTime());

    			Date dtActEndDate = eMatrixDateFormat.getJavaDate(riskActEndDate);
    			Date dtActStartDate = eMatrixDateFormat.getJavaDate(riskActStartDate);

                if(dtActEndDate.before(dtActStartDate)){
                    riskActStartDate = riskActEndDate;
                    risk.setState(context, "Complete");
    				risk.setAttributeValue(context, risk.ATTRIBUTE_ACTUAL_START_DATE, riskActStartDate);
    				risk.setAttributeValue(context, risk.ATTRIBUTE_ACTUAL_END_DATE, riskActEndDate);
                }else{
                    risk.setState(context, "Complete");
    				risk.setAttributeValue(context, risk.ATTRIBUTE_ACTUAL_START_DATE, riskActStartDate);
    				risk.setAttributeValue(context, risk.ATTRIBUTE_ACTUAL_END_DATE, riskActEndDate);
                }
            }else{
    			Date dtEstimatedEndDate = eMatrixDateFormat.getJavaDate(riskActEndDate);
    			Date dtEstimatedStartDate = eMatrixDateFormat.getJavaDate(riskActStartDate);

                if(dtEstimatedEndDate.before(dtEstimatedStartDate)){
    				String sErrMsg = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,
                            "emxProgramCentral.Import.EndDateBeforeStartDate", context.getSession().getLanguage());
                    throw new MatrixException(sErrMsg);
                }else{
                    risk.setState(context, "Complete");
    				risk.setAttributeValue(context, risk.ATTRIBUTE_ACTUAL_END_DATE, riskActEndDate);
                }
            }
        }
    }

    /**
     * Allow user to edit risk object.
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about objects.
     * @return list of boolean value.
     * @throws Exception if operation fails.
     */
    public StringList hasAccessToEditRisk(Context context,String[]args)throws Exception
    {
    	StringList hasAccess = new StringList();

    	Map programMap = JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		String []riskIdArray = new String[objectList.size()];
        for(int i=0;i<objectList.size();i++){
			Map <String,String>objectMap = (Map) objectList.get(i);
			String riskId = objectMap.get(DomainObject.SELECT_ID);
            riskIdArray[i] = riskId;
        }
		StringList slSelect = new StringList(1);
		slSelect.addElement(DomainObject.SELECT_CURRENT);

		MapList riskList  = DomainObject.getInfo(context, riskIdArray, slSelect);

		for (Iterator iterator = riskList.iterator(); iterator.hasNext();) {
			Map <String,String>riskMap = (Map) iterator.next();
			String riskState = riskMap.get(DomainObject.SELECT_CURRENT);

            if(ProgramCentralUtil.isNotNullString(riskState)){
                if(riskState.equalsIgnoreCase("Complete")){
                    hasAccess.addElement(false);
                }else{
                    hasAccess.addElement(true);
                }
            }else{
                hasAccess.addElement(false);
            }
        }

        return hasAccess;
    }

    /**
     * When an Risk is created, grant the creator the proper
     * permissions on the Risk object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the assessment id
     * @throws Exception if operation fails
     */
    public void triggerCreateAction(Context context, String[] args)
        throws Exception
    {
        // get values from args
        String riskId = args[0]; // Budget ID
        String personId = com.matrixone.apps.domain.util.PersonUtil.getPersonObjectID(context);
        DomainAccess.createObjectOwnership(context, riskId, personId, "Project Lead", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
    }

    /**
     * Create dynamic multiple ownership command for risk object.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds information about object.
     * @return Map containing information about commands to dynamically generated.
     * @throws Exception if operation fails.
     */
    public  MapList getDynamicRiskCategory (Context context, String[]args )throws Exception
    {
    	MapList categoryMapList = new MapList();
        try{
    		Map inputMap = JPO.unpackArgs(args);
    		Map requestMap = (Map) inputMap.get("requestMap");
    		String objectId = (String)requestMap.get("objectId");

    		UIMenu uiMenu = new UIMenu();

    		Map multipleOwnershipCmdMap = ProgramCentralUtil.createDynamicCommand(context,"DomainAccessTreeCategory",uiMenu,true);
            String strHref = (String)multipleOwnershipCmdMap.get("href");

    		strHref = FrameworkUtil.findAndReplace(strHref, "DomainAccessToolBar", DomainObject.EMPTY_STRING);
    		strHref = FrameworkUtil.findAndReplace(strHref, "&editLink=true", DomainObject.EMPTY_STRING);
            multipleOwnershipCmdMap.put("href",strHref);

    		Map multipleOwnershipPageMap = ProgramCentralUtil.createDynamicCommand(context,"PMCAssignee",uiMenu,true);

    		String description = (String)multipleOwnershipCmdMap.get("description");
    		Map settingsMap = (Map)multipleOwnershipCmdMap.get("settings");
    		String name = (String)multipleOwnershipCmdMap.get("name");
    		String label = (String)multipleOwnershipCmdMap.get("label");
    		Map propertiesMap = (Map)multipleOwnershipCmdMap.get("properties");


            multipleOwnershipPageMap.put("href", strHref);
            multipleOwnershipPageMap.put("properties", propertiesMap);
            multipleOwnershipPageMap.put("settings", settingsMap);
            multipleOwnershipPageMap.put("description", description);
            multipleOwnershipPageMap.put("name", name);
            multipleOwnershipPageMap.put("label", label);

            categoryMapList.add(multipleOwnershipPageMap);

    	}catch (Exception e) {
            e.printStackTrace();
        }

        return categoryMapList;

    }

    /**
     * It Will Display people icon if person is attached to risk
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public StringList getColumnPersonIcon(Context context, String args[]) throws Exception {

		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

	       HashMap paramMap          = (HashMap) programMap.get("paramList");
           String exportFormat       = (String) paramMap.get("exportFormat");
           boolean isPrinterFriendly = false;
           Map paramList             = (Map) programMap.get("paramList");
           String PrinterFriendly    = (String) paramList.get("reportFormat");

           if(PrinterFriendly != null)
           {
               isPrinterFriendly = true;
           }

		StringList vecResult = new StringList();
		String strIcon = EMPTY_STRING;
        Map riskMap = null;
		String riskId = EMPTY_STRING;
		String strRelationshipPattern = RELATIONSHIP_ASSIGNED_RISK;
		String strTypePattern = TYPE_PERSON;

		StringList slBusSelect = new StringList();
		slBusSelect.add(SELECT_ID);
		slBusSelect.add(SELECT_NAME);

		StringList slRelSelect = new StringList();
        slRelSelect.add(DomainRelationship.SELECT_ID);
		boolean getTo = true; 
		boolean getFrom = false; 
		short recurseToLevel = 1;
		String strBusWhere = "";
		String strRelWhere = "";

		for(Object objectMap : objectList) {

            riskMap = (Map) objectMap;
            riskId = (String)riskMap.get("id");
			String strRelId = (String)riskMap.get(SELECT_RELATIONSHIP_ID); 
			DomainObject risk = DomainObject.newInstance(context,riskId);
			String objectType = risk.getInfo(context, SELECT_TYPE);
            //if(risk.isKindOf(context, TYPE_PROJECT_SPACE) || risk.isKindOf(context, TYPE_PERSON)){
				if(objectType.equalsIgnoreCase(TYPE_PROJECT_SPACE) || objectType.equalsIgnoreCase(TYPE_PERSON)) {
                vecResult.add("");
            } else {
				MapList mlRequestPersonList = risk.getRelatedObjects(context,strRelationshipPattern,
                                                                    strTypePattern,slBusSelect,
                                                                    slRelSelect,getTo,
                                                                    getFrom,recurseToLevel,
                                                                    strBusWhere,strRelWhere,0);
				int personInfoSize = mlRequestPersonList.size();
                if(mlRequestPersonList.size()>0) {
					StringBuffer strNameBuffer = new StringBuffer();
                    for (int i=0;i<personInfoSize;i++) {
						Map mapRequestPersonInfo = (Map) mlRequestPersonList.get(i);
						String personFullName = PersonUtil.getFullName(context,(String)mapRequestPersonInfo.get(SELECT_NAME)).replace(",", " ");
                        //personFullName = XSSUtil.encodeForHTML(context,(personFullName));
                         if((exportFormat != null)
                                    && (exportFormat.length() > 0)
                                    && ("CSV".equals(exportFormat)))
                            {
                             strNameBuffer.append(personFullName);
                            }
                         else{
                             strNameBuffer.append(XSSUtil.encodeForHTML(context,personFullName));
                         }

                        if(i<personInfoSize-1) {
                            strNameBuffer.append(", ");
                        }
                    }

					StringBuffer strHTMLBuffer = new StringBuffer();
                    strHTMLBuffer.append(strNameBuffer);
                    strIcon = strHTMLBuffer.toString();
                }
                else {
                    strIcon = "";
                }
				String strIconURL = FrameworkUtil.findAndReplace(strIcon,"&","&#38;");
                vecResult.add(strIcon);
            }
        }
        return vecResult;
    }

    /**
     * Retrieve Risks from both Project and Task Summary Page
     * @param context
     * @param args
     * @param busWhere
     * @return
     * @throws MatrixException
     */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getRiskAndRpn (Context context,String[] args, String busWhere)throws MatrixException {

            MapList riskRPNMapList = new MapList();
            MapList infoMapList = null;
            final String relWhere = DomainConstants.EMPTY_STRING;
            String strRelPattern = DomainConstants.EMPTY_STRING;
            String strTypePattern = DomainConstants.EMPTY_STRING;
            final String SELECT_RISK_TO_REL = "to["+DomainConstants.RELATIONSHIP_RISK_RPN+"].from.id";
            final String SELECT_RISK_FROM_REL = "from["+DomainConstants.RELATIONSHIP_RISK_RPN+"].to.id";
            final boolean getTo = false;
            final boolean getFrom = true;
            final StringList busSelectList = new StringList (8);
            busSelectList.add(DomainConstants.SELECT_ID);
            busSelectList.add(DomainConstants.SELECT_CURRENT);
            busSelectList.add(DomainConstants.SELECT_TYPE);
            busSelectList.add(DomainConstants.SELECT_NAME);
            busSelectList.add(SELECT_RISK_TO_REL);
            busSelectList.add(SELECT_RISK_FROM_REL);
            busSelectList.add(Risk.SELECT_RISK_IMPACT);
            busSelectList.add(Risk.SELECT_RISK_PROBABILITY);

            final StringList relSelectList = new StringList();
            relSelectList.add(DomainRelationship.SELECT_ID);
            relSelectList.add(DomainRelationship.SELECT_NAME);
            relSelectList.add(RiskRPNRelationship.SELECT_EFFECTIVE_DATE);
            relSelectList.add(RiskRPNRelationship.SELECT_RISK_IMPACT);
            relSelectList.add(RiskRPNRelationship.SELECT_RISK_PROBABILITY);
            relSelectList.add(RiskRPNRelationship.SELECT_RISK_RPN_VALUE);
            relSelectList.add(DomainRelationship.SELECT_ORIGINATED);

            final MapList riskMapList = new MapList();
            final StringList rpnMapList = new StringList();

            try {

                final HashMap programMap        = (HashMap) JPO.unpackArgs(args);
                final String objectId           = (String) programMap.get("objectId");

                // For Impact and Probability filtering (from Risk Grid)
                final String xValue             =  (String) programMap.get("xValue"); // Impact
                final String yValue             =  (String) programMap.get("yValue"); // Probability
                int iXValue = 0;    // Default values for all inclusion
                int iYValue = 0;
                // Default values for all inclusion
                if (UIUtil.isNotNullAndNotEmpty(xValue))
                    iXValue = Integer.parseInt(xValue);

                if (UIUtil.isNotNullAndNotEmpty(yValue))
                    iYValue = Integer.parseInt(yValue);


                final String importType         = PropertyUtil.getSchemaProperty(context,"type_Risk");
                ContextUtil.setAttribute(context, "importType", importType);
                final String selectedProgram = (String) programMap.get("selectedProgram");

                if((objectId == null) || objectId.equals("") || objectId.equals("null"))
                {
                    final Person person = Person.getPerson(context);

                      //page is called from 'myDesk'
                      //Retrieves a list of risks associated with an assignee (Person).
                      riskRPNMapList = Risk.getAssignedRisks(context, person, busSelectList, null, busWhere, null);
                      
               } else {
                   final DomainObject dmoParentObject = DomainObject.newInstance(context, objectId);

                   strRelPattern = DomainConstants.RELATIONSHIP_RISK+","+DomainConstants.RELATIONSHIP_RISK_RPN;
                   strTypePattern = DomainConstants.TYPE_RISK+","+DomainConstants.TYPE_RPN;
                   final String strExpandLevel = (String) programMap.get("expandLevel");
                   final short recurseToLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);

                   // Get the Project Direct Risks
                   infoMapList = dmoParentObject.getRelatedObjects(context, strRelPattern,
                                                                			strTypePattern,
                                                                			busSelectList,
                                                                			relSelectList,
                                                                			false,			// to
                                                                			true,			// from 
                                                                			recurseToLevel,
                                                                			busWhere, 
                                                                			relWhere,
                                                                			0);

                   final String typeName = dmoParentObject.getInfo(context, DomainConstants.SELECT_TYPE);

                   if(mxType.isOfParentType(context, typeName, DomainConstants.TYPE_PROJECT_SPACE)) //Modified for Subtype
                   {
                	   // context, Type, Application Name
                       final ProjectSpace project =  (ProjectSpace)DomainObject.newInstance(context,
                                                        DomainConstants.TYPE_PROJECT_SPACE,
                                                        DomainConstants.PROGRAM);
                       project.setId(objectId);
                       
                       // WE need to filter out the RPNs coming back from the Task query 
                       // if we are only looking at Risk
                       MapList mlTasksRisks = Risk.getProjectAllRisks(context, project, busWhere);
                       
                       if(("1").equalsIgnoreCase(strExpandLevel)){
                           for(int i = 0; i < mlTasksRisks.size(); i++) {
                               final Map objectMap = (Map) mlTasksRisks.get(i);
                               String sType = (String) objectMap.get(DomainConstants.SELECT_TYPE);
                               if (sType.equalsIgnoreCase(TYPE_RISK)) {
                            	   infoMapList.add(objectMap);                          	   
                               }
                           }
                       } else {
                    	   infoMapList.addAll(mlTasksRisks);
                       }
                       // End of Filter
                   }
                   
                   
                   // *************  Filtering for Grid Hyperlink **************                 
                   // Process the filter at this point - This assumes values coming off the objects
                   // This is a NO OP until the link starts working
                   
                   MapList mlFilter = new MapList(infoMapList.size());
                   mlFilter.addAll(infoMapList);
                   infoMapList.clear();
                   
                   // Dont Filter the RPN rel data - we want all values
                   for(int i = 0; i < mlFilter.size(); i++) {
                       final Map objectMap = (Map) mlFilter.get(i);
                       boolean bKeep = true;
                       final String objType = (String) objectMap.get(DomainConstants.SELECT_TYPE);
                       
                       if (objType.equalsIgnoreCase( DomainConstants.TYPE_RISK)) {
                    	   if ((iXValue > 0) && !(xValue.equalsIgnoreCase((String)objectMap.get(Risk.SELECT_RISK_IMPACT)))) {
                    		   bKeep = false;
                    	   }
                    	   if ((iYValue > 0) && !(yValue.equalsIgnoreCase((String)objectMap.get(Risk.SELECT_RISK_PROBABILITY)))) {
                    		   bKeep = false;
                    	   }
                       }
                       //TODO - Need to remove RPN where Risk was eliminated?
                	   if (bKeep)
                		   infoMapList.add(objectMap);

                   }
                   // *************  End Filtering **************

                   if(("1").equalsIgnoreCase(strExpandLevel)){
                       infoMapList.sort(DomainRelationship.SELECT_ORIGINATED,"descending", "date");
                  //   infoMapList.sort(RiskRPNRelationship.SELECT_EFFECTIVE_DATE,"descending", "date");

                       for(int i = 0; i < infoMapList.size(); i++) {
                           final Map    objectMap = (Map) infoMapList.get(i);
                           final String objType   = (String) objectMap.get(DomainConstants.SELECT_TYPE);
                           
                           if(objType.equalsIgnoreCase(DomainConstants.TYPE_RPN)){
                               objectMap.put("RowEditable", "readonly");
                           }
                       }
                       riskRPNMapList.addAll(infoMapList);

                   } else {

                       for(int i = 0; i < infoMapList.size(); i++) {
                           final Map objectMap  = (Map) infoMapList.get(i);
                           final String objType = (String) objectMap.get(DomainConstants.SELECT_TYPE);

                           if(objType.equalsIgnoreCase(DomainConstants.TYPE_RISK)){
                        	   
                               riskMapList.add(objectMap);
                               final Object riskRpn = objectMap.get(SELECT_RISK_FROM_REL);
                               StringList riskRpnId = new StringList();
                               String rpnId = DomainConstants.EMPTY_STRING;
                               if(riskRpn instanceof StringList){
                                   riskRpnId = (StringList)riskRpn;
                                   rpnId = (String)riskRpnId.get(0);
                               } else {
                                   rpnId = (String)riskRpn;
                               }
                           } else {
                        	   
                        	   final Map tempMap = new HashMap();
                               final Object riskRpn = objectMap.get(SELECT_RISK_TO_REL);
                               StringList riskRpnId1 = new StringList();
                               String riskId = DomainConstants.EMPTY_STRING;

                               if(riskRpn instanceof StringList){
                                   riskRpnId1 = (StringList)riskRpn;
                                   riskId = (String)riskRpnId1.get(0);
                               } else {
                                   riskId = (String)riskRpn;
                               }
                               tempMap.put(riskId, objectMap);
                               rpnMapList.add(tempMap);
                           }
                       } // End of FOR

                       for (final Object mapObject : riskMapList) {

                           final Map riskMap   = (Map) mapObject;
                           final MapList resultList = new MapList();
                           resultList.add(riskMap);
                           final String riskId = (String)riskMap.get(DomainConstants.SELECT_ID);
                           final MapList RPNMapList = new MapList();

                           for(int i = 0; i<rpnMapList.size(); i++){
                               final Map  rnpsMap = (Map)rpnMapList.get(i);
                               final Map  rnpsMap1 = (Map)rnpsMap.get(riskId);
                               if(rnpsMap1!=null){
                                   RPNMapList.add(rnpsMap1);
                               }
                           }

                           RPNMapList.sort(DomainRelationship.SELECT_ORIGINATED,"descending", "date");
                         //RPNMapList.sort(RiskRPNRelationship.SELECT_EFFECTIVE_DATE,"descending", "date");
                           final MapList rpnList = new MapList();

                           for(int j=0; j<RPNMapList.size(); j++){
                               final Map RPNMap = (Map)RPNMapList.get(j);
                               if(j==0){
                                   RPNMap.put("isRPNEditable", "true");
                                   rpnList.add(RPNMap);
                               } else {
                                   RPNMap.put("RowEditable", "readonly");
                                   rpnList.add(RPNMap);
                               }
                           }
                           resultList.addAll(RPNMapList);
                           riskRPNMapList.addAll(resultList);
                       }
                   } // End of ELSE
               }
               return riskRPNMapList;

            } catch(final Exception exception) {
                exception.printStackTrace();
                throw new MatrixException(exception);
            }
        }

        public StringList getRiskRpnName(Context context, String[] args) throws MatrixException {
	    	StringList riskRPNName = new StringList();
            try{
	    		Map programMap         = (HashMap) JPO.unpackArgs(args);
	    		MapList objectList     = (MapList) programMap.get("objectList");
	    		Map paramList          = (HashMap) programMap.get("paramList");
	    		String jsTreeID        = (String) paramList.get("jsTreeID");
	    		String strLanguage     = (String) paramList.get("languageStr");
	    		String exportFormat    = (String) paramList.get("exportFormat");
	    	    String PrinterFriendly = (String) paramList.get("reportFormat");
	    		String key = "emxFramework.Relationship.Risk_RPN";

                boolean isPrinterFriendly = false;
                 if(PrinterFriendly != null) {
                                isPrinterFriendly = true;
                            }

	    		StringList slBusSelect = new StringList();
	    		slBusSelect.add(SELECT_ID);
	    		slBusSelect.add(SELECT_TYPE);
	    		slBusSelect.add(SELECT_NAME);
	    		slBusSelect.add(SELECT_CURRENT);
	    		slBusSelect.add(SELECT_POLICY);

	    		for (Object objectMap : objectList) {

	    			Map riskRPNMap = (Map)objectMap;
	    			String objectId = (String) riskRPNMap.get(SELECT_ID);
	    			DomainObject riskObject = DomainObject.newInstance(context);
                    riskObject.setId(objectId);
	    			Map riskObjectInfo = riskObject.getInfo(context, slBusSelect);
	    			String objectName = (String)riskObjectInfo.get(SELECT_NAME);
	    			String objectType = (String)riskObjectInfo.get(SELECT_TYPE);

                    if(ProgramCentralUtil.isNotNullString(objectId)) {
                        if((ProgramCentralUtil.isNotNullString(exportFormat) && ("CSV".equals(exportFormat))) || (isPrinterFriendly == true))
                        {
                            riskRPNName.add(objectName);
                        }
                        else {

                    objectId   = XSSUtil.encodeForURL(context, objectId);
                    objectName = XSSUtil.encodeForHTML(context,objectName);

	    			if(objectType.equals(TYPE_RPN) ) {
	    				String convertedName =  EnoviaResourceBundle.getProperty(context, "Framework",key, strLanguage);
                        riskRPNName.add(XSSUtil.encodeForHTML(context,convertedName));
	    			} else if(objectType.equals(TYPE_RISK)){
	    				String nextURL = "../common/emxTree.jsp?mode=insert&amp;objectId=";
                        riskRPNName.add("<a href=\""+ nextURL + objectId +"\">"+ objectName +"</a>");
                    } else {
	    				String nextURL = "../common/emxTree.jsp?mode=insert&amp;objectId=";
                        riskRPNName.add("<a href=\""+ nextURL + objectId +"\">"+ objectName +"</a>");
                    }
                        }
                }
                }
	    	} catch(Exception exception) {
                throw new MatrixException(exception);
            }
            return riskRPNName;
        }

        public StringList makeRiskCellEditable(Context context, String[] argumentArray) throws MatrixException {
             boolean hasAccess = false;
            try {
	            Map programMap = JPO.unpackArgs(argumentArray);
	            List<Map<String,String>> objectList = (List<Map<String,String>>) programMap.get("objectList");
	            StringList editAccessList = new StringList(objectList.size());

	            for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();){
	                Map objectInfo = (Map) itrTableRows.next();
	                String riskState = (String) objectInfo.get(SELECT_CURRENT);
	                String riskLevel = (String) objectInfo.get("level");

                    if(ProgramCentralUtil.isNotNullString(riskState)) {
                        if(riskState.equalsIgnoreCase("Complete")) {
                            hasAccess = false;
                        } else {
                            hasAccess = true;
                        }
                    }
                    if(hasAccess) {
                        editAccessList.add("1".equalsIgnoreCase(riskLevel));
                    } else {
                        editAccessList.add(false);
                    }
                    }

                return editAccessList;

	        } catch (Exception exception) {
                throw new MatrixException(exception);
            }
        }

        public StringList makeRPNCellEditable(Context context, String[] argumentArray) throws MatrixException {

            try {
	            Map programMap = JPO.unpackArgs(argumentArray);
	            List<Map<String,String>> objectList = (List<Map<String,String>>) programMap.get("objectList");
	            StringList editAccessList = new StringList(objectList.size());
                boolean hasAccess = true;
	            for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();){
	                Map riskInfo = (Map) itrTableRows.next();
	                String objectId = (String) riskInfo.get(SELECT_ID);
	                String objectLevel = (String) riskInfo.get("level");

	            	DomainObject domainObject = DomainObject.newInstance(context, objectId);
	            	if(domainObject.isKindOf(context, TYPE_RPN)){
	            	String riskId = domainObject.getInfo(context,"to["+RELATIONSHIP_RISK_RPN+"].from.id");

	                DomainObject risk = DomainObject.newInstance(context, riskId);
	                String riskState = (String) risk.getInfo(context, (SELECT_CURRENT));

                    if(ProgramCentralUtil.isNotNullString(riskState)){
                        if(riskState.equalsIgnoreCase("Complete")){
                            hasAccess = false;
                        }else{
                            hasAccess = true;
                        }
                    }

                    if(hasAccess) {
                        editAccessList.add("2".equalsIgnoreCase(objectLevel));
                    } else {
                        editAccessList.add(false);
                    }
                } else {
                    editAccessList.add(false);
                    }
                }
                return editAccessList;

	    	}catch(Exception exception) {
                throw new MatrixException(exception);
            }
        }

    /**
     * Exclude Experiment project from search timesheet and project list page.
     * @param context the ENOVIA <code>Context</code> object.
     * @param args holds information about object.
     * @return list of project except experiment.
     * @throws Exception, if operation fails.
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeExistingRisks(Context context,String[]args)throws Exception {

		StringList slFinalList = new StringList();
        MapList riskList = new MapList();
        MapList mlTasksRisks = new MapList();
		StringList busSelects = new StringList(1);
		busSelects.add(Risk.SELECT_ID);
		String busWhere = EMPTY_STRING;
		String SELECT_TASK_PARENT_TYPE = "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";

        try {
			Map programMap 	= (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");

			ProjectSpace project = (ProjectSpace) DomainObject .newInstance(context, TYPE_PROJECT_SPACE, PROGRAM);
            project.setId(objectId);

			StringList objectSelect = new StringList();
			objectSelect.add(SELECT_TYPE);
            objectSelect.add(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);
            objectSelect.add(SELECT_TASK_PARENT_TYPE);
			Map objectInfo = project.getInfo(context, objectSelect);
			String typeName =(String)objectInfo.get(SELECT_TYPE);
			String projectId =(String)objectInfo.get(ProgramCentralConstants.SELECT_TASK_PROJECT_ID);
			String parentType =(String)objectInfo.get(SELECT_TASK_PARENT_TYPE);
			boolean isOfTaskType = mxType.isOfParentType(context, typeName,DomainConstants.TYPE_TASK_MANAGEMENT);

			if(TYPE_PROJECT_SPACE.equals(typeName) || (isOfTaskType == true && !parentType.equalsIgnoreCase(ProgramCentralConstants.TYPE_PROJECT_CONCEPT))){
				String typePattern = TYPE_RISK;
				String vaultPattern = QUERY_WILDCARD;
				StringList objectSelects = new StringList();
				objectSelects.add(SELECT_ID);
				String SELECT_TASK_PROJECT_CONCEPT_TYPE = "to["+RELATIONSHIP_RISK+"].from.to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
				String objectWhere = "('"+SELECT_TASK_PROJECT_CONCEPT_TYPE+"' == '"+TYPE_PROJECT_CONCEPT+"' || to["+RELATIONSHIP_RISK+"].from.type == '"+TYPE_PROJECT_CONCEPT+"')";

				MapList allRisk = DomainObject.findObjects(context, typePattern, vaultPattern, objectWhere, objectSelects);
				Iterator allRiskListIterator = allRisk.iterator();
                while(allRiskListIterator.hasNext()){
					Map riskMap = (Map)allRiskListIterator.next();
					slFinalList.add((String)riskMap.get(SELECT_ID));
                }
            }

			if(TYPE_PROJECT_SPACE.equals(typeName)|| mxType.isOfParentType(context, typeName,DomainConstants.TYPE_PROJECT_SPACE)) {
                project.setId(objectId);

            } else if(isOfTaskType == true) {

                project.setId(projectId);
            }

            riskList = Risk.getRisks(context, project, busSelects,null, busWhere);
            mlTasksRisks = Risk.getProjectAllRisks(context, project, busWhere);
            riskList.addAll(mlTasksRisks);

			Iterator riskListIterator = riskList.iterator();

            while(riskListIterator.hasNext()){
				Map riskMap = (Map)riskListIterator.next();
				slFinalList.add((String)riskMap.get(SELECT_ID));
            }

            return slFinalList;
		} catch(Exception e) {
            e.printStackTrace();
            throw new MatrixException(e);
        }
    }

    public Object displayRiskAttachmentCheckbox(Context context, String[] args) throws Exception {

    	StringList attachmentCheckboxList = new StringList();
    	StringList slBusSelect = new StringList();
   		slBusSelect.add(SELECT_ID);
   		slBusSelect.add(SELECT_NAME);

   		StringList slRelSelect = new StringList();
   		slRelSelect.add(SELECT_ID);
   		boolean getTo = false; 
   		boolean getFrom = true; 
   		short recurseToLevel = 1;
   		String strBusWhere = EMPTY_STRING;
   		String strRelWhere = EMPTY_STRING;
    	HashMap  programMap = (HashMap) JPO.unpackArgs(args);
    	MapList  objectList = (MapList) programMap.get("objectList");

    	Iterator objectListIterator = objectList.iterator();
        while (objectListIterator.hasNext()) {

    		Map objectMap = (Map) objectListIterator.next();
    		String strId = (String) objectMap.get(SELECT_ID);
    		Risk risk = (Risk) DomainObject.newInstance(context, TYPE_RISK, PROGRAM);
            risk.setId(strId);

       		MapList riskDocList = risk.getRelatedObjects(context,RELATIONSHIP_REFERENCE_DOCUMENT,
    						DomainObject.TYPE_DOCUMENT,slBusSelect,
                            slRelSelect,getTo,
                            getFrom,recurseToLevel,
                            strBusWhere,strRelWhere,0);

    		StringBuffer ckbColumn = new StringBuffer();
            if(!riskDocList.isEmpty()){
                ckbColumn.append("<input type=\"checkbox\" name=\"" + strId + "\" value=\"true\" />");
            } else {
                ckbColumn.append("<input type=\"checkbox\" name=\"" + strId + "\" disabled=\"disabled\" value=\"false\" />");
            }
            attachmentCheckboxList.add(ckbColumn.toString());
        }
        return attachmentCheckboxList;
    }

    public StringList getEffectiveDateValue(Context context, String[] args)throws Exception {
    	StringList finalEffectiveDateList = new StringList();
    	Map programMap               = (HashMap) JPO.unpackArgs(args);
            MapList objectList          = (MapList) programMap.get("objectList");
            Iterator objectListIterator = objectList.iterator();
            String[] objIdArr           = new String[objectList.size()];
            int arrayCount              = 0;
        Map objectMap               = null;
		String sOriginatedDates = DomainObject.EMPTY_STRING;
		String effectiveDate = DomainObject.EMPTY_STRING;

            while(objectListIterator.hasNext()) {
                objectMap                = (Map) objectListIterator.next();
                objIdArr[arrayCount]     = (String) objectMap.get(DomainConstants.SELECT_ID);
                arrayCount++;
            }
		StringList busSelect = new StringList();
		busSelect.add(SELECT_ORIGINATED_DATES);
		busSelect.add(SELECT_EFFECTIVE_DATES);
		MapList riskInfoList = DomainObject.getInfo(context, objIdArr,busSelect);

		for (Iterator iterator = riskInfoList.iterator(); iterator.hasNext();) {
            objectMap = (Map) iterator.next();
            StringList slOriginatedDates = new StringList();
			sOriginatedDates = (String)objectMap.get(SELECT_ORIGINATED_DATES);
			slOriginatedDates = FrameworkUtil.split(sOriginatedDates, "");
			String latestOriginated = getLatestOrignated(slOriginatedDates);
			int latestOriginatedIndex =  slOriginatedDates.indexOf(latestOriginated);

            StringList slEffectiveDates = new StringList();
			slEffectiveDates = (StringList)objectMap.get(SELECT_EFFECTIVE_DATES);
            effectiveDate = (String) slEffectiveDates.get(latestOriginatedIndex);
            finalEffectiveDateList.add(effectiveDate);
            }
        return finalEffectiveDateList;
    }
}

